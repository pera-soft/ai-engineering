---
slug: assertion-derinligi-sepet
baslik: "Assertion Derinliği: Sepet Toplamı Doğrulaması"
kademe: İleri
sira: 204
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir testin "geçmesi" ile bir testin **doğru şeyi** doğrulaması aynı şey değildir. Vaka çalışmasının (case study) S4 gereksinimi bunu açıkça istiyordu: fiyat metinleri ayrıştırılacak (para birimi, binlik ayracı) ve sayısal olarak karşılaştırılacak. Ekran görüntüsü karşılaştırması veya salt metin eşitliği yeterli sayılmıyordu.

Bu istek sınav sorusu gibi görünse de gerçek bir mühendislik tuzağını hedefler. Metin karşılaştırması ve ekran görüntüsü biçime bağlıdır; dil ayarı veya dinamik veri değişince kırılır. Daha kötüsü, asıl doğrulanmak istenen şeyi hiç sınamaz: sepet **matematiğinin** doğruluğunu.

Bu bölümde üç konu var. Birincisi, `utils/price.js` içindeki fiyat ayrıştırma mantığında bulunmuş gerçek ve tehlikeli bir hata. İkincisi, üç bağımsız sayısal kontrol. Üçüncüsü, mutlak yerine göreli (delta) doğrulama tercihi.

### Temel kavramlar

- **Doğrulama (assertion)**: testin "beklenen değer şu, gerçekleşen değer bu" diye karşılaştırdığı satır. Testin asıl işi burada yapılır.
- **Sayısal karşılaştırma, metin/görsel karşılaştırmaya karşı**: fiyat metinleri (`"1.299,90 TL"`) önce sayıya çevrilir (`parsePrice`). Doğrulama bu sayılar üzerinde yapılır; metin eşitliği veya ekran görüntüsü farkı üzerinden değil.
- **"İlk sayı" tuzağı**: bir özet metni (`"Ürünler Toplamı (2 Ürün) 1.299,90 TL"`) tek parça okunduğunda, etikette geçen sayı (adet) fiyatla karıştırılabilir.
- **Sessiz mantık hatası**: hata çökme (exception) üretmez, **geçerli ama yanlış** bir sayı üretir. Hiçbir doğrulama patlamaz; test "geçer", ama yanlış bir temel üzerinde geçer.
- **Üç bağımsız kontrol**: ara toplam = satır tutarlarının toplamı; adet +1 → ara toplam + birim fiyat; satır silme → ara toplam − silinen tutar.
- **Birim testle korunan ayrıştırma mantığı**: `parsePrice` gibi saf bir fonksiyon ayrıca birim testle sınanır. Uçtan uca (end-to-end, e2e) test bu hata sınıfını yapısal olarak göremez.
- **Mutlak doğrulama ve göreli (delta) doğrulama**: "toplam == sabit tutar" yerine "toplamdaki değişim == beklenen fark". Canlı ortamda fiyat ve kampanya değiştiği için ikincisi sağlamdır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="parsePrice tuzağı: ilk sayı ile para birimi taşıyan son sayı arasındaki fark">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Özet metninde hangi sayı fiyattır?</text>
  <rect x="70" y="42" width="500" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="320" y="64" text-anchor="middle" font-size="12" fill="#1C3247" font-family="monospace">"Ürünler Toplamı (2 Ürün) 1.299,90 TL"</text>
  <path d="M170,76 C170,100 130,100 130,118" fill="none" stroke="#B5504F" stroke-width="1.4"/>
  <rect x="30" y="118" width="220" height="58" rx="10" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="140" y="140" text-anchor="middle" font-size="11" font-weight="700" fill="#B5504F" font-family="system-ui">İlk sayı: "2"</text>
  <text x="140" y="158" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">yanlış — adet, fiyat değil</text>
  <path d="M470,76 C470,100 510,100 510,118" fill="none" stroke="#25804C" stroke-width="1.4"/>
  <rect x="390" y="118" width="220" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="140" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Para birimi taşıyan SON sayı</text>
  <text x="500" y="158" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">doğru — "1.299,90 TL"</text>
