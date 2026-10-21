package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tr.velzon.aiogretmen.repo.KaynakRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaynakServiceTest {

    KaynakService servis = new KaynakService(
            new KaynakRepo(Path.of("src/test/resources/test-content/kaynaklar")));

    @Test
    void okuHtmlVeMetaDondurur() {
        Map<String, Object> k = servis.oku("test-kaynagi");

        assertThat(k.get("baslik")).isEqualTo("Test Kaynağı");
        assertThat((String) k.get("html")).contains("<h2>").contains("KAYNAK_GOVDE_ISARETI");
    }

    @Test
    void semaSizYalnizTamSvgCitiniAyiklarJavaBlogunuKorur() {
        // kapanmayan svg çiti sonraki ```java'yı yutmamalı; svg-dışı blok korunmalı
        String md = "```svg\n<svg/>\n```\nara metin\n```java\nkod satiri\n```\nson";
        String cikti = KaynakService.semaSiz(md);
        assertThat(cikti)
                .doesNotContain("<svg")           // svg bloğu ayıklandı
                .contains("ara metin")            // aradaki metin korundu
                .contains("kod satiri")           // ```java bloğu SİLİNMEDİ
                .contains("son");
    }

    @Test
    void semaSizSvgBlogunuAyiklarMetniKorur() {
        // AI bağlamı: ```svg markup gürültüsü çıkmalı, çevresindeki öğretim metni kalmalı
        String md = "## Factory\nÖnce açıklama.\n```svg\n<svg viewBox=\"0 0 10 10\"><rect/></svg>\n```\nSonra devam.";
        String cikti = KaynakService.semaSiz(md);
        assertThat(cikti)
                .contains("Önce açıklama.")
                .contains("Sonra devam.")
                .doesNotContain("<svg")
                .doesNotContain("viewBox")
                .contains("(diyagram");
    }

    @Test
    void svgFenceCanliInlineSvgeAcilir() {
        // commonmark ```svg → <pre><code class="language-svg">escaped</code></pre>
        String html = "<pre><code class=\"language-svg\">"
                + "&lt;svg viewBox=&quot;0 0 10 10&quot;&gt;&lt;rect fill=&quot;currentColor&quot;/&gt;&lt;/svg&gt;"
                + "</code></pre>";
        String cikti = KaynakService.semalariAc(html);
        assertThat(cikti)
                .contains("<figure class=\"oku-sema\">")
                .contains("<svg viewBox=\"0 0 10 10\"><rect fill=\"currentColor\"/></svg>")
                .doesNotContain("language-svg");
    }

    @Test
    void svgDisiKodBloguACILMAZ_escapedKalir() {
        // least-privilege: yalnız language-svg açılır; diğer bloklar escaped kalır
        String html = "<pre><code class=\"language-java\">&lt;script&gt;kotu&lt;/script&gt;</code></pre>";
        assertThat(KaynakService.semalariAc(html)).isEqualTo(html);
    }

    @Test
    void olmayanSlug404Firlatir() {
        assertThatThrownBy(() -> servis.oku("boyle-kaynak-yok"))
                .isInstanceOf(BulunamadiException.class);
    }

    @Test
    void listeGovdesizMetaDoner() {
        assertThat(servis.liste()).singleElement()
                .satisfies(k -> assertThat(k.govde()).isNull());
    }

    @Test
    void baglamIcinPerKaynakOzVeSvgAyiklar(@TempDir Path tempDir) throws Exception {
        // tek büyük kaynak: SVG markup AI bağlamına GİRMEMELİ, gövde per-kaynak öze kırpılmalı
        String dev = "---\nbaslik: \"Dev Kaynak\"\netiketler: 2\n---\n"
                + "```svg\n<svg viewBox=\"0 0 9 9\"><rect/></svg>\n```\n" + "x".repeat(9000);
        Files.writeString(tempDir.resolve("dev-kaynak.md"), dev);
        KaynakService buyuk = new KaynakService(new KaynakRepo(tempDir));

        assertThat(buyuk.baglamIcin(5)).contains("eşleşen kaynak metni yok"); // eşleşme yok
        String baglam = buyuk.baglamIcin(2);
        assertThat(baglam)
                .doesNotContain("<svg").doesNotContain("viewBox") // SVG ayıklandı (gürültü yok)
                .contains("devamı");                              // per-kaynak öz kırpması (BaglamKirpici)
        assertThat(baglam.length()).isLessThan(3500);            // ~3000 öz + başlık + not
    }

    @Test
    void baglamIcinToplamSinirCokKaynagiKirpar(@TempDir Path tempDir) throws Exception {
        // 6 kaynak × ~3000 öz → toplam 14000 sınırını aşar → genel kırpma notu
        for (int i = 1; i <= 6; i++) {
            Files.writeString(tempDir.resolve("k" + i + ".md"),
                    "---\nbaslik: \"K" + i + "\"\netiketler: 2\n---\n" + "y".repeat(4000));
        }
        KaynakService cok = new KaynakService(new KaynakRepo(tempDir));
        String baglam = cok.baglamIcin(2);
        assertThat(baglam.length()).isLessThan(14100);
        assertThat(baglam).contains("bağlam sınıra kırpıldı");
    }
}
