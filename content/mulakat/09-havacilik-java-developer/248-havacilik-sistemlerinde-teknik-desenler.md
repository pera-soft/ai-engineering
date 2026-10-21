---
slug: havacilik-sistemlerinde-teknik-desenler
baslik: "Havacılık Sistemlerinde Teknik Desenler: Son Koltuk, Saga, Eski Dünya ve Tepe Yük"
kademe: İleri
sira: 248
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir stadyum düşün: final maçına yüz bin kişi bilet bakıyor, ama koltuk sayısı elli bin. Gişeye gelen herkes önce panoya bakıyor — boş yer var mı? Çok azı gerçekten kuyruğa girip para ödüyor.

Havacılık BT'si tam bu stadyumdur. **Bakan çok, alan azdır.** Son koltuğa aynı anda iki el uzanır. Bilet kesildikten sonra maç iptal olursa binlerce kişiyi yeniden yerleştirmek gerekir.

`247` numaralı konu bu dünyanın kavramlarını anlattı: PNR, e-bilet, IRROPS, GDS. Bu konu ise aynı kavramların **mühendislik karşılığını** anlatır. Mülakatta "havacılığı bilir misin?" sorusunun kıdemli cevabı ürün adları saymak değildir; bu desenleri kurabilmektir.

### Temel kavramlar

- **arama/satış oranı (look-to-book ratio):** kaç arama sorgusuna karşılık bir satış geldiği. Havacılıkta yüzlerce aramaya bir rezervasyon düşmesi yaygındır.
- **aşırı satış (overbooking):** kapasitenin üstünde satışa izin verme. Bu bir hata değildir; gelmeme (no-show) istatistiğine dayalı **bilinçli bir iş politikasıdır**.
- **saga (adım adım telafili işlem zinciri):** uzun süren dağıtık akışı yerel adımlarla ve **telafi işlemleriyle** yöneten desen (`126`).
- **tekrar edilse de sonucu değişmeyen işleme (idempotency):** aynı isteğin ikinci kez gelmesi yan etkiyi tekrarlamaz (`130`).
- **yozlaşma önleyici katman (anti-corruption layer):** eski sistemin modelini çevirir ve iş alanına (domain) sızmasını önler. **Boğan incir (strangler fig)** ise eskiyi kademeli olarak devre dışı bırakan taşıma stratejisidir.

### Nasıl çalışır? — çift rezervasyon senaryosu adım adım

Uçuşta tek koltuk kaldı; iki kullanıcı aynı saniyede "satın al" dedi. Olay uçtan uca şöyle akar:

1. **İki istek de koltuğu "boş" okur.** Okuma anında kilit yoktur ve olmamalıdır — okuma yolunu kilitlemeye çalışmak arama ölçeğini öldürür. İkisinin de aynı görüntüyü görmesi normaldir.
2. **Yazma anında iyimser kilit karar verir** (`225`). Envanter satırında bir versiyon kolonu vardır; güncelleme `UPDATE ... WHERE id = ? AND versiyon = 5` mantığıyla yazılır. İlk istek yazar ve versiyon 6 olur. İkinci isteğin güncellemesi ise sıfır satır etkiler; bu istek **versiyon çakışması istisnası** alır.
3. **Kaybeden istek zarifçe yönetilir.** Kullanıcıya teknik hata ekranı gösterilmez. Bunun yerine "bu koltuk az önce satın alındı, şu koltuklar boş" cevabı döner ve güncel uygunlukla yeniden deneme sunulur.
4. **Kötümser kilit (FOR UPDATE) bir alternatiftir** ama istekleri sıraya dizip bekletir. Çakışmanın nadir, okumanın devasa olduğu bu trafikte iyimser yol tercih edilir.
5. **Son savunma hattı veritabanı benzersiz kısıtıdır.** Uygulama katmanı bir gün atlanırsa bile aynı koltuk-uçuş ikilisine iki kayıt yazılamaz.

Bu senaryonun üstünde bir de bilinçli bir katman vardır: **aşırı satış**. Sistem "kapasite aşıldı, hata!" diye alarm üretmez. Satış sınırı üründe bir **parametredir**. İş birimi, gelmeme istatistiğine göre uçuş başına bir tolerans belirler; yazılım bu toleransı taşır. "Kapasite üstü kaydı her koşulda engellerim" diyen aday, iş kuralını yazılım hatası sanmıştır.

### Arama ucuz, rezervasyon kutsal

