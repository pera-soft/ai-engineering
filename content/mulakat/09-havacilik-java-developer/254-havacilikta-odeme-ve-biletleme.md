---
slug: havacilikta-odeme-ve-biletleme
baslik: "Havacılıkta Ödeme ve Biletleme: BSP Takası, Para Çekildi Bilet Kesilemedi ve Çoklu Para Birimi"
kademe: İleri
sira: 254
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir yayınevi düşün: dergisini kendi web sitesinden de satıyor, ülkedeki üç bin bayiden de. Web satışının parası kart ödemesiyle birkaç gün içinde hesabına geçiyor; bayilerin parasını ise yayınevi tek tek üç bin dükkândan toplamıyor — arada bir **dağıtımcı** var, satışları raporluyor, dönem sonunda netleştirip yayınevine tek bir havale yapıyor. İki kanal, iki ayrı para akışı, iki ayrı mutabakat defteri.

Havayolu tam olarak bu yayınevidir. Kendi web ve mobil kanalından kartla satar. Dünyadaki binlerce seyahat acentesinden gelen satışın parası ise **IATA BSP** üzerinden gelir; dönemseldir ve netleştirilmiş haldedir.

Üstüne havacılığa özgü bir zorluk biner: satılan şey bir dergi gibi rafta hazır durmaz. Müşteri "öde" düğmesine bastığında bilet henüz yoktur. Biletleme (ticketing) **başka bir sistemde** ve sonradan olur; bazen de hiç olmaz.

`250` numaralı konu kart ödemesinin aktörlerini ve auth/capture/void durum makinesini kurdu. `248` ise rezervasyon sagasının envanter tarafını anlattı. Burası aynı zincirin **para tarafıdır**. Üç soruyu cevaplar: para hangi kanaldan geldi, teslimat başarısız olduğunda para nasıl geri döner, tutar hangi para biriminde hangi kurla dondurulur.

### Temel kavramlar

- **IATA:** havayollarının uluslararası birliği; sektörün dağıtım, biletleme ve takas standartlarını koyar.
- **BSP (Billing and Settlement Plan):** IATA'nın acente satışları için işleyen takas sistemi; acente satışlarını toplar, dönemsel netleştirir, her havayoluna toplu ödeme yapar.
- **doğrudan satış kanalı:** havayolunun kendi web/mobil kanalı; ödeme kartla, PSP veya acquirer üzerinden alınır (`250`).
- **acente satışı kanalı:** GDS ve acenteler üzerinden satış (`247`); parası BSP takasıyla gelir, kart mutabakatıyla değil.
- **ticketing (biletleme):** ödemenin karşılığı olan e-biletin üretilmesi; PSS/host tarafında, ödeme servisinden **ayrı** çalışan adım (`247`, `221`).
- **ara durum:** ödemenin alındığı ama biletin henüz üretilmediği gerçeği taşıyan açık durum — `PAID_TICKET_PENDING` gibi.
- **kanıt zinciri (compelling evidence):** bir kart itirazına karşı hizmetin verildiğini gösteren belge kümesi; havacılıkta PNR, bilet kupon durumu ve check-in/biniş kaydı.
- **Money value object:** tutar ile para biriminin ayrılmaz biçimde birlikte taşındığı değer nesnesi.
- **ISO 4217 exponent:** her para biriminin kaç ondalık basamakla yazıldığını söyleyen standart alan; JPY 0, çoğu para 2, BHD 3.
- **DCC (Dynamic Currency Conversion):** müşteriye kendi para biriminde ödeme sunma; kur ve komisyon şeffaflığı zorunludur.

### Havayolunun iki gelir kanalı: kart mutabakatı ve BSP takası

Havayolu ödeme ekibine gelen ilk sürpriz, "ödeme" kelimesinin iki bambaşka şeyi anlatmasıdır.

**Doğrudan satışta** akış tanıdıktır. Müşteri kartını girer, otorizasyon alınır, bilet kesilir, capture edilir. Para issuer'dan şema ve acquirer üzerinden havayolunun hesabına dönemsel takasla geçer ve komisyon kesilir (`250`, ayrıntı `252`). Mutabakatın birimi **tek tek işlemdir**; her satırın bir otorizasyon kodu, bir PSP işlem kimliği ve bir sipariş numarası vardır.

**Acente satışında** ise havayolu müşterinin kartını hiç görmez. Acente bileti kendi kanalından satar ve satışı BSP'ye raporlar. BSP, dönem içindeki tüm acente-havayolu hareketlerini toplar, iadeleri ve komisyonları düşer, **netleştirir**. Dönem sonunda her havayoluna tek bir toplu ödeme yapar.

Mutabakatın birimi burada işlem değil, **dönemsel dosyadır**. Gelen kalemler kendi biletleriyle, kendi kupon durumlarıyla ve muhasebe kayıtlarıyla eşleştirilir. Havayolu ödeme ekiplerinin klasik işlerinden biri tam olarak budur: BSP dosyasını okuyup kalem kalem eşleştiren, eşleşmeyenleri istisna kuyruğuna düşüren bir toplu iş (`222`, `223`).

İki kanalı tek bir mutabakat akışında birleştirmeye çalışmak yaygın ve pahalı bir hatadır. Üç şey farklıdır. Anahtarlar farklıdır: bir tarafta PSP işlem kimliği, diğer tarafta acente kodu ve bilet numarası. Zamanlama farklıdır: T+1/T+2 karşısında dönemsel takas. Kesinti mantığı farklıdır: acquirer komisyonu karşısında acente komisyonu.

Doğru tasarım şudur: iki ayrı besleme ve iki ayrı eşleştirme kuralı, ama tek ortak **defter** modeli. Gelir kaydı biletin kendisine bağlanır; hangi kanaldan tahsil edildiği ise o kaydın bir özniteliğidir.

