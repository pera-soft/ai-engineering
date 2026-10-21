package tr.velzon.aiogretmen.config;

/**
 * Aktif kullanıcının izolasyon kimliğini profile göre soyutlar (Faz 2a-3, ADR-0008).
 *
 * <p><strong>İki mod:</strong>
 * <ul>
 *   <li><strong>web / stdio</strong> (varsayılan, {@code !mcp-http}): auth yok, tek kullanıcı →
 *       {@link YerelKullaniciBaglami} sabit {@link #YEREL} döndürür.</li>
 *   <li><strong>mcp-http</strong> (JWT): çok kullanıcı → {@link JwtKullaniciBaglami} doğrulanmış
 *       token'ın email/sub kimliğini döndürür.</li>
 * </ul>
 *
 * <p><strong>Katman/ArchUnit:</strong> {@code config} katmanındadır; repo/service/ai/web/mcp
 * tarafından erişilebilir (katman_kurali). Yalnız Spring Security kullanılır, Spring AI DEĞİL →
 * {@code claude_yalniz_ai_paketinden} YEŞİL kalır.
 */
public interface KullaniciBaglami {

    /** Auth'suz (tek kullanıcı) modun sabit kimliği; şema DEFAULT'u ile aynı (geriye uyumluluk). */
    String YEREL = "yerel";

    /** @return aktif kullanıcının izolasyon kimliği (asla boş/null değildir). */
    String aktifKullanici();
}
