---
slug: rabbitmq-quorum-streams
baslik: "RabbitMQ 4.x: Quorum Queue (Raft), Streams ve Khepri"
kademe: İleri
sira: 123
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

Bütün mesajlar tek bir bilgisayarda, tek bir kutuda duruyorsa o bilgisayarın diski bozulduğunda her şey biter. Çözüm basit görünüyor: aynı kutunun **kopyasını** birkaç bilgisayarda birden tut. Buna **replikasyon (çoğaltma)** denir; her bilgisayara da **düğüm (node)**, hepsine birden **küme (cluster)** diyoruz.

Ama kopya tutmak yeni bir sorun doğurur: kopyalar birbirinden farklı olursa hangisi doğrudur? Özellikle ağ kablosu kopup küme ikiye bölünürse, iki taraf da "asıl ben benim" deyip ayrı ayrı yazmaya başlarsa veriler çatallanır. Buna **split-brain (ikiye bölünmüş beyin)** denir ve veri kaybının en sinsi biçimidir.

RabbitMQ **4.x**'in cevabı nettir: eski **aynalı klasik kuyruk (classic mirrored queue)** **tamamen kaldırıldı**. Yüksek erişilebilirliğin (HA, high availability) standardı artık **çoğunluk kuyruğudur (quorum queue)**. Mülakatta "aynalı kuyruk kurarım" demek güncelliğini yitirmiş bir cevaptır.

### Temel kavramlar

- **Düğüm (node)**: kümedeki tek bir RabbitMQ sunucusu.
- **Replikasyon (çoğaltma)**: aynı verinin birden çok düğümde kopyalanması.
- **Konsensüs (uzlaşma)**: kopyaların "hangi veri geçerli" konusunda anlaşma yöntemi.
- **Raft**: RabbitMQ'nun kullandığı konsensüs algoritması. Bir **lider** ve **takipçiler** vardır.
- **Çoğunluk (quorum)**: düğümlerin yarısından fazlası — 3 düğümde 2, 5 düğümde 3.
- **Commit (onaylama)**: bir yazmanın "artık kalıcı, geri alınamaz" sayılması.
- **`pause_minority`**: ağ bölünmesinde azınlıkta kalan tarafın kendini durdurması.
- **Append-only log (yalnızca sona eklenen kayıt)**: mesajların defterin sonuna yazıldığı, hiçbirinin okununca silinmediği yapı.
- **Okuma konumu (offset)**: bu defterdeki satır numarası; her okuyucu kendi satırını takip eder.
- **Saklama sınırı (retention)**: defterin kaç gün ya da kaç gigabayt tutulacağı.
- **Üstveri (metadata)**: mesajlar değil, **tanımlar** — **mesaj dağıtıcıları (exchange)**, kuyruklar, dağıtıcıyı kuyruğa bağlayan **bağlar (binding)**, kullanıcılar ve izinler.

### Nasıl çalışır?

**Çoğunluk kuyruğu (quorum queue)**, kuyruğu Raft ile çoğaltır. Lider mesajı takipçilere yollar; düğümlerin **çoğunluğu** mesajı kaydedince yazma **commit** edilir.

Neden çoğunluk? Çünkü "tek düğüm yazdı" demek yetmez; o düğüm ölürse veri gider. Öte yandan "tüm düğümler yazsın" demek de gereksiz katıdır: bir düğüm düştüğü anda küme yazmayı tamamen durdururdu. Çoğunluk kuralı ikisinin ortasıdır ve **bir düğüm kaybında bile** kümenin çalışmasını sürdürür. (Bu, tüketicinin sonradan verdiği ack ile karıştırılmamalı; commit yayım aşamasındadır.) Quorum kuyruklar **daima kalıcıdır** ve kuyruk içi sırayı korur.

