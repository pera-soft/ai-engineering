---
slug: kafka-producer-idempotence
baslik: "Kafka Producer: acks, Idempotent Producer ve Batching"
kademe: İleri
sira: 109
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo şubesine paket bırakıyorsun. İki şeyi merak edersin. Birincisi: memur "aldım" diyene kadar bekleyecek misin, yoksa paketi bırakıp gidecek misin? İkincisi: internet koptuğu için formu iki kez gönderdiysen karşı tarafta **iki paket** mi görünecek?

Kafka'ya mesaj yazan programa **üretici** (producer) denir ve bu iki soru onun iki ayarıdır: onay beklemeyi `acks`, çift kayıt olmamasını `enable.idempotence` yönetir. Üçüncü bir konu daha vardır: her paketi tek tek yollamak yerine birkaçını **bir kutuda toplayıp** göndermek — buna **partileme** (batching) denir ve hızı belirler.

### Temel kavramlar

- **Üretici (producer):** Kafka'ya mesaj yazan program.
- **`acks` (onay seviyesi):** üreticinin, yazma isteğine kaç sunucudan "yazdım" cevabı bekleyeceği. `acks=0` hiç onay beklemez; en hızlısıdır, kayıp riski en yüksek olandır. `acks=1` yalnız **lider** yazınca onay verir; lider, bir bölümün yazma ve okuma sorumlusu olan kopyadır. `acks=all` ise o anki **senkron kopyalar kümesindeki (ISR — in-sync replicas)** kopyaların tümü yazınca onay verir; en dayanıklısıdır ve `min.insync.replicas` ile birlikte anlam kazanır.
- **Yeniden deneme (retry):** ağ hatası veya zaman aşımı sonrası aynı mesajı tekrar göndermek. Onay kaybolmuş ama mesaj aslında yazılmış olabilir → **yinelenme (duplicate)** riski.
- **Tekrar edilse de sonucu değişmeyen üretici (idempotent producer):** aynı mesaj yeniden deneme yüzünden iki kez ulaşsa bile log'a **bir kez** yazılmasını sağlayan mekanizma. "Idempotent" demek, aynı işlemi kaç kez tekrarlarsan tekrarla sonucun değişmemesi demektir.
- **PID (producer id) ve sıra numarası (sequence number):** **Kafka sunucusu (broker)** her üretici oturumuna bir kimlik (PID) verir. Üretici de her **bölüm (partition)** için mesajlara **artan** bir sıra numarası koyar. Broker, o (PID, bölüm) çifti için bir sonraki beklediği numarayı hatırlar.
- **Partileme (batching):** mesajları hemen değil, kısa süre biriktirip topluca göndermek. `linger.ms` bekleme süresi, `batch.size` **bayt** cinsinden parti eşiği, `compression.type` (lz4/zstd/snappy) sıkıştırma yöntemidir.
- **`max.in.flight.requests.per.connection` (yoldaki istek sayısı):** aynı bağlantı üzerinde cevabı aynı anda beklenen istek sayısı. Yani kaç isteğin aynı anda "havada" olabileceği.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Idempotent producer PID ve sequence ile retry yinelenmesini onler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Idempotent producer: (PID, partition) → sequence takibi</text>
  <rect x="24" y="46" width="150" height="120" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Producer</text>
  <text x="99" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">PID=42</text>
  <text x="99" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">seq: 5</text>
  <text x="99" y="134" text-anchor="middle" font-size="10" fill="#B4322B" font-family="system-ui">ağ hatası → retry</text>
  <text x="99" y="150" text-anchor="middle" font-size="10" fill="#B4322B" font-family="system-ui">seq 5 tekrar gönderilir</text>
  <text x="255" y="86" font-size="11" fill="#5D7789" font-family="system-ui">seq=5 →</text>
  <text x="255" y="126" font-size="11" fill="#5D7789" font-family="system-ui">seq=5 (retry) →</text>
  <rect x="360" y="46" width="256" height="120" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="488" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Broker (partition lideri)</text>
  <text x="488" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">beklenen sonraki seq: 6</text>
  <text x="488" y="116" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">ilk seq=5 → yaz</text>
  <text x="488" y="138" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">retry seq=5 &lt; 6 → ATIL (duplicate)</text>
  <text x="320" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kapsam: tek producer session + tek partition. Session'lar arası dedup için transactions gerekir.</text>
