---
baslik: "Ajan Mimarisi Desenleri — İş Akışı mı Ajan mı, Döngü, Topoloji ve Çoklu Ajan Hataları"
yazar: "AI Öğretmen çalışma metni — AI Engineering from Scratch (Rohit Ghumare, MIT) dersleri: 14-01, 14-05, 14-06, 14-12, 14-13, 14-26, 14-28, 14-29, 14-30 (+ 14-15, 14-16, 14-20, 14-34'ten birer kural); 16-01, 16-04, 16-05, 16-06, 16-08, 16-09, 16-13, 16-15, 16-16, 16-23 + bu deponun kendi vakası"
tarih: "2026-10-25"
url: "https://github.com/rohitg00/ai-engineering-from-scratch"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 7,8,10
---

### Bu metin ne anlatıyor?
Bir LLM özelliği tasarlarken ilk mimari soru "hangi framework?" değildir. İlk soru şudur:
**akışın grafiğini kim sahipleniyor?** Grafiği kod sahipleniyorsa yaptığın şey bir **iş akışıdır
(workflow)**. Model sahipleniyorsa bir **ajandır (agent)**. Metnin karar kuralları şunlar:

1. **En basitle başla.** Önce tek çağrı, sonra iş akışı desenleri, en son ajan. Beş iş akışı deseni
   var: zincirleme (prompt chaining), yönlendirme (routing), paralelleştirme (parallelization),
   orkestratör–işçi (orchestrator-workers), değerlendirici–iyileştirici (evaluator-optimizer). Üç
   durumda iş akışı kazanır: adımları önceden sayabiliyorsan, maliyetin sınırlı kalması gerekiyorsa
   ya da denetçi grafiği okumak istiyorsa. Ajan ancak bir sonraki adım bir öncekinin sonucuna
   bağlıysa hak edilir.
2. **Ajan döngüsünün tek bir durma koşulu yoktur.** Model "bitti" der ya da araç çağırmayı bırakır;
   adım bütçesi, token/süre bütçesi biter; bir korkuluk (guardrail) devreye girer. Bunların her biri
   ayrı bir durma koşuludur. Araç hatası döngüyü çökertmez; modele **gözlem** olarak geri döner.
   Her araç çağrısı yürütülmeden önce şemaya göre doğrulanır.
3. **Yan etkiler (yazma, ödeme, e-posta) deterministik kod adımlarında yaşar**, ajanın serbest araç
   döngüsünde değil.
4. **Ajanın "başardım" demesine değil, son duruma bak.** Kayıt gerçekten oluştu mu? Ajanı
   değerlendirirken de transkripte (konuşma dökümüne) değil son duruma bakılır.
5. **Tekli ajandan çoklu ajana** ancak tek bağlam ya da tek rol tıkandığında geçilir. Sıra şöyledir:
   tekli ajan + iş akışı → supervisor (2–4 uzman) → sürü (swarm) → hiyerarşi. Her devirde (handoff)
   bir hop sayacı tutulur.
6. Çoklu ajan hatalarının en büyük payı **tanım (spec)** eksiğinden gelir (MAST taksonomisi).
   **Eleştirmen (LLM) ≠ doğrulayıcı (kod).** **Aynı modelin N kopyası bağımsız oy değildir.**

Bu deponun kendisi bir vaka. **AI Öğretmen bir ajan değil, bir iş akışıdır**; kanıtı §8'de.

## Neden bu özet
Hafta 7–8 metinleri ([`hafta07-ai-engineering-i.md`](hafta07-ai-engineering-i.md),
[`hafta08-ai-engineering-ii.md`](hafta08-ai-engineering-ii.md)) model, prompt, RAG ve değerlendirme
anlatıyor; **ajan mimarisini hiç anlatmıyor.** Programdaki ajan içeriği ajan *kullanmakla* ilgili:
Hafta 1–3 ve [`harness-mekanigi.md`](harness-mekanigi.md). Bir ürünün *içine* ajan koymanın desenleri
şimdiye dek yalnız mülakat bankasında dağınık duruyordu
(`content/mulakat/06-uzmanlik-guncel/184-agentic-sistemler.md`). Hafta 3, Gün 5 çok-ajanlı desenleri
ve "ne zaman kullanmamalı" sorusunu kodlama ajanı için verdi; bu metin aynı soruyu **ürün mimarı**
gözüyle soruyor.

Ayrıntısı kardeş metinlerde olan konular burada tekrar edilmez:
[`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md) (idempotency, retry, devre kesici),
[`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md) (harcama tavanı, kill
switch, dayanıklı yürütme), [`llm-uretim-yolu.md`](llm-uretim-yolu.md) (canlıda LLM),
[`mcp-adapter.md`](mcp-adapter.md) §7 (araç açıklaması, hata sözleşmesi),
[`evals-llm-observability.md`](evals-llm-observability.md) (EDD, CI kapısı). Hafta 10'da ise "ajan
kararını kim verir" tartışmasına (`content/hafta-10/gun-5.md`) bir yapı sözlüğü ekler.

## 1. İş akışı mı ajan mı — grafı kim sahipleniyor?
Anthropic'ten Erik Schluntz ve Barry Zhang'ın "Building Effective Agents" yazısı (19 Aralık 2024) iki kavramı
ayırır. **İş akışında** LLM çağrıları ve araçlar, önceden yazılmış kod yolları üzerinden bağlanır;
grafiği mühendis çizer. **Ajanda** LLM araçlarını ve sonraki adımını kendisi seçer; grafiği model
çalışma anında çizer. İkisinin de temel birimi **güçlendirilmiş LLM'dir (augmented LLM)**: arama
(retrieval), araç (tools) ve hafıza (memory) bağlanmış tek bir çağrı. Fark, bu birimleri kimin
sıraladığıdır.

| Desen | Mekanik | Ne zaman | Tuzak |
|---|---|---|---|
| **Zincirleme** | 1. çağrının çıktısı 2.'nin girdisi; arada **kod kapısı** (gate) | İş temiz bir doğrusal sıraya ayrılıyorsa | Kapı yoksa ilk adımın hatası sessizce akar |
| **Yönlendirme** | Sınıflandırıcı girdiyi uygun işleyiciye gönderir | Kategorileri farklı girdiler (iade / hata / satış) | Düşük güvende insana devretme eşiği gerekir |
| **Paralelleştirme** | N çağrı aynı anda: **bölümleme** ya da **oylama** | Bağımsız parçalar ya da çoklu görüş | N kopya bağımsız oy değildir (§6.5) |
| **Orkestratör–işçi** | LLM alt işleri çalışma anında seçer, dağıtır, sentezler | Alt işler önceden belli değil ama döngü sınırlı | Bütçesiz kalırsa artık ajandır |
| **Değerlendirici–iyileştirici** | Biri önerir, öteki değerlendirir; geçene kadar tekrar | Ölçütü net, yinelemeyle iyileşen iş | Aynı model değerlendirirse onaylayıcıya döner (§2.2) |

İş akışı üç yerde kazanır. Adımları sayabiliyorsan saymalısın. Bütçe sınırlıysa iş akışının adım
sayısı sabittir; ajanınki kontrolden çıkabilir. Uyum (compliance) gerekiyorsa denetçi grafiği okumak
ister; onu yürütme kayıtlarından tahmin etmek zorunda kalmamalı. Ajan da üç yerde kazanır: sonraki
adımın son sonuca bağlı olduğu açık uçlu araştırmada, adım sayısı bilinmeyen uzun işlerde ve doğru
iş akışını henüz bilmediğin yeni bir alanda. Framework kararı en sona kalır. Desen başına ~10–15
satır doğrudan API kodu yazılabilir; framework yalnız gerçek bir ihtiyaç (ör. dayanıklı durum, §4)
varsa hak edilir.

> *Şerh:* 14-01 dersi "2026'daki her ajan 2022 ReAct döngüsünün bir varyantıdır" diyor. Döngünün
> iskeleti (gözle → düşün → eyle) gerçekten ortak, ama "her" kelimesi ölçülmüş değil. Döngüyü
> ayrıştıran plan-sonra-yürüt (plan-then-execute) tasarımları da var. Sistemden sisteme değişen şey
> döngünün kendisi değil, **etrafındaki sözleşmelerdir**: durma, doğrulama, durum.

