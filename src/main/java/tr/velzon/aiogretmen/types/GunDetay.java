package tr.velzon.aiogretmen.types;

import java.util.List;

public record GunDetay(int gun, String baslik, String dersHtml, List<GorevDurum> gorevler) {
}
