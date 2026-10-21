---
slug: api-sozlesmesi-on-yuz-gozuyle
baslik: "API Sözleşmesi Ön Yüz Gözüyle: N+1 İstek, Pagination, ProblemDetail ve BFF"
kademe: Orta
sira: 212
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoranda garsonsun. Müşteri "menüdeki 20 yemeği görmek istiyorum" diyor. Bir yol var: masaya boş bir liste bırakıp her yemek için müşterinin ayrı ayrı mutfağa gidip "bunun içinde ne var, kaç lira?" diye sormasını beklemek — 21 gidiş-geliş. Bir yol daha var: listeyi getirirken herkesin merak edeceği bilgiyi (ad, fiyat, görsel) zaten yanına yazmak — tek gidiş. Yemekler aynı, mutfak aynı; değişen tek şey **sen sözleşmeyi nasıl kurdun**.

Senin yazdığın API tam olarak bu menüdür. Ön yüz (frontend) geliştiricisi onu tüketir; ekranın hızlı mı yavaş mı açıldığı, hata mesajının doğru kutunun altında mı çıktığı, uygulamanın eski sürümünün bugün çalışıp çalışmadığı büyük ölçüde **senin backend kararlarına** bağlıdır. Bu konu "ön yüz yazmayı öğren" değil; **"senin API'n ön yüzü nasıl mutlu ya da mutsuz eder"** konusudur. Mülakatta "ön yüz ekibiyle çalıştın mı?" diye sorulduğunda gerçekte ölçülen budur.

### Temel kavramlar

- **API sözleşmesi (API contract)**: Uçların adresleri, aldıkları girdi, döndükleri alanlar, tipleri ve hata biçimi — istemciyle aranızdaki yazılı olmayan (ya da OpenAPI ile yazılı) anlaşma.
- **Ağ turu (round trip) ve geveze/iri arayüz (chatty vs chunky)**: Bir isteğin gidip yanıtın dönmesi bir turdur; her turun sabit bir gecikme bedeli vardır. Çok sayıda küçük istek isteyen arayüz **geveze**, az sayıda dolu istek isteyen arayüz **iri**dir.
- **İstemci tarafı N+1 (client-side N+1)**: Liste ucundan N kayıt alıp her kayıt için ayrı bir detay isteği atmak; toplam 1+N HTTP turu.
- **Aşırı ve eksik veri çekme (over-fetching / under-fetching)**: Yanıtın ekranın kullanmadığı alanlarla şişmesi (aşırı), ya da ekranı çizmeye yetmeyip ek istek gerektirmesi (eksik).
- **Sayfalama (pagination), offset/limit**: "Baştan 20 kayıt atla, sonraki 20'yi ver" mantığıyla veriyi parça parça vermek.
- **İmleç tabanlı sayfalama (cursor/keyset pagination)**: "Şu kayıttan sonrakini ver" mantığı; sayfa numarası yerine son görülen kaydı işaret eden bir imleç taşınır.
- **Zarf (envelope) ve başlıkla taşıma (header)**: Sayfalama bilgisini gövdenin içine sarmalayarak (`{"veri": [...], "toplam": 340}`) ya da `X-Total-Count` / `Link` gibi HTTP başlıklarıyla vermek.
- **`ProblemDetail` (RFC 9457)**: Hatalar için standart JSON gövdesi; `type`, `title`, `status`, `detail`, `instance` alanlarını taşır.
- **Makinece okunabilir hata kodu ve alan bazlı doğrulama hatası (field error)**: Ön yüzün `switch`'e sokabileceği sabit bir kod (`STOK_YETERSIZ`) ve hangi girdi alanının reddedildiğini söyleyen liste (`{"alan": "email", "kod": "GECERSIZ_FORMAT"}`).
- **Geriye dönük uyum (backward compatibility) ve hoşgörülü okuyucu (tolerant reader)**: Eski istemcinin yeni yanıtla çalışmaya devam etmesi; istemcinin tanımadığı alanları görmezden gelip yalnız ihtiyacı olanı okuması ilkesi.
- **BFF (Backend For Frontend)**: Belirli bir istemci tipine (web/mobil) özel, arkadaki servislerden veri toplayıp o ekrana biçilmiş yanıt üreten ince katman.
- **Koşullu istek (conditional request), `ETag` / `If-None-Match`**: Yanıtın sürüm parmak izini istemciye verip, değişmediyse gövdesiz `304 Not Modified` dönerek trafiği kesme.
- **Canlı veri kanalları**: Yoklama (polling — istemci belli aralıkla sorar), **SSE** (server-sent events — sunucudan istemciye tek yönlü akış), **WebSocket** (çift yönlü kalıcı bağlantı).

### Nasıl çalışır?

Ön yüzün gördüğü ilk gerçek, **her isteğin bir bedeli olduğudur**. Bu bedel sadece senin sunucunda geçen süre değildir: bağlantı kurma, TLS el sıkışması, sıraya girme ve fiziksel mesafe. Ofis ağında 20 ms görünen bir tur, zayıf mobil bağlantıda 300 ms olabilir. Bu yüzden 20 kart için 20 ayrı detay isteği atan bir ekran, tek tek sorguların hepsi hızlı olsa bile yavaştır.

Bu tam olarak **istemci tarafı N+1**'dir ve JPA'daki N+1 probleminin ağ katmanındaki kardeşidir: orada 1+N SQL sorgusu vardı, burada 1+N HTTP turu var — aynı hastalık, başka katman. Çareler de benzer biçimde "toplu getir" ekseninde toplanır:

