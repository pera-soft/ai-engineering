---
slug: kart-odeme-akisi-aktorler
baslik: "Kart Ödeme Akışı ve Aktörler: Otorizasyon, Capture, Void ve Ödeme Durum Makinesi"
kademe: Orta
sira: 250
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir otele giriş yaptığını düşün. Resepsiyon kartını alır ve üç bin liralık bir **bloke** koyar — hesabında o para "ayrılmıştır" ama kimse almamıştır. Çıkışta gerçek hesabın bin sekiz yüz lira çıkar; otel yalnızca o kadarını çeker, kalan bloke birkaç gün içinde kendiliğinden çözülür. Odayı hiç kullanmadan iptal edersen bloke tamamen kaldırılır ve ekstrende hiçbir iz kalmaz. Kart ödemesinin tamamı bu üç hareketin üzerine kuruludur: **bloke (otorizasyon)**, **tahsilat (capture)**, **iptal (void)**.

Havayolu bileti tam olarak bu modeli zorlar. Çünkü satın alma anı ile teslimat anı aynı değildir: müşteri ödeme düğmesine bastığında bilet henüz kesilmemiştir.

Ödeme sistemleri mülakatı da hep aynı yerden başlar: "Bir kart ödemesinde para nasıl akar, kim kime ne der?" Bu soruya "PSP'ye istek atıyoruz, başarılı dönerse siparişi kapatıyoruz" diye cevap veren aday kendini dışarıdan bakan bir entegratör konumuna koyar. Aranan cevap üç şeyi anlatır: aktörler kimlerdir, aralarındaki **iki ayrı yolculuk** nedir (mesaj saniyeler sürer, para günler sürer), bu ayrım kendi veri modelinde ne yaratır.

### Temel kavramlar

- **kart hamili (cardholder):** ödemeyi yapan müşteri; kart onun bankasınca verilmiştir.
- **issuer (kartı veren banka):** kartı çıkaran ve **onay/red kararını veren** taraftır. Limit, bakiye ve müşteri riski onun defterindedir.
- **acquirer (üye işyeri bankası):** satıcının anlaşmalı bankası; işlemi şemaya taşır, parayı toplayıp merchant'a aktarır.
- **kart şeması (card scheme):** Visa, Mastercard, Troy gibi ağlar; mesajı yönlendirir, kuralları ve ücretleri koyar, uyuşmazlıkta karar mercii olur. Bakiye kararını **vermez**.
- **merchant (üye işyeri):** satıcı — burada havayolu.
- **PSP (payment service provider):** iyzico, Craftgate, Adyen, Stripe gibi aracı; birden fazla acquirer'ı, 3DS akışını, tokenizasyonu ve taksiti tek API arkasına saklar.
- **otorizasyon (authorization):** tutarın kartta **bloke** edilmesi; para henüz çıkmaz, limit ayrılır.
- **capture (tahsilat):** bloke edilen tutarın fiilen tahsil edilmek üzere işaretlenmesi.
- **void (iptal):** settle olmamış işlemin iptali; para hiç çekilmemiş olur (ayrıntı: `252`).
- **settlement (takas):** paranın taraflar arasında fiilen el değiştirmesi; dönemseldir, T+1/T+2 (ayrıntı: `252`).

### Para nasıl akar? — bir işlem, iki ayrı yolculuk

Aktör listesini ezberlemek yetmez. Asıl anlaşılması gereken şudur: tek bir "ödeme" kelimesinin altında **birbirinden bağımsız iki akış** vardır.

1. **Mesaj yolculuğu (saniyeler).** Müşteri kart bilgisini girer. İstek merchant'tan PSP/acquirer'a, oradan kart şemasına, şemadan issuer'a gider. Issuer kartın geçerliliğine, limite, bakiyeye ve kendi risk kurallarına bakar; **onay veya red** döner. Cevap aynı yolu tersine kat eder. Bu turun tamamı normalde bir-iki saniyedir. Sonunda merchant'ın elinde bir **otorizasyon kodu** vardır — ama kasasında para yoktur.
2. **Para yolculuğu (günler).** Onaylanmış işlemler dönem dönem netleştirilir; buna **netleştirme (clearing)** denir. Ardından para issuer'dan şema üzerinden acquirer'a, oradan merchant'ın hesabına geçer; bu adım da **takas (settlement)** adını alır. Arada **komisyon kesilir**. Bu yüzden müşteriden tahsil edilen tutar ile hesaba geçen tutar aynı sayı değildir.

