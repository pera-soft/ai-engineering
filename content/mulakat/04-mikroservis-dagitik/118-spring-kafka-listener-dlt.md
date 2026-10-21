---
slug: spring-kafka-listener-dlt
baslik: "Spring Kafka: @KafkaListener, DefaultErrorHandler ve @RetryableTopic + DLT"
kademe: İleri
sira: 118
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Kafka'dan mesaj okuyan kodu elle yazmak külfetlidir. Sonsuz bir döngü kurup mesaj istemeniz, gelen baytları nesneye çevirmeniz, "nereye kadar okudum" bilgisini kaydetmeniz ve hata çıkınca ne yapacağınıza karar vermeniz gerekir. Spring Kafka bunların hepsini üstlenir. Siz yalnız "şu **konuya (topic)** mesaj gelince şu metodu çalıştır" dersiniz. Kafka'da topic, mesajların yazıldığı adlandırılmış kanaldır. Asıl mesele de burada başlar: **mesaj işlenemezse ne olacak?** Sürekli patlayan tek bir mesajın bütün akışı kilitlemesine izin veremezsiniz.

### Temel kavramlar

- **`@KafkaListener`:** bir metodu bir (veya birkaç) topic'e bağlayan işaret. `topics` bir **dizi** alır, yani tek metot birden çok topic dinleyebilir.
- **Listener container (dinleyici konteyneri):** arka planda `poll()` döngüsünü çeviren Spring bileşenidir. `poll()`, "yeni mesaj var mı?" diye brokera sorma çağrısıdır. Konteyner gelen mesajı nesneye çevirir ve sizin metodunuza teslim eder. `containerFactory` ile özelleştirilir. **Belirtmezseniz** Spring Boot'un otomatik kurduğu `kafkaListenerContainerFactory` kullanılır; bunu yazmamak hata değildir.
- **`groupId` (tüketici grubu / consumer group):** aynı işi yapan tüketicilerin oluşturduğu takımdır. Bir topic'in **bölümleri (partition)** bu takımın üyeleri arasında paylaştırılır; bölüm, topic'in paralel okunabilmesi için ayrıldığı parçalardan her biridir. Her bölüm için **okuma konumu (offset)**, yani "nereye kadar okundu" bilgisi de grup adına saklanır. Farklı `groupId` değerleri aynı mesajları birbirinden bağımsız okur. Yani `groupId` sadece bir log etiketi değildir.
- **`concurrency`:** konteynerin başlatacağı paralel tüketici thread sayısı.
- **Yeniden deneme (retry):** hata veren mesajı bir süre sonra tekrar işlemeyi denemek. **Bekleme politikası (BackOff):** denemeler arasında ne kadar bekleneceğini belirler; süre sabit olabilir ya da her denemede katlanarak artabilir.
- **Ölü mektup konusu / DLT (dead letter topic):** hiçbir denemede işlenemeyen mesajların kenara alındığı ayrı topic. Amacı şudur: hiçbir zaman işlenemeyecek "zehirli mesajın" (poison pill) tüm akışı tıkamasını önlemek.
- **Onay (ack):** okuma konumunun kalıcı olarak kaydedilmesi (commit), yani Kafka'ya "bu mesajı bitirdim" demek.

### Nasıl çalışır?

