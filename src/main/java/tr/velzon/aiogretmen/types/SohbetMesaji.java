package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Sokratik sohbet mesajı (brief 7.3). {@code kullaniciId} izolasyon anahtarıdır (Faz 2a-3, ADR-0008).
 */
@Table("SOHBET_MESAJ")
public record SohbetMesaji(@Id Long id, String kullaniciId, int hafta, String rol, String icerik, Instant tarih) {

    public static SohbetMesaji kullanici(String kullaniciId, int hafta, String icerik) {
        return new SohbetMesaji(null, kullaniciId, hafta, "USER", icerik, Instant.now());
    }

    public static SohbetMesaji asistan(String kullaniciId, int hafta, String icerik) {
        return new SohbetMesaji(null, kullaniciId, hafta, "ASSISTANT", icerik, Instant.now());
    }
}
