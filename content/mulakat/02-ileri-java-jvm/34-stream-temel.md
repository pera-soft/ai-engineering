---
slug: stream-temel
baslik: "Stream Temelleri: Tembellik, Ara/Sonlandırıcı İşlemler ve Tekrar Kullanım"
kademe: Orta
sira: 34
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir fabrikanın montaj bandını düşün. Bandın başında ham parçalar var, ortasında ayıklama ve şekil verme istasyonları, sonunda paketleme. Kritik nokta şu: banda düğmeye basılana kadar hiçbir istasyon çalışmaz, ve bir parça bandın SONUNA kadar tek başına gider — bant "önce hepsini ayıkla, sonra hepsine şekil ver" diye çalışmaz. Stream API tam olarak böyle bir banttır. Sen bandın nasıl kurulacağını tarif edersin ("şunları ele, kalanları şuna çevir, sonucu topla"), döngü yazmazsın. Bu bildirimsel (declarative) yazım hem daha okunur olur hem de kütüphaneye erken durma ve paralelleştirme gibi optimizasyon fırsatı verir.

### Temel kavramlar

**Kaynak (source):** Bandın başı — `list.stream()`, `Arrays.stream(dizi)`, `IntStream.range(...)`.

**Ara işlem (intermediate operation):** Bandın ortasındaki istasyonlar: `map`, `filter`, `sorted`, `distinct`, `limit`, `peek`... Her biri YENİ bir stream döndürür ve **tembeldir** (lazy) — çağrıldığında hiçbir eleman işlenmez, yalnız bandın bir halkası kurulur.

**Sonlandırıcı işlem (terminal operation):** Bandın düğmesi: `collect`, `forEach`, `reduce`, `count`, `findFirst`, `anyMatch`... Bandı ÇALIŞTIRIR ve bir sonuç (veya yan etki) üretir.

**Tembellik (laziness):** Sonlandırıcı işlem çağrılana kadar ara işlemlerin HİÇBİRİ yürütülmez.

**Boru hattı (pipeline):** Kaynak + ara işlemler + sonlandırıcı işlemin oluşturduğu bütün.

```svg
<svg viewBox="0 0 700 178" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Stream boru hattı: kaynak, ara işlemler tembel, sonlandırıcı hattı çalıştırır">
  <defs>
    <marker id="stream-pl-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="350" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Boru hattı: kaynak → ara → sonlandırıcı</text>
  <rect x="14" y="66" width="132" height="52" rx="9" fill="#EEF2F5" stroke="#5D7789" stroke-width="2"/>
  <text x="80" y="88" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">list.stream()</text>
  <text x="80" y="106" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">kaynak</text>
  <rect x="176" y="66" width="118" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="235" y="88" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#186399">filter</text>
  <text x="235" y="106" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">ara · tembel</text>
  <rect x="324" y="66" width="118" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="383" y="88" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#186399">map</text>
  <text x="383" y="106" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">ara · tembel</text>
  <rect x="472" y="66" width="160" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="552" y="88" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#25804C">collect</text>
  <text x="552" y="106" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">sonlandırıcı</text>
  <line x1="146" y1="92" x2="174" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#stream-pl-ar)"/>
  <line x1="294" y1="92" x2="322" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#stream-pl-ar)"/>
  <line x1="442" y1="92" x2="470" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#stream-pl-ar)"/>
  <text x="350" y="152" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#8A2E2E">sonlandırıcı gelene kadar HİÇBİR eleman işlenmez</text>
</svg>
```

**Kısa devre (short-circuit):** Sonuç kesinleştiği an bandı durdurabilen işlemler: `findFirst`, `findAny`, `limit`, `anyMatch`, `allMatch`, `noneMatch`.

**Kimlik değeri (identity):** Bir birleştirme işleminin "hiçbir şey yapmayan" nötr elemanı — toplamada `0`, çarpmada `1`, metin birleştirmede `""`.

### Nasıl çalışır?

Elemanlar hattı **tek tek (dikey)** kat eder: bir eleman `filter`'dan geçerse hemen `map`'e, oradan sonlandırıcıya ulaşır. "Önce tüm liste süzülür, sonra tümü dönüştürülür" (yatay işleme) diye bir şey YOKTUR. Kısa devrenin mümkün olmasının sebebi budur:

```java
// 10 milyon eleman olsa bile pahaliKontrol belki 3 kez çalışır
Optional<Kayit> ilk = kayitlar.stream()
        .filter(this::pahaliKontrol)
        .findFirst();
```

`findFirst` ilk uyan elemanı bulur bulmaz hat DURUR; kalan milyonlarca eleman hiç işlenmez. Bu tasarruf tamamen tembellikten gelir — `findFirst` arka planda paralellik EKLEMEZ.

```svg
<svg viewBox="0 0 620 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kısa devre: filter findFirst ilk eşleşen elemanda durur, kalan elemanlar hiç işlenmez">
  <text x="310" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Kısa devre — ilk eşleşmede DUR</text>
  <text x="310" y="46" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">filter(x&gt;0).findFirst() · elemanlar tek tek (dikey) akar</text>
  <rect x="36" y="58" width="88" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="80" y="78" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#25804C">3</text>
  <text x="150" y="78" font-family="system-ui" font-size="12.5" fill="#25804C" font-weight="700">filter ✓ → findFirst DÖNDÜ</text>
  <rect x="36" y="98" width="88" height="30" rx="6" fill="#EEF2F5" stroke="#5D7789" stroke-width="1.5"/>
  <text x="80" y="118" text-anchor="middle" font-family="system-ui" font-size="13" fill="#5D7789">-1</text>
  <text x="150" y="118" font-family="system-ui" font-size="12" fill="#8A2E2E">işlenmez</text>
  <rect x="36" y="138" width="88" height="30" rx="6" fill="#EEF2F5" stroke="#5D7789" stroke-width="1.5"/>
  <text x="80" y="158" text-anchor="middle" font-family="system-ui" font-size="13" fill="#5D7789">7</text>
  <text x="150" y="158" font-family="system-ui" font-size="12" fill="#8A2E2E">işlenmez</text>
  <rect x="36" y="178" width="88" height="30" rx="6" fill="#EEF2F5" stroke="#5D7789" stroke-width="1.5"/>
  <text x="80" y="198" text-anchor="middle" font-family="system-ui" font-size="13" fill="#5D7789">2</text>
  <text x="150" y="198" font-family="system-ui" font-size="12" fill="#8A2E2E">işlenmez</text>
  <rect x="28" y="94" width="470" height="120" rx="8" fill="none" stroke="#8A2E2E" stroke-width="1.5" stroke-dasharray="5 4"/>
  <text x="424" y="86" text-anchor="middle" font-family="system-ui" font-size="11" font-weight="700" fill="#8A2E2E">bu elemanlar hiç çalışmaz</text>
</svg>
```

Öte yandan, sonlandırıcı hiç yoksa hiçbir şey olmaz:

```java
list.stream().filter(x -> x > 0).map(x -> x * 2);   // geçerli kod, HİÇBİR etkisi yok
```

Bu satır derlenir, istisna atmaz, ama hiçbir eleman işlenmez — sonucu tüketilmediği için "ölü" bir hattır. Kural: stream'de bir şey olması için MUTLAKA bir sonlandırıcı işlem gerekir.

**Tek kullanımlık (single-use).** Bir stream yalnız BİR KEZ tüketilebilir. İlk sonlandırıcı işlem stream'i tüketip kapatır; aynı stream nesnesi üzerinde ikinci bir işlem çağırırsan `IllegalStateException: stream has already been operated upon or closed` fırlar. Kod DERLENİR (bu bir çalışma-zamanı kısıtıdır) ve ikinci çağrı sessizce boşa da düşmez, açıkça patlar. Stream tekrar tekrar gezilebilen bir veri yapısı değil, akıp geçen bir borudur; veriyi yeniden işlemek için `list.stream()`'i baştan çağırmak gerekir. Bu yüzden stream'ler alanlarda (field) saklanmaz — koleksiyon saklanır, stream ihtiyaç anında üretilir.

