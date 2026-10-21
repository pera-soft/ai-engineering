package tr.velzon.aiogretmen.web;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tr.velzon.aiogretmen.service.KitapService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kitap bilgi tabanı uçları (kullanıcı eklemesi): not yapıştırma + sayfa fotoğrafı.
 * Faz 9b (ADR-0012): {@code site} profilinde YOK (istek → 404) — kitap notları ve vektör deposu tüm
 * kullanıcılar için ORTAK; birinin notu diğerinin prompt'una sızardı. Kişiye ayırmak ayrı iş.
 */
@RestController
@Profile("!site")
@RequestMapping("/api")
public class KitapController {

    private static final Set<String> IZINLI_MIME = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_GORUNTU_BOYUT = 10 * 1024 * 1024; // 10MB

    private final KitapService kitapService;

    public KitapController(KitapService kitapService) {
        this.kitapService = kitapService;
    }

    public record KitapNotuIstek(String kitap, String not) {
    }

    static final int MAX_NOT = 20_000;
    static final int MAX_KITAP_ADI = 200;

    /** Kitap adı dosya frontmatter'ına gömülür — satır sonu ve uzunluk temizlenir (inceleme bulgusu). */
    private static String kitapAdiTemizle(String kitap) {
        if (kitap == null || kitap.isBlank()) {
            throw new IllegalArgumentException("kitap adı zorunlu");
        }
        String temiz = kitap.replaceAll("[\\r\\n\\t]", " ").strip();
        if (temiz.length() > MAX_KITAP_ADI) {
            throw new IllegalArgumentException("kitap adı çok uzun (üst sınır " + MAX_KITAP_ADI + " karakter)");
        }
        return temiz;
    }

    @PostMapping("/kitap-notu")
    public KitapService.IslenmisNot notEkle(@RequestBody KitapNotuIstek istek) {
        if (istek.not() == null || istek.not().isBlank()) {
            throw new IllegalArgumentException("not alanı zorunlu");
        }
        if (istek.not().length() > MAX_NOT) {
            throw new IllegalArgumentException("not çok uzun — üst sınır " + MAX_NOT + " karakter; bölerek gönder");
        }
        return kitapService.notEkle(kitapAdiTemizle(istek.kitap()), istek.not());
    }

    @PostMapping("/kitap-notu/goruntu")
    public KitapService.IslenmisNot goruntuEkle(@RequestParam("kitap") String kitap,
                                                @RequestParam("goruntu") MultipartFile goruntu) throws IOException {
        String temizAd = kitapAdiTemizle(kitap);
        if (goruntu.isEmpty()) {
            throw new IllegalArgumentException("görüntü zorunlu");
        }
        if (goruntu.getSize() > MAX_GORUNTU_BOYUT) {
            throw new IllegalArgumentException("Görüntü 10MB'dan büyük olamaz");
        }
        String mime = goruntu.getContentType();
        if (mime == null || !IZINLI_MIME.contains(mime)) {
            throw new IllegalArgumentException("Desteklenen biçimler: JPEG, PNG, WebP, GIF");
        }
        return kitapService.goruntudenEkle(temizAd, goruntu.getBytes(), mime);
    }

    @GetMapping("/kitaplar")
    public List<Map<String, Object>> kitaplar() {
        return kitapService.kitaplar();
    }
}
