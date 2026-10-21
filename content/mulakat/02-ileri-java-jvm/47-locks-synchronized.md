---
slug: locks-synchronized
baslik: "Açık Kilitler: ReentrantLock, ReadWriteLock, StampedLock"
kademe: İleri
sira: 47
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Ortak bir deponun kapısında tek anahtar olduğunu düşün. `synchronized` bu basit anahtardır: alırsın, içeri girersin, çıkınca anahtar otomatik olarak yerine asılır. Yalın ve güvenlidir. Ama şunları diyemezsin: "beş dakika bekler, anahtar boşalmazsa başka işe geçerim", "sırada en uzun bekleyen önce girsin", "yalnızca bakacaklar aynı anda onar kişi girebilsin", "beklerken vazgeçebileyim". `java.util.concurrent.locks` paketindeki **açık kilitler** (explicit locks) tam olarak bu eksik yetenekleri getirir — karşılığında anahtarı geri asma sorumluluğunu da sana yükleyerek.

### Temel kavramlar

- **Kritik bölge (critical section)**: paylaşılan veriye dokunan, aynı anda tek iş parçacığına açık olması gereken kod parçası.
- **Kilit (lock) ve karşılıklı dışlama (mutual exclusion)**: kritik bölgeye tek girişi sağlayan mekanizma ve verdiği garanti.
- **Örtük kilit (intrinsic lock)**: `synchronized`'ın kullandığı, her nesnenin içindeki monitör. Blok bitince — istisnada bile — JVM **otomatik bırakır**.
- **Açık kilit (explicit lock)**: `lock()` ile alınan, `unlock()` ile SENİN bıraktığın nesne (`ReentrantLock` vb.).
- **Yeniden girişlilik (reentrancy)**: kilidi elinde tutan iş parçacığının aynı kilidi tekrar alabilmesi (bir `synchronized` metot diğerini çağırdığında gerekir). `ReentrantLock` her modda yeniden girişlidir; yeniden girişli olmayan bir kilitte aynı iş parçacığı kendini kilitler.
- **Bloklama (blocking)**: kilit boşalana kadar iş parçacığının durup beklemesi.
- **Çekişme (contention)**: aynı kilidi isteyen iş parçacıklarının çokluğu; arttıkça bekleme ve bağlam değişimi (context switch) maliyeti artar.
- **Açlık (starvation) ve adalet (fairness)**: bir iş parçacığının sürekli atlanıp sıra alamaması; adalet, kilidi gelme sırasına (FIFO) göre dağıtma politikasıdır.
- **Kilitlenme (deadlock)**: iki iş parçacığının birbirinin tuttuğu kilidi beklemesi; kimse ilerleyemez.
- **Koşul değişkeni (Condition)**: "kuyruk doldu" / "kuyruk boşaldı" gibi ayrı bekleme kuyrukları; `await()` ile beklenir, `signal()` ile uyandırılır.
- **Damga (stamp)**: `StampedLock`'un verdiği sürüm fişi; "okumaya başladığımdan beri bir şey değişti mi" sorusunu yanıtlamak için kullanılır.
- **`ReadWriteLock`**: kilidi ikiye ayırır — **okuma kilidi paylaşımlıdır** (birden çok okuyucu aynı anda okur), **yazma kilidi tekildir**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ReadWriteLock: çok okuyucu, tek yazıcı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ReadWriteLock: paylaşımlı okuma, tekil yazma</text>
  <rect x="30" y="52" width="290" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Okuma kilidi (paylaşımlı)</text>
  <text x="175" y="104" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Okuyucu 1  ✓</text>
  <text x="175" y="124" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Okuyucu 2  ✓  (aynı anda)</text>
  <text x="175" y="144" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Okuyucu 3  ✓</text>
  <rect x="350" y="52" width="260" height="120" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="480" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#B4322B" font-family="system-ui">Yazma kilidi (tekil)</text>
  <text x="480" y="106" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Yazıcı  ✓  (yalnız 1)</text>
  <text x="480" y="130" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yazarken hiçbir okuyucu giremez</text>
  <text x="320" y="196" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Okuma-ağırlıklı yükte üretimi artırır; StampedLock iyimser okuma ekler.</text>
