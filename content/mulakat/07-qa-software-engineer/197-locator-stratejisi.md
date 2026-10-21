---
slug: locator-stratejisi
baslik: "Locator Stratejisi: id → Rol/Metin → Stabil CSS → (En Son) XPath"
kademe: Temel
sira: 197
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uçtan uca (e2e) testin her adımı, sayfadaki bir elementi bulmakla başlar. "Giriş yap" butonu nerede? Telefon alanı hangisi? Bu bulma işine **seçici (locator)** denir.

Seçici, testin en kırılgan noktasıdır. Çünkü sayfanın tasarımı, kullandığı çatı (framework) ve iç yapısı sana haber verilmeden değişebilir. İyi bir seçici stratejisi iki şey sağlar. Birincisi, sayfa görsel olarak değişse bile test kırılmaz. İkincisi, sayfanın iç yapısı değiştiğinde test **doğru yerden** ve **tek bir dosyadan** güncellenir.

Bu projede çözüm şöyle olmuş. E-bebek sitesi bir **SAP Spartacus (Angular)** uygulamasıdır. Anlamlı `id` değerleri ve bileşen sınıfları (class) kararlı olduğu için kırılgan XPath zincirlerine hiç ihtiyaç duyulmamıştır.

Tüm CSS ve rol seçicileri tek dosyada toplanmıştır: `pages/locators.js`. Yalnız birkaç metin sabiti kullanıldığı yerde durur; örneğin `'Ürünler Toplamı'` özet satırı. Böylece bir seçici değiştiğinde güncellenecek yer tektir.

### Temel kavramlar

