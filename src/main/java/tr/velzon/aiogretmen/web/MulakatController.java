package tr.velzon.aiogretmen.web;

import tr.velzon.aiogretmen.config.MulakatEtkin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.service.MulakatAciklamaService;
import tr.velzon.aiogretmen.service.MulakatService;
import tr.velzon.aiogretmen.types.MulakatAlanOzeti;
import tr.velzon.aiogretmen.types.MulakatCevapSonucu;
import tr.velzon.aiogretmen.types.MulakatKonuGorunumu;
import tr.velzon.aiogretmen.types.MulakatSoruGorunumu;

import java.util.List;
import java.util.Map;

/**
 * Mülakat (Java mülakat hazırlık) uçları (Faz 1):
 *  - GET  /api/mulakat                       → alan/konu ağacı (özet; cevap yok).
 *  - GET  /api/mulakat/{alanSlug}/{konuSlug} → konu dersi + CEVAPSIZ sorular (+ kullanıcının
 *                                              kendi son denemeleri; cevaplanmamışta null).
 *  - POST /api/mulakat/cevap                 → cevabı sunucuda değerlendirir + kaydeder.
 *  - GET  /api/mulakat/tekrar                → son denemesi yanlış olan sorular (cevapsız).
 *  - GET  /api/mulakat/dogrular              → son denemesi doğru olan sorular (cevapsız; takip).
 *  - POST /api/mulakat/acikla                → AI sonuç-açıklaması (düz metin).
 * Boş soruId → 400; soru/konu yok → 404; anahtar yoksa → 503 (HataYonetimi sözleşmesi).
 * Cevap ({@code dogruIndex}/{@code aciklama}) listeleme uçlarında ASLA dönmez.
 *
 * <p>{@code /acikla} önceden {@code /cevap} gerektirmez; kendi-kendine çalışma aracında bilinçli
 * bir karardır — kullanıcı yalnız kendini test eder, sıra zorlaması gereksiz sürtünme olurdu.
 */
@MulakatEtkin
@RestController
@RequestMapping("/api/mulakat")
public class MulakatController {

    private final MulakatService mulakatService;
    private final MulakatAciklamaService aciklamaService;
    private final KullaniciBaglami kullaniciBaglami;

    public MulakatController(MulakatService mulakatService, MulakatAciklamaService aciklamaService,
                             KullaniciBaglami kullaniciBaglami) {
        this.mulakatService = mulakatService;
        this.aciklamaService = aciklamaService;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    @GetMapping
    public List<MulakatAlanOzeti> alanlar() {
        return mulakatService.alanlar(kullaniciBaglami.aktifKullanici());
    }

    @GetMapping("/{alanSlug}/{konuSlug}")
    public MulakatKonuGorunumu konu(@PathVariable String alanSlug, @PathVariable String konuSlug) {
        // kullaniciId zorunlu: sorular cevapsız döner ama kullanıcının KENDİ son denemeleri
        // eklenir (konu yeniden açılınca cevaplar geri boyansın) — izolasyon aktif kimlikle.
        return mulakatService.konuGetir(kullaniciBaglami.aktifKullanici(), alanSlug, konuSlug);
    }

    /**
     * {@code secilenIndex} sarmalayıcı (Integer) — alan JSON'dan eksikse Jackson primitive'i
     * sessizce 0 (=A şıkkı) yapardı; null gelip {@link #gerekliIndex} ile 400'e çevrilir.
     */
    public record CevapIstek(String soruId, Integer secilenIndex) {
    }

    @PostMapping("/cevap")
    public MulakatCevapSonucu cevap(@RequestBody CevapIstek istek) {
        return mulakatService.cevapGonder(kullaniciBaglami.aktifKullanici(),
                gerekli(istek.soruId()), gerekliIndex(istek.secilenIndex()));
    }

    @GetMapping("/tekrar")
    public List<MulakatSoruGorunumu> tekrar() {
        return mulakatService.hataliSorularTekrar(kullaniciBaglami.aktifKullanici());
    }

    @GetMapping("/dogrular")
    public List<MulakatSoruGorunumu> dogrular() {
        return mulakatService.dogruCevaplananlar(kullaniciBaglami.aktifKullanici());
    }

    @PostMapping("/acikla")
    public Map<String, String> acikla(@RequestBody CevapIstek istek) {
        return Map.of("aciklama",
                aciklamaService.acikla(gerekli(istek.soruId()), gerekliIndex(istek.secilenIndex())));
    }

    private static String gerekli(String soruId) {
        if (soruId == null || soruId.isBlank()) {
            throw new IllegalArgumentException("soruId boş olamaz");
        }
        return soruId;
    }

    private static int gerekliIndex(Integer secilenIndex) {
        if (secilenIndex == null) {
            throw new IllegalArgumentException("secilenIndex zorunludur");
        }
        return secilenIndex;
    }
}
