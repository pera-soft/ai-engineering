package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.config.KullaniciBaglami;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Faz 9b: kullanıcı başına dosya dizini — yerel eski köke, diğerleri güvenli özet dizinine. */
class KisiselDizinTest {

    Path kok = Path.of("data/hafiza");

    @Test
    void yerelKullaniciEskiKokuKullanir() {
        assertThat(KisiselDizin.coz(kok, KullaniciBaglami.YEREL)).isEqualTo(kok);
    }

    @Test
    void epostaKullanicisiKullanicilarAltindaOzetDizinineGider() {
        Path d = KisiselDizin.coz(kok, "ali@pera.software");
        assertThat(d.getParent()).isEqualTo(kok.resolve("kullanicilar"));
        assertThat(d.getFileName().toString()).matches("[0-9a-f]{64}");
        // e-posta dosya sistemine düz yazılmaz (KVKK minimizasyonu + yol güvenliği)
        assertThat(d.toString()).doesNotContain("ali").doesNotContain("@");
    }

    @Test
    void ayniKimlikHepAyniDizinFarkliKimlikFarkliDizin() {
        assertThat(KisiselDizin.coz(kok, "ali@pera.software"))
                .isEqualTo(KisiselDizin.coz(kok, "ali@pera.software"))
                .isNotEqualTo(KisiselDizin.coz(kok, "veli@pera.software"));
    }

    @Test
    void yolGezintisiDenemesiKokunDisinaCikamaz() {
        for (String kotu : new String[]{"../../etc", "..", "/etc/passwd", "a/../../b", "kitaplar", "yerel/.."}) {
            Path d = KisiselDizin.coz(kok, kotu).normalize();
            assertThat(d.startsWith(kok.resolve("kullanicilar"))).as(kotu).isTrue();
            assertThat(d.getNameCount()).isEqualTo(kok.getNameCount() + 2);
        }
    }

    @Test
    void bosKimlikReddedilir() {
        assertThatThrownBy(() -> KisiselDizin.coz(kok, " ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KisiselDizin.coz(kok, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
