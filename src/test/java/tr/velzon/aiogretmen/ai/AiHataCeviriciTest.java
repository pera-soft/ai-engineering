package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.ResourceAccessException;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AiSaglayiciException;
import tr.velzon.aiogretmen.types.AiSaglayiciException.Neden;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sağlayıcı hataları (Spring AI 1.1.8'in GERÇEKTE fırlattığı türler: RetryUtils'in hata işleyicisi
 * 4xx → NonTransientAiException("kod - gövde"), 5xx → TransientAiException; zaman aşımı →
 * ResourceAccessException) sınırda (ChatModel) taklit edilir; istemci kategoriye çevrilmiş,
 * ham gövdesiz {@link AiSaglayiciException} fırlatmalı.
 */
class AiHataCeviriciTest {

    private static final String HAM_GOVDE = "{\"error\":{\"message\":\"No auth credentials found sk-or-v1-GIZLI\",\"code\":401}}";

    ChatModel chatModel = mock(ChatModel.class);
    KullanimRepo kullanimRepo = mock(KullanimRepo.class);
    AppProperties props = new AppProperties(null, null, null, null, null,
            new AppProperties.Ai(new AppProperties.Model("guclu-m", "hizli-m", null)), null);
    SpringAiOgretmenClient client = new SpringAiOgretmenClient(chatModel, props, kullanimRepo, () -> "a@ornek.com");

    private AiSaglayiciException cagirVeYakala(RuntimeException saglayiciHatasi) {
        when(chatModel.call(any(Prompt.class))).thenThrow(saglayiciHatasi);
        return catchThrowableOfType(AiSaglayiciException.class, () -> client.cagir(
                OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                        OgretmenAiClient.ModelTuru.GUCLU, "sistem", "mesaj", 100)));
    }

    @ParameterizedTest
    @CsvSource({
            "401, ANAHTAR_GECERSIZ, AI anahtarı geçersiz — site sorumlusuna haber ver.",
            "403, ANAHTAR_GECERSIZ, AI anahtarı geçersiz — site sorumlusuna haber ver.",
            "402, KREDI_BITTI, AI kredisi bitti — site sorumlusuna haber ver.",
            "429, YOGUN, AI şu an yoğun ya da aylık kullanım sınırına ulaşıldı — biraz sonra tekrar dene.",
            "400, ISTEK_REDDEDILDI, AI isteği kabul etmedi — tekrar dene; sürerse site sorumlusuna haber ver.",
    })
    void dortyuzlukHatalarKategoriyeCevrilir(int kod, Neden beklenen, String mesaj) {
        AiSaglayiciException e = cagirVeYakala(new NonTransientAiException(kod + " - " + HAM_GOVDE));

        assertThat(e).isNotNull();
        assertThat(e.neden()).isEqualTo(beklenen);
        assertThat(e.saglayiciKodu()).isEqualTo(kod);
        assertThat(e.getMessage()).isEqualTo(mesaj).doesNotContain("sk-or").doesNotContain("error");
        verify(kullanimRepo, never()).save(any()); // başarısız çağrı kullanım kaydı yazmaz
    }

    @Test
    void besyuzlukHataYanitYokOlur() {
        AiSaglayiciException e = cagirVeYakala(new TransientAiException("503 - upstream overloaded"));
        assertThat(e.neden()).isEqualTo(Neden.YANIT_YOK);
        assertThat(e.saglayiciKodu()).isEqualTo(503);
        assertThat(e.getMessage()).isEqualTo("AI şu an yanıt vermiyor — biraz sonra tekrar dene.");
    }

    @Test
    void zamanAsimiYanitYokOlur() {
        AiSaglayiciException e = cagirVeYakala(new ResourceAccessException("I/O error on POST request",
                new SocketTimeoutException("Read timed out")));
        assertThat(e.neden()).isEqualTo(Neden.YANIT_YOK);
        assertThat(e.saglayiciKodu()).isNull();
    }

    @Test
    void sarmalanmisHataDaCevrilir() {
        AiSaglayiciException e = cagirVeYakala(new RuntimeException("sarmal",
                new NonTransientAiException("402 - " + HAM_GOVDE)));
        assertThat(e.neden()).isEqualTo(Neden.KREDI_BITTI);
    }

    @Test
    void saglayiciDisiHataAynenGecer() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("başka"));
        IllegalStateException e = catchThrowableOfType(IllegalStateException.class, () -> client.cagir(
                OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                        OgretmenAiClient.ModelTuru.GUCLU, "s", "m", 10)));
        assertThat(e).hasMessage("başka");
    }

    /** Faz 10: ziyaretçinin kendi anahtarı — mesaj "site sorumlusuna" değil, ziyaretçinin anahtarına göre. */
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "401| ANAHTAR_GECERSIZ| OpenRouter anahtarın geçersiz ya da iptal edilmiş.",
            "403| ANAHTAR_GECERSIZ| OpenRouter anahtarın geçersiz ya da iptal edilmiş.",
            "402| KREDI_BITTI| OpenRouter kredin bitti ya da anahtarının harcama limitine ulaştın.",
            "429| YOGUN| OpenRouter çok fazla istek dedi, biraz bekleyip tekrar dene.",
            "400| ISTEK_REDDEDILDI| AI isteği kabul etmedi — tekrar dene; sürerse site sorumlusuna haber ver.",
    })
    void ziyaretciAnahtarindaMesajlarZiyaretciye(int kod, Neden beklenen, String mesaj) {
        ChatModelKaynagi ziyaretciKaynagi = new ChatModelKaynagi() {
            @Override public boolean hazir() { return true; }
            @Override public ChatModel model() { return chatModel; }
            @Override public boolean ziyaretciAnahtari() { return true; }
        };
        var ziyaretciClient = new SpringAiOgretmenClient(ziyaretciKaynagi, props, kullanimRepo, () -> "z-x");
        when(chatModel.call(any(Prompt.class))).thenThrow(new NonTransientAiException(kod + " - " + HAM_GOVDE));

        AiSaglayiciException e = catchThrowableOfType(AiSaglayiciException.class, () -> ziyaretciClient.cagir(
                OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                        OgretmenAiClient.ModelTuru.GUCLU, "s", "m", 10)));

        assertThat(e.neden()).isEqualTo(beklenen);
        assertThat(e.ziyaretciAnahtari()).isTrue();
        assertThat(e.getMessage()).isEqualTo(mesaj).doesNotContain("sk-or");
        // ham sağlayıcı gövdesi (anahtarı yansıtabilir) istisna zincirinde TAŞINMAZ — yalnız HTTP kodu kalır
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            assertThat(String.valueOf(t.getMessage())).as(t.getClass().getName()).doesNotContain("sk-or-");
            assertThat(t.toString()).doesNotContain("sk-or-");
        }
    }

    /** Yerel mod (sunucu anahtarı) değişmedi: teşhis için asıl sağlayıcı istisnası cause olarak kalır. */
    @Test
    void yerelModdaAsilHataCauseOlarakKalir() {
        NonTransientAiException asil = new NonTransientAiException("401 - " + HAM_GOVDE);
        AiSaglayiciException e = cagirVeYakala(asil);
        assertThat(e.ziyaretciAnahtari()).isFalse();
        assertThat(e.getCause()).isSameAs(asil);
    }
}