- **Seçici öncelik sırası:** anlamlı **`id`** → **rol/metin tabanlı seçim** (`getByRole`) → **kararlı CSS sınıfı** → (en son çare) **XPath zinciri**. Sıralama rastgele değildir; her basamak bir öncekinden daha kırılgandır.
- **Anlamlı `id`.** `#txtPhoneNumberMobile` veya `#btnLoginWithEmail` gibi, ne işe yaradığı adından anlaşılan kimlikler. Genelde sayfada tekildirler ve kararlıdırlar. Çatı (framework) tarafından üretilmiş olsalar bile, rastgele değil anlamlı isimlendirilmişlerse en güvenilir seçim yoludur.
- **Rol/metin tabanlı seçim (`getByRole`).** Örnek: `page.getByRole('button', { name: /giriş yap/i })`. Bu yöntem elementi görsel veya yapısal konumundan bulmaz. Onu **erişilebilirlik ağacındaki (accessibility tree) rolünden ve görünen metninden** bulur. Ekran okuyucuların kullandığı bilginin aynısını kullanır. Bu yüzden iki faydası vardır: sayfanın iç yapısı değişse bile kırılmaz, ayrıca dolaylı olarak erişilebilirliği de sınar.
- **Kararlı CSS sınıfı.** `.basket-product-item` veya `.old-price` gibi, bileşenin işlevini yansıtan sınıflar. Bunlar çatı tarafından tutarlı biçimde üretilir. Otomatik üretilmiş sınıflardan ayırt edilmelidir; `ng-tns-c123-4` gibi görünenler makine işidir. O tür sınıflar bir sayaçtan gelir, bileşen tipine göre değişir ve sonraki koşumda farklı olabilir.
- **XPath zinciri (en son çare).** `//div[3]/ul/li[2]/a` gibi, sayfa ağacındaki **konuma** dayanan seçim. Kırılgandır, çünkü hiçbir anlamsal çapaya bağlı değildir; ne id'ye, ne role, ne kararlı bir sınıfa. Araya bir `<div>` eklenmesi bile zinciri kırar. Bu projede hiç gerekmemiştir. `pages/locators.js` dosyasının başındaki not bunu açıkça yazar: kararlı `id` ve bileşen sınıfları olduğu için XPath zincirine ihtiyaç duyulmadı.
- **`data-testid` yok.** Bazı projelerde sadece testler için `data-testid` özniteliği eklenir; bu sitede yoktur. Bunun anlamı "site test edilemez" değildir. Anlamı, önceliğin bir sonraki basamağına inmektir: önce anlamlı `id`, sonra rol/metin.
- **Playwright'ın resmî sırası farklıdır.** Playwright belgeleri önce kullanıcıya dönük seçicileri önerir (`getByRole`, `getByLabel`, `getByText`), sonra `getByTestId` gelir; CSS ve id "uygulama detayına bağlı" sayılır. Bu projedeki sıralama bilinçli bir uyarlamadır. Gerekçeleri şunlar: site test ekibinin kontrolünde değildir, `data-testid` yoktur ve Spartacus zaten anlamlı, kararlı id'ler üretir. Ayrıca rol + isim eşleşmesi görünen Türkçe metne bağlıdır; bu yüzden kampanya veya metin değişimine `id`'den daha duyarlıdır. Mülakatta bu farkı **kendin söylemek** en güçlü cevaptır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Locator öncelik hiyerarşisi: id, rol/metin, stabil CSS, XPath en son çare">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Locator öncelik hiyerarşisi</text>
  <rect x="40" y="42" width="130" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="105" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">1. id</text>
  <text x="105" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">#txtPhoneNumberMobile</text>
  <rect x="185" y="42" width="130" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="250" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2. rol/metin</text>
  <text x="250" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">getByRole('button')</text>
  <rect x="330" y="42" width="130" height="60" rx="10" fill="#F5F1E4" stroke="#B26B00" stroke-width="1.5"/>
  <text x="395" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">3. stabil CSS</text>
  <text x="395" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">.basket-product-item</text>
  <rect x="475" y="42" width="130" height="60" rx="10" fill="#F3E1E1" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="540" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">4. XPath</text>
  <text x="540" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">en son çare</text>
  <line x1="170" y1="72" x2="185" y2="72" stroke="#5D7789" stroke-width="1.4" marker-end="url(#locarrow)"/>
  <line x1="315" y1="72" x2="330" y2="72" stroke="#5D7789" stroke-width="1.4" marker-end="url(#locarrow)"/>
  <line x1="460" y1="72" x2="475" y2="72" stroke="#5D7789" stroke-width="1.4" marker-end="url(#locarrow)"/>
  <text x="320" y="130" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">→ sağa gidildikçe kırılganlık artar, anlamsal bağ azalır</text>
  <text x="320" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">e-bebek (Spartacus/Angular) için: id ve bileşen sınıfları kararlı</text>
  <text x="320" y="178" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ 1., 2., 3. basamaklar yeterli; XPath zincirine hiç ihtiyaç duyulmadı</text>
  <defs>
    <marker id="locarrow" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**Neden bu sıralama, tersi değil?** `id` genelde tekildir ve elementin kimliğini doğrudan taşır. Sayfanın görsel tasarımı — renk, boşluk, sıralama — değişse bile `id` çoğunlukla sabit kalır.

`getByRole` bir adım daha soyuttur. Sayfada hangi etiketin kullanıldığına bakmaz; `<button>` da olabilir `<a role="button">` de. Erişilebilirlik ağacındaki **role** ve görünen **metne** bakar. Bu yüzden bir `<div onclick>` yerine gerçek bir `<button>` konulsa bile, rol ve isim aynı kaldıkça seçici kırılmaz.

Kararlı CSS sınıfı, bileşenin işlevsel adını taşıdığı sürece makul bir seçimdir; `.basket-product-item` gibi. Ama çatının otomatik ürettiği karışık sınıflardan ayırt edilmelidir.

XPath zinciri en kırılgandır, çünkü sayfanın **tam konumuna** dayanır. `//div[3]/ul/li[2]/a` ifadesi, araya bir `<div>` eklenince veya sıralama değişince anlamını kaybeder. Hiçbir anlamsal çapası yoktur.

**`data-testid` yok, ama bu sorun değil.** `data-testid`, yalnızca testler için eklenen bir özniteliktir; üretim koduna hiçbir işlevsel etkisi olmaz. Bazı ekipler bunu zorunlu tutar.

Bu projede yoktur, çünkü site test ekibinin kontrolünde değildir; canlı ve üçüncü tarafa ait bir sitedir. Yine de bu bir eksiklik sayılmaz. Önceliğin bir sonraki basamağı aynı güvenceyi verir: Spartacus zaten anlamlı ve kararlı `id` değerleri üretir.

