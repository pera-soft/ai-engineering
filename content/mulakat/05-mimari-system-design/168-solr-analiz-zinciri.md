---
slug: solr-analiz-zinciri
baslik: "Solr Analiz Zinciri: char filter → tokenizer → token filter, Türkçe i/İ"
kademe: İleri
sira: 168
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Elinde on bin sayfalık bir yemek kitabı olduğunu düşün. "zeytinyağı" geçen sayfaları bulmak için kitabı baştan sona okumak saatler alır. Onun yerine kitabın sonundaki dizine bakarsın: kelimeler alfabetik sıralıdır ve her kelimenin yanında geçtiği sayfa numaraları yazar. Arama motorları da tam olarak bu dizini kurar; adı **ters indeks** (inverted index).

Dört kelimeyi bilmen yeter:

- **Belge** (document): aranabilir tek bir kayıt — bir haber yazısı, bir ürün sayfası, bir CV.
- **Terim** (term): indekste saklanan en küçük aranabilir parça; çoğunlukla bir kelime.
- **İndeks** (index): "hangi terim hangi belgelerde geçiyor" listesi, yani kitabın sonundaki dizin.
- **Sorgu** (query): kullanıcının arama kutusuna yazdığı metin.

Ama dizin kendiliğinden oluşmaz. Ham metnin önce **aranabilir parçalara doğranması** gerekir. Sorular şunlardır: `<b>Krediler ve Faiz</b>` cümlesinden hangi terimler çıkacak? Etiket kalacak mı? Büyük harf ne olacak? "Krediler" ile "kredi" aynı sayılacak mı?

Bu doğrama işine **analiz (analysis)**, işi yapan boru hattına da **analiz zinciri (analysis chain)** denir. Aynı doğrama hem belgeye hem sorguya uygulanmazsa arama sessizce boş döner. Bu konunun tüm meselesi budur.

### Temel kavramlar

Zincirin üç tür halkası vardır ve her biri farklı bir şeye dokunur:

1. **Char filter** (karakter süzgeci): ortada henüz kelime yokken, ham karakter akışını düzeltir. HTML etiketlerini söker, `&` yerine `ve` yazar.
2. **Tokenizer** (belirteçleyici): akışı **token**'lara, yani parçalara böler. Bunu yaparken boşluk ve noktalama sınırlarını kullanır. Bir zincirde **tam olarak bir** tokenizer bulunur; doğrama kararını tek bir bileşen verir.
3. **Token filter** (token süzgeci): artık elde kelimeler vardır; her token'ı tek tek dönüştürür ya da atar. Küçük harfe indirme (lowercase), durak kelime (stopword) atma, kök bulma (stemming), eşanlamlı (synonym) ekleme burada olur.

İkinci ayrım zamanla ilgilidir: aynı zincir hem belge indekslenirken (**index-time**) hem sorgu geldiğinde (**query-time**) çalışır. Solr şemasında bu iki taraf ayrı ayrı tanımlanabilir — ve hataların çoğu tam buradan doğar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Analiz zinciri: char filter, tokenizer, token filter">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Analiz Zinciri (index-time = query-time olmalı)</text>
  <rect x="24" y="80" width="120" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="103" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Char filter</text>
  <text x="84" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">HTML soy, eşle</text>
  <rect x="176" y="80" width="120" height="52" rx="8" fill="#F5EFE0" stroke="#B8860B" stroke-width="1.5"/>
  <text x="236" y="103" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A6608" font-family="system-ui">Tokenizer</text>
  <text x="236" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">böl → token</text>
  <rect x="328" y="80" width="120" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="388" y="103" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Token filter</text>
  <text x="388" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">küçült/kök/eşanlam</text>
  <rect x="480" y="80" width="136" height="52" rx="8" fill="#F0E9F4" stroke="#7A4FA0" stroke-width="1.5"/>
  <text x="548" y="103" text-anchor="middle" font-size="11.5" font-weight="700" fill="#7A4FA0" font-family="system-ui">Ters indeks</text>
  <text x="548" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">terimler</text>
  <line x1="144" y1="106" x2="174" y2="106" stroke="#1C3247" stroke-width="1.5"/>
  <polygon points="174,106 165,102 165,110" fill="#1C3247"/>
  <line x1="296" y1="106" x2="326" y2="106" stroke="#1C3247" stroke-width="1.5"/>
  <polygon points="326,106 317,102 317,110" fill="#1C3247"/>
  <line x1="448" y1="106" x2="478" y2="106" stroke="#1C3247" stroke-width="1.5"/>
  <polygon points="478,106 469,102 469,110" fill="#1C3247"/>
  <text x="320" y="168" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">"&lt;b&gt;Krediler&lt;/b&gt;" → "krediler" → [kredi]</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Türkçe tuzağı: "İ".toLowerCase() → İngilizce'de "i̇", Türkçe'de "i" olmalı</text>
