---
slug: volatile-gorunurluk-atomiklik
baslik: "volatile: Görünürlük ve Atomiklik"
kademe: İleri
sira: 48
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

İki kişi ortak bir not defterine bakıyor diyelim. Birincisi deftere yazıyor, ama ikincisi hızlı olsun diye defterin kendi çektiği fotokopisine bakıyor. Yazı deftere geçse bile fotokopiye geçmez — ikinci kişi eski değeri okumaya devam eder. Bilgisayarda da her işlemci çekirdeğinin kendi önbelleği vardır ve derleyici, hızlansın diye değerleri yerel kopyada tutabilir. `volatile`, "bu değeri fotokopiden okuma, hep asıl deftere bak" demenin yoludur. Ama dikkat: aynı deftere iki kişinin AYNI ANDA yazmasını engellemez.

### Temel kavramlar

- **İş parçacığı (thread)**: aynı program içinde eşzamanlı ilerleyen yürütme akışı. Aynı belleği paylaşırlar.
- **Görünürlük (visibility)**: bir iş parçacığının YAZDIĞI değeri başka bir iş parçacığının GÖREBİLMESİ. CPU önbellekleri ve derleyici optimizasyonları yüzünden bu otomatik değildir.
- **Atomiklik (atomicity)**: bir işlemin **bölünmez** olması — arada başka bir iş parçacığı araya giremez.
- **Bileşik işlem (compound operation)**: tek satır gibi görünse de birden çok adımdan oluşan işlem. `sayac++` aslında üç adımdır: oku, +1, geri yaz.
- **Kayıp güncelleme (lost update)**: iki iş parçacığı aynı eski değeri okuyup aynı yeni değeri yazınca bir artışın yok olması.
- **Yeniden sıralama (reordering)**: derleyicinin ve işlemcinin, tek iş parçacığı açısından sonucu değiştirmediği sürece komutların sırasını değiştirmesi. Tek başına çalışan koda zarar vermez, paylaşılan veride felaket olur.
- **Önce gerçekleşir (happens-before)**: Java Bellek Modeli'nin "A'nın etkileri B'ye görünür" kuralı.
- **CAS (compare-and-swap, karşılaştır-ve-değiştir)**: donanım destekli "değer hâlâ X ise Y yap, değilse başarısız ol" komutu. Atomik oku-değiştir-yaz'ın temelidir.

`volatile` yalnız **görünürlüğü** ve **sıralamayı** garanti eder; **atomikliği vermez**. Bu ayrım mülakatın kalbidir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="volatile görünürlük verir, atomiklik vermez">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">volatile ne verir, ne vermez</text>
  <rect x="30" y="48" width="270" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="76" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">VERİR: Görünürlük</text>
  <text x="165" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">son yazılan değer</text>
  <text x="165" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">hemen okunur</text>
  <text x="165" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">+ yeniden-sıralama engeli</text>
  <rect x="340" y="48" width="270" height="120" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="475" y="76" text-anchor="middle" font-size="14" font-weight="700" fill="#B4322B" font-family="system-ui">VERMEZ: Atomiklik</text>
  <text x="475" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">oku-değiştir-yaz</text>
  <text x="475" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">(i++) bölünebilir</text>
  <text x="475" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">kayıp güncelleme riski</text>
  <text x="320" y="192" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Sayaç için: AtomicInteger ya da senkronizasyon.</text>
