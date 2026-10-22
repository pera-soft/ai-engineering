package tr.velzon.aiogretmen.types;

/**
 * AI sağlayıcısı (OpenRouter) çağrısı başarısız oldu — ham hata ai/ katmanında bu türe çevrilir
 * (tek nokta: {@code ai.AiHataCevirici}); web katmanı {@link Neden}e göre Türkçe mesaj + HTTP kodu döner.
 * Mesaj kullanıcıya gösterilir: anahtar, ham sağlayıcı gövdesi ya da sunucu yolu İÇERMEZ.
 */
public class AiSaglayiciException extends RuntimeException {

    /** Sağlayıcı hatasının kullanıcıya anlatılan kategorisi. */
    public enum Neden {
        /** 401/403 — anahtar geçersiz, iptal edilmiş ya da yetkisiz. */
        ANAHTAR_GECERSIZ("AI anahtarı geçersiz — site sorumlusuna haber ver."),
        /** 402 — OpenRouter bakiyesi bitti. */
        KREDI_BITTI("AI kredisi bitti — site sorumlusuna haber ver."),
        /** 429 — istek sınırı ya da anahtarın harcama tavanı. */
        YOGUN("AI şu an yoğun ya da aylık kullanım sınırına ulaşıldı — biraz sonra tekrar dene."),
        /** Zaman aşımı, bağlantı hatası, 408 ya da 5xx. */
        YANIT_YOK("AI şu an yanıt vermiyor — biraz sonra tekrar dene."),
        /** Diğer 4xx (ör. 400 bozuk istek/bağlam aşımı, 404 model yok) — tekrar denemek çoğu zaman yetmez. */
        ISTEK_REDDEDILDI("AI isteği kabul etmedi — tekrar dene; sürerse site sorumlusuna haber ver.");

        private final String mesaj;

        Neden(String mesaj) {
            this.mesaj = mesaj;
        }

        public String mesaj() {
            return mesaj;
        }
    }

    private final Neden neden;
    /** Sağlayıcının döndürdüğü HTTP kodu; bağlantı/zaman aşımı hatasında null. */
    private final Integer saglayiciKodu;

    public AiSaglayiciException(Neden neden, Integer saglayiciKodu, Throwable sebep) {
        super(neden.mesaj(), sebep);
        this.neden = neden;
        this.saglayiciKodu = saglayiciKodu;
    }

    public Neden neden() {
        return neden;
    }

    public Integer saglayiciKodu() {
        return saglayiciKodu;
    }
}
