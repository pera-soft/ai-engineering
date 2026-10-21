---
slug: kredi-kullanici-ekranlari-backend
baslik: "Kredi Kullanıcı Ekranlarının Backend'i: Akışlar, API Tasarımı, Para, Yetki ve Denetim"
kademe: İleri
sira: 240
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir mağaza vitrini düşün. Vitrindeki fiyat etiketini görüp içeri giriyorsun ama kasada başka bir tutar söyleniyor. Etiketi basan kişi kasadaki hesabı da yapsaydı bu fark hiç doğmazdı.

Kredi ekranları da böyledir. Müşterinin gördüğü taksit tablosu bir vitrindir. Gerçek borç ise kasada, yani sunucuda oluşur. Ekran geliştiricisi "ekran yaptım" der. Ama mülakatta sorulan şey ekranın kendisi değildir; **ekranın arkasındaki servistir**.

Sorular şunlardır: Hangi uç noktayı çağırıyor? Tutarı kim hesaplıyor? Kullanıcı butona iki kez basarsa ne oluyor? Başkasının başvurusunu görebiliyor mu? Kim ne zaman neyi değiştirdi? "Bankacılık kredi kullanıcı sayfaları yaptınız mı?" sorusunun teknik karşılığı budur.

### Ekran envanteri ve her birinin backend karşılığı

Kredi tarafında kullanıcının gördüğü ekranlar kabaca dokuz başlıkta toplanır. Her birinin arkasında ayrı bir sorumluluk vardır:

- **Kredi başvuru formu (çok adımlı sihirbaz):** Arkasında bir **taslak (draft)** kaydı vardır. Her adımda kısmi doğrulama yapılır ve veri sunucuda saklanır. Kullanıcı yarıda bırakıp döndüğünde kaldığı yerden devam eder. Taslak henüz "başvuru" değildir; durum makinesinde ayrı bir durumdur.
- **Taksit/ödeme planı simülasyonu:** Kullanıcı tutar ve vade seçtikçe anlık taksit tablosu görür. Bu **salt okunur** bir hesaplamadır ve kayıt açmaz. Ama hesabı yapan yer kritiktir; aşağıda ayrı bir başlıkta anlatılır.
- **Limit ve teklif görüntüleme:** Müşterinin kullanılabilir limitini ve ona özel teklifleri gösterir. Genellikle birden çok arka servisten derlenir. Ekranın ihtiyacı olan alanlar tek yanıtta toplanır.
- **Sözleşme onayı ve tek kullanımlık şifre (OTP) doğrulaması:** Kullanıcının onayladığı sözleşme metninin **sürümü** kayıt altına alınır. Hangi metni onayladığı sonradan kanıtlanabilmelidir.
- **Kullandırım/onay ekranı:** Paranın hesaba geçtiği andır. Sistemin en kritik ve geri alınması en pahalı işlemi buradadır.
- **Ödeme planı ve ekstre listeleme:** Sayfalama (pagination) ve tarih filtresi gerektirir. Büyük listelerde sayfa numarası yerine **anahtar tabanlı sayfalama (keyset)** tercih edilir. Sebebi şudur: araya yeni kayıt girdiğinde satırlar kaymaz.
- **Erken kapama tutarı sorgulama:** Belirli bir **ana** ait tutardır. Sorgu anı ile ödeme anı arasında değişebilir. Bu yüzden tutara bir geçerlilik süresi ve teklif kimliği verilir.
- **Taksit ödeme:** Para hareketi olduğu için tekrar koruması zorunludur.
- **Başvuru durum takibi:** Uzun süren karar sürecinin ekranda izlenmesidir. Sunucudaki durum belirli aralıklarla sorgulanır.

### Temel kavramlar

- **Durum makinesi (state machine):** Başvurunun alabileceği durumlar ve aralarındaki **geçerli geçişlerdir**. Durum alanı serbest metin gibi güncellenmez. Onun yerine "hangi durumdan hangisine geçilebilir" kuralı sunucuda tutulur ve geçersiz geçiş isteği reddedilir. Reddedilmiş bir başvurunun doğrudan kullandırıma geçmesi mümkün olmamalıdır.
- **Sunucu tarafı doğrulama:** İstemcideki doğrulama yalnız **kullanıcı deneyimi** içindir; kullanıcıya anında geri bildirim verir. Güvenlik ve veri bütünlüğü açısından hiçbir hükmü yoktur. Çünkü istek tarayıcı olmadan da gönderilebilir. Aynı kural sunucuda **tekrar** çalıştırılır.
- **Hata sözleşmesi:** Doğrulama başarısızsa istemciye tek bir "hata oldu" mesajı dönmez. **Alan bazlı** bir hata listesi döner: hangi alan, neden hatalı ve makine tarafından okunabilir bir hata kodu. Ekran bu listeyi ilgili girdi kutularının altına yazar.
- **Tekrar anahtarı (idempotency key):** İstemcinin ürettiği, aynı kullanıcı eylemine ait tekil bir anahtardır. Sunucu bu anahtarı kaydeder. Aynı anahtar tekrar gelirse işi ikinci kez yapmaz; ilk sonucun aynısını döner. Ayrıntısı `130` konusundadır.
- **Yeniden kimlik doğrulama (step-up authentication):** Oturum açık olsa bile yüksek riskli işlemde ek doğrulama istenmesidir; örneğin OTP ya da parola tekrarı. Oturumun varlığı, "bu tutarı kullandırma yetkin var" demek değildir.
- **Yatay yetki açığı (IDOR — dolaylı nesne referansı):** Kullanıcının **başka müşterinin** kaydını görmesidir. Bunu, adres satırındaki ya da isteğin gövdesindeki kimlik numarasını değiştirerek yapar. Rol denetimi bunu yakalamaz; çünkü rol doğrudur, kayıt yanlıştır.
- **Maskeleme:** Kimlik numarası, kart numarası ve IBAN gibi verilerin ekranda kısmen gösterilmesidir. Bu veriler **kayıtlara (log) hiç yazılmaz**.
- **Denetim izi (audit trail):** Şu bilginin ayrı ve değiştirilemez biçimde tutulmasıdır: kim, ne zaman, hangi kaydın hangi alanını, neyle değiştirdi.
- **Aşırı/eksik veri çekme:** Ekranın ihtiyacı olan alanların fazlasıyla ya da eksik dönmesidir. Fazla dönerse gereksiz hassas veri tarayıcıya iner. Eksik dönerse ekran arka arkaya çok sayıda istek atmak zorunda kalır.

