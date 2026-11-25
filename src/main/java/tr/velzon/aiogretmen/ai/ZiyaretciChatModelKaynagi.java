package tr.velzon.aiogretmen.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.retry.support.RetryTemplate;
import tr.velzon.aiogretmen.config.AiAnahtarBaglami;
import tr.velzon.aiogretmen.types.AnahtarGerekliException;

/**
 * Sitede sohbet modeli ziyaretçinin kendi OpenRouter anahtarıyla, İSTEK BAŞINA kurulur (Faz 10, ADR-0017).
 * Önbellek YOK: anahtar istek bitince bellekte tutulmaz. Sunucu anahtarı ({@code spring.ai.openai.api-key})
 * burada hiç okunmaz — başlık yoksa {@link AnahtarGerekliException}, çağrı yapılmaz.
 *
 * <p>Zaman aşımı ve yeniden deneme yerel modla aynı ({@link AiYapilandirma}); 4xx (401/402/429) denenmez.
 */
final class ZiyaretciChatModelKaynagi implements ChatModelKaynagi {

    private final AiAnahtarBaglami anahtarBaglami;
    private final String baseUrl;
    private final RetryTemplate yenidenDeneme;

    ZiyaretciChatModelKaynagi(AiAnahtarBaglami anahtarBaglami, String baseUrl, int denemeSayisi) {
        this.anahtarBaglami = anahtarBaglami;
        this.baseUrl = baseUrl;
        this.yenidenDeneme = AiYapilandirma.yenidenDenemeSablonu(denemeSayisi,
                AiYapilandirma.YENIDEN_DENEME_PENCERESI, AiYapilandirma.ILK_BEKLEME);
    }

    @Override
    public boolean hazir() {
        return anahtarBaglami.anahtar().isPresent();
    }

    @Override
    public ChatModel model() {
        String anahtar = anahtarBaglami.anahtar().orElseThrow(AnahtarGerekliException::new);
        OpenAiApi api = AiYapilandirma.sohbetApisi(baseUrl, new GizliAnahtar(anahtar));
        return AiYapilandirma.sohbetModeli(api, yenidenDeneme);
    }

    @Override
    public boolean ziyaretciAnahtari() {
        return true;
    }

    /** {@link ApiKey}: değer yalnız {@code getValue()} ile (Authorization başlığı); {@code toString()} maskeli. */
    private static final class GizliAnahtar implements ApiKey {

        private final String deger;

        private GizliAnahtar(String deger) {
            this.deger = deger;
        }

        @Override
        public String getValue() {
            return deger;
        }

        @Override
        public String toString() {
            return "GizliAnahtar[***]";
        }
    }
}
