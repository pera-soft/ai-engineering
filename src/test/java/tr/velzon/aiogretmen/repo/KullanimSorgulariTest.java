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
        kullanimRepo.save(AiKullanim.kayit("yerel", "SOHBET", "m", 100, 50, null, null));
        kullanimRepo.save(AiKullanim.kayit("yerel", "SOHBET", "m", 200, 80, null, null));
        kullanimRepo.save(AiKullanim.kayit("yerel", "DEGERLENDIRME", "m", 500, 300, null, null));

        List<Map<String, Object>> toplam = sorgular.islemBazindaToplam("yerel");

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

    /** ADR-0016: süre + cevaptaki model sütunlara eşlenir ve geri okunur (toplam sorgusu değişmez). */
    @Test
    void sureVeYanitModeliKaydedilipOkunur() {
        AiKullanim kayit = kullanimRepo.save(
                AiKullanim.kayit("yerel", "SOHBET", "istenen-m", 1, 2, 1234, "saglayici/yedek-m"));

        assertThat(kullanimRepo.findById(kayit.id())).hasValueSatisfying(k -> {
            assertThat(k.sureMs()).isEqualTo(1234);
            assertThat(k.yanitModel()).isEqualTo("saglayici/yedek-m");
            assertThat(k.model()).isEqualTo("istenen-m");
        });
    }

    @Test
    void bosTablodaBosListeDoner() {
        assertThat(sorgular.islemBazindaToplam("yerel")).isEmpty();
    }

    /** Faz 9b: /api/kullanim yalnız aktif kullanıcının çağrılarını sayar. */
    @Test
    void toplamYalnizVerilenKullanicininSatirlariniSayar() {
        kullanimRepo.save(AiKullanim.kayit("a@pera.software", "SOHBET", "m", 100, 50, null, null));
        kullanimRepo.save(AiKullanim.kayit("a@pera.software", "RAPOR", "m", 10, 5, null, null));
        kullanimRepo.save(AiKullanim.kayit("b@pera.software", "SOHBET", "m", 7, 3, null, null));

        List<Map<String, Object>> b = sorgular.islemBazindaToplam("b@pera.software");
        assertThat(b).hasSize(1);
        assertThat(((Number) b.get(0).get("cagri")).longValue()).isEqualTo(1);
        assertThat(((Number) b.get(0).get("input_tokens")).longValue()).isEqualTo(7);
        assertThat(sorgular.islemBazindaToplam("a@pera.software")).hasSize(2);
        assertThat(sorgular.islemBazindaToplam("yerel")).isEmpty();
    }
}
