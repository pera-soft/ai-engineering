# ADR-0005: Anlam-eşleşmeli bağlam — SimpleVectorStore (bellek-içi) + OpenRouter gemini-embedding-001

## Durum (Status)
Accepted — 2026-07-09

## Bağlam (Context)
Kitap notları (`data/hafiza/kitaplar/*.md`, ~133 blok) şimdiye dek AI bağlamına yalnızca
**hafta-etiketi** eşleşmesiyle giriyordu (`KitapService.baglamIcin(int hafta)`): notun
`etiketler: N,N` satırı aktif haftayı içeriyorsa enjekte ediliyordu. Bu kaba bir filtre —
öğrencinin gerçekte sorduğu/çalıştığı konuyla ilgisiz notları getirebiliyor, ilgili ama
farklı haftaya etiketlenmiş notları kaçırıyor. İhtiyaç: notları hafta etiketi yerine
**anlam benzerliğiyle** (embedding/RAG) getirip AI'ya enjekte etmek.

Kısıtlar: uygulama yerel, tek kullanıcı, git dışı `data/` ağacı; anahtarsız da açılabilmeli
(mevcut `AiDevreDisi` deseni). Not sayısı düşük (~133) ve yavaş büyüyor.

## Karar (Decision)
- **Vektör deposu: Spring AI `SimpleVectorStore`** (bellek-içi, brute-force cosine, JSON'a
  save/load). Persist yolu `./data/hafiza/vektor.json` (git dışı). Tek yeni bağımlılık:
  `org.springframework.ai:spring-ai-vector-store` (sürüm Spring AI BOM'dan).
- **Embedding: OpenRouter `/v1/embeddings`**, model `google/gemini-embedding-001`. Mevcut
  `spring-ai-openai` içindeki `OpenAiEmbeddingModel` kullanılır — base-url sohbet istemcisiyle
  aynı (`https://openrouter.ai/api`, ADR-0004). Yeni embedding bağımlılığı yok.
- **Yeni arayüz `ai/AnlamArama`** (tek çıkış noktası, `OgretmenAiClient` gibi): `indexle`,
  `benzerleri(sorgu, k)`, `temizle`, `sayac`. Spring AI `VectorStore`/`EmbeddingModel`/`Document`
  yalnız `SpringAiAnlamArama` içinde yaşar; service `NotParcasi`/`BenzerNot` (types) görür.
- **Sorgu kaynağı: HİBRİT** — öğrencinin son sohbet mesajı varsa ona göre ara; yoksa
  (değerlendirme/rapor/ders başı) aktif günün/haftanın ders metnine göre ara.
- **Index-at-creation:** `notEkle` yeni bloğu oluşturulduğu an embed+add+persist eder
  (id = `slug#tarih#içerik-hash` → idempotent, tekrar eklenince üzerine yazılır). Açılışta
  (`@PostConstruct`) depo boşsa mevcut tüm bloklar toplu indekslenir.
- **Fallback (belt-and-suspenders):** depo boş VEYA anahtar yok VEYA embedding hatası →
  `AnlamArama.benzerleri` boş liste döner ve `KitapService` mevcut hafta-etiketi eşleşmesine
  (`baglamIcin(hafta)`) düşer. Anahtar yokken bean no-op'tur; uygulama anahtarsız açılır.

## Değerlendirilen alternatifler
- **pgvector / Postgres** — üretim RAG'in standardı, HNSW/IVFFlat ile milyonlarca vektörde
  ölçeklenir. Reddedildi: bu araç yerel/tek-kullanıcı, ~133 not; ayrı bir veritabanı süreci
  (Postgres) kurmak, işletmek ve `data/` yerine dış bağımlılık eklemek maliyeti faydayı aşıyor.
  DDIA 2e (B4) not eder: HNSW gibi ANN indeksleri milyonlarca vektör içindir; birkaç yüz
  vektörde flat (brute-force) cosine zaten anında ve **daha doğru** (yaklaşık değil, kesin).
- **Yerel ONNX / gömülü embedding modeli** (spring-ai-transformers, all-MiniLM vb.) — çevrimdışı
  çalışır, API maliyeti yok. Reddedildi: yeni ağır bağımlılık (ONNX runtime + model dosyası),
  Türkçe içerikte kalite belirsiz, ve embedding'i **sohbetle aynı sağlayıcıdan** (OpenRouter)
  almak tek anahtar/tek panel/tek kimlik ilkesini korur (ADR-0004 ile tutarlı).
- **Etiket eşleşmesini korumak (statüko)** — reddedildi: özelliğin amacı tam da bunu anlam
  benzerliğiyle değiştirmek. Yine de fallback olarak tutuluyor (kayıpsız geri dönüş).

## Sonuçlar (Consequences)
- (+) Değişiklik katman mimarisine uydu: Spring AI importları yalnız `ai/`'da; service ara
  tipleri (`NotParcasi`/`BenzerNot`) görür; ArchUnit yeşil.
- (+) Tek yeni artifact (`spring-ai-vector-store`), ayrı süreç/veritabanı yok; `data/` git dışı.
- (+) Anahtarsız açılış ve her hata yolunda hafta-etiketi fallback'i → özellik hiçbir koşulda
  uygulamayı bozmaz.
- (−) Açılışta depo boşsa ~133 blok için toplu embedding çağrısı (bir kerelik, sonra JSON'dan
  yüklenir). Canlı OpenRouter maliyeti; kişisel araç için kabul edilebilir.
- (−) Bellek-içi flat cosine yüz binlerce nota ölçeklenmez; not tabanı büyürse pgvector'a
  geçiş bu ADR'yi supersede eder (`AnlamArama` arayüzü sayesinde service değişmez).
