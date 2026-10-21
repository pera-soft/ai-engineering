package tr.velzon.aiogretmen.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** AI değerlendirme sözleşmesi (brief 7.2). Alan adları JSON şemasıyla birebir. */
public record Degerlendirme(
        Integer puan,
        String ozet,
        @JsonProperty("dogru_noktalar") List<String> dogruNoktalar,
        List<String> eksikler,
        @JsonProperty("yanlis_anlamalar") List<YanlisAnlama> yanlisAnlamalar,
        @JsonProperty("sonraki_adim") String sonrakiAdim,
        @JsonProperty("tekrar_onerisi") String tekrarOnerisi) {

    public record YanlisAnlama(String ne, String dogrusu,
                               @JsonProperty("neden_onemli") String nedenOnemli) {
    }

    /** JSON düzelme çabası da başarısız olduğunda ham metinle zarif düşüş (brief 7.2). */
    public static Degerlendirme hamMetinden(String hamMetin) {
        return new Degerlendirme(null, hamMetin, List.of(), List.of(), List.of(), null, null);
    }
}
