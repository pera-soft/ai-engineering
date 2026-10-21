---
slug: redis-pubsub-vs-streams
baslik: "Redis Pub/Sub vs Streams: Fire-and-Forget mi, Kalıcı Log mu?"
kademe: İleri
sira: 161
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir programın bir parçası bir şey yapar (sipariş alındı), başka bir parçası bunu duymak ister (fatura kes, bildirim gönder). İki parçanın birbirini beklemeden konuşmasına **mesajlaşma** denir. Mesajı yazan tarafa **yayıncı (publisher)**, dinleyen tarafa **abone (subscriber)** denir. Bu düzenin adı **yayın/abone (publish/subscribe)** modelidir.

Redis bu işi iki ayrı yolla yapar ve mülakatta en sık karıştırılan şey ikisinin farkıdır: biri mesajı **hiç saklamaz**, diğeri **saklar**.

### Temel kavramlar

- **Kalıcılık (persistence)**: Verinin, onu üreten bağlantı kapansa ya da sunucu yeniden başlasa bile durmaya devam etmesi.
- **Pub/Sub (yayın/abone)**: `PUBLISH` komutu mesajı yalnız **o an bağlı olan** abonelere anında iletir, sonra unutur. Buna **ateşle-unut (fire-and-forget)** denir. Mesaj hiçbir yere yazılmaz. Abone o an bağlı değilse mesaj **kaybolur** ve geri istenemez. Teslim garantisi bu yüzden "**en fazla bir kez**" (at-most-once) düzeyindedir. `PUBLISH` bu durumda hata da vermez; yalnız mesajı kaç aboneye ulaştırdığını döndürür, abone yoksa `0` döner.
- **Streams**: **Sona-ekli günlük (append-only log)** — deftere sırayla yazılan, silinmeyen satırlar. `XADD` ile eklenen her satır bir kimlik (ID) alır ve Redis'te **durur**; `XREAD` ile geçmiş satırlar sonradan da okunabilir.
- **Tüketici grubu (consumer group)**: Aynı işi paylaşan işçiler takımı. `XREADGROUP` ile bir mesajı takımdan **yalnız bir** işçi alır.
- **PEL (Pending Entries List — bekleyen girdiler listesi)**: Teslim edilmiş ama henüz "işledim" denmemiş mesajların tutulduğu listedir. İşçi `XACK` ile onaylayınca mesaj PEL'den düşer. Onaylamadan çökerse mesaj PEL'de kalır. Bu yüzden garanti "**en az bir kez**" (at-least-once) düzeyindedir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Pub/Sub fire-and-forget, Streams kalıcı log">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki mesajlaşma modeli</text>
  <rect x="28" y="44" width="278" height="140" rx="12" fill="#FBEDE9" stroke="#B4472B" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#B4472B" font-family="system-ui">Pub/Sub (PUBLISH)</text>
  <text x="167" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Fire-and-forget · persistence YOK</text>
  <text x="167" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Bağlı olmayan abone</text>
  <text x="167" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">mesajı ASLA göremez</text>
  <text x="167" y="164" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">en fazla bir kez (at-most-once)</text>
  <rect x="334" y="44" width="278" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Streams (XADD)</text>
  <text x="473" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Kalıcı log · consumer group</text>
  <text x="473" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">XREADGROUP → PEL → XACK</text>
  <text x="473" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tüketici çökse de backlog kalır</text>
  <text x="473" y="164" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">en az bir kez (at-least-once)</text>
