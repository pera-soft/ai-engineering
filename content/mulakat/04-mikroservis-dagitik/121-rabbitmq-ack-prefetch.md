---
slug: rabbitmq-ack-prefetch
baslik: "RabbitMQ Ack, Nack ve Prefetch: Teslim Garantisi, Fair Dispatch ve Prefetch Tuzağı"
kademe: İleri
sira: 121
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

Kargocu kapına gelir, kutuyu uzatır ve **imza** ister. İmzayı attığın anda kargo şirketinin defterinde "teslim edildi" yazar; artık kutu senin sorumluluğundadır. Peki kargocu kutuyu kapının önüne bırakıp imza almadan gitseydi ve kutu kaybolsaydı? Kimse bilemezdi.

Mesajlaşmada da aynı sorun var: **aracı (broker)** bir mesajı tüketiciye gönderdi; mesajı kuyruktan ne zaman silmeli? Hemen silerse ve tüketici o sırada çökerse iş yapılmadan mesaj yok olur. İşte **onay (ack, acknowledgement)** bu imzadır: tüketici "işi gerçekten bitirdim" der, broker ancak o zaman mesajı siler.

İkinci soru da şu: broker bir tüketiciye aynı anda kaç kutu bırakabilir? Bunu **tüketiciye peşin verilen mesaj sayısı (prefetch)** ayarı belirler. Bu ayarın yanlış verilmesi, RabbitMQ'nun en sık görülen üretim sorunudur.

### Temel kavramlar

- **Onay / ack**: tüketicinin "bu mesajı başarıyla işledim" bildirimi (`basic.ack`). Broker mesajı bunun üzerine siler.
- **Otomatik ack (autoack / no-ack)**: broker mesajı **teslim ettiği anda** onaylanmış sayar. Tüketici işlerken çökerse mesaj **kaybolur** → **at-most-once** (en fazla bir kez).
- **Manuel ack**: onay iş bittikten sonra elle gönderilir. Tüketici önce çökerse mesaj **yeniden teslim** edilir → **at-least-once** (en az bir kez).
- **Red (nack / reject)**: "bu mesajı işleyemedim" bildirimi. `basic.nack` birden çok mesajı, `basic.reject` tek mesajı kapsar.
- **`requeue` parametresi**: `requeue=true` mesajı kuyruğa **geri koyar** (yeniden denenir). `requeue=false` ise mesajı **düşürür**; kuyruğa tanımlıysa **ölü mektup dağıtıcısına (DLX, dead-letter exchange)** yollar.
- **Unacked (onaylanmamış) mesaj**: teslim edilmiş ama henüz ack/nack almamış mesaj. Kuyrukta değildir, ama silinmemiştir de; askıdadır.
- **`redelivered` bayrağı**: bir mesaj ikinci kez teslim edilirse üstünde `redelivered=true` yazar.
- **Zehirli mesaj (poison message)**: her denemede aynı hatayı veren, `requeue=true` ile sonsuz döngüye giren mesaj.
- **Peşin verilen mesaj sayısı (prefetch / QoS)**: bir tüketiciye **onay (ack) beklenmeden** gönderilebilecek azami mesaj sayısı.
- **Adil dağıtım (fair dispatch)**: işini bitiren tüketicinin bir sonraki mesajı almasını sağlayan denge.

### Nasıl çalışır?

Prefetch bir **tavan** değeridir. Anlamı şudur: "bu tüketicinin elinde aynı anda en çok şu kadar onaylanmamış mesaj olabilir."

Sınırsız ya da çok büyük bırakılırsa broker mesajları **sırayla, teker teker (round-robin)** önden dağıtır. Böylece **hızlı** tüketiciye de **yavaş** tüketiciye de aynı miktarda yükler. Uzun süren işler yavaş tüketicinin önünde birikir ve o boğulur. Kısa işleri çabuk bitiren hızlı tüketici ise **boşta** bekler.

