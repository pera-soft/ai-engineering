---
baslik: "Canlıda LLM — Gecikme SLO'su, AI Gateway, Hata Katmanları ve Güvenli Yayın"
yazar: "AI Öğretmen çalışma metni — AI Engineering from Scratch (Rohit Ghumare, MIT) dersleri: 11-13 production-app, 17-01 managed-llm-platforms, 17-08 inference-metrics-goodput, 17-19 ai-gateways, 17-20 shadow-canary-progressive, 17-21 ab-testing-llm-features, 17-22 load-testing-llm-apis, 17-24 chaos-engineering-llm + bu deponun kendi vakası"
tarih: "2026-10-25"
url: "https://github.com/rohitg00/ai-engineering-from-scratch"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 8,9
---

### Bu metin ne anlatıyor?
Yapay zekâ özelliği dizüstünde çalışınca iş bitmez; canlıda (production) dört soru doğar: **ne kadar
hızlı**, **hangi kapıdan çıkıyor**, **bozulunca ne oluyor**, **yeni sürümü nasıl güvenle açıyorum**.
Bu metin dördüne birer karar kuralı verir:

1. **Gecikmeyi tek sayı sanma — üçe ayır.** İlk token süresi (TTFT = kuyruk + ağ + prompt'u işleme)
   prompt uzadıkça büyür; token başına süre (TPOT) çıktı uzadıkça birikir; **uçtan uca = TTFT +
   TPOT × çıktı token sayısı.** Kaldıracı hangi terim baskınsa ona uygula.
2. **SLO'yu çok kısıtlı yaz, "goodput" ile ölç.** Bir istek ancak *bütün* kısıtları aynı anda
   sağlarsa "iyi"dir (TTFT ≤ a **VE** TPOT ≤ b **VE** E2E ≤ c); goodput iyi isteklerin payıdır.
   Yük testinde tek prompt'u tekrarlama: önbellek kapasiteyi şişirir.
3. **AI gateway bir giden port'un (outgoing port) adaptörüdür.** Yönlendirme, yedek model,
   yeniden deneme, hız sınırı, sır yönetimi, gözlem, korkuluk — hepsi tek yerde. Ürün için kritik
   çağrıda **en az iki bağımsız sağlayıcı**.
4. **Üç hata katmanı, üç ayrı ilaç:** API hatası → geri çekilmeli yeniden deneme; model hatası →
   doğrulama hatasını içeren yeniden deneme; bağımlılık hatası → zarif düşüş. Yedek model zinciri
   kaliteyi erişilebilirliğe takas eder; katman katman yeniden deneme yükü **çarpar**.
5. **Yayın merdiveni:** gölge (shadow) → kanarya (canary; LLM kapıları: istek başı maliyet, ret
   oranı, çıktı uzunluğu) → gerekiyorsa A/B. Eval "işi yapabiliyor mu?", A/B "kullanıcı önemsiyor
   mu?" sorusunu cevaplar. **Geri alma = bayrak çevirmek + sabitlenmiş model kimliği**, yeniden
   dağıtım değil.

## Neden bu özet
Programda bu konuların parçaları dağınık duruyordu. [`hafta08-ai-engineering-ii.md`](hafta08-ai-engineering-ii.md)
"Maliyet ve gecikme" bölümü akışı (streaming) tek satırla anar ama gecikmenin **bileşenlerini**
vermez; [`hafta11-ddia-veri-yogun.md`](hafta11-ddia-veri-yogun.md) "ortalama değil yüzdelik dilim"
der ama LLM'e özgü TTFT/TPOT ayrımını bilmez; [`mikroservis-monolit-hatalar.md`](mikroservis-monolit-hatalar.md)
genel zaman aşımı/yeniden deneme/devre kesici anlatır ama **modelin kendisinin** hata
üretebileceğini (bozuk JSON, uydurma araç adı) ele almaz; Hafta 8 yeni sürümü "küçük bir kullanıcı
dilimine aç" diye geçer, ama o dilimde **neye bakılacağını** söylemez. Hiçbir kaynakta AI gateway bir
mimari öğe olarak geçmiyordu — oysa bu deponun **ADR-0004 (OpenRouter)** kararı tam olarak bir
gateway kararıdır.

Bu metin o boşlukları modeli **eğiten** değil **kullanan** mimarın gözünden doldurur: GPU'ya
dokunmazsın, ama prompt uzunluğunu, çıktı sınırını, sağlayıcı sayısını, yeniden deneme politikasını
ve yayın kapılarını **sen** seçersin. Her bölüm bu deponun gerçek koduna bağlanır:
`ai/OgretmenAiClient` (port), `ai/SpringAiOgretmenClient` (adaptör), `ai/AiYapilandirma` (yeniden
deneme ve zaman aşımı bütçesi), `ai/AiHataCevirici` (hata sınıflandırması), `ai/JsonCevapParser`
(model hatası). Maliyet tarafı [`finops-ai-tokenomics.md`](finops-ai-tokenomics.md)'de, eval tarafı
[`evals-llm-observability.md`](evals-llm-observability.md)'de, yeniden deneme ayrıntısı
[`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md)'de; burada tekrarlanmaz, bağlanır.

## 1. Gecikmenin anatomisi — TTFT, TPOT, uçtan uca
Bir LLM isteğinin süresi tek bir blok değildir; farklı nedenlerle büyüyen üç parçadan oluşur.

- **İlk token süresi (TTFT — time to first token)** = kuyrukta bekleme + ağ (TLS dahil) + **ön
  doldurma (prefill)**, yani modelin bütün prompt'u okuyup işlemesi. Prefill, prompt uzunluğuyla
  büyür. Uzun sistem prompt'u, şişkin sohbet geçmişi, bağlama eklenmiş belge → TTFT'yi uzatır.
- **Token başına süre (TPOT — time per output token)**, bazı araçlarda **token arası gecikme (ITL —
  inter-token latency)**: ilk token'dan sonra her yeni token için geçen süre. Çıktı token token,
  sırayla üretildiği için (bkz. [`model-ve-caba.md`](model-ve-caba.md) §2) bu süre çıktı uzunluğuyla
  **çarpılır**.
- **Uçtan uca gecikme (E2E — end-to-end)** = TTFT + TPOT × çıktı token sayısı (+ dönüş ağı).

Pratik sonuç: **hangi terim baskınsa kaldıraç odur.** Uzun prompt + kısa cevap (ör. "şu metni tek
kelimeyle sınıfla") TTFT'nin baskın olduğu iştir — prompt'u kırpmak, önbelleklemek işe yarar. Kısa
prompt + uzun cevap (ör. rapor üretimi) TPOT'un baskın olduğu iştir — orada prompt'u kısaltmak
neredeyse hiçbir şey kazandırmaz; **çıktıyı sınırlamak** (max_tokens, "kısa yaz" talimatı, daha hızlı
model) kazandırır.

**Akış (streaming) neyi değiştirir?** Toplam süreyi değil, *algılanan* süreyi. Akışta kullanıcı
TTFT'den sonra metni görmeye başlar; akışsız çağrıda kullanıcı **E2E'nin tamamını** boş ekranda
bekler.

**Bu projedeki bağ.** `SpringAiOgretmenClient` modeli akışsız çağırır (`chatModel.call(...)`; kodda
akış yok). Yani öğrenci her zaman E2E'yi bekler. `AiYapilandirma` bunu bilerek bir zaman bütçesi
kurar: bağlantı 5 sn, okuma 120 sn, yeniden deneme penceresi 30 sn; en kötü durum ~157 sn, ön yüzün
180 sn'lik kesme süresinin altında. Kod yorumu okuma süresinin neden 120 sn olduğunu da söyler:
3000–4000 token'lık rapor/quiz/görsel üretimi ~60–80 sn sürüyor. Bu tam olarak **TPOT baskın** bir
iş yüküdür — haftalık raporu hızlandırmanın yolu prompt'u kırpmak değil, çıktıyı kısaltmak ya da
akışa geçmektir.

**Ölçüm tuzağı.** Benchmark araçları ITL'i farklı tanımlar: biri ilk token'ı hesaba katmaz (2.
token'dan başlar), diğeri katar. Aynı koşu için iki araç farklı TPOT raporlar. Kural: **bir gecikme
rakamı aktarıyorsan aracın adını ve tanımını yaz.**

> *Şerh:* Ders, belirli bir açık model + NVIDIA sunucu yığını için "ortalama TTFT 162 ms, TPOT 7,33
> ms" gibi referans rakamlar verir. Bunlar **kendi GPU'sunu işleten** biri için anlamlıdır; API
> üzerinden model kullanan bizim kitle için hedef değildir — senin TTFT'ne sağlayıcının kuyruğu,
> gateway sıçraması ve bölge mesafesi eklenir. Hedefini kendi ölçümünden koy.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir LLM isteğinin zaman çizelgesi: istek önce kuyrukta bekler, sonra ağdan geçer, sonra model prompt'un tamamını işler (prefill); bu üçünün toplamı ilk token süresi TTFT'dir ve prompt uzadıkça büyür; ardından çıktı token'ları tek tek gelir, aralarındaki süre token başına süre TPOT'tur ve çıktı uzadıkça birikir; uçtan uca süre TTFT artı TPOT çarpı çıktı token sayısıdır. Altta iki senaryo: uzun prompt kısa cevapta TTFT baskındır ve kaldıraç prompt'u kırpmak veya önbelleklemektir; kısa prompt uzun cevapta, örneğin bu depodaki haftalık raporda, TPOT çarpı N baskındır ve kaldıraç çıktıyı sınırlamaktır. Bu depoda çağrı akışsızdır, kullanıcı uçtan uca sürenin tamamını bekler">
  <defs>
    <marker id="lu-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir isteğin süresi üç parçadır — kaldıraç baskın parçadadır</text>
  <text x="330" y="41" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Amber = TTFT bileşenleri · yeşil = çıktı token'ları · E2E = TTFT + TPOT × N</text>

  <line x1="30" y1="110" x2="635" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#lu-1a)"/>
  <text x="630" y="128" text-anchor="end" font-size="9" fill="#5D7789" font-family="system-ui">zaman →</text>

  <rect x="30" y="78" width="60" height="28" rx="5" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="60" y="96" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">kuyruk</text>
  <rect x="92" y="78" width="46" height="28" rx="5" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="115" y="96" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">ağ</text>
  <rect x="140" y="78" width="130" height="28" rx="5" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="205" y="96" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">prefill (prompt'u işle)</text>

  <rect x="276" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="304" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="332" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="360" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="388" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="416" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <text x="470" y="97" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">· · ·</text>
  <rect x="520" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="548" y="84" width="12" height="18" rx="2" fill="#25804C"/>
  <rect x="576" y="84" width="12" height="18" rx="2" fill="#25804C"/>

  <line x1="30" y1="62" x2="276" y2="62" stroke="#C77A17" stroke-width="1.4"/>
  <line x1="30" y1="57" x2="30" y2="67" stroke="#C77A17" stroke-width="1.4"/>
  <line x1="276" y1="57" x2="276" y2="67" stroke="#C77A17" stroke-width="1.4"/>
  <text x="153" y="56" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">TTFT — prompt uzadıkça büyür</text>
  <line x1="288" y1="62" x2="304" y2="62" stroke="#25804C" stroke-width="1.4"/>
  <text x="360" y="66" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">TPOT (token arası)</text>

  <line x1="30" y1="140" x2="588" y2="140" stroke="#186399" stroke-width="1.6"/>
  <line x1="30" y1="134" x2="30" y2="146" stroke="#186399" stroke-width="1.6"/>
  <line x1="588" y1="134" x2="588" y2="146" stroke="#186399" stroke-width="1.6"/>
  <text x="309" y="158" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">E2E = TTFT + TPOT × çıktı token sayısı — akışsız çağrıda kullanıcı bunun TAMAMINI bekler</text>

  <rect x="30" y="176" width="295" height="104" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="177" y="196" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Uzun prompt + kısa cevap</text>
  <rect x="48" y="206" width="200" height="16" rx="3" fill="#C77A17"/>
  <rect x="252" y="206" width="20" height="16" rx="3" fill="#25804C"/>
  <text x="177" y="242" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">TTFT baskın (ör. sınıflandırma, kısa değerlendirme)</text>
  <text x="177" y="258" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kaldıraç: prompt'u kırp · önbellekle</text>
  <text x="177" y="273" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">çıktıyı kısaltmak az kazandırır</text>

  <rect x="340" y="176" width="295" height="104" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="487" y="196" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kısa prompt + uzun cevap</text>
  <rect x="358" y="206" width="40" height="16" rx="3" fill="#C77A17"/>
  <rect x="402" y="206" width="210" height="16" rx="3" fill="#25804C"/>
  <text x="487" y="242" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">TPOT × N baskın (ör. haftalık rapor, 3000+ token)</text>
  <text x="487" y="258" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kaldıraç: çıktıyı sınırla · akışa geç</text>
  <text x="487" y="273" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">prompt'u kırpmak az kazandırır</text>

  <rect x="30" y="292" width="605" height="28" rx="8" fill="#1C3247"/>
  <text x="332" y="311" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Önce hangi terimin baskın olduğunu ölç; sonra kaldıracı seç</text>
</svg>
```

## 2. Goodput — SLO'yu çok kısıtlı yazmak
"Saniyede 15.000 token işliyoruz" cümlesi ürün hakkında bir şey söylemez. İsteklerin %40'ı 2
saniyeyi aşıyorsa kullanıcılar çoktan gitmiştir. **Verim (throughput)** filonun verimliliğini ölçer;
tek tek isteklerin sağlığını ölçmez.

Ürün için anlamlı ölçü **goodput**'tur: **bütün SLO kısıtlarını aynı anda sağlayan isteklerin payı.**

> goodput = (TTFT ≤ a **VE** TPOT ≤ b **VE** E2E ≤ c koşulunu sağlayan istek sayısı) / toplam istek

Bir istek tek bir kısıtı bile aşarsa "kötü"dür. Bu yüzden üç ayrı panoda üç ayrı yeşil çizgi
görmek seni yanıltabilir: TTFT'si iyi ama E2E'si kötü istekler ile E2E'si iyi ama TTFT'si kötü
istekler farklı isteklerse, her pano yeşil görünürken goodput düşük olabilir. Yüksek verimde %60
goodput başarısızlıktır; düşük verimde %99 goodput hedeftir.

İki ek kural:

- **Ortalama değil, yüzdelik dilim (percentile).** LLM gecikme dağılımı sağa çarpıktır; uzun bir
  komşu istek yüzünden bazı isteklerin TPOT'u birkaç katına fırlar. Ortalama bunu gizler; kullanıcı
  p99'u yaşar. p50 / p90 / p99 üçlüsünü raporla ([`hafta11-ddia-veri-yogun.md`](hafta11-ddia-veri-yogun.md)
  "Ölçmenin doğru birimi" bölümü aynı ilkeyi veritabanı için anlatır).
- **SLO'yu işe göre yaz.** Sohbet ile haftalık rapor aynı SLO'yu taşımaz. Sohbette TTFT
  (akış varsa) ya da kısa E2E önemlidir; raporda kullanıcı bir dakika beklemeye razıdır ama 157
  saniyelik tavanı aşmamak gerekir. Tek bir "gecikme SLO'su" yazan ekip ya sohbeti cezalandırır ya
  raporu yanlış alarm kaynağına çevirir.

**Bu projedeki bağ — veri var, SLO yok.** `ai_kullanim` tablosu (`AiKullanim`: kullanıcı, işlem,
model, girdi/çıktı token, tarih) 2026-10-26'ya kadar **süre kaydetmiyordu**; goodput hesaplanamaz,
ancak log satırlarından tahmin edilirdi. ADR-0016 (karar 4) iki eklemeli sütun getirdi: `sure_ms`
ve `yanit_model` (eski satırlarda boş). `sure_ms`, `SpringAiOgretmenClient`'ta `System.nanoTime()`
ile ölçülen **duvar saati** süresidir ve `chatModel.call`'ın tamamını kapsar — Spring AI'nın kendi
yeniden denemeleri (429/5xx) bu çağrının içinde olduğu için **dahildir**. Yani kullanıcının gerçekten
beklediği E2E'dir, tek denemenin süresi değil. Çağrı akışsız olduğu için TTFT ve TPOT ayrı ölçülmez;
goodput bu depoda yalnız **E2E kısıtıyla** hesaplanabilir. Artık ölçülebilir veri var, ama asıl iş
hâlâ yapılmadı: "`islem` başına hangi E2E kısıtı?" sorusunu yazmak. SLO yazılmadan süre sütunu
yalnızca bir sayı daha olur; ADR-0016 da özet/panoyu bilinçli olarak eklemedi.

## 3. Yük testi — tek prompt'u tekrarlama tuzağı
Klasik bir yük testi aynı isteği binlerce kez gönderir. LLM'de bu, ölçtüğünü sandığın şeyi ölçmez.

- **Prompt tekdüzeliği tuzağı (prompt-uniformity trap):** Aynı prompt'u döngüde gönderirsen
  sağlayıcının önek önbelleği (prefix cache) neredeyse her istekte isabet eder; prefill fiilen
  atlanır ve kapasite olduğundan çok yüksek görünür. Canlı trafik ise farklı uzunlukta, farklı
  önekli isteklerden oluşur. Çözüm: prompt uzunluğunu ve içeriğini **bir dağılımdan örnekle**
  (gerçek trafikten ya da ortalama + standart sapmayla).
- **Kendi önbelleğin de aynı yalanı söyler.** Bu depoda haftalık rapor `RaporService`'te dosyaya
  önbelleklenir (`data/raporlar/...`); yenileme istenmezse model hiç çağrılmaz. `GET /api/rapor/{hafta}`
  ucuna aynı hafta için 500 istek atan bir test **sıfır** model çağrısını ölçer.
- **Test aracı darboğaz olabilir.** Ders, token ölçümünü istemci tarafında Python'da yapan bir yük
  aracının kendi kuyruğunu sunucunun yavaşlığı gibi raporladığı vakayı anlatır. Kural: ölçüm
  istemcisinin de doyduğunu varsayma, kontrol et.
- Dört desen (sabit, rampa, ani sıçrama, uzun süreli/soak) genel yük testi bilgisidir; LLM'e özgü
  olan, her desende **prompt dağılımını** gerçekçi tutmaktır.

**API kullanan mimar için ek bir incelik:** ücretli bir sağlayıcıya yük testi yapmak hem para yakar
hem de sağlayıcının hız sınırına (429) çarpar — yani kendi kapasiteni değil **onun kotanı** ölçersin.
Burada port/adaptör ayrımı işe yarar: `OgretmenAiClient` arayüzünün arkasına gerçekçi gecikme
üreten bir sahte (stub) adaptör koyarak **kendi** tarafının (bağlantı havuzu, iş parçacıkları, DB
yazımı) kapasitesini ölçersin; sağlayıcı tarafını ise küçük ve gerçekçi bir örnekle ayrıca
doğrularsın. İkisini tek testte karıştırmak hangi tarafın doyduğunu gizler.

## 4. AI gateway — giden port'un adaptörü
Ürünün birden çok sağlayıcıyı (ve modeli) çağırıyorsa, her sağlayıcının farklı SDK'sı, hata modeli,
hız sınırı ve kimlik doğrulaması vardır. Bunu her serviste ayrı ayrı çözmek, her servisi her
sağlayıcıya bağlar. **AI gateway** bu işi tek bir katmanda toplar: genellikle OpenAI-uyumlu tek bir
API sunar, arkada sağlayıcılara dağıtır.

Hafta 9'un diliyle: çekirdeğin dış dünyadan istediği şey ("şu prompt'a cevap ver") bir **giden port**
(outgoing / driven port), o isteği gerçek bir sağlayıcıya ileten şey bir **adaptör**dür
([`hafta09-hexagonal-clean.md`](hafta09-hexagonal-clean.md) "Gelen ve giden port"). Gateway, bu
adaptörün kendisi ya da adaptörün konuştuğu dış katmandır.

**Bu depodaki vaka.** Üç halka var:

1. **Port:** `ai/OgretmenAiClient` — `cagir(AiIstek)`, `cagirGoruntulu(...)`; içinde yalnız
   projenin kendi tipleri (`Islem`, `ModelTuru`, `AiYanit`) var, Spring AI tipi sızmıyor. Servisler
   (`DegerlendirmeService`, `HafizaService`, `AtolyeService`…) yalnız bu arayüzü görür.
2. **Adaptör:** `ai/SpringAiOgretmenClient` + `ai/AiYapilandirma` — Spring AI'nın OpenAI-uyumlu
   istemcisi, kısa yeniden deneme şablonu, zaman aşımları, `AiHataCevirici` ile hata çevirisi,
   `ai_kullanim` kaydı. Uygulama içindeki küçük bir gateway gibi davranır.
3. **Dış gateway:** OpenRouter (ADR-0004) — tek anahtarla çok sağlayıcı, kullanım paneli, harcama
   limiti.

`MimariTestleri` bu sınırı mühürler: Spring AI sınıfları yalnız `ai/` paketinde kullanılabilir ve
`ai` katmanına yalnız `service` erişir. ADR-0004'ün kendi cümlesi portun değerini kanıtlar: doğrudan
Anthropic API'ye dönmek gerekirse "starter'ı ve base-url'ü değiştir — `OgretmenAiClient` arayüzü
sayesinde servisler değişmez."

> *Şerh (Hafta 9'un "portu kim tanımlar" sorusu):* Saf altıgen mimaride portu çekirdek tanımlar ve
> adaptörden ayrı bir pakette durur. Bu depoda `OgretmenAiClient` arayüzü adaptörle **aynı** `ai/`
> paketindedir; katman yönü `service → ai`dir. Bu pragmatik bir sadeleştirmedir: arayüz tipleri saf
> olduğu için servisler sağlayıcıdan bağımsız kalır, ama "port çekirdeğin malıdır" ilkesi paket
> düzeyinde değil, sözleşme düzeyinde korunur. Bunu bilerek kabul edilmiş bir takas olarak oku,
> ders kitabı örneği olarak değil.

**Gateway'in yedi işi** ve bu depodaki karşılığı:

| İş | Ne demek | Bu depoda |
|---|---|---|
| Yönlendirme (routing) | işe göre model/sağlayıcı seçimi | `ModelTuru` GUCLU/HIZLI, model kimlikleri `application.yml`'de (ADR-0003) |
| Yedek (fallback) | 429/5xx/kalite hatasında başka yere git | **Uygulamada yok** — tek model (§6). OpenRouter aynı model için sağlayıcılar arası yedeği varsayılan yapar (`allow_fallbacks: true`); model yedeği (`models` dizisi) kullanılmıyor |
| Yeniden deneme (retry) | sınırlı, geri çekilmeli | 3 deneme, yalnız 5xx/bağlantı hatası, 30 sn pencere (`AiYapilandirma`) |
| Hız sınırı (rate limit) | kiracı/anahtar/model başına | **Yok** — sitede kota yok kararı (ADR-0012); bkz. [`finops-ai-tokenomics.md`](finops-ai-tokenomics.md) |
| Sır referansı (secret reference) | anahtarı çalışma anında kasadan çek | Ortam değişkeni `OPENROUTER_API_KEY`; değiştirmek yeniden başlatma ister (`docs/kurulum-site.md` §8.3) |
| Gözlem (observability) | iz, maliyet, kim/ne | `ai_kullanim`: kullanıcı + işlem + istenen model + token + **süre** (`sure_ms`) + **cevaptaki model** (`yanit_model`) — ADR-0016; pano yok (§2) |
| Korkuluk (guardrail) | PII, kötüye kullanım, konu filtresi | Kısmi: kimliksiz çağrı yapılmaz (fail-closed), ham sağlayıcı gövdesi kullanıcıya/loga gitmez (`AiHataCevirici`) |

**Kendin mi işletirsin, yönetilen mi kullanırsın?** Bu karar teknik zevk değil, iki kısıtla verilir:
**veri ikametgâhı** (verinin hangi ülkede/kimin altyapısında işlendiği; düzenlemeye tabi kiracı
kendi işlettiğin gateway'i gerektirebilir) ve **işletme bütçesi** (kendi gateway'in bir servis daha
demektir: nöbet, güncelleme, ölçekleme). Gateway'in kendisi de TTFT'ye eklenir; gecikme bütçesi
dar bir SLO'da bu sıçrama ölçülmelidir.

> *Şerh — satıcı benchmark'ları:* Ders gateway'leri karşılaştırırken bir satıcının **kendi
> yürüttüğü** benchmark'ını ("rakiplerinden %228 / %859 daha hızlı") ve bir açık kaynak gateway'in
> "~2000 istek/sn'de çöktüğü" iddiasını aktarır; ikinci rakam da **rakip satıcının** benchmark'ından
> gelir. Gateway başına "5–15 ms / 20–40 ms ek gecikme" rakamları da bağımsız kaynak göstermez. Bu
> sayıları seçim gerekçesi yapma; kendi trafiğinle ölç.

## 5. Kritik çağrıda en az iki sağlayıcı — ve rezerve kapasite
**İki sağlayıcı kuralı (two-provider minimum).** Ürünün çalışması için *şart* olan bir LLM çağrısı
tek bir sağlayıcıya bağlıysa, o sağlayıcının kesintisi senin kesintindir. Kural: ürün için kritik
çağrılarda en az iki **bağımsız** sağlayıcı, tek bir gateway arkasında, aralarında otomatik geçiş.
İnce nokta "bağımsız" kelimesindedir: aynı gateway üzerinden iki farklı modele gitmek, gateway'in
kendisi çöktüğünde seni kurtarmaz — aynı **arıza alanını** (failure domain) paylaşırlar.

**Bu depodaki durum.** Tek dış gateway (OpenRouter), tek anahtar, tek model ailesi. ADR-0004 bunu
bilerek kabul eder: "Araya bir sağlayıcı daha girdi (gecikme + kullanım koşulları); kişisel eğitim
aracı için kabul edilebilir." Bu bir eğitim aracı için doğru karardır — bir öğrencinin değerlendirmesi
bir saat gecikse kimse para kaybetmez. Aynı mimari bir müşteri ürününde kabul edilemezdi; fark
mimari değil, **kritiklik sınıfı**dır. Mimarın işi bu sınıfı ADR'ye yazmaktır.

**Rezerve kapasite (provisioned / reserved throughput) ne zaman öder?** Bazı platformlar GPU
kapasitesini sana ayırır: kuyruk değişkenliği azalır, TTFT öngörülebilir olur, ama **boşta da
ödersin**. Mekanik basittir: rezerve kapasitenin saatlik bedeli, aynı saatte isteğe bağlı (on-demand)
ödeyeceğin tutardan düşükse kazanırsın — bu da ancak **sürekli** yüksek kullanımda olur. Gündüz
yoğun, gece boş, hafta sonu ölü bir iş yükü (50–60 geliştiricinin eğitim sitesi tam budur)
isteğe bağlı fiyatlamada kalmalıdır. Rezerve kapasiteyi gecikme SLO'su gerektiriyorsa al, "indirim"
diye alma.

> *Şerh:* Ders başa baş noktasını "~%40–60 sürekli kullanım" diye verir; kaynak göstermez ve oran
> fiyat listesine göre değişir — kendi fiyatınla hesapla. Platformlar arası "50 ms vs 75 ms TTFT"
> karşılaştırması belirli bir açık modelde ve belirli bir tarihte ölçülmüştür; ders ayrıca "Claude
> 3.7"yi güncel model gibi anar — tarihli bilgidir.

## 6. Üç hata katmanı, yedek model zinciri ve yükün çarpılması
Canlıdaki bir LLM uygulaması üç **farklı** yoldan bozulur ve her birinin ilacı farklıdır. Hepsine
aynı "yeniden dene" refleksini uygulamak, ya hatayı uzatır ya faturayı katlar.

| Katman | Örnek | İlaç | Bu depoda |
|---|---|---|---|
| **1. API hatası** | 429 hız sınırı, 5xx, zaman aşımı | geri çekilmeli (backoff) + rastgele sapmalı (jitter) sınırlı yeniden deneme | 5xx/bağlantı: 3 deneme, bekleme 1→2 sn (tavan 4 sn); 401/402/429 **denenmez**, Türkçe nedene çevrilir (`AiHataCevirici`: ANAHTAR_GECERSIZ, KREDI_BITTI, YOGUN…) |
| **2. Model hatası** | bozuk JSON, uydurma araç adı, şemaya uymayan çıktı | **doğrulama hatasını içeren** yeniden deneme ("şu alan eksikti, düzelt") | `JsonCevapParser` toleranslı ayrıştırır, olmazsa `Optional.empty` → çağıran zarif düşer; boş cevap → `AiCiktisiBozukException` |
| **3. Bağımlılık hatası** | vektör deposu, önbellek, korkuluk servisi | **zarif düşüş** (graceful degradation): ana yolu koru, ikincil özelliği atla | `AnlamArama` yoksa no-op; `KitapService` hafta etiketi eşleşmesine düşer (ADR-0005) |

İkinci satır LLM'e özgüdür: geleneksel bir HTTP istemcisi "200 OK" gördüğünde işi bitmiş sayar;
LLM'de 200 OK içinde **bozuk** bir cevap gelebilir. Onu aynı prompt'la körü körüne tekrar istemek
çoğu zaman aynı hatayı üretir; hatayı modele geri vermek düzeltme şansını artırır.

> *Karar notu — 429:* Ders 429'u da geri çekilmeyle yeniden denemeyi önerir. Bu depo 429'u
> **denemez**, kullanıcıya "yoğun" der. İkisi de savunulabilir: yeniden denemek kısa süreli
> sıkışmayı emer, ama paylaşılan tek anahtarda zaten doymuş bir hız sınırına daha çok istek
> yollamak sıkışmayı uzatır. Hangisini seçtiğini ve neden seçtiğini yaz; varsayılanı kendiliğinden
> kabul etme.

**Yedek model zinciri (fallback chain).** Birincil model erişilemezken sırayla düşülen basamaklar:
birincil model → **başka sağlayıcıdaki** eşdeğer model → daha küçük/ucuz model → önbellekteki son
iyi cevap → dürüst "şu an hizmet veremiyoruz" mesajı. Her basamak **kaliteyi erişilebilirliğe
takas eder.** Mimarın kararı zinciri kurmak değil, **işlem başına** zinciri seçmektir: sohbet için
küçük modele düşmek kabul edilebilir; öğrencinin ödevini puanlayan değerlendirmede küçük modelin
verdiği yanlış puan, "şu an puanlayamıyoruz" mesajından **daha kötüdür**. Bazı işlemlerde zincirin
doğru son basamağı dürüst hatadır. Bu depoda zincir yok — ve bu artık yazılı bir karar (ADR-0016
karar 4): OpenRouter aynı model için sağlayıcılar arası yedeği zaten varsayılan yapar
(`allow_fallbacks: true`); uygulamanın başka bir **modele** sessizce düşmesi quiz/değerlendirme JSON
biçimini ve kaliteyi değiştirir; yedeğe geçiş de artık `yanit_model` ile görülebiliyor. Yeniden açma
koşulu: `ai_kullanim` verisi tek modelin süreğen kesintisini gösterirse. Kurulursa `Islem` enum'u
zaten zincirin anahtarı olabilecek yerdedir, haftalık rapor dosya önbelleği de doğal bir
"önbellekteki cevap" basamağıdır.

**Yeniden deneme yükü çarpar (retry amplification).** Varsayımsal bir yığın düşün: her katman kendi
başına makul sayıda deneme yapıyor — ön yüz 2, adaptör 3, gateway 3. Sağlayıcı yavaşladığında tek bir kullanıcı tıklaması
2 × 3 × 3 = **18** çağrıya dönüşebilir ve bu, sağlayıcının zaten zorlandığı anda olur. Ders bunu
kaos deneylerinin bir bulgusu olarak anlatır: sağlayıcı 429 dönünce gateway'in yeniden denemeleri
eşzamanlılığı katlar ve servisin belleğini tüketir. Kural: **yeniden denemeyi tek bir katmana ver**,
diğerleri hatayı yukarı iletsin; her katmanda toplam zaman bütçesi olsun. Bu depoda ön yüz yeniden denemez (180 sn'de keser), yalnız adaptör dener;
30 sn yeniden deneme penceresi + 157 sn tavanı bu bütçenin örneğidir. Bir katman daha var:
OpenRouter aynı model için sağlayıcı hata verirse varsayılan olarak sıradaki sağlayıcıya geçer
(`allow_fallbacks: true`, [provider selection](https://openrouter.ai/docs/guides/routing/provider-selection)) —
yani adaptörün 3 denemesinin her biri arkada birden fazla sağlayıcı denemesi olabilir. Bütçe
hesabında bu görünmeyen katmanı da say. Yeniden deneme sınıfları, idempotency
anahtarı ve devre kesici ayrıntısı için: [`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md).

**Kaos deneyi — en ucuz ilk adım.** "Yedeğimiz var" demek, yedeğin çalıştığını göstermez. Ders,
çoğu ekibin ilk denemede yedeğin hiç sınanmadığını keşfettiğini söyler. İlk LLM'e özgü deney
küçüktür: sağlayıcıyı 5 dakika boyunca %100 429 dönüyormuş gibi davrandır; geçiş SLA içinde oluyor
mu, yük çarpılıyor mu, kullanıcı ne görüyor? Port/adaptör ayrımı bunu kolaylaştırır: hata enjekte
eden bir adaptör testte yeterlidir. Canlıda kaos için önkoşullar ise yazılı SLO, gözlem, otomatik
geri alma ve runbook'tur; biri eksikse deney gerçek bir olaya dönüşür.

```svg
<svg viewBox="0 0 660 380" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gateway'li altıgen akış ve yedek model zinciri: solda çekirdek servisler, örneğin DegerlendirmeService ve SohbetService, yalnız OgretmenAiClient giden port arayüzünü görür; port, SpringAiOgretmenClient adaptörüne bağlanır; adaptör yeniden deneme şablonu, AiHataCevirici ve ai_kullanim kaydını içerir; adaptör dış gateway OpenRouter'a gider, OpenRouter da arkadaki model sağlayıcılarına dağıtır. ArchUnit Spring AI'yı yalnız ai paketine mühürler. Altta yedek model zinciri: birincil model, başka sağlayıcıdaki eşdeğer model, küçük model, önbellekteki cevap, dürüst hata mesajı; her basamak kaliteyi erişilebilirliğe takas eder; bu depoda zincir yoktur, kırmızı ile işaretli; değerlendirme gibi işlemlerde doğru son basamak dürüst hatadır">
  <defs>
    <marker id="lu-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="lu-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Gateway bir giden port'un adaptörüdür — yedek zinciri onun arkasındadır</text>
  <text x="330" y="41" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Mavi = çekirdek · mor = port · yeşil = adaptör (bu depoda var) · kırmızı = bu depoda yok</text>

  <rect x="20" y="60" width="150" height="120" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="95" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">ÇEKİRDEK (service)</text>
  <text x="95" y="102" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">DegerlendirmeService</text>
  <text x="95" y="118" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">SohbetService</text>
  <text x="95" y="134" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">HafizaService · RaporService</text>
  <text x="95" y="160" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">sağlayıcıyı bilmez</text>

  <line x1="170" y1="120" x2="196" y2="120" stroke="#5D7789" stroke-width="1.6" marker-end="url(#lu-2a)"/>
  <rect x="198" y="88" width="104" height="64" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.8"/>
  <text x="250" y="108" text-anchor="middle" font-size="10.5" font-weight="700" fill="#4F46E5" font-family="system-ui">PORT</text>
  <text x="250" y="124" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">OgretmenAiClient</text>
  <text x="250" y="139" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">saf tipler, Spring AI yok</text>

  <line x1="302" y1="120" x2="326" y2="120" stroke="#5D7789" stroke-width="1.6" marker-end="url(#lu-2a)"/>
  <rect x="328" y="60" width="160" height="120" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="408" y="80" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ADAPTÖR (ai/)</text>
  <text x="408" y="98" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">SpringAiOgretmenClient</text>
  <text x="408" y="114" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">yeniden deneme: 3 · 30 sn</text>
  <text x="408" y="130" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">AiHataCevirici</text>
  <text x="408" y="146" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">ai_kullanim kaydı</text>
  <text x="408" y="166" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">ArchUnit: Spring AI yalnız burada</text>

  <line x1="488" y1="120" x2="512" y2="120" stroke="#5D7789" stroke-width="1.6" marker-end="url(#lu-2a)"/>
  <rect x="514" y="60" width="126" height="120" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="577" y="80" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">DIŞ GATEWAY</text>
  <text x="577" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">OpenRouter</text>
  <text x="577" y="114" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">ADR-0004</text>
  <text x="577" y="134" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">→ model sağlayıcıları</text>
  <text x="577" y="156" text-anchor="middle" font-size="8" fill="#B3402F" font-family="system-ui">tek anahtar = tek</text>
  <text x="577" y="168" text-anchor="middle" font-size="8" fill="#B3402F" font-family="system-ui">arıza alanı</text>

  <text x="330" y="212" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Yedek model zinciri — her basamak kaliteyi erişilebilirliğe takas eder</text>

  <rect x="20" y="228" width="110" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="75" y="250" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Birincil model</text>
  <text x="75" y="266" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">bu depoda: var</text>
  <line x1="130" y1="254" x2="148" y2="254" stroke="#B3402F" stroke-width="1.5" marker-end="url(#lu-2b)"/>
  <rect x="150" y="228" width="110" height="52" rx="8" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.5"/>
  <text x="205" y="246" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">Başka sağlayıcı</text>
  <text x="205" y="260" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">eşdeğer model</text>
  <text x="205" y="273" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">yok</text>
  <line x1="260" y1="254" x2="278" y2="254" stroke="#B3402F" stroke-width="1.5" marker-end="url(#lu-2b)"/>
  <rect x="280" y="228" width="110" height="52" rx="8" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.5"/>
  <text x="335" y="250" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">Küçük model</text>
  <text x="335" y="266" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">yok (HIZLI ≠ yedek)</text>
  <line x1="390" y1="254" x2="408" y2="254" stroke="#B3402F" stroke-width="1.5" marker-end="url(#lu-2b)"/>
  <rect x="410" y="228" width="110" height="52" rx="8" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.5"/>
  <text x="465" y="246" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">Önbellekteki</text>
  <text x="465" y="260" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">son iyi cevap</text>
  <text x="465" y="273" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">yalnız rapor dosyası</text>
  <line x1="520" y1="254" x2="538" y2="254" stroke="#B3402F" stroke-width="1.5" marker-end="url(#lu-2b)"/>
  <rect x="540" y="228" width="100" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="590" y="250" text-anchor="middle" font-size="9" font-weight="700" fill="#25804C" font-family="system-ui">Dürüst hata</text>
  <text x="590" y="266" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">var: Türkçe neden</text>

  <rect x="20" y="296" width="620" height="38" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="330" y="312" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">Zincir İŞLEM başına seçilir: sohbet küçük modele düşebilir;</text>
  <text x="330" y="326" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">ödev puanlamada yanlış puan, "şu an puanlayamıyoruz"dan daha kötüdür</text>

  <rect x="20" y="342" width="620" height="28" rx="8" fill="#1C3247"/>
  <text x="330" y="361" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Yeniden denemeyi TEK katmana ver — 2 × 3 × 3 = 18 çağrı, sağlayıcı zaten zorlanırken</text>
</svg>
```

## 7. Güvenli yayın — gölge, kanarya, A/B
Yeni bir model ya da prompt sürümü çevrimdışı eval'da %3 iyi çıktı; açtın. Bir gün sonra maliyet
%40 yukarıda, olumsuz geri bildirim artmış, birkaç "garip cevap" şikâyeti var. Geri almak yeniden
dağıtım gerektiriyor ve saatler sürüyor. Dersin bu senaryosundaki her adım önlenebilirdi; önleyen şey
bir **yayın merdivenidir**.

**1. Gölge modu (shadow mode).** Aday sürüm canlı isteklerin kopyasını alır; çıktısı **loglanır,
kullanıcıya dönmez.** Yakaladıkları: token sayısı (maliyet farkı), çıktı uzunluğu, gecikme, ret ve
hata oranı. **Yakalayamadığı: kalite.** Kimse gölge çıktısını okuyup "bu daha iyi" demedi; gölge bir
duman testidir (smoke test), kalite testi değil. Ve bedavaya gelmez: gölgedeki her istek ikinci bir
model çağrısıdır — gölge süresince LLM faturası kabaca ikiye katlanır. Kısa tut, örnekle.

**2. Kanarya (canary).** Gerçek trafiğin küçük bir dilimi yeni sürüme gider; dilim kapılardan geçtikçe
büyür (ör. %1 → %10 → %25 → %50 → %100). Klasik kanarya hata oranına ve gecikmeye bakar; LLM
kanaryası beş kapıya bakar:

1. **Gecikme yüzdelikleri** (p50/p95/p99 — §2'nin goodput'u),
2. **İstek başı maliyet** — "daha iyi" model istek başına üç kat pahalıysa birim ekonomiyi bozar; bu
   bir geri alma sebebidir,
3. **Hata + ret (refusal) oranı** — model "bunu yapamam" demeye başladıysa 5xx gibi sayılır,
4. **Çıktı uzunluğu dağılımı** — ortalama ve p99; yeni model sessizce kısalıyor ya da uzuyorsa hem
   maliyet hem kalite sinyalidir,
5. **Kullanıcı geri bildirimi** — olumsuz oy, şikâyet.

İki LLM'e özgü incelik: (a) Aynı girdi aynı çıktıyı vermez (örnekleme, toplu işlem boyutu,
kayan nokta sırası). Bu yüzden "kararlı" = "taban çizgisiyle aynı" değil, **"beklenen gürültü
içinde"** demektir; kapı eşiklerini gürültü tabanının üstüne koy, yoksa kanarya sürekli yanlış alarm
verir. (b) Küçük dilimde örnek azdır: %1'lik dilim gecikme için yeterli veri toplar ama kullanıcı
geri bildirimi için gürültülüdür; her basamakta yeterli örnek birikene kadar bekle.

> *Şerh:* Ders "aynı eval setinde koşudan koşuya %15'e varan doğruluk farkı" der ve kaynak göstermez;
> eşik örnekleri (p99 > 1,5× taban, maliyet > %20, ret > 2×) bir standart değil, dersin örnek
> değerleridir. Kendi gürültü tabanını aynı sürümü iki kez koşturarak ölç.

**3. A/B testi — isteğe bağlı basamak.** Yeni sürüm yalnızca "iyileştirilmiş aynı şey"se kanarya
kapıları geçince %100'e çıkılır. Davranışı, tonu ya da maliyet eğrisi belirgin biçimde farklıysa
kanaryadan sonra %50/%50 A/B koşulur. Burada iki soruyu karıştırmamak gerekir:

- **Eval** (çevrimdışı, etiketli set, rubrik/yargıç model) sorar: **"İşi yapabiliyor mu?"**
- **A/B** (çevrimiçi, canlı kullanıcı, rastgele atama) sorar: **"Kullanıcı farkı önemsiyor mu?"**

İkisi de gereklidir: eval, kullanıcıya göstermeden regresyonu yakalar; A/B, ürün etkisini sonradan
doğrular. Eval'ın nasıl kurulduğu [`evals-llm-observability.md`](evals-llm-observability.md)'de
(özellikle §6 çevrimdışı/çevrimiçi ayrımı); burada tekrar edilmez. A/B'nin iki ucuz ama hayati
kontrolü: **örnek oranı uyumsuzluğu (SRM — sample ratio mismatch)** — %50/%50 tasarlanan atama
%47/%53 geliyorsa atama bozuktur, sonuca bakma; **çoklu karşılaştırma düzeltmesi** — %95 güvenle 20
ayrı test koşarsan şans eseri yaklaşık bir "kazanan" çıkar. Kullanıcıyı varyanta **kullanıcı
kimliğinin özetiyle** (hash) ata, rastgele değil; aynı kişi her istekte aynı varyantı görsün.

> *Şerh:* Ders, belirli bir deney platformunun belirli bir tutarla satın alındığını "hatırlanacak
> rakam" olarak verir; doğrulayamadık, alıntılamıyoruz. Belirsizlik nedeniyle örneklem büyüklüğünü
> "1,3–1,5 kat artır" önerisi de kaynaksız bir pratik kuraldır.

```svg
<svg viewBox="0 0 660 360" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Güvenli yayın kapı akışı: önce çevrimdışı eval kapısı işi yapabiliyor mu sorusunu cevaplar; sonra gölge modu canlı trafiğin kopyasını alır, çıktıyı loglar ama kullanıcıya döndürmez, maliyet, uzunluk, ret ve hata değişimini yakalar ama kaliteyi yakalamaz; sonra kanarya trafiği yüzde birden yüzde ona, yirmi beşe, elliye çıkarır; her basamakta beş LLM kapısı kontrol edilir: p99 gecikme, istek başı maliyet, hata artı ret oranı, çıktı uzunluğu dağılımı, kullanıcı geri bildirimi; eşikler gürültü tabanının üstündedir; davranış belirgin farklıysa A/B testi kullanıcı önemsiyor mu sorusunu cevaplar; sonra yüzde yüz. Herhangi bir kapı ihlalinde geri alma: bayrağı çevir ve sabitlenmiş önceki model kimliğine dön, saniyeler sürer, yeniden dağıtım gerekmez">
  <defs>
    <marker id="lu-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="lu-3b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yayın merdiveni — her basamak farklı bir soruyu cevaplar</text>
  <text x="330" y="41" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Kullanıcı etkisi soldan sağa artar · kırmızı ok = herhangi bir kapı ihlalinde geri alma</text>

  <rect x="16" y="62" width="110" height="78" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="71" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#4F46E5" font-family="system-ui">EVAL</text>
  <text x="71" y="98" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">çevrimdışı set</text>
  <text x="71" y="114" text-anchor="middle" font-size="8.5" font-weight="700" fill="#1C3247" font-family="system-ui">"İşi yapabiliyor mu?"</text>
  <text x="71" y="130" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">kullanıcı etkisi: 0</text>
  <line x1="126" y1="101" x2="142" y2="101" stroke="#5D7789" stroke-width="1.6" marker-end="url(#lu-3a)"/>

  <rect x="144" y="62" width="120" height="78" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="204" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">GÖLGE</text>
  <text x="204" y="98" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">kopya trafik, logla,</text>
  <text x="204" y="112" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">kullanıcıya DÖNME</text>
  <text x="204" y="128" text-anchor="middle" font-size="8" fill="#B3402F" font-family="system-ui">kaliteyi yakalamaz · fatura ×2</text>
  <line x1="264" y1="101" x2="280" y2="101" stroke="#5D7789" stroke-width="1.6" marker-end="url(#lu-3a)"/>

  <rect x="282" y="62" width="216" height="78" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="390" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">KANARYA</text>
  <rect x="296" y="92" width="34" height="18" rx="4" fill="#25804C"/>
  <text x="313" y="105" text-anchor="middle" font-size="8.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">%1</text>
  <rect x="338" y="92" width="38" height="18" rx="4" fill="#25804C"/>
  <text x="357" y="105" text-anchor="middle" font-size="8.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">%10</text>
  <rect x="384" y="92" width="42" height="18" rx="4" fill="#25804C"/>
  <text x="405" y="105" text-anchor="middle" font-size="8.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">%25</text>
  <rect x="434" y="92" width="50" height="18" rx="4" fill="#25804C"/>
  <text x="459" y="105" text-anchor="middle" font-size="8.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">%50</text>
  <text x="390" y="128" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">her basamakta yeterli örnek birikene kadar bekle</text>
  <line x1="498" y1="101" x2="514" y2="101" stroke="#5D7789" stroke-width="1.6" marker-end="url(#lu-3a)"/>

  <rect x="516" y="62" width="128" height="78" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="580" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">A/B (isteğe bağlı)</text>
  <text x="580" y="98" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">%50/%50, kimlik özeti</text>
  <text x="580" y="114" text-anchor="middle" font-size="8.5" font-weight="700" fill="#1C3247" font-family="system-ui">"Kullanıcı önemsiyor mu?"</text>
  <text x="580" y="130" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">SRM + çoklu karşılaştırma</text>

  <line x1="390" y1="140" x2="390" y2="160" stroke="#25804C" stroke-width="1.6" marker-end="url(#lu-3a)"/>
  <rect x="150" y="162" width="480" height="104" rx="10" fill="#FFFFFF" stroke="#25804C" stroke-width="1.6"/>
  <text x="390" y="182" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Her basamakta beş LLM kapısı (eşik = gürültü tabanının ÜSTÜ)</text>
  <text x="170" y="204" font-size="9.5" fill="#1C3247" font-family="system-ui">1 · Gecikme p50/p95/p99 (goodput)</text>
  <text x="410" y="204" font-size="9.5" fill="#1C3247" font-family="system-ui">2 · İstek başı maliyet</text>
  <text x="170" y="224" font-size="9.5" fill="#1C3247" font-family="system-ui">3 · Hata + ret (refusal) oranı</text>
  <text x="410" y="224" font-size="9.5" fill="#1C3247" font-family="system-ui">4 · Çıktı uzunluğu dağılımı</text>
  <text x="170" y="244" font-size="9.5" fill="#1C3247" font-family="system-ui">5 · Kullanıcı geri bildirimi (olumsuz oy, şikâyet)</text>
  <text x="390" y="260" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">klasik kanarya yalnız 1 ve 3'e bakar — 2 ve 4 LLM'e özgüdür</text>

  <path d="M150,214 L80,214 L80,148" fill="none" stroke="#B3402F" stroke-width="1.8" marker-end="url(#lu-3b)"/>
  <text x="76" y="232" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">ihlal →</text>
  <text x="76" y="246" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">geri al</text>

  <rect x="16" y="280" width="628" height="36" rx="8" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.5"/>
  <text x="330" y="296" text-anchor="middle" font-size="10" font-weight="700" fill="#B3402F" font-family="system-ui">Geri alma = bayrağı çevir + sabitlenmiş ÖNCEKİ model kimliğine dön</text>
  <text x="330" y="310" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">saniyeler sürer; yeniden dağıtım gerektiriyorsa önce bunu düzelt, sonra yayına çık</text>

  <rect x="16" y="324" width="628" height="28" rx="8" fill="#1C3247"/>
  <text x="330" y="343" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Eval regresyonu kullanıcıdan ÖNCE yakalar; A/B ürün etkisini SONRA doğrular — biri diğerinin yerini tutmaz</text>
</svg>
```

## 8. Geri alma — bayrak + sabitlenmiş model kimliği
Merdivenin bütün değeri, bir kapı kırmızıya döndüğünde **ne kadar hızlı** geri dönebildiğine
bağlıdır. Ders iki parçayı birlikte ister:

- **Politika bayrağı (feature flag):** trafiğin yüzde kaçının hangi sürüme gittiği yapılandırmadadır;
  değiştirmek saniyeler sürer, yeniden dağıtım gerektirmez.
- **Sabitlenmiş model kimliği (pinned model id / digest):** "en güncel sürüm" anlamına gelen bir
  takma ad (alias) kullanırsan, sağlayıcı arkadaki modeli değiştirdiğinde sen hiçbir şey
  yayınlamadan davranışın değişir — ve geri alacak bir "önceki sürüm"ün yoktur. Sabitlenmiş kimlik
  hem sessiz değişimi önler hem de geri almanın hedefini verir.

**Bu projedeki bağ.** ADR-0003 model kimliklerini koda gömmez, `application.yml`'de property tutar
(`app.ai.model.guclu` / `hizli`): model değiştirmek bir satırdır. Ama bu bir **bayrak değildir**:
değiştirmek yeniden başlatma ister ve trafiği ikiye bölemez (ya hep eski ya hep yeni). İki somut
gözlem:

1. `ai_kullanim.model` sütununa **istenen** model kimliği yazılır (`calistir(...)`a giden parametre).
   2026-10-26'ya kadar sağlayıcının cevapta bildirdiği kimlik hiç kaydedilmiyordu; gateway arkadaki
   modeli değiştirse log bunu göstermezdi. Bu "ucuz iyileştirme" ADR-0016 ile yapıldı: yeni
   `yanit_model` sütunu Spring AI'nın `ChatResponseMetadata.getModel()` değerini tutar (OpenAI-uyumlu
   cevabın `model` alanı); sağlayıcı bildirmezse uydurma değer yazılmaz, `null` kalır; 100 karakteri
   aşan değer kullanım satırı düşmesin diye kırpılır. Artık `model` ile `yanit_model` farkı istenenle
   gerçekleşeni ayırır.
2. OpenRouter dokümanına göre somut kimlik (`anthropic/claude-sonnet-4.6` gibi) tekrarlanabilirlik
   için önerilen yoldur; "hep en yenisi" isteyen takma ad ayrı bir sözdizimidir (`~…-latest`). Cevabın
   `model` alanı isteği gerçekte hangi modelin karşıladığını bildirir — 1. maddedeki (artık yapılmış)
   iyileştirme bu yüzden aynı zamanda yedeğe geçişi de görünür kılar
   ([latest resolution](https://openrouter.ai/docs/guides/routing/routers/latest-resolution)).

Bir eğitim aracı için "property değiştir, yeniden başlat" yeterli olabilir. Ama merdiveni kurmadan
önce sorulacak soru şudur: **kapı kırmızıya döndüğü anda geri dönüş kaç saniye sürüyor?** Cevap
"bir dağıtım" ise önce bunu düzelt.

## 9. Bu projede özet — nerede duruyoruz
| Konu | Durum | Not |
|---|---|---|
| Gecikme ayrıştırması | Akışsız; kullanıcı E2E bekler; zaman bütçesi kodda (157 sn) | Rapor TPOT baskın |
| Goodput | Veri var — `ai_kullanim.sure_ms` (E2E, yeniden denemeler dahil; ADR-0016); TTFT/TPOT yok (akışsız) | SLO hâlâ yazılmadı: `islem` başına E2E kısıtı |
| Port/adaptör | Var, ArchUnit mühürlü | Port `ai/` paketinde (şerh §4) |
| İki sağlayıcı | Yok — tek gateway, tek anahtar | ADR-0004 bilerek kabul eder |
| Hata katmanları | Üçü de var (çevirici, toleranslı ayrıştırıcı, no-op düşüş) | 429 denenmez — bilinçli seçim gibi oku |
| Yedek zinciri | Yok — bilinçli (ADR-0016 karar 4); sağlayıcı yedeği OpenRouter'da | Yeniden açma: tek modelin süreğen kesintisi `ai_kullanim`'da görünürse |
| Yayın merdiveni | Yok — model property, yeniden başlatma | Bayrak eksik; cevaptaki model kimliği artık kayıtlı (`yanit_model`) |

## Öz-sınav (kendini test et)
1. TTFT, TPOT ve E2E'yi tanımla. TTFT hangi üç bileşenden oluşur, hangisi prompt uzunluğuyla
   büyür? E2E formülünü yaz.
2. Bu depodaki haftalık rapor 3000+ token üretir ve ~60–80 sn sürer. Raporu hızlandırmak için sistem
   prompt'unu yarıya indirmek mi, çıktı sınırını düşürmek mi daha çok kazandırır? Neden?
3. Akış (streaming) E2E'yi kısaltır mı? Bu depoda kullanıcı hangi süreyi bekler ve neden?
4. Goodput'u tanımla. Üç ayrı panoda TTFT, TPOT ve E2E yüzdeliklerinin hepsi yeşilken goodput nasıl
   düşük olabilir?
5. Bir yük testi aynı prompt'u 10.000 kez gönderip "kapasitemiz 500 eşzamanlı kullanıcı" sonucunu
   veriyor. İki ayrı önbelleğin (sağlayıcı ve uygulama) bu sonucu nasıl şişirebileceğini bu depodan
   bir örnekle açıkla.
6. Bu depoda AI gateway'in port'u, adaptörü ve dış gateway'i hangi sınıf/karar? ADR-0004'ün "servisler
   değişmez" cümlesini hangi mimari öğe mümkün kılıyor, ve hangi test bunu zorluyor?
7. "Aynı gateway üzerinden iki farklı model = iki sağlayıcı kuralını sağladık" iddiası neden
   yanlıştır?
8. Üç hata katmanını say ve her birine bu depodan bir örnek ver. Model hatasında neden "aynı
   prompt'la tekrar dene" yetmez?
9. Ön yüz 2, adaptör 3, gateway 3 deneme yapıyorsa tek tıklama en kötü kaç çağrı üretir? Çözüm
   kuralı nedir?
10. Gölge modu neyi yakalar, neyi yakalayamaz? Gölgenin maliyet tarafındaki bedeli nedir?
11. Eval ile A/B testi hangi farklı soruları cevaplar? SRM nedir, %50/%50 atama %47/%53 gelirse ne
    yaparsın?
12. **Şerh sorusu:** Ders "gateway X, rakiplerinden %859 daha hızlı" ve "kanaryada koşudan koşuya
    %15'e varan fark" diyor. Her iki rakamı bir mimari kararda nasıl kullanırsın — ya da neden
    kullanmazsın?
13. ADR-0016 `ai_kullanim`'a `sure_ms` ve `yanit_model` ekledi. `sure_ms` neden yeniden denemeleri
    **dahil** ölçer, ve bu sütunla bu depoda goodput'un hangi kısıtı hesaplanabilir, hangileri
    hesaplanamaz? `model` ile `yanit_model` farklı çıkarsa bu neyi gösterir? Uygulamada yedek model
    zinciri neden kurulmadı?

### Cevaplar
1. TTFT = kuyruk + ağ + prefill; prefill prompt uzunluğuyla büyür. TPOT = ilk token'dan sonra token
   başına süre. E2E = TTFT + TPOT × çıktı token sayısı (+ dönüş ağı).
2. Çıktı sınırını düşürmek. Rapor kısa prompt + uzun çıktı işidir; süre TPOT × N teriminde birikir,
   prompt'u kırpmak yalnız küçük TTFT terimini kısaltır.
3. Hayır; algılanan süreyi kısaltır. Bu depoda çağrı akışsızdır (`chatModel.call`), kullanıcı E2E'nin
   tamamını bekler.
4. Goodput, bütün kısıtları **aynı anda** sağlayan isteklerin payıdır. Bir kısıtı aşan istekler her
   panoda farklı küçük dilimler olabilir; her pano tek başına eşiğin içinde kalırken "en az bir
   kısıtı aşan" isteklerin toplamı büyük olur.
5. Sağlayıcının önek önbelleği tekrarlanan prompt'ta prefill'i atlar; uygulama tarafında
   `RaporService` dosya önbelleği `yenile` olmadan modeli hiç çağırmaz. Test, var olmayan bir
   kapasiteyi ölçer. Prompt'u dağılımdan örnekle, önbellekleri test planında ayrı ele al.
6. Port `OgretmenAiClient`, adaptör `SpringAiOgretmenClient` (+ `AiYapilandirma`), dış gateway
   OpenRouter (ADR-0004). Servisler yalnız portu gördüğü için; `MimariTestleri` Spring AI'yı `ai/`
   paketine mühürler.
7. İkisi aynı arıza alanını paylaşır: gateway (ya da tek anahtar) çökerse ikisi birlikte gider.
   "Bağımsız" sağlayıcı ayrı arıza alanı demektir.
8. API: 5xx/429/zaman aşımı → `AiHataCevirici` + yeniden deneme şablonu. Model: bozuk JSON →
   `JsonCevapParser` `Optional.empty`, boş cevap → `AiCiktisiBozukException`. Bağımlılık: vektör
   deposu yok → `AnlamArama` no-op, hafta etiketine düşüş. Aynı prompt çoğu zaman aynı hatayı
   üretir; hatayı modele geri vermek düzeltme şansı verir.
9. 2 × 3 × 3 = 18. Yeniden denemeyi tek katmana ver, diğerleri hatayı iletsin; her katmanda toplam
   zaman bütçesi olsun (ayrıntı: [`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md)).
10. Maliyet (token), uzunluk, gecikme, ret/hata değişimini yakalar; kaliteyi yakalamaz (kimse
    çıktıyı kullanıcı gözüyle değerlendirmez). Gölge süresince her istek ikinci bir model çağrısıdır.
11. Eval: "işi yapabiliyor mu?" (çevrimdışı, etiketli). A/B: "kullanıcı önemsiyor mu?" (çevrimiçi).
    SRM atamanın bozuk olduğunu gösterir; sonuçlara bakmadan önce atamayı düzelt.
12. İlki satıcının kendi yürüttüğü benchmark'tır; seçim gerekçesi olamaz, kendi trafiğinle ölçersin.
    İkincisi kaynaksızdır; yerine aynı sürümü iki kez koşturup kendi gürültü tabanını ölçer, kapı
    eşiğini onun üstüne koyarsın.
13. Kullanıcının beklediği gerçek gecikme, Spring AI'nın kendi denemeleri dahil `chatModel.call`'ın
    tamamıdır; tek denemenin süresi bunu küçük gösterir. Çağrı akışsız olduğu için yalnız E2E kısıtı
    hesaplanır; TTFT ve TPOT ayrı ölçülmez. Veri var, ama `islem` başına SLO hâlâ yazılmadı. Fark,
    istenenle gerçekleşenin ayrıştığını (gateway'in arkada başka bir model kimliğine yönlendirdiğini)
    gösterir. Zincir kurulmadı, çünkü OpenRouter aynı model için sağlayıcı yedeğini zaten yapar; başka
    bir modele sessizce düşmek quiz/değerlendirme JSON biçimini ve kaliteyi değiştirir. Yeniden açma
    koşulu: veri tek modelin süreğen kesintisini gösterirse.

## Kaynak ve lisans
Bu metin aşağıdaki derslerden **sentezlenmiştir** (çeviri değildir; ders kodları kopyalanmamış,
rakamlar şerhlenmiş, bu deponun vakası eklenmiştir):

- AI Engineering from Scratch — Faz 11 · 13 *Building a Production LLM Application* (üç hata katmanı,
  yedek model zinciri, akış)
- Faz 17 · 01 *Managed LLM Platforms* (iki sağlayıcı kuralı, rezerve kapasite)
- Faz 17 · 08 *Inference Metrics — TTFT, TPOT, ITL, Goodput, P99*
- Faz 17 · 19 *AI Gateways* (yedi iş, kendin işlet / yönetilen)
- Faz 17 · 20 *Shadow, Canary, Progressive Rollout* (LLM kanarya kapıları, bayrak + sabit kimlikle
  geri alma)
- Faz 17 · 21 *A/B Testing LLM Features* (eval ≠ A/B, SRM, çoklu karşılaştırma)
- Faz 17 · 22 *Load Testing LLM APIs* (prompt tekdüzeliği tuzağı, istemci darboğazı)
- Faz 17 · 24 *Chaos Engineering for LLM* (yeniden deneme çarpılması, sağlayıcı 429 deneyi,
  önkoşullar)
- Bu deponun vakası: ADR-0016 karar 4 (`ai_kullanim.sure_ms` / `yanit_model`, yedek model zinciri
  kurulmaması), `ai/SpringAiOgretmenClient.java`, `schema.sql` (2026-10-26'da okundu)

Kaynak depo: <https://github.com/rohitg00/ai-engineering-from-scratch> — **MIT Lisansı**, Copyright
(c) 2026 Rohit Ghumare. Uyarlama notu: satıcı/ürün karşılaştırmaları, araç sürüm listeleri ve
kaynaksız rakamlar ya çıkarıldı ya da metin içinde *Şerh* ile işaretlendi.

Bu depodan birincil vaka: `ai/OgretmenAiClient`, `ai/SpringAiOgretmenClient`, `ai/AiYapilandirma`,
`ai/AiHataCevirici`, `ai/JsonCevapParser`, `service/RaporService`, `MimariTestleri`;
`docs/adr/0003-model-kademeleme.md`, `docs/adr/0004-openrouter-saglayici.md`,
`docs/adr/0005-anlam-eslesmeli-baglam.md`, `docs/adr/0012-cok-kullanicili-site-profili.md`.

### Bu programdaki ilgili kaynaklar
- [`finops-ai-tokenomics.md`](finops-ai-tokenomics.md) — aynı katmanın maliyet yüzü: önbellek,
  toplu işleme, yönlendirme, maliyet damgası, harcama tavanı.
- [`evals-llm-observability.md`](evals-llm-observability.md) — eval'ın kurulumu ve gözlem katmanı;
  §7'deki "işi yapabiliyor mu?" sorusunun yöntemi.
- [`arac-cagrisi-guvenilirligi.md`](arac-cagrisi-guvenilirligi.md) — yeniden deneme sınıfları,
  idempotency, devre kesici (§6'nın ayrıntısı).
- [`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md) — harcama tavanı, kill
  switch, hız limiti.
- [`hafta09-hexagonal-clean.md`](hafta09-hexagonal-clean.md) — port/adaptör ve "portu kim tanımlar".
- [`hafta11-ddia-veri-yogun.md`](hafta11-ddia-veri-yogun.md) — yüzdelik dilimler ve SLO.
- [`mikroservis-monolit-hatalar.md`](mikroservis-monolit-hatalar.md) — genel zaman aşımı / devre kesici.
