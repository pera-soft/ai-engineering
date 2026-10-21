---
slug: cap-pacelc
baslik: "CAP ve PACELC: Partition'da C/A Seçimi, Latency ve Tutarlılık Modelleri"
kademe: Uzman
sira: 128
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Veriyi tek bir makinede tutarsan hayat basittir ama o makine ölünce her şey durur. Bu yüzden veri birden çok makineye **kopyalanır (replikasyon)**. Kopyalar hayat kurtarır, karşılığında yeni bir soru getirir: iki kopya birbirinden farklı olduğunda hangisi doğrudur ve müşteriye ne söyleyeceğiz?

CAP ve PACELC bu soruya çerçeve verir. Bunlar "hangi veritabanı iyidir" listesi değil, **hangi kötü haberi kabul edeceğini seçme** kurallarıdır. Mülakatta CAP'i doğru çerçevelemek, dağıtık sistem olgunluğunun en hızlı göstergelerinden biridir.

### Temel kavramlar

- **Düğüm (node) / kopya (replica):** aynı veriyi tutan makineler.
- **Tutarlılık (C — consistency):** her okumanın **en güncel** yazmayı görmesi. Buradaki C, ACID'in C'si (kısıtların korunması) değildir. Buradaki C, **doğrusallaştırılabilirliktir (linearizability)**: sistem, dışarıdan bakınca sanki tek bir kopya varmış gibi davranır.
- **Kullanılabilirlik (A — availability):** ayakta olan her düğümün her isteğe (hata değil) bir yanıt vermesi.
- **Ağ bölünmesi (P — network partition):** ağın kopması. İki grup düğüm birbiriyle konuşamaz ama ikisi de çalışmaya devam eder. Kablo kopar, anahtar arızalanır, veri merkezleri arası hat düşer. Bu bir tasarım seçeneği değil, er geç yaşanan bir **gerçektir**.

Buradan CAP'in doğru okuması çıkar. Yaygın "üçünden ikisini seç" özeti **yanıltıcıdır**, çünkü P'yi menüden çıkarma seçeneğin yoktur. Asıl seçim yalnız **bölünme sırasında** yapılır: tutarlılığı korumak için isteği reddetmek (**CP**) mi, yoksa elindeki muhtemelen bayat veriyle yanıt verip çakışmayı sonra çözmek (**AP**) mi? Bölünme yokken böyle bir ikilem CAP'in konusu değildir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="PACELC karar ağacı: partition varsa A/C, yoksa L/C">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">PACELC: iki durum, iki ödünleşme</text>
  <rect x="234" y="40" width="172" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="67" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Ağ bölünmesi (P) var mı?</text>
  <rect x="70" y="120" width="200" height="76" rx="10" fill="#FBECEA" stroke="#B0453B" stroke-width="1.5"/>
  <text x="170" y="145" text-anchor="middle" font-size="12" font-weight="700" fill="#B0453B" font-family="system-ui">EVET → A vs C</text>
  <text x="170" y="167" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">CP: tutarlılık için reddet</text>
  <text x="170" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">AP: yanıt ver, sonra çöz</text>
  <rect x="370" y="120" width="200" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="145" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">HAYIR (Else) → L vs C</text>
  <text x="470" y="167" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EL: düşük gecikme seç</text>
  <text x="470" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EC: güçlü tutarlılık seç</text>
  <line x1="290" y1="84" x2="180" y2="118" stroke="#B0453B" stroke-width="2"/>
  <line x1="350" y1="84" x2="460" y2="118" stroke="#25804C" stroke-width="2"/>
