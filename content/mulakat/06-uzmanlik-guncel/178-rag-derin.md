---
slug: rag-derin
baslik: "RAG Derinlemesine: Chunking, Embedding, Hybrid Search (BM25+Vektör, RRF), Reranking ve Guardrail"
kademe: Uzman
sira: 178
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Açık kitap sınavını düşün. Sorunun cevabını ezberlemiş olman gerekmez; kitapta doğru sayfayı bulup önüne koyarsan cevabı oradan yazarsın. Yanlış sayfayı açarsan, ne kadar iyi yazarsan yaz, cevap yanlış olur. **RAG** (Retrieval-Augmented Generation — getirmeyle zenginleştirilmiş üretim) yapay zekâ modeline tam bunu yaptırır: modeli yeniden eğitmeden, sorulan soruya ilgili belge parçalarını bulup modelin önüne koyar, cevabı o parçalara dayandırır. Böylece kurum içi, güncel veya gizli bilgi, modelin eğitimine hiç girmemiş olsa bile kullanılabilir.

### Temel kavramlar

- **Büyük dil modeli (LLM)**: Metnin devamını olasılığa göre üreten program. Eğitim verisinde olmayan bir şeyi bilmez; bilmediğini uydurabilir.
- **Uydurma (halüsinasyon / hallucination)**: Modelin doğruymuş gibi duran, aslında olmayan bilgi üretmesi.
- **Parça (chunk)**: Belgenin, arama birimi olacak şekilde bölünmüş küçük kısmı. Metni bu şekilde bölme işine **parçalama (chunking)** denir. Getirilen şey belgenin tamamı değil, bu parçalardır.
- **Vektör**: Bir şeyi sayı listesiyle temsil etme (ör. `[0.21, -0.04, 0.88, ...]`). Bir noktanın uzaydaki koordinatları gibi düşün.
- **Sayısal vektör temsili (embedding / gömme)**: Bir metni, **anlamını** temsil eden vektöre çeviren model. Anlamca yakın metinler uzayda birbirine yakın noktalara düşer.
- **Benzerlik**: İki vektörün ne kadar yakın olduğunun ölçüsü (en yaygını **kosinüs benzerliği**: aradaki açının küçük olması).
- **BM25**: Klasik anahtar-kelime araması; kelimenin belgede kaç kez geçtiğine ve ne kadar nadir olduğuna bakar. Anlamı değil, **kelimeyi** eşler.
- **Reranking (yeniden sıralama)**: Kabaca bulunan adayları, soru ile belgeyi birlikte okuyan daha pahalı bir modelle (**cross-encoder**) yeniden sıralamak.
- **Guardrail (koruma bandı)**: Modelin kanıt dışına çıkmasını engelleyen kurallar ve kontroller.

### Nasıl çalışır?

1. **Parçala (chunk)**: Belgeleri anlamlı birimlere böl; başlık/paragraf sınırlarına saygı göster, sınırdaki bağlamı korumak için biraz **örtüşme (overlap)** bırak.
2. **Göm (embed)**: Her parçayı embedding modeliyle vektöre çevirip vektör veritabanına yaz. Bu adıma **indeksleme** denir.
3. **Geri getir (retrieval)**: Soru da **aynı** gömme modeliyle vektöre çevrilir ve en yakın parçalar bulunur. Yanına **BM25** araması da eklenirse buna **melez arama (hybrid search)** denir.
4. **Birleştir**: İki listeyi **RRF** (Reciprocal Rank Fusion) ile kaynaştır: her belge için `skor = Σ 1/(k + sıra)`, k tipik olarak 60.
5. **Sırala (rerank)**: Cross-encoder ile ilk k adayı yeniden sırala, en ilgiliyi öne al.
6. **Zenginleştir ve ürettir**: Seçilen parçaları isteme koy; modelden yalnız bunlara dayanan, kaynak gösteren bir cevap iste. Cevabı kanıta bağlamanın adı **kaynağa dayama (grounding)**'dır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RAG hattı: hybrid retrieval, RRF, rerank, üretim">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">RAG hattı: getir → birleştir → sırala → ürettir</text>
  <rect x="24" y="46" width="120" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">BM25</text>
  <text x="84" y="88" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sözlüksel</text>
  <rect x="24" y="120" width="120" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="84" y="144" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Vektör</text>
  <text x="84" y="162" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">anlamsal</text>
  <path d="M148 84 L196 104" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M196 104 l-10 -1 l4 9 z" fill="#5D7789"/>
  <path d="M148 138 L196 118" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M196 118 l-4 -9 l-6 8 z" fill="#5D7789"/>
  <rect x="200" y="86" width="110" height="52" rx="9" fill="#FBEEE6" stroke="#C8722B" stroke-width="1.5"/>
  <text x="255" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#C8722B" font-family="system-ui">RRF birleştir</text>
  <text x="255" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Σ 1/(k+rank)</text>
  <path d="M310 112 L356 112" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M356 112 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="360" y="86" width="110" height="52" rx="9" fill="#F0EAF7" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="415" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Rerank</text>
  <text x="415" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">cross-encoder</text>
  <path d="M470 112 L516 112" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M516 112 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="520" y="86" width="98" height="52" rx="9" fill="#FBF1F1" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="569" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Üretim</text>
  <text x="569" y="122" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">+ atıf/guardrail</text>
  <text x="320" y="200" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">zincir en zayıf halkası kadar güçlü: her adım ölçülür</text>
