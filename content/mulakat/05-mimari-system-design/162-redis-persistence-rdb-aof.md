---
slug: redis-persistence-rdb-aof
baslik: "Redis Persistence: RDB Snapshot, AOF ve fork+COW Bellek Maliyeti"
kademe: İleri
sira: 162
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Redis veriyi **bellekte (RAM)** tutar; bu yüzden çok hızlıdır ama elektrik kesilir ya da süreç çökerse bellek boşalır. **Kalıcılık (persistence)**, bellekteki veriyi **diske** de yazıp yeniden başlatmadan sonra geri yükleyebilmektir. Redis bunu iki farklı yolla yapar ve mülakatta sorulan hep aynı takastır: **ne kadar veri kaybını göze alıyorsun, karşılığında ne kadar hız istiyorsun?**

### Temel kavramlar

- **Anlık görüntü (snapshot / RDB)**: Verinin belirli bir andaki fotoğrafı, yani **noktasal (point-in-time)** kopya. Redis bu fotoğrafı çekerken gelen yazmaları **durdurmaz**. İşi bir iş parçacığı (thread) değil, `fork()` ile yaratılan ayrı bir **çocuk süreç** yapar. Yeniden yükleme hızlıdır; ama iki fotoğraf **arasında** gelen yazılar çökmede kaybolur.
- **fork() ve copy-on-write (COW / yazarken-kopyala)**: Çocuk süreç doğduğunda ana süreçle aynı bellek sayfalarını **paylaşır**. Ek bellek maliyeti başta sıfıra yakındır. Ama ana süreç bir sayfayı **değiştirince** işletim sistemi o sayfanın kopyasını çıkarır. Yazma yoğunsa çok sayıda sayfa kopyalanır. O anda fiziksel bellek, **tepe noktada veri setinin ~2 katına** yaklaşabilir. Bu şişme geçicidir ve yalnız fork süresi boyunca yaşanır; yine de kapasite planında hesaba katılmalıdır.
- **AOF (Append-Only File — sona-ekli dosya)**: Her yazma komutu bir günlük dosyasının sonuna eklenir; yeniden başlarken bu komutlar sırayla tekrar oynatılıp veri kurulur.
- **fsync ve `appendfsync`**: `fsync`, işletim sisteminin tampondaki veriyi gerçekten diske bastırmasıdır. Üç ayar vardır. **`always`** her komutta fsync yapar: kayıp ~0, ama en yavaş seçenek. **`everysec`** saniyede bir yapar: çökmede en fazla ~1 saniyelik yazı kaybı olur; dengeli ve yaygın varsayılan budur. **`no`** ise kararı tümüyle işletim sistemine bırakır: en hızlı ama **en riskli** seçenek. "İşletim sistemi nasılsa optimize eder" diye bunu güvenli sanmak klasik hatadır.
- **AOF yeniden yazımı (rewrite)**: Dosya sürekli büyüdüğü için, mevcut veriyi üretecek **en az komut kümesi** arka planda yeni bir dosyaya yazılır (bu iş de fork ile yapılır). Bu bir gzip sıkıştırması değil, **mantıksal sadeleştirmedir**. Kalıcılığı kapatmaz ve RDB'ye geçiş anlamına gelmez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RDB snapshot ve AOF log, fork COW">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki kalıcılık mekanizması</text>
  <rect x="28" y="44" width="278" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">RDB (snapshot)</text>
  <text x="167" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">fork() + copy-on-write</text>
  <text x="167" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">noktasal kopya, hızlı yükleme</text>
  <text x="167" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">snapshot arası yazılar</text>
  <text x="167" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">çökmede kaybolur</text>
  <text x="167" y="172" text-anchor="middle" font-size="11" fill="#B4472B" font-family="system-ui">fork'ta bellek ~2x'e çıkabilir</text>
  <rect x="334" y="44" width="278" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">AOF (append-only)</text>
  <text x="473" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">her yazma komutu loglanır</text>
  <text x="473" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">always / everysec / no</text>
  <text x="473" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">everysec: ~1 sn kayıp riski</text>
  <text x="473" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">rewrite ile dosya küçülür</text>
  <text x="473" y="172" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">düşük veri kaybı</text>