Uygunluk (availability) sorgusu okuma ağırlıklıdır ve **önbelleklenir**. Önbellek desenleri, yaşam süresi (TTL) ve eşzamanlı yığılma savunmaları burada devreye girer (`157`-`160`). Rezervasyon ise yazma açısından kritiktir ve yetkili kaynağa, yani envanter veritabanına gider.

Bunun bir iş sonucu vardır. Önbellek bayatsa, kullanıcının gördüğü koltuk aslında satılmış olabilir. Çözüm, bayatlığı yok etmeye çalışmak değildir; **rezervasyon anında yeniden doğrulamaktır**. Aramada yaklaşık doğruluk kabul edilir; para el değiştirirken kesin doğruluk aranır.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Arama ve rezervasyon asimetrisi: onbellekli okuma yolu, yetkili kaynaga giden yazma yolu">
  <defs>
    <marker id="havtek-asi-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Arama / rezervasyon asimetrisi (look-to-book)</text>
  <rect x="20" y="44" width="180" height="46" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.5"/>
  <text x="110" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kullanıcılar</text>
  <text x="110" y="80" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yüzlerce arama · tek satış</text>
  <path d="M200 56 L280 56" fill="none" stroke="#186399" stroke-width="2.2" marker-end="url(#havtek-asi-arw)"/>
  <text x="240" y="48" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">ARAMA (çok)</text>
  <path d="M200 82 L280 168" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#havtek-asi-arw)"/>
  <text x="212" y="130" font-size="9.5" fill="#25804C" font-family="system-ui">REZERVASYON (az)</text>
  <rect x="284" y="36" width="220" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="394" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Önbellek (Redis, 157-160)</text>
  <text x="394" y="74" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">TTL'li uygunluk · bayatlık kabul edilir</text>
  <rect x="284" y="150" width="220" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="394" y="171" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Envanter (yetkili kaynak)</text>
  <text x="394" y="188" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">iyimser kilit, versiyon kolonu (225)</text>
  <path d="M504 65 L560 65 L560 168 L508 168" fill="none" stroke="#B07D20" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#havtek-asi-arw)"/>
  <text x="586" y="120" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">satın almada</text>
  <text x="586" y="134" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">yeniden doğrula</text>
  <rect x="90" y="234" width="480" height="46" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="253" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Görünen koltuk satılmış olabilir — bayat önbellek bir iş gerçeğidir</text>
  <text x="330" y="271" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aramada yaklaşık doğruluk · para el değiştirirken kesin doğrulama</text>
</svg>
```

### Rezervasyon sagası, idempotency ve time limit

Senaryoyu ilerletelim. Kullanıcı koltuğu kaptı ve kartını girdi. Sağlayıcı 30 saniye sessiz kaldı, sonra "yetersiz bakiye" döndü. Koltuk ne olacak?

Satın alma tek bir işlem değildir. **Koltuk bloke → ödeme → biletleme** zinciridir ve ödeme çoğu zaman harici bir sağlayıcıdadır. İki aşamalı dağıtık işlem (2PC) burada çalışmaz. Doğru desen **sagadır** (`126`): her adımın telafisi tanımlanır. Ödeme düşerse koltuğu serbest bırakan telafi otomatik koşar.

Adım olayları, veritabanı yazımıyla **aynı yerel işlem içinde giden kutusu tablosuna (outbox)** yazılıp oradan yayınlanır (`116`). Böylece "kaydettim ama olayı yayınlayamadım" boşluğu kapanır.

Çift tıklama çift bilet kesmesin diye biletleme ucu bir **tekilleştirme anahtarıyla (idempotency key)** korunur (`130`). Anahtar sonuçla birlikte saklanır; tekrar gelen istek aynı bileti döndürür.

Son sigorta **süre sınırıdır (time limit)**. Ödemesi tamamlanmayan rezervasyonun koltuğu, süre dolunca otomatik serbest kalır. Telafi hiç tetiklenmese bile envanter kilitli kalmaz.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Rezervasyon sagasi: koltuk bloke, odeme, biletleme zinciri ve odeme dusunce telafi">
  <defs>
    <marker id="havtek-saga-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="havtek-saga-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Rezervasyon sagası: ileri adımlar + telafi</text>
  <rect x="24" y="48" width="170" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="109" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">1. Koltuk bloke</text>
  <text x="109" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">time limit başlar</text>
  <path d="M194 75 L246 75" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#havtek-saga-arw)"/>
  <rect x="250" y="48" width="160" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2. Ödeme</text>
  <text x="330" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">harici sağlayıcı</text>
  <path d="M410 75 L462 75" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#havtek-saga-arw)"/>
  <rect x="466" y="48" width="170" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="551" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">3. Biletleme</text>
  <text x="551" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">idempotency anahtarı (130)</text>
  <path d="M330 102 L330 138 L200 138" fill="none" stroke="#B23B3B" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#havtek-saga-red)"/>
  <text x="356" y="126" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">ödeme DÜŞTÜ</text>
  <rect x="24" y="118" width="172" height="42" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="110" y="136" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">TELAFİ (126)</text>
  <text x="110" y="152" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">koltuğu serbest bırak</text>
  <rect x="24" y="182" width="612" height="34" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="203" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">Adım olayları outbox ile yayınlanır (116): kayıt + olay aynı yerel işlemde, dual-write boşluğu yok</text>
  <rect x="24" y="226" width="612" height="34" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="330" y="247" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Time limit sigortası: ödemesi bitmeyen rezervasyonun koltuğu süre sonunda otomatik serbest kalır</text>
</svg>
```

