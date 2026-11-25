package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.service.HizSiniri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Faz 10: girişsiz sitede IP/ziyaretçi hız sınırları (429). Her test kendi IP'sini ({@code remoteAddr}) ve
 * kimliklerini kullanır — sayaçlar paylaşılan uygulama bağlamında tutulur, testler birbirini etkilemesin.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sitehizsiniri;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-sitehizsiniri",
        "app.rapor-dir=target/test-raporlar-sitehizsiniri"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteHizSiniriTest {

    private static final String GOVDE_429 = "{\"hata\":\"Çok sık istek gönderdin, biraz bekleyip tekrar dene.\"}";
    private static final String GOREV = "{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":true}";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    HizSiniri hizSiniri;

    private static RequestPostProcessor ip(String adres) {
        return r -> {
            r.setRemoteAddr(adres);
            return r;
        };
    }

    /** 22 karakterlik geçerli kimlik: ön ek + sıra numarası. */
    private static String kimlik(char harf, int n) {
        return String.format("%c%021d", harf, n);
    }

    private MvcResult gorev(String adres, String cerez) throws Exception {
        return mvc.perform(post("/api/gorev/durum").with(ip(adres)).cookie(new Cookie(ZiyaretciCerezi.AD, cerez))
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(GOREV)).andReturn();
    }

    @Test
    void yazmaSiniri61inci429() throws Exception {
        String cerez = "YYYYYYYYYYYYYYYYYYYYYa";
        for (int i = 1; i <= 60; i++) {
            assertThat(gorev("10.0.1.1", cerez).getResponse().getStatus()).as("istek " + i).isEqualTo(200);
        }
        MvcResult r = gorev("10.0.1.1", cerez);

        assertThat(r.getResponse().getStatus()).isEqualTo(429);
        assertThat(r.getResponse().getContentAsString()).isEqualTo(GOVDE_429);
        // Retry-After: yazma penceresinin (1 dk) kalan süresi, saniye, en az 1
        assertThat(r.getResponse().getHeader("Retry-After")).isNotNull();
        assertThat(Integer.parseInt(r.getResponse().getHeader("Retry-After"))).isBetween(1, 60);
    }

    /** Tech-debt #15: geri-yükle sınırına takılan (reddedilen) istekler aynı IP'nin yazma kovasını tüketmez. */
    @Test
    void reddedilenGeriYukleYazmaKovasiniTuketmez() throws Exception {
        for (int i = 1; i <= 50; i++) {   // 10 izinli (422) + 40 reddedilen (429)
            MvcResult r = mvc.perform(post("/api/ziyaretci/geri-yukle").with(ip("10.0.8.1"))
                    .cookie(new Cookie(ZiyaretciCerezi.AD, "KKKKKKKKKKKKKKKKKKKKKa"))
                    .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"kod\":\"NNNNNNNNNNNNNNNNNNNNNa\"}")).andReturn();
            assertThat(r.getResponse().getStatus()).as("geri-yükle " + i).isEqualTo(i <= 10 ? 422 : 429);
            if (i > 10) {
                assertThat(Integer.parseInt(r.getResponse().getHeader("Retry-After")))
                        .as("geri-yükle penceresi 1 saat").isBetween(3500, 3600);
            }
        }
        // yaz|ip kovasında yalnız 10 izinli istek var → 50 yazma daha sığar
        for (int i = 1; i <= 50; i++) {
            assertThat(gorev("10.0.8.1", "KKKKKKKKKKKKKKKKKKKKKa").getResponse().getStatus()).as("görev " + i)
                    .isEqualTo(200);
        }
        assertThat(gorev("10.0.8.1", "KKKKKKKKKKKKKKKKKKKKKa").getResponse().getStatus()).isEqualTo(429);
    }

    /** Sitede sunucu anahtarı yok → sohbet 503 döner; sayaç preHandle'da işler, 21. istek yine 429 olmalı. */
    @Test
    void sohbetSiniri21inci429() throws Exception {
        String cerez = "SSSSSSSSSSSSSSSSSSSSSa";
        MvcResult son = null;
        for (int i = 1; i <= 21; i++) {
            son = mvc.perform(post("/api/sohbet").with(ip("10.0.2.1")).cookie(new Cookie(ZiyaretciCerezi.AD, cerez))
                    .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"hafta\":1,\"mesaj\":\"merhaba\"}")).andReturn();
            if (i <= 20) {
                assertThat(son.getResponse().getStatus()).as("istek " + i).isNotEqualTo(429);
            }
        }
        assertThat(son.getResponse().getStatus()).isEqualTo(429);
        assertThat(son.getResponse().getContentAsString()).isEqualTo(GOVDE_429);
    }

    @Test
    void geriYukle11inci429() throws Exception {
        MvcResult son = null;
        for (int i = 1; i <= 11; i++) {
            son = mvc.perform(post("/api/ziyaretci/geri-yukle").with(ip("10.0.3.1"))
                    .cookie(new Cookie(ZiyaretciCerezi.AD, "GGGGGGGGGGGGGGGGGGGGGa"))
                    .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"kod\":\"NNNNNNNNNNNNNNNNNNNNNa\"}")).andReturn();
            if (i <= 10) {
                assertThat(son.getResponse().getStatus()).as("istek " + i).isEqualTo(422);
            }
        }
        assertThat(son.getResponse().getStatus()).isEqualTo(429);
    }

    @Test
    void yeniZiyaretci21inci429() throws Exception {
        for (int i = 1; i <= 20; i++) {
            assertThat(gorev("10.0.4.1", kimlik('N', i)).getResponse().getStatus()).as("ziyaretçi " + i).isEqualTo(200);
        }
        MvcResult r = gorev("10.0.4.1", kimlik('N', 21));

        assertThat(r.getResponse().getStatus()).isEqualTo(429);
        assertThat(r.getResponse().getContentAsString()).isEqualTo(GOVDE_429);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class,
                "z-" + kimlik('N', 21))).as("reddedilen 21. ziyaretçi satır açmaz").isZero();
    }

    /** "yeni" reddi, ziyaretçiye özgü sohbet anahtarından ÖNCE: reddedilen taze kimlik haritada iz bırakmaz. */
    @Test
    void yenidenReddedilenSohbetAnahtarBirakmaz() throws Exception {
        for (int i = 1; i <= 20; i++) {
            gorev("10.0.6.1", kimlik('R', i));
        }
        int once = hizSiniri.anahtarSayisi();

        MvcResult r = mvc.perform(post("/api/sohbet").with(ip("10.0.6.1"))
                .cookie(new Cookie(ZiyaretciCerezi.AD, kimlik('R', 99))).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"hafta\":1,\"mesaj\":\"m\"}")).andReturn();

        assertThat(r.getResponse().getStatus()).isEqualTo(429);
        assertThat(hizSiniri.anahtarSayisi()).as("yaz|ip zaten vardı; sohbet| anahtarı eklenmemeli").isEqualTo(once);
    }

    /**
     * "/api/%73ohbet" güvenlik duvarında (RequestRejectedException) 400 ile elenir ve hiçbir işleyiciye ulaşmaz
     * (handler == null) → bu yoldan sınır atlatılamaz. Yine de interceptor eşleşmeyi ÇÖZÜLMÜŞ yol üzerinden yapar.
     */
    @Test
    void yuzdeKodluSohbetYoluIsleyiciyeUlasmaz() throws Exception {
        MvcResult r = mvc.perform(post("/api/%73ohbet").with(ip("10.0.7.1"))
                .cookie(new Cookie(ZiyaretciCerezi.AD, "EEEEEEEEEEEEEEEEEEEEEa")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"hafta\":1,\"mesaj\":\"m\"}")).andReturn();

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(r.getHandler()).isNull();
    }

    @Test
    void farkliIpEtkilenmez() throws Exception {
        String cerez = "FFFFFFFFFFFFFFFFFFFFFa";
        for (int i = 1; i <= 61; i++) {
            gorev("10.0.5.1", cerez);
        }
        assertThat(gorev("10.0.5.1", cerez).getResponse().getStatus()).isEqualTo(429);

        assertThat(gorev("10.0.5.2", cerez).getResponse().getStatus()).isEqualTo(200);
    }
}