</svg>
```

### Nasıl çalışır?

Kritik ayrım kullanım amacındadır. Redis'i **önbellek** olarak kullanıyorsan asıl veri zaten başka bir yerde durur; hafif RDB yetebilir. **Birincil veri deposu** olarak kullanıyorsan veri **tek kaynaktır** ve kaybı geri alınamaz. Bu durumda üçlü şarttır: düşük kayıplı **AOF** (`everysec`; dayanıklılık kritikse `always`), **kopyalama (replication)** ve **düzenli yedek**. Yalnız RDB'ye güvenmek, iki anlık görüntü arasında gelen yazıları çökmede kaybettirir. "RAM yeter, veri sonra nasılsa hesaplanır" varsayımı da birincil depoda geçerli değildir.

Bellek şişmesinin sebebi copy-on-write'ın işleyişidir. Fork anında ana ve çocuk süreç sayfaları **paylaşır**, yani bellek ~1x'tir. Ana süreç yazdıkça değişen her sayfa ayrıca kopyalanır. Yazma yoğunsa anlık görüntü ya da yeniden yazım süresince neredeyse tüm veri seti kopyalanabilir ve fiziksel bellek ~2x'e yaklaşır. Dikkat: bunun sebebi RDB dosyasının belleğe geri yüklenmesi **değildir**; Redis'in her anahtarı ikişer kopya tutması da **değildir**.

```svg
<svg viewBox="0 0 640 186" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="fork copy-on-write bellek neden iki kata çıkar">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">fork() + COW: bellek neden ~2x'e çıkar</text>
  <line x1="70" y1="150" x2="570" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="72" y="80" width="80" height="70" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="112" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">paylaşılan</text>
  <text x="112" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">fork anı</text>
  <text x="112" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~1x</text>
  <rect x="280" y="80" width="80" height="70" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <rect x="280" y="48" width="80" height="32" fill="#FBEDE9" stroke="#B4472B" stroke-width="1.4"/>
  <text x="320" y="68" text-anchor="middle" font-size="10.5" fill="#B4472B" font-family="system-ui">+kopya</text>
  <text x="320" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">yazılar sürerken</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~1.4x</text>
  <rect x="488" y="80" width="80" height="70" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <rect x="488" y="18" width="80" height="62" fill="#FBEDE9" stroke="#B4472B" stroke-width="1.4"/>
  <text x="528" y="54" text-anchor="middle" font-size="10.5" fill="#B4472B" font-family="system-ui">kopyalanan</text>
  <text x="528" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">yoğun yazıda</text>
  <text x="528" y="182" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">~2x tepe</text>
</svg>
```

`appendfsync` ayarı ise dayanıklılık ile performans arasında bir kaydırıcıdır. `always` en dayanıklı ama en yavaş uçtur; `no` en hızlı ama en riskli uçtur; `everysec` ikisinin dengeli orta noktasıdır. Hangi ucu seçtiğin tek bir soruya bağlıdır: çökmede kaç saniyelik yazıyı kaybetmeyi kabul ediyorsun?

```svg
<svg viewBox="0 0 640 150" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="appendfsync politikaları dayanıklılık performans dengesi">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">appendfsync: dayanıklılık ↔ performans</text>
  <rect x="30" y="46" width="180" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">always</text>
  <text x="120" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kayıp ~0 · en yavaş</text>
  <rect x="230" y="46" width="180" height="58" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">everysec</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~1 sn kayıp · dengeli</text>
  <rect x="430" y="46" width="180" height="58" rx="9" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="520" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">no</text>
  <text x="520" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">OS'e bağlı · en hızlı/riskli</text>
  <text x="70" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">◀ daha dayanıklı</text>
  <text x="570" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">daha hızlı ▶</text>
