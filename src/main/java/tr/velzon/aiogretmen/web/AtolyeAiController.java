package tr.velzon.aiogretmen.web;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.service.AtolyeService;
import tr.velzon.aiogretmen.types.AtolyeDegerlendirme;

import static tr.velzon.aiogretmen.web.AtolyeController.sinirla;

/**
 * Prompt Atölyesi'nin sunucu anahtarıyla çalışan ucu: POST /api/atolye/degerlendir.
 * Faz 10 (ADR-0017): {@code site} profilinde YOK (istek → JSON 404). Yerelde davranış aynıdır;
 * kategori ağacı ({@code GET /api/atolye}) {@link AtolyeController}'da sitede de açıktır.
 */
@RestController
@Profile("!site")
@RequestMapping("/api/atolye")
public class AtolyeAiController {

    private final AtolyeService atolyeService;

    public AtolyeAiController(AtolyeService atolyeService) {
        this.atolyeService = atolyeService;
    }

    public record DegerlendirIstek(String alistirmaId, String ogrenciPrompt) {
    }

    @PostMapping("/degerlendir")
    public AtolyeDegerlendirme degerlendir(@RequestBody DegerlendirIstek istek) {
        String alistirmaId = sinirla("alistirmaId", istek.alistirmaId());
        String ogrenciPrompt = sinirla("ogrenciPrompt", istek.ogrenciPrompt());
        return atolyeService.degerlendir(alistirmaId, ogrenciPrompt);
    }
}
