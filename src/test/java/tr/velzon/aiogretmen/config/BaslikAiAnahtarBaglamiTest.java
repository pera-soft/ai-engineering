package tr.velzon.aiogretmen.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Faz 10 (ADR-0017): sitede ziyaretçinin OpenRouter anahtarı yalnız istekteki {@code X-OpenRouter-Anahtar}
 * başlığından okunur; biçim hatası anahtarı mesaja KOYMAZ.
 */
class BaslikAiAnahtarBaglamiTest {

    private final BaslikAiAnahtarBaglami baglam = new BaslikAiAnahtarBaglami();

    @AfterEach
    void temizle() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static void istek(String baslik) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        if (baslik != null) {
            r.addHeader(BaslikAiAnahtarBaglami.BASLIK, baslik);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(r));
    }

    @Test
    void basliksizBos() {
        istek(null);
        assertThat(baglam.anahtar()).isEmpty();

        istek("   ");
        assertThat(baglam.anahtar()).isEmpty();

        RequestContextHolder.resetRequestAttributes();
        assertThat(baglam.anahtar()).as("istek dışı iş parçacığı").isEmpty();
    }

    @Test
    void bosluklarKirpilir() {
        istek("  sk-or-v1-abcdef0123456789  ");
        assertThat(baglam.anahtar()).contains("sk-or-v1-abcdef0123456789");
    }

    @Test
    void bozukBicim400VeMesajdaAnahtarYok() {
        String bozuk = "sk-ant-GIZLIANAHTAR123456";
        istek(bozuk);

        IllegalArgumentException e = catchThrowableOfType(IllegalArgumentException.class, baglam::anahtar);

        assertThat(e).isNotNull();
        assertThat(e.getMessage()).isEqualTo("OpenRouter anahtarının biçimi geçersiz — 'sk-or-' ile başlamalı.")
                .doesNotContain("GIZLI");

        istek("sk-or-kisa");
        assertThat(catchThrowableOfType(IllegalArgumentException.class, baglam::anahtar)).as("10 karakterden kısa")
                .isNotNull();
        istek("sk-or-" + "a".repeat(201));
        assertThat(catchThrowableOfType(IllegalArgumentException.class, baglam::anahtar)).as("200 karakterden uzun")
                .isNotNull();
        istek("sk-or-abcdef0123 456789");
        assertThat(catchThrowableOfType(IllegalArgumentException.class, baglam::anahtar)).as("iç boşluk")
                .isNotNull();
    }
}
