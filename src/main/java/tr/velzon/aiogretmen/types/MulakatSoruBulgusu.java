package tr.velzon.aiogretmen.types;

/**
 * {@code soruBul} sonucu: soruyu bulunduğu alan/konu bağlamıyla döndürür (kayıt için gerekli).
 */
public record MulakatSoruBulgusu(String alanSlug, String konuSlug, MulakatSoru soru) {
}
