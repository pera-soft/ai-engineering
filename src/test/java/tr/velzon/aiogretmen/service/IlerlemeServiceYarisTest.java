package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * İnceleme bulgusu: eşzamanlı ilk toggle'da (çift tık) iki istek de kayıt bulamayıp
 * INSERT dener; ikincisi UNIQUE ihlaliyle 500 dönerdi. Düzeltme: çakışmada bir kez
 * yeniden oku-ve-çevir.
 */
class IlerlemeServiceYarisTest {

    IlerlemeRepo ilerlemeRepo = mock(IlerlemeRepo.class);
    QuizSonucRepo quizSonucRepo = mock(QuizSonucRepo.class);
    IlerlemeService servis = new IlerlemeService(ilerlemeRepo, quizSonucRepo);

    @Test
    void uniqueIhlalindeYenidenOkuyupCevirir() {
        GorevIlerlemeKaydi rakibinKaydi = new GorevIlerlemeKaydi(9L, "yerel", 1, 1, 0, true, Instant.now());

        // 1. okuma: kayıt yok (yarış penceresi) → INSERT dener → rakip önce yazdığı için çakışır
        when(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("yerel", 1, 1, 0))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(rakibinKaydi));
        when(ilerlemeRepo.save(any()))
                .thenThrow(new DuplicateKeyException("UNIQUE ihlali"))
                .thenAnswer(inv -> inv.getArgument(0));

        boolean durum = servis.gorevToggle("yerel", 1, 1, 0);

        // rakip true yazmıştı; bu istek toggle olarak false'a çevirir — 500 yerine anlamlı sonuç
        assertThat(durum).isFalse();
        verify(ilerlemeRepo, times(2)).save(any());
    }
}
