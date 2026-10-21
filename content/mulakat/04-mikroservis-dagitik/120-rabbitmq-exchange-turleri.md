---
slug: rabbitmq-exchange-turleri
baslik: "RabbitMQ Exchange Türleri: Direct, Topic, Fanout, Headers ve Yönlendirme Esnekliği"
kademe: Orta
sira: 120
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir postaneyi düşün. Sabah binlerce mektup geliyor. Postanenin bir dağıtım masası var; her mektubun üstündeki adrese bakıp onu doğru mahallenin kutusuna atıyor. **Yönlendirme (routing)** işte tam olarak budur: gelen bir mesajın hangi kutuya (kuyruğa) gideceğine karar vermek.

RabbitMQ'da bu dağıtım masasının adı **mesaj dağıtıcısıdır (exchange)**. RabbitMQ'nun en güçlü yanı da şudur: dağıtım masasının **kuralını sen seçersin**. Kimi masa yalnızca adresi harfi harfine tutan mektupları geçirir. Kimisi adresin yalnız bir parçasına bakar. Kimisi de adrese hiç bakmadan mektubun kopyasını **herkese** dağıtır. Bu seçim, exchange'in **türüdür**.

Bu neden önemli? Çünkü yönlendirme kuralı **broker'da** yaşarsa, yeni bir dinleyici eklemek için tek satır uygulama kodu değiştirmen gerekmez — yeni bir kuyruk açıp bağlarsın, biter.

### Temel kavramlar

- **Kuyruk (queue)**: mesajların beklediği kutu.
- **Mesaj dağıtıcısı (exchange)**: mesajın ilk uğradığı dağıtım masası. Üretici mesajı **daima** buraya yayımlar, asla doğrudan kuyruğa koymaz.
- **Yönlendirme anahtarı (routing key)**: üretici mesaja iliştirdiği adres etiketi, ör. `log.odeme.error`. Noktayla ayrılmış "kelimelerden" oluşur.
- **Bağ (binding)** ve **bağ anahtarı (binding key)**: bir kuyruğu bir exchange'e bağlayan kural ve o kuralın beklediği adres deseni.
- **Joker (wildcard)**: desen içinde "buraya herhangi bir şey gelebilir" anlamına gelen özel işaret.

### Nasıl çalışır?

Dört yerleşik exchange türü vardır:

- **direct (birebir eşleşen)**: yönlendirme anahtarı ile bağ anahtarı **birebir eşitse** mesaj kuyruğa gider. Nokta atışıdır (ör. `odeme.basarili` → yalnızca ödeme kuyruğu).
- **topic (desenle eşleşen)**: bağ anahtarı bir **desen** içerebilir. Burada iki **joker (wildcard)** vardır ve bunları karıştırmak klasik bir hatadır: **`*` tam bir kelimeyi**, **`#` ise sıfır veya daha çok kelimeyi** eşler.
- **fanout (herkese dağıtan)**: yönlendirme anahtarını **tamamen yok sayar** ve mesajı bağlı **tüm** kuyruklara kopyalar. Saf yayındır (broadcast / **yayınla-abone ol**, pub-sub).
- **headers (başlığa bakan)**: yönlendirme anahtarı yerine mesajın **başlık (header) tablosuna** bakar. Eşleşme kuralı `x-match=all` (hepsi tutmalı) veya `x-match=any` (biri yetsin) ile belirlenir.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dört exchange türü ve yönlendirme davranışı">
  <defs>
    <marker id="rex-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Exchange türü = yönlendirme kuralı</text>
  <rect x="20" y="44" width="150" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">direct</text>
  <text x="95" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">key == binding key</text>
  <text x="95" y="106" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">birebir eşleşme</text>
  <rect x="185" y="44" width="150" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="260" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">topic</text>
  <text x="260" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">log.*.error / emir.#</text>
  <text x="260" y="106" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">* = 1 kelime, # = 0..n</text>
  <rect x="350" y="44" width="150" height="72" rx="10" fill="#FBE9E7" stroke="#C0562B" stroke-width="1.5"/>
  <text x="425" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#C0562B" font-family="system-ui">fanout</text>
  <text x="425" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">key YOK SAYILIR</text>
  <text x="425" y="106" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tüm kuyruklara yayın</text>
  <rect x="515" y="44" width="130" height="72" rx="10" fill="#F3ECF7" stroke="#7A4F9E" stroke-width="1.5"/>
  <text x="580" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#7A4F9E" font-family="system-ui">headers</text>
  <text x="580" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">başlık tablosu</text>
  <text x="580" y="106" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">x-match all / any</text>
  <text x="330" y="160" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Varsayılan (adsız) exchange: direct'tir, binding key = kuyruk adı.</text>
  <text x="330" y="184" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Eşleşmeyen mesaj → alternate exchange tanımlıysa oraya, değilse düşer.</text>
  <line x1="120" y1="200" x2="540" y2="200" stroke="#5D7789" stroke-width="1.5" marker-end="url(#rex-ok)"/>
  <text x="330" y="214" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">artan yönlendirme esnekliği</text>
