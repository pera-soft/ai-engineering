---
slug: concurrency-temel
baslik: "Eşzamanlılık Temelleri: Thread, synchronized, wait/notify"
kademe: Orta
sira: 46
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir mutfakta tek aşçı varken sıra karışmaz; ama sipariş çoğalınca ikinci, üçüncü aşçıyı alırsın. İşler hızlanır — ta ki ikisi aynı tencereye aynı anda tuz atana kadar. Eşzamanlılık (concurrency) tam olarak budur: bir programın birden çok işi aynı dönemde ilerletmesi. Kazanç hız ve yanıt verebilirliktir; bedel, paylaşılan şeylere aynı anda dokunulunca çıkan öngörülemez hatalardır — bu konu o çarpışmaları önlemenin kurallarıdır.

### Temel kavramlar

- **İş parçacığı (thread)**: aynı program içinde bağımsız ilerleyen yürütme hattı. Kendi çağrı yığını (stack) vardır, ama nesnelerin durduğu belleği (heap) diğerleriyle **paylaşır**.
- **Eşzamanlılık ve paralellik**: eşzamanlılık işlerin iç içe ilerlemesi, paralellik gerçekten farklı çekirdeklerde aynı anda çalışmasıdır.
- **Paylaşılan durum (shared state)**: birden çok iş parçacığının okuyup yazdığı değişken ya da nesne.
- **Araya girme (interleaving)**: iş parçacıklarının adımlarının birbirinin arasına karışması. Sıra her koşuda değişebilir; kimse garanti vermez.
- **Yarış durumu (race condition)**: programın doğruluğunun bu araya girme sırasına bağlı ve öngörülemez hâle gelmesi. Klasik örnek `sayac++`: iki iş parçacığı da 7 okur, ikisi de 8 yazar — bir artış kaybolur (kayıp güncelleme).
- **Kritik bölge (critical section)**: paylaşılan duruma dokunan, aynı anda tek iş parçacığına açık olması gereken kod parçası.
- **Kilit (lock) ve monitör (monitor)**: her Java nesnesinin içinde gizli bir "anahtar" vardır; buna örtük kilit (intrinsic lock) ya da monitör denir. Anahtarı alan içeri girer, işi bitince bırakır.
- **Karşılıklı dışlama (mutual exclusion)**: aynı kilit üzerinde aynı anda yalnız bir iş parçacığının çalışabilmesi.
- **Atomiklik ve görünürlük**: bölünmezlik ve "benim yazdığımı öteki görsün" garantisi. `synchronized` ikisini birden verir.
- **Bloklama (blocking)**: iş parçacığının bir kilit ya da olay beklerken durup ilerlememesi.

```java
int sayac = 0;   // iki iş parçacığı 100_000'er kez sayac++ yapsın
// beklenen 200000, gerçek çıktı ör. 156341
// çünkü sayac++ tek adım değil: oku sayac → 1 ekle → geri yaz
```

### Nasıl çalışır?