Aşağıdaki diyagram başvurunun durumlarını ve geçerli geçişlerini gösterir; ekranın "şu an ne göstereceği" sorusunun cevabı da bu makinedir.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kredi başvurusu durum makinesi ve geçerli geçişler">
  <defs>
    <marker id="krediekr-sm-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="krediekr-sm-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Başvuru durum makinesi: yalnız çizili oklar geçerlidir</text>
  <rect x="20" y="48" width="120" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">TASLAK</text>
  <text x="80" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kısmi doğrulama</text>
  <rect x="184" y="48" width="120" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="244" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">GÖNDERİLDİ</text>
  <text x="244" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tam doğrulama</text>
  <rect x="348" y="48" width="120" height="40" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="408" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">SKORLAMADA</text>
  <text x="408" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">uzun sürebilir</text>
  <rect x="512" y="48" width="126" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="575" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">ONAYLI</text>
  <text x="575" y="81" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">teklif/limit hazır</text>
  <rect x="20" y="140" width="120" height="40" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="80" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">VAZGEÇİLDİ</text>
  <text x="80" y="173" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">süresi doldu</text>
  <rect x="348" y="140" width="120" height="40" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="408" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">REDDEDİLDİ</text>
  <text x="408" y="173" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">son durum</text>
  <rect x="512" y="140" width="126" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="575" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">SÖZLEŞME/OTP</text>
  <text x="575" y="173" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ek doğrulama</text>
  <rect x="512" y="228" width="126" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="575" y="246" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">KULLANDIRILDI</text>
  <text x="575" y="261" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">geri alınamaz</text>
  <path d="M140 68 L182 68" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#krediekr-sm-arw)"/>
  <path d="M304 68 L346 68" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#krediekr-sm-arw)"/>
  <path d="M468 68 L510 68" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#krediekr-sm-arw)"/>
  <path d="M408 88 L408 138" fill="none" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#krediekr-sm-red)"/>
  <path d="M575 88 L575 138" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#krediekr-sm-arw)"/>
  <path d="M575 180 L575 226" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#krediekr-sm-arw)"/>
  <path d="M80 88 L80 138" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#krediekr-sm-arw)"/>
  <text x="80" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">zaman aşımı</text>
  <text x="424" y="115" text-anchor="start" font-size="10" fill="#B23B3B" font-family="system-ui">olumsuz karar</text>
  <text x="590" y="115" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">onay</text>
  <text x="590" y="208" text-anchor="start" font-size="10" fill="#25804C" font-family="system-ui">OTP</text>
  <text x="330" y="292" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">REDDEDİLDİ → KULLANDIRILDI gibi çizilmemiş bir geçiş isteği sunucuda reddedilir (409)</text>
</svg>
```

### Nasıl çalışır?

1. **Taslak açılır.** Kullanıcı sihirbazın ilk adımını doldurur. Sunucu bir taslak kaydı oluşturup kimliğini döner. Her adımda **o adıma ait alanlar** doğrulanır; sonraki adımların boş olması hata değildir. Taslak sunucuda durduğu için tarayıcı kapansa da veri kaybolmaz.
2. **Simülasyon çağrılır.** Kullanıcı tutar ya da vade değiştirdikçe ekran, sunucudaki **hesaplama servisine** sorar ve dönen taksit tablosunu gösterir. İstek sık geldiği için bu uç nokta hızlı ve salt okunur olmalıdır. İstemci tarafında kısa bir bekletme (debounce) ile istek sayısı düşürülür.
3. **Başvuru gönderilir.** Sunucu **tüm** kuralları baştan doğrular ve durum `TASLAK → GÖNDERİLDİ` geçişini yapar. İstek bir **tekrar anahtarı** taşır. Kullanıcı butona iki kez bastıysa ikinci istek yeni başvuru açmaz; birincinin sonucunu döner.
4. **Karar süreci başlar.** Skorlama dış servislere de sorabildiği için saniyeler sürebilir. Bu yüzden istek anında sonuç dönmek yerine "kabul edildi, işleniyor" yanıtı verilir ve başvuru kimliği döndürülür. Ekran, **durum sorgulama** uç noktasını belirli aralıklarla çağırıp ilerlemeyi gösterir.
5. **Teklif gösterilir.** Başvuru onaylıysa limit ve teklif ekranı bir uç noktadan beslenir. Bu uç nokta, ekranın ihtiyacı olan alanları **tek istekte** derler. Hassas alanlar maskeli döner.
6. **Sözleşme onaylanır.** Kullanıcının gördüğü sözleşme metninin sürüm kimliği isteğe eklenir. Sunucu, "kullanıcının onayladığı sürüm" ile "yürürlükteki sürüm" aynı mı diye bakar. Yüksek tutarlı işlemde **yeniden kimlik doğrulama** (OTP) istenir.
7. **Kullandırım yapılır.** Para hareketi olduğu için istek yine tekrar anahtarı taşır. Sunucuda ayrıca bir **tekil iş anahtarı** üzerinde benzersizlik kısıtı bulunur; örneğin başvuru kimliği + işlem türü. Aynı limitten iki başvurunun eşzamanlı düşmesi ihtimaline karşı limit kaydı kilitlenir. Kilit stratejisi `225` konusundadır.
8. **Ödeme planı ve ekstre listelenir.** Tarih aralığı ve sayfa boyutu sunucuda sınırlanır; bir üst sınır konur. Sıralama alanı beyaz listeden seçilir. Sayfa boyutunu istemcinin serbestçe belirlemesine izin verilmez.
9. **Erken kapama sorgulanır.** Dönen tutar belirli bir ana aittir. Yanıt bir geçerlilik süresi ve teklif kimliği taşır. Ödeme isteği bu kimlikle gelir ve sunucu tutarı yeniden hesaplayıp doğrular.
10. **Her adım denetim izine yazılır.** Yalnız değiştirme değil, bazı durumlarda **görüntüleme** de kayıt gerektirir. Örneğin: hangi kullanıcı hangi müşterinin ekranını açtı?

Aşağıdaki diyagram bu akıştaki en sık mülakat sorusunu özetler: taksit hesabı nerede yapılmalı?

```svg
<svg viewBox="0 0 660 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Taksit hesabının istemcide ve sunucuda yapılmasının karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Taksit hesabı nerede yapılır?</text>
  <rect x="22" y="44" width="292" height="164" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="168" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B23B3B" font-family="system-ui">YANLIŞ: tarayıcıda hesapla</text>
  <text x="168" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">formül ön yüz koduna kopyalanır</text>
  <text x="168" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kayan noktalı sayı → kuruş farkı</text>
  <text x="168" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kural değişince iki yer güncellenir</text>
  <text x="168" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">vitrindeki taksit ≠ sözleşmedeki</text>
  <text x="168" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tutar istemciden gelirse değiştirilebilir</text>
  <text x="168" y="192" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">müşteri şikâyeti + denetim bulgusu</text>
  <rect x="346" y="44" width="292" height="164" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="492" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">DOĞRU: tek hesaplama motoru</text>
  <text x="492" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">simülasyon ve kullandırım aynı servisi çağırır</text>
  <text x="492" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ondalık duyarlı tür + belirlenmiş yuvarlama</text>
  <text x="492" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kuruş farkı bilinçli bir taksite yazılır</text>
  <text x="492" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ekran yalnız gösterir, karar vermez</text>
  <text x="492" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tutar sunucuda yeniden doğrulanır</text>
  <text x="492" y="192" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">vitrin ile kasa hep aynı sonucu verir</text>
