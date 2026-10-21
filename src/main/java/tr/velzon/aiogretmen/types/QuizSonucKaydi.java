package tr.velzon.aiogretmen.types;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Bir kullanıcının bir haftadaki quiz sonucu. {@code kullaniciId} izolasyon anahtarıdır
 * (Faz 2a-3, ADR-0008).
 */
@Table("QUIZ_SONUC")
public record QuizSonucKaydi(@Id Long id, String kullaniciId, int hafta, String tur, int puan,
                             String detayJson, Instant tarih) {

    public static QuizSonucKaydi statik(String kullaniciId, int hafta, int puan, String detayJson) {
        return new QuizSonucKaydi(null, kullaniciId, hafta, "STATIK", puan, detayJson, Instant.now());
    }

    public static QuizSonucKaydi aiUretim(String kullaniciId, int hafta, int puan, String detayJson) {
        return new QuizSonucKaydi(null, kullaniciId, hafta, "AI_URETIM", puan, detayJson, Instant.now());
    }
}
