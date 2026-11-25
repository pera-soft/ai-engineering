package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 9d (deploy/nginx-site.conf.ornek): nginx TLS'i sonlandırıp uygulamaya http ile gelir ve
 * {@code X-Forwarded-Proto/Host} başlıklarını ekler. {@code server.forward-headers-strategy: native}
 * (Tomcat RemoteIpValve, yalnız 127.0.0.1 gibi iç vekillere güvenir) bunları onurlandırmalı: istek güvenli
 * sayılır → çerezler — {@code guvenli-cerez} kapalı olsa bile — {@code Secure} alır. GERÇEK Tomcat (MockMvc
 * valve'leri çalıştırmaz, çerez niteliklerini tam yansıtmaz).
 *
 * <p>Girişsiz site (ADR-0017): yönlendirme yok; ziyaretçi çerezi {@code HttpOnly}, XSRF çerezi DEĞİL (statik ön yüz
 * okuyup başlığa koyar); ikisi de {@code SameSite=Lax}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sitevekil;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-site",
        "app.rapor-dir=target/test-raporlar-site",
        "app.site.guvenli-cerez=false"
})
@ActiveProfiles("site")
class SiteVekilBasliklariTest {

    @LocalServerPort
    int port;

    private final HttpClient istemci = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    private HttpResponse<String> anaSayfa(boolean vekilArkasinda) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"));
        if (vekilArkasinda) {
            b.header("X-Forwarded-Proto", "https")
                    .header("X-Forwarded-Host", "egitim.ornek.com")
                    .header("X-Forwarded-Port", "443")
                    .header("X-Forwarded-For", "10.20.30.40");
        }
        return istemci.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cerez(HttpResponse<?> yanit, String ad) {
        return yanit.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith(ad + "="))
                .findFirst().orElseThrow(() -> new AssertionError(ad + " yok: " + yanit.headers()));
    }

    @Test
    void anaSayfaYonlendirmesizAcilir() throws Exception {
        assertThat(anaSayfa(true).statusCode()).isEqualTo(200);
        assertThat(anaSayfa(false).statusCode()).isEqualTo(200);
    }

    @Test
    void vekilArkasindaCerezlerSecureOlur() throws Exception {
        // guvenli-cerez=false olsa da https istek (X-Forwarded-Proto) Secure çerez almalı;
        // aksi halde canlıda ayar unutulursa çerezler düz kalırdı
        HttpResponse<String> vekil = anaSayfa(true);
        HttpResponse<String> dogrudan = anaSayfa(false);
        for (String ad : new String[]{"XSRF-TOKEN", "aio_ziyaretci"}) {
            assertThat(cerez(vekil, ad)).contains("Secure");
            assertThat(cerez(dogrudan, ad)).doesNotContain("Secure");
        }
    }

    @Test
    void cerezNitelikleri() throws Exception {
        HttpResponse<String> yanit = anaSayfa(true);
        assertThat(cerez(yanit, "aio_ziyaretci")).contains("HttpOnly").containsIgnoringCase("SameSite=Lax")
                .contains("Max-Age=31536000").contains("Path=/");
        assertThat(cerez(yanit, "XSRF-TOKEN")).containsIgnoringCase("SameSite=Lax").doesNotContainIgnoringCase("HttpOnly");
        assertThat(yanit.headers().allValues("Set-Cookie")).noneMatch(c -> c.startsWith("JSESSIONID="));
    }
}
