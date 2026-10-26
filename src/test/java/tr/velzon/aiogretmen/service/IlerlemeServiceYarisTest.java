package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbAction;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.data.relational.core.conversion.IdValueSource;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * İnceleme bulgusu: eşzamanlı ilk işaretlemede (çift tık / MCP retry) iki istek de kayıt bulamayıp
 * INSERT dener; ikincisi UNIQUE ihlaliyle 500 dönerdi. Düzeltme: çakışmada bir kez yeniden oku-ve-ayarla.
 * Hedef-durum (set) semantiğinde yeniden deneme güvenlidir: rakibin yazdığı satır ne olursa olsun
 * sonuç istenen durumdur (toggle'da rakibin işareti geri alınırdı).
 */
class IlerlemeServiceYarisTest {

    IlerlemeRepo ilerlemeRepo = mock(IlerlemeRepo.class);
    QuizSonucRepo quizSonucRepo = mock(QuizSonucRepo.class);
    IlerlemeService servis = new IlerlemeService(ilerlemeRepo, quizSonucRepo);

    @Test
    void uniqueIhlalindeYenidenOkuyupHedefDurumaAyarlar() {
        GorevIlerlemeKaydi rakibinKaydi = new GorevIlerlemeKaydi(9L, "yerel", 1, 1, 0, true, Instant.now());

        // 1. okuma: kayıt yok (yarış penceresi) → INSERT dener → rakip önce yazdığı için çakışır
        when(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("yerel", 1, 1, 0))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(rakibinKaydi));
        when(ilerlemeRepo.save(any()))
                .thenThrow(new DuplicateKeyException("UNIQUE ihlali"))
                .thenAnswer(inv -> inv.getArgument(0));

        boolean durum = servis.gorevDurumuAyarla("yerel", 1, 1, 0, false);

        // rakip true yazmıştı; bu istek false istedi → false (500 yerine istenen sonuç)
        assertThat(durum).isFalse();
        verify(ilerlemeRepo, times(2)).save(any());
    }

    @Test
    void springDataJdbcSarmaliCakismaDaYakalanir() {
        // Gerçek DB'de save() DuplicateKeyException'ı DbActionExecutionException'a (RuntimeException,
        // DataAccessException DEĞİL) sarar — yalnız DataIntegrityViolationException yakalamak yetmezdi
        // (IlerlemeServiceEszamanliTest gerçek H2'de yakaladı).
        GorevIlerlemeKaydi yeni = GorevIlerlemeKaydi.yeni("yerel", 1, 1, 0, true);
        GorevIlerlemeKaydi rakibinKaydi = new GorevIlerlemeKaydi(9L, "yerel", 1, 1, 0, false, Instant.now());
        when(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("yerel", 1, 1, 0))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(rakibinKaydi));
        when(ilerlemeRepo.save(any()))
                .thenThrow(new DbActionExecutionException(new DbAction.InsertRoot<>(yeni, IdValueSource.GENERATED),
                        new DuplicateKeyException("UNIQUE ihlali")))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThat(servis.gorevDurumuAyarla("yerel", 1, 1, 0, true)).isTrue();
        verify(ilerlemeRepo, times(2)).save(any());
    }

    @Test
    void cakismaDisiHataYenidenDenenmez() {
        when(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("yerel", 1, 1, 0))
                .thenReturn(Optional.empty());
        when(ilerlemeRepo.save(any())).thenThrow(new IllegalStateException("bağlantı koptu"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> servis.gorevDurumuAyarla("yerel", 1, 1, 0, true))
                .isInstanceOf(IllegalStateException.class);
        verify(ilerlemeRepo, times(1)).save(any());
    }

    @Test
    void rakipAyniHedefiYazdiysaYenidenYazmaz() {
        GorevIlerlemeKaydi rakibinKaydi = new GorevIlerlemeKaydi(9L, "yerel", 1, 1, 0, true, Instant.now());
        when(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("yerel", 1, 1, 0))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(rakibinKaydi));
        when(ilerlemeRepo.save(any())).thenThrow(new DuplicateKeyException("UNIQUE ihlali"));

        boolean durum = servis.gorevDurumuAyarla("yerel", 1, 1, 0, true);

        // çift tıkın ikinci isteği: rakip zaten true yazdı → aynı sonuç, ikinci yazma yok
        assertThat(durum).isTrue();
        verify(ilerlemeRepo, times(1)).save(any());
    }

    @Test
    void kayitZatenHedefDurumdaysaYazmaz() {
        when(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("yerel", 1, 1, 0))
                .thenReturn(Optional.of(new GorevIlerlemeKaydi(9L, "yerel", 1, 1, 0, true, Instant.now())));

        assertThat(servis.gorevDurumuAyarla("yerel", 1, 1, 0, true)).isTrue();
        verify(ilerlemeRepo, never()).save(any()); // tamamlanma zamanı (tarih) tekrarla ezilmez
    }
}
