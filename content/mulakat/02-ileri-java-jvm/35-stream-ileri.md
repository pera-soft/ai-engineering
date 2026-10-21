---
slug: stream-ileri
baslik: "Stream (İleri): Collectors, flatMap, reduce ve Durumlu İşlem Tuzağı"
kademe: İleri
sira: 35
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir posta ayrıştırma merkezinde binlerce zarf gelir; personel bunları şehirlere göre kutulara ayırır, sonra her kutudaki zarfı sayıp tutanağa yazar. Elinde tek tek zarflar vardı, sonunda "Ankara: 240, İzmir: 90" gibi bir ÖZET oldu. Stream'in ileri kullanımı tam bu iştir: akıp giden elemanları anlamlı bir yapıya toplamak. Bunu yapan araç `Collectors` sınıfıdır ve SQL'deki `GROUP BY` + toplama fonksiyonlarının Java karşılığıdır. Yanına iki konu daha eklenir: iç içe yapıları düzleştiren `flatMap` ve işi birden çok çekirdeğe dağıtan paralel stream.

### Temel kavramlar

**Toplayıcı (collector):** `collect` işleminin elemanları neye dönüştüreceğini tarif eden tarif kâğıdı. `Collectors` sınıfı hazır tarifler sunar: `toList()`, `toSet()`, `toMap(...)`, `groupingBy(...)`, `counting()`, `joining()`.

**Sınıflandırıcı (classifier):** `groupingBy`'ye verdiğin fonksiyon; her elemandan bir ANAHTAR üretir. `Kisi::getSehir` her kişiden bir şehir adı üretir.

**Alt-toplayıcı (downstream collector):** Gruplama bittikten sonra HER GRUBA ayrıca uygulanan ikinci toplayıcı. Verilmezse varsayılan `toList()` uygulanır:

```java
groupingBy(Kisi::getSehir)                          // Map<String, List<Kisi>>
groupingBy(Kisi::getSehir, counting())              // Map<String, Long>
groupingBy(Kisi::getSehir, mapping(Kisi::getAd, toList()))  // Map<String, List<String>>
```

Alt-toplayıcılar birleştirilebilir ve iç içe konabilir: `mapping`, `summingInt`, `averagingDouble`, `maxBy`, hatta başka bir `groupingBy`.

```svg
<svg viewBox="0 0 680 228" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="groupingBy: önce sınıflandır, sonra alt-toplayıcı counting ile grubu sayıya indir">
  <defs>
    <marker id="stri-gb-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">groupingBy — sınıflandır, sonra alt-toplayıcı</text>
  <rect x="18" y="60" width="150" height="112" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="93" y="80" text-anchor="middle" font-family="system-ui" font-size="11" font-weight="700" fill="#186399">girdi</text>
  <text x="93" y="104" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">Ali · Ankara</text>
  <text x="93" y="128" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">Ece · İzmir</text>
  <text x="93" y="152" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">Can · Ankara</text>
  <line x1="168" y1="86" x2="228" y2="86" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-gb-ar)"/>
  <text x="198" y="78" text-anchor="middle" font-family="system-ui" font-size="10" fill="#5D7789">sehir</text>
  <rect x="230" y="56" width="432" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="446" y="80" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">Map&lt;String, List&lt;Kisi&gt;&gt;</text>
  <text x="446" y="100" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">{ Ankara=[Ali, Can], İzmir=[Ece] }</text>
  <line x1="446" y1="114" x2="446" y2="150" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-gb-ar)"/>
  <text x="540" y="134" text-anchor="middle" font-family="system-ui" font-size="10.5" fill="#5D7789">+ counting()</text>
  <rect x="230" y="152" width="432" height="58" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="446" y="176" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#6B3FA0">Map&lt;String, Long&gt;</text>
  <text x="446" y="196" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">{ Ankara=2, İzmir=1 }</text>
</svg>
```

**`partitioningBy`:** `groupingBy`'nin özel hâli. Bir **predicate** (boolean koşul) alır ve elemanları TAM İKİ parçaya böler; her zaman `Map<Boolean, List<T>>` üretir.

**Birleştirme fonksiyonu (merge function):** `toMap`'te iki elemanın aynı anahtarı üretmesi durumunda hangi değerin kalacağını söyleyen fonksiyon.

**Kimlik değeri (identity) ve birleşimlilik (associative):** Bir işlem birleşimlidir eğer `(a op b) op c` ile `a op (b op c)` aynı sonucu veriyorsa. Paralel doğruluğun ön koşuludur.

