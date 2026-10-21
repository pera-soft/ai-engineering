package tr.velzon.aiogretmen.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.service.AtolyeKatalogService;
import tr.velzon.aiogretmen.service.KaynakService;
import tr.velzon.aiogretmen.service.MufredatService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI Öğretmen'in MCP <em>kaynakları</em> (resources): salt-okunur içerik primitifleri. İstemcinin
 * Claude'u bunları bağlamına ekler; sunucu LLM çağırmaz (ADR-0007).
 *
 * <ul>
 *   <li>Somut kaynaklar (discovery için listelenir): her hafta ({@code content://hafta/{no}}),
 *       her çalışma kaynağı ({@code kaynak://{slug}}), her atölye kategorisi ({@code atolye://{slug}}).</li>
 *   <li>Şablon kaynak: gün dersleri ({@code content://hafta/{no}/gun/{gun}}) — sayısı çok olduğundan
 *       (12 hafta × ~5 gün) tek tek listelenmez, URI şablonuyla sunulur.</li>
 * </ul>
 *
 * <p>Spring AI MCP server auto-config hem {@code List<SyncResourceSpecification>} hem
 * {@code List<SyncResourceTemplateSpecification>} bean'lerini toplar. Tipler MCP Java SDK'sındandır
 * ({@code io.modelcontextprotocol..}); Spring AI import edilmez.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class OgretmenMcpKaynaklari {

    private static final String MD = "text/markdown";
    private static final String JSON = "application/json";
    private static final Pattern GUN_URI =
            Pattern.compile("content://hafta/(\\d+)/gun/(\\d+)");

    private final MufredatService mufredatService;
    private final KaynakService kaynakService;
    private final AtolyeKatalogService atolyeKatalogService;
    private final ObjectMapper om;

    public OgretmenMcpKaynaklari(MufredatService mufredatService, KaynakService kaynakService,
                                 AtolyeKatalogService atolyeKatalogService, ObjectMapper om) {
        this.mufredatService = mufredatService;
        this.kaynakService = kaynakService;
        this.atolyeKatalogService = atolyeKatalogService;
        this.om = om;
    }

    @Bean
    public List<SyncResourceSpecification> mcpKaynakListesi() {
        List<SyncResourceSpecification> hepsi = new ArrayList<>();

        // Kaynak LİSTESİ açılışta (istek/JWT yokken) kurulur; kimlik yerine sabit YEREL kullanılır.
        // Buradaki ilerleme yüzdesi yalnız açıklama metnindedir (kullanıcıya özel READ değil).
        mufredatService.programOzeti(KullaniciBaglami.YEREL).forEach(h -> {
            String uri = "content://hafta/" + h.no();
            Resource r = Resource.builder().uri(uri)
                    .name("hafta-" + h.no())
                    .title("Hafta " + h.no() + ": " + h.baslik())
                    .description("Haftanın ham müfredat metni (hedef + quiz). Hedef: " + h.hedef())
                    .mimeType(MD).build();
            hepsi.add(new SyncResourceSpecification(r,
                    (ex, req) -> metin(uri, MD, mufredatService.hamHafta(h.no()))));
        });

        kaynakService.liste().forEach(k -> {
            String uri = "kaynak://" + k.slug();
            Resource r = Resource.builder().uri(uri)
                    .name("kaynak-" + k.slug())
                    .title(k.baslik())
                    .description("Türkçe çalışma metni — " + k.yazar())
                    .mimeType(MD).build();
            hepsi.add(new SyncResourceSpecification(r,
                    (ex, req) -> metin(uri, MD, (String) kaynakService.hamKaynak(k.slug()).get("metin"))));
        });

        atolyeKatalogService.kategoriler().forEach(kat -> {
            String uri = "atolye://" + kat.slug();
            Resource r = Resource.builder().uri(uri)
                    .name("atolye-" + kat.slug())
                    .title("Atölye: " + kat.baslik())
                    .description(kat.aciklama() + " (alıştırmalar + ideal çözüm/gerekçe; istemci rubrik olarak kullanır)")
                    .mimeType(JSON).build();
            hepsi.add(new SyncResourceSpecification(r, (ex, req) -> {
                var esles = atolyeKatalogService.kategoriler().stream()
                        .filter(x -> x.slug().equals(kat.slug())).findFirst().orElse(kat);
                return metin(uri, JSON, jsonMetin(esles));
            }));
        });

        return hepsi;
    }

    @Bean
    public List<SyncResourceTemplateSpecification> mcpKaynakSablonListesi() {
        ResourceTemplate gunSablon = new ResourceTemplate(
                "content://hafta/{no}/gun/{gun}",
                "gun-dersi",
                "Gün dersi",
                "Belirli bir haftanın belirli bir gününün ham ders metni ve görevleri.",
                MD, null);

        SyncResourceTemplateSpecification gun = new SyncResourceTemplateSpecification(gunSablon,
                (ex, req) -> {
                    Matcher m = GUN_URI.matcher(req.uri());
                    if (!m.matches()) {
                        return metin(req.uri(), MD, "(geçersiz URI: " + req.uri() + ")");
                    }
                    int hafta = Integer.parseInt(m.group(1));
                    int gunNo = Integer.parseInt(m.group(2));
                    return metin(req.uri(), MD, mufredatService.hamGun(hafta, gunNo));
                });

        return List.of(gun);
    }

    private static ReadResourceResult metin(String uri, String mimeType, String icerik) {
        return new ReadResourceResult(List.of(new TextResourceContents(uri, mimeType, icerik)));
    }

    private String jsonMetin(Object payload) {
        try {
            return om.writeValueAsString(payload);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return "{\"hata\":\"serileştirilemedi\"}";
        }
    }
}
