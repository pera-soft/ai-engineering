---
slug: redis-nedir-single-thread
baslik: "Redis Nedir: Bellek-İçi Veri Yapısı Sunucusu ve Tek-Thread Komut İşleme"
kademe: Temel
sira: 155
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir web sitesi düşün: her sayfa açılışında veritabanı (database) diskteki dosyalardan veri okur. Disk yavaştır. Aynı soru binlerce kez sorulunca sistem tıkanır.

**Bellek içi (in-memory)** demek, verinin bilgisayarın hızlı geçici belleğinde (RAM) tutulması demektir — diskte değil. RAM'den okumak diskten okumaya göre binlerce kat hızlıdır. **Redis**, veriyi RAM'de tutan bir depodur: sık sorulan soruların cevaplarını ona koyarsın, veritabanı rahatlar.

**Anahtar-değer (key-value) deposu** ne demek? Bir sözlük gibi: her verinin bir **adı (anahtar/key)** vardır — `kullanici:42:ad` gibi; o adı verince **değerini (value)** geri alırsın — `"Ali"` gibi. Arada tablo, sütun, SQL yoktur; ad ver, değeri al.

Redis bundan biraz fazlasıdır: **veri yapısı sunucusu (data structure server)**. Yalnız düz metin değil, liste, küme, sıralı küme gibi hazır yapıları da doğrudan sunar. Tipik kullanım alanları: **önbellek (cache)**, oturum (session) deposu, hız sınırlama (rate limit), skor tablosu (leaderboard), pub/sub ve olay akışı.

### Temel kavramlar

- **Bellek içi (in-memory)**: veri RAM'de durur. Hızlı ama sınırlı ve uçucudur (kalıcılık için RDB/AOF dosyaları vardır, ama Redis'in evi RAM'dir).
- **Önbellek (cache)**: pahalı bir hesabın sonucunu bir kenara yazıp aynı soru gelince tekrar hesaplamadan oradan vermek.
- **İş parçacığı (thread)**: bir programın aynı anda yürüyen "kolları". Çok iş parçacığı çok iş demektir; ama araya girme, yani yarış (race) riski getirir.
- **Atomik**: bölünmez. Bir komut ya tamamen olur ya hiç olmaz; yarısı asla görünmez.
- **O(1) / O(N)**: bir komutun süresinin veriyle nasıl büyüdüğü. O(1) veri ne kadar büyürse büyüsün sabit süre, O(N) ise veri kadar süre demektir — tehlikeli olan budur.

### Nasıl çalışır?

Redis'in imza kararı: **komutlar tek bir iş parçacığında, sıra sıra işlenir.** Gelen komutlar kuyruğa girer, teker teker yürütülür.

Bu bir kayıp gibi görünür ama büyük bir hediye verir: **her komut atomiktir.** İki komut asla iç içe geçmez, bu yüzden `INCR sayac` gibi bir sayaç artırmada kilit (lock) almana gerek kalmaz — yarış tasarımdan silinmiştir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek thread komutları sırayla işler">
  <defs>
    <marker id="rnedir-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek thread: komutlar teker teker, atomik</text>
  <rect x="24" y="58" width="70" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="59" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">GET</text>
  <rect x="24" y="100" width="70" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="59" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">INCR</text>
  <rect x="24" y="142" width="70" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="59" y="164" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">SET</text>
  <text x="59" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gelen komutlar</text>
  <line x1="98" y1="117" x2="256" y2="117" stroke="#186399" stroke-width="1.6" marker-end="url(#rnedir-ok)"/>
  <rect x="262" y="82" width="130" height="70" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="327" y="112" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">TEK event loop</text>
  <text x="327" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sırayla işler</text>
  <line x1="394" y1="117" x2="470" y2="117" stroke="#186399" stroke-width="1.6" marker-end="url(#rnedir-ok)"/>
  <rect x="476" y="90" width="140" height="54" rx="10" fill="#FBEDD9" stroke="#B26B0E" stroke-width="1.4"/>
  <text x="546" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">KEYS * gibi O(N)</text>
  <text x="546" y="130" text-anchor="middle" font-size="10.5" fill="#B26B0E" font-family="system-ui">herkesi bloke eder</text>
