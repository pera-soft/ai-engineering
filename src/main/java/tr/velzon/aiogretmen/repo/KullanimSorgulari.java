package tr.velzon.aiogretmen.repo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Toplu (aggregate) kullanım sorguları. Spring Data JDBC @Query, Map dönüşünü
 * satır varken desteklemiyor (RowDocumentAccessor hatası) — JdbcClient kullanılır.
 */
@Repository
public class KullanimSorgulari {

    private final JdbcClient jdbc;

    public KullanimSorgulari(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Yalnız verilen kullanıcının çağrıları (Faz 9b: başka kullanıcının kullanımı görünmez). */
    public List<Map<String, Object>> islemBazindaToplam(String kullaniciId) {
        // Takma adlar tırnaklı küçük harf: H2 tırnaksızları BÜYÜTÜR ve Map anahtarları
        // JSON'a büyük harf sızardı (inceleme 2. tur — API sözleşme tutarlılığı).
        return jdbc.sql("""
                        SELECT islem            AS "islem",
                               COUNT(*)          AS "cagri",
                               SUM(input_tokens)  AS "input_tokens",
                               SUM(output_tokens) AS "output_tokens"
                        FROM ai_kullanim
                        WHERE kullanici_id = :kullaniciId
                        GROUP BY islem
                        ORDER BY islem""")
                .param("kullaniciId", kullaniciId)
                .query()
                .listOfRows();
    }
}
