---
slug: completablefuture
baslik: "CompletableFuture: Asenkron Orkestrasyon"
kademe: İleri
sira: 50
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoranda garson siparişi mutfağa iletir ve tepsinin başında bekleyip durmaz; başka masalara bakar, yemek hazır olunca gelip alır. `CompletableFuture` programda tam olarak bu garsonu tarif eder: uzun süren bir işi başlatır, sonucu beklemek yerine "sonuç gelince şunu yap" talimatını bırakır. Çözdüğü problem israftır — bir akış hattının ağdan cevap gelmesini boş boş beklemesi, o hattın o süre boyunca hiçbir iş yapmaması demektir. Talimat bırakma yöntemiyle aynı sürede çok daha fazla iş biter.

### Temel kavramlar

- **İş parçacığı (thread)**: Programın aynı anda ilerleyen ayrı akış hatlarından biri; restorandaki bir garson.
- **Bloklama (blocking)**: Bir thread'in sonuç gelene kadar durup hiçbir şey yapamaması; garsonun mutfak kapısında dikilmesi.
- **Asenkron (asynchronous, eşzamansız)**: Sonucu beklemeden devam etmek; sonuç hazır olduğunda önceden bırakılan talimatın kendiliğinden çalışması.
- **Aşama (stage)**: Zincirdeki her adım. `CompletableFuture<T>`, "gelecekte T tipinde bir sonuç" vaadidir.
- **Yürütücü (executor) / havuz (pool)**: Görevleri çalıştırmaya hazır bekleyen thread topluluğu.

Klasik `Future` sonucu yalnız `get()` ile BLOKLAYARAK verir. `CompletableFuture` ise sonucun üzerine bloklamadan işlem zincirleri kurmanı sağlar (deklaratif, olay-tabanlı). Üç dönüşüm sürekli karıştırılır:

```java
CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 21);

f.thenApply(x -> x * 2);                 // düz değer döndürür → CF<Integer> (42)
f.thenCompose(x -> kullaniciGetir(x));   // fn'in kendisi CF döndürür → düzleştirir
f.thenCombine(digerF, (a, b) -> a + b);  // BAĞIMSIZ iki iş, ikisi bitince birleşir
```

- **`thenApply(fn)`**: sonucu SENKRON bir fonksiyonla dönüştürür; `fn` düz bir değer döndürür → `CompletableFuture<U>`.
- **`thenCompose(fn)`**: `fn`'in kendisi bir `CompletableFuture` döndürdüğünde kullanılır; iç içe geçmeyi (`CF<CF<T>>`) düzleştirir. (Akış: birbirine BAĞLI iki asenkron adım.)
- **`thenCombine(other, fn)`**: BAĞIMSIZ iki `CompletableFuture`'ı, ikisi de bitince `(T, U) → R` ile birleştirir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="thenApply, thenCompose ve thenCombine karşılaştırması">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç dönüşüm, üç senaryo</text>
  <rect x="24" y="46" width="190" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="119" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">thenApply</text>
  <text x="119" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">senkron dönüşüm</text>
  <text x="119" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">fn: T → U</text>
  <text x="119" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düz değer döndürür</text>
  <rect x="225" y="46" width="190" height="140" rx="12" fill="#E6F0F8" stroke="#1C5A8A" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#1C5A8A" font-family="system-ui">thenCompose</text>
  <text x="320" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">bağlı asenkron adım</text>
  <text x="320" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">fn: T → CF&lt;U&gt;</text>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iç içeyi düzleştirir</text>
  <rect x="426" y="46" width="190" height="140" rx="12" fill="#FBF3E6" stroke="#B4772B" stroke-width="1.5"/>
  <text x="521" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#B4772B" font-family="system-ui">thenCombine</text>
  <text x="521" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">bağımsız iki iş</text>
  <text x="521" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">(T, U) → R</text>
  <text x="521" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ikisi bitince birleştir</text>
  <text x="320" y="208" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Zincir ortasında .join()/.get() = paralelliği öldüren tuzak.</text>
