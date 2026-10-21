---
slug: lucene-ters-indeks
baslik: "Lucene Ters İndeks: term→posting, neden SQL LIKE ölçeklenmez"
kademe: Orta
sira: 167
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Elinde 500 sayfalık bir kitap var ve "enflasyon" kelimesinin nerede geçtiğini bulmak istiyorsun. İki yol var. Birincisi: ilk sayfadan başlayıp son sayfaya kadar her satırı okumak. İkincisi: kitabın arkasındaki **dizin**e bakmak — orada "enflasyon → 47, 112, 350" yazar, doğrudan o sayfalara gidersin. Arama motorlarının tamamı, ikinci yolun makineleşmiş halidir.

Önce dört kelime. **Belge (document)**: sistemin sakladığı tek bir kayıt — bir haber metni, bir ürün açıklaması, bir CV. **Alan (field)**: belgenin parçaları; başlık, gövde, fiyat. **Terim (term)**: belgeden ayıklanmış tek bir arama birimi, kabaca bir kelime. **Sorgu (query)**: kullanıcının yazdığı arama. **İndeks (index)**: kitabın arkasındaki dizinin makinedeki karşılığı.

İlişkisel veritabanı birinci yolu izler; satırları "belge 1 → şu metin" biçiminde saklar. `WHERE aciklama LIKE '%kredi%'` yazdığında motor her satırın metnini tek tek kontrol eder. Bu bir **tam tablo taramasıdır (full table scan)** ve maliyet satır sayısıyla **doğrusal** büyür (O(n)).

Sıradan bir B-tree indeksi de kurtarmaz. B-tree değerleri baştan sıralar; bu yüzden `'kredi%'` gibi bir önek aramasını hızlandırır. Ama baştaki `%` işareti "her şeyle başlayabilir" demektir; sıralamadan yararlanılamaz ve indeks devre dışı kalır. Sorun ne diskin yavaşlığıdır ne de `LIKE`'ın yasak oluşu. Sorun, kullanılabilir bir indeksin olmamasıdır.

**Apache Lucene** tam olarak bu problemi çözen kütüphanedir. Solr, Elasticsearch ve OpenSearch ayrı ürünler gibi görünür; ama üçünün de indeksleme ve arama çekirdeği aynı Lucene'dir. Farkları üst katmanlardadır: dağıtım ve küme modeli, sorgu API'si, işletim araçları. OpenSearch ayrıca Elasticsearch'ten çatallanmıştır (fork) — yine Lucene tabanlıdır.

### Temel kavramlar

**Ters indeks** (inverted index) ilişkiyi tersine çevirir: "belge → kelimeler" yerine "kelime → belgeler". Kitabın arkasındaki dizin de tam olarak budur.

**Term dictionary** (terim sözlüğü): indekste geçen tüm farklı terimlerin sıralı listesi — dizindeki alfabetik kelime sütunu.

**Posting list** (gönderi listesi): bir terimin karşısında duran liste. O terimin geçtiği belgeleri, belge kimliğine göre **sıralı** biçimde tutar; dizindeki sayfa numaraları sütunu gibi. Zenginleştirilmiş biçimi her belge için iki bilgi daha taşır: **sıklık** (term frequency; skorlamada kullanılır) ve **konum** (position; tırnak içinde ifade araması için). Posting list ham belge metni saklamaz ve bir sorgu sonucu önbelleği de değildir; indeksin kendi çekirdek yapısıdır.

**Segment**: Lucene indeksi tek dev bir dosya değil, çok sayıda segmentten oluşan bir koleksiyondur.