</svg>
```

### Nasıl çalışır?

Sıra keyfî değildir; bir bağımlılık zinciridir. Char filter çalışırken ortada hâlâ ham bir karakter akışı vardır. Tokenizer o akışı bölerek token üretir. Token filter ise ancak token varsa iş görebilir.

Bu yüzden char filter tokenizer'dan sonraya konamaz; ondan sonra ortada ham akış kalmamıştır. Token filter de tokenizer'dan önce konamaz; henüz token yoktur. Yani tek geçerli sıra **char filter → tokenizer → token filter**'dır.

Arama, analiz edilmiş terimlerin **birebir eşleşmesidir**. Belge tarafında `Kredi` küçültülüp indekse `kredi` diye yazılmışsa, sorgu tarafında küçültme yoksa motor `Kredi` terimini arar; indekste öyle bir terim yoktur ve belge bulunamaz. Solr araya girip "aslında aynı kelime" demez, hata da fırlatmaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Index-time küçültme var, query-time yok: terimler eşleşmez">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Uyumsuz analiz = kaçan eşleşme</text>
  <text x="24" y="58" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">index-time</text>
  <rect x="24" y="66" width="96" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="72" y="86" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">"Kredi"</text>
  <line x1="120" y1="81" x2="156" y2="81" stroke="#1C3247" stroke-width="1.4"/>
  <polygon points="156,81 147,77 147,85" fill="#1C3247"/>
  <rect x="158" y="66" width="104" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="210" y="86" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">lowercase ✓</text>
  <line x1="262" y1="81" x2="298" y2="81" stroke="#1C3247" stroke-width="1.4"/>
  <polygon points="298,81 289,77 289,85" fill="#1C3247"/>
  <rect x="300" y="66" width="96" height="30" rx="6" fill="#ffffff" stroke="#186399" stroke-width="1.2"/>
  <text x="348" y="86" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">kredi</text>
  <text x="24" y="132" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">query-time</text>
  <rect x="24" y="140" width="96" height="30" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <text x="72" y="160" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">"Kredi"</text>
  <line x1="120" y1="155" x2="156" y2="155" stroke="#1C3247" stroke-width="1.4"/>
  <polygon points="156,155 147,151 147,159" fill="#1C3247"/>
  <rect x="158" y="140" width="104" height="30" rx="6" fill="#ffffff" stroke="#8A2E2E" stroke-width="1.2"/>
  <text x="210" y="160" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">lowercase YOK</text>
  <line x1="262" y1="155" x2="298" y2="155" stroke="#1C3247" stroke-width="1.4"/>
  <polygon points="298,155 289,151 289,159" fill="#1C3247"/>
  <rect x="300" y="140" width="96" height="30" rx="6" fill="#ffffff" stroke="#B26B00" stroke-width="1.2"/>
  <text x="348" y="160" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">Kredi</text>
  <rect x="430" y="86" width="186" height="64" rx="10" fill="#ffffff" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="523" y="112" text-anchor="middle" font-size="12" fill="#1C3247" font-family="monospace">kredi ≠ Kredi</text>
  <text x="523" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">EŞLEŞMEZ</text>
</svg>
```

Bu hatanın en sinsi tarafı sessiz olmasıdır: istisna yok, log yok, sadece eksik sonuç var. İki taraf **bilerek** farklılaştırılabilir, ama bu bilinçli bir tasarım kararıdır, kaza değil.

Türkçe'nin kendine has tuzağı i/İ harflerindedir. İngilizce kuralında `I` küçültülünce `i` olur. Türkçe'de ise noktalı/noktasız ayrımı vardır: `İ` → `i` ve `I` → `ı`. Dil ayarını (locale) dikkate almayan bir küçültme, `İSTANBUL` kelimesini `i̇stanbul` gibi bozuk bir terime çevirir; `istanbul` sorgusu da tutmaz. Kelime silinmez, motor da çökmez; sessizce yanlış bir terim üretilir. Çözüm: Türkçe alanlarda `TurkishLowerCaseFilter` ve Türkçe kök bulma (`TurkishStemmer`, snowball) kullanmak.