</svg>
```

Joker farkını örnekle sabitleyelim. Binding key `log.*.error` olsun:

- `log.odeme.error` → **eşleşir** (ortada tam bir kelime var).
- `log.error` → eşleşmez (`*` bir kelimeyi **zorunlu** kılar, boş geçilmez).
- `log.odeme.kritik.error` → eşleşmez (`*` iki kelimeyi karşılamaz; bunun için `log.#.error` gerekir).
- `error.odeme.log` → eşleşmez (eşleşme sıralıdır, kelimelerin yeri önemlidir).

```svg
<svg viewBox="0 0 660 226" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="topic exchange joker eşleşmesi: yıldız bir kelime, kare işareti sıfır veya çok kelime">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">topic joker: * = tam 1 kelime, # = 0..n kelime</text>
  <rect x="180" y="40" width="300" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">routing key: log.odeme.error</text>
  <rect x="40" y="90" width="270" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="54" y="110" font-size="11" fill="#1C3247" font-family="system-ui">log.*.error</text>
  <text x="298" y="110" text-anchor="end" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">eşleşir ✓</text>
  <rect x="40" y="126" width="270" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="54" y="146" font-size="11" fill="#1C3247" font-family="system-ui">log.#</text>
  <text x="298" y="146" text-anchor="end" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">eşleşir ✓</text>
  <rect x="350" y="90" width="270" height="30" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="364" y="110" font-size="11" fill="#1C3247" font-family="system-ui">log.error  (orta kelime eksik)</text>
  <text x="608" y="110" text-anchor="end" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">✗</text>
  <rect x="350" y="126" width="270" height="30" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="364" y="146" font-size="11" fill="#1C3247" font-family="system-ui">*.error  (2 kelime bekler)</text>
  <text x="608" y="146" text-anchor="end" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">✗</text>
  <text x="330" y="182" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">* tek kelimeyi zorunlu kılar (ne eksik ne fazla); # kelime sayısından bağımsız önek eşler.</text>
  <text x="330" y="206" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">İki kelimelik orta için log.#.error gerekir; log.*.error yalnız bir kelime kabul eder.</text>
</svg>
```

İki ayrıntı mülakatta sık sorulur. Birincisi **varsayılan (adsız) exchange**'tir. Adı boş metin (`""`) olan özel bir **direct** exchange'tir. **Aracı sunucu (broker)**, her kuyruğu otomatik olarak **kendi adıyla** bu exchange'e bağlar. `basicPublish("", "siparisKuyrugu", ...)` çağrısı bu yüzden çalışır: kuyruğun adı yönlendirme anahtarı olarak kullanılır. Yani ne rastgele bir davranıştır ne de fanout.

İkincisi **yedek dağıtıcıdır (alternate exchange)**. Bir exchange hiçbir bağ (binding) ile eşleşme bulamazsa mesaj normalde sessizce düşer. Exchange'e bir yedek dağıtıcı tanımlarsan, eşleşmeyen mesajlar kaybolmak yerine oraya gider; oradan da bir "yakalanamayanlar" kuyruğuna düşer.

Bunu **ölü mektup dağıtıcısıyla (DLX)** karıştırma. DLX, bir kuyruğa **girmiş** sonra reddedilen ya da yaşam süresi (TTL) dolan mesajlar içindir. `mandatory=false` zaten varsayılandır ve hiçbir şey yakalamaz. Kuyruğun TTL ayarının da yönlendirmeyle ilgisi yoktur.

