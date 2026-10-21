---
slug: map-implementasyonlari
baslik: "Map Uygulamaları: HashMap, LinkedHashMap, TreeMap"
kademe: Temel
sira: 22
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sözlük düşün: kelimeye bakarsın, anlamını bulursun. Ya da telefon rehberi: isim → numara. Sözlükte kelime aramak için baştan sona okumazsın; harfe göre doğru yere gidersin. Java'da bu yapının adı `Map`'tir: **anahtar → değer** (key → value) eşlemesi tutar ve "bu anahtarın değeri ne?" sorusunu taramadan yanıtlar.

Anahtar tekildir (aynı anahtar bir kez bulunur), değer tekrarlanabilir. Üç ana uygulama aynı işi yapar ama farklı takas sunar: `HashMap` en hızlısıdır ve sıra sözü vermez, `LinkedHashMap` sırayı hatırlar, `TreeMap` anahtarları sıralı tutar.

### Temel kavramlar

- **Anahtar (key) / değer (value)**: aradığın etiket ve ona bağlı veri.
- **`hashCode()`**: nesneden üretilen tamsayı parmak izi. Aynı nesne hep aynı sayıyı verir.
- **Kova dizisi (bucket array)**: `HashMap`'in içi. Anahtarın hash'inden bir **indeks** hesaplanır, giriş o gözde saklanır.
- **Çakışma (collision)**: iki farklı anahtarın aynı kova indeksine düşmesi. Kova sayısı sonlu olduğu için kaçınılmazdır.
- **O(1)**: eleman sayısı artsa da süre değişmez. **O(log n)**: eleman sayısı iki katına çıkınca süreye yalnız bir adım eklenir. **O(n)**: süre eleman sayısıyla orantılı.
- **Kırmızı-siyah ağaç (red-black tree)**: kendini dengede tutan ikili arama ağacı; sıralı tutar, arama O(log n).
- **Yük faktörü (load factor)**: kova dizisinin ne kadar dolunca büyütüleceğini belirleyen oran.

### Nasıl çalışır?

**`HashMap`'e bir giriş koymak**: anahtarın `hashCode`'u alınır, dağılımı iyileştirmek için bir **karıştırma** (spread/perturbation) uygulanır ve sonuç kova dizisinin boyutuna indirgenir (`hash & (n-1)`). Giriş bu **kovaya** yerleşir; arama da aynı hesapla doğrudan doğru kovaya gider. Kova seçiminde ne ekleme sırası, ne doğal sıra, ne de **değerin** hash'i rol oynar — yalnız anahtarın hash'i.

Aynı kovaya düşen girişler önce bir **bağlı liste** olarak zincirlenir; arama kova içinde `equals` ile doğru anahtarı bulur. Çakışma ne anahtarı reddeder ne de değeri ezer — **ezme yalnız aynı anahtar** (`equals` eşit) için olur. Bir kovadaki düğüm sayısı **8** eşiğini (`TREEIFY_THRESHOLD`) aşarsa **ve** kova dizisinin kapasitesi en az **64** ise, o kova bir **kırmızı-siyah ağaca** dönüştürülür (**treeify**): en kötü durumda arama O(n) yerine **O(log n)** olur. Kapasite 64'ten küçükken eşik aşılırsa ağaçlaştırma yerine önce tablo büyütülür, çünkü küçük tabloda çakışmanın sebebi genelde darlıktır. Kovadaki düğüm sayısı sonradan **6 veya altına** inerse kova tekrar bağlı listeye döner. Dönüşen yalnız **o kovadır**; harita bir bütün olarak `HashMap` kalır.