</svg>
```

### Para ve hesaplama

Kredi ekranlarının en sinsi hatası, taksit formülünün ön yüz koduna kopyalanmasıdır. İki ayrı yerde iki ayrı hesap yaşamaya başlar. Kural değiştiğinde biri güncellenir, diğeri unutulur. Doğru desen tektir: **simülasyon sonucu ile gerçek kullandırım aynı hesaplama motorundan geçer**. Ekranın gösterdiği taksit, sözleşmede yazacak taksitin ta kendisidir.

Bunun teknik gerekçeleri şunlardır:

- **Sayı türü.** Tarayıcının varsayılan sayı türü ikili kayan noktalıdır ve para için uygun değildir. Sunucuda ondalık duyarlı tür kullanılır (`226` BigDecimal konusu). Aynı formül, iki farklı sayı dünyasında iki farklı kuruş üretir.
- **Yuvarlama ve kuruş farkı.** Anapara vadeye tam bölünmediğinde bir kuruş farkı çıkar. Bu farkın hangi taksite yazılacağı bir **iş kararıdır**; yuvarlama tesadüfü değildir. Bu karar tek bir yerde verilmelidir.
- **Tutarın taşınması.** Tutar yanıtta iki bilgiyle birlikte gider. Birincisi makine tarafından işlenecek biçimdir; ondalık değeri dizgi olarak taşımak yaygın bir tercihtir ve kayan noktalı sayıya çevrilmesi engellenir. İkincisi **para birimi** bilgisidir. Ekranın göstereceği biçimlendirilmiş metni ayrıca dönmek de faydalıdır; böylece ekran kendi kendine yuvarlamaz.
- **İstemciden gelen tutara güvenilmez.** "Ekran zaten doğru tutarı gönderiyor" varsayımı bir güvenlik açığıdır. Kullandırım isteğinde gelen tutar sunucuda yeniden hesaplanır ve uyuşmazsa istek reddedilir.

### Eşzamanlılık ve tekrar

Ekran tarafında dört klasik tekrar senaryosu vardır:

- **Çift tıklama.** Kullanıcı "Onayla" butonuna iki kez basar. Butonu ilk tıklamada devre dışı bırakmak yalnız bir kolaylıktır, çözüm değildir. Çünkü ağ gecikmesinde tarayıcı isteği kendisi de tekrarlayabilir. Çözüm **tekrar anahtarıdır**. Bu anahtar, ekranın o eylem için ürettiği ve tekrar denemelerde **değiştirmediği** bir değerdir.
- **Tarayıcı geri tuşu ve formun yeniden gönderilmesi.** Aynı işlem ikinci kez gönderilir. Sunucudaki durum makinesi, zaten `KULLANDIRILDI` olan bir başvuruya ikinci kullandırımı reddeder ve ekrana anlaşılır bir çakışma hatası döner.
- **Aynı limitten eşzamanlı iki başvuru.** İki istek aynı anda limiti okur, ikisi de yeterli görür ve ikisi de düşer; böylece limit aşılır. Bu bir kilit sorunudur. İyimser ve kötümser kilit ayrıntısı `225` konusundadır.
- **Ödeme talebinin çift işlenmesi.** Ağ zaman aşımında ekran isteği tekrarlar ama ilk istek aslında başarılı olmuştur. Tekil iş anahtarı üzerindeki benzersizlik kısıtı, ikinci kaydı veritabanı seviyesinde engeller. Uygulama da bu hatayı "zaten yapılmış" olarak yorumlar.

### Oturum ve güvenlik

Bankacılık ekranlarında oturum ömrü bilinçli olarak kısadır. Hareketsizlik süresi dolduğunda oturum düşer. Kullanıcıyı hiç uyarmadan düşürmek doğru değildir; ekran süre dolmadan önce bilgilendirir. Kullanıcının taslağını kaybetmemesi için de taslak zaten sunucuda tutulur.

Oturumun tarayıcıya nasıl taşındığı (çerez, belirteç) `211` konusundadır. Tarayıcı sınırının kendisi `210`, kimlik doğrulama altyapısı ise `86`/`87` konularındadır. Burada önemli olan üç kural vardır:

1. **Yetkilendirme her istekte sunucuda yapılır.** Ekranda butonu gizlemek bir yetki denetimi değildir; yalnız görsel bir düzenlemedir. İstek doğrudan da gönderilebilir. "Kullanıcı zaten o ekranı göremiyor" cümlesi mülakatta eleyici bir cevaptır.
2. **Rol denetimi tek başına yetmez; kayıt sahipliği de denetlenir.** Kullanıcının rolü doğru olabilir ama istediği kayıt başkasının olabilir. Sorgu, oturumdaki müşteri kimliğiyle **daraltılarak** çalıştırılır. Kayıt bulunduktan sonra sahipliği kontrol etmek de kabul edilebilir. Ama kontrol atlanırsa **yatay yetki açığı** doğar. Tahmin edilmesi zor kimlikler kullanmak (sıralı sayı yerine rastgele kimlik) yardımcıdır ama tek başına denetim yerine geçmez.
3. **Yüksek riskli işlemde yeniden kimlik doğrulama.** Yüksek tutarlı kullandırım ya da hesap değişikliği gibi işlemlerde oturumun varlığı yetmez; ek doğrulama istenir.

Hassas veri tarafında iki ayrı sorumluluk vardır. Birincisi **gösterimdir**: kimlik numarası, kart numarası ve IBAN ekranda kısmi gösterilir. İkincisi **kayıttır**: bu veriler uygulama kayıtlarına, hata izlerine ve istek/yanıt günlüklerine **hiç yazılmaz**.

Uygulama kayıtları geniş bir ekip tarafından görülebilir. Bu yüzden buradaki sızıntı, en yaygın kişisel veri ihlali biçimidir. Kişisel verilerin işlenmesi KVKK kapsamındadır ve ihlalin sonucu yalnız teknik değil, hukukidir. Güvenlik tarafının derinliği `189`/`190` konularındadır.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ekranda buton gizlemek ile sunucuda yetki denetimi karşılaştırması">
  <defs>
    <marker id="krediekr-auth-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yetki: ekranda gizlemek değil, sunucuda denetlemek</text>
  <rect x="22" y="44" width="256" height="170" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="150" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yalnız ekranda gizlemek</text>
  <text x="150" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">buton görünmüyor → yetki var sanılır</text>
  <text x="150" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istek tarayıcı olmadan da atılabilir</text>
  <text x="150" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">adresteki kimliği değiştir → IDOR</text>
  <text x="150" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istemci doğrulaması yalnız deneyimdir</text>
  <text x="150" y="192" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">başka müşterinin verisi okunur</text>
  <rect x="310" y="44" width="328" height="170" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="474" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Sunucuda üç kapı, her istekte</text>
  <rect x="330" y="82" width="288" height="32" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="474" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1. Kimlik doğrulama: bu istek kimden geliyor?</text>
  <rect x="330" y="122" width="288" height="32" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="474" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2. Yetki/rol: bu işlemi yapabilir mi?</text>
  <rect x="330" y="162" width="288" height="32" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="474" y="182" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">3. Sahiplik: bu KAYIT ona mı ait? (IDOR kapısı)</text>
  <path d="M474 114 L474 120" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#krediekr-auth-arw)"/>
  <path d="M474 154 L474 160" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#krediekr-auth-arw)"/>
  <text x="330" y="228" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">Ek olarak: yüksek tutarda yeniden kimlik doğrulama (step-up) · hassas veri kayda yazılmaz</text>
</svg>
```

