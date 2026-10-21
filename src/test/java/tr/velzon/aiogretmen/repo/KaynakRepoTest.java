package tr.velzon.aiogretmen.repo;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.types.Kaynak;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KaynakRepoTest {

    KaynakRepo repo = new KaynakRepo(Path.of("src/test/resources/test-content/kaynaklar"));

    @Test
    void kaynagiFrontmatterVeGovdeyleYukler() {
        assertThat(repo.hepsi()).hasSize(1);
        Kaynak k = repo.hepsi().get(0);

        assertThat(k.slug()).isEqualTo("test-kaynagi");
        assertThat(k.baslik()).isEqualTo("Test Kaynağı");
        assertThat(k.yazar()).isEqualTo("Test Yazar");
        assertThat(k.url()).isEqualTo("https://example.com/test");
        assertThat(k.etiketler()).containsExactly(1, 3);
        assertThat(k.govde()).contains("KAYNAK_GOVDE_ISARETI").doesNotContain("baslik:");
    }

    @Test
    void haftaEtiketineGoreFiltreler() {
        assertThat(repo.haftaninKaynaklari(1)).hasSize(1);
        assertThat(repo.haftaninKaynaklari(3)).hasSize(1);
        assertThat(repo.haftaninKaynaklari(2)).isEmpty();
    }

    @Test
    void slugIleBulur_metaGovdesiz() {
        assertThat(repo.bul("test-kaynagi")).isPresent();
        assertThat(repo.bul("yok-boyle-kaynak")).isEmpty();
        assertThat(repo.bul("test-kaynagi").get().meta().govde()).isNull();
    }

    @Test
    void dizinYoksaBosListe() {
        assertThat(new KaynakRepo(Path.of("boyle-dizin-yok")).hepsi()).isEmpty();
    }
}
