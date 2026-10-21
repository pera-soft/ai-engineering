---
slug: spring-async-scheduling
baslik: "Spring Async ve Scheduling: @Async, @Scheduled ve Sanal Thread Executor"
kademe: Orta
sira: 91
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Kargo şubesine gidip paket bıraktığını düşün. Görevli seni paketin teslim edilmesini beklerken tezgâhta tutmaz; "kaydını aldım, gidebilirsin" der ve dağıtımı arka planda yapar. Ayrıca şubenin her sabah 09:00'da kasa sayımı gibi tekrar eden işleri vardır; bunlar kimse istemeden, takvime göre kendiliğinden çalışır. Spring'te birincisi `@Async` (asenkron yürütme), ikincisi `@Scheduled` (zamanlanmış görev) ile yapılır.

### Temel kavramlar

- **Senkron**: Çağıran, işin bitmesini bekler.
- **Asenkron**: Çağıran beklemez; iş arka planda başka bir thread'de yürür.
- **Thread (iş parçacığı)**: Programın aynı anda yürüyebilen çalışma hatlarından biri.
- **Thread havuzu / `TaskExecutor`**: Önceden açılmış thread'lerin tutulduğu ve işlerin sıraya konduğu yapı. Her iş için yeni thread açmaktan ucuzdur.
- **Zamanlanmış görev (scheduled task)**: Belirli aralıkla veya takvime göre kendiliğinden çalışan metot.
- **`Future` / `CompletableFuture`**: "Sonuç ileride burada olacak" sözü veren nesne. `CompletableFuture` üstüne `.thenApply`, `.exceptionally` gibi halkalar takılabilir.
- **Vekil nesne (proxy)**: Spring'in bean'in (kendi yönettiği nesnenin) etrafına sardığı sarmalayıcı. Çağrıyı yakalar, ek davranışı (asenkronluk, işlem, önbellek) ekler, sonra gerçek metoda geçirir.
- **Sanal thread (virtual thread)**: Java 21+ ile kararlı hâle gelen, işletim sistemi thread'ine göre çok ucuz thread. Bloke olunca altındaki **taşıyıcı** (carrier) platform thread'ini serbest bırakır.

`@Async`, işaretlenen metodu çağıran thread yerine bir `TaskExecutor` havuzunda çalıştırır; çağıran hemen devam eder. Metot `void`, `Future<T>` veya tercihen `CompletableFuture<T>` döndürebilir. `@Async`'in önbelleklemeyle (`@Cacheable`), işlem yönetimiyle (`@Transactional`) veya derlemeyle ilgisi yoktur. `@Scheduled` ise metodu periyodik tekrarlar — tek seferlik değildir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Async metot proxy üzerinden ayrı thread havuzunda çalışır, self-invocation atlar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@Async proxy üzerinden çalışır</text>
  <rect x="30" y="46" width="150" height="60" rx="10" fill="#F3EEE6" stroke="#9A6A1E" stroke-width="1.5"/>
  <text x="105" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#9A6A1E" font-family="system-ui">Çağıran</text>
  <text x="105" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">beklemeden devam</text>
  <rect x="230" y="46" width="150" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="305" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Spring proxy</text>
  <text x="305" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işi havuza devreder</text>
  <rect x="430" y="46" width="180" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="520" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">TaskExecutor havuzu</text>
  <text x="520" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrı thread'de çalışır</text>
  <path d="M180 76 L228 76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#async-ok)"/>
  <path d="M380 76 L428 76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#async-ok)"/>
  <rect x="150" y="150" width="340" height="44" rx="10" fill="#FBEDED" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="320" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Tuzak: this.asyncMetot() proxy'yi ATLAR</text>
  <text x="320" y="187" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">kendini-çağırma senkron çalışır — asenkron olmaz</text>
  <defs>
    <marker id="async-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**1. `@Async` proxy'ye bağlıdır.** İki koşul gerekir: `@EnableAsync` ile özellik açılmalı ve çağrı **proxy üzerinden** geçmeli. Aynı sınıf içinden `this.gonder()` yazarsan çağrı doğrudan nesnenin kendisine gider. **Vekil atlanır** ve asenkronluğu ekleyen sarmalayıcı hiç çalışmaz; metot sıradan senkron çağrı gibi yürür. Bu klasik tuzağın adı **nesnenin kendi metodunu kendi içinden çağırmasıdır** (self-invocation).

