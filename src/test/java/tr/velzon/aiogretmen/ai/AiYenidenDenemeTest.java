package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.ResourceAccessException;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AiSaglayiciException;
import tr.velzon.aiogretmen.types.AiSaglayiciException.Neden;

import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kısa yeniden deneme politikası GERÇEK OpenAiChatModel üzerinden (yalnız HTTP katmanı = OpenAiApi taklit):
 * geçici hata en çok N kez denenir, sonra YANIT_YOK'a çevrilir; 4xx hiç yeniden denenmez; pencere
 * dolunca (ör. uzun okuma zaman aşımı sonrası) yeni deneme başlamaz. Varsayılan Spring AI şablonu
 * (10 deneme, 3dk'ya varan bekleme) kullanıcıya hiçbir zaman mesaj göstermezdi.
 */
class AiYenidenDenemeTest {

    OpenAiApi api = mock(OpenAiApi.class);
    AppProperties props = new AppProperties(null, null, null, null, null,
            new AppProperties.Ai(new AppProperties.Model("guclu-m", "hizli-m", null)), null);

    private AiSaglayiciException cagir(int deneme, Duration pencere) {
        var model = AiYapilandirma.sohbetModeli(api,
                AiYapilandirma.yenidenDenemeSablonu(deneme, pencere, Duration.ofMillis(5)));
        var client = new SpringAiOgretmenClient(model, props, mock(KullanimRepo.class), () -> "a@pera.software");
        return catchThrowableOfType(AiSaglayiciException.class, () -> client.cagir(
                OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                        OgretmenAiClient.ModelTuru.GUCLU, "sistem", "mesaj", 100)));
    }

    @Test
    void geciciHataEnCokVarsayilanSayidaDenenirSonraYanitYok() {
        when(api.chatCompletionEntity(any(), any())).thenThrow(new TransientAiException("503 - aşırı yük"));

        AiSaglayiciException e = cagir(props.ai().denemeSayisi(), Duration.ofSeconds(30));

        assertThat(props.ai().denemeSayisi()).isEqualTo(3);
        verify(api, times(3)).chatCompletionEntity(any(), any());
        assertThat(e.neden()).isEqualTo(Neden.YANIT_YOK);
        assertThat(e.getMessage()).isEqualTo("AI şu an yanıt vermiyor — biraz sonra tekrar dene.");
    }

    @Test
    void zamanAsimiDaDenenirAyarlananSayiyaUyulur() {
        when(api.chatCompletionEntity(any(), any())).thenThrow(
                new ResourceAccessException("I/O error", new SocketTimeoutException("Connect timed out")));

        AiSaglayiciException e = cagir(2, Duration.ofSeconds(30));

        verify(api, times(2)).chatCompletionEntity(any(), any());
        assertThat(e.neden()).isEqualTo(Neden.YANIT_YOK);
    }

    @Test
    void dortyuzlukHataHicYenidenDenenmez() {
        when(api.chatCompletionEntity(any(), any())).thenThrow(new NonTransientAiException("401 - yetkisiz"));

        AiSaglayiciException e = cagir(3, Duration.ofSeconds(30));

        verify(api, times(1)).chatCompletionEntity(any(), any());
        assertThat(e.neden()).isEqualTo(Neden.ANAHTAR_GECERSIZ);
    }

    @Test
    void pencereDolduysaYeniDenemeBaslamaz() {
        // uzun süren (okuma zaman aşımı benzeri) başarısız deneme pencereyi aşar → tekrar YOK
        when(api.chatCompletionEntity(any(), any())).thenAnswer(inv -> {
            Thread.sleep(60);
            throw new ResourceAccessException("I/O error", new SocketTimeoutException("Read timed out"));
        });

        AiSaglayiciException e = cagir(3, Duration.ofMillis(20));

        verify(api, times(1)).chatCompletionEntity(any(), any());
        assertThat(e.neden()).isEqualTo(Neden.YANIT_YOK);
    }

    @Test
    void ayarYoksaYaDaGecersizseVarsayilanUc() {
        assertThat(new AppProperties.Ai(null, null).denemeSayisi()).isEqualTo(3);
        assertThat(new AppProperties.Ai(null, 0).denemeSayisi()).isEqualTo(3);
        assertThat(new AppProperties.Ai(null, 5).denemeSayisi()).isEqualTo(5);
    }
}
