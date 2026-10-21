---
slug: playwright-vs-selenium
baslik: "Playwright mı Selenium mı? Mimari Fark, Kararlılık, Hız ve Seçim Ölçütü"
kademe: İleri
sira: 231
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

İki usta marangoz düşün. Birincisi elli yıllık klasik el aletleriyle çalışıyor. Bu aletler her atölyede bulunur ve her usta kullanmayı bilir. Parçaları herkesle uyumludur, yedeği her yerde vardır. Ama bazı işleri yapmak için ustanın deneyimi ve disiplini şarttır.

İkincisi ise son on yılda tasarlanmış yeni nesil bir takımla çalışıyor. Bu takım ölçüyü kendisi alır ve kesmeden önce tahtanın oturup oturmadığını kendi kontrol eder. Acemi elinde bile daha az hata çıkar. Ama bu takım daha az atölyede vardır ve bazı özel işlerde eski aletin yerini tutmaz. **Selenium ile Playwright arasındaki fark tam olarak budur**: biri "standart ve her yerde", diğeri "varsayılan olarak daha kararlı".

Bu konu, bir QA mülakatında **en olası sorudur** ve genelde tuzaklı sorulur: "Sence hangisi daha iyi?" Doğru cevap bir marka seçmek değil, **ölçüt kurmaktır**. Aşağıda önce farkların nereden doğduğunu göreceksin; bu mimari kısımdır. Sonra bu farkın günlük hayatta ne anlama geldiğine bakacağız: kararlılık, izolasyon, hız ve teşhis. En sonunda da iki soruyu cevaplayacağız: hangi bağlamda hangisi doğru seçimdir ve mevcut bir Selenium yatırımından nasıl kademeli geçilir?

### Temel kavramlar

