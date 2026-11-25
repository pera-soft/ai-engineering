package tr.velzon.aiogretmen.repo;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Girişsiz sitenin ziyaretçi kaydı (Faz 10, ADR-0017) — {@code ziyaretci} tablosu; {@code id} = {@code kullanici_id}.
 * Yazma yükü düşük tutulur: {@code son_gorulme} en çok günde bir güncellenir.
 */
@Repository
@Profile("site")
public class ZiyaretciRepo {

    /** {@code son_gorulme} bundan yeni ise yeniden yazılmaz. */
    static final Duration YENILEME_ARALIGI = Duration.ofDays(1);

    private final JdbcTemplate jdbc;

    public ZiyaretciRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Kayıt yoksa ekler; varsa {@code son_gorulme}'yi yalnız 1 günden eskiyse günceller. Eşzamanlı ilk yazmalara
     * dayanıklı: INSERT birincil anahtarda çakışırsa (başka istek önce ekledi) hata yutulur, koşullu UPDATE denenir.
     * İşlem (transaction) dışında çağrılmalı — PostgreSQL'de başarısız INSERT açık işlemi bozar.
     */
    public void dokun(String kullaniciId, Instant simdi) {
        Timestamp an = Timestamp.from(simdi);
        Timestamp esik = Timestamp.from(simdi.minus(YENILEME_ARALIGI));
        if (eskiyseGuncelle(kullaniciId, an, esik) > 0 || varMi(kullaniciId)) {
            return;
        }
        try {
            jdbc.update("INSERT INTO ziyaretci (id, olusturma, son_gorulme) VALUES (?, ?, ?)", kullaniciId, an, an);
        } catch (DuplicateKeyException yaris) {
            eskiyseGuncelle(kullaniciId, an, esik);
        }
    }

    public boolean varMi(String kullaniciId) {
        Integer adet = jdbc.queryForObject("SELECT COUNT(*) FROM ziyaretci WHERE id = ?", Integer.class, kullaniciId);
        return adet != null && adet > 0;
    }

    /**
     * Satır VARSA {@code son_gorulme}'yi (1 günden eskiyse) tazeler; satır yoksa hiçbir şey yapmaz (insert yok).
     * Salt okuyan (GET) ziyaretçinin "180 gün gelmeyen" sayılmaması için (son inceleme #3).
     */
    public void tazele(String kullaniciId, Instant simdi) {
        eskiyseGuncelle(kullaniciId, Timestamp.from(simdi), Timestamp.from(simdi.minus(YENILEME_ARALIGI)));
    }

    /** Temizlik sayfası satırı: kimlik + sıralama/imleç için {@code son_gorulme}. */
    public record EskiZiyaretci(String id, Instant sonGorulme) {
    }

    /**
     * @return {@code son_gorulme}'si eşikten eski en çok {@code enCok} ziyaretçi, {@code (son_gorulme, id)} sırasıyla,
     *         imleçten ({@code sonZaman, sonId} = önceki partinin son satırı; ilk partide {@code null}) SONRAKİLER
     *         (keyset sayfalama — tech-debt #17, son inceleme). OFFSET yok: arada geri dönen ya da silinemeyen kimlik
     *         sayfayı kaydırmaz. Satır-değeri karşılaştırması yerine açık OR — H2 ve PostgreSQL'de aynı.
     */
    public List<EskiZiyaretci> eskiler(Instant esik, int enCok, Instant sonZaman, String sonId) {
        RowMapper<EskiZiyaretci> eslem = (rs, i) -> new EskiZiyaretci(rs.getString("id"),
                rs.getTimestamp("son_gorulme").toInstant());
        if (sonZaman == null) {
            return jdbc.query("SELECT id, son_gorulme FROM ziyaretci WHERE son_gorulme < ? "
                    + "ORDER BY son_gorulme, id LIMIT ?", eslem, Timestamp.from(esik), enCok);
        }
        Timestamp imlec = Timestamp.from(sonZaman);
        return jdbc.query("SELECT id, son_gorulme FROM ziyaretci WHERE son_gorulme < ? "
                        + "AND (son_gorulme > ? OR (son_gorulme = ? AND id > ?)) ORDER BY son_gorulme, id LIMIT ?",
                eslem, Timestamp.from(esik), imlec, imlec, sonId, enCok);
    }

    /**
     * Satırı yalnız {@code son_gorulme} hâlâ eşikten eskiyse siler (silme anında yaş yeniden denetlenir).
     *
     * @return silinen satır sayısı (0 = arada geri dönmüş ya da zaten yok)
     */
    public int sil(String kullaniciId, Instant esik) {
        return jdbc.update("DELETE FROM ziyaretci WHERE id = ? AND son_gorulme < ?", kullaniciId,
                Timestamp.from(esik));
    }

    private int eskiyseGuncelle(String kullaniciId, Timestamp an, Timestamp esik) {
        return jdbc.update("UPDATE ziyaretci SET son_gorulme = ? WHERE id = ? AND son_gorulme < ?", an, kullaniciId,
                esik);
    }
}