</svg>
```

### Nasıl çalışır?

Açık kilidin ilk kuralı bırakma sorumluluğudur. `synchronized`'daki güvenlik ağı burada yoktur; kritik bölge istisna fırlatırsa ve `unlock()` `finally` içinde değilse kilit sonsuza dek tutulur ve onu bekleyen herkes kilitlenir. Desen değişmez:

```java
lock.lock();
try {
    // kritik bölge
} finally {
    lock.unlock();   // istisna olsa bile daima bırakılır
}
```

`lock()` çağrısının `try`'ın DIŞINDA, hemen öncesinde olması da bilinçli bir tercihtir: `lock()` fırlatırsa (nadir) `finally` çalışıp hiç alınmamış kilidi bırakmaya kalkmamalıdır. Derleyici kilitleri izlemez; `finally` yazmamak derleme hatası değil, çalışma zamanında kilit sızıntısıdır.

İkinci yetenek beklemeyi yönetmektir. `lock()` kilit alınana kadar süresiz bloklar. `tryLock()` hemen dener, alamazsa bloklanmadan `false` döner. `tryLock(süre, birim)` verilen süre boyunca dener, süre dolunca vazgeçer. Böylece iş parçacığı "kilidi iki saniyede alamazsam hata döneyim / yeniden deneyeyim / başka yol izleyeyim" diyebilir; sistem donmak yerine zarifçe ilerler. `lockInterruptibly()` ise beklerken iş parçacığının kesilebilmesini (interrupt) sağlar — `synchronized`'da kilit beklemesi kesilemez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="lock, tryLock ve zaman aşımlı tryLock davranışı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kilit alınamıyorsa ne olur?</text>
  <rect x="24" y="46" width="190" height="140" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="119" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">lock()</text>
  <text x="119" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sonsuza dek bekler</text>
  <text x="119" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">(bloklar)</text>
  <text x="119" y="152" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">deadlock riski</text>
  <rect x="226" y="46" width="188" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">tryLock()</text>
  <text x="320" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">hemen dener</text>
  <text x="320" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">alamazsa false</text>
  <text x="320" y="152" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">hiç bloklamaz</text>
  <rect x="426" y="46" width="190" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="521" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">tryLock(süre)</text>
  <text x="521" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">süre boyunca dener</text>
  <text x="521" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">dolunca vazgeçer</text>
  <text x="521" y="152" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">alternatif yol izler</text>
</svg>
```

`lock.newCondition()` tek bir kilide birden çok bekleme kuyruğu bağlar; `synchronized`'ın tek `wait`/`notify` kuyruğunun aksine "dolu" ve "boş" koşullarını ayırıp yalnız ilgili tarafı uyandırabilirsin.

`ReadWriteLock`'un kazancı okumaların birbirini beklememesidir. `synchronized` ve düz `ReentrantLock`, okuma da olsa yazma da olsa aynı anda TEK iş parçacığına izin verir; okumalar birbirini gereksiz yere dışlar. Okuma kilidi paylaşımlı olduğunda — durum değişmediği için bu güvenlidir — çoğunlukla okunan bir önbellek ya da yapılandırmada üretim (throughput) belirgin artar. Kilit yine de alınır; kilitsiz (lock-free) değildir.

`StampedLock` bir adım öteye gider ve **iyimser okuma** (optimistic read) sunar:

```java
long stamp = lock.tryOptimisticRead();   // HİÇ kilit almaz, sürüm damgası verir
int a = x, b = y;                        // önce yerel kopyaya oku
if (!lock.validate(stamp)) {             // arada yazma oldu mu?
    stamp = lock.readLock();             // olduysa gerçek okuma kilidine düş
    try { a = x; b = y; } finally { lock.unlockRead(stamp); }
}
```

