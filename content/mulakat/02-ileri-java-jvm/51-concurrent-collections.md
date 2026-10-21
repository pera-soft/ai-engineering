---
slug: concurrent-collections
baslik: "Concurrent Koleksiyonlar: ConcurrentHashMap ve Ötesi"
kademe: İleri
sira: 51
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir markette tek kasa varsa herkes o kasanın önünde tek sıra olur: biri ödeme yaparken diğerleri bekler. On kasa açarsan müşteriler dağılır ve aynı anda ilerler. Eşzamanlı koleksiyonlar da bunu yapar: birçok iş parçacığının (thread) aynı veri yapısına aynı anda güvenle dokunmasını sağlar, ama hepsini tek bir kapıdan geçirmeye zorlamaz. Çözdükleri problem şudur: iki thread aynı listeyi/haritayı aynı anda değiştirirse veri bozulur; klasik "her şeyi tek kilitle koru" çözümü ise doğru ama yavaştır.

### Temel kavramlar

- **Kilit (lock)**: Bir anda yalnız tek thread'in girebildiği kapı; Java'da `synchronized` bunu sağlar.
- **Eşzamanlı koleksiyon (concurrent collection)**: `java.util.concurrent` altındaki, dıştan `synchronized` sarmadan iş parçacığı-güvenli (thread-safe) çalışan veri yapısı.
- **CAS (compare-and-swap, karşılaştır-ve-değiştir)**: "Değer hâlâ X ise Y yap" diyen, kilit almadan çalışan tek adımlık işlem.
- **Kova (bin/bucket)**: Hash tablosunda anahtarların düştüğü gözlerden biri.
- **Atomik (atomic)**: Bölünemez; ya tamamen olur ya hiç olmaz, ortasına başkası giremez.

Üç aile, üç farklı eşzamanlılık modeli:

- **`ConcurrentHashMap`**: Okumalar genellikle KİLİTSİZDİR; yazmalar tüm map'i değil, yalnız ilgili kova/düğümü (bin) CAS ve dar kilitle günceller. Yüksek eşzamanlı okuma/yazma için standart seçimdir. Eski `Hashtable`/`Collections.synchronizedMap` ise TEK global kilit kullanır (darboğaz).
- **`CopyOnWriteArrayList`**: Her YAZMADA altındaki diziyi kopyalar. Okuma bol, yazma çok nadirken (ör. dinleyici/listener listeleri) idealdir; yazma-ağırlıklıysa felakettir.
- **`BlockingQueue`** (ör. `ArrayBlockingQueue`, `LinkedBlockingQueue`): `put`/`take` bloklar — kuyruk doluysa üretici, boşsa tüketici bekler; üretici-tüketici deseninde manuel `wait/notify` ihtiyacını ortadan kaldırır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Eşzamanlı koleksiyonların kilit modelleri">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kilit modeli: tek kilit mi, dar kilit mi?</text>
  <rect x="24" y="46" width="285" height="130" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="166" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#B4322B" font-family="system-ui">Hashtable / synchronizedMap</text>
  <text x="166" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">TEK global kilit</text>
  <text x="166" y="122" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">tüm map bir anda tek thread'e</text>
  <text x="166" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yüksek çekişmede darboğaz</text>
  <rect x="331" y="46" width="285" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">ConcurrentHashMap</text>
  <text x="473" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">okuma kilitsiz</text>
  <text x="473" y="122" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">yazma yalnız ilgili kovaya (CAS)</text>
  <text x="473" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yüksek eşzamanlılık</text>
  <text x="320" y="198" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Tek metod atomiktir; metodlar ARASI birleşim atomik DEĞİLDİR.</text>
