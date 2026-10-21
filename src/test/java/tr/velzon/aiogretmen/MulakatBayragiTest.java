package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.MulakatRepo;
import tr.velzon.aiogretmen.service.MulakatAciklamaService;
import tr.velzon.aiogretmen.service.MulakatService;
import tr.velzon.aiogretmen.web.MulakatController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ADR-0011: Mülakat modülü {@code app.mulakat.etkin} bayrağıyla açılır; varsayılan KAPALI.
 * Kapalıyken bean'ler yok, uçlar 404, arayüz bayrağı false; sıfırlama yine çalışır.
 * Açıkken modül eskisi gibi kurulur (gerçek content/mulakat yüklenir).
 */
class MulakatBayragiTest {

    private static HttpEntity<String> json(String govde) {
        HttpHeaders basliklar = new HttpHeaders();
        basliklar.add("Content-Type", "application/json");
        return new HttpEntity<>(govde, basliklar);
    }

    @Nested
    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {
                    "spring.datasource.url=jdbc:h2:mem:mulakatkapali;DB_CLOSE_DELAY=-1",
                    "spring.ai.openai.api-key=",
                    "app.hafiza-dir=target/test-hafiza-mulakat-kapali",
                    "app.rapor-dir=target/test-raporlar-mulakat-kapali"
            })
    class VarsayilanKapali {

        @Autowired
        TestRestTemplate rest;

        @Autowired
        ApplicationContext baglam;

        @Test
        void mulakatBeanleriKurulmaz() {
            assertThat(baglam.getBeanNamesForType(MulakatController.class)).isEmpty();
            assertThat(baglam.getBeanNamesForType(MulakatService.class)).isEmpty();
            assertThat(baglam.getBeanNamesForType(MulakatAciklamaService.class)).isEmpty();
            assertThat(baglam.getBeanNamesForType(MulakatRepo.class)).isEmpty(); // content/mulakat okunmaz
            // sıfırlama bağımlılığı: JDBC repo koşulsuz kalır (tablo şemada; zararsız)
            assertThat(baglam.getBeanNamesForType(MulakatCevapRepo.class)).hasSize(1);
        }

        @Test
        void mulakatUclari404Doner() {
            assertThat(rest.getForEntity("/api/mulakat", String.class).getStatusCode().value()).isEqualTo(404);
            assertThat(rest.getForEntity("/api/mulakat/tekrar", String.class).getStatusCode().value()).isEqualTo(404);
            assertThat(rest.getForEntity("/api/mulakat/dogrular", String.class).getStatusCode().value()).isEqualTo(404);
            assertThat(rest.getForEntity("/api/mulakat/cekirdek-java/x", String.class).getStatusCode().value())
                    .isEqualTo(404);
            assertThat(rest.postForEntity("/api/mulakat/cevap", json("{\"soruId\":\"x\",\"secilenIndex\":0}"),
                    String.class).getStatusCode().value()).isEqualTo(404);
            assertThat(rest.postForEntity("/api/mulakat/acikla", json("{\"soruId\":\"x\",\"secilenIndex\":0}"),
                    String.class).getStatusCode().value()).isEqualTo(404);
        }

        @Test
        void ozellikBayragiKapaliDoner() {
            ResponseEntity<String> yanit = rest.getForEntity("/api/ozellikler", String.class);
            assertThat(yanit.getStatusCode().value()).isEqualTo(200);
            assertThat(yanit.getBody()).contains("\"mulakat\":false");
        }

        @Test
        void uygulamaninGeriKalaniVeSifirlamaCalisir() {
            assertThat(rest.getForEntity("/api/program", String.class).getStatusCode().value()).isEqualTo(200);
            ResponseEntity<String> sifirla = rest.postForEntity("/api/sifirla", json("{}"), String.class);
            assertThat(sifirla.getStatusCode().value()).isEqualTo(200);
            assertThat(sifirla.getBody()).contains("sifirlandi");
        }
    }

    @Nested
    @SpringBootTest(
            webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {
                    "spring.datasource.url=jdbc:h2:mem:mulakatacik;DB_CLOSE_DELAY=-1",
                    "spring.ai.openai.api-key=",
                    "app.hafiza-dir=target/test-hafiza-mulakat-acik",
                    "app.rapor-dir=target/test-raporlar-mulakat-acik",
                    "app.mulakat.etkin=true"
            })
    class BayrakAcik {

        @Autowired
        TestRestTemplate rest;

        @Test
        void mulakatUcuVeBayrakCalisir() {
            ResponseEntity<String> alanlar = rest.getForEntity("/api/mulakat", String.class);
            assertThat(alanlar.getStatusCode().value()).isEqualTo(200);
            assertThat(alanlar.getBody()).startsWith("[").contains("slug");
            assertThat(rest.getForEntity("/api/ozellikler", String.class).getBody())
                    .contains("\"mulakat\":true");
        }
    }
}
