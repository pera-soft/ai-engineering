package tr.velzon.aiogretmen.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mülakat modülü bean'leri yalnız {@code app.mulakat.etkin=true} iken kurulur (ADR-0011).
 * Varsayılan KAPALI: bayrak yoksa/false ise MulakatRepo içerik okumaz, servisler ve
 * {@code /api/mulakat/**} uçları hiç kayıtlı olmaz (istek → 404). Kod repoda kalır.
 *
 * <p>{@code MulakatCevapRepo} (Spring Data JDBC arayüzü) bilinçli olarak koşulsuzdur:
 * SifirlamaService onu kullanır; tablo şemada durur, kapalıyken boş kalması zararsızdır.
 *
 * <p>Faz 9b (ADR-0012): {@code site} profilinde bayrak ne olursa olsun KAPALI ({@code @Profile("!site")}) —
 * çok kullanıcılı sitede mülakat modülü yok (ortam değişkeniyle yanlışlıkla açılamaz).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = "app.mulakat", name = "etkin", havingValue = "true")
@Profile("!site")
public @interface MulakatEtkin {
}