</svg>
```

### Nasıl çalışır?

1. Üretici mesajı bir **bölüme (partition)** yönlendirir. Anahtar (key) varsa anahtarın hash'ine göre, anahtar yoksa dengeli dağıtımla seçer. Sonra mesajı **biriktirici (accumulator)** denen tampona koyar.
2. `linger.ms` süresi dolduğunda ya da parti `batch.size` eşiğine ulaştığında parti sıkıştırılır. Sonra **tek bir istek** hâlinde o bölümün liderine gönderilir.
3. Broker mesajı yazar ve `acks` ayarına göre onay döner. `acks=all` ise onayı, ISR'daki bütün kopyalar yazana kadar bekletir.
4. Onay gelmezse üretici mesajı **yeniden gönderir (retry)**. Idempotence açıkken broker gelen sıra numarasına bakar. Beklediğinden **küçük** bir numara gelirse bunu **yinelenme (duplicate)** sayıp sessizce atar. Arada boşluk bırakan, beklenenden **büyük** bir numara gelirse hata verir. Böylece hem çift kayıt önlenir hem de **sıra** korunur.
5. Kafka **3.0'dan beri `enable.idempotence=true` varsayılandır**; yanında `acks=all` da varsayılan gelir. Idempotence açıkken `max.in.flight.requests.per.connection` değeri **5'i geçmemelidir**. Bu sınır içinde sıra numarası takibi bölüm içi sırayı korur. Idempotence **kapalıyken** iş değişir: aynı anda yolda olan birden çok istek ve araya giren yeniden denemeler sırayı bozabilir. O durumda sırayı garanti etmenin tek yolu `max.in.flight=1`'dir, bu da verimi düşürür.

`acks` seçimi hız ile dayanıklılık arasında bir kademe seçtirir:

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="acks sifir bir all onay seviyeleri hiz dayaniklilik dengesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">acks: onay seviyesi — hız ↔ dayanıklılık dengesi</text>
  <rect x="24" y="44" width="188" height="120" rx="10" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="118" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">acks=0</text>
  <text x="118" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">onay beklemez</text>
  <text x="118" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gönder-unut</text>
  <text x="118" y="132" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">en HIZLI</text>
  <text x="118" y="150" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">kayıp riski en yüksek</text>
  <rect x="226" y="44" width="188" height="120" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">acks=1</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yalnız lider yazınca</text>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">onay verilir</text>
  <text x="320" y="132" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">orta denge</text>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">lider hemen çökerse kayıp</text>
  <rect x="428" y="44" width="188" height="120" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="522" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">acks=all</text>
  <text x="522" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ISR'ın tümü yazınca</text>
  <text x="522" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">onay verilir</text>
  <text x="522" y="132" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">en DAYANIKLI</text>
  <text x="522" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">min.insync.replicas ile anlamlı</text>
  <path d="M60 184 L580 184" stroke="#5D7789" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="320" y="202" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sağa gittikçe: daha çok onay, daha yüksek gecikme, daha güçlü dayanıklılık.</text>
</svg>
```

Verimin kaynağı ise partilemedir. Tekil mesajlar yerine **az sayıda ama büyük** istek gönderilir; böylece ağ turu sayısı düşer. Sıkıştırma da parti düzeyinde yapıldığı için sıkıştırma oranı yüksek çıkar.

`linger.ms=0` gecikmeyi en aza indirir ama küçük partiler yollar. Birkaç milisaniyelik bekleme çoğu zaman **iş hacmini (throughput)** belirgin biçimde artırır. Yani partileme bir **gecikme ↔ iş hacmi** takasıdır.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Batching linger ms batch size compression ile daha az istek">
  <defs>
    <marker id="kprod-batch-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Batching: linger.ms + batch.size + compression → az ama büyük istek</text>
  <text x="80" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">tekil mesajlar</text>
  <rect x="40" y="60" width="80" height="20" rx="3" fill="#EAF1F7" stroke="#186399" stroke-width="1"/><text x="80" y="74" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">msg</text>
  <rect x="40" y="86" width="80" height="20" rx="3" fill="#EAF1F7" stroke="#186399" stroke-width="1"/><text x="80" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">msg</text>
  <rect x="40" y="112" width="80" height="20" rx="3" fill="#EAF1F7" stroke="#186399" stroke-width="1"/><text x="80" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">msg</text>
  <rect x="40" y="138" width="80" height="20" rx="3" fill="#EAF1F7" stroke="#186399" stroke-width="1"/><text x="80" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">msg</text>
  <path d="M128 109 L168 109" stroke="#186399" stroke-width="1.8" marker-end="url(#kprod-batch-arrow)"/>
  <rect x="176" y="58" width="200" height="102" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="276" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Accumulator (parti)</text>
  <text x="276" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">linger.ms kadar bekle</text>
  <text x="276" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ya da batch.size (bayt) dolunca</text>
  <text x="276" y="144" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">compression: lz4/zstd/snappy</text>
  <path d="M384 109 L424 109" stroke="#186399" stroke-width="1.8" marker-end="url(#kprod-batch-arrow)"/>
  <rect x="432" y="70" width="176" height="78" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="520" y="98" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Broker'a TEK istek</text>
  <text x="520" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">daha az ağ turu</text>
  <text x="520" y="137" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">daha yüksek throughput</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">linger.ms=0 gecikmeyi düşürür ama küçük partiler yollar → gecikme/throughput dengesi.</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`acks` seviyeleri ne anlama gelir?"** 0 = onay yok (kayıp riski), 1 = yalnız lider onayı (lider hemen çökerse kayıp), all = ISR'ın tümü. `acks=all` tek başına "kayıp imkânsız" demek değildir; garantisi `min.insync.replicas` ile birlikte kurulur.