- **Listeye yeterli alanı gömmek**: Kartın çizilmesi için gereken alanları (ad, fiyat, kapak görseli, stok durumu) liste yanıtına koy. Ek istek gerekmez.
- **Alan seçimi (`?fields=ad,fiyat`)**: İstemci hangi alanları istediğini söyler; hem eksik hem aşırı veri çekmeyi aynı anda azaltır.
- **Toplu uç (`/urunler?id=1,2,3`)**: Ön yüz elindeki kimlikleri tek istekte sorar; N tur 1 tura iner.
- **BFF ile birleştirme**: Ekranın verisi birden çok servisten geliyorsa, birleştirmeyi tarayıcıya değil sunucu tarafına aldığın ince bir katmana yaptırırsın.

Karşı denge önemlidir: her şeyi listeye gömersen yanıt devleşir, kimsenin bakmadığı alanlar her satırda ağ üzerinden taşınır — bu **over-fetching**'tir. Az gömersen ön yüz ek istek atmak zorunda kalır — bu **under-fetching**'tir. Doğru nokta "kart hangi alanları çiziyorsa onlar listede, gerisi detay ucunda" çizgisidir.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İstemci tarafı N+1: yirmi ayrı detay isteği yerine tek zenginleştirilmiş liste yanıtı ya da BFF birleştirmesi">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İstemci tarafı N+1: 1+20 tur mu, 1 tur mu?</text>
  <text x="160" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B3261E" font-family="system-ui">Geveze (chatty) sözleşme</text>
  <rect x="20" y="52" width="280" height="168" rx="10" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.5"/>
  <rect x="38" y="68" width="86" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="81" y="88" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Tarayıcı</text>
  <text x="150" y="82" text-anchor="middle" font-size="15" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="176" y="68" width="106" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.2"/>
  <text x="229" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">GET /urunler</text>
  <text x="160" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">20 kayıt döner, ama kart çizilemez</text>
  <text x="150" y="142" text-anchor="middle" font-size="15" fill="#B3261E" font-family="system-ui">→</text>
  <rect x="176" y="128" width="106" height="26" rx="7" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.2"/>
  <text x="229" y="145" text-anchor="middle" font-size="9.5" fill="#B67A1A" font-family="system-ui">GET /urunler/1</text>
  <text x="150" y="168" text-anchor="middle" font-size="15" fill="#B3261E" font-family="system-ui">→</text>
  <rect x="176" y="154" width="106" height="26" rx="7" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.2"/>
  <text x="229" y="171" text-anchor="middle" font-size="9.5" fill="#B67A1A" font-family="system-ui">GET /urunler/2 …</text>
  <text x="160" y="200" text-anchor="middle" font-size="11" font-weight="700" fill="#B3261E" font-family="system-ui">Toplam 21 ağ turu ✗</text>
  <text x="480" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İri (chunky) sözleşme</text>
  <rect x="340" y="52" width="280" height="168" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="358" y="68" width="86" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="401" y="88" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Tarayıcı</text>
  <text x="470" y="82" text-anchor="middle" font-size="15" fill="#25804C" font-family="system-ui">→</text>
  <rect x="496" y="62" width="106" height="42" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.2"/>
  <text x="549" y="79" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">GET /urunler</text>
  <text x="549" y="94" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(ya da BFF ucu)</text>
  <text x="480" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yanıt kartın alanlarını içerir:</text>
  <text x="480" y="144" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ad, fiyat, görsel, stok</text>
  <text x="480" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">toplu uç: /urunler?id=1,2,3</text>
  <text x="480" y="200" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Toplam 1 ağ turu ✓</text>