```svg
<svg viewBox="0 0 660 282" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Havayolunun iki gelir kanali: dogrudan kart satisi ve acente satisinin BSP takasi">
  <defs>
    <marker id="havode-kanal-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki gelir kanalı, iki ayrı mutabakat akışı</text>
  <text x="16" y="42" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">KANAL 1 — doğrudan satış (kart)</text>
  <rect x="16" y="50" width="150" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="91" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Yolcu · web/mobil</text>
  <text x="91" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kart ödemesi</text>
  <path d="M166 78 L192 78" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#havode-kanal-arw)"/>
  <rect x="196" y="50" width="180" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="286" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">PSP / acquirer (250)</text>
  <text x="286" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işlem bazlı · auth · capture</text>
  <path d="M376 78 L402 78" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#havode-kanal-arw)"/>
  <rect x="406" y="50" width="230" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="521" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Havayolu hesabı · T+1 / T+2</text>
  <text x="521" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">eşleştirme anahtarı: PSP işlem kimliği (252)</text>
  <text x="16" y="134" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">KANAL 2 — acente satışı (BSP takası)</text>
  <rect x="16" y="142" width="150" height="56" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="91" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Acente · GDS (247)</text>
  <text x="91" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">binlerce satış noktası</text>
  <path d="M166 170 L192 170" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#havode-kanal-arw)"/>
  <rect x="196" y="142" width="180" height="56" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="286" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">IATA BSP</text>
  <text x="286" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">raporla · netleştir · dönemsel</text>
  <path d="M376 170 L402 170" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#havode-kanal-arw)"/>
  <rect x="406" y="142" width="230" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="521" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Havayoluna toplu settle</text>
  <text x="521" y="182" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dosya işleyen toplu iş: 222, 223</text>
  <rect x="16" y="216" width="620" height="56" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="326" y="236" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">İki kanalı tek mutabakat akışında birleştirmek klasik hatadır</text>
  <text x="326" y="253" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">anahtarlar farklı · zamanlama farklı · kesinti mantığı farklı</text>
  <text x="326" y="267" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ortak olan defterdir: gelir bilete bağlanır, kanal o kaydın özniteliğidir</text>
</svg>
```

### Zincirin kırıldığı yer: para çekildi, bilet kesilemedi

Konunun kalbi burasıdır; havacılık ödeme mülakatlarında bu senaryo neredeyse kesin sorulur. Kurgusu şudur: capture başarılı olmuştur ve para müşterinin kartından çekilmiştir. Ardından biletleme isteği host/GDS tarafına gider (`221`, `247`) ve orada başarısız olur. Sebep birkaç türlü olabilir: envanter o anda tükenmiştir, ücret kuralları uyuşmamıştır, karşı sistem bakımdadır ya da hiç cevap dönmemiştir. Sonuçta müşterinin hesabından para gitmiştir ve elinde bilet yoktur.

Doğru tasarım dört parçadır ve dördü de aynı anda gerekir.

**Ara durum modelde açıkça bulunur.** `PAID_TICKET_PENDING` gibi bir durum, "ödeme tamam, teslimat bekliyor" gerçeğini birinci sınıf bir bilgi haline getirir. Bu durum yoksa sistem tutarsızlığı ancak müşteri çağrı merkezini aradığında öğrenir — ve o noktada artık bir mühendislik sorunu değil, bir itibar sorunudur.

**Telafi otomatiktir.** Biletleme kesin olarak başarısız olduğunda bir **telafi işlemi (compensating transaction)** koşar. İşlem henüz takas edilmemişse void, edilmişse refund tetiklenir. Rezervasyon durumu güncellenir ve müşteri bilgilendirilir. Telafi bir "geri alma" (rollback) değildir: geçmiş silinmez, **yeni bir finansal işlem** açılır ve kendi izini bırakır (`126`, `253`).

**Cevapsızlık ile başarısızlık ayrılır.** Biletleme isteği zaman aşımına düştüyse sonuç bilinmiyordur; bilet kesilmiş de olabilir. Bu durumda körlemesine iade göndermek, bileti olan bir yolcunun parasını geri vermek anlamına gelir. Doğru davranış şudur: cevapsız kalan istekleri tarayan bir **zaman aşımı işi** çalışır. Bu iş önce biletleme tarafının sorgulama ucundan akıbeti netleştirir. Telafiyi ancak "kesilmedi" kesinleştikten sonra tetikler (`253`).

**Her adım tekrar edilse de sonucu değişmez.** Telafinin kendisi de tekrar tetiklenebilir; iki kez koşan bir iade iki kez para gönderir. Bu yüzden iade, biletleme ve capture uçlarının hepsi tekilleştirme anahtarıyla korunur ve sonuç anahtarla birlikte saklanır (`130`). Adım olayları da veritabanı yazımıyla aynı yerel işlemde giden kutusu tablosuna (outbox) yazılıp yayınlanır (`116`).

Bir tasarım tercihi daha vardır ve mülakatta ayırt edicidir: bu saga **orkestrasyonla** yürütülür, koreografiyle değil. Yani adımları merkezi bir yönetici sırayla çağırır; servisler birbirine olay atarak kendi başlarına ilerlemez. Sebep teknik değil, denetimseldir. Bir itiraz veya iç inceleme geldiğinde "bu ödeme hangi adımda takıldı, telafi ne zaman koştu" sorusunun cevabı tek bir tablodan okunabilmelidir. Koreografi ise bu izi beş ayrı sistemin loguna dağıtır.

```svg
<svg viewBox="0 0 660 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Capture ve biletleme zinciri: ara durum, telafi islemi ve timeout isi">
  <defs>
    <marker id="havode-tel-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="havode-tel-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
    <marker id="havode-tel-amb" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B07D20"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Para çekildi, bilet kesilemedi: ara durum + telafi</text>
  <rect x="16" y="46" width="150" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="91" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">CAPTURE (250)</text>
  <text x="91" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">para alındı</text>
  <path d="M166 72 L192 72" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#havode-tel-arw)"/>
  <rect x="196" y="46" width="200" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="296" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">PAID_TICKET_PENDING</text>
  <text x="296" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ara durum modelde açıktır</text>
  <path d="M396 72 L422 72" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#havode-tel-arw)"/>
  <rect x="426" y="46" width="210" height="52" rx="10" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.6"/>
  <text x="531" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ticketing · host/GDS (221)</text>
  <text x="531" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrı sistem — düşebilir, susabilir</text>
  <path d="M470 98 L470 110 L116 110 L116 122" fill="none" stroke="#B23B3B" stroke-width="1.7" stroke-dasharray="5 3" marker-end="url(#havode-tel-red)"/>
  <text x="292" y="106" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">kesin başarısız</text>
  <path d="M512 98 L512 116 L336 116 L336 122" fill="none" stroke="#B07D20" stroke-width="1.7" stroke-dasharray="5 3" marker-end="url(#havode-tel-amb)"/>
  <text x="424" y="130" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B07D20" font-family="system-ui">cevapsız</text>
  <path d="M580 98 L580 122" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#havode-tel-arw)"/>
  <rect x="16" y="126" width="200" height="50" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="116" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">TELAFİ (126)</text>
  <text x="116" y="163" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">void / refund · müşteriye bildirim</text>
  <rect x="236" y="126" width="200" height="50" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="336" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Timeout job (253)</text>
  <text x="336" y="163" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">önce sorgula, sonra telafi et</text>
  <rect x="456" y="126" width="180" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="546" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">TICKETED</text>
  <text x="546" y="163" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">bilet no PNR'a işlenir</text>
  <rect x="16" y="192" width="620" height="36" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="326" y="214" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Tüm uçlar idempotenttir (130) — telafinin kendisi de tekrar tetiklenebilir; olaylar outbox ile yayınlanır (116)</text>
  <rect x="16" y="236" width="620" height="40" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="326" y="255" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Orkestrasyon tercih edilir: "bu ödeme hangi adımda takıldı?" tek tablodan okunmalıdır</text>
  <text x="326" y="270" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">koreografi izi beş sistemin loguna dağıtır; itiraz ve denetim bunu kaldırmaz</text>
</svg>
```

