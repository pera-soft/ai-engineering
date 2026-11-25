package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 10: sitede sohbet dışı AI uçları (değerlendirme, örnek, açıklama, quiz üretimi, rapor, atölye
 * değerlendirmesi, kitap) kayıtlı DEĞİL → JSON 404; sohbet geçmişi ve /api/ozellikler açık.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitekapaliai;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-sitekapaliai",
        "app.rapor-dir=target/test-raporlar-sitekapaliai"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteKapaliAiUclariTest {

    private static final String CEREZ = "KKKKKKKKKKKKKKKKKKKKKa";
    private static final String GOVDE = "{\"hafta\":1,\"gun\":1,\"icerik\":\"x\",\"gorevMetni\":\"x\"}";

    @Autowired
    MockMvc mvc;

    private static RequestPostProcessor adres() {
        return r -> {
            r.setRemoteAddr("10.5.0.1");
            return r;
        };
    }

    private void postKapali(String yol) throws Exception {
        mvc.perform(post(yol).with(adres()).with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ))
                        .contentType(MediaType.APPLICATION_JSON).content(GOVDE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.hata").exists());
    }

    private void getKapali(String yol) throws Exception {
        mvc.perform(get(yol).with(adres()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.hata").exists());
    }

    @Test
    void kapaliUclarSitede404() throws Exception {
        postKapali("/api/degerlendir");
        postKapali("/api/ornek");
        postKapali("/api/aciklama");
        postKapali("/api/quiz/uret");
        postKapali("/api/rapor/1/yenile");
        postKapali("/api/atolye/degerlendir");
        postKapali("/api/kitap-notu");
        getKapali("/api/rapor/1");
        getKapali("/api/kitaplar");
    }

    @Test
    void sohbetGecmisiSitedeAcik() throws Exception {
        mvc.perform(get("/api/sohbet/1").with(adres()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ)))
                .andExpect(status().isOk());
    }

    @Test
    void ozelliklerSitede() throws Exception {
        mvc.perform(get("/api/ozellikler").with(adres()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.site").value(true))
                .andExpect(jsonPath("$.sunucuAi").value(false))
                .andExpect(jsonPath("$.kullanici").doesNotExist());
    }
}