</svg>
```

### Nasıl çalışır?

Bölünme anında bir düğüm, karşı tarafla konuşamadığı için en güncel veriyi garanti edemez. İki seçenek vardır. **CP** yolunda düğüm "emin olamıyorum" der ve isteği (özellikle yazmayı) **reddeder**. **Uzlaşma (consensus)** tabanlı **ZooKeeper** ve **etcd** böyledir; düğümlerin **çoğunluğu (quorum)** el sıkışmadan ilerlemezler.

**AP** yolunda ise düğüm yanıt vermeye **devam eder** ve doğan çakışmaları sonradan çözer. Çözüm yolları şunlardır: son yazan kazanır, okuma sırasında onarım (read repair) ya da **çakışmasız kopyalanan veri tipleri (CRDT)** — yani birleştirildiğinde her zaman aynı sonucu veren özel veri yapıları. Dynamo soyundan gelen **Cassandra** varsayılanıyla bu uçtadır.

Bu ayrım "ilişkisel mi NoSQL mü" sorusu değildir. Sınıf sabit de değildir: birçok sistem istek başına **ayarlanabilir tutarlılık (tunable consistency)** sunar.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ağ bölünmesinde CP tutarlılık için reddeder AP yanıt vermeye devam eder">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bölünme (P) kaçınılmaz — seçim: C mi A mı?</text>
  <circle cx="150" cy="70" r="26" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="150" y="74" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">N1</text>
  <circle cx="230" cy="70" r="26" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="230" y="74" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">N2</text>
  <circle cx="410" cy="70" r="26" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="410" y="74" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">N3</text>
  <circle cx="490" cy="70" r="26" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="490" y="74" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">N4</text>
  <path d="M320 40 L308 60 L326 80 L310 100" fill="none" stroke="#8A2E2E" stroke-width="2.4"/>
  <text x="320" y="120" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">ağ bölündü</text>
  <rect x="40" y="140" width="270" height="74" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="175" y="164" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">CP — tutarlılığı koru</text>
  <text x="175" y="186" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">erişilemez taraf yazmayı reddeder</text>
  <text x="175" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ZooKeeper, etcd · A'dan vazgeç</text>
  <rect x="330" y="140" width="270" height="74" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="465" y="164" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">AP — erişilebilirliği koru</text>
  <text x="465" y="186" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yanıt ver, çakışmayı sonra çöz</text>
  <text x="465" y="204" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Dynamo/Cassandra · C'den vazgeç</text>
</svg>
```

**PACELC** CAP'in bıraktığı boşluğu doldurur: eğer **P**artition varsa **A** ile **C** arasında seçim yaparsın; **E**lse — yani sistem sağlıklıyken — **L**atency ile **C**onsistency arasında seçim yaparsın. Anahtar kavrayış şudur: güçlü tutarlılık hiçbir bölünme olmasa **bile** bedelsiz değildir, çünkü kopyalar arasında senkron koordinasyon **gecikme** ekler. Böylece sistemler iki boyutlu sınıflanır: "PA/EL" (bölünmede kullanılabilirlik, normalde düşük gecikme) ya da "PC/EC" (her koşulda tutarlılık) gibi.

Cassandra varsayılanıyla PA/EL profilindedir. Ama `ONE`, `QUORUM`, `ALL` gibi tutarlılık seviyeleriyle daha güçlü tutarlılığa doğru kaydırılabilir. Klasik ölçüt `W + R > N` kuralıdır: yazmanın onay beklediği kopya sayısı (W) ile okumanın yanıt beklediği kopya sayısı (R) toplamı, toplam kopya sayısını (N) geçerse okuma en güncel yazmayı görmek zorundadır. Bedeli de vardır: daha çok gecikme ve daha az kullanılabilirlik.

Tutarlılık ikili bir anahtar (var/yok) değil, bir **spektrumdur**:

- **Güçlü tutarlılık (strong / linearizable):** tüm işlemler tek, ortak bir sıraya göre görünür. Her okuma en güncel yazmayı görür. En yüksek koordinasyon, en yüksek gecikme.
- **Nedensel tutarlılık (causal):** birinin diğerini gördüğü, yani **nedensel ilişkili** işlemler herkeste aynı sırada görünür. İlişkisiz ve eşzamanlı işlemler ise farklı sıralanabilir. Böylece güçlü tutarlılığın bedelini ödemeden sezgisel bir sebep-sonuç garantisi alınır. Güç bakımından güçlü ile nihai tutarlılığın arasındadır.
- **Nihai tutarlılık (eventual):** yeni yazma durursa kopyalar **sonunda** aynı değere yakınsar. Arada farklı değerler okunabilir. Bu, "sonsuza dek farklı kalır" demek değildir.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tutarlılık spektrumu: strong, causal, eventual">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tutarlılık bir spektrumdur</text>
  <line x1="60" y1="70" x2="580" y2="70" stroke="#5D7789" stroke-width="2"/>
  <text x="70" y="60" font-size="10" fill="#25804C" font-family="system-ui">güçlü / yüksek koordinasyon</text>
  <text x="570" y="60" text-anchor="end" font-size="10" fill="#B26B00" font-family="system-ui">zayıf / düşük gecikme</text>
  <circle cx="110" cy="70" r="7" fill="#25804C"/>
  <circle cx="320" cy="70" r="7" fill="#186399"/>
  <circle cx="540" cy="70" r="7" fill="#B26B00"/>
  <text x="110" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Strong</text>
  <text x="110" y="118" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">linearizable</text>
  <text x="110" y="134" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her okuma en güncel</text>
  <text x="320" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Causal</text>
  <text x="320" y="118" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">nedensellik korunur</text>
  <text x="320" y="134" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ilişkisizler serbest</text>
  <text x="540" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Eventual</text>
  <text x="540" y="118" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sonunda yakınsar</text>
  <text x="540" y="134" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">arada farklı değer</text>
  <text x="320" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sola gittikçe garanti artar, gecikme/koordinasyon maliyeti de artar</text>