Bir iş parçacığı şu durumlardan geçer: `NEW` → `RUNNABLE` → `BLOCKED` / `WAITING` / `TIMED_WAITING` → `TERMINATED`. Anlamları sırasıyla şudur: nesne oluştu ama henüz başlamadı; çalışıyor ya da çalışmaya hazır; kilit bekliyor; bir olayı ya da süreyi bekliyor; bitti. Geçişi `start()` başlatır: işletim sisteminden yeni bir iş parçacığı ister ve `run()`'ı ONUN içinde çalıştırır. `run()`'ı doğrudan çağırmak ise sıradan bir metot çağrısıdır — `run()` herkese açık bir metottur, kod sorunsuz derlenir, hiçbir istisna fırlamaz; ama yeni iş parçacığı açılmaz ve gövde mevcut (çağıran) iş parçacığında senkron çalışır. Beklenen paralellik olmaz — klasik bir başlangıç hatası. Ayrıca aynı `Thread` nesnesinde `start()` yalnızca BİR kez çağrılabilir; ikinci çağrı `IllegalThreadStateException` fırlatır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İş parçacığı yaşam döngüsü durumları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Thread yaşam döngüsü</text>
  <rect x="20" y="80" width="110" height="50" rx="10" fill="#EEF3FA" stroke="#2C5B8A" stroke-width="1.5"/>
  <text x="75" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#2C5B8A" font-family="system-ui">NEW</text>
  <rect x="170" y="80" width="120" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="230" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">RUNNABLE</text>
  <rect x="330" y="80" width="150" height="50" rx="10" fill="#FFF6E6" stroke="#B5851A" stroke-width="1.5"/>
  <text x="405" y="104" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B5851A" font-family="system-ui">BLOCKED/WAITING</text>
  <text x="405" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kilit/olay bekler</text>
  <rect x="520" y="80" width="100" height="50" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="570" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B4322B" font-family="system-ui">TERMINATED</text>
  <path d="M130 105 L168 105" stroke="#5D7789" stroke-width="1.5" marker-end="url(#conc-ok)"/>
  <path d="M290 100 L328 100" stroke="#5D7789" stroke-width="1.5" marker-end="url(#conc-ok)"/>
  <path d="M405 128 C405 160, 230 160, 230 132" stroke="#5D7789" stroke-width="1.3" fill="none" marker-end="url(#conc-ok)"/>
  <path d="M290 112 L328 118" stroke="#B5851A" stroke-width="1.2" fill="none" opacity="0.6"/>
  <path d="M480 105 L518 105" stroke="#5D7789" stroke-width="1.5" marker-end="url(#conc-ok)"/>
  <defs>
    <marker id="conc-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="192" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">start() RUNNABLE'a taşır; run() doğrudan çağrılırsa mevcut iş parçacığında çalışır.</text>
</svg>
```

Kritik bölgeyi korumanın en yalın yolu `synchronized`'dır: bir nesnenin monitörünü alır, blok normal ya da istisnayla bittiğinde JVM kilidi otomatik bırakır. Peki kilit **hangi nesnenin**? Örnek (instance) metodunda `this`; statik metotta sınıfın `Class` nesnesi (`Sinif.class`); `synchronized (x) { ... }` bloğunda `x`. Bu ayrım pratikte kritiktir: aynı nesnenin iki `synchronized` örnek metodu aynı anda çalışamaz, ama farklı örnekler birbirini bekletmez; statik ve örnek metotlar FARKLI kilitler kullandığından birbirini dışlamaz. JVM'e ait global tek bir kilit yoktur, kilidi metodun parametreleri belirlemez ve her çağrıda yeni monitör üretilmez. Yanlış nesne üzerinden kilitlemek (her seferinde `new Object()`, ya da kutulanmış/paylaşılan `Integer`) sessiz eşzamanlılık hatalarının kaynağıdır; bu yüzden genelde `private final Object kilit = new Object();` tercih edilir.

`synchronized` iki sorunu birden çözer: karşılıklı dışlama (yarış durumunu keser) ve görünürlük — kilidi BIRAKAN iş parçacığının tüm yazmaları, aynı kilidi sonradan ALAN iş parçacığına görünür olur (happens-before). Dikkat: bu garanti yalnız aynı kilidi alanlar arasındadır; kilit dışındaki senkronizasyonsuz okumalar için hiçbir söz verilmez, metot içinde okunan alanlar `volatile` olmaz. Metodu gizlemez ya da bir iş parçacığına sahiplendirmez — erişimi sıraya sokar, sırayla herkes girer. Bedeli de vardır: çekişme (contention) altında iş parçacıkları birbirini bekler, yani hızlanma değil yavaşlama olur. Bu yüzden kilitlenen bölge olabildiğince küçük tutulur.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="sayac++ yarış durumu ve kayıp güncelleme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yarış durumu: sayac++ kayıp güncelleme</text>
  <text x="150" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İş parçacığı A</text>
  <text x="490" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">İş parçacığı B</text>
  <rect x="40" y="64" width="220" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="150" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku sayac = 7</text>
  <rect x="380" y="102" width="220" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="490" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku sayac = 7</text>
  <rect x="40" y="140" width="220" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="150" y="160" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz sayac = 8</text>
  <rect x="380" y="178" width="220" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="490" y="198" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz sayac = 8</text>
  <text x="320" y="224" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">İki artış → 9 beklenirdi ama 8; bir artış kayboldu.</text>
</svg>
```

