package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import tr.velzon.aiogretmen.config.SiteAyarlari;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.service.HizSiniri;
import tr.velzon.aiogretmen.service.ZiyaretciService;
import tr.velzon.aiogretmen.types.HizSiniriAsildiException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Girişsiz sitede (Faz 10, ADR-0017) kötüye kullanıma karşı IP/ziyaretçi hız sınırları; aşımda
 * {@link HizSiniriAsildiException} → 429. Sınır sayıları (ve varsayılanları) {@link SiteAyarlari.Hiz}'dan.
 * Yalnız YAZAN istekler ({@code POST/PUT/DELETE /api/**}) sayılır.
 * İstemci IP'si {@code getRemoteAddr()}'dır ({@code server.forward-headers-strategy: native} nginx'in
 * X-Forwarded-For'unu çözer).
 *
 * <p>{@link ZiyaretciDokunmaInterceptor}'dan ÖNCE çalışır ({@code order(-1)}): reddedilen istek ziyaretçi satırı
 * açmamalı. Yeni ziyaretçi sınırı, kaydı henüz olmayan ziyaretçinin ilk yazmalarını IP başına sayar.
 *
 * <p>İki aşama (tech-debt #15): önce isteğe uyan TÜM kovalara tüketmeden bakılır ({@link HizSiniri#bekleme});
 * biri doluysa 429 + {@code Retry-After} (kovaların en uzun kalan süresi, saniye, en az 1) ve HİÇBİR kovaya
 * yazılmaz — reddedilen geri-yükleme yazma kovasını tüketmez. Hepsi izin veriyorsa sırayla yazılır.
 *
 * <p>Testler: sayaçlar uygulama bağlamı boyunca yaşar; önbelleklenmiş Spring bağlamını paylaşan testler aynı
 * {@code HizSiniri}'ni görür (bugün her site test sınıfı ayrık H2 URL'siyle kendi bağlamını kurar, varsayılan
 * 127.0.0.1 kovası sınıf içinde kalır). Prod yüzeyini büyütecek bir {@code sifirla()} YOK; sınıra yaklaşan ya da
 * bağlam paylaşan site testleri kendine ayrık {@code remoteAddr} (ör. 10.0.x.y) ve çerez kimliği kullanmalı.
 */
@Component
@Profile("site")
public class HizSiniriInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    // Sınır sayıları SiteAyarlari.Hiz'dan (app.site.hiz.*, ortamda APP_SITE_HIZ_*); pencereler sabit.
    static final Duration YAZMA_PENCERE = Duration.ofMinutes(1);
    static final Duration SOHBET_PENCERE = Duration.ofMinutes(1);
    static final Duration GERI_YUKLE_PENCERE = Duration.ofHours(1);
    static final Duration YENI_ZIYARETCI_PENCERE = Duration.ofHours(1);

    private static final Set<String> YAZMA_YONTEMLERI = Set.of("POST", "PUT", "DELETE");

    private final HizSiniri hizSiniri;
    private final ZiyaretciService ziyaretciService;
    private final SiteAyarlari.Hiz sinirlar;

    public HizSiniriInterceptor(HizSiniri hizSiniri, ZiyaretciService ziyaretciService, SiteAyarlari ayarlar) {
        this.hizSiniri = hizSiniri;
        this.ziyaretciService = ziyaretciService;
        this.sinirlar = ayarlar.hiz();
    }

    @Override
    public void addInterceptors(InterceptorRegistry kayit) {
        kayit.addInterceptor(this).addPathPatterns("/api/**").order(-1);
    }

    @Override
    public boolean preHandle(HttpServletRequest istek, HttpServletResponse yanit, Object isleyici) {
        if (!YAZMA_YONTEMLERI.contains(istek.getMethod())) {
            return true;
        }
        String ip = istek.getRemoteAddr();
        // yönlendirmenin kullandığı ÇÖZÜLMÜŞ yol: "/api/%73ohbet" de /api/sohbet'e gider, sınırı atlayamasın
        String yol = UrlPathHelper.defaultInstance.getLookupPathForRequest(istek);
        boolean post = "POST".equals(istek.getMethod());
        String kimlik = istek.getAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI) instanceof String k
                && ZiyaretciCerezi.gecerliMi(k) ? k : null;

        List<Kova> kovalar = new ArrayList<>(3);
        kovalar.add(new Kova("yaz|" + ip, sinirlar.yazmaDakika(), YAZMA_PENCERE));
        // Geri yükleme eski kimlik için kayıt açmaz (dokunma interceptor'ı da /api/ziyaretci/**'yi atlar).
        if (kimlik != null && !yol.startsWith("/api/ziyaretci/")
                && !ziyaretciService.varMi(ZiyaretciKullaniciBaglami.ONEK + kimlik)) {
            kovalar.add(new Kova("yeni|" + ip, sinirlar.yeniZiyaretciSaat(), YENI_ZIYARETCI_PENCERE));
        }
        if (post && "/api/sohbet".equals(yol) && kimlik != null) {
            kovalar.add(new Kova("sohbet|" + ZiyaretciKullaniciBaglami.ONEK + kimlik, sinirlar.sohbetDakika(), SOHBET_PENCERE));
        }
        if (post && "/api/ziyaretci/geri-yukle".equals(yol)) {
            kovalar.add(new Kova("geri|" + ip, sinirlar.geriYukleSaat(), GERI_YUKLE_PENCERE));
        }

        // 1) tüketmeden bak: biri doluysa hiçbirine yazma (reddedilen taze kimlik de bellekte iz bırakmaz)
        Duration enUzun = Duration.ZERO;
        for (Kova k : kovalar) {
            Duration bekle = hizSiniri.bekleme(k.anahtar(), k.enCok(), k.pencere());
            if (bekle.compareTo(enUzun) > 0) {
                enUzun = bekle;
            }
        }
        if (!enUzun.isZero()) {
            throw new HizSiniriAsildiException(saniye(enUzun));
        }
        // 2) yaz: izinVer kova başına atomik; eşzamanlı yarışı kaybeden istek burada (nadiren) reddedilir
        for (Kova k : kovalar) {
            if (!hizSiniri.izinVer(k.anahtar(), k.enCok(), k.pencere())) {
                throw new HizSiniriAsildiException(saniye(hizSiniri.bekleme(k.anahtar(), k.enCok(), k.pencere())));
            }
        }
        return true;
    }

    /** Yukarı yuvarlanmış saniye; {@link HizSiniriAsildiException} en az 1'e çeker. */
    private static long saniye(Duration sure) {
        return (sure.toMillis() + 999) / 1000;
    }

    private record Kova(String anahtar, int enCok, Duration pencere) {
    }
}