**Paralellik kuralı.** Kafka'da bir partition, bir grup içinde aynı anda **yalnız bir** tüketiciye atanır. Dolayısıyla `concurrency`, dinlenen topic'in **partition sayısını aşamaz**. Örnek: 3 partition ve `concurrency=5` verirseniz yalnız 3 thread partition alır, kalan 2 thread boşta bekler. Bu bir hata değildir; uygulama normal açılır, sadece iki thread israf olur. Kafka bir partition'ı ikiye bölüp iki thread'e paylaştırmaz. Tüketici tarafı partition sayısını da artıramaz. Gerçekten 5 paralel işlem istiyorsanız önce topic'i 5 partition'a çıkarmalısınız.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="3 partition, concurrency 5: yalnız 3 thread partition alır, 2 thread boşta bekler">
  <defs>
    <marker id="spk-conc-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">concurrency ≤ partition sayısı</text>

  <text x="120" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">5 thread (concurrency=5)</text>
  <text x="500" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">3 partition</text>

  <rect x="60" y="72" width="120" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="120" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">thread 1</text>
  <rect x="60" y="104" width="120" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="120" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">thread 2</text>
  <rect x="60" y="136" width="120" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="120" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">thread 3</text>
  <rect x="60" y="168" width="120" height="26" rx="6" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="120" y="186" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">thread 4 — boşta</text>
  <rect x="60" y="200" width="120" height="26" rx="6" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="120" y="218" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">thread 5 — boşta</text>

  <rect x="440" y="72" width="120" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="500" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">partition 0</text>
  <rect x="440" y="104" width="120" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="500" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">partition 1</text>
  <rect x="440" y="136" width="120" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="500" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">partition 2</text>

  <line x1="180" y1="85" x2="438" y2="85" stroke="#5D7789" stroke-width="1.4" marker-end="url(#spk-conc-ok)"/>
  <line x1="180" y1="117" x2="438" y2="117" stroke="#5D7789" stroke-width="1.4" marker-end="url(#spk-conc-ok)"/>
  <line x1="180" y1="149" x2="438" y2="149" stroke="#5D7789" stroke-width="1.4" marker-end="url(#spk-conc-ok)"/>

  <text x="320" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Gerçek paralellik için önce partition sayısını artır — fazla thread yalnız israftır.</text>
