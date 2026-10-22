package tr.velzon.aiogretmen.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.velzon.aiogretmen.config.SiteAyarlari;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * İnceleme bulgusu (CSRF): 127.0.0.1'e bind etmek, kullanıcının tarayıcısındaki
 * BAŞKA bir sitenin bu sunucuya istek göndermesini engellemez (multipart "simple
 * request" sayılır, <img> GET tetikler). Bu filtre siteler-arası istekleri reddeder:
 *  - Modern tarayıcılar Sec-Fetch-Site başlığı gönderir: "cross-site" → 403.
 *  - Origin başlığı varsa yalnız kendi origin'lerimize izin verilir: 127.0.0.1/localhost +
 *    sunucunun GERÇEK portu (soketten; Host başlığından değil — o istemci kontrolünde).
 *    Böylece server.port değişince arayüz bozulmaz, yereldeki başka bir port da yabancı sayılır.
 * Sunucu tarafı araçlar (curl) bu başlıkları göndermez ve etkilenmez.
 *
 * <p>Faz 9a ({@code site} profili, ADR-0012): nginx/TLS arkasında tarayıcının origin'i
 * {@code https://<alan-adı>} olur → {@code app.site.izinli-originler} listesindeki origin'ler de
 * (birebir eşleşme; kırpılır, sondaki {@code /} atılır) kabul edilir. Liste boşsa (yerel mod)
 * davranış eskisiyle aynıdır. Site profilinde Spring Security CSRF token'ı ayrıca zorunludur;
 * bu filtre ek (derinlemesine savunma) katmandır.
 */
@Component
public class CsrfKoruma extends OncePerRequestFilter {

    private final Set<String> izinliOriginler;

    /** Yerel mod / testler: yalnız 127.0.0.1|localhost + gerçek port. */
    public CsrfKoruma() {
        this(List.of());
    }

    /** ObjectProvider: @WebMvcTest dilimlerinde {@link SiteAyarlari} bean'i yoktur → boş liste (yerel). */
    @Autowired
    public CsrfKoruma(ObjectProvider<SiteAyarlari> ayarlar) {
        this(ayarlar.getIfAvailable(() -> new SiteAyarlari(List.of(), true, null, true, null, null, null)).izinliOriginler());
    }

    public CsrfKoruma(List<String> izinliOriginler) {
        this.izinliOriginler = (izinliOriginler == null ? List.<String>of() : izinliOriginler).stream()
                .map(String::trim)
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .filter(o -> !o.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest istek, HttpServletResponse yanit,
                                    FilterChain zincir) throws ServletException, IOException {
        String fetchSite = istek.getHeader("Sec-Fetch-Site");
        String origin = istek.getHeader("Origin");

        boolean crossSite = "cross-site".equalsIgnoreCase(fetchSite)
                || (origin != null && !izinliOrigin(origin, istek.getLocalPort()));

        if (crossSite && istek.getRequestURI().startsWith("/api/")) {
            yanit.setStatus(403);
            yanit.setContentType("application/json;charset=UTF-8");
            yanit.getWriter().write("{\"hata\":\"Siteler arası istek reddedildi\",\"detay\":\"Bu API yalnızca uygulamanın kendi arayüzünden çağrılabilir.\"}");
            return;
        }
        zincir.doFilter(istek, yanit);
    }

    private boolean izinliOrigin(String origin, int sunucuPortu) {
        return yerelOrigin(origin, sunucuPortu) || izinliOriginler.contains(origin);
    }

    private static boolean yerelOrigin(String origin, int sunucuPortu) {
        return origin.equals("http://127.0.0.1:" + sunucuPortu) || origin.equals("http://localhost:" + sunucuPortu);
    }
}
