package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tr.velzon.aiogretmen.config.SiteAyarlari;
import tr.velzon.aiogretmen.service.EpostaGonderici;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ADR-0015 — ekip kodu modunun açılış kuralları: kod yok/kısa → site AÇILMAZ (kod değeri mesaja sızmaz);
 * bilinmeyen kayıt yöntemi açılmaz; SMTP yokken ve {@code eposta-zorunlu=true} (varsayılan) iken bile açılır.
 */
class SiteEkipKoduAcilisTest {

    private static ConfigurableApplicationContext ac(String db, String... ek) {
        List<String> arg = new ArrayList<>(List.of("--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:" + db + ";DB_CLOSE_DELAY=-1",
                "--spring.ai.openai.api-key=",
                "--app.hafiza-dir=target/test-hafiza-ekipacilis",
                "--app.rapor-dir=target/test-raporlar-ekipacilis"));
        arg.addAll(List.of(ek));
        return new SpringApplicationBuilder(AiOgretmenApplication.class).profiles("site")
                .run(arg.toArray(String[]::new));
    }

    @Test
    void kodsuzAcilmaz() {
        assertThatThrownBy(() -> ac("ekipkodsuz").close())
                .hasStackTraceContaining("APP_SITE_EKIP_KODU");
        assertThatThrownBy(() -> ac("ekipbos", "--app.site.ekip-kodu=   ").close())
                .hasStackTraceContaining("APP_SITE_EKIP_KODU");
    }

    @Test
    void kisaKodlaAcilmazKodMesajaSizmaz() {
        String kisa = "kisa-kod-11"; // 11 karakter
        assertThatThrownBy(() -> ac("ekipkisa", "--app.site.ekip-kodu=" + kisa).close())
                .hasStackTraceContaining("en az 12 karakter")
                .satisfies(h -> {
                    for (Throwable t = h; t != null; t = t.getCause()) {
                        assertThat(String.valueOf(t.getMessage())).doesNotContain(kisa);
                    }
                });
    }

    @Test
    void bilinmeyenKayitYontemiAcilmaz() {
        assertThatThrownBy(() -> ac("ekipbilinmeyen", "--app.site.kayit=sms",
                "--app.site.ekip-kodu=yeterince-uzun-kod").close())
                .hasStackTraceContaining("app.site.kayit");
    }

    @Test
    void smtpsizVeEpostaZorunluyaRagmenAcilirKodMaskelenir() {
        String kod = "yeterince-uzun-kod-12";
        try (ConfigurableApplicationContext c = ac("ekipacik", "--app.site.ekip-kodu=" + kod)) {
            assertThat(c.getBean(EpostaGonderici.class).getClass().getSimpleName())
                    .isEqualTo("KapaliEpostaGonderici");
            SiteAyarlari ayarlar = c.getBean(SiteAyarlari.class);
            assertThat(ayarlar.epostaKaydi()).isFalse();
            assertThat(ayarlar.toString()).doesNotContain(kod).contains("ekipKodu=***");
        }
    }

    @Test
    void epostaModundaKodGerekmez() {
        try (ConfigurableApplicationContext c = ac("ekipeposta", "--app.site.kayit=eposta",
                "--app.site.eposta-zorunlu=false")) {
            assertThat(c.getBean(SiteAyarlari.class).epostaKaydi()).isTrue();
        }
    }
}
