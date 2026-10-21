---
baslik: "Program 2026 Güncelleme Raporu — Kaynak Tazeliği ve Boşluk Analizi"
yazar: "3 paralel web-araştırma ajanı (Claude), Temmuz 2026"
tarih: 2026-07-04
url: ""
etiketler: 1,2,3,4,7,8,10,11,12
---

# Program 2026 Güncelleme Raporu

### Bu metin ne anlatıyor?
Bu bir ders metni değil, programın kendi **bakım kaydıdır**. 12 haftalık eğitimin
kaynakları belirli aralıklarla taranıp "hangi bilgi eskidi, hangi konu eksik kaldı, neyi
eklemeye karar verdik, neyi bilerek eklemedik" diye yazıldı. Konuları burada öğrenmezsin;
burada programın neden şu anki hâlinde olduğunu okursun.

Bu yüzden burada diyagram yok — anlatılan bir konu değil, tutulan bir defter var.

### Bu raporu nasıl okumalı — üç kural
1. **Tarih koyarak oku.** Her madde yazıldığı günün fotoğrafıdır. Model adları (GPT-5.5,
   Sonnet 5), sürüm numaraları ve ürün isimleri en hızlı eskiyen kısımdır; bir karar
   vermeden önce güncel hâlini doğrula.
2. **İddianın kaynağını sor.** Bir rakamı kim ölçtü? Ölçen tarafın o sonuçtan **ticari
   çıkarı** var mı? Bir güvenlik şirketinin "kodların %45'i açıklı" ölçümü ile bağımsız bir
   akademik çalışma aynı ağırlıkta değildir. Aşağıda satıcı kaynaklı rakamlara ⚠️ ile şerh
   düşüldü.
3. **"Eklemedik" maddeleri de karardır.** Raporun sonundaki kapanışlar başarısızlık değil;
   kanıt bulunamadığı için içerik üretilmemesidir. Bir konunun müfredata girmemesi de
   gerekçelendirilmesi gereken bir mimari karardır.

### Raporda geçen kısaltmalar
- **Ajan** (agent): Kendi başına adım adım iş yapan yapay zekâ programı.
- **Harness** (koşum takımı): Bir yapay zekâ modelinin etrafına kurulan araç, kural ve
  geri besleme düzeni. Model aynıyken bile harness sonucu değiştirir.
- **Eval** (değerlendirme): Bir yapay zekâ çıktısının kalitesini otomatik ölçen test.
- **Guardrail** (korkuluk): Modelin izin verilen sınırlar içinde kalmasını sağlayan denetim.
- **RCT** (Randomized Controlled Trial, randomize kontrollü deney): Katılımcıların rastgele
  iki gruba ayrıldığı, en güvenilir sayılan deney türü.
- **RAG** (Retrieval-Augmented Generation): Modele cevap verdirmeden önce ilgili belgeleri
  bulup bağlamına koyma yöntemi.
- **MCP** (Model Context Protocol): Yapay zekâ istemcilerinin dış araçlara/verilere bağlandığı
  açık protokol.
- **OWASP**: Uygulama güvenliği alanında en bilinen açık kaynaklı topluluk; "Top 10" listeleri
  yayımlar.
- **OTel** (OpenTelemetry): Yazılımın içini izlemek (log, metrik, iz) için açık standart.
- **SBOM** (Software Bill of Materials): Yazılımın içindeki tüm bileşenlerin listesi —
  "malzeme listesi".
- **CRA** (Cyber Resilience Act): AB'nin dijital ürün güvenliği yasası.
- **SLM** (Small Language Model): Küçük, çoğu zaman cihaz üstünde çalışan dil modeli.
- **PoC** (Proof of Concept): Denemelik prototip; üretimde kullanılan sistem değil.
- **GA** (General Availability): Ürünün deneme aşamasından çıkıp genel kullanıma açılması.
- **Allowlist** (izin listesi): Yalnızca listedekilere izin veren güvenlik kuralı.
- **Drift** (kayma): Sistemin zamanla tasarlanan hâlinden sessizce uzaklaşması.

12 haftalık programın kaynakları 4 Temmuz 2026'da ~45 web aramasıyla denetlendi.
**Genel hüküm: programın omurgası doğru ve öncü çıktı** (iSAQB'nin yeni SWARC4AI
modülü ve SFIA AI beceri çerçevesi programın yönünü bağımsız doğruluyor) — ama
kaynak sürümleri tazelenmeli ve beş boşluk kapatılmalı.

