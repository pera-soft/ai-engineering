package tr.velzon.aiogretmen.types;

public record HaftaOzeti(int no, String baslik, String hedef,
                         int toplamGorev, int tamamlananGorev, int ilerlemeYuzde,
                         Integer sonQuizPuan) {
}
