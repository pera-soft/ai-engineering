---
slug: iterator-fail-fast
baslik: "Iterator, fail-fast ve ConcurrentModificationException"
kademe: Temel
sira: 24
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kitabı okurken parmağını satırda tutarsın: nerede kaldığını parmağın bilir, kitap bilmez. `Iterator` (yineleyici) tam olarak bu parmaktır — koleksiyonun **içini bilmeden** elemanları tek tek gezmenin standart yolu. Sayesinde aynı `for-each` döngüsü `ArrayList`'te de, `HashSet`'te de, `HashMap`'in anahtarlarında da çalışır.

Peki okurken kitaptan sayfa koparırsan ne olur? Parmağın yanlış yeri gösterir. Koleksiyonu **gezerken değiştirmek** de aynı sorunu doğurur. Java bunu sessizce yanlış sonuç üretmek yerine erken bir hatayla haber vermeye çalışır; bu davranışın adı **fail-fast**'tir.

### Temel kavramlar

- **`Iterator` sözleşmesi (contract)** üç metottur. **`hasNext()`** dolaşılacak başka eleman kalıp kalmadığını `boolean` olarak söyler; imleci **hareket ettirmez**. **`next()`** bir sonraki elemanı döner **ve** imleci bir ileri taşır; eleman kalmamışsa `NoSuchElementException` fırlatır. **`remove()`** en son `next` ile dönen elemanı güvenle siler. Tipik döngü: `while (it.hasNext()) { X x = it.next(); ... }`.
- **İmleç (cursor)**: iteratörün "kaçıncı elemandayım" bilgisi.
- **Yapısal değişiklik (structural modification)**: koleksiyonun boyutunu değiştiren işlem (`add`, `remove`, `clear`). Var olan bir elemanın değerini değiştirmek yapısal değildir.
- **`modCount`**: koleksiyonun içinde tutulan **değişiklik sayacı**; her yapısal değişiklikte artar.
- **fail-fast (hızlı-başarısızlık)**: iteratör, kendi `expectedModCount`'u ile koleksiyonun `modCount`'u uyuşmuyorsa **`ConcurrentModificationException` (CME)** fırlatır.
- **fail-safe**: CME atmayan iteratör; bir **anlık görüntü** (snapshot) veya **zayıf tutarlı** (weakly consistent) görünüm üzerinde çalışır.

### Nasıl çalışır?

`for-each` döngüsü arka planda tam olarak bir `Iterator` kullanır — sihir yoktur. İteratör oluşturulurken koleksiyonun o anki `modCount`'unu `expectedModCount` olarak kopyalar; her `next()` bu ikisini karşılaştırır. Döngü gövdesinde `liste.remove(s)` çağırırsan koleksiyonun `modCount`'u artar ama iteratörünki artmaz; bir sonraki `next()` uyuşmazlığı görüp **CME** atar.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="fail-fast modCount kontrolü ve CME">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">fail-fast: modCount uyuşmazlığı</text>
  <rect x="40" y="46" width="250" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Iterator başlarken</text>
  <text x="165" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">expectedModCount = modCount</text>
  <text x="165" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her next() bu ikisini</text>
  <text x="165" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">karşılaştırır</text>
  <rect x="350" y="46" width="250" height="120" rx="12" fill="#FBECEC" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">list.add/remove olursa</text>
  <text x="475" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">modCount++ → uyuşmazlık</text>
  <text x="475" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sonraki next() →</text>
  <text x="475" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">ConcurrentModificationException</text>
  <text x="320" y="188" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Doğru silme: Iterator.remove() (veya removeIf) — sayaçları senkron tutar.</text>
