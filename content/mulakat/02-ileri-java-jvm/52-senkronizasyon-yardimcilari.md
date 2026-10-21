---
slug: senkronizasyon-yardimcilari
baslik: "Senkronizasyon Yardımcıları: Latch, Barrier, Semaphore"
kademe: İleri
sira: 52
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir okul gezisini düşün. Rehber "beş öğrenci de otobüse binmeden hareket etmiyorum" der — herkesi BİR KEZ bekler. Grup her durakta "hepimiz toplanınca devam" der — aynı buluşma tekrar tekrar yaşanır. Müze girişinde "içeride en fazla üç kişi" tabelası vardır — bir kota. İki öğrenci sandviçlerini takas eder. `java.util.concurrent`'ın senkronizasyon yardımcıları bu kalıpların hazır hâlidir. Çözdükleri problem: thread'lerin birbirini beklemesini elle `wait/notify` ile yazmak zordur ve hataya çok açıktır; bu sınıflar doğru beklemeyi kutudan çıkmış olarak verir.

### Temel kavramlar

- **İş parçacığı (thread)**: Programın aynı anda ilerleyen ayrı akış hatlarından biri.
- **Bloklama (blocking)**: Bir thread'in bir koşul gerçekleşene kadar durup beklemesi; bu sırada CPU harcamaz.
- **Sayaç kapısı (latch)**: Sayaç sıfıra inince BİR KEZ açılan kapı.
- **Buluşma noktası (barrier)**: Belirli sayıda thread gelmeden kimsenin geçemediği nokta.
- **İzin (permit)**: Kaynağa girmek için gereken bilet; bilet sayısı kadar thread aynı anda içeride olabilir.

Beş araç, beş senaryo; mülakatta hangi aracın hangi senaryoya uyduğu sorulur:

- **`CountDownLatch`**: Bir sayaçtan geri sayar. Bir/çok thread `await()` ile bekler; başka thread'ler `countDown()` çağırdıkça sayaç düşer, 0 olunca bekleyenler serbest kalır. **Tek kullanımlıktır** (sıfırlanmaz).
- **`CyclicBarrier`**: Belirli SAYIDA thread bir noktada BULUŞUNCA hepsini birlikte serbest bırakır ve **yeniden kullanılabilir** (döngüsel). İsteğe bağlı bir "barrier action" çalıştırır.
- **`Semaphore`**: Sınırlı sayıda İZİN (permit) yönetir; `acquire`/`release` ile aynı anda en fazla N thread'in bir kaynağa erişmesine izin verir (ör. bağlantı havuzu sınırı).
- **`Phaser`**: Katılımcı ve faz sayısının DİNAMİK değişebildiği, çok-fazlı (multi-phase) senkronizasyon. Katılımcı sayısı çalışma sırasında `register()` ile artar, `arriveAndDeregister()` ile azalır; her thread `arriveAndAwaitAdvance()` çağırarak mevcut fazı bitirip bir sonrakine geçer.
- **`Exchanger`**: İKİ thread bir noktada buluşup veri nesnelerini TAKAS eder.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Senkronizasyon yardımcıları karar tablosu">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi senaryo, hangi araç?</text>
  <rect x="24" y="44" width="290" height="42" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="40" y="70" font-size="12" fill="#1C3247" font-family="system-ui">N iş bitene dek BEKLE (tek sefer) → CountDownLatch</text>
  <rect x="24" y="92" width="290" height="42" rx="8" fill="#E6F0F8" stroke="#1C5A8A" stroke-width="1.3"/>
  <text x="40" y="118" font-size="12" fill="#1C3247" font-family="system-ui">N thread BULUŞ, tekrarla → CyclicBarrier</text>
  <rect x="24" y="140" width="290" height="42" rx="8" fill="#FBF3E6" stroke="#B4772B" stroke-width="1.3"/>
  <text x="40" y="166" font-size="12" fill="#1C3247" font-family="system-ui">En fazla N erişim (kota) → Semaphore</text>
  <rect x="326" y="44" width="290" height="42" rx="8" fill="#F2E9F7" stroke="#6E3B9E" stroke-width="1.3"/>
  <text x="342" y="70" font-size="12" fill="#1C3247" font-family="system-ui">Dinamik katılımcı + çok faz → Phaser</text>
  <rect x="326" y="92" width="290" height="42" rx="8" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.3"/>
  <text x="342" y="118" font-size="12" fill="#1C3247" font-family="system-ui">İki thread veri TAKAS → Exchanger</text>
  <rect x="326" y="140" width="290" height="42" rx="8" fill="#EEF1F3" stroke="#5D7789" stroke-width="1.3"/>
  <text x="342" y="166" font-size="12" fill="#1C3247" font-family="system-ui">Latch: sıfırlanmaz · Barrier: yeniden kullanılır</text>