`validate` `true` dönerse okuma tutarlıydı ve hiç kilit çekişmesi yaşanmadı — çakışması nadir, okuma ağırlıklı yükte bu `ReadWriteLock`'tan bile hızlıdır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="StampedLock iyimser okuma doğrulama akışı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İyimser okuma: kilitsiz oku, sonra doğrula</text>
  <rect x="20" y="70" width="150" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">stamp =</text>
  <text x="95" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tryOptimisticRead()</text>
  <rect x="200" y="70" width="150" height="56" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="275" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">alanları yerel</text>
  <text x="275" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kopyaya oku</text>
  <rect x="380" y="70" width="150" height="56" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="455" y="102" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">validate(stamp)?</text>
  <path d="M170 98 L198 98" stroke="#5D7789" stroke-width="1.5" marker-end="url(#lock2-ok)"/>
  <path d="M350 98 L378 98" stroke="#5D7789" stroke-width="1.5" marker-end="url(#lock2-ok)"/>
  <rect x="470" y="150" width="150" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="545" y="170" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">true: okuma bedava</text>
  <text x="545" y="186" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">(çekişme yok)</text>
  <rect x="286" y="150" width="164" height="44" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="368" y="170" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">false: araya yazma girdi</text>
  <text x="368" y="186" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">readLock ile yeniden oku</text>
  <path d="M475 126 L545 148" stroke="#25804C" stroke-width="1.4" fill="none" marker-end="url(#lock2-ok)"/>
  <path d="M455 126 L378 148" stroke="#8A2E2E" stroke-width="1.4" fill="none" marker-end="url(#lock2-ok)"/>
  <defs>
    <marker id="lock2-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Mülakatta ne sorulur?

- **Neden `try/finally`?** Açık kilidi JVM bırakmaz; istisnada bile bırakılması garanti olmalıdır. Amaç performans değil, doğruluktur.
- **`tryLock`'un avantajı** alma hızı değil, **alamayınca ne yapıldığıdır**: bloklamayı bırakıp alternatif yola geçmek. Adaleti değiştirmez.
- **Adalet (fairness)**: `new ReentrantLock(true)` kilidi FIFO ile en uzun bekleyene verir, açlığı önler; maliyeti kuyruk yönetimi ve artan bağlam değişimi yüzünden düşen üretim ve artan gecikmedir. Varsayılan adil olmayan mod, kilit boşaldığında o an talep edeni kuyruğun önüne alabilir (barging) — hızlıdır ama sistematik açlık riski taşır. Adalet önceliğe (thread priority) göre değil gelme sırasına bakar ve yeniden girişlilikle ilgisi yoktur.
- **`ReadWriteLock` ne zaman?** Yalnız "okuma çok, yazma az" kalıbında. Yazma sıklaşınca avantaj kaybolur, ek karmaşıklık zarara döner; sürekli okuyucu varken yazan aç kalabilir (writer starvation).
- **`StampedLock` incelikleri**: iyimser okumada değerler tutarsız olabilir, bu yüzden önce yerel kopyaya al, SONRA `validate` et. Yazma her zaman özel (exclusive) kilit alır. **Yeniden girişli DEĞİLDİR**; aynı iş parçacığı kilidi tekrar almaya kalkarsa kendini kilitler. `Condition` da desteklemez. Güçlü ama keskin bir araçtır; ancak ölçülmüş bir okuma darboğazı varsa kullanılır.
- **Seçim ilkesi**: önce `synchronized` — en yalın, otomatik bırakma, en az hata yüzeyi ve modern JVM'de yoğun biçimde optimize. Açık kilide yalnız `synchronized`'ın VEREMEDİĞİ bir yetenek gerektiğinde geç: zaman aşımı/`tryLock`, `lockInterruptibly` ile iptal, adalet, birden çok `Condition`, okuma/yazma ayrımı, iyimser okuma ya da kilidi bir metotta alıp başkasında bırakma (hand-over-hand). Karar "hangisi modern" değil, "hangi ek yeteneğe gerçekten ihtiyacım var" sorusuyla verilir.

### Tek cümlede

Açık kilitler `synchronized`'ın veremediği zaman aşımı, iptal, adalet, okuma/yazma ayrımı ve iyimser okuma yeteneklerini verir; karşılığında `finally`'de `unlock` sorumluluğunu ve daha geniş bir hata yüzeyini üstlenirsin.

