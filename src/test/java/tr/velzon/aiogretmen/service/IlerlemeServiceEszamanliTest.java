package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gerçek H2 + tekil indeks üzerinde eşzamanlı aynı-hedef çağrıları: hiçbiri istisna fırlatmaz,
 * tek satır kalır ve son durum hedeftir. (Toggle semantiğinde N çağrının sonucu N'nin tek/çift
 * olmasına ve yarışa bağlıydı.) Test-işlemi YOK: iş parçacıkları kendi bağlantılarında commit eder.
 */
@DataJdbcTest
@Import(IlerlemeService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class IlerlemeServiceEszamanliTest {

    static final String KULLANICI = "eszamanli@example.com";
    static final int N = 8;

    @Autowired
    IlerlemeService service;

    @Autowired
    IlerlemeRepo ilerlemeRepo;

    @AfterEach
    void temizle() {
        ilerlemeRepo.deleteByKullaniciId(KULLANICI); // işlemsiz test → paylaşılan bağlamda iz bırakma
    }

    @Test
    void ayniHedefeEszamanliNCagri_sonDurumHedefIstisnaYok() throws Exception {
        ExecutorService havuz = Executors.newFixedThreadPool(N);
        CountDownLatch baslat = new CountDownLatch(1);
        try {
            List<Future<Boolean>> sonuclar = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                sonuclar.add(havuz.submit(() -> {
                    baslat.await(); // hepsi aynı anda — ilk INSERT yarışını zorla
                    return service.gorevDurumuAyarla(KULLANICI, 3, 1, 0, true);
                }));
            }
            baslat.countDown();
            for (Future<Boolean> s : sonuclar) {
                assertThat(s.get(10, TimeUnit.SECONDS)).isTrue(); // istisna olsaydı get() fırlatırdı
            }
        } finally {
            havuz.shutdownNow();
        }

        assertThat(ilerlemeRepo.findByKullaniciIdAndHafta(KULLANICI, 3)).singleElement()
                .satisfies(k -> assertThat(k.tamamlandi()).isTrue());
    }
}
