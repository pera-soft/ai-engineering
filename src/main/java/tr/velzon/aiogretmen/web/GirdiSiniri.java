package tr.velzon.aiogretmen.web;

/**
 * Serbest metin girdilerinin ortak üst sınırı (inceleme bulgusu; tech-debt #13 ile tek yerde toplandı):
 * dev yapıştırma → context aşımı → anlamsız 500 olmasın. Boş/null ya da çok uzun girdi
 * {@link IllegalArgumentException} → HataYonetimi'nde 400.
 */
final class GirdiSiniri {

    static final int MAX_GIRDI = 20_000;

    /** Öğretmen uçlarının (sohbet, değerlendirme, örnek, açıklama) "çok uzun" mesajına eklenen öneri. */
    static final String BOLEREK_GONDER = " Metni bölerek gönder.";

    private GirdiSiniri() {
    }

    static String sinirla(String alanAdi, String deger) {
        return sinirla(alanAdi, deger, "");
    }

    /** {@code ek}: "çok uzun" mesajının sonuna eklenen metin (ör. {@link #BOLEREK_GONDER}); boş olabilir. */
    static String sinirla(String alanAdi, String deger, String ek) {
        if (deger == null || deger.isBlank()) {
            throw new IllegalArgumentException(alanAdi + " boş olamaz");
        }
        if (deger.length() > MAX_GIRDI) {
            throw new IllegalArgumentException(
                    alanAdi + " çok uzun (%d karakter) — üst sınır %d.".formatted(deger.length(), MAX_GIRDI) + ek);
        }
        return deger;
    }
}
