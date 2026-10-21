package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.types.Gonderim;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DegerlendirmeServiceTest {

    static final String GECERLI_JSON = """
            {"puan": 65, "ozet": "Fena değil.", "dogru_noktalar": [], "eksikler": ["derinlik"],
             "yanlis_anlamalar": [], "sonraki_adim": "Örnek ekle.", "tekrar_onerisi": null}""";

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    BaglamServisi baglamServisi = mock(BaglamServisi.class);
    GonderimRepo gonderimRepo = mock(GonderimRepo.class);
    HafizaService hafizaService = mock(HafizaService.class);
    DegerlendirmeService servis = new DegerlendirmeService(aiClient, baglamServisi,
            new PromptFactory(Path.of("prompts")), new JsonCevapParser(), gonderimRepo, hafizaService,
            () -> "yerel");

    @BeforeEach
    void kur() {
        when(baglamServisi.sistemBaglami(anyInt(), any())).thenReturn("SISTEM");
        // save() id atanmış kopya döndürür (H2 IDENTITY simülasyonu)
        when(gonderimRepo.save(any())).thenAnswer(inv -> {
            Gonderim g = inv.getArgument(0);
            return g.id() != null ? g
                    : new Gonderim(42L, g.kullaniciId(), g.hafta(), g.gun(), g.tur(), g.icerik(),
                            g.degerlendirmeJson(), g.puan(), g.tarih());
        });
    }

    @Test
    void gecerliJsonTekCagridaDegerlendirir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(GECERLI_JSON, 100, 50, "test-model"));

        var sonuc = servis.degerlendir(1, 2, "OZET", "Benim özetim");

        assertThat(sonuc.degerlendirme().puan()).isEqualTo(65);
        assertThat(sonuc.gonderimId()).isEqualTo(42L);
        verify(aiClient, times(1)).cagir(any());

        // inceleme bulgusu: hafızaya giden İÇERİK de doğrulanmalı — çağrının varlığı yetmez
        ArgumentCaptor<String> hafizaIcerik = ArgumentCaptor.forClass(String.class);
        verify(hafizaService).guncelle(org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq("DEGERLENDIRME"), hafizaIcerik.capture());
        assertThat(hafizaIcerik.getValue())
                .contains("Benim özetim")   // öğrencinin gerçek gönderimi
                .contains("\"puan\":65");   // değerlendirme JSON'u
    }

    @Test
    void gorevMetniVerildigindePromptGoreviIcerir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(GECERLI_JSON, 100, 50, "m"));

        servis.degerlendir(1, 2, "OZET", "Benim özetim", "Mikroservis sınırlarını çiz ve gerekçelendir.");

        ArgumentCaptor<AiIstek> istekler = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient, times(1)).cagir(istekler.capture());
        String prompt = istekler.getValue().kullaniciMesaji();
        assertThat(prompt).contains("Mikroservis sınırlarını çiz ve gerekçelendir.");
    }

    @Test
    void gorevMetniNullIkenEskiDavranisBozulmaz() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(GECERLI_JSON, 100, 50, "m"));

        // 5-argümanlı imzaya null geçmek, 4-argümanlı eski davranışla aynı olmalı
        servis.degerlendir(1, 2, "OZET", "Benim özetim", null);

        ArgumentCaptor<AiIstek> istekler = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient, times(1)).cagir(istekler.capture());
        String prompt = istekler.getValue().kullaniciMesaji();
        assertThat(prompt).doesNotContain("cevapladığı görev"); // görev bölümü hiç görünmemeli
        assertThat(prompt).contains("Benim özetim");            // içerik yerinde
    }

    @Test
    void bozukJsondaBirKezDuzeltmeCagrisiYapar() {
        when(aiClient.cagir(any()))
                .thenReturn(new AiYanit("Tabii! İşte puanın: 65. Harika iş!", 100, 50, "m"))
                .thenReturn(new AiYanit(GECERLI_JSON, 100, 50, "m"));

        var sonuc = servis.degerlendir(1, null, "OZET", "içerik");

        assertThat(sonuc.degerlendirme().puan()).isEqualTo(65);

        ArgumentCaptor<AiIstek> istekler = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient, times(2)).cagir(istekler.capture());
        AiIstek duzeltme = istekler.getAllValues().get(1);
        assertThat(duzeltme.kullaniciMesaji()).contains("geçerli JSON");
        assertThat(duzeltme.gecmis()).hasSize(2); // ilk istek + bozuk cevap bağlamda
    }

    @Test
    void ikiKezBozuksaZarifDusus_puanNullHamMetinOzette() {
        when(aiClient.cagir(any()))
                .thenReturn(new AiYanit("bozuk 1", 1, 1, "m"))
                .thenReturn(new AiYanit("hala bozuk 2", 1, 1, "m"));

        var sonuc = servis.degerlendir(1, null, "OZET", "içerik");

        assertThat(sonuc.degerlendirme().puan()).isNull();
        assertThat(sonuc.degerlendirme().ozet()).isEqualTo("hala bozuk 2");
        verify(gonderimRepo, times(2)).save(any()); // önce ham, sonra değerlendirmeyle güncel
    }
}