### Denetim izi

Kredi ekranlarında "kim ne yaptı" sorusunun cevabı, uygulamanın kendi kayıt dosyalarından verilmez. Cevap, ayrı tutulan **denetim izinden** verilir. Denetim izi kaydında en az şunlar bulunur: eylemi yapan kullanıcı, eylemin zamanı, hangi kayıt, hangi alan, eski ve yeni değer, isteğin izlenebilir kimliği. Üç ayırt edici nokta vardır:

- **Görüntüleme de kayıt gerektirebilir.** Müşteri temsilcisinin hangi müşterinin ekranını açtığı, kötüye kullanım incelemelerinde en çok bakılan bilgidir.
- **Geri alınamaz işlemler silinerek düzeltilmez.** Yanlış kullandırımın kaydı silinmez. Bir **ters kayıt**, yani düzeltici bir işlem atılır ve ikisi de izde durur. Denetim izi yalnız ekleme yapılabilen (append-only) bir yapıdır.
- **Denetim izi hassas veriyi çıplak taşımaz.** İzin amacı sorumluluk zinciri kurmaktır. Kart numarasının tamamını ize yazmak gerekmez.

### Ön yüzle çalışma

Ekranı başka bir ekip yazıyorsa iş, **sözleşme** üzerinden yürür. Önce OpenAPI tanımı yazılır; iki taraf ona göre paralel ilerler. Ayrıntısı `212` (API sözleşmesi) ve `213` (ön yüz ekibiyle işbirliği) konularındadır. Burada backend tarafına düşen iki karar vardır:

- **Ekran odaklı derleme.** Limit ekranı üç ayrı servisten besleniyorsa tarayıcıya üç istek attırma. Sunucu tarafında derlenmiş tek bir yanıt dön; aksi hâlde eksik veri çekme sorunu doğar. Ters uçta "her ihtimale karşı her alanı dönmek" de yanlıştır; o zaman gereksiz hassas veri tarayıcıya iner (aşırı veri çekme).
- **Tarayıcı sınırı.** Farklı alan adından yapılan çağrılarda iki konu vardır: tarayıcının ön kontrol isteği ve oturum bilgisinin taşınması. Bunlar `210` ve `211`'de anlatılır. Backend'in yapması gereken, bu ayarları "her yere izin ver" biçiminde gevşetmemektir.

