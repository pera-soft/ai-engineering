---
slug: event-driven-mimari
baslik: "Olay Güdümlü Mimari: Notification, State Transfer ve Event Sourcing"
kademe: İleri
sira: 125
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Alışılmış program yazma biçiminde bir servis diğerini doğrudan çağırır ve yanıtı bekler: "Sipariş servisi, Stok servisini çağır, stok düşene kadar bekle." Bu yöntemde iki taraf birbirine kenetlenir; Stok servisi kapalıysa sipariş de alınamaz.

Olay güdümlü mimari (event-driven architecture) bunu tersine çevirir: servis kimseyi çağırmaz, sadece **olan biteni duyurur**. İlgilenen başkaları bu duyuruyu okuyup kendi işini yapar. Gündelik benzetme: okul panosuna "1-A gezisi iptal edildi" ilanı asılır. İlanı asan kişi kimin okuyacağını bilmez ve kimseyi beklemez; öğretmen, servis şoförü ve muhasebe ilanı görüp kendi işini ayarlar. Kazanç şudur: yeni bir ilgilenen eklendiğinde ilanı asan tarafta hiçbir şey değişmez.

### Temel kavramlar

- **Olay (event):** Geçmişte olmuş, artık değiştirilemeyecek bir gerçeğin kaydıdır: `SiparişOluşturuldu`, `ÖdemeAlındı`. Komuttan farkı zamandır — komut "şunu yap" der, olay "şu oldu" der.
- **Üretici (producer)** olayı yayınlar, **tüketici (consumer)** okur. Aradaki **broker** (Kafka, RabbitMQ) olayı saklayan postanedir.
- **Gövde (payload):** olayın içinde taşınan veri.
- **Bağlılık (coupling):** iki tarafın birbirine bağımlılığı. **Zamansal (temporal)** bağlılık "ikisi aynı anda ayakta olmalı" demektir; **şema/anlam bağlılığı** ise "tüketici payload'ın alanlarına ve anlamına güvenir" demektir.

Aynı isim altında üç farklı **stil** toplanır. Mülakatta en sık düşülen tuzak bu üçünü birbirinden ayırt edememektir.

- **Olay bildirimi (event notification):** olay yalnız bir kimlik taşır. Tüketici ayrıntıyı öğrenmek için kaynağa geri döner.
- **Durumu taşıyan olay (event-carried state transfer):** olay, tüketicinin ihtiyaç duyduğu bilgiyi eksiksiz içinde taşır.
- **Olayla kayıt tutma (event sourcing):** durum bir tabloda değil, olay akışının kendisinde saklanır. Güncel durum, olayların baştan yeniden oynatılmasıyla (replay) türetilir. Yani olay günlüğü tek gerçeklik kaynağıdır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç olay stili: notification, state transfer, event sourcing">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı isim, üç farklı stil</text>
  <rect x="24" y="44" width="188" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Event Notification</text>
  <text x="118" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">payload = sadece id</text>
  <text x="118" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tüketici kaynağa</text>
  <text x="118" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">geri-çağrı yapar</text>
  <text x="118" y="164" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">gizli runtime coupling</text>
  <rect x="226" y="44" width="188" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">State Transfer</text>
  <text x="320" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">payload = tam durum</text>
  <text x="320" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tüketici kaynağı</text>
  <text x="320" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">çağırmaz (otonom)</text>
  <text x="320" y="164" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">kopya + bayatlama riski</text>
  <rect x="428" y="44" width="188" height="150" rx="12" fill="#F3EEF8" stroke="#6B4E9E" stroke-width="1.5"/>
  <text x="522" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#6B4E9E" font-family="system-ui">Event Sourcing</text>
  <text x="522" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">durum = olay akışı</text>
  <text x="522" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">replay/fold ile</text>
  <text x="522" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yeniden kurulur</text>
  <text x="522" y="164" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">log = tek gerçeklik</text>