Buna **açlık (starvation)** denir. Dikkat: mesajların sırası bozulmaz ve tüketicilerden biri "hiç mesaj almamış" da olmaz. Sorun tek başına **yük dengesizliğidir**.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Prefetch ve fair dispatch: düşük prefetch yükü dengeler">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Prefetch = uçuştaki (ack beklenen) mesaj tavanı</text>
  <text x="165" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#C0562B" font-family="system-ui">prefetch = ∞ (round-robin)</text>
  <rect x="40" y="64" width="110" height="34" rx="7" fill="#FBE9E7" stroke="#C0562B" stroke-width="1.5"/>
  <text x="95" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Yavaş: 8 mesaj</text>
  <rect x="180" y="64" width="110" height="34" rx="7" fill="#FBE9E7" stroke="#C0562B" stroke-width="1.5"/>
  <text x="235" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hızlı: boşta</text>
  <text x="165" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yük dengesiz: biri boğulur, biri aç kalır</text>
  <text x="495" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">prefetch = küçük (fair)</text>
  <rect x="370" y="64" width="110" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="425" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Yavaş: 1 işler</text>
  <rect x="510" y="64" width="110" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="565" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hızlı: akışta</text>
  <text x="495" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">boşalan işçi bir sonrakini çeker</text>
  <line x1="20" y1="150" x2="640" y2="150" stroke="#D0D8DE" stroke-width="1"/>
  <text x="330" y="176" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">TUZAK: prefetch=∞ → hızlı işçi tüm mesajları kapar, yavaş işçi boğulur (açlık).</text>
  <text x="330" y="198" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ayar: iş süresine göre küçük başla; ölç, sonra artır. Tek doğru değer yoktur.</text>
</svg>
```

Çözüm prefetch'i **küçük** tutmaktır. Ama küçüklüğün de bedeli vardır: `prefetch=1` her mesaj için bir ağ gidiş-dönüşü ekler ve toplam **iş hacmini (throughput)** düşürür. Doğru değer tek bir sihirli sayı değildir. İş süresine göre küçük başlanır (tipik olarak 1–20) ve **ölçerek** ayarlanır.

Önemli bir ayrıntı var: prefetch, **onay bekleyen** mesajları sayar. **Otomatik onay (autoack)** kipinde ise bekleyen onay hiç oluşmaz. Sayaç dolmadığı için prefetch **fiilen uygulanmaz**. İkisi birlikte tanımlanabilir, broker hata vermez; prefetch sadece etkisiz kalır. Bu yüzden akış kontrolü ancak manuel ack ile anlamlıdır.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teslim edilen mesajın ack nack ile üç olası sonu">
  <defs>
    <marker id="rack-fl" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Teslim edilen mesajın üç olası sonu (manuel ack)</text>
  <rect x="30" y="82" width="140" height="56" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="100" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">unacked</text>
  <text x="100" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işleniyor</text>
  <rect x="470" y="20" width="160" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="550" y="40" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">kuyruktan SİLİNİR</text>
  <text x="550" y="56" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">basic.ack</text>
  <rect x="470" y="87" width="160" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="550" y="107" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">kuyruğa GERİ</text>
  <text x="550" y="123" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">nack/reject requeue=true</text>
  <rect x="470" y="154" width="160" height="46" rx="9" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="550" y="174" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">DLX'e / düşer</text>
  <text x="550" y="190" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">nack/reject requeue=false</text>
  <line x1="170" y1="100" x2="466" y2="44" stroke="#25804C" stroke-width="2" marker-end="url(#rack-fl)"/>
  <line x1="170" y1="110" x2="466" y2="110" stroke="#186399" stroke-width="2" marker-end="url(#rack-fl)"/>
  <line x1="170" y1="120" x2="466" y2="176" stroke="#8A2E2E" stroke-width="2" marker-end="url(#rack-fl)"/>
  <text x="330" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hiç ack/nack yok → mesaj "unacked" asılı kalır; kuyruğa ancak tüketici/kanal düşünce döner.</text>
</svg>
```