### Erişilebilirlik ve dayanıklılık

- **Ağ koparsa taslak kaybolmamalıdır.** Taslağın sunucuda tutulmasının asıl sebebi budur. Tarayıcı belleğinde tutulan form, sekme kapanınca gider.
- **Zaman aşımında kullanıcıya ne gösterilir?** "Hata oluştu" demek yanlıştır. Çünkü işlem arka planda **başarılı olmuş olabilir**. Doğru davranış, işlem kimliğiyle durum sorgulamaktır. Kullanıcıya da "işleminiz alındı, sonucu kontrol ediliyor" denir. İstek tekrar denenecekse aynı tekrar anahtarıyla denenir.
- **Arka uç servisi yavaşsa.** Skorlama gibi bir bağımlılık yavaşladığında ekranın sonsuza kadar dönen bir simge göstermesi kabul edilemez. Üç önlem uygulanır: zaman aşımı, yeniden deneme sınırı ve devre kesici (circuit breaker). Kullanıcıya da bir geri çekilme (fallback) yolu sunulur; örneğin "kararınız hazırlanıyor, sonucu bildireceğiz". Ayrıntısı `101` konusundadır.

### Mülakatta ne sorulur / tuzaklar

- **"Taksit hesabını nerede yaptınız?"** Beklenen cevap şudur: sunucuda ve tek hesaplama motorunda; ekran yalnız gösterir. "Kullanıcı anında görsün diye tarayıcıda hesapladık" cevabı, tutarsızlık ve kuruş farkı riskini kabul etmek demektir.
- **"Kullanıcı iki kez tıklarsa?"** Beklenen cevap: tekrar anahtarı + sunucuda tekil iş anahtarı. "Butonu devre dışı bırakıyoruz" tek başına yetersizdir.
- **"Ekranda o butonu göremeyen kullanıcı o işlemi yapabilir mi?"** Beklenen cevap: evet, yapabilir. Bu yüzden yetki her istekte sunucuda denetlenir.
- **"Başka müşterinin başvurusunu görebilir mi?"** Cevap iki şeyi içermelidir: yatay yetki açığını (IDOR) ve sorgunun oturumdaki müşteriyle daraltılmasını.
- **"Doğrulamayı nerede yaptınız?"** Her iki tarafta. İstemcide deneyim için, sunucuda zorunlu olarak. "Yalnız istemcide" demek eleyicidir.
- **"Kredi kararı 10 saniye sürüyor, ekran ne yapıyor?"** Beklenen cevap: isteği kabul edip bir kimlik döndürmek, sonra durum sorgulamayla ilerlemeyi göstermek. İsteği açık tutup kullanıcıyı bekletmek değil.
- **"Hangi verileri kayda yazdınız?"** Kimlik numarası, kart numarası ve IBAN gibi veriler kayda yazılmaz. Ekranda maskeli gösterilir.
- **Sık yapılan hata:** durum alanını serbestçe güncellemek. Durum makinesi olmadan "reddedilmiş" bir başvuru kullandırıma kadar gidebilir. Geçersiz geçiş sunucuda reddedilmelidir.

### Tek cümlede

Kredi ekranının arkasındaki backend, başvuruyu bir durum makinesiyle yürütür ve taslağı sunucuda saklar. Taksiti hem simülasyonda hem kullandırımda **aynı hesaplama motorundan** üretir. Tekrarlanan istekleri tekrar anahtarıyla tekilleştirir. Yetkiyi her istekte üç kapıda denetler: kimlik, rol ve sahiplik. Hassas veriyi maskeler ve kayda yazmaz. Son olarak her anlamlı eylemi denetim izine düşer.

# krediekr-1 | Orta
## Soru
Kredi başvuru ekranında kullanıcı tutar ve vadeyi değiştirdikçe taksit tablosunun anında güncellenmesi isteniyor. Hesaplamanın nerede yapılması doğrudur ve neden?
## A
Tarayıcıda hesaplanmalı; her tuşta sunucuya gitmemek gecikmeyi düşürür ve kullandırım anında sunucu zaten kendi hesabını yapacağı için fark önemsizdir
🤖 iki ayrı hesap iki ayrı sonuç üretir; ekranda gösterilen taksitle sözleşmedeki taksitin farklı çıkması kabul edilemez.
## B
Tarayıcıda hesaplanmalı ve bulunan taksit tutarı kullandırım isteğinin gövdesinde sunucuya gönderilmeli; böylece sunucu hesaplama yükünden kurtulur
🤖 istemciden gelen tutara güvenmek hem tutarsızlık hem güvenlik açığıdır; tutar sunucuda yeniden hesaplanmalıdır.
## C
Sunucudaki tek hesaplama motorunda hesaplanmalı; simülasyon ile gerçek kullandırım aynı servisi çağırmalı, ekran yalnız sonucu göstermeli
🤖 Doğru: tek kaynak hem kuruş farkını hem kural değişikliklerinde ikiye ayrışmayı ortadan kaldırır.
## D
Formül hem tarayıcıya hem sunucuya yazılmalı; iki taraf da aynı formülü uyguladığı için sonuçların eşit çıkacağı garanti edilir
🤖 aynı formül farklı sayı türleriyle farklı kuruş üretir ve kural değişince iki koddan biri güncellenmeden kalır.
## Doğru
C
## Açıklama
Kredi ekranlarında **simülasyon sonucu ile gerçek kullandırımın aynı hesaplama motorundan geçmesi** temel kuraldır; ekranın gösterdiği taksit, sözleşmede yazacak taksitin ta kendisi olmalıdır. Hesabı tarayıcıya taşımak üç sorun doğurur. Birincisi **sayı türü**: tarayıcının varsayılan sayı türü ikili kayan noktalıdır, para hesabına uygun değildir; sunucuda ondalık duyarlı tür kullanılır (`226`). İkincisi **yuvarlama ve kuruş farkı**: anapara vadeye tam bölünmediğinde artan kuruşun hangi taksite yazılacağı bir iş kararıdır ve tek yerde verilmelidir. Üçüncüsü **bakım**: formül iki yerde yaşarsa kural değiştiğinde biri güncellenir, diğeri unutulur — "iki tarafa da aynı formülü yazarız" diyen şık bu yüzden yanlıştır. Hesabı tarayıcıda yapıp tutarı sunucuya göndermek ayrıca bir **güvenlik** hatasıdır: istemciden gelen tutara güvenilmez, sunucu tutarı yeniden hesaplayıp uyuşmazsa isteği reddeder. Performans kaygısı gerçek olsa da çözümü hesabı taşımak değil, simülasyon uç noktasını salt okunur ve hızlı tutup istemci tarafında kısa bir bekletme (debounce) ile istek sayısını azaltmaktır.

