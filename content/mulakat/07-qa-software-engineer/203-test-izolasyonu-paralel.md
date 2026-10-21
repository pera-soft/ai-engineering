---
slug: test-izolasyonu-paralel
baslik: "Test İzolasyonu ve Paralel Koşum"
kademe: İleri
sira: 203
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Vaka çalışmasının (case study) gereksinimi netti: senaryolar en az 2 işçi süreçle (worker) paralel koşabilmeli ve sonuç her koşumda aynı çıkmalı. Ayrıca senaryolar arası veri veya oturum sızıntısı olmamalı. Bu tek cümle aslında iki ayrı problemi işaret eder.

Birincisi **hız**. Senaryoları tek tek, sırayla koşturmak koşum süresini gereksiz uzatır.

İkincisi ve daha kritik olanı **doğruluk**. Senaryolar aynı anda koşarken birbirinin çerezini, oturumunu veya sepetini görüp ezebilir. O zaman testler öngörülebilir (deterministik) olmaktan çıkar. Bir senaryonun geçip geçmemesi, o an hangi senaryolarla birlikte koştuğuna bağlı hâle gelir.

Bu projede izolasyon özellikle önemlidir. Testler canlı ve gerçek bir e-ticaret sitesine karşı koşuyor. Giriş gerektiren senaryolar da **aynı test hesabını** paylaşıyor. Ortada sıfırlanabilecek bir test veritabanı yok; izolasyon yalnızca test kodunun kendi tasarımıyla sağlanabilir.

### Temel kavramlar

