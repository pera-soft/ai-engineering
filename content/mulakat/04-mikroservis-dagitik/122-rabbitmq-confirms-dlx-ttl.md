---
slug: rabbitmq-confirms-dlx-ttl
baslik: "RabbitMQ Publisher Confirms, DLX/DLQ ve TTL ile Gecikmeli Retry"
kademe: İleri
sira: 122
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

Postaneye bir mektup bıraktın. İki ayrı soru var ve çoğu kişi bunları karıştırır:

1. **"Postane mektubumu gerçekten aldı mı?"** — Bunun cevabı, gişedeki memurun sana verdiği **alındı belgesidir**. RabbitMQ'da buna **yayıncı onayları (publisher confirms)** denir.
2. **"Mektup gideceği mahalledeki kutuya girebildi mi?"** — Bu bambaşka bir sorudur. Adres yanlışsa postane mektubu almıştır ama kutuya koyamaz. RabbitMQ'da bu soruyu **zorunlu yönlendirme bayrağı (mandatory)** ile onun cevabı olan **geri dönüş bildirimi (`basic.return`)** izler. mandatory şu anlama gelir: "bu mesaj hiçbir kuyruğa giremezse sessizce silme, bana geri yolla".

Bir de üçüncü durum var: mektup kutuya girdi ama alıcı onu açıp "bu bana göre değil" diye geri verdi, ya da kutuda çok uzun bekleyip bayatladı. Postaneler bu mektupları çöpe atmaz; **ölü mektup** bölümüne koyar, orada incelenir. RabbitMQ'da bu bölümün adı **DLX/DLQ**'dur: ölen mesajları toplayıp yönlendiren **ölü mektup dağıtıcısı (DLX, dead-letter exchange)** ve onların biriktiği **ölü mektup kuyruğu (DLQ, dead-letter queue)**.

Bu üç mekanizmayı ayırt edebilmek, mülakatta "mesaj kayboldu, nereye gitti?" sorusunun tek cevabıdır.

### Temel kavramlar

- **Aracı sunucu (broker)**: mesajları alan, saklayan ve dağıtan RabbitMQ sunucusu.
- **Mesaj dağıtıcısı (exchange)**: üreticiden gelen mesajı alıp kurallara göre **kuyruklara (queue)** yönlendiren bileşen. Mesajı kendisi saklamaz.
- **Yayıncı onayları (publisher confirms)**: kanal "confirm" kipine alınır. Aracı sunucu mesajı güvenle işleyince üreticiye **eşzamansız (asenkron)** bir **onay (ack)** döner. İşleyemezse **ret (nack)** döner.
- **İşlem bloğu (transaction — `tx.select`/`tx.commit`)**: aynı güvenceyi veren eski yol. Ama her `commit`, **eşzamanlı (senkron)** bir gidiş-dönüş ve aracı sunucuda ek maliyet ister.
- **mandatory + `basic.return`**: mesaj hiçbir kuyruğa **giremezse** üreticiye geri döner.
- **Ölü mektup (dead letter)**: bir kuyruğa girmiş ama orada "ölmüş" mesaj.
- **Ölü mektup dağıtıcısı (DLX, dead-letter exchange)**: kuyruğa `x-dead-letter-exchange` ayarıyla tanımlanır; ölen mesajlar buraya yönlendirilir.
- **Ölü mektup kuyruğu (DLQ, dead-letter queue)**: DLX'e bağlı, ölü mesajların biriktiği ve incelendiği kuyruk.
- **Yaşam süresi (TTL, time-to-live)**: mesaja veya kuyruğa verilen ömür. Süre dolunca mesaj ölü mektup olur.
- **Kalıcı kuyruk (durable)**: aracı sunucu yeniden başlayınca kuyruk **tanımının** hayatta kalması.
- **Kalıcı mesaj (persistent)**: mesajın diske yazılması (delivery mode = 2).

### Nasıl çalışır?

**DLX + TTL** ikilisi, eklenti kurmadan **gecikmeli yeniden deneme (delay queue)** kurmanın klasik yoludur: başarısız mesaj, TTL'i 30 saniye olan ve **tüketicisi olmayan** bir "bekleme" kuyruğuna konur. Kuyrukta kimse dinlemediği için mesaj bekler; TTL dolunca ölü mektup olur ve o kuyruğun **DLX**'i onu **asıl işleme kuyruğuna** geri yollar. Sonuç: 30 saniye sonra yeniden denenmiş olur. Tüketicide `Thread.sleep` çağırmak aynı işi yapmaz. Tüketiciyi kilitler ve **tüketiciye peşin verilen mesaj sayısı (prefetch)** kotasındaki bir yeri boşuna işgal eder. `requeue=true` (kuyruğa geri koy) da çözüm değildir: mesajı **anında** geri koyar, yerleşik bir bekleme süresi yoktur.

Kalıcılık ise **iki koşulun birlikteliğidir**: kuyruk **durable** olmalı **ve** mesaj **persistent** yayımlanmalı. Biri eksikse restart sonrası mesaj yaşamaz — durable kuyruk yalnız kuyruk tanımını kurtarır, içindeki geçici mesajları değil.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="TTL dolan mesaj DLX üzerinden yeniden işleme kuyruğuna döner">
  <defs>
    <marker id="rconf-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#7A4F9E"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">TTL + DLX = gecikmeli retry (delay queue)</text>
  <rect x="30" y="60" width="150" height="60" rx="10" fill="#F3ECF7" stroke="#7A4F9E" stroke-width="1.5"/>
  <text x="105" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#7A4F9E" font-family="system-ui">Bekleme kuyruğu</text>
  <text x="105" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">TTL = 30s</text>
  <rect x="255" y="60" width="150" height="60" rx="10" fill="#FBE9E7" stroke="#C0562B" stroke-width="1.5"/>
  <text x="330" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#C0562B" font-family="system-ui">DLX</text>
  <text x="330" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">süre dolunca yönlendirir</text>
  <rect x="480" y="60" width="150" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="555" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Asıl kuyruk</text>
  <text x="555" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeniden işlenir</text>
  <line x1="180" y1="90" x2="251" y2="90" stroke="#7A4F9E" stroke-width="2" marker-end="url(#rconf-ok)"/>
  <line x1="405" y1="90" x2="476" y2="90" stroke="#7A4F9E" stroke-width="2" marker-end="url(#rconf-ok)"/>
  <text x="330" y="158" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Kalıcılık İKİ koşul ister: durable kuyruk + persistent mesaj.</text>
  <text x="330" y="182" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Biri eksikse restart sonrası mesaj yaşamaz. Confirms bunu üreticiye doğrular.</text>
  <text x="330" y="206" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Publisher confirms: senkron transaction'a göre asenkron ve yüksek verimli.</text>
</svg>
```

**Publisher confirms neden transaction'a yeğlenir?** Transaction'da üretici her `tx.commit` için **bekler**: senkron bir gidiş-dönüş ve broker tarafında ek yük. Confirms'te ise üretici yayımlamayı hiç durdurmaz; onaylar arka planda **asenkron** akar ve toplu izlenir. Fark güvenlik değildir; ikisi de teslim güvencesi verir. Fark mesaj sayısı da değildir; işlem bloğu da çok mesajı kapsar. Şifrelemeyle ise hiç ilgisi yoktur. Tek fark şudur: biri **eşzamanlı (senkron)**, diğeri **eşzamansız (asenkron)** çalışır.

```svg
<svg viewBox="0 0 660 222" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Transaction senkron bekler, publisher confirms asenkron akar">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Neden confirms? Senkron transaction vs asenkron onay</text>
  <text x="150" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">transaction: senkron</text>
  <rect x="30" y="66" width="80" height="30" rx="6" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="70" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">publish</text>
  <rect x="120" y="66" width="80" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="160" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">BEKLE commit</text>
  <rect x="210" y="66" width="80" height="30" rx="6" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="250" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">publish</text>
  <text x="160" y="120" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">her mesajda round-trip + kilit → yavaş</text>
  <line x1="20" y1="140" x2="640" y2="140" stroke="#D0D8DE" stroke-width="1"/>
  <text x="150" y="162" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">confirms: asenkron</text>
  <rect x="30" y="174" width="56" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="58" y="191" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">pub</text>
  <rect x="92" y="174" width="56" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="191" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">pub</text>
  <rect x="154" y="174" width="56" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="182" y="191" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">pub</text>
  <text x="430" y="192" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">üretici durmadan yayımlar; ack'ler arka planda akıp toplanır</text>
