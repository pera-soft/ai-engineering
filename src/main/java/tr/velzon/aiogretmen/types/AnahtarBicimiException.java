package tr.velzon.aiogretmen.types;

/**
 * Sitede {@code X-OpenRouter-Anahtar} başlığı biçimsiz (Faz 10, ADR-0017) → HTTP 400, {@code neden=ANAHTAR_BICIMI}.
 * {@link IllegalArgumentException} alt türü: genel "Geçersiz istek" sözleşmesine uyar. Mesaj anahtarı İÇERMEZ.
 */
public class AnahtarBicimiException extends IllegalArgumentException {

    public AnahtarBicimiException(String mesaj) {
        super(mesaj);
    }
}
