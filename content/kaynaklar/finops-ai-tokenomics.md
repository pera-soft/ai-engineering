---
baslik: "FinOps for AI ve Tokenomics — Model Seçimi Bir Maliyet Mimarisi Kararıdır"
yazar: "2026 pratiği (FinOps Foundation · Tokenomics) — Türkçe kavram özeti"
tarih: "2026-07-17"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 7,8
---

### Bu metin ne anlatıyor?
Yapay zekâya sorulan her soru para harcar. Bu metin o parayı anlatıyor: nereden çıkıyor, neden
kullanıcı sayısıyla birlikte büyüyor ve nasıl küçültülür. Asıl iddiası şu: hangi modeli seçtiğin
sadece bir "kalite" tercihi değil, faturanın nasıl büyüyeceğini belirleyen bir **tasarım** kararıdır
— sonradan düzeltilemeyecek türden. Sonunda bunu kendi projenin iki yüzünde canlı göreceksin.

> **Baştan tek terim: token.** Dil modeli metni kelime kelime değil, **token** denen küçük
> parçalarla işler (bir token kabaca birkaç harf ya da kısa bir kelimedir; Türkçede uzun kelimeler
> birkaç token'a bölünür). Sağlayıcılar ücreti işte bu parçaların **sayısına** göre keser. Bu metinde
> "maliyet" dendiğinde neredeyse her zaman kastedilen budur: **kaç token girdi, kaç token çıktı.**

## Neden bu özet
Hafta 7-8 (AI Engineering I-II) model seçimini (model selection) **yetenek** ekseninde veriyor: hangi
model daha iyi muhakeme ediyor, hangisi JSON şemasına uyuyor, hangisi Türkçe'de daha akıcı. Maliyet
ekseni ise yalnızca "başarılı çıktı başına maliyet" değinisiyle geçiliyor — bir dipnot gibi.

Bu özet o dipnotu **birinci sınıf bir mimari kısıta** çevirir. İddiası tek cümlede şudur:

> **Model seçimi bir yetenek kararı değil, bir MALİYET MİMARİSİ kararıdır** — ve maliyet, sonradan
> optimize edilecek bir muhasebe kalemi değil, tasarım anında verilen bir **mimari nitelik (quality
> attribute)** kararıdır.

Neden şimdi? Çünkü bu iddia artık bir kişisel kanaat değil, **kurumsal olarak formalize edilmiş** bir
disiplin: FinOps Foundation'ın Framework'ünde "FinOps for AI" resmî bir teknoloji kategorisi. Ve daha
önemlisi: bu projenin kendisi, aynı içeriği **iki tamamen farklı maliyet ekonomisiyle** sunan canlı bir
örnek — web arayüzü faturayı sunucu sahibine, MCP sunucusu ise kullanıcıya yazıyor (§6).

> **Tek cümle:** Aynı ürün, iki mimari, iki tamamen farklı maliyet eğrisi — ve farkı yaratan şey model
> değil, **mimari karardır.**

## Türkçe çalışma metni

### 1. Boşluk neden var — yetenek ekseni tek başına yanıltır
Hafta 7-8'in model seçimi anlatısı doğrudur ama **eksiktir**. "Hangi model bu işi yapabilir?" sorusu
gerekli, ama yeter değil. Çünkü:

- Aynı işi yapabilen iki model arasında **10-100x maliyet farkı** olabilir. Yetenek ekseninde ikisi de
  "yapabiliyor" kutusuna girer; ekonomik olarak biri diğerinin katıdır.
- Yetenek eksenini tek başına optimize eden ekip, en güçlü modeli seçer ve **ölçekte iflas eder.**
- Maliyet eksenini tek başına optimize eden ekip, en ucuz modeli seçer ve **kalitede iflas eder** —
  ucuz model daha çok deneme/başarısızlık üretirse gerçek maliyet artabilir (§5).

Boşluğun kaynağı şudur: maliyet, geleneksel yazılımda gerçekten de sonradan gelen bir kalemdi. Bir CRUD
uç noktasının (endpoint) çağrı başına maliyeti öylesine küçüktür ki mimar onu düşünmez; sunucu kapasitesi
bir işletme kalemidir, tasarım kısıtı değil. **LLM bunu bozar:** çağrı başına maliyet artık ölçülebilir,
anlamlı ve kullanıcı sayısıyla doğrusal büyüyen bir sayıdır. Maliyet, tasarım masasına geri döner.

> Bunu Hafta 5-6'nın diliyle söyle: maliyet, **gecikme (latency)** ve **ölçeklenebilirlik (scalability)**
> gibi bir **mimari niteliktir (quality attribute)**. Gecikmeyi sonradan "optimize ederiz" demek nasıl
> naifse, maliyeti sonraya bırakmak da öyledir.

### 2. FinOps for AI resmî bir kategori — kurumsal olarak formalize edilmiş
Bu, bir blog yazarının uydurduğu bir terim değil. Kurumsal olarak formalize edilmiş durumda:

- **FinOps Foundation** — bir **Linux Foundation** projesi — kendi **FinOps Framework**'ünde
  **"FinOps for AI"**'ı resmî bir **teknoloji kategorisi (technology category)** olarak tanımlıyor.
- Kritik ayrıntı: bu kategori, **Public Cloud · SaaS · Data Center · Data Cloud Platforms** ile
  **AYNI SEVİYEDE** duruyor. Yani "bulut maliyetinin bir alt başlığı" değil; kendi başına bir maliyet
  yüzeyi olarak kabul edilmiş.
- Framework'ün dört **domain**'i AI'a da uygulanıyor:
  1. **Understand Usage & Cost** (kullanımı ve maliyeti anla),
  2. **Quantify Business Value** (iş değerini nicele),
  3. **Optimize Usage and Cost** (kullanımı ve maliyeti iyileştir),
  4. **Manage the FinOps Practice** (pratiği yönet).
- **Kalıcılık kanıtı:** **19 Mart 2026** tarihli Framework güncellemesinde kategori **KORUNDU** —
  **deprecate** edilmedi ("kullanımdan kaldırılmış, artık önerilmiyor" damgası yemedi), yeniden
  adlandırılmadı, başka bir kategorinin altına gömülmedi. Bir standart
  gövdesinin bir kategoriyi bir güncelleme turundan **değiştirmeden geçirmesi**, o kategorinin geçici bir
  moda değil yerleşik bir ayrım olduğunun en somut işaretidir.
- Ayrıca Linux Foundation, AI altyapı ekonomisi için **satıcı-bağımsız (vendor-neutral) standartlar**
  üretmek üzere ayrı bir **Tokenomics Foundation** kuruyor.

Öğrenci için çıkarım: "token maliyetini düşünmek" bir mikro-optimizasyon takıntısı değil; adı konmuş,
çerçevesi çizilmiş, kurumsal olarak sahiplenilmiş bir mühendislik alanına giriş.

### 3. Neden MİMARİ bir mesele — kesişim kaygısı (cross-cutting concern)
Asıl argüman burada. FinOps Foundation'ın kendi cümlesi (birebir):

> *"AI cost and usage is not only new to many organizations, and very granular, but also tends to
> transcend technology category boundaries – with investments in the data center, in enterprise
> agreements with AI companies, SaaS products, startup AI model vendors, AI-based neo-clouds, and...
> multiple hyperscale cloud providers."*

Türkçesi: AI maliyeti pek çok kurum için yalnızca **yeni** ve **çok ince taneli (granular)** değil; aynı
zamanda **teknoloji kategorisi sınırlarını aşma eğiliminde** — veri merkezindeki yatırımlar, AI
şirketleriyle yapılan kurumsal anlaşmalar, SaaS ürünleri, girişim model satıcıları, AI tabanlı
**neo-cloud**'lar (yalnızca yapay zekâ hesaplaması — özellikle GPU kiralama — üzerine kurulmuş yeni
nesil bulut şirketleri) ve birden çok **hiperskalır** (hyperscaler — dünya çapında dev veri
merkezleri işleten AWS/Azure/Google Cloud gibi sağlayıcılar) bulut sağlayıcısı arasına yayılıyor.

Bu cümlenin mimari anlamını açıkça çıkaralım. Maliyet:

- veri merkezinde bir **altyapı** kalemi,
- kurumsal anlaşmada bir **tedarik/hukuk** kalemi,
- SaaS aboneliğinde bir **ürün** kalemi,
- model satıcısında bir **mühendislik** kalemi,
- birden çok hiperskalırda bir **bulut** kalemi.

Yani maliyet, **hiçbir tek ekibin tek başına sahiplenemediği** bir yüzeye yayılıyor. Altyapı ekibi
kendi parçasını görür, ürün ekibi kendininkini, tedarik kendininkini — ve **kimse bütünü görmez.**

> Bir kaygı (concern) birden çok modülü/ekibi kesiyor ve hiçbirine tek başına ait olmuyorsa, tanımı gereği
> bir **kesişim kaygısıdır (cross-cutting concern)**. Ve kesişim kaygıları, tanımı gereği **mimarın
> işidir** — çünkü onları görecek konumda başka kimse yoktur.

