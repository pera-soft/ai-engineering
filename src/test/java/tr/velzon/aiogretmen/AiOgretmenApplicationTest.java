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
}
