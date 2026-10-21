---
slug: scenario-outline-parametrik
baslik: "Scenario Outline + Examples: Parametrik Negatif Test Tasarımı"
kademe: Orta
sira: 201
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

**Parametrik senaryo şablonu (Scenario Outline)**, aynı adım dizisini farklı veri kümeleriyle tekrar tekrar koşturmanın Cucumber/Gherkin karşılığıdır. Türkçe Gherkin'de adı `Senaryo taslağı`dır. Adımlar bir kez yazılır, değişen değerler yer tutucuyla (`<placeholder>`) işaretlenir.

Şablonun altındaki **Örnekler (Examples)** tablosunun her satırı, o şablondan üretilen **bağımsız bir senaryo örneğidir**. Her satırın kendi adı, kendi sonucu ve kendi raporu vardır.

Bu projede vaka çalışmasının (case study) zorunlu negatif giriş senaryoları bu yapıyla yazılmıştır. *Hatalı şifre*, *eksik haneli telefon* ve *boş telefon* durumlarının adım dizileri birebir aynıdır; bu yüzden tek bir `Senaryo taslağı` altında toplanmışlardır. E-posta ile ilgili iki negatif durum farklı adımlar içerdiği için ayrı `Senaryo:` blokları olarak bırakılmıştır.

Buradaki parametrik veri tasarımı salt bir sözdizimi alıştırması değildir. Testler **gerçek bir hesaba karşı** koştuğu için hesabın kilitlenme riskiyle de hesaplaşmak gerekmiştir.

### Temel kavramlar

