package tr.velzon.aiogretmen.service;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Faz 9c (ADR-0013): SMTP göndericisi — gerçek ağ YOK, sahte {@link JavaMailSender}. Türkçe konu,
 * düz metin + HTML (multipart), gönderen adı, HTML kaçışı; SMTP hatası çağırana sızmaz.
 */
class SmtpEpostaGondericiTest {

    private static final String BAGLANTI = "https://egitim.pera.software/api/kimlik/dogrula?token=abc_DEF-123";

    private JavaMailSender mail;
    private final Executor hemen = Runnable::run; // testte eşzamanlı

    @BeforeEach
    void kur() {
        mail = mock(JavaMailSender.class);
        when(mail.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
    }

    private SmtpEpostaGonderici gonderici() {
        return new SmtpEpostaGonderici(mail, "AI Öğretmen <noreply@pera.software>", hemen);
    }

    private MimeMessage gonderilen() throws Exception {
        ArgumentCaptor<MimeMessage> m = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mail).send(m.capture());
        m.getValue().saveChanges(); // Content-Type başlıkları gönderimdeki gibi yazılsın
        return m.getValue();
    }

    /** multipart ağacındaki düz metin ve HTML gövdelerini toplar. */
    private static void parcalar(Part p, List<String> duz, List<String> html) throws Exception {
        if (p.isMimeType("text/plain")) {
            duz.add((String) p.getContent());
        } else if (p.isMimeType("text/html")) {
            html.add((String) p.getContent());
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                parcalar(mp.getBodyPart(i), duz, html);
            }
        }
    }

    @Test
    void dogrulamaEpostasiTurkceVeMultipart() throws Exception {
        gonderici().dogrulamaBaglantisiGonder("ali@pera.software", BAGLANTI);

        MimeMessage m = gonderilen();
        assertThat(m.getSubject()).contains("doğrula");
        assertThat(m.getAllRecipients()).extracting(Object::toString).containsExactly("ali@pera.software");
        InternetAddress kimden = (InternetAddress) m.getFrom()[0];
        assertThat(kimden.getAddress()).isEqualTo("noreply@pera.software");
        assertThat(kimden.getPersonal()).isEqualTo("AI Öğretmen");

        List<String> duz = new ArrayList<>();
        List<String> html = new ArrayList<>();
        parcalar(m, duz, html);
        assertThat(duz).hasSize(1);
        assertThat(html).hasSize(1);
        assertThat(duz.get(0)).contains(BAGLANTI).contains("24 saat");
        assertThat(html.get(0)).contains("href=\"" + BAGLANTI + "\"").contains("<html");
    }

    @Test
    void sifreSifirlamaEpostasi() throws Exception {
        String link = "https://egitim.pera.software/sifre-sifirla.html?token=xyz";
        gonderici().sifreSifirlamaBaglantisiGonder("veli@pera.software", link);

        MimeMessage m = gonderilen();
        assertThat(m.getSubject()).contains("Şifre sıfırlama");
        List<String> duz = new ArrayList<>();
        List<String> html = new ArrayList<>();
        parcalar(m, duz, html);
        assertThat(duz.get(0)).contains(link).contains("1 saat");
        assertThat(html.get(0)).contains(link);
    }

    @Test
    void htmlDegerleriKacislanir() throws Exception {
        gonderici().dogrulamaBaglantisiGonder("ali@pera.software", "https://x/a?b=1&c=\"<script>");
        List<String> duz = new ArrayList<>();
        List<String> html = new ArrayList<>();
        parcalar(gonderilen(), duz, html);
        assertThat(html.get(0)).doesNotContain("<script>").contains("&amp;c=&quot;&lt;script&gt;");
    }

    @Test
    void smtpHatasiCagiranaSizmaz() {
        doThrow(new MailSendException("Bağlantı reddedildi")).when(mail).send(any(MimeMessage.class));
        assertThatCode(() -> gonderici().dogrulamaBaglantisiGonder("ali@pera.software", BAGLANTI))
                .doesNotThrowAnyException();
    }

    @Test
    void kuyrukDoluysaCagiranaSizmaz() {
        Executor dolu = r -> {
            throw new RejectedExecutionException("dolu");
        };
        SmtpEpostaGonderici g = new SmtpEpostaGonderici(mail, "AI Öğretmen <noreply@pera.software>", dolu);
        assertThatCode(() -> g.sifreSifirlamaBaglantisiGonder("ali@pera.software", BAGLANTI))
                .doesNotThrowAnyException();
        verify(mail, never()).send(any(MimeMessage.class));
    }

    @Test
    void bozukGonderenAdresiAcilistaReddedilir() {
        assertThatThrownBy(() -> new SmtpEpostaGonderici(mail, "", hemen))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new SmtpEpostaGonderici(mail, "AI <bozuk", hemen))
                .isInstanceOf(IllegalStateException.class);
    }
}