# krediekr-2 | İleri
## Soru
Kullandırım ekranında kullanıcı "Onayla" butonuna arka arkaya iki kez basıyor ve bazı durumlarda iki kayıt oluştuğu görülüyor. Bu sorunu kalıcı olarak çözen yaklaşım hangisidir?
## A
İstek bir idempotency anahtarı taşımalı; sunucu bu anahtarı kaydedip aynı anahtarla gelen tekrarda işi yeniden yapmadan ilk sonucu dönmeli, ayrıca tekil iş anahtarında benzersizlik kısıtı bulunmalı
🤖 Doğru: tekrar koruması sunucuda kalıcılaşır ve tarayıcının, ağın ya da kullanıcının ürettiği her tekrarı kapsar.
## B
Buton ilk tıklamada devre dışı bırakılmalı ve ekranda bir bekleme göstergesi açılmalı; kullanıcı ikinci kez tıklayamayınca çift kayıt sorunu ortadan kalkar
🤖 bu yalnız kullanıcı deneyimi önlemidir; ağ zaman aşımında tarayıcı ya da kullanıcı isteği yine tekrarlayabilir.
## C
Kullandırım isteği gönderilmeden hemen önce sunucuya "bu başvuru için kayıt var mı" sorusu sorulmalı; kayıt varsa ikinci istek gönderilmemeli
🤖 kontrol ile yazma arasında yarış penceresi kalır; eşzamanlı iki istek de "kayıt yok" cevabını alabilir.
## D
Kullandırım uç noktası yeniden deneme yapılmayacak biçimde işaretlenmeli ve ağ hatalarında ekran isteği asla tekrarlamamalı; böylece ikinci istek hiç oluşmaz
🤖 tekrarı yasaklamak, zaman aşımında sonucu bilinmeyen işlemi çözümsüz bırakır; sorun tekrarda değil tekrarın tekilleştirilmemesindedir.
## Doğru
A
## Açıklama
Çift kayıt sorununun kaynağı "kullanıcının iki kez tıklaması" değil, **aynı işin ikinci kez yapılabilmesidir**. Butonu devre dışı bırakmak yalnız görsel bir önlemdir: ağ zaman aşımında tarayıcı isteği kendisi tekrarlayabilir, kullanıcı sayfayı yenileyebilir veya geri tuşuyla formu yeniden gönderebilir. Kalıcı çözüm **idempotency anahtarıdır**: ekran o kullanıcı eylemi için tekil bir anahtar üretir, tekrar denemelerinde **aynı anahtarı** gönderir; sunucu anahtarı kaydeder ve aynı anahtarı ikinci kez gördüğünde işi tekrar yapmadan ilk sonucun aynısını döner (`130`). Bunun altına ikinci bir emniyet konur: **tekil iş anahtarı** (ör. başvuru kimliği + işlem türü) üzerinde veritabanı benzersizlik kısıtı; böylece uygulama katmanı atlansa bile ikinci kayıt veritabanında engellenir. "Önce sorgula, yoksa yaz" yaklaşımı klasik bir yarış koşuludur: eşzamanlı iki istek de kontrolü geçip ikisi de yazabilir — kontrol ile yazma arasındaki pencere ancak kilit veya benzersizlik kısıtıyla kapanır. Yeniden denemeyi tümden yasaklamak da çözüm değildir; zaman aşımında işlemin başarılı olup olmadığı bilinmediği için ekranın ya aynı anahtarla tekrar denemesi ya da durum sorgulaması gerekir.