```svg
<svg viewBox="0 0 680 194" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek kullanımlık stream: ikinci sonlandırıcı işlem IllegalStateException fırlatır">
  <defs>
    <marker id="stream-su-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Stream tek kullanımlıktır</text>
  <rect x="20" y="54" width="150" height="52" rx="9" fill="#EEF2F5" stroke="#5D7789" stroke-width="2"/>
  <text x="95" y="85" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">list.stream()</text>
  <rect x="234" y="54" width="150" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="309" y="80" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#25804C">count() ✓</text>
  <text x="309" y="98" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">1. terminal · tüketildi</text>
  <rect x="448" y="54" width="196" height="52" rx="9" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="2"/>
  <text x="546" y="80" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#8A2E2E">forEach() ✗</text>
  <text x="546" y="98" text-anchor="middle" font-family="system-ui" font-size="11" fill="#8A2E2E">2. terminal · patlar</text>
  <line x1="170" y1="80" x2="232" y2="80" stroke="#5D7789" stroke-width="2" marker-end="url(#stream-su-ar)"/>
  <line x1="384" y1="80" x2="446" y2="80" stroke="#5D7789" stroke-width="2" marker-end="url(#stream-su-ar)"/>
  <text x="340" y="140" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#8A2E2E">IllegalStateException: stream has already been operated upon</text>
  <text x="340" y="164" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#5D7789">yeniden işlemek için list.stream()'i TEKRAR çağır</text>
</svg>
```

**İndirgeme — `reduce`.** İki yaygın biçimi vardır. `reduce(akümülatör)` başlangıç değeri almaz, bu yüzden boş stream olasılığına karşı `Optional<T>` döndürür. `reduce(identity, akümülatör)` ise bir kimlik değeri alır; boş stream'de bile identity'yi döndürür ve sonucu `Optional`'a sarmadan doğrudan verir:

```java
int toplam = stream.reduce(0, (a, b) -> a + b);   // kimlik 0 · Integer döner, kutu açılır
```

Kimliği yanlış seçmek sessiz hata üretir: toplamada `1` ile başlamak sonuca fazladan 1 ekler (1, ÇARPMANIN nötr elemanıdır). Ayrıca `Stream<Integer>` üzerinde `sum()` YOKTUR; `sum()` ve `average()` yalnız ilkel (primitive) stream'lerde — `IntStream`, `LongStream`, `DoubleStream` — bulunur. Geçiş `mapToInt(Integer::intValue).sum()` ile yapılır.

**`peek` — yalnız gözlem için.** `peek(Consumer)` tembel bir ARA işlemdir; sonlandırıcı yoksa hiçbir elemanı görmez, çünkü ara işlemler bağımsız çalışmaz. Dahası, kısa devre yapan bir sonlandırıcı (`findFirst`, `limit`) veya JDK'nın hattı sadeleştiren optimizasyonları yüzünden bazı elemanlar `peek` aşamasına HİÇ ulaşmayabilir; Java 9+ bazı durumlarda `peek`'i tamamen atlayabilir. Bu yüzden `peek`'e iş mantığı (business logic) ya da yan etki bağlamak güvenilmezdir ve üretim kodunda anti-pattern sayılır. `peek` elemanları dönüştürmez de — dönüşüm `map`'in işidir.

### Mülakatta ne sorulur?

- **Sonlandırıcısız hat ne yapar?** Hiçbir şey. Ne istisna atar ne eleman işler.
- **İkinci sonlandırıcı işlem.** `IllegalStateException`; derleme hatası değil, sessiz boş çalışma da değil.
- **Kısa devre neden mümkün?** Tembellik + dikey akış. `findFirst`, `limit`, `anyMatch`, `allMatch`, `noneMatch` erken durur.
- **`reduce` inceliği.** Kimlik değerinin doğru seçimi; tek argümanlı biçimin `Optional` döndürmesi; `sum()`'ın yalnız ilkel stream'lerde olması.
- **`peek` hakkında doğru ifade.** Tembel ara işlemdir; yalnız bir sonlandırıcı varsa ve eleman oraya ulaşırsa çalışır; gözlem içindir.
- **Ne zaman klasik `for` daha iyidir?** (1) Sıkı performans gereken sıcak yollarda — stream'in kurulum/lambda ek yükü ölçülebilir olabilir. (2) Erken `break`/`return`/`continue` gerektiğinde — stream'de bu dolaylıdır (`findFirst`, `takeWhile`). (3) Kontrol edilen istisnalar (checked exceptions) fırlatılacaksa — lambda içinde bunlar zahmetlidir. (4) Adım adım hata ayıklamada — satır satır breakpoint koymak hat izlemekten kolaydır. (5) İndeks tabanlı erişimde veya iki koleksiyonu paralel gezerken. Buna karşılık zincirleme dönüşüm, gruplama ve paralelleştirme (`parallelStream`) stream'in güçlü olduğu alandır. Doğru cevap "stil değil, göreve göre araç seçimi"dir.

