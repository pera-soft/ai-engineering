package tr.velzon.aiogretmen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.config.AppProperties;

import java.util.Map;

/**
 * Arayüzün açılışta okuduğu özellik bayrakları (ADR-0011):
 *  - GET /api/ozellikler → {@code {"mulakat": false}}
 * Statik index.html sunucuda işlenmez (CSP: yalnız 'self' betik); hangi bölümün gösterileceğini
 * frontend bu uçtan öğrenir. Mülakat kapalıyken mulakat.js hiçbir /api/mulakat isteği atmaz.
 */
@RestController
@RequestMapping("/api")
public class OzellikController {

    private final AppProperties props;

    public OzellikController(AppProperties props) {
        this.props = props;
    }

    @GetMapping("/ozellikler")
    public Map<String, Object> ozellikler() {
        return Map.of("mulakat", props.mulakatEtkin());
    }
}
