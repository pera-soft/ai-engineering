# CLAUDE_CODE_BRIEF.md — "AI Öğretmen" Projesi

> Bu belge, Claude Code'a verilecek tam proje talimatıdır.
> Proje dizini: `/Users/felece/Dev/ai_engineering`
> Bu belgeyi proje köküne koy ve Claude Code'a "Bu brief'i oku ve Faz 0'dan başla" de.
> Fazlar sıralıdır; her fazın kabul kriterleri YEŞİL olmadan sonraki faza geçme.

---

## 1. VİZYON — Ne yapıyoruz?

Kişisel, yerel çalışan bir **yapay zekâ destekli öğrenme koçu** yapıyoruz.

Kullanıcı (Velzon) 12 haftalık bir "AI Çağı Yazılım Mimarlığı" programını takip ediyor.
Uygulama bu programın içeriğini markdown dosyalarından okur, kullanıcının ilerlemesini
takip eder ve **arka planda Anthropic API (Claude) ile çalışan bir "öğretmen"** olarak:

1. Kullanıcının yazdığı özetleri / cevapları / yaptığı işleri **değerlendirir** →
   "şu doğru, şu eksik, şunu yanlış anlamışsın, şunu şu şekilde öğrenmen lazım" der.
2. **Sokratik sohbet** modunda ders anlatır: hazır cevap vermek yerine soru sorarak
   düşündürür (Conceptual Inquiry deseni — kavramayı en çok artıran kullanım şekli).
3. Ders içeriğinden **ek quiz soruları üretir** ve cevapları puanlar.
4. Hafta sonunda **ilerleme raporu** çıkarır: güçlü yönler, zayıf noktalar, tekrar planı.

Bu proje aynı zamanda kullanıcının 12 haftalık programının CANLI UYGULAMASIDIR:
AGENTS.md, docs/, ADR, katman mimarisi, ArchUnit — programda öğrenilen her pratik
bu repoda bizzat kullanılacak. Yani proje hem araç hem ödevdir.

---

## 2. TEKNOLOJİ KARARLARI (tartışmasız, bunlarla ilerle)

