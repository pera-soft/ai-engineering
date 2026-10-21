package tr.velzon.aiogretmen.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tr.velzon.aiogretmen.types.AiDevreDisiException;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.util.Map;

/**
 * API hata sözleşmesi (brief §8): tüm hatalar {hata, detay} JSON'u ile doğru HTTP koduyla döner.
 */
@RestControllerAdvice
public class HataYonetimi {

    private static final Logger log = LoggerFactory.getLogger(HataYonetimi.class);

    @ExceptionHandler(BulunamadiException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> bulunamadi(BulunamadiException e) {
        return Map.of("hata", "Kayıt bulunamadı", "detay", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> bozukIstek(HttpMessageNotReadableException e) {
        return Map.of("hata", "İstek gövdesi okunamadı", "detay", String.valueOf(e.getMessage()));
    }

    @ExceptionHandler(AiDevreDisiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> aiDevreDisi(AiDevreDisiException e) {
        return Map.of("hata", "AI özellikleri devre dışı", "detay", e.getMessage());
    }

    /** OpenRouter/HTTP hataları anlamlı mesaja çevrilir (402 kredi, 429 limit — inceleme 2. tur). */
    @ExceptionHandler(org.springframework.web.client.RestClientResponseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> aiSaglayiciHatasi(org.springframework.web.client.RestClientResponseException e) {
        int kod = e.getStatusCode().value();
        String detay = switch (kod) {
            case 401, 403 -> "API anahtarı geçersiz veya yetkisiz — OPENROUTER_API_KEY'i kontrol et.";
            case 402 -> "OpenRouter kredisi tükenmiş görünüyor — panelden bakiye kontrol et.";
            case 429 -> "AI servisi istek sınırında (rate limit) — biraz bekleyip tekrar dene.";
            default -> "AI sağlayıcısı " + kod + " döndürdü — birazdan tekrar dene.";
        };
        log.warn("AI sağlayıcı hatası: HTTP {} — {}", kod, e.getMessage());
        return Map.of("hata", "AI servisi hatası", "detay", detay);
    }

    @ExceptionHandler(org.springframework.web.client.ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> aiUlasilamiyor(org.springframework.web.client.ResourceAccessException e) {
        return Map.of("hata", "AI servisine ulaşılamadı",
                "detay", "Ağ hatası veya zaman aşımı — bağlantını kontrol edip tekrar dene.");
    }

    @ExceptionHandler(tr.velzon.aiogretmen.types.AiCiktisiBozukException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> aiCiktisiBozuk(tr.velzon.aiogretmen.types.AiCiktisiBozukException e) {
        return Map.of("hata", "AI beklenen çıktıyı üretemedi", "detay", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> gecersizIstek(IllegalArgumentException e) {
        return Map.of("hata", "Geçersiz istek", "detay", String.valueOf(e.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, String> dosyaBuyuk(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        return Map.of("hata", "Dosya çok büyük", "detay", "Görüntü 10MB'dan küçük olmalı.");
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> kaynakYok(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        return Map.of("hata", "Kaynak bulunamadı", "detay", e.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> beklenmeyen(Exception e) {
        log.error("Beklenmeyen hata", e);
        return Map.of("hata", "Beklenmeyen bir hata oluştu", "detay", String.valueOf(e.getMessage()));
    }
}
