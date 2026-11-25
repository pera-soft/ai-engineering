package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tr.velzon.aiogretmen.types.AnahtarBicimiException;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Girişsiz {@code site} profilinde ziyaretçinin kendi OpenRouter anahtarı (Faz 10, ADR-0017): o anki isteğin
 * {@value #BASLIK} başlığı. Biçim: {@code sk-or-} + 10–200 URL-güvenli karakter. Biçim hatası mesajı anahtarı
 * İÇERMEZ (400 gövdesine ve loglara gider).
 */
@Component
@Profile("site")
public class BaslikAiAnahtarBaglami implements AiAnahtarBaglami {

    /** Ön yüzün yalnız sohbet isteklerine eklediği başlık. */
    public static final String BASLIK = "X-OpenRouter-Anahtar";

    private static final Pattern BICIM = Pattern.compile("^sk-or-[A-Za-z0-9_-]{10,200}$");

    @Override
    public Optional<String> anahtar() {
        RequestAttributes ozn = RequestContextHolder.getRequestAttributes();
        if (!(ozn instanceof ServletRequestAttributes servlet)) {
            return Optional.empty();
        }
        String ham = servlet.getRequest().getHeader(BASLIK);
        if (ham == null || ham.isBlank()) {
            return Optional.empty();
        }
        String anahtar = ham.strip();
        if (!BICIM.matcher(anahtar).matches()) {
            throw new AnahtarBicimiException("OpenRouter anahtarının biçimi geçersiz — 'sk-or-' ile başlamalı.");
        }
        return Optional.of(anahtar);
    }
}
