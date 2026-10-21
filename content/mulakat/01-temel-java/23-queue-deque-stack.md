---
slug: queue-deque-stack
baslik: "Queue, Deque ve Stack: FIFO, LIFO ve Öncelik"
kademe: Temel
sira: 23
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Markette kasa sırasına girersin: ilk gelen ilk çıkar. Bulaşıkta tabakları üst üste koyarsın: en son koyduğun ilk alınır. Acil serviste ise sıra değil **aciliyet** işler: en ağır hasta önce girer. Bu üç günlük hayat düzeni, Java'da üç yapıya karşılık gelir.

Ortak nokta: bu yapılarda "kaçıncı elemanı ver" diye sormazsın. Elemanların **hangi uçtan girip hangi uçtan çıktığı** kuralı belirler. Bu kural işi basitleştirir ve hızlandırır: iş kuyruğu, geri-al (undo) yığını, en kısa yol algoritması gibi yerlerde tam olarak bu davranış istenir.

### Temel kavramlar

- **FIFO (First-In-First-Out)**: ilk giren ilk çıkar — **kuyruk** (Queue), bankadaki sıra gibi.
- **LIFO (Last-In-First-Out)**: son giren ilk çıkar — **yığın** (Stack), üst üste konmuş tabaklar gibi.
- **Baş (head) / son (tail)**: kuyruğun çıkış ve giriş uçları.
- **`Deque` (çift uçlu kuyruk, double-ended queue)**: iki uçtan da ekleyip çıkarabildiğin yapı; hem FIFO hem LIFO olarak kullanılabilir.
- **Öncelik kuyruğu (PriorityQueue)**: ne FIFO ne LIFO; elemanları **önceliğe** göre verir.
- **İkili yığın / öbek (binary heap)**: `PriorityQueue`'nun iç yapısı. Yalnız "en öncelikli eleman **tepede**" değişmezini korur; tümünü sıralı tutmaz.
- **O(1)**: eleman sayısı artsa da süre değişmez. **O(log n)**: eleman sayısı ikiye katlanınca süreye bir adım eklenir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="FIFO kuyruk ve LIFO yığın yönleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı yapı, farklı uçlar</text>
  <text x="165" y="52" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Queue (FIFO)</text>
  <rect x="60" y="66" width="46" height="38" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="106" y="66" width="46" height="38" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="152" y="66" width="46" height="38" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="198" y="66" width="46" height="38" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="152" y="128" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">arkadan gir → önden çık</text>
  <text x="152" y="146" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ilk giren ilk çıkar</text>
  <text x="475" y="52" text-anchor="middle" font-size="13" font-weight="700" fill="#8A4B1E" font-family="system-ui">Stack (LIFO)</text>
  <rect x="452" y="66" width="46" height="38" rx="5" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <rect x="452" y="104" width="46" height="38" rx="5" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <rect x="452" y="142" width="46" height="38" rx="5" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <text x="560" y="92" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">üstten gir</text>
  <text x="560" y="110" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">üstten çık</text>
  <text x="560" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">son giren</text>
  <text x="560" y="166" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ilk çıkar</text>
  <text x="320" y="198" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ArrayDeque ikisini de yapar; legacy java.util.Stack'ten kaçın.</text>
