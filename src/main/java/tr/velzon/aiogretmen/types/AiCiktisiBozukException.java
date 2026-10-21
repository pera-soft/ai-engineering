package tr.velzon.aiogretmen.types;

/** AI beklenen yapıda çıktı üretemedi (düzeltme denemesi de dahil) → HTTP 502. */
public class AiCiktisiBozukException extends RuntimeException {

    public AiCiktisiBozukException(String mesaj) {
        super(mesaj);
    }
}
