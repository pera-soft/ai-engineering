---
slug: jmh-mikrobenchmark
baslik: "JMH ile Güvenilir Mikrobenchmark"
kademe: Orta
sira: 67
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir koşucunun hızını kronometreyle ölçmek istiyorsunuz. Ama pist yokuş aşağı, arkadan rüzgâr var ve koşucu daha ısınmamış. Kronometre doğru çalışsa bile ÇIKAN SAYI yalan söyler. Java'da bir kod parçasının süresini `System.nanoTime()` ile ölçmek tam olarak budur: JVM canlı bir sistemdir; ölçtüğünüz sırada kodu değiştirir, hatta hesabı tümden siler. JMH, bu bozucu etkileri tek tek etkisizleştiren standart ölçüm düzeneğidir.

### Temel kavramlar

- **Ölçüm / kıyaslama (benchmark)**: bir kod parçasının ne kadar sürdüğünü, tekrarlanabilir ve güvenilir biçimde saptamak. Amaç tek bir sayı değil, o sayının ne kadar güvenilir olduğudur.
- **Mikrobenchmark**: tek bir metot ya da döngü gibi çok küçük, İZOLE bir parçanın ölçümü. Bütün sistemin performansı değildir.
- **JIT (Just-In-Time)**: JVM'in kodu çalışırken makine koduna derlemesi. Ölçümü bozmasının kaynağı budur.
- **Isınma (warm-up)**: kodun önce yorumlanıp sonra C1, en sonunda C2/Graal ile tepe optimizasyona ulaşması. Bu geçiş sürerken ölçülen süreler gerçek performans değildir.
- **Ölü kod eleme (dead-code elimination)**: JIT, gözlemlenebilir etkisi olmayan hesapları tümden siler.
- **Sabit katlama (constant folding)**: girdiler derleme anında sabitse, JIT sonucu önceden hesaplayıp gömer; hesap hiç yapılmaz.
- **Dağılım ve varyans**: aynı ölçüm defalarca yapılınca sonuçlar bir aralığa yayılır. Ortalama tek başına yetmez; sapma büyükse sonuca güvenilmez.
- **Profil kirlenmesi**: bir benchmark'ın JIT'e yüklediği istatistiğin, aynı JVM'de sonra çalışan benchmark'ın derlenmesini etkilemesi.

### Nasıl çalışır?

**JMH (Java Microbenchmark Harness)**, OpenJDK ekibinin bu tuzakları etkisizleştirmek için yazdığı standart araçtır. Çözdüğü başlıca sorunlar:

