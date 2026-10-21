---
slug: solr-faceting-highlighting
baslik: "Solr Faceting & Highlighting: JSON Facet, range/pivot, collapsing, suggester"
kademe: Orta
sira: 173
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş sitesinde arama kutusuna "ayakkabı" yazdığınızı düşünün. Sayfanın solunda bir filtre çubuğu belirir: "Marka: Nike (42), Adidas (31)", "Fiyat: 0-100 TL (12)", "Renk: Siyah (25)". O çubuğu üreten şeyin adı **faceting** (kırılım sayımı). Diğer davranışların da adları var. Sonuç kartlarında aradığınız kelimenin işaretli çıkması **highlighting** (vurgulama). Aynı ayakkabının 38, 39, 40 numaralarının tek kart hâlinde görünmesi **collapsing** (katlama). "Kredii" yazınca "kredi mi demek istediniz?" uyarısının çıkması **spellcheck** (yazım düzeltme). Siz yazarken açılan öneri listesi ise **suggester** (tamamlayıcı). Solr bu vitrin davranışlarının hepsini arama sonucuyla aynı istekte döndürür. Uygulamanın ayrıca veritabanına gidip saydırmasına gerek kalmaz.

### Temel kavramlar

Önce sözlük.

**Belge (document)**: aramanın tek bir kaydı — burada bir ürün. Kütüphanedeki tek bir fiş gibi. **Alan (field)**: belgenin adlandırılmış parçası. Ürünün `marka`, `fiyat`, `renk` ve `aciklama` alanları vardır; bunlar fişteki satırlardır. **Terim (term)**: metin alanının parçalanmış hâli. "Kırmızı Spor Ayakkabı" başlığı "kırmızı", "spor" ve "ayakkabı" terimlerine ayrılır. **İndeks (index)**: kitabın arkasındaki dizin gibi, hangi terimin hangi belgelerde geçtiğini tutan defter. **Sorgu (query)**: kullanıcının isteği. **Alaka (relevance)**: bir sonucun sorguya ne kadar uyduğunu söyleyen puan; sıralamayı o belirler.

Facet dört türlüdür ve mülakatta ayrımları sorulur. **Field facet (alan kırılımı)** bir alanın ayrık değerlerini sayar; kenar çubuğunun kendisi budur. **Range facet (aralık kırılımı)** sayısal ya da tarih alanını `start`, `end` ve `gap` (adım) parametreleriyle otomatik kovalara böler. **Query facet (sorgu kırılımı)** ise elle yazdığınız her sorgu için bir sayaç döndürür; kovaları siz yazarsınız, Solr sizin yerinize bölmez. **Pivot facet (çapraz kırılım)** iki alanı çaprazlar — "Apple içinde Siyah 12, Beyaz 8" gibi. Ama yalnızca *sayar*; sonuç listesine dokunmaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Facet türleri: field, range, pivot">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Faceting: değerleri grupla + say (docValues ister)</text>
  <rect x="24" y="46" width="185" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Field facet</text>
  <text x="116" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Marka: Apple (42)</text>
  <text x="116" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Samsung (31)</text>
  <text x="116" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Xiaomi (18)</text>
  <text x="116" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrık değer sayımı</text>
  <rect x="227" y="46" width="185" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="319" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Range facet</text>
  <text x="319" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Fiyat 0-100 (12)</text>
  <text x="319" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">100-200 (25)</text>
  <text x="319" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">200-300 (9)</text>
  <text x="319" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">otomatik aralık bölme</text>
  <rect x="430" y="46" width="186" height="140" rx="10" fill="#F5EFE0" stroke="#B8860B" stroke-width="1.5"/>
  <text x="523" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#8A6608" font-family="system-ui">Pivot facet</text>
  <text x="523" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Apple →</text>
  <text x="523" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">  Siyah (12)</text>
  <text x="523" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">  Beyaz (8)</text>
  <text x="523" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iç içe çapraz gruplama</text>