</svg>
```

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Chunking bir dengedir: çok büyük gürültü, çok küçük bağlam kaybı, örtüşmeli anlamsal denge">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Chunking: parça boyutu bir dengedir</text>
  <rect x="24" y="48" width="192" height="134" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="120" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Çok büyük parça</text>
  <rect x="44" y="82" width="152" height="58" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="120" y="160" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">gürültü + maliyet</text>
  <rect x="228" y="48" width="184" height="134" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Çok küçük parça</text>
  <rect x="248" y="86" width="46" height="16" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <rect x="300" y="86" width="46" height="16" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <rect x="352" y="86" width="46" height="16" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <rect x="248" y="110" width="46" height="16" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <rect x="300" y="110" width="46" height="16" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="320" y="160" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">bağlam kaybı</text>
  <rect x="424" y="48" width="192" height="134" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="520" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Anlamsal + örtüşme</text>
  <rect x="444" y="84" width="130" height="26" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="466" y="104" width="130" height="26" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="520" y="160" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">denge — isabetli</text>
</svg>
```

### Mülakatta ne sorulur?

**"Parça boyutunu nasıl seçersin?"** Bu bir **takastır**. Çok büyük parça birden çok konuyu karıştırır; gürültü getirir ve boşuna jeton harcatır. Çok küçük parça ise cümleyi bağlamından koparır ve anlamı kaybettirir. En iyi boyut, alana ve sorgu tipine göre ölçülerek bulunur.

**"Neden hem BM25 hem vektör?"** İkisi **farklı hataları** yakalar. BM25 nadir terimi, tam kod/ürün/hata adını iyi bulur ama eş anlamı kaçırır. Vektör araması eş anlamı ve niyeti yakalar ama nadir terimde zayıflayabilir. Birleşim, her birinin kör noktasını kapatır ve **bulma oranını (recall — gerçekten ilgili belgelerin yüzde kaçını yakaladığın)** yükseltir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hybrid search: BM25 sözlüksel ile vektör anlamsal arama tamamlayıcıdır, birleşim recall artırır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hybrid: BM25 ve vektör tamamlayıcıdır</text>
  <ellipse cx="258" cy="118" rx="150" ry="72" fill="#EAF1F7" stroke="#186399" stroke-width="1.5" fill-opacity="0.65"/>
  <ellipse cx="382" cy="118" rx="150" ry="72" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5" fill-opacity="0.65"/>
  <text x="168" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">BM25</text>
  <text x="168" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sözlüksel</text>
  <text x="168" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">nadir terim · tam eşleşme</text>
  <text x="472" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Vektör</text>
  <text x="472" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">anlamsal</text>
  <text x="472" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eş anlam · niyet</text>
  <text x="320" y="112" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">RRF</text>
  <text x="320" y="128" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">recall ↑</text>
  <text x="320" y="202" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her yöntem diğerinin kör noktasını kapatır</text>
