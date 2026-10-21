package tr.velzon.aiogretmen.types;

/**
 * Hesap isteği kurallara uymuyor (alan adı {@code @pera.software} değil, şifre kısa/uzun, şifre sıfırlama
 * bağlantısı geçersiz/süresi dolmuş…).
 * {@link IllegalArgumentException} alt sınıfı → web katmanı 400 {hata, detay} döndürür.
 */
public class KayitGecersizException extends IllegalArgumentException {

    public KayitGecersizException(String mesaj) {
        super(mesaj);
    }
}
