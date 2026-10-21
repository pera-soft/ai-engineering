package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.repo.SohbetRepo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SifirlamaServiceTest {

    IlerlemeRepo ilerlemeRepo = mock(IlerlemeRepo.class);
    GonderimRepo gonderimRepo = mock(GonderimRepo.class);
    QuizSonucRepo quizSonucRepo = mock(QuizSonucRepo.class);
    SohbetRepo sohbetRepo = mock(SohbetRepo.class);
    MulakatCevapRepo mulakatCevapRepo = mock(MulakatCevapRepo.class);
    HafizaService hafizaService = mock(HafizaService.class);
    RaporService raporService = mock(RaporService.class);
    SifirlamaService servis = new SifirlamaService(
            ilerlemeRepo, gonderimRepo, quizSonucRepo, sohbetRepo, mulakatCevapRepo,
            hafizaService, raporService, () -> "yerel");

    @Test
    void yalnizAktifKullanicininTumTablolariniTemizler() {
        servis.sifirla();
        verify(ilerlemeRepo).deleteByKullaniciId("yerel");
        verify(gonderimRepo).deleteByKullaniciId("yerel");
        verify(quizSonucRepo).deleteByKullaniciId("yerel");
        verify(sohbetRepo).deleteByKullaniciId("yerel");
    }

    @Test
    void mulakatCevaplariniDaTemizler() {
        servis.sifirla();
        verify(mulakatCevapRepo).deleteByKullaniciId("yerel"); // Mülakat modülü izole sıfırlama
    }

    @Test
    void ogrenciHafizasiniDaTemizler() {
        servis.sifirla();
        verify(hafizaService).ogrenciNotlariniSil("yerel"); // AI eski öğrenciyi hatırlamasın (inceleme bulgusu)
    }

    @Test
    void raporOnbeleginiDeTemizler() {
        servis.sifirla();
        verify(raporService).raporOnbeleginiSil("yerel"); // eski ilerlemenin raporu yeni başlangıçta görünmesin
    }

    @Test
    void acikKimlikleYalnizOKullaniciyiTemizler() {
        servis.sifirla("a@pera.software");
        verify(ilerlemeRepo).deleteByKullaniciId("a@pera.software");
        verify(hafizaService).ogrenciNotlariniSil("a@pera.software");
        verify(raporService).raporOnbeleginiSil("a@pera.software");
    }
}
