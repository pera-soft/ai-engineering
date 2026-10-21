package tr.velzon.aiogretmen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.velzon.aiogretmen.config.KullaniciBaglami;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;

/**
 * Kullanıcı başına dosya dizini (Faz 9b, ADR-0012): öğrenci hafızası ve rapor önbelleği.
 *
 * <ul>
 *   <li>{@link KullaniciBaglami#YEREL} → kökün KENDİSİ ({@code data/hafiza/OGRENCI.md},
 *       {@code data/raporlar/hafta-NN.md}): tek kişilik yerel modun mevcut verisi taşınmadan aynen kullanılır.</li>
 *   <li>diğer kimlikler → {@code <kök>/kullanicilar/<sha256-hex(kimlik)>/}. Kimlik (e-posta) dosya sistemine
 *       düz YAZILMAZ: özet sabit 64 hex karakterdir → yol gezintisi ({@code ../}), ayırıcı, büyük/küçük harf
 *       veya ayrılmış ad ({@code kitaplar}) sorunu yapısal olarak imkânsız; e-posta dizin listesinde görünmez.</li>
 * </ul>
 * {@code kullanicilar/} yerel kökün ALTINDA olduğundan yerel sıfırlama yalnız {@code OGRENCI.md} ve
 * {@code hafta-*} adlarını hedefler — bu dizine dokunmaz.
 */
public final class KisiselDizin {

    private static final Logger log = LoggerFactory.getLogger(KisiselDizin.class);
    static final String KULLANICILAR = "kullanicilar";

    private KisiselDizin() {
    }

    /** @return kullanıcının bu köke göre dizini (oluşturmaz). */
    public static Path coz(Path kok, String kullaniciId) {
        if (kullaniciId == null || kullaniciId.isBlank()) {
            throw new IllegalArgumentException("kullanıcı kimliği boş olamaz — kişisel dizin çözülemez");
        }
        if (KullaniciBaglami.YEREL.equals(kullaniciId)) {
            return kok;
        }
        return kok.resolve(KULLANICILAR).resolve(dizinAdi(kullaniciId));
    }

    /** Kimliğin SHA-256 özeti (küçük harf hex, 64 karakter). */
    static String dizinAdi(String kullaniciId) {
        try {
            byte[] ozet = MessageDigest.getInstance("SHA-256").digest(kullaniciId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(ozet);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 yok", e); // her JVM'de zorunlu algoritma
        }
    }

    /** Dizin ağacını yaprak→kök sırasıyla siler; yoksa no-op (idempotent). Tekil hatalar loglanır. */
    static void agaciSil(Path kok) throws IOException {
        if (!Files.exists(kok)) {
            return;
        }
        try (var yol = Files.walk(kok)) {
            yol.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ex) {
                    log.warn("Silinemedi {}: {}", p, ex.getMessage());
                }
            });
        }
    }
}
