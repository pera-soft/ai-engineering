---
slug: forkjoin-parallel-stream
baslik: "ForkJoinPool ve Parallel Stream"
kademe: İleri
sira: 53
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir öğretmen 10.000 sınav kâğıdını tek başına sırayla okur. Yirmi öğretmen olsa desteyi ikiye, dörde, sekize böler ve paylaşırlar; üstelik erken biten öğretmen boş oturmaz, hâlâ yığını olan arkadaşının destesinden bir tomar alır. `ForkJoinPool` tam olarak böyle çalışan bir thread havuzudur: büyük işi "böl-ve-fethet" (divide and conquer) mantığıyla parçalar ve işçiler arasında kendiliğinden dengeler. `parallelStream()` de altında bu havuzu kullanır. Çözdüğü problem, çok çekirdekli bir makinede çekirdeklerin boş kalması ve parçaların eşit büyüklükte çıkmamasıdır.

### Temel kavramlar

- **Havuz (thread pool)**: Görev bekleyen, önceden kurulmuş thread topluluğu.
- **Çift-uçlu kuyruk (deque)**: Hem baştan hem sondan ekleme/çıkarma yapılabilen kuyruk.
- **İş çalma (work-stealing)**: Kendi kuyruğu boşalan işçinin, hâlâ meşgul bir işçinin kuyruğunun DİĞER ucundan görev alması — amaç boşta çekirdek bırakmamaktır.
- **Bölücü (`Spliterator`)**: Bir kaynağı paralel işlenebilir parçalara ayıran nesne. Ne kadar ucuz ve dengeli bölerse paralellik o kadar kazandırır.
- **Açlık (starvation)**: Görevlerin işçi bulamayıp ilerleyememesi.

İki başlık her mülakatta çıkar:

- **Work-stealing (iş çalma)**: Her işçi thread'in KENDİ çift-uçlu kuyruğu (deque) vardır. İşi biten bir işçi boş beklemez; MEŞGUL bir işçinin kuyruğunun DİĞER ucundan görev "çalar". Bu, yükü dengeler ve çekirdekleri dolu tutar.
- **Common pool tehlikesi**: Parallel stream ve executor'suz `...Async` çağrıları, JVM genelinde PAYLAŞILAN tek bir `ForkJoinPool.commonPool()` kullanır. Boyutu genelde `çekirdek sayısı - 1`'dir. Buraya konan bloklayan/uzun bir görev, tüm uygulamanın paralel işlerini aç bırakabilir (starvation).

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Work-stealing ve common pool tehlikesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Work-stealing: boşta kalan çalar</text>
  <rect x="30" y="42" width="170" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="115" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">İşçi A (meşgul)</text>
  <rect x="50" y="80" width="130" height="20" rx="4" fill="#ffffff" stroke="#25804C" stroke-width="1"/>
  <rect x="50" y="106" width="130" height="20" rx="4" fill="#ffffff" stroke="#25804C" stroke-width="1"/>
  <rect x="50" y="132" width="130" height="20" rx="4" fill="#ffffff" stroke="#25804C" stroke-width="1"/>
  <text x="115" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kendi kuyruğu dolu</text>
  <path d="M210 100 L295 100" stroke="#B4772B" stroke-width="2" fill="none"/>
  <polygon points="295,100 285,95 285,105" fill="#B4772B"/>
  <text x="252" y="92" text-anchor="middle" font-size="10.5" font-weight="600" fill="#B4772B" font-family="system-ui">çalar</text>
  <rect x="305" y="42" width="170" height="120" rx="12" fill="#E6F0F8" stroke="#1C5A8A" stroke-width="1.5"/>
  <text x="390" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C5A8A" font-family="system-ui">İşçi B (boşta)</text>
  <text x="390" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kendi kuyruğu boş →</text>
  <text x="390" y="128" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">A'nın kuyruğundan alır</text>
  <rect x="500" y="42" width="115" height="120" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="557" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B4322B" font-family="system-ui">commonPool</text>
  <text x="557" y="98" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">~çekirdek-1</text>
  <text x="557" y="116" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">JVM geneli</text>
  <text x="557" y="140" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bloklayan iş</text>
  <text x="557" y="153" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">= starvation</text>
  <text x="320" y="192" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Parallel stream: büyük veri + CPU-yoğun + iyi bölünen kaynak.</text>