### Eski dünyayla konuşmak

Havacılıkta çok eski ve çok yeni yan yana yaşar. Host/GDS sistemleri EDIFACT tarzı mesajlarla, SOAP veya mesaj kuyruğu (MQ) üzerinden konuşabilir (`221`).

Sürdürülebilir yol üç adımlıdır. Bu mesajlaşmayı bir **uyarlayıcıyla (adapter) sararsın**. Eski formatın alan adlarını ve kısıtlarını **yozlaşma önleyici katmanla** kendi iş modeline çevirirsin. İşlevleri de **kademeli taşıma (strangler fig)** ile parça parça yeni sisteme aktarırsın.

İki uç yanlıştır. Eski sistemi tek hamlede yeniden yazmak, on yılların iş kuralını tek seferde riske atar. Eski mesaj yapısını iş modeline olduğu gibi taşımak ise çürümeyi içeri sızdırır; modern sistem eskinin kopyası olur.

### Tepe yük, 7/24 ve IRROPS fırtınası

Kampanya ve bayram dönemlerinde trafik katlanır. Çözüm dikey büyütme değildir. Üç araç kullanılır: **yatay ölçekleme**, uçta **istek hızı sınırlama (rate limiting)** (`153`) ve **kuyrukla yük düzleştirme**.

Küresel ve 7/24 çalışan bir sistemde "gece bakım penceresi" yoktur; dağıtım kesintisiz olmalıdır. **Mavi-yeşil** ve **kademeli yayın (canary)** yaklaşımları bu yüzden fikir olarak bilinmelidir.

IRROPS (`247`) ise yükün en sert halidir: tek bir iptal, binlerce PNR'ın yeniden hesaplanmasını tetikler. Bunu isteğin kendi iş parçacığında ve eşzamanlı olarak yapmak sistemi devirir. Doğru desen şudur: etkilenen PNR'ları **kuyruğa yaz, tekilleştirilmiş bir toplu işle tüket** (`222`-`223`). İş yarıda kalırsa güvenle yeniden başlar; canlı trafik bu sırada korunur.

```svg
<svg viewBox="0 0 660 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="IRROPS ve tepe yuk: ani yuk kuyrukla duzlestirilir, idempotent toplu is tuketir">
  <defs>
    <marker id="havtek-yuk-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">IRROPS fırtınası: kuyrukla yük düzleştirme</text>
  <rect x="20" y="44" width="180" height="64" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="110" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Uçuş iptali</text>
  <text x="110" y="84" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">binlerce PNR etkilenir</text>
  <text x="110" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ani, sivri yük</text>
  <path d="M200 76 L252 76" fill="none" stroke="#5D7789" stroke-width="1.8" marker-end="url(#havtek-yuk-arw)"/>
  <rect x="256" y="44" width="150" height="64" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="331" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Kuyruk</text>
  <text x="331" y="84" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">etkilenen PNR listesi</text>
  <text x="331" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yükü düzleştirir</text>
  <path d="M406 76 L458 76" fill="none" stroke="#5D7789" stroke-width="1.8" marker-end="url(#havtek-yuk-arw)"/>
  <rect x="462" y="44" width="178" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="551" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Toplu iş (222-223)</text>
  <text x="551" y="84" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">idempotent adımlar</text>
  <text x="551" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yarıda kalırsa yeniden başlar</text>
  <rect x="20" y="132" width="302" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="171" y="153" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Uçta rate limiting (153)</text>
  <text x="171" y="170" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">canlı arama/rezervasyon trafiği korunur</text>
  <rect x="338" y="132" width="302" height="52" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="489" y="153" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">7/24: bakım penceresi yok</text>
  <text x="489" y="170" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dağıtım mavi-yeşil / canary ile kesintisiz</text>
  <rect x="60" y="204" width="540" height="42" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="330" y="222" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Yanlış: yeniden hesaplamayı iptal isteğinin thread'inde eşzamanlı fan-out ile yapmak</text>
  <text x="330" y="239" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tek istek binlerce işleme dönüşür, havuzlar tükenir, iptalin kendisi zaman aşımına düşer</text>
</svg>
```