## Diyagram 1 — İş akışı ↔ ajan spektrumu ve beş desen
Soldan sağa gittikçe grafın sahipliği koddan modele geçer. Yeşil kutularda bütün kenarları kod çizer.
Mavi kutularda model sınırlı bir alanda seçim yapar. Turuncu kutuda grafı tümüyle model çizer.

```svg
<svg viewBox="0 0 680 410" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İş akışı ile ajan arasındaki spektrum: soldan sağa grafın sahipliği koddan modele geçer; en solda tek çağrı yani güçlendirilmiş LLM, bağlamı kod koyar; sonra zincirleme yani çağrı, kod kapısı, çağrı; sonra yönlendirme yani sınıflandırıcı girdiyi işleyiciye gönderir; bu üçünde bütün kenarları kod çizer ve AI Öğretmen bu bölgededir; ikinci sırada paralelleştirme yani bölümleme ya da oylama, orkestratör işçi yani LLM alt işleri seçer ama döngü sınırlıdır, değerlendirici iyileştirici yani öner değerlendir tur sınırına kadar tekrarla; en altta ajan yani sonraki adımı model seçer ve adım sayısı bilinmez; kural en basitten başla, sağa ancak ölçülmüş bir ihtiyaç iter">
  <defs>
    <marker id="am-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İş akışı ↔ ajan — grafı kim sahipleniyor?</text>
  <text x="340" y="43" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Anthropic'in beş deseni, sahiplik ekseninde</text>

  <line x1="40" y1="66" x2="636" y2="66" stroke="#5D7789" stroke-width="1.6" marker-end="url(#am-1a)"/>
  <text x="40" y="82" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">KOD sahiplenir (iş akışı)</text>
  <text x="640" y="82" text-anchor="end" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">MODEL sahiplenir (ajan)</text>

  <rect x="30" y="94" width="196" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="128" y="114" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">0 · Tek çağrı</text>
  <text x="128" y="131" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">güçlendirilmiş LLM</text>
  <text x="128" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arama · araç · hafıza</text>
  <text x="128" y="159" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bağlamı KOD koyar</text>

  <rect x="242" y="94" width="196" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="340" y="114" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">1 · Zincirleme</text>
  <text x="340" y="133" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çağrı → [kod kapısı] → çağrı</text>
  <text x="340" y="150" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">doğrusal ayrışan iş</text>
  <text x="340" y="163" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kapı yoksa hata sessizce akar</text>

  <rect x="454" y="94" width="196" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="552" y="114" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">2 · Yönlendirme</text>
  <text x="552" y="133" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınıflandır → işleyici A | B | C</text>
  <text x="552" y="150" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kategorileri farklı girdi</text>
  <text x="552" y="163" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">düşük güven → insana</text>

  <rect x="30" y="190" width="196" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="128" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · Paralelleştirme</text>
  <text x="128" y="229" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">böl → N çağrı → birleştir</text>
  <text x="128" y="246" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bölümleme ya da oylama</text>
  <text x="128" y="259" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">N kopya ≠ N bağımsız oy</text>

  <rect x="242" y="190" width="196" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="340" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">4 · Orkestratör–işçi</text>
  <text x="340" y="229" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">LLM alt işleri seçer → işçiler</text>
  <text x="340" y="246" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alt işler önceden belli değil</text>
  <text x="340" y="259" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">döngü SINIRLI — bütçe şart</text>

  <rect x="454" y="190" width="196" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="552" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">5 · Değerlendirici–iyileştirici</text>
  <text x="552" y="229" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">öner → değerlendir → (tekrar)</text>
  <text x="552" y="246" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tur sınırı 1–3, sonra insan</text>
  <text x="552" y="259" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">mümkünse değerlendirici = KOD</text>

  <rect x="30" y="286" width="620" height="56" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="340" y="307" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">AJAN — sonraki adımı model seçer, adım sayısı bilinmez</text>
  <text x="340" y="326" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yalnız adımlar önceden sayılamıyorsa · bedeli: bütçe, durma koşulu, son-durum doğrulaması (Diyagram 2)</text>

  <rect x="30" y="354" width="408" height="22" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="234" y="369" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">AI Öğretmen burada: 0 + 1 + kod kapısı, tek düzeltme turu (§8)</text>

  <rect x="30" y="382" width="620" height="22" rx="6" fill="#1C3247"/>
  <text x="340" y="397" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kural: en soldan başla — sağa ancak ölçülmüş bir ihtiyaç iter</text>
</svg>
```

## 2. Ajan döngüsü — durma koşulları ve araç çağrısı sözleşmesi
Ajanın çekirdeği ReAct döngüsüdür (Yao vd., ICLR 2023): model düşünür, araç seçer, sonucu okur,
yeniden düşünür. Bir sohbet botunu ajana çeviren beş parça vardır: büyüyen bir **mesaj tamponu**,
ada göre çağrılan bir **araç kaydı** (tool registry), bir **durma koşulu**, bir **tur bütçesi** ve
araç çıktısını modelin okuyacağı metne çeviren bir **gözlem biçimlendirici**. Mimari açıdan
önemli olan üç kural şu:

- **Durma koşulları ayrı ayrı tanımlanır.** Model `bitir` der, asistan turunda araç çağrısı olmaz,
  azami tura ya da token'a ulaşılır, ya da korkuluk tetiklenir. Yalnız birine güvenen döngü ya erken
  durur ya hiç durmaz. "Araç çağrısı yoksa bitti" kuralı ucuzdur, ama model açıklama yazıp aracı
  unutursa döngü erken kapanır. Açık bir `bitir` aracı niyeti görünür kılar, ama **beyan kanıt
  değildir**. İki yolun arkasına da kodla yapılan bir **hedef kontrolü** koyarsın (§7.3).
- **Tur bütçesi iş sınıfına göre seçilir.** "Bir sorunun cevabını bul" ile "bir ortamı yapılandır"
  aynı tavanı almaz.
- **Her hata bir gözlemdir.** Yığındaki her 400 hatası modele okunabilir bir metin olarak döner,
  döngüyü çökertmez.

### 2.1 Araç çağrısı sözleşmesi — hiçbir çağrıya güvenme
Çalışma zamanı (runtime) her araç çağrısını yürütmeden önce doğrular (14-06). Şema tamsayı beklerken
`"5"` geldiyse ve anlamı belirsiz değilse **çevirir** (type coercion). Belirsizse reddeder. Enum
dışı değer (şemada `acik | kapali` varken `"devam_ediyor"`), eksik zorunlu alan ya da bozuk biçim
(tarih ve e-posta regex ile değil, gerçek ayrıştırıcıyla denetlenir) **yapılandırılmış bir hata
gözlemiyle** reddedilir. Gözlem neyin yanlış olduğunu ve ne beklendiğini söyler ki model doğru
biçimle yeniden deneyebilsin. Paralel çağrılarda her sonuç kendi korelasyon kimliğiyle (`tool_use_id`)
eşlenir. Araçlar dar tutulur: genel bir `kabuk_calistir(komut)` tehlike işaretidir, `git_durumu()`
daha güvenlidir. Araç çıktısı da **güvenilmeyen girdidir**; ayrıntısı
[`llm-uygulama-guvenligi.md`](llm-uygulama-guvenligi.md)'nde.

### 2.2 Değerlendirici döngüsü — Self-Refine ve CRITIC
Değerlendirici–iyileştiricinin iki akademik atası var (14-05). **Self-Refine** (Madaan vd., NeurIPS
2023) tek modele üret → eleştir → düzelt rollerini verir. **CRITIC** (Gou vd.) eleştiriyi **dış bir
araca dayandırır**: arama, kod yorumlayıcı, test, tip denetleyici. Ürün döngüsü için çıkan kurallar:
değerlendiriciyi mümkünse dış kanıta bağla, çünkü halüsinasyon onu üreten modele ikna edici görünür.
Aynı model aynı üslupla hem üretip hem eleştirirse "bence iyi" noktasında buluşur (rubber-stamp);
değerlendiricinin prompt'u yapısal olarak farklı olmalı ya da ayrı bir model kullanılmalı. Bu, Hafta
8, Gün 3'teki "kendine benzeyeni sevme" yanlılığının döngüdeki hâlidir. Durma politikası şöyle:
doğrulayıcı geçerse dur; ya da model "sorun yok" der ve en az 2 tur geçtiyse dur; ya da azami tura
ulaşınca dur. **1–3 tur** ayır, sonrası insana gider.

