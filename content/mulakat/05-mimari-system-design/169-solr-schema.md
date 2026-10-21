---
slug: solr-schema
baslik: "Solr Schema: managed vs classic, field/fieldType/copyField, schemaless tuzağı"
kademe: Orta
sira: 169
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Arama motoru, elindeki kayıtları saklayan ve "içinde şu kelime geçenleri bana getir" diyebildiğin bir sistemdir. Solr'da her kayda **belge** (document) denir: bir ürün, bir haber, bir öğrenci kaydı. Belge doldurulmuş bir forma benzer; formdaki her kutu bir **alan** (field) olur — "başlık", "açıklama", "fiyat", "kategori". Kullanıcının yazıp gönderdiği arama isteğine **sorgu** (query) denir.

Peki Solr iki şeyi nereden bilecek? "Fiyat" kutusuna yazılanın sayı mı yazı mı olduğunu, "açıklama" kutusunun kelimelere bölünüp bölünmeyeceğini... Bunları **şema (schema)** söyler. Şema, formun hangi kutusuna ne yazılacağını ve o kutuyla neler yapılabileceğini bildiren kural listesidir. Şema yanlışsa arama da yanlış çalışır; üstelik çoğu zaman hata vermeden, sessizce.

### Temel kavramlar

- **Terim** (term): bir metin aramaya hazırlanırken parçalara ayrılır; bu parçaların her birine terim denir. "Ev Aletleri" metni `ev` ve `aletleri` diye iki terime ayrılır.
- **İndeks** (index): kitabın arkasındaki dizin gibi düşün — hangi terimin hangi belgelerde geçtiğini tutan liste. Arama, bütün metinleri baştan okumaz; bu dizine bakar, bu yüzden hızlıdır.
- **fieldType**: alan tipi tanımıdır. Metnin parçalara ayrılıp ayrılmayacağı — buna **analiz (analysis)** denir — ve küçük harfe çevrilip çevrilmeyeceği burada belirlenir. `string` analiz **yapmaz**; değeri tek parça hâlinde tutar. `text_general` analiz **yapar**; metni kelimelere böler. `pint` ve `pdate` ise sayı ve tarih içindir.
- **field**: adı ve tipi olan somut kutu (`baslik: text_general`).
- **dynamicField**: alan adının **kalıbına** göre tip verme yöntemidir (`*_i` → tam sayı, `*_txt` → metin, `*_dt` → tarih). Böylece `stok_i`, `adet_i` gibi onlarca alanı tek tek yazmadan tiplersin. Kalıp sayıya özel değildir; herhangi bir önek ya da sonek olabilir. Tipi yine **sen** önceden belirlersin. Bu yönüyle tahmine dayalı schemaless kipinden ayrılır ve klasik şemada da kullanılabilir.
- **copyField**: bir alanın içeriğini, indeksleme sırasında ikinci bir alana **kopyalar**. Belgeyi silmez, çoğaltmaz, çekirdekler (core) arasında veri taşımaz.
- **multiValued**: alanın birden çok değer taşıyabilmesi (bir kitabın birden çok yazarı gibi).

Bir alanın yeteneklerini üç **bağımsız** bayrak belirler; hiçbiri diğerinin yerine geçmez:

- **indexed**: alan aranabilir olur.
- **stored**: ham değer geri okunabilir, sonuç listesinde gösterilebilir.
- **docValues**: sütun-yönlü ek yapı; sıralama, **facet** (değerlere göre sayım: "Elektronik 42, Giyim 17") ve toplama bunu kullanır.