Bu ayrımın doğrudan bir tasarım sonucu vardır: sistemde "ödendi" diye tek bir boolean tutulamaz. En az üç ayrı gerçek vardır — **onaylandı**, **tahsil edildi**, **hesaba geçti** — ve bunlar farklı zamanlarda, farklı kaynaklardan öğrenilir.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kart odemesinde mesaj yolculugu saniyeler ve para yolculugu gunler">
  <defs>
    <marker id="odeme-akis-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
    <marker id="odeme-akis-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek işlem, iki ayrı yolculuk</text>
  <rect x="14" y="44" width="112" height="50" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.5"/>
  <text x="70" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kart hamili</text>
  <text x="70" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">müşteri</text>
  <rect x="144" y="44" width="112" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="200" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Merchant</text>
  <text x="200" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">havayolu · PSP</text>
  <rect x="274" y="44" width="112" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Acquirer</text>
  <text x="330" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">satıcının bankası</text>
  <rect x="404" y="44" width="112" height="50" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="460" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Kart şeması</text>
  <text x="460" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yönlendirir · kural koyar</text>
  <rect x="534" y="44" width="112" height="50" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="590" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Issuer</text>
  <text x="590" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">KARARI VEREN</text>
  <path d="M126 62 L140 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#odeme-akis-arw)"/>
  <path d="M256 62 L270 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#odeme-akis-arw)"/>
  <path d="M386 62 L400 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#odeme-akis-arw)"/>
  <path d="M516 62 L530 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#odeme-akis-arw)"/>
  <path d="M590 94 L590 116 L200 116 L200 98" fill="none" stroke="#186399" stroke-width="1.6" stroke-dasharray="5 3" marker-end="url(#odeme-akis-arw)"/>
  <text x="395" y="132" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">MESAJ YOLCULUĞU: onay / red — saniyeler · sonunda elde otorizasyon kodu var, para yok</text>
  <rect x="14" y="148" width="632" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="330" y="169" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">PARA YOLCULUĞU: clearing ve settlement — günler</text>
  <text x="330" y="187" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Issuer → şema → acquirer → merchant hesabı · T+1 / T+2 · komisyon kesilir</text>
  <text x="330" y="203" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrıntı: 252</text>
  <path d="M560 148 L120 148" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#odeme-akis-grn)"/>
  <rect x="60" y="230" width="540" height="58" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="251" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış model: tek bir "ödendi mi?" boolean alanı</text>
  <text x="330" y="269" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">En az üç ayrı gerçek vardır: onaylandı · tahsil edildi · hesaba geçti</text>
  <text x="330" y="284" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">üçü farklı zamanda, farklı kaynaktan öğrenilir</text>
</svg>
```

### Otorizasyon, capture, void: havayolu neden bu sırayla çalışır?

Bilet satışında teslim edilen ürün fiziksel bir mal değildir. Teslim edilen şey **bir envanter kaydı ve bir bilet numarasıdır**. Üstelik biletleme (ticketing) adımı ödeme adımından ayrı bir sistemde çalışır: bazen saniyeler, bazen dakikalar sürer, bazen hiç tamamlanmaz. Akış bu yüzden şöyle kurulur:

1. **Otorizasyon alınır.** Müşterinin kartında tutar bloke edilir. Bu adım kartın ödeme kabiliyetini kanıtlar: limit yeterli mi, kart geçerli mi, issuer bu işleme razı mı? Bilet henüz kesilmemiştir ve para hâlâ müşterinin hesabındadır — sadece kullanılamaz haldedir.
2. **Biletleme denenir.** Envanter kesinleşir, bilet numarası üretilir, PNR'a işlenir (`247`).
3. **Biletleme başarılıysa capture edilir.** Artık gerçekten teslim edilecek bir ürün vardır; tahsilat bu noktada anlamlıdır.
4. **Biletleme başarısızsa void edilir.** Bloke kaldırılır ve müşteri ekstresinde **hiçbir iz kalmaz**. Bu yol "önce çekelim, olmazsa iade ederiz" yaklaşımından çok daha temizdir. İade ise ekstrede iki satır bırakır, komisyon maliyeti doğurur, günler sürer ve çağrı merkezine iş çıkarır.

Sıranın tersi de yanlıştır. "Bileti önce keselim, sonra parayı alırız" demek şu demektir: ödeme kabiliyeti doğrulanmamış bir yolcuya envanterden koltuk vermek. Havayolu tarafında bunun karşılığı doğrudan gelir kaybıdır.

**Auth'un bir ömrü vardır.** Otorizasyonun geçerlilik penceresi kart şemasına, işlem tipine, işyeri kategorisine ve issuer'a göre değişir. Kartın fiziken ortada olmadığı işlemlerde (card-not-present, yani internet satışı) **tipik olarak yedi gün** konuşulur. Havayolu, otel, araç kiralama gibi **teslimatı gecikmeli** kategorilerde şemalar daha uzun pencereler tanıyabilir.

Bu yüzden "kaç gün?" sorusuna ezber bir sayı verme. Doğru cevap şudur: "Şema kuralına ve acquirer sözleşmemize bakılır; sistemde bu bir parametredir." Pencere dolduğunda issuer blokeyi kendiliğinden çözer. Bunun iki sonucu vardır ve ikisi de mülakatta sorulur:

- Süresi dolmuş bir auth ile capture denemek **reddedilebilir**. O noktada koltuk satılmış, hizmet taahhüt edilmiş ama para alınamamış olur.
- Bu yüzden auth'un geçerlilik bitişi **ödeme kaydında bir alan olarak tutulur**. Capture edilmemiş otorizasyonları tarayan bir arka plan işi vardır. Pencere dolmadan iki şeyden biri yapılır: ya capture edilir, ya işlem serbest bırakılıp müşteriye yeni bir ödeme akışı sunulur. "Auth alındı, gerisi ilgili ekibin işi" diyen tasarım bu boşluğu üretimde bulur.

```svg
<svg viewBox="0 0 660 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Otorizasyon capture ve void akisi zaman ekseninde ve auth gecerlilik penceresi">
  <defs>
    <marker id="odeme-auth-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="odeme-auth-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Auth → biletleme → capture · başarısızsa void</text>
  <line x1="40" y1="96" x2="628" y2="96" stroke="#5D7789" stroke-width="1.3" marker-end="url(#odeme-auth-arw)"/>
  <line x1="72" y1="76" x2="72" y2="116" stroke="#186399" stroke-width="2"/>
  <text x="72" y="68" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">OTORİZASYON</text>
  <text x="72" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tutar bloke</text>
  <text x="72" y="146" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">para çıkmadı</text>
  <line x1="270" y1="76" x2="270" y2="116" stroke="#B07D20" stroke-width="2"/>
  <text x="270" y="68" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">BİLETLEME</text>
  <text x="270" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bilet no üretilir</text>
  <line x1="452" y1="76" x2="452" y2="116" stroke="#25804C" stroke-width="2"/>
  <text x="452" y="68" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">CAPTURE</text>
  <text x="452" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tahsilat başlar</text>
  <rect x="72" y="88" width="452" height="16" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="298" y="100" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">auth geçerlilik penceresi — tipik olarak 7 gün</text>
  <line x1="524" y1="80" x2="524" y2="112" stroke="#B23B3B" stroke-width="2" stroke-dasharray="4 3"/>
  <text x="590" y="98" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">pencere kapanır</text>
  <path d="M270 116 L270 168 L150 168" fill="none" stroke="#B23B3B" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#odeme-auth-red)"/>
  <text x="300" y="160" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">bilet kesilemedi</text>
  <rect x="20" y="150" width="126" height="40" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="83" y="168" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">VOID</text>
  <text x="83" y="183" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ekstrede iz yok</text>
  <rect x="20" y="206" width="308" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="174" y="226" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Neden önce auth?</text>
  <text x="174" y="243" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ödeme kabiliyeti kanıtlanır, para çıkmaz</text>
  <text x="174" y="259" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iptal ucuz ve görünmezdir; iade pahalı ve görünürdür</text>
  <rect x="340" y="206" width="306" height="66" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="493" y="226" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Süresi dolmuş auth ile capture</text>
  <text x="493" y="243" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">reddedilebilir: koltuk gitti, para alınamadı</text>
  <text x="493" y="259" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">geçerlilik bitişi kayıtta alan olarak izlenir</text>