```svg
<svg viewBox="0 0 660 218" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="direct nokta atışı yönlendirir, fanout tüm kuyruklara kopyalar">
  <defs>
    <marker id="rex-rt" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı mesaj, farklı yönlendirme: direct vs fanout</text>
  <text x="165" y="48" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">direct — nokta atışı</text>
  <rect x="30" y="88" width="90" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="75" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">direct</text>
  <rect x="220" y="60" width="90" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="265" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Q: odeme</text>
  <rect x="220" y="122" width="90" height="34" rx="7" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="265" y="144" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Q: kargo</text>
  <line x1="120" y1="104" x2="216" y2="80" stroke="#25804C" stroke-width="2" marker-end="url(#rex-rt)"/>
  <text x="150" y="86" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">key=odeme</text>
  <text x="180" y="150" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">eşleşmez</text>
  <text x="495" y="48" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">fanout — yayın</text>
  <rect x="360" y="88" width="90" height="40" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="405" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">fanout</text>
  <rect x="540" y="60" width="90" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="585" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Q: log</text>
  <rect x="540" y="122" width="90" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="585" y="144" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Q: cache</text>
  <line x1="450" y1="104" x2="536" y2="80" stroke="#B26B00" stroke-width="2" marker-end="url(#rex-rt)"/>
  <line x1="450" y1="112" x2="536" y2="138" stroke="#B26B00" stroke-width="2" marker-end="url(#rex-rt)"/>
  <text x="495" y="106" text-anchor="middle" font-size="9.5" fill="#B26B00" font-family="system-ui">key YOK SAYILIR</text>
  <text x="330" y="196" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">direct: key eşleşen TEK kuyruğa. fanout: bağlı TÜM kuyruklara kopya (yeni abone = yeni kuyruk, üreticide değişiklik yok).</text>
</svg>
```

### Mülakatta ne sorulur?

- "Birebir eşleşme hangi tür?" — **direct**.
- "Herkese yayın hangi tür?" — **fanout**; yeni abone eklemek için sadece yeni kuyruk açıp bağlarsın, üretici hiç değişmez.
- "`log.*.error` neyi eşler?" — tam üç kelimelik, ortası tek kelime olan anahtarları.
- "Exchange adı boş string ne demek?" — varsayılan (adsız) direct exchange.
- "Yönlendirilemeyen mesajı nasıl yakalarım?" — sunucu tarafında **alternate exchange**, üretici tarafında `mandatory` + `basic.return`.
- "RabbitMQ neden Kafka'dan esnek yönlendiriyor?" — Kafka'da mesaj bir topic/partition hedefler; içerik-tabanlı yönlendirme için tüketici kodu ya da Kafka Streams/ksqlDB gibi ek katman gerekir. RabbitMQ'da aynı iş **topic/headers exchange** ile, broker düzeyinde ve **bildirimsel** olarak yapılır. Bu bir depolama, ölçek veya sıralama iddiası değildir — Kafka kalıcı log tutar ve partition içinde güçlü sıra verir.

### Tek cümlede

Exchange türü, "bu mesaj hangi kuyruklara gitsin?" sorusunun cevabını aracı sunucuda ve kod yazmadan tanımlamanı sağlayan kuraldır. Kısaca: direct birebir eşler, topic desenle eşler, fanout herkese dağıtır, headers ise başlığa bakar.

# rex-1 | Temel
## Soru
Yalnızca routing key'i binding key ile **birebir eşit** olan mesajları ilgili kuyruğa gönderen exchange türü hangisidir?
## A
topic
🤖 topic desen (`*`/`#`) eşlemesi yapar; birebir eşitlik gerektiren tür bu değildir.
## B
direct
🤖 Doğru: direct exchange, routing key ile binding key'in tam eşitliğine göre yönlendirir.
## C
fanout
🤖 fanout routing key'i yok sayıp tüm kuyruklara yayar; eşleşme aramaz.
## D
headers
🤖 headers routing key yerine başlık tablosuna bakar; birebir key eşitliği kullanmaz.
## Doğru
B
## Açıklama
**direct** exchange en basit yönlendirmeyi yapar: mesajın routing key'i, bir binding'in binding key'iyle **tam olarak eşitse** o kuyruğa iletir. Nokta atışı senaryolar için idealdir (ör. `odeme.basarili` → yalnız ödeme kuyruğu). **topic** (A) desen eşlemesi içindir; **fanout** (C) key'i hiç dikkate almadan yayar; **headers** (D) ise routing key yerine mesaj başlıklarına bakar. Not: varsayılan (adsız) exchange de bir direct exchange'tir ve kuyruklar ona adlarıyla bağlıdır.

