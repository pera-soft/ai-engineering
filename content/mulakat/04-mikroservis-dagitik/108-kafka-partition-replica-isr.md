---
slug: kafka-partition-replica-isr
baslik: "Partition, Replikasyon ve ISR: High Watermark, acks=all ve Sessiz Veri Kaybı"
kademe: İleri
sira: 108
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sınıfın not defterini düşün. Tek bir defter varsa iki sorun çıkar: bir öğretmen defteri kullanırken diğerleri bekler; defter kaybolursa notlar tümden yok olur. Kafka bu iki soruna iki ayrı çözüm getirir: defteri **ciltlere böler** (bölüm) ve her cildin **birebir kopyasını** başka dolaplarda tutar (kopya). Birincisi hızı, ikincisi güvenliği verir.

Kopyalardan biri "asıl" defterdir: yazma ve okuma hep onun üzerinden gider, diğerleri onu takip edip aynı satırları kendilerine geçirir. Asıl defter kaybolursa, takip edenler arasından **yeterince güncel olan** biri asıl ilan edilir; iş durmaz, yazılmış satırlar kaybolmaz. Bu bölümde asıl konu şudur: bir satırın gerçekten "kayıtlı" sayılması için **kaç kopyada** bulunması gerekir?

### Temel kavramlar

- **Bölüm (partition):** bir konunun (topic) sıralı ve yalnız sonuna eklenen parçası. Hem paralelliğin hem de sıra garantisinin birimidir. Sıralama **yalnız bölüm içinde** garantilidir, konunun tamamında değil.
- **Kopya (replica):** aynı bölümün başka bir **Kafka sunucusunda (broker)** duran birebir kopyası.
- **Replikasyon çarpanı (replication factor, RF):** kaç kopya tutulacağı. RF=3 → aynı bölüm üç ayrı broker'da durur.
- **Lider (leader) / takipçi (follower):** kopyalardan biri liderdir; üretici ve tüketici **daima lider** ile konuşur. Kalan (RF−1) kopya takipçidir, lideri arka planda kopyalar.
- **ISR (In-Sync Replicas, senkron kopyalar):** lider dâhil, lidere **yeterince yakın** kopyaların kümesi. Bir takipçi `replica.lag.time.max.ms` boyunca geri kalırsa ISR'dan **düşer**, yetişince geri katılır — yani ISR **dinamiktir**.
- **LEO (Log End Offset — log sonu konumu):** bir kopyanın kayıt defterinin bittiği yer. Son yazılan mesajın **okuma konumunun (offset)** bir fazlasıdır.
- **High Watermark (HW — güvenli okuma sınırı):** ISR'daki **bütün kopyaların** kendine geçirdiği en yüksek offset. Yani "buraya kadarı kesin kayıtlı" sınırı. Tüketici yalnız HW'ye kadar okuyabilir; HW ile LEO arasındaki mesajlar henüz yolda sayılır. İlişki daima **HW ≤ LEO**'dur.
- **`acks`:** üreticinin kaç onay bekleyeceği. `acks=all` → o anki ISR'ın tümü yazana dek bekle.
- **`min.insync.replicas` (misr):** bir yazmanın kabul edilmesi için ISR'da bulunması gereken **en az** kopya sayısı.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ISR lider takipci HW ve LEO iliskisi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ISR: lider + senkron takipçiler · HW = tüm ISR'ın kopyaladığı offset</text>
  <text x="30" y="58" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Lider (P0)</text>
  <rect x="30" y="66" width="360" height="26" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="34" y="70" width="52" height="18" fill="#25804C" opacity="0.75"/><rect x="88" y="70" width="52" height="18" fill="#25804C" opacity="0.75"/><rect x="142" y="70" width="52" height="18" fill="#25804C" opacity="0.75"/><rect x="196" y="70" width="52" height="18" fill="#B5701B" opacity="0.7"/><rect x="250" y="70" width="52" height="18" fill="#B5701B" opacity="0.7"/>
  <text x="196" y="112" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">↑ HW (ISR onayladı)</text>
  <text x="310" y="112" text-anchor="middle" font-size="10.5" fill="#B5701B" font-family="system-ui">↑ LEO (henüz commit değil)</text>
  <text x="30" y="150" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Takipçi A (ISR)</text>
  <rect x="30" y="158" width="360" height="22" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="34" y="161" width="52" height="16" fill="#25804C" opacity="0.55"/><rect x="88" y="161" width="52" height="16" fill="#25804C" opacity="0.55"/><rect x="142" y="161" width="52" height="16" fill="#25804C" opacity="0.55"/>
  <text x="410" y="80" font-size="11" fill="#1C3247" font-family="system-ui">min.insync.replicas=2</text>
  <text x="410" y="100" font-size="11" fill="#1C3247" font-family="system-ui">+ acks=all</text>
  <text x="410" y="120" font-size="10.5" fill="#B4322B" font-family="system-ui">ISR &lt; 2 → yazma reddedilir</text>
  <text x="410" y="172" font-size="10.5" fill="#5D7789" font-family="system-ui">RF=3, misr=1 →</text>
  <text x="410" y="190" font-size="10.5" fill="#B4322B" font-family="system-ui">sessiz veri kaybı riski</text>
  <text x="30" y="210" font-size="10.5" fill="#5D7789" font-family="system-ui">Tüketici yalnız HW'ye kadar okur; HW ile LEO arası "uçuşta"dır.</text>