</svg>
```

### Ödeme durum makinesi: tek alan değil, tanımlı bir graf

Ödeme kaydının hayatı bir **durum makinesidir**. Yani kayıt, tanımlı durumlar arasında tanımlı kurallarla dolaşır. Bu makineyi açıkça modellemek ile sadece ödeme çağrısı yapmak arasındaki fark, ödeme sistemi yazmak ile ödeme sistemi kullanmak arasındaki farktır. Tipik ana hat:

`CREATED → PENDING_AUTH → AUTHORIZED → CAPTURED → SETTLED`

Yan dallar da modelin birinci sınıf parçasıdır: `DECLINED` (issuer reddetti), `VOIDED` (auth iptal edildi), `REFUNDED` (tam veya **kısmi** iade), `CHARGEBACK` (kart hamili itiraz etti — `252`).

Bir de sonucu bilinmeyen işlemler vardır. PSP zaman aşımına düştüğünde işlemin gerçekleşip gerçekleşmediği **bilinmez**. Bu yüzden `PENDING_AUTH` ve gerekirse `UNKNOWN` birer gerçek durumdur; geçici bir rahatsızlık değildir.

Dört tasarım kuralı bu modeli üretime dayanıklı kılar:

1. **Geçişler açıkça tanımlanır.** Hangi durumdan hangisine geçilebileceği bir tablo/graf olarak yazılır. `SETTLED` durumundan `AUTHORIZED` durumuna dönülemez; tanımsız geçiş isteği sessizce yok sayılmaz, **istisna fırlatır**. Yanlış giden bir işlem "durumu geri alarak" değil, **yeni bir işlemle** (void, refund, ters kayıt) düzeltilir.
2. **Her geçiş yalnızca eklenerek yazılır (append-only).** Durum alanının üzerine yazmak yeterli değildir. Şu bilgilerin hepsi **yeni bir satır** olarak eklenir: kim, ne zaman, hangi kanaldan, hangi dış referansla (auth kodu, PSP işlem kimliği) geçirdi. Bu tarihçe üç işin birden temelidir: denetim, chargeback savunması ve üretimdeki hata ayıklama. İstekleri uçtan uca birbirine bağlayan izleme kimliği (correlation-id) de tüm servislerde taşınır (`104`, `193`).
3. **Durum enum'u metin olarak saklanır.** Sıra numarasına (ordinal) göre saklarsan, enum'a bir eleman eklendiğinde geçmiş kayıtların anlamı değişir. Para üzerinde bu affedilmez bir hatadır (`27`, `81`).
4. **Zaman aşımı durumları da modele dahildir.** `PENDING_AUTH` içinde N dakikadan uzun kalan işlem bir arka plan işiyle taranır. Akıbeti PSP'nin sorgulama (inquiry) ucundan netleştirilir. Netleşmiyorsa son sözü mutabakat dosyası söyler (`252`).

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Odeme durum makinesi ana hat ve yan dallar ile gecersiz gecis kurali">
  <defs>
    <marker id="odeme-sm-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="odeme-sm-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ödeme durum makinesi: ana hat ve yan dallar</text>
  <rect x="12" y="46" width="110" height="42" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="67" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">CREATED</text>
  <rect x="142" y="46" width="110" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="197" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">PENDING_AUTH</text>
  <text x="197" y="80" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">zaman aşımı taranır</text>
  <rect x="272" y="46" width="110" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="327" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">AUTHORIZED</text>
  <rect x="402" y="46" width="110" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="457" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">CAPTURED</text>
  <rect x="532" y="46" width="112" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="588" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">SETTLED</text>
  <path d="M122 67 L138 67" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#odeme-sm-arw)"/>
  <path d="M252 67 L268 67" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#odeme-sm-arw)"/>
  <path d="M382 67 L398 67" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#odeme-sm-arw)"/>
  <path d="M512 67 L528 67" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#odeme-sm-arw)"/>
  <path d="M197 88 L197 128" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#odeme-sm-red)"/>
  <rect x="142" y="132" width="110" height="38" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="197" y="156" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">DECLINED</text>
  <path d="M327 88 L327 128" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#odeme-sm-red)"/>
  <rect x="272" y="132" width="110" height="38" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="327" y="156" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">VOIDED</text>
  <path d="M457 88 L457 128" fill="none" stroke="#B07D20" stroke-width="1.6" marker-end="url(#odeme-sm-red)"/>
  <rect x="402" y="132" width="110" height="38" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="457" y="150" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">REFUNDED</text>
  <text x="457" y="163" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tam / kısmi</text>
  <path d="M588 88 L588 128" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#odeme-sm-red)"/>
  <rect x="532" y="132" width="112" height="38" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="588" y="150" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">CHARGEBACK</text>
  <text x="588" y="163" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">252</text>
  <path d="M588 46 L588 34 L327 34 L327 42" fill="none" stroke="#B23B3B" stroke-width="1.6" stroke-dasharray="5 3"/>
  <text x="458" y="30" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">✕ tanımsız geçiş — istisna fırlatır</text>
  <rect x="16" y="192" width="628" height="44" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="212" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Her geçiş append-only olay kaydıdır: kim · ne zaman · hangi kanal · dış referans (auth kodu, PSP işlem no)</text>
  <text x="330" y="229" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">correlation-id uçtan uca taşınır (104, 193) — denetim, chargeback savunması ve hata ayıklama aynı izden beslenir</text>
  <rect x="16" y="246" width="628" height="60" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="330" y="266" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Durum enum'u metin olarak saklanır (27, 81) — ordinal saklamak geçmiş kayıtların anlamını değiştirir</text>
  <text x="330" y="284" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Yanlış giden işlem "durumu geri alarak" değil, YENİ bir işlemle düzeltilir: void, refund, ters kayıt</text>
  <text x="330" y="299" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">PSP zaman aşımında sonuç bilinmez: sorgulama ucu netleştirir, netleşmezse mutabakat söyler (252)</text>
</svg>
```

