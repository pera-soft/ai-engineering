---
slug: soap-jms-legacy-entegrasyon
baslik: "Eski Sistemlerle Entegrasyon: SOAP/WSDL, JMS/MQ, Dağıtık İşlem ve Sarmalama"
kademe: İleri
sira: 221
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir bankanın ana bankacılık (core banking) sistemi, şehrin altındaki kanalizasyon ve su şebekesi gibidir: kırk yıl önce döşenmiştir, hâlâ çalışır, kimse tek gecede sökemez. Üstüne yeni binalar dikilir, yeni mahalleler kurulur — ama hepsi eninde sonunda aynı ana borulara bağlanır. Yeni yazdığın mikroservis de öyledir: müşteri bilgisini, hesap bakiyesini, limit düşümünü sonunda o eski sistemden alır ya da ona yazdırır. Bu yüzden "modern mimari" bilmek yetmez; **eski sistemin konuştuğu dili** — SOAP zarfını, MQ kuyruğunu, gün sonu dosyasını — bilmek gerekir. Bu bölüm, yeni ile eskiyi birbirine bağlarken karşına çıkacak protokolleri, desenleri ve tuzakları anlatır.

### Temel kavramlar

- **SOAP:** XML tabanlı bir mesajlaşma protokolü. Mesaj bir **zarfa (Envelope)** konur; zarfın **başlığı (Header)** teknik bilgiyi (güvenlik, ilişkilendirme), **gövdesi (Body)** iş verisini taşır. Hata dönerken gövdeye `SOAP Fault` konur.
- **WSDL / XSD:** servisin makine-okunur sözleşmesi. **WSDL** hangi işlemlerin bulunduğunu ve nereden çağrılacağını, **XSD** mesajın alan alan yapısını ve tiplerini tanımlar. **Sözleşme-önce (contract-first)** çalışmak, önce XSD/WSDL'i yazıp koddaki sınıfları ondan **üretmek** demektir.
- **WS-Security:** SOAP başlığında taşınan güvenlik katmanı; mesajın kendisini imzalar veya şifreler ve kimlik bilgisini (kullanıcı adı/parola belirteci, X.509 sertifika) taşır. Taşıma katmanı güvenliğinden (TLS) farkı, korumanın **mesaja bağlı** olmasıdır: araya bir aracı girse bile mesajın imzası bozulmaz.
- **REST ile gerçek fark:** SOAP'ta sözleşme **zorunlu ve katıdır**, mesaj XSD'ye göre doğrulanır ve hata modeli tekdüzedir (`SOAP Fault`). REST'te sözleşme (OpenAPI) isteğe bağlıdır, hata HTTP durum koduyla taşınır. SOAP taşıma katmanından bağımsızdır — HTTP dışında JMS üzerinden de taşınabilir; REST doğrudan HTTP'nin üstüne kuruludur. "SOAP daha hızlıdır" ya da "SOAP durum bilgisi tutar" ifadeleri yanlıştır.
- **İstemci üretimi:** Java tarafında iki yol vardır. Birincisi **Jakarta XML Web Services** (eski adıyla JAX-WS): WSDL'den istemci sınıfları üretilir. Bu araç Java 11'den itibaren JDK'nın içinde gelmez, ayrı bir bağımlılık olarak eklenir. Spring Boot 3 ile birlikte paket adları `javax.*` yerine `jakarta.*` olmuştur. İkinci yol **Spring Web Services**'tir: XSD'den JAXB sınıfları üretilir, çağrı `WebServiceTemplate` ile yapılır. Bu ikinci yaklaşım tasarım gereği sözleşme-öncedir.
- **Kurumsal servis veri yolu (ESB):** merkezî bir aracı katmandır. Protokol dönüştürme (SOAP ↔ dosya ↔ MQ), yönlendirme, mesaj biçimi dönüşümü ve akış yönetimi (orkestrasyon) yapar. Mikroservis çağında **"akıllı uç nokta, aptal boru"** tercih edilir: iş kuralı servislerin içinde durur, aradaki taşıma katmanı yalnız mesaj taşır. Sebebi şudur: ESB zamanla iş mantığını içine çeker. Sonunda tek noktadan dağıtılan, her ekibin sırasını beklediği bir darboğaza dönüşür.
- **JMS:** bir **Java arayüz standardıdır** (bugünkü adıyla Jakarta Messaging), tel üzerindeki protokol değildir. Sağlayıcıyı IBM MQ, ActiveMQ Artemis gibi ürünler gerçekler; kodun aynı kalır, yalnız sağlayıcı değişir. Mesajları saklayıp dağıtan bu sunucuya **aracı sunucu (broker)** denir.
- **Kuyruk (queue) ve konu (topic):** kuyruk **noktadan noktaya** çalışır. Bir mesajı yalnız bir tüketici alır; tüketici onaylayınca mesaj kuyruktan silinir. Birden çok tüketici bağlanırsa iş aralarında paylaşılır. Konu ise **yayınla/abone ol** çalışır: mesaj o an kayıtlı tüm abonelere kopyalanır. Abone yoksa mesaj kaybolur. **Kalıcı abonelik (durable subscription)** tanımlanmışsa, o sırada çevrimdışı olan abone mesajı sonra alabilir.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sözleşme-önce çalışma ve SOAP zarfının yapısı">
  <defs>
    <marker id="legacyint-ok1" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sözleşme önce, kod sonra: WSDL/XSD → SOAP zarfı</text>
  <rect x="16" y="48" width="180" height="170" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="106" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">WSDL + XSD</text>
  <text x="106" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">servisin sözleşmesi</text>
  <text x="106" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">işlemler, alanlar, tipler</text>
  <text x="106" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istemci sınıfları</text>
  <text x="106" y="170" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bundan ÜRETİLİR</text>
  <text x="106" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Jakarta XML WS / Spring WS</text>
  <line x1="200" y1="130" x2="230" y2="130" stroke="#6B3FA0" stroke-width="1.5" marker-end="url(#legacyint-ok1)"/>
  <rect x="236" y="48" width="408" height="170" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="440" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SOAP Envelope (zarf)</text>
  <rect x="252" y="84" width="376" height="48" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="440" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Header (başlık)</text>
  <text x="440" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">WS-Security: imza, şifreleme, kimlik belirteci</text>
  <rect x="252" y="142" width="376" height="62" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="440" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Body (gövde)</text>
  <text x="440" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş verisi — XSD'ye göre doğrulanır</text>
  <text x="440" y="198" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">hata durumunda: SOAP Fault</text>
  <text x="330" y="238" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">Tuzak: HTTP 200 dönse bile gövdede Fault olabilir — durum koduna bakmak yetmez.</text>