**Alan yetenekleri**: bir alanın aranabilir, geri okunabilir ve sıralanabilir olması birbirinden bağımsız üç seçimdir.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ters indeks: terim sözlüğünden posting listlerine">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ters İndeks: terim → posting list</text>
  <rect x="30" y="46" width="200" height="180" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="130" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Term Dictionary (sözlük)</text>
  <text x="130" y="98" text-anchor="middle" font-size="12" fill="#1C3247" font-family="monospace">"kredi"</text>
  <text x="130" y="134" text-anchor="middle" font-size="12" fill="#1C3247" font-family="monospace">"faiz"</text>
  <text x="130" y="170" text-anchor="middle" font-size="12" fill="#1C3247" font-family="monospace">"mevduat"</text>
  <line x1="185" y1="94" x2="410" y2="94" stroke="#186399" stroke-width="1.5"/>
  <polygon points="410,94 401,90 401,98" fill="#186399"/>
  <line x1="185" y1="130" x2="410" y2="130" stroke="#186399" stroke-width="1.5"/>
  <polygon points="410,130 401,126 401,134" fill="#186399"/>
  <line x1="185" y1="166" x2="410" y2="166" stroke="#186399" stroke-width="1.5"/>
  <polygon points="410,166 401,162 401,170" fill="#186399"/>
  <rect x="415" y="80" width="195" height="28" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="512" y="99" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="monospace">→ [d2:2, d7:1, d9:3]</text>
  <rect x="415" y="116" width="195" height="28" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="512" y="135" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="monospace">→ [d2:1, d5:4]</text>
  <rect x="415" y="152" width="195" height="28" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="512" y="171" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="monospace">→ [d7:2]</text>
  <text x="320" y="212" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Arama = sözlükte terimi bul → hazır posting list'i oku (belge sayısından bağımsız)</text>
</svg>
```

### Nasıl çalışır?

**İndeksleme.** Metin bir analiz zincirinden geçer (küçük harfe indirme, kelimelere bölme, kök bulma), geriye terimler kalır. Her terim sözlüğe eklenir, belgenin kimliği de o terimin posting list'inin sonuna yazılır.

**Arama.** Sorgudaki kelime aynı analizden geçirilir, sözlükte aranır (O(log n) ya da daha iyi) ve hazır posting list okunur. İki terimli bir sorgu, iki sıralı listenin kesişimidir. Listelerin sıralı olması bu kesişimi tek geçişte yapılabilir kılar. Süre toplam belge sayısına değil, terimin nadirliğine bağlıdır. `LIKE '%…%'` ile arasındaki temel fark da budur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tam tablo taraması O(n) ile ters indeks aramasının karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı arama, iki maliyet</text>
  <rect x="26" y="42" width="276" height="150" rx="12" fill="#ffffff" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="164" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="monospace">LIKE '%kredi%' → tam tarama</text>
  <rect x="48" y="76" width="232" height="17" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="0.8"/>
  <text x="60" y="89" font-size="10.5" fill="#5D7789" font-family="system-ui">belge 1 — tara</text>
  <rect x="48" y="97" width="232" height="17" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="0.8"/>
  <text x="60" y="110" font-size="10.5" fill="#5D7789" font-family="system-ui">belge 2 — tara</text>
  <rect x="48" y="118" width="232" height="17" rx="3" fill="#EAF1F7" stroke="#5D7789" stroke-width="0.8"/>
  <text x="60" y="131" font-size="10.5" fill="#5D7789" font-family="system-ui">belge 3 — tara</text>
  <text x="164" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">… her satır tek tek …</text>
  <text x="164" y="176" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">O(n): belge sayısıyla büyür</text>
  <text x="320" y="120" text-anchor="middle" font-size="13" font-weight="700" fill="#1C3247" font-family="system-ui">vs</text>
  <rect x="338" y="42" width="276" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Ters indeks → doğrudan erişim</text>
  <rect x="356" y="82" width="110" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="411" y="103" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">sözlük: "kredi"</text>
  <line x1="466" y1="99" x2="520" y2="99" stroke="#186399" stroke-width="1.5"/>
  <polygon points="520,99 511,95 511,103" fill="#186399"/>
  <rect x="522" y="82" width="84" height="34" rx="6" fill="#ffffff" stroke="#25804C" stroke-width="1.2"/>
  <text x="564" y="103" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">[d2,d7,d9]</text>
  <text x="476" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sözlükte bul → hazır listeyi oku</text>
  <text x="476" y="176" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">≈ O(log n): belge sayısından bağımsız</text>
</svg>
```