### Mülakatta ne sorulur?

- **"İki kişi aynı anda son koltuğa basarsa ne olur?"** — sözlü cevap iskeleti, aynen kullan (4 adım): (1) "Okuma anında kilit yok; ikisi de koltuğu boş görür, bu kabul edilir." (2) "Yazma anında versiyon kolonlu iyimser kilit karar verir (`225`); kaybeden istek çakışma istisnası alır." (3) "Kaybedene teknik hata değil, 'koltuk az önce alındı, alternatifler şunlar' dönerim." (4) "Son savunma veritabanı benzersiz kısıtı; aşırı satış ise ayrı bir bilinçli iş parametresidir, kilit konusuyla karıştırılmaz."
- **"Availability neden önbelleklenir, bayatlık ne olacak?"** Look-to-book asimetrisi + TTL'li önbellek (`157`-`160`) + rezervasyonda yetkili kaynaktan yeniden doğrulama; bayat görüntü kullanıcıya alternatifle telafi edilir.
- **"Ödeme düşerse / kullanıcı iki kez basarsa ne olur?"** — cevap iskeleti (4 adım): (1) "Ödeme düşünce saga telafisi koltuğu serbest bırakır (`126`)." (2) "Adım olayları outbox'la kayıpsız yayınlanır (`116`)." (3) "Çift tıklama idempotency anahtarıyla yan etkisiz kalır — ikinci istek aynı bileti döndürür (`130`)." (4) "Time limit sigortası her durumda envanteri geri kazanır."
- **"Eski host sistemle nasıl entegre olursun?"** SOAP/MQ sarmalama (`221`), anti-corruption layer, strangler fig; "hepsini yeniden yazarız" eleyici cevaptır.
- **"Bayram trafiğine nasıl hazırlanırsın?"** Yatay ölçekleme, rate limiting (`153`), kuyrukla düzleştirme, kesintisiz dağıtım; genel tasarım iskeleti için `141`.

### Tek cümlede

Havacılık BT'sinin mühendislik özü dört desendir. Son koltukta: **iyimser kilit ve parametreli aşırı satış**. Aramada: **önbellek, satın almada yeniden doğrulama**. Rezervasyonda: **saga, giden kutusu, tekilleştirme anahtarı ve süre sınırı**. Eski dünyayla: **sarmalama ve kademeli taşıma**. Hepsinin üstünde ise 7/24 tepe yükünü **kuyruk ve tekrar edilebilir toplu işle** düzleştirme disiplini vardır.

# havtek-1 | Orta

## Soru
Bir uçuşta tek koltuk kaldı; iki kullanıcı aynı anda koltuğu seçip ödemeye ilerledi ve iki yazma isteği aynı milisaniyede envantere ulaştı. Sistemin bu çakışmaya doğru tepkisi hangisidir?

## A
Uygunluk sorgusu dahil tüm okuma ve yazmalar FOR UPDATE ile kötümser kilitlenir; böylece iki talep aynı koltuğu hiçbir aşamada aynı anda göremez
🤖 okuma-ağırlıklı availability trafiğini kilit kuyruğuna sokmak arama ölçeğini öldürür; kilit yalnız yazma anında gerekir.

## B
Envanter satırında versiyon kolonuyla iyimser kilit kullanılır; çakışan ikinci talep yeniden denenir veya kullanıcıya alternatif önerilir
🤖 Doğru: son koltuk çakışması nadir bir olaydır ve iyimser kilit bu nadir çakışmayı ucuza çözer.

