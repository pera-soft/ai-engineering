package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.MulakatFixture;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.MulakatRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI sonuç-açıklayıcının prompt'u SUNUCUDAKİ kanonik soruyla doldurduğunu ve ucuz modeli
 * kullandığını sınar. İçerik BELİRLENİMCİ fixture'dan gelir (üretim content'e bağlanmaz);
 * assertion'lar kelimesi-kelimesine üretim metnini değil, fixture değerlerini + davranışı doğrular.
 */
class MulakatAciklamaServiceTest {

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);

    @TempDir
    Path tmp;

    MulakatAciklamaService servis;

    @BeforeEach
    void init() throws IOException {
        servis = new MulakatAciklamaService(aiClient, new MulakatRepo(MulakatFixture.yaz(tmp)),
                new PromptFactory(Path.of("prompts")));
    }

    @Test
    void yanlisCevaptaPromptSecilenVeDogruSikiIcerirUcuzModel() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("Seçtiğin şık yanlış çünkü…", 40, 120, "hizli"));

        // f-1 doğru cevabı B (index 1); index 0 (A) seçimi → YANLIŞ
        String metin = servis.acikla("f-1", 0);
        assertThat(metin).startsWith("Seçtiğin şık yanlış");

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istek.capture());
        AiIstek g = istek.getValue();
        assertThat(g.islem()).isEqualTo(Islem.MULAKAT);
        assertThat(g.modelTuru()).isEqualTo(ModelTuru.HIZLI);
        assertThat(g.sistemPrompt())
                .contains("YANLIŞ")                 // sonuç placeholder'ı
                .contains(MulakatFixture.F1_A)      // öğrencinin seçtiği şık (A)
                .contains(MulakatFixture.F1_B);     // doğru şık (B) — sunucudan
    }

    @Test
    void dogruCevaptaSonucDogruIsaretlenir() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("Evet, doğru muhakeme.", 20, 60, "hizli"));

        servis.acikla("f-1", 1); // B = doğru

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istek.capture());
        assertThat(istek.getValue().sistemPrompt()).contains("DOĞRU");
    }

    @Test
    void aralikDisiSecimIllegalArgument() {
        assertThatThrownBy(() -> servis.acikla("f-1", 9))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void olmayanSoruBulunamadiFirlatir() {
        assertThatThrownBy(() -> servis.acikla("yok-boyle-id", 0))
                .isInstanceOf(BulunamadiException.class);
    }
}