### PNR, ödeme ve bilet: üçlünün anahtarı ve kanıt zinciri

`247` numaralı konu PNR'ın rezervasyon kaydı, e-biletin ise ödemenin karşılığı olduğunu kurmuştu. Ödeme sistemi açısından bu ayrımın pratik sonucu şudur: **PNR işlemin iş anahtarıdır.** Ödeme kaydı PNR'a bağlanır. Ödeme başarılıysa bilet düzenlenir ve üretilen **bilet numarası PNR'a işlenir**. Böylece üç ayrı sistemde yaşayan üç kayıt (rezervasyon, ödeme, bilet) ortak bir referansla birbirine bağlanır. "Bu paranın karşılığı hangi yolculuktu" sorusu tek sorguyla cevaplanır hale gelir.

Bunun en somut faydası **itiraz (chargeback) savunmasında** ortaya çıkar. Kart hamili "hizmeti alamadım" gerekçesiyle itiraz ettiğinde (`252`), sunulacak kanıt bir PSP işlem kaydı değildir. O kayıt yalnızca paranın çekildiğini gösterir; hizmetin verildiğini göstermez.

Havacılıkta en güçlü delil zinciri üç parçadır: **PNR** (kim, hangi uçuş), **bilet kupon durumu** (`used`/`flown`, yani uçuldu) ve **check-in / biniş kaydı** (yolcu fiilen uçağa bindi). Bu üçlü, "hizmeti alamadım" iddiasını doğrudan çürütür.

Mühendislik sonucu nettir: bu kayıtlar itiraz penceresi boyunca **erişilebilir kalmalıdır**. İtiraz süreçleri aylarca sürebilir. Check-in kaydını otuz günde arşivden silen bir tasarım savunmasız kalır. PNR'ı uçuş sonrası sadeleştiren veya kupon geçmişini üzerine yazan tasarımlar da öyle. Kanıt zinciri bir raporlama kolaylığı değil, doğrudan gelir koruyan bir veri saklama kararıdır. Bu da `250`'de anlatılan "yalnızca ekleyerek yazılan durum geçmişi"nin havacılık karşılığıdır.

### Uçuş iptalinde toplu iade: IRROPS'un para tarafı

IRROPS (`247`, `248`) mühendislik tarafında yolcu yeniden yerleştirme problemi olarak anlatılır. Para tarafında ise başka bir şeydir: **kitlesel iade**. Tek bir iptal, iki yüz yolcunun biletinin iade edilmesi demektir. Büyük bir fırtına dalgası bu sayıyı on binlere çıkarır. Bu iş kesinlikle bir toplu iştir ve dört özelliği taşımalıdır.

**Kayıt bazında tekilleştirilmiştir.** İade işleminin tekliği, bilet numarasına bağlı benzersiz bir iade kaydıyla korunur. Aynı bilet için ikinci bir iade isteği yeni işlem açmaz; mevcut kaydın sonucunu döndürür. İş yarıda kesilip yeniden başladığında çift iadeyi imkânsız kılan tek mekanizma budur.

**Yeniden başlatılabilir.** İş, işlenmiş kalemleri atlayarak kaldığı yerden devam eder. Spring Batch'in yeniden başlatma ve atlama/yeniden deneme yetenekleri tam bu senaryo için vardır (`222`, `223`). Tekilleştirme olmadan "baştan çalıştıralım" demek, iki yüz yolcuya iki kez para göndermek demektir.

**Kontrollü hızdadır.** PSP'nin ve iade uçlarının hız sınırları vardır. On bin iadeyi aynı anda fırlatmak üç zarar verir: karşı tarafta hız sınırına takılırsın, kendi bağlantı havuzunu tüketirsin ve canlı satış trafiğini aç bırakırsın. Parça (chunk) boyutu ile eşzamanlılık bilinçli parametrelerdir (`153`).

**Başarısızlar ayrıştırılır.** Kalıcı olarak başarısız olan kalemler işi durdurmaz; örneğin kart kapanmıştır ya da kayıt tutarsızdır. Bunlar ayrı bir istisna kuyruğuna düşer, raporlanır ve operasyon tarafından veya alternatif iade yöntemiyle ele alınır.

Toplu iade ayrıca muhasebe tarafında **ayrı bir mutabakat kalemi** olarak izlenir. Çünkü normal günün iade hacminden farklı bir imzası vardır. Bu ayrımı yapamayan sistem, gün sonunda açıklanamayan bir fark üretir.

Bir de sürekliliğin diğer yüzü vardır: ödeme servisi çöktüğünde rezervasyon servisi de düşmemelidir. Ödeme çağrısında devre kesici açılır (`101`). Devre açıkken ne yapılacağı bir iş kararıdır. Üç seçenek vardır: alternatif sağlayıcıya geçmek, rezervasyonu tutma durumunda bekletip müşteriye ödeme bağlantısı göndermek, ya da anlamlı bir hata verip yeniden denemeye yönlendirmek. Zaman aşımı, bölme duvarı (bulkhead) ve eşzamansız kuyruk ise bir servisin çöküşünün zincirleme yayılmasını engelleyen üçlüdür.

### Çoklu para birimi: tutar tek başına bir sayı değildir

Uluslararası bir havayolu düzinelerce para biriminde satış yapar ve bu, ödeme modelinin en sessiz hata kaynağıdır.

**Tutar ve para birimi ayrılmaz taşınır.** Doğru model bir `Money` değer nesnesidir (value object). İçinde `BigDecimal` tutar ve ISO 4217 para birimi kodu birlikte durur. Farklı para birimlerine sahip iki `Money` toplanamaz; denendiğinde sessizce bir sayı üretmek yerine **istisna fırlatır**.

