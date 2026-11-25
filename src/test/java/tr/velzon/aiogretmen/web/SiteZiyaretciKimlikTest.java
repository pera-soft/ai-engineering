package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.service.ZiyaretciService;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 10 (ADR-0017): girişsiz site — her ziyaretçi rastgele {@code aio_ziyaretci} çereziyle tanınır.
 * Giriş/yönlendirme yok, oturum ({@code JSESSIONID}) yok; CSRF korunur; veri çerez kimliğine göre ayrışır.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:siteziyaretci;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-siteziyaretci",
        "app.rapor-dir=target/test-raporlar-siteziyaretci"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteZiyaretciKimlikTest {

    private static final String CEREZ_A = "AAAAAAAAAAAAAAAAAAAAAa";
    // paylaşılan h2 mem DB: izolasyon ve CSRF testleri kendi kimliklerini kullanır (başka testle çakışmasın)
    private static final String CEREZ_IZOLE_A = "IIIIIIIIIIIIIIIIIIIIIa";
    private static final String CEREZ_IZOLE_B = "IIIIIIIIIIIIIIIIIIIIIb";
    private static final String CEREZ_CSRF = "SSSSSSSSSSSSSSSSSSSSSs";
    private static final String CEREZ_C = "CCCCCCCCCCCCCCCCCCCC_-";
    private static final String CEREZ_OKUYAN = "OOOOOOOOOOOOOOOOOOOOOo";
    private static final String CEREZ_YOK = "OOOOOOOOOOOOOOOOOOOOOy";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoSpyBean
    ZiyaretciService ziyaretciService;

    private static List<String> setCookie(MvcResult r, String ad) {
        return r.getResponse().getHeaders("Set-Cookie").stream().filter(c -> c.startsWith(ad + "=")).toList();
    }

    private static String ziyaretciCerezi(MvcResult r) {
        List<String> cerezler = setCookie(r, ZiyaretciCerezi.AD);
        assertThat(cerezler).as("aio_ziyaretci çerezi yazılmalı").hasSize(1);
        return cerezler.get(0);
    }

    private static String deger(String setCookie) {
        return setCookie.substring(setCookie.indexOf('=') + 1).split(";")[0];
    }

    @Test
    void cerezsizIlkIstekGecerliCerezYazar() throws Exception {
        MvcResult r = mvc.perform(get("/api/program")).andExpect(status().isOk()).andReturn();

        String cerez = ziyaretciCerezi(r);
        assertThat(ZiyaretciCerezi.gecerliMi(deger(cerez))).isTrue();
        assertThat(cerez).contains("HttpOnly").contains("SameSite=Lax").contains("Max-Age=31536000")
                .contains("Path=/");
    }

    @Test
    void gecersizCerezYenisiyleDegisir() throws Exception {
        MvcResult r = mvc.perform(get("/api/program").cookie(new Cookie(ZiyaretciCerezi.AD, "../../x")))
                .andExpect(status().isOk()).andReturn();

        String yeni = deger(ziyaretciCerezi(r));
        assertThat(yeni).isNotEqualTo("../../x");
        assertThat(ZiyaretciCerezi.gecerliMi(yeni)).isTrue();
    }

    @Test
    void gecerliCerezAynenYenilenir() throws Exception {
        MvcResult r = mvc.perform(get("/api/program").cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ_A)))
                .andExpect(status().isOk()).andReturn();

        assertThat(deger(ziyaretciCerezi(r))).isEqualTo(CEREZ_A); // kaydırmalı ömür: aynı kimlik, yeni Max-Age
    }

    @Test
    void girisYokTumSayfalarAcik() throws Exception {
        for (String yol : List.of("/", "/index.html")) {
            MvcResult r = mvc.perform(get(yol)).andExpect(status().isOk()).andReturn();
            assertThat(r.getResponse().getRedirectedUrl()).isNull();
            assertThat(setCookie(r, "JSESSIONID")).isEmpty();
            assertThat(r.getRequest().getSession(false)).as("oturum oluşmamalı (" + yol + ")").isNull();
        }
    }

    /** Başarısızlık ölçütü: aynı çerez aynı kullanıcı; A, B'nin verisini görmemeli (SiteKimlikTest'ten taşındı). */
    @Test
    void ayniCerezAyniKullanici() throws Exception {
        Cookie a = new Cookie(ZiyaretciCerezi.AD, CEREZ_IZOLE_A);
        Cookie b = new Cookie(ZiyaretciCerezi.AD, CEREZ_IZOLE_B);

        // A: görev işaretler, quiz kaydeder, sohbet geçmişi olur
        mvc.perform(post("/api/gorev/durum").cookie(a).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tamamlandi").value(true));
        mvc.perform(post("/api/quiz/statik-sonuc").cookie(a).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"puan\":80,\"detay\":\"{}\"}"))
                .andExpect(status().isOk());
        // sohbet AI ister (anahtarsız test) → doğrudan A'nın kovasına yazılır
        jdbc.update("INSERT INTO sohbet_mesaj (kullanici_id, hafta, rol, icerik) VALUES (?, 1, 'USER', 'A sırrı')",
                ZiyaretciKullaniciBaglami.ONEK + CEREZ_IZOLE_A);

        // A (aynı çerezle sonraki istekler) kendi verisini görür
        mvc.perform(get("/api/program").cookie(a)).andExpect(jsonPath("$[0].tamamlananGorev").value(1));
        mvc.perform(get("/api/hafta/1").cookie(a))
                .andExpect(jsonPath("$.gunler[0].gorevler[0].tamamlandi").value(true))
                .andExpect(jsonPath("$.sonQuizPuan").value(80));
        mvc.perform(get("/api/sohbet/1").cookie(a)).andExpect(jsonPath("$.length()").value(1));

        // B göremez
        mvc.perform(get("/api/program").cookie(b)).andExpect(jsonPath("$[0].tamamlananGorev").value(0));
        mvc.perform(get("/api/hafta/1").cookie(b))
                .andExpect(jsonPath("$.gunler[0].gorevler[0].tamamlandi").value(false))
                .andExpect(jsonPath("$.sonQuizPuan").doesNotExist());
        mvc.perform(get("/api/sohbet/1").cookie(b)).andExpect(jsonPath("$.length()").value(0));

        // kova "z-" + çerez kimliği; ne 'yerel'e ne öneksiz kimliğe yazılır
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = ?",
                Integer.class, ZiyaretciKullaniciBaglami.ONEK + CEREZ_IZOLE_A)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id IN ('yerel', ?)",
                Integer.class, CEREZ_IZOLE_A)).isZero();
    }

    @Test
    void ozelliklerSiteModunuSoylerKimligiSizdirmaz() throws Exception {
        mvc.perform(get("/api/ozellikler").cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ_C)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.site").value(true))
                .andExpect(jsonPath("$.kullanici").doesNotExist())
                .andExpect(jsonPath("$.mulakat").value(false));
    }

    @Test
    void csrfsizYazmaReddedilir() throws Exception {
        mvc.perform(post("/api/gorev/durum").cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ_CSRF))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":1,\"tamamlandi\":true}"))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = ?",
                Integer.class, ZiyaretciKullaniciBaglami.ONEK + CEREZ_CSRF)).isZero();
    }

    /**
     * Son inceleme #3: "180 gün gelmeyen" gerçekten gelmeyen olsun — yalnız okuyan (GET /api/**) ve satırı OLAN
     * ziyaretçinin {@code son_gorulme}'si tazelenir; satırı olmayan GET satır açmaz.
     */
    @Test
    void getSatiriOlaninSonGorulmesiniTazelerYeniSatirAcmaz() throws Exception {
        String okuyan = ZiyaretciKullaniciBaglami.ONEK + CEREZ_OKUYAN;
        Timestamp eski = Timestamp.from(Instant.now().minus(Duration.ofDays(100)));
        jdbc.update("INSERT INTO ziyaretci (id, olusturma, son_gorulme) VALUES (?, ?, ?)", okuyan, eski, eski);

        mvc.perform(get("/api/program").with(ip()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ_OKUYAN)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/program").with(ip()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ_YOK)))
                .andExpect(status().isOk());

        Instant son = jdbc.queryForObject("SELECT son_gorulme FROM ziyaretci WHERE id = ?", Timestamp.class, okuyan)
                .toInstant();
        assertThat(son).isAfter(Instant.now().minus(Duration.ofMinutes(5)));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class,
                ZiyaretciKullaniciBaglami.ONEK + CEREZ_YOK)).as("GET satır açmaz").isZero();
    }

    /** Kalıntı #3: GET'teki tazeleme bir yan iştir — veritabanı hatası okumayı düşürmez (yazmalar düşürür). */
    @Test
    void getTazelemeHatasiOkumayiDusurmez() throws Exception {
        doThrow(new DataAccessResourceFailureException("db yok")).when(ziyaretciService).tazele(anyString());

        mvc.perform(get("/api/program").with(ip()).cookie(new Cookie(ZiyaretciCerezi.AD, CEREZ_OKUYAN)))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor ip() {
        return r -> {
            r.setRemoteAddr("10.0.31.1");
            return r;
        };
    }

    @Test
    void kimlikDogrulamasiBicimiDenetler() {
        assertThat(ZiyaretciCerezi.gecerliMi(ZiyaretciCerezi.yeniKimlik())).isTrue();
        assertThat(ZiyaretciCerezi.yeniKimlik()).hasSize(22).isNotEqualTo(ZiyaretciCerezi.yeniKimlik());
        assertThat(ZiyaretciCerezi.gecerliMi(null)).isFalse();
        assertThat(ZiyaretciCerezi.gecerliMi("")).isFalse();
        assertThat(ZiyaretciCerezi.gecerliMi("AAAAAAAAAAAAAAAAAAAAA")).isFalse();   // 21
        assertThat(ZiyaretciCerezi.gecerliMi("AAAAAAAAAAAAAAAAAAAAAAA")).isFalse(); // 23
        assertThat(ZiyaretciCerezi.gecerliMi("AAAAAAAAAAAAAAAAAAAA/A")).isFalse();
        assertThat(ZiyaretciCerezi.gecerliMi("AAAAAAAAAAAAAAAAAAAA\nA")).isFalse();
    }
}
