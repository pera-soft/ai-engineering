package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(IlerlemeService.class)
class IlerlemeServiceTest {

    @Autowired
    IlerlemeService service;

    @Autowired
    IlerlemeRepo ilerlemeRepo;

    @Autowired
    QuizSonucRepo quizSonucRepo;

    static final String YEREL = "yerel";

    @Test
    void ilkToggleTamamlandiOlarakOlusturur() {
        boolean durum = service.gorevToggle(YEREL, 1, 1, 0);

        assertThat(durum).isTrue();
        assertThat(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex(YEREL, 1, 1, 0))
                .hasValueSatisfying(k -> assertThat(k.tamamlandi()).isTrue());
    }

    @Test
    void ikinciToggleGeriAlir_kayitSayisiArtmaz() {
        service.gorevToggle(YEREL, 2, 3, 1);
        boolean durum = service.gorevToggle(YEREL, 2, 3, 1);

        assertThat(durum).isFalse();
        assertThat(ilerlemeRepo.findByKullaniciIdAndHafta(YEREL, 2)).hasSize(1); // upsert — ikinci kayıt açılmadı
    }

    @Test
    void ucuncuToggleTekrarTamamlar() {
        service.gorevToggle(YEREL, 1, 2, 0);
        service.gorevToggle(YEREL, 1, 2, 0);
        boolean durum = service.gorevToggle(YEREL, 1, 2, 0);

        assertThat(durum).isTrue();
    }

    @Test
    void statikQuizSonucuKaydeder() {
        var kayit = service.statikQuizKaydet(YEREL, 4, 80, "{\"dogru\":4}");

        assertThat(kayit.id()).isNotNull();
        assertThat(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(YEREL, 4))
                .singleElement()
                .satisfies(k -> {
                    assertThat(k.puan()).isEqualTo(80);
                    assertThat(k.tur()).isEqualTo("STATIK");
                });
    }
}