</svg>
```

### Nasıl çalışır?

Faceting, "şu alanın tüm değerlerini tara ve say" işidir. Bu iş satır satır değil, **sütun yönlü (columnar)** erişim ister. Şemada alana `docValues=true` verilirse Solr o alanın değerlerini ardışık ve sıkıştırılmış bir sütun olarak diske yazar; sayım da bu sütun üzerinde yürür. docValues yoksa Solr aynı sütunu çalışma anında bellekte kurmak zorunda kalır; buna **fieldcache** denir. O zaman ilk sorgu yavaşlar, bellek (heap) baskısı artar ve büyük indekste bellek taşması (OOM) riski doğar. Bu yüzden kural nettir: facet, sıralama ve toplama yapılacak her alanda docValues açılır. `stored=true` yalnızca ham değeri geri okumak, `termVectors=true` ise vurgulama/benzer-belge için tutulur; ikisi de facet hızını değiştirmez.

Highlighting ayrı bir yoldan gider: eşleşen terimin belge metnindeki yerini bulur, çevresinden kısa bir parça (snippet) keser ve terimi `<em>...</em>` etiketiyle sarar. Böylece kullanıcı sonucun neden geldiğini görür.

```svg
<svg viewBox="0 0 640 170" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Highlighting: eşleşen kelime snippet içinde vurgulanır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Highlighting: eşleşeni vurgula + bağlam ver</text>
  <rect x="40" y="44" width="150" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="115" y="62" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sorgu</text>
  <text x="115" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="monospace">kredi</text>
  <line x1="190" y1="64" x2="238" y2="64" stroke="#1C3247" stroke-width="1.5"/>
  <polygon points="238,64 229,60 229,68" fill="#1C3247"/>
  <rect x="240" y="40" width="374" height="90" rx="10" fill="#ffffff" stroke="#25804C" stroke-width="1.4"/>
  <text x="427" y="62" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonuç kartı — snippet</text>
  <text x="258" y="90" font-size="11.5" fill="#1C3247" font-family="system-ui">"…düşük faizli konut</text>
  <rect x="415" y="78" width="52" height="18" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="441" y="91" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">kredi</text>
  <text x="470" y="90" font-size="11.5" fill="#1C3247" font-family="system-ui">si için…"</text>
  <text x="427" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="monospace">&lt;em&gt;kredi&lt;/em&gt; ile işaretlenir → neden eşleşti görünür</text>
</svg>
```

Yinelenen varyantlar için iki seçenek vardır. Klasik `group` parametresi grupları ayrı listeler hâlinde döndürür. Daha ucuz olanı **CollapsingQParser**'dır. `fq={!collapse field=urun_id}` yazdığında aynı `urun_id` değerine sahip belgeler tek bir temsilciye katlanır; temsilci en yüksek skorlu ya da en ucuz olan olabilir. `expand` bileşeni istenirse katlanan diğerlerini ek bir blokta verir. Büyük indekste katlama, `group`'a göre belirgin biçimde daha az bellek ve CPU harcar. Dikkat: pivot facet de "gruplar" ama yalnız sayaç üretir — sonuç listesindeki yinelenen kartları teke indirmez.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Collapsing: aynı ürünün varyantları tek temsilciye katlanır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Collapsing: varyantları teke indir</text>
  <text x="110" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">önce (yinelenen)</text>
  <rect x="40" y="60" width="140" height="26" rx="5" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="110" y="78" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">ayakkabı — 40</text>
  <rect x="40" y="90" width="140" height="26" rx="5" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="110" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">ayakkabı — 41</text>
  <rect x="40" y="120" width="140" height="26" rx="5" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="110" y="138" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">ayakkabı — 42</text>
  <line x1="200" y1="103" x2="270" y2="103" stroke="#25804C" stroke-width="1.8"/>
  <polygon points="270,103 260,98 260,108" fill="#25804C"/>
  <text x="235" y="94" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="monospace">collapse</text>
  <text x="235" y="122" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="monospace">field=urun_id</text>
  <text x="400" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sonra (tek temsilci)</text>
  <rect x="320" y="90" width="160" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="400" y="110" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="monospace">ayakkabı (1 kart)</text>
  <text x="560" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">expand ile</text>
  <text x="560" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">diğerleri</text>
  <text x="320" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">CollapsingQParser büyük indekste group'tan daha performanslı</text>
</svg>
```

Arama kutusunun iki yardımcısı da farklı yapılardır. Spellcheck, yazılan terimi indeksteki terimlere düzenleme uzaklığıyla (edit distance) ya da ayrı bir sözlükle karşılaştırıp düzeltme önerir. Suggester ise her tuş vuruşunda hızlı yanıt vermek için öneriye özel bir yapı kullanır. İki seçenek vardır: **edge-n-gram** (kelimenin önek parçaları) ya da **FST** (finite state transducer — aynı öneki paylaşan kelimeleri sıkışık bir otomat olarak tutar).