</svg>
```

### Nasıl çalışır?

Pratik kural nettir. **"Kaçırılırsa kaçsın"** türü bildirimler için Pub/Sub yeter; canlı gösterge ya da "yazıyor…" sinyali gibi. **İşlenmesi garanti olması gereken** olaylar için ise Streams gerekir; sipariş ve ödeme gibi.

Streams bellek odaklıdır ve bir akış ağırlıkla tek düğüm üzerinde yaşar. Devasa hacim, günler süren saklama (retention) ve baştan yeniden okuma (replay) gerekiyorsa **Kafka** daha uygundur. Kafka bölüm (partition) sayısıyla yatay ölçeklenir ve veriyi diskte uzun süre tutar. Tüketici grubu kavramı ise ikisinde de vardır; Redis Streams onu Kafka'dan esinlenerek sunar.

Streams'te bir mesajın izlediği yol, "en az bir kez" garantisinin temelidir. Mesaj `XADD` ile günlüğe eklenir. `XREADGROUP` ile bir tüketiciye teslim edilir ve aynı anda **PEL**'e düşer. İş bitince `XACK` ile PEL'den silinir. Tüketici çökerse mesaj PEL'de kalır ve **kendiliğinden başka bir tüketiciye geçmez**. `XPENDING` ile görülür; `XCLAIM` ya da `XAUTOCLAIM` ile açıkça devredilir:

```svg
<svg viewBox="0 0 640 170" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Streams mesaj yaşam döngüsü: XADD, XREADGROUP, PEL, XACK">
  <defs>
    <marker id="rpubsub-flow" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Streams: bir mesajın yaşam döngüsü</text>
  <rect x="12" y="58" width="108" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="66" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">XADD</text>
  <text x="66" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">loga ekle</text>
  <rect x="152" y="58" width="108" height="60" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="206" y="84" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">XREADGROUP</text>
  <text x="206" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tüketiciye ver</text>
  <rect x="292" y="58" width="108" height="60" rx="9" fill="#FBEDE9" stroke="#B4472B" stroke-width="1.5"/>
  <text x="346" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#B4472B" font-family="system-ui">PEL</text>
  <text x="346" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">onaysız bekler</text>
  <text x="346" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(pending)</text>
  <rect x="432" y="58" width="108" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="486" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">XACK</text>
  <text x="486" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">PEL'den düş</text>
  <line x1="122" y1="88" x2="150" y2="88" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-flow)"/>
  <line x1="262" y1="88" x2="290" y2="88" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-flow)"/>
  <line x1="402" y1="88" x2="430" y2="88" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-flow)"/>
  <text x="320" y="146" text-anchor="middle" font-size="10.5" fill="#B4472B" font-family="system-ui">tüketici çökerse → XCLAIM / XAUTOCLAIM ile başka tüketiciye devir</text>
</svg>
```

Tüketici grubunda iki ayrı davranışı karıştırmamak gerekir. Aynı akışı okuyan **ayrı gruplar**, her biri mesajın kendi kopyasını alır; buna dağıtım (fan-out) denir. **Tek bir grubun** içindeki tüketiciler ise mesajı bölüşür: bir mesaj yalnız bir tüketiciye gider. Buna rekabetçi tüketiciler (competing consumers) denir. Bir mesajın onaylanmamış olması yalnız o girdiyi PEL'de tutar; grubun diğer tüketicileri sıradaki başka mesajları almaya devam eder:

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fan-out ayrı gruplar vs rekabetçi tüketiciler tek grup">
  <defs>
    <marker id="rpubsub-fan" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı stream, iki farklı senaryo</text>
  <rect x="14" y="34" width="300" height="172" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Ayrı gruplar → fan-out</text>
  <rect x="30" y="72" width="80" height="46" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="70" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">stream</text>
  <rect x="196" y="66" width="100" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="246" y="87" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Grup A</text>
  <rect x="196" y="118" width="100" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="246" y="139" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Grup B</text>
  <line x1="110" y1="88" x2="194" y2="82" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-fan)"/>
  <line x1="110" y1="100" x2="194" y2="134" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-fan)"/>
  <text x="164" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her grup mesajın TAM kopyasını alır</text>
  <text x="164" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(fan-out)</text>
  <rect x="326" y="34" width="300" height="172" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Tek grup → bölüşüm</text>
  <rect x="340" y="72" width="72" height="40" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="376" y="97" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">stream</text>
  <rect x="446" y="74" width="66" height="36" rx="7" fill="#F4EEF9" stroke="#6A3FA0" stroke-width="1.3"/>
  <text x="479" y="97" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">grup g</text>
  <rect x="548" y="52" width="66" height="32" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="581" y="73" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C1</text>
  <rect x="548" y="100" width="66" height="32" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="581" y="121" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">C2</text>
  <line x1="412" y1="92" x2="444" y2="92" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-fan)"/>
  <line x1="512" y1="86" x2="546" y2="70" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-fan)"/>
  <line x1="512" y1="98" x2="546" y2="114" stroke="#5D7789" stroke-width="2" marker-end="url(#rpubsub-fan)"/>
  <text x="476" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">mesaj yalnız BİR tüketiciye gider</text>
  <text x="476" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(rekabetçi tüketiciler)</text>
</svg>
```

