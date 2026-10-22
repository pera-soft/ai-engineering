package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Faz 9d (ADR-0014 §2): {@code site} profili veritabanını YALNIZ açıkça verilen URL'den alır —
 * verilmezse açılmaz (canlı sessizce yerel H2 dosyasına düşmesin); yerel mod H2 varsayılanıyla kalır.
 */
class SiteVeritabaniTest {

    private static ConfigurableApplicationContext ac(String profil, String... ek) {
        List<String> arg = new ArrayList<>(List.of("--server.port=0",
                "--spring.ai.openai.api-key=",
                "--app.site.eposta-zorunlu=false", "--app.site.kayit=eposta",
                "--app.hafiza-dir=target/test-hafiza-sitedb",
                "--app.rapor-dir=target/test-raporlar-sitedb"));
        arg.addAll(List.of(ek));
        SpringApplicationBuilder b = new SpringApplicationBuilder(AiOgretmenApplication.class);
        if (profil != null) {
            b.profiles(profil);
        }
        return b.run(arg.toArray(String[]::new));
    }

    @Test
    void siteVeritabaniUrlsizAcilmaz() {
        assertThatThrownBy(() -> ac("site").close())
                .hasStackTraceContaining("SPRING_DATASOURCE_URL");
    }

    @Test
    void siteAcikH2UrlIleAcilirSurucuUrldenCikarilir() {
        try (ConfigurableApplicationContext c = ac("site",
                "--spring.datasource.url=jdbc:h2:mem:sitedbacik;DB_CLOSE_DELAY=-1")) {
            assertThat(c.getBean(JdbcTemplate.class).queryForObject("SELECT COUNT(*) FROM kullanici", Integer.class))
                    .isZero();
        }
    }

    @Test
    void yerelModH2SurucusunuKorur() {
        try (ConfigurableApplicationContext c = ac(null,
                "--spring.datasource.url=jdbc:h2:mem:sitedbyerel;DB_CLOSE_DELAY=-1")) {
            assertThat(c.getEnvironment().getProperty("spring.datasource.driver-class-name"))
                    .isEqualTo("org.h2.Driver");
        }
    }
}