- **`Senaryo taslağı` + `<placeholder>`.** Adım metninde açılı ayraç içinde yazılmış (`<kullanıcı>`) bir isim geçer; bu isim, Examples tablosundaki aynı isimli sütundan değer alır. Aynı taslak, tablo kaç satırsa o kadar kez — her seferinde farklı değerlerle — çalıştırılır.
- **`Örnekler:` tablosu.** İlk satır sütun başlıklarıdır (`kullanıcı`, `hata mesajı`). Sonraki her satır bağımsız bir test örneğidir. Bir satır başarısız olursa diğerleri etkilenmez; her biri ayrı ayrı raporlanır. Java dünyasındaki `@ParameterizedTest` ile aynı fikirdir.
- **Aynı isimli senaryoları ayırt etme sorunu.** Bir şablonun tüm örnek satırları **aynı senaryo adını** paylaşır, çünkü hepsi aynı `Senaryo taslağı:` başlığından türer. Raporda "Hatalı bilgilerle giriş denemesi engellenir" satırı üç kez görünür. Hangi satırın hangi durumu test ettiği isimden anlaşılmaz.
- **Çözüm: ilk adımı isme eklemek.** Bu projedeki `readable-formatter.js` dosyası bu sorunu çözer. Aynı isimli pickle varsa senaryo adının sonuna ilk adımın metnini ekler. (Pickle, Cucumber'ın koşturduğu somut senaryo örneğidir.) Çıktı şöyle olur: `"Hatalı bilgilerle giriş denemesi engellenir → hatalı şifre kullanıcısı ile giriş denenir"`. Böylece üç satır birbirinden ayrılır.
- **Gerçek hesaba karşı test etmenin veri tasarımına etkisi.** Site, hatalı şifre denemelerini sayar ve "Kalan deneme hakkınız: 4" gibi bir uyarı gösterir. Hesap kilitlenmesin diye örnek tablosuna "hatalı şifre" yalnızca **bir kez** konmuştur. Gerçek hesaba tek bir yanlış deneme gider. Diğer iki negatif durum (eksik haneli telefon, boş telefon) telefon **biçim** kontrolünde takılır ve akış şifre adımına hiç ulaşmaz. `fixtures/users.js` dosyasında bu iki kullanıcının `password` alanı yoktur; bu yüzden hesaba hiçbir şifre denemesi gönderilmez.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Scenario Outline: taslak, Examples tablosu ve üretilen üç bağımsız senaryo">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir taslak → üç bağımsız senaryo</text>
  <rect x="180" y="40" width="280" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Senaryo taslağı</text>
  <text x="320" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"&lt;kullanıcı&gt;" ile giriş denenir</text>
  <line x1="320" y1="92" x2="320" y2="112" stroke="#5D7789" stroke-width="1.4"/>
  <rect x="120" y="112" width="400" height="46" rx="8" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.3"/>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Örnekler: hatalı şifre | eksik haneli telefon | boş telefon</text>
  <line x1="200" y1="158" x2="140" y2="182" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="320" y1="158" x2="320" y2="182" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="440" y1="158" x2="500" y2="182" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="50" y="182" width="180" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="140" y="207" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">senaryo #1: hatalı şifre</text>
  <rect x="230" y="182" width="180" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="320" y="207" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">senaryo #2: eksik hane</text>
  <rect x="410" y="182" width="180" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="500" y="207" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">senaryo #3: boş telefon</text>
</svg>
```

### Nasıl çalışır?

Projedeki gerçek `login_negative.feature` dosyası şöyledir:

```gherkin
Senaryo taslağı: Hatalı bilgilerle giriş denemesi engellenir
  Eğer ki "<kullanıcı>" kullanıcısı ile giriş denenir
  O zaman "<hata mesajı>" metninin görünür olduğu kontrol edilir
  Ve kullanıcının misafir durumunda olduğu doğrulanır

  Örnekler:
    | kullanıcı            | hata mesajı                                                |
    | hatalı şifre         | Kullanıcı adı veya parolanız hatalıdır                     |
    | eksik haneli telefon | Lütfen 10 haneli olan geçerli bir telefon numarası giriniz |
    | boş telefon          | Lütfen 10 haneli olan geçerli bir telefon numarası giriniz |
```

Üç satır da aynı `Senaryo taslağı:` adını taşır. Bu yüzden ham Cucumber çıktısında hepsi "Hatalı bilgilerle giriş denemesi engellenir" olarak görünür. Hangisinin hangi durumu kapsadığı isimden anlaşılmaz. Proje bunu `reporters/readable-formatter.js` içindeki `describe()` metoduyla çözer:

```js
// Scenario Outline satırlari ayni ismi paylasiyor. Cucumber tum pickle'lari
// kosum baslamadan yayinladigi icin ismin tekrar edip etmedigi burada bilinir;
// tekrar ediyorsa ilk adim eklenerek satirlar ayirt edilebilir hale gelir.
describe(pickle) {
  const duplicated = [...this.pickles.values()]
    .filter((other) => other.name === pickle.name).length > 1;
  if (!duplicated || !pickle.steps.length) {
    return pickle.name;
  }
  return `${pickle.name} → ${pickle.steps[0].text}`;
}
```

Sonuç, konsolda şuna benzer ayırt edici bir satırdır: `"Hatalı bilgilerle giriş denemesi engellenir → hatalı şifre kullanıcısı ile giriş denenir"`. İlk adımın metninde `<kullanıcı>` yer tutucusu yerine gerçek değer geçmiştir. Bu yüzden o metin doğal bir ayraç görevi görür.

İkinci ve daha kritik tasarım kararı **veri kümesinin kendisiyle** ilgilidir. Site arka planda hatalı şifre denemelerini sayar ve kullanıcıya "Kalan deneme hakkınız: 4" gibi bir uyarı gösterir. Belli bir eşikten sonra hesap kilitlenir.

Bu durum, gerçek bir hesaba karşı koşan bir otomasyon için doğrudan risktir. Örnek tablosuna "hatalı şifre" durumunu birkaç farklı yanlış şifreyle çoğaltmak, her koşumda hesaba fazladan bir başarısız deneme göndermek demektir. Bu yüzden tasarım bilinçli olarak dengesizdir:

- **"hatalı şifre"** → gerçek hesaba, gerçek (doğru) telefon numarasıyla, **tek bir** yanlış şifre gönderilir.
- **"eksik haneli telefon"** ve **"boş telefon"** → akış telefon **biçim** kontrolünde takılır, şifre adımına hiç ulaşmaz. `fixtures/users.js` dosyasında bu iki kullanıcı için `password` tanımlı değildir. Dolayısıyla `auth.steps.js` içindeki `if (user.password !== undefined)` koşulu sağlanmaz ve `submitPassword` hiç çağrılmaz. Hesabın deneme sayacı artmaz; bu satırlar istendiği kadar tekrar koşabilir.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Negatif Examples satırlarının hesaba dokunma riski karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi satır gerçek hesaba dokunur?</text>
  <rect x="40" y="46" width="260" height="118" rx="11" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="170" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">hatalı şifre</text>
  <text x="170" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gerçek telefon + yanlış şifre</text>
  <text x="170" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sunucuya ulaşır, deneme sayılır</text>
  <text x="170" y="136" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">→ tek satır, tek deneme</text>
  <rect x="340" y="46" width="260" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">eksik hane / boş telefon</text>
  <text x="470" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">telefon format kontrolünde takılır</text>
  <text x="470" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">şifre adımına hiç ulaşılmaz</text>
  <text x="470" y="136" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ sayaç artmaz, kilitlenme riski yok</text>
</svg>
```

Buradaki ders şudur: parametrik tablonun her satırı eşit maliyetli değildir. Tabloya yeni bir satır eklemek görünüşte bedavadır. Ama satırın arkasındaki gerçek sistem davranışı — burada deneme sayacı — farklı satırlara farklı riskler yükler.

### Mülakatta ne sorulur?

- **"Scenario Outline + Examples ne zaman kullanılır, `<placeholder>` nasıl çalışır?"** Beklenen: aynı adım dizisi farklı veri kümeleriyle tekrarlanacaksa kullanılır; adımdaki `<isim>` Examples tablosundaki aynı başlıklı sütundan değer alır, her satır bağımsız bir senaryo örneği üretir.
- **"Raporda üç satır aynı isimle görünüyor, bunu nasıl çözdün?"** Beklenen: `readable-formatter.js`'teki `describe()` metodu, aynı isimli pickle'lar varsa senaryo adının sonuna ilk adımın metnini ekleyerek satırları ayırt edilebilir hale getiriyor.
- **"Neden Examples tablosunda 'hatalı şifre' yalnızca bir kez var, birden çok yanlış şifre varyasyonu eklemedin mi?"** Beklenen: site hatalı şifre denemelerini sayıyor ve hesabı kilitleyebiliyor; gerçek hesaba karşı test edildiği için deneme sayısı bilinçli olarak sınırlı tutuluyor.
- **"Bütün negatif durumlar gerçek hesaba mı gidiyor?"** Beklenen: hayır. Yalnızca "hatalı şifre" satırı hesaba başarısız bir şifre denemesi gönderir. Diğer iki satırda akış telefon biçim kontrolünde takılır. `fixtures/users.js` dosyasında `password` tanımlı olmadığı için `submitPassword` hiç çağrılmaz ve deneme sayacı artmaz.
- **"Scenario Outline'ın parametrik testten (ör. JUnit `@ParameterizedTest`) farkı ne?"** Beklenen: kavramsal olarak aynı fikir — her satır bağımsız örnek, biri kalsa diğerleri koşar — ama Gherkin'de bu, iş dilinde okunabilir bir tabloya (Examples) karşılık gelir; teknik parametre listesine değil.
- **"Examples tablosuna yeni bir negatif durum eklemek her zaman 'bedava' mı?"** Beklenen: hayır; yeni satırın arkasındaki gerçek sistem davranışı (ör. bir sayaç, bir kilitlenme eşiği) farklı maliyet/risk taşıyabilir — veri tasarımı bu riski hesaba katmalı.

### Tek cümlede

Parametrik senaryo şablonu, tek bir adım dizisini yer tutucular üzerinden çok sayıda bağımsız senaryoya çoğaltır. Bu projede raporda ayırt edilebilirlik, ilk adımın senaryo adına eklenmesiyle sağlanmıştır. Veri güvenliği ise gerçek hesaba dokunan tek durumun ("hatalı şifre") bilinçli olarak sınırlı tutulmasıyla sağlanmıştır.

# outline-1 | Orta
## Soru
`login_negative.feature` dosyasındaki `Senaryo taslağı: Hatalı bilgilerle giriş denemesi engellenir` birden fazla satırlık bir `Örnekler:` tablosuyla birlikte tanımlı. Cucumber bu yapıyı çalıştırdığında ortaya kaç bağımsız senaryo örneği (pickle) çıkar?
## A
Bir — tüm satırlar tek bir senaryo içinde sırayla değerlendirilir, ortak bir sonuç üretir
🤖 Scenario Outline böyle çalışmaz; her Examples satırı ayrı, bağımsız bir senaryo örneği üretir.
## B
Üç — Examples tablosundaki her satır, taslaktan türeyen bağımsız bir senaryo örneğidir
🤖 Doğru: taslak + üç satırlık tablo, her biri kendi adımları/sonucuyla koşan üç ayrı pickle üretir.
## C
Dört — taslağın kendisi de ayrı bir senaryo olarak koşulur, tablo satırlarına ek olarak
🤖 Taslağın kendisi bağımsız koşulmaz; yalnızca Examples satırları somut senaryo üretir.
## D
Sıfır — `Senaryo taslağı` yalnız dokümantasyon amaçlıdır, gerçek koşum için ayrı `Senaryo:` bloğu gerekir
🤖 `Senaryo taslağı` dokümantasyon değildir; Examples tablosuyla birleşip gerçekten koşulan senaryolar üretir.
## Doğru
B
## Açıklama
Scenario Outline (Senaryo taslağı), `<placeholder>` içeren bir adım şablonudur; kendi başına koşulmaz. Altındaki `Örnekler:` tablosunun her veri satırı, şablondaki yer tutucuları o satırın değerleriyle doldurup **bağımsız bir senaryo örneği (pickle)** üretir. Bu projedeki tabloda üç satır (hatalı şifre, eksik haneli telefon, boş telefon) olduğu için üç ayrı senaryo koşulur; her biri kendi adımlarını çalıştırır, kendi sonucunu (geçti/kaldı) üretir ve raporda ayrı bir satır olarak görünür — biri başarısız olsa diğer ikisi etkilenmez.

# outline-2 | Orta
## Soru
Aynı Scenario Outline'dan türeyen üç senaryo, ham Cucumber çıktısında hepsi `"Hatalı bilgilerle giriş denemesi engellenir"` adıyla görünüyor. Bu projedeki `readable-formatter.js`, bu ayırt edilemezlik sorununu nasıl çözüyor?
## A
Her Examples satırına elle benzersiz bir isim sütunu eklenerek, senaryo adı bu sütundan okunuyor
🤖 Tabloya elle isim sütunu eklenmedi; çözüm formatter'ın çalışma zamanında ürettiği bir isimlendirmedir.
## B
Cucumber'ın kendi varsayılan raporlama motoru bu ayrımı otomatik olarak yapıyor, ek koda gerek kalmıyor
🤖 Varsayılan motor bu ayrımı otomatik yapmaz; proje bunun için özel bir formatter yazmıştır.
## C
Aynı isimli pickle'lar tespit edilip senaryo adının sonuna o satırın ilk adımının metni ekleniyor
🤖 Doğru: `describe()` metodu, isim tekrar ediyorsa `pickle.name` sonuna `pickle.steps[0].text`'i ekleyerek satırları ayırt edilebilir yapıyor.
## D
Her satır ayrı bir `.feature` dosyasına bölünerek dosya adı üzerinden ayrım sağlanıyor
🤖 Proje tek bir `.feature` dosyasında Scenario Outline kullanıyor; dosya bölme bir çözüm olarak uygulanmadı.
## Doğru
C
## Açıklama
`readable-formatter.js` içindeki `describe(pickle)` metodu, çalışma zamanında tüm pickle'ları tarar; aynı isme sahip birden fazla pickle varsa (`duplicated` true), senaryo adının sonuna o pickle'ın **ilk adımının metnini** ("hatalı şifre kullanıcısı ile giriş denenir" gibi, çünkü `<kullanıcı>` yerine gerçek değer geçmiştir) `→` işaretiyle ekler. Böylece konsol çıktısında ve olay akışında üç satır `"...engellenir → hatalı şifre..."`, `"...engellenir → eksik haneli telefon..."` gibi ayırt edilebilir hale gelir — tabloya elle bir isim sütunu eklemeye gerek kalmaz, çünkü ilk adım zaten doğal bir ayraçtır.

# outline-3 | Orta
## Soru
Site, hatalı şifre girişimlerini sayıyor ve bir eşikten sonra hesabı kilitliyor. Bu proje Examples tablosunda "hatalı şifre" durumunu neden yalnızca **bir** satırla temsil ediyor, birkaç farklı yanlış şifre varyasyonuyla değil?
## A
Gherkin sözdizimi, aynı sütunda birden fazla benzer değer bulunmasına izin vermiyor
🤖 Gherkin'in böyle bir sözdizimi kısıtlaması yoktur; Examples tablosunda istenildiği kadar benzer satır olabilir.
## B
Birden fazla "hatalı şifre" satırı eklemek CI çalışma süresini kabul edilemez ölçüde uzatırdı
🤖 Süre kaygısı burada belirleyici değildir; asıl kısıt hesabın kilitlenme riskidir, birkaç ek satır süreyi anlamlı ölçüde etkilemez.
## C
Playwright, aynı test dosyasında birden fazla başarısız login denemesini teknik olarak desteklemiyor; ikinci deneme sessizce atlanıyor
🤖 Playwright'ın böyle bir kısıtlaması yoktur; sorun teknik değil, test edilen gerçek hesabın durumudur.
## D
Testler gerçek bir hesaba karşı koşuyor; her ek "hatalı şifre" satırı hesaba bir başarısız deneme daha gönderirdi
🤖 Doğru: deneme sayacı gerçek, tekrar eden yanlış şifre denemeleri hesabı kilitleme eşiğine yaklaştırır — veri tasarımı bu riski azaltmak için bilinçli olarak tek satırla sınırlanmış.
## Doğru
D
## Açıklama
Bu proje, negatif senaryoları **gerçek** bir hesaba karşı test eder; sahte/mock bir kimlik doğrulama katmanı yoktur. Site arka planda başarısız şifre denemelerini sayar ve kullanıcıya "Kalan deneme hakkınız: 4" gibi bir uyarı gösterir; belli bir eşikten sonra hesap kilitlenir. Examples tablosuna "hatalı şifre" için birden fazla varyasyon eklemek, her test koşusunda hesaba ek başarısız denemeler göndermek anlamına gelirdi — bu, testin kendisinin hesabı kilitleyip sonraki tüm testleri (ve gerçek kullanıcıyı) etkileme riski taşırdı. Bu yüzden tasarım bilinçli olarak tek bir "hatalı şifre" satırıyla sınırlanmış; diğer negatif durumlar ise hesaba hiç dokunmayan bir yolla (format doğrulaması) test edilerek sınırsız tekrarlanabilir kalmıştır.

# outline-4 | Orta
## Soru
Aynı `Senaryo taslağı`'ndaki "eksik haneli telefon" ve "boş telefon" satırları, "hatalı şifre" satırının aksine sınırsız sayıda tekrar koşulabilir. Bu farkın temel nedeni nedir?
## A
Bu iki durumda akış telefon format kontrolünde takılıp şifre adımına hiç ulaşmaz; hesabın deneme sayacı artmaz
🤖 Doğru: `fixtures/users.js`'te bu iki kullanıcı için `password` alanı yoktur, bu yüzden `auth.steps.js`'teki `if (user.password !== undefined)` koşulu sağlanmaz ve `submitPassword` hiç çağrılmaz — akış şifre adımına hiç ulaşmaz, deneme sayacı artmaz.
## B
Bu iki durum ayrı bir test ortamında (staging) koşulduğu için gerçek hesap hiç devreye girmez
🤖 Proje tek bir test ortamı kullanır; ayrım staging/production değil, doğrulamanın nerede durduğudur.
## C
Cucumber, aynı senaryo adı altında yalnızca ilk satırı gerçek sunucuya gönderir, diğerlerini otomatik olarak simüle eder
🤖 Cucumber böyle bir otomatik simülasyon yapmaz; her satır gerçekten kendi adımlarını çalıştırır.
## D
Bu iki durum farklı bir tarayıcı bağlamında (incognito) çalıştığı için hesap durumu sıfırlanır
🤖 Tarayıcı bağlamı deneme sayacını sıfırlamaz; sayaç sunucu tarafında, hesaba bağlı olarak tutulur.
## Doğru
A
## Açıklama
"Eksik haneli telefon" ve "boş telefon" durumlarında beklenen hata mesajı `"Lütfen 10 haneli olan geçerli bir telefon numarası giriniz"`dir — bu, kimlik doğrulama sonucu değil, telefon **format** kontrolünün ürettiği bir mesajdır. Akış şifre adımına hiç ulaşmaz: `fixtures/users.js`'te bu iki kullanıcı için `password` alanı yoktur, bu yüzden `auth.steps.js`'teki `if (user.password !== undefined)` koşulu sağlanmaz ve `submitPassword` hiç çağrılmaz. Sonuç: hesaba **başarısız şifre denemesi** gitmez, deneme sayacı artmaz — bu iki satır istenildiği kadar tekrar koşulabilir. "Hatalı şifre" satırı ise gerçek telefon numarasıyla şifre adımına kadar ilerler ve gerçek bir başarısız deneme olarak sayılır.

# outline-5 | Orta
## Soru
`login_negative.feature` dosyasındaki Examples tablosunun sütun başlıkları `kullanıcı` ve `hata mesajı`dır; adımlarda `"<kullanıcı>"` ve `"<hata mesajı>"` yer tutucuları kullanılır. `<kullanıcı>` yer tutucusunun değerleri (`hatalı şifre`, `eksik haneli telefon`, `boş telefon`) nereden karşılığını bulur?
## A
Doğrudan `pages/locators.js` içinde, her biri bir CSS seçiciye karşılık gelen anahtarlar olarak tanımlıdır; step ismi bu dosyadan çözer
🤖 `locators.js` CSS seçicileri tutar; kullanıcı senaryolarının verisi ayrı bir dosyada, `fixtures/users.js`'te tanımlıdır.
## B
Cucumber, `<kullanıcı>` yer tutucusunu otomatik olarak `.env` dosyasındaki ortam değişkenleriyle eşler
🤖 Cucumber'ın böyle bir otomatik ortam değişkeni eşlemesi yoktur; eşleme uygulamaya özgü `users` sözlüğü üzerinden yapılır.
## C
`fixtures/users.js` içindeki `users` sözlüğünde aynı isimli anahtarlar olarak tanımlıdır; step bu sözlükten veriyi çeker
🤖 Doğru: `'{string} kullanıcısı ile giriş denenir'` step'i, gelen ismi (`hatalı şifre` vb.) `users[userKey]` ile sözlükte arayıp karşılığındaki telefon/şifre bilgisini kullanır.
## D
Değerler doğrudan adımın kendisinde yorumlanır, ayrı bir veri kaynağına ihtiyaç yoktur
🤖 Değerler yalnızca birer isimdir; gerçek telefon/şifre verisini çözümlemek için `fixtures/users.js`'teki sözlüğe ihtiyaç vardır.
## Doğru
C
## Açıklama
Examples tablosundaki `<kullanıcı>` sütunu yalnızca birer **isim** taşır (`hatalı şifre`, `eksik haneli telefon`, `boş telefon`); bu isimlerin arkasındaki gerçek telefon/şifre verisi `fixtures/users.js` içindeki `users` sözlüğünde tanımlıdır. `auth.steps.js`'teki `When('{string} kullanıcısı ile giriş denenir', async function (userKey) { const user = users[userKey]; ... })` step'i, Examples'tan gelen ismi bu sözlükte arar ve karşılığındaki `phone`/`password` alanlarını kullanır. Bu ayrım, senaryo dosyasını okunabilir tutarken gerçek test verisini (özellikle gerçek hesap bilgilerini, `.env`'den gelen) koddan ayrı bir yerde yönetmeyi sağlar.

