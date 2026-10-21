package tr.velzon.aiogretmen.repo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.types.Gun;
import tr.velzon.aiogretmen.types.Hafta;
import tr.velzon.aiogretmen.types.QuizSorusu;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentRepoTest {

    static ContentRepo repo;

    @BeforeAll
    static void yukle() {
        repo = new ContentRepo(Path.of("src/test/resources/test-content"));
    }

    @Test
    void haftalariYukler() {
        List<Hafta> haftalar = repo.haftalar();
        assertThat(haftalar).hasSize(1);

        Hafta h = haftalar.get(0);
        assertThat(h.no()).isEqualTo(1);
        assertThat(h.baslik()).isEqualTo("Test Haftası \"tırnaklı\"");
        assertThat(h.hedef()).isEqualTo("Parser'ı doğrulamak.");
        assertThat(h.kaynak()).isEqualTo("Kaynak: birim test");
    }

    @Test
    void gunleriSiraliYukler() {
        Hafta h = repo.hafta(1).orElseThrow();
        assertThat(h.gunler()).hasSize(2);

        Gun g1 = h.gunler().get(0);
        assertThat(g1.hafta()).isEqualTo(1);
        assertThat(g1.gun()).isEqualTo(1);
        assertThat(g1.baslik()).isEqualTo("İlk gün dersi");
        assertThat(g1.ders()).isEqualTo("Bu ders içeriğidir.\nİkinci satır da var.");
        assertThat(g1.gorevler()).containsExactly("Birinci görev", "İkinci görev");

        assertThat(h.gunler().get(1).gun()).isEqualTo(2);
        assertThat(h.gunler().get(1).gorevler()).containsExactly("Tek görev");
    }

    @Test
    void quizSorulariniDogruIndexleParseEder() {
        Hafta h = repo.hafta(1).orElseThrow();
        assertThat(h.quiz()).hasSize(2);

        QuizSorusu s1 = h.quiz().get(0);
        assertThat(s1.soru()).isEqualTo("İki kere iki kaç eder?");
        assertThat(s1.secenekler()).containsExactly("Üç", "Dört", "Beş", "Altı");
        assertThat(s1.dogruIndex()).isEqualTo(1);
        assertThat(s1.aciklama()).isEqualTo("Temel aritmetik.");

        assertThat(h.quiz().get(1).dogruIndex()).isEqualTo(0);
    }

    @Test
    void olmayanHaftaBosDoner() {
        assertThat(repo.hafta(99)).isEmpty();
    }

    @Test
    void olmayanDizinNetHataVerir() {
        assertThatThrownBy(() -> new ContentRepo(Path.of("boyle-bir-dizin-yok")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("content");
    }
}
