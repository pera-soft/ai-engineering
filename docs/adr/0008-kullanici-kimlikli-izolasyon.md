# ADR-0008: Kullanıcı-kimlikli veri izolasyonu (Faz 2a-3)

## Durum (Status)
Accepted — 2026-07-16

## Bağlam (Context)
Faz 2a-1/2a-2 ile MCP artık streamable-HTTP + OAuth 2.1 (JWT) ile ÇOK kullanıcı taşıyabiliyor
(`mcp/McpKullaniciKimligi` doğrulanmış email/sub döndürüyor). Ama ilerleme verisi (görev, gönderim,
quiz, sohbet) hâlâ TEK kovada: dört tablonun hiçbirinde kullanıcı ayrımı yoktu. İki kullanım modu var:

- **web** (`!mcp-http`, 127.0.0.1:8090, auth YOK): tek kişisel kullanıcı — yerel ilerleme.
- **mcp-http** (JWT): her kullanıcı kendi kimliğiyle.

Mevcut yerel veri KAYBEDİLMEDEN her kullanıcının ilerlemesi izole edilmeli.

## Karar (Decision)

### 1. Şema — `kullanici_id` (geriye-uyumlu migration)
Dört tabloya (`gorev_ilerleme`, `gonderim`, `quiz_sonuc`, `sohbet_mesaj`)
`kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel'` eklendi. `ai_kullanim` DEĞİŞMEDİ (MCP LLM
çağırmaz; token takibi sunucu-LLM tarafına ait, kullanıcı ayrımı gereksiz).

`schema.sql` her açılışta çalıştığından migration **hem yeni kurulum hem MEVCUT dosya DB'si** için
idempotent yazıldı:
- `CREATE TABLE IF NOT EXISTS ...` yeni kurulumu `kullanici_id` + doğru UNIQUE ile kurar.
- Mevcut DB'de tablo atlanır → `ALTER TABLE ... ADD COLUMN IF NOT EXISTS kullanici_id ... DEFAULT 'yerel'`
  kolonu ekler; **mevcut satırlar otomatik `'yerel'` alır (veri kaybı yok)**.
- `gorev_ilerleme`'nin eski kullanıcısız `UNIQUE(hafta,gun,gorev_index)` kısıtı (H2 otomatik adı
  `CONSTRAINT_BB`) çok-kullanıcıyı engellerdi → `DROP CONSTRAINT IF EXISTS CONSTRAINT_BB` +
  `ADD CONSTRAINT IF NOT EXISTS uq_gorev_ilerleme_kullanici UNIQUE(kullanici_id,hafta,gun,gorev_index)`.
- Diğer üç tabloya `(kullanici_id, hafta)` index'i eklendi (yeni erişim deseni).

Mevcut `data/ogrenme` DB'sinin BİR KOPYASI üzerinde doğrulandı: eski satırlar korundu, `'yerel'` oldu;
migration 2./3. çalıştırmada da hatasız (idempotent); iki kullanıcı aynı (hafta,gun,gorev)'i
işaretleyebiliyor, aynı kullanıcı ikinci kez işaretleyince UNIQUE ihlali (per-user teklik korunur).

### 2. `KullaniciBaglami` soyutlaması (profil ayrımı)
Kimlik kaynağı profile göre değişir. `config` katmanında `KullaniciBaglami` arayüzü + iki bean:
- `YerelKullaniciBaglami` (`@Profile("!mcp-http")`) → sabit `"yerel"` (web + stdio, tek kullanıcı).
- `JwtKullaniciBaglami` (`@Profile("mcp-http")`) → SecurityContext'ten email/sub; kimlik yoksa
  **fail-closed** (`IllegalStateException`) — sessizce `"yerel"`e düşüp veri sızdırmaz.

`config` katmanı seçildi (katman_kurali: config'e repo/service/ai/web/mcp erişebilir). `JwtKullaniciBaglami`
yalnız Spring Security kullanır (Spring AI DEĞİL) → `claude_yalniz_ai_paketinden` YEŞİL; `mcp` paketine
bağımlı değildir (config→mcp yasak) — `McpKullaniciKimligi` ile aynı çıkarımı bağımsız yapar.

### 3. Kimlik iki yoldan akıtılır
- **MCP tool sınırı** (async/istemci sınırı — explicit identity): `mcp/OgretmenMcpAraclari` tool'ları
  kimliği `McpKullaniciKimligi.kullaniciKimligiVeyaYerel()` ile ALIR ve `IlerlemeService`/`MufredatService`
  metotlarına **açık parametre** olarak geçirir. stdio'da JWT yok → `"yerel"` (web ile aynı kova);
  mcp-http'de auth zorunlu → email/sub.
- **Web + prompt yolu**: `ProgramController` kimliği `KullaniciBaglami`'dan alıp servise geçirir;
  AI servisleri (`BaglamServisi`, `Degerlendirme`, `Sohbet`, `Rapor`, `Sifirlama`) `KullaniciBaglami`'yı
  enjekte edip istek-kapsamında çözer (endpoint imzaları değişmedi).

Tüm repo sorguları `kullanici_id` ile filtrelenir; `Sifirlama` yalnız aktif kullanıcının satırlarını siler
(`deleteByKullaniciId`).

## KVKK / kişisel veri notu
`email` = kişisel veridir. **Minimizasyon**: yalnız izolasyon için gereken kimlik (email; yoksa `sub`)
saklanır — ad/soyad, telefon vb. gereksiz PII saklanmaz. Kimlik doğrulanmış JWT'den okunur; uygulama
kendi parola/PII deposunu tutmaz (AS harici — ADR-0007). Web (tek kullanıcı) modunda kimlik sabit
`"yerel"`dir, kişisel veri değildir. "Temiz başlangıç" kullanıcının kendi verisini siler (silme hakkı).

## Değerlendirilen alternatifler
- **Servislerde tek-tip `KullaniciBaglami` enjeksiyonu (parametresiz)** — en az kod; ama MCP tool
  sınırında "current SecurityContext"e derin servis içinde güvenmek, explicit-identity ilkesine aykırı
  (thread/dispatch değişirse yanlış kova). Bu yüzden MCP yolunda **açık parametre** tercih edildi.
- **`ThreadLocal`/scope tabanlı global kimlik** — gizli bağımlılık; test edilmesi zor; reddedildi.

## Sonuçlar (Consequences)
- (+) İzolasyon + geriye-uyum TDD ile kanıtlı (`KullaniciIzolasyonTest`); mevcut yerel veri korunur.
- (+) Katman/ArchUnit ve LLM-izolasyon kuralları YEŞİL (grep boş); MCP paketi LLM'siz kaldı.
- (+) `KullaniciBaglami` yeni bir `config` bean'i — ArchUnit katman yönü korunur (yeni katman gerekmedi).
- (−) `IlerlemeService`/`MufredatService` imzalarına `kullaniciId` parametresi eklendi (çağıranlar güncellendi).
- (−) `JwtKullaniciBaglami` fail-closed: mcp-http'de kimliksiz istek 500 üretir (kasıtlı — sızıntı yerine hata).