</svg>
```

### Nasıl çalışır?

`Hashtable` ve `Collections.synchronizedMap` tüm yapıyı TEK kilitle korur: bir thread yazarken diğer herkes bekler. `ConcurrentHashMap` ise okumaları çoğunlukla KİLİTSİZ yapar (değerler `volatile` görünürlükle okunur) ve yazmayı yalnız ilgili KOVAYA yöneltir: önce CAS denenir, çekişme olursa o kovanın ilk düğümüne dar bir `synchronized` alınır. Böylece farklı kovalara yazan thread'ler birbirini hiç engellemez. Hız, "kilit hiç kullanılmamasından" değil bu dar taneli (fine-grained) modelden gelir — sınıf tabii ki thread-safe'tir.

Sık sorulan üç ayrıntı:

- **Segment yok**: Java 7'de map, sabit sayıda `Segment` nesnesine bölünürdü ve kilit segment başınaydı. Java 8'den itibaren bu yapı KALDIRILDI; artık eşzamanlılık, kova (bin) başına CAS ve çekişme olursa o kovanın ilk düğümüne alınan dar `synchronized` kilitle sağlanır. "Segment kilitleme" cevabı bugün eskimiştir.
- **`null` yasak**: Anahtar da değer de `null` OLAMAZ (`NullPointerException` atılır) — oysa `HashMap` ikisine de izin verir. Sebep: eşzamanlı bir map'te `get(k) == null` sonucu "anahtar yok" ile "değeri null" arasında ayrılamaz; bu belirsizlik yarış koşullarında çözülemeyeceği için tasarım gereği yasaklanmıştır.
- **`size()` yaklaşıktır**: Sayım, eşzamanlı yazmalar sürerken kilit almadan hesaplanır; dönen değer o an anlık-doğru olmayabilir (yaklaşık/tahmini). Kesin sayıma iş mantığı bağlanmamalıdır; `isEmpty()` de aynı zayıflığı taşır.

Kritik tuzak — **atomiklik illüzyonu**: `ConcurrentHashMap` her BİR metodunu (put, get, remove) atomik yapar; ama İKİ metodun birleşimi atomik DEĞİLDİR.

```java
// YANLIŞ — kontrol-sonra-hareket (check-then-act): iki thread de false görüp ikisi de yazar
if (!map.containsKey(k)) map.put(k, v);

// DOĞRU — işlemi tek atomik metoda indir
map.putIfAbsent(k, v);
map.computeIfAbsent(k, anahtar -> pahaliUret(anahtar));
map.merge(k, 1, Integer::sum);   // birikimli güncelleme
```

Dıştan kilit eklemek çözüm değildir: TEK bir `put`'u ayrıca `synchronized`'a almak zaten atomik olanı yavaşlatıp ölçeklenmeyi öldürür; `Collections.synchronizedMap` ile sarmalamak da yalnız tek tek çağrıları kilitler — aradaki boşluk yine açıktır. `compute`/`merge`/`computeIfAbsent`'e verdiğin yeniden-eşleme (remapping) fonksiyonu ise o anahtarın düğümü KİLİTLİ tutulurken çalışır; atomikliğin bedeli budur. Bu yüzden fonksiyon KISA, hızlı ve yan etkisiz olmalıdır ve içinden AYNI map'e yazmak — farklı kovaya düşse bile — yasaktır: yeniden giriş kilitlenmeye ya da tanımsız davranışa yol açar. Atomiklik metodun kendisinde sağlandığı için dış kilit de gerekmez.

`CopyOnWriteArrayList` bambaşka bir modeldir: kilit yerine her yazmada tüm diziyi kopyalar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CopyOnWriteArrayList her yazmada diziyi kopyalar">
  <defs>
    <marker id="cc-arrow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0,0 L9,4.5 L0,9 z" fill="#B26B00"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">CopyOnWriteArrayList: yaz = tüm diziyi kopyala</text>
  <text x="110" y="56" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">Eski dizi (snapshot)</text>
  <rect x="34" y="70" width="38" height="34" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="74" y="70" width="38" height="34" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="114" y="70" width="38" height="34" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="53" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">A</text>
  <text x="93" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">B</text>
  <text x="133" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C</text>
  <text x="110" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">okuyucular kilitsiz gezer</text>
  <line x1="200" y1="87" x2="288" y2="87" stroke="#B26B00" stroke-width="2" marker-end="url(#cc-arrow)"/>
  <text x="244" y="78" text-anchor="middle" font-size="10.5" font-weight="600" fill="#B26B00" font-family="system-ui">add(D) → kopyala</text>
  <text x="430" y="56" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">Yeni dizi (yazma sonrası)</text>
  <rect x="320" y="70" width="38" height="34" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <rect x="360" y="70" width="38" height="34" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <rect x="400" y="70" width="38" height="34" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <rect x="440" y="70" width="38" height="34" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="339" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">A</text>
  <text x="379" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">B</text>
  <text x="419" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C</text>
  <text x="459" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">D</text>
  <text x="399" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">referans atomik değişir</text>
  <rect x="34" y="150" width="572" height="46" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="320" y="169" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Yazma maliyeti O(n): oku-bol / yaz-nadir (listener) için ideal.</text>
  <text x="320" y="187" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yazma-ağırlıklı ya da büyük listede felaket</text>
</svg>
```