- **Isınma (warm-up) yinelemeleri**: JMH önce, sonuç KAYDETMEDEN çok sayıda tur çalıştırır; JIT tepe koda ulaşsın diye. Warm-up turlarının süreleri ortalamaya düşük ağırlıkla bile katılmaz — **tümüyle atılır**; böylece yorumlayıcı/C1 gürültüsü ölçüme karışmaz. Bu, disk/IO önbelleği ısıtmak ya da GC'yi kapatmak değildir (GC kapatılamaz da).
- **Ölçüm (measurement) yinelemeleri**: ısınma bittikten sonra çalışan ve raporlanan turlar. Tek tur yerine çok sayıda tur alınır, çünkü hedef tek bir sayı değil **dağılımdır** — JMH ortalamayla birlikte hata payını/yüzdelikleri de verir.
- **Fork**: her benchmark'ı **ayrı bir JVM sürecinde** çalıştırır. Tek JVM'de arka arkaya koşan benchmark'lar birbirini kirletir: birincisi JIT'e belli bir profil (tip dağılımı, dal istatistiği) yükler, ikincisi kendine ait olmayan bu profille derlenmiş kodu miras alır ve ölçüm sıraya göre sapar. Birden çok fork, JVM örnekleri arasındaki sapmayı da görünür kılar.
- **Ölü kod eleme**: bir hesabın sonucunu kullanmazsan JIT o hesabı tümden siler — "sıfır nanosaniye" gibi sahte sonuçlar çıkar. Çözüm: sonucu **döndürmek** ya da `Blackhole.consume(...)` ile tüketmek.
- **Sabit katlama**: girdi sabitse JIT sonucu derleme anında hesaplayıp döngüyü siler. Çözüm: girdileri `@State` nesnesinden okumak, sabit yazmamak.
- **Blackhole**: JMH'nin, üretilen değerlerin "kullanılıyor" numarası yaptığı yardımcı. Bir benchmark döndüremeyeceği BİRDEN ÇOK değer üretiyorsa (tek `return` yetmez) bunları JIT'in silmesini engeller — ve bunu ölçümü bozacak gözle görülür bir maliyet eklemeden yapar.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JMH: warm-up, fork ve dead-code elimination tuzakları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JMH mikrobenchmark tuzaklarını etkisizleştirir</text>
  <rect x="24" y="46" width="285" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="40" y="68" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Warm-up yinelemeleri</text>
  <text x="40" y="88" font-size="10.5" fill="#1C3247" font-family="system-ui">JIT tepe koda ısınana dek ölçme</text>
  <rect x="331" y="46" width="285" height="54" rx="10" fill="#EAF0F6" stroke="#2C5A82" stroke-width="1.4"/>
  <text x="347" y="68" font-size="11.5" font-weight="700" fill="#2C5A82" font-family="system-ui">Fork: ayrı JVM</text>
  <text x="347" y="88" font-size="10.5" fill="#1C3247" font-family="system-ui">profil kirlenmesini önler</text>
  <rect x="24" y="112" width="285" height="54" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.4"/>
  <text x="40" y="134" font-size="11.5" font-weight="700" fill="#B4322B" font-family="system-ui">Ölü kod eleme tuzağı</text>
  <text x="40" y="154" font-size="10.5" fill="#1C3247" font-family="system-ui">sonucu döndür / Blackhole.consume</text>
  <rect x="331" y="112" width="285" height="54" rx="10" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.4"/>
  <text x="347" y="134" font-size="11.5" font-weight="700" fill="#B5701B" font-family="system-ui">Sabit katlama tuzağı</text>
  <text x="347" y="154" font-size="10.5" fill="#1C3247" font-family="system-ui">girdiyi @State'ten oku, sabit yazma</text>
  <text x="320" y="196" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Elle nanoTime + tek JVM = yanıltıcı; JMH bunları standartlaştırır.</text>
</svg>
```

Örnek iskelet:

```java
@State(Scope.Thread)
public class ToplamaBench {
    private int a = 40, b = 2;

    @Benchmark
    @Warmup(iterations = 5)
    @Fork(2)
    public int topla() {         // sonucu DÖNDÜR → ölü kod elenmez
        return a + b;
    }

    @Benchmark
    public void coklu(Blackhole bh) {
        bh.consume(a * b);       // Blackhole ile tüket
        bh.consume(a - b);
    }
}
```

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JMH koşusu: fork, warm-up yinelemeleri atılır, ölçüm yinelemeleri kaydedilir">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">JMH koşusu: warm-up (atılır) → ölçüm (kaydedilir)</text>
  <text x="60" y="54" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Fork 1 (taze JVM)</text>
  <rect x="60" y="64" width="40" height="40" rx="4" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="106" y="64" width="40" height="40" rx="4" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="152" y="64" width="40" height="40" rx="4" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="198" y="64" width="40" height="40" rx="4" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="244" y="64" width="40" height="40" rx="4" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="80" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">W</text>
  <text x="126" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">W</text>
  <text x="172" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">W</text>
  <text x="218" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">W</text>
  <text x="264" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">W</text>
  <line x1="292" y1="58" x2="292" y2="112" stroke="#1C3247" stroke-width="1" stroke-dasharray="4 4"/>
  <rect x="300" y="64" width="40" height="40" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="346" y="64" width="40" height="40" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="392" y="64" width="40" height="40" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="438" y="64" width="40" height="40" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="484" y="64" width="40" height="40" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="320" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">M</text>
  <text x="366" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">M</text>
  <text x="412" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">M</text>
  <text x="458" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">M</text>
  <text x="504" y="89" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">M</text>
  <text x="172" y="130" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">warm-up → sonuç atılır</text>
  <text x="412" y="130" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">ölçüm → kaydedilir</text>
  <text x="320" y="168" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">@Fork(2): her benchmark ayrı JVM'de tekrarlanır → profil kirlenmesi ve varyans kontrolü</text>
</svg>
```

### Mülakatta ne sorulur?