## C
Koltuk sayacı önbellekte tutulur ve satış kararı önbellekteki değere göre verilir; veritabanına gece toplu senkronizasyonla yazılır
🤖 önbellek satış kararında gerçeğin kaynağı yapılamaz; bayat sayaçla verilen karar çift satışa yol açar.

## D
Aşırı satış bir yazılım hatası olduğu için benzersiz kısıt ve tetikleyicilerle her koşulda engellenir; kapasite üstü hiçbir kayıt oluşamaz
🤖 aşırı satış (overbooking) hata değil, no-show istatistiğine dayalı bilinçli iş politikasıdır; sistem bunu parametreyle taşır.

## Doğru
B

## Açıklama
Çakışma **yazma anında** çözülmelidir, okuma yolunda değil. Yaygın çözüm envanter satırına **versiyon kolonu** koyup **iyimser kilit** kullanmaktır (`225`): iki eşzamanlı yazmadan biri versiyon çakışmasıyla reddedilir, uygulama bu talebi yeniden dener ya da kullanıcıya alternatif uçuş/koltuk önerir. Kötümser kilidi tüm uygunluk sorgularına yaymak, look-to-book oranı yüzlerce olan bir sistemde arama trafiğini kilit kuyruğuna dizer ve ölçeği öldürür. Önbelleği satış kararının kaynağı yapmak ise ters yöndedir: önbellek aramayı hızlandırır (`157`-`159`) ama para el değiştirirken karar **yetkili kaynaktan** verilir. Son şıkkın yanılgısı daha inceliklidir: havacılıkta kapasite üstü satış bir kusur değil, gelmeme (no-show) istatistiğine dayalı **bilinçli bir iş politikasıdır**; uçuş başına tolerans iş birimince belirlenir ve sistemde **parametre** olarak yaşar. "Aşırı satışı kod seviyesinde imkânsız kılarım" diyen aday, iş kuralını yazılım hatası sanmış olur — bu, alan bilgisi eksikliğinin klasik işaretidir.

# havtek-2 | Orta

## Soru
Availability önbelleği kullanıcıya koltuğu boş gösterdi; kullanıcı rezervasyona ilerlediğinde koltuk envanterde az önce satılmış çıktı. Bu olayın doğru okuması ve tasarım tepkisi hangisidir?

## A
Bu bir tasarım hatasıdır; önbellek kaldırılır ve her availability sorgusu güncel doğruluk için doğrudan envanter veritabanından okunur
🤖 look-to-book oranı yüzlerce aramaya bir satış demektir; her aramayı ana veritabanına indirmek yazma yolunu da boğar.

## B
Sorunun kökü tek yönlü akıştır; rezervasyon da önbelleğe yazarsa iki katman hep eşit kalır ve bu durum bir daha hiç yaşanmaz
🤖 rezervasyon yazma-kritiktir; kararı bayat ve kaybolabilir bir katmana dayandırmak çift satış ve kayıp kayıt riski taşır.

## C
Kullanıcı mağdur edilmez: rezervasyon önbellekteki görüntüye dayanarak onaylanır, envanter farkı gece mutabakat işiyle düzeltilir
🤖 görüntüye dayanarak satış onaylamak aynı koltuğu iki kişiye satmaktır; gece mutabakatı mağduriyeti çözmez, ertelemekle büyütür.

## D
Bu beklenen bir durumdur: bayatlık aramada kabul edilir, rezervasyon yetkili kaynaktan yeniden doğrular, kullanıcıya alternatif sunulur
🤖 Doğru: look-to-book asimetrisi önbelleği zorunlu kılar; kesinlik satın alma anında aranır.

## Doğru
D

## Açıklama
Bu senaryo bir arıza değil, tasarımın **bilinçli bedelidir**. Havacılık trafiğinin karakteri **look-to-book asimetrisidir**: yüzlerce arama sorgusuna karşılık bir satış gelir; bu yüzden availability okuma yolu **TTL'li önbellekten** beslenir (`157`-`158`; stampede/avalanche savunmaları için `159`), rezervasyon yazma yolu ise **yetkili kaynağa** (envanter veritabanı, iyimser kilitle `225`) gider. Önbelleğin bedeli bayatlıktır: kullanıcının gördüğü koltuk o arada satılmış olabilir. Doğru tepki bayatlığı sıfırlamaya çalışmak değil, **rezervasyon anında yeniden doğrulamak** ve kaybeden kullanıcıya "koltuk az önce alındı, şunlar boş" diyerek güncel alternatif sunmaktır — aramada yaklaşık doğruluk yeterlidir, para el değiştirirken kesinlik aranır. Önbelleği kaldırmak, seyrek ama kritik yazma trafiğinin kaynağını okuma seliyle paylaştırır. Rezervasyonu önbelleğe yazdırmak en tehlikeli seçenektir: bellek-içi katman kaybolabilir, gecikmeli senkronizasyon çift satışın kapısını açar. Görüntüye güvenip satışı onaylamak ise aynı koltuğu iki kişiye satmaktır; "gece mutabakatı" mağduriyeti çözmez, havalimanında patlayacak bir soruna erteler.

