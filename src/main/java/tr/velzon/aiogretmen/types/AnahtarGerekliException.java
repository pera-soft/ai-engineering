package tr.velzon.aiogretmen.types;

/**
 * Sitede sohbet ziyaretçinin kendi OpenRouter anahtarını ({@code X-OpenRouter-Anahtar}) ister; başlık yoksa
 * fırlatılır → HTTP 400 (Faz 10, ADR-0017). Sunucu anahtarına düşülmez; mesaj sabittir, anahtar içermez.
 */
public class AnahtarGerekliException extends RuntimeException {

    public AnahtarGerekliException() {
        super("AI öğretmenle sohbet için kendi OpenRouter anahtarını eklemelisin.");
    }
}