</svg>
```

### Nasıl çalışır?

`thenApply`, verdiğin fonksiyonun döndürdüğü değeri OLDUĞU GİBİ sarar. Fonksiyon zaten bir `CompletableFuture<U>` döndürüyorsa elinde iç içe `CompletableFuture<CompletableFuture<U>>` kalır; JVM bunu kendiliğinden düzleştirmez. `thenCompose` tam olarak bunu, `Stream.flatMap` gibi düzleştirir — "önce kullanıcıyı çek, SONRA onun siparişlerini çek" gibi bağımlı adımların doğru birleştiricisidir. Bağımsız işleri `thenCompose` ile zincirlemek ise onları gereksiz yere SIRAYA sokar; orada `thenCombine` (ya da `allOf`) gerekir.

Peki dönüşüm hangi thread'de çalışır? `thenApply` gibi async-olmayan varyantlar işi genelde önceki aşamayı TAMAMLAYAN thread'de yürütür (aşama zaten bitmişse çağıran thread'de) ve yeni thread AÇMAZ; bloklamaz da — bloklama `get()`/`join()` işidir. `thenApplyAsync` ise görevi bir `Executor`'a devreder: executor verirsen onu, vermezsen varsayılan `ForkJoinPool.commonPool()` kullanılır. Fark önemlidir: async-olmayan varyantta ağır veya bloklayan bir dönüşüm, sonucu üreten thread'i (ör. bir I/O geri-çağırma thread'ini) meşgul edip zincirin ilerisini yavaşlatır.

`commonPool` JVM genelinde PAYLAŞILIR ve boyutu genelde `çekirdek sayısı - 1` kadardır; CPU-yoğun ve KISA görevler için tasarlanmıştır, ihtiyaç oldukça sınırsız işçi AÇMAZ. İçine ağ/DB gibi BLOKLAYAN iş koyarsan birkaç görev tüm işçileri tutar ve parallel stream'ler dâhil uygulamanın bütün asenkron işleri aç kalır (thread starvation). Bu bir test kolaylığı değil, üretim gereğidir: bloklayan işlere iş yüküne göre boyutlandırılmış **özel bir executor** verip `commonPool`'u temiz tutmak şarttır.

Hata yolunda üç kanca (hook) vardır. `exceptionally` YALNIZ hata olduğunda devreye girer; alternatif bir değer üretip akışı kurtarır, başarıda hiç çağrılmaz. `handle` HER İKİ durumda çalışır: `(sonuç, throwable)` çiftini alır (biri null olur) ve YENİ bir değer döndürür. `handle` yakaladığı istisnayı kendiliğinden yeniden fırlatmaz. `whenComplete` de her iki durumda çalışır ama gözlem ve yan etki içindir; sonucu DEĞİŞTİRMEZ, zincir aynı sonuçla (ya da aynı hatayla) devam eder.

```svg
<svg viewBox="0 0 640 224" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="exceptionally, handle ve whenComplete ne zaman çalışır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hata mı, başarı mı? Hangi kanca ne zaman çalışır</text>
  <rect x="24" y="46" width="190" height="150" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="119" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#B26B00" font-family="system-ui">exceptionally</text>
  <text x="119" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">YALNIZ hata olunca</text>
  <text x="119" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">girdi: throwable</text>
  <text x="119" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çıktı: alternatif değer</text>
  <text x="119" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">başarıda atlanır</text>
  <rect x="225" y="46" width="190" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">handle</text>
  <text x="320" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">HER İKİ durumda</text>
  <text x="320" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">girdi: (sonuç, throwable)</text>
  <text x="320" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çıktı: yeni değer</text>
  <text x="320" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">biri null olur</text>
  <rect x="426" y="46" width="190" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="521" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">whenComplete</text>
  <text x="521" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">HER İKİ durumda</text>
  <text x="521" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">girdi: (sonuç, throwable)</text>
  <text x="521" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">çıktı: YOK (yan etki)</text>
  <text x="521" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonucu değiştirmez</text>
  <text x="320" y="214" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Kurtar → exceptionally · dönüştür → handle · gözle → whenComplete</text>
