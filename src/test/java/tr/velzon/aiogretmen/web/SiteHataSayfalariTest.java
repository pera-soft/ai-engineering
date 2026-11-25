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
 * Türkçe hata sayfaları (static/error/*.html) site profilinde, Spring Security zinciriyle GERÇEK
 * Tomcat'te: ziyaretçi olmayan sayfada 404 sayfası, bayat/eksik CSRF token'lı form gönderiminde 403
 * sayfası görür (Whitelabel değil); /api/** JSON kalır. Girişsiz site (ADR-0017): kimlik
 * {@code aio_ziyaretci} çereziyle.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:sitehatasayfa;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-site",
        "app.rapor-dir=target/test-raporlar-site",
        "app.site.guvenli-cerez=false"
})
@ActiveProfiles("site")
class SiteHataSayfalariTest {

    private static final String TARAYICI_KABUL = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    @LocalServerPort
    int port;

    private final HttpClient istemci = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    private URI uri(String yol) {
        return URI.create("http://127.0.0.1:" + port + yol);
    }

    private static String cerezDegeri(HttpResponse<?> yanit, String ad) {
        return yanit.headers().allValues("Set-Cookie").stream().filter(c -> c.startsWith(ad + "="))
                .findFirst().map(c -> c.substring(ad.length() + 1).split(";")[0])
                .orElseThrow(() -> new AssertionError(ad + " çerezi yok"));
    }

    /** Ana sayfayı açar (tarayıcı gibi); ziyaretçi + CSRF çerez başlığını ("aio_ziyaretci=...; XSRF-TOKEN=...") döner. */
    private String ziyaretciOl() throws Exception {
        HttpResponse<String> sayfa = istemci.send(HttpRequest.newBuilder(uri("/")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(sayfa.statusCode()).isEqualTo(200);
        return "aio_ziyaretci=" + cerezDegeri(sayfa, "aio_ziyaretci") + "; XSRF-TOKEN=" + cerezDegeri(sayfa, "XSRF-TOKEN");
    }

    @Test
    void ziyaretciOlmayanSayfadaTurkce404Gorur() throws Exception {
        String cerez = ziyaretciOl();

        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/boyle/bir-sayfa-yok"))
                .header("Accept", TARAYICI_KABUL).header("Cookie", cerez).build(), HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(404);
        assertThat(yanit.body()).contains("Sayfa bulunamadı").contains("href=\"/\"").doesNotContain("Whitelabel");

        // sayfanın stil dosyası (mutlak yol) yüklenebilir
        HttpResponse<String> stil = istemci.send(HttpRequest.newBuilder(uri("/style.css")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(stil.statusCode()).isEqualTo(200);
    }

    @Test
    void apiIstegiJson404Alir() throws Exception {
        String cerez = ziyaretciOl();

        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/api/boyle-bir-uc-yok"))
                .header("Cookie", cerez).build(), HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(404);
        assertThat(yanit.headers().firstValue("Content-Type")).hasValueSatisfying(t -> assertThat(t).startsWith("application/json"));
        assertThat(yanit.body()).contains("\"hata\"");
    }

    @Test
    void bayatCsrfTokenliFormTurkce403SayfasiGorur() throws Exception {
        // form eski sekmede kalmış: çerezdeki token ile formdaki uyuşmuyor → Spring Security 403
        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/api/sifirla"))
                .header("Accept", TARAYICI_KABUL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "XSRF-TOKEN=yeni-token")
                .POST(HttpRequest.BodyPublishers.ofString("_csrf=eski-token")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(403);
        assertThat(yanit.body()).contains("Bu işlem için yetkin yok").contains("yenile").contains("Ana sayfaya dön");
    }

    /**
     * Sitede anahtarsız sohbet (Faz 10, ADR-0017): geliştirici yönergesi (ortam değişkeni) yerine öğrenciye
     * yönelik "kendi anahtarını ekle" metni, 400.
     */
    @Test
    void sitedeAiKapaliMesajiOgrenciDilinde() throws Exception {
        String cerez = ziyaretciOl();
        String token = cerez.substring(cerez.indexOf("XSRF-TOKEN=") + "XSRF-TOKEN=".length());

        HttpResponse<String> yanit = istemci.send(HttpRequest.newBuilder(uri("/api/sohbet"))
                .header("Cookie", cerez).header("X-XSRF-TOKEN", token).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"hafta\":1,\"mesaj\":\"selam\"}")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(yanit.statusCode()).isEqualTo(400);
        assertThat(yanit.body()).contains("kendi OpenRouter anahtarını eklemelisin").doesNotContain("OPENROUTER_API_KEY");
    }
}