Teslim edilmiş bir mesajın üç sonu vardır: `ack` → silinir; `nack/reject` + `requeue=true` → kuyruğa geri döner; `nack/reject` + `requeue=false` → düşer ya da DLX'e gider.

Hiçbir şey gönderilmezse mesaj **onaylanmamış (unacked)** olarak askıda kalır. Kuyruğa ancak tüketici veya kanal düştüğünde geri döner. Bunu bir yeniden deneme yöntemi olarak kullanma; öyle tasarlanmamıştır.

```svg
<svg viewBox="0 0 660 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zehirli mesaj requeue döngüsü ve redelivered bayrağıyla DLQ'ya alınması">
  <defs>
    <marker id="rack-ps" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#8A2E2E"/>
    </marker>
    <marker id="rack-ps2" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Zehirli mesaj (poison): sonsuz döngüyü sayaç + DLQ kırar</text>
  <rect x="60" y="70" width="150" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="135" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kuyruk</text>
  <text x="135" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">hatalı mesaj</text>
  <rect x="330" y="70" width="150" height="58" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="405" y="92" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Tüketici</text>
  <text x="405" y="110" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yine hata verir</text>
  <line x1="210" y1="90" x2="326" y2="90" stroke="#186399" stroke-width="2" marker-end="url(#rack-ps)"/>
  <text x="268" y="82" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">teslim</text>
  <path d="M405 128 C405 168, 135 168, 135 130" fill="none" stroke="#8A2E2E" stroke-width="2" marker-end="url(#rack-ps)"/>
  <text x="270" y="170" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">nack requeue=true → redelivered=true (döngü)</text>
  <rect x="510" y="70" width="130" height="58" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="575" y="92" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">DLQ</text>
  <text x="575" y="110" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">incelemeye alınır</text>
  <line x1="480" y1="90" x2="506" y2="90" stroke="#25804C" stroke-width="2" marker-end="url(#rack-ps2)"/>
  <text x="330" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sayaç eşiği aşılınca requeue=false ile reddet → DLQ. redelivered tek başına sayı tutmaz, sayaç gerekir.</text>
</svg>
```

`requeue=true`'yu koşulsuz kullanmanın tuzağı burada ortaya çıkar: kalıcı hatalı bir mesaj sonsuza dek dönüp durur. Tespit için mesajın **`redelivered`** bayrağına bakılır. Ama bu bayrak yalnızca "daha önce teslim edildi mi" der; kaç kez denendiğini saymaz. Sağlam çözüm bir **yeniden-deneme sayacı** tutup eşik aşılınca mesajı `requeue=false` ile reddetmek ve **DLQ**'ya almaktır. Mesajın `content-type`, `priority` gibi alanları ya da kuyruğun `x-max-length` ayarı bu bilgiyi taşımaz.

### Mülakatta ne sorulur?

- "autoack'te tüketici çökerse?" — mesaj kaybolur; geri dönmez, DLX'e gitmez, ack geri alınamaz.
- "Yeniden denenmesini nasıl isterim?" — `nack`/`reject` + `requeue=true`.
- "Poison message'ı nasıl kırarsın?" — `redelivered` + sayaç, eşikte `requeue=false` → DLQ. Spring AMQP'de `RepublishMessageRecoverer`.
- "Prefetch sınırsızsa ne olur?" — açlık ve dengesiz yük.
- "Fair dispatch nasıl kurulur?" — küçük prefetch + ölçüm.
- "autoack ile prefetch birlikte?" — prefetch etkisiz kalır.

### Tek cümlede

Onay (ack) teslim garantisini belirler, prefetch ise dağıtım adaletini. Manuel onay olmadan mesaj kaybolur; küçük prefetch olmadan yük dengesizleşir.

