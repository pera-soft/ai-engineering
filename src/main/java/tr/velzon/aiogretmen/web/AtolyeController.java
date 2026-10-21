package tr.velzon.aiogretmen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.service.AtolyeService;
import tr.velzon.aiogretmen.types.AtolyeDegerlendirme;
import tr.velzon.aiogretmen.types.AtolyeKategori;

import java.util.List;

/**
 * Prompt Atölyesi uçları (Faz A):
 *  - GET  /api/atolye            → kategori ağacı (anahtarsız da çalışır, statik akış).
 *  - POST /api/atolye/degerlendir → öğrenci prompt'unu AI Öğretmen değerlendirir.
 * Boş girdi → 400; alıştırma yok → 404; anahtar yoksa → 503 (HataYonetimi sözleşmesi).
 */
@RestController
@RequestMapping("/api/atolye")
public class AtolyeController {

    /** Girdi üst sınırı (OgretmenController deseni): dev yapıştırma → context aşımı olmasın. */
    static final int MAX_GIRDI = 20_000;

    private final AtolyeService atolyeService;

    public AtolyeController(AtolyeService atolyeService) {
        this.atolyeService = atolyeService;
    }

    @GetMapping
    public List<AtolyeKategori> kategoriler() {
        return atolyeService.kategoriler();
    }

    public record DegerlendirIstek(String alistirmaId, String ogrenciPrompt) {
    }

    @PostMapping("/degerlendir")
    public AtolyeDegerlendirme degerlendir(@RequestBody DegerlendirIstek istek) {
        String alistirmaId = sinirla("alistirmaId", istek.alistirmaId());
        String ogrenciPrompt = sinirla("ogrenciPrompt", istek.ogrenciPrompt());
        return atolyeService.degerlendir(alistirmaId, ogrenciPrompt);
    }

    private static String sinirla(String alanAdi, String deger) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz");
        }
        if (deger.length() > MAX_GIRDI) {
            throw new IllegalArgumentException(
                    alanAdi + " çok uzun (%d karakter) — üst sınır %d."
                            .formatted(deger.length(), MAX_GIRDI));
        }
        return deger;
    }
}
