---
slug: executor-future
baslik: "Executor ve Future: Thread Havuzları, Shutdown, Reddedilme"
kademe: İleri
sira: 49
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoranda her gelen müşteri için yeni bir garson işe alıp akşam işten çıkarmazsın: sabit bir garson kadrosu ve bir sipariş kuyruğu tutarsın. Siparişler kadroya dağıtılır; kadro dolduğunda kuyruk uzar, kuyruk da dolarsa "şu an alamıyoruz" dersin. Java'da her görev için `new Thread()` açmak, her müşteriye yeni garson almaya benzer: pahalıdır ve sınırsızdır. `java.util.concurrent` bunu **görev ile yürütücüyü ayırarak** çözer — sen ne yapılacağını (görev) verirsin, `ExecutorService` bunu kimin yapacağına (havuzdaki iş parçacıkları) karar verir.

### Temel kavramlar

- **İş parçacığı (thread)**: program içinde bağımsız ilerleyen yürütme hattı. Oluşturmak pahalıdır: yığın belleği ayrılır, işletim sistemi kaydı yapılır.
- **Görev (task)**: yapılacak iş. `Runnable` (değer döndürmez) ya da `Callable<V>` (değer döndürür, istisna fırlatabilir) olarak yazılır. Görev, onu çalıştıracak iş parçacığından bağımsızdır.
- **İş parçacığı havuzu (thread pool)**: önceden açılmış, görev bitince yok olmayıp sıradakini alan iş parçacıkları kümesi. `corePoolSize` (çekirdek boyut) ve `maximumPoolSize` (üst sınır) ile tanımlanır.
- **Kuyruk (queue)**: havuzdaki iş parçacıkları meşgulken görevlerin beklediği sıra. **Sınırlı** (bounded) ya da **sınırsız** (unbounded) olabilir.
- **`ExecutorService`**: havuzu yöneten arayüz; `execute`, `submit`, `invokeAll`, `shutdown` metotları buradadır.
- **`Future<V>`**: "ileride dolacak sonuç kutusu". `get()` sonucu ister, `isDone()` durumu sorar, `cancel(boolean)` iptal ister.
- **Bloklama (blocking)**: `get()` gibi bir çağrının, sonuç hazır olana kadar çağıran iş parçacığını bekletmesi.
- **Reddedilme (rejection)** ve **geri-basınç (backpressure)**: havuz ve kuyruk dolduğunda yeni görevin geri çevrilmesi; geri-basınç, üretenin hızını tüketenin hızına indirme fikridir.
- **Kesme (interrupt)**: iş parçacığına "işini bırak" isteği göndermek — zorla durdurmak değil.
- **Kullanıcı (non-daemon) iş parçacığı**: JVM, kullanıcı iş parçacıkları bitene kadar kapanmaz. Havuz iş parçacıkları varsayılanda böyledir.

### Nasıl çalışır?

Hazır havuz tipleri `Executors` sınıfındadır. `newFixedThreadPool(n)` sabit n iş parçacığı ve **sınırsız kuyruk** verir. `newCachedThreadPool()` gerektikçe iş parçacığı açar; 60 saniye boşta kalanı geri toplar. `newSingleThreadExecutor()` görevleri tek iş parçacığında sırayla çalıştırır. `newScheduledThreadPool(n)` ise gecikmeli ve periyodik görevler içindir. Üretimde en kontrollü yol, `ThreadPoolExecutor`'ı doğrudan **açık üst sınır ve sınırlı kuyrukla** kurmaktır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Görevler kuyruğa girer, havuzdaki iş parçacıkları işler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">submit → kuyruk → havuz iş parçacıkları</text>
  <rect x="30" y="70" width="120" height="80" rx="10" fill="#EEF3FA" stroke="#2C5B8A" stroke-width="1.5"/>
  <text x="90" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#2C5B8A" font-family="system-ui">Görevler</text>
  <text x="90" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">submit()</text>
  <rect x="200" y="70" width="150" height="80" rx="10" fill="#FFF6E6" stroke="#B5851A" stroke-width="1.5"/>
  <text x="275" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#B5851A" font-family="system-ui">Kuyruk (sınırlı)</text>
  <text x="275" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dolarsa → reddedilme</text>
  <rect x="400" y="56" width="210" height="36" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="505" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">iş parçacığı 1</text>
  <rect x="400" y="98" width="210" height="36" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="505" y="122" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">iş parçacığı 2 (yeniden kullanılır)</text>
  <path d="M150 110 L198 110" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exec-ok)"/>
  <path d="M350 100 L398 84" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exec-ok)"/>
  <path d="M350 118 L398 116" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exec-ok)"/>
  <defs>
    <marker id="exec-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="188" text-anchor="middle" font-size="12" font-weight="600" fill="#B4322B" font-family="system-ui">newCachedThreadPool: sınırsız iş parçacığı → yük altında tehlike.</text>
