package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.ContentRepo;
import tr.velzon.aiogretmen.types.AiCiktisiBozukException;
import tr.velzon.aiogretmen.types.BulunamadiException;
import tr.velzon.aiogretmen.types.Hafta;
import tr.velzon.aiogretmen.types.QuizSorusu;
import tr.velzon.aiogretmen.types.UretilenQuiz;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AI quiz üretimi (brief 7.4): haftanın TÜM gün içeriklerinden, statik quizde
 * OLMAYAN yeni sorular. Hizli model; sonuç frontend'te statik quizle aynı arayüzde.
 */
@Service
@LlmKullanan
public class QuizUretimService {

    static final int MAX_TOKENS = 3000;
    static final int VARSAYILAN_ADET = 5;

    private final OgretmenAiClient aiClient;
    private final ContentRepo contentRepo;
    private final PromptFactory promptFactory;
    private final JsonCevapParser parser;

    public QuizUretimService(OgretmenAiClient aiClient, ContentRepo contentRepo,
                             PromptFactory promptFactory, JsonCevapParser parser) {
        this.aiClient = aiClient;
        this.contentRepo = contentRepo;
        this.promptFactory = promptFactory;
        this.parser = parser;
    }

    public UretilenQuiz uret(int haftaNo, Integer adet) {
        Hafta hafta = contentRepo.hafta(haftaNo)
                .orElseThrow(() -> new BulunamadiException("Hafta bulunamadı: " + haftaNo));

        String tumGunler = IntStream.rangeClosed(1, hafta.gunler().size())
                .mapToObj(g -> contentRepo.hamGun(haftaNo, g))
                .collect(Collectors.joining("\n\n---\n\n"));

        String mevcutSorular = hafta.quiz().stream()
                .map(QuizSorusu::soru)
                .map(s -> "- " + s)
                .collect(Collectors.joining("\n"));

        String prompt = promptFactory.doldur("quiz-uret", Map.of(
                "adet", String.valueOf(adet == null || adet < 1 ? VARSAYILAN_ADET : Math.min(adet, 10)),
                "mevcut_sorular", mevcutSorular,
                "hafta_icerik", tumGunler));

        String sistem = "Sen bir yazılım mimarlığı eğitmenisin. Yalnızca geçerli JSON döndürürsün.";
        var yanit = aiClient.cagir(AiIstek.tekSeferlik(Islem.QUIZ, ModelTuru.HIZLI, sistem, prompt, MAX_TOKENS));

        var sonuc = parser.parse(yanit.metin(), UretilenQuiz.class);
        if (sonuc.isEmpty()) {
            // İnceleme 2. tur: kesilen JSON tüm partiyi çöpe atıyordu — brief 7.2 deseniyle
            // 1 düzeltme çağrısı (DegerlendirmeService ile tutarlı).
            var duzeltme = aiClient.cagir(new AiIstek(Islem.QUIZ, ModelTuru.HIZLI, sistem,
                    java.util.List.of(new OgretmenAiClient.GecmisMesaj("USER", prompt),
                            new OgretmenAiClient.GecmisMesaj("ASSISTANT", yanit.metin())),
                    "Cevabın geçerli/tam JSON değildi. Aynı soruları YALNIZCA geçerli JSON olarak, "
                    + "şemaya birebir uyarak yeniden döndür. Gerekirse soru sayısını azalt ama JSON'u tamamla.",
                    MAX_TOKENS));
            sonuc = parser.parse(duzeltme.metin(), UretilenQuiz.class);
        }
        UretilenQuiz quiz = sonuc
                .orElseThrow(() -> new AiCiktisiBozukException("Quiz üretilemedi — lütfen tekrar deneyin."));

        // asgari doğrulama: eksik alanlı/bozuk soruları at
        var gecerli = quiz.sorular().stream()
                .filter(s -> s.soru() != null && !s.soru().isBlank())
                .filter(s -> s.secenekler() != null && s.secenekler().size() == 4)
                .filter(s -> s.dogruIndex() != null && s.dogruIndex() >= 0 && s.dogruIndex() < 4)
                .toList();
        if (gecerli.isEmpty()) {
            throw new AiCiktisiBozukException("Quiz üretilemedi (geçerli soru yok) — lütfen tekrar deneyin.");
        }
        return new UretilenQuiz(gecerli);
    }
}
