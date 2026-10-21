---
slug: selenium-webdriver-temelleri
baslik: "Selenium WebDriver Temelleri: W3C Protokolü, Bekleme Stratejisi ve Grid"
kademe: Orta
sira: 230
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir mağazanın deposunu telsizle yöneten bir sorumlu düşün: kendisi depoda değil, ofiste oturuyor. "Üçüncü rafın ikinci gözünde ne var?" diye soruyor, telsizin öbür ucundaki görevli bakıp cevap veriyor; "onu al, sepete koy" diyor, görevli yapıyor. Her soru ve her komut ayrı bir telsiz konuşması. Sorumlu depoyu gerçekten yönetebiliyor ama depodaki her ayrıntıyı (kutunun içinde ne var, ışık ne zaman yandı) göremiyor ve her konuşma birkaç saniye alıyor. **Selenium WebDriver** tam olarak böyle çalışır: test kodun tarayıcının *dışında*, ayrı bir süreçte koşar; tarayıcıyla arasında standart bir protokol konuşan bir **sürücü (driver)** vardır. Bu tasarımın hem büyük gücü hem de bilinen sınırları buradan doğar.

Selenium 2004'ten beri hayatta ve bugün **W3C WebDriver** adıyla bir tarayıcı standardıdır — yani Chrome, Firefox, Edge, Safari bu protokolü *kendi* destekler. Bir e-ticaret ekibinde "regresyon paketimiz Selenium" cümlesini duyduğunda, arkasında genelde yıllarca birikmiş yüzlerce test, kurumsal bir **Grid** altyapısı ve Appium ile ortak bir mobil zemin vardır.

### Temel kavramlar