Görevi iş parçacığından ayırmak için iki arayüz kullanılır. `Runnable`'ın `void run()` metodu değer döndürmez ve kontrollü istisna (checked exception) fırlatamaz; `Callable<V>`'nin `V call() throws Exception` metodu bir **değer döndürür** ve istisna fırlatabilir — sonucu `ExecutorService.submit(...)`'in verdiği `Future<V>` üzerinden `get()` ile okursun. İkisi de arayüzdür (üstelik fonksiyonel arayüzdür, lambda ile yazılabilir), ikisi de tek başına iş parçacığı açmaz — onu `Thread` ya da havuz açar — ve ikisi de çok iş parçacıklı havuzlarda paralel çalışabilir. Sadece yan etki içinse (log yazmak, kuyruk işlemek) `Runnable` yeterlidir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Runnable ile Callable farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Runnable vs Callable</text>
  <rect x="30" y="48" width="270" height="130" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Runnable</text>
  <text x="165" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">void run()</text>
  <text x="165" y="120" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">değer döndürmez</text>
  <text x="165" y="142" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">checked istisna fırlatamaz</text>
  <text x="165" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yan etki: log, kuyruk</text>
  <rect x="340" y="48" width="270" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Callable&lt;V&gt;</text>
  <text x="475" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">V call() throws Exception</text>
  <text x="475" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">değer DÖNDÜRÜR</text>
  <text x="475" y="142" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">istisna fırlatabilir</text>
  <text x="475" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonuç Future ile alınır</text>
