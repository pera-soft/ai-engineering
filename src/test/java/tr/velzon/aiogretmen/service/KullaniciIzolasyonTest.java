package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.MulakatCevapKaydi;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 2a-3 (ADR-0008) TDD güvenceleri: gerçek H2 + schema.sql üzerinde
 * <ol>
 *   <li><strong>İzolasyon:</strong> kullanıcı A'nın işaretlediği görev kullanıcı B'nin sorgusunda GÖRÜNMEZ.</li>
 *   <li><strong>Geriye uyumluluk:</strong> {@code kullanici_id} verilmeden (eski davranış) eklenen satır
 *       şema DEFAULT'u ile {@code 'yerel'} olur ve web ({@code 'yerel'}) sorgusunda görünür.</li>
 * </ol>
 */
@DataJdbcTest
@Import(IlerlemeService.class)
class KullaniciIzolasyonTest {

    @Autowired
    IlerlemeService service;

    @Autowired
    IlerlemeRepo ilerlemeRepo;

    @Autowired
    QuizSonucRepo quizSonucRepo;

    @Autowired
    MulakatCevapRepo mulakatCevapRepo;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void kullaniciAninGoreviBdeGorunmez() {
        service.gorevDurumuAyarla("ali@example.com", 1, 1, 0, true);
        service.gorevDurumuAyarla("veli@example.com", 1, 1, 0, true);
        service.gorevDurumuAyarla("veli@example.com", 1, 1, 1, true); // veli fazladan bir görev daha

        assertThat(ilerlemeRepo.findByKullaniciIdAndHafta("ali@example.com", 1)).hasSize(1);
        assertThat(ilerlemeRepo.findByKullaniciIdAndHafta("veli@example.com", 1)).hasSize(2);

        // Ali'nin (1,1,0) kaydı Veli'nin sorgusundan ayrı satırdır — aynı (hafta,gun,gorev) çakışmaz
        assertThat(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("ali@example.com", 1, 1, 0)).isPresent();
        assertThat(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("veli@example.com", 1, 1, 0)).isPresent();

        // Ali kendi görevini geri alır; Veli etkilenmez (izolasyon)
        boolean aliDurum = service.gorevDurumuAyarla("ali@example.com", 1, 1, 0, false);
        assertThat(aliDurum).isFalse();
        assertThat(ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex("veli@example.com", 1, 1, 0))
                .hasValueSatisfying(k -> assertThat(k.tamamlandi()).isTrue());
    }

    @Test
    void quizSonucuDaKullaniciIzole() {
        service.statikQuizKaydet("ali@example.com", 2, 90, null);
        service.statikQuizKaydet("veli@example.com", 2, 40, null);

        assertThat(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc("ali@example.com", 2))
                .singleElement().satisfies(k -> assertThat(k.puan()).isEqualTo(90));
        assertThat(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc("veli@example.com", 2))
                .singleElement().satisfies(k -> assertThat(k.puan()).isEqualTo(40));
    }

    @Test
    void mulakatCevabiDaKullaniciIzole() {
        mulakatCevapRepo.save(MulakatCevapKaydi.yeni("ali@example.com", "eq-1",
                "cekirdek-java", "esitlik-vs-equals", 1, true));
        mulakatCevapRepo.save(MulakatCevapKaydi.yeni("veli@example.com", "eq-2",
                "cekirdek-java", "esitlik-vs-equals", 0, false));

        assertThat(mulakatCevapRepo.findByKullaniciIdOrderByTarihDescIdDesc("ali@example.com"))
                .singleElement().satisfies(k -> {
                    assertThat(k.soruId()).isEqualTo("eq-1");
                    assertThat(k.dogru()).isTrue();
                });
        assertThat(mulakatCevapRepo.findByKullaniciIdOrderByTarihDescIdDesc("veli@example.com"))
                .singleElement().satisfies(k -> assertThat(k.dogru()).isFalse());

        // Silme izole: ali silinince veli etkilenmez
        mulakatCevapRepo.deleteByKullaniciId("ali@example.com");
        assertThat(mulakatCevapRepo.findByKullaniciIdOrderByTarihDescIdDesc("ali@example.com")).isEmpty();
        assertThat(mulakatCevapRepo.findByKullaniciIdOrderByTarihDescIdDesc("veli@example.com")).hasSize(1);
    }

    @Test
    void kullaniciIdsizEklenenEskiSatirYerelOlurVeGorunur() {
        // Eski (Faz 2a-3 ÖNCESİ) davranışı simüle et: kullanici_id KOLONU verilmeden INSERT.
        // Şema DEFAULT 'yerel' devreye girmeli → veri kaybı yok, web ('yerel') sorgusunda görünür.
        jdbc.update("INSERT INTO gorev_ilerleme (hafta, gun, gorev_index, tamamlandi) VALUES (?, ?, ?, TRUE)",
                5, 1, 0);

        var yerelSatirlar = ilerlemeRepo.findByKullaniciIdAndHafta("yerel", 5);
        assertThat(yerelSatirlar).hasSize(1);
        assertThat(yerelSatirlar.get(0).kullaniciId()).isEqualTo("yerel");
        assertThat(yerelSatirlar.get(0).tamamlandi()).isTrue();

        // Başka bir kullanıcı bu eski 'yerel' satırı GÖRMEZ (izolasyon)
        assertThat(ilerlemeRepo.findByKullaniciIdAndHafta("baskasi@example.com", 5)).isEmpty();
    }
}
