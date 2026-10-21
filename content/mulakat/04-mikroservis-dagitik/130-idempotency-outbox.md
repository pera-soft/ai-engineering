---
slug: idempotency-outbox
baslik: "Idempotency ve Transactional Outbox: Retry Güvenliği ve Effectively-Once"
kademe: İleri
sira: 130
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Ağ üzerinden bir istek gönderdin ve yanıt gelmedi. İşte o an cevabı olmayan bir soruyla baş başa kalırsın: istek karşıya hiç ulaşmadı mı, yoksa ulaştı, işlendi ve sadece yanıtı mı kayboldu? Dışarıdan bakınca bu iki durum **aynı** görünür.

Bu belirsizlik yüzünden dağıtık sistemler isteği **yeniden dener (retry)**. Mesaj kuyrukları da onaylanmamış mesajı tekrar teslim eder. Sonuç ortadadır: aynı işlem birden çok kez gelebilir. Buna **en az bir kez teslimat** (at-least-once delivery) denir.

**Idempotency** — Türkçesiyle **tekrar edilse de sonucun değişmemesi** — bu dünyada hayatta kalma tekniğidir. Tanımı şudur: bir işlemi bir kez ya da beş kez uygulamak **aynı** sonucu verir.

Asansör düğmesine beş kez basmak asansörü beş kez çağırmaz; bu işlem idempotenttir. Buna karşılık "hesaba 100 TL yükle" idempotent değildir; beş kez uygulanırsa 500 TL yüklenir. Onu idempotent hâle getirmenin yolu, isteğe bir kimlik takıp "bu kimlikli yükleme zaten yapıldı mı?" diye sormaktır. Amaç şudur: tekrarlar kaçınılmazken sonucun tek seferlik kalması.

### Temel kavramlar

- **Yeniden deneme (retry):** yanıt alınamayan isteği tekrar gönderme.
- **Idempotency key (tekrar koruma anahtarı):** bir isteği benzersiz biçimde kimlikleyen anahtardır. İki türü vardır. **Doğal (natural) anahtar** iş verisinin kendisinden gelir; sipariş numarası buna örnektir. **Yapay (surrogate) anahtar** ise iş verisinde doğal bir benzersizlik yoksa devreye girer; istemci bu iş için rastgele bir kimlik üretir. HTTP'deki `Idempotency-Key` başlığı bunun standart hâlidir ve Stripe gibi ödeme API'lerinde kullanılır.
- **Tekilleştirme (deduplication / dedup):** daha önce işlenmiş bir kimliği tanıyıp mesajı atlama.
- **Çift yazma (dual write):** iki ayrı sisteme, yani veritabanına ve mesaj sunucusuna (broker), ayrı ayrı yazma. Biri başarılı olurken öteki başarısız olabilir.
- **Giden kutusu tablosu (outbox):** yayınlanacak olayların, iş verisiyle **aynı** veritabanında tutulduğu tablo.

### Nasıl çalışır?

**HTTP tarafında** akış şöyle işler. İstemci ilk isteğe ve sonraki tüm yeniden denemelere **aynı** tekrar koruma anahtarını (idempotency key) koyar. Sunucu bu anahtarı, ürettiği sonuçla birlikte saklar. Aynı anahtarla ikinci istek geldiğinde sunucu onu tanır. İşlemi **tekrar yürütmez**; ilk işlemin **kaydedilmiş yanıtını** olduğu gibi geri döner. Böylece istek kaç kez gönderilirse gönderilsin tek bir tahsilat olur.

Akla gelen üç kolay çözüm ise işe yaramaz. Yeniden denemeyi tümden yasaklamak gerçekçi değildir; ağ hatası kaçınılmazdır. Her isteği yeni saymak zaten sorunun ta kendisidir. Şifreleme ise gizlilik sağlar, ama tekrarı önlemez.

