package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * {@code site} profilinde bir hesap (Faz 9a, ADR-0012). {@code email} küçük harfe çevrilmiş
 * ({@code @pera.software}) adrestir ve aynı zamanda tüm ilerleme tablolarındaki {@code kullanici_id}
 * izolasyon kimliğidir (ADR-0008 ile aynı: e-posta). Şifre yalnız BCrypt özeti olarak saklanır.
 *
 * <p>{@code dogrulandi=false} iken giriş yapılamaz; {@code etkin=false} hesabı kapatır (işten ayrılan).
 * {@code basarisizDeneme}/{@code kilitBitis} kaba kuvvet kilidini taşır (5 hata → 15 dk).
 */
@Table("KULLANICI")
public record Kullanici(@Id Long id, String email, String sifreHash, boolean dogrulandi, boolean etkin,
                        Instant olusturma, int basarisizDeneme, Instant kilitBitis) {

    public static Kullanici yeni(String email, String sifreHash) {
        return new Kullanici(null, email, sifreHash, false, true, Instant.now(), 0, null);
    }

    /** Ekip koduyla açılan hesap (ADR-0015): e-posta doğrulaması yok, doğrulanmış başlar. */
    public static Kullanici yeniDogrulanmis(String email, String sifreHash) {
        return new Kullanici(null, email, sifreHash, true, true, Instant.now(), 0, null);
    }

    public Kullanici sifreyle(String yeniHash) {
        return new Kullanici(id, email, yeniHash, dogrulandi, etkin, olusturma, basarisizDeneme, kilitBitis);
    }

    /** Verilen anda kilit sürüyor mu? */
    public boolean kilitli(Instant simdi) {
        return kilitBitis != null && kilitBitis.isAfter(simdi);
    }
}