**"Fork paralellik midir?" — HAYIR.** `@Fork` bir hızlandırma mekanizması değildir; benchmark'lar sırayla, ama TAZE JVM'lerde çalışır ve her yeni JVM ile onun warm-up'ı toplam koşu süresini UZATIR. Isınma ihtiyacını da ortadan kaldırmaz: taze JVM ısınmayı sıfırladığı için her fork kendi warm-up turlarını baştan çalıştırır. Paylaşılan bellek/veri bozulmasını önlemek de derdi değildir — amaç **JIT profil izolasyonudur**.

**Sonucu tüketmezsen ölçüm yalan söyler.** `int x = pahaliHesap(40, 2);` yazıp `x`'i hiç kullanmayan bir benchmark'ta JIT `pahaliHesap` çağrısını bile silebilir; sonuç "neredeyse sıfır ns" olur. `void` dönen bir benchmark metodu geçerlidir, derleme hatası vermez; yerel bir `int` değişkenin yığıt maliyeti ihmal edilebilir. Sabit argümanlar (`40, 2`) ayrı bir risktir ama oradaki ASIL ve kesin tuzak ölü kod elemesidir.

**`@State` neden?** Amaç okunabilirlik değildir ve `@State` zorunlu bir açıklama (annotation) da değildir. Değerleri bir `@State` nesnesinin alanlarından okumak, JIT'e "bunlar çalışma zamanında değişebilir" izlenimi verir ve **sabit katlamayı** engeller; `40 + 2` derleme anında `42`'ye çevrilemez. `@State` nesnesi her yinelemede yeniden yaratılmaz — kapsamına göre (`Scope.Thread` / `Scope.Benchmark`) bir kez kurulur; ölçümü "gerçekçi biçimde yavaşlatmak" gibi bir amacı yoktur.

**Blackhole ne DEĞİLDİR?** Loglama/raporlama yapmaz, çöpü serbest bırakıp GC etkisini ölçümden çıkarmaz, benchmark'ı paralel çalıştırmaz (o `@Threads` işidir). Tek işi değerleri güvenle tüketmektir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ölü kod eleme: kullanılmayan sonuç silinir; Blackhole ile tüketilen ölçülür">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ölü kod eleme: sonucu tüket, yoksa JIT siler</text>
  <rect x="30" y="44" width="270" height="40" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="165" y="69" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">x = pahaliHesap();  // kullanılmaz</text>
  <path d="M165 84 L165 108" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#jmh-dce-ok)"/>
  <rect x="30" y="110" width="270" height="34" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="165" y="132" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">JIT: ölü kod olarak eler</text>
  <text x="165" y="176" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">≈ 0 ns — sahte sonuç</text>
  <rect x="340" y="44" width="270" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="475" y="69" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">return x / bh.consume(x)</text>
  <path d="M475 84 L475 108" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#jmh-dce-ok)"/>
  <rect x="340" y="110" width="270" height="34" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="475" y="132" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">değer 'kullanılıyor' sayılır</text>
  <text x="475" y="176" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">gerçek süre ölçülür</text>
  <defs>
    <marker id="jmh-dce-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**Sonucu nasıl yorumlamalı?** JMH, mikroölçümün TEKNİK tuzaklarını (ısınma, ölü kod, sabit katlama, profil kirlenmesi) büyük ölçüde ortadan kaldırır; ama bir mikrobenchmark hâlâ yalnızca izole bir parçayı ölçer. Gerçek sistemde önbellek etkileri, çekişme (contention), IO, GC etkileşimi ve gerçek veri dağılımı sonucu değiştirebilir. Doğru tutum: mikroölçümü değerli bir **ipucu** saymak, ama gerçek iş yükü altında **JFR/async-profiler** ile teyit etmeden mutlaklaştırmamak. Sonuç ne kesin üretim kanıtıdır, ne JMH onu mutlak yapar, ne de yorum "daha büyük donanım al" demektir.

### Tek cümlede

JMH, benchmark'ı taze JVM'lerde (fork) ve ısınma turları atılarak çalıştırıp sonuçları `Blackhole`/`@State` ile ölü kod elemesinden ve sabit katlamadan korur; ama çıkan sayı hâlâ izole bir parçanın ölçümüdür ve üretim profiliyle doğrulanmadan mutlaklaştırılmamalıdır.

