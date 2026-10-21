package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * İSTEMCİ görünümü — sorunun kendi cevabı YOKTUR: {@code dogruIndex}/{@code aciklama} kasten
 * alan olarak bulunmaz, doğruluk yalnız sunucuda ({@link MulakatSoru}) kalır.
 *
 * <p>{@code sonDeneme} bunun İSTİSNASI DEĞİLDİR: soru hiç cevaplanmamışsa {@code null}'dır;
 * doluysa yalnız o kullanıcının KENDİ geçmiş cevabını taşır ve doğru şıkkı ancak
 * {@link MulakatSonDeneme#acik} kipinde — yani kullanıcının zaten gördüğü yerde — açar.
 */
public record MulakatSoruGorunumu(String id, String soru, List<String> secenekler,
                                  MulakatZorluk zorluk, MulakatSonDeneme sonDeneme) {

    /** Aynı görünüm + son deneme (kayıt bulunduğunda zenginleştirme; görünüm değişmez). */
    public MulakatSoruGorunumu ile(MulakatSonDeneme deneme) {
        return new MulakatSoruGorunumu(id, soru, secenekler, zorluk, deneme);
    }
}