# havtek-3 | İleri

## Soru
Rezervasyon akışı üç ayrı servisten geçiyor: koltuk bloke, ödeme (harici sağlayıcı), biletleme. Ödeme başarısız olduğunda tutarlılık nasıl sağlanmalıdır?

## A
Saga kurulur: her adımın telafisi tanımlanır, ödeme düşünce koltuğu serbest bırakan telafi çalışır; adım olayları outbox ile yayınlanır
🤖 Doğru: uzun süren dağıtık akışta yerel işlem + telafi zinciri güvenilir yoldur; olaylar outbox'la kayıpsız yayınlanır.

## B
Üç servis tek dağıtık işlemde (2PC/XA) birleştirilir; ödeme başarısız olursa koordinatör tüm adımları otomatik olarak geri alır
🤖 harici ödeme sağlayıcısı ve uzun süren adımlar 2PC'ye uymaz; kilitleri dakikalarca tutmak sistemi kilitler (gerekçe: 126).

## C
Koltuk bloke kaydı olduğu gibi bırakılır; ödemesi düşen kayıtlar gün sonu raporuyla tespit edilir ve operasyon ekibi elle serbest bırakır
🤖 satılabilir envanteri saatlerce kilitli tutmak doğrudan gelir kaybıdır; telafi otomatik ve hızlı olmalıdır.

## D
Ödeme adımı akışın en başına alınır; para çekildikten sonra koltuk bloke edilir, böylece telafi ihtiyacı hiç doğmaz
🤖 sıra değişince bu kez "para çekildi ama koltuk kalmadı" durumu doğar; iade de bir telafidir, ihtiyaç ortadan kalkmaz.

## Doğru
A

## Açıklama
Koltuk bloke → ödeme → biletleme zinciri, **uzun süren ve harici bağımlılık içeren** bir dağıtık akıştır; bu tür akışların deseni **sagadır** (`126`). Her ileri adımın bir **telafi işlemi** (compensation) tanımlanır: ödeme düşerse koltuğu serbest bırakan telafi otomatik koşar, kullanıcı bekletilmez, envanter geri kazanılır. Adımlar arası olaylar, servis kendi veritabanına yazarken **aynı yerel işlem içinde outbox tablosuna** da yazılır ve oradan yayınlanır (`116`) — böylece "kaydettim ama olayı yayınlayamadım" (dual-write) boşluğu kapanır; tüketici tarafında tekrar güvenliği için idempotent tüketim gerekir (`130`). 2PC/XA cevabı eleyicidir: harici ödeme sağlayıcısı XA'ya katılmaz ve kullanıcının ödeme ekranında geçirdiği dakikalar boyunca kilit tutmak sistemi kilitler — saganın var olma sebebi tam budur. Elle temizlik, satılabilir envanteri saatlerce bloke bırakır; time limit ve otomatik telafi varken bu kabul edilemez. Ödemeyi başa almak ise problemi çözmez, yönünü değiştirir: bu kez para çekilmiş ama koltuk tükenmiş olabilir ve **iade** adı altında yine bir telafi gerekir.

# havtek-4 | İleri

## Soru
Ödeme sağlayıcısı 30 saniye yanıt vermeyince kullanıcı onay düğmesine bir kez daha bastı ve iki biletleme isteği de sunucuya ulaştı. Çift bilet kesilmesini önlemenin doğru yolu hangisidir?

## A
Ön yüzde düğme ilk tıklamada devre dışı bırakılır; isteğin tekilliği istemci tarafında bu şekilde garanti edilir
🤖 istemci önlemi yararlı ama yetersizdir; ağ katmanının yeniden denemesi, ikinci sekme veya API'yi doğrudan çağıran istemci düğmeyi görmez.