</svg>
```

Redis'in hızı iki kaynaktan gelir: veri RAM'dedir ve çoğu komut O(1)'dir. RAM erişimi diske göre binlerce kat kısa gecikmelidir; sonuç, milisaniyenin altında yanıt süresidir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Redis neden hızlı: bellek erişimi diske göre çok daha kısa gecikmeli">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Neden hızlı: bellek + O(1) komutlar</text>
  <text x="24" y="58" font-size="11" fill="#5D7789" font-family="system-ui">Erişim gecikmesi (yaklaşık, log ölçek):</text>
  <rect x="150" y="72" width="60" height="26" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="24" y="90" font-size="11.5" fill="#1C3247" font-family="system-ui">Bellek (RAM)</text>
  <text x="220" y="90" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">~100 ns</text>
  <rect x="150" y="106" width="200" height="26" rx="5" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="24" y="124" font-size="11.5" fill="#1C3247" font-family="system-ui">SSD</text>
  <text x="360" y="124" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">~100 µs (~1000x)</text>
  <rect x="150" y="140" width="330" height="26" rx="5" fill="#F6E4E4" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="24" y="158" font-size="11.5" fill="#1C3247" font-family="system-ui">Disk (HDD)</text>
  <text x="490" y="158" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">~10 ms (~100000x)</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Veri bellekte + çoğu komut O(1) → sub-milisaniye yanıt</text>
</svg>
```

Peki tek kol binlerce kullanıcıya nasıl yetişir? Cevap **G/Ç çoğullamadır (I/O multiplexing)**. Tek bir olay döngüsü (event loop), işletim sisteminin `epoll`/`kqueue` mekanizmasıyla binlerce soketi aynı anda gözler. Hangisi hazırsa onu işler; boşta bekleyerek zaman harcamaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek event loop epoll ile binlerce bağlantıyı çoğullar">
  <defs>
    <marker id="rnedir-mux-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">I/O çoğullama: tek loop, binlerce soket</text>
  <rect x="26" y="54" width="120" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="86" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağlantı 1</text>
  <rect x="26" y="90" width="120" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="86" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağlantı 2</text>
  <rect x="26" y="126" width="120" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="86" y="144" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağlantı 3</text>
  <text x="86" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">… binlerce soket</text>
  <line x1="146" y1="67" x2="252" y2="100" stroke="#6B3FA0" stroke-width="1.4" marker-end="url(#rnedir-mux-ok)"/>
  <line x1="146" y1="103" x2="252" y2="108" stroke="#6B3FA0" stroke-width="1.4" marker-end="url(#rnedir-mux-ok)"/>
  <line x1="146" y1="139" x2="252" y2="116" stroke="#6B3FA0" stroke-width="1.4" marker-end="url(#rnedir-mux-ok)"/>
  <rect x="258" y="82" width="150" height="56" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="333" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">epoll / kqueue</text>
  <text x="333" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hazır soketi seç</text>
  <line x1="408" y1="110" x2="486" y2="110" stroke="#6B3FA0" stroke-width="1.6" marker-end="url(#rnedir-mux-ok)"/>
  <rect x="492" y="84" width="122" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="553" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">tek event loop</text>
  <text x="553" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">işler, boşta beklemez</text>
