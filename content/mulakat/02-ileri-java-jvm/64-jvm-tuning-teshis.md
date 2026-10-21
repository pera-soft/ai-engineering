---
slug: jvm-tuning-teshis
baslik: "JVM Ayarlama ve Teşhis Araçları"
kademe: İleri
sira: 64
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Arabanız garip ses çıkarıyorsa iyi bir usta kaputu açıp rastgele parça değiştirmez; önce ölçer, dinler, arıza kodunu okur. Üretimdeki bir Java uygulaması yavaşladığında, takıldığında ya da belleği şiştiğinde de aynısı geçerlidir. JVM, içinde ne olup bittiğini anlatan hazır ölçü aletleriyle gelir. İş, doğru aracı doğru soruya bağlamaktır — "sanırım GC sorunu" demek yerine logu açıp bakmaktır.

### Temel kavramlar

- **Bayrak (flag)**: JVM'i başlatırken verilen ayar seçeneği (`-Xmx4g` gibi). Kod değişmeden davranışı değiştirir.
- **Yığın (heap)**: nesnelerin yaşadığı bellek; `-Xms` başlangıç boyutu, `-Xmx` maksimum boyuttur.
- **Duraklama (pause / stop-the-world)**: GC'nin uygulamanın tüm iş parçacıklarını dondurduğu an — kullanıcı bunu takılma olarak görür.
- **Canlı taban (live set)**: bir toplama BİTTİKTEN sonra hâlâ hayatta olan nesnelerin toplam boyutu. Sağlıklı uygulamada yatay seyreder.
- **Profil (profiling)**: programın zamanı/belleği NEREDE harcadığını ölçmek. Modern profilciler **örnekleme (sampling)** yapar: belirli aralıklarla "şu an hangi metottasın?" diye bakar, sonucu istatistiğe döker.
- **Ek yük (overhead)**: ölçüm aracının uygulamaya bindirdiği ek maliyet. Bir aracın üretimde açık bırakılıp bırakılamayacağını bu belirler.
- **Thread dump (iş parçacığı dökümü)**: o anda tüm iş parçacıklarının nerede olduğunun anlık fotoğrafı.
- **Heap dump (yığın dökümü)**: yığındaki tüm nesnelerin ve aralarındaki referansların diske yazılmış tam kopyası.
- **Alev grafiği (flame graph)**: hangi çağrı yolunun ne kadar CPU yediğini gösteren görselleştirme.
- **Tuttuğu boyut (retained size)**: bir nesne serbest kalırsa GC'nin geri kazanacağı toplam bellek.

### Nasıl çalışır?

Her araç FARKLI bir soruyu yanıtlar:

- **Yığın bayrakları**: `-Xms` başlangıç, `-Xmx` maksimum yığın boyutu. İkisini **eşit** vermek (ör. `-Xms4g -Xmx4g`), çalışma sırasında yığın yeniden boyutlandırmasını ve buna bağlı duraklamaları önler — konteyner/servis dünyasında yaygın pratik.
- **GC logları**: `-Xlog:gc*` ile açılır; duraklama süreleri, toplama sıklığı ve yığın doluluk eğilimini gösterir. "Sızıntı var mı?" sorusunun ilk durağıdır — her toplama sonrası **live set** sürekli yükseliyorsa sızıntı sinyalidir.
- **`jstack`**: iş parçacığı yığın izlerini (thread dump) döker — kilitlenme (deadlock), takılan iş parçacığı, sıcak döngü teşhisi.
- **`jmap`**: heap özeti, sınıf histogramı ve **heap dump** üretimi. (Kaza anını yakalamak için `-XX:+HeapDumpOnOutOfMemoryError` bayrağı da eklenir: OOM anında otomatik dump alınır.)
- **`jstat`**: çalışan JVM'in GC ve sınıf yükleme sayaçlarını periyodik basar (ör. `jstat -gcutil <pid> 1s`) — eğilim izlemek için ucuzdur, ama dump alamaz.
- **`jcmd`**: tek kapıdan çok komut (thread dump, heap dump, GC tetikleme, sınıf histogramı, JFR başlat/durdur). Modern pratikte `jstack`/`jmap` yerine sıkça tercih edilir.
- **JFR (Java Flight Recorder)**: JVM'e gömülü, **çok düşük ek yükle** sürekli çalışabilen olay kaydedicisi — ayırma (allocation), GC, kilit çekişmesi, CPU örnekleme, IO. Üretimde açık bırakılabilecek kadar ucuzdur; `.jfr` dosyası JDK Mission Control ile incelenir.
- **async-profiler**: örnekleme temelli CPU ve ayırma profilcisi; **alev grafiği** üretir. Düşük ek yüklüdür ve safepoint yanlılığından kaçınır.
- **Heap dump analizi (MAT)**: Eclipse Memory Analyzer'da **hâkim ağacı (dominator tree)** — bir nesne serbest bırakılırsa ne kadar bellek geri gelir? Sızıntının KÖKÜNÜ bulmanın en hızlı yolu.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teşhis araçlarını sorulara eşleme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Soru → Araç eşlemesi</text>
  <rect x="24" y="46" width="285" height="52" rx="10" fill="#EAF0F6" stroke="#2C5A82" stroke-width="1.4"/>
  <text x="40" y="68" font-size="11.5" font-weight="700" fill="#2C5A82" font-family="system-ui">İş parçacığı takıldı / deadlock?</text>
  <text x="40" y="88" font-size="11" fill="#1C3247" font-family="system-ui">→ jstack / jcmd Thread.print</text>
  <rect x="331" y="46" width="285" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="347" y="68" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">CPU/ayırma nerede yanıyor?</text>
  <text x="347" y="88" font-size="11" fill="#1C3247" font-family="system-ui">→ JFR / async-profiler (flame graph)</text>
  <rect x="24" y="110" width="285" height="52" rx="10" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.4"/>
  <text x="40" y="132" font-size="11.5" font-weight="700" fill="#B5701B" font-family="system-ui">Bellek sızıntısının kökü?</text>
  <text x="40" y="152" font-size="11" fill="#1C3247" font-family="system-ui">→ heap dump + MAT dominator tree</text>
  <rect x="331" y="110" width="285" height="52" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.4"/>
  <text x="347" y="132" font-size="11.5" font-weight="700" fill="#B4322B" font-family="system-ui">GC çok mu duraklatıyor?</text>
  <text x="347" y="152" font-size="11" fill="#1C3247" font-family="system-ui">→ -Xlog:gc* log analizi</text>
  <text x="320" y="196" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">jcmd tek kapıdan hepsini tetikler; heap boyutu için -Xms=-Xmx.</text>
</svg>
```

Pratik akış: önce **çıktıya bak** (GC logu, JFR kaydı), soruyu daralt, ANCAK SONRA bayrak/kod değiştir.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="-Xms ve -Xmx eşit verince yığın yeniden boyutlandırma duraklamaları ortadan kalkar">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yığın boyutlandırma: farklı vs eşit -Xms/-Xmx</text>
  <text x="60" y="48" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">-Xms1g -Xmx4g (farklı): çalışırken büyür</text>
  <rect x="60" y="94" width="80" height="24" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="150" y="82" width="80" height="36" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="240" y="68" width="80" height="50" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <rect x="330" y="52" width="80" height="66" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <line x1="60" y1="118" x2="410" y2="118" stroke="#5D7789" stroke-width="1.2"/>
  <text x="490" y="82" font-size="10.5" fill="#8A2E2E" font-family="system-ui">her büyüme adımı</text>
  <text x="490" y="100" font-size="10.5" fill="#8A2E2E" font-family="system-ui">= STW duraklama</text>
  <text x="490" y="118" font-size="10.5" fill="#5D7789" font-family="system-ui">öngörülemez</text>
  <text x="60" y="158" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">-Xms4g -Xmx4g (eşit): baştan tam ayrılır</text>
  <rect x="60" y="172" width="350" height="40" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="235" y="197" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sabit 4g — resize YOK</text>
  <text x="490" y="190" font-size="10.5" fill="#25804C" font-family="system-ui">tek ayırma,</text>
  <text x="490" y="208" font-size="10.5" fill="#25804C" font-family="system-ui">öngörülebilir</text>
</svg>
```

### Mülakatta ne sorulur?