Ağ bölünmesinde **`pause_minority`** devreye girer: yalnızca çoğunluğu gören taraf yazmayı sürdürür, **azınlıkta** kalan taraf kendini durdurur. Küme tamamen durmaz, azınlıktaki bir düğüm de kendini lider ilan edip yazamaz — çünkü çoğunluk oyu toplayamaz. Bölünme bitince azınlık düğüm tekrar katılıp lideri yakalar (catch-up). Split-brain böyle önlenir.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Quorum queue Raft çoğunluğu ve pause_minority">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Quorum queue: Raft çoğunluğu yazmayı onaylar</text>
  <circle cx="150" cy="95" r="34" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="150" y="92" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Lider</text>
  <text x="150" y="108" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">node 1</text>
  <circle cx="150" cy="175" r="26" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="150" y="179" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">takipçi 2</text>
  <circle cx="240" cy="135" r="26" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="240" y="139" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">takipçi 3</text>
  <text x="150" y="42" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">çoğunluk (2/3) yazınca onay</text>
  <line x1="340" y1="55" x2="340" y2="200" stroke="#D0D8DE" stroke-width="1.5" stroke-dasharray="5 4"/>
  <text x="500" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#C0562B" font-family="system-ui">Ağ bölünmesi (partition)</text>
  <rect x="380" y="70" width="110" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="435" y="92" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Çoğunluk tarafı</text>
  <text x="435" y="110" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yazmaya devam</text>
  <rect x="510" y="70" width="120" height="52" rx="9" fill="#FBE9E7" stroke="#C0562B" stroke-width="1.5"/>
  <text x="570" y="92" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C0562B" font-family="system-ui">Azınlık tarafı</text>
  <text x="570" y="110" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">pause_minority: durur</text>
  <text x="505" y="150" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Azınlık kendini durdurur → split-brain önlenir.</text>
  <text x="505" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Streams: log gibi tutar (replay); Khepri: Raft-tabanlı metadata.</text>
</svg>
```

**RabbitMQ akışları (Streams)**, Kafka'ya benzeyen bir **append-only log**'tur. Mesajlar tüketilince **silinmez**; saklama sınırına kadar defterde durur. Her tüketici istediği **okuma konumundan (offset)** — baştan, ortadan ya da sondan — bağımsız okuyabilir. Yani **baştan tekrar okuma (replay)** mümkündür.

Bu, klasik kuyruğun "tüketince sil" modelinin alternatifidir. Mesajı **herkese dağıtan bir exchange (fanout)** ile çoğaltmak aynı şeyi vermez, çünkü her kopya yine tüketilince gider.

```svg
<svg viewBox="0 0 660 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Streams append-only log: tüketiciler farklı offset'lerden bağımsız okur, mesaj silinmez">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Streams: append-only log — tüketince SİLİNMEZ, offset'ten replay</text>
  <rect x="40" y="70" width="60" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="70" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">m0</text>
  <rect x="104" y="70" width="60" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="134" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">m1</text>
  <rect x="168" y="70" width="60" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="198" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">m2</text>
  <rect x="232" y="70" width="60" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="262" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">m3</text>
  <rect x="296" y="70" width="60" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="326" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">m4</text>
  <text x="380" y="95" font-size="11" fill="#5D7789" font-family="system-ui">→ eklenir</text>
  <rect x="70" y="140" width="150" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="145" y="161" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Tüketici A · offset 0</text>
  <rect x="300" y="140" width="180" height="34" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="390" y="161" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">Tüketici B · offset 3 (baştan değil)</text>
  <line x1="70" y1="140" x2="70" y2="112" stroke="#186399" stroke-width="1.5" stroke-dasharray="4 3"/>
  <line x1="262" y1="140" x2="262" y2="112" stroke="#6B3FA0" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="330" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Her tüketici kendi offset'ini taşır; retention (boyut/süre) sınırına kadar geçmiş korunur.</text>