</svg>
```

**Hata yönetimi.** Eski `SeekToCurrentErrorHandler`'ın yerini **`DefaultErrorHandler`** aldı. Bir `BackOff` nesnesiyle (ör. `FixedBackOff`, `ExponentialBackOff`) kaç kez ve hangi gecikmelerle yeniden deneneceğini tanımlarsınız. `new FixedBackOff(1000L, 3)` şu demektir: denemeler arası 1 saniye bekle, en fazla **3 yeniden deneme** yap. Yani 1 ilk deneme + 3 yeniden deneme = toplam 4 işleme girişimi olur. Sonsuz döngü oluşmaz; ilk hatada da doğrudan DLT'ye gidilmez.

Denemeler tükenince **`DeadLetterPublishingRecoverer`** devreye girer. Kaydı `<topic>.DLT` adlı topic'e yayınlar, okuma konumu ilerler ve **tüketim devam eder**. Konteyner durmaz. DLT yalnız baytları nesneye çevirme (deserialization) hatalarına özgü de değildir; kalıcı olarak işlenemeyen her mesaj oraya düşebilir.

**Ama `DefaultErrorHandler`'ın yeniden denemesi bloke edicidir (blocking).** Başarısız mesaj **aynı thread** üzerinde ve **aynı partition** içinde senkron olarak beklenir. Bekleme süresi uzunsa o partition'daki bütün mesajlar sırada bekler.

Buradan klasik bir tuzak doğar. `FixedBackOff(60_000, 10)` en kötü ihtimalle partition'ı yaklaşık 10 dakika kilitler. Tüketici bu süre boyunca `poll()` çağıramaz. İki `poll()` arasındaki süre **`max.poll.interval.ms`** sınırını (varsayılan 5 dakika) aşar. Bunun üzerine broker tüketiciyi **ölü** sayar, gruptan atar ve **bölümlerin tüketiciler arasında yeniden dağıtılmasını (rebalance)** tetikler. Yani "beklemeyi daha da uzatmak" çözüm değil, sorunun ta kendisidir. `concurrency` artırmak da kurtarmaz; her thread yine kendi partition'ını bloke eder.

```svg
<svg viewBox="0 0 640 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Non-blocking retry akışı: ana topic, gecikmeli retry topic'leri ve DLT; blocking retry ile karşıtlık">
  <defs>
    <marker id="spk-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Non-blocking retry (@RetryableTopic): ana partition bloke olmaz</text>

  <rect x="20" y="44" width="120" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">ana topic</text>
  <text x="80" y="85" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">akmaya devam eder</text>

  <rect x="175" y="44" width="120" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="235" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">retry-0</text>
  <text x="235" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gecikme: 5 sn</text>

  <rect x="330" y="44" width="120" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="390" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">retry-1</text>
  <text x="390" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gecikme: 25 sn</text>

  <rect x="485" y="44" width="130" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="550" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">DLT</text>
  <text x="550" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ölü mektup</text>

  <line x1="140" y1="70" x2="173" y2="70" stroke="#5D7789" stroke-width="1.6" marker-end="url(#spk-ok)"/>
  <line x1="295" y1="70" x2="328" y2="70" stroke="#5D7789" stroke-width="1.6" marker-end="url(#spk-ok)"/>
  <line x1="450" y1="70" x2="483" y2="70" stroke="#5D7789" stroke-width="1.6" marker-end="url(#spk-ok)"/>
  <text x="157" y="62" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hata</text>
  <text x="312" y="62" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yine hata</text>
  <text x="467" y="62" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tükendi</text>

  <text x="80" y="122" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">✓ tüketim akışı hiç durmaz</text>

  <line x1="20" y1="150" x2="620" y2="150" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>

  <text x="320" y="182" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Blocking retry (DefaultErrorHandler + uzun BackOff): partition tıkanır</text>

  <rect x="20" y="204" width="300" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="170" y="228" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">partition 0 — aynı thread</text>
  <text x="170" y="247" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">m1 senkron bekliyor (backoff 30 sn × 5)</text>
  <text x="170" y="263" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">m2, m3, m4 → SIRADA BEKLİYOR</text>

  <rect x="345" y="204" width="275" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="482" y="226" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">toplam bekleme &gt; max.poll.interval.ms</text>
  <text x="482" y="245" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">broker consumer'ı ölü sanar →</text>
  <text x="482" y="261" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gruptan atılır, rebalance tetiklenir</text>

  <line x1="320" y1="239" x2="343" y2="239" stroke="#5D7789" stroke-width="1.6" marker-end="url(#spk-ok)"/>
  <text x="320" y="300" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sonuç: uzun gecikmeli retry için blocking değil, non-blocking (@RetryableTopic) tercih edilir.</text>