**`-Xms` = `-Xmx` neden?** İkisi farklıysa JVM yığını çalışırken büyütür/küçültür; bu, işletim sistemiyle konuşma ve ek GC işi getirir, gecikmeyi öngörülemez kılar. Eşit vermek yığını baştan tam ayırır ve davranışı öngörülebilir yapar. Bu bayrak GC'yi KAPATMAZ, iş parçacığı sayısıyla hiçbir ilgisi yoktur (`-Xmx4g` "4 thread" demek değildir) ve uygulamanın çöp üretmesini değiştirmez — o uygulamanın işidir.

**Sızıntının GC logundaki imzası.** Belirleyici sinyal, her toplama SONRASI **canlı tabanın sürekli ve kalıcı yükselmesidir**; testere dişi grafiğin alt zarfı tırmanır ve sonunda OOM gelir. Toplamaların sıklaşması tek başına kanıt değildir — yüksek ayırma hızı da aynı görüntüyü verir ama tabanı yükseltmez. Duraklama sürelerinin sabit kalması sağlık işaretidir, sızıntı sinyali değil; log dosyasının hızla büyümesi ise yalnızca ayrıntı düzeyini gösterir.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GC log: sağlıklı sabit taban vs sızıntıda tırmanan taban">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">GC log: toplama sonrası canlı taban (live set) eğilimi</text>
  <line x1="60" y1="52" x2="60" y2="190" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="60" y1="190" x2="305" y2="190" stroke="#5D7789" stroke-width="1.4"/>
  <polyline points="60,158 98,104 102,158 140,104 144,158 182,104 186,158 224,104 228,158 266,104 270,158 300,110" fill="none" stroke="#25804C" stroke-width="2"/>
  <line x1="60" y1="158" x2="300" y2="158" stroke="#25804C" stroke-width="1" stroke-dasharray="5 4"/>
  <text x="182" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Sağlıklı: taban sabit</text>
  <text x="182" y="228" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her GC tabana döner</text>
  <line x1="360" y1="52" x2="360" y2="190" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="360" y1="190" x2="605" y2="190" stroke="#5D7789" stroke-width="1.4"/>
  <polyline points="360,168 398,118 402,160 440,108 444,150 482,96 486,138 524,86 528,126 566,76 570,120 600,74" fill="none" stroke="#8A2E2E" stroke-width="2"/>
  <line x1="360" y1="168" x2="600" y2="120" stroke="#8A2E2E" stroke-width="1" stroke-dasharray="5 4"/>
  <text x="595" y="66" text-anchor="end" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">OOM →</text>
  <text x="482" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Sızıntı: taban tırmanır</text>
  <text x="482" y="228" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">serbest kalmayan nesneler birikir</text>
