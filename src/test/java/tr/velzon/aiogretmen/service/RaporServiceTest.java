package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.repo.SohbetRepo;
import tr.velzon.aiogretmen.types.Gonderim;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RaporServiceTest {

    @TempDir
    Path tempDir;

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    BaglamServisi baglamServisi = mock(BaglamServisi.class);
    IlerlemeRepo ilerlemeRepo = mock(IlerlemeRepo.class);
    QuizSonucRepo quizSonucRepo = mock(QuizSonucRepo.class);
    GonderimRepo gonderimRepo = mock(GonderimRepo.class);
    SohbetRepo sohbetRepo = mock(SohbetRepo.class);
    RaporService servis;

    @BeforeEach
    void kur() {
        AppProperties props = new AppProperties("src/test/resources/test-content", "prompts",
                null, tempDir.resolve("raporlar").toString(), null, null, null);
        servis = new RaporService(props, aiClient, baglamServisi,
                new PromptFactory(Path.of("prompts")),
                new ContentRepo(Path.of("src/test/resources/test-content")),
                ilerlemeRepo, quizSonucRepo, gonderimRepo, sohbetRepo, () -> "yerel");

        when(baglamServisi.sistemBaglami(anyInt(), any())).thenReturn("SISTEM");
        when(ilerlemeRepo.findByKullaniciIdAndHafta(anyString(), anyInt())).thenReturn(List.of());
        when(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of(
                new QuizSonucKaydi(1L, "yerel", 1, "STATIK", 80, null, Instant.now())));
        when(gonderimRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of(
                new Gonderim(1L, "yerel", 1, 1, "OZET", "özet içeriği", "{\"puan\":65}", 65, Instant.now())));
        when(sohbetRepo.findByKullaniciIdAndHaftaOrderByTarihAsc(anyString(), anyInt())).thenReturn(List.of(
                new SohbetMesaji(1L, "yerel", 1, "USER", "harness nedir", Instant.now())));
        when(aiClient.cagir(any())).thenReturn(new AiYanit("# Hafta 1 Raporu\n## Güçlü yönler\n- iyi", 100, 200, "m"));
    }

    @Test
    void raporUretirVeHaftaninVerisiniPromptaKoyar() {
        var rapor = servis.rapor(1, false);

        assertThat(rapor.cacheden()).isFalse();
        assertThat(rapor.markdown()).contains("# Hafta 1 Raporu");
        assertThat(rapor.html()).contains("<h1>");

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istek.capture());
        String prompt = istek.getValue().kullaniciMesaji();
        assertThat(prompt).contains("STATIK: %80");        // quiz özeti
        assertThat(prompt).contains("özet içeriği");       // gönderim
        assertThat(prompt).contains("harness nedir");      // sohbet özeti
        assertThat(prompt).contains("Gün 1");              // görev özeti
    }

    @Test
    void ikinciCagriCachedenDoner_aiCagrilmaz() {
        servis.rapor(1, false);
        var ikinci = servis.rapor(1, false);

        assertThat(ikinci.cacheden()).isTrue();
        verify(aiClient, times(1)).cagir(any()); // sadece ilk üretim
    }

    @Test
    void yenileCacheiAtlarVeYenidenUretir() {
        servis.rapor(1, false);
        var yenilenen = servis.rapor(1, true);

        assertThat(yenilenen.cacheden()).isFalse();
        verify(aiClient, times(2)).cagir(any());
    }
}