### PSP mi, doğrudan acquirer (VPOS) mi?

Ödeme almanın iki yolu vardır ve seçim tamamen bir ödünleşmedir.

| | PSP üzerinden | Doğrudan acquirer (VPOS) |
|---|---|---|
| Komisyon | daha yüksek | daha düşük |
| Geliştirme yükü | tek API, hazır 3DS/tokenizasyon/taksit | her banka için ayrı entegrasyon |
| Sertifikasyon | sağlayıcı taşır | kendin geçersin, sürer |
| PCI kapsamı | büyük ölçüde daralır (`251`) | genişler, denetim yükü sende |
| Çoklu banka / failover | hazır gelir | kendin kurarsın |
| Pazarlık gücü | sağlayıcının koşulları | hacim büyüdükçe lehine |

Pratikte büyük havayolları **hibrit** çalışır: ana kanalda bir PSP ya da ödeme orkestratörü, hacimli pazarlarda doğrudan banka anlaşmaları. Bu yüzden mülakattaki doğru cevap "PSP daha iyidir" ya da "VPOS daha ucuzdur" değildir; doğru cevap **kararı geri dönülebilir kılmaktır**.

Bunun somut karşılığı, sağlayıcıyı bir **kapı (port) arkasına almaktır** (`136`). İş kodu `PaymentGateway` gibi kendi terimlerinle yazılmış bir arayüzü çağırır: `authorize`, `capture`, `void`, `refund`, `inquiry`. Her sağlayıcı bu arayüzün bir **uyarlayıcısıdır (adapter)**. Sağlayıcının veri sınıfları (DTO), hata kodları ve tuhaflıkları uyarlayıcının içinde kalır; iş alanına (domain) sızmaz.

İkinci bir sağlayıcı eklemek, yeni bir uyarlayıcı yazmak demektir. İş kuralları, durum makinesi ve mutabakat mantığı hiç değişmez. Sağlayıcının istemci sınıflarını doğrudan servis katmanında kullanmak ise kısa vadede en hızlı görünen karardır. Uzun vadede en pahalıya mal olan da odur: bir yıl sonra sağlayıcı değişimi bir "entegrasyon" değil, bir "yeniden yazım" olur.

### Mülakatta ne sorulur?

- **"Bir kart ödemesinde para nasıl akar, aktörler kim?"** — sözlü cevap iskeleti (4 adım): (1) "Kart hamili, merchant, acquirer, kart şeması, issuer — kararı **issuer** verir." (2) "Mesaj yolculuğu saniyeler sürer, sonunda otorizasyon kodu vardır, para yoktur." (3) "Para yolculuğu dönemseldir: clearing ve settlement ile issuer'dan acquirer'a, oradan merchant'a; T+1/T+2 (`252`)." (4) "Komisyon kesildiği için işlem tutarı ile hesaba geçen tutar farklı kavramlardır."
- **"Authorization ile capture farkı nedir, havayolunda neden önemli?"** Auth blokedir, capture tahsilattır; bilet kesilene kadar para çekilmez, kesilemezse void edilir ve müşteri ekstresinde iz kalmaz. Auth'un geçerlilik penceresi (tipik 7 gün) izlenmelidir.
- **"Void ile refund ne zaman kullanılır?"** Settle olmadan iptal void'dir, sonrası refund'dur; ayrıntı ve maliyet farkı `252`.
- **"Bir ödemenin durumları nelerdir?"** `CREATED → PENDING_AUTH → AUTHORIZED → CAPTURED → SETTLED`, yan dallar `DECLINED`, `VOIDED`, `REFUNDED`, `CHARGEBACK`; geçişler tanımlıdır, tanımsız geçiş istisna fırlatır, her geçiş append-only yazılır, enum metin saklanır (`27`).
- **"İşlem reddedildi, kim reddetti?"** Issuer. Acquirer ve şema mesajı taşır; şema kural koyar ama bakiye kararı vermez. Bu üçünü karıştırmak alan bilgisi eksikliğinin en hızlı görünen işaretidir.
- **"PSP mi kullanırsın, doğrudan entegrasyon mu?"** Ödünleşmeyi anlat (komisyon vs geliştirme + sertifikasyon + PCI kapsamı), sonra asıl cevabı ver: sağlayıcı bir port arkasına alınır (`136`), değişim yeni bir adaptördür.
- **"Para çekildi ama bilet kesilemedi, ne yaparsın?"** Saga telafisi (`126`): ara durum modelde açıkça bulunur, void/refund otomatik tetiklenir, adım olayları outbox ile yayınlanır (`116`), her adım idempotency anahtarıyla korunur (`130`), zaman aşımı işi cevapsız kalanları toplar.