**Yayın tarafında** ise ayrı bir sorun vardır. Bir servis çoğu zaman iki iş birden yapmak zorundadır: kendi veritabanını güncellemek ve bir olay yayınlamak. Bunları iki ayrı adımda yaparsa birincisi başarılı, ikincisi başarısız olabilir. O zaman veritabanı güncellenmiş, ama olay hiç yayınlanmamıştır; sistem kalıcı biçimde tutarsız kalır.

Buna **çift yazma (dual write)** problemi denir. Sırayı değiştirmek de kurtarmaz: önce Kafka'ya yazıp sonra veritabanını güncellersen bu kez olay yayınlanmış ama veri yazılmamış olur. Sıra değişikliği sorunu çözmez, yalnız yönünü değiştirir.

**Transactional outbox** (işlem paketine dahil giden kutusu) çözümü şudur: olayı, iş verisiyle **aynı yerel veritabanı işlem paketi (transaction) içinde**, özel bir **giden kutusu tablosuna (outbox)** yazarsın.

Bunun kazancı büyüktür. "İş verisi değişti" ile "yayınlanacak bir olay var" artık ya birlikte kalıcı yazılır ya da hiçbiri yazılmaz. Yani ya hep ya hiç garantisi, tek bir yerel işlem paketinden bedavaya gelir; iki fazlı kalıcı yazmaya (2PC) gerek kalmaz. Olayın gerçekten dışarı çıkması ise ikinci bir bileşenin işidir: ayrı çalışan bir **aktarıcı (message relay)** giden kutusunu okur ve mesaj sunucusuna yayınlar.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Transactional outbox: tek transaction ve message relay">
  <defs>
    <marker id="idem-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Outbox: iş verisi + olay tek transaction'da</text>
  <rect x="30" y="54" width="250" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="155" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Servis DB (tek TX)</text>
  <rect x="52" y="92" width="94" height="64" rx="8" fill="#FFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="99" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">iş verisi</text>
  <text x="99" y="138" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(sipariş)</text>
  <rect x="164" y="92" width="94" height="64" rx="8" fill="#FFF" stroke="#6B4E9E" stroke-width="1.3"/>
  <text x="211" y="120" text-anchor="middle" font-size="11" fill="#6B4E9E" font-family="system-ui">outbox</text>
  <text x="211" y="138" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(olay)</text>
  <rect x="352" y="88" width="130" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="417" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Message Relay</text>
  <text x="417" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">polling / CDC</text>
  <rect x="512" y="88" width="104" height="56" rx="10" fill="#F3EEF8" stroke="#6B4E9E" stroke-width="1.5"/>
  <text x="564" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B4E9E" font-family="system-ui">Broker</text>
  <text x="564" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(Kafka)</text>
  <line x1="280" y1="120" x2="350" y2="116" stroke="#186399" stroke-width="2" marker-end="url(#idem-ar)"/>
  <line x1="482" y1="116" x2="510" y2="116" stroke="#186399" stroke-width="2" marker-end="url(#idem-ar)"/>
  <text x="320" y="200" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">Ayrı yazsaydın: DB commit olur, broker'a yayın patlar → dual write tutarsızlığı</text>