</svg>
```

İkinci gerçek **sayfalamadır**. Klasik `?offset=40&limit=20` yaklaşımı "baştan 40 atla" der. Sorun şu: atlama sayısı, veri kümesinin o andaki sırasına bağlıdır. Kullanıcı ilk 20 kaydı görürken listenin başına yeni bir kayıt eklendiğini düşün. İkinci sayfayı istediğinde her şey bir sıra kaymıştır. Birinci sayfada gördüğü son kaydı **tekrar** görür. Silme olduysa tersi olur: bir kaydı hiç görmeden **atlar**. Sonsuz kaydırmada (infinite scroll) bu, kullanıcının gözüne "aynı ürünü iki kez gördüm" diye çarpar.

**İmleç (cursor/keyset) tabanlı sayfalama** bu sorunu köküne iner. Sayfa numarası yerine "son gördüğüm kayıt buydu" bilgisi taşınır (`?imlecSonrasi=<kayit-anahtari>&limit=20`). Sunucu da `WHERE anahtar > :imlec ORDER BY anahtar LIMIT 20` mantığıyla ilerler. Araya kayıt girmesi bu pencereyi kaydırmaz. Ayrıca büyük `offset` değerlerinde bir maliyet daha kalkar: veri tabanı, atladığı satırları yine de taramak zorundaydı. Bedeli şudur: rastgele sayfaya atlama ("9. sayfaya git") artık yoktur. Sonsuz kaydırma ile ise mükemmel uyuşur.

Toplam kayıt sayısı (`total`) vermek ayrı bir karardır: her sayfa isteğinde ikinci bir `COUNT` sorgusu demektir ve büyük tablolarda sayfanın kendisinden pahalıya gelebilir. Ön yüzün gerçekten sayfa numarası göstermesi gerekmiyorsa toplamı vermemek (ya da "yaklaşık" vermek, ya da yalnız "sonraki sayfa var mı" bilgisini vermek) meşru bir tercihtir. Bu bilgiyi nerede taşıyacağın da sözleşmenin parçasıdır: **zarf** içinde gövdede mi, yoksa `X-Total-Count` / `Link` **başlıklarında** mı? Başlığı seçersen bir tuzak vardır. Çapraz kaynaklı (cross-origin) bir çağrıda tarayıcı, güvenli listenin dışındaki yanıt başlıklarını ön yüz koduna **açmaz**. Bunun için sunucunun `Access-Control-Expose-Headers: X-Total-Count, Link` demesi gerekir. CORS konusunda anlatılan izin mekanizmasının burada somut bir sonucu vardır: "başlığı gönderdim ama ön yüz göremiyor" şikâyetinin sebebi neredeyse her zaman budur.

```svg
<svg viewBox="0 0 640 244" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Offset sayfalamada araya kayıt eklenince kayıt tekrarlanır, imleç tabanlı sayfalamada pencere kaymaz">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Araya yeni kayıt girerse: offset vs imleç</text>
  <text x="160" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B67A1A" font-family="system-ui">offset / limit</text>
  <rect x="20" y="52" width="280" height="176" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="160" y="72" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">1. sayfa: offset=0, limit=3</text>
  <rect x="44" y="82" width="52" height="24" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="70" y="98" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">K5</text>
  <rect x="102" y="82" width="52" height="24" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="128" y="98" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">K4</text>
  <rect x="160" y="82" width="52" height="24" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="186" y="98" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">K3</text>
  <text x="160" y="126" text-anchor="middle" font-size="10" fill="#B3261E" font-family="system-ui">araya K6 eklenir → hepsi bir kayar</text>
  <text x="160" y="148" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">2. sayfa: offset=3, limit=3</text>
  <rect x="44" y="158" width="52" height="24" rx="5" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.4"/>
  <text x="70" y="174" text-anchor="middle" font-size="9.5" fill="#B3261E" font-family="system-ui">K3</text>
  <rect x="102" y="158" width="52" height="24" rx="5" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.1"/>
  <text x="128" y="174" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">K2</text>
  <rect x="160" y="158" width="52" height="24" rx="5" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.1"/>
  <text x="186" y="174" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">K1</text>
  <text x="160" y="204" text-anchor="middle" font-size="11" font-weight="700" fill="#B3261E" font-family="system-ui">K3 İKİNCİ kez göründü ✗</text>
  <text x="160" y="220" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(silme olsaydı bir kayıt atlanırdı)</text>
  <text x="480" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">imleç (cursor / keyset)</text>
  <rect x="340" y="52" width="280" height="176" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="72" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">1. sayfa: limit=3</text>
  <rect x="364" y="82" width="52" height="24" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="390" y="98" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">K5</text>
  <rect x="422" y="82" width="52" height="24" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="448" y="98" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">K4</text>
  <rect x="480" y="82" width="52" height="24" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="506" y="98" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">K3</text>
  <text x="480" y="126" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">araya K6 eklenir → imleç hâlâ K3</text>
  <text x="480" y="148" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">2. sayfa: imlecSonrasi=K3</text>
  <rect x="364" y="158" width="52" height="24" rx="5" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.1"/>
  <text x="390" y="174" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">K2</text>
  <rect x="422" y="158" width="52" height="24" rx="5" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.1"/>
  <text x="448" y="174" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">K1</text>
  <text x="480" y="204" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Tekrar yok, atlama yok ✓</text>
  <text x="480" y="220" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(rastgele sayfaya atlama kaybolur)</text>
</svg>
```

Üçüncü gerçek **hata sözleşmesidir**. Ön yüzü en çok zehirleyen kötü desen buradadır: her yanıta `200 OK` verip gövdeye `{"hata": true, "mesaj": "..."}` koymak. Bunun bedeli ağırdır. Ön yüz artık "istek başarılı mı?" sorusunu HTTP'ye soramaz; **her** yanıtın gövdesini ayrıştırıp bir bayrağa bakmak zorundadır. HTTP'nin anlamına dayanan her şey ölür: `fetch`/`axios` gibi istemcilerin hata dalı, ara katmanların (vekil sunucu, ağ geçidi) hata sayaçları, otomatik yeniden deneme (retry) politikaları, izleme panolarındaki hata oranı — hepsi "her şey yolunda" görür. Üstelik gerçek bir altyapı hatası olduğunda gövde beklenen biçimde gelmez ve ön yüz ayrıştırma hatasıyla çöker.

Doğrusu iki parçalıdır: **doğru durum kodu** + **standart hata gövdesi**. `ProblemDetail` (RFC 9457) bu gövdenin standardıdır: `type` (hata türünü tanımlayan URI), `title` (kısa insan başlığı), `status` (durum kodu), `detail` (bu olaya özgü açıklama), `instance` (olayın adresi). Standart alanların yanına iki şeyi eklemek ön yüzün hayatını değiştirir:

- **Makinece okunabilir hata kodu**: `"kod": "STOK_YETERSIZ"` gibi sabit bir dize. Ön yüz insan metnine göre `if` yazmak zorunda kalmaz; metin değişince kodu bozulmaz ve mesajı kendi dilinde kendisi üretir.
- **Alan bazlı doğrulama hataları**: `"alanHatalari": [{"alan": "email", "kod": "GECERSIZ_FORMAT"}]`. Ön yüz mesajı hangi input kutusunun altına yazacağını böyle bilir; aksi halde tüm doğrulama uyarıları formun tepesinde tek bir kırmızı kutuya yığılır.

Durum kodu sınıfının ön yüzde **davranışsal** bir karşılığı vardır. **4xx** "istemci düzeltebilir" demektir: formu işaretle, kullanıcıya ne yapması gerektiğini söyle, aynı isteği tekrarlama. Karşılıkları şöyledir: `400` doğrulama, `401` oturum yenile ya da giriş ekranı, `403` yetkisiz uyarısı, `404` bulunamadı ekranı, `409` çakışma ya da yeniden yükle, `422` iş kuralı reddi. **5xx** ise "senin hatan değil, tekrar denenebilir" demektir. Burada doğru davranış, aralıkları uzayan yeniden deneme (backoff) ve "bir sorun oluştu, tekrar deneyin" mesajıdır. Bu ayrım kaybolursa ön yüz her hataya aynı genel mesajı basar. (Hatanın sunucu tarafında nasıl yakalanıp `ProblemDetail`'e çevrildiği — `@ExceptionHandler` / `@ControllerAdvice` — ayrı bir konudur; burada bizi ilgilendiren **tüketicideki sonucudur**.)

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Her hataya 200 dönmek ile doğru durum kodu ve ProblemDetail dönmenin ön yüzdeki karşılaştırması">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hata sözleşmesi: ön yüz ne yazmak zorunda kalır?</text>
  <text x="160" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B3261E" font-family="system-ui">Anti-desen: her yanıt 200</text>
  <rect x="20" y="52" width="280" height="182" rx="10" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.5"/>
  <rect x="40" y="68" width="240" height="42" rx="7" fill="#FFFFFF" stroke="#B3261E" stroke-width="1.2"/>
  <text x="160" y="85" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">HTTP 200 OK</text>
  <text x="160" y="101" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">{ "hata": true, "mesaj": "Stok yok" }</text>
  <text x="160" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Ön yüz HER yanıtı ayrıştırır</text>
  <text x="160" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ve bayrağa bakar</text>
  <text x="160" y="172" text-anchor="middle" font-size="10" fill="#B3261E" font-family="system-ui">hata dalı hiç çalışmaz</text>
  <text x="160" y="190" text-anchor="middle" font-size="10" fill="#B3261E" font-family="system-ui">panolarda hata oranı: %0</text>
  <text x="160" y="212" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">metne göre if yazılır ✗</text>
  <text x="480" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Doğrusu: kod + ProblemDetail</text>
  <rect x="340" y="52" width="280" height="182" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="360" y="68" width="240" height="58" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="85" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">HTTP 409 Conflict</text>
  <text x="480" y="100" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">type / title / status / detail</text>
  <text x="480" y="116" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kod: STOK_YETERSIZ + alanHatalari</text>
  <text x="480" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">4xx → kullanıcıya düzelt de</text>
  <text x="480" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">5xx → geri çekilip tekrar dene</text>
  <text x="480" y="186" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">mesaj doğru input altına yazılır</text>
  <text x="480" y="212" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">koda göre if yazılır ✓</text>
</svg>
```