</svg>
```

**Uzun gecikmeli yeniden denemenin doğru yolu: `@RetryableTopic` (bloke etmeyen / non-blocking).** Spring, ana topic'in yanına **ayrı yeniden deneme topic'leri** açar (`...-retry-0`, `...-retry-1`; gecikmeleri gittikçe artar). Ayrıca **DLT'yi de kendisi oluşturur**. Başarısız mesaj ana partition'da beklemez. Bir sonraki yeniden deneme topic'ine kopyalanır ve gecikmenin sonunda orada tekrar işlenir. Ana `poll()` döngüsü akmaya devam ettiği için `max.poll.interval.ms` aşılmaz. Deneme sayısını `attempts`, gecikme ve çarpanı `@Backoff` belirler; bu değerler sabit değildir. DLT'yi elle kurmanız da gerekmez.

**Manuel onay ve teslim garantisi.** `AckMode.MANUAL` (veya `MANUAL_IMMEDIATE`) modunda otomatik kayıt kapanır. Okuma konumunu siz `Acknowledgment.acknowledge()` ile ilerletirsiniz; çağırmazsanız konum hiç ilerlemez. Bu çağrının **yeri** teslim garantisini belirler.

Metodun **en başında** `acknowledge()` derseniz, iş sonradan patlasa bile okuma konumu ilerlemiş olur ve mesaj kaybolur. Buna **en-fazla-bir-kez (at-most-once)** denir. İş **başarıyla bittikten sonra** çağırırsanız, hata hâlinde konum ilerlemez ve mesaj yeniden teslim edilir. Buna da **en-az-bir-kez (at-least-once)** denir. İkinci yol, işin **tekrar edilse de sonucu değişmeyen (idempotent)** olmasını gerektirir; aynı mesaj iki kez işlenebilir çünkü. Onayı ayrı bir thread'e taşımak garantiyi bozar, çünkü kaydı işin sonucundan koparır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="acknowledge() işten önce çağrılırsa hata halinde mesaj kaybolur; işten sonra çağrılırsa yeniden işlenir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Manuel ack: acknowledge() nereye?</text>

  <rect x="20" y="42" width="600" height="88" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="36" y="64" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">EN BAŞTA ack → at-most-once (riskli)</text>
  <text x="36" y="88" font-size="10.5" fill="#1C3247" font-family="system-ui">ack.acknowledge() → veritabaninaYaz() ✗ hata</text>
  <text x="36" y="112" font-size="10.5" fill="#8A2E2E" font-family="system-ui">offset zaten ilerledi → mesaj KAYBOLUR</text>

  <rect x="20" y="142" width="600" height="88" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="36" y="164" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">EN SONDA ack → at-least-once (doğru)</text>
  <text x="36" y="188" font-size="10.5" fill="#1C3247" font-family="system-ui">veritabaninaYaz() ✗ hata → ack ÇAĞRILMAZ</text>
  <text x="36" y="212" font-size="10.5" fill="#25804C" font-family="system-ui">offset ilerlemez → mesaj YENİDEN işlenir (idempotentlik gerekir)</text>
</svg>
```

### Mülakatta ne sorulur?

`groupId` ne işe yarar, `containerFactory` zorunlu mu, tek metot birden çok topic dinleyebilir mi? `concurrency` partition sayısını aşarsa ne olur? `FixedBackOff(1000L, 3)` kaç deneme yapar ve sonunda mesaj nereye gider? `@RetryableTopic` bloke edici midir, DLT'yi kim oluşturur? `acknowledge()` nereye konur ve hangi teslim garantisini verir? Uzun bekleme süresi neden rebalance doğurur?

### Tek cümlede

Spring Kafka'da `@KafkaListener` mesajı getirir, `DefaultErrorHandler` ve DLT kısa süreli hataları kurtarır, uzun beklemeli denemeler partition'ı kilitlememek için `@RetryableTopic` ile yapılır ve `acknowledge()`'ın yeri at-most-once ile at-least-once farkını belirler.

# spk-1 | Orta
## Soru
Aşağıdaki dinleyici hakkında hangisi DOĞRUDUR?

```java
@KafkaListener(topics = "siparisler", groupId = "siparis-servisi",
               containerFactory = "kafkaListenerContainerFactory")
public void dinle(Siparis s) {
    // ...
}
```
## A
`groupId` yalnızca loglama ve izleme içindir; offset takibiyle hiçbir ilgisi yoktur
🤖 `groupId` offset commit'lerinin ve grup içi iş bölümünün anahtarıdır; yalnız log etiketi değildir.
## B
`groupId` tüketici grubunu belirler; grup üyeleri partition'ları paylaşır, offset'ler grup bazında tutulur
🤖 Doğru: tüketici grubu hem partition dağıtımının hem de offset'lerin izlendiği birimdir.
## C
`containerFactory` belirtmek zorunludur; verilmezse uygulama açılış sırasında hata verip durur
🤖 Verilmezse Spring Boot varsayılan `kafkaListenerContainerFactory`'yi kullanır; zorunlu değildir.
## D
Bir metot yalnızca tek bir topic dinleyebilir; birden fazla topic için ayrı metotlar şarttır
🤖 `topics` dizi kabul eder; tek metot birden çok topic dinleyebilir (ya da `topicPattern` kullanılır).
## Doğru
B
## Açıklama
**`groupId`** bir **tüketici grubunu** (consumer group) tanımlar: Kafka, bir topic'in partition'larını aynı grup içindeki tüketicilere paylaştırır ve **offset**'leri (nereye kadar okundu) grup bazında saklar. Farklı `groupId`'ler aynı mesajları bağımsızca okur. **`containerFactory`** verilmezse Spring Boot otomatik yapılandırdığı varsayılanı kullanır (zorunlu değil), ve `topics` bir dizi olduğundan tek metot birden fazla topic dinleyebilir. Bu yüzden yalnız B doğrudur.

