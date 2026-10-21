package tr.velzon.aiogretmen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.service.DegerlendirmeService;
import tr.velzon.aiogretmen.service.KullanimService;
import tr.velzon.aiogretmen.service.SohbetService;
import tr.velzon.aiogretmen.types.SohbetMesaji;

import java.util.List;
import java.util.Map;

/** AI öğretmen uçları (brief §8): değerlendirme, sohbet, kullanım istatistiği. */
@RestController
@RequestMapping("/api")
public class OgretmenController {

    private final DegerlendirmeService degerlendirmeService;
    private final SohbetService sohbetService;
    private final KullanimService kullanimService;
    private final tr.velzon.aiogretmen.service.QuizUretimService quizUretimService;
    private final tr.velzon.aiogretmen.service.RaporService raporService;
    private final tr.velzon.aiogretmen.service.OrnekService ornekService;

    public OgretmenController(DegerlendirmeService degerlendirmeService,
                              SohbetService sohbetService, KullanimService kullanimService,
                              tr.velzon.aiogretmen.service.QuizUretimService quizUretimService,
                              tr.velzon.aiogretmen.service.RaporService raporService,
                              tr.velzon.aiogretmen.service.OrnekService ornekService) {
        this.degerlendirmeService = degerlendirmeService;
        this.sohbetService = sohbetService;
        this.kullanimService = kullanimService;
        this.quizUretimService = quizUretimService;
        this.raporService = raporService;
        this.ornekService = ornekService;
    }

    /** Girdi üst sınırı (inceleme bulgusu): dev yapıştırma → context aşımı → anlamsız 500 olmasın. */
    static final int MAX_GIRDI = 20_000;

    private static String sinirla(String alanAdi, String deger) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz");
        }
        if (deger.length() > MAX_GIRDI) {
            throw new IllegalArgumentException(
                    alanAdi + " çok uzun (%d karakter) — üst sınır %d. Metni bölerek gönder."
                            .formatted(deger.length(), MAX_GIRDI));
        }
        return deger;
    }

    /** gorevMetni opsiyoneldir (geriye-uyumlu): gelmezse null olur, eski davranış korunur. */
    public record DegerlendirIstek(int hafta, Integer gun, String tur, String icerik, String gorevMetni) {
    }

    @PostMapping("/degerlendir")
    public Map<String, Object> degerlendir(@RequestBody DegerlendirIstek istek) {
        String tur = istek.tur() == null || istek.tur().isBlank() ? "OZET" : istek.tur();
        var sonuc = degerlendirmeService.degerlendir(istek.hafta(), istek.gun(), tur,
                sinirla("icerik", istek.icerik()), istek.gorevMetni());
        return Map.of("gonderimId", sonuc.gonderimId(), "degerlendirme", sonuc.degerlendirme());
    }

    public record SohbetIstek(int hafta, Integer gun, String mesaj) {
    }

    @PostMapping("/sohbet")
    public SohbetMesaji sohbet(@RequestBody SohbetIstek istek) {
        return sohbetService.mesajGonder(istek.hafta(), istek.gun(), sinirla("mesaj", istek.mesaj()));
    }

    @GetMapping("/sohbet/{hafta}")
    public List<SohbetMesaji> sohbetGecmisi(@PathVariable int hafta) {
        return sohbetService.gecmis(hafta);
    }

    public record OrnekIstek(int hafta, Integer gun, String gorevMetni) {
    }

    /** "Örnek göster": Sokratik sohbetten AYRI — reddetmez, görev için somut örnek taslak verir. */
    @PostMapping("/ornek")
    public Map<String, Object> ornek(@RequestBody OrnekIstek istek) {
        return Map.of("ornek", ornekService.ornekUret(istek.hafta(), istek.gun(),
                sinirla("gorevMetni", istek.gorevMetni())));
    }

    /** "Soruyu aç": görevi sade Türkçeyle açar + terimleri tanımlar (CEVAP/ÖRNEK vermez). */
    @PostMapping("/aciklama")
    public Map<String, Object> aciklama(@RequestBody OrnekIstek istek) {
        return Map.of("aciklama", ornekService.aciklaUret(istek.hafta(), istek.gun(),
                sinirla("gorevMetni", istek.gorevMetni())));
    }

    @GetMapping("/kullanim")
    public Map<String, Object> kullanim() {
        return kullanimService.istatistik();
    }

    public record QuizUretIstek(int hafta, Integer adet) {
    }

    @PostMapping("/quiz/uret")
    public tr.velzon.aiogretmen.types.UretilenQuiz quizUret(@RequestBody QuizUretIstek istek) {
        return quizUretimService.uret(istek.hafta(), istek.adet());
    }

    /** GET yalnızca okur/ilk üretir; YENİLEME yan etkili olduğu için POST (inceleme bulgusu #3). */
    @GetMapping("/rapor/{hafta}")
    public tr.velzon.aiogretmen.service.RaporService.Rapor rapor(@PathVariable int hafta) {
        return raporService.rapor(hafta, false);
    }

    @PostMapping("/rapor/{hafta}/yenile")
    public tr.velzon.aiogretmen.service.RaporService.Rapor raporYenile(@PathVariable int hafta) {
        return raporService.rapor(hafta, true);
    }
}