- **Selenium ailesi.** Üç ayrı ürün vardır ve mülakatta karıştırılmaları sık görülür. **WebDriver**: kodla tarayıcı süren kütüphane ve protokoldür; asıl iş budur. **Selenium IDE**: bir tarayıcı eklentisidir; kaydet-oynat ile hızlı prototip içindir, ciddi bir regresyon paketi için değildir. **Selenium Grid**: testleri birden çok makineye ve tarayıcıya dağıtan koşum altyapısıdır. Tarihte kalan **Selenium RC** (Remote Control) ise sayfaya JavaScript enjekte ederek çalışırdı; WebDriver onun yerini aldı ve artık kullanılmaz.
- **W3C WebDriver protokolü.** İstemci (test kodun) ile sürücü arasındaki iletişim **HTTP üzerinden JSON** ile yapılır. `driver.findElement(...)` çağrısı aslında `POST /session/{oturumId}/element` gibi bir HTTP isteğine dönüşür. Bu bir standarttır: Java, Python, C#, JavaScript, Ruby istemcileri aynı protokolü konuşur — bu yüzden Selenium'un dil desteği bu kadar geniştir.
- **Süreç dışı (out-of-process) mimari.** Test JVM'i ile tarayıcı **ayrı işletim sistemi süreçleridir**. Bunun üç sonucu vardır. (1) Her komut bir ağ gidiş-dönüşüdür; çok sayıda ince taneli komut atan testlerde gecikme birikir. (2) Tarayıcının iç durumuna doğrudan erişim yoktur; yani hangi JavaScript olay dinleyicisinin bağlandığını veya hangi ağ isteğinin uçuşta olduğunu göremezsiniz. Yalnız protokolün açtığı kadarını görürsünüz. (3) Buna karşılık test kodu tarayıcıdan tamamen bağımsızdır. Gerçek tarayıcıda gerçek davranış ölçülür ve altyapı standarttır.
- **Sürücü (driver) ve sürüm uyumu.** Her tarayıcının kendi sürücü ikilisi vardır: `chromedriver`, `geckodriver` (Firefox), `msedgedriver`, `safaridriver`. Eskiden bu ikililerin tarayıcı sürümüyle *aynı ana sürümde* olması gerekirdi ve Chrome kendini otomatik güncelledikçe CI kırılırdı; ekipler bunu `WebDriverManager` kütüphanesiyle çözerdi. **Selenium 4.6'dan itibaren gömülü gelen Selenium Manager** bu işi kendisi yapar: doğru sürücüyü bulur, yoksa indirir, `PATH` ayarıyla uğraşmana gerek kalmaz. Şerh: indirme yapabilmesi için ağ erişimi ister; kapalı (air-gapped) bir CI ağında sürücüyü elle sağlamak veya önbelleği önceden doldurmak hâlâ gerekir.
- **`WebDriver`, `WebElement`, `By`.** `WebDriver` tarayıcı oturumunu temsil eden arayüzdür (`get`, `quit`, `switchTo`, `manage`). `WebElement` sayfadaki tek bir düğüme **o anki** referanstır (`click`, `sendKeys`, `getText`). `By` ise arama ölçütüdür (`By.id`, `By.cssSelector`, `By.xpath`, `By.tagName`).
- **`findElement` vs `findElements`.** İkisi kritik biçimde farklı davranır: `findElement` bulamazsa **`NoSuchElementException` fırlatır**; `findElements` bulamazsa **boş liste döner**, istisna atmaz. "Bu element sayfada olmamalı" doğrulaması bu yüzden `findElements(...).isEmpty()` ile yazılır.
- **Üç bekleme türü.** **Örtük bekleme (implicit wait)** sürücü oturumuna bir kez kurulur, *yalnız element arama* çağrılarına uygulanır ve "bulunana kadar dene" der. **Açık bekleme (explicit wait)** `WebDriverWait` + `ExpectedConditions` ile *belirli bir koşulu* bekler (görünür oldu mu, tıklanabilir mi, metin geldi mi). **Akıcı bekleme (fluent wait)** `FluentWait` ile aynı işi yapar ama yoklama aralığını (`pollingEvery`) ve yok sayılacak istisnaları (`ignoring`) ayarlamana izin verir.
- **İki bekleme türünü karıştırmanın tehlikesi.** Selenium'un kendi belgeleri açıkça uyarır: örtük ve açık beklemeyi **birlikte kullanma**. Sebebi şu: açık beklemenin her yoklaması içeride bir element arama çağrısı yapar, o çağrı da örtük bekleme bütçesini tetikler; süreler çarpışıp toplam bekleme **öngörülemez** hale gelir. 5 saniyelik bir `WebDriverWait` pratikte 30+ saniye sürebilir. Kural: örtük beklemeyi hiç kurma (0'da bırak), her beklemeyi açık yaz.
- **`Thread.sleep` neden yasak.** Sabit süre bir koşulu değil saati bekler: hızlı ortamda boşuna harcanır, yavaş ortamda (CI yükü, paralel koşum, ağ gecikmesi) yine yetersiz kalır. Yarış koşulunu **çözmez, gizler**; ortam değişince test yeniden kırmızı yanar.
- **`StaleElementReferenceException`.** Elinde tuttuğun `WebElement`, bulunduğu andaki DOM düğümüne bir referanstır. Sayfa o bölümü yeniden çizerse (Angular/React yeniden render, sayfa yenileme, liste güncellemesi) eski düğüm atılır ve referans **bayatlar** — sonraki her çağrı bu istisnayı verir. `NoSuchElementException` ile farkı önemlidir: `NoSuchElement` "hiç bulamadım" der, `StaleElementReference` "bulmuştum ama artık geçerli değil" der.
- **`ElementClickInterceptedException`.** Tıklamak istediğin elementin tam üstünde başka bir element vardır — çerez banner'ı, yapışkan (sticky) başlık, kampanya açılır penceresi. Element görünür ve etkindir, ama tıklama koordinatına düşen başka bir düğümdür. Çözüm engeli kaldırmaktır (banner'ı kapat, elemente kaydır), JavaScript ile zorla tıklamak değil — o, gerçek kullanıcının yapamayacağı bir şeyi yapıp hatayı gizler.
- **Selenium Grid.** Testleri merkezi bir uç noktaya gönderip birden çok makinede/tarayıcıda koşturan altyapı. Klasik anlatımı **hub-node**'dur: testler hub'a bağlanır, hub uygun node'a dağıtır. Selenium 4'te bu bileşenlere ayrıldı (Router, Distributor, Session Map, Node) ve tek makinede `standalone` modda da çalışır. Test tarafında değişen tek şey `ChromeDriver` yerine `RemoteWebDriver` kullanmaktır.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Selenium WebDriver süreç dışı mimarisi: test kodu, W3C protokolü, sürücü ve tarayıcı">
  <defs>
    <marker id="selwd-ok" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#186399"/>
    </marker>
    <marker id="selwd-okgeri" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Süreç dışı mimari: her komut bir ağ gidiş-dönüşü</text>
  <rect x="14" y="46" width="176" height="96" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="102" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Test kodu (JVM)</text>
  <text x="102" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Java + TestNG</text>
  <text x="102" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Selenium istemci kütüphanesi</text>
  <text x="102" y="130" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">driver.findElement(...)</text>
  <line x1="192" y1="80" x2="266" y2="80" stroke="#186399" stroke-width="1.8" marker-end="url(#selwd-ok)"/>
  <text x="229" y="70" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">HTTP + JSON</text>
  <line x1="266" y1="112" x2="192" y2="112" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="4,3" marker-end="url(#selwd-okgeri)"/>
  <text x="229" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">cevap</text>
  <rect x="270" y="46" width="176" height="96" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="358" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B07D20" font-family="system-ui">chromedriver</text>
  <text x="358" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ayrı bir süreç</text>
  <text x="358" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">W3C komutlarını çevirir</text>
  <text x="358" y="130" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sürüm uyumu: Selenium Manager</text>
  <line x1="448" y1="80" x2="522" y2="80" stroke="#186399" stroke-width="1.8" marker-end="url(#selwd-ok)"/>
  <text x="485" y="70" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">tarayıcı içi</text>
  <line x1="522" y1="112" x2="448" y2="112" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="4,3" marker-end="url(#selwd-okgeri)"/>
  <rect x="526" y="46" width="120" height="96" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="586" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Chrome</text>
  <text x="586" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gerçek tarayıcı</text>
  <text x="586" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gerçek DOM</text>
  <rect x="14" y="164" width="316" height="72" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="172" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Bedeli</text>
  <text x="172" y="206" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her findElement / click ayrı istek → gecikme birikir</text>
  <text x="172" y="224" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tarayıcı iç durumuna sınırlı erişim</text>
  <rect x="346" y="164" width="300" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="496" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kazancı</text>
  <text x="496" y="206" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">W3C standardı: her tarayıcı, her dil</text>
  <text x="496" y="224" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Grid ve Appium ile ortak zemin</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

