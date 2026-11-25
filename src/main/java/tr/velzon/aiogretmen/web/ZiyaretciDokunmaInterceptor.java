package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
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
 * ziyaretçiyi kaydeder/tazeler → salt bakan ziyaretçi veritabanında iz bırakmaz. {@code GET /api/**} yalnız satırı
 * ZATEN olan ziyaretçinin {@code son_gorulme}'sini (günde en çok bir kez) tazeler, satır açmaz — yalnız okuyan
 * ziyaretçi 180 gün temizliğinde "gelmeyen" sayılmasın (son inceleme #3). Statik dosyalar ({@code /api/**} dışı)
 * hiç dokunmaz; GET'teki tazeleme hatası ({@link DataAccessException}) okumayı düşürmez, WARN loglanır.
 * {@code /api/ziyaretci/**} hariç: geri yükleme isteği, terk edilecek eski kimlik için kayıt açmamalı.
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

    private static final Logger log = LoggerFactory.getLogger(ZiyaretciDokunmaInterceptor.class);

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
        if (!(istek.getAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI) instanceof String kimlik)
                || !ZiyaretciCerezi.gecerliMi(kimlik)) {
            return true;
        }
        if (YAZMA_YONTEMLERI.contains(istek.getMethod())) {
            ziyaretciService.dokun(ZiyaretciKullaniciBaglami.ONEK + kimlik);
        } else if ("GET".equals(istek.getMethod())) {
            String kullaniciId = ZiyaretciKullaniciBaglami.ONEK + kimlik;
            try {
                ziyaretciService.tazele(kullaniciId);
            } catch (DataAccessException e) {
                // GET'te tazeleme yan iştir: okuma düşmez (yazmada düşer — satır geri yüklemeyi mümkün kılar).
                log.warn("Ziyaretçi son_gorulme tazelenemedi ({}…): {}", kullaniciId.substring(0, 6),
                        e.getClass().getSimpleName());
            }
        }
        return true;
    }
}