</svg>
```

### Nasıl çalışır?

**`ArrayDeque`** dizi tabanlı, modern ve hızlı bir `Deque` uygulamasıdır. Yığın API'si **baş** uca eşlenir: `push` = `addFirst`, `pop` = `removeFirst`, `peek` = `peekFirst`. Aynı yapıyı `offer`/`poll` ile kullanırsan FIFO kuyruk olur. Önemli kısıt: `ArrayDeque` **`null` eleman kabul etmez** — çünkü `null`, `poll`/`peek` metotlarında "kuyruk boş" işareti olarak ayrılmıştır; içeride gerçek bir `null` olsa bu işaret anlamını yitirirdi.

**`java.util.Stack`** Java 1.0'dan kalma bir **legacy** sınıftır ve `Vector`'dan türer. Bu miras iki bedel getirir: her işlem **senkronizedir** (synchronized) — tek kanallı kodda gereksiz kilit maliyeti — ve `Vector`'dan indeksli erişim gibi bir yığında olmaması gereken metotlar sızar (zayıf kapsülleme). `push`/`pop` metotları vardır; sorun API eksikliği değil, mirastır. Modern kodda yığın için **`ArrayDeque`** kullanılır. Not: `ArrayDeque` **thread-safe değildir**; avantajı güvenli olması değil, gereksiz kilit taşımamasıdır. Eşzamanlılık gerekirse `ConcurrentLinkedDeque` veya `BlockingDeque` kullanılır.

**`PriorityQueue`** bir **ikili yığın** üzerine kuruludur. `offer` ve `poll` **O(log n)**, tepeye bakma (`peek`) **O(1)**'dir. `poll` her zaman **en yüksek öncelikli** — varsayılan doğal sırada **en küçük** — elemanı verir. Örneğin 30, 10, 20 eklenmiş bir `PriorityQueue<Integer>`'da ilk `poll` **10** döner; ne ilk eklenen ne son eklenen gelir, sonuç belirsiz de değildir. Öncelik ölçütü elemanın `Comparable`'ından ya da yapıcıya verilen `Comparator`'dan gelir; ikisi de yoksa kod **derlenir** ama ilk karşılaştırmada **`ClassCastException`** alırsın.

### Mülakatta ne sorulur?

```svg
<svg viewBox="0 0 640 194" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Queue metot aileleri: istisna fırlatan ve özel değer dönen">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki metot ailesi</text>
  <rect x="20" y="38" width="180" height="28" rx="4" fill="#1C3247"/>
  <rect x="200" y="38" width="210" height="28" rx="4" fill="#B26B00"/>
  <rect x="410" y="38" width="210" height="28" rx="4" fill="#25804C"/>
  <text x="30" y="57" text-anchor="start" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Amaç</text>
  <text x="305" y="57" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Fırlatan (istisna)</text>
  <text x="515" y="57" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Özel değer dönen</text>
  <rect x="20" y="66" width="600" height="30" fill="#EAF1F7"/>
  <rect x="20" y="96" width="600" height="30" fill="#FFFFFF"/>
  <rect x="20" y="126" width="600" height="30" fill="#EAF1F7"/>
  <text x="30" y="85" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">Ekle</text>
  <text x="305" y="85" text-anchor="middle" font-size="11.5" fill="#B26B00" font-family="system-ui">add(x) → istisna</text>
  <text x="515" y="85" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">offer(x) → false</text>
  <text x="30" y="115" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">Çıkar (baştan)</text>
  <text x="305" y="115" text-anchor="middle" font-size="11.5" fill="#B26B00" font-family="system-ui">remove() → istisna</text>
  <text x="515" y="115" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">poll() → null</text>
  <text x="30" y="145" text-anchor="start" font-size="11.5" fill="#1C3247" font-family="system-ui">Bak (baş)</text>
  <text x="305" y="145" text-anchor="middle" font-size="11.5" fill="#B26B00" font-family="system-ui">element() → istisna</text>
  <text x="515" y="145" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">peek() → null</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Boşluk normal ise offer/poll/peek; boşluk bir hata sayılıyorsa fırlatan aile.</text>
