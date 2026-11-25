package tr.velzon.aiogretmen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.service.KullanimService;
import tr.velzon.aiogretmen.service.SohbetService;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.util.List;
import java.util.Map;

/**
 * AI öğretmen uçları (brief §8): sohbet, sohbet geçmişi, kullanım istatistiği. Sitede de açık.
 * Değerlendirme/örnek/açıklama/quiz üretimi/rapor → {@link OgretmenAiOzellikController} (yalnız !site, Faz 10).
 */
@RestController
@RequestMapping("/api")
public class OgretmenController {

    private final SohbetService sohbetService;
    private final KullanimService kullanimService;

    public OgretmenController(SohbetService sohbetService, KullanimService kullanimService) {
        this.sohbetService = sohbetService;
        this.kullanimService = kullanimService;
    }

    /** Girdi üst sınırı (inceleme bulgusu): dev yapıştırma → context aşımı → anlamsız 500 olmasın. */
    static final int MAX_GIRDI = 20_000;

    static String sinirla(String alanAdi, String deger) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz");
        }
        if (deger.length() > MAX_GIRDI) {
            throw new IllegalArgumentException(
                    alanAdi + " çok uzun (%d karakter) — üst sınır %d. Metni bölerek gönder."
                            .formatted(deger.length(), MAX_GIRDI));
        }
        return deger;
    }

    /**
     * Hafta alanı zorunlu: eksik/null gelirse primitive int sessizce 0 olurdu (ana sayfadan sohbet hatası).
     * Var olmayan hafta numarası (0, 13…) servis katmanında BulunamadiException → 404 olur.
     */
    static int haftaGerekli(Integer hafta) {
        if (hafta == null) {
            throw new IllegalArgumentException("hafta alanı eksik — önce bir hafta seç.");
        }
        return hafta;
    }

    public record SohbetIstek(Integer hafta, Integer gun, String mesaj) {
    }

    @PostMapping("/sohbet")
    public SohbetMesaji sohbet(@RequestBody SohbetIstek istek) {
        return sohbetService.mesajGonder(haftaGerekli(istek.hafta()), istek.gun(), sinirla("mesaj", istek.mesaj()));
    }

    @GetMapping("/sohbet/{hafta}")
    public List<SohbetMesaji> sohbetGecmisi(@PathVariable int hafta) {
        return sohbetService.gecmis(hafta);
    }

    @GetMapping("/kullanim")
    public Map<String, Object> kullanim() {
        return kullanimService.istatistik();
    }
}