| Konu | Karar | Gerekçe |
|---|---|---|
| Backend | **Java 21 + Spring Boot 3.4+ + Spring AI (Anthropic starter)** | Kullanıcı Java developer; Spring AI öğrenmek programın hedeflerinden biri |
| Frontend | **Statik HTML + vanilla JS** (Spring'in `static/` klasöründen servis edilir) | Basitlik; build zinciri yok; tek kişilik yerel araç |
| Veritabanı | **H2 (file mode)** — `./data/ogrenme.mv.db` | Sıfır kurulum, yerel kalıcılık |
| İçerik | **Markdown dosyaları** `content/` altında (frontmatter'lı) | Ajan-okunabilir, versiyonlanabilir, elle düzenlenebilir |
| AI modelleri | Değerlendirme + sohbet: `claude-sonnet-4-6` · Ucuz işler (başlık, sınıflandırma): `claude-haiku-4-5` | Model kademeleme — maliyet mühendisliği pratiği |
| Port | `127.0.0.1:8090` — SADECE localhost'a bind | Yerel kişisel araç; dışarı açılmaz |
| API anahtarı | `ANTHROPIC_API_KEY` ortam değişkeni. Koda / git'e ASLA gömme | Güvenlik temel kuralı |
| Build | Maven (`./mvnw`) | Standart |

Bağımlılıklar (pom.xml):
- `spring-boot-starter-web`
- `spring-ai-anthropic-spring-boot-starter` (Spring AI BOM ile; güncel sürümü Spring AI dokümantasyonundan doğrula)
- `spring-boot-starter-data-jdbc`
- `com.h2database:h2`
- `org.commonmark:commonmark` (markdown → HTML)
- Test: `spring-boot-starter-test`, `com.tngtech.archunit:archunit-junit5`

---

## 3. MİMARİ — Katmanlı, tek yönlü, mekanik zorlamalı

Paket kökü: `tr.velzon.aiogretmen`

Katman yönü (OpenAI harness yazısındaki desenin aynısı, tek yönlü):

```
types → config → repo → service → runtime(ai) → web(ui)
```

```
tr.velzon.aiogretmen
├── types/        # record'lar, enum'lar, DTO'lar. HİÇBİR ŞEYE bağımlı değil.
│                 # Ders, Gun, Gorev, QuizSorusu, Degerlendirme, IlerlemeDurumu...
├── config/       # AppProperties (@ConfigurationProperties), model adları,
│                 # content dizin yolu, prompt şablon yolları
├── repo/         # H2 erişimi (Spring Data JDBC): IlerlemeRepo, GonderimRepo,
│                 # QuizSonucRepo, SohbetRepo + ContentRepo (markdown okuyucu)
├── service/      # İş mantığı: MufredatService, IlerlemeService,
│                 # DegerlendirmeService, QuizService, RaporService
├── ai/           # (runtime katmanı) Claude entegrasyonu: OgretmenAiClient,
│                 # PromptFactory, JsonCevapParser. Spring AI ChatClient burada yaşar.
│                 # service katmanı AI'a SADECE buradaki arayüz üzerinden erişir.
└── web/          # REST controller'lar + statik frontend. Sadece service'i çağırır.
```

Kurallar:
- `types` hiçbir katmana bağımlı olamaz.
- `web` doğrudan `repo` veya `ai`'a erişemez — her şey `service` üzerinden.
- Claude API çağrısı SADECE `ai` paketinde yapılır (tek çıkış noktası).
- Bu kurallar **ArchUnit testi** ile CI'da bloklayıcı olarak zorlanır (Faz 1'de yazılacak).

---

## 4. DİZİN YAPISI (repo kökü)

```
ai_engineering/
├── AGENTS.md                 # ~100 satır, içindekiler tablosu (içeriği Bölüm 10'da)
├── CLAUDE.md -> AGENTS.md    # symlink: ln -s AGENTS.md CLAUDE.md
├── CLAUDE_CODE_BRIEF.md      # bu dosya
├── ARCHITECTURE.md           # katman haritası + kurallar (kısa)
├── docs/
│   ├── adr/                  # ADR-0001'den itibaren (ilk ADR'ler Bölüm 11'de)
│   ├── exec-plans/
│   │   ├── active/
│   │   └── completed/
│   └── tech-debt-tracker.md
├── content/                  # MÜFREDAT — markdown kaynak (format Bölüm 5'te)
│   ├── program.md            # programın genel tanıtımı + hafta indeksi
│   ├── hafta-01/
│   │   ├── hafta.md          # haftanın başlık/hedef/kaynak bilgisi + quiz
│   │   ├── gun-1.md ... gun-5.md
│   ├── hafta-02/ ... hafta-12/
├── prompts/                  # AI prompt şablonları (versiyonlanır — prompt da koddur)
│   ├── degerlendirme.st
│   ├── sokratik-sohbet.st
│   ├── quiz-uret.st
│   └── haftalik-rapor.st
├── src/main/java/...         # Bölüm 3'teki yapı
├── src/main/resources/
│   ├── application.yml
│   └── static/               # index.html, app.js, style.css
├── src/test/java/...         # birim testler + MimariTestleri (ArchUnit)
└── data/                     # H2 dosyası (gitignore'da)
```

---

## 5. İÇERİK FORMATI (content/*.md)

**ÖNEMLİ:** Müfredatın tamamı, proje köküne kopyalanacak olan `mimar-programi.html`
dosyasının içindeki `const PROGRAM = [...]` JavaScript dizisinde hazır duruyor
(12 hafta × 5 gün: başlıklar, ders özetleri, görevler ve 5'er soruluk quiz'ler,
cevap ve açıklamalarıyla). **Faz 0'da bu diziyi parse edip aşağıdaki markdown
formatına dönüştüren tek seferlik bir script yaz ve çalıştır** (Java veya basit
bir Node/Python scripti olabilir; script `tools/` altına konur).

`content/hafta-01/hafta.md` formatı:

```markdown
---
hafta: 1
baslik: "Harness Mühendisliği Temelleri"
hedef: "Rolün nasıl değiştiğini kavra: ..."
kaynak: "OpenAI 'Harness Engineering' + martinfowler.com (Böckeler)"
---

## Quiz
### S1: OpenAI deneyinde insan mühendislerin birincil işi neye dönüştü?
- [ ] Daha hızlı kod yazmak
- [x] Ajanların güvenilir çalışacağı ortamı, spec'leri ve geri-besleme döngülerini tasarlamak
- [ ] Ajan çıktısını satır satır yeniden yazmak
- [ ] Sadece toplantı yönetmek
> Açıklama: Yazının özü: 'design environments, specify intent...'
```

`content/hafta-01/gun-1.md` formatı:

```markdown
---
hafta: 1
gun: 1
baslik: "'Humans steer, agents execute' ilkesi"
---

## Ders
(HTML'deki `d` alanındaki ders özeti buraya, em etiketleri temizlenmiş halde)

## Görevler
- [ ] OpenAI Harness Engineering yazısını oku
- [ ] Kendi cümlelerinle 5 maddede 'benim yeni rolüm ne?' yaz
- [ ] Ekibindeki mevcut iş akışında insanların hâlâ elle yaptığı 3 işi listele
```

ContentRepo bu dosyaları başlangıçta okur, frontmatter'ı parse eder ve bellekte
`Mufredat` modeli olarak tutar (uygulama yeniden başlatılınca içerik değişiklikleri
yansır; hot-reload gerekmiyor).

---

## 6. VERİTABANI ŞEMASI (H2, schema.sql ile)

```sql
CREATE TABLE IF NOT EXISTS gorev_ilerleme (
  id IDENTITY PRIMARY KEY,
  hafta INT NOT NULL, gun INT NOT NULL, gorev_index INT NOT NULL,
  tamamlandi BOOLEAN NOT NULL DEFAULT FALSE,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (hafta, gun, gorev_index)
);

CREATE TABLE IF NOT EXISTS gonderim (            -- kullanıcının yazdığı özet/cevap/iş
  id IDENTITY PRIMARY KEY,
  hafta INT NOT NULL, gun INT,
  tur VARCHAR(20) NOT NULL,                       -- OZET | GOREV_CIKTISI | SORU_CEVABI
  icerik CLOB NOT NULL,
  degerlendirme_json CLOB,                        -- AI değerlendirmesi (Bölüm 7.2 şeması)
  puan INT,                                       -- 0-100
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quiz_sonuc (
  id IDENTITY PRIMARY KEY,
  hafta INT NOT NULL,
  tur VARCHAR(10) NOT NULL,                       -- STATIK | AI_URETIM
  puan INT NOT NULL,
  detay_json CLOB,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sohbet_mesaj (
  id IDENTITY PRIMARY KEY,
  hafta INT NOT NULL,
  rol VARCHAR(10) NOT NULL,                       -- USER | ASSISTANT
  icerik CLOB NOT NULL,
  tarih TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. AI ÖĞRETMEN — çekirdek özellikler

Tüm Claude çağrıları `ai/OgretmenAiClient` üzerinden. Spring AI `ChatClient` kullan.
Prompt şablonları `prompts/` dizininden okunur (koda string gömme — prompt da koddur,
versiyonlanır). Her şablonun başına yorum satırıyla sürüm notu koy.

### 7.1 Ortak sistem bağlamı
Her çağrıda sistem promptuna şunlar enjekte edilir:
- Aktif haftanın `hafta.md` + ilgili günün `gun-N.md` içeriği (ham markdown)
- Kullanıcının o haftaki ilerleme özeti (tamamlanan görevler, quiz skorları, son gönderim puanları)
- Kimlik: "Sen deneyimli bir yazılım mimarı ve sabırlı bir öğretmensin. Öğrencin
  Java geliştiricisi, AI-destekli ekip liderliğine ve mimarlığa hazırlanıyor.
  Türkçe konuş. Teknik terimlerin İngilizcesini parantezle ver."

### 7.2 Değerlendirme (ana özellik)
`POST /api/degerlendir` → body: `{hafta, gun, tur, icerik}`

Kullanıcı bir görev çıktısı / günlük özet / serbest cevap yollar. Claude,
`prompts/degerlendirme.st` şablonuyla değerlendirir ve **YALNIZCA JSON** döner
(şablonda açıkça belirt: "Markdown backtick'i ve JSON dışında hiçbir şey yazma"):

```json
{
  "puan": 0-100,
  "ozet": "tek cümlelik genel değerlendirme",
  "dogru_noktalar": ["..."],
  "eksikler": ["..."],
  "yanlis_anlamalar": [{"ne": "...", "dogrusu": "...", "neden_onemli": "..."}],
  "sonraki_adim": "öğrencinin YARINKİ somut adımı (tek, net, yapılabilir)",
  "tekrar_onerisi": "hangi ders/kavram tekrar edilmeli (yoksa null)"
}
```

Kurallar:
- JSON parse hatasında 1 kez "sadece geçerli JSON döndür" düzeltme çağrısı yap;
  yine bozuksa ham metni `ozet` alanına koyup `puan: null` ile kaydet (uygulama çökmez).
- Değerlendirme rubriği şablonda sabit: kavram doğruluğu (40) + uygulanabilirlik (30)
  + derinlik/bağ kurma (20) + ifade netliği (10).
- Sonuç `gonderim` tablosuna yazılır; frontend puanı ve maddeleri kart olarak gösterir.

### 7.3 Sokratik sohbet
`POST /api/sohbet` → body: `{hafta, mesaj}` — hafta bazlı sohbet geçmişi DB'de tutulur,
son 20 mesaj bağlama eklenir.

`prompts/sokratik-sohbet.st` davranış kuralları (şablona yaz):
- Doğrudan cevap İSTENMEDİKÇE verme; önce yönlendirici soru sor (en fazla 1 soru/mesaj).
- Öğrenci 2 kez üst üste takılırsa cevabı ver ama ardından "kendi cümlenle özetle" iste.
- Ders içeriği dışına çıkan sorularda kısaca cevapla ve derse bağla.
- Kod istenirse önce yaklaşımı anlattır, sonra kod ver.

### 7.4 AI quiz üretimi
`POST /api/quiz/uret` → body: `{hafta, adet}` (varsayılan 5)
`prompts/quiz-uret.st`: haftanın TÜM gün içeriklerinden, statik quizde OLMAYAN
yeni çoktan seçmeli sorular üretir. JSON: `[{soru, secenekler[4], dogru_index, aciklama}]`.
Frontend statik quizle aynı arayüzde gösterir; sonuç `quiz_sonuc(tur=AI_URETIM)` olarak kaydedilir.

### 7.5 Haftalık rapor
`GET /api/rapor/{hafta}` → o haftanın tüm verisi (görevler, gönderimler+değerlendirmeler,
quiz sonuçları, sohbet başlıkları) `prompts/haftalik-rapor.st` ile Claude'a verilir. Çıktı
markdown: Güçlü yönler / Zayıf noktalar / Yanlış anlaşılan kavramlar / Gelecek hafta için
3 maddelik kişisel plan / Haftayı geçti mi (görev %80 + quiz ≥60 kuralına göre).
Rapor `docs/`'a değil `data/raporlar/hafta-NN.md` olarak kaydedilir ve arayüzde gösterilir.

### 7.6 Maliyet disiplini
- Değerlendirme/sohbet/rapor: `claude-sonnet-4-6`.
- Quiz üretimi ve kısa yardımcı işler: `claude-haiku-4-5`.
- Her AI çağrısının token kullanımını logla (Spring AI usage metadata) ve
  `GET /api/kullanim` ile toplam çağrı/token sayısını göster.
- İstek başına max_tokens sınırı koy (değerlendirme 2000, sohbet 1500, rapor 3000).

---

## 8. REST API (tamamı)

```
GET  /api/program                     → hafta listesi + ilerleme yüzdeleri
GET  /api/hafta/{n}                   → hafta detayı (günler, görevler, statik quiz, durum)
POST /api/gorev/toggle                → {hafta, gun, gorevIndex}
POST /api/degerlendir                 → 7.2
POST /api/sohbet                      → 7.3
GET  /api/sohbet/{hafta}              → sohbet geçmişi
POST /api/quiz/statik-sonuc           → {hafta, puan, detay}
POST /api/quiz/uret                   → 7.4
GET  /api/rapor/{hafta}               → 7.5 (yoksa üretir, varsa cache'ten döner)
GET  /api/kullanim                    → token/çağrı istatistikleri
```

Hata sözleşmesi: tüm hatalar `{hata: "insan-okur mesaj", detay: "..."}` JSON'u ile,
doğru HTTP koduyla döner. API anahtarı yoksa uygulama açılışta NET bir mesajla uyarır
ama çökmez (AI özellikleri devre dışı, müfredat+ilerleme çalışır).

---

## 9. FRONTEND (static/)

Tek sayfa, üç sekme. Mevcut `mimar-programi.html`'in AÇIK TEMASINI ve genel görsel
dilini koru (açık zemin, koyu lacivert metin, mavi/turuncu vurgu) ama veriyi artık
API'den al:

1. **Program** — sol tarafta 12 haftalık katman haritası (alttan üste), sağda seçili
   haftanın günleri, görev checkbox'ları (API'ye yazar), statik quiz.
2. **Öğretmen** — üstte "değerlendirme" alanı: gün seç + metin yaz + "Değerlendir"
   → puan kartı ve maddeler. Altta Sokratik sohbet penceresi.
3. **Rapor** — hafta seç → haftalık rapor markdown render'ı + token kullanım özeti.

JS: fetch ile API çağrıları, framework yok. Markdown render için `marked` CDN'i
KULLANMA (offline çalışsın) — backend commonmark ile HTML'e çevirip döndürsün.

---

## 10. AGENTS.md (bu içerikle oluştur)

```markdown
# AGENTS.md — ai_engineering (AI Öğretmen)
> İçindekiler tablosudur, ansiklopedi değil. Detay: docs/ ve ARCHITECTURE.md.

## Komutlar
- Derle+test: ./mvnw verify
- Çalıştır: ANTHROPIC_API_KEY=... ./mvnw spring-boot:run  (http://127.0.0.1:8090)
- Sadece mimari testleri: ./mvnw -Dtest=MimariTestleri test

## Sınırlar
- ALWAYS: types → config → repo → service → ai → web katman yönüne uy (ArchUnit zorlar).
- ALWAYS: Claude çağrısı yalnız ai/ paketinden; prompt'lar prompts/ dizininden.
- ALWAYS: yeni davranışa test; API değişikliğinde bu dosyadaki komutları güncelle.
- ASK FIRST: yeni bağımlılık (önce docs/adr/'ye ADR).
- NEVER: API anahtarını koda/git'e koymak; 127.0.0.1 dışına bind; content/ altını
  kod değişikliğiyle aynı PR'da yeniden üretmek.

## Nerede ne var
- Mimari: ARCHITECTURE.md · Kararlar: docs/adr/ · Planlar: docs/exec-plans/
- Müfredat: content/ (frontmatter'lı md) · Prompt şablonları: prompts/
- Borç listesi: docs/tech-debt-tracker.md
```

`ln -s AGENTS.md CLAUDE.md` symlink'ini kur.

---

## 11. İLK ADR'LER (Faz 0'da docs/adr/ altına yaz)

- **ADR-0001:** Backend olarak Spring Boot + Spring AI seçimi (alternatifler: saf Anthropic
  Java SDK, Python FastAPI; gerekçe: öğrenme hedefi + ekosistem uyumu).
- **ADR-0002:** İçerik deposu olarak frontmatter'lı markdown (alternatif: DB'de içerik;
  gerekçe: ajan-okunabilirlik, elle düzenlenebilirlik, versiyonlama).
- **ADR-0003:** Model kademeleme politikası (sonnet ↔ haiku hangi işte, max_tokens sınırları).

---

## 12. FAZLAR VE KABUL KRİTERLERİ

### Faz 0 — İskelet ve içerik (yarım gün)
- [ ] Maven projesi + paket yapısı + application.yml (port 8090, 127.0.0.1)
- [ ] AGENTS.md (Bölüm 10) + CLAUDE.md symlink + ARCHITECTURE.md + docs/ iskeleti + 3 ADR
- [ ] `tools/` altındaki dönüştürücü script ile `mimar-programi.html` → `content/` üretildi
      (12 hafta × 5 gün + 12 quiz; spot kontrolle doğrulandı)
- [ ] `.gitignore` (data/, target/, .env) + ilk commit
- **Kabul:** `./mvnw verify` yeşil; `content/` ağacı tam; README'de 3 satır kurulum notu.

### Faz 1 — Müfredat + ilerleme (API anahtarsız çalışan çekirdek)
- [ ] ContentRepo (md + frontmatter parse) ve Mufredat modeli
- [ ] H2 şeması + gorev_ilerleme CRUD
- [ ] `GET /api/program`, `GET /api/hafta/{n}`, `POST /api/gorev/toggle`,
      `POST /api/quiz/statik-sonuc`
- [ ] Frontend "Program" sekmesi uçtan uca çalışıyor
- [ ] **MimariTestleri (ArchUnit):** katman kuralları + döngüsüzlük, CI'da bloklayıcı
- **Kabul:** API anahtarı OLMADAN uygulama tam çalışır (AI sekmeleri "anahtar gerekli" der);
  mimari testler yeşil; görev işaretleme kalıcı.

### Faz 2 — AI Öğretmen çekirdeği
- [ ] Spring AI yapılandırması + OgretmenAiClient + PromptFactory + prompts/ şablonları
- [ ] Değerlendirme uçtan uca (7.2): gönder → JSON değerlendirme → kart görünümü → DB kaydı
- [ ] Sokratik sohbet (7.3) geçmişiyle birlikte
- [ ] Token kullanım logu + `GET /api/kullanim`
- **Kabul:** Gerçek anahtar ile örnek bir özet gönderilir, yapılandırılmış değerlendirme
  döner; bozuk JSON senaryosu zarifçe ele alınır (test yaz); sohbet bağlamı hafta içeriğini içerir.

### Faz 3 — Quiz üretimi + rapor + cila
- [ ] AI quiz üretimi (7.4) ve arayüzü
- [ ] Haftalık rapor (7.5) + `data/raporlar/` cache
- [ ] Hata durumları, boş durumlar, yükleniyor göstergeleri
- [ ] docs/tech-debt-tracker.md güncel; kalan borçlar listelendi
- **Kabul:** 1. hafta baştan sona "yaşanabilir": günler okunur, görevler işaretlenir,
  özet değerlendirilir, sohbet edilir, ek quiz çözülür, rapor alınır.

---

## 13. ÇALIŞMA KURALLARI (Claude Code için)

1. Her fazın başında `docs/exec-plans/active/` altına kısa bir ExecPlan yaz;
   faz bitince `completed/`e taşı. ExecPlan tek başına işi yeniden başlatmaya yetmeli.
2. Küçük, tek amaçlı commit'ler; mesaj formatı: `[faz-N] özet`.
3. Her yeni davranışa test. Değerlendirme JSON parse'ı için birim test ZORUNLU.
4. Belirsizlikte varsayılanı seç, ADR'ye not düş, İLERLE — soru yığma.
5. Bir fazın kabul kriterlerini kendin doğrula (çalıştır, curl ile test et),
   sonra kısa bir doğrulama raporu yaz.
```