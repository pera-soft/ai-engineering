---
slug: rabbitmq-amqp-model
baslik: "RabbitMQ AMQP Modeli: Exchange, Binding, Queue ve Broker-Merkezli Akış"
kademe: Temel
sira: 119
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş sitesi yazdığını düşün. Müşteri "Satın al" düğmesine bastığında üç iş yapılmalı: fatura kesilecek, kargo hazırlanacak, müşteriye e-posta gidecek. Sipariş programı bu üç işi kendisi, sırayla yaparsa; kargo servisi o an bozuksa müşteri ekranın karşısında bekler ve sipariş de tamamlanmaz.

Daha iyi bir yol var: sipariş programı küçük bir kâğıda "şu sipariş oluştu" yazar, kâğıdı bir **posta kutusuna** bırakır ve kendi işine devam eder. Fatura, kargo ve e-posta programları o kutuya bakar, kâğıdı alır, işini yapar. Programlar birbirini beklemez, hatta birbirinin adresini bile bilmez. Buna **eşzamansız mesajlaşma (asenkron messaging)** denir.

RabbitMQ bu sistemin **posta müdürüdür**. Kutuları o tutar, kâğıtları o dağıtır, kimin neyi aldığını o izler.

### Temel kavramlar

Hiçbirini bildiğini varsaymıyorum; hepsini sıfırdan tanımlayalım:

- **Mesaj (message)**: bir programdan diğerine giden küçük veri paketi — yani o "kâğıt". İçinde gövde (ör. sipariş numarası) ve üstünde etiketler/başlıklar vardır.
- **Aracı, yani broker**: mesajları kabul eden, saklayan ve dağıtan sunucu programı. RabbitMQ bir broker'dır.
- **Kuyruk (queue)**: mesajların sırasını bekleyerek durduğu kutu. Broker'ın içindedir; ilk giren ilk çıkar.
- **Üretici (producer)**: mesajı yazıp gönderen program.
- **Tüketici (consumer)**: kuyruğu dinleyen, mesajı alıp işi yapan program.
- **Mesaj dağıtıcısı (exchange)**: postanenin dağıtım masası. Her mesaj önce buraya gelir, kuyruğa buradan girer.
- **Yönlendirme anahtarı (routing key)**: mesajın üstündeki adres etiketi (ör. `siparis.olustu`).
- **Bağ (binding)**: "şu masaya gelen, şu etiketli mektuplar şu kutuya gitsin" kuralı. Kuyruğu exchange'e bağlar.
- **Onay (ack, acknowledgement)**: tüketicinin "bu mesajı gerçekten bitirdim" demesi. Onay gelince mesaj kuyruktan silinir.
- **AMQP 0-9-1**: bütün bu kuralların yazılı olduğu, RabbitMQ'nun konuştuğu protokol.

### Nasıl çalışır?

En kritik kural şudur: **üretici mesajı asla doğrudan kuyruğa koyamaz.** Her `publish` (yayım) bir **mesaj dağıtıcısına (exchange)** gider. Dağıtıcı, kendisine tanımlı **bağlara (binding)** ve mesajın **yönlendirme anahtarına (routing key)** bakar; mesajı bir ya da birden çok kuyruğa iletir. Kuyruk mesajı tüketilene kadar tutar. Tüketici işi bitirip **onay (ack)** verince mesaj kuyruktan **silinir**.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üretici exchange üzerinden kuyruğa, oradan tüketiciye">
  <defs>
    <marker id="ramq-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">AMQP akışı: üretici mesajı kuyruğa DOĞRUDAN koyamaz</text>
  <rect x="20" y="70" width="120" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Üretici</text>
  <text x="80" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(producer)</text>
  <rect x="200" y="70" width="120" height="60" rx="10" fill="#FBE9E7" stroke="#C0562B" stroke-width="1.5"/>
  <text x="260" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C0562B" font-family="system-ui">Exchange</text>
  <text x="260" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yönlendirir</text>
  <rect x="380" y="70" width="120" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="440" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Kuyruk</text>
  <text x="440" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(queue) tutar</text>
  <rect x="560" y="70" width="90" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="605" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Tüketici</text>
  <text x="605" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ack → siler</text>
  <line x1="140" y1="100" x2="196" y2="100" stroke="#186399" stroke-width="2" marker-end="url(#ramq-ok)"/>
  <line x1="320" y1="100" x2="376" y2="100" stroke="#186399" stroke-width="2" marker-end="url(#ramq-ok)"/>
  <line x1="500" y1="100" x2="556" y2="100" stroke="#186399" stroke-width="2" marker-end="url(#ramq-ok)"/>
  <text x="350" y="60" text-anchor="middle" font-size="10.5" fill="#C0562B" font-family="system-ui">binding + routing key</text>
  <text x="330" y="170" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Bir TCP bağlantısı (connection) içinde birçok hafif KANAL (channel) çoğullanır.</text>
</svg>
```

Kodda `channel.basicPublish("", "siparisKuyrugu", ...)` gibi bir satır "doğrudan kuyruğa gönderiyorum" izlenimi verir; aslında bu, **varsayılan (adsız) exchange**'tir ve her kuyruk otomatik olarak kendi adıyla ona bağlıdır. Yani kural bozulmaz.

Ya hiçbir bağ eşleşmezse? Mesaj **yönlendirilemez** ve **sessizce düşürülür**. Standart `publish` "ateşle-unut" biçiminde çalıştığı için üretici hiçbir hata almaz.

Bunu görünür kılmanın iki yolu vardır. Birincisi, mesajı **zorunlu yönlendirme bayrağıyla (mandatory)** yayımlamaktır; o zaman mesaj `basic.return` ile üreticiye geri döner. İkincisi, dağıtıcıya bir **yedek dağıtıcı (alternate exchange)** tanımlamaktır.

Bunu **ölü mektup dağıtıcısıyla (DLX, dead-letter exchange)** karıştırma. DLX yalnızca bir kuyruğa **girmiş**, sonra reddedilen ya da süresi dolan mesajlar içindir.

Altyapı tarafında iki kavram var. Birincisi: bir **TCP bağlantısı (connection)** kurmak pahalıdır — el sıkışma, TLS ve soket maliyeti çıkar. Bu yüzden AMQP, tek bir bağlantının içinde birçok hafif **kanal (channel)** açar. Yayım, tüketim ve onay komutları bu kanallar üzerinden akar.

Buradaki tuzak şudur: **bir kanal eşzamanlı erişime güvenli değildir (thread-safe değildir)**. Bu yüzden her **iş parçacığı (thread)** kendi kanalını açar.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek pahalı TCP bağlantısı içinde birçok hafif kanal çoğullanır">
  <defs>
    <marker id="ramq-ch" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bağlantı pahalı, kanal (channel) hafif: bir bağlantı çoğullar</text>
  <rect x="26" y="46" width="360" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="206" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 TCP bağlantısı (connection)</text>
  <text x="206" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">pahalı: el sıkışma + TLS + soket</text>
  <rect x="46" y="98" width="320" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="206" y="115" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kanal 1 — thread A: publish</text>
  <rect x="46" y="130" width="320" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="206" y="147" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kanal 2 — thread B: consume + ack</text>
  <rect x="46" y="162" width="320" height="26" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="206" y="179" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kanal thread-safe DEĞİL: 1 thread = 1 kanal</text>
  <rect x="470" y="86" width="160" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="550" y="116" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Broker</text>
  <text x="550" y="134" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">AMQP komutları</text>
  <line x1="386" y1="121" x2="466" y2="121" stroke="#186399" stroke-width="2" marker-end="url(#ramq-ch)"/>
  <text x="330" y="213" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Az sayıda uzun ömürlü bağlantı + thread başına bir kanal = doğru desen.</text>
</svg>
```