**Durumsuz (stateless) / yan etkisiz (side-effect-free):** Lambdanın dışarıdaki paylaşılan bir değişkeni okuyup değiştirmemesi.

### Nasıl çalışır?

**`flatMap` — düzleştirme.** `map` her elemandan TEK bir eleman üretir; `flatMap` ise her elemandan bir STREAM üretir ve bu alt-stream'leri tek bir düz stream'de birleştirir. `Stream<List<Integer>>` üzerinde `map(list -> list.stream())` yazarsan `Stream<Stream<Integer>>` elde edersin — iç içe kalır, işe yaramaz. `flatMap(List::stream)` ise `Stream<Integer>` verir. Tipik kullanımları: cümleleri kelimelere ayırmak (`flatMap(s -> Arrays.stream(s.split(" ")))`), iç içe koleksiyonları açmak, `Stream<Optional<T>>`'i değerlere indirmek. Kural: bir elemandan SIFIR veya ÇOK eleman üretilecekse `flatMap`, TEK eleman üretilecekse `map`. Not: `filter` yalnız eler, `reduce` tek değere indirger — ikisi de düzleştirme yapmaz.

```svg
<svg viewBox="0 0 680 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="map iç içe stream üretir, flatMap düz stream üretir">
  <defs>
    <marker id="stri-fm-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">map vs flatMap — düzleştirme</text>
  <rect x="240" y="38" width="200" height="48" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="340" y="60" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#186399">Stream&lt;List&lt;Integer&gt;&gt;</text>
  <text x="340" y="78" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">[ [1,2], [3,4] ]</text>
  <line x1="300" y1="86" x2="150" y2="118" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-fm-ar)"/>
  <line x1="380" y1="86" x2="530" y2="118" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-fm-ar)"/>
  <rect x="40" y="120" width="204" height="44" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="142" y="147" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#B26B00">map(List::stream)</text>
  <rect x="424" y="120" width="216" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="532" y="147" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#25804C">flatMap(List::stream)</text>
  <rect x="40" y="176" width="204" height="50" rx="9" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="2"/>
  <text x="142" y="199" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#8A2E2E">Stream&lt;Stream&lt;Integer&gt;&gt;</text>
  <text x="142" y="217" text-anchor="middle" font-family="system-ui" font-size="11" fill="#8A2E2E">iç içe kalır ✗</text>
  <rect x="424" y="176" width="216" height="50" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="532" y="199" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#25804C">Stream&lt;Integer&gt; 1 2 3 4</text>
  <text x="532" y="217" text-anchor="middle" font-family="system-ui" font-size="11" fill="#25804C">düzleşti ✓</text>
</svg>
```

**Üç argümanlı `reduce`.** `reduce(identity, accumulator, combiner)` biçimi paralel işlem için tasarlanmıştır: stream parçalara bölünür, her parça `accumulator` ile ayrı indirgenir, sonra kısmi sonuçlar `combiner` ile birleştirilir. `combiner` sıralı çalışmada gerekmez ama paralelde MUTLAKA çağrılır — sırf API uyumu için duran bir parametre değildir.

```svg
<svg viewBox="0 0 680 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Paralel reduce: stream parçalanır, her parça accumulator ile indirgenir, combiner kısmi sonuçları birleştirir">
  <defs>
    <marker id="stri-rd-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Paralel reduce(identity, accumulator, combiner)</text>
  <rect x="240" y="40" width="200" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="340" y="62" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">[1, 2, 3, 4]</text>
  <text x="340" y="80" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">identity = 0</text>
  <line x1="300" y1="86" x2="180" y2="116" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-rd-ar)"/>
  <line x1="380" y1="86" x2="500" y2="116" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-rd-ar)"/>
  <rect x="60" y="118" width="240" height="48" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="180" y="147" text-anchor="middle" font-family="system-ui" font-size="12" fill="#25804C">parça [1,2] → accumulator → 3</text>
  <rect x="380" y="118" width="240" height="48" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="500" y="147" text-anchor="middle" font-family="system-ui" font-size="12" fill="#25804C">parça [3,4] → accumulator → 7</text>
  <line x1="180" y1="166" x2="310" y2="196" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-rd-ar)"/>
  <line x1="500" y1="166" x2="370" y2="196" stroke="#5D7789" stroke-width="2" marker-end="url(#stri-rd-ar)"/>
  <rect x="240" y="198" width="200" height="30" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="340" y="218" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#6B3FA0">combiner(3, 7) → 10</text>
</svg>
```

