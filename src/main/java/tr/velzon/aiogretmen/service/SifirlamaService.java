package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.MulakatCevapRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.repo.SohbetRepo;

/**
 * "Temiz başlangıç" (kullanıcı eklemesi): öğrenme ilerlemesi + sohbeti + AI'ın yazdığı öğrenci
 * hafızasını + rapor önbelleğini sıfırlar. Kitaplar/RAG(vektor.json)/içerik/müfredat DOKUNULMAZ.
 *
 * <p>Faz 2a-3 (ADR-0008): sıfırlama KULLANICI-İZOLE — yalnız aktif kullanıcının ({@code kullanici_id})
 * satırları silinir, başka kullanıcıların ilerlemesine dokunulmaz.
 */
@Service
public class SifirlamaService {

    private final IlerlemeRepo ilerlemeRepo;
    private final GonderimRepo gonderimRepo;
    private final QuizSonucRepo quizSonucRepo;
    private final SohbetRepo sohbetRepo;
    private final MulakatCevapRepo mulakatCevapRepo;
    private final HafizaService hafizaService;
    private final RaporService raporService;
    private final KullaniciBaglami kullaniciBaglami;

    public SifirlamaService(IlerlemeRepo ilerlemeRepo, GonderimRepo gonderimRepo,
                            QuizSonucRepo quizSonucRepo, SohbetRepo sohbetRepo,
                            MulakatCevapRepo mulakatCevapRepo, HafizaService hafizaService,
                            RaporService raporService, KullaniciBaglami kullaniciBaglami) {
        this.ilerlemeRepo = ilerlemeRepo;
        this.gonderimRepo = gonderimRepo;
        this.quizSonucRepo = quizSonucRepo;
        this.sohbetRepo = sohbetRepo;
        this.mulakatCevapRepo = mulakatCevapRepo;
        this.hafizaService = hafizaService;
        this.raporService = raporService;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    /** Aktif kullanıcı için {@link #sifirla(String)}. */
    @Transactional
    public void sifirla() {
        sifirla(kullaniciBaglami.aktifKullanici());
    }

    /**
     * Verilen kullanıcının görev ilerleme + gönderim + quiz + sohbet + mülakat cevapları (DB, atomik) VE
     * öğrenci hafızası + rapor önbelleği (dosya; Faz 9b: yalnız O kullanıcının dizini) temizlenir. Dosya
     * silme DB silmelerinden SONRA ve kendi içinde hata-yutan (best-effort) — DB rollback'i tetiklemez.
     */
    @Transactional
    public void sifirla(String kullaniciId) {
        ilerlemeRepo.deleteByKullaniciId(kullaniciId);
        gonderimRepo.deleteByKullaniciId(kullaniciId);
        quizSonucRepo.deleteByKullaniciId(kullaniciId);
        sohbetRepo.deleteByKullaniciId(kullaniciId);
        mulakatCevapRepo.deleteByKullaniciId(kullaniciId);
        hafizaService.ogrenciNotlariniSil(kullaniciId); // AI eski öğrenciyi hatırlamasın (kitaplar/RAG korunur)
        raporService.raporOnbeleginiSil(kullaniciId);   // eski ilerlemenin raporu yeni başlangıçta görünmesin
    }
}
