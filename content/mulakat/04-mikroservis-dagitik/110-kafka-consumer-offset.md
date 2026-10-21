---
slug: kafka-consumer-offset
baslik: "Kafka Consumer: Consumer Group, Offset Commit ve Teslim Semantiği"
kademe: İleri
sira: 110
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Kütüphanedeki kalın bir defteri birden çok kişinin okuyacağını düşün. Herkesin defteri baştan sona tek başına okuması yavaş olur. Bunun yerine üç kişilik bir **ekip** kurulur ve defterin ciltleri paylaştırılır: 1. cilt Ali'de, 2. cilt Ayşe'de, 3. cilt Can'da. Her ciltte **tek** okuyucu vardır; böylece hem hızlı okunur hem her cildin sırası bozulmaz. Ali hastalanırsa onun cildi ekipteki başka birine devredilir.

Herkes ayrıca "kaçıncı satıra kadar okudum" bilgisini kütüphaneye **yazılı** bırakır ve ertesi gün oradan devam eder. Kafka'da bu ekip **tüketici grubu (consumer group)**, bu yazılı not ise **okuma konumunun kaydedilmesi (offset commit)**'dir. Offset, bir mesajın bölüm içindeki sıra numarasıdır; "kaçıncı satıra kadar okudum" bilgisidir. Başka bir ekip (ör. raporlama ekibi) aynı defteri **kendi notuyla**, ötekini hiç etkilemeden okur.

### Temel kavramlar

- **Tüketici (consumer):** Kafka'dan mesaj okuyan program. Okumak mesajı **silmez**.
- **Tüketici grubu (consumer group):** aynı `group.id` ile çalışan tüketiciler. Bir grup içinde bir **bölüm (partition)** yalnız **tek** bir tüketiciye atanır. Bölüm, bir konunun (topic) paralel çalışan alt-günlüklerinden biridir. Paralelliğin tavanı bölüm sayısıdır; bölümden fazla tüketici **boşta** kalır.
- **Bağımsız gruplar:** farklı `group.id`'ler aynı topic'i birbirini etkilemeden okur; her grubun **kendi offset'i** vardır.
- **Okuma konumunu kaydetme (offset commit):** "bu grup, bu bölümde şuraya kadar işledi" kaydı. `__consumer_offsets` adlı **dahili** bir topic'te saklanır. Bu topic **compacted**'tır; yani her anahtar için yalnızca son değeri tutar. ZooKeeper'da saklanmaz.
- **Otomatik commit** (`enable.auto.commit=true`): `auto.commit.interval.ms` aralığıyla arka planda commit. Kolaydır ama işleme ile commit'i eşzamansız kılar.
- **Manuel commit** (`commitSync` / `commitAsync`): kod işini bitirince açıkça commit eder; teslim semantiğini kontrol etmenin yolu budur.
- **`auto.offset.reset`:** geçerli bir commit'li offset **yokken** (yeni grup) veya offset geçersizken nereden başlanacağı: `earliest` (en eskiden), `latest` (yalnız yeni gelenlerden), `none` (hata fırlat).
- **Yeniden dağıtım (rebalance):** grup üyeleri değiştiğinde bölümlerin üyeler arasında yeniden paylaştırılması.
- **Geride kalma (lag):** bölümün log ucu ile grubun kaydettiği offset arasındaki fark. Yani "kaç mesaj geride kaldım" sorusunun cevabı. **Offset** cinsindendir, milisaniye cinsinden değil; negatif olamaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Consumer group partition atamasi ve offset commit">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Consumer group: her partition tek tüketiciye · offset __consumer_offsets'te</text>
  <rect x="24" y="46" width="150" height="130" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Topic (3 partition)</text>
  <text x="99" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">P0</text>
  <text x="99" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">P1</text>
  <text x="99" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">P2</text>
  <rect x="230" y="46" width="180" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Group A</text>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">C1 → P0</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">C2 → P1</text>
  <text x="320" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">C3 → P2</text>
  <text x="320" y="160" text-anchor="middle" font-size="10" fill="#B4322B" font-family="system-ui">4. tüketici → boşta</text>
  <rect x="452" y="46" width="164" height="130" rx="10" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.5"/>
  <text x="534" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#B5701B" font-family="system-ui">Group B (bağımsız)</text>
  <text x="534" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kendi offset'i</text>
  <text x="534" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı veriyi okur</text>
  <text x="534" y="140" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">__consumer_offsets</text>
  <text x="534" y="156" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(compacted iç topic)</text>
