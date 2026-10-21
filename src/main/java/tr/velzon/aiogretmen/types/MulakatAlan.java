package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Bir mülakat alanı (ör. "Çekirdek Java") ve içindeki konular. Dizin adındaki NN öneki
 * (01-cekirdek-java) alan sırasını, frontmatter da slug/baslik/aciklama'yı verir.
 */
public record MulakatAlan(String slug, String baslik, String aciklama, List<MulakatKonu> konular) {
}
