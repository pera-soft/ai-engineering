package tr.velzon.aiogretmen.repo;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
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

    /** @return {@code son_gorulme}'si eşikten eski ziyaretçilerin kimlikleri (temizlik için). */
    public List<String> eskiler(Instant esik) {
        return jdbc.queryForList("SELECT id FROM ziyaretci WHERE son_gorulme < ? ORDER BY id", String.class,
                Timestamp.from(esik));
    }

    public void sil(String kullaniciId) {
        jdbc.update("DELETE FROM ziyaretci WHERE id = ?", kullaniciId);
    }

    private int eskiyseGuncelle(String kullaniciId, Timestamp an, Timestamp esik) {
        return jdbc.update("UPDATE ziyaretci SET son_gorulme = ? WHERE id = ? AND son_gorulme < ?", an, kullaniciId,
                esik);
    }
}
