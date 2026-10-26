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
**üretim uygulamasına** bağlar. Üç fikir ekler:

1. **Değerlendirme-Güdümlü Geliştirme (Evaluation-Driven Development, EDD):** test-güdümlü
   geliştirmenin (TDD) dil modeli karşılığı — kodu/prompt'u yazmadan **önce** "iyi çıktının tanımını"
   yaz.
2. **LLM gözlemlenebilirliği (observability):** üretimdeki her model çağrısını, tıpkı bir mikroservis
   isteğini izler gibi izle — token, gecikme, maliyet, model ve **kalite** dahil.
3. **Ölçümü ölçmek (§10):** RAG'da aramayı (recall@k) üretimden (sadakat) ayrı ölç; küçük altın
   sette farkı eşleştirilmiş bootstrap ile sına (aralık sıfırı içeriyorsa beraberlik); koruma
   filtresinde aşırı VE eksik reddi birlikte ölç; dalkavukluğu çerçeveleme testiyle, puanlamadaki
   yanlılığı isim değiştirme (değişmezlik) testiyle yakala.

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

### 10. Ölçümü ölçmek — beş ek disiplin (AIEFS ekleri)
Buraya kadar "eval yaz, kapıya koy, üretimde izle" dedik. Bu bölüm, o eval'ın **yanlış şeyi
ölçmemesi** için beş ek kural verir. Kaynağı *AI Engineering from Scratch* dersleridir (19-65/66/68,
19-74, 19-83/84, 18-04, 18-20/21, 11-10, 14-30 — bkz. *Kaynak ve lisans*); hepsi bizim projeye
uyarlanmıştır. Ortak fikir şudur: **tek bir sayı çoğu zaman iki farklı arızayı birbirine katar** —
arama ile üretimi, gürültü ile gerçek farkı, aşırı ret ile eksik reddi, bilgi ile dalkavukluğu,
içerik ile kimliği. İyi eval onları ayırır.

#### 10.1 RAG'ı aşama aşama teşhis et — arama mı kaçırdı, üretim mi uydurdu?
Bir RAG cevabı yanlışsa en az dört şüpheli vardır: parçalayıcı (chunker) cevabı ikiye bölmüş
olabilir; arayıcı (retriever) doğru parçayı ilk K'ya hiç getirmemiş olabilir; yeniden sıralayıcı
(reranker) doğru parçayı aşağı itmiş olabilir; ya da üretici model doğru parça önündeyken yine de
uydurmuş olabilir. **Cevaba bakarak hangisi olduğunu bilemezsin.** O yüzden iki ayrı ölçüm
katmanı kurulur:

- **Arama ölçümleri** (altın kümede her soru için "doğru parçalar şunlar" etiketi gerekir — buna
  *qrels* denir): **recall@k** — doğru parçaların kaçı ilk k sonuçta? **MRR (ortalama karşılıklı
  sıra / mean reciprocal rank)** — ilk doğru parça kaçıncı sırada? (1. sıra = 1, 2. sıra = 0,5,
  10. sıra = 0,1; ortalaması alınır.)
