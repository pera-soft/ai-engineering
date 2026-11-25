package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tech-debt #13: Ogretmen ve Atölye controller'larının ortak girdi sınırı — mesajlar eskisiyle birebir. */
class GirdiSiniriTest {

    @Test
    void gecerliDegerAynenDoner() {
        assertThat(GirdiSiniri.sinirla("mesaj", "merhaba")).isEqualTo("merhaba");
        assertThat(GirdiSiniri.sinirla("mesaj", "x".repeat(GirdiSiniri.MAX_GIRDI), GirdiSiniri.BOLEREK_GONDER))
                .hasSize(20_000);
    }

    @Test
    void bosVeNullReddedilir() {
        assertThatThrownBy(() -> GirdiSiniri.sinirla("mesaj", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("mesaj boş olamaz");
        assertThatThrownBy(() -> GirdiSiniri.sinirla("icerik", "   ", GirdiSiniri.BOLEREK_GONDER))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("icerik boş olamaz");
    }

    @Test
    void uzunGirdiMesajiEkleVeEksizAyni() {
        String uzun = "x".repeat(20_001);
        assertThatThrownBy(() -> GirdiSiniri.sinirla("ogrenciPrompt", uzun))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ogrenciPrompt çok uzun (20001 karakter) — üst sınır 20000.");
        assertThatThrownBy(() -> GirdiSiniri.sinirla("mesaj", uzun, GirdiSiniri.BOLEREK_GONDER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("mesaj çok uzun (20001 karakter) — üst sınır 20000. Metni bölerek gönder.");
    }
}
