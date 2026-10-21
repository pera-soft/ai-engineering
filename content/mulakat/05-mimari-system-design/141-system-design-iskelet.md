---
slug: system-design-iskelet
baslik: "System Design Mülakatı: 5 Adımlı İskelet, Gereksinim Netleştirme ve Trade-off"
kademe: İleri
sira: 141
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

System design mülakatında sana bir cümle verilir — "bir URL kısaltma servisi tasarla", "bir sohbet uygulaması tasarla" — ve 45 dakikan olur. Soru **kasıtlı olarak açık uçludur**; tek bir doğru cevabı yoktur. Ölçülen şey ezberlediğin mimari değildir. İki beceri ölçülür: **belirsiz bir problemi düzenli bir sırayla ele alabilmek** ve her kararı bedelini görerek gerekçelendirebilmek.

Bu yüzden hazırlık, hazır çözüm ezberlemek değil, her soruda tekrar edebileceğin bir **iskelet** edinmektir. İskelet üç işi birden yapar. Zamanı dengeli böler, hiçbir kritik adımı atlamanı engeller ve mülakatçıyla aynı noktada kalmanı sağlar. Aynı iskelet gerçek işte de çalışır; mülakat, günlük mühendislik akıl yürütmesinin sıkıştırılmış hâlidir.

### Temel kavramlar

