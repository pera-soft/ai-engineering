package tr.velzon.aiogretmen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiYanit;
import tr.velzon.aiogretmen.service.EpostaGonderici;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 9d (ADR-0014) — {@code site} profili GERÇEK PostgreSQL'e karşı. Testcontainers YOK (Docker garanti
 * değil): yalnız {@code PG_TEST_URL} ortam değişkeni verilince çalışır, normal {@code ./mvnw verify}'da
 * ATLANIR. Örnek (boş, atılabilir bir veritabanıyla):
 * <pre>
 *   createdb ai_ogretmen_test_x
 *   PG_TEST_URL=jdbc:postgresql://localhost:5432/ai_ogretmen_test_x PG_TEST_USERNAME=... PG_TEST_PASSWORD=... \
 *     ./mvnw -Dtest=SitePostgresEntegrasyonTest test
 *   dropdb ai_ogretmen_test_x
 * </pre>
 * Her koşu rastgele e-posta kullanır → aynı veritabanında tekrar koşulabilir.
 */
@EnabledIfEnvironmentVariable(named = "PG_TEST_URL", matches = ".+")
@SpringBootTest(properties = "spring.ai.openai.api-key=")
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SitePostgresEntegrasyonTest {

    private static final String SIFRE = "cok-gizli-sifre-123";

    @DynamicPropertySource
    static void veritabani(DynamicPropertyRegistry r) throws IOException {
        r.add("spring.datasource.url", () -> System.getenv("PG_TEST_URL"));
        r.add("spring.datasource.username", () -> Objects.requireNonNullElse(System.getenv("PG_TEST_USERNAME"), ""));
        r.add("spring.datasource.password", () -> Objects.requireNonNullElse(System.getenv("PG_TEST_PASSWORD"), ""));
        r.add("app.site.eposta-zorunlu", () -> "false");
        var kok = Files.createTempDirectory("site-pg");
        r.add("app.hafiza-dir", () -> kok.resolve("hafiza").toString());
        r.add("app.rapor-dir", () -> kok.resolve("raporlar").toString());
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DataSource dataSource;

    @MockitoBean
    EpostaGonderici eposta;

    @MockitoBean
    OgretmenAiClient ai;

    @Test
    void gercekPostgresKullaniliyor() {
        assertThat(jdbc.queryForObject("SELECT version()", String.class)).startsWith("PostgreSQL");
    }

    @Test
    void semaIkiKezCalisirIdempotent() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("schema.sql"));
            ScriptUtils.executeSqlScript(c, new ClassPathResource("schema.sql"));
        }
        // görev benzersizliği kullanıcıya bölünmüş: aynı kullanıcı aynı görevi iki kez ekleyemez
        String k = "tekil-" + UUID.randomUUID() + "@pera.software";
        jdbc.update("INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index) VALUES (?,1,1,0)", k);
        jdbc.update("INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index) VALUES (?,1,1,0)", k + "x");
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index) VALUES (?,1,1,0)", k))
                .isInstanceOf(DuplicateKeyException.class);
        jdbc.update("DELETE FROM gorev_ilerleme WHERE kullanici_id IN (?, ?)", k, k + "x");
    }

    @Test
    void kayitDogrulamaGirisIlerlemeRaporVeKapatma() throws Exception {
        String email = "pg-" + UUID.randomUUID().toString().substring(0, 8) + "@pera.software";

        // kayıt → doğrulama bağlantısı (sahte gönderici yakalar)
        mvc.perform(post("/api/kimlik/kayit").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"sifre\":\"" + SIFRE + "\"}"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> baglanti = ArgumentCaptor.forClass(String.class);
        verify(eposta).dogrulamaBaglantisiGonder(eq(email), baglanti.capture());
        String token = baglanti.getValue().substring(baglanti.getValue().indexOf("token=") + 6);

        // doğrulanmadan giriş yok
        mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", SIFRE))
                .andExpect(redirectedUrl("/giris.html?hata=dogrulanmamis"));
        mvc.perform(get("/api/kimlik/dogrula").param("token", token))
                .andExpect(redirectedUrl("/dogrulama-sonuc.html?durum=basarili"));

        // hatalı deneme sayacı (atomik UPDATE) + başarılı giriş sıfırlar
        mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", "yanlis-sifre-000"))
                .andExpect(redirectedUrl("/giris.html?hata=bilgi"));
        assertThat(jdbc.queryForObject("SELECT basarisiz_deneme FROM kullanici WHERE email = ?", Integer.class, email))
                .isEqualTo(1);
        MvcResult giris = mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", SIFRE))
                .andExpect(redirectedUrl("/")).andReturn();
        MockHttpSession oturum = (MockHttpSession) giris.getRequest().getSession(false);
        assertThat(jdbc.queryForObject("SELECT basarisiz_deneme FROM kullanici WHERE email = ?", Integer.class, email))
                .isZero();

        // görev işaretle (insert) → geri al (update) → yeniden işaretle
        String gorev = "{\"hafta\":1,\"gun\":1,\"gorevIndex\":0}";
        for (boolean beklenen : new boolean[]{true, false, true}) {
            mvc.perform(post("/api/gorev/toggle").with(csrf()).session(oturum)
                            .contentType(MediaType.APPLICATION_JSON).content(gorev))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.tamamlandi").value(beklenen));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = ? AND tamamlandi",
                Integer.class, email)).isEqualTo(1);

        // quiz sonucu (TEXT kolon) + hafta görünümü + sohbet geçmişi (türetilmiş sorgular)
        mvc.perform(post("/api/quiz/statik-sonuc").with(csrf()).session(oturum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"puan\":80,\"detay\":\"{\\\"dogru\\\":4}\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.puan").value(80));
        mvc.perform(get("/api/hafta/1").session(oturum)).andExpect(status().isOk());
        mvc.perform(get("/api/sohbet/1").session(oturum)).andExpect(status().isOk());

        // haftalık rapor (AI sahte) + kullanım raporu (aggregate sorgu, yalnız kendi satırları)
        when(ai.aktif()).thenReturn(true);
        when(ai.cagir(any())).thenReturn(new AiYanit("## Hafta 1 raporu\nİyi gidiyor.", 10, 20, "sahte"));
        mvc.perform(get("/api/rapor/1").session(oturum)).andExpect(status().isOk())
                .andExpect(jsonPath("$.markdown").value(org.hamcrest.Matchers.containsString("Hafta 1 raporu")));
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens) "
                + "VALUES (?, 'RAPOR', 'sahte', 10, 20)", email);
        mvc.perform(get("/api/kullanim").session(oturum)).andExpect(status().isOk())
                .andExpect(jsonPath("$..islem").value(org.hamcrest.Matchers.hasItem("RAPOR")));

        // temiz başlangıç: tüm tablolardaki kullanıcıya-özel DELETE'ler
        mvc.perform(post("/api/sifirla").with(csrf()).session(oturum)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM quiz_sonuc WHERE kullanici_id = ?", Integer.class, email))
                .isZero();

        // işletmen hesabı kapatır (runbook) → giriş 'kapali'
        jdbc.update("UPDATE kullanici SET etkin = FALSE WHERE email = ?", email);
        mvc.perform(post("/api/kimlik/giris").with(csrf()).param("email", email).param("sifre", SIFRE))
                .andExpect(redirectedUrl("/giris.html?hata=kapali"));
    }
}
