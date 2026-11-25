package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.config.SiteAyarlari;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.service.ZiyaretciService;

import java.util.Map;

/**
 * İlerleme kodu (Faz 10, ADR-0017): girişsiz sitede ilerleme çereze bağlıdır; kod (= öneksiz çerez kimliği)
 * başka tarayıcıda/cihazda girilerek aynı ilerlemeye dönülür. Kod bir parola gibidir — sahibi saklar.
 */
@RestController
@Profile("site")
@RequestMapping("/api/ziyaretci")
public class ZiyaretciController {

    private final ZiyaretciService ziyaretciService;
    private final SiteAyarlari ayarlar;

    public ZiyaretciController(ZiyaretciService ziyaretciService, SiteAyarlari ayarlar) {
        this.ziyaretciService = ziyaretciService;
        this.ayarlar = ayarlar;
    }

    /** Bu tarayıcının ilerleme kodu. Kayıt açmaz (henüz yazmamış ziyaretçinin kodu geri yüklenemez). */
    @GetMapping("/kod")
    public Map<String, String> kod(HttpServletRequest istek) {
        Object kimlik = istek.getAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI);
        if (!(kimlik instanceof String k) || !ZiyaretciCerezi.gecerliMi(k)) {
            throw new IllegalStateException("ziyaretçi kimliği istekte yok");
        }
        return Map.of("kod", k);
    }

    public record GeriYukleIstek(String kod) {
    }

    /** Geçersiz biçim → 400; kayıtsız kod → 422; başarı → çerez bu koda döner. */
    @PostMapping("/geri-yukle")
    public Map<String, String> geriYukle(@RequestBody GeriYukleIstek govde, HttpServletRequest istek,
                                         HttpServletResponse yanit) {
        String kimlik = ziyaretciService.geriYukle(govde.kod());
        ZiyaretciCerezi.degistir(istek, yanit, kimlik, ayarlar.guvenliCerez());
        istek.setAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, kimlik);
        return Map.of("durum", "geri-yuklendi");
    }
}
