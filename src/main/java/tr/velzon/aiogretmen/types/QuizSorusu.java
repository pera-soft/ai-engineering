package tr.velzon.aiogretmen.types;

import java.util.List;

public record QuizSorusu(String soru, List<String> secenekler, int dogruIndex, String aciklama) {
}