- **Süreç seviyesi izolasyon (işçi süreç / worker)**: Cucumber'ın `parallel: N` ayarı senaryoları ayrı işletim sistemi süreçlerine dağıtır (`WORKERS` ortam değişkeniyle artırılabilir). Bu katman **eşzamanlılığı** belirler, yani hangi senaryoların aynı anda koşacağını.
- **Senaryo seviyesi izolasyon (BrowserContext / tarayıcı bağlamı)**: `Browser` pahalı bir kaynaktır, çünkü yeni bir tarayıcı süreci başlatır. `BeforeAll` ile **her işçi sürecinde** bir kez açılır (2 işçi = 2 tarayıcı süreci); senaryo başına yeniden açılmaz. `BrowserContext` ise ucuzdur. En kolay benzetmesi tarayıcıdaki gizli sekmedir (incognito): kendi çerezi, kendi `localStorage`'ı ve kendi sekmesi vardır, ama alttaki tarayıcı motorunu paylaşır. Her senaryo kendi bağlamını alır ve senaryo bitince o bağlam kapatılır.
- **World nesnesi (Cucumber)**: her senaryo için ayrı örneği oluşturulan sınıf. Senaryoya özel durumu (`this.context`, `this.page`, `this.state`) taşır. Adımlar arasında taşınan veri (seçilen ürünler, ölçülen tutarlar) modül seviyesinde bir değişkende değil, burada tutulur.
- **Koşum çıktısı toplarken `try/finally`**: koşum çıktısı (artefakt) demek ekran görüntüsü ve izleme kaydıdır (trace). Bu çıktıları toplamak hata verse bile, bağlamın kapatılması garanti altına alınır.
- **Paylaşılan hesapla çakışma riski**: aynı test hesabıyla aynı anda sepet işlemi yapmak, oturuma bağlı bir kaynak üzerinde yarış durumuna (race condition) yol açabilir. Bu risk altyapıyla değil, **senaryo tasarımıyla** önlenir.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Her worker process'i kendi tarayıcısını açar, her senaryo kendi BrowserContext'ini alır">
  <text x="320" y="22" text-anchor="middle" font-size="13.5" font-weight="700" fill="#1C3247" font-family="system-ui">Her worker kendi tarayıcısını açar, her senaryo kendi context'ini alır</text>
  <text x="320" y="42" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">parallel: 2 → iki ayrı Node process'i, iki ayrı Chromium</text>
  <rect x="24" y="56" width="284" height="176" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="166" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Worker 1 (ayrı Node process'i)</text>
  <rect x="42" y="92" width="248" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="166" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Browser (BeforeAll — bu worker'da bir kez)</text>
  <text x="166" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kendi Chromium process'i</text>
  <text x="166" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Senaryo A → kendi BrowserContext</text>
  <text x="166" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Senaryo B → kendi BrowserContext</text>
  <text x="166" y="198" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">senaryolar bu worker içinde sırayla koşar</text>
  <text x="166" y="216" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çerez / oturum / sepet paylaşılmaz</text>
  <rect x="332" y="56" width="284" height="176" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="474" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Worker 2 (ayrı Node process'i)</text>
  <rect x="350" y="92" width="248" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="474" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Browser (BeforeAll — bu worker'da bir kez)</text>
  <text x="474" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kendi Chromium process'i</text>
  <text x="474" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Senaryo C → kendi BrowserContext</text>
  <text x="474" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Senaryo D → kendi BrowserContext</text>
  <text x="474" y="198" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">senaryolar bu worker içinde sırayla koşar</text>
  <text x="474" y="216" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çerez / oturum / sepet paylaşılmaz</text>
</svg>
```

### Nasıl çalışır?

**Process seviyesi (`cucumber.js`).** Varsayılan koşum konfigürasyonu:

```js
module.exports = {
  default: { ...common, parallel: Number(process.env.WORKERS || 2) },
  serial: { ...common, parallel: 0 },
  parallel: { ...common, parallel: Number(process.env.WORKERS || 2) },
};
```

`WORKERS=4` teknik olarak çalışır ve denenmiştir. Yine de varsayılan değer 2'de bırakılmıştır. Gerekçe şu: testler canlı siteye karşı koşuyor. Daha fazla eşzamanlı istek hem siteye yük bindirir, hem de aynı hesabı kullandığımız için gereksiz risk yaratır. Ayrı bir test ortamı olsaydı bu sayı artırılabilirdi.

**Senaryo seviyesi (`features/support/world.js` ve `hooks.js`).** `Browser` her işçi sürecinde bir kez açılır. Nedeni şu: cucumber-js her işçiyi ayrı bir Node alt süreci olarak başlatır ve `hooks.js` dosyasını baştan yükler. Bu yüzden `BeforeAll` kancası her işçide bir kez çalışır:

```js
BeforeAll(async function () {
  browser = await chromium.launch({ headless: config.headless, slowMo: config.slowMo });
  writeAllureEnvironment();
});

Before(async function () {
  await this.openBrowser(browser);
});
```

`openBrowser` ise her senaryo için ayrı bir `BrowserContext` (tarayıcı bağlamı) açar:

```js
async openBrowser(browser) {
  this.context = await browser.newContext({ locale: config.locale, viewport, ... });
  this.context.setDefaultTimeout(config.defaultTimeout);
  this.page = await this.context.newPage();
  this.pages = { home: new HomePage(this.page), login: new LoginPage(this.page), /* ... */ };
}
```

World sınıfının kendi yorum satırı bunu net özetliyor. Yorum kodda İngilizce yazılmış; Türkçesi şu: "Her senaryo kendi World örneğini alır, üstüne bir de kendi tarayıcı bağlamını alır. Böylece çerez, oturum ve sepet bir senaryodan diğerine asla sızmaz. Paralel koşumu güvenli kılan şey budur."

**`After` kancasındaki `try/finally` düzeltmesi.** Kanca (hook), senaryonun öncesinde ya da sonrasında otomatik çalışan koddur. Koşum çıktısı toplamak (ekran görüntüsü, izleme kaydı) hata fırlatabilir. Bu hata, bağlamın kapatılmasını engellememelidir:

```js
After(async function ({ pickle, result }) {
  try {
    if (failed && this.page) { /* ekran görüntüsü al */ }
  } catch (error) { console.warn(...); }

  try {
    if (this.context) { /* trace durdur, ekle */ }
  } catch (error) {
    console.warn(...);
  } finally {
    await this.closeBrowser();
  }
});
```

REVIEW.md bu düzeltmenin nedenini belgeliyor. Ekran görüntüsü veya izleme kaydı toplarken bir hata çıkarsa `closeBrowser()` hiç çalışmıyordu. Sayfası çökmüş bir senaryo, işçi sürecinin kalan koşumu boyunca açık bir bağlam bırakıyordu. Artık çıktı toplama `try/finally` içinde.

**Paylaşılan hesapla çakışma.** Sepet, oturuma bağlı bir kaynaktır. İki senaryo aynı anda giriş yapıp sepete dokunsaydı çakışabilirdi. Çözüm bir kilitleme mekanizması değil, bir **tasarım kararıdır**: giriş gerektiren senaryolar arasında sepeti değiştiren tek bir senaryo vardır; sepet matematiği ise misafir kullanıcı olarak koşar.

Veri sızıntısı olmadığının garantisi de yapısaldır. Sızıntı zaten mümkün değildir: her senaryo kendi `BrowserContext` bağlamını alır ve bu bağlam senaryo sonunda kapatılır. Bağlamlar çerezi ve tarayıcı deposunu (storage) paylaşmaz.

### Mülakatta ne sorulur?

- **"İzolasyonu nasıl kurdun?"** Beklenen: iki katman — process (worker, `parallel`/`WORKERS`) eşzamanlılığı, BrowserContext (senaryo başına, `world.js`) veri izolasyonunu sağlar.
- **"Browser'ı neden her senaryoda yeniden açmıyorsun?"** Beklenen: Browser açmak pahalı (yeni process); Context açmak ucuz. Pahalı kaynak **worker içinde** paylaşılır (her worker kendi tarayıcısını açar), izolasyon ucuz katmanda kurulur.
- **"İki worker'dan fazlasını denedin mi?"** Beklenen: `WORKERS=4` çalışıyor; varsayılan 2, çünkü canlı siteye yük bindirmemek ve paylaşılan hesabı korumak için.
- **"Aynı hesapla paralel koşuyorsun, çakışma olmuyor mu?"** Beklenen: sepet oturuma bağlı; giriş gerektiren tek senaryo sepete dokunuyor, sepet matematiği misafir sepetiyle test ediliyor.
- **"Senaryolar arasında veri sızıntısı olmadığını nasıl biliyorsun?"** Beklenen: yapısal garanti — her senaryo kendi BrowserContext'ini alıp senaryo sonunda kapatıyor; context'ler çerez/storage paylaşmıyor, ayrıca modül seviyesinde paylaşılan durum yok.
- **"After hook'ta neden try/finally var?"** Beklenen: artefakt (ekran görüntüsü/trace) toplama sırasında oluşan bir hata, context'in kapatılmasını engellememeli; aksi halde context sızıntısı olur.

### Tek cümlede

Test izolasyonu iki katmanda kurulur: **süreç seviyesinde** eşzamanlılık bilinçli olarak sınırlı tutulur, **senaryo seviyesinde** ise her senaryonun kendi tarayıcı bağlamı veri sızıntısını yapısal olarak imkânsız kılar. Çıktı toplama `try/finally` ile güvenceye alınır; paylaşılan hesap riski ise kilitle değil **senaryo tasarımıyla** yönetilir.

# izol-1 | Orta
## Soru
`cucumber.js` dosyasında `parallel: Number(process.env.WORKERS || 2)` tanımlı; `features/support/world.js` içinde ise her senaryo kendi `browser.newContext()`'ini alıyor. Bu iki mekanizma birlikte nasıl bir izolasyon modeli kurar?
## A
Tek katmanlı bir model: worker sayısı context sayısını otomatik olarak belirler, ikisi pratikte aynı kavramdır
🤖 Worker ve context farklı kavramlardır; bir worker, birden çok senaryoyu (dolayısıyla birden çok context'i) sırayla koşturabilir.
## B
Yalnızca senaryo seviyesinde izolasyon sağlanır; process seviyesinde ekstra bir izolasyon yoktur, tüm senaryolar tek process'te sırayla koşar
🤖 `parallel` ayarı tam olarak process seviyesinde bir eşzamanlılık/izolasyon katmanı ekler; tüm senaryolar tek process'te koşmaz.
## C
İki ayrı katman: worker'lar hangi senaryoların eşzamanlı koştuğunu, BrowserContext ise her senaryonun verisinin izolasyonunu belirler
🤖 Doğru: process izolasyonu eşzamanlılığı, context izolasyonu ise senaryolar arası veri sızıntısını önler — ikisi farklı sorunları çözer.
## D
İzolasyon veritabanı seviyesinde sağlanır: her worker koşum başında kendi test veritabanı şemasını oluşturur ve senaryolar bu şema üzerinden birbirinden ayrılır
🤖 Bu projede test edilen canlı bir web sitesi; ayrı bir test veritabanı yok. İzolasyon tarayıcı context'i seviyesinde sağlanır.
## Doğru
C
## Açıklama
İzolasyon iki bağımsız katmanda kurulur. Process seviyesinde Cucumber'ın `parallel` ayarı (varsayılan 2, `WORKERS` ile artırılabilir) senaryoları ayrı worker process'lerine dağıtır — bu, hangi senaryoların **aynı anda** koşacağını belirler. Senaryo seviyesinde ise her senaryo `world.js` içindeki `openBrowser()` ile kendi `BrowserContext`'ini alır ve senaryo bitince kapatılır; bu da o senaryonun çerezlerinin, oturumunun ve sepetinin başka hiçbir senaryoyla paylaşılmamasını sağlar. İki katman farklı sorunları çözer: yalnız context izolasyonu olsaydı ama tüm senaryolar tek worker'da koşsaydı paralellik kazanılmazdı; yalnız worker paralelliği olsaydı ama context paylaşılsaydı senaryolar birbirinin çerezini/sepetini görürdü.

# izol-2 | İleri
## Soru
`features/support/hooks.js`'te tarayıcı `BeforeAll` içinde bir kez açılıyor (`browser = await chromium.launch(...)`), `world.js`'teki `openBrowser()` ise her senaryodan önce `browser.newContext()` çağırıyor. Bu ayrımın (Browser bir kez, Context senaryo başına) gerekçesi nedir?
## A
Browser açmak pahalıdır (yeni bir tarayıcı process'i başlatır); BrowserContext ise gizli sekme gibi ucuz bir oturumdur
🤖 Doğru: Browser'ı her senaryoda yeniden açmak koşum süresini ciddi uzatırdı; Context açıp kapatmak milisaniyeler mertebesindedir ve izolasyonu ucuza sağlar. Pahalı kaynak worker içinde paylaşılır, izolasyon ucuz katmanda kurulur.
## B
Browser'ı tekrar açmak Playwright API'sinde desteklenmiyor, bu yüzden BeforeAll'da bir kez açılmak zorunda
🤖 API bunu destekler (`chromium.launch` her yerde çağrılabilir); tercih performans gerekçesiyle yapılmış bir tasarım kararıdır, API kısıtlaması değil.
## C
BrowserContext her senaryoda yeniden açılırsa Allure raporunda ayrı bir "tarayıcı açıldı" adımı oluşur, bu da raporu şişirir
🤖 Allure adım sayısı bu kararın gerekçesi değil; asıl gerekçe kaynak maliyeti ve izolasyon ihtiyacıdır.
## D
Context paylaşılırsa senaryolar arası veri sızıntısı garanti olur; bu yüzden Browser da her senaryoda yeniden açılmak zorunda kalır
🤖 Context zaten senaryo başına açılıp kapatıldığı için veri sızıntısı önleniyor; bu, Browser'ın da yeniden açılmasını gerektirmez.
## Doğru
A
## Açıklama
Bir `Browser` örneği açmak (`chromium.launch`) yeni bir tarayıcı process'i başlatmak demektir — bu, saniyeler mertebesinde bir maliyettir ve bu maliyeti her senaryoda tekrar ödemek koşum süresini ciddi biçimde uzatırdı. Bir `BrowserContext` ise aynı tarayıcı process'i içinde açılan izole bir oturumdur — "gizli sekme (incognito) gibi" düşünülebilir: kendi çerezine, `localStorage`'ına ve sekmelerine sahiptir ama alttaki tarayıcı motorunu paylaşır. `hooks.js` bu yüzden `Browser`'ı `BeforeAll`'da — her worker process'inde bir kez — açıp `AfterAll`'da kapatır (pahalı kaynak worker içindeki tüm senaryolarca paylaşılır), `world.js` ise her senaryo öncesi yeni bir `BrowserContext` açar ve senaryo sonunda kapatır (izolasyon ucuz katmanda kurulur). Bu ayrım hem hız hem izolasyon kazandırır — biri diğerinden feragat gerektirmez.

# izol-3 | Orta
## Soru
`EbebekWorld` sınıfı `this.state = {}` ile başlıyor ve adımlar arası veri (seçilen ürünler, ölçülen tutarlar) modül seviyesinde bir değişken yerine bu `this.state` üzerinde tutuluyor. Bunun paralel koşumdaki önemi nedir?
## A
`this.state` yalnızca performans amaçlı bir önbellektir (cache); paralel koşumla doğrudan ilgisi yoktur
🤖 İlgisi var: modül seviyesinde bir değişken kullanılsaydı, aynı process içinde art arda koşan senaryolar bu değişkeni paylaşır, biri diğerinin verisini ezerdi.
## B
Cucumber World nesnesi zorunlu bir arayüzdür (interface); `this.state` olmadan adım fonksiyonları derlenmez
🤖 World'ün kendisi zorunludur ama `state` adlı bir alan tutmak Cucumber'ın dayattığı bir kural değil, bu projenin tercih ettiği bir kalıptır.
## C
`this.state`, senaryolar arası veri paylaşımını kolaylaştırmak için tasarlanmıştır; bir senaryonun seçtiği ürün bir sonrakine de aktarılabilir
🤖 Tam tersi: her senaryo kendi `World` örneğini (dolayısıyla kendi `state`'ini) alır; amaç paylaşım değil, senaryolar arası veri sızıntısını önlemektir.
## D
Her senaryo kendi `World` örneğini, dolayısıyla kendi `this.state`'ini aldığı için senaryolar arası veri karışması imkânsız hale gelir
🤖 Doğru: World, Cucumber'ın senaryo-özel durum taşıma mekanizmasıdır; `this.state` bu izolasyonu adımlar arası veri için de genişletir — modül seviyesinde paylaşılan bir değişken olsaydı bu garanti kaybolurdu.
## Doğru
D
## Açıklama
Cucumber, her senaryo için ayrı bir `World` örneği oluşturur — `EbebekWorld` bu örneğin projeye özel genişletmesidir ve `this.context`, `this.page`, `this.state` gibi alanları taşır. Adımlar arası veri (hangi ürün seçildi, hangi tutar ölçüldü) modül seviyesinde bir değişkende tutulsaydı, aynı process içinde ardışık koşan senaryolar bu değişkeni **paylaşırdı** — biri diğerinin verisini ezebilir veya bir önceki senaryodan kalan veriyi okuyabilirdi. `this.state` bunu yapısal olarak imkânsız kılar: her senaryonun kendi `World`'ü, dolayısıyla kendi `state`'i vardır ve senaryo bitince o örnekle birlikte atılır.

# izol-4 | İleri
## Soru
`features/support/hooks.js`'teki `After` hook'unda artefakt (ekran görüntüsü, trace) toplama kodu `try { ... } finally { await this.closeBrowser(); }` biçiminde yazılmış. REVIEW.md bunun bir düzeltme olduğunu not ediyor: "…sayfası çökmüş bir senaryo, worker'ın kalan koşumu boyunca açık context bırakıyordu." Bu hatanın kök nedeni neydi?
## A
`closeBrowser()` fonksiyonunun kendisinde bir mantık hatası vardı; context'i kapatmak yerine yalnızca sayfayı kapatıyordu
🤖 `closeBrowser()`'ın kendi mantığı doğruydu (`context.close()`); sorun onun hiç çağrılmamasıydı, yanlış çalışmasında değildi.
## B
Artefakt toplama hata fırlattığında, hemen ardından gelen `closeBrowser()` çağrısına hiç ulaşılmıyordu
🤖 Doğru: `finally` olmadan, ekran görüntüsü veya trace toplama sırasında (ör. sayfa çökmüşse) atılan bir hata, sonraki satırdaki `closeBrowser()`'ı hiç çalıştırmadan fonksiyonu terk ediyordu; `finally`'ye taşımak bunu garanti altına aldı.
## C
`Status.FAILED` kontrolü yanlış yazıldığı için başarısız senaryolar `After` hook'unu hiç tetiklemiyordu
🤖 `After` hook her senaryoda (başarılı/başarısız fark etmeksizin) tetiklenir; sorun hook'un tetiklenmemesi değil, hook içindeki bir adımın hata fırlatıp devamını engellemesiydi.
## D
`BeforeAll`'daki `browser` değişkeni `After` hook'undan erişilemediği için context hiç kapatılamıyordu
🤖 `browser` erişim sorunu yoktu; `context` zaten `this.context` üzerinden (World üzerinden) erişiliyordu, sorun erişim değil hata sonrası akıştı.
## Doğru
B
## Açıklama
`After` hook'u önce başarısız senaryolar için ekran görüntüsü alıyor, sonra trace'i durdurup dosyaya yazıyor, en sonunda `closeBrowser()` ile context'i kapatıyor. Bu adımlar art arda, `try/finally` olmadan yazılmış olsaydı, örneğin sayfa çökmüş bir senaryoda `this.page.screenshot()` çağrısı hata fırlatabilir ve fonksiyon bu hatayla birlikte **hemen sonlanırdı** — `closeBrowser()`'a hiç sıra gelmezdi. Sonuç, açık kalan bir `BrowserContext`; bu context o worker'ın kalan koşumu boyunca bellekte kalır, birikir ve kaynak sızıntısına yol açar. `try { /* ekran görüntüsü + trace */ } finally { await this.closeBrowser(); }` yapısı, artefakt toplama ne olursa olsun (başarılı ya da hatalı) context'in **her koşulda** kapatılmasını garanti eder.

# izol-5 | Orta
## Soru
`cucumber.js`'te varsayılan `parallel` değeri 2'dir (`WORKERS=4 npm run test:parallel` ile artırılabilir). Ekip WORKERS=4'ü test edip çalıştığını doğrulamış olmasına rağmen 2'yi varsayılan bırakmış. Gerekçesi nedir?
## A
Playwright lisansı eşzamanlı en fazla 2 tarayıcı context'ine izin veriyor, 4 worker'da bazı context'ler açılamıyor
🤖 Böyle bir lisans kısıtlaması yok; context sayısı sistem kaynaklarıyla sınırlıdır, Playwright kendisi bir üst sınır dayatmaz.
## B
4 worker'da Allure sonuç dosyaları aynı dizine eşzamanlı yazıldığı için rapor doğru sırada üretilemiyor ve senaryo sonuçları karışıyor
🤖 Allure sonuçları senaryo/pickle kimliğine göre ayrıştırılır, worker sayısından bağımsız doğru üretilir; bu, gerekçe değil.
## C
Testler canlı siteye karşı koşuyor; daha fazla eşzamanlı istek hem siteye yük bindirir hem paylaşılan hesap için gereksiz risk taşır
🤖 Doğru: canlı/paylaşılan ortama karşı koşarken eşzamanlılığı bilinçli olarak sınırlı tutmak, hem nezaket hem risk yönetimidir; ayrı bir test ortamı olsaydı worker sayısı artırılırdı.
## D
4 worker açıldığında `BeforeAll` hook'u dört kez tetiklenip dört ayrı tarayıcı açmaya çalışıyor, bu da bellek taşmasına yol açıyor
🤖 `BeforeAll` her worker process'inde bir kez çalışır, bu beklenen ve desteklenen davranıştır; bellek taşması gözlenen bir sorun değil.
## Doğru
C
## Açıklama
`WORKERS=4` teknik olarak çalışıyor — mimari buna hazır. Varsayılanın 2'de bırakılmasının nedeni teknik bir sınır değil, bir **risk kararı**: testler gerçek, canlı bir siteye karşı koşuyor ve giriş gerektiren senaryolar aynı test hesabını paylaşıyor. Daha yüksek eşzamanlılık hem canlı siteye gereksiz yük bindirir hem de paylaşılan hesap üzerinde çakışma riskini artırır. Ayrı/izole bir test ortamı (staging) olsaydı worker sayısı rahatlıkla artırılabilirdi; canlı ortama karşı koşarken 2, hız ile risk arasında bilinçli seçilmiş bir denge noktasıdır.

# izol-6 | İleri
## Soru
Sepet, kullanıcının oturumuna bağlı; giriş gerektiren senaryolar aynı test hesabını kullanıyor. İki senaryo aynı anda giriş yapıp sepete dokunsaydı çakışma riski olurdu. Proje bu riski nasıl yönetiyor?
## A
Giriş gerektiren senaryolardan yalnızca biri sepete dokunuyor; sepet matematiği doğrulamaları misafir sepeti üzerinden koşuyor
🤖 Doğru: giriş gerektiren tek senaryo sepete dokunduğu için aynı hesapta iki senaryonun eşzamanlı sepet değişikliği yapması tasarım gereği oluşmuyor; ekleme/adet/silme kontrolleri giriş yapmadan koşar.
## B
Cucumber, aynı etikete (`@auth`) sahip senaryoları otomatik olarak ayrı worker'lara değil aynı worker'a atayıp sıraya sokuyor
🤖 Cucumber böyle bir otomatik etiket-bazlı sıralama yapmaz; dağıtım worker sayısına göredir, etikete göre değil.
## C
Her senaryo başında test hesabının şifresi otomatik değiştirilip önceki oturumlar geçersiz kılınıyor
🤖 Böyle bir şifre rotasyonu yok ve bu, hesabı diğer senaryolar için de kilitleyerek yeni sorunlar yaratırdı.
## D
Giriş gerektiren senaryolar koşuma başlamadan veritabanı seviyesinde bir kilit (lock) alıp aynı hesabı kullanan diğer senaryoları sırada bekletiyor
🤖 Proje bir test veritabanına erişmiyor (canlı site); kilitleme mekanizması yok, çözüm senaryo tasarımı seviyesinde.
## Doğru
A
## Açıklama
Sepet sunucu tarafında oturuma bağlı bir kaynaktır; aynı hesapla eşzamanlı iki senaryo sepete dokunsaydı biri diğerinin değişikliğini görebilir veya ezebilirdi. Proje bunu altyapı (kilit, ayrı hesap havuzu) yerine **senaryo tasarımıyla** çözüyor: giriş gerektiren senaryolardan yalnızca biri sepet üzerinde işlem yapıyor, sepet matematiğini (ekleme, adet artırma, silme) doğrulayan senaryolar ise **misafir** (giriş yapılmamış) sepeti üzerinden koşuyor — bu sepet oturuma değil tarayıcı context'ine bağlı olduğu için paralel senaryolar arasında zaten izole. Ölçek büyüyüp giriş gerektiren sepet senaryosu sayısı artarsa, ekip bunun bir hesap havuzu gerektireceğini kabul ediyor.
