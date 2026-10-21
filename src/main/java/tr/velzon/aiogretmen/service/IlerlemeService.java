package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;
import tr.velzon.aiogretmen.types.QuizSonucKaydi;

@Service
public class IlerlemeService {

    private final IlerlemeRepo ilerlemeRepo;
    private final QuizSonucRepo quizSonucRepo;

    public IlerlemeService(IlerlemeRepo ilerlemeRepo, QuizSonucRepo quizSonucRepo) {
        this.ilerlemeRepo = ilerlemeRepo;
        this.quizSonucRepo = quizSonucRepo;
    }

    /**
     * Görev durumunu tersine çevirir; kayıt yoksa "tamamlandı" olarak oluşturur.
     * Eşzamanlı ilk-toggle (çift tık) UNIQUE(kullanici_id,hafta,gun,gorev_index) çakışması üretebilir;
     * bu durumda bir kez yeniden okunup çevrilir — en yaygın çakışma senaryosu (çift tık)
     * 500 üretmez. (Üç+ eşzamanlı istek gibi uç durumda ikinci deneme de çakışırsa
     * istisna yukarı taşar; tek kullanıcılı araçta kabul edilen artık risk.)
     *
     * @return görevin yeni durumu
     */
    public boolean gorevToggle(String kullaniciId, int hafta, int gun, int gorevIndex) {
        try {
            return okuVeCevir(kullaniciId, hafta, gun, gorevIndex);
        } catch (org.springframework.dao.DataIntegrityViolationException yarisCakismasi) {
            return okuVeCevir(kullaniciId, hafta, gun, gorevIndex);
        }
    }

    private boolean okuVeCevir(String kullaniciId, int hafta, int gun, int gorevIndex) {
        GorevIlerlemeKaydi kayit = ilerlemeRepo
                .findByKullaniciIdAndHaftaAndGunAndGorevIndex(kullaniciId, hafta, gun, gorevIndex)
                .map(mevcut -> mevcut.durumla(!mevcut.tamamlandi()))
                .orElseGet(() -> GorevIlerlemeKaydi.yeni(kullaniciId, hafta, gun, gorevIndex, true));
        return ilerlemeRepo.save(kayit).tamamlandi();
    }

    public QuizSonucKaydi statikQuizKaydet(String kullaniciId, int hafta, int puan, String detayJson) {
        return quizSonucRepo.save(QuizSonucKaydi.statik(kullaniciId, hafta, puan, detayJson));
    }

    public QuizSonucKaydi aiQuizKaydet(String kullaniciId, int hafta, int puan, String detayJson) {
        return quizSonucRepo.save(QuizSonucKaydi.aiUretim(kullaniciId, hafta, puan, detayJson));
    }
}
