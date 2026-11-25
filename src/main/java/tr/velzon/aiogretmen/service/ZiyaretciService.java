package tr.velzon.aiogretmen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.config.ZiyaretciCerezi;
import tr.velzon.aiogretmen.config.ZiyaretciKullaniciBaglami;
import tr.velzon.aiogretmen.repo.ZiyaretciRepo;
import tr.velzon.aiogretmen.types.IlerlemeKoduBulunamadiException;

import java.time.Clock;

/**
 * Girişsiz sitenin ziyaretçi kaydı ve ilerleme kodu (Faz 10, ADR-0017). İlerleme kodu = çerezdeki öneksiz kimlik;
 * başka tarayıcıda girilince o tarayıcının çerezi bu kimliğe döner.
 */
@Service
@Profile("site")
public class ZiyaretciService {

    private final ZiyaretciRepo repo;
    private final Clock saat;

    @Autowired
    public ZiyaretciService(ZiyaretciRepo repo) {
        this(repo, Clock.systemUTC());
    }

    ZiyaretciService(ZiyaretciRepo repo, Clock saat) {
        this.repo = repo;
        this.saat = saat;
    }

    /** Ziyaretçiyi kaydeder ya da {@code son_gorulme}'yi (günde en çok bir kez) tazeler. */
    public void dokun(String kullaniciId) {
        repo.dokun(kullaniciId, saat.instant());
    }

    public boolean varMi(String kullaniciId) {
        return repo.varMi(kullaniciId);
    }

    /**
     * İlerleme kodunu doğrular: boşluk kırpılır, biçim denetlenir (geçersiz → {@link IllegalArgumentException}, 400),
     * kayıt aranır (yok → {@link IlerlemeKoduBulunamadiException}, 422). Geri yüklenen kaydın {@code son_gorulme}'si
     * tazelenir.
     *
     * @return çereze yazılacak öneksiz kimlik
     */
    public String geriYukle(String kod) {
        String kimlik = kod == null ? null : kod.strip();
        if (!ZiyaretciCerezi.gecerliMi(kimlik)) {
            throw new IllegalArgumentException("İlerleme kodu geçersiz: 22 karakterlik kodu eksiksiz gir.");
        }
        String kullaniciId = ZiyaretciKullaniciBaglami.ONEK + kimlik;
        if (!repo.varMi(kullaniciId)) {
            throw new IlerlemeKoduBulunamadiException();
        }
        dokun(kullaniciId);
        return kimlik;
    }
}