### Tek cümlede

Kart ödemesi tek bir olay değildir. İki yolculuğun üst üste binmesidir: saniyeler süren **mesaj yolculuğu** (kararı issuer verir) ve günler süren **para yolculuğu** (clearing/settlement, komisyon kesintili). Havayolu bu ayrımı **auth → biletleme → capture, olmazsa void** sırasıyla yönetir. Sistemdeki karşılığı bir **ödeme durum makinesidir**: geçişleri açıkça tanımlı, her adımı yalnızca ekleyerek iz bırakan, enum'unu metin saklayan bir makine. Sağlayıcı ise iş kodunun içinde değil, bir uyarlayıcının arkasında durur.

# odeme-1 | Orta

## Soru
Havayolunun web sitesinde müşteri "öde" düğmesine bastıktan sonra biletleme (ticketing) ayrı bir sistemde yapılıyor ve bazen tamamlanamıyor. Ödeme adımı hangi kurguyla tasarlanmalıdır?

## A
Satın alma anında doğrudan capture yapılır; bilet kesilemezse aynı gün içinde otomatik refund başlatılır ve müşteriye bilgi verilir
🤖 settle olmamış bir işlemin doğru aracı void'dir; gereksiz yere çekilip iade edilen para ekstrede iki satır bırakır, komisyon ve çağrı merkezi maliyeti üretir.

## B
Satın almada otorizasyon alınır, bilet kesilince capture edilir, bilet kesilemezse otorizasyon void edilir
🤖 Doğru: ödeme kabiliyeti önce kanıtlanır, para ancak teslim edilecek ürün kesinleşince çekilir.

## C
Ödeme hiç başlatılmaz; önce bilet kesilir, bilet numarası üretildikten sonra karttan tek adımda tahsilat yapılır
🤖 kartın ödeme kabiliyeti doğrulanmadan envanterden koltuk vermek demektir; auth tam olarak bu riski kapatmak için vardır.

## D
Auth ve capture tek isteğe birleştirilir; bilet kesilemezse tutar müşterinin bir sonraki satın almasında mahsup edilir
🤖 kart şemalarında böyle bir mahsup akışı yoktur; alınan para ya void ya refund ile geri verilir.

## Doğru
B

## Açıklama
Havayolu satışında satın alma anı ile teslim anı **aynı değildir**: müşteri ödeme düğmesine bastığında bilet henüz kesilmemiştir ve biletleme ayrı bir sistemde çalışır. Doğru akış bu yüzden üç hareketlidir: **otorizasyon** ile tutar kartta bloke edilir (para çıkmaz, sadece kullanılamaz hale gelir ve kartın ödeme kabiliyeti kanıtlanır), biletleme başarılıysa **capture** ile tahsilat yapılır, biletleme başarısızsa **void** ile bloke kaldırılır ve müşterinin ekstresinde hiçbir iz kalmaz. "Önce çekelim, olmazsa iade ederiz" yaklaşımı teknik olarak çalışır ama pahalıdır: iade ayrı bir finansal işlemdir, günler sürer, komisyon maliyeti doğurur, ekstrede iki satır bırakır ve müşteri "param neden çekildi" diye çağrı merkezini arar (void/refund ayrımı için `252`). Ödemeyi biletlemeden sonraya bırakmak ise riski ters yöne çevirir: ödeme kabiliyeti doğrulanmamış bir yolcuya envanterden koltuk verilmiş olur ve kart reddedilirse koltuk boşuna satılmıştır. Son şık ise ödeme dünyasında karşılığı olmayan bir mekanizma icat eder; kart şemalarında "sonraki alışverişte mahsup" diye bir akış yoktur — alınan para yalnızca void veya refund ile geri verilir.

# odeme-2 | Orta

## Soru
Grup rezervasyonunda otorizasyon alındı, ancak operasyonel bir sorun nedeniyle biletleme onayı dokuz gün sonra geldi ve ekip şimdi capture etmek istiyor. Doğru tasarım hangisidir?

## A
Bloke, capture yapılana kadar issuer tarafında süresiz durur; işlem dokuzuncu günde de sorunsuz capture edilir, çünkü otorizasyon kartta tutar ayrılmış olduğunu kalıcı olarak işaretler
🤖 otorizasyonun bir geçerlilik penceresi vardır ve kart-mevcut-değil işlemlerde tipik olarak yedi gün konuşulur; pencere kapandığında issuer blokeyi kendiliğinden çözer.

## B
Süresi dolmuş otorizasyon için capture isteği yine gönderilir; reddedilirse müşteriye "kartınızda bir sorun var" mesajı verilip işlem kapatılır, müşteri isterse yeni bir rezervasyon açabilir
🤖 sorun kartta değil süreçtedir; bu davranış müşteriyi yanıltır ve gerçek nedeni (izlenmeyen auth penceresi) gizleyerek sorunun tekrarlamasına yol açar.