Son kavram **sanal sunucudur (vhost, virtual host)**: tek bir broker içinde ayrı bir ad alanı. Exchange, kuyruk, bağ ve **izinler** vhost bazında ayrılır. Bu sayede iki farklı vhost'ta aynı adlı kuyruk çakışmaz. Sadece isim öneki koymak (`musteriA.siparis`) bunu vermez, çünkü izin sınırı getirmez.

```svg
<svg viewBox="0 0 660 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek broker içinde vhost'lar exchange kuyruk ve izinleri izole eder">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sanal host (vhost): tek broker içinde mantıksal izolasyon</text>
  <rect x="24" y="40" width="612" height="146" rx="12" fill="none" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="6 4"/>
  <text x="40" y="58" font-size="10.5" fill="#5D7789" font-family="system-ui">tek RabbitMQ broker / küme</text>
  <rect x="44" y="68" width="182" height="106" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="135" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">/musteriA</text>
  <text x="135" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">exchange · kuyruk</text>
  <text x="135" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">binding · kendi izinleri</text>
  <text x="135" y="156" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"siparis" kuyruğu</text>
  <rect x="238" y="68" width="182" height="106" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="329" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">/musteriB</text>
  <text x="329" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">exchange · kuyruk</text>
  <text x="329" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">binding · kendi izinleri</text>
  <text x="329" y="156" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"siparis" kuyruğu</text>
  <rect x="432" y="68" width="182" height="106" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="523" y="92" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">/prod</text>
  <text x="523" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">exchange · kuyruk</text>
  <text x="523" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">binding · kendi izinleri</text>
  <text x="523" y="156" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ortam ayrımı</text>
  <text x="330" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Aynı adlı "siparis" kuyruğu vhost'lar arası ÇAKIŞMAZ; izin vhost bazındadır. Adlandırma öneki bunu sağlamaz.</text>
</svg>
```

### Mülakatta ne sorulur?

- "Publish edilen mesaj ilk nereye gider?" — **exchange'e**, asla doğrudan kuyruğa.
- "Ack verilince ne olur?" — mesaj **silinir**. Kafka'da ise mesaj log'da kalır, tüketici kendi **offset**'ini taşır ve tekrar okuyabilir (replay). RabbitMQ'da bunu **Streams** kuyruğu sağlar.
- "Connection mi channel mı?" — az sayıda uzun ömürlü bağlantı, thread başına bir kanal.
- "Yönlendirilemeyen mesaj ne olur?" — sessizce düşer; mandatory/return veya alternate exchange ile yakalanır.
- "Çok kiracılı izolasyon?" — **vhost**.
- "Smart broker / dumb consumer ne demek?" — bu bir hız veya iş mantığı iddiası değildir. İş (business) mantığı her iki sistemde de tüketici uygulamasındadır; ayrım **yönlendirme ve teslim durumu takibinin** nerede olduğudur. Ayrıca "RabbitMQ diske yazmaz" da yanlıştır: kalıcı (persistent) mesajlar diske yazılır.

### Tek cümlede

RabbitMQ'da mesaj daima **üretici → dağıtıcı (exchange) → bağ (binding) → kuyruk → tüketici** yolunu izler. Yönlendirme kararını aracı sunucu verir; onay (ack) alınan mesaj ise kuyruktan silinir.

