package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;
import tr.velzon.aiogretmen.types.NotParcasi;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KitapDosyaFormatiTest {

    @Test
    void sluglamaTurkceKarakterleriCevirir() {
        assertThat(KitapDosyaFormati.slugla("Yazılım Mimarisi: Zor Kısımlar!")).isEqualTo("yazilim-mimarisi-zor-kisimlar");
        assertThat(KitapDosyaFormati.slugla("AI Engineering")).isEqualTo("ai-engineering");
    }

    @Test
    void notBlogParseDeterministik() {
        String md = """
                ---
                kitap: "AI Engineering"
                ---

                ## Not — 2026-07-08
                etiketler: 1,4

                Harness modelin dışındaki her şeydir.

                Anahtar kavramlar: harness, sensor

                ## Not — 2026-07-09
                etiketler: 2

                Fitness function mimariyi korur.
                """;

        List<NotParcasi> parcalar = KitapDosyaFormati.parcalariAyikla(md, "AI Engineering");

        assertThat(parcalar).hasSize(2);
        assertThat(parcalar.get(0).tarih()).isEqualTo("2026-07-08");
        assertThat(parcalar.get(0).kaynak()).isEqualTo("AI Engineering");
        assertThat(parcalar.get(0).metin())
                .contains("Harness modelin dışındaki")
                .contains("Anahtar kavramlar")
                .doesNotContain("etiketler:");       // etiketler satırı gövdeden çıkarıldı
        assertThat(parcalar.get(1).metin()).contains("Fitness function mimariyi korur");
        // idempotent id: aynı içerik → aynı id
        assertThat(parcalar.get(0).id())
                .isEqualTo(KitapDosyaFormati.parcaId("AI Engineering", "2026-07-08", parcalar.get(0).metin()));
    }
}