Otomatik tamamlama (autocomplete) için **edge n-gram** kullanılır. `kredi` terimi baştan büyüyen öneklere açılır: `k, kr, kre, kred, kredi`. Bu açılım tipik olarak **yalnız indeksleme anında** yapılır. Kullanıcı `kre` yazınca bu ham önek, indeksteki `kre` gramıyla doğrudan eşleşir.

Sorgu tarafında da gram üretmek genelde istenmez. Çünkü sorgu da parçalanırsa `k` ve `kr` gramları alakasız belgelerle eşleşir ve sonucu kirletir. Aynı işi veritabanı tarzı desen taramasıyla (`LIKE '%kre%'`) çözmek de seçenek değildir; bu tarama indeksten yararlanamaz ve belge sayısı arttıkça ölçeklenmez.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Edge-n-gram: kredi kelimesi öneklere açılır, kre girdisi eşleşir">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Edge-n-gram ile otomatik tamamlama</text>
  <text x="30" y="56" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">index-time: "kredi" → önekler (gram)</text>
  <rect x="30" y="66" width="60" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="60" y="86" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">k</text>
  <rect x="98" y="66" width="60" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="128" y="86" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">kr</text>
  <rect x="166" y="66" width="60" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="196" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="monospace">kre</text>
  <rect x="234" y="66" width="66" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="267" y="86" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">kred</text>
  <rect x="308" y="66" width="66" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="341" y="86" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">kredi</text>
  <text x="30" y="132" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">query-time: kullanıcı "kre" yazar (ham önek)</text>
  <rect x="166" y="142" width="60" height="30" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="196" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="monospace">kre</text>
  <line x1="196" y1="142" x2="196" y2="98" stroke="#25804C" stroke-width="1.6"/>
  <polygon points="196,98 192,107 200,107" fill="#25804C"/>
  <text x="312" y="162" font-size="11" fill="#5D7789" font-family="system-ui">→ "kre" gramıyla anında eşleşir</text>
