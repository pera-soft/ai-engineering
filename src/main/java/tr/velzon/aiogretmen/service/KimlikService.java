package tr.velzon.aiogretmen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.velzon.aiogretmen.config.SiteAyarlari;
import tr.velzon.aiogretmen.repo.KullaniciRepo;
import tr.velzon.aiogretmen.repo.KullaniciTokenRepo;
import tr.velzon.aiogretmen.types.KayitGecersizException;
import tr.velzon.aiogretmen.types.Kullanici;
import tr.velzon.aiogretmen.types.KullaniciToken;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * {@code site} profili hesap işlemleri (Faz 9a, ADR-0012): kayıt ({@code @pera.software} zorunlu),
 * e-posta doğrulama, kaba kuvvet sayacı; Faz 9c (ADR-0013): doğrulama e-postasını yeniden gönderme ve
 * şifre sıfırlama. Giriş Spring Security form login'dir ({@link KullaniciDetayServisi} + {@link GirisDenemeTakibi}).
 *
 * <p><strong>Kayıt yöntemi (ADR-0015):</strong> {@code app.site.kayit=ekip-kodu} (varsayılan) iken kayıt ortak
 * ekip koduyla yapılır, hesap doğrulanmış açılır ve HİÇ e-posta gönderilmez (doğrulama/yeniden gönder/şifremi
 * unuttum kapalı; şifre sıfırlama bağlantısını site sorumlusu {@code tools/sifre-baglantisi.sh} ile üretir).
 * Kod yoksa ya da {@value SiteAyarlari#MIN_EKIP_KODU} karakterden kısaysa site AÇILMAZ. {@code eposta} modu
 * Faz 9c akışının aynısıdır.
 *
 * <p><strong>E-posta gönderimi isteği bozmaz:</strong> gönderici hatası yutulur ve sırsız loglanır; hız
 * sınırı ({@link EpostaHizSiniri}) doluysa yeni token ÜRETİLMEZ (önceki bağlantı geçerli kalır).
 */
@Service
@Profile("site")
public class KimlikService {

    private static final Logger log = LoggerFactory.getLogger(KimlikService.class);

    public static final String ALAN_ADI = "@pera.software";
    public static final int MIN_SIFRE = 10;
    /** BCrypt yalnız ilk 72 BAYTI kullanır (Spring 6.3+ daha uzununu reddeder) — açıkça sınırla. */
    public static final int MAX_SIFRE_BAYT = 72;
    public static final int MAX_DENEME = 5;
    public static final Duration KILIT_SURESI = Duration.ofMinutes(15);
    public static final Duration DOGRULAMA_SURESI = Duration.ofHours(24);
    public static final Duration SIFIRLAMA_SURESI = Duration.ofHours(1);

    /** Yerel kısım: harf/rakam ve . _ % + - ; alan adı birebir pera.software (alt alan adı YOK). */
    private static final Pattern EPOSTA = Pattern.compile("^[a-z0-9._%+-]{1,64}@pera\\.software$");
    private static final SecureRandom RASTGELE = new SecureRandom();

    private final KullaniciRepo kullaniciRepo;
    private final KullaniciTokenRepo tokenRepo;
    private final PasswordEncoder sifreKodlayici;
    private final EpostaGonderici eposta;
    private final EpostaHizSiniri hizSiniri;
    private final String temelUrl;
    private final boolean epostaKaydi;
    /** {@code ekip-kodu} modunda beklenen kodun UTF-8 baytları (kırpılmış); {@code eposta} modunda {@code null}. */
    private final byte[] ekipKodu;

    public KimlikService(KullaniciRepo kullaniciRepo, KullaniciTokenRepo tokenRepo,
                         PasswordEncoder sifreKodlayici, EpostaGonderici eposta, EpostaHizSiniri hizSiniri,
                         SiteAyarlari ayarlar) {
        this.kullaniciRepo = kullaniciRepo;
        this.tokenRepo = tokenRepo;
        this.sifreKodlayici = sifreKodlayici;
        this.eposta = eposta;
        this.hizSiniri = hizSiniri;
        String url = ayarlar.temelUrl() == null ? "" : ayarlar.temelUrl().trim();
        this.temelUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.epostaKaydi = ayarlar.epostaKaydi();
        if (epostaKaydi) {
            this.ekipKodu = null;
        } else {
            String kod = ayarlar.ekipKodu() == null ? "" : ayarlar.ekipKodu().trim();
            if (kod.length() < SiteAyarlari.MIN_EKIP_KODU) { // değer mesaja/loga YAZILMAZ
                throw new IllegalStateException("site profili ekip kodu olmadan açılamaz: APP_SITE_EKIP_KODU en az "
                        + SiteAyarlari.MIN_EKIP_KODU + " karakter olmalı (üret: openssl rand -base64 18); "
                        + "e-postalı kayıt için APP_SITE_KAYIT=eposta — ADR-0015");
            }
            this.ekipKodu = kod.getBytes(StandardCharsets.UTF_8);
        }
    }

    /** {@code true}: e-postalı kayıt (Faz 9c); {@code false}: ekip kodu (ADR-0015, varsayılan). */
    public boolean epostaKaydi() {
        return epostaKaydi;
    }

    /** Kimlik normalleştirme: kırp + küçük harf (Locale.ROOT — Türkçe İ/ı dönüşümü e-postayı bozmasın). */
    public static String normallestir(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Yeni hesap açar. {@code eposta} modunda hesap doğrulanmamış açılır ve doğrulama bağlantısı gönderilir;
     * {@code ekip-kodu} modunda önce ekip kodu (sabit zamanlı) denetlenir, hesap doğrulanmış açılır, e-posta yok.
     *
     * <p><strong>Hesap varlığı sızdırılmaz:</strong> adres zaten kayıtlıysa da aynı başarılı yanıt
     * döner. Kayıtlı ama doğrulanmamışsa şifre güncellenir (eposta: yeni bağlantı gönderilir, eski
     * bağlantı geçersizleşir; ekip-kodu: hesap doğrulanmış olur); doğrulanmışsa hiçbir şey değişmez
     * (şifre ele geçirilemez).
     *
     * @param hamEkipKodu yalnız {@code ekip-kodu} modunda bakılır
     * @throws KayitGecersizException ekip kodu yanlış/eksik, alan adı/şifre kuralı ihlalinde
     */
    @Transactional
    public void kayit(String hamEmail, String sifre, String hamEkipKodu) {
        if (!epostaKaydi) {
            ekipKoduDenetle(hamEkipKodu); // koddan ÖNCE hiçbir kural/hesap bilgisi dönmez
        }
        String email = normallestir(hamEmail);
        if (!EPOSTA.matcher(email).matches()) {
            throw new KayitGecersizException("Yalnız " + ALAN_ADI + " uzantılı şirket e-postasıyla kayıt olunabilir.");
        }
        sifreKurali(sifre);

        Optional<Kullanici> mevcut = kullaniciRepo.findByEmail(email);
        if (!epostaKaydi) {
            ekipKoduylaHesapAc(email, sifre, mevcut);
            return;
        }
        Kullanici hesap;
        if (mevcut.isEmpty()) {
            hesap = kullaniciRepo.save(Kullanici.yeni(email, sifreKodlayici.encode(sifre)));
        } else if (!mevcut.get().dogrulandi()) {
            hesap = kullaniciRepo.save(mevcut.get().sifreyle(sifreKodlayici.encode(sifre)));
        } else {
            log.info("Zaten doğrulanmış hesaba yeniden kayıt denemesi: {}", email);
            return;
        }
        dogrulamaBaglantisiUret(hesap);
    }

    /** Ekip kodu modunda hesap: yeni → doğrulanmış açılır; doğrulanmamış (eski e-posta dönemi) → şifre + doğrulandı. */
    private void ekipKoduylaHesapAc(String email, String sifre, Optional<Kullanici> mevcut) {
        if (mevcut.isEmpty()) {
            kullaniciRepo.save(Kullanici.yeniDogrulanmis(email, sifreKodlayici.encode(sifre)));
            log.info("Ekip koduyla hesap açıldı: {}", email);
        } else if (!mevcut.get().dogrulandi()) {
            Kullanici hesap = kullaniciRepo.save(mevcut.get().sifreyle(sifreKodlayici.encode(sifre)));
            kullaniciRepo.dogrulandiIsaretle(hesap.id());
            log.info("Doğrulanmamış hesap ekip koduyla açıldı: {}", email);
        } else {
            log.info("Zaten doğrulanmış hesaba yeniden kayıt denemesi: {}", email);
        }
    }

    /** Sabit zamanlı karşılaştırma (süre yalnız BEKLENEN kodun uzunluğuna bağlı); kod değeri loglanmaz. */
    private void ekipKoduDenetle(String hamEkipKodu) {
        byte[] verilen = (hamEkipKodu == null ? "" : hamEkipKodu.trim()).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(ekipKodu, verilen)) {
            log.warn("Kayıt reddedildi: ekip kodu yanlış ya da eksik");
            throw new KayitGecersizException("Ekip kodu hatalı. Kodu site sorumlusundan al.");
        }
    }

    /**
     * "Doğrulama e-postasını yeniden gönder" (Faz 9c). Yalnız var olan, doğrulanmamış, etkin hesaba yeni
     * bağlantı gider (eskisi geçersizleşir); diğer her durumda sessizce hiçbir şey yapılmaz — çağıran
     * her zaman aynı nötr yanıtı döner (hesap varlığı sızmaz). Hız sınırı kayıtla ortaktır.
     */
    @Transactional
    public void dogrulamaYenidenGonder(String hamEmail) {
        String email = normallestir(hamEmail);
        if (!epostaKaydi || !EPOSTA.matcher(email).matches()) {
            return;
        }
        kullaniciRepo.findByEmail(email)
                .filter(k -> !k.dogrulandi() && k.etkin())
                .ifPresent(this::dogrulamaBaglantisiUret);
    }

    /**
     * "Şifremi unuttum" (Faz 9c). Yalnız doğrulanmış + etkin hesaba 1 saatlik, tek kullanımlık sıfırlama
     * bağlantısı gider (önceki sıfırlama bağlantıları geçersizleşir). Bilinmeyen/uygunsuz adreste hiçbir
     * şey yapılmaz; çağıran her zaman aynı nötr yanıtı döner.
     */
    @Transactional
    public void sifreSifirlamaIste(String hamEmail) {
        String email = normallestir(hamEmail);
        if (!epostaKaydi || !EPOSTA.matcher(email).matches()) {
            return;
        }
        Optional<Kullanici> hesap = kullaniciRepo.findByEmail(email).filter(k -> k.dogrulandi() && k.etkin());
        if (hesap.isEmpty()) {
            return;
        }
        if (!hizSiniri.izinVer(KullaniciToken.SIFRE_SIFIRLAMA, email)) {
            log.warn("Şifre sıfırlama e-postası hız sınırında ({} / saat): {}", EpostaHizSiniri.EN_COK, email);
            return;
        }
        String token = tokenUret(hesap.get().id(), KullaniciToken.SIFRE_SIFIRLAMA, SIFIRLAMA_SURESI);
        guvenliGonder(KullaniciToken.SIFRE_SIFIRLAMA, email, () -> eposta.sifreSifirlamaBaglantisiGonder(email,
                temelUrl + "/sifre-sifirla.html?token=" + token));
    }

    /**
     * Sıfırlama bağlantısındaki token'la yeni şifre koyar (Faz 9c). Şifre kuralı token'dan ÖNCE denetlenir
     * (kural ihlalinde token harcanmaz). Başarıda: şifre güncellenir, hatalı deneme sayacı + kilit temizlenir,
     * hesabın tüm sıfırlama token'ları silinir. Oturumları düşürmek çağıranın (web) işidir.
     *
     * @return şifresi değişen hesabın e-postası; token yok/kullanılmış/süresi dolmuş ya da hesap uygun
     *         değilse boş
     * @throws KayitGecersizException şifre kuralı ihlalinde
     */
    @Transactional
    public Optional<String> sifreSifirla(String hamToken, String yeniSifre) {
        sifreKurali(yeniSifre);
        if (hamToken == null || hamToken.isBlank()) {
            return Optional.empty();
        }
        String hash = ozet(hamToken);
        Optional<KullaniciToken> token = tokenRepo.findByTokenHashAndTur(hash, KullaniciToken.SIFRE_SIFIRLAMA);
        // tek kullanımlık: eşzamanlı iki istekten yalnız satırı silebilen devam eder
        if (token.isEmpty() || tokenRepo.tokenSil(hash) == 0) {
            return Optional.empty();
        }
        KullaniciToken t = token.get();
        tokenRepo.hesabinTokenlariniSil(t.hesapId(), KullaniciToken.SIFRE_SIFIRLAMA);
        if (!t.sonGecerlilik().isAfter(Instant.now())) {
            return Optional.empty();
        }
        Optional<Kullanici> hesap = kullaniciRepo.findById(t.hesapId()).filter(k -> k.dogrulandi() && k.etkin());
        if (hesap.isEmpty()) {
            return Optional.empty();
        }
        kullaniciRepo.sifreSifirla(t.hesapId(), sifreKodlayici.encode(yeniSifre));
        log.info("Şifre sıfırlandı: {}", hesap.get().email());
        return Optional.of(hesap.get().email());
    }

    /**
     * Doğrulama bağlantısındaki token'ı tüketir.
     *
     * @return token geçerliyse (var, süresi dolmamış) {@code true}; hesap doğrulanır ve token silinir.
     */
    @Transactional
    public boolean dogrula(String hamToken) {
        if (!epostaKaydi || hamToken == null || hamToken.isBlank()) {
            return false;
        }
        Optional<KullaniciToken> token = tokenRepo.findByTokenHashAndTur(ozet(hamToken), KullaniciToken.DOGRULAMA);
        if (token.isEmpty()) {
            return false;
        }
        KullaniciToken t = token.get();
        tokenRepo.hesabinTokenlariniSil(t.hesapId(), KullaniciToken.DOGRULAMA); // tek kullanımlık
        if (!t.sonGecerlilik().isAfter(Instant.now())) {
            return false;
        }
        kullaniciRepo.dogrulandiIsaretle(t.hesapId());
        return true;
    }

    /** Yanlış şifre: sayaç +1; {@value #MAX_DENEME}. hatada {@link #KILIT_SURESI} kilit. Bilinmeyen e-posta: no-op. */
    @Transactional
    public void basarisizGiris(String hamEmail) {
        String email = normallestir(hamEmail);
        if (kullaniciRepo.basarisizDenemeArtir(email) == 0) {
            return;
        }
        kullaniciRepo.findByEmail(email)
                .filter(k -> k.basarisizDeneme() >= MAX_DENEME)
                .ifPresent(k -> {
                    kullaniciRepo.kilitle(email, Instant.now().plus(KILIT_SURESI));
                    log.warn("Hesap {} art arda {} hatalı giriş nedeniyle {} dk kilitlendi", email, MAX_DENEME,
                            KILIT_SURESI.toMinutes());
                });
    }

    @Transactional
    public void basariliGiris(String hamEmail) {
        kullaniciRepo.girisBasarili(normallestir(hamEmail));
    }

    private static void sifreKurali(String sifre) {
        if (sifre == null || sifre.length() < MIN_SIFRE) {
            throw new KayitGecersizException("Şifre en az " + MIN_SIFRE + " karakter olmalı.");
        }
        if (sifre.getBytes(StandardCharsets.UTF_8).length > MAX_SIFRE_BAYT) {
            throw new KayitGecersizException("Şifre çok uzun (en fazla " + MAX_SIFRE_BAYT + " bayt).");
        }
    }

    /** Hız sınırı izin verirse yeni doğrulama token'ı üretir (eskiler geçersiz) ve gönderir. */
    private void dogrulamaBaglantisiUret(Kullanici hesap) {
        if (!hizSiniri.izinVer(KullaniciToken.DOGRULAMA, hesap.email())) {
            log.warn("Doğrulama e-postası hız sınırında ({} / saat): {}", EpostaHizSiniri.EN_COK, hesap.email());
            return;
        }
        String token = tokenUret(hesap.id(), KullaniciToken.DOGRULAMA, DOGRULAMA_SURESI);
        guvenliGonder(KullaniciToken.DOGRULAMA, hesap.email(), () -> eposta.dogrulamaBaglantisiGonder(hesap.email(),
                temelUrl + "/api/kimlik/dogrula?token=" + token));
    }

    /** Hesabın o türdeki eski token'larını siler, yenisini (yalnız özetini) kaydeder; ham token'ı döner. */
    private String tokenUret(long hesapId, String tur, Duration sure) {
        tokenRepo.hesabinTokenlariniSil(hesapId, tur); // eski bağlantılar geçersiz
        byte[] ham = new byte[32];
        RASTGELE.nextBytes(ham);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(ham);
        tokenRepo.save(KullaniciToken.yeni(hesapId, tur, ozet(token), Instant.now().plus(sure)));
        return token;
    }

    /** Gönderici hatası isteği/işlemi BOZMAZ; bağlantı (sır) loglanmaz. */
    private static void guvenliGonder(String tur, String email, Runnable gonderim) {
        try {
            gonderim.run();
        } catch (RuntimeException e) {
            log.warn("E-posta gönderilemedi (tür={}, alıcı={}): {}", tur, email, e.getClass().getSimpleName());
        }
    }

    /**
     * SHA-256 (küçük harf hex, UTF-8 baytları üzerinde). Token 256-bit rastgele olduğundan tuz/yavaş özet
     * gerekmez. {@code tools/sifre-baglantisi.sh} AYNI biçimi üretir ({@code printf %s token | sha256sum}) —
     * değiştirirsen betiği de değiştir (KimlikServiceOzetTest bu sözleşmeyi sabitler).
     */
    static String ozet(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 yok", e);
        }
    }
}
