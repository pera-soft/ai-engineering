---
slug: raporlama-allure-hooks-tag
baslik: "Allure Raporlama, Cucumber Hooks ve Tag Stratejisi"
kademe: Orta
sira: 205
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Vaka çalışmasının (case study) raporlama için koştuğu iki şart nettir. Birincisi: senaryolar anlamlı etiketlerle gruplanacak, senaryo öncesi ve sonrası çalışan kancalar kullanılacak, başarısız senaryolarda ekran görüntüsü otomatik olarak rapora eklenecek. İkincisi: Allure raporu adım açıklamalarını, ortam bilgisini ve hata anındaki ekran görüntüsünü içerecek.

Bu bir "güzel görünsün" isteği değildir. Mülakatı yapan kişinin önüne açacağı asıl kanıt katmanı budur. Bir testin "geçti" demesi yetmez. Test, **neden** geçtiğini, hangi ortamda koştuğunu ve kaldıysa nereden kaldığını gösteren bir iz bırakmalıdır.

Bu konuda üç şey birbirine geçer. **Etiketler (tag)** hangi senaryonun hangi alt kümeye ait olduğunu söyler. **Kancalar (hooks)** her senaryodan önce ve sonra neyin otomatik olacağını belirler. **Allure** ise bunların hepsini okunur bir rapora dönüştürür. Üçü tek başına anlamsızdır; birlikte "test koştu, kanıtı burada" cümlesini kurar.

### Temel kavramlar

- **Etiket (tag)**: bir senaryoya iş anlamı katan işarettir. Projedeki gerçek dağılım şöyledir: `login.feature` → `@auth @smoke`; `login_negative.feature` → `@auth @negative @regression`; `search.feature` → `@search @smoke @regression`; `cart.feature` → `@cart @regression`; `cart_persistence.feature` → `@cart @auth @regression`; `logout.feature` → `@auth @regression`. Bir senaryo **birden fazla** etiket taşıyabilir. Etiketler ayrık kutular değil, kesişen kümelerdir.
- **Kancalar (hooks)**, Cucumber'ın yaşam döngüsünde otomatik çalışan kod parçalarıdır. `BeforeAll` bir kez çalışır: tarayıcıyı başlatır, ortam bilgisini yazar. `Before` her senaryodan önce çalışır: temiz bir `BrowserContext` açar. `After` her senaryodan sonra çalışır: koşum çıktılarını (ekran görüntüsü, izleme kaydı, video) toplar ve bağlamı kapatır. `AfterAll` bir kez çalışır: tarayıcıyı kapatır.
- **İzleme kaydı (trace)**: Playwright'ın koşum kaydıdır. Her adımdaki sayfa görüntüsünü de içerir. Hatadan sonra geri sarıp adım adım inceleyebilirsin. Diske bir zip dosyası olarak yazılır.
- **İşçi süreç (worker)**: testleri koşturan paralel süreçlerden biridir. Bu projede iki işçi aynı anda çalışır. Bir işçi sızdırdığı tarayıcı bağlamını kendi koşumu bitene kadar taşır.
- **Allure sonuç akışı**: koşum sırasında her adım ve senaryo için `allure-results/` klasörüne JSON dosyaları yazılır. `npm run report` komutu (`allure generate` + `allure open`) bu dosyaları statik bir HTML rapora dönüştürür. Koşum ile rapor üretimi **ayrı iki adımdır**.
- **`this.attach`**: Cucumber'ın World nesnesine ait bir metottur. O an koşan senaryonun rapor girdisine ikili veya metin içerik ekler; ekran görüntüsü, izleme kaydı, video gibi. `require('allure-cucumberjs')` satırı, bu mekanizmayı dinleyen Allure çalışma zamanını kurar. Çalışma zamanı (runtime), koşum sırasında arka planda çalışıp olayları dinleyen katmandır. Eklenen dosyalar bu köprü üzerinden Allure JSON'una düşer.
- **Biçimlendirici (formatter)**: cucumber-js'in koşum olaylarını bir çıktı biçimine çeviren eklentisidir. Biri konsola okunur ilerleme basar, bir diğeri Allure JSON'u üretir.
- **Standart çıktı (stdout)**: bir programın konsola bastığı akıştır. Bir akışa aynı anda tek yazıcı bağlanabilir. Bu kısıt, aşağıda anlatılan gerçek hatanın kaynağıdır.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cucumber hook yaşam döngüsü: bir kez ve her senaryo başına çalışanlar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hook yaşam döngüsü</text>
  <rect x="30" y="44" width="150" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="105" y="65" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">BeforeAll</text>
  <text x="105" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bir kez: tarayıcı + env</text>
  <rect x="220" y="110" width="200" height="90" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="132" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">her senaryo</text>
  <text x="320" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Before → yeni context</text>
  <text x="320" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Senaryo koşar</text>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">After → artefakt + kapatma</text>
  <rect x="460" y="44" width="150" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="535" y="65" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">AfterAll</text>
  <text x="535" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bir kez: tarayıcı kapanır</text>
  <path d="M105,94 L105,110 L220,145" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#a205arrow)"/>
  <path d="M420,155 L535,155 L535,94" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#a205arrow)"/>
  <path d="M320,200 L320,215 L220,145" fill="none" stroke="#B26B00" stroke-width="1.4" stroke-dasharray="4 3"/>
  <text x="320" y="222" text-anchor="middle" font-size="9.5" fill="#B26B00" font-family="system-ui">döngü: bir sonraki senaryo</text>
  <defs>
    <marker id="a205arrow" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0,0 L6,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**`After` kancasında işlem sırası önemlidir.** Proje kodunda video tutamacı, `closeBrowser()` sayfayı kapatmadan **önce** alınır; çünkü Playwright videoyu ancak bağlam kapanınca diske yazar. Ekran görüntüsü yalnız senaryo başarısızsa alınır.