</svg>
```

**Khepri** ise mesajların değil, **üstverinin (metadata)** yeni deposudur: exchange/kuyruk/binding tanımları, kullanıcılar, izinler, politikalar. Eski **Mnesia**'nın yerini alır ve o da **Raft-tabanlıdır**; yani aynı çoğunluk modeli metadata katmanına da taşınır. Kazanç, ağ bölünmesi sonrası tutarlı toparlanmadır (Mnesia'nın bölünme sonrası çakışma çözümü kırılgandı). Khepri harici bir sistem değildir — **etcd** ve **ZooKeeper** ayrı koordinasyon ürünleridir (ZooKeeper eski Kafka'da kullanılırdı), **Redis** ise tamamen başka bir alandır.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Classic, quorum ve streams kuyruk türlerinin karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">4.x kuyruk türleri: hangi iş için hangisi</text>
  <rect x="24" y="44" width="196" height="146" rx="11" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="122" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#5D7789" font-family="system-ui">classic</text>
  <text x="122" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek düğüm, hafif</text>
  <text x="122" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">HA çoğaltma YOK</text>
  <text x="122" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tüketince siler</text>
  <text x="122" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kısa ömürlü / HA gereksiz</text>
  <rect x="232" y="44" width="196" height="146" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">quorum</text>
  <text x="330" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Raft, çoğunluk onayı</text>
  <text x="330" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">daima kalıcı, HA</text>
  <text x="330" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tüketince siler</text>
  <text x="330" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iş kuyruğu + HA (≥3 düğüm)</text>
  <rect x="440" y="44" width="196" height="146" rx="11" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="538" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">streams</text>
  <text x="538" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">append-only log</text>
  <text x="538" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tutar (retention)</text>
  <text x="538" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">offset'ten replay</text>
  <text x="538" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">log / yüksek verim / fan-out</text>
</svg>
```

Doğru araç seçimi şöyledir: iş kuyruğu + yüksek erişilebilirlik → **quorum**; log/replay/çok okuyuculu okuma → **Streams**; kısa ömürlü ve HA gerektirmeyen iş → **klasik kuyruk (classic queue)**.

"Her kuyruk quorum olsun" demek yanlıştır. Quorum her yazıda bir Raft gidiş-dönüşü (round-trip) yapar; bu da ek ağ ve disk maliyeti demektir. Ayrıca anlamlı bir çoğunluk için **en az 3 düğüm** ister. Tek düğümlü ortamda çoğaltacak başka düğüm yoktur; HA faydası gelmez ama maliyet ödenir.

### Mülakatta ne sorulur?

- "Mirrored queue nerede?" — 4.x'te kaldırıldı; yerine quorum queue. Federation ve Shovel broker'lar arası köprü/taşıma eklentileridir, küme içi HA replikasyonu değildir.
- "Yazma ne zaman commit olur?" — çoğunluk kaydedince.
- "Bölünmede ne olur?" — azınlık durur, çoğunluk çalışır.
- "Replay istiyorum" — Streams (quorum veya DLQ değil).
- "Metadata deposu?" — Khepri (Raft), Mnesia'nın yerine.
- "Her yere quorum?" — maliyet ve topoloji eleştirisi; quorum güvensiz değildir, Streams'e bağımlı değildir, sırayı da korur.

### Tek cümlede

RabbitMQ 4.x'te dayanıklılık Raft'ın çoğunluk kuralına dayanır. Çoğunluk kuyruğu (quorum queue) iş kuyruğunu güvenle çoğaltır, Streams log/replay ihtiyacını karşılar, Khepri de aynı güvenceyi üstveriye (metadata) taşır.