</svg>
```

Görev göndermenin iki yolu vardır ve aralarındaki fark mülakatın favorisidir. `execute(Runnable)` eski ve yalın `Executor` arayüzünden gelir, `void` döner ve yalnız `Runnable` alır. `submit(...)` hem `Runnable` hem `Callable` alır ve bir `Future` döndürür. `invokeAll(görevler)` ise bir görev listesini gönderip **hepsi bitene kadar** bekler, `List<Future>` döner. İkisi de aynı kuyruk/havuz yolunu kullanır; hemen çalıştırma ya da gecikmeli çalıştırma farkı yoktur (gecikme `ScheduledExecutorService`'in işidir).

Kritik ayrıntı istisnalarda saklıdır: `submit` ile gönderilen görev istisna fırlatırsa konsola hiçbir şey basılmaz — istisna **yutulur** ve `Future`'a sarılır; ancak `Future.get()` çağrıldığında `ExecutionException` olarak ortaya çıkar. `execute` ile gönderilende ise istisna iş parçacığının `UncaughtExceptionHandler`'ına gider ve kolayca gözden kaçar. Sonuç ya da hata izlemesi gerekiyorsa `submit` + `Future`; sadece "ateşle ve unut" bir yan etki içinse `execute` yeterlidir.

Havuz kendi başına doğruluk sağlamaz: görevler paralel çalıştığı için paylaşılan durumu korumak yine senin işindir, `synchronized` ihtiyacı ortadan kalkmaz. Havuzun verdiği şey **kaynak yönetimidir**: yeniden kullanım (oluşturma/yıkma maliyetini amortize eder) ve sınırlama (aynı anda kaç görevin çalışacağını havuz boyutu belirler).

Kapatma da açık bir sorumluluktur. Doğru desen üç adımlıdır. (1) `shutdown()` yeni görev kabulünü durdurur ama kuyruktaki ve çalışan görevlerin bitmesine izin verir; bloklamaz, hemen döner. (2) `awaitTermination(süre, birim)` tümü bitene ya da süre dolana kadar bloklayarak bekler. (3) Süre yetmezse `shutdownNow()` çalışanları `interrupt` ile kesmeye çalışır ve henüz başlamamış görevleri liste olarak döndürür.

```java
pool.shutdown();
if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
    pool.shutdownNow();   // zaman aşımı: kalanları kesmeye çalış
}
```

Doğrudan `shutdownNow()` çağırmak işi yarıda keser ve veri tutarsızlığı üretebilir; hiç kapatmamak ise havuz iş parçacıkları kullanıcı (non-daemon) iş parçacığı olduğundan JVM'in kapanmasını bile engeller.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zarif kapatma üç adımı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Zarif kapatma: iste → bekle → gerekirse zorla</text>
  <rect x="24" y="70" width="180" height="70" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="114" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1. shutdown()</text>
  <text x="114" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeni görevi durdur</text>
  <rect x="230" y="70" width="180" height="70" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">2. awaitTermination</text>
  <text x="320" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bitişi süreyle bekle</text>
  <rect x="436" y="70" width="180" height="70" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="526" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">3. shutdownNow()</text>
  <text x="526" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kalanları kesmeye çalış</text>
  <path d="M204 105 L228 105" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exec2-ok)"/>
  <path d="M410 105 L434 105" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exec2-ok)"/>
  <defs>
    <marker id="exec2-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="176" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Sadece shutdownNow işi yarıda keser; hiç kapatmama JVM'i asar.</text>
</svg>
```

### Mülakatta ne sorulur?

