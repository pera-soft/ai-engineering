package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 9a (ADR-0012): çerez nitelikleri GERÇEK Tomcat üzerinde (MockMvc nitelikleri tam yansıtmaz).
 * Oturum çerezi HttpOnly + Secure + SameSite=Lax; XSRF-TOKEN çerezi Secure + SameSite=Lax ama
 * HttpOnly DEĞİL (statik ön yüz okuyup başlığa koyar). Yönlendirme izlenmez (çerezler 302'de).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sitecerez;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-site",
        "app.rapor-dir=target/test-raporlar-site",
        "app.site.eposta-zorunlu=false"  // SMTP yok → log gönderici (ADR-0013)
})
@ActiveProfiles("site")
class SiteCerezTest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder sifreKodlayici;

    private final HttpClient istemci = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    private static String cerez(HttpResponse<?> yanit, String ad) {
        List<String> hepsi = yanit.headers().allValues("Set-Cookie");
        return hepsi.stream().filter(c -> c.startsWith(ad + "=")).findFirst()
                .orElseThrow(() -> new AssertionError(ad + " çerezi yok: " + hepsi));
    }

    @Test
    void cerezlerGuvenliNiteliklerle() throws Exception {
        jdbc.update("INSERT INTO kullanici (email, sifre_hash, dogrulandi) VALUES (?, ?, TRUE)",
                "cerez@pera.software", sifreKodlayici.encode("cok-gizli-sifre-123"));

        HttpResponse<String> sayfa = istemci.send(HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + "/giris.html")).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(sayfa.statusCode()).isEqualTo(200);
        String xsrf = cerez(sayfa, "XSRF-TOKEN");
        assertThat(xsrf).contains("Secure").containsIgnoringCase("SameSite=Lax").doesNotContainIgnoringCase("HttpOnly");
        String token = xsrf.substring("XSRF-TOKEN=".length()).split(";")[0];

        String govde = "email=cerez%40pera.software&sifre=" + URLEncoder.encode("cok-gizli-sifre-123", StandardCharsets.UTF_8)
                + "&_csrf=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        HttpResponse<String> giris = istemci.send(HttpRequest.newBuilder(
                        URI.create("http://127.0.0.1:" + port + "/api/kimlik/giris"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "XSRF-TOKEN=" + token)
                .POST(HttpRequest.BodyPublishers.ofString(govde)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(giris.statusCode()).isEqualTo(302);
        assertThat(giris.headers().firstValue("Location")).hasValueSatisfying(l -> assertThat(l).endsWith("/"));
        assertThat(cerez(giris, "JSESSIONID")).contains("HttpOnly").contains("Secure")
                .containsIgnoringCase("SameSite=Lax");
    }

    @Test
    void cerezdekiCsrfTokeniBaslikOlarakGonderilinceKayitGecer() throws Exception {
        // ön yüz deseni (kimlik.js / app.js): XSRF-TOKEN çerezi okunur → X-XSRF-TOKEN başlığı
        HttpResponse<String> sayfa = istemci.send(HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + "/kayit.html")).build(), HttpResponse.BodyHandlers.ofString());
        String token = cerez(sayfa, "XSRF-TOKEN").substring("XSRF-TOKEN=".length()).split(";")[0];

        assertThat(kayit("baslik@pera.software", token, token).statusCode()).isEqualTo(200);
        assertThat(kayit("sahte@pera.software", token, "sahte-token").statusCode()).isEqualTo(403);
        assertThat(kayit("eksik@pera.software", token, null).statusCode()).isEqualTo(403);
    }

    private HttpResponse<String> kayit(String email, String cerezToken, String baslikToken) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/kimlik/kayit"))
                .header("Content-Type", "application/json")
                .header("Cookie", "XSRF-TOKEN=" + cerezToken)
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"email\":\"" + email + "\",\"sifre\":\"cok-gizli-sifre-123\"}"));
        if (baslikToken != null) {
            b.header("X-XSRF-TOKEN", baslikToken);
        }
        return istemci.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }
}
