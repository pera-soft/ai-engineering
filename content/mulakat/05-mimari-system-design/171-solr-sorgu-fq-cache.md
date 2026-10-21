---
slug: solr-sorgu-fq-cache
baslik: "Solr Sorgu: q vs fq (filter cache), eDisMax, yüksek-kardinalite tuzağı"
kademe: İleri
sira: 171
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş sitesinde arama kutusuna "kablosuz kulaklık" yazıp Ara'ya bastığını düşün. Sitede milyonlarca ürün var; buna rağmen sonuç yarım saniyede geliyor ve en üstte gerçekten aradığın şey duruyor. Bunu yapan yazılıma **arama motoru** (search engine) denir; Solr da bunlardan biridir.

Solr'a aslında iki ayrı şey söylersin. Birincisi ne aradığındır. İkincisi hangi şartları taşıyan ürünleri görmek istediğindir: "sadece elektronik kategorisi", "sadece stokta olanlar" gibi. Bu ikisi kulağa benzer gelir; ama Solr'da farklı kutulara yazılır: `q` ve `fq`. Hangisinin nereye yazıldığını bilmemenin iki bedeli vardır. Sonuçların sırası bozulur ve sistem zamanla yavaşlar. Bu metin, o ayrımı ve arkasındaki önbellek mantığını sıfırdan anlatır.

### Temel kavramlar

- **Belge (document)**: Solr'ın sakladığı tek bir kayıt — bir ürün, bir haber, bir CV. Kütüphane benzetmesinde bir kitap.
- **Alan (field)**: Belgenin bir parçası — başlık, açıklama, fiyat, kategori. Kitabın adı, yazarı, rafı.
- **Terim (term)**: Metnin parçalanmasıyla çıkan tek tek kelimeler; "kablosuz kulaklık" iki terimdir.
- **Ters indeks (inverted index)**: Kitabın arkasındaki dizin gibidir. Normalde "belge → içindeki kelimeler" bilinir; ters indeks tersini tutar: "kelime → o kelimenin geçtiği belgeler". Aramanın hızlı olmasının tek sebebi budur; hiçbir belge baştan sona okunmaz.
- **Sorgu (query)**: Kullanıcının isteğinin, Solr'ın anlayacağı hale gelmiş biçimi.
- **Alaka (relevance) ve skor (score)**: Her belgeye "bu sorguya ne kadar uyuyor" diye bir sayı verilir; o sayı skordur ve sonuçlar ona göre sıralanır. Kütüphanecinin "en çok işine yarayacak kitap bu" derken kafasında yaptığı sıralama.
- **Önbellek (cache)**: Pahalı bir hesabın sonucunu kenara not edip, aynı hesap yeniden istendiğinde baştan yapmak yerine nottan okumak. Her seferinde 17×24'ü çarpmak yerine bir kez hesaplayıp kâğıdın kenarına yazmak gibi.

### Nasıl çalışır?

`q` **skorlanan** sorgudur. Alaka hesaplanır ve sonuçlar skora göre sıralanır. "Kullanıcı ne aradı" bilgisi buraya gider. `fq` (filter query) ise **skorlanmayan** filtredir. Her belge için yalnız "geçsin mi, geçmesin mi" cevabı üretir ve sıralamaya hiç karışmaz.

