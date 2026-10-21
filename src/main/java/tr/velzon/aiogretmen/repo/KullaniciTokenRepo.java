package tr.velzon.aiogretmen.repo;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import tr.velzon.aiogretmen.types.KullaniciToken;

import java.util.Optional;

/** Süreli, tek kullanımlık token deposu (Faz 9a, ADR-0012) — yalnız SHA-256 özetleri tutulur. */
public interface KullaniciTokenRepo extends ListCrudRepository<KullaniciToken, Long> {

    Optional<KullaniciToken> findByTokenHashAndTur(String tokenHash, String tur);

    /** Tek token'ı siler; 0 dönerse başkası (eşzamanlı istek) önce tüketti → tek kullanımlık garanti. */
    @Modifying
    @Query("DELETE FROM kullanici_token WHERE token_hash = :hash")
    int tokenSil(@Param("hash") String hash);

    /** Hesabın o türdeki tüm token'larını siler (kullanım sonrası tek-kullanımlık + yeni token eskisini geçersiz kılar). */
    @Modifying
    @Query("DELETE FROM kullanici_token WHERE hesap_id = :hesapId AND tur = :tur")
    int hesabinTokenlariniSil(@Param("hesapId") long hesapId, @Param("tur") String tur);
}
