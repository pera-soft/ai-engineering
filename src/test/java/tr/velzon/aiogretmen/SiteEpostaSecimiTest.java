package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import tr.velzon.aiogretmen.service.EpostaGonderici;
import tr.velzon.aiogretmen.service.LogEpostaGonderici;
import tr.velzon.aiogretmen.service.SmtpEpostaGonderici;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Faz 9c (ADR-0013 §3): {@code site} profilinde gönderici seçimi ve "e-posta zorunlu" açılış kilidi.
 * Gerçek SMTP'ye bağlanılmaz ({@code JavaMailSender} yalnız gönderimde bağlanır; burada gönderim yok).
 */
class SiteEpostaSecimiTest {

    private static ConfigurableApplicationContext ac(String profil, String db, String... ek) {
        List<String> arg = new ArrayList<>(List.of("--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:" + db + ";DB_CLOSE_DELAY=-1",
                "--spring.ai.openai.api-key=",
                "--app.site.kayit=eposta",
                "--app.hafiza-dir=target/test-hafiza-epostasecim",
                "--app.rapor-dir=target/test-raporlar-epostasecim"));
        arg.addAll(List.of(ek));
        SpringApplicationBuilder b = new SpringApplicationBuilder(AiOgretmenApplication.class);
        if (profil != null) {
            b.profiles(profil);
        }
        return b.run(arg.toArray(String[]::new));
    }

    @Test
    void siteSmtpsizVeZorunluysaAcilmaz() {
        // application-site.yml varsayılanı: app.site.eposta-zorunlu=true
        assertThatThrownBy(() -> ac("site", "epostazorunlu").close())
                .hasStackTraceContaining("SMTP");
    }

    @Test
    void siteSmtpVarsaSmtpGondericiSecilir() {
        try (ConfigurableApplicationContext c = ac("site", "epostasmtp",
                "--spring.mail.host=smtp.ornek.invalid", "--spring.mail.username=noreply@pera.software",
                "--spring.mail.password=gizli")) {
            assertThat(c.getBean(EpostaGonderici.class)).isInstanceOf(SmtpEpostaGonderici.class);
        }
    }

    @Test
    void siteZorunluDegilseLogGondericiyleAcilir() {
        try (ConfigurableApplicationContext c = ac("site", "epostalog", "--app.site.eposta-zorunlu=false")) {
            assertThat(c.getBean(EpostaGonderici.class)).isInstanceOf(LogEpostaGonderici.class);
        }
    }

    @Test
    void yerelModEtkilenmez() {
        try (ConfigurableApplicationContext c = ac(null, "epostayerel")) {
            assertThat(c.getBeansOfType(EpostaGonderici.class)).isEmpty();
        }
    }
}
