---
baslik: "Değerlendirme-Güdümlü Geliştirme (EDD) ve LLM Gözlemlenebilirliği"
yazar: "2026 pratiği (EDD · OTel GenAI · evals engineering) — Türkçe kavram özeti"
tarih: "2026-07-15"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 8
---

### Bu metin ne anlatıyor?
Sıradan bir programda "çalışıyor mu?" sorusunun cevabı nettir: test ya geçer ya kalır. Ama yapay
zekâ aynı soruya her seferinde biraz farklı cevap verir — o zaman "çalışıyor" ne demek? Bu metin
iki alışkanlık öğretiyor: **eval** (değerlendirme), yani "iyi cevap neye benzer"i önceden yazıp
ölçmek; ve **gözlemlenebilirlik**, yani gerçek kullanıcılar sistemi kullanırken kalitenin sessizce
düşüp düşmediğini görebilmek.

## Neden bu özet
Hafta 8 (AI Engineering II) değerlendirmeyi (evaluation) kavram olarak veriyor: referanslı ölçüm,
rubrik, yargıç-model (AI-as-judge), yargıcın yanlılıkları. Bu özet o kavramı bir **disipline** ve bir
**üretim uygulamasına** bağlar. İki yeni fikir ekler:

1. **Değerlendirme-Güdümlü Geliştirme (Evaluation-Driven Development, EDD):** test-güdümlü
   geliştirmenin (TDD) dil modeli karşılığı — kodu/prompt'u yazmadan **önce** "iyi çıktının tanımını"
   yaz.
2. **LLM gözlemlenebilirliği (observability):** üretimdeki her model çağrısını, tıpkı bir mikroservis
   isteğini izler gibi izle — token, gecikme, maliyet, model ve **kalite** dahil.

Neden şimdi? Çünkü öğrencinin gerçek uygulaması (AI Öğretmen) artık üretim davranışı sergiliyor:
öğrenci gönderimini puanlayan `Degerlendirme`, quiz üreten `UretilenQuiz` (brief 7.4), atölye
prompt'unu yargıç-modele puanlatan `AtolyeDegerlendirme` (`prompt-degerlendirme.st`, brief 7.5) ve
token kaydını tutan `AiKullanim`/`KullanimService` (brief 7.6). Bunların hepsi **olasılıksal** çıktı
üretir — "çalışıyor mu?" sorusunun cevabı tek bir örnekle bilinemez. Bu özet, o soruyu ölçülebilir ve
sürdürülebilir kılan iki aracı verir.

> **Tek cümle:** Kod için testin, mimari için ArchUnit'in yaptığını LLM kalitesi için **eval** yapar;
> ve çalışan bir sisteme dair gerçeği yalnızca **gözlemlenebilirlik** söyler.

## Türkçe çalışma metni

### 1. Değerlendirme-Güdümlü Geliştirme (EDD) nedir — TDD'nin LLM karşılığı
TDD'de kural nettir: **başarısız bir test yaz, sonra onu geçiren kodu yaz.** Test "doğrunun tanımıdır";
kod ona uyar. EDD aynı tersine-çevirmeyi dil modeline uygular:

> **Prompt'u ya da özelliği yazmadan ÖNCE değerlendirmeyi (eval) yaz.**
> "İyi çıktının tanımı" önce gelir; prompt sonra ona uyar.

Somut örnek — AI Öğretmen'in quiz üretimi (`UretilenQuiz`). Klasik refleks: önce
`prompt-quiz.st`'yi yaz, birkaç quiz üret, gözle bak, "iyi görünüyor" de. EDD refleksi bunun tersidir.
Önce **eval'ı** tanımlarsın:

- Üretilen her `QuizSorusu`'nun tam **4 seçeneği** olmalı,
- `dogruIndex` 0–3 aralığında ve seçenek sayısıyla tutarlı olmalı,
- soru metni ilgili haftanın kaynak metninden bir kavrama değmeli (referanssız kontrol),
- `aciklama` boş olmamalı.

Bu kurallar, henüz iyi bir prompt yokken bile **başarısızdır** (kırmızı) — çünkü ortada üretim yoktur.
Sonra prompt'u yazar, üretir, eval'ı çalıştırır ve yeşile döndürürsün. Tıpkı TDD'nin kırmızı→yeşil
döngüsü gibi (bkz. **Diyagram 1**).

Farkı şudur: TDD'de test **deterministik** geçer/kalır; EDD'de eval bir **eşiktir** — "20 quizden en az
18'i şema-geçerli ve konu-ilgili" gibi. Olasılıksal bir sistemi olasılıksal bir kabul kriteriyle
sınarsın.

> **EDD, "gözle bakıp beğenmenin" (vibe check) yerine geçen mühendislik disiplinidir.** Gözle bakmak
> ölçeklenmez, tekrarlanamaz ve iki kişi arasında tutarsızdır; eval kaydedilir, tekrarlanır ve sayıya
> döner.

### 2. Neden gerekli — olasılıksal çıktı ve "iyi işin tanımı" kalıcı bir varlıktır
Sıradan kodda `topla(2, 2)` her zaman `4` döndürür; bir örnek yeterlidir. LLM çıktısı **olasılıksaldır**:
aynı prompt, aynı model, farklı çağrıda farklı `AtolyeDegerlendirme` puanı üretebilir. Dolayısıyla:

> **"Çalışıyor mu?" sorusu tek bir örnekle CEVAPLANAMAZ. Ancak bir örneklem üzerinde ölçülür.**

Asıl mesele değişim anlarında ortaya çıkar. Şu üç şeyden biri değiştiğinde kalite **sessizce** düşebilir:

1. **Model yükseltmesi** — OpenRouter'daki sağlayıcı modeli yeni sürüme geçirir (ADR-0004),
2. **Prompt değişikliği** — `prompt-degerlendirme.st`'de bir cümleyi "iyileştirirsin",
3. **Bağlam değişikliği** — kaynak metin (content/) güncellenir, RAG parçaları kayar.

Somut senaryo: atölye puanlamasının promptunu "daha kısa geri bildirim" için düzenledin. Kod derlenir,
`./mvnw verify` yeşildir, uygulama çalışır — **ama** yargıç-model artık aynı öğrenci çözümüne dün 80,
bugün 55 veriyor. Hiçbir test bunu yakalamaz; çünkü bu bir **kalite regresyonudur**, bir derleme
hatası değil. Eval olmadan bunu ancak öğrenci şikâyet edince fark edersin.

İşte eval'ın kalıcı değeri buradan gelir:

> **Bir eval takımı, model yükseltmeleri arasında "iyi işin tanımını" koruyan KALICI BİR VARLIKTIR —
> neredeyse bir fikri mülkiyet gibi.** Model gelir geçer; "iyi bir atölye değerlendirmesi neye benzer"
> bilgisi sende kalır.

Prompt'lar ucuzdur, modeller değişir; ama "iyi çıktı neye benzer" sorusunun **kodlanmış, çalıştırılabilir
cevabı** birikimli bir servettir. Bir ekibin en değerli AI varlığı çoğu zaman modeli değil, **eval
takımıdır.**

## Diyagram 1 — EDD döngüsü (TDD'ye paralel)
Üstte tanıdık TDD döngüsü, altta onun LLM karşılığı EDD. İkisi de "önce doğrunun tanımı" ile başlar,
bir **kapıdan** (gate) geçer; kapı kalırsa geri dönülür, geçerse ileri gidilir.

```svg
<svg viewBox="0 0 640 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="EDD döngüsü: üstte TDD (test yaz, kod yaz, çalıştır, yeşil mi kapısı, merge), altta EDD (eval yaz, prompt yaz, çalıştır, eval geçti mi kapısı, üret); kapı kalırsa uygulama adımına geri döner">
  <defs>
    <marker id="ev-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="ev-1b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
    <marker id="ev-1c" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">EDD Döngüsü — TDD'nin dil modeli karşılığı</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Önce "doğrunun tanımı" · sonra uygulama · kapı: geçerse ileri, kalırsa geri</text>

  <text x="14" y="66" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">TDD (test-güdümlü)</text>
  <rect x="14" y="74" width="104" height="46" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="66" y="100" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Test yaz</text>
  <rect x="132" y="74" width="104" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="184" y="100" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Kod yaz</text>
  <rect x="250" y="74" width="90" height="46" rx="9" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.5"/>
  <text x="295" y="100" text-anchor="middle" font-size="11" font-weight="600" fill="#5D7789" font-family="system-ui">Çalıştır</text>
  <polygon points="392,74 434,97 392,120 350,97" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="392" y="94" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">Yeşil</text>
  <text x="392" y="106" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">mi?</text>
  <rect x="450" y="74" width="104" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="502" y="100" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Merge</text>
  <line x1="118" y1="97" x2="130" y2="97" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-1a)"/>
  <line x1="236" y1="97" x2="248" y2="97" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-1a)"/>
  <line x1="340" y1="97" x2="349" y2="97" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-1a)"/>
  <line x1="434" y1="97" x2="449" y2="97" stroke="#25804C" stroke-width="1.8" marker-end="url(#ev-1c)"/>
  <text x="443" y="90" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">geç</text>
  <path d="M392,120 C392,150 200,150 184,122" fill="none" stroke="#C77A17" stroke-width="1.6" stroke-dasharray="5 4" marker-end="url(#ev-1b)"/>
  <text x="300" y="146" text-anchor="middle" font-size="9" fill="#C77A17" font-family="system-ui">kal → düzelt</text>

  <text x="14" y="196" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">EDD (değerlendirme-güdümlü)</text>
  <rect x="14" y="204" width="104" height="46" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="66" y="230" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Eval yaz</text>
  <rect x="132" y="204" width="104" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="184" y="224" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Prompt /</text>
  <text x="184" y="238" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">özellik yaz</text>
  <rect x="250" y="204" width="90" height="46" rx="9" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.5"/>
  <text x="295" y="230" text-anchor="middle" font-size="11" font-weight="600" fill="#5D7789" font-family="system-ui">Çalıştır</text>
  <polygon points="392,204 434,227 392,250 350,227" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="392" y="224" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">Eval</text>
  <text x="392" y="236" text-anchor="middle" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">geçti?</text>
  <rect x="450" y="204" width="104" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="502" y="230" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Üret</text>
  <line x1="118" y1="227" x2="130" y2="227" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-1a)"/>
  <line x1="236" y1="227" x2="248" y2="227" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-1a)"/>
  <line x1="340" y1="227" x2="349" y2="227" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-1a)"/>
  <line x1="434" y1="227" x2="449" y2="227" stroke="#25804C" stroke-width="1.8" marker-end="url(#ev-1c)"/>
  <text x="443" y="220" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">geç</text>
  <path d="M392,250 C392,280 200,280 184,252" fill="none" stroke="#C77A17" stroke-width="1.6" stroke-dasharray="5 4" marker-end="url(#ev-1b)"/>
  <text x="300" y="276" text-anchor="middle" font-size="9" fill="#C77A17" font-family="system-ui">kal → prompt'u düzelt</text>

  <text x="320" y="312" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kırmızı→yeşil yerine "eşiğin altı → eşiğin üstü"; kapı CI'da mühürlenir (bkz. §5).</text>
</svg>
```

