package tr.velzon.aiogretmen.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

import java.util.List;
import java.util.Map;

/**
 * MCP adaptörü için saf yardımcılar: JSON serileştirme, argüman ayıklama, girdi şeması kurma.
 *
 * <p><strong>Mimari kilit (ADR-0007):</strong> Bu paket ({@code ..aiogretmen.mcp..}) Spring AI
 * paketlerini import ETMEZ; yalnız MCP Java SDK'sını
 * ({@code io.modelcontextprotocol..}) ve Jackson'ı kullanır. Araçlar Spring AI'nın {@code @Tool} +
 * {@code ToolCallbackProvider} deseniyle DEĞİL, doğrudan SDK'nın {@code Sync*Specification} bean'leriyle
 * tanımlanır (Spring AI MCP server auto-config bu bean'leri de toplar). Böylece muhakeme —ve token
 * maliyeti— bağlanan istemcinin Claude'unda kalır, sunucunun OpenRouter anahtarı yanmaz. Kural
 * {@code MimariTestleri.claude_yalniz_ai_paketinden} ile derleme zamanında zorlanır.
 */
final class McpAdaptorDestek {

    private McpAdaptorDestek() {
    }

    /** Herhangi bir nesneyi JSON metne çevirip tek metin-içerikli başarılı araç sonucu kurar. */
    static CallToolResult json(ObjectMapper om, Object payload) {
        try {
            return CallToolResult.builder().addTextContent(om.writeValueAsString(payload)).build();
        } catch (JsonProcessingException e) {
            return hata("Sonuç serileştirilemedi: " + e.getOriginalMessage());
        }
    }

    /** Hata sonucu (isError=true) — istemci Claude'u kullanıcıya zarif bir mesaj gösterebilsin. */
    static CallToolResult hata(String mesaj) {
        return CallToolResult.builder().addTextContent(mesaj).isError(true).build();
    }

    /** {@code type:object} girdi şeması; verilen alanlar zorunlu kabul edilir. */
    static JsonSchema sema(Map<String, Object> ozellikler, List<String> zorunlu) {
        return new JsonSchema("object", ozellikler, zorunlu, false, null, null);
    }

    /** Argümansız araçlar için boş girdi şeması. */
    static JsonSchema semaBos() {
        return sema(Map.of(), List.of());
    }

    static Map<String, Object> tamsayiAlan(String aciklama) {
        return Map.of("type", "integer", "description", aciklama);
    }

    static Map<String, Object> metinAlan(String aciklama) {
        return Map.of("type", "string", "description", aciklama);
    }

    static Map<String, Object> mantiksalAlan(String aciklama) {
        return Map.of("type", "boolean", "description", aciklama);
    }

    /** JSON sayıları Integer/Double/Long gelebilir; hepsini int'e indirger. Eksikse hata fırlatır. */
    static int tamsayi(Map<String, Object> args, String anahtar) {
        Object v = args.get(anahtar);
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            return Integer.parseInt(s.trim());
        }
        throw new IllegalArgumentException("Eksik/geçersiz sayısal argüman: " + anahtar);
    }

    static int tamsayiVarsayilan(Map<String, Object> args, String anahtar, int varsayilan) {
        Object v = args.get(anahtar);
        if (v == null) {
            return varsayilan;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        String s = v.toString().trim();
        return s.isBlank() ? varsayilan : Integer.parseInt(s);
    }

    /**
     * Opsiyonel boolean argüman: yoksa varsayılan. Bazı istemciler "true"/"false" metni gönderir —
     * kabul edilir; başka her değer (ör. "belki", 1) sessizce varsayılana DÜŞMEZ, hata olur
     * (yanlış yorum görevi istenmeyen duruma ayarlardı). Harf katlaması Locale.ROOT ile — tr_TR'de
     * varsayılan katlama I/ı tuzağına düşer (testler tr_TR koşar).
     */
    static boolean mantiksalVarsayilan(Map<String, Object> args, String anahtar, boolean varsayilan) {
        Object v = args.get(anahtar);
        if (v == null) {
            return varsayilan;
        }
        if (v instanceof Boolean b) {
            return b;
        }
        String s = v.toString().trim().toLowerCase(java.util.Locale.ROOT);
        if (s.equals("true") || s.equals("false")) {
            return Boolean.parseBoolean(s);
        }
        throw new IllegalArgumentException("Geçersiz mantıksal argüman (true/false bekleniyor): " + anahtar);
    }

    static String metin(Map<String, Object> args, String anahtar) {
        Object v = args.get(anahtar);
        if (v == null) {
            throw new IllegalArgumentException("Eksik metin argümanı: " + anahtar);
        }
        return v.toString();
    }

    static String metinVarsayilan(Map<String, Object> args, String anahtar, String varsayilan) {
        Object v = args.get(anahtar);
        return v == null ? varsayilan : v.toString();
    }

    static Integer tamsayiOpsiyonel(Map<String, Object> args, String anahtar) {
        Object v = args.get(anahtar);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        String s = v.toString().trim();
        return s.isBlank() ? null : Integer.parseInt(s);
    }
}