Dördüncü gerçek **alan adlandırma ve tiplerdir**. Küçük görünür, en çok zaman yakan yer burasıdır:

- **Tarih/saat**: Her zaman ISO-8601 ve UTC (`2026-07-29T14:05:00Z`). Kullanıcının saat dilimini ve biçimini (dün / 2 saat önce / 29 Tem 2026) ön yüz zaten biliyor ve uygulayabiliyor. Sunucunun yerel saatinde biçimlenmiş bir metin gönderirsen ön yüz onu geri ayrıştırmak zorunda kalır.
- **Para**: Kayan noktalı sayı (`double`) **kullanma** — `0.1 + 0.2` klasiği JavaScript'te de aynen yaşanır. En küçük birimde tamsayı (kuruş: `129990`) ya da ondalık koruyan bir dize (`"1299.90"`) ver, para birimini ayrı alanda (`"TRY"`) belirt.
- **`null` ile boş dizi farkı**: `null` "bilgi yok/uygulanmaz", `[]` "var ama boş" demektir. Koleksiyonlarda `null` göndermek ön yüzde döngüden önce fazladan bir kontrol zorunluluğu doğurur; tutarlı ol.
- **Sabit değer kümeleri (enum)**: Durumu `"Kargoya Verildi"` gibi serbest metinle değil, `"KARGOYA_VERILDI"` gibi sabit kodla ver. Kullanıcıya gösterilecek metni arka ucun (backend) üretmesi **yerelleştirmeyi (i18n) kilitler**. Ön yüz ikinci bir dil eklemek istediğinde senin dağıtımını beklemek zorunda kalır. Kod sabit kalsın, metin ön yüzde üretilsin.

Beşinci gerçek **sözleşme kararlılığıdır**. Ön yüzün sürümü senin dağıtımından bağımsız yayınlanır; özellikle mobilde mağazadaki eski sürüm haftalarca, bazen aylarca canlıdır. Kural nettir: **alan eklemek** geriye dönük uyumludur (tanımayan istemci görmezden gelir), **alan silmek veya yeniden adlandırmak kırıcıdır**. Aynı şekilde bir alanın tipini değiştirmek (sayıdan metne), bir enum'a istemcinin bilmediği yeni bir değer eklemek (istemci `default` dalını yazmadıysa) ve zorunlu bir istek parametresi eklemek de kırıcıdır. Güvenli yol: yeni alanı ekle, eskisini bir süre **birlikte** döndür, istemciler geçtikten sonra kaldır. İstemci tarafındaki karşılığı **hoşgörülü okuyucu** ilkesidir: tanımadığın alanı görmezden gel, yalnız ihtiyacın olanı oku — böylece sunucunun eklediği alanlar istemciyi kırmaz.

Altıncı gerçek **uzun süren iş ve canlı veridir**. Bir işlem saniyeler sürecekse istemciyi bekletme. Bunun yerine `202 Accepted` ve bir durum ucu (`/isler/{id}`) verirsin; ön yüz o ucu **yoklar** (polling) ve ilerleme gösterir. Sunucudan istemciye tek yönlü sürekli akış gerekiyorsa **SSE** yeter ve basittir; canlı sayaç, bildirim, üretilen metnin akması gibi durumlar böyledir. Sohbet, işbirlikli düzenleme, oyun gibi **çift yönlü** ihtiyaçlarda ise **WebSocket** gerekir. WebSocket'in ölçekleme bedeli ayrıdır: her bağlantı sunucuda durum tutar.