Doğruluk için iki koşul şarttır: (1) **identity nötr olmalı** — `accumulator(identity, x)` her zaman `x`'e eşit çıkmalı; bu değer işleme göre değişir (toplamada 0, çarpmada 1, liste birleştirmede boş liste), sabit 0 değildir. (2) **`combiner` ile `accumulator` tutarlı ve birleşimli olmalı** — `combiner(a, accumulator(identity, b))` ile `accumulator(a, b)` aynı sonucu vermeli. Bu koşullar sağlanmazsa kod SIRALI stream'de doğru, PARALEL stream'de yanlış ve tekrarlanamayan sonuç verir; ayıklaması en zor hata türlerindendir. Karmaşık biriktirmelerde `reduce` yerine değişebilir konteyner kullanan `collect` genelde daha uygundur.

**Durumlu lambda tuzağı.** Stream işlemleri lambdaların durumsuz ve yan etkisiz olduğunu VARSAYAR. `map`/`filter` içinde paylaşılan bir alanı biriktirmek (`this.toplam += x`) bu sözleşmeyi bozar. Paralelde birden çok iş parçacığı (thread) aynı değişkeni eşzamanlı değiştirir; sonuç yarış koşulu (race condition), kayıp güncelleme ve öngörülemez çıktıdır. Sıralı çalışmada bile kırılgandır: tembellik ve kısa devre yüzünden lambdanın kaç kez çalışacağı garanti değildir. Doğru yol biriktirmeyi API'ye bırakmaktır: `mapToInt(...).sum()`, `reduce(0, Integer::sum)` veya `collect(Collectors.summingInt(...))`.

### Mülakatta ne sorulur?

- **`groupingBy(Kisi::getSehir, counting())` ne döndürür?** `Map<String, Long>` — anahtar sınıflandırıcının dönüşüdür (`String`, `Kisi` değil), değer ise alt-toplayıcının indirgediği sayıdır. `List` değil, `Map` döner.
- **`toMap`'te anahtar çakışması.** İki argümanlı `Collectors.toMap(anahtarFn, değerFn)`, aynı anahtarı iki eleman üretirse `IllegalStateException: Duplicate key ...` fırlatır. Sessizce üzerine YAZMAZ, "ilki kazanır" gibi otomatik bir kural YOKTUR ve derleme hatası da değildir — bilinçli bir gürültülü başarısızlık (fail-fast) tasarımıdır. Çözüm üçüncü parametredir: `toMap(k, v, (eski, yeni) -> yeni)` yenisini tutar, `(eski, yeni) -> eski` eskisini, `(eski, yeni) -> eski + yeni` toplar. Dördüncü parametreyle `Map` uygulaması da seçilir: ekleme sırasını korumak için `LinkedHashMap::new`, sıralı anahtar için `TreeMap::new`.
- **`partitioningBy` vs `groupingBy`.** İkisi de `Map` döndürür; fark anahtar kümesindedir. `partitioningBy` SABİT iki anahtar (`true`/`false`) üretir ve koşulu sağlayan eleman OLMASA bile her iki anahtar da map'te bulunur (biri boş liste olur) — bu, her iki tarafa güvenle erişebilmeni sağlar. `groupingBy` ise DİNAMİK sayıda anahtar üretir ve yalnız gerçekten oluşan anahtarlar map'te yer alır. `partitioningBy`'nin sayısal alan kısıtı yoktur, bir predicate alır; ikisi de alt-toplayıcı kabul eder (`partitioningBy(k -> k.getYas() >= 18, counting())` → `Map<Boolean, Long>`).
- **Lambda içinde alan biriktirmek neden tehlikeli?** Durumlu yan etki → paralelde yarış koşulu. Dikkat: bu DERLEME hatası değildir — effectively final kısıtı yalnız yakalanan YEREL değişkenlere uygulanır, bir ALANA yazmak sorunsuz derlenir. Aynı işi yerel bir `int` ile denemek derleme hatası verirdi; ama alan ya da `AtomicInteger` kullanıp derleyiciyi susturmak asıl tasarım hatasını yalnızca gizler. `map` her tipi döndürebilir, sorun dönüş tipi değildir.
- **Paralel stream ne zaman kazandırır?** `parallelStream()` sihirli bir hızlandırıcı değildir. Kazanç ancak dört koşul BİRLİKTE sağlanınca gelir. (1) Veri kümesi büyüktür ve eleman başına iş işlemci-yoğundur (CPU-bound). (2) Kaynak kolay bölünebilir: `ArrayList`, dizi ve `IntStream.range` iyidir; `LinkedList` ya da yineleyici (iterator) tabanlı kaynaklar kötüdür. (3) İşlemler durumsuz ve yan etkisizdir. (4) Ortak iş havuzu (`ForkJoinPool.commonPool`) bloklanmaz; yani G/Ç (I/O) bekleyen işler paralel stream'e UYGUN DEĞİLDİR. Küçük koleksiyonlarda bölme ve birleştirme maliyeti kazancı yutar, hatta sıralı sürümden yavaş olur.

