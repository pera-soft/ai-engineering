package tr.velzon.aiogretmen.repo;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 9b: ESKİ (kullanıcısız) {@code ai_kullanim} tablosu olan mevcut DB'de schema.sql
 * {@code kullanici_id} kolonunu ekler, eski satırlar {@code 'yerel'} olur; tekrar çalıştırma idempotent.
 */
class AiKullanimGocTest {

    @Test
    void eskiTabloyaKullaniciKolonuEklenirEskiSatirlarYerelOlur() throws Exception {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:aikullanimgoc;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.execute("""
                CREATE TABLE ai_kullanim (
                  id IDENTITY PRIMARY KEY, islem VARCHAR(30) NOT NULL, model VARCHAR(50) NOT NULL,
                  input_tokens INT NOT NULL DEFAULT 0, output_tokens INT NOT NULL DEFAULT 0,
                  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP)""");
        jdbc.update("INSERT INTO ai_kullanim (islem, model, input_tokens, output_tokens) VALUES ('SOHBET','m',5,6)");

        try (Connection c = ds.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("schema.sql"));
            ScriptUtils.executeSqlScript(c, new ClassPathResource("schema.sql")); // idempotent
        }

        assertThat(jdbc.queryForObject("SELECT kullanici_id FROM ai_kullanim", String.class)).isEqualTo("yerel");
        jdbc.update("INSERT INTO ai_kullanim (kullanici_id, islem, model) VALUES ('a@pera.software','RAPOR','m')");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ai_kullanim WHERE kullanici_id='a@pera.software'",
                Integer.class)).isEqualTo(1);
    }
}