</svg>
```

### Nasıl çalışır?

1. Üretici mesajı **lider** kopyaya yazar; mesaj liderin log'unun sonuna eklenir ve LEO artar.
2. Takipçiler liderden veri **çeker** (fetch) ve aynı mesajı kendi log'larına yazar.
3. Lider, ISR'daki kopyaların nereye kadar geldiğini görünce **HW**'yi ilerletir. Tüketiciler ancak HW'ye kadar okuyabildiği için, okunan her mesaj zaten birden çok kopyada vardır.
4. `acks=all` ile üretici, o anki **ISR'ın tümü** yazana kadar onay beklemiş olur. Dikkat: "tümü" mutlak bir sayı değil, **o anki ISR**'dır — ISR tek kopyaya düşmüşse "tümü" = 1 demektir.
5. Bu yüzden `acks=all` **tek başına yetmez**; `min.insync.replicas` ile birlikte anlam kazanır. ISR misr'in altına inerse broker yazmayı **NotEnoughReplicas** hatasıyla reddeder, sessizce eksik kopyayla devam etmez.
6. Lider düşerse yeni lider **yalnız ISR içinden** seçilir; buna **lider seçimi (leader election)** denir. Atamayı, kümenin yönetim kurulu sayılan **denetleyici çoğunluğu (controller quorum)** yapar. İstemciler güncellenen **küme bilgisiyle (metadata)** yeni lidere yönelir.

Kopyaların yerleşimi ve lider seçimi şöyle görünür:

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RF uc replika yerlesimi ve lider secimi ISR icinden">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">RF=3: her replika ayrı broker'da · lider düşerse ISR'dan seçim</text>
  <text x="30" y="56" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">Normal</text>
  <rect x="30" y="64" width="180" height="48" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="120" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Broker 1</text>
  <text x="120" y="102" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">P0 · LİDER</text>
  <rect x="222" y="64" width="180" height="48" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="312" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Broker 2</text>
  <text x="312" y="102" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">P0 · takipçi (ISR)</text>
  <rect x="414" y="64" width="180" height="48" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="504" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Broker 3</text>
  <text x="504" y="102" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">P0 · takipçi (ISR)</text>
  <text x="30" y="148" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Broker 1 düşer → seçim</text>
  <rect x="30" y="156" width="180" height="48" rx="8" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.4" stroke-dasharray="4 3"/>
  <text x="120" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Broker 1 ✗</text>
  <text x="120" y="194" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">erişilemez</text>
  <rect x="222" y="156" width="180" height="48" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="312" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Broker 2</text>
  <text x="312" y="194" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">P0 · YENİ LİDER</text>
  <rect x="414" y="156" width="180" height="48" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="504" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Broker 3</text>
  <text x="504" y="194" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">P0 · takipçi</text>
  <text x="30" y="222" font-size="10.5" fill="#5D7789" font-family="system-ui">Yeni lider yalnız ISR içinden seçilir; controller quorum atamayı yönetir. Üretici/tüketici yeni liderle konuşur.</text>
</svg>
```

