---
slug: data-streaming-modern
baslik: "Modern Stream Processing: Windowing, Watermark, Exactly-Once ve Stateful Stream"
kademe: İleri
sira: 186
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

İki tür veri işleme vardır. **Batch (toplu)**: veri önce bir yere birikir, sonra hepsi birden işlenir — "dünkü satışları sabah raporla". **Stream processing (akış işleme)**: veri geldikçe, o anda işlenir — "şu an dakikada kaç tıklama geliyor". Akış **sınırsızdır (unbounded)**: hiç bitmez, "hepsi geldi" diyebileceğin bir an yoktur.

Neden gerekir? Dolandırıcılık tespiti, canlı gösterge panosu, stok ve fiyat güncellemesi, uyarı sistemleri — kısacası cevabın yarın değil şimdi gerekmesi. JVM dünyasında iki baskın araç var: **Kafka Streams** (uygulamanın içinde koşan bir kütüphane) ve **Apache Flink** (ayrı çalışan dağıtık motor; ağır durum ve düşük **gecikme (latency)** gereken işler için).

### Temel kavramlar

- **Olay (event)**: akıştaki tek kayıt — bir tıklama, bir ödeme, bir sensör ölçümü. Her olayın bir zaman damgası vardır.
- **Pencere (window)**: sınırsız akışı sonlu zaman dilimlerine bölme. Toplam ve ortalama gibi hesaplar ancak bir pencere içinde anlamlıdır.
  - **Tumbling**: bitişik, çakışmayan sabit pencereler (her 1 dakika).
  - **Hopping / Sliding**: çakışabilen, kaydırmalı pencereler (her 1 dakikada bir, son 5 dakika).
  - **Session**: sabit süresi yoktur; belirlenen **hareketsizlik boşluğu (gap)** kadar olay gelmeyince kapanan, değişken uzunlukta pencere.
- **Olay zamanı (event time)**: olayın gerçekte olduğu an. **İşleme zamanı (processing time)**: olayın senin sistemine ulaştığı an. İkisi ağ ve kuyruk gecikmesi yüzünden farklıdır.
- **Su işareti (watermark)**: "olay zamanı T'ye kadarki olayları büyük ihtimalle aldım" diyen, akışta ilerleyen bir eşik.
- **Durum (state)**: işin hatırladığı bilgi — sayaçlar, görülmüş anahtarlar, birleştirme (join) tamponları.
- **Tam bir kez etki (exactly-once)**: bir olay, hata ve yeniden işleme durumunda bile sonucu tam bir kez etkiler.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Pencere türleri: tumbling, hopping, session">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pencere türleri</text>
  <text x="16" y="56" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Tumbling</text>
  <text x="16" y="72" font-size="10" fill="#5D7789" font-family="system-ui">bitişik, çakışmaz</text>
  <rect x="150" y="46" width="90" height="26" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <rect x="244" y="46" width="90" height="26" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <rect x="338" y="46" width="90" height="26" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <rect x="432" y="46" width="90" height="26" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="16" y="130" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Hopping</text>
  <text x="16" y="146" font-size="10" fill="#5D7789" font-family="system-ui">çakışan kaydırma</text>
  <rect x="150" y="112" width="120" height="22" rx="4" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <rect x="210" y="140" width="120" height="22" rx="4" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <rect x="270" y="112" width="120" height="22" rx="4" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <rect x="330" y="140" width="120" height="22" rx="4" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="16" y="210" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Session</text>
  <text x="16" y="226" font-size="10" fill="#5D7789" font-family="system-ui">boşluğa göre</text>
  <rect x="150" y="196" width="70" height="26" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <rect x="250" y="196" width="130" height="26" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <rect x="410" y="196" width="55" height="26" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="235" y="214" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">gap</text>
  <text x="395" y="214" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">gap</text>
