package tr.velzon.aiogretmen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YALNIZ YEREL DENEME göndericisi: bağlantıyı uygulama loguna INFO yazar (Faz 9a, ADR-0012). Yerel
 * duman testinde bağlantı logdan alınıp tarayıcıya yapıştırılır.
 *
 * <p><strong>Uyarı:</strong> bağlantı tek kullanımlık bir sırdır; log'a erişen hesabı doğrular/şifresini
 * sıfırlar. Bu yüzden yalnız {@code app.site.eposta-zorunlu=false} iken seçilir
 * ({@link EpostaGondericiSecimi}); varsayılan ({@code true}) SMTP'siz açılışı reddeder (ADR-0013).
 * Bean değil — seçim sınıfı üretir.
 */
public class LogEpostaGonderici implements EpostaGonderici {

    private static final Logger log = LoggerFactory.getLogger(LogEpostaGonderici.class);

    @Override
    public void dogrulamaBaglantisiGonder(String email, String baglanti) {
        log.info("[SMTP yok — yerel deneme] {} için doğrulama bağlantısı: {}", email, baglanti);
    }

    @Override
    public void sifreSifirlamaBaglantisiGonder(String email, String baglanti) {
        log.info("[SMTP yok — yerel deneme] {} için şifre sıfırlama bağlantısı: {}", email, baglanti);
    }
}