</svg>
```

Sonucu okumanın iki yolu vardır ve istisna davranışları farklıdır: `get()`, kontrol edilen (checked) `InterruptedException` ve `ExecutionException` fırlatır — bu yüzden `try/catch` zorunludur. `join()` ise kontrol edilmeyen (unchecked) `CompletionException` fırlatır; bu nedenle lambda ve zincir içinde `join()` çok daha kullanışlıdır. İkisi de asıl istisnayı SARMALAR (wrap); gerçek sebebe `getCause()` ile ulaşılır.

Bağımsız işleri paralel başlatıp toplamanın anahtarı `allOf`'tur: üçünü de başlat, hepsinin bitmesini bekle, sonra sonuçları oku. Dikkat: `allOf(...)` bir `CompletableFuture<Void>` döndürür — sonuçları TAŞIMAZ, yalnız "hepsi bitti" sinyali verir; değerleri, `allOf` tamamlandıktan SONRA her future'ın `join()`'ini okuyarak (artık bloklamadan) toplarsın. Kardeşi `anyOf` ise ilk tamamlananın sonucunu `CompletableFuture<Object>` olarak verir. Zincirin ORTASINDA `join()`/`get()` çağırmak ise işleri sinsice SERİ hâle getirir — toplam süre "en yavaş iş" olmaktan çıkıp "işlerin toplamı" olur:

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="allOf ile paralel toplama; ortada join anti-pattern">
  <defs>
    <marker id="cf-ok" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><polygon points="0,0 7,3 0,6" fill="#25804C"/></marker>
    <marker id="cf-bad" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><polygon points="0,0 7,3 0,6" fill="#8A2E2E"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Bağımsız 3 iş: paralel topla, ortada join'leme</text>
  <rect x="20" y="40" width="290" height="184" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">DOĞRU — allOf ile topla</text>
  <rect x="38" y="76" width="80" height="26" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="78" y="93" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">f1</text>
  <rect x="38" y="110" width="80" height="26" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="78" y="127" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">f2</text>
  <rect x="38" y="144" width="80" height="26" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="78" y="161" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">f3</text>
  <path d="M120 89 L172 116" stroke="#25804C" stroke-width="1.6" fill="none" marker-end="url(#cf-ok)"/>
  <path d="M120 123 L172 123" stroke="#25804C" stroke-width="1.6" fill="none" marker-end="url(#cf-ok)"/>
  <path d="M120 157 L172 130" stroke="#25804C" stroke-width="1.6" fill="none" marker-end="url(#cf-ok)"/>
  <rect x="176" y="104" width="76" height="38" rx="8" fill="#25804C" stroke="#25804C" stroke-width="1"/>
  <text x="214" y="128" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">allOf</text>
  <text x="165" y="196" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üçü PARALEL akar, bitince topla</text>
  <text x="165" y="213" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">süre ≈ en yavaş iş</text>
  <rect x="330" y="40" width="290" height="184" rx="12" fill="#F7EDED" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="475" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">YANLIŞ — ortada join()</text>
  <rect x="348" y="86" width="78" height="30" rx="6" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1"/>
  <text x="387" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">f1.join()</text>
  <rect x="436" y="86" width="78" height="30" rx="6" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1"/>
  <text x="475" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">f2.join()</text>
  <rect x="524" y="86" width="78" height="30" rx="6" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1"/>
  <text x="563" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">f3.join()</text>
  <path d="M426 101 L436 101" stroke="#8A2E2E" stroke-width="1.6" fill="none" marker-end="url(#cf-bad)"/>
  <path d="M514 101 L524 101" stroke="#8A2E2E" stroke-width="1.6" fill="none" marker-end="url(#cf-bad)"/>
  <text x="475" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her join bir sonrakini bekletir</text>
  <text x="475" y="170" text-anchor="middle" font-size="11" font-weight="600" fill="#8A2E2E" font-family="system-ui">SERİ: süre ≈ f1+f2+f3</text>
  <text x="475" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">paralellik ölür</text>
</svg>
```

### Mülakatta ne sorulur?

