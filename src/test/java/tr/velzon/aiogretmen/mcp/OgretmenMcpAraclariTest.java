package tr.velzon.aiogretmen.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.service.AtolyeKatalogService;
import tr.velzon.aiogretmen.service.IlerlemeService;
import tr.velzon.aiogretmen.service.KaynakService;
import tr.velzon.aiogretmen.service.KullanimService;
import tr.velzon.aiogretmen.service.MufredatService;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.HaftaDetay;
import tr.velzon.aiogretmen.types.HaftaOzeti;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * MCP araçlarının LLM'siz servislere doğru bağlandığını ve doğru JSON döndürdüğünü doğrular.
 * (LLM-izolasyonunun derleme-zamanı garantisi {@code MimariTestleri.claude_yalniz_ai_paketinden}'dedir;
 * burada davranış + veri doğruluğu test edilir.)
 */
class OgretmenMcpAraclariTest {

    MufredatService mufredat = mock(MufredatService.class);
    KaynakService kaynak = mock(KaynakService.class);
    AtolyeKatalogService atolye = mock(AtolyeKatalogService.class);
    IlerlemeService ilerleme = mock(IlerlemeService.class);
    KullanimService kullanim = mock(KullanimService.class);
    ObjectMapper om = new ObjectMapper();

    OgretmenMcpAraclari config = new OgretmenMcpAraclari(mufredat, kaynak, atolye, ilerleme, kullanim, om);
    List<SyncToolSpecification> araclar = config.mcpAracListesi();

    SyncToolSpecification arac(String ad) {
        return araclar.stream().filter(a -> a.tool().name().equals(ad)).findFirst().orElseThrow();
    }

    CallToolResult cagir(String ad, Map<String, Object> args) {
        return arac(ad).callHandler().apply(null, new CallToolRequest(ad, args));
    }

    static String metin(CallToolResult r) {
        return ((TextContent) r.content().get(0)).text();
    }

    @Test
    void araclarAzSayidaVeTanimli() {
        // MCP "context tax": araç sayısı bilinçli olarak az tutulur
        assertThat(araclar).hasSize(8);
        assertThat(araclar).allSatisfy(a -> {
            assertThat(a.tool().name()).isNotBlank();
            assertThat(a.tool().description()).isNotBlank();
            assertThat(a.tool().inputSchema()).isNotNull();
        });
    }

    @Test
    void programOzetiJsonDondurur() {
        // Test'te SecurityContext yok → McpKullaniciKimligi.kullaniciKimligiVeyaYerel() "yerel" döner
        when(mufredat.programOzeti("yerel")).thenReturn(List.of(
                new HaftaOzeti(1, "Mimari Temelleri", "hedef", 3, 1, 33, 80)));

        String json = metin(cagir("program_ozeti", Map.of()));

        assertThat(json).contains("Mimari Temelleri").contains("33");
        verifyNoInteractions(kaynak, atolye, ilerleme, kullanim); // yalnız gereken servis çağrıldı
    }

    @Test
    void haftaDetayNoArgumaniniGecirir() {
        when(mufredat.haftaDetay("yerel", 2)).thenReturn(
                new HaftaDetay(2, "İkinci Hafta", "hedef", "k", List.of(), List.of(), null));

        assertThat(metin(cagir("hafta_detay", Map.of("no", 2)))).contains("İkinci Hafta");
    }

    @Test
    void haftaDetayBulunamadiZarifHataDoner() {
        when(mufredat.haftaDetay("yerel", 99)).thenThrow(new BulunamadiException("Hafta bulunamadı: 99"));

        CallToolResult r = cagir("hafta_detay", Map.of("no", 99));

        assertThat(r.isError()).isTrue();
        assertThat(metin(r)).contains("bulunamadı");
    }

    @Test
    void gorevIsaretleYeniDurumuDondurur() {
        when(ilerleme.gorevToggle("yerel", 1, 2, 0)).thenReturn(true);

        String json = metin(cagir("gorev_isaretle",
                Map.of("hafta", 1, "gun", 2, "gorev_index", 0)));

        assertThat(json).contains("tamamlandi").contains("true");
    }

    @Test
    void statikQuizKaydetOpsiyonelDetaysizCalisir() {
        when(ilerleme.statikQuizKaydet("yerel", 1, 90, null))
                .thenReturn(new QuizSonucKaydi(7L, "yerel", 1, "STATIK", 90, null, Instant.now()));

        String json = metin(cagir("statik_quiz_kaydet", Map.of("hafta", 1, "puan", 90)));

        assertThat(json).contains("\"puan\":90").contains("STATIK");
    }

    @Test
    void kullanimIstatistikSayaciSunar() {
        when(kullanim.istatistik()).thenReturn(Map.of("aiAktif", false, "toplamCagri", 0L));

        assertThat(metin(cagir("kullanim_istatistik", Map.of()))).contains("aiAktif");
    }
}
