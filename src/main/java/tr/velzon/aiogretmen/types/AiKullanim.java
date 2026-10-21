package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Tek bir AI çağrısının token kaydı (brief 7.6 maliyet disiplini). Faz 9b: {@code kullaniciId} çağrıyı
 * yapan kullanıcıdır — {@code /api/kullanim} yalnız kendi satırlarını gösterir (ADR-0012).
 */
@Table("AI_KULLANIM")
public record AiKullanim(@Id Long id, String kullaniciId, String islem, String model,
                         int inputTokens, int outputTokens, Instant tarih) {

    public static AiKullanim kayit(String kullaniciId, String islem, String model, int inputTokens, int outputTokens) {
        return new AiKullanim(null, kullaniciId, islem, model, inputTokens, outputTokens, Instant.now());
    }
}
