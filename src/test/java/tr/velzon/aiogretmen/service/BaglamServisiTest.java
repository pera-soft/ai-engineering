package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.CalismaModu;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BaglamServisiTest {

    ContentRepo contentRepo = new ContentRepo(Path.of("src/test/resources/test-content"));
    IlerlemeRepo ilerlemeRepo = mock(IlerlemeRepo.class);
    QuizSonucRepo quizSonucRepo = mock(QuizSonucRepo.class);
    PromptFactory promptFactory = new PromptFactory(Path.of("prompts"));
    HafizaService hafizaService = mock(HafizaService.class);
    KitapService kitapService = mock(KitapService.class);
    KaynakService kaynakService = mock(KaynakService.class);
    BaglamServisi servis = new BaglamServisi(contentRepo, ilerlemeRepo, quizSonucRepo,
            promptFactory, hafizaService, kitapService, kaynakService, () -> "yerel", CalismaModu.yerelIcin());

    @Test
    void sistemBaglamiHaftaVeGunIcerigiIcerir() {
        when(ilerlemeRepo.findByKullaniciIdAndHafta("yerel", 1)).thenReturn(List.of(
                new GorevIlerlemeKaydi(1L, "yerel", 1, 1, 0, true, Instant.now())));
        when(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of());
        when(hafizaService.hafizaBaglami(1)).thenReturn("HAFIZA_ISARETI");
        when(kitapService.baglamIcinAnlam(anyString(), anyInt())).thenReturn("KITAP_ISARETI");
        when(kaynakService.baglamIcin(1)).thenReturn("KAYNAK_ISARETI");

        String baglam = servis.sistemBaglami(1, 1);

        assertThat(baglam).contains("KITAP_ISARETI");    // kitap bilgi tabanı enjekte
        assertThat(baglam).contains("KAYNAK_ISARETI");   // Türkçe çalışma metinleri enjekte

        assertThat(baglam).contains("Test Haftası");        // hafta.md ham içeriği (kabul: brief §12)
        assertThat(baglam).contains("İlk gün dersi");       // gun-1.md ham içeriği
        assertThat(baglam).contains("1/3 tamamlandı");      // ilerleme özeti
        assertThat(baglam).contains("HAFIZA_ISARETI");      // öğrenci hafızası enjekte
        assertThat(baglam).contains("yazılım mimarı");      // kimlik
    }

    @Test
    void gunVerilmezseYalnizHaftaBaglami() {
        when(ilerlemeRepo.findByKullaniciIdAndHafta("yerel", 1)).thenReturn(List.of());
        when(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of());
        when(hafizaService.hafizaBaglami(1)).thenReturn("");
        when(kitapService.baglamIcinAnlam(anyString(), anyInt())).thenReturn("");
        when(kaynakService.baglamIcin(1)).thenReturn("");

        String baglam = servis.sistemBaglami(1, null);

        assertThat(baglam).contains("belirli bir gün seçilmedi");
        assertThat(baglam).contains("quiz henüz çözülmedi");
    }

    /** Faz 9b: site profilinde kitap notları/vektör deposu ORTAK → prompt'a hiç girmez (sızıntı yok). */
    @Test
    void siteModundaKitapBaglamiEnjekteEdilmez() {
        BaglamServisi site = new BaglamServisi(contentRepo, ilerlemeRepo, quizSonucRepo,
                promptFactory, hafizaService, kitapService, kaynakService, () -> "a@pera.software",
                CalismaModu.siteIcin());
        when(ilerlemeRepo.findByKullaniciIdAndHafta(anyString(), anyInt())).thenReturn(List.of());
        when(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of());
        when(hafizaService.hafizaBaglami(1)).thenReturn("HAFIZA");
        when(kaynakService.baglamIcin(1)).thenReturn("KAYNAK_ISARETI");

        String baglam = site.sistemBaglami(1, 1, "soru");

        assertThat(baglam).contains("KAYNAK_ISARETI").contains("HAFIZA");
        verifyNoInteractions(kitapService);
    }
}
