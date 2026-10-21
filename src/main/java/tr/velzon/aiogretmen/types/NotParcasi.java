package tr.velzon.aiogretmen.types;

/**
 * Anlam-eşleşmeli bağlam için tek bir kitap notu bloğu (embedding'lenecek en küçük birim).
 * ai/ paketindeki Spring AI {@code Document} tipi buraya (ve service'e) sızmasın diye
 * sade bir taşıyıcı — ArchUnit: types hiçbir şeye bağımlı değil.
 *
 * @param id     idempotent kimlik (slug#tarih#içerik-hash) — aynı blok tekrar eklenince üzerine yazılır
 * @param kaynak kitap adı
 * @param tarih  notun eklendiği gün (## Not — TARİH başlığından)
 * @param metin  embedding'lenecek gövde (özet + anahtar kavramlar; etiketler satırı hariç)
 */
public record NotParcasi(String id, String kaynak, String tarih, String metin) {
}