- **`thenApply` / `thenCompose` / `thenCombine`**: senkron dönüşüm · bağımlı asenkron adımı düzleştirme (flatMap benzeri) · BAĞIMSIZ iki işi birleştirme. `thenApply` otomatik düzleştirme yapmaz; "`thenCompose` yalnız senkron fonksiyonlarla kullanılır" cümlesi terstir.
- **`...Async` havuzu**: executor verilmezse `commonPool`. Bu havuz sınırlıdır (~çekirdek-1), sınırsız büyümez, üretimde de etkindir; bloklayan iş orada starvation üretir. Çözüm: ayrı, doğru boyutlu executor.
- **`join()` vs `get()`**: `join` unchecked `CompletionException`; `get` checked `ExecutionException` + `InterruptedException` (try/catch zorunlu). İkisi de asıl hatayı SARMALAR → `getCause()`.
- **`exceptionally` / `handle` / `whenComplete`**: yalnız hata + alternatif değer · her iki durum + yeni değer · her iki durum ama sonucu değiştirmeyen gözlem. `handle` istisnayı kendiliğinden yeniden fırlatmaz.
- **`allOf` `Void` döndürür**: sonuçları taşımaz, değerleri tek tek okumak sana kalır. `anyOf` ilk bitenin sonucunu `Object` olarak verir.
- **Anti-pattern**: `f1.join()` deyip sonra f2'yi başlatmak; senkron döngüde tek tek çağırmak. İkisi de paralelliği öldürür.

### Tek cümlede

`CompletableFuture`, sonucu beklemek yerine "gelince şunu yap" diyerek bağımsız işleri paralel başlatıp bloklamadan birleştirmeni sağlar; iki büyük tuzağı zincirin ortasındaki `join()` ve varsayılan `commonPool`'a konan bloklayan iştir.

# cf-1 | Temel
## Soru
`thenApply(fn)` ne işe yarar?
## A
İki BAĞIMSIZ `CompletableFuture`'ı, ikisi de tamamlanınca tek bir sonuçta birleştirir
🤖 O `thenCombine`; `thenApply` tek bir aşamanın sonucunu dönüştürür.
## B
Bir aşamanın sonucunu SENKRON bir fonksiyonla dönüştürüp `CompletableFuture<U>` döndürür
🤖 Doğru: `thenApply` sonucu düz bir değere map'ler.
## C
Çağıran thread'i bloklayıp aşamanın tamamlanmış sonucunu doğrudan geri döndürür
🤖 Bloklama `get()`/`join()` işidir; `thenApply` bloklamaz, zincire yeni aşama ekler.
## D
Zincirde bir istisna oluştuğunda devreye girip akışı kurtaran alternatif değeri üretir
🤖 O `exceptionally`; `thenApply` yalnız başarı yolunda çalışır.
## Doğru
B
## Açıklama
`thenApply`, tamamlanmış bir aşamanın sonucunu SENKRON bir fonksiyonla (`Function<T,U>`) dönüştürür ve sonucu yeni bir `CompletableFuture<U>` olarak sarar. Fonksiyon düz bir değer döndürür — asenkron bir `CompletableFuture` değil. Bloklamaz (C yanlış), iki future birleştirmez (A → `thenCombine`) ve hata yolunu ele almaz (D → `exceptionally`/`handle`). Kısaca: "sonucu al, dönüştür, akışta ilerle."

# cf-2 | Orta
## Soru
Çağırdığın fonksiyonun KENDİSİ bir `CompletableFuture` döndürüyorsa (ör. ilk çağrının sonucuyla ikinci bir asenkron çağrı yapacaksın), hangisini kullanmalısın?
## A
`thenCompose` — iç içe `CompletableFuture<CompletableFuture<T>>` yapısını düzleştirir
🤖 Doğru: bağlı asenkron adımlar için `thenCompose` (flatMap benzeri).
## B
`thenApply` — döndürülen future'ı JVM otomatik düzleştirdiği için sonuç aynı olur
🤖 Otomatik düzleştirme yoktur: `thenApply` iç içe `CF<CF<U>>` üretir.
## C
`thenApply` — çünkü `thenCompose` yalnızca senkron fonksiyonlarla birlikte kullanılabilir
🤖 Tam tersi: `thenCompose` zaten asenkron (future döndüren) adım içindir.
## D
`thenCombine` — ortada iki ayrı future olduğundan ikisinin sonucunu birleştirmek gerekir
🤖 `thenCombine` BAĞIMSIZ iki iş içindir; burada ikinci iş birincinin sonucuna bağlı.
## Doğru
A
## Açıklama
`thenApply` verdiğin fonksiyonun döndürdüğü değeri OLDUĞU GİBİ sarar. Fonksiyon zaten bir `CompletableFuture<U>` döndürürse, elinde iç içe `CompletableFuture<CompletableFuture<U>>` kalır. `thenCompose`, tıpkı `Stream.flatMap` gibi bu iç içe yapıyı **düzleştirir** ve sana temiz bir `CompletableFuture<U>` verir. Bu, "önce kullanıcıyı çek, SONRA kullanıcının siparişlerini çek" gibi birbirine BAĞLI asenkron adımların doğru birleştirme yoludur. Bağımsız işler için `thenCombine` (D) uygundur.

