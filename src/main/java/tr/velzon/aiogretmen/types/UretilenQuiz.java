package tr.velzon.aiogretmen.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** AI quiz üretimi çıktı sözleşmesi (brief 7.4). */
public record UretilenQuiz(List<Soru> sorular) {

    /** dogruIndex Integer: AI alanı unutursa Jackson'ın sessizce 0 ataması yerine null olsun. */
    public record Soru(String soru, List<String> secenekler,
                       @JsonProperty("dogru_index") Integer dogruIndex, String aciklama) {
    }
}
