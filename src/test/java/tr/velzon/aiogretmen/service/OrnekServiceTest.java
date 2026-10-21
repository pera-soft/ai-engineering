package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.PromptFactory;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrnekServiceTest {

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    BaglamServisi baglamServisi = mock(BaglamServisi.class);
    OrnekService servis = new OrnekService(aiClient, baglamServisi, new PromptFactory(Path.of("prompts")));

    @Test
    void gorevMetniniKullaniciMesajinaKoyarVeUcuzModelleCagirir() {
        when(baglamServisi.sistemBaglami(anyInt(), any())).thenReturn("SISTEM");
        when(aiClient.cagir(any())).thenReturn(new AiYanit("💡 Örnek: 1. ... 2. ...", 40, 120, "hizli"));

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        String ornek = servis.ornekUret(1, 1, "5 maddede 'yeni rolüm ne?' yaz");

        assertThat(ornek).startsWith("💡 Örnek");
        org.mockito.Mockito.verify(aiClient).cagir(istek.capture());
        AiIstek g = istek.getValue();
        assertThat(g.modelTuru()).isEqualTo(ModelTuru.HIZLI);            // ucuz model
        assertThat(g.kullaniciMesaji()).contains("5 maddede");          // görev kullanıcı mesajında
        assertThat(g.sistemPrompt()).contains("REDDETME");              // ornek.st yardımcı prompt eklendi
    }

    @Test
    void aciklaUretGoreviAcarUcuzModelleVeAciklamaPromptuKullanir() {
        when(baglamServisi.sistemBaglami(anyInt(), any())).thenReturn("SISTEM");
        when(aiClient.cagir(any())).thenReturn(new AiYanit("Bu görevde şunu yapman bekleniyor…", 30, 90, "hizli"));

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        String a = servis.aciklaUret(1, 1, "5 maddede 'yeni rolüm ne?' yaz");

        assertThat(a).startsWith("Bu görevde");
        org.mockito.Mockito.verify(aiClient).cagir(istek.capture());
        AiIstek g = istek.getValue();
        assertThat(g.modelTuru()).isEqualTo(ModelTuru.HIZLI);
        assertThat(g.kullaniciMesaji()).contains("5 maddede");
        assertThat(g.sistemPrompt()).contains("SADE Türkçeyle AÇ"); // aciklama.st eklendi (ornek DEĞİL)
    }
}