# rack-1 | Temel
## Soru
Bir tüketici mesajı **otomatik ack (autoack)** modunda alıyor. Mesajı işlerken tüketici süreci çöküyor. Mesaja ne olur?
## A
Mesaj kaybolur; autoack modunda mesaj teslim anında onaylanıp kuyruktan silinmiştir
🤖 Doğru: autoack teslimde onaylar, işleme başarısı beklenmez; çökme veri kaybına yol açar (at-most-once).
## B
Mesaj otomatik olarak kuyruğa geri döner ve başka bir tüketiciye yeniden teslim edilir
🤖 Bu manuel ack davranışıdır; autoack'te mesaj çoktan silinmiştir, geri dönmez.
## C
Mesaj ölü mektup kuyruğuna (DLQ) taşınıp incelenmek üzere orada bekletilir
🤖 autoack'te reddetme/DLX aşaması yoktur; mesaj teslim anında onaylanıp gitmiştir.
## D
Broker çökmeyi algılar, verilen ack'i geri alıp mesajı kuyrukta yeniden oluşturur
🤖 Ack geri alınamaz; teslimde verilen onay kalıcıdır, çökme fark etmez.
## Doğru
A
## Açıklama
**Otomatik ack (autoack / no-ack)** modunda broker mesajı tüketiciye **ulaştırdığı anda** onaylanmış sayar ve kuyruktan siler — tüketicinin işi bitirip bitirmediğine bakmaz. Tüketici mesajı işlerken çökerse mesaj çoktan silinmiş olduğu için **kalıcı olarak kaybolur** (at-most-once semantiği). Yeniden teslim (B), DLQ'ya taşıma (C) veya ack geri alma (D) gibi kurtarma yollarının hiçbiri devreye girmez. Güvenilir teslim için **manuel ack** gerekir: mesaj yalnızca iş başarıyla bittikten sonra `basic.ack` ile onaylanır; tüketici ondan önce çökerse mesaj başka tüketiciye yeniden teslim edilir.

# rack-2 | Orta
## Soru
Manuel ack modunda bir tüketici, mesajı işleyemediğini bildirip mesajın **kuyruğa geri konup yeniden denenmesini** istiyor. Hangi komut/parametre doğrudur?
## A
`basic.ack` gönderip broker'ın mesajı kendiliğinden yeniden teslim etmesini beklemek
🤖 Ack başarı bildirir ve mesajı siler; yeniden deneme sağlamaz.
## B
Hiçbir şey göndermeyip bağlantıyı açık tutmak; broker bir süre sonra mesajı geri koyar
🤖 Ack/nack gönderilmezse mesaj "unacked" olarak asılı kalır; kuyruğa ancak tüketici/kanal düşünce döner.
## C
`basic.nack` (veya `basic.reject`) komutunu `requeue=true` ile göndermek
🤖 Doğru: nack/reject + requeue=true mesajı kuyruğa geri koyup yeniden denenmesini sağlar.
## D
`basic.reject` komutunu `requeue=false` parametresiyle göndermek
🤖 requeue=false mesajı düşürür veya DLX'e yollar; yeniden denemez.
## Doğru
C
## Açıklama
Başarısızlığı bildirip **yeniden deneme** istemek için `basic.nack` veya `basic.reject` kullanılır ve **`requeue=true`** verilir — mesaj kuyruğa geri konur ve yeniden teslim edilir (bu teslim `redelivered=true` bayrağı taşır). `requeue=false` (D) ise mesajı düşürür ya da kuyrukta DLX tanımlıysa oraya gönderir; yeniden denemez. `basic.ack` (A) başarı bildirimi olduğu için mesajı siler. Hiçbir şey göndermemek (B) mesajı "onaylanmamış" (unacked) durumda tutar; kuyruğa geri konması için tüketici/kanal düşmelidir — kasıtlı yeniden deneme için doğru araç değildir. Uyarı: `requeue=true`'yu koşulsuz kullanmak, kalıcı hatalı mesajlarda sonsuz döngü (poison message) üretir.

