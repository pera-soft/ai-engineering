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
 * @param hiz             hız sınırları ({@code app.site.hiz.*}; ortamda {@code APP_SITE_HIZ_*}) — pencereler sabit.
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteAyarlari(@DefaultValue List<String> izinliOriginler,
                           @DefaultValue("true") boolean guvenliCerez,
                           @DefaultValue Hiz hiz) {

    /**
     * Ayarlanabilir hız sınırları (son inceleme #4) — tek NAT arkasındaki sınıf/ofis için yükseltilebilir.
     * Varsayılanların TEK kaynağı buradaki {@code @DefaultValue}'lardır: {@code application-site.yml}
     * placeholder'ları ({@code ${APP_SITE_HIZ_…:}}) ortam değişkeni yoksa boş verir, boş değer buraya düşer
     * ({@code Integer}: boş → null → varsayılan; {@code int} boşu bağlayamazdı).
     *
     * @param yazmaDakika        yazan istek ({@code POST/PUT/DELETE /api/**}) / dakika / IP
     * @param sohbetDakika       sohbet mesajı / dakika / ziyaretçi
     * @param geriYukleSaat      ilerleme kodu geri yükleme / saat / IP
     * @param yeniZiyaretciSaat  yeni ziyaretçi satırı / saat / IP
     */
    public record Hiz(@DefaultValue("60") Integer yazmaDakika,
                      @DefaultValue("20") Integer sohbetDakika,
                      @DefaultValue("10") Integer geriYukleSaat,
                      @DefaultValue("20") Integer yeniZiyaretciSaat) {

        public Hiz {
            for (Integer deger : new Integer[]{yazmaDakika, sohbetDakika, geriYukleSaat, yeniZiyaretciSaat}) {
                if (deger == null || deger < 1) {
                    throw new IllegalArgumentException("app.site.hiz.* sınırları en az 1 olmalı");
                }
            }
        }
    }
}