Dosya yüklemede iki yol vardır. Birincisi, çoklu parça (multipart) ile dosyayı kendi sunucundan geçirmek; basittir ama sunucunu ve bant genişliğini meşgul eder. İkincisi, **ön imzalı URL** (presigned URL) verip tarayıcının doğrudan nesne deposuna yüklemesini sağlamak; ölçeklenir, sunucun yalnız izin üretir.

Yedinci ve son gerçek **önbellek başlıklarıdır**. Ön yüzün ağ trafiğini azaltmanın en ucuz yolu, yanıta bir sürüm parmak izi (`ETag`) koymaktır. İstemci sonraki isteğinde `If-None-Match` ile o parmak izini geri gönderir. Veri değişmediyse gövdesiz **`304 Not Modified`** dönersin; ağdan hiçbir gövde geçmez. Değişme sıklığını biliyorsan `Cache-Control: max-age=...` ile tarayıcının bir süre hiç istek atmamasını bile sağlayabilirsin. Bu, ön yüzde tek satır kod yazılmadan kazanılan bir hızdır ve tamamen senin kararındır.

**BFF ne zaman?** Tek bir ekran tipi ve tek istemci varsa gerek yoktur; sözleşmeyi düzgün kurmak yeter. BFF, birden çok ekran ve birden çok istemci tipi olduğunda anlamlı hâle gelir. Çünkü web geniş veri ister, mobil ise dar ve az veri ister; ikisi aynı uçtan farklı biçimde veri bekler. Çözüm şudur: her istemci tipine kendi ince katmanını verirsin. O katman arkadaki servislerden veriyi toplar ve ekrana biçilmiş yanıt üretir. Böylece çekirdek servisler "her müşteriye yaranan" şişkin uçlara dönüşmez. (Bunun API ağ geçidiyle ilişkisi ve genel mimarisi ayrı bir konudur.)

### Mülakatta ne sorulur?

- **"Ön yüz ekibi liste ekranı yavaş diyor, ne yaparsın?"** → Önce **kaç istek** attıklarına bakarım. 1 liste + N detay isteği varsa bu **istemci tarafı N+1**'dir; sunucudaki JPA N+1'in ağ katmanındaki hâli. Çare: kartın çizimi için gereken alanları liste yanıtına gömmek, gerekirse alan seçimi (`?fields=`) ve toplu uç (`/urunler?id=1,2,3`) sunmak; birden çok servis birleştiriliyorsa BFF. Denge: aşırı gömme over-fetching, az gömme under-fetching. Yanlış cevap kalıbı: "ön yüz paralel istek atsın" — paralellik tur **sayısını** azaltmaz, yalnız gizler ve mobilde bağlantı sınırına takılır.
- **"Offset yerine imleç tabanlı sayfalama ne zaman?"** → Veri sık değişiyorsa ve sonsuz kaydırma varsa. Offset'te araya kayıt girince kayıtlar **tekrarlanır ya da atlanır**; imleçte pencere "son görülen kayıttan sonrası" olduğu için kaymaz. Ek fayda: derin sayfalarda `OFFSET`'in taradığı satır maliyeti kalkar. Bedeli: rastgele sayfaya atlama yoktur. Toplam sayı (`total`) her istekte ikinci bir `COUNT` demektir — gerekmiyorsa vermem, "sonraki sayfa var mı" bilgisi çoğu zaman yeter.
- **"Her hataya 200 + `{"hata": true}` dönen bir API gördün, itirazın ne?"** → Ön yüz **her** yanıtın gövdesini ayrıştırıp bayrağa bakmak zorunda kalır. Dahası, HTTP anlamına dayanan her şey körleşir: istemci hata dalları, ara katman sayaçları, yeniden deneme politikaları ve izleme panoları hatayı hiç görmez. Doğrusu **doğru durum kodu + `ProblemDetail` (RFC 9457)** gövdesidir. Üstüne **makinece okunabilir hata kodu** ve **alan bazlı doğrulama hataları** eklenir. Ön yüzün mesajı doğru giriş kutusunun altına yazabilmesi buna bağlıdır.
- **"4xx ile 5xx ayrımının ön yüzdeki karşılığı nedir?"** → **4xx**: istemci düzeltebilir → formu işaretle, kullanıcıya söyle, aynı isteği tekrarlama (`401` oturum yenile, `403` yetki uyarısı, `409` çakışma/yeniden yükle). **5xx**: geçici olabilir → geri çekilmeli yeniden deneme (backoff) ve jenerik "tekrar deneyin" mesajı. Bu ayrım yoksa ön yüz her şeye aynı mesajı basar.
- **"Tarihi ve parayı JSON'da nasıl verirsin?"** → Tarihi **ISO-8601 + UTC**; saat dilimi ve görüntü biçimi ön yüzün işi. Parayı **kuruş cinsinden tamsayı** ya da ondalık koruyan **dize**, para birimi ayrı alanda; kayan nokta (`double`) asla. `null` ile `[]` farkına tutarlı davranırım. Enum'ları sabit kodla veririm — kullanıcıya gösterilecek metni backend üretirse **yerelleştirme kilitlenir**, ön yüz yeni dil için senin dağıtımını bekler.
- **"Yanıttan bir alanı kaldırmak/yeniden adlandırmak güvenli midir?"** → Hayır, **kırıcıdır**. Alan **eklemek** uyumludur; silmek, yeniden adlandırmak, tip değiştirmek ve zorunlu parametre eklemek değildir. Mobil istemcinin eski sürümü mağazada bir süre daha yaşar; bu yüzden yeni alanı ekleyip eskisini bir süre birlikte döndürür, geçiş bitince kaldırırım. İstemci tarafında **hoşgörülü okuyucu** ilkesi beklerim: tanımadığın alanı görmezden gel.
- **"Sayfalama bilgisini başlıkta verdim ama ön yüz göremiyor, neden?"** → Çapraz kaynak isteklerde tarayıcı, güvenli listenin dışındaki yanıt başlıklarını istemci koduna **açmaz**; `Access-Control-Expose-Headers: X-Total-Count, Link` demek gerekir. Bu, "sunucu gönderdi ama JavaScript okuyamıyor" şikâyetinin standart sebebidir. Bunu unutmak istemiyorsan bilgiyi gövdedeki zarfta taşımak da meşru bir tercihtir.
- **"Uzun süren işi ve canlı veriyi nasıl sunarsın?"** → Uzun iş için **`202 Accepted` + durum ucu** ve yoklama (polling). Tek yönlü akış için **SSE** (basit, HTTP üzerinde çalışır), çift yönlü etkileşim için **WebSocket** (ölçekleme bedeli var). Büyük dosyada **ön imzalı URL** ile doğrudan yükleme, sunucumu bant genişliği taşıyıcısı olmaktan kurtarır.
- **"Ön yüzün ağ trafiğini backend olarak nasıl azaltırsın?"** → `ETag` + `If-None-Match` ile koşullu istek: değişmemişse gövdesiz `304` dönerim. Uygun yerlerde `Cache-Control` ile isteğin hiç atılmamasını sağlarım. Bir de tabii sözleşmeyi iri (chunky) kurarak tur sayısını düşürürüm.