Peki "kategori = elektronik" kısıtı neden `fq`'ya gider? İki sebep. Birincisi **doğruluk**: bir ürünün elektronik olması, onun "kablosuz kulaklık" sorgusuna ne kadar uyduğunu değiştirmemelidir; kısıtı `q`'ya koyarsan skoru kirletirsin. İkincisi **hızdır**. Her `fq`'nun sonucu **filterCache** içinde bir **bit kümesi (bitset)** olarak saklanır. Bu kümede her belge için tek bir bit vardır: geçti ya da geçmedi. Aynı filtre bin sorguda tekrar etse bile yalnız bir kez hesaplanır; sonra hep hazır okunur ve `q`'nun sonucuyla kesiştirilir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="q skorlanır, fq skorlanmaz ve filterCache'lenir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">q (skor) vs fq (filtre + cache)</text>
  <rect x="30" y="46" width="270" height="94" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="monospace">q</text>
  <text x="165" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Skorlanır → alaka sıralaması</text>
  <text x="165" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">"kullanıcı ne aradı"</text>
  <text x="165" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önbelleklenmez (skor değişir)</text>
  <rect x="340" y="46" width="270" height="94" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="monospace">fq</text>
  <text x="475" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Skorlanmaz → evet/hayır filtre</text>
  <text x="475" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">"kategori/fiyat/stok kısıtı"</text>
  <text x="475" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">filterCache'lenir (bitset, yeniden kullanılır)</text>
  <rect x="90" y="160" width="460" height="46" rx="8" fill="#F7E9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="320" y="180" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">TUZAK: fq=fiyat:499.99 gibi benzersiz değerler</text>
  <text x="320" y="197" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">her sorgu yeni cache girişi → filterCache kirlenir, isabet oranı düşer</text>
</svg>
```

Solr'da üç ayrı önbellek vardır; mülakatta en çok karıştırılan yer burasıdır. **filterCache** tek bir `fq`'nun belge kümesini tutar: skorsuz, sırasız bir bit kümesi. **queryResultCache** belirli bir `q` + `fq` + sıralama kombinasyonunun **sıralı kimlik listesini** tutar; yani ancak birebir aynı sorgu tekrar ederse isabet eder. **documentCache** ise belge başına saklanan (stored) alan değerlerini tutar ve sonuç ekranında ham veriyi verir.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Solr önbellek türleri: filterCache, queryResultCache, documentCache">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç Solr önbelleği, üç farklı içerik</text>
  <rect x="24" y="42" width="192" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="monospace">filterCache</text>
  <text x="120" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek fq → belge kümesi</text>
  <text x="120" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(bitset, skorsuz)</text>
  <text x="120" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">farklı q'larla</text>
  <text x="120" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeniden kullanılır</text>
  <rect x="224" y="42" width="192" height="130" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="monospace">queryResultCache</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">q+fq+sort →</text>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sıralı id listesi</text>
  <text x="320" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">birebir aynı sorgu</text>
  <text x="320" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tekrarında isabet</text>
  <rect x="424" y="42" width="192" height="130" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="520" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="monospace">documentCache</text>
  <text x="520" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">belge → stored alan</text>
  <text x="520" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">değerleri</text>
  <text x="520" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonuç gösteriminde</text>
  <text x="520" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ham değeri verir</text>
</svg>
```

Buradan iki pratik kural çıkar.

**Birincisi: kısıtları ayrı ayrı `fq` olarak yaz.** `fq=kategori:elektronik&fq=durum:aktif` yazarsan iki **bağımsız** bit kümesi önbelleğe girer. Başka sorgular bunları tek tek yeniden kullanabilir. Aynı şeyi `fq=kategori:elektronik AND durum:aktif` diye tek parçada yazarsan yalnız tek bir bit kümesi oluşur. Bu küme ancak o **tam kombinasyon** tekrar ettiğinde işe yarar; kombinasyon değişince baştan hesaplanır ve isabet oranı düşer.

**İkincisi: yüksek kardinaliteli filtreden kaçın.** Kardinalite, bir alanın kaç farklı değer aldığıdır. Kategori azdır (onlarca değer); fiyat ise neredeyse her kayıtta farklıdır. `fq=fiyat:499.99` gibi bir filtre her sorguda **yeni** bir önbellek girişi üretir. Önbellek dolar. Kategori gibi faydalı girişler dışarı atılır (eviction) ve isabet oranı çöker. Buna **önbellek kirlenmesi (cache pollution)** denir. Çözüm: o filtreyi `{!cache=false}` ile önbelleksiz çalıştırmak ya da aralığa çevirmek (`fq=fiyat:[* TO 500]`).