# ramq-1 | Temel
## Soru
Bir üretici (producer) AMQP'de bir mesaj yayımladığında (publish) mesaj ilk olarak nereye gider?
## A
Doğrudan hedef kuyruğa (queue); exchange yalnızca kuyruk dolunca devreye girer
🤖 Üretici kuyruğun adını bilse bile mesajı doğrudan kuyruğa koyamaz; her publish bir exchange'e gider.
## B
Doğrudan tüketiciye (consumer); broker yalnızca aradaki bağlantıyı kurmakla yetinir
🤖 RabbitMQ broker-merkezlidir; mesaj tüketiciye push edilmeden önce mutlaka bir kuyrukta tutulur.
## C
Bir exchange'e; exchange binding kurallarına göre onu kuyruk(lar)a yönlendirir
🤖 Doğru: üretici daima bir exchange'e yayımlar, yönlendirmeyi exchange yapar.
## D
Önce diske, sonra broker'daki kuyruklardan rastgele seçilen birine; yönlendirme kuralı yoktur
🤖 Yönlendirme rastgele değildir; binding ve routing key ile belirlenir, ayrıca kalıcılık ayrı bir konudur.
## Doğru
C
## Açıklama
AMQP modelinde üretici ile kuyruk arasında **her zaman bir exchange** vardır. Üretici mesajı bir exchange'e ve bir routing key ile yayımlar; exchange, kendisine tanımlı binding'lere bakarak mesajı hangi kuyruğa/kuyruklara ileteceğine karar verir. "Doğrudan kuyruğa gönderme" yanılgısı (A) yaygındır — pratikte varsayılan exchange (default/nameless exchange) kuyruk adını routing key olarak kullanıp bu izlenimi verir, ama mesaj yine bir exchange üzerinden geçer. Mesaj tüketiciye ancak bir kuyrukta tutulduktan sonra ulaşır (B yanlış); yönlendirme rastgele değildir (D yanlış).

# ramq-2 | Temel
## Soru
RabbitMQ'da bir mesaj bir tüketici tarafından başarıyla işlenip **ack**'lendiğinde ne olur? (Kafka ile farkı vurgulayın.)
## A
Mesaj kuyruktan silinir; klasik/quorum kuyrukta tüketilen mesajı tekrar okumanın yerleşik bir yolu yoktur
🤖 Doğru: RabbitMQ broker-merkezlidir, ack sonrası mesaj kuyruktan kaldırılır; replay yerleşik değildir.
## B
Mesaj kuyrukta kalır ve offset ilerler; başka bir tüketici aynı mesajı baştan okuyabilir
🤖 Bu Kafka'nın log/offset modelidir; klasik/quorum RabbitMQ kuyruğunda ack'lenen mesaj tutulmaz.
## C
Mesaj otomatik olarak bir ölü mektup kuyruğuna (dead-letter queue) taşınıp orada saklanır
🤖 Ölü mektup kuyruğu yalnızca reddedilen/süresi dolan mesajlar için devreye girer, başarılı ack için değil.
## D
Hiçbir şey olmaz; mesajı kaldırmak için ayrıca bir silme komutu göndermek gerekir
🤖 Ack'in kendisi mesajı kaldırır; ayrı bir silme komutu yoktur.
## Doğru
A
## Açıklama
RabbitMQ'nun temel farkı budur: klasik ve quorum kuyruklarda mesaj **tüketilene kadar tutulur**, tüketici **ack** verince kuyruktan **kalıcı olarak silinir**. Yerleşik bir "geçmişi tekrar oku" (replay) yeteneği yoktur. Kafka ise bir **commit log**'tur: mesaj retention süresi boyunca kalır, tüketiciler kendi offset'lerini taşır ve aynı veriyi baştan okuyabilir (B, Kafka modeli). Ölü mektup kuyruğu (C) yalnızca reddedilen veya TTL'i dolan mesajlar içindir. Ack'in kendisi silme işlemidir (D yanlış). Not: RabbitMQ **Streams** özelliği bu boşluğu kapatıp log-benzeri replay sunar, ama klasik kuyruk davranışı silmedir.