</svg>
```

### Nasıl çalışır?

**Görünürlük tarafı:** `volatile` bir alan yazmaçta ya da yerel kopyada saklanamaz; derleyici okumaları eleyemez. Yazılan son değer diğer iş parçacıklarına görünür olur.

**Sıralama tarafı:** bir `volatile` alana yazma, o alanı sonradan okuyan iş parçacığı için **önce gerçekleşmiş sayılır (happens-before)** — yazmadan ÖNCE yapılmış tüm diğer yazmalar da okuyana görünür olur. Derleyici ve işlemci bu erişimin etrafındaki bellek işlemlerini serbestçe yeniden sıralayamaz.

**Atomiklik tarafı — yok.** `sayac++` üç ayrı adımdır. `volatile` her adımın görünürlüğünü sağlar ama üçünü bölünmez yapmaz. İki iş parçacığı aynı değeri (ör. 5) okur, ikisi de 6 yazar → beklenen 7, gerçek 6; bir artış kaybolmuştur.

```svg
<svg viewBox="0 0 640 235" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İki thread sayac++ araya girer, bir artış kaybolur">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">sayac++ iki thread: kayıp güncelleme</text>
  <text x="60" y="44" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">başlangıç: sayac = 5</text>
  <text x="600" y="44" text-anchor="end" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">sonuç: sayac = 6</text>
  <text x="34" y="82" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">T1</text>
  <text x="34" y="166" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">T2</text>
  <rect x="70" y="62" width="90" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="115" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku: 5</text>
  <rect x="300" y="62" width="110" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="355" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">5 + 1 = 6</text>
  <rect x="470" y="62" width="90" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="515" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz: 6</text>
  <rect x="180" y="146" width="90" height="34" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="225" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku: 5</text>
  <rect x="300" y="146" width="110" height="34" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="355" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">5 + 1 = 6</text>
  <rect x="470" y="146" width="90" height="34" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="515" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz: 6</text>
  <text x="320" y="212" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">İkisi de 5 okur → beklenen 7, gerçek 6: bir artış KAYBOLDU</text>
</svg>
```

Atomiklik gerekiyorsa `AtomicInteger` gibi **CAS temelli** bir sınıf ya da kilit (`synchronized`) gerekir.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="volatile üç adımlı bölünebilir; AtomicInteger CAS tek atomik adım">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sayaç: volatile (yetmez) vs AtomicInteger (CAS)</text>
  <rect x="24" y="42" width="285" height="120" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="166" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">volatile int sayac</text>
  <rect x="40" y="80" width="76" height="30" rx="5" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.1"/>
  <text x="78" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku</text>
  <rect x="126" y="80" width="76" height="30" rx="5" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.1"/>
  <text x="164" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">+1</text>
  <rect x="212" y="80" width="76" height="30" rx="5" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.1"/>
  <text x="250" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaz</text>
  <text x="166" y="140" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">3 ayrı adım → araya girilebilir</text>
  <text x="166" y="156" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">(lost update)</text>
  <rect x="331" y="42" width="285" height="120" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">AtomicInteger</text>
  <rect x="360" y="80" width="228" height="30" rx="5" fill="#EAF1F7" stroke="#25804C" stroke-width="1.1"/>
  <text x="474" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">CAS: karşılaştır-ve-değiştir</text>
  <text x="473" y="140" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">tek atomik adım →</text>
  <text x="473" y="156" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kayıp güncelleme yok</text>
  <text x="320" y="190" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Görünürlük ≠ atomiklik</text>
</svg>
```

```java
// YETER: tek yazan, tek okuyan, bileşik işlem yok → yalnız görünürlük gerekli
volatile boolean calisiyor = true;

// YETMEZ: oku-değiştir-yaz bileşiktir → kayıp güncelleme olur
volatile int sayac;
void artir() { sayac++; }

// DOĞRU: CAS ile tek atomik adım
final AtomicInteger sayacAtomik = new AtomicInteger();
void artirDogru() { sayacAtomik.incrementAndGet(); }
```

İki özel durum daha bilinmelidir:

- **64-bit ilkeller**: `volatile` OLMAYAN bir `long`/`double` yazması iki ayrı 32-bit yazmaya bölünebilir. Buna spesifikasyon izin verir (JLS §17.7, "`double` ve `long`'un atomik olmayan işlenmesi"). O sırada başka bir iş parçacığı, üst 32 biti yeni alt 32 biti eski değerden olan **yırtık (torn) bir ara değer** okuyabilir; yani hiç yazılmamış bir sayı görürsünüz. `volatile` bu tek okuma/yazmayı atomik yapar. `int` ve daha küçük tipler ile referanslar zaten atomiktir; yırtılma yalnız 64-bit ilkellere özgüdür. (Bu, komşu alanların birbirini bozması anlamına gelen **"word tearing" DEĞİLDİR**; JLS word tearing'i açıkça yasaklar.) Modern 64-bit JVM'lerde pratikte nadir görülse de spesifikasyon garantisi ancak `volatile` ile gelir; JVM böyle alanları kendiliğinden kilitlemez ve otomatik `volatile` yapmaz.
- **Güvenli yayımlama ve çift kontrollü kilitleme (double-checked locking)**: tembel başlatmada alan `null` mı diye kilit DIŞINDA okunur, kilit yalnız ilk kurulumda alınır. Alan `volatile` DEĞİLSE, yapıcıdaki (constructor) yazmalar ile referans ataması yeniden sıralanabilir ve başka bir iş parçacığı **yarı-inşa edilmiş** bir nesne görebilir. `volatile`, referans görünür olmadan önce yapıcının tüm yazmalarının tamamlanmasını garanti eder. `static` bu sıralama garantisini vermez; yalnız `synchronized` da yetmez (içteki atama kilit dışında okunuyor); `final` ise tembel atamayla bağdaşmaz — alan sonradan atanacaktır. Statik alanlar için modern pratikte daha yalın olan "holder idiom" (statik iç sınıf) sıkça tercih edilir.

### Mülakatta ne sorulur?

**`volatile` ne SAĞLAR?** Görünürlük + sıralama (happens-before). Ne SAĞLAMAZ: atomiklik (`i++` yine bölünebilir), karşılıklı dışlama (kilit değildir), değişmezlik (alanı salt-okunur yapmaz).

**Dur bayrağı (stop flag).** Bir iş parçacığı `boolean calisiyor` bayrağını yazıyor, diğeri döngüde okuyorsa — bileşik işlem yok, yalnız görünürlük gerekli — en yalın doğru çözüm bayrağı `volatile` yapmaktır. `static` yapmak alanı yalnız PAYLAŞILIR kılar, görünürlüğü GARANTİ ETMEZ; derleyici/JIT okumayı döngü dışına taşıyabilir ya da okuyan iş parçacığı değeri yerel kopyada tutup sonsuza dek dönebilir. "JVM er geç kendiliğinden görür" diye bir garanti yoktur; `System.gc()` çağırmak ise tamamen ilgisiz ve pahalıdır.

**Sayaç için ne kullanmalı?** `AtomicInteger.incrementAndGet()` — CAS ile tek, bölünmez adım. Çok yüksek çekişmeli (contended) durumlarda `LongAdder` daha iyi ölçeklenir: sayımı hücrelere böler, her hücreyi CAS ile atomik günceller, toplamı okurken birleştirir. Yanlış alternatifler üç tanedir. `volatile long`'a geçmek işe yaramaz; tip büyütmek atomiklik vermez, `long++` yine oku-değiştir-yaz'dır. `transient` yalnız serileştirmeyi ilgilendirir, eşzamanlılıkla ilgisi yoktur. Sayacı iki `volatile int` alana bölüp rastgele birini artırmak çekişmeyi dağıtır ama her alan hâlâ atomik olmayan `++` ile artırılır, yani kayıp güncelleme sürer; bu fikrin DOĞRU uygulaması zaten `LongAdder`'dır. Artışı `synchronized` bloğa almak da geçerlidir; ama tek bir sayaç için `AtomicInteger` en yalın ve en performanslı çözümdür.

**`volatile int sayac; sayac++` neden bozuk?** Kod derlenir (yazma yasak değildir), sorun performans da değildir — SONUÇ yanlıştır: eşzamanlı artışlar kaybolur. "Sayaç sıfırlanır" gibi bir davranış yoktur.

### Tek cümlede

`volatile` bir alanın son değerinin diğer iş parçacıklarına görünmesini ve etrafındaki işlemlerin yeniden sıralanmamasını garanti eder, ama `i++` gibi oku-değiştir-yaz işlemlerini atomik yapmaz — onun için `AtomicInteger`/`LongAdder` (CAS) ya da kilit gerekir.

# vol-1 | İleri
## Soru
`volatile` bir alan (field) tek başına hangi garantiyi SAĞLAR?
## A
Alan üzerindeki tüm işlemleri — `i++` dahil — bölünmez (atomik) hale getirir
🤖 Atomiklik `volatile`'in işi değil; `i++` gibi bileşik işlemler yine bölünebilir.
## B
Yazılan en son değerin diğer iş parçacıklarınca görülmesini ve sıralamayı (happens-before) garanti eder
🤖 Doğru: görünürlük + sıralama garantisi verir, atomiklik vermez.
## C
Alana erişimi tek iş parçacığıyla sınırlar; kilit gibi karşılıklı dışlama sağlar
🤖 `volatile` kilit değildir; karşılıklı dışlama (mutual exclusion) sağlamaz.
## D
Alanı salt-okunur yapıp ilk atamadan sonra değiştirilmesini engeller
🤖 `volatile` değiştirilebilirliği etkilemez; sadece okuma/yazma görünürlüğünü düzenler.
## Doğru
B
## Açıklama
`volatile` iki şey verir: (1) **görünürlük** — yazılan değer diğer iş parçacıklarına anında görünür olur; derleyici onu yazmaçta/yerel kopyada saklayıp okumaları eleyemez; (2) **sıralama** — `volatile` erişiminin etrafındaki bellek erişimleri serbestçe yeniden-sıralanamaz; volatile yazma, sonraki volatile okumadan **önce gerçekleşmiş sayılır (happens-before)**, dolayısıyla yazmadan önceki diğer yazmalar da okuyana görünür. Ama **atomiklik VERMEZ**: `i++` gibi oku-değiştir-yaz işlemleri hâlâ bölünebilir (A yanlış). Kilit de değildir (C yanlış) ve değişkenliği etkilemez (D yanlış). Bu ayrımı bilmek, `volatile`'i nerede kullanıp nerede kullanmamak gerektiğini belirler.

# vol-2 | İleri
## Soru
`volatile int sayac;` alanında `sayac++` çok iş parçacığından çağrılırsa neden güvenli değildir?
## A
`volatile` alanlara yazma yasak olduğu için kod derlenmez, hata alınır
🤖 `volatile` yazmayı engellemez; kod derlenir, sorun çalışma-zamanı yarışıdır.
## B
`sayac++` üç adımdır (oku-değiştir-yaz); araya giren iş parçacığı güncelleme kaybına yol açar
🤖 Doğru: bileşik işlem atomik değil, araya girme kayıp güncelleme üretir.
## C
`volatile` değerleri önbelleğe almadığı için performans düşer ama sonuç doğrudur
🤖 Sorun performans değil; sonuç YANLIŞ olabilir (kayıp artış).
## D
`sayac` bir sonraki okumada her zaman sıfırlanıp baştan saymaya başlar
🤖 Sıfırlanma diye bir davranış yok; sorun eşzamanlı artışların kaybolmasıdır.
## Doğru
B
## Açıklama
`sayac++` tek bir işlem gibi görünse de aslında **üç adımdır**: oku, +1 ekle, geri yaz. `volatile` her adımın GÖRÜNÜRLÜĞÜNÜ sağlar ama üç adımı BÖLÜNMEZ yapmaz. İki iş parçacığı aynı anda aynı değeri (ör. 5) okur, ikisi de 6 yazar → bir artış KAYBOLUR (lost update). Doğru çözüm `AtomicInteger.incrementAndGet()` (atomik oku-değiştir-yaz) veya artışı `synchronized`/kilit içine almaktır. Bu, "görünürlük ≠ atomiklik" ilkesinin en somut örneğidir.

# vol-3 | Orta
## Soru
Bir iş parçacığını döngüde döndüren `boolean calisiyor` bayrağını, başka bir iş parçacığı `false` yapınca döngünün durması için en yalın doğru çözüm nedir?
## A
Bayrağı `static boolean calisiyor` yapıp iki iş parçacığının aynı alanı görmesini sağlamak
🤖 `static` yalnız paylaşımı sağlar; görünürlüğü garanti etmez, döngü yine takılabilir.
## B
Hiçbir şey yapmaya gerek yok; JVM değişikliği er geç kendiliğinden görür
🤖 Böyle bir garanti yok; okuyan iş parçacığı eski değerle sonsuza dek dönebilir.
## C
Bayrağı `volatile boolean calisiyor` olarak tanımlamak
🤖 Doğru: tek yazan/okuyan bir bayrakta görünürlük yeterlidir, `volatile` bunu verir.
## D
Döngünün her turunda `System.gc()` çağırıp belleğin tazelenmesini sağlamak
🤖 Çöp toplamanın görünürlükle ilgisi yoktur; bayrağı tazelemez, üstelik pahalıdır.
## Doğru
C
## Açıklama
Klasik "dur bayrağı" (stop flag) senaryosu tam olarak `volatile`'in çözdüğü sorundur: bir iş parçacığı bayrağı YAZAR, diğeri OKUR ve bileşik bir işlem yoktur — yani yalnız **görünürlük** gerekir, atomiklik değil. `volatile` olmadan, derleyici/JIT bayrağın okunmasını döngü dışına taşıyabilir ya da okuyan iş parçacığı değeri yerel kopyada tutup değişikliği hiç görmeyebilir; sonuç sonsuz döngüdür (B yanlış). `static` (A) alanı paylaşılır kılar ama görünürlüğü GARANTİ ETMEZ; `System.gc()` (D) ise tamamen ilgisizdir. Bu yüzden en yalın ve doğru çözüm bayrağı `volatile` yapmaktır.

# vol-4 | Uzman
## Soru
`volatile` olmayan bir `long` veya `double` alanına yazma ile ilgili hangi ifade doğrudur?
## A
Spesifikasyon 64-bit yazmanın her platformda atomik olduğunu garanti eder
🤖 JLS bunu garanti etmez; `volatile` olmayan 64-bit yazma iki 32-bit yarıya bölünebilir.
## B
Yazma iki 32-bit işleme bölünebilir; başka iş parçacığı "yarısı eski, yarısı yeni" bir değer okuyabilir
🤖 Doğru: JLS 64-bit atomikliğini yalnız `volatile` alanlar için garanti eder.
## C
`long` yazmaları `int` yazmalarından yavaş olduğu için JVM bunları kilit altına alır
🤖 Otomatik kilitleme yoktur; sorun performans değil, spesifikasyondaki atomiklik garantisidir.
## D
Derleyici, birden çok iş parçacığından erişilen böyle alanları otomatik `volatile` yapar
🤖 Böyle bir otomatik dönüşüm yoktur; garanti istiyorsan `volatile`'ı sen yazmalısın.
## Doğru
B
## Açıklama
Java Bellek Modeli (JLS §17.7, "`double` ve `long`'un atomik olmayan işlenmesi"), `volatile` OLMAYAN bir `long`/`double` yazmasının JVM tarafından **iki ayrı 32-bit yazmaya** bölünmesine izin verir. Bu durumda başka bir iş parçacığı, üst 32 biti yeni değerden alt 32 biti eski değerden olan **yırtık (torn) bir ara değer** okuyabilir — hiç yazılmamış bir sayı görürsünüz. `volatile` işaretlenirse 64-bit okuma/yazma **atomik** olur ve bu risk ortadan kalkar. Not: `int` ve daha küçük tipler ile referans yazmaları zaten atomiktir; bu yırtılma yalnız 64-bit ilkellere özgüdür. Modern 64-bit JVM'lerde pratikte nadir görülse de spesifikasyon garantisi ancak `volatile` ile gelir. JVM böyle alanları kendiliğinden kilitlemez (C) ve otomatik `volatile` yapmaz (D).

# vol-5 | Uzman
## Soru
Tembel başlatmada (lazy initialization) "çift kontrollü kilitleme" (double-checked locking) deseninin DOĞRU çalışması için alan nasıl tanımlanmalıdır?
## A
Alan `volatile` olmalıdır
🤖 Doğru: volatile olmadan yarı-kurulmuş nesne başka iş parçacığına görünebilir.
## B
Alan `static` olması yeterlidir
🤖 `static` yeniden-sıralamayı engellemez; yarı-inşa edilmiş nesne sızabilir.
## C
Alan `final` olmalıdır
🤖 `final` tembel atamayla bağdaşmaz; alan sonradan atanacağı için `final` olamaz.
## D
Hiçbir değiştirici gerekmez; `synchronized` blok her şeyi halleder
🤖 İçteki atama kilit dışında okunduğu için görünürlük/sıralama garantisi eksik kalır.
## Doğru
A
## Açıklama
Çift kontrollü kilitlemede alan `null` mı diye kilit DIŞINDA okunur; kilit yalnız ilk kurulumda alınır. Alan `volatile` DEĞİLSE, nesne yapıcısındaki (constructor) yazmalar ile referans atamasının **yeniden-sıralanması** yüzünden başka bir iş parçacığı henüz TAM kurulmamış (yarı-inşa) bir nesneyi görebilir. `volatile`, referans atamasından önce yapıcının tüm yazmalarının tamamlanmasını (happens-before) garanti eder ve bu yarış ortadan kalkar. `static` (B) ve yalnız `synchronized` (D) bu sıralama garantisini vermez; `final` (C) tembel atamayla bağdaşmaz. Modern pratikte, statik alanlar için daha yalın olan "holder idiom" (statik iç sınıf) sıkça tercih edilir.

# vol-6 | İleri
## Soru
Birden çok iş parçacığının GÜVENLE artırdığı bir sayaç için `volatile int` yerine ne kullanılmalıdır?
## A
Daha geniş bir tip: `volatile long` — 64 bit olduğu için artış bölünmez olur
🤖 Tip büyütmek atomikliği vermez; `long++` da oku-değiştir-yaz'dır, yarış sürer.
## B
`transient int` — alan kalıcılaştırılmadığı için iş parçacıkları arasında bozulmaz
🤖 `transient` yalnız serileştirmeyi (serialization) etkiler; eşzamanlılıkla ilgisi yoktur.
## C
Sayacı iki `volatile int` alana bölüp her artışta rastgele birini artırmak, okurken toplamak
🤖 Çekişme azalır ama her alan hâlâ atomik-değil artırılır; kayıp güncelleme sürer.
## D
`AtomicInteger` (ör. `incrementAndGet`) ya da yüksek çekişmede `LongAdder`
🤖 Doğru: CAS temelli atomik oku-değiştir-yaz, kayıp güncellemeyi önler.
## Doğru
D
## Açıklama
Sayaç, oku-değiştir-yaz (compound) bir işlem gerektirir; bu yüzden görünürlük tek başına (yani `volatile`) yetmez — **atomiklik** gerekir. `AtomicInteger.incrementAndGet()` donanım destekli **CAS (compare-and-swap)** ile artışı tek, bölünmez adımda yapar ve kayıp güncellemeyi (lost update) önler; çok yüksek çekişmede `LongAdder` daha iyi ölçeklenir. `volatile long` (A) tipi büyütür ama işlemi atomik yapmaz. `transient` (B) yalnız serileştirmeyi ilgilendirir. Sayacı iki `volatile int` alana bölmek (C) çekişmeyi dağıtır ama her alan hâlâ atomik olmayan `++` ile artırıldığından kayıp güncelleme sürer — sorun bölünür, çözülmez. (Aynı fikrin DOĞRU uygulaması `LongAdder`'dır: hücreleri CAS ile atomik günceller.) Alternatif olarak artış `synchronized`/kilit içine de alınabilir; ama tek bir sayaç için `AtomicInteger` en yalın ve en performanslı çözümdür.
