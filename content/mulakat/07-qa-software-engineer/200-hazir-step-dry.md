---
slug: hazir-step-dry
baslik: "Hazır (Generic) Step'ler ve DRY: Ne Zaman Kütüphane Yeter, Ne Zaman Yetmez"
kademe: Orta
sira: 200
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Cucumber senaryoları **adımlardan (step)** oluşur. Her adımın arkasında bir JavaScript fonksiyonu vardır; buna **adım tanımı (step definition)** denir.

Aynı cümle kalıbını — örneğin "X elementine tıklanır" — her senaryoda yeniden yazmak gereksizdir. Onun yerine **parametreli ve tekrar kullanılabilir** adımlar tanımlanır. Bunlara **hazır adım (generic step)** denir. Bu projede on bir hazır adım vardır ve senaryoların çoğu bunlarla yazılmıştır. Özellik (feature) dosyalarında hiç CSS seçici geçmez.

Buradaki asıl mühendislik kararı iki sorudan oluşur. Hazır adımlar **ne zaman yeterlidir?** **Ne zaman alan bilgisi gerektiren özel bir adım yazmak gerekir?**

Bir de üçüncü soru var. Aynı iş için ikinci bir adım tanımlanırsa buna mükerrer tanım denir. Bu neden tekrar etme ilkesinin (DRY, "kendini tekrar etme") ihlali sayılır?

### Temel kavramlar

- **Hazır adım (generic step)**, iş dilinde yazılmış parametreli bir cümle kalıbıdır: `When('{string} elementine tıklanır', ...)`. `{string}` yer tutucusu her senaryoda farklı bir element adı taşıyabilir. Aynı adım tanımı hem "sepete ekle butonu"na hem "giriş yap linki"ne hizmet eder.
- **Element sözlüğü (element registry)**, `features/support/element-registry.js` dosyasındadır. İş dilindeki element isimlerini (`'e-posta alanı'`, `'hesap menüsü'`) `pages/locators.js` içindeki gerçek CSS seçicilere çevirir. Hazır adımlar seçiciyi bilmez; yalnızca ismi bilir, çevirmeyi sözlük yapar.
- **Tek noktadan güncelleme.** Bir seçici değiştiğinde güncellenecek yer `locators.js` dosyasıdır (bkz. Sayfa Nesnesi konusu). Element sözlüğündeki isim-seçici eşlemesi de aynı zincirin parçasıdır. İsim aynı kalırken arkasındaki seçici değişebilir; hazır adımlar ve özellik dosyaları bundan etkilenmez.
- **Alana özel adım (domain-specific step).** Giriş akışı (`auth.steps.js`) ve sepet matematiği gibi iş mantığı içeren işler hazır adımlarla ifade edilemez. Çünkü bunlar tek bir tıklama veya doldurma değildir; **birden çok adımı ve bir kararı** kapsarlar. Örnek karar: "şifre alanına geçilsin mi, geçilmesin mi?" Bu durumlarda ayrı ve anlamlı isimli adımlar yazılır.
- **Mükerrer adım tanımı, tekrar etme ilkesinin (DRY) ihlalidir.** Aynı cümle kalıbını, ya da anlamca aynı işi yapan farklı bir cümleyi iki kez tanımlamak sorun çıkarır. Cucumber ya çakışma hatası verir ya da hangi adımın çalışacağı belirsizleşir. Vaka çalışmasının kurallarında bu doğrudan eksi puandır.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Generic step akışı: feature, step definition, element registry, locators">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"e-posta alanına yazılır" step'i nasıl çözülür?</text>
  <rect x="20" y="46" width="140" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="68" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">.feature</text>
  <text x="90" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"e-posta alanı"</text>
  <text x="175" y="76" font-size="16" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="190" y="46" width="150" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="265" y="68" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">generic.steps.js</text>
  <text x="265" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">When(...alanına...)</text>
  <text x="355" y="76" font-size="16" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="370" y="46" width="160" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="450" y="68" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">element-registry.js</text>
  <text x="450" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">isim → locator</text>
  <line x1="450" y1="98" x2="450" y2="128" stroke="#5D7789" stroke-width="1.4"/>
  <rect x="370" y="128" width="160" height="46" rx="10" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="450" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">locators.js</text>
  <text x="450" y="166" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">'#txtEmail'</text>
  <text x="320" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">feature dosyasında selector hiç görünmez</text>
