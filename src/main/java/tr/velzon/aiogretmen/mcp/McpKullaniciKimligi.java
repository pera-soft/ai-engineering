package tr.velzon.aiogretmen.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import tr.velzon.aiogretmen.config.KullaniciBaglami;

import java.util.Optional;

/**
 * Doğrulanmış MCP kullanıcısının kimliğini (email ya da {@code sub}) {@link SecurityContextHolder}'dan
 * güvenle döndürür. {@code mcp-http} profilinde {@link tr.velzon.aiogretmen.config.McpHttpGuvenlikYapilandirma}
 * zinciri /mcp'yi JWT ile koruduğu için, tool handler'ları çalışırken güvenlik context'inde bir
 * {@code JwtAuthenticationToken} bulunur (principal = {@link Jwt}).
 *
 * <p><strong>Mimari kilit (ADR-0007) korunur:</strong> bu sınıf Spring Security'yi
 * ({@code org.springframework.security..}) kullanır ama Spring AI / LLM paketlerini DEĞİL →
 * {@code claude_yalniz_ai_paketinden} kuralı YEŞİL, {@code mcp} paketi LLM'siz kalır.
 *
 * <p><strong>Kapsam (Faz 2a-3):</strong> MCP tool'ları veriyi bu kimlikle kullanıcı-izole kaydeder/okur.
 * {@code mcp-http} (JWT) profilinde kimlik email/sub'tır; {@code mcp} (stdio, auth'suz yerel tek-kullanıcı)
 * profilinde kimlik yoktur → {@link #kullaniciKimligiVeyaYerel()} {@link KullaniciBaglami#YEREL}'e düşer
 * (web arayüzüyle AYNI veri kovası — aynı yerel kullanıcı).
 */
public final class McpKullaniciKimligi {

    private static final Logger log = LoggerFactory.getLogger(McpKullaniciKimligi.class);

    private McpKullaniciKimligi() {
    }

    /**
     * @return doğrulanmış kullanıcının {@code email} claim'i, yoksa {@code sub}; kimlik yoksa/anonimse boş.
     */
    public static Optional<String> kullaniciKimligi() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        String email = jwt.getClaimAsString("email");
        String kimlik = (email != null && !email.isBlank()) ? email : jwt.getSubject();
        if (kimlik == null || kimlik.isBlank()) {
            return Optional.empty();
        }
        log.debug("MCP doğrulanmış kullanıcı kimliği: {}", kimlik);
        return Optional.of(kimlik);
    }

    /**
     * Tool handler'ları için pratik kimlik: doğrulanmış email/sub varsa o, yoksa
     * {@link KullaniciBaglami#YEREL}. {@code mcp-http}'de auth zorunlu olduğundan JWT hep vardır;
     * {@code mcp} (stdio) yerel tek-kullanıcı olduğundan {@code "yerel"} doğru kovadır.
     */
    public static String kullaniciKimligiVeyaYerel() {
        return kullaniciKimligi().orElse(KullaniciBaglami.YEREL);
    }
}
