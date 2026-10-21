package tr.velzon.aiogretmen.mcp;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.service.McpPromptServisi;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** MCP prompt'larının argümanları doğru servise geçirip tek USER mesajı ürettiğini doğrular. */
class OgretmenMcpPromptlariTest {

    McpPromptServisi promptServisi = mock(McpPromptServisi.class);
    OgretmenMcpPromptlari config = new OgretmenMcpPromptlari(promptServisi);
    List<SyncPromptSpecification> promptlar = config.mcpPromptListesi();

    SyncPromptSpecification prompt(String ad) {
        return promptlar.stream().filter(p -> p.prompt().name().equals(ad)).findFirst().orElseThrow();
    }

    static String metin(GetPromptResult r) {
        assertThat(r.messages()).hasSize(1);
        assertThat(r.messages().get(0).role()).isEqualTo(Role.USER);
        return ((TextContent) r.messages().get(0).content()).text();
    }

    GetPromptResult cagir(String ad, Map<String, Object> args) {
        return prompt(ad).promptHandler().apply(null, new GetPromptRequest(ad, args));
    }

    @Test
    void besPromptTanimli() {
        assertThat(promptlar).extracting(p -> p.prompt().name())
                .containsExactlyInAnyOrder("ogretmen-sistem", "sokratik", "degerlendir",
                        "atolye-degerlendir", "quiz-uret");
    }

    @Test
    void ogretmenSistemHaftaVeGunuGecirir() {
        when(promptServisi.ogretmenSistem(1, 2)).thenReturn("SISTEM METNİ");

        assertThat(metin(cagir("ogretmen-sistem", Map.of("hafta", 1, "gun", 2))))
                .isEqualTo("SISTEM METNİ");
    }

    @Test
    void ogretmenSistemGunOpsiyonel() {
        when(promptServisi.ogretmenSistem(3, null)).thenReturn("YALNIZ HAFTA");

        assertThat(metin(cagir("ogretmen-sistem", Map.of("hafta", 3)))).isEqualTo("YALNIZ HAFTA");
    }

    @Test
    void degerlendirGonderimiGecirir() {
        when(promptServisi.degerlendir(eq(1), eq(null), eq(null), eq("öğrenci metni")))
                .thenReturn("DEĞERLENDİRME TALİMATI");

        assertThat(metin(cagir("degerlendir", Map.of("hafta", 1, "gonderim", "öğrenci metni"))))
                .isEqualTo("DEĞERLENDİRME TALİMATI");
    }

    @Test
    void atolyeDegerlendirBulunamadiZarifMetne() {
        when(promptServisi.atolyeDegerlendir(eq("yok"), eq("p")))
                .thenThrow(new BulunamadiException("Alıştırma bulunamadı: yok"));

        assertThat(metin(cagir("atolye-degerlendir", Map.of("alistirma_id", "yok", "ogrenci_prompt", "p"))))
                .contains("üretilemedi").contains("bulunamadı");
    }

    @Test
    void quizUretAdetVarsayilan() {
        when(promptServisi.quizUret(1, 5)).thenReturn("QUIZ TALİMATI");

        assertThat(metin(cagir("quiz-uret", Map.of("hafta", 1)))).isEqualTo("QUIZ TALİMATI");
    }
}
