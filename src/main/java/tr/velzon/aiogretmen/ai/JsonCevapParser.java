package tr.velzon.aiogretmen.ai;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Model çıktısından JSON ayıklar: markdown çitlerini soyar, etraftaki metni atar.
 * Parse edilemezse Optional.empty — çağıran zarif düşüşten sorumlu (brief 7.2).
 */
@Component
public class JsonCevapParser {

    // Modeller string içine literal satır sonu koyabiliyor — katı JSON'un reddettiği
    // kontrol karakterlerine tolerans şart (FoSA Bölüm 20 kitap notu vakası).
    // Ayrıca string beklenen alana dizi dönebiliyorlar (AIE önsözü vakası) — birleştirilir.
    private final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true)
            .addModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                    .addDeserializer(String.class, new DiziyiBirlestirenStringDeserializer()))
            .build();

    /** String hedefine JSON dizisi gelirse öğeleri satır sonuyla birleştirir. */
    private static final class DiziyiBirlestirenStringDeserializer
            extends com.fasterxml.jackson.databind.JsonDeserializer<String> {
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p,
                                  com.fasterxml.jackson.databind.DeserializationContext ctx)
                throws java.io.IOException {
            if (p.currentToken() == com.fasterxml.jackson.core.JsonToken.START_ARRAY) {
                StringBuilder sb = new StringBuilder();
                while (p.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(p.getValueAsString());
                }
                return sb.toString();
            }
            return p.getValueAsString();
        }
    }

    public <T> Optional<T> parse(String hamMetin, Class<T> tip) {
        if (hamMetin == null || hamMetin.isBlank()) {
            return Optional.empty();
        }
        String aday = jsonAyikla(hamMetin);
        if (aday == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(aday, tip));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** İlk '{' ile dengelenen son '}' arasını alır; ```json çitlerini yok sayar. */
    private String jsonAyikla(String metin) {
        String temiz = metin.replaceAll("```(?:json)?", "").strip();
        int bas = temiz.indexOf('{');
        if (bas < 0) {
            return null;
        }
        int derinlik = 0;
        boolean stringIci = false;
        for (int i = bas; i < temiz.length(); i++) {
            char c = temiz.charAt(i);
            if (stringIci) {
                if (c == '\\') {
                    i++; // kaçış dizisi: bir sonraki karakteri (\" \\ \n ...) atla
                } else if (c == '"') {
                    stringIci = false;
                }
            } else if (c == '"') {
                stringIci = true;
            } else if (c == '{') {
                derinlik++;
            } else if (c == '}' && --derinlik == 0) {
                return temiz.substring(bas, i + 1);
            }
        }
        return null;
    }
}