</svg>
```

**"Redis tamamen tek thread'dir" cümlesi bir abartıdır.** Doğrusu: **komut yürütme** tek thread'dedir. Yanında başka iş parçacıkları çalışır:

- **G/Ç iş parçacıkları** (`io-threads`, Redis 6 ve sonrası): yalnız ağ soketi okuma/yazma ve protokol ayrıştırma paralel çalışır. Komut mantığı yine tek iş parçacığındadır; atomiklik bozulmaz.
- **Arka plan thread'leri**: `UNLINK`/lazy-free ile büyük anahtarın belleğini boşaltma, AOF fsync gibi işler.
- **Fork edilen süreç**: RDB anlık görüntüsü (snapshot) alınırken.

Bunun bedeli nettir: **yavaş bir komut herkesi bekletir.** Üretimde `KEYS *` çalıştırdığını düşün; bu, tüm anahtar uzayını tarayan O(N) bir komuttur. Tarama bitene kadar bütün istekler kuyrukta bekler. Milyonlarca anahtarda bu, saniyelerce donma ve bir zaman aşımı fırtınası demektir. Onun yerine imleçle (cursor) parça parça ilerleyen `SCAN` kullanılır.

Aynı tehlike büyük koleksiyonlarda `HGETALL`, `SMEMBERS` ve `LRANGE 0 -1` komutlarında da vardır. Devasa bir anahtarı `DEL` ile silmek de aynı riski taşır; onun yerine `UNLINK` kullanılır. Buna karşılık **boru hattı (pipelining)** — komutları tek pakette toplamak — ağ turlarını (RTT) azalttığı için iş hacmini **artırır**.

Tek komut iş parçacığı tek bir CPU çekirdeğini doyurur. Çok çekirdekli bir sunucuyu kullanmanın yolu "çok iş parçacığı düğmesi" değildir; böyle bir ayar zaten yoktur. Yol **yatay bölmedir (sharding)**: aynı makinede birden çok Redis örneği çalıştırmak ya da **Redis Cluster** ile veriyi yuvalara bölmek.

2026 manzarası şöyle. Lisans dönüşünden sonra iki ürün yan yana duruyor: **Redis 8** (yeniden açık kaynak, AGPL) ve Linux Foundation çatısındaki BSD lisanslı çatal **Valkey 8**. API'leri büyük ölçüde uyumludur. Valkey, G/Ç tarafında daha ileri paralellik iddia ediyor; ama komutların mantıksal olarak sırayla işlenmesi ikisinde de esastır. Satıcı "X kat hızlı" iddialarını kendi yükünle ölçmeden kabul etme.

### Mülakatta ne sorulur?

- "Redis nedir?" → bellek içi anahtar-değer deposu **ve** veri yapısı sunucusu; ilişkisel veritabanı değil, salt mesaj kuyruğu değil, ORM hiç değil.
- "Tek iş parçacıklı mı?" → **Komut yürütme** tek iş parçacığındadır. G/Ç iş parçacıkları ve arka plan işleri ayrıca vardır. Sonuç: atomiklik bedavaya gelir.
- "`io-threads` neyi paralelleştirir?" → yalnız ağ okuma/yazma ve protokol ayrıştırma.
- "Üretimde `KEYS *` neden yasak?" → O(N) bir komuttur; tek komut iş parçacığını tıkar ve herkes bekler. Yerine `SCAN` kullan.
- "İş hacmini (throughput) en çok ne düşürür?" → Büyük koleksiyonlarda çalışan O(N) komutlar. Boru hattı (pipelining) ise tam tersine iş hacmini artırır.
- "Çok çekirdek nasıl kullanılır?" → birden çok örnek / Redis Cluster ile sharding.

### Tek cümlede

Redis, veriyi RAM'de tutan ve komutları tek sırada yürüten bir veri yapısı sunucusudur. Bu sadelik ona atomikliği ve mikrosaniyelik gecikmeyi bedavaya verir. Karşılığında tek bir yavaş komutun herkesi bekletmesi riskini yükler.

# rnedir-1 | Temel
## Soru
Redis'i en doğru tanımlayan ifade hangisidir?
## A
Verileri diskteki tablolarda tutan, SQL ile sorgulanan ilişkisel (relational) bir veritabanıdır
🤖 Redis veriyi öncelikle bellekte tutar; tablo, sütun ve SQL modeli yoktur.
## B
Yalnızca mesajları sıraya dizip tüketicilere dağıtan bir mesaj kuyruğudur (message queue)
🤖 Pub/Sub ve Stream sunar ama kimliği bu değildir; asıl işi veri saklamaktır.
## C
Bellek içi (in-memory) anahtar-değer deposu ve veri yapısı sunucusudur
🤖 Doğru: veriyi RAM'de ad-değer olarak tutar, ayrıca hazır veri yapıları sunar.
## D
Java nesnelerini veritabanı tablolarına eşleyen bir ORM kütüphanesidir
🤖 ORM bir eşleme katmanıdır; Redis ise verinin kendisini saklayan bir sunucudur.
## Doğru
C
## Açıklama
Redis'in iki tanımlayıcı özelliği vardır: veri **bellek içinde (in-memory)** tutulur — bu yüzden çok hızlıdır — ve yalnız string değil **veri yapıları** (Hash, List, Set, Sorted Set, Stream…) sunar; bu yüzden ona "data structure server" denir. İlişkisel değildir, SQL ve tablo modeli yoktur (A); Pub/Sub ve Stream sunsa da salt bir kuyruk değildir (B); nesne-tablo eşlemesi yapan bir ORM ile hiç ilgisi yoktur (D). Kalıcılık için RDB/AOF seçenekleri vardır ama birincil çalışma yeri bellektir.

# rnedir-2 | Temel
## Soru
Redis'in komut işleme modeli hakkında hangisi doğrudur?
## A
Komutlar tek bir iş parçacığında sırayla işlenir; bu yüzden her komut atomiktir
🤖 Doğru: tek sıra yürütme, iki komutun iç içe geçmesini olanaksız kılar.
## B
Her komut ayrı bir iş parçacığına dağıtılır ve çekirdekler arasında tam paralel yürütülür
🤖 Komut yürütme paralel değildir; bu yüzden iç içe geçme de olmaz.
## C
Her komut için önce açık bir kilit (lock) alınır, iş bitince serbest bırakılır
🤖 Tek sıra yürütmede kilide gerek yoktur; atomiklik zaten garantidir.
## D
Gelen her istemci isteği için ayrı bir süreç (process) fork edilerek yürütülür
🤖 Fork yalnız RDB anlık görüntüsü gibi işlerde olur; komut yolunda kullanılmaz.
## Doğru
A
## Açıklama
Redis komutları **tek thread'li olay döngüsünde** sırayla işler. Bunun doğrudan sonucu **atomikliktir**: iki komut asla iç içe çalışmaz, bu yüzden `INCR`, `SETNX`, `GETDEL` gibi işlemler yarış (race) olmadan tek adımda tamamlanır ve açık kilit gerekmez (C yanlış). Komut yürütmede paralellik yoktur (B); süreç fork'lama yalnız RDB anlık görüntüsü gibi arka plan işlerinde kullanılır, komut yolunda değil (D). Bu model, "yarım işlem görünmesin" garantisini bedavaya verir.

# rnedir-3 | Orta
## Soru
Redis 6 ile gelen "threaded I/O" (`io-threads`) ayarı tam olarak neyi paralelleştirir?
## A
Komut mantığının kendisini, yani veri yapısına dokunan yürütmeyi birden çok çekirdekte
🤖 Komut yürütme tek thread'de kalır; `io-threads` bunu değiştirmez.
## B
Lua script'lerinin ve MULTI/EXEC işlemlerinin yürütülmesini birden çok thread'e
🤖 Lua ve transaction da tek thread'de atomik çalışır; paralelleşmez.
## C
Sorted Set ve Hash gibi veri yapıları üzerindeki işlemleri arka plan thread'lerine
🤖 Veri yapısı işlemleri komut yürütmenin parçasıdır; tek thread'de kalır.
## D
Ağ soketi okuma/yazmayı ve protokol ayrıştırmayı; komut mantığı tek thread'de kalır
🤖 Doğru: yalnız I/O katmanı thread'lere dağıtılır, yürütme sıralı kalır.
## Doğru
D
## Açıklama
`io-threads` ayarı yalnız **ağ katmanını** — soket okuma/yazma ve RESP protokol ayrıştırma — birden çok thread'e dağıtır; böylece çok sayıda bağlantıda I/O darboğazı hafifler. Ancak **komut yürütme**, yani veri yapısına dokunan asıl mantık, tek thread'de kalır (A, B, C yanlış); Lua script'leri ve MULTI/EXEC de aynı tek sırada atomik çalışır. Böylece atomiklik garantisi korunur. 2026'da Valkey 8, I/O ve bazı yardımcı işleri daha ileri asenkron modele taşıyan iyileştirmeler getirdi; yine de komutların mantıksal sıralılığı esas alınmaya devam ediyor.

# rnedir-4 | Orta
## Soru
Prod bir Redis'te `KEYS *` çalıştırmanın asıl tehlikesi nedir?
## A
Hiçbir tehlikesi yoktur; prod ortamda anahtarları listelemek için resmî olarak önerilen komut budur
🤖 Tam tersi; prod'da yasaklanması gereken klasik bir hatadır, önerilen `SCAN`'dir.
## B
O(N) olarak tüm anahtar uzayını tarar ve tek komut thread'ini bloke eder
🤖 Doğru: tarama bitene dek başka komut işlenemez, gecikmeler patlar.
## C
Yavaş çalışır ama komutlar tek thread'de sıralandığı için başka isteği geciktirmez
🤖 Aksine; tek sıra olduğu İÇİN tarama sürerken herkes kuyrukta bekler.
## D
Bellek taşmasına (OOM) yol açar ama komutun kendisi çok hızlı tamamlanır
🤖 Sorun bellek değil bloklama; komut O(N) süre boyunca thread'i tutar.
## Doğru
B
## Açıklama
`KEYS *` **O(N)**'dir: tüm anahtar uzayını gezer. Redis komutları tek sırada işlediği için tarama sürdükçe **başka hiçbir komut işlenemez** — milyonlarca anahtarda bu, saniyelerce süren bir donma ve zaman aşımı fırtınası demektir. Tek thread olması durumu hafifletmez, tam tersine kötüleştirir (C yanlış); sorun bellek taşması da değildir (D). Doğru yol, imleç (cursor) tabanlı ve bloke etmeyen **`SCAN`** komutudur. Aynı mantık büyük koleksiyonlarda `SMEMBERS`, `HGETALL` ve `FLUSHALL` gibi O(N) işlemler için de geçerlidir.

# rnedir-5 | İleri
## Soru
Tek sıra komut yürütme göz önüne alınınca, aşağıdaki iş yüklerinden hangisi Redis'in toplam işlem hacmini (throughput) EN ÇOK düşürür?
## A
Çok sayıda küçük `GET`/`SET` komutunun saniyede binlerce kez sürekli akması
🤖 Bunlar O(1)'dir ve mikrosaniyede biter; ideal Redis yüküdür.
## B
Komutları tek pakette toplayıp tek turda gönderen pipelining kullanımı
🤖 Pipelining ağ turlarını (RTT) azaltıp throughput'u ARTIRIR, düşürmez.
## C
Büyük koleksiyonlarda `SMEMBERS`, `HGETALL`, `LRANGE 0 -1` gibi komutlar
🤖 Doğru: her biri O(N)'dir, tek thread'i uzun tutar, diğerleri bekler.
## D
Yazılan her cache anahtarına ayrıca birer `EXPIRE` ile TTL atanması
🤖 TTL atamak O(1)'dir; süre dolumu arka planda örneklemeyle yürür.
## Doğru
C
## Açıklama
Tek sıra yürütmede darboğaz, **tek bir komutun thread'i ne kadar süre tuttuğudur.** Büyük koleksiyonlarda **O(N)** komutlar (`SMEMBERS`, `HGETALL`, `LRANGE 0 -1`, geniş aralıklı `ZRANGE`) yürütülürken başka hiçbir istek işlenemez; bu yüzden gecikme ve işlem hacmi birlikte bozulur. Küçük O(1) işlemler (A) idealdir; pipelining (B) ağ turlarını birleştirerek throughput'u **artırır**; TTL atamak (D) O(1)'dir ve süre dolumu arka planda örneklemeyle yürür. Çözüm: büyük koleksiyonları parçalamak ve `HSCAN`/`SSCAN`/`ZSCAN` ile parçalı okumak.

# rnedir-6 | İleri
## Soru
Komut yürütme tek thread iken, çok çekirdekli (multi-core) bir sunucunun çekirdeklerini nasıl daha iyi kullanırsın?
## A
Aynı makinede birden çok Redis örneği çalıştırmak veya Redis Cluster ile sharding
🤖 Doğru: her örnek bir çekirdeği doyurur; yük yatay olarak dağılır.
## B
`io-threads` değerini 1'e düşürüp tüm ağ işini tek bir thread'e toplamak
🤖 Bu I/O paralelliğini azaltır; çekirdek kullanımını artırmaz, düşürür.
## C
Komut yürütmeyi birden çok thread'e açan `multi-threaded-exec` ayarını yapılandırmadan etkinleştirmek
🤖 Böyle bir ayar yoktur; komut yürütme tasarımı gereği tek sıradadır.
## D
`maxmemory` değerini 0 yapıp süreç üzerindeki bellek sınırını tamamen kaldırmak
🤖 Bu bellek sınırıyla ilgilidir; CPU çekirdeklerinin kullanımını etkilemez.
## Doğru
A
## Açıklama
Tek komut thread'i tek çekirdeği doyurur; bir Redis örneği tüm çekirdekleri kullanamaz. Standart çözüm **yatay bölme (sharding)**: aynı makinede birden çok örnek çalıştırmak veya **Redis Cluster** ile veriyi slot'lara bölüp yükü çekirdeklere yaymak. Komut yürütmeyi çok thread'e açan bir düğme yoktur (C yanlış); `io-threads` yalnız ağ katmanı içindir ve değerini düşürmek işleri kötüleştirir (B); `maxmemory` bellek tavanıdır, CPU ile ilgisizdir (D). 2026'da Valkey çekirdek başına ölçeklenmede iyileştirmeler sunsa da mimari yaklaşım hâlâ sharding'dir.