## B
Sunucu, aynı PNR'a kısa zaman aralığında gelen ikinci biletleme isteğini istek gövdesi karşılaştırmasıyla yakalayıp reddeder
🤖 zaman penceresi + içerik karşılaştırması kırılgandır; meşru bir ikinci işlemi de reddedebilir, pencere dışındaki tekrarı kaçırır.

## C
İstemcinin ürettiği idempotency anahtarı istekle gönderilir; sunucu anahtarı sonuçla birlikte saklar, tekrar gelen istek aynı bileti döndürür
🤖 Doğru: tekrar güvenliği anahtar + saklanan sonuçla kurulur; ikinci istek yan etkisiz biçimde aynı yanıtı alır.

## D
Biletleme tablosuna benzersiz kısıt eklenir; ikinci istek veritabanı hatasıyla düşer ve kullanıcıya genel bir hata sayfası gösterilir
🤖 kısıt son savunma hattı olarak değerlidir ama tek başına kullanıcıya "hata" gösterir; doğru davranış aynı başarılı sonucu döndürmektir.

## Doğru
C

## Açıklama
Çift tıklama, ağ katmanının otomatik yeniden denemesi veya mobil uygulamanın zaman aşımı sonrası tekrarı — hepsi aynı sınıfın üyesidir: **aynı iş isteğinin birden fazla kez ulaşması**. Kalıcı çözüm **idempotency anahtarıdır** (`130`): istemci her iş girişimi için benzersiz bir anahtar üretir, sunucu bu anahtarı **işlem sonucuyla birlikte** saklar; aynı anahtarla gelen ikinci istek yeni bilet kesmez, saklanan başarılı yanıtı aynen döndürür. Böylece kullanıcı iki kez bassa da tek bilet ve tutarlı bir ekran görür. Ön yüz önlemi (düğmeyi kilitleme) iyi bir pratik ama **güvenlik sınırı değildir**: retry eden proxy'ler, ikinci sekme veya API'yi doğrudan çağıran bir istemci düğmeyi hiç görmez — sunucu tarafı garanti şarttır. Zaman penceresi içinde gövde karşılaştırmak kırılgandır: aynı yolcunun bilinçli ikinci satın alması yanlışlıkla reddedilebilir, pencere dışına sarkan tekrar ise yakalanmaz. Benzersiz kısıt **son savunma hattı** olarak yerinde durmalıdır; ama tek başına kaldığında ikinci istek kullanıcıya hata sayfası gösterir — oysa doğru deneyim, isteğin "zaten yapıldı, işte sonucu" diye yanıtlanmasıdır.

# havtek-5 | Uzman

## Soru
Modern bir Spring Boot rezervasyon servisi, EDIFACT tarzı mesajlarla konuşan eski host/GDS sistemleriyle entegre edilecek. Sürdürülebilir yaklaşım hangisidir?

## A
Eski sistem tek projeyle komple yeniden yazılır ve tek seferde yenisine geçilir; çeviri katmanına yapılacak yatırım böylece gereksizleşir
🤖 büyük patlama (big bang) yeniden yazımı, on yıllara yayılmış iş kurallarını tek seferde riske atar; sektör bu tür başarısız geçiş örnekleriyle doludur.

## B
EDIFACT mesaj yapıları domain modeline olduğu gibi taşınır; ara çeviri katmanı olmadığı için format uyumsuzluğu riski de ortadan kalkar
🤖 eski formatın alan adları ve kısıtları domain'e sızarsa modern sistem eskinin kopyası olur; çürüme dışarıda kalacağına içeri taşınır.

## C
Host sisteme her istekte senkron bağlanılır; hata durumunda başarılı olana kadar yeniden denenir, araya kuyruk katmanı eklenmez
🤖 eski host'ların kapasite pencereleri dardır; sınırsız senkron yeniden deneme karşı sistemi devirir ve gecikmeyi kullanıcıya taşır.

## D
Mesajlaşma bir adaptörle sarılır, anti-corruption layer formatı domain'e çevirir, işlevler strangler fig ile kademeli taşınır
🤖 Doğru: eski dünya sarmalanır, domain temiz kalır, geçiş kademeli ve geri dönülebilir olur.

## Doğru
D

