package tr.velzon.aiogretmen.service;

import java.util.List;

/**
 * AI bağlam bütçesi (kesişen kaygı): her parçayı per-parça sınıra — mümkünse BLOK/satır sınırında —
 * öz'ler, sonra toplamı sınırlar. KaynakService ve KitapService bunu ORTAK kullanır; eskiden her
 * serviste kopyalanmış "doldur → setLength → kırpıldı etiketi" idiomu birleştirildi (mimari inceleme).
 *
 * <p>Neden blok-sınırında kesme: düz {@code substring(0, sinir)} markdown'ı bir başlığın/cümlenin
 * ortasından böler; son satır sonuna kadar geri gitmek metni bütün bırakır.
 */
final class BaglamKirpici {

    private BaglamKirpici() {
    }

    /**
     * @param parcalar       önceden biçimlenmiş bağlam parçaları (ör. "### Başlık — Yazar\n gövde")
     * @param parcaBasiSinir her parçanın azami karakteri; {@code <= 0} ise per-parça sınır uygulanmaz
     * @param toplamSinir    tüm bağlamın azami karakteri
     * @param bosMesaj       parça yoksa dönecek metin
     */
    static String birlestir(List<String> parcalar, int parcaBasiSinir, int toplamSinir, String bosMesaj) {
        if (parcalar == null || parcalar.isEmpty()) {
            return bosMesaj;
        }
        StringBuilder sb = new StringBuilder();
        for (String parca : parcalar) {
            sb.append(ozle(parca, parcaBasiSinir)).append("\n\n");
            if (sb.length() > toplamSinir) {
                sb.setLength(toplamSinir);
                sb.append("\n(… bağlam sınıra kırpıldı)");
                break;
            }
        }
        return sb.toString();
    }

    /** Parçayı sınıra kadar, mümkünse sınırdan önceki son satır sonunda keser (blok ortadan bölünmesin). */
    private static String ozle(String parca, int sinir) {
        if (sinir <= 0 || parca == null || parca.length() <= sinir) {
            return parca;
        }
        int kes = parca.lastIndexOf('\n', sinir); // sınırdan önceki en yakın satır sonu
        if (kes < sinir / 2) {                    // satır sonu çok geride ise (uzun tek satır) düz kes
            kes = sinir;
        }
        return parca.substring(0, kes) + "\n(… devamı öğrencinin okuma sayfasında)";
    }
}
