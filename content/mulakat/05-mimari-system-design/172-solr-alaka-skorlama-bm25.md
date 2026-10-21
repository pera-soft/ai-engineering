---
slug: solr-alaka-skorlama-bm25
baslik: "Solr Alaka Skorlama: TF-IDF → BM25, boosting, LTR + kNN hibrit"
kademe: İleri
sira: 172
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kütüphaneye girip görevliye "kredi kartı borcu" dediğini düşün. Görevli sana raftaki bütün kitapları değil, birkaç kitabı **sırayla** uzatır: önce en çok işine yarayacağını düşündüğünü. Arama motoru da tam olarak bunu yapar.

Zor kısım "hangi kayıtlar eşleşiyor" değildir; gerçek sistemlerde zaten binlerce kayıt eşleşir. Zor kısım **sıralamadır**: hangisi en üste çıkacak? Bu işi yapan mekanizmaya **alaka skorlaması (relevance scoring)** denir. Bu konunun tek sorusu şudur: "Neden bu sonuç en üstte?" — ve cevabı tahmin değil, hesaplanabilir bir sayıdır.

### Temel kavramlar

- **Belge** (document): aramaya konu olan tek kayıt — bir haber yazısı, bir ürün, bir CV.
- **Terim** (term): metnin bölündüğü en küçük arama birimi. "kırmızı koltuk" metni "kırmızı" ve "koltuk" terimlerine ayrılır.
- **İndeks** (index): kitabın arkasındaki dizin gibidir. Her terimin karşısında, o terimin geçtiği belgelerin listesi durur; motor belgeleri tek tek okumaz, dizine bakar.
- **Sorgu** (query): kullanıcının arama kutusuna yazdığı şey.
- **Alaka** (relevance): belgenin kullanıcının derdine gerçekten cevap verip vermediği. Bu bir insan yargısıdır; makine onu doğrudan bilemez, yalnızca **tahmin eder**.
- **Skor** (score): makinenin bu tahmini çevirdiği sayı. Sonuçlar skora göre büyükten küçüğe dizilir. Yani skor, alakanın ölçülebilir vekilidir.

