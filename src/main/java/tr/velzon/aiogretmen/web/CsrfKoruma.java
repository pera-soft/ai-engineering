package tr.velzon.aiogretmen.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * İnceleme bulgusu (CSRF): 127.0.0.1'e bind etmek, kullanıcının tarayıcısındaki
 * BAŞKA bir sitenin bu sunucuya istek göndermesini engellemez (multipart "simple
 * request" sayılır, <img> GET tetikler). Bu filtre siteler-arası istekleri reddeder:
 *  - Modern tarayıcılar Sec-Fetch-Site başlığı gönderir: "cross-site" → 403.
 *  - Origin başlığı varsa yalnız kendi origin'lerimize izin verilir: 127.0.0.1/localhost +
 *    sunucunun GERÇEK portu (soketten; Host başlığından değil — o istemci kontrolünde).
 *    Böylece server.port değişince arayüz bozulmaz, yereldeki başka bir port da yabancı sayılır.
 * Sunucu tarafı araçlar (curl) bu başlıkları göndermez ve etkilenmez.
 */
@Component
public class CsrfKoruma extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest istek, HttpServletResponse yanit,
                                    FilterChain zincir) throws ServletException, IOException {
        String fetchSite = istek.getHeader("Sec-Fetch-Site");
        String origin = istek.getHeader("Origin");

        boolean crossSite = "cross-site".equalsIgnoreCase(fetchSite)
                || (origin != null && !yerelOrigin(origin, istek.getLocalPort()));

        if (crossSite && istek.getRequestURI().startsWith("/api/")) {
            yanit.setStatus(403);
            yanit.setContentType("application/json;charset=UTF-8");
            yanit.getWriter().write("{\"hata\":\"Siteler arası istek reddedildi\",\"detay\":\"Bu API yalnızca uygulamanın kendi arayüzünden çağrılabilir.\"}");
            return;
        }
        zincir.doFilter(istek, yanit);
    }

    private static boolean yerelOrigin(String origin, int sunucuPortu) {
        return origin.equals("http://127.0.0.1:" + sunucuPortu) || origin.equals("http://localhost:" + sunucuPortu);
    }
}