</svg>
```

### Nasıl çalışır?

Sınırsız bir akışta "son 1 dakikadaki tıklama sayısı" ancak pencerelemeyle hesaplanır: akış sonlu dilimlere bölünür, her dilim kapandığında toplulaştırma yayınlanır. Pencereleme, batch dünyasındaki "GROUP BY zaman" işleminin karşılığıdır.

Zor kısım şudur: olaylar **sırasız (out-of-order)** ve **geç** gelir. Mobil cihaz çevrimdışıydı, kuyruk birikti, bir **bölüm (partition — konunun paralel işlenen parçalarından biri)** yavaştı. İşleme zamanına göre pencereyi kapatırsan, 12:00:59'da gerçekleşen ama 12:01:03'te ulaşan olay yanlış pencereye düşer veya tümüyle kaybolur.

Doğrusu **olay zamanına göre** hesaplamak, pencereyi ne zaman kapatacağına da **su işaretiyle** karar vermektir. Su işareti pencerenin bitiş zamanını geçtiğinde pencere kapanır ve sonuç yayınlanır. Sonradan gelen **geç (late) veri** izin verilen gecikme payı (allowed lateness) içindeyse pencereyi günceller; değilse ayrı bir yan çıkışa (side output) yönlendirilir. Su işareti özünde bir denge ayarıdır: ne kadar beklersen doğruluk artar, gecikme de artar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Olaylar sırasız gelir, watermark pencereyi kapatır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Watermark pencereyi ne zaman kapatır</text>
  <line x1="40" y1="120" x2="600" y2="120" stroke="#5D7789" stroke-width="1.5"/>
  <text x="40" y="140" font-size="10.5" fill="#5D7789" font-family="system-ui">event time →</text>
  <circle cx="120" cy="120" r="7" fill="#186399"/>
  <text x="120" y="104" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">t=1</text>
  <circle cx="200" cy="120" r="7" fill="#186399"/>
  <text x="200" y="104" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">t=3</text>
  <circle cx="280" cy="120" r="7" fill="#B4442E"/>
  <text x="280" y="104" text-anchor="middle" font-size="10" fill="#B4442E" font-family="system-ui">t=2 (geç)</text>
  <circle cx="360" cy="120" r="7" fill="#186399"/>
  <text x="360" y="104" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">t=4</text>
  <line x1="430" y1="86" x2="430" y2="154" stroke="#25804C" stroke-width="2.5"/>
  <text x="430" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">watermark = 4</text>
  <text x="430" y="190" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">"≤4'ü aldım" → pencere kapanır</text>
  <text x="320" y="60" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Olaylar sırasız gelir (t=2, t=3'ten sonra); watermark geç veriyi tolere eder.</text>
</svg>
```

Birleştirme (join), toplulaştırma (aggregation) ve tekilleştirme (dedup) **durumlu (stateful)** işlemlerdir; doğru sonuç için geçmişi hatırlamak gerekir. Bu durum bir **durum deposunda (state store)** tutulur ve arızaya dayanıklı olmalıdır. Flink bunu belirli aralıklarla aldığı **durum anlık kaydıyla (checkpoint)** ve elle alınan savepoint'lerle yapar. Kafka Streams ise durum deposunu bir **changelog topic**'e yazar. Süreç çökerse durum son tutarlı noktadan geri yüklenir.

Buradan **tam bir kez etkiye (exactly-once)** geliriz. Çökme sonrası toparlanmada (recovery) bazı olaylar yeniden işlenir. Buna rağmen sonucun tam bir kez etkilenmesi isteniyorsa iki parça birlikte gerekir. Birincisi durumun tutarlı **anlık kaydı**. İkincisi, çıktı yazımının **tekrar edilse de sonucu değişmeyen (idempotent) ya da işlem (transaction) içinde** olması — Kafka transactions veya Flink'in iki fazlı commit yapan çıkış hedefi (sink) gibi.

Kritik incelik şudur: tam bir kez etki ancak **uçtan uca** geçerlidir. Yani kaynağın **okuma konumu (offset)**, durum ve çıkış hedefinin commit'i birlikte, tek parça hâlinde onaylanmalıdır. Motorun içinde garanti verip çıkış hedefine sıradan bir `INSERT` atarsan zincir kopar ve çift yazma görürsün. Bu yüzden bir satıcı "exactly-once garantisi var" dediğinde sorulacak soru bellidir: kapsam nedir, çıkış hedefine yazım nasıl commit ediliyor?