</svg>
```

**Üretimde CPU nerede yanıyor?** Yanıt **örnekleme temelli profillemedir**: JFR veya async-profiler. İkisi de üretimde açık bırakılabilecek kadar ucuzdur ve sıcak çağrı yollarını görselleştirir. Hata ayıklayıcıda her satırda durup adım adım izlemek uygulamayı felç eder; her metoda elle `System.out.println` serpiştirmek ölçeklemez ve ölçümü çarpıtır. Heap dump + MAT doğru araçtır ama **bellek** sorunu için, CPU sıcak noktası için değil.

**"Tek araç" denince beklenen yanıt `jcmd`.** `jcmd <pid> Thread.print` (thread dump), `GC.heap_dump` (heap dump), `GC.run` (GC tetikleme), `GC.class_histogram` (sınıf histogramı), `JFR.start`/`JFR.stop` (Flight Recorder kontrolü) — hepsi tek araçtan. Diğerleri gerçek tanılama araçlarıdır ama her biri TEK iş görür: `jstack` sadece thread dump; `jmap` sadece heap dump/histogram, thread dump alamaz ve JFR başlatamaz; `jstat` sadece GC/sınıf yükleme sayaçlarının periyodik raporu.

**JFR neden üretimde açık bırakılabilir?** Çünkü JVM'e **gömülüdür** ve **çok düşük ek yükle** olay toplar — tipik ayarlarda etkisi yüzde birkaçın altındadır. Kod derlemez (o JIT'in işi), uygulamayı olay başına durdurup senkronize etmez (tam zıddı) ve mimariden bağımsızdır; "yalnız 32-bit JVM'lerde çalışır" iddiası yanlıştır.

**Dominator tree neyi yanıtlar?** Bir X nesnesi, Y'ye giden TÜM yollar X'ten geçiyorsa Y'ye "hâkimdir". MAT bu ilişkiden her nesnenin **tuttuğu boyutunu (retained size)** hesaplar: "bunu serbest bıraksam GC ne kadar bellek geri kazanır?" Böylece küçük görünen ama arkasında dev bir nesne grafiği tutan sızıntı kökleri tepede belirir. Deadlock (thread dump), CPU sıcak noktası (flame graph) ya da başlangıç süresi soruları değildir — dominator tree tamamen **bellek tutulumu (retention)** hakkındadır.

### Tek cümlede

JVM teşhisi tahmin değil gözlemdir: soruyu belirle (takılma mı, CPU mu, bellek mi, GC mi), ona uygun aracı seç (`jstack`/`jcmd`, JFR/async-profiler, heap dump + MAT, `-Xlog:gc*`), çıktıyı oku ve ancak ondan sonra bayrak veya kod değiştir.

# tune-1 | İleri
## Soru
`-Xms` ve `-Xmx` bayraklarını eşit vermenin (ör. `-Xms4g -Xmx4g`) tipik gerekçesi nedir?
## A
Yığın sabitlendiği için çöp toplamanın tümden devre dışı kalmasını sağlamak
🤖 Yığın boyutu GC'yi kapatmaz; yalnız yeniden boyutlandırmayı engeller, toplama sürer.
## B
Uygulamanın kullanabileceği iş parçacığı sayısını 4 ile sınırlayarak çekişmeyi azaltmak
🤖 `-Xmx` bellek sınırıdır; iş parçacığı sayısıyla hiçbir ilgisi yoktur.
## C
Uygulamanın çöp üretmesini engelleyip GC duraklamalarını sıfıra indirmek
🤖 Çöp üretimi uygulama davranışıdır; yığın bayrağı bunu değiştirmez.
## D
Çalışma sırasında yığının yeniden boyutlandırılmasını ve buna bağlı maliyeti önlemek
🤖 Doğru: min=max, dinamik büyüme/küçülme adımlarını ve öngörülemezliği ortadan kaldırır.
## Doğru
D
## Açıklama
`-Xms` başlangıç, `-Xmx` maksimum yığın boyutudur. İkisi FARKLI olursa JVM, ihtiyaca göre yığını çalışma sırasında büyütür/küçültür; bu yeniden boyutlandırma işletim sistemiyle konuşma ve ek GC işi getirir, gecikmeyi öngörülemez kılar. **İkisini eşit** vermek yığını baştan tam ayırır, dinamik boyutlandırmayı ortadan kaldırır ve davranışı **öngörülebilir** yapar — konteyner/servis dünyasında yaygın bir pratiktir. GC'yi kapatmaz (A), iş parçacığı sayısıyla ilgisi yoktur (B) ve çöp üretimini değiştirmez (C — o uygulamanın işidir).

# tune-2 | İleri
## Soru
Üretimde bir bellek sızıntısından şüphelenildiğinde, GC logunda (`-Xlog:gc*`) aranacak en güçlü sinyal nedir?
## A
Toplam GC sayısının artması; toplamaların giderek sıklaşması
🤖 Sıklaşma tek başına yüksek ayırma hızını da gösterebilir; belirleyici sinyal TABANDIR.
## B
Duraklama sürelerinin her toplamada hemen hemen aynı kalması
🤖 Sabit duraklama süresi sağlık işareti sayılabilir; sızıntı sinyali değildir.
## C
Her toplama SONRASI canlı nesne tabanının (live set) sürekli ve kalıcı yükselmesi
🤖 Doğru: full GC'lerden sonra bile taban düşmüyor, sürekli tırmanıyorsa sızıntı sinyalidir.
## D
GC logunun çok satır içermesi ve dosyanın hızla büyümesi
🤖 Satır sayısı log ayrıntı düzeyidir; sızıntı hakkında bilgi vermez.
## Doğru
C
## Açıklama
Bellek sızıntısının GC logundaki imzası, **toplama sonrası canlı taban (live set) eğilimidir**. Sağlıklı bir uygulamada her (özellikle full) GC'den sonra kullanılan yığın belli bir tabana geri döner; bu taban zaman içinde yatay seyreder. Sızıntıda ise, GC ne kadar çalışırsa çalışsın **serbest bırakılamayan** nesneler biriktiği için bu taban **sürekli ve geri dönüşsüz yükselir** — testere dişi grafiğin alt zarfı tırmanır. Bu, en sonunda OutOfMemoryError ile biter. Toplamaların sıklaşması (A) tek başına yeterli kanıt değildir — yüksek ayırma hızı da aynı görüntüyü verir ama tabanı yükseltmez; sabit duraklama (B) veya log satır sayısı (D) ise sızıntı hakkında hiçbir şey söylemez. Belirleyici olan **artan live set**'tir.

# tune-3 | İleri
## Soru
Çalışan bir üretim JVM'inde CPU'nun nerede yandığını **düşük ek yükle** bulmak için en uygun araç hangisidir?
## A
JFR (Java Flight Recorder) veya async-profiler — örnekleme temelli profilciler
🤖 Doğru: ikisi de üretimde açık bırakılabilecek kadar ucuz ve CPU sıcak noktasını gösterir.
## B
Uygulamayı hata ayıklayıcıda (debugger) her satırda durdurup adım adım izlemek
🤖 Adım adım debug üretimde uygulamayı felç eder; profil için uygun değildir.
## C
Her metodun başına ve sonuna `System.out.println` ile zaman damgası basmak
🤖 Elle log serpiştirme ölçeklemez, ölçümü bozar ve sıcak noktayı güvenilir göstermez.
## D
Heap dump alıp MAT'ta en çok bellek tutan nesneleri incelemek
🤖 Heap dump BELLEK sorununu gösterir; CPU sıcak noktası için doğru araç değildir.
## Doğru
A
## Açıklama
"CPU nerede harcanıyor?" sorusunun üretim-güvenli yanıtı **örnekleme temelli profillemedir**. **JFR**, JVM'e gömülü, çok düşük ek yükle sürekli çalışabilen bir kaydedicidir; **async-profiler** ise safepoint yanlılığından kaçınan, CPU ve ayırma için **alev grafiği** üreten harici bir profilcidir. İkisi de üretimde açık bırakılabilecek kadar ucuzdur ve sıcak çağrı yollarını görselleştirir. Adım-adım debug (B) uygulamayı durdurur, elle `println` (C) hem ölçeklemez hem ölçümü çarpıtır. Heap dump + MAT (D) doğru araçtır ama **bellek** sorunu için — CPU sıcak noktası için değil. Araç-soru eşlemesi teşhisin özüdür.

# tune-4 | İleri
## Soru
Modern JDK'de tek bir araçla thread dump, heap dump, GC tetikleme ve JFR başlatmayı yapabilen komut hangisidir?
## A
`jstack` — çalışan JVM'e bağlanıp tanılama çıktısı üretir
🤖 `jstack` yalnız thread dump verir; heap dump, GC tetikleme ve JFR onun kapsamında değil.
## B
`jcmd` — çalışan JVM'e tek kapıdan çok sayıda tanılama komutu gönderir
🤖 Doğru: jcmd thread/heap dump, GC, sınıf histogramı ve JFR kontrolünü tek araçta toplar.
## C
`jmap` — çalışan JVM'in bellek durumunu inceler
🤖 `jmap` heap dump/histogram verir ama thread dump alamaz, JFR başlatamaz.
## D
`jstat` — çalışan JVM'in sayaçlarını periyodik olarak raporlar
🤖 `jstat` yalnız GC/sınıf yükleme istatistiği basar; dump veya JFR üretmez.
## Doğru
B
## Açıklama
**`jcmd`**, modern JDK'nin "İsviçre çakısı" tanılama aracıdır: çalışan bir JVM sürecine PID üzerinden komut gönderir. `jcmd <pid> Thread.print` (thread dump), `GC.heap_dump` (heap dump), `GC.run` (GC tetikleme), `GC.class_histogram` (sınıf histogramı) ve `JFR.start`/`JFR.stop` (Flight Recorder kontrolü) hepsi tek araçtan yapılır. Diğer seçenekler gerçek tanılama araçlarıdır ama her biri TEK bir işi görür: `jstack` (A) sadece thread dump, `jmap` (C) sadece heap dump/histogram, `jstat` (D) sadece GC ve sınıf yükleme sayaçlarının periyodik raporu. Hiçbiri dördünü birden yapamaz. Bu yüzden modern pratikte `jstack`/`jmap` yerine `jcmd` tercih edilir; mülakatta "tek araç" denince beklenen yanıt `jcmd`'dir.

# tune-5 | İleri
## Soru
Eclipse Memory Analyzer'da (MAT) "hâkim ağacı (dominator tree)" hangi soruyu yanıtlar?
## A
Hangi iş parçacıkları kilit bekliyor, aralarında bir deadlock var mı?
🤖 Deadlock thread dump sorusudur; heap dump/dominator tree bellek içindir.
## B
Kod hangi satırda en çok CPU harcıyor, sıcak çağrı yolu hangisi?
🤖 CPU profili flame graph işidir; dominator tree belleği ölçer.
## C
Bir nesne serbest bırakılırsa ne kadar bellek geri gelir — "tuttuğu boyut (retained size)" nedir?
🤖 Doğru: dominator tree, hangi kökün ne kadar belleği canlı tuttuğunu gösterir.
## D
Uygulamanın başlangıç süresi ne kadar, hangi ilklendirme adımı yavaş?
🤖 Başlangıç süresi profil/log konusudur; dominator tree ile ölçülmez.
## Doğru
C
## Açıklama
**Hâkim ağacı (dominator tree)**, heap dump analizinde sızıntının KÖKÜNÜ bulmanın en hızlı yoludur. Bir X nesnesi, Y'ye giden TÜM yollar X'ten geçiyorsa Y'ye "hâkimdir". MAT bu ilişkiyi kullanıp her nesnenin **tuttuğu boyutu (retained size)** hesaplar: "bu nesneyi serbest bıraksam, GC ne kadar bellek geri kazanır?" Böylece küçük görünen ama arkasında dev bir nesne grafiği tutan "sızıntı kökleri" tepede belirir. Bu bir CPU (B), deadlock (A) veya başlangıç süresi (D) sorusu değildir — dominator tree tamamen **bellek tutulumu (retention)** hakkındadır ve leak avının merkezidir.

# tune-6 | İleri
## Soru
JFR'nin (Java Flight Recorder) üretimde sürekli açık bırakılabilmesinin temel nedeni nedir?
## A
Uygulama kodunu otomatik olarak yeniden derlemesi
🤖 JFR kod derlemez; olay kaydeder. Yeniden derleme JIT'in işidir.
## B
Uygulamayı her olayda durdurup senkronize etmesi
🤖 Tam tersi; JFR durdurmadan, çok düşük ek yükle olay toplar.
## C
Yalnızca 32-bit JVM'lerde çalışıp modern sürümlerde kendiliğinden devre dışı kalması
🤖 Mimariyle ilgisi yok; JFR modern 64-bit JVM'lerin standart parçasıdır.
## D
JVM'e gömülü olması ve çok düşük ek yükle sürekli olay toplayabilmesi
🤖 Doğru: JFR düşük maliyetle çalışır, bu yüzden üretimde açık bırakılabilir.
## Doğru
D
## Açıklama
**JFR**, JVM'in içine gömülü bir olay kaydedicisidir (ayırma, GC, kilit çekişmesi, CPU örnekleme, IO ...). Ayırt edici özelliği **çok düşük ek yüküdür (low overhead)** — tipik ayarlarla performansa etkisi yüzde birkaçın altında kalır. Bu sayede, sorun çıktığında "acaba profilci açsam mı" telaşı yerine, JFR üretimde **sürekli açık** bırakılabilir; olay çıktığında geriye dönük kayıt (`.jfr` dosyası) JDK Mission Control ile incelenir. Kod derlemez (A), uygulamayı durdurmaz (B — bu onun tam zıddı) ve mimariden bağımsızdır (C). Düşük ek yük + gömülü olması, JFR'yi üretim teşhisinin bel kemiği yapar.