### Tek cümlede

Stream tembel bir boru hattıdır: ara işlemler yalnız hattı kurar, sonlandırıcı işlem onu çalıştırır, elemanlar tek tek aktığı için erken durabilir ve hat bir kez tüketildikten sonra yeniden kullanılamaz.

# strm-1 | Orta
## Soru
`list.stream().filter(x -> x > 0).map(x -> x * 2);` satırı çalıştığında ne olur?
## A
Liste filtrelenir ve kalan elemanlar ikiyle çarpılır; işlem satırda hemen yürütülür
🤖 Sonlandırıcı işlem yok; hiçbir eleman işlenmez, sonuç da üretilmez.
## B
`filter` elemanları hemen süzer, `map` ise sonlandırıcı işleme kadar bekler
🤖 İkisi de ara işlemdir ve ikisi de tembeldir; hiçbiri hemen çalışmaz.
## C
`IllegalStateException` fırlatır: sonlandırıcı işlem olmadan hat kurulamaz
🤖 Stream henüz tüketilmedi; tekrar kullanım söz konusu değil, istisna atılmaz.
## D
Hiçbir eleman işlenmez; yalnız tembel bir işlem hattı kurulur
🤖 Doğru: `filter` ve `map` ara/tembel işlemlerdir; terminal işlem gelene dek yürütülmez.
## Doğru
D
## Açıklama
`filter` ve `map` **ara işlemlerdir** (intermediate) ve **tembeldir** (lazy): çağrıldıklarında yalnız bir işlem hattı TANIMLARLAR, hiçbir eleman işlenmez. Bir **sonlandırıcı işlem** (`collect`, `forEach`, `count`, `reduce` vb.) çağrılmadıkça hat asla yürütülmez. Bu kod tamamen geçerlidir, hiçbir istisna atmaz ama HIÇBIR ETKISI de yoktur — sonuç bir yere atanmadığı ve tüketilmediği için "ölü" bir hattır. Bu tembellik bilinçli bir tasarımdır: elemanların tek tek akmasını, optimizasyon (örn. `map`+`filter` birleştirme) ve kısa devre yapılmasını mümkün kılar. Kural: "Stream'de bir şey olması için MUTLAKA bir sonlandırıcı işlem gerekir."

# strm-2 | Orta
## Soru
Aynı stream değişkeni üzerinde iki sonlandırıcı işlem çağrılırsa (`s.count()` sonra `s.forEach(...)`) ne olur?
## A
İkinci çağrı `IllegalStateException` fırlatır: stream zaten tüketilmiştir
🤖 Doğru: stream tek kullanımlıktır; ikinci terminal işlem "already operated upon" hatası verir.
## B
İkinci işlem stream'i baştan işler, sorun olmaz
🤖 Stream başa sarılmaz; ikinci tüketim istisna fırlatır.
## C
İkinci işlem artık boş bir stream üzerinde çalışır ve sessizce hiçbir şey yapmaz
🤖 Sessiz boş çalışma yoktur; ikinci sonlandırıcı işlem doğrudan istisna fırlatır.
## D
Derleme hatası verir; derleyici ikinci sonlandırıcı çağrıyı baştan engeller
🤖 Kod derlenir; sorun çalışma-zamanında ortaya çıkan tekrar kullanım istisnasıdır.
## Doğru
A
## Açıklama
Bir stream **tek kullanımlıktır** (single-use). İlk sonlandırıcı işlem stream'i TÜKETIR ve kapatır; aynı stream nesnesi üzerinde ikinci bir sonlandırıcı (veya ara) işlem çağrılırsa `IllegalStateException: stream has already been operated upon or closed` fırlatır. Kod DERLENIR (D yanlış) çünkü bu bir çalışma-zamanı kısıtıdır; ikinci çağrı sessizce boşa da düşmez (C yanlış), açıkça patlar. Stream bir koleksiyon gibi tekrar tekrar gezinilebilir bir veri yapısı DEĞILDIR; akıp geçen bir boru hattıdır. Veriyi yeniden işlemek gerekiyorsa, stream'i KAYNAKTAN yeniden oluşturmak gerekir: her seferinde `list.stream()` çağırmak. Bu yüzden stream'ler alanlarda (field) saklanmaz; koleksiyon saklanır, stream ihtiyaç anında üretilir.

