# ADR-0009: MCP LLM-siz mührünün mekanikleştirilmesi (@LlmKullanan + ArchUnit)

## Durum (Status)
Accepted — 2026-07-24

## Bağlam (Context)
ADR-0007 kararı: **MCP sunucusu LLM çağırmaz; muhakeme bağlanan istemcide kalır** — böylece
sunucu MCP tarafında token maliyeti oluşmaz, MCP yalnız içerik/durum primitifleri sunar. Bu ilke
şimdiye dek iki yolla korunuyordu:

1. `MimariTestleri.claude_yalniz_ai_paketinden` — `ai/` dışındaki hiçbir sınıf Spring AI'yı
   (`org.springframework.ai..`) import edemez. Bu, mcp'nin LLM'yi DOĞRUDAN çağırmasını engeller.
2. Konvansiyon — "mcp servisleri LLM'siz olmalı" (yazılı kural, mekanik değil).

Boşluk: mcp bir servisi enjekte edip o servis LLM çağırıyorsa, kural (1) bunu görmez (Spring AI
import'u serviste, mcp'de değil). Nitekim `OgretmenMcpAraclari`/`OgretmenMcpKaynaklari`, Prompt
Atölyesi katalogu için `AtolyeService`'i enjekte ediyordu — ve `AtolyeService.degerlendir` LLM
çağırır. mcp yalnız statik `kategoriler()`'i kullansa da, TİP düzeyinde LLM üreten bir servise
tutunuyordu: gelecekte biri mcp'den `degerlendir`i çağırabilirdi (latent token-sızıntısı yolu).

## Karar (Decision)

### 1. `@LlmKullanan` marker anotasyonu (config katmanı)
LLM (`OgretmenAiClient`) ÇAĞIRAN — yani token maliyeti üreten — her servis
`config/LlmKullanan` (`@Retention(RUNTIME) @Target(TYPE)`) ile işaretlendi:
`Kitap`, `Degerlendirme`, `Atolye`, `MulakatAciklama`, `Hafiza`, `QuizUretim`, `Rapor`, `Ornek`,
`Sohbet` servisleri. **Yalnız durum sorgusu yapanlar işaretlenmez**: `KullanimService` sadece
`aiClient.aktif()` (boolean) çağırır, token üretmez → mühür token maliyetini hedefler, tip
bağımlılığını değil.

### 2. ArchUnit kuralı (`MimariTestleri.mcp_llmsiz`)
```
noClasses().that().resideInAPackage("..aiogretmen.mcp..")
    .should().dependOnClassesThat().areAnnotatedWith(LlmKullanan.class)
```
Konvansiyon artık mekanik: mcp `@LlmKullanan` bir sınıfa bağlanırsa build KIRMIZI olur.

### 3. Token-siz katalog seam (`AtolyeKatalogService`)
mcp'nin ihtiyaç duyduğu tek Atölye verisi statik katalogdur (`kategoriler()`). Kuralı YEŞİL yapmak
için — ve latent yolu gerçekten kapatmak için — `AtolyeRepo.kategoriler()`'i saran, LLM'siz
(işaretsiz) `AtolyeKatalogService` eklendi. mcp bu seam'i enjekte eder; `AtolyeService` (LLM'li,
`@LlmKullanan`) yalnız web'de değerlendirme için kullanılır. Katman yönü korunur (mcp → service).

## Değerlendirilen alternatifler
- **Sadece konvansiyon (kural eklememek)** — mevcut boşluğu bırakırdı; "sıfır teknik borç" hedefiyle
  çelişir. Reddedildi.
- **`AtolyeService`'i işaretlememek** — mühür yalancı olurdu (LLM çağıran servis işaretsiz); mcp yine
  LLM'li servise tutunurdu. Reddedildi.
- **`AtolyeService`'i çağrı düzeyinde bölmek yerine mcp'yi repo'ya bağlamak** — katman_kurali repo'ya
  yalnız service/ai erişimine izin verir; mcp repo'ya erişemez. Seam (yeni servis) doğru katman çözümü.

## Sonuçlar (Consequences)
- (+) ADR-0007 mührü mekanik: mcp'nin LLM'e (token'a) ulaşan hiçbir yolu ArchUnit'te YEŞİL kanıtlı.
- (+) Latent yol kapandı: mcp artık token üreten bir servise TİP düzeyinde bile tutunmaz.
- (+) `@LlmKullanan` gelecekteki servisler için kendini-belgeleyen bir işaret (token maliyeti taşır).
- (−) Küçük yüzey artışı: `AtolyeKatalogService` (ince seam) + 9 servise anotasyon; mcp wiring/testleri
  katalog seam'ine güncellendi (davranış değişmedi).
