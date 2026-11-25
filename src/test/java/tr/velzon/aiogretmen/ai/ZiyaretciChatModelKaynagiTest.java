package tr.velzon.aiogretmen.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import tr.velzon.aiogretmen.config.AppProperties;
import tr.velzon.aiogretmen.repo.KullanimRepo;
import tr.velzon.aiogretmen.types.AnahtarGerekliException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Faz 10 (ADR-0017): sitede sohbet modeli istek başına ziyaretçinin anahtarıyla kurulur; anahtar yoksa
 * model KURULMAZ ve çağrı yapılmaz (sunucu anahtarına düşmek yok).
 */
class ZiyaretciChatModelKaynagiTest {

    private static final String URL = "http://127.0.0.1:9";

    @Test
    void anahtarYokkenHazirDegil() {
        var kaynak = new ZiyaretciChatModelKaynagi(Optional::empty, URL, 1);
        assertThat(kaynak.hazir()).isFalse();
        assertThat(kaynak.ziyaretciAnahtari()).isTrue();
    }

    @Test
    void anahtarYokkenModelAnahtarGerekli() {
        var kaynak = new ZiyaretciChatModelKaynagi(Optional::empty, URL, 1);
        assertThatThrownBy(kaynak::model).isInstanceOf(AnahtarGerekliException.class)
                .hasMessage("AI öğretmenle sohbet için kendi OpenRouter anahtarını eklemelisin.");
    }

    @Test
    void anahtarVarkenHerCagridaYeniModel() {
        var kaynak = new ZiyaretciChatModelKaynagi(() -> Optional.of("sk-or-v1-abcdef0123456789"), URL, 1);
        assertThat(kaynak.hazir()).isTrue();
        ChatModel ilk = kaynak.model();
        assertThat(ilk).isNotNull().isNotSameAs(kaynak.model()); // anahtar başına önbellek YOK
        assertThat(ilk.toString()).doesNotContain("abcdef0123456789");
    }

    @Test
    void istemciAnahtarYokkenKullanimYazmazVeAktifDegil() {
        KullanimRepo repo = mock(KullanimRepo.class);
        AppProperties props = new AppProperties(null, null, null, null, null,
                new AppProperties.Ai(new AppProperties.Model("guclu-m", "hizli-m", null)), null);
        var client = new SpringAiOgretmenClient(new ZiyaretciChatModelKaynagi(Optional::empty, URL, 1),
                props, repo, () -> "z-x");

        assertThat(client.aktif()).isFalse();
        assertThatThrownBy(() -> client.cagir(OgretmenAiClient.AiIstek.tekSeferlik(OgretmenAiClient.Islem.SOHBET,
                OgretmenAiClient.ModelTuru.GUCLU, "s", "m", 10))).isInstanceOf(AnahtarGerekliException.class);
        verify(repo, never()).save(any());
    }
}