## Diyagram 2 — Ajan döngüsü, durma ve doğrulama noktalarıyla
Döngünün **dört yerinde** kod karar verir, model değil: araç çağrısı doğrulaması, bütçe kapısı,
hedef kontrolü ve yan etki sınırı. Kırmızı oklar başarısız durumların **gözlem olarak** modele
döndüğünü gösterir.

```svg
<svg viewBox="0 0 680 452" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ajan döngüsü ve durma ile doğrulama noktaları: görev ve bağlam model adımına girer; model ya bitir der ya da araç çağırır; bitir derse hedef kontrolü yani kodla son durumun yeniden yoklanması yapılır, geçerse bitti, kalırsa doğrulanmadı gözlemi olarak modele döner; araç çağırırsa önce şema, enum, zorunlu alan ve izin doğrulaması yapılır, geçersizse yapılandırılmış hata gözlemi döner, geçerliyse dar araç zaman aşımı ve korelasyon kimliğiyle yürütülür; yan etkili işlem bu döngüde değil deterministik adımda yapılır; sonuç ya da hata gözlem olarak biçimlendirilir ve bütçe kapısına gider; adım, token, süre ve korkuluk bütçesi varsa modele döner, aşıldıysa durulur ve insana devredilir; durma koşulu tek değildir">
  <defs>
    <marker id="am-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="am-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ajan döngüsü — kodun karar verdiği dört nokta</text>

  <rect x="30" y="64" width="180" height="44" rx="10" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="120" y="83" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">GÖREV + BAĞLAM</text>
  <text x="120" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">hedef ve kabul ölçütü yazılı</text>

  <rect x="250" y="64" width="180" height="44" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="340" y="83" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">MODEL ADIMI</text>
  <text x="340" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">düşün → araç seç ya da bitir</text>

  <rect x="470" y="64" width="180" height="44" rx="10" fill="#FBEDEA" stroke="#B3402F" stroke-width="1.6"/>
  <text x="560" y="83" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3402F" font-family="system-ui">HEDEF KONTROLÜ (kod)</text>
  <text x="560" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">beyana değil SON DURUMA bak</text>

  <line x1="210" y1="86" x2="248" y2="86" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-2a)"/>
  <line x1="430" y1="86" x2="468" y2="86" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-2a)"/>
  <text x="449" y="80" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">bitir</text>

  <path d="M560,64 L560,48 L360,48 L360,62" fill="none" stroke="#B3402F" stroke-width="1.4" marker-end="url(#am-2b)"/>
  <text x="460" y="43" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">kaldı → "doğrulanmadı" gözlemi</text>

  <rect x="30" y="160" width="180" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="120" y="179" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">BÜTÇE KAPISI (kod)</text>
  <text x="120" y="195" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">adım · token · süre · korkuluk</text>

  <rect x="250" y="160" width="180" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="340" y="179" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">ÇAĞRI DOĞRULAMA (kod)</text>
  <text x="340" y="195" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">şema · enum · zorunlu · izin</text>

  <rect x="470" y="160" width="180" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="560" y="179" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">BİTTİ</text>
  <text x="560" y="195" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">kanıtıyla: kayıt/dosya var</text>
  <line x1="560" y1="108" x2="560" y2="158" stroke="#25804C" stroke-width="1.5" marker-end="url(#am-2a)"/>
  <text x="568" y="138" font-size="8" fill="#25804C" font-family="system-ui">geçti</text>

  <line x1="340" y1="108" x2="340" y2="158" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-2a)"/>
  <text x="346" y="138" font-size="8" fill="#5D7789" font-family="system-ui">araç çağrısı</text>

  <rect x="250" y="256" width="180" height="48" rx="10" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="340" y="275" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">YÜRÜT</text>
  <text x="340" y="291" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">dar araç · zaman aşımı · korelasyon id</text>
  <line x1="340" y1="208" x2="340" y2="254" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-2a)"/>
  <text x="346" y="235" font-size="8" fill="#5D7789" font-family="system-ui">geçerli</text>

  <rect x="470" y="256" width="180" height="48" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4" stroke-dasharray="5 4"/>
  <text x="560" y="275" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">YAN ETKİ SINIRI (§3)</text>
  <text x="560" y="291" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">yazma/ödeme → deterministik adım</text>
  <line x1="430" y1="280" x2="468" y2="280" stroke="#C77A17" stroke-width="1.3" stroke-dasharray="4 3" marker-end="url(#am-2a)"/>

  <rect x="30" y="256" width="180" height="48" rx="10" fill="#FBEDEA" stroke="#B3402F" stroke-width="1.6"/>
  <text x="120" y="275" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3402F" font-family="system-ui">DUR → insana devret</text>
  <text x="120" y="291" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">neden durduğu kayda geçer</text>
  <line x1="120" y1="208" x2="120" y2="254" stroke="#B3402F" stroke-width="1.5" marker-end="url(#am-2b)"/>
  <text x="126" y="235" font-size="8" fill="#B3402F" font-family="system-ui">aşıldı</text>

  <rect x="250" y="352" width="180" height="48" rx="10" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="340" y="371" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">GÖZLEM</text>
  <text x="340" y="387" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">sonuç ya da hata — çökme değil</text>
  <line x1="340" y1="304" x2="340" y2="350" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-2a)"/>

  <path d="M430,184 L450,184 L450,376 L432,376" fill="none" stroke="#B3402F" stroke-width="1.4" marker-end="url(#am-2b)"/>
  <text x="456" y="340" font-size="8" fill="#B3402F" font-family="system-ui">geçersiz → yapılandırılmış hata</text>

  <path d="M250,376 L226,376 L226,186 L212,186" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#am-2a)"/>
  <path d="M210,168 L232,168 L232,100 L248,100" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#am-2a)"/>
  <text x="236" y="134" font-size="8" fill="#5D7789" font-family="system-ui">bütçe var</text>

  <rect x="30" y="418" width="620" height="26" rx="7" fill="#1C3247"/>
  <text x="340" y="435" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Durma koşulu tek değildir: bitti beyanı + hedef kontrolü + bütçe + korkuluk</text>
</svg>
```

## 3. Yan etkiler yalnız deterministik adımlarda
14-15 dersi (CrewAI) tek bir kural için okunmaya değer: otonom bir ekip bir aracı beklediğinden fazla
kez çağırabilir, bu yüzden **POST, DELETE ve ödeme bir iş akışı adımına aittir, otonom ekibin
aracına asla.** Ders, CrewAI dokümanının "üretime hazır her uygulamada Flow (deterministik akış) ile
başla" tavsiyesini de aktarır. Mimari karşılığı şudur. Ajan **okur ve önerir**: arama, sınıflandırma,
taslak, plan. Geri alınamayan işlemi (kayıt yazmak, e-posta göndermek, para çekmek) doğrulanmış,
tipli bir öneri üzerinden **kod** yapar. Ajanın serbest döngüsünde bir yazma aracı yine de kaldıysa,
yeniden deneme aynı işlemi iki kez yapmamalıdır. Bunun yolu **idempotency anahtarıdır**; ayrıntısı
[`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md)'nde (14-34 dersi de aynı uyarıyı
yapar).

Aynı ilkenin genel hâli 16-16 dersinden gelir: **mekanizma kodda, LLM yalnız anlatır.** Sayısal
teklifi (fiyat, süre, miktar) deterministik bir üretici hesaplar, LLM yalnız metnini yazar. Teklif bir
yapıysa (görev ayrıştırması, rol ataması) LLM taslağı yazar, taslak şema ve kısıt denetiminden geçer.
Ajanın özel karalama alanı (private scratchpad) karşı tarafa giden kanaldan ayrı tutulur.
*Şerh:* Ders bu ayrımın anlaşma oranını ~%27'den ~%89'a çıkardığını bir makaleye (arXiv:2402.15813)
dayandırıyor. Bu, dar parametreli bir pazarlık deneyidir. Taşınabilir olan ilkedir, rakam değil.

Korkuluk yerleşiminde bir ödünleşim var (14-16). Korkuluk ana çağrıyla **paralel** çalışırsa gecikme
azalır, ama tetiklendiğinde ana çağrının token'ı boşa gider. **Engelleyici (blocking)** çalışırsa
gecikme artar, israf olmaz. Yan etkili bir adımın önündeki korkuluk her zaman engelleyicidir;
paralel korkuluk tetiklendiğinde işlem çoktan yapılmış olabilir.

## 4. Önce çalışma zamanı şekli, sonra framework
Framework seçmeden önce işin çalışma zamanı şeklini (runtime shape) seç (14-29):

| Şekil | Ne zaman | Gözlenecek |
|---|---|---|
| **İstek–yanıt** (senkron HTTP) | Yalnız kısa işler; dersin pratik eşiği ~30 sn | erişim günlüğü + iz (span) |
| **Akış** (SSE/WebSocket) | Kullanıcı ilerlemeyi görmeli | ilk token gecikmesi |
| **Kuyruk / arka plan** | Uzun, çok adımlı ajan işleri | kuyruk derinliği, iş süresi, **DLQ boyu** |
| **Dayanıklı yürütme** (durable) | Adım sayısı bilinmiyor, baştan başlatmak pahalı | adım başına kontrol noktası |
| **Olay güdümlü / zamanlanmış** | Tetikleyiciyle (PR açıldı) ya da gece işi | tetik → başlangıç gecikmesi |

Üç hata en sık görülür: 5 dakikalık işi istek–yanıta koymak (kullanıcı bekler, işçiler birikir,
yeniden denemeler katlanır), **ölü mektup kuyruğu (DLQ) olmayan kuyruk** (başarısız iş sessizce
kaybolur) ve izi dışa aktarılmayan arka plan işi. Dayanıklı yürütmenin iki kuralı framework'ten
bağımsızdır (14-13). **(a)** Kontrol noktası (checkpoint) **tüm durumu** yazar; yalnız konuşma
turlarını yazarsan araç durumu ve hafıza yazmaları kurtarılamaz. **(b)** Saat, rastgele tohum ve dış
API cevabı gibi deterministik olmayan girdiler kaydedilir; yoksa yeniden başlatılan adım farklı
sonuç üretir. Her kenarı koşullu bir graf, akıl yürütülemeyen bir durum makinesidir; doğrusal zincir
tercih edilir. Ayrıntı [`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md)'nde.