- **Gereksinim (requirement):** sistemden beklenen, doğrulanabilir talep.
- **Fonksiyonel gereksinim:** sistemin *ne yaptığı* — "kısa URL üret ve yönlendir", "gönderi paylaş, beğen".
- **Non-fonksiyonel gereksinim (mimari nitelik):** sistemin *nasıl olması gerektiği* — p99 **gecikmesi (latency)** 200 ms altında, %99.9 erişilebilirlik, ölçek, tutarlılık, maliyet. Mimariyi asıl belirleyen bunlardır. (p99: isteklerin %99'unun altında kaldığı süre.)
- **API (uygulama programlama arayüzü):** sistemin dışarıya sunduğu işlemlerin sözleşmesi; kimin ne göndereceğini ve ne alacağını sabitler (`POST /links {uzunUrl} → {kisaKod}`).
- **Veri modeli:** hangi varlığın hangi alanlarla, nasıl saklanacağı ve hangi erişim desenine göre indeksleneceği.
- **Derinleşme (deep dive):** seçilmiş tek bir bileşenin iç tasarımına inmek.
- **Trade-off (ödünleşme):** bir şeyi kazanırken başka bir şeyden vazgeçmek; her mimari kararın kaçınılmaz yüzü.

```svg
<svg viewBox="0 0 640 150" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="System design 5 adımlı iskelet akışı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">5 adımlı iskelet: çözüme atlamadan önce</text>
  <rect x="20" y="50" width="110" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="75" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1. Gereksinim</text>
  <text x="75" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">netleştir</text>
  <rect x="148" y="50" width="110" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="203" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2. Kapasite</text>
  <text x="203" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">+ gereksinim türleri</text>
  <rect x="276" y="50" width="110" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="331" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3. Yüksek</text>
  <text x="331" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">seviye tasarım</text>
  <rect x="404" y="50" width="110" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="459" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">4. Deep dive</text>
  <text x="459" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">bileşen detay</text>
  <rect x="532" y="50" width="96" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="580" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">5. Trade-off</text>
  <text x="580" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">& ölçekleme</text>
  <text x="139" y="84" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <text x="267" y="84" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <text x="395" y="84" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <text x="523" y="84" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
</svg>
```

### Nasıl çalışır?

**1. Gereksinimleri netleştir (yaklaşık 5 dk).** İskeletin en kritik adımıdır ve en sık atlanandır. Soru sor: "Günde kaç istek? Okuma mı yazma mı ağırlıklı? Hangi özellik kapsamda, hangisi değil? Tutarlılık mı gecikme mi öncelikli?" Cevaplanamayan noktalarda varsayımı **sesli** yap: "günde 100 milyon yazma varsayıyorum, uygun mu?" Soru sormak zayıflık değil, olgunluk işaretidir. Fonksiyonel ve non-fonksiyonel gereksinimleri de burada ayırırsın.

**2. Kapasiteyi tahmin et (yaklaşık 5 dk).** Kaba hesapla saniyedeki istek sayısı (QPS), depolama ve bant genişliği çıkarılır. Amaç ondalık kesinlik değil, doğru **büyüklük mertebesidir**: 100 mü, 100 bin mi? Çıkan sayı bir sonraki adımın tamamını belirler — tek sunucu mu yeter, önbellek mi gerekir, veriyi parçalara bölmek (sharding) şart mı?

**3. Yüksek seviye tasarım (10-15 dk).** Bu adımın kendi iç sırası vardır. Önce **API sözleşmesi** gelir: sistem dışarıya neyi vaat ediyor? Sonra bu çağrıları besleyecek **veri modeli** çıkar: hangi varlık, hangi indeks? En son ana bileşenleri ve aralarındaki akışı gösteren **kutu-ok diyagramını** çizersin. API'yi önce sabitlemek, tasarımın geri kalanını somut bir sözleşmeye bağlar.

**4. Deep dive (10-15 dk).** Mülakatçı bir bileşeni ya da darboğazı seçer ("benzersiz kısa anahtarı nasıl üretirsin?"). Doğru ele alış, tek bir cevap vermek değil **alternatifleri karşılaştırmaktır**. Şu seçenekler masaya konur: özet fonksiyonu (hash) + çakışma kontrolü, önceden üretilmiş anahtar havuzu, base62 ile kodlanmış artan id, Snowflake benzeri dağıtık id. Her birini dört eksende tart: çarpışma riski, dışarıdan tahmin edilebilirlik, sunucular arası koordinasyon ihtiyacı ve ölçeğe etkisi.

**5. Ödünleşmeler ve ölçekleme (yaklaşık 5 dk).** Darboğazları, tek hata noktalarını ve büyüme planını özetler, kararların bedelini açıkça söylersin.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fonksiyonel ne yapar, non-fonksiyonel nasıl olur">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fonksiyonel 'ne', non-fonksiyonel 'nasıl'</text>
  <rect x="40" y="46" width="250" height="134" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Fonksiyonel: ne yapar</text>
  <text x="165" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kısa URL üret, yönlendir</text>
  <text x="165" y="116" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gönderi paylaş, beğen</text>
  <text x="165" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">davranışlar / özellikler</text>
  <rect x="350" y="46" width="250" height="134" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Non-fonksiyonel: nasıl</text>
  <text x="475" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">p99 &lt; 200 ms gecikme</text>
  <text x="475" y="116" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">%99.9 erişilebilirlik, ölçek</text>
  <text x="475" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">mimariyi asıl bunlar belirler</text>
  <text x="475" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">nitelikler / -ility</text>
</svg>
```

### Mülakatta ne sorulur?

Değerlendirme büyük ölçüde **hata kalıpları** üzerinden yapılır. En pahalı hata **çözüme atlamaktır**: daha ne istendiği belli değilken şema çizmek, bulut sağlayıcı seçmek veya kod yazmak. Sık görülen diğer hatalar şunlardır. Ezberlenmiş bir referans mimariyi soruyu dinlemeden anlatmak. Gerekçe vermeden teknoloji adı saymak. Tek bir bileşende boğulup genel resmi hiç çizmemek.

En güçlü olumlu sinyal ise bir kararı **iki yönüyle** sunmaktır. Örnek bir cümle şöyle kurulur: "Yüksek yazma hacmi ve esnek şema için NoSQL seçiyorum. **Karşılığında** güçlü tutarlılıktan ve çok tablolu join kolaylığından ödün veriyorum. Bu sistemde okuma erişilebilirliği tutarlılıktan öncelikli olduğu için bu ödünü kabul edilebilir buluyorum." Mülakatçı çoğu zaman kararın kendisiyle değil, **arkasındaki muhakemeyle** ilgilenir. Bu yüzden "popüler olduğu için" ya da "her zaman daha iyi" gibi mutlak ifadeler zayıf sayılır. Gereksinime bağlanmış ve bedeli açıkça söylenmiş kararlar ise güçlü sayılır.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Trade-off: bir kararın her iki yönünü sun">
  <defs>
    <marker id="sd-arrow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Olgunluk sinyali: kararı iki yönüyle sun</text>
  <rect x="200" y="42" width="240" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="62" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Karar: NoSQL seçtim</text>
  <text x="320" y="79" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çünkü yüksek yazma + esnek şema</text>
  <line x1="280" y1="86" x2="185" y2="120" stroke="#5D7789" stroke-width="2" marker-end="url(#sd-arrow)"/>
  <line x1="360" y1="86" x2="455" y2="120" stroke="#5D7789" stroke-width="2" marker-end="url(#sd-arrow)"/>
  <rect x="40" y="122" width="240" height="54" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="160" y="144" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kazanç</text>
  <text x="160" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yatay ölçek, yazma hacmi</text>
  <rect x="360" y="122" width="240" height="54" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="480" y="144" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Ödün (bedel)</text>
  <text x="480" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">güçlü tutarlılık, join / ACID</text>
</svg>
```

### Tek cümlede

System design mülakatı ezberlenmiş bir çözümü değil, izlediğin süreci ölçer. Sıra şudur: gereksinimi soruyla netleştir, kapasiteyi tahmin et, API ve veri modeliyle yüksek seviye tasarımı kur, seçilen bileşende alternatifleri karşılaştırarak derinleş ve her kararı bedeliyle birlikte sun.

# sd-1 | İleri
## Soru
Bir system design mülakatında en sağlam genel yaklaşım hangisidir?
## A
Belirsiz problemi tutarlı bir iskelete oturtup adım adım ve dengeli biçimde ilerlemek
🤖 Doğru: değerlendirilen şey yapılandırılmış düşünme ve gerekçeli ödünleşmelerdir.
## B
Ezberlediğin bir referans mimariyi, soruyu hiç dinlemeden baştan sona anlatmak
🤖 Ezber çözüm probleme uymayabilir; mülakat süreci ve uyarlamayı ölçer, ezberi değil.
## C
Bildiğin en fazla teknoloji adını sayarak bilgi genişliğini göstermeye çalışmak
🤖 Terim yağmuru derinlik göstermez; gerekçesiz araç listesi zayıf bir sinyaldir.
## D
Tek bir bileşeni sürenin tamamında detaylandırıp genel resmi hiç çizmemek
🤖 Dengesiz derinlik; önce genel iskelet, sonra seçilmiş bir deep dive gelir.
## Doğru
A
## Açıklama
System design mülakatı **açık uçludur**; tek doğru cevap yoktur, değerlendirilen şey belirsizliği **yapılandırılmış** biçimde yönetme ve gerekçeli **ödünleşme** yapma becerisidir. Sağlam yaklaşım tutarlı bir iskeleti izlemektir: gereksinimi netleştir → kapasiteyi tahmin et → API, veri modeli ve yüksek seviye tasarımı kur → deep dive → ödünleşme ve ölçekleme özeti. Ezberlenmiş mimariyi soruyu dinlemeden anlatmak (B) probleme uymaz ve uyarlama becerisini gizler; teknoloji adı saymak (C) derinlik yerine gürültü üretir; tek bileşende boğulup genel resmi çizmemek (D) dengesizdir. İskelet, hem adayın hem mülakatçının aynı sayfada kalmasını ve zamanın dengeli kullanılmasını sağlar.

# sd-2 | İleri
## Soru
Mülakatçı "bir URL kısaltma servisi tasarla" dedi. İlk yapman gereken nedir?
## A
Hemen veritabanı şemasını ve tabloların alanlarını kağıda çizmeye başlamak
🤖 Şema erken bir karar; kapsam ve ölçek netleşmeden şema çizmek körlemesine tasarımdır.
## B
Kapsamı ve ölçeği netleştiren sorular sormak: trafik, okuma/yazma, tutarlılık
🤖 Doğru: ilk adım daima gereksinim netleştirmedir; belirsizliği soruyla daralt.
## C
Doğrudan hangi bulut sağlayıcıyı ve hangi sunucu tipini seçeceğini anlatmak
🤖 Altyapı seçimi çok erken; önce gereksinim ve ölçek belirlenmelidir.
## D
Uç noktanın kodunu yazmaya başlamak; en somut ilerleme çalışan koddur
🤖 System design kod yazma değildir; önce gereksinim ve yüksek seviye tasarım gelir.
## Doğru
B
## Açıklama
Açık uçlu bir istemde **ilk adım her zaman gereksinim netleştirmedir**: kapsamı ve ölçeği daraltan sorular sormak. "Günde kaç istek? Okuma mı yazma mı ağırlıklı — kısaltmada okuma çok baskındır. Özel (vanity) URL, son kullanma tarihi, analitik kapsamda mı? Tutarlılık mı gecikme mi öncelikli?" Bu sorular hem yanlış varsayımları önler hem de olgunluk gösterir; cevaplanamayan yerde varsayımı **sesli** yaparsın ("günde 100 milyon yazma varsayıyorum"). Doğrudan şemaya (A), altyapıya (C) veya koda (D) atlamak, henüz tanımlanmamış bir problemi çözmeye çalışmaktır ve mülakattaki en sık, en pahalı hatadır.

# sd-3 | İleri
## Soru
Bir haber besleme (news feed) sistemi için aşağıdakilerden hangisi **non-fonksiyonel** gereksinimdir?
## A
Kullanıcı gönderi paylaşabilmeli ve paylaştığı gönderiyi silebilmeli
🤖 Bu sistemin ne yaptığını söyler; bir davranıştır, fonksiyonel gereksinimdir.
## B
Kullanıcı takip ettiği kişilerin gönderilerini akışında görebilmeli
🤖 Bu da bir davranış/özellik; fonksiyonel gereksinimdir.
## C
Feed p99 gecikmesi 200 ms altında ve sistem %99.9 erişilebilir olmalı
🤖 Doğru: gecikme ve erişilebilirlik "nasıl" nitelikleridir, non-fonksiyoneldir.
## D
Kullanıcı bir gönderiyi beğenebilmeli ve beğenisini geri alabilmeli
🤖 Bir özellik/davranış; fonksiyonel gereksinimdir.
## Doğru
C
## Açıklama
**Fonksiyonel gereksinim** sistemin *ne yaptığını* tanımlar — davranışlar ve özellikler: gönderi paylaşma (A), takip edilenleri görme (B), beğenme (D). **Non-fonksiyonel gereksinim** ise sistemin *nasıl* olması gerektiğini tanımlar; bunlar mimari niteliklerdir: **gecikme** (p99 < 200 ms), **erişilebilirlik** (%99.9), ölçeklenebilirlik, tutarlılık, dayanıklılık (C). Mülakatta bu ayrım kritiktir, çünkü **mimariyi asıl belirleyen non-fonksiyonellerdir**: aynı fonksiyonel gereksinim ("feed göster") 1.000 kullanıcı için tek sunucuyla, 1 milyar kullanıcı ve 200 ms hedefiyle ise önbellek, fan-out, sharding ve CDN içeren bambaşka bir mimariyle karşılanır. İkisini erken ayırmak tasarımı doğru ölçeğe oturtur.

# sd-4 | İleri
## Soru
Gereksinimleri netleştirdin ve kapasite tahminini yaptın. İskelette bunu izleyen doğal adım nedir?
## A
Mülakatçının henüz seçmediği bir bileşeni kendi başına derinlemesine detaylandırmak
🤖 Deep dive genel resimden sonra ve genellikle mülakatçının seçtiği bileşen üzerinde yapılır.
## B
Doğrudan darboğaz ve ölçekleme tartışmasına geçip ödünleşme özetini vermek
🤖 Bu son adımdır; ortada henüz ölçeklenecek somut bir tasarım yoktur.
## C
Bulut sağlayıcıyı, sunucu tipini ve dağıtım aracını seçip altyapıyı sabitlemek
🤖 Altyapı seçimi tasarımın sonucudur; mimari kararı belirleyen o değildir.
## D
API sözleşmesini ve veri modelini çıkarıp yüksek seviye kutu-ok diyagramını çizmek
🤖 Doğru: üçüncü adım yüksek seviye tasarımdır — API, veri modeli ve ana bileşenler.
## Doğru
D
## Açıklama
İskeletin üçüncü adımı **yüksek seviye tasarımdır** ve kendi içinde bir sırası vardır: önce **API sözleşmesi** — sistem dışarıya hangi işlemleri, hangi girdi ve çıktıyla vaat ediyor (`POST /links {uzunUrl} → {kisaKod}`, `GET /{kisaKod} → 301`); sonra bu çağrıları besleyecek **veri modeli** — hangi varlık, hangi alan, hangi erişim desenine göre indeks; sonra ana bileşenleri ve aralarındaki akışı gösteren **kutu-ok diyagramı**. API'yi önce sabitlemek tasarımın geri kalanını somut bir sözleşmeye bağlar ve "neyi ölçekliyoruz" sorusunu netleştirir. Genel resim çizilmeden bileşen derinliğine dalmak (A) ya da ölçekleme tartışmasına geçmek (B) sırayı bozar; altyapı ve sağlayıcı seçimi (C) mimari tasarımın sonucudur, başlangıcı değil.

# sd-5 | İleri
## Soru
Tasarımında veri deposu için SQL yerine NoSQL seçtin. Bu kararı mülakatçıya sunmanın en güçlü yolu nedir?
## A
"NoSQL bugün en modern ve en popüler veri deposu olduğu için onu seçtim" demek
🤖 Popülerlik gerekçe değildir; karar iş gereksinimine ve ödünleşmeye dayanmalıdır.
## B
"NoSQL her koşulda SQL'den daha ölçeklenebilir ve her açıdan daha iyidir" demek
🤖 Mutlak iddia yanlış; her bağlamda üstün olan bir veri deposu yoktur.
## C
Kararı gereksinime bağlamak ve bedelini söylemek: ne kazandırıyor, ne kaybettiriyor
🤖 Doğru: kararı gereksinime bağla ve ödünleşmeyi her iki yönüyle sun.
## D
Kararı hiç gerekçelendirmeden bırakıp doğrudan bir sonraki bileşene geçmek
🤖 Gerekçesiz karar en zayıf sinyaldir; mülakatçı asıl "neden"i görmek ister.
## Doğru
C
## Açıklama
Bir teknoloji kararını sunmanın en güçlü yolu, onu **gereksinime bağlamak** ve **ödünleşmesini her iki yönüyle** söylemektir: "Yüksek yazma hacmi, yatay ölçekleme ihtiyacı ve evrilen şema için NoSQL seçiyorum; **karşılığında** güçlü tutarlılıktan (çoğu NoSQL'de nihai tutarlılık), çok tablolu join ve ACID transaction kolaylığından **ödün veriyorum** — bu sistemde okuma erişilebilirliği tutarlılıktan öncelikli olduğu için kabul edilebilir." Bu, mühendislik olgunluğunun en güçlü sinyalidir, çünkü hiçbir seçimin bedelsiz olmadığını gösterir. "Popüler olduğu için" (A) veya "her zaman daha iyi" (B) gibi mutlak ifadeler ve sessiz geçiş (D) zayıf sinyallerdir; mülakatçı kararın kendisinden çok **muhakemesini** değerlendirir.

# sd-6 | İleri
## Soru
Mülakatçı yüksek seviye tasarımdan sonra "URL kısaltmada benzersiz kısa anahtarı nasıl üretirsin?" diye sordu. Bu, mülakatın hangi aşamasıdır ve nasıl ele alınmalıdır?
## A
Gereksinim netleştirme aşaması; kapsamı en baştan sormaya geri dönmelisin
🤖 Kapsam zaten netleşti; bu, seçili bir bileşende derinleşme talebidir, baştan başlama değil.
## B
Deep dive aşaması; bileşeni alternatifleri ve ödünleşmeleriyle detaylandırmalısın
🤖 Doğru: belirli bir bileşende derinleşip alternatifleri karşılaştırma anıdır.
## C
Kapasite tahmini aşaması; yalnızca QPS ve depolama hesabını yeniden yapmalısın
🤖 Soru bir bileşenin iç tasarımını istiyor; ham kapasite hesabını değil.
## D
Kapanış aşaması; tasarımı kısaca özetleyip konuyu burada sonlandırmalısın
🤖 Bu kapanış değil, en teknik derinleşme anı; ciddi bir tasarım tartışması beklenir.
## Doğru
B
## Açıklama
Belirli bir bileşene ("benzersiz kısa anahtar üretimi") odaklanan bu soru **deep dive** aşamasıdır: mülakatçı genel resmi gördükten sonra bir bileşeni veya darboğazı **derinlemesine** görmek ister. Doğru ele alış tek bir cevap vermek değil, **alternatifleri karşılaştırıp ödünleşmelerini sunmaktır**: hash + çakışma kontrolü (basit ama çakışma yönetimi ister), önceden üretilmiş anahtar havuzu (KGS — koordinasyon ve depolama gerektirir), base62 kodlanmış artan id (basit ama tahmin edilebilir), Snowflake benzeri dağıtık id (koordinasyonsuz ama daha karmaşık). Her birinin çarpışma olasılığı, tahmin edilebilirlik, koordinasyon maliyeti ve ölçek etkisi tartışılır. Bunu gereksinim (A), kapasite (C) veya kapanış (D) aşamasıyla karıştırmak, mülakatçının aradığı teknik derinliği kaçırmaktır.
