package tr.velzon.aiogretmen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * {@code site} profili ayarları (Faz 10, ADR-0017) — {@code app.site.*}. Girişsiz site: kimlik/e-posta ayarı yok.
 *
 * @param izinliOriginler nginx arkasındaki genel adres(ler) (ör. {@code https://egitim.ornek.com});
 *                        {@code web/CsrfKoruma} yerel origin'e EK olarak bunları kabul eder. Boş = yalnız yerel.
 * @param guvenliCerez    ziyaretçi + CSRF çerezlerinde {@code Secure}; yalnız yerel http denemesinde {@code false}.
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteAyarlari(@DefaultValue List<String> izinliOriginler,
                           @DefaultValue("true") boolean guvenliCerez) {
}