</svg>
```

**Bir mesajı ölü mektup yapan üç durum** vardır: (1) `basic.reject`/`basic.nack` ile **`requeue=false`** reddedilmesi, (2) mesajın veya kuyruğun **TTL**'inin dolması, (3) kuyruğun **uzunluk sınırını** (`x-max-length`) aşması. Bunları **tetiklemeyenler** de en az o kadar önemlidir: başarılı **ack** mesajı normal biçimde siler, **`requeue=true`** ise mesajı asıl kuyruğa geri koyar. **mandatory** de DLX'i tetiklemez; o tamamen farklı bir aşamadır (hiç kuyruğa **giremeyen** mesaj).

```svg
<svg viewBox="0 0 660 224" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir mesajı dead-letter yapan üç durum ve DLX üzerinden DLQ">
  <defs>
    <marker id="rconf-dl" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir mesajı "dead-letter" yapan ÜÇ durum</text>
  <rect x="24" y="48" width="200" height="34" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="124" y="70" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">reject/nack + requeue=false</text>
  <rect x="24" y="90" width="200" height="34" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="124" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">mesaj/kuyruk TTL'i dolar</text>
  <rect x="24" y="132" width="200" height="34" rx="7" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="124" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">x-max-length aşılır</text>
  <rect x="300" y="90" width="110" height="44" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="355" y="112" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">DLX</text>
  <text x="355" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yönlendirir</text>
  <rect x="480" y="90" width="150" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="555" y="112" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">DLQ</text>
  <text x="555" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">izole edilir</text>
  <line x1="224" y1="65" x2="298" y2="104" stroke="#8A2E2E" stroke-width="1.8" marker-end="url(#rconf-dl)"/>
  <line x1="224" y1="107" x2="298" y2="112" stroke="#8A2E2E" stroke-width="1.8" marker-end="url(#rconf-dl)"/>
  <line x1="224" y1="149" x2="298" y2="120" stroke="#8A2E2E" stroke-width="1.8" marker-end="url(#rconf-dl)"/>
  <line x1="410" y1="112" x2="476" y2="112" stroke="#6B3FA0" stroke-width="2" marker-end="url(#rconf-dl)"/>
  <text x="330" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">TETİKLEMEZ: başarılı ack (normal siler) ve requeue=true (asıl kuyruğa geri koyar).</text>
  <text x="330" y="216" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">mandatory ise farklı aşamadır: hiç kuyruğa GİREMEYEN mesajı üreticiye döndürür.</text>
