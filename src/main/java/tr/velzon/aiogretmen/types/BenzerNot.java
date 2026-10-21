package tr.velzon.aiogretmen.types;

/**
 * Anlam aramasının bir sonucu: eşleşen not metni + benzerlik skoru (0..1 cosine).
 * ai/ paketindeki Spring AI {@code Document} tipini service/web'e taşımamak için ara tip.
 *
 * @param metin eşleşen notun gövdesi
 * @param skor  cosine benzerlik (1'e yakın = daha alakalı)
 */
public record BenzerNot(String metin, double skor) {
}
