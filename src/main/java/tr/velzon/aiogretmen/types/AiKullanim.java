package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/** Tek bir AI çağrısının token kaydı (brief 7.6 maliyet disiplini). */
@Table("AI_KULLANIM")
public record AiKullanim(@Id Long id, String islem, String model,
                         int inputTokens, int outputTokens, Instant tarih) {

    public static AiKullanim kayit(String islem, String model, int inputTokens, int outputTokens) {
        return new AiKullanim(null, islem, model, inputTokens, outputTokens, Instant.now());
    }
}
