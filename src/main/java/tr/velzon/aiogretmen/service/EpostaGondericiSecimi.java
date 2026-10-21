package tr.velzon.aiogretmen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import tr.velzon.aiogretmen.config.SiteAyarlari;

/**
 * {@code site} profilinde e-posta göndericisini TEK yerde seçer (Faz 9c, ADR-0013 §3):
 * <ul>
 *   <li>{@code spring.mail.host} dolu ({@code SPRING_MAIL_HOST}) → {@link SmtpEpostaGonderici}
 *       (Boot'un {@code JavaMailSender}'ı ile);</li>
 *   <li>boş ve {@code app.site.eposta-zorunlu=true} (varsayılan) → açılış HATASI — canlı, gizli
 *       bağlantıları sessizce log'a yazamaz;</li>
 *   <li>boş ve {@code eposta-zorunlu=false} (yalnız yerel deneme) → {@link LogEpostaGonderici} + uyarı.</li>
 * </ul>
 * SMTP şifresi hiçbir koşulda loglanmaz.
 */
@Configuration
@Profile("site")
public class EpostaGondericiSecimi {

    private static final Logger log = LoggerFactory.getLogger(EpostaGondericiSecimi.class);

    @Bean
    EpostaGonderici epostaGonderici(Environment ortam, SiteAyarlari ayarlar,
                                    ObjectProvider<JavaMailSender> mailGonderici) {
        String host = ortam.getProperty("spring.mail.host", "").trim();
        if (!host.isEmpty()) {
            JavaMailSender mail = mailGonderici.getIfAvailable();
            if (mail == null) {
                throw new IllegalStateException("spring.mail.host verildi ama JavaMailSender kurulamadı (ADR-0013)");
            }
            log.info("E-posta: SMTP {}:{} üzerinden, gönderen '{}'", host,
                    ortam.getProperty("spring.mail.port", "25"), ayarlar.gonderici());
            return new SmtpEpostaGonderici(mail, ayarlar.gonderici());
        }
        if (ayarlar.epostaZorunlu()) {
            throw new IllegalStateException("site profili SMTP olmadan açılamaz: SPRING_MAIL_HOST (+ SPRING_MAIL_PORT, "
                    + "SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD) ver; yalnız yerel deneme için "
                    + "APP_SITE_EPOSTA_ZORUNLU=false (bağlantılar log'a yazılır) — ADR-0013");
        }
        log.warn("E-posta: SMTP YOK — doğrulama/şifre sıfırlama bağlantıları LOG'A yazılacak "
                + "(app.site.eposta-zorunlu=false; yalnız yerel deneme, canlıda KULLANMA)");
        return new LogEpostaGonderici();
    }
}