Güvenlik ve gözlemlenebilirlik neden mimari meselelerdir? Tam da bu yüzden: hiçbir tek modüle ait
değildirler, her şeyi keserler. **AI maliyeti de artık bu listeye girmiştir** (bkz. **Diyagram 1**).

Hafta 5-6'nın "mimari karakteristikler" diliyle bağlarsak: maliyet bir **mimari nitelik (quality
attribute)** olarak modellenmelidir — ölçülebilir, hedeflenebilir, takas edilebilir (trade-off). Bir
mimari nitelik gibi davranır: başka niteliklerle çatışır (yetenek↔maliyet, gecikme↔maliyet), erken
alınan kararlarla mühürlenir ve sonradan değiştirmesi pahalıdır.

## Diyagram 1 — AI maliyeti kategori sınırlarını aşıyor
Merkezde AI maliyeti; çevresinde onu besleyen altı ayrı yatırım yüzeyi. Her yüzey farklı bir ekibe ait;
hiçbiri bütünü görmüyor → sahipsiz kalan kaygı → mimarın işi.

```svg
<svg viewBox="0 0 640 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="AI maliyeti kategori sınırlarını aşıyor: merkezdeki AI maliyeti kalemini veri merkezi, kurumsal anlaşmalar, SaaS ürünleri, model satıcıları, neo-cloud sağlayıcıları ve çoklu hiperskalır bulut sağlayıcıları besliyor; her yüzey farklı bir ekibe ait olduğu için hiçbir ekip bütünü tek başına sahiplenemiyor, bu yüzden AI maliyeti bir mimari kesişim kaygısıdır">
  <defs>
    <marker id="fo-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="fo-1b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">AI Maliyeti Kategori Sınırlarını Aşıyor</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Altı ayrı yatırım yüzeyi · altı ayrı ekip · hiçbiri bütünü görmüyor</text>

  <rect x="20" y="70" width="150" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Veri merkezi</text>
  <text x="95" y="103" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">altyapı ekibi</text>

  <rect x="245" y="70" width="150" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kurumsal anlaşma</text>
  <text x="320" y="103" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">tedarik / hukuk</text>

  <rect x="470" y="70" width="150" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="545" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">SaaS ürünleri</text>
  <text x="545" y="103" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">ürün ekibi</text>

  <rect x="20" y="286" width="150" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="304" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Model satıcısı</text>
  <text x="95" y="319" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">mühendislik</text>

  <rect x="245" y="286" width="150" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="304" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Neo-cloud</text>
  <text x="320" y="319" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">AI tabanlı sağlayıcı</text>

  <rect x="470" y="286" width="150" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="545" y="304" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Çoklu hiperskalır</text>
  <text x="545" y="319" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">bulut ekibi</text>

  <rect x="230" y="172" width="180" height="56" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="2.2"/>
  <text x="320" y="196" text-anchor="middle" font-size="14" font-weight="700" fill="#C77A17" font-family="system-ui">AI MALİYETİ</text>
  <text x="320" y="215" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yeni · çok ince taneli · yayılmış</text>

  <line x1="95" y1="112" x2="228" y2="172" stroke="#5D7789" stroke-width="1.5" marker-end="url(#fo-1a)"/>
  <line x1="320" y1="112" x2="320" y2="170" stroke="#5D7789" stroke-width="1.5" marker-end="url(#fo-1a)"/>
  <line x1="545" y1="112" x2="412" y2="172" stroke="#5D7789" stroke-width="1.5" marker-end="url(#fo-1a)"/>
  <line x1="95" y1="286" x2="228" y2="228" stroke="#5D7789" stroke-width="1.5" marker-end="url(#fo-1a)"/>
  <line x1="320" y1="286" x2="320" y2="230" stroke="#5D7789" stroke-width="1.5" marker-end="url(#fo-1a)"/>
  <line x1="545" y1="286" x2="412" y2="228" stroke="#5D7789" stroke-width="1.5" marker-end="url(#fo-1a)"/>

  <text x="95" y="248" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">parçayı görür</text>
  <text x="545" y="248" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">parçayı görür</text>
  <text x="95" y="152" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">parçayı görür</text>
  <text x="545" y="152" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">parçayı görür</text>

  <rect x="150" y="346" width="340" height="38" rx="9" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.8"/>
  <text x="320" y="363" text-anchor="middle" font-size="11" font-weight="700" fill="#B3402F" font-family="system-ui">Hiçbir ekip tek başına sahiplenemiyor</text>
  <text x="320" y="377" text-anchor="middle" font-size="9.5" fill="#B3402F" font-family="system-ui">→ kesişim kaygısı (cross-cutting concern) → MİMARIN İŞİ</text>
  <line x1="320" y1="332" x2="320" y2="344" stroke="#B3402F" stroke-width="1.8" marker-end="url(#fo-1b)"/>
</svg>
```

### 4. ⚠️ ZORUNLU ŞERH — formalize edilmiş olmak, olgun olmak değildir
Buraya kadar okuyup "demek ki FinOps for AI çözülmüş bir problem" sonucuna varmak **yanlış** olur. Bu
özetin en önemli cümlesi budur:

> **FinOps for AI, kurumsal olarak FORMALİZE EDİLMİŞ ama pratiği henüz OLGUNLAŞMAKTA olan bir
> disiplindir.**

Kanıtlar, disiplinin kendi içinden geliyor:

- **FinOps X 2026** açılış konuşmasının (keynote) başlığı: ***"The Great Token Panic and Is FinOps
  Dead?!"*** — "Büyük Token Paniği ve FinOps Öldü mü?!" Bir disiplinin kendi amiral konferansı, kendi
  varlığını soru işaretiyle açıyorsa, orada rahat bir yerleşiklik yoktur.
- Uygulayıcılar (practitioners) token tüketimi ve AI değeri konusunda açıkça **"iyinin ne olduğuna karar
  verilmedi"** diyor. Yani ölçüyorlar — ama ölçtükleri sayının hangi seviyesinin "iyi" olduğuna dair
  paylaşılan bir ölçüt henüz yok.

Şimdi bu şerhin **nasıl okunmayacağını** da netleştirelim, çünkü burada iki karşıt hataya düşmek kolay:

- **Hata 1 — "Formalize edildi, demek ki çözüldü."** Hayır. Çerçeve var; en iyi pratikler henüz
  oturmadı. Kategoriyi bir standart gövdesinin tanıması, ona uyacak olgun bir reçete olduğu anlamına
  gelmez.
- **Hata 2 — "Olgunlaşmamış, demek ki hype; boş verelim."** Bu da hayır — ve daha tehlikeli olan hata
  budur.

> **Olgunlaşmamışlık, hype demek DEĞİLDİR.** Bir alan hem gerçek hem de henüz olgunlaşmamış olabilir.
> Hype, ortada gerçek bir problem yokken gürültü olmasıdır. Burada problem **fazlasıyla gerçek**
> (maliyet gerçekten yayılıyor, gerçekten büyüyor, gerçekten kimse sahiplenmiyor); olgunlaşmamış olan
> şey **çözümdür**, problemin kendisi değil.

Mühendislik açısından doğru duruş şudur: bu alana gir, **ölçmeye bugün başla**, ama kimsenin sana
"doğru sayı budur" diye vereceği hazır bir eşik beklemeden başla. Eşiği kendi bağlamında sen kuracaksın.
Bir disiplinin genç olması, ona katılmamak için değil, **katkı verebilmek** için bir sebeptir.

### 5. Model seçimi = maliyet mimarisi kararı
Şimdi asıl karara gelelim. Aynı işi yapan iki model arasında **10-100x maliyet farkı** olabilir. Naif
refleks: "ucuzu seç." Tuzak şudur:

> **Ucuz model daha çok deneme/başarısızlık üretirse, gerçek maliyet ARTAR.** Çağrı başına ucuz olan,
> iş başına pahalı olabilir — çünkü aynı işi bitirmek için üç çağrı gerekir, üstelik üçünün de
> tutmadığı durumlarda insan müdahalesi devreye girer (bkz. **Diyagram 3**).

Bu yüzden tek doğru metrik **çağrı başına maliyet (cost per call)** değil:

> **Başarılı çıktı başına maliyet (cost per successful output).**

Bu metrik yeni değil — [`evals-llm-observability.md`](evals-llm-observability.md) §7'de zaten tanımlı ve
`KullanimService` verisiyle eval sonucunu nasıl birleştireceğin orada anlatılıyor. **Burada tekrar
etmiyoruz;** o özete git. Buradaki katkı farklı bir şey: o metriğin yalnızca bir gösterge panosu kalemi
değil, bir **mimari karar girdisi** olduğunu göstermek.

Çünkü model seçimi tek eksenli değildir. Kararın **beş ekseni** vardır ve hepsi birbiriyle takas
(trade-off) hâlindedir:

| Eksen | Soru | Takas |
|---|---|---|
| **Yetenek (capability)** | Bu model işi yapabiliyor mu? | Güçlü model genelde pahalı |
| **Gecikme (latency)** | Kullanıcı ne kadar bekliyor? | Hızlı model genelde daha küçük/zayıf |
| **Maliyet (cost)** | Başarılı çıktı başına ne ödüyoruz? | Ucuz model daha çok deneme üretebilir |
| **Gizlilik / veri ikametgâhı (data residency)** | Veri nereye gidiyor, hangi hukuka tabi? | Yerel/özel model pahalı ve zayıf olabilir |
| **Satıcı bağımlılığı (vendor lock-in)** | Yarın bu modelden çıkabilir miyiz? | Soyutlama katmanı maliyet ve karmaşıklık ekler |

Bu tablonun mimari okuması şudur: bunların hiçbiri "sonra bakarız" kalemi değildir. Beşi de **erken**
alınan ve **değiştirmesi pahalı** kararlardır — yani tanımı gereği mimari kararlardır. Bu projede
**ADR-0004 (OpenRouter sağlayıcısı)** tam da bu takasların bir kaydıdır: tek bir satıcıya doğrudan
bağlanmak yerine bir yönlendirici (router) üzerinden geçmek, satıcı bağımlılığı eksenini bilinçli olarak
gevşetir — ve bunun bedeli kabul edilmiş bir karmaşıklıktır. Karar, bir ADR'ye yazıldığı için de
görünürdür; kimsenin kafasında saklı değildir.

#### 5.1. Küçük modeller (SLM) — gizlilik satırının derinleşmesi
Yukarıdaki tablonun **gizlilik/veri ikametgâhı** satırı bir soru bırakıyor: veriyi dışarı hiç
çıkarmadan iş yapılabilir mi? Cevap "bazen evet" — ve bu, küçük dil modelleri (small language models,
**SLM**) tartışmasıdır: **3B** (≈3 milyar parametre — "parametre", modelin eğitimle ayarlanmış iç
sayılarıdır; ne kadar çoksa model o kadar büyük, güçlü ve pahalıdır) civarı, cihazda ya da kendi
sunucunda çalışan modeller.

**Üretim kanıtı var (satıcı-birincil ama doğrulanabilir):** Apple, **~3B parametrelik cihaz-üstü**
(on-device) bir modeli üretimde çalıştırıyor ve **iki nesildir** sürdürüyor — Haziran 2026'da üçüncü
nesil "AFM 3 Core" olarak devam etti (yanına 20B seyrek/sparse bir varyant eklendi; aynı anda yalnızca
1-4B parametre aktif). Bu, uydurulabilir bir pazarlama iddiası değil: model iOS'ta kamuya açık bir
API olarak sunuluyor, üçüncü taraf geliştiriciler kullanıyor. Mimari fikir: **hafif iş cihazda, ağır
iş bulutta.**

> ⚠️ **Ama burada asıl ders SLM değil — kanıt okuma.** NVIDIA'nın çokça paylaşılan *"SLM'ler ajansal
> AI'ın geleceğidir"* tezi bir **satıcı görüş bildirisidir (position paper)**, ampirik çalışma değil.
> Makale bunu **kendisi söylüyor**: *"Our position, formulated as a value statement..."*. Dolaşan
> "%60 / %40 / %70 şu ajanın çağrıları SLM'e devredilebilir" rakamları **ölçülmemiştir** — kod
> çalıştırılmamış, benchmark yapılmamış; hepsi *"we estimate that about X%"* kalıbında masa başı
> tahmini. Metodoloji bölümü yok. Yazarların 8'i de NVIDIA Research'ten ve makale kendi ürünlerini
> (Nemotron-H, Hymba, Dynamo, ChatRTX) kanıt gösteriyor — SLM'lerin uca kayması NVIDIA'nın donanım
> portföyünü besler. Dahası: NVIDIA'nın **kendi savunma sayfasında bile** isimli tek bir üretim vakası,
> ölçülmüş tek bir maliyet tasarrufu ya da benimseme rakamı **yok**.
>
> Yani: **SLM ekseni gerçek, NVIDIA'nın tezi kanıt değil.** Üretim kanıtı için Apple hattına bak;
> "geleceğin bu" cümlesi için kimseye bakma. Bu ayrımı yapmak — satıcı tezi ile bağımsız üretim
> kanıtını ayırmak — bu kaynağın sana öğrettiği asıl beceridir. Aynı refleksin ölçüm tarafı:
> [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md).

Pratik sonuç: SLM'i **bir karar çerçevesi** olarak kullan (hangi işler küçüğe devredilebilir? veri
dışarı çıkmamalı mı? gecikme kritik mi?), **yerleşik bir pratik** olarak değil. Bu proje için bugün
geçerli değil — muhakeme zaten istemcide (§6) — ama veri ikametgâmı sorunu olan bir kurumda ilk
bakacağın eksen budur.

#### 5.2. Üç büyük maliyet kaldıracı daha — önbellek, toplu işleme, yönlendirme
Model seçimi tek kaldıraç değil. Aynı işi daha ucuza yapmanın üç mekanizması daha var ve üçü de
**mimari** karardır — sonradan eklenen ayar değil.

**Prompt önbelleği (prompt caching):** API, isteğin **öneki** (prefix — isteğin baş tarafı) aynı
kaldığı sürece o kısmın işlenmiş hâlini saklayıp yeniden kullanabilir; önbellekten okumak, taze
işlemekten **kabaca bir kertelik (10x)** ucuzdur.
Sistem promptu, araç kataloğu, paylaşılan doküman, büyüyen sohbet geçmişi — aynı büyük öneki tekrar
tekrar gönderen her iş yükünde bu, çoğu zaman **model seçiminden bile büyük** bir maliyet/gecikme
kaldıracıdır. Tek bir kural her şeyi yönetir:

> **Önbellek, TAM önek eşleşmesidir.** Sıra: araçlar → sistem promptu → mesajlar. Önekteki tek bir
> bayt değişirse, sonrası tamamen geçersizleşir. Önbellek arızalarının çoğu eksik işaret değil,
> **kararsız önektir.**

Sessiz geçersizleştiriciler (denetle): sistem promptuna gömülen **zaman damgası** (her istek benzersiz
önek olur — klasik önbellek katili) · sırasız JSON/araç listesi (deterministik olmayan serileştirme) ·
öneğin başına konan kullanıcı/oturum kimliği · bayrağa göre değişen sistem promptu bölümleri · oturum
ortasında araç seti ya da model değiştirmek. Tasarım ilkesi: **içeriği kararlılığa göre sırala** —
en kararlı en önde, değişken her şey (son kullanıcı mesajı, güncel durum) en sona.
Ve **varsayma, doğrula:** yanıtın `usage` bloğu önbellek yazma/okuma sayılarını ayrı raporlar;
tekrarlanan isteklerde okuma sıfırsa sessiz bir geçersizleştirici iş başındadır.