## Hafta 1 — Harness Mühendisliği

- OpenAI "Harness Engineering" (Şub 2026) hâlâ kurucu metin; ekosistem patladı.
  **LangChain'in ampirik kanıtı**: aynı model, farklı harness → Terminal Bench'te
  %52,8 → %66,5. "Harness engineering 2026'nın en etkili terimi."
  ⚠️ **Şerh:** Bu ölçümü, harness araçları satan bir şirket kendi yaptı ve tek bir
  kıyaslama testi (Terminal Bench) üzerinde yaptı. "Harness fark yaratır" yönü makul; ama
  **belirli yüzde** genellenemez ve "2026'nın en etkili terimi" bir ölçüm değil, bir
  pazarlama cümlesidir. Doğru kullanım: yönü öğret, rakamı "LangChain'in kendi ölçümü"
  diye atfet.
- **Böckeler'in memo'su Nisan 2026'da tam makaleye evrildi**
  (martinfowler.com/articles/harness-engineering.html): harness öğeleri
  **guides (ileri besleme) ve sensors (geri besleme)**, her biri computational
  veya inferential; kurum içi paylaşım için harness templates. Devam yazısı:
  "Maintainability sensors for coding agents". Ders bu taksonomiyle işlenmeli.
- Terimin kökeni: **Mitchell Hashimoto** (Şub 2026) — "Ajan bir hata yaptığında,
  o hatayı bir daha asla yapmayacağı şekilde çözüm mühendisliği yap."
- YENİ ALT MODÜL — **Context Engineering**: Karpathy'nin terimi (2025),
  Anthropic'in resmi rehberi (Eyl 2025) ile formalleşti; prompt engineering'in
  yerini büyük ölçüde aldı. Formül genişledi: Harness = Model + Context.
  Pratik teknikler: compaction, tool-result temizliği, memory tasarımı,
  RAG'e karşı just-in-time bağlam.

## Hafta 2 — Repo Bilgi Sistemi

- AGENTS.md kurumsallaştı: Linux Foundation / Agentic AI Foundation (AAIF,
  170+ üye) yönetiminde; 60.000+ proje. "Kısa AGENTS.md + docs/" pratiği
  hâlâ önerilen desen.