</svg>
```

Aktarıcının iki yaygın uygulaması vardır.

**Yoklamalı yayıncı (polling publisher):** ayrı bir süreç giden kutusu tablosunu belirli aralıklarla sorgular. Henüz yayınlanmamış satırları gönderir ve "gönderildi" diye işaretler. Kurulumu basittir; buna karşılık veritabanına sürekli sorgu yükü biner ve olayın çıkışı yoklama aralığı kadar gecikir.

**Değişiklik yakalama (CDC — Change Data Capture):** Debezium gibi bir araç, veritabanının kendi kayıt defterini izler. Bu defter PostgreSQL'de WAL, MySQL'de binlog adını alır ve veritabanının yaptığı her değişikliği sırayla tutar. Araç, giden kutusuna eklenen satırları neredeyse anında yakalar. Böylece **gecikme düşer** ve **veritabanı yükü azalır**; yüksek hacimli sistemlerde tercih edilen yol budur.

Her iki yolda da aktarıcı aynı olayı iki kez yayınlayabilir. Örneğin satırı gönderip "gönderildi" işaretini yazamadan çökerse, ayağa kalktığında aynı satırı yeniden gönderir. Bu yüzden tüketiciler yine tekrar edilse de sonucu değişmeyen (idempotent) biçimde yazılmalıdır.

Buradan mülakatın klasik ayrımına geliriz. **"Tam olarak bir kez teslimat" (exactly-once delivery)**, yani her mesajın ağ üzerinden tam bir kez ulaşması, genel dağıtık sistemlerde **sağlanamaz**. Sebebi baştaki belirsizliktir: çökme ve zaman aşımı durumlarında elinde ya "en az bir kez" ya da "en fazla bir kez" kalır, ikisinin ortası yoktur.

Doğru hedef **tam olarak bir kez işleme (exactly-once processing)**, diğer adıyla **effectively-once**'tır. Reçetesi şudur: teslimatın tekrarlamasına izin ver, böylece mesaj kaybolmasın; ama işlemeyi idempotent yap, böylece **etki yalnız bir kez** oluşsun. Kafka'nın "exactly-once" diye anılan özellikleri bile aslında bunu yapar; idempotent üretici (producer) ve işlem paketi içinde yazma ile bu davranışı belirli bir kapsamda kurar. Kısacası garantiyi teslimatta değil, **işlemede** ara.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="At-least-once teslimat artı idempotent işleme eşittir effectively-once">
  <text x="320" y="26" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Exactly-once teslimat değil, effectively-once</text>
  <rect x="30" y="60" width="180" height="80" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="120" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">At-least-once</text>
  <text x="120" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">teslimat</text>
  <text x="120" y="128" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">mesaj tekrar gelebilir</text>
  <text x="245" y="106" text-anchor="middle" font-size="22" font-weight="700" fill="#1C3247" font-family="system-ui">+</text>
  <rect x="280" y="60" width="180" height="80" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="370" y="88" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">İdempotent işleme</text>
  <text x="370" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dedup / idempotency key</text>
  <text x="370" y="128" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tekrar etkisiz</text>
  <text x="497" y="106" text-anchor="middle" font-size="20" font-weight="700" fill="#1C3247" font-family="system-ui">=</text>
  <rect x="520" y="60" width="100" height="80" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="570" y="92" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Effectively</text>
  <text x="570" y="110" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">-once</text>
  <text x="570" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">etki bir kez</text>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Garantiyi teslimatta değil, işlemede ara</text>
</svg>
```

**Tüketici (consumer) tarafında** bu ilke şöyle uygulanır. Bir tüketici mesajı işler, ama **onayı (ack)** göndermeden çökerse mesaj kendisine yeniden teslim edilir.

**İdempotent tüketici (idempotent consumer)** deseni buna karşı savunmadır. Her mesajın benzersiz bir kimliği vardır. Tüketici, işlediği kimlikleri bir **tekilleştirme deposunda (dedup store)** tutar; bu depo bir veritabanı tablosu veya Redis olabilir. Yeni mesaj geldiğinde tüketici önce oraya bakar. Kimlik daha önce görülmüşse mesajı **atlar**. Görülmemişse mesajı işler ve kimliği kaydeder. İdeal olan, bu kaydı iş etkisiyle **aynı işlem paketi içinde** yapmaktır; aksi hâlde biri yazılıp öteki yazılmayabilir.