# cf-3 | Orta
## Soru
`thenApply` ile `thenApplyAsync` arasındaki fark hangi ifadeyle en doğru anlatılır?
## A
`thenApply`, her çağrıda işletim sisteminden yeni bir thread açıp dönüşümü orada çalıştırır
🤖 Tersine; `thenApply` mevcut bir thread'de yürür, yeni thread açmaz.
## B
`thenApplyAsync`, verilen executor'ı yok sayıp görevi her zaman çağıran thread'de çalıştırır
🤖 Tam tersi; `...Async` görevi executor'a devreder, çağıran thread'i kullanmaz.
## C
`thenApply` önceki aşamayı tamamlayan thread'de çalışır; `thenApplyAsync` işi executor'a devreder
🤖 Doğru: async varyant işi ayrı bir havuza taşır.
## D
İkisi de her koşulda `commonPool` işçilerinde çalışır; aradaki fark yalnızca isimlendirmedir
🤖 `thenApply` ayrı havuz garantisi vermez; genelde tamamlayan thread'de yürür.
## Doğru
C
## Açıklama
`thenApply` gibi async-olmayan varyantlar, dönüşümü genellikle önceki aşamayı TAMAMLAYAN thread'de (aşama zaten bitmişse çağıran thread'de) çalıştırır — ayrı bir thread'e sıçrama yoktur. `thenApplyAsync` ise görevi bir `Executor`'a devreder: executor verirsen onu, vermezsen varsayılan `ForkJoinPool.commonPool()` kullanılır. Bu ayrım önemlidir çünkü async-olmayan varyantta ağır/bloklayan bir dönüşüm, sonucu üreten thread'i (ör. bir I/O geri-çağırma thread'i) meşgul edip zincirin ilerisini yavaşlatabilir.

# cf-4 | İleri
## Soru
`exceptionally` ile `handle` arasındaki fark nedir?
## A
`exceptionally`, başarı olsun hata olsun her tamamlanmada çalışıp sonucu yeniden yazar
🤖 Hayır; `exceptionally` YALNIZ istisna olduğunda tetiklenir, başarıda atlanır.
## B
`handle` yalnızca başarı durumunda çalışır; istisna varsa tamamen atlanıp zincir kırılır
🤖 Hayır; `handle` hem başarıda hem hatada çalışır.
## C
`handle`, yakaladığı istisnayı işledikten sonra otomatik olarak yeniden fırlatır (rethrow) ve zinciri kırar
🤖 `handle` istisnayı yakalar ve bir sonuç üretmene izin verir; kendiliğinden fırlatmaz.
## D
`exceptionally` yalnız hata yolunda çalışır; `handle` her iki durumda `(sonuç, throwable)` alır
🤖 Doğru: `exceptionally` sadece hata yolu, `handle` her iki yol.
## Doğru
D
## Açıklama
`exceptionally(fn)`, zincirde bir istisna oluştuğunda devreye girer ve `fn` alternatif bir değer üreterek akışı KURTARIR; başarı durumunda hiç çağrılmaz. `handle(bifn)` ise HER İKİ durumda da çalışır: parametre olarak `(sonuç, throwable)` alır — biri null'dır — ve tek noktada hem başarıyı hem hatayı işleyip bir değer döndürür. Yalnız hatayı yakalamak istiyorsan `exceptionally`; başarı/başarısızlık ayrımına göre tek yerde karar verecek ve dönüşüm yapacaksan `handle` (veya yan etki için `whenComplete`) uygundur.

