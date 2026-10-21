package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * {@code mcp-http} (çok kullanıcı) profilinde aktif kullanıcının kimliğini doğrulanmış JWT'den
 * ({@code email} claim'i, yoksa {@code sub}) döndürür (Faz 2a-3, ADR-0008).
 *
 * <p><strong>Fail-closed:</strong> kimlik yoksa (kimliksiz/anonim istek) sessizce {@link #YEREL}'e
 * DÜŞMEZ — {@link IllegalStateException} fırlatır. Aksi halde bir kullanıcının verisi paylaşılan
 * {@code 'yerel'} kovasına sızardı. {@code mcp-http} güvenlik zinciri {@code /mcp} ve {@code /api}
 * için auth zorunlu kıldığından, bu bean yalnız doğrulanmış istekte çağrılır; istisna gerçekleşmez.
 *
 * <p><strong>Katman/ArchUnit:</strong> {@code mcp} paketindeki {@code McpKullaniciKimligi} ile aynı
 * çıkarımı yapar ama ona bağımlı DEĞİLDİR (config → mcp yasak). Yalnız Spring Security tipleri
 * kullanılır (Spring AI DEĞİL) → izolasyon kuralları YEŞİL.
 */
@Component
@Profile("mcp-http")
public class JwtKullaniciBaglami implements KullaniciBaglami {

    @Override
    public String aktifKullanici() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            String kimlik = (email != null && !email.isBlank()) ? email : jwt.getSubject();
            if (kimlik != null && !kimlik.isBlank()) {
                return kimlik;
            }
        }
        throw new IllegalStateException(
                "mcp-http profilinde doğrulanmış kullanıcı kimliği (email/sub) bulunamadı — "
                        + "istek auth'suz erişmiş olabilir; veri izolasyonu için işlem reddedildi.");
    }
}
