package tr.velzon.aiogretmen.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.repo.SohbetRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;
import tr.velzon.aiogretmen.types.Hafta;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Haftalık rapor (brief 7.5): haftanın tüm verisi guclu modele verilir; markdown rapor
 * data/raporlar/hafta-NN.md olarak CACHE'lenir (varsa dosyadan döner, yenile=true zorlar).
 *
 * <p>Faz 9b (ADR-0012): önbellek KULLANICI BAŞINADIR ({@link KisiselDizin}) — yerel kullanıcı eski
 * {@code data/raporlar/hafta-NN.md}, diğerleri {@code data/raporlar/kullanicilar/<sha256>/hafta-NN.md}.
 */
@Service
@LlmKullanan
public class RaporService {

    static final int MAX_TOKENS = 3000;

    private final Path raporKok;
    private final OgretmenAiClient aiClient;
    private final BaglamServisi baglamServisi;
    private final PromptFactory promptFactory;
    private final ContentRepo contentRepo;
    private final IlerlemeRepo ilerlemeRepo;
    private final QuizSonucRepo quizSonucRepo;
    private final GonderimRepo gonderimRepo;
    private final SohbetRepo sohbetRepo;
    private final KullaniciBaglami kullaniciBaglami;
    /** GFM tablo eklentisi — commonmark çekirdeği tablo bilmez (ADR-0006). escapeHtml korunur. */
    private static final List<Extension> MD_EKLENTILER = List.of(TablesExtension.create());
    private final Parser mdParser = Parser.builder().extensions(MD_EKLENTILER).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(MD_EKLENTILER).escapeHtml(true).build();

