package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.types.AiCiktisiBozukException;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.UretilenQuiz;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuizUretimServiceTest {

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    ContentRepo contentRepo = new ContentRepo(Path.of("src/test/resources/test-content"));
    QuizUretimService servis = new QuizUretimService(aiClient, contentRepo,
            new PromptFactory(Path.of("prompts")), new JsonCevapParser());

    @Test
    void gecerliCiktidaSorulariDondurur_veStatikSorulariPromptaKoyar() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("""
                {"sorular": [
                  {"soru": "Yeni soru?", "secenekler": ["a","b","c","d"], "dogru_index": 2, "aciklama": "çünkü"}
                ]}""", 100, 80, "m"));

        UretilenQuiz quiz = servis.uret(1, 5);

        assertThat(quiz.sorular()).hasSize(1);
        assertThat(quiz.sorular().get(0).dogruIndex()).isEqualTo(2);

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istek.capture());
        assertThat(istek.getValue().kullaniciMesaji())
                .contains("İki kere iki kaç eder?")   // statik quiz "SORMA" listesinde
                .contains("Bu ders içeriğidir.");     // gün içerikleri bağlamda
        assertThat(istek.getValue().modelTuru()).isEqualTo(OgretmenAiClient.ModelTuru.HIZLI);
    }

    @Test
    void bozukSorulariAyiklar_tumuBozuksa502() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("""
                {"sorular": [
                  {"soru": "3 seçenekli bozuk", "secenekler": ["a","b","c"], "dogru_index": 0, "aciklama": ""},
                  {"soru": "", "secenekler": ["a","b","c","d"], "dogru_index": 1, "aciklama": ""},
                  {"soru": "indeks taşmış", "secenekler": ["a","b","c","d"], "dogru_index": 7, "aciklama": ""},
                  {"soru": "dogru_index alanı unutulmuş", "secenekler": ["a","b","c","d"], "aciklama": ""}
                ]}""", 1, 1, "m"));

        // inceleme bulgusu: eksik dogru_index eskiden sessizce 0 sayılırdı — artık ayıklanıyor
        assertThatThrownBy(() -> servis.uret(1, 5))
                .isInstanceOf(AiCiktisiBozukException.class);
    }

    @Test
    void kesilenJsondaDuzeltmeCagrisiYapar() {
        when(aiClient.cagir(any()))
                .thenReturn(new AiYanit("{\"sorular\": [{\"soru\": \"yarıda kes", 1, 1, "m"))
                .thenReturn(new AiYanit("""
                        {"sorular": [{"soru": "Tam soru?", "secenekler": ["a","b","c","d"],
                          "dogru_index": 0, "aciklama": "x"}]}""", 1, 1, "m"));

        UretilenQuiz quiz = servis.uret(1, 5);

        assertThat(quiz.sorular()).hasSize(1);
        verify(aiClient, org.mockito.Mockito.times(2)).cagir(any());
    }

    @Test
    void ikiKezBozuksa502() {
        when(aiClient.cagir(any())).thenReturn(new AiYanit("işte soruların: 1) ...", 1, 1, "m"));

        assertThatThrownBy(() -> servis.uret(1, 5))
                .isInstanceOf(AiCiktisiBozukException.class);
        verify(aiClient, org.mockito.Mockito.times(2)).cagir(any()); // düzeltme de denendi
    }

    @Test
    void olmayanHafta404() {
        assertThatThrownBy(() -> servis.uret(99, 5))
                .isInstanceOf(BulunamadiException.class);
    }
}