</svg>
```

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mutlak doğrulama ile göreli (delta) doğrulama karşılaştırması">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Canlı ortamda: mutlak mı, göreli mi?</text>
  <rect x="30" y="40" width="270" height="130" rx="12" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="165" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#B5504F" font-family="system-ui">Mutlak: toplam == 1.299,90 TL</text>
  <text x="165" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kampanya değişince testin</text>
  <text x="165" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">mantığı doğru olsa bile kırılır</text>
  <text x="165" y="130" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">→ canlı ortamda kırılgan</text>
  <rect x="340" y="40" width="270" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Göreli: Δtoplam == birim fiyat</text>
  <text x="475" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hesaplama hatasını yakalar,</text>
  <text x="475" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">fiyat değişiminden etkilenmez</text>
  <text x="475" y="130" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ canlı ortamda sağlam</text>
</svg>
```

### Nasıl çalışır?

**`utils/price.js` — ayrıştırma mantığı.** Fiyatlar `"1.299,90 TL"` biçiminde geliyor: nokta binlik ayracı, virgül ondalık ayracı. `parsePrice` bunu sayıya çeviriyor:

```js
const WITH_CURRENCY = new RegExp(`${AMOUNT}\\s*(?:TL|₺)`, 'g');
const BARE_AMOUNT = new RegExp(AMOUNT);

function parsePrice(text) {
  // \u00A0 = kırılmayan boşluk (non-breaking space): site fiyatı "1.299,90 TL"
  // biçiminde basıyor, bu satır onu normal boşluğa çeviriyor.
  const normalizedText = String(text).replace(/\u00A0/g, ' ');
  const withCurrency = normalizedText.match(WITH_CURRENCY);
  const raw = withCurrency
    ? withCurrency[withCurrency.length - 1]
    : (normalizedText.match(BARE_AMOUNT) || [])[0];
  // ... temizle, sayıya çevir
}
```

REVIEW.md bu dosyada gerçek bir hatayı belgeliyor. `subtotal()` özet satırının tamamını metin olarak okuyup **ilk** sayıyı fiyat kabul ediyordu. Satır `Ürünler Toplamı (2 Ürün) 1.299,90 TL` biçiminde gelirse, tüm sepet doğrulaması `2` sayısı üzerinden çalışırdı. Üstelik bunu sessizce, hiçbir uyarı vermeden yapardı.

Düzeltme basit oldu: ilk sayıyı değil, **para birimi taşıyan sonuncu** eşleşmeyi almak. `"(2 Ürün)"` içindeki `2`'nin ardından `TL` veya `₺` gelmez. Bu yüzden `WITH_CURRENCY` deseni o sayıyı hiç eşleştirmez; yalnızca `"1.299,90 TL"` eşleşir.

Kodun ilk satırındaki `replace(/\u00A0/g, ' ')` çağrısı da önemli. Site fiyatı basarken sayı ile `TL` arasına kırılmayan boşluk (non-breaking space) koyar. Bu görünmez karakter temizlenmezse desen eşleşmesi bozulabilir. Kod alıntısında gözle görülmediği için kolayca gözden kaçar; mülakatta "bu satır ne yapıyor?" sorusuna verilebilecek en somut cevaplardan biridir.

**Neden bu hata özellikle tehlikeliydi?** Çünkü çökme değil, **sessiz ve sayısal olarak geçerli** bir yanlıştı. `2` de meşru bir sayıdır; test kırmızı yanmaz. Sadece yanlış bir tutar üzerinde "tutarlı" çalışmaya devam eder.

Bu sınıf hatalar, açıkça hata fırlatan hatalardan daha tehlikelidir. Fark edilmeleri için ayrı bir doğrulama katmanı gerekir. Bu yüzden `utils/price.test.js` ayrı bir **birim test** paketiyle korunuyor (`npm run test:unit`, `node --test utils/`):

```js
test('etiketteki adet değil, tutar okunur', () => {
  assert.equal(parsePrice('Ürünler Toplamı\n1.299,90 TL'), 1299.9);
  assert.equal(parsePrice('Ürünler Toplamı (2 Ürün)\n1.299,90 TL'), 1299.9);
  assert.equal(parsePrice('2 Ürün 349,97 TL'), 349.97);
});
```

