package tr.velzon.aiogretmen.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hız sınırı ayarlarının bağlanması (son inceleme kalıntıları #4–#5). Varsayılanların TEK kaynağı
 * {@link SiteAyarlari.Hiz}'daki {@code @DefaultValue}'lardır; {@code application-site.yml} placeholder'ları
 * ortam değişkeni yoksa BOŞ değer verir ve boş değer varsayılana düşer.
 */
class SiteAyarlariTest {

    @Configuration
    @EnableConfigurationProperties(SiteAyarlari.class)
    static class Yapi {
    }

    private final ApplicationContextRunner calistirici = new ApplicationContextRunner().withUserConfiguration(Yapi.class);

    @Test
    void ayarYoksaVarsayilanlar() {
        calistirici.run(b -> assertThat(b.getBean(SiteAyarlari.class).hiz())
                .isEqualTo(new SiteAyarlari.Hiz(60, 20, 10, 20)));
    }

    /** yml'deki {@code ${APP_SITE_HIZ_…:}} ortam değişkeni yoksa boş dize üretir → @DefaultValue. */
    @Test
    void bosDegerVarsayilanaDuser() {
        calistirici.withPropertyValues("app.site.hiz.yazma-dakika=", "app.site.hiz.sohbet-dakika=",
                        "app.site.hiz.geri-yukle-saat=", "app.site.hiz.yeni-ziyaretci-saat=7")
                .run(b -> assertThat(b.getBean(SiteAyarlari.class).hiz())
                        .isEqualTo(new SiteAyarlari.Hiz(60, 20, 10, 7)));
    }

    @Test
    void sifirSinirBaglamiAcmaz() {
        calistirici.withPropertyValues("app.site.hiz.yazma-dakika=0").run(b -> {
            assertThat(b).hasFailed();
            Throwable t = b.getStartupFailure();
            StringBuilder mesajlar = new StringBuilder();
            while (t != null) {
                mesajlar.append(t.getMessage()).append('\n');
                t = t.getCause();
            }
            assertThat(mesajlar.toString()).contains("en az 1");
        });
    }
}