# krediekr-3 | İleri
## Soru
Bir denetimde, oturum açmış bir müşterinin başvuru detay adresindeki kimlik numarasını değiştirerek başka bir müşterinin başvurusunu görüntüleyebildiği tespit ediliyor. Bu açığın doğru tanımı ve çözümü nedir?
## A
Bu bir kimlik doğrulama açığıdır; oturum süresi kısaltılıp hareketsizlik zaman aşımı düşürülerek ve yüksek riskli ekranlarda tekrar giriş istenerek kapatılır
🤖 kullanıcı zaten geçerli biçimde kimliklenmiştir; sorun kimlikte değil, o kayda erişme hakkındadır.
## B
Bu bir rol açığıdır; müşteri rolüne başvuru görüntüleme yetkisi verilmemeli, bu ekran yalnız yetkili personel rolüne açılmalıdır
🤖 rol denetimi doğrudur ve müşterinin kendi başvurusunu görmesi gerekir; eksik olan kaydın kime ait olduğunun kontrolüdür.
## C
Bu bir ekran açığıdır; detay bağlantısı yalnız kullanıcının kendi başvuru listesinde gösterilmeli, listede olmayan kaydın bağlantısı hiç üretilmemelidir
🤖 ekranda bağlantı üretmemek isteğin elle gönderilmesini engellemez; denetim sunucuda yapılmalıdır.
## D
Bu bir yatay yetki (IDOR) açığıdır; sorgu oturumdaki müşteri kimliğiyle daraltılmalı ve kaydın sahipliği her istekte sunucuda denetlenmelidir
🤖 Doğru: yetkilendirme her istekte sunucuda yapılır ve rol denetiminin üstüne kayıt sahipliği denetimi eklenir.
## Doğru
D
## Açıklama
Bu klasik **yatay yetki açığıdır (IDOR — dolaylı nesne referansı)**: kullanıcı doğru biçimde kimliklenmiştir, rolü de doğrudur, ama **kendisine ait olmayan bir kayda** erişmektedir. Bu yüzden ne kimlik doğrulama (oturum süresi, tekrar giriş) ne de rol tanımı bu açığı kapatır; oturum kısaltmak yalnız çalınmış oturumun ömrünü kısaltır, müşterinin kendi başvurusunu görmesi ise zaten olması gereken davranıştır. Ekran tarafında bağlantı üretmemek de çözüm değildir: **istek tarayıcı olmadan da gönderilebilir**, ekranda butonu gizlemek veya bağlantı basmamak bir yetki denetimi değil görsel bir düzenlemedir. Doğru çözüm yetkilendirmenin **her istekte sunucuda** ve üç kapı olarak yapılmasıdır: (1) kimlik doğrulama — istek kimden geliyor, (2) yetki/rol — bu işlemi yapabilir mi, (3) **sahiplik** — bu kayıt ona mı ait. Uygulamada en sağlam yol sorguyu oturumdaki müşteri kimliğiyle **daraltmaktır** (kayıt zaten başkasının ise hiç dönmez); kaydı çekip sonra sahipliği kontrol etmek de kabul edilebilir, yeter ki kontrol atlanmasın. Kimlikleri tahmin edilmesi zor değerler yapmak (sıralı sayı yerine rastgele kimlik) saldırıyı zorlaştırır ama denetimin yerine geçmez.

# krediekr-4 | Orta
## Soru
Kredi başvuru sihirbazında tüm alan kuralları (zorunluluk, tutar aralığı, vade seçenekleri) ekran tarafında ayrıntılı biçimde uygulanmış durumda. Sunucu tarafındaki doğrulama için hangisi doğrudur?
## A
Ekrandaki doğrulama yeterlidir; aynı kuralları sunucuda tekrarlamak kod tekrarı yaratır ve tek yapılması gereken veritabanı kısıtlarına güvenmektir
🤖 istek tarayıcı olmadan da gönderilebilir; veritabanı kısıtları iş kurallarının yalnız küçük bir kısmını karşılar.
## B
Sunucu tüm kuralları bağımsız olarak yeniden doğrulamalı ve başarısızlıkta alan bazlı hata listesi dönmelidir; ekrandaki doğrulama yalnız kullanıcı deneyimi içindir
🤖 Doğru: güvenlik ve veri bütünlüğü yalnız sunucuda sağlanabilir, ekran ise anında geri bildirim için doğrular.
## C
Sunucu yalnız ekranın kontrol edemediği kuralları (limit yeterliliği gibi) doğrulamalı, biçimsel alan kurallarını ekrana bırakmalıdır
🤖 biçimsel kurallar da sunucuda doğrulanmazsa geçersiz veri doğrudan gönderilen isteklerle sisteme girer.
## D
Sunucu doğrulama başarısızlığında tek bir genel hata mesajı dönmeli, ayrıntıyı ekrana vermemelidir; alan bazlı ayrıntı vermek bilgi sızdırır
🤖 kendi gönderdiği verinin hangi alanının neden hatalı olduğunu kullanıcıya söylemek sızıntı değildir, kullanılabilirlik gereğidir.
## Doğru
B
## Açıklama
İstemci tarafındaki doğrulama **yalnız kullanıcı deneyimi** içindir: kullanıcıya anında geri bildirim verir, gereksiz gidiş dönüşü azaltır. Güvenlik ve veri bütünlüğü açısından hiçbir hükmü yoktur, çünkü istek tarayıcı olmadan da gönderilebilir; bu yüzden aynı kurallar sunucuda **bağımsız olarak yeniden** çalıştırılır. Bu bir "kod tekrarı" değil, iki farklı sorumluluktur; veritabanı kısıtları da yalnız biçimsel bütünlüğün küçük bir kısmını karşılar, vade seçenekleri veya tutar aralığı gibi iş kuralları oraya sığmaz. Doğrulamayı bölmek — "biçimsel kurallar ekranda, iş kuralları sunucuda" — de yanlıştır: doğrudan gönderilen bir istek biçimsel kuralları hiç uygulamaz ve geçersiz veri sisteme girer. Sunucunun **hata sözleşmesi** de önemlidir: tek bir genel mesaj yerine **alan bazlı** hata listesi döner (hangi alan, makine tarafından okunabilir hata kodu, kullanıcıya gösterilecek metin), ekran bu listeyi ilgili girdi kutusunun altına yazar. Kullanıcının kendi gönderdiği verinin neden reddedildiğini söylemek bilgi sızdırmak değildir; sızıntı riski, sistemin iç ayrıntılarını (yığın izi, sorgu metni, iç servis adı) yanıta koymakla doğar — onlar dışarı verilmez.