- **"Idempotent producer nasıl çalışır?"** Şifreleme, UUID ya da içerik hash'i **değil**: (PID, partition) başına **artan sequence** takibi. Broker tekrar gelen sequence'i atar. **3.0'dan beri varsayılan açık.**
- **"Kapsamı nedir?"** **Tek üretici oturumu** ve **tek bölüm (partition)**. Üretici yeniden başlarsa **yeni PID** alır; oturumlar arası tekillik için `transactional.id` ile **transactions** gerekir. Farklı üreticilerin ya da farklı topic'lerin mesajlarını tekilleştirmez.
- **"Idempotence = exactly-once mudur?"** Hayır. Yalnız üretici→broker retry yinelenmesini çözer; tüketicinin çift işlemesi ve dış sistemlerdeki yan etkiler kapsam dışıdır.
- **"Sıralama ile in-flight ilişkisi?"** Idempotence açık + `max.in.flight ≤ 5` → partition içi sıra korunur. Kapalıyken sıra ancak `max.in.flight=1` ile garanti edilir.
- **"`batch.size` adet mi, bayt mı?"** **Bayt**. Sıkıştırma üreticide yapılır, tüketicide açılır.

### Tek cümlede

Üretici tarafında dayanıklılığı `acks` (+ `min.insync.replicas`), yinelenmemeyi (PID, partition, sequence) üçlüsüne dayanan idempotence, verimi ise `linger.ms`/`batch.size`/sıkıştırma ile yapılan partileme belirler.

# kprod-1 | Orta
## Soru
`acks` değerlerinin (0 / 1 / all) anlamı nedir?
## A
`acks=0` en güvenli seçenektir; üretici mesajı her replikadan tek tek onaylatır
🤖 acks=0 hiç onay beklemez, en hızlı ama en riskli olandır — en güvenli değildir.
## B
`acks=1` ISR'ın tümü yazana kadar bekler; bu yüzden en dayanıklı seviye odur
🤖 acks=1 yalnız lider yazınca onay verir; tüm ISR'ı bekleyen acks=all'dır.
## C
`acks=0` onay beklemez, `acks=1` yalnız lider onayı, `acks=all` ISR'ın tümünün onayı demektir
🤖 Doğru: üç seviye hız/dayanıklılık dengesini kademeler.
## D
`acks=all` her koşulda, diğer ayarlardan bağımsız olarak veri kaybını imkânsız kılar
🤖 acks=all'ın garantisi min.insync.replicas'a bağlıdır; misr=1 ise koruma zayıflar.
## Doğru
C
## Açıklama
`acks` onay seviyesidir ve hız/dayanıklılık dengesini kademeler: **`acks=0`** üretici onay beklemeden gönderir — en hızlı ama kayıp riski en yüksek (A tersini iddia eder); **`acks=1`** yalnız **lider** yazınca onay — lider hemen çökerse veri kaybolabilir (B, bunu tüm replikalar sanır); **`acks=all`** **ISR'ın tümü** yazınca onay — en dayanıklı, ama garantisi `min.insync.replicas` ile birlikte anlamlıdır (D'nin "her koşulda" iddiası bu yüzden yanlıştır).

