package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.CalismaModu;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.repo.IlerlemeRepo;
import tr.velzon.aiogretmen.repo.QuizSonucRepo;
import tr.velzon.aiogretmen.types.GorevIlerlemeKaydi;

import java.util.Map;

/**
 * Ortak sistem bağlamı (brief 7.1): kimlik + aktif hafta/gün ham markdown'ı +
 * ilerleme özeti + öğrenci hafızası. Her AI çağrısının sistem prompt'unun temelidir.
 */
@Service
public class BaglamServisi {

    private final ContentRepo contentRepo;
    private final IlerlemeRepo ilerlemeRepo;
    private final QuizSonucRepo quizSonucRepo;
    private final PromptFactory promptFactory;
    private final HafizaService hafizaService;
    private final KitapService kitapService;
    private final KaynakService kaynakService;
    private final KullaniciBaglami kullaniciBaglami;
    /** Faz 9b: site profilinde ortak kitap notu/vektör bağlamı prompt'a GİRMEZ (kullanıcıya ayrılmamış veri). */
    private final boolean kitapBaglamiAcik;

    public BaglamServisi(ContentRepo contentRepo, IlerlemeRepo ilerlemeRepo,
                         QuizSonucRepo quizSonucRepo, PromptFactory promptFactory,
                         HafizaService hafizaService, KitapService kitapService,
                         KaynakService kaynakService, KullaniciBaglami kullaniciBaglami,
                         CalismaModu calismaModu) {
        this.contentRepo = contentRepo;
        this.ilerlemeRepo = ilerlemeRepo;
        this.quizSonucRepo = quizSonucRepo;
        this.promptFactory = promptFactory;
        this.hafizaService = hafizaService;
        this.kitapService = kitapService;
        this.kaynakService = kaynakService;
        this.kullaniciBaglami = kullaniciBaglami;
        this.kitapBaglamiAcik = !calismaModu.siteMi();
    }

    /** @param gun null olabilir — o zaman yalnız hafta bağlamı verilir (sorgu = ders metni). */
    public String sistemBaglami(int hafta, Integer gun) {
        return sistemBaglami(hafta, gun, null);
    }

    /**
     * @param sorgu anlam-eşleşmeli kitap bağlamı için HİBRİT sorgu (ADR-0005): öğrencinin son
     *              sohbet mesajı varsa o; yoksa (değerlendirme/rapor) aktif ders metni kullanılır.
     */
    public String sistemBaglami(int hafta, Integer gun, String sorgu) {
        String etkinSorgu = sorgu != null && !sorgu.isBlank() ? sorgu
                : (gun == null ? contentRepo.hamHafta(hafta) : contentRepo.hamGun(hafta, gun));
        return promptFactory.doldur("sistem-kimlik", Map.of(
                "hafta_icerik", contentRepo.hamHafta(hafta),
                "gun_icerik", gun == null ? "(belirli bir gün seçilmedi)" : contentRepo.hamGun(hafta, gun),
                "ilerleme_ozeti", ilerlemeOzeti(hafta),
                "hafiza", hafizaService.hafizaBaglami(hafta),
                "kitaplar", kitapBaglamiAcik ? kitapService.baglamIcinAnlam(etkinSorgu, hafta)
                        : "(Kitap notu bağlamı bu sitede kapalı.)",
                "kaynaklar", kaynakService.baglamIcin(hafta)));
    }

    /**
     * Embeddingsiz sistem bağlamı — MCP prompt'ları için (ADR-0007). {@link #sistemBaglami}'dan farkı:
     * anlam-eşleşmeli kitap bağlamı (embedding çağrısı) ve öğrenci hafızası ATLANIR. Böylece MCP
     * adaptörü LLM/embedding sağlayıcısına HİÇ dokunmaz — muhakeme (ve maliyet) bağlanan istemcide kalır.
     */
    public String sistemBaglamiEmbeddingsiz(int hafta, Integer gun) {
        return promptFactory.doldur("sistem-kimlik", Map.of(
                "hafta_icerik", contentRepo.hamHafta(hafta),
                "gun_icerik", gun == null ? "(belirli bir gün seçilmedi)" : contentRepo.hamGun(hafta, gun),
                "ilerleme_ozeti", ilerlemeOzeti(hafta),
                "hafiza", "",
                "kitaplar", "(kitap bağlamı istemci tarafında — sunucu embedding çağırmaz)",
                "kaynaklar", kaynakService.baglamIcin(hafta)));
    }

    private String ilerlemeOzeti(int hafta) {
        String kullaniciId = kullaniciBaglami.aktifKullanici();
        long tamamlanan = ilerlemeRepo.findByKullaniciIdAndHafta(kullaniciId, hafta).stream()
                .filter(GorevIlerlemeKaydi::tamamlandi).count();
        int toplam = contentRepo.hafta(hafta)
                .map(h -> h.gunler().stream().mapToInt(g -> g.gorevler().size()).sum())
                .orElse(0);
        String quiz = quizSonucRepo.findByKullaniciIdAndHaftaOrderByTarihDesc(kullaniciId, hafta).stream().findFirst()
                .map(k -> "son quiz puanı %" + k.puan())
                .orElse("quiz henüz çözülmedi");
        return "Görev: %d/%d tamamlandı; %s.".formatted(tamamlanan, toplam, quiz);
    }
}
