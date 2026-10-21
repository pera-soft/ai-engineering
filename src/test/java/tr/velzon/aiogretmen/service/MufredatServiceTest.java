package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;
import tr.velzon.aiogretmen.types.HaftaDetay;
import tr.velzon.aiogretmen.types.HaftaOzeti;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MufredatServiceTest {

    ContentRepo contentRepo = new ContentRepo(Path.of("src/test/resources/test-content"));
    IlerlemeRepo ilerlemeRepo = mock(IlerlemeRepo.class);
    QuizSonucRepo quizSonucRepo = mock(QuizSonucRepo.class);
    MufredatService service = new MufredatService(contentRepo, ilerlemeRepo, quizSonucRepo);

    static final String YEREL = "yerel";

    @BeforeEach
    void varsayilanlar() {
        when(ilerlemeRepo.findByKullaniciIdAndHafta(anyString(), anyInt())).thenReturn(List.of());
        when(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(anyString(), anyInt())).thenReturn(List.of());
    }

    @Test
    void programOzetiIlerlemeYuzdesiHesaplar() {
        // fixture: hafta 1'de toplam 3 görev (gun-1: 2, gun-2: 1); 1'i tamamlanmış
        when(ilerlemeRepo.findByKullaniciIdAndHafta(YEREL, 1)).thenReturn(List.of(
                new GorevIlerlemeKaydi(1L, YEREL, 1, 1, 0, true, Instant.now())));

        List<HaftaOzeti> ozet = service.programOzeti(YEREL);

        assertThat(ozet).hasSize(1);
        HaftaOzeti h1 = ozet.get(0);
        assertThat(h1.toplamGorev()).isEqualTo(3);
        assertThat(h1.tamamlananGorev()).isEqualTo(1);
        assertThat(h1.ilerlemeYuzde()).isEqualTo(33);
    }

    @Test
    void haftaDetayDersiHtmlOlarakDondurur() {
        HaftaDetay detay = service.haftaDetay(YEREL, 1);

        assertThat(detay.gunler()).hasSize(2);
        assertThat(detay.gunler().get(0).dersHtml()).contains("<p>").contains("Bu ders içeriğidir.");
        assertThat(detay.gunler().get(0).gorevler()).hasSize(2);
        assertThat(detay.quiz()).hasSize(2);
    }

    @Test
    void haftaDetayGorevDurumlariniIsler() {
        when(ilerlemeRepo.findByKullaniciIdAndHafta(YEREL, 1)).thenReturn(List.of(
                new GorevIlerlemeKaydi(1L, YEREL, 1, 1, 1, true, Instant.now())));

        HaftaDetay detay = service.haftaDetay(YEREL, 1);

        assertThat(detay.gunler().get(0).gorevler().get(0).tamamlandi()).isFalse();
        assertThat(detay.gunler().get(0).gorevler().get(1).tamamlandi()).isTrue();
    }

    @Test
    void sonQuizPuaniOzeteYansir() {
        when(quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(YEREL, 1)).thenReturn(List.of(
                new QuizSonucKaydi(1L, YEREL, 1, "STATIK", 80, null, Instant.now())));

        assertThat(service.programOzeti(YEREL).get(0).sonQuizPuan()).isEqualTo(80);
    }

    @Test
    void olmayanHaftaBulunamadiFirlatir() {
        assertThatThrownBy(() -> service.haftaDetay(YEREL, 42))
                .isInstanceOf(BulunamadiException.class);
    }
}
