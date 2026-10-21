package tr.velzon.aiogretmen.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.config.KullaniciBaglami;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Öğrenci hafızası (kullanıcı eklemesi): AI öğretmen gözlemlerini markdown ağacına yazar,
 * sonraki tüm çağrılara enjekte edilir. Güncelleme hataları ana akışı ASLA bozmaz.
 *
 *   &lt;dizin&gt;/OGRENCI.md            — genel profil
 *   &lt;dizin&gt;/hafta-NN/notlar.md    — hafta bazlı gözlemler
 *
 * <p>Faz 9b (ADR-0012): {@code <dizin>} KULLANICI BAŞINADIR ({@link KisiselDizin}) — yerel kullanıcı için
 * eski kök {@code data/hafiza/}, diğerleri için {@code data/hafiza/kullanicilar/<sha256>/}. Kimlik her
 * işlemin BAŞINDA bir kez çözülür; okuma ve yazma aynı kullanıcının dizinine gider.
 */
@Service
@LlmKullanan
public class HafizaService {

    private static final Logger log = LoggerFactory.getLogger(HafizaService.class);
    private static final String VARSAYILAN_PROFIL = """
            # Öğrenci Profili
            (Henüz gözlem yok — ilk etkileşimlerden sonra AI öğretmen burayı dolduracak.)
            """;

    /** Hafıza KÖKÜ (kitaplar/ ve vektor.json da burada — onlar kullanıcıya ait değildir). */
    private final Path hafizaKok;
    private final OgretmenAiClient aiClient;
    private final PromptFactory promptFactory;
    private final JsonCevapParser parser;
    private final KullaniciBaglami kullaniciBaglami;