**Üç bağımsız sayısal kontrol (`pages/CartPage.js`).** İki yardımcı metot var. `subtotal()` özet metnindeki tutarı sayıya çevirir. `sumOfLineTotals()` ise her satırın tutarını `lineTotal()` ile okuyup toplar; `lineTotal()` indirimli satırda `.old-price`, indirimsiz satırda `.product-price` seçicisini kullanır:

```js
async subtotal() {
  const row = this.locator(locators.cart.summaryItem).filter({ hasText: 'Ürünler Toplamı' }).first();
  await row.waitFor({ state: 'visible' });
  return parsePrice(await row.innerText());
}

async sumOfLineTotals() {
  const count = await this.lineCount();
  let total = 0;
  for (let i = 0; i < count; i += 1) { total += await this.lineTotal(i); }  // … kısaltıldı
  return total;
}
```

Bu iki metot üzerine üç kontrol kurulur. (1) `subtotal() === sumOfLineTotals()` mi? (2) Adet bir artırıldığında `subtotal()` tam birim fiyat kadar mı yükseliyor? (3) Bir satır silindiğinde `subtotal()` o satırın tutarı kadar mı düşüyor?

**Mutlak değil, göreli doğrulama.** REVIEW.md şöyle diyor: "Ara toplam doğrulaması mutlak değil, göreli yapıldı." Gerekçe şu: kampanyalar ve fiyatlar canlı ortamda değişir. Sabit bir tutar beklemek yerine değişim miktarı (delta) doğrulanır. Böylece hesaplama hatası yakalanır, fiyat değişimi ise testi kırmaz.

### Mülakatta ne sorulur?

- **"Neden sepet toplamını ekran görüntüsüyle karşılaştırmadın?"** Beklenen: görsel/metin karşılaştırma yerelleştirmeye ve dinamik veriye karşı kırılgan; asıl doğrulanmak istenen matematiktir, string eşitliği değil.
- **"Fiyat ayrıştırmada ne tür bir hata buldun?"** Beklenen: `subtotal()` özet metninin ilk sayısını (adet etiketi) fiyat sanıyordu; düzeltme para birimi taşıyan son sayıyı almak oldu.
- **"Bu hatayı neden e2e testler yakalayamadı, ilk etapta nasıl fark edildi?"** Beklenen: hata sessizdi — `2` geçerli bir sayı, hesaplamalar bu yanlış temel üzerinde tutarlı kalıyordu; kod incelemesinde/birim testte fark edildi.
- **"`test:unit` ile e2e testler arasındaki iş bölümü nedir?"** Beklenen: birim test saf `parsePrice` mantığını izole sınar; e2e, sayfa akışını ve gerçek DOM etkileşimini sınar; ikisi farklı hata sınıflarını yakalar.
- **"Sepet toplamını nasıl doğruluyorsun?"** Beklenen: üç bağımsız sayısal kontrol — ara toplam = satır toplamı, adet artışı = birim fiyat kadar delta, silme = o satırın tutarı kadar delta.
- **"Neden sabit bir tutar (`== 1.299,90 TL`) beklemedin?"** Beklenen: canlı sitede kampanya/fiyat her an değişebilir; mutlak beklenti testi anlamsız yere kırar, göreli (delta) doğrulama hesaplama hatasını yakalar ama fiyat değişiminden etkilenmez.

### Tek cümlede

Doğrulama (assertion) derinliği şudur: fiyat metnini sayıya çevir, sepet matematiğini üç bağımsız ilişkiyle sına, ayrıştırma mantığını birim testle koru ve sabit tutar yerine **göreli (delta)** doğrulama kullan.

