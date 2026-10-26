package tr.velzon.aiogretmen.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.config.KullaniciBaglami;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AiKullanim;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenRouter üzerinden Claude (OpenAI-uyumlu API — ADR-0004).
 * Her çağrının token kullanımını ai_kullanim'a yazar (brief 7.6) — Faz 9b: çağıran kullanıcının
 * kimliğiyle. Kimlik çağrıdan ÖNCE çözülür: site profilinde oturumsuz bir yol varsa model HİÇ
 * çağrılmaz (fail-closed; kimliksiz harcama yok).
 */
public class SpringAiOgretmenClient implements OgretmenAiClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiOgretmenClient.class);
    /** schema.sql: ai_kullanim.yanit_model VARCHAR(100). */
    private static final int YANIT_MODEL_SINIRI = 100;

    private final ChatModel chatModel;
    private final AppProperties props;
    private final KullanimRepo kullanimRepo;
    private final KullaniciBaglami kullaniciBaglami;

    public SpringAiOgretmenClient(ChatModel chatModel, AppProperties props, KullanimRepo kullanimRepo,
                                  KullaniciBaglami kullaniciBaglami) {
        this.chatModel = chatModel;
        this.props = props;
        this.kullanimRepo = kullanimRepo;
        this.kullaniciBaglami = kullaniciBaglami;
    }

    @Override
    public boolean aktif() {
        return true;
    }

    @Override
    public AiYanit cagir(AiIstek istek) {
        List<Message> mesajlar = new ArrayList<>();
        mesajlar.add(new SystemMessage(istek.sistemPrompt()));
        for (GecmisMesaj g : istek.gecmis()) {
            mesajlar.add("USER".equals(g.rol()) ? new UserMessage(g.icerik()) : new AssistantMessage(g.icerik()));
        }
        mesajlar.add(new UserMessage(istek.kullaniciMesaji()));

        return calistir(istek.islem(), modelAdi(istek.modelTuru()), mesajlar, istek.maxTokens());
    }

    @Override
    public AiYanit cagirGoruntulu(Islem islem, ModelTuru tur, String sistem,
                                  String mesaj, byte[] goruntu, String mimeType, int maxTokens) {
        org.springframework.ai.content.Media media = org.springframework.ai.content.Media.builder()
                .mimeType(org.springframework.util.MimeTypeUtils.parseMimeType(mimeType))
                .data(new org.springframework.core.io.ByteArrayResource(goruntu))
                .build();
        UserMessage kullanici = UserMessage.builder().text(mesaj).media(media).build();

        return calistir(islem, modelAdi(tur), List.of(new SystemMessage(sistem), kullanici), maxTokens);
    }

    private String modelAdi(ModelTuru tur) {
        return tur == ModelTuru.GUCLU ? props.ai().model().guclu() : props.ai().model().hizli();
    }

    private AiYanit calistir(Islem islem, String model, List<Message> mesajlar, int maxTokens) {
        String kullaniciId = kullaniciBaglami.aktifKullanici(); // çağrıdan ÖNCE: kimliksiz çağrı para harcamasın
        OpenAiChatOptions secenekler = OpenAiChatOptions.builder()
                .model(model)
                .maxTokens(maxTokens)
                .build();

        ChatResponse yanit;
        // Süre: duvar saati, chatModel.call'ın TAMAMI — Spring AI'nın kendi yeniden denemeleri
        // (spring.ai.retry, 429/5xx) bu çağrının içinde olduğundan DAHİLDİR; kullanıcının beklediği
        // gerçek gecikme budur, tek denemenin süresi değil. nanoTime: saat ayarından etkilenmez.
        long baslangic = System.nanoTime();
        try {
            yanit = chatModel.call(new Prompt(mesajlar, secenekler));
        } catch (RuntimeException e) {
            // 401/402/429/5xx/zaman aşımı → AiSaglayiciException (Türkçe, ham gövdesiz); tüm AI uçları buradan geçer
            throw AiHataCevirici.cevir(e, islem.name());
        }

        // İnceleme 2. tur: content-filter/boş completion durumlarında result/output null
        // olabilir — NPE yerine anlamlı 502 (AiCiktisiBozuk) fırlatılır.
        String metin = yanit == null || yanit.getResult() == null
                || yanit.getResult().getOutput() == null ? null
                : yanit.getResult().getOutput().getText();
        if (metin == null || metin.isBlank()) {
            throw new tr.velzon.aiogretmen.types.AiCiktisiBozukException(
                    "AI boş yanıt döndürdü — lütfen tekrar deneyin.");
        }
        var kullanim = yanit.getMetadata() == null ? null : yanit.getMetadata().getUsage();
        int input = kullanim == null || kullanim.getPromptTokens() == null ? 0 : kullanim.getPromptTokens();
        int output = kullanim == null || kullanim.getCompletionTokens() == null ? 0 : kullanim.getCompletionTokens();

        int sureMs = (int) Math.min(Integer.MAX_VALUE, (System.nanoTime() - baslangic) / 1_000_000);
        String yanitModel = yanitModeli(yanit);

        kullanimRepo.save(AiKullanim.kayit(kullaniciId, islem.name(), model, input, output, sureMs, yanitModel));
        log.info("AI çağrısı: islem={} model={} yanitModel={} input={} output={} token sure={}ms",
                islem, model, yanitModel, input, output, sureMs);

        return new AiYanit(metin, input, output, model);
    }

    /**
     * Sağlayıcının cevapta bildirdiği model ({@code ChatResponseMetadata.getModel()}; OpenAI-uyumlu
     * cevabın {@code model} alanından doldurulur). Bildirilmediyse metadata boş metin döner → null
     * (uydurma değer yazılmaz). Sütun sınırını (100) aşan değer kaydı düşürmesin diye kırpılır —
     * ücreti ödenmiş çağrının kullanım satırı kaybolmamalı.
     */
    private static String yanitModeli(ChatResponse yanit) {
        String m = yanit.getMetadata() == null ? null : yanit.getMetadata().getModel();
        if (m == null || m.isBlank()) {
            return null;
        }
        return m.length() > YANIT_MODEL_SINIRI ? m.substring(0, YANIT_MODEL_SINIRI) : m;
    }
}