</svg>
```

Üçüncü bir seçenek ikisini birleştirir: **hibrit kalıcılık** (`aof-use-rdb-preamble yes`). Burada AOF dosyasının **başına** veri setinin RDB biçiminde kompakt bir **tabanı (preamble)** yazılır. O noktadan **sonrasına** ise yeni yazma komutları AOF satırları olarak eklenir. Yeniden yüklemede verinin büyük kısmı hızlıca RDB olarak okunur; bu, komutları tek tek oynatmaktan çok daha hızlıdır. Son değişiklikler ise AOF sayesinde düşük kayıpla kurtarılır. Bu mod, saf RDB'nin hızıyla saf AOF'un dayanıklılığını birleştirir ve modern varsayılandır. İki sunucuya birden yazmakla (kopyalama / yüksek erişilebilirlik) karıştırılmamalıdır.

### Mülakatta ne sorulur?

(1) "Anlık görüntünün tutarlılığı nasıl sağlanır?" — Yazmalar durdurulmaz. İşi `fork()` ile açılan ayrı bir **süreç** yapar; ayrı bir iş parçacığı değil.

(2) "`everysec` ne vaat eder?" — Saniyede bir fsync yapar, yani çökmede en fazla ~1 saniyelik kayıp olur. Her komutta fsync `always`, hiç fsync yapmamak `no` demektir.

(3) "Yazma yoğunken bellek neden ikiye katlanıyor?" — COW kopyaları yüzünden, hem de yalnız geçici olarak.

(4) "AOF yeniden yazımı ne yapar?" — Aynı son durumu üretecek en küçük komut kümesini yeniden yazar.

(5) "Birincil depoda hangi tutum doğru?" — AOF + kopyalama + yedek. `appendfsync no` "güvenli" değildir.

### Tek cümlede

RDB, hızlı yüklenen ama iki kare arasını kaybettiren bir fotoğraftır. AOF ise her yazıyı tutan, buna karşılık `appendfsync` ile hız/kayıp takası yaptıran bir günlüktür. İkisi hibrit modda birleşir; fork ve COW'un faturası ise tepe bellek kullanımında ödenir.

# rpersist-1 | İleri
## Soru
RDB snapshot alırken Redis, veri setinin **tutarlı** bir kopyasını nasıl çıkarır?
## A
Snapshot bitene kadar gelen tüm yazma komutlarını bloklayıp veriyi dondurarak
🤖 Redis snapshot boyunca yazmaları bloklamaz; fork + COW ile ana süreç çalışmaya devam eder.
## B
Ana süreç içinde bir arka plan thread'i açıp veriyi o thread üzerinden yazarak
🤖 Snapshot ayrı bir thread değil, `fork()` ile yaratılan ayrı bir SÜREÇ tarafından alınır.
## C
`fork()` ile çocuk süreç yaratarak; copy-on-write sayesinde ana süreç yazmaya devam eder
🤖 Doğru: fork+COW, bloklamadan tutarlı bir noktasal kopya çıkarmayı sağlar.
## D
RESP protokolü üzerinden veriyi başka bir sunucuya akıtıp orada dosyaya çevirerek
🤖 RESP bir istemci protokolüdür; RDB dosyasının üretilmesiyle ilgisi yoktur.
## Doğru
C
## Açıklama
RDB, **`fork()`** ile bir **çocuk süreç** oluşturur. Fork anında ana ve çocuk süreç aynı bellek sayfalarını **paylaşır**; işletim sisteminin **copy-on-write (COW)** mekanizması sayesinde ana süreç sayfa değiştirdikçe yalnız o sayfalar kopyalanır. Böylece çocuk süreç, fork anındaki **tutarlı görüntüyü** ana süreci bloklamadan diske yazar (C). Yazmalar bloklanmaz (A yanlış); iş bir thread değil ayrı **süreç** tarafından yapılır (B yanlış); RESP protokolü (D) alakasızdır.

# rpersist-2 | İleri
## Soru
AOF'ta `appendfsync everysec` politikası ne vaat eder?
## A
Saniyede bir fsync yapar; çökmede en fazla ~1 saniyelik yazı kaybı riski taşır
🤖 Doğru: everysec, kayıp riski ile hız arasında yaygın kullanılan dengedir.
## B
Her yazma komutundan sonra fsync yapar ve çökmede sıfır veri kaybı garantisi verir
🤖 Bu `always` politikasıdır; everysec her komutta değil, saniyede bir fsync yapar.
## C
Hiç fsync çağırmaz; diske yazma zamanlamasını tümüyle işletim sistemine bırakır
🤖 Bu `no` politikasıdır; everysec düzenli olarak, saniyede bir fsync yapar.
## D
Yalnızca Redis düzgün kapanırken bir kez fsync yapar; çalışırken diske dokunmaz
🤖 Böyle bir politika yoktur; everysec periyodik olarak saniyede bir diske senkronlar.
## Doğru
A
## Açıklama
`appendfsync` üç değer alır: **`always`** her yazma komutunda `fsync` çağırır (kayıp neredeyse sıfır, en yavaş), **`everysec`** saniyede bir `fsync` yapar (çökmede en fazla ~1 saniyelik yazı kaybı, dengeli ve varsayılan tercih), **`no`** ise senkronlamayı tümüyle işletim sistemine bırakır (en hızlı, en riskli). Soru `everysec`'i sorduğundan doğru cevap A'dır. B `always`'i (C ise `no`'yu) tarif eder; D diye bir politika yoktur.

# rpersist-3 | İleri
## Soru
AOF dosyası her yazma komutuyla sürekli büyür. **AOF rewrite** bu büyümeyi nasıl çözer?
## A
AOF dosyasını tamamen silip kalıcılığı kapatır; veri yalnız bellekte kalmaya devam eder
🤖 Rewrite kalıcılığı kapatmaz; veriyi koruyarak dosyayı yeniden ve daha küçük yazar.
## B
AOF'u bırakıp kalıcılığı tümüyle RDB anlık görüntülerine devreder ve günlüğü kapatır
🤖 Rewrite yine bir AOF üretir; RDB'ye geçiş değildir (hibrit ayrı bir konudur).
## C
Dosyayı gzip ile bayt düzeyinde sıkıştırarak diskte kapladığı yeri küçültür
🤖 Rewrite bir gzip sıkıştırması değildir; komut kümesini mantıksal olarak azaltır.
## D
Mevcut veriyi üretecek en az komut kümesini fork ile yeni bir AOF'a yazar
🤖 Doğru: rewrite, aynı son durumu veren minimal komut kümesini yeniden üretir.
## Doğru
D
## Açıklama
Bir anahtara defalarca `SET`/`INCR` yapılırsa AOF'ta onlarca komut birikir, oysa **son durumu** üretmek için tek bir komut yeter. **AOF rewrite** tam olarak bunu yapar: mevcut veri setini üretecek **en az komut kümesini** (fork ile arka planda) yeni bir dosyaya yazar ve eskisiyle değiştirir; böylece dosya mantıksal olarak küçülür (D). Veri silinmez (A yanlış), RDB'ye geçilmez (B yanlış), işlem bir bayt-seviyesi gzip sıkıştırması değildir (C yanlış).

# rpersist-4 | İleri
## Soru
Yazma yoğun bir Redis'te RDB üretimi veya AOF rewrite sırasında **fiziksel bellek kullanımı iki katına kadar** neden çıkabilir?
## A
Üretilen RDB dosyası bittikten sonra doğrulama amacıyla tekrar belleğe yüklendiği için
🤖 RDB üretimi dosyayı belleğe geri yüklemez; ikiye katlanma COW'dan kaynaklanır.
## B
Copy-on-write gereği ana süreç değiştirdiği her sayfayı kopyalatır; kopyalar birikir
🤖 Doğru: yazma yoğunsa COW ile kopyalanan sayfa sayısı artar ve bellek şişer.
## C
Redis her anahtarı güvenlik gerekçesiyle sürekli iki ayrı kopya hâlinde saklamaktadır
🤖 Redis anahtarları ikişer kopya tutmaz; şişme yalnız fork anına özgü ve geçicidir.
## D
AOF buffer'ı, snapshot süresince veri setinin tam bir kopyasını bellekte tutmak zorundadır
🤖 AOF buffer küçük bir yazma tamponudur; veri setini ikiye katlayacak boyutta değildir.
## Doğru
B
## Açıklama
`fork()` anında ana ve çocuk süreç bellek sayfalarını **paylaşır**; ekstra bellek başlangıçta sıfıra yakındır. Ama **copy-on-write** gereği, ana süreç bir sayfayı **değiştirince** o sayfanın kopyası çıkarılır. Yazma yoğun bir yükte snapshot/rewrite süresi boyunca **çok sayıda sayfa** değişir; en kötü durumda neredeyse tüm veri seti kopyalanır ve fiziksel bellek veri setinin **~2 katına** yaklaşır (B). Bu yüzden kapasite planında bu tepe göz önünde tutulmalıdır. A, C ve D bu geçici COW şişmesini yanlış nedenlere bağlar.

# rpersist-5 | İleri
## Soru
Redis'te **RDB + AOF hibrit** (`aof-use-rdb-preamble`) yaklaşımının amacı nedir?
## A
Kalıcılığı tümden kapatıp Redis'i yalnız bellekte çalışan saf bir önbelleğe indirmek
🤖 Hibrit kalıcılığı kapatmaz; tam tersine iki mekanizmanın güçlü yanlarını birleştirir.
## B
Yalnızca RDB kullanıp AOF'u kalıcı olarak devre dışı bırakıp günlüğü kapatmak
🤖 Hibritte AOF açık kalır; RDB yalnız AOF'un başına taban (preamble) olarak eklenir.
## C
AOF'un başına RDB biçiminde kompakt bir taban koyup sonrasına komutları eklemek
🤖 Doğru: hibrit, RDB'nin hızlı yüklemesi ile AOF'un düşük kaybını birleştirir.
## D
Veriyi iki ayrı sunucuya eşzamanlı yazarak yüksek erişilebilirlik ve dayanıklılık sağlamak
🤖 Bu replikasyon/HA konusudur; hibrit kalıcılık tek node'daki dosya biçimiyle ilgilidir.
## Doğru
C
## Açıklama
**Hibrit** (`aof-use-rdb-preamble yes`) yaklaşımında AOF dosyasının **başına** veri setinin RDB formatında **kompakt bir tabanı (preamble)** yazılır; bu tabandan **sonrasına** ise yeni yazma komutları AOF olarak eklenir. Böylece yeniden yüklemede büyük kısım **hızlıca RDB olarak** okunur (AOF'u baştan sona komut komut yürütmekten hızlı), son değişiklikler ise AOF sayesinde **düşük veri kaybıyla** kurtarılır (C). Bu, saf RDB'nin hızıyla saf AOF'un dayanıklılığını birleştiren varsayılan modern moddur. A, B ve D hibridin ne olduğunu yanlış tanımlar.

# rpersist-6 | İleri
## Soru
Redis'i **birincil veri deposu** olarak kullanacaksan (yalnız önbellek değil), persistence açısından hangi tutum doğrudur?
## A
AOF'u (`everysec` ya da `always`) aç, replikasyon ve düzenli yedek ekle
🤖 Doğru: birincil depoda düşük kayıplı AOF + replikasyon + yedek birlikte gerekir.
## B
Kalıcılık gereksizdir; RAM yeterlidir ve çökmede veri baştan yeniden hesaplanabilir
🤖 Birincil depoda veri tek kaynaktır; RAM uçunca yeniden hesaplanamaz, kalıcılık şarttır.
## C
Kalıcılığı Pub/Sub üzerinden başka bir servise akıtmak tek başına yeterli olur
🤖 Pub/Sub kalıcı değildir; abone yokken mesaj düşer, dayanıklı depolama sağlamaz.
## D
`appendfsync no` en güvenli ayardır çünkü işletim sistemi yazmayı daha iyi optimize eder
🤖 `no` en riskli ayardır; fsync'i OS'e bırakır, çökmede saniyelerce yazı kaybedilebilir.
## Doğru
A
## Açıklama
Redis **birincil depo** olduğunda veri **tek kaynaktır**; kaybı geri alınamaz. Bu yüzden düşük kayıplı **AOF** (`everysec` ya da dayanıklılık kritikse `always`) açılmalı, **replikasyon** ile ikinci bir kopya tutulmalı ve **periyodik yedek** alınmalıdır (A). Yalnız RDB'ye güvenmek, iki snapshot arasında gelen yazıları çökmede kaybettirir. Persistence'ı kapatmak (B), kalıcı olmayan Pub/Sub'a güvenmek (C) veya en riskli `appendfsync no`'yu "güvenli" sanmak (D) birincil depo için yanlıştır. Önbellek senaryosunda ise bu gereksinimler gevşer.