</svg>
```

### Nasıl çalışır?

1. **Sözleşmeyi al, kodu üret.** Ana bankacılık ekibi sana WSDL/XSD verir. Sen elle sınıf yazmazsın; derleme sırasında sözleşmeden üretirsin. Sözleşme değişince derleme kırılır — bu iyi bir şeydir, sürprizi çalışma zamanından derleme zamanına çeker.
2. **Zarfı doldur, güvenliği başlığa koy.** İş verisi gövdeye, kimlik ve imza başlığa gider. Yanıtı okurken **`SOAP Fault` kontrolü** ayrı yapılır: taşıma katmanı `HTTP 200` dönmüş olabilir ama gövdede hata durabilir.
3. **Eşzamansız işler için kuyruğa geç.** Anında yanıt gerekmeyen ya da eski sistemin yavaş olduğu akışlarda çağrı yerine mesaj kullanılır: uygulaman IBM MQ / ActiveMQ Artemis üzerinde bir kuyruğa mesaj bırakır.
4. **Onaylama modunu bilinçli seç.** JMS'te mesajın "işlendi" sayılma anını sen belirlersin. `AUTO_ACKNOWLEDGE`, mesajı dinleyiciye teslim edilir edilmez onaylar. `CLIENT_ACKNOWLEDGE` onaylama kararını koda bırakır. **İşlemsel oturum (transacted session)** ise alma ve gönderme işlemlerini tek bir mesajlaşma işlemi içinde toplar. Bu işlem geri alınırsa (rollback) mesaj kuyruğa geri döner.
5. **Zehirli mesaja hazırlıklı ol.** Sürekli hata veren bir mesaj, her geri alınışta yeniden teslim edilir ve kuyruğu tıkar. Buna **zehirli mesaj (poison message)** denir. Sağlayıcı her teslimde bir sayaç tutar (`JMSXDeliveryCount`). Sayaç belirlenen eşiği aşınca mesaj **ölü mektup kuyruğuna (DLQ — dead letter queue)** taşınır. Bunu IBM MQ'da geri alma kuyruğu ile `BOTHRESH` eşiği, Artemis'te `max-delivery-attempts` ile ölü mektup adresi yapar. Denemeler arasına da giderek artan bir gecikme (backoff) konur.
6. **Gün sonunda dosyayla mutabık ol.** Çevrim içi akış ne kadar iyi olursa olsun, banka gün sonunda (EOD) karşı tarafla **mutabakat dosyası** değişir; bu dosya SFTP ile taşınır ve toplu iş tarafından işlenir.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JMS kuyruğu ile konusunun farkı ve Kafka kütük modeli">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kuyruk mu, konu mu — ve Kafka neden üçüncü bir model?</text>
  <rect x="16" y="44" width="310" height="160" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="171" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Kuyruk (queue) — noktadan noktaya</text>
  <text x="171" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üretici → kuyruk → TEK tüketici</text>
  <text x="171" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">onaylanan mesaj kuyruktan silinir</text>
  <text x="171" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çok tüketici = iş paylaşımı</text>
  <text x="171" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hata → yeniden teslim, sayaç artar</text>
  <text x="171" y="186" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">eşik aşılınca → DLQ (ölü mektup)</text>
  <rect x="350" y="44" width="294" height="160" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="497" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Konu (topic) — yayınla/abone ol</text>
  <text x="497" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üretici → konu → TÜM aboneler</text>
  <text x="497" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her abone kendi kopyasını alır</text>
  <text x="497" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">abone yoksa mesaj kaybolur</text>
  <text x="497" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kalıcı abonelik (durable) ile</text>
  <text x="497" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çevrimdışı abone sonra alır</text>
  <text x="330" y="226" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Kafka: mesaj tüketilince SİLİNMEZ; saklama süresi boyunca kayıt kütüğünde kalır.</text>
  <text x="330" y="244" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">Nerede kaldığını broker değil tüketici bilir (offset) — geri sarıp yeniden okuyabilir.</text>
</svg>
```

**JMS ile Kafka'nın model farkı.** Bu, mülakatta en çok karıştırılan konulardan biridir. Klasik bir JMS kuyruğunda mesaj bir **iş parçasıdır**: teslim edilir, onaylanır ve aracı sunucudan (broker) silinir. Nerede kalındığı bilgisini de aracı sunucunun kendisi tutar.