### Tek cümlede

Ön yüzü hızlı, hatasız ve bağımsız yapan şey senin sözleşmendir: **az turda yeterli veri**, **kaymayan sayfalama**, **doğru durum kodu + `ProblemDetail`**, **makine okunur tipler** ve **kırmayan değişiklikler**.

# sozl-1 | Temel
## Soru
Ön yüz ekibi ürün listesi ekranından şikâyetçi: `GET /urunler` 20 kayıt dönüyor, ardından her kart için ayrıca `GET /urunler/{id}` çağrılıyor ve ekran özellikle mobilde çok geç açılıyor. Bu duruma ne denir ve backend tarafındaki ilk çare nedir?
## A
Bu tamamen ön yüzün sorunudur; backend'in yapabileceği bir şey yoktur, ön yüz 20 detay isteğini paralel atarak sorunu kendi tarafında çözmelidir.
🤖 Paralellik tur sayısını azaltmaz, yalnız gizler; asıl çare sözleşmeyi düzeltmektir.
## B
Buna over-fetching denir; çare liste ucunun döndürdüğü alan sayısını azaltıp her yanıtı olabildiğince küçültmek ve gerisini detay ucuna bırakmaktır.
🤖 Sorun fazla veri değil fazla İSTEK; alanları azaltmak ek istekleri artırır.
## C
Buna istemci tarafı N+1 denir; kartın çizimi için gereken alanları liste yanıtına gömmek ya da toplu uç sunmak tur sayısını düşürür.
🤖 Doğru: 1+20 ağ turu, sözleşme düzeltilerek tek tura iner.
## D
Buna cache stampede denir; liste ucuna `ETag` eklemek eşzamanlı gelen detay isteklerini tekilleştirdiği için gecikme ortadan kalkar.
🤖 Stampede önbellek tazelenmesi sorunudur; buradaki dert istek sayısıdır.
## Doğru
C
## Açıklama
1 liste + N detay isteği deseni **istemci tarafı N+1**'dir: sunucudaki JPA N+1 probleminin ağ katmanındaki hâli. Her HTTP turunun sabit bir gecikme bedeli vardır (bağlantı, TLS, mesafe) ve bu bedel zayıf mobil bağlantıda katlanır; tek tek sorgular hızlı olsa bile 21 tur yavaştır. Backend'in çaresi sözleşmeyi **iri (chunky)** kurmaktır: kartın çizimi için gereken alanları (ad, fiyat, görsel, stok) liste yanıtına gömmek, gerekirse alan seçimi (`?fields=`) ve toplu uç (`/urunler?id=1,2,3`) sunmak, birden çok servis birleşiyorsa BFF kullanmak. Paralel istek atmak (A) tur sayısını azaltmaz, yalnız gizler ve tarayıcının eşzamanlı bağlantı sınırına takılır. Alanları azaltmak (B) tam ters yönde çalışır ve under-fetching'i büyütür; sorun cache stampede (D) değildir. Karşı denge unutulmamalı: her şeyi gömmek de over-fetching demektir.

# sozl-2 | Temel
## Soru
İncelediğin bir API, iş kuralı ihlâli dâhil her durumda `200 OK` döndürüp gövdeye `{"hata": true, "mesaj": "Stok yok"}` koyuyor. Ön yüz açısından bu neden bir anti-desendir?
## A
Anti-desen değildir; HTTP durum kodları yalnız ağ/taşıma katmanını ilgilendirir, uygulama seviyesindeki hatalar her zaman gövdede taşınmalıdır.
🤖 Durum kodu taşıma değil uygulama seviyesinin sözleşmesidir; hata sınıfını o taşır.
## B
Ön yüz her yanıtın gövdesini ayrıştırıp bayrağa bakmak zorunda kalır; HTTP semantiğine dayanan istemci ve ara katman davranışları devre dışı kalır.
🤖 Doğru: başarı/hata ayrımı HTTP'den koparılınca her katman körleşir.
## C
Tarayıcılar 200 dışındaki yanıtları CORS politikası gereği ön yüz koduna hiç iletmediğinden bu yaklaşım pratikte zorunlu bir çözümdür.
🤖 CORS durum koduna göre engelleme yapmaz; hata yanıtları da ön yüze ulaşır.
## D
Tek sorun performanstır: `200` yanıtları ara önbelleklerde saklandığından hata mesajı bir süre daha kullanıcıya dönmeye devam eder.
🤖 Asıl sorun anlamsaldır; önbellek etkisi tali bir yan etkidir.
## Doğru
B
## Açıklama
Her yanıta `200` vermek, "istek başarılı mı?" sorusunun cevabını HTTP'den koparıp gövdenin içine gömer. Sonuç: ön yüz **her** yanıtı ayrıştırıp bir bayrağa bakmak zorunda kalır; `fetch`/`axios` gibi istemcilerin hata dalları, ara katmanların (proxy, gateway) hata sayaçları, otomatik yeniden deneme politikaları ve izleme panolarındaki hata oranı "her şey yolunda" görür. Gerçek bir altyapı hatasında gövde beklenen biçimde gelmez ve ön yüz ayrıştırma sırasında çöker. Doğrusu **doğru durum kodu + `ProblemDetail` (RFC 9457)** gövdesidir; üstüne makinece okunabilir hata kodu ve alan bazlı doğrulama hataları eklenir. Durum kodu taşıma katmanına ait değildir (A), CORS 200 dışı yanıtları engellemez (C) ve sorun önbellekten ibaret bir performans meselesi değildir (D).

