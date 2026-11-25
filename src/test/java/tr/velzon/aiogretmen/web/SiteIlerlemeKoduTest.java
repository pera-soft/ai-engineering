package tr.velzon.aiogretmen.web;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 10 (ADR-0017): ziyaretçi kaydı + ilerleme kodu. Salt bakan ziyaretçi satır bırakmaz; ilk yazma kaydeder.
 * İlerleme kodu = çerezdeki kimlik; başka tarayıcıda geri yüklenince çerez o kimliğe döner.
 * Paylaşılan h2 mem DB → her test kendi kimliklerini kullanır.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:siteilerlemekodu;DB_CLOSE_DELAY=-1",
        "spring.ai.openai.api-key=",
        "app.hafiza-dir=target/test-hafiza-siteilerlemekodu",
        "app.rapor-dir=target/test-raporlar-siteilerlemekodu"
})
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SiteIlerlemeKoduTest {

    private static final String SALT_BAKAN = "OOOOOOOOOOOOOOOOOOOOOa";
    private static final String YAZAN = "WWWWWWWWWWWWWWWWWWWWWa";
    private static final String KOD_SORAN = "KKKKKKKKKKKKKKKKKKKKKa";
    private static final String TARAYICI_A = "GGGGGGGGGGGGGGGGGGGGGa";
    private static final String TARAYICI_B = "GGGGGGGGGGGGGGGGGGGGGb";
    private static final String OLMAYAN_KOD = "NNNNNNNNNNNNNNNNNNNNNa";
    private static final String OLMAYAN_ISTEYEN = "NNNNNNNNNNNNNNNNNNNNNb";
    private static final String BOZUK_ISTEYEN = "BBBBBBBBBBBBBBBBBBBBBz";
    private static final String KIRPMA = "PPPPPPPPPPPPPPPPPPPPPa";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private static Cookie cerez(String kimlik) {
        return new Cookie(ZiyaretciCerezi.AD, kimlik);
    }

    private int satir(String kimlik) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class,
                ZiyaretciKullaniciBaglami.ONEK + kimlik);
    }

    private static List<String> ziyaretciCerezleri(MvcResult r) {
        return r.getResponse().getHeaders("Set-Cookie").stream()
                .filter(c -> c.startsWith(ZiyaretciCerezi.AD + "=")).toList();
    }

    private void gorevIsaretle(String kimlik) throws Exception {
        mvc.perform(post("/api/gorev/durum").cookie(cerez(kimlik)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":true}"))
                .andExpect(status().isOk());
    }

    private MvcResult geriYukle(String kimlik, String govde) throws Exception {
        return mvc.perform(post("/api/ziyaretci/geri-yukle").cookie(cerez(kimlik)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(govde)).andReturn();
    }

    @Test
    void salaBakanSatirBirakmaz() throws Exception {
        mvc.perform(get("/api/program").cookie(cerez(SALT_BAKAN))).andExpect(status().isOk());
        mvc.perform(get("/api/hafta/1").cookie(cerez(SALT_BAKAN))).andExpect(status().isOk());

        assertThat(satir(SALT_BAKAN)).isZero();
    }

    @Test
    void yazmaSatirOlusturur() throws Exception {
        gorevIsaretle(YAZAN);
        gorevIsaretle(YAZAN);

        assertThat(satir(YAZAN)).isEqualTo(1);
    }

    @Test
    void kodMevcutKimligiDoner() throws Exception {
        mvc.perform(get("/api/ziyaretci/kod").cookie(cerez(KOD_SORAN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kod").value(KOD_SORAN));
        assertThat(satir(KOD_SORAN)).as("kod sormak kayıt oluşturmaz").isZero();
    }

    @Test
    void geriYuklemeCereziDegistirir() throws Exception {
        gorevIsaretle(TARAYICI_A);

        MvcResult r = geriYukle(TARAYICI_B, "{\"kod\":\"" + TARAYICI_A + "\"}");

        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(r.getResponse().getContentAsString()).contains("\"durum\":\"geri-yuklendi\"");
        // filtre eski kimliği (B) zaten yazmıştı; yanıtta YALNIZ geri yüklenen kimlik kalmalı
        List<String> cerezler = ziyaretciCerezleri(r);
        assertThat(cerezler).hasSize(1);
        assertThat(cerezler.get(0)).startsWith(ZiyaretciCerezi.AD + "=" + TARAYICI_A + ";")
                .contains("HttpOnly").contains("SameSite=Lax");
        assertThat(satir(TARAYICI_B)).as("geri yükleme isteği B için kayıt açmaz").isZero();

        // B tarayıcısı artık A'nın çereziyle A'nın ilerlemesini görür
        Cookie donenCerez = r.getResponse().getCookie(ZiyaretciCerezi.AD);
        assertThat(donenCerez).as("yanıttaki çerez").isNotNull();
        mvc.perform(get("/api/program").cookie(donenCerez))
                .andExpect(jsonPath("$[0].tamamlananGorev").value(1));
    }

    @Test
    void olmayanKod422() throws Exception {
        MvcResult r = geriYukle(OLMAYAN_ISTEYEN, "{\"kod\":\"" + OLMAYAN_KOD + "\"}");

        assertThat(r.getResponse().getStatus()).isEqualTo(422);
        assertThat(r.getResponse().getContentAsString())
                .isEqualTo("{\"hata\":\"Bu kodla kayıtlı ilerleme bulunamadı.\"}");
        List<String> cerezler = ziyaretciCerezleri(r);
        assertThat(cerezler).hasSize(1);
        assertThat(cerezler.get(0)).startsWith(ZiyaretciCerezi.AD + "=" + OLMAYAN_ISTEYEN + ";");
    }

    @Test
    void bozukKod400() throws Exception {
        for (String govde : List.of("{\"kod\":\"abc\"}", "{\"kod\":\"../../AAAAAAAAAAAAAAAAAA\"}", "{}",
                "{\"kod\":\"   \"}")) {
            assertThat(geriYukle(BOZUK_ISTEYEN, govde).getResponse().getStatus()).as(govde).isEqualTo(400);
        }
    }

    @Test
    void geriYukleBoslukKirpilirAyniKodNoOp() throws Exception {
        gorevIsaretle(KIRPMA);

        MvcResult r = geriYukle(KIRPMA, "{\"kod\":\"  " + KIRPMA + "\\n \"}");

        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        List<String> cerezler = ziyaretciCerezleri(r);
        assertThat(cerezler).hasSize(1);
        assertThat(cerezler.get(0)).startsWith(ZiyaretciCerezi.AD + "=" + KIRPMA + ";");
        assertThat(satir(KIRPMA)).isEqualTo(1);
    }

    /** Değiştirme yalnız aio_ziyaretci başlığını hedefler; aynı yanıttaki diğer çerezler (XSRF-TOKEN) kalır. */
    @Test
    void cerezDegistirmeDigerCerezleriKorur() {
        var istek = new MockHttpServletRequest();
        var yanit = new MockHttpServletResponse();
        yanit.addHeader("Set-Cookie", "XSRF-TOKEN=abc; Path=/");
        ZiyaretciCerezi.yaz(istek, yanit, TARAYICI_B, true);

        ZiyaretciCerezi.degistir(istek, yanit, TARAYICI_A, true);

        assertThat(yanit.getHeaders("Set-Cookie")).hasSize(2)
                .anySatisfy(c -> assertThat(c).startsWith("XSRF-TOKEN=abc"))
                .anySatisfy(c -> assertThat(c).startsWith(ZiyaretciCerezi.AD + "=" + TARAYICI_A + ";"))
                .noneSatisfy(c -> assertThat(c).contains(TARAYICI_B));
    }

    @Test
    void geriYuklemeCsrfIster() throws Exception {
        mvc.perform(post("/api/ziyaretci/geri-yukle").cookie(cerez(BOZUK_ISTEYEN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"kod\":\"" + TARAYICI_A + "\"}"))
                .andExpect(status().isForbidden());
    }
}