# ramq-3 | Orta
## Soru
Yüksek verimli bir üretici, tek bir TCP bağlantısı (connection) üzerinden çok sayıda paralel yayım (publish) yapmak istiyor. Doğru yaklaşım nedir?
## A
Her paralel iş parçacığı (thread) için ayrı bir TCP bağlantısı açmak; kanal (channel) kavramı gereksizdir
🤖 Her thread'e ayrı TCP bağlantısı pahalıdır ve önerilmez; AMQP tam da bunu önlemek için kanal sunar.
## B
Tek bir kanalı (channel) tüm iş parçacıkları arasında paylaşmak; kanal zaten thread-safe'tir
🤖 Kanal thread-safe değildir; bir kanalı birden çok thread'in paylaşması protokol bozulmasına yol açar.
## C
Tek bağlantı içinde hiç kanal açmadan, tüm publish çağrılarını bağlantı nesnesi üzerinden yapmak
🤖 AMQP komutları bağlantı üzerinden değil kanal üzerinden gönderilir; kanalsız publish yoktur.
## D
Tek bir TCP bağlantısı içinde her paralel iş için ayrı bir kanal (channel) çoğullamak (multiplex)
🤖 Doğru: kanal, pahalı TCP bağlantısını çoğullayan hafif sanal bağlantıdır; thread başına bir kanal önerilir.
## Doğru
D
## Açıklama
TCP **bağlantısı** (connection) kurmak pahalıdır (el sıkışma, TLS, dosya tanıtıcısı). AMQP bunu **kanal** (channel) ile çözer: tek bir bağlantı içinde birçok hafif, sanal bağlantı çoğullanır ve tüm AMQP komutları (publish, consume, ack) bir kanal üzerinden akar. İyi desen: uygulama başına az sayıda uzun ömürlü bağlantı, **thread başına bir kanal**. Kritik tuzak: bir kanal **thread-safe değildir** — aynı kanalı birden çok thread'in paylaşması (B) yarış koşulu ve protokol bozulması üretir. Thread başına ayrı TCP bağlantısı (A) kaynak israfıdır; kanalsız publish (C) diye bir şey yoktur.

# ramq-4 | Orta
## Soru
Bir üretici mesajı bir exchange'e yayımladı ama o exchange'e bağlı **hiçbir kuyruk yok** (veya routing key hiçbir binding ile eşleşmiyor). Varsayılan davranış nedir?
## A
Broker mesajı, uygun bir kuyruk oluşturulana kadar bellekte bekletir ve sonra teslim eder
🤖 Yönlendirilemeyen mesaj bekletilmez; eşleşme yoksa varsayılan olarak düşer.
## B
Mesaj yönlendirilemez ve varsayılan olarak sessizce düşürülür (drop)
🤖 Doğru: eşleşme yoksa mesaj sessizce kaybolur; fark etmek için ek mekanizma gerekir.
## C
Üretici anında bir istisna (exception) alır; publish çağrısı hata dönerek başarısız olur
🤖 Standart publish ateşle-unut'tur; yönlendirme başarısızlığı çağrıyı otomatik hataya çevirmez.
## D
Mesaj otomatik olarak kuyruğun ölü mektup exchange'ine (DLX) yönlendirilip orada saklanır
🤖 DLX kuyruğa girmiş sonra reddedilen/süresi dolan mesajlar içindir; hiç kuyruğa girmemiş mesaj için değil.
## Doğru
B
## Açıklama
Bir exchange, gelen mesajı eşleşen bir binding bulamazsa **yönlendiremez** ve mesaj **sessizce düşürülür**. Standart `basic.publish` ateşle-unut (fire-and-forget) olduğu için üretici bunu doğrudan anlamaz (C yanlış) ve broker mesajı ileride bir kuyruk açılır diye saklamaz (A yanlış). Bu sessiz kaybı görünür kılmanın iki yolu vardır: (1) **mandatory** bayrağıyla yayımlamak — yönlendirilemeyen mesaj üreticiye `basic.return` ile geri döner; (2) exchange'e bir **alternate exchange** tanımlamak — eşleşmeyen mesajlar oraya yönlendirilir. DLX (D) farklı bir kavramdır: yalnızca bir kuyruğa **girmiş** ama sonra reddedilen/TTL'i dolan mesajlar için geçerlidir.

