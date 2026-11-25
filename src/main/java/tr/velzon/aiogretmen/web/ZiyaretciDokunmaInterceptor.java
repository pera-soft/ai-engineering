package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.service.ZiyaretciService;

import java.util.Set;

/**
 * Girişsiz sitede ziyaretçi kaydı (Faz 10, ADR-0017): yalnız YAZAN istekler ({@code POST/PUT/DELETE /api/**})
 * ziyaretçiyi kaydeder/tazeler → salt bakan ziyaretçi veritabanında iz bırakmaz. {@code /api/ziyaretci/**} hariç:
 * geri yükleme isteği, terk edilecek eski kimlik için kayıt açmamalı.
 *
 * <p>Yazma yöntemli her istekte (POST/PUT/DELETE) çalışır — işleyici isteği sonradan reddetse (400/422…) bile.
 * {@code dokun} veritabanı hatası BİLEREK yazmayı da düşürür: ziyaretçi satırı geri yüklemeyi mümkün kılan şeydir,
 * satırsız ilerleme kaydedilmemeli. {@link HizSiniriInterceptor} bundan önce çalışır.
 *
 * <p>Kendini {@link WebMvcConfigurer} olarak kaydeder (yalnız {@code site} profili). CSRF reddi Spring Security
 * zincirinde olduğundan reddedilen istek buraya ulaşmaz.
 */
@Component
@Profile("site")
public class ZiyaretciDokunmaInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private static final Set<String> YAZMA_YONTEMLERI = Set.of("POST", "PUT", "DELETE");

    private final ZiyaretciService ziyaretciService;

    public ZiyaretciDokunmaInterceptor(ZiyaretciService ziyaretciService) {
        this.ziyaretciService = ziyaretciService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry kayit) {
        kayit.addInterceptor(this).addPathPatterns("/api/**").excludePathPatterns("/api/ziyaretci/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest istek, HttpServletResponse yanit, Object isleyici) {
        if (YAZMA_YONTEMLERI.contains(istek.getMethod())
                && istek.getAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI) instanceof String kimlik
                && ZiyaretciCerezi.gecerliMi(kimlik)) {
            ziyaretciService.dokun(ZiyaretciKullaniciBaglami.ONEK + kimlik);
        }
        return true;
    }
}
