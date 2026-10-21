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
}
