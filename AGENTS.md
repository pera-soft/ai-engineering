# AGENTS.md — ai_engineering (AI Öğretmen)
> İçindekiler tablosudur, ansiklopedi değil. Detay: docs/ ve ARCHITECTURE.md.

## Komutlar
- Derle+test: `./mvnw verify`
- Çalıştır: `OPENROUTER_API_KEY=... ./mvnw spring-boot:run`  (http://127.0.0.1:8090; sağlayıcı: OpenRouter, ADR-0004)
  - Mülakat modülü varsayılan KAPALI; açmak için başına `APP_MULAKAT_ETKIN=true` ekle (ADR-0011)
  - Girişsiz çok ziyaretçili site (ADR-0017): `--spring.profiles.active=site` + `SPRING_DATASOURCE_URL`
    (yerelde: `SPRING_DATASOURCE_URL=... ./mvnw spring-boot:run -Dspring-boot.run.profiles=site`). Giriş/kayıt/
    e-posta YOK; ziyaretçi `aio_ziyaretci` çereziyle tanınır. Sitede `OPENROUTER_API_KEY` GEREKMEZ (verilse de
    kullanılmaz) — sohbeti ziyaretçi kendi anahtarıyla yapar; sohbet dışı AI uçları sitede kapalı.
    Yerel http denemesi: `APP_SITE_GUVENLI_CEREZ=false`
  - Site veritabanı (ADR-0014): `SPRING_DATASOURCE_URL` (+ `_USERNAME`, `_PASSWORD`) ŞART — canlıda
    PostgreSQL; yoksa açılmaz. Yerel deneme: `SPRING_DATASOURCE_URL='jdbc:h2:file:./data/site-deneme'`
  - Site hız sınırları (isteğe bağlı, tek NAT arkasındaki sınıf/ofis için): `APP_SITE_HIZ_*` (yazma/dk 60, sohbet/dk 20, geri-yükle/saat 10, yeni ziyaretçi/saat 20; `docs/kurulum-site.md` §7.6)
  - Site dağıtımı/işletmesi: `docs/kurulum-site.md` (runbook) + `deploy/` (systemd, nginx, env, PostgreSQL
    şablonları — gerçek host/IP/sır YOK); nginx arkasında `APP_SITE_IZINLI_ORIGINLER`
- PostgreSQL uyumluluk testi (opt-in, atılabilir DB ile): `PG_TEST_URL=jdbc:postgresql://localhost:5432/<db>
  PG_TEST_USERNAME=... ./mvnw -Dtest=SitePostgresEntegrasyonTest test` (değişken yoksa atlanır)
- Sadece mimari testleri: `./mvnw -Dtest=MimariTestleri test`
- Müfredat üret (HTML → content/): `node tools/html-to-content.mjs`

## Sınırlar
- ALWAYS: KULLANICI İNGİLİZCE BİLMİYOR — verdiği İngilizce kaynaklar Türkçe çalışma
  metnine çevrilir (content/kaynaklar/), tüm eğitim içeriği ve AI çıktıları Türkçe olur;
  teknik terimlerin İngilizcesi parantezle verilir.
- ALWAYS: types → config → repo → service → ai → web katman yönüne uy (ArchUnit zorlar).
- ALWAYS: Claude çağrısı yalnız ai/ paketinden; prompt'lar prompts/ dizininden.
- ALWAYS: yeni davranışa test; API değişikliğinde bu dosyadaki komutları güncelle.
- ALWAYS: ANLAMLI iş bitince Obsidian Vault'a yaz (eşik: "3 ay sonraki okuyucu bunu bilmek
  ister mi?"). İLK vault işleminden önce docs/obsidian-vault-rehberi.md'yi OKU — sıra,
  terfi sorusu ve link disiplini orada, bağlayıcıdır.
- ALWAYS: yeni/büyüyen kaynak metni (content/kaynaklar/) konvansiyona uyar: **3 inline SVG
  diyagram** (```svg çit; CSP-temiz — `<style>`/`class` YOK, yalnız presentation attribute;
  dosyaya özgü marker id öneki) + öz-sınav + satıcı iddiasına şerh. Render doğrula:
  `/api/kaynak/{slug}` → 3 svg. İstisna: rapor/soru bankası türü metinler diyagramsız kalabilir.
- ASK FIRST: yeni bağımlılık (önce docs/adr/'ye ADR).
- NEVER: API anahtarını koda/git'e koymak; **Obsidian Vault'a şifre/token/secret yazmak**
  (vault kuralı); 127.0.0.1 dışına bind — uygulama daima 127.0.0.1'e bağlanır; public erişim
  yalnız nginx/TLS önünden (ADR-0007 Faz 2b); content/ altını kod değişikliğiyle aynı PR'da
  yeniden üretmek.

## Nerede ne var
- Mimari: ARCHITECTURE.md · Kararlar: docs/adr/ · Planlar: docs/exec-plans/
- Müfredat: content/ (frontmatter'lı md) · Prompt şablonları: prompts/
- Öğrenci hafızası (Faz 2, AI yazar): data/hafiza/ (git dışı)
- Borç listesi: docs/tech-debt-tracker.md
- Oturum defterleri: thoughts/ledgers/ (git DIŞI — sunucu/SSH ayrıntısı içerir; /clear öncesi güncellenir)
- İkinci beyin (git dışı): `~/Documents/Obsidian Vault/AI ÖĞRETMEN/` — kurallar:
  docs/obsidian-vault-rehberi.md (İLK vault işleminden önce OKU)
- MCP sunucusu: kod duruyor (`mcp-http` profili, ADR-0007); canlı sunucular (ai.velzon.tr,
  mulakat.velzon.tr) 2026-10-21'de KAPATILDI — canlı dağıtım/senkron yok, uygulama yalnız yerelde.

## Herkese açık yayın (pera-soft/ai-engineering)
- NEVER: çalışma dalını pera-soft'a push etmek — geçmişi mülakat dökümü/sunucu ayrıntısı taşır.
- Yayın: `bash tools/yayin-hazirla.sh` (sızıntı taraması + geçmişsiz `pera-soft-yayin` dalına
  yeni commit) → `git push pera-soft pera-soft-yayin:main`
- Kilit: `git config core.hooksPath tools/git-kancalari` (pre-push yalnız pera-soft-yayin'e izin verir)

## PR / commit kuralları
- Küçük, tek amaçlı commit'ler; mesaj: `[faz-N] özet`
- Merge öncesi `./mvnw verify` (birim + ArchUnit) YEŞİL olmalı.
