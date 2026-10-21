package tr.velzon.aiogretmen.repo;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import tr.velzon.aiogretmen.types.Kullanici;

import java.time.Instant;
import java.util.Optional;

/**
 * {@code site} profili hesap deposu (Faz 9a, ADR-0012). Sayaç/kilit güncellemeleri tek SQL
 * ifadesiyle (atomik {@code basarisiz_deneme + 1}) yapılır — eşzamanlı yanlış denemeler sayımı kaybetmez.
 */
public interface KullaniciRepo extends ListCrudRepository<Kullanici, Long> {

    Optional<Kullanici> findByEmail(String email);

    @Modifying
    @Query("UPDATE kullanici SET basarisiz_deneme = basarisiz_deneme + 1 WHERE email = :email")
    int basarisizDenemeArtir(@Param("email") String email);

    /** Kilidi koyar ve sayacı sıfırlar (kilit bitince yeni 5 deneme hakkı). */
    @Modifying
    @Query("UPDATE kullanici SET kilit_bitis = :bitis, basarisiz_deneme = 0 WHERE email = :email")
    int kilitle(@Param("email") String email, @Param("bitis") Instant bitis);

    @Modifying
    @Query("UPDATE kullanici SET basarisiz_deneme = 0, kilit_bitis = NULL WHERE email = :email")
    int girisBasarili(@Param("email") String email);

    @Modifying
    @Query("UPDATE kullanici SET dogrulandi = TRUE WHERE id = :id")
    int dogrulandiIsaretle(@Param("id") long id);

    /** Şifre sıfırlama (Faz 9c): yeni özet + kaba kuvvet sayacı ve kilit temizlenir. */
    @Modifying
    @Query("UPDATE kullanici SET sifre_hash = :hash, basarisiz_deneme = 0, kilit_bitis = NULL WHERE id = :id")
    int sifreSifirla(@Param("id") long id, @Param("hash") String hash);
}