# sepet-1 | Orta
## Soru
Case study'nin S4 gereksinimi şöyle: "Fiyat metinlerinin parse edilmesi ve sayısal karşılaştırma beklenmektedir; ekran görüntüsü karşılaştırması veya salt metin eşitliği yeterli değildir." Bu gereksinimin arkasındaki asıl gerekçe nedir?
## A
Ekran görüntüsü karşılaştırması Playwright'ın desteklemediği bir özellik; gereksinim bu araç eksikliğinden doğuyor
🤖 Playwright görsel karşılaştırmayı (`toHaveScreenshot`) destekler; gereksinim araç eksikliğinden değil, bu senaryo için uygun olmamasından kaynaklanır.
## B
Metin eşitliği kontrolü sayısal kontrolden daha yavaş çalışır, performans için sayısal karşılaştırma tercih edilmeli
🤖 Performans farkı burada ölçülebilir/anlamlı değil; asıl sorun hız değil, doğruluk ve kırılganlık.
## C
Metin eşitliği yalnız İngilizce sayı biçimlerinde (nokta ondalık ayracı) çalışır, Türkçe biçimlerde hata verir
🤖 Metin eşitliği dil/biçimden bağımsız olarak zaten yanlış yaklaşım; sorun yalnızca Türkçe biçimle sınırlı değil.
## D
Metin/görsel eşitliği biçim farklarında (boşluk, para simgesi) kırılır ve sepet matematiğini hiç sınamaz
🤖 Doğru: sayısal karşılaştırma hem yerelleştirme farklılıklarından etkilenmez hem de asıl istenen şeyi — matematiğin doğruluğunu — doğrudan doğrular.
## Doğru
D
## Açıklama
Metin eşitliği (`"1.299,90 TL" === "1.299,90 TL"`) veya ekran görüntüsü karşılaştırması iki açıdan yetersizdir: **kırılganlık** — biçimlendirmedeki en ufak fark (fazladan boşluk, farklı para birimi simgesi, satır sonu) testi gereksiz yere kırar; ve **yanlış hedef** — asıl doğrulanmak istenen şey bir string'in birebir eşleşmesi değil, sepet **matematiğinin** doğru olmasıdır (ara toplam = satır tutarlarının toplamı gibi). Fiyat metinlerini sayıya çevirip (`parsePrice`) sayısal karşılaştırma yapmak, hem yerelleştirme farklılıklarından (binlik ayracı, ondalık ayracı, para birimi simgesi) etkilenmez hem de gerçek hesaplama hatalarını doğrudan yakalar.

# sepet-2 | İleri
## Soru
`utils/price.js` içindeki `parsePrice` fonksiyonu şu mantıkla yazılmış:
```js
const withCurrency = normalizedText.match(WITH_CURRENCY);
const raw = withCurrency
  ? withCurrency[withCurrency.length - 1]
  : (normalizedText.match(BARE_AMOUNT) || [])[0];
```
Özet satırı `"Ürünler Toplamı (2 Ürün) 1.299,90 TL"` biçiminde geldiğinde bu kod hangi sayıyı fiyat olarak döndürür ve neden?
## A
`2`'yi döndürür; regex metindeki ilk sayıyı her zaman önceliklendirir
🤖 Tam tersi — kod bilinçli olarak ilk sayıyı değil, para birimi taşıyan sonuncu eşleşmeyi tercih edecek şekilde yazılmıştır; bu, önceki (hatalı) sürümün davranışıydı.
## B
`1.299,90`'ı döndürür; `WITH_CURRENCY` yalnızca hemen ardından TL/₺ gelen sayıları eşleştirir, `(2 Ürün)` bu koşulu sağlamadığı için elenir
🤖 Doğru: `2` sayısının ardından para birimi gelmiyor (`Ürün)` geliyor), bu yüzden `WITH_CURRENCY` eşleşmesine girmiyor; yalnız `1.299,90 TL` eşleşiyor ve tek eşleşme olarak seçiliyor.
## C
İki sayıyı da toplayıp `1.301,90` döndürür, çünkü `withCurrency[withCurrency.length - 1]` en son elemanı değil tüm elemanların toplamını alır
🤖 `[withCurrency.length - 1]` dizinin son elemanına erişen standart bir indeks ifadesidir, toplama işlemi yapmaz.
## D
Hata fırlatır, çünkü metinde birden fazla sayı olduğunda `parsePrice` bunu geçersiz girdi sayar
🤖 `parsePrice` yalnızca hiç sayı bulunamazsa hata fırlatır; burada geçerli bir eşleşme (1.299,90 TL) bulunuyor, hata koşulu oluşmuyor.
## Doğru
B
## Açıklama
`WITH_CURRENCY` regex'i yalnızca **hemen ardından** (opsiyonel boşlukla) `TL` veya `₺` gelen sayı örüntülerini eşleştirir. `"(2 Ürün)"` içindeki `2`'den sonra `Ürün)` geldiği için bu koşulu sağlamaz ve eşleşmeye girmez; yalnızca `1.299,90 TL` eşleşir. Kod bu tek eşleşmeyi (`withCurrency[withCurrency.length - 1]`, yani dizinin son elemanı) seçer. Bu tasarım bilinçlidir: özet satırı tek parça metin olarak okunduğunda, etikette geçen bir sayının (adet gibi) fiyat sanılması riskini ortadan kaldırır — önceki (hatalı) sürüm bunun yerine metindeki **ilk** sayıyı alıyordu, ki bu tam olarak `2`'yi (adedi) fiyat sanma hatasına yol açıyordu.