Dördüncü bir seçenek daha var: **termVectors**. Bu, alandaki terimleri konum ve sıklık bilgisiyle birlikte belge başına ayrıca saklar. İki işi hızlandırır: **highlighting** (eşleşen kelimeyi sonuçta vurgulama) ve **MoreLikeThis** (benzer belge bulma). Bunu belgeyi yeniden analiz etmeden yapar. Bedeli, indeksin belirgin biçimde büyümesidir; gerekmedikçe açılmaz. Aranabilirliği (indexed) veya sıralanabilirliği (docValues) sağlamaz. Adında "vektör" geçse de **dense vektör / kNN** (anlamsal arama) ile karıştırılmamalıdır: o ayrı bir alan tipidir (`DenseVectorField`) ve tamamen farklı bir mekanizmadır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Solr schema yapı taşları ve copyField">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Schema: fieldType → field → copyField</text>
  <rect x="30" y="46" width="180" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">fieldType</text>
  <text x="120" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="monospace">text_general</text>
  <text x="120" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="monospace">string, pint</text>
  <text x="120" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(analiz + tip)</text>
  <text x="120" y="160" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">field bunları</text>
  <text x="120" y="176" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tip olarak kullanır</text>
  <rect x="250" y="60" width="150" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="325" y="84" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">baslik (text)</text>
  <rect x="250" y="112" width="150" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="325" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">aciklama (text)</text>
  <rect x="450" y="86" width="160" height="46" rx="8" fill="#F5EFE0" stroke="#B8860B" stroke-width="1.5"/>
  <text x="530" y="106" text-anchor="middle" font-size="11" font-weight="700" fill="#8A6608" font-family="monospace">_text_ (catch-all)</text>
  <text x="530" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tek alanda ara</text>
  <line x1="400" y1="80" x2="448" y2="102" stroke="#B8860B" stroke-width="1.5"/>
  <polygon points="448,102 438,100 442,93" fill="#B8860B"/>
  <line x1="400" y1="132" x2="448" y2="116" stroke="#B8860B" stroke-width="1.5"/>
  <polygon points="448,116 438,120 442,127" fill="#B8860B"/>
  <text x="325" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">copyField: baslik + aciklama → _text_</text>
  <text x="320" y="206" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Schemaless tuzağı: ilk belgenin tipi tüm alanı kilitler ("42" vs 42)</text>
</svg>
```

### Nasıl çalışır?

copyField'ın iki klasik amacı vardır. Birincisi **catch-all**: `baslik`, `aciklama`, `etiket` alanlarının hepsi tek bir `_text_` alanına kopyalanır, böylece kullanıcı tek sorguyla hepsinde birden arayabilir. İkincisi **çift analiz**: aynı içerik bir kez analizli `text` alanına (arama için), bir kez analizsiz `string` alanına (docValues ile facet ve sıralama için) yazılır.

İkincisi neden gerekli? Çünkü arama ile facet birbiriyle çelişen analiz ister. Serbest metin araması alanın parçalara ayrılmasını gerektirir. Ama analizli bir `text` alanını facet edersen Solr terimleri sayar. "Ev Aletleri" değeri `ev` ve `aletleri` diye iki ayrı facet satırına bölünür. Kullanıcı listede hiç var olmayan kategoriler görür ve sayımlar anlamını yitirir. `string` alanda ise değer bölünmez; docValues ile tam değer tek satır olarak sayılır. Yani tek alan iki işi birden yapamaz. Alanı `multiValued` yapmak da bunu çözmez; çünkü sorun çok değerlilik değil, analizdir. Çözüm, copyField ile iki ayrı alan tutmaktır.

Bir de **schemaless tuzağı** var. Yönetilen (managed) şemada Solr, tanımadığı bir alanın tipini **ilk gelen belgeden tahmin eder ve o tipi kilitler**. İlk belgede `fiyat: "42"` gelirse — tırnaklı, yani metin — alan `string` olur. Sonra `fiyat: 42` (sayı) gelen belge ya `string`'e zorlanır ya reddedilir. Her hâlükârda sayısal sıralama ve aralık sorgusu (`fiyat:[10 TO 50]`) bozulur. Sebebi şudur: `string` alanda değerler sözlük sırasına göre karşılaştırılır ve "100" değeri "9" değerinden **önce** gelir. Solr her belgede tipi yeniden tahmin etmez, iki tipi birleştirip alanı hem sayı hem metin yapmaz.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Schemaless tuzağı: ilk belge alan tipini kilitler">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Schemaless tuzağı: ilk belge tipi kilitler</text>
  <rect x="26" y="48" width="200" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="126" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1. belge</text>
  <text x="126" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">fiyat: "42"</text>
  <text x="126" y="109" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(tırnaklı = metin)</text>
  <line x1="226" y1="83" x2="270" y2="83" stroke="#1C3247" stroke-width="1.5"/>
  <polygon points="270,83 261,79 261,87" fill="#1C3247"/>
  <rect x="272" y="48" width="150" height="70" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="347" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">tip KİLİTLENDİ</text>
  <text x="347" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">fiyat = string</text>
  <text x="347" y="111" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">artık değişmez</text>
  <rect x="440" y="48" width="174" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="527" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Çözüm</text>
  <text x="527" y="93" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üretimde şemayı</text>
  <text x="527" y="109" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">açıkça tanımla</text>
  <rect x="26" y="130" width="200" height="52" rx="10" fill="#ffffff" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="126" y="152" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">2. belge</text>
  <text x="126" y="172" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">fiyat: 42  (sayı)</text>
  <line x1="226" y1="156" x2="270" y2="156" stroke="#8A2E2E" stroke-width="1.5"/>
  <polygon points="270,156 261,152 261,160" fill="#8A2E2E"/>
  <rect x="272" y="130" width="342" height="52" rx="10" fill="#ffffff" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="443" y="151" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">reddedilir / string'e zorlanır</text>
  <text x="443" y="171" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sayısal sıralama &amp; aralık sorgusu bozulur ("100" &lt; "9")</text>
</svg>
```