- **Doluluk anında ne olur?** Sınırlı bir `ThreadPoolExecutor`'da hem iş parçacıkları meşgul hem kuyruk doluysa görev reddedilir; bunu `RejectedExecutionHandler` yönetir. `AbortPolicy` (varsayılan) `RejectedExecutionException` fırlatır, yani görev sessizce kaybolmaz. `CallerRunsPolicy` görevi gönderen iş parçacığında senkron çalıştırır. Böylece gönderen yavaşlar ve sisteme doğal **geri-basınç** (backpressure) uygulanır; genelde en sağlıklı seçenek budur. `DiscardPolicy` görevi sessizce atar. `DiscardOldestPolicy` ise kuyruktaki en eskiyi atıp yenisini ekler. JVM havuzu kendiliğinden sınırsız büyütmez; `maximumPoolSize` bağlayıcıdır.
- **`newCachedThreadPool()`'un tehlikesi**: üst sınırı `Integer.MAX_VALUE`'dur ve doğrudan devir (`SynchronousQueue`) kullanır — görev kuyrukta beklemez, boşta iş parçacığı yoksa YENİSİ açılır. Görevler tüketilebildiğinden hızlı geldiğinde (trafik zirvesi, yavaşlayan bağımlı servis) on binlerce iş parçacığı açılır; bellek ve bağlam değişimi patlar, sonuç `OutOfMemoryError: unable to create new native thread` ya da yanıt veremeyen sistemdir. Sorun boşta kalanları temizlememesi değil (60 saniyede temizler), üst sınırın olmamasıdır. Aynı gerekçeyle **sınırsız kuyruklu** `newFixedThreadPool` da dikkat ister: orada reddedilme hiç olmaz, ama kuyruk büyüyüp belleği tüketir.
- **`submit` vs `execute`**: `Future`, sonuç + durum + iptal + `ExecutionException` ile hata görünürlüğü demektir.
- **Zarif kapatma**: iste (`shutdown`) → bekle (`awaitTermination`) → gerekirse zorla (`shutdownNow`).
- **`Future`'ın sınırları**: `get()` bloklar (asıl asenkronluğu kırar), görevler zincirlenemez ve sonuçlar birleştirilemez; iptal ise `Future`'da da vardır (`cancel(boolean)`) ve `Future<V>` zaten `Callable<V>`'nin sonucunu taşır. `CompletableFuture` eksik olanı verir: `thenApply`/`thenAccept` (sonucu dönüştür), `thenCompose` (asenkron görevleri zincirle), `thenCombine`/`allOf`/`anyOf` (birleştir/bekle), `exceptionally`/`handle` (hata akışı). Kazanç hızda değil — görevin süresi değişmez — **bloklamayan kompozisyondadır**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Future.get bloklama ile CompletableFuture zincirleme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Future.get() bloklar — CompletableFuture zincirler</text>
  <rect x="30" y="48" width="270" height="140" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Future</text>
  <text x="165" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">get() → BLOKLAR</text>
  <text x="165" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">zincirlenemez</text>
  <text x="165" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">birleştirilemez</text>
  <text x="165" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tekil sonuç için yeter</text>
  <rect x="340" y="48" width="270" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">CompletableFuture</text>
  <text x="475" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">thenApply / thenCompose</text>
  <text x="475" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">thenCombine / allOf</text>
  <text x="475" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">exceptionally (hata akışı)</text>
  <text x="475" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bloklamayan kompozisyon</text>