# sepet-3 | İleri
## Soru
REVIEW.md şunu not ediyor: "`subtotal()` özet satırının tamamını metin olarak okuyup ilk sayıyı fiyat kabul ediyordu... tüm sepet doğrulaması `2` üzerinden çalışır, üstelik sessizce yanlış sonuç verirdi." Bu hatanın özellikle tehlikeli olmasının nedeni nedir?
## A
`2` de geçerli bir sayıydı; test hata fırlatmadan, yanlış bir tutar üzerinden sessizce "geçiyordu"
🤖 Doğru: hata bir çökme (crash) değil, sessiz bir yanlış hesaplamaydı; testler "2 TL" üzerinden tutarlı çalışmaya devam ettiği için görünürde her şey normaldi.
## B
Hata yalnızca CI ortamında ortaya çıkıyordu, yerel geliştirme ortamında hiç tetiklenmiyordu
🤖 Hata ortam bağımsızdı — özet satırının biçimi her yerde aynıydı; sorun CI'a özgü değildi.
## C
Hata yalnızca indirimli ürünlerde ortaya çıkıyordu, indirimsiz ürünlerde `subtotal()` doğru çalışıyordu
🤖 Hata özet satırının kendi metin biçimiyle ilgiliydi (adet etiketi + tutar); indirim durumu bu hatayı tetiklemiyordu.
## D
Hata yalnızca sepette tek ürün olduğunda ortaya çıkıyordu, birden fazla üründe fark edilip otomatik düzeltiliyordu
🤖 Otomatik bir düzeltme mekanizması yok; ürün sayısından bağımsız olarak, özet metninde adet etiketi göründüğü her durumda hata oluşurdu.
## Doğru
A
## Açıklama
Bu hatanın tehlikesi bir çökme (crash) olmamasıydı — tam tersine sessizce yanlış ama sayısal olarak geçerli bir değer üretiyordu. `2`, geçerli bir sayıdır; `parsePrice("Ürünler Toplamı (2 Ürün) 1.299,90 TL")` eski mantıkla `2` döndürdüğünde, sepet doğrulaması bu değeri "ara toplam" sanıp diğer hesaplamaları (satır toplamı, adet artışı) bu yanlış temel üzerine kurardı. Hiçbir assertion patlamazdı çünkü mantık **kendi içinde tutarlıydı** — yalnızca yanlış bir sayı üzerinde tutarlıydı. Bu tür "sessiz ama sayısal olarak makul" hatalar, açık bir hata mesajı üreten hatalardan çok daha tehlikelidir çünkü fark edilmeleri için ayrı bir doğrulama katmanı (bu durumda birim test) gerekir.