# kprod-2 | İleri
## Soru
Idempotent producer (idempotent üretici) nasıl çalışır ve varsayılan durumu nedir?
## A
Broker mesaj gövdesinin hash'ini hesaplar; aynı hash ikinci kez gelirse mesajı yazmaz
🤖 dedup içerik hash'ine değil, (PID, partition) başına artan sequence numarasına dayanır.
## B
Elle açılması gerekir; Kafka 3.0 ve sonrasında da varsayılan olarak kapalı gelir
🤖 3.0'dan beri varsayılan AÇIKTIR (enable.idempotence=true).
## C
Üretici her mesaja rastgele bir UUID ekler; broker bu string'leri karşılaştırarak yinelenmeyi bulur
🤖 mekanizma UUID string karşılaştırması değil; (PID, partition) başına artan sequence takibidir.
## D
Broker her (PID, partition) için beklenen sequence'i takip eder; tekrar gelen sequence atılır, 3.0+ varsayılan açıktır
🤖 Doğru: sequence takibi retry duplicate'ini eler ve 3.0'dan beri varsayılan etkindir.
## Doğru
D
## Açıklama
Idempotent producer, ağ hatası sonrası **retry**'ın aynı mesajı iki kez yazmasını engeller. Broker, her **(producer PID, partition)** için beklediği bir **sequence number** tutar; üretici retry'da aynı sequence'i tekrar gönderirse broker onu **duplicate** olarak **atar**, ama sırayı ve tam-bir-kez yazımı korur. Kafka **3.0'dan beri** bu varsayılan olarak **açıktır** (B tersini söyler). Mekanizma içerik hash'i (A) veya UUID string karşılaştırması (C) değildir — hafif, sequence tabanlı bir dedup'tır. Kapsamı **tek üretici oturumu** ve **tek partition**tır.

# kprod-3 | İleri
## Soru
`retries`, `max.in.flight.requests.per.connection` ve sıralama arasındaki ilişki nedir?
## A
Idempotence kapalıyken `max.in.flight > 1` + retry sırayı bozabilir; açıkken ≤5 in-flight istekte sıra korunur
🤖 Doğru: sequence numarası, birden çok uçuştaki isteğe rağmen sıranın korunmasını sağlar.
## B
Sıralama yalnızca `max.in.flight=1` ile korunur; idempotence açık olsa bile birden çok in-flight sırayı bozar
🤖 idempotence açıkken broker sequence ile ≤5 in-flight'ta sırayı garanti eder; max.in.flight=1 zorunlu değildir.
## C
En güvenli sıralama için `retries=0` ayarlanmalıdır; yeniden deneme her durumda sırayı bozar
🤖 retries=0 dayanıklılığı düşürür; idempotence ile retry hem güvenli hem sıralı olur.
## D
Sıralama partition sınırının dışında, topic genelinde de bu ayarlarla garanti altına alınabilir
🤖 sıralama yalnız partition içindir; bu ayarlar o kapsamda anlamlıdır.
## Doğru
A
## Açıklama
Sorun şudur: birden çok istek aynı anda "uçuşta" iken (`max.in.flight > 1`) biri başarısız olup **retry** edilirse, sonraki başarılı istek önce yazılıp sıra bozulabilir. **Idempotence kapalıyken** bunu önlemenin klasik yolu `max.in.flight=1`'dir (ama throughput düşer). **Idempotence açıkken** broker her mesajın **sequence**'ini bildiğinden, `max.in.flight` **≤5** olsa bile out-of-order yazımları reddedip sırayı **korur** — hem sıralı hem yüksek verimli olursun (B'nin yanıldığı nokta). `retries=0` (C) dayanıklılığı gereksiz feda eder. Tüm bu garantiler yalnız **partition içi** sıralama içindir (D yanlış).

# kprod-4 | İleri
## Soru
Batching (`linger.ms`, `batch.size`, `compression.type`) throughput'u nasıl etkiler?
## A
`linger.ms=0` her zaman throughput'u maksimize eder; göndermeden önce beklemek her durumda zararlıdır
🤖 linger.ms=0 gecikmeyi düşürür ama küçük partiler yollar; kısa bekleme partiyi doldurup verimi artırır.
## B
`linger.ms` partiyi doldurmak için kısa süre bekletir; `batch.size` ve compression ağ/disk verimini artırır
🤖 Doğru: batching daha az, daha büyük istek üretir; sıkıştırma parti düzeyinde verimi yükseltir.
## C
`batch.size` bir partiye kaç mesaj (adet) konacağını belirtir; bayt cinsinden bir eşik değildir
🤖 batch.size bayt (byte) cinsindendir, mesaj adedi değil.
## D
Compression yalnızca tüketici tarafında uygulanır; üretici sıkıştırılmamış veri göndermek zorundadır
🤖 sıkıştırma üreticide parti düzeyinde yapılır; tüketici açar.
## Doğru
B
## Açıklama
Batching, üreticiyi verimli kılar: `linger.ms` mesajları göndermeden önce kısa bir süre bekleterek onları bir **partiye** doldurur; böylece daha **az ama daha büyük** istekle ağ/disk verimi artar. `batch.size` bu partinin **bayt** eşiğidir (adet değil — C yanlış). `compression.type` (lz4, zstd, snappy) partiyi üreticide **sıkıştırır** (D yanlış — tüketici yalnız açar) ve ağ/depolama maliyetini düşürür. `linger.ms=0` gecikmeyi minimize eder ama küçük partilerle throughput'u düşürebilir (A'nın "her zaman maksimize" iddiası yanlış); bu bir **gecikme/throughput dengesidir**.

