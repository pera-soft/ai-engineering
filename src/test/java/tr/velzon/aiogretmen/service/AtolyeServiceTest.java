package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.AtolyeRepo;
import tr.velzon.aiogretmen.types.AtolyeDegerlendirme;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtolyeServiceTest {

    static final String GECERLI_JSON = """
            {"puan": 78, "geriBildirim": "Görevi karşılıyor; rol ve format iyi.",
             "iyilestirme": "Bir de negatif kısıt ekle: 'jargon kullanma'."}""";

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    AtolyeRepo atolyeRepo = new AtolyeRepo(Path.of("content/prompt-atolyesi"));
    AtolyeService servis = new AtolyeService(atolyeRepo, aiClient,
            new PromptFactory(Path.of("prompts")), new JsonCevapParser());

    @Test
    void kategorileriDondurur() {
        // İçerik büyüyebilir; sabit sayı yerine rep'nin verdiğiyle tutarlı ve boş olmadığını doğrula.
        assertThat(servis.kategoriler())
                .isNotEmpty()
                .hasSameSizeAs(atolyeRepo.kategoriler());
    }

    @Test
    void gecerliJsonuAtolyeDegerlendirmeyeAyristirir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(GECERLI_JSON, 120, 60, "test-model"));

        AtolyeDegerlendirme d = servis.degerlendir("birlestir", "Kıdemli teknik yazar gibi davran...");

        assertThat(d.puan()).isEqualTo(78);
        assertThat(d.geriBildirim()).contains("Görevi karşılıyor");
        assertThat(d.iyilestirme()).contains("negatif kısıt");
        verify(aiClient, times(1)).cagir(any());
    }

    @Test
    void promptAlistirmaBaglaminiIcerir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit(GECERLI_JSON, 1, 1, "m"));

        servis.degerlendir("birlestir", "benim prompt'um");

        ArgumentCaptor<AiIstek> istekler = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istekler.capture());
        String kullanici = istekler.getValue().kullaniciMesaji();
        assertThat(kullanici)
                .contains("benim prompt'um")         // öğrencinin yazdığı prompt
                .contains("Altı taşı birleştir");    // alıştırmanın tekniği rubriğe girmiş
    }

    @Test
    void bozukJsonHamMetneDuser() {
        when(aiClient.cagir(any()))
                .thenReturn(new AiYanit("Harika bir prompt olmuş, tebrikler!", 1, 1, "m"));

        AtolyeDegerlendirme d = servis.degerlendir("birlestir", "prompt");

        assertThat(d.puan()).isNull();
        assertThat(d.geriBildirim()).isEqualTo("Harika bir prompt olmuş, tebrikler!");
    }

    @Test
    void olmayanAlistirmaBulunamadiFirlatir() {
        assertThatThrownBy(() -> servis.degerlendir("yok-boyle-id", "prompt"))
                .isInstanceOf(BulunamadiException.class)
                .hasMessageContaining("yok-boyle-id");
        verify(aiClient, times(0)).cagir(any()); // alıştırma yoksa AI çağrısı da yapılmaz
    }
}