</svg>
```

Sinsi kısım şudur: bu tespit **her zaman** çalışmaz. Üç elemanlı `[a, b, c]` listesinde `for-each` içinde `"a"`yı silersen liste 2 elemana iner, imleç 1'dedir, `hasNext()` hâlâ `true` döner ve `next()` istisnayı atar. Ama **sondan bir önceki** elemanı (`"b"`) silersen silme sonrası `cursor == size` olur, `hasNext()` **`false`** döner ve döngü **istisna atmadan sessizce** biter — kalan elemanlar hiç gezilmez. Yani "CME almadım" ifadesi "kodum doğru çalıştı" anlamına gelmez.

Doğrusu, silmeyi **iteratörün kendisi** üzerinden yapmaktır; `Iterator.remove()` hem koleksiyonu hem `expectedModCount`'u **birlikte** günceller:

```java
Iterator<String> it = liste.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("a")) it.remove();   // sayaçlar senkron kalır, CME yok
}
```

Java 8+ ile aynı işi tek satırda ve daha okunaklı yapan yol: `liste.removeIf(s -> s.equals("a"))`. Silinecekleri not edip sonra `clear()` çağırmak fazlasını siler; `liste = new ArrayList<>(liste)` gibi yeni bir liste atamak ise orijinali etkilemez ve iteratör hâlâ eski liste üzerindedir — ikisi de çözüm değildir.

### Mülakatta ne sorulur?

```svg
<svg viewBox="0 0 640 194" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="fail-fast ve fail-safe iteratör karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">fail-fast vs fail-safe iteratör</text>
  <rect x="24" y="42" width="290" height="138" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">fail-fast</text>
  <text x="169" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ArrayList · HashMap · HashSet</text>
  <text x="169" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">değişikliği modCount ile fark eder</text>
  <text x="169" y="130" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">→ ConcurrentModificationException</text>
  <text x="169" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">canlı görünüm · sessiz bozulmayı</text>
  <text x="169" y="168" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">erken hataya çevirir</text>
  <rect x="326" y="42" width="290" height="138" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">fail-safe</text>
  <text x="471" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">CopyOnWriteArrayList · ConcurrentHashMap</text>
  <text x="471" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">snapshot / zayıf tutarlı görünüm</text>
  <text x="471" y="130" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">→ CME atmaz</text>
  <text x="471" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">dolaşırken yapılan değişikliği</text>
  <text x="471" y="168" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yansıtmaz; yazımda maliyet</text>