## 5. Tekli ajandan çoklu ajana — ne zaman ve hangi parçalarla
Tekli ajan üç yerde kırılır (16-01): **bağlam doygunluğu** (araç sonuçları birikir, erken turdaki
ayrıntı kaybolur), **rol karışıklığı** ("sen araştırmacı, kodcu ve inceleyicisin" diyen prompt her
işi yarım yapar) ve **sıralı darboğaz**. Çoklu ajanın bedeli de aynı derste yazıyor ve akılda
tutulacak cümle şu: **her ajan sınırı kayıplı bir sıkıştırmadır** (lossy compression). A'nın tüm
bağlamı B için bir mesaja özetlenir. Koordinasyon mantığı kendi başına bir hata kaynağıdır; gecikme
ve maliyet ajan sayısıyla çarpılır.

> *Şerh:* Dersin "20'den az araç çağrısı ve 100k token'a sığıyorsa tekli kal" eşiği ölçülmüş değil,
> bir **başparmak kuralıdır** (heuristic). "SWE-bench'te tekli ajan sistemleri daha düşük puan alır"
> cümlesi de aşırı genelleme: güçlü tekli ajanlar, hatta ajansız sabit iş akışları (ör. "Agentless"
> yaklaşımı) aynı tabloda rekabetçi sonuçlar almıştır. Lider tablosu kirlenmesi (contamination) için
> Hafta 7 quizinin S2 sorusuna bak.

16-04 dersi bütün çoklu ajan framework'lerini **dört ilkele** (primitive) indirger:
1. **Ajan:** sistem prompt'u + araç listesi + model. **Durumsuzdur** (stateless).
2. **Devir (handoff):** denetimin başka ajana geçmesi. Aracın sonraki ajanı döndürmesi, bir graf
   kenarı ya da konuşmacı seçimi biçiminde olur.
3. **Paylaşılan durum (shared state):** mesaj havuzu, kara tahta (blackboard), anahtar-değer deposu.
4. **Orkestratör:** sırada kimin konuşacağına karar veren. **Statik** graf (denetlenebilir, yeniden
   oynatılabilir), **LLM seçimli** (uyarlanabilir), **devir güdümlü** ya da **kuyruk güdümlü** olabilir.

Kilit gözlem şu: **dört ilkelden yalnız paylaşılan durum durum taşır.** Hafıza zehirlenmesi, mesaj
sırası, sürümleme ve yazma çekişmesi gibi ilginç hataların hepsi orada yaşar. Yeni bir framework
geldiğinde değişiklik günlüğünü değil, dört ilkele eşlemesini okursun.

## 6. Topolojiler — supervisor, hiyerarşi, sürü, kara tahta, oylama
### 6.1 Supervisor (orkestratör–işçi)
Lider ajan planlar, alt soruları işçilere dağıtır, işçi özetlerini sentezler; ham malzemeyi görmez
(16-05). Kazancın üç mekanizması var: işçi başına **taze bağlam**, prompt ile uzmanlaşma ve
paralellik. Kurallar şunlar. **Çabayı sorunun karmaşıklığına göre ölçekle**: basit soruya tek ajan
yeter ve bu tahmini lider yapar. **Sentez çelişkisi yüzeye çıkarılır**: iki işçi çelişirse lider ya
yeniden sorar ya da çelişkiyi açıkça yazar; en kötü hata tarafı sessizce seçmektir. İşçiye zaman
aşımı ve token tavanı konur. Adımlar sıralı bağımlıysa (pipeline), soru basitse ya da sıkı denetim ve
yeniden oynatma gerekiyorsa (statik graf) supervisor yanlış seçimdir.

> *Şerh:* Ders Anthropic'in araştırma sistemi için üç rakam aktarıyor: tekli ajana göre "+%90.2",
> BrowseComp varyansının "%80'ini token kullanımı açıklıyor" ve "~15× token". Üçünün kaynağı da
> Anthropic'in kendi mühendislik yazısı, hepsi **şirket içi değerlendirme** (internal eval); dışarıda
> tekrarlanmış değil. Ders 15×'i "tekli ajana göre" diye aktarıyor; **yanlış**. Yazıda karşılaştırma
> **sohbete göredir**: tekli ajan sohbetin ~4×'ü, çoklu ajan ~15×'i kadar token harcar; %90.2 ise
> "tekli ajan Claude Opus 4'e göre" şirket içi araştırma değerlendirmesindeki farktır
> ([Anthropic, multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system)). Taşınabilir olan rakam değil mekanizmadır: çoklu ajan büyük ölçüde **daha
> fazla token = daha fazla taze bağlam** satın almaktır. Maliyeti için bkz.
> [`finops-ai-tokenomics.md`](finops-ai-tokenomics.md).

### 6.2 Hiyerarşi ve anlam kayması
Hiyerarşi iç içe supervisor'dur (16-06). LLM yönetici, ekibinin ne bildiğini kalıcı olarak bilmez;
organizasyonu her turda bağlamından yeniden çıkarır. Üç hata tekrar tekrar görülür. **Yanlış
atama:** hedef yanlış ayrıştırılır, alt yönetici verileni sadakatle yapar, hata ancak bir kat
yukarıda görünür. **Anlam kayması:** alt yönetici "X **doğrulanamadı**" der, üst yönetici "X **teyit
edilmedi**" diye özetler, bir sonraki katta bu "X yanlış" olur. **Uzlaşma döngüsü:** anlaşamayan alt
yöneticilere iş yeniden dağıtılır, biraz farklı cevaplar gelir, döngü sürer. Önlemler: derinlik ≤ 2,
uzlaşma için tur bütçesi (genelde 2), her sentezde dayandığı yaprak çıktılara **köken** (provenance).
Karar sorusu şudur: **gerçekten bağımsız alt ekipler mi var, yoksa ağaç kılığına girmiş doğrusal bir
akış mı?** İkincisi sahte hiyerarşidir (14-28); bu, Conway yasasının ajan versiyonudur
([`hafta06-mimari-stiller-c4.md`](hafta06-mimari-stiller-c4.md)). *Şerh:* "Derinlik 2 tavanı" ampirik
diye sunuluyor ama ölçümüne kaynak gösterilmiyor; makul bir varsayılan olarak oku.

### 6.3 Sürü (swarm) / kuyruk
Merkezi karar verici yoktur; işçiler ortak bir kuyruktan iş çeker (16-09). **Çok sayıda bağımsız,
süresi değişken iş** varsa ve sıra değil toplam verim önemliyse uyar. **Sıralı iş akışında** ve
**tek tutarlı plan isteyen işte** başarısız olur: bir araştırmacı sürüsü kopuk olgular üretir, rapor
üretmez. Tipik arıza **açlıktır** (starvation): herkes hızlı işi çekerse uzun iş hiç alınmaz. Çare,
bekleme süresiyle artan önceliktir (aging).