</svg>
```

Bir de "akışı kim yönetir?" ekseni vardır. **Koreografi**de merkezi bir beyin yoktur; her servis gelen olaya tepki verip yeni olay yayınlar, akış bu zincirden kendiliğinden doğar. **Orkestrasyon**da merkezi bir orkestratör adımları sırayla ve açıkça çağırır; akış görünür olur ama orkestratör bir bağımlılık noktasına dönüşür. Bu eksen, payload stilinden bağımsızdır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Koreografi ile orkestrasyon koordinasyon farkı">
  <defs>
    <marker id="eda-koor-g" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#25804C"/>
    </marker>
    <marker id="eda-koor-b" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Akışı kim yönetir?</text>
  <text x="160" y="50" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Koreografi — merkez yok</text>
  <circle cx="90" cy="110" r="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="90" y="114" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Svc A</text>
  <circle cx="240" cy="110" r="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="240" y="114" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Svc B</text>
  <circle cx="165" cy="205" r="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="209" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Svc C</text>
  <line x1="122" y1="105" x2="207" y2="105" stroke="#25804C" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#eda-koor-g)"/>
  <line x1="235" y1="140" x2="185" y2="178" stroke="#25804C" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#eda-koor-g)"/>
  <line x1="140" y1="180" x2="95" y2="142" stroke="#25804C" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#eda-koor-g)"/>
  <text x="160" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">olay zinciri: herkes tepki verir</text>
  <line x1="320" y1="58" x2="320" y2="232" stroke="#5D7789" stroke-width="1" stroke-dasharray="3 3"/>
  <text x="480" y="50" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Orkestrasyon — merkez var</text>
  <rect x="425" y="80" width="110" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="480" y="107" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Orkestratör</text>
  <rect x="378" y="180" width="70" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="413" y="204" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Svc A</text>
  <rect x="455" y="180" width="70" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="490" y="204" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Svc B</text>
  <rect x="532" y="180" width="70" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="567" y="204" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Svc C</text>
  <line x1="460" y1="124" x2="418" y2="176" stroke="#186399" stroke-width="1.8" marker-end="url(#eda-koor-b)"/>
  <line x1="485" y1="124" x2="488" y2="176" stroke="#186399" stroke-width="1.8" marker-end="url(#eda-koor-b)"/>
  <line x1="505" y1="124" x2="558" y2="176" stroke="#186399" stroke-width="1.8" marker-end="url(#eda-koor-b)"/>
  <text x="480" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">merkez adımları açıkça çağırır</text>
</svg>
```

### Nasıl çalışır?

Üretici olayı broker'a yazar ve işine döner. Broker olayı **tamponlar** (saklar). Tüketici hazır olduğunda kendi hızında okur. Böylece üretici ile tüketicinin aynı anda ayakta olma zorunluluğu, yani **zamansal bağlılık** ortadan kalkar. "Artık gevşek bağlıyız" hissi buradan gelir.

Ama bağlılık yok olmaz, **yer değiştirir**: tüketici payload'ın yapısına ve anlamına bağımlı kalmaya devam eder. Üretici bir alanı yeniden adlandırır, ikiye böler ya da anlamını değiştirirse tüketiciler sessizce kırılır. Bu yüzden olay şeması bir **yayınlanmış sözleşmedir**; tıpkı bir REST API gibi sürümlenir ve yönetilir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Broker zamansal bağı çözer ama şema bağı kalır">
  <defs>
    <marker id="eda-cpl-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Broker zamanı ayırır, sözleşmeyi ayırmaz</text>
  <text x="207" y="60" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">aynı anda ayakta olma şartı YOK</text>
  <rect x="30" y="70" width="130" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="95" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Üretici</text>
  <text x="95" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">olay yayınlar</text>
  <rect x="255" y="70" width="130" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Broker</text>
  <text x="320" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">olayı tamponlar</text>
  <rect x="480" y="70" width="130" height="56" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="545" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Tüketici</text>
  <text x="545" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">payload'ı okur</text>
  <line x1="160" y1="98" x2="253" y2="98" stroke="#186399" stroke-width="2" marker-end="url(#eda-cpl-ar)"/>
  <line x1="385" y1="98" x2="478" y2="98" stroke="#186399" stroke-width="2" marker-end="url(#eda-cpl-ar)"/>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">temporal coupling ÇÖZÜLDÜ</text>
  <path d="M95 130 C 95 176, 545 176, 545 130" fill="none" stroke="#8A2E2E" stroke-width="1.8" stroke-dasharray="6 3"/>
  <text x="320" y="200" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">şema/anlam coupling KALIR: alan değişirse tüketici kırılır</text>