Bu kısıt kulağa katı gelir ama amaç tam olarak budur. "Yetmiş beş" ile "yetmiş beş"i toplayıp yüz elli yazan bir rapor düşün. Bu sayının EUR mi TRY mi olduğu belli değilse rapor yanlıştır — üstelik yanlış olduğu aylarca fark edilmez. Toplama gerçekten gerekiyorsa açık bir çevrim adımı yazılır ve kullanılan kur kayda geçer.

**Kur işlem anında sabitlenir ve işlemle birlikte saklanır.** Ödeme yapıldığı anda geçerli olan kur, ödeme kaydının bir alanıdır. İade, itiraz ve muhasebe **o günkü kurla** yapılır. Kırk gün sonra gelen bir iadeyi bugünkü kurla hesaplarsan kur farkı kadar kayıp ya da kazanç doğar; mutabakatta da karşılığı olmayan bir kalem kalır. Aynı sebeple orijinal para birimi ile tahsilat para birimi ayrı alanlarda tutulur.

**Yuvarlama para birimine göre değişir.** Her para biriminin ondalık basamak sayısı ISO 4217 üs (exponent) alanında tanımlıdır. JPY'nin kuruşu yoktur (0 ondalık). TRY, EUR ve USD 2 ondalıkla yazılır. BHD ve KWD gibi bazı para birimleri 3 ondalık kullanır. Her yeri iki ondalığa sabitleyen kod, JPY işlemlerinde karşı sistemle uyuşmayan tutarlar üretir. Doğru davranış iki adımlıdır: `setScale` çağrısının ölçeğini para biriminin üs değerinden almak ve yuvarlama modunu **açıkça** belirtmek.

**DCC şeffaflık gerektirir.** Müşteriye kendi para biriminde ödeme seçeneği sunuluyorsa, uygulanan kur ve kur farkı komisyonu ekranda açıkça gösterilmelidir. Müşteri karşılaştırma yapabilmelidir. Gizlenen bir DCC farkı, itirazların ve düzenleyici şikâyetlerin bilinen kaynağıdır.

`double` bu dünyada yasaktır. Sebebi şudur: ikili kayan nokta ondalıkları tam temsil edemez. Milyonlarca işlemde biriken kuruş farkları da mutabakatı tutmaz hale getirir (`226`).