# spk-2 | İleri
## Soru
Bir topic'in **3 partition**'ı var ve dinleyici konteynerinizi `concurrency = 5` ile yapılandırdınız. Sonuç ne olur?
## A
5 thread'in tümü aktif çalışır; Kafka partition'ları thread'ler arasında böler
🤖 Bir partition aynı anda tek thread'e atanır; partition bölünüp paylaşılmaz.
## B
Uygulama açılışta hata verir çünkü `concurrency` partition sayısını aşamaz
🤖 Hata olmaz; Spring fazlalığı sessizce boşta bırakır, açılışı engellemez.
## C
Yalnızca 3 thread partition alıp çalışır; kalan 2 thread boşta bekler
🤖 Doğru: her partition tek thread'e atanır, fazla thread'ler atanacak partition bulamaz.
## D
Kafka otomatik olarak topic'in partition sayısını 5'e çıkarıp thread'leri doldurur
🤖 Tüketici tarafı partition sayısını değiştiremez; bu topic yapılandırmasıdır.
## Doğru
C
## Açıklama
`concurrency`, konteynerin kaç paralel tüketici thread'i (her biri kendi `KafkaConsumer`'ı) başlatacağını belirler. Kafka'da **bir partition aynı anda bir grup içinde yalnız bir tüketiciye** atanır; bu yüzden **paralellik partition sayısıyla sınırlıdır**. 3 partition + 5 thread → yalnız **3 thread** partition alır, kalan **2 thread boşta** bekler (hata değildir, sadece israftır). Gerçekten 5 paralel işlem istiyorsanız topic'i en az 5 partition'a çıkarmanız gerekir. Bu, mülakatta sık sorulan `concurrency ≤ partition` kuralıdır.

# spk-3 | İleri
## Soru
`DefaultErrorHandler`'ı `new FixedBackOff(1000L, 3)` ve bir `DeadLetterPublishingRecoverer` ile yapılandırdınız. Bir mesajı işlerken metot her seferinde istisna fırlatıyor. Ne olur?
## A
Mesaj toplam 4 kez denenir (1 ilk + 3 retry, 1'er sn arayla), sonra DLT'ye yayınlanır
🤖 Doğru: `FixedBackOff(1000,3)` 3 yeniden deneme demek; tükenince recoverer DLT'ye yollar.
## B
Mesaj sonsuza dek yeniden denenir; `FixedBackOff` yalnız gecikmeyi belirler, deneme sayısını değil
🤖 İkinci argüman (`3`) maksimum deneme sayısıdır; sonsuz döngü olmaz.
## C
İlk hatada mesaj hemen DLT'ye gider; `FixedBackOff` ayarı tümüyle yok sayılır
🤖 Backoff yok sayılmaz; önce denemeler tükenir, ancak sonra DLT'ye gider.
## D
Hata konteyneri durdurur ve tüketim tümüyle durur; DLT yalnızca deserializasyon hataları için çalışır
🤖 `DeadLetterPublishingRecoverer` genel işleme hatalarında da devrededir; konteyner durmaz, mesaj DLT'ye gidip devam eder.
## Doğru
A
## Açıklama
**`DefaultErrorHandler`** (eski `SeekToCurrentErrorHandler` yerine) başarısız kaydı `BackOff` politikasına göre yeniden dener. **`FixedBackOff(1000L, 3)`** = her deneme arası 1 sn, en fazla **3 yeniden deneme** → toplam 1 ilk deneme + 3 retry = 4 işleme girişimi. Denemeler tükenince kayıt "kurtarılamaz" sayılır ve **`DeadLetterPublishingRecoverer`** onu bir **DLT** topic'ine (`<topic>.DLT`) yayınlar; ardından offset ilerler ve tüketim **devam eder** (zehirli mesaj tüm partition'ı sonsuza kilitlemez). Bu, "poison pill" mesajının akışı tıkamasını önlemenin standart yoludur.

