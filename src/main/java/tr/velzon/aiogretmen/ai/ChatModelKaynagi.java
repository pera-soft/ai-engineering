package tr.velzon.aiogretmen.ai;

import org.springframework.ai.chat.model.ChatModel;

/**
 * {@link SpringAiOgretmenClient}'in her çağrıda modeli aldığı yer (Faz 10, ADR-0017):
 * yerelde açılışta kurulan tek model (sunucu anahtarı), sitede istek başına ziyaretçinin anahtarıyla
 * kurulan model ({@link ZiyaretciChatModelKaynagi}).
 */
interface ChatModelKaynagi {

    /** Bu istekte çağrı yapılabilir mi? ({@link OgretmenAiClient#aktif()}'in anlamı.) */
    boolean hazir();

    /** @throws tr.velzon.aiogretmen.types.AnahtarGerekliException ziyaretçi anahtarı yoksa */
    ChatModel model();

    /** Hata mesajları ziyaretçinin kendi anahtarına göre mi yazılsın? (AiHataCevirici) */
    default boolean ziyaretciAnahtari() {
        return false;
    }

    /** Açılışta kurulmuş tek model — yerel mod (sunucu anahtarı). */
    static ChatModelKaynagi sabit(ChatModel model) {
        return new ChatModelKaynagi() {
            @Override
            public boolean hazir() {
                return true;
            }

            @Override
            public ChatModel model() {
                return model;
            }
        };
    }
}