</svg>
```

### Nasıl çalışır?

1. Tüketici `poll()` çağırır ve **Kafka sunucusundan (broker)** bir demet kayıt alır.
2. Kod kayıtları işler (veritabanına yazar, hesap yapar, mesaj gönderir...).
3. Okuma konumu kaydedilir (commit). Tüketici çökerse, yerine geçen tüketici bu kayıtlı konumdan devam eder.

Asıl mesele 2 ile 3 arasındaki **sıradır**, çünkü çökme tam oraya denk gelebilir:

- **İşle → sonra commit et:** çökme olursa offset ilerlememiştir ve mesaj **yeniden işlenir**. Buna **en az bir kez teslim (at-least-once)** denir: yinelenme mümkündür, kayıp yoktur.
- **Commit et → sonra işle:** çökme olursa offset ilerlemiştir ve mesaj bir daha okunmaz. Buna **en fazla bir kez teslim (at-most-once)** denir: kayıp mümkündür, yinelenme yoktur.

Hiçbiri tek başına **tam bir kez teslim (exactly-once)** vermez. Bunun için ya işlem (transaction) ya da **tekrar edilse de sonucu değişmeyen (idempotent)** bir tüketim gerekir.

Ayrıca `auto.offset.reset` yalnızca geçerli bir kayıtlı offset **yoksa** devreye girer. Normal bir yeniden dağıtımda (rebalance) kayıtlı offset korunur; okuma sıfırdan başlamaz.

```svg
<svg viewBox="0 0 640 228" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Isleme ve offset commit sirasi teslim semantigini belirler">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sıra semantiği belirler: crash penceresi işleme ↔ commit arasındadır</text>
  <text x="30" y="52" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">işle → sonra commit = at-least-once</text>
  <rect x="30" y="62" width="120" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/><text x="90" y="84" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">poll</text>
  <rect x="176" y="62" width="120" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/><text x="236" y="84" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">İŞLE</text>
  <rect x="322" y="62" width="120" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/><text x="382" y="84" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">COMMIT</text>
  <text x="309" y="58" text-anchor="middle" font-size="18" fill="#8A2E2E" font-family="system-ui">↯</text>
  <text x="470" y="76" font-size="10.5" fill="#8A2E2E" font-family="system-ui">crash burada →</text>
  <text x="470" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">offset ilerlemedi → tekrar</text>
  <text x="470" y="108" font-size="10.5" fill="#25804C" font-family="system-ui">işlenir (yinelenme)</text>
  <text x="30" y="146" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">commit → sonra işle = at-most-once</text>
  <rect x="30" y="156" width="120" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/><text x="90" y="178" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">poll</text>
  <rect x="176" y="156" width="120" height="34" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/><text x="236" y="178" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">COMMIT</text>
  <rect x="322" y="156" width="120" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/><text x="382" y="178" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">İŞLE</text>
  <text x="309" y="152" text-anchor="middle" font-size="18" fill="#8A2E2E" font-family="system-ui">↯</text>
  <text x="470" y="170" font-size="10.5" fill="#8A2E2E" font-family="system-ui">crash burada →</text>
  <text x="470" y="186" font-size="10.5" fill="#1C3247" font-family="system-ui">offset ilerledi → mesaj</text>
  <text x="470" y="202" font-size="10.5" fill="#8A2E2E" font-family="system-ui">kaybolur</text>
  <text x="30" y="222" font-size="10.5" fill="#5D7789" font-family="system-ui">Hiçbiri tek başına exactly-once vermez; onun için transactions / idempotent tüketim gerekir.</text>