```svg
<svg viewBox="0 0 660 286" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Money value object, kur sabitleme ve para birimine gore yuvarlama olcegi">
  <defs>
    <marker id="havode-money-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Money: tutar tek başına bir sayı değildir</text>
  <rect x="20" y="42" width="190" height="82" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="115" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Money value object</text>
  <text x="115" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">amount: BigDecimal (226)</text>
  <text x="115" y="98" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">currency: ISO 4217</text>
  <text x="115" y="114" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ikisi hiç ayrılmaz</text>
  <path d="M210 83 L236 83" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#havode-money-arw)"/>
  <rect x="240" y="42" width="190" height="82" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="335" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">TRY 1.250 + EUR 40</text>
  <text x="335" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">toplanamaz → exception</text>
  <text x="335" y="98" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sessiz yanlış sayı üretmez</text>
  <text x="335" y="114" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çevrim ayrı ve açık adımdır</text>
  <path d="M430 83 L456 83" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#havode-money-arw)"/>
  <rect x="460" y="42" width="180" height="82" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="550" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kur sabitlenir</text>
  <text x="550" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">işlem anındaki kur</text>
  <text x="550" y="98" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">işlemle birlikte saklanır</text>
  <text x="550" y="114" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iade · itiraz · muhasebe aynı kur</text>
  <text x="330" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yuvarlama ölçeği ISO 4217 exponent'inden gelir</text>
  <rect x="60" y="156" width="170" height="50" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="145" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">JPY · 0 ondalık</text>
  <text x="145" y="193" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kuruşu yoktur</text>
  <rect x="245" y="156" width="170" height="50" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">TRY · EUR · USD · 2</text>
  <text x="330" y="193" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yaygın hal</text>
  <rect x="430" y="156" width="170" height="50" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="515" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">BHD · KWD · 3</text>
  <text x="515" y="193" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her yer 2 varsayımı kırılır</text>
  <rect x="20" y="220" width="620" height="52" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="240" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">DCC: müşterinin kendi para biriminde ödemesi — kur ve komisyon farkı ekranda açıkça gösterilir</text>
  <text x="330" y="258" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">double yasaktır: ikili kayan nokta ondalığı tam temsil edemez, biriken kuruş farkı mutabakatı bozar (226)</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Havayolu parasını nereden alır?"** İki kanal: doğrudan satış kartla (`250`, `252`) ve acente satışı **IATA BSP** takasıyla. BSP dönemsel netleştirip toplu settle eder; iki kanalın mutabakat anahtarları, zamanlaması ve kesinti mantığı farklıdır, bu yüzden iki ayrı eşleştirme akışı gerekir.
- **"Para çekildi ama bilet kesilemedi, ne yaparsın?"** — sözlü cevap iskeleti, aynen kullan (5 adım): (1) "Ara durum modelde açıktır: `PAID_TICKET_PENDING`." (2) "Kesin başarısızlıkta telafi otomatik koşar — void veya refund (`126`)." (3) "Cevapsızlık başarısızlık değildir; önce biletleme tarafının sorgulama ucundan akıbet netleşir, sonra telafi (`253`)." (4) "Her uç idempotenttir, olaylar outbox ile yayınlanır (`130`, `116`)." (5) "Sagayı orkestrasyonla yürütürüm; itirazda 'hangi adımda takıldı' tek tablodan okunmalı."
- **"Chargeback'te kanıtın ne olur?"** PSP kaydı yetmez; PNR + bilet kupon durumu (`used`/`flown`) + check-in/biniş kaydı. Bu zincir itiraz penceresi boyunca erişilebilir tutulur — arşivleme politikası doğrudan gelir koruma kararıdır.
- **"Uçuş iptal oldu, iki yüz yolcuya iade yapacaksın."** Bilet bazında benzersiz iade kaydıyla idempotent, restart edilebilir toplu iş (`222`, `223`), PSP hız sınırına takılmayan kontrollü hız (`153`), başarısızlar istisna kuyruğuna; muhasebede ayrı mutabakat kalemi.
- **"Çoklu para biriminde nelere dikkat edersin?"** `Money` value object, farklı birimler toplanmaz, kur işlem anında sabitlenip saklanır, yuvarlama ISO 4217 exponent'ine göre (JPY 0, BHD 3), DCC şeffaflığı; `double` yasak (`226`).
- **"Ödeme servisi çökerse rezervasyon ne olur?"** Devre kesici (`101`) + timeout + bulkhead; fallback bir iş kararıdır (alternatif sağlayıcı, rezervasyonu tutup ödeme linki, anlamlı hata). "Çağrıyı sonsuza kadar yeniden dener" cevabı eleyicidir.

### Tek cümlede

Havacılıkta ödeme beş işin toplamıdır. Bir: iki ayrı gelir kanalının ayrı defterlerini tutmak — kart mutabakatlı doğrudan satış ve dönemsel netleştirilen **BSP takası**. İki: satın alma ile teslimatın farklı sistemlerde olmasından doğan **"para çekildi, bilet kesilemedi"** boşluğunu kapatmak; bunun araçları açık bir ara durum, otomatik telafi, sorgulamayla ayrılan cevapsızlık ve tekrar edilse de sonucu değişmeyen adımlardır. Üç: itiraza karşı **PNR + kupon + biniş kaydı** kanıt zincirini saklamak. Dört: iptal dalgalarını yeniden başlatılabilir toplu iadeyle karşılamak. Beş: tutarı **para birimi, kur ve ölçeğiyle birlikte** taşıyan bir para modeli kurmak.

# havode-1 | Orta

## Soru
Bir havayolunun gelirinin yaklaşık yarısı seyahat acenteleri üzerinden geliyor. Ekip, acente satışlarını da mevcut kart mutabakat akışına sokmak istiyor: acente satışlarını sanki kart işlemiymiş gibi kaydedip PSP dosyalarıyla eşleştirmeyi planlıyorlar. Doğru tasarım hangisidir?

## A
Acente satışları da bir PSP üzerinden kaydedilir ve tüm satışlar tek bir işlem tablosunda toplanır; böylece mutabakat tek bir eşleştirme kuralıyla yürür
🤖 acente satışında havayolu kartı hiç görmez ve ortada PSP işlemi yoktur; olmayan bir kaydı üretmek mutabakatı baştan uydurma anahtarlara dayandırır.

## B
Acente satışlarının parası zaten kart kanalından geldiği için ayrı bir akışa gerek yoktur; fark yalnızca komisyon oranındadır
🤖 acente satışının parası kart şemasından değil BSP takasından gelir; iki akışın kaynağı, zamanlaması ve kesinti mantığı farklıdır.

## C
Acente satışları IATA BSP üzerinden dönemsel netleştirilip toplu ödenir; bu kanal için ayrı bir dosya işleyen mutabakat akışı kurulur, gelir kaydı ise ortak defterde bilete bağlanır ve kanal bir öznitelik olur
🤖 Doğru: iki kanalın besleme ve eşleştirme kuralı ayrılır, defter tek kalır.

## D
Acente satışları mutabakat kapsamı dışında bırakılır; BSP'den gelen toplu ödeme tek kalem olarak muhasebeye yazılır ve kalem kırılımı takip edilmez
🤖 toplu ödemenin içindeki kalemler bilet bazında eşleştirilmezse eksik veya fazla ödenen satışlar hiç görünmez; netleştirilmiş tutar tek başına doğrulanamaz.

## Doğru
C

## Açıklama
Havayolunun iki gelir kanalı vardır ve ikisi **yapısal olarak farklıdır**. Doğrudan satışta müşteri kartını havayoluna verir; otorizasyon, capture ve takas kart dünyasının kurallarıyla işler ve mutabakatın birimi tek tek işlemdir — anahtar PSP işlem kimliği ya da otorizasyon kodudur (`250`, `252`). Acente satışında ise havayolu kartı hiç görmez: acente bileti satar, satışı **IATA BSP**'ye raporlar, BSP dönem içindeki tüm hareketleri toplayıp iadeleri ve komisyonları düşerek **netleştirir** ve her havayoluna dönem sonunda toplu ödeme yapar. Mutabakatın birimi burada dosya ve kalemdir. İki kanalı tek akışta birleştirmek, uyuşmayan anahtarları (PSP kimliği karşısında acente kodu ve bilet numarası), uyuşmayan zamanlamaları (T+1/T+2 karşısında dönemsel takas) ve uyuşmayan kesinti mantıklarını aynı kurala sıkıştırmak demektir; bu tür bir birleştirme birkaç dönem sonra açıklanamayan farklar üretir. Doğru yapı iki ayrı besleme ve iki ayrı eşleştirme kuralı, tek ortak **defterdir**: gelir kaydı biletin kendisine bağlanır, hangi kanaldan tahsil edildiği o kaydın bir özniteliğidir. BSP ödemesini kırılımsız tek kalem yazmak da savunulamaz: netleştirilmiş bir tutarın doğruluğu ancak içindeki kalemler kendi biletleriyle eşleştirilerek kanıtlanır — bu, havacılık ödeme ekiplerinin en klasik toplu işidir (`222`, `223`).

# havode-2 | Orta

## Soru
Havayolunun raporlama servisi, gün içinde farklı pazarlardan gelen tahsilatları toplayarak tek bir "günlük ciro" rakamı üretiyor. Kod, tüm tutarları `BigDecimal` olarak alıp topluyor ve sonucu ekrana yazıyor; para birimi bilgisi ayrı bir kolonda duruyor ve toplama sırasında kullanılmıyor. Doğru model hangisidir?

## A
Tutar ile para birimi ayrılmaz bir `Money` value object'inde birlikte taşınır; farklı para birimlerinin toplanması istisna fırlatır, tek rakam gerekiyorsa açık bir çevrim adımı yazılır ve kullanılan kur kayda geçer
🤖 Doğru: para birimi tutarın etiketi değil parçasıdır; çevrim sessiz değil açık bir işlemdir.

## B
`BigDecimal` kullanıldığı için hassasiyet zaten güvendedir; para birimi bir sunum ayrıntısıdır ve toplama işlemini etkilemez, birim yalnızca ekranda ve raporda etiket olarak gösterilir
🤖 `BigDecimal` yalnızca ondalık hassasiyeti çözer; farklı birimlerin toplamı hassas ama anlamsız bir sayıdır.

## C
Tüm tutarlar veritabanına yazılırken kuruş cinsinden `long` olarak saklanır; ortak birimde tutulan sayılar güvenle toplanabilir ve tam sayı aritmetiği ondalık sorunlarını tümüyle ortadan kaldırır
🤖 kuruş cinsinden saklamak ölçek sorununu ele alır ama para birimi sorununa dokunmaz; JPY ile TRY kuruşları da toplanamaz.

## D
Toplama serbest bırakılır, tutarsızlık gün sonunda mutabakat işi tarafından yakalanır ve rapor o aşamada düzeltilir; mutabakat zaten tüm farkları gördüğü için ikinci bir kontrole gerek kalmaz
🤖 mutabakat farklı para birimlerinin toplanmasından doğan hatayı göremez; ortaya çıkan sayı tutarlı görünür ama yanlıştır.

## Doğru
A

## Açıklama
Para birimi, tutarın yanına iliştirilen bir etiket değil, **tutarın parçasıdır**. Doğru model bir `Money` value object'idir: içinde `BigDecimal` tutar ve ISO 4217 para birimi kodu birlikte durur, ikisi hiçbir katmanda ayrılmaz. Bu modelin en değerli davranışı bir kısıttır: farklı para birimlerine sahip iki `Money` toplanamaz ve denendiğinde **istisna fırlatır**. Katı görünür, amaç tam da budur — "kırk" ile "kırk"ı toplayıp seksen yazan bir raporun EUR mi TRY mi olduğu belli değilse rapor yanlıştır ve yanlışlığı aylarca fark edilmez, çünkü ortada bir hata mesajı yoktur, yalnızca makul görünen bir sayı vardır. Gerçekten tek rakam gerekiyorsa çevrim **açık bir adım** olur: kullanılan kur seçilir, uygulanır ve kayda geçer. `BigDecimal` cevabı doğru aracı yanlış soruna uygular: `BigDecimal` ikili kayan noktanın ondalık temsil sorununu çözer (`226`), farklı birimlerin anlamsızlığını değil. Kuruş cinsinden `long` saklamak da yalnızca ölçek tarafını ele alır; JPY kuruşu ile TRY kuruşunu toplamak hâlâ yanlıştır ve JPY'nin ondalığı olmadığı için ayrıca ölçek varsayımını da kırar. Hatanın mutabakatta yakalanacağını ummak ise en tehlikelisidir: mutabakat kalemleri kendi para birimleriyle eşleştirir, raporun içindeki karışık toplamı hiç görmez.

# havode-3 | İleri

## Soru
Tokyo'dan satılan bir bilet 128.000 JPY tutarında ödendi; işlem tarihindeki kur ödeme kaydına yazılmadı. Kırk gün sonra yolcu iade talep etti ve ekip iade tutarını bugünkü kurla yeniden hesaplayıp, tutarı iki ondalık basamağa yuvarlayarak PSP'ye gönderdi. Doğru tasarım hangisidir?

## A
İade her zaman iade gününün kuruyla hesaplanır; müşterinin bugünkü değeri alması adil olan davranıştır ve kur farkı havayolunun doğal riskidir
🤖 iade, gerçekleşmiş bir işlemin geri alınmasıdır; farklı bir kurla dönmek mutabakatta karşılığı olmayan bir kur farkı kalemi bırakır.

## B
Kur sorunu ortadan kalksın diye tüm işlemler tahsilat anında havayolunun ana para birimine çevrilip yalnızca çevrilmiş tutar saklanır; orijinal tutara ihtiyaç duyulmaz
🤖 orijinal para birimi ve tutar kaybolursa müşteriye kartından çekilen tutarın aynısı iade edilemez ve itiraz savunmasında işlem yeniden kurulamaz.

## C
Ondalık farkları önemsizdir; tüm para birimleri iki ondalıkla standartlaştırılır ve karşı sistemde oluşan küçük farklar gün sonu yuvarlama kalemiyle kapatılır
🤖 JPY'nin ondalığı yoktur; iki ondalık göndermek karşı sistemle uyuşmayan tutar üretir ve bu bir yuvarlama kalemi değil, reddedilen ya da hatalı işlem demektir.

## D
İşlem anındaki kur ödeme kaydının bir alanıdır ve iade o kurla hesaplanır; orijinal tutar ile tahsilat tutarı ayrı alanlarda tutulur, yuvarlama ölçeği para biriminin ISO 4217 exponent'inden alınır — JPY için sıfır ondalık
🤖 Doğru: kur işlemle birlikte dondurulur ve ölçek para biriminin kendi tanımından gelir.

## Doğru
D

## Açıklama
Çoklu para biriminde iki ayrı kural aynı anda ihlal edilmiş: kur sabitleme ve ölçek. **Kur, işlem anında sabitlenir ve işlemle birlikte saklanır**; iade, itiraz ve muhasebe kayıtları o kurla yürür. Sebep adalet değil, kapanabilirliktir: iade gerçekleşmiş bir işlemin geri alınmasıdır ve farklı bir kurla dönüldüğünde ortaya, karşılığında hiçbir işlem bulunmayan bir kur farkı çıkar — bu kalem mutabakatta açıklanamaz ve muhasebe onu bir hata olarak raporlar. Orijinal para birimi ile tahsilat para birimi ayrı alanlarda tutulur ki müşterinin kartından çekilen tutarın **aynısı** geri verilebilsin. Burada ince ama mülakatta değerli bir ayrım vardır: şemaya gönderilen iade tutarı **orijinal işlem para biriminde ve orijinal tutardadır** — saklanan kur, iadeyi "yeniden hesaplamak" için değil, muhasebedeki ters kaydın ilk kayıtla aynı değerden kapanması için kullanılır. Kuru iade anında yeniden uygulamak ile bugünün kurunu kullanmak aynı hatanın iki yüzüdür. İkinci ihlal ölçektedir: her para biriminin ondalık basamak sayısı ISO 4217 exponent alanında tanımlıdır ve JPY için bu değer **sıfırdır** — yenin kuruşu yoktur. İki ondalıkla gönderilen bir JPY tutarı karşı sistemle uyuşmaz; bu bir "küçük fark" değil, reddedilen ya da yanlış tutarla işlenen bir istektir. Aynı tuzağın diğer ucu BHD ve KWD gibi **üç ondalıklı** para birimleridir. Doğru davranış, `setScale` ölçeğini para biriminin exponent'inden almak ve yuvarlama modunu açıkça belirtmektir. Tüm işlemleri tek para birimine çevirip orijinali atmak da savunulamaz: itiraz savunmasında işlemin müşterinin gördüğü haliyle yeniden kurulabilmesi gerekir. Bu kuralların hepsinin altında `BigDecimal` disiplini durur; `double` ile kur çarpımı yapmak, biriken kuruş farklarıyla mutabakatı zaten baştan bozar (`226`).

# havode-4 | İleri

## Soru
Bilet satışında capture başarılı oldu, ardından biletleme isteği host sisteme gönderildi ve otuz saniye sonra istek zaman aşımına düştü — hiçbir cevap gelmedi. Servisin bu noktada yapması gereken nedir?

## A
Zaman aşımı başarısızlık sayılır ve refund hemen tetiklenir; müşteri parasını en hızlı şekilde geri almalıdır, böylece çağrı merkezine düşen şikâyet ve itiraz riski en baştan kesilmiş olur
🤖 zaman aşımı "olmadı" değil "bilmiyorum" demektir; bilet kesilmiş olabilir ve o durumda bileti olan yolcunun parası iade edilmiş olur.

## B
Ödeme `PAID_TICKET_PENDING` ara durumunda kalır; bir timeout işi cevapsız istekleri tarayıp biletleme tarafının sorgulama ucundan akıbeti netleştirir, "kesilmedi" kesinleştiğinde idempotent telafi (void/refund) koşar ve tüm adımlar tek bir orkestrasyon kaydından izlenir
🤖 Doğru: belirsizlik açık bir durumla temsil edilir, karar sorgulamayla verilir, telafi tekrar güvenlidir.

## C
İstek başarılı cevap dönene kadar aynı gövdeyle yeniden gönderilir; biletleme sistemi eninde sonunda cevap verecektir ve tekrarlar arasında kısa bir bekleme konarak karşı tarafa toparlanma şansı verilir
🤖 idempotency anahtarı olmadan tekrarlanan biletleme isteği aynı yolcuya birden fazla bilet kesebilir; ayrıca zaten yüklü olan karşı sistemi daha da bastırır.

## D
Ödeme kaydı `FAILED` işaretlenir ve durum operasyon ekibinin gün sonu raporuna bırakılır; gerçek akıbet elle netleştirilir, böylece belirsiz vakalarda kararı otomatik bir mekanizma değil insan vermiş olur
🤖 parası alınmış ve bileti belirsiz bir müşteriyi insan hızına bırakmak itirazın birinci sebebidir; ayrıca yanlış işaretlenmiş bir durum sonraki tüm kararları bozar.

## Doğru
B

## Açıklama
Zaman aşımının anlamı "işlem olmadı" değil, **"sonucu bilmiyorum"dur** (`253`) ve bu senaryonun tamamı bu ayrımın üzerine kuruludur. Biletleme isteği host tarafına ulaşmış, bilet kesilmiş ve yalnızca cevap dönüşü kaybolmuş olabilir. O yüzden ilk hamle karar vermek değil, **belirsizliği temsil etmektir**: ödeme kaydı `PAID_TICKET_PENDING` gibi açık bir ara durumda kalır — "para alındı, teslimat kesinleşmedi" gerçeği modelde birinci sınıf bir bilgi olur. İkinci hamle akıbeti netleştirmektir: cevapsız kalan istekleri tarayan bir timeout işi, biletleme tarafının sorgulama ucundan işlemin gerçekleşip gerçekleşmediğini sorar. Yalnızca "kesilmedi" kesinleştiğinde telafi koşar ve o telafi de idempotency anahtarıyla korunur (`130`), çünkü telafinin kendisi tekrar tetiklenebilir. Hemen refund atmak tam da bu yüzden yanlıştır: bileti kesilmiş bir yolcunun parası iade edilir ve havayolu hizmeti karşılıksız vermiş olur. Aynı gövdeyle körlemesine yeniden denemek, idempotency anahtarı yoksa aynı yolcuya ikinci bir bilet kestirir ve zaten sıkışmış karşı sistemi daha da bastırır. Kaydı `FAILED` işaretleyip elle çözüme bırakmak ise iki hatayı birleştirir: durum yanlış yazılır ve sonraki tüm otomatik kararlar bu yanlış üzerinden ilerler, üstelik parası alınmış müşteri insan hızında bekletilir — chargeback'in bilinen birinci sebebi budur. Akışın orkestrasyonla yürütülmesi de bilinçli bir tercihtir: itiraz veya denetim geldiğinde "bu ödeme hangi adımda takıldı, telafi ne zaman koştu" sorusu tek bir tablodan cevaplanabilmelidir (`126`, `116`).

# havode-5 | Uzman

## Soru
Bir yolcu üç ay önce satın aldığı bileti kullanarak uçtu; şimdi kart hamili olarak "hizmeti alamadım" gerekçesiyle itiraz açtı. Havayolunun ödeme ekibi savunma dosyasını hazırlıyor ve elinde yalnızca PSP işlem kaydı ile otorizasyon kodu var; check-in kayıtları uçuştan otuz gün sonra arşivden silinmiş. Bu tablonun doğru okuması nedir?

## A
PSP işlem kaydı ve otorizasyon kodu yeterlidir; ödemenin başarıyla gerçekleştiğini gösterdikleri için itiraz bu belgelerle karşılanabilir ve tahsilatın gerçekliği tartışmasız biçimde kanıtlanmış olur
🤖 bu belgeler paranın çekildiğini kanıtlar, hizmetin verildiğini değil; itirazın konusu tam olarak hizmetin verilip verilmediğidir.

## B
İtiraz kaçınılmaz olarak kaybedilir ve tek yapılabilecek şey benzer itirazları azaltmak için fraud kurallarını sıkılaştırıp riskli profilleri satın alma anında elemektir
🤖 fraud kuralları satın alma anına bakar; buradaki sorun kural değil, kanıtın saklanmamış olmasıdır ve bu doğrudan çözülebilir bir tasarım eksiğidir.

## C
Bilet numarasının PNR'a işlenmiş olması tek başına yeterli kanıttır; bilet üretilmişse hizmet verilmiş sayılır ve yolcunun uçup uçmadığını ayrıca belgelemeye gerek kalmaz
🤖 bilet üretimi yolculuğun yapıldığını göstermez; kupon `open` kalmış, uçulmamış bir bilet de vardır ve itiraz tam bu ayrımda kazanılır ya da kaybedilir.

## D
Havacılıkta kanıt zinciri PNR, bilet kupon durumu (`used`/`flown`) ve check-in/biniş kaydından oluşur; bu kayıtlar itiraz penceresi boyunca erişilebilir tutulmalıdır ve arşivleme politikası buna göre belirlenir
🤖 Doğru: hizmetin fiilen alındığını gösteren zincir budur ve saklama süresi doğrudan bir gelir koruma kararıdır.

## Doğru
D

## Açıklama
Bir chargeback itirazında sorulan soru "para çekildi mi" değil, **"hizmet verildi mi"dir** (`252`). PSP işlem kaydı ve otorizasyon kodu yalnızca birincisini kanıtlar; ikincisine hiç değinmez ve bu yüzden tek başlarına savunma değeri düşüktür. Havacılıkta hizmetin verildiğini gösteren zincir üç halkalıdır: **PNR** işlemin iş anahtarıdır ve kimin hangi uçuşta olduğunu bağlar (`247`); **bilet kupon durumu** `used`/`flown` ise o bacağın fiilen uçulduğunu söyler — kupon durum makinesinin tek yönlü geçişleri tam da bunu güvenilir kılar; **check-in ve biniş kaydı** ise yolcunun uçağa bindiğini gösteren operasyonel delildir. Bu üçlü, "hizmeti alamadım" iddiasını doğrudan çürüten en güçlü kanıt kümesidir. Bilet numarasının PNR'a işlenmiş olması ise yeterli değildir: bilet üretilmiş ama uçulmamış olabilir, kupon hâlâ `open` durumda durabilir — itiraz tam bu ayrımda kazanılır veya kaybedilir. Vakanın asıl dersi saklama politikasındadır: itiraz süreçleri aylar alır, dolayısıyla kanıt zincirini oluşturan kayıtlar **itiraz penceresi boyunca erişilebilir** tutulmalıdır. Check-in kaydını otuz günde silen, uçuş sonrası PNR'ı sadeleştiren ya da kupon geçmişini üzerine yazan bir tasarım, teknik olarak temiz görünür ama her itirazı peşinen kaybeder. Arşivleme burada bir depolama maliyeti tartışması değil, doğrudan gelir koruyan bir karardır ve `250`'de anlatılan append-only durum geçmişi disiplininin havacılık karşılığıdır. Fraud kurallarını sıkılaştırmak farklı bir problemi çözer: satın alma anındaki riski azaltır, geçmiş bir işlemin kanıtını geri getirmez.

# havode-6 | Uzman

## Soru
Fırtına nedeniyle on iki uçuş iptal edildi; yaklaşık dört bin bilet için iade yapılacak. Ekip, iade listesini tek seferde okuyup tüm çağrıları paralel olarak PSP'ye gönderen bir iş yazdı; iş yarıda hata alırsa baştan çalıştırılıyor. Doğru tasarım hangisidir?

## A
İade çağrıları eşzamanlı olarak azami hızda gönderilir ve hata alanlar aynı iş içinde sınırsız yeniden denenir; iadenin hızı müşteri memnuniyetinin birincil ölçüsüdür ve iptal haberini alan yolcuya para ne kadar erken dönerse itiraz olasılığı o kadar düşer
🤖 azami hız PSP'nin hız sınırına takılır, kendi bağlantı havuzunu tüketir ve canlı satış trafiğini aç bırakır; sınırsız tekrar bunu kalıcı hale getirir.

## B
İade işi gerçek zamanlı akıştan tamamen ayrılıp yolcunun talebine bağlanır; yolcu uygulamadan iade isteyene kadar hiçbir çağrı yapılmaz, böylece gereksiz iade üretilmez ve yük zamana yayılır
🤖 iptal edilen uçuşun iadesi havayolunun yükümlülüğüdür ve yolcunun girişini bekleyemez; ayrıca bu, sorunu çözmeyip yığılmayı çağrı merkezine kaydırır.

## C
Her bilet için benzersiz bir iade kaydıyla korunan idempotent adımlar kurulur, iş yeniden başlatılabilir biçimde chunk'lar hâlinde ilerler, gönderim hızı PSP sınırına göre kontrollü tutulur ve kalıcı başarısızlar istisna kuyruğuna ayrılıp raporlanır
🤖 Doğru: tekrar güvenliği, yeniden başlatılabilirlik, kontrollü hız ve başarısızların ayrıştırılması birlikte gerekir.

## D
İş her hata aldığında baştan çalıştırılır; iade uçları doğası gereği aynı bilete ikinci kez para göndermeyeceği için tekrar riski yoktur ve böylece hangi kaydın işlendiğini takip eden ek bir yapı tutmaya gerek kalmaz
🤖 iade ucunun kendiliğinden tekil olduğu varsayımı yanlıştır; tekillik kendi tarafında benzersiz iade kaydıyla kurulmadıkça aynı bilete ikinci iade gidebilir.

## Doğru
C

## Açıklama
IRROPS'un para tarafı bir **toplu iade** problemidir ve dört özelliği aynı anda taşımak zorundadır. Birincisi **idempotency**: iade işleminin tekliği bilet numarasına bağlı benzersiz bir iade kaydıyla korunur; aynı bilet için gelen ikinci istek yeni bir finansal işlem açmaz, mevcut kaydın sonucunu döndürür (`130`). Bu olmadan "baştan çalıştıralım" demek, dört bin yolcuya ikinci kez para göndermek demektir — ve iade ucunun kendiliğinden tekil davranacağı varsayımı hiçbir sağlayıcıda garanti edilmez. İkincisi **yeniden başlatılabilirlik**: iş chunk'lar hâlinde ilerler, işlenmiş kalemleri atlayarak kaldığı yerden devam eder; Spring Batch'in restart ve skip/retry yetenekleri tam bu senaryo içindir (`222`, `223`). Üçüncüsü **kontrollü hız**: dört bin çağrıyı azami paralellikle fırlatmak PSP'nin hız sınırına takılır, kendi bağlantı havuzunu tüketir ve aynı anda devam eden canlı satış trafiğini kaynaksız bırakır — chunk boyutu ve eşzamanlılık bilinçli parametrelerdir (`153`). Dördüncüsü **başarısızların ayrıştırılması**: kalıcı olarak düşen kalemler (kart kapanmış, kayıt tutarsız) işi durdurmaz, ayrı bir istisna kuyruğuna düşer ve raporlanır; kalıcı hataları sınırsız yeniden denemek yalnızca karşı sistemi ve kendi kaynaklarını yakar. Muhasebe tarafında toplu iade ayrıca **ayrı bir mutabakat kalemi** olarak izlenir; normal günün iade hacminden farklı bir imzası vardır ve bu ayrımı yapmayan sistem gün sonunda açıklanamayan bir fark üretir. İadeyi yolcunun talebine bağlamak ise yükümlülüğü müşteriye devretmektir: yığılma ortadan kalkmaz, çağrı merkezine kayar.