</svg>
```

- **fail-fast vs fail-safe.** `ArrayList`/`HashMap`/`HashSet` iteratörleri **canlı** görünüm sunar ve değişikliği `modCount` ile fark edip CME atar. `CopyOnWriteArrayList`, iteratör oluşturulduğu andaki dizinin bir **anlık görüntüsü** üzerinde dolaşır: CME atmaz, ama dolaşırken yapılan değişiklikleri de **yansıtmaz**. Bedeli her yazımda tüm dizinin **kopyalanmasıdır** — yani daha hızlı ve daha az bellekli değil, aksine pahalıdır; okuma ağırlıklı, nadir yazımlı eşzamanlı senaryolar için uygundur. `ConcurrentHashMap` iteratörü de CME atmaz ama snapshot değil **zayıf tutarlı** bir görünüm verir.
- **"Garanti değil, en iyi çaba (best-effort)" ne demek?** fail-fast tespiti `modCount`'a dayanır ve her yasadışı değişikliği yakalamayı **garanti etmez**. Bazı durumlarda iteratör CME atmadan **sessizce yanlış** sonuç üretebilir; senkronizasyonsuz çok iş parçacıklı erişim ile yukarıdaki tek-thread silme deseni bunun tipik örnekleridir. Pratik sonuç: CME'yi bir **program mantığı aracı** olarak kullanma. CME bir **hata ayıklama yardımıdır**; yakalayıp yutmak değil, onu doğuran hatalı desen düzeltilir. CME **tek thread'de de** atılır (mesele thread sayısı değildir) ve tüm fail-fast koleksiyonlar için geçerlidir — `Collections.synchronizedList` gibi tek bir sarmalayıcıya özgü değildir.
- **`ListIterator`** (yalnız `List` uygulamalarında bulunur): sıradan `Iterator`'ın `hasNext`/`next`/`remove`'una ek olarak **geriye** dolaşma (`hasPrevious()`/`previous()`), konum sorgulama (`nextIndex()`/`previousIndex()`), en son dönen elemanı **değiştirme** (`set(x)`) ve imleç konumuna **ekleme** (`add(x)`) sunar. Bu işlemler iteratörün sayaçlarını da güncellediğinden CME oluşmadan güvenle yapılır. Sıralamayla veya eşzamanlılıkla ilgisi yoktur; hash tabanlı yapılar `listIterator()` sunmaz.

### Tek cümlede

`Iterator` koleksiyonu iç yapısından bağımsız gezmenin standart yoludur; gezerken koleksiyonu dışarıdan değiştirmek fail-fast iteratörlerde `ConcurrentModificationException` doğurur — ama bu tespit garanti değil en iyi çaba olduğundan, silmeyi daima `Iterator.remove()` veya `removeIf` ile yaparsın.

# itf-1 | Temel
## Soru
`Iterator` sözleşmesinde (contract) `hasNext()` ve `next()` metotlarının görevi nedir?
## A
`hasNext()` başka eleman var mı bilgisini (boolean) verir; `next()` sonrakini döner ve imleci ilerletir
🤖 Doğru: hasNext kontrol eder, next hem değeri verir hem ilerler.
## B
`hasNext()` bir sonraki elemanı döner; `next()` ise geriye kalan eleman sayısını verir
🤖 Karışmış: değeri `next` verir, `hasNext` yalnız var/yok bilgisini döner.
## C
İkisi de aynı elemanı döner, biri kopya biri orijinal
🤖 `hasNext` eleman döndürmez; boolean döner. Böyle bir kopya/orijinal ayrımı yoktur.
## D
`next()` imleci ilerletmez; imleci `hasNext()` ilerlettiği için aynı elemanı tekrar döner
🤖 `next()` her çağrıda imleci **ilerletir**; aksi halde döngü sonsuz olurdu.
## Doğru
A
## Açıklama
`Iterator` sözleşmesi: **`hasNext()`** koleksiyonda dolaşılacak başka eleman kalıp kalmadığını `boolean` olarak bildirir (imleci hareket ettirmez). **`next()`** bir sonraki elemanı döndürür **ve** imleci bir ileri taşır; eleman kalmamışsa `NoSuchElementException` fırlatır. Bu yüzden tipik döngü `while (it.hasNext()) { X x = it.next(); ... }` biçimindedir. `for-each` döngüsü de arka planda tam olarak bu iteratörü kullanır.

# itf-2 | Temel
## Soru
Aşağıdaki kod çalışınca ne olur?

```java
List<String> liste = new ArrayList<>(List.of("a", "b", "c"));
for (String s : liste) {
    if (s.equals("a")) liste.remove(s);
}
```
## A
Sorunsuz çalışır ve `"a"` silinir
🤖 `for-each` içinde koleksiyonu doğrudan değiştirmek fail-fast iteratörü tetikler; sorunsuz bitmez.
## B
`ConcurrentModificationException` fırlatır
🤖 Doğru: for-each'in gizli iteratörü modCount değişimini fark edip CME atar.
## C
Derleme hatası verir
🤖 Kod geçerli derlenir; sorun çalışma zamanında ortaya çıkar.
## D
Sonsuz döngüye girer
🤖 Sonsuz döngü değil; döngü bir sonraki `next()` çağrısında CME ile kesilir.
## Doğru
B
## Açıklama
`for-each` döngüsü arka planda bir **`Iterator`** kullanır. Döngü gövdesinde `liste.remove(s)` çağırmak koleksiyonun **`modCount`**'unu artırır ama iteratörün `expectedModCount`'u güncellenmez. Bir sonraki `next()` çağrısında iteratör bu uyuşmazlığı görüp **`ConcurrentModificationException`** (fail-fast) fırlatır: burada `"a"` silindikten sonra liste 2 elemana iner, imleç (cursor) 1'dedir, `hasNext()` hâlâ `true` döner ve `next()` istisnayı atar. Sinsi tuzak: aynı desen **sondan bir önceki** elemanı silerse — ör. üç elemanlı listede `"b"` — silme sonrası `cursor == size` olur, `hasNext()` `false` döner ve döngü **CME atmadan sessizce** biter; kalan elemanlar hiç gezilmez. Yani "istisna almadım" demek "doğru çalıştı" demek değildir. Doğrusu her iki durumda da `Iterator.remove()` veya `liste.removeIf(...)` kullanmaktır.

# itf-3 | Orta
## Soru
Bir listeden iterasyon sırasında bazı elemanları silmenin **doğru** yolu hangisidir?
## A
`for-each` içinde `list.remove(eleman)` çağırmak
🤖 Bu tam olarak CME'ye yol açan yanlış yoldur; koleksiyonu iteratör dışından değiştirir.
## B
`Iterator`'ı açıkça alıp `it.next()` sonrası `it.remove()` çağırmak (veya `list.removeIf(...)`)
🤖 Doğru: `Iterator.remove` sayaçları senkron tutar; removeIf ise en temiz tek satırlık yoldur.
## C
Silinecek elemanları not edip döngüden sonra `list.clear()` çağırmak
🤖 `clear()` tüm listeyi boşaltır; yalnız istenenleri değil her şeyi siler.
## D
Döngü içinde `list = new ArrayList<>(list)` ile listeyi kopyalamak
🤖 Yeni liste atamak orijinali değiştirmez ve iteratör hâlâ eski liste üzerindedir; sorunu çözmez.
## Doğru
B
## Açıklama
İterasyon sırasında güvenli silmenin yolu, silmeyi **iteratörün kendisi** üzerinden yapmaktır: `Iterator<X> it = list.iterator(); while (it.hasNext()) { X x = it.next(); if (kosul) it.remove(); }`. `Iterator.remove()`, hem koleksiyonu hem iteratörün `expectedModCount`'unu **birlikte** günceller, böylece CME oluşmaz. Java 8+'da aynı işi **`list.removeIf(x -> kosul)`** tek satırda ve daha okunaklı yapar. `for-each` içinde doğrudan `remove` (A) fail-fast tetikler; `clear()` (C) fazlasını siler; liste kopyalamak (D) orijinali etkilemez.

# itf-4 | Orta
## Soru
`ArrayList`'in fail-fast iteratörü ile `CopyOnWriteArrayList`'in fail-safe iteratörü arasındaki temel fark nedir?
## A
fail-fast iteratör thread-safe olduğu için CME atar; fail-safe iteratör thread-safe değildir
🤖 Tam ters çağrışım; fail-safe koleksiyonlar eşzamanlı okuma için tasarlanır, `ArrayList` değildir.
## B
fail-safe iteratör her zaman daha hızlıdır ve kopya tutmadığı için daha az bellek kullanır
🤖 Aksine; `CopyOnWriteArrayList` her yazımda diziyi kopyalar, bellek ve yazma maliyeti yüksektir.
## C
fail-fast yapısal değişikliği fark edip CME atar; fail-safe anlık görüntü (snapshot) üzerinde dolaştığından CME atmaz
🤖 Doğru: biri değişikliği hata sayar; diğeri snapshot üzerinde sessizce çalışır, ama güncel görmez.
## D
İkisi de yapısal değişiklikte CME atar; fark yalnızca isimlendirmedir
🤖 fail-safe koleksiyonlar tanımı gereği CME atmaz; fark gerçektir.
## Doğru
C
## Açıklama
**fail-fast** iteratörler (`ArrayList`, `HashMap` vb.) iterasyon sırasında koleksiyonun **yapısal olarak** değiştiğini `modCount` üzerinden fark eder ve **`ConcurrentModificationException`** atar — amaç sessiz bozulma yerine erken hata. **fail-safe** (daha doğrusu "zayıf tutarlı"/snapshot) iteratörler CME atmaz: `CopyOnWriteArrayList`, iteratör oluşturulduğu andaki dizinin bir **anlık görüntüsü** (snapshot) üzerinde dolaşır; dolaşırken yapılan değişiklikler bu iterasyona **yansımaz**. Bunun bedeli, her yazımda tüm dizinin **kopyalanmasıdır** — okuma ağırlıklı, nadir yazımlı eşzamanlı senaryolar için uygundur (B yanlış: ucuz değildir). `ConcurrentHashMap` iteratörü de CME atmaz ama snapshot değil, **zayıf tutarlı** bir görünüm sunar.

# itf-5 | Orta
## Soru
`ListIterator`'ın (yalnız `List` için) sıradan `Iterator`'a göre sağladığı ek yetenek nedir?
## A
Elemanları otomatik sıralı tutma
🤖 `ListIterator` sıralama yapmaz; yalnız dolaşma/düzenleme yeteneği ekler.
## B
Koleksiyonu paralel olarak birden çok thread'de dolaşabilme
🤖 `ListIterator` eşzamanlılık sağlamaz; tek kanallı, iki yönlü bir imleçtir.
## C
`HashMap` ve `HashSet` üzerinde de çalışabilme
🤖 `ListIterator` yalnız `List` uygulamalarında bulunur; hash tabanlı yapılar `listIterator()` sunmaz.
## D
İki yönlü dolaşma (`hasPrevious`/`previous`) ve iterasyon sırasında güvenli `set`/`add`
🤖 Doğru: ListIterator geri de gider ve mevcut elemanı değiştirip yeni eleman ekleyebilir.
## Doğru
D
## Açıklama
**`ListIterator`**, `List` arayüzüne özgü genişletilmiş bir iteratördür. Sıradan `Iterator`'ın `hasNext`/`next`/`remove`'una ek olarak: **geriye** dolaşma (`hasPrevious()`/`previous()`), konum sorgulama (`nextIndex()`/`previousIndex()`), en son dönen elemanı **değiştirme** (`set(x)`) ve imleç konumuna **yeni eleman ekleme** (`add(x)`) sunar. Bu `set`/`add` işlemleri iteratörün sayaçlarını da güncellediğinden iterasyon sırasında CME oluşmadan güvenle yapılır. Yalnız `List` uygulamalarında bulunur (C yanlış); eşzamanlılıkla (B) veya sıralamayla (A) ilgisi yoktur.

# itf-6 | İleri
## Soru
Bir dokümantasyonda "`ConcurrentModificationException` fail-fast davranışı **garanti değildir**, yalnız en iyi çaba (best-effort) tespittir" deniyor. Bunun pratik anlamı nedir?
## A
Yapısal değişiklik olsa bile iteratör bazı durumlarda CME atmadan sessizce yanlış sonuç üretebilir; CME'nin yokluğuna güvenilemez
🤖 Doğru: tespit modCount'a dayalı en iyi çabadır; her ihlali yakalayamaz, dolayısıyla CME'ye program mantığı olarak güvenilmez.
## B
CME hiçbir zaman gerçek bir sorunu işaret etmez, güvenle bastırılabilir (catch edilip yutulabilir)
🤖 Tam tersi; CME gerçek bir eşzamanlılık/değiştirme hatasının işaretidir, yutulmamalı.
## C
"Garanti değil" ifadesi CME'nin yalnız çok kanallı (multithread) kodda atıldığı anlamına gelir
🤖 CME tek thread'de de atılır (for-each içinde remove gibi); mesele thread sayısı değil.
## D
Bu ifade yalnızca `Collections.synchronizedList` için geçerlidir
🤖 fail-fast/best-effort tüm standart fail-fast koleksiyonlar için geçerlidir, tek bir sarmalayıcıya özgü değil.
## Doğru
A
## Açıklama
fail-fast tespiti `modCount` sayacına dayalı **en iyi çaba** (best-effort) bir mekanizmadır; her yasadışı değişikliği yakalamayı **garanti etmez**. Belirli durumlarda (özellikle çok kanallı senkronizasyonsuz erişimde, ya da bazı tek-thread silme desenlerinde) iteratör CME atmadan **sessizce yanlış** sonuç üretebilir. Pratik sonuç: **CME'yi bir program mantığı aracı olarak kullanma** — "CME atmadıysa değişiklik güvenliydi" diye düşünmek yanlıştır. CME bir **hata ayıklama yardımıdır** (bir hatanın erken işareti); onu yakalayıp yutmak (B) değil, onu doğuran hatalı değiştirme desenini düzeltmek gerekir. CME tek thread'de de atılabilir (C yanlış) ve tüm fail-fast koleksiyonlar için geçerlidir (D yanlış).
