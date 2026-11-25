package tr.velzon.aiogretmen.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Girişsiz {@code site} profilinde aktif kullanıcının kimliği (Faz 10, ADR-0017): {@link ZiyaretciCerezi}'nin
 * isteğe koyduğu ziyaretçi kimliği, {@value #ONEK} önekiyle → {@code kullanici_id = "z-<kimlik>"}. Önek, eski
 * {@code yerel} ve e-posta kimliklerinden ayırt eder.
 *
 * <p><strong>Fail-closed (ADR-0008):</strong> istek/kimlik yoksa ASLA {@link #YEREL}'e düşmez —
 * {@link IllegalStateException}. Filtre her isteğe kimlik koyduğundan bu yol normalde tetiklenmez; tetiklenirse
 * (ör. istek dışı bir iş parçacığı) sızıntı yerine hata.
 */
@Component
@Profile("site")
public class ZiyaretciKullaniciBaglami implements KullaniciBaglami {

    /** Ziyaretçi kimliklerinin {@code kullanici_id} öneki. */
    public static final String ONEK = "z-";

    @Override
    public String aktifKullanici() {
        RequestAttributes istek = RequestContextHolder.getRequestAttributes();
        if (istek != null
                && istek.getAttribute(ZiyaretciCerezi.ISTEK_OZNITELIGI, RequestAttributes.SCOPE_REQUEST)
                        instanceof String kimlik
                && ZiyaretciCerezi.gecerliMi(kimlik)) {
            return ONEK + kimlik;
        }
        throw new IllegalStateException(
                "site profilinde ziyaretçi kimliği bulunamadı — veri izolasyonu için işlem reddedildi.");
    }
}