Yazma maliyeti O(n) olduğundan liste büyükse veya yazma sıksa felakettir; buna karşılık okumalar tamamen kilitsizdir ve iteratör, iterasyonun başladığı andaki dizinin anlık görüntüsü (snapshot) üzerinde gezer. Bu yüzden iterasyon sırasında liste değişse bile `ConcurrentModificationException` (CME) ATMAZ. Klasik `ArrayList`/`HashMap` iteratörleri ise FAIL-FAST'tir: yapısal değişiklikte bir `modCount` uyuşmazlığı yakalanır ve CME — bir ÇALIŞMA-ZAMANI istisnası, derleme hatası değil — atılır. `ConcurrentHashMap`'in iteratörü ZAYIF-TUTARLIDIR (weakly consistent); o da CME atmaz, yalnız değişikliği görme garantisi farklıdır. Sıradan bir listeyi `synchronized`'a almak görünürlüğü ayarlar ama fail-fast davranışını tek başına değiştirmez; iterasyon içinde silmek için `Iterator.remove()` gerekir.

`BlockingQueue` ise üretici-tüketici koordinasyonunu kuyruğun içine gömer: `put(x)` kuyruk DOLUYSA üreticiyi, `take()` kuyruk BOŞSA tüketiciyi bekletir. Böylece "tampon dolu mu, boş mu" kontrolünü ve `wait/notify` dansını elle yazmaktan kurtulursun. `ArrayBlockingQueue` sınırlı kapasitesiyle geri-basınç (backpressure) sağlar, `LinkedBlockingQueue` isteğe bağlı sınırla çalışır; bloklamadan denemek istersen `offer`/`poll` (isteğe bağlı zaman aşımıyla) varyantları vardır. Tüm bu implementasyonlar thread-safe'tir, elle senkronizasyon gerektirmez.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="BlockingQueue üretici-tüketici bloklama semantiği">
  <defs>
    <marker id="cc-flow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0,0 L9,4.5 L0,9 z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">BlockingQueue: bloklama koordinasyonu kuyruğa devreder</text>
  <text x="320" y="50" text-anchor="middle" font-size="11" font-weight="600" fill="#186399" font-family="system-ui">sınırlı kuyruk (tampon)</text>
  <rect x="24" y="58" width="120" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="84" y="88" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Üretici</text>
  <text x="84" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">put() · doluysa bekler</text>
  <line x1="148" y1="94" x2="212" y2="94" stroke="#186399" stroke-width="2" marker-end="url(#cc-flow)"/>
  <rect x="218" y="58" width="204" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="232" y="80" width="30" height="30" rx="4" fill="#186399"/>
  <rect x="266" y="80" width="30" height="30" rx="4" fill="#186399"/>
  <rect x="300" y="80" width="30" height="30" rx="4" fill="#186399"/>
  <rect x="334" y="80" width="30" height="30" rx="4" fill="#ffffff" stroke="#186399" stroke-width="1.2"/>
  <rect x="368" y="80" width="30" height="30" rx="4" fill="#ffffff" stroke="#186399" stroke-width="1.2"/>
  <line x1="426" y1="94" x2="490" y2="94" stroke="#186399" stroke-width="2" marker-end="url(#cc-flow)"/>
  <rect x="496" y="58" width="120" height="72" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="556" y="88" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Tüketici</text>
  <text x="556" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">take() · boşsa bekler</text>
  <text x="320" y="166" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Manuel wait/notify yok; geri-basınç (backpressure) otomatik.</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ArrayBlockingQueue sınırlı · offer/poll bloklamadan dener</text>
