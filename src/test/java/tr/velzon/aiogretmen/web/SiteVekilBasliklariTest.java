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
 * (Tomcat RemoteIpValve, yalnız 127.0.0.1 gibi iç vekillere güvenir) bunları onurlandırmalı: giriş
 * yönlendirmesi {@code https://<alan-adı>/...} olur ve oturum çerezi — ayar kapalı olsa bile — istek
 * güvenli sayıldığı için Secure alır. GERÇEK Tomcat (MockMvc valve'leri çalıştırmaz).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sitevekil;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-site",
        "app.rapor-dir=target/test-raporlar-site",
        "app.site.eposta-zorunlu=false",
        "app.site.kayit=eposta",
        "app.site.guvenli-cerez=false",
        "server.servlet.session.cookie.secure=false"
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
                    .header("X-Forwarded-Host", "egitim.pera.software")
                    .header("X-Forwarded-Port", "443")
                    .header("X-Forwarded-For", "10.20.30.40");
        }
        return istemci.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void vekilBasliklariylaYonlendirmeHttpsVeAlanAdiKullanir() throws Exception {
        HttpResponse<String> yanit = anaSayfa(true);
        assertThat(yanit.statusCode()).isEqualTo(302);
        assertThat(yanit.headers().firstValue("Location"))
                .hasValue("https://egitim.pera.software/giris.html");
    }

    @Test
    void vekilsizDogrudanHttpIstegiHttpKalir() throws Exception {
        HttpResponse<String> yanit = anaSayfa(false);
        assertThat(yanit.statusCode()).isEqualTo(302);
        assertThat(yanit.headers().firstValue("Location")).hasValueSatisfying(l ->
                assertThat(l).startsWith("http://127.0.0.1:" + port));
    }

    @Test
    void vekilArkasindaXsrfCereziSecureOlur() throws Exception {
        // guvenli-cerez=false olsa da https istek (X-Forwarded-Proto) Secure çerez almalı;
        // aksi halde canlıda ayar unutulursa çerezler düz kalırdı
        String vekil = cerez(anaSayfaGiris(true));
        String dogrudan = cerez(anaSayfaGiris(false));
        assertThat(vekil).contains("Secure");
        assertThat(dogrudan).doesNotContain("Secure");
    }

    private HttpResponse<String> anaSayfaGiris(boolean vekilArkasinda) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/giris.html"));
        if (vekilArkasinda) {
            b.header("X-Forwarded-Proto", "https").header("X-Forwarded-Host", "egitim.pera.software")
                    .header("X-Forwarded-Port", "443");
        }
        return istemci.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cerez(HttpResponse<?> yanit) {
        return yanit.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith("XSRF-TOKEN="))
                .findFirst().orElseThrow(() -> new AssertionError("XSRF-TOKEN yok: " + yanit.headers()));
    }
}
