---
slug: page-object-pattern
baslik: "Page Object Pattern: Selector Merkezileştirme ve Davranış Kapsülleme"
kademe: Orta
sira: 198
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

**Sayfa nesnesi deseni (Page Object Pattern, POM)**, her sayfayı ya da sayfa parçasını bir sınıf olarak modellemektir. "Ürün sayfası" diye bir sınıf varsa, o sayfada yapılabilecek her şey o sınıfın içinde durur: başlığı okumak, sepete eklemek gibi. Özellik (feature) dosyaları bu sınıfları çağırır ama nasıl çalıştıklarını bilmez.

Amaç iki kelimeyle özetlenir: **tekrarı** ve **kırılganlığı** azaltmak. Bir düğmenin CSS seçicisi değişirse tek bir dosya güncellenir; bir işlemin (mesela "sepete ekle") arka planda ne kadar karmaşık olduğu tek bir yerde yazılıdır ve onu kullanan her senaryo bu detaydan habersiz kalır.

### Temel kavramlar

- **Seçicileri tek yerde toplamak.** `pages/locators.js` dosyasında tüm CSS seçiciler bir arada durur; örneğin `product.addToCartButton: 'button.btn-add'`. Site değişip bir seçici kırılırsa güncellenecek dosya bellidir. Sayfa nesnelerinin ya da özellik dosyalarının içine dağılmış seçicileri tek tek aramak gerekmez.
- **Davranışı kapsülleme (encapsulation).** "Sepete ekle" işlemi göründüğünden karmaşıktır. Sitenin onay penceresinin (modal) açılmasını beklemek gerekir; açılmazsa tıklamayı tekrarlamak gerekir. Bu mantık `ProductPage.addToCart()` içinde bir kez yazılır. Onu çağıran senaryolar bu ayrıntıyı hiç bilmez.
- **Kalıtımla paylaşılan davranış.** `BasePage` sınıfı ortak işleri taşır: sayfaya gitme (`goto`), seçici çözümleme ve sayfanın üstünü örten pencereleri kapatma. Bu örten pencereler çerez şeridi (banner) ve sepet onay penceresi olabilir. `ProductPage` ve `CartPage` bu sınıftan türer ve yalnız kendi sayfasına özgü davranışı ekler.
- **Doğrulama sayfa nesnesinde OLMAZ.** Sayfa nesnesi iki soruyu cevaplar: "ne yapılır" (eylem) ve "ne okunur" (veri). "İş kuralı doğru mu?" sorusunu cevaplamaz; doğrulamalar adım tanımlarında yaşar. Tek görünür istisna `expect.poll` kullanımıdır. `expect.poll` teknik olarak bir doğrulamadır; koşul zaman aşımına kadar sağlanmazsa hata fırlatır ve adımı düşürür. Ama buradaki **rolü** doğrulama değil, eşzamanlamadır. Bir iş beklentisini ("ara toplam şu olmalı") sınamaz; sayfanın kararlı hâle gelmesini yoklar. İş beklentisini yargılayan doğrulamalar yine adımlarda kalır.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Page object katmanları: BasePage, ProductPage, CartPage ve locators.js ilişkisi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Page object katmanları</text>
  <rect x="230" y="42" width="180" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">BasePage</text>
  <line x1="320" y1="88" x2="150" y2="122" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="320" y1="88" x2="490" y2="122" stroke="#5D7789" stroke-width="1.4"/>
  <rect x="60" y="122" width="180" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="150" y="150" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">ProductPage</text>
  <rect x="400" y="122" width="180" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="150" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">CartPage</text>
  <line x1="150" y1="168" x2="150" y2="196" stroke="#5D7789" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="490" y1="168" x2="490" y2="196" stroke="#5D7789" stroke-width="1.2" stroke-dasharray="3,3"/>
  <rect x="140" y="196" width="360" height="26" rx="7" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.3"/>
  <text x="320" y="213" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">locators.js — tüm CSS seçiciler tek dosyada</text>
