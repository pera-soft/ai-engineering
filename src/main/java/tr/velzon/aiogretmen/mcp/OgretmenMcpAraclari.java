package tr.velzon.aiogretmen.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tr.velzon.aiogretmen.service.AtolyeKatalogService;
import tr.velzon.aiogretmen.service.IlerlemeService;
import tr.velzon.aiogretmen.service.KaynakService;
import tr.velzon.aiogretmen.service.KullanimService;
import tr.velzon.aiogretmen.service.MufredatService;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.hata;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.json;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.metinAlan;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.metinVarsayilan;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.sema;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.semaBos;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.tamsayi;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.tamsayiAlan;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.tamsayiVarsayilan;

/**
 * AI Öğretmen'in MCP <em>araçları</em> (tools): durum/veri okuma-yazma. Hepsi LLM'siz servislere
 * gider — muhakeme bağlanan istemcide (ADR-0007). Araç SAYISI bilinçli olarak azdır (MCP "context
 * tax": her araç istemcinin bağlam penceresini şişirir); açıklamalar özenlidir.
 *
 * <p>Araçlar Spring AI'nın {@code @Tool} anotasyonuyla değil, MCP Java SDK'sının
 * {@link SyncToolSpecification} bean'leriyle tanımlanır — {@code mcp} paketi Spring AI'yı
 * import etmesin diye (bkz {@link McpAdaptorDestek}).
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class OgretmenMcpAraclari {

    private final MufredatService mufredatService;
    private final KaynakService kaynakService;
    private final AtolyeKatalogService atolyeKatalogService;
    private final IlerlemeService ilerlemeService;
    private final KullanimService kullanimService;
    private final ObjectMapper om;

    public OgretmenMcpAraclari(MufredatService mufredatService, KaynakService kaynakService,
                               AtolyeKatalogService atolyeKatalogService, IlerlemeService ilerlemeService,
                               KullanimService kullanimService, ObjectMapper om) {
        this.mufredatService = mufredatService;
        this.kaynakService = kaynakService;
        this.atolyeKatalogService = atolyeKatalogService;
        this.ilerlemeService = ilerlemeService;
        this.kullanimService = kullanimService;
        this.om = om;
    }

    @Bean
    public List<SyncToolSpecification> mcpAracListesi() {
        return List.of(
                arac("program_ozeti",
                        "12 haftalık mimarlık programının özeti: her hafta için başlık, hedef, "
                                + "görev tamamlanma yüzdesi ve son quiz puanı. Argüman almaz.",
                        semaBos(),
                        (ex, req) -> json(om, mufredatService.programOzeti(
                                McpKullaniciKimligi.kullaniciKimligiVeyaYerel()))),

                arac("hafta_detay",
                        "Bir haftanın tüm detayı: günlük dersler (HTML), görevler ve durumları, "
                                + "quiz soruları. 'no' = hafta numarası (1-12).",
                        sema(Map.of("no", tamsayiAlan("Hafta numarası (1-12)")), List.of("no")),
                        (ex, req) -> json(om, mufredatService.haftaDetay(
                                McpKullaniciKimligi.kullaniciKimligiVeyaYerel(), tamsayi(req.arguments(), "no")))),

                arac("kaynaklar",
                        "Programın Türkçe çalışma kaynaklarının listesi (başlık, yazar, ilgili haftalar). "
                                + "Tam metin için 'kaynak' aracını kullan. Argüman almaz.",
                        semaBos(),
                        (ex, req) -> json(om, kaynakService.liste())),

                arac("kaynak",
                        "Bir çalışma kaynağının tam Türkçe metnini döndürür (diyagramlar ayıklanmış). "
                                + "'slug' = kaynağın kısa adı ('kaynaklar' aracından alınır).",
                        sema(Map.of("slug", metinAlan("Kaynağın slug'ı")), List.of("slug")),
                        (ex, req) -> json(om, kaynakService.hamKaynak(McpAdaptorDestek.metin(req.arguments(), "slug")))),

                arac("atolye_kategorileri",
                        "Prompt Atölyesi kategorileri ve alıştırmaları (teknik, görev, ideal çözüm, "
                                + "gerekçe). İstemci bunları rubrik olarak kullanıp öğrencinin prompt'unu "
                                + "kendi değerlendirir. Argüman almaz.",
                        semaBos(),
                        (ex, req) -> json(om, atolyeKatalogService.kategoriler())),

                arac("gorev_isaretle",
                        "Bir günlük görevin tamamlanma durumunu tersine çevirir (toggle) ve yeni durumu "
                                + "döndürür. 'hafta', 'gun', 'gorev_index' (0'dan başlar).",
                        sema(Map.of(
                                        "hafta", tamsayiAlan("Hafta numarası"),
                                        "gun", tamsayiAlan("Gün numarası"),
                                        "gorev_index", tamsayiAlan("Görev sırası (0'dan başlar)")),
                                List.of("hafta", "gun", "gorev_index")),
                        (ex, req) -> {
                            Map<String, Object> a = req.arguments();
                            boolean tamam = ilerlemeService.gorevToggle(
                                    McpKullaniciKimligi.kullaniciKimligiVeyaYerel(),
                                    tamsayi(a, "hafta"), tamsayi(a, "gun"), tamsayi(a, "gorev_index"));
                            return json(om, Map.of("tamamlandi", tamam));
                        }),

                arac("statik_quiz_kaydet",
                        "Öğrencinin statik quiz sonucunu kaydeder. 'hafta', 'puan' (0-100), "
                                + "'detay' (opsiyonel JSON dizesi).",
                        sema(Map.of(
                                        "hafta", tamsayiAlan("Hafta numarası"),
                                        "puan", tamsayiAlan("Quiz puanı (0-100)"),
                                        "detay", metinAlan("Opsiyonel detay (JSON dizesi)")),
                                List.of("hafta", "puan")),
                        (ex, req) -> {
                            Map<String, Object> a = req.arguments();
                            var kayit = ilerlemeService.statikQuizKaydet(
                                    McpKullaniciKimligi.kullaniciKimligiVeyaYerel(),
                                    tamsayi(a, "hafta"),
                                    tamsayiVarsayilan(a, "puan", 0),
                                    metinVarsayilan(a, "detay", null));
                            return json(om, Map.of("id", kayit.id(), "puan", kayit.puan(), "tur", kayit.tur()));
                        }),

                arac("kullanim_istatistik",
                        "Sunucudaki AI token kullanım istatistiği (toplam çağrı, girdi/çıktı token, "
                                + "işlem bazında dağılım). MCP muhakemesi bu sayacı ARTIRMAZ — muhakeme "
                                + "istemcide olduğundan sunucu token'ı yanmaz. Argüman almaz.",
                        semaBos(),
                        (ex, req) -> json(om, kullanimService.istatistik())));
    }

    /** Ortak sarmalayıcı: BulunamadiException/IllegalArgumentException → zarif hata sonucu (isError). */
    private static SyncToolSpecification arac(
            String ad, String aciklama, JsonSchema sema,
            BiFunction<io.modelcontextprotocol.server.McpSyncServerExchange, CallToolRequest, CallToolResult> govde) {
        Tool tool = Tool.builder().name(ad).description(aciklama).inputSchema(sema).build();
        return SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, req) -> {
                    try {
                        return govde.apply(exchange, req);
                    } catch (BulunamadiException e) {
                        return hata(e.getMessage());
                    } catch (IllegalArgumentException e) {
                        return hata("Geçersiz argüman: " + e.getMessage());
                    }
                })
                .build();
    }
}