# jmh-1 | Orta
## Soru
JMH'de "warm-up" yinelemelerinin amacı nedir?
## A
İlk turların sonuçlarını da ortalamaya katmak, ama onlara daha düşük ağırlık vermek
🤖 Ağırlıklandırma yok: warm-up turlarının sonuçları ortalamaya HİÇ katılmaz, tümüyle atılır.
## B
Benchmark verisini diske ve işletim sistemi önbelleğine önceden yükleyip IO etkisini silmek
🤖 IO/önbellek ısıtma değil; warm-up asıl olarak JIT'in tepe koda ulaşmasını beklemek içindir.
## C
JIT tepe (optimize) koda ulaşana dek sonuç KAYDETMEDEN çalıştırmak; ölçümü ancak ısındıktan sonra almak
🤖 Doğru: warm-up, yorumlayıcı/C1 gürültüsünü ölçümden dışlar.
## D
Çöp toplamayı geçici olarak devre dışı bırakıp ölçümü GC duraklamalarından arındırmak
🤖 Warm-up GC'yi kapatmaz (kapatılamaz da); yaptığı iş JIT ısınmasını beklemektir.
## Doğru
C
## Açıklama
JVM'de kod **kademeli** hızlanır: önce yorumlanır, sonra C1, en sonunda sıcak metotlar C2/Graal ile tepe optimizasyona ulaşır (warm-up). Ölçümü ısınmadan almak, yorumlayıcı/C1 döneminin yavaş sonuçlarını gerçek performansmış gibi kaydeder — yanıltıcıdır. **Warm-up yinelemeleri**, JMH'nin benchmark'ı **sonuç kaydetmeden** defalarca çalıştırıp JIT'in tepe koda ulaşmasını beklemesidir; gerçek ölçüm ancak bundan sonra alınır. Warm-up turları ortalamaya düşük ağırlıkla da olsa KATILMAZ, tamamen atılır (A); amaç disk/IO önbelleği ısıtmak (B) veya GC'yi kapatmak (D) değildir — tamamen **JIT ısınmasını ölçümden ayıklamakla** ilgilidir ve güvenilir mikrobenchmark'ın ilk şartıdır.

# jmh-2 | Orta
## Soru
JMH'nin her benchmark'ı ayrı bir JVM sürecinde çalıştırması (`@Fork`) neyi önler?
## A
Benchmark'ların birbirini beklemesini: fork'lar paralel koştuğu için toplam süre kısalır
🤖 Fork paralellik değildir; benchmark'lar sırayla ama TAZE JVM'lerde çalışır, süre uzar.
## B
Warm-up ihtiyacını: taze JVM'de kod baştan derlenmiş geldiği için ısınma gerekmez
🤖 Tersi; taze JVM ısınmayı SIFIRLAR, bu yüzden her fork kendi warm-up turlarını çalıştırır.
## C
Benchmark iş parçacıklarının aynı yığına yazıp veriyi bozmasını
🤖 Benchmark'lar zaten paylaşılan durum kullanmaz; fork'un derdi bellek değil JIT profilidir.
## D
Bir önceki benchmark'ın topladığı JIT profilinin sonrakini kirletmesini (profil kirlenmesi)
🤖 Doğru: her benchmark taze bir JVM'de çalışır, böylece profil taşınmaz.
## Doğru
D
## Açıklama
Tek bir JVM'de arka arkaya çalışan benchmark'lar birbirini **kirletebilir**: birincisi JIT'e belli bir profil (tip dağılımı, dal istatistiği) yükler; ikincisi, aslında kendisine ait olmayan bu profille derlenmiş kodu miras alır. Sonuç: ölçüm, sıraya ve önceki yüke bağlı olarak sapar (run-to-run kararsızlık). **`@Fork`**, her benchmark'ı **taze, ayrı bir JVM sürecinde** çalıştırarak bu profil taşınmasını keser; birden çok fork ise sonuçların JVM-örneği varyansını da görünür kılar. Fork bir hızlandırma/paralellik mekanizması değildir (A) — aksine her yeni JVM ve onun warm-up'ı toplam koşu süresini uzatır. Isınma ihtiyacını da ortadan kaldırmaz (B): taze JVM ısınmayı sıfırladığı için her fork kendi warm-up turlarını baştan çalıştırır. Paylaşılan bellek/veri bozulması da konu değildir (C) — amaç **JIT profil izolasyonudur**.