**Seçicilerin tek dosyada toplanması.** `pages/locators.js`, projedeki CSS ve rol tabanlı seçicilerin tek kaynağıdır. Yalnız birkaç metin sabiti kullanıldığı yerde kalır; örneğin sepet özetindeki `'Ürünler Toplamı'` satırı.

Bu dosyayı iki taraf okur: sayfa nesneleri (`LoginPage` gibi) ve hazır adımların arkasındaki `element-registry.js`. Kazanç şudur: "Sepete Ekle" butonunun CSS sınıfı değişirse güncellenecek yer **tek satırdır**. Testin geri kalanına — sayfa nesnelerine, adımlara, özellik dosyalarına — hiç dokunulmaz.

Aşağıdaki gerçek örnek, hem `id` hem rol/metin tabanlı seçimin birlikte kullanıldığını gösterir:

```javascript
// pages/locators.js
login: {
  emailTab: '#btnLoginWithEmail',                        // id
  phoneInput: '#txtPhoneNumberMobile',                    // id
  submitButton: { role: 'button', name: /giriş yap/i },   // rol/metin
},
cart: {
  lineItem: '.basket-product-item',                       // stabil CSS sınıfı
},
```

`utils/locator.js` içindeki `resolveLocator` fonksiyonu bu iki biçimi tek arayüz altında birleştirir: CSS metni ve `{ role, name }` nesnesi. Böylece sayfa nesneleri hangi stratejinin kullanıldığını bilmek zorunda kalmaz; hepsi `this.locator(descriptor)` çağırır.

### Mülakatta ne sorulur?

- **"Locator stratejin ne, neden `data-testid` kullanmadın?"** Beklenen: öncelik sırası anlamlı `id` → rol/metin → stabil CSS sınıfı; `data-testid` kontrol edildi ve sitede yok, ama Spartacus tabanlı olduğu için anlamlı `id`'ler zaten mevcuttu, XPath zincirine hiç gerek kalmadı.
- **"Neden XPath zinciri yerine `id`/rol tercih edilir?"** Beklenen: XPath zinciri DOM'daki konuma dayanır, anlamsal çapası yoktur; bir `<div>` eklenmesi veya sıralama değişimi zinciri kırar. `id` ve rol, elementin kimliğine/işlevine bağlıdır, görsel/yapısal değişime karşı daha dayanıklıdır.
- **"`getByRole` ile CSS sınıfı arasındaki fark ne, ne zaman hangisini seçersin?"** Beklenen: `getByRole` erişilebilirlik ağacı üzerinden çalışır ve sayfadaki etiketten bağımsızdır; dolaylı olarak erişilebilirliği de sınar. CSS sınıfı ise sayfanın iç yapısına daha yakındır. Yine de çatının ürettiği kararlı ve anlamlı sınıflarla kullanılırsa yeterince güvenilirdir.
- **"Tüm selector'ları neden tek dosyada topladın, ayrı ayrı page object'lerde tutmadın?"** Beklenen: bir selector değiştiğinde güncellenecek yerin tek dosya olmasını sağlamak; hem page object'ler hem generic step'lerin arkasındaki element-registry aynı kaynaktan okuyor, tekrarı ve senkronizasyon riskini ortadan kaldırıyor.
- **"Rastgele/otomatik üretilmiş bir CSS sınıfını (ör. `ng-tns-c123-4`) locator olarak kullanır mısın?"** Beklenen: hayır — bu tür sınıflar Angular'ın animasyon altyapısının çalışma zamanında ürettiği sayaçlardır, koşumdan koşuma değişebilir, stabil değildir; yalnız bileşenin işlevsel adını taşıyan, framework tarafından tutarlı üretilen sınıflar (`.basket-product-item` gibi) tercih edilir.
- **"Playwright'ın kendi dokümantasyonu önce `getByRole` öneriyor; sen neden `id` ile başlıyorsun?"** Beklenen: farkın bilincinde olduğunu göstermek. Playwright'ın sırası (`getByRole` → `getByLabel`/`getByText` → `getByTestId`) kullanıcıya dönük seçicileri öne alır ve CSS/id'yi uygulama detayı sayar. Burada sıra bilinçli olarak uyarlandı: site kontrolümüzde değil, `data-testid` yok, Spartacus kararlı ve anlamlı `id`'ler üretiyor; buna karşılık rol+isim eşleşmesi görünen Türkçe metne bağlı olduğu için kampanya/metin değişimine `id`'den daha duyarlı. Kendi ürününde `data-testid` ekleyebiliyorsan Playwright'ın sırası daha doğrudur.
- **"Sayfanın tasarımı değişse seçicilerin ne kadarı kırılır?"** Beklenen: `id` ve rol/metin tabanlı seçiciler görsel değişime dayanıklıdır; kimlik ve işlev korunduğu sürece kırılmazlar. Asıl risk sayfanın iç yapısı değişirse ortaya çıkar; element hiyerarşisi veya sıralama değişince CSS sınıfı ve varsa XPath zincirleri risk altındadır. XPath bu yüzden en son çaredir.