</svg>
```

### Mülakatta ne sorulur?

- CAP bölünme anında tam olarak ne söyler? "İkisini seç" neden yanlış bir özettir?
- CP ve AP sistemlere doğru örnek verebiliyor musun?
- CAP'teki C hangi tutarlılıktır? (Linearizability, ACID'in C'si değil.)
- PACELC CAP'e neyi ekler? Bölünme yokken hangi takas vardır?
- Nedensel tutarlılık (causal consistency) nerede durur? Cassandra hangi profildedir?

### Tek cümlede

Ağ bölünmesi bir seçim değil, kaçınılmaz bir gerçektir. CAP sana yalnız **bölünme anında** C ile A arasında seçtirir. PACELC ise bölünme yokken bile güçlü tutarlılığın **gecikmeyle** ödendiğini hatırlatır.

# cap-1 | İleri
## Soru
CAP teoremi, bir ağ **bölünmesi** (partition) yaşandığında ne söyler?
## A
İyi tasarlanmış bir sistem C, A ve P özelliklerinin üçünü aynı anda garanti edebilir
🤖 Klasik efsane; bölünme anında C ve A aynı anda garanti edilemez, birinden vazgeçilir.
## B
Bölünme anında tutarlılıktan her zaman vazgeçilmek zorundadır; başka seçenek yoktur
🤖 Zorunlu değil; sistem tutarlılığı koruyup (CP) kullanılabilirlikten vazgeçmeyi de seçebilir.
## C
Bölünme olmasa bile her istekte C ile A arasından yalnızca biri seçilebilir
🤖 CAP'in seçimi bölünme anına özgüdür; bölünme yokken bu ikilem CAP kapsamında değildir.
## D
Bölünme sırasında C ile A arasında seçim zorunludur; ikisi birden garanti edilemez
🤖 Doğru: bölünme anında tutarlılık ile kullanılabilirlik arasında bir seçim yapılır.
## Doğru
D
## Açıklama
CAP'in özü **bölünme anıyla** ilgilidir: ağ bölündüğünde bir düğüm, diğer tarafla konuşamadığı için ya (a) en güncel veriyi garanti edemeyeceğinden isteği **reddeder** (tutarlılığı korur, kullanılabilirlikten vazgeçer → **CP**), ya da (b) elindeki muhtemelen bayat veriyle **yanıt verir** (kullanılabilirliği korur, tutarlılıktan vazgeçer → **AP**). İkisini birden garanti edemez. "Üçünü de al" (A) bir efsanedir; "her zaman C feda edilir" (B) yanlıştır çünkü CP mümkündür; bölünme yokken seçim zorunluluğu (C) CAP'in değil PACELC'nin konusudur.

# cap-2 | İleri
## Soru
Aşağıdakilerden hangisi doğru bir **CP** ve **AP** sınıflandırması ve örneğidir?
## A
DNS bir CP sistemidir; önbellek kullansa da sorgulara her zaman en güncel kaydı döndürür
🤖 DNS, önbellekleme yüzünden bayat kayıt döndürebilen tipik bir AP sistemidir; CP değildir.
## B
Bölünmede yazmayı reddeden CP'dir (etcd); yanıt vermeyi sürdüren AP'dir (Cassandra)
🤖 Doğru: reddeden = CP, yanıt vermeye devam eden = AP; örnekler yerinde.
## C
Cassandra varsayılan yapılandırmasıyla bir CP sistemidir; ağ bölündüğünde yazmayı reddeder
🤖 Cassandra Dynamo soyundandır ve varsayılanıyla AP eğilimlidir; bölünmede yazmaya devam eder.
## D
Bir sistemin CP/AP sınıfı sabittir; hiçbir yapılandırmayla tutarlılık davranışı değiştirilemez
🤖 Birçok sistem istek başına ayar (tunable consistency) sunar; sınıf katı biçimde sabit değildir.
## Doğru
B
## Açıklama
Bölünme anındaki tercih sistemleri ikiye ayırır. **CP** sistemler tutarlılığı korumak için erişilemez/azınlık tarafta **isteği reddeder** — konsensüs tabanlı **ZooKeeper** ve **etcd** tipik örneklerdir, çoğunluk kuorumu olmadan ilerlemezler. **AP** sistemler bölünmede **yanıt vermeye devam eder** ve doğan çakışmaları sonradan çözer (son yazan kazanır, CRDT, read-repair) — Dynamo soyundan **Cassandra** varsayılanıyla bu uçtadır. DNS bir AP örneğidir (A yanlış); Cassandra varsayılanıyla CP değildir (C yanlış); tutarlılık seviyesi birçok sistemde ayarlanabildiği için sınıf katı biçimde sabit değildir (D yanlış).

# cap-3 | İleri
## Soru
CAP teoremi hakkındaki **en yaygın yanlış anlama** hangisidir?
## A
CAP, dağıtık sistem tasarımının tüm ödünleşmelerini tek çerçevede eksiksiz özetler
🤖 CAP dar bir teoremdir (yalnız bölünme anı); ama sorulan klasik hata bu değil, aşağıdaki daha temel olandır.
## B
CAP yalnız yazma işlemleri için geçerlidir; okuma tarafı tümüyle kapsam dışıdır ve etkilenmez
🤖 CAP hem okuma hem yazma tutarlılığını kapsar; ama sorulan asıl klasik yanılgı bu değildir.
## C
"Üçten ikisini seç" özeti: bölünme seçilebilir sanılır, oysa kaçınılmaz bir gerçektir
🤖 Doğru: en yaygın hata bölünmeyi seçilebilir sanmak ve teoremi "iki tanesini seç" diye özetlemektir.
## D
CAP, ağ gecikmesinin her koşulda ihmal edilebilir olduğunu örtük olarak varsayar
🤖 CAP gecikmeyi varsaymaz; gecikme boyutunu ekleyen PACELC'dir, bu bir "yanlış anlama" değil kapsam farkıdır.
## Doğru
C
## Açıklama
En yaygın ve en zararlı yanılgı, CAP'i "**üç özellikten istediğin ikisini seç**" diye özetlemektir. Bu, partition tolerance'ı menüden çıkarılabilir bir seçenek gibi gösterir; oysa gerçek ağlarda **bölünme kaçınılmazdır**, onu "seçmemek" diye bir şey yoktur. Doğru okuma: bölünme **olduğunda** C ile A arasında seçim yaparsın, bölünme yokken bu ikilem yoktur. İkinci ince nokta: CAP'teki **C linearizability'dir** (güçlü, tek kopya illüzyonu), ACID'in tutarlılık kuralı değil. A ve B kısmen doğru gözlemlerdir ama "en yaygın yanlış anlama" değildir; D ise PACELC'nin eklediği boyutu bir hata sanmaktır.

# cap-4 | Uzman
## Soru
**PACELC**, CAP'i nasıl genişletir?
## A
Bölünme varsa A ile C arasında; Else yani normal çalışmada Latency ile Consistency arasında
🤖 Doğru: PACELC hem bölünme anını hem de normal çalışmadaki gecikme-tutarlılık takasını kapsar.
## B
Bölünme boyutunu tümüyle atar; yalnız gecikme ile tutarlılık eksenini ele almakla yetinir
🤖 PACELC bölünmeyi atmaz; P durumunu korur ve üzerine "Else" (normal çalışma) durumunu ekler.
## C
Availability kavramını çerçeveden çıkarır; yalnız tutarlılık ile bölünmeyi karşılaştırır
🤖 Availability, PACELC'nin P dalında (A vs C) hâlâ merkezîdir; çıkarılmaz.
## D
Yalnız tek düğümlü veritabanları için tanımlıdır; çok kopyalı dağıtık sistemleri hiç kapsamaz
🤖 PACELC dağıtık sistemler için tanımlıdır; tek düğümde bölünme/replikasyon takası anlamsız olur.
## Doğru
A
## Açıklama
CAP yalnız **bölünme anını** konuşur ve bölünme yokken sistemlerin nasıl farklılaştığı sorusunu boş bırakır. **PACELC** (Abadi) bu boşluğu doldurur: eğer **P**artition varsa **A** vs **C** (CAP'in klasik ikilemi); **E**lse — yani sistem sağlıklıyken — **L**atency vs **C**onsistency. Anahtar kavrayış: güçlü tutarlılık, hiçbir bölünme olmasa **bile** bir maliyet taşır, çünkü kopyalar arası senkron koordinasyon **gecikme** ekler. Böylece bir sistem "PA/EL" (bölünmede kullanılabilirlik, normalde düşük gecikme) veya "PC/EC" (her koşulda tutarlılık) gibi iki boyutlu sınıflanır. B, C ve D PACELC'yi yanlış tarif eder.

# cap-5 | Uzman
## Soru
Tutarlılık modelleri (consistency models) hakkında hangisi doğrudur?
## A
Eventual ile strong consistency pratikte aynı garantileri farklı isimlerle sunar
🤖 Aynı değiller: eventual yalnız "sonunda yakınsama" vaat eder, strong her okumanın en güncel yazmayı görmesini garanti eder.
## B
Causal, nedensel ilişkili işlemlerin sırasını korur; strong ile eventual arasında yer alır
🤖 Doğru: causal nedenselliği korur, ilişkisizlerde serbesttir; güç olarak ortadadır.
## C
Causal consistency, linearizable (strong) tutarlılıktan daha güçlü bir garanti sunar
🤖 Tersine: linearizable en güçlüsüdür; causal ondan zayıf, eventual'dan güçlüdür.
## D
Eventual consistency, iki kopyanın sonsuza dek birbirinden farklı değer döndürebilmesi demektir
🤖 eventual, yeni yazma olmazsa kopyaların sonunda aynı değere yakınsayacağını garanti eder.
## Doğru
B
## Açıklama
Tutarlılık ikili (var/yok) değil, bir **spektrumdur**. En güçlü uçta **strong / linearizable**: tüm işlemler tek, ortak bir sıraya göre görünür ve her okuma en güncel yazmayı görür (yüksek koordinasyon → yüksek gecikme). Pratik uçların diğerinde **eventual**: yeni yazma durursa kopyalar **sonunda** aynı değere yakınsar, ama arada farklı değerler görülebilir. Ortada **causal consistency** vardır: **nedensel olarak ilişkili** işlemler (birinin diğerini gördüğü) herkeste aynı sırada görünür, ilişkisiz eşzamanlı işlemler farklı sıralanabilir — bu, güçlü tutarlılığın maliyetine katlanmadan sezgisel bir sebep-sonuç garantisi verir. A ve C güç sıralamasını, D ise eventual'ın yakınsama garantisini yanlış kurar.

# cap-6 | Uzman
## Soru
PACELC çerçevesinde varsayılan yapılandırmasıyla **Cassandra** nasıl sınıflanır ve bu ne anlama gelir?
## A
PC/EC: hem bölünmede hem de normal çalışmada tutarlılığı önceler
🤖 Bu daha çok konsensüs tabanlı sistemlerin profilidir; Cassandra varsayılanı bu değildir.
## B
PA/EC: bölünmede kullanılabilirliği, normal çalışmada güçlü tutarlılığı seçer
🤖 Cassandra normal çalışmada da senkron koordinasyondan kaçınıp düşük gecikmeyi (EL) önceler.
## C
PC/EL: bölünmede tutarlılığı, normal çalışmada düşük gecikmeyi seçer
🤖 Cassandra bölünmede tutarlılığı değil kullanılabilirliği önceler; bu yüzden PC yanlıştır.
## D
PA/EL: bölünmede kullanılabilirliği, normal çalışmada düşük gecikmeyi seçer
🤖 Doğru: Dynamo soyundan Cassandra varsayılanı PA/EL profilindedir.
## Doğru
D
## Açıklama
**Cassandra**, Dynamo mimarisini izler ve varsayılanıyla **PA/EL** profilindedir: ağ **bölündüğünde** kullanılabilirliği (A) korur, düğümler yanıt vermeye devam eder; **normal çalışmada** ise kopyalar arası senkron koordinasyondan kaçınıp **düşük gecikmeyi** (L) önceler. Karşılığında güçlü tutarlılıktan ödün verilir. Ancak Cassandra **tunable consistency** sunar: istek başına `consistency level` (`ONE`, `QUORUM`, `ALL`) ile daha güçlü tutarlılığa doğru kayabilirsin — ör. yazma ve okuma kuorumları `W + R > N` olduğunda güçlü tutarlılığa yaklaşılır; bu da gecikme ve kullanılabilirlik bedeliyle gelir. PC/EC (A), PA/EC (B) ve PC/EL (C) bu davranışı yanlış tanımlar.
