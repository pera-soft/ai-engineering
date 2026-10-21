package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.AtolyeRepo;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MCP prompt metinlerinin embeddingsiz (LLM/embedding çağrısı olmadan) doğru şablonlarla
 * üretildiğini doğrular. BaglamServisi mock'lanır — sistem bağlamı bu testin konusu değil.
 */
class McpPromptServisiTest {

    BaglamServisi baglam = mock(BaglamServisi.class);
    PromptFactory promptFactory = new PromptFactory(Path.of("prompts"));
    ContentRepo contentRepo = new ContentRepo(Path.of("src/test/resources/test-content"));
    AtolyeRepo atolyeRepo = new AtolyeRepo(Path.of("content/prompt-atolyesi"));

    McpPromptServisi servis = new McpPromptServisi(baglam, promptFactory, contentRepo, atolyeRepo);

    @Test
    void sokratikSistemBaglamiVeDavranisKurallariniBirlestirir() {
        when(baglam.sistemBaglamiEmbeddingsiz(1, 1)).thenReturn("SISTEM_ISARETI");

        String p = servis.sokratik(1, 1);

        assertThat(p).contains("SISTEM_ISARETI")   // embeddingsiz sistem bağlamı
                .contains("Sohbet davranışın");     // sokratik-sohbet.st kuralları eklendi
    }

    @Test
    void degerlendirGonderimiVeRubrigiIcerir() {
        when(baglam.sistemBaglamiEmbeddingsiz(2, null)).thenReturn("SISTEM");

        String p = servis.degerlendir(2, null, "yazılı gönderim", "öğrencinin cevabı");

        assertThat(p).contains("SISTEM")
                .contains("Rubrik")                 // degerlendirme.st
                .contains("öğrencinin cevabı");     // gömülen gönderim
    }

    @Test
    void quizUretHaftaIcerigiVeMevcutSorulariGomer() {
        String p = servis.quizUret(1, 5);

        assertThat(p)
                .contains("Bu ders içeriğidir")     // test-content hafta-01/gun-1 dersi
                .contains("İki kere iki")           // hafta-01 statik quiz sorusu → tekrar yasağı
                .contains("5");                     // istenen adet
    }

    @Test
    void atolyeDegerlendirIdealCozumVeOgrenciPromptunuKiyaslar() {
        // content/prompt-atolyesi'nde geçerli alıştırma id'si (AtolyeServiceTest ile aynı)
        String p = servis.atolyeDegerlendir("birlestir", "benim prompt'um");

        assertThat(p).contains("Altı taşı birleştir")   // alıştırmanın tekniği rubriğe girdi
                .contains("benim prompt'um");            // öğrencinin yazdığı prompt
    }

    @Test
    void atolyeDegerlendirOlmayanIdBulunamadiFirlatir() {
        assertThatThrownBy(() -> servis.atolyeDegerlendir("yok-boyle", "p"))
                .isInstanceOf(BulunamadiException.class)
                .hasMessageContaining("yok-boyle");
    }

    @Test
    void quizUretOlmayanHaftaBulunamadiFirlatir() {
        assertThatThrownBy(() -> servis.quizUret(42, 5))
                .isInstanceOf(BulunamadiException.class);
    }
}
