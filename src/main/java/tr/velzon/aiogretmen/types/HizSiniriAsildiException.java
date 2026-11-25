package tr.velzon.aiogretmen.types;

/**
 * Girişsiz sitede IP/ziyaretçi hız sınırı aşıldı (Faz 10, ADR-0017) → HTTP 429 + {@code Retry-After}
 * (tech-debt #15): {@link #beklemeSaniye()} ilgili pencerenin kalan süresi, en az 1 saniye.
 */
public class HizSiniriAsildiException extends RuntimeException {

    public static final String MESAJ = "Çok sık istek gönderdin, biraz bekleyip tekrar dene.";

    private final long beklemeSaniye;

    public HizSiniriAsildiException(long beklemeSaniye) {
        super(MESAJ);
        this.beklemeSaniye = Math.max(1, beklemeSaniye);
    }

    public long beklemeSaniye() {
        return beklemeSaniye;
    }
}
