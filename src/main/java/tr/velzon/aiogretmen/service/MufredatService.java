package tr.velzon.aiogretmen.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.GorevDurum;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;
import tr.velzon.aiogretmen.types.Gun;
import tr.velzon.aiogretmen.types.GunDetay;
import tr.velzon.aiogretmen.types.Hafta;
import tr.velzon.aiogretmen.types.HaftaDetay;
import tr.velzon.aiogretmen.types.HaftaOzeti;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MufredatService {

    private final ContentRepo contentRepo;
    private final IlerlemeRepo ilerlemeRepo;
    private final QuizSonucRepo quizSonucRepo;
    /** GFM tablo eklentisi — commonmark çekirdeği tablo bilmez (ADR-0006). escapeHtml korunur. */
    private static final List<Extension> MD_EKLENTILER = List.of(TablesExtension.create());
    private final Parser mdParser = Parser.builder().extensions(MD_EKLENTILER).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(MD_EKLENTILER).escapeHtml(true).build();

    public MufredatService(ContentRepo contentRepo, IlerlemeRepo ilerlemeRepo, QuizSonucRepo quizSonucRepo) {
        this.contentRepo = contentRepo;
        this.ilerlemeRepo = ilerlemeRepo;
        this.quizSonucRepo = quizSonucRepo;
    }

    public List<HaftaOzeti> programOzeti(String kullaniciId) {
        return contentRepo.haftalar().stream().map(h -> ozetle(kullaniciId, h)).toList();
    }

    /**
     * Ham hafta.md içeriği (frontmatter + quiz) — MCP <code>content://hafta/{no}</code> kaynağı için.
     * Muhakeme istemcide olduğundan sunucu yalnız metni sunar, LLM çağırmaz.
     *
     * @throws BulunamadiException hafta yoksa
     */
    public String hamHafta(int no) {
        contentRepo.hafta(no)
                .orElseThrow(() -> new BulunamadiException("Hafta bulunamadı: " + no));
        return contentRepo.hamHafta(no);
    }

    /**
     * Ham gun-N.md içeriği (ders + görevler) — MCP <code>content://hafta/{no}/gun/{gun}</code> kaynağı için.
     *
     * @throws BulunamadiException gün yoksa (boş içerik)
     */
    public String hamGun(int hafta, int gun) {
        String ham = contentRepo.hamGun(hafta, gun);
        if (ham.isBlank()) {
            throw new BulunamadiException("Gün bulunamadı: hafta " + hafta + " gün " + gun);
        }
        return ham;
    }

    public HaftaDetay haftaDetay(String kullaniciId, int no) {
        Hafta hafta = contentRepo.hafta(no)
                .orElseThrow(() -> new BulunamadiException("Hafta bulunamadı: " + no));

        Set<String> tamamlananlar = tamamlananAnahtarlar(kullaniciId, no);
        List<GunDetay> gunler = hafta.gunler().stream()
                .map(g -> gunDetay(g, tamamlananlar))
                .toList();

        return new HaftaDetay(hafta.no(), hafta.baslik(), hafta.hedef(), hafta.kaynak(),
                gunler, hafta.quiz(), sonQuizPuan(kullaniciId, no));
    }

    private HaftaOzeti ozetle(String kullaniciId, Hafta hafta) {
        int toplamGorev = hafta.gunler().stream().mapToInt(g -> g.gorevler().size()).sum();
        int tamamlanan = tamamlananAnahtarlar(kullaniciId, hafta.no()).size();
        int yuzde = toplamGorev == 0 ? 0 : Math.round(100f * tamamlanan / toplamGorev);
        return new HaftaOzeti(hafta.no(), hafta.baslik(), hafta.hedef(),
                toplamGorev, tamamlanan, yuzde, sonQuizPuan(kullaniciId, hafta.no()));
    }

    private GunDetay gunDetay(Gun gun, Set<String> tamamlananlar) {
        List<GorevDurum> gorevler = java.util.stream.IntStream.range(0, gun.gorevler().size())
                .mapToObj(i -> new GorevDurum(i, gun.gorevler().get(i),
                        tamamlananlar.contains(gun.gun() + ":" + i)))
                .toList();
        String dersHtml = htmlRenderer.render(mdParser.parse(gun.ders()));
        return new GunDetay(gun.gun(), gun.baslik(), dersHtml, gorevler);
    }

    private Set<String> tamamlananAnahtarlar(String kullaniciId, int hafta) {
        return ilerlemeRepo.findByKullaniciIdAndHafta(kullaniciId, hafta).stream()
                .filter(GorevIlerlemeKaydi::tamamlandi)
                .map(k -> k.gun() + ":" + k.gorevIndex())
                .collect(Collectors.toSet());
    }

    private Integer sonQuizPuan(String kullaniciId, int hafta) {
        return quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(kullaniciId, hafta).stream()
                .findFirst()
                .map(QuizSonucKaydi::puan)
                .orElse(null);
    }
}
