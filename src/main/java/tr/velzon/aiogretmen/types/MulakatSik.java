package tr.velzon.aiogretmen.types;

/**
 * Bir çoktan seçmeli sorunun tek şıkkı: görünen {@code metin} ve o şıkka özel kısa
 * AI notu {@code aiNot} (içerikteki 🤖 satırı). {@code aiNot} istemciye sunulabilir —
 * doğru cevabı ele vermez, yalnız şık hakkında ipucu/uyarıdır.
 */
public record MulakatSik(String metin, String aiNot) {
}
