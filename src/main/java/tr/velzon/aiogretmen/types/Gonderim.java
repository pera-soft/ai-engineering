package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Kullanıcının değerlendirilmek üzere gönderdiği özet/cevap/iş (brief §6 gonderim).
 * {@code kullaniciId} izolasyon anahtarıdır (Faz 2a-3, ADR-0008).
 */
@Table("GONDERIM")
public record Gonderim(@Id Long id, String kullaniciId, int hafta, Integer gun, String tur,
                       String icerik, String degerlendirmeJson, Integer puan, Instant tarih) {

    public static Gonderim yeni(String kullaniciId, int hafta, Integer gun, String tur, String icerik) {
        return new Gonderim(null, kullaniciId, hafta, gun, tur, icerik, null, null, Instant.now());
    }

    public Gonderim degerlendirildi(String json, Integer puan) {
        return new Gonderim(id, kullaniciId, hafta, gun, tur, icerik, json, puan, tarih);
    }
}