    public RaporService(tr.velzon.aiogretmen.config.AppProperties props,
                        OgretmenAiClient aiClient, BaglamServisi baglamServisi,
                        PromptFactory promptFactory, ContentRepo contentRepo,
                        IlerlemeRepo ilerlemeRepo, QuizSonucRepo quizSonucRepo,
                        GonderimRepo gonderimRepo, SohbetRepo sohbetRepo,
                        KullaniciBaglami kullaniciBaglami) {
        this.raporKok = Path.of(props.raporDir() == null ? "./data/raporlar" : props.raporDir());
        this.aiClient = aiClient;
        this.baglamServisi = baglamServisi;
        this.promptFactory = promptFactory;
        this.contentRepo = contentRepo;
        this.ilerlemeRepo = ilerlemeRepo;
        this.quizSonucRepo = quizSonucRepo;
        this.gonderimRepo = gonderimRepo;
        this.sohbetRepo = sohbetRepo;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    public record Rapor(int hafta, String markdown, String html, boolean cacheden) {
    }

    public Rapor rapor(int hafta, boolean yenile) {
        contentRepo.hafta(hafta)
                .orElseThrow(() -> new BulunamadiException("Hafta bulunamadı: " + hafta));

        Path cache = KisiselDizin.coz(raporKok, kullaniciBaglami.aktifKullanici())
                .resolve(String.format("hafta-%02d.md", hafta));
        if (!yenile && Files.isRegularFile(cache)) {
            String md = oku(cache);
            return new Rapor(hafta, md, htmlRenderer.render(mdParser.parse(md)), true);
        }

        String prompt = promptFactory.doldur("haftalik-rapor", Map.of(
                "hafta", String.valueOf(hafta),
                "gorev_ozeti", gorevOzeti(hafta),
                "quiz_ozeti", quizOzeti(hafta),
                "gonderimler", gonderimOzeti(hafta),
                "sohbet_ozeti", sohbetOzeti(hafta)));

        var yanit = aiClient.cagir(AiIstek.tekSeferlik(
                Islem.RAPOR, ModelTuru.GUCLU, baglamServisi.sistemBaglami(hafta, null), prompt, MAX_TOKENS));

        String md = yanit.metin();
        yaz(cache, md);
        return new Rapor(hafta, md, htmlRenderer.render(mdParser.parse(md)), false);
    }

    private static final java.util.regex.Pattern RAPOR_DOSYASI =
            java.util.regex.Pattern.compile("hafta-\\d{2}\\.md(\\.tmp)?");

    /**
     * VERİLEN kullanıcının rapor önbelleğini siler (sıfırlama). Yerel: eski kökte yalnız
     * {@code hafta-NN.md} dosyaları ({@code kullanicilar/} korunur); diğerleri: kişisel dizin bütünüyle.
     * Best-effort ve idempotent — hata loglanır, çağıranın (DB) işlemini bozmaz.
     */
    public void raporOnbeleginiSil(String kullaniciId) {
        try {
            Path dizin = KisiselDizin.coz(raporKok, kullaniciId);
            if (!tr.velzon.aiogretmen.config.KullaniciBaglami.YEREL.equals(kullaniciId)) {
                KisiselDizin.agaciSil(dizin);
                return;
            }
            if (!Files.isDirectory(dizin)) {
                return;
            }
            try (var alt = Files.list(dizin)) {
                for (Path f : alt.filter(Files::isRegularFile)
                        .filter(f -> RAPOR_DOSYASI.matcher(f.getFileName().toString()).matches()).toList()) {
                    Files.deleteIfExists(f);
                }
            }
        } catch (IOException | RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(RaporService.class)
                    .warn("Rapor önbelleği silinemedi: {}", e.getMessage());
        }
    }

    private String gorevOzeti(int haftaNo) {
        Hafta hafta = contentRepo.hafta(haftaNo).orElseThrow();
        var tamamlanan = ilerlemeRepo.findByKullaniciIdAndHafta(kullaniciBaglami.aktifKullanici(), haftaNo).stream()
                .filter(GorevIlerlemeKaydi::tamamlandi)
                .map(k -> k.gun() + ":" + k.gorevIndex())
                .collect(Collectors.toSet());
        return hafta.gunler().stream()
                .map(g -> {
                    long biten = java.util.stream.IntStream.range(0, g.gorevler().size())
                            .filter(i -> tamamlanan.contains(g.gun() + ":" + i)).count();
                    return "Gün %d (%s): %d/%d görev".formatted(g.gun(), g.baslik(), biten, g.gorevler().size());
                })
                .collect(Collectors.joining("\n"));
    }

    private String quizOzeti(int hafta) {
        var sonuclar = quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(kullaniciBaglami.aktifKullanici(), hafta);
        return sonuclar.isEmpty() ? "(quiz çözülmedi)"
                : sonuclar.stream()
                        .map(s -> "%s: %%%d".formatted(s.tur(), s.puan()))
                        .collect(Collectors.joining("\n"));
    }

    private String gonderimOzeti(int hafta) {
        var gonderimler = gonderimRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(kullaniciBaglami.aktifKullanici(), hafta);
        return gonderimler.isEmpty() ? "(gönderim yok)"
                : gonderimler.stream()
                        .map(g -> "- [%s%s] %s\n  Değerlendirme: %s".formatted(
                                g.tur(),
                                g.puan() == null ? "" : ", puan %" + g.puan(),
                                kirp(g.icerik(), 200),
                                g.degerlendirmeJson() == null ? "(yok)" : kirp(g.degerlendirmeJson(), 400)))
                        .collect(Collectors.joining("\n"));
    }

    private String sohbetOzeti(int hafta) {
        var mesajlar = sohbetRepo.findByKullaniciIdAndHaftaOrderByTarihAsc(kullaniciBaglami.aktifKullanici(), hafta);
        if (mesajlar.isEmpty()) {
            return "(sohbet yok)";
        }
        String sonKonular = mesajlar.stream()
                .filter(m -> "USER".equals(m.rol()))
                .map(m -> "- " + kirp(m.icerik(), 120))
                .collect(Collectors.joining("\n"));
        return "Toplam %d mesaj. Öğrencinin sorduğu/söylediği:\n%s".formatted(mesajlar.size(), sonKonular);
    }

    private static String kirp(String s, int limit) {
        String tek = s.replaceAll("\\s+", " ").strip();
        return tek.length() <= limit ? tek : tek.substring(0, limit) + "…";
    }

    private static String oku(Path p) {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException("rapor okunamadı: " + p, e);
        }
    }

    /** Yaz-yeniden-adlandır: yarım yazılmış cache dosyası okunmasın (idempotent yedeklilik). */
    private static void yaz(Path p, String icerik) {
        try {
            Files.createDirectories(p.getParent());
            Path gecici = p.resolveSibling(p.getFileName() + ".tmp");
            Files.writeString(gecici, icerik);
            Files.move(gecici, p, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("rapor yazılamadı: " + p, e);
        }
    }
}