## C
Auth alındıktan hemen sonra capture edilip para bilet kesilene kadar merchant hesabında bekletilir; böylece süre sorunu tamamen ortadan kalkar ve tahsilatın gecikmeden yapıldığı garanti altına alınır
🤖 karşılığı henüz verilmemiş bir hizmet için tahsil edilmiş para, iade yükü ve itiraz riski demektir; otorizasyon zaten bu bekleme ihtiyacını karşılamak için vardır.

## D
Otorizasyonun geçerlilik bitişi ödeme kaydında izlenir; pencere dolmadan capture edilemeyecek işlemler için ya yeni bir otorizasyon alınır ya işlem serbest bırakılıp müşteriye yeni ödeme akışı sunulur
🤖 Doğru: auth ömürlüdür ve bu ömür bir alan olarak modellenip proaktif izlenmelidir.

## Doğru
D

## Açıklama
Otorizasyon süresiz değildir: geçerlilik penceresi kart şemasına, işlem tipine ve issuer'a göre değişir ve kart-mevcut-değil işlemlerde **tipik olarak yedi gün** konuşulur. Pencere kapandığında issuer blokeyi kendiliğinden çözer; süresi dolmuş bir auth ile yapılan capture denemesi reddedilebilir ve tam o anda **koltuk satılmış ama para alınamamış** olur. Bu yüzden auth'un geçerlilik bitişi ödeme kaydında bir alandır ve capture edilmemiş otorizasyonları tarayan bir iş vardır: pencere dolmadan ya capture edilir, ya işlem serbest bırakılıp müşteriye yeni bir ödeme akışı sunulur. "Bloke süresizdir" cevabı ürünün temel bir özelliğini bilmemektir. Süresi dolan auth'u yine deneyip müşteriyi "kartınızda sorun var" diye bilgilendirmek daha da kötüdür: yanlış teşhis müşteriye aktarılır, gerçek neden (izlenmeyen pencere) görünmez kalır ve aynı hata her grup rezervasyonunda tekrar eder. Peşinen capture etmek ise otorizasyonun varlık sebebini ortadan kaldırır: karşılığı henüz teslim edilmemiş bir hizmet için elde tutulan para, iade yükü, muhasebe karmaşası ve itiraz riski demektir. Kural olarak, bekleme ihtiyacı olan her akışta beklemeyi **bloke** taşır, tahsilat değil.

# odeme-3 | İleri

## Soru
Bir ödeme kaydı mutabakat sonrası `SETTLED` durumuna geçmişken, operasyon ekranından yanlışlıkla "yeniden otorize et" işlemi tetiklendi ve kayıt `AUTHORIZED` durumuna çekilmek isteniyor. Sistem nasıl davranmalıdır?

## A
Durum makinesinde izinli geçişler açıkça tanımlıdır; `SETTLED → AUTHORIZED` tanımsız bir geçiştir ve istisna fırlatır — düzeltme, yeni bir finansal işlemle (refund/ters kayıt) ve append-only iz bırakılarak yapılır
🤖 Doğru: durum geriye alınmaz; hata, geçmişi değiştirerek değil yeni bir olay ekleyerek düzeltilir.

## B
Durum alanı serbest bırakılır ve hangi geçişin geçerli olduğuna servis metodu çalışma anında karar verir; böylece operasyon esnek kalır
🤖 geçiş kuralı tek bir yerde tanımlı değilse her yeni metot kendi kuralını icat eder; para üzerinde bu, birbiriyle çelişen durum geçmişleri üretir.

## C
Kayıt doğrudan güncellenerek durum `AUTHORIZED` yapılır ve önceki değer üzerine yazılır; tek geçerli durumun bulunması ekranlarda karışıklığı önler
🤖 üzerine yazmak denetim izini yok eder; chargeback savunmasında ve düzenleyici incelemede "o an hangi durumdaydı" sorusu cevapsız kalır.

## D
Durum enum'u sıra numarasına (ordinal) göre saklanıp geçişler bu sayılar üzerinden karşılaştırılır; geriye geçiş küçük sayıya dönüş demektir ve otomatik engellenir
🤖 ordinal saklamak enum'a bir eleman eklendiğinde tüm geçmiş kayıtların anlamını değiştirir (27); üstelik geçiş kuralı sayı sıralamasına indirgenemez.

## Doğru
A

## Açıklama
Ödeme kaydının hayatı bir **durum makinesidir** ve izinli geçişler açık bir graf olarak tanımlanır: `CREATED → PENDING_AUTH → AUTHORIZED → CAPTURED → SETTLED` ana hattı ile `DECLINED`, `VOIDED`, `REFUNDED`, `CHARGEBACK` yan dalları. `SETTLED` durumundaki bir işlem takas edilmiş, yani parası fiilen el değiştirmiş bir işlemdir; oradan `AUTHORIZED` durumuna dönmek fiziksel olarak anlamsızdır. Tanımsız geçiş sessizce yok sayılmaz, **istisna fırlatır** — yanlış tetiklenen bir operasyon işlemi böylece kaynakta durur. Düzeltme, geçmişi değiştirerek değil **yeni bir finansal işlemle** yapılır: gerekiyorsa refund veya ters kayıt açılır ve bu da kendi durumuyla, kendi izleriyle kaydedilir. Geçiş kuralını servis metotlarının içine dağıtmak, her yeni akışın kendi kuralını icat etmesi demektir; birkaç sürüm sonra aynı kayıt üzerinde birbiriyle çelişen geçmişler oluşur. Durumu doğrudan güncelleyip eskisini silmek en zararlı seçenektir: her geçişin kim, ne zaman, hangi kanaldan, hangi dış referansla (auth kodu, PSP işlem kimliği) yapıldığı **append-only** yazılmalıdır (`104`, `193`) — bu tarihçe hem denetimin hem chargeback kanıt zincirinin temelidir (`252`). Enum'u ordinal saklamak ise ayrı bir klasik hatadır (`27`): listeye bir durum eklendiğinde geçmiş kayıtlar sessizce başka bir anlama gelir.