# kprod-5 | İleri
## Soru
Idempotent producer'ın yinelenme (duplicate) garantisinin kapsamı nedir?
## A
Garanti tek üretici oturumu (session) ve tek partition kapsamındadır; producer yeniden başlarsa yeni PID alır
🤖 Doğru: PID session'a bağlıdır; restart yeni PID demektir, önceki oturumun mesajıyla dedup yapılmaz.
## B
Idempotent producer aynı mesajı farklı topic'ler arasında da tekilleştirir; kapsam topic üstüdür
🤖 garanti partition düzeyindedir; topic'ler arası bir dedup yoktur.
## C
Farklı producer'ların gönderdiği aynı içerikli mesajlar da otomatik olarak de-dup edilir
🤖 dedup (PID, partition, sequence) üçlüsüne bağlıdır; farklı producer'lar farklı PID'lerdir, dedup edilmez.
## D
Uygulama seviyesindeki her türlü yinelenmeyi ortadan kaldırır; ek bir önlemeye gerek kalmaz
🤖 yalnız retry duplicate'ini önler; iş mantığı kaynaklı yinelenmeler için ayrıca önlem gerekir.
## Doğru
A
## Açıklama
Idempotent producer'ın garantisi **dardır**: yalnız **tek üretici oturumu** (session) içinde ve **tek partition** başına, **retry** kaynaklı yinelenmeyi önler. Producer **yeniden başlarsa yeni bir PID** alır; eski oturumda gönderdiği bir mesajı yeni oturumda tekrar yollarsa broker bunu **duplicate olarak göremez** — oturumlar arası dedup için **transactions** (`transactional.id`) gerekir. Farklı producer'ların (C) veya farklı topic'lerin (B) mesajlarını tekilleştirmez; iş mantığı kaynaklı yinelenmeleri de kapsamaz (D). Bu sınırları bilmemek, "idempotent producer açık, artık hiç duplicate yok" yanılgısına götürür.

# kprod-6 | Uzman
## Soru
[TUZAK] "Idempotent producer'ı açtık, artık uçtan uca exactly-once garantimiz var." Bu iddia neden yanlıştır?
## A
Doğrudur; idempotent producer tek başına uçtan uca exactly-once sağlar, başka mekanizma gerekmez
🤖 idempotent producer yalnız üretici→broker retry duplicate'ini önler; uçtan uca EOS'u kapsamaz.
## B
Idempotent producer, üretici tarafıyla birlikte tüketicideki çift işlemeyi de otomatik kapsar
🤖 tüketicinin çift işlemesi ayrı bir problemdir; idempotent producer buna dokunmaz.
## C
Yalnız üretici→broker retry yinelenmesini önler; tüketicinin çift işlemesini ve dış yan etkileri kapsamaz
🤖 Doğru: EOS çok katmanlı bir sözleşmedir; idempotent producer sadece bir katmanı çözer.
## D
`acks=all` ile birleştirildiğinde idempotent producer otomatik olarak exactly-once semantiğe dönüşür
🤖 acks=all dayanıklılıktır, exactly-once değil; EOS için transaction ve tüketici tarafı gerekir.
## Doğru
C
## Açıklama
Bu, mülakatta çok sevilen bir **kapsam tuzağıdır**. Idempotent producer yalnız **üretici→broker** yolundaki **retry** kaynaklı yinelenmeyi (tek session, tek partition) önler. **Uçtan uca exactly-once** (EOS) ise daha geniştir: tüketicinin bir mesajı **çift işlememesi**, birden çok partition'a yazımın **atomikliği** ve **downstream yan etkilerin** (DB, e-posta, ödeme) tekilliği de gerekir. Bunun için **transactions**, tüketicide `isolation.level=read_committed` ve genelde **idempotent tüketim/uygulama seviyesi dedup** şarttır. `acks=all` (D) yalnız dayanıklılıktır, EOS değil. Idempotent producer'ı EOS ile eşitlemek (A, B), sistemin çift-işleme risklerini görünmez kılar.