Tehlikeli ayar `unclean.leader.election.enable=true`'dur. Bu açıkken ISR'da **olmayan**, geride kalmış bir kopya da lider olabilir. Senaryo şöyledir: lider ölmüştür ve elde yalnızca geride kalmış bir kopya vardır. Kafka bölümü bekletmek yerine o kopyayı lider yapar. Sonuç ağırdır: sadece ölü liderde bulunan ve üreticiye **onaylandı** denmiş mesajlar sessizce yok olur.

Üretimde bu ayar `false` olmalıdır; kısa süreli erişilemezlik, sessiz veri kaybına yeğdir. RF=3 için pratik reçete şudur: `acks=all` + `min.insync.replicas=2` + `unclean.leader.election.enable=false`.

```svg
<svg viewBox="0 0 640 225" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Unclean leader election sessiz veri kaybi senaryosu">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">unclean.leader.election: geri kalmış replika lider olursa commit'li veri gider</text>
  <rect x="24" y="44" width="270" height="70" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="159" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1) ISR daralır</text>
  <text x="159" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Lider: offset 100'e dek yazdı</text>
  <text x="159" y="99" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Takipçi 60'ta kaldı → ISR'dan düştü</text>
  <rect x="346" y="44" width="270" height="70" rx="9" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="481" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">2) Lider çöker</text>
  <text x="481" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Geriye yalnız 60'lık, ISR-dışı replika kaldı</text>
  <text x="481" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">61–100 arası sadece ölü liderde vardı</text>
  <rect x="24" y="130" width="290" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">unclean=false (güvenli)</text>
  <text x="169" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ISR-dışı replika lider OLAMAZ</text>
  <text x="169" y="185" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">partition bekler, veri korunur</text>
  <rect x="326" y="130" width="290" height="66" rx="9" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="471" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">unclean=true (riskli)</text>
  <text x="471" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">60'lık replika lider olur</text>
  <text x="471" y="185" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">61–100 SESSİZCE KAYBOLUR</text>
  <text x="320" y="216" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Koruma: unclean.leader.election.enable=false + acks=all + min.insync.replicas≥2</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Sıralama garantisi nerede geçerli?"** Yalnız bölüm içinde. Sırası önemli olan mesajlar aynı **anahtarla (key)** aynı bölüme yönlendirilir.
- **"RF ile misr farkı ne?"** RF **kaç kopya saklanacağını**, misr **bir yazma için kaç kopyanın onaylaması gerektiğini** söyler. RF=3 + misr=1, RF'nin verdiği güveni yok eder: yazma fiilen `acks=1` gibi davranır.
- **"RF=3'te misr kaç olmalı?"** **2**. misr=3 tolerans bırakmaz; tek broker düşünce yazma tümden durur.
- **"HW ile LEO farkı?"** HW commit sınırıdır (tüm ISR onayladı, tüketici görebilir), LEO log'un sonudur. **HW ≤ LEO**; HW tüketicinin commit ettiği offset değildir.
- **"Unclean leader election neden kapalı olmalı?"** Açıkken ISR-dışı kopya lider olabilir → commit'li veri **sessizce** kaybolur. Kapalıyken partition bir süre yazılamaz kalır; erişilebilirlik yerine tutarlılık seçilmiş olur.
- **"`acks=all` veri kaybını imkânsız kılar mı?"** Hayır — garantisi `min.insync.replicas`'a bağlıdır.

### Tek cümlede

Partition paralelliğin ve sıranın birimidir; dayanıklılık yalnız RF'den değil, **ISR + `acks=all` + `min.insync.replicas≥2` + unclean leader election'ın kapalı olması** bileşiminden gelir.

# kpart-1 | Orta
## Soru
Partition neden vardır ve sıralama garantisi hangi kapsamdadır?
## A
Partition yalnızca depolamayı bölmek içindir; tüketici paralelliğine hiçbir katkısı yoktur
🤖 partition tam da tüketici paralelliğinin birimidir; bir grupta bir partition'ı en çok bir tüketici okur.
## B
Partition hem paralelliğin birimidir hem sıralamanın sınırıdır; sıra yalnız partition içinde garantilidir
🤖 Doğru: paralellik partition sayısıyla ölçeklenir ve sıralama partition kapsamındadır.
## C
Sıralama tüm topic genelinde garanti edilir; mesajın hangi partition'a düştüğü hiç fark etmez
🤖 Kafka topic genelinde toplam sıralama vermez; garanti partition içidir.
## D
Aynı tüketici grubunda bir partition'ı birden çok tüketici aynı anda paylaşarak okuyabilir
🤖 bir grup içinde bir partition tek bir tüketiciye atanır; fazla tüketici boşta kalır.
## Doğru
B
## Açıklama
Partition iki işlevi birden taşır: **paralellik** — bir tüketici grubunda bir partition en fazla **bir** tüketiciye atanır, dolayısıyla paralellik partition sayısıyla sınırlıdır (D yanlış); ve **sıralama** — Kafka mesaj sırasını **yalnızca partition içinde** garanti eder, topic genelinde toplam sıralama yoktur (C yanlış). Bu yüzden sırası önemli olan mesajlar (ör. aynı hesabın işlemleri) aynı key ile aynı partition'a yönlendirilir. Partition'ı salt depolama sanmak (A), Kafka'nın ölçekleme ve sıralama modelini yanlış kurar.

# kpart-2 | İleri
## Soru
Replication factor ve leader/follower ilişkisi için hangisi doğrudur?
## A
RF=3 demek, aynı veriyi tutan üç ayrı topic oluşturup elle senkron tutmak demektir
🤖 RF bir partition'ın kopya sayısıdır, ayrı topic'ler değil.
## B
Takipçi (follower) replikalar da üreticiden gelen yazma isteğini doğrudan kabul edip kendi başına yazar
🤖 yazma daima lidere gider; takipçiler lideri kopyalar, varsayılan okuma da liderdendir.
## C
Her partition'ın bir lideri ve (RF-1) takipçisi vardır; istemci liderle konuşur, takipçiler lideri kopyalar
🤖 Doğru: lider tek yazma/okuma noktasıdır, takipçiler dayanıklılık için senkronize olur.
## D
Lider erişilemezse herhangi bir takipçi kendiliğinden yazma kabul etmeye başlar; seçime gerek kalmaz
🤖 yeni lider bir seçim (election) ile belirlenir; rastgele takipçi kendiliğinden yazma kabul etmez.
## Doğru
C
## Açıklama
**Replication factor (RF)** bir partition'ın kaç kopyada tutulacağını söyler; kopyalar farklı broker'lara yerleşir. Her partition için kopyalardan biri **lider**, kalan **(RF-1)** tanesi **takipçidir**. Üretici ve tüketici **daima lider** ile konuşur (varsayılan); takipçiler lideri arka planda kopyalayarak dayanıklılık sağlar. RF=3'ü üç topic sanmak (A) veya takipçilerin serbestçe yazma kabul ettiğini düşünmek (B, D) replikasyon modelini bozar. Lider düşerse yeni lider ISR içinden **seçilir**, kendiliğinden atanmaz.

# kpart-3 | İleri
## Soru
ISR (In-Sync Replicas) kümesi nedir?
## A
Bir topic'in tüm replikaları her zaman ISR'dadır; bu küme kurulumdan sonra hiç değişmez
🤖 geri kalan replika ISR'dan düşer; ISR dinamik bir kümedir.
## B
ISR yalnızca lideri içerir; takipçi replikalar hiçbir koşulda ISR üyesi olamaz, sadece yedektir
🤖 yeterince güncel takipçiler de ISR üyesidir; ISR'ın amacı budur.
## C
ISR, silinmiş veya kapatılmış partition'ların broker'da tutulduğu bakım listesidir
🤖 ISR silinen partition listesi değil; canlı ve senkron replikaların kümesidir.
## D
ISR, lidere yeterince yakın (replica.lag.time.max.ms içinde) kopyalayan senkron replikaların kümesidir
🤖 Doğru: ISR güncel replikaları tutar ve dayanıklılık kararının temelidir.
## Doğru
D
## Açıklama
**ISR (In-Sync Replicas)**, lider dahil, lidere **yeterince yakın** kopyalayan replikaların kümesidir; bir takipçi `replica.lag.time.max.ms` süresince geri kalırsa ISR'dan **düşer**, tekrar yetişince geri katılır — yani ISR **dinamiktir** (A yanlış). `acks=all`, üreticinin **ISR'daki tüm** replikalar mesajı yazana kadar onay beklemesi demektir; High Watermark da ISR'ın kopyaladığı offset'e göre ilerler. ISR'ı yalnız lider (B) veya silinmiş partition listesi (C) sanmak, dayanıklılık ve HW mantığını yanlış kurar.

# kpart-4 | İleri
## Soru
High Watermark (HW) ile Log End Offset (LEO) arasındaki fark nedir?
## A
LEO log'un sonudur; HW tüm ISR'ın kopyaladığı en yüksek offset'tir ve tüketici yalnız HW'ye kadar görür
🤖 Doğru: HW commit sınırıdır; HW ile LEO arası henüz "uçuşta" olup okunamaz.
## B
HW her zaman LEO'ya eşittir; ikisi aynı offset'i gösterdiği için ayrı kavram sayılmazlar
🤖 takipçiler geri kaldığında HW < LEO olur; eşitlik ancak tümü yetiştiğinde geçicidir.
## C
HW, tüketicinin commit ettiği offset'tir; LEO ise üreticinin gönderdiği son mesajın offset'idir
🤖 HW tüketici commit'i değil, replikasyonun onayladığı sınırdır; ikisi farklı kavramlardır.
## D
LEO her zaman HW'den küçüktür; log sonu, commit sınırının bir miktar gerisinde kalır
🤖 LEO log'un sonudur, HW ondan büyük olamaz; ilişki tam tersidir (HW ≤ LEO).
## Doğru
A
## Açıklama
**LEO (Log End Offset)** bir replikanın log'unun sonudur — son yazılan mesajın bir fazlası. **High Watermark (HW)**, **tüm ISR** tarafından kopyalanmış en yüksek offset'tir ve **commit sınırıdır**: tüketici yalnız **HW'ye kadar** olan mesajları görebilir. HW ile lider'in LEO'su arasındaki mesajlar henüz tüm ISR'a ulaşmadığından "uçuşta" (in-flight) sayılır, okunamaz. Böylece lider çökse bile tüketici sadece dayanıklı hale gelmiş veriyi görmüş olur. HW=LEO sanmak (B) veya HW'yi tüketici commit'iyle (C) karıştırmak, görünürlük ve dayanıklılık garantisini yanlış modellemektir. İlişki daima **HW ≤ LEO**'dur (D tersini iddia eder).

# kpart-5 | İleri
## Soru
`acks=all` ile `min.insync.replicas` birlikte ne sağlar (RF=3 varsayalım)?
## A
`acks=all` tek başına, `min.insync.replicas`'tan bağımsız olarak tam dayanıklılık verir; ek ayar gerekmez
🤖 acks=all "mevcut ISR"a yazar; ISR küçülmüşse tek replika yeter, garanti zayıflar. misr ile birlikte anlamlıdır.
## B
`min.insync.replicas` yalnızca tüketiciyi ilgilendiren bir okuma ayarıdır; üreticinin yazmasını etkilemez
🤖 misr üreticinin yazma başarısını doğrudan belirler; ISR misr altına düşerse yazma reddedilir.
## C
En güvenli yapılandırma RF=3 ile `min.insync.replicas=3`'tür; bu ayar tek broker kaybına bile dayanır
🤖 misr=3, RF=3'te tek broker kaybında yazmayı durdurur (tolerans sıfır); önerilen misr=2'dir.
## D
`acks=all` + `min.insync.replicas=2` (RF=3): ISR 2'nin altına düşerse yazma NotEnoughReplicas ile reddedilir
🤖 Doğru: iki ayar birlikte "en az 2 kopya onaylamadan yazma kabul etme" kuralını mekanikleştirir.
## Doğru
D
## Açıklama
Dayanıklılık, iki ayarın **birlikte** çalışmasıyla gelir. `acks=all` üreticinin **mevcut ISR'ın tümü** yazana dek onay beklemesini sağlar; ama ISR tek replikaya düşmüşse "tümü" = 1 olur ve garanti zayıflar. İşte bu yüzden `min.insync.replicas` (misr) gerekir: ISR bu sayının altına inerse broker yazmayı **NotEnoughReplicas** ile **reddeder**. RF=3 için önerilen **misr=2**'dir: en az iki kopya onaylar (bir broker kaybına dayanır), ama misr=3 gibi tolerans-sıfır bir ayar (C) tek broker düşünce yazmayı tümden durdurur. `acks=all`'ı tek başına yeterli sanmak (A) veya misr'i tüketici ayarı sanmak (B), sessiz veri kaybına açık kapı bırakır.

# kpart-6 | Uzman
## Soru
[TUZAK] RF=3 ayarlı bir topic'te `min.insync.replicas=1` bırakılmış ve `acks=all` kullanılıyor. En büyük risk nedir?
## A
`min.insync.replicas=1` ile `acks=all` fiilen `acks=1` gibi davranır; tek lider onaylar, o lider ölürse onaylı veri gider
🤖 Doğru: misr=1, acks=all'ın koruyucu etkisini etkisiz kılar; RF yüksek olsa da sessiz kayıp mümkündür.
## B
`min.insync.replicas=1` hiçbir fark yaratmaz; RF=3 olduğu sürece veri her koşulda korunur, kayıp olmaz
🤖 RF depolanan kopya sayısıdır ama misr=1 yazmanın kaç kopyayca onaylanacağını 1'e indirir; koruma zayıflar.
## C
`min.insync.replicas` cluster'daki toplam broker sayısını belirler; bir kapasite ayarıdır, kayıpla ilgisi yoktur
🤖 misr broker sayısı değil, bir yazma için gereken minimum senkron replika sayısıdır; kayıpla doğrudan ilgilidir.
## D
`unclean.leader.election.enable=false` olduğu sürece `min.insync.replicas=1` güvenlidir; ISR daralsa bile onaylanan veri hiçbir koşulda kaybolmaz
🤖 unclean seçim kapalı olsa da misr=1, tek replika onayıyla veriyi "commit" sayar; o replika ölürse veri gider.
## Doğru
A
## Açıklama
Bu klasik bir **sessiz veri kaybı** tuzağıdır. RF=3 sadece **kaç kopya saklanacağını** söyler; **kaç kopyanın onaylaması gerektiğini** `min.insync.replicas` belirler. `misr=1` iken `acks=all`, "mevcut ISR'ın tümü" = **tek lider** anlamına gelir; yani fiilen `acks=1` gibi davranır. ISR daralıp yalnız lider kalmışken bir yazma "onaylanır", ardından lider çökerse ve geri kalmış bir replika devralırsa, **onaylanmış** görünen veri kaybolur. RF'yi tek başına güvence sanmak (B), misr'i broker sayısıyla karıştırmak (C) yaygın yanılgılardır. `unclean.leader.election.enable=false` gerekli ama **yeterli değildir** (D): asıl koruma `misr≥2` + `acks=all` kombinasyonudur.
