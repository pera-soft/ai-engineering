# ExecPlan: Faz 0 + Faz 1 — İskelet ve API-anahtarsız çekirdek

> Yaşayan belge. Yalnızca bu belgeden işi yeniden başlatmak mümkün olmalı.
> Kaynak spec: kökteki CLAUDE_CODE_BRIEF.md (§3 mimari, §5 içerik formatı, §6 şema, §8 API, §12 fazlar).

## Neden önemli (kullanıcı bakışı)
Kullanıcı 12 haftalık mimarlık programını tarayıcıdan takip edebilecek: günleri okur,
görevleri işaretler, statik quiz çözer — API anahtarı olmadan. AI özellikleri (Faz 2)
bu çekirdeğin üstüne gelir.

## Durum
- [x] Faz 0.1: Maven iskeleti (pom: Boot 3.5.16 / Java 17 / Spring AI BOM 1.1.8; wrapper; paketler; application.yml; .gitignore)
- [x] Faz 0.2: AGENTS.md + CLAUDE.md symlink + ARCHITECTURE.md + ADR 0001-0003 + docs/ + README
- [x] Faz 0.3: tools/html-to-content.mjs → content/ üretimi (73 md; spot kontrol)
- [x] Faz 0.4: git init + `[faz-0]` commit; kabul: `./mvnw verify` yeşil
- [x] Faz 1.1: types/ record'ları (Hafta, Gun, QuizSorusu, ...)
- [x] Faz 1.2: ContentRepo (frontmatter+quiz+görev parser, TDD) + schema.sql + IlerlemeRepo/QuizSonucRepo
- [x] Faz 1.3: MufredatService + IlerlemeService (toggle idempotent upsert)
- [x] Faz 1.4: REST — GET /api/program, GET /api/hafta/{n}, POST /api/gorev/toggle, POST /api/quiz/statik-sonuc; {hata,detay} sözleşmesi
- [x] Faz 1.5: Frontend Program sekmesi + HER GÜNÜN YANINDA sohbet paneli (Faz 2 placeholder)
- [x] Faz 1.6: MimariTestleri (ArchUnit: katmanlar + döngüsüzlük + Claude-yalnız-ai-paketinden)
- [x] Faz 1.7: Uçtan uca doğrulama (curl + restart kalıcılık + tarayıcı) + `[faz-1]` commit + doğrulama raporu

## Önemli kararlar / sapmalar
- Starter adı: `spring-ai-starter-model-anthropic` (brief'teki ad eskiydi).
- Java 17 (kullanıcı kararı; ADR-0001).
- Faz 1'de `spring.ai.model.chat: none` — anahtarsız açılış garantisi; Faz 2'de `anthropic`.
- Kullanıcı eklemesi: sohbet paneli ayrı sekme DEĞİL, her görev görünümünün yanında kalıcı panel.
  Faz 2'de AI, öğrenciyi data/hafiza/ markdown ağacına kaydedecek (OGRENCI.md + hafta-NN/notlar.md).
- Kullanıcı eklemesi (Faz 2/3): Kitap Bilgi Tabanı — kullanıcı okuduğu kitaplardan not verir
  (content/kitaplar/<slug>/ veya POST /api/kitap-notu); AI notu işleyip hafta/kavram etiketleriyle
  data/hafiza/kitaplar/<slug>.md'ye yazar; sohbet/değerlendirme prompt'larına etiket-eşleşen
  kitap notları enjekte edilir (sistem kendini kullanıcının okuduklarıyla eğitir).
- Kullanıcı eklemesi (Faz 2/3): Kitap sayfası GÖRÜNTÜLERİ de yüklenebilecek — upload sayfası,
  Claude vision ile metin çıkarma → aynı kitap-notu işleme hattı (POST /api/kitap-notu/goruntu).

## Doğrulama adımları
1. `./mvnw verify` — birim + ArchUnit yeşil
2. `node tools/html-to-content.mjs && find content -name '*.md' | wc -l` → 73
3. Uygulama başlat → curl 4 endpoint; toggle → restart → kalıcılık
4. Tarayıcıda Program sekmesi + sohbet paneli placeholder

## Kabul kriterleri
- Faz 0: `./mvnw verify` yeşil; content/ tam; README 3 satır kurulum.
- Faz 1: API anahtarı OLMADAN tam çalışır; mimari testler yeşil; görev işaretleme kalıcı.