Çözüm, `@Async` metodu **ayrı bir bean'e** taşıyıp enjekte ederek çağırmaktır; nadiren `AopContext.currentProxy()` kullanılır. Ayrıca `@Async` metotları, vekilin sarabilmesi için **public** olmalıdır (private değil). Dönüş tipi kısıtı yoktur ve metot herhangi bir Spring bean'inde çalışır — controller zorunluluğu yoktur.

**2. `fixedRate` ile `fixedDelay` farklı şeyleri ölçer.** İkisi de milisaniye alır; fark birimde değil, süreyi **neyin arasında** saydıklarındadır. `fixedRate` ardışık çalıştırmaların **başlangıç anları** arasındaki sabit süredir. `fixedRate=1000` ile her saniye başında yeni çalıştırma tetiklenmeye çalışılır; görev süresi bu aralığı aşarsa çalıştırmalar sıkışır ve kuyruğa girer.

`fixedDelay` ise bir çalıştırmanın **bittiği** an ile sonrakinin **başladığı** an arasındaki sabit süredir. Görev bitince beklenir, sonra tekrar başlar; böylece çalıştırmalar asla üst üste binmez. Takvim tabanlı tetikleme için (`her Pazartesi 09:00`) `cron` kullanılır. İş süresi değişkense ve örtüşme istenmiyorsa genellikle `fixedDelay` seçilir. `@Scheduled` için `@EnableScheduling` gerekir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="fixedRate başlangıçlar arası sabit, fixedDelay bir çalışmanın bitişi ile sonrakinin başlangıcı arası sabit">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">fixedRate ile fixedDelay zaman çizgisi</text>
  <text x="14" y="70" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">fixedRate</text>
  <rect x="120" y="52" width="64" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="152" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görev</text>
  <rect x="248" y="52" width="64" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="280" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görev</text>
  <rect x="376" y="52" width="64" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="408" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görev</text>
  <path d="M120 94 L246 94" stroke="#186399" stroke-width="1.3" marker-start="url(#async2-ar)" marker-end="url(#async2-ar)"/>
  <path d="M248 94 L374 94" stroke="#186399" stroke-width="1.3" marker-start="url(#async2-ar)" marker-end="url(#async2-ar)"/>
  <text x="520" y="72" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">başlangıç → başlangıç</text>
  <text x="280" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">süre sabit; görev uzarsa çalıştırmalar sıkışır</text>
  <text x="14" y="156" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">fixedDelay</text>
  <rect x="120" y="138" width="88" height="30" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="164" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görev</text>
  <rect x="258" y="138" width="52" height="30" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="284" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görev</text>
  <rect x="360" y="138" width="96" height="30" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="408" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görev</text>
  <path d="M208 182 L258 182" stroke="#25804C" stroke-width="1.3" marker-start="url(#async2-ar)" marker-end="url(#async2-ar)"/>
  <path d="M310 182 L360 182" stroke="#25804C" stroke-width="1.3" marker-start="url(#async2-ar)" marker-end="url(#async2-ar)"/>
  <text x="548" y="158" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">bitiş → başlangıç</text>
  <text x="284" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">boşluk sabit; çalıştırmalar asla üst üste binmez</text>
  <defs>
    <marker id="async2-ar" markerWidth="7" markerHeight="7" refX="5" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**3. Varsayılan planlayıcı havuzu TEK thread'lidir.** Spring'in varsayılan `@Scheduled` planlayıcısı boyutu 1 olan bir `ThreadPoolTaskScheduler` kullanır. Birden çok zamanlanmış görev varsa hepsi **aynı thread'de sırayla** çalışır: uzun süren bir görev diğerlerini geciktirir veya kaçırtır. Görevler ayrı işletim sistemi süreçlerinde çalışmaz, hepsi aynı JVM'dedir; sınırsız thread de açılmaz. Çözüm: bir `TaskScheduler`/`ThreadPoolTaskScheduler` bean'i tanımlayıp havuz boyutunu artırmak (`spring.task.scheduling.pool.size`) ya da `@Async("raporExecutor")` ile iş türüne göre **kendi yürütücünü (executor)** seçmek. Böylece hızlı bildirim işi, ağır rapor işinin kuyruğunda beklemez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Varsayılan tek threadli planlayıcı havuzu görevleri sıraya sokar, havuz büyütmek veya sanal thread paralel yürütür">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">@Scheduled havuzu: varsayılan tek thread tuzağı</text>
  <rect x="24" y="42" width="290" height="150" rx="12" fill="#FBEDED" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="169" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Varsayılan: havuz boyutu 1</text>
  <rect x="44" y="80" width="250" height="30" rx="6" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.2"/>
  <text x="169" y="100" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Thread-1: A (uzun) çalışıyor…</text>
  <rect x="44" y="118" width="250" height="26" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1"/>
  <text x="169" y="136" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">B kuyrukta bekliyor</text>
  <rect x="44" y="150" width="250" height="26" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1"/>
  <text x="169" y="168" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">C gecikiyor / kaçırılıyor</text>
  <rect x="326" y="42" width="290" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Çözüm: havuzu büyüt / sanal thread</text>
  <rect x="346" y="80" width="250" height="26" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="471" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Thread-1: A çalışıyor</text>
  <rect x="346" y="112" width="250" height="26" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="471" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Thread-2: B çalışıyor</text>
  <rect x="346" y="144" width="250" height="26" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="471" y="162" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Thread-3: C çalışıyor</text>
  <text x="471" y="184" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">paralel — birbirini bloke etmez</text>
