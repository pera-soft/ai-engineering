package tr.velzon.aiogretmen.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import tr.velzon.aiogretmen.types.AiSaglayiciException;
import tr.velzon.aiogretmen.types.AiSaglayiciException.Neden;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring AI / OpenRouter hatalarını {@link AiSaglayiciException}'a çeviren TEK nokta.
 *
 * <p>Spring AI 1.1.8'in gerçekte fırlattıkları (spring-ai-retry {@code RetryUtils}, OpenAiApi'nin
 * varsayılan {@code ResponseErrorHandler}'ı):
 * <ul>
 *   <li>HTTP 4xx → {@link NonTransientAiException}, mesaj {@code "<kod> - <ham gövde>"} (yeniden denenmez)</li>
 *   <li>HTTP 5xx → {@link TransientAiException}, aynı biçim (varsayılan RetryTemplate yeniden dener,
 *       tükenince sonuncuyu fırlatır)</li>
 *   <li>bağlantı/okuma zaman aşımı → {@link ResourceAccessException} (o da yeniden denenir)</li>
 *   <li>özel hata işleyici olmadan RestClient → {@link RestClientResponseException} (savunma amaçlı)</li>
 * </ul>
 * Ham gövde ve anahtar kullanıcıya ve loga GİTMEZ; log satırı yalnız kod + kategori taşır.
 */
final class AiHataCevirici {

    private static final Logger log = LoggerFactory.getLogger(AiHataCevirici.class);

    /** "401 - {...}" → 401 */
    private static final Pattern KOD_ONEKI = Pattern.compile("^\\s*(\\d{3})\\s*-");

    private AiHataCevirici() {
    }

    /**
     * @return tanınan bir sağlayıcı hatasıysa {@link AiSaglayiciException}; değilse {@code hata}nın kendisi
     */
    static RuntimeException cevir(RuntimeException hata, String islem) {
        AiSaglayiciException cevrilen = bul(hata);
        if (cevrilen == null) {
            return hata;
        }
        log.warn("AI sağlayıcı hatası: islem={} kod={} neden={} tur={}", islem,
                cevrilen.saglayiciKodu() == null ? "-" : cevrilen.saglayiciKodu(),
                cevrilen.neden(), hata.getClass().getSimpleName());
        return cevrilen;
    }

    private static AiSaglayiciException bul(Throwable hata) {
        // sarmalanmış olabilir (ör. RuntimeException(cause)) — zincirde ilk tanıdığımızı ara
        for (Throwable t = hata; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof AiSaglayiciException zaten) {
                return zaten;
            }
            if (t instanceof NonTransientAiException) {
                Integer kod = kodOku(t.getMessage());
                return new AiSaglayiciException(kod == null ? Neden.ISTEK_REDDEDILDI : kodaGore(kod), kod, hata);
            }
            if (t instanceof TransientAiException) {
                Integer kod = kodOku(t.getMessage());
                return new AiSaglayiciException(kod == null ? Neden.YANIT_YOK : kodaGore(kod), kod, hata);
            }
            if (t instanceof RestClientResponseException r) {
                int kod = r.getStatusCode().value();
                return new AiSaglayiciException(kodaGore(kod), kod, hata);
            }
            if (t instanceof ResourceAccessException) {
                return new AiSaglayiciException(Neden.YANIT_YOK, null, hata);
            }
        }
        return null;
    }

    static Neden kodaGore(int kod) {
        return switch (kod) {
            case 401, 403 -> Neden.ANAHTAR_GECERSIZ;
            case 402 -> Neden.KREDI_BITTI;
            case 429 -> Neden.YOGUN;
            case 408 -> Neden.YANIT_YOK;
            default -> kod >= 500 ? Neden.YANIT_YOK : Neden.ISTEK_REDDEDILDI;
        };
    }

    private static Integer kodOku(String mesaj) {
        if (mesaj == null) {
            return null;
        }
        Matcher m = KOD_ONEKI.matcher(mesaj);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }
}
