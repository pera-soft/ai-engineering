---
slug: list-arraylist-linkedlist
baslik: "List: ArrayList vs LinkedList"
kademe: Temel
sira: 20
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş listesi düşün: maddeler bir sıradadır, aynı madde iki kez yazılabilir ve "üçüncü satırda ne var?" diye sorabilirsin. Java'da bunun karşılığı `List`'tir — sıralı, indeksli ve tekrar (duplicate) kabul eden bir koleksiyon.

Aynı listeyi iki farklı şekilde saklayabilirsin. Ya bir kitap rafı gibi elemanları yan yana bitişik dizersin: 40. kitaba tek hamlede uzanırsın, ama araya kitap sıkıştırmak için sağdaki hepsini itmen gerekir. Ya da bir tren gibi kurarsın: her vagon bir sonrakinin yerini bilir; araya vagon eklemek iki bağlantı değiştirmekle biter, ama 40. vagona ulaşmak için vagonları tek tek yürümek gerekir. `ArrayList` raftır, `LinkedList` trendir. Aynı işi yaparlar; maliyetleri farklı yerdedir.

### Temel kavramlar

- **Big-O**: bir işlemin, eleman sayısı (n) büyüdükçe nasıl yavaşladığını anlatan gösterim. **O(1)** = eleman sayısı artsa da süre değişmez; 10 elemanda da 10 milyonda da aynı. **O(n)** = süre eleman sayısıyla orantılı; 10 kat eleman, 10 kat süre.
- **Dizi (array)**: bellekte **bitişik** (contiguous) duran, sabit boyutlu kutular. i numaralı kutunun adresi tek bir çarpma-toplamayla bulunur; aramaya gerek yoktur.
- **Dinamik dizi (dynamic array)**: dolunca kendini büyüten dizi. `ArrayList`'in içi budur.
- **Düğüm (node)**: kendi verisini ve komşusunun adresini taşıyan kutu. O adrese **işaretçi** (pointer) denir.
- **Çift bağlı liste (doubly linked list)**: her düğümün hem önceki hem sonraki düğümü işaret ettiği zincir. `LinkedList`'in içi budur.
- **Amortize edilmiş (amortized)**: tek tek bakınca bazı çağrılar pahalıdır, ama pahalı iş çok sayıda çağrıya yayıldığı için **ortalama** ucuzdur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ArrayList bitişik dizi, LinkedList dağınık düğümler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki iç yapı</text>
  <text x="120" y="52" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">ArrayList (bitişik dizi)</text>
  <rect x="30" y="64" width="46" height="40" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="76" y="64" width="46" height="40" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="122" y="64" width="46" height="40" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="168" y="64" width="46" height="40" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="53" y="89" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">0</text>
  <text x="99" y="89" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">1</text>
  <text x="145" y="89" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">2</text>
  <text x="191" y="89" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">3</text>
  <text x="120" y="126" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">get(i): O(1) · doğrudan adres</text>
  <text x="120" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ortaya ekle/sil: O(n) kaydırma</text>
  <text x="470" y="52" text-anchor="middle" font-size="13" font-weight="700" fill="#8A4B1E" font-family="system-ui">LinkedList (dağınık düğümler)</text>
  <rect x="360" y="64" width="52" height="40" rx="5" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <rect x="452" y="64" width="52" height="40" rx="5" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <rect x="544" y="64" width="52" height="40" rx="5" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <line x1="412" y1="84" x2="452" y2="84" stroke="#8A4B1E" stroke-width="1.5"/>
  <line x1="504" y1="84" x2="544" y2="84" stroke="#8A4B1E" stroke-width="1.5"/>
  <text x="470" y="126" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">düğüm elde ise ekle/sil: O(1)</text>
  <text x="470" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">get(i): O(n) düğümleri yürü</text>
  <text x="320" y="176" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Pratikte ArrayList genelde kazanır: bitişik bellek = CPU önbelleği (cache) dostu.</text>
  <text x="320" y="194" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">LinkedList'in dağınık düğümleri her adımda önbellek ıskası (cache miss) üretir.</text>
