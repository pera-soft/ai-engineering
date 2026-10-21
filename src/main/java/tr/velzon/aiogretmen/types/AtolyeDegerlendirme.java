package tr.velzon.aiogretmen.types;

/**
 * AI Öğretmen'in öğrenci prompt'una verdiği değerlendirme (Faz A).
 * Alan adları prompt-degerlendirme.st JSON şemasıyla birebir (camelCase → @JsonProperty gereksiz).
 */
public record AtolyeDegerlendirme(Integer puan, String geriBildirim, String iyilestirme) {

    /** JSON düzelme çabası da başarısız olduğunda ham metinle zarif düşüş (degerlendirme deseni). */
    public static AtolyeDegerlendirme hamMetinden(String hamMetin) {
        return new AtolyeDegerlendirme(null, hamMetin, null);
    }
}
