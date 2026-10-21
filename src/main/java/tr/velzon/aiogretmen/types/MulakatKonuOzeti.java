package tr.velzon.aiogretmen.types;

/**
 * Konu listeleme projeksiyonu — soru CEVABI içermez, yalnız üst-veri (kaç soru, hangi kademe)
 * ve kullanıcının SAYISAL ilerlemesi. İkisi ayrı sayılır, çünkü ayrı sorulara cevap verirler:
 * <ul>
 *   <li>{@code cevaplananSayisi} — kaç farklı soruya cevap verdi (doğru/yanlış fark etmez):
 *       "bu konuya başladım mı, boş soru kaldı mı".</li>
 *   <li>{@code dogruSayisi} — SON denemesi doğru olan farklı soru sayısı: "ustalaştım mı"
 *       ({@code .done} bundan türetilir).</li>
 * </ul>
 * İkisi de yalnız SAYIdır; doğru cevabı ele vermez.
 */
public record MulakatKonuOzeti(String slug, String baslik, MulakatZorluk kademe, int sira,
                               int soruSayisi, int cevaplananSayisi, int dogruSayisi) {
}