Eleman sayısı `kapasite × yükFaktörü` eşiğini (varsayılan **0.75**) aşınca kova sayısı **iki katına** çıkarılır ve tüm elemanlar yeniden dağıtılır (**resize/rehash**) — bu O(n)'dir ama seyrek olur. Ortalama erişim **O(1)**.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HashMap kovaları, çakışma, treeify ve resize">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">HashMap iç yapısı</text>
  <text x="70" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">kova dizisi</text>
  <rect x="40" y="60" width="60" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="40" y="92" width="60" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="40" y="124" width="60" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="40" y="156" width="60" height="30" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="70" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">0</text>
  <text x="70" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1</text>
  <text x="70" y="144" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">2</text>
  <text x="70" y="176" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">3</text>
  <line x1="100" y1="107" x2="150" y2="107" stroke="#8A4B1E" stroke-width="1.5"/>
  <rect x="150" y="92" width="52" height="30" rx="4" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <line x1="202" y1="107" x2="222" y2="107" stroke="#8A4B1E" stroke-width="1.5"/>
  <rect x="222" y="92" width="52" height="30" rx="4" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <text x="238" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çakışma → zincir</text>
  <text x="238" y="154" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(kovada &gt;8 → ağaç)</text>
  <rect x="330" y="70" width="280" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">yük faktörü 0.75 aşılınca</text>
  <text x="470" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kova sayısı 2×, elemanlar yeniden dağıtılır (resize)</text>
  <text x="470" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ortalama O(1); iyi dağılan hashCode şarttır.</text>
  <text x="470" y="168" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Kötü hashCode = her şey tek kovada = O(n)/O(log n).</text>
</svg>
```

**`LinkedHashMap`** = `HashMap` + girişleri birbirine bağlayan bir liste; varsayılanda **ekleme sırasını** korur. **`TreeMap`** ise hash kullanmaz; anahtarları bir **kırmızı-siyah ağaçta** sıralı tutar, `get`/`put`/`remove` **O(log n)**'dir ve `Comparable`/`Comparator` gerektirir.

### Mülakatta ne sorulur?

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HashMap, LinkedHashMap ve TreeMap karşılaştırma tablosu">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç Map yan yana</text>
  <rect x="20" y="36" width="130" height="28" rx="4" fill="#1C3247"/>
  <rect x="150" y="36" width="157" height="28" rx="4" fill="#186399"/>
  <rect x="307" y="36" width="157" height="28" rx="4" fill="#25804C"/>
  <rect x="464" y="36" width="156" height="28" rx="4" fill="#B26B00"/>
  <text x="30" y="55" text-anchor="start" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Özellik</text>
  <text x="228" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">HashMap</text>
  <text x="385" y="55" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">LinkedHashMap</text>
  <text x="542" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">TreeMap</text>
  <rect x="20" y="66" width="600" height="30" fill="#EAF1F7"/>
  <rect x="20" y="96" width="600" height="30" fill="#FFFFFF"/>
  <rect x="20" y="126" width="600" height="30" fill="#EAF1F7"/>
  <rect x="20" y="156" width="600" height="30" fill="#FFFFFF"/>
  <text x="30" y="85" text-anchor="start" font-size="11" fill="#1C3247" font-family="system-ui">Sıra garantisi</text>
  <text x="228" y="85" text-anchor="middle" font-size="11" fill="#B26B00" font-family="system-ui">yok</text>
  <text x="385" y="85" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">ekleme/erişim</text>
  <text x="542" y="85" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">sıralı (anahtar)</text>
  <text x="30" y="115" text-anchor="start" font-size="11" fill="#1C3247" font-family="system-ui">Karmaşıklık</text>
  <text x="228" y="115" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">O(1)</text>
  <text x="385" y="115" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">O(1)</text>
  <text x="542" y="115" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">O(log n)</text>
  <text x="30" y="145" text-anchor="start" font-size="11" fill="#1C3247" font-family="system-ui">null anahtar</text>
  <text x="228" y="145" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1 tane</text>
  <text x="385" y="145" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1 tane</text>
  <text x="542" y="145" text-anchor="middle" font-size="11" fill="#B26B00" font-family="system-ui">yok (NPE)</text>
  <text x="30" y="175" text-anchor="start" font-size="11" fill="#1C3247" font-family="system-ui">İç yapı</text>
  <text x="228" y="175" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kova dizisi</text>
  <text x="385" y="175" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kova + liste</text>
  <text x="542" y="175" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kırmızı-siyah ağaç</text>
  <text x="320" y="203" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hepsi null değere izin verir; yalnız Hashtable / ConcurrentHashMap null'ı reddeder.</text>
</svg>
```