Son parça `q`'nun nasıl yorumlandığıdır. **eDisMax** bir **sorgu ayrıştırıcısıdır (query parser)**. Son kullanıcının sade girdisini birden çok alanda arar ve her alana ağırlık verir: `qf=baslik^3 aciklama` yazarsan başlıktaki eşleşme üç kat değerli olur. Ayrıca iki araç daha sunar: `pf` (kelimeler yan yana geçince ek puan) ve `bf`/`boost` (bir fonksiyonla puan ekleme). eDisMax indeksleme yapmaz, şema üretmez, SQL'e çevirmez; yalnız sorguyu yorumlayıp skorlar.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="eDisMax: kullanıcı girdisi birden çok alanda alan-başına ağırlıkla aranır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">eDisMax: çok-alanlı, ağırlıklı arama</text>
  <rect x="40" y="70" width="150" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="115" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanıcı girdisi</text>
  <text x="115" y="108" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="monospace">kablosuz kulaklık</text>
  <rect x="410" y="42" width="200" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="510" y="64" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">baslik ^3</text>
  <rect x="410" y="86" width="200" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <text x="510" y="108" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">aciklama ^1</text>
  <rect x="410" y="130" width="200" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <text x="510" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">etiket ^1</text>
  <line x1="190" y1="93" x2="406" y2="59" stroke="#186399" stroke-width="1.6"/>
  <polygon points="406,59 396,58 399,66" fill="#186399"/>
  <line x1="190" y1="93" x2="406" y2="103" stroke="#186399" stroke-width="1.2"/>
  <polygon points="406,103 396,99 397,107" fill="#186399"/>
  <line x1="190" y1="93" x2="406" y2="147" stroke="#186399" stroke-width="1.2"/>
  <polygon points="406,147 397,143 396,151" fill="#186399"/>
  <text x="300" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">qf=baslik^3 aciklama etiket — başlıktaki eşleşme 3 kat değerli</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`q` ile `fq` farkı nedir?"** Tek kelimeyle geçiştirme: hem skorlama (fq skorlamaz) hem önbellekleme (fq filterCache'lenir) tarafını söyle. Yarım cevap ("fq performans içindir") tuzaktır; `fq` aynı zamanda bir **doğruluk** aracıdır, çünkü skoru korur.
- **"Kategori filtresini nereye koyarsın, neden?"** `fq`'ya: alakayı bozmasın ve sürekli tekrar ettiği için önbellekten faydalansın.
- **"filterCache tam olarak neyi saklar?"** "Sorguyu" değil; filtre başına bir bit kümesi saklar. Sorulmadan queryResultCache farkını da ekle: biri tek bir filtre parçasıdır, diğeri tam sonuç sayfasının sıralı kimlik listesidir.
- **"Aramamız zamanla yavaşladı, nereden başlarsın?"** Solr admin panelinden filterCache isabet oranına (hit ratio) ve eviction sayısına bak; yüksek kardinaliteli `fq` ara. Çözüm `{!cache=false}` veya aralık filtresi.
- **"Üç kısıtı tek `fq`'da mı, ayrı ayrı mı verirsin?"** Ayrı — her biri yeniden kullanılabilir bir bitset olur. Bilerek tek `fq` yazacağın istisna, kısıtların daima birlikte geldiği ve kombinasyonun kendisinin tekrar ettiği durumdur.
- **"eDisMax ne işe yarar?"** Teknik olmayan kullanıcının sade girdisinden iyi alaka üretmek: çok alanlı arama, `qf` ile alan ağırlığı, `pf`/`bf` ile ek puan.

### Tek cümlede

`q` ne aradığını söyler ve skorlar. `fq` ise kimin eleneceğini söyler; skorlamaz ve sonucunu filterCache'te yeniden kullanılabilir bir bit kümesi olarak saklar. Bu yüzden kısıtlar ayrı ayrı `fq` ile verilir. Yüksek kardinaliteli olanlar ise `{!cache=false}` ya da aralık filtresiyle önbellek dışında tutulur.

# fq-1 | İleri
## Soru
"Kategori = elektronik" kısıtı `q` yerine `fq` ile verilmelidir. Neden?
## A
`fq` skorlamaya hiç girmez, yalnız evet/hayır eler; sonucu filterCache'te bitset olarak saklanır ve sonraki sorgularda yeniden kullanılır
🤖 Doğru: filtre skoru bozmamalı ve tekrar ettiği için önbelleklenmeli — tam olarak `fq`'nun işi.
## B
Aradaki tek fark sözdizimidir; Solr `fq`'yu iç planda `q`'ya ekleyip ikisini tek bir birleşik sorgu olarak yürütür
🤖 Fark sözdizimi değil; skorlama ve önbellekleme davranışı temelden farklıdır, iki parça ayrı yürütülür.
## C
`fq` de alaka skoruna katkı verir, ama ağırlığı `q`'ya göre düşük tutulduğundan kategori gibi kısıtlar oraya yazılınca sıralamayı az bozar
🤖 Cezbedici ama yanlış: `fq` skora "az" değil, hiç katkı vermez; ağırlık kavramı `fq` için tanımlı değildir.
## D
`fq` kendi içinde bir sıralama uygulayıp sonuçları belge ekleme sırasına döndürür; bu yüzden kısıtlar oraya, sıralama `q`'ya bırakılır
🤖 Sıralamayı `fq` belirlemez; sıra skordan veya `sort` parametresinden gelir.
## Doğru
A
## Açıklama
Kategori gibi bir kısıt bir **filtredir**: belgenin geçip geçmeyeceğine dair evet/hayır kararıdır ve **alaka skorunu etkilememelidir** (bir ürünün "elektronik" olması, sorguya ne kadar uyduğunu değiştirmez). `fq` tam bunu yapar: **skorlamaz**. Ayrıca her `fq` **filterCache**'de bir bitset olarak saklanır; `fq=kategori:elektronik` binlerce sorguda tekrar ettiği için **bir kez** hesaplanıp hep önbellekten okunur — büyük performans kazancı. Aynı kısıtı `q`'ya koymak hem skoru kirletir hem önbellek avantajını kaybettirir. (B) Fark sözdizimsel değil işlevseldir; `fq` `q`'ya eklenmez, ayrı bir belge kümesi olarak hesaplanıp kesişime girer. (C) En cezbedici şık budur ama `fq`'nun skora "düşük ağırlıklı" bir katkısı yoktur; katkısı sıfırdır — skor istiyorsan `bq`/`boost` kullanılır. (D) `fq` sıralamayı hiç belirlemez.

# fq-2 | İleri
## Soru
**filterCache** ne saklar?
## A
Her belgenin stored alanlarındaki ham metni, sonuç ekranında hızlı gösterilebilsin diye belge kimliğiyle eşleyerek tutar
🤖 Ham stored değerleri documentCache'in işidir; filterCache metin değil belge kümesi tutar.
## B
Şemadaki her alan için terim sözlüğünü ve terim başına belge frekanslarını, skor hesabı diske inmesin diye bellekte tutar
🤖 Terim sözlüğü ve frekanslar Lucene'in ters indeks yapılarıdır, bir Solr önbelleği değil; filterCache bunları saklamaz.
## C
Her `fq` için hangi belgelerin filtreyi geçtiğini bir bit kümesi (DocSet/bitset) olarak; aynı `fq` tekrarında yeniden hesaplanmaz
🤖 Doğru: filterCache filtre başına belge kümesini bitset olarak önbellekler.
## D
Her belge için `q`'dan gelen alaka skorunu, aynı sorgu tekrarlandığında sıralama baştan hesaplanmasın diye tutar
🤖 Skorlar önbelleklenmez ve `fq` zaten skorlanmaz; saklanan şey skor değil belge kümesidir.
## Doğru
C
## Açıklama
**filterCache**, her benzersiz `fq` için o filtreyi **hangi belgelerin geçtiğini** bir **bit kümesi** (DocSet/bitset — her belge için 1 bit: geçti/geçmedi) olarak saklar. Bir sonraki sorgu aynı `fq`'yu kullanınca filtre yeniden **hesaplanmaz**; hazır bitset okunur ve `q` sonucuyla kesişim (intersection) alınır — çok hızlı. Bu yüzden tekrar eden, düşük-kardinaliteli filtreler (kategori, durum, stok) filterCache'ten muazzam fayda sağlar. (A) Ham stored alan değerleri **documentCache**'te tutulur. (B) Terim sözlüğü ve belge frekansları ters indeksin kendi yapılarıdır; onlar bir Solr önbelleği değil, indeks dosyalarıdır. (D) Skor kavramı `fq` için hiç yoktur; sıralı sonuç listesini saklayan önbellek ise queryResultCache'tir. Önbellek boyutu ve isabet oranı (hit ratio) Solr admin panelinden izlenir.

# fq-3 | Uzman
## Soru
Bir geliştirici `fq=fiyat:499.99` gibi tam-sayısal fiyatları filtre olarak kullanıyor ve zamanla arama yavaşlıyor. Neden?
## A
`fq` sayısal alanlarla çalışmaz; ondalıklı fiyat metin gibi ayrıştırılıp her sorguda yeniden sayıya dönüştürüldüğü için pahalıdır
🤖 `fq` sayısal alanlarla sorunsuz çalışır; sorun tip dönüşümü değil, filtrenin kardinalitesidir.
## B
Fiyat alanı `indexed=false` olduğu için filtre ters indeksi kullanamaz ve her sorguda tüm belgeleri tam tarama (full scan) ile gezer
🤖 Sorun indeksleme ayarı değil; alan indeksli olsa da benzersiz değerler önbelleği doldurur.
## C
`fq` `q`'dan yavaştır, çünkü sonuca fazladan bir kesişim (intersection) adımı ekler; tekrarlayan kısıtlar da `q` içine alınmalıdır
🤖 `fq` yavaş değil, kesişim ucuzdur; yavaşlatan şey yanlış kullanım, yani yüksek kardinalitedir.
## D
Neredeyse her fiyat benzersiz (yüksek kardinalite) olduğundan her sorgu yeni bir filterCache girişi üretir ve faydalı girişler atılır
🤖 Doğru: yüksek-kardinaliteli `fq` önbelleği kirletir ve isabet oranını çökertir.
## Doğru
D
## Açıklama
filterCache her **benzersiz** `fq` değeri için ayrı bir giriş tutar. `fiyat:499.99` gibi neredeyse **benzersiz** (high-cardinality) değerler filtre olunca her sorgu **yeni** bir önbellek girişi yaratır; önbellek hızla dolar, en faydalı (kategori/durum gibi tekrar eden) girişler **atılır** (eviction) ve isabet oranı çöker — buna **cache pollution / önbellek kirlenmesi** denir. (A) `fq` sayısal değer alır, tip sorunu yoktur. (B) Alan indekssiz olsaydı filtre hiç çalışmaz, "zamanla yavaşlama" değil baştan hata görülürdü. (C) `fq` genel olarak yavaş değildir; bitset kesişimi en ucuz adımlardan biridir. Çözümler: bu filtreyi `{!cache=false}` ile önbelleksiz çalıştırmak (bir kerelik hesap, önbelleği kirletmez) ya da **aralık** (range) filtresine dönüştürmek (`fq=fiyat:[* TO 500]`) — böylece az sayıda, tekrar eden kova (bucket) girişi oluşur.

# fq-4 | İleri
## Soru
**eDisMax** sorgu ayrıştırıcısının (query parser) temel amacı nedir?
## A
Solr sorgularını SQL'e çevirip Parallel SQL katmanı üzerinden JDBC istemcilerinin sonuç almasını sağlamak
🤖 SQL arayüzü ayrı bir Solr özelliğidir; eDisMax SQL üretmez, kullanıcı girdisini yorumlar.
## B
Kullanıcının sade girdisini birden çok alanda, alan-başına ağırlıkla (`qf=baslik^3 aciklama`) aramak ve boost/phrase ayarları sunmak
🤖 Doğru: eDisMax çok-alanlı, ağırlıklı, son-kullanıcı dostu arama içindir.
## C
Belge eklenirken alanların analiz zincirini seçmek ve üretilen terimleri ters indekse yazarak belgeyi aranabilir kılmak
🤖 Bu indeksleme aşamasının işidir; eDisMax yalnız sorgu zamanında devreye girer.
## D
Gelen belgelerden alan tiplerini tahmin edip schemaless modda şemaya yeni alanlar eklemek ve tip yönetimini yapmak
🤖 Tip tahmini schemaless/managed şema özelliğidir; eDisMax şemaya hiç dokunmaz.
## Doğru
B
## Açıklama
**DisMax** (Maximum Disjunction) ve gelişmişi **eDisMax**, son-kullanıcı arama kutusu için tasarlanmış sorgu ayrıştırıcılarıdır. Kullanıcının sade girdisini (`kablosuz kulaklık`) **birden çok alanda** arar ve her alana **ağırlık** verir: `qf=baslik^3 aciklama` (başlıktaki eşleşme 3 kat değerli). eDisMax ek olarak **`pf`** (phrase boost — kelimeler yan yana geçince ekstra puan), **`bf`/`boost`** (fonksiyon tabanlı boost — ör. yeniliğe/popülerliğe göre), operatör güvenliği ve kısmi hata toleransı sunar. Amaç: teknik olmayan kullanıcı girdisinden **iyi alaka** üretmek. (A) SQL'e çevirmez — Parallel SQL bambaşka bir bileşendir. (C) İndeksleme ve analiz zinciri, belge yazılırken çalışır; eDisMax sorgu zamanındadır. (D) Alan tipi tahmini schemaless modun işidir. eDisMax yalnız sorguyu yorumlar ve skorlar.

# fq-5 | İleri
## Soru
**queryResultCache** ile **filterCache** arasındaki fark nedir?
## A
queryResultCache tam bir sorgunun (`q`+`fq`+sort) sıralı sonuç kimlik listesini; filterCache ise tek bir `fq`'nun belge kümesini saklar
🤖 Doğru: biri tam sonuç listesi, diğeri tekil filtrenin bitset'i.
## B
İkisi de aynı veriyi saklar; queryResultCache dağıtık kurulumda kullanılan yeni ad, filterCache ise geriye dönük uyumluluk için tutulan eski addır
🤖 İki ayrı önbellektir, farklı granülerlikte farklı veri tutarlar; ad değişikliği söz konusu değildir.
## C
queryResultCache belgelerin stored alan değerlerini, filterCache ise her belge için hesaplanmış alaka skorlarını saklar
🤖 Stored değerler documentCache'in işidir ve `fq` skorlanmadığı için filterCache'te skor bulunmaz.
## D
filterCache yalnız SolrCloud'da bulunur; tek-node kurulumda filtreler queryResultCache içinde tutulur ve orada eşleşir
🤖 filterCache tek-node Solr'da da vardır; dağıtık kuruluma özel bir yapı değildir.
## Doğru
A
## Açıklama
**filterCache** granüler ve yeniden-kullanılabilirdir: **tek bir `fq`**'nun sonucunu (hangi belgeler geçti — skorsuz, sırasız bir **bitset**) saklar; farklı `q`'larla tekrar tekrar kesişimde kullanılır. **queryResultCache** ise **daha bütünseldir**: **belirli bir `q` + `fq` + sort** kombinasyonunun **sıralı sonuç kimlik listesini** (ilk N belgenin id'leri) saklar — yalnız birebir aynı sorgu tekrarında isabet eder. Yani filterCache "filtre parçalarını", queryResultCache "tam sonuç sayfasını" önbellekler. (B) Aynı şey değildirler, biri diğerinin yeni adı da değildir. (C) Stored alan değerlerini **documentCache** tutar; `fq` skorlanmadığı için filterCache'te skor yoktur. (D) filterCache tek-node kurulumda da çalışır, SolrCloud'a özel değildir.

# fq-6 | İleri
## Soru
Aynı üç kısıtı (`kategori:elektronik`, `durum:aktif`, `stok:[1 TO *]`) tek bir `fq` içinde `AND` ile birleştirmek yerine üç ayrı `fq` parametresi olarak vermenin filterCache açısından avantajı nedir?
## A
Ayrı `fq`'lar Solr tarafından paralel iş parçacıklarında yürütülür; asıl kazanç önbellekte değil, çok çekirdekli CPU hızlanmasındadır
🤖 Kazanç paralellikten gelmez; ayrı `fq`'ların değeri önbellekte yeniden kullanılabilir olmalarıdır.
## B
Ayrı yazılan `fq`'lar skorlamaya girip her biri alaka puanına kendi ağırlığını ekler; birleşik `fq` ise bu katkıyı tek seferde hesaplar
🤖 Kaç parçaya bölünürse bölünsün `fq` skorlamaya girmez; alaka puanına katkısı yoktur.
## C
Her `fq` ayrı bir bitset olarak önbelleklenip başka kombinasyonlarda yeniden kullanılır; birleşik `fq` yalnız aynı kombinasyon tekrarında isabet eder
🤖 Doğru: granüler `fq`'lar filterCache isabet oranını yükseltir.
## D
Ayrı `fq`'lar queryResultCache'te tek bir anahtara indirgenir; birleşik `fq` ise her sorgu için ayrı bir sonuç listesi anahtarı üretir
🤖 queryResultCache anahtarı `q`+`fq`+sort'un tamamıdır; `fq`'ları ayırmak anahtarı tekilleştirmez.
## Doğru
C
## Açıklama
Solr her `fq` parametresini **bağımsız** olarak değerlendirir ve her birinin sonucunu filterCache'te **ayrı bir bitset** olarak saklar. `fq=kategori:elektronik&fq=durum:aktif&fq=stok:[1 TO *]` yazarsan üç ayrı giriş oluşur; başka bir sorgu yalnız `kategori:elektronik` ile gelse bile o bitset **yeniden kullanılır**. Aynı kısıtları `fq=kategori:elektronik AND durum:aktif AND stok:[1 TO *]` diye tek parçada verirsen tek bir giriş oluşur ve bu giriş **yalnız o tam kombinasyon** birebir tekrar ettiğinde isabet eder; kombinasyonlar çoğaldıkça hem giriş sayısı patlar hem isabet oranı düşer. Yani granülerlik = yeniden kullanım. (A) Ayrı `fq`'lar paralellik için değil, önbellek granülerliği için ayrılır. (B) `fq` hiçbir biçimde skorlanmaz; parçalamak alaka puanını değiştirmez. (D) queryResultCache anahtarı zaten `q` + tüm `fq`'lar + sort'tan oluşur; `fq`'ları ayırmak o anahtarı sadeleştirmez. İstisna: kısıtlar daima birlikte geliyorsa ve kombinasyonun kendisi sık tekrar ediyorsa tek `fq` da makuldür.