Kafka'da ise mesaj bir **kayıt kütüğüne (log)** eklenir; tüketilmesi onu silmez. Mesaj, tanımlanan **saklama süresi** boyunca yerinde durur (ya da kütük sıkıştırma politikası ne diyorsa o kadar kalır). Nerede kalındığını aracı sunucu değil, tüketicinin kendi **okuma konumu (offset)** söyler. Bu yüzden Kafka'da geri sarıp aynı veriyi yeniden okuyabilirsin. Yeni bir tüketici grubunu en baştan başlatmak da mümkündür. Kuyrukta bunların hiçbiri yapılamaz, çünkü mesaj artık orada değildir.

Pratik sonuç şudur: JMS "iş dağıtımı" için, Kafka "olay akışı ve yeniden oynatma" için doğaldır. Kafka'nın **XA'ya katılamadığını** da not et. Kendi işlem (transaction) desteği vardır, ama iki fazlı işleme koordinatörüne kaynak olarak giremez.

**XA ve iki fazlı işleme (2PC).** Bir veritabanı güncellemesi ile bir MQ mesajının **ya ikisi birden ya hiçbiri** olmasını istersin. Bunun klasik çözümü XA'dır: bir **işlem yöneticisi (transaction manager)** iki kaynağı birden yönetir. Önce her kaynağa "hazır mısın?" diye sorar; buna **hazırlama (prepare)** fazı denir. Hepsi olumlu oy verirse "işle" der; bu da **işleme (commit)** fazıdır. XA bankada hâlâ vardır, çünkü ana bankacılık ürünleri bunu yıllardır destekler. Kurumsal uygulama sunucuları, IBM MQ ve ticari veritabanları XA konuşur; mevcut akışlar da bunun üzerine kurulmuştur.

Yeni tasarımda XA'dan kaçınılmasının üç somut nedeni vardır. **(1) Kilitler uzun süre açık kalır.** Kilit yalnız işlem boyunca değil, hazırlama ile işleme arasındaki ağ turu boyunca da tutulur; eşzamanlılık düşer. **(2) Koordinatör çökerse kaynaklar asılı kalır.** Hazırlamadan sonra koordinatör çökerse kaynaklar **şüpheli (in-doubt)** durumda kilitli kalır. Bu kilit, kurtarma kütüğü okunana ya da operatör elle müdahale edene kadar açılmaz. Bazen taraflar farklı karar verir; biri işler, öteki geri alır. Buna **sezgisel sonuç (heuristic outcome)** denir. **(3) Modern bileşenlerin çoğu XA kaynağı olamaz.** Kafka, çoğu HTTP servisi ve birçok bulut hizmeti bu gruba girer.

Yaygın bir ara çözüm **son kaynak optimizasyonudur (last resource gambit)**. İşleme katılan kaynaklardan yalnız biri XA desteklemiyorsa, o kaynak oylamada en sona bırakılır ve onun sonucu tüm işlemin kararı sayılır. Pratiktir ama tam atomiklik vermez; geriye dar bir hata penceresi kalır. Bugünkü tercih ise XA yerine **giden kutusu tablosu (outbox)** desenidir. İş verisi ile gönderilecek mesaj **tek bir yerel veritabanı işleminde** yazılır; tek kaynak olduğu için iki fazlı işlemeye gerek kalmaz. Ayrı bir yayıncı bu tabloyu okuyup mesajı gönderir. Tüketici ise aynı mesajı iki kez alsa bile tek kayıt üretecek biçimde yazılır; buna **tekrar edilse de sonucu değişmeyen (idempotent)** tüketici denir. Ayrıntısı için 130 numaralı "Idempotency ve Transactional Outbox" konusuna bak.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="XA iki fazlı işleme ile outbox deseninin karşılaştırması">
  <defs>
    <marker id="legacyint-ok2" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#B23B3B" font-family="system-ui">XA / iki fazlı işleme (2PC) — bedeli</text>
  <rect x="250" y="36" width="160" height="44" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="330" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Koordinatör</text>
  <rect x="60" y="106" width="150" height="42" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="135" y="132" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Veritabanı</text>
  <rect x="450" y="106" width="150" height="42" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="525" y="132" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">MQ kuyruğu</text>
  <path d="M272 82 L150 103" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#legacyint-ok2)"/>
  <path d="M388 82 L510 103" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#legacyint-ok2)"/>
  <text x="330" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1) hazırla → oy → 2) işle</text>
  <text x="330" y="170" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Kilitler hazırlama ile işleme arasında da açık kalır.</text>
  <text x="330" y="188" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Koordinatör çökerse kaynaklar şüpheli (in-doubt) kilitli kalır.</text>
  <line x1="30" y1="206" x2="630" y2="206" stroke="#5D7789" stroke-width="1" stroke-dasharray="5 4"/>
  <text x="330" y="228" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Yeni tasarımda tercih: outbox + idempotent tüketici</text>
  <rect x="24" y="242" width="200" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="124" y="262" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">tek yerel işlem</text>
  <text x="124" y="280" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş verisi + outbox satırı</text>
  <line x1="228" y1="266" x2="244" y2="266" stroke="#5D7789" stroke-width="1.3" marker-end="url(#legacyint-ok2)"/>
  <rect x="248" y="242" width="170" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="333" y="262" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">yayıncı</text>
  <text x="333" y="280" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">outbox'ı okur, gönderir</text>
  <line x1="422" y1="266" x2="438" y2="266" stroke="#5D7789" stroke-width="1.3" marker-end="url(#legacyint-ok2)"/>
  <rect x="442" y="242" width="194" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="539" y="262" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">idempotent tüketici</text>
  <text x="539" y="280" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iki teslim ≠ iki kayıt</text>