# rquorum-1 | Temel
## Soru
RabbitMQ **4.x**'te yüksek erişilebilirlik (HA) için eski **classic mirrored queue** yerine gelen standart mekanizma nedir?
## A
Aynı adlı classic queue'yu iki farklı düğümde ayrı ayrı tanımlamak
🤖 Kuyruğu iki kez tanımlamak çoğaltma sağlamaz; HA için ayrı bir kuyruk türü gerekir.
## B
Federation eklentisini kurup kuyruğu ikinci bir broker'a yansıtmak
🤖 Federation farklı broker'lar arası bağ kurar; mirrored queue'nun HA yerine geçen türü değildir.
## C
Quorum queue (Raft konsensüsüyle çoğaltılan kuyruk türü)
🤖 Doğru: 4.x'te mirrored queue kaldırıldı; HA standardı Raft-tabanlı quorum queue'dur.
## D
Shovel eklentisiyle mesajları sürekli ikinci bir düğüme kopyalamak
🤖 Shovel mesajı bir broker'dan diğerine taşır; kuyruk düzeyi HA replikasyonu sağlamaz.
## Doğru
C
## Açıklama
RabbitMQ **4.x** ile **classic mirrored queue tamamen kaldırıldı**; yüksek erişilebilirliğin (HA) standart yolu artık **quorum queue**'dur. Quorum queue, kuyruğu bir düğüm kümesinde **Raft** konsensüs algoritmasıyla çoğaltır ve yazmayı çoğunluk onayınca kabul eder. Kuyruğu iki düğümde ayrı ayrı tanımlamak (A) çoğaltma değildir. **Federation** (B) ve **Shovel** (D), broker'lar/kümeler arası mesaj taşıma/köprüleme eklentileridir — coğrafi dağıtım veya göç için kullanılır, tek küme içi HA replikasyonunun yerini tutmaz. Mülakatta "aynalı kuyruk kurarım" demek 4.x için güncelliğini yitirmiş bir yanıttır.

# rquorum-2 | Orta
## Soru
Bir quorum queue'da bir yayım (publish) ne zaman **onaylanmış** (committed) sayılır?
## A
Kümedeki düğümlerin çoğunluğu (quorum) mesajı kalıcı olarak kaydettiğinde
🤖 Doğru: Raft yazmayı çoğunluk onayınca commit eder; tüm düğümler değil, çoğunluk yeter.
## B
Tek bir düğüm (lider) mesajı belleğine aldığında, replikasyon hiç beklenmeden
🤖 Tek düğüm yetmez; Raft commit için çoğunluğun onayını bekler, aksi halde veri güvenliği bozulur.
## C
Kümedeki tüm düğümler istisnasız biçimde mesajı onayladığında
🤖 Tüm düğüm gerekmez; bir düğüm düşse bile çoğunluk varsa yazma sürer. "Hepsi" gereksiz katı olurdu.
## D
Tüketici mesajı işleyip `basic.ack` komutuyla onayladığında
🤖 Tüketici ack'i çok sonraki bir aşamadır; yayımın commit'i ile ilgisizdir.
## Doğru
A
## Açıklama
Quorum queue **Raft** kullanır: lider mesajı takipçilere çoğaltır ve düğümlerin **çoğunluğu** (ör. 3 düğümde 2, 5 düğümde 3) mesajı kalıcı olarak kaydedince yazma **commit** edilir. Bu, "tek düğüm belleğe aldı" (B) ile "tüm düğümler onayladı" (C) arasında dengeli bir noktadır: tek düğüm veri güvenliği için yetersizdir, tüm düğümü beklemek ise gereksiz katıdır ve bir düğüm düşünce yazmayı durdururdu. Çoğunluk kuralı sayesinde **bir düğüm kaybında bile** küme yazmaya devam eder ve commit edilmiş veri korunur. Tüketici ack'i (D) tamamen farklı, sonraki bir aşamadır.

