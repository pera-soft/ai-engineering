package tr.velzon.aiogretmen.web;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.velzon.aiogretmen.service.DegerlendirmeService;
import tr.velzon.aiogretmen.service.OrnekService;
import tr.velzon.aiogretmen.service.QuizUretimService;
import tr.velzon.aiogretmen.service.RaporService;
import tr.velzon.aiogretmen.types.UretilenQuiz;

import java.util.Map;

import static tr.velzon.aiogretmen.web.OgretmenController.haftaGerekli;
import static tr.velzon.aiogretmen.web.OgretmenController.sinirla;

/**
 * Sunucu anahtarıyla çalışan, sohbet dışı AI uçları: değerlendirme, örnek, açıklama, quiz üretimi, rapor.
 * Faz 10 (ADR-0017): {@code site} profilinde YOK (istek → JSON 404, {@link HataYonetimi}) — girişsiz sitede
 * sunucu anahtarı yalnız sohbete ayrılır. Yerelde yollar/gövdeler eskisiyle aynıdır.
 */
@RestController
@Profile("!site")
@RequestMapping("/api")
public class OgretmenAiOzellikController {

    private final DegerlendirmeService degerlendirmeService;
    private final QuizUretimService quizUretimService;
    private final RaporService raporService;
    private final OrnekService ornekService;

    public OgretmenAiOzellikController(DegerlendirmeService degerlendirmeService,
                                       QuizUretimService quizUretimService,
                                       RaporService raporService, OrnekService ornekService) {
        this.degerlendirmeService = degerlendirmeService;
        this.quizUretimService = quizUretimService;
        this.raporService = raporService;
        this.ornekService = ornekService;
    }

    /** gorevMetni opsiyoneldir (geriye-uyumlu): gelmezse null olur, eski davranış korunur. */
    public record DegerlendirIstek(Integer hafta, Integer gun, String tur, String icerik, String gorevMetni) {
    }

    @PostMapping("/degerlendir")
    public Map<String, Object> degerlendir(@RequestBody DegerlendirIstek istek) {
        String tur = istek.tur() == null || istek.tur().isBlank() ? "OZET" : istek.tur();
        var sonuc = degerlendirmeService.degerlendir(haftaGerekli(istek.hafta()), istek.gun(), tur,
                sinirla("icerik", istek.icerik()), istek.gorevMetni());
        return Map.of("gonderimId", sonuc.gonderimId(), "degerlendirme", sonuc.degerlendirme());
    }

    public record OrnekIstek(Integer hafta, Integer gun, String gorevMetni) {
    }

    /** "Örnek göster": Sokratik sohbetten AYRI — reddetmez, görev için somut örnek taslak verir. */
    @PostMapping("/ornek")
    public Map<String, Object> ornek(@RequestBody OrnekIstek istek) {
        return Map.of("ornek", ornekService.ornekUret(haftaGerekli(istek.hafta()), istek.gun(),
                sinirla("gorevMetni", istek.gorevMetni())));
    }

    /** "Soruyu aç": görevi sade Türkçeyle açar + terimleri tanımlar (CEVAP/ÖRNEK vermez). */
    @PostMapping("/aciklama")
    public Map<String, Object> aciklama(@RequestBody OrnekIstek istek) {
        return Map.of("aciklama", ornekService.aciklaUret(haftaGerekli(istek.hafta()), istek.gun(),
                sinirla("gorevMetni", istek.gorevMetni())));
    }

    public record QuizUretIstek(Integer hafta, Integer adet) {
    }

    @PostMapping("/quiz/uret")
    public UretilenQuiz quizUret(@RequestBody QuizUretIstek istek) {
        return quizUretimService.uret(haftaGerekli(istek.hafta()), istek.adet());
    }

    /** GET yalnızca okur/ilk üretir; YENİLEME yan etkili olduğu için POST (inceleme bulgusu #3). */
    @GetMapping("/rapor/{hafta}")
    public RaporService.Rapor rapor(@PathVariable int hafta) {
        return raporService.rapor(hafta, false);
    }

    @PostMapping("/rapor/{hafta}/yenile")
    public RaporService.Rapor raporYenile(@PathVariable int hafta) {
        return raporService.rapor(hafta, true);
    }
}