</svg>
```

**Dosya tabanlı entegrasyon.** Bankada en eski ve en dirençli entegrasyon biçimidir. Gün sonu mutabakat dosyası, tahsilat listesi ve kart hareketleri SFTP ile taşınır. Tuzakları sayılıdır ama her biri pahalıdır.

Birincisi **yarım dosya okumadır**. Karşı taraf dosyayı yazarken toplu iş dosyayı görür ve okumaya başlar; sonuç, eksik kayıt işlemektir. Çözüm gönderen taraftadır: dosya **geçici bir adla yazılır, bitince nihai ada taşınır**. Yeniden adlandırma anlık olduğu için yarım dosya hiç görünmez. İkinci yol, dosyanın tamamlandığını bildiren ayrı bir **kontrol/bayrak dosyası** bırakmaktır; alıcı yalnız onu görünce başlar.

İkincisi **mükerrer işlemedir**. Aynı dosya iki kez okunursa gün sonu iki kez muhasebeleşir. Çözüm, dosya adının ve içerik özetinin (hash) işlenmiş dosya tablosuna yazılmasıdır. Aynı özet ikinci kez gelirse dosya işlenmez.

Üçüncüsü **karakter kodlamasıdır**. Eski sistemler çoğu zaman `ISO-8859-9` veya `windows-1254` üretir, yeni sistem ise UTF-8 bekler. Java 18'den beri sanal makinenin varsayılan kodlaması UTF-8'dir; ama bu, **dosyanın** kodlamasını değiştirmez. Okurken kodlama **açıkça** verilmezse Türkçe karakterler (ı, ğ, ş, İ) bozulur. Sabit uzunluklu kayıtta bu yüzden alan sınırları da kayabilir: çok baytlı kodlamada karakter sayısı ile bayt sayısı aynı değildir.

Dördüncüsü **büyük/küçük harf ve sıralama** farkıdır. Türkçe yerel ayarında `I` harfinin küçüğü `i` değil `ı`'dır. Bu yüzden kod içinde kimlik karşılaştırması yaparken yerelden bağımsız karşılaştırma kullanılmalıdır.

**Strangler fig ile kademeli sarmalama.** Eski sistemi tek seferde değiştirmek (big-bang) bankada gerçekçi değildir: risk çok büyüktür ve geri dönüş yolu yoktur. **Strangler fig** (sarmaşık deseni), adını ev sahibi ağacı yıllar içinde sarıp yerini alan sarmaşık incirden alır. Şöyle çalışır: eski sistemin önüne bir **yönlendirme katmanı** konur. Bu katmana cephe (facade) denir ve çoğu zaman API ağ geçididir. Başlangıçta tüm istekler eskiye gider. Sonra işlevler **parça parça** yeni servise taşınır ve yönlendirici o işlevi yeniye yollamaya başlar.

Geçiş sırasında **karşılaştırmalı çalıştırma (parallel run / shadow)** yapılır. İstek her iki sisteme de gönderilir. Kullanıcıya eski sistemin yanıtı döner; yeni sistemin yanıtı yalnız kaydedilip karşılaştırılır. Fark oranı kabul edilebilir düzeye inince trafik yeniye çevrilir. Tüm işlevler taşınınca eski sistem susturulur. Kritik nokta şudur: yeni servis eski sistemin veritabanına doğrudan yazmamalıdır. Paylaşılan veritabanı iki sistemi birbirine kilitler. Ayrıca eski sistemin hiçbir yerde yazılı olmayan iş kurallarını (kontrol, tetikleyici, saklı yordam) atlamana yol açar.

### Mülakatta ne sorulur / tuzaklar

- **"SOAP ile REST arasındaki fark nedir?"** Beklenen: sözleşme katılığı (WSDL/XSD zorunlu, doğrulanır), tekdüze hata modeli (`SOAP Fault`), taşımadan bağımsızlık, mesaj düzeyi güvenlik (WS-Security). "SOAP daha hızlıdır" ya da "SOAP durum bilgisi tutar" demek elenme sebebidir.
- **"SOAP istemcisini nasıl yazarsın?"** Beklenen: elle değil, **sözleşmeden üreterek**. Sözleşme değişince derlemenin kırılması istenen davranıştır.
- **"Kuyruk ile konu farkı?"** Beklenen: noktadan noktaya (tek tüketici, onaylanınca silinir) ile yayınla/abone ol (tüm abonelere kopya). Ardından gelen soru genelde Kafka farkıdır: tüketilen mesaj silinmez, offset tüketicidedir, geri sarılabilir.
- **"Bir mesaj sürekli hata veriyor, ne olur?"** Beklenen: yeniden teslim, sayaç, eşik, DLQ ve gecikmeli yeniden deneme. "Sonsuza kadar yeniden dener" ya da "kaybolur" cevapları yanlıştır.
- **"Veritabanı ile MQ'yu atomik yapmak için XA kullanır mısın?"** Beklenen: bankada var olduğunu bil, bedelini (uzun kilit, in-doubt, sınırlı destek) say, yeni tasarımda outbox + idempotent tüketici öner.
- **"Gün sonu dosyası bazen eksik okunuyor."** Beklenen: geçici ad + yeniden adlandırma ya da kontrol dosyası; ayrıca dosya adı/özet ile mükerrer işleme koruması ve kodlamanın açıkça verilmesi.
- **"Eski core'u nasıl değiştirirsin?"** Beklenen: strangler fig, yönlendirme katmanı, parça parça taşıma, karşılaştırmalı çalıştırma, en son eski sistemi kapatma; paylaşılan veritabanına yazmama.

### Tek cümlede

Eski sistem entegrasyonu; **sözleşmeden üretilen** SOAP istemcileriyle konuşmayı, JMS kuyruk/konu ayrımını ve zehirli mesaj–DLQ döngüsünü doğru kurmayı gerektirir. Bunun üstüne şunlar gelir: XA'nın bedelini bilip yeni tasarımda **outbox + idempotent tüketiciyi** seçmek, dosya akışındaki yarım okuma/mükerrer işleme/kodlama tuzaklarını kapatmak ve eski sistemi **strangler fig** ile karşılaştırmalı çalıştırarak kademeli sarmalamak.

# legacy-1 | Orta
## Soru
Ana bankacılık ekibi müşteri sorgulama servisi için sana bir WSDL dosyası verdi. Mülakatta "SOAP ile REST arasındaki gerçek fark nedir?" diye soruluyor. En doğru cevap hangisidir?
## A
SOAP mesajları ikili (binary) biçimde taşındığı için REST'in metin tabanlı gövdesinden her koşulda daha hızlıdır; asıl fark performanstır
🤖 SOAP mesajı XML'dir, ikili değildir; ayrıntılı XML zarfı çoğu durumda JSON'dan daha büyüktür.
## B
SOAP, WSDL/XSD ile makine-okunur ve doğrulanan katı bir sözleşme ile tekdüze bir hata modeli (`SOAP Fault`) sunar; REST'te sözleşme isteğe bağlıdır ve hata HTTP durum koduyla taşınır
🤖 Doğru: fark biçimde değil, sözleşmenin bağlayıcılığında ve hata modelinin standartlığındadır.
## C
SOAP yalnız HTTP üzerinde çalışabilir; REST ise HTTP, JMS ve SMTP dahil birçok taşıma katmanı üzerinde çalışabildiği için daha esnektir
🤖 durum tam tersidir; SOAP taşıma katmanından bağımsızdır ve JMS üzerinden de taşınabilir, REST ise HTTP'ye bağlıdır.
## D
SOAP durum bilgisi tutan (stateful) bir protokoldür ve oturum yönetimini kendisi yapar; REST durumsuz olduğu için asıl ayrım budur
🤖 SOAP oturum yönetmez, mesaj başına çalışır; durum tutma protokolün değil uygulama tasarımının konusudur.
## Doğru
B
## Açıklama
SOAP ile REST arasındaki fark "XML mi JSON mu" ya da "hızlı mı yavaş mı" değildir. SOAP'ta sözleşme **zorunludur ve bağlayıcıdır**: WSDL hangi işlemlerin bulunduğunu, XSD mesajın alanlarını ve tiplerini tanımlar; gelen mesaj bu şemaya göre **doğrulanır** ve istemci sınıfları sözleşmeden üretilir. Hata modeli de standarttır: her hata gövdede bir **`SOAP Fault`** olarak döner. REST'te ise sözleşme (OpenAPI) isteğe bağlı bir belgedir, çoğu zaman zorlayıcı değildir ve hata HTTP durum koduyla taşınır. Performans iddiası (A) yanlıştır — SOAP mesajı XML'dir, ikili değildir ve zarf yükü nedeniyle genelde daha büyüktür. Taşıma katmanı iddiası (C) ters çevrilmiştir: **SOAP taşımadan bağımsızdır**, HTTP dışında JMS üzerinden de taşınabilir; REST doğrudan HTTP'nin üstüne kuruludur. Durum tutma (D) protokolün değil uygulamanın konusudur; SOAP oturum yönetmez. Pratik tuzak olarak şunu da bil: SOAP çağrısında taşıma katmanı `HTTP 200` dönse bile gövdede `Fault` olabilir, bu yüzden yalnız durum koduna bakmak yetmez.

# legacy-2 | Orta
## Soru
Ekipte bazı akışlar IBM MQ kuyruğu, bazıları Kafka üzerinden çalışıyor. Bir JMS kuyruğu ile bir Kafka konusu arasındaki temel model farkı nedir?
## A
Kafka'da mesaj tüketildiği anda kütükten silinir; JMS kuyruğunda ise mesaj kalıcı olarak saklanır ve istenince yeniden okunabilir
🤖 roller ters verilmiş; silinen JMS kuyruğundaki mesajdır, Kafka mesajı saklama süresi boyunca tutar.
## B
İkisi aynı modeldir; Kafka yalnızca daha hızlı çalışan bir JMS gerçeklemesidir ve JMS arayüzü üzerinden kullanılır
🤖 Kafka JMS gerçeklemesi değildir; kütük tabanlı ve offset temelli modeli JMS'in teslim-onay-sil modelinden farklıdır.
## C
Kafka broker'ı her tüketici grubu için mesajın ayrı bir kopyasını saklar; JMS kuyruğunda ise tek kopya tutulup sırayla dağıtılır
🤖 Kafka tek kopya tutar, gruplar aynı bölümü kendi offset'leriyle okur; kopya çoğaltma tüketici sayısına bağlı değildir.
## D
JMS kuyruğunda mesaj bir tüketiciye teslim edilip onaylanınca broker'dan silinir; Kafka mesajı saklama süresi boyunca kayıt kütüğünde tutar ve tüketici nerede kaldığını offset ile kendi taşır
🤖 Doğru: biri iş dağıtımı modelidir, diğeri geri sarılabilir kayıt kütüğü modelidir.
## Doğru
D
## Açıklama
Klasik JMS kuyruğunda mesaj bir **iş parçasıdır**: bir tüketiciye teslim edilir, tüketici onaylayınca (ack) mesaj **broker'dan silinir** ve nerede kalındığı bilgisini broker tutar. Kafka'da mesaj bir **kayıt kütüğüne (log)** eklenir; tüketilmesi onu silmez, mesaj tanımlı **saklama süresi** (ya da kütük sıkıştırma politikası) boyunca yerinde durur. Nerede kalındığını broker değil **tüketici** bilir: her tüketici grubu kendi **offset**'ini taşır, bu yüzden geri sarıp aynı veriyi yeniden okumak veya yeni bir grubu geçmişten başlatmak mümkündür — kuyrukta bu mümkün değildir, çünkü mesaj artık orada yoktur. A şıkkı bu iki davranışı ters çevirir. Kafka bir JMS gerçeklemesi değildir (B); JMS bir Java arayüz standardıdır, Kafka'nın kendi istemci modeli vardır (uyumluluk katmanları ayrı bir konudur). C şıkkındaki "grup başına kopya" da yanlıştır: Kafka mesajın **tek** kopyasını tutar (çoğaltma/replikasyon dayanıklılık içindir, tüketici sayısıyla ilgili değildir); farklı gruplar aynı bölümü kendi offset'leriyle bağımsız okur. Pratik sonuç: JMS iş dağıtımı için, Kafka olay akışı ve yeniden oynatma için doğaldır.

# legacy-3 | İleri
## Soru
IBM MQ'dan gelen bir mesaj, işlemsel oturum (transacted session) kullanan dinleyicide sürekli hata veriyor: her seferinde işlem geri alınıyor, mesaj kuyruğa geri dönüyor ve yeniden teslim ediliyor. Kuyruk tıkandı ve arkasındaki mesajlar işlenemiyor. Doğru çözüm nedir?
## A
Sağlayıcının tuttuğu yeniden teslim sayacını bir eşiğe bağlamak; eşik aşılınca mesajı ölü mektup kuyruğuna (DLQ) taşımak ve denemeler arasına artan gecikme koymak
🤖 Doğru: zehirli mesaj sonlu denemeden sonra yoldan çekilir, kuyruk akmaya devam eder ve mesaj kaybolmadan incelenebilir.
## B
Onaylama modunu `AUTO_ACKNOWLEDGE` yapmak; mesaj hata alsa bile teslimle birlikte onaylanacağı için kuyruktan düşer ve tıkanma sona erer
🤖 bu, sorunu sessiz veri kaybına çevirir; hatalı mesaj hiçbir yere kaydedilmeden yok olur ve bankada bu kabul edilemez.
## C
MQ sunucusunu yeniden başlatmak; yeniden başlatma sırasında teslim sayaçları sıfırlanacağı için mesaj bir sonraki denemede işlenir
🤖 sorun mesajın içeriğinde ya da işleme kodunda; yeniden başlatma aynı döngüyü baştan başlatır.
## D
Dinleyiciyi durdurup mesajı yönetim aracıyla elle silmek; zehirli mesaj için tek uygulanabilir yöntem budur
🤖 elle silme ölçeklenmez, kaydı bırakmaz ve mesajı incelenemez biçimde yok eder; bu iş için DLQ vardır.
## Doğru
A
## Açıklama
Sürekli hata veren mesaja **zehirli mesaj (poison message)** denir. İşlemsel oturumda hata geri alma (rollback) ürettiği için mesaj kuyruğa geri döner ve yeniden teslim edilir; hiçbir sınır konmazsa bu sonsuz bir döngüdür ve kuyruğu tıkar. Doğru çözüm sağlayıcının zaten tuttuğu **yeniden teslim sayacını** (`JMSXDeliveryCount`) bir eşiğe bağlamaktır: IBM MQ'da geri alma eşiği (`BOTHRESH`) ve geri alma kuyruğu, ActiveMQ Artemis'te `max-delivery-attempts` ve ölü mektup adresi bunu yapar; eşik aşılınca mesaj **DLQ'ya** taşınır, kuyruk akmaya devam eder ve mesaj kaybolmadan incelenebilir. Denemeler arasına artan gecikme (backoff) koymak, geçici bir arıza sırasında kuyruğu boşuna döndürmeyi de önler. `AUTO_ACKNOWLEDGE`'a geçmek (B) tıkanmayı kaldırır ama bunu **sessiz veri kaybı** pahasına yapar — hatalı mesaj hiçbir kayıt bırakmadan yok olur, bir bankada bu kabul edilemez. Sunucuyu yeniden başlatmak (C) mesajın içeriğinden veya işleme kodundan kaynaklanan hatayı değiştirmez, aynı döngüyü baştan başlatır. Elle silmek (D) tek seferlik bir müdahaledir; ölçeklenmez, iz bırakmaz ve mesajı incelenemez kılar.

# legacy-4 | Uzman
## Soru
Kredi limiti düşümünü veritabanına yazan ve aynı anda muhasebe sistemine MQ mesajı gönderen bir servis yazıyorsun; ikisinin de ya birlikte olması ya da hiç olmaması gerekiyor. XA / iki fazlı işleme (2PC) hakkında hangisi doğrudur?
## A
XA yeni tasarımlarda önerilen çözümdür; iki fazlı işleme atomikliği ek bir performans veya kullanılabilirlik bedeli getirmeden sağladığı için varsayılan tercih olmalıdır
🤖 bedelsiz değildir; kilitler ağ turu boyunca açık kalır ve koordinatör tek hata noktası haline gelir.
## B
İki işlemi ayrı ayrı işlemek yeterlidir; önce veritabanına yaz, hemen ardından mesajı gönder — sıra doğru olduğu sürece tutarsızlık oluşmaz
🤖 iki işlem arasında süreç çökerse veritabanı güncellenmiş ama mesaj gönderilmemiş olur; sıra bunu engellemez.
## C
XA atomikliği sağlar ama kilitleri hazırlama ile işleme arasında da tutar; yeni tasarımda tercih, iş verisi ile mesajı tek yerel işlemde outbox tablosuna yazıp ayrı bir yayıncıyla göndermektir
🤖 Doğru: XA'nın bedeli bilinir, outbox aynı garantiyi tek kaynaklı yerel işlem ve idempotenslik ile daha ucuza verir.
## D
XA'ya gerek yoktur; mesajı önce gönderip ardından veritabanını güncellemek daha güvenlidir, çünkü mesaj gönderimi başarısız olursa veritabanına hiç dokunulmamış olur
🤖 mesaj gidip veritabanı güncellemesi başarısız olursa muhasebeye gerçekleşmemiş bir limit düşümü bildirilmiş olur.
## Doğru
C
## Açıklama
**XA / iki fazlı işleme**, bir işlem yöneticisinin iki kaynağa önce "hazır mısın?" (**prepare**) diye sorup hepsi olumlu oy verirse "işle" (**commit**) demesidir; atomikliği gerçekten sağlar ve bankada hâlâ vardır, çünkü ana bankacılık ürünleri bunu yıllardır destekler. Ama bedelsiz değildir (A yanlış): kilitler yalnız işlem boyunca değil, **hazırlama ile işleme arasındaki ağ turu boyunca da** açık kalır ve eşzamanlılık düşer; koordinatör hazırlamadan sonra çökerse kaynaklar **şüpheli (in-doubt)** durumda kilitli kalır, kurtarma kütüğü okunana ya da operatör müdahale edene kadar açılmaz, bazen taraflar farklı karar verip **sezgisel (heuristic)** sonuç doğar; üstelik Kafka gibi modern bileşenlerin çoğu XA kaynağı olamaz. İki işlemi sırayla yapmak (B) ve sırayı ters çevirmek (D) aynı hatanın iki yüzüdür: iki işlem arasında süreç çökerse ya veritabanı güncellenip mesaj gitmez ya da mesaj gidip veritabanı güncellenmez — sıralama bu pencereyi kapatmaz. Bugünkü tercih **outbox** desenidir: iş verisi ile gönderilecek mesaj **tek bir yerel veritabanı işleminde** yazılır (tek kaynak olduğu için 2PC gerekmez), ayrı bir yayıncı outbox tablosunu okuyup mesajı gönderir ve en az bir kez teslim garantisi altında tüketici **idempotent** yazılır. Not: mevcut bir XA akışında yalnız bir kaynak XA desteklemiyorsa uygulanan **son kaynak optimizasyonu (last resource gambit)** pratik bir ara çözümdür ama tam atomiklik vermez, dar bir hata penceresi bırakır.

# legacy-5 | İleri
## Soru
Gün sonu mutabakat dosyası karşı taraf tarafından SFTP dizinine yazılıyor, toplu işiniz dizini tarayıp dosyayı işliyor. İki sorun var: bazı geceler dosya eksik kayıtla işleniyor, bazı gecelerse aynı dosya iki kez işlenip kayıtlar mükerrer oluşuyor. Doğru çözüm nedir?
## A
Dosyanın dizinde görülmesinden sonra birkaç dakika beklemek; yazma süresi genelde sabit olduğu için bu bekleme iki sorunu da ortadan kaldırır
🤖 bekleme süresi bir tahmindir, yavaş bir gecede yine eksik okunur ve mükerrer işlemeyle hiç ilgisi yoktur.
## B
Gönderen dosyayı geçici adla yazıp tamamlanınca nihai ada taşımalı ya da ayrı bir kontrol dosyası bırakmalı; alıcı da işlediği dosyanın adını ve içerik özetini kaydedip aynı dosyayı ikinci kez işlememeli
🤖 Doğru: biri yarım dosya okumayı, diğeri mükerrer işlemeyi kaynağında keser ve ikisi de zamanlama tahminine dayanmaz.
## C
Dosyanın son satırındaki toplam kayıt sayısını okuyup satır sayısıyla karşılaştırmak; bu tek kontrol hem eksik okumayı hem mükerrer işlemeyi engeller
🤖 sayaç satırı eksik dosyayı fark ettirebilir ama aynı dosyanın ikinci kez işlenmesini hiçbir biçimde engellemez.
## D
Dosyayı işledikten sonra kaynak dizinden silmek; dosya ortadan kalktığı için ikinci kez işlenemez ve eksik okuma da kendiliğinden çözülür
🤖 silme yetkisi çoğu zaman yoktur, silme ile işleme arasında hâlâ pencere kalır ve eksik okumayla ilgisi yoktur.
## Doğru
B
## Açıklama
İki ayrı sorun var ve her birinin kendi çözümü var. **Yarım dosya okuma**, karşı taraf dosyayı yazarken toplu işin dosyayı görüp okumaya başlamasıdır; çözümü zamanlama tahmini değil (A yanlış, çünkü yavaş bir gecede süre tutmaz), **atomik görünürlüktür**: gönderen dosyayı geçici bir adla (ör. `.tmp` uzantısıyla) yazar, tamamlanınca nihai adına taşır — yeniden adlandırma anlıktır — ya da dosyanın hazır olduğunu bildiren ayrı bir **kontrol/bayrak dosyası** bırakır ve alıcı yalnız onu görünce başlar. **Mükerrer işleme** ise ayrı bir sorundur; çözümü alıcı tarafında **idempotenslik**tir: işlenen her dosyanın adı, tarihi ve içerik özeti (hash) bir tabloya yazılır, aynı özet ikinci kez gelirse işlenmez. Sayaç satırı kontrolü (C) değerli bir bütünlük kontrolüdür ve eksik dosyayı yakalayabilir, ama aynı **eksiksiz** dosyanın iki kez işlenmesine karşı hiçbir şey yapmaz. Dosyayı silmek (D) de yeterli değildir: karşı tarafın dizininde silme yetkin olmayabilir, işleme ile silme arasında hâlâ bir pencere kalır ve eksik okuma sorununa dokunmaz. Bu akışta ayrıca **karakter kodlaması** açıkça verilmelidir: eski sistemler sıklıkla `ISO-8859-9`/`windows-1254` üretir; Java 18'den beri sanal makinenin varsayılanı UTF-8 olsa da bu dosyanın kodlamasını değiştirmez, kodlama belirtilmezse Türkçe karakterler bozulur ve sabit uzunluklu kayıtta alan sınırları kayabilir.

# legacy-6 | İleri
## Soru
Ana bankacılık sistemindeki müşteri sorgulama işlevini yeni bir mikroservise taşımak istiyorsunuz. Sistem 20 yıllık, dokümantasyonu eksik ve kesinti kabul edilmiyor. Hangi yaklaşım doğrudur?
## A
Eski sistemin davranışını inceleyip yeni servisi eksiksiz yazmak ve belirlenen bir hafta sonu tüm trafiği tek seferde yeni servise çevirmek; en hızlı ve en temiz geçiş budur
🤖 tek seferlik geçişte dokümante edilmemiş kuralların hepsi aynı anda üretimde patlar ve geri dönüş yolu yoktur.
## B
Eski sistemi olduğu gibi bırakıp yeni servisi bağımsız geliştirmek ve iki sistemi kalıcı olarak yan yana çalıştırmak; kullanıcı hangisini isterse onu kullanır
🤖 kalıcı ikilik iki ayrı doğruluk kaynağı üretir; veriler ayrışır ve mutabakat sorunu kalıcı hale gelir.
## C
Yeni servisi doğrudan ana bankacılık veritabanına yazacak biçimde kurmak; böylece eski sistemin arayüzleriyle uğraşmadan hızla ilerlenir
🤖 paylaşılan veritabanı iki sistemi birbirine kilitler ve eski sistemin saklı yordam/tetikleyicilerdeki kurallarını atlatır.
## D
Eski sistemin önüne bir yönlendirme katmanı koymak, işlevleri parça parça yeni servise taşımak, geçiş sırasında iki sistemi karşılaştırmalı çalıştırmak (parallel run) ve fark kapanınca trafiği çevirip eski yolu kapatmak
🤖 Doğru: strangler fig deseni riski küçük parçalara böler ve her adımda geri dönüş yolunu açık bırakır.
## Doğru
D
## Açıklama
Yirmi yıllık ve eksik dokümante edilmiş bir sistemin en değerli varlığı, hiçbir yerde yazılı olmayan **iş kurallarıdır**; bunlar ancak üretimde davranış karşılaştırılarak ortaya çıkar. **Strangler fig** deseni tam bunun için vardır: eski sistemin önüne bir **yönlendirme katmanı** (cephe/facade, çoğu zaman API ağ geçidi) konur ve başlangıçta tüm istekler eskiye gider; sonra işlevler **parça parça** yeni servise taşınır ve yönlendirici o işlevi yeniye yollar. Geçiş sırasında **karşılaştırmalı çalıştırma (parallel run / shadow)** yapılır: istek her iki sisteme gönderilir, kullanıcıya eski sistemin yanıtı döner, yeni sistemin yanıtı yalnız kaydedilip karşılaştırılır; fark oranı kabul edilebilir düzeye inince trafik çevrilir. Tüm işlevler taşınınca eski yol kapatılır. Tek seferlik büyük geçiş (A) tüm bilinmeyen kuralları aynı anda üretime taşır ve geri dönüş yolu bırakmaz. İki sistemi kalıcı olarak yan yana yaşatmak (B) iki ayrı doğruluk kaynağı üretir; veriler ayrışır ve mutabakat kalıcı bir yük olur. Yeni servisi doğrudan eski veritabanına yazdırmak (C) ise en tehlikelisidir: paylaşılan veritabanı iki sistemi şemayla birbirine kilitler ve eski sistemin kısıt, tetikleyici, saklı yordam içinde yaşayan kurallarını atlatarak sessiz veri bozulması üretir — entegrasyon eski sistemin **arayüzü** üzerinden yapılmalıdır.
