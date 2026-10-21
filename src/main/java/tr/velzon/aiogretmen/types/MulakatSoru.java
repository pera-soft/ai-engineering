package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * SUNUCU-İÇİ tam soru: dört şık, doğru şıkkın indeksi ve yazılı açıklama. Bu tip web/JSON
 * gövdesine ASLA doğrudan serialize edilmez — istemciye {@link #gorunum()} (cevapsız) döner.
 * Doğruluk hesabı ({@code secilenIndex == dogruIndex}) hep sunucuda yapılır.
 */
public record MulakatSoru(String id, String soru, List<MulakatSik> siklar, int dogruIndex,
                          String aciklama, MulakatZorluk zorluk) {

    /** İstemciye sunulacak cevapsız görünüm (dogruIndex/aciklama ayıklanır; deneme yok). */
    public MulakatSoruGorunumu gorunum() {
        return new MulakatSoruGorunumu(id, soru,
                siklar.stream().map(MulakatSik::metin).toList(), zorluk, null);
    }

    /**
     * Cevapsız görünüm + kullanıcının son denemesi, AÇIK kipte (doğru şık + açıklama dahil).
     * Kullanıcı bu bilgiyi cevap verdiği an zaten gördü; ekranı geri boyamak sızıntı değildir.
     */
    public MulakatSoruGorunumu gorunumAcik(int secilenIndex, boolean dogru) {
        return gorunum().ile(MulakatSonDeneme.acik(secilenIndex, dogru, dogruIndex, aciklama, siklar));
    }

    /** Cevapsız görünüm + son deneme, GİZLİ kipte (yalnız seçim; doğru cevap saklı → tekrar çözülebilir). */
    public MulakatSoruGorunumu gorunumGizli(int secilenIndex, boolean dogru) {
        return gorunum().ile(MulakatSonDeneme.gizli(secilenIndex, dogru));
    }
}