# spk-4 | Orta
## Soru
Aşağıdaki dinleyici hakkında hangisi DOĞRUDUR?

```java
@RetryableTopic(attempts = "4",
    backoff = @Backoff(delay = 5000, multiplier = 2.0))
@KafkaListener(topics = "odemeler", groupId = "odeme-servisi")
public void isle(Odeme o) {
    // hata fırlarsa non-blocking retry devreye girer
}
```
## A
`@RetryableTopic` retry'ları ana topic'in aynı partition'ında senkron bekleterek gerçekleştirir
🤖 Tam tersi; `@RetryableTopic` non-blocking'tir, ana partition'ı bloke etmez.
## B
DLT'yi kendiniz elle oluşturup yapılandırmalısınız; `@RetryableTopic` yalnız retry topic'lerini üretir
🤖 `@RetryableTopic` denemeler tükenince otomatik bir DLT de oluşturur ve yönlendirir.
## C
Retry sayısı yapılandırılamaz; annotation'da ne yazarsa yazsın her zaman sabit 3'tür
🤖 `attempts` ile ayarlanır; burada 4 denemedir, sabit değildir.
## D
Spring artan gecikmeli ayrı retry topic'leri ve bir DLT oluşturur; ana partition bloke olmaz
🤖 Doğru: non-blocking model ayrı retry topic'leri + otomatik DLT kurar, ana akış durmaz.
## Doğru
D
## Açıklama
**`@RetryableTopic`** **non-blocking retry** sağlar: Spring, ana topic'in yanına **ayrı retry topic'leri** (`odemeler-retry-0`, `-retry-1`, … artan gecikmelerle; burada 5 sn, sonra ×2 = 10 sn …) ve **otomatik bir DLT** oluşturur. Başarısız kayıt ana partition'da **beklemez**; bir sonraki retry topic'ine kopyalanır, ana partition işlemeye devam eder. `attempts` deneme sayısını, `@Backoff` gecikme/çarpanı ayarlar. Denemeler tükenince mesaj **DLT'ye** gider — hepsi otomatik. Bu yüzden yalnız D doğrudur; DLT'yi elle kurmak (B) gerekmez.

# spk-5 | İleri
## Soru
`AckMode.MANUAL` ile yapılandırılmış bir dinleyicide **en-az-bir-kez** (at-least-once) teslim istiyorsunuz. `acknowledge()` çağrısını nereye koymalısınız?