# rack-3 | Orta
## Soru
Sürekli aynı hatayı veren bir mesaj `requeue=true` ile tekrar tekrar kuyruğa dönüyor ve tüketiciyi kilitliyor (poison message). Bu durumu tespit edip kırmak için mesajın hangi özelliğine bakılır?
## A
Mesajın `content-type` başlığına (gövdenin biçimini bildiren alan)
🤖 content-type gövdenin biçimini belirtir; yeniden teslim sayısı/durumu ile ilgisi yoktur.
## B
Mesajın `redelivered` bayrağına ve buna eşlik eden bir yeniden-deneme sayacına
🤖 Doğru: redelivered=true mesajın daha önce teslim edildiğini gösterir; sayaçla birlikte poison tespiti yapılır.
## C
Mesajın `priority` alanına (öncelik kuyruğunda sırayı belirleyen değer)
🤖 priority sıralama önceliğidir; yeniden teslim geçmişini taşımaz.
## D
Kuyruğun `x-max-length` ayarına (kuyrukta tutulabilecek azami mesaj sayısı)
🤖 x-max-length kuyruk uzunluk sınırıdır; bireysel mesajın kaç kez denendiğini söylemez.
## Doğru
B
## Açıklama
Yeniden teslim edilen bir mesaj **`redelivered=true`** bayrağı taşır — bu, "bu mesaj daha önce en az bir kez teslim edildi" demektir. Ancak `redelivered` yalnızca ikili bir işarettir, kaç kez denendiğini saymaz; sağlam poison-message koruması için genelde bir **yeniden-deneme sayacı** (mesaj başlığında veya DLX üzerinden bir sayaç) tutulur ve eşik aşılınca mesaj `requeue=false` ile reddedilip **DLQ**'ya alınır. `content-type` (A), `priority` (C) ve kuyruk düzeyindeki `x-max-length` (D) teslim geçmişiyle ilgili bilgi taşımaz. En temiz desen: sınırlı yeniden deneme + DLQ; Spring AMQP'de bu iş `RepublishMessageRecoverer` ile yapılır.

# rack-4 | İleri
## Soru
İşlem süreleri çok değişken (bazı mesajlar 10ms, bazıları 30s) bir iş kuyruğunda (work queue) birden çok tüketici var. Prefetch **sınırsız/çok yüksek** bırakılırsa en olası sorun nedir? (Prefetch tuzağı)
## A
Broker bellek yetersizliğinden anında çöker ve kuyruklardaki mesajları kaybeder
🤖 Yüksek prefetch bellek baskısı yaratabilir ama tipik ve öncelikli sorun broker çökmesi değil, yük dengesizliğidir.
## B
Tüm mesajlar tek bir tüketiciye gider; diğer tüketiciler hiçbir mesaj almaz
🤖 Round-robin dağıtım hâlâ çalışır; sorun "hiç almamak" değil, önden kapılan mesajların yavaş işçide birikmesidir.
## C
Mesajlar kuyruk içi sıralamasını kaybeder ve tüketicilere rastgele sırada teslim edilir
🤖 Prefetch sıralamayı bozmaz; tek kuyruk-tek tüketici sırası korunur, sorun sıralama değildir.
## D
Yavaş tüketicilerin önünde mesajlar birikirken hızlı tüketiciler boşta kalır (açlık)
🤖 Doğru: yüksek prefetch fair dispatch'i bozar; yük dengesizleşir ve toplam throughput düşer.
## Doğru
D
## Açıklama
**Prefetch tuzağı**: prefetch, bir tüketiciye ack beklenmeden gönderilebilecek mesaj sayısını sınırlar. Sınırsız/çok yüksek olduğunda broker round-robin ile mesajları önden dağıtır ve **hızlı** ile **yavaş** tüketiciye aynı miktarda yükler. Uzun süren mesajlar yavaş tüketicinin önünde birikir, o boğulurken kısa mesajları bitiren hızlı tüketici **boşta** kalır — bu **açlık (starvation)** ve dengesiz yük demektir; toplam throughput düşer. Çözüm **fair dispatch**: prefetch'i işleme süresine göre küçük tutmak, böylece işini bitiren tüketici bir sonraki mesajı çeker. Ana sorun broker çökmesi (A), "hiç mesaj almama" (B) veya sıra bozulması (C) değil, yük dengesizliğidir.

