---
slug: vector-db
baslik: "Vektör Veritabanları: ANN (HNSW/IVF), Benzerlik Metriği, Metadata Filtre ve pgvector Kararı"
kademe: İleri
sira: 179
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir şehirde "bana en yakın üç kafe" sorusunu düşün. Elinde herkesin koordinatı varsa cevabı bulabilirsin, ama tek tek bütün şehri gezerek değil — mahallelere bölünmüş bir harita ile. **Vektör veritabanı** aynı işi anlamlar için yapar: her metni bir "koordinat"a çevirip saklar ve "bu soruya anlamca en yakın parçalar hangileri?" sorusunu milisaniyeler içinde cevaplar. Klasik veritabanı "ad = 'Deniz'" gibi **kesin eşleşme** arar; vektör veritabanı ise **yakınlık** arar. RAG sistemlerinin bilgi deposu tam olarak budur.

### Temel kavramlar

- **Vektör**: Bir şeyi sayı listesiyle temsil etme, ör. `[0.12, -0.44, 0.90, ...]`. Uzaydaki bir noktanın koordinatları gibi düşün; tipik olarak 384-3072 sayı, yani "boyut" içerir.
- **Sayısal vektör temsili (embedding / gömme)**: Metni, anlamını temsil eden vektöre çeviren model. Anlamca yakın metinler yakın noktalara düşer.
- **Benzerlik metriği**: İki vektörün yakınlığını ölçme kuralı. **Kosinüs** aradaki açıya (yöne) bakar, **iç çarpım (dot product)** yön ve büyüklüğü birlikte hesaba katar, **Öklid (L2)** düz mesafeyi ölçer.
- **En yakın komşu araması (kNN)**: Sorgu vektörüne en yakın k kaydı bulma işi. **Kesin (exact)** yapılırsa tüm kayıtları taramak gerekir.
- **ANN (Approximate Nearest Neighbor)**: "Yaklaşık" en yakın komşu. Küçük bir doğruluk ödünüyle çok daha hızlı cevap verir.
- **Recall (bulma oranı)**: Gerçek en yakın komşuların yüzde kaçını yakaladığın. ANN'in ödün verdiği şey budur.
- **İndeks**: Aramayı hızlandıran veri yapısı. İki yaygın aile: **HNSW** (katmanlı çizge) ve **IVF** (kümelere bölme).
- **Metadata**: Vektörün yanında saklanan yapısal alanlar (dil, tarih, yetki, müşteri no) — filtreleme bunlarla yapılır.

### Nasıl çalışır?

1. Metinler parçalanır, embedding modeliyle vektöre çevrilir ve metadata'sıyla birlikte yazılır.
2. Veritabanı bu vektörler üzerinde bir **indeks** kurar: HNSW komşuluk çizgesi örer, IVF uzayı kümelere böler.
3. Sorgu geldiğinde o da **aynı** modelle vektöre çevrilir.
4. İndeks, tüm veriyi taramadan umut vaat eden bölgeye gider: HNSW komşudan komşuya atlar, IVF yalnız en yakın `nprobe` kümeyi tarar.
5. En yakın k aday, seçilen metrikle skorlanıp döndürülür; varsa metadata koşulu uygulanır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ANN hız-doğruluk dengesi ve indeks seçenekleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ANN: hız ile doğruluğu (recall) dengele</text>
  <rect x="30" y="48" width="180" height="130" rx="12" fill="#FBF1F1" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="120" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kesin (exact) kNN</text>
  <text x="120" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">recall = %100</text>
  <text x="120" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">lineer tarama</text>
  <text x="120" y="138" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">ölçekte çok yavaş</text>
  <rect x="230" y="48" width="180" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">HNSW</text>
  <text x="320" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çizge, hızlı + yüksek recall</text>
  <text x="320" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bellek yoğun</text>
  <text x="320" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">inşa maliyetli</text>
  <rect x="430" y="48" width="180" height="130" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="520" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">IVF</text>
  <text x="520" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kümele + nprobe tara</text>
  <text x="520" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bellekte ekonomik</text>
  <text x="520" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">nprobe ile ayarla</text>
