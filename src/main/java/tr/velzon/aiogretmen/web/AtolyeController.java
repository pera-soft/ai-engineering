package tr.velzon.aiogretmen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.service.AtolyeService;
import tr.velzon.aiogretmen.types.AtolyeKategori;

import java.util.List;

/**
 * Prompt Atölyesi uçları (Faz A):
 *  - GET  /api/atolye            → kategori ağacı (anahtarsız da çalışır, statik akış).
 *  - POST /api/atolye/degerlendir → {@link AtolyeAiController} (yalnız !site; sitede 404, Faz 10).
 * Boş girdi → 400; alıştırma yok → 404; anahtar yoksa → 503 (HataYonetimi sözleşmesi).
 */
@RestController
@RequestMapping("/api/atolye")
public class AtolyeController {

    private final AtolyeService atolyeService;

    public AtolyeController(AtolyeService atolyeService) {
        this.atolyeService = atolyeService;
    }

    @GetMapping
    public List<AtolyeKategori> kategoriler() {
        return atolyeService.kategoriler();
    }
}