</svg>
```

Son incelik şu: confirms etkinken mandatory ile yayımlanan bir mesaj hiçbir kuyruğa giremeyebilir. Bu durumda üretici **hem** bir `basic.return` **hem** bir confirm onayı (ack) görür. Çünkü confirms "broker mesajı işledi mi?" sorusunu, mandatory ise "bir kuyruğa girdi mi?" sorusunu yanıtlar. İkisi birlikte kullanılabilir ve genellikle birlikte kullanılır.

### Mülakatta ne sorulur?

- "Mesajın broker'a ulaştığını nasıl doğrularsın?" — publisher confirms; kuyruk sayısını sorgulamak (polling) yanlış ve yavaştır, autoack ise tüketici tarafı bir ayardır.
- "Confirms mi transaction mı, neden?" — asenkron olduğu için confirms.
- "DLX'i ne tetikler, ne tetiklemez?" — üç tetikleyici; ack ve `requeue=true` tetiklemez.
- "Eklentisiz gecikmeli retry?" — TTL'li bekleme kuyruğu + DLX.
- "durable kuyruk yeter mi?" — hayır; mesaj da persistent olmalı. (**Çoğunluk kuyrukları (quorum queue)** zaten daima kalıcıdır.)
- "confirms + mandatory birlikte ne gösterir?" — return **ve** ack.

### Tek cümlede

Confirms mesajın broker'a ulaştığını, mandatory bir kuyruğa girdiğini, DLX ise kuyrukta öldüğünü söyler; TTL ile birleşen DLX ise beklemeyi yeniden denemeye çevirir.

# rconf-1 | Temel
## Soru
Bir üretici, yayımladığı mesajın **broker tarafından güvenle alındığını** yüksek verimi bozmadan doğrulamak istiyor. En uygun mekanizma nedir?
## A
Her mesajdan sonra kuyruğu sorgulayıp mesaj sayısının arttığını kontrol etmek
🤖 Polling hem yanlış (sayı tüketimle değişir) hem çok yavaştır; yerleşik mekanizma vardır.
## B
autoack'i (otomatik onay) açıp broker'ın her mesajı hemen onaylamasını sağlamak
🤖 autoack tüketici tarafı bir ayardır; üreticinin mesajının broker'a ulaşıp ulaşmadığını söylemez.
## C
Hiçbir şey yapmamak; standart publish zaten mesajın alındığını garanti eder
🤖 Standart publish ateşle-unut'tur; başarı garantisi vermez, sessizce kaybolabilir.
## D
Publisher confirms (yayıncı onayları) etkinleştirmek; broker asenkron bir onay döner
🤖 Doğru: confirms tam da bu amaç içindir ve asenkron olduğu için verimi korur.
## Doğru
D
## Açıklama
**Publisher confirms**, üreticinin "mesajım broker'a güvenle ulaştı mı?" sorusunu yanıtlayan mekanizmadır: kanal confirm moduna alınır, broker her mesajı işleyip (kalıcıysa diske yazıp, kuyruklara yönlendirip) üreticiye **asenkron bir ack** (veya işleyemezse nack) döner. Asenkron olduğu için üretici yayımlamayı durdurmadan devam eder, onaylar arka planda toplanır — bu yüzden verim yüksektir. Standart `basic.publish` ateşle-unut olduğundan garanti vermez (C); autoack (B) tüketici tarafını ilgilendirir; kuyruk sorgulama (A) hem yanlış hem yavaştır.

# rconf-2 | Orta
## Soru
Publisher **confirms** ile AMQP **transaction**'ları (`tx.select`/`tx.commit`) aynı sorunu çözer. Confirms neden genelde tercih edilir?
## A
Transaction mesaj kaybını hiç önlemez; teslim güvencesini yalnızca confirms verir
🤖 İkisi de teslim güvencesi verir; fark güvenlik değil, performans/çalışma modelidir.
## B
Confirms asenkron çalışır; transaction her commit'te senkron round-trip getirip verimi düşürür
🤖 Doğru: confirms'in asenkron doğası onu transaction'a göre çok daha yüksek verimli kılar.
## C
Transaction yalnızca tek bir mesajı kapsar; confirms ise sınırsız sayıda mesajı destekler
🤖 Transaction da birden çok mesajı kapsar; fark mesaj sayısı değil, senkron/asenkron çalışmadır.
## D
Confirms mesaj gövdesini şifreleyerek gönderir; transaction ise şifreleme sunmaz
🤖 İkisinin de şifrelemeyle ilgisi yoktur; fark performans modelidir.
## Doğru
B
## Açıklama
AMQP **transaction**'ları teslim güvencesi verir ama pahalıdır: her `tx.commit` **senkron** bir round-trip gerektirir ve broker tarafında ek maliyet getirir; üretici her commit için beklemek zorunda kalır, bu da verimi ciddi biçimde düşürür (ölçümlerde kat kat yavaş). **Publisher confirms** aynı güvenceyi **asenkron** verir: üretici yayımlamaya devam eder, broker onayları (ack/nack) arka planda akar ve üretici bunları toplu izler. Böylece hem "mesaj broker'a ulaştı" garantisi korunur hem yüksek verim sağlanır. Fark güvenlik (A), mesaj sayısı (C) veya şifreleme (D) değil, **senkron ve asenkron performans modelidir**.

# rconf-3 | Orta
## Soru
Bir kuyruğa `x-dead-letter-exchange` tanımlı. Aşağıdakilerden hangisi bir mesajın bu **DLX**'e yönlendirilmesine yol açar?
## A
Mesajın başarıyla tüketilip `basic.ack` ile onaylanması
🤖 Başarılı ack mesajı normal siler; dead-letter tetiklemez.
## B
Tüketicinin mesajı `basic.nack` ile `requeue=true` vererek reddetmesi
🤖 requeue=true mesajı asıl kuyruğa geri koyar; DLX'e göndermez.
## C
Mesajın `requeue=false` ile reddedilmesi, TTL'inin dolması veya uzunluk sınırının aşılması
🤖 Doğru: bu üç durum (requeue=false red, TTL dolması, uzunluk aşımı) mesajı dead-letter yapar.
## D
Üreticinin mesajı `mandatory=true` bayrağıyla yayımlayıp `basic.return` beklemesi
🤖 mandatory üretici tarafı bir bayraktır; yönlendirilemeyen mesajı üreticiye döndürür, DLX'i tetiklemez.
## Doğru
C
## Açıklama
Bir mesaj **dead-letter** olur (yani kuyruğun `x-dead-letter-exchange`'ine yönlendirilir) üç durumda: (1) `basic.reject`/`basic.nack` ile **`requeue=false`** reddedilirse, (2) mesajın/kuyruğun **TTL**'i dolarsa, (3) kuyruk **uzunluk/bellek sınırını** (`x-max-length`) aşarsa. Başarılı ack (A) mesajı normal siler; `requeue=true` red (B) mesajı asıl kuyruğa geri koyar — ikisi de DLX tetiklemez. `mandatory` (D) tamamen farklı bir aşamadır (üretici → hiç kuyruğa giremeyen mesaj), DLX ile ilgisizdir. DLX + bir DLQ, hatalı mesajları izole edip incelemek için standart desendir.

# rconf-4 | İleri
## Soru
Başarısız bir mesajı **hemen değil, 30 saniye sonra** yeniden denemek (gecikmeli retry / delay) istiyorsunuz, üstelik bir eklenti kurmadan. Klasik RabbitMQ deseni nedir?
## A
Mesajı, TTL=30s tanımlı ve DLX'i asıl işleme kuyruğuna bağlı bir bekleme kuyruğuna koymak
🤖 Doğru: TTL + DLX birleşimi eklentisiz klasik delay/retry desenidir.
## B
Tüketicide `Thread.sleep(30000)` çağırıp süre dolduktan sonra mesajı işlemeye devam etmek
🤖 Tüketiciyi 30s bloke etmek prefetch'i tıkar, ölçeklenmez ve kaynak israfıdır; yanlış çözümdür.
## C
Mesajı `requeue=true` ile hemen geri koymak; RabbitMQ mesajı 30 saniye otomatik bekletir
🤖 requeue=true mesajı ANINDA geri koyar; yerleşik bir bekleme süresi yoktur, döngü hızlanır.
## D
Kuyruğa `x-max-priority` verip başarısız mesajların en son sırada işlenmesini sağlamak
🤖 Öncelik kuyruğu sıralamayı değiştirir; gecikmeli yeniden deneme sağlamaz.
## Doğru
A
## Açıklama
Eklentisiz klasik **gecikmeli retry (delay queue)** deseni **TTL + DLX** birleşimidir: başarısız mesaj, TTL'i 30 saniye olan bir "bekleme/retry" kuyruğuna gönderilir; bu kuyruğun tüketicisi yoktur, sadece bekletir. TTL dolduğunda mesaj **dead-letter** olur ve kuyruğun **DLX**'i onu **asıl işleme kuyruğuna** geri yönlendirir — yani 30s sonra yeniden denenir. `Thread.sleep` (B) tüketiciyi bloke edip prefetch slotunu tıkar; `requeue=true` (C) mesajı anında geri koyup sıkı döngü yaratır; öncelik kuyruğu (D) gecikmeyle ilgisizdir. (Alternatif: `rabbitmq-delayed-message-exchange` eklentisi, ama soru "eklentisiz" istiyordu.)

# rconf-5 | İleri
## Soru
Bir kuyruk `durable` olarak tanımlı ama mesajlar **persistent olmayan** (geçici / transient) delivery mode ile yayımlanıyor. Broker yeniden başlarsa ne olur?
## A
Mesajlar hayatta kalır; durable kuyruk içindeki mesajları da otomatik olarak kalıcı yapar
🤖 Kuyruğun durable olması mesajı kalıcı yapmaz; mesajın kendisi de persistent olmalıdır.
## B
Mesajlar hayatta kalır çünkü publisher confirms alınmış ve diske yazma doğrulanmıştır
🤖 Confirms "broker aldı" der; ama persistent olmayan mesaj diske yazılmadığından restart'ta yine kaybolur.
## C
Broker açılışta eksik mesajları üreticiden isteyip otomatik olarak yeniden üretir
🤖 Broker kaybolan mesajı yeniden üretemez; kaynağı üreticidir.
## D
Mesajlar kaybolur; kalıcılık hem durable kuyruk hem persistent mesaj gerektirir
🤖 Doğru: iki koşul birden şarttır; persistent olmayan mesaj diske yazılmadığı için restart'ta gider.
## Doğru
D
## Açıklama
Kalıcılık **iki bağımsız koşulun birlikteliğidir**: kuyruk **durable** olmalı **ve** mesaj **persistent** (delivery mode = 2) yayımlanmalı. Kuyruğun durable olması yalnızca **kuyruk tanımının** restart'ta yeniden yaratılmasını garanti eder; içindeki persistent olmayan mesajlar diske hiç yazılmadığından broker yeniden başlayınca **kaybolur** (D). Durable kuyruk tek başına mesajı kalıcı yapmaz (A); publisher confirms (B) mesajın broker'a ulaştığını doğrular ama persistent olmayan mesaj için "diske yazıldı" güvencesi vermez; broker kaybolanı yeniden üretemez (C). Sık yapılan hata tam da bu ikisinden birini atlamaktır. (Not: quorum kuyruklar zaten daima kalıcıdır; bu ayrım özellikle klasik kuyruklarda kritiktir.)

# rconf-6 | İleri
## Soru
Publisher confirms **etkin** ve mesaj `mandatory=true` ile yayımlandı. Broker mesajı aldı ama o exchange'e bağlı **hiçbir uygun kuyruk yok**. Üretici ne gözlemler?
## A
Yalnızca bir `nack` (negatif onay) gelir; mesaj broker'a hiç ulaşmamış sayılır
🤖 Mesaj broker'a ulaştı, bu yüzden confirm ack gelir; ayrıca yönlendirilememe nack ile değil return ile bildirilir.
## B
Önce yönlendirilemediğini bildiren bir `basic.return`, ardından bir confirm ack gelir
🤖 Doğru: iki mekanizma farklı soruları yanıtlar; return yönlendirmeyi, confirm broker teslimini bildirir.
## C
Hiçbir bildirim gelmez; mandatory ile confirms aynı kanalda birlikte kullanılamaz
🤖 İkisi birlikte kullanılır ve birbirini tamamlar; bu yaygın bir kombinasyondur.
## D
Sadece bir `basic.return` gelir; mesaj kuyruğa girmediği için confirm ack gönderilmez
🤖 Confirm ack yönlendirmeye değil broker'ın mesajı işlemesine bağlıdır; kuyruğa girmese de ack gelir.
## Doğru
B
## Açıklama
Bu, iki mekanizmanın **farklı soruları** yanıtladığını gösteren klasik senaryodur. **mandatory=true** "mesaj en az bir kuyruğa **girdi mi**?" sorusunu izler: girmediyse üreticiye `basic.return` ile mesaj geri döner. **Publisher confirms** ise "broker mesajı **güvenle işledi mi** (aldı ve yönlendirme kararını verdi)?" sorusunu izler ve mesaj kuyruğa girmese bile **confirm ack** döner (broker mesajla işini bitirmiştir). Dolayısıyla üretici **hem** bir `basic.return` **hem** bir confirm ack görür. Sağlam bir üretici bunu şöyle yorumlar: "broker aldı ama hiçbir kuyruğa koyamadı" — çoğu zaman bir yapılandırma/binding hatasının işaretidir. İkisi birlikte kullanılır (C yanlış) ve ack gelmemesi (A, D) yanlış beklentidir.
