package tr.velzon.aiogretmen.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Faz 10 (ADR-0017): kayıt/giriş/e-posta kimlik sistemi ve sayfaları kaldırıldı — uçlar yok. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitekimlikkaldirildi;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-sitekimlikkaldirildi",
        "app.rapor-dir=target/test-raporlar-sitekimlikkaldirildi"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteKimlikKaldirildiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void kimlikUclariYok() throws Exception {
        mvc.perform(post("/api/kimlik/kayit").with(csrf())).andExpect(status().isNotFound());
        mvc.perform(post("/api/kimlik/giris").with(csrf())).andExpect(status().isNotFound());
        mvc.perform(get("/api/kimlik/ayarlar")).andExpect(status().isNotFound());
    }

    @Test
    void kimlikSayfalariYok() throws Exception {
        mvc.perform(get("/giris.html")).andExpect(status().isNotFound());
        mvc.perform(get("/kayit.html")).andExpect(status().isNotFound());
        mvc.perform(get("/kimlik.js")).andExpect(status().isNotFound());
    }
}
