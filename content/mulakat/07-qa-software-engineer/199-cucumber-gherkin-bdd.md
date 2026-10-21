---
slug: cucumber-gherkin-bdd
baslik: "Cucumber, Gherkin ve BDD: İş Dili ile Teknik Detayın Ayrılması"
kademe: Temel
sira: 199
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

**Cucumber**, test senaryolarını programlama diliyle değil, düz ve yapılandırılmış bir dille yazmayı sağlayan bir araçtır. Bu dil Türkçe de olabilir. Yaklaşımın adı **davranış odaklı geliştirmedir (BDD, behavior-driven development)**.

Fikir şudur: testler yalnız yazılımcının değil, **ürün sahibinin de** okuyup anlayabileceği bir dille yazılır. Böylece "bu senaryo doğru mu?" sorusuna teknik bilgisi olmayan biri de cevap verebilir.

Bu projede karar somuttur: senaryolar **Türkçe Gherkin** ile yazılmıştır. Sebebi, vaka çalışmasındaki (case study) ekibin Türk olması ve iş dilinin Türkçe olmasıdır.

Kod tarafı ise ağırlıklı olarak İngilizcedir. Değişken ve fonksiyon adları tamamen, yorumların çoğu İngilizcedir; birkaç yerde siteye özgü Türkçe not vardır. Bu ayrım kendi başına BDD'nin "iş dili ile teknik detayı ayır" ilkesinin bir yansımasıdır.

### Temel kavramlar

- **Gherkin.** Cucumber'ın kullandığı yapılandırılmış dildir. İki temel yapı taşı vardır: `Özellik` (Feature) üst düzey bir iş yeteneğini, `Senaryo` (Scenario) tek bir somut örneği anlatır. Üç de anahtar kelime vardır: **`Diyelim ki`** (Given) başlangıç durumunu, **`Eğer ki`** (When) yapılan eylemi, **`O zaman`** (Then) beklenen sonucu belirtir. `Ve` ile `Ama` (And/But) art arda gelen adımları bağlar.
- **Özellik dosyası (feature file).** `.feature` uzantılı, Gherkin ile yazılan dosyadır. Bu projede her dosya bir iş yeteneğini kapsar: `login.feature`, `search.feature` gibi. Bu dosyalarda **hiç teknik detay yoktur**; ne seçici, ne API çağrısı, ne kod. Yalnızca iş diliyle yazılmış cümleler vardır.
- **Adım tanımı (step definition).** Özellik dosyasındaki her cümleyi gerçek koda bağlayan fonksiyondur. Cucumber, cümledeki metni bir düzenli ifadeyle (regex) veya cucumber-expression kalıbıyla eşleştirir. Kalıptaki yer tutucular (`{string}`, `{int}`) eşleşen kısımları fonksiyona parametre olarak geçirir.
- **Element sözlüğü (element registry).** Bu projeye özgü bir katmandır. Adım tanımları bile doğrudan CSS seçici yazmaz; onun yerine `"e-posta alanı"` gibi iş diline yakın bir **isim** kullanır. Bu isim `features/support/element-registry.js` üzerinden `pages/locators.js` içindeki gerçek seçiciye çevrilir.
- **BDD'nin amacı.** Testleri yazılımcı diliyle değil iş diliyle yazmak. Böylece iş tarafı ile teknik taraf **aynı belgeyi** okuyup anlaşır. Senaryo dosyası hem çalıştırılabilir bir testtir, hem de okunabilir bir gereksinim belgesidir (yaşayan dokümantasyon).

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gherkin senaryosundan step definition'a ve locator'a giden akış">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Feature dosyasından gerçek koda: üç katman</text>
  <rect x="30" y="42" width="180" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">.feature</text>
  <text x="120" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"e-posta alanı"na</text>
  <text x="120" y="104" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"x" yazılır</text>
  <text x="120" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">saf iş dili</text>
  <text x="120" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">selector YOK</text>
  <text x="120" y="170" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">herkes okur</text>
  <rect x="230" y="42" width="180" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">step definition</text>
  <text x="320" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">{string} alanına</text>
  <text x="320" y="104" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">{string} yazılır</text>
  <text x="320" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">cümleyi kodla</text>
  <text x="320" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">eşleştirir</text>
  <text x="320" y="170" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">element adı ile çağırır</text>
  <rect x="430" y="42" width="180" height="150" rx="12" fill="#F5F1E4" stroke="#B26B00" stroke-width="1.5"/>
  <text x="520" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">locators.js</text>
  <text x="520" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">emailInput:</text>
  <text x="520" y="104" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">'#txtEmail'</text>
  <text x="520" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">teknik detay</text>
  <text x="520" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yalnız burada</text>
  <text x="520" y="170" text-anchor="middle" font-size="9.5" fill="#B26B00" font-family="system-ui">tek dosya, tek kaynak</text>
  <line x1="210" y1="117" x2="230" y2="117" stroke="#5D7789" stroke-width="1.4" marker-end="url(#bddarrow)"/>
  <line x1="410" y1="117" x2="430" y2="117" stroke="#5D7789" stroke-width="1.4" marker-end="url(#bddarrow)"/>
  <defs>
    <marker id="bddarrow" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**Cümleden koda eşleşme.** Aşağıdaki gerçek örnek, projedeki bir hazır adımı (generic step) gösterir:

```javascript
When('{string} alanına {string} yazılır', async function (elementName, value) {
  await locatorFor(this, elementName).first().fill(value);
});
```

Gherkin'de şu cümle yazıldığını düşünün: `Eğer ki "e-posta alanı" alanına "gecersiz-eposta" yazılır`. Cucumber yukarıdaki tanımı bulur ve `{string}` yer tutucularını sırayla doldurur: `elementName = "e-posta alanı"`, `value = "gecersiz-eposta"`.

`locatorFor` fonksiyonu ise `elementName` değerini `element-registry.js` üzerinden `pages/locators.js` içindeki gerçek CSS seçiciye (`#txtEmail`) çevirir. Bu iki aşamalı çözümleme sayesinde özellik dosyasında **hiçbir zaman** seçici geçmez.

**Neden Türkçe Gherkin?** Cevap nettir: vaka çalışması senaryoların iş diliyle yazılmasını istiyordu; Türk bir ekipte iş dili Türkçedir. Bu, BDD'nin temel önermesinin doğrudan uygulanmasıdır. Senaryolar, o dili konuşan paydaşın **kendi dilinde** okuyabileceği bir belge olmalıdır. Paydaş ürün sahibi, test uzmanı veya geliştirici olabilir.

Kod tarafı ise ağırlıklı olarak İngilizce kalmıştır. Değişken ve fonksiyon adları tamamen İngilizcedir; yorumların çoğu da öyledir. Birkaç dosyada siteye özgü davranışı anlatan Türkçe notlar vardır (`auth.steps.js`, `locators.js`, `config.js`). Gerekçe şu: yazılım geliştirmenin evrensel dili İngilizcedir. Ayrım, dilin **kime** hitap ettiğine göre yapılmıştır: senaryo iş insanına konuşur, kod yazılımcıya.

**"Site değişirse senaryoyu değil, arkasındaki kodu güncelliyorum."** Bu cümle BDD'nin pratik kazancını özetler. Bir seçici ya da akış detayı değiştiğinde — örneğin bir butonun sınıf adı değiştiğinde — güncellenecek yer `pages/locators.js` veya bir adım tanımıdır. Özellik dosyasındaki `Eğer ki "sepete ekle butonu" elementine tıklanır` cümlesi **aynı kalır**.

Bu ayrım olmasaydı ne olurdu? Senaryolarda doğrudan CSS seçici geçseydi, sayfadaki her değişiklik hem teknik dosyaları hem de "iş belgesi" sayılan senaryoları kırardı. Senaryoların okunabilirlik değeri kaybolurdu.