</svg>
```

Eşanlamlı (synonym) genişletmesi hangi tarafta yapılmalı? **İndeksleme anında** yapılırsa eşanlamlılar kalıcı terim olarak indekse yazılır. O zaman indeks büyür ve sözlük her değiştiğinde tüm belgeleri **yeniden indekslemek (reindex)** gerekir.

**Sorgu anında** yapılırsa indekse hiçbir şey yazılmaz. Sözlüğe yeni bir eşleme eklediğin an, bir sonraki sorgudan itibaren geçerli olur. Bu esneklik bedava değildir: çok kelimeli eşanlamlıların konum yönetimi ve skorlamaya etkisi asıl olarak sorgu anında zorlaşır.

**Durak kelime (stopword)** filtresi, dilde çok sık geçen ve bu yüzden **ayırt ediciliği düşük** kelimeleri atar: `ve, ile, bir, the, is` gibi. Ölçüt kelimenin uzunluğu değil, önceden tanımlanmış bir listedir.

İki tuzağı vardır. (1) İndeks ve sorgu tarafında tutarsız uygulanırsa yine eşleşme kaçar. (2) Bazı ifadeler tümüyle durak kelimelerden oluşur ("to be or not to be") ve hepsini silmek ifadeyi yok eder. Ayrıca modern skorlama (BM25) sık geçen terimleri zaten düşük ağırlıklandırır; bu yüzden durak kelime atmak birçok senaryoda gereksizdir.

### Mülakatta ne sorulur?

- "Analiz zincirindeki bileşenlerin sırası nedir, neden bu sıra?" — Sırayı ezberden saymak yetmez. Bağımlılığı da söyle: token yoksa token filter çalışamaz. Bir zincirde **tek** tokenizer olduğunu da ekle.
- "Belgeler bulunmuyor ama indekste duruyorlar, nereye bakarsın?" — beklenen ilk cevap: index-time ve query-time analizini karşılaştırmak (Solr'ın Analysis ekranı). Klasik tuzak: cevabı sorgu sözdiziminde aramak.
- "Türkçe alanda `İSTANBUL` neden `istanbul` ile eşleşmiyor?" — i/İ tuzağı. Tuzak, "Solr hata verir" ya da "kelime silinir" demek; gerçekte sessizce yanlış terim üretilir.
- "Autocomplete'i nasıl kurarsın?" — edge n-gram'ı index tarafında üret, query tarafında ham öneği ara. "Her iki tarafta da uygularım" cevabı elenir.
- "Eşanlamlıyı indeksleme anında mı, sorgu anında mı yaparsın?" — Aradaki takası anlat: bir yanda yeniden indeksleme maliyeti, öbür yanda çok kelimeli eşanlam ve skorlama zorluğu. Mutlak bir "şu her zaman daha doğru" cevabı beklenmiyor.
- "Stopword kullanır mısın?" — ne olduğunu tanımla, iki tuzağı say, BM25 varken çoğu zaman gerekmediğini ekle.

### Tek cümlede

Analiz, ham metni char filter → tokenizer → token filter sırasıyla aranabilir terimlere doğrar. İndeksleme anı ile sorgu anı bu doğramada uyuşmazsa arama hata vermeden, sessizce boş döner.

# analiz-1 | İleri
## Soru
Solr analiz zincirinde bileşenlerin doğru sırası nedir?
## A
char filter → tokenizer → token filter
🤖 Doğru: önce ham karakter düzeltilir, sonra token'lara bölünür, sonra token'lar dönüştürülür.
## B
tokenizer → char filter → token filter
🤖 char filter tokenizasyondan ÖNCE çalışmalı; token'ları değil ham karakter akışını düzeltir.
## C
token filter → tokenizer → char filter
🤖 Tamamen ters sıra; token'lar henüz üretilmeden token filter çalışamaz.
## D
tokenizer → token filter → char filter
🤖 char filter en başta ham metin üzerinde çalışır, sona konamaz.
## Doğru
A
## Açıklama
Sıra mantığı akışı takip eder: **char filter** en başta **ham karakter akışını** düzeltir (HTML soyma, karakter eşleme) — henüz token yoktur. Ardından **tokenizer** akışı **token'lara böler** (bir zincirde tam olarak bir tokenizer). Son olarak **token filter'lar** her token'ı sırayla dönüştürür (küçültme, stopword, stemming, synonym). char filter token'ları değil karakterleri, token filter karakterleri değil token'ları işlediği için sıra zorunludur. (B/C/D) bu bağımlılığı bozar: token filter token olmadan, char filter token'lardan sonra çalışamaz.

# analiz-2 | İleri
## Soru
Bir belge alanı index-time'da küçük harfe indiriliyor ama query-time analizinde küçültme yok. `Kredi` araması ne olur?
## A
Sorun olmaz; Solr alan tipini okuyup büyük/küçük harf farkını eşleşme anında kendisi giderir
🤖 Böyle bir telafi katmanı yoktur; eşleşme yalnız analiz edilmiş terimler üzerinden yapılır.
## B
Belge de sorgu da aynı terime indiği için eşleşir; küçültmeyi zaten tokenizer yapmıştır
🤖 Tokenizer yalnızca böler, küçültmez; küçültme ayrı bir token filter'ın işidir.
## C
İndekste `kredi` terimi saklıyken sorguda `Kredi` aranır; terimler birebir tutmadığı için belge bulunamaz
🤖 Doğru: index-time ve query-time analiz uyuşmazsa eşleşme sessizce kaçar.
## D
Query-time analizi index-time'ı geçersiz kılar ve etkilenen belgeler arka planda yeniden indekslenir
🤖 Sorgu analizi yalnızca sorguyu işler; indeksteki belgelere hiç dokunmaz.
## Doğru
C
## Açıklama
Arama, **analiz edilmiş terimlerin birebir eşleşmesidir**. Belge index-time'da `Kredi` → `kredi` olarak saklanır. Query-time'da küçültme yoksa sorgu `Kredi` olarak aranır. İndekste `Kredi` terimi olmadığı için (yalnız `kredi` var) **eşleşme bulunamaz** ve hata da fırlamaz — sonuç sessizce boş döner. Bu, analiz zincirlerinin en klasik hatasıdır: **index-time ve query-time analizinin uyumlu olması** gerekir. (A) Solr'da eşleşme anında harf farkını gideren bir telafi katmanı yoktur. (B) Küçültme tokenizer'ın değil, ayrı bir token filter'ın işidir; tokenizer yalnızca akışı böler. (D) Sorgu analizi yeniden indeksleme tetiklemez, indeksteki belgelere dokunmaz. Çözüm: her iki tarafa da lowercase filter koymak.

# analiz-3 | İleri
## Soru
`İSTANBUL` kelimesi yerelden-bağımsız (locale-insensitive) küçültmeyle işlenirse Türkçe aramada ne sorun çıkar?
## A
Sorun çıkmaz; Unicode küçültme kuralı Türkçe için de aynı sonucu verdiğinden terimler örtüşür
🤖 Yerelden-bağımsız kural `İ` ve `I`'yı Türkçe'nin beklediği gibi çevirmez; sonuçlar örtüşmez.
## B
Kelime indeksten tümüyle düşer, çünkü noktalı büyük harfler tokenizer'da sınır sayılıp atılır
🤖 Tokenizer noktalı harfleri sınır saymaz; kelime silinmez, yalnızca yanlış küçültülür.
## C
Solr analiz sırasında yerel uyuşmazlığını algılar, belgeyi reddedip indeksleme hatası döndürür
🤖 Böyle bir denetim yoktur; hata fırlamaz, sessizce bozuk terim üretilir — asıl sinsi olan budur.
## D
`İ` İngilizce kuralıyla bozuk küçültülür, `istanbul` sorgusu tutmaz; Türkçe'de `İ`→`i`, `I`→`ı` olmalıdır
🤖 Doğru: yerele duyarlı küçültme (TurkishLowerCaseFilter) şarttır.
## Doğru
D
## Açıklama
Türkçe, noktalı/noktasız harfleri ayırır: büyük `İ`'nin küçüğü `i`, büyük `I`'nin küçüğü `ı`'dır. Yerelden-bağımsız (locale-insensitive) küçültme kuralı `İ`'yi doğru çeviremez; sıklıkla ekstra birleşik nokta bırakarak (`i̇`) terimi bozar ve sonuç `istanbul` sorgusuyla **eşleşmez**. Bu sessiz bir hatadır. (A) Unicode'un yerelden bağımsız kuralı Türkçe'nin beklediği çıktıyı vermez, terimler örtüşmez. (B) Tokenizer noktalı büyük harfi kelime sınırı saymaz; kelime indeksten düşmez. (C) Solr'da yerel uyuşmazlığını yakalayan bir denetim yoktur, indeksleme hatası da dönmez. Çözüm: Türkçe alanlarda **TurkishLowerCaseFilter** ve Türkçe kök bulma (TurkishStemmer/snowball) kullanmak; analiz zincirini Türkçe yereline (locale) duyarlı kurmak.

# analiz-4 | Orta
## Soru
Otomatik tamamlama (autocomplete) için **edge-n-gram** filtresi tipik olarak nerede uygulanır?
## A
Yalnız query-time'da; kullanıcının yazdığı önek gram'lara açılır, indekste ham terim durur
🤖 Yön ters: eşleşmenin tutması için gram'lar indeks tarafında üretilmiş olmalıdır.
## B
Yalnız index-time'da; `kredi` → `k, kr, kre, kred, kredi` gram'larına açılır, sorgu ham gelir
🤖 Doğru: gram'lar indekste üretilir, ham önek sorgu tarafında aranır.
## C
Hem index hem query tarafında birebir simetrik olarak; aksi halde Lucene önek eşleşmesi üretemez
🤖 Simetri şart değildir; iki tarafta da gram üretmek kısa gram'ları çarpıştırıp sonucu kirletir.
## D
Hiçbir yerde; autocomplete `LIKE '%kre%'` benzeri desen taramasıyla indeks üzerinde çözülür
🤖 Desen taraması indeks büyüdükçe ölçeklenmez; autocomplete tam da n-gram'ın çözdüğü sorundur.
## Doğru
B
## Açıklama
**Edge-n-gram** bir terimi baştan büyüyen öneklere açar: `kredi` → `k, kr, kre, kred, kredi`. Otomatik tamamlamada bu genellikle **yalnız index-time** yapılır: belge indekslenirken tüm önekler terim olarak saklanır; kullanıcı `kre` yazınca bu ham önek doğrudan `kre` gram'ıyla eşleşir. (A) yön terstir — gram'lar sorguda değil indekste bulunmalıdır, aksi halde eşleşecek terim yoktur. (C) İki tarafın simetrik olması gerekmez; sorgu da parçalanırsa `kre` → `k, kr, kre` olur ve `k` gibi kısa gram'lar alakasız belgelerle eşleşip sonucu kirletir, bu yüzden query analizinde ham önek aranır. (D) Desen taraması indeks büyüdükçe ölçeklenmez; n-gram tam da bu problemin çözümüdür.

# analiz-5 | İleri
## Soru
Eşanlamlı (synonym) genişletmesini **yalnız query-time'da** uygulamanın index-time'a kıyasla avantajı nedir?
## A
Sözlük değişince reindex gerekmez; yeni eşlemeler bir sonraki sorgudan itibaren geçerli olur
🤖 Doğru: genişletme sorgu anında yapıldığı için indeksin yeniden üretilmesi gerekmez.
## B
İndeks boyutunu büyütür ama eşanlamlılar önceden çözüldüğü için sorgu gecikmesini düşürür
🤖 İndeksi büyüten ve eşlemeyi önceden çözen taraf index-time'dır; query-time indekse hiç yazmaz.
## C
Eşanlamlılar indekse kalıcı terim olarak yazılır; sözlükten çıkarsan da indekste kalmayı sürdürür
🤖 Bu index-time genişletmenin davranışıdır; sorgu tarafı indekse terim eklemez.
## D
Çok kelimeli eşanlamlıların konum sorununu ortadan kaldırır; sorgu tarafında pozisyon üretilmez
🤖 Tam tersi: çok kelimeli eşanlamda konum/öbek yönetimi asıl sorgu tarafında zorlaşır.
## Doğru
A
## Açıklama
Eşanlamlıyı **query-time'da** genişletmenin temel kazancı **esnekliktir**: eşanlamlı sözlüğü değiştiğinde (yeni eşleme eklendi/çıkarıldı) tüm belgeleri **yeniden indekslemeye gerek yoktur**; değişiklik bir sonraki sorgudan itibaren geçerli olur. Index-time genişletme ise eşanlamlıları kalıcı olarak indekse yazar (indeksi büyütür) ve sözlük değişince **reindex** gerektirir. (B) İndeksi şişiren ve eşlemeyi peşinen çözen taraf index-time'dır; query-time indekse hiçbir şey yazmaz. (C) Aynı şekilde bu, sorgu tarafının değil index-time genişletmenin davranışıdır. (D) İddia terstir: çok kelimeli eşanlamlıların (multi-word synonym) konum/öbek yönetimi ve skorlamaya etkisi asıl olarak query-time tarafında zorlaşır — esnekliğin bedeli budur.

# analiz-6 | Orta
## Soru
Durak kelime (stopword) filtresi ne yapar ve dikkat edilmesi gereken tuzak nedir?
## A
Yazım hatalarını sözlüğe bakarak düzeltir; tuzağı, öneri üretiminin sorguyu yavaşlatmasıdır
🤖 Yazım düzeltme spellcheck bileşeninin işidir; stopword filtresi kelime düzeltmez.
## B
Kelimeleri köküne indirger; tuzağı, farklı anlamlı kelimelerin aynı köke düşüp karışmasıdır
🤖 Köke indirgeme stemming'in işidir; anlatılan tuzak da aşırı köktenleştirmeye aittir.
## C
`ve, ile` gibi ayırt ediciliği düşük kelimeleri atar; tuzağı iki tarafta tutarsız uygulanmasıdır
🤖 Doğru: indeksi küçültür ama bağlama ve iki tarafın uyumuna duyarlı uygulanmalıdır.
## D
Belirli bir harf sayısının altındaki tüm kelimeleri uzunluk eşiğine göre eler; listeye gerek yoktur
🤖 Ölçüt uzunluk değil kelimenin sıklığı/ayırt ediciliğidir; filtre önceden tanımlı listeye dayanır.
## Doğru
C
## Açıklama
**Stopword** filtresi, dilde çok sık geçip **ayırt ediciliği düşük** kelimeleri (`ve, ile, bir, the, is`) indeksten atar; bu, indeks boyutunu ve gürültüyü azaltır. İki tuzağı vardır: (1) index ve query tarafında **tutarsız** uygulanırsa eşleşme sessizce kaçar; (2) bazı bağlamlarda stopword'ler **anlam taşır** — "to be or not to be" tümüyle stopword'lerden oluşur ve hepsini silmek ifadeyi yok eder; marka ve kişi adlarında da risklidir. Modern skorlama (BM25) sık geçen terimleri zaten düşük ağırlıklandırdığından stopword atmak birçok senaryoda gereksizdir. (A) Yazım düzeltme ayrı bir bileşenin (spellcheck) işidir. (B) Köke indirgeme stemming'e aittir; tarif edilen karışma tuzağı da aşırı köktenleştirmenin sorunudur. (D) Ölçüt kelime **uzunluğu değil**, önceden tanımlı bir **listedir**.