</svg>
```

### Nasıl çalışır?

Anahtar ayrım: `CountDownLatch` bir GİRİŞ KAPISIDIR ve bir kez açılır; `CyclicBarrier` bir BULUŞMA NOKTASIDIR ve tekrar tekrar kullanılabilir.

```java
CountDownLatch kapi = new CountDownLatch(3);

// bekleyen taraf (ör. ana thread):
kapi.await();       // sayaç 0 olana dek bloklanır

// çalışan taraf (üç ayrı işçi thread'i):
kapi.countDown();   // her çağrı sayacı bir azaltır
```

Sayaç 0'a düştüğünde bekleyen HERKES aynı anda serbest kalır. Sayacı azaltan thread'ler ile bekleyen thread'ler AYRI olabilir — latch asimetriktir; bir taraf iş yapar, öbür taraf yalnız bekler. En kritik özelliği tek kullanımlık olmasıdır: 0'a indikten sonra sıfırlanamaz, sayaç geri de artmaz.

`CyclicBarrier` ise simetriktir: belirlenen SAYIDA thread'in hepsi `await()` diyene kadar herkes bekler, son gelenle birlikte hepsi serbest kalır ve bariyer otomatik SIFIRLANIR. Bu yüzden döngü içinde her turda yeniden kullanılabilir. İsteğe bağlı bir "barrier action" (bariyer eylemi) her tur sonunda bir kez çalıştırılır. Roller sık ters öğrenilir: buluşturan `CyclicBarrier`, geri sayan `CountDownLatch`'tir.

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CountDownLatch tek kullanımlık geri sayım, CyclicBarrier döngüsel buluşma">
  <defs>
    <marker id="sync-a" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Latch: tek yön geri sayım · Barrier: tekrar kullanılır</text>
  <rect x="24" y="36" width="592" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="38" y="56" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">CountDownLatch(3) — sıfırlanmaz</text>
  <circle cx="70" cy="86" r="17" fill="#ffffff" stroke="#186399" stroke-width="1.3"/><text x="70" y="90" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">3</text>
  <line x1="90" y1="86" x2="128" y2="86" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sync-a)"/>
  <circle cx="148" cy="86" r="17" fill="#ffffff" stroke="#186399" stroke-width="1.3"/><text x="148" y="90" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">2</text>
  <line x1="168" y1="86" x2="206" y2="86" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sync-a)"/>
  <circle cx="226" cy="86" r="17" fill="#ffffff" stroke="#186399" stroke-width="1.3"/><text x="226" y="90" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">1</text>
  <line x1="246" y1="86" x2="284" y2="86" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sync-a)"/>
  <circle cx="304" cy="86" r="17" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/><text x="304" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">0</text>
  <text x="342" y="80" font-size="10.5" fill="#5D7789" font-family="system-ui">her countDown() düşürür</text>
  <text x="342" y="99" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">→ kapı bir kez açılır (await serbest)</text>
  <rect x="24" y="126" width="592" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="38" y="146" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">CyclicBarrier(3) — buluş, serbest bırak, sıfırla, tekrarla</text>
  <circle cx="78" cy="178" r="15" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/><text x="78" y="182" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">T1</text>
  <circle cx="118" cy="178" r="15" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/><text x="118" y="182" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">T2</text>
  <circle cx="158" cy="178" r="15" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/><text x="158" y="182" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">T3</text>
  <line x1="184" y1="178" x2="238" y2="178" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sync-a)"/>
  <text x="322" y="174" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">hepsi await() → birlikte geç</text>
  <text x="500" y="182" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">↻ otomatik sıfırlanır → sonraki tur</text>
</svg>
```

