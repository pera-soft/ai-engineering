package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.repo.AtolyeRepo;
import tr.velzon.aiogretmen.types.AtolyeAlistirma;
import tr.velzon.aiogretmen.types.AtolyeDegerlendirme;
import tr.velzon.aiogretmen.types.AtolyeKategori;
import tr.velzon.aiogretmen.types.BulunamadiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prompt Atölyesi (Faz A): kategori ağacını sunar ve öğrencinin yazdığı prompt'u AI Öğretmen'e
 * rubrikle değerlendirtir. AI çağrısı YALNIZ ai/ paketindeki OgretmenAiClient üzerinden yapılır;
 * token kullanımı çağrı içinde ai_kullanim'a yazılır (SpringAiOgretmenClient).
 *
 * <p>Anahtar yoksa {@link OgretmenAiClient#cagir} AiDevreDisiException fırlatır → web katmanı 503;
 * statik akış (kategoriler, ideal çözüm) anahtarsız da çalışır.
 */
@Service
@LlmKullanan
public class AtolyeService {

    /** Değerlendirme kısa ve odaklı — atölye "az token" hedefi (brief 2.). */
    static final int MAX_TOKENS = 800;

    private final AtolyeRepo atolyeRepo;
    private final OgretmenAiClient aiClient;
    private final PromptFactory promptFactory;
    private final JsonCevapParser parser;

    public AtolyeService(AtolyeRepo atolyeRepo, OgretmenAiClient aiClient,
                         PromptFactory promptFactory, JsonCevapParser parser) {
        this.atolyeRepo = atolyeRepo;
        this.aiClient = aiClient;
        this.promptFactory = promptFactory;
        this.parser = parser;
    }

    public List<AtolyeKategori> kategoriler() {
        return atolyeRepo.kategoriler();
    }

    /**
     * Öğrencinin bir alıştırma için yazdığı prompt'u değerlendirir.
     *
     * @throws BulunamadiException alıştırma id'si yoksa (web → 404)
     * @throws tr.velzon.aiogretmen.types.AiDevreDisiException anahtar yoksa (web → 503)
     */
    public AtolyeDegerlendirme degerlendir(String alistirmaId, String ogrenciPrompt) {
        AtolyeAlistirma a = alistirmaBul(alistirmaId)
                .orElseThrow(() -> new BulunamadiException("Alıştırma bulunamadı: " + alistirmaId));

        // Atölye haftadan bağımsız — sistem kimliği nötr bağlamla doldurulur (hafta/gün yok).
        String sistem = promptFactory.doldur("sistem-kimlik", Map.of(
                "hafta_icerik", "(Prompt Atölyesi — belirli bir haftaya bağlı değil, serbest pratik)",
                "gun_icerik", "(atölye)",
                "ilerleme_ozeti", "(atölye: öğrenci teknikleri deneyerek öğreniyor)",
                "hafiza", "",
                "kitaplar", "",
                "kaynaklar", ""));

        String istek = promptFactory.doldur("prompt-degerlendirme", Map.of(
                "teknik", a.teknik(),
                "gorev", a.gorev(),
                "ideal_cozum", a.idealCozum(),
                "neden", a.neden(),
                "ogrenci_prompt", ogrenciPrompt));

        var yanit = aiClient.cagir(AiIstek.tekSeferlik(
                Islem.DEGERLENDIRME, ModelTuru.GUCLU, sistem, istek, MAX_TOKENS));

        // Bozuk JSON → ham metinle zarif düşüş (DegerlendirmeService deseni; puan=null).
        return parser.parse(yanit.metin(), AtolyeDegerlendirme.class)
                .orElseGet(() -> AtolyeDegerlendirme.hamMetinden(yanit.metin()));
    }

    private Optional<AtolyeAlistirma> alistirmaBul(String id) {
        return atolyeRepo.kategoriler().stream()
                .flatMap(k -> k.alistirmalar().stream())
                .filter(a -> a.id().equals(id))
                .findFirst();
    }
}