# rquorum-3 | Orta
## Soru
Üç düğümlü bir quorum queue kümesinde ağ, bir düğümü diğer ikisinden ayıran bir **bölünme (partition)** yaşıyor. `pause_minority` davranışıyla ne olur?
## A
Her iki taraf da bağımsız yazmaya devam eder; bölünme bitince veriler birleştirilir
🤖 Bu tam da split-brain'dir ve quorum queue bunu önler; iki taraf bağımsız yazamaz.
## B
Küme tamamen durur; bölünme çözülene kadar hiçbir taraf yazma kabul etmez
🤖 Çoğunluğu gören taraf yazmaya devam eder; tüm küme durmaz, yalnızca azınlık durur.
## C
En düşük ID'li düğüm otomatik olarak lider olur ve tek başına yazmayı sürdürür
🤖 Tek düğüm çoğunluk oluşturmaz; azınlıkta kalan lider bile yazmayı sürdüremez.
## D
Azınlık tarafı kendini durdurur; çoğunluk tarafı (2 düğüm) yazmaya devam eder
🤖 Doğru: pause_minority azınlığı durdurur, çoğunluk çalışmayı sürdürür; split-brain önlenir.
## Doğru
D
## Açıklama
**`pause_minority`** stratejisi Raft'ın çoğunluk kuralının doğal sonucudur: ağ bölünmesinde yalnızca **çoğunluğu** (3 düğümde 2) gören taraf yazma kabul edebilir; azınlıkta kalan taraf (1 düğüm) yeterli quorum'u sağlayamadığı için **kendini durdurur** ve yazmayı reddeder. Böylece iki tarafın birbirinden habersiz ilerleyip veriyi çatallaması — **split-brain** (A) — engellenir. Küme tamamen durmaz (B): çoğunluk tarafı hizmete devam eder. Azınlıkta kalan bir düğümün kendini lider ilan edip yazması (C) mümkün değildir, çünkü çoğunluk oyu toplayamaz. Bölünme bitince azınlık düğüm tekrar katılıp lideri yakalar (catch-up).

# rquorum-4 | İleri
## Soru
Bir olay akışını **birden çok tüketicinin bağımsız olarak, geçmişe dönük (replay) dahil** okuyabilmesini istiyorsunuz; mesajlar tüketilince **silinmemeli**, retention sınırına kadar tutulmalı. RabbitMQ 4.x'te hangi yapı doğrudur?
## A
Klasik bir quorum queue kullanmak; kuyrukta retention ayarını açmak yeterlidir
🤖 Quorum queue bir iş kuyruğudur; tüketilen mesaj silinir ve offset'ten replay sunmaz.
## B
fanout exchange kurup her tüketici için ayrı bir klasik kuyruk tanımlamak
🤖 Fanout mesajı çoğaltır ama her klasik kuyrukta tüketilince silinir; geçmişe dönük replay yine yoktur.
## C
Her tüketici için ayrı bir ölü mektup kuyruğu (DLQ) tanımlayıp oradan okumak
🤖 DLQ hatalı mesajlar içindir; log/replay semantiği sağlamaz.
## D
RabbitMQ Streams kullanmak; mesajlar tutulur ve offset'ten tekrar okunabilir
🤖 Doğru: Streams tam olarak log/replay/çoklu bağımsız okuma senaryosu içindir.
## Doğru
D
## Açıklama
**RabbitMQ Streams**, Kafka-benzeri bir **append-only log**'tur: mesajlar tüketilince silinmez, **retention** (boyut/süre) sınırına kadar tutulur ve her tüketici bir **offset** belirterek (baştan, belirli noktadan, sondan) bağımsız olarak okur — **replay** mümkündür. Bu, "log/olay akışı, çoklu bağımsız okuyucu, geçmişe erişim" senaryosunun doğru aracıdır. **Quorum queue** (A) ve klasik kuyruklar iş kuyruğudur: tüketilen mesaj silinir, offset'ten replay yoktur — fanout ile çoğaltmak (B) bunu değiştirmez, her kopya yine tüketilince gider. **DLQ** (C) hatalı mesaj izolasyonu içindir, log semantiği vermez. Streams'i seçmek, RabbitMQ'yu Kafka'nın güçlü olduğu bir alanda kullanabilmeyi sağlar.

