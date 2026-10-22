package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tr.velzon.aiogretmen.types.AiCiktisiBozukException;
import tr.velzon.aiogretmen.types.AiDevreDisiException;
import tr.velzon.aiogretmen.types.AiSaglayiciException;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * API hata sözleşmesi (brief §8): tüm hatalar {hata, detay} JSON'u ile doğru HTTP koduyla döner;
 * arayüz {@code detay}'ı (yoksa {@code hata}'yı) kullanıcıya gösterir. {@code detay} kullanıcıya
 * yöneliktir — sunucu yolu, istisna mesajı, sağlayıcı gövdesi ya da anahtar TAŞIMAZ; teşhis logdadır.
 *
 * <p>Tarayıcı sayfa istekleri ({@code /api/} dışı + {@code Accept: text/html}) JSON almaz: hata
 * {@code sendError} ile Spring Boot'un {@code /error} akışına devredilir → {@code static/error/*.html}.
 */
@RestControllerAdvice
public class HataYonetimi {

    private static final Logger log = LoggerFactory.getLogger(HataYonetimi.class);

    static final String BEKLENMEYEN_DETAY = "Beklenmeyen bir hata oluştu. Tekrar dene; sürerse site sorumlusuna bildir.";
    static final String AI_KAPALI_SITE = "AI öğretmen henüz açılmadı (OpenRouter anahtarı bekleniyor). "
            + "Dersleri okuyup görevlerini işaretleyebilirsin; AI açılınca burada çalışacak.";

    private final boolean site;

    public HataYonetimi(Environment ortam) {
        this.site = ortam.matchesProfiles("site");
    }

    @ExceptionHandler(BulunamadiException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> bulunamadi(BulunamadiException e) {
        return Map.of("hata", "Kayıt bulunamadı", "detay", e.getMessage());
    }

    /** Jackson'ın ham mesajı Java sınıf adları taşır — kullanıcıya sabit Türkçe metin, ayrıntı logda. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> bozukIstek(HttpMessageNotReadableException e) {
        log.debug("Okunamayan istek gövdesi: {}", e.getMessage());
        return Map.of("hata", "İstek gövdesi okunamadı",
                "detay", "İstek gövdesi geçersiz JSON ya da alan türleri hatalı.");
    }

    /** /api/hafta/abc gibi: yol/parametre türü uymuyor → 400 (eskiden genel 500'e düşüyordu). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> turUyusmazligi(MethodArgumentTypeMismatchException e) {
        return Map.of("hata", "Geçersiz istek", "detay", "'" + e.getName() + "' değeri geçersiz.");
    }

    /** Anahtarsız açılış. Sitede geliştirici yönergesi (ortam değişkeni) yerine kullanıcı dili. */
    @ExceptionHandler(AiDevreDisiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> aiDevreDisi(AiDevreDisiException e) {
        return Map.of("hata", "AI özellikleri devre dışı", "detay", site ? AI_KAPALI_SITE : e.getMessage());
    }

    /**
     * Sağlayıcı (OpenRouter) hatası — ai/ katmanında kategoriye çevrildi (AiHataCevirici; log orada).
     * Hepsi 503 (servis şu an kullanılamıyor); yalnız sağlayıcının reddettiği istek 502.
     * Gövde: {@code {"hata": "AI servisi kullanılamıyor", "detay": <Türkçe mesaj>, "neden": <kategori>}}.
     */
    @ExceptionHandler(AiSaglayiciException.class)
    public ResponseEntity<Map<String, String>> aiSaglayiciHatasi(AiSaglayiciException e) {
        HttpStatus durum = e.neden() == AiSaglayiciException.Neden.ISTEK_REDDEDILDI
                ? HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
        Map<String, String> govde = new LinkedHashMap<>();
        govde.put("hata", "AI servisi kullanılamıyor");
        govde.put("detay", e.neden().mesaj());
        govde.put("neden", e.neden().name());
        return ResponseEntity.status(durum).body(govde);
    }

    @ExceptionHandler(AiCiktisiBozukException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> aiCiktisiBozuk(AiCiktisiBozukException e) {
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
    public ResponseEntity<Map<String, String>> kaynakYok(org.springframework.web.servlet.resource.NoResourceFoundException e,
                                                         HttpServletRequest istek, HttpServletResponse yanit)
            throws IOException {
        if (sayfaIstegi(istek)) {
            yanit.sendError(HttpStatus.NOT_FOUND.value()); // → /error → static/error/404.html
            return null;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("hata", "Kaynak bulunamadı", "detay", e.getResourcePath()));
    }

    /**
     * Son çare. Spring'in kendi durum kodlu istisnaları (405, 415, eksik parametre…) kodunu korur;
     * gerisi 500. İstemciye istisna mesajı GİTMEZ (sunucu yolu/sağlayıcı gövdesi sızıyordu) — yalnız
     * sabit Türkçe metin + kısa referans; aynı referans log satırında, operatör eşleştirebilsin.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> beklenmeyen(Exception e, HttpServletRequest istek,
                                                           HttpServletResponse yanit) throws IOException {
        HttpStatusCode durum = e instanceof ErrorResponse er ? er.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR;
        String referans = UUID.randomUUID().toString().substring(0, 8);
        Map<String, String> govde = new LinkedHashMap<>();
        if (durum.is5xxServerError()) {
            log.error("Beklenmeyen hata [ref={}] {} {}", referans, istek.getMethod(), istek.getRequestURI(), e);
            govde.put("hata", "Beklenmeyen bir hata oluştu");
            govde.put("detay", BEKLENMEYEN_DETAY + " (Referans: " + referans + ")");
            govde.put("referans", referans);
        } else {
            log.debug("İstemci hatası [{}] {} {}: {}", durum.value(), istek.getMethod(), istek.getRequestURI(),
                    e.toString());
            govde.put("hata", "Geçersiz istek");
            govde.put("detay", "İstek işlenemedi (HTTP " + durum.value() + ") — sayfayı yenileyip tekrar dene.");
        }
        if (sayfaIstegi(istek)) {
            yanit.sendError(durum.value()); // tarayıcı: Türkçe hata sayfası (static/error/*.html)
            return null;
        }
        return ResponseEntity.status(durum).body(govde);
    }

    /** Tarayıcı sayfa isteği mi? API (/api/**) her zaman JSON alır; diğerlerinde Accept: text/html belirleyici. */
    private static boolean sayfaIstegi(HttpServletRequest istek) {
        String yol = istek.getRequestURI().substring(istek.getContextPath().length());
        if (yol.startsWith("/api/")) {
            return false;
        }
        String kabul = istek.getHeader("Accept");
        return kabul != null && kabul.contains("text/html");
    }
}