</svg>
```

- **İki metot ailesi.** **İstisna fırlatanlar**: `add` (kapasite dolu ise `IllegalStateException`), `remove` / `element` (boş ise `NoSuchElementException`). **Özel değer dönenler**: `offer` (dolu ise `false`), `poll` (boş ise `null`, elemanı çıkarır), `peek` (boş ise `null`, çıkarmadan bakar). İkisi de aynı yapıya erişir; fark hız değil **hata-ele-alma** stilidir. Kapasite sınırlı kuyruklarda (`BlockingQueue`) ve boşluk normal bir durumsa `offer`/`poll`/`peek`; boşluk bir **hata** sayılıyorsa fırlatan aile uygundur. İki aile de `Queue`/`Deque` arayüzünde birlikte tanımlıdır, yapıya göre ayrılmazlar.
- **`pop` ile `poll` farkı.** `ArrayDeque`'te ikisi de **baş** uçtan çıkarır — uç aynıdır. Fark yalnız boş durumdadır: `pop()` = `removeFirst()` **`NoSuchElementException`** atar, `poll()` = `pollFirst()` **`null`** döner. `push` sona değil **başa** ekler. Tutarlılık için tek bir modele sadık kal: yığın için `push`/`pop`, kuyruk için `offer`/`poll`.
- **`PriorityQueue` iterasyonu sıralı değildir.** Öbek yalnız tepe değişmezini korur; `for`/`toString`/`iterator` heap'in iç dizilişini gösterir. Öncelikli sırayla tüm elemanları almak için elemanlar tükenene dek **`poll()`** etmelisin. `Comparator` vermek de onu `TreeSet` gibi **tam sıralı** yapmaz; tam sıralı yapı istiyorsan `TreeSet`/`TreeMap` düşünülür.
- **`PriorityQueue` ne FIFO ne LIFO'dur**; FIFO/LIFO kuyruk ve yığın kavramlarıdır.

### Tek cümlede

`Queue` FIFO, `Stack` LIFO, `PriorityQueue` önceliğe göre çalışır; modern Java'da ilk ikisini de `ArrayDeque` karşılar ve her işlem için "istisna atan" (`add`/`remove`/`element`) ile "özel değer dönen" (`offer`/`poll`/`peek`) iki aile arasında bilinçli seçim yaparsın.

# qds-1 | Temel
## Soru
FIFO (First-In-First-Out) ve LIFO (Last-In-First-Out) davranışları hangi yapılara karşılık gelir?
## A
İkisi de aynı sırayı üretir, sadece isimleri farklıdır
🤖 Sıralar zıttır; kuyruk baştan, yığın sondan çıkarır.
## B
FIFO → Stack; LIFO → Queue
🤖 Tam ters: yığın LIFO, kuyruk FIFO'dur.
## C
FIFO → Queue (kuyruk); LIFO → Stack (yığın)
🤖 Doğru: kuyrukta ilk giren ilk çıkar; yığında son giren ilk çıkar.
## D
FIFO ve LIFO yalnızca `PriorityQueue` için tanımlıdır
🤖 `PriorityQueue` ne FIFO ne LIFO'dur; önceliğe göre çalışır. FIFO/LIFO kuyruk/yığın kavramlarıdır.
## Doğru
C
## Açıklama
**FIFO** (ilk giren ilk çıkar) **kuyruk** (Queue) davranışıdır — bir bankada sıra gibi. **LIFO** (son giren ilk çıkar) **yığın** (Stack) davranışıdır — üst üste konmuş tabaklar gibi; en son konan ilk alınır. `PriorityQueue` (D) bunların hiçbiri değildir; elemanları giriş sırasına değil **önceliğe** göre verir.

# qds-2 | Temel
## Soru
Modern Java'da bir **yığın** (stack) davranışı gerektiğinde neden `java.util.Stack` yerine `ArrayDeque` önerilir?
## A
`ArrayDeque` elemanları otomatik olarak sıralı (sorted) tuttuğu için `pop` en küçüğü verir
🤖 `ArrayDeque` sıralama yapmaz; yalnız LIFO/FIFO düzenini tutar, öncelik/sıralama vermez.
## B
`ArrayDeque` thread-safe olduğu için çok kanallı (multithread) kodda kilit gerekmeden güvenlidir
🤖 `ArrayDeque` senkronize DEĞİLDİR; avantajı thread-safe olması değil, gereksiz kilit taşımamasıdır.
## C
`java.util.Stack` `push`/`pop` metotlarına sahip değildir, yalnız `add`/`remove` sunar
🤖 `Stack` bu metotlara sahiptir; sorun API eksikliği değil, `Vector` mirası ve senkronizasyondur.
## D
`java.util.Stack` `Vector`'dan türer, gereksiz yere senkronizedir (synchronized) ve legacy'dir; `ArrayDeque` daha hızlı ve modern alternatiftir
🤖 Doğru: `Stack`'in `Vector` mirası her işlemde kilit maliyeti ve sızdıran bir API getirir; `ArrayDeque` tercih edilir.
## Doğru
D
## Açıklama
`java.util.Stack`, 1.0 döneminden kalma bir **legacy** sınıftır ve `Vector`'dan türer; bu yüzden her işlemi **senkronize** (synchronized) yapar — tek kanallı kodda gereksiz kilit maliyeti demektir. Ayrıca `Vector`'dan indeksli erişim gibi bir yığında olmaması gereken metotları da miras alır (kapsülleme zayıf). **`ArrayDeque`**, dizi tabanlı, senkronize olmayan, hızlı bir çift uçlu kuyruktur; `push`/`pop`/`peek` ile temiz bir yığın API'si sunar. Not: `ArrayDeque` **thread-safe değildir** (B yanlış); eşzamanlılık gerekirse `ConcurrentLinkedDeque` veya `BlockingDeque` kullanılır.

# qds-3 | Orta
## Soru
`offer`/`poll`/`peek` ile `add`/`remove`/`element` metot aileleri arasındaki temel fark nedir?
## A
İki aile tamamen aynıdır, sadece isimlendirme tercihidir
🤖 Davranış farkı gerçektir: başarısızlıkta biri istisna atar, diğeri özel değer döner.
## B
`offer`/`poll`/`peek` yalnızca `Stack`'te, `add`/`remove`/`element` yalnızca `Queue`'da bulunur
🤖 Her iki aile de `Queue`/`Deque` arayüzünde birlikte tanımlıdır; yapıya göre ayrılmazlar.
## C
`offer`/`poll`/`peek` başarısızlıkta özel değer (`false`/`null`) döner; `add`/`remove`/`element` ise istisna (exception) fırlatır
🤖 Doğru: ilk aile "yumuşak" (özel değer), ikinci aile "sert" (istisna) davranır.
## D
`add`/`remove`/`element` daha hızlıdır çünkü kontrol yapmaz
🤖 Hız farkı değil hata-ele-alma farkıdır; ikisi de aynı yapıya erişir.
## Doğru
C
## Açıklama
`Queue`/`Deque` iki paralel metot ailesi sunar. **İstisna fırlatanlar**: `add` (kapasite dolu ise `IllegalStateException`), `remove` / `element` (boş ise `NoSuchElementException`). **Özel değer dönenler**: `offer` (dolu ise `false`), `poll` (boş ise `null`, elemanı çıkarır), `peek` (boş ise `null`, çıkarmadan bakar). Kapasite sınırlı kuyruklarda (`BlockingQueue`) ve boşluk normalse `offer`/`poll`/`peek` tercih edilir — istisna kontrol akışını kirletmez. Boşluk bir **hata** sayılıyorsa istisna atan aile uygundur.

# qds-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(30);
pq.offer(10);
pq.offer(20);
System.out.println(pq.poll());
```
## A
`30` (ilk eklenen)
🤖 `PriorityQueue` FIFO değildir; ilk ekleneni değil en öncelikliyi verir.
## B
`10` (en küçük eleman)
🤖 Doğru: doğal sıralı PriorityQueue en küçüğü (min-heap) önce verir → 10.
## C
`20` (son eklenen)
🤖 Yığın (LIFO) değildir; son ekleneni vermez.
## D
Ekleme sırası korunmadığı için sonuç belirsizdir
🤖 Sonuç belirsiz değildir; öncelik kuralı nettir: en küçük eleman gelir.
## Doğru
B
## Açıklama
`PriorityQueue` bir **ikili yığın** (binary heap) üzerine kuruludur ve FIFO değildir: `poll` her zaman **en yüksek öncelikli** (varsayılan doğal sırada **en küçük**) elemanı verir. Burada elemanlar 30, 10, 20 eklenmiş olsa da `poll` **10**'u döndürür. Ekleme/çıkarma **O(log n)**, tepeye bakma (`peek`) **O(1)**'dir. Önemli tuzak: `PriorityQueue` üzerinde **iterasyon** (ör. `for`/`toString`) sıralı **değildir** — yalnız `poll` sırası önceliklidir; tamamen sıralı çıktı için tekrar tekrar `poll` etmelisin.