# lock-1 | Orta
## Soru
`ReentrantLock` kullanırken kilit neden `try/finally` deseniyle bırakılmalıdır?
## A
`lock()` ile alınan kilidi JVM otomatik bırakmaz; kritik bölge istisna fırlatsa bile `unlock()`'ın çalışması için `finally` gerekir
🤖 Doğru: aksi hâlde istisna durumunda kilit sonsuza dek tutulur (deadlock).
## B
`finally` bloğu JIT tarafından daha iyi optimize edildiği için kilitli bölge hızlanır ve kilit tutma süresi kısalır
🤖 Amaç performans değil; istisna durumunda bile kilidin bırakılma garantisidir.
## C
`ReentrantLock`, `lock()` çağrısının `finally` bloğu içinde yapılmasını zorunlu kılar
🤖 `lock()` her yerde çağrılabilir; kural, `unlock()`'ın `finally`'de olmasıdır.
## D
`finally` yazılmazsa derleyici "unclosed lock" hatası verir ve kod derlenmez
🤖 Derleyici kilitleri izlemez; kod derlenir, sorun çalışma zamanındaki kilit sızıntısıdır.
## Doğru
A
## Açıklama
`synchronized`'da JVM, blok normal veya istisnayla bittiğinde monitörü **otomatik bırakır**. Açık kilitte bu güvenlik ağı yoktur: `lock()` ile aldığın kilidi SEN bırakmalısın. Kritik bölge bir istisna fırlatırsa ve `unlock()` `finally`'de değilse, kilit sonsuza dek tutulur; onu bekleyen tüm iş parçacıkları kilitlenir (deadlock). Bu yüzden standart desen değişmezdir:

```java
lock.lock();
try {
    // kritik bölge
} finally {
    lock.unlock();   // istisna olsa bile daima bırakılır
}
```

`lock()` çağrısının `try`'ın DIŞINDA/hemen öncesinde olması da önemlidir: eğer `lock()` fırlatırsa (nadir), `finally` çalışmamalı ve hiç alınmamış kilidi bırakmaya çalışmamalıyız.

# lock-2 | Orta
## Soru
`ReentrantLock.tryLock(süre, birim)` metodunun `lock()`'a göre avantajı nedir?
## A
Kilidi `lock()`'a göre daha hızlı, yaklaşık iki kat kısa sürede alır
🤖 Böyle bir hız garantisi yok; fark alma hızında değil, alınamayınca ne yapıldığındadır.
## B
Kilit alınana kadar iş parçacığını sonsuza dek bloklar, böylece kilidin mutlaka alınmasını garantiler
🤖 O `lock()`'un davranışıdır; `tryLock` tam da sonsuz beklemeyi önlemek içindir.
## C
Kilitte adil (fair) modu otomatik açarak bekleyenlere sırayla dağıtır
🤖 Adalet yapıcıda (`new ReentrantLock(true)`) belirlenir; `tryLock` bunu değiştirmez.
## D
Kilidi verilen süre içinde alamazsa bloklanmayı bırakıp `false` döner; iş parçacığı alternatif bir yol izleyebilir
🤖 Doğru: zaman aşımlı deneme, yanıt vermeme ve deadlock riskini azaltır.
## Doğru
D
## Açıklama
`lock()` kilit alınana kadar iş parçacığını süresiz bloklar; kilit hiç serbest kalmazsa iş parçacığı sonsuza dek bekler. `tryLock()` ise kilidi hemen almaya çalışır, alamazsa BLOKLANMADAN `false` döner; `tryLock(süre, birim)` verilen süre boyunca dener, süre dolunca vazgeçip `false` döner. Bu, `synchronized`'ın veremediği kritik bir yetenektir: iş parçacığı "kilidi X saniyede alamazsam başka bir şey yapayım / hata döneyim / yeniden deneyeyim" diyebilir. Böylece kaynak kilitlenmelerinde (deadlock) sistem sonsuz bekleyip tümüyle donmak yerine zarif biçimde ilerler. Ayrıca `lockInterruptibly()`, beklerken iş parçacığının kesilebilmesini (interrupt) sağlar — uzun süren bir kilit beklemesini iptal etmek için.