`Semaphore` ise bir buluşma değil, sınırlı bir izin sayacıdır. `new Semaphore(N)` N izin tutar; thread kaynağa girmeden `acquire()` ile bir izin alır (izin yoksa bekler), işi bitince `release()` ile geri verir. Böylece aynı anda en fazla N thread erişir — sınırlı bağlantı havuzu, hız sınırlama (rate limiting) ve kota senaryolarının doğrudan aracıdır. Latch'in sayacı geri artmadığı için aynı işi latch ile yapamazsın; barrier ise yalnız buluşturur, girenlerin sayısını sürekli sınırlamaz.

`Phaser`, `CyclicBarrier`'ın esnek akrabasıdır: katılımcı sayısı çalışma sırasında `register()` ile artar, `arriveAndDeregister()` ile azalır (DİNAMİK) ve iş birden çok FAZDAN oluşabilir — her thread `arriveAndAwaitAdvance()` ile mevcut fazı bitirip bir sonrakine geçer. Aşamalı hesaplamalarda (her faz sonunda herkes buluşup devam eder) idealdir. Taraf sayısı sabit ve tek buluşma yetiyorsa `CyclicBarrier` zaten yeter, `Phaser` orada gereksiz karmaşıklıktır. `Phaser` bir `Semaphore` değildir; onun eski/kaldırılmış adı da değildir.

`Exchanger<V>` tam olarak İKİ thread içindir: ikisi de `exchange(nesne)` çağırır, ikisi de bu noktaya gelince nesnelerini TAKAS eder — birinci ikincinin getirdiğini, ikinci birincinin getirdiğini alır. Klasik kullanım tampon (buffer) eşleştirmedir: üretici dolu tamponu verip boş tampon alır, tüketici tersini yapar (pipeline).

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Semaphore izin sayacı, aynı anda en fazla N thread erişir">
  <defs>
    <marker id="sync-in" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#25804C"/></marker>
    <marker id="sync-out" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#B26B00"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Semaphore(3): aynı anda en fazla 3 izin</text>
  <text x="84" y="52" text-anchor="middle" font-size="11" font-weight="600" fill="#5D7789" font-family="system-ui">bekleyenler (izin yok)</text>
  <circle cx="60" cy="96" r="18" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/><text x="60" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">T4</text>
  <circle cx="108" cy="96" r="18" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/><text x="108" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">T5</text>
  <line x1="140" y1="118" x2="212" y2="130" stroke="#25804C" stroke-width="2" marker-end="url(#sync-in)"/>
  <text x="176" y="126" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">acquire()</text>
  <rect x="220" y="52" width="220" height="118" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">kaynak · 3 izin dolu</text>
  <circle cx="270" cy="118" r="20" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/><text x="270" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">T1</text>
  <circle cx="330" cy="118" r="20" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/><text x="330" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">T2</text>
  <circle cx="390" cy="118" r="20" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/><text x="390" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">T3</text>
  <text x="330" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">izin bitti → yeni gelen bekler</text>
  <line x1="448" y1="110" x2="520" y2="98" stroke="#B26B00" stroke-width="2" marker-end="url(#sync-out)"/>
  <text x="484" y="92" text-anchor="middle" font-size="10.5" font-weight="600" fill="#B26B00" font-family="system-ui">release()</text>
  <rect x="500" y="118" width="118" height="52" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="559" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">izin geri döner,</text>
  <text x="559" y="156" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bekleyen biri girer</text>
  <text x="320" y="192" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Bağlantı havuzu / hız sınırlama (rate limit) / kota aracı.</text>
