package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Süreli, tek kullanımlık hesap token'ı (Faz 9a, ADR-0012). Ham token YALNIZ bağlantıda yaşar;
 * veritabanında SHA-256 özeti ({@code tokenHash}) tutulur — DB sızsa bile bağlantı üretilemez.
 * {@code tur}: {@code DOGRULAMA} (e-posta doğrulama, 24 saat) | {@code SIFRE_SIFIRLAMA} (Faz 9c, 1 saat).
 * {@code hesapId} = {@code kullanici.id} (ilerleme tablolarındaki e-posta-kimlik {@code kullanici_id} ile
 * karışmasın diye farklı adlandırıldı).
 */
@Table("KULLANICI_TOKEN")
public record KullaniciToken(@Id Long id, Long hesapId, String tur, String tokenHash,
                             Instant sonGecerlilik, Instant olusturma) {

    public static final String DOGRULAMA = "DOGRULAMA";
    public static final String SIFRE_SIFIRLAMA = "SIFRE_SIFIRLAMA";

    public static KullaniciToken yeni(Long hesapId, String tur, String tokenHash, Instant sonGecerlilik) {
        return new KullaniciToken(null, hesapId, tur, tokenHash, sonGecerlilik, Instant.now());
    }
}