### Tek cümlede

İleri stream, elemanları `Collectors` ile anlamlı yapılara toplamak, iç içe olanı `flatMap` ile düzleştirmek ve bunu paralelde de doğru yapabilmek için lambdaları durumsuz, kimliği nötr, birleştiriciyi birleşimli tutmak demektir.

# stri-1 | İleri
## Soru
`kisiler.stream().collect(groupingBy(Kisi::getSehir, counting()))` ifadesinin dönüş tipi nedir?
## A
`Map<String, List<Kisi>>`
🤖 Alt-toplayıcı `counting()` grubu bir sayıya indirger; liste değil sayı döner.
## B
`List<Long>`
🤖 `groupingBy` bir `Map` üretir, `List` değil; anahtarlar da korunur.
## C
`Map<String, Long>`
🤖 Doğru: şehir anahtarı, `counting()` alt-toplayıcısı her grup için `Long` sayı verir.
## D
`Map<Kisi, Long>`
🤖 Anahtar `getSehir` sonucudur (`String`), `Kisi` değil; sınıflandırıcı fonksiyonun dönüşü anahtardır.
## Doğru
C
## Açıklama
`groupingBy(sınıflandırıcı, altToplayıcı)` iki aşamalıdır: önce elemanları sınıflandırıcının (`getSehir` → `String`) döndürdüğü anahtara göre gruplar; sonra HER grubu **alt-toplayıcı** (downstream collector) ile işler. `counting()` her grubu eleman SAYISINA (`Long`) indirger. Sonuç `Map<String, Long>` olur — her şehir için kaç kişi olduğu. Alt-toplayıcı verilmeseydi varsayılan `toList()` uygulanır ve `Map<String, List<Kisi>>` (A) çıkardı. Alt-toplayıcılar birleştirilebilir: `mapping`, `summingInt`, `averagingDouble`, `maxBy`, hatta iç içe başka bir `groupingBy` — bu, SQL'deki `GROUP BY` + agrega fonksiyonlarına karşılık gelen güçlü bir mekanizmadır.

# stri-2 | İleri
## Soru
`Stream<List<Integer>>` (liste listesi) tüm iç sayıları TEK bir düz stream'de işlemek için hangisi kullanılır?
## A
`map(list -> list.stream())`
🤖 Bu `Stream<Stream<Integer>>` üretir; iç içe kalır, düzleşmez.
## B
`flatMap(List::stream)`
🤖 Doğru: her listeyi stream'e açıp hepsini tek düz `Stream<Integer>`'da birleştirir.
## C
`filter(list -> !list.isEmpty())`
🤖 `filter` yalnız eler; iç içe yapıyı düzleştirmez, boş olmayan listeler yine iç içe kalır.
## D
`reduce((a, b) -> a)`
🤖 `reduce` tek bir değere indirger; düzleştirme yapmaz, listeleri açmaz.
## Doğru
B
## Açıklama
`flatMap`, iç içe yapıları düzleştirmenin (flatten) standart yoludur. `flatMap(List::stream)` her `List<Integer>` elemanını bir `Stream<Integer>`'a dönüştürür ve bu alt-stream'leri TEK bir düz `Stream<Integer>`'da birleştirir. Buna karşılık `map(list -> list.stream())` (A) her listeyi ayrı bir stream'e sarar ama düzleştirmez → sonuç `Stream<Stream<Integer>>` olur, işe yaramaz. `flatMap`'in tipik kullanımları: cümleleri kelimelere ayırmak (`flatMap(s -> Arrays.stream(s.split(" ")))`), iç içe koleksiyonları açmak, ve `Stream<Optional<T>>`'i değerlere düzleştirmek. Kural: "bir elemandan SIFIR veya ÇOK eleman üretilecekse `flatMap`; bir elemandan TEK eleman üretilecekse `map`."

