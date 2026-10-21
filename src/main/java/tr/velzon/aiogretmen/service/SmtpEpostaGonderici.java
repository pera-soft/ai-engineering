package tr.velzon.aiogretmen.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SMTP e-posta göndericisi (Faz 9c, ADR-0013): Türkçe, {@code multipart/alternative} (düz metin + basit
 * HTML); şablonlar {@code classpath:eposta/*.txt|html} ({@code {{baglanti}}} yer tutucusu, HTML'de kaçışlı).
 *
 * <p><strong>İsteği bloklamaz/bozmaz:</strong> gönderim tek iş parçacıklı, sınırlı kuyruklu (100) bir
 * yürütücüde yapılır; SMTP hatası ya da kuyruk taşması yalnız sırsız loglanır (alıcı + tür + hata
 * sınıfı/mesajı) — bağlantı ve SMTP şifresi ASLA loglanmaz. Bean değil: {@link EpostaGondericiSecimi} üretir.
 */
public class SmtpEpostaGonderici implements EpostaGonderici, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(SmtpEpostaGonderici.class);
    private static final int KUYRUK = 100;

    private record Sablon(String konu, String duz, String html) {
    }

    private final JavaMailSender mail;
    private final InternetAddress kimden;
    private final Executor yurutucu;
    private final ExecutorService sahipOlunan; // kendi açtığımız yürütücü (kapanışta durdurulur)
    private final Sablon dogrulama;
    private final Sablon sifirlama;

    /** Üretim: kendi sınırlı kuyruklu arka plan yürütücüsüyle. */
    public SmtpEpostaGonderici(JavaMailSender mail, String gonderici) {
        this(mail, gonderici, arkaPlanYurutucu());
    }

    /** Test: yürütücü dışarıdan (ör. eşzamanlı {@code Runnable::run}). */
    SmtpEpostaGonderici(JavaMailSender mail, String gonderici, Executor yurutucu) {
        this.mail = mail;
        this.kimden = adresCoz(gonderici);
        this.yurutucu = yurutucu;
        this.sahipOlunan = yurutucu instanceof ExecutorService es ? es : null;
        this.dogrulama = new Sablon("AI Öğretmen — e-posta adresini doğrula",
                oku("eposta/dogrulama.txt"), oku("eposta/dogrulama.html"));
        this.sifirlama = new Sablon("AI Öğretmen — Şifre sıfırlama",
                oku("eposta/sifre-sifirlama.txt"), oku("eposta/sifre-sifirlama.html"));
    }

    @Override
    public void dogrulamaBaglantisiGonder(String email, String baglanti) {
        gonder("dogrulama", dogrulama, email, baglanti);
    }

    @Override
    public void sifreSifirlamaBaglantisiGonder(String email, String baglanti) {
        gonder("sifre-sifirlama", sifirlama, email, baglanti);
    }

    private void gonder(String tur, Sablon sablon, String email, String baglanti) {
        try {
            MimeMessage mesaj = mail.createMimeMessage();
            MimeMessageHelper yardimci = new MimeMessageHelper(mesaj, true, StandardCharsets.UTF_8.name());
            yardimci.setFrom(kimden);
            yardimci.setTo(email);
            yardimci.setSubject(sablon.konu());
            yardimci.setText(sablon.duz().replace("{{baglanti}}", baglanti),
                    sablon.html().replace("{{baglanti}}", htmlKacis(baglanti)));
            yurutucu.execute(() -> {
                try {
                    mail.send(mesaj);
                    log.info("E-posta gönderildi (tür={}, alıcı={})", tur, email);
                } catch (RuntimeException e) {
                    log.warn("E-posta gönderilemedi (tür={}, alıcı={}): {}", tur, email, ozet(e));
                }
            });
        } catch (MessagingException | RuntimeException e) {
            // RejectedExecutionException (kuyruk dolu) dahil — çağırana SIZMAZ
            log.warn("E-posta gönderilemedi (tür={}, alıcı={}): {}", tur, email, ozet(e));
        }
    }

    /** Sınıf adı + mesaj; yığın izi YOK (iç içe istisnalarda mesaj gövdesi taşınmasın). */
    private static String ozet(Exception e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    @Override
    public void destroy() throws InterruptedException {
        if (sahipOlunan != null) {
            sahipOlunan.shutdown();
            if (!sahipOlunan.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Kapanışta e-posta kuyruğu boşalmadı; kalan e-postalar gönderilmedi");
                sahipOlunan.shutdownNow();
            }
        }
    }

    private static ExecutorService arkaPlanYurutucu() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(KUYRUK), r -> {
            Thread t = new Thread(r, "eposta-gonderici");
            t.setDaemon(true);
            return t;
        }, (r, havuz) -> {
            throw new RejectedExecutionException("e-posta kuyruğu dolu (" + KUYRUK + ")");
        });
    }

    /** "Ad <adres>" ya da düz adres; ad UTF-8 kodlanır (Türkçe karakter). Bozuksa açılış hatası. */
    private static InternetAddress adresCoz(String gonderici) {
        if (gonderici == null || gonderici.isBlank()) {
            throw new IllegalStateException("app.site.gonderici boş — SMTP gönderen adresi gerekli (ADR-0013)");
        }
        try {
            InternetAddress a = new InternetAddress(gonderici.trim(), true);
            return new InternetAddress(a.getAddress(), a.getPersonal(), StandardCharsets.UTF_8.name());
        } catch (AddressException | UnsupportedEncodingException e) {
            throw new IllegalStateException("app.site.gonderici geçersiz e-posta adresi: " + gonderici, e);
        }
    }

    private static String oku(String yol) {
        try (InputStream in = SmtpEpostaGonderici.class.getClassLoader().getResourceAsStream(yol)) {
            if (in == null) {
                throw new IllegalStateException("E-posta şablonu yok: " + yol);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("E-posta şablonu okunamadı: " + yol, e);
        }
    }

    static String htmlKacis(String s) {
        StringBuilder b = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> b.append("&amp;");
                case '<' -> b.append("&lt;");
                case '>' -> b.append("&gt;");
                case '"' -> b.append("&quot;");
                case '\'' -> b.append("&#39;");
                default -> b.append(c);
            }
        }
        return b.toString();
    }
}