### Mülakatta ne sorulur?

En sık gelen üç soru şunlardır.

(1) "Abone yokken `PUBLISH` yaparsan ne olur?" — Mesaj hiçbir yere yazılmadan düşer. Komut hata vermez, `0` döner.

(2) "En az bir kez teslimi hangi komutlar kurar?" — `XADD` → `XREADGROUP` → `XACK` üçlüsü. `SUBSCRIBE` ve `PUBLISH` Pub/Sub'a aittir; `XRANGE` ve `XTRIM` ise grup semantiği taşımaz.

(3) "Tüketici `XACK` yapmadan çökerse ne olur?" — Mesaj silinmez, PEL'de bekler. `XPENDING` ile görülür; `XCLAIM` ya da `XAUTOCLAIM` ile devredilip yeniden işlenir.

Bir de klasik tuzak vardır: "Pub/Sub'a RDB/AOF eklesek kaçan mesaj kurtulur mu?" Cevap hayır. RDB ve AOF anahtar/değer verisini kaydeder; uçup giden Pub/Sub mesajını değil. Keyspace bildirimleri (keyspace notifications) de Pub/Sub üzerinden gittiği için tam olarak aynı kayba tabidir.

### Tek cümlede

Pub/Sub anlık ve unutkandır (abone yoksa mesaj kaybolur), Streams ise kalıcı bir günlüktür ve grup + PEL + `XACK` üçlüsüyle mesajı en az bir kez işletir.

# rpubsub-1 | İleri
## Soru
Bir Pub/Sub kanalına `PUBLISH` yapıldığı anda o kanala bağlı **hiç abone (subscriber) yoksa** ne olur?
## A
Mesaj Redis'te saklanır ve ilk abone bağlandığında ona geriye dönük teslim edilir
🤖 Pub/Sub'ın kalıcılığı yoktur; mesaj hiçbir yerde tutulmaz, sonradan teslim edilmez.
## B
Mesaj kaybolur; Pub/Sub ateşle-unut çalışır, bağlı olmayan abone onu asla göremez
🤖 Doğru: teslim yalnız anlıktır, bağlı abone yoksa mesaj düşer.
## C
`PUBLISH` hata döndürür ve komut başarısız sayılıp mesaj hiç işlenmez
🤖 Hata dönmez; `PUBLISH` mesajı alan abone sayısını (`0`) döndürür, başarısız olmaz.
## D
Mesaj AOF dosyasına yazılır ve Redis yeniden başlayınca kanala tekrar yayınlanır
🤖 Pub/Sub mesajları AOF/RDB'ye yazılmaz; onların bir kalıcılık mekanizması yoktur.
## Doğru
B
## Açıklama
Pub/Sub **fire-and-forget**'tir: `PUBLISH` mesajı yalnız **o an bağlı** abonelere anında iletir ve döndürdüğü sayı, mesajı alan abone sayısıdır. Kanalda abone yoksa mesaj **hiçbir yerde saklanmadan düşer** (A, D yanlış). Komut hata da vermez, yalnız `0` döner (C yanlış). Bu yüzden Pub/Sub, kaçırılması tolere edilebilen anlık bildirimler için uygundur; teslim garantisi isteniyorsa **Streams** (kalıcı log + PEL) kullanılmalıdır.