</svg>
```

### Mülakatta ne sorulur?

**"Neden kesin arama değil?"** Yüksek boyutlu uzayda kesin kNN, her sorguda tüm vektörleri taramayı gerektirir (lineer tarama, O(n)). Milyonlarca vektörde bu gecikme kabul edilemez olur. ANN bilinçli bir **hız–bulma oranı takasıdır**: biraz doğruluk verir, çok hız alır. Sorgu anında `efSearch` (HNSW) veya `nprobe` (IVF) ile bu denge ayarlanır.

**"HNSW mi IVF mi?"** HNSW katmanlı bir çizge kurar; hızlı sorgu ve yüksek bulma oranı verir. Buna karşılık **bellek yoğundur** ve indeksin inşası maliyetlidir. IVF ise uzayı kümelere böler ve sorguda yalnız birkaç kümeyi tarar; **bellekte daha ekonomiktir** ve `nprobe` ile ayarlanabilir. `M` ve `efConstruction` gibi HNSW parametrelerini büyütmek bulma oranını yükseltir; ama belleği ve indeks inşa süresini de yükseltir. Bedava öğle yemeği yoktur.

**"Metriği nasıl seçersin?"** Metrik, "yakınlık" tanımının ta kendisidir ve **gömme modelinin eğitildiği metrikle eşleşmelidir**. Kosinüs için eğitilmiş bir modelde L2 kullanmak yakınlık geometrisini değiştirir ve sıralamayı bozabilir. Not: vektörler birim uzunluğa normalize edilirse kosinüs ile iç çarpım aynı sırayı verir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Metadata filtre: pre-filter önce koşul sonra ANN, post-filter önce ANN sonra koşul">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Metadata filtre: pre-filter vs post-filter</text>
  <rect x="24" y="48" width="150" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="99" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Filtre koşulu</text>
  <text x="99" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dil='tr' AND yil&gt;=2024</text>
  <path d="M174 71 L214 71" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M214 71 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="218" y="48" width="120" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="278" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">ANN arama</text>
  <text x="360" y="66" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Pre-filter</text>
  <text x="360" y="84" font-size="10.5" fill="#5D7789" font-family="system-ui">doğru — ama indeks desteği ister</text>
  <rect x="24" y="120" width="120" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="148" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">ANN arama</text>
  <path d="M144 143 L184 143" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M184 143 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="188" y="120" width="150" height="46" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="263" y="142" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Filtre koşulu</text>
  <text x="263" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonradan ele</text>
  <text x="360" y="138" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Post-filter</text>
  <text x="360" y="156" font-size="10.5" fill="#5D7789" font-family="system-ui">hızlı — ama sonuç kıtlığı riski</text>
  <text x="320" y="194" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">seçim: filtrenin seçiciliği + indeksin yeteneği belirler</text>
</svg>
```

**"Filtreyi nereye koyarsın?"** `dil='tr' AND yil>=2024` gibi bir koşulu ANN ile birleştirmenin iki yolu vardır. **Önce filtreleme (pre-filter)**: koşul önce uygulanır, ANN yalnız uygun alt kümede çalışır. Doğru sonuç verir, ama indeksin filtreli aramayı desteklemesini gerektirir ve dar filtrelerde maliyetli olabilir. **Sonra filtreleme (post-filter)**: önce ANN en yakın k adayı getirir, koşul sonra uygulanır. Basit ve hızlıdır; ama filtre adayların çoğunu elerse elinde yeterli sonuç kalmaz.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="pgvector orta ölçek için tek sistem, özel vektör DB devasa ölçek ve dağıtık ANN için">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">pgvector mi, özel vektör DB mi?</text>
  <rect x="28" y="46" width="270" height="118" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="163" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">pgvector</text>
  <text x="163" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">orta ölçek · Postgres içinde</text>
  <text x="163" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ACID · SQL join/filtre</text>
  <text x="163" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek sistem · düşük işletme yükü</text>
  <text x="163" y="152" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">güçlü ilişkisel ihtiyaçta ideal</text>
  <rect x="342" y="46" width="270" height="118" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="477" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Özel vektör DB</text>
  <text x="477" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Milvus · Qdrant · Weaviate</text>
  <text x="477" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">devasa ölçek · dağıtık ANN</text>
  <text x="477" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yüksek eşzamanlılık</text>
  <text x="477" y="152" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">ayrı sistem işletme maliyeti</text>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ölçek özel DB'yi zorlayana kadar mevcut Postgres'te kal</text>