# sozl-3 | Orta
## Soru
Sonsuz kaydırmalı (infinite scroll) bir akış `?offset=20&limit=20` ile sayfalanıyor. Kullanıcı kaydırırken listenin başına sürekli yeni kayıt ekleniyor. Ön yüzde ne görülür ve doğru çözüm nedir?
## A
Hiçbir sorun oluşmaz; offset tabanlı sayfalama araya kayıt eklenmesine karşı dayanıklıdır, yalnızca toplam kayıt sayısı alanı güncellenir.
🤖 Offset "baştan N atla" der; araya kayıt girince pencere kayar.
## B
Kayıtlar rastgele sırayla gelir; çözüm `limit` değerini büyütüp toplam sayfa sayısını azaltmak ve sıralamayı istemci tarafında yapmaktır.
🤖 Sıra bozulmaz, pencere kayar; limit büyütmek sorunu ertelemekten öteye gitmez.
## C
Sunucu offset üst sınırı aşıldığı için hata döndürmeye başlar; çözüm offset değerini istemci yerine sunucudaki oturumda saklamaktır.
🤖 Hata dönmez, sessizce yanlış veri döner; durumu sunucuda tutmak da ölçeklenmez.
## D
Aynı kayıt ikinci kez görünür ya da bir kayıt hiç görünmez; çözüm imleç (cursor/keyset) tabanlı sayfalamaya geçmektir.
🤖 Doğru: imleç "son görülen kayıttan sonrası" dediği için pencere kaymaz.
## Doğru
D
## Açıklama
`offset` "baştan şu kadarını atla" demektir ve atlama sayısı veri kümesinin **o andaki** sırasına bağlıdır. Kullanıcı ilk sayfayı görürken listenin başına kayıt eklenirse her şey bir sıra kayar; ikinci sayfada birinci sayfanın son kayıtları **tekrar** görünür. Silme olsaydı tersi olur ve bir kayıt hiç görünmeden **atlanırdı**. **İmleç (cursor/keyset) tabanlı sayfalama** bunu köküne iner: sayfa numarası yerine "son gördüğüm kayıt buydu" bilgisi taşınır ve sunucu `WHERE anahtar > :imlec ORDER BY anahtar LIMIT n` ile ilerler; araya kayıt girmesi pencereyi kaydırmaz. Ek fayda, derin sayfalarda veritabanının atlanan satırları yine de taraması maliyetinin kalkmasıdır; bedeli ise rastgele sayfaya atlamanın kaybolmasıdır — sonsuz kaydırmada bu bir kayıp değildir. Offset dayanıklı değildir (A), sıra rastgeleleşmez (B) ve sunucu hata döndürmez, sessizce yanlış veri döner (C).

# sozl-4 | Orta
## Soru
Yeni bir uçta tarih, tutar ve sipariş durumu alanlarını nasıl döndüreceğine karar veriyorsun. Ön yüzü en az zorlayan ve yerelleştirmeyi (i18n) kilitlemeyen seçenek hangisidir?
## A
Tarih ISO-8601 ve UTC, tutar kuruş cinsinden tamsayı (para birimi ayrı alanda), durum sabit kod; gösterim metnini ön yüz üretir.
🤖 Doğru: makine okunur tipler + gösterimin ön yüzde kalması.
## B
Tarih sunucunun yerel saatinde `29.07.2026 17:05`, tutar `1.299,90 TL` biçimli metin, durum `Kargoya Verildi` etiketi olarak döner.
🤖 Biçimlenmiş metin ön yüzü geri ayrıştırmaya zorlar ve dili sunucuya kilitler.
## C
Tarih Unix zaman damgası, tutar kayan noktalı sayı (`double`), durum hem kod hem hazır Türkçe metin olarak birlikte döner.
🤖 Parada `double` yuvarlama hatası doğurur; hazır metin yine dili sunucuya bağlar.
## D
Backend her alanın gösterime hazır hâlini de ekler (`tarihMetni`, `fiyatMetni`, `durumMetni`); ön yüz hiçbir biçimlendirme yapmaz.
🤖 Bu, ikinci dil eklemeyi backend dağıtımına bağımlı kılar; i18n kilitlenir.
## Doğru
A
## Açıklama
Tarihi **ISO-8601 + UTC** vermek doğru tercihtir: saat dilimi dönüşümünü ve "dün / 2 saat önce / 29 Tem 2026" gibi biçimlendirmeyi ön yüz zaten yapabilir; sunucunun yerel saatinde biçimlenmiş metin ise ön yüzü geri ayrıştırmaya zorlar (B). Parada kayan nokta (`double`) kullanılmaz — yuvarlama hataları JavaScript'te de aynen yaşanır; en küçük birimde tamsayı (kuruş) veya ondalık koruyan dize, para birimi ayrı alanda verilir (C). Durum alanı `KARGOYA_VERILDI` gibi **sabit bir kodla** dönmelidir: ön yüz koda göre dallanır, metni kendi sözlüğünden üretir. Kullanıcıya gösterilecek metni backend'in üretmesi (B, D) **yerelleştirmeyi kilitler**: ön yüz ikinci bir dil eklemek istediğinde senin dağıtımını beklemek zorunda kalır. Aynı disiplin `null` ile boş dizi ayrımında da geçerlidir: `null` "bilgi yok", `[]` "var ama boş" demektir.