# ramq-5 | İleri
## Soru
Çok-kiracılı (multi-tenant) bir platformda her müşterinin exchange, kuyruk ve izinlerini birbirinden **mantıksal olarak izole** etmek istiyorsunuz. AMQP'de bunun yerleşik birimi nedir?
## A
Sanal host (virtual host / vhost) — kaynakları ve izinleri kendi ad alanında ayıran mantıksal birim
🤖 Doğru: vhost, tek broker içinde ad alanı ve izin izolasyonu sağlayan yerleşik birimdir.
## B
Her müşteri için ayrı bir fiziksel RabbitMQ düğümü/kümesi kurmak; başka bir izolasyon yolu yoktur
🤖 Ayrı küme bir seçenektir ama AMQP'nin yerleşik mantıksal izolasyon birimi vhost'tur; fiziksel ayrım zorunlu değil.
## C
Kuyruk adlarına müşteri öneki eklemek (ör. `musteriA.siparis`); bu tam izolasyon sağlar
🤖 Adlandırma kuralı izin/ad alanı izolasyonu vermez; farklı müşteriler yine birbirinin kaynağını görebilir.
## D
Her müşteriye ayrı bir TCP bağlantısı (connection) vermek; bağlantı sınırı izolasyonu sağlar
🤖 Bağlantı bir taşıma katmanı kavramıdır; kaynak/izin izolasyonu sağlamaz.
## Doğru
A
## Açıklama
**Sanal host (vhost)**, tek bir RabbitMQ broker'ı (veya kümesi) içinde tamamen ayrı bir ad alanı yaratır: bir vhost'taki exchange, kuyruk ve binding'ler başka vhost'takilerle çakışmaz ve **izinler vhost bazında** verilir. Bu, çok-kiracılı kurulumlar veya ortam ayrımı (dev/test/prod) için doğru, hafif izolasyon aracıdır — ayrı fiziksel küme (B) kurmadan. Sadece adlandırma öneki (C) izin sınırı getirmez; farklı kiracılar birbirinin kaynaklarına erişebilir. Bağlantı (D) taşıma katmanıdır, izolasyon birimi değildir.

# ramq-6 | İleri
## Soru
"RabbitMQ smart broker / dumb consumer, Kafka dumb broker / smart consumer" ifadesinin pratikteki en doğru anlamı nedir?
## A
RabbitMQ tüketicileri yavaş, Kafka tüketicileri ise hızlı olacak biçimde optimize edilmiştir
🤖 Bu bir hız iddiası değil; sorumluluğun broker'da mı tüketicide mi olduğuyla ilgilidir.
## B
RabbitMQ'da tüm iş (business) mantığı broker içinde çalışır; Kafka'da tüketici uygulamasında çalışır
🤖 İş mantığı her iki sistemde de tüketici uygulamasındadır; ayrım yönlendirme/durum sorumluluğundadır.
## C
Yönlendirme ve teslim durumu sorumluluğu RabbitMQ'da broker'da, Kafka'da ise tüketicidedir
🤖 Doğru: ayrım, karmaşık yönlendirme/durum takibinin broker'da mı yoksa tüketicide mi olduğudur.
## D
RabbitMQ mesajı asla diske yazmaz, yalnızca bellekte tutar; Kafka ise her mesajı diske yazar
🤖 RabbitMQ da kalıcı (durable/persistent) mesajları diske yazar; ayrım depolama değil sorumluluk dağılımıdır.
## Doğru
C
## Açıklama
Bu ifade **sorumluluğun nerede olduğunu** anlatır, hız veya iş mantığı yerini değil. RabbitMQ'da broker "akıllıdır": exchange karmaşık yönlendirme yapar, kuyruk mesajı tüketiciye **push** eder, broker her mesajın **ack** durumunu izler ve teslim edilmeyenleri yeniden dağıtır. Tüketici sadece dinler ("dumb consumer"). Kafka'da broker "aptaldır": sadece bir append-only log tutar; tüketici kendi **offset**'ini yönetir, hangi partition'ı nereden okuyacağına ve replay yapıp yapmayacağına kendisi karar verir ("smart consumer"). İş mantığı her ikisinde de tüketicidedir (B yanlış), bu bir hız iddiası değildir (A yanlış) ve RabbitMQ da persistent mesajları diske yazar (D yanlış). Pratik sonuç: RabbitMQ esnek yönlendirme ve iş kuyruğu senaryolarında; Kafka yüksek verim, sıralı log ve tekrar-okuma senaryolarında güçlüdür.
