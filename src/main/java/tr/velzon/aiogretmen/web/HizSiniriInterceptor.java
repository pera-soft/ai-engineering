package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.service.HizSiniri;
import tr.velzon.aiogretmen.service.ZiyaretciService;
import tr.velzon.aiogretmen.types.HizSiniriAsildiException;

import java.time.Duration;
import java.util.Set;

/**
 * Girişsiz sitede (Faz 10, ADR-0017) kötüye kullanıma karşı IP/ziyaretçi hız sınırları; aşımda
 * {@link HizSiniriAsildiException} → 429. Yalnız YAZAN istekler ({@code POST/PUT/DELETE /api/**}) sayılır.
 * İstemci IP'si {@code getRemoteAddr()}'dır ({@code server.forward-headers-strategy: native} nginx'in
 * X-Forwarded-For'unu çözer).
 *
 * <p>{@link ZiyaretciDokunmaInterceptor}'dan ÖNCE çalışır ({@code order(-1)}): reddedilen istek ziyaretçi satırı
 * açmamalı. Yeni ziyaretçi sınırı, kaydı henüz olmayan ziyaretçinin ilk yazmalarını IP başına sayar.
 */
@Component
@Profile("site")
public class HizSiniriInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    static final int YAZMA_EN_COK = 60;
    static final Duration YAZMA_PENCERE = Duration.ofMinutes(1);
    static final int SOHBET_EN_COK = 20;
    static final Duration SOHBET_PENCERE = Duration.ofMinutes(1);
    static final int GERI_YUKLE_EN_COK = 10;
    static final Duration GERI_YUKLE_PENCERE = Duration.ofHours(1);
    static final int YENI_ZIYARETCI_EN_COK = 20;
    static final Duration YENI_ZIYARETCI_PENCERE = Duration.ofHours(1);

    private static final Set<String> YAZMA_YONTEMLERI = Set.of("POST", "PUT", "DELETE");

    private final HizSiniri hizSiniri;
    private final ZiyaretciService ziyaretciService;

    public HizSiniriInterceptor(HizSiniri hizSiniri, ZiyaretciService ziyaretciService) {
        this.hizSiniri = hizSiniri;
        this.ziyaretciService = ziyaretciService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry kayit) {
        kayit.addInterceptor(this).addPathPatterns("/api/**").order(-1);
    }

    @Override
    public boolean preHandle(HttpServletRequest istek, HttpServletResponse yanit, Object isleyici) {
        if (!YAZMA_YONTEMLERI.contains(istek.getMethod())) {
            return true;
        }
        String ip = istek.getRemoteAddr();
        // yönlendirmenin kullandığı ÇÖZÜLMÜŞ yol: "/api/%73ohbet" de /api/sohbet'e gider, sınırı atlayamasın
        String yol = UrlPathHelper.defaultInstance.getLookupPathForRequest(istek);
        boolean post = "POST".equals(istek.getMethod());
        String kimlik = istek.getAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI) instanceof String k
                && ZiyaretciCerezi.gecerliMi(k) ? k : null;

        sinirla("yaz|" + ip, YAZMA_EN_COK, YAZMA_PENCERE);
        // yeni-ziyaretçi sınırı, ziyaretçiye özgü herhangi bir anahtardan ÖNCE: reddedilen taze kimlik bellek bırakmaz.
        // Geri yükleme eski kimlik için kayıt açmaz (dokunma interceptor'ı da /api/ziyaretci/**'yi atlar).
        if (kimlik != null && !yol.startsWith("/api/ziyaretci/")
                && !ziyaretciService.varMi(ZiyaretciKullaniciBaglami.ONEK + kimlik)) {
            sinirla("yeni|" + ip, YENI_ZIYARETCI_EN_COK, YENI_ZIYARETCI_PENCERE);
        }
        if (post && "/api/sohbet".equals(yol) && kimlik != null) {
            sinirla("sohbet|" + ZiyaretciKullaniciBaglami.ONEK + kimlik, SOHBET_EN_COK, SOHBET_PENCERE);
        }
        if (post && "/api/ziyaretci/geri-yukle".equals(yol)) {
            sinirla("geri|" + ip, GERI_YUKLE_EN_COK, GERI_YUKLE_PENCERE);
        }
        return true;
    }

    private void sinirla(String anahtar, int enCok, Duration pencere) {
        if (!hizSiniri.izinVer(anahtar, enCok, pencere)) {
            throw new HizSiniriAsildiException();
        }
    }
}
