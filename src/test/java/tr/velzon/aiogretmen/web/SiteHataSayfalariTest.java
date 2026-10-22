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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Türkçe hata sayfaları (static/error/*.html) site profilinde, Spring Security zinciriyle GERÇEK
 * Tomcat'te: oturumlu kullanıcı olmayan sayfada 404 sayfası, bayat/eksik CSRF token'lı form
 * gönderiminde 403 sayfası görür (Whitelabel değil); /api/** JSON kalır.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sitehatasayfa;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-site",
        "app.rapor-dir=target/test-raporlar-site",
        "app.site.eposta-zorunlu=false",
        "app.site.kayit=eposta",
        "app.site.guvenli-cerez=false",
        "server.servlet.session.cookie.secure=false"
})
@ActiveProfiles("site")
class SiteHataSayfalariTest {

    private static final String TARAYICI_KABUL = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder sifreKodlayici;

    private final HttpClient istemci = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    private URI uri(String yol) {
        return URI.create("http://127.0.0.1:" + port + yol);
    }

    private static String cerezDegeri(HttpResponse<?> yanit, String ad) {
        return yanit.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith(ad + "="))
                .findFirst().map(c -> c.substring(ad.length() + 1).split(";")[0])
                .orElseThrow(() -> new AssertionError(ad + " çerezi yok"));
    }

    /** Giriş yapar; oturum çerezi başlığını ("JSESSIONID=...; XSRF-TOKEN=...") döner. */
    private String girisYap(String email) throws Exception {
        jdbc.update("INSERT INTO kullanici (email, sifre_hash, dogrulandi) VALUES (?, ?, TRUE)",
                email, sifreKodlayici.encode("cok-gizli-sifre-123"));
        HttpResponse<String> sayfa = istemci.send(HttpRequest.newBuilder(uri("/giris.html")).build(),
                HttpResponse.BodyHandlers.ofString());
        String token = cerezDegeri(sayfa, "XSRF-TOKEN");
        String govde = "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&sifre=cok-gizli-sifre-123&_csrf="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
        HttpResponse<String> giris = istemci.send(HttpRequest.newBuilder(uri("/api/kimlik/giris"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "XSRF-TOKEN=" + token)
                .POST(HttpRequest.BodyPublishers.ofString(govde)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(giris.statusCode()).isEqualTo(302);
        return "JSESSIONID=" + cerezDegeri(giris, "JSESSIONID") + "; XSRF-TOKEN=" + token;
    }

    @Test
    void oturumluKullaniciOlmayanSayfadaTurkce404Gorur() throws Exception {
        String cerez = girisYap("hata404@pera.software");

        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/boyle/bir-sayfa-yok"))
                .header("Accept", TARAYICI_KABUL).header("Cookie", cerez).build(), HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(404);
        assertThat(yanit.body()).contains("Sayfa bulunamadı").contains("href=\"/\"").doesNotContain("Whitelabel");

        // sayfanın stil dosyası (mutlak yol) aynı oturumla yüklenebilir
        HttpResponse<String> stil = istemci.send(HttpRequest.newBuilder(uri("/style.css")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(stil.statusCode()).isEqualTo(200);
    }

    @Test
    void oturumluApiIstegiJson404Alir() throws Exception {
        String cerez = girisYap("hataapi@pera.software");

        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/api/boyle-bir-uc-yok"))
                .header("Cookie", cerez).build(), HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(404);
        assertThat(yanit.headers().firstValue("Content-Type")).hasValueSatisfying(t -> assertThat(t).startsWith("application/json"));
        assertThat(yanit.body()).contains("\"hata\"");
    }

    @Test
    void bayatCsrfTokenliFormTurkce403SayfasiGorur() throws Exception {
        // giriş formu eski sekmede kalmış: çerezdeki token ile formdaki uyuşmuyor → Spring Security 403
        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/api/kimlik/giris"))
                .header("Accept", TARAYICI_KABUL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "XSRF-TOKEN=yeni-token")
                .POST(HttpRequest.BodyPublishers.ofString("email=a%40pera.software&sifre=x&_csrf=eski-token")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(403);
        assertThat(yanit.body()).contains("Bu işlem için yetkin yok").contains("yenile").contains("Ana sayfaya dön");
    }

    /** Sitede anahtarsız AI ucu: geliştirici yönergesi (ortam değişkeni) yerine öğrenciye yönelik metin. */
    @Test
    void sitedeAiKapaliMesajiOgrenciDilinde() throws Exception {
        String cerez = girisYap("aikapali@pera.software");
        String token = cerez.substring(cerez.indexOf("XSRF-TOKEN=") + "XSRF-TOKEN=".length());

        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/api/sohbet"))
                .header("Cookie", cerez).header("X-XSRF-TOKEN", token).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"hafta\":1,\"mesaj\":\"selam\"}")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(503);
        assertThat(yanit.body()).contains("AI öğretmen henüz açılmadı").doesNotContain("OPENROUTER_API_KEY");
    }
}
