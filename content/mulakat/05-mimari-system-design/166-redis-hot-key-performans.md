---
slug: redis-hot-key-performans
baslik: "Redis Hot Key ve Performans: Tek-Node Darboğazı, SCAN ve Teşhis"
kademe: İleri
sira: 166
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Redis çok hızlıdır; ama iki özelliği tuzak yaratır. Komutları **tek bir iş parçacığında (thread)** işler ve veriyi **bellekte** tutar. Tek iş parçacığı şu demektir: **uzun süren tek bir komut tüm sunucuyu bekletir.** Bu bölüm, "Redis yavaşladı" dendiğinde bakılacak iki asıl sebebi anlatır: **dengesiz erişim** (sıcak anahtar) ve **bloke eden komutlar**.

### Temel kavramlar

- **Sıcak anahtar (hot key)**: Trafiğin orantısız biçimde **tek bir anahtara** yığılmasıdır. Küme (cluster) **veriyi** 16384 özet yuvası (hash slot) üzerinden düğümlere böler. Ama tek bir anahtar her zaman tek bir yuvaya düşer; o yuva da **tek bir ana düğüme (master node)** aittir. Anahtar **bölünemediği** için 6 düğümlü bir kümede bile tüm yük o tek düğüme biner. Küme tek düğümden ibaret değildir ve anahtarı tüm ana düğümlere kopyalamaz. Yani sorun dağıtımın olmaması değil, anahtarın bölünemez oluşudur. Bu yüzden **düğüm eklemek bu sorunu çözmez**.
- **Hafifletme katmanları**: Üç katman vardır. (1) Uygulama içindeki **yerel önbellek** (near/L1 cache) çok okunan değeri Redis'e hiç gitmeden karşılar. (2) Sistem okuma ağırlıklıysa **kopyalardan (replika) okumak** yükü yayar. (3) Mümkünse anahtarın kendisi **parçalanır (sharding)**: tek bir sayaç yerine `sayac:{parca}` biçiminde birden çok anahtar tutulur, okurken hepsi toplanır. TTL'i sonsuz yapmak ise erişim yükünü hiç etkilemez.
- **O(N) ve tıkayan komutlar**: Maliyeti veri büyüklüğüyle orantılı artan komutlardır. `KEYS pattern` tüm anahtar uzayını **tek komutta** gezer. `SMEMBERS`, `HGETALL` ve `LRANGE 0 -1` de büyük koleksiyonların tüm içeriğini tek seferde çeker. Tek iş parçacığı bu süre boyunca **kilitlenir** ve diğer bütün istemciler bekler. Ayrıca **devasa yanıt** hem ağı hem istemci belleğini zorlar. Dikkat: bunlar okuma komutlarıdır, veriyi silmezler; tehlike tıkamada ve yanıt boyutundadır.
- **Artımlı tarama (`SCAN`)**: `SCAN` ve tip-özel kardeşleri `SSCAN`/`HSCAN`/`ZSCAN`, bir **imleç (cursor)** ile veriyi küçük parçalar hâlinde gezer; her çağrı kısa sürer ve sunucuyu bloklamaz. Üretimde `KEYS` yerine daima bunlar kullanılır.
- **Teşhis araçları**: `redis-cli --bigkeys` her tipteki en büyük anahtarları örnekleyerek tarar; `MEMORY USAGE <key>` tek bir anahtarın gerçek bellek maliyetini ölçer; `SLOWLOG GET` eşiği aşan yavaş komutları listeler; `LATENCY` gecikme kaynaklarını izler. Toplam bellek (`INFO memory`) veya anahtar sayısı (`DBSIZE`) hangi anahtarın şiştiğini söylemez.
- **Bağlantı yönetimi**: Her istekte bağlantı açıp kapatmaya **bağlantı çalkantısı (connection churn)** denir. Her seferinde TCP el sıkışması, varsa `AUTH` ve soket kurulum maliyeti ödenir. Bu da gecikmeyi artırır; sunucunun sınırlı bağlantı bütçesini ve dosya tanıtıcılarını tüketir. Doğrusu bağlantıyı **yeniden kullanmaktır**: Jedis'te bağlantı havuzu, Lettuce'ta paylaşılan eşzamansız bağlantı. Yeni bağlantı açmak Redis'i paralel çalıştırmaz; komutlar yine tek iş parçacığında sırayla işlenir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hot key tek node darboğazı ve teşhis araçları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hot key ve performans</text>
  <rect x="24" y="44" width="290" height="140" rx="12" fill="#FBEDE9" stroke="#B4472B" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#B4472B" font-family="system-ui">Hot key darboğazı</text>
  <text x="169" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1 anahtar → 1 slot → 1 node</text>
  <text x="169" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">cluster büyük olsa da</text>
  <text x="169" y="130" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yük tek node'a biner</text>
  <text x="169" y="158" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">çözüm: yerel cache, replika,</text>
  <text x="169" y="174" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">anahtar parçalama</text>
  <rect x="326" y="44" width="290" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Bloke etme ve teşhis</text>
  <text x="471" y="94" text-anchor="middle" font-size="11" fill="#B4472B" font-family="system-ui">KEYS, SMEMBERS → bloklar</text>
  <text x="471" y="114" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">SCAN/SSCAN → artımlı</text>
  <text x="471" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">--bigkeys · MEMORY USAGE</text>
  <text x="471" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">SLOWLOG · LATENCY</text>
  <text x="471" y="172" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bağlantı havuzu kullan</text>