# qds-5 | Orta
## Soru
`ArrayDeque`'i bir **yığın** (LIFO) olarak kullanıyorsun. `push(x)` ve `pop()` hangi uçta çalışır ve `poll()`'dan farkı nedir?
## A
`push`/`pop` baş (head) uçta çalışır (LIFO); `poll` da baştan çıkarır, bu yüzden `pop` ile `poll` aynı uçta iş görür
🤖 Doğru: ArrayDeque'te push/pop ve poll hepsi baş uçtandır; push başa ekler, pop/poll baştan çıkarır.
## B
`push`/`pop` sonda, `poll` başta çalışır; bu yüzden karıştırılırsa FIFO olur
🤖 `ArrayDeque`'te `push` başa ekler (`addFirst`), sona değil; ifade yanlış.
## C
`push`/`pop` baş uçta çalışır ama `pop` boş yığında `null` döner, `poll` ise istisna fırlatır
🤖 Uç doğru, boş durum ters: `pop` boşta `NoSuchElementException` atar, `poll` `null` döner.
## D
`push` ile `poll` aslında aynı metodun iki adıdır; ikisi de sona ekleme yapar
🤖 `push` ekler, `poll` çıkarır; aynı metot değiller ve `push` sona değil başa ekler.
## Doğru
A
## Açıklama
`ArrayDeque`'te yığın API'si baş uca eşlenir: `push(x)` = `addFirst(x)` (başa ekler), `pop()` = `removeFirst()` (baştan çıkarır, boşsa `NoSuchElementException`). `poll()` = `pollFirst()` de **baştan** çıkarır ama boşta **`null`** döner. Yani `pop` ile `poll` **aynı uçta** (baş) iş görür; fark yalnız boş durumdaki davranıştır (istisna vs null). Bu tutarlılık sayesinde `ArrayDeque` hem LIFO yığın hem FIFO kuyruk olarak güvenle kullanılabilir — yeter ki tek bir kullanım modeline sadık kal (yığın için `push`/`pop`, kuyruk için `offer`/`poll`).

