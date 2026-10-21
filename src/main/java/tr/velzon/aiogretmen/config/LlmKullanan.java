package tr.velzon.aiogretmen.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bu servis LLM (OgretmenAiClient) ÇAĞIRIR — yani sunucu-taraflı token maliyeti üretir.
 *
 * <p><strong>Mühür (ADR-0009):</strong> {@code mcp/} paketi bu anotasyonla işaretli hiçbir sınıfa
 * BAĞIMLI OLAMAZ. Böylece "MCP LLM'siz kalır; muhakeme bağlanan istemcide" ilkesi (ADR-0007)
 * konvansiyon olmaktan çıkıp {@code MimariTestleri}'nde mekanik olarak zorlanır (kural kırılırsa
 * build KIRMIZI). mcp'nin ihtiyaç duyduğu token-SIZ okumalar (ör. statik katalog) işaretsiz
 * servislerden verilmelidir (bkz. {@code AtolyeKatalogService}).
 *
 * <p>Yalnız {@code aiClient.aktif()} gibi durum sorgusu yapan (token üretmeyen) servisler bu
 * anotasyonu ALMAZ — mühür token maliyetini hedefler, tip bağımlılığını değil.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LlmKullanan {
}
