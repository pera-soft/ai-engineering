package tr.velzon.aiogretmen.web;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.config.KullaniciBaglami;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Arayüzün açılışta okuduğu özellik bayrakları (ADR-0011):
 *  - GET /api/ozellikler → {@code {"mulakat": false, "site": false}}
 *  - {@code site} profilinde (ADR-0012) ayrıca oturumdaki kullanıcı: {@code "kullanici": "x@pera.software"}
 *    → arayüz e-postayı ve "Çıkış" düğmesini gösterir.
 * Statik index.html sunucuda işlenmez (CSP: yalnız 'self' betik); hangi bölümün gösterileceğini
 * frontend bu uçtan öğrenir. Mülakat kapalıyken mulakat.js hiçbir /api/mulakat isteği atmaz.
 */
@RestController
@RequestMapping("/api")
public class OzellikController {

    private final AppProperties props;
    private final KullaniciBaglami kullaniciBaglami;
    private final boolean site;

    public OzellikController(AppProperties props, KullaniciBaglami kullaniciBaglami, Environment ortam) {
        this.props = props;
        this.kullaniciBaglami = kullaniciBaglami;
        this.site = ortam.matchesProfiles("site");
    }

    @GetMapping("/ozellikler")
    public Map<String, Object> ozellikler() {
        Map<String, Object> ozellik = new LinkedHashMap<>();
        ozellik.put("mulakat", props.mulakatEtkin() && !site); // sitede zorla kapalı (MulakatEtkin, Faz 9b)
        ozellik.put("site", site);
        if (site) {
            ozellik.put("kullanici", kullaniciBaglami.aktifKullanici());
        }
        return ozellik;
    }
}