# odeme-4 | İleri

## Soru
Havayolu yeni bir pazarda kart ödemesi almaya başlayacak. Ekip küçük, PCI denetim yükü istenmiyor; ancak hacim büyüdüğünde doğrudan acquirer anlaşmasına geçme ihtimali var. Doğru mimari karar hangisidir?

## A
PSP seçilir ve sağlayıcının istemci sınıfları ile DTO'ları doğrudan iş servislerinde kullanılır; ara katman gereksiz bir soyutlama maliyetidir
🤖 sağlayıcının modeli domain'e sızarsa geçiş bir entegrasyon değil yeniden yazım olur; ödeme sağlayıcıları da tam olarak değişen bileşendir.

## B
Baştan doğrudan acquirer (VPOS) entegrasyonu yapılır; komisyon en büyük maliyet kalemi olduğu için diğer tüm maliyetleri fazlasıyla karşılar
🤖 sertifikasyon, her banka için ayrı entegrasyon ve genişleyen PCI kapsamı doğrudan entegrasyonun görünmeyen ve küçük ekipler için baskın maliyetidir.

## C
PSP ile başlanır, ancak sağlayıcı kendi terimlerimizle yazılmış bir port arkasına alınır; VPOS'a veya ikinci bir sağlayıcıya geçiş yeni bir adaptör yazmak olur, iş kodu değişmez
🤖 Doğru: karar bugünün koşuluna göre verilir ama geri dönülebilir tutulur.

## D
İki sağlayıcı aynı anda entegre edilip her istekte biri seçilerek yük paylaştırılır; böylece bağımlılık baştan dağıtılmış olur
🤖 iki sağlayıcı iki mutabakat akışı, iki tokenizasyon kasası ve iki hata sözlüğü demektir; küçük bir ekip için bu maliyet, çözdüğü sorundan büyüktür.

## Doğru
C

## Açıklama
PSP ile doğrudan acquirer entegrasyonu arasındaki seçim bir ödünleşmedir: PSP daha yüksek komisyon karşılığında tek API, hazır 3DS akışı, tokenizasyon, taksit, çoklu banka yönlendirmesi ve **büyük ölçüde daralan PCI kapsamı** sunar (`251`); doğrudan entegrasyon komisyonu düşürür ama sertifikasyon, her banka için ayrı geliştirme ve genişleyen denetim yükü getirir. Küçük bir ekip ve yeni bir pazar için PSP makul başlangıçtır — fakat mülakatta değerli olan bu tercih değil, **kararı geri dönülebilir kılan tasarımdır**. Sağlayıcı, iş kodunun kendi diliyle yazılmış bir port arkasına alınır (`136`): `authorize`, `capture`, `void`, `refund`, `inquiry`. Her sağlayıcı bu portun bir adaptörüdür; onun DTO'ları, hata kodları ve tuhaflıkları adaptörün içinde kalır. Böylece hacim büyüyüp VPOS'a geçildiğinde durum makinesi, mutabakat mantığı ve iş kuralları hiç değişmez. Sağlayıcının sınıflarını doğrudan servis katmanında kullanmak kısa vadede en hızlı, uzun vadede en pahalı karardır; ödeme sağlayıcısı zaten sistemin **en sık değişen** bileşenidir. Baştan VPOS'a gitmek küçük ekip için görünmeyen maliyetleri hafife alır. İki sağlayıcıyı gün bir kullanmak ise çözdüğünden fazla sorun üretir: iki mutabakat akışı, iki kasa, iki hata sözlüğü ve iki kez sertifikasyon — çoklu sağlayıcı, hacim ve süreklilik ihtiyacı doğduğunda ve port zaten hazırken anlamlıdır.

# odeme-5 | İleri

## Soru
Üretimde ödeme redlerinde artış var; loglarda çoğunlukla "yetersiz bakiye" karşılığı bir red kodu görünüyor. Ekip PSP'yi ve acquirer'ı suçluyor. Bu tablonun doğru okuması nedir?

## A
Red kararı acquirer'a aittir; acquirer merchant'ın bankası olduğu için işlemi ilk gören ve müşterinin limitini kontrol eden taraftır, dolayısıyla red oranı acquirer değiştirilerek düzeltilir
🤖 acquirer üye işyeri tarafındadır; kart hamilinin limiti, bakiyesi ve risk kuralları issuer'ın defterindedir.

## B
Onay/red kararını kartı veren banka (issuer) verir; acquirer ve şema mesajı taşır. Merchant tarafında yapılacak iş red kodlarını doğru sınıflandırmak ve yeniden deneme politikasını buna göre kurmaktır
🤖 Doğru: kararın sahibi issuer'dır; merchant'ın kontrol edebildiği şey kodun yorumlanması ve kullanıcıya verilen mesajdır.

## C
Kart şeması işlemi onaylar veya reddeder; Visa/Mastercard tarafındaki risk kuralları bakiye yetersizliğini de bu aşamada yakalar ve karar issuer'a hiç sorulmadan verilir
🤖 şema yönlendirir ve kural koyar, uyuşmazlıkta karar mercii olur; ancak bakiye ve limit kararını vermez.