# rack-5 | İleri
## Soru
Yukarıdaki değişken-süreli iş kuyruğunda **fair dispatch** (adil dağıtım) sağlamak için en uygun prefetch stratejisi nedir?
## A
prefetch'i olabildiğince yüksek tutmak (ör. 10.000) — ne kadar çok önden gönderilirse o kadar iyi
🤖 Yüksek prefetch tam da açlığı ve dengesizliği doğurur; fair dispatch'in tersidir.
## B
prefetch = 0 (sınırsız) bırakmak; RabbitMQ yük dengesini kendi kendine ayarlar
🤖 0 sınırsız demektir ve broker dengeyi kendi ayarlamaz; bu en kötü durumdur.
## C
prefetch'i işleme süresine göre küçük başlatıp (ör. 1–20) ölçüm sonrası ayarlamak
🤖 Doğru: düşük prefetch fair dispatch sağlar; kesin değer ölçümle bulunur, tek sihirli sayı yoktur.
## D
Her mesajı ayrı bir kuyruğa koyup her kuyruğa yalnızca tek bir tüketici bağlamak
🤖 Aşırı karmaşık ve ölçeklenmez bir çözüm; problem prefetch ayarıyla doğrudan çözülür.
## Doğru
C
## Açıklama
**Fair dispatch** için prefetch küçük tutulur: tüketici o an ancak sınırlı sayıda mesajı üstlenir, birini bitirip ack verince broker bir sonrakini gönderir — böylece boşta kalan işçi işi çeker, yavaş işçinin önünde yığılma olmaz. Değişken süreli işlerde tipik başlangıç 1–20 civarıdır; ama **tek doğru değer yoktur**: çok küçük (=1) prefetch her mesaj için ağ gidiş-dönüşü ekleyip throughput'u düşürür, çok büyük prefetch açlık yaratır. Doğru yaklaşım küçük başlayıp **ölçerek** ayarlamaktır. Yüksek/sınırsız prefetch (A, B) tuzağın kendisidir; mesaj başına ayrı kuyruk (D) gereksiz karmaşıklıktır.

# rack-6 | İleri
## Soru
`autoack` (otomatik onay) ile `prefetch` ayarı arasındaki ilişki hakkında hangisi **doğrudur**?
## A
Prefetch yalnızca manuel ack modunda anlamlıdır; autoack'te sınır fiilen uygulanmaz
🤖 Doğru: prefetch "ack beklenen uçuştaki mesaj" tavanıdır; autoack'te bekleyen ack olmadığından sınır etkisizdir.
## B
Prefetch autoack modunda da mesaj akışını tıpkı manuel ack'teki gibi sınırlandırır
🤖 autoack'te onay beklenmediği için prefetch sayacı işlemez; akış tavanı uygulanmaz.
## C
autoack, prefetch değerini otomatik olarak 1'e sabitleyip mesaj akışını yavaşlatır
🤖 autoack prefetch'i 1'e sabitlemez; tam tersine prefetch mekanizması devre dışı kalır.
## D
autoack ve prefetch birlikte tanımlanamaz; broker kanalı kapatıp hata döndürür
🤖 İkisi birlikte tanımlanabilir; hata olmaz, yalnızca prefetch etkisiz kalır.
## Doğru
A
## Açıklama
**Prefetch (basic.qos)**, "bir tüketiciye **ack beklenmeden** gönderilebilecek mesaj" tavanıdır; yani onay bekleyen ("unacked") mesajları sayar. **autoack** modunda mesaj teslim anında zaten onaylanır, dolayısıyla **bekleyen ack yoktur** — prefetch sayacı hiç dolmaz ve akış sınırı **fiilen uygulanmaz**; broker mesajları olabildiğince hızlı iter (bu, tüketiciyi bunaltma ve bellek baskısı riski taşır). Bu yüzden prefetch ile akış kontrolü **yalnızca manuel ack** ile anlamlıdır (A doğru, B yanlış). autoack prefetch'i 1'e sabitlemez (C) ve ikisi birlikte tanımlanınca hata da vermez (D); prefetch sadece etkisiz kalır.