### Tek cümlede

İyi bir seçici stratejisi önce en kararlı yolları kullanır: anlamlı `id` ve erişilebilirlik temelli rol/metin. Konuma dayanan kırılgan XPath zincirlerini yalnız başka seçenek kalmadığında kullanır. Ayrıca tüm seçicileri tek bir kaynakta toplayarak her değişikliği tek noktadan yönetilebilir kılar.

# loc-1 | Temel
## Soru
Bir SAP Spartacus (Angular) tabanlı sitede test yazıyorsun; sitede `data-testid` özniteliği yok ama anlamlı `id`'ler (`#txtPhoneNumberMobile` gibi) ve kararlı bileşen sınıfları (`.basket-product-item` gibi) mevcut. Kabul edilen locator önceliği neye göre sıralanmalıdır?
## A
Anlamlı id → rol/metin tabanlı seçim (`getByRole`) → stabil CSS sınıfı → (en son çare) XPath zinciri
🤖 Doğru: sıralama, elementin kimliğine/işlevine en çok bağlı olandan DOM konumuna dayanan (en kırılgan) olana doğru gider.
## B
XPath zinciri → stabil CSS sınıfı → anlamlı id → rol/metin tabanlı seçim (en özgülden en genele doğru)
🤖 Bu sıralama tam tersi: XPath, DOM konumuna dayandığı için en kırılgan seçimdir, ilk tercih olmamalı.
## C
Yalnızca stabil CSS sınıfı kullanılır; id ve rol tabanlı seçim bu tür sitelerde önerilmez
🤖 CSS sınıfı geçerli bir seçim olabilir ama anlamlı id ve rol tabanlı seçim genelde daha kararlıdır, dışlanmamalı.
## D
Sırasız: hangisi önce bulunursa o kullanılır; seçicinin çalışıyor olması önceliğin kendisinden önemlidir
🤖 Öncelik rastgele değildir; her basamak bir öncekinden daha kırılgandır, bu yüzden bilinçli bir sıralama izlenir.
## Doğru
A
## Açıklama
Locator önceliği rastgele değil, **kırılganlığa göre** sıralanır. Anlamlı `id` genelde tekildir ve elementin kimliğini doğrudan taşır; görsel tasarım değişse bile sabit kalır. Rol/metin tabanlı seçim (`getByRole`) erişilebilirlik ağacına dayanır, DOM etiketinden bağımsızdır. Stabil CSS sınıfı, framework'ün tutarlı ürettiği işlevsel sınıflarla makuldür. XPath zinciri ise DOM'daki **konuma** dayandığı için en kırılgandır — hiçbir anlamsal çapası yoktur, bu yüzden yalnız hiçbir alternatif kalmadığında son çare olarak kullanılır.

