package tr.velzon.aiogretmen.types;

import java.util.List;

public record Gun(int hafta, int gun, String baslik, String ders, List<String> gorevler) {
}
