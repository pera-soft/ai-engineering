package tr.velzon.aiogretmen.types;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Kullanıcının bir soruya verdiği EN SON cevabın istemciye dönen özeti — ekranın yeniden
 * boyanabilmesi için ("cevaplarım kayboldu" hatası: DB'de duruyordu, istemciye hiç gitmiyordu).
 *
 * <p><strong>İki kip</strong> — cevap sızıntısı görünüme göre ayarlanır:
 * <ul>
 *   <li>{@link #acik} — {@code dogruIndex} + {@code aciklama} + şık {@code aiNot}'ları DOLU.
 *       Konu görünümü ve "Doğru Cevaplar" listesi için: kullanıcı cevabı verdiği an bu bilgiyi
 *       zaten gördü, geri yüklemek yeni bilgi sızdırmaz. Alan kümesi {@link MulakatCevapSonucu}
 *       ile bilerek aynıdır — istemci ikisini AYNI boyama koduyla işler, ekran birebir döner.</li>
 *   <li>{@link #gizli} — {@code dogruIndex}/{@code aciklama}/{@code siklar} {@code null}.
 *       "Hatalı Cevaplar" (tekrar) listesi için: hangi şıkkı işaretlediği gösterilir ama doğru
 *       cevap saklı kalır, böylece soru YENİDEN çözülebilir (listenin var oluş sebebi).</li>
 * </ul>
 * Hiç cevaplanmamış soruda alan {@code null}'dır — kip değil, "deneme yok" demektir.
 *
 * <p>{@code NON_NULL}: gizli kipte {@code dogruIndex}/{@code aciklama} JSON gövdesine hiç
 * YAZILMAZ ({@code "dogruIndex":null} bile çıkmaz) — sızıntı testi alan ADININ yokluğunu
 * doğrulayabilsin diye; sözleşme "değeri boş" değil, "alan orada değil"dir.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MulakatSonDeneme(int secilenIndex, boolean dogru, Integer dogruIndex,
                               String aciklama, List<MulakatSik> siklar) {

    /** Tam geri yükleme: doğru şık + açıklama + şık notları (kullanıcı bunları zaten gördü). */
    public static MulakatSonDeneme acik(int secilenIndex, boolean dogru, int dogruIndex,
                                        String aciklama, List<MulakatSik> siklar) {
        return new MulakatSonDeneme(secilenIndex, dogru, dogruIndex, aciklama, siklar);
    }

    /** Yalnız seçim: doğru cevap GİZLİ kalır — soru tekrar çözülebilsin. */
    public static MulakatSonDeneme gizli(int secilenIndex, boolean dogru) {
        return new MulakatSonDeneme(secilenIndex, dogru, null, null, null);
    }
}
