package tr.velzon.aiogretmen.config;

import java.util.Optional;

/**
 * Bu istekte kullanılacak ziyaretçi AI anahtarı (Faz 10, ADR-0017). Sitede istekteki
 * {@code X-OpenRouter-Anahtar} başlığı ({@link BaslikAiAnahtarBaglami}); yerelde hep boş
 * ({@link YokAiAnahtarBaglami} — yerel mod sunucu anahtarıyla çalışır).
 *
 * <p>Anahtar hiçbir yerde saklanmaz: her çağrıda istekten okunur, istek bitince kaybolur.
 */
public interface AiAnahtarBaglami {

    /**
     * @return kırpılmış anahtar; başlık yok/boşsa ya da istek dışındaysa boş
     * @throws IllegalArgumentException başlık var ama biçimi geçersizse (mesaj anahtarı İÇERMEZ)
     */
    Optional<String> anahtar();
}