# qds-6 | İleri
## Soru
`PriorityQueue<Task>` oluşturup elemanları **önceliğe** göre almak istiyorsun ama `Task` doğal sıra tanımlamıyor. Ayrıca kuyruğu `for` ile dolaşınca sıralı çıkmadığını fark ettin. Aşağıdakilerden hangisi **doğrudur**?
## A
Yapıcıya bir `Comparator<Task>` vermelisin; iterasyon sıralı değildir, sıralı çıktı için `poll()` etmelisin
🤖 Doğru: öncelik bir Comparator'la tanımlanır; iterasyon heap iç düzenini gösterir, sıralı değil.
## B
`Task` `Comparable` uygulamadığından `PriorityQueue` derlenmez
🤖 Derlenir; sorun çalışma zamanında ilk karşılaştırmada `ClassCastException` olarak çıkar (Comparator yoksa).
## C
`for` ile dolaşmak da sıralı sonuç verir; sorun başka yerdedir
🤖 `PriorityQueue` iterasyonu sıralı değildir; yalnız `poll` sırası önceliklidir.
## D
Bir `Comparator` verirsen `PriorityQueue` bir `TreeSet` gibi tüm elemanları tam sıralı tutar
🤖 Heap yalnız "en öncelikli eleman tepede" değişmezini tutar; tam sıralı değildir.
## Doğru
A
## Açıklama
`PriorityQueue`, önceliği ya elemanın `Comparable`'ıyla ya da yapıcıya verilen bir **`Comparator`** ile belirler; `Task` doğal sıra tanımlamıyorsa `new PriorityQueue<>(comparator)` **vermelisin** (aksi halde ilk karşılaştırmada `ClassCastException`). Arkadaki **yığın/heap** yalnız "en öncelikli eleman **tepede**" değişmezini korur; **tam sıralı değildir**. Bu yüzden `for`/`toString`/`iterator` ile dolaşmak heap'in iç dizilişini gösterir, **sıralı çıktı vermez**. Öncelikli sırayla tüm elemanları almak için elemanları tükenene dek **`poll()`** etmen gerekir. Tam sıralı bir yapı istiyorsan `TreeSet`/`TreeMap` düşünülür (D'deki gibi `PriorityQueue` onlar gibi davranmaz).
