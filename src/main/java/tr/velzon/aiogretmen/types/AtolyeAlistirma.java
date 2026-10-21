package tr.velzon.aiogretmen.types;

/**
 * Prompt Atölyesi'nde tek bir alıştırma (Faz A). İçerik = veri:
 * content/prompt-atolyesi/NN-*.md gövdesindeki "# id | teknik | ai:bool" bloğundan parse edilir.
 *
 * @param kotuOrnek OPSİYONEL — "## Kötü örnek" bölümü olmayan alıştırmalarda boş string.
 * @param aiDegerlendir true ise öğrenci prompt'u AI Öğretmen'e değerlendirtilebilir (az token).
 */
public record AtolyeAlistirma(String id, String teknik, String gorev, String kotuOrnek,
                              String ipucu, String idealCozum, String neden, boolean aiDegerlendir) {
}