</svg>
```

### Nasıl çalışır?

Bu projede hazır adımlar üç yardımcı fonksiyonla çalışır: `resolveLocator`, `resolveElement` ve `resolvePage`:

```js
function locatorFor(world, name) {
  return resolveLocator(world.page, resolveElement(name));
}

When('{string} elementine tıklanır', async function (elementName) {
  await locatorFor(this, elementName).first().click();
});

When('{string} alanına {string} yazılır', async function (elementName, value) {
  await locatorFor(this, elementName).first().fill(value);
});

Given('{string} sayfasına gidilir', async function (pageName) {
  await this.pages.home.goto(resolvePage(pageName));
});
```

`resolveElement('e-posta alanı')` çağrısı, `element-registry.js` içindeki `elements` sözlüğünde bu ismi arar ve karşılığındaki `locators.login.emailInput` tanımını döndürür. Kayıtlı olmayan bir isim çağrılırsa hata fırlatılır; hata mesajına tanımlı elementlerin listesi eklenir. Böylece yanlış yazılmış bir element adı sessizce geçemez.

Sözlük dosyasında ikinci bir eşleme daha vardır: `pages` (`'ana sayfa' → '/'`, `'sepet' → '/cart'`). `resolvePage('sepet')` bir seçici değil, bir **URL yolu** döndürür ve doğrudan `goto` fonksiyonuna verilir.

İki çözümleyicinin ayrı tutulmasının iki nedeni var. Birincisi, döndürdükleri değerin **türü** farklıdır: biri seçici tanımı, diğeri yol. İkincisi, her biri bulunamayan isim için kendi geçerli isim listesini hata mesajına ekler ("Tanımlı elementler: …" ya da "Tanımlı sayfalar: …"). Tek sözlük kullanılsaydı bu mesaj yanıltıcı olurdu.

Ama her iş hazır adıma sığmaz. Örneğin giriş denemesi tek bir tıklama değildir. Önce telefon numarası girilir. Numara geçerliyse şifre adımına geçilir; geçersizse akış orada durur. Bu **koşullu ve çok adımlı** mantık, `auth.steps.js` içinde ayrı bir adım olarak yazılmıştır:

```js
When('{string} kullanıcısı ile giriş denenir', async function (userKey) {
  const user = users[userKey];
  await this.pages.login.open();
  await this.pages.login.submitPhone(user.phone);
  if (user.password !== undefined) {           // numara geçersizse şifre adımına hiç ulaşılmaz
    await this.pages.login.submitPassword(user.password);
  }
});
```

Aynı ayrım sepet matematiğinde de geçerlidir. "Sepetteki toplam, satırların toplamına eşit mi?" gibi bir kontrol tek bir hazır adıma indirgenemez. Çünkü bu kontrol birden çok satırı gezmeyi ve fiyat metinlerini sayıya çevirmeyi gerektirir. Bu yüzden `CartPage` üzerinden özel bir doğrulama yazılır.

Mükerrer tanım burada somut bir bakım riskidir. Aynı işi yapan ikinci bir adım tanımlandığını düşünün: "X elementine basılır", "X elementine tıklanır"ın neredeyse aynısıdır. Cucumber ya çakışma hatası verir ya da hangi adımın eşleşeceği belirsizleşir. Ekip büyüdükçe kimin hangi adımı kullanacağı da belirsizleşir. Vaka çalışmasının kuralı bu yüzden nettir: gerektiğinde yeni adım yazılabilir, ama var olanı kopyalamak eksi puandır.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ne zaman generic step yeter, ne zaman alan-özel step gerekir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Generic mi, alan-özel mi?</text>
  <rect x="40" y="46" width="260" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="170" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Generic step yeter</text>
  <text x="170" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek eylem: tıkla, yaz, kontrol et</text>
  <text x="170" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş mantığı içermez</text>
  <text x="170" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ör. "e-posta alanına yazılır"</text>
  <rect x="340" y="46" width="260" height="118" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="470" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Alan-özel step gerekir</text>
  <text x="470" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çok adımlı + karar içerir</text>
  <text x="470" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">giriş akışı, sepet matematiği</text>
  <text x="470" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ör. "kullanıcısı ile giriş denenir"</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Generic step nedir, feature dosyalarında selector geçmez demek ne anlama gelir?"** Beklenen: generic step, parametreli/tekrar kullanılabilir bir cümle kalıbıdır; element ismini alır, gerçek CSS seçiciye çözümlemeyi element registry yapar — feature dosyası yalnız iş dili görür.
- **"Element registry olmasaydı ne kaybederdin?"** Beklenen: seçici değiştiğinde her step'i veya feature dosyasını tek tek taramak gerekirdi; registry sayesinde isim-seçici eşlemesi tek dosyada değişir.
- **"Neden her şeyi generic step'le yazmadın, hangi durumlarda özel step gerekti?"** Beklenen: giriş akışı ve sepet matematiği gibi çok adımlı, koşul/karar içeren, alan bilgisi gerektiren işler — tek bir "tıkla/yaz" generic step'e sığmaz.
- **"İki step aynı işi yapıyorsa ne olur, neden önemli?"** Beklenen: DRY ihlali; Cucumber'da çakışma veya belirsiz eşleşme riski, ekipte bakım maliyeti artar; case kuralı bunu eksi puan sayar.
- **"11 generic step yeterli mi, her yeni senaryo için yeni step mi yazarsın?"** Beklenen: önce mevcut generic step'lerle ifade edilip edilemeyeceğine bakılır; sadece gerçekten yeni bir eylem/kontrol kalıbı varsa yeni step yazılır, var olanı kopyalamak yerine.
- **"`resolveElement` bilinmeyen bir isim aldığında ne olur?"** Beklenen: hata fırlatır ve tanımlı elementlerin listesini mesaja ekler; bu, yazım hatasının sessizce geçmesini engeller.

### Tek cümlede

Hazır adımlar tek eylemli, parametreli ve tekrar kullanılabilir yapı taşlarıdır; element sözlüğü sayesinde içlerinde seçici geçmez. Giriş akışı ve sepet matematiği gibi çok adımlı, karar içeren işler ise alana özel adım gerektirir. Var olan bir adımı kopyalamak her zaman tekrar etme ilkesinin ihlalidir ve eksi puandır.

# step-1 | Orta
## Soru
Bu projede feature dosyalarında hiç CSS seçici geçmiyor; senaryolar "e-posta alanı", "hesap menüsü" gibi iş dilindeki isimleri kullanıyor. Bu isimlerin gerçek CSS seçicilere dönüşümünü hangi bileşen yapar?
## A
`pages/locators.js` — element isimlerini doğrudan kendi içinde saklar ve step'lere döndürür
🤖 `locators.js` yalnız seçicileri tutar; iş dili isimlerini seçiciye eşleme işini yapan ayrı bir katman vardır.
## B
`features/support/element-registry.js` — iş dili isimlerini `locators.js`'teki tanımlara eşler
🤖 Doğru: `resolveElement()` ismi alır, `elements` sözlüğünden karşılık gelen locator tanımını döndürür.
## C
`generic.steps.js` — her step fonksiyonu kendi içinde bir switch-case ile ismi seçiciye çevirir
🤖 Generic step'ler çözümleme mantığını kendileri barındırmaz; `resolveElement`'e delege ederler.
## D
Playwright'ın kendi API'si — element isimlerini otomatik olarak DOM'daki metinle eşler
🤖 Playwright böyle bir otomatik eşleme sunmaz; eşleme bu projeye özgü `element-registry.js` ile yapılır.
## Doğru
B
## Açıklama
`element-registry.js` içindeki `elements` sözlüğü, iş dilindeki her ismi (`'e-posta alanı'`, `'hesap menüsü'`) `pages/locators.js`'teki karşılığına (`locators.login.emailInput`, `locators.header.accountMenu`) eşler. Generic step'ler (`generic.steps.js`) bu çözümlemeyi kendileri yapmaz; `locatorFor(world, name)` yardımcı fonksiyonu üzerinden `resolveElement(name)`'i çağırıp sonucu `resolveLocator`'a iletirler. Bu ayrım sayesinde feature dosyaları, step tanımları ve hatta locator değerleri birbirinden bağımsız değişebilir — isim aynı kalırken arkasındaki seçici güncellenebilir.

# step-2 | Orta
## Soru
`auth.steps.js` içindeki `'{string} kullanıcısı ile giriş denenir'` step'i, generic step'lerle (`"X alanına Y yazılır"` gibi) ifade edilmemiş, ayrı yazılmıştır. Neden?
## A
Generic step'ler Cucumber'da yalnızca `Given` anahtar kelimesiyle tanımlanabilir; bu step `When` ile yazıldığı için generic kütüphaneye giremez
🤖 `Given`/`When` ayrımı Cucumber'da anlam taşısa da bu step'in ayrı yazılma nedeni anahtar kelime değil, içerdiği dallanmadır.
## B
`users` nesnesindeki veri generic step'lerin `{string}` parametresiyle uyumlu bir formatta değil
🤖 `{string}` parametresi herhangi bir metni taşıyabilir; sorun format uyumsuzluğu değil, akışın koşullu olması.
## C
Generic step'ler yalnızca `login` sayfasında çalışır, `auth.steps.js` başka bir sayfayı hedeflediği için
🤖 Bu step de `login` sayfasını hedefliyor (`this.pages.login.open()`); sayfa farkı ayrı step yazmanın nedeni değil.
## D
Bu step çok adımlı ve koşullu bir iş akışıdır; generic step'ler tek bir eylemi ifade eder, dallanan iş mantığını taşımaz
🤖 Doğru: step, telefon numarasını gönderdikten sonra yalnızca `user.password` tanımlıysa şifre adımına geçiyor — bu, tek bir "tıkla/yaz" generic step'ine sığmayan bir iş kararı.
## Doğru
D
## Açıklama
Generic step'ler ("X elementine tıklanır", "X alanına Y yazılır") tek bir, dallanmayan eylemi ifade etmek için tasarlanmıştır: bir elementi bul, bir işlem yap. `'{string} kullanıcısı ile giriş denenir'` step'i ise `users` sözlüğünden kullanıcıyı bulur, telefon numarasını gönderir ve **yalnızca `password` tanımlıysa** şifre adımına geçer — numara geçersizse akış orada durur, çünkü site bu durumda şifre alanını hiç göstermez. Bu, tek bir generic step'in taşıyamayacağı kadar iş mantığı (domain knowledge) içerir; bu yüzden alan-özel bir step olarak `auth.steps.js` içinde ayrı tanımlanmıştır.

# step-3 | Orta
## Soru
Ekipteki bir geliştirici, var olan `'{string} elementine tıklanır'` step'i dururken, aynı işi yapan yeni bir step `'{string} elementine basılır'` ekliyor. Case study kurallarına göre bu neden eksi puan sayılır?
## A
Cucumber'da aynı anlama gelen iki farklı step cümlesi tanımlamak sözdizimi hatası verir ve koşum hiç başlamadan çöker
🤖 Bu bir sözdizimi hatası değildir; Cucumber iki farklı cümleyi ayrı step olarak kabul eder, kod çalışır.
## B
Yalnızca `generic.steps.js` dosyasına yeni step eklemek yasaktır, diğer step dosyalarına eklenebilir
🤖 Kısıtlama hangi dosyaya eklendiğiyle ilgili değil; sorun aynı işi yapan step'in tekrar tanımlanmasıdır.
## C
Bu bir DRY ihlalidir: mükerrer step tanımı bakım maliyetini artırır ve hangi step'in kullanılacağı belirsizleşir
🤖 Doğru: iki step aynı işi görüyorsa biri gereksizdir; ekip hangisini kullanacağını bilemez, ikisi de bağımsız bakım gerektirir.
## D
Yeni step'in cümlesi proje konvansiyonundaki fiil kalıbına uymuyor; case study yalnızca "tıklanır" ile biten step adlarına izin veriyor
🤖 Case study böyle bir fiil kalıbı dayatmıyor; sorun cümlenin biçimi değil, işlevin tekrarlanmasıdır.
## Doğru
C
## Açıklama
Case study kuralı nettir: hazır (generic) step kütüphanesi kullanılmalı, gerektiğinde yeni step yazılabilir, ama **kopya (duplicate) step tanımları eksi puandır**. `'{string} elementine tıklanır'` zaten "bir elemente tıkla" işini karşılıyorken aynı işi yapan `'{string} elementine basılır'` eklemek klasik bir DRY (Don't Repeat Yourself) ihlalidir: artık iki step aynı davranışı bağımsız olarak taşır, biri değişirse diğeri unutulabilir, yeni bir senaryo yazan kişi hangisini kullanacağını bilemez. Doğru yaklaşım, önce mevcut step kütüphanesinde karşılığı olup olmadığını kontrol etmek, yalnızca gerçekten yeni bir eylem/kontrol kalıbı için yeni step tanımlamaktır.

# step-4 | Orta
## Soru
`generic.steps.js` içindeki `'{string} metninin görünmediği kontrol edilir'` step'i, sayfadaki **tüm** eşleşen metinleri `.filter({ visible: true })` ile süzüp sayının 0 olduğunu doğruluyor. Bu tasarım kararının nedeni nedir?
## A
Yalnız ilk eşleşmeye bakmak, sayfada aynı metnin görünür ikinci bir kopyası olsa bile testi yanlışlıkla geçirebilirdi
🤖 Doğru: ilk eleman gizli olsa da DOM'da aynı metnin görünür başka bir kopyası olabilir; testin amacı "hiçbir görünür kopya kalmadı" demek.
## B
Playwright'ta `.first()` metodu görünürlük (visibility) kontrolüyle birlikte kullanılamaz, teknik bir zorunluluktur
🤖 `.first()` görünürlük kontrolleriyle birlikte kullanılabilir; sınırlama teknik değil, tasarım tercihidir.
## C
Bu, sayım yapmak yerine tek elemana bakmaya göre performansı artıran bir optimizasyondur; işlevsel olarak ikisi aynı sonucu verir
🤖 Fark işlevseldir: `.filter({visible:true})` ile sayım, yanlış negatif (testin gözden kaçırdığı görünür metin) riskini azaltır.
## D
Cucumber, `{string}` parametresiyle yalnızca çoğul kontrol step'lerinin yazılmasını zorunlu kılar
🤖 Cucumber'ın böyle bir zorunluluğu yoktur; tasarım kararı tamamen bu projeye özgüdür.
## Doğru
A
## Açıklama
"Metin görünmüyor" iddiasını yalnızca ilk eşleşen elementin gizli olduğuna bakarak doğrulamak yanıltıcı olabilir: sayfada aynı metni taşıyan, DOM'da daha aşağıda ama **görünür** ikinci bir kopya varsa, yalnız ilk elemana bakan bir kontrol bunu gözden kaçırır ve test yanlışlıkla geçer. Bu step bunun yerine `getByText(text, { exact: false }).filter({ visible: true })` ile **tüm görünür eşleşmeleri** toplar ve sayının 0 olduğunu (`toHaveCount(0)`) doğrular — yani "hiçbir görünür kopya kalmadı" iddiasını tam olarak karşılar. Bu, generic step tasarlarken bile alan bilgisinin (burada: DOM'un birden fazla eşleşme barındırabileceği gerçeği) göz ardı edilmemesi gerektiğinin bir örneğidir.

# step-5 | Orta
## Soru
`features/support/element-registry.js` iki ayrı çözümleyici dışa aktarıyor: `resolveElement` ve `resolvePage`. Generic step'lerden `'{string} elementine tıklanır'` birincisini, `'{string} sayfasına gidilir'` ise ikincisini kullanıyor. Neden tek bir sözlük ve tek bir çözümleyici yeterli olmadı?
## A
İkisi de aynı `elements` sözlüğüne bakar; `resolvePage` yalnızca döndürülen seçicinin başına `/` ekleyen ince bir sarmalayıcıdır
🤖 `resolvePage` ayrı bir `pages` sözlüğüne bakar ve seçici değil, `/cart` gibi bir URL yolu döndürür.
## B
Cucumber, `Given` ile başlayan step'lerin ayrı bir çözümleyici (resolver) kullanmasını zorunlu kılar
🤖 Cucumber'da `Given`/`When`/`Then` ayrımı yalnızca okunabilirlik içindir; ayrı bir çözümleyici dil zorunluluğu değildir.
## C
İkisi farklı türde değer çözer: `resolveElement` bir locator tanımı, `resolvePage` ise `goto`'ya verilecek URL yolu döndürür
🤖 Doğru: `pages` sözlüğü `'sepet' → '/cart'` gibi yollar tutar; bunları locator sözlüğüyle birleştirmek hata mesajlarını da yanıltıcı yapardı.
## D
`resolvePage` sayfa yüklenene kadar bekleme mantığını da içerdiği için `resolveElement`'ten ayrılmıştır; ikisi aynı sözlüğü paylaşamaz
🤖 `resolvePage` bekleme yapmaz, yalnızca sözlükten bir yol döndürür; bekleme `goto` ve Playwright'ın kendi otomatik beklemesiyle sağlanır.
## Doğru
C
## Açıklama
`element-registry.js` iki ayrı sözlük tutar: `elements` (iş dilindeki element isimlerini `locators.js`'teki tanımlara eşler) ve `pages` (`'ana sayfa' → '/'`, `'sepet' → '/cart'` gibi yolları tutar). `resolveElement` bir **locator tanımı** döndürür ve sonuç `resolveLocator(world.page, ...)` ile gerçek bir Playwright locator'ına çevrilir; `resolvePage` ise `this.pages.home.goto(...)`'ya verilecek bir **URL yolu** döndürür. İkisini tek sözlükte birleştirmek, aynı isim uzayında iki farklı türde değer barındırmak olurdu: bir isim bazen seçiciye, bazen yola çözülürdü. Üstelik her iki çözümleyici de bulunamayan isim için geçerli isimleri listeleyen bir hata mesajı üretir ("Tanımlı elementler: …" / "Tanımlı sayfalar: …"); tek sözlükte bu mesaj yanlış listeyi gösterip senaryo yazarını yanıltırdı. Ayrım küçük ama registry'nin hata mesajlarını isabetli tutan bir tasarım detayıdır.

# step-6 | Orta
## Soru
`resolveElement()` fonksiyonu, `elements` sözlüğünde karşılığı bulunamayan bir isim verildiğinde bir `Error` fırlatıyor. Bu tasarımın generic step yaklaşımına katkısı nedir?
## A
Testin çalışma süresini kısaltır: hata fırlatmak, Playwright'ın bulunamayan element için saniyelerce beklemesinden çok daha hızlı sonuçlanır
🤖 Amaç hız değil; hata mesajının bilgi içeriği. Timeout karşılaştırması bu tasarımın gerekçesi değildir.
## B
Generic step'lerin `{string}` parametresi yerine sabit (hardcoded) element isimleri kullanmasını zorunlu kılar
🤖 Tam tersi: `{string}` parametresi serbest metin taşır, `resolveElement` bu metni sözlükte arar; zorunluluk getirmez.
## C
Element registry'nin, bulunamayan isim için CSS seçiciyi otomatik olarak DOM'dan keşfedip sözlüğe eklemesini sağlar
🤖 `resolveElement` otomatik keşif yapmaz; yalnızca elle tanımlanmış `elements` sözlüğünde arama yapar.
## D
Yanlış yazılmış veya kayıtlı olmayan bir element adının sessizce geçmesini engeller; mesaj geçerli isimleri de listeler
🤖 Doğru: hatanın erken ve açıklayıcı olması, senaryo yazan kişinin doğru element adını bulmasını kolaylaştırır — mesaja tanımlı tüm element isimleri eklenir.
## Doğru
D
## Açıklama
`resolveElement(name)`, `elements` sözlüğünde ismi bulamazsa `` `"${name}" element kaydı bulunamadı. Tanımlı elementler: ${Object.keys(elements).join(', ')}` `` mesajıyla hata fırlatır. Bu, generic step yaklaşımının en kırılgan noktasını — serbest metin parametresiyle çalışmak — güvenli hale getirir: bir senaryo yazarı "e-posta alanı" yerine yanlışlıkla "eposta alani" yazarsa, test sessizce yanlış bir elemente bakmaz veya anlaşılmaz bir Playwright hatası vermez; anında "bu isim tanımlı değil, işte tanımlı olanlar" der. Bu, generic step kütüphanesinin büyüdükçe de güvenilir kalmasını sağlayan küçük ama önemli bir tasarım detayıdır.