**Segmentler ve değişmezlik.** Her segment, kendi başına eksiksiz ve küçük bir ters indekstir. Yazıldıktan sonra **değişmezdir (immutable)**. Yeni bir belge var olan segmenti güncellemez; yeni bir segmente yazılır. Silme işlemi belgeyi yalnız "silindi" diye işaretler; fiziksel olarak hemen atmaz. Arka planda çalışan **birleştirme (merge)** küçük segmentleri büyüklere kaynaştırır ve bu sırada silinmiş belgeleri gerçekten çıkarır. Değişmezliğin üç kazancı vardır: kilitsiz eşzamanlı okuma, geçersizleşmeyen önbellek ve çökme sonrası tutarlılık.

**Üç bağımsız alan yeteneği.** **indexed** açıksa alan ters indekse girer, yani aranabilir olur. **stored** açıksa alanın ham değeri belge yönlü saklanır ve sonuç ekranında geri okunabilir. **docValues** açıksa değerler sütun yönlü (columnar) saklanır; sıralama, faceting ve toplama (aggregation) hızlanır.

Üçü ayrı ayrı açılır. Dev bir gövde metni aranabilir ama saklanmayabilir. Yalnız ekranda gösterilen bir resim adresi saklanabilir ama aranmayabilir. Sadece göstermek için `stored` yeterlidir. `docValues` gösterim için değildir; "tüm belgelerde tek bir sütunu tara" işleri içindir. `stored` ise binlerce belgeyi sıralamak için verimsizdir, çünkü her belgeyi tek tek açmak gerekir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir alanın üç bağımsız yeteneği: indexed, stored, docValues">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir alanın üç bağımsız yeteneği</text>
  <rect x="24" y="44" width="192" height="112" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="monospace">indexed</text>
  <text x="120" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ters indekse girer</text>
  <text x="120" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">→ ARANABİLİR</text>
  <text x="120" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"kredi" sorgusu bulur</text>
  <rect x="224" y="44" width="192" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="monospace">stored</text>
  <text x="320" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ham değer saklanır</text>
  <text x="320" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">→ GERİ OKUNUR</text>
  <text x="320" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonuçta gösterilir</text>
  <rect x="424" y="44" width="192" height="112" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="520" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="monospace">docValues</text>
  <text x="520" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sütun-yönlü saklama</text>
  <text x="520" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">→ SIRALA / FACET</text>
  <text x="520" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">toplama (aggregation)</text>
  <text x="320" y="184" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Üçü ayrı ayrı açılır: örn. aranır ama saklanmaz, ya da saklanır ama aranmaz</text>