</svg>
```

### Nasıl çalışır?

`ArrayList` arkada bir dizi tutar. `get(i)` adresi doğrudan hesaplar ve okur → **O(1)**. Dizinin boyutu sabit olduğu için dolduğunda yerinde büyütülemez; bunun yerine **daha büyük yeni bir dizi** ayrılır ve eski elemanlar oraya kopyalanır. Java'da yeni boyut yaklaşık **1.5 kat**tır (`oldCapacity + (oldCapacity >> 1)`). Bu tekil işlem **O(n)**'dir; ama büyüme **katlanarak** (geometrik) olduğundan N ekleme boyunca toplam kopyalama maliyeti O(N)'de kalır — yani sona ekleme **amortize O(1)**'dir. Kapasite dolunca `add` istisna fırlatmaz ve liste tip değiştirmez; sessizce büyür. Buna karşılık `add(0, x)` gibi başa veya ortaya ekleme, sağdaki tüm elemanları bir konum **kaydırmayı** gerektirir → **O(n)**. Kaç eleman geleceğini biliyorsan `new ArrayList<>(beklenenBoyut)` ile başlangıç kapasitesi vermek yeniden ayırmaları (reallocation) ortadan kaldırır.

`LinkedList` ise düğümler kurar. `addFirst` yeni bir düğüm oluşturup **baş işaretçisini** günceller — kaydırma yok → **O(1)**. Ortadan ekleme/silme de yalnız birkaç işaretçiyi değiştirir; ama bu, o düğümü **elde tutuyorsan** geçerlidir. `get(500)` için baştan (uygulama en yakın uçtan başlar) düğüm düğüm **yürümek** gerekir → **O(n)**. Düğümler bellekte dağınık durur: bu tasarım ekleme/silmeyi ucuzlatırken erişimi pahalılaştırır.

### Mülakatta ne sorulur?

```svg
<svg viewBox="0 0 640 228" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ArrayList ve LinkedList işlem karmaşıklığı tablosu">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İşlem karmaşıklığı (Big-O)</text>
  <rect x="24" y="38" width="232" height="28" rx="4" fill="#1C3247"/>
  <rect x="256" y="38" width="176" height="28" rx="4" fill="#186399"/>
  <rect x="432" y="38" width="184" height="28" rx="4" fill="#B26B00"/>
  <text x="34" y="57" text-anchor="start" font-size="12.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">İşlem</text>
  <text x="344" y="57" text-anchor="middle" font-size="12.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">ArrayList</text>
  <text x="524" y="57" text-anchor="middle" font-size="12.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">LinkedList</text>
  <rect x="24" y="66" width="592" height="27" fill="#EAF1F7"/>
  <rect x="24" y="93" width="592" height="27" fill="#FFFFFF"/>
  <rect x="24" y="120" width="592" height="27" fill="#EAF1F7"/>
  <rect x="24" y="147" width="592" height="27" fill="#FFFFFF"/>
  <rect x="24" y="174" width="592" height="27" fill="#EAF1F7"/>
  <text x="34" y="84" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">get(i) rastgele erişim</text>
  <text x="344" y="84" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">O(1)</text>
  <text x="524" y="84" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">O(n)</text>
  <text x="34" y="111" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">addLast (amortize)</text>
  <text x="344" y="111" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">O(1)</text>
  <text x="524" y="111" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">O(1)</text>
  <text x="34" y="138" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">addFirst / removeFirst</text>
  <text x="344" y="138" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">O(n)</text>
  <text x="524" y="138" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">O(1)</text>
  <text x="34" y="165" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">ekle / sil (ortaya)</text>
  <text x="344" y="165" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">O(n)</text>
  <text x="524" y="165" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">O(n) *</text>
  <text x="34" y="192" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">contains / indexOf</text>
  <text x="344" y="192" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">O(n)</text>
  <text x="524" y="192" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">O(n)</text>
  <text x="320" y="218" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">* LinkedList'te O(1) araya-girme yalnız düğüm/imleç zaten elde ise; konuma yürümek O(n).</text>