</svg>
```

### Nasıl çalışır?

Her işçi, kendi ürettiği alt-görevleri kendi deque'ine iter ve oradan LIFO (son giren ilk çıkar) çeker; bu önbellek dostudur, çünkü en taze veri en sıcaktır. Kuyruğu BOŞALAN işçi beklemez: hâlâ meşgul bir işçinin kuyruğunun DİĞER (baş) ucundan FIFO bir görev çalar. Böylece görevler eşitsiz bölünse bile boşta çekirdek kalmaz. Merkezi tek kilitli bir kuyruk bunu sağlayamaz — kilit çekişmesi yaratır, amacın tersidir. Bu mekanizma işletim sisteminin thread'leri çekirdekler arasında taşımasıyla da, çöp toplayıcının paralel işaretlemesiyle de aynı şey değildir: havuzun KENDİ görev zamanlamasıdır.

`parallelStream()` (ve executor verilmeyen `CompletableFuture.*Async` çağrıları) varsayılan olarak `ForkJoinPool.commonPool()` kullanır. Bu havuz JVM genelinde TEK ve PAYLAŞILANDIR; işçi sayısı genelde `Runtime.availableProcessors() - 1`'dir — çağıran thread de işe katıldığı için toplam paralellik yaklaşık çekirdek sayısı olur. Eleman başına thread açılmaz, terminal işlem başına yeni executor kurulmaz, iş yalnız çağıran thread'de de kalmaz.

Tehlike buradan doğar: havuz sınırsız büyümez ve üretimde de etkindir. İçine BLOKLAYAN (ağ, DB, `sleep`, kilit bekleme) ya da çok uzun bir görev koyarsan, o görevler az sayıdaki işçiyi esir alır. Bloklamada CPU boşalsa bile işçi meşgul sayılır; havuz da o işçinin yerine otomatik yeni işçi açmaz. Sonuçta uygulamanın BAŞKA yerlerindeki parallel stream'ler ve async işler işçi bulamaz (thread starvation) — sorun tek bileşenden tüm uygulamaya yayılır.

Parallel stream ne zaman KAZANDIRIR? Veri büyük, işlem CPU-yoğun/bağımsız/yan etkisiz VE kaynak kolay bölünebilir (splittable: dizi, `ArrayList`) olduğunda — bu üç koşul BİRLİKTE gerekir. Paralellik bedava değildir: bölme, alt-sonuçları birleştirme ve koordinasyon maliyeti vardır; bu sabit maliyet ancak iş yeterince büyükse amorti edilir. Küçük veride veya `LinkedList` gibi zayıf bölünen kaynakta paralellik overhead'i çoğu zaman zarar yazar. "Her zaman daha hızlıdır" yanlış bir varsayımdır; ölçmeden paralele geçmek çoğu kez regresyon getirir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="parallelStream ne zaman kazandırır ne zaman kaybettirir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">parallelStream: kazanç mı, overhead mi?</text>
  <rect x="24" y="42" width="290" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">KAZANDIRIR</text>
  <text x="44" y="92" font-size="11" fill="#1C3247" font-family="system-ui">• büyük veri kümesi</text>
  <text x="44" y="114" font-size="11" fill="#1C3247" font-family="system-ui">• CPU-yoğun, bağımsız, yan etkisiz</text>
  <text x="44" y="136" font-size="11" fill="#1C3247" font-family="system-ui">• kolay/dengeli bölünen kaynak</text>
  <text x="44" y="156" font-size="10.5" fill="#5D7789" font-family="system-ui">  (dizi, ArrayList — O(1) böl)</text>
  <rect x="326" y="42" width="290" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="471" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#8A2E2E" font-family="system-ui">KAYBETTİRİR</text>
  <text x="346" y="92" font-size="11" fill="#1C3247" font-family="system-ui">• küçük veri / ucuz işlem</text>
  <text x="346" y="114" font-size="11" fill="#1C3247" font-family="system-ui">• zayıf bölünen kaynak (LinkedList)</text>
  <text x="346" y="136" font-size="11" fill="#1C3247" font-family="system-ui">• bloklayan I/O (ağ, DB)</text>
  <text x="346" y="156" font-size="10.5" fill="#5D7789" font-family="system-ui">  → böl/birleş overhead'i kazancı yer</text>
</svg>
```

