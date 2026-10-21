package tr.velzon.aiogretmen.service;

/**
 * Hesap e-postalarının gönderim kapısı (Faz 9a/9c, ADR-0012 + ADR-0013). Gerçeklemeyi
 * {@link EpostaGondericiSecimi} seçer: SMTP ayarlıysa {@link SmtpEpostaGonderici}, değilse (yalnız
 * {@code app.site.eposta-zorunlu=false} yerel denemede) {@link LogEpostaGonderici}.
 *
 * <p>Bağlantılar ham token içerir — gizlidir; gerçeklemeler onları LOGLAMAZ (log gönderici hariç,
 * o yalnız yerel deneme içindir). Çağıran ({@link KimlikService}) gönderim hatasını yutar: e-posta
 * gidemese de HTTP isteği bozulmaz.
 */
public interface EpostaGonderici {

    /**
     * @param email     alıcı (küçük harfe çevrilmiş {@code @pera.software} adresi)
     * @param baglanti  tek kullanımlık, 24 saat geçerli doğrulama bağlantısı
     */
    void dogrulamaBaglantisiGonder(String email, String baglanti);

    /**
     * @param email     alıcı (doğrulanmış, etkin hesap)
     * @param baglanti  tek kullanımlık, 1 saat geçerli şifre sıfırlama bağlantısı
     */
    void sifreSifirlamaBaglantisiGonder(String email, String baglanti);
}