Akla gelen kestirme yol, mesajı işlemeden önce onayı göndermektir. Bu yol tekrarları önler, ama karşılığında mesaj kaybını satın alır: işleme sırasında çökersen mesaj bir daha gelmez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İdempotent consumer: dedup deposuyla görülen mesaj atlanır">
  <defs>
    <marker id="idem-dc-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İdempotent consumer: gördüğünü atla</text>
  <rect x="30" y="80" width="110" height="50" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="85" y="102" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Mesaj gelir</text>
  <text x="85" y="120" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">msg-id taşır</text>
  <path d="M230 105 L280 78 L330 105 L280 132 Z" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="280" y="100" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">id daha önce</text>
  <text x="280" y="114" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">görüldü mü?</text>
  <line x1="140" y1="105" x2="228" y2="105" stroke="#186399" stroke-width="2" marker-end="url(#idem-dc-ar)"/>
  <text x="280" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">dedup deposu</text>
  <text x="280" y="176" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(tablo / Redis)</text>
  <rect x="420" y="46" width="180" height="46" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="510" y="68" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">EVET → atla (dedup)</text>
  <text x="510" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yeniden işleme yok</text>
  <rect x="420" y="118" width="180" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="510" y="140" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">HAYIR → işle</text>
  <text x="510" y="158" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">+ id'yi dedup deposuna yaz</text>
  <text x="510" y="172" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(ideal: iş etkisiyle aynı TX)</text>
  <line x1="330" y1="96" x2="418" y2="72" stroke="#186399" stroke-width="1.8" marker-end="url(#idem-dc-ar)"/>
  <line x1="330" y1="114" x2="418" y2="142" stroke="#186399" stroke-width="1.8" marker-end="url(#idem-dc-ar)"/>