# outline-6 | Orta
## Soru
`login_negative.feature` dosyasında negatif login senaryoları hem `Senaryo taslağı` (Scenario Outline) hem de ayrı `Senaryo:` (tekil senaryo) blokları olarak yazılmış: "kayıtlı olmayan e-posta" ve "geçersiz formatlı e-posta" ayrı `Senaryo:` bloklarıdır, oysa "hatalı şifre/eksik hane/boş telefon" tek bir `Senaryo taslağı` altında toplanmıştır. Bu ayrımın olası nedeni nedir?
## A
Scenario Outline yalnızca üç veya daha az Examples satırı olduğunda kullanılabilir, e-posta senaryoları bu sınırı aştığı için ayrı yazılmıştır
🤖 Scenario Outline'da satır sayısı sınırı yoktur; ayrım satır sayısından değil, adım dizisinin ortaklığından kaynaklanır.
## B
E-posta senaryoları performans nedeniyle ayrı tutulmuştur, çünkü Scenario Outline testleri daha yavaş çalıştırır
🤖 Scenario Outline'ın kendisi ek bir performans maliyeti getirmez; her satır zaten bağımsız bir senaryo olarak koşardı.
## C
Gherkin'de bir `.feature` dosyasında yalnızca bir tane `Senaryo taslağı` bloğu bulunabilir, bu yüzden geri kalanı `Senaryo:` olarak yazılmıştır
🤖 Bir `.feature` dosyasında birden fazla `Senaryo taslağı` bloğu bulunabilir; bu bir sözdizimi kısıtlaması değildir.
## D
İlk üç durum tamamen aynı adım dizisini paylaşırken, e-posta senaryoları farklı adımlar ve farklı bir alan akışı içerir
🤖 Doğru: Scenario Outline, adım dizisi birebir aynı (giriş dene → hata mesajını kontrol et → misafir durumunu doğrula) kaldığında anlamlıdır; e-posta senaryoları kayıt formunun açılması gibi farklı adımlar içerdiği için ayrı yazılmıştır.
## Doğru
D
## Açıklama
Scenario Outline'ı anlamlı kılan şey, adım dizisinin **birebir aynı kalıp yalnızca verinin değişmesidir**. "Hatalı şifre", "eksik haneli telefon" ve "boş telefon" durumlarının üçü de aynı üç adımı izler: giriş dene, hata mesajını kontrol et, misafir durumunu doğrula — yalnızca kullanıcı ve beklenen mesaj değişir. "Kayıtlı olmayan e-posta" senaryosu ise farklı bir akış izler (e-posta sekmesine tıklanır, kayıt formunun açıldığı doğrulanır — hata mesajı değil), "geçersiz formatlı e-posta" senaryosu da kendi özel adımlarını içerir. Bu adımları zorla ortak bir Scenario Outline'a sıkıştırmak, `<placeholder>`'ları gereksiz yere karmaşıklaştırır ve okunabilirliği azaltırdı; bu yüzden yalnızca gerçekten aynı adım dizisini paylaşan durumlar taslağa alınmış, farklı akışlar ayrı `Senaryo:` blokları olarak bırakılmıştır.
