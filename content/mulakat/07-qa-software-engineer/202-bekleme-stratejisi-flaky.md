---
slug: bekleme-stratejisi-flaky
baslik: "Bekleme Stratejisi ve Flaky Test Yönetimi"
kademe: İleri
sira: 202
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

**Kararsız test (flaky test)**, aynı kodu aynı ortamda koşturduğunda bazen yeşil bazen kırmızı yanan testtir. Hatanın nedeni test edilen özellik değildir; testin **zamanlama varsayımıdır**.

Bu, kullanıcı arayüzü (UI) testlerinde en sık görülen kalite sorunudur. Sayfanın çizilmesi, ağ isteği, animasyon ve tarayıcının olay döngüsü — hepsi eşzamansız (asenkron) çalışır. Test kodu "şu an hazır" diye varsayıp devam ederse bazen haklı, bazen haksız çıkar.

Bu projede vaka çalışmasının (case study) açık bir kuralı vardı: **sabit süre beklemek (`sleep` / `waitForTimeout`) yasak**. Sebep teknik değil, kavramsaldır. Sabit bir bekleme süresi iki türlü de yanlıştır. Ya gereksiz uzundur; durum hızlı gerçekleşince boşuna beklenir. Ya da yetersizdir; durum yavaş gerçekleşince hâlâ erken davranılır.

Hiçbir sabit sayı **yarış durumunu (race condition)** çözmez; yalnızca görünmez kılar. Test bir süre yeşil kalır. Ortam yavaşladığında — sürekli tümleştirme (CI) sunucusu yüklendiğinde, ağ geciktiğinde, paralel koşumda — test yeniden kırmızı yanar.

Bu bölüm, sabit süre yerine **koşullu bekleme** ile "kanıt" temelli doğrulamanın nasıl kurulduğunu anlatıyor. Anlatım gerçek kararsızlık vakaları üzerinden ilerliyor.

### Temel kavramlar

- **Koşullu bekleme (conditional wait)**: "şu kadar bekle" değil, "şu koşul doğru olana kadar bekle" mantığı. Bu ailenin dört üyesi vardır. Playwright'ın kendiliğinden beklemesi (auto-wait) işlemi, öğe görünür ve etkileşime hazır olana kadar erteler. `expect(locator)` bir sayfa koşulunu zaman aşımına kadar yeniden dener. `expect.poll(fn)` herhangi bir eşzamansız ifadeyi koşul sağlanana kadar yeniden dener. Son olarak projeye özel yardımcılar vardır (`utils/waits.js`).
- **Kanıt (proof)**: bir işlemin **gerçekten yürüdüğünü gösteren gözlenebilir belirti**. Kanıt seçimi kararsız testin can alıcı noktasıdır. Yanlış kanıt seçilirse — örneğin "buton görünür" veya "adet metni değişti" — işlem başarısız olsa bile test yeşil yanabilir. Doğru kanıt, sunucunun ya da uygulamanın **kendi ürettiği** bir sonuçtur: onay penceresinin açılması, özet metninin değişmesi gibi. İstemcinin iyimser tahmini kanıt sayılmaz.
- **İyimser güncelleme (optimistic UI update)**: arayüzün, sunucu cevabını beklemeden isteğin başarılı olacağını varsayıp anında güncellenmesi. Kullanıcıya hızlı hissettirir ama test için tuzaktır. Arayüz güncellendi diye istek gerçekten sunucuya ulaşmış olmayabilir.
- **Yeniden deneme ile kök neden analizi arasındaki fark**: Cucumber'ın yeniden deneme (retry) mekanizması başarısız senaryoyu otomatik tekrar koşturabilir. Bu, kararsızlığı **çözmez, gizler**. Testi yeşile çevirir, ama altındaki gerçek uygulama davranışını ortaya çıkarmaz. O davranış erken tıklanabilen bir buton veya iyimser bir güncelleme olabilir.
- **İşlem zaman aşımı ile doğrulama zaman aşımı ayrıdır**: Playwright'ta bir tıklamanın veya doldurmanın ne kadar bekleyeceği (action timeout) ile bir doğrulamanın ne kadar yeniden deneneceği (assertion timeout) **ayrı ayarlardır**. Birini uzatmak diğerini uzatmaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kanıt kavramı: buton görünürlüğü ile onay penceresi arasındaki fark">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yanlış kanıt: "görünür" ≠ "hazır"</text>
  <rect x="30" y="46" width="270" height="140" rx="12" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B5504F" font-family="system-ui">Buton görünürlüğü</text>
  <text x="165" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">buton DOM'da erken belirir</text>
  <text x="165" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Angular olay dinleyicisi</text>
  <text x="165" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">henüz bağlı değil</text>
  <text x="165" y="158" text-anchor="middle" font-size="11" fill="#B5504F" font-family="system-ui">→ erken tıklama sessiz kalır</text>
  <rect x="340" y="46" width="270" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Onay penceresi (modal)</text>
  <text x="475" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sitenin kendi ürettiği</text>
  <text x="475" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">gözlenebilir sonuç</text>
  <text x="475" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">gelmezse tıklama</text>
  <text x="475" y="154" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tekrarlanır</text>
  <text x="475" y="176" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">→ güvenilir kanıt</text>