Karşılaştırma için iki zayıf garanti daha vardır. **En fazla bir kez (at-most-once)** veri kaybına izin verir; **en az bir kez (at-least-once)** ise aynı olayın tekrarına izin verir. Pratikte "en az bir kez + tekrar edilse de sonucu değişmeyen çıkış hedefi" birleşimi çoğu ihtiyacı karşılar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uçtan uca exactly-once: kaynak offset, durum checkpoint, transactional sink">
  <defs>
    <marker id="dstream-flow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Exactly-once ancak uçtan uca zincir kurulunca geçerli</text>
  <rect x="20" y="72" width="150" height="64" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="98" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kaynak</text>
  <text x="95" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">offset takibi</text>
  <line x1="170" y1="104" x2="204" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#dstream-flow)"/>
  <rect x="208" y="72" width="150" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="283" y="98" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Durum</text>
  <text x="283" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">checkpoint</text>
  <line x1="358" y1="104" x2="392" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#dstream-flow)"/>
  <rect x="396" y="72" width="150" height="64" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="471" y="98" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Sink</text>
  <text x="471" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">transactional / idempotent</text>
  <rect x="20" y="152" width="526" height="30" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="283" y="172" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">Üçü birlikte atomik commit — biri transactional değilse garanti kopar</text>