# rpubsub-2 | İleri
## Soru
Bir Redis Stream'de **consumer group** ile "en az bir kez" (at-least-once) teslim ve onay (ack) döngüsü hangi komut üçlüsüyle kurulur?
## A
`XADD` / `SUBSCRIBE` / `XDEL`
🤖 `SUBSCRIBE` Pub/Sub komutudur; Stream tüketimi ve consumer group ile ilgisi yoktur.
## B
`PUBLISH` / `XREAD` / `XACK`
🤖 `PUBLISH` Pub/Sub'a yazar; Stream'e girdi eklemek `XADD` iledir, `PUBLISH` değil.
## C
`XADD` / `XREADGROUP` / `XACK`
🤖 Doğru: `XADD` ekler, `XREADGROUP` gruptan okur (PEL'e alır), `XACK` işlendiğini onaylar.
## D
`XADD` / `XRANGE` / `XTRIM`
🤖 `XRANGE` düz okuma, `XTRIM` kırpmadır; consumer group / ack semantiği sağlamazlar.
## Doğru
C
## Açıklama
Consumer group akışı şudur: `XADD stream * alan değer` girdiyi loga ekler; `XREADGROUP GROUP g tüketici ... >` mesajı bir tüketiciye teslim eder ve grubun **Pending Entries List (PEL)**'ine koyar; tüketici işi bitirince `XACK stream g id` ile onaylar ve mesaj PEL'den düşer. Onaylanmayan mesaj PEL'de kalıp yeniden işlenebildiği için **at-least-once** garantisi bu üçlüyle kurulur. `SUBSCRIBE`/`PUBLISH` (A, B) Pub/Sub'a aittir; `XRANGE`/`XTRIM` (D) grup semantiği taşımaz.

# rpubsub-3 | İleri
## Soru
Aynı **consumer group**'a ait iki tüketici `XREADGROUP` ile okurken, gruba gelen bir mesajın akıbeti ne olur?
## A
Her iki tüketici de aynı mesajın kendi kopyasını alır (fan-out çoğaltması)
🤖 Fan-out, aynı akışı okuyan AYRI gruplarda olur; tek grup içinde mesaj çoğaltılmaz.
## B
Mesaj gruptaki yalnız bir tüketiciye gider; iş tüketiciler arasında bölüşülür
🤖 Doğru: tüketici grubunda her mesaj tek tüketiciye gider, yük paylaştırılır.
## C
Mesaj rastgele düşürülür ve tüketicilerin hiçbiri onu hiç alamaz
🤖 Mesaj düşürülmez; gruptaki tam olarak bir tüketiciye teslim edilir.
## D
İlk tüketici `XACK` yapana kadar ikinci tüketici hiçbir mesaj alamaz
🤖 İkinci tüketici sıradaki BAŞKA mesajları alabilir; ack tek bir girdiyi kilitler, grubu değil.
## Doğru
B
## Açıklama
Bir **consumer group** içinde her mesaj **yalnız bir tüketiciye** teslim edilir; iş yükü gruptaki tüketicilere **bölüştürülür** (rekabetçi tüketiciler / competing consumers deseni). Aynı mesajın birden çok kopyasının işlenmesi (fan-out) ancak **ayrı gruplar** aynı Stream'i okuduğunda olur (A yanlış). Mesaj asla kaybolmaz (C yanlış). Bir mesajın ack'lenmemiş olması yalnız o girdiyi PEL'de tutar; diğer tüketiciler sıradaki başka mesajları almaya devam eder (D yanlış).

# rpubsub-4 | İleri
## Soru
Sipariş olaylarını işleyen bir tüketici birkaç saniye çökse bile, **çökme süresi boyunca gelen olayları** geri geldiğinde işlemek istiyorsun. Hangi seçim doğrudur?
## A
Pub/Sub; düşük gecikmesi olduğundan bu senaryo için en iyi seçimdir
🤖 Pub/Sub çökme boyunca gelen mesajları kaybeder; kaçan olaylar geri işlenemez.
## B
Pub/Sub + RDB snapshot; anlık görüntü kaçan mesajları da geri getirir
🤖 RDB anahtar/değer verisini kaydeder, Pub/Sub mesajlarını değil; kaçan olay yine kaybolur.
## C
Keyspace notifications (anahtar-alanı bildirimleri) tek başına bu iş için yeterlidir
🤖 Keyspace bildirimleri de Pub/Sub üzerinden gider; abone yokken kaçan bildirim geri gelmez.
## D
Streams; kalıcı günlük sayesinde tüketici geri gelince kaldığı yerden okur
🤖 Doğru: Streams olayları saklar, tüketici çökse de birikeni sonradan işleyebilir.
## Doğru
D
## Açıklama
Gereksinim, **çökme süresince biriken olayların kaybolmadan** sonradan işlenmesidir. Bu tam olarak **Streams**'in işidir: `XADD` ile eklenen olaylar loga **kalıcı** yazılır; tüketici geri geldiğinde consumer group'un PEL'i ve stream backlog'u üzerinden **kaldığı yerden** devam eder (D). Pub/Sub (A) ve Pub/Sub üzerine kurulu keyspace notifications (C), abone bağlı değilken gelen mesajları kaybeder. RDB snapshot (B) anahtar/değer durumunu kaydeder ama uçup giden Pub/Sub mesajlarını kurtarmaz.

# rpubsub-5 | İleri
## Soru
Redis Streams ile Kafka arasındaki mimari sınır için hangisi **doğrudur**?
## A
Kafka uzun saklama ve yeniden okuma için tasarlanmıştır; Redis Streams bellek odaklıdır
🤖 Doğru: ikisi de günlük ve grup sunar, ama ölçek ile saklama profilleri ayrışır.
## B
Redis Streams disk tabanlı olduğundan Kafka'dan daha yüksek saklama kapasitesi ve daha uzun retention sunar
🤖 Tersine; Redis bellek odaklıdır, uzun süreli ve büyük hacimli saklamada Kafka öndedir.
## C
Tüketici grubu kavramı Kafka'da yoktur; bölümleme ve grup semantiği yalnız Redis Streams'e özgüdür
🤖 Tüketici grubu Kafka'nın çekirdek kavramıdır; Redis Streams bunu ondan esinlenerek sunar.
## D
İkisi de aynı ağ protokolü ve aynı depolama motoru üzerine kuruludur; fark yalnız isimlendirmededir
🤖 Farklı protokol ve mimarilerdir; benzerlik yalnız kavramsaldır (günlük + gruplar).
## Doğru
A
## Açıklama
Her iki sistem de **kalıcı log + consumer group** sunar, ama sınır ölçek ve saklamadadır: **Kafka** partition'larla yatay ölçeklenir, verileri diskte uzun süre tutar ve **replay** (baştan yeniden okuma) için tasarlanmıştır; devasa hacim ve gün/hafta ölçeğinde saklama onun alanıdır. **Redis Streams** ise bellek odaklı, çoğunlukla tek node üzerinde yaşar (bir stream tek slota/node'a bağlıdır) ve düşük gecikmeli, orta hacimli iş kuyrukları için idealdir (A). B saklama profilini ters çevirir, C consumer group'u yanlışlıkla Redis'e özgü sanar, D iki farklı sistemi eşitler.

