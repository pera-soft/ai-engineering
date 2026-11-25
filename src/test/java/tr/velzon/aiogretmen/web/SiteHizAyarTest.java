package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Son inceleme #4: hız sınırları {@code app.site.hiz.*} (ortamda {@code APP_SITE_HIZ_*}) ile ayarlanabilir — tek NAT
 * arkasındaki sınıf/ofis için. Burada yeni ziyaretçi sınırı 2/saat/IP: 3. yeni ziyaretçi 429.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitehizayar;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-sitehizayar",
        "app.rapor-dir=target/test-raporlar-sitehizayar",
        // ortam değişkeni adı → application-site.yml placeholder'ı → app.site.hiz.yeni-ziyaretci-saat
        "APP_SITE_HIZ_YENI_ZIYARETCI_SAAT=2"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteHizAyarTest {

    @Autowired
    MockMvc mvc;

    private int gorev(String cerez) throws Exception {
        return mvc.perform(post("/api/gorev/durum").with(r -> {
                    r.setRemoteAddr("10.0.41.1");
                    return r;
                }).cookie(new Cookie(ZiyaretciCerezi.AD, cerez)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":true}"))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void ayarlananYeniZiyaretciSiniriUygulanir() throws Exception {
        assertThat(gorev("HHHHHHHHHHHHHHHHHHHHH1")).isEqualTo(200);
        assertThat(gorev("HHHHHHHHHHHHHHHHHHHHH2")).isEqualTo(200);

        assertThat(gorev("HHHHHHHHHHHHHHHHHHHHH3")).isEqualTo(429);
        assertThat(gorev("HHHHHHHHHHHHHHHHHHHHH1")).as("kayıtlı ziyaretçi yeni sayılmaz").isEqualTo(200);
    }
}