</svg>
```

### Mülakatta ne sorulur?

Sık sorulanlar: "Toplu (batch) işleme ile akış işleme farkı nedir?" "Olay zamanı ile işleme zamanı — neden önemli?" "Su işareti ne yapar, geç veriye ne olur?" "Hangi pencere türünü ne zaman seçersin?" (oturum davranışı → session). "Birleştirme ve tekilleştirme neden durumludur, durum nasıl kurtarılır?" "Tam bir kez etki nasıl sağlanır ve zincir nerede kopar?"

Yanlış cevap kalıpları: "veriyi biriktirip toplu koşarız", "işleme zamanı yeter", "durumu bellekte tutarız", "satıcının garanti sözü yeterli".

### Tek cümlede

Akış işleme sınırsız veriyi pencerelere böler. Doğruluğu olay zamanı ve su işaretiyle korur. Birleştirme, toplulaştırma ve tekilleştirme için durumu anlık kayıtlarla dayanıklı tutar. Tam bir kez etkiyi ise ancak kaynak + durum + çıkış hedefi zinciri uçtan uca işlemsel kurulduğunda gerçekten sağlar.

# dstream-1 | Orta
## Soru
Sınırsız (unbounded) bir tıklama akışında "son 1 dakikadaki tıklama sayısı"nı sürekli hesaplamak istiyorsun. Bunu mümkün kılan temel kavram nedir?
## A
Akışı önce tümüyle diske toplayıp akış bittiğinde tek bir batch iş olarak saymak
🤖 Akış sınırsızdır ve bitmez; batch mantığı burada uygulanamaz, pencereleme gerekir.
## B
Windowing: sınırsız akışı zaman pencerelerine bölüp her pencerede toplu hesap yapmak
🤖 Doğru: sınırsız akışta agregasyon ancak pencerelere bölünerek sonlu hesaplara dönüşür.
## C
Her olayı tek tek işleyip hiç toplulaştırma yapmadan sonucu doğrudan yayınlamak
🤖 Tek tek işleme "1 dakikalık sayım" toplamını vermez; zaman penceresi gerekir.
## D
Olayları bir tabloya yazıp her saniye SELECT COUNT(*) sorgusu koşturmak
🤖 Sürekli poll etmek ölçeklenmez ve geç/sırasız veriyi doğru ele almaz; pencere gerekir.
## Doğru
B
## Açıklama
Sınırsız bir akışta "son 1 dakika" gibi toplu bir hesap, akışı sonlu dilimlere bölmeden yapılamaz; bunu sağlayan kavram **windowing (pencereleme)**'dir. Pencere türleri: **tumbling** (bitişik, çakışmayan sabit pencere — "her 1 dakika"), **hopping/sliding** (çakışan kaydırmalı pencere) ve **session** (aktivite boşluğuna göre dinamik). Her pencere kapandığında o dilimdeki olaylar toplulaştırılır. Tümünü toplayıp batch saymak (A) imkânsızdır çünkü akış bitmez; tek tek işleme (C) toplamı vermez. Tabloya yazıp saniyede bir `COUNT(*)` çekmek (D) sahada sık görülen bir yanılgıdır: hacim büyüdükçe ölçeklenmez, geç gelen olayları doğru pencereye yazamaz ve zaten pencerelemeyi elle ve hatalı biçimde yeniden icat eder. Pencereleme, batch dünyasının "GROUP BY zaman" karşılığıdır.

# dstream-2 | İleri
## Soru
Olaylar ağ gecikmesi yüzünden sırasız (out-of-order) ve geç geliyor. Bir zaman penceresinin ne zaman "tamamlandı" sayılıp sonuç üreteceğine olay zamanına (event time) göre karar veren mekanizma nedir?
## A
Processing time'a göre pencereyi hemen kapatıp geç gelen veriyi tümüyle yok saymak
🤖 Processing time geç ve sırasız veriyi doğru ele almaz; sonuç sistematik olarak eksik çıkar.
## B
Her olayı geldiği anda ayrı sonuç olarak yayınlayıp pencere kavramını tamamen atmak
🤖 Pencereyi atmak agregasyonu bozar; soru tam da pencerenin ne zaman kapanacağıdır.
## C
Kaynakta olayları zaman damgasına göre tam sıralayıp motora sıralı vermek
🤖 Dağıtık ve sınırsız akışta küresel sıralama pratik değildir; watermark bu yüzden vardır.
## D
Watermark: olay zamanında ilerleyen eşik; pencere sonunu geçince pencere kapanır
🤖 Doğru: watermark olay-zamanı ilerlemesini izler ve pencerenin kapanmasını ilkeli yönetir.
## Doğru
D
## Açıklama
**Watermark (su işareti)**, olay-zamanı temelli işlemenin kalbidir: akışta ilerleyen ve "event time T'ye kadar (muhtemelen) tüm olayları aldım" diyen bir eşiktir. Watermark bir pencerenin bitiş zamanını geçtiğinde o pencere kapatılır ve sonucu yayınlanır; sonrasında gelen **geç (late) veri** yapılandırmaya göre tolere edilir (izin verilen gecikme içinde pencere güncellenir) veya ayrı bir yan çıkışa (side output) yönlendirilir. Processing time'a göre kapatmak (A) sırasız ve geç veriyi bozar. Pencereyi atmak (B) agregasyonu yok eder. Kaynakta küresel sıralama yapmak (C) cazip görünen ama uygulanamaz bir fikirdir: sınırsız ve dağıtık bir akışta tüm bölümleri (partition) küresel olarak sıralamak sınırsız tampon ve sınırsız bekleme gerektirir — watermark tam da bunun pratik alternatifidir. Watermark, doğruluk (geç veriyi bekle) ile latency (sonucu üret) arasındaki dengeyi ilkeli biçimde kurar.

# dstream-3 | İleri
## Soru
Bir stream işi checkpoint'ten kurtulup (recovery) bazı olayları yeniden işlese bile, çıktı sonucunun her olaydan tam bir kez etkilenmesini istiyorsun. Bu semantik nedir ve tipik olarak nasıl sağlanır?
## A
Exactly-once: checkpoint'li durum ile idempotent/transactional çıktı yazımının birleşimi
🤖 Doğru: durum checkpoint'i ve transactional sink birlikte tam-bir-kez etkiyi sağlar.
## B
At-most-once: her olay en fazla bir kez işlenir, kayıp olsa bile yeniden denenmez
🤖 At-most-once veri kaybına izin verir; istenen "tam bir kez etki" bu değildir.
## C
At-least-once: olay en az bir kez işlenir; yineleme sonucu bozmaz
🤖 At-least-once yinelemeye izin verir; sink idempotent değilse sonuç iki kez etkilenir.
## D
Best-effort: motor elinden geleni yapar, hata durumunda bir garanti verilmez
🤖 Garantisiz yaklaşım "tam bir kez" gereksinimini karşılamaz.
## Doğru
A
## Açıklama
Aranan semantik **exactly-once (tam-bir-kez)**'dir: bir olay, hata ve yeniden işleme (recovery) durumunda bile sonucu **tam bir kez** etkiler. Tipik sağlanışı iki parçalıdır: (1) işleme durumu periyodik **checkpoint** ile tutarlı biçimde kaydedilir (Flink checkpoint, Kafka Streams changelog), (2) çıktı yazımı **idempotent veya transactional** olur (Kafka transactions, Flink two-phase commit sink). Kurtulmada durum son checkpoint'e döner ve transactional sink yarım veya yinelenen yazımı geri alır; net etki tam bir kezdir. At-most-once (B) veri kaybına izin verir. At-least-once (C) en sık karıştırılan seçenektir: olayın kaybolmamasını garanti eder ama **yinelenmesine izin verir** — sink idempotent değilse sayaç iki kez artar, dolayısıyla "sonucu bozmaz" ifadesi yanlıştır. Best-effort (D) hiçbir garanti vermez.

# dstream-4 | İleri
## Soru
İki akışı (ör. sipariş akışı ile ödeme akışı) anahtara göre birleştiren (join), sayan ve tekilleştiren (dedup) bir iş yazıyorsun. Bu işlemlerin ortak zorluğu ve doğru ele alınışı nedir?
## A
Bu işlemler durumsuzdur (stateless); geçmişten hiçbir şey saklamak gerekmez
🤖 Join, aggregation ve dedup geçmişi hatırlamayı gerektirir; durumsuz değildir.
## B
Daha hızlı CPU ve daha çok bellek almak yeterlidir; ayrı bir durum yönetimi gerekmez
🤖 Sorun hız değil, durumun çökme sonrası kurtarılabilir olmasıdır; donanım bunu çözmez.
## C
Durumludurlar: state store'da tutulan durum checkpoint/changelog ile dayanıklı yapılır
🤖 Doğru: bu işlemler durum gerektirir ve durum kurtarılabilir biçimde saklanmalıdır.
## D
Durumu yalnız süreç belleğinde tutmak en hızlısıdır; yeniden başlatmada sıfırdan başlanır
🤖 Yedeksiz bellek çökünce durumu kaybeder; sayaçlar sıfırlanır, join eşleşmeleri kaybolur.
## Doğru
C
## Açıklama
Join, aggregation ve dedup **durumlu (stateful)** işlemlerdir: doğru sonuç için geçmiş olayları ve anahtarları hatırlamak gerekir — join için karşı akışın tampon durumu, dedup için görülmüş anahtarlar, sayım için biriken değer. Bu durum bir **state store**'da tutulur ve **dayanıklı (fault-tolerant)** olmalıdır: Flink'te checkpoint/savepoint, Kafka Streams'te changelog topic'e yazılan state store. Böylece süreç çökse bile durum son tutarlı noktadan kurtulur ve exactly-once mümkün hâle gelir. İşlemleri durumsuz sanmak (A) temel bir yanlıştır; donanım büyütmek (B) sorunu yanlış eksende ele alır — mesele hız değil kurtarılabilirliktir; durumu yedeksiz bellekte tutmak (D) çökmede sayaçları sıfırlar ve yarım kalan join eşleşmelerini kaybettirir. Durum yönetimi ve dayanıklılığı, stateful stream işlerinin en kritik tasarım eksenidir.

# dstream-5 | Orta
## Soru
Bir e-ticaret sitesinde her kullanıcının "oturum" boyunca yaptığı tıklamaları, oturum aralarındaki hareketsizlik boşluğuna (inactivity gap) göre gruplamak istiyorsun. Hangi pencere türü uygundur?
## A
Tumbling window: sabit süreli, çakışmayan dilimler hâlinde saymak
🤖 Tumbling sabit süreli dilimler verir; kullanıcı hareketsizliğine göre dinamik gruplamaz.
## B
Session window: hareketsizlik boşluğuna göre dinamik açılıp kapanan pencere
🤖 Doğru: session window tam da hareketsizlik boşluğuna göre oturumları dinamik gruplar.
## C
Global window: hiç bölme yapmadan tüm akışı tek pencerede toplamak
🤖 Tek global pencere oturumları ayırmaz; her kullanıcının aktivite boşluğunu yakalamaz.
## D
5 dakikada bir kayan, 30 dakikalık sabit hopping window kullanmak
🤖 Sabit süreli hopping, kullanıcının değişken oturum uzunluğuna ve boşluğuna uymaz.
## Doğru
B
## Açıklama
Kullanıcı oturumlarını **hareketsizlik boşluğuna** göre gruplamak tam olarak **session window**'un işidir: pencere olaylar geldikçe uzar ve belirlenen boşluk süresi (gap) kadar hiç olay gelmeyince kapanır — böylece her kullanıcının doğal oturumu değişken uzunlukta yakalanır. Tumbling (A) sabit dilimler verir ve oturum sınırıyla hizalanmaz; global window (C) hiç ayırmaz; sabit süreli hopping (D) değişken oturum uzunluğuna uymaz. Session window'lar özellikle kullanıcı davranışı, cihaz oturumları ve doğal aktivite kümeleri için tasarlanmıştır. Boşluk süresi doğru seçilmelidir: çok kısa bir gap tek oturumu ikiye böler, çok uzun bir gap ayrı oturumları tek oturumda birleştirir.

# dstream-6 | Uzman
## Soru
Bir satıcı ürünü için "exactly-once garantisi var" diyor. Senior mühendis olarak hangi soruyu sormak en kritik?
## A
"Saniyede kaç olay işliyor?" — asıl kritik olan garanti değil, iş hacmi (throughput) rakamıdır
🤖 Throughput ayrı bir eksendir; doğruluk garantisinin kapsamını hiç açıklamaz.
## B
"Consumer commit'i otomatik mi?" — otomatik commit açıksa exactly-once kendiliğinden sağlanır
🤖 Otomatik commit tam tersine yineleme ve kayıp üretir; exactly-once garantisi vermez.
## C
"Hiçbir soru sormaya gerek yok; sözleşmede yazan garanti ifadesi bağlayıcıdır"
🤖 Çıplak garanti sözü uçtan uca kapsamı doğrulamaz; sorgulamak gerekir.
## D
"Garanti uçtan uca mı — kaynak, durum ve sink birlikte mi commit ediliyor?"
🤖 Doğru: exactly-once ancak kaynak+durum+sink zinciri birlikte atomikse gerçekten geçerlidir.
## Doğru
D
## Açıklama
"Exactly-once" iddiası **kapsam** belirtilmeden yanıltıcıdır. Bir motor kendi iç durumunu checkpoint ile exactly-once tutabilir; ama sonuç **dış sistemlere** (veritabanı, başka bir topic, bir API) yazılıyorsa gerçek uçtan uca exactly-once ancak **sink transactional veya idempotent** olduğunda geçerlidir. Bu yüzden kritik soru şudur: garanti uçtan uca mı — kaynak okuma offset'i, durum ve çıkış commit'i birlikte atomik mi — yoksa yalnız motor içi mi? Sink'e yazım nasıl commit ediliyor (two-phase commit, idempotency anahtarı)? Throughput sormak (A) ayrı bir eksendir ve doğruluk garantisini açıklamaz. Otomatik commit (B) yaygın bir yanılgıdır: otomatik offset commit, işlem tamamlanmadan ilerleyebildiği için kayıp veya yineleme üretir, exactly-once'ın tam tersidir. Garantiyi sorgusuz kabul etmek (C) üretimde sessiz çift yazma veya kayıp riskidir.