**Neden bazı adımlar hazır adım değil?** Bu projede iki iş için ayrı, özel adımlar yazılmıştır: iki aşamalı giriş akışı (telefon + şifre) ve sepet matematiği. Bunlar **alan bilgisi gerektiren** işlerdir.

Bu durum, "her şeyi hazır adımla yapalım" yaklaşımının sınırını gösterir. Hazır adımlar tekrar eden basit etkileşimler için idealdir: tıklama, yazma, görünürlük kontrolü. İş mantığı barındıran akışlar ise kendi adına adım yazmayı hak eder. Aksi hâlde özellik dosyası okunaklılığını kaybeder ve adım adım bir betiğe dönüşür.

### Mülakatta ne sorulur?

- **"Neden Türkçe Gherkin kullandın, İngilizce değil?"** Beklenen: case ekibinin iş dili Türkçe; BDD'nin amacı senaryonun iş tarafınca okunabilir olması, bu yüzden Türkçe seçildi. Kod tarafı ağırlıklı olarak İngilizce (değişken/fonksiyon adları tamamen; yorumların çoğu — birkaç yerde siteye özgü Türkçe not var) tutularak yazılım pratiğinin evrensel diliyle uyumlu kalındı.
- **"Bir özellik dosyasında CSS seçici veya API çağrısı görürsen ne düşünürsün?"** Beklenen: bu, BDD'nin amacına aykırıdır. Özellik dosyaları teknik detaydan arındırılmalıdır. Seçiciler element sözlüğü veya `locators.js` gibi ayrı bir katmanda tutulmalıdır. Aksi hâlde senaryo iş tarafı için okunmaz hâle gelir ve her sayfa değişikliğinde kırılır.
- **"`{string}` gibi bir yer tutucu adım tanımında nasıl çalışır?"** Beklenen: Cucumber, Gherkin cümlesindeki tırnak içi metni adım tanımının kalıbıyla eşleştirir. Eşleşen kısmı sırasıyla fonksiyon parametresi olarak geçirir. Böylece tek bir adım tanımı, farklı element adları veya değerlerle birden fazla cümleyi karşılayabilir.
- **"Element adını (`'e-posta alanı'`) doğrudan adım tanımına gömmek yerine ayrı bir sözlükte tutmanın kazancı ne?"** Beklenen: element adı ile gerçek seçici arasındaki eşleme tek dosyada toplanır. Hem hazır adımlar hem sayfa nesneleri aynı kaynaktan okur. Bir seçici değiştiğinde güncellenecek yer tek olur.
- **"Her adımı hazır adımla mı yazdın?"** Beklenen: hayır. Tekrar eden basit etkileşimler hazır adımlarla yazıldı. İki aşamalı giriş akışı veya sepet ara toplamı gibi alan bilgisi gerektiren işler kendine özel adım tanımı aldı. Çünkü bunları zorla hazır adıma sığdırmak okunabilirliği ve niyeti gizler.
- **"BDD'nin test otomasyonuna kattığı gerçek değer nedir, sadece süslü bir sözdizimi mi?"** Beklenen: hayır. Asıl değer, iş dili ile teknik detayın ayrılmasıdır. Senaryo hem çalıştırılabilir bir test hem de paydaşların okuyup doğrulayabileceği bir belge olur. Ayrıca sayfa veya kod değişince senaryonun kendisi değil, arkasındaki kod güncellenir.

### Tek cümlede

Cucumber ve Gherkin, senaryoları `Diyelim ki` / `Eğer ki` / `O zaman` kalıplarıyla iş diliyle yazdırır. Bu cümleler adım tanımları ve element sözlüğü üzerinden gerçek koda bağlanır. Böylece özellik dosyası teknik detaydan tamamen arınır: sayfa değişince senaryo değil, arkasındaki kod güncellenir.

