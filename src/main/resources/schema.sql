-- H2 şeması — CLAUDE_CODE_BRIEF.md §6. Idempotent (IF NOT EXISTS); her açılışta çalışır.
--
-- Faz 2a-3: KULLANICI-KİMLİKLİ İZOLASYON (ADR-0008). Dört ilerleme tablosuna kullanici_id
-- eklendi (VARCHAR(255) NOT NULL DEFAULT 'yerel'). Böylece mevcut TEK-KULLANICI veri kaybolmaz:
-- kolon eklendiğinde mevcut satırlar otomatik 'yerel' değerini alır (web profili = tek kullanıcı).
-- mcp-http (JWT) profilinde her kullanıcı kendi email/sub'ı ile izole olur.
--
-- MİGRASYON STRATEJİSİ (hem yeni kurulum hem MEVCUT data/ogrenme dosya DB'si çalışsın):
--   * Yeni kurulum: aşağıdaki CREATE TABLE ... zaten kullanici_id + doğru UNIQUE ile kurar.
--   * Mevcut DB: CREATE TABLE IF NOT EXISTS mevcut tabloyu ATLAR → kolonu ALTER ... ADD COLUMN
--     IF NOT EXISTS ekler; eski (kullanıcısız) UNIQUE kısıtı düşürülüp kullanıcıya-bölünmüş
--     yenisi eklenir. Tüm ifadeler IF [NOT] EXISTS ile idempotent — her açılışta güvenle çalışır.
-- ai_kullanim DEĞİŞMEZ: MCP LLM çağırmaz; token takibi yalnız web/sunucu-LLM tarafıdır (kullanıcı ayrımı gerekmez).

CREATE TABLE IF NOT EXISTS gorev_ilerleme (
  id IDENTITY PRIMARY KEY,
  kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel',
  hafta INT NOT NULL, gun INT NOT NULL, gorev_index INT NOT NULL,
  tamamlandi BOOLEAN NOT NULL DEFAULT FALSE,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_gorev_ilerleme_kullanici UNIQUE (kullanici_id, hafta, gun, gorev_index)
);
-- Mevcut DB migrasyonu (yeni kurulumda no-op):
ALTER TABLE gorev_ilerleme ADD COLUMN IF NOT EXISTS kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel';
-- Eski kullanıcısız UNIQUE(hafta,gun,gorev_index) — H2 otomatik adı CONSTRAINT_BB — çok kullanıcıyı
-- engeller (iki kullanıcı aynı görevi işaretleyemezdi); düşür. IF EXISTS → yeni kurulumda no-op.
ALTER TABLE gorev_ilerleme DROP CONSTRAINT IF EXISTS CONSTRAINT_BB;
ALTER TABLE gorev_ilerleme ADD CONSTRAINT IF NOT EXISTS uq_gorev_ilerleme_kullanici
  UNIQUE (kullanici_id, hafta, gun, gorev_index);

CREATE TABLE IF NOT EXISTS gonderim (            -- kullanıcının yazdığı özet/cevap/iş (Faz 2)
  id IDENTITY PRIMARY KEY,
  kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel',
  hafta INT NOT NULL, gun INT,
  tur VARCHAR(20) NOT NULL,                       -- OZET | GOREV_CIKTISI | SORU_CEVABI
  icerik CLOB NOT NULL,
  degerlendirme_json CLOB,
  puan INT,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE gonderim ADD COLUMN IF NOT EXISTS kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel';
CREATE INDEX IF NOT EXISTS ix_gonderim_kullanici_hafta ON gonderim (kullanici_id, hafta);

CREATE TABLE IF NOT EXISTS quiz_sonuc (
  id IDENTITY PRIMARY KEY,
  kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel',
  hafta INT NOT NULL,
  tur VARCHAR(10) NOT NULL,                       -- STATIK | AI_URETIM
  puan INT NOT NULL,
  detay_json CLOB,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE quiz_sonuc ADD COLUMN IF NOT EXISTS kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel';
CREATE INDEX IF NOT EXISTS ix_quiz_sonuc_kullanici_hafta ON quiz_sonuc (kullanici_id, hafta);

CREATE TABLE IF NOT EXISTS sohbet_mesaj (         -- Faz 2
  id IDENTITY PRIMARY KEY,
  kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel',
  hafta INT NOT NULL,
  rol VARCHAR(10) NOT NULL,                       -- USER | ASSISTANT
  icerik CLOB NOT NULL,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE sohbet_mesaj ADD COLUMN IF NOT EXISTS kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel';
CREATE INDEX IF NOT EXISTS ix_sohbet_mesaj_kullanici_hafta ON sohbet_mesaj (kullanici_id, hafta);

CREATE TABLE IF NOT EXISTS mulakat_cevap (        -- Faz 1 (Mülakat): kullanıcının soru cevapları
  id IDENTITY PRIMARY KEY,
  kullanici_id VARCHAR(255) NOT NULL DEFAULT 'yerel',
  soru_id VARCHAR(100) NOT NULL, alan_slug VARCHAR(100) NOT NULL, konu_slug VARCHAR(100) NOT NULL,
  secilen_index INT NOT NULL, dogru BOOLEAN NOT NULL, tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Ana sorgu: kullanıcının cevapları tarih DESC (hataliSorularTekrar); id DESC tie-break sorgu tarafında.
CREATE INDEX IF NOT EXISTS ix_mulakat_cevap_kullanici_tarih ON mulakat_cevap (kullanici_id, tarih DESC);

CREATE TABLE IF NOT EXISTS ai_kullanim (           -- token/maliyet takibi (brief 7.6)
  id IDENTITY PRIMARY KEY,
  islem VARCHAR(30) NOT NULL,                     -- DEGERLENDIRME | SOHBET | HAFIZA | QUIZ | RAPOR | MULAKAT
  model VARCHAR(50) NOT NULL,
  input_tokens INT NOT NULL DEFAULT 0,
  output_tokens INT NOT NULL DEFAULT 0,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