# krediekr-5 | Uzman
## Soru
Kredi kullandırım ekranında, oturumu açık olan bir kullanıcı yüksek tutarlı bir işlemi onaylıyor. Bu adımın güvenlik tasarımı için en doğru yaklaşım hangisidir?
## A
Oturum açıkken yapılan işlemler zaten doğrulanmış sayılır; ek doğrulama istemek kullanıcıyı yorar ve kullandırımın tamamlanma oranını düşürür
🤖 oturumun varlığı yalnız kimliği gösterir, yüksek riskli tek bir işlem için yetkinin tazelendiğini göstermez.
## B
Yüksek tutarlı işlemlerde oturum tümüyle sonlandırılıp kullanıcıdan baştan giriş yapması istenmeli, işlem yeni oturumda tekrar kurulmalıdır
🤖 oturumu sonlandırmak taslağı ve işlem bağlamını kaybettirir; gereken tazeleme, oturumu yıkmak değildir.
## C
Oturuma ek olarak işleme özel yeniden kimlik doğrulama (step-up) istenmeli; onaylanan sözleşme sürümü ve doğrulama sonucu denetim izine yazılmalıdır
🤖 Doğru: risk arttıkça doğrulama tazelenir ve işlemin hangi metne, hangi doğrulamayla dayandığı sonradan kanıtlanabilir olur.
## D
Ek doğrulama sunucuda değil ekranda yapılmalı; ekran doğrulamayı tamamladıktan sonra kullandırım isteğine "doğrulandı" bilgisini bayrak olarak eklemelidir
🤖 istemcinin gönderdiği bir bayrağa güvenmek doğrulamayı tümüyle etkisizleştirir; sonucu sunucu üretmeli ve doğrulamalıdır.
## Doğru
C
## Açıklama
Bankacılıkta oturumun varlığı ile **belirli bir işlemi yapma yetkisi** aynı şey değildir. Oturum, kullanıcının kim olduğunu gösterir; yüksek tutarlı kullandırım gibi geri alınması pahalı bir işlemde doğrulamanın **tazelenmesi** gerekir — buna **yeniden kimlik doğrulama (step-up authentication)** denir ve tek kullanımlık şifre (OTP), parola tekrarı gibi ek bir adımla yapılır. Oturumu tümüyle sonlandırıp baştan giriş istemek bunun kaba ve zararlı bir biçimidir: kullanıcı işlem bağlamını ve akışın ortasındaki durumunu kaybeder; amaç oturumu yıkmak değil, o tek işlem için yetkiyi tazelemektir. Doğrulamanın **ekranda** yapılıp sunucuya "doğrulandı" bayrağı gönderilmesi ise doğrulamayı tümüyle etkisiz kılar; istemciden gelen hiçbir güvenlik iddiasına güvenilmez, doğrulamayı sunucu üretir ve sunucu tüketir. Son parça **kanıtlanabilirliktir**: kullanıcının onayladığı sözleşme metninin **sürüm kimliği** ve yapılan doğrulamanın sonucu, kullanıcı/zaman/kayıt bilgisiyle birlikte **denetim izine** yazılır. Böylece sonradan "müşteri hangi metni, hangi doğrulamayla onayladı" sorusuna cevap verilebilir; geri alınamaz bir işlem düzeltilecekse kayıt silinmez, **ters kayıt** atılır ve iki kayıt da izde durur.

# krediekr-6 | İleri
## Soru
Başvuru gönderildikten sonra skorlama ve karar süreci dış servis sorguları nedeniyle bazen 10-15 saniye sürüyor. Ekran ve API bu durumu nasıl ele almalıdır?
## A
İstek sunucuda açık tutulmalı ve karar dönene kadar yanıt verilmemeli; ekranda bekleme göstergesi dönmeli, gerekirse istemci zaman aşımı süresi uzatılmalıdır
🤖 uzun tutulan istek bağlantı ve iş parçacığı kaynaklarını tüketir, aradaki bileşenlerin zaman aşımlarına takılır ve sonucu belirsiz bırakır.
## B
Sunucu isteği kabul edip başvuru kimliğini hemen dönmeli, ekran durum sorgulama uç noktasıyla ilerlemeyi izlemeli; skorlama çağrısında zaman aşımı ve devre kesici bulunmalıdır
🤖 Doğru: uzun süren iş asenkron hale gelir, kullanıcı durumu görebilir ve yavaş bağımlılık ekranı kilitlemez.
## C
Skorlama çağrısı ekrandan doğrudan yapılmalı; tarayıcı dış servisi kendisi çağırırsa sunucu kaynakları meşgul olmaz ve gecikme kullanıcıya yansımaz
🤖 karar mantığını ve dış servis erişimini tarayıcıya taşımak hem güvenlik hem tutarlılık açısından kabul edilemez.
## D
Zaman aşımına uğrayan istekte ekran kullanıcıya "işlem başarısız" demeli ve kullanıcıdan başvuruyu baştan doldurmasını istemelidir
🤖 zaman aşımı başarısızlık anlamına gelmez; işlem arka planda başarılı olmuş olabilir, durum sorgulanmalıdır.
## Doğru
B
## Açıklama
Uzun süren işlerde doğru desen, isteği **kabul edip kimliğini dönmek** ve sonucu ayrı bir **durum sorgulama** uç noktasından izletmektir: sunucu başvuruyu `SKORLAMADA` durumuna alır, ekran belirli aralıklarla durumu sorup kullanıcıya ilerlemeyi gösterir. İsteği açık tutup yanıtı geciktirmek kötü ölçeklenir — bağlantı ve iş parçacığı kaynakları meşgul kalır, aradaki yük dengeleyici ve ağ geçidi zaman aşımlarına takılır ve kullanıcı sonucu hiç öğrenemeyebilir. Skorlama çağrısını tarayıcıya taşımak ise karar mantığını ve dış servis erişimini istemciye açmak demektir; kredi kararı sunucuda verilir. Zaman aşımında "işlem başarısız" demek de yanlıştır: **zaman aşımı sonucun bilinmemesidir**, işlem arka planda tamamlanmış olabilir; ekran işlem kimliğiyle durumu sorgular, tekrar deneyecekse aynı **idempotency anahtarıyla** dener. Bağımlılık yavaşladığında ekranın sonsuza kadar dönen bir simge göstermemesi için skorlama çağrısına zaman aşımı, sınırlı yeniden deneme ve **devre kesici (circuit breaker)** konur; devre açıldığında kullanıcıya "kararınız hazırlanıyor, sonucu bildireceğiz" gibi bir geri çekilme yolu sunulur (`101`).