İzleme kaydı her zaman durdurulur, ama yalnız senaryo başarısızsa rapora eklenir. Başarılıysa dosya silinir. Geçen senaryoların kaydı, kimsenin bakmayacağı bir maliyettir:

```javascript
// … ekran görüntüsü bloğu kısaltıldı
try {
  if (this.context) {
    const tracePath = path.join(tracesDir, `${traceName(pickle)}.zip`);
    fs.mkdirSync(tracesDir, { recursive: true });
    await this.context.tracing.stop({ path: tracePath });
    if (failed) {
      await this.attach(fs.readFileSync(tracePath), { mediaType: ContentType.PLAYWRIGHT_TRACE, fileName: 'Playwright trace.zip' });
    } else {
      fs.rmSync(tracePath, { force: true });
    }
  }
} catch (error) {
  console.warn(`Trace kaydedilemedi: ${error.message}`);
} finally {
  await this.closeBrowser();
}
```

Buradaki `finally` bloğu kozmetik değildir; gerçek bir hatanın düzeltmesidir. Çıktı toplama sırasında hata çıkabilir. Sayfa zaten çökmüşse `this.page.screenshot()` kendisi hata fırlatır. İzleme kaydını durdurmak (`tracing.stop`) da diske yazarken hata verebilir.

`finally` olmasaydı ne olurdu? Bir hata çıktığında `closeBrowser()` hiç çağrılmazdı. O bağlam açık kalır ve işçi sürecin kalan koşumu boyunca kaynak sızdırırdı.

Yapı iki katmanlıdır. Ekran görüntüsü kendi `try/catch` bloğu içinde alınır; hatası kancayı durdurmaz. İzleme kaydı bloğundaki `finally` ise `closeBrowser()` çağrısının her durumda yapılmasını garanti eder. Bu, proje geliştirilirken gerçekten yaşanmış ve düzeltilmiş bir hatadır.

**Allure eklentisinde iki ayrı zorluk yaşandı.** İkisi de gerçektir ve ikisi de sessizce olmuştur; hiçbiri hata fırlatmadı.

