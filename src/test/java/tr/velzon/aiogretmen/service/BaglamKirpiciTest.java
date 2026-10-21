package tr.velzon.aiogretmen.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaglamKirpiciTest {

    @Test
    void bosListeBosMesajDoner() {
        assertThat(BaglamKirpici.birlestir(List.of(), 100, 1000, "yok")).isEqualTo("yok");
    }

    @Test
    void perParcaSinirBlokSinirindaKeser() {
        // 3 satırlık parça; sınır 2. satırın ortasına denk gelir → son satır sonunda kesilmeli
        String parca = "birinci satir\nikinci satir\nucuncu satir";
        String c = BaglamKirpici.birlestir(List.of(parca), 20, 1000, "yok");
        assertThat(c).contains("birinci satir")
                .doesNotContain("ucuncu satir")   // per-parça sınır kesti
                .contains("devamı");
        // blok-sınırında kesim: "ikinci" ortadan bölünmemeli (satır sonuna kadar geri gitti)
        assertThat(c).doesNotContain("ikinci sat\n");
    }

    @Test
    void toplamSinirCokParcayiKirpar() {
        String p = "x".repeat(400);
        String c = BaglamKirpici.birlestir(List.of(p, p, p, p, p), 0, 1000, "yok");
        assertThat(c.length()).isLessThan(1100);
        assertThat(c).contains("bağlam sınıra kırpıldı");
    }

    @Test
    void sinirAltiParcaAynenKalir() {
        String c = BaglamKirpici.birlestir(List.of("kısa"), 100, 1000, "yok");
        assertThat(c).contains("kısa").doesNotContain("devamı").doesNotContain("kırpıldı");
    }
}