</svg>
```

İş parçacıklarının birbirini beklemesi için `Object` monitörünün üç metodu vardır. `wait()` iş parçacığını uyutur ve **kilidi BIRAKIR** (bırakmasaydı kimse durumu değiştirip haber veremezdi); `notify()` bekleyenlerden yalnız BİRİNİ, `notifyAll()` hepsini uyandırır. Üçü de yalnızca o nesnenin monitörü elde tutulurken — yani `synchronized` metot/blok içinden — çağrılabilir; dışarıdan çağrılırsa `IllegalMonitorStateException` fırlar. Ve `wait()` daima bir `while` koşuluyla kullanılır:

```java
synchronized (kilit) {
    while (!kosulHazir) {   // if DEĞİL, while
        kilit.wait();       // kilidi bırakır; uyanınca koşulu YENİDEN sınar
    }
    // koşul sağlandı, güvenle devam
}
```

Sebep: yanlış/erken uyanma (spurious wakeup) mümkündür ve uyandıktan sonra kilidi geri alana kadar başka bir iş parçacığı koşulu tekrar bozmuş olabilir.

### Mülakatta ne sorulur?

- **`start()` mi `run()` mu?** Eşzamanlılığı başlatan `start()`'tır; `run()` doğrudan çağrılırsa yeni iş parçacığı açılmaz (daemon ayarının da bununla ilgisi yoktur).
- **Yarış durumu nedir?** İki iş parçacığının aynı anda başlatılması ya da birinin daha hızlı bitmesi değil; paylaşılan duruma senkronizasyonsuz erişimdir. Öngörülemez yanlış sonuç bir hatadır, "normal" değildir; nadiren ve yük altında çıktığı, yeniden üretilmesi zor olduğu için en sinsisidir. Çözüm: kritik bölgeyi senkronize etmek ya da atomik/değişmez yapılar kullanmak.
- **`synchronized` neyi garanti eder, kilit hangi nesnedir?** Karşılıklı dışlama + happens-before görünürlüğü; kilit `this` / `Sinif.class` / blokta verilen nesne.
- **`wait`/`notify` kuralları**: monitör zorunlu, `wait` kilidi bırakır, `notify` yalnız birini uyandırır (hangisi olduğu garanti değildir, bu yüzden `notifyAll` genelde daha güvenlidir), koşul `while` içinde sınanır.
- **`Runnable` vs `Callable`**: dönüş değeri ve kontrollü istisna yeteneği; görev-tabanlı eşzamanlılıkta hangisini seçeceğini bu belirler.
- Modern kodda bu düşük seviyeli mekanizma yerine `BlockingQueue`, `Semaphore`, `CountDownLatch` gibi `java.util.concurrent` araçları tercih edilir.

### Tek cümlede

Eşzamanlılık paylaşılan duruma aynı anda dokunma sorunudur; `start()` iş parçacığını açar, `synchronized` kritik bölgeyi hem yarıştan hem görünürlük sorunundan korur, `wait`/`notify` ise monitör elde tutulurken ve `while` koşuluyla kullanılır.

# conc-1 | Temel
## Soru
Bir `Thread` nesnesinde `start()` yerine yanlışlıkla `run()` çağırırsan ne olur?
## A
Derleme hatası alırsın; `run()` yalnızca JVM'in çağırabileceği korumalı bir metottur
🤖 `run()` herkese açık (public) bir metottur; kod sorunsuz derlenir.
## B
Çalışma zamanında `IllegalThreadStateException` fırlatılır
🤖 O istisna aynı `Thread` üzerinde `start()`'ı ikinci kez çağırınca fırlar; `run()` sessizce çalışır.
## C
Yeni bir iş parçacığı açılır ama arka plan (daemon) önceliğiyle çalışır
🤖 `run()` doğrudan çağrıldığında yeni iş parçacığı hiç açılmaz; daemon ayarının da bununla ilgisi yoktur.
## D
Yeni iş parçacığı AÇILMAZ; `run()` gövdesi MEVCUT (çağıran) iş parçacığında sıradan bir metot gibi eşzamanlı olmadan çalışır
🤖 Doğru: eşzamanlılığı başlatan `start()`'tır, `run()`'ı çağırmak değil.
## Doğru
D
## Açıklama
`start()`, işletim sistemine yeni bir iş parçacığı oluşturmasını söyler ve o yeni iş parçacığı içinde `run()`'ı çalıştırır — asıl eşzamanlılık buradan gelir. `run()`'ı DOĞRUDAN çağırmak ise onu sıradan bir metot çağrısına indirger: kod, çağrıyı yapan MEVCUT iş parçacığında, senkron biçimde çalışır; hiç yeni iş parçacığı açılmaz. Bu, yeni başlayanların en klasik hatasıdır — program hatasız derlenir ve çalışır ama beklenen paralellik olmaz. Ayrıca bir `Thread` üzerinde `start()` yalnızca BİR kez çağrılabilir; ikinci çağrı `IllegalThreadStateException` fırlatır.

# conc-2 | Temel
## Soru
`Runnable` ile `Callable` arasındaki temel fark nedir?
## A
`Runnable` yeni bir iş parçacığı açar, `Callable` ise görevi çağıran iş parçacığında çalıştırır; yani iş parçacığını arayüz seçimi belirler
🤖 İkisi de yalnızca göreve ait bir arayüzdür; iş parçacığını açan `Thread` ya da havuzdur.
## B
`Callable.call()` bir değer DÖNDÜRÜR ve kontrollü istisna (checked exception) fırlatabilir; `Runnable.run()` ikisini de yapamaz
🤖 Doğru: dönüş değeri ve istisna yeteneği temel ayrımdır.
## C
`Callable` yalnızca tek iş parçacıklı bir havuzda çalışabilir, paralel yürütülemez
🤖 Böyle bir kısıt yok; `Callable` da çok iş parçacıklı havuzlarda paralel çalışır.
## D
`Runnable` bir soyut sınıftır, bu yüzden ondan türetmek zorunludur; `Callable` ise arayüzdür
🤖 İkisi de arayüzdür (üstelik fonksiyonel arayüz); lambda ile de yazılabilirler.
## Doğru
B
## Açıklama
`Runnable`, `void run()` metoduna sahiptir: değer döndürmez ve yalnız denetimsiz (unchecked) istisna fırlatabilir. `Callable<V>` ise `V call() throws Exception` imzasına sahiptir: bir **sonuç** üretir ve kontrollü istisna fırlatabilir. Bir görevin sonucuna ihtiyacın varsa `Callable` kullanır ve dönüşü `ExecutorService.submit(callable)` ile aldığın `Future<V>` üzerinden `get()` ile okursun. Eğer görev yalnızca yan etki için çalışıyorsa (log yazmak, kuyruk işlemek) `Runnable` yeterlidir. Bu ayrım, görev-tabanlı eşzamanlılıkta (executor'lar) hangi arayüzü seçeceğini belirler.

# conc-3 | Orta
## Soru
"Race condition" (yarış durumu) tam olarak nedir?
## A
İki iş parçacığının aynı anda başlatılmaya çalışılmasıdır
🤖 Başlatma değil; paylaşılan duruma eşzamanlı erişimdir.
## B
Bir iş parçacığının diğerinden daha hızlı bitmesidir
🤖 Hız farkı sorun değildir; sorun senkronizasyonsuz paylaşımdır.
## C
Sonucun, paylaşılan duruma senkronizasyonsuz erişen iş parçacıklarının araya girme (interleaving) sırasına bağlı hâle gelmesidir
🤖 Doğru: sonuç zamanlamaya bağlıysa yarış vardır.
## D
CPU'nun iş parçacıklarını rastgele sıraladığı normal bir durumdur, öngörülemez sonuç doğursa da hata sayılmaz
🤖 Öngörülemez yanlış sonuç bir hatadır; "normal" değildir.
## Doğru
C
## Açıklama
Race condition, programın doğruluğunun iş parçacıklarının **zamanlamasına/araya girme sırasına** (interleaving) bağlı hâle gelmesidir. Birden çok iş parçacığı paylaşılan bir değişkeni senkronizasyon olmadan okuyup yazdığında, hangi iş parçacığının hangi adımı ne zaman yürüteceği belirsizdir; bir çalışmada doğru, diğerinde yanlış sonuç çıkabilir. Klasik örnek `sayac++`: iki iş parçacığı aynı değeri okuyup ikisi de aynı sonucu yazarsa bir artış kaybolur (lost update). Çözüm, paylaşılan duruma erişimi bir **kritik bölge** olarak senkronize etmek (`synchronized`, kilit) veya atomik/değişmez yapılar kullanmaktır. Yarış durumları en sinsi hatalardır çünkü nadiren, yük altında ve yeniden üretilmesi zor biçimde ortaya çıkar.

# conc-4 | Orta
## Soru
Bir metodu `synchronized` yapmak neyi garanti eder?
## A
O nesnenin monitörü (intrinsic lock) üzerinden karşılıklı dışlama sağlar; ayrıca kilit bırak→al arası happens-before ile görünürlük getirir
🤖 Doğru: hem karşılıklı dışlama hem görünürlük getirir.
## B
Metodu diğer iş parçacıklarından tamamen gizler; yalnızca onu ilk çağıran iş parçacığı bir daha çağırabilir
🤖 Gizleme ya da sahiplenme yok; eşzamanlı erişimi sıraya sokar, sırayla herkes girer.
## C
Metodun çekişme (contention) altında bile her zaman daha hızlı çalışmasını sağlar
🤖 Aksine; kilit bekleyişi ve bağlam değişimi maliyeti yüzünden çekişme altında yavaşlatır.
## D
Metot içinde okunan tüm alanları otomatik `volatile` yapar, dolayısıyla kilit dışındaki okumalar da güncel olur
🤖 Kilit dışında yapılan senkronizasyonsuz okumalar için hiçbir garanti yoktur; happens-before yalnız aynı kilidi alanlar arasında kurulur.
## Doğru
A
## Açıklama
`synchronized`, bir nesnenin **monitörünü** (intrinsic lock) kullanır. Örnek (instance) metodu için kilit `this`, statik metot için sınıf nesnesidir. Aynı kilit üzerinde aynı anda yalnızca bir iş parçacığı çalışabilir — bu **karşılıklı dışlamadır** (mutual exclusion) ve kritik bölgeyi korur. Ek olarak, bir iş parçacığı kilidi BIRAKTIĞINDA yaptığı tüm yazmalar, kilidi sonradan ALAN iş parçacığına görünür olur (happens-before). Yani `synchronized` iki sorunu birden çözer: yarış durumu (atomik kritik bölge) ve görünürlük. Karşılığında maliyeti vardır: kilit çekişmesi (contention) altında iş parçacıkları birbirini bekleyeceğinden ölçeklenirlik düşebilir; bu yüzden kilitlenen bölge olabildiğince küçük tutulur.

# conc-5 | İleri
## Soru
`synchronized` kilidi hangi nesne üzerinden alınır?
## A
Her zaman JVM'e ait tek bir global kilit üzerinden; bu yüzden farklı nesnelerin `synchronized` metotları da birbirini bekletir
🤖 Global tek kilit yoktur; kilit nesne bazlıdır, farklı nesneler birbirini bekletmez.
## B
Metodun ilk parametresi üzerinden; parametresiz metotlarda kilit alınmaz
🤖 Parametrelerin kilitle ilgisi yoktur; kilidi bildirim yeri (örnek/statik/blok) belirler.
## C
Her çağrıda JVM'in ayırdığı yeni bir monitör üzerinden
🤖 Her çağrıda yeni kilit olsaydı hiçbir dışlama olmazdı; monitör belirli ve kalıcı bir nesnedir.
## D
Örnek metodunda `this`, statik metotta sınıf nesnesi (`Sinif.class`), `synchronized(x)` bloğunda ise `x` nesnesinin monitörü üzerinden
🤖 Doğru: kilit her zaman belirli bir monitör nesnesine bağlıdır.
## Doğru
D
## Açıklama
`synchronized`'ın kilidi daima belirli bir **monitör nesnesine** bağlıdır: (1) `synchronized` örnek metodu → `this` nesnesinin monitörü; (2) `synchronized` statik metot → sınıfın `Class` nesnesinin (`Sinif.class`) monitörü; (3) `synchronized (kilitNesnesi) { ... }` bloğu → verilen nesnenin monitörü. Bu ayrım pratikte kritiktir: örnek metotları `this` üzerinden kilitlenir, bu yüzden aynı nesnenin iki `synchronized` örnek metodu aynı anda çalışamaz — ama farklı örnekler bağımsızdır. Statik ve örnek `synchronized` metotları FARKLI kilitler kullandığından birbirini dışlamaz. Yanlış nesne üzerinden kilitlemek (ör. her seferinde yeni bir nesne, veya kutulanmış/paylaşılan `Integer`) sessiz eşzamanlılık hatalarının kaynağıdır; genelde özel `private final Object kilit = new Object();` alanı tercih edilir.

# conc-6 | İleri
## Soru
`wait()`, `notify()` ve `notifyAll()` metotlarıyla ilgili hangisi doğrudur?
## A
Herhangi bir yerden, kilit almadan çağrılabilirler; monitör sahipliğini JVM çağrı anında kendisi sağlar
🤖 Yanlış; kilit dışında çağrılırsa `IllegalMonitorStateException` fırlar.
## B
Yalnızca ilgili nesnenin `synchronized` bloğu içinden (monitör elde tutulurken) çağrılabilirler; `wait()` daima bir `while` koşuluyla kullanılmalıdır
🤖 Doğru: monitör sahipliği zorunlu, `while` koşulu güvenlik için şart.
## C
`notify()` bekleyen tüm iş parçacıklarını uyandırır; `notifyAll()` ise yalnız en uzun bekleyeni uyandırır
🤖 `notify()` yalnız BİRİNİ uyandırır; hepsi için `notifyAll()` gerekir.
## D
`wait()` çağrıldığında iş parçacığı kilidi tutmaya devam eder; bu yüzden `wait()` bloğun en sonuna yazılmalıdır
🤖 Aksine, `wait()` kilidi BIRAKIR; yoksa hiç kimse durumu değiştiremezdi.
## Doğru
B
## Açıklama
`wait`/`notify`/`notifyAll`, `Object` monitör mekanizmasının koordinasyon araçlarıdır ve **yalnız o nesnenin monitörü elde tutulurken** (yani `synchronized` bloğu/metodu içinden) çağrılabilir; aksi hâlde `IllegalMonitorStateException` fırlar. `wait()`, iş parçacığını uyutur ve kilidi BIRAKIR (D yanlış) — böylece başka bir iş parçacığı kritik bölgeye girip durumu değiştirebilir ve `notify()` çağırabilir. `notify()` bekleyenlerden yalnız BİRİNİ, `notifyAll()` hepsini uyandırır (C yanlış); hangi iş parçacığının uyanacağı garanti olmadığından genellikle `notifyAll()` daha güvenlidir. Kritik kural: `wait()` mutlaka bir `while (koşulSağlanmadı) wait();` döngüsünde kullanılmalıdır — çünkü yanlış/erken uyanma (spurious wakeup) veya koşulun başka iş parçacığınca tekrar bozulması mümkündür. Modern kodda bu düşük seviyeli mekanizma yerine `BlockingQueue`, `Semaphore`, `CountDownLatch` gibi `java.util.concurrent` araçları tercih edilir.

```java
synchronized (kilit) {
    while (!kosulHazir) {   // if DEĞİL, while
        kilit.wait();       // kilidi bırakır, uyandırılınca yeniden dener
    }
    // koşul sağlandı, güvenle devam
}
```