Şemayı iki biçimde yönetebilirsin ve Solr 9'da **ikisi de mevcuttur**. **Managed schema** alanları API üzerinden yönetir. Schemaless kipi açıkken bilinmeyen alanları gelen veriden tahmin eder; bu hızlı denemede kolaylık, üretimde ise sürpriz demektir. **Classic schema.xml** ise alanları elle ve açıkça tanımlatır. Dosya git'e girer, sürümlenir ve her değişiklik kod incelemesinden geçer. Üretimde classic'in tercih edilme nedeni hız veya alan sayısı sınırı değil; **öngörülebilirlik ve kontroldür**.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Managed schemaless ile classic schema.xml karşılaştırması">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Managed (schemaless) vs Classic schema.xml</text>
  <rect x="26" y="44" width="288" height="130" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="170" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Managed / schemaless</text>
  <text x="46" y="92" font-size="11" fill="#1C3247" font-family="system-ui">• tipi gelen veriden tahmin eder</text>
  <text x="46" y="113" font-size="11" fill="#1C3247" font-family="system-ui">• hızlı prototip, kolay başlangıç</text>
  <text x="46" y="134" font-size="11" fill="#8A2E2E" font-family="system-ui">• öngörülemez: ilk-belge tuzağı</text>
  <text x="46" y="155" font-size="10.5" fill="#5D7789" font-family="system-ui">→ keşif / deneme için</text>
  <rect x="326" y="44" width="288" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Classic schema.xml</text>
  <text x="346" y="92" font-size="11" fill="#1C3247" font-family="system-ui">• alanlar elle, açıkça tanımlı</text>
  <text x="346" y="113" font-size="11" fill="#1C3247" font-family="system-ui">• git'e girer, versiyonlanır</text>
  <text x="346" y="134" font-size="11" fill="#1C3247" font-family="system-ui">• öngörülebilir, review'dan geçer</text>
  <text x="346" y="155" font-size="10.5" fill="#5D7789" font-family="system-ui">→ üretim için</text>