# sepet-4 | Orta
## Soru
README'ye göre sepet toplamı üç bağımsız kontrolle doğrulanıyor: (1) ara toplam = satır tutarlarının toplamı, (2) adet bir artırıldığında ara toplam tam birim fiyat kadar artıyor mu, (3) bir ürün silindiğinde ara toplam o satırın tutarı kadar azalıyor mu. `CartPage`'teki hangi metot çifti bu üç kontrolü sayısal olarak mümkün kılan temel yapı taşlarıdır?
## A
`titles()` ve `lineCount()` — sepetteki ürün adlarını ve satır sayısını döndürürler
🤖 Bu metotlar isim/sayı bilgisi verir, tutar hesaplamaz; üç kontrol de sayısal tutar karşılaştırmasına dayanır.
## B
`open()` ve `waitForLineCount()` — sepet sayfasını açar ve satır sayısının beklenen değere ulaşmasını bekler
🤖 Bunlar navigasyon/bekleme yardımcılarıdır; tutarların doğru okunup karşılaştırılmasıyla ilgili değildir.
## C
`subtotal()`/`sumOfLineTotals()` — özet metnini ve satır tutarları toplamını `parsePrice` ile sayıya çevirip döndürürler
🤖 Doğru: `subtotal()` özet metnini, `sumOfLineTotals()` her satırı `lineTotal()` ile toplayarak sayısal tutar üretir; üç kontrol de bu iki sayının karşılaştırılmasına dayanır.
## D
`removeLine()` ve `increaseQuantity()` — sepette silme ve adet artırma aksiyonlarını tetikleyip yeni ara toplamı hesaplayan metotlardır
🤖 Bu metotlar aksiyonu tetikler (silme, adet artırma) ama tutarı kendileri hesaplamaz; doğrulama için `subtotal()`/`lineTotal()` gibi okuma metotları gerekir.
## Doğru
C
## Açıklama
Üç kontrolün hepsi, aynı iki temel yapı taşına dayanır: `subtotal()` sepet özetindeki (`"Ürünler Toplamı"`) metni `parsePrice` ile sayıya çevirir; `sumOfLineTotals()` her satırın tutarını `lineTotal()` (yine `parsePrice` üzerinden, indirimli/indirimsiz satır biçimine göre `.old-price` veya `.product-price` okuyarak) ile toplar. (1) `subtotal() === sumOfLineTotals()` — ara toplam satırların toplamına eşit mi; (2) `increaseQuantity()` sonrası yeni `subtotal()` − eski `subtotal()` === birim fiyat mı; (3) `removeLine()` sonrası eski `subtotal()` − yeni `subtotal()` === silinen satırın `lineTotal()`'ı mı. Üçü de metin karşılaştırması değil, `parsePrice` ile elde edilmiş **sayılar** üzerinde aritmetik karşılaştırmadır.

# sepet-5 | Orta
## Soru
Projede uçtan uca (e2e) testlerin yanında `npm run test:unit` (`node --test utils/`) ile ayrı bir birim test paketi de var; `utils/price.test.js` `parsePrice`'ı doğrudan test ediyor. Sepet matematiği zaten e2e senaryolarıyla doğrulanıyorken, `parsePrice` için ayrıca birim test yazmanın gerekçesi nedir?
## A
Birim testler e2e testlerden daha güvenilir (flaky olmaz) olduğu için tüm doğrulama birim teste taşınmalı, e2e gereksizdir
🤖 İkisi birbirinin yerine geçmez: birim test parse mantığını, e2e sayfa/akış davranışını doğrular; e2e'yi gereksiz kılmaz.
## B
`node --test` Playwright'tan daha hızlı çalıştığı için CI süresini kısaltmak amacıyla eklendi, işlevsel bir gerekçesi yok
🤖 Hız bir yan fayda olabilir ama asıl gerekçe değil; asıl gerekçe e2e'nin yakalayamadığı bir hata sınıfını hedeflemesi.
## C
`parsePrice` fonksiyonu tarayıcı ortamında çalışmadığı için e2e testler onu hiç çağırmıyor, bu yüzden ayrı test şart
🤖 `parsePrice` Node tarafında (Playwright test kodu içinde) çalışır ve e2e senaryolarında da (CartPage metotları üzerinden) dolaylı olarak çağrılır; sorun çağrılmaması değil.
## D
e2e yalnız sayıların birbiriyle tutarlı olduğunu görür; sayıların kendisi yanlış ayrıştırılmışsa bunu ayırt edemez
🤖 Doğru: e2e testin gördüğü şey yalnızca "iki sayı tutarlı mı"; sayıların kendisi yanlış ayrıştırılmışsa bile tutarlı kalabilir. Birim test, ayrıştırma mantığını gerçek biçim örnekleriyle (binlik ayracı, adet etiketi) doğrudan sınar.
## Doğru
D
## Açıklama
e2e testler `parsePrice`'ın ürettiği sayıları **birbirleriyle** karşılaştırır (ara toplam, satır toplamları, delta) — ama `parsePrice`'ın kendisi yanlış bir sayı üretiyorsa (ör. `"2"` yerine `"1.299,90"` alması gerekirken adedi alması), bu yanlışlık e2e testin gördüğü aritmetik içinde **tutarlı** kalabilir ve hiçbir assertion kırılmayabilir — tam olarak REVIEW.md'deki gerçek hata buydu. Birim test (`utils/price.test.js`), `parsePrice`'ı gerçek metin biçimleriyle (`"1.299,90 TL"`, `"Ürünler Toplamı (2 Ürün)\n1.299,90 TL"`) doğrudan çağırıp beklenen sayıyla karşılaştırarak bu ayrıştırma mantığını **izole** şekilde doğrular — e2e'nin yapısal olarak göremeyeceği bir hata sınıfını hedefler.

