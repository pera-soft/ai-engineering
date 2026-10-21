package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.SohbetRepo;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SohbetServiceTest {

    OgretmenAiClient aiClient = mock(OgretmenAiClient.class);
    BaglamServisi baglamServisi = mock(BaglamServisi.class);
    SohbetRepo sohbetRepo = mock(SohbetRepo.class);
    HafizaService hafizaService = mock(HafizaService.class);
    SohbetService servis = new SohbetService(aiClient, baglamServisi,
            new PromptFactory(Path.of("prompts")), sohbetRepo, hafizaService, () -> "yerel");

    @BeforeEach
    void kur() {
        when(baglamServisi.sistemBaglami(anyInt(), any(), any())).thenReturn("HAFTA_BAGLAMI");
        when(sohbetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sohbetRepo.findTop20ByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of());
        when(aiClient.cagir(any())).thenReturn(new AiYanit("Peki bunu neden yaptın?", 10, 5, "m"));
    }

    @Test
    void sistemPromptuHaftaBaglamiVeSokratikKurallariIcerir() {
        servis.mesajGonder(1, 2, "Harness nedir?");

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istek.capture());
        assertThat(istek.getValue().sistemPrompt()).contains("HAFTA_BAGLAMI");
        assertThat(istek.getValue().sistemPrompt()).contains("EN FAZLA 1 soru"); // sokratik kurallar
        assertThat(istek.getValue().kullaniciMesaji()).isEqualTo("Harness nedir?");
    }

    @Test
    void gecmisEskidenYeniyeSiralanirVeIstegeEklenir() {
        when(sohbetRepo.findTop20ByKullaniciIdAndHaftaOrderByTarihDesc("yerel", 1)).thenReturn(List.of(
                new SohbetMesaji(2L, "yerel", 1, "ASSISTANT", "yeni cevap", Instant.parse("2026-07-03T08:01:00Z")),
                new SohbetMesaji(1L, "yerel", 1, "USER", "eski soru", Instant.parse("2026-07-03T08:00:00Z"))));

        servis.mesajGonder(1, null, "devam");

        ArgumentCaptor<AiIstek> istek = ArgumentCaptor.forClass(AiIstek.class);
        verify(aiClient).cagir(istek.capture());
        assertThat(istek.getValue().gecmis()).extracting("icerik")
                .containsExactly("eski soru", "yeni cevap"); // kronolojik
    }

    @Test
    void herIkiMesajDaKaydedilir() {
        servis.mesajGonder(3, null, "soru");

        ArgumentCaptor<SohbetMesaji> kayitlar = ArgumentCaptor.forClass(SohbetMesaji.class);
        verify(sohbetRepo, org.mockito.Mockito.times(2)).save(kayitlar.capture());
        assertThat(kayitlar.getAllValues().get(0).rol()).isEqualTo("USER");
        assertThat(kayitlar.getAllValues().get(1).rol()).isEqualTo("ASSISTANT");
        assertThat(kayitlar.getAllValues().get(1).icerik()).contains("neden yaptın");
    }

    @Test
    void hafizaHerDortKullaniciMesajindaBirGuncellenir() {
        when(sohbetRepo.countByKullaniciIdAndHaftaAndRol("yerel", 1, "USER")).thenReturn(4L);
        servis.mesajGonder(1, null, "dördüncü mesaj");

        ArgumentCaptor<String> icerik = ArgumentCaptor.forClass(String.class);
        verify(hafizaService).guncelle(org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq("SOHBET"), icerik.capture());
        assertThat(icerik.getValue())
                .contains("dördüncü mesaj")       // öğrencinin gerçek mesajı
                .contains("neden yaptın");        // öğretmenin gerçek yanıtı

        when(sohbetRepo.countByKullaniciIdAndHaftaAndRol("yerel", 1, "USER")).thenReturn(5L);
        org.mockito.Mockito.reset(hafizaService);
        servis.mesajGonder(1, null, "beşinci mesaj");
        verify(hafizaService, never()).guncelle(anyInt(), anyString(), anyString());
    }
}
