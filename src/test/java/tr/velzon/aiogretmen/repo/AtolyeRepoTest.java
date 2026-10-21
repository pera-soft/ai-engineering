package tr.velzon.aiogretmen.repo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.types.AtolyeAlistirma;
import tr.velzon.aiogretmen.types.AtolyeKategori;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Gerçek atölye içeriğini (content/prompt-atolyesi) parse ettiğini doğrular
 * (ContentRepoTest'in prompts/ gibi gerçek dizini kullanma deseni).
 */
class AtolyeRepoTest {

    static AtolyeRepo repo;

    @BeforeAll
    static void yukle() {
        repo = new AtolyeRepo(Path.of("content/prompt-atolyesi"));
    }

    @Test
    void kategorileriDosyaAdiSirasiylaYukler() {
        List<AtolyeKategori> kategoriler = repo.kategoriler();
        // İçerik büyüdükçe kategori sayısı artar; sabit sayıya bağlanma. Sıra (dosya adı öneki) ve
        // uçlar sabittir: 01-temel-yapi ilk, 07-anti-pattern son.
        assertThat(kategoriler).hasSizeGreaterThanOrEqualTo(2);
        assertThat(kategoriler.get(0).slug()).isEqualTo("temel-yapi");
        assertThat(kategoriler.get(0).baslik()).isEqualTo("Temel Yapı");
        assertThat(kategoriler.get(0).aciklama()).isNotBlank();
        assertThat(kategoriler.get(kategoriler.size() - 1).slug()).isEqualTo("anti-pattern");
        // her kategori en az bir alıştırma içermeli
        assertThat(kategoriler).allSatisfy(k -> assertThat(k.alistirmalar()).isNotEmpty());
    }

    @Test
    void temelYapiYediAlistirmaIcerir() {
        AtolyeKategori temel = repo.kategoriler().get(0);
        assertThat(temel.alistirmalar()).hasSize(7);
        assertThat(temel.alistirmalar())
                .extracting(AtolyeAlistirma::id)
                .containsExactly("net-gorev", "baglam", "rol", "format", "negatif", "few-shot", "birlestir");
    }

    @Test
    void bolumleriKodFenceDahilTamParseEder() {
        AtolyeAlistirma netGorev = alistirma("net-gorev");
        assertThat(netGorev.teknik()).isEqualTo("Net görev verme");
        assertThat(netGorev.aiDegerlendir()).isFalse();
        assertThat(netGorev.gorev()).contains("kime", "ne uzunlukta");
        assertThat(netGorev.kotuOrnek()).contains("şunu açıkla");
        assertThat(netGorev.ipucu()).isNotBlank();
        // İdeal çözüm çok satırlı + ```java kod fence'i içerir — fence içeriği korunur
        assertThat(netGorev.idealCozum()).contains("```java", "[metodu buraya yapıştır]");
        assertThat(netGorev.neden()).isNotBlank();
    }

    @Test
    void birlestirAlistirmasiAiDegerlendirir() {
        AtolyeAlistirma birlestir = alistirma("birlestir");
        assertThat(birlestir.aiDegerlendir()).isTrue();
    }

    @Test
    void kotuOrnekOlmayanAlistirmadaBosString() {
        // "birlestir" alıştırmasında "## Kötü örnek" bölümü YOK → boş string
        AtolyeAlistirma birlestir = alistirma("birlestir");
        assertThat(birlestir.kotuOrnek()).isEmpty();
        // ama diğer bölümleri dolu olmalı
        assertThat(birlestir.gorev()).isNotBlank();
        assertThat(birlestir.idealCozum()).isNotBlank();
    }

    @Test
    void antiPatternKategorisindeAiDegerlendirenAlistirmaVar() {
        AtolyeKategori anti = repo.kategoriler().get(1);
        assertThat(anti.alistirmalar()).isNotEmpty();
        assertThat(anti.alistirmalar()).anyMatch(AtolyeAlistirma::aiDegerlendir);
    }

    @Test
    void olmayanDizinNetHataVerir() {
        assertThatThrownBy(() -> new AtolyeRepo(Path.of("boyle-bir-dizin-yok")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("atölye");
    }

    private AtolyeAlistirma alistirma(String id) {
        return repo.kategoriler().stream()
                .flatMap(k -> k.alistirmalar().stream())
                .filter(a -> a.id().equals(id))
                .findFirst().orElseThrow();
    }
}