- **Yük faktörü ne demek değildir**: "en fazla 75 eleman" değil, "belleğin %75'i boş" değil, "aramaların %75'i O(1)" değil. Doluluk oranı 0.75'i geçince **büyüme** tetiklenir; düşük yük faktörü az çakışma çok bellek, yüksek yük faktörü tersi demektir. Beklenen boyutu biliyorsan başlangıç kapasitesi vermek resize'ları azaltır.
- **`null` kuralları**: `HashMap` ve `LinkedHashMap` **tek bir `null` anahtara** (özel olarak 0. kovaya konur) ve **birçok `null` değere** izin verir; anahtarlar tekil olduğundan birden fazla `null` anahtar olamaz. Doğal sıralı `TreeMap` `null` anahtarı karşılaştıramaz → **`NullPointerException`** (null'ı ele alan özel bir `Comparator` verilmedikçe). Ayrı tuzak: eski `Hashtable` ve `ConcurrentHashMap` ne `null` anahtar ne `null` değer kabul eder.
- **LRU önbelleği tarifi**: `LinkedHashMap`'i `accessOrder=true` ile kurarsan her `get`/`put` ilgili girişi listenin **sonuna** taşır; **baştaki** giriş her zaman "en az yakın zamanda kullanılan"dır. `removeEldestEntry(...)` metodunu ezip kapasite aşıldığında `true` döndürürsen en eski giriş otomatik atılır — birkaç satırda çalışan bir **LRU cache**. `HashMap` sıra tutmadığı için, `TreeMap` ise kullanım değil **anahtar** sırası tuttuğu için bu iş için uygun değildir; `ArrayList` zaten eşleme bile değildir.
- **`NavigableMap` navigasyonu** (`TreeMap` bunu uygular); aşağıdaki işlemlerin hepsi **O(log n)**'dir. `floorKey(k)` = k'ye eşit ya da ondan **küçük en büyük** anahtar. `ceilingKey(k)` = k'ye eşit ya da ondan **büyük en küçük** anahtar. `lowerKey`/`higherKey` = kesin küçük/büyük komşu. Kalanlar: `firstKey`/`lastKey` uçları, `headMap`/`tailMap`/`subMap` ise aralık görünümlerini verir. Aynı işi `HashMap` anahtarlarını her sorguda listeye alıp sıralayarak yapmak O(n log n)'dir ve `TreeMap`'in amacını boşa çıkarır.
- **Kötü `hashCode` = tek kovada yığılma** = O(1) yerine O(n)/O(log n). Anahtar sınıfının iyi dağılan bir `hashCode`'u performans için kritiktir.
- **Seçim kuralı**: sıra gerekmiyorsa `HashMap`; ekleme/erişim sırası veya LRU için `LinkedHashMap`; sıralı anahtar veya aralık sorgusu için `TreeMap`.

### Tek cümlede

`HashMap` anahtarın hash'inden kova seçip ortalama O(1) verir (çakışmayı zincir, gerekirse ağaçla çözer), `LinkedHashMap` buna sıra hafızası ekler, `TreeMap` ise O(log n) karşılığında sıralı anahtar ve aralık sorgusu sunar.

# map-1 | Temel
## Soru
`HashMap`'te bir anahtarın hangi **kovaya** (bucket) düşeceği neye göre belirlenir?
## A
Anahtarın doğal sırasına (`compareTo`) göre
🤖 `compareTo` `TreeMap` içindir; `HashMap` sıralama değil hash kullanır.
## B
Anahtarın belleğe eklenme sırasına göre
🤖 Ekleme sırası kova seçimini belirlemez; kova hash'ten hesaplanır.
## C
Anahtarın `hashCode`'undan türetilen bir indekse göre
🤖 Doğru: `HashMap` hash değerini işleyip kova dizisi boyutuna indirger; kova bu indekstir.
## D
Değerin (value) `hashCode`'una göre
🤖 Kova anahtarın hash'inden seçilir; değerin hash'i kullanılmaz.
## Doğru
C
## Açıklama
`HashMap`, anahtarın **`hashCode`**'unu alır, dağılımı iyileştirmek için bir karıştırma (spread/perturbation) uygular ve sonucu kova dizisinin boyutuna göre bir **indekse** indirger (`hash & (n-1)`). Anahtar bu **kovaya** yerleşir; arama da aynı hesapla doğru kovaya gider. Ekleme sırası (B), doğal sıra (A) veya değerin hash'i (D) kova seçiminde rol oynamaz. Bu yüzden anahtar sınıfının **iyi dağılan bir `hashCode`**'u performans için kritiktir.

# map-2 | Temel
## Soru
`HashMap`'te iki farklı anahtar aynı kovaya düşerse (çakışma / collision) ne olur?
## A
İkinci anahtar reddedilir, `put` `false` döner
🤖 Farklı anahtarlar çakışsa da ikisi de saklanır; `HashMap` anahtarı reddetmez.
## B
`HashMap` derhal yeniden boyutlanır (resize) ve çakışma kaybolur
🤖 Resize yük faktörüne bağlıdır; tek çakışma resize tetiklemez ve resize çakışmayı garantili çözmez.
## C
Var olan anahtarın değeri yeni anahtarla ezilir (overwrite)
🤖 Ezme yalnız **aynı** anahtar (equals eşit) için olur; farklı anahtarların çakışması ezme değildir.
## D
Aynı kovada zincirlenirler (bağlı liste); kova çok büyürse ağaca dönüşür
🤖 Doğru: çakışan girişler kovada zincir olur, eşik aşılınca kırmızı-siyah ağaca (treeify) geçer.
## Doğru
D
## Açıklama
**Çakışma** (collision), farklı `hashCode`/anahtarların aynı kova indeksine düşmesidir — kaçınılmazdır. `HashMap` bunları o kovada bir **bağlı liste** olarak zincirler; arama kova içinde `equals` ile doğru anahtarı bulur. Java 8+ ile bir kovadaki düğüm sayısı **8** eşiğini aşarsa (ve tablo yeterince büyükse) o kova **kırmızı-siyah ağaca** dönüştürülür (**treeify**), böylece kötü durumda arama O(n) yerine **O(log n)** olur. Çakışma ne anahtarı reddeder (A), ne değeri ezer (C) — ezme yalnız **aynı** anahtar için geçerlidir.

# map-3 | Orta
## Soru
`HashMap`'in **yük faktörü** (load factor) 0.75 olması ne anlama gelir?
## A
Harita en fazla 75 eleman tutabilir
🤖 Sabit bir eleman sınırı değildir; oransal bir doluluk eşiğidir.
## B
Eleman sayısı, kova sayısının %75'ini aşınca kova dizisi büyütülür (resize)
🤖 Doğru: doluluk oranı 0.75'i geçince kapasite ~2× artar, rehash yapılır.
## C
Haritanın belleğinin %75'i her zaman boş tutulur, böylece çakışma hiç olmaz
🤖 Bu bellek ayırma kuralı değildir; 0.75 doluluk-büyüme eşiğidir.
## D
Aramaların %75'i O(1), %25'i O(n)'dir
🤖 Böyle bir olasılık dağılımı değildir; 0.75 zaman-bellek dengesini ayarlayan bir eşiktir.
## Doğru
B
## Açıklama
**Yük faktörü**, kova dizisinin ne kadar dolunca **büyütüleceğini** (resize) belirleyen eşiktir: `eşik = kapasite × loadFactor`. Varsayılan **0.75**, zaman ve bellek arasında dengeli bir seçimdir. Eleman sayısı bu eşiği aşınca `HashMap` kova sayısını (genelde) **iki katına** çıkarır ve tüm elemanları yeni kovalara **yeniden dağıtır** (rehash) — bu O(n) bir işlemdir ama seyrek olur. Düşük yük faktörü daha az çakışma ama daha çok bellek; yüksek yük faktörü tersi demektir. Beklenen boyutu bilirsen başlangıç kapasitesi vermek resize'ları azaltır.

# map-4 | Orta
## Soru
En son **erişilen** girişleri sonda tutan basit bir **LRU önbelleği** (Least Recently Used cache) kurmak için hangi yapı en uygundur?
## A
`ArrayList` — girişleri listeye ekleyip aramayı baştan sona tarayarak yapmak
🤖 `ArrayList` anahtar-değer eşlemesi değildir; O(n) arama LRU önbelleği için verimsizdir.
## B
`HashMap` — girişleri anahtarla saklayıp O(1) aramanın yeterli olmasına güvenmek
🤖 `HashMap` hiçbir sıra tutmaz; hangi girişin "en eski" olduğunu bilemezsin.
## C
`TreeMap` — girişleri anahtar sırasına göre tutup baştaki anahtarı atmak
🤖 `TreeMap` anahtarları sıralar, erişim/kullanım sırasını değil; LRU için uygun değildir.
## D
`accessOrder=true` ile kurulmuş `LinkedHashMap` + `removeEldestEntry` eziminin (override) birlikte kullanımı
🤖 Doğru: erişim sırasına göre örülen `LinkedHashMap`, en eskiyi çıkararak doğal bir LRU verir.
## Doğru
D
## Açıklama
`LinkedHashMap`, `accessOrder=true` ile kurulduğunda girişleri **son erişilme sırasına** göre örer: her `get`/`put`, ilgili girişi listenin **sonuna** taşır; böylece **baştaki** giriş her zaman "en az yakın zamanda kullanılan"dır. `removeEldestEntry(...)` metodunu ezip kapasite aşıldığında `true` döndürerek en eski girişin otomatik atılmasını sağlarsın — birkaç satırda çalışan bir **LRU önbelleği**. `HashMap` (B) sıra tutmaz, `TreeMap` (C) kullanım değil anahtar sırası tutar, `ArrayList` (A) eşleme bile değildir.

# map-5 | Orta
## Soru
Aşağıdaki `null` davranışlarından hangisi **doğrudur**?
## A
`HashMap` `null` anahtara izin verir; doğal sıralı `TreeMap` `null` anahtarda `NullPointerException` fırlatır
🤖 Doğru: HashMap null anahtarı özel kovaya koyar; TreeMap null'ı karşılaştıramaz, NPE atar.
## B
Her iki harita da `null` anahtarı serbestçe kabul eder; `null` sıralamada en başa konur
🤖 `TreeMap` doğal sırada `null` anahtarı karşılaştıramaz; NPE fırlatır.
## C
Hiçbir `Map` `null` değer (value) kabul etmez
🤖 `HashMap` ve `LinkedHashMap` birçok `null` **değeri** kabul eder; yalnız `Hashtable`/`ConcurrentHashMap` reddeder.
## D
`HashMap` birden fazla `null` anahtar tutabilir
🤖 Anahtarlar tekildir; `null` da tek bir anahtardır, en fazla bir tane olabilir.
## Doğru
A
## Açıklama
`HashMap` (ve `LinkedHashMap`) **tek bir `null` anahtara** izin verir — onu özel olarak 0. kovaya yerleştirir — ve **birçok `null` değere** izin verir. `TreeMap` ise anahtarları **karşılaştırarak** sıralar; `null` karşılaştırılamayacağı için doğal sıralı bir `TreeMap`'e `null` anahtar koymak **`NullPointerException`** fırlatır (null'ı ele alan özel bir `Comparator` vermedikçe). Ayrı bir tuzak: eski `Hashtable` ve `ConcurrentHashMap` ne `null` anahtar ne `null` değer kabul eder. Anahtarlar tanımı gereği tekil olduğundan (D) birden fazla `null` anahtar olamaz.

