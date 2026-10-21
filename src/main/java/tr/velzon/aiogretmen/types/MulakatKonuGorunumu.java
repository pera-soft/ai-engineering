package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Konu İSTEMCİ görünümü — CEVAPSIZ. Ders metni (render edilmiş {@code dersHtml}) ve CEVAPSIZ
 * soru görünümleri taşır; {@code dogruIndex}/{@code aciklama} alanı bu tipin ağacında HİÇBİR
 * yerde YOKTUR → cevap-sızıntısı derleyici-zorlamalıdır (tip düzeyinde imkânsız).
 */
public record MulakatKonuGorunumu(String baslik, MulakatZorluk kademe, String dersHtml,
                                  List<MulakatSoruGorunumu> sorular) {
}
