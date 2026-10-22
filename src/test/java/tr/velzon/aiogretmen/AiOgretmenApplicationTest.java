package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uçtan uca duman testi (inceleme bulgusu: hiç @SpringBootTest yoktu — AiYapilandirma'nın
 * elle bean kurulumu ancak gerçek açılışta patlar). Anahtarsız profil: AI devre dışı yolu.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:baglamtest;DB_CLOSE_DELAY=-1",
                "spring.ai.openai.api-key=",
                "app.hafiza-dir=target/test-hafiza",
                "app.rapor-dir=target/test-raporlar"
        })
class AiOgretmenApplicationTest {

    @Autowired
    TestRestTemplate rest;

    /** Faz 9b: yerel modda kitap notu uçları DEĞİŞMEDİ (yalnız site profilinde kapalı). */
    @Test
    void yerelModdaKitapUclariAcik() {
        assertThat(rest.getForEntity("/api/kitaplar", String.class).getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void baglamYuklenirVeProgramDoner() {
        ResponseEntity<String> yanit = rest.getForEntity("/api/program", String.class);

        assertThat(yanit.getStatusCode().value()).isEqualTo(200);
        assertThat(yanit.getBody()).contains("Harness"); // gerçek content/ yüklendi
    }

    @Test
    void anahtarsizAiUcu503Doner() {
        HttpHeaders basliklar = new HttpHeaders();
        basliklar.add("Content-Type", "application/json");
        ResponseEntity<String> yanit = rest.postForEntity("/api/sohbet",
                new HttpEntity<>("{\"hafta\":1,\"mesaj\":\"selam\"}", basliklar), String.class);

        assertThat(yanit.getStatusCode().value()).isEqualTo(503);
        assertThat(yanit.getBody()).contains("hata");
    }

    @Test
    void csrfFiltresiGercekZincirdeCalisir() {
        HttpHeaders basliklar = new HttpHeaders();
        basliklar.add("Content-Type", "application/json");
        basliklar.add("Sec-Fetch-Site", "cross-site");
        ResponseEntity<String> yanit = rest.postForEntity("/api/gorev/toggle",
                new HttpEntity<>("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0}", basliklar), String.class);

        assertThat(yanit.getStatusCode().value()).isEqualTo(403); // filtre gerçekten kayıtlı
    }

    @Test
    void varsayilanModKimliksizVeSiteKapali() {
        // Faz 9a (ADR-0012): site profili OLMADAN davranış aynı — login yok, kayıt ucu yok
        ResponseEntity<String> ozellik = rest.getForEntity("/api/ozellikler", String.class);
        assertThat(ozellik.getStatusCode().value()).isEqualTo(200);
        assertThat(ozellik.getBody()).contains("\"site\":false").doesNotContain("kullanici");

        assertThat(rest.getForEntity("/", String.class).getStatusCode().value()).isEqualTo(200);

        HttpHeaders basliklar = new HttpHeaders();
        basliklar.add("Content-Type", "application/json");
        ResponseEntity<String> kayit = rest.postForEntity("/api/kimlik/kayit",
                new HttpEntity<>("{\"email\":\"x@pera.software\",\"sifre\":\"0123456789ab\"}", basliklar),
                String.class);
        assertThat(kayit.getStatusCode().value()).isEqualTo(404);
    }

    /** Ana sayfadan sohbet: hafta=null/0/13 eskiden 500 + sunucu yolu ("./content/hafta-00/hafta.md") dönüyordu. */
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"0", "-1", "13", "999"})
    void olmayanHaftayaSohbet404VeTurkceMesaj(String hafta) {
        ResponseEntity<String> yanit = jsonPost("/api/sohbet", "{\"hafta\":" + hafta + ",\"gun\":null,\"mesaj\":\"selam\"}");

        assertThat(yanit.getStatusCode().value()).isEqualTo(404);
        assertThat(yanit.getBody()).contains("Hafta bulunamadı: " + hafta).doesNotContain("content/").doesNotContain(".md");
    }

    @Test
    void haftasizSohbet400() {
        ResponseEntity<String> yanit = jsonPost("/api/sohbet", "{\"hafta\":null,\"gun\":null,\"mesaj\":\"selam\"}");
        assertThat(yanit.getStatusCode().value()).isEqualTo(400);
        assertThat(yanit.getBody()).contains("hafta alanı eksik");
    }

    @Test
    void olmayanHaftayaDegerlendirme404() {
        ResponseEntity<String> yanit = jsonPost("/api/degerlendir", "{\"hafta\":0,\"tur\":\"OZET\",\"icerik\":\"özet\"}");
        assertThat(yanit.getStatusCode().value()).isEqualTo(404);
    }

    /** Tarayıcıdan olmayan sayfa: Whitelabel yerine Türkçe hata sayfası (static/error/404.html). */
    @Test
    void htmlIstegineTurkce404Sayfasi() {
        HttpHeaders basliklar = new HttpHeaders();
        basliklar.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        ResponseEntity<String> yanit = rest.exchange("/boyle-bir-sayfa-yok/alt", org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(basliklar), String.class);

        assertThat(yanit.getStatusCode().value()).isEqualTo(404);
        assertThat(yanit.getHeaders().getContentType().toString()).startsWith("text/html");
        assertThat(yanit.getBody()).contains("Sayfa bulunamadı").contains("Ana sayfaya dön")
                .contains("href=\"/style.css\"").doesNotContain("Whitelabel");
    }

    /** API istemcileri JSON almaya devam eder (frontend {hata, detay} okur). */
    @Test
    void apiIstegineJson404() {
        ResponseEntity<String> yanit = rest.getForEntity("/api/boyle-bir-uc-yok", String.class);
        assertThat(yanit.getStatusCode().value()).isEqualTo(404);
        assertThat(yanit.getHeaders().getContentType().toString()).startsWith("application/json");
        assertThat(yanit.getBody()).contains("\"hata\"");
    }

    private ResponseEntity<String> jsonPost(String yol, String govde) {
        HttpHeaders basliklar = new HttpHeaders();
        basliklar.add("Content-Type", "application/json");
        return rest.postForEntity(yol, new HttpEntity<>(govde, basliklar), String.class);
    }
}