</svg>
```

### Mülakatta ne sorulur?

- **CHM'de segment YOK** (Java 8+): kova başına CAS + çekişmede ilk düğüme dar `synchronized`. Java 7'nin `Segment` cevabı eskimiştir. Kazanç tek kilidin hızlı alınmasından değil, dar kilitten gelir.
- **`null` yasak**: anahtar da değer de `null` olamaz → NPE. Sebebi `get(k) == null` belirsizliğidir. `HashMap` ikisine de izin verir.
- **`size()` yaklaşıktır**, `isEmpty()` de öyle: kilitsiz sayılır, iş mantığı buna bağlanmaz.
- **Bileşik işlem atomik değildir**: `containsKey` + `put` bölünebilir. Doğrusu `putIfAbsent` / `computeIfAbsent` / `merge`. Tek `put`'u dıştan kilitlemek gereksiz, `synchronizedMap` ile sarmak yetersizdir.
- **remap fonksiyonu**: düğüm kilidi altında çalışır → kısa, hızlı, yan etkisiz olmalı; aynı map'e yeniden girmek yasaktır.
- **COW = snapshot iteratör**: CME atmaz, yazma O(n). Listener listesi için ideal; iş kuyruğu için `BlockingQueue` gerekir. `ConcurrentHashMap` iteratörü zayıf-tutarlıdır; `ArrayList` fail-fast'tir ve CME runtime istisnasıdır.

### Tek cümlede

Eşzamanlı koleksiyonlar tek global kilidi kaldırıp eşzamanlılığı yapının içine gömer; ama garanti ettikleri şey tek metodun atomikliğidir — bileşik işlemi atomik yapmak hâlâ senin işindir.

# cc-1 | Temel
## Soru
`ConcurrentHashMap` yüksek eşzamanlı erişimde neden `Hashtable`'dan daha iyi ölçeklenir?
## A
Tüm map'i yine tek bir global kilitle korur ama bu kilidi diğerlerinden daha hızlı alıp bırakır
🤖 O eski `Hashtable`/`synchronizedMap` modeli; tek kilit darboğaz yaratır.
## B
Her okuma ve yazmada map'in tamamının bir kopyasını çıkarıp o kopya üzerinde çalışır
🤖 O `CopyOnWrite` modeli; `ConcurrentHashMap` böyle çalışmaz.
## C
Okumalar çoğunlukla kilitsizdir; yazma yalnız ilgili kovayı (bin) CAS/dar kilitle günceller
🤖 Doğru: dar taneli (fine-grained) kilitleme ölçeklenmeyi sağlar.
## D
Aslında thread-safe değildir; kilit hiç kullanmadığı için yalnızca daha hızlı görünür
🤖 `ConcurrentHashMap` thread-safe'tir; asıl kazanç dar kilit modelidir.
## Doğru
C
## Açıklama
`Hashtable` ve `Collections.synchronizedMap` TEK bir kilitle tüm yapıyı korur: bir thread yazarken diğer herkes bekler (darboğaz). `ConcurrentHashMap` ise okumaları çoğunlukla KİLİTSİZ yapar (değerler `volatile` görünürlükle okunur) ve yazmalarda tüm map'i değil yalnız ilgili KOVAYI (bin) hedefler — genelde CAS, çekişme olursa o düğüme dar bir kilit. Böylece farklı kovalara yazan thread'ler birbirini engellemez. Bu dar taneli model, çok çekirdekli ortamda yüksek eşzamanlılık sağlar. `CopyOnWrite` (B) ayrı bir tekniktir ve map'te değil listede kullanılır.

# cc-2 | Orta
## Soru
`CopyOnWriteArrayList` hangi kullanım deseni için en uygundur?
## A
Yazma-ağırlıklı, her istekte birçok kez güncellenen listeler
🤖 Her yazmada dizi kopyalandığından yazma-ağırlıklı kullanımda çok pahalıdır.
## B
Milyonlarca elemanlı, sürekli eleman eklenip çıkarılan büyük listeler
🤖 Her değişiklikte tüm dizi kopyalanır; büyük + değişken listede maliyet patlar.
## C
Üretici-tüketicinin sık `add`/`remove` yaptığı iş kuyruğu (work queue) yapıları
🤖 Kuyruk için `BlockingQueue` uygundur; her ekleme kopya üretmez.
## D
Okumanın çok, yazmanın çok nadir olduğu listeler (ör. dinleyici/listener listeleri)
🤖 Doğru: oku-bol/yaz-nadir deseni için tasarlanmıştır.
## Doğru
D
## Açıklama
`CopyOnWriteArrayList`, HER değişiklikte (add/set/remove) altındaki diziyi baştan KOPYALAR ve referansı atomik olarak değiştirir. Bu yüzden yazma maliyeti O(n)'dir — yazma sık veya liste büyükse felakettir (A, B, C). Buna karşılık okumalar tamamen kilitsizdir ve iteratör, iterasyonun başladığı andaki dizinin anlık görüntüsü (snapshot) üzerinde gezer; bu yüzden iterasyon sırasında liste değişse bile `ConcurrentModificationException` ATMAZ. İdeal senaryo: nadiren güncellenen ama çok sık okunan/iterasyona sokulan olay dinleyicisi (listener) listeleri gibi yapılardır.

# cc-3 | Orta
## Soru
Üretici-tüketici (producer-consumer) deseninde `BlockingQueue` ne sağlar?
## A
`put` kuyruk doluyken, `take` kuyruk boşken bloklar; koordinasyon kuyruğun içine gömülüdür
🤖 Doğru: bloklama semantiği manuel `wait/notify` ihtiyacını kaldırır.
## B
Hiçbir koşulda bloklamaz; kuyruk dolu ya da boş olsa da çağrılar anında geri döner
🤖 `BlockingQueue`'nun tanımı tam olarak bloklamaktır (doluysa/boşsa).
## C
Thread-safe değildir; üretici ve tüketici erişimini elle senkronize etmen gerekir
🤖 `BlockingQueue` implementasyonları thread-safe'tir; manuel kilit gerekmez.
## D
Aynı anda yalnızca tek eleman tutabilir; sıra (FIFO) kavramı yoktur
🤖 Sınırlı ya da sınırsız kapasiteli tam bir kuyruktur; tek elemanlık değildir.
## Doğru
A
## Açıklama
`BlockingQueue`, üretici-tüketici koordinasyonunu KUYRUĞUN İÇİNE gömer: `put(x)` kuyruk DOLUYSA üreticiyi bekletir, `take()` kuyruk BOŞSA tüketiciyi bekletir. Böylece "tampon dolu mu, boş mu" kontrolünü ve `wait/notify` dansını elle yazmaktan kurtulursun — hem daha az hata hem daha okunur kod. `ArrayBlockingQueue` sınırlı kapasiteyle geri-basınç (backpressure) sağlarken, `LinkedBlockingQueue` isteğe bağlı sınırla çalışır. Bloklamadan denemek istersen `offer`/`poll` (isteğe bağlı zaman aşımıyla) varyantları vardır.

# cc-4 | İleri
## Soru
Bir koleksiyonu iterasyonla gezerken üzerinde değişiklik yapmak `ConcurrentModificationException`'a (CME) yol açar. Bunu güvenle yapmak için hangisi doğrudur?
## A
Sıradan `ArrayList` üzerinde gelişmiş `for` döngüsü içinde eleman silmek güvenlidir
🤖 `ArrayList` iteratörü fail-fast'tir; iterasyon sırasında değişiklik CME atar.
## B
Eşzamanlı koleksiyonların iteratörleri CME atmaz; `ArrayList`'in fail-fast iteratörü ise atar
🤖 Doğru: `ConcurrentHashMap` zayıf-tutarlı, `CopyOnWriteArrayList` anlık-görüntü tabanlıdır.
## C
CME bir derleme (compile) hatasıdır; böyle bir kod zaten derlenip çalıştırılamaz
🤖 CME çalışma-zamanı (runtime) istisnasıdır; kod sorunsuz derlenir.
## D
Koleksiyona yapılan tüm erişimleri `synchronized` bloğa almak CME'yi tek başına çözer
🤖 Kilit görünürlüğü ayarlar ama fail-fast iteratörün CME'sini engellemez.
## Doğru
B
## Açıklama
`ArrayList`, `HashMap` gibi klasik koleksiyonların iteratörleri FAIL-FAST'tir: iterasyon sırasında yapısal değişiklik olursa bir `modCount` uyuşmazlığı yakalanır ve `ConcurrentModificationException` (bir çalışma-zamanı istisnası, C yanlış) atılır. Buna karşılık `ConcurrentHashMap`'in iteratörü ZAYIF-TUTARLIDIR (weakly consistent) ve `CopyOnWriteArrayList` ANLIK-GÖRÜNTÜ (snapshot) üzerinde gezer; ikisi de iterasyon sırasında değişiklikte CME ATMAZ (yalnız değişikliği görme garantisi farklıdır). Sıradan bir listeyi `synchronized`'a almak (D) görünürlüğü ayarlar ama tek başına fail-fast davranışını değiştirmez; iterasyon içinde silmek için `Iterator.remove()` gerekir.

# cc-5 | İleri
## Soru
`ConcurrentHashMap` kullanırken en yaygın "atomiklik illüzyonu" hatası hangisidir?
## A
Tek bir `put(k, v)` çağrısının bile atomik olmadığını sanıp onu ayrıca `synchronized` bloğa almak
🤖 Ters yönde bir yanılgı: TEK metod zaten atomiktir, dış kilit gereksiz yere ölçeklenmeyi öldürür.
## B
`ConcurrentHashMap`'i `Collections.synchronizedMap` ile sarıp bileşik işlemlerin de atomikleştiğini sanmak
🤖 Sarmalamak yalnız tek tek çağrıları kilitler; kontrol-sonra-hareket yine bölünebilir kalır.
## C
`if (!map.containsKey(k)) map.put(k, v)` gibi kontrol-sonra-hareket dizisini atomik sanmak
🤖 Doğru: iki thread araya girebilir; `putIfAbsent`/`compute`/`merge` gerekir.
## D
`size()`'ın her zaman kesin, kilitli ve anlık doğru bir değer döndürdüğünü sanmak
🤖 Doğru bir yanılgı ama atomiklikle değil sayım kesinliğiyle ilgilidir; `size()` yaklaşıktır.
## Doğru
C
## Açıklama
`ConcurrentHashMap` her BİR metodu atomik yapar; fakat İKİ ayrı metodu peş peşe çağırmak (kontrol-sonra-hareket: check-then-act) atomik DEĞİLDİR. `if (!map.containsKey(k)) map.put(k, v)` dizisinde iki thread aynı anda `containsKey`'i false görüp ikisi de `put` yapabilir — biri diğerini ezer. Doğru yol, işlemi tek atomik metoda indirgemektir: `putIfAbsent(k, v)`, `computeIfAbsent(k, fn)`, veya birikimli güncelleme için `merge(k, v, remap)`. Bu metodlar anahtar bazında atomik yürütülür. Diğer şıklar da gerçek yanılgılardır ama sorulan hata değildir: tek bir `put`'u ayrıca `synchronized`'a almak (A) ters yönde bir yanılgıdır — tek metod zaten atomiktir, dış kilit yalnızca ölçeklenmeyi öldürür. `Collections.synchronizedMap` ile sarmalamak (B) da bileşik işlemi kurtarmaz: her çağrı ayrı ayrı kilitlenir, aralarındaki boşluk yine açıktır. `size()` (D) ise eşzamanlı bir map'te yaklaşıktır — ama bu bir sayım kesinliği sorunudur, bileşik işlem atomikliği değil.

# cc-6 | Uzman
## Soru
`ConcurrentHashMap.compute` / `merge` metodlarına verilen yeniden-eşleme (remapping) fonksiyonu için hangi kural geçerlidir?
## A
Fonksiyon içinde uzun süren ağır hesaplar yapmak serbesttir; hiçbir kilit tutulmadığı için etkisi olmaz
🤖 Fonksiyon o düğümün kilidini TUTAR; uzun iş çekişmeyi ve gecikmeyi artırır.
## B
Fonksiyonun içinden aynı map'e başka anahtarlar yazmak, farklı kovaya düştüğü sürece güvenlidir
🤖 Kova farkı fark etmez: aynı map'e yeniden girmek tanımsız davranışa/kilitlenmeye yol açar.
## C
`compute` yalnız görünürlük (visibility) garantisi verir; atomiklik için dıştan `synchronized` gerekir
🤖 `compute`/`merge` anahtar bazında atomiktir; dış kilit gerekmez.
## D
Fonksiyon KISA, hızlı ve yan etkisiz olmalı; içinden AYNI map'i değiştirmeye çalışmak yasaktır
🤖 Doğru: fonksiyon kısa ve saf tutulmalı, map yeniden girilmemeli.
## Doğru
D
## Açıklama
`compute`, `merge`, `computeIfAbsent` verdiğin fonksiyonu ilgili anahtar üzerinde ATOMİK olarak (o düğümün kilidi altında) çalıştırır — bu atomikliğin bedeli, fonksiyon çalışırken o kova/düğümün kilitli tutulmasıdır. Bu yüzden fonksiyon KISA, HIZLI ve yan etkisiz olmalıdır (A yanlış). Java dokümanı açıkça uyarır: bu fonksiyonun İÇİNDEN AYNI map üzerinde başka güncelleme yapmaya çalışmak (yeniden giriş) yasaktır — kilitlenmeye veya tanımsız davranışa yol açabilir (B yanlış). Atomiklik zaten metodun kendisinde sağlandığı için dıştan `synchronized`'a da gerek yoktur (C yanlış).
