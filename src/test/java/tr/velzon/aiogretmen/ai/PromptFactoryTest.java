package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptFactoryTest {

    final PromptFactory factory = new PromptFactory(Path.of("prompts"));

    @Test
    void sablonuYuklerVeSurumYorumlariniAtar() {
        String s = factory.doldur("sokratik-sohbet", Map.of());

        assertThat(s).doesNotContain("# v1");         // "# " ile başlayan sürüm satırı atılır
        assertThat(s).contains("## Sohbet davranışın"); // markdown "## " başlıkları korunur
        assertThat(s).contains("EN FAZLA 1 soru");
    }

    @Test
    void yerTutuculariDoldurur() {
        String s = factory.doldur("degerlendirme", Map.of(
                "tur", "OZET",
                "icerik", "Benim özetim burada."));

        assertThat(s).contains("(tür: OZET)");
        assertThat(s).contains("Benim özetim burada.");
        assertThat(s).doesNotContain("{tur}").doesNotContain("{icerik}");
    }

    @Test
    void jsonSemasindakiSuslularaDokunmaz() {
        String s = factory.doldur("degerlendirme", Map.of("tur", "OZET", "icerik", "x"));

        // şablondaki JSON şema örneği bozulmamalı
        assertThat(s).contains("\"puan\": 0-100 arası tamsayı");
        assertThat(s).contains("\"yanlis_anlamalar\"");
    }

    @Test
    void olmayanSablonNetHataVerir() {
        assertThatThrownBy(() -> factory.doldur("boyle-sablon-yok", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("boyle-sablon-yok");
    }
}