Son olarak facet'i yazma biçimine bakalım. Eski, düz parametre listesi (`facet.field`, `facet.range`, `facet.pivot`) yerine modern Solr'da **JSON Facet API** kullanılır. Üstünlüğü hız ya da önbellek değildir; **ifade gücüdür**. Bir facet kovasının içine alt facet yerleştirebilir ve metrikleri (avg, sum, unique, percentile, min/max) aynı yapıda hesaplayabilirsin. Örneğin "her marka için ortalama fiyat ve renk dağılımı" tek bir JSON ile kurulur. Tek düğümlü kurulumda da çalışır; SolrCloud şartı yoktur.

### Mülakatta ne sorulur?

En sık gelen kalıplar şunlardır.

"Kenar çubuğundaki filtreleri nasıl üretirsin?" — Beklenen cevap: field facet; tıklanınca da `fq` ile daraltma.

"Fiyat aralıklarını kim üretir?" — Buradaki tuzak query facet'tir. Query facet gerçek bir türdür, ama kovaları sana elle yazdırır. Otomatik bölen tür range facet'tir.

"Aynı ürün üç kez çıkıyor." — Pivot facet demek, sayım ile katlamayı karıştırdığını ele verir. Doğru cevap collapse ya da group'tur.

"Facet neden yavaş?" — docValues kapalıysa fieldcache kurulur ve bellek baskısı artar; bu söylenmelidir.

"JSON Facet neden?" — "Daha hızlı" demek zayıf bir cevaptır. İç içe facet ve metrik hesabı denmelidir.

Sık tuzaklar: highlighting ile faceting'i karıştırmak, suggester'ı spellcheck sanmak ve `group` ile `collapse` arasındaki maliyet farkını bilmemek.

### Tek cümlede

Faceting, alan değerlerini sayıp filtre çubuğunu üretir; bunu docValues sayesinde hızlı yapar. Highlighting eşleşen kelimeyi gösterir. Collapsing yinelenen kartları teke indirir. Spellcheck ve suggester ise arama kutusunu akıllandırır.
# facet-1 | Orta
## Soru
E-ticaret sitesinde "Marka: Apple (42), Samsung (31)" gibi kenar çubuğu filtreleri hangi Solr özelliğiyle üretilir?
## A
Highlighting: eşleşen kelimeyi işaretleyip kısa bir bağlam parçası döndürür
🤖 Sonuç metnini süsler; alan değerlerinin belge sayısını üretmez.
## B
Spellcheck: yanlış yazılan sorgu terimi için düzeltme önerisi üretir
🤖 Sorgu yazımını onarır; filtre sayaçlarını hesaplamaz.
## C
Field facet: bir alanın ayrık değerlerini kaç belgede geçtikleriyle sayar
🤖 Doğru: kenar çubuğundaki değer-sayı listesi tam olarak bunun çıktısıdır.
## D
Grouping: aynı gruptan gelen belgeleri teke indirip tek temsilci gösterir
🤖 Yinelenen sonuçları katlar; değer-sayı dağılımı vermez.
## Doğru
C
## Açıklama
Kenar çubuğundaki "Marka (Apple 42, Samsung 31)" tarzı filtreler **field faceting** ile üretilir (C): Solr `marka` alanının **ayrık değerlerini** gruplar ve her değerin o anki sonuç kümesinde **kaç belgede** geçtiğini sayar. Kullanıcı bir değere tıklayınca uygulama genelde bir `fq` (filtre sorgusu) ekler ve sonuç daralır — facet ile `fq` birlikte çalışır. Highlighting (A) eşleşen metni işaretler, spellcheck (B) yazımı düzeltir, grouping (D) yinelenen belgeleri teke indirir; üçü de **değer-sayı dağılımı** üretmez. Performansın temeli alanda `docValues=true` olmasıdır: sütun-yönlü saklama sayım taramasını ucuzlatır.