### 6.4 Kara tahta ve hafıza zehirlenmesi
16-13 dersi referans hatayı tek senaryoyla anlatır. Getirme ajanı sayfada "%4.2 iyileşme" okur,
paylaşılan duruma "%42" yazar. Özetleyici "büyük %42 kazanç" diye aktarır, analist "benimseyin" der.
**Hiçbir ajan çökmedi, hiçbir test kırılmadı; sistem "çalıştı".** Paylaşılan durum bir ajanın
halüsinasyonunu olguya çevirdi. Üç yapısal önlem var:
1. **Her yazmada köken:** kim yazdı, ne zaman, hangi prompt'la, hangi kaynağa dayanarak.
2. **Yalnız-ekle (append-only) sürümleme:** düzeltme eski kaydın üstüne yazılmaz, onu geçersiz kılan
   yeni bir kayıttır.
3. **Yazamayan doğrulayıcı:** havuzu okuyan ama havuza yazma yetkisi olmayan bir ajan. Kaynakları
   bağımsız olarak yeniden getirir, tutarsızlığı ayrı bir kanala (insana) bildirir. Çıktısı havuza
   dönerse, zehirli havuz doğrulayıcıyı da zehirler.

Yazma çekişmesinin çareleri klasik veri sistemi çareleridir: tek yazıcı, sürüm numarasıyla iyimser
eşzamanlılık ya da konu bölümleme ([`hafta11-ddia-veri-yogun.md`](hafta11-ddia-veri-yogun.md)).

### 6.5 Oylama ve tartışma — N kopya bağımsız oy değildir
N örnek alıp çoğunluk oyuna başvurmak (öz-tutarlılık, self-consistency) en ucuz birleştirmedir. Ama
**aynı modelin N kopyası aynı yanlılığı ve aynı halüsinasyonları paylaşır**; hatalar ilişkilidir
(16-15, 16-23: tek kültür çöküşü, monoculture collapse). Beş kopyanın oybirliği, tek modelin beş kez
aynı şeyi söylemesidir. Bağımsızlık için **çeşitlilik** gerekir: farklı model, prompt, kaynak ya da
araç. Tartışma turları sınırlıdır (2–3) ve kararı **ayrı bir hakem** verir; sınırsız turda **uyma
yanlılığı** (conformity) kazanır. Cevabı görüşe dayalı sorularda oylamanın anlamı yoktur.
*Şerh:* Dersin "~4 ajandan sonra koordinasyon vergisi" bulgusu tek bir benchmark'a (MARBLE) ait;
sabit sınır değil, "ajan eklemenin getirisi hızla düşer" uyarısı olarak oku.

### 6.6 Karar sırası ve hop sayacı
14-28 dersinin Anthropic rehberinden çıkardığı sıra şöyle: **(1)** tekli ajan + iş akışı (buradan
başla) → **(2)** supervisor (2–4 uzman) → **(3)** sürü (gecikme akıl yürütme netliğinden önemliyse)
→ **(4)** hiyerarşi (yalnız supervisor bağlamı uzmanların tanımını taşıyamıyorsa) → **(5)** tartışma
(doğruluk maliyetten önemliyse). En sık ihlal **önce topoloji düşünmektir**: "çoklu ajan lazım"
kararı, hangi sorunu çözeceği belirlenmeden verilir. Devir güdümlü sistemlerde A → B → A → B sekmesi
olur; her devirde bir **hop sayacı** tutulur ve N devirden sonra devir reddedilir (14-16).

## Diyagram 3 — Tekli → çoklu ajan: topoloji karar ağacı
Sorular yukarıdan aşağıya sorulur. Her "evet" seni daha basit bir sonuçta durdurur. Sağdaki kutudaki
kurallar seçtiğin her topolojide geçerlidir.

```svg
<svg viewBox="0 0 680 486" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Topoloji karar ağacı: birinci soru adımları önceden sayabiliyor musun; evetse iş akışı yani beş desen; hayırsa ikinci soru tek bağlam ve tek rol yetiyor mu; evetse tekli ajan ve adım bütçesi; hayırsa üçüncü soru alt işler birbirinden bağımsız mı; hayırsa yani sıralı bağımlıysa pipeline yani sözleşmeli devirle sıralı ajanlar; evetse dördüncü soru tutarlı tek plan ya da sentez gerekiyor mu; hayırsa sürü ya da kuyruk yani çok, bağımsız, değişken süreli iş ve açlığa karşı yaşlandırma; evetse supervisor yani iki ila dört uzman, taze bağlam, çelişkiyi yüzeye çıkar; supervisor bağlamı uzman listesini taşıyamıyorsa hiyerarşi yani derinlik en fazla iki, uzlaşma bütçesi ve köken; her topolojide geçerli kurallar hop sayacı, kodla karar veren en az bir doğrulayıcı, paylaşılan durumda köken ve yalnız ekle, işçi başına zaman aşımı ve token tavanı, sentez çelişkisini yüzeye çıkarma">
  <defs>
    <marker id="am-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tekli → çoklu ajan — topoloji karar ağacı</text>
  <text x="340" y="40" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her "evet" seni daha basit bir sonuçta durdurur</text>

  <rect x="240" y="56" width="200" height="44" rx="10" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="340" y="75" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">S1 · Adımları önceden</text>
  <text x="340" y="90" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">sayabiliyor musun?</text>
  <rect x="20" y="56" width="190" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="115" y="75" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">İŞ AKIŞI</text>
  <text x="115" y="90" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">5 desen (Diyagram 1)</text>
  <line x1="240" y1="78" x2="212" y2="78" stroke="#25804C" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="226" y="72" text-anchor="middle" font-size="8" fill="#25804C" font-family="system-ui">evet</text>

  <rect x="240" y="136" width="200" height="44" rx="10" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="340" y="155" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">S2 · Tek bağlam + tek rol</text>
  <text x="340" y="170" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">yetiyor mu?</text>
  <rect x="20" y="136" width="190" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="115" y="155" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">TEKLİ AJAN</text>
  <text x="115" y="170" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">adım bütçesi + hedef kontrolü</text>
  <line x1="240" y1="158" x2="212" y2="158" stroke="#25804C" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="226" y="152" text-anchor="middle" font-size="8" fill="#25804C" font-family="system-ui">evet</text>
  <line x1="340" y1="100" x2="340" y2="134" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="348" y="121" font-size="8" fill="#5D7789" font-family="system-ui">hayır</text>

  <rect x="240" y="216" width="200" height="44" rx="10" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="340" y="235" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">S3 · Alt işler birbirinden</text>
  <text x="340" y="250" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">bağımsız mı?</text>
  <rect x="20" y="216" width="190" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="115" y="235" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">PIPELINE</text>
  <text x="115" y="250" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">sıralı ajanlar · sözleşmeli devir</text>
  <line x1="240" y1="238" x2="212" y2="238" stroke="#186399" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="226" y="232" text-anchor="middle" font-size="8" fill="#186399" font-family="system-ui">hayır</text>
  <line x1="340" y1="180" x2="340" y2="214" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="348" y="201" font-size="8" fill="#5D7789" font-family="system-ui">hayır</text>

  <rect x="240" y="296" width="200" height="44" rx="10" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="340" y="315" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">S4 · Tutarlı tek plan /</text>
  <text x="340" y="330" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">sentez gerekiyor mu?</text>
  <line x1="340" y1="260" x2="340" y2="294" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="348" y="281" font-size="8" fill="#5D7789" font-family="system-ui">evet</text>

  <rect x="470" y="296" width="190" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="565" y="315" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">SÜRÜ / KUYRUK</text>
  <text x="565" y="330" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">çok, bağımsız iş · yaşlandırma</text>
  <line x1="440" y1="318" x2="468" y2="318" stroke="#186399" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="454" y="312" text-anchor="middle" font-size="8" fill="#186399" font-family="system-ui">hayır</text>

  <rect x="240" y="376" width="200" height="48" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="340" y="395" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">SUPERVISOR (2–4 uzman)</text>
  <text x="340" y="411" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">taze bağlam · çelişkiyi yüzeye çıkar</text>
  <line x1="340" y1="340" x2="340" y2="374" stroke="#5D7789" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="348" y="361" font-size="8" fill="#5D7789" font-family="system-ui">evet</text>

  <rect x="470" y="376" width="190" height="48" rx="10" fill="#FBEDEA" stroke="#B3402F" stroke-width="1.6"/>
  <text x="565" y="395" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3402F" font-family="system-ui">HİYERARŞİ</text>
  <text x="565" y="411" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">derinlik ≤ 2 · uzlaşma bütçesi</text>
  <line x1="440" y1="400" x2="468" y2="400" stroke="#B3402F" stroke-width="1.5" marker-end="url(#am-3a)"/>
  <text x="454" y="434" text-anchor="middle" font-size="8" fill="#B3402F" font-family="system-ui">yalnız bağlam taşarsa</text>

  <rect x="470" y="56" width="190" height="204" rx="10" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="5 4"/>
  <text x="565" y="76" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Her topolojide geçerli</text>
  <text x="482" y="100" font-size="9" fill="#1C3247" font-family="system-ui">· her devirde hop sayacı</text>
  <text x="482" y="122" font-size="9" fill="#1C3247" font-family="system-ui">· en az bir doğrulayıcı KOD</text>
  <text x="482" y="144" font-size="9" fill="#1C3247" font-family="system-ui">· paylaşılan durumda köken</text>
  <text x="494" y="158" font-size="9" fill="#1C3247" font-family="system-ui">+ yalnız-ekle</text>
  <text x="482" y="180" font-size="9" fill="#1C3247" font-family="system-ui">· işçiye zaman aşımı +</text>
  <text x="494" y="194" font-size="9" fill="#1C3247" font-family="system-ui">token tavanı</text>
  <text x="482" y="216" font-size="9" fill="#1C3247" font-family="system-ui">· rol sözleşmesi: ne yapar,</text>
  <text x="494" y="230" font-size="9" fill="#1C3247" font-family="system-ui">ne YAPMAZ (spec)</text>
  <text x="482" y="250" font-size="9" fill="#1C3247" font-family="system-ui">· N kopya ≠ N bağımsız oy</text>

  <rect x="20" y="448" width="640" height="30" rx="7" fill="#1C3247"/>
  <text x="340" y="467" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Topolojiyi problem seçer, moda değil — önce "çoklu ajan hangi sorunu çözecek?"</text>
</svg>
```

