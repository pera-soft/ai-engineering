package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Tek bir AI çağrısının token kaydı (brief 7.6 maliyet disiplini). Faz 9b: {@code kullaniciId} çağrıyı
 * yapan kullanıcıdır — {@code /api/kullanim} yalnız kendi satırlarını gösterir (ADR-0012).
 *
 * <p>ADR-0016: {@code model} İSTENEN model; {@code yanitModel} sağlayıcının cevapta bildirdiği model
 * (OpenRouter yedeğe geçtiyse farklı olabilir; bildirmediyse null). {@code sureMs} çağrının duvar saati
 * süresidir. İkisi de nullable — sütunlardan önceki eski satırlar boş kalır.
 */
@Table("AI_KULLANIM")
public record AiKullanim(@Id Long id, String kullaniciId, String islem, String model,
                         int inputTokens, int outputTokens, Integer sureMs, String yanitModel, Instant tarih) {

    public static AiKullanim kayit(String kullaniciId, String islem, String model, int inputTokens, int outputTokens,
                                   Integer sureMs, String yanitModel) {
        return new AiKullanim(null, kullaniciId, islem, model, inputTokens, outputTokens, sureMs, yanitModel,
                Instant.now());
    }
}
