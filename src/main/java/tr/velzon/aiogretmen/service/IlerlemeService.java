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
     * Görevi istenen duruma AYARLAR (idempotent "hedef durum"; eskiden toggle'dı): kayıt yoksa o
     * durumda oluşturur, varsa durumu yazar. Aynı çağrı N kez = aynı sonuç — zaman aşımı sonrası
     * tekrar denenen istek (MCP istemcisi, çift tık) işareti geri ALMAZ. Kayıt zaten hedef
     * durumdaysa yazma yapılmaz (tamamlanma zamanı {@code tarih} tekrarla ezilmez).
     * Eşzamanlı ilk ayarlama UNIQUE(kullanici_id,hafta,gun,gorev_index) çakışması üretebilir; bu
     * durumda bir kez yeniden okunup ayarlanır. İkinci deneme artık mevcut satırı bulur ve en fazla
     * UPDATE yapar (tekil indeks yalnız INSERT'te çakışır); set semantiğinde rakibin yazdığı değer
     * ne olursa olsun sonuç hedeftir — yeniden deneme güvenlidir.
     *
     * @return görevin yeni (= istenen) durumu
     */
    public boolean gorevDurumuAyarla(String kullaniciId, int hafta, int gun, int gorevIndex, boolean tamamlandi) {
        try {
            return okuVeAyarla(kullaniciId, hafta, gun, gorevIndex, tamamlandi);
        } catch (RuntimeException e) {
            if (!yarisCakismasi(e)) {
                throw e;
            }
            return okuVeAyarla(kullaniciId, hafta, gun, gorevIndex, tamamlandi);
        }
    }

    /**
     * Tekil indeks çakışması mı? Spring Data JDBC {@code save()} çakışmayı
     * {@code DbActionExecutionException}'a (düz RuntimeException) sarar — doğrudan
     * {@code DataIntegrityViolationException} yakalamak gerçek DB'de hiç tetiklenmiyordu
     * (IlerlemeServiceEszamanliTest yakaladı). Neden zinciri taranır.
     */
    private static boolean yarisCakismasi(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof org.springframework.dao.DataIntegrityViolationException) {
                return true;
            }
        }
        return false;
    }

    private boolean okuVeAyarla(String kullaniciId, int hafta, int gun, int gorevIndex, boolean tamamlandi) {
        var mevcut = ilerlemeRepo.findByKullaniciIdAndHaftaAndGunAndGorevIndex(kullaniciId, hafta, gun, gorevIndex);
        if (mevcut.isPresent() && mevcut.get().tamamlandi() == tamamlandi) {
            return tamamlandi; // zaten hedefte — yazma yok
        }
        GorevIlerlemeKaydi kayit = mevcut
                .map(k -> k.durumla(tamamlandi))
                .orElseGet(() -> GorevIlerlemeKaydi.yeni(kullaniciId, hafta, gun, gorevIndex, tamamlandi));
        return ilerlemeRepo.save(kayit).tamamlandi();
    }

    public QuizSonucKaydi statikQuizKaydet(String kullaniciId, int hafta, int puan, String detayJson) {
        return quizSonucRepo.save(QuizSonucKaydi.statik(kullaniciId, hafta, puan, detayJson));
    }

    public QuizSonucKaydi aiQuizKaydet(String kullaniciId, int hafta, int puan, String detayJson) {
        return quizSonucRepo.save(QuizSonucKaydi.aiUretim(kullaniciId, hafta, puan, detayJson));
    }
}