</svg>
```

### Mülakatta ne sorulur?

- "Neden `LIKE '%x%'` yerine arama motoru?" Beklenen cevap: baştaki joker indeksi kullanılamaz kılar, maliyet O(n) olur. "Disk yavaş" ya da "`LIKE` yasak" cevapları elenir.
- "Posting list'te ne durur?" Belge kimlikleri, isteğe bağlı sıklık ve konum. Tuzak: "ham belge metni" ya da "sorgu önbelleği" demek.
- "Segment neden değişmez, silinen belge ne zaman gider?" Değişmezlik kilitsiz okuma ve güvenli önbellek sağlar; silinen belge fiziksel olarak merge sırasında atılır.
- "Bu alanı gösteriyorum ama aramıyorum, ne yaparsın?" stored açık, indexed kapalı. Üç yeteneğin hepsini açmak indeks boyutunu boşuna büyütür.
- "stored ile docValues farkı?" Erişim deseni farkıdır: belge-yönlü tekil okuma vs. sütun-yönlü toplu tarama. Klasik hata yönleri ters söylemek ya da "aynı şey" demek.
- "Solr, Elasticsearch, OpenSearch farkı?" Çekirdek aynıdır (Lucene); fark üst katmandadır.

### Tek cümlede

Ters indeks, "belge → kelime" ilişkisini "kelime → belgeler" ilişkisine çevirir ve aramayı belge sayısından bağımsız kılar. Lucene bunu değişmez segmentlerde tutar. Bir alanın aranması (indexed), gösterilmesi (stored) ve sıralanması (docValues) ise birbirinden bağımsız üç ayrı seçimdir.

# lucene-1 | Orta
## Soru
`WHERE aciklama LIKE '%kredi%'` sorgusu milyonlarca satırlık bir tabloda neden ölçeklenmez?
## A
`LIKE` operatörü SQL standardında yasaktır; motor sorguyu ayrıştırırken hata verip reddeder
🤖 `LIKE` tamamen geçerli bir operatördür; sorun yasak olması değil, çalışma maliyetidir.
## B
Baştaki joker B-tree indeksini kullanılamaz kılar; motor tüm satırları taramak zorunda kalır
🤖 Doğru: `%...%` önek eşleşmesini bozar, indeks devre dışı kalır, maliyet O(n) olur.
## C
`LIKE` sonucu tampon havuzuna (buffer pool) hiç alınmaz; her satır diskten yeniden okunduğu için yavaşlar
🤖 Tamponlama sorunun kaynağı değil; yavaşlığı indekssiz tam tarama üretir.
## D
Motor `LIKE` için arka planda otomatik ters indeks kurar; bu indeksin güncellenmesi sorguyu bekletir
🤖 İlişkisel motorlar `LIKE` için ters indeks kurmaz; tam metin arama ayrı bir motorun işidir.
## Doğru
B
## Açıklama
`LIKE '%kredi%'` metnin **herhangi bir yerinde** geçen kalıbı arar. B-tree indeksi değerleri baştan sıralar; baştaki `%` "her şeyle başlayabilir" dediği için bu sıralamadan yararlanılamaz ve indeks kullanılamaz hale gelir. Motor her satırın metnini teker teker kontrol eder — bu bir **tam tablo taramasıdır** (full table scan) ve maliyet satır sayısıyla **doğrusal** (O(n)) artar. Ters indeks tam da bunu çözer: "kredi" terimini bir kez **term dictionary**'de bulur ve doğrudan o terimin **posting list**'ine gider; hiçbir belgeyi taramaz, süre belge sayısından bağımsızdır. (A) `LIKE` yasak değildir; (C) sorun disk/tampon değil indekssizliktir; (D) ilişkisel motorlar `LIKE` için ters indeks üretmez.

# lucene-2 | Orta
## Soru
Lucene ters indeksinde bir **posting list** ne tutar?
## A
Veritabanındaki tüm satırların ham kopyasını, belge kimliğine göre sıralanmış biçimde
🤖 Ham satırları saklamak ters indeksin değil, stored alanların ya da veritabanının işidir.
## B
Her belgenin tam metnini, terimleri alfabetik sıraya dizilmiş bir sözlük halinde
🤖 Bu bir düz metin deposunu tarif eder; posting list belge başına değil, terim başına tutulur.
## C
Bir terimin geçtiği belgelerin sıralı listesini; isteğe bağlı sıklık ve konum bilgisiyle
🤖 Doğru: posting list = terim → [belge kimliği, sıklık, konumlar].
## D
Sık çalışan sorguların önbelleğe alınmış sonuç kümelerini, terim başına gruplanmış olarak
🤖 Bu bir sorgu önbelleğini anlatır; posting list indeksin kendi çekirdek yapısıdır.
## Doğru
C
## Açıklama
**Posting list**, bir terimin hangi belgelerde geçtiğini tutan, belge kimliğine göre **sıralı** bir listedir. Temel biçimi yalnız belge kimliklerinden oluşur; zenginleştirilmiş biçimi her belge için **sıklığı** (term frequency — skorlama için) ve **konumları** (positions — ifade/phrase sorguları için) da içerir. Sıralı olması, çok terimli sorgularda listelerin kesişimini tek geçişte almayı mümkün kılar. Sözlük (term dictionary) terimden bu listeye O(log n) ya da daha iyi erişim verir; böylece arama "belgeleri tara" değil, "terimi bul, hazır listeyi oku" işine dönüşür. (A) ham satır kopyası saklamak ters indeksin işi değildir; (B) belge başına tam metin, posting list değil metin deposudur; (D) sorgu önbelleği indeksin kendisi değil, üstüne konan bir katmandır.

# lucene-3 | İleri
## Soru
Lucene segmentleri hakkında hangisi doğrudur?
## A
Segmentler değiştirilebilir; her yeni belge en güncel segmentin içine eklenerek yazılır
🤖 Segmentler yazıldıktan sonra değişmezdir (immutable); yeni belge yeni segmente gider.
## B
Segmentler her sorguda bellekte yeniden oluşturulur; diskte kalıcı bir karşılıkları bulunmaz
🤖 Segmentler diskte kalıcıdır; sorgu sırasında yeniden kurulmaz, yalnızca okunur.
## C
İndeks tek ve dev bir segment dosyasıdır; büyüdükçe yerinde genişletilir, parçalanmaz
🤖 İndeks çok sayıda segmentten oluşan bir koleksiyondur; tek dev dosya değildir.
## D
Her segment değişmez, eksiksiz bir ters indekstir; birleştirme silinenleri fiziksel atar
🤖 Doğru: değişmezlik + merge, Lucene'in yazma/okuma modelinin temelidir.
## Doğru
D
## Açıklama
Lucene indeksi bir **segment koleksiyonudur**. Her segment küçük ama kendi başına eksiksiz bir ters indekstir ve bir kez yazıldıktan sonra **değişmezdir** (immutable). Yeni belgeler var olan segmenti değiştirmez, **yeni** bir segmente yazılır. Silme, ilgili belgeyi "silindi" olarak işaretler ama fiziksel olarak hemen çıkarmaz. Arka planda çalışan **birleştirme** (segment merge) küçük segmentleri daha büyüklere kaynaştırır ve bu sırada silinmiş belgeleri gerçekten atar. Değişmezliğin faydaları: kilitsiz eşzamanlı okuma, güvenli önbellekleme (segment değişmediği için önbellek geçersizleşmez) ve çökme sonrası tutarlılık. (A) yeni belge var olan segmenti güncellemez; (B) segmentler diskte kalıcıdır, sorgu başına üretilmez; (C) indeks tek dosya değil, çok segmentlidir.

# lucene-4 | Orta
## Soru
Bir alanı yalnız **sonuç ekranında göstermek** (ama üzerinde arama yapmamak) istiyorsun. Lucene/Solr'da hangi ayar gerekir?
## A
`stored=true`, `indexed=false`, `docValues=false`
🤖 Doğru: gösterim stored ister; arama gerekmediği için indexed kapalı bırakılır.
## B
`indexed=true`, `stored=false`, `docValues=false`
🤖 Bu alan aranır ama ham değeri geri okunamaz; sonuç ekranında gösterilemez.
## C
`indexed=true`, `docValues=true`, `stored=false`
🤖 docValues sıralama/faceting içindir; stored olmadan ham değer gösterime dönmez.
## D
`indexed=true`, `stored=true`, `docValues=true`
🤖 Üçünü birden açmak, hiç kullanılmayacak indeks ve disk maliyetini boşuna doğurur.
## Doğru
A
## Açıklama
Lucene bir alanın üç yeteneğini **bağımsız** yönetir: **indexed** (ters indekse girer, aranabilir), **stored** (ham değeri geri okunabilir, sonuçta gösterilebilir) ve **docValues** (sütun-yönlü saklama; sıralama, faceting, toplama için). Sadece göstermek için **stored=true** yeterlidir; arama yapılmayacağı için **indexed=false** ile gereksiz indeks maliyeti ve indeks boyutu önlenir. (B) yalnız indexed ile değer geri okunamaz, ekranda gösterilemez; (C) docValues gösterim için değil sıralama/toplama içindir, stored'ın yerini tutmaz; (D) üç yeteneğin hepsini açmak zorunlu değildir ve kullanılmayan yetenek disk ile indeksleme süresine yazılır. Bu ayrımı bilmek indeks boyutunu ve performansını doğrudan etkiler.

# lucene-5 | İleri
## Soru
Solr, Elasticsearch ve OpenSearch arasındaki ortak nokta nedir?
## A
Üçü de tamamen bağımsız arama motorlarıdır; hiçbir ortak kod tabanı paylaşmazlar
🤖 Üçü de aynı indeksleme/arama çekirdeğini paylaşır; bağımsız motorlar değildir.
## B
Üçünün de indeksleme ve arama çekirdeği aynı kütüphanedir: Apache Lucene
🤖 Doğru: Lucene ortak çekirdek; ayrıştıkları yer üstteki katmanlardır.
## C
Üçü de arama indeksini PostgreSQL tabloları üzerinde tutar; Lucene yalnız arayüzdür
🤖 Hiçbiri ilişkisel veritabanı üzerine kurulu değildir; çekirdek doğrudan Lucene'dir.
## D
Yalnız OpenSearch Lucene tabanlıdır; Solr ve Elasticsearch kendi motorlarını yazmıştır
🤖 Üçü de Lucene tabanlıdır; OpenSearch zaten Elasticsearch'ten çatallanmıştır.
## Doğru
B
## Açıklama
Solr, Elasticsearch ve OpenSearch'ün **ortak çekirdeği Apache Lucene**'dir: ters indeks, analiz zinciri, skorlama ve segment yönetimi Lucene'den gelir. Farklar **üst katmanlardadır**: farklı sorgu API'leri (Solr'ın istek parametreleri vs. Elasticsearch'ün JSON DSL'i), farklı küme/koordinasyon modelleri (SolrCloud + ZooKeeper vs. ES'in kendi koordinasyonu) ve farklı operasyon araçları. OpenSearch ise Elasticsearch'ün açık kaynak çatalıdır (fork) ve yine Lucene tabanlıdır. "Aynı motor, farklı elbise" demek bu yüzden yanlış olmaz. (A) ortak çekirdeği inkâr eder; (C) çekirdeği ilişkisel veritabanına oturtur, oysa Lucene kendi dosya biçimini kullanır; (D) Lucene'i yalnız birine yakıştırır.

# lucene-6 | İleri
## Soru
`docValues=true` ile `stored=true` arasındaki fark nedir?
## A
İkisi de aynı fiziksel yapıdır; yalnızca farklı sürümlerde farklı adlar almışlardır
🤖 Farklı erişim desenleri için farklı fiziksel düzenlerdir; adlandırma farkı değildir.
## B
docValues satır-yönlü, stored sütun-yönlü saklamadır; roller birbirinin tersidir
🤖 Yönler ters verilmiş: docValues sütun-yönlü, stored belge (satır) yönlüdür.
## C
docValues sütun-yönlü saklamadır; stored belge-yönlü ham değer saklamasıdır
🤖 Doğru: erişim deseni farkı — toplu sütun taraması vs. tekil belge okuma.
## D
docValues yalnız metin, stored yalnız sayısal alanlar için kullanılabilir
🤖 İkisi de çeşitli tipleri saklar; ayrım tipte değil, fiziksel düzendedir.
## Doğru
C
## Açıklama
**stored** alanlar **belge-yönlüdür**: "şu belgenin şu alanının ham değeri" biçiminde saklanır; sonuç listesinde başlığı ya da fiyatı geri okumak için idealdir, ama binlerce belgeyi tek alan üzerinden sıralamak/toplamak için verimsizdir çünkü her belgeyi tek tek açmak gerekir. **docValues** ise **sütun-yönlüdür** (columnar): bir alanın tüm değerleri bir arada, sıkıştırılmış ve ardışık saklanır; **sıralama** (sort), **faceting** ve **toplama** (aggregation) gibi "tüm belgelerde tek sütunu tara" işlemleri için çok hızlıdır. Bu yüzden sıralanan/facet edilen alanlarda docValues, sonuçta gösterilen alanlarda stored açılır; ikisi birbirinin yerine geçmez. (A) aynı yapı değildirler; (B) yönler ters verilmiştir; (D) ayrım alan tipinde değil, fiziksel düzen ve erişim desenindedir.
