package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * {@code site} profilinde aktif kullanıcının kimliği: oturumdaki (form login) doğrulanmış
 * kullanıcının küçük harfli e-postası (Faz 9a, ADR-0012). ADR-0008'deki JWT modu gibi e-posta
 * kimliktir → ilerleme tablolarının {@code kullanici_id}'si doğrudan e-postadır.
 *
 * <p><strong>Fail-closed (ADR-0008):</strong> doğrulanmış kullanıcı yoksa ASLA {@link #YEREL}'e
 * düşmez — {@link IllegalStateException}. Güvenlik zinciri {@code /api/**}'yi zaten kimlik ister
 * yaptığından bu yol normalde tetiklenmez; tetiklenirse sızıntı yerine hata.
 */
@Component
@Profile("site")
public class OturumKullaniciBaglami implements KullaniciBaglami {

    @Override
    public String aktifKullanici() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String kimlik = auth.getName();
            if (kimlik != null && !kimlik.isBlank()) {
                return kimlik;
            }
        }
        throw new IllegalStateException(
                "site profilinde oturum açmış kullanıcı bulunamadı — veri izolasyonu için işlem reddedildi.");
    }
}