# stri-3 | Uzman
## Soru
Paralel bir stream'de `reduce(identity, accumulator, combiner)` kullanırken en kritik doğruluk koşulu nedir?
## A
identity nötr olmalı; combiner, accumulator ile tutarlı ve birleşimli (associative) olmalıdır
🤖 Doğru: identity nötr + combiner-accumulator uyumu paralel doğruluğun ön koşuludur.
## B
combiner hiç çağrılmaz; yalnız API uyumluluğu için istenen bir parametredir
🤖 Paralel stream'de combiner kısmi sonuçları birleştirmek için ÇAĞRILIR; atlanamaz.
## C
identity her koşulda 0 olmalıdır; başka bir başlangıç değeri paralelde bozulur
🤖 identity işleme bağlıdır (toplamada 0, çarpmada 1, birleştirmede boş liste); sabit 0 değildir.
## D
Paralel stream'de `reduce` hiç kullanılamaz; bu iş yalnız `collect` ile yapılabilir
🤖 `reduce` paralel çalışır; üç argümanlı biçimi tam da paralel için tasarlanmıştır.
## Doğru
A
## Açıklama
Üç argümanlı `reduce(identity, accumulator, combiner)` paralel işlem için tasarlanmıştır: stream parçalara bölünür, her parça `accumulator` ile ayrı indirgenir, sonra kısmi sonuçlar `combiner` ile birleştirilir. Doğruluk için iki koşul şarttır: (1) **identity nötr olmalı** — `accumulator(identity, x)` her zaman `x`'e eşit olmalı (toplamada 0, çarpmada 1); (2) **combiner ve accumulator tutarlı ve birleşimli (associative) olmalı** — `combiner(a, accumulator(identity, b))` ile `accumulator(a, b)` aynı sonucu vermeli. Bu koşullar sağlanmazsa SIRALI stream'de doğru, PARALEL stream'de yanlış (ve tekrarlanamayan) sonuç çıkar — ayıklaması en zor hatalardandır. combiner paralelde MUTLAKA çağrılır (B yanlış); identity işleme göre değişir (C yanlış). Karmaşık biriktirmelerde `reduce` yerine değişebilir konteyner kullanan `collect` genelde daha uygundur.

# stri-4 | İleri
## Soru
`toMap` ile bir `Map` oluştururken iki eleman AYNI anahtarı üretirse ne olur ve nasıl önlenir?
## A
Sessizce sonuncu değer kazanır ve `Map`'e o yazılır, hiçbir sorun çıkmaz
🤖 Sessiz üzerine yazma yoktur; iki argümanlı `toMap` çakışmada istisna atar.
## B
İlk eleman her zaman korunur; sonraki çakışan değerler sessizce atlanır
🤖 Otomatik "ilki kazanır" davranışı yoktur; birleştirme fonksiyonu verilmezse istisna fırlar.
## C
Derleme hatası verir; derleyici anahtar çakışması olasılığını baştan yakalar
🤖 Kod derlenir; çakışma yalnız çalışma-zamanında istisna olarak ortaya çıkar.
## D
`IllegalStateException` (duplicate key) fırlatır; birleştirme (merge) fonksiyonu ile önlenir
🤖 Doğru: iki argümanlı `toMap` çakışmada patlar; merge fonksiyonu çakışmayı çözer.
## Doğru
D
## Açıklama
İki argümanlı `Collectors.toMap(anahtarFn, değerFn)`, iki eleman AYNI anahtarı ürettiğinde `IllegalStateException: Duplicate key ...` fırlatır — sessizce üzerine yazMAZ (A yanlış) ve "ilki/sonuncusu kazanır" gibi otomatik bir kural YOKTUR (B yanlış). Bu bilinçli bir "gürültülü başarısızlık" (fail-fast) tasarımıdır: veride beklenmedik çakışma varsa hata gizlenmez. Çözüm, üçüncü parametre olan **birleştirme fonksiyonudur** (merge function): `toMap(k, v, (eski, yeni) -> yeni)` çakışmada yenisini tutar, `(eski, yeni) -> eski` eskisini tutar, `(eski, yeni) -> eski + yeni` toplayabilir. Ayrıca dördüncü parametreyle `Map` tipi (`LinkedHashMap::new`) seçilebilir. Bu, `toMap`'in en sık düşülen tuzağıdır.

