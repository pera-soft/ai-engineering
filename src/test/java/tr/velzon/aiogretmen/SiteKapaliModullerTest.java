package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import tr.velzon.aiogretmen.ai.AnlamArama;
import tr.velzon.aiogretmen.repo.MulakatRepo;
import tr.velzon.aiogretmen.service.MulakatService;
import tr.velzon.aiogretmen.web.KitapController;
import tr.velzon.aiogretmen.web.MulakatController;
import tr.velzon.aiogretmen.web.OzellikController;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Faz 9b: {@code site} profilinde ortak/tek-kişilik modüller ZORLA kapalı — bayrak/ortam değişkeni
 * açık olsa bile: mülakat (ADR-0011), kitap notu uçları + ortak vektör deposu, MCP sunucusu.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitekapali;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=sahte-anahtar-ag-cagrisi-yok", // anahtar VARKEN bile vektör deposu kapalı
        "app.mulakat.etkin=true",   // birisi APP_MULAKAT_ETKIN=true verse bile
        "app.hafiza-dir=target/test-hafiza-sitekapali",
        "app.rapor-dir=target/test-raporlar-sitekapali",
        "app.site.eposta-zorunlu=false",  // SMTP yok → log gönderici (ADR-0013)
        "app.site.kayit=eposta"
})
@ActiveProfiles("site")
class SiteKapaliModullerTest {

    @Autowired
    ApplicationContext ctx;

    @Autowired
    OzellikController ozellikController;

    @Autowired
    AnlamArama anlamArama;

    @Test
    void mulakatBayragiAcikOlsaBileSitedeKapali() {
        assertThat(ctx.getBeansOfType(MulakatController.class)).isEmpty();
        assertThat(ctx.getBeansOfType(MulakatService.class)).isEmpty();
        assertThat(ctx.getBeansOfType(MulakatRepo.class)).isEmpty();
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated("x@pera.software", null, List.of()));
            assertThat(ozellikController.ozellikler()).containsEntry("mulakat", false);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void kitapUclariVeOrtakVektorDeposuSitedeYok() {
        assertThat(ctx.getBeansOfType(KitapController.class)).isEmpty();
        assertThat(anlamArama.aktif()).isFalse();
    }

    @Test
    void mcpSunucusuSiteIleAcilamaz() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(AiOgretmenApplication.class)
                .profiles("site")
                // komut satırı argümanı: application.yml'deki enabled=false'u EZER (ortam değişkeni gibi)
                .run("--server.port=0",
                        "--spring.datasource.url=jdbc:h2:mem:sitemcp;DB_CLOSE_DELAY=-1",
                        "--spring.ai.openai.api-key=",
                        "--app.site.eposta-zorunlu=false", "--app.site.kayit=eposta",
                        "--spring.ai.mcp.server.enabled=true").close())
                .hasStackTraceContaining("MCP");
    }
}