</svg>
```

### Nasıl çalışır?

Anahtar içgörü şudur: Redis tek iş parçacıklı olduğu için **tek bir uzun komut tüm sunucuyu bekletir**. Performans sorunları çoğu zaman bellek yetersizliğinden çıkmaz. Asıl sebepler **tıkayan komutlar** ve **dengesiz erişimdir** (sıcak anahtar). Çözüm de makine eklemek değildir; **erişimi yaymak** ve **komutları küçük parçalara bölmektir**.

Sıcak anahtar düğüm ekleyerek hafiflemez. İsteği o tek düğüme ulaşmadan **katman katman süzmen** gerekir. Uygulama içindeki yerel önbellek (L1) okumaların çoğunu Redis'e hiç götürmeden karşılar. Kalan istekler **kopyalara (replika)** yayılır. Gerekiyorsa anahtarın kendisi **parçalanır (sharding)**. Böylece ana düğüme kalan yük azalır:

```svg
<svg viewBox="0 0 640 168" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hot key hafifletme katmanları: yerel cache, replika, sharding">
  <defs>
    <marker id="rhot-layer" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Hot key'i hafifletme katmanları</text>
  <rect x="10" y="58" width="96" height="54" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="58" y="82" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">istekler</text>
  <text x="58" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(çok)</text>
  <rect x="134" y="58" width="116" height="54" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="192" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">L1 yerel cache</text>
  <text x="192" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çoğunu keser</text>
  <rect x="278" y="58" width="116" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="336" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">replikadan oku</text>
  <text x="336" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">okuma yayılır</text>
  <rect x="422" y="58" width="116" height="54" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="480" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">sharding</text>
  <text x="480" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">anahtarı parçala</text>
  <rect x="566" y="58" width="66" height="54" rx="9" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="599" y="80" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">node</text>
  <text x="599" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">az yük</text>
  <line x1="106" y1="85" x2="132" y2="85" stroke="#5D7789" stroke-width="2" marker-end="url(#rhot-layer)"/>
  <line x1="250" y1="85" x2="276" y2="85" stroke="#5D7789" stroke-width="2" marker-end="url(#rhot-layer)"/>
  <line x1="394" y1="85" x2="420" y2="85" stroke="#5D7789" stroke-width="2" marker-end="url(#rhot-layer)"/>
  <line x1="538" y1="85" x2="564" y2="85" stroke="#5D7789" stroke-width="2" marker-end="url(#rhot-layer)"/>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her katman yükü azaltır → tek node darboğazı hafifler</text>