```java
@KafkaListener(topics = "olaylar", containerFactory = "manuelAckFactory")
public void isle(Olay o, Acknowledgment ack) {
    // ... A: en başta ack.acknowledge();
    veritabaninaYaz(o);   // istisna fırlatabilir
    // ... B: en sonda ack.acknowledge();
}
```
## A
İş başlamadan, metodun EN BAŞINDA `acknowledge()` çağırıp offset'i hemen ilerletmek
🤖 Baştan commit edersen iş başarısız olsa bile offset ilerler; mesaj kaybolur (at-most-once olur).
## B
`acknowledge()` çağırmaya hiç gerek yok; `MANUAL` modda Spring periyodik olarak otomatik commit eder
🤖 MANUAL modda otomatik commit kapanır; `acknowledge()` çağırmazsan offset ilerlemez.
## C
İş (veritabanına yazma) BAŞARIYLA bittikten sonra, metodun sonunda `acknowledge()` çağırmak
🤖 Doğru: işi bitirdikten sonra commit edersen, hata halinde mesaj yeniden işlenir (at-least-once).
## D
`acknowledge()`'ı ayrı bir thread'de asenkron çağırmak, böylece dinleyici bloke olmaz
🤖 Offset commit'i işin sonucuna bağlanmalı; ayrı thread'e almak sıra ve garanti bütünlüğünü bozar.
## Doğru
C
## Açıklama
**`AckMode.MANUAL`** (veya `MANUAL_IMMEDIATE`) modunda offset commit'i tümüyle sizin elinizdedir; otomatik commit devre dışıdır. **En-az-bir-kez** garantisi için offset'i ancak iş **başarıyla tamamlandıktan sonra** commit etmelisiniz: `veritabaninaYaz(o)` çalıştıktan sonra `acknowledge()`. Böylece yazma sırasında istisna olursa offset **ilerlemez** ve mesaj yeniden teslim edilir (yeniden işlenir) — kayıp olmaz. Baştan commit etmek (A) offset'i işten önce ilerletir → hata olursa mesaj kaybolur (en-fazla-bir-kez). Bu yüzden C doğrudur. (Not: at-least-once, işin **idempotent** olmasını gerektirir, çünkü aynı mesaj tekrar işlenebilir.)

# spk-6 | İleri
## Soru
**[TUZAK]** Uzun süreli bir dış servis kesintisine karşı `DefaultErrorHandler`'a `new FixedBackOff(60_000L, 10)` (her deneme arası 60 sn, 10 deneme) verdiniz. Yük altında tüketiciler beklenmedik biçimde gruptan atılıp sürekli **rebalance** yaşıyor. En olası neden ve doğru çözüm nedir?
## A
`FixedBackOff` gecikmesi çok kısa kalmış; daha uzun bir backoff vermek rebalance'ı çözer
🤖 Sorun zaten uzun blocking backoff; daha da uzatmak rebalance'ı kötüleştirir, çözmez.
## B
Blocking retry partition'ı bekletir; toplam bekleme `max.poll.interval.ms`'i aşınca üye atılır — çözüm non-blocking `@RetryableTopic`
🤖 Doğru: blocking backoff `poll()` arası süreyi şişirir, limit aşılır ve rebalance olur; çözüm non-blocking retry'dir.
## C
Rebalance her zaman bir ağ sorunudur; kod tarafında yapılabilecek hiçbir şey yoktur
🤖 Burada neden koddaki uzun blocking backoff'tur; non-blocking retry'e geçmek doğrudan çözer.
## D
`concurrency`'yi artırmak tek çözümdür; daha çok thread beklemeyi bölüşüp rebalance'ı önler
🤖 Daha çok thread her thread'in partition'ını bloke etmesini engellemez; kök neden blocking backoff'tur.
## Doğru
B
## Açıklama
**Bu klasik tuzaktır.** `DefaultErrorHandler`'ın retry'ı **bloke-edicidir** (blocking): başarısız mesaj **aynı thread**'de, **aynı partition**'da senkron beklenir. `FixedBackOff(60_000, 10)` en kötü halde ~10 dakika partition'ı kilitler. Kafka tüketicisi bu süre boyunca `poll()`'u çağıramaz; iki `poll()` arasındaki süre **`max.poll.interval.ms`** (varsayılan 5 dk) sınırını aşınca broker tüketiciyi **ölü** sayar, gruptan atar ve **rebalance** tetiklenir — bu da işi durdurup sorunu büyütür. Doğru çözüm: uzun gecikmeli/çok denemeli retry'ı **partition'ı bloke etmeyen** **`@RetryableTopic` (non-blocking)** ile yapmak; böylece bekleme ayrı retry topic'lerinde olur, ana `poll()` döngüsü akmaya devam eder ve `max.poll.interval.ms` aşılmaz. (Alternatif olarak backoff kısaltılıp `max.poll.interval.ms` artırılabilir ama uzun kesintiler için asıl doğru desen non-blocking retry'dir.)