# sepet-6 | İleri
## Soru
REVIEW.md: "Ara toplam doğrulaması mutlak değil, göreli yapıldı." Sepet kontrolleri "ara toplam == sabit bir TL tutarı" yerine "adet artışı == birim fiyat kadar artış" gibi göreli (delta) ilişkiler doğruluyor. Canlı bir e-ticaret sitesine karşı koşan bir test paketinde bu tercihin gerekçesi nedir?
## A
Göreli doğrulama, mutlak doğrulamadan matematiksel olarak daha kesin sonuç verir, yuvarlama hatalarını tamamen ortadan kaldırır
🤖 İkisi de aynı `parsePrice`/kayan nokta hassasiyetine tabidir (kod bu yüzden bir "kuruş toleransı" da taşır); fark kesinlik değil, canlı ortamda kırılganlık.
## B
Canlı ortamda fiyat/kampanya değişince sabit tutar beklentisi testi kırar; göreli kontrol yalnız hesaplama mantığını sınar
🤖 Doğru: "sepet toplamı tam olarak X TL olmalı" beklentisi, kampanya değişince testi anlamsız yere kırar; "adet artışı birim fiyat kadar artış getirmeli" ise fiyat ne olursa olsun geçerli bir doğrulamadır.
## C
Mutlak doğrulama Playwright'ın `expect` API'sinde desteklenmiyor, yalnızca göreli karşılaştırma operatörleri mevcut
🤖 Playwright/AssertJ tarzı kütüphaneler mutlak eşitlik kontrolünü de destekler; tercih API kısıtlamasından değil, ortamın doğasından kaynaklanıyor.
## D
Göreli doğrulama daha az kod satırı gerektirdiği için, bakım kolaylığı amacıyla tercih edildi
🤖 Kod miktarı ikisinde de benzer; asıl gerekçe canlı ortamdaki fiyat değişkenliğine karşı testin sağlamlığıdır, satır sayısı değil.
## Doğru
B
## Açıklama
Test, üretim/staging'e özel sabit veriler yerine **gerçek, canlı** e-ticaret sitesine karşı koşuyor — kampanyalar, indirimler ve fiyatlar herhangi bir anda değişebilir. "Sepet toplamı tam olarak 1.299,90 TL olmalı" gibi **mutlak** bir beklenti, testin kendi mantığında hiçbir hata olmasa bile bir fiyat güncellemesiyle kırılırdı — bu, testin gerçek değerini düşüren bir kırılganlıktır (yanlış pozitif). **Göreli (delta)** doğrulama ise ("ara toplam = satır tutarlarının toplamı", "adet +1 → ara toplam + birim fiyat") fiyatın kendisinden bağımsız olarak **hesaplama mantığının tutarlılığını** sınar: gerçek bir hesaplama hatası hâlâ yakalanır, ama bir kampanya güncellemesi testi anlamsız yere kırmaz.