# facet-2 | Orta
## Soru
Fiyat filtresini otomatik olarak "0-100, 100-200, 200-300" gibi kovalara bölüp her kovanın sayısını vermek için hangi facet türü uygundur?
## A
Field facet: alandaki her ayrık değeri ayrı bir kova sayıp listeler
🤖 Her benzersiz fiyat ayrı satır olurdu; aralık kovası oluşmaz.
## B
Pivot facet: iki alanı çaprazlayıp iç içe kırılım sayaçları üretir
🤖 Çapraz kırılım verir ama sayısal ekseni aralıklara bölmez.
## C
Query facet: elle yazılan her sorgu ifadesi için ayrı bir sayaç verir
🤖 Aralıkları tek tek siz yazarsınız; otomatik bölme yapmaz.
## D
Range facet: sayısal alanı start-end-gap ile otomatik kovalara böler
🤖 Doğru: sınırları ve adımı verirsiniz, kovaları Solr üretir.
## Doğru
D
## Açıklama
Fiyat gibi **sürekli/sayısal** bir alanı sabit aralıklara bölmek **range facet**'in işidir (D): `start`, `end` ve `gap` (adım) verilir, Solr `[0 TO 100)`, `[100 TO 200)`… kovalarını kendisi üretip her birinin belge sayısını döndürür; tarih alanlarında da çalışır ("son 7 gün"). **Field facet** (A) her **benzersiz** fiyatı ayrı sayardı — yüksek kardinalitede kullanışsız bir liste çıkar. **Pivot facet** (B) marka×renk gibi çapraz kırılım içindir, sayısal ekseni bölmez. **Query facet** (C) gerçek bir türdür ve `fiyat:[0 TO 100]` gibi **elle yazdığınız** her sorgu için sayaç verir; aralıkları siz yazdığınız için "otomatik bölme" yoktur — kova sayısı arttıkça sorgu da şişer. Aynı iş JSON Facet API'de `type: range` ile daha esnek ifade edilir.

# facet-3 | Orta
## Soru
Aynı ürünün farklı bedenleri arama sonucunda tekrar tekrar çıkıyor; her üründen yalnız bir temsilci göstermek istiyoruz. Hangi özellik kullanılır?
## A
Collapsing: aynı grup değerli belgeleri teke indirip tek temsilci döndürür
🤖 Doğru: collapse alanı aynı olan belgeler tek karta iner.
## B
Range facet: sayısal alanı aralıklara bölüp her aralığın sayısını verir
🤖 Aralık sayaçları üretir; sonuç listesindeki yinelenenlere dokunmaz.
## C
Highlighting: eşleşen terimi işaretleyip kısa bağlam parçası döndürür
🤖 Metni işaretler; kaç kart döndüğünü hiç değiştirmez.
## D
Pivot facet: alanları çaprazlayıp her grup için ayrı belge sayacı üretir
🤖 Grupları sayar ama sonuç listesini katlamaz; kartlar yine yinelenir.
## Doğru
A
## Açıklama
Aynı mantıksal ürünün varyantlarını sonuçta **tek temsilciyle** göstermek için grouping ya da daha performanslı **collapsing** kullanılır (A). **CollapsingQParser** (`fq={!collapse field=urun_id}`) aynı `urun_id` değerine sahip belgeleri tek temsilciye — en yüksek skorlu ya da en ucuz olana — **katlar**; `expand` bileşeni istenirse katlananları ayrı bir blokta döndürür. Büyük indekste collapse, klasik `group` parametresinden az bellek ve CPU harcar. Range facet (B) aralık sayar, highlighting (C) metni işaretler; ikisi de listeyi değiştirmez. En cezbedici yanlış **pivot facet**'tir (D): gerçekten gruplama yapar ve "her ürün için kaç varyant var" bilgisini verir, ama çıktısı bir **sayaç tablosudur** — sonuç listesindeki yinelenen kartlar aynen kalır. Sayım ile katlama farklı işlerdir.

# facet-4 | Orta
## Soru
Kullanıcı "kredii" (yanlış) yazdı; "kredi mi demek istediniz?" önerisi ve yazarken otomatik tamamlama hangi bileşenlerle sağlanır?
## A
Faceting ve highlighting: kırılım sayaçları üretir, eşleşen terimi işaretler
🤖 Sayım ve vurgulama yapar; sorgu metnini onarmaz, tamamlamaz.
## B
Spellcheck ve Suggester: yazımı düzeltir, yazarken tamamlama önerir
🤖 Doğru: biri "bunu mu demek istediniz", diğeri otomatik tamamlama.
## C
Range facet ve pivot facet: aralık kovası ve çapraz kırılım sayımı verir
🤖 İkisi de facet ailesinden; öneri ya da düzeltme üretmezler.
## D
Collapsing ve grouping: yinelenen belgeleri tek temsilciye katlayıp gösterir
🤖 Sonuç listesini kısaltır; arama kutusuna öneri taşımaz.
## Doğru
B
## Açıklama
İki ayrı bileşen iş birliği yapar (B). **Spellcheck** yanlış yazımı yakalayıp düzeltme önerir ("kredii" → "kredi mi demek istediniz?"); indeksteki terimlere düzenleme uzaklığıyla (edit distance) ya da ayrı bir sözlükle bakar. **Suggester** ise kullanıcı yazarken **otomatik tamamlama** (autocomplete) sunar; her tuş vuruşunda hızlı yanıt için öneriye-optimize bir yapı kullanır — **edge-n-gram** (önek parçaları) ya da **FST** (finite state transducer). Faceting ve highlighting (A) sayım ile vurgulamadır, range/pivot facet (C) yalnız sayaç üretir, collapsing/grouping (D) sonuç listesini katlar; hiçbiri sorgu metnine dokunmaz. İkisi birlikte "yaz–öner–düzelt" deneyimini tamamlar.