</svg>
```

### Mülakatta ne sorulur?

- **Latch tek-kullanım, barrier tekrar**: latch 0'a inince sıfırlanmaz; barrier her turda otomatik sıfırlanır. Bu ikisini ters söylemek klasik hatadır ve "aynı şeyin yeni adı" da değildirler.
- **Rol farkı**: latch geri sayar (sayan ile bekleyen AYRI thread'ler olabilir), barrier buluşturur (katılan herkes `await` etmelidir).
- **"5 işçi bir kez ısınsın, sonra ana akış başlasın"** → `CountDownLatch(5)`: senaryo tek seferlik ve asimetriktir. `CyclicBarrier(6)` çalışıyormuş gibi görünür ama tekrar yokken gereksiz karmaşıktır ve anlamsal olarak yanlıştır — ana thread iş yapmaz, yalnız bekler. `Semaphore` ile kurmak dolaylı ve hataya açıktır, `Exchanger` iki thread'liktir.
- **Kota / eşzamanlı erişim sınırı** → `Semaphore(N)`; latch, barrier ve exchanger kota kavramı taşımaz.
- **`Phaser`** → katılımcı sayısı dinamik + çok fazlı senkronizasyon. Sabit taraf + tek faz için değil; iki thread'in takası için hiç değil.
- **`Exchanger`** → tam iki thread arasında çift yönlü veri takası; N thread buluşması, izin dağıtımı veya geri sayım değildir.

### Tek cümlede

Latch bir kez açılan geri-sayım kapısı, barrier tekrar tekrar kullanılan buluşma noktası, semaphore eşzamanlı erişim kotası, phaser dinamik ve çok fazlı buluşma, exchanger ise tam iki thread'lik takas noktasıdır.

# sync-1 | Temel
## Soru
`CountDownLatch` temel olarak ne yapar?
## A
Bir sayaçtan geri sayar; sayaç 0'a inene dek `await()` edenleri bekletir ve TEK kullanımlıktır
🤖 Doğru: tek seferlik geri-sayım kapısı, sıfırlanamaz.
## B
Belli sayıda thread bir noktada buluşunca hepsini birlikte serbest bırakır ve yeniden kullanılabilir
🤖 O `CyclicBarrier`; latch buluşturmaz, yalnız geri sayar.
## C
Sınırlı sayıda izinle bir kaynağa aynı anda erişebilecek thread sayısını kısıtlar
🤖 O `Semaphore`; latch izin yönetmez.
## D
İki thread'in bir buluşma noktasında veri nesnelerini karşılıklı takas etmesini sağlar
🤖 O `Exchanger`; latch veri takas etmez.
## Doğru
A
## Açıklama
`CountDownLatch`, bir başlangıç sayısıyla kurulur (ör. `new CountDownLatch(3)`). `await()` çağıran thread(ler), sayaç 0'a düşene dek bloklanır; başka thread'ler her `countDown()` ile sayacı bir azaltır. Sayaç 0 olduğunda bekleyen HERKES aynı anda serbest kalır. En önemli özelliği TEK KULLANIMLIK olmasıdır: 0'a indikten sonra sıfırlanamaz — tekrar tekrar buluşma gerekiyorsa `CyclicBarrier` (B) kullanılır. İzin kotası `Semaphore` (C), veri takası `Exchanger` (D) işidir.

# sync-2 | Orta
## Soru
`CyclicBarrier` ile `CountDownLatch` arasındaki temel fark nedir?
## A
İkisi de aynı işi yapar; `CyclicBarrier` yalnızca `CountDownLatch`'in yeni ve daha okunur adıdır
🤖 Davranışları farklı: biri tekrar kullanılır, diğeri tek seferliktir.
## B
`CyclicBarrier` N thread buluşunca hepsini serbest bırakıp YENİDEN kullanılabilir; latch tek kullanımlıktır
🤖 Doğru: barrier döngüsel/karşılıklı, latch tek seferlik.
## C
`CyclicBarrier` tek kullanımlıktır; `CountDownLatch` ise 0'a inince otomatik sıfırlanıp yeniden kullanılır
🤖 Tam tersi; yeniden kullanılabilir olan `CyclicBarrier`'dır, latch sıfırlanmaz.
## D
`CountDownLatch` thread'leri bir noktada birbiriyle buluşturur; `CyclicBarrier` yalnızca geri sayar
🤖 Roller ters verilmiş; buluşturan `CyclicBarrier`, geri sayan latch'tir.
## Doğru
B
## Açıklama
`CyclicBarrier`, belirlenen SAYIDA thread'in hepsi bariyere gelene (`await()`) kadar herkesi bekletir; son thread geldiğinde HEP BİRLİKTE serbest kalırlar ve bariyer otomatik SIFIRLANIR — bu yüzden döngülerde (her turda thread'leri senkronlamak için) tekrar tekrar kullanılabilir. `CountDownLatch` ise farklı bir modeldir: sayacı azaltan thread'ler ile bekleyen thread'ler AYRI olabilir, 0'a inince kapı bir kez açılır ve sıfırlanmaz. Kısaca: karşılıklı buluşma ve tekrar → `CyclicBarrier`; tek seferlik "hazır olana kadar bekle" kapısı → `CountDownLatch`.

# sync-3 | Orta
## Soru
Aynı anda en fazla N thread'in bir kaynağa (ör. sınırlı sayıda DB bağlantısı) erişmesini istiyorsun. Hangi araç uygundur?
## A
`CountDownLatch(N)` — her giren thread sayacı düşürsün, sayaç 0 olunca erişim kapansın
🤖 Latch tek seferlik bekleme kapısıdır; sayaç geri artmaz, sürekli kota sağlamaz.
## B
`CyclicBarrier(N)` — N thread bariyerde buluşana kadar beklesin, sonra birlikte kaynağa girsinler
🤖 Barrier buluşma içindir; girenlerin sayısını sürekli sınırlamaz.
## C
`Exchanger` — thread'ler kaynağa erişim hakkını birbirleriyle takas ederek sırayla girsinler
🤖 Exchanger yalnız iki thread'in veri takası içindir; kota kavramı yoktur.
## D
`Semaphore(N)` — `acquire()` ile izin al, `release()` ile bırak; aynı anda en fazla N thread girer
🤖 Doğru: sınırlı izinli eşzamanlı erişim tam olarak semaphore işidir.
## Doğru
D
## Açıklama
`Semaphore`, belirli sayıda İZİN (permit) tutar. Bir thread kaynağa girmeden `acquire()` ile bir izin alır (izin yoksa bekler), işi bitince `release()` ile geri verir. `new Semaphore(N)` ile aynı anda en fazla N thread'in kaynağa erişmesini garanti edersin — sınırlı bağlantı havuzu, hız sınırlama (rate limiting) veya kota gibi senaryoların doğrudan aracıdır. `CountDownLatch` (A) tek seferlik bir bekleme kapısı, `CyclicBarrier` (B) buluşma noktası, `Exchanger` (C) iki-thread veri takası içindir; hiçbiri "en fazla N eşzamanlı erişim" kısıtı sağlamaz.

# sync-4 | İleri
## Soru
`Phaser`'ı `CyclicBarrier`'a tercih etmen gereken durum hangisidir?
## A
Taraf sayısı sabit ve tek bir buluşma noktası yeterli olduğunda; `Phaser` bu durumda daha hızlı çalışır
🤖 Sabit taraf + tek faz için `CyclicBarrier` zaten yeterlidir; `Phaser` gereksiz karmaşıklık katar.
## B
Yalnızca tam iki thread'in buluşup veri takas etmesi gerektiğinde `Phaser` zorunlu araçtır
🤖 İki thread'in takası `Exchanger` işidir; `Phaser`'ın amacı bu değildir.
## C
Katılımcı sayısının çalışma sırasında DİNAMİK değiştiği ve birden çok FAZ boyunca senkronizasyon gerektiğinde
🤖 Doğru: dinamik katılımcı + çok-faz senaryosu `Phaser`'ın alanıdır.
## D
`Phaser`, `Semaphore`'un kullanımdan kaldırılmış eski adıdır; yeni kodda `Phaser` yazılmalıdır
🤖 Farklı sınıflardır; `Phaser` faz senkronizasyonu yapar, `Semaphore` izin yönetir.
## Doğru
C
## Açıklama
`CyclicBarrier`, taraf sayısı SABİT olan tek bir buluşma noktası için uygundur. `Phaser` ise daha esnektir: çalışma sırasında `register()`/`arriveAndDeregister()` ile katılımcı sayısı DİNAMİK değişebilir ve iş birden çok FAZDAN oluşabilir — her thread `arriveAndAwaitAdvance()` ile mevcut fazı bitirip bir sonrakine geçebilir. Bu, aşamalı hesaplamalarda (her faz sonunda herkesin buluşup sonra devam etmesi) ve katılımcıların dinamik olarak eklenip çıktığı senaryolarda idealdir. Sabit taraf + tek faz için `CyclicBarrier` (A) fazlasıyla yeter; `Phaser` bir `Semaphore` (D) değildir.

# sync-5 | İleri
## Soru
`Exchanger` hangi senaryo için tasarlanmıştır?
## A
İKİ thread bir buluşma noktasında (`exchange`) veri nesnelerini karşılıklı TAKAS eder
🤖 Doğru: çift yönlü, tam iki-thread'lik veri takası.
## B
N thread'i bir noktada bekletip son gelen geldiğinde hepsini birlikte serbest bırakır
🤖 O `CyclicBarrier`; `Exchanger` yalnız iki thread arasında çalışır.
## C
Sınırlı sayıda izin dağıtarak bir kaynağa eşzamanlı erişen thread sayısını kısıtlar
🤖 O `Semaphore`; `Exchanger` izin yönetmez.
## D
Bir sayaçtan geri sayıp sayaç sıfırlanınca bekleyen tüm thread'leri serbest bırakır
🤖 O `CountDownLatch`; `Exchanger` geri sayım yapmaz.
## Doğru
A
## Açıklama
`Exchanger<V>`, tam olarak İKİ thread'in bir buluşma noktasında karşılıklı veri değiş-tokuşu yapması için tasarlanmıştır. İki thread de `exchange(nesne)` çağırır; ikisi de bu noktaya gelince nesnelerini TAKAS eder — birinci thread ikincinin getirdiğini, ikinci birincinin getirdiğini alır. Klasik kullanım: bir üretici dolu bir tampon (buffer) verip boş bir tampon alırken, tüketici boş tamponu verip dolu tamponu alır (tampon eşleştirme / pipeline). N thread buluşması `CyclicBarrier` (B), kota `Semaphore` (C), geri sayım `CountDownLatch` (D) işidir; `Exchanger` bunların hiçbirini yapmaz, yalnız iki-thread takasına odaklanır.

# sync-6 | Uzman
## Soru
Bir servis başlarken, 5 bağımsız işçinin (worker) BİR KEZ ısınmasını/tamamlanmasını bekleyip ondan sonra ana akışı başlatmak istiyorsun. En yalın ve doğru araç hangisidir?
## A
`Semaphore(5)` — beş izin tanımlayıp ana thread'in beşini birden `acquire` etmesini beklemek
🤖 Semaphore izin kotası içindir; "N iş bitti mi" beklemesi için dolaylı ve hataya açıktır.
## B
`CountDownLatch(5)` — ana thread `await()` eder, her işçi bitince `countDown()` çağırır
🤖 Doğru: tek seferlik "N iş bitene kadar bekle" tam olarak latch senaryosudur.
## C
`Exchanger` — her işçi ana thread ile sırayla buluşup "bittim" nesnesini takas etsin
🤖 Exchanger yalnız iki thread'lidir; 5 işçiyi tek noktada beklemek için uygun değildir.
## D
`CyclicBarrier(6)` — ana thread ve beş işçi aynı bariyerde `await()` çağırıp buluşsun
🤖 Bariyer karşılıklı buluşma/tekrar içindir; tek seferlik bekleme için latch daha yalın.
## Doğru
B
## Açıklama
Senaryo tek seferliktir ve asimetriktir: BİR ana thread, N işçinin BİR KEZ bitmesini bekliyor. Bu tam olarak `CountDownLatch(5)`'in tasarım amacıdır — ana thread `await()` ile bekler, her işçi kendi işini bitirince `countDown()` çağırır, sayaç 0 olunca kapı açılır. `CyclicBarrier` (D) karşılıklı BULUŞMA ve TEKRAR için tasarlanmıştır; burada tekrar yok ve ana thread iş yapmıyor, sadece bekliyor — bariyer hem gereksiz karmaşık hem de anlamsal olarak yanlış (herkesin `await` etmesini ister). `Semaphore` (A) izin kotası, `Exchanger` (C) iki-thread takası içindir. En yalın ve doğrudan çözüm `CountDownLatch`'tir.
