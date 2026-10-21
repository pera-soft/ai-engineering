package tr.velzon.aiogretmen.types;

import java.util.List;

public record Hafta(int no, String baslik, String hedef, String kaynak,
                    List<Gun> gunler, List<QuizSorusu> quiz) {
}