# strm-3 | İleri
## Soru
10 milyon elemanlı bir stream'de `filter(pahalıKontrol).findFirst()` neden tüm elemanları işlemeyebilir?
## A
`findFirst` stream'i arka planda paralel çalıştırıp ilk sonucu erken döndürdüğü için
🤖 `findFirst` otomatik paralelleştirmez; erken durması paralelizmden değil kısa devreden gelir.
## B
`filter` önce tüm listeyi süzüp yeni bir liste kurar, `findFirst` sonra onun ilkini alır
🤖 Yatay (önce tüm liste süzülür) işleme yoktur; elemanlar tek tek akar, bu yüzden erken durulabilir.
## C
Elemanlar hat boyunca tek tek (dikey) akar ve kısa devre yapan `findFirst` ilk eşleşmede durur
🤖 Doğru: tembel dikey akış + kısa devre sayesinde ilk eşleşmede işleme biter.
## D
`filter` tembel olmayan bir işlem olduğundan bütün elemanları önceden işlemek zorundadır
🤖 `filter` tembeldir; erken durmanın nedeni tam da bu tembelliktir.
## Doğru
C
## Açıklama
Stream işleme **tembel ve dikeydir** (vertical): elemanlar hattı TEK TEK kat eder — bir eleman `filter`'dan geçerse hemen `findFirst`'e ulaşır, tüm liste önce filtrelenmez (B yanlış). `findFirst` bir **kısa devre** (short-circuit) sonlandırıcı işlemdir: ilk uygun elemanı bulur bulmaz hat DURUR ve kalan milyonlarca eleman hiç işlenmez. Böylece `pahalıKontrol` belki yalnız birkaç kez çağrılır. Aynı kısa devre `limit`, `anyMatch`, `allMatch`, `noneMatch` için de geçerlidir. Bu, stream tembelliğinin en somut performans faydasıdır ve klasik "önce hepsini filtrele, sonra ilkini al" yaklaşımına göre büyük tasarruf sağlar. `findFirst` paralellik EKLEMEZ (A yanlış); tasarruf tamamen tembellikten gelir.

# strm-4 | Orta
## Soru
`Stream.reduce` ile bir `List<Integer>`'ın toplamını bulmak için doğru kullanım hangisidir?
## A
`stream.reduce((a, b) -> a + b)` her zaman bir `int` döndürür
🤖 İki argümanlı `reduce` `Optional<Integer>` döndürür; boş stream'de değer olmayabilir.
## B
`stream.reduce(0, (a, b) -> a + b)` — kimlik (identity) 0 ile toplar
🤖 Doğru: identity'li reduce boş stream'de bile 0 döner ve sonucu `Optional`'a sarmaz.
## C
`stream.reduce(1, (a, b) -> a + b)` — başlangıç 1 olmalı
🤖 Toplamada kimlik 0'dır; 1 ile başlamak sonuca yanlışlıkla 1 ekler.
## D
`stream.sum()` her stream türünde doğrudan çağrılabilir
🤖 `sum()` yalnız `IntStream` gibi ilkel stream'lerde vardır; `Stream<Integer>`'da yoktur.
## Doğru
B
## Açıklama
`reduce(identity, accumulator)` biçiminde **kimlik değeri** (identity), akümülatörün nötr elemanı olmalıdır — toplama için bu **0**'dır (`0 + x = x`). Bu biçim boş stream'de bile identity'yi (0) döndürür ve sonucu `Optional`'a sarmadan doğrudan verir. (Kesin tip ayrıntısı: `Stream<Integer>` üzerinde dönüş `Integer`'dır — `int`'e otomatik kutu açma ile atanır.) Kimliği 1 seçmek (C) sonuca yanlışlıkla 1 ekler — 1, ÇARPMANIN nötr elemanıdır. Tek argümanlı `reduce((a,b) -> a+b)` (A) identity içermediğinden `Optional<Integer>` döndürür (boş stream olasılığına karşı). `sum()` (D) yalnız `IntStream`/`LongStream`/`DoubleStream` gibi ilkel stream'lerde tanımlıdır; `Stream<Integer>` için önce `mapToInt(Integer::intValue).sum()` gerekir. Kimlik değerinin doğru seçimi reduce'un temel inceliğidir.