## 7. Çoklu ajan hata taksonomisi ve doğrulama
### 7.1 MAST — hataların payı ve "önce spec" iddiası
16-23 dersi referans taksonomi olarak **MAST**'ı verir: Cemri vd., "Why Do Multi-Agent LLM Systems
Fail?", arXiv:2503.13657, NeurIPS 2025. Makalenin ilk sürümündeki adı MASFT'tir; 14-26 dersi onu bu
adla anar. Dersin aktardığı dağılım, 7 açık kaynaklı çoklu ajan çerçevesinden 1642 yürütme kaydına
dayanıyor:

| Kök kategori | Pay | Örnek | Önlem |
|---|---|---|---|
| **Tanım (spec) sorunları** | %41.77 | rol belirsizliği, eksik görev tanımı, örtük başarı ölçütü | rol sözleşmesi (ne yapar **ve ne yapmaz**), iş başlamadan kabul testi |
| **Koordinasyon hataları** | %36.94 | eşzamansız durum güncellemesi, kayıp mesaj, "A bitti sanıyor, B çalışıyor" | sürümlü durum, kritik mesajda onay (ack), durum eşitleme |
| **Doğrulama boşlukları** | %21.30 | ajan başarı beyan eder, kimse kontrol etmez | bağımsız doğrulayıcı, "C kontrolünden geçmeden B başlamaz" |

Bu dağılım **Hafta 3, Gün 5'in tezini destekliyor.** Orada "sorun genelde ajan eksikliği değil,
tarifin ya da planın zayıflığıdır; zayıf tarifle üç ajan, tek ajandan daha hızlı yanlış yapar"
deniyor ve ajanlar arası yazılı **sprint sözleşmesi** olmadan döngünün kapanmayacağı söyleniyordu.
MAST'ta en büyük pay tanım eksiğine, ikinci pay koordinasyona, yani sözleşmenin bozulmasına düşüyor.