### 3. Eval türleri — hangi soruya hangi ölçüm
Tek bir "eval" yoktur; ölçülen şeye göre farklı türler kullanılır. AI Öğretmen'in her özelliği farklı
bir türe oturur.

Önce bu bölümün her yerinde geçecek olan tek bir kavramı oturtalım:

> **Altın küme (golden set / golden dataset):** İnsanın elden geçirip "doğru cevabı budur" ya da
> "iyi cevap böyle olur" diye **onayladığı**, sabit ve versiyonlanan örnek koleksiyonu. Eval'ın
> ölçüm cetvelidir: model değişir, prompt değişir — **altın küme sabit kalır**, ölçümlerin
> kıyaslanabilir olmasının tek sebebi budur.
>
> Üç kuralı vardır: (1) **temsili** olmalı — kolay örneklerden değil, gerçek trafiğin dağılımından
> seçilmeli, zor ve sınır vakalar dahil; (2) **küçük ama titiz** olmalı — 5000 gelişigüzel örnek
> yerine 50 iyi seçilmiş örnek daha iyi ölçer ve daha az token yakar; (3) **kirlenmemeli** — modeli
> ayarlarken sürekli ona bakıp prompt'u ona uydurursan (aşırı uydurma, overfitting) küme artık
> ölçmez, sadece onaylar. Bu yüzden bir kısmını **hiç açmadığın** ayrı bir kümede tutmak iyi
> pratiktir.

- **Referanslı eval (reference-based):** Çıktı, önceden bilinen **beklenen cevapla** kıyaslanır. En
  kesin türdür ama "tek doğru cevabı olan" görevler ister. Örnek: `UretilenQuiz`'in JSON şeması —
  `dogruIndex` gerçekten doğru seçeneği mi gösteriyor? Altın kümedeki quizlerle kıyaslanır. Kod
  testine en yakın türdür (kesin geç/kal).
- **Referanssız / rubrik eval (reference-free, rubric):** Tek doğru cevap yoktur; çıktı bir
  **rubriğe** göre puanlanır. **Rubrik**, "neye kaç puan verileceğini" önceden yazan kriter
  listesidir — öğretmenin sınav kâğıdı okurken kullandığı puanlama anahtarının aynısı. Örnek:
  öğrencinin atölye çözümü için "SOLID'e değindi mi? somut örnek verdi mi? Türkçe ve anlaşılır mı?"
  — her kriter bir puan. `AtolyeDegerlendirme`'nin `geriBildirim` ve `iyilestirme` alanları tam da
  bir rubriğin çıktısıdır.