# loc-2 | Temel
## Soru
Aşağıdaki iki locator tanımı aynı "Giriş Yap" butonunu hedefliyor. `getByRole('button', { name: /giriş yap/i })` kullanmanın, `//form/div[2]/button[1]` gibi bir XPath zincirine göre temel avantajı nedir?
## A
`getByRole` her zaman `page.locator()` çağrısından daha hızlı çalışır, performans avantajı sağlar
🤖 Fark performans değildir; ikisi de Playwright'ın locator API'sinin bir parçasıdır, hız avantajı iddiası doğru değildir.
## B
`getByRole`, erişilebilirlik ağacındaki role ve görünen metne dayanır; DOM yapısı değişse bile kırılmaz
🤖 Doğru: rol/metin, elementin işlevine bağlıdır; XPath zinciri ise `<div>` eklenmesi gibi yapısal değişimlerde kırılır.
## C
XPath zincirleri Playwright'ta artık desteklenmiyor, bu yüzden rol tabanlı seçim zorunlu hâle gelmiştir
🤖 XPath, Playwright'ta hâlâ desteklenir; sorun teknik imkânsızlık değil kırılganlıktır.
## D
`getByRole` yalnızca `<button>` etiketiyle çalışır; `role="button"` taşıyan bir `<div>` veya `<a>` bu yolla hiç bulunamaz
🤖 Tam tersi: `getByRole` etiketten bağımsız çalışır, `role="button"` taşıyan herhangi bir elementi de bulur.
## Doğru
B
## Açıklama
`getByRole`, elementin DOM'daki konumuna değil **erişilebilirlik ağacındaki rolüne ve görünen adına** bakar — ekran okuyucuların kullandığı aynı bilgidir. Bu yüzden buton `<button>` yerine `role="button"` taşıyan bir `<div>` olsa bile, ya da form içindeki başka bir alan eklenip sıralama değişse bile, rol ve isim aynı kaldığı sürece locator kırılmaz. `//form/div[2]/button[1]` gibi bir XPath zinciri ise tam da bu yapısal konuma bağımlıdır: aradaki bir `<div>`'in eklenmesi veya sırasının değişmesi zinciri geçersiz kılar. Bu yüzden rol/metin tabanlı seçim, DOM konumuna dayanan zincirlerden yapısal olarak daha dayanıklıdır.

# loc-3 | Temel
## Soru
Bir proje, sepet satırlarını `.basket-product-item` CSS sınıfıyla işaretliyor; aynı satırlarda ayrıca `ng-tns-c118-7` gibi Angular'ın kendi ürettiği sınıflar da bulunuyor. Locator olarak hangisi tercih edilmelidir ve neden?
## A
`ng-tns-c118-7`, çünkü Angular'ın kendi ürettiği bir sınıf olduğu için geliştiricinin elle verdiği isimlerden daha kararlı sayılır
🤖 Kaynağın framework olması kararlılık anlamına gelmez; `ng-tns-*` bir sayaçtır, bileşen tipine ve örneğine göre değişir.
## B
İkisi de eşit derecede güvenilirdir; ikisi de sonuçta bir CSS sınıfı olduğu için hangisinin seçildiği fark etmez
🤖 Fark önemlidir: biri bileşenin işlevsel adını taşır, diğeri çalışma zamanında atanan, kararsız bir kimliktir.
## C
`.basket-product-item`, çünkü bileşenin işlevini yansıtan, tutarlı üretilen bir sınıftır; `ng-tns-*` ise kararsızdır
🤖 Doğru: işlevsel adlandırılmış, kararlı sınıflar tercih edilir; otomatik üretilen sayaç sınıfları stabil değildir.
## D
Hiçbiri; bu durumda iki sınıfı birleştirip (`.basket-product-item.ng-tns-c118-7`) seçimi daha kesin hâle getirmek gerekir
🤖 Kararsız bir sınıfı kararlı olanla birleştirmek seçimi kesinleştirmez, kararsızlığı ona da bulaştırır.
## Doğru
C
## Açıklama
`.basket-product-item` gibi bir sınıf, bileşenin **işlevini** yansıtan ve genelde framework/geliştirici tarafından bilinçli, tutarlı biçimde verilen bir isimdir — derlemeler arasında değişmesi beklenmez. Buna karşılık `ng-tns-c118-7` gibi sınıflar Angular'ın **animasyon** altyapısının (transition namespace) çalışma zamanında ürettiği bir sayaçtır; bileşen tipine ve örneğine göre değişir, sonraki koşumda/derlemede farklı olabilir. (Angular'ın **view encapsulation** mekanizması ise sınıf değil `_ngcontent-*`/`_nghost-*` **öznitelikleri** üretir — ikisi karıştırılmamalı.) Locator seçerken "bu sınıf bir isim mi, yoksa bir üretim artefaktı mı?" sorusunu sormak, stabil olanı ayırt etmenin pratik yoludur. Kararsız sınıfı kararlı olanla birleştirmek (D) de çare değildir: bileşik seçici, en kararsız parçası kadar kararlıdır.