# map-6 | Orta
## Soru
Java 8+ `HashMap`'te bir kovadaki (bucket) düğüm sayısı **8** eşiğini aşınca ne olur ve amacı nedir?
## A
Kova bir kırmızı-siyah ağaca (treeify) dönüştürülür; arama O(n) yerine O(log n) olur
🤖 Doğru: aşırı çakışan kovada zincir yerine ağaç, en kötü arama süresini düşürür.
## B
`HashMap` o kovadaki elemanları siler
🤖 Eleman silinmez; yapı korunur, yalnız kova içi organizasyon değişir.
## C
Tüm harita anında `TreeMap`'e dönüşür ve anahtarlar sıralı hale gelir
🤖 Yalnız o **kova** ağaçlaşır; harita `HashMap` olarak kalır, `TreeMap`'e dönmez.
## D
Bir istisna fırlatılır çünkü kova taştı ve zincir uzunluğu sınırı aşıldı
🤖 Taşma istisnası yoktur; kova büyümeyi ağaçlaşarak ele alır.
## Doğru
A
## Açıklama
Kötü bir `hashCode` (ya da hash çakışması saldırısı) çoğu anahtarı **tek bir kovaya** yığabilir; o zaman kova içi arama bağlı liste üzerinde **O(n)** olur. Java 8+ bir kovadaki düğüm sayısı **TREEIFY_THRESHOLD = 8**'i aşınca (ve toplam kapasite ≥ 64 ise) o **tek kovayı** bir **kırmızı-siyah ağaca** dönüştürür (**treeify**); böylece o kovadaki arama **O(log n)**'e iner. Kapasite 64'ten küçükken eşik aşılırsa ağaçlaştırma yerine önce tablo büyütülür (resize), çünkü küçük tabloda çakışmanın sebebi genelde darlıktır. Kovadaki düğüm sayısı sonradan **6 veya altına** inerse (yeniden boyutlandırma sırasında) kova tekrar bağlı listeye döner. Dönüşen yalnız **o kovadır**, harita bir bütün olarak `HashMap` kalır (C yanlış).

