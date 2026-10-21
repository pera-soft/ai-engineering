package tr.velzon.aiogretmen.mcp;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tr.velzon.aiogretmen.service.McpPromptServisi;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.util.List;
import java.util.function.BiFunction;

import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.metin;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.metinVarsayilan;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.tamsayi;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.tamsayiOpsiyonel;
import static tr.velzon.aiogretmen.mcp.McpAdaptorDestek.tamsayiVarsayilan;

/**
 * AI Öğretmen'in MCP <em>prompt'ları</em> (prompts): öğretmenlik talimatı METİNLERİ. İstemcinin
 * Claude'u bu talimatı alıp <em>kendi token'ıyla</em> muhakeme eder; sunucu LLM'e dokunmaz (ADR-0007).
 * Metinler {@link McpPromptServisi} tarafından embeddingsiz üretilir (anlam-eşleşmeli kitap bağlamı
 * kapalı) — sunucunun embedding sağlayıcısı da çağrılmaz.
 *
 * <p>Prompt'lar MCP Java SDK'sının {@link SyncPromptSpecification} bean'iyle sunulur;
 * Spring AI import edilmez.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class OgretmenMcpPromptlari {

    private final McpPromptServisi promptServisi;

    public OgretmenMcpPromptlari(McpPromptServisi promptServisi) {
        this.promptServisi = promptServisi;
    }

    @Bean
    public List<SyncPromptSpecification> mcpPromptListesi() {
        PromptArgument hafta = new PromptArgument("hafta", "Hafta numarası (1-12)", true);
        PromptArgument gun = new PromptArgument("gun", "Gün numarası (opsiyonel)", false);

        return List.of(
                prompt("ogretmen-sistem",
                        "Öğretmen sistem kimliği + aktif hafta/gün bağlamı. İstemci bunu sistem "
                                + "talimatı olarak kullanır; muhakeme istemcide.",
                        List.of(hafta, gun),
                        (ex, req) -> promptServisi.ogretmenSistem(
                                tamsayi(req.arguments(), "hafta"), tamsayiOpsiyonel(req.arguments(), "gun"))),

                prompt("sokratik",
                        "Sokratik sohbet talimatı: doğrudan cevap yerine yönlendirici sorular. "
                                + "Sistem kimliği + davranış kurallarını içerir.",
                        List.of(hafta, gun),
                        (ex, req) -> promptServisi.sokratik(
                                tamsayi(req.arguments(), "hafta"), tamsayiOpsiyonel(req.arguments(), "gun"))),

                prompt("degerlendir",
                        "Öğrencinin bir gönderimini sabit rubrikle (JSON çıktı) değerlendirme talimatı. "
                                + "'gonderim' = öğrencinin metni; 'tur' = gönderim türü (opsiyonel).",
                        List.of(hafta, gun,
                                new PromptArgument("tur", "Gönderim türü (opsiyonel)", false),
                                new PromptArgument("gonderim", "Öğrencinin değerlendirilecek metni", true)),
                        (ex, req) -> promptServisi.degerlendir(
                                tamsayi(req.arguments(), "hafta"),
                                tamsayiOpsiyonel(req.arguments(), "gun"),
                                metinVarsayilan(req.arguments(), "tur", null),
                                metin(req.arguments(), "gonderim"))),

                prompt("atolye-degerlendir",
                        "Prompt Atölyesi: öğrencinin yazdığı prompt'u ideal çözümle kıyaslayıp rubrikle "
                                + "(JSON çıktı) değerlendirme talimatı. 'alistirma_id' + 'ogrenci_prompt'.",
                        List.of(new PromptArgument("alistirma_id", "Alıştırma id'si", true),
                                new PromptArgument("ogrenci_prompt", "Öğrencinin yazdığı prompt", true)),
                        (ex, req) -> promptServisi.atolyeDegerlendir(
                                metin(req.arguments(), "alistirma_id"),
                                metin(req.arguments(), "ogrenci_prompt"))),

                prompt("quiz-uret",
                        "Haftanın ders içeriğinden yeni çoktan seçmeli sorular üretme talimatı (JSON "
                                + "çıktı). 'hafta' + 'adet' (opsiyonel, varsayılan 5).",
                        List.of(hafta, new PromptArgument("adet", "Üretilecek soru sayısı (opsiyonel)", false)),
                        (ex, req) -> promptServisi.quizUret(
                                tamsayi(req.arguments(), "hafta"),
                                tamsayiVarsayilan(req.arguments(), "adet", 5))));
    }

    /** Ortak sarmalayıcı: metin üreticiyi tek USER mesajlı GetPromptResult'a çevirir; hataları zarif metne. */
    private static SyncPromptSpecification prompt(
            String ad, String aciklama, List<PromptArgument> argumanlar,
            BiFunction<io.modelcontextprotocol.server.McpSyncServerExchange, GetPromptRequest, String> uretici) {
        Prompt tanim = new Prompt(ad, aciklama, argumanlar);
        return new SyncPromptSpecification(tanim, (exchange, req) -> {
            String metin;
            try {
                metin = uretici.apply(exchange, req);
            } catch (BulunamadiException | IllegalArgumentException e) {
                metin = "(Prompt üretilemedi: " + e.getMessage() + ")";
            }
            return new GetPromptResult(aciklama,
                    List.of(new PromptMessage(Role.USER, new TextContent(metin))));
        });
    }
}