</svg>
```

**4. Hata `void`'de kaybolur.** `@Async` metot çağıran thread'den bağımsız çalıştığı için, `void` dönen bir async metotta fırlayan istisna **çağırana geri dönemez**; çağıran zaten devam etmiştir. Varsayılan davranış yalnız loglamaktır — kolayca gözden kaçar. Uygulama çökmez, JVM kapanmaz ve istisna bir sonraki HTTP yanıtına da iliştirilmez. Yakalamak için `AsyncConfigurer.getAsyncUncaughtExceptionHandler()` ezilerek bir **`AsyncUncaughtExceptionHandler`** bağlanır. Metot `CompletableFuture<T>` döndürüyorsa istisna future'ın tamamlanma durumuna yerleşir ve `.exceptionally(...)`, `.handle(...)` veya `get()` (ki `ExecutionException` sarar) ile ele alınır. Bu yüzden hata yönetimi önemli işlerde `void` yerine `CompletableFuture` tercih edilir.

### Mülakatta ne sorulur?

- **`@Async` neden çalışmadı?** Ya `@EnableAsync` yok, ya self-invocation: `this.metot()` **proxy'yi atlar**, iş senkron yürür.
- **`fixedRate` vs `fixedDelay`**: **başlangıçtan başlangıca** / **bitişten başlangıca**. Tek cümlelik bu ayrım klasik elemedir.
- **Varsayılan havuz**: `@Scheduled` planlayıcısı **tek thread'lidir**; üretimde boyut ayarlanmalı ya da iş türüne göre ayrı executor tanımlanmalıdır.
- **2026 executor seçimi**: I/O ağırlıklı işlerde `Executors.newVirtualThreadPerTaskExecutor()` en uygunudur; her göreve ucuz bir sanal thread düşer, bloke olunca taşıyıcı platform thread'i serbest kalır ve az OS thread'iyle on binlerce eş zamanlı bloke görev taşınır. Boot 3.2+ ile `spring.threads.virtual.enabled=true` bunu `@Async`, web istekleri ve zamanlanmış görevler için otomatik açar. Küçük sabit platform havuzu I/O'da eş zamanlılığı boğar, elle `new Thread(...)` yönetilemez, senkron çalıştırmak zaten asenkronluğu kaldırır. **DİKKAT:** CPU-yoğun işlerde sanal thread avantaj sağlamaz; orada sınırlı bir platform havuzu daha uygundur.
- **Async hata yönetimi**: `void` → `AsyncUncaughtExceptionHandler`; `CompletableFuture` → istisna future içinde taşınır.

### Tek cümlede

`@Async` ve `@Scheduled` bildirimsel kolaylıklardır, ama üç ayrıntıya bağlıdır. Çağrı vekilden geçmeli, `fixedRate`/`fixedDelay` ayrımı doğru seçilmeli ve varsayılan tek thread'li havuz yerine iş yüküne uygun bir yürütücü tanımlanmalıdır (2026'da I/O işleri için sanal thread'li olanı).

# async-1 | Temel
## Soru
Bir metoda `@Async` koymak ne sağlar?
## A
Metodu build sırasında daha hızlı derlenecek biçimde işaretler
🤖 `@Async` derlemeyle ilgili değildir; çalışma anındaki thread davranışını değiştirir.
## B
Metodun dönüş değerini otomatik olarak önbelleğe alıp tekrar kullanır
🤖 Önbellekleme `@Cacheable` işidir; `@Async` asenkron yürütmedir.
## C
Metodu bir veritabanı işlemine (transaction) sarıp atomik hâle getirir
🤖 İşlem yönetimi `@Transactional` ile yapılır; `@Async` ile ilgisizdir.
## D
Metodu ayrı bir thread'de asenkron çalıştırır
🤖 Doğru: çağıran beklemeden devam eder, iş arka planda başka thread'de yürür.
## Doğru
D
## Açıklama
**`@Async`**, işaretlenen metodu çağıran thread yerine **ayrı bir thread'de** (bir `TaskExecutor` havuzunda) çalıştırır; çağıran metot dönüşü beklemeden hemen devam eder. Uzun süren, çağıranı bloke etmesi istenmeyen işler için kullanılır: e-posta gönderme, rapor üretme, dış servis çağrısı. Metot `void`, `Future<T>` veya tercihen `CompletableFuture<T>` döndürebilir. Çalışması için iki koşul gerekir: `@EnableAsync` ile özelliğin açılması ve çağrının Spring **proxy'si üzerinden** yapılması. `@Async`'in önbellekleme, işlem veya derlemeyle bir ilgisi yoktur.

# async-2 | Temel
## Soru
`@Scheduled` içinde `fixedRate` ile `fixedDelay` arasındaki fark nedir?
## A
`fixedRate` yalnız cron ifadesiyle, `fixedDelay` yalnız saniye cinsinden çalışır
🤖 İkisi de milisaniye alır; fark ölçüm biriminde değil, süreyi neyin arasında saydıklarında.
## B
İkisi tamamen aynıdır; Spring iki isim sağlar ama davranışları birebir aynıdır
🤖 Davranışları farklıdır: biri başlangıçlar arasını, diğeri bitiş-başlangıç arasını sayar.
## C
`fixedRate` başlangıçtan başlangıca, `fixedDelay` bitişten başlangıca sayar
🤖 Doğru: fixedRate ardışık çalışmaların başlangıçlarını, fixedDelay bitiş ile sonraki başlangıcı ölçer.
## D
`fixedRate` görevi yalnız bir kez, `fixedDelay` ise sonsuz kez çalıştırır
🤖 İkisi de periyodik tekrarlar; tek seferlik çalıştırma anlamına gelmezler.
## Doğru
C
## Açıklama
**`fixedRate`**, ardışık çalıştırmaların **başlangıç anları** arasındaki sabit süredir: görev 100 ms sürse de `fixedRate=1000` ile her saniye başında yeni çalıştırma tetiklenmeye çalışılır (önceki bittiyse). Görev süresi rate'i aşarsa çalıştırmalar sıkışır/kuyruklanır. **`fixedDelay`** ise bir çalıştırmanın **bittiği** an ile bir sonrakinin **başladığı** an arasındaki sabit süredir: `fixedDelay=1000` ile görev bitince 1 saniye beklenir, sonra tekrar başlar — böylece çalıştırmalar asla üst üste binmez, süre ne olursa olsun aralarında sabit boşluk kalır. Takvim tabanlı tetikleme (örn. "her Pazartesi 09:00") için `cron` ifadesi kullanılır. İş süresi değişkense ve örtüşme istenmiyorsa genellikle `fixedDelay` tercih edilir.

# async-3 | Orta
## Soru
Aynı sınıf içindeki bir metot, yine aynı sınıftaki bir `@Async` metodu `this.gonder()` şeklinde çağırıyor ama iş asenkron çalışmıyor. Neden?
## A
`@Async` yalnız `void` dönen metotlarda çalışır; buradaki dönüş tipi yanlıştır
🤖 `@Async` `Future`/`CompletableFuture` de döndürebilir; sorun dönüş tipinde değil.
## B
Kendini-çağırma Spring proxy'sini atladığı için metot senkron çalışır
🤖 Doğru: aynı sınıftan `this.` çağrısı proxy'yi baypas eder, `@Async` etkisiz kalır.
## C
`@Async` metotlarının `private` olması zorunludur; public metotta etkisizdir
🤖 Aksine; `@Async` metotları proxy'nin görebilmesi için public olmalıdır, private değil.
## D
Spring `@Async`'i yalnız controller sınıflarında destekler, servislerde desteklemez
🤖 Böyle bir kısıt yoktur; `@Async` herhangi bir Spring bean'inde çalışır.
## Doğru
B
## Açıklama
Spring, `@Async` (ve `@Transactional`, `@Cacheable`) davranışını **proxy** üzerinden ekler: bean'in etrafına sarılan bir vekil, metot çağrısını yakalayıp işi ayrı thread'e devreder. Ancak bir metot **aynı sınıf içinden** başka bir metodu `this.gonder()` ile çağırırsa, çağrı doğrudan nesnenin kendisine gider ve **proxy'yi atlar** — dolayısıyla asenkronluğu ekleyen sarmalayıcı hiç çalışmaz, metot sıradan senkron çağrı gibi yürür. Bu klasik **self-invocation** tuzağıdır. Çözümler: (1) `@Async` metodu **ayrı bir bean'e** taşıyıp onu enjekte ederek çağırmak (proxy devreye girer); (2) nadiren, `AopContext.currentProxy()` ile proxy'yi elde etmek. Ayrıca `@Async` metotları proxy'nin sarabilmesi için **public** olmalıdır.

# async-4 | Orta
## Soru
Zamanlanmış (`@Scheduled`) görevlerin varsayılan davranışıyla ilgili hangi ifade doğrudur ve üretimde neye dikkat etmek gerekir?
## A
Varsayılan planlayıcı havuzu tek thread'lidir; havuz boyutu ayarlanmalıdır
🤖 Doğru: tek thread'li varsayılan, uzun süren bir görev yüzünden diğerlerini sıraya sokar.
## B
Her zamanlanmış görev kendi ayrı işletim sistemi sürecinde (process) yürütülür
🤖 Ayrı süreç değil; hepsi aynı JVM'de bir thread havuzunda çalışır.
## C
Zamanlanmış görevler için varsayılan olarak sınırsız sayıda thread açılır
🤖 Aksine; varsayılan havuz tek thread'lidir, sınırsız değildir.
## D
Zamanlanmış görevler yalnız uygulama başlangıcında bir kez çalıştırılır
🤖 `@Scheduled` görevleri periyodik tekrarlar; tek seferlik değildir.
## Doğru
A
## Açıklama
Spring'in varsayılan `@Scheduled` planlayıcısı **tek thread'li** bir havuz (`ThreadPoolTaskScheduler` boyutu 1) kullanır. Bu, birden çok zamanlanmış görev varsa onların **aynı thread'de sırayla** çalışması demektir: uzun süren bir görev, aynı anda tetiklenmesi gereken diğerlerini **geciktirir** veya kaçırtır. Üretimde bu kolayca "işim neden zamanında çalışmadı?" sürprizine yol açar. Çözüm: bir `TaskScheduler`/`ThreadPoolTaskScheduler` bean'i tanımlayıp havuz boyutunu artırmak ya da 2026'da I/O ağırlıklı görevler için **sanal thread** tabanlı bir executor kullanmak. Böylece görevler birbirini bloke etmeden paralel yürür.

# async-5 | İleri
## Soru
2026'da I/O ağırlıklı asenkron/zamanlanmış işler için hangi executor seçimi en uygunudur ve neden?
## A
Sabit boyutlu küçük bir platform thread havuzu her iş yükünde en iyi seçimdir
🤖 I/O'da bloke olan az sayıda platform thread eş zamanlılığı sınırlar; en iyi seçim değildir.
## B
Her görev için elle `new Thread(...)` çağırıp doğrudan yeni bir thread yaratmak
🤖 Elle sınırsız platform thread yaratmak bellek/OS kaynağını tüketir ve yönetilemez.
## C
Görevleri senkron çalıştırıp executor'ı hiç devreye sokmamak
🤖 Bu asenkronluğu tümden kaldırır; soru zaten asenkron yürütmeyle ilgilidir.
## D
Sanal thread executor'ı; her göreve ucuz bir sanal thread düşer
🤖 Doğru: `Executors.newVirtualThreadPerTaskExecutor()` bloke I/O görevlerinde platform havuzunu tüketmeden yüksek eş zamanlılık verir.
## Doğru
D
## Açıklama
**Sanal thread'ler** (virtual threads, Project Loom; Java 21+ kararlı), bloke eden **I/O ağırlıklı** görevler için idealdir. Klasik platform thread havuzunda, bir görev I/O'da bloke olunca değerli bir OS thread'ini tutar; havuz küçükse eş zamanlılık boğulur, büyükse bellek/bağlam-değişim maliyeti artar. `Executors.newVirtualThreadPerTaskExecutor()` ise **her göreve ayrı, çok ucuz bir sanal thread** verir; sanal thread bloke olunca altındaki taşıyıcı (carrier) platform thread'i serbest bırakır, böylece az sayıda OS thread'iyle on binlerce eş zamanlı bloke görev taşınabilir. Spring Boot 3.2+ ile `spring.threads.virtual.enabled=true` bunu `@Async`, web istekleri ve zamanlanmış görevler için otomatik etkinleştirir. DİKKAT: **CPU-yoğun** işlerde sanal thread avantaj sağlamaz (iş zaten CPU'yu meşgul eder); orada sınırlı bir platform havuzu daha uygundur.

# async-6 | İleri
## Soru
`void` dönen bir `@Async` metodun içinde bir istisna (exception) fırlarsa ne olur ve nasıl ele alınır?
## A
İstisna otomatik olarak çağırana fırlatılır ve orada try/catch ile yakalanır
🤖 Metot ayrı thread'de çalıştığından istisna çağırana geri dönmez; try/catch onu yakalayamaz.
## B
İstisna çağırana ulaşmaz; `AsyncUncaughtExceptionHandler` ile ele alınır
🤖 Doğru: void async'te istisna kaybolur, handler ile yakalanır; `CompletableFuture` dönerse istisna future içinde taşınır.
## C
Uygulama anında çöker (crash) ve JVM tüm thread'leriyle birlikte kapanır
🤖 İstisna o thread'de sessizce loglanır/yutulur; JVM'i kapatmaz.
## D
İstisna bir sonraki HTTP isteğinin yanıtına iliştirilerek istemciye döner
🤖 Böyle bir mekanizma yoktur; async istisnası HTTP isteğine bağlanmaz.
## Doğru
B
## Açıklama
`@Async` metot **çağıran thread'den bağımsız** bir thread'de çalıştığı için, `void` dönen bir async metotta fırlayan istisna **çağırana geri dönemez** — çağıran zaten dönüşü beklemeden devam etmiştir. Böyle bir istisna varsayılan olarak yalnız loglanır (kolayca gözden kaçar). Yakalamak için bir **`AsyncUncaughtExceptionHandler`** tanımlanır; `AsyncConfigurer.getAsyncUncaughtExceptionHandler()` ezilerek bu handler bağlanır ve tüm `void` async istisnaları oraya düşer. Metot **`CompletableFuture<T>`** döndürüyorsa durum farklıdır: istisna future'ın **tamamlanma durumuna** yerleşir ve çağıran `.exceptionally(...)`, `.handle(...)` veya `get()` (ki `ExecutionException` sarar) ile ele alır. Bu yüzden hata yönetimi önemli async işlerde `void` yerine `CompletableFuture` dönmek genellikle tercih edilir.