# rquorum-5 | İleri
## Soru
RabbitMQ'nun exchange/kuyruk/binding **tanımlarını ve kullanıcı verisini** (metadata) tutan katman için 4.x'te eski **Mnesia**'nın yerine gelen, **Raft-tabanlı** yeni depo hangisidir?
## A
Khepri
🤖 Doğru: Khepri, Mnesia'nın yerini alan Raft-tabanlı yeni metadata deposudur.
## B
etcd
🤖 etcd harici bir Raft KV deposudur; RabbitMQ metadata'sının yerleşik yeni motoru değildir.
## C
ZooKeeper
🤖 ZooKeeper eski Kafka'nın koordinasyon aracıdır; RabbitMQ metadata deposu değildir.
## D
Redis
🤖 Redis bir önbellek/veri deposudur; RabbitMQ'nun metadata katmanı olarak kullanılmaz.
## Doğru
A
## Açıklama
**Khepri**, RabbitMQ'nun metadata deposunun (exchange, kuyruk ve binding tanımları, kullanıcılar, izinler, politikalar) yeni motorudur ve eski **Mnesia**'nın yerini alır. Kritik nokta: Khepri de **Raft-tabanlıdır** — yani quorum queue ile aynı konsensüs modelini metadata katmanına taşır. Bu, özellikle ağ bölünmesi sonrası metadata'nın **tutarlı** biçimde toparlanmasını iyileştirir; Mnesia'nın bölünme sonrası çakışma çözümü kırılgandı. **etcd** (B) ve **ZooKeeper** (C) harici koordinasyon sistemleridir (ZooKeeper eski Kafka'da kullanılırdı), RabbitMQ'nun yerleşik motoru değildir; **Redis** (D) tamamen farklı bir kullanım alanıdır.

# rquorum-6 | İleri
## Soru
Bir ekip "quorum queue en yüksek erişilebilirliği verdiği için **her kuyruğu** quorum queue yapalım, hatta tek düğümlü ortamda bile" diyor. En doğru eleştiri nedir?
## A
Quorum queue veri güvenliği düşük bir türdür; kritik verilerde kullanılması sakıncalıdır
🤖 Tam tersi; quorum queue veri güvenliği yüksek türdür. Eleştiri güvenlik değil, maliyet/uygunluk olmalı.
## B
Quorum queue yalnızca Streams ile birlikte çalışır; tek başına bir kuyruk olarak kurulamaz
🤖 Yanlış; quorum queue bağımsız çalışan bir kuyruk türüdür, Streams'e bağımlı değildir.
## C
Quorum queue çoğaltma maliyeti getirir ve anlamlı çoğunluk için en az 3 düğüm ister
🤖 Doğru: quorum queue'nun bedeli vardır ve her senaryo (özellikle tek düğüm) için doğru araç değildir.
## D
Quorum queue tek bir kuyruk içinde bile mesaj sırasını (ordering) garanti etmez, bu yüzden risklidir
🤖 Quorum queue tek kuyruk içi sırayı korur; eleştiri sıralama değil, maliyet/topoloji uygunluğudur.
## Doğru
C
## Açıklama
"Her yere quorum queue" yaklaşımının sorunu **maliyet ve topoloji uygunluğudur**. Quorum queue her yazıyı Raft ile çoğaltır ve çoğunluk onayını bekler; bu **ek gecikme, ağ ve disk maliyeti** getirir ve anlamlı HA için **en az 3 düğüm** ister (çoğunluk = 2). **Tek düğümlü** bir ortamda çoğaltacak başka düğüm olmadığından HA avantajı yoktur ama Raft ek yükü ve "daima kalıcı" davranışı yine ödenir — bu senaryoda gereksizdir. Çok kısa ömürlü, yeniden üretilebilir veya HA gerektirmeyen kuyruklarda **klasik kuyruk** daha hafif ve uygundur. Quorum queue veri güvenliği **yüksek** bir türdür (A yanlış), Streams'e bağımlı değildir (B) ve kuyruk içi sırayı korur (D); doğru eleştiri "her araç her işe uymaz"dır.