# stri-5 | İleri
## Soru
`partitioningBy` ile `groupingBy` arasındaki temel fark nedir?
## A
`partitioningBy` bir `List` döndürür, `groupingBy` ise bir `Map` nesnesi döndürür
🤖 İkisi de `Map` döndürür; fark anahtar kümesindedir, dönüş kabında değil.
## B
`partitioningBy` yalnız sayısal alanlar üzerinde çalışır, metinlerde kullanılamaz
🤖 Tip kısıtı yoktur; `partitioningBy` bir predicate (boolean koşul) alır, alan tipiyle ilgisi yoktur.
## C
`partitioningBy` sabit iki anahtar (`true`/`false`), `groupingBy` dinamik sayıda anahtar üretir
🤖 Doğru: partitioning sabit iki bölme, grouping dinamik çok bölme demektir.
## D
İkisi tamamen aynı işi yapar; yalnız metot isimleri ve okunuşları farklıdır
🤖 Aynı değil; biri sabit iki bölme, diğeri dinamik sayıda anahtar üretir.
## Doğru
C
## Açıklama
`partitioningBy(predicate)`, `groupingBy`'nin özel bir hâlidir: elemanları bir **boolean koşula** göre TAM IKI parçaya böler ve her zaman `Map<Boolean, List<T>>` üretir. Kritik incelik: koşulu sağlayan eleman OLMASA bile hem `true` hem `false` anahtarları HER ZAMAN mevcuttur (biri boş liste olur) — bu, koşulun her iki sonucuna güvenle erişebilmeni sağlar. `groupingBy(fn)` ise sınıflandırıcı fonksiyonun ürettiği kadar DINAMIK anahtar oluşturur (2, 5, 100...) ve yalnız gerçekten oluşan anahtarlar map'te bulunur. İkisi de alt-toplayıcı (downstream) alabilir: `partitioningBy(k -> k.getYas() >= 18, counting())` → `Map<Boolean, Long>`. Özet: sabit ikili bölme → `partitioningBy`; çok değerli/dinamik bölme → `groupingBy`.

# stri-6 | İleri
## Soru
Bir stream işleminde toplamı hesaplamak için `map` lambda'sı içinde çevreleyen sınıfın `toplam` ALANINI (`this.toplam += x`) artırmak neden tehlikelidir?
## A
Derleme hatası verir; lambda içinde erişilen her değişken effectively final olmak zorundadır
🤖 Effectively final kısıtı yalnız YEREL değişkenlere uygulanır; alan yazımı sorunsuz derlenir.
## B
Durumlu (stateful) bir yan etkidir; paralel stream'de yarış koşulu (race condition) ve yanlış sonuç üretir
🤖 Doğru: paylaşılan durumu değiştirmek stream sözleşmesini bozar ve paralelde bozulur.
## C
`map` yalnız `String` döndürebildiği için lambda içinde sayısal toplama yapılamaz
🤖 `map` her tipi döndürebilir; sorun dönüş tipi değil, paylaşılan durumu değiştirmektir.
## D
Yan etki, ara sonuçları önbelleğe aldığı için performansı artırır ve bu yüzden önerilir
🤖 Yan etki hem yanlış sonuç riski taşır hem de stream ilkelerine aykırıdır; önerilmez.
## Doğru
B
## Açıklama
Stream işlemleri, lambdaların **durumsuz** (stateless) ve **yan etkisiz** (side-effect-free) olmasını varsayar. `map`/`filter` lambda'sı içinde paylaşılan bir alanı (`this.toplam += x`) değiştirmek durumlu bir yan etkidir ve iki sorun doğurur: (1) **paralel stream'de** birden çok iş parçacığı aynı değişkeni eşzamanlı değiştirir → yarış koşulu (race condition), kayıp güncelleme, öngörülemez sonuç; (2) SIRALI stream'de bile bu, tembellik/kısa devre nedeniyle kaç kez çalışacağı garantisiz olduğundan kırılgandır. Doğru yol biriktirmeyi API'ye bırakmaktır: `mapToInt(...).sum()`, `reduce(0, Integer::sum)` veya `collect(Collectors.summingInt(...))` — bunlar hem sıralı hem paralelde doğru ve tekrarlanabilir sonuç verir. (Not: A şıkkı bu senaryoda YANLIŞTIR — effectively final kısıtı yalnız yakalanan YEREL değişkenler için geçerlidir, bir alana yazmak sorunsuz derlenir. Aynı işi yerel bir `int` ile denemek derleme hatası verirdi; ama alan ya da `AtomicInteger` kullanarak derleyiciyi susturmak asıl tasarım hatasını yalnızca gizler.)