1. Rapor hiç üretilmiyordu. Sebep şuydu: cucumber-js aynı anda yalnızca **tek bir biçimlendiriciyi** standart çıktıya bağlar. Fazlası sessizce düşer. Bu projede düşen biçimlendirici Allure'du. Çözüm, Allure'un kullanılmayan akış çıktısını bir dosyaya yönlendirmek oldu. Standart çıktı ise okunabilir konsol ilerlemesine kaldı.
2. Ekran görüntüleri alınıyordu ama rapora **bağlanmıyordu**. Sebep, `allure-js-commons` kütüphanesinin kendi dosya ekleme fonksiyonunun kullanılmasıydı. Çözüm, Cucumber'ın kendi `this.attach` mekanizmasına geçmek oldu. Çünkü `allure-cucumberjs` çalışma zamanı yalnız bu köprüyü dinler.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek formatter stdout darboğazı ve sonuç raporu akışı">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">cucumber-js: tek formatter stdout'a bağlanır</text>
  <rect x="40" y="40" width="180" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="130" y="68" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">readable-formatter</text>
  <rect x="40" y="98" width="180" height="46" rx="9" fill="#F3E1E1" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="130" y="126" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">allure-cucumberjs (sessizce düşer)</text>
  <text x="230" y="66" font-size="18" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="260" y="62" width="90" height="40" rx="8" fill="none" stroke="#5D7789" stroke-width="1.3"/>
  <text x="305" y="87" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">stdout</text>
  <rect x="400" y="40" width="220" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="510" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Çözüm: Allure çıktısı → dosya</text>
  <text x="510" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">allure-results/*.json</text>
  <text x="510" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">↓ npm run report</text>
  <text x="510" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">allure generate → HTML</text>
  <text x="510" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">adımlar + environment</text>
  <text x="510" y="162" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">+ hata görüntüsü/trace</text>
