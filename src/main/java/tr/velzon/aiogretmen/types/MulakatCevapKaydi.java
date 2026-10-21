package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Bir kullanıcının bir mülakat sorusuna verdiği cevabın kaydı. {@code kullaniciId} izolasyon
 * anahtarıdır (Faz 2a-3, ADR-0008). {@code dogru} SUNUCUDA hesaplanır — istemci iddiası değil.
 */
@Table("MULAKAT_CEVAP")
public record MulakatCevapKaydi(@Id Long id, String kullaniciId, String soruId, String alanSlug,
                                String konuSlug, int secilenIndex, boolean dogru, Instant tarih) {

    public static MulakatCevapKaydi yeni(String kullaniciId, String soruId, String alanSlug,
                                         String konuSlug, int secilenIndex, boolean dogru) {
        return new MulakatCevapKaydi(null, kullaniciId, soruId, alanSlug, konuSlug,
                secilenIndex, dogru, Instant.now());
    }
}
