---
slug: odeme-guvenligi-pci-3ds
baslik: "Ödeme Güvenliği ve Uyum: 3D Secure, PCI-DSS, Tokenizasyon ve Fraud"
kademe: İleri
sira: 251
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir otelin vestiyerini düşün. Paltonu bırakırsın, sana numaralı bir fiş verirler. O fiş cebinde dolaşır; kaybedersen bulan kişi senin paltonu değil, sadece bir karton parçası kazanır — üstelik o fiş yalnızca **o vestiyerde** işe yarar, başka otelde hiçbir anlamı yoktur. Palto ise arkada, kilitli bir odada durur ve o odaya sadece görevli girer. Ödeme güvenliğinin tamamı bu resmin üzerine kuruludur: **gerçek değer kasada durur, sistemde vekili dolaşır**; kasaya girenler ayrıca denetlenir; kapıda ise "bu fişi getiren gerçekten paltonun sahibi mi?" diye soran bir kontrol vardır.

`250` numaralı konu paranın nasıl aktığını anlattı. Bu konu ise aynı akışın üzerine binen dört ayrı yükü anlatır:

- kart hamilinin **doğrulanması** (3D Secure),
- kart verisinin **korunması ve denetimi** (PCI-DSS),
- verinin **vekille değiştirilmesi** (tokenizasyon),
- işlemin **kötü niyetli olup olmadığının** değerlendirilmesi (fraud, yani dolandırıcılık kontrolü).

Mülakatta bu dördü genellikle tek soruda gelir. Ayırt edici cevap, tanımları saymak değildir. Ayırt edici olan şudur: bunların hangisi **teknik** bir konu, hangisi **ticari** bir konu, hangisi **bilinçli bir iş kararı**? Havacılık bu konunun doğal laboratuvarıdır: tutarlar yüksek, teslimat gecikmeli, iptal ve itiraz yoğundur. Çalıntı kartla alınan son dakika bileti ise klasik bir dolandırıcılık desenidir.

### Temel kavramlar