## Açıklama
Havacılık BT'sinin ayırt edici gerçeği, çok eski ve çok yeni sistemlerin **yan yana yaşamasıdır**: host/GDS tarafı SOAP/MQ üzerinden, EDIFACT tarzı mesajlarla konuşabilir (`221`). Sürdürülebilir desen üç parçadır. Birincisi, mesajlaşmayı bir **adaptörle sarmak**: protokol ayrıntısı (SOAP zarfı, kuyruk, karakter seti) tek modülde toplanır. İkincisi, **anti-corruption layer**: eski formatın alan adları, kodlamaları ve kısıtları domain modeline **çevrilerek** girer; çeviri katmanı olmazsa modern sistemin iç modeli eski mesaj yapısının kopyası olur ve teknik çürüme dışarıda kalacağına içeri taşınır. Üçüncüsü, **strangler fig**: işlevler eski sistemden yenisine **kademeli** taşınır, her adım geri dönülebilirdir ve iki sistem geçiş boyunca birlikte çalışır. Büyük patlama yeniden yazımı bunun tam karşıtıdır: on yıllara yayılmış, çoğu belgelenmemiş iş kuralını tek hamlede riske atar. Sınırsız senkron yeniden deneme ise dar kapasite pencereli eski host'u devirir; araya kuyruk koymak hem karşı tarafı korur hem gecikmeyi kullanıcıdan ayırır. Bu üçlü, mülakatta "eski sistemle nasıl entegre olursun" sorusunun kıdemli cevabıdır.

# havtek-6 | Uzman

## Soru
Büyük bir uçuş iptali (IRROPS) binlerce PNR'ın yeniden hesaplanmasını tetikledi; sistem 7/24 canlı ve bayram tepe trafiği sürüyor. Doğru tasarım hangisidir?

## A
Etkilenen PNR'lar kuyruğa yazılır ve idempotent toplu iş adımlarıyla tüketilir; uçta rate limiting canlı trafiği korur
🤖 Doğru: kuyruk ani yükü düzleştirir; yeniden çalıştırılabilir toplu iş, yarıda kalan fırtınayı güvenle tamamlar.

## B
Yeniden hesaplama, iptal isteğini işleyen thread içinde eşzamanlı fan-out ile hemen yapılır; böylece hiçbir PNR işlenmeden bekletilmez
🤖 tek istek binlerce işleme dönüşünce thread havuzu ve bağlantılar tükenir; iptal işleminin kendisi zaman aşımıyla düşer.

## C
Sezon öncesi sunucular dikey büyütülür ve yeniden hesaplama gece bakım penceresinde topluca koşulur; gündüz trafiği hiç etkilenmez
🤖 küresel 7/24 sistemde herkes için "gece" olan bir bakım penceresi yoktur; tepe yük dikey büyütmenin tavanını da aşar.

## D
Hesaplama pasif bırakılır; yolcu uygulamaya girdiğinde yalnızca kendi PNR'ı o anda yeniden hesaplanır, arka plan işine gerek kalmaz
🤖 yeniden yerleştirme ve bildirim yolcunun girişini bekleyemez; bağlantılı uçuşlar ve süre hedefleri proaktif işlemeyi zorunlu kılar.

## Doğru
A

## Açıklama
IRROPS'un teknik imzası **yük şeklidir**: tek bir olay (iptal), binlerce türev işi (PNR yeniden hesaplama, yeniden yerleştirme, bildirim) aynı anda doğurur. Bu sivri yükü istek yolunda emmeye çalışmak sistemi devirir; doğru desen **kuyrukla yük düzleştirmedir**: etkilenen PNR'lar kuyruğa yazılır, **idempotent adımlarla çalışan toplu iş** (`222`-`223`) kendi hızında tüketir — iş yarıda kalırsa kaldığı yerden güvenle yeniden başlar, aynı PNR iki kez işlense de sonuç değişmez. Uçtaki **rate limiting** (`153`) bu sırada canlı arama/rezervasyon trafiğinin kaynaklarını korur. Eşzamanlı fan-out cevabı klasik tuzaktır: iptali işleyen tek istek binlerce çağrıya dönüşür, thread havuzu ve veritabanı bağlantıları tükenir, iptalin kendisi zaman aşımına düşer. "Gece bakım penceresi" cevabı havacılıkta geçersizdir: küresel 7/24 sistemde herkes için gece olan bir saat yoktur; bu yüzden dağıtımlar da mavi-yeşil / canary gibi kesintisiz tekniklerle yapılır. Tembel (yolcu girince hesapla) yaklaşım ise iş gerçeğiyle çelişir: yeniden yerleştirme ve bildirim, yolcunun uygulamayı açmasını bekleyemez — bağlantılı uçuşlar ve operasyonel süre hedefleri proaktif işlemeyi zorunlu kılar.
