package tr.velzon.aiogretmen.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.service.AtolyeKatalogService;
import tr.velzon.aiogretmen.service.KaynakService;
import tr.velzon.aiogretmen.service.MufredatService;
import tr.velzon.aiogretmen.types.AtolyeKategori;
import tr.velzon.aiogretmen.types.HaftaOzeti;
import tr.velzon.aiogretmen.types.Kaynak;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** MCP kaynaklarının (somut + şablon) doğru URI'lerle listelendiğini ve ham içeriği döndürdüğünü doğrular. */
class OgretmenMcpKaynaklariTest {

    MufredatService mufredat = mock(MufredatService.class);
    KaynakService kaynak = mock(KaynakService.class);
    AtolyeKatalogService atolye = mock(AtolyeKatalogService.class);
    ObjectMapper om = new ObjectMapper();

    OgretmenMcpKaynaklari config = new OgretmenMcpKaynaklari(mufredat, kaynak, atolye, om);

    void varsayilanKesif() {
        when(mufredat.programOzeti("yerel")).thenReturn(List.of(
                new HaftaOzeti(1, "Hafta Bir", "hedef", 3, 0, 0, null)));
        when(kaynak.liste()).thenReturn(List.of(
                new Kaynak("dip", "Dependency Injection", "Fowler", "2004", "u", List.of(1), null)));
        when(atolye.kategoriler()).thenReturn(List.of(
                new AtolyeKategori("temel", "Temel Yapı", "açıklama", List.of())));
    }

    static String metin(ReadResourceResult r) {
        return ((TextResourceContents) r.contents().get(0)).text();
    }

    @Test
    void somutKaynaklarHaftaKaynakAtolyeIcin() {
        varsayilanKesif();
        List<SyncResourceSpecification> kaynaklar = config.mcpKaynakListesi();

        assertThat(kaynaklar).extracting(k -> k.resource().uri())
                .contains("content://hafta/1", "kaynak://dip", "atolye://temel");
    }

    @Test
    void haftaKaynagiHamMarkdownDondurur() {
        varsayilanKesif();
        when(mufredat.hamHafta(1)).thenReturn("# HAM HAFTA METNİ");

        SyncResourceSpecification hafta = config.mcpKaynakListesi().stream()
                .filter(k -> k.resource().uri().equals("content://hafta/1")).findFirst().orElseThrow();

        ReadResourceResult r = hafta.readHandler().apply(null,
                new ReadResourceRequest("content://hafta/1"));

        assertThat(metin(r)).isEqualTo("# HAM HAFTA METNİ");
    }

    @Test
    void kaynakKaynagiCalismaMetniniDondurur() {
        varsayilanKesif();
        when(kaynak.hamKaynak("dip")).thenReturn(Map.of("metin", "Türkçe çalışma metni"));

        SyncResourceSpecification k = config.mcpKaynakListesi().stream()
                .filter(s -> s.resource().uri().equals("kaynak://dip")).findFirst().orElseThrow();

        assertThat(metin(k.readHandler().apply(null, new ReadResourceRequest("kaynak://dip"))))
                .isEqualTo("Türkçe çalışma metni");
    }

    @Test
    void gunSablonuUriDenHaftaVeGunuAyristirir() {
        SyncResourceTemplateSpecification gun = config.mcpKaynakSablonListesi().get(0);
        when(mufredat.hamGun(3, 2)).thenReturn("## Ders\ngün 3/2 içeriği");

        ReadResourceResult r = gun.readHandler().apply(null,
                new ReadResourceRequest("content://hafta/3/gun/2"));

        assertThat(gun.resourceTemplate().uriTemplate()).isEqualTo("content://hafta/{no}/gun/{gun}");
        assertThat(metin(r)).contains("gün 3/2 içeriği");
    }
}