# sozl-5 | İleri
## Soru
Mobil uygulamanın mağazadaki eski sürümü haftalarca canlı kalacak. Yanıt gövdesindeki `musteriAdi` alanını `musteriTamAdi` olarak yeniden adlandırmak istiyorsun. Doğru değerlendirme hangisidir?
## A
Alan adı değişikliği geriye dönük uyumludur; JSON şemasız olduğundan istemci kütüphaneleri benzer adları otomatik eşleyerek sorunu kapatır.
🤖 Otomatik ad eşleme diye bir garanti yoktur; alan adı sözleşmenin parçasıdır.
## B
Sorun yoktur; uç sürümünü artırdığında (`/v1` → `/v2`) eski istemciler de yeni alan adını otomatik olarak almaya başlar.
🤖 Sürümleme eskiyi korumak içindir; eski istemci yeni alanı almaz, alamamalıdır.
## C
Bu kırıcı bir değişikliktir; eski alanı bir süre yenisiyle birlikte döndürüp istemciler geçtikten sonra kaldırmak gerekir.
🤖 Doğru: ekle-birlikte döndür-sonra kaldır, güvenli geçiş yoludur.
## D
Yeni alan eklemek de silmek de eşit ölçüde kırıcıdır; her iki durumda da tüm istemcilerin sunucuyla aynı anda güncellenmesi şarttır.
🤖 Alan eklemek uyumludur; hoşgörülü okuyucu tanımadığı alanı görmezden gelir.
## Doğru
C
## Açıklama
Alan adı sözleşmenin parçasıdır: `musteriAdi` yerine `musteriTamAdi` dönmeye başladığın anda eski istemci o alanı **bulamaz** ve ekranda boş/`undefined` görünür ya da çöker. Ön yüzün sürümü senin dağıtımından bağımsız yayınlanır; özellikle mobilde mağazadaki eski sürüm haftalarca canlı kalır, dolayısıyla "herkes aynı anda güncellenir" varsayımı geçersizdir. Kural: **alan eklemek** geriye dönük uyumludur (tanımayan istemci görmezden gelir — hoşgörülü okuyucu ilkesi), **silmek/yeniden adlandırmak/tip değiştirmek/zorunlu parametre eklemek** kırıcıdır. Güvenli yol yeni alanı eklemek, eskisini bir süre **birlikte** döndürmek ve istemciler geçtikten sonra kaldırmaktır. Otomatik ad eşleme diye bir garanti yoktur (A); sürümleme eski istemciyi **korumak** içindir, onu yeni biçime taşımaz (B); alan eklemek ile silmek eşdeğer değildir (D).

# sozl-6 | İleri
## Soru
Sayfalama bilgisini gövdedeki zarf yerine `X-Total-Count` ve `Link` yanıt başlıklarıyla vermeye karar verdin. Ön yüz farklı bir kaynaktan (origin) çağırıyor ve JavaScript tarafında bu başlıkları hiç göremiyor. Sebep ve çözüm nedir?
## A
Tarayıcılar standart dışı yanıt başlıklarını tümüyle yasaklar; sayfalama bilgisi her koşulda gövdede bir zarf içinde taşınmak zorundadır.
🤖 Yasak değil, varsayılan olarak gizlidir; sunucu açıkça izin verirse okunur.
## B
Ön uçuş (preflight) yanıtı eksiktir; okunacak yanıt başlıklarının adları `Access-Control-Allow-Methods` listesine eklenmelidir.
🤖 O başlık HTTP metotları içindir; yanıt başlığı açmakla ilgisi yoktur.
## C
Başlık adı `X-` önekiyle başladığı için modern tarayıcılar onu sessizce düşürür; adı `Total-Count` yapmak sorunu çözer.
🤖 `X-` öneki artık önerilmez ama tarayıcı bu yüzden başlığı düşürmez.
## D
Tarayıcı çapraz kaynak yanıtta yalnız güvenli listedeki başlıkları açar; `Access-Control-Expose-Headers` ile izin verilmelidir.
🤖 Doğru: sunucu hangi başlıkların okunabileceğini açıkça bildirmelidir.
## Doğru
D
## Açıklama
Çapraz kaynak (cross-origin) bir yanıtta tarayıcı, istemci koduna varsayılan olarak yalnızca küçük bir **güvenli liste** başlığı açar (`Content-Type`, `Cache-Control` gibi). Sunucu başlığı gerçekten göndermiş olsa da, JavaScript onu `null` görür; çözüm sunucunun `Access-Control-Expose-Headers: X-Total-Count, Link` diyerek hangi başlıkların okunabileceğini açıkça bildirmesidir. Bu, CORS'un izin mekanizmasının somut bir sonucudur ve "sunucu gönderdi ama ön yüz okuyamıyor" şikâyetinin standart sebebidir. Başlıklar yasak değildir (A), `Access-Control-Allow-Methods` HTTP **metotlarını** ilgilendirir ve yanıt başlığı açmaz (B), `X-` öneki artık önerilmese de tarayıcının başlığı düşürmesine yol açmaz (C). Bu ayrıntıyı unutmamak için sayfalama bilgisini gövdedeki zarfta taşımak da meşru bir tercihtir; ikisi arasındaki seçim sözleşme kararıdır.