# jmh-3 | Orta
## Soru
Aşağıdaki JMH benchmark'ında olası tuzak nedir?
```java
@Benchmark
public void hesapla() {
    int x = pahaliHesap(40, 2);   // sonuç hiçbir yerde kullanılmıyor
}
```
## A
`x` hiçbir yerde kullanılmadığından JIT hesabı ölü kod olarak eleyebilir; benchmark neredeyse sıfır ölçer
🤖 Doğru: kullanılmayan sonuç dead-code elimination'a uğrar, ölçüm anlamsızlaşır.
## B
Metot `void` döndüğü için JMH ölçümü hiç başlatmaz ve derleme hatası verir
🤖 `void` benchmark geçerlidir (Blackhole ile kullanılır); sorun derleme değil, sonucun tüketilmemesidir.
## C
Yerel değişken kullanıldığı için ölçüme yığın (stack) ayırma maliyeti eklenir
🤖 Yerel `int` ihmal edilebilir; asıl tuzak hesabın tümden silinebilmesidir.
## D
Sabit argümanlar (`40, 2`) yüzünden yalnızca sabit katlama olur, başka sorun yoktur
🤖 Sabit katlama da risktir ama buradaki asıl ve kesin tuzak ölü kod elemesidir.
## Doğru
A
## Açıklama
Bu benchmark'ın sonucu (`x`) hiçbir yerde kullanılmıyor. JIT, gözlemlenebilir etkisi olmayan hesapları **ölü kod (dead code)** sayıp tümden **eler**; `pahaliHesap` çağrısı bile silinebilir. Sonuç: benchmark, gerçekte pahalı olan işi "neredeyse sıfır nanosaniye" olarak ölçer — tamamen yanıltıcı. Çözüm, sonucu **döndürmek** (`return pahaliHesap(...)`) ya da bir **`Blackhole.consume(x)`** ile tüketerek JIT'e "bu değer kullanılıyor" demektir. `void` dönen benchmark metodu geçerlidir, derleme hatası vermez (B); yerel bir `int` değişkenin maliyeti ihmal edilebilir (C). Sabit argümanlar da ayrı bir risktir ama "tek sorun sabit katlama" demek (D) yanlıştır: burada asıl ve kesin tuzak, sonucun hiç tüketilmemesiyle oluşan klasik **dead-code elimination**'dır ve JMH'nin var oluş nedenlerinden biridir.

# jmh-4 | Orta
## Soru
JMH'de `Blackhole` (kara delik) ne işe yarar?
## A
Üretilen değerleri toplayıp benchmark raporuna ve log dosyasına yazar
🤖 Loglama/raporlama yapmaz; Blackhole değerleri "tüketerek" ölü kod elemesini engeller.
## B
Üretilen değerleri "tüketerek" JIT'in onları ölü kod olarak elemesini engeller
🤖 Doğru: Blackhole.consume, sonucun kullanıldığı numarasıyla dead-code elimination'ı önler.
## C
Benchmark sırasında üretilen çöpü anında serbest bırakıp GC etkisini ölçümden çıkarır
🤖 GC ile ilgisi yok; amaç JIT'in ölü kod elemesini engellemektir.
## D
Benchmark metodunu birden çok iş parçacığında otomatik paralel çalıştırır
🤖 Paralellik `@Threads` işidir; Blackhole yalnızca değer tüketicisidir.
## Doğru
B
## Açıklama
Bir benchmark, döndüremeyeceği **birden çok** değer üretiyorsa (tek `return` yetmez), bunları JIT'in silmesini engellemek için bir yola gerek vardır. **`Blackhole`**, JMH'nin sağladığı yardımcıdır: `blackhole.consume(deger)` çağrısı, o değerin "kullanılıyor" olduğunu JIT'e inandırır ve **ölü kod elemesini** (ve bazı sabit katlama durumlarını) engeller — ama değeri tüketirken ölçümü bozacak gözle görülür bir maliyet eklemeyecek şekilde tasarlanmıştır. Böylece hesap gerçekten yapılır ve dürüstçe ölçülür. Blackhole loglamaz (A), GC'yi hızlandırmaz (C) ve paralelleştirme yapmaz (D); tek işi **değerleri güvenle tüketip** JIT optimizasyonlarının ölçümü sahtelemesini durdurmaktır.

