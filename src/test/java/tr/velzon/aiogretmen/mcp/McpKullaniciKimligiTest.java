package tr.velzon.aiogretmen.mcp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link McpKullaniciKimligi} — güvenlik context'inden kullanıcı kimliğini (email/sub) ayıklama.
 * LLM'siz; yalnız Spring Security tipleri kullanılır (ADR-0007 izolasyonu korunur).
 */
class McpKullaniciKimligiTest {

    @AfterEach
    void temizle() {
        SecurityContextHolder.clearContext();
    }

    private static Jwt jwt(String subject, String email) {
        Jwt.Builder b = Jwt.withTokenValue("test-token").header("alg", "none").subject(subject);
        if (email != null) {
            b.claim("email", email);
        }
        return b.build();
    }

    @Test
    void authYokkaBosDoner() {
        assertThat(McpKullaniciKimligi.kullaniciKimligi()).isEmpty();
    }

    @Test
    void emailClaimiVarsaEmailDoner() {
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt("auth0|123", "ogrenci@example.com"), List.of()));

        assertThat(McpKullaniciKimligi.kullaniciKimligi()).contains("ogrenci@example.com");
    }

    @Test
    void emailYoksaSubDoner() {
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt("auth0|123", null), List.of()));

        assertThat(McpKullaniciKimligi.kullaniciKimligi()).contains("auth0|123");
    }
}
