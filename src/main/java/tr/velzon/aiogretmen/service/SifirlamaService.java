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
 * hafızasını (data/hafiza) sıfırlar. Kitaplar/RAG(vektor.json)/içerik/müfredat DOKUNULMAZ.
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
    private final KullaniciBaglami kullaniciBaglami;

    public SifirlamaService(IlerlemeRepo ilerlemeRepo, GonderimRepo gonderimRepo,
                            QuizSonucRepo quizSonucRepo, SohbetRepo sohbetRepo,
                            MulakatCevapRepo mulakatCevapRepo, HafizaService hafizaService,
                            KullaniciBaglami kullaniciBaglami) {
        this.ilerlemeRepo = ilerlemeRepo;
        this.gonderimRepo = gonderimRepo;
        this.quizSonucRepo = quizSonucRepo;
        this.sohbetRepo = sohbetRepo;
        this.mulakatCevapRepo = mulakatCevapRepo;
        this.hafizaService = hafizaService;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    /**
     * Aktif kullanıcının görev ilerleme + gönderim + quiz + sohbet + mülakat cevapları (DB, atomik) VE öğrenci
     * hafızası (dosya) temizlenir. Hafıza silme DB silmelerinden SONRA ve kendi içinde hata-yutan
     * (best-effort) — DB rollback'i tetiklemez.
     */
    @Transactional
    public void sifirla() {
        String kullaniciId = kullaniciBaglami.aktifKullanici();
        ilerlemeRepo.deleteByKullaniciId(kullaniciId);
        gonderimRepo.deleteByKullaniciId(kullaniciId);
        quizSonucRepo.deleteByKullaniciId(kullaniciId);
        sohbetRepo.deleteByKullaniciId(kullaniciId);
        mulakatCevapRepo.deleteByKullaniciId(kullaniciId);
        hafizaService.ogrenciNotlariniSil(); // AI eski öğrenciyi hatırlamasın (kitaplar/RAG korunur)
    }
}
