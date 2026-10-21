package tr.velzon.aiogretmen.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import tr.velzon.aiogretmen.types.AiKullanim;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Regresyon: tablo DOLUYKEN toplam sorgusu çalışmalı (canlıda yakalanan 500 hatası). */
@DataJdbcTest
@Import(KullanimSorgulari.class)
class KullanimSorgulariTest {

    @Autowired
    KullanimRepo kullanimRepo;

    @Autowired
    KullanimSorgulari sorgular;

    @Test
    void doluTablodaIslemBazindaToplamDoner() {
        kullanimRepo.save(AiKullanim.kayit("SOHBET", "m", 100, 50));
        kullanimRepo.save(AiKullanim.kayit("SOHBET", "m", 200, 80));
        kullanimRepo.save(AiKullanim.kayit("DEGERLENDIRME", "m", 500, 300));

        List<Map<String, Object>> toplam = sorgular.islemBazindaToplam();

        assertThat(toplam).hasSize(2);
        // JSON sözleşmesi: anahtarlar küçük harf serileşmeli (get() case-insensitive olduğundan
        // yalnız get ile test etmek yetmez — gerçek anahtar kümesi doğrulanır)
        assertThat(toplam.get(0).keySet())
                .containsExactlyInAnyOrder("islem", "cagri", "input_tokens", "output_tokens");
        Map<String, Object> sohbet = toplam.stream()
                .filter(s -> "SOHBET".equals(s.get("islem") instanceof String x ? x : String.valueOf(s.get("islem"))))
                .findFirst().orElseThrow();
        assertThat(((Number) sohbet.get("cagri")).longValue()).isEqualTo(2);
        assertThat(((Number) sohbet.get("input_tokens")).longValue()).isEqualTo(300);
        assertThat(((Number) sohbet.get("output_tokens")).longValue()).isEqualTo(130);
    }

    @Test
    void bosTablodaBosListeDoner() {
        assertThat(sorgular.islemBazindaToplam()).isEmpty();
    }
}