</svg>
```

Tüketicinin **canlı** sayılması iki ayrı sınırla ölçülür. `session.timeout.ms` **kalp atışı (heartbeat)** sinyali içindir; bu sinyal ayrı bir iş parçacığından (thread) gönderilir. `max.poll.interval.ms` ise **iki `poll()` çağrısı arasında geçen işleme süresi** içindir.

İşleme uzun sürerse ikinci sınır aşılır. Tüketici gruptan düşer ve yeniden dağıtım başlar. Yerine atanan tüketici de aynı uzun işi yapınca aynı sınırı aşar; döngü baştan tekrarlar. Buna klasik olarak "sonsuz rebalance" denir. Çaresi üç tanedir: `max.poll.records` değerini küçültmek, `max.poll.interval.ms` değerini büyütmek ya da ağır işi ayrı bir kanalda (asenkron) yürütmek.

Tüketicinin geride kalması, log ucu ile commit edilen ofset arasındaki **lag** ile ölçülür:

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Consumer lag log ucu ile commit edilen offset arasindaki fark">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Consumer lag = log ucu (son offset) − commit edilen offset</text>
  <text x="40" y="66" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Partition log (offset →)</text>
  <rect x="40" y="76" width="560" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <rect x="42" y="78" width="300" height="36" rx="4" fill="#E9F4EE"/>
  <rect x="342" y="78" width="256" height="36" rx="4" fill="#FBEEDC"/>
  <line x1="342" y1="70" x2="342" y2="124" stroke="#25804C" stroke-width="2"/>
  <line x1="598" y1="70" x2="598" y2="124" stroke="#8A2E2E" stroke-width="2"/>
  <text x="190" y="100" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">işlenmiş (commit edildi)</text>
  <text x="470" y="100" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">LAG: işlenmeyi bekleyen</text>
  <text x="342" y="142" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">↑ commit offset</text>
  <text x="598" y="142" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">↑ log ucu (LEO)</text>
  <text x="40" y="170" font-size="10.5" fill="#5D7789" font-family="system-ui">Sürekli BÜYÜYEN lag: tüketici üretim hızına yetişemiyor → ölçekle (partition/tüketici) ya da işlemeyi hızlandır.</text>
  <text x="40" y="188" font-size="10.5" fill="#5D7789" font-family="system-ui">Lag offset cinsindendir (ms değil) ve negatif olamaz: tüketici log ucunun ötesini işleyemez.</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Bir grup içinde bir bölümü kaç tüketici okur?"** Bir. Fazla tüketici boşta kalır; paralellik bölüm sayısıyla sınırlıdır. Farklı gruplar ise tamamen bağımsızdır, her birinin kendi offset'i vardır.
- **"Ofset nerede saklanır?"** `__consumer_offsets` adlı iç, compacted topic'te — tüketicinin belleğinde ya da ZooKeeper'da değil (4.0'da ZooKeeper zaten yoktur).
- **"Otomatik mi manuel commit mi?"** Otomatik commit periyodiktir ve işlemeyle senkron değildir; teslim semantiğini kontrol etmek isteyen manuel commit yapar.
- **"at-least-once mi at-most-once mı?"** Bunu **işleme ile commit'in sırası** belirler. İşle-sonra-commit → at-least-once; commit-sonra-işle → at-most-once.
- **"`auto.offset.reset` ne zaman devreye girer?"** Yalnız geçerli commit'li ofset yoksa veya geçersizse. Commit sıklığı ayarı değildir (o `auto.commit.interval.ms`).
- **"Lag nedir?"** Log ucu − commit edilen ofset; **ofset** cinsinden, negatif olamaz. Sürekli büyüyorsa tüketici üretim hızına yetişemiyordur.
- **"Sürekli rebalance ve tekrar işleme?"** Önce `max.poll.interval.ms` aşımına bakılır; heartbeat'in ayrı thread'de olması bu sınırı kaldırmaz.

### Tek cümlede

Tüketici grubu bölümleri (partition) üyeleri arasında tekil olarak paylaştırır, nerede kalındığı `__consumer_offsets`'e commit edilir ve **işleme ile commit'in sırası** teslim semantiğinin at-least-once mı at-most-once mı olacağını belirler.

# kcons-1 | Orta
## Soru
Consumer group içinde partition ataması nasıl çalışır?
## A
Bir gruptaki tüm tüketiciler her partition'ı okur (broadcast); herkes bütün mesajları alır
🤖 bu broadcast davranışıdır; grup içinde her partition tek tüketiciye atanır, herkes her partition'ı okumaz.
## B
Gruptaki tüketici sayısı partition'dan fazlaysa fazlalar da yükü paylaşıp işlemeyi hızlandırır
🤖 partition'dan fazla tüketici boşta kalır; paralellik partition sayısıyla sınırlıdır.
## C
Farklı grupların offset'leri ortaktır; bir grup ilerlerse diğeri de o offset'ten devam eder
🤖 her grubun kendi bağımsız offset'i vardır; bir grubun ilerlemesi diğerini etkilemez.
## D
Grup içinde her partition tek bir tüketiciye atanır; farklı gruplar aynı veriyi bağımsız offset'lerle okur
🤖 Doğru: grup içi tekil atama + gruplar arası bağımsız okuma Kafka'nın çekirdek modelidir.
## Doğru
D
## Açıklama
Kafka'da **bir consumer group içinde** her partition **tek bir tüketiciye** atanır — böylece grup, veriyi bölüşerek işler ama bir partition'ın sırası korunur. Grup içi tüketici sayısı partition'dan fazlaysa **fazlalar boşta kalır** (B yanlış); paralellik partition sayısıyla tavanlıdır. **Farklı gruplar** ise aynı topic'i **bağımsız** okur, her birinin **kendi offset'i** vardır (C yanlış) — bu, aynı akışı hem gerçek zamanlı hem batch işleyen ekiplerin birlikte çalışmasını sağlar. Tüm tüketicilerin her mesajı aldığı broadcast (A) Kafka'nın grup modeli değildir.

# kcons-2 | İleri
## Soru
Offset commit nerede ve nasıl saklanır?
## A
Commit edilen offset `__consumer_offsets` iç (compacted) topic'inde tutulur; otomatik ya da manuel commit edilebilir
🤖 Doğru: offset kalıcı olarak iç topic'te saklanır ve commit stratejisi seçilebilir.
## B
Offset yalnızca tüketicinin belleğinde tutulur; tüketici yeniden başlarsa hep baştan okumak zorundadır
🤖 offset kalıcı olarak __consumer_offsets'te saklanır; restart'ta kaldığı yerden devam eder.
## C
Offset, Kafka 4.0'da controller quorum'u üzerinden ZooKeeper'a yazılır ve orada saklanır
🤖 ZooKeeper 4.0'da kaldırıldı; offset zaten uzun süredir __consumer_offsets iç topic'inde tutulur.
## D
Her poll çağrısı offset'i otomatik olarak diske yazar; bu davranış hiçbir ayarla kapatılamaz
🤖 commit poll'dan ayrıdır ve otomatik commit kapatılıp manuel yapılabilir.
## Doğru
A
## Açıklama
Tüketici, işlediği son konumu **offset commit** ile kalıcılaştırır ve bu, `__consumer_offsets` adlı **iç, compacted bir topic'te** saklanır — böylece tüketici yeniden başladığında **kaldığı yerden** devam eder (B yanlış). Commit stratejisi seçilebilir: **otomatik** (`enable.auto.commit=true`, periyodik) veya **manuel** (`commitSync`/`commitAsync`) — manuel commit teslim semantiğini kontrol etmenin yoludur (D'nin "kapatılamaz" iddiası yanlış). Offset artık **ZooKeeper'da tutulmaz**; eski sürümlerde kısmen öyleydi ama uzun süredir iç topic'te, KRaft'la birlikte ZooKeeper zaten yok (C yanlış).

# kcons-3 | İleri
## Soru
`auto.offset.reset` ne zaman ve nasıl devreye girer?
## A
Her rebalance'ta devreye girer ve tüketiciyi partition'ın en başından okumaya zorlar
🤖 rebalance commit edilmiş offset'i korur; reset yalnız geçerli offset yoksa devreye girer.
## B
Yalnız geçerli bir commit edilmiş offset yokken devreye girer: `earliest`, `latest` ya da `none`
🤖 Doğru: reset bir başlangıç noktası politikasıdır, sadece offset bilinmiyorsa kullanılır.
## C
Her zaman `latest` gibi davranır; bu davranış yapılandırmayla değiştirilemez, sabit bir kuraldır
🤖 earliest/latest/none seçilebilir; sabit değildir.
## D
Offset commit'inin kaç milisaniyede bir yapılacağını, yani commit sıklığını belirler
🤖 commit sıklığı auto.commit.interval.ms'tir; auto.offset.reset başlangıç noktası politikasıdır.
## Doğru
B
## Açıklama
`auto.offset.reset` bir **başlangıç noktası politikasıdır** ve yalnızca tüketicinin **geçerli bir commit edilmiş offset'i olmadığında** (ilk kez okuyan yeni grup) veya saklanan offset **artık geçersiz** olduğunda (ör. retention nedeniyle silinmiş) devreye girer. Seçenekler: `earliest` (mevcut en eski offset'ten), `latest` (yalnız yeni gelenlerden), `none` (offset yoksa hata fırlat). Normal rebalance'ta commit edilmiş offset korunur, reset tetiklenmez (A yanlış). Sabit `latest` değildir (C), commit sıklığıyla da ilgisizdir (D — o `auto.commit.interval.ms`'tir).

# kcons-4 | İleri
## Soru
İşleme ve offset commit sırası teslim semantiğini nasıl belirler?
## A
Önce commit edip sonra işlemek at-least-once verir; çökme olsa da mesaj yeniden işlenir
🤖 commit-sonra-işle, çökme durumunda mesajı kaybettirir; bu at-most-once'tır.
## B
Commit'in işlemeye göre sırası teslim semantiğini etkilemez; iki sıralama da aynı sonucu üretir
🤖 sıra tam da semantiği belirleyen unsurdur; hangisi önce yapıldığı sonucu değiştirir.
## C
İşle-sonra-commit → at-least-once; commit-sonra-işle → at-most-once; sırayı seçmek semantiği belirler
🤖 Doğru: crash penceresi commit ile işleme arasındadır; sıra hangi riski aldığını belirler.
## D
Her iki sıralama da tek başına exactly-once garantisi verir; ek bir mekanizmaya gerek yoktur
🤖 ikisi de tek başına exactly-once vermez; EOS ek mekanizmalar (transactions/idempotent tüketim) gerektirir.
## Doğru
C
## Açıklama
Teslim semantiği, **işleme** ile **offset commit** arasındaki **sıraya** bağlıdır çünkü kritik pencere ikisi arasındadır. **İşle → sonra commit et**: mesaj işlendikten sonra commit'ten önce çökersen, offset ilerlememiştir ve mesaj yeniden işlenir → **at-least-once** (yinelenme mümkün). **Commit et → sonra işle**: commit'ten sonra işlemeden önce çökersen, offset ilerlemiştir ve mesaj bir daha okunmaz → **at-most-once** (kayıp mümkün, A bunu at-least-once sanır). Sıra semantiği belirler (B yanlış); hiçbiri tek başına **exactly-once** vermez (D) — EOS için transactions veya idempotent tüketim gerekir.

# kcons-5 | İleri
## Soru
Consumer "lag" nedir?
## A
Lag, tüketicinin şimdiye dek işlediği toplam mesaj sayısıdır; büyümesi sağlıklı bir işarettir
🤖 lag işlenen toplam değil, işlenmemiş kalan (geride kalma) miktarıdır.
## B
Lag, partition'ın son offset'i ile tüketicinin commit ettiği offset arasındaki farktır
🤖 Doğru: lag işlenmeyi bekleyen mesaj miktarını ölçer; artışı tüketicinin yetişemediğini gösterir.
## C
Lag, broker'ın bir mesajı diske yazmasındaki gecikmedir ve milisaniye cinsinden ölçülür
🤖 lag ms cinsinden broker gecikmesi değil, offset cinsinden geride kalmadır.
## D
Lag negatif olabilir; tüketici log ucunun ötesini okuduğunda bu sağlıklı bir durumdur
🤖 tüketici log ucunun ötesini işleyemez; lag negatif olmaz.
## Doğru
B
## Açıklama
**Consumer lag**, bir partition'ın **son offset'i** (log ucu, yaklaşık HW/LEO) ile tüketicinin **commit ettiği offset** arasındaki farktır — yani **işlenmeyi bekleyen** mesaj miktarı. Sürekli **büyüyen lag**, tüketicinin üretim hızına yetişemediğinin en önemli sağlık sinyalidir (ölçeklemek veya işlemeyi hızlandırmak gerekir). Lag işlenen toplam değildir (A), ms cinsinden broker gecikmesi değildir (C — offset cinsindendir) ve tüketici log ucunun ötesine geçemeyeceği için **negatif olamaz** (D).

# kcons-6 | Uzman
## Soru
[TUZAK] Bir tüketici her poll'da uzun süren bir işlem (ör. yavaş dış API çağrısı) yapıyor ve grup sürekli rebalance oluyor, mesajlar tekrar tekrar işleniyor. En olası neden ve çözüm nedir?
## A
Uzun işlem sorun değildir; heartbeat ayrı bir thread'den gittiği için tüketici asla ölü sayılmaz
🤖 heartbeat ayrı gitse de poll'lar arası süre max.poll.interval.ms'i aşarsa tüketici gruptan düşer.
## B
Sorun `session.timeout.ms` aşımıdır; tek çözüm bu değeri çok büyük bir sayıya çekmektir
🤖 buradaki asıl sınır max.poll.interval.ms'tir; ayrıca timeout'u sonsuza çekmek ölü tüketiciyi gizler, doğru yaklaşım değildir.
## C
`max.poll.records` değerini artırmak her turda daha çok iş yaptırarak sorunu çözer
🤖 daha fazla kayıt çekmek poll'lar arası işleme süresini uzatır, sorunu ağırlaştırır.
## D
Poll'lar arası işleme `max.poll.interval.ms`'i aşıyor; çözüm daha az kayıt çekmek veya işi asenkron yapmaktır
🤖 Doğru: iki poll arası işleme süresinin aşımı klasik "rebalance döngüsü" nedenidir.
## Doğru
D
## Açıklama
Bu klasik bir **sonsuz rebalance** tuzağıdır. Kafka'da iki poll çağrısı arasında geçen süre `max.poll.interval.ms`'i **aşarsa**, tüketici kendini gruptan çeker (LeaveGroup) ve koordinatör partition'ları yeniden dağıtır; rebalance başlar. Yeni atanan tüketici de aynı uzun işi yapınca yine sınırı aşar ve **tekrar düşer** — mesajlar sürekli yeniden işlenir. Heartbeat'in ayrı thread'de olması bu sınırı **kaldırmaz** (A yanlış): heartbeat `session.timeout.ms`'i, işleme süresi ise `max.poll.interval.ms`'i ilgilendirir. Çözüm: **`max.poll.records`'u küçültmek** (her turda daha az iş — C tersini yapar), **`max.poll.interval.ms`'i büyütmek** veya ağır işi **asenkron/dışarıda** yürütmektir. Timeout'u sonsuza çekmek (B) gerçek ölü tüketicileri gizler, doğru çözüm değildir.
