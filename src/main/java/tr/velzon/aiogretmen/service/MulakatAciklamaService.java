package tr.velzon.aiogretmen.service;

import tr.velzon.aiogretmen.config.MulakatEtkin;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.MulakatRepo;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.MulakatSoru;
import tr.velzon.aiogretmen.types.MulakatSoruBulgusu;

import java.util.Map;

/**
 * Mülakat sonuç-açıklayıcı (AI): öğrencinin seçtiği şıkka göre "neden doğru/yanlış" açıklar.
 * SOKRATİK DEĞİL — yeni soru sormaz, doğrudan gerekçeyi verir (mulakat-acikla.st).
 *
 * <p>Kanonik soru SUNUCUDAN alınır ({@link MulakatRepo#soruBul}): istemci hangi şıkkın doğru
 * olduğunu değil, yalnız {@code soruId}+{@code secilenIndex} gönderir; doğru şık ve açıklama
 * sunucuda belirlenir. Düz metin döner (JSON parse YOK — OrnekService deseni). Ucuz model yeterli.
 */
@MulakatEtkin
@Service
@LlmKullanan
public class MulakatAciklamaService {

    static final int MAX_TOKENS = 700;

    private final OgretmenAiClient aiClient;
    private final MulakatRepo mulakatRepo;
    private final PromptFactory promptFactory;

    public MulakatAciklamaService(OgretmenAiClient aiClient, MulakatRepo mulakatRepo,
                                  PromptFactory promptFactory) {
        this.aiClient = aiClient;
        this.mulakatRepo = mulakatRepo;
        this.promptFactory = promptFactory;
    }

    /**
     * Öğrencinin verdiği cevabı sonuç-açıklayıcı bir metinle gerekçelendirir.
     *
     * @throws BulunamadiException soru id'si yoksa (web → 404)
     * @throws IllegalArgumentException {@code secilenIndex} aralık dışıysa (web → 400)
     */
    public String acikla(String soruId, int secilenIndex) {
        MulakatSoruBulgusu bulgu = mulakatRepo.soruBul(soruId)
                .orElseThrow(() -> new BulunamadiException("Soru bulunamadı: " + soruId));
        MulakatSoru soru = bulgu.soru();
        if (secilenIndex < 0 || secilenIndex >= soru.siklar().size()) {
            throw new IllegalArgumentException(
                    "Seçilen şık aralık dışı: " + secilenIndex + " (0.." + (soru.siklar().size() - 1) + ")");
        }
        boolean dogru = secilenIndex == soru.dogruIndex();
        String secilenMetin = soru.siklar().get(secilenIndex).metin();
        String dogruMetin = soru.siklar().get(soru.dogruIndex()).metin();

        String sistem = promptFactory.doldur("mulakat-acikla", Map.of(
                "soru", soru.soru(),
                "secilen", secilenMetin,
                "dogru", dogruMetin,
                "sonuc", dogru ? "DOĞRU" : "YANLIŞ",
                "aciklama", soru.aciklama()));

        var yanit = aiClient.cagir(AiIstek.tekSeferlik(Islem.MULAKAT, ModelTuru.HIZLI, sistem,
                "Öğrencinin cevabını yukarıdaki kurallara göre açıkla.", MAX_TOKENS));
        return yanit.metin();
    }
}