- **EN BÜYÜK EKLEME — Agent Skills / SKILL.md**: Anthropic'in açık standardı
  (Ara 2025, agentskills.io); Haziran 2026'da Codex, Copilot, Cursor, Gemini CLI
  dahil ~40 ürün destekliyor — çapraz-ajan standardı. İşbölümü:
  AGENTS.md = her oturumda yüklenen repo kuralları; SKILL.md = isteğe bağlı
  yüklenen yetenek paketi (progressive disclosure). Haftanın yeni çerçevesi:
  **repo bilgi sisteminin üç katmanı: AGENTS.md + docs/ + skills/**.
- ADR'ler ajan-farkındalıklı oldu: AGENTS.md'ye "mimari karardan önce
  docs/adr/ kontrol et; yeni karar için önce ADR taslağı" politikası yazmak
  2026 pratiği.

## Hafta 3 — Spec-Driven Akış

- Model adları eskidi: Codex'te güncel öneri **GPT-5.5** (karmaşık işler) +
  5.4-mini (alt-ajanlar). Codex Remote GA, domain allowlist'li internet erişimi,
  multi-agent delegation geldi.
- Claude Code: plan modu duruyor; yeniler: **Ultraplan** (planı bulutta taslakla,
  uzaktan çalıştır) ve Auto mode; Sonnet 5 (1M bağlam) varsayılan.
  ⚠️ **Şerh — model ve sürüm adları:** Yukarıdaki iki madde raporun **en hızlı eskiyen**
  kısmıdır ve tamamı satıcı duyurularına dayanır. Model adlarını ezberleme; öğrenilecek şey
  ürün ismi değil, **desendir**: plan modu, uzaktan çalıştırma, alt-ajanlara devretme, geniş
  bağlam. Bir ada göre karar vereceksen sağlayıcının güncel belgesinden doğrula.
- Spec-driven niş değil standart: GitHub Spec Kit 90K+ yıldız (v0.8.7, May 2026);
  Amazon **Kiro** uluslararası lansman yaptı ve Amazon Q Developer'ın yerini aldı.
- Çerçeve değişimi: R→P→E→R→S akışı geçerli; ama spec-driven artık
  **context engineering şemsiyesinin alt kümesi**. Yeni pratik: kod incelemesi
  gibi **spec review / context audit**.

## Hafta 4 — Guardrails

- Araçlar canlı: import-linter v2.12 (Haz 2026; yeni acyclic_siblings/protected
  kontratları), ArchUnit 1.4.2.
- **Veracode Spring 2026 güncellemesi**: rakamlar kımıldamadı — AI kodun %45'i
  hâlâ OWASP açıklı; Java en riskli dil (%72 başarısızlık); XSS yalnız %15,
  log injection %13 geçiyor. "Modeller iddialara rağmen güvenlikte gelişmiyor."
  ⚠️ **Şerh:** Veracode bir **kod güvenliği tarama ürünü satan şirkettir**; "üretilen kodun
  yarısı açıklı" bulgusu kendi ürününe talep yaratır. Ayrıca yüzdeler, şirketin kendi
  seçtiği görev kümesi ve kendi tarayıcısının tanımına göre hesaplanır — başka bir tarayıcı
  başka bir sayı verir. Dersin dayanağı **yön** olmalı ("üretilen kod güvenli varsayılamaz,
  kapı koy"), yüzde değil. Kaynağı derste "Veracode'un ölçümü" diye adlandır.
- **Thoughtworks Radar Vol 34 (Nis 2026) bu haftanın ders kitabı**:
  "architecture drift reduction with LLMs" (ArchUnit/Spring Modulith gibi
  deterministik araçlar + LLM değerlendirmesi) ve "feedback sensors for coding
  agents" (derleyici/linter/test → ajan döngüsüne oto-düzeltme sensörü).
  Temalar: izin-açgözlü ajanları güvenceye alma, ajanları tasmada tutma,
  **AI-kaynaklı bilişsel borç (cognitive debt)**.
- KRİTİK BOŞLUK — **Ajan güvenliği**: OWASP "Top 10 for Agentic Applications
  2026" (9 Ara 2025): Agent Goal Hijack, Tool Misuse, Identity & Privilege
  Abuse, **Agentic Supply Chain (MCP sunucuları da tedarik zinciridir!)**,
  Memory Poisoning, Rogue Agents. Mevcut H4 "AI'ın ürettiği kodun güvenliği"ne
  bakıyor; "AI sisteminin KENDİSİNİN güvenliği" ayrı tehdit modeli. Savunma:
  least-privilege araç erişimi, sandbox yürütme, talimat hiyerarşisi, çıktı
  şema doğrulaması.
- EKLEME — **SBOM/CRA**: AB Cyber Resilience Act raporlaması 11 Eylül 2026'da
  başlıyor (24 saatte ENISA bildirimi; ceza 15M€/%2,5 ciro). CycloneDX ile
  Maven SBOM üretimi temel beceri.

## Hafta 7-8 — AI Engineering (kitap notlarına ek)

- EKLEME — **FinOps/tokenomics**: State of FinOps 2026 — kuruluşların %98'i
  AI harcaması yönetiyor (2024'te %31'di); "AI token ekonomisi" adlandırılmış
  disiplin. Model seçimi + caching + prompt optimizasyonu = %30-40 tasarruf.
  Maliyet, FoSA anlamında birinci sınıf mimari karakteristik.
  ⚠️ **Şerh:** "%98" bir **anket** sonucudur — ankete katılanlar zaten maliyet yönetimiyle
  ilgilenen kişilerdir (öz-seçilim). "%30-40 tasarruf" ise bir **aralık vaadi**dir; hangi
  yükte, hangi modelde, hangi başlangıç noktasından ölçüldüğü belirtilmezse kanıt değil
  beklentidir. Doğru öğretim: tasarruf kalemlerini (model seçimi, önbellekleme, istem
  kısaltma) **mekanizma** olarak öğret, tasarruf oranını **kendi uygulamanda ölç**.
- EKLEME — **AI Observability**: OpenTelemetry GenAI semantic conventions
  fiili standart (LLM çağrı span'leri, ajan span'leri, MCP araç izleme, token
  metrikleri) — ama Mart 2026'da hâlâ "experimental" statüde, taşınabilir.
  Evals çevrimdışı kaliteyi ölçer; üretim izleme (tracing, maliyet atfı, drift)
  bunun tamamlayıcısı.
- Eval mühendisliği ayrı meslek oldu (OpenAI "Applied Evals", Microsoft
  "Evaluations Engineering" kadroları) — H8 dersi "eval pipeline'ı + CI
  regresyon kapısı" pratiğine yükseltilmeli.

## Hafta 10 — The Hard Parts

- 2. baskı YOK; saga/dağıtık transaction/data ownership içeriği hâlâ kanonik.
- **2026 okuma notu**: mikroservis konsolidasyon konsensüsü kitabın granularity
  bölümlerini yeniden çerçeveliyor — varsayılan cevap artık "böl" değil
  **"bölme, gerekirse çıkar"**; integrator tarafı ağırlık kazandı.
  [DÜZELTME Tem 2026: önceki sürümde geçen "maliyet 3,75-6x" rakamı üçüncü
  tarama turunda KAYNAKSIZ/UYDURMA çıktı (Medium'daki hayali örnekten türemiş;
  Villamizar/IEEE 2016 tersini bulmuştu) — trend gerçek, o sayı değil.
  Sidecar-mesh düşüşü %50(2023)→%42(2024, CNCF); %18→%8 rakamı da teyitsiz.]
- Yazarların **"Architecture as Code"** kitabı (Eki 2026) ikame değil devam:
  Hard Parts "hangi kararı vereceğim", AaC "kararı nasıl kod olarak zorlarım".

## Hafta 11 — DDIA

- **DDIA 2e YAYIMLANDI: Mart 2026** (Kleppmann + Riccomini, O'Reilly, 670 sf) —
  programdaki soru işareti kalksın. Yenilikler: cloud-native/object storage
  varsayılan; AI iş yüklerinin depolama/indekslemeye etkisi; genişletilmiş
  stream processing; 1e'de olmayan iki bölüm: **"Trade-offs in Data Systems
  Architecture"** ve **"Defining Nonfunctional Requirements"** (FoSA
  haftalarıyla doğal köprü). Önerilen çekirdek okuma: bu iki bölüm +
  "The Trouble with Distributed Systems". Alımlanma olumlu: "hâlâ incil."
  ⚠️ **Şerh:** "Hâlâ incil" bir okur yorumudur, bir ölçüm değil. Kitabın iyi olması, her
  bölümünün senin bağlamında geçerli olduğu anlamına gelmez; özellikle somut ürün ve
  sürüm örnekleri her baskıda eskir.

## Hafta 12 — Liderlik ve Capstone

- Kaynağın tam kimliği: **"How AI assistance impacts the formation of coding
  skills"** (Anthropic, 29 Oca 2026, RCT, 52 junior): AI'lı grup sınavda %17
  düşük (en büyük açık: debugging). KRİTİK NÜANS: kavramsal soru soranlar
  %65+, kodu tümüyle delege edenler %40 altı — **sorun AI değil, kullanım
  biçimi**. Devam raporu: Economic Index "Learning curves" (24 Mar 2026):
  6+ ay deneyimli kullanıcılar ~%10 daha başarılı, daha işbirlikçi.
  ⚠️ **Şerh:** Her iki çalışmayı da **model satan şirketin kendisi** (Anthropic) yayımladı.
  Yöntem güçlü (randomize kontrollü deney) ama örneklem küçük (52 kişi), tek bağlam ve
  birinci taraf. "AI kullanmak öğrenmeyi %17 düşürür" diye genelleme **çıkarılamaz**;
  çıkarılabilen şey daha dar ve zaten programın tezidir: *kavramsal soru soranlar ile işi
  tümüyle delege edenler farklı sonuç alıyor.* Sayıyı değil, ayrımı öğret.
- **Bu bulgu programın yöntemini doğruluyor**: "Değerlendir'e teslim et,
  AI'dan çözüm isteme" akışı ölçülmüş doğru taraf. Kata pratiğinin 2026 hali:
  önce kendin çöz, sonra LLM'i **karşı-mimar/jüri** olarak kullan.
- nealford.com/katas yaşıyor; O'Reilly "AI Kata" formatı ekledi. Yeni liderlik
  okumaları: Addy Osmani "Leading Effective Engineering Teams in the Age of
  GenAI"; iSAQB SWARC4AI müfredatı öz-denetim listesi olarak kullanılabilir.
- EKLEME — **Mevzuat operasyonelleştirme**: AI Act Madde 50 şeffaflık
  yükümlülükleri **2 Ağustos 2026'da yürürlükte** (Omnibus bunu ERTELEMEDİ):
  chatbot'un AI olduğunun bildirimi + sentetik içeriğin makine-okunur
  işaretlenmesi. KVKK Üretken YZ Rehberi (24 Kas 2025, 64 sayfa, 15 soru) —
  kendi uygulamanın data/hafiza/ öğrenci verisine uygula.

## Öncelikli "Programa Ekle" Listesi

1. **Ajan güvenliği → H4**: OWASP Agentic Top 10 + least-privilege + sandbox.
2. **AI observability → H8**: OTel GenAI ile LLM tracing (experimental notuyla).
3. **Context engineering → H1**: Anthropic rehberi eksenli alt modül.
4. **Mevzuat → H12/H4**: AI Act Md. 50 (2 Ağu 2026!) + KVKK rehberi.
5. **FinOps/tokenomics → H7-8**: kendi OpenRouter uygulamasında maliyet ölçümü.

Ayrıca ayrı hafta değil lab olarak: MCP sunucu yazma (Spring AI @McpTool ile
kendi uygulamanın bir portunu MCP adapter olarak dışa açmak — H9) ve Spring
Modulith sınır lab'ı (H9).

## KAPATILDI (2026-07-15)

Yukarıdaki boşlukların büyük kısmı yeni kaynak/atölye kategorisiyle kapatıldı
(yeni hafta gerektirmeden; ilgili haftalara etiketle bağlandı):

1. **Ajan/LLM güvenliği → H4** ✓ — `kaynaklar/llm-uygulama-guvenligi.md`
   (etiket 4,7,8): prompt enjeksiyonu (doğrudan/dolaylı), OWASP LLM01 + Agentic
   Top 10, savunma derinliği, red-teaming. 3 diyagram + 10 öz-sınav.
2. **Evals + LLM observability → H8** ✓ — `kaynaklar/evals-llm-observability.md`
   (etiket 8): Değerlendirme-Güdümlü Geliştirme (EDD), CI kapısı, OTel GenAI,
   drift izleme, cost-per-successful-output. 3 diyagram + 10 öz-sınav.
3. **Context engineering** ✓ — Prompt Atölyesi'ne 8. kategori
   `07-context-muhendisligi.md`: bağlam bütçesi, bellek/durum, retrieval
   orkestrasyonu, araç tanımı, gürültü atma, bağlam hijyeni (7 alıştırma).
4. **Mevzuat/yönetişim → H8/H11** ✓ — `kaynaklar/ai-yonetisim-uyum.md`
   (etiket 8,11): KVKK/GDPR, EU AI Act (2 Ağu 2026), PII maskeleme, RAG'de
   silme/embedding tuzağı, veri soyağacı. 3 diyagram + 10 öz-sınav.
5. **FinOps/tokenomics** ~ kısmen — evals ve yönetişim kaynaklarında
   "başarılı çıktı başına maliyet" çerçevesi olarak işlendi (ayrı kaynak değil).
   → **2026-07-17'de tam kapatıldı**: `kaynaklar/finops-ai-tokenomics.md` (etiket 7,8).

**MCP sunucu yazma lab'ı → KAPANDI (2026-07-16).** Lab'ın ötesine geçildi: uygulamanın MCP
adapter'ı yazıldı (8 araç / 46 kaynak / 5 prompt), OAuth 2.1 arkasında `ai.velzon.tr/mcp`
adresinde **canlıya alındı** ve token modeli üretimde ölçüldü (sunucu süreçinde LLM anahtarı
yok, çağrı sıfır). Bkz. ADR-0007 Faz 2b + `kaynaklar/mcp-adapter.md`.

Açık kalan (lab): **Spring Modulith sınır lab'ı**.

## İKİNCİ TUR — kanıtlı boşluk analizi (2026-07-17)

Yeni bir araştırma turu (26 kaynak → 124 iddia → 25'i çekişmeli doğrulamadan geçirildi;
18 onaylandı, 7 çürütüldü) müfredatın en büyük boşluğunu **teknoloji değil, ÖLÇÜM** olarak
buldu. Kapatıldı:

6. **AI verimlilik ölçümü / kanıt okuma** ✓ — `kaynaklar/ai-verimlilik-olcumu.md`
   (etiket 4,7,8,12): algı–gerçeklik uçurumu (METR RCT), üç ayrı alet (RCT / anket / telemetri),
   DORA "Great Amplifier" tezi — *AI bir ekibi düzeltmez, mevcut durumu büyütür* — ki bu
   **Hafta 4'ün (guardrail) ampirik gerekçesidir**. Benimseme–güven makası (Stack Overflow).
7. **A2A sınırı** ✓ — `kaynaklar/mcp-adapter.md`'ye bölüm: A2A, MCP'nin rakibi değil **üstü**
   (A2A kurumlar arası ajan koordinasyonu, MCP iç araç erişimi; ikisi de Linux Foundation'da).
   Ayrı hafta AÇILMADI — gerekçe: v1.0 kararlı ama üretim kullanımı çoğunlukla hâlâ PoC.

**EKLENMEDİ (bilinçli):** *"Vibe coding"* bir teknik olarak — Stack Overflow 2025'te bu soruyu
yanıtlayan 26.564 kişinin **%77,5'i profesyonel işinde kullanmıyor** (%17,2 kullanıyor); terimin
mucidi bile Şubat 2026'da onu "agentic engineering" diye yeniden adlandırdı. Hafta 1 (harness) +
Hafta 3 (spec-driven) bu alanı zaten **doğru kavramlarla** kaplıyor; başlık eklemek terminolojik
gerileme olurdu. Yeri yalnızca anti-pattern alıştırması.

## ÜÇÜNCÜ TUR — kalan 5 konu karara bağlandı (2026-07-17)

İkinci turda kanıtsız kalan beş konu ayrı bir araştırma turuyla hedeflendi (25 iddia doğrulandı:
14 onay / 10 çürütme / 1 belirsiz). Sonuç — **üçü girdi, ikisi kalıcı olarak kapandı:**

8. **Ajan sandbox izolasyonu** ✓ — `kaynaklar/ajan-sandbox-izolasyonu.md` (etiket 4,9).
   Tekrar değil, **ayrı katman**: `llm-uygulama-guvenligi` *önlemeyi*, bu *sınırlamayı* (containment)
   anlatır — enjeksiyon başarılı olduğunda blast radius'u daraltmak. Kanıt güçlü: **ICML 2026 Oral**
   (Oxford + UK AI Security Institute) sınır modellerin yanlış-yapılandırılmış konteynerden kaçış
   oranını ölçtü (zorluk 1-2'de 1.00'e varan; zorluk 4-5'te **tüm modeller 0.00**; sertleştirilmiş
   sandbox hiç aşılamadı; **GPT-5.2 geriledi** → "ölçekle risk monoton artar" desteklenmiyor) +
   Google'ın kendi bounty ödeyip yamaladığı **Antigravity RCE** vakası. Asıl ders: *her yürütme
   yolunu aracılık etmeyen kontrol düzlemi, kontrol düzlemi değildir* — projenin ArchUnit kurgusuyla
   birebir analog.
9. **SLM ekseni** ✓ (sınırlı) — `finops-ai-tokenomics.md` §5.1'e eklendi. Üretim kanıtı **Apple**
   (~3B cihaz-üstü, iki nesil). Ama **NVIDIA'nın "SLM'ler geleceğidir" tezi kanıt olarak
   ÖĞRETİLMİYOR**: kendi ilan ettiği bir *"value statement"*, dolaşan %60/%40/%70 rakamları
   **ölçülmemiş** masa başı tahmini, satıcının kendi savunma sayfasında bile isimli üretim vakası yok.
   Bölümün asıl dersi bu ayrım: **satıcı tezi ≠ bağımsız üretim kanıtı.**
10. **Talimat şişmesi → bağlam çürümesi** ✓ — `harness-engineering-openai.md` §4.1'e eklendi.
    Araç (Spec Kit) **eklenmedi**: Radar Vol.34'te **Assess** halkasında ("denemene bile gerek yok"),
    ekipler *deniyor*, üretimde kullanmıyor; üstelik araç eskiyor (v0.10.0 bayrak ailesini kaldırdı,
    öğreticiler kırıldı). Araç yerine **araç-agnostik arıza modeli** öğretiliyor. Canlı vaka olarak
    projenin kendi `AGENTS.md`'sinin bir günde 29→85 satıra çıkması kullanıldı.

### KALICI OLARAK KAPANDI — iki turda kanıt üretilemedi
- **AI ile legacy modernizasyon:** iki bağımsız turda doğrulamayı geçen **tek bir iddia yok**.
  Satıcı ürünleri (IBM watsonx Code Assistant for Z, Amazon Q Code Transformation) *mevcut* olabilir;
  ama bağımsız vaka ya da ölçülmüş sonuç toplanamadı. **Satıcı ürün sayfasının varlığı, kurumsal
  pratik kanıtı değildir.** Bir konuyu 12 haftalık müfredata sokmak için "cevapsız" yeterli değil.
- **Spring Modulith sınır lab'ı:** sürüm/benimseme/"ArchUnit'ten farkı" — hiçbiri doğrulanamadı.
  Ayrıca gerekçe kanıttan bağımsız olarak da geçerli: proje **zaten** ArchUnit ile katman sınırlarını
  derleme zamanında zorluyor (`MimariTestleri`, `./mvnw verify` yeşil) → *"sınır zorlama mimari bir
  karardır ve otomatik test edilebilir"* hedefine **ulaşılmış**. Hafta 9'da hexagonal zaten canlı
  yapıldı; dikey sınır kavramı başka yoldan verilmiş. Kanıtsız yeni bağımlılık, projenin **ASK FIRST**
  kuralına da aykırı. **Kavramı ArchUnit'le öğretmek yeter.**
- **Durable execution / ajan determinizmi:** kanıt yok → sandbox kaynağına dahil **edilmedi**
  (o kaynağın kararı yalnızca izolasyon boyutunu kapsar).

> Bu iki kapanış bir başarısızlık değil, **yöntemin çalıştığının kanıtı**: iki tur araştırma sonunda
> "eklemiyoruz" demek, zorlayarak doğrulanmamış içerik üretmekten iyidir.

## DÖRDÜNCÜ TUR — dünya programlarıyla kıyas (benchmark, 2026-07-19)

Önceki turlar **konu** soruyordu; bu tur **biçim** sordu: iSAQB CPSA, SEI/CMU (ATAM/QAW),
O'Reilly/Neal Ford kataları, üniversite dersleri, AI mühendisliği kursları, staff-eng.
22 iddia doğrulandı (21'i 3-0). Bulgu: eksik olan konu değil, **ölçme ve pratik biçimi** —
dünya, mimarı *"kurduğunu savunabiliyor ve başkasınınkini sorgulayabiliyor mu"* ile ölçüyor.

11. **Mimari değerlendirme + kata serisi + savunma** ✓ — `kaynaklar/mimari-degerlendirme-kata.md`
    (etiket 5,6,10,12). Üç doğrulanmış boşluk tek kaynakta:
    - **ATAM/QAW yoktu:** iSAQB'de değerlendirme ayrı modül (ARCEVAL); SEI'nin 9 adımlı ATAM'ı
      ölçülebilir artefakt üretir (fayda ağacı, riskler/risk-olmayanlar, duyarlılık, trade-off
      noktaları); QAW aynı işi mimari YOKKEN yapar. Biz kurmayı öğretiyorduk, sorgulamayı değil.
    - **Kata tek atıştı:** O'Reilly katası çok turlu + jüri geri beslemeli + izleyici oylu bir
      SERİ (2020-2025'te 9+ döngü). Çözüm: 2 haftada bir mini-kata bankası + capstone.
    - **Savunma (design defense) yoktu:** CPSA-A sınavı ~40 saatlik yazılı vaka + iki bağımsız
      denetçi + telefonda **sözlü savunma**; kata'nın 2. fazı "düşüncendeki delikleri arayan"
      çapraz sorgu. Solo uyarlama: AI öğretmen müşteri (Moderatör) ve sorgucu rolü oynar.
- **DÜRÜST SINIRLAR (metne gömüldü):** (a) Kata'nın çekirdeği yabancılarla **takım tartışması**
  — solo formata **UYARLANAMAZ**; AI sorgusu üç insanın üç sezgisinin yerini tutmaz. (b) "AI,
  ATAM'ı kısmen otomatikleştirir" genellemesi doğrulamada **çürütüldü** (0-3); ayakta kalan yalnız
  dar bir ilk-çalışma bulgusu (LLM, öğrenci senaryolarındaki riskleri çoğu durumda daha isabetli
  buldu) → AI "sparring partneri", jüri değil. (c) **Aralıklı tekrar / geri getirme** iddiaları
  benchmark turunda oturum limitine takılmıştı; **sonradan ayrıca doğrulandı** (2026-07-19):
  Dunlosky'nin dönüm-noktası derlemesinde 10 çalışma tekniğinden yalnız İKİSİ "yüksek fayda" —
  geri getirme pratiği (retrieval practice / testing effect) + aralıklı tekrar; Roediger & Karpicke:
  1 hafta sonra test %61 vs yeniden-okuma %40; etki, aralık uzadıkça BÜYÜR. Mekanik eklendi:
  H6 Gün 5'e program-ortası karma tekrar quizi (hafta 1-5), H12 Gün 5'e karma final tekrarı
  (hafta 1-11) — mevcut AI quiz üretimi yeterli, kod gerekmedi.
- **EKLENMEDİ:** ayrı "dokümantasyon" kaynağı (iSAQB ADOC ayrı modül tutuyor ama bizde ADR+C4
  zaten var; eksik olan doküman *yazmak* değil, dokümanın **notlandırılan teslimat** olmasıydı —
  capstone protokolüne girdi). Sertifika hedefi (CPSA-F/A) yalnız bir dipnot — program kişisel.

*Teyitsiz işaretler: CNCF %42 konsolidasyon rakamı (ikincil kaynak), OpenAI
prompt-injection rehberi birincil URL'si, %280 ilan büyümesi.*
*Tek-kaynak/satıcı uyarısı: GitClear'ın "blok tekrarı %81 arttı" ölçümü doğrulamayı geçti ama
tek kaynak ve satıcı çıkarı var — "GitClear'ın ölçümü" diye atfedilmeli, "sektör gerçeği" değil.
Aynı raporun diğer metrikleri (moved code, function connectivity, legacy update) çürütüldü.*

## Şerh özeti — bu rapordaki rakamların kaynak sınıfı

Derste bir rakam kullanacaksan önce buraya bak. Sınıflar:

| İddia | Kaynak sınıfı | Nasıl kullanılmalı |
|---|---|---|
| Harness fark yaratır (%52,8 → %66,5) | Satıcı (LangChain), tek kıyaslama | Yönü öğret, rakamı atıfla ver |
| AI kodunun %45'i açıklı | Satıcı (Veracode, tarama ürünü satıyor) | "Kod güvenli varsayılamaz" dersi; yüzdeye yaslanma |
| FinOps %98 / %30-40 tasarruf | Anket + vaat aralığı | Mekanizmayı öğret, oranı kendi uygulamanda ölç |
| Öğrenme üzerine RCT (%17 düşüş) | Birinci taraf (Anthropic), n=52 | Sayıyı değil, "kullanım biçimi" ayrımını öğret |
| NVIDIA "SLM'ler geleceğidir" | Satıcı değer beyanı, ölçüm yok | Öğretilmiyor; yalnızca *satıcı tezi ≠ kanıt* örneği |
| GitClear "blok tekrarı %81" | Tek kaynak + satıcı çıkarı | "GitClear'ın ölçümü" diye atfet |
| ATAM'ı LLM değerlendirir | Tek ders, tek model, ilk çalışma | Genelleme yok; "idman eşi" düzeyinde |
| Model/sürüm adları (GPT-5.5, Sonnet 5…) | Satıcı duyurusu, hızla eskir | Deseni öğret, adı kullanmadan önce doğrula |
| ATAM/QAW adımları, ADR, C4 | Kurumsal/akademik yöntem belgesi | Doğrudan öğretilebilir |
| AI Act Md. 50, CRA tarihleri | Mevzuat metni | Doğrudan öğretilebilir; tarihi teyit et |

**Kural:** Satıcının kendi ürününü doğrulayan ölçümü, bağımsız kanıt değildir. Bu rapor bunu
maddelerinde birkaç kez uyguladı ("maliyet 3,75-6x" rakamının uydurma çıkması, NVIDIA SLM
tezinin öğretilmemesi, legacy modernizasyon konusunun kanıtsız diye kapanması) — aynı süzgeci
okurken sen de uygula.
