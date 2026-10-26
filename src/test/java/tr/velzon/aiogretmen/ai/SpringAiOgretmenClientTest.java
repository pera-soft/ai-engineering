package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AiKullanim;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Faz 9b: her AI çağrısının token kaydı ÇAĞIRAN kullanıcının kimliğiyle yazılır. */
class SpringAiOgretmenClientTest {

    ChatModel chatModel = mock(ChatModel.class);
    KullanimRepo kullanimRepo = mock(KullanimRepo.class);
    AppProperties props = new AppProperties(null, null, null, null, null,
            new AppProperties.Ai(new AppProperties.Model("guclu-m", "hizli-m", null)), null);

    @Test
    void kullanimKaydiAktifKullaniciyiTasir() {
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("cevap"))),
                ChatResponseMetadata.builder().usage(new DefaultUsage(10, 5)).build()));
        var client = new SpringAiOgretmenClient(chatModel, props, kullanimRepo, () -> "a@pera.software");

        client.cagir(OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                OgretmenAiClient.ModelTuru.HIZLI, "sistem", "mesaj", 100));

        ArgumentCaptor<AiKullanim> kayit = ArgumentCaptor.forClass(AiKullanim.class);
        verify(kullanimRepo).save(kayit.capture());
        assertThat(kayit.getValue().kullaniciId()).isEqualTo("a@pera.software");
        assertThat(kayit.getValue().inputTokens()).isEqualTo(10);
        assertThat(kayit.getValue().model()).isEqualTo("hizli-m");
    }

    @Test
    void kullanimKaydiSureyiVeCevaptakiModeliTasir() {
        // OpenRouter yedeğe geçtiyse cevabın bildirdiği model istenenden farklıdır — ikisi ayrı saklanır
        when(chatModel.call(any(Prompt.class))).thenAnswer(inv -> {
            Thread.sleep(20); // ölçülen süre bu gecikmeyi kapsamalı
            return new ChatResponse(List.of(new Generation(new AssistantMessage("cevap"))),
                    ChatResponseMetadata.builder().model("anthropic/yedek-model").usage(new DefaultUsage(1, 1)).build());
        });
        var client = new SpringAiOgretmenClient(chatModel, props, kullanimRepo, () -> "yerel");

        client.cagir(OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                OgretmenAiClient.ModelTuru.GUCLU, "sistem", "mesaj", 100));

        ArgumentCaptor<AiKullanim> kayit = ArgumentCaptor.forClass(AiKullanim.class);
        verify(kullanimRepo).save(kayit.capture());
        assertThat(kayit.getValue().model()).isEqualTo("guclu-m");                 // istenen
        assertThat(kayit.getValue().yanitModel()).isEqualTo("anthropic/yedek-model"); // sağlayıcının bildirdiği
        assertThat(kayit.getValue().sureMs()).isNotNull().isGreaterThanOrEqualTo(20);
    }

    @Test
    void cevaptaModelYoksaYanitModelBosKalir() {
        // metadata.getModel() varsayılanı boş metin — uydurma değer yazılmaz, NULL kalır
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
                List.of(new Generation(new AssistantMessage("cevap"))),
                ChatResponseMetadata.builder().usage(new DefaultUsage(1, 1)).build()));
        var client = new SpringAiOgretmenClient(chatModel, props, kullanimRepo, () -> "yerel");

        client.cagir(OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                OgretmenAiClient.ModelTuru.HIZLI, "sistem", "mesaj", 100));

        ArgumentCaptor<AiKullanim> kayit = ArgumentCaptor.forClass(AiKullanim.class);
        verify(kullanimRepo).save(kayit.capture());
        assertThat(kayit.getValue().yanitModel()).isNull();
        assertThat(kayit.getValue().sureMs()).isNotNull().isGreaterThanOrEqualTo(0);
    }

    @Test
    void kimlikYoksaModeleHicGidilmez() {
        var client = new SpringAiOgretmenClient(chatModel, props, kullanimRepo, () -> {
            throw new IllegalStateException("oturum yok");
        });
        assertThatThrownBy(() -> client.cagir(OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                OgretmenAiClient.ModelTuru.HIZLI, "s", "m", 10))).isInstanceOf(IllegalStateException.class);
        verify(chatModel, never()).call(any(Prompt.class)); // fail-closed: kimliksiz çağrı para harcamaz
    }
}