</svg>
```

Tıkayan komutlarda ise ayrım nettir. `KEYS pattern` tüm anahtar uzayını **tek komutta** gezer ve tek iş parçacığını uzun süre kilitler. `SCAN` (ve `SSCAN`/`HSCAN`/`ZSCAN`) bir **imleçle** küçük parçalar hâlinde tarar; sunucuyu tıkamaz. Aynı ayrım koleksiyonlarda da geçerlidir: milyonlarca üyeli bir Set'te `SMEMBERS` yerine `SSCAN` kullanılır. `RANDOMKEY` gibi örnekleme komutları tam ve tekrarsız tarama garantisi vermez. `MONITOR` da bir tarama aracı değildir; üstelik ciddi ek yük getirir:

```svg
<svg viewBox="0 0 640 196" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="KEYS tek büyük bloklayan tarama vs SCAN imleçli artımlı tarama">
  <defs>
    <marker id="rhot-scan" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">KEYS vs SCAN</text>
  <rect x="14" y="36" width="300" height="150" rx="12" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="164" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">KEYS pattern</text>
  <rect x="40" y="76" width="248" height="46" rx="8" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="164" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tüm anahtar uzayı · TEK komut</text>
  <text x="164" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">tek-thread uzun süre BLOKLANIR</text>
  <text x="164" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">diğer tüm istekler bekler</text>
  <rect x="326" y="36" width="300" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">SCAN (imleçli)</text>
  <rect x="346" y="76" width="60" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="376" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">parça</text>
  <rect x="446" y="76" width="60" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="476" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">parça</text>
  <rect x="546" y="76" width="60" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="576" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">parça</text>
  <line x1="406" y1="96" x2="444" y2="96" stroke="#5D7789" stroke-width="1.8" marker-end="url(#rhot-scan)"/>
  <line x1="506" y1="96" x2="544" y2="96" stroke="#5D7789" stroke-width="1.8" marker-end="url(#rhot-scan)"/>
  <text x="476" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">imleç (cursor) ile artımlı</text>
  <text x="476" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her çağrı kısa · bloklamaz</text>