</svg>
```

### Nasıl çalışır?

**1) Maskeli telefon alanı — yazma yönteminin yarattığı yarış.** Giriş formundaki telefon alanı maskelidir. Rakamları tek tek yazan yaklaşım (`pressSequentially`), maskenin imleci yeniden konumlandırmasıyla yarıştı. **Paralel koşumda** haneler yer değiştirdi: `5551234567` yerine `5551234657` yazıldı.

Çözüm iki adımlıydı. Değer **tek işlemde** yazılacak, sonra tıklamadan önce formun değeri gerçekten aldığı doğrulanacak (`pages/LoginPage.js`):

```js
async submitPhone(phone) {
  await this.phoneInput.click();
  await this.phoneInput.fill(phone);
  await expect
    .poll(async () => (await this.phoneInput.inputValue()).replace(/\D/g, '').replace(/^90/, ''))
    .toBe(phone);
  await this.submitButton.first().click();
}
```

`fill` alanın değerini tek seferde atar, bu yüzden maskeyle yarışmaz. `expect.poll` ise "değer forma gerçekten işlendi mi?" sorusunu sorar. Bunu göze görünen yazıya bakarak değil, `inputValue()` ile nesnel biçimde yapar.

**2) Sepete ekleme sessizce başarısız olabiliyor.** "Sepete Ekle" butonu, Angular'ın olay dinleyicisi bağlanmadan çok önce ekranda görünür. Üstelik buton `disable` sınıfını hiç bırakmaz; Playwright'ın "etkin mi" kontrolü de bu durumu göremez.

Erken yapılan tıklama **hiçbir şey yapmaz ve hata da vermez**. Sonuç birkaç adım sonra "sepet boş" olarak ortaya çıkar. Çözüm şöyle (`pages/ProductPage.js`):

```js
async addToCart() {
  const modal = this.locator(locators.common.addToCartModal);
  const button = this.addToCartButton.first();
  await button.waitFor({ state: 'visible' });

  for (let attempt = 1; attempt <= 3; attempt += 1) {
    if (await modal.isVisible()) {
      await this.closeAddToCartModal();
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

`if (await modal.isVisible())` satırı bilinçli bir temizliktir. Bir önceki üründen kalmış bir onay penceresi varsa onu kapatır. Bu temizlik olmasa, o eski pencere **bu ürün için kanıt sanılırdı**. Tıklama hiç işlememiş olsa bile test "başarılı" derdi.

Bu çözümün bilinen bir tavizi var. Tıklama gerçekten işlediyse ama pencere 8 saniyeden geç açıldıysa, ürün iki kez eklenmiş olur. Gözlenen açılma süresi 1-3 saniye olduğu için 8 saniyelik sınır bilinçli olarak geniş tutulmuştur.

**3) Adet artışında iyimser güncelleme.** Sepette adet metni **iyimser** güncellenir. İstek sunucuya hiç ulaşmasa bile satır "2" gösterir; tutarlar ise hâlâ tek ürünü anlatır. Adet metnini kanıt saymak bir hataya yol açtı: ara toplam 20 saniye boyunca eski değerde kaldı.

Çözüm, beklemeyi adet metninden **sunucunun onayladığı özet değişimine** taşımaktı (`pages/CartPage.js`):

```js
async increaseQuantity(index) {
  const line = this.lineByIndex(index);
  const before = await this.quantityOf(index);
  const summaryBefore = await this.summaryText();

  await line.locator(locators.cart.increaseButton).click();

  await expect
    .poll(async () => {
      const quantity = await this.quantityOf(index).catch(() => before);
      const summary = await this.summaryText().catch(() => summaryBefore);
      return quantity > before && summary !== summaryBefore;
    }, { timeout: 25000 })
    .toBe(true);

  return before + 1;
}
```

`.catch(() => before)` ve `.catch(() => summaryBefore)` şunu varsayar: sayfa yeniden çizilirken okuma anlık olarak hata verebilir. Bu hata `poll` döngüsünü kırmamalı, sadece "henüz olmadı" anlamına gelmelidir. Okuma hatası ile "koşul sağlanmadı" durumu birbirine karıştırılmamalıdır.

**4) İşlem zaman aşımı ile doğrulama zaman aşımı farkı.** Tarayıcı bağlamı 20 saniyelik işlem zaman aşımıyla kuruluyordu. Ama Playwright'ta `expect` varsayılanı **5 saniyedir** ve bağlam ayarından etkilenmez. Sunucudan dönen bir geçişi doğrulayan adımlar bu yüzden ara ara düşüyordu.

Çözüm, `utils/expect.js` içinde işlemlerle **aynı süreyi kullanan ortak bir `expect`** tanımlamaktı:

```js
const expect = playwrightExpect.configure({ timeout: config.defaultTimeout });
```

İki kararsızlık daha aynı ilkeyle çözüldü. Bunlar bu bölümün sorularına konu edilmedi, ama README'de belgelendi:

- **Arama sonuçlarının yönlendirme (redirect) sırasında okunması.** Sonuç veren bir arama terimi kendi kategori sayfasına yönlendirilir. O geçiş anında eski sayfanın ürünleri hâlâ ekrandadır. Çözüm, listeyi hedef sayfada sabitlenene kadar yeniden okumaktır. Ölçüt **ilişki oranıdır**: `SearchResultsPage.relatedRatio()`, sonuç başlıklarının yüzde kaçının aranan kelimeyi içerdiğini döner. `search.steps.js` bu oranın en az 0,7 (%70) olmasını bekler. Listenin sabitlendiğine süre sayarak değil, `utils/waits.js` içindeki **`waitForListToSettle`** yardımcısıyla karar verilir. Bu yardımcı, öğe sayısı iki ardışık ölçümde aynı kalana kadar bekler.
- **Gecikmeli açılan çerez şeridinin (banner) tıklamaları engellemesi.** Şerit sepete ekleme sonrasında beliriyor ve tıklamaların önüne geçiyordu. Çözüm yine `utils/waits.js` içindeki **`dismissIfVisible`** yardımcısıdır: şerit görünürse kapatır, görünmezse bekletmeden geçer. Kapatma denemesi zorunlu değildir, çünkü şerit kendiliğinden de kapanabilir.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zaman çizelgesi: iyimser güncelleme ile sunucu onayı arasındaki boşluk">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İyimser güncelleme vs sunucu onayı</text>
  <line x1="60" y1="70" x2="600" y2="70" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="60" y1="150" x2="600" y2="150" stroke="#5D7789" stroke-width="1.4"/>
  <text x="60" y="196" font-size="10.5" fill="#5D7789" font-family="system-ui">t=0 tıklama</text>
  <text x="560" y="196" text-anchor="end" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman →</text>
  <circle cx="90" cy="70" r="5" fill="#B5504F"/>
  <text x="90" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#B5504F" font-family="system-ui">adet metni "2"</text>
  <text x="90" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">anında (iyimser)</text>
  <rect x="90" y="106" width="230" height="26" rx="6" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.3"/>
  <text x="205" y="123" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">tehlikeli aralık: adet≠özet</text>
  <circle cx="380" cy="150" r="5" fill="#25804C"/>
  <text x="380" y="182" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">özet değişti (sunucu onayı)</text>
  <text x="380" y="132" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gerçek kanıt burada</text>
  <line x1="90" y1="70" x2="90" y2="150" stroke="#5D7789" stroke-width="1" stroke-dasharray="3,3"/>
  <line x1="380" y1="70" x2="380" y2="150" stroke="#5D7789" stroke-width="1" stroke-dasharray="3,3"/>
</svg>
```

Cucumber'ın kendi **yeniden deneme (retry)** özelliği de bilinçli olarak kullanılmadı. Gerekçe şu: yeniden deneme kararsızlığı çözmez, gizler. Önce kök neden arandı. Bulunanlar gerçek uygulama davranışlarıydı: iyimser güncellenen adet metni ve olay dinleyicisi bağlanmadan tıklanabilen buton. Bunlar yeniden denemeyle geçiştirilseydi hiç öğrenilemezdi.

### Mülakatta ne sorulur?

- **"Kod tabanında hiç sleep yok mu, nasıl garanti ediyorsun?"** Beklenen: `waitForTimeout`/`sleep` aranabilir, hiç geçmiyor; her bekleme somut bir koşula (görünürlük, satır sayısı, özet metni) bağlı.
- **"En zorlandığın flaky durum neydi, nasıl buldun?"** Beklenen: maskeli telefon alanı — yalnız paralel koşumda ortaya çıkması, seri koşumda hiç görünmemesi kanıttı; kök neden yazma stratejisiydi, zamanlama değil.
- **"Neden Cucumber'ın retry özelliğini kullanmadın?"** Beklenen: retry semptomu gizler, kök nedeni gizler; case study'de flaky'ler gerçek uygulama davranışlarını (Angular handler gecikmesi, iyimser güncelleme) ortaya çıkardı.
- **"Zaman aşımı değerlerini nasıl seçtin?"** Beklenen: aksiyon 20 sn, navigasyon 45 sn (`.env`'den ayarlanabilir); `expect`'in kendi 5 sn'lik varsayılanı fark yarattığı için `utils/expect.js` ile aksiyonlarla eşitlendi.
- **"Sepete eklemede retry mantığı çift ekleme riski taşımıyor mu?"** Beklenen: evet, bilinen ve belgelenmiş bir taviz — pencere süresi (8 sn) gözlenen açılma süresinin (1-3 sn) belirgin üzerinde tutularak risk küçültüldü; kesin çözüm sepeti API'den doğrulamak olurdu.
- **"expect.poll içindeki .catch neden var?"** Beklenen: sayfa yeniden render olurken okuma anlık patlayabilir; bu durumu "koşul sağlanmadı" gibi ele almak için, poll'u kırmamak için.

### Tek cümlede

Kararsız test yönetimi üç işten oluşur. Sabit süre yerine **gözlenebilir kanıta** dayalı koşullu bekleme kurmak. Kanıtı doğru seçmek: buton görünürlüğünü değil, sunucu onayını beklemek. Belirtiyi yeniden denemeyle gizlemek yerine **kök nedeni** bulup düzeltmek; bu kök neden iyimser güncelleme, geç bağlanan olay dinleyicisi veya farklı zaman aşımı bütçeleri olabilir.

# flaky-1 | İleri
## Soru
Case study kuralı "sleep / waitForTimeout kullanımı yasaktır" diyor. Bir ekip arkadaşın "madem UI gecikmeleri değişken, neden sabit bir süre (ör. 3 saniye) yeterince güvenli olmasın?" diye soruyor. Bu kurala teknik olarak en isabetli gerekçe hangisidir?
## A
Playwright API'sinde `waitForTimeout` metodu artık kaldırıldığı için kullanılamaz
🤖 Metot hâlâ Playwright API'sinde var; yasak API eksikliğinden değil, tasarım kararından.
## B
Sabit süre yarış koşulunu çözmez, yalnız gizler: ya gereksiz yavaştır ya da hâlâ erkendir
🤖 Doğru: sabit bir sayı her koşumda aynı garantiyi vermez; ortam yavaşladığında (CI yükü, paralel koşum) yine erken kalır.
## C
Sabit süre kullanmak Allure raporunda adım sayısını yapay olarak artırır, okunabilirliği bozar
🤖 Rapor okunabilirliği ayrı bir konu; asıl sorun testin güvenilirliği, rapor biçimi değil.
## D
CI ortamında `waitForTimeout` çağrıları güvenlik taraması tarafından otomatik engellenir
🤖 Böyle bir engelleme yok; kısıtlama proje kuralı/tasarım tercihidir, araç kısıtlaması değil.
## Doğru
B
## Açıklama
Sabit bir bekleme süresi (`sleep`/`waitForTimeout`) iki türlü de yanlıştır: durum beklenenden hızlı gerçekleşirse test gereksiz yere yavaşlar; durum beklenenden yavaş gerçekleşirse (CI yükü, ağ gecikmesi, paralel koşumda kaynak paylaşımı) süre hâlâ yetersiz kalır ve test erken davranır. Hiçbir sabit sayı yarış koşulunu **çözmez**, yalnızca bir süreliğine görünmez kılar — ortam koşulları değiştiğinde flaky davranış geri döner. Doğru çözüm, süre tahmin etmek değil, koşulun kendisini (element görünürlüğü, özet metninin değişmesi gibi) beklemektir.

# flaky-2 | İleri
## Soru
Giriş formundaki maskeli telefon alanına önce `pressSequentially` (rakam rakam yazma) kullanılmış, ardından `fill` + `expect.poll` kombinasyonuna geçilmiş:
```js
async submitPhone(phone) {
  await this.phoneInput.click();
  await this.phoneInput.fill(phone);
  await expect
    .poll(async () => (await this.phoneInput.inputValue()).replace(/\D/g, '').replace(/^90/, ''))
    .toBe(phone);
  await this.submitButton.first().click();
}
```
Bu değişikliğin asıl nedeni neydi?
## A
`pressSequentially` maskenin imleç mantığıyla yarışıp haneleri yer değiştirebiliyordu; `fill` bu yarışa girmez
🤖 Doğru: tek tek yazma, maskenin kendi imleç mantığıyla çakışıp haneleri karıştırdı (5551234567 yerine 5551234657) ve bu özellikle paralel koşumda gözlemlendi; `fill` tek işlemde yazdığı için bu yarışa girmez.
## B
`pressSequentially` Playwright'ın son sürümünde kaldırıldığı için `fill`'e geçmek zorunlu oldu
🤖 API kaldırılmadı; değişim davranışsal bir hatayı (hane karışması) çözmek içindi, uyumluluk zorunluluğu değil.
## C
`fill` metodu klavye olaylarını (keydown/keyup) tetiklediği için maskeyle daha uyumlu çalışıyor
🤖 Tam tersi: `fill` DOM değerini doğrudan atar, klavye olaylarını simüle etmez; asıl avantajı maskeyle yarışa hiç girmemesidir.
## D
`expect.poll` eklenmeden önce alan tamamen boş kalıyordu; değer hiç yazılmadığı için gönder butonu boş formu gönderiyordu
🤖 Alan boş kalmıyordu; sorun değerin yazılmaması değil, yanlış sırada/hatalı yazılmasıydı — `expect.poll` bunu doğrulamak için eklendi.
## Doğru
A
## Açıklama
Maskeli alanlar, kullanıcının yazdığı her tuşa göre imleci ve görüntülenen değeri kendi mantığıyla yeniden konumlandırır. `pressSequentially` her rakamı ayrı bir tuş olayı olarak gönderdiği için bu iç mantıkla **yarışır**; sonuç, özellikle kaynakların paylaşıldığı paralel koşumda hanelerin karışmasıydı (`5551234567` yerine `5551234657`). `fill` değeri **tek işlemde** DOM'a yazdığı için bu yarışa hiç girmez. `expect.poll` ise ek bir güvence katmanıdır: değerin forma gerçekten işlendiğini, göze güvenmek yerine `inputValue()` üzerinden nesnel olarak doğrular — böylece bir sonraki adım (gönder butonuna tıklama) yanlış bir değer üzerinden ilerlemez.

# flaky-3 | İleri
## Soru
`ProductPage.addToCart()` içindeki şu kontrolün amacı nedir?
```js
for (let attempt = 1; attempt <= 3; attempt += 1) {
  if (await modal.isVisible()) {
    await this.closeAddToCartModal();
  }
  await button.click();
  // ...
}
```
## A
Sayfa her yenilendiğinde modal bileşenini yeniden DOM'a eklemek için gereken bir geçici çözüm
🤖 Modal'ı DOM'a ekleme/kaldırma bu satırın işi değil; satır yalnızca mevcut görünürlüğü kontrol edip gerekirse kapatıyor.
## B
Angular uygulamasının aynı anda birden fazla modal açmasını engellemek için tarayıcı düzeyinde bir kısıtlama uygulamak
🤖 Bu satır tarayıcıya bir kısıtlama uygulamaz; yalnızca test tarafında hangi pencerenin hangi tıklamaya ait olduğunu netleştirir.
## C
Önceki üründen kalmış açık bir onay penceresi varsa kapatmak — yoksa bu ürün için yanlışlıkla "kanıt" sayılabilir
🤖 Doğru: eski pencere kapatılmazsa, bu tıklama hiç işlemese bile eski pencerenin varlığı "onaylandı" sanılıp yanlış pozitif üretir.
## D
Üç deneme hakkının ilkini her zaman "ısınma" turu olarak harcayıp asıl tıklamayı ikinci denemeden başlatmak
🤖 Deneme sayısı ısınma amaçlı harcanmıyor; her deneme gerçek bir tıklama denemesi, ilk denemede de kanıt gelirse döngü hemen çıkıyor.
## Doğru
C
## Açıklama
`addToCart()` üç deneme boyunca tıklayıp onay penceresinin (modal) açılmasını **kanıt** sayıyor. Ancak bir önceki ürün eklendiğinde açılan pencere kapatılmadan bu ürüne geçilmişse, döngü başında hâlâ görünür olabilir. O durumda `modal.waitFor({ state: 'visible' })` hemen "başarılı" dönebilir — oysa bu tıklama hiç işlememiş, ekranda duran **eski** ürünün penceresidir. `if (await modal.isVisible())` satırı bu tuzağı önler: döngüye girmeden önce eski pencere varsa kapatılır, böylece asıl tıklamadan sonra görünen pencere gerçekten **bu ürünün** kanıtı olur.

# flaky-4 | İleri
## Soru
`CartPage.increaseQuantity()` içinde adet artırıldıktan sonra şu poll koşulu kullanılıyor:
```js
await expect
  .poll(async () => {
    const quantity = await this.quantityOf(index).catch(() => before);
    const summary = await this.summaryText().catch(() => summaryBefore);
    return quantity > before && summary !== summaryBefore;
  }, { timeout: 25000 })
  .toBe(true);
```
Kanıt olarak yalnızca `quantityOf(index) > before` yeterli olmadığı için `summary !== summaryBefore` de eklenmiş. Bunun nedeni nedir?
## A
Adet metni sayfada hem satırda hem özet kutusunda gösteriliyor ve ikisi arasında geçici bir tutarsızlık çıkabiliyor
🤖 Sorun ikinci bir adet gösterimi değil; sorun tek bir adet metninin ne zaman güvenilir olduğu.
## B
`quantityOf` fonksiyonu bazen `NaN` döndürüyor, bu yüzden ikinci bir sayısal kontrol gerekiyor
🤖 `NaN` sorunu değil; `.catch` zaten geçici okuma hatalarını ele alıyor. Asıl sorun adet metninin anlamı.
## C
25 saniyelik zaman aşımı çok kısa kaldığı için ikinci bir koşulla poll'un daha uzun sürmesi sağlanıyor
🤖 İkinci koşul süreyi uzatmak için değil, kanıtın doğruluğunu artırmak için eklendi; zaman aşımı ayrı bir parametre.
## D
Adet metni sunucu onayından bağımsız, iyimser (optimistic) güncelleniyor; tek başına kanıt değil
🤖 Doğru: istek sunucuya hiç ulaşmasa bile adet metni "2" gösterebiliyor, yani gerçek bir aksiyonun kanıtı olmuyor; sunucunun onayladığı tek gösterge özet (summary) değişimi.
## Doğru
D
## Açıklama
Sepette adet metni **iyimser (optimistic)** güncellenir: arayüz, sunucudan cevap gelmeden isteğin başarılı olacağını varsayıp satırı hemen "2" gösterir. İstek sunucuya hiç ulaşmasa bile bu görünür değişir — bu yüzden yalnız `quantity > before` kontrolü, isteğin gerçekten işlendiğini garanti etmez. Sunucunun onayladığı tek gözlenebilir işaret, ara toplamı da içeren **özetin (summary) değişmesidir**. İki koşulu birlikte istemek (`quantity > before && summary !== summaryBefore`), iyimser güncellemeyi kanıt sanma hatasını önler; `.catch` blokları ise sayfa yeniden render olurken oluşabilecek anlık okuma hatalarını "koşul henüz sağlanmadı" olarak ele alır, poll'u kırmaz.

# flaky-5 | Orta
## Soru
Ekip, sepete ekleme ve sepetten silme akışlarında Cucumber'ın kendi yeniden deneme (retry) özelliğini bilinçli olarak **kullanmamayı** tercih etti; bunun yerine kök nedeni bulup kodu düzeltti. Bu tercihin gerekçesi nedir?
## A
Cucumber'ın retry özelliği yalnızca `@smoke` etiketli senaryolarda çalışır; bu projede o etiket kullanılmadığı için devrede değil
🤖 Retry özelliği etikete bağlı bir kısıtlama değil; tercih teknik bir zorunluluktan değil, bilinçli bir karardan kaynaklandı.
## B
Retry, başarısız senaryoyu tekrar koşturup flaky'yi yeşile çevirir ama altındaki gerçek uygulama davranışını ortaya çıkarmaz
🤖 Doğru: retry semptomu gizler; ekip bu kararsızlıkların (geç bağlanan olay dinleyicisi, iyimser güncelleme) gerçek uygulama davranışları olduğunu ancak kök nedene inerek keşfetti.
## C
Retry mekanizması Allure raporunda her deneme için ayrı bir senaryo satırı oluşturup raporu karmaşıklaştırır
🤖 Rapor okunabilirliği ikincil bir etken olsa da asıl gerekçe bu değil; asıl gerekçe kök neden analizinin kaybedilmesi.
## D
Cucumber'ın retry özelliği paralel koşumda worker'lar arası kilitlenmeye yol açtığı için devre dışı bırakıldı
🤖 Böyle bir kilitlenme belirtisi yok; tercih performans/kararlılık sorunundan değil, tasarım felsefesinden kaynaklandı.
## Doğru
B
## Açıklama
Retry, başarısız bir senaryoyu otomatik olarak tekrar koşturup genellikle testi yeşile çevirir — ama **neden** ilk seferinde başarısız olduğunu açıklamaz. Bu projede karşılaşılan kararsızlıklar (Angular olay dinleyicisinin geç bağlanması, adet metninin iyimser güncellenmesi) gerçek uygulama davranışlarıydı; retry ile üzeri örtülseydi bu davranışlar hiç keşfedilmeyecek, kod da gerçek bir kanıt beklemeyi öğrenmeyecekti. Kök nedene inmek daha yavaş ama testin **neyi doğruladığını** gerçekten anlamlı kılan yoldur; retry, CI'da nadir/dış kaynaklı dalgalanmalara karşı ek bir güvenlik ağı olarak sonradan eklenebilir, ama kök neden analizinin yerine geçmez.

# flaky-6 | İleri
## Soru
Proje, Playwright'ın `expect`'ini doğrudan kullanmak yerine `utils/expect.js` üzerinden yeniden yapılandırılmış bir sürümünü dışa aktarıyor ve tüm sayfa/step dosyaları bunu import ediyor:
```js
const expect = playwrightExpect.configure({ timeout: config.defaultTimeout });
```
Tarayıcı context'i zaten aynı `config.defaultTimeout` (20 sn) ile kuruluyor. Bu ortak `expect` katmanının gerekçesi nedir?
## A
Sunucudan dönen geçişler her zaman 20 saniyeden uzun sürdüğü için tek merkezden daha büyük bir değer verilebilsin diye
🤖 Geçiş süresi tutarlı biçimde 20 sn'yi aşmıyordu; sorun geçişin süresi değil, `expect`'in bundan bağımsız 5 sn'lik varsayılanıydı.
## B
Context'in `setDefaultTimeout` çağrısı yalnızca ilk sayfa yüklemesinde etkili oluyor; her navigasyondan sonra Playwright bu değeri varsayılana sıfırlıyor
🤖 `setDefaultTimeout` sıfırlanmıyor; sorun bir sıfırlanma hatası değil, iki ayrı zaman aşımı türünün varsayılan olarak eşit olmaması.
## C
Playwright'ın `expect` fonksiyonu ağ isteklerini kendiliğinden yeniden denediği için ekstra süre harcıyor
🤖 `expect` ağ isteğini yeniden denemez; DOM/koşulu tekrar tekrar kontrol eder. Sorun bu tekrar kontrolün süresinin kısa kalmasıydı.
## D
Aksiyonların zaman aşımı ile assertion'ların zaman aşımı Playwright'ta bağımsız ayarlardır; context'teki 20 sn yalnız aksiyonları kapsar
🤖 Doğru: Playwright'ın `expect` varsayılanı 5 sn'dir ve context'in aksiyon zaman aşımından etkilenmez; ikisi ayrı bütçelerdir, bu yüzden `expect` ayrıca yapılandırılmıştır.
## Doğru
D
## Açıklama
Playwright'ta bir **aksiyonun** (tıklama, doldurma) ne kadar bekleyeceği ile bir **assertion'ın** (`expect(locator)`) ne kadar yeniden deneneceği birbirinden bağımsız ayarlardır. Context üzerinde `setDefaultTimeout(20000)` çağrısı yalnızca aksiyonları kapsar; `expect`'in kendi varsayılanı (5 sn) bundan etkilenmez. Sonuç, sayfa hâlâ normal çalışırken, sunucudan dönen bir geçişi doğrulayan adımların aralıklı olarak zaman aşımına uğramasıydı — geçiş bazen `expect`'in 5 saniyelik varsayılanını aşıyordu, oysa aynı bekleme aksiyonlar için 20 saniyeye kadar tanınıyordu. `playwrightExpect.configure({ timeout: config.defaultTimeout })` ile projenin tüm `expect` çağrıları aksiyonlarla **aynı bütçeyi** paylaşacak şekilde yeniden tanımlandı.
