package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Bir prompt tekniği kategorisi (ör. "Temel Yapı") ve içindeki alıştırmalar.
 * Dosya adındaki NN öneki sıraları belirler (01, 07…). frontmatter'dan: slug, baslik, aciklama.
 */
public record AtolyeKategori(String slug, String baslik, String aciklama,
                             List<AtolyeAlistirma> alistirmalar) {
}
