package tr.velzon.aiogretmen.types;

/**
 * Mülakat sorusunun zorluk kademesi. İçerik dosyalarında Türkçe etiketle yazılır
 * ("Temel", "Orta", "İleri", "Uzman"); {@link #ayikla(String)} enum'a çevirir.
 */
public enum MulakatZorluk {
    TEMEL, ORTA, ILERI, UZMAN;

    /**
     * İçerikteki Türkçe etiketi enum'a çevirir ("İleri" → {@link #ILERI}).
     *
     * @throws IllegalStateException tanınmayan etiket (yükleme-anı fail-fast)
     */
    public static MulakatZorluk ayikla(String etiket) {
        if (etiket == null) {
            throw new IllegalStateException("Zorluk etiketi boş");
        }
        return switch (etiket.strip()) {
            case "Temel" -> TEMEL;
            case "Orta" -> ORTA;
            case "İleri" -> ILERI;
            case "Uzman" -> UZMAN;
            default -> throw new IllegalStateException("Bilinmeyen zorluk kademesi: " + etiket);
        };
    }
}