Ortak havuzdan kaçınmanın iki yolu vardır. Birincisi, paralel akışı KENDİ `ForkJoinPool`'unda çalıştırmaktır: bir görev hangi `ForkJoinPool` içinden gönderilirse akış da o havuzu kullanır.

```java
ForkJoinPool ozelHavuz = new ForkJoinPool(4);
long toplam = ozelHavuz.submit(
        () -> liste.parallelStream().mapToLong(this::agirHesap).sum()
).get();   // commonPool değil, ozelHavuz kullanılır
```

Bu, `commonPool`'u temiz tutan pratik bir izolasyon yöntemidir (belgelenmiş bir garanti değil, gözlenen davranıştır). İkincisi ve bloklayan I/O için asıl doğru yol: paralel akış yerine sanal thread'ler (virtual threads, Java 21+) kullanmaktır — sanal thread bloklandığında altındaki taşıyıcı (carrier) thread serbest kalır, dolayısıyla havuz doymaz.

Kazanç, kaynağın `Spliterator`'ının ne kadar ucuz ve dengeli bölündüğüne bağlıdır. Diziler ve `ArrayList` gibi rastgele erişimli (indeksli) yapılar ortadan O(1) ikiye ayrılır; bölme bir kopyalama değil, yalnızca indeks aralığı ayırmadır. İlkel dizilerin de `Spliterator.OfInt` gibi özel bölücüleri vardır, `IntStream.range(0, n)` ise sınırları tam bilindiği için en ideal bölünen kaynaklardandır. `LinkedList`'te ortayı bulmak baştan gezmeyi gerektirir: dengeli parça üretilemez, alt-görevler eşitsiz olur ve work-stealing bile bunu kurtaramaz. IO tabanlı (ör. satır satır okunan) kaynaklar da benzer şekilde zayıf bölünür.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dizi ve ArrayList ucuz bölünür, LinkedList zayıf bölünür">
  <defs>
    <marker id="fj-hop" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#8A2E2E"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kaynak ne kadar ucuz ve dengeli bölünür?</text>
  <text x="40" y="50" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">ArrayList / dizi: indeksle ortadan O(1) böl</text>
  <rect x="40" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="74" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="108" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="142" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="176" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="210" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="244" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <rect x="278" y="60" width="34" height="30" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <line x1="176" y1="52" x2="176" y2="98" stroke="#25804C" stroke-width="2"/>
  <text x="340" y="79" font-size="11" fill="#1C3247" font-family="system-ui">→ dengeli iki yarı, mükemmel bölünür</text>
  <text x="40" y="128" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">LinkedList: ortayı bulmak için baştan gez</text>
  <circle cx="58" cy="162" r="14" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2"/>
  <circle cx="118" cy="162" r="14" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2"/>
  <circle cx="178" cy="162" r="14" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2"/>
  <circle cx="238" cy="162" r="14" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2"/>
  <circle cx="298" cy="162" r="14" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2"/>
  <line x1="72" y1="162" x2="98" y2="162" stroke="#8A2E2E" stroke-width="1.5" marker-end="url(#fj-hop)"/>
  <line x1="132" y1="162" x2="158" y2="162" stroke="#8A2E2E" stroke-width="1.5" marker-end="url(#fj-hop)"/>
  <line x1="192" y1="162" x2="218" y2="162" stroke="#8A2E2E" stroke-width="1.5" marker-end="url(#fj-hop)"/>
  <line x1="252" y1="162" x2="278" y2="162" stroke="#8A2E2E" stroke-width="1.5" marker-end="url(#fj-hop)"/>
  <text x="340" y="150" font-size="11" fill="#1C3247" font-family="system-ui">→ dengeli parça üretilemez,</text>
  <text x="340" y="168" font-size="11" fill="#1C3247" font-family="system-ui">   paralellik overhead'i kazanamaz</text>
  <text x="340" y="190" font-size="10.5" fill="#5D7789" font-family="system-ui">IO tabanlı akış kaynakları da zayıf bölünür</text>
