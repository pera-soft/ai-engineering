package tr.velzon.aiogretmen.types;

/** Girişsiz sitede IP/ziyaretçi hız sınırı aşıldı (Faz 10, ADR-0017) → HTTP 429. */
public class HizSiniriAsildiException extends RuntimeException {

    public static final String MESAJ = "Çok sık istek gönderdin, biraz bekleyip tekrar dene.";

    public HizSiniriAsildiException() {
        super(MESAJ);
    }
}
