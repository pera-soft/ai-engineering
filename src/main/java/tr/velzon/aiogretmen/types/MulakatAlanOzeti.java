package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Alan listeleme projeksiyonu — konu özetlerini taşır, soru cevabı içermez.
 */
public record MulakatAlanOzeti(String slug, String baslik, String aciklama,
                               List<MulakatKonuOzeti> konular) {
}