</svg>
```

**"Neden RRF, neden ham skor toplamı değil?"** BM25 skoru ile kosinüs benzerliği **farklı ölçeklerdedir**; toplamak veya normalize etmek kırılgandır. RRF skor yerine **sıraya** bakar, bu yüzden ölçek sorunu ortadan kalkar.

**"Gömme modelini değiştirirsek?"** Kritik nokta şudur: sorgu ile belgeler **aynı vektör uzayında** olmalıdır. Model ya da sürümü değişirse eski vektörler yeni sorgu vektörleriyle kıyaslanamaz. Bu durumda **tüm koleksiyon yeniden indekslenmelidir**; yani sürüm ve maliyet planlaması gerektiren bir karardır. Metadata alanları (dil, tarih, yetki) da parçalarla birlikte saklanır ki filtreli arama yapılabilsin.

**"RAG uydurmayı bitirir mi?"** Hayır — bu satıcı iddiasına şerh koy. RAG uydurmayı **azaltır**, ortadan kaldırmaz: model doğru kanıt önündeyken bile kanıtın ötesine geçebilir. Bir koruma bandı seti gerekir: yalnız verilen bağlama dayan, bilgi yoksa "bilmiyorum" de, her iddiaya **kaynak/atıf** ver, bağlam boş veya alakasızsa üretimi hiç başlatma.

**"Kaliteyi nasıl ölçersin?"** RAG bir zincirdir; uçtan uca beğeni ölçmek hatanın nerede olduğunu gizler. Geri getirme katmanı ayrı ölçülür: **recall@k** (ilk k sonuçta doğru belgeyi yakalama oranı), **MRR** (doğru belge ortalama kaçıncı sırada çıkıyor), **nDCG** (sıralamanın kalitesi). Düşük bulma oranı parçalama, gömme veya arama sorununu gösterir. Bulma oranı iyi olduğu hâlde cevap kötüyse sorun üretim ya da koruma bandı katmanındadır.

### Tek cümlede

RAG sihir değil bir bilgi getirme (information retrieval) problemidir: akıllı parçala, uygun gömme modeli seç, melez arama ve yeniden sıralama ile alakayı yükselt, modeli kanıta ve atfa bağla.

# ragd-1 | İleri
## Soru
RAG hattında "chunking" (parçalama) stratejisi neden doğrudan cevap kalitesini etkiler?
## A
Chunk boyutu yalnızca depolama maliyetini belirler; alaka ve sıralamayla hiçbir ilgisi yoktur
🤖 Chunk boyutu doğrudan getirme alakasını belirler; salt depolama meselesi değildir.
## B
Chunk ne kadar büyükse cevap her koşulda o kadar iyi olur; küçültmenin faydası yoktur
🤖 Aşırı büyük parça birden çok konuyu karıştırır, gürültü getirir; "büyük daima iyi" yanlıştır.
## C
Arama birimi chunk'tır: çok büyük parça gürültü, çok küçük parça bağlam kaybı getirir
🤖 Doğru: getirilen birim chunk olduğundan boyut ve sınır seçimi neyin bağlama gireceğini belirler.
## D
Parçalama yalnızca embedding modelinin çalışmasını hızlandırmak için yapılan bir optimizasyondur
🤖 Amaç hız değil; parçalama, aranabilir anlamlı birimler üretip getirme isabetini belirler.
## Doğru
C
## Açıklama
Getirmenin **arama birimi chunk'tır**; dolayısıyla parçalama, neyin bulunup bağlama gireceğini doğrudan belirler (C). Çok büyük parçalar birden çok konuyu karıştırıp gürültü ve gereksiz token getirir; çok küçük parçalar cümleyi bağlamından koparıp anlamı kaybettirir. İyi strateji **anlamsal/başlık sınırlarına saygılı** böler ve sınırdaki bağlamı korumak için **örtüşme (overlap)** kullanır. Boyutu salt depolamaya (A) veya hıza (D) indirgemek yanlıştır; "büyük daima iyi" (B) de yanlıştır — optimum, alana ve sorgu tipine göre ayarlanan bir dengedir.

# ragd-2 | Uzman
## Soru
Hybrid search'te BM25 (sözlüksel) ile vektör (anlamsal) aramanın birleştirilmesinin temel gerekçesi nedir?
## A
BM25 modası geçmiş bir yöntemdir; yalnızca geriye dönük uyumluluk için hatır gönül eklenir
🤖 BM25 nadir terim ve tam eşleşmede hâlâ güçlüdür; anlamsal arama bunları kaçırabilir.
## B
İki yöntem farklı hataları yakalar; birleşim kör noktaları kapatıp recall'ı yükseltir
🤖 Doğru: sözlüksel ve anlamsal sinyaller tamamlayıcıdır, tek başına her biri bir şeyi kaçırır.
## C
Vektör arama her tür sorguda BM25'ten üstündür; birleşim yalnızca gereksiz maliyet ekler
🤖 Vektör; tam kod, ürün kodu veya nadir terim eşleşmesinde zayıf kalabilir, BM25 bu boşluğu kapatır.
## D
İki aramayı paralel çalıştırmanın amacı toplam sorgu gecikmesini yarıya indirmektir
🤖 İki arama çalıştırmak gecikmeyi genelde artırır; amaç hız değil, alaka ve recall artışıdır.
## Doğru
B
## Açıklama
**Hybrid search**, iki farklı sinyalin **tamamlayıcılığından** güç alır: **BM25** (sözlüksel/seyrek) nadir terimleri, tam anahtar-kelime, kod/isim/ID eşleşmesini iyi bulur ama eş anlamı kaçırır; **vektör** (anlamsal/yoğun) eş anlam ve niyeti yakalar ama tam nadir-terim eşleşmesinde zayıflayabilir. İkisini birleştirmek her birinin kör noktasını kapatır ve recall'ı artırır (B). BM25 eskimiş değildir (A), vektör her durumda üstün değildir (C) ve amaç gecikme düşürmek değildir (D) — iki arama genelde gecikmeyi artırır ama isabeti yükseltir.

# ragd-3 | Uzman
## Soru
Reciprocal Rank Fusion (RRF), iki farklı arama sonucunu birleştirirken hangi problemi zarifçe çözer?
## A
Embedding modelini gereksiz kılarak vektör arama adımını hattan tamamen kaldırır
🤖 RRF bir birleştirme yöntemidir; aramaları ortadan kaldırmaz, sonuç listelerini kaynaştırır.
## B
Sonuçları rastgele karıştırarak listeye çeşitlilik katar ve tekdüzeliği kırar
🤖 RRF rastgele değildir; sıra-temelli, deterministik bir füzyon formülüdür.
## C
Kıyaslanamaz skor ölçekleri sorununu, ham skor yerine SIRAYA dayanarak çözer
🤖 Doğru: skor = Σ 1/(k + sıra) olduğundan normalizasyona gerek kalmaz, ölçek farkı önemsizleşir.
## D
Tek bir arama sisteminin sonuçlarını kendi içinde tekrar sıralar; çoklu liste desteklemez
🤖 RRF'in tüm amacı birden çok sıralı listeyi birleştirmektir; tek liste için anlamsızdır.
## Doğru
C
## Açıklama
Farklı arama sistemlerinin **ham skorları kıyaslanamaz** ölçeklerdedir (BM25'in TF-IDF benzeri skoru ile kosinüs benzerliği aynı birimde değildir). Bunları toplamak veya normalize etmek kırılgandır. **RRF** bu sorunu, skor yerine **sıraya (rank)** dayanarak çözer: her belge için `skor = Σ 1/(k + sıra)` (k tipik olarak 60), yani belge her listede ne kadar üstteyse o kadar çok katkı alır (C). Normalizasyon gerekmez, uygulaması basit ve gürbüzdür. RRF aramaları ortadan kaldırmaz (A), rastgele değildir (B) ve tam olarak **çoklu liste** birleştirmek için vardır (D).

# ragd-4 | Uzman
## Soru
Bir RAG sistemi doğru belgeleri getirdiği hâlde model bazen bağlamda olmayan "uydurma" bilgi üretiyor. Halüsinasyonu azaltmak için en etkili guardrail seti hangisidir?
## A
Yalnız bağlama dayanmayı ve bilgi yoksa "bilmiyorum" demeyi dayatmak; atıf istemek
🤖 Doğru: kanıta bağlama, atıf zorunluluğu ve boş bağlamda durdurma dayanaksız üretimi sistemli biçimde kısar.
## B
Sıcaklığı maksimuma çıkarıp modelin daha yaratıcı ve akıcı cevaplar üretmesini sağlamak
🤖 Yüksek sıcaklık üretimi daha savruk ve dayanaksız yapar; halüsinasyonu azaltmaz, artırır.
## C
Getirmeyi tamamen kapatıp modelin eğitimden gelen kendi bilgisine güvenmesini sağlamak
🤖 Getirmeyi kapatmak dış kanıtı kaldırır; halüsinasyon riskini azaltmaz, doğrudan büyütür.
## D
Bağlama daha fazla belge doldurup modelin doğru bilgiyi bulma ihtimalini yükseltmek
🤖 İlgisiz doldurma gürültü ve "ortada kaybolma" yaratır; dayanaksız üretimi kolaylaştırır.
## Doğru
A
## Açıklama
Doğru belge getirilse bile model "kanıtın ötesine" geçebilir; bunu sınırlamak **guardrail** işidir. Etkili set: (1) talimatla modeli **yalnız verilen bağlama** dayanmaya ve bilgi yoksa **"bilmiyorum"** demeye zorlamak, (2) her iddiayı **kaynak/atıf** ile desteklettirmek (böylece dayanaksız iddia görünür olur), (3) bağlam boş veya alakasızsa üretimi **engellemek**, (4) çıktıyı kanıta karşı doğrulamak (A). Sıcaklığı yükseltmek (B), getirmeyi kapatmak (C) veya ilgisiz doldurma (D) halüsinasyonu artırır. Not: RAG halüsinasyonu **bitirmez, azaltır** — "RAG kurduk, halüsinasyon sorunu çözüldü" iddiası ölçümle değil temenniyle konuşur.

# ragd-5 | İleri
## Soru
Bir ekip "RAG kalitemizi iyileştirdik" diyor ama yalnız son cevabın kullanıcı beğenisini ölçüyor. Getirme (retrieval) katmanını ayrı değerlendirmek neden kritiktir?
## A
Getirme kalitesi doğası gereği ölçülemez; yalnızca son cevabın beğenisi değerlendirilebilir
🤖 Getirme bağımsız ölçülebilir: recall@k, MRR, nDCG gibi standart IR metrikleri tam bunun içindir.
## B
Ölçmeye gerek yoktur; üretim adımı kötü getirilmiş kanıtı kendi bilgisiyle telafi eder
🤖 Model kötü getirilen kanıttan iyi cevap üretemez; "çöp girdi, çöp çıktı" burada da geçerlidir.
## C
Getirme için yalnız gecikmeyi ölçmek yeterlidir; alaka üretim adımının sorunudur
🤖 Gecikme performanstır ama alaka olmadan doğru cevap gelmez; asıl kalite recall ve sıralamadır.
## D
Zincir en zayıf halkası kadar güçlüdür; katmanlı ölçüm darboğazın yerini ayırır
🤖 Doğru: uçtan uca beğeni hatanın nerede olduğunu gizler, recall@k/MRR/nDCG sorunu izole eder.
## Doğru
D
## Açıklama
RAG bir **zincirdir**; uçtan-uca kullanıcı beğenisi, hatanın **getirmede mi üretimde mi** olduğunu gizler. Model, alakasız kanıttan iyi cevap üretemez, bu yüzden **getirme ayrı ölçülmelidir**: **recall@k** (ilgili belge ilk k içinde mi), **MRR** (ilk ilgili sonucun sırası), **nDCG** (sıralama kalitesi) (D). Bu metrikler darboğazı izole eder — düşük recall parçalama/embedding/arama sorununu, iyi recall'a rağmen kötü cevap ise üretim/guardrail sorununu işaret eder. Getirmeyi ölçülemez saymak (A), üretimin her şeyi telafi ettiğini varsaymak (B) veya yalnız gecikmeye bakmak (C) yanlıştır.

# ragd-6 | Uzman
## Soru
Hybrid + RRF ile getirilen ilk 50 aday arasında doğru belge var ama sıralamada 30. sırada kalıyor ve modele giren ilk 5 parçaya ulaşamıyor. En uygun çözüm hangisidir?
## A
50 adayın hepsini bağlam penceresine doldurup seçimi tamamen modele bırakmak
🤖 50 adayı doldurmak gürültü ve "ortada kaybolma" yaratır; doğru belge yine gölgede kalabilir.
## B
Aday kümesini cross-encoder reranker ile yeniden sıralayıp en ilgiliyi öne almak
🤖 Doğru: reranking, ucuz getirmenin kaba sırasını hassas alaka skoruyla düzeltir.
## C
Yalnız sorgu tarafında daha güçlü bir embedding modeline geçip belgeleri olduğu gibi bırakmak
🤖 Sorgu ve belgeler aynı embedding uzayında olmalıdır; model değişirse tüm koleksiyon yeniden indekslenir.
## D
Soruyu kullanıcıya daha iyi kelimelerle yeniden yazdırmasını isteyerek sorunu ona devretmek
🤖 Kullanıcıya yük bırakmak sistemik çözüm değildir; sıralama darboğazı sistemde çözülür.
## Doğru
B
## Açıklama
İlk aşama getirme (BM25/vektör/RRF) **hızlı ama kaba** bir sıralama üretir; doğru belge aday kümesinde olsa da alt sıralarda kalabilir. **Reranking**, bu aday kümesini bir **cross-encoder** ile — soru ve belgeyi birlikte kodlayıp derin alaka skoru üreterek — yeniden sıralar ve en ilgiliyi ilk sıralara taşır (B). Böylece modele giren küçük bağlam gerçekten en ilgili parçalardan oluşur ve "ortada kaybolma" azalır. 50 adayı doldurmak (A) gürültü ekler; C'deki hamle ise ayrı bir tuzaktır: **sorgu ve belge aynı embedding uzayında olmak zorundadır**, embedding modeli değişince tüm koleksiyon yeniden indekslenmeden arama bozulur. Soruyu kullanıcıya yıkmak (D) sistematik çözüm değildir. Tipik desen: geniş getir → RRF birleştir → dar reranking → küçük, isabetli bağlam.