1. **Oturumu aç ve kapat (TestNG iskeleti).** `@BeforeMethod` her test metodundan önce, `@AfterMethod` sonra çalışır; sürücüyü burada açıp kapatmak testler arası izolasyonun temelidir. `quit()` yalnız pencereyi değil sürücü sürecini de sonlandırır (`close()` sadece o pencereyi kapatır — CI'da unutulursa süreç sızıntısı olur).

```java
public class SepetTesti {

  private WebDriver driver;
  private WebDriverWait bekle;

  @BeforeMethod
  public void hazirla() {
    ChromeOptions secenekler = new ChromeOptions();
    secenekler.addArguments("--headless=new", "--window-size=1440,900", "--no-sandbox");
    // Sürücü ikilisini Selenium Manager bulur; PATH ayarı gerekmez.
    driver = new ChromeDriver(secenekler);
    bekle = new WebDriverWait(driver, Duration.ofSeconds(10));
    // Örtük bekleme BİLİNÇLİ olarak kurulmuyor; tüm beklemeler açık.
  }

  @AfterMethod(alwaysRun = true)
  public void kapat() {
    if (driver != null) {
      driver.quit();
    }
  }
}
```

2. **Elementi bul ve eylemi yap — beklemeyi açık yaz.** Aşağıdaki akış e-ticaret için tipik: ürün ara, ilk sonuca gir, sepete ekle, sepet rozetinin güncellenmesini kanıt say.

```java
@Test(groups = {"sepet"})
public void aramaSonucundanSepeteEkle() {
  driver.get("https://magaza.example.com/");

  driver.findElement(By.cssSelector("[data-test='arama-kutusu']")).sendKeys("bebek bezi\n");

  WebElement ilkUrun = bekle.until(ExpectedConditions.visibilityOfElementLocated(
      By.cssSelector("[data-test='urun-kart']")));
  ilkUrun.click();

  bekle.until(ExpectedConditions.elementToBeClickable(
      By.cssSelector("[data-test='sepete-ekle']"))).click();

  // Kanıt: sunucunun güncellediği rozet. "Buton tıklandı" kanıt değildir.
  bekle.until(ExpectedConditions.textToBePresentInElementLocated(
      By.cssSelector("[data-test='sepet-rozet']"), "1"));
}
```

3. **"Element olmamalı" doğrulaması `findElements` ile yazılır.** `findElement`'i `try/catch` içine almak hem çirkin hem yavaştır (örtük bekleme kuruluysa her negatif kontrol tam bütçeyi harcar).

```java
// Stokta olmayan ürün için "Sepete Ekle" butonu hiç render edilmemeli
List<WebElement> butonlar = driver.findElements(By.cssSelector("[data-test='sepete-ekle']"));
Assert.assertTrue(butonlar.isEmpty(), "Stok yokken sepete ekle butonu görünmemeli");
```

4. **Bayat referansı (stale) locator saklayarak çöz.** Sepette adet artırınca satır listesi yeniden çizilir; elde tutulan `WebElement` referansı ölür.

```java
// YANLIŞ — element referansı saklanıyor
WebElement satir = driver.findElement(By.cssSelector("[data-test='sepet-satir']"));
driver.findElement(By.cssSelector("[data-test='adet-arttir']")).click();
satir.getText();   // StaleElementReferenceException: satır yeniden çizildi

// DOĞRU — locator saklanır, element her seferinde yeniden bulunur
By satirLocator = By.cssSelector("[data-test='sepet-satir']");
driver.findElement(By.cssSelector("[data-test='adet-arttir']")).click();
bekle.until(ExpectedConditions.textToBePresentInElementLocated(satirLocator, "2"));
String metin = driver.findElement(satirLocator).getText();
```

Kalıcı olarak bayatlayan bir bölgede `FluentWait` ile istisnayı yok sayarak yeniden denemek de bir seçenektir:

```java
Wait<WebDriver> akiciBekle = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(15))
    .pollingEvery(Duration.ofMillis(300))
    .ignoring(StaleElementReferenceException.class);

String toplam = akiciBekle.until(d -> d.findElement(By.cssSelector("[data-test='ara-toplam']")).getText());
```

5. **Örtük engeli kaldır, tıklamayı zorlama.** `ElementClickInterceptedException` alıyorsan üstte bir şey vardır; önce onu kapat.

```java
List<WebElement> cerezKapat = driver.findElements(By.cssSelector("[data-test='cerez-kabul']"));
if (!cerezKapat.isEmpty()) {
  cerezKapat.get(0).click();   // best-effort: yoksa bekletmeden geç
}
```

6. **Veri odaklı test: `@DataProvider`.** Aynı akışı farklı verilerle koşturmanın TestNG yolu; kupon/indirim senaryoları için biçilmiş kaftandır.

```java
@DataProvider(name = "kuponlar")
public Object[][] kuponlar() {
  return new Object[][] {
      {"HOSGELDIN10", 10, true},
      {"KARGOBEDAVA", 0, true},
      {"SURESIDOLMUS", 0, false}
  };
}

@Test(dataProvider = "kuponlar", groups = {"kupon"})
public void kuponUygula(String kod, int beklenenIndirimYuzdesi, boolean gecerliMi) {
  // ... tek akış, üç veri satırı: TestNG bunu üç ayrı test olarak raporlar
}
```

7. **Paralel koşum: `testng.xml` + `ThreadLocal`.** TestNG paralelliği XML tarafında ayarlanır:

```xml
<suite name="eticaret-regresyon" parallel="methods" thread-count="4">
  <test name="sepet-ve-kupon">
    <groups>
      <run><include name="sepet"/><include name="kupon"/></run>
    </groups>
    <classes>
      <class name="tr.magaza.testler.SepetTesti"/>
    </classes>
  </test>
</suite>
```

`parallel="methods"` ayarı metotları ayrı iş parçacıklarına dağıtır. Ama **aynı sınıf örneğini** paylaştırır. Bu durumda `private WebDriver driver` alanı, iş parçacıkları arasında paylaşılan bir kaynağa dönüşür ve testler birbirinin oturumunu ezer. Doğru kalıp, sürücüyü iş parçacığına özel tutmaktır:

```java
private static final ThreadLocal<WebDriver> SURUCU = new ThreadLocal<>();

@BeforeMethod
public void hazirla() {
  SURUCU.set(new ChromeDriver(secenekler));
}

@AfterMethod(alwaysRun = true)
public void kapat() {
  SURUCU.get().quit();
  SURUCU.remove();   // remove() unutulursa iş parçacığı havuzunda sızıntı olur
}
```

`parallel="classes"` seçeneği ise her sınıfı tek iş parçacığında koşturur; sınıf içindeki alan paylaşımı o durumda güvenlidir ama paralellik tanesi kabalaşır.

8. **Uzak koşum: Grid.** Docker ile tek komutta ayağa kalkar:

```
docker run -d -p 4444:4444 --shm-size=2g selenium/standalone-chrome:latest
```

Test tarafında değişen tek satır sürücünün nasıl yaratıldığıdır:

```java
ChromeOptions secenekler = new ChromeOptions();
driver = new RemoteWebDriver(new URL("http://localhost:4444"), secenekler);
```

`--shm-size=2g` bilinen bir tuzaktır. Docker'ın varsayılan paylaşımlı bellek alanı Chrome için küçüktür. Büyütülmezse tarayıcı rastgele çöker ve bu çökme "kararsız (flaky) test" sanılır.

```svg
<svg viewBox="0 0 660 270" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Örtük, açık ve akıcı bekleme karşılaştırması ve karıştırma uyarısı">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç bekleme türü — ve karıştırma yasağı</text>
  <rect x="14" y="42" width="200" height="150" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="114" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Örtük (implicit)</text>
  <text x="114" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">oturuma bir kez kurulur</text>
  <text x="114" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yalnız element aramaya uygulanır</text>
  <text x="114" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">görünürlük / metin koşulunu</text>
  <text x="114" y="142" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bekleyemez</text>
  <text x="114" y="166" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">negatif kontrolü yavaşlatır</text>
  <text x="114" y="182" text-anchor="middle" font-size="10" fill="#B07D20" font-family="system-ui">öneri: 0'da bırak</text>
  <rect x="230" y="42" width="200" height="150" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Açık (explicit)</text>
  <text x="330" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">WebDriverWait + ExpectedConditions</text>
  <text x="330" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">belirli bir koşulu bekler</text>
  <text x="330" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">visibilityOfElementLocated</text>
  <text x="330" y="142" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">elementToBeClickable</text>
  <text x="330" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">textToBePresentInElementLocated</text>
  <text x="330" y="182" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">öneri: varsayılan yol</text>
  <rect x="446" y="42" width="200" height="150" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="546" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Akıcı (fluent)</text>
  <text x="546" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">FluentWait</text>
  <text x="546" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yoklama aralığı ayarlanır</text>
  <text x="546" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">pollingEvery(300 ms)</text>
  <text x="546" y="142" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ignoring(StaleElement...)</text>
  <text x="546" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sık bayatlayan bölgeler için</text>
  <text x="546" y="182" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">öneri: özel durumlarda</text>
  <rect x="14" y="206" width="632" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="228" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Örtük + açık beklemeyi ASLA birlikte kullanma</text>
  <text x="330" y="248" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">açık beklemenin her yoklaması örtük bütçeyi tetikler → toplam süre öngörülemez (5 sn beklenen adım 30+ sn sürebilir)</text>
</svg>
```

**Selenium 4 ile ne değişti?** Mülakatta "Selenium 3'ten sonra ne oldu?" sorusu sık gelir:

- **W3C protokolüne tam geçiş.** Eski JSON Wire Protocol kaldırıldı. Pratik sonucu: istemci ile sürücü arasında protokol çevirisi yok, davranış tarayıcılar arasında daha tutarlı.
- **Göreli konumlandırıcılar (relative locators).** Bir elementi başka bir elemente göre konumundan bulmak: `above`, `below`, `toLeftOf`, `toRightOf`, `near`.

```java
import static org.openqa.selenium.support.locators.RelativeLocator.with;

WebElement sepeteEkle = driver.findElement(
    with(By.tagName("button")).below(By.cssSelector("[data-test='urun-fiyat']")));
```

  Şerh: göreli konumlandırıcılar *görsel* konuma bağlıdır; duyarlı (responsive) bir düzende ekran boyutu değişince "altında" olan element değişebilir. Kararlı bir `data-test` özniteliği varken bunu tercih etmek kırılganlık üretir — bu yetenek, tutunacak başka hiçbir şeyin olmadığı eski sayfalar için bir çıkış kapısıdır.
- **CDP erişimi ve BiDi yönü.** Selenium 4, Chromium tabanlı tarayıcılarda **Chrome DevTools Protocol (CDP)** üzerinden ağ isteğine müdahale, konsol logu okuma, coğrafi konum sahteleme gibi işlere kapı açtı. Şerh — mülakatta bunu söylemek fark yaratır: CDP **Chromium'a özgüdür** ve tarayıcı sürümüne sıkı bağlıdır (Chrome ana sürüm atlayınca kırılabilir); Selenium bunu kalıcı bir taahhüt olarak sunmaz. Standart ve uzun vadeli yön **WebDriver BiDi**'dir: W3C'nin, tarayıcıyla çift yönlü ve olay tabanlı iletişim için hazırladığı yeni standart. BiDi olgunlaştıkça Selenium'un "tek yönlü komut/cevap" sınırı büyük ölçüde kalkacaktır.
- **Yeni pencere/sekme API'si.** Artık pencere açmak için JavaScript hilesine gerek yok: `driver.switchTo().newWindow(WindowType.TAB)` doğrudan yeni sekme açıp odağı ona verir.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Selenium Grid: testler router üzerinden node'lara dağıtılır">
  <defs>
    <marker id="selwd-gridok" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Selenium Grid: aynı test kodu, dağıtık koşum</text>
  <rect x="14" y="60" width="150" height="140" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="89" y="84" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">TestNG paketi</text>
  <text x="89" y="106" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">thread-count=4</text>
  <text x="89" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">RemoteWebDriver</text>
  <text x="89" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">http://grid:4444</text>
  <text x="89" y="172" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ChromeOptions ile</text>
  <text x="89" y="188" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tarayıcı talep edilir</text>
  <line x1="166" y1="130" x2="238" y2="130" stroke="#186399" stroke-width="1.8" marker-end="url(#selwd-gridok)"/>
  <rect x="242" y="80" width="150" height="100" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="317" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Router / Hub</text>
  <text x="317" y="128" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">oturum talebini karşılar</text>
  <text x="317" y="148" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">uygun node'a dağıtır</text>
  <text x="317" y="168" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kuyruk + eşleştirme</text>
  <line x1="394" y1="110" x2="466" y2="82" stroke="#186399" stroke-width="1.6" marker-end="url(#selwd-gridok)"/>
  <line x1="394" y1="130" x2="466" y2="130" stroke="#186399" stroke-width="1.6" marker-end="url(#selwd-gridok)"/>
  <line x1="394" y1="150" x2="466" y2="178" stroke="#186399" stroke-width="1.6" marker-end="url(#selwd-gridok)"/>
  <rect x="470" y="58" width="176" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="558" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Node — Chrome</text>
  <text x="558" y="95" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">3 eşzamanlı oturum</text>
  <rect x="470" y="110" width="176" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="558" y="130" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Node — Firefox</text>
  <text x="558" y="147" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">2 eşzamanlı oturum</text>
  <rect x="470" y="162" width="176" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="558" y="182" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Node — Edge</text>
  <text x="558" y="199" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">2 eşzamanlı oturum</text>
  <text x="330" y="232" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Dikkat: Grid ölçek ve tarayıcı çeşitliliği kazandırır — tek testi hızlandırmaz, araya bir ağ atlaması daha ekler.</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Selenium nasıl çalışır, mimarisini anlat."** Beklenen: test kodu → Selenium istemci kütüphanesi → W3C WebDriver protokolü (HTTP + JSON) → tarayıcı sürücüsü (chromedriver) → tarayıcı. Süreç dışıdır; bedeli komut başına ağ gidiş-dönüşü ve tarayıcı iç durumuna sınırlı erişim, kazancı standart protokol ve çok geniş dil/altyapı desteğidir. **Elenme sebebi:** "Selenium sayfaya JavaScript enjekte eder" demek — o, 2011'de emekliye ayrılan Selenium RC'ydi.
- **"`findElement` ile `findElements` farkı ne?"** Beklenen: biri bulunamayınca `NoSuchElementException` fırlatır, diğeri boş liste döner. Devamı da sorulur: "bir elementin sayfada *olmadığını* nasıl doğrularsın?" → `findElements(...).isEmpty()`.
- **"Örtük ve açık bekleme farkı; ikisini birlikte kullanır mısın?"** Beklenen: kesin "hayır". Örtük yalnız element aramaya uygulanır ve koşul bekleyemez; ikisi birlikteyken süreler öngörülemez biçimde birleşir. **Elenme sebebi:** "İkisini birlikte kullanırım, garanti olsun diye" demek.
- **"`Thread.sleep` neden kullanmıyorsun?"** Beklenen: sabit süre koşulu değil saati bekler; yarış koşulunu çözmez, gizler. Hızlı ortamda israf, yavaş ortamda yetersizdir.
- **"`StaleElementReferenceException` neden alınır, nasıl çözülür?"** Beklenen: DOM o bölümü yeniden çizdi, elde tutulan referans bayatladı. Çözüm: `WebElement` değil `By` sakla, elementi eylem anında yeniden bul; sık bayatlayan bölgede `FluentWait().ignoring(StaleElementReferenceException.class)`. **Tuzak:** bunu `NoSuchElementException` ile karıştırmak veya `Thread.sleep` ile geçmeye çalışmak.
- **"`ElementClickInterceptedException` alıyorsun, ne yaparsın?"** Beklenen: üstte bir element var (çerez banner'ı, sticky başlık, açılır pencere) — engeli kaldır veya elemente kaydır. **Tuzak:** "JavascriptExecutor ile zorla tıklarım" demek; bu gerçek kullanıcının yapamayacağı bir eylemdir ve gerçek bir arayüz hatasını gizler.
- **"Sürücü sürüm uyumunu nasıl yönetiyorsun?"** Beklenen: Selenium 4.6+ ile gelen Selenium Manager doğru sürücüyü kendisi bulur/indirir; öncesinde WebDriverManager ya da elle sürüm sabitleme kullanılırdı. Kapalı ağda önbelleği önceden doldurmak gerekir.
- **"Grid ne işe yarar, ne zaman kullanırsın?"** Beklenen: farklı tarayıcı/işletim sistemi kombinasyonlarını merkezi altyapıda koşturmak ve ölçeklemek. Testte değişen tek şey `RemoteWebDriver`. **Tuzak:** "Grid testleri hızlandırır" demek — Grid *paralelliği ölçekler*, tek bir testin süresini araya bir ağ atlaması daha ekleyerek **uzatır**.
- **"TestNG ile paralel nasıl koşturursun?"** Beklenen: `testng.xml` içinde `parallel="methods"` (veya `classes`) ve `thread-count`; sürücü `ThreadLocal` ile iş parçacığına özel tutulur, `@AfterMethod`'da `quit()` + `remove()`. **Tuzak:** paylaşılan `private WebDriver driver` alanıyla `parallel="methods"` kullanmak.
- **"Selenium 4 ile ne geldi?"** Beklenen: W3C'ye tam geçiş, göreli konumlandırıcılar, CDP erişimi ve BiDi yönü, yeni pencere/sekme API'si, Grid'in bileşen mimarisi. CDP'yi anlatırken Chromium'a özgü ve sürüme bağlı olduğunu, standart yönün BiDi olduğunu eklemek olgunluk gösterir.

### Tek cümlede

Selenium WebDriver, testi tarayıcının dışında çalıştırıp W3C standardı üzerinden HTTP+JSON komutlarıyla süren bir araçtır. Kararlılığı **açık bekleme disiplininden** gelir: örtük beklemeyi hiç kurmamak, `Thread.sleep`'i yasaklamak, öğenin kendisini değil seçicisini (locator) saklamak. Ölçeği ise **Grid ve TestNG paralelliğinden** gelir; ama bunun şartı, sürücüyü `ThreadLocal` ile iş parçacığına bağlamaktır.

# sel-1 | Temel
## Soru
Bir e-ticaret testinde "stokta olmayan üründe Sepete Ekle butonu hiç görünmemeli" doğrulaması yazacaksın. `driver.findElement(...)` ile `driver.findElements(...)` arasındaki davranış farkı nedir ve bu doğrulama için hangisi uygundur?
## A
İkisi de element bulunamadığında `null` döner; aralarındaki tek fark dönüş tipidir, negatif doğrulama için ikisi de `null` kontrolüyle kullanılır
🤖 Selenium'da hiçbiri `null` döndürmez; `findElement` istisna fırlatır, `findElements` boş liste döner.
## B
`findElement` bulamazsa `NoSuchElementException` fırlatır, `findElements` boş liste döner; negatif doğrulama `findElements(...).isEmpty()` ile yazılır
🤖 Doğru: boş liste dönen sürüm, "yok" durumunu istisna yakalamadan, doğal bir koşul olarak ifade etmeyi sağlar.
## C
`findElements` bulamazsa `NoSuchElementException` fırlatır, `findElement` ise `null` döner; bu yüzden negatif doğrulamada `findElement` kullanılır
🤖 Davranışlar tam ters anlatılmış: istisna fırlatan `findElement`, boş liste dönen `findElements`'tır.
## D
İkisi de bulunamayınca istisna fırlatır; tek fark `findElements`'ın örtük beklemeyi yok sayıp anında sonuç dönmesidir
🤖 `findElements` istisna fırlatmaz ve örtük beklemeyi de yok saymaz; bekleme kuruluysa boş liste dönmeden önce bütçeyi harcar.
## Doğru
B
## Açıklama
`findElement` tek bir element döndürmek zorundadır; bulamazsa yapabileceği tek şey `NoSuchElementException` fırlatmaktır. `findElements` ise bir liste döndürür ve "hiç yok" durumunun doğal karşılığı **boş listedir** — istisna fırlatmaz. Bu fark, negatif doğrulamalarda belirleyicidir: `findElement`'i `try/catch` ile sarmalamak hem okunabilirliği bozar hem de örtük bekleme kuruluysa her negatif kontrolü tam bekleme bütçesi kadar yavaşlatır. `List<WebElement> butonlar = driver.findElements(...); assertTrue(butonlar.isEmpty())` biçimi hem niyeti açıkça söyler hem de istisna akışını iş mantığı için kullanmaktan kaçınır.

# sel-2 | Orta
## Soru
Bir ekip hem `driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))` kurmuş hem de adımlarda `new WebDriverWait(driver, Duration.ofSeconds(5))` kullanıyor. Teoride en fazla 5 saniyede pes etmesi gereken bazı adımlar gerçekte 30 saniyeden uzun sürüyor. En isabetli açıklama nedir?
## A
`WebDriverWait` yapıcısının ikinci parametresi aslında milisaniye kabul ettiği için verilen değer bin katına çıkıyor
🤖 Modern Selenium'da parametre bir `Duration` nesnesidir; birim karışıklığı söz konusu değil.
## B
Açık bekleme her zaman örtük beklemeyi ezer; fazladan geçen süre tamamen Grid'e giden ağ gecikmesinden kaynaklanır
🤖 Açık bekleme örtüğü ezmez, onunla iç içe geçer; ayrıca ağ gecikmesi tek başına 25 saniyelik bir fark üretmez.
## C
Örtük ve açık bekleme birlikte kullanılınca süreler öngörülemez biçimde birleşir: açık beklemenin her yoklaması içeride bir element arama çağrısı yapar ve o çağrı örtük bütçeyi de tetikler
🤖 Doğru: Selenium'un kendi belgeleri bu karışımı açıkça uyarır; çözüm örtük beklemeyi hiç kurmamak ve tüm beklemeleri açık yazmaktır.
## D
Örtük bekleme yalnızca `findElements` çağrılarına uygulanır; `findElement` çağrıları bundan etkilenmediği için süre farkı ortaya çıkar
🤖 Örtük bekleme her iki arama çağrısına da uygulanır; ayrıca bu iddia gözlenen *uzama* davranışını açıklamaz.
## Doğru
C
## Açıklama
Örtük bekleme sürücü oturumuna kurulan **global** bir "elementi bulana kadar dene" bütçesidir ve her element arama çağrısına uygulanır. Açık bekleme (`WebDriverWait`) ise bir koşulu yoklarken, her yoklama turunda içeride yine bir element arama çağrısı yapar — o çağrı da örtük bütçeyi tetikler. Sonuç, iki sürenin toplanması değil, **iç içe geçip öngörülemez biçimde çarpışmasıdır**; 5 saniyelik bir bekleme pratikte onlarca saniyeye çıkabilir. `WebDriverWait`'in milisaniye/saniye birimi ya da Grid gecikmesi bu ölçekte bir fark yaratmaz, örtük beklemenin yalnız `findElements`'a uygulandığı iddiası da yanlıştır. Doğru duruş nettir: örtük beklemeyi hiç kurma (0'da bırak), her beklemeyi koşula bağlı olarak açık yaz.

# sel-3 | Orta
## Soru
Sepet sayfasında satır listesi tutuluyor; adet artırma butonuna basılınca uygulama listeyi yeniden çiziyor. Daha önce `WebElement satir = driver.findElement(...)` ile alınan referans üzerinden `satir.getText()` çağrıldığında `StaleElementReferenceException` alınıyor. Sebep ve doğru çözüm nedir?
## A
DOM yeniden oluşturulduğu için eski referans artık geçerli bir düğümü göstermiyor; çözüm elementi değil locator'ı (`By`) saklayıp eylem anında yeniden bulmaktır
🤖 Doğru: `WebElement` o anki düğüme bir referanstır; düğüm değişince referans bayatlar, tek sağlıklı yol yeniden bulmaktır.
## B
Element sayfada hiç bulunamamıştır; `StaleElementReferenceException`, `NoSuchElementException` ile aynı durumun farklı bir adıdır
🤖 İkisi farklı durumlardır: `NoSuchElement` "hiç bulamadım", `StaleElementReference` "bulmuştum ama referans artık geçersiz" demektir.
## C
Element başka bir element tarafından örtülmüştür; çözüm sayfayı elemente kaydırmak veya üstteki banner'ı kapatmaktır
🤖 Bu tarif `ElementClickInterceptedException`'a aittir; bayatlama örtülmeyle değil DOM'un yeniden çizilmesiyle ilgilidir.
## D
Sürücü sürümü tarayıcı sürümüyle uyumsuzdur; Selenium Manager sürücüyü güncelleyince istisna kendiliğinden kaybolur
🤖 Sürüm uyumsuzluğu oturum açılışında `SessionNotCreated` benzeri hatalar üretir, çalışan bir testte bayat referans üretmez.
## Doğru
A
## Açıklama
`WebElement`, bulunduğu andaki DOM düğümüne verilen bir tutamaçtır. Uygulama o bölümü yeniden render ettiğinde (liste güncellemesi, sayfa yenileme, bileşenin yeniden oluşturulması) eski düğüm atılır ve tutamaç **bayatlar** — sonraki her çağrı `StaleElementReferenceException` verir. Bu, elementin hiç bulunamadığı `NoSuchElementException` durumundan farklıdır; üstte başka bir element olması durumu ise `ElementClickInterceptedException` ile raporlanır ve konuyla ilgisizdir. Sürücü-tarayıcı sürüm uyumsuzluğu da oturum açılışında patlar, çalışan bir akışta bayat referans üretmez. Doğru kalıp elementi değil **`By` locator'ını** saklamak ve elementi kullanılacağı anda yeniden bulmaktır; sürekli bayatlayan bölgelerde `FluentWait().ignoring(StaleElementReferenceException.class)` ile yeniden deneme de eklenebilir.

# sel-4 | İleri
## Soru
Bir mülakatta "ağ isteklerini kesip taklit etmek (mock) için Selenium'un CDP desteğini kullanacağım, bu kalıcı bir çözüm" dedin. Aşağıdaki ifadelerden hangisi bu konuda doğrudur?
## A
W3C WebDriver protokolü ağ isteğini kesme yeteneğini zaten standart olarak sunar; CDP'ye hiç gerek yoktur
🤖 Klasik W3C WebDriver komut kümesi ağ katmanına müdahale sunmaz; bu boşluk tam da CDP'nin ve sonrasında BiDi'nin doldurmaya çalıştığı yerdir.
## B
CDP, Chromium'un yanı sıra Gecko (Firefox) ve WebKit (Safari) motorlarında da aynı komut kümesiyle desteklenir
🤖 CDP Chromium'un kendi protokolüdür; diğer motorlarda aynı komut kümesiyle desteklenmez, bu yüzden tarayıcılar arası taşınabilir değildir.
## C
Selenium 4, JSON Wire Protocol'ü korudu ve W3C protokolünü isteğe bağlı bir mod olarak ekledi; CDP bu iki protokolün üstünde çalışır
🤖 Tam tersi: Selenium 4 ile JSON Wire Protocol kaldırıldı, W3C tek protokol oldu.
## D
CDP Chromium'a özgüdür ve tarayıcı sürümüne sıkı bağlıdır; standart, tarayıcılar arası ve uzun vadeli yön WebDriver BiDi'dir
🤖 Doğru: CDP pratik bir çıkış kapısıdır ama taşınabilir ve kalıcı bir taahhüt değildir; W3C'nin çift yönlü standardı BiDi bu boşluğu doldurmak için geliştiriliyor.
## Doğru
D
## Açıklama
Selenium 4 ile eski JSON Wire Protocol tamamen kaldırıldı ve **W3C WebDriver** tek protokol oldu; bu protokol tek yönlü komut/cevap modeline dayanır ve ağ katmanına müdahale, konsol olaylarını dinleme gibi yetenekleri standart olarak sunmaz. Selenium bu boşluğu Chromium tabanlı tarayıcılarda **CDP (Chrome DevTools Protocol)** erişimiyle kapatır — ama CDP Chromium'un kendi protokolüdür, Firefox veya Safari'de aynı komut kümesiyle çalışmaz ve tarayıcının ana sürümü atladığında kırılabilir. Bu yüzden CDP'ye dayanan bir çözüm pratik ama kırılgan ve taşınabilir olmayan bir tercihtir. W3C'nin çift yönlü, olay tabanlı yeni standardı **WebDriver BiDi** tam olarak bu ihtiyaç için geliştiriliyor; mülakatta CDP'den bahsederken bu şerhi düşmek, aracı ezberden değil sınırlarıyla birlikte bildiğini gösterir.

# sel-5 | İleri
## Soru
`testng.xml` dosyasında `parallel="methods" thread-count="4"` ayarlandı. Test sınıfında `private WebDriver driver` alanı var ve `@BeforeMethod` içinde atanıyor. Koşum sonrası testler rastgele biçimde birbirinin sayfasında işlem yapıyor, bazıları `NoSuchWindowException` alıyor. Kök neden ve doğru çözüm nedir?
## A
TestNG her `@Test` metodu için ayrı bir sınıf örneği yaratır; sorun sürücüde değil, `thread-count` değerinin makinedeki çekirdek sayısını aşmasındadır
🤖 TestNG aynı sınıfın metotlarını **aynı örnek** üzerinde koşturur; ayrıca çekirdek sayısı aşımı yavaşlık üretir, oturum karışması değil.
## B
`parallel="methods"` metotları ayrı iş parçacıklarına dağıtır ama aynı sınıf örneğini paylaştırır; tek `driver` alanı paylaşılan kaynağa dönüşür, sürücü `ThreadLocal` ile iş parçacığına özel tutulmalıdır
🤖 Doğru: paylaşılan alan son atayan iş parçacığının sürücüsünü gösterir; `ThreadLocal<WebDriver>` her iş parçacığına kendi oturumunu verir.
## C
Selenium aynı makinede aynı anda birden fazla ChromeDriver süreci başlatılmasına izin vermez; paralel koşum yalnızca Grid üzerinden mümkündür
🤖 Aynı makinede birden çok sürücü süreci sorunsuz çalışır; Grid ölçek ve tarayıcı çeşitliliği içindir, yerel paralelliğin ön koşulu değildir.
## D
Paralel koşumda `@BeforeMethod` hiç tetiklenmez; `driver` alanı `null` kaldığı için testler bir önceki testin oturumunu kullanmak zorunda kalır
🤖 `@BeforeMethod` paralel koşumda da her metot için tetiklenir; `null` bir sürücü ise `NullPointerException` verirdi, sessizce başka oturuma bağlanmazdı.
## Doğru
B
## Açıklama
TestNG `parallel="methods"` modunda aynı sınıfın test metotlarını farklı iş parçacıklarına dağıtır, ancak bunları **aynı sınıf örneği** üzerinde koşturur. Dolayısıyla `private WebDriver driver` alanı iş parçacıkları arasında paylaşılan bir duruma dönüşür: iki `@BeforeMethod` neredeyse aynı anda çalışıp alanı üst üste yazar, testler son atanan sürücüye bağlanır ve biri oturumu `quit()` ettiğinde diğeri `NoSuchWindowException` alır. Çözüm `private static final ThreadLocal<WebDriver> SURUCU` kalıbıdır; her iş parçacığı kendi oturumunu görür, `@AfterMethod` içinde `quit()` ve `remove()` birlikte çağrılır (`remove()` unutulursa iş parçacığı havuzunda referans sızıntısı olur). Aynı makinede birden çok sürücü süreci çalıştırmak mümkündür (Grid bunun ön koşulu değildir) ve `@BeforeMethod` paralel koşumda da her metot için tetiklenir; `parallel="classes"` seçilirse her sınıf tek iş parçacığında koşacağı için alan paylaşımı güvenli olur ama paralellik tanesi kabalaşır.

# sel-6 | Uzman
## Soru
Regresyon paketi yerelde 4 iş parçacığıyla 22 dakika sürüyordu; ekip aynı paketi 3 node'lu bir Selenium Grid'e taşıdı ve süre 26 dakikaya çıktı. Ekipte "Grid yavaşmış" yorumu yapılıyor. En isabetli teknik açıklama hangisidir?
## A
Grid, gönderilen senaryoları her zaman sırayla koşturur; gerçek paralellik yalnızca testleri yerelde çalıştırırken mümkündür
🤖 Grid'in varlık sebebi tam olarak eşzamanlı oturum dağıtmaktır; sıraya sokmaz, node kapasitesi yettiği sürece paralel koşturur.
## B
`RemoteWebDriver`, yerel sürücülerin kullandığı W3C protokolü yerine daha yavaş olan eski JSON Wire Protocol'ü kullanmak zorundadır
🤖 Selenium 4 ile JSON Wire Protocol tamamen kaldırıldı; `RemoteWebDriver` da aynı W3C protokolünü konuşur.
## C
Her komut bir HTTP gidiş-dönüşüdür; Grid araya bir ağ atlaması daha ekler ve ince taneli komutlarda gecikme birikir — Grid ölçek içindir, tek testi hızlandırmak için değil
🤖 Doğru: süreç dışı mimaride komut sayısı doğrudan gecikmeye dönüşür; Grid bu gecikmeyi artırır ama karşılığında eşzamanlı kapasite ve tarayıcı matrisi sunar.
## D
Grid node'ları headless çalışamaz; her oturumda sanal ekran (Xvfb) başlatma maliyeti eklendiği için toplam süre uzamıştır
🤖 Grid node'ları headless modda sorunsuz koşar ve resmi Docker imajları zaten sanal ekranı hazır tutar; oturum başına böyle bir başlatma maliyeti yoktur.
## Doğru
C
## Açıklama
Selenium'un süreç dışı mimarisinde `findElement`, `click`, `getText` gibi her çağrı ayrı bir HTTP isteğine dönüşür; bir senaryonun süresi, komut sayısı ile komut başına gecikmenin çarpımına yakındır. Yerelde bu gecikme `localhost` üzerinden mikro saniyeler mertebesindeyken, Grid'de istek önce Router'a, oradan node'a gider — yani her komuta bir ağ atlaması daha eklenir. Yüzlerce ince taneli komut atan bir pakette bu fark toplamda dakikalara çıkar; gözlenen 22 → 26 dakika artışı beklenen bir sonuçtur, bir arıza değil. Grid'in vaadi tek bir testi hızlandırmak değildir: aynı anda çok daha fazla oturum koşturabilmek (yatay ölçek) ve tek bir paketi Chrome/Firefox/Edge gibi farklı tarayıcı ve sürümlerde çalıştırabilmektir. Doğru okuma şudur: 3 node'un toplam kapasitesi yerel makineyi aşana kadar iş parçacığı sayısı artırılmalı; kazanç ancak eşzamanlılık ölçeklendiğinde görünür. Ayrıca `RemoteWebDriver` da aynı W3C protokolünü konuşur ve node'lar headless koşabilir — süre farkının kaynağı protokol veya ekran değil, ağ mesafesidir.