# bdd-1 | Temel
## Soru
Bir case study ekibi test senaryolarının hem geliştiriciler hem ürün sahibi tarafından okunup onaylanabilmesini istiyor. Cucumber/Gherkin ile BDD (davranış odaklı geliştirme) yaklaşımının bu isteğe verdiği temel cevap nedir?
## A
Senaryoları programlama dili yerine iş diline yakın, yapılandırılmış bir dille (Given/When/Then) yazdırmak
🤖 Doğru: BDD'nin temel önermesi, senaryonun hem çalıştırılabilir test hem okunabilir belge olmasıdır.
## B
Senaryoların ürün sahibi tarafından yazılmasını zorunlu kılarak testlerin gereksinim belgesiyle birebir örtüştüğünü garanti etmek
🤖 BDD yazarın kim olduğunu dayatmaz; senaryolar çoğunlukla ekipçe yazılır, amaç iş tarafının **okuyup doğrulayabilmesidir**.
## C
Test kodunu tamamen ortadan kaldırıp senaryoları yalnız Gherkin metninden yorumlayarak çalıştırmak
🤖 Gherkin metni tek başına çalışmaz; step definition'lar aracılığıyla gerçek koda bağlanması gerekir.
## D
Senaryoları yalnızca İngilizce yazmayı zorunlu kılarak uluslararası ekiplerde ortak bir dil sağlamak
🤖 Gherkin belirli bir dili zorunlu kılmaz; hangi dilin kullanılacağı ekibin iş diline göre seçilir (bu projede Türkçe).
## Doğru
A
## Açıklama
BDD'nin (davranış odaklı geliştirme) temel fikri, testleri yalnızca yazılımcının değil **iş tarafının da** okuyup anlayabileceği bir dille yazmaktır. Cucumber, bu dili Gherkin ile sağlar: `Diyelim ki`/`Eğer ki`/`O zaman` (Given/When/Then) yapısı, teknik detaydan arındırılmış, düz cümlelerle senaryo yazmaya izin verir. Bu cümleler arkada step definition'lar aracılığıyla gerçek koda bağlanır (B ve C, Gherkin'in ne yaptığını yanlış tarif ediyor), ama dil seçimi zorunlu değildir — ekibin iş diline göre belirlenir (D yanlış, bu proje Türkçe kullanmış).

# bdd-2 | Temel
## Soru
Aşağıdaki gerçek Gherkin senaryosunu incele:

```gherkin
Diyelim ki "giriş" sayfasına gidilir
Eğer ki "e-posta sekmesi" elementine tıklanır
Ve "e-posta alanı" alanına "gecersiz-eposta" yazılır
Ve "giriş yap butonu" elementine tıklanır
O zaman "Geçerli bir e-posta adresi giriniz." metninin görünür olduğu kontrol edilir
```

Bu senaryonun BDD prensiplerine en uygun özelliği hangisidir?
## A
Senaryo, `#txtEmail` gibi doğrudan CSS seçicileri içeriyor, bu da BDD'nin önerdiği yöntemdir
🤖 Senaryoda hiçbir selector geçmiyor; BDD'nin önerdiği de tam olarak bu — teknik detayın senaryodan uzak tutulması.
## B
Senaryo yalnızca geliştiriciler tarafından anlaşılabilecek teknik jargon kullanıyor
🤖 Tam tersi: senaryo bilinçli olarak teknik jargondan arındırılmış, iş diliyle yazılmış cümleler kullanıyor.
## C
Senaryoda hiçbir CSS selector veya teknik detay geçmiyor, yalnız iş diliyle yazılmış adımlar var
🤖 Doğru: feature dosyası selector'dan arındırılmış; "e-posta alanı" gibi isimler ayrı bir katmanda gerçek selector'a çözülür.
## D
Senaryo doğrudan bir JavaScript fonksiyonu çağırıyor; bu da adım eşleşmesini atlayarak testin hızını artırıyor
🤖 Senaryo hiçbir fonksiyon çağrısı içermiyor; Gherkin adımları düz metin cümleleridir, kod çağrısı değildir.
## Doğru
C
## Açıklama
Bu senaryonun dikkat çeken özelliği, "e-posta sekmesi", "e-posta alanı" gibi **iş diline yakın isimler** kullanması ve hiçbir CSS selector, API çağrısı veya teknik uygulama detayı içermemesidir. Bu isimler arka planda (bu projede `element-registry.js` üzerinden) gerçek selector'lara çözülür, ama senaryonun kendisi bu çözümlemeden habersizdir. Bu ayrım, BDD'nin temel amacını — senaryonun iş tarafınca okunup doğrulanabilmesini — pratikte gerçekleştirir. Site değiştiğinde güncellenecek olan, senaryo değil arkasındaki kod ve locator tanımlarıdır.

# bdd-3 | Temel
## Soru
Bir step definition şöyle tanımlanmış:

```javascript
When('{string} alanına {string} yazılır', async function (elementName, value) {
  await locatorFor(this, elementName).first().fill(value);
});
```

Gherkin'de `Eğer ki "e-posta alanı" alanına "gecersiz-eposta" yazılır` cümlesi çalıştığında ne olur?
## A
Cucumber cümledeki tüm kelimeleri tek bir string olarak `elementName` parametresine geçirir; `value` parametresi tanımlı olsa da hiç doldurulmaz
🤖 `{string}` yer tutucuları ayrı ayrı eşleşir; tüm cümle tek parametreye yığılmaz, iki tırnaklı ifade iki ayrı parametre olur.
## B
Bu step definition yalnızca "e-posta alanı" adlı elementle çalışır; başka element adları için ayrı bir tanım yazmak gerekir
🤖 Tam tersi: `{string}` bir yer tutucu olduğu için herhangi bir element adıyla (registry'de tanımlıysa) eşleşir, sabit değildir.
## C
Cucumber, `elementName` ve `value` parametrelerini eşleşme sırasından bağımsız, rastgele bir sırayla fonksiyona geçirir
🤖 Sıra rastgele değildir; cümledeki tırnaklı ifadeler soldan sağa, tanımdaki `{string}` sırasıyla eşlenir.
## D
Cucumber tırnaklı ifadeleri sırayla `elementName` ve `value` parametrelerine bağlar; ilki selector'a çözülür, ikincisi forma yazılır
🤖 Doğru: `{string}` yer tutucuları cümledeki tırnaklı ifadeleri sırasıyla fonksiyon parametrelerine eşler.
## Doğru
D
## Açıklama
Cucumber-expression'daki `{string}` yer tutucuları, Gherkin cümlesindeki **tırnak içi ifadelerle** soldan sağa eşleşir ve bu değerleri step definition fonksiyonuna parametre olarak geçirir. `"e-posta alanı" alanına "gecersiz-eposta" yazılır` cümlesinde ilk tırnaklı ifade (`"e-posta alanı"`) `elementName`'e, ikincisi (`"gecersiz-eposta"`) `value`'ya bağlanır. Fonksiyon içinde `locatorFor(this, elementName)` bu ismi gerçek bir selector'a çözer (element registry üzerinden), `value` ise `fill()` ile forma yazılır. Bu mekanizma, tek bir step definition'ın registry'de tanımlı **herhangi bir** element adıyla ve herhangi bir değerle çalışabilmesini sağlar — bu yüzden aynı step, e-posta alanı, telefon alanı gibi birçok farklı alanda tekrar tekrar kullanılabilir.

# bdd-4 | Temel
## Soru
Kullanıcı, mülakatta "Neden Türkçe Gherkin kullandın?" sorusuna şu cevabı veriyor: "Case senaryoların iş diliyle yazılmasını istiyordu. Türk bir ekipte iş dili Türkçe... Kod tarafı — değişken ve fonksiyon isimleri, yorumların çoğu — İngilizce." Bu cevabın gösterdiği ayrım nedir?
## A
Türkçe Gherkin kullanmak Cucumber'ın teknik bir kısıtlamasıdır; senaryoları başka bir dilde yazmak mümkün değildir
🤖 Cucumber birçok dili destekler (`# language: tr` gibi); Türkçe seçimi kısıtlama değil ekibin tercihidir.
## B
Gherkin'in dili iş tarafına, kodun dili yazılım pratiğinin evrensel diline hitap eder; farklı kitleler farklı dillerde kalabilir
🤖 Doğru: dil seçimi kime hitap edildiğine göre bilinçli yapılmış; senaryo iş tarafına, kod yazılımcıya konuşuyor.
## C
Kod içindeki yorumların İngilizce olması, aynı projedeki Gherkin dosyalarının da tutarlılık gereği İngilizce yazılmasını gerektirir
🤖 Bu proje tam tersini gösteriyor: Gherkin Türkçe, kod İngilizce — iki katman farklı dillerde bilinçli olarak tutulabilir.
## D
Bu bir tutarsızlıktır; ya tüm proje Türkçe ya tüm proje İngilizce olmalıydı, karışık kullanım bakımı zorlaştırır
🤖 Bu bir tutarsızlık değil, bilinçli bir ayrımdır: BDD'nin "iş dili / teknik dil" ayrımının doğal bir sonucudur.
## Doğru
B
## Açıklama
BDD'nin merkezi fikri iş dili ile teknik detayın ayrılmasıdır; bu ayrım dil seçimine de yansır. Gherkin senaryoları, o senaryoyu okuyacak **iş tarafına** (ürün sahibi, test uzmanı) hitap eder — Türk bir ekipte bu Türkçe demektir. Kod tarafı ise ağırlıklı olarak İngilizce'de kalır — değişken/fonksiyon adları tamamen, yorumların çoğu; birkaç yerde siteye özgü davranışı anlatan Türkçe notlar bulunur — çünkü bu katmanı okuyacak kitle geliştiricilerdir ve İngilizce teknik terimler, kütüphane dokümantasyonuyla ve genel pratikle uyumludur. Bu, tutarsızlık değil (D), BDD'nin katmanlar arası ayrımının dil düzeyinde somutlaşmasıdır.

# bdd-5 | Temel
## Soru
Bir projede bazı adımlar (tıklama, alan doldurma, görünürlük kontrolü) "generic" (tekrar kullanılabilir) step'lerle yazılmışken, iki adımlı giriş akışı ve sepet ara toplamı hesaplaması gibi işler için **ayrı, özel** step definition'lar yazılmış. Bu ayrımın gerekçesi nedir?
## A
Generic step'ler her zaman daha hızlı çalışır; özel step'ler ek dosya yükü getirdiği için CI süresini uzatır
🤖 Performans farkı bu ayrımın gerekçesi değildir; ayrım step'in taşıdığı iş mantığının karmaşıklığıyla ilgilidir.
## B
Generic step'ler basit, tekrar eden etkileşimler için idealdir; alan bilgisi gerektiren akışları generic step'lere sıkıştırmak niyeti gizler
🤖 Doğru: bazı işler yeterince iş mantığı taşır ki generic bir "tıkla/yaz" step'i o mantığı temsil edemez, kendine özel adım gerekir.
## C
Cucumber teknik olarak yalnızca sınırlı sayıda generic step tanımlamaya izin verir; bu sınır aşıldığında kalan adımlar için özel step yazmak zorunludur
🤖 Cucumber'da step definition sayısına teknik bir sınır yoktur; ayrım tasarım tercihi, teknik kısıtlama değil.
## D
Özel step'ler yalnızca hata ayıklama (debug) amaçlıdır; üretim koşumlarında generic step'lerle değiştirilirler
🤖 Özel step'ler üretim (gerçek) senaryolarda kullanılır; hata ayıklamayla sınırlı bir mekanizma değildir.
## Doğru
B
## Açıklama
Generic step'ler ("elementine tıklanır", "alanına yazılır" gibi) basit, tekrar eden ve iş mantığı taşımayan etkileşimler için idealdir — aynı kalıp onlarca farklı elementte yeniden kullanılabilir. Ama iki adımlı giriş akışı (telefon doğrulanır, sonra şifre alanı açılır) veya sepet ara toplamının doğru hesaplandığının kontrolü gibi işler **alan bilgisi (domain knowledge)** taşır; bunu zorla generic bir "tıkla ve yaz" kalıbına sıkıştırmak, senaryoyu okuyanın gerçekte neyin test edildiğini anlamasını zorlaştırır. Kendine özel bir step definition (`{string} kullanıcısı ile giriş denenir` gibi), o iş akışını tek, anlamlı bir cümlede özetleyerek hem senaryonun okunabilirliğini korur hem karmaşık mantığı kendi step'inin içine hapseder.

# bdd-6 | Temel
## Soru
`auth.steps.js` bir adımı `Given('geçerli kullanıcı bilgileri ile giriş yapılır', ...)` olarak kaydediyor. Aynı cümle `login.feature`'da `Diyelim ki ...`, `cart_persistence.feature`'da ise `Eğer ki ...` anahtar kelimesiyle kullanılıyor ve ikisi de çalışıyor. Bu nasıl mümkün olur, `Diyelim ki`/`Eğer ki` ayrımı ne işe yarar?
## A
Cucumber eşleşmeyi yalnız adım metnine bakarak yapar; anahtar kelime ise okuyucuya adımın rolünü anlatır: ön koşul mu, test edilen eylem mi
🤖 Doğru: `Given`/`When`/`Then` kayıtları eşleşme açısından birbirinin yerine geçer; ayrım insan okuyucu içindir.
## B
`Eğer ki` ile yazılan bir adım yalnız `When(...)` ile kaydedilmiş tanımlarla eşleşir; bu yüzden aynı cümlenin ikinci bir `When` tanımı bulunmak zorundadır
🤖 Cucumber-js'te anahtar kelime eşleşmeye girmez; tek bir `Given` kaydı her üç anahtar kelimeyle de bulunur.
## C
Anahtar kelimeler koşum sırasını belirler: `Diyelim ki` adımları senaryodan önce, `Eğer ki` adımları senaryo sırasında çalıştırılır
🤖 Adımlar dosyada yazıldıkları sırayla koşar; "önce/sonra" davranışı `Before`/`After` hook'larının işidir, anahtar kelimenin değil.
## D
Türkçe Gherkin'de anahtar kelimeler yalnızca süslemedir; ne Cucumber ne de okuyucu için bir anlam taşır, silinseler de senaryo aynı şekilde çalışır
🤖 Yarısı doğru, yarısı yanlış: eşleşmeye girmezler ama sözdizimi gereği zorunludurlar ve okuyucuya adımın rolünü (ön koşul / eylem / beklenti) anlatırlar.
## Doğru
A
## Açıklama
Cucumber-js'te `Given`, `When` ve `Then` **aynı kayıt defterine** yazar: bir adım aranırken yalnız cümlenin metni (cucumber-expression veya regex) karşılaştırılır, cümlenin başındaki anahtar kelime hesaba katılmaz. Bu yüzden `Given(...)` ile kaydedilmiş `geçerli kullanıcı bilgileri ile giriş yapılır` adımı, feature dosyasında `Eğer ki` ile de çağrılabilir. Anahtar kelime yine de anlamsız değildir — **okuyucuya** adımın senaryodaki rolünü anlatır: `login.feature`'da giriş, ardından gelen doğrulamaların **ön koşuludur** (`Diyelim ki`); `cart_persistence.feature`'da ise asıl merak edilen davranış "giriş yapınca misafir sepeti korunuyor mu" olduğu için giriş **test edilen eylemdir** (`Eğer ki`). Aynı kodu iki farklı anlatı rolünde kullanabilmek, Gherkin'in okunabilirlik değerini bozmadan tekrarı önler. Mülakatta bunu şöyle özetleyebilirsin: "Anahtar kelime çalıştırıcı için değil, okuyucu için."