Skorun iki klasik hammaddesi vardır. **TF (term frequency — terim sıklığı)**: terim o belgede kaç kez geçiyor? Çok geçiyorsa belge muhtemelen o konuyla ilgilidir. **IDF (inverse document frequency — ters belge sıklığı)**: terim **koleksiyonun genelinde** ne kadar nadir? "ve" hemen her belgede geçer, hiçbir şeyi ayırt etmez; IDF'i düşüktür. "Kuantum" yalnız birkaç belgede geçer; IDF'i yüksektir. Dikkat: IDF belge içindeki tekrara bakmaz (o TF'nin işi), belgenin uzunluğuna veya tarihine ise hiç bakmaz.

### Nasıl çalışır?

Klasik **TF-IDF** modeli TF ile IDF'i çarpar. Sorunu şudur: TF'nin katkısı **doğrusaldır**. Terimi 100 kez tekrarlayan sayfa, 1 kez geçen sayfadan 100 kata yakın puan alır — "terim tıkıştırma" (keyword stuffing) bu modeli sömürür.

**BM25**, Lucene ve Solr 6+ sürümlerinin **varsayılan** skorlamasıdır ve TF-IDF'i iki noktada düzeltir:

1. **TF doygunluğu** (saturation): tekrarın katkısı doğrusal değil, **doyan bir eğriyle** artar. İlk birkaç tekrar değerlidir, 50. tekrarın katkısı neredeyse sıfırdır. Eğrinin dikliğini **`k1`** parametresi ayarlar. Stuffing böylece kırılır.
2. **Belge uzunluğu normalizasyonu**: uzun bir belgede terimin sık geçmesi doğaldır. BM25 uzunluğu **`b`** parametresiyle (0–1 arası) hesaba katar: `b=1` tam normalizasyon, `b=0` uzunluğu hiç dikkate almaz, tipik varsayılan ~0.75'tir. Böylece kısa ve öz belge haksız yere ezilmez.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="BM25 TF doygunluk eğrisi vs TF-IDF doğrusal">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">TF katkısı: BM25 doygunluğu vs TF-IDF doğrusallığı</text>
  <line x1="70" y1="180" x2="600" y2="180" stroke="#1C3247" stroke-width="1.5"/>
  <line x1="70" y1="180" x2="70" y2="50" stroke="#1C3247" stroke-width="1.5"/>
  <text x="335" y="205" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">terim sıklığı (TF) →</text>
  <text x="52" y="115" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui" transform="rotate(-90 52 115)">skor katkısı →</text>
  <line x1="70" y1="175" x2="595" y2="60" stroke="#B23B3B" stroke-width="2"/>
  <text x="520" y="72" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">TF-IDF (doğrusal)</text>
  <path d="M70 175 Q 160 90 320 78 T 595 70" fill="none" stroke="#25804C" stroke-width="2.5"/>
  <text x="380" y="98" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">BM25 (doygunluk, k1)</text>
  <text x="335" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">BM25: ilk tekrarlar değerli, sonrakiler doyar → stuffing sömürülemez</text>
</svg>
```

Skoru kasıtlı olarak eğmenin adı **öne çıkarmadır (boosting)** ve üç katmanı vardır. **Sorgu zamanı öne çıkarma**: `baslik:kredi^3` yazarsan bu sorguda başlık alanı 3 kat değerli olur. **Alan bazlı öne çıkarma**: eDisMax'in `qf` parametresi. **Fonksiyon ile öne çıkarma**: `bf` ya da `boost`.

Sonuncusu sayısal iş sinyallerini skora katar: yenilik, popülerlik, stok durumu gibi. Örnek: `boost=recip(ms(NOW,tarih),3.16e-11,1,1)` yeni belgeye daha yüksek çarpan verir. Kritik nokta şudur: fonksiyon ile öne çıkarma alaka skorunu **korur** ve üstüne ek ağırlık bindirir. Oysa `sort=tarih desc` yazmak skoru tümüyle devre dışı bırakır; alakasız ama yeni belgeleri en üste taşır.

Peki bir belge neden yukarıda? Tahmin yürütmek yerine sorguya **`debugQuery=true`** eklenir. Solr her belge için skorun **açıklama (explain)** ağacını döndürür. Bu ağaçta şunlar görünür: hangi terim ne kadar TF kattı, IDF'i neydi, uzunluk normalizasyonu skoru ne kadar kırptı, hangi öne çıkarma çarptı. Genel DEBUG kaydı, yeniden indeksleme ya da önbellek temizliği bu bilgiyi vermez. Skorun matematiğini yalnız explain açar.

Sözcük eşleşmesinin bir sınırı vardır: BM25 için "otomobil" ile "araba" bambaşka iki terimdir. **Yoğun getirim (dense retrieval)** bu sınırı aşar. Metin bir **vektöre (embedding)** çevrilir ve **kNN (en yakın komşu)** araması anlamca yakın belgeleri getirir. Solr 9 bunu `DenseVectorField` ve kNN sorgusuyla destekler.

Ama yalnız kNN'e geçmek de hatalıdır: ürün kodu, kimlik numarası ve nadir özel adlar gibi kesin eşleşmeleri kaçırır. Eşanlam dosyası (`synonyms.txt`) ya da daha agresif kök bulma (stemming) yardımcı olur; ama her anlam ilişkisini kapsayamaz.

2026 üretim standardı **hibrit** yaklaşımdır. BM25 ile kNN sonuç listeleri **RRF (Reciprocal Rank Fusion)** gibi bir sıra birleştirmesiyle harmanlanır. Üstüne isteğe bağlı olarak **LTR (Learning to Rank)** ile ilk N sonuç yeniden sıralanır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hibrit arama: BM25 sözcük ve kNN anlam sonuçları RRF ile birleşir">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hibrit arama: sözcük + anlam</text>
  <rect x="30" y="48" width="220" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="140" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">BM25 (sözcük)</text>
  <text x="140" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kesin terim / kod / ID eşleşmesi</text>
  <rect x="30" y="120" width="220" height="58" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="140" y="142" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">kNN (anlam / dense)</text>
  <text x="140" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"otomobil" ≈ "araba" (vektör)</text>
  <line x1="250" y1="77" x2="360" y2="105" stroke="#186399" stroke-width="1.6"/>
  <polygon points="360,105 350,102 352,110" fill="#186399"/>
  <line x1="250" y1="149" x2="360" y2="120" stroke="#6B3FA0" stroke-width="1.6"/>
  <polygon points="360,120 352,116 350,124" fill="#6B3FA0"/>
  <rect x="362" y="86" width="130" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="427" y="108" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">RRF birleştir</text>
  <text x="427" y="127" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">rank füzyonu</text>
  <line x1="492" y1="112" x2="524" y2="112" stroke="#25804C" stroke-width="1.6"/>
  <polygon points="524,112 515,108 515,116" fill="#25804C"/>
  <rect x="526" y="86" width="94" height="52" rx="10" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="573" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">tek sıralı</text>
  <text x="573" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">sonuç</text>
</svg>
```

Nihai skor tek bir sayı gibi görünür; ama katmanlıdır. Altta BM25 alakası durur. Üstüne alan ve fonksiyon öne çıkarmaları çarpılır. Hibrit kurulumda buna bir de kNN sıralaması karışır.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Skor bileşenleri: BM25 alaka üzerine boost sinyalleri çarpılır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Nihai skor = alaka × boost sinyalleri</text>
  <rect x="30" y="60" width="150" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="105" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">BM25 alaka</text>
  <text x="105" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">TF · IDF · uzunluk</text>
  <text x="200" y="96" text-anchor="middle" font-size="16" font-weight="700" fill="#1C3247" font-family="system-ui">×</text>
  <rect x="220" y="44" width="180" height="42" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="310" y="70" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">qf: baslik^3 (alan boost)</text>
  <rect x="220" y="94" width="180" height="42" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="310" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">bf: recip(yenilik) fonk.</text>
  <line x1="400" y1="90" x2="452" y2="90" stroke="#1C3247" stroke-width="1.6"/>
  <polygon points="452,90 443,86 443,94" fill="#1C3247"/>
  <rect x="454" y="64" width="160" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="534" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">nihai sıralama</text>
  <text x="534" y="105" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">alaka + iş sinyali</text>
  <text x="320" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">debugQuery=true → bu bileşenleri explain ağacıyla parçalar</text>
</svg>
```

### Mülakatta ne sorulur?

- "BM25 ile TF-IDF farkı nedir?" — İki maddeyi say: TF doygunluğu (`k1`) ve belge uzunluğu normalizasyonu (`b`). "IDF'i kaldırır" demek klasik tuzaktır; BM25 IDF'i korur, yalnız TF davranışını değiştirir.
- "IDF tam olarak neyi ölçer?" — Koleksiyon genelinde **nadirliği** ölçer. Belge içi tekrar TF'nin işidir; uzunluk `b`'nin işidir; tazelik ise öne çıkarmanın işidir. Hepsini IDF'e yıkmak en sık yapılan hatadır.
- "`k1` ile `b` ne yapar?" — Sürekli karıştırılırlar: `k1` doyum eğrisinin dikliği, `b` uzunluk normalizasyonunun gücüdür. Ne biri ne öteki IDF'i ölçekler ya da önbellekle ilgilidir.
- "Bu belge neden 3. sırada?" — Cevap tek satırdır: `debugQuery=true` → explain ağacı. "Loglara bakarım" demek pratikte eleyici bir cevaptır.
- "Yeni içerik öne çıksın ama alaka bozulmasın" — Fonksiyon boost (`bf`/`boost`, `recip(ms(NOW,tarih),...)`). Sorunu `sort` ile çözmeye kalkmak alakayı tümden yok eder.
- "Eşanlam sorununu nasıl çözersin?" — `synonyms.txt` ve stemming yardımcıdır ama ölçeklenmez; ölçekli cevap hibrit BM25 + kNN ve RRF, gerekirse üstüne LTR'dir.

### Tek cümlede

BM25, terim sıklığını doyurarak (`k1`) ve belge uzunluğunu normalize ederek (`b`) alakayı bir skora çevirir. Sen de o skoru öne çıkarmalarla eğersin, `debugQuery` ile açıklarsın ve kNN + RRF hibridiyle anlam boyutuna taşırsın.

# bm25-1 | İleri
## Soru
BM25'in TF-IDF'e göre en önemli iyileştirmesi nedir?
## A
IDF'i skordan çıkarıp ağırlığı tümüyle terim sıklığına verir, böylece nadir terim yanlılığını giderir
🤖 BM25 IDF'i kaldırmaz; onu korur ve asıl olarak TF'nin davranışını değiştirir.
## B
Skorlamayı büyük/küçük harf ve ek ayrımına duyarlı kılarak eşleşme hassasiyetini yükseltir
🤖 Harf ve ek işleme analiz zincirinin (tokenizer/filter) işidir, skorlama modelinin değil.
## C
Terimin belge içindeki konumunu ödüllendirir: başta geçen terim sonda geçenden yüksek puan alır
🤖 Konum bilgisi öbek sorgusunda kullanılır; BM25 formülünde konum terimi yoktur.
## D
Terim sıklığının katkısını doyum eğrisiyle sınırlar (`k1`) ve belge uzunluğunu normalize eder (`b`)
🤖 Doğru: TF doygunluğu ile uzunluk normalizasyonu BM25'in özüdür.
## Doğru
D
## Açıklama
Klasik **TF-IDF**'te terim sıklığının katkısı **doğrusaldır**: bir terimi 100 kez tekrarlamak skoru orantılı şişirir; bu, sayfaya terim tıkıştıran (keyword stuffing) içeriği haksızca öne çıkarır. **BM25** bunu bir **doyum eğrisiyle** (saturation) düzeltir: ilk birkaç tekrar değerlidir, sonraki tekrarların katkısı hızla azalır — eğrinin dikliğini **`k1`** ayarlar. Ayrıca BM25 belge uzunluğunu **`b`** parametresiyle normalize eder (tipik ~0.75): uzun belgede terimin sık geçmesi doğaldır, BM25 kısa-öz belgeleri cezalandırmaz. Böylece hem stuffing sömürüsü hem uzun belge yanlılığı kırılır. BM25 IDF'i **korur**, kaldırmaz (A yanlış). Büyük/küçük harf ve ek işleme skorlama modelinin değil, analiz zincirinin konusudur (B). Terimin belge içindeki **konumu** BM25 formülünde yer almaz; konum yalnız öbek (phrase) sorgularında kullanılır (C). Bu iki iyileştirme sayesinde BM25, Solr 6+ ve modern Lucene'de **varsayılan** skorlamadır.

# bm25-2 | Orta
## Soru
IDF (inverse document frequency) skorlamada neyi ödüllendirir?
## A
Koleksiyon genelinde nadir geçen terimleri; nadir terim daha ayırt edici olduğu için yüksek ağırlık alır
🤖 Doğru: IDF nadirliği ödüllendirir, terimin ayırt edicilik ölçüsüdür.
## B
Belgede en çok tekrarlanan terimi; tekrar sayısı arttıkça o terimin ağırlığı da orantılı olarak büyür
🤖 Belge içi tekrar TF'nin ölçtüğü şeydir; IDF koleksiyon geneli dağılıma bakar.
## C
Uzun belgeleri; daha fazla terim barındırdıkları için sorguyu karşılama olasılıkları yüksek sayılır
🤖 Uzunluk IDF'in konusu değildir; BM25'te `b` parametresiyle normalize edilir.
## D
İndekse yeni eklenmiş belgeleri; tazelik katsayısı sayesinde sıralamada öne geçerler
🤖 Yenilik ancak bir fonksiyon boost sinyalidir; IDF formülünde tarih hiç yer almaz.
## Doğru
A
## Açıklama
**IDF** (ters belge sıklığı), bir terimin **tüm koleksiyondaki nadirliğini** ölçer: her belgede geçen "ve", "bir" gibi terimler düşük IDF (düşük ayırt edicilik) alırken, yalnız birkaç belgede geçen "kuantum" gibi terimler yüksek IDF alır. Mantık şudur: nadir terim, bir belgeyi diğerlerinden **ayırt etmede** daha bilgilendiricidir, dolayısıyla eşleşmesi daha değerlidir. IDF belge-içi tekrara bakmaz — o **TF**'nin işidir (B). Belge uzunluğu da IDF'in konusu değildir; onu BM25'te ayrı bir parametre, **`b`**, normalize eder (C). Belgenin tarihi/tazeliği ise skora ancak bir **fonksiyon boost** ile katılır, IDF formülünde tarih diye bir terim yoktur (D). TF (belgede ne kadar sık) ile IDF (koleksiyonda ne kadar nadir) birlikte çarpılarak temel alaka sinyalini üretir.

# bm25-3 | İleri
## Soru
Bir belgenin neden belirli bir sırada geldiğini teşhis etmek için Solr'da hangi araç kullanılır?
## A
Solr log seviyesini DEBUG'a çekmek; sorgu çalışırken skorun her bileşeni log dosyasına yazılır
🤖 Genel log sorgu akışını gösterir, belge başına skor bileşenlerini dökmez.
## B
Koleksiyonu yeniden indekslemek; skor belgeye indeksleme anında yazıldığı için böylece tazelenir
🤖 Skor sorgu anında hesaplanır, belgeye yazılmaz; reindex hiçbir şey açıklamaz.
## C
`debugQuery=true`; her belge için skorun explain ağacını bileşenlerine ayırır
🤖 Doğru: debugQuery/explain skorun matematiğini adım adım gösterir.
## D
`filterCache`i temizlemek; önbellekteki eski skorlar atılınca gerçek sıralama ortaya çıkar
🤖 `filterCache` filtre bit kümelerini tutar, skor saklamaz; temizlik teşhis vermez.
## Doğru
C
## Açıklama
**`debugQuery=true`** parametresi, sonuçtaki her belge için skorun **explain** ağacını döndürür: hangi terimin TF katkısı, IDF katkısı, alan/sorgu boost'u ne kadardı, uzunluk normalizasyonu skoru nasıl etkiledi — skorun tüm matematiği adım adım açılır. Bu, "neden A belgesi B'den yukarıda?" sorusunun **tek güvenilir cevabıdır**. Genel DEBUG log (A) sorgu akışını gösterir, belge başına skor bileşenlerini dökmez. Skor **indekste saklanmaz**, sorgu anında hesaplanır; bu yüzden yeniden indeksleme (B) skoru açıklamaz. `filterCache` (D) filtre sonuçlarının bit kümelerini tutar, skor tutmaz — temizlemek teşhis üretmez. eDisMax ile birlikte `debugQuery`, hangi alanın eşleştiğini ve boost'ların skora nasıl bindiğini de gösterir.

# bm25-4 | İleri
## Soru
"Yeni belgeleri sonuçlarda yukarı çekmek" (ama alaka eşleşmesini de korumak) için doğru mekanizma nedir?
## A
`sort=tarih desc` ile sonuçları tarihe göre sabit sıralamak; skor yalnız eşleşme filtresi olarak kalır
🤖 Bu, alaka sıralamasını tümden devre dışı bırakır; alakasız yeni belge en üste çıkar.
## B
`bf` / `boost` fonksiyon sorgusuyla yeniliği skora sinyal katmak (ör. `recip(ms(NOW,tarih),...)`)
🤖 Doğru: fonksiyon boost alakayı korur, üstüne yenilik ağırlığı bindirir.
## C
Belirli bir tarihten eski belgeleri indeksten silmek; kalan küçük indekste yeniler kendiliğinden öne çıkar
🤖 Bu sıralama ayarı değil veri kaybıdır; hâlâ eski kayıtlar aranabilir olmalıdır.
## D
Skorlama modelini TF-IDF'ten BM25'e çevirmek; BM25 belge tazeliğini normalizasyona dâhil eder
🤖 BM25 formülünde tarih diye bir bileşen yoktur; model değişimi yeniliği yukarı çekmez.
## Doğru
B
## Açıklama
Amaç iki sinyali **birleştirmektir**: metin **alakası** (BM25 skoru) ve **yenilik**. Doğru araç **fonksiyon sorgusu boost**'udur — eDisMax'te `boost` ya da `bf` ile bir fonksiyon skoru katılır, ör. `recip(ms(NOW,tarih), 3.16e-11, 1, 1)` yeni belgeye daha yüksek çarpan verir. Böylece alaka skoru **korunur**, yenilik onun üzerine ek ağırlık bindirir; alakasız ama yeni belge yine de yukarı fırlamaz. `sort=tarih desc` (A) skoru sıralamadan tamamen çıkarır ve "eşleşmeyi de koru" gereksinimini karşılamaz. Eski belgeleri silmek (C) bir sıralama çözümü değil, geri alınamaz veri kaybıdır. Skorlama modelini değiştirmek (D) de işe yaramaz: BM25 formülü TF, IDF ve belge uzunluğundan oluşur, içinde tarih/tazelik bileşeni yoktur. Fonksiyon boost, "alaka + iş sinyali" (popülerlik, stok, marj) harmanlamanın standart yoludur.

# bm25-5 | Uzman
## Soru
Kullanıcı "otomobil" arıyor ama ilgili belgeler "araba" kelimesini kullanıyor. BM25 tek başına bunları eşleştiremez. 2026 üretiminde en güçlü yaklaşım nedir?
## A
Bütün eşanlam ilişkilerini elle `synonyms.txt`e yazıp sorgu zamanında genişletmek; başka yola gerek yok
🤖 Sözlük bakımı ölçeklenmez ve dile bağlı her anlam yakınlığını asla kapsayamaz.
## B
BM25'i tamamen bırakıp yalnız kNN vektör aramasına geçmek; anlam yakınlığı her ihtiyacı karşılar
🤖 Yalnız kNN ürün kodu, ID ve nadir özel ad gibi kesin eşleşmeleri kaçırır.
## C
Stemming'i agresifleştirip daha saldırgan bir kök bulucu takmak; kökler birleşince eşanlamlar da eşleşir
🤖 Kök bulma yalnız aynı sözcüğün çekimlerini birleştirir; farklı kökleri yakınlaştıramaz.
## D
Hibrit arama: BM25 ile dense retrieval/kNN sonuçlarını RRF ile birleştirmek (Solr 9 `DenseVectorField`)
🤖 Doğru: sözcük + anlam hibriti 2026 üretim standardıdır.
## Doğru
D
## Açıklama
BM25 **sözcük eşleşmesine** dayanır: "otomobil" ile "araba" farklı token'lardır, eşleşmezler. **Dense retrieval** bunu çözer: metin bir **embedding vektörüne** çevrilir ve **kNN** (en yakın komşu) araması anlamca yakın belgeleri bulur — "otomobil" ve "araba" vektör uzayında yakındır. Elle eşanlam sözlüğü (A) işe yarar ama bakımı ölçeklenmez; her yeni ürün, argo ya da yazım varyantı için satır eklemek gerekir. Yalnız kNN'e geçmek (B) de risklidir: kesin sözcük, ürün kodu, ID, nadir özel ad eşleşmelerini kaçırır. Stemming'i agresifleştirmek (C) yalnız **aynı kökün** çekimlerini birleştirir ("arabalar" → "araba"); "otomobil" ile "araba" farklı köklerdir, ayrıca aşırı kök bulma alakasız sözcükleri de çakıştırıp hassasiyeti düşürür. Bu yüzden 2026 üretim standardı **hibrit**: BM25 ve kNN sonuç listelerini **RRF** (Reciprocal Rank Fusion) ile birleştirmek. Solr 9 `DenseVectorField` ve kNN sorgusunu yerel destekler; üstüne isteğe bağlı **LTR** yeniden sıralama en güçlü kombinasyondur.

# bm25-6 | İleri
## Soru
BM25'in `b` parametresi neyi kontrol eder?
## A
Belge uzunluğu normalizasyonunun gücünü; `b=1` tam normalizasyon, `b=0` uzunluğu hiç dikkate almaz
🤖 Doğru: `b`, 0–1 arası uzunluk normalizasyon katsayısıdır (tipik ~0.75).
## B
Terim sıklığının doyum eğrisinin dikliğini; kaçıncı tekrardan sonra katkının düzleşeceğini belirler
🤖 Bu tarif `k1`'e aittir; `b` doyum eğrisinin şekliyle ilgilenmez.
## C
IDF'in skor içindeki ağırlığını; büyüdükçe nadir terimler sıralamada daha baskın hale gelir
🤖 `b` IDF'i ölçeklemez; BM25'te IDF'in ayrı bir ağırlık parametresi yoktur.
## D
Sorgu başına taranacak belge tamponunun boyutunu; büyütmek skoru kararlı ama sorguyu yavaş yapar
🤖 `b` bir skorlama katsayısıdır; tampon/önbellek boyutuyla hiçbir ilgisi yoktur.
## Doğru
A
## Açıklama
BM25'in iki ayar parametresi vardır: **`k1`** ve **`b`**. **`b`**, **belge uzunluğu normalizasyonunun gücünü** kontrol eder (0 ile 1 arası): `b=1` uzunluğu tam olarak hesaba katar, yani uzun belgelerdeki yüksek TF'yi güçlü şekilde kırpar; `b=0` uzunluğu tamamen yok sayar; tipik varsayılan ~0.75'tir. **`k1`** ise bambaşka bir işi yapar — TF **doyum eğrisinin dikliğini**, yani terim tekrarının ne kadar hızlı doyacağını ayarlar; B şıkkı tam olarak `k1`'i tarif eder. `b` IDF'i ölçeklemez (C); BM25'te IDF için ayrı bir ağırlık katsayısı zaten yoktur, IDF terimin koleksiyondaki nadirliğinden doğrudan hesaplanır. `b` bir performans/kapasite ayarı da değildir: tampon veya önbellek boyutuyla ilgisi yoktur (D). Bu iki parametre, koleksiyonun karakterine göre (kısa ürün başlıkları mı, uzun makaleler mi) ince ayarlanarak alaka kalitesi optimize edilir.
