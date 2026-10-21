package tr.velzon.aiogretmen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.JsonCevapParser;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.GecmisMesaj;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.GonderimRepo;
import tr.velzon.aiogretmen.types.Degerlendirme;
import tr.velzon.aiogretmen.types.Gonderim;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ana özellik (brief 7.2): kullanıcı gönderimini rubrikle değerlendirir.
 * Bozuk JSON'da 1 düzeltme çağrısı; yine bozuksa puan=null + ham metin (çökmez).
 */
@Service
@LlmKullanan
public class DegerlendirmeService {

    static final int MAX_TOKENS = 2000;
    /** Görev metni üst sınırı: prompt'u şişirmesin diye kırpılır (geriye-uyumlu, opsiyonel alan). */
    static final int MAX_GOREV = 2000;

    private final OgretmenAiClient aiClient;
    private final BaglamServisi baglamServisi;
    private final PromptFactory promptFactory;
    private final JsonCevapParser parser;
    private final GonderimRepo gonderimRepo;
    private final HafizaService hafizaService;
    private final KullaniciBaglami kullaniciBaglami;
    private final ObjectMapper mapper = new ObjectMapper();

    public DegerlendirmeService(OgretmenAiClient aiClient, BaglamServisi baglamServisi,
                                PromptFactory promptFactory, JsonCevapParser parser,
                                GonderimRepo gonderimRepo, HafizaService hafizaService,
                                KullaniciBaglami kullaniciBaglami) {
        this.aiClient = aiClient;
        this.baglamServisi = baglamServisi;
        this.promptFactory = promptFactory;
        this.parser = parser;
        this.gonderimRepo = gonderimRepo;
        this.hafizaService = hafizaService;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    public record Sonuc(long gonderimId, Degerlendirme degerlendirme) {
    }

    /** Geriye-uyumlu sarmalayıcı: görev bağlamı olmadan (eski çağıranlar için). */
    public Sonuc degerlendir(int hafta, Integer gun, String tur, String icerik) {
        return degerlendir(hafta, gun, tur, icerik, null);
    }

    public Sonuc degerlendir(int hafta, Integer gun, String tur, String icerik, String gorevMetni) {
        Gonderim gonderim = gonderimRepo.save(
                Gonderim.yeni(kullaniciBaglami.aktifKullanici(), hafta, gun, tur, icerik));

        String sistem = baglamServisi.sistemBaglami(hafta, gun);
        String istekMetni = promptFactory.doldur("degerlendirme",
                Map.of("tur", tur, "icerik", icerik, "gorev", gorevBolumu(gorevMetni)));

        var ilkYanit = aiClient.cagir(AiIstek.tekSeferlik(
                Islem.DEGERLENDIRME, ModelTuru.GUCLU, sistem, istekMetni, MAX_TOKENS));

        Optional<Degerlendirme> sonuc = parser.parse(ilkYanit.metin(), Degerlendirme.class);

        if (sonuc.isEmpty()) {
            // brief 7.2: 1 kez düzeltme çağrısı
            var duzeltme = aiClient.cagir(new AiIstek(
                    Islem.DEGERLENDIRME, ModelTuru.GUCLU, sistem,
                    List.of(new GecmisMesaj("USER", istekMetni),
                            new GecmisMesaj("ASSISTANT", ilkYanit.metin())),
                    "Cevabın geçerli JSON değildi. Aynı değerlendirmeyi YALNIZCA geçerli JSON olarak, "
                    + "şemaya birebir uyarak tekrar döndür. JSON dışında hiçbir şey yazma.",
                    MAX_TOKENS));
            sonuc = parser.parse(duzeltme.metin(), Degerlendirme.class);
            if (sonuc.isEmpty()) {
                sonuc = Optional.of(Degerlendirme.hamMetinden(duzeltme.metin())); // zarif düşüş
            }
        }

        Degerlendirme d = sonuc.get();
        gonderim = gonderimRepo.save(gonderim.degerlendirildi(jsonYaz(d), d.puan()));

        hafizaService.guncelle(hafta, "DEGERLENDIRME",
                "Öğrencinin gönderimi (%s):\n%s\n\nÖğretmen değerlendirmesi:\n%s".formatted(tur, icerik, jsonYaz(d)));

        return new Sonuc(gonderim.id(), d);
    }

    /**
     * Görev bağlamı bölümü: NON-BLANK ise AI'ın değerlendirmeyi öğrencinin cevapladığı
     * göreve göre yapması için başlıklı bölüm; boşsa "" (şablonda hiç görünmez).
     */
    private static String gorevBolumu(String gorevMetni) {
        if (gorevMetni == null || gorevMetni.isBlank()) {
            return "";
        }
        String metin = gorevMetni.strip();
        if (metin.length() > MAX_GOREV) {
            metin = metin.substring(0, MAX_GOREV);
        }
        return "## Öğrencinin cevapladığı görev\n" + metin + "\n"
                + "Değerlendirmeni bu göreve göre yap; metni soyut bir özet gibi değil, "
                + "görevin karşılığı olarak ele al.\n";
    }

    private String jsonYaz(Degerlendirme d) {
        try {
            return mapper.writeValueAsString(d);
        } catch (Exception e) {
            return "{}";
        }
    }
}