# rpubsub-6 | İleri
## Soru
Bir tüketici `XREADGROUP` ile mesaj aldı ama `XACK`'lemeden **çöktü**. Mesajın durumu ve kurtarılması nasıldır?
## A
Mesaj otomatik silinir; onaylanmayan girdi gruptan tümüyle düşer ve iş kaybolur
🤖 Onaylanmayan mesaj silinmez; grubun PEL'inde bekler, aksi hâlde iş kaybı olurdu.
## B
Mesaj hiçbir müdahale gerekmeden kendiliğinden sağlıklı başka bir tüketiciye devredilir
🤖 Devir kendiliğinden olmaz; `XCLAIM`/`XAUTOCLAIM` ile açıkça devredilmesi gerekir.
## C
Mesaj grubun PEL'inde kalır; `XPENDING` ile görülüp `XCLAIM`/`XAUTOCLAIM` ile devredilir
🤖 Doğru: onaylanmamış girdi PEL'de bekler ve claim komutlarıyla kurtarılır.
## D
Mesaj bir daha asla işlenemez; günlükten silinerek kalıcı olarak kaybolur
🤖 Kaybolmaz; PEL'de durur ve başka tüketiciye devredilerek yeniden işlenebilir.
## Doğru
C
## Açıklama
`XREADGROUP` ile teslim edilen ama `XACK`'lenmeyen mesaj, grubun **Pending Entries List (PEL)**'inde bekler; bu, at-least-once garantisinin temelidir. Operatör `XPENDING` ile bekleyen girdileri (hangi tüketicide, ne kadar süredir beklediğini) görür ve `XCLAIM` (belirli girdileri) veya `XAUTOCLAIM` (belirli süreden uzun bekleyenleri) ile mesajı **sağlıklı bir tüketiciye devreder**; böylece yeniden işlenir (C). Mesaj ne silinir (A), ne otomatik devredilir (B), ne de kalıcı kaybolur (D) — kurtarma açık claim komutlarıyla yapılır.
