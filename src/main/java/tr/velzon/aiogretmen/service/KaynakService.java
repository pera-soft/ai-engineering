package tr.velzon.aiogretmen.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.repo.KaynakRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.Kaynak;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Eğitim kaynakları: listeleme, okuma (HTML) ve AI bağlamına enjeksiyon. */
@Service
public class KaynakService {

    /** AI bağlamı toplam üst sınırı (tüm hafta kaynakları). SVG ayıklandıktan sonra saf metin. */
    private static final int BAGLAM_KARAKTER_SINIRI = 14000;
    /** Her kaynaktan alınan ÖZ (baş kısım): tek kaynak bütçeyi yutup diğer konuları aç bırakmasın. */
    private static final int KAYNAK_BASI_SINIR = 3000;
    /**
     * ```svg diyagram blokları — AI bağlamı için markup gürültüsü; okuma sayfasında canlı çizilir.
     * Çitler SATIR BAŞINA sabit (^```svg$ … ^```$): kapanmayan bir svg çiti, sonraki ```java gibi
     * bir bloğun backtick'lerine yanlış eşleşip aradaki metni silmesin (inceleme UYARI'sı).
     */
    static final Pattern SVG_MD_FENCE =
            Pattern.compile("^```svg$.*?^```$", Pattern.DOTALL | Pattern.MULTILINE);

    private final KaynakRepo kaynakRepo;
    /** GFM tablo eklentisi — commonmark çekirdeği tablo bilmez (ADR-0006). escapeHtml korunur. */
    private static final List<Extension> MD_EKLENTILER = List.of(TablesExtension.create());
    private final Parser mdParser = Parser.builder().extensions(MD_EKLENTILER).build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(MD_EKLENTILER).escapeHtml(true).build();

    public KaynakService(KaynakRepo kaynakRepo) {
        this.kaynakRepo = kaynakRepo;
    }

    public List<Kaynak> liste() {
        return kaynakRepo.hepsi().stream().map(Kaynak::meta).toList();
    }

    public Map<String, Object> oku(String slug) {
        Kaynak k = kaynakRepo.bul(slug)
                .orElseThrow(() -> new BulunamadiException("Kaynak bulunamadı: " + slug));
        return Map.of(
                "slug", k.slug(), "baslik", k.baslik(), "yazar", k.yazar(),
                "tarih", k.tarih(), "url", k.url(), "etiketler", k.etiketler(),
                "html", semalariAc(htmlRenderer.render(mdParser.parse(k.govde()))));
    }

    /**
     * ```svg fence'lerini CANLI inline SVG'ye açar. escapeHtml(true) KALIR — yalnız açıkça
     * "svg" olarak işaretlenmiş diyagram blokları açılır (least-privilege). Diyagramlar içerik
     * yazarının (AI) ürettiği güvenilir artefaktlardır; ayrıca CSP (default-src 'self') script'i bloke eder.
     */
    static final Pattern SVG_FENCE =
            Pattern.compile("<pre><code class=\"language-svg\">(.*?)</code></pre>", Pattern.DOTALL);

    static String semalariAc(String html) {
        Matcher m = SVG_FENCE.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String svg = m.group(1)
                    .replace("&lt;", "<").replace("&gt;", ">")
                    .replace("&quot;", "\"").replace("&#39;", "'")
                    .replace("&amp;", "&"); // &amp; en SON (çift-çözmeyi önler)
            m.appendReplacement(sb, Matcher.quoteReplacement("<figure class=\"oku-sema\">" + svg + "</figure>"));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Ham çalışma metni (Türkçe) + meta — MCP <code>kaynak(slug)</code> aracı ve
     * <code>kaynak://{slug}</code> kaynağı için. ```svg diyagram blokları ayıklanır (LLM için gürültü).
     * LLM çağrılmaz: yalnız dosyadaki metin sunulur, muhakeme istemcide kalır.
     *
     * @throws BulunamadiException slug yoksa
     */
    public Map<String, Object> hamKaynak(String slug) {
        Kaynak k = kaynakRepo.bul(slug)
                .orElseThrow(() -> new BulunamadiException("Kaynak bulunamadı: " + slug));
        return Map.of(
                "slug", k.slug(), "baslik", k.baslik(), "yazar", k.yazar(),
                "tarih", k.tarih(), "url", k.url(), "etiketler", k.etiketler(),
                "metin", semaSiz(k.govde()));
    }

    /**
     * Aktif haftayla etiket-eşleşen çalışma metinleri — sistem prompt'una girer. SVG AI bağlamından ayıklanır.
     * Sıra ÖNCELİKLİ, alfabetik değil: bütçe (~4 öz) aşılınca sondakiler kırpılır; alfabetik sırada her
     * haftaya etiketli genel metinler haftanın kendi metnini ({@code haftaNN-…}) dışarıda bırakıyordu.
     */
    public String baglamIcin(int hafta) {
        String haftaOneki = String.format("hafta%02d-", hafta);
        List<String> parcalar = kaynakRepo.haftaninKaynaklari(hafta).stream()
                .sorted(Comparator.comparing((Kaynak k) -> !k.slug().startsWith(haftaOneki)) // haftanın kendisi
                        .thenComparingInt(k -> k.etiketler().size()))                        // özel → genel
                .map(k -> "### " + k.baslik() + " — " + k.yazar() + "\n" + semaSiz(k.govde()))
                .toList();
        return BaglamKirpici.birlestir(parcalar, KAYNAK_BASI_SINIR, BAGLAM_KARAKTER_SINIRI,
                "(Bu haftayla eşleşen kaynak metni yok.)");
    }

    /** ```svg diyagram bloklarını AI bağlamından çıkarır (yerine kısa etiket); okuma render'ı etkilenmez. */
    static String semaSiz(String md) {
        return SVG_MD_FENCE.matcher(md).replaceAll("(diyagram — görsel özet)");
    }
}