- **Yargıç-model / AI-as-judge:** Bir model, **başka bir modelin** (ya da öğrencinin) çıktısını
  rubriğe göre puanlar. **Bu, programda zaten var:** `prompt-degerlendirme.st` çağrılan LLM'i bir
  yargıç yapar; sonuç `AtolyeDegerlendirme(puan, geriBildirim, iyilestirme)` olur. Ucuzdur ve
  ölçeklenir — ama **yargıcın kendi yanlılıkları vardır** (Hafta 8'in uyarısı): uzun cevabı iyi sanma
  (**uzunluk yanlılığı**), kendi ürettiğine yüksek puan verme (**kendini kayırma**, self-preference),
  iki cevaptan ilk gördüğünü seçme (**konum yanlılığı**), ve kendinden emin/cilalı üsluba fazladan
  puan verme (**üslup yanlılığı**). Üçü de ucuz önlemlerle azaltılabilir: konum yanlılığı için aynı
  çiftin **sırasını değiştirip iki kez** sor ve sonuç değişiyorsa güvenme; kendini kayırma için
  yargıcı **üreten modelden farklı bir model** seç; uzunluk/üslup için rubriğe "uzunluk puanı
  etkilemez" maddesini açıkça yaz. Yine de bunlar azaltır, **sıfırlamaz** — bu yüzden yargıç-modelin
  kendisi de zaman zaman **insan puanıyla kalibre edilmelidir.**
- **İnsan değerlendirme:** En pahalı, en yavaş, ama **altın standart**. Yargıç-modeli doğrulamak,
  rubriği kalibre etmek ve zor/sınır vakaları çözmek için küçük bir örneklemde kullanılır. Amaç insanı
  ölçeğe koymak değil; **otomatik eval'ları insana göre ayarlamaktır.**

Bu türler bir dik eksende daha kesişir — ölçümün **nerede** çalıştığı:

> **Offline eval** = geliştirmede, **sabit bir veri seti** üzerinde (tekrarlanabilir, deploy öncesi).
> **Online eval** = üretimde, **canlı trafik** üzerinde (değişken, sürekli, drift'i yakalar).

Aynı rubrik hem offline (CI'da, sabit 50 örnek) hem online (üretimde, örneklenen gerçek gönderimler)
koşabilir. İkisi tek bir şey değildir; birbirini tamamlar (bkz. **Diyagram 3**).

### 4. CI kapısı (gate) — eval'ı sürekli tümleştirmeye koymak
EDD'nin döngüsü ancak **otomatikleşince** disiplin olur. Aksi hâlde herkes "eval'ı sonra çalıştırırım"
der ve çalıştırmaz. Çözüm: eval'ı **sürekli tümleştirmeye (CI)** bir kapı olarak koymak.

> **Prompt değişikliği eval skorunu eşiğin altına düşürürse, merge ENGELLENİR.**
> ArchUnit'in mimari için yaptığını (`MimariTestleri` yeşil değilse merge yok), eval kalite için yapar.

Bu projede paralellik birebirdir. `./mvnw verify` şu an birim testlerini ve ArchUnit kurallarını
mühürlüyor. Aynı boru hattına bir **eval adımı** eklenir: sabit bir değerlendirme veri seti üzerinde
`AtolyeDegerlendirme` puanlamasını çalıştır; ortalama puan ya da "insan puanıyla uyum" belli bir eşiğin
altına düşerse yapı **kırmızı** olur.

Pratik incelikler:

- **Eşik, ortalama değil dağılım olmalı.** "Ortalama 75" iyi görünüp, örneklerin %20'sinde felaket
  olabilir. Eşiği "en kötü %10 da kabul edilebilir sınırda" gibi kur.
- **Belirlenimsizliği (non-determinism) yönet.** Model olasılıksal olduğu için aynı eval iki koşuda
  hafif farklı skor verir. Ya `temperature`'ı (sıcaklık — modelin ne kadar "serbest/yaratıcı" seçim
  yapacağını ayarlayan sayı; 0'a yakın = en olası kelimeyi seç, daha tekrarlanabilir) 0'a yakın
  tut, ya birden çok koşunun ortalamasını al, ya da
  eşiğe bir **tolerans payı** bırak. Yoksa CI kapısı rastgele kırmızı yanıp güveni öldürür (flaky gate).
- **Maliyeti gözet.** Her CI koşusu gerçek LLM çağrısı demektir — token yakar. Küçük ama **temsili** bir
  veri seti tut; tam takımı yalnızca gecelik (nightly) koştur.

> **Kapı olmayan eval, dilek listesidir.** ArchUnit gibi, eval de ancak "geçmeden merge edilemez"
> olduğunda davranışı biçimlendirir.

### 5. LLM gözlemlenebilirliği (observability) — üretimdeki gerçeği görmek
Offline eval "deploy öncesi iyi miydi?" sorusunu yanıtlar. Ama üretimde model gerçek, öngörülemeyen
girdilerle karşılaşır. Orada tek gerçek kaynağı **gözlemlenebilirliktir**: her LLM çağrısını, tıpkı bir
mikroservis isteğini izler gibi izlemek.

Önce iki kelimeyi oturtalım, çünkü bu bölümün tamamı bu ikisi üzerine kurulu:

> **Span (aralık):** Tek bir işin başlangıcı ile bitişi arasındaki kayıt — "şu an, şu kadar sürdü,
> şu bilgilerle, başarılı/başarısız". Bir LLM çağrısı bir span'dir.
> **Trace (iz):** Aynı isteğe ait span'lerin **iç içe geçmiş bütünü** — "kullanıcı bir kez düğmeye
> bastı, arka planda şu 5 iş sırayla oldu". Span bir satır, trace o satırların oluşturduğu ağaçtır.
> **Öznitelik (attribute):** Bir span'e iliştirilen `ad = değer` çifti (ör. `model = ...`).

Standart araç **OpenTelemetry** (kısaca **OTel**: uygulamaların ürettiği iz/ölçüm verisinin biçimini
tanımlayan, satıcıdan bağımsız açık standart) ve onun **GenAI semantic conventions**'ıdır (yapay
zekâ çağrılarında öznitelikleri hangi adla yazacağını söyleyen ortak adlandırma kuralı). Herkesin
aynı adı kullanmasının değeri şudur: izleme aracını değiştirdiğinde panolarını baştan yazmazsın.
Her model çağrısı bir **span** üretir ve span'e standart öznitelikler yazılır (bkz. **Diyagram 2**):

- **girdi / çıktı** (`gen_ai.prompt` / `gen_ai.completion`) — ne soruldu, ne döndü,
- **token** (`gen_ai.usage.input_tokens` / `output_tokens`) — tam da `AiKullanim`'in tuttuğu alanlar,
- **gecikme** (latency) — çağrı kaç ms sürdü,
- **maliyet** (cost) — token × birim fiyat,
- **model** (`gen_ai.request.model`) — hangi model/sürüm (model yükseltmesini izlemenin anahtarı),
- **eval özniteliği** — ve kritik hamle: **eval sonucunu span'in bir özniteliği yap**
  (`eval.puan = 55`). Böylece kalite, tek bir üretim çağrısına kadar izlenebilir olur.

> ⚠️ **Eskime şerhi — öznitelik adları hâlâ oturuyor.** OTel'in GenAI kuralları henüz **deneysel
> (experimental)** statüdedir ve sürümler arasında ad değiştirir: token alanları eskiden
> `prompt_tokens`/`completion_tokens` iken `gen_ai.usage.input_tokens`/`output_tokens` oldu; prompt
> ve cevap **metninin** span özniteliği olarak taşınması ise ayrı olay/log kayıtlarına taşındı
> (metin uzun ve hassastır, öznitelikte durması hem şişirir hem sızdırır). Yani buradaki **fikir
> kalıcıdır** — "her çağrı bir span, token/gecikme/maliyet/model/kalite onun öznitelikleri" — ama
> **tam alan adlarını kod yazmadan önce güncel OTel semconv sürümünden doğrula.** Ve hassas veri
> için karar baştan verilmelidir: öğrenci metnini olduğu gibi ize yazmak, `llm-uygulama-guvenligi`
> kaynağındaki "veri minimizasyonu" ilkesiyle çelişir; çoğu ekip metni ya hiç yazmaz ya örnekleyerek
> ve maskeleyerek yazar.

**Ajan/araç izleri (traces).** Bir işlem birden çok adımdan oluşuyorsa (önce kaynak getir, sonra
LLM'e sor, sonra JSON'u ayrıştır), bu adımlar iç içe span'ler olarak bir **trace**'te toplanır. Böylece
"atölye puanlaması neden 9 saniye sürdü?" sorusunun cevabı — LLM mi yavaştı, ayrıştırma mı — izde
görünür.

**Drift (kayma) izleme.** Asıl kazanç budur: eval'ı span özniteliği yaptığın için, **üretimdeki kaliteyi
zaman içinde çizebilirsin.** Ortalama `eval.puan` haftalar içinde sessizce düşüyorsa — sağlayıcı modeli
değiştirdiği, girdiler kaydığı ya da bir prompt "iyileştirmesi" geri teptiği için — bunu **öğrenci
şikâyet etmeden** görürsün. Offline eval bir fotoğraftır; drift izleme bir **filmdir.**

> **Gözlemlenebilirlik, "çalışıyor sanıyorum"u "çalıştığını görüyorum"a çevirir.** Loglar ne olduğunu
> anlatır; izler + eval öznitelikleri **ne kadar iyi** olduğunu anlatır.

## Diyagram 2 — LLM gözlemlenebilirliği katmanı
Her çağrı bir OTel span üretir; span'e girdi/çıktı, token, gecikme, maliyet, model ve **eval puanı**
yazılır. Span'ler bir ize (trace) dizilir; eval özniteliği zaman içinde drift izlemeyi besler.

```svg
<svg viewBox="0 0 640 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="LLM gözlemlenebilirliği: LLM çağrısı bir OpenTelemetry GenAI span üretir; span'de girdi çıktı, token, gecikme, maliyet, model ve eval puanı öznitelikleri; span'ler bir ize dizilir ve eval özniteliği drift izlemeyi besler">
  <defs>
    <marker id="ev-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="ev-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#4F46E5"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">LLM Gözlemlenebilirliği — OpenTelemetry GenAI</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Her çağrı bir span; eval puanı da span özniteliğidir → drift izlenebilir</text>

  <rect x="14" y="95" width="110" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="69" y="122" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">LLM çağrısı</text>
  <text x="69" y="139" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(ai/ paketi)</text>

  <rect x="150" y="70" width="228" height="196" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.8"/>
  <text x="264" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">OTel GenAI span</text>
  <text x="166" y="116" font-size="10.5" fill="#1C3247" font-family="system-ui">girdi / çıktı (prompt · completion)</text>
  <text x="166" y="140" font-size="10.5" fill="#1C3247" font-family="system-ui">token (input · output)</text>
  <text x="166" y="164" font-size="10.5" fill="#1C3247" font-family="system-ui">gecikme (latency, ms)</text>
  <text x="166" y="188" font-size="10.5" fill="#1C3247" font-family="system-ui">maliyet (token × fiyat)</text>
  <text x="166" y="212" font-size="10.5" fill="#1C3247" font-family="system-ui">model (istek modeli / sürüm)</text>
  <rect x="160" y="224" width="208" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="264" y="243" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">eval.puan  ← kalite özniteliği</text>
  <line x1="124" y1="125" x2="148" y2="125" stroke="#5D7789" stroke-width="1.8" marker-end="url(#ev-2a)"/>

  <rect x="404" y="70" width="222" height="96" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="515" y="90" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">Trace (iz) — iç içe span'ler</text>
  <rect x="418" y="100" width="194" height="16" rx="4" fill="#FFFFFF" stroke="#C77A17" stroke-width="1"/>
  <text x="424" y="112" font-size="9" fill="#5D7789" font-family="system-ui">ajan span</text>
  <rect x="436" y="120" width="158" height="16" rx="4" fill="#FFFFFF" stroke="#C77A17" stroke-width="1"/>
  <text x="442" y="132" font-size="9" fill="#5D7789" font-family="system-ui">araç span (kaynak getir)</text>
  <rect x="454" y="140" width="120" height="16" rx="4" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1"/>
  <text x="460" y="152" font-size="9" fill="#4F46E5" font-family="system-ui">LLM span</text>
  <line x1="378" y1="118" x2="402" y2="112" stroke="#4F46E5" stroke-width="1.8" marker-end="url(#ev-2b)"/>

  <rect x="404" y="188" width="222" height="96" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="515" y="208" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Drift izleme (zaman içinde)</text>
  <polyline points="422,232 452,236 482,240 512,250 542,258 572,266 600,272" fill="none" stroke="#25804C" stroke-width="2"/>
  <line x1="422" y1="248" x2="600" y2="248" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 3"/>
  <text x="606" y="251" font-size="8.5" fill="#5D7789" font-family="system-ui">eşik</text>
  <text x="515" y="278" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ort. eval.puan düşüyor → uyar</text>
  <line x1="515" y1="166" x2="515" y2="186" stroke="#25804C" stroke-width="1.8" marker-end="url(#ev-2a)"/>

  <text x="320" y="308" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Loglar "ne oldu"yu; izler + eval öznitelikleri "ne kadar iyi"yi anlatır.</text>
  <text x="320" y="326" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Token alanları AI Öğretmen'de zaten var: AiKullanim(inputTokens, outputTokens).</text>
</svg>
```

### 6. Offline vs online — sabit veri seti vs canlı trafik
İki eval ortamı farklı soruları yanıtlar; ikisi de gereklidir.

**Offline (geliştirmede).** Sabit, elle seçilmiş bir **veri seti** üzerinde koşar: örneğin 50 gerçek
öğrenci gönderimi ve bunların insan-onaylı "ideal" değerlendirmeleri. Özellikleri: **tekrarlanabilir**
(aynı girdi, aynı sette), **deploy öncesi** çalışır, CI kapısını besler (§4). Zayıflığı: veri seti
donuktur — gerçek dünyanın yarın getireceği yeni girdi türlerini bilmez.

**Online (üretimde).** **Canlı trafik** üzerinde koşar: gerçek öğrencilerin gerçek gönderimleri.
Hepsini puanlamak pahalı olduğu için trafik **örneklenir** (ör. her 20 gönderimden biri yargıç-modele
gider) ya da düşük-güven sinyalleri (kısa cevap, aşırı uzun cevap, JSON düzelme çabası) hedeflenir.
Özellikleri: **değişken**, **sürekli**, ve **drift'i yakalayan tek yer** burasıdır. Zayıflığı:
tekrarlanamaz (trafik hep değişir) ve altın küme yoktur — çoğu zaman yargıç-model veya dolaylı
sinyaller (öğrenci "bu değerlendirme yanlış" dedi mi?) kullanılır.

> **Offline eval "değişikliğim iyi miydi?" sorusuna, online eval "üretimde hâlâ iyi mi?" sorusuna
> cevap verir.** Biri deploy'u geçer/kalır kılar; diğeri deploy'dan sonra sessiz çürümeyi (drift)
> yakalar. Sadece offline'ı olan ekip "yeşildi ama kullanıcı şikâyetçi" tuzağına düşer.

## Diyagram 3 — Offline vs online eval
Solda geliştirmede, sabit veri seti üzerinde tekrarlanabilir eval → CI kapısı. Sağda üretimde, canlı
trafik üzerinde örnekleme + izleme → drift alarmı.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Offline eval geliştirmede sabit veri seti üzerinde tekrarlanabilir olarak koşar ve CI kapısını besler; online eval üretimde canlı trafik üzerinde örnekleme ve izleme ile koşar ve drift alarmı üretir">
  <defs>
    <marker id="ev-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="ev-3b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Offline vs Online Eval</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">"Değişikliğim iyi miydi?" · "Üretimde hâlâ iyi mi?"</text>

  <line x1="320" y1="60" x2="320" y2="288" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>

  <text x="160" y="78" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">OFFLINE — geliştirmede</text>
  <text x="160" y="94" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sabit · tekrarlanabilir · deploy öncesi</text>
  <rect x="40" y="106" width="240" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="160" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Sabit veri seti</text>
  <text x="160" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">50 gönderim + insan-onaylı ideal</text>
  <rect x="40" y="176" width="240" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="160" y="196" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Eval çalıştır (rubrik + referans)</text>
  <text x="160" y="212" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">aynı sette hep aynı ölçüm</text>
  <rect x="40" y="246" width="240" height="42" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="160" y="272" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Skor → CI kapısı (geç / kal)</text>
  <line x1="160" y1="152" x2="160" y2="174" stroke="#186399" stroke-width="1.8" marker-end="url(#ev-3a)"/>
  <line x1="160" y1="222" x2="160" y2="244" stroke="#C77A17" stroke-width="1.8" marker-end="url(#ev-3b)"/>

  <text x="480" y="78" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">ONLINE — üretimde</text>
  <text x="480" y="94" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">değişken · sürekli · drift'i yakalar</text>
  <rect x="360" y="106" width="240" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="480" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Canlı trafik</text>
  <text x="480" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gerçek öğrenci gönderimleri</text>
  <rect x="360" y="176" width="240" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="196" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Örnekle + izle (yargıç / sinyal)</text>
  <text x="480" y="212" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">her N'de bir · düşük-güven hedefle</text>
  <rect x="360" y="246" width="240" height="42" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="480" y="272" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Drift alarmı (kalite düşüyor)</text>
  <line x1="480" y1="152" x2="480" y2="174" stroke="#186399" stroke-width="1.8" marker-end="url(#ev-3a)"/>
  <line x1="480" y1="222" x2="480" y2="244" stroke="#C77A17" stroke-width="1.8" marker-end="url(#ev-3b)"/>
</svg>
```

### 7. AI ekonomisi köprüsü — "başarılı çıktı başına maliyet"
AI Öğretmen'de token takibi zaten var: `AiKullanim(islem, model, inputTokens, outputTokens)` her çağrıyı
kaydeder, `KullanimService` toplar, `GET /api/kullanim` gösterir (brief 7.6). Ama bu tek başına yanıltıcı
bir metrik verir. Klasik refleks **token başına maliyeti** (cost-per-token) düşürmektir — daha ucuz
modele geç, prompt'u kısalt. Sorun şu:

> **Ucuz ama YANLIŞ çıktı, pahalıdan daha maliyetlidir.** Token başına maliyeti yarıya indirdin ama
> atölye değerlendirmelerinin yarısı çöpse, gerçek maliyet **arttı** — çünkü çöp değerlendirmeyi
> düzeltmek, yeniden üretmek ve öğrenci güvenini onarmak pahalıdır.

Doğru metrik ikisini **birleştirir**:

> **Başarılı çıktı başına maliyet (cost-per-successful-output) = toplam token maliyeti ÷ eval'ı geçen
> çıktı sayısı.**

Yani `KullanimService`'in maliyet toplamı ile eval'ın "kaç çıktı eşiği geçti" sayısını **aynı gösterge
panosunda** birleştirirsin. Örnek: A modeli çağrı başına ucuz ama %60'ı eval'ı geçiyor; B modeli iki kat
pahalı ama %95'i geçiyor. Başarılı çıktı başına B daha ucuz olabilir. Bu kararı **ancak eval + maliyet
birlikte** verdirir; token grafiği tek başına A'yı seçtirir ve yanıltır.

> **Köprü şudur:** maliyet takibi (`KullanimService`) + kalite takibi (eval) = tek disiplin. Biri
> olmadan diğeri kör; ikisi birlikte "para başına gerçek değer"i ölçer.

### 8. Yeni meslek — "evals engineering"
Bu bir kişisel alışkanlık değil, doğmakta olan bir **meslek**. 2023'te böyle bir rol yoktu; 2026'da
üretim yapan AI ekiplerinde **eval mühendisi (evals engineer)** var: işi model eğitmek değil, "iyi
çıktının tanımını" kodlayan eval takımlarını inşa etmek, sürdürmek, yargıç-modelleri insan puanıyla
kalibre etmek ve drift'i izlemek.

Neden ayrı bir meslek hâline geldi? Çünkü bu iş, sıradan test yazmaktan farklı beceriler ister:
olasılıksal sistemleri örneklem üzerinden düşünmek, rubrik tasarlamak, yargıç yanlılıklarını fark edip
düzeltmek, istatistiksel eşik/tolerans kurmak. Tıpkı test mühendisliği ya da SRE'nin (site reliability
engineering) ayrı birer disipline dönüşmesi gibi.

Öğrenci için çıkarım nettir: AI Öğretmen'e eklenen her AI özelliği (değerlendirme, quiz, atölye
puanlama), bir prompt'tan **çok daha fazlasını** gerektirir — o özelliğin "iyi"sini tanımlayan bir eval,
onu mühürleyen bir CI kapısı ve onu üretimde izleyen bir gözlemlenebilirlik katmanı. Prompt yazmak
kolaydır; **prompt'un iyi kaldığını kanıtlamak** mühendisliktir.

> **EDD + gözlemlenebilirlik, "LLM ekledim" ile "üretim-kalite AI sistemi kurdum" arasındaki farktır.**

### 9. Dokuz arıza modu — pratiğin haritası
Eval'lar boşlukta yaşamaz; her biri belirli bir **arıza modunu** yakalamak için vardır. Aşağıdaki
tablo, AI'la üretim yapan herkesin eninde sonunda yaşadığı dokuz modu, bu programda **nerede
çalıştığına** bağlar — "bu kavramı nerede pratik ederim?" sorusunun haritası:

| # | Arıza modu | Bu programda nerede çalışılır |
|---|---|---|
| 1 | Çıktı kararlılığı (aynı girdi ≠ aynı çıktı) | Bu kaynak (EDD, §2) + **hızlı lab:** aynı promptu aynı veriyle 3 kez çalıştır, sapmayı gör — sonra format + örnek sabitle, yeniden ölç |
| 2 | Halüsinasyon | `model-ve-caba` (doğası) · `llm-uygulama-guvenligi` · **kural:** doğrulayamadığını "doğrulanamadı" diye işaretlet |
| 3 | Kaynak tazeliği/güvenilirliği | `ai-verimlilik-olcumu` (kanıt okuma) · web araması + "kaynak adı + tarih yaz" kuralı |
| 4 | Zincirde birikimli hata | Atölye `zincirleme` — her adımdan sonra kontrol; orijinali referans tut |
| 5 | Oturumlar-arası hafıza | Atölye `bellek` + uygulamanın kendi `data/hafiza` sistemi (canlı örnek) |
| 6 | Prompt hassasiyeti / format bozulması | Atölye `iskele`, `sema`, `tam-iskelet` + bu kaynağın CI kapısı (§4) |
| 7 | Alan bilgisi vs anahtar-kelime eşleştirme | Atölye `rubrik verme` (muhakeme) — operatör sezgisini açık kurala dök; modelin hâlâ kaçırdığını **belgele** |
| 8 | Deterministik hesap | Atölye `hesap-koda` — sayı/tarih/para asla kafadan, daima koda |
| 9 | Önerilen ≠ yapılabilir otomasyon | Atölye `ajan-gerekmez` + **hızlı lab:** her otomasyon önerisini "hangi araç/erişim gerekir, bizde var mı?" sorusuyla sına; açığı belgele |

Tablonun asıl mesajı şu: bu dokuz mod ayrı ayrı "AI tuhaflığı" değil — **eval takımının var olma
nedenleri listesi**dir. Üretimde bir AI özelliği tasarlarken bu tabloyu kontrol listesi gibi gez:
her satır için "benim sistemimde bu modu kim yakalar?" sorusunun bir cevabı olmalı.

## Öz-sınav (kendini test et)
1. EDD ile TDD'nin ortak çekirdek fikri nedir, ve EDD'de kabul kriterinin TDD'dekinden farkı neden
   "eşik" olmak zorunda? Quiz üretimi (`UretilenQuiz`) örneğinde bir eval'ı prompt'tan önce nasıl
   yazarsın?
2. "LLM çıktısı olasılıksaldır, bir örnekle 'çalışıyor mu' bilinemez" cümlesi pratikte ne demek?
   `AtolyeDegerlendirme` puanının aynı çözüme iki çağrıda farklı çıkması EDD tasarımını nasıl etkiler?
3. Bir eval takımının neden "model yükseltmeleri arasında korunan kalıcı bir varlık (fikri mülkiyet
   gibi)" olduğunu açıkla. Sağlayıcı modeli sessizce yükseltirse (ADR-0004) bunu ne yakalar, ne yakalamaz?
4. Referanslı, rubrik ve yargıç-model (AI-as-judge) eval'larını AI Öğretmen'in hangi üç özelliğine
   eşlersin? `prompt-degerlendirme.st` bunlardan hangisini gerçekliyor?
5. Yargıç-modelin dört tipik yanlılığını say ve her birine bir azaltma yöntemi eşle. Bunlar neden
   yanlılığı **sıfırlamaz** — ve bir yargıç-modeli neden ve nasıl "insan puanıyla kalibre edersin",
   bu kalibrasyon offline mı online mı yapılır?
6. **Altın küme (golden set)** nedir ve neden "sabit kalması" ölçümün ön koşuludur? Üç kuralını
   (temsili · küçük ama titiz · kirlenmemiş) say. 5000 rastgele örnek yerine 50 seçilmiş örneğin
   neden daha iyi ölçtüğünü ve prompt'u sürekli altın kümeye göre ayarlamanın (overfitting) neyi
   bozduğunu açıkla.
7. Eval'ı CI kapısına koymanın ArchUnit mührüyle paralelliğini kur. "Flaky gate" (rastgele kırmızı
   yanan kapı) neden LLM eval'larında özellikle bir risktir ve üç önleme yolu nedir?
8. **Span** ile **trace** arasındaki farkı bir cümleyle kur. Bir OTel GenAI span'inde hangi
   öznitelikler bulunur? "Eval sonucunu span özniteliği yapmak" neyi mümkün kılar — ve bu,
   `AiKullanim`'in hâlihazırda tuttuğu alanlarla nerede örtüşür? Ayrıca: öznitelik **adlarına**
   neden ezberden güvenmemelisin, ve prompt metnini olduğu gibi ize yazmak hangi ilkeyle çelişir?
9. Drift (kayma) nedir, ve neden yalnızca online (üretim) izlemeyle yakalanır? "Yeşil CI ama şikâyetçi
   kullanıcı" durumu offline eval'ın hangi zayıflığını gösterir?
10. "Token başına maliyet" ile "başarılı çıktı başına maliyet" farkını bir sayısal örnekle anlat
    (ucuz ama %60 geçen model vs pahalı ama %95 geçen model). `KullanimService` verisi tek başına
    neden yanıltır?
11. "Evals engineering" 2023'te yokken 2026'da neden ayrı bir meslek oldu? AI Öğretmen'e yeni bir AI
    özelliği eklerken, bir prompt yazmanın ötesinde hangi üç yapıyı (eval, kapı, gözlemlenebilirlik)
    kurman gerekir?