# facet-5 | İleri
## Soru
Neden **JSON Facet API** eski parametre-tabanlı faceting'e tercih edilir?
## A
Daha az bellek harcar ama yalnız tek alanlı düz facet üretebilir
🤖 Tam tersi; eski API'den çok daha geniş bir facet dili sunar.
## B
Yalnız SolrCloud'da çalışır; tek düğümlü kurulumda devre dışıdır
🤖 Tek düğümlü Solr'da da çalışır; dağıtıma özel bir yetenek değildir.
## C
İç içe facet ve metrikleri (avg, sum, unique) tek yapıda ifade eder
🤖 Doğru: asıl kazanç iç içe yapı ve metrik ifade gücüdür.
## D
Facet sonuçlarını otomatik önbellekler; eski API önbellek kullanmaz
🤖 Önbellek davranışı benzerdir; üstünlük oradan gelmez.
## Doğru
C
## Açıklama
**JSON Facet API**'nin temel üstünlüğü **ifade gücüdür** (C): facet'leri **iç içe** (bir kovanın içinde alt-facet) ve **metriklerle** (avg, sum, unique, percentile, min/max) tek, okunur bir JSON yapısında tanımlar. "Her marka için ortalama fiyat ve renk dağılımı" gibi bir istek eski düz parametre listesiyle (`facet.field`, `facet.pivot`…) hantal ve sınırlıyken burada doğal ifade edilir; arama sayfası hafif bir analitik araca dönüşür. (A) yanlıştır, yetenek daralmaz genişler; (B) yanlıştır, tek düğümlü kurulumda da çalışır — SolrCloud şartı yoktur; (D) yanlıştır, iki API'nin önbellek davranışı arasında böyle bir uçurum yoktur, kazanç **yapısal esneklikten** gelir. Modern Solr projeleri yeni facet işlerini JSON Facet ile kurar.

# facet-6 | Orta
## Soru
Faceting performansı için hangi alan ayarı kritiktir?
## A
stored=true: alanın ham değerini saklayıp sonuçta geri okunmasını sağlar
🤖 Gösterim için ham değeri tutar; sayım taramasını hızlandırmaz.
## B
termVectors=true: terim konum ve frekanslarını belge başına ayrıca saklar
🤖 Vurgulama ve benzer-belge içindir; facet yolunda kullanılmaz.
## C
multiValued=true: bir alanın aynı belgede birden çok değer taşımasına izin verir
🤖 Facet edilebilirliği etkiler ama hızın belirleyicisi değildir.
## D
docValues=true: alanı sütun-yönlü saklayıp facet ve sıralamayı hızlandırır
🤖 Doğru: sütun-yönlü erişim facet taramasının temelidir.
## Doğru
D
## Açıklama
Faceting "bir alanın tüm değerlerini tara ve say" işidir; bu, satır değil **sütun-yönlü** (columnar) erişim gerektirir. **docValues=true** (D) alanın değerlerini ardışık, sıkıştırılmış sütun düzeninde diske yazar ve faceting/sıralama/toplama işlemlerini çok hızlandırır. docValues **olmadan** Solr aynı sütunu çalışma-zamanında bellekte **fieldcache** olarak kurmak zorunda kalır: yavaş ilk-yükleme, yüksek heap baskısı ve büyük indekste OOM riski. `stored=true` (A) yalnız gösterim için ham değeri saklar, `termVectors=true` (B) highlighting ve MoreLikeThis içindir, `multiValued=true` (C) bir alanın kaç değer taşıyabileceğini söyler — çok değerli alan facet edilebilir ama hızın belirleyicisi bu değildir. Kural: facet/sort/aggregation yapılacak her alanda docValues aç.