# loc-4 | Temel
## Soru
**Kontrolünde olmayan, üçüncü taraf bir sitede** `data-testid` özniteliği hiçbir elementte tanımlı değil ve geliştirici ekibine erişimin yok. Bu durumda doğru yaklaşım hangisidir?
## A
Site "test edilemez" kabul edilip uçtan uca otomasyon projesinden tamamen vazgeçilir
🤖 `data-testid` yokluğu test edilemezlik anlamına gelmez; hiyerarşinin diğer basamakları kullanılabilir.
## B
Doğrudan tüm elementler için XPath zinciri yazılır, çünkü `data-testid` olmadan başka güvenilir seçenek kalmaz
🤖 XPath zinciri hiyerarşinin en kırılgan basamağıdır; önce anlamlı id ve rol/metin denenmeden ona atlamak gerekçesizdir.
## C
Hiyerarşinin bir sonraki basamağına inilir: önce anlamlı id, sonra rol/metin, sonra stabil CSS sınıfı
🤖 Doğru: `data-testid` yokluğu, hiyerarşideki diğer güvenilir basamaklara (id, rol, stabil sınıf) geçmeyi gerektirir.
## D
Sitenin geliştiricilerinden `data-testid` eklemeleri istenir; öznitelikler eklenene kadar test yazımı ertelenir
🤖 Kendi ürününde doğru yaklaşım budur; ama kontrolünde olmayan bir sitede uygulanamaz.
## Doğru
C
## Açıklama
`data-testid`, bazı projelerde test kolaylığı için özel olarak eklenen bir özniteliktir, ama her sitede bulunması beklenmez — özellikle test ekibinin kontrolünde olmayan, üçüncü taraf bir sitede. Bu yokluk bir engel değil, locator hiyerarşisinin bir sonraki basamağına inmeyi gerektirir: önce anlamlı `id` (genelde framework tarafından zaten üretilmiştir), sonra rol/metin tabanlı seçim, sonra stabil CSS sınıfı. Yalnızca bunların hiçbiri yoksa (nadir durum) XPath zinciri son çare olarak devreye girer. Doğrudan XPath'e atlamak (B) veya projeden vazgeçmek (A) hiyerarşiyi atlamaktır. Geliştiricilerden `data-testid` eklemelerini istemek (D) **kendi ürününde** en doğru yaklaşımdır — sektör pratiği de budur; ama burada site üçüncü tarafın, o kanal kapalıdır.