# lock-3 | İleri
## Soru
`new ReentrantLock(true)` (adil / fair mod) ne yapar ve maliyeti nedir?
## A
Kilidi hiç bloklamadan alır; kilit meşgulse çağrı beklemeden geri döner
🤖 Adalet bayrağı bloklama davranışını değiştirmez; yalnızca bekleyenler arasındaki sıralama politikasını belirler.
## B
Kilidi, bekleyen iş parçacıklarına GELME SIRASINA göre (en uzun bekleyen önce) verir; açlığı (starvation) önler ama üretim (throughput) düşer
🤖 Doğru: adalet açlığı engeller ama performanstan ödün verir.
## C
Kilidi öncelik sırasına göre dağıtır: yüksek öncelikli (priority) iş parçacıkları kuyruğun önüne geçer
🤖 Adil mod önceliğe değil, gelme sırasına (FIFO) bakar; hiçbir iş parçacığının ayrıcalığı yoktur.
## D
Kilidin yeniden-girişli (reentrant) olmasını sağlar; `false` verilirse aynı iş parçacığı kilidi tekrar alamaz
🤖 `ReentrantLock` her iki modda da daima yeniden-girişlidir; adalet bundan bağımsız bir seçenektir.
## Doğru
B
## Açıklama
Varsayılan `ReentrantLock` (adil değil) kilidi serbest kaldığında, o an talep eden herhangi bir iş parçacığına verebilir — kuyrukta uzun süredir bekleyen birini atlayabilir (barging). Bu, üretim (throughput) açısından hızlıdır çünkü bağlam değişimi (context switch) azalır, ama bir iş parçacığı sürekli atlanırsa **açlık** (starvation) yaşayabilir. Adil mod (`true`), kilidi **FIFO** sırasıyla, en uzun bekleyene verir; açlığı önler. Maliyeti: her devir teslimde kuyruk yönetimi ve daha fazla bağlam değişimi gerektiğinden genel üretim düşer, gecikme (latency) artar. Pratikte adalet çoğu zaman GEREKMEZ; yalnız belirli bir iş parçacığının sistematik olarak aç kalma riski varsa açılır. Bu, "hız mı, adalet mi" tasarım dengesinin somut bir örneğidir.

# lock-4 | İleri
## Soru
`ReadWriteLock`, okuma-ağırlıklı bir yükte `synchronized`'a göre neden daha iyi ölçeklenir?
## A
Okuma ve yazmayı tek bir kilitte birleştirip kilit sayısını azalttığı için
🤖 Tam tersi; kazanç kilitleri birleştirmekten değil, okuma ile yazmayı AYIRMAKTAN gelir.
## B
Yazma işlemlerini de paralel çalıştırıp birden çok yazıcıya aynı anda izin verdiği için
🤖 Yazma kilidi tekildir; eşzamanlılık yalnızca okumalarda vardır.
## C
Okuma kilidi PAYLAŞIMLIDIR: birden çok okuyucu aynı anda okuyabilir, yalnız yazma tekildir; okuyucular birbirini beklemez
🤖 Doğru: eşzamanlı okuma, okuma-ağırlıklı yükte üretimi artırır.
## D
Kilit almayı tümüyle ortadan kaldırıp kilitsiz (lock-free) çalıştığı için; okuyucular hiçbir kilit almaz
🤖 Kilit hâlâ alınır; yalnızca okuma kilidi paylaşımlıdır. Kilitsiz okumayı `StampedLock`'un iyimser modu sunar.
## Doğru
C
## Açıklama
`synchronized` ve düz `ReentrantLock`, okuma da olsa yazma da olsa aynı anda TEK iş parçacığına izin verir; okumalar birbirini gereksiz yere dışlar. `ReadWriteLock` kilidi ikiye ayırır: **okuma kilidi paylaşımlıdır** — durum değişmediği için birden çok okuyucu aynı anda güvenle okuyabilir; **yazma kilidi tekildir** — yazan varken ne başka yazan ne okuyan girebilir. Okumaların yazmalardan çok baskın olduğu (ör. çoğunlukla okunan bir önbellek/yapılandırma) yüklerde bu, üretimi belirgin artırır çünkü okuyucular artık sıraya girmez. Uyarı: yazma sık ise avantaj kaybolur, hatta ek karmaşıklık yüzünden zarar edebilir; ayrıca okuyucular sürekliyse yazan aç kalabilir (writer starvation). Yani `ReadWriteLock` bir "okuma çok, yazma az" kalıbına özel araçtır, genel amaçlı bir hızlandırıcı değil.