</svg>
```

- **Kağıt üstündeki Big-O ile gerçek donanım aynı şeyi söylemez.** `ArrayList`'in bitişik belleği **önbellek yerelliği** (cache locality) sağlar: CPU bir **önbellek satırı** (cache line) çektiğinde sonraki elemanları da beraberinde getirir, iterasyon büyük ölçüde önbellekten beslenir. `LinkedList`'te her `next()` bir **işaretçi takibi** (pointer chasing) yapar ve dağınık düğümler sık **önbellek ıskası** (cache miss) üretip yavaş ana belleğe gider; üstelik her düğüm iki işaretçi için fazladan bellek harcar. Bu yüzden tablo `LinkedList`'i cazip gösterse de pratikte `ArrayList` çoğu iş yükünde kazanır. Bu fark otomatik paralellikten veya sıkıştırmadan değil, yalnız bellek erişim deseninden gelir.
- **`LinkedList` ne zaman gerçekten kazanır?** Yalnız iki dar durumda: (1) işlemler listenin **uçlarında** (baş/son) yapılıyorsa, (2) ekleme noktasını gösteren bir **düğüm/iteratör imleci** (cursor) zaten elindeyse. Konuma **yürümek** başlı başına O(n) olduğu için "ortaya O(1) ekleme" iddiası imleç olmadan geçersizdir.
- **Uçlardan işlem gerekiyorsa modern cevap `LinkedList` değil `ArrayDeque`'tir**: düğüm başına ek bellek ve önbellek ıskası maliyeti olmadan uçlarda O(1) verir.
- **`ArrayList` seçmenin sağlam gerekçeleri**: sık `get(i)` (rastgele erişim), küçük ve öngörülebilir bellek ayak izi (footprint), hızlı iterasyon. **Zayıf** gerekçe: elde tutulan bir imleçle listenin ortasına sürekli ekleme/silme — orada `LinkedList` haklıdır.
- `contains`/`indexOf` her iki yapıda da **O(n)**'dir; hızlı "var mı?" sorgusu istiyorsan `List` değil `HashSet` düşünülür.

### Tek cümlede

`ArrayList` bitişik diziyle O(1) erişim ve amortize O(1) sona ekleme verir, `LinkedList` işaretçilerle yalnız uçlarda veya elde imleç varken O(1) sunar; varsayılanın `ArrayList`, uçlar için `ArrayDeque` olsun.

# lst-1 | Temel
## Soru
`ArrayList` elemanlarını bellekte nasıl saklar?
## A
Arkada dinamik bir dizide (dynamic array), elemanları bellekte bitişik (contiguous) olarak
🤖 Doğru: `ArrayList` bir diziyle desteklenir; elemanlar bitişik durur, bu yüzden indeks erişimi O(1)'dir.
## B
Her elemanı ayrı bir düğümde (node) tutar ve düğümler işaretçilerle (pointer) birbirine bağlıdır
🤖 Bu tanım `LinkedList`'e aittir; `ArrayList` işaretçi tabanlı düğümler kullanmaz.
## C
Bir karma tablosunda (hash table), anahtarı indeks olan
🤖 `ArrayList` hash kullanmaz; bu `HashMap`/`HashSet` iç yapısıdır.
## D
Dengeli bir ikili arama ağacında (balanced BST)
🤖 Ağaç yapısı `TreeMap`/`TreeSet` içindir; `List` sıralı indeksli bir dizidir.
## Doğru
A
## Açıklama
`ArrayList`, içeride büyüyebilen bir **dizi** (array) tutar ve elemanları bellekte **bitişik** yerleştirir. Bitişiklik iki sonuç doğurur: indeksle erişim tek adımlık adres hesabıyla **O(1)** olur, ve bitişik bellek CPU **önbelleği** (cache) için idealdir. İşaretçiyle bağlı düğümler (B) `LinkedList`'e, hash (C) `HashMap`'e, ağaç (D) `TreeMap`'e özgüdür.

# lst-2 | Temel
## Soru
Bir listeden `list.get(500)` çağrısı yapıyorsun. `ArrayList` ve `LinkedList` için erişim karmaşıklığı (big-O) sırasıyla nedir?
## A
O(1) ve O(1)
🤖 `LinkedList`'te indeksli erişim doğrudan olamaz; 500. düğüme ulaşmak için yürümek gerekir.
## B
O(n) ve O(1)
🤖 Tam ters: bitişik dizi olan `ArrayList` doğrudan erişir; bağlı liste yürümek zorundadır.
## C
O(1) ve O(n)
🤖 Doğru: `ArrayList` diziden doğrudan (O(1)); `LinkedList` düğümleri baştan yürür (O(n)).
## D
O(log n) ve O(log n)
🤖 Logaritmik erişim ağaç yapılarına özgüdür; ne dizi ne de bağlı liste böyle davranır.
## Doğru
C
## Açıklama
`ArrayList` bir diziyle desteklendiğinden `get(i)` adresi doğrudan hesaplar → **O(1)**. `LinkedList` çift bağlı listedir; 500. elemana ulaşmak için baştan (veya sondan) düğüm düğüm **yürümek** gerekir → **O(n)** (uygulama en yakın uçtan başlar ama yine doğrusaldır). Bu yüzden rastgele erişim (random access) gereken senaryolarda `ArrayList` tercih edilir.

# lst-3 | Orta
## Soru
`ArrayList`'in başlangıç kapasitesi (capacity) dolduğunda `add` çağrısında ne olur?
## A
`IndexOutOfBoundsException` fırlatır çünkü dizi doludur
🤖 `add` sona ekleme için kapasiteyi otomatik büyütür; istisna fırlatmaz.
## B
Her `add`'de dizi tam bir eleman büyütülür, bu yüzden her ekleme O(n)'dir
🤖 Büyüme her eklemede değil, kapasite dolunca ve katlanarak (yaklaşık 1.5x) olur; bu yüzden ortalama O(1).
## C
Daha büyük dizi ayrılıp elemanlar kopyalanır; tek `add` bazen O(n) olur ama ortalama (amortized) O(1)'dir
🤖 Doğru: büyüme+kopyalama nadiren olur; masraf birçok eklemeye yayıldığından amortize edilmiş O(1).
## D
Liste otomatik olarak `LinkedList`'e dönüşür ve kapasite kavramı ortadan kalkar
🤖 Böyle bir tip dönüşümü yoktur; `ArrayList` her zaman dizi tabanlı kalır.
## Doğru
C
## Açıklama
Kapasite dolunca `ArrayList` **daha büyük** bir dizi ayırır (Java'da yaklaşık **1.5 kat**: `oldCapacity + (oldCapacity >> 1)`) ve eski elemanları kopyalar — bu tekil işlem **O(n)**'dir. Ancak büyüme **katlanarak** (geometrik) olduğundan, N ekleme boyunca toplam kopyalama maliyeti O(N)'de kalır; eklemenin **ortalama (amortized)** maliyeti **O(1)**'dir. Çok eleman ekleyeceğini biliyorsan `new ArrayList<>(beklenenBoyut)` ile başlangıç kapasitesi vermek yeniden ayırmaları (reallocation) ortadan kaldırır.

# lst-4 | Orta
## Soru
Bir listenin **başına** (indeks 0) sık sık eleman ekleyeceksin. Salt bu işlem açısından `ArrayList.add(0, x)` neden `LinkedList.addFirst(x)`'ten pahalıdır?
## A
`ArrayList` her eklemede tüm listeyi yeniden hash'ler
🤖 `ArrayList` hash kullanmaz; maliyet hash'leme değil eleman kaydırmadır.
## B
`ArrayList` başa eklemede tüm elemanları sağa kaydırır → O(n); `LinkedList` ise O(1)
🤖 Doğru: dizide araya girmek kaydırma gerektirir; bağlı listede baş düğüm eklemek sabit zamanlıdır.
## C
`LinkedList` elemanları sıralı tutmadığı için ekleme bedava olur
🤖 `LinkedList` de ekleme sırasını korur; ucuzluğun sebebi sıralamama değil, işaretçi güncellemesidir.
## D
İkisi de O(1)'dir, fark yoktur
🤖 `ArrayList.add(0, x)` kaydırma yüzünden O(n)'dir; eşit değillerdir.
## Doğru
B
## Açıklama
Dizide **0. indekse** eklemek, mevcut tüm elemanları bir konum **sağa kaydırmayı** gerektirir → **O(n)**. `LinkedList.addFirst` yalnız yeni bir düğüm oluşturup **baş işaretçisini** günceller → **O(1)**. Bu, `LinkedList`'in gerçekten parladığı dar senaryodur: uçlardan (baş/son) sık ekleme/silme. Yine de bu iş için modern Java'da genelde **`ArrayDeque`** önerilir — bağlı listenin düğüm başına bellek ve önbellek ıskası maliyetini taşımadan uçlarda O(1) sunar.

# lst-5 | Orta
## Soru
Teorik olarak `LinkedList` ortadan ekleme/silmede avantajlı görünür. Pratikte bir liste üzerinde **iterasyon + ardışık işlem** yaparken `ArrayList` neden genelde daha hızlı çalışır?
## A
`ArrayList` çok çekirdekli (multi-core) işlemede otomatik paralelleşir
🤖 Otomatik paralellik yoktur; fark tek çekirdekte bile bellek erişim deseninden gelir.
## B
Bitişik bellek CPU önbelleğine (cache) uyumludur; `LinkedList`'in dağınık düğümleri önbellek ıskası üretir
🤖 Doğru: önbellek yerelliği (cache locality) `ArrayList` lehine; dağınık düğümler ana belleğe sık gidiş yaratır.
## C
`LinkedList` her `next()` çağrısında elemanları yeniden sıralar
🤖 `next()` sadece bir sonraki işaretçiyi izler; yeniden sıralama yapmaz.
## D
`ArrayList` elemanları sıkıştırarak (compression) saklar, bu yüzden daha az bellek okur
🤖 Sıkıştırma yoktur; avantaj sıkıştırma değil bitişik yerleşimin önbellek dostu olmasıdır.
## Doğru
B
## Açıklama
`ArrayList`'in elemanları bellekte **bitişik** durduğundan, CPU bir bloğu önbelleğe (cache line) çektiğinde sonraki birkaç elemanı da **beraberinde** getirir — iterasyon büyük ölçüde önbellekten beslenir. `LinkedList` düğümleri belleğe **dağınık** serpiştirilmiştir; her `next()` bir işaretçiyi takip eder (**pointer chasing**) ve sıklıkla **önbellek ıskası** (cache miss) üretip yavaş ana belleğe gider. Ayrıca her düğüm işaretçiler için ek bellek harcar. Bu yüzden Big-O tabloları `LinkedList`'i cazip gösterse de, gerçek donanımda `ArrayList` çoğu iş yükünde kazanır.

# lst-6 | İleri
## Soru
Aşağıdakilerden hangisi `LinkedList` yerine `ArrayList` seçmek için **en zayıf** (en tartışmalı) gerekçedir?
## A
Rastgele indeksli erişim (`get(i)`) sık yapılıyor
🤖 Bu güçlü bir `ArrayList` gerekçesidir; O(1) erişim `LinkedList`'in O(n)'ine üstündür — zayıf değil.
## B
Bellek ayak izini (footprint) küçük ve önbellek (cache) davranışını öngörülebilir tutmak isteniyor
🤖 Bu da güçlü bir gerekçedir; `LinkedList` düğüm başına iki işaretçi için fazladan bellek harcar.
## C
Liste üzerinde baştan sona sık iterasyon yapılıyor ve iterasyon hızı önemli
🤖 Bu da güçlü bir gerekçedir; bitişik bellek iterasyonda önbellek dostudur, `ArrayList` kazanır.
## D
Elde tutulan bir iteratör imleciyle (cursor) listenin ortasına sürekli ekleme/silme yapılıyor
🤖 Doğru: bu senaryoda `LinkedList` gerçekten O(1) araya girer; dolayısıyla `ArrayList`'i savunmak için zayıf bir gerekçedir.
## Doğru
D
## Açıklama
Soru, `ArrayList` seçmek için hangi gerekçenin **en zayıf** olduğunu soruyor — yani `LinkedList`'in aslında kazandığı durumu. `LinkedList`'in O(1) araya-ekleme avantajı **yalnız** ekleme noktasını gösteren bir düğüm/imleç **zaten elindeyse** geçerlidir (D); bu, bağlı listenin gerçekten parladığı nadir senaryodur, dolayısıyla burada `ArrayList` savunmak zayıftır. A (O(1) erişim), B (küçük bellek + önbellek), C (hızlı iterasyon) ise `ArrayList` lehine **sağlam** gerekçelerdir. Kural: `LinkedList`'i ancak uçlardan işlem veya elde tutulan imleçle araya girme gibi dar senaryolarda düşün; varsayılanın `ArrayList` (uçlar için `ArrayDeque`) olsun.