</svg>
```

Bir de video politikası var. Video varsayılan olarak **kapalıdır**, çünkü hem koşum süresine hem diske maliyeti vardır. `VIDEO=true` ile açıldığında bile yalnızca **başarısız** senaryoların kaydı saklanır; geçenlerinki silinir. Çünkü bir kaydın değeri yalnız hata ayıklarken ortaya çıkar.

Pratik kural şu: etiketler **hangi alt kümenin koşacağını** çözer. Kancalar **hangi çıktının ne zaman toplanacağını ve bağlamın nasıl güvenle kapatılacağını** çözer. Allure ise **bunların hepsini nasıl okunur hâle getireceğini** çözer.

### Mülakatta ne sorulur?

- **"Rapor hiç üretilmiyordu, hata da vermiyordu. Sebebi neydi?"** Beklenen: cucumber-js tek formatter'ı stdout'a bağlar, ikinci formatter (Allure) sessizce düşüyordu; çözüm çıktısını bir dosyaya yönlendirmek.
- **"Ekran görüntüleri neden rapora bağlanmıyordu?"** Beklenen: `allure-js-commons`'ın kendi attach fonksiyonu yerine Cucumber'ın `this.attach`'ına geçmek gerekti; `allure-cucumberjs` yalnız bu köprüyü dinliyor.
- **"After hook'unda try/finally neden zorunlu?"** Beklenen: `screenshot()` sayfa çökmüşse hata fırlatabilir; `finally` olmadan `closeBrowser()` hiç çağrılmaz, context worker boyunca açık kalır.
- **"Video neden varsayılan kapalı?"** Beklenen: koşum süresi + disk maliyeti; `VIDEO=true` ile bile yalnız başarısızların kaydı saklanır.
- **"@auth, @negative, @regression gibi tag'lar neden aynı senaryoya birden fazla veriliyor?"** Beklenen: alt küme koşumları (`npm run test:smoke` vb.) tag ifadesiyle filtrelenir; bir senaryo birden çok alt kümeye ait olabilir.
- **"`environment.properties` ne işe yarıyor?"** Beklenen: raporun hangi ortamda (baseUrl, headless, locale, Node sürümü) koştuğunu gösterir; `BeforeAll`'da bir kez yazılır.

### Tek cümlede

Allure, kancalar ve etiketler birlikte bir hesap verebilirlik katmanı kurar. Bu katman "geçti/kaldı" demekle yetinmez; **neden** geçtiğini ya da kaldığını kanıtla gösterir. Etiketler hangi alt kümenin koştuğunu söyler. Kancalar hangi çıktının ne zaman toplandığını ve bağlamın nasıl güvenle kapatıldığını belirler. Allure ise bunların hepsini okunur bir rapora dönüştürür.

# allure-1 | Orta
## Soru
Case study zorunlu kılıyor: senaryolar anlamlı tag'larla gruplanmalı. Projede `login_negative` senaryosu `@auth @negative @regression`, `cart_persistence` senaryosu `@cart @auth @regression` taşıyor — yani bir senaryo birden fazla tag alabiliyor. Bu çoklu-tag yaklaşımının pratik faydası nedir?
## A
Bir senaryo birden fazla alt küme koşumunun parçası olabilir; tag'lar ayrık kategoriler değil, kesişen kümelerdir
🤖 Doğru: tag ifadeleri filtre görevi görür, aynı senaryo hem `@negative` hem `@regression` koşumuna dahil olabilir.
## B
Tag sayısı arttıkça Allure raporundaki adım süresi ölçümü daha hassas hale gelir
🤖 Tag sayısı adım süresi ölçümünü etkilemez; ölçüm Allure'un kendi zamanlamasından gelir.
## C
Cucumber, aynı senaryoyu taşıdığı her tag için ayrı bir worker'da tekrar çalıştırıp sonuçları tek raporda birleştirir
🤖 Cucumber senaryoyu tag başına tekrar koşturmaz; tag yalnız hangi koşuma dahil olacağını belirler.
## D
Tag sayısı arttıkça senaryonun Allure raporunda görünme önceliği yükselir, üstte listelenir
🤖 Allure sıralaması tag sayısına göre değişmez; tag'lar filtreleme/gruplama için kullanılır.
## Doğru
A
## Açıklama
Tag ifadeleri (`npm run test:smoke` → `--tags @smoke`, `test:regression` → `@regression`, `test:negative` → `@negative`) birer **filtredir**, ayrık kutular değil. `login_negative` senaryosu hem `@negative` hem `@regression` taşıdığı için her iki alt küme koşumunda da yer alır; tag'ları "en az yeterli sayıya" indirmek o senaryoyu mantıken ait olması gereken bir koşumdan dışlardı. Cucumber bir senaryoyu tag sayısı kadar tekrar koşturmaz (C yanlış), Allure raporundaki sıralama veya adım süresi ölçümü tag sayısından etkilenmez (B, D yanlış).

# allure-2 | Orta
## Soru
`hooks.js`'teki `After` hook'unda ekran görüntüsü ayrı bir `try/catch` içinde alınıyor; context kapatma (`closeBrowser()`) ise trace toplama bloğunun `finally`'sinde çağrılıyor. Bu tasarımın (`finally` kullanımı) amacı nedir?
## A
`closeBrowser()` senkron bir fonksiyon olduğu için `finally` bloğu performansı artırır
🤖 `finally` performans için değil; senkron/asenkron ayrımı burada belirleyici değil.
## B
Artefakt toplama hata fırlatabilir; `finally` olmadan context hiç kapanmaz, worker boyunca açık kalır
🤖 Doğru: REVIEW.md'de bulunup düzeltilen gerçek bir hata — `finally` olmadan artefakt toplama hatası context sızıntısına yol açıyordu.
## C
Trace dosyasının diske yazılmasını hızlandırmak için `finally` bloğu kullanılır
🤖 Trace yazımı `finally`'den önce tamamlanır; `finally`'nin amacı yazım hızı değil.
## D
`finally` bloğu yalnızca `AfterAll` hook'unda tarayıcıyı kapatmak için gereklidir, `After`'da isteğe bağlıdır
🤖 Aksine bu proje `After` hook'unda `finally` kullanıyor; `AfterAll` ayrı ve daha basit bir tarayıcı kapatma yapıyor.
## Doğru
B
## Açıklama
`After` hook'unda artefakt toplama sırasında hata çıkabilir — sayfa zaten çökmüşse `screenshot()` fırlatır, `tracing.stop()` de diske yazarken hata verebilir. Ekran görüntüsü kendi `try/catch`'i içindedir; trace bloğunun `finally`'si ise `closeBrowser()`'ı garantiler. `finally` olmasaydı, trace tarafındaki bir hata `closeBrowser()` çağrısını hiç yapmadan hook'u sonlandırır, context açık kalır ve worker'ın kalan koşumu boyunca kaynak sızdırırdı. Bu, projenin gerçek gözden geçirmesinde bulunup düzeltilen bir hatadır — kozmetik bir tercih değil, somut bir kaynak sızıntısı düzeltmesidir. Performans (A, C) veya `AfterAll`'a özgü olma (D) bu tasarımın gerekçesi değildir.

# allure-3 | Orta
## Soru
Allure entegrasyonunun ilk denemesinde rapor hiç üretilmiyordu ve koşum herhangi bir hata da vermiyordu. Kök sebep neydi?
## A
`allure-results` klasörü `.gitignore`'da olduğu için CI'da dosyalar hiç oluşmuyordu
🤖 `.gitignore` rapor üretimini etkilemez, yalnız versiyon kontrolüne dahil olup olmamasını belirler; sorun yerelde de vardı.
## B
Allure CLI kurulu olmadığı için `npm run report` komutu arka planda başarısız oluyordu
🤖 Sorun rapor *üretimi* (`allure generate`) değil, koşum sırasında sonuç JSON'unun hiç yazılmamasıydı.
## C
cucumber-js yalnız tek formatter'ı stdout'a bağlar; fazlası sessizce düşüyordu ve düşen Allure'unkiydi
🤖 Doğru: birden çok formatter tanımlanınca cucumber-js sessizce birini eler; bu projede elenen Allure'un formatter'ıydı.
## D
Testler iki worker ile paralel koştuğu için ikisi de aynı `allure-results` dosyasına yazıp birbirinin sonucunu eziyordu
🤖 Worker'lar ayrı dosyalara yazar; çakışma değil, formatter'ın hiç çıktı üretmemesi sorundu.
## Doğru
C
## Açıklama
cucumber-js, birden fazla formatter tanımlandığında bunların yalnızca **birini** stdout'a bağlar; fazlası sessizce elenir — hata fırlatmaz, uyarı da vermez. Bu projede elenen formatter Allure'unkiydi, bu yüzden koşum normal görünüyor ama `allure-results/` altında hiçbir JSON oluşmuyordu. Çözüm, Allure'un kullanılmayan akış çıktısını ayrı bir dosyaya yönlendirmekti; konsol ilerlemesi (readable-formatter) stdout'ta kaldı. `.gitignore` (A) yalnızca versiyon kontrolünü etkiler, Allure CLI kurulumu (B) rapor *üretim* adımıyla ilgilidir (sorun kaynak veri eksikliğiydi), paralel çakışma (D) ise worker'ların ayrı dosyalara yazmasından dolayı burada gerçekleşmiyordu.

# allure-4 | Orta
## Soru
Aynı entegrasyonda ikinci bir sorun: koşum sonuçları rapora düşüyordu ama başarısız senaryoların ekran görüntüleri rapora **bağlanmıyordu**. Çözüm neydi?
## A
Ekran görüntüsünü `fullPage: true` ile almak, kırpılmış görüntülerin reddedilmesini önlemek
🤖 `fullPage` seçeneği görüntünün kapsamını belirler, rapora bağlanıp bağlanmamasıyla ilgisizdir.
## B
Ekran görüntüsünü PNG yerine JPEG formatında kaydetmek, Allure'ın PNG desteklemediğini varsaymak
🤖 Allure PNG'yi sorunsuz destekler; format değişimi kök sebep değildi.
## C
`resultsDir` klasörünü `mkdirSync` ile önceden oluşturmak, klasör yokluğundan kaynaklanan sessiz hatayı önlemek
🤖 `resultsDir` zaten `writeAllureEnvironment` içinde oluşturuluyordu; klasör yokluğu bu bağlanma sorununun sebebi değildi.
## D
`allure-js-commons`'ın kendi attachment fonksiyonu yerine Cucumber'ın `this.attach` mekanizmasına geçmek
🤖 Doğru: `allure-cucumberjs` (hooks.js'te `require` edilen runtime) Cucumber'ın `this.attach` çağrılarını dinliyor; başka bir yoldan eklenen içerik bu köprüyü atlıyordu.
## Doğru
D
## Açıklama
`allure-cucumberjs` paketi (dosyanın başında `require('allure-cucumberjs')` ile içeri alınıp Allure'un Cucumber runtime köprüsünü kurar) yalnızca Cucumber'ın kendi `this.attach` çağrılarını dinler. İlk denemede `allure-js-commons`'ın kendi attachment fonksiyonu doğrudan çağrılıyordu; bu çağrı Allure JSON'una hiç düşmüyordu çünkü köprünün dinlediği kanal değildi. Çözüm, `this.attach(screenshot, { mediaType: ContentType.PNG, ... })` çağrısına geçmekti — proje kodunda bugünkü hali budur. Görüntü formatı (A, B) veya klasör oluşturma (C) bu sorunun kaynağı değildi.

# allure-5 | Orta
## Soru
Projede video kaydı varsayılan olarak kapalı ve `VIDEO=true` ile açıldığında bile yalnızca **başarısız** senaryoların kaydı saklanıyor, geçenlerinki siliniyor. Bu tasarımın gerekçesi nedir?
## A
Video koşum süresine ve diske somut maliyet ekler; bir kaydın değeri ise yalnızca hata ayıklarken ortaya çıkar
🤖 Doğru: geçen senaryoların videosu kimsenin bakmayacağı bir maliyettir; hata ayıklama değeri yalnız başarısız koşumda vardır.
## B
Playwright başarılı senaryoların videosunu teknik olarak kaydedemiyor, yalnız hata anını yakalayabiliyor
🤖 Playwright her senaryonun videosunu kaydedebilir; sınırlama teknik değil, tasarım tercihidir.
## C
Allure video dosyalarını yalnızca başarısız senaryo bağlamında görüntüleyebilir, başarılı senaryoya eklense de oynatılmaz
🤖 Allure başarılı senaryoya eklenen videoyu da oynatabilir; kısıtlama Allure'da değil proje tasarımındadır.
## D
CI ortamı video codec'ini desteklemediği için yalnız yerel koşumlarda video saklanabiliyor
🤖 CI ortamı `VIDEO=true` ile aynı şekilde koşup videoyu saklayabiliyor; kısıtlama ortam değil politikadır.
## Doğru
A
## Açıklama
Video kaydı, koşum süresine ve diske somut bir maliyet ekler; bir kaydın değeri neredeyse yalnızca hata ayıklarken ortaya çıkar. Bu yüzden proje video'yu varsayılan kapalı tutuyor (`VIDEO=true` ile açılıyor) ve açık olsa bile yalnızca başarısız senaryoların kaydını saklıyor — başarılı senaryolarınki koşum sonunda siliniyor. `VIDEO=all` ile demo/hata ayıklama amaçlı her senaryonun kaydı senaryo adıyla tutulabiliyor. Bu kısıtlama Playwright'ın teknik yeteneğinden (B), Allure'un oynatma desteğinden (C) veya CI codec desteğinden (D) değil, bilinçli bir maliyet/fayda kararından kaynaklanır.

# allure-6 | Temel
## Soru
`hooks.js`'teki `BeforeAll` içinde çağrılan `writeAllureEnvironment()` fonksiyonu ne işe yarıyor?
## A
Her senaryo öncesi tarayıcı context'inin temiz (çerez/localStorage boş) olduğunu doğrular, kirli state bulursa koşumu durdurur
🤖 Context temizliği `Before` hook'unun işi; `writeAllureEnvironment` yalnız bir dosya yazar, doğrulama yapmaz.
## B
`allure-results/environment.properties` dosyasına baseUrl, headless, locale, Node gibi koşum bilgilerini bir kez yazar
🤖 Doğru: rapor, hangi ortama/tarayıcıya karşı koşulduğunu bu dosyadan okuyup gösterir; `BeforeAll`'da bir kez yazılması yeterlidir.
## C
Allure CLI'nin kurulu olup olmadığını kontrol edip eksikse otomatik kurar
🤖 Kurulum kontrolü/otomatik kurulum bu fonksiyonun işi değil; yalnızca bir metin dosyası yazar.
## D
Her senaryo sonunda çalışıp o senaryonun ortam değişkenlerini ayrı ayrı kaydeder
🤖 Fonksiyon `BeforeAll`'da (bir kez) çalışır, senaryo sonunda değil; tüm koşum için tek bir ortam bilgisi yazılır.
## Doğru
B
## Açıklama
`writeAllureEnvironment()`, `allure-results/environment.properties` dosyasına `BaseURL`, `Browser`, `Headless`, `Locale`, `Node`, `Platform` satırlarını bir kez yazar. Allure raporu bu dosyayı okuyup "Environment" sekmesinde gösterir — böylece rapora bakan biri hangi ortama, hangi tarayıcıyla, headless mi headed mi koştuğunu görebilir. Fonksiyon `BeforeAll`'da çalıştığı için koşum başına bir kez yeterlidir; senaryo başına tekrarlanmaz (D yanlış). Context temizliği (A) `Before` hook'unun, Allure CLI kurulumu (C) ise `npm run report`'un sorumluluğundadır.