- **PAN (primary account number — kart numarası):** kartın üzerindeki numara; PCI dünyasının koruduğu ana veridir. Çoğu kartta 16 hanedir ama standart 13-19 hane arasına izin verir (American Express 15 hanedir) — alanı sabit 16 karaktere göre tasarlamak klasik bir entegrasyon hatasıdır.
- **CVV/CVC (card verification value — güvenlik kodu):** kartın arkasındaki üç haneli kod (American Express'te dört hanelidir ve **ön yüzdedir**); doğrulamada kullanılır, **saklanması istisnasız yasaktır**.
- **3D Secure:** kart hamilinin işlem sırasında **issuer** tarafından doğrulanması; üç domain issuer, acquirer ve kart şemasıdır.
- **liability shift (sorumluluk devri):** 3DS ile doğrulanmış işlemde fraud kaynaklı chargeback sorumluluğunun merchant'tan issuer'a geçmesi.
- **frictionless / challenge:** 3DS2'de kullanıcıya hiç soru sorulmadan tamamlanan akış / OTP veya banka uygulaması onayı istenen akış.
- **PCI-DSS:** kart verisini işleyen, ileten veya saklayan sistemlerin uyması zorunlu güvenlik standardı.
- **scope (kapsam):** kart verisine dokunan tüm bileşenler; denetimin yüzeyi budur, hedef onu **daraltmaktır**.
- **tokenizasyon (tokenization):** gerçek PAN'ı anlamsız bir vekille değiştirme; asıl değer bir **vault** (kasa) içinde durur.
- **fraud (dolandırıcılık) skoru:** işlemin kötü niyetli olma ihtimalini kural motoru ve modelle sayısallaştıran değer.
- **card testing (kart deneme):** çalıntı kart listesini küçük tutarlı denemelerle sınayarak hangilerinin geçerli olduğunu bulma saldırısı.

### 3D Secure ve 3DS2: asıl mesele güvenlik değil, sorumluluk

3D Secure'ı "SMS'le gelen kod" diye anlatan aday konuyu yüzeyde bırakır. Doğrulama mekanizması işin görünen yüzüdür; **var olma sebebi ticaridir.**

Bir işlem 3DS ile doğrulanmış olsun. Kart hamili sonradan "bu işlemi ben yapmadım" diyerek itiraz ederse ne olur? Dolandırıcılık kaynaklı itirazın (chargeback) sorumluluğu **merchant'tan issuer'a geçer**. Buna **sorumluluk devri (liability shift)** denir. Yani 3DS dolandırıcılığı sıfırlayan bir kalkan değildir. 3DS, dolandırıcılığın **faturasını kimin ödeyeceğini** değiştiren bir sözleşmedir.

Uçak bileti yüksek tutarlı, iptal ve itiraz oranı yüksek bir üründür. Böyle bir üründe bu sorumluluk devri doğrudan bir bilanço kalemidir. Havayollarında 3DS'in fiilen standart olmasının sebebi de budur.

3DS'in ilk sürümü ciddi bir sorun üretiyordu. Her işlemde kullanıcı bir doğrulama sayfasına yönleniyor, mobilde akış kırılıyor, sepetler terk ediliyordu. Yani güvenlik kazancı **satışa dönüşme oranındaki kayıpla** ödeniyordu.

3DS2 bu ödünleşmeyi risk bazlı hale getirdi. Merchant, işlem hakkında zengin bir bağlam gönderir: cihaz bilgisi, geçmiş davranış, tutar, teslimat deseni. Issuer bunu kendi risk motorunda değerlendirir ve iki yoldan biri seçilir.

- **Sürtünmesiz akış (frictionless):** risk düşükse kullanıcıya **hiçbir şey sorulmaz**. Doğrulama arka planda tamamlanır ve sorumluluk devri yine de geçerlidir. Kullanıcı 3DS'ten geçtiğini fark bile etmez.
- **Sorgulu akış (challenge):** risk yüksekse SMS kodu (OTP) veya banka uygulaması onayı istenir. Kullanıcıyı yoran bu adım burada bilinçli olarak kabul edilir.

Bunun mühendislik sonucu nettir. "3DS'i kapatalım, satış artsın" önerisi 3DS2 dünyasında büyük ölçüde geçersizdir. Çünkü düşük riskli işlemler zaten sürtünmesiz geçmektedir. Kapatmanın karşılığı satış kazancı değil, **itiraz sorumluluğunu geri almaktır**. Ters uç da yanlıştır: tüm işlemlere sorgu dayatmak, 3DS2'nin getirdiği asıl yeniliği çöpe atar ve 3DS1 davranışına geri döner.

Akış tarafında iki teknik ayrıntı mülakatta işe yarar. Birincisi, 3DS **akışta bir dallanma üretir**. Sorgu çıktığında kullanıcı issuer tarafına yönlenir ve geri döner. Bu yüzden ödeme durumu modelinde "3DS bekleniyor" gerçek bir ara durumdur ve zaman aşımıyla yönetilir (`250`). İkincisi, 3DS sonucu işlemin **kanıt zincirine** yazılır: doğrulama durumu ve ilgili referanslar kaydedilir. İtiraz savunmasında ilk sorulan şey budur (`252`).

```svg
<svg viewBox="0 0 660 326" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="3D Secure 2 akisi: issuer risk degerlendirmesi, frictionless ve challenge dallari, liability shift">
  <defs>
    <marker id="pci-3ds-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
    <marker id="pci-3ds-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="pci-3ds-amb" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B07D20"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">3DS2: risk bazlı doğrulama ve liability shift</text>
  <rect x="10" y="42" width="148" height="52" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.5"/>
  <text x="84" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kart hamili</text>
  <text x="84" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ödeme formu</text>
  <rect x="174" y="42" width="148" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="248" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Merchant · PSP</text>
  <text x="248" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bağlam verisini gönderir</text>
  <rect x="338" y="42" width="148" height="52" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="412" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Kart şeması</text>
  <text x="412" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yönlendirir · kural koyar</text>
  <rect x="502" y="42" width="148" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="576" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Issuer</text>
  <text x="576" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">DOĞRULAYAN TARAF</text>
  <path d="M158 62 L170 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#pci-3ds-arw)"/>
  <path d="M322 62 L334 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#pci-3ds-arw)"/>
  <path d="M486 62 L498 62" fill="none" stroke="#186399" stroke-width="1.8" marker-end="url(#pci-3ds-arw)"/>
  <path d="M576 94 L576 130 L486 130" fill="none" stroke="#186399" stroke-width="1.6" marker-end="url(#pci-3ds-arw)"/>
  <rect x="180" y="108" width="300" height="46" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="128" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Issuer risk değerlendirmesi</text>
  <text x="330" y="145" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">cihaz · davranış · tutar · teslimat deseni</text>
  <path d="M330 154 L330 172 L175 172 L175 182" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#pci-3ds-grn)"/>
  <path d="M330 154 L330 172 L485 172 L485 182" fill="none" stroke="#B07D20" stroke-width="1.7" marker-end="url(#pci-3ds-amb)"/>
  <rect x="40" y="186" width="270" height="56" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="175" y="206" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">FRICTIONLESS — düşük risk</text>
  <text x="175" y="222" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kullanıcıya hiçbir şey sorulmaz</text>
  <text x="175" y="236" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sürtünme yok · liability shift yine geçerli</text>
  <rect x="350" y="186" width="270" height="56" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="485" y="206" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">CHALLENGE — yüksek risk</text>
  <text x="485" y="222" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">OTP veya banka uygulaması onayı</text>
  <text x="485" y="236" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sürtünme bilinçli kabul edilir</text>
  <rect x="20" y="256" width="620" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="277" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">LIABILITY SHIFT — 3DS'in asıl ticari gerekçesi</text>
  <text x="330" y="294" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Doğrulanmış işlemde fraud chargeback sorumluluğu merchant'tan ISSUER'a geçer</text>
  <text x="330" y="309" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">3DS sonucu kanıt zincirine yazılır — itiraz savunmasında ilk sorulan budur (252)</text>
</svg>
```

### PCI-DSS geliştiriciye ne der?

PCI-DSS'i "bir sertifika süreci, hukuk ve altyapı ekibinin işi" sanmak yaygın bir hatadır. Standardın günlük yazılım pratiğine indiği yer çok somuttur ve mülakatta buradan sorulur.

**PAN düz metin saklanmaz ve loglanmaz.** Kart numarası ekrana, hata mesajına, istek/cevap loguna, izleme (tracing) verisine, hata takip sistemine yazılamaz. Gösterim gerektiğinde **maskelenir**: ilk 6 ve son 4 hane açık kalır, ortası gizlenir. İlk 6 hane BIN'dir ve kartın bankasını/tipini söyler; son 4 hane kullanıcının kendi kartını tanımasını sağlar. Ortadaki haneler ne operasyona ne kullanıcıya lazımdır.

**CVV hiçbir koşulda saklanmaz.** Bu maddede "şifreleyerek", "kısa süreliğine", "sadece hata ayıklarken" gibi istisna yoktur. Doğrulama anında kullanılır ve atılır. Bir sistem tasarımında CVV alanının bir veritabanı kolonu olarak belirmesi tek başına eleyici bir işarettir.

**Maskeleme tek tek geliştiricinin dikkatine bırakılmaz.** Yüz kişilik bir ekipte "kimse PAN loglamasın" kuralı er ya da geç bir kişide kırılır. Log bir kez yazıldığında da yedeklere, log toplama hattına ve arama indeksine kopyalanmış olur. "Geçici log" diye bir şey yoktur.

Doğru çözüm **merkezi bir garantidir**. Üç somut yolu vardır: log çerçevesine takılan bir maskeleme filtresi; serileştirme katmanında kart alanlarını maskeleyen bir tip (`Money` gibi bir `MaskedPan` değer nesnesi); istek/cevap loglarında alan bazlı karartma (redaksiyon). Böylece kural bireysel disiplinden bağımsız hale gelir (`104`, `190`).

**Asıl strateji kapsamı daraltmaktır (scope reduction).** PCI kapsamı; kart verisini saklayan, işleyen **veya ileten** her bileşendir. Bu listeye uygulama sunucusu, veritabanı, log altyapısı, yedekler ve o ağ segmenti girer; o sistemlere erişen personel bile girer. Kapsam ne kadar genişse denetim, kanıtlama ve sürdürme maliyeti o kadar büyür.

Bu yüzden olgun tasarımın hedefi "kart verisini iyi korumak" değildir. Hedef, **kart verisine hiç dokunmamaktır**. Bunun yolu şudur: kart alanları PSP'nin barındırdığı ödeme sayfası, iframe'i veya alan bazlı bileşenleriyle gömülür. PAN tarayıcıdan **doğrudan PSP'ye** gider, merchant sunucusuna hiç uğramaz. Geriye yalnızca bir token ve son 4 hane döner.

Burada iki yaygın yanlış inanış vardır; ikisi de mülakatta çeldirici olarak kullanılır. Birincisi: "Saklamıyoruz, sadece iletiyoruz, o yüzden kapsam dışıyız." Hayır — **iletmek de kapsamdır**. PAN'ın sunucunun belleğinden geçmesi, bir hata izinde belirebilmesi ve o ağ segmentinde dolaşması kapsamı doğurur.

İkincisi: "TLS var, yeterli." TLS **taşımayı** korur, kapsamı daraltmaz. Veri uçta çözülür ve çözüldüğü yer kapsam içindedir. Kendi şifreleme akışını kurup PAN'ı tarayıcıda şifrelemek de kurtarmaz. Çözme yeteneği sende olduğu sürece veri senin kapsamındadır. Üstelik el yapımı şifreleme ayrı bir risk kalemidir (`190`).

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="PCI kapsam daraltma: kart verisi sunucudan gecen tasarim ile hosted alan ve token kullanan tasarimin karsilastirmasi">
  <defs>
    <marker id="pci-scope-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
    <marker id="pci-scope-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">PCI kapsamı: dokunursan içindesin</text>
  <rect x="12" y="36" width="312" height="212" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6" stroke-dasharray="6 3"/>
  <text x="168" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">ÖNCE: PAN bizim sunucumuzdan geçer</text>
  <rect x="30" y="66" width="276" height="36" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="168" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Tarayıcı — kart formu bizim sayfamızda</text>
  <path d="M168 102 L168 116" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#pci-scope-red)"/>
  <rect x="30" y="120" width="276" height="46" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="168" y="139" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Uygulama sunucusu</text>
  <text x="168" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">PAN bellekte · hata izlerinde belirebilir</text>
  <path d="M168 166 L168 180" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#pci-scope-red)"/>
  <rect x="30" y="184" width="276" height="46" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="168" y="203" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Log altyapısı · yedekler · ağ segmenti</text>
  <text x="168" y="220" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kapsam: fiilen tüm sistem</text>
  <text x="168" y="242" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">"saklamıyoruz, sadece iletiyoruz" kapsamı kaldırmaz</text>
  <rect x="336" y="36" width="312" height="212" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="492" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">SONRA: PAN'a hiç dokunmayız</text>
  <rect x="354" y="66" width="276" height="36" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="492" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Tarayıcı — PSP iframe / hosted alan</text>
  <path d="M492 102 L492 116" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#pci-scope-grn)"/>
  <rect x="354" y="120" width="276" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="492" y="139" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">PSP kasası (vault)</text>
  <text x="492" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">gerçek PAN yalnızca burada durur</text>
  <path d="M492 166 L492 180" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#pci-scope-grn)"/>
  <rect x="354" y="184" width="276" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="492" y="203" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Bizim sistemimiz: token + son 4 hane</text>
  <text x="492" y="220" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kapsam: en dar denetim seviyesi</text>
  <text x="492" y="242" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">marka tutarlılığı stil özelleştirmesiyle çözülür</text>
  <rect x="12" y="258" width="636" height="42" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="277" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">CVV hiçbir koşulda saklanmaz · gösterimde maskeleme ilk 6 + son 4</text>
  <text x="330" y="293" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Maskeleme geliştiricinin dikkatine bırakılmaz: merkezi log filtresi ve serileştirme katmanı garanti eder (104, 190)</text>
</svg>
```

### Tokenizasyon şifreleme değildir

Bu ayrım, ödeme mülakatlarının en sevilen ayrım sorusudur ve doğru cevabı tek cümlede verilebilir: **şifreleme geri döndürülebilir, tokenizasyon değildir.**

**Şifreleme (encryption)** veriyi matematiksel bir dönüşümle okunamaz hale getirir. Ama bu dönüşüm çift yönlüdür: anahtara sahip olan veriyi açar. Böylece güvenlik tamamen **anahtar yönetimine** indirgenir.

Anahtar bir yapılandırma dosyasına sızabilir. Anahtara erişebilen bir uygulama ele geçirilebilir. Bir yedek yanlış yere kopyalanabilir. Bu üç durumun her birinde PAN geri gelir. Üstelik PAN'ı şifreli de olsa kendi veritabanında tutmak seni **PCI kapsamında tutar** — çünkü veriyi saklıyorsundur.

**Tokenizasyon** ise matematiksel bir dönüşüm değil, bir **yerine koymadır**. Gerçek PAN bir kasaya (vault) yazılır. Sistemde onun yerine anlamsız bir dize dolaşır. Token ile PAN arasında hesaplanabilir bir ilişki yoktur. İlişki yalnızca kasadaki eşleme tablosunda vardır ve o tabloya erişim kasanın kendi denetim alanındadır.

Sonuç iki katlıdır. Token'ı çalan kişi kart numarasını **elde edemez**. Ayrıca token tipik olarak o merchant/PSP bağlamına özgüdür; başka bir yerde ödeme yapmak için kullanılamaz.

Bunun ürün karşılığı "kayıtlı kart" özelliğidir. Kullanıcı "kartımı hatırla" dediğinde sistemde saklanan şey PAN değildir. Saklanan şey PSP'nin döndürdüğü **token**, gösterim için son 4 hane ve kart markasıdır. Tekrarlayan ödemede token PSP'ye gönderilir; PAN'ı kasadan PSP çözer ve şemaya iletir. Veri sızıntısı olursa kaybedilen şey "işe yaramaz vekiller listesi" olur.

İki çeldirici burada sık kullanılır. Birincisi "PAN'ı hash'leyelim" önerisidir. Özet (hash) geri döndürülemez; onunla ödeme **alınamaz**. Ayrıca kart numarası uzayı dar ve düzenlidir, tuzsuz bir özet sözlük saldırısına açıktır. Özet yalnızca eşleştirme/tekilleştirme amacıyla ve ayrı kurallarla düşünülür.

İkincisi "her seferinde kullanıcıdan kart bilgisini isteyelim" önerisidir. Güvenlidir ama tokenizasyonun çözdüğü ürün ihtiyacını reddeder. Bu bir cevap değil, sorudan kaçmaktır.

### Fraud kontrolü: akışın neresinde durur, ne karar verir?

Fraud kontrolünün yeri kesindir: **otorizasyondan önce ya da onunla aynı anda.** Sebebi basittir. Auth sonrası kontrol, parayı bloke ettikten sonra karar vermek demektir. O noktada iptal maliyeti doğmuş, müşteri deneyimi bozulmuş ve issuer tarafındaki itibarın zedelenmiştir.

Karar mekanizması iki parçalıdır: **kural motoru** ve **skor**. Kurallar açık ve denetlenebilirdir; skor daha bulanık desenleri yakalar. Tipik girdiler:

- **hız kuralları (velocity):** aynı kartla kısa sürede çok deneme, aynı cihazdan çok kart, aynı IP'den anormal hacim.
- **tutar, ülke ve BIN anomalileri:** kartın ülkesiyle satın alma ülkesinin uyuşmaması, o BIN için alışılmadık tutar, riskli coğrafya.
- **device fingerprint (cihaz parmak izi):** tarayıcı/cihaz özelliklerinden türetilen kimlik; farklı kartların aynı cihazdan geldiğini görünür kılar.
- **hesap ve davranış geçmişi:** hesabın yaşı, geçmiş başarılı işlemler, oturum içi davranış hızı.

Sonuç **üçlüdür** ve bu üçlü olmadan tasarım eksiktir:

1. **Geçir** — akış auth'a devam eder.
2. **Reddet** — işlem hiç başlamaz.
3. **Şüpheli** — bu en önemli kutudur. İşlem ne geçirilir ne reddedilir; **ek doğrulamaya** (3DS sorgusu) veya **elle inceleme kuyruğuna** yönlendirilir. İkili karar veren bir fraud tasarımı ya çok müşteri kaybeder ya çok zarar yazar. Ara kutu tam olarak bu ikilemi çözer.

Havacılıkta dolandırıcılık desenleri sektöre özgüdür. Bunları sayabilmek alan bilgisi sinyali verir (`247`). Tipik desenler şunlardır: **çalıntı kartla son dakika bileti** (dolandırıcı hızlı nakde dönmek ister), **hemen uçulan tek yön rotalar**, yolcu adı ile kart sahibinin birbiriyle ilişkisiz olması, tek rezervasyonda çok sayıda ilgisiz yolcu, alışılmadık saatlerde ve riskli hatlarda yoğunlaşan satışlar. Bunlar ayrıca itiraza (chargeback) dönüşme olasılığı en yüksek işlemlerdir (`252`).

**Kart deneme (card testing)** ayrı bir başlıktır. Dolandırıcılığın merchant'a maliyetini en iyi anlatan örnek budur. Saldırgan elindeki çalıntı kart listesini küçük tutarlarla dener; amacı hangi kartların canlı olduğunu görmektir. Senin ödeme ucun onun için ücretsiz bir kart doğrulama makinesine dönüşür.

"Nasılsa reddediliyor, kayıp yok" demek yanlıştır. Her deneme auth maliyeti üretir ve onay oranını düşürür. Kart şemaları da yüksek red oranını izler ve cezalandırır. Savunma iki katmanlıdır: kart/IP/cihaz bazında **istek hızı sınırlama (rate limiting)** (`153`) ve hız kuralları. Üstüne red oranı ile deneme hızı, alarm eşiği tanımlı birer metrik olur (`104`). Ödeme uçlarında hız sınırlama genel bir API koruması değildir; **alana özgü bir güvenlik kontrolüdür**.

```svg
<svg viewBox="0 0 660 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fraud karar agaci: kural motoru ve skor sonrasi gecir, supheli ve reddet dallari">
  <defs>
    <marker id="pci-frd-gry" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="pci-frd-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="pci-frd-amb" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B07D20"/>
    </marker>
    <marker id="pci-frd-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fraud kararı: üç sonuç, ortadaki kutu şart</text>
  <rect x="250" y="38" width="160" height="38" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.5"/>
  <text x="330" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ödeme isteği</text>
  <path d="M330 76 L330 94" fill="none" stroke="#5D7789" stroke-width="1.7" marker-end="url(#pci-frd-gry)"/>
  <rect x="190" y="98" width="280" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="119" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kural motoru + skor</text>
  <text x="330" y="135" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hız kuralları · tutar/ülke/BIN · device fingerprint</text>
  <path d="M330 150 L330 168 L114 168 L114 180" fill="none" stroke="#25804C" stroke-width="1.7" marker-end="url(#pci-frd-grn)"/>
  <path d="M330 150 L330 180" fill="none" stroke="#B07D20" stroke-width="1.7" marker-end="url(#pci-frd-amb)"/>
  <path d="M330 150 L330 168 L546 168 L546 180" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#pci-frd-red)"/>
  <rect x="14" y="184" width="200" height="62" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="114" y="205" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">GEÇİR</text>
  <text x="114" y="222" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">otorizasyona devam</text>
  <text x="114" y="237" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">akış 250'deki gibi sürer</text>
  <rect x="230" y="184" width="200" height="62" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="330" y="205" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">ŞÜPHELİ</text>
  <text x="330" y="222" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">3DS challenge veya manuel kuyruk</text>
  <text x="330" y="237" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ikili karar veren tasarım eksiktir</text>
  <rect x="446" y="184" width="200" height="62" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="546" y="205" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">REDDET</text>
  <text x="546" y="222" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">işlem hiç başlamaz</text>
  <text x="546" y="237" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">auth denemesi bile yapılmaz</text>
  <rect x="14" y="256" width="632" height="48" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="275" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kontrol auth'tan ÖNCE veya eşzamanlı çalışır — auth sonrası karar, parayı bloke ettikten sonra karar vermektir</text>
  <text x="330" y="292" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Fraud servisi düşerse fallback fail-open mu fail-closed mu? Bu bir İŞ kararıdır; circuit breaker ona göre kurulur (101) · card testing için rate limiting (153)</text>
</svg>
```

### Kritik mimari karar: fraud servisi çökerse ne olacak?

Bu soru, konunun kıdem ayıran noktasıdır ve doğru cevap teknik değildir.

Fraud skorlama servisi cevap vermiyor. Circuit breaker açıldı (`101`). Fallback ne yapacak?

- **Fail-open:** işlemler fraud kontrolü olmadan geçer. Satış devam eder, ama kesinti penceresi boyunca sistem korumasızdır — ve dolandırıcılar bu pencereleri arar.
- **Fail-closed:** işlemler reddedilir. Koruma sürer, ama kısa bir altyapı arızası tüm satışı durdurur; havayolunda bu, dakikalar içinde ölçülebilir gelir kaybıdır.

Buradaki asıl mesele şudur: **bu iki seçenek arasındaki tercih bir mühendislik tercihi değildir.** "Kesinti sırasında ne kadar dolandırıcılık riskini kabul ediyoruz?" sorusunun cevabı risk ve iş birimlerine aittir. Mühendisin işi kararı **almak** değildir; kararı **talep etmek** ve sonra onu kodda sadakatle uygulamaktır. Mülakatta "ben fail-closed yapardım, güvenlik önce gelir" demek yarım cevaptır. Tam cevap, kararın sahibini doğru göstermektir.

Olgun kurgular genelde ikili değil **risk bazlıdır**. Düşük tutarlı ve düşük riskli profildeki işlemler kesinti sırasında geçer. Yüksek tutarlı işlemler ya da bilinen riskli desenlere uyanlar durdurulur veya 3DS sorgusuna zorlanır.

Üstüne üç sigorta konur. Devrenin açık kaldığı süre ve o sırada geçen işlem hacmi metrik olarak izlenir (`104`). Kesinti sırasında geçen işlemler işaretlenip **sonradan skorlanır**. Gerekirse capture öncesi durdurulur (`250`). Beklemeye alıp kuyruklamak ise çalışmaz: müşteri ödeme ekranında bekleyemez ve kuyruk gecikmesi PSP'nin zaman aşımı penceresini de bozar.

Aynı disiplin ödeme akışının diğer dış bağımlılıkları için de geçerlidir. Zaman aşımı, yeniden deneme (retry) ve tekrar edilse de sonucu değişmeyen işleme (idempotency) birlikte düşünülür (`253`). Fraud kararı ile ödeme durumu da aynı iz üzerinden takip edilir (`104`).

### Mülakatta ne sorulur?

- **"3D Secure nedir, neden kullanılır?"** — mekanizmayı bir cümlede geç (issuer doğrulaması), asıl cevabı ver: **liability shift**; doğrulanmış işlemde fraud chargeback sorumluluğu issuer'a geçer. 3DS2'nin risk bazlı akışını (frictionless / challenge) ekle. "Güvenliği artırır" tek başına yüzeysel cevaptır.
- **"3DS dönüşümü düşürmüyor mu?"** 3DS1'de düşürüyordu; 3DS2'de düşük riskli işlemler frictionless geçtiği için sürtünme risk bazlı dağıtılır. Kapatmanın karşılığı dönüşüm kazancı değil, sorumluluğu geri almaktır.
- **"PCI-DSS geliştirici olarak seni nasıl etkiler?"** PAN düz metin saklanmaz/loglanmaz, maskeleme ilk 6 + son 4, CVV hiçbir koşulda saklanmaz, maskeleme merkezi filtreyle garanti edilir (`104`), ve asıl strateji **kapsam daraltmadır** — hosted page/iframe ile kart verisine hiç dokunmamak.
- **"Kart verisini saklamıyoruz, sadece iletiyoruz — kapsam dışıyız değil mi?"** Hayır. İletmek de kapsamdır; PAN'ın sunucudan geçmesi uygulamayı, logları, yedekleri ve ağ segmentini kapsama sokar.
- **"Tokenizasyon ile şifreleme farkı nedir?"** Şifreleme geri döndürülebilir (güvenlik anahtar yönetimine indirgenir), token anlamsız bir vekildir ve gerçek PAN yalnızca kasada durur; token'dan PAN'a matematiksel yol yoktur. "Kayıtlı kart" bu şekilde çalışır; sızıntıda çalınan token işe yaramaz.
- **"Fraud kontrolü akışın neresinde durur?"** Auth'tan önce veya eşzamanlı. Kural motoru + skor; sonuç üçlüdür: geçir / reddet / **şüpheli → 3DS challenge veya manuel kuyruk**. İkili karar veren tasarım eksiktir.
- **"Fraud servisi çökerse ne olur?"** Fail-open mu fail-closed mu sorusunu sor ve bunun bir **iş kararı** olduğunu söyle; circuit breaker fallback'i o karara göre tasarlanır (`101`), kesintide geçen işlemler işaretlenip geriye dönük skorlanır.
- **"Ödeme ucuna dakikada binlerce istek geliyor, çoğu reddediliyor — ne olabilir?"** Card testing saldırısı. Kart/IP/cihaz bazlı rate limiting (`153`), hız kuralları, red oranı ve deneme hızı üzerinde alarm (`104`). "Reddediliyorsa kayıp yok" cevabı eleyicidir: auth maliyeti, düşen onay oranı ve şema cezaları merchant'ta kalır.
- **"Chargeback'e karşı kanıt zinciri nedir?"** 3DS doğrulama sonucu, otorizasyon kodu, PNR, check-in kaydı ve durum geçiş geçmişi; ayrıntı `252`.

### Tek cümlede

Ödeme güvenliği dört ayrı sorunun üst üste binmesidir ve her birinin cevabı farklı cinstendir. **3D Secure** teknik bir doğrulamadan çok ticari bir **sorumluluk devridir**; 3DS2'de kullanıcıyı yoran adım risk bazlı dağıtılır. **PCI-DSS**'in geliştiriciye yansıması PAN'ı hiç loglamamak ve maskelemeyi merkezî bir garantiye bağlamaktır; asıl strateji ise **kapsamı daraltmak**, yani kart verisine hiç dokunmamaktır. **Tokenizasyon** şifrelemenin daha iyisi değil, **başka bir şeydir**: geri dönüşü olmayan bir vekildir ve gerçek PAN kasada kalır. **Fraud** ise geçir/reddet ikilisi değildir; ortasında "şüpheli → ek doğrulama" kutusu olan üçlü bir karardır. Bu servis düştüğünde ne yapılacağı da koda gömülecek bir mühendislik tercihi değil, önceden alınması gereken bir **iş kararıdır**.

# pci-1 | Orta

## Soru
Havayolunun dönüşüm oranlarını iyileştirmek isteyen ürün ekibi, "ödeme adımındaki SMS doğrulaması sepetleri öldürüyor, 3DS'i kapatalım; zaten güçlü bir fraud motorumuz var" diyor. Ödeme ekibi olarak bu öneriyi nasıl değerlendirirsin?

## A
3DS yalnızca ek bir doğrulama katmanıdır; güçlü bir fraud motoru aynı riskli işlemleri zaten yakalayacağı için kapatmak toplam riski artırmaz — iki kontrol aynı işi yaptığından biri kapatıldığında diğeri açığı kapatır ve dönüşüm de serbest kalır
🤖 3DS'in sağladığı şey yakalama değil sorumluluk devridir; fraud motoru ne kadar iyi olursa olsun chargeback faturasını merchant'tan alıp issuer'a geçirmez.

## B
3DS açık tutulur ancak dönüşüm kaybını en aza indirmek için tüm işlemlerde challenge zorunlu kılınıp akış tek tipleştirilir; tek bir akışın hem geliştirilmesi hem test edilmesi kolay olur ve kullanıcı her ödemede aynı adımı gördüğü için sürprizle karşılaşmaz
🤖 3DS2 zaten risk bazlıdır; her işleme challenge dayatmak 3DS1 davranışına geri dönmek ve getirilen asıl yeniliği çöpe atmaktır.

## C
3DS doğrulanmış işlemde fraud kaynaklı chargeback sorumluluğu issuer'a geçer; 3DS2'de düşük riskli işlemler zaten frictionless tamamlandığı için kapatmanın karşılığı dönüşüm kazancı değil sorumluluğu geri almaktır — doğru kurgu 3DS2'yi açık tutup sürtünmeyi risk bazlı yönetmektir
🤖 Doğru: karar dönüşüm ile güvenlik arasında değil, dönüşüm ile chargeback sorumluluğu arasındadır.

## D
3DS düzenleyici zorunluluk olduğu pazarlarda açık, diğer pazarlarda kapalı tutulur; teknik olarak fark yaratmadığı için tercih tamamen uyum meselesidir ve zorunluluk olmayan pazarlarda kapalı tutmak dönüşümü serbest bırakır
🤖 3DS'in etkisi uyumla sınırlı değildir: liability shift ticari bir kazanımdır ve düzenleme zorunlu kılmadığı yerde de yüksek tutarlı satışta değerlidir.

## Doğru
C

## Açıklama
3D Secure'ı "SMS kodu" olarak anlatmak konuyu yüzeyde bırakır. Mekanizma issuer'ın kart hamilini doğrulamasıdır, ama **var olma sebebi ticaridir**: doğrulanmış bir işlemde kart hamili sonradan "bu işlemi ben yapmadım" derse fraud kaynaklı chargeback sorumluluğu merchant'tan **issuer'a geçer**. Yani 3DS dolandırıcılığı sıfırlamaz, faturasını taşıyan tarafı değiştirir; uçak bileti gibi yüksek tutarlı ve itiraz oranı yüksek bir üründe bu doğrudan bir bilanço kalemidir. Fraud motorunun ne kadar iyi olduğu bu devri sağlamaz — iki kontrol farklı işler yapar ve birbirinin yerine geçmez. "Dönüşümü öldürüyor" itirazı ise 3DS1 dünyasının gerçeğidir: 3DS2 merchant'tan zengin bağlam (cihaz, davranış, tutar, teslimat deseni) alır, issuer bunu kendi risk motorunda değerlendirir ve düşük riskli işlemleri kullanıcıya hiçbir şey sormadan **frictionless** tamamlar; sürtünme yalnızca riskli işlemlerde challenge olarak belirir. Bu yüzden 3DS'i kapatmanın karşılığı beklenen dönüşüm kazancı değil, sorumluluğu geri almaktır. Ters uçtaki "her işlemde challenge" tercihi de aynı ölçüde yanlıştır: 3DS2'nin risk bazlı akışını devre dışı bırakıp eski davranışa dönmek demektir. Uyum gerekçesine indirgemek ise eksiktir — düzenleme zorunlu kılmadığı pazarlarda bile liability shift tek başına 3DS'i savunmaya yeter.

# pci-2 | Orta

## Soru
Üretimde ödeme reddi hatalarını ayıklamak için bir geliştirici, ödeme isteğinin gövdesini olduğu gibi loglayan bir satır ekledi ve pull request'e "geçici, sorun çözülünce kaldıracağım" notunu düştü. İnceleme sırasında doğru tepki hangisidir?

## A
İstek gövdesi loglanamaz; PAN ve CVV hiçbir logda bulunamaz ve maskeleme tek tek geliştiricinin dikkatine bırakılmaz — merkezi bir log filtresi/serileştirme katmanıyla garanti altına alınır, hata ayıklama korelasyon kimliği ve maskeli alanlarla yapılır
🤖 Doğru: kural bireysel disipline değil, kaçınılamayan merkezi bir mekanizmaya bağlanır.

## B
Geçici olduğu ve sorun çözülür çözülmez kaldırılacağı için kabul edilebilir; log seviyesi DEBUG yapılıp yalnızca sorunun görüldüğü saatlerde açılır, sorun kapandığında satır aynı gün geri alınır
🤖 log bir kez yazıldığında toplama hattına, arama indeksine ve yedeklere kopyalanır; kalıcı veri üzerinde "geçici" diye bir kategori yoktur.

## C
Loglanabilir, çünkü log diskleri şifreli ve log sistemine erişim yetkilendirilmiş; şifreli ortamda tutulan kart verisi kabul edilebilir bir risktir ve erişim kayıtları da ayrıca tutulmaktadır
🤖 disk şifrelemesi ekrandaki, toplama hattındaki ve yedekteki kopyayı korumaz; ayrıca CVV şifreli bile olsa hiçbir koşulda saklanamaz.

## D
PAN'ın yalnızca son dört hanesi loglanır; CVV ise sorunun tekrarlandığı ilk denemede bir kez loglanıp inceleme bitince ilgili kayıtlar silinir, böylece hem hata ayıklanır hem veri kalıcı olmaz
🤖 maskeleme kısmı doğru yönde ama CVV bir kez bile yazılamaz; silme sözü, yedeklere ve indekslere çoktan yayılmış veriyi geri getirmez.

## Doğru
A

## Açıklama
PCI-DSS'in günlük yazılım pratiğine indiği en somut yer budur: **PAN düz metin loglanmaz, CVV hiçbir koşulda saklanmaz.** İkinci maddede "şifreleyerek", "kısa süreliğine" veya "sadece hata ayıklarken" gibi bir istisna yoktur; CVV doğrulama anında kullanılır ve atılır. Tasarımda CVV'nin bir kolon ya da bir log alanı olarak belirmesi tek başına eleyici bir işarettir. "Geçici log" savunması log altyapısının nasıl çalıştığını bilmemektir: satır bir kez yazıldığında merkezi toplama sistemine gider, arama indeksine yazılır, yedeklenir ve çoğu kurumda uzun saklama süresine tabidir — kodu geri almak veriyi geri almaz. Disk şifrelemesi de kurtarmaz; koruduğu şey çalınan bir diskken, kart verisi log arayüzünde, uyarı e-postasında ve yedek kopyada çözülmüş halde durur. Asıl mesele ise tek bir pull request değil, **kuralın nasıl garanti edildiğidir**: yüz kişilik bir ekipte "kimse PAN loglamasın" talimatı er ya da geç bir yerde kırılır. Doğru tasarım maskelemeyi kaçınılmaz kılar — log çerçevesine takılan bir maskeleme filtresi, kart alanlarını kendi serileştirmesinde maskeleyen bir değer nesnesi, istek/cevap loglarında alan bazlı redaksiyon (`104`, `190`). Hata ayıklama ihtiyacı bununla çelişmez: korelasyon kimliği, maskeli PAN (ilk 6 + son 4), red kodu ve durum geçiş geçmişi bir ödeme sorununu çözmek için fazlasıyla yeterlidir (`250`).

# pci-3 | İleri

## Soru
Ürün ekibi "kartımı hatırla" özelliği istiyor. Ekip, PAN'ı AES-256 ile şifreleyip kendi veritabanında saklamayı, anahtarı da yönetilen bir anahtar servisinde tutmayı öneriyor. Doğru tasarım hangisidir?

## A
AES-256 ve yönetilen anahtar servisi yeterlidir; anahtar donanım destekli bir kasada durduğu için PAN'ı kendi veritabanımızda saklamak tokenizasyonla eşdeğer korumadır
🤖 şifreleme geri döndürülebilirdir: anahtara erişebilen bir uygulama ele geçerse veri açılır; ayrıca PAN'ı saklamak sistemi PCI kapsamı içinde tutar.

## B
PAN hiç saklanmaz ve tekrarlayan her ödemede müşteriden kart bilgileri yeniden istenir; kart verisini saklamamanın tek yolu budur
🤖 güvenli ama ürün gereksinimini reddediyor; tokenizasyon tam olarak "saklamadan hatırlama" ihtiyacı için vardır.

## C
PAN tuzlanmış SHA-256 ile hash'lenip saklanır; tekrarlayan ödemede bu hash PSP'ye gönderilerek işlem başlatılır
🤖 hash geri döndürülemez, dolayısıyla onunla ödeme alınamaz; kart numarası uzayı dar ve yapılı olduğu için tuzsuz hash ayrıca sözlük saldırısına açıktır.

## D
PSP'nin ürettiği token saklanır; gerçek PAN yalnızca PSP kasasında durur, token anlamsız bir vekildir ve ondan PAN'a matematiksel yol yoktur — gösterim için son dört hane ve kart markası tutulur
🤖 Doğru: saklanan şey değerin kendisi değil, yalnızca o bağlamda anlamlı olan vekilidir.

## Doğru
D

## Açıklama
Şifreleme ile tokenizasyon arasındaki fark tek cümlede verilebilir: **şifreleme geri döndürülebilir, tokenizasyon değildir.** Şifrelemede veri matematiksel bir dönüşümle okunamaz hale gelir ama dönüşüm çift yönlüdür; güvenlik tamamen anahtar yönetimine indirgenir. Anahtar bir yapılandırmaya sızarsa, anahtara erişebilen uygulama ele geçirilirse veya bir yedek yanlış yere kopyalanırsa PAN geri gelir. Üstelik şifreli de olsa PAN'ı kendi veritabanında tutmak seni **PCI kapsamında tutar** — veriyi saklıyorsundur ve o veritabanı, yedekleri, erişimi ve denetimi kapsama girer. Tokenizasyon ise matematiksel bir dönüşüm değil bir **yerine koymadır**: gerçek PAN kasaya (vault) yazılır, sistemde onun yerine anlamsız bir dize dolaşır ve token ile PAN arasında hesaplanabilir bir ilişki yoktur; ilişki yalnızca kasadaki eşleme tablosunda vardır. Sızıntı senaryosunda çalınan şey işe yaramaz vekiller listesidir ve token tipik olarak o merchant/PSP bağlamına özgü olduğu için başka bir yerde ödeme almak için kullanılamaz. Ürün karşılığı da tam olarak budur: "kayıtlı kart" özelliğinde saklanan şey PAN değil, token ile gösterim için son dört hane ve kart markasıdır; tekrarlayan ödemede token PSP'ye gönderilir, PAN'ı kasadan o çözer. Kart bilgisini her seferinde yeniden istemek güvenlidir ama sorudan kaçmaktır — tokenizasyon zaten bu ödünleşmeyi ortadan kaldırmak için icat edilmiştir. Hash önerisi ise mekanizmayı yanlış anlamaktır: hash tek yönlüdür, dolayısıyla onunla ödeme başlatılamaz; kart numarası uzayı da dar ve kurallı olduğu için zayıf korunmuş bir hash tersine çevrilebilir.

# pci-4 | İleri

## Soru
Yeni ödeme sayfası tasarlanıyor. UX ekibi kart formunun marka tutarlılığı için kendi sayfamızda kalmasını istiyor; backend ekibi de "PAN'ı hiç saklamayacağız, sadece alıp PSP'ye ileteceğiz, böylece PCI kapsamı dışında kalırız" diyor. Doğru mimari karar hangisidir?

## A
Kart formu kendi sayfamızda tutulur ve PAN hiçbir yerde saklanmaz; saklamadığımız sürece sistem PCI kapsamı dışında kaldığı için ek önlem gerekmez, veri yalnızca istek süresince bellekte kalır ve hemen PSP'ye iletilir
🤖 kapsamı saklamak değil kart verisine dokunmak doğurur; PAN'ın sunucudan geçmesi uygulamayı, logları, yedekleri ve ağ segmentini kapsama sokar.

## B
Kart alanları PSP'nin hosted ödeme sayfası, iframe'i veya alan bazlı bileşenleriyle gömülür; PAN tarayıcıdan doğrudan PSP'ye gider, bize yalnızca token ve son dört hane döner — marka tutarlılığı bu bileşenlerin stil özelleştirmesiyle sağlanır
🤖 Doğru: en dar kapsam, kart verisine hiç dokunmayan tasarımdır.

## C
Kart formu bizde kalır; PAN uçtan uca TLS ile iletildiği ve trafik şifreli olduğu için ek bir kapsam daraltma önlemine gerek yoktur, güncel şifre paketleri ve sertifika sabitleme ile taşıma katmanı zaten güvence altındadır
🤖 TLS taşımayı korur, kapsamı daraltmaz; veri uçta çözülür ve çözüldüğü yer kapsamdadır.

## D
Kart formu bizde kalır ancak PAN tarayıcıda uygulama tarafından şifrelenip sunucuya şifreli gönderilir; çözme anahtarı yalnızca ödeme servisinde durur ve diğer servisler veriyi hiçbir zaman açık göremez
🤖 çözme yeteneği sende olduğu sürece kart verisi senin kapsamındadır; üstelik el yapımı bir kripto akışı denetimde ek yük ve ayrı bir risk kalemidir (190).

## Doğru
B

## Açıklama
PCI kapsamı (scope), kart verisini **saklayan, işleyen veya ileten** her bileşendir: uygulama sunucusu, veritabanı, log altyapısı, yedekler, o ağ segmenti ve oralara erişen personel. Bu tanımdaki "ileten" kelimesi sorunun kilit noktasıdır — "saklamıyoruz, sadece iletiyoruz, o yüzden kapsam dışıyız" savunması yanlıştır ve mülakatta en sık düşülen tuzaktır. PAN sunucunun belleğinden geçiyorsa bir hata izinde belirebilir, bir istek logunda görünebilir, bir bellek dökümünde bulunabilir; dolayısıyla o sunucu, onun logları ve yedekleri kapsamdadır. Kapsam genişledikçe denetim, kanıtlama ve sürdürme maliyeti büyür; bu yüzden olgun tasarımın hedefi "kart verisini iyi korumak" değil, **kart verisine hiç dokunmamaktır**. Bunun pratik yolu PSP'nin hosted ödeme sayfası, iframe'i veya alan bazlı bileşenleridir: kart alanları sayfaya gömülür ama içerikleri tarayıcıdan **doğrudan PSP'ye** gider, merchant sunucusuna hiç uğramaz, geriye yalnızca bir token ve gösterim için son dört hane döner (`253` idempotency ve `250` akış tarafında değişen bir şey olmaz). UX itirazı gerçek bir itiraz değildir: bu bileşenler yazı tipi, renk ve yerleşim düzeyinde özelleştirilebilir ve marka tutarlılığı denetim yükü ödemeden korunur. TLS cevabı katmanları karıştırır — şifreli taşıma zaten zorunludur ama kapsamı belirleyen şey verinin **nerede çözüldüğüdür**. Kendi kripto akışını kurup PAN'ı tarayıcıda şifrelemek de kurtarmaz: çözme anahtarı sendeyse veri de senin sorumluluğundadır ve üstüne kendi kriptografini doğru kurma yükünü almış olursun (`189`, `190`).

# pci-5 | İleri

## Soru
Ödeme akışında fraud skorlama servisi cevap vermiyor; çağrılar zaman aşımına düşüyor ve circuit breaker açıldı. Havayolunun satış trafiği devam ediyor. Sistemin bu durumda nasıl davranması gerektiğini kim, neye göre belirler?

## A
Fallback davranışı önceden alınmış bilinçli bir iş kararıdır ve tercihen risk bazlı kurulur: düşük tutarlı ve düşük riskli profildeki işlemler geçer, yüksek tutarlı veya bilinen riskli desenler durdurulur ya da 3DS challenge'a zorlanır; devrenin açık kaldığı süre izlenir ve o sırada geçen işlemler işaretlenip geriye dönük skorlanır
🤖 Doğru: fail-open/fail-closed tercihi mühendisin değil iş birimlerinin kararıdır; mühendisin işi kararı talep etmek ve sadakatle uygulamaktır.

## B
Fraud servisi geri gelene kadar ödeme istekleri kuyruğa alınır ve servis ayağa kalktığında sırayla işlenir; böylece hiçbir işlem kontrolsüz geçmez ve hiçbir satış kaybedilmez. Kullanıcıya ödeme ekranında bir bekleme göstergesi gösterilir, servis döndüğünde kuyruk boşaltılır ve sonuçlar toplu olarak bildirilir
🤖 müşteri ödeme ekranında bekleyemez; kuyruk gecikmesi hem dönüşümü hem PSP zaman aşımı penceresini bozar ve sorunu ötelemekten başka bir şey yapmaz.

## C
Fraud servisi zorunlu bir bağımlılıktır; cevap vermediği sürece tüm ödemeler reddedilir, çünkü güvenlik her koşulda gelirin önünde gelir. Bu davranış doğrudan kodda sabitlenir ki kesinti anında kimse tartışmaya açmasın; kesinti kısa sürerse kayıp da sınırlı kalır
🤖 savunulabilir bir uç ama otomatik doğru değil: birkaç dakikalık bir altyapı arızası tüm satışı durdurur ve bu maliyeti kabul edip etmeyeceğine mühendis tek başına karar veremez.

## D
Circuit breaker fallback'inde fraud skoru sabit olarak "düşük risk" kabul edilir ve tüm işlemler geçirilir; kesinti kısa süreceği için ek bir önlem gerekmez. Satış kesintisiz sürdüğünden iş tarafına ayrıca haber verilmesine de gerek duyulmaz, devre kapandığında akış kendiliğinden normale döner
🤖 bu, fail-open kararını iş birimine sormadan koda gömmektir; dolandırıcılar tam olarak bu pencereleri arar ve kesintide geçen işlemler hiç işaretlenmediği için sonradan da yakalanamaz.

## Doğru
A

## Açıklama
Bu soru kıdem ayıran noktadır ve doğru cevabın çekirdeği teknik değildir. Fraud servisi düştüğünde iki uç davranış vardır: **fail-open** (kontrol olmadan geçir — satış sürer, kesinti penceresi boyunca sistem korumasızdır) ve **fail-closed** (reddet — koruma sürer, kısa bir arıza tüm satışı durdurur ve havayolunda bu dakikalar içinde ölçülebilir gelir kaybıdır). Asıl mesele hangisinin "doğru" olduğu değil, **bu tercihin kime ait olduğudur**: "kesinti sırasında ne kadar fraud riskini kabul ediyoruz?" sorusunun cevabı risk ve iş birimlerine aittir. Mühendisin işi kararı almak değil, kararı talep etmek, sonra circuit breaker fallback'ini o karara göre kurmaktır (`101`). "Ben fail-closed yapardım, güvenlik önce gelir" cevabı bu yüzden yarım kalır — sağlam bir refleks gösterir ama karar sahibini yanlış yere koyar. Olgun kurgular ayrıca ikili değil risk bazlıdır: düşük tutarlı işlemler geçerken yüksek tutarlı veya bilinen riskli desenler durdurulur ya da 3DS challenge'a zorlanır — böylece kesinti hem satışı tamamen kilitlemez hem de en pahalı işlemleri korumasız bırakmaz. Üstüne üç sigorta konur: devrenin açık kaldığı süre ve o sırada geçen hacim metrik olarak izlenir (`104`), kesintide geçen işlemler işaretlenir ve servis dönünce geriye dönük skorlanır, gerekirse capture öncesi durdurulur (`250`). İstekleri kuyruklamak çalışmaz çünkü ödeme senkron bir kullanıcı etkileşimidir; sabit "düşük risk" fallback'i ise en tehlikelisidir, zira hem kararı gizlice verir hem de kesintide geçen işlemleri işaretlemediği için sonradan telafi imkânını da yok eder.

# pci-6 | Uzman

## Soru
Gece 03:00'te ödeme denemeleri normalin kırk katına çıktı. İşlemlerin neredeyse tamamı çok düşük tutarlı, hepsi farklı kart numaralarıyla, birkaç IP bloğundan ve aynı cihaz parmak izleriyle geliyor; red oranı %95 civarında. Operasyon "zaten reddediliyor, finansal kayıp yok" diyor. Doğru tepki hangisidir?

## A
Red oranındaki artış geçici bir sağlayıcı sorununa işaret ettiği için başarısız işlemler otomatik yeniden denemeye alınır; her red için geri çekilmeli üç deneme yapılır ve böylece geçici arızadan etkilenen meşru satışlar kurtarılmış olur
🤖 bu, saldırganın işini üstlenmektir: deneme hacmi katlanır, onay oranı daha da düşer ve merchant kart şemaları nezdinde riskli sınıfa girer.

## B
Tüm işlemlerde 3DS zorunlu kılınıp konu kapatılır; doğrulanmış işlemde sorumluluk issuer'a geçtiği için saldırının merchant'a maliyeti kalmaz ve ek bir hız sınırı ya da kural yazmaya gerek duyulmaz
🤖 liability shift yalnızca fraud temelli chargeback'i kapsar, kart deneme saldırısının kendisini değil: sistem hâlâ ücretsiz bir kart doğrulama makinesi olarak kullanılır ve auth maliyeti ile onay oranı kaybı merchant'ta durur.

## C
Bu bir card testing (kart deneme) saldırısıdır; kart/IP/cihaz bazlı rate limiting ve hız kuralları devreye alınır, eşleşen desenler 3DS challenge'a veya manuel inceleme kuyruğuna yönlendirilir, red oranı ile deneme hızı alarm eşiği tanımlı metriklere bağlanır
🤖 Doğru: saldırı desen olarak teşhis edilir, hız sınırı ve fraud kuralları birlikte çalışır, tespit kalıcı bir metriğe bağlanır.

## D
Trafik meşru bir kampanya etkisi olabileceği için müdahale edilmez, yalnızca izlenir; işlemler reddedildiği sürece sistemde finansal bir kayıp oluşmaz, erken müdahale ise gerçek müşterileri kesme riski taşır
🤖 kayıp vardır: her auth denemesi ücretlidir, onay oranı düşer, şemalar yüksek red oranını cezalandırır ve doğrulanan kartlar başka bir yerde kullanılır.

## Doğru
C

## Açıklama
Tablodaki her işaret aynı yeri gösteriyor: çok sayıda **farklı kart**, **çok düşük tutar**, dar bir IP ve cihaz kümesi, ezici red oranı. Bu meşru trafik değil, **card testing** saldırısıdır — saldırgan elindeki çalıntı kart listesinden hangilerinin canlı olduğunu öğrenmek için senin ödeme ucunu ücretsiz bir kart doğrulama makinesi olarak kullanmaktadır. "Reddediliyorsa kayıp yok" cevabı eleyicidir ve dört ayrı maliyeti gözden kaçırır: her otorizasyon denemesinin bir bedeli vardır, onay oranın düşer, kart şemaları yüksek red oranını izler ve eşik aşılırsa merchant'ı izleme programına alıp ceza uygular, ve en önemlisi doğrulanan kartlar başka bir yerde gerçek zarar üretir — sen bu zincirin ilk halkası olmuş olursun. Doğru tepki iki katmanlıdır. Birincisi **rate limiting** (`153`): kart, IP, cihaz parmak izi ve hesap bazında pencere sınırı; bu, ödeme uçlarında genel bir API koruması değil, domain'e özgü bir güvenlik kontrolüdür. İkincisi **fraud tarafındaki hız kuralları**: aynı cihazdan çok kart, kısa sürede çok deneme, tutar/BIN anomalileri — ve eşleşen işlemler doğrudan reddedilmek yerine üçüncü kutuya, yani 3DS challenge'a veya manuel kuyruğa yönlendirilir ki meşru bir kampanya trafiği yanlışlıkla kesilmesin. Üstüne olay kalıcı hale getirilir: red oranı ve deneme hızı alarm eşiği tanımlı metrikler olur, saldırı sonraki sefer insan gözüne değil sisteme takılır (`104`). Yeniden deneme önerisi durumu ters yönde büyütür — saldırganın hacmini kendi elinle çarpmış olursun ve `250`'deki red kodu sınıflandırması tam da bunu engellemek içindir. 3DS'i tek çözüm sanmak ise liability shift'in kapsamını yanlış okumaktır: sorumluluk devri chargeback faturasını taşır, denemelerin kendisini durdurmaz.