# loc-5 | Temel
## Soru
Projede "giriş yap butonu" `{ role: 'button', name: /giriş yap/i }` tanımıyla seçiliyor; `utils/locator.js` bunu `page.getByRole(role, { name, exact })` çağrısına aktarıyor. `getByRole`'da **isim eşleşmesinin** davranışı hakkında hangisi doğrudur?
## A
İsim string ise eşleşme harf duyarsız alt-dize (substring) aramasıdır; `exact: true` tam eşleşme yapar, regex verilirse `exact` yok sayılır
🤖 Doğru: varsayılan gevşektir, `exact: true` daraltır; regex zaten kendi kalıbını taşıdığı için `exact` dikkate alınmaz.
## B
İsim eşleşmesi varsayılan olarak tam ve harf duyarlıdır; `exact: false` verilmedikçe "Giriş Yap" yazan bir buton `/giriş yap/i` kalıbıyla hiç bulunamaz
🤖 Varsayılan tersidir: `exact` verilmediğinde eşleşme harf duyarsız ve alt-dize temellidir; ayrıca regex'te `exact` rol oynamaz.
## C
`exact` yalnızca regex kalıplarıyla anlamlıdır; düz bir string verildiğinde Playwright her zaman tam ve harf duyarlı eşleşme arar
🤖 Tam tersi: `exact` string'lerde işler, regex'te yok sayılır; string'in varsayılanı da tam eşleşme değildir.
## D
`name`, elementin erişilebilir adına değil HTML `name` özniteliğine bakar; `exact` de yalnız bu öznitelik karşılaştırmasını etkiler
🤖 `getByRole`'un `name`'i erişilebilirlik ağacındaki *erişilebilir ad*'dır (metin, `aria-label`, `<label>`), HTML `name` özniteliği değil.
## Doğru
A
## Açıklama
`getByRole`'un `name` seçeneği elementin **erişilebilir adına** (görünen metin, `aria-label`, ilişkili `<label>`) bakar. Bu isim bir **string** olarak verilirse Playwright varsayılan olarak harf duyarsız, boşlukları kırpılmış bir **alt-dize** araması yapar; `exact: true` verildiğinde eşleşme harf duyarlı ve tam-dize hâline gelir. İsim bir **regex** ise `exact` tamamen yok sayılır — kalıbın kendisi zaten neyin eşleşeceğini belirler. Projede `submitButton: { role: 'button', name: /giriş yap/i }` tanımının regex olması bilinçlidir: canlı sitedeki butonun metni "Giriş Yap", "GİRİŞ YAP" veya etrafında ek boşlukla gelebilir; `/i` bayrağı bu varyasyonları tek kalıpta emer. `resolveLocator` yine de `exact`'i ileri taşır, çünkü düz string ile tanımlanacak bir gelecek locator'da ("Sil" gibi kısa bir metnin başka bir butona da uyması riskine karşı) daraltma gerekebilir.

# loc-6 | Temel
## Soru
Bir mülakatçı soruyor: "Kırılgan XPath zincirlerinden neden kaçındın?" En doğru cevap hangisidir?
## A
XPath yalnız sunucudan gelen statik HTML'de çalışır; Angular gibi tek sayfa uygulamalarında JavaScript ile sonradan oluşan DOM'da sonuç döndüremez
🤖 XPath, o anki DOM üzerinde çalışır; içeriğin sunucudan mı JavaScript'ten mi geldiği eşleşmeyi etkilemez.
## B
XPath ile bulunan elementler Playwright'ın otomatik beklemesinden yararlanamaz; her XPath seçicisi için elle `waitFor` yazmak gerekir
🤖 Otomatik bekleme locator'ın hangi sözdizimiyle yazıldığından bağımsızdır; XPath locator'ları da aynı beklemeyi alır.
## C
XPath zincirleri her zaman `id` tabanlı seçimlerden daha yavaş çalışır ve testin süresini gözle görülür biçimde uzatır
🤖 Ana kaygı performans değil kırılganlıktır; asıl sorun ilgisiz DOM değişikliklerinde bile kırılma riskidir.
## D
XPath zinciri DOM'daki konuma dayanır; ara bir element eklenmesi veya sıralama değişimi gibi işlevsel olmayan değişikliklerde bile kırılır
🤖 Doğru: XPath zinciri anlamsal bir çapaya değil yapısal konuma bağlıdır, bu yüzden ilgisiz görünen değişikliklere karşı kırılgandır.
## Doğru
D
## Açıklama
XPath zinciri (`//div[3]/ul/li[2]/a` gibi) elementi **DOM ağacındaki konumuna** göre bulur; hiçbir anlamsal çapaya (id, rol, kararlı sınıf adı) bağlı değildir. Bu, sayfanın **görsel** olarak hiç değişmediği durumlarda bile — örneğin geliştiricinin ilgisiz bir `<div>` sarmalayıcı eklemesi, bir stil düzeltmesi için hiyerarşiyi yeniden düzenlemesi gibi — zincirin kırılabileceği anlamına gelir. Bu kırılganlık, testin bakım maliyetini artırır: her ilgisiz DOM değişikliğinde testler kırılır ve gerçek bir hatayı mı yoksa yalnız bir yapı değişikliğini mi gösterdiğini ayırt etmek zaman alır. Bu yüzden XPath zinciri yalnız hiçbir alternatif kalmadığında son çare sayılır. Bu projede o noktaya hiç gelinmedi: `pages/locators.js` başlığındaki not, Spartacus'ün kararlı `id`'leri ve bileşen sınıfları sayesinde XPath zincirine ihtiyaç duyulmadığını açıkça yazar.
