package tr.velzon.aiogretmen.service;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.repo.KullaniciRepo;
import tr.velzon.aiogretmen.types.Kullanici;

import java.time.Instant;

/**
 * Spring Security form login'in hesap kaynağı ({@code site} profili, Faz 9a, ADR-0012).
 * Durum eşlemesi (Spring'in ön-kontrolleri şifreden ÖNCE çalışır):
 * <ul>
 *   <li>{@code dogrulandi=false} → {@code enabled=false} → DisabledException ("doğrulanmamış")</li>
 *   <li>{@code etkin=false} → {@code accountNonExpired=false} → AccountExpiredException ("kapalı")</li>
 *   <li>kilit sürüyor → {@code accountNonLocked=false} → LockedException ("kilitli")</li>
 * </ul>
 * Kullanıcı adı = küçük harfli e-posta = izolasyon kimliği ({@code config/OturumKullaniciBaglami}).
 */
@Service
@Profile("site")
public class KullaniciDetayServisi implements UserDetailsService {

    private final KullaniciRepo kullaniciRepo;

    public KullaniciDetayServisi(KullaniciRepo kullaniciRepo) {
        this.kullaniciRepo = kullaniciRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String kullaniciAdi) {
        Kullanici k = kullaniciRepo.findByEmail(KimlikService.normallestir(kullaniciAdi))
                .orElseThrow(() -> new UsernameNotFoundException("Hesap yok"));
        return User.withUsername(k.email())
                .password(k.sifreHash())
                .disabled(!k.dogrulandi())
                .accountExpired(!k.etkin())
                .accountLocked(k.kilitli(Instant.now()))
                .authorities("ROLE_OGRENCI")
                .build();
    }
}