- **Mimari fark — süreç dışı komut/cevap ile kalıcı çift yönlü bağlantı.** Selenium, W3C WebDriver protokolü üzerinden çalışır. Test kodu her komut için sürücüye (örneğin chromedriver'a) ayrı bir **HTTP + JSON** isteği gönderir, cevabı bekler, sonra bir sonrakini gönderir. Tarayıcı "sorulmadan" bir şey söyleyemez. Playwright ise tarayıcıyla **kalıcı, çift yönlü bir bağlantı** kurar. Chromium'da bu CDP'dir; Firefox ve WebKit için Playwright'ın kendi yamalanmış yapıları ve protokolü kullanılır. Bu kanal üzerinden tarayıcı olayları kendiliğinden bildirir: yeni sayfa açıldı, ağ isteği başladı, konsola log düştü. Playwright'ın "sihir" gibi görünen yeteneklerinin çoğu bu tek mimari farktan doğar.
- **WebDriver BiDi.** Bu, W3C'nin aradaki farkı standart tarafta kapatma çabasıdır. WebDriver'ın tek yönlü komut/cevap modeline, olay tabanlı çift yönlü bir kanal ekleyen yeni bir standarttır. Olgunlaştıkça Selenium da ağ müdahalesi ve olay dinleme gibi yetenekleri **taşınabilir** biçimde sunabilecek. Şerh: bu bir yön beyanıdır. Bugünkü olgunluk seviyesi tarayıcı ve yetenek bazında değişkendir. Mülakatta "BiDi ile fark tamamen kapandı" demek erken bir iddiadır.
- **Eyleme hazırlık kontrolleri (actionability).** Playwright bir öğeye tıklamadan önce beş şeyi **kendisi** kontrol eder ve sağlanana kadar bekler. DOM'a bağlı mı (attached)? Görünür mü (visible)? Kararlı mı (stable) — yani animasyon bitmiş ve konumu iki ardışık karede aynı mı? Etkin mi (enabled)? Olayları alıyor mu (receives events) — yani üstünü başka bir öğe örtmüyor mu? Selenium'da bu sorumluluk **test yazarındadır**. `ExpectedConditions.elementToBeClickable` görünürlüğü ve etkinliği kontrol eder. Ama öğenin başka bir öğeyle **örtülmediğini garanti etmez**. Örtülme durumu ancak tıklama denendiğinde `ElementClickInterceptedException` olarak ortaya çıkar. **Kararsız (flaky)** test oranındaki farkın en büyük tek kaynağı budur.
- **İzolasyon maliyeti — `BrowserContext` ile yeni oturum.** Playwright'ta `browser.newContext()` milisaniyeler içinde tertemiz bir oturum verir. Bu oturumun çerezi, `localStorage`'ı, `sessionStorage`'ı, IndexedDB'si ve önbelleği tamamen ayrıdır; "gizli sekme" gibi düşünebilirsiniz. Selenium'da eşdeğer temizlik için yeni bir WebDriver oturumu gerekir; yani yeni bir tarayıcı süreci veya en azından temiz bir profil. Bu, saniyeler mertebesindedir. `driver.manage().deleteAllCookies()` ise yalnız çerezi siler. `localStorage`, `sessionStorage`, IndexedDB ve service worker yerinde kalır. Yani bu **tam izolasyon değildir**.
- **Paralellik modeli.** Selenium'da paralellik test koşucusunun işidir: TestNG'de `parallel="methods"` ve `ThreadLocal`, JUnit 5'te paralel koşum. Ölçek ise Grid ile yatay büyütülür. Playwright'ın kendi koşucusu (`@playwright/test`) süreç seviyesinde bir işçi (worker) havuzu yönetir. `fullyParallel` ayarıyla dosya içindeki testleri bile dağıtır. İzolasyon ucuz olduğu için eşzamanlılığı artırmanın maliyeti düşüktür.
- **Teşhis araçları.** Playwright kutudan çıkar çıkmaz dört araç sunar. **Trace viewer**: koşumu adım adım geriye sarar; her adımda DOM anlık görüntüsü, ağ trafiği ve konsol kaydı vardır. **Video kaydı**. **`codegen`**: tarayıcıda tıkladıklarınızı koda çevirir. **`--ui` modu**: etkileşimli koşum sağlar. Selenium tarafında bunların karşılığı ayrı ayrı kurulur. Ekran görüntüsünü siz alırsınız, raporlamayı Allure veya ExtentReports yapar, video için Grid'in kayıt konteynerini açmanız gerekir, kayıt-oynat için Selenium IDE vardır. Yani yapılamaz değil — **hazır gelmez**.
- **Ağ katmanı.** Playwright'ta isteği kesmek ve taklit etmek yerleşiktir. `page.route(...)` ile bir uç noktayı sahte cevapla karşılamak tek satırdır. "Stok servisi 0 dönerse buton ne olur?" gibi bir kenar durumu, gerçek stok yaratmadan test edilir. Selenium'da bunun karşılığı üç yoldan biridir: CDP (yalnız Chromium'da ve sürüme bağlı), araya konan bir vekil sunucu (proxy) veya olgunlaştıkça BiDi.
- **iframe, shadow DOM, yeni sekme.** Playwright **seçicileri (locator)**, açık (open) shadow DOM'u varsayılan olarak deler. iframe için `frameLocator` kullanılır; bağlam değiştirmeye gerek kalmaz. Yeni sekme ise `context.waitForEvent('page')` ile bir olay olarak yakalanır. Selenium'da iframe için `driver.switchTo().frame(...)` çağrılır ve sonra `defaultContent()` ile geri dönülür; bu bağlam yönetimi unutulduğunda tipik bir hata kaynağı olur. Shadow DOM için Selenium 4'ün `getShadowRoot()` arayüzü vardır, ama CSS seçici gölge sınırını kendiliğinden geçmez. Yeni sekme ise `driver.getWindowHandles()` ile bulunup `switchTo().window(...)` ile geçilir.
- **Dil ve ekosistem.** Selenium'un dil desteği ve ekosistemi belirgin biçimde daha geniştir. Diller: Java, Python, C#, JavaScript, Ruby ve topluluk bağlayıcıları. Ayrıca **Appium** mobil otomasyonu WebDriver protokolü üzerine kuruludur. Bulut tarayıcı çiftlikleri ve kurumsal Grid altyapıları da hep bu standardı konuşur. Playwright'ın resmî bağlayıcıları ise JavaScript/TypeScript, Python, Java ve .NET içindir. Yani Java ekibi için de bir seçenek vardır. Ancak topluluk örnekleri, eklentiler ve dokümantasyon ağırlığı JS/TS tarafındadır. Son bir nokta: Selenium Grid WebDriver protokolü üzerine kuruludur. Playwright'ı bu altyapıya oturtmak standart yol değildir; bulut sağlayıcıları Playwright için ayrı uç noktalar sunar.

```svg
<svg viewBox="0 0 660 270" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Selenium ve Playwright mimari farkı: ayrı HTTP istekleri ile kalıcı çift yönlü bağlantı">
  <defs>
    <marker id="pwsel-ok" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#186399"/>
    </marker>
    <marker id="pwsel-okyesil" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Farkın kaynağı: iletişim kanalı</text>
  <rect x="14" y="42" width="310" height="212" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Selenium — W3C WebDriver</text>
  <rect x="34" y="80" width="120" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="94" y="101" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">test kodu (JVM)</text>
  <rect x="184" y="80" width="120" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="244" y="101" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sürücü + tarayıcı</text>
  <line x1="156" y1="128" x2="182" y2="128" stroke="#186399" stroke-width="1.6" marker-end="url(#pwsel-ok)"/>
  <text x="94" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">komut 1</text>
  <line x1="182" y1="148" x2="156" y2="148" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="3,3"/>
  <line x1="156" y1="168" x2="182" y2="168" stroke="#186399" stroke-width="1.6" marker-end="url(#pwsel-ok)"/>
  <text x="94" y="172" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">komut 2</text>
  <line x1="182" y1="188" x2="156" y2="188" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="3,3"/>
  <text x="244" y="140" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">her komut ayrı</text>
  <text x="244" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">HTTP + JSON isteği</text>
  <text x="244" y="180" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">tarayıcı sorulmadan</text>
  <text x="244" y="194" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bir şey söyleyemez</text>
  <text x="169" y="220" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bekleme sorumluluğu test yazarında</text>
  <text x="169" y="240" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kazanç: standart, her dil, Grid, Appium</text>
  <rect x="336" y="42" width="310" height="212" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Playwright — kalıcı çift yönlü kanal</text>
  <rect x="356" y="80" width="120" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="416" y="101" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">test kodu</text>
  <rect x="506" y="80" width="120" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="566" y="101" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tarayıcı</text>
  <line x1="478" y1="134" x2="504" y2="134" stroke="#25804C" stroke-width="2.2" marker-end="url(#pwsel-okyesil)"/>
  <line x1="504" y1="154" x2="478" y2="154" stroke="#25804C" stroke-width="2.2" marker-end="url(#pwsel-okyesil)"/>
  <text x="416" y="138" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">komutlar</text>
  <text x="416" y="158" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">olaylar (event)</text>
  <text x="566" y="136" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tek kalıcı bağlantı</text>
  <text x="566" y="156" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">tarayıcı kendiliğinden</text>
  <text x="566" y="170" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">haber verir</text>
  <text x="491" y="196" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni sayfa, ağ isteği, konsol → olay olarak yakalanır</text>
  <text x="491" y="220" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bekleme sorumluluğu araçta (actionability)</text>
  <text x="491" y="240" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">standart tarafta karşılığı: WebDriver BiDi</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

**1) Aynı akış, iki araç.** "Ürün sayfasına git, sepete ekle, rozet 1 olsun" senaryosunu yan yana koyalım. Selenium (Java):

```java
WebDriverWait bekle = new WebDriverWait(driver, Duration.ofSeconds(10));
driver.get("https://magaza.example.com/urun/42");

bekle.until(ExpectedConditions.elementToBeClickable(
    By.cssSelector("[data-test='sepete-ekle']"))).click();

bekle.until(ExpectedConditions.textToBePresentInElementLocated(
    By.cssSelector("[data-test='sepet-rozet']"), "1"));
```

Playwright (JavaScript):

```js
await page.goto('https://magaza.example.com/urun/42');
await page.getByTestId('sepete-ekle').click();
await expect(page.getByTestId('sepet-rozet')).toHaveText('1');
```

Fark satır sayısı değil, **beklemenin nerede durduğudur**. Selenium sürümünde `elementToBeClickable` ve `textToBePresentInElementLocated` çağrılarını *sen* yazdın. Unutsaydın test yavaş bir ağda kırılırdı. Playwright sürümünde ise `click()` hazırlık kontrollerini kendisi yapar. `expect(...).toHaveText(...)` de koşul sağlanana kadar kendini tekrarlar. Aynı disiplin Selenium'da da kurulabilir; ama **sen kurarsın**, araç seni zorlamaz. Ekip disiplinli değilse fark burada patlar.

**2) İzolasyon.** Playwright'ta temiz oturum ucuzdur:

```js
const context = await browser.newContext();   // ms mertebesinde, tamamen temiz
const page = await context.newPage();
// ... senaryo ...
await context.close();
```

Selenium'da eşdeğeri yeni bir oturumdur:

```java
driver.quit();                              // saniyeler mertebesinde
driver = new ChromeDriver(secenekler);      // yeni, temiz profil
```

`driver.manage().deleteAllCookies()` bir kestirme gibi görünür ama yalnız çerezi siler. `localStorage`, `sessionStorage`, IndexedDB ve service worker yerinde kalır. Yani "temiz oturum" sandığın şey aslında yarı temizdir. Testler arası sızıntı da tam buradan sızar.

**3) Ağ katmanı.** "Stok servisi 0 dönerse Sepete Ekle butonu pasifleşiyor mu?" sorusunu Playwright gerçek stok yaratmadan test eder:

```js
await page.route('**/api/stok/*', route =>
  route.fulfill({ status: 200, body: JSON.stringify({ adet: 0 }) }));

await page.goto('/urun/42');
await expect(page.getByTestId('sepete-ekle')).toBeDisabled();
```

Selenium'da bunun üç karşılığı vardır: CDP (yalnız Chromium'da ve tarayıcı sürümüne bağlı), araya bir vekil sunucu koymak, ya da BiDi olgunlaştıkça standart yol. Yapılamaz değil; ama **maliyetlidir ve taşınabilir değildir**.

**4) Teşhis.** Bir test CI'da kırıldı; elinde ne var?

```
npx playwright show-trace trace.zip     # adım adım geriye sar: DOM + ağ + konsol
npx playwright codegen https://magaza.example.com   # tıkla, kod çıksın
npx playwright test --ui                # etkileşimli koşum, adım adım izle
```

Selenium tarafında bunların hepsi kurulabilir, ama ayrı ayrı. Ekran görüntüsünü `TakesScreenshot` ile sen alırsın. Raporu Allure veya ExtentReports üretir. Videoyu Grid'in kayıt konteyneri kaydeder. Kayıt-oynat için Selenium IDE vardır. Yani fark "mümkün mü" sorusu değil, **ne kadar emek** sorusudur.

```svg
<svg viewBox="0 0 660 280" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Eyleme hazırlık kontrolleri: Playwright otomatik, Selenium test yazarının sorumluluğunda">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tıklamadan önce kim kontrol ediyor?</text>
  <rect x="14" y="42" width="310" height="224" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Playwright — araç yapar</text>
  <text x="34" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ DOM'a bağlı (attached)</text>
  <text x="34" y="114" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ görünür (visible)</text>
  <text x="34" y="136" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ kararlı (animasyon bitti, konum sabit)</text>
  <text x="34" y="158" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ etkin (enabled)</text>
  <text x="34" y="180" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ olay alıyor (üstü örtülmemiş)</text>
  <text x="169" y="212" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hepsi sağlanana kadar bekler, sonra tıklar</text>
  <text x="169" y="234" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ varsayılan davranış kararlı</text>
  <text x="169" y="254" text-anchor="middle" font-size="10" fill="#B07D20" font-family="system-ui">yine de: "tıklandı" ≠ "etki oldu"</text>
  <rect x="336" y="42" width="310" height="224" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="491" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Selenium — test yazarı yapar</text>
  <text x="356" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ visibilityOfElementLocated (elle)</text>
  <text x="356" y="114" font-size="10.5" fill="#1C3247" font-family="system-ui">✓ elementToBeClickable (elle)</text>
  <text x="356" y="136" font-size="10.5" fill="#B23B3B" font-family="system-ui">✗ kararlılık kontrolü yok</text>
  <text x="356" y="158" font-size="10.5" fill="#B23B3B" font-family="system-ui">✗ örtülme kontrolü yok</text>
  <text x="356" y="180" font-size="10" fill="#5D7789" font-family="system-ui">örtülme ancak tıklayınca anlaşılır:</text>
  <text x="356" y="196" font-size="10" fill="#5D7789" font-family="system-ui">ElementClickInterceptedException</text>
  <text x="491" y="228" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">disiplin varsa aynı kararlılık kurulabilir</text>
  <text x="491" y="250" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">→ disiplin yoksa flaky oranı yükselir</text>
</svg>
```

**5) Hangisi ne zaman doğru seçim?** Mülakatın asıl sorusu budur; cevabı ölçütle verilir.

**Selenium hâlâ doğru seçimdir**, çünkü:

1. **Mevcut yatırım ve yetkinlik.** Yüzlerce testlik olgun bir paketiniz, çalışan Page Object'leriniz ve aracı bilen bir ekibiniz olabilir. Bu durumda göçün maliyeti, elde edilecek kararlılık kazancını aşabilir. Göç maliyeti şunlardan oluşur: yeniden yazma, öğrenme ve iki aracın birlikte kullanıldığı geçiş dönemi.
2. **Kurumsal Grid / tarayıcı çiftliği.** Kurulmuş, izlenen ve güvenlik onayı almış bir Grid altyapınız varsa Selenium'da kalmak mantıklıdır; çünkü Playwright bu altyapıyı doğrudan kullanamaz.
3. **Geniş tarayıcı ve sürüm matrisi.** Belirli Safari/Edge sürümleri veya eski kurumsal tarayıcılar hedefteyse W3C WebDriver bu matriste daha kapsayıcıdır. Playwright, Chromium/Firefox/WebKit'in kendi indirdiği yapılarıyla çalışır. "Gerçek Safari 16.4" gibi bir hedefte bu bir sınırdır.
4. **Mobil ile ortak zemin.** Appium, WebDriver protokolü üzerine kuruludur. Web ve mobil otomasyonu tek bir zihinsel modelde tutmak isteyen ekipler için Selenium doğal taraftır.
5. **Katı araç onay süreçleri.** Bankacılık, kamu ve sağlık gibi alanlarda yeni bir aracın güvenlik ve lisans onayı aylar sürebilir. "Standart" olmak burada teknik bir özellikten fazlasıdır.

**Playwright doğru seçimdir**, çünkü:

1. **Sıfırdan başlayan proje.** Taşınacak yatırım yoksa, varsayılan olarak daha kararlı olanı seçmek rasyoneldir.
2. **Kararsız (flaky) testlerle boğuşan ekip.** Kararsızlığın kaynağı çoğunlukla bekleme disiplini eksikliğidir. Eyleme hazırlık kontrolleri, bu sınıf hataların büyük bölümünü kaynağında keser.
3. **Hızlı geri bildirim ihtiyacı.** Ucuz izolasyon ve yerleşik paralellik bir araya gelince, her pull request'te koşabilen bir paket mümkün olur.
4. **Güçlü teşhis ihtiyacı.** CI'da kırılan bir testi trace viewer ile geriye sarabilmek, "bende çalışıyordu" tartışmasını bitirir.
5. **Ağ seviyesinde kenar durum testi.** Stok tükendi, ödeme servisi 500 döndü, kampanya API'si yavaş... Bu durumları gerçek veri yaratmadan `route` ile üretmek, Playwright'ta yerleşik bir yetenektir.

**6) Göç (migration) stratejisi.** Doğru cevap "hepsini bir hafta sonunda çeviririz" değildir. Kademeli yol:

1. **Yeni testleri Playwright'la yaz**, mevcut Selenium paketini yerinde bırak. Regresyon güvencesi ilk günden kaybolmaz.
2. **İki koşucuyu CI'da bir arada yaşat**: ayrı işler (job), ayrı raporlar, ortak bir özet. Birinin kırmızısı diğerini engellemesin.
3. **Alan bilgisini taşı, kodu değil.** Page Object'lerdeki asıl değer seçiciler ve iş akışı bilgisidir. Bunlar araçtan bağımsızdır ve doğrudan taşınabilir. Bekleme kodunun büyük kısmı ise Playwright'ta gereksiz kalır; taşınmaz, silinir.
4. **Önce en çok kırılan senaryoları çevir.** Göçün faydası en hızlı orada görünür. Ekibi ikna eden şey slayt değil, düşen kararsızlık oranıdır.
5. **Çıkış ölçütü belirle.** Örnek: "Selenium paketi altı ay içinde yalnız X modülünde kalacak." Ölçütsüz göç, kalıcı bir ikili bakım yüküne dönüşür. Bu, göçün en sık görülen başarısızlık biçimidir.

**7) Mülakat cevabı kalıbı.** "Hangisi daha iyi?" sorusuna bağlam sormadan cevap vermek tuzaktır. "Playwright her açıdan üstün" demek araç fanatizmi olarak okunur. "İkisi aynı şey" demek ise yüzeysellik olarak okunur. Kullanılabilir kalıp şudur:

> "Bağlama bağlı. Sıfırdan bir proje kuruyorsak Playwright'ı tercih ederim. Eyleme hazırlık kontrolleri ve ucuz `BrowserContext` izolasyonu, en çok vakit kaybettiğimiz kararsız test sınıfını kaynağında kesiyor. Trace viewer da teşhis süresini kısaltıyor. Ama sizde olgun bir Selenium paketi, kurulu bir Grid ve Appium ile ortak bir mobil zemin varsa göç maliyeti kazancı aşabilir. O durumda Selenium'da kalırım; kararlılığı seçici ve bekleme disipliniyle çözerim. Yeni modüllerde de Playwright'ı pilot olarak denerim. Sizin tarafta durum nedir — mevcut paketin büyüklüğü ve tarayıcı matrisi ne?"

Bu cevap üç şeyi aynı anda gösterir: ölçüt kurabildiğini, iki aracı da sınırlarıyla bildiğini ve karar verirken bağlam topladığını.

```svg
<svg viewBox="0 0 660 290" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Seçim ölçütü: hangi bağlamda Selenium, hangi bağlamda Playwright">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Seçim ölçütü — marka değil bağlam</text>
  <rect x="14" y="40" width="632" height="30" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Soru: mevcut yatırım ne kadar? tarayıcı matrisi ne? ekip neyi biliyor? altyapı hazır mı?</text>
  <rect x="14" y="84" width="310" height="186" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="108" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Selenium'da kal</text>
  <text x="34" y="132" font-size="10.5" fill="#1C3247" font-family="system-ui">• yüzlerce testlik olgun paket + yetkin ekip</text>
  <text x="34" y="154" font-size="10.5" fill="#1C3247" font-family="system-ui">• kurulu ve onaylı kurumsal Grid</text>
  <text x="34" y="176" font-size="10.5" fill="#1C3247" font-family="system-ui">• geniş tarayıcı / sürüm matrisi</text>
  <text x="34" y="198" font-size="10.5" fill="#1C3247" font-family="system-ui">• Appium ile ortak mobil zemin</text>
  <text x="34" y="220" font-size="10.5" fill="#1C3247" font-family="system-ui">• katı araç onay süreci</text>
  <text x="169" y="250" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kararlılık: locator + açık bekleme disiplini</text>
  <rect x="336" y="84" width="310" height="186" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="108" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Playwright'ı seç</text>
  <text x="356" y="132" font-size="10.5" fill="#1C3247" font-family="system-ui">• sıfırdan başlayan proje</text>
  <text x="356" y="154" font-size="10.5" fill="#1C3247" font-family="system-ui">• flaky ile boğuşan ekip</text>
  <text x="356" y="176" font-size="10.5" fill="#1C3247" font-family="system-ui">• PR başına hızlı geri bildirim</text>
  <text x="356" y="198" font-size="10.5" fill="#1C3247" font-family="system-ui">• güçlü teşhis (trace, video, codegen)</text>
  <text x="356" y="220" font-size="10.5" fill="#1C3247" font-family="system-ui">• ağ taklidi ile kenar durum testi</text>
  <text x="491" y="250" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">göç: yeni testler yeni araçla, eskisi yerinde</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Playwright mı Selenium mı, hangisi daha iyi?"** Beklenen: önce bağlam sormak, sonra ölçütle cevap vermek. Ölçütler: mevcut yatırım, tarayıcı matrisi, ekip yetkinliği, altyapı ve teşhis ihtiyacı. **Elenme sebebi:** bağlam sormadan "Playwright her açıdan daha iyi" veya "Selenium standart, gerisi hikâye" demek.
- **"Mimari farkı anlat."** Beklenen: Selenium W3C WebDriver üzerinden çalışır; süreç dışıdır ve her komut için ayrı bir HTTP+JSON isteği gider. Playwright ise tarayıcıyla kalıcı çift yönlü bir kanal kurar ve olayları kendiliğinden alır. Standart tarafta bu farkı kapatma çabası **WebDriver BiDi**'dir. Bu son cümle mülakatta belirgin fark yaratır.
- **"Playwright neden daha az kararsız (flaky)?"** Beklenen: eyleme hazırlık (actionability) kontrolleri her eylemden önce araç tarafından yapılır. Bu kontroller şunlardır: attached, visible, stable, enabled, receives events. Selenium'da aynı disiplin **elle** kurulur ve özellikle örtülme kontrolü `ExpectedConditions` ile yapılamaz. **Tuzak:** "Playwright kararsızlığı tamamen bitirir" demek. Otomatik bekleme "tıklanabilir mi?" sorusuna cevap verir, "tıklama etki etti mi?" sorusuna değil.
- **"İzolasyon farkı nedir?"** Beklenen: `BrowserContext` milisaniyede tam temiz bir oturum verir. Selenium'da eşdeğeri yeni bir oturum veya profildir ve saniyeler sürer. **Tuzak:** `deleteAllCookies()`'i tam temizlik sanmak. `localStorage`, `sessionStorage`, IndexedDB ve service worker yerinde kalır.
- **"Selenium'da yapılamayan ne var?"** Beklenen: "yapılamaz" değil, "hazır gelmez" çerçevesi. Ağ taklidi, trace viewer, codegen ve video — hepsinin Selenium karşılığı vardır. Ama bunlar ayrı parçalarla kurulur: CDP, vekil sunucu, Grid kayıt konteyneri, Allure gibi. Ayrıca CDP çözümü Chromium'a bağlıdır.
- **"Mevcut Selenium paketini nasıl göç ettirirsin?"** Beklenen cevap kademelidir. Yeni testler Playwright'la yazılır, eski paket yerinde kalır. CI'da iki koşucu ayrı iş olarak yaşar. Page Object'lerdeki seçici ve akış bilgisi taşınır, bekleme kodu silinir. Önce en çok kırılan senaryolar çevrilir. Son olarak bir **çıkış ölçütü** tanımlanır. **Tuzak:** "otomatik dönüştürücüyle hepsini çeviririz" demek.
- **"Playwright yalnız JavaScript'te mi var?"** Beklenen: hayır. Resmî bağlayıcıları JS/TS, Python, Java ve .NET içindir. Ama topluluk örneklerinin ve eklentilerin ağırlığı JS/TS'tedir. Selenium'un dil ve araç ekosistemi hâlâ daha geniştir.
- **"iframe / shadow DOM / yeni sekme farkı?"** Beklenen: Playwright açık shadow DOM'u varsayılan olarak deler; iframe için `frameLocator`, yeni sekme için `waitForEvent('page')` kullanır. Selenium'da ise üç ayrı yol gerekir: `switchTo().frame()` ve `defaultContent()` ile bağlam yönetmek, `getShadowRoot()` ile gölge kökü almak, `getWindowHandles()` ile pencereye geçmek.
- **"Grid'i Playwright ile kullanabilir misin?"** Beklenen: Selenium Grid, WebDriver protokolü üzerine kuruludur. Playwright'ı bu altyapıya oturtmak standart yol değildir. Playwright kendi işçi (worker) modeliyle paralelleşir; uzak koşum için bulut sağlayıcıların Playwright uç noktaları kullanılır. Bu, mevcut Grid yatırımı olan kurumlarda göçün gerçek maliyet kalemidir.

### Tek cümlede

Playwright ile Selenium arasındaki tüm somut farklar tek bir mimari ayrımdan doğar: süreç dışı komut/cevap ile kalıcı çift yönlü kanal. Bu ayrım Playwright'a otomatik hazırlık kontrolleri, ucuz izolasyon, yerleşik ağ taklidi ve trace gibi teşhis araçları kazandırır. Selenium'a ise standartlık, en geniş dil/tarayıcı/altyapı desteği ve Appium ile ortak zemin bırakır. Doğru cevap "hangisi iyi" değil, **"senin bağlamında hangisinin maliyet-fayda dengesi tutuyor"** sorusudur.

# pwsel-1 | Orta
## Soru
Playwright ile Selenium arasındaki farkların çoğunun kaynağı olan temel mimari ayrım nedir?
## A
İkisi de tarayıcıyı aynı W3C WebDriver protokolü üzerinden sürer; fark yalnızca API adlandırması ve sözdizimi tercihleridir
🤖 Playwright W3C WebDriver protokolünü kullanmaz; kendi kalıcı bağlantısı üzerinden konuşur, fark sözdiziminden çok daha derindir.
## B
Selenium tarayıcıya JavaScript enjekte ederek çalışır, Playwright ise tarayıcı sürücüsü üzerinden komut gönderir
🤖 JavaScript enjeksiyonu 2011'de emekliye ayrılan Selenium RC'nin modeliydi; ayrıca sürücü kullanan taraf Selenium'dur.
## C
Playwright tarayıcı sürecinin içinde (in-process) çalıştığı için hiçbir iletişim kanalına ihtiyaç duymaz
🤖 Playwright da tarayıcının dışında çalışır; farkı süreç içi olması değil, kanalın kalıcı ve çift yönlü olmasıdır.
## D
Selenium her komutu ayrı bir HTTP isteğiyle sürücüye gönderip cevabını bekler; Playwright tarayıcıyla kalıcı, çift yönlü bir bağlantı üzerinden konuşur ve tarayıcıdan olay alabilir
🤖 Doğru: komut/cevap modeli ile olay tabanlı kalıcı kanal ayrımı, sonraki bütün farkların (otomatik bekleme, ağ taklidi, hız) kaynağıdır.
## Doğru
D
## Açıklama
Selenium, W3C WebDriver standardını uygular: test kodu her komut için sürücüye ayrı bir HTTP + JSON isteği gönderir, cevabını bekler, sonrakini gönderir. Bu tek yönlü komut/cevap modelinde tarayıcı "sorulmadan" bir şey bildiremez — ne bir ağ isteğinin başladığını, ne yeni bir sekmenin açıldığını. Playwright ise tarayıcıyla kalıcı ve çift yönlü bir bağlantı kurar (Chromium'da CDP, Firefox ve WebKit için kendi protokolü ve yamalanmış yapıları); tarayıcı olayları kendiliğinden bildirir. Bu ayrım, sonraki bütün somut farkların kaynağıdır: otomatik hazırlık kontrolleri, yerleşik ağ taklidi, ucuz izolasyon ve trace. Selenium'un sayfaya JavaScript enjekte ettiği iddiası tarihte kalan Selenium RC'ye aittir; Playwright'ın süreç içi çalıştığı iddiası ise yanlıştır — o da tarayıcının dışındadır, farkı kanalın niteliğidir. W3C bu boşluğu **WebDriver BiDi** ile standart tarafta kapatmaya çalışıyor.

# pwsel-2 | Orta
## Soru
Aynı e-ticaret senaryosu Selenium'da yazıldığında Playwright'a göre belirgin biçimde daha fazla kararsız (flaky) davranıyor. Bu farkın en büyük tek teknik kaynağı nedir?
## A
Playwright her eylemden önce elementin görünür, kararlı, etkin ve üstünün örtülmemiş olduğunu kendisi kontrol edip bekler; Selenium'da bu sorumluluk açık beklemeyle test yazarındadır
🤖 Doğru: eyleme hazırlık (actionability) kontrollerinin araçta mı yoksa test yazarında mı olduğu, flaky oranındaki farkın en büyük tek kaynağıdır.
## B
Selenium'un örtük beklemesi (implicit wait) Playwright'ın hazırlık kontrolleriyle aynı işi yapar; aradaki tek fark varsayılan süre değerleridir
🤖 Örtük bekleme yalnız "element bulundu mu" sorusunu bekler; görünürlük, kararlılık veya örtülme gibi hazırlık koşullarını hiç kontrol etmez.
## C
Playwright'ın otomatik beklemesi flaky testi tamamen ortadan kaldırdığı için kanıt seçimi gibi konular Playwright'ta gündeme gelmez
🤖 Otomatik bekleme "elemente dokunulabilir mi" sorusunu çözer, "dokunmanın etkisi oldu mu" sorusunu değil; kanıt seçimi Playwright'ta da gereklidir.
## D
Selenium'da `ExpectedConditions.elementToBeClickable`, elementin bir başka elementle örtülmediğini de garanti eder; fark yalnızca yazım zahmetidir
🤖 Bu koşul görünürlük ve etkinliği kontrol eder, örtülmeyi değil; örtülme ancak tıklama denendiğinde `ElementClickInterceptedException` olarak ortaya çıkar.
## Doğru
A
## Açıklama
Playwright her eylemden önce bir kontrol listesi işletir: element DOM'a bağlı mı, görünür mü, kararlı mı (animasyon bitti, konumu iki ardışık karede aynı), etkin mi ve olayları alıyor mu (üstünü başka bir element örtmüyor). Bu koşullar sağlanana kadar bekler, sonra tıklar. Selenium'da böyle bir varsayılan yoktur; aynı güvenceyi `WebDriverWait` ve `ExpectedConditions` ile **test yazarı** kurar ve en kritik koşullardan ikisi — kararlılık ve örtülme — standart koşullar arasında hiç yoktur. `elementToBeClickable` yalnız görünürlük ve etkinliğe bakar; üstteki bir çerez banner'ı ancak tıklama denendiğinde `ElementClickInterceptedException` olarak ortaya çıkar. Örtük bekleme de bu boşluğu doldurmaz, çünkü yalnız "element bulundu mu" sorusuyla ilgilenir. Buna karşılık Playwright'ın otomatik beklemesi de sihir değildir: "elemente dokunulabilir mi" sorusunu çözer, "dokunma bir şey yaptı mı" sorusunu çözmez — bu yüzden kanıt seçimi her iki araçta da test yazarının işidir.

# pwsel-3 | İleri
## Soru
60 senaryoluk bir pakette her senaryonun tamamen temiz bir oturumla (çerez, `localStorage`, `sessionStorage`, IndexedDB) başlaması isteniyor. İki araçtaki izolasyon maliyeti açısından hangisi doğrudur?
## A
Selenium'da `driver.manage().deleteAllCookies()` çağrısı tam izolasyon sağlar; yeni bir oturum açmaya hiç gerek yoktur
🤖 Bu çağrı yalnız çerezleri siler; `localStorage`, `sessionStorage`, IndexedDB ve service worker olduğu gibi kalır, yani izolasyon yarım kalır.
## B
Playwright'ta her senaryoya yeni bir `BrowserContext` vermek milisaniyeler sürer; Selenium'da eşdeğer temizlik yeni bir WebDriver oturumu/profili gerektirir ve saniyeler mertebesindedir
🤖 Doğru: izolasyonun ucuz mu pahalı mı olduğu, "her senaryo temiz başlasın" kuralının uygulanabilirliğini doğrudan belirler.
## C
Playwright'ta aynı `Browser` altındaki context'ler çerezleri paylaşır; gerçek izolasyon ancak her senaryoda yeni bir `Browser` başlatmakla mümkündür
🤖 Context'ler tam da izolasyon için vardır: her biri kendi çerez, depolama ve önbellek alanına sahiptir, süreci paylaşmaları bunu değiştirmez.
## D
Her iki araçta da tam izolasyon yalnızca ayrı bir işletim sistemi kullanıcısı veya konteyner başına bir senaryo çalıştırarak sağlanabilir
🤖 Bu aşırı maliyetli bir yol; Playwright context'i ve Selenium'un temiz profilli yeni oturumu bu güvenceyi çok daha ucuza verir.
## Doğru
B
## Açıklama
Playwright'ta `browser.newContext()` aynı tarayıcı süreci içinde, kendi çerezi, `localStorage`'ı, `sessionStorage`'ı, IndexedDB'si ve önbelleği olan "gizli sekme" benzeri bir oturum açar; maliyeti milisaniyeler mertebesindedir ve senaryo bitince `context.close()` ile temizlenir. Selenium'da bunun eşdeğeri yeni bir WebDriver oturumudur — yani yeni bir tarayıcı süreci veya en azından temiz bir profil — ve bu saniyeler mertebesinde bir maliyettir; 60 senaryoda toplamda dakikalara çıkar. `deleteAllCookies()` cazip bir kestirmedir ama yalnız çerezleri siler: oturum bilgisi `localStorage`'da tutuluyorsa veya bir service worker kayıtlıysa sızıntı devam eder ve "izole sanılan" testler birbirini etkiler. Context'lerin çerez paylaştığı iddiası yanlıştır — izolasyon context'in varlık sebebidir. Kullanıcı/konteyner başına bir senaryo yaklaşımı ise teknik olarak izolasyon verir ama maliyeti bu iki seçeneğin çok üstündedir.

# pwsel-4 | İleri
## Soru
Mülakatta "sence Playwright mı Selenium mı daha iyi?" diye soruluyor. Aşağıdaki yaklaşımlardan hangisi bu soruya verilecek en güçlü cevaptır?
## A
"Playwright her ölçütte üstündür" deyip otomatik bekleme ve trace viewer örnekleriyle konuyu kapatmak
🤖 Bağlam sorulmadan verilen mutlak yargı araç fanatizmi olarak okunur; mevcut yatırım ve altyapı gibi kritik ölçütleri hiç hesaba katmaz.
## B
"İkisi de tarayıcı otomasyonu yapar, pratikte aralarında anlamlı bir fark yoktur" diyerek tarafsız kalmak
🤖 Mimari, izolasyon maliyeti, hazırlık kontrolleri ve ekosistem farkları somut ve ölçülebilirdir; "fark yok" demek yüzeysellik olarak okunur.
## C
Bağlamı sorup ölçütle cevap vermek: mevcut yatırım, tarayıcı matrisi, kurulu altyapı ve flaky yükü üzerinden karşılaştırmak
🤖 Doğru: soru bir marka tercihi değil karar verme yöntemi sınavıdır; ölçüt kurup bağlam toplamak beklenen cevaptır.
## D
"Selenium W3C standardıdır, Playwright standart dışı olduğu için kurumsal projelerde kullanılamaz" demek
🤖 Playwright kurumsal projelerde yaygın biçimde kullanılıyor; "standart değil" olmak bir yasak değil, taşınabilirlik ve altyapı uyumu açısından bir ölçüttür.
## Doğru
C
## Açıklama
Bu soru bir araç bilgisi sorusu gibi görünür ama aslında **karar verme yöntemini** ölçer. Bağlam sormadan verilen mutlak yargılar iki yönde de zayıftır: "Playwright her açıdan üstün" demek mevcut yatırımı, kurulu Grid altyapısını, tarayıcı matrisini ve göç maliyetini yok saymaktır; "ikisi aynı" demek ise mimari, izolasyon maliyeti ve hazırlık kontrolleri gibi somut farkları görmediğini gösterir. "Playwright standart değil, kurumsalda kullanılamaz" iddiası da gerçeğe aykırıdır — standartlık bir yasak değil, taşınabilirlik ve mevcut altyapıyla uyum açısından bir ölçüttür. Güçlü cevap ölçütleri sıralar (mevcut paketin büyüklüğü ve ekip yetkinliği, tarayıcı/sürüm matrisi, kurulu Grid ve Appium zemini, teşhis ihtiyacı, mevcut flaky yükü), her ölçütte hangi aracın öne çıktığını söyler ve karşı tarafa bağlam sorusu yönelterek biter.

# pwsel-5 | İleri
## Soru
Olgun bir Selenium paketi (yaklaşık 300 test) olan bir ekip Playwright'a geçmeye karar verdi. Hangi göç stratejisi en sağlıklısıdır?
## A
Tüm paketi bir otomatik dönüştürücüyle tek seferde Playwright koduna çevirmek ve Selenium testlerini aynı gün silmek
🤖 Otomatik dönüşüm bekleme mantığını ve bağlam yönetimini doğru taşıyamaz; tek seferde geçiş, regresyon güvencesini bir anda ortadan kaldırır.
## B
Selenium testlerini hemen silip Playwright paketi bitene kadar regresyon koşumunu tamamen durdurmak
🤖 Regresyon güvencesini göç boyunca kaybetmek kabul edilemez bir risktir; göçün amacı kaliteyi artırmakken tam tersi olur.
## C
İki aracı aynı test sınıfı içinde birlikte kullanıp aynı tarayıcı oturumunu paylaştırarak kademeli geçiş yapmak
🤖 İki araç aynı oturumu güvenilir biçimde paylaşamaz; bu kurulum kırılgan olur ve göçü kolaylaştırmak yerine yeni bir hata kaynağı yaratır.
## D
Yeni testleri Playwright'la yazıp mevcut paketi yerinde bırakmak, iki koşucuyu CI'da bir arada yaşatmak ve bir çıkış ölçütü tanımlamak
🤖 Doğru: kademeli geçiş regresyon güvencesini korur, faydayı erken görünür kılar ve çıkış ölçütü kalıcı ikili bakım yükünü engeller.
## Doğru
D
## Açıklama
Büyük bir paketi tek seferde çevirmek üç ayrı riski aynı anda üstlenmek demektir: regresyon güvencesinin kaybı, yeni araçta deneyimsizlik ve dönüştürülmüş kodun sessiz hataları. Otomatik dönüştürücüler sözdizimini çevirebilir ama Selenium'daki açık bekleme mantığını, `switchTo()` bağlam yönetimini ve retry kalıplarını Playwright'ın modeline doğru biçimde taşıyamaz — çıkan kod genelde hem Playwright'ın avantajını kullanmaz hem de eski varsayımları taşır. İki aracın aynı tarayıcı oturumunu paylaşması da güvenilir bir kurulum değildir. Sağlıklı yol kademelidir: yeni testler yeni araçla yazılır, mevcut paket yerinde kalır ve regresyon hiç durmaz; CI'da iki koşucu ayrı iş olarak yaşar, birinin kırmızısı diğerini engellemez; Page Object'lerdeki asıl değer olan locator'lar ve iş akışı bilgisi taşınır, gereksizleşen bekleme kodu ise taşınmaz. Önce en çok kırılan senaryoları çevirmek faydayı erken görünür kılar. Son olarak bir **çıkış ölçütü** (hangi tarihte hangi kapsam kalacak) tanımlanmazsa göç, kalıcı ikili bakım yüküne dönüşür — bu, göçün en sık görülen başarısızlık biçimidir.

# pwsel-6 | Uzman
## Soru
Kurumsal bir perakende şirketinin QA ekibi: altı farklı dilde yazılmış test paketleri, şirket içinde kurulu ve güvenlik onayı almış bir Selenium Grid çiftliği, Appium ile paylaşılan mobil otomasyon zemini, hedef tarayıcı matrisinde belirli eski Safari ve Edge sürümleri, ve yeni araçlar için aylar süren bir onay süreci var. Ekip flaky test oranından şikâyetçi. En savunulabilir teknik karar hangisidir?
## A
Selenium'da kalmak ve flaky sorununu ayrı bir problem olarak ele almak: locator disiplini, açık bekleme kalıpları ve kanıt seçimi; Playwright yeni bir modülde pilot olarak denenebilir
🤖 Doğru: bu bağlamda göç maliyeti (altyapı, dil çeşitliliği, onay süreci, tarayıcı matrisi) kararlılık kazancını aşar; flaky'nin kökü çoğunlukla araç değil disiplindir.
## B
Playwright'a geçmek her koşulda daha az flaky sonuç verir; Grid, Appium ve onay süreci gibi altyapı kalemleri karar verirken göz ardı edilebilir
🤖 Kararlılık kazancı gerçek olsa da altyapı, dil çeşitliliği ve onay süreci bu ölçekte kararın belirleyici kalemleridir; göz ardı edilemez.
## C
Playwright yalnızca JavaScript ile kullanılabildiği için bu ekibe hiçbir seçenek sunmaz; konuyu tamamen kapatmak gerekir
🤖 Playwright'ın resmî bağlayıcıları JS/TS, Python, Java ve .NET içindir; "yalnız JavaScript" iddiası yanlıştır.
## D
Tarayıcı matrisi ne olursa olsun iki araç da aynı tarayıcı ve sürümleri desteklediğinden matris karara etki etmez; seçim tamamen ekip zevkine kalmıştır
🤖 Matris fark yaratır: Playwright kendi indirdiği Chromium/Firefox/WebKit yapılarıyla çalışır, belirli kurumsal tarayıcı sürümleri hedefteyse bu bir sınırdır.
## Doğru
A
## Açıklama
Bu bağlamda göçün maliyeti olağandışı yüksektir: altı dilde yazılmış paketler yeniden yazılmalı, güvenlik onayı almış ve işletilen Grid çiftliği Playwright tarafından doğrudan kullanılamaz, Appium ile paylaşılan WebDriver zemini kaybolur, hedeflenen eski Safari/Edge sürümleri Playwright'ın kendi tarayıcı yapılarıyla tam karşılanmayabilir ve yeni araç onayı aylar sürer. Buna karşılık flaky sorunu **araç değiştirmeden de** ciddi biçimde azaltılabilir: kırılgan XPath yerine kararlı `data-test` locator'ları, `Thread.sleep` yasağı ve tutarlı açık bekleme kalıpları, örtük beklemenin tamamen kaldırılması, bayat referans yerine locator saklama ve en önemlisi doğru kanıt seçimi (butonun görünmesi değil, sunucunun onayladığı sonucun gerçekleşmesi). Playwright'ın "yalnız JavaScript" olduğu iddiası yanlıştır (Java, Python ve .NET bağlayıcıları vardır) ve tarayıcı matrisinin karara etki etmediği iddiası da yanlıştır. Olgun cevap, aracı tamamen reddetmek yerine sınırlı bir pilot önerir: yeni ve bağımsız bir modülde Playwright denenir, ölçülebilir bir kararlılık/süre kazancı gösterilirse onay süreci o kanıtla başlatılır.
