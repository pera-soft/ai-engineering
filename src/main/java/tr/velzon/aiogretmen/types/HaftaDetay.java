package tr.velzon.aiogretmen.types;

import java.util.List;

public record HaftaDetay(int no, String baslik, String hedef, String kaynak,
                         List<GunDetay> gunler, List<QuizSorusu> quiz,
                         Integer sonQuizPuan) {
}
