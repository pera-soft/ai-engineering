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
        ANAHTAR_GECERSIZ("AI anahtarı geçersiz — site sorumlusuna haber ver.",
                "OpenRouter anahtarın geçersiz ya da iptal edilmiş."),
        /** 402 — OpenRouter bakiyesi bitti. */
        KREDI_BITTI("AI kredisi bitti — site sorumlusuna haber ver.",
                "OpenRouter kredin bitti ya da anahtarının harcama limitine ulaştın."),
        /** 429 — istek sınırı ya da anahtarın harcama tavanı. */
        YOGUN("AI şu an yoğun ya da aylık kullanım sınırına ulaşıldı — biraz sonra tekrar dene.",
                "OpenRouter çok fazla istek dedi, biraz bekleyip tekrar dene."),
        /** Zaman aşımı, bağlantı hatası, 408 ya da 5xx. */
        YANIT_YOK("AI şu an yanıt vermiyor — biraz sonra tekrar dene."),
        /** Diğer 4xx (ör. 400 bozuk istek/bağlam aşımı, 404 model yok) — tekrar denemek çoğu zaman yetmez. */
        ISTEK_REDDEDILDI("AI isteği kabul etmedi — tekrar dene; sürerse site sorumlusuna haber ver.");

        private final String mesaj;
        /** Ziyaretçinin kendi anahtarıyla yapılan çağrıda (Faz 10, ADR-0017) gösterilen mesaj. */
        private final String ziyaretciMesaji;

        Neden(String mesaj) {
            this(mesaj, mesaj);
        }

        Neden(String mesaj, String ziyaretciMesaji) {
            this.mesaj = mesaj;
            this.ziyaretciMesaji = ziyaretciMesaji;
        }

        public String mesaj() {
            return mesaj;
        }

        public String ziyaretciMesaji() {
            return ziyaretciMesaji;
        }
    }

    private final Neden neden;
    /** Sağlayıcının döndürdüğü HTTP kodu; bağlantı/zaman aşımı hatasında null. */
    private final Integer saglayiciKodu;
    /** Çağrı ziyaretçinin kendi anahtarıyla mı yapıldı? (mesaj ve HTTP kodu ona göre) */
    private final boolean ziyaretciAnahtari;

    public AiSaglayiciException(Neden neden, Integer saglayiciKodu, Throwable sebep) {
        this(neden, saglayiciKodu, sebep, false);
    }

    /**
     * @param sebep asıl sağlayıcı istisnası. Ziyaretçinin kendi anahtarıyla (site) BAĞLANMAZ: Spring AI'ın
     *              mesajı ham sağlayıcı gövdesini taşır, gövde {@code Authorization} başlığını (ziyaretçi
     *              anahtarını) yansıtabilir — ileride bir {@code log.error(…, e)} ya da zincir dökümü onu
     *              sızdırmasın. Teşhis için HTTP kodu {@link #saglayiciKodu()}'nda kalır. Yerelde (sunucu
     *              anahtarı) asıl istisna cause olarak korunur.
     */
    public AiSaglayiciException(Neden neden, Integer saglayiciKodu, Throwable sebep, boolean ziyaretciAnahtari) {
        super(ziyaretciAnahtari ? neden.ziyaretciMesaji() : neden.mesaj(), ziyaretciAnahtari ? null : sebep);
        this.neden = neden;
        this.saglayiciKodu = saglayiciKodu;
        this.ziyaretciAnahtari = ziyaretciAnahtari;
    }

    public boolean ziyaretciAnahtari() {
        return ziyaretciAnahtari;
    }

    public Neden neden() {
        return neden;
    }

    public Integer saglayiciKodu() {
        return saglayiciKodu;
    }
}