# strm-5 | İleri
## Soru
Ne zaman klasik `for` döngüsü, stream'e göre daha uygun bir tercihtir?
## A
Basit bir koleksiyonu okunabilir biçimde dönüştürüp toplamak gerektiğinde
🤖 Bu senaryo stream'in güçlü olduğu alandır; döngü burada avantaj sağlamaz.
## B
Elemanları birden çok çekirdekte paralel işlemek gerektiğinde
🤖 Paralellik stream'in kolaylaştırdığı iştir (`parallelStream`); döngüde iş parçacıklarını elle yönetmek gerekir.
## C
Zincirleme dönüşümler ve anahtara göre gruplama gerektiğinde
🤖 Zincirleme ve `groupingBy` stream'in en güçlü olduğu alandır; döngü burada daha hantal kalır.
## D
Erken `break`/`return`, kontrol edilen istisnalar veya adım adım hata ayıklama gerektiğinde
🤖 Doğru: bu durumlarda döngü daha doğrudan, öngörülebilir ve ayıklaması kolaydır.
## Doğru
D
## Açıklama
Stream her zaman "daha iyi" DEĞILDIR. Klasik `for` döngüsü şu durumlarda daha uygundur: (1) **sıkı performans** gereken sıcak yollar — stream'in kurulum/lambda ek yükü ölçülebilir olabilir; (2) **erken çıkış** — `break`/`return`/`continue` döngüde doğrudan, stream'de dolaylıdır (`findFirst`, `takeWhile` gerekir); (3) **kontrol edilen istisnalar** (checked exceptions) — lambda içinde bunları fırlatmak zahmetlidir; (4) **adım adım hata ayıklama** — döngüde satır satır breakpoint koymak, stream hattını izlemekten kolaydır; (5) indeks tabanlı erişim veya iki koleksiyonu paralel gezme. Buna karşılık stream, bildirimsel dönüşüm-filtreleme-gruplama ve paralelleştirme (`parallelStream`) gereken yerlerde çok daha nettir. Doğru cevap "stil değil, göreve göre araç seçimi"dir.

# strm-6 | İleri
## Soru
`peek` ara işlemi hakkında hangi ifade doğrudur?
## A
Tembel bir ara işlemdir; yalnız bir sonlandırıcı varsa yürütülür, gözlem içindir
🤖 Doğru: `peek` ara işlemdir; terminal işlem yoksa veya kısa devre bazı elemanları atlarsa çalışmayabilir.
## B
Sonlandırıcı bir işlemdir; hattı çalıştırır ve stream'i tüketip kapatır
🤖 `peek` ara işlemdir; stream'i tüketmez, yeni bir stream döndürür.
## C
Her elemanı mutlaka işler; sonlandırıcı işlem olsa da olmasa da bağımsız çalışır
🤖 Ara ve tembel olduğu için sonlandırıcı işlem olmadan hiçbir elemanı işlemez.
## D
Elemanları değiştirmek (mutate) için tasarlanmıştır ve `map`'in yerini alır
🤖 `peek` dönüştürmek için değil, gözlemlemek içindir; dönüşüm için `map` kullanılır.
## Doğru
A
## Açıklama
`peek(Consumer)` **tembel bir ara işlemdir** ve esas amacı hata ayıklama/gözlemdir (örn. hat boyunca akan elemanları loglamak). Ara işlem olduğu için, hattı çalıştıran bir **sonlandırıcı işlem** olmadan HIÇBIR şey yapmaz (C yanlış). Daha incesi: kısa devre yapan bir sonlandırıcı (`findFirst`, `limit`) veya optimizasyon nedeniyle bazı elemanlar hattın `peek` aşamasına HIÇ ULAŞMAYABILIR — bu yüzden `peek`'e iş mantığı (business logic) veya yan etki bağlamak GÜVENILMEZDIR; yalnız gözlem için kullanılmalıdır. `peek` elemanları dönüştürmez (`map`'in işi, D yanlış) ve sonlandırıcı değildir (B yanlış). Java 9+ bazı durumlarda `peek`'i tamamen atlayacak optimizasyonlar yapabildiğinden, üretim kodunda `peek`'e bel bağlamak bir anti-pattern sayılır.