> *Şerh:* Rakamları kullanırken üç sınırı unutma. **(1)** %41.77 / %36.94 / %21.30 oranları makalenin
> v2 sürümüne aittir ([arXiv:2503.13657v2](https://arxiv.org/abs/2503.13657v2)). Güncel v3 (Ekim
> 2025, 1600+ kayıt) kategori toplamı vermez, ilk kategorinin adını "sistem tasarımı sorunları"
> yapar; mod yüzdelerinin toplamı kabaca %44 / %32 / %24'tür — **sıralama aynı kalır**. **(2)** Kayıtlar 7 açık kaynaklı araştırma çerçevesinin
> benchmark görevlerinden gelir, üretimden değil. Dersin "%41–86.7 başarısızlık" rakamı da bu
> çerçevelere aittir; "çoklu ajan sistemlerinin yarısı batar" diye genellenemez. **(3)** MAST'ın
> "tanım" kategorisi rol ve sistem tanımını da kapsar, bizim "spec"ten geniştir. Destek "önce spec"e
> **yön** olarak geçerli; "spec yazınca hataların %42'si gider" diye okunamaz. Ayrıca 16-08 dersinin
> "her çoklu ajan hatası eksik doğrulamaya dayandırılabilir" cümlesi, aynı dersin verdiği %21.3 ile
> çelişir.

### 7.2 Eleştirmen ≠ doğrulayıcı
16-08 dersinin dört rolünden (plancı, yürütücü, eleştirmen, doğrulayıcı) son ikisi sıkça karıştırılır.
**Eleştirmen** (critic) bir LLM'dir: öznel, üslup ve tasarım sorunlarını yakalar, **akıcı bir metinle
kandırılabilir**. **Doğrulayıcı** (verifier) deterministik bir programdır: test koşucusu, tip
denetleyici, şema doğrulayıcı. Kanıtla geçti/kaldı der. İkisi birden kullanılır, ama kural şu:
**sistemde kararı kodla verilen en az bir rol olmalı.** Her rolü LLM olan, her rolün "bence iyi"
dediği sistem klasik MAST hatasıdır. [`harness-mekanigi.md`](harness-mekanigi.md) §5'in diliyle
eleştirmen bir **guide**, doğrulayıcı bir **sensor**dur. Hafta 3, Gün 5'in üç-ajan desenindeki
"değerlendirici" bir eleştirmendir; arkasında `./mvnw verify` gibi bir doğrulayıcı yoksa döngü
yalnız kanaat üretir. Bir ek ilke: **uydurmadan önce sor.** Yürütücüye eksik bir ayrıntı gerektiğinde
onu uydurmak yerine ilgili role sorması talimat olarak verilir (ChatDev).
*Şerh:* Dersteki "PwC doğruluğu %10'dan %70'e çıkardı" bir CrewAI vaka çalışmasıdır, satıcı
kaynaklıdır; yöntemi ve temel çizgisi (baseline) yayımlanmamıştır. Alıntılanmaz.

### 7.3 "Başardım" yanılsaması — transkripte değil son duruma bak
14-26 dersinin en pratik bulgusu şu: ajan "başaramadım" ile "bu iş imkânsız" farkını çoğu zaman
göremez ve **400 hatası aldıktan sonra bile döngüyü kapatmak için başarı mesajı yazabilir.** Bir de
zincirleme hata var: uydurulmuş tek bir ürün kodu dört alt API çağrısını tetikler, tek hata çok
sistemli bir olaya dönüşür. Önlemler:
- **Son durumu yeniden yokla:** dosya gerçekten oluştu mu, kayıt veritabanında var mı? Kontrol ajanın
  beyanından bağımsız olarak kodla yapılır (Diyagram 2'deki hedef kontrolü).
- **Değerlendirmeyi de son duruma göre yap.** Berkeley'nin araç çağırma lider tablosu (BFCL) v3'ten
  beri araç dizisinin metnini değil API'nin son durumunu kontrol eder (14-06). Aynı hedefe farklı
  yoldan varan ajan cezalandırılmaz; hiçbir şey yapmamış ama kendinden emin ajan ödüllendirilmez.
- **Yalnız çökmeleri işaretlemek yetmez;** ajan hatalarının çoğu geçerli görünen çıktı üretir.
- **Yörünge uzunluğunu ölç:** 14-20 dersi, OSWorld-Human verisine göre en iyi ajanların gereken adımın
  1.4–2.7 katını kullandığını aktarıyor. Bu tek bir benchmark'ın rakamı; taşınabilir ders, altın
  yörüngeye (gold trajectory) göre adım oranını izlemektir.
- **Her korkuluk bir eval vakasına, her üretim hatası yeni bir vakaya karşılık gelir** (14-30).
- **Yavaş hatayı vekil sinyallerle izle** (16-23): uyuşma oranı, yeniden deneme oranı, çıktı uzunluğu
  dağılımı. Yeniden denemelerin zincirleme çoğalması (retry fırtınası) devre kesiciyle durdurulur
  ([`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md)).

## 8. Bu depodan vaka — AI Öğretmen fiilen hangi desen?
**AI Öğretmen bir ajan değil, bir iş akışıdır.** Koddan kanıtlar:

- **Modele hiç araç verilmiyor.** `ai/OgretmenAiClient` arayüzünde yalnız `cagir(AiIstek)` ve
  `cagirGoruntulu(...)` var. `AiIstek` işlem etiketi, model türü, sistem prompt'u, geçmiş, kullanıcı
  mesajı ve `maxTokens`'tan ibaret; araç listesi alanı yok. `src/main` altında Spring AI'nın `@Tool` ya da `ToolCallback`
  kullanımı yok; bu adlar yalnız `mcp/` paketindeki iki yorumda, "kullanmıyoruz" demek için geçiyor.
- **Çağrı sayısı kodda sabit.** `QuizUretimService.uret` bir çağrı yapar; JSON ayrıştırılamazsa
  **en fazla bir** düzeltme çağrısı yapar ve durur. `DegerlendirmeService.degerlendir` de böyledir.
  Hiçbir serviste ikiden fazla `aiClient.cagir` yok ve hiçbiri döngü içinde değil.
- **Bağlamı kod getiriyor.** `BaglamServisi` anlam eşleşmeli kitap bağlamını (ADR-0005) ve öğrenci
  hafızasını çağrıdan önce prompt'a koyuyor: Diyagram 1'deki "0 · güçlendirilmiş LLM".
- **Yönlendirme statik.** Hangi işin hangi modele gideceğine (`ModelTuru.GUCLU` / `HIZLI`) bir
  sınıflandırıcı LLM değil, ADR-0003'ün tablosu karar veriyor.

Değerlendirme akışı **kod kapılı bir zincirdir**: bağlam (kod) → gönderim kaydı (`gonderimRepo.save`)
→ değerlendirme (`degerlendirme.st`,
GUCLU) → kapı (`JsonCevapParser`) → gerekirse tek düzeltme turu → zarif düşüş
(`Degerlendirme.hamMetinden`) → değerlendirmeli kayıt (ikinci `gonderimRepo.save`) → hafıza güncelleme (`hafiza-guncelle.st`,
HIZLI) → karakter sınırlı atomik dosya yazımı. Düzeltme turu, değerlendiricisi **kod** olan ve tek
turla sınırlı bir değerlendirici–iyileştiricidir. Quiz üretiminde de dört seçeneği olmayan ya da
`dogruIndex`'i 0–3 dışında kalan soru kodla atılır. §3'ün kuralı burada eksiksiz uygulanmış: **bütün
yazmalar kod adımında, model hiçbir yazma kararı vermiyor.** Bu bir eksiklik değil, doğru seçim:
adımlar sayılabilir, maliyet sınırlı (ADR-0003), her çağrı `ai_kullanim`'a işlem etiketiyle yazılıyor
ve tek HTTP isteğinde bitiyor (§4'teki istek–yanıt şekli).

**Ajan döngüsü bu depoda nerede?** Depoda değil, **istemcide**. MCP sunucusu (ADR-0007; kod `mcp` ve
`mcp-http` profillerinde duruyor, canlı sunucular 2026-10-21'de kapatıldı) dışarıdaki bir ajana
(Claude Code/Desktop) araç sunuyor. ADR-0009 ve `MimariTestleri.mcp_llmsiz` bu katmanın LLM
çağırmamasını zorluyor. §2.1'in sözleşmesinin bir kısmı zaten var: `McpAdaptorDestek.tamsayi`
belirsiz olmayan `"5"`'i sayıya çeviriyor, eksik alanı reddediyor; `OgretmenMcpAraclari.arac`
sarmalayıcısı hatayı çökme yerine `isError` sonucu olarak, yani **gözlem olarak** döndürüyor. Ama §3
açısından öğretici bir boşluk vardı: iki **yazma aracı** ajanın serbest döngüsüne açık.
`gorev_isaretle` 2026-10-26'ya kadar görev durumunu **tersine çeviriyordu** (toggle); istemci çağrıyı
yeniden denerse görev geri açılıyordu. ADR-0016 bunu düzeltti: araç artık "çevir" değil "şu değere
ayarla" diyor (opsiyonel `hedef_durum`, varsayılan `true`; servis `IlerlemeService.gorevDurumuAyarla`),
yani idempotent — tekrar denenen çağrı işareti geri almaz. `statik_quiz_kaydet` ise hâlâ her çağrıda
**yeni satır** ekliyor ve idempotency anahtarı yok; yeniden deneme çift kayıt üretir. ADR-0016 karar 2
bunu **kabul edilen risk** olarak yazdı (yalnız-ekle deneme günlüğü, MCP sunucuları kapalı, etkisi bir
fazla deneme) ve yeniden açma koşulunu koydu: MCP sunucusu yeniden uzakta çalışırsa ya da quiz denemesi
bir değerlendirmeye girerse. Ayrıntı: [Araç çağrısı güvenilirliği](arac-cagrisi-guvenilirligi.md) §8.

## Öz-sınav (kendini test et)
1. "İş akışı" ile "ajan" arasındaki farkı **tek bir soruyla** ifade et. Orkestratör–işçi deseni neden
   hâlâ iş akışı sayılabilir ve hangi noktada ajana dönüşür?
2. Bir ajan döngüsünün durma koşullarını say. "Araç çağrısı yoksa bitti" kuralının riski nedir ve
   açık bir `bitir` aracı bu riski neden tek başına kapatmaz?
3. Model `durum: "devam_ediyor"` ile bir araç çağırdı, şemada yalnız `acik | kapali` var. Çalışma
   zamanı ne yapar? Model `"5"` gönderdiğinde ne yapar? İki durum neden farklı ele alınır?
4. Ajan e-posta gönderebilmeli. Bu yeteneği mimaride **nereye** koyarsın? Korkuluğu paralel mi
   engelleyici mi çalıştırırsın, neden?
5. Bir iş 4–6 dakika sürüyor ve adım sayısı belli değil. Hangi çalışma zamanı şeklini seçersin? DLQ
   olmazsa ne olur? Kontrol noktasında yalnız konuşma turlarını saklamak neden yetmez?
6. Dört ilkeli say. "Yalnız paylaşılan durum durum taşır" gözlemi hataları nerede aramanı söyler?
7. Hiyerarşik bir sistemde alt yönetici "doğrulanamadı" yazdı, en üstteki rapor "yanlış" diyor. Bu
   hatanın adı nedir? Hangi önlemler alınır, hangi karar sorusu hiyerarşiyi gereksiz kılabilir?
8. Kara tahtadaki "%4.2 → %42" vakasında hiçbir test kırılmadı. Üç yapısal önlemi say. Doğrulayıcının
   havuza **yazamaması** neden kritik?
9. "Aynı modelden beş ajan oyladı, oybirliğiyle doğru" çıkarımının hatası nedir? Oylamayı anlamlı
   kılmak için ne değişmeli?
10. MAST dağılımını Hafta 3, Gün 5'teki "önce tarif" iddiasına bağla. Dağılımı kullanırken hangi üç
    sınırı söylersin?
11. **Şerh sorusu:** Bir sunumda üç cümle var: "Anthropic'in çoklu ajan sistemi tekli ajandan %90.2
    daha iyi", "PwC doğruluğu %10'dan %70'e çıkardı", "ajan projelerinin %88'i başarısız". Her birini
    kaynağına ve sınıfına göre nasıl okursun? Hangisinden taşınabilir bir ders çıkar?
12. AI Öğretmen'in hangi desen olduğunu **koddan** kanıtla (en az üç kanıt). Ajan döngüsü bu depoda
    nerede çalışıyor ve §3 açısından hangi iki araç sorunlu?

### Cevap anahtarı (önce kendin cevapla, sonra bak)
1. **Grafı kim sahipleniyor?** Kod sahipleniyorsa iş akışı, model sahipleniyorsa ajan. Orkestratör–
   işçide alt işleri LLM seçer, ama döngü sınırlıdır ve çerçeveyi kod çizer. Orkestratör bütçesiz
   biçimde kendi sonraki adımına karar vermeye başlarsa ajana dönüşür.
2. `bitir` beyanı, araç çağrısı olmaması, azami tur, azami token/süre, korkuluk. Model yalnız açıklama
   yazıp aracı unutursa "araç yok = bitti" döngüyü erken kapatır. `bitir` niyeti görünür kılar ama
   beyan kanıt değildir; arkasında kodla yapılan bir hedef kontrolü gerekir.
3. Enum ihlali yapılandırılmış bir hata gözlemiyle reddedilir ("izin verilen: acik, kapali"), döngü
   çökmez. `"5"`'in anlamı tek olduğu için sayıya çevrilir. Fark belirsizliktir: tek anlamlı değer
   çevrilir, başka anlama gelebilecek değer reddedilir.
4. Deterministik bir kod adımına. Ajan taslak üretir, kod doğrular ve gönderir. Döngüde yazma aracı
   kalacaksa idempotency anahtarı şarttır. Korkuluk engelleyici çalışır; paralel korkuluk
   tetiklendiğinde e-posta çoktan gitmiş olabilir.
5. Kuyruk/arka plan, gerekirse dayanıklı yürütme; ~30 sn'yi aşan iş istek–yanıta uymaz. DLQ yoksa
   başarısız işler sessizce kaybolur. Yalnız konuşma turları saklanırsa araç durumu ve hafıza
   yazmaları kurtarılamaz; saat, tohum ve dış API cevabı kaydedilmezse yeniden başlatma farklı sonuç
   verir.
6. Ajan, devir, paylaşılan durum, orkestratör. Diğer üçü durumsuz olduğu için zehirlenme, sıra, sürüm
   ve yazma çekişmesi hataları paylaşılan durumda aranır.
7. Anlam kayması. Önlemler: derinlik ≤ 2, uzlaşma tur bütçesi, her sentezde köken. Karar sorusu:
   gerçekten bağımsız alt ekipler var mı, yoksa ağaç kılığında doğrusal bir akış mı? İkincisiyse
   sıralı akış kullanılır.
8. Her yazmada köken, yalnız-ekle sürümleme, yazamayan doğrulayıcı. Doğrulayıcının çıktısı havuza
   dönerse zehirli havuz onu da zehirler; bu yüzden ayrı bir kanala (insana) raporlar.
9. Kopyalar aynı yanlılığı ve halüsinasyonları paylaşır, hataları ilişkilidir; oybirliği bağımsız
   teyit değildir. Model, prompt, kaynak ya da araç çeşitlendirilir, turlar sınırlanır, karar ayrı
   hakeme verilir. Soru görüşe dayalıysa oylama zaten anlamsızdır.
10. En büyük pay (~%42–44, makale sürümüne göre) tanım sorunlarına, ikincisi koordinasyona düşüyor; bu, "sorun ajan eksikliği
    değil tarif zayıflığı" ve "yazılı sprint sözleşmesi" tezleriyle aynı yöne bakar. Sınırlar:
    rakamlar makalenin v2 sürümünden (v3'te oranlar az kayar, sıra aynı); kayıtlar 7 açık kaynaklı araştırma
    çerçevesinden, üretimden değil; "tanım" kategorisi bizim "spec"ten geniştir.
11. %90.2: Anthropic'in şirket içi değerlendirmesi, satıcı birincil kaynak; taşınabilir ders
    mekanizmadır (daha çok token = taze bağlam), rakam değil. PwC: CrewAI vaka çalışması, yöntemsiz,
    alıntılanmaz. %88: kaynağı belirsiz dolaşım rakamı, alıntılanmaz.
12. Kanıtlar: `AiIstek`'te araç listesi yok; `src/main`'de `@Tool`/`ToolCallback` kullanımı yok; çağrı
    sayısı sabit (bir çağrı + en fazla bir düzeltme, döngü yok); bağlamı `BaglamServisi` kodla koyuyor;
    model seçimini ADR-0003 belirliyor. Desen, kod kapılı zincirleme. Ajan döngüsü MCP istemcisinde
    (ADR-0007/0009). Yazma araçlarından `gorev_isaretle` eskiden toggle'dı (yeniden deneme durumu geri
    çevirirdi); 2026-10-26'da ADR-0016 ile idempotent "ayarla"ya çevrildi. `statik_quiz_kaydet`
    (idempotency anahtarı yok; çift kayıt) yeniden açma koşulu yazılı, kabul edilen risk olarak kaldı.

## Kaynak ve lisans
Bu metin bir çeviri değil, bir **sentezdir**: dersler okunup bu programın kitlesine (modeli eğiten
değil, kullanan mimar) ve bu depoya bağlanarak Türkçe yeniden yazıldı; ders kodları kopyalanmadı.

- **AI Engineering from Scratch** — Rohit Ghumare, github.com/rohitg00/ai-engineering-from-scratch,
  MIT Lisansı, "Copyright (c) 2026 Rohit Ghumare". Kullanılan dersler (`phases/…/docs/en.md`):
  - 14-agent-engineering: 01 the-agent-loop, 05 self-refine-and-critic, 06
    tool-use-and-function-calling, **12 anthropic-workflow-patterns (ana ders)**, 13
    langgraph-stateful-graphs, 26 failure-modes-agentic, 28 orchestration-patterns, 29
    production-runtimes, 30 eval-driven-agent-development; tek kural için 15, 16, 20, 34.
  - 16-multi-agent-and-swarms: 01 why-multi-agent, 04 primitive-model, 05
    supervisor-orchestrator-pattern, 06 hierarchical-architecture, 08 role-specialization, 09
    parallel-swarm-networks, 13 shared-memory-blackboard, 15 voting-debate-topology, 16
    negotiation-bargaining (yalnız mimari kuralı), 23 failure-modes-mast-groupthink.
- Derslerin dayandığı birincil kaynaklar: Erik Schluntz & Barry Zhang, "Building Effective Agents"
  (Anthropic, 19 Ara 2024); Anthropic, "How we built our multi-agent
  research system"; Yao vd., ReAct (arXiv:2210.03629); Madaan vd., Self-Refine; Gou vd., CRITIC
  (arXiv:2305.11738); Cemri vd., MAST (arXiv:2503.13657).
- **Toplu şerh:** Metin içinde şerhlenenlere (§1, §3, §5, §6.1, §6.2, §6.5, §7.1, §7.2, §7.3) ek
  olarak, derslerdeki "%88 kurumsal ajan projesi başarısız" (ön-baskı/dolaşım rakamı), Agno "2μs
  örnekleme", Mem0 LoCoMo puanları ve framework hız karşılaştırmaları (satıcı ya da kendi beyanı olan
  benchmark'lar) bu metne bilerek alınmadı.
- Bu deponun vakası (§8) kaynak koddan okundu: `ai/OgretmenAiClient`, `ai/JsonCevapParser`,
  `service/QuizUretimService`, `service/DegerlendirmeService`, `service/HafizaService`,
  `service/BaglamServisi`, `mcp/OgretmenMcpAraclari`, `mcp/McpAdaptorDestek`, `MimariTestleri`;
  ADR-0003, ADR-0005, ADR-0007, ADR-0009.

### Bu programdaki ilgili kaynaklar
- [`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md) ve
  [`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md): §3, §4 ve §7.3'ün
  ayrıntısı.
- [`mcp-adapter.md`](mcp-adapter.md): araç arayüzü ve hata sözleşmesi.
- [`harness-mekanigi.md`](harness-mekanigi.md): guide/sensor; eleştirmen ve doğrulayıcının karşılığı.
- [`evals-llm-observability.md`](evals-llm-observability.md): EDD, CI kapısı, judge yanlılıkları.
- [`llm-uygulama-guvenligi.md`](llm-uygulama-guvenligi.md): güvenilmeyen araç çıktısı, hafıza
  zehirlenmesinin güvenlik yüzü.
- [`hafta11-ddia-veri-yogun.md`](hafta11-ddia-veri-yogun.md), [`hafta06-mimari-stiller-c4.md`](hafta06-mimari-stiller-c4.md),
  [`finops-ai-tokenomics.md`](finops-ai-tokenomics.md): yazma çekişmesi, Conway, çoklu ajan maliyeti.