> ⚠️ **Önbellek sağlayıcıya özeldir — mekanizma ortak, kurallar değil.** "Önek eşleşmesi" fikri her
> yerde aynıdır; ama **nasıl açıldığı** sağlayıcıdan sağlayıcıya değişir. Bazı sağlayıcılarda
> önbellek **kendiliğinden** çalışır (yeterince uzun bir önek tekrarlanırsa otomatik yakalanır),
> bazılarında ise isteğin içinde **açıkça işaretlemen** gerekir ("buraya kadarki kısmı önbelleğe
> al" diyen bir sınır koyarsın) ve işaret sayısı sınırlıdır. Ayrıca her sağlayıcının kendi
> **asgari token eşiği** (bu uzunluğun altındaki önek hiç önbelleğe alınmaz), kendi **saklama
> süresi (TTL)** ve kendi **fiyatlaması** vardır — çoğunda önbelleğe *yazmak* normal girdiden bir
> miktar pahalı, *okumak* ise çok ucuzdur. Sonuç: bir sağlayıcı için yazdığın önbellek stratejisi
> diğerine olduğu gibi taşınmaz; **taşımadan önce o sağlayıcının dokümanını oku ve `usage` ile
> ölçerek doğrula.** Bizim durumumuzda araya bir yönlendirici (OpenRouter, ADR-0004) girdiği için
> bu bir kat daha doğrudur: arkadaki modele göre davranış değişebilir.

**Toplu işleme (batch):** acele etmeyen hacim (gece raporları, toplu değerlendirme) indirimli ve
asenkron (sonucu hemen değil, hazır olunca alacak şekilde) işlenebilir — gecikmeye toleransı
maliyete çevirirsin.

**Yönlendirme (routing):** Tek bir modele her işi yaptırmak zorunda değilsin. İstek geldiğinde
**önce zorluğuna bak, sonra modeli seç**: rutin/şablonik iş küçük ve ucuz modele, gerçekten
muhakeme isteyen iş büyük modele gitsin. Kritik incelik şudur: yönlendirmenin kendisi de bir
karardır ve **yanlış yönlendirme pahalıdır** — küçük modele giden iş tutmazsa büyük modele yeniden
gider ve ikisini birden ödersin (§5'in "ucuz model daha çok deneme üretir" tuzağı). Bu yüzden
yönlendirme kuralını **eval ile** ölç: küçüğe devredilen sınıfta başarı oranı düşüyorsa kural
yanlıştır. Ayrıca yönlendiriciyi bir **soyutlama noktası** olarak da düşün — model değiştirmeyi tek
yerden yapılabilir kılar (satıcı bağımlılığı ekseni).

Beş kaldıracın eşleme tablosu (senaryo sorusu geldiğinde neyin pahalı olduğuna bak):

| Pahalı olan ne? | Doğru kaldıraç |
|---|---|
| Tekrarlanan büyük önek | **Önbellek** |
| Ertelenebilir hacim | **Batch** |
| Fazla katmanlı yetenek (iş rutin) | **Küçük model** |
| İşlerin zorluğu çok değişken (bazısı basit, bazısı zor) | **Yönlendirme** |
| Şişkin, benzersiz içerik | **Kırp** (önbellek KURTARMAZ — pencereyi de büyütmez) |

> ⚠️ **Eskime şerhi:** buradaki oranlar ve limitler (≈10x okuma indirimi, birkaç dakikalık varsayılan
> TTL, işaret sayısı sınırı) **bugünün ürün değerleridir** — mekanizma kalıcı, sayılar eskir; güncel
> değeri sağlayıcının dokümanından doğrula. Bu bölümün çekirdeği Claude Certified Architect sınavının
> topluluk rehberinden (Daron Yöndem) süzülmüştür — resmî Anthropic dokümanı değildir, ilkeler oradan
> teyitlidir.

Bu projedeki bağ: MCP sunucumuz LLM çağırmadığı için önbellek MCP tarafında konu dışı — ama **web
yüzü** (OpenRouter, `OgretmenAiClient`) her sohbette aynı sistem kimliğini ve hafta bağlamını
gönderiyor: tekrarlanan önek tam da bu. `AI_KULLANIM` tablosunda girdi token'ı yüksekse ilk bakılacak
kaldıraç budur.

#### 5.3. Üç kaldıracı derinleştirmek — uygulama önbelleği, paralel yarış, SLA'ya göre batch, rota başına kapı
§5.2'deki üç kaldıraç kâğıt üstünde basittir; canlıda her birinin kendine özgü bir tuzağı vardır.

**Uygulama seviyesi önbellek — ayrı bir katman, ayrı bir risk.** §5.2'deki prompt önbelleği
**sağlayıcının** önbelleğidir (L2): aynı öneki ucuza yeniden işler, ama model yine çağrılır ve
cevap yine taze üretilir. Bunun önünde, **senin** uygulamanda duran ikinci bir katman olabilir
(L1): model hiç çağrılmadan, daha önce verilmiş bir cevap geri döner. İki türü vardır:

- **Birebir (exact) önbellek:** prompt'un tamamının özeti (hash) anahtar olur; aynı girdi → aynı
  kayıtlı cevap. Yalnız **deterministik** sayılabilecek çağrılarda güvenlidir (aynı model, aynı
  prompt, sıcaklık 0, cevabın zamana bağlı olmadığı iş).
- **Anlamsal önbellek (semantic cache):** soru gömülür (embedding), önbellekte **benzer** bir soru
  aranır; benzerlik eşiği aşılırsa (tipik olarak kosinüs 0,92–0,95) onun cevabı döner. "İade
  politikası nedir?" ile "Ürünü nasıl iade ederim?" aynı cevabı alır.

Anlamsal önbelleğin kendine özgü riski **yanlış cevaptır**: eşik gevşekse, *benzer ama farklı* bir
soru başkasının cevabını alır ("Java 17'de record nasıl yazılır?" ile "Java 21'de record pattern
nasıl yazılır?" gömme uzayında çok yakındır). Prompt önbelleği asla yanlış cevap vermez — en kötü
ihtimalle ıskalar; anlamsal önbellek ise **kendinden emin biçimde yanlış** cevap verebilir. Bu yüzden
eşik bir maliyet ayarı değil, bir **doğruluk** ayarıdır ve eval ile ölçülür.

Gerçekçi isabet oranları iş yüküne göre çok değişir. Dersin aktardığı, **uygulayıcıların bildirdiği**
(satıcı dokümanı ya da ölçülmüş bir taban değil) aralıklar: açık uçlu sohbette ~%10–15, yapılandırılmış
SSS/destekte %40–70, kod sorularında %20–30 (küçük farklar isabeti öldürür). Satıcıların "doğruluk"
dediği şey çoğu zaman isabet oranı değil, **isabet olduğunda dönen cevabın uygun olma oranıdır** —
ikisini karıştırma.

> *Şerh:* Derslerden biri "sorguların %40–60'ı neredeyse aynıdır" der ve kaynak göstermez; bir
> başkası anlamsal önbellekle "%15–30 tasarruf", yönlendirmeyle "%40–70" gibi katman tasarrufları
> sıralar — bunlar da kaynaksızdır. Kendi trafiğinden bir haftalık örnekle ölç.

**Bu projedeki bağ.** Bu depoda zaten bir **birebir** uygulama önbelleği var: haftalık rapor
`RaporService`'te kullanıcı + hafta anahtarıyla dosyaya yazılır, "Yenile" denmedikçe model
çağrılmaz. Anlamsal önbellek ise sohbet için **yanlış** bir fikir olurdu ve sebebi maliyet değil
**izolasyondur**: sohbet prompt'u öğrencinin kendi hafızasını (`OGRENCI.md`) ve son mesajlarını
taşır; iki öğrencinin "benzer" sorusu farklı cevap gerektirir. Kullanıcıdan bağımsız anahtarlı bir
anlamsal önbellek, A'nın kişisel bağlamla üretilmiş cevabını B'ye verir — ADR-0012'nin "bir
kullanıcının başkasının verisini görmesi" başarısızlık ölçütünü doğrudan ihlal eder. Kural: L1
önbelleğin anahtarı, cevabı etkileyen **her** girdiyi (kullanıcı, bağlam sürümü, model) içermelidir.

**Paralel çağrıda önbellek yarışı (fan-out race).** Bir ajan aynı uzun sistem prompt'uyla 10 çağrıyı
**aynı anda** başlatır. Sağlayıcı önbelleği ilk isteğin yazması bittikten sonra okunabilir; diğer 9
istek aynı milisaniye penceresinde gelir ve hepsi ıskalar. Sonuç: **N paralel çağrı = N yazma, 0
okuma** — §5.2'deki yazma primini 10 kez ödersin, okuma indirimini hiç almazsın. Çözüm basittir:
**önce bir çağrı, sonra yelpaze**: ilkini tek başına gönder, önbellek dolunca kalan 9'u paralel
başlat. İlk çağrıya birkaç yüz milisaniye eklersin, faturanın katlanmasını önlersin. (Bu depoda web
yüzü AI çağrılarını paralel başlatmaz; tuzak ajan iş akışlarında ve toplu değerlendirmede doğar.)

**Batch kararını "canlı mı?" sorusuyla değil, SLA ile ver.** Sık yapılan hata: "bu üretimde çalışıyor,
o hâlde senkron olmalı." Üretim bir gecikme şartı değildir; **SLA** gecikme şartıdır. Üç sınıf:

| Sınıf | Kullanıcı ne bekler | Doğru yol |
|---|---|---|
| Etkileşimli (interactive) | cevabı ekranda bekler | senkron + prompt önbelleği; batch olmaz |
| Yarı etkileşimli (semi-interactive) | işi verir, dakikalar sonra bakar | asenkron kuyruk |
| Toplu (batch) | "sabaha kadar", "gün sonuna kadar" | toplu işleme API'si + önbellek |

**Kısmi etkileşim tuzağı (partial-interactivity trap):** bazı özellikler etkileşimli *görünür* ama
10 dakikalık beklemeye razıdır — "Yenile" düğmeli bir gece raporu gibi. Ekip onu senkron yazar; 50
kişi aynı anda yeniler; her biri tam fiyat öder. Sorulacak soru: **"24 saat bu kullanıcı için ne
demek?"** Cevap "fark etmez" ise batch'tir. Bu depodaki haftalık rapor bu soruya adaydır: haftanın
sonunda bir kez üretilip hazır beklese öğrenci büyük ihtimalle fark etmezdi. (OpenRouter'ın da bir
[Batch API](https://openrouter.ai/docs/guides/features/batch-api)'si var: tipik olarak eşzamanlı
token fiyatının ~%50'si, çoğu sağlayıcıda 24 saat içinde teslim; oran sağlayıcıya göre değişir.)

İki ek not: Toplu işleme ile prompt önbelleği **üst üste** biner; ders bu yığının senkron, önbelleksiz
maliyetin ~%10'una inebileceğini söyler — bu, açıklayıcı bir hesap örneğidir, ölçülmüş bir taban
değil. Ve batch dosya biçimleri **sağlayıcıya özeldir**: "tek bir batch istemcisi" yazmak sağlayıcı
başına bir adaptör demektir — taşınabilirlik ekseninde bir bedel (port/adaptör: Hafta 9).

**Yönlendirmede rota başına sapma kapısı.** §5.2 yönlendirmeyi eval ile ölç der. Hangi desende
olduğun, ölçülecek şeyi değiştirir:

| Desen | Nasıl | Gecikme bedeli |
|---|---|---|
| Ön yönlendirme (pre-route) | önce sınıflandırıcı, sonra model | küçük (sınıflandırıcı kadar) |
| Kademeli (cascade) | önce ucuz model; güvensizse pahalıya yükselt | yükselen isteklerde ~iki çağrı |
| Topluluk (ensemble) | ikisini paralel koş, iyisini seç | en pahalı; yalnız kritik A/B için |

Asıl risk **sapmadır (drift)**: yönlendirici ilk çeyreğin verisiyle kuruldu, kullanıcıların soruları
altı ayda uzadı ve zorlaştı; ucuz rotanın kalitesi sessizce düşer, kimse yüksek sesle şikâyet etmez.
Bu yüzden her rotanın **kendi** kapısı olur: rota başına olumsuz geri bildirim; rota başına küçük bir
örneklemde (ders ~%5 der) yargıç-model puanı; rota başına ret oranı; kademeli desende **yükseltme
oranı** — ucuz modelden pahalıya yükselenlerin payı ~%30'u aşıyorsa ucuz rotaya fazla iş gidiyordur
ve "iki kez ödeme" tuzağı (§5.2) büyümüştür. Bu depodaki yönlendirme statik bir ön yönlendirmedir
(ADR-0003: işlem türüne göre GUCLU/HIZLI); ADR-0003'ün kendi "(−)" maddesi ve
`docs/tech-debt-tracker.md`'deki 3. kalem ("Haiku'nun quiz üretim kalitesi izlenmedi") tam olarak
eksik bir rota başına kapıdır.

> *Şerh:* Dersin "yönlendirmeyle aynı kalitede %20–60 tasarruf" rakamı vaka çalışmalarına dayanır ve
> kaynakları verilmez; eşik değerleri (%30 yükseltme, %5 örneklem) pratik kurallardır, standart değil.

### 6. Bu projedeki canlı örnek — iki zıt ekonomi
Bu özetin en değerli bölümü burası; çünkü kavram burada soyut olmaktan çıkıyor. **AI Öğretmen, aynı
içeriği iki tamamen farklı ekonomiyle sunuyor.** Aynı müfredat, aynı kaynaklar, aynı prompt'lar — iki
mimari, iki maliyet eğrisi.

| | **Web arayüzü (8090)** | **MCP sunucusu (ai.velzon.tr/mcp)** |
|---|---|---|
| **Muhakemeyi kim yapar** | Sunucu — `OgretmenAiClient` üzerinden OpenRouter'ı çağırır | Bağlanan kişinin kendi Claude'u (Claude Desktop/Code) |
| **Sunucu LLM çağırır mı** | **Evet** | **Hayır** — ArchUnit ile mühürlü |
| **Faturayı kim öder** | **Sunucu sahibi** | **Kullanıcı** (kendi token'ı) |
| **N kullanıcıda maliyet** | **N × maliyet** (doğrusal büyür) | **Sabit** (içerik sunmanın maliyeti) |
| **Sunucunun rolü** | İçerik **+ muhakeme** | Yalnız içerik + araç + prompt |
| **Token sayacı** | `KullanimService.istatistik()` / `AI_KULLANIM` tablosu | Yok — çünkü sunucu token harcamıyor |
| **Ölçekleme sonucu** | Çok kullanıcıda **token yetmez** | Sunucu ölçeklenir, **maliyet ölçeklenmez** |
| **Karar kaydı** | ADR-0004 (OpenRouter) | ADR-0007 (MCP sunucusu) |

Bu tabloyu satır satır okumak yerine, **tek bir satırına** odaklan: *"N kullanıcıda maliyet."* Web'de
**N × maliyet**, MCP'de **sabit**. Aynı ürün. Aynı içerik. Aynı öğretmenlik kalitesi. Farkı yaratan tek
şey: **muhakemenin hangi tarafta yapıldığı.**

Ve bu, bir optimizasyon değil bir **topoloji** kararıdır. Kimse web arayüzüne "daha ucuz model" takarak
onu MCP'nin maliyet eğrisine getiremez — çünkü mesele modelin fiyatı değil, **faturanın hangi tarafa
yazıldığıdır.** 10x ucuz model, N × maliyeti N × (maliyet/10) yapar; hâlâ **N ile büyür**. MCP ise
eğrinin **eğimini** sıfırlar. Bu, prompt ayarıyla ulaşılabilecek bir yer değildir.

> **Ders:** Maliyet modeli bir **mimari karardır**, sonradan optimize edilecek bir detay değil. Doğru
> mimari, yanlış mimarinin en iyi optimize edilmiş hâlinden **kategorik olarak** ucuzdur.

Dikkat edilecek incelik: MCP kararı maliyeti **yok etmiyor**, **taşıyor**. Kullanıcı kendi token'ını
harcıyor — bedava değil, sadece fatura sahibi değişti. Bu bir hile değil, bilinçli bir takastır: karşılığında
sunucu sahibi kontrolden ve merkezî ölçümden feragat eder (§7). ADR-0007 bu takası açıkça kaydeder.

Mimari mührü de not et: MCP katmanının LLM çağırmaması bir niyet beyanı değil, **`MimariTestleri` ile
zorlanan bir kuraldır** — `..aiogretmen.mcp..` katmanı Spring AI'ya erişemez. Yani "MCP sunucusu token
harcamaz" cümlesi bir umut değil, `./mvnw verify` yeşil olduğu sürece **kanıtlanmış bir olgudur.**
Maliyet mimarisi, ArchUnit tarafından korunan bir mimari niteliktir.

## Diyagram 2 — İki ekonomi (bu projenin canlı örneği)
Üstte web: sunucu OpenRouter'ı çağırır, fatura sunucuda, N kullanıcı = N × maliyet. Altta MCP: sunucu
yalnız içerik verir, muhakeme kullanıcının Claude'unda, fatura kullanıcıda, N kullanıcı = sabit maliyet.

```svg
<svg viewBox="0 0 640 380" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="AI Öğretmen projesinin iki zıt ekonomisi: üstte web arayüzünde kullanıcı sunucuya gider, sunucu OpenRouter'ı çağırır ve fatura sunucu sahibine yazılır, N kullanıcı N çarpı maliyet demektir; altta MCP sunucusunda kullanıcı kendi Claude'uyla bağlanır, sunucu yalnızca içerik ve araç sunar, LLM çağırmaz, fatura kullanıcıya yazılır ve N kullanıcıda sunucu maliyeti sabit kalır">
  <defs>
    <marker id="fo-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="fo-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
    <marker id="fo-2c" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki Ekonomi — Aynı İçerik, İki Mimari</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Farkı yaratan model değil; muhakemenin hangi tarafta yapıldığı</text>

  <text x="14" y="70" font-size="11.5" font-weight="700" fill="#B3402F" font-family="system-ui">WEB (8090) — fatura SUNUCUDA · ADR-0004</text>
  <rect x="14" y="80" width="96" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="62" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kullanıcı</text>
  <text x="62" y="118" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">(tarayıcı)</text>

  <rect x="146" y="80" width="126" height="52" rx="9" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.8"/>
  <text x="209" y="100" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3402F" font-family="system-ui">Sunucu + LLM</text>
  <text x="209" y="115" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">OgretmenAiClient</text>
  <text x="209" y="127" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">muhakeme BURADA</text>

  <rect x="308" y="80" width="110" height="52" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="363" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">OpenRouter</text>
  <text x="363" y="118" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">token yanar</text>

  <rect x="452" y="80" width="174" height="52" rx="9" fill="#F7E9E7" stroke="#B3402F" stroke-width="2"/>
  <text x="539" y="100" text-anchor="middle" font-size="11" font-weight="700" fill="#B3402F" font-family="system-ui">FATURA: sunucu sahibi</text>
  <text x="539" y="116" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">N kullanıcı = N × maliyet</text>
  <text x="539" y="128" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">sayaç: KullanimService.istatistik()</text>

  <line x1="110" y1="106" x2="144" y2="106" stroke="#5D7789" stroke-width="1.8" marker-end="url(#fo-2a)"/>
  <line x1="272" y1="106" x2="306" y2="106" stroke="#B3402F" stroke-width="1.8" marker-end="url(#fo-2b)"/>
  <line x1="418" y1="106" x2="450" y2="106" stroke="#B3402F" stroke-width="1.8" marker-end="url(#fo-2b)"/>
  <text x="209" y="150" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">AI_KULLANIM tablosuna yazılır</text>

  <line x1="14" y1="176" x2="626" y2="176" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>

  <text x="14" y="200" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">MCP (ai.velzon.tr/mcp) — fatura KULLANICIDA · ADR-0007</text>
  <rect x="14" y="212" width="126" height="60" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="77" y="232" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kullanıcı +</text>
  <text x="77" y="247" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">kendi Claude'u</text>
  <text x="77" y="263" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">muhakeme BURADA</text>

  <rect x="176" y="212" width="140" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="246" y="232" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Sunucu (LLM'siz)</text>
  <text x="246" y="247" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">içerik + araç + prompt</text>
  <text x="246" y="263" text-anchor="middle" font-size="8" fill="#25804C" font-family="system-ui">ArchUnit mühürlü</text>

  <rect x="352" y="212" width="120" height="60" rx="9" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 4"/>
  <text x="412" y="238" text-anchor="middle" font-size="10" font-weight="700" fill="#5D7789" font-family="system-ui">OpenRouter</text>
  <text x="412" y="255" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ÇAĞRILMAZ</text>

  <rect x="452" y="286" width="174" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="539" y="306" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">FATURA: kullanıcı</text>
  <text x="539" y="322" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">N kullanıcı = SABİT maliyet</text>
  <text x="539" y="334" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">sunucu ölçeklenir, maliyet ölçeklenmez</text>

  <line x1="140" y1="242" x2="174" y2="242" stroke="#5D7789" stroke-width="1.8" marker-end="url(#fo-2a)"/>
  <text x="157" y="234" text-anchor="middle" font-size="7.5" fill="#5D7789" font-family="system-ui">MCP</text>
  <line x1="316" y1="242" x2="350" y2="242" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="4 4"/>
  <line x1="330" y1="228" x2="342" y2="256" stroke="#B3402F" stroke-width="2"/>
  <line x1="342" y1="228" x2="330" y2="256" stroke="#B3402F" stroke-width="2"/>
  <path d="M77,272 C77,310 300,320 450,312" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#fo-2c)"/>
  <text x="240" y="308" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">token kullanıcının tarafında yanar</text>

  <text x="320" y="362" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Aynı içerik · iki mimari · iki maliyet eğrisi. Maliyet modeli bir MİMARİ KARARDIR.</text>
</svg>
```

### 7. Pratik — neyi ölç, ve ölçtüğünü nasıl okur
"Ölçmediğini yönetemezsin" doğru bir klişedir. Ölçülecekler:

- **Token** — girdi (input) ve çıktı (output) **ayrı ayrı**. Ayrı tutmak iki sebeple şarttır.
  **Birincisi: fiyatları aynı değildir ve çıktı pahalıdır.** Sağlayıcılar tipik olarak ürettiği her
  token için girdiye göre **birkaç kat** (çoğu modelde kabaca 3-5 katı; oran modelden modele değişir
  ve zamanla eskir, güncelini fiyat sayfasından doğrula) ücret alır. Sebebi teknik: girdinin tamamı
  tek seferde toplu işlenebilir, çıktı ise **token token, sırayla** üretilir — her yeni token için
  model baştan çalışır. Pratik sonucu şudur: **"cevabı kısa tut" demek, "prompt'u kısalt"maktan çoğu
  zaman daha çok para kazandırır.** Uzun uzun düşünen/gerekçelendiren modellerde bu fark daha da
  büyür, çünkü o düşünme de çıktı olarak faturalanır.
  **İkincisi: ikisi farklı mühendislik problemine işaret eder** — şişen girdi bir **bağlam (context)**
  tasarımı problemidir (fazla şey gönderiyorsun), şişen çıktı bir **prompt/format** problemidir
  (fazla şey istiyorsun). Toplam token'a bakan bir pano, hangi problemin sende olduğunu söylemez.
- **Çağrı sayısı** — trafiğin hacmi.
- **Başarısız / yeniden deneme (retry) oranı** — gizli maliyetin saklandığı yer burasıdır. Bir işi
  bitirmek için ortalama kaç çağrı gidiyor?
- **Başarılı çıktı başına maliyet** — §5'teki tek doğru metrik.
- **Kullanıcı başına maliyet** — ölçekleme sorusunun cevabı. Web'de bu sayı sabit ve pozitiftir; MCP'de
  sunucu tarafında sıfıra yakındır (§6).

İyi haber: bu projede altyapı **zaten var.**

- `AI_KULLANIM` tablosu her çağrıyı token'larıyla kaydeder (`AiKullanim`),
- `KullanimService.istatistik()` bunları toplar,
- MCP tarafında `kullanim_istatistik` aracı bu özeti bir ajana açar.

Ama burada bitirmek, bu özetin en başta uyardığı hataya düşmek olur:

> **Ölçmediğini yönetemezsin — ama ÖLÇTÜĞÜNÜ DE YORUMLAMAN GEREK.** Sayı, kendi anlamını beraberinde
> getirmez.

Bir örnekle: `kullanim_istatistik` "bu hafta token kullanımı %40 düştü" diyor. Bu **iyi mi?** Cevap
tek başına o sayıda **yok**:

- Prompt'lar verimlileşti ve aynı iş daha az token'la yapıldıysa → iyi.
- Kullanıcılar uygulamayı kullanmayı bıraktığı için düştüyse → felaket.
- Model sessizce daha kısa (ve daha yüzeysel) cevaplar vermeye başladıysa → **kalite regresyonu**, ve
  maliyet grafiği bunu **iyi haber gibi gösterir.**

Üçü de aynı grafiği çizer. Sayı aynı, anlam zıt. İşte bu yüzden §4'teki şerh pratik bir uyarıdır:
uygulayıcıların **"iyinin ne olduğuna karar verilmedi"** demesi tam olarak bu belirsizliktir.

Buradan iki kural çıkar:

1. **Maliyet metriği tek başına asla okunmaz** — mutlaka bir kalite metriğiyle (eval) yan yana konur.
   Token grafiği + eval skoru **aynı panoda** olmalı; ayrı panolarda ikisi de yalan söyler.
   ([`evals-llm-observability.md`](evals-llm-observability.md) §7 bu birleştirmeyi anlatıyor.)
2. **Öz-bildirim (self-report) tuzağına dikkat.** "AI beni %30 hızlandırdı" ya da "token %40 düştü"
   türü ifadeler, ölçüm gibi görünen **algılardır** — ölçüm değil. Sistemin ürettiği sayı bile, hangi
   soruya cevap verdiği tanımlanmadan bir şey söylemez. Bu yüzden hangi sayının neyi kanıtladığını
   **önceden** yazmak gerekir; sayıyı görüp sonra hikâye uydurmak değil.
   Bu tuzağın ölçülmüş hali için: [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) — geliştiriciler
   yavaşladıkları bir deneyden sonra bile hızlandıklarına inanmıştı. Maliyet sayını okurken aynı
   refleks iş başında: **ucuzladığını görmek istersen ucuzlamış gibi okursun.**

> **Özet:** `AI_KULLANIM` sana *ne kadar harcadığını* söyler. *Karşılığında ne aldığını* yalnızca eval
> söyler. FinOps'un dört domain'inden ikisi zaten bunlardır: **Understand Usage & Cost** (harcama) ve
> **Quantify Business Value** (karşılık). Birincisi olmadan kör uçarsın; ikincisi olmadan **ucuz ve
> işe yaramaz** bir sisteme doğru optimize edersin.

#### 7.1. Maliyeti çağrı noktasında damgala — ve dört token katmanı
Yukarıdaki ölçümlerin hepsi bir şarta bağlıdır: her çağrının **kimin, hangi iş için, hangi rotada**
yapıldığını bilmek. Bu bilgi ya çağrı anında kayda yazılır ya da hiç yazılmaz; sonradan yapılan
atıf (retroactive attribution) her zaman bazı durumları kaçırır. Üç boyut:

- **Kullanıcı başına** (`user_id`) — kim neye mal oluyor,
- **İş/rota başına** (`task_id` + model) — hangi ürün yüzeyi neye mal oluyor,
- **Kiracı başına** (`tenant_id`) — hangi müşteri kârlı (çok müşterili üründe).

**Bu projedeki bağ.** `OgretmenAiClient.AiIstek` her çağrıda bir `Islem` taşır (DEGERLENDIRME,
SOHBET, QUIZ…; enum'un kendi yorumu "maliyet raporunun etiketi (ADR-0003)" der) ve
`SpringAiOgretmenClient` kaydı çağrının yapıldığı yerde, model adıyla birlikte `ai_kullanim`'a
yazar. Yani iş + rota damgası **ilk günden** vardı. Kullanıcı damgası ise sonradan geldi: Faz 9b'de
`ai_kullanim.kullanici_id` sütunu eklendi ve eski satırlar varsayılan `yerel` değerini aldı
(ADR-0012 §10). O satırların kime ait olduğu artık bilinemez — "geriye dönük atıf her zaman eksik
kalır" kuralının bu depodaki küçük kanıtı. Kiracı boyutu tek şirketlik sitede gereksizdir.

**Dört token katmanı.** Girdi/çıktı ayrımı (§7) gereklidir ama yeterli değildir; girdinin kendisi
farklı nedenlerle büyüyen üç parçadan oluşur:

| Katman | Örnek | Büyürse ne demek |
|---|---|---|
| Prompt | sistem prompt'u + kullanıcının mesajı | talimat/şablon şişti |
| Araç (tool) | araç çağrısı sonuçlarının modele geri verilmesi | ajan çok araç çağırıyor ya da sonuçlar kırpılmıyor |
| Hafıza (memory) | sohbet geçmişi, getirilen belgeler, öğrenci hafızası | bağlam birikiyor |
| Cevap (response) | modelin çıktısı | format/uzunluk problemi |

Dördü tek kovaya girerse hangisinin büyüdüğünü göremezsin. Bu depoda `ai_kullanim` yalnız girdi ve
çıktı tutar; web yüzünde araç çağrısı yok, ama **hafıza** var: sohbet son 20 mesajı
(`SohbetService`) ve öğrencinin `OGRENCI.md` hafızasını bağlama ekler. Hafıza büyüdükçe girdi token'ı
artar ve pano bunu "prompt şişti" diye gösterir. Katmanı ayırmanın en ucuz yolu, prompt'u kuran
yerde (`BaglamServisi` + `PromptFactory`) parça uzunluklarını ayrı ayrı loglamaktır.

#### 7.2. Oran sınırı → harcama tavanı → kill switch — ve bu sitenin "kota YOK" kararı
Derslerin önerdiği yaptırım merdiveni (enforcement ladder) üç basamaklıdır: (1) kiracı/kullanıcı
başına **hız sınırı** (beklenen tepenin 2–3 katı; aşılırsa 429), (2) günlük **harcama tavanı**
(aşılırsa sınır sıkılaşır, uyarı gider), (3) olağandışı harcamada (ör. kişinin kendi tabanından çok
sapan bir gün) **kill switch** — otomatik durdurma + nöbetçiye haber. Eşiklerin nasıl seçildiği,
kill switch'in nasıl tetiklendiği ve ajan bağlamındaki operasyonel ayrıntı için:
[`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md). Burada tek soru şudur:
bu merdiven **bu depoya** nasıl oturur?

Çok kullanıcılı site (ADR-0012) bilinçli bir kullanıcı kararı taşır: "şirketin tek OpenRouter
anahtarı, **kota yok**." ADR'nin başarısızlık ölçütü de bellidir: "pera dışından birinin kayıt olup
şirket anahtarını harcaması." Yani bu karar "harcama önemsiz" demiyor; **kişi başı tavanı**
reddediyor ve kötüye kullanım riskini **kimlik** katmanında kapatıyor: yalnız `@pera.software`,
kayıt ekip koduyla (ADR-0015), kimliksiz model çağrısı yok (`SpringAiOgretmenClient` kimliği
çağrıdan önce çözer). 50–60 kişilik, kimliği bilinen bir iç kitlede kişi başı kota çoğu zaman
öğrenmeyi cezalandırır; bu karar savunulabilirdir.

Kota yokken **hangi korkuluk yine de anlamlı?** Merdivenin her basamağını "kota mı, değil mi"
sorusuyla ayır:

| Korkuluk | Kota mı? | Bu depoda | Yorum |
|---|---|---|---|
| Çağrı başına tavan (`max_tokens`, sınırlı yeniden deneme) | Hayır | **Var** (`max_tokens`: ADR-0003; 3 deneme / 30 sn: `application.yml`) | Tek bir çağrının kaçmasını önler; kotayla çelişmez |
| Toplam harcama tavanı (gateway'de) | Hayır — kişiye değil şirkete | **Runbook'ta**: aylık tavan OpenRouter panelinden (`docs/kurulum-site.md` §8.3); aşılınca 402 → `KREDI_BITTI` | Şirketi korur; ama aşılınca **herkes** durur (geniş patlama yarıçapı) |
| Olağandışılık uyarısı (kişi başı günlük toplam) | Hayır — engellemez, haber verir | **Yok**; veri var (`ai_kullanim` kullanıcı + işlem) | En düşük sürtünmeli eksik; "kota yok" ile tam uyumlu |
| Yüksek hız sınırı (insanın ulaşamayacağı, döngünün ulaşacağı) | Hayır — toplamı değil hızı sınırlar | **Yok** (AI uçlarında) | Takılan bir sekme/betik döngüsüne karşı; ADR ile karar verilmeli |
| Kill switch | — | **Kaba**: anahtarı kaldırıp yeniden başlatınca AI uçları 503, site açık kalır (`AiYapilandirma`) | Çalışır ama dakikalar sürer, tümden kapatır |
| Kişi başı günlük/aylık token kotası | **Evet** | Bilinçli olarak yok (ADR-0012) | Kullanıcı kararı |

Sonuç: "kota YOK" kararı merdivenin **kişiye özel toplam tavanını** kaldırır, merdivenin kendisini
değil. Çağrı başına tavan ve gateway'deki şirket tavanı zaten var; en ucuz eksik basamak
**engellemeyen bir olağandışılık uyarısıdır** (ör. `ai_kullanim` üzerinden günlük kişi başı toplamın
o kişinin olağan düzeyinden çok sapması). Yüksek bir hız sınırı ise kota değil bir **hata
korkuluğudur**; eklenecekse bu ayrım bir ADR'ye yazılmalı ki ileride "kotayı gizlice getirdik" diye
okunmasın. Bunlar bir reçete değil, kaydedilecek kararlardır.

## Diyagram 3 — Çağrı başına maliyet vs başarılı çıktı başına maliyet
Solda ucuz model üç denemede tutturuyor; sağda pahalı model bir denemede. Çağrı başına metrik solu
seçtirir; başarılı çıktı başına metrik gerçeği gösterir.

```svg
<svg viewBox="0 0 640 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Çağrı başına maliyet ile başarılı çıktı başına maliyet karşılaştırması: solda ucuz model çağrı başına düşük maliyetli ama aynı işi bitirmek için üç deneme gerekiyor, sağda pahalı model çağrı başına yüksek maliyetli ama tek denemede başarılı oluyor; çağrı başına bakan yanıltıcı metrik ucuz modeli seçtirir, başarılı çıktı başına bakan doğru metrik toplam işi kıyaslar">
  <defs>
    <marker id="fo-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
    <marker id="fo-3b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yanıltıcı Metrik — Çağrı Başına vs Başarılı Çıktı Başına</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Aynı iş: bir atölye değerlendirmesi üret · ikisi de sonunda başarır</text>

  <line x1="320" y1="60" x2="320" y2="252" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>

  <text x="160" y="80" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B3402F" font-family="system-ui">UCUZ MODEL</text>
  <text x="160" y="95" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">çağrı başına düşük · 3 deneme</text>
  <rect x="34" y="108" width="80" height="34" rx="7" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.4"/>
  <text x="74" y="124" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">Deneme 1</text>
  <text x="74" y="136" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">başarısız</text>
  <rect x="120" y="108" width="80" height="34" rx="7" fill="#F7E9E7" stroke="#B3402F" stroke-width="1.4"/>
  <text x="160" y="124" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">Deneme 2</text>
  <text x="160" y="136" text-anchor="middle" font-size="8.5" fill="#B3402F" font-family="system-ui">başarısız</text>
  <rect x="206" y="108" width="80" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="246" y="124" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Deneme 3</text>
  <text x="246" y="136" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">başarılı</text>
  <line x1="114" y1="125" x2="118" y2="125" stroke="#B3402F" stroke-width="1.6"/>
  <line x1="200" y1="125" x2="204" y2="125" stroke="#B3402F" stroke-width="1.6"/>

  <rect x="34" y="158" width="252" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="160" y="179" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1 başarılı çıktı için: 3 çağrı ödendi</text>

  <text x="480" y="80" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">PAHALI MODEL</text>
  <text x="480" y="95" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">çağrı başına yüksek · 1 deneme</text>
  <rect x="440" y="108" width="80" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="480" y="124" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Deneme 1</text>
  <text x="480" y="136" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">başarılı</text>

  <rect x="354" y="158" width="252" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="480" y="179" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1 başarılı çıktı için: 1 çağrı ödendi</text>

  <rect x="34" y="208" width="252" height="40" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="160" y="224" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">Çağrı başına: UCUZ görünür ✓</text>
  <text x="160" y="240" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">grafik bu modeli seçtirir</text>
  <rect x="354" y="208" width="252" height="40" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="480" y="224" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">Çağrı başına: PAHALI görünür ✗</text>
  <text x="480" y="240" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">grafik bu modeli eletir</text>

  <rect x="80" y="272" width="480" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="320" y="291" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Doğru metrik: BAŞARILI ÇIKTI BAŞINA MALİYET</text>
  <text x="320" y="307" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">= toplam token maliyeti ÷ eval'ı geçen çıktı sayısı — deneme sayısını içerir</text>
  <text x="320" y="319" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">gecikme ve insan müdahalesi de ucuz modelin gizli faturasıdır</text>
  <line x1="160" y1="248" x2="200" y2="270" stroke="#B3402F" stroke-width="1.6" marker-end="url(#fo-3a)"/>
  <line x1="480" y1="248" x2="440" y2="270" stroke="#25804C" stroke-width="1.6" marker-end="url(#fo-3b)"/>
</svg>
```

## Öz-sınav (kendini test et)
1. "Model seçimi bir maliyet mimarisi kararıdır" cümlesini, Hafta 5-6'nın **mimari nitelik (quality
   attribute)** kavramıyla bağla. Maliyetin gecikmeden (latency) farkı var mı — ikisi de sonradan
   "optimize edilebilir" mi?
2. FinOps Foundation hangi kuruluşun projesidir, ve **"FinOps for AI"**'ın Framework'te Public Cloud /
   SaaS / Data Center ile **aynı seviyede** bir teknoloji kategorisi olması neyi ima eder? Dört domain'i
   say.
3. **19 Mart 2026** Framework güncellemesinde kategorinin **korunmuş** olması neden bir kalıcılık
   kanıtıdır? Bir standart gövdesinin bir kategoriyi deprecate etmemesinden ne çıkarırsın, ne çıkaramazsın?
4. FinOps'un "AI maliyeti teknoloji kategorisi sınırlarını aşar" tespitini, **kesişim kaygısı
   (cross-cutting concern)** tanımına bağla. Neden bu, tanımı gereği mimarın işi olur — ve maliyet bu
   açıdan güvenlik/gözlemlenebilirlikle nasıl aynı sınıfa girer?
5. **⚠️** "FinOps for AI kurumsal olarak formalize edilmiş ama pratiği olgunlaşmakta" ifadesini savun.
   FinOps X 2026 keynote başlığı bu iddiayı nasıl destekler? **Olgunlaşmamışlık neden hype demek
   değildir** — ikisini ayıran şey nedir?
6. Model seçiminin beş eksenini say (yetenek · gecikme · maliyet · gizlilik/veri ikametgâhı · satıcı
   bağımlılığı). **ADR-0004** (OpenRouter) bu eksenlerden hangisinde bilinçli bir takas yapıyor, ve
   bedeli ne?
7. "Ucuz model daha çok deneme üretirse gerçek maliyet artar" — bunu **başarılı çıktı başına maliyet
   (cost per successful output)** metriğiyle açıkla. Çağrı başına bakan bir gösterge panosu, `Diyagram
   3`'teki iki modelden hangisini seçtirir ve neden yanılır?
8. Girdi ve çıktı token'ı neden **ayrı** izlenir? Hangisi tipik olarak daha pahalıdır ve **neden**
   (üretimin sırayla yapılması)? "Prompt'u kısaltmak" ile "cevabı kısaltmak"tan hangisi genelde daha
   çok kazandırır — ve şişen girdi ile şişen çıktı hangi iki **farklı** mühendislik problemine
   işaret eder?
9. Beş maliyet kaldıracını (önbellek · batch · küçük model · yönlendirme · kırpma) "pahalı olan ne?"
   sorusuyla eşle. Prompt önbelleğinin çalışması neden **tam önek eşleşmesine** bağlıdır, sistem
   promptuna gömülen bir **zaman damgası** neden önbelleği tamamen öldürür, ve önbellek stratejisi
   neden bir sağlayıcıdan diğerine olduğu gibi taşınamaz? Çalıştığını nasıl **doğrularsın**?
10. Yönlendirme (routing) bir maliyet kaldıracıyken aynı zamanda nasıl **yeni bir maliyet riski**
    doğurur? Yanlış yönlendirmenin faturası neden çift çıkar, ve yönlendirme kuralının doğru
    olduğunu hangi ölçümle kontrol edersin?
11. **Bu projenin iki ekonomisini** karşılaştır: web arayüzü (8090) OpenRouter'ı `OgretmenAiClient`
    üzerinden çağırırken faturayı kim öder, MCP sunucusunda (ADR-0007) kim öder? "N kullanıcı = N ×
    maliyet" ile "N kullanıcı = sabit maliyet" farkını yaratan mimari karar tam olarak nedir?
12. Web arayüzüne 10x daha ucuz bir model takarsan, MCP'nin maliyet eğrisine ulaşır mısın? Cevabını
    eğrinin **eğimi** üzerinden gerekçelendir. MCP kararı maliyeti **yok mu ediyor**, yoksa
    **taşıyor** mu — ve karşılığında neyden feragat ediliyor?
13. `KullanimService.istatistik()` "bu hafta token %40 düştü" diyor. Bunun **iyi mi kötü mü** olduğunu
    bu sayıdan bilebilir misin? En az üç zıt açıklama üret (biri kalite regresyonu olsun) ve
    "ölçmediğini yönetemezsin **ama ölçtüğünü de yorumlaman gerek**" kuralını `AI_KULLANIM` +
    `kullanim_istatistik` üzerinden savun.
14. Prompt önbelleği (sağlayıcı, L2) ile anlamsal önbellek (uygulama, L1) arasındaki fark nedir —
    hangisi **yanlış cevap** verebilir, neden? Bu depoda sohbet için kullanıcıdan bağımsız anahtarlı
    bir anlamsal önbellek kurulsaydı ADR-0012'nin hangi başarısızlık ölçütü ihlal edilirdi?
    *Cevap:* L2 yalnız aynı öneki ucuza işler, model yine çalışır; L1 modeli atlayıp kayıtlı cevabı
    döner ve gevşek eşikte benzer-ama-farklı soruya yanlış cevap verir. Sohbet prompt'u öğrencinin
    kendi hafızasını taşıdığı için A'nın cevabı B'ye gider: "bir kullanıcının başkasının verisini
    görmesi."
15. Bir ajan aynı sistem prompt'uyla 10 çağrıyı paralel başlatıyor ve önbellek okumaları sıfır
    çıkıyor. Ne oluyor, nasıl düzeltirsin? Haftalık raporun senkron mu batch mi olacağına hangi
    soruyla karar verirsin?
    *Cevap:* Hepsi ilk yazma bitmeden gelir: 10 yazma, 0 okuma. Önce tek çağrı, önbellek dolunca
    kalan 9'u paralel. Rapor için "24 saat bu kullanıcı için ne demek?" — "üretim" olması değil
    SLA'sı belirler.
16. Sitede kişi başı kota yok (ADR-0012). Yine de anlamlı olan iki korkuluğu say ve neden kotayla
    çelişmediklerini açıkla. Çağrı noktasında damgalamanın bu depodaki kanıtı nedir (hangi sütun
    sonradan geldi, eski satırlara ne oldu)?
    *Cevap:* Ör. çağrı başına tavan (`max_tokens` + sınırlı deneme) ve engellemeyen olağandışılık
    uyarısı; ikisi de kişinin toplam kullanımını sınırlamaz. (Gateway'deki şirket tavanı da var ama
    aşılınca herkesi durdurur.) `Islem` + model ilk günden damgalı; `kullanici_id` Faz 9b'de
    eklendi, eski satırlar `yerel` oldu — kimin olduğu artık bilinemez.

## Kaynaklar
- **FinOps for AI — teknoloji kategorisi (FinOps Framework):**
  <https://www.finops.org/framework/technology-categories/ai/>
- **2026 FinOps Framework güncellemesi (19 Mart 2026):**
  <https://www.finops.org/insights/2026-finops-framework/>
- **FinOps X 2026 — 1. Gün açılış konuşması ("The Great Token Panic and Is FinOps Dead?!"):**
  <https://www.finops.org/insights/finops-x-2026-day-1-keynote/>
- **AI Engineering from Scratch** (Rohit Ghumare, MIT Lisansı, Copyright (c) 2026 Rohit Ghumare) —
  <https://github.com/rohitg00/ai-engineering-from-scratch> — §5.3, §7.1 ve §7.2 şu derslerden
  sentezlendi (çeviri değil; rakamlar metin içinde şerhli): Faz 11 · 11 *Caching, Rate Limiting &
  Cost Optimization*; Faz 17 · 14 *Prompt & Semantic Caching*; Faz 17 · 15 *Batch APIs*; Faz 17 · 16
  *Model Routing*; Faz 17 · 27 *FinOps for LLMs*. Bu deponun vakası (`RaporService`, `ai_kullanim`,
  ADR-0003/0012/0015, `docs/kurulum-site.md` §8.3) eklendi.

### Bu programdaki ilgili kaynaklar
- [`evals-llm-observability.md`](evals-llm-observability.md) — başarılı çıktı başına maliyet metriği,
  eval + maliyet birleşimi, drift izleme.
- [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) — **kardeş kaynak**: bu özet *ne kadara mal
  oluyor*u, o *işe yarıyor mu*yu sorar. Ölçtüğün sayıyı yanlış okumanın ölçülmüş kanıtı orada.
- [`mcp-adapter.md`](mcp-adapter.md) — MCP'nin adaptör olarak mimarisi (Hafta 9).
- `docs/adr/0004-openrouter-saglayici.md` · `docs/adr/0007-mcp-sunucusu.md` — bu özetteki iki ekonomi
  kararının kayıtları.
- [`llm-uretim-yolu.md`](llm-uretim-yolu.md) — aynı katmanın canlı işletim yüzü: gecikme SLO'su, AI
  gateway, yedek model zinciri, güvenli yayın.
- [`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md) — harcama tavanı, hız
  limiti ve kill switch'in operasyonel ayrıntısı (§7.2).