</svg>
```

### Mülakatta ne sorulur?

(1) "6 düğüm var, darboğaz neden tek düğümde?" — Anahtar bölünemez: tek yuva, tek ana düğüm.

(2) "Sıcak anahtarı nasıl hafifletirsin?" — Yerel önbellek, kopyadan okuma ve anahtar parçalama ile. TTL'i sonsuz yapmak veya düğüm eklemek işe yaramaz.

(3) "Büyük anahtarı nasıl teşhis edersin?" — `--bigkeys` taraması ve `MEMORY USAGE` ile; yanında `SLOWLOG` ve `LATENCY`.

(4) "Üretimde `KEYS *` neden yasak?" — Tek iş parçacığını tıkar; yerine `SCAN` kullanılır.

(5) "Devasa bir Set'te `SMEMBERS` neden tehlikeli?" — O(N) tıkama ve devasa yanıt yüzünden; yerine `SSCAN` kullanılır.

(6) "Her istekte yeni bağlantı açmanın maliyeti nedir?" — TCP ve kimlik doğrulama maliyeti ile tükenen bağlantı bütçesi. Havuz ya da paylaşılan bağlantı gerekir.

### Tek cümlede

Redis tek iş parçacıklı olduğu için tek bir sıcak anahtar ya da tek bir O(N) komut tüm sunucuyu bekletebilir. Çözüm düğüm eklemek değildir; erişimi katmanlara yaymak ve taramayı `SCAN` ile parça parça yapmaktır.

# rhot-1 | İleri
## Soru
Redis Cluster'da milyonlarca istek **tek bir anahtara** gidiyor. 6 node'lu cluster'a rağmen darboğaz neden tek node'da toplanır?
## A
Cluster gelen istekleri tüm node'lara otomatik dağıttığından böyle bir darboğaz oluşmaz
🤖 Cluster VERİYİ dağıtır ama tek anahtar bölünemez; o anahtarın tüm yükü tek node'da kalır.
## B
Sıcak anahtar performans için tüm master'larda kopyalanır ve yük eşit biçimde paylaşılır
🤖 Anahtar tüm master'lara kopyalanmaz; yalnız slotunun sahibi olan node'a bağlıdır.
## C
Anahtar tek bir hash slot'a, slot da tek bir master'a aittir; anahtar bölünemez
🤖 Doğru: tek anahtar tek slota ve tek node'a düşer, cluster büyüklüğü bunu değiştirmez.
## D
Redis Cluster aslında tek node'dan ibarettir; gerçek bir veri dağıtımı söz konusu değildir
🤖 Cluster gerçek çok-node bir yapıdır; sorun dağıtımın yokluğu değil, anahtarın bölünemezliğidir.
## Doğru
C
## Açıklama
Redis Cluster **veriyi** 16384 hash slot üzerinden node'lara böler; ama **tek bir anahtar** deterministik olarak **tek bir slota**, o slot da **tek bir master node'a** düşer. Anahtar birden çok node'a **bölünemediği** için, o anahtara gelen tüm okuma/yazma yükü **tek node'da** toplanır — cluster'da kaç node olduğu fark etmez (C). Cluster yükü otomatik dengelemez (A), anahtar tüm master'lara kopyalanmaz (B) ve cluster tek node değildir (D). Bu, "node ekleyerek çözülmez" tipi bir hot key sorunudur.

# rhot-2 | İleri
## Soru
Bir hot key'in tek-node darboğazını hafifletmek için en uygun yaklaşımlardan biri nedir?
## A
Yerel önbellek ve replikadan okuma ile erişimi yaymak, gerekirse anahtarı parçalamak
🤖 Doğru: erişimi Redis'e ulaşmadan veya replikalara yayarak tek node yükü azaltılır.
## B
Anahtarın TTL'ini sonsuz yaparak değerin yeniden hesaplanmasını tamamen önlemek
🤖 TTL erişim yükünü azaltmaz; sıcak anahtar okuma trafiği yine tek node'a gider.
## C
Cluster'a daha fazla master node ekleyip slotları yeniden bölüştürmek
🤖 Node eklemek tek bir anahtarı bölmez; o anahtarın yükü hâlâ tek node'da kalır.
## D
Üretimde `KEYS *` çalıştırıp sıcak anahtarları listeleyip elle taşımaya çalışmak
🤖 `KEYS *` tüm sunucuyu bloklar; darboğazı çözmez, tam tersine kötüleştirir.
## Doğru
A
## Açıklama
Hot key darboğazının çözümü **ölçek eklemek değil, erişimi yaymak/azaltmak**tır: (1) **uygulama-içi yerel önbellek** (near/L1 cache) ile çok okunan değeri Redis'e hiç gitmeden karşılamak, (2) okuma ağırlıklıysa **replikalardan okuma** ile yükü dağıtmak, (3) mümkünse anahtarı **parçalamak** (ör. sayaç için `sayac:{shard}` çoklu anahtar + toplama) (A). Sonsuz TTL (B) erişim yükünü etkilemez; **node eklemek** tek anahtarı bölmediğinden çözmez (C); üretimde `KEYS *` (D) sunucuyu bloklayarak durumu kötüleştirir.

# rhot-3 | İleri
## Soru
Belleği şişiren **büyük anahtarları** (big keys) teşhis etmek için hangi araç/komut doğrudan işe yarar?
## A
`DBSIZE` ile toplam anahtar sayısını okuyup ortalama anahtar boyutunu hesaplamak
🤖 `DBSIZE` yalnız anahtar sayısını verir; ortalama, tek bir devasa anahtarı gizler.
## B
`INFO memory` çıktısındaki toplam bellek kullanımına bakıp anahtarı çıkarsamak
🤖 `INFO memory` yalnız toplamı gösterir; hangi anahtarın şiştiğini söylemez.
## C
`SUBSCRIBE` ile anahtar değişimlerini dinleyip büyüyen anahtarları yakalamak
🤖 `SUBSCRIBE` bir Pub/Sub aboneliğidir; anahtar boyutunu hiç raporlamaz.
## D
`redis-cli --bigkeys` taraması ve şüpheli anahtarda `MEMORY USAGE <key>` ölçümü
🤖 Doğru: `--bigkeys` en büyükleri örnekler, `MEMORY USAGE` tek anahtarın maliyetini ölçer.
## Doğru
D
## Açıklama
Büyük/sıcak anahtar teşhisinin standart araçları: **`redis-cli --bigkeys`** her tipteki en büyük anahtarları örnekleyerek tarar; şüpheli bir anahtarın gerçek bellek maliyeti **`MEMORY USAGE <key>`** ile ölçülür; yavaş çalışan komutlar **`SLOWLOG GET`** ile, gecikme kaynakları ise **`LATENCY`** (latency monitor) ile bulunur (D). `DBSIZE` (A) yalnız anahtar sayısını verir ve ortalama hesabı tek bir devasa anahtarı gizler; `INFO memory` (B) toplam bellek kullanımını gösterir ama hangi anahtarın şiştiğini söylemez; `SUBSCRIBE` (C) bir Pub/Sub aboneliğidir, boyut raporlamaz.

# rhot-4 | İleri
## Soru
Üretimde büyük bir veri setinde anahtarları taramak için `KEYS pattern` yerine ne kullanılmalı ve neden?
## A
`KEYS pattern` üretimde güvenlidir; büyük veri setinde bile performans farkı yaratmaz
🤖 `KEYS` tüm anahtar uzayını tek seferde gezip tek-thread'i bloklar; üretimde tehlikelidir.
## B
`SCAN` (ve `SSCAN`/`HSCAN`/`ZSCAN`): imleçle artımlı çalışır ve sunucuyu bloklamaz
🤖 Doğru: `SCAN` parça parça tarar ve diğer istemcileri bekletmez.
## C
`RANDOMKEY`'i tekrar tekrar çağırıp anahtar uzayını örnekleyerek listeyi çıkarmak
🤖 `RANDOMKEY` örnekleme yapar; tam ve tekrarsız bir tarama garantisi vermez.
## D
`MONITOR` ile canlı komut akışını izleyip geçen anahtarları toplayarak listelemek
🤖 `MONITOR` komut trafiğini gösterir, anahtar taramaz; ayrıca ciddi ek yük getirir.
## Doğru
B
## Açıklama
`KEYS pattern`, eşleşen tüm anahtarları bulmak için **tüm anahtar uzayını tek bir komutta** gezer; Redis tek-thread olduğundan bu, büyük veritabanında sunucuyu **uzun süre bloklar** ve diğer tüm istemciler bekler. Doğru araç **`SCAN`** (ve tip-özel `SSCAN`/`HSCAN`/`ZSCAN`)'dir: bir **imleç (cursor)** ile küçük parçalar hâlinde **artımlı** tarar, her çağrı kısa sürer ve sunucuyu bloklamaz (B). `RANDOMKEY` yalnız örnekleme yapar, tam ve tekrarsız tarama garantisi vermez (C); `MONITOR` bir tarama aracı olmayıp ciddi ek yük getirir (D); `KEYS`'in "güvenli/farksız" olduğu iddiası (A) yanlıştır.

# rhot-5 | İleri
## Soru
Milyonlarca üyeli bir Set üzerinde `SMEMBERS` çağırmak neden tehlikelidir?
## A
Hiçbir tehlikesi yoktur; `SMEMBERS` üye sayısından bağımsız olarak sabit sürede döner
🤖 Devasa Set'te `SMEMBERS` O(N) iş yapıp tek-thread'i bloklar; güvenli değildir.
## B
Set'in tüm üyelerini okuduktan sonra koleksiyondan kalıcı olarak siler
🤖 `SMEMBERS` bir okuma komutudur, silmez; tehlike bloklamada ve büyük yanıttadır.
## C
Tek komutta O(N) iş yapıp tek-thread'i bloklar ve devasa bir yanıt üretir
🤖 Doğru: büyük Set'te `SMEMBERS` sunucuyu bloklar ve ağır bir yanıt oluşturur.
## D
`SMEMBERS` yalnızca Redis Cluster'da çalışır, tek node kurulumunda hata döndürür
🤖 `SMEMBERS` her modda çalışır; sorun çalışmaması değil, bloklaması ve yanıt boyutudur.
## Doğru
C
## Açıklama
`SMEMBERS`, bir Set'in **tüm üyelerini tek komutta** döndürür ve maliyeti **O(N)**'dir. Milyonlarca üyeli bir Set'te bu, Redis'in tek-thread'ini **uzun süre bloklar** (diğer tüm istekler bekler) ve ayrıca **devasa bir yanıt** üreterek ağı ve istemci belleğini zorlar (C). Doğrusu **`SSCAN`** ile üyeleri **imleçle parça parça** okumaktır. `SMEMBERS` bir okuma komutudur, silmez (B); büyük Set'te "güvenli" değildir (A); Cluster'a özgü de değildir (D).

# rhot-6 | İleri
## Soru
Yüksek eşzamanlılıkta her istekte yeni bir Redis bağlantısı açıp kapatmak (connection churn) neye yol açar ve doğru yaklaşım nedir?
## A
TCP el sıkışması ve kimlik doğrulama maliyeti gecikme yaratır; bağlantı yeniden kullanılmalıdır
🤖 Doğru: bağlantı kurma maliyeti pahalıdır; havuz veya paylaşılan bağlantı bunu amorti eder.
## B
Hiçbir sorun olmaz; Redis sınırsız sayıda bağlantıyı bedelsiz biçimde karşılayabilir
🤖 Bağlantı kurmak TCP ve auth maliyetlidir, sunucunun bağlantı bütçesi de sınırlıdır.
## C
Sorun yoktur; bağlantı başına ayrı soket açmak tek-thread'li Redis'i paralelleştirir
🤖 Yeni bağlantı Redis'i paralelleştirmez; komutlar yine tek thread'de sırayla işlenir.
## D
Bağlantı açmak bedavadır; Redis'te asıl darboğaz her zaman disk giriş/çıkışıdır
🤖 Bağlantı açmak bedava değildir ve Redis bellek odaklıdır; darboğaz "her zaman disk" değildir.
## Doğru
A
## Açıklama
Her istekte bağlantı açıp kapatmak (**connection churn**), her seferinde **TCP el sıkışması**, varsa **`AUTH`/handshake** ve soket kurulum maliyeti ödetir; yüksek eşzamanlılıkta bu **gecikmeyi** artırır, sunucunun sınırlı bağlantı bütçesini ve dosya tanıtıcılarını tüketir. Doğru yaklaşım bağlantıyı **yeniden kullanmak**tır: klasik istemcilerde (Jedis) bir **bağlantı havuzu**, Lettuce'ta ise **paylaşılan asenkron/thread-safe bağlantı** (A). Bağlantı açmak bedelsiz değildir (B, D); yeni bağlantı açmak Redis'i paralelleştirmez de (C) — komutlar yine tek thread'de sırayla işlenir.