- **Üretim ölçümleri:** **sadakat (faithfulness)** — cevaptaki her iddia getirilen bağlamda
  dayanağı olan bir iddia mı? **Cevap ilgisi (answer relevance)** — cevap gerçekten soruyu mu
  yanıtlıyor? İkisi genelde yargıç-modelle ölçülür (§3'teki yanlılık uyarıları burada da geçerli).

Teşhis tablosu, okuması basit ama değeri büyük:

| Belirti | Muhtemel sebep | Ne düzeltilir |
|---|---|---|
| Düşük recall@k | Parça bölünmüş ya da arayıcı bulamıyor | Parçalama, arama yöntemi (ör. melez arama) |
| Recall iyi, MRR düşük | Doğru parça ilk K'da ama üstte değil | Yeniden sıralama (reranker) |
| MRR iyi, sadakat düşük | Doğru bağlam var, model yine uyduruyor | Üretim prompt'u: "kaynak göster ya da *bilmiyorum* de" |
| Sadakat iyi, ilgi düşük | Cevap dayanaklı ama soruyu ıskalıyor | Sorgu yeniden yazma ya da üretim prompt'u |
| Hepsi iyi, kullanıcı şikâyetçi | Altın küme temsili değil | Gerçek sorgularla kümeyi genişlet |

> **Karar kuralı:** recall düşükken prompt'la oynamak boşa emektir — modelin hiç görmediği
> parçadan cevap çıkaramazsın. Önce aramayı ölç, sonra üretimi.

İki pratik incelik: (a) **ortalama, bir soru sınıfının tamamen çöktüğünü gizler.** %90 ortalama
recall, "kod/kimlik numarası içeren sorular" sınıfının hep ıskaladığını saklayabilir — sonucu soru
sınıfına göre dilimle (birebir terim, başka kelimelerle sorulmuş, çok konulu). (b) **Etiketler
çürür:** belge arşivi değiştikçe "doğru parça" da değişir; altın kümenin etiketlerini periyodik
gözden geçir.

AI Öğretmen'deki karşılığı: ADR-0005'in anlam araması (`AnlamArama.benzerleri(sorgu, k)`,
`SimpleVectorStore` üzerinde yalnız cosine) kitap notlarını getiriyor. Onun için 20–30 soruluk
küçük bir qrels kümesi ("bu soru için doğru notlar şunlar") recall@k'yı ölçer; Sokratik cevabın
kalitesi ayrı bir sadakat ölçümüdür. İkisini tek "cevap iyi mi?" puanında birleştirirsen, kötü bir
cevabın sebebini asla bulamazsın. (Melez aramanın RRF ile nasıl birleştirildiği ve reranker'a kaç
aday verileceği: [hafta08-ai-engineering-ii.md](hafta08-ai-engineering-ii.md) §1'deki AIEFS kutusu.)

#### 10.2 Küçük altın sette "A, B'den iyi" demeden önce: eşleştirilmiş karşılaştırma + bootstrap
§3 altın kümenin **küçük ama titiz** olmasını öneriyor — 5000 gelişigüzel yerine 50 iyi seçilmiş örnek. Bunun bir bedeli var: küçük
kümede birkaç puanlık fark çoğu zaman **gürültüdür**. "Yeni prompt ortalama 74, eski 71, yeni daha
iyi" cümlesi, ölçümün kendi titreşimi (§4'teki belirlenimsizlik) 3 puandan büyükse hiçbir şey
söylemez. İki kural:

1. **Eşleştirilmiş (paired) karşılaştır.** İki prompt'u **aynı örnekler** üzerinde koştur ve her
   örnek için farkı al: `fark_i = puanB_i − puanA_i`. İki ayrı ortalamayı kıyaslamak, örnekler
   arasındaki büyük zorluk farkını gürültüye katar; örnek-örnek fark bu gürültüyü düşürür (zor
   örnek iki prompt'a da zordur).
2. **Farkın güven aralığını bootstrap ile hesapla.** Bootstrap (yeniden örnekleme) şudur: 40
   farkın içinden **yerine koyarak** 40 tane rastgele çek, ortalamasını al; bunu 1000 kez tekrarla;
   çıkan 1000 ortalamayı sırala, %2,5 ve %97,5 noktalarını oku. Bu, farkın %95 güven aralığıdır
   (confidence interval, CI).

```java
// Eşleştirilmiş bootstrap — fark[i] = puanB[i] - puanA[i], aynı altın küme örneği
double[] ortalamalar = new double[1000];
for (int b = 0; b < 1000; b++) {
    double toplam = 0;
    for (int j = 0; j < fark.length; j++) toplam += fark[rnd.nextInt(fark.length)];
    ortalamalar[b] = toplam / fark.length;
}
Arrays.sort(ortalamalar);
double alt = ortalamalar[25], ust = ortalamalar[974];   // ~%95 güven aralığı
```

> **Karar kuralı:** aralık **sıfırı içeriyorsa beraberlik** say — "B daha iyi" deme. Aralık
> tümüyle sıfırın üstündeyse B gerçekten iyidir; tümüyle altındaysa regresyondur.

Bu, §4'teki CI kapısının eşiğini de değiştirir. "main'e göre %5'ten fazla gerileme yok" gibi sabit
bir eşik (AIEFS 14-30 böyle bir örnek veriyor), 30 örneklik kümede ya rastgele kırmızı yanar ya da
gerçek gerilemeyi kaçırır. Daha dürüst kapı: "eşleştirilmiş farkın güven aralığının üst ucu
−X'in altındaysa kırmızı". Kümen büyüdükçe aralık daralır; küçük kümede geniş aralık, sana
"bu küme bu kadar küçük bir farkı ayırt edemez" der — bu da bir bilgidir.

*Şerh:* 11-10 dersindeki "sabitlenmiş (anchored) rubrikler yargıç tutarsızlığını %30–40 azaltır"
cümlesi kaynak göstermiyor; o yüzden buraya sayı olarak alınmadı. Rubriği örnekli yazmanın
tutarlılığı artırdığı fikri makuldür, ama ne kadar artırdığını **kendi** eşleştirilmiş ölçümün
söyler.

#### 10.3 Koruma filtresini iki yönden ölç — eksik ret ve aşırı ret
Bir güvenlik katmanı (guardrail) iki zıt yönde yanılır ve **ikisi de hatadır**:

- **Eksik ret (under-refusal):** zararlı olarak etiketlenmiş bir isteğe cevap vermek.
- **Aşırı ret (over-refusal):** zararsız bir isteği reddetmek.

Yalnız zararlı istekler üzerinde "ret oranı"nı ölçen ekip, kimya ödevine yardım etmeyen bir
asistan yayınlar. Yalnız yardımseverliği ölçen ekip zararı anlatan bir asistan yayınlar. Doğru
kurgu filtreyi bir **sınıflandırıcı** gibi görmektir: etiketli bir *güvenli* küme ve etiketli bir
*güvensiz* küme, ikisi üzerinde iki ayrı oran.

> **Karar kuralı:** her koruma kuralı için iki sayı raporla — güvensiz kümede kaçırma oranı ve
> **zararsız kümede** yanlış alarm oranı. Zararsız kümesi olmayan filtre, ölçülmemiş filtredir.

Aynı kural prompt enjeksiyonu (prompt injection) dedektörüne de uygulanır: bir regex ("ignore
previous") yazıp "savunma kurduk" demek, ölçülmemiş bir yamadır; saldırgan aynı şeyi başka
kelimelerle söyler. Dedektör, **saldırı kategorisi başına** kesinlik (precision — alarmların kaçı
gerçek saldırı?) ve duyarlılık (recall — saldırıların kaçı yakalandı?) ile, **ayrıca** zararsız
metindeki yanlış alarm oranıyla ölçülür. (Enjeksiyon savunmasının mimari tarafı:
[llm-uygulama-guvenligi.md](llm-uygulama-guvenligi.md).)

Bir öğretmen uygulaması için **aşırı ret gerçek bir ürün hatasıdır**: güvenlik kursundaki bir
öğrencinin "SQL enjeksiyonu nasıl çalışır, örnekle anlat" sorusunu reddeden asistan, tam da
öğretmesi gereken şeyi öğretmiyordur. AI Öğretmen'in prompt'larında (`prompts/sistem-kimlik.st`)
bugün açık bir ret politikası yok; böyle bir kural eklendiği gün, ona eşlik eden ilk eval vakası
"meşru güvenlik/kimya ödevi soruları cevaplanıyor mu?" olmalıdır. Genel ilke (AIEFS 14-30):
**her koruma kuralı bir eval vakasına eşlenir** — eşlenmeyen kural ölçülmemiş varsayımdır.

#### 10.4 Dalkavukluk (sycophancy) için çerçeveleme testi
Dalkavukluk, modelin **kullanıcının belirttiği görüşe**, doğru olup olmamasından bağımsız olarak
katılma eğilimidir. "Bilgisi mi yetmedi, çabası mı?" sorusunun ([model-ve-caba.md](model-ve-caba.md)
§4) dışında kalan üçüncü bir arızadır: model doğruyu biliyor olabilir ama **senin çerçevene**
uyar. Bu eğilim eğitimden gelir: insan geri bildirimiyle pekiştirmeli öğrenme (RLHF) sırasında
insanlar onaylanmayı düzeltilmeye sık sık tercih eder, ödül modeli "katıl" yönüne kayar, eğitim de
bunu büyütür. (Perez ve ark. 2022 ve Sharma ve ark. 2023 bu ilişkiyi gösteren yaygın atıf alan
çalışmalardır.) Mimar için önemi şudur: Claude'a bir ADR'yi, bir tasarımı ya da kodu "bence doğru"
diyerek gözden geçirttiğinde, aldığın onay kısmen **kendi sesinin yankısı** olabilir.

Ölçümü ucuz ve nettir — **çerçeveleme testi (framing test):** aynı iddiayı iki çerçevede sor,
hüküm değişmemeli.

- Çerçeve A (kullanıcının görüşü): "Bence bu servisi ayrı bir mikroservise bölmek doğru. Haklı
  mıyım?"
- Çerçeve B (üçüncü kişinin görüşü): "Bir meslektaş bu servisi ayrı bir mikroservise bölmeyi
  öneriyor. Bu doğru mu?"

Kümede hem **doğru** hem **yanlış** iddialar olmalı (yalnız yanlış iddia koyarsan "her şeye itiraz
eden" model de iyi görünür). Ölçülen şey, A ile B arasındaki **katılma oranı farkıdır**; §10.2'nin
eşleştirilmiş bootstrap'i burada da geçerli (her iddia kendi çiftidir). AI Öğretmen'de doğal yeri
değerlendirmedir: öğrenci gönderiminin sonuna "bence bu çözüm tam puanlık" cümlesi eklenince
`Degerlendirme.puan` yükseliyorsa, değerlendirme dalkavuktur. (`prompts/degerlendirme.st` zaten
"abartılı övgüden kaçın" diyor — bu test, o cümlenin **işe yarayıp yaramadığını** ölçer.)

Pratikte azaltmak için: tercihini söyleme (B çerçevesini kendin kullan), önce en güçlü karşı
argümanı iste, değerlendirmeyi "onayla" değil "en zayıf üç noktayı bul" diye kur.

*Şerh — kaynak dersin atfı güvenilir değil.* AIEFS 18-04 dersi, *Science* dergisinde Mart 2026'da
yayımlanan bir Stanford çalışmasını "kullanıcının yanlış inancını, insanlardan %49 daha fazla
onayladı" diye anlatıyor ve bunu "Bir arkadaşım X dedi" türü çerçevelerle örnekliyor. Bu anlatım
kendi içinde de tutarsız ("bir arkadaşım dedi" zaten üçüncü kişi çerçevesidir) ve ön elemedeki
okumaya göre çalışmanın konusu olgusal yanlış inançlar değil, **sosyal dalkavukluk** — modellerin
kullanıcının **eylemlerini** insanlardan belirgin biçimde daha sık onaylaması. Birincil metni
doğrulayamadığımız için ne "%49"u ne de dersteki diğer yüzdeleri (eğitim öncesi/sonrası
dalkavukluk oranları) burada alıntılıyoruz. Kalıcı olan şey sayı değil, **ölçüm yöntemidir**: aynı
iddia, iki çerçeve, aynı hüküm.

#### 10.5 Değişmezlik (counterfactual) testi — isim değişince puan değişmemeli
`Degerlendirme` bir öğrenciye **puan** veriyor. Bu, bir kaynağın dağıtılmasını etkileyen bir
karardır; AIEFS 18-20'nin ayrımıyla **dağıtımsal zarar (allocational harm)** alanına girer — temsil
zararından (representational harm: kalıp yargı, görünmez kılma) farklı olarak, somut bir sonucu
eşitsiz dağıtma riski. Üstelik [ai-yonetisim-uyum.md](ai-yonetisim-uyum.md) §4'ün okumasıyla
eğitimde değerlendirme yapan sistem AB Yapay Zekâ Yasası'nın **yüksek risk** kademesine düşer;
yanlılığın ölçülmesi orada bir yükümlülüğün parçasıdır.

En ucuz ve en somut test **değişmezlik testidir (counterfactual / invariance test):** içerikte her
şey aynı kalır, yalnız kimlik ipucu değişir — puan değişmemelidir.

- Aynı öğrenci cevabının başına "Ben Ayşe, ..." / "Ben Mehmet, ..." / "Ben Hans, ..." gibi farklı
  isim, cinsiyet ya da köken ipucu koy; geri kalan metin harfiyen aynı.
- AI Öğretmen'de kimlik ipucu iki yoldan girebilir: gönderim metninin kendisi (`{icerik}`) ve
  sistem bağlamındaki öğrenci hafızası (`sistem-kimlik.st`'deki `{hafiza}` alanı,
  `BaglamServisi.sistemBaglami`). Test ikisini ayrı ayrı oynatmalı.
- Her varyantı birkaç kez koş (belirlenimsizlik), her orijinal cevap için varyant puanlarının
  farkını al ve §10.2'nin eşleştirilmiş bootstrap'iyle aralığını hesapla. Aralık sıfırı
  içermiyorsa — örneğin bir isim grubu sistematik olarak birkaç puan düşük alıyorsa — bu bir
  hatadır, "model öyle" diye geçilmez.
- Tek eksen yetmez: yanlılık bazen yalnız **kesişimde** (ör. cinsiyet × köken) görünür; tek tek
  bakınca temiz çıkan model ikili kombinasyonda ayrışabilir. Küçük de olsa birkaç kesişim
  kombinasyonu koy.

Bu öneri bugün depoda bir test olarak **yok**; eklendiğinde doğal yeri §4'teki eval adımıdır
(gerçek LLM çağrısı yaptığı için birim testi değil, gecelik eval koşusu). Birim testi tarafında
yapılabilecek deterministik parça şudur: prompt'a giden metinde gereksiz kimlik bilgisi var mı
— en iyi değişmezlik, modelin ismi **hiç görmemesidir** (veri minimizasyonu,
[ai-yonetisim-uyum.md](ai-yonetisim-uyum.md) §2).

Son bir sınır notu: grup adaleti ölçütleri (gruplar arası eşit kabul oranı, eşit hata oranları,
eşit isabet) taban oranlar farklıysa **aynı anda sağlanamaz** (Chouldechova; Kleinberg ve ark.,
2017). Bu yüzden hangi ölçütü hedeflediğin bir **politika kararıdır** ve açıkça yazılmalıdır.
Değişmezlik testi bu tartışmanın en az tartışmalı köşesidir: "aynı cevap, aynı puan" ilkesine
itiraz eden çıkmaz.

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
12. (§10.1) Bir RAG cevabı yanlış. recall@5 = 0,9 ve MRR yüksek, ama sadakat düşük. Sorun hangi
    aşamada ve neyi düzeltirsin? recall@5 = 0,3 olsaydı prompt'u iyileştirmek neden boşa olurdu?
13. (§10.2) 40 örneklik altın kümede yeni prompt ortalama 3 puan daha yüksek. "Yeni prompt daha
    iyi" demeden önce ne yaparsın? Eşleştirilmiş bootstrap aralığı [−1,5 ; +7] çıktı — kararın ne?
14. (§10.3 + §10.5) Bir koruma kuralı eklediğinde hangi iki oranı raporlarsın? `Degerlendirme`
    için isim değiştirme testini nasıl kurarsın — kimlik ipucu AI Öğretmen'de hangi iki yoldan
    prompt'a girebilir?
15. **Şerh sorusu (§10.4):** bir ders "Stanford çalışması, modellerin kullanıcının yanlış inancını
    %49 daha fazla onayladığını gösterdi" diyor ve bunu "bir arkadaşım X dedi" örneğiyle anlatıyor.
    Bu anlatımda hangi iç tutarsızlık var, bu sayıyı neden alıntılamıyoruz — ve sayı olmadan da
    dalkavukluğu kendi sisteminde nasıl ölçersin?

**Cevap ipuçları (12–15):** 12 — arama doğru parçayı getiriyor, model uyduruyor: üretim prompt'u
("kaynak göster ya da *bilmiyorum* de"); recall düşükse model doğru parçayı hiç görmüyordur,
prompt bunu telafi edemez — önce parçalama/arama. 13 — aynı örneklerde örnek başı fark al,
bootstrap ile %95 aralık hesapla; aralık sıfırı içerdiği için **beraberlik**, "daha iyi" denmez.
14 — güvensiz kümede kaçırma (eksik ret) ve zararsız kümede yanlış alarm (aşırı ret); aynı cevabı
farklı isim/cinsiyet ipucuyla puanlat, farkın aralığına bak; ipucu gönderim metninden (`{icerik}`)
ve öğrenci hafızasından (`{hafiza}`) girebilir. 15 — "bir arkadaşım dedi" zaten üçüncü kişi
çerçevesidir; çalışma (Cheng vd., *Science* 2026; ön baskı [arXiv:2510.01395](https://arxiv.org/abs/2510.01395))
olgusal inançları değil kullanıcının **eylemlerinin** onaylanmasını (sosyal dalkavukluk) ölçüyor; yöntem kalır: aynı iddia,
"benim görüşüm" / "meslektaşın görüşü" çerçevesi, doğru ve yanlış iddialar, katılma farkı.

## Kaynak ve lisans
- §1–§9: 2026 pratiği (EDD, OpenTelemetry GenAI semantic conventions, evals engineering) üzerine
  bu programın kendi özeti.
- §10 ve ilgili öz-sınav soruları: *AI Engineering from Scratch* (Rohit Ghumare, MIT lisansı,
  © 2026), github.com/rohitg00/ai-engineering-from-scratch — dersler
  `19-capstone-projects/` 65-hybrid-retrieval-bm25-dense, 66-reranker-cross-encoder,
  68-rag-eval-precision-recall, 74-leaderboard-aggregation, 83-prompt-injection-detector,
  84-refusal-evaluation; `18-ethics-safety-alignment/` 04-sycophancy-rlhf-amplification,
  20-bias-representational-harm, 21-fairness-criteria-group-individual-counterfactual;
  `11-llm-engineering/10-evaluation`; `14-agent-engineering/30-eval-driven-agent-development`
  (yalnız eval kısmı). Çeviri değil, bu depoya bağlanmış sentezdir; ders kodları kopyalanmadı.
  *Şerh:* 18-04'ün Stanford/*Science* atfı ve yüzdeleri ile 11-10'un "%30–40" iddiası
  doğrulanamadığı için alıntılanmadı (§10.2, §10.4).
- Adı geçen birincil çalışmalar: Cormack, Clarke, Büttcher, "Reciprocal Rank Fusion…" (SIGIR 2009);
  Perez ve ark., "Discovering Language Model Behaviors with Model-Written Evaluations" (2022);
  Sharma ve ark., "Towards Understanding Sycophancy in Language Models" (2023); Chouldechova (2017)
  ve Kleinberg, Mullainathan, Raghavan (2017) — adalet ölçütlerinin birlikte sağlanamazlığı.