</svg>
```

### Nasıl çalışır?

`ProductPage.addToCart()` örneği, kapsüllemenin neden sadece "kod düzenleme" olmadığını gösterir. Bu, **kırılganlığa karşı bir savunmadır**.

Buton, Angular kendi tıklama işleyicisini bağlamadan çok önce görünür hâle gelir. Bu süre boyunca üzerindeki `disable` CSS sınıfı hiç kalkmaz. Playwright'ın "etkin mi" kontrolü bu sınıfı görmez; o kontrol `disabled` özniteliğine ve `aria-disabled` değerine bakar.

Yani ne görünürlük kontrolü ne de "etkin mi" kontrolü, butonun gerçekten tıklamaya hazır olduğunu söyler. Erken yapılan tıklama hiçbir şey yapmaz ve sepet sessizce boş kalır.

Bu yüzden `addToCart()` kanıt olarak sitenin **kendi onay penceresini** bekler. Pencere açılmazsa tıklamayı üç kez dener. Her denemede onay penceresine tanınan süre 8 saniyedir ve bu bilinçli olarak geniş tutulmuştur. Aksi hâlde işe yaramış bir tıklamadan erken vazgeçilir, tıklama tekrarlanır ve ürün sepete iki kez eklenirdi.

```js
async addToCart() {
  const modal = this.locator(locators.common.addToCartModal);
  const button = this.addToCartButton.first();
  await button.waitFor({ state: 'visible' });

  for (let attempt = 1; attempt <= 3; attempt += 1) {
    if (await modal.isVisible()) {
      await this.closeAddToCartModal();       // önceki üründen kalma modal olmasın
    }
    await button.click();
    const confirmed = await modal
      .waitFor({ state: 'visible', timeout: 8000 })
      .then(() => true)
      .catch(() => false);
    if (confirmed) {
      await this.closeAddToCartModal();
      return;
    }
  }
  throw new Error(`Ürün sepete eklenemedi (onay penceresi açılmadı): ${this.page.url()}`);
}
```

Bütün sepet senaryoları bu mantığı tek bir alana özel adım üzerinden kullanır: `Diyelim ki sepete {int} farklı ürün eklenir`. Bu senaryolar `cart.feature` (adet değiştirme, silme ve ara toplam) ve `cart_persistence.feature` (misafir sepetinin giriş sonrası korunması) dosyalarındadır. Hiçbiri "onay penceresi ne zaman açılır, kaç kez denenir" sorusuyla uğraşmaz; hepsi `ProductPage` sınıfının arkasında saklıdır.

Peki neden bu desen seçildi, alternatifi yok muydu? Vaka çalışması zaten bu deseni istiyordu. Ama katkısı da gerçektir: seçiciler `locators.js` içinde tek yerde, sayfa davranışı ise sayfa nesnesi içinde tek yerde toplanır.

Alternatif olarak **screenplay deseni** düşünülebilirdi; bu desen aktör, görev ve soru soyutlamaları kullanır. Ama bu ölçekte — birkaç sayfa ve sınırlı sayıda senaryo — fazla soyutlama olurdu. Sayfa nesnesi deseninin basitlik-fayda dengesi daha uygundu.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Assertion nerede yaşar: step definition mı page object mi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Assertion nerede yaşar?</text>
  <rect x="40" y="46" width="260" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="170" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Step definition</text>
  <text x="170" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">expect(...).toBeVisible()</text>
  <text x="170" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"doğru mu?" sorusunu</text>
  <text x="170" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">burada sorarız</text>
  <text x="170" y="154" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ test kırılabilir</text>
  <rect x="340" y="46" width="260" height="118" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="470" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Page object</text>
  <text x="470" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">expect.poll(...) → senkronizasyon</text>
  <text x="470" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"ne yapılır / ne okunur"</text>
  <text x="470" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sorularını cevaplar</text>
  <text x="470" y="154" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">→ rolü doğrulama değil, bekleme</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Page object'lerinde assertion var mı?"** Beklenen: iş kuralını yargılayan doğrulamalar step definition'larda. Page object'ler "ne yapılır/ne okunur" der, "iş beklentisi karşılandı mı" demez. Tek görünen istisna `expect.poll` — o teknik olarak bir assertion'dır (zaman aşımında adımı düşürür) ama oradaki rolü doğrulama değil senkronizasyondur.
- **"Neden Page Object Pattern seçtin, alternatifi düşündün mü?"** Beklenen: case zaten istiyordu ama katkısı net — locator merkezileştirme + davranış kapsülleme. Alternatif olarak screenplay pattern düşünülebilirdi, ama bu ölçekte fazla soyutlama olurdu.
- **"`addToCart()` neden döngü içinde tıklama yapıyor?"** Beklenen: buton, Angular handler bağlanmadan önce görünür/"enabled" görünür; erken tıklama sessizce hiçbir şey yapmaz. Sitenin kendi onay modalı kanıt olarak beklenir, açılmazsa tıklama tekrarlanır.
- **"Bir CSS seçici değişirse kaç dosya güncellersin?"** Beklenen: tek dosya, `pages/locators.js`. Page object'ler ve step'ler bu dosyadan okur, seçiciyi kendileri barındırmaz.
- **"`BasePage`'de ne var, neden ayrı bir sınıf?"** Beklenen: her sayfada tekrar eden şeyler — gezinme, çerez banner'ı ve sepet modalı gibi sayfanın üstünü örten pencereleri (overlay) kapatma. Tekilleştirme olmasa her page object bu kodu kendi başına tekrar ederdi.
- **"`expect.poll` kullanman POM kuralını çiğnemiyor mu?"** Beklenen: hayır — ama "o assertion değil" demek yanlış olur. `expect.poll(...).toBe(true)` teknik olarak bir assertion'dır: koşul 25 saniye içinde sağlanmazsa hata fırlatır ve adım düşer. Kuralı çiğnememesinin nedeni kategorisi değil **rolü**: orada bir iş beklentisi ("ara toplam 450 TL olmalı") yargılanmıyor, sunucu onaylı durumun oluşması bekleniyor. İş beklentileri yine step'lerde doğrulanıyor.

### Tek cümlede

Sayfa nesnesi deseni, seçicileri `locators.js` dosyasında toplar ve sayfa davranışını sayfa nesnelerinde kapsüller; böylece tekrarı ve kırılganlığı azaltır. İş kuralını yargılayan doğrulama ise her zaman adım tanımlarının işidir, sayfa nesnelerinin değil.

# pom-1 | Orta
## Soru
`ProductPage.addToCart()` metodunda buton tıklandıktan sonra sepete ekleme onay penceresinin (modal) açılıp açılmadığı kontrol ediliyor, açılmazsa tıklama tekrarlanıyor. Bu yaklaşımın temel gerekçesi nedir?
## A
Playwright'ın `click()` metodu bazen hiçbir hata vermeden başarısız olur, bu yüzden her tıklama iki kez yapılmalıdır
🤖 Gerekçe `click()`'in genel güvenilmezliği değil; sorun bu sayfaya özgü bir zamanlama durumudur.
## B
Buton görünür ve "enabled" görünse de Angular'ın tıklama işleyicisi bağlanmamış olabilir; onay modalı gerçek kanıttır
🤖 Doğru: görünürlük ve enabled kontrolü işleyicinin bağlandığını garanti etmez, erken tıklama sessizce hiçbir şey yapmaz — modal kanıt olarak beklenir.
## C
Sepete ekleme işlemi sunucu tarafında yavaş olduğu için üç deneme performans amaçlı yapılır
🤖 Amaç performans değil; buton hazır olmadan tıklamanın sessizce başarısız olmasına karşı doğrulama.
## D
Butondaki `disable` sınıfı yüzünden Playwright elementi hazır saymaz; döngü, bu sınıfın kalkmasını beklemek için yazılmıştır
🤖 Ters yönde: `disable` sınıfı Playwright'ın "enabled" kontrolüne hiç görünmez (o kontrol `disabled` özniteliğine bakar) ve sınıf zaten hiç kalkmaz.
## Doğru
B
## Açıklama
Buton, Angular kendi tıklama işleyicisini bağlamadan önce DOM'da görünür hale gelir ve üzerindeki `disable` CSS sınıfı hiç kalkmaz. Playwright'ın "enabled" kontrolü bu sınıfı zaten görmez — o kontrol `disabled` özniteliğine ve `aria-disabled`'a bakar — yani buton Playwright'a hep "aktif" görünür; ne görünürlük ne enabled kontrolü butonun gerçekten tıklamaya hazır olduğunu söyler. Erken bir tıklama hiçbir hata üretmeden hiçbir şey yapmaz, sepet sessizce boş kalır. `addToCart()` bu belirsizliği, sitenin kendi ürettiği onay penceresini kanıt olarak bekleyerek çözer: pencere açılmazsa tıklama tekrarlanır (en fazla üç kez), açılırsa döngüden çıkılır. Her denemenin onay penceresine tanıdığı süre (8 sn) bilinçli olarak geniştir — zaten işe yaramış bir tıklamayı erken pes edip tekrarlamak ürünü sepete iki kez eklerdi. Denemeler arasında ayrıca bir gecikme yoktur.

# pom-2 | Orta
## Soru
Bir ekip arkadaşı `ProductPage` içine `expect(await this.title.first().innerText()).toContain('Bebek Bezi')` gibi bir assertion eklemek istiyor. Page Object Pattern'in bu projedeki uygulanışına göre bu neden uygun değildir?
## A
Playwright'ın `expect` fonksiyonu page object dosyalarından içe aktarılamaz; yalnız step modüllerinde kullanılabilir
🤖 Teknik bir kısıtlama değil; `expect` her dosyadan içe aktarılabilir. Sorun mimari sorumluluk ayrımıdır.
## B
Assertion'lar yalnızca `CartPage` sınıfında toplanmalıdır, diğer page object'lerde yasaktır
🤖 Kural sayfaya özgü değil, geneldir: hiçbir page object'te assertion olmamalıdır, `CartPage` istisna değildir.
## C
`innerText()` metodu asenkron olduğu için assertion içinde kullanılamaz
🤖 `innerText()` zaten `async`/`await` ile kullanılıyor; asenkron olması assertion'ı engellemez.
## D
Page object'ler "ne yapılır" ve "ne okunur" sorularını cevaplar; "doğru mu" sorusu step'lerin işidir
🤖 Doğru: doğrulamalar step tanımlarında yaşar, page object'ler aksiyon ve veri okuma sağlar, sonucu yargılamaz.
## Doğru
D
## Açıklama
Bu projede page object'lerin sorumluluğu net çizilmiştir: bir eylemi gerçekleştirmek (`addToCart`, `increaseQuantity`) veya bir veriyi okuyup döndürmek (`productName`, `lineTotal`). "Bu doğru mu, bu bekleneni karşılıyor mu" sorusunu sormak — yani assertion yazmak — step definition'ların işidir. Bu ayrım aynı page object'in farklı senaryolarda farklı beklentilerle (farklı ürün adı, farklı fiyat) yeniden kullanılmasını sağlar; assertion page object'e gömülseydi her yeni beklenti için page object'i değiştirmek gerekirdi. `expect.poll` kullanımı bu kuralı çiğnemez: teknik olarak bir assertion olsa da orada yargılanan bir iş beklentisi yoktur, yalnız sayfanın kararlı hâle gelmesi beklenir.

# pom-3 | Orta
## Soru
`CartPage.increaseQuantity()` içinde miktar artırma butonuna tıklandıktan sonra `expect.poll` ile hem satırdaki miktarın arttığı hem de özet metninin değiştiği kontrol ediliyor. Bu kullanım, "page object'lerde assertion olmaz" kuralını çiğniyor mu?
## A
`expect.poll` teknik olarak bir assertion'dır; ama rolü iş kuralı yargılamak değil, sunucu onaylı durumun oluşmasını beklemektir
🤖 Doğru: kuralın koruduğu şey kategori değil sorumluluk — iş beklentisini yargılayan assertion'lar yine step'lerde kalıyor.
## B
Evet; `expect.poll` de bir assertion olduğuna göre onu page object'e koymak kuralın açık bir ihlalidir ve bu satır step definition'a taşınmalıdır
🤖 İlk yarısı doğru, sonucu yanlış: kural "assertion sözdizimi" yasağı değil, iş kuralı doğrulamasının step'lerde kalması kuralıdır.
## C
Hayır; `expect.poll` yalnızca `CartPage` gibi hesap yapan sınıflarda kullanılabilir, diğer page object'lerde kullanılamaz
🤖 Kısıtlama sınıfa özgü değil; `expect.poll`'un rolü hangi sınıfta kullanıldığına bakılmaksızın aynıdır.
## D
Evet, çiğniyor; ama bu proje ölçeğinde kabul edilebilir bir istisna olarak bilinçli şekilde bırakılmıştır
🤖 Burada tolere edilen bir ihlal yok: `expect.poll` bir iş beklentisini yargılamıyor, dolayısıyla "istisna" çerçevesine hiç gerek kalmıyor — ölçekle de ilgisi yok.
## Doğru
A
## Açıklama
`increaseQuantity()` metodunda miktar artırma optimistik biçimde güncellenir: sunucu onayından önce ekranda yeni değer görünebilir, isteğin sonuçlanmaması durumunda satır "2" yazsa bile özet toplamı hâlâ tek ürünü yansıtabilir. `expect.poll`, hem miktarın arttığını hem özetin değiştiğini — yani sunucu onaylı sinyali — bekler ve koşul sağlanana kadar (25 sn) periyodik olarak yeniden dener; süre dolarsa hata fırlatır ve adım düşer, yani teknik olarak bir assertion'dır. Bu davranış klasik bir **değer** assertion'ından farklıdır: `expect(x).toBe(y)` tek seferlik bir kontroldür. Playwright'ın locator assertion'ları (`toBeVisible`, `toHaveCount`) da tıpkı `expect.poll` gibi koşul sağlanana kadar yeniden dener; `expect.poll`'un farkı, locator'la ifade edilemeyen keyfi bir hesabı (burada "adet arttı **ve** özet değişti") yoklayabilmesidir. Kuralı çiğnememesinin nedeni kategorisi değil **rolü**: burada bir iş beklentisi ("ara toplam 450 TL olmalı") yargılanmıyor, sayfanın kararlı hâle gelmesi bekleniyor. İş beklentileri — ara toplamın satır toplamlarına eşitliği gibi — yine step definition'larda doğrulanıyor.

# pom-4 | Orta
## Soru
`e-bebek` sitesinde bir ürünün CSS seçicisi (`button.btn-add`) değişirse, bu projenin Page Object Pattern uygulamasında kaç dosya güncellenmesi gerekir?
## A
Yalnızca `pages/locators.js` — seçiciler tek bu dosyada tanımlıdır, page object'ler ve step'ler oradan okur
🤖 Doğru: `locators.js` tek kaynak (single source of truth); değişiklik page object veya feature dosyalarına sızmaz.
## B
`pages/locators.js` ile birlikte, seçiciyi kullanan her page object dosyası (`ProductPage.js`, `CartPage.js` vb.)
🤖 Page object'ler seçiciyi `locators` üzerinden referans alır, kendi içlerinde string seçici barındırmaz.
## C
Yalnızca ilgili `.feature` dosyası, çünkü seçiciler senaryo adımlarında yazılıdır
🤖 Bu proje feature dosyalarını selector'suz tutar; seçiciler senaryolarda hiç görünmez.
## D
`pages/locators.js`, ilgili page object ve `features/support/element-registry.js` — üçü birden
🤖 `element-registry.js` iş dili isimlerini `locators.js`'e eşler; seçici değeri kendisi barındırmaz, güncelleme gerektirmez.
## Doğru
A
## Açıklama
Bu projede tüm CSS seçiciler `pages/locators.js` içinde tek yerde toplanır (ör. `product.addToCartButton: 'button.btn-add'`). Page object'ler (`ProductPage`, `CartPage`, `BasePage`) bu tanımları `this.locator(locators.product.addToCartButton)` gibi referans alır, kendi içlerinde ham seçici string'i barındırmaz. `element-registry.js` da aynı şekilde iş dili isimlerini (`'sepete ekle butonu'`) `locators` nesnesindeki tanıma eşler — o dosya da seçici değerini kopyalamaz, yalnız işaret eder. Sonuç: bir CSS seçici değiştiğinde güncellenmesi gereken tek yer `locators.js`'tir; bu, Page Object Pattern'in "kırılganlığı azaltma" faydasının somut kanıtıdır.

# pom-5 | Orta
## Soru
Case study zaten Page Object Pattern kullanılmasını istiyordu. Screenplay pattern gibi daha soyut bir alternatif yerine POM'un tercih edilmesinin ek gerekçesi olarak proje notlarında ne belirtiliyor?
## A
Screenplay pattern Playwright ile teknik olarak uyumlu değildir; aktör/görev/soru soyutlaması JavaScript'te kurulamaz
🤖 Uyumsuzluk teknik değil; screenplay pattern JavaScript'te de uygulanabilir, sorun ölçek-soyutlama dengesidir.
## B
POM, screenplay pattern'e göre performans açısından daha hızlı test çalıştırması sağlar
🤖 Tercih gerekçesi çalışma hızı değil; ikisi de aynı Playwright çağrılarını sarmalar, hız farkı anlamlı değildir.
## C
Bu ölçekte (birkaç sayfa, sınırlı senaryo) screenplay pattern'in getireceği ek soyutlama fazla olurdu
🤖 Doğru: proje küçük/orta ölçekli; aktör/görev/soru gibi ek katmanlar bu ölçekte karmaşıklığı haklı çıkarmaz.
## D
Screenplay pattern yalnızca mobil test otomasyonunda kullanılabilir, web projelerinde desteklenmez
🤖 Screenplay pattern web test otomasyonunda da yaygın kullanılır; kısıtlama platform değil ölçek gerekçesidir.
## Doğru
C
## Açıklama
Screenplay pattern, testleri "aktör" (kullanıcı), "görev" (task) ve "soru" (question) soyutlamalarıyla modelleyen daha katmanlı bir yaklaşımdır; büyük, çok aktörlü, uzun ömürlü test paketlerinde faydası artar. Bu projede sayfa sayısı ve senaryo çeşitliliği sınırlıdır; Page Object Pattern'in sağladığı iki somut fayda — locator merkezileştirme ve davranış kapsülleme — zaten yeterlidir. Screenplay pattern'in ek soyutlama katmanları (aktör tanımları, görev nesneleri) bu ölçekte okunabilirliği artırmak yerine gereksiz dolaylılık (indirection) ekler; bu yüzden case'in istediği POM, ölçeğe uygun ve yeterli bir seçim olarak bilinçli şekilde korunmuştur.

# pom-6 | Orta
## Soru
`BasePage` sınıfı, `dismissOverlays()` metoduyla her sayfa geçişinde çerez banner'ını ve (varsa) sepet onay modalını kapatmaya çalışır. Bu davranış neden `ProductPage` veya `CartPage` içine değil, `BasePage`'e konmuştur?
## A
`BasePage` diğer sınıflardan performans olarak daha hızlı çalışır, bu yüzden ortak kod oraya taşınır
🤖 Yerleşim kararı performansla değil, davranışın kaç sayfada tekrar ettiğiyle ilgilidir.
## B
Çerez banner'ı ve sepet modalı yalnızca `ProductPage`'de görülür, bu yüzden mantıken oraya ait değildir
🤖 Tam tersi: sayfanın üstünü örten bu pencereler birden fazla sayfada karşılaşılabilir, bu yüzden ortaklaştırılmıştır.
## C
Bu davranış her sayfada karşılaşılabilecek ortak bir sorundur; `BasePage`'de tutmak aynı kodun tekrarını önler
🤖 Doğru: `goto()` çağrısının ardından `dismissOverlays()` çalışır; her sayfa bunu miras alır, kendi başına yazmaz.
## D
`dismissOverlays()` Cucumber'ın `Before` hook'una konsaydı aynı sonucu verirdi; `BasePage`'e konması gereksiz bir kalıtım kullanımıdır
🤖 Tartışmaya değer ama eksik: `Before` senaryo başında bir kez çalışır; oysa bu pencereler her gezinmeden sonra yeniden açılabildiği için davranışın `goto()`'ya bağlı olması gerekir.
## Doğru
C
## Açıklama
Çerez banner'ı ve sepete ekleme onay modalı, hangi sayfada olunursa olunsun geç açılabilen ve tıklamaları yutabilen, sayfanın üstünü örten (overlay) pencerelerdir; ikisinin de her sayfada garanti bulunacağı yoktur, bu yüzden `dismissOverlays()` "varsa kapat, yoksa beklemeden devam et" mantığıyla yazılmıştır. Bu davranış sayfaya özgü değil, gezinmenin (`goto()`) doğal bir parçasıdır — `BasePage.goto()` her çağrıldığında `dismissOverlays()`'i tetikler. Eğer bu mantık `ProductPage` ve `CartPage` içine ayrı ayrı yazılsaydı, hem kod tekrar ederdi hem de yeni bir page object eklendiğinde aynı sorun unutulabilirdi. `Before` hook'una taşımak (D) ilk bakışta makul görünür ama yetmez: hook senaryo başında bir kez çalışır, oysa bu pencereler her yeni sayfa açılışında yeniden belirebilir — davranışın gezinmeye bağlı olması bu yüzden gerekir. Kalıtım burada tam da bunun için kullanılır: ortak davranış üst sınıfta bir kez yazılır, alt sınıflar bunu miras alır.