</svg>
```

### Mülakatta ne sorulur?

- "copyField ne işe yarar?" — beklenen cevap iki amaçtır: catch-all ve çift analiz. Tuzak, copyField'ı yedekleme ya da çekirdekler arası kopyalama sanmaktır.
- "dynamicField ile schemaless farkı nedir?" — dynamicField'da tipi kalıpla **sen** belirlersin (öngörülebilir); schemaless gelen veriden **tahmin eder** (öngörülemez).
- "indexed, stored, docValues arasındaki fark nedir?" — Üçü birbirinden bağımsız yeteneklerdir. Yalnız gösterilecek bir alan `stored` olup `indexed` olmayabilir. Yalnız facet edilecek bir alan `docValues` alıp `stored` olmayabilir.
- "termVectors ne zaman açılır?" — highlighting/MoreLikeThis ihtiyacı varsa; maliyeti indeks boyutudur. Tuzak, bunu kNN/anlamsal aramayla karıştırmaktır.
- "Şu alan hem aranacak hem facet edilecek, ne yaparsın?" — analizli `text` + `string` ile docValues, arada copyField. "Tek alan yeter" cevabı elenmenin klasik yoludur.
- "Üretimde schemaless kullanır mısın?" — hayır: ilk-belge tip kilidi ve sözlüksel sıralama bozulması. Keşifte evet, üretimde açık şema.

### Tek cümlede

Şema, her alanın nasıl analiz edileceğini ve hangi yetenekleri (indexed, stored, docValues) taşıyacağını önceden ve açıkça bildiren kural listesidir. Bu işi tahmine bırakırsan tip kilitlenir, facet bölünür ve sıralama bozulur.

# schema-1 | Orta
## Soru
`copyField`'ın en yaygın kullanım amacı nedir?
## A
Bir alandaki eski değerleri indeksten silip yerine hedef alanın yeni değerlerini yazar
🤖 copyField hiçbir değeri silmez; kaynak alan olduğu gibi kalır, hedefe yalnız kopya çıkar.
## B
İki Solr çekirdeği (core) arasında belgeleri eşitleyip yedek bir kopya oluşturur
🤖 Kopyalama aynı belgenin alanları arasındadır; çekirdekler arası veri taşıma işi değildir.
## C
Aynı belgeyi diske ikinci kez yazarak çökme anında geri dönülecek bir kopya tutar
🤖 Yedekleme replikasyon ve backup işidir; copyField'ın amacı farklı arama davranışı sağlamaktır.
## D
Bir alanın içeriğini catch-all alanında toplar ya da ikinci kez farklı analizle indeksler
🤖 Doğru: catch-all ile tek sorguda hepsinde arama, çift analiz ile arama + facet ayrımı.
## Doğru
D
## Açıklama
**copyField**, indeksleme sırasında bir kaynak (source) alanın içeriğini bir hedef (dest) alana **kopyalar**. İki tipik amacı vardır: (1) **catch-all** — `baslik`, `aciklama`, `etiket` gibi alanları tek bir `_text_` alanında toplayıp kullanıcının tek sorguyla hepsinde arayabilmesini sağlamak; (2) **çift analiz** — aynı içeriği bir kez arama için (analizli `text`), bir kez faceting/sıralama için (analizsiz `string` + docValues) indekslemek. copyField kaynak veriyi silmez (A), çekirdekler arası kopyalama yapmaz (B), yedekleme aracı değildir (C) — yedekleme replikasyon ve backup'ın işidir. Şema tasarımında catch-all + copyField, DisMax/eDisMax sorgularının temel taşıdır.

# schema-2 | Orta
## Soru
Şemadaki `dynamicField` (ör. `*_i`) ne işe yarar?
## A
Adı belirli bir kalıba uyan alanları, her birini elle tanımlamadan tek kuralla tipler
🤖 Doğru: kalıp temelli tipleme, şemayı elle şişirmeden esneklik verir.
## B
Belge indekslenirken alanın değerini çalışma-zamanında hesaplayıp yeniden yazar
🤖 Alanın değerine hiç dokunmaz; yalnız alan tanımını adına göre eşler.
## C
Şemayı schemaless kipe geçirip bilinmeyen alanların tipini gelen veriden tahmin ettirir
🤖 Tahmin yapmaz; tipi kalıpla sen belirlersin ve klasik şemada da kullanılabilir.
## D
Yalnız sayısal alan kalıplarında çalışır; metin ve tarih sonekleri için desteklenmez
🤖 Kalıp herhangi bir önek/sonek olabilir; `*_txt` ve `*_dt` de tümüyle geçerlidir.
## Doğru
A
## Açıklama
**dynamicField** bir **ad kalıbı** ile tip eşler: `*_i` → int, `*_txt` → text, `*_dt` → date gibi. Böylece `stok_i`, `adet_i` gibi onlarca alanı tek tek tanımlamadan, sonekine göre otomatik doğru tiplersin — şemayı elle şişirmeden esneklik sağlar. Bu **değer** değiştirmez (B), yalnızca alan **tanımını** kalıpla eşler. Schemaless'tan farkıdır (C): dynamicField'da tipi sen kalıpla önceden belirlersin (öngörülebilir); schemaless ise gelen veriden tip **tahmin eder** (öngörülemez). Kalıp herhangi bir sonek/önek olabilir, yalnız sayı değil (D). Klasik ve managed şemanın ikisinde de kullanılır.

# schema-3 | İleri
## Soru
`termVectors=true` bir alanda ne sağlar ve neden dikkatli açılmalıdır?
## A
Alanı aranabilir kılar; indexed bayrağının yerine geçtiği için ikisi birlikte açılmaz
🤖 Aranabilirliği indexed sağlar; termVectors onun yerine geçmez, ek bir yapıdır.
## B
Alanı sıralanabilir kılar; docValues yerine geçip sütun-yönlü erişimi ucuza sağlar
🤖 Sıralama ve facet docValues işidir; termVectors sütun-yönlü erişim sunmaz.
## C
Terim, konum ve sıklığı belge başına saklar; highlighting'i hızlandırır, indeksi büyütür
🤖 Doğru: highlighting ve MoreLikeThis hızlanır, bedeli indeks boyutunun büyümesidir.
## D
Dense vektör (kNN) aramasını açar; gömme vektörlerini alanla birlikte saklayıp eşleştirir
🤖 kNN ayrı bir `DenseVectorField` işidir; termVectors klasik sözcük-tabanlı bir yapıdır.
## Doğru
C
## Açıklama
**termVectors**, bir alan için terim listesini ve her terimin **konum/sıklık/offset** bilgisini **belge başına** ayrıca saklar. Bu, **highlighting** (eşleşen kısmı vurgulama) ve **MoreLikeThis** (benzer belge bulma) gibi işlemleri belgeyi yeniden analiz etmeden hızlandırır. Bedeli: indeks boyutu belirgin büyür, bu yüzden **gerçekten gerekmedikçe** açılmaz (modern Solr birçok highlighter'ı termVectors olmadan da çalıştırabilir). termVectors aranabilirlik (indexed) veya sıralama (docValues) sağlamaz (A/B). Ayrıca **dense vektör/kNN** (semantik arama) ile karıştırılmamalıdır (D): o, ayrı bir `DenseVectorField` ve farklı bir mekanizmadır; termVectors klasik sözcük-tabanlı terim vektörüdür.

# schema-4 | Orta
## Soru
Managed (schemaless) modda ilk belgede `fiyat: "42"` metin olarak, sonraki belgede `fiyat: 42` sayı olarak geldi. En olası sonuç nedir?
## A
Solr her belgede tipi yeniden tahmin eder; alan sessizce sayısala yükseltilir, sorun olmaz
🤖 Tip ilk belgede kilitlenir; sonraki belgeler için yeniden tahmin yapılmaz.
## B
İlk belge tipi `string`'e kilitler; sayısal sıralama ve aralık sorguları bozulur
🤖 Doğru: schemaless'ın klasik tuzağı, alanın tipini ilk gelen belge belirler.
## C
İki tip otomatik birleşir; alan aynı anda hem metin hem sayı olarak indekslenip aranır
🤖 Bir alanın tek tipi olur; metin ile sayı tek alanda birleştirilemez.
## D
Solr her iki belgeyi de reddeder; tip çakışması yüzünden koleksiyonda hiç belge oluşmaz
🤖 İlk belge sorunsuz indekslenir; sıkıntı yalnız sonraki uyumsuz belgelerdedir.
## Doğru
B
## Açıklama
Schemaless modda Solr, bir alanın tipini **ilk gelen belgeden** tahmin eder ve **kilitler**. `fiyat: "42"` (tırnaklı, metin) gelirse alan `string` olur. Sonra `fiyat: 42` (sayı) gelince ya değer `string`'e zorlanır ya belge tip uyuşmazlığından reddedilir; her hâlde **sayısal sıralama ve aralık sorguları** (`fiyat:[10 TO 50]`) bozulur çünkü `string` alanda "100" < "9" gibi sözlüksel sıralanır. Solr her belgede tipi yeniden tahmin etmez (A); tipler birleşmez (C); ilk belge indekslendiği için "hiçbiri indekslenmez" yanlıştır (D). Ders: üretimde şemayı **açıkça** tanımla; schemaless yalnız keşif içindir.

# schema-5 | İleri
## Soru
`kategori` alanı analizli `text_general` tipindedir. `facet.field=kategori` çalıştırıldığında "Ev Aletleri" değerine ne olur ve doğru çözüm nedir?
## A
Değer bütün sayılır; analizli alan faceting için de doğrudur, ek bir alana gerek yoktur
🤖 Analizli alan facet'te terimler üzerinden sayılır; bütün değeri geri veremez.
## B
Solr hata verip facet'i tümüyle reddeder; alanı multiValued yapmak sayımı düzeltir
🤖 Solr hata vermez, sessizce terimleri sayar; multiValued analiz farkını çözmez.
## C
Değer bütün kalır ama her belge iki kez sayılır; docValues'ı kapatmak sayımı düzeltir
🤖 Çift sayım değil bölünme olur; docValues'ı kapatmak faceting'i büsbütün pahalılaştırır.
## D
`ev` ve `aletleri` ayrı facet olur; çözüm `string` + docValues ile tam değeri saymaktır
🤖 Doğru: analizsiz `string` alan, tam değeri tek bir facet satırı olarak sayar.
## Doğru
D
## Açıklama
Faceting, alandaki **indekslenmiş değerleri** sayar. Analizli bir `text_general` alanda indekse giren şey bütün değer değil **terimlerdir**: "Ev Aletleri" analiz zincirinden `ev` ve `aletleri` diye iki terim olarak çıkar, dolayısıyla facet sonucunda hiç var olmayan iki ayrı satır belirir ve sayımlar anlamsızlaşır (A yanlış). Solr bunu hata olarak bildirmez, sessizce yapar; `multiValued` ise çok-değerlilik içindir, analiz farkını çözmez (B yanlış). Sorun çift sayım da değildir, bölünmedir; `docValues`'ı kapatmak facet'i düzeltmez, yalnızca daha pahalı bir yola (alan önbelleği) düşürür (C yanlış). Doğru çözüm, değeri **analizsiz** tutan bir `string` alanı `docValues` ile facet/sıralamada kullanmaktır (D). Arama hâlâ analizli alandan yapılacağı için iki alan birden gerekir; bu yüzden aynı içerik `copyField` ile `kategori` (text, indexed) ve `kategori_str` (string, docValues) alanlarına yazılır. Kural: arama analiz ister, facet ise bölünmemiş tam değer — tek alan ikisini birden karşılayamaz.

# schema-6 | Orta
## Soru
Classic `schema.xml`'i managed (schemaless) şemaya kıyasla üretimde tercih etmenin temel nedeni nedir?
## A
Alan tanımları açık ve versiyon kontrolüne girer; tip tahmininin sürprizleri yaşanmaz
🤖 Doğru: üretimde öngörülebilirlik ve versiyonlanabilirlik belirleyicidir.
## B
Classic şema her koşulda daha hızlı indeksler ve sorgu gecikmesini belirgin düşürür
🤖 Hız temel fark değildir; asıl kazanç öngörülebilirlik ve kontroldür.
## C
Managed şema Solr 9'da kaldırıldı; artık tek desteklenen biçim classic schema.xml'dir
🤖 Managed şema kaldırılmadı; Solr 9'da ikisi de mevcuttur, seçim amaca bağlıdır.
## D
Classic şema alan sayısı sınırını kaldırır; managed şemada alan sayısı sabit tutulur
🤖 Böyle bir alan sayısı sınırı yoktur; her iki biçimde de yeni alan eklenebilir.
## Doğru
A
## Açıklama
Üretimde **classic schema.xml** (ya da managed şemayı elle, açık tanımlarla kullanmak) tercih edilir çünkü alan tipleri **açıkça** bildirilir: tip tahmininin sürprizleri, yani schemaless'ın ilk-belge tuzağı yaşanmaz; şema dosyası **git'e girer** ve versiyonlanır; ekip değişiklikleri kod incelemesinden (review) geçer. Bu **öngörülebilirlik** ve **kontrol**, üretim güvenilirliğinin temelidir. Hız (B) esas ayrım değildir; indeksleme ve sorgu başarımını belirleyen şey şemanın yönetim biçimi değil, alan tipleri ile analiz zinciridir. Managed şema Solr 9'da kaldırılmamıştır (C) — her iki biçim de mevcuttur ve schemaless keşif/prototip aşamasında hâlâ kullanışlıdır, sadece üretim için risklidir. Alan sayısına dair böyle bir sınır farkı da yoktur (D). İlke: keşifte esneklik, üretimde açık şema.
