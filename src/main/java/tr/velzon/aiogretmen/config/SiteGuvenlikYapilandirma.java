package tr.velzon.aiogretmen.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Girişsiz {@code site} profili güvenlik zinciri (Faz 10, ADR-0017; ADR-0012'nin giriş kısmını değiştirir).
 *
 * <ul>
 *   <li><strong>Giriş YOK:</strong> tüm sayfalar ve {@code /api/**} herkese açık; yönlendirme, form login, çıkış,
 *       HTTP Basic yok.</li>
 *   <li><strong>Ziyaretçi kimliği:</strong> {@link ZiyaretciCerezi} her isteğe {@code aio_ziyaretci} çerezinden (yoksa
 *       yeni üretilmiş) kimliği koyar → {@link ZiyaretciKullaniciBaglami} {@code "z-" + kimlik} döndürür.</li>
 *   <li><strong>Oturum YOK:</strong> {@code STATELESS} → {@code JSESSIONID} oluşturulmaz.</li>
 *   <li><strong>CSRF token AÇIK</strong> (kimlik çerezle taşındığı için durum değiştiren uçlar hâlâ CSRF'e açık):
 *       {@link CookieCsrfTokenRepository} → {@code XSRF-TOKEN} çerezi (JS okur, HttpOnly DEĞİL) ↔
 *       {@code X-XSRF-TOKEN} başlığı / {@code _csrf} form alanı. Düz (XOR'suz) işleyici: statik ön yüz çerezdeki
 *       değeri aynen geri yollar. {@link CsrfCerezFiltresi} her yanıtta token'ı yükler ki çerez ilk sayfa
 *       açılışında yazılsın. {@code web/CsrfKoruma} (Origin/Sec-Fetch-Site) ek katman olarak çalışmaya devam eder.</li>
 * </ul>
 *
 * <p><strong>Ortak modüller (Faz 9b):</strong> {@code site} profili MCP sunucusuyla (ADR-0007) birlikte
 * açılamaz — açılışta hata (ortak araçlar çok kullanıcılı siteye göre değil).
 * Mülakat {@code MulakatEtkin} üzerinde {@code @Profile("!site")} ile zorla kapalı.
 */
@Configuration
@Profile("site")
public class SiteGuvenlikYapilandirma {

    /** Açılış denetimi: site + MCP birlikte ÇALIŞMAZ (ortam değişkeniyle yanlışlıkla açılmasın). */
    public SiteGuvenlikYapilandirma(Environment ortam) {
        boolean mcpAcik = ortam.getProperty("spring.ai.mcp.server.enabled", Boolean.class, false)
                || ortam.matchesProfiles("mcp", "mcp-http");
        if (mcpAcik) {
            throw new IllegalStateException("site profili MCP sunucusuyla birlikte açılamaz "
                    + "(spring.ai.mcp.server.enabled / mcp / mcp-http kapalı olmalı) — ADR-0012");
        }
    }

    @Bean
    SecurityFilterChain siteGuvenlikZinciri(HttpSecurity http, SiteAyarlari ayarlar) throws Exception {
        CookieCsrfTokenRepository csrfDeposu = CookieCsrfTokenRepository.withHttpOnlyFalse();
        // guvenli-cerez=true → her zaman Secure; false (yalnız yerel http denemesi) → isteğe göre: nginx
        // arkasında X-Forwarded-Proto=https gelirse yine Secure (ayar unutulsa da canlıda düz çerez olmaz).
        csrfDeposu.setCookieCustomizer(c -> {
            if (ayarlar.guvenliCerez()) {
                c.secure(true);
            }
            c.sameSite("Lax");
        });

        http
                .authorizeHttpRequests(yetki -> yetki.anyRequest().permitAll())
                .sessionManagement(oturum -> oturum.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfDeposu)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterBefore(new ZiyaretciCerezi(ayarlar.guvenliCerez()), CsrfFilter.class)
                .addFilterAfter(new CsrfCerezFiltresi(), CsrfFilter.class)
                .formLogin(form -> form.disable())
                .logout(cikis -> cikis.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }

    /**
     * Ertelenmiş CSRF token'ını her istekte yükler → {@code XSRF-TOKEN} çerezi ilk yanıtta yazılır
     * (Spring Security "SPA" deseni). Statik sayfalar token'ı sunucu şablonu olmadan çerezden okur.
     */
    static final class CsrfCerezFiltresi extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest istek, HttpServletResponse yanit, FilterChain zincir)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) istek.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                token.getToken();
            }
            zincir.doFilter(istek, yanit);
        }
    }
}