# jmh-5 | Orta
## Soru
Bir benchmark'ta girdi olarak sabit `topla(40, 2)` yazmak yerine değerleri bir `@State` nesnesinden okumak neden önemlidir?
## A
Benchmark kodunu kısaltıp okunabilirliği artırmak; ölçüme etkisi yoktur
🤖 Amaç okunabilirlik değil; ölçüme etkisi büyüktür — sabit katlamayı (constant folding) engeller.
## B
`@State` olmadan JMH benchmark'ı hiç çalıştırmaz; zorunlu bir açıklamadır (annotation)
🤖 Zorunlu değildir; `@State` sabit katlamayı önlemeye ve durum paylaşımına yarayan bir desendir.
## C
Girdi derleme anında sabitse JIT sonucu önceden hesaplayabilir (sabit katlama); `@State`'ten okunan değer bunu engeller
🤖 Doğru: değişken görünen girdi, JIT'in sonucu derleme anında sabitlemesini önler.
## D
Her yinelemede yeni nesne ayırarak ölçümü gerçekçi biçimde yavaşlatmak
🤖 Yavaşlatma amacı yok; `@State` nesnesi yineleme başına yeniden yaratılmaz.
## Doğru
C
## Açıklama
JIT çok agresiftir: girdiler **derleme anında sabitse**, `40 + 2` gibi bir ifadeyi çalıştırmak yerine sonucu (`42`) doğrudan gömer — buna **sabit katlama (constant folding)** denir; hesap hiç yapılmaz, benchmark yalan söyler. Değerleri bir **`@State`** nesnesinin alanlarından okumak, JIT'e "bunlar çalışma zamanında değişebilir" izlenimi verir; böylece sabit katlama devreye girmez ve hesap gerçekten ölçülür. Bu, okunabilirlik (A) veya JMH'nin zorunlu kıldığı bir kural (B) meselesi değildir; `@State` nesnesi her yinelemede yeniden yaratılıp benchmark'ı yavaşlatmaz da (D) — kapsamına göre (`Scope.Thread`/`Scope.Benchmark`) bir kez kurulur — amaç, JIT'in **girdiyi sabitleyerek** ölçümü çarpıtmasını engelleyip gerçekçi bir sonuç almaktır. Dead-code elimination ile birlikte, sabit katlama JMH'nin sürekli savaştığı iki temel tuzaktan biridir.

# jmh-6 | Orta
## Soru
Bir mikrobenchmark sonucunu yorumlarken hangisi en doğru tutumdur?
## A
Mikrobenchmark sonucu tek başına, gerçek üretim performansının kesin kanıtıdır
🤖 Değil; izole kod parçasını ölçer, bütün sistemi temsil etmeyebilir.
## B
JMH kullanıldığı sürece hiçbir tuzak kalmaz, sonuç mutlaktır
🤖 JMH tuzakları azaltır ama yanlış tasarlanmış benchmark yine yanıltır; mutlak değildir.
## C
Sonuç ne olursa olsun daha büyük donanım almak tek çözümdür
🤖 Bu benchmark yorumuyla ilgisiz; ölçüm, kod düzeyinde kararı besler.
## D
Mikrobenchmark izole bir parçayı ölçer; sonucu gerçek iş yükü profiliyle doğrulamadan mutlaklaştırmamak gerekir
🤖 Doğru: mikroölçüm faydalı bir ipucudur ama üretim profiliyle teyit ister.
## Doğru
D
## Açıklama
JMH, mikroölçümün **teknik** tuzaklarını (ısınma, ölü kod, sabit katlama, profil kirlenmesi) büyük ölçüde ortadan kaldırır; ama bir mikrobenchmark hâlâ yalnızca **izole bir kod parçasını** ölçer. Gerçek sistemde önbellek etkileri, çekişme (contention), IO, GC etkileşimi ve gerçek veri dağılımı sonucu değiştirebilir. Bu yüzden doğru tutum: mikroölçümü değerli bir **ipucu** olarak almak, ama gerçek iş yükü altında **JFR/async-profiler** gibi araçlarla teyit etmeden mutlaklaştırmamaktır. Sonuç ne kesin üretim kanıtıdır (A), ne JMH onu mutlak yapar (B), ne de yorum "daha büyük donanım" demektir (C). Ölçüm bir karar aracıdır; bağlamıyla birlikte okunur.