    public HafizaService(AppProperties props, OgretmenAiClient aiClient,
                         PromptFactory promptFactory, JsonCevapParser parser,
                         KullaniciBaglami kullaniciBaglami) {
        this.hafizaKok = Path.of(props.hafizaDir());
        this.aiClient = aiClient;
        this.promptFactory = promptFactory;
        this.parser = parser;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    /** Aktif kullanıcının hafıza dizini (oturum yoksa site profilinde fail-closed). */
    private Path dizin() {
        return KisiselDizin.coz(hafizaKok, kullaniciBaglami.aktifKullanici());
    }

    public String ogrenciProfili() {
        return ogrenciProfili(dizin());
    }

    public String haftaNotlari(int hafta) {
        return haftaNotlari(dizin(), hafta);
    }

    /** Prompt'lara enjekte edilecek birleşik hafıza bloğu (brief 7.1 + kullanıcı eklemesi). */
    public String hafizaBaglami(int hafta) {
        Path d = dizin();
        return ogrenciProfili(d) + "\n\n## Hafta " + hafta + " notları\n" + haftaNotlari(d, hafta);
    }

    private static String ogrenciProfili(Path dizin) {
        return oku(dizin.resolve("OGRENCI.md"), VARSAYILAN_PROFIL);
    }

    private static String haftaNotlari(Path dizin, int hafta) {
        return oku(haftaNotlariYolu(dizin, hafta), "(Bu hafta için not yok.)");
    }

    /**
     * Hizli model ile hafızayı günceller. Sessiz-dayanıklı: AI kapalıysa, JSON bozuksa
     * veya disk hatası olursa uyarı loglar ve akışa devam eder.
     */
    public void guncelle(int hafta, String etkilesimTuru, String etkilesim) {
        if (!aiClient.aktif()) {
            return;
        }
        Path d = dizin(); // kimlik bir kez: okunan ve yazılan dizin AYNI kullanıcınınki
        try {
            String prompt = promptFactory.doldur("hafiza-guncelle", Map.of(
                    "ogrenci_md", ogrenciProfili(d),
                    "hafta_notlari_md", haftaNotlari(d, hafta),
                    "hafta", String.valueOf(hafta),
                    "etkilesim_turu", etkilesimTuru,
                    "etkilesim", etkilesim));

            var yanit = aiClient.cagir(AiIstek.tekSeferlik(
                    Islem.HAFIZA, ModelTuru.HIZLI,
                    "Sen bir öğretmen not defterisin. Yalnızca geçerli JSON döndürürsün.",
                    prompt, 2000));

            parser.parse(yanit.metin(), HafizaGuncelleme.class).ifPresentOrElse(
                    g -> yaz(d, hafta, g),
                    () -> log.warn("Hafıza güncellemesi JSON parse edilemedi — atlandı (hafta {})", hafta));
        } catch (Exception e) {
            log.warn("Hafıza güncellemesi başarısız — akış etkilenmedi: {}", e.getMessage());
        }
    }

    record HafizaGuncelleme(
            @JsonProperty("ogrenci_profili") String ogrenciProfili,
            @JsonProperty("hafta_notlari") String haftaNotlari) {
    }

    // İnceleme 2. tur: tam-içerik geri-yazma modeli sınırsız büyürse 2000 token'lık
    // güncelleme çıktısına sığmaz olur ve güncellemeler sessizce atlanmaya başlar.
    // Mekanik üst sınır: profil başı (özet üstte), notlarda ise SON kayıtlar korunur.
    static final int PROFIL_KARAKTER_SINIRI = 4000;
    static final int NOT_KARAKTER_SINIRI = 6000;

    private static void yaz(Path dizin, int hafta, HafizaGuncelleme g) {
        try {
            if (g.ogrenciProfili() != null && !g.ogrenciProfili().isBlank()) {
                atomikYaz(dizin.resolve("OGRENCI.md"),
                        basTut(g.ogrenciProfili(), PROFIL_KARAKTER_SINIRI));
            }
            if (g.haftaNotlari() != null && !g.haftaNotlari().isBlank()) {
                atomikYaz(haftaNotlariYolu(dizin, hafta),
                        sonTut(g.haftaNotlari(), NOT_KARAKTER_SINIRI));
            }
        } catch (IOException e) {
            log.warn("Hafıza dosyası yazılamadı: {}", e.getMessage());
        }
    }

    private static String basTut(String icerik, int sinir) {
        return icerik.length() <= sinir ? icerik
                : icerik.substring(0, sinir) + "\n(… profil sınıra kırpıldı)";
    }

    private static String sonTut(String icerik, int sinir) {
        return icerik.length() <= sinir ? icerik
                : "(… eski notlar sınıra kırpıldı)\n" + icerik.substring(icerik.length() - sinir);
    }

    /** Yaz-yeniden-adlandır: yarım yazılmış hafıza dosyası kalmasın (idempotent yedeklilik kuralı). */
    private static void atomikYaz(Path hedef, String icerik) throws IOException {
        Files.createDirectories(hedef.getParent());
        Path gecici = hedef.resolveSibling(hedef.getFileName() + ".tmp");
        Files.writeString(gecici, icerik);
        Files.move(gecici, hedef, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Temiz başlangıç: VERİLEN kullanıcının öğrenci profili (OGRENCI.md) + haftalık
     * gözlemleri (hafta-NN/) silinir. Kimlik açık parametre (explicit identity) — çağıran kimin verisini
     * sildiğini kendisi söyler.
     * <ul>
     *   <li>yerel: eski kökte yalnız {@code OGRENCI.md} + {@code hafta-*}; kitaplar/, vektor.json (RAG) ve
     *       {@code kullanicilar/} (başka kullanıcılar) KORUNUR.</li>
     *   <li>diğerleri: kullanıcının kişisel dizini bütünüyle silinir (içinde yalnız onun hafızası var).</li>
     * </ul>
     * Idempotent — eksik dosya sorun değil, ana akışı bozmaz.
     */
    public void ogrenciNotlariniSil(String kullaniciId) {
        try {
            Path dizin = KisiselDizin.coz(hafizaKok, kullaniciId);
            if (!KullaniciBaglami.YEREL.equals(kullaniciId)) {
                KisiselDizin.agaciSil(dizin);
                return;
            }
            Files.deleteIfExists(dizin.resolve("OGRENCI.md"));
            if (Files.isDirectory(dizin)) {
                try (var alt = Files.list(dizin)) {
                    for (Path d : alt.filter(Files::isDirectory)
                            .filter(p -> p.getFileName().toString().startsWith("hafta-")).toList()) {
                        KisiselDizin.agaciSil(d);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // Files.list/walk traversal DirectoryIteratorException/UncheckedIOException (RuntimeException)
            // atabilir; bunlar @Transactional sifirla()'ya SIZMASIN → DB rollback tetiklenmesin (best-effort).
            log.warn("Öğrenci hafızası silinemedi: {}", e.getMessage());
        }
    }

    private static Path haftaNotlariYolu(Path dizin, int hafta) {
        return dizin.resolve(String.format("hafta-%02d", hafta)).resolve("notlar.md");
    }

    private static String oku(Path dosya, String varsayilan) {
        try {
            return Files.isRegularFile(dosya) ? Files.readString(dosya) : varsayilan;
        } catch (IOException e) {
            return varsayilan;
        }
    }
}