</svg>
```

### Tek cümlede

Havuz, görevi iş parçacığından ayırıp yeniden kullanım ve sınırlama sağlar; `submit`/`Future` sonucu ve hatayı görünür kılar, sınırlı kuyruk + reddedilme politikası yük altındaki davranışı belirler, üç adımlı kapatma da işi kaybetmeden temiz çıkışı sağlar.

# exec-1 | Orta
## Soru
İş parçacığı havuzu (thread pool) kullanmanın, her görev için `new Thread()` açmaya göre temel avantajı nedir?
## A
Havuz, görevleri sıraya soktuğu için yarış durumlarını (race condition) engeller ve görevleri her zaman hatasız çalıştırır
🤖 Çok iş parçacıklı havuzda görevler paralel çalışır; doğruluk yine kilit/senkronizasyondan gelir.
## B
Havuz her zaman tek bir iş parçacığı kullandığı için bellekten tasarruf eder
🤖 Havuz genelde çok iş parçacıklıdır (tek iş parçacıklı olan yalnız `newSingleThreadExecutor`); kazanç yeniden kullanımdadır.
## C
İş parçacıklarını yeniden kullanarak oluşturma/yıkma maliyetini ortadan kaldırır ve havuz boyutuyla eşzamanlılığı sınırlayarak kaynağı (bellek, CPU) kontrol altında tutar
🤖 Doğru: yeniden kullanım + sınırlama, ölçeklenebilir eşzamanlılığın temelidir.
## D
Havuz, paylaşılan duruma erişimi kendisi senkronize ettiği için `synchronized` ihtiyacını ortadan kaldırır
🤖 Kaldırmaz; havuz yalnızca görevleri yürütür, paylaşılan durumu korumak yine senin sorumluluğundur.
## Doğru
C
## Açıklama
İş parçacığı oluşturmak pahalıdır (yığın belleği ayrılır, işletim sistemi kaydı yapılır) ve sınırsız iş parçacığı açmak, yük altında belleği tüketip sistemi çökertir. İş parçacığı havuzu iki sorunu birden çözer: (1) **yeniden kullanım** — bir havuz iş parçacığı bir görevi bitirince yok olmaz, sıradaki görevi alır; böylece oluşturma/yıkma maliyeti amortize edilir. (2) **sınırlama** — havuz boyutu, aynı anda kaç görevin çalışacağını sınırlar; bu, CPU ve belleği öngörülebilir tutar ve aşırı yükte sistemi korur. Havuz kendi başına doğruluk sağlamaz (A yanlış; doğruluk kilit/senkronizasyondan gelir) ve `synchronized` ihtiyacını da kaldırmaz (D yanlış) — paylaşılan durum hâlâ korunmalıdır. Yani havuz bir **kaynak yönetimi** aracıdır: görevi iş parçacığından ayırır, eşzamanlılığı ölçeklenebilir kılar.

# exec-2 | Orta
## Soru
`ExecutorService.submit(task)` ile `execute(runnable)` arasındaki fark nedir?
## A
`submit` bir `Future` döndürür (görevin sonucuna ve fırlattığı istisnaya erişim sağlar); `execute` dönüş vermez, `void`'tir
🤖 Doğru: `Future` sonuç ve istisna erişimi için kritik farktır.
## B
`execute` daha yeni ve gelişmiş bir API olduğu için `submit` yerine her zaman tercih edilmelidir
🤖 Aksine `execute` daha eski ve daha yalın olan `Executor` metodudur; tercih ihtiyaca göre yapılır.
## C
`submit` görevi hemen çalıştırır, `execute` ise görevi kuyruğa alıp gecikmeli çalıştırır
🤖 İkisi de aynı kuyruk/havuz yolunu kullanır; gecikmeli çalıştırma `ScheduledExecutorService`'in işidir.
## D
`submit` yalnız `Runnable`, `execute` yalnız `Callable` kabul eder
🤖 Tersine yakın: `submit` hem `Runnable` hem `Callable` alır, `execute` yalnız `Runnable` alır.
## Doğru
A
## Açıklama
`execute(Runnable)`, `Executor` arayüzünden gelir; görevi çalıştırır ama geriye bir şey vermez (`void`) ve yalnız `Runnable` kabul eder. `submit(...)`, `ExecutorService`'ten gelir; hem `Runnable` hem `Callable` alır ve bir `Future` döndürür. Bu fark pratikte kritiktir: `Future` ile görevin **sonucunu** (`Callable` ise) alabilir, `isDone()` ile durumunu sorabilir, `cancel()` ile iptal isteyebilir ve en önemlisi görev bir istisna fırlattıysa bunu `get()` çağrısında `ExecutionException` olarak YAKALAYABİLİRSİN. `execute` ile gönderilen bir görev istisna fırlatırsa, istisna havuzun `UncaughtExceptionHandler`'ına gider ve kolayca gözden kaçar. Sonuç veya hata izlemesi gerekiyorsa `submit` + `Future`; sadece "ateşle ve unut" bir yan etki için `execute` yeterlidir.

# exec-3 | İleri
## Soru
Bir `ExecutorService`'i zarifçe (graceful) kapatmanın doğru yolu nedir?
## A
Doğrudan `shutdownNow()` çağırmak yeterlidir; havuz zaten çalışan görevlerin bitmesini bekler
🤖 `shutdownNow` beklemez; çalışan görevleri `interrupt` ile keser ve başlamamışları liste olarak geri verir.
## B
Hiç kapatmamak; JVM kapanınca havuz da kendiliğinden kapanır, havuz iş parçacıkları arka plan (daemon) olduğu için engel çıkarmaz
🤖 Havuzun iş parçacıkları varsayılanda kullanıcı (non-daemon) iş parçacığıdır; kapatılmazsa JVM'in kapanmasını bile engeller.
## C
`shutdown()` çağırıp görevlerin bitişini hiç beklemeden devam etmek yeterlidir
🤖 `shutdown()` yeni görev alımını durdurur ama hemen döner; görevlerin bittiğini `awaitTermination` ile doğrulamadan bilemezsin.
## D
`shutdown()` ile yeni görev alımını durdur, `awaitTermination(süre)` ile bitmelerini bekle, süre dolarsa `shutdownNow()` ile kes
🤖 Doğru: iste-bekle-gerekirse-zorla üç adımlı standart desen.
## Doğru
D
## Açıklama
Zarif kapatma üç adımlıdır: (1) `shutdown()` — havuz yeni görev KABUL ETMEYİ durdurur ama kuyrukta bekleyen ve çalışan görevlerin tamamlanmasına izin verir (bloklamaz, hemen döner). (2) `awaitTermination(süre, birim)` — tüm görevler bitene ya da süre dolana kadar BLOKLAYARAK bekler. (3) Süre içinde bitmezse `shutdownNow()` — çalışan iş parçacıklarını `interrupt` ile kesmeye çalışır ve henüz başlamamış görevleri liste olarak döndürür. Sadece `shutdownNow` (A) devam eden işi yarıda bırakır ve veri tutarsızlığına yol açabilir; hiç kapatmamak (B) ise havuz iş parçacıkları varsayılanda **kullanıcı (non-daemon) iş parçacığı** olduğundan JVM'in kapanmasını bile engelleyebilir (sızıntı). Bu üç adımlı desen, işi kaybetmeden ama sonsuza dek de beklemeden temiz kapanmayı sağlar.

```java
pool.shutdown();
if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
    pool.shutdownNow();   // zaman aşımı: kalanları kesmeye çalış
}
```

# exec-4 | İleri
## Soru
`ThreadPoolExecutor`'da kuyruk ve havuz dolduğunda gelen yeni görevleri ne belirler?
## A
Görevler otomatik olarak sessizce çalıştırılır; havuz ne kadar dolu olursa olsun bir sınır devreye girmez
🤖 Sınırlı yapılandırmada sessiz çalıştırma yoktur; bir politika devreye girer.
## B
`RejectedExecutionHandler` (reddedilme politikası): varsayılan `AbortPolicy` istisna fırlatır, `CallerRunsPolicy` geri-basınç uygular
🤖 Doğru: doluluk anında davranışı reddedilme politikası belirler.
## C
JVM havuzu ihtiyaç kadar otomatik büyütür, bu yüzden hiçbir görev reddedilmez
🤖 Havuz `maximumPoolSize` ile sınırlıdır; otomatik sınırsız büyümez.
## D
Görevler sessizce kaybolur; ne istisna ne de günlük kaydı ile bildirim olur
🤖 Varsayılan politika sessizce atmaz; `AbortPolicy` istisna fırlatır.
## Doğru
B
## Açıklama
Sınırlı bir `ThreadPoolExecutor`'da (sınırlı kuyruk + `maximumPoolSize`), hem tüm iş parçacıkları meşgul hem de kuyruk doluysa yeni görev **reddedilir** ve bunu `RejectedExecutionHandler` yönetir. Yerleşik politikalar: `AbortPolicy` (varsayılan) — `RejectedExecutionException` fırlatır, çağıran hatayı görür; `CallerRunsPolicy` — görevi, gönderen iş parçacığında SENKRON çalıştırır, böylece gönderen yavaşlar ve sisteme doğal **geri-basınç** (backpressure) uygulanır (üretim hızı tüketim hızına iner, bu genelde en sağlıklı seçenektir); `DiscardPolicy` — görevi sessizce atar; `DiscardOldestPolicy` — kuyruktaki en eskiyi atıp yenisini ekler. Doğru politika seçimi, aşırı yük altında sistemin nasıl davranacağını (hata mı ver, yavaşla mı, veri mi at) belirleyen bir tasarım kararıdır. Sınırsız kuyruklu `newFixedThreadPool` gibi havuzlarda reddedilme hiç olmaz — ama bu sefer kuyruk sınırsız büyüyüp belleği tüketebilir.

# exec-5 | Uzman
## Soru
`Executors.newCachedThreadPool()`'un üretim (production) sistemlerinde en büyük tehlikesi nedir?
## A
Yalnızca tek bir iş parçacığı kullandığı için yoğun yükte darboğaz oluşturur
🤖 Tam tersi; sorun iş parçacığı azlığı değil, üst sınırın olmamasıdır.
## B
Görevleri kuyrukta biriktirip hiç çalıştırmaması, bu yüzden isteklerin zaman aşımına uğraması
🤖 Görevleri hemen çalıştırır; hatta kuyrukta biriktirmediği (`SynchronousQueue`) için sınırsız iş parçacığı açar.
## C
Üst sınırı `Integer.MAX_VALUE` olduğundan ani yükte neredeyse sınırsız iş parçacığı açar ve `OutOfMemoryError`'a yol açabilir
🤖 Doğru: sınırsız iş parçacığı = kontrolsüz kaynak tüketimi.
## D
Boştaki iş parçacıklarını hiç temizlememesi, bu yüzden havuzun sürekli büyümesi
🤖 Boşta kalanları 60 saniye sonra temizler; tehlike yavaş küçülme değil, sınırsız büyümedir.
## Doğru
C
## Açıklama
`newCachedThreadPool()`, iş parçacığı üst sınırı `Integer.MAX_VALUE` olan ve doğrudan devir (`SynchronousQueue`) kullanan bir havuzdur: gelen her görev, boşta iş parçacığı yoksa YENİ bir iş parçacığı açar. Görevler tüketilebildiğinden daha hızlı geldiği ani yük (trafik zirvesi, yavaş bağımlı servis) durumunda, havuz on binlerce iş parçacığı açmaya çalışır; her biri yığın belleği tüketir, bağlam değişimi (context switch) maliyeti patlar ve sonuç genellikle `OutOfMemoryError: unable to create new native thread` ya da sistemin yanıt veremez hâle gelmesidir. Bu, "başarı anında çökme" (yük arttıkça çöken) tehlikeli bir kalıptır. Doğru yaklaşım, `ThreadPoolExecutor`'ı **açık üst sınır ve sınırlı kuyruk** ile yapılandırıp uygun bir reddedilme politikası (ör. `CallerRunsPolicy`) vermektir; böylece sistem yük altında kontrollü biçimde yavaşlar veya geri-basınç uygular, sessizce büyüyüp çökmez. Aynı gerekçeyle sınırsız kuyruklu `newFixedThreadPool` da dikkatli kullanılmalıdır.

# exec-6 | İleri
## Soru
`Future`'ın sınırları ve bunları aşmak için `CompletableFuture`'ın sunduğu nedir?
## A
`Future.get()` sonuç gelene dek BLOKLAR, görevler zincirlenemez ve sonuçlar birleştirilemez; `CompletableFuture` bloklamayan asenkron kompozisyon sağlar
🤖 Doğru: bloklama ve kompozisyon eksikliği `Future`'ın sınırıdır, `CompletableFuture` bunları çözer.
## B
`Future` bir kez başlayan görevi iptal edemez; asıl yenilik `CompletableFuture`'ın iptal yeteneği eklemesidir
🤖 `Future`'da da `cancel(boolean)` vardır; asıl fark iptalde değil, kompozisyondadır.
## C
`Future` yalnızca `Runnable` görevlerle çalışır; `CompletableFuture` ise değer döndüren `Callable` desteğini ekler
🤖 `Future<V>` zaten `Callable<V>`'nin sonucunu taşır; ayrım bu değildir.
## D
`CompletableFuture` aynı görevi her zaman daha hızlı, daha az CPU harcayarak tamamlar
🤖 Görevin süresi değişmez; kazanım hızda değil, bloklamayan asenkron kompozisyondadır.
## Doğru
A
## Açıklama
`Future` sonuç almak için yeterli ama kısıtlıdır: `get()` sonuç hazır olana kadar çağıran iş parçacığını **bloklar** (asıl asenkronluğu kırar); bir görevin sonucunu başka bir göreve akıtmak (zincirleme) ya da birden çok görevin sonucunu birleştirmek için dilde doğrudan destek yoktur — elle `get` + yeni `submit` yazarsın, bu da bloklamaya ve karmaşaya döner. `CompletableFuture` bu boşlukları doldurur: `thenApply`/`thenAccept` (sonucu dönüştür), `thenCompose` (bir asenkron görevi diğerine zincirle), `thenCombine`/`allOf`/`anyOf` (birden çok sonucu birleştir/bekle) ve `exceptionally`/`handle` (hata akışını yönet) ile **bloklamadan** asenkron işlem grafikleri kurabilirsin. Böylece "A'yı çağır, sonucunu B'ye ver, C ile birleştir, hata olursa yedek değer dön" gibi akışlar geri çağrım (callback) zinciriyle, iş parçacığını meşgul etmeden ifade edilir. Kısaca: tekil sonuç için `Future` yeter; asenkron kompozisyon gerektiğinde `CompletableFuture`.
