package tr.velzon.aiogretmen.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Çok kullanıcılı {@code site} profili güvenlik zinciri (Faz 9a, ADR-0012).
 *
 * <ul>
 *   <li><strong>Form + sunucu oturumu:</strong> {@code POST /api/kimlik/giris} (email, sifre, _csrf);
 *       başarı → {@code /}, hata → {@code /giris.html?hata=bilgi|dogrulanmamis|kilitli|kapali}.</li>
 *   <li><strong>Kimliksiz erişim:</strong> {@code /api/**} → 401 (JSON istemcisi yönlendirme izlemesin);
 *       sayfalar → {@code /giris.html}'e yönlendirme. Açık uçlar: giriş/kayıt/doğrulama/şifre sıfırlama
 *       sayfaları, {@code style.css}, {@code kimlik.js}, kayıt, doğrulama, yeniden gönder, şifremi unuttum ve
 *       şifre sıfırla uçları (Faz 9c; hepsi CSRF'li), kayıt yöntemi bayrağı {@code GET /api/kimlik/ayarlar}
 *       (ADR-0015).</li>
 *   <li><strong>CSRF token AÇIK:</strong> {@link CookieCsrfTokenRepository} → {@code XSRF-TOKEN} çerezi
 *       (JS okur, HttpOnly DEĞİL) ↔ {@code X-XSRF-TOKEN} başlığı / {@code _csrf} form alanı. Düz (XOR'suz)
 *       işleyici: statik ön yüz çerezdeki değeri aynen geri yollar. {@link CsrfCerezFiltresi} her yanıtta
 *       token'ı yükler ki çerez ilk sayfa açılışında yazılsın. {@code web/CsrfKoruma} (Origin/Sec-Fetch-Site)
 *       ek katman olarak çalışmaya devam eder.</li>
 *   <li><strong>Çıkış:</strong> {@code POST /api/kimlik/cikis} (CSRF'li) → 204; ön yüz giriş sayfasına gider.</li>
 *   <li>Kaydedilen istek (request cache) KAPALI: kimliksiz API çağrısı oturum yaratmasın; giriş hep {@code /}'e.</li>
 * </ul>
 *
 * <p><strong>Oturum kaydı (Faz 9b):</strong> {@link SessionRegistry} kullanıcının TÜM oturumlarını bilir →
 * şifre sıfırlama (Faz 9c) diğer tarayıcılardaki oturumları da anında düşürür (eski şifreyle açılmış oturum sürmesin).
 * Süresi düşürülmüş oturumla gelen {@code /api/**} isteği 401, sayfa isteği giriş sayfasına gider.
 *
 * <p><strong>Hesap kapatma anında etkili (Faz 9d):</strong> {@link HesapEtkinlikFiltresi} kimlikli HER istekte
 * hesabı yeniden okur (e-postanın UNIQUE indeksi üzerinden tek sorgu); hesap silinmiş ya da {@code etkin=false}
 * ise oturum geçersiz kılınır, {@code /api/**} → 401, sayfa → {@code /giris.html?hata=kapali}. Kilit (kaba
 * kuvvet) açık oturumu DÜŞÜRMEZ — saldırgan başkasının oturumunu yanlış şifreyle kapatamasın.
 *
 * <p><strong>Ortak modüller (Faz 9b):</strong> {@code site} profili MCP sunucusuyla (ADR-0007) birlikte
 * açılamaz — açılışta hata (kimlik modelleri ve ortak araçlar çok kullanıcılı siteye göre değil).
 * Mülakat {@code MulakatEtkin} üzerinde {@code @Profile("!site")} ile zorla kapalı.
 *
 * <p><strong>Katman/ArchUnit:</strong> yalnız Spring tipleri; {@code UserDetailsService} ve olay dinleyicisi
 * service katmanında bean olarak gelir (config → service bağımlılığı YOK).
 */
@Configuration
@Profile("site")
public class SiteGuvenlikYapilandirma {

    public static final String GIRIS_SAYFASI = "/giris.html";

    /** Açılış denetimi: site + MCP birlikte ÇALIŞMAZ (ortam değişkeniyle yanlışlıkla açılmasın). */
    public SiteGuvenlikYapilandirma(Environment ortam) {
        boolean mcpAcik = ortam.getProperty("spring.ai.mcp.server.enabled", Boolean.class, false)
                || ortam.matchesProfiles("mcp", "mcp-http");
        if (mcpAcik) {
            throw new IllegalStateException("site profili MCP sunucusuyla birlikte açılamaz "
                    + "(spring.ai.mcp.server.enabled / mcp / mcp-http kapalı olmalı) — ADR-0012");
        }
    }

    /** Kullanıcının tüm oturumları (şifre sıfırlamada hepsini düşürmek için). */
    @Bean
    SessionRegistry oturumKaydi() {
        return new SessionRegistryImpl();
    }

    /** Oturum yok edilince kayıttan da düşsün (bellek sızıntısı olmasın). Boot dinleyici olarak kaydeder. */
    @Bean
    static HttpSessionEventPublisher oturumOlayYayici() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    PasswordEncoder sifreKodlayici() {
        return new BCryptPasswordEncoder(); // maliyet 10 (varsayılan)
    }

    @Bean
    SecurityFilterChain siteGuvenlikZinciri(HttpSecurity http, SiteAyarlari ayarlar,
                                            SessionRegistry oturumKaydi,
                                            UserDetailsService hesaplar) throws Exception {
        CookieCsrfTokenRepository csrfDeposu = CookieCsrfTokenRepository.withHttpOnlyFalse();
        // guvenli-cerez=true → her zaman Secure; false (yalnız yerel http denemesi) → isteğe göre: nginx
        // arkasında X-Forwarded-Proto=https gelirse yine Secure (ayar unutulsa da canlıda düz çerez olmaz).
        csrfDeposu.setCookieCustomizer(c -> {
            if (ayarlar.guvenliCerez()) {
                c.secure(true);
            }
            c.sameSite("Lax");
        });

        PathPatternRequestMatcher.Builder yol = PathPatternRequestMatcher.withDefaults();

        http
                .authorizeHttpRequests(yetki -> yetki
                        .requestMatchers(HttpMethod.GET, GIRIS_SAYFASI, "/kayit.html", "/dogrulama-bekleniyor.html",
                                "/dogrulama-sonuc.html", "/sifremi-unuttum.html", "/sifre-sifirla.html", "/kimlik.js",
                                "/style.css", "/api/kimlik/dogrula", "/api/kimlik/ayarlar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/kimlik/kayit", "/api/kimlik/dogrulama-yeniden",
                                "/api/kimlik/sifremi-unuttum", "/api/kimlik/sifre-sifirla").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfDeposu)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCerezFiltresi(), CsrfFilter.class)
                .addFilterBefore(new HesapEtkinlikFiltresi(hesaplar), AuthorizationFilter.class)
                .formLogin(form -> form
                        .loginPage(GIRIS_SAYFASI)
                        .loginProcessingUrl("/api/kimlik/giris")
                        .usernameParameter("email")
                        .passwordParameter("sifre")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(girisHatasiYonlendir())
                        .permitAll())
                .logout(cikis -> cikis
                        .logoutRequestMatcher(yol.matcher(HttpMethod.POST, "/api/kimlik/cikis"))
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(hata -> hata
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                yol.matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint(GIRIS_SAYFASI),
                                AnyRequestMatcher.INSTANCE))
                .sessionManagement(oturum -> oturum
                        .maximumSessions(-1) // sınırsız eşzamanlı oturum; amaç yalnız kayıt (toplu düşürme)
                        .sessionRegistry(oturumKaydi)
                        .expiredSessionStrategy(dusurulmusOturum()))
                .requestCache(onbellek -> onbellek.requestCache(new NullRequestCache()))
                .httpBasic(basic -> basic.disable());
        return http.build();
    }

    /** Düşürülmüş oturum (ör. hesap silindi): API → 401, sayfa → giriş. */
    private static SessionInformationExpiredStrategy dusurulmusOturum() {
        return olay -> {
            HttpServletRequest istek = olay.getRequest();
            HttpServletResponse yanit = olay.getResponse();
            if (istek.getRequestURI().startsWith(istek.getContextPath() + "/api/")) {
                yanit.sendError(HttpStatus.UNAUTHORIZED.value());
            } else {
                yanit.sendRedirect(istek.getContextPath() + GIRIS_SAYFASI);
            }
        };
    }

    /** Hata türünü giriş sayfasına kod olarak taşır (mesajı sayfa Türkçe gösterir). */
    private static AuthenticationFailureHandler girisHatasiYonlendir() {
        return (istek, yanit, hata) -> {
            String kod;
            if (hata instanceof LockedException) {
                kod = "kilitli";
            } else if (hata instanceof DisabledException) {
                kod = "dogrulanmamis";
            } else if (hata instanceof AccountExpiredException) {
                kod = "kapali";
            } else {
                kod = "bilgi"; // yanlış şifre ya da olmayan hesap — ayırt ETMEZ
            }
            yanit.sendRedirect(istek.getContextPath() + GIRIS_SAYFASI + "?hata=" + kod);
        };
    }

    /**
     * Oturumdaki hesap hâlâ var ve açık mı? Her kimlikli istekte {@link UserDetailsService} ile (service
     * katmanındaki {@code KullaniciDetayServisi}; config → service bağımlılığı YOK) yeniden okunur — ~60
     * kullanıcıda indeksli tek sorgu ucuzdur, önbellek gereksiz. Kapalıysa/yoksa oturum düşer: API → 401,
     * sayfa → {@code /giris.html?hata=kapali}. Yalnız {@code etkin}/varlık bakılır; kilit ve doğrulama değil.
     */
    static final class HesapEtkinlikFiltresi extends OncePerRequestFilter {
        private final UserDetailsService hesaplar;

        HesapEtkinlikFiltresi(UserDetailsService hesaplar) {
            this.hesaplar = hesaplar;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest istek, HttpServletResponse yanit, FilterChain zincir)
                throws ServletException, IOException {
            Authentication kimlik = SecurityContextHolder.getContext().getAuthentication();
            if (kimlik != null && kimlik.isAuthenticated() && !(kimlik instanceof AnonymousAuthenticationToken)
                    && !hesapAcik(kimlik.getName())) {
                HttpSession oturum = istek.getSession(false);
                if (oturum != null) {
                    oturum.invalidate();
                }
                SecurityContextHolder.clearContext();
                if (istek.getRequestURI().startsWith(istek.getContextPath() + "/api/")) {
                    yanit.sendError(HttpStatus.UNAUTHORIZED.value());
                } else {
                    yanit.sendRedirect(istek.getContextPath() + GIRIS_SAYFASI + "?hata=kapali");
                }
                return;
            }
            zincir.doFilter(istek, yanit);
        }

        private boolean hesapAcik(String kullaniciAdi) {
            try {
                UserDetails hesap = hesaplar.loadUserByUsername(kullaniciAdi);
                return hesap.isAccountNonExpired(); // etkin=false ↔ accountExpired (KullaniciDetayServisi)
            } catch (UsernameNotFoundException e) {
                return false;
            }
        }
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