</svg>
```

Üretici ile tüketiciler **ayrı ayrı** yayına alındığı için ikisini aynı anda güncelleyemezsiniz. Şemayı eskiyi bozmadan değiştirmeye **şema evrimi (schema evolution)** denir. Güvenli değişikliğin kuralı **ekleyici (additive)** ve **isteğe bağlı (optional)** olmaktır. Yani: yeni alanı isteğe bağlı olarak ekleyin, var olan alanı silmeyin, tipini veya anlamını değiştirmeyin.

Böylece eski tüketici bilmediği alanı yok sayar. Yeni tüketici de alanın hiç gelmemesine karşı dayanıklı kalır. Avro ve Protobuf gibi formatlar tam da bu kuralları zorlamak için vardır. Buna karşılık alan silmek ya da tip değiştirmek kırıcıdır. "Hepsini aynı anda yayına alırız" fikri ise dağıtık sistem gerçeğiyle çelişir.

Stil seçimi de bilinçli olmalıdır. Notification küçük bir gövde gönderir. Ama tüketiciyi ayrıntı için kaynağa geri döndürdüğü için **gizli bir çalışma zamanı (runtime) bağlılığı** doğurur: kaynak ayakta değilse tüketici de iş yapamaz.

State transfer bu geri çağrıyı ortadan kaldırır. Karşılığında veri **kopyalanır** ve kaynak değişince kopya **bayatlar (stale)**. Event sourcing ise zengin bir denetim izi ve geçmişe dönüp bakabilme imkânı verir. Bedeli şema evriminin yükü ve olayları yeniden oynatmanın maliyetidir.

Bir uyarı: olayları yalnız denetim amacıyla bir kenara yazıp güncel durumu yine bir tablodan okuyan sistem event sourcing yapmıyordur. Ölçüt şudur: tek gerçeklik kaynağı (source of truth) hangisidir?

### Mülakatta ne sorulur?

- Verilen olay örneği hangi stildir ve gizli bedeli nedir?
- "Olay yayınladık, gevşek bağlıyız" iddiasına şerhin ne? (Zamansal bağ çözülür, şema/anlam bağı kalır.)
- Koreografi ile orkestrasyonun farkı nedir?
- Çalışan tüketicileri bozmadan şemaya alan nasıl eklenir?
- Olayları yalnız denetim için yazmak neden event sourcing sayılmaz?

### Tek cümlede

Olay güdümlü mimari servisleri **zamanda** ayırır ama **sözleşmede** ayırmaz; stilini ve koordinasyon biçimini bilinçli seçmezseniz kazandığınızı sandığınız gevşekliği şema kırılmalarıyla geri ödersiniz.

# eda-1 | Orta
## Soru
Sipariş servisi `SiparisOlusturuldu` olayını yayınlıyor; olay yalnızca `siparisId` içeriyor ve tüketiciler detay için tekrar Sipariş servisini HTTP ile çağırıyor. Bu hangi stildir ve temel dezavantajı nedir?

```java
// Yayınlanan olay
record SiparisOlusturuldu(String siparisId) {}
// Tüketici: detay için kaynağa geri döner
var detay = siparisClient.getir(olay.siparisId());
```
## A
Event-carried state transfer; olay tam durumu taşıdığı için tüketici kaynağa hiç dönmez ve bağımsızdır
🤖 State transfer olsaydı olay tam durumu taşırdı; burada yalnız `id` var, bu yüzden tüketici bağımsız değil.
## B
Event notification; tüketici detay için kaynağa döner, bu da gizli runtime coupling doğurur
🤖 Doğru: sadece kimlik taşıyan olay klasik notification'dır; geri-çağrı Sipariş servisine gizli bağımlılık kurar.
## C
Event sourcing; durum olay akışında saklandığı için tablo yerine günlük okunur
🤖 Event sourcing durumu olay akışında saklar; burada olay yalnız bir tetikleyici, kaynak-of-truth değil.
## D
Orkestrasyon; merkezi bir koordinatör adımları sırayla çağırıp akışı yönetir
🤖 Orkestrasyon akış yönetimi eksenidir; buradaki soru payload stili, koordinasyon değil.
## Doğru
B
## Açıklama
Yalnız bir kimlik taşıyıp tüketiciyi detay için kaynağa döndüren stil **event notification**'dır. Avantajı küçük payload ve düşük yayın maliyetidir; gizli bedeli ise **runtime coupling**: tüketici işini bitirmek için Sipariş servisinin o an ayakta ve hızlı olmasına bağımlıdır. Bu bağımlılık "olay yayınladık, gevşek bağlıyız" yanılsamasının altında saklanır. State transfer'e (A) geçmek bu geri-çağrıyı ortadan kaldırır ama veri kopyası doğurur; event sourcing (C) ve orkestrasyon (D) farklı eksenlerin kavramlarıdır.

# eda-2 | Orta
## Soru
Bir ekip olayların içine ilgili varlığın **tam durumunu** koymaya karar verdi (tüketiciler kaynağı hiç çağırmasın diye). Bu yaklaşım hakkında hangisi doğrudur?
## A
Event notification'dır; payload küçük kaldığı için coupling her koşulda en aza iner
🤖 Tam durum taşımak notification değil state transfer'dir; ayrıca "her zaman en iyi" yoktur, ödünleşme vardır.
## B
Yalnız event sourcing kullanan sistemlerde mümkündür; event store olmadan yapılamaz
🤖 State transfer, event sourcing'den bağımsızdır; olayın payload'ını zenginleştirmek için event store gerekmez.
## C
Event-carried state transfer'dir; otonomi kazanılır ama veri kopyalanır ve bayatlayabilir
🤖 Doğru: otonomi kazanılır, karşılığında kopya veri ve staleness riski satın alınır.
## D
Tam durum taşındığı için tüketicilerin tuttuğu kopya hiçbir zaman bayat (stale) hale gelmez
🤖 Tersine: tüketici kendi kopyasını tutar; kaynak sonradan değişirse tüketicideki kopya bayatlar.
## Doğru
C
## Açıklama
Olayın içine ihtiyaç duyulan tam veriyi koymak **event-carried state transfer**'dir. Kazanç nettir: tüketici kaynağa geri-çağrı yapmadan çalışır, yani **runtime coupling** düşer ve kaynak servisin geçici kesintisi tüketiciyi durdurmaz. Bedeli de nettir: her tüketici kendi yerel **kopyasını** tutar (veri çoğaltma) ve kaynaktaki sonraki değişiklikler yayılana dek bu kopya **bayat** (stale) olabilir. Bu ödünleşmeyi görmek senior'ın işidir; "coupling her koşulda en aza iner" (A) veya "kopya asla bayatlamaz" (D) diyen basitleştirmeler yanlıştır. State transfer, event sourcing'i (B) gerektirmez.

# eda-3 | İleri
## Soru
Bir ekip "event sourcing yapıyoruz" diyor; ama güncel durumu bir durum tablosunda tutuyor, olayları yalnızca **denetim (audit)** kaydı olarak yan tarafa yazıyor. Bu neden event sourcing değildir?
## A
Event sourcing'de kaynak-of-truth olay günlüğüdür; durum replay/fold ile ondan türetilir
🤖 Doğru: ölçüt olay yazmak değil, durumun olaylardan türetilmesi ve günlüğün tek gerçeklik olmasıdır.
## B
Fark yoktur; olayları bir yere yazan her sistem tanım gereği event sourcing yapıyor sayılır
🤖 Olay yazmak tek başına yetmez; durum hâlâ tablodan okunuyorsa kaynak-of-truth olaylar değildir.
## C
Olaylar broker'a yayınlanmadığı için sayılmaz; asıl ölçüt olayların dışarı gönderilmesidir
🤖 Yayınlamak bir entegrasyon kararıdır; event sourcing depolama kararıdır, ikisi aynı şey değildir.
## D
Sayılır; tek eksik, olay kayıtlarının ayrı bir veritabanında tutulmamasıdır
🤖 Olayların hangi veritabanında durduğu belirleyici değildir; belirleyici olan durumun nereden türetildiğidir.
## Doğru
A
## Açıklama
Event sourcing'in ölçütü "olay yazıyor muyuz" değil, "**güncel durumu nereden alıyoruz**"dur. Gerçek event sourcing'de durum hiçbir yerde saklanmaz: varlığın **değişmez olay akışı** baştan (performans için genelde son **snapshot**'tan) yeniden oynatılıp katlanarak (replay/fold) hesaplanır ve **olay günlüğü tek gerçeklik kaynağıdır**. Sorudaki sistemde durum tablosu güncelleniyor, olaylar ise yalnız yan bir denetim kaydı; tablo ile olaylar çelişirse doğru kabul edilen tablodur — bu klasik CRUD'dur (bu kullanım kötü değildir, sadece adı event sourcing değildir). Olayı yayınlamak (C) veya olayları ayrı veritabanına koymak (D) bu ölçütü değiştirmez; "olay yazan her sistem event sourcing'tir" (B) ise yaygın bir gevşeklik.

# eda-4 | İleri
## Soru
Olay güdümlü bir akışta **koreografi** (choreography) ile **orkestrasyon** (orchestration) arasındaki temel fark nedir?
## A
Koreografide merkezi bir beyin adımları tek tek çağırır ve akışın sırasını baştan sona yönetir
🤖 Bu orkestrasyonun tanımı; koreografide merkezi yönetici yoktur.
## B
Orkestrasyonda hiçbir merkezi bileşen yoktur; her servis olaya bakıp kendi kararını verir
🤖 Bu koreografinin tanımı; orkestrasyonda tam da merkezi bir orkestratör vardır.
## C
İkisi eşanlamlıdır; yalnız farklı ekollerin aynı şey için kullandığı iki isimdir
🤖 Farklı koordinasyon modelleridir; görünürlük, coupling ve hata yönetimi açısından ayrışırlar.
## D
Koreografide akış olay zincirinden doğar; orkestrasyonda merkezi orkestratör adımları yönetir
🤖 Doğru: fark, akışı yöneten merkezi bir bileşenin olup olmamasıdır.
## Doğru
D
## Açıklama
İki koordinasyon modeli ayrımı: **Koreografi**de merkezi bir yönetici yoktur; her servis gelen olaya tepki verip yeni olaylar yayınlar, akış bu tepkilerin zincirinden **ortaya çıkar** (emergent). Gevşek bağlıdır ama uçtan uca akışı görmek ve hata ayıklamak zorlaşır. **Orkestrasyon**da ise merkezi bir **orkestratör** adımları açıkça çağırır ve sırayı yönetir; akış görünür olur, hata yönetimi merkezîleşir, karşılığında orkestratör bir bağımlılık ve olası darboğaz noktasına dönüşür. A ve B tanımları ters çevirir; C ise iki modeli aynı sanmaktır.

# eda-5 | İleri
## Soru
Yayınlanan bir olayın şemasına, **çalışan eski tüketicileri bozmadan** yeni bir alan eklemek istiyorsun. Hangi yaklaşım geriye dönük uyumluluğu (backward compatibility) korur?
## A
Kullanılmayan eski alanları hemen sil; şema temizlensin, kimse okumadığı için kimse etkilenmez
🤖 Alan silmek eski tüketicileri kırar (bekledikleri alanı bulamazlar); bu geriye dönük uyumlu değildir.
## B
Mevcut bir alanın tipini değiştir (ör. `int` yerine `String`); anlam korunduğu için güvenlidir
🤖 Var olan alanın tipini/anlamını değiştirmek en yıkıcı değişikliktir; tüm mevcut tüketicileri bozar.
## C
Yeni alanı isteğe bağlı ve yalnız ekleyerek getir; eski tüketici bilmediği alanı yok saysın
🤖 Doğru: additive + optional değişiklik eski ve yeni tüketicilerin bir arada yaşamasını sağlar.
## D
Üretici ile tüm tüketicileri aynı anda ve tek seferde deploy et; uyumsuzluk penceresi kalmasın
🤖 Dağıtık sistemde eşzamanlı big-bang deploy pratikte imkânsız ve kırılgandır; asıl amaç bağımsız evrimdir.
## Doğru
C
## Açıklama
Olay şeması evrimi (schema evolution) senior mikroservis işinin kalbindedir, çünkü üreticiyle tüketiciler **bağımsız** deploy edilir; ikisi aynı anda güncellenemez. Güvenli değişiklik **additive** ve **optional**'dır: yeni alan ekle, eski alanı kaldırma, var olanın anlamını veya tipini değiştirme (Avro/Protobuf gibi formatlar tam da bunun için kurallar koyar). Böylece eski tüketiciler bilmedikleri alanı **yok sayar** (forward compatibility) ve yeni tüketiciler alanın yokluğuna dayanmaz (backward compatibility). Alan silmek (A) veya tip değiştirmek (B) kırıcıdır; herkesi aynı anda deploy etmek (D) dağıtık gerçeklikle çelişir.

# eda-6 | Uzman
## Soru
"Olay güdümlü mimari servisleri gevşek bağlar" denir. Broker araya girdiği için **temporal** (zamansal) bağımlılık çözülse bile, olaylarda sıkça **gözden kaçan** hangi coupling biçimi kalır?
## A
Hiçbir coupling kalmaz; broker araya girdiği için olaylar tam bağımsızlık sağlar
🤖 "Sıfır coupling" bir efsanedir; olayın payload'ı üzerinden bağımlılık her zaman vardır.
## B
Şema/anlam coupling'i: tüketici payload'ın yapısına ve anlamına bağımlı kalmayı sürdürür
🤖 Doğru: temporal decoupling coupling'i yok etmez, yalnız yerini şema/anlam bağımlılığına kaydırır.
## C
Yalnız paylaşılan veritabanı şeması üzerinden coupling; olay payload'ı bir sözleşme sayılmaz
🤖 Olay payload'ı tam da bir sözleşmedir; asıl gizli coupling oradadır, paylaşılan DB'de değil.
## D
Coupling yalnız senkron HTTP çağrılarında doğar; asenkron mesajlaşmada kavramsal olarak imkânsızdır
🤖 Asenkronluk temporal coupling'i azaltır ama içerik/sözleşme bağımlılığını kaldırmaz.
## Doğru
B
## Açıklama
Broker araya girince üretici ile tüketicinin **aynı anda ayakta olma** zorunluluğu (temporal coupling) çözülür ve "gevşek bağladık" hissi doğar. Ama coupling yok olmaz, **yer değiştirir**: tüketiciler olayın **payload yapısına ve anlamına** (semantic + schema coupling) bağımlı kalır. Üretici bir alanı yeniden adlandırır, bölerse ya da anlamını değiştirirse tüketiciler sessizce kırılır. Bu yüzden olay şeması bir **yayınlanmış sözleşmedir** (published contract) ve schema registry, sürümleme ve uyumluluk kurallarıyla, tıpkı bir REST API gibi yönetilir. "Sıfır coupling" (A), coupling'in yalnız veritabanında (C) veya yalnız senkron çağrıda (D) olduğu iddiaları senior seviyede yaygın yanılgılardır.
