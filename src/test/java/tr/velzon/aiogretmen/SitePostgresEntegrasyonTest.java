package tr.velzon.aiogretmen;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.service.ZiyaretciTemizlikService;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faz 9d (ADR-0014), Faz 10 (ADR-0017) — girişsiz {@code site} profili GERÇEK PostgreSQL'e karşı: şema, ziyaretçi
 * kaydı, iki ziyaretçinin izolasyonu, ilerleme kodu ve 180 gün temizliği. Testcontainers YOK (Docker garanti
 * değil): yalnız {@code PG_TEST_URL} ortam değişkeni verilince çalışır, normal {@code ./mvnw verify}'da
 * ATLANIR. Örnek (boş, atılabilir bir veritabanıyla):
 * <pre>
 *   createdb ai_ogretmen_test_x
 *   PG_TEST_URL=jdbc:postgresql://localhost:5432/ai_ogretmen_test_x PG_TEST_USERNAME=... PG_TEST_PASSWORD=... \
 *     ./mvnw -Dtest=SitePostgresEntegrasyonTest test
 *   dropdb ai_ogretmen_test_x
 * </pre>
 * Her koşu rastgele ziyaretçi kimliği kullanır → aynı veritabanında tekrar koşulabilir (girişsiz site, ADR-0017).
 */
@EnabledIfEnvironmentVariable(named = "PG_TEST_URL", matches = ".+")
@SpringBootTest(properties = "spring.ai.openai.api-key=")
@ActiveProfiles("site")
@AutoConfigureMockMvc
class SitePostgresEntegrasyonTest {

