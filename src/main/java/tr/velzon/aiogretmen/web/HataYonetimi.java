package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
import tr.velzon.aiogretmen.types.AnahtarBicimiException;
import tr.velzon.aiogretmen.types.AnahtarGerekliException;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.HizSiniriAsildiException;
import tr.velzon.aiogretmen.types.IlerlemeKoduBulunamadiException;

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
    @ExceptionHandler(BulunamadiException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> bulunamadi(BulunamadiException e) {
        return Map.of("hata", "Kayıt bulunamadı", "detay", e.getMessage());
    }

    /** Biçimce geçerli ama kayıtsız ilerleme kodu (Faz 10, ADR-0017): istek doğru, veritabanı durumu reddediyor. */
    @ExceptionHandler(IlerlemeKoduBulunamadiException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> ilerlemeKoduBulunamadi(IlerlemeKoduBulunamadiException e) {
        return Map.of("hata", e.getMessage());
    }

    /** Girişsiz sitede IP/ziyaretçi hız sınırı aşıldı (Faz 10, ADR-0017). */
    @ExceptionHandler(HizSiniriAsildiException.class)
    public ResponseEntity<Map<String, String>> hizSiniriAsildi(HizSiniriAsildiException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(e.beklemeSaniye()))
                .body(Map.of("hata", e.getMessage()));
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

    /**
     * Anahtarsız yerel açılış (geliştirici yönergesi). Sitede oluşmaz: site AI istemcisi sunucu anahtarı
     * kullanmaz, ziyaretçi anahtarı yoksa {@link AnahtarGerekliException} (400) fırlatır (Faz 10, ADR-0017).
     */
    @ExceptionHandler(AiDevreDisiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> aiDevreDisi(AiDevreDisiException e) {
        return Map.of("hata", "AI özellikleri devre dışı", "detay", e.getMessage());
    }

    /**
     * Sitede ziyaretçinin kendi anahtarı yok (Faz 10, ADR-0017) — sabit Türkçe metin, anahtar içermez.
     * {@code neden} kararlıdır: ön yüz metne değil koda göre dallanır (anahtar kartını yeniden açar).
     */
    @ExceptionHandler(AnahtarGerekliException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> anahtarGerekli(AnahtarGerekliException e) {
        Map<String, String> govde = new LinkedHashMap<>();
        govde.put("hata", e.getMessage());
        govde.put("neden", "ANAHTAR_GEREKLI");
        return govde;
    }

    /** {@code X-OpenRouter-Anahtar} biçimsiz (Faz 10, ADR-0017) — mesaj anahtarı içermez; kararlı {@code neden}. */
    @ExceptionHandler(AnahtarBicimiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> anahtarBicimi(AnahtarBicimiException e) {
        Map<String, String> govde = new LinkedHashMap<>();
        govde.put("hata", "Geçersiz istek");
        govde.put("detay", e.getMessage());
        govde.put("neden", "ANAHTAR_BICIMI");
        return govde;
    }

    /**
     * Sağlayıcı (OpenRouter) hatası — ai/ katmanında kategoriye çevrildi (AiHataCevirici; log orada).
     * Sunucu anahtarıyla: hepsi 503 (servis şu an kullanılamıyor); yalnız sağlayıcının reddettiği istek 502.
     * Ziyaretçinin kendi anahtarıyla (site, Faz 10): anahtar geçersiz 401, kredi/limit 402, çok istek 429 —
     * ön yüz anahtar kartını yeniden açabilsin; diğerleri aynı.
     * Gövde: {@code {"hata": "AI servisi kullanılamıyor", "detay": <Türkçe mesaj>, "neden": <kategori>}}.
     */
    @ExceptionHandler(AiSaglayiciException.class)
    public ResponseEntity<Map<String, String>> aiSaglayiciHatasi(AiSaglayiciException e) {
        HttpStatus durum = e.neden() == AiSaglayiciException.Neden.ISTEK_REDDEDILDI
                ? HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
        if (e.ziyaretciAnahtari()) {
            durum = switch (e.neden()) {
                case ANAHTAR_GECERSIZ -> HttpStatus.UNAUTHORIZED;
                case KREDI_BITTI -> HttpStatus.PAYMENT_REQUIRED;
                case YOGUN -> HttpStatus.TOO_MANY_REQUESTS;
                default -> durum;
            };
        }
        Map<String, String> govde = new LinkedHashMap<>();
        govde.put("hata", "AI servisi kullanılamıyor");
        govde.put("detay", e.getMessage());
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