</svg>
```

### Mülakatta ne sorulur?

- Doğal ve yapay idempotency anahtarı arasındaki fark nedir?
- Zaman aşımına uğrayan ödeme isteği nasıl güvenle tekrar denenir?
- İdempotent consumer tekrarlardan nasıl korunur?
- Dual write nedir, outbox onu nasıl çözer?
- Relay için hangi iki yaklaşım var, hangisi neden daha verimli?
- "Exactly-once" iddiasına cevabın ne?

### Tek cümlede

Dağıtık dünyada teslimat tekrarlanır. Tekrar koruma anahtarıyla (idempotency key) yeniden denemeleri, tekilleştirmeyle (dedup) yeniden teslimleri, giden kutusu tablosuyla (outbox) çift yazmayı çözersin. Elde ettiğin şey "tam olarak bir kez teslimat" değildir; ondan daha gerçekçi olan **effectively-once**, yani etkinin bir kez oluşmasıdır.

# idem-1 | Orta
## Soru
Idempotency için **doğal** (natural) anahtar ile **yapay** (surrogate) anahtar arasındaki fark nedir?
## A
İkisi de aynı şeydir; yalnız farklı ekiplerin aynı kavram için kullandığı iki isimdir
🤖 Farklıdırlar: biri iş verisinden gelir, diğeri özellikle üretilmiş bir kimliktir.
## B
Doğal anahtar iş verisinden gelir; yapay anahtar istemcinin ürettiği rastgele kimliktir
🤖 Doğru: doğal = iş verisinden, yapay = özellikle üretilen benzersiz kimlik.
## C
Yapay anahtar her zaman kötüdür; güvenli tek yol her senaryoda doğal anahtar kullanmaktır
🤖 Yapay anahtar, doğal bir benzersizlik olmadığında (ör. "para yükle" isteği) tam da doğru araçtır.
## D
Doğal anahtar yalnız veritabanı birincil anahtarı (primary key) için tanımlı bir kavramdır
🤖 Doğal idempotency anahtarı bir iş kimliğidir (sipariş no gibi); DB primary key kavramıyla sınırlı değildir.
## Doğru
B
## Açıklama
Idempotency'yi uygulamak için isteği benzersiz kimlikleyecek bir anahtara ihtiyaç vardır. **Doğal anahtar** işin kendi verisinden gelir — bir "sipariş oluştur" isteğinde istemcinin ürettiği sipariş numarası gibi; iş zaten benzersiz bir kimlik taşıyorsa onu kullanırsın. **Yapay (surrogate) anahtar** ise doğal bir benzersizlik olmadığında (ör. "hesaba 100 TL yükle" — iki gerçek yükleme birbirinin aynısı görünür) istemcinin özellikle ürettiği rastgele bir kimliktir; tipik olarak HTTP `Idempotency-Key` başlığıyla taşınır ve Stripe gibi API'lerin standardıdır. Sunucu bu anahtarı kaydeder ve tekrar görürse işlemi yinelemez. Yapay anahtar kötü değildir (C); doğal anahtar veritabanı primary key'ine indirgenmez (D).

# idem-2 | İleri
## Soru
Bir ödeme `POST` isteği ağ zaman aşımına uğradı; istemci sonucu bilmiyor ve retry edecek. **Çift tahsilatı** önlemenin doğru yolu nedir?
## A
İstemci hiçbir zaman retry etmemeli; zaman aşımını doğrudan başarısızlık saymalı
🤖 Retry etmemek kaybolan işlemlere yol açar; dağıtık sistemde retry gereklidir, güvenli hale getirilmelidir.
## B
Sunucu her isteği yeni bir işlem saymalı; bu senaryoda çift tahsilat kaçınılmazdır
🤖 Çift tahsilat kaçınılmaz değildir; idempotency key ile önlenebilir, bu yaklaşım hatalıdır.
## C
İstek gövdesi şifrelenmeli; şifreli bir isteğin tekrarı sunucuda ikinci kez işlenmez
🤖 Şifreleme gizlilik sağlar, tekrarı engellemez; aynı şifreli istek iki kez de tahsil edebilir.
## D
İstek bir idempotency key taşımalı; sunucu tanıdığı anahtarda ilk sonucu geri döner
🤖 Doğru: idempotency key ile retry, ikinci kez tahsil etmeden ilk sonucu döndürür.
## Doğru
D
## Açıklama
Zaman aşımı, istemcinin isteğin sunucuya ulaşıp ulaşmadığını **bilemediği** klasik belirsizliktir; güvenli davranış retry etmektir, ama retry çift tahsilat riski taşır. Çözüm **idempotency key**'dir: istemci ilk isteğe ve tüm retry'lere **aynı** anahtarı koyar; sunucu bu anahtarı işlediği işlemle birlikte saklar. Retry geldiğinde sunucu anahtarı tanır, işlemi **tekrar yürütmez** ve ilk işlemin **kaydedilmiş sonucunu** döner. Böylece kaç kez gönderilirse gönderilsin tek bir tahsilat oluşur. Retry'i yasaklamak (A) gerçekçi değildir; her isteği yeni saymak (B) tam da sorunun kaynağıdır; şifreleme (C) tekrarı engellemez.

# idem-3 | İleri
## Soru
Bir mesaj kuyruğu **at-least-once** teslim ettiği için tüketici aynı mesajı iki kez alabilir. **Idempotent consumer** bu tekrara karşı nasıl korunur?
## A
İşlediği mesaj kimliklerini bir dedup deposunda tutar; tekrar geleni yeniden işlemez
🤖 Doğru: işlenen kimlikleri hatırlayıp tekrarı atlamak idempotent consumer'ın temel tekniğidir.
## B
Broker'da exactly-once teslimat açılır; mesaj tam bir kez ulaştığı için tekrar hiç oluşmaz
🤖 Uçtan uca exactly-once teslimat bir efsanedir; tüketici at-least-once varsayıp kendini idempotent yapmalıdır.
## C
Mesajı işlemeden hemen önce ack gönderir; böylece yeniden teslim penceresi tümüyle kapanır
🤖 İşlemeden ack göndermek tekrarı değil, çökme durumunda mesaj kaybını satın alır (at-most-once).
## D
Mesajlar TCP üzerinden gittiği için tüketici düzeyinde tekrar zaten hiç oluşamaz
🤖 TCP paket düzeyinde güvenilirlik verir; uygulama düzeyinde yeniden teslim tekrar üretmeye devam eder.
## Doğru
A
## Açıklama
At-least-once teslimatta bir tüketici, mesajı işleyip ama **ack göndermeden önce** çökerse mesaj yeniden teslim edilir — yani aynı mesaj iki kez işlenebilir. **Idempotent consumer** deseni tüketiciyi buna karşı savunur: her mesajın benzersiz bir kimliği (message id veya iş anahtarı) vardır ve tüketici işlediği kimlikleri bir **dedup deposunda** (tablo, Redis) tutar. Yeni mesaj geldiğinde önce bu depoya bakar; kimlik daha önce görülmüşse mesajı **atlar** (deduplication), yoksa işleyip kimliği kaydeder — ideal olarak iş etkisiyle aynı transaction içinde. Broker exactly-once garanti etmez (B); işlemeden önce ack göndermek (C) mesaj kaybına yol açar; TCP'ye güvenmek (D) uygulama düzeyi tekrarları önlemez.

# idem-4 | İleri
## Soru
Bir servis, iş verisini kendi veritabanına yazmalı **ve ayrıca** Kafka'ya bir olay yayınlamalı. İkisini ayrı adımlarda yapmanın yarattığı **"dual write"** problemi nedir ve **transactional outbox** bunu nasıl çözer?
## A
İki ayrı yazma her koşulda güvenlidir; dual write diye bir sorun gerçekte hiç yoktur
🤖 Dual write gerçek bir tutarsızlık kaynağıdır: biri başarılı, diğeri başarısız olabilir.
## B
Dual write yalnız iki fazlı commit (2PC) ile çözülebilir; pratikte başka bir yol yoktur
🤖 2PC bir seçenek olsa da kırılgan ve pahalıdır; outbox tam da 2PC'siz atomiklik sağlayan çözümdür.
## C
Biri commit olup diğeri patlayabilir; outbox olayı iş verisiyle aynı transaction'a yazar
🤖 Doğru: outbox, olayı iş verisiyle tek transaction'da yazıp atomikliği sağlar; yayını relay üstlenir.
## D
Sıra değiştirilip önce Kafka'ya sonra veritabanına yazılırsa problem tümüyle ortadan kalkar
🤖 Sıra değiştirmek problemi çözmez; ilk yazma başarılı, ikincisi başarısız olursa yine tutarsızlık kalır.
## Doğru
C
## Açıklama
**Dual write** problemi şudur: bir servis (1) veritabanını güncelleyip (2) broker'a olay yayınlamayı **iki ayrı işlemle** yaparsa, birincisi başarılı ikincisi başarısız olabilir (ya da tersi) — veritabanı güncellendi ama olay hiç yayınlanmadı ve sistem kalıcı biçimde tutarsız kaldı. İki farklı sistemi tek atomik işlemde birleştirmek doğrudan mümkün değildir. **Transactional outbox** çözümü: olayı, iş verisiyle **aynı yerel veritabanı transaction'ında** özel bir **outbox tablosuna** yazarsın — böylece "iş verisi değişti" ile "yayınlanacak olay var" ya birlikte commit olur ya birlikte olmaz. Sonra ayrı bir **message relay** outbox'ı okuyup broker'a yayınlar. Dual write'ı yok saymak (A), tek çare olarak 2PC'yi göstermek (B) veya sırayı değiştirmek (D) çözüm değildir.

# idem-5 | Uzman
## Soru
Çoğu broker'da gerçek "exactly-once **teslimat**" pratikte mümkün olmadığına göre, pratikte **"effectively-once"** (etkisi bir kez) nasıl elde edilir?
## A
Broker'da bir yapılandırma ayarı açılarak uçtan uca exactly-once teslimat garantiye alınır
🤖 Uçtan uca exactly-once teslimat pratikte sağlanamaz; broker özellikleri bile sınırlı kapsamlıdır.
## B
TCP kullanmak mesajın alıcıya tam bir kez ulaşmasını protokol düzeyinde garanti eder
🤖 TCP paket düzeyinde güvenilirlik verir; uygulama yeniden teslimi ve çökme senaryoları tekrar üretir.
## C
İstemci retry'i tümüyle kapatılır; tekrar olasılığı böylece kökten sıfırlanmış olur
🤖 Retry'i kapatmak mesaj kaybına yol açar; effectively-once, retry'e rağmen doğru sonuç üretmektir.
## D
At-least-once teslimat ile idempotent işlemenin birleşimi: etki bir kezmiş gibi olur
🤖 Doğru: garantiyi teslimatta değil işlemede ararsın; at-least-once + idempotency = effectively-once.
## Doğru
D
## Açıklama
Senior seviyede kritik kavrayış: "exactly-once **delivery**" (her mesajın ağ üzerinden tam bir kez ulaşması) genel dağıtık sistemlerde **sağlanamaz** — çökme ve zaman aşımı belirsizlikleri her zaman en fazla "en az bir kez" veya "en fazla bir kez" bırakır. Doğru hedef **exactly-once processing**, yani **effectively-once**'tır: mesajın birden çok kez teslim edilmesine izin ver (at-least-once, kayıp olmasın), ama işlemeyi **idempotent** yap (dedup deposu / idempotency key) ki mesaj kaç kez gelirse gelsin **etki bir kez** oluşsun. Kafka'nın "exactly-once" özellikleri bile aslında idempotent producer ve transactional yazma ile bu semantiği belirli bir kapsamda kurar. Broker ayarı (A), TCP (B) veya retry'i kapatmak (C) bu sonucu vermez.

# idem-6 | İleri
## Soru
Transactional outbox deseninde, outbox tablosundaki olayları broker'a taşıyan **message relay** için iki yaygın yaklaşım nedir?
## A
Yalnız iki fazlı commit (2PC); outbox satırları başka bir yöntemle yayınlanamaz
🤖 Outbox zaten 2PC'den kaçınmak için vardır; relay polling veya CDC ile çalışır.
## B
Polling publisher (tabloyu periyodik sorgula) veya CDC (commit log'unu izle)
🤖 Doğru: iki yaygın relay yaklaşımı polling ve CDC'dir; CDC log tabanlı ve daha verimlidir.
## C
Olayı istemcinin doğrudan broker'a göndermesi; sunucu tarafında relay'e gerek kalmaz
🤖 Outbox'ın amacı sunucu tarafında atomik yayındır; istemciye bırakmak dual write'ı geri getirir.
## D
Broker'ın outbox tablosuna doğrudan bağlanıp satırları kendisinin okuyup silmesi
🤖 Broker'lar uygulama veritabanı tablolarını okumaz; bu işi ayrı bir relay süreci üstlenir.
## Doğru
B
## Açıklama
Outbox tablosuna yazılan olayları broker'a **taşıyan** bileşene **message relay** denir ve iki yaygın uygulaması vardır. (1) **Polling publisher** — ayrı bir süreç outbox tablosunu periyodik olarak sorgular, yayınlanmamış satırları broker'a gönderir ve işaretler/siler; basittir ama sorgu yükü ve gecikme getirir. (2) **CDC (Change Data Capture)** — Debezium gibi bir araç veritabanının **commit/transaction log'unu** (ör. PostgreSQL WAL, MySQL binlog) izleyip outbox'a eklenen satırları neredeyse gerçek zamanlı yakalar ve yayınlar; **daha düşük gecikme** ve **daha az veritabanı yükü** sağlar, bu yüzden yüksek hacimde tercih edilir. Her iki yaklaşımda da relay at-least-once yayınlayabileceği için tüketiciler yine idempotent olmalıdır. 2PC (A), istemcinin doğrudan yayını (C) ve broker'ın tabloyu kendisinin okuması (D) geçerli relay yöntemleri değildir.
