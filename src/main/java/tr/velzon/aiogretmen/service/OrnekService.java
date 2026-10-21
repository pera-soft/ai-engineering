package tr.velzon.aiogretmen.service;

import org.springframework.stereotype.Service;
import tr.velzon.aiogretmen.ai.OgretmenAiClient;
import tr.velzon.aiogretmen.config.LlmKullanan;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.AiIstek;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.ModelTuru;
import tr.velzon.aiogretmen.ai.OgretmenAiClient.Islem;
import tr.velzon.aiogretmen.ai.PromptFactory;

import java.util.Map;

/**
 * "Örnek göster" (kullanıcı eklemesi): yazma görevleri için SOMUT örnek taslak üretir.
 * Sokratik sohbetten AYRI prompt (ornek.st) kullanır — reddetmez, doğrudan örnek verir.
 * Ucuz model (HIZLI) yeterli; kalıcı kayıt tutmaz.
 */
@Service
@LlmKullanan
public class OrnekService {

    static final int MAX_TOKENS = 700;
    /** Açıklama örnekten daha uzun olabilir (terim tanımları + adımlar) — cevap kesilmesin. */
    static final int ACIKLAMA_MAX_TOKENS = 1300;

    private final OgretmenAiClient aiClient;
    private final BaglamServisi baglamServisi;
    private final PromptFactory promptFactory;

    public OrnekService(OgretmenAiClient aiClient, BaglamServisi baglamServisi,
                        PromptFactory promptFactory) {
        this.aiClient = aiClient;
        this.baglamServisi = baglamServisi;
        this.promptFactory = promptFactory;
    }

    /** Görev için somut örnek taslak. Sistem = hafta bağlamı (kimlik+ders) + ornek.st (yardımcı, reddetmez). */
    public String ornekUret(int hafta, Integer gun, String gorevMetni) {
        return uret("ornek", Islem.ORNEK, hafta, gun, gorevMetni, MAX_TOKENS);
    }

    /** "Soruyu aç": görevi/soruyu SADE Türkçeyle açar, terimleri tanımlar; CEVAP/ÖRNEK vermez (aciklama.st). */
    public String aciklaUret(int hafta, Integer gun, String gorevMetni) {
        return uret("aciklama", Islem.ACIKLAMA, hafta, gun, gorevMetni, ACIKLAMA_MAX_TOKENS);
    }

    private String uret(String sablon, Islem islem, int hafta, Integer gun, String gorevMetni, int maxTokens) {
        String sistem = baglamServisi.sistemBaglami(hafta, gun)
                + "\n\n" + promptFactory.doldur(sablon, Map.of());
        String gorev = gorevMetni == null ? "" : gorevMetni.strip();
        var yanit = aiClient.cagir(AiIstek.tekSeferlik(islem, ModelTuru.HIZLI, sistem, "Görev: " + gorev, maxTokens));
        return yanit.metin();
    }
}