</svg>
```

**"pgvector mi, özel vektör DB mi?"** Zaten Postgres kullanan, orta ölçekli (birkaç milyon vektör), güçlü ilişkisel filtre/join ve işlemsel tutarlılık isteyen bir uygulama için **pgvector** pragmatik seçimdir: tek sistem, ACID, SQL filtre. **Özel** vektör DB'ler (Milvus, Qdrant, Weaviate) asıl değerini devasa ölçek, dağıtık ANN ve yüksek eşzamanlılıkta gösterir — bedeli ayrı bir sistemi işletmek ve iki depo arasında tutarlılık köprüsü kurmaktır.

### Tek cümlede

Vektör veritabanı, anlamsal yakınlığı ölçeklenebilir kılan bir indeks meselesidir: metriği modele göre seç, indeksi hız/bulma oranı/bellek üçlemesine göre ayarla, filtreyi bilinçle önce ya da sonra uygula.

# vecdb-1 | Orta
## Soru
Milyonlarca embedding içinde arama yaparken kesin (exact) en-yakın-komşu yerine ANN (yaklaşık) kullanmanın temel gerekçesi nedir?
## A
Kesin arama ölçekte pahalıdır; ANN küçük bir recall ödünüyle çok daha hızlıdır
🤖 Doğru: ANN, ölçeklenebilir gecikme için ufak bir doğruluk kaybını kabul eder; problemin özü bu takastır.
## B
ANN, indeks yapısı sayesinde kesin aramadan daha yüksek recall üretir ve hiç komşu kaçırmaz
🤖 ANN tanımı gereği "yaklaşık"tır; recall'ı kesin aramanınkinden düşük olabilir, yüksek olamaz.
## C
ANN, vektörlerin boyut sayısını otomatik düşürerek karşılaştırma maliyetini sıfırlar
🤖 ANN boyut indirgeme yöntemi değildir; indeks yapısıyla arama uzayını daraltır, boyutu silmez.
## D
Yüksek boyutlu uzayda kesin arama matematiksel olarak imkânsızdır; tek seçenek ANN'dir
🤖 Kesin arama mümkündür ama ölçekte pahalıdır; ANN zorunluluk değil, pragmatik bir tercihtir.
## Doğru
A
## Açıklama
Yüksek-boyutlu uzayda **kesin** en-yakın-komşu, her sorgu için tüm vektörleri taramayı (lineer, O(n)) gerektirir; milyonlarca vektörde bu gecikme kabul edilemez olur. **ANN**, indeks yapılarıyla (HNSW, IVF…) arama uzayını akıllıca daraltarak **çok daha hızlı** yanıt verir; karşılığında bazı gerçek komşuları kaçırabilir — yani **recall'dan biraz ödün verilir** (A). Bu bilinçli bir **hız/doğruluk takasıdır**. ANN daha doğru değildir (B), boyut düşürmez (C) ve kesin arama imkânsız değildir (D) — sadece ölçekte pahalıdır. Sorgu anında `efSearch`/`nprobe` ile denge ayarlanır.

# vecdb-2 | Orta
## Soru
HNSW ile IVF indeksleri arasındaki temel karakter farkı hangisidir?
## A
HNSW yalnız CPU üzerinde, IVF ise yalnız GPU üzerinde çalışabilen algoritmalardır
🤖 İkisi de donanımdan bağımsız algoritmalardır; ayrım donanım değil, yapı ve takastır.
## B
İkisi aynı algoritmanın farklı kütüphanelerdeki isimleridir; davranışları özdeştir
🤖 Yapıları ve takasları farklıdır; biri çizge gezinmesi, diğeri küme tarama temellidir.
## C
HNSW katmanlı çizge kurar (hızlı, yüksek recall, bellek yoğun); IVF kümeler ve nprobe tarar
🤖 Doğru: HNSW çizge gezinmesiyle yüksek recall/bellek, IVF kümelemeyle ekonomi ve ayarlanabilirlik sunar.
## D
HNSW verinin ekleme sırasına göre sıralı olmasını gerektirir; IVF rastgele veri kabul etmez
🤖 İkisi de veride sıralılık koşulu aramaz; böyle bir kısıt yoktur.
## Doğru
C
## Açıklama
**HNSW**, vektörleri **katmanlı bir gezinilebilir çizgede** (navigable small world graph) düğüm olarak bağlar; sorgu üst katmandan aşağı doğru komşuları izleyerek hızla yakınsar. Yüksek recall ve düşük gecikme verir ama **bellek yoğundur** ve inşası maliyetlidir. **IVF**, uzayı önce **kümelere** (Voronoi hücreleri) böler; sorguda yalnız en yakın `nprobe` kümeyi tarar — **bellekte daha ekonomiktir** ve `nprobe` artırılınca recall (ve gecikme) yükselir (C). Donanım ayrımı (A), eşdeğerlik (B) veya sıralılık koşulu (D) yanlıştır. Seçim ölçek, bellek bütçesi ve hedef recall'a göre yapılır.

# vecdb-3 | İleri
## Soru
Bir embedding modeli, benzerliği kosinüs (cosine) ile ölçmek üzere eğitilmiş. Vektör DB'de yanlışlıkla Öklid (L2) metriği seçilirse ne olur?
## A
Hiçbir fark olmaz; tüm benzerlik metrikleri her veri kümesinde aynı sıralamayı üretir
🤖 Normalize edilmemiş vektörlerde metrik değişimi sıralamayı değiştirebilir; "her zaman aynı" yanlıştır.
## B
Yakınlık tanımı modelinkiyle uyuşmaz; alaka sıralaması bozulup getirme kalitesi düşebilir
🤖 Doğru: yanlış metrik "yakınlık" geometrisini değiştirir, model beklediği uzayda aranmamış olur.
## C
Veritabanı uyumsuzluğu algılayıp embedding modelini otomatik olarak yeniden eğitir
🤖 Metrik seçimi modeli eğitmez; yalnızca arama sırasında mesafenin nasıl hesaplandığını belirler.
## D
Öklid mesafesi matematiksel olarak daha kesin olduğundan sonuçların isabeti artar
🤖 "Her zaman üstün" bir metrik yoktur; doğru metrik modele ve vektörlerin normalize olup olmadığına bağlıdır.
## Doğru
B
## Açıklama
Benzerlik metriği, "iki vektör ne kadar yakın" sorusunun **tanımıdır**. Bir embedding modeli belirli bir metrik (çoğunlukla kosinüs veya iç çarpım) altında anlamlı olacak şekilde eğitilir. Yanlış metrik seçmek — ör. kosinüs için eğitilmiş modelde L2 kullanmak — "yakınlık" geometrisini değiştirir; normalize edilmemiş vektörlerde sıralama bozulur ve **getirme kalitesi düşer** (B). Not: vektörler birim uzunluğa normalize edilirse kosinüs ile iç çarpım aynı sırayı verir, ama L2 yine farklı davranabilir. "Tüm metrikler aynı" (A), "model yeniden eğitilir" (C) ve "Öklid her zaman üstün" (D) yanlıştır. Kural: metriği embedding modelinin dokümantasyonuna göre seç.

# vecdb-4 | İleri
## Soru
Bir sistem, vektör benzerliğini `dil='tr' AND yil>=2024` metadata koşuluyla birleştirmek istiyor. Pre-filter ve post-filter arasındaki temel takas nedir?
## A
İkisi de aynı sonucu üretir; koşulun ANN'den önce mi sonra mı uygulandığı önemsizdir
🤖 Sıra sonucu etkiler: aday havuzunu ve dolayısıyla doğruluğu ile sonuç sayısını değiştirir.
## B
Post-filter her zaman daha doğrudur ve hiçbir dezavantaj taşımaz; varsayılan seçim olmalıdır
🤖 Post-filter ANN sonrası eleme yaptığından seçici filtrede elde yetersiz aday kalabilir.
## C
Metadata filtresi vektör DB'lerde desteklenmez; koşullar uygulama katmanında çözülür
🤖 Metadata filtresi yaygın bir yetenektir; sorun mümkünlük değil, ANN ile nasıl sıralanacağıdır.
## D
Pre-filter doğrudur ama indeks desteği ister; post-filter hızlıdır ama aday bırakmayabilir
🤖 Doğru: biri isabetlidir ve indeksten destek bekler, diğeri ucuzdur ve seçici filtrede aday bırakmayabilir.
## Doğru
D
## Açıklama
Metadata filtresi ile ANN'i birleştirmenin iki temel stratejisi vardır. **Pre-filter**: önce koşul (`dil='tr' AND yil>=2024`) uygulanır, ANN yalnız uygun alt küme içinde çalışır — sonuçlar doğrudur ama indeksin **filtreli aramayı** desteklemesi gerekir ve dar filtrelerde maliyetli olabilir. **Post-filter**: önce ANN top-k getirir, sonra koşul uygulanır — basit ve hızlıdır ama filtre adayların çoğunu elerse **yeterli sonuç kalmayabilir** (top-k'yı büyütmek gerekir) (D). Yani seçim, filtrenin seçiciliği ve indeksin yeteneğine bağlı bir dengedir. Diğer şıklar (A/B/C) bu takası yok sayar.

# vecdb-5 | İleri
## Soru
HNSW indeksinde `M` (bağlantı sayısı) ve `efConstruction` gibi parametreleri agresif biçimde artırmanın etkisi nedir?
## A
Bellek kullanımını ve indeks inşa süresini azaltırken recall'ı da belirgin biçimde düşürür
🤖 Tersine, bu parametreleri artırmak belleği ve inşa süresini artırır; azaltmaz.
## B
Hiçbir işlevsel etkisi yoktur; bunlar yalnızca günlük çıktısını etkileyen kozmetik ayarlardır
🤖 Bu parametreler recall/bellek/inşa dengesini doğrudan belirler; kozmetik değildir.
## C
Genelde recall'ı yükseltir ama bellek kullanımı ve indeks inşa süresi de artar
🤖 Doğru: daha zengin çizge daha iyi recall verir; bedeli bellek ve inşa maliyetidir.
## D
Yalnızca sorgu gecikmesini artırır; recall, bellek ve inşa süresi bu ayarlardan etkilenmez
🤖 Etki çok boyutludur: recall, bellek ve inşa süresi de değişir; tek boyuta indirgenemez.
## Doğru
C
## Açıklama
HNSW'de `M` her düğümün komşu sayısını, `efConstruction` inşa sırasında değerlendirilen aday sayısını belirler. Bunları artırmak **daha zengin ve iyi bağlı bir çizge** kurar → genelde **daha yüksek recall ve daha iyi sorgu kalitesi**; ama karşılığında **bellek kullanımı ve indeks inşa süresi artar** (C). Bu klasik bir **takastır** — "ücretsiz öğle yemeği yoktur". Parametreleri azaltmak belleği düşürür ama recall'ı riske atar (A tersini söylüyor). Bunları kozmetik saymak (B) veya etkisini yalnız gecikmeye indirgemek (D) yanlıştır. Doğru ayar; hedef recall, bellek bütçesi ve kabul edilebilir inşa süresine göre ölçülerek yapılır.

# vecdb-6 | İleri
## Soru
Zaten Postgres kullanan, birkaç milyon vektörlük, güçlü ilişkisel filtre/join ve işlemsel tutarlılık gerektiren bir uygulama için hangi seçim daha uygundur?
## A
pgvector; tek sistemde ACID, SQL filtre/join ve düşük işletme yükü sağlar
🤖 Doğru: bu ölçek ve gereksinim profilinde ayrı bir vektör sistemi gereksiz karmaşıklık katar.
## B
Özel vektör DB; ölçek ve gereksinim ne olursa olsun pgvector üretim için uygun değildir
🤖 "Her durumda üstün" yanlıştır; orta ölçek ve güçlü ilişkisel ihtiyaçta pgvector daha basit ve tutarlıdır.
## C
İkisi arasında pratikte anlamlı fark yoktur; ekip hangisini tanıyorsa rastgele seçilebilir
🤖 İşletme modeli, ölçek tavanı, filtre/join ve tutarlılık açısından ciddi farklar vardır.
## D
pgvector yalnızca ilişkisel veriyi saklar; vektör benzerlik aramasını hiç desteklemez
🤖 pgvector tam olarak Postgres'e vektör benzerlik aramasını ekleyen bir eklentidir.
## Doğru
A
## Açıklama
Bu profil — **orta ölçek**, zaten **Postgres**, güçlü **ilişkisel filtre/join** ve **işlemsel tutarlılık** — **pgvector** için idealdir (A). pgvector, vektörleri ilişkisel verinin yanında tutar; tek sistemde ACID, SQL `JOIN`/`WHERE` filtreleri ve işlemsel tutarlılık sağlar, ayrı bir altyapı işletme yükü getirmez. **Özel** vektör DB'ler (Milvus, Qdrant, Weaviate…) asıl değerini **devasa ölçek**, dağıtık ANN ve yüksek eşzamanlı yükte gösterir — ama ayrı bir sistemin işletilmesi, senkronizasyonu ve tutarlılık köprüsü maliyet getirir. "Özel her zaman üstün" (B), "fark yok" (C) ve "pgvector vektör aramayı desteklemez" (D) yanlıştır. İlke: ölçek özel DB'yi zorlayana kadar mevcut Postgres'te kal.