# rex-2 | Temel
## Soru
Routing key'i **tamamen yok sayıp** kendisine bağlı **tüm** kuyruklara mesajı kopyalayan exchange türü hangisidir? (Yayın / pub-sub)
## A
direct
🤖 direct key eşitliğine bakar; tüm kuyruklara koşulsuz yaymaz.
## B
topic
🤖 topic desen eşlemesi yapar; yalnızca desene uyan kuyruklara gider, hepsine değil.
## C
headers
🤖 headers başlık koşuluna bakar; koşula uymayan kuyruğa göndermez.
## D
fanout
🤖 Doğru: fanout routing key'i yok sayar ve bağlı tüm kuyruklara kopyalar.
## Doğru
D
## Açıklama
**fanout** exchange, routing key'i **tamamen görmezden gelir** ve mesajı kendisine bağlı **her** kuyruğa kopyalar. Klasik yayın (broadcast) / yayınla-abone ol (pub-sub) senaryosudur: ör. bir "yapılandırma değişti" olayını dinleyen tüm servislere aynı anda ulaştırmak. Diğer türler koşullu yönlendirir: **direct** (A) key eşitliği, **topic** (B) desen, **headers** (C) başlık koşulu. Yeni bir aboneyi eklemek için sadece yeni bir kuyruk oluşturup fanout'a bağlamak yeterlidir; üreticide değişiklik gerekmez.

# rex-3 | Orta
## Soru
Bir **topic** exchange'de binding key `log.*.error` tanımlı. Aşağıdaki routing key'lerden hangisi bu binding ile eşleşir?
## A
`log.odeme.error`
🤖 Doğru: `*` tam bir kelimeyi (odeme) eşler; üç bölüm de uyuyor.
## B
`log.error`
🤖 `*` tam BİR kelime gerektirir; ortadaki kelime eksik olduğu için eşleşmez.
## C
`log.odeme.kritik.error`
🤖 `*` yalnızca tek kelime; iki kelime (odeme.kritik) `*` ile eşleşmez, bunun için `#` gerekir.
## D
`error.odeme.log`
🤖 topic eşleşmesi sıralıdır; kelime dizilimi `log.*.error` desenine uymuyor.
## Doğru
A
## Açıklama
**topic** exchange'de routing key noktayla ayrılmış kelimelerden oluşur ve binding key desen içerebilir: **`*` tam bir kelimeyi**, **`#` sıfır veya daha çok kelimeyi** eşler. `log.*.error` deseni "log ile başlayıp, ortada tam bir kelime olup, error ile biten" anahtarları eşler → `log.odeme.error` (A) uyar. `log.error` (B) ortadaki kelime eksik olduğu için, `log.odeme.kritik.error` (C) `*` tek kelimelik olduğu için (burada `log.#.error` gerekirdi) eşleşmez; `error.odeme.log` (D) sıra yanlıştır. topic, direct'in nokta atışı ile fanout'un genişliği arasında ayarlanabilir bir orta yol sunar.

# rex-4 | Orta
## Soru
Uygulama kodunuz mesajı `channel.basicPublish("", "siparisKuyrugu", ...)` ile, yani exchange adı **boş string** vererek yayımlıyor. Burada ne olur?
## A
Publish hata verir; AMQP'de exchange adı boş bırakılamayan zorunlu bir alandır
🤖 Boş string geçerlidir; varsayılan exchange'i ifade eder, hata olmaz.
## B
Mesaj, broker'da o an tanımlı kuyruklardan rastgele seçilen birine yönlendirilir
🤖 Yönlendirme rastgele değildir; varsayılan exchange routing key'i kuyruk adı olarak kullanır.
## C
Boş string varsayılan (adsız) direct exchange'i gösterir; routing key kuyruk adı sayılır
🤖 Doğru: her kuyruk varsayılan exchange'e kendi adıyla otomatik bağlıdır.
## D
Mesaj fanout gibi davranıp broker'daki tüm kuyruklara birer kopya olarak dağıtılır
🤖 Varsayılan exchange fanout değil direct'tir; yalnızca adı eşleşen kuyruğa gider.
## Doğru
C
## Açıklama
Boş string (`""`) exchange adı **varsayılan (default / adsız) exchange**'i belirtir. Bu özel bir **direct** exchange'tir ve broker, **her kuyruğu otomatik olarak kendi adıyla** buna bağlar. Dolayısıyla routing key olarak bir kuyruk adı verildiğinde mesaj o kuyruğa gider — "doğrudan kuyruğa gönderiyormuş gibi" görünen kodun altındaki mekanizma budur. Rastgele (B) veya fanout (D) davranışı yoktur; boş string tamamen geçerlidir (A yanlış). Üretim kodunda genelde açıkça adlandırılmış exchange kullanmak, yönlendirmeyi merkezi ve değiştirilebilir kıldığı için tercih edilir.