# map-7 | İleri
## Soru
Anahtarları **sıralı** tutmak ve "verilen anahtardan küçük en büyük anahtar" gibi **aralık/komşu** sorguları yapmak istiyorsun. Hangi `Map` uygundur ve karmaşıklığı nedir?
## A
`HashMap`, O(1)
🤖 `HashMap` O(1) verir ama hiçbir sıra tutmaz; aralık/komşu sorgusu yapamazsın.
## B
`TreeMap`, temel işlemler O(log n); `floorKey`/`ceilingKey`/`subMap` gibi navigasyon sağlar
🤖 Doğru: TreeMap sıralı ağaçtır, NavigableMap navigasyon metotlarını O(log n)'de sunar.
## C
`LinkedHashMap`, O(1)
🤖 `LinkedHashMap` yalnız ekleme/erişim sırasını tutar; sıralı değildir, aralık sorgusu veremez.
## D
`HashMap`, her sorgudan önce anahtarları bir listeye alıp elle sıralayarak, yine O(1)
🤖 Elle sıralama O(n log n)'dir ve her sorguda tekrarlanır; toplam maliyet O(1) değildir.
## Doğru
B
## Açıklama
`TreeMap`, anahtarları bir **kırmızı-siyah ağaçta** sıralı tutar; `get`/`put`/`remove` **O(log n)**'dir. `NavigableMap` arayüzü üzerinden **`floorKey`/`ceilingKey`/`higherKey`/`lowerKey`** (komşu anahtarlar), **`firstKey`/`lastKey`**, ve **`headMap`/`tailMap`/`subMap`** (aralık görünümleri) gibi sorguları da O(log n)'de sağlar. `HashMap` (A) ve `LinkedHashMap` (C) hiç sıralı değildir; anahtarları her seferinde elle sıralamak (D) O(n log n) maliyetlidir ve `TreeMap`'in tüm amacını boşa çıkarır. Sıralı gezinme/aralık ihtiyacı `TreeMap`'i tercih etmenin başlıca sebebidir.
