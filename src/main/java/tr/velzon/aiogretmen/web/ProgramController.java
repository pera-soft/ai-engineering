package tr.velzon.aiogretmen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.service.IlerlemeService;
import tr.velzon.aiogretmen.service.MufredatService;
import tr.velzon.aiogretmen.types.HaftaDetay;
import tr.velzon.aiogretmen.types.HaftaOzeti;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProgramController {

    private final MufredatService mufredatService;
    private final IlerlemeService ilerlemeService;
    private final tr.velzon.aiogretmen.service.KaynakService kaynakService;
    private final tr.velzon.aiogretmen.service.SifirlamaService sifirlamaService;
    /** Aktif kullanıcının izolasyon kimliği (web profilinde "yerel"; ADR-0008). */
    private final KullaniciBaglami kullaniciBaglami;

    public ProgramController(MufredatService mufredatService, IlerlemeService ilerlemeService,
                             tr.velzon.aiogretmen.service.KaynakService kaynakService,
                             tr.velzon.aiogretmen.service.SifirlamaService sifirlamaService,
                             KullaniciBaglami kullaniciBaglami) {
        this.mufredatService = mufredatService;
        this.ilerlemeService = ilerlemeService;
        this.kaynakService = kaynakService;
        this.sifirlamaService = sifirlamaService;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    @GetMapping("/kaynaklar")
    public List<tr.velzon.aiogretmen.types.Kaynak> kaynaklar() {
        return kaynakService.liste();
    }

    @GetMapping("/kaynak/{slug}")
    public Map<String, Object> kaynak(@PathVariable String slug) {
        return kaynakService.oku(slug);
    }

    @GetMapping("/program")
    public List<HaftaOzeti> program() {
        return mufredatService.programOzeti(kullaniciBaglami.aktifKullanici());
    }

    @GetMapping("/hafta/{no}")
    public HaftaDetay hafta(@PathVariable int no) {
        return mufredatService.haftaDetay(kullaniciBaglami.aktifKullanici(), no);
    }

    /** Temiz başlangıç: ilerleme + sohbet + gönderim + quiz sıfırlanır (kitaplar/içerik korunur). */
    @PostMapping("/sifirla")
    public Map<String, Object> sifirla() {
        sifirlamaService.sifirla();
        return Map.of("durum", "sifirlandi");
    }

    /** {@code tamamlandi} kutulu (Boolean): eksikse sessizce false'a düşmesin — 400 dönsün. */
    public record GorevDurumIstek(int hafta, int gun, int gorevIndex, Boolean tamamlandi) {
    }

    /** İdempotent: görevi istenen duruma ayarlar (tekrar denenen istek işareti geri almaz). */
    @PostMapping("/gorev/durum")
    public Map<String, Object> gorevDurumu(@RequestBody GorevDurumIstek istek) {
        if (istek.tamamlandi() == null) {
            throw new IllegalArgumentException("tamamlandi alanı zorunludur (true/false)");
        }
        boolean tamamlandi = ilerlemeService.gorevDurumuAyarla(kullaniciBaglami.aktifKullanici(),
                istek.hafta(), istek.gun(), istek.gorevIndex(), istek.tamamlandi());
        return Map.of("tamamlandi", tamamlandi);
    }

    public record QuizSonucIstek(int hafta, int puan, String detay, String tur) {
    }

    /** tur: STATIK (varsayılan) | AI_URETIM — AI üretimi quiz sonuçları da buraya (brief 7.4). */
    @PostMapping("/quiz/statik-sonuc")
    public Map<String, Object> quizSonuc(@RequestBody QuizSonucIstek istek) {
        String kullaniciId = kullaniciBaglami.aktifKullanici();
        var kayit = "AI_URETIM".equals(istek.tur())
                ? ilerlemeService.aiQuizKaydet(kullaniciId, istek.hafta(), istek.puan(), istek.detay())
                : ilerlemeService.statikQuizKaydet(kullaniciId, istek.hafta(), istek.puan(), istek.detay());
        return Map.of("id", kayit.id(), "puan", kayit.puan(), "tur", kayit.tur());
    }
}