</svg>
```

### Mülakatta ne sorulur?

- **Work-stealing**: işçi başına ayrı deque; kendi görevleri LIFO, çalınan görev meşgul işçinin diğer ucundan FIFO. Merkezi tek kuyruk DEĞİL, OS thread göçü DEĞİL, GC işaretlemesi DEĞİL.
- **`commonPool` paylaşımı**: JVM'de tek, ~çekirdek-1 işçi, üretimde de etkin, sınırsız büyümez. `parallelStream()` akış başına havuz/executor açmaz.
- **Bloklayan iş tehlikesi**: `ForkJoinPool` CPU-yoğun ve KISA görevler içindir; bloklayan çağrı sınırlı işçileri esir alıp havuzu felç eder ve starvation'ı tüm uygulamaya yayar.
- **Kazanç koşulları BİRLİKTE**: büyük veri + CPU-yoğun/bağımsız/yan etkisiz işlem + ucuz ve dengeli bölünen kaynak. Küçük veride overhead kazancı yer.
- **Zayıf bölünen kaynak**: `LinkedList` ve IO tabanlı kaynaklar. `ArrayList`, ilkel diziler ve `IntStream.range` iyi bölünür; bölme kopyalama değil indeks ayırmadır.
- **İzolasyon**: özel `ForkJoinPool` içinden `submit` (gözlenen davranış, belgelenmiş garanti değil); bloklayan I/O içinse asıl doğru yol sanal thread'lerdir.

### Tek cümlede

`ForkJoinPool`, işi bölüp iş çalmayla dengeleyerek CPU-yoğun ve iyi bölünen yükleri hızlandırır; paylaşılan `commonPool`'a bloklayan iş koymak ise bu kazancı tüm uygulama için tersine çevirir.

# fj-1 | Temel
## Soru
`list.parallelStream()` varsayılan olarak hangi thread havuzunu kullanır?
## A
Her `parallelStream()` çağrısı için işletim sisteminden eleman sayısı kadar yeni thread açar
🤖 Hayır; paylaşılan sabit bir havuz kullanılır, eleman başına thread açılmaz.
## B
Her terminal işlem için ayrı bir `ThreadPoolExecutor` oluşturur, iş bitince onu kapatır
🤖 Hayır; ayrı executor açmaz, doğrudan ortak havuza gider.
## C
Yalnız çağıran thread'i kullanır; `parallel` sadece işlem sırasının serbest olduğunu belirtir
🤖 Hayır; çağıran thread'e ek olarak havuz işçileri de devreye girer, iş gerçekten paralelleşir.
## D
JVM genelinde PAYLAŞILAN ortak `ForkJoinPool` (`commonPool`) — boyutu genelde çekirdek sayısı eksi bir
🤖 Doğru: varsayılan olarak `commonPool` kullanılır.
## Doğru
D
## Açıklama
`parallelStream()` (ve executor verilmeyen `CompletableFuture.*Async` çağrıları), varsayılan olarak `ForkJoinPool.commonPool()`'u kullanır. Bu havuz JVM genelinde TEK ve PAYLAŞILANDIR; işçi sayısı genelde `Runtime.availableProcessors() - 1` kadardır (çağıran thread de işe katıldığından toplam paralellik ~çekirdek sayısıdır). Her stream için yeni thread/executor açılmaz (A, B). Bu paylaşım, common pool'a bloklayan iş koymanın neden tehlikeli olduğunu da açıklar: tüm uygulama aynı sınırlı havuzu paylaşır.

# fj-2 | Orta
## Soru
`ForkJoinPool`'daki "work-stealing" (iş çalma) mekanizması nasıl çalışır?
## A
Tüm işçiler tek bir merkezi kilitli kuyruğu paylaşır; sıradaki işi kim önce kaparsa o çalıştırır
🤖 Merkezi tek kuyruk work-stealing değildir; kilit çekişmesi yaratır, amacın tersidir.
## B
İşletim sistemi, yoğun bir çekirdekte biriken thread'leri boştaki çekirdeğe taşıyarak yükü dengeler
🤖 O, OS düzeyinde thread göçüdür; work-stealing havuzun KENDİ görev dağıtım mekanizmasıdır.
## C
Her işçinin kendi çift-uçlu kuyruğu (deque) vardır; boşta kalan, meşgul işçinin kuyruğundan iş çalar
🤖 Doğru: boşta kalan işçi meşgul işçiden görev çalarak yükü dengeler.
## D
Çöp toplayıcının (GC) paralel işaretleme fazında bellek bölgelerini işçilere paylaştırma tekniğidir
🤖 Work-stealing bir görev zamanlama tekniğidir; buradaki bağlamı GC değil `ForkJoinPool`'dur.
## Doğru
C
## Açıklama
Work-stealing, `ForkJoinPool`'un yük dengeleme kalbidir. Her işçi thread'in KENDİ çift-uçlu kuyruğu (deque) vardır ve kendi ürettiği alt-görevleri buraya iter/çeker (LIFO, önbellek dostu). Bir işçinin kuyruğu BOŞALINCA boş beklemez: hâlâ MEŞGUL başka bir işçinin kuyruğunun DİĞER (baş) ucundan bir görev "çalar" (FIFO). Böylece görevler eşitsiz bölünse bile boşta çekirdek kalmaz, iş dinamik olarak dengelenir. Merkezi tek kuyruk (A) bu dengelemeyi ve düşük kilit çekişmesini sağlayamaz. İşletim sisteminin thread'leri çekirdekler arasında taşıması (B) bambaşka bir katmandır — work-stealing, havuzun KENDİ görev dağıtımıdır, OS zamanlayıcısının değil. Benzer bir fikir bazı çöp toplayıcıların içinde de kullanılır ama burada sorulan mekanizma `ForkJoinPool`'un görev zamanlamasıdır (D).

# fj-3 | Orta
## Soru
Bir veri işlemini parallel stream'e çevirmek genellikle NE ZAMAN kazanç sağlar?
## A
Her zaman; parallel stream sıralı (sequential) sürümden daima daha hızlı çalışır
🤖 Hayır; küçük veri veya ucuz işlemde overhead yüzünden çoğu kez daha YAVAŞTIR.
## B
Veri BÜYÜK, işlem CPU-yoğun ve bağımsız, kaynak da kolay bölünebilir (dizi, `ArrayList`) olduğunda
🤖 Doğru: bu üç koşul BİRLİKTE sağlanınca paralellik kazandırır.
## C
Küçük listelerde; eleman sayısı azaldıkça koordinasyon maliyeti de sıfıra yaklaştığı için
🤖 Tersine; küçük veride sabit bölme/birleştirme maliyeti kazancın tamamını yer.
## D
`LinkedList` üzerinde; bağlı liste düğümleri thread'lere tek tek dağıtılabildiği için
🤖 `LinkedList` zayıf bölünür (poor splitting); düğüm dağıtımı diye bir mekanizma yoktur.
## Doğru
B
## Açıklama
Paralellik bedavaya gelmez: veriyi bölme, alt-sonuçları birleştirme, thread koordinasyonu ve olası önbellek/görünürlük maliyetleri vardır. Bu overhead ancak İŞİN BÜYÜKLÜĞÜ yeterince fazlaysa amorti edilir. Kazanç için tipik koşullar BİRLİKTE gerekir: (1) büyük veri kümesi, (2) eleman başına CPU-yoğun, bağımsız ve yan etkisiz işlem, (3) kolay ve DENGELİ bölünebilen kaynak (dizi, `ArrayList` — rastgele erişimli). Küçük veri (C) veya `LinkedList`/IO gibi zayıf bölünen kaynaklar (D) overhead'i kazanamaz. "Her zaman hızlı" (A) yanlış bir varsayımdır; ölçmeden paralele geçmek çoğu zaman regresyon getirir.

# fj-4 | İleri
## Soru
Parallel stream ve `commonPool` ile ilgili "common pool tehlikesi" nedir?
## A
`commonPool` JVM genelinde paylaşıldığı için bir yerdeki bloklayan iş, DİĞER paralel işleri aç bırakır
🤖 Doğru: paylaşılan sınırlı havuz, tek bir bloklayan işle tüm uygulamayı etkiler.
## B
`commonPool` her yeni görev için sınırsız thread açtığından bellek tükenir; başka bir risk yoktur
🤖 `commonPool` sınırlıdır (~çekirdek-1); sınırsız değildir, asıl risk starvation'dır.
## C
Her parallel stream kendi izole havuzunu açtığından paylaşım yoktur; asıl tehlike havuz sayısıdır
🤖 Hayır; varsayılan olarak ortak `commonPool` paylaşılır, akış başına havuz açılmaz.
## D
`commonPool` yalnız birim testlerinde etkindir; üretimde JVM onu otomatik devre dışı bırakır
🤖 `commonPool` üretimde de etkindir; tehlike özellikle canlıda gerçekleşir.
## Doğru
A
## Açıklama
`commonPool` JVM genelinde TEK ve PAYLAŞILANDIR, boyutu da sınırlıdır (~çekirdek-1). Bu havuza BLOKLAYAN (ağ, DB, `sleep`, kilit bekleme) ya da çok uzun süren bir görev koyarsan, o görevler sınırlı işçileri tutar ve uygulamanın BAŞKA yerlerindeki parallel stream'ler/async işler işçi bulamayıp aç kalır (thread starvation) — sorun bir bileşenden tüm uygulamaya yayılır. Doğru yaklaşım: paralel işi ayrı, kendi ömrü yönetilen bir `ForkJoinPool` (ör. `customPool.submit(() -> stream.parallel()...).get()`) veya uygun bir executor içinde izole etmektir. `commonPool` sınırsız değildir (B) ve üretimde de etkindir (D).

# fj-5 | İleri
## Soru
Parallel stream / `commonPool` içinde BLOKLAYAN bir çağrı (ör. senkron HTTP isteği, DB sorgusu) yapmak neden anti-pattern'dir?
## A
Anti-pattern değildir; parallel stream tam olarak bloklayan I/O'yu paralelleştirmek için tasarlanmıştır
🤖 Tam tersi; `ForkJoinPool` CPU-yoğun ve KISA görevler içindir, bloklama için değil.
## B
Bloklama havuzu hızlandırır; thread beklerken CPU boşaldığı için diğer görevler daha çabuk biter
🤖 CPU boşalsa da sınırlı işçiler meşgul tutulur; toplam paralellik çöker.
## C
Sorun değildir; `commonPool` bir işçi bloklandığında yerine otomatik yeni işçi açıp paralelliği korur
🤖 `commonPool` bloklamada sınırsız büyümez; işçiler tükenir ve havuz durur.
## D
`ForkJoinPool` az sayıda (~çekirdek) işçiyle çalışır; bloklayan çağrı bu işçileri esir alıp havuzu felç eder
🤖 Doğru: bloklayan iş sınırlı işçileri tutar, havuz kilitlenir.
## Doğru
D
## Açıklama
`ForkJoinPool` az sayıda (yaklaşık çekirdek kadar) işçiyle, CPU-yoğun ve KISA alt-görevleri hızla çevirmek üzere tasarlanmıştır. Bir işçi BLOKLAYAN bir çağrıda (ağ/DB/`sleep`) beklerken o işçi başka iş yapamaz; birkaç bloklayan görev tüm işçileri tutarsa havuz felç olur ve hem bu stream hem de aynı `commonPool`'u kullanan başka işler ilerleyemez (starvation). Havuz bu durumda otomatik olarak sınırsız büyümez (C). Doğru çözüm: bloklayan işleri parallel stream/`commonPool` dışına, uygun boyutlu AYRI bir executor'a taşımak; ya da modern alternatif olarak bloklayan I/O için sanal thread'leri (virtual threads) kullanmaktır.

# fj-6 | Uzman
## Soru
Aşağıdaki kaynaklardan hangisi parallel stream'de ZAYIF bölünür (poor splitting) ve bu yüzden paralellikten genellikle kazanç sağlamaz?
## A
`int[]` gibi ilkel diziler; ilkel türler için `Spliterator` tanımlı olmadığından bölünemezler
🤖 Yanlış; ilkel dizilerin `Spliterator.OfInt` gibi karşılıkları vardır ve O(1) bölünürler.
## B
`ArrayList`; dizi tabanlı olduğu için her bölme adımında parçaların kopyalanması gerekir
🤖 Yanlış; bölme kopyalama değil indeks aralığı ayırmadır — `ArrayList` dengeli bölünür.
## C
`LinkedList` ve IO tabanlı kaynaklar; ortadan O(1) bölünemedikleri için dengeli parça üretilemez
🤖 Doğru: bağlı listede ortayı bulmak baştan gezmeyi gerektirir.
## D
`IntStream.range(0, n)` ile üretilen akışlar; aralık sınırları önceden bilinmediği için bölünemezler
🤖 Yanlış; aralık sınırları tam olarak bilinir, bu akışlar en ideal bölünen kaynaklardandır.
## Doğru
C
## Açıklama
Parallel stream'in verimi, kaynağın `Spliterator`'ının veriyi ne kadar UCUZ ve DENGELİ bölebildiğine bağlıdır. Diziler ve `ArrayList` gibi rastgele erişimli (indeksli) yapılar ortadan O(1) ikiye bölünür — mükemmel bölünürler. İlkel dizilerin de `Spliterator.OfInt` gibi özel bölücüleri vardır (A yanlış) ve bölme işlemi kopyalama değil, yalnızca indeks aralığı ayırmadır (B yanlış). `LinkedList` ise ortasını bulmak için baştan gezmeyi gerektirir; `Spliterator` dengeli parçalar çıkaramaz, alt-görevler eşitsiz olur ve work-stealing bile bunu kurtaramaz — paralellik overhead'i kazanç getirmez. IO tabanlı (ör. satır satır okunan) kaynaklar da benzer şekilde zayıf bölünür. `IntStream.range` (D) ise tam tersidir: aralığın alt ve üst sınırı bilindiği için mükemmel ve maliyetsiz bölünür. Kural: bölme ne kadar ucuz ve dengeliyse paralellik o kadar kazandırır — kesin kazanç yine de ancak ölçümle doğrulanır.