    @DynamicPropertySource
    static void veritabani(DynamicPropertyRegistry r) throws IOException {
        r.add("spring.datasource.url", () -> System.getenv("PG_TEST_URL"));
        r.add("spring.datasource.username", () -> Objects.requireNonNullElse(System.getenv("PG_TEST_USERNAME"), ""));
        r.add("spring.datasource.password", () -> Objects.requireNonNullElse(System.getenv("PG_TEST_PASSWORD"), ""));
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

    @Autowired
    ZiyaretciTemizlikService temizlik;

    /** Gerçek AI çağrısı olmasın (sitede sohbet dışı AI uçları zaten kapalı). */
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
        String k = "tekil-" + UUID.randomUUID();
        jdbc.update("INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index) VALUES (?,1,1,0)", k);
        jdbc.update("INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index) VALUES (?,1,1,0)", k + "x");
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index) VALUES (?,1,1,0)", k))
                .isInstanceOf(DuplicateKeyException.class);
        jdbc.update("DELETE FROM gorev_ilerleme WHERE kullanici_id IN (?, ?)", k, k + "x");
    }

    @Test
    void ziyaretciIlerlemeKullanimVeSifirlama() throws Exception {
        String kimlik = ZiyaretciCerezi.yeniKimlik();
        String kullanici = ZiyaretciKullaniciBaglami.ONEK + kimlik;
        Cookie ziyaretci = new Cookie(ZiyaretciCerezi.AD, kimlik);

        // görev işaretle (insert) → tekrar (idempotent, yazma yok) → geri al (update) → yeniden işaretle
        for (boolean beklenen : new boolean[]{true, true, false, true}) {
            String gorev = "{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":" + beklenen + "}";
            mvc.perform(post("/api/gorev/durum").with(csrf()).cookie(ziyaretci)
                            .contentType(MediaType.APPLICATION_JSON).content(gorev))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.tamamlandi").value(beklenen));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = ? AND tamamlandi",
                Integer.class, kullanici)).isEqualTo(1);

        // quiz sonucu (TEXT kolon) + hafta görünümü + sohbet geçmişi (türetilmiş sorgular)
        mvc.perform(post("/api/quiz/statik-sonuc").with(csrf()).cookie(ziyaretci)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hafta\":1,\"puan\":80,\"detay\":\"{\\\"dogru\\\":4}\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.puan").value(80));
        mvc.perform(get("/api/hafta/1").cookie(ziyaretci)).andExpect(status().isOk());
        mvc.perform(get("/api/sohbet/1").cookie(ziyaretci)).andExpect(status().isOk());

        // ilk yazma ziyaretci satırını açtı (Faz 10, ADR-0017)
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class, kullanici))
                .isEqualTo(1);

        // haftalık rapor sitede kapalı (sohbet dışı AI uçları yok) → 404
        mvc.perform(get("/api/rapor/1").cookie(ziyaretci)).andExpect(status().isNotFound());
        // kullanım raporu (aggregate sorgu, yalnız kendi satırları)
        // sure_ms + yanit_model (ADR-0016): PostgreSQL'de de ADD COLUMN IF NOT EXISTS ile eklendi
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens, sure_ms, "
                + "yanit_model) VALUES (?, 'RAPOR', 'sahte', 10, 20, 1500, 'sahte/yedek')", kullanici);
        assertThat(jdbc.queryForObject("SELECT sure_ms FROM ai_kullanim WHERE kullanici_id = ? AND yanit_model = ?",
                Integer.class, kullanici, "sahte/yedek")).isEqualTo(1500);
        mvc.perform(get("/api/kullanim").cookie(ziyaretci)).andExpect(status().isOk())
                .andExpect(jsonPath("$..islem").value(org.hamcrest.Matchers.hasItem("RAPOR")));

        // temiz başlangıç: tüm tablolardaki kullanıcıya-özel DELETE'ler
        mvc.perform(post("/api/sifirla").with(csrf()).cookie(ziyaretci)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM quiz_sonuc WHERE kullanici_id = ?", Integer.class,
                kullanici)).isZero();
    }

    /** İki ziyaretçi aynı görevi/quiz'i yazar; birinin sıfırlaması ötekine dokunmaz. İlerleme kodu PostgreSQL'de. */
    @Test
    void ikiZiyaretciIzoleVeIlerlemeKoduGeriYuklenir() throws Exception {
        String a = ZiyaretciCerezi.yeniKimlik();
        String b = ZiyaretciCerezi.yeniKimlik();
        String gorev = "{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":true}";
        for (String k : new String[]{a, b}) {
            mvc.perform(post("/api/gorev/durum").with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, k))
                    .contentType(MediaType.APPLICATION_JSON).content(gorev)).andExpect(status().isOk());
        }
        String ka = ZiyaretciKullaniciBaglami.ONEK + a;
        String kb = ZiyaretciKullaniciBaglami.ONEK + b;
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id IN (?, ?)", Integer.class, ka, kb))
                .isEqualTo(2);

        mvc.perform(post("/api/sifirla").with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, b)))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = ?", Integer.class,
                kb)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gorev_ilerleme WHERE kullanici_id = ? AND tamamlandi",
                Integer.class, ka)).as("B'nin sıfırlaması A'ya dokunmaz").isEqualTo(1);

        // yeni bir tarayıcı A'nın koduyla geri yükler → çerez A'ya döner; kayıtsız kod 422
        String yeni = ZiyaretciCerezi.yeniKimlik();
        mvc.perform(post("/api/ziyaretci/geri-yukle").with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, yeni))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"kod\":\"" + a + "\"}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie()
                        .value(ZiyaretciCerezi.AD, a));
        mvc.perform(post("/api/ziyaretci/geri-yukle").with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, yeni))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kod\":\"" + ZiyaretciCerezi.yeniKimlik() + "\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    /** 180 gün temizliği PostgreSQL'de tek okuma-yazma işleminde siler (salt-okunur işlem DELETE'i reddederdi). */
    @Test
    void eskiZiyaretciTemizlenirYenisiKalir() throws Exception {
        String eski = ZiyaretciKullaniciBaglami.ONEK + ZiyaretciCerezi.yeniKimlik();
        Timestamp cokOnce = Timestamp.from(Instant.now().minus(Duration.ofDays(200)));
        jdbc.update("INSERT INTO ziyaretci (id, olusturma, son_gorulme) VALUES (?, ?, ?)", eski, cokOnce, cokOnce);
        jdbc.update("INSERT INTO gorev_ilerleme (kullanici_id, hafta, gun, gorev_index, tamamlandi) "
                + "VALUES (?,1,1,0,TRUE)", eski);
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model, input_tokens, output_tokens) "
                + "VALUES (?, 'SOHBET', 'sahte', 1, 1)", eski);

        String yeni = ZiyaretciCerezi.yeniKimlik();
        mvc.perform(post("/api/gorev/durum").with(csrf()).cookie(new Cookie(ZiyaretciCerezi.AD, yeni))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hafta\":1,\"gun\":1,\"gorevIndex\":0,\"tamamlandi\":true}")).andExpect(status().isOk());

        assertThat(temizlik.temizle()).isPositive();

        for (String tablo : new String[]{"gorev_ilerleme", "ai_kullanim"}) {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + tablo + " WHERE kullanici_id = ?",
                    Integer.class, eski)).as(tablo).isZero();
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class, eski)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class,
                ZiyaretciKullaniciBaglami.ONEK + yeni)).isEqualTo(1);
    }
}