## D
Red alan her işlem, geçici bir sorun olma ihtimaline karşı otomatik olarak arka planda üç ile beş kez yeniden denenir; denemeler arasına kısa bekleme konur ve müşteriye yalnızca son sonuç gösterilir
🤖 yetersiz bakiye gibi kalıcı redlerde tekrar denemek kart deneme davranışına benzer, onay oranını düşürür ve rate limiting gerektiren bir desen üretir (153).

## Doğru
B

## Açıklama
Aktör rollerini karıştırmak, ödeme mülakatında alan bilgisi eksikliğinin en hızlı görünen işaretidir. **Issuer** kartı veren bankadır ve limit, bakiye, kart durumu ile kendi risk kuralları onun defterinde olduğu için **onay/red kararını o verir**. **Acquirer** satıcının bankasıdır: işlemi şemaya taşır, parayı toplar, merchant'a aktarır. **Kart şeması** mesajı yönlendirir, kuralları ve ücretleri koyar, uyuşmazlıkta karar mercii olur — ama tek bir işlemin bakiye kararını vermez. Dolayısıyla "yetersiz bakiye" redlerinden PSP'yi veya acquirer'ı sorumlu tutmak yanlış adrese bakmaktır. Merchant tarafında yapılabilecek iş vardır ve mülakatta beklenen cevap odur: red kodlarını **kalıcı** (yetersiz bakiye, kart kapalı, çalıntı bildirimli) ve **geçici** (sistem hatası, zaman aşımı) diye sınıflandırmak, kullanıcıya anlamlı mesaj vermek (alternatif kart/ödeme yöntemi önermek), yeniden deneme politikasını yalnızca geçici sınıfa uygulamak ve red oranını bir metrik olarak izlemek. Her redi körlemesine üç-beş kez tekrar denemek ise ayrı bir risktir: davranış kart deneme saldırısının imzasına benzer, issuer ve şema tarafında onay oranını düşürür ve kendi tarafında rate limiting gerektirir (`153`, ayrıntı `251`).

# odeme-6 | Uzman

## Soru
Bu üründe PSP gecikmeli capture desteklemediği için tahsilat biletlemeden hemen önce yapılıyor: capture başarılı döndü, ardından biletleme servisi cevap vermedi. Müşterinin parası alınmış, elinde bilet yok. Sistem bu durumu hangi tasarımla yönetmelidir?

## A
Ödeme ve biletleme adımları tek bir dağıtık işlemde (2PC/XA) birleştirilir; biletleme düşerse koordinatör ödemeyi de geri alır ve iki sistem arasında kalıcı bir tutarsızlık hiç oluşmaz
🤖 harici PSP bir XA katılımcısı değildir ve kullanıcı ödeme ekranında geçen dakikalar boyunca kilit tutulamaz — saganın var olma sebebi budur (126).

## B
Capture, biletleme sonucu beklenmeden yapılır ve başarısız kayıtlar gün içinde operasyonun elle iade ettiği bir listeye düşer; vaka sayısı düşük olduğu için insan gücüyle kapatmak yeterli olur
🤖 parası alınmış ama hizmeti verilmemiş müşteriyi elle sürece bırakmak hem itiraz hem çağrı merkezi maliyeti üretir; telafi otomatik olmalıdır.

## C
Biletleme başarısız olduğunda ödeme kaydı silinir ve müşteriden işlemi baştan yapması istenir; böylece tutarsız kayıt hiç oluşmaz ve ekranlarda yarım kalmış hiçbir işlem görünmez
🤖 finansal kayıt silinmez; para karttan çekilmiştir ve kaydı yok etmek mutabakatta karşılıksız bir hareket bırakır.

## D
Modelde açık bir ara durum bulunur (ör. `CAPTURED_TICKET_PENDING`); biletleme düşerse saga telafisi void/refund tetikler, zaman aşımı işi cevapsız kalan istekleri toplar, tüm adımlar idempotency anahtarıyla korunur
🤖 Doğru: ara durum görünür kılınır, telafi otomatik koşar ve tekrar gelen istekler yan etkisiz kalır.

## Doğru
D

## Açıklama
"Para çekildi ama bilet kesilemedi", ödeme mülakatlarının klasik tasarım sorusudur ve cevabı üç parçalıdır. Birincisi, **ara durum modelde açıkça bulunur**: `CAPTURED_TICKET_PENDING` gibi bir durum, "ödeme tamam, teslimat bekliyor" gerçeğini görünür kılar; bu durum olmadan sistem sorunu ancak müşteri arayınca öğrenir. İkincisi, akış bir **sagadır** (`126`): ödeme ve biletleme farklı sistemlerde ayrı yerel işlemlerdir; biletleme düşerse telafi işlemi (void veya refund) otomatik koşar, adım olayları veritabanı yazımıyla aynı yerel işlemde outbox'a yazılıp yayınlanır (`116`) ve tüm uçlar idempotency anahtarıyla korunur (`130`) — çünkü telafinin kendisi de tekrar tetiklenebilir. Üçüncüsü, **zaman aşımı işi** sigortadır: hiç cevap dönmeyen biletleme istekleri bir işle taranır, PSP ve biletleme tarafının sorgulama uçlarından akıbet netleştirilir, netleşmiyorsa mutabakat son sözü söyler (`252`). 2PC/XA cevabı eleyicidir: harici PSP XA'ya katılmaz ve kullanıcının ödeme ekranında geçirdiği dakikalar boyunca kilit tutmak sistemi kilitler. Elle düzeltme listesi, parası alınmış ve hizmeti verilmemiş müşteriyi insan hızına bırakır — bu durum chargeback'in birinci sebebidir. Kaydı silmek ise en zararlısıdır: para gerçekten çekilmiştir, kaydı yok etmek mutabakatta karşılıksız bir hareket ve açıklanamayan bir fark bırakır.