# rex-5 | İleri
## Soru
Bir exchange'e gelen ve **hiçbir binding ile eşleşmeyen** mesajların sessizce düşmesini istemiyor, bunları ayrı bir "yakalama" kuyruğunda toplamak istiyorsunuz. RabbitMQ'da bunun için hangi mekanizmayı kullanırsınız?
## A
Exchange'e bir alternate exchange (alternatif exchange) tanımlamak
🤖 Doğru: alternate exchange tam olarak yönlendirilemeyen mesajları toplamak içindir.
## B
Mesajı `mandatory=false` bayrağıyla yayımlayıp broker'ın yakalamasını beklemek
🤖 `mandatory=false` zaten varsayılandır ve mesajı yakalamaz; eşleşmeyen mesaj yine düşer.
## C
Exchange'e bir ölü mektup exchange'i (DLX) tanımlayıp eşleşmeyenleri oraya bağlamak
🤖 DLX bir kuyruğa GİRMİŞ sonra reddedilen/TTL'i dolan mesajlar içindir; hiç yönlendirilememiş mesaj için değil.
## D
Hedef kuyruğa mesaj TTL'i verip süresi dolan mesajları ayrı bir kuyrukta toplamak
🤖 TTL mesaj yaşını sınırlar; yönlendirilemeyen mesajı yakalamakla ilgisi yoktur.
## Doğru
A
## Açıklama
**alternate exchange (AE)**, bir exchange'e tanımlanan yedek yönlendirme yoludur: ana exchange gelen bir mesajı **hiçbir binding ile eşleştiremezse**, mesajı sessizce düşürmek yerine tanımlı alternate exchange'e iletir — oradan da genelde bir "yakalanamayanlar" (unroutable) kuyruğuna bağlanır. Bu, sessiz mesaj kaybını önlemenin sunucu-taraflı yoludur (üretici tarafında `mandatory` + `basic.return` alternatifidir). **DLX** (C) farklı bir aşamadır: mesaj bir kuyruğa **girdikten** sonra reddedilirse veya TTL'i dolarsa devreye girer; hiç yönlendirilememiş mesajı kapsamaz. `mandatory=false` (B) varsayılandır ve hiçbir şey yakalamaz; TTL (D) yönlendirmeyle ilgisizdir.

# rex-6 | İleri
## Soru
Bir mimar "RabbitMQ, aynı olay akışı için Kafka'ya göre daha esnek yönlendirme sunar" diyor. Bu iddianın en somut teknik dayanağı nedir?
## A
RabbitMQ mesajları her zaman diskte tutar; Kafka ise mesajları yalnızca bellekte tutup hemen siler
🤖 Kafka kalıcı bir log tutar. Ayrıca bu depolamayla ilgilidir, yönlendirmeyle değil.
## B
RabbitMQ tüketici sayısını sınırsız ölçekler; Kafka ise partition sayısıyla sıkı biçimde sınırlıdır
🤖 Ölçek/paralellik ayrı bir konudur; iddia yönlendirme esnekliğiyle ilgili ve bu cümle onu desteklemez.
## C
Kafka'da içerik-tabanlı yönlendirme tüketici tarafında ek kod ister; RabbitMQ'da topic/headers exchange bunu broker düzeyinde sağlar
🤖 Doğru: yönlendirme mantığının broker'da bildirimsel olarak kurulabilmesi somut farktır.
## D
RabbitMQ mesaj sıralamasını (ordering) Kafka'dan daha güçlü garanti ettiği için yönlendirme serbestliği artar
🤖 Sıralama garantileri ayrı bir tartışmadır (Kafka partition içinde güçlü sıra verir); bu, yönlendirme esnekliği iddiasını desteklemez.
## Doğru
C
## Açıklama
İddianın somut dayanağı **yönlendirmenin nerede tanımlandığıdır**. Kafka'da bir mesaj bir **topic** (ve içindeki partition) hedefler; "şu başlığı taşıyan veya şu desene uyan mesajları farklı tüketicilere gönder" gibi **içerik-tabanlı yönlendirme** için tüketici tarafında filtreleme kodu ya da Kafka Streams/ksqlDB gibi ek katman gerekir. RabbitMQ'da ise **topic** (desen: `*`/`#`) ve **headers** (başlık koşulu) exchange'leriyle bu yönlendirme **broker düzeyinde, bildirimsel (declarative)** olarak, uygulama kodu yazmadan kurulur ve sonradan değiştirilebilir. Depolama (A) iddiası zaten ters — Kafka kalıcı log tutar; ölçek (B) ve sıralama (D) gerçek tartışma başlıkları olsa da bu **yönlendirme esnekliği** iddiasının dayanağı değildir.
