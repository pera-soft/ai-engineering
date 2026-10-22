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
 * @param kayit            kayıt yöntemi (ADR-0015): {@code ekip-kodu} (varsayılan; e-posta GÖNDERİLMEZ, hesap ortak
 *                         ekip koduyla doğrulanmış açılır) | {@code eposta} (Faz 9c akışı: SMTP + doğrulama bağlantısı).
 * @param ekipKodu         {@code ekip-kodu} modunda kayıtta istenen ortak kod ({@code APP_SITE_EKIP_KODU}); en az
 *                         {@value #MIN_EKIP_KODU} karakter, yoksa site AÇILMAZ. Sırdır: loglanmaz, {@link #toString()}
 *                         onu maskeler.
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteAyarlari(@DefaultValue List<String> izinliOriginler,
                           @DefaultValue("true") boolean guvenliCerez,
                           @DefaultValue("http://127.0.0.1:8090") String temelUrl,
                           @DefaultValue("true") boolean epostaZorunlu,
                           @DefaultValue("AI Öğretmen <noreply@pera.software>") String gonderici,
                           @DefaultValue("ekip-kodu") KayitYontemi kayit,
                           String ekipKodu) {

    /** Ekip kodunun en kısa uzunluğu (kaba kuvvete karşı; üretim önerisi {@code openssl rand -base64 18}). */
    public static final int MIN_EKIP_KODU = 12;

    /** Kayıt yöntemi; ayarda küçük harf-tire yazılır ({@code ekip-kodu}, {@code eposta}). */
    public enum KayitYontemi {
        EKIP_KODU, EPOSTA
    }

    /** E-postalı kayıt akışı (SMTP, doğrulama, "şifremi unuttum") açık mı? */
    public boolean epostaKaydi() {
        return kayit == KayitYontemi.EPOSTA;
    }

    /** Ekip kodu sır olduğundan maskelenir (kayıt bileşenlerinin varsayılan toString'i değeri yazardı). */
    @Override
    public String toString() {
        return "SiteAyarlari[izinliOriginler=" + izinliOriginler + ", guvenliCerez=" + guvenliCerez
                + ", temelUrl=" + temelUrl + ", epostaZorunlu=" + epostaZorunlu + ", gonderici=" + gonderici
                + ", kayit=" + kayit + ", ekipKodu=" + (ekipKodu == null || ekipKodu.isBlank() ? "(yok)" : "***") + "]";
    }
}