# cf-5 | İleri
## Soru
Bloklayan (I/O, ağ, DB) işler içeren asenkron zincirlerde neden `...Async` varyantlarına ÖZEL bir executor vermek gerekir?
## A
Özel executor her koşulda fazladan bağlam değiştirme (context switch) maliyeti getirip performansı düşürür
🤖 Tersine; doğru boyutlu özel havuz, bloklayan işte performansı korur.
## B
Executor verilmezse görevler paylaşılan `ForkJoinPool.commonPool`'a düşer; bloklayan iş bu sınırlı havuzu doyurur
🤖 Doğru: bloklayan iş için ayrı, uygun boyutlu havuz şarttır.
## C
`commonPool` ihtiyaç oldukça sınırsız yeni işçi açtığından bloklayan iş hiçbir sorun çıkarmaz
🤖 `commonPool` sınırlıdır (~çekirdek-1); sınırsız büyümez.
## D
Özel executor yalnız birim testlerinde determinizm sağlamak için gerekir; üretimde `commonPool` yeterlidir
🤖 Asıl risk üretimdedir; bloklayan iş canlıda havuzu kilitler.
## Doğru
B
## Açıklama
Executor verilmeyen `...Async` çağrıları varsayılan `ForkJoinPool.commonPool()`'a düşer. Bu havuz JVM genelinde PAYLAŞILIR ve boyutu genelde `çekirdek sayısı - 1` kadardır. `commonPool`, CPU-yoğun ve KISA görevler için tasarlanmıştır; içine ağ/DB gibi BLOKLAYAN işler koyarsan, birkaç görev tüm işçileri tutup bloke ederek parallel stream'ler dahil tüm uygulamanın asenkron işlerini aç bırakır (thread starvation). Çözüm: bloklayan işlere, iş yüküne göre boyutlandırılmış AYRI bir `Executor` verip `commonPool`'u temiz tutmaktır.

# cf-6 | Uzman
## Soru
Birbirinden BAĞIMSIZ üç servis çağrısını paralel başlatıp sonuçlarını birleştirmek istiyorsun. En verimli yaklaşım hangisidir?
## A
Üçünü ayrı `CompletableFuture` olarak başlatıp `allOf(f1,f2,f3)` ile toplamak
🤖 Doğru: hepsi paralel akar, birleştirme tümü bitince yapılır.
## B
Her future'ı başlattıktan hemen sonra `get()` çağırıp sonucu alıp bir sonraki çağrıya geçmek
🤖 `get()` bloklar; her adımda beklemek paralelliği yok eder.
## C
Üç çağrıyı `thenCompose` ile zincirleyip her biri bitince bir sonrakini tetiklemek
🤖 `thenCompose` adımları SIRALI çalıştırır; bağımsız işleri gereksiz yere seri yapar.
## D
Üçünü tek bir thread içinde bir döngüde senkron çağırıp sonuçları listeye toplamak
🤖 Senkron döngü paralellik sağlamaz; toplam süre üç çağrının toplamı olur.
## Doğru
A
## Açıklama
Bağımsız işlerde amaç, üçünü de AYNI ANDA başlatıp toplam süreyi en yavaş çağrıya indirmektir. Üç `CompletableFuture`'ı başlatıp `CompletableFuture.allOf(...)` ile hepsinin tamamlanmasını bekler, sonra `join()`'leri (artık hepsi bittiği için bloklamadan) okuyup birleştirirsin. Dikkat: `allOf` bir `CompletableFuture<Void>` döndürür — sonuçları taşımaz, yalnız "hepsi bitti" sinyali verir; değerleri tek tek okumak sana kalır. Kritik anti-pattern: zincirin ORTASINDA `f1.join()` deyip sonra f2'yi başlatmak — bu, işleri istemeden SERİ hale getirir ve paralelliği öldürür (B, D). `thenCompose` (C) ise bağımlı adımlar içindir; bağımsız işleri zincirlemek onları gereksizce sıralı yapar. Doğru desen: önce hepsini başlat, sonra `allOf` ile topla.
