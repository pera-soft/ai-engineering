package tr.velzon.aiogretmen.web;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.service.KullanimService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Arayüzün açılışta okuduğu özellik bayrakları (ADR-0011):
 *  - GET /api/ozellikler → {@code {"mulakat": false, "site": false, "sunucuAi": true}}
 *  - Ziyaretçi kimliği (HttpOnly çerez) ASLA dönmez (Faz 10); {@code sunucuAi} = !site && sunucu anahtarı var.
 * Statik index.html sunucuda işlenmez (CSP: yalnız 'self' betik); hangi bölümün gösterileceğini
 * frontend bu uçtan öğrenir. Mülakat kapalıyken mulakat.js hiçbir /api/mulakat isteği atmaz.
 */
@RestController
@RequestMapping("/api")
public class OzellikController {

    private final AppProperties props;
    private final KullanimService kullanimService;
    private final boolean site;

    public OzellikController(AppProperties props, KullanimService kullanimService, Environment ortam) {
        this.props = props;
        this.kullanimService = kullanimService;
        this.site = ortam.matchesProfiles("site");
    }

    @GetMapping("/ozellikler")
    public Map<String, Object> ozellikler() {
        Map<String, Object> ozellik = new LinkedHashMap<>();
        ozellik.put("mulakat", props.mulakatEtkin() && !site); // sitede zorla kapalı (MulakatEtkin, Faz 9b)
        ozellik.put("site", site);
        // sunucu anahtarıyla çalışan AI düğmeleri (rapor/değerlendirme/AI quiz): yalnız yerelde ve anahtar varsa
        ozellik.put("sunucuAi", !site && kullanimService.aiAktif());
        return ozellik;
    }
}
