package tr.velzon.aiogretmen.types;

import java.util.List;

/**
 * Eğitim kaynağı (content/kaynaklar/*.md): kullanıcının verdiği makalelerin
 * Türkçe çalışma metinleri. Etiketler hangi haftalarla ilgili olduğunu söyler.
 */
public record Kaynak(String slug, String baslik, String yazar, String tarih,
                     String url, List<Integer> etiketler, String govde) {

    /** Liste görünümü için gövdesiz kopya. */
    public Kaynak meta() {
        return new Kaynak(slug, baslik, yazar, tarih, url, etiketler, null);
    }
}