# lock-5 | Uzman
## Soru
`StampedLock`'un "iyimser okuma" (optimistic read) modu nasıl çalışır ve ne kazandırır?
## A
`tryOptimisticRead()` ile HİÇ kilit almadan bir damga (stamp) alınır, alanlar okunur, sonra `validate(stamp)` arada yazma olup olmadığını söyler
🤖 Doğru: iyimser okuma, çakışma yoksa kilitsiz okuma hızını verir.
## B
Okuma süresince yazıcıları bloklayarak tutarlılığı garantiler; bu yüzden `validate` çağrısına gerek kalmaz, damga yalnız kayıt amaçlıdır
🤖 İyimser okuma kimseyi bloklamaz; tam da bu yüzden `validate(stamp)` ile doğrulamak ZORUNLUDUR.
## C
Yazmaları da kilitsiz ve iyimser yapar; çakışan yazmalar otomatik olarak yeniden denenir
🤖 Yazma her zaman özel (exclusive) kilit alır; iyimserlik yalnızca okumaya özgüdür.
## D
`StampedLock` yeniden-girişlidir ve `synchronized` ile birebir aynı garantileri verir, yalnız ismi farklıdır
🤖 `StampedLock` yeniden-girişli DEĞİLDİR; aynı iş parçacığı kilidi tekrar alırsa kendini kilitler.
## Doğru
A
## Açıklama
`StampedLock`, iyimser okumayı şöyle sunar: `long stamp = lock.tryOptimisticRead()` HİÇBİR kilit almaz, sadece bir sürüm damgası verir. Kod paylaşılan alanları yerel değişkenlere okur, sonra `lock.validate(stamp)` ile o damgadan bu yana bir YAZMA olup olmadığını sorar. `validate` `true` dönerse okuma tutarlıydı ve **hiç kilit çekişmesi yaşanmadı** — bu, okuma-ağırlıklı ve çakışması nadir yüklerde `ReadWriteLock`'tan bile hızlıdır. `false` dönerse (araya yazma girmiş) kod, gerçek okuma kilidine (`readLock`) düşüp yeniden okur. Önemli uyarılar: iyimser okuma sırasında okunan değerler tutarsız olabileceğinden önce yerel kopyaya alıp SONRA `validate` etmek gerekir; `StampedLock` **yeniden-girişli değildir** (aynı iş parçacığı tekrar alırsa deadlock), `Condition` desteklemez ve yanlış kullanımı kolaydır. Yani güçlü ama keskin bir araçtır; ancak ölçülmüş bir okuma darboğazı varsa tercih edilir.

# lock-6 | İleri
## Soru
Örtük (`synchronized`) ile açık (`ReentrantLock` vb.) kilit arasında seçim yaparken hangi ilke en doğrudur?
## A
Her zaman `ReentrantLock` kullanılmalı; `synchronized` artık kullanılmamalıdır, yeni kodda hiçbir yeri yoktur
🤖 Genelleme yanlış; `synchronized` çoğu durumda hâlâ en iyi seçimdir.
## B
Açık kilitler her zaman daha hızlıdır, bu yüzden performans ölçmeye gerek kalmadan hep tercih edilmelidir
🤖 Modern JVM'de `synchronized` de çok optimizedir; hız tek başına ölçüt değildir.
## C
`synchronized` iptal edilemez (interruptible değil) ve eski olduğu için her durumda kaçınılmalıdır
🤖 "Eski" değildir; iptal gerekmiyorsa yalınlığı avantajdır.
## D
Önce `synchronized` (en yalın, otomatik bırakma); yalnız `synchronized`'ın veremediği bir yetenek gerektiğinde açık kilitlere geçilmelidir
🤖 Doğru: yeteneğe göre seç; varsayılan yalınlık, gerekince güç.
## Doğru
D
## Açıklama
Doğru ilke "en yalın yeterli aracı seç"tir. `synchronized` avantajları: sözdizimi kısa, kilit blok bitince (istisnada bile) OTOMATİK bırakılır, unutulacak `unlock` yoktur, modern JVM'de yoğun biçimde optimizedir. Bu yüzden **varsayılan tercih `synchronized`** olmalıdır. Açık kilitlere (`ReentrantLock`, `ReadWriteLock`, `StampedLock`) yalnız `synchronized`'ın VEREMEDİĞİ bir yetenek gerektiğinde geçilir: `tryLock`/zaman aşımı, `lockInterruptibly` ile iptal, adalet (fairness) politikası, birden çok `Condition`, okuma/yazma ayrımı, iyimser okuma, veya kilidi bir metotta alıp başkasında bırakma (hand-over-hand). Açık kilit güç verir ama `finally`'de `unlock` sorumluluğu ve artan hata yüzeyi getirir. Yani karar "hangisi modaya uygun" değil, "hangi ek yeteneğe gerçekten ihtiyacım var" sorusuyla verilir.
