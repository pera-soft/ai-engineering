package tr.velzon.aiogretmen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Çok kullanıcılı {@code site} profili ayarları (Faz 9a, ADR-0012) — {@code app.site.*}.
 *
 * @param izinliOriginler  nginx arkasındaki genel adres(ler) (ör. {@code https://egitim.pera.software});
 *                         {@code web/CsrfKoruma} yerel origin'e EK olarak bunları kabul eder. Boş = yalnız yerel.
 * @param guvenliCerez     oturum + CSRF çerezlerinde {@code Secure}; yalnız yerel http denemesinde {@code false}.
 * @param temelUrl         e-postadaki bağlantıların kökü (ör. {@code https://egitim.pera.software}). İstek
 *                         Host başlığından TÜRETİLMEZ (Host zehirlemesiyle sahte bağlantı üretilmesin).
 * @param epostaZorunlu    {@code true} iken SMTP ({@code spring.mail.host}) yoksa {@code site} AÇILMAZ — canlı
 *                         sessizce gizli bağlantıları log'a yazmasın (Faz 9c, ADR-0013). Yerel denemede {@code false}.
 * @param gonderici        e-postaların "Kimden" adresi, ad dahil (ör. {@code AI Öğretmen <noreply@pera.software>}).
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteAyarlari(@DefaultValue List<String> izinliOriginler,
                           @DefaultValue("true") boolean guvenliCerez,
                           @DefaultValue("http://127.0.0.1:8090") String temelUrl,
                           @DefaultValue("true") boolean epostaZorunlu,
                           @DefaultValue("AI Öğretmen <noreply@pera.software>") String gonderici) {
}
