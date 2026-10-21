---
slug: jmm-happens-before
baslik: "Java Bellek Modeli: happens-before ve Görünürlük"
kademe: İleri
sira: 45
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Aynı ofiste iki kişi çalışıyor ve ortak bir panoya not bırakıyorlar. Ama ikisi de hız olsun diye önce kendi masasındaki deftere yazıyor; panoya ne zaman geçireceği belli değil. Ali "yazdım" dese bile Veli hâlâ eski notu okuyor olabilir, hatta Ali'nin iki notu panoya ters sırada geçmiş olabilir. Bilgisayarda durum aynıdır: her işlemci çekirdeğinin kendi hızlı önbelleği vardır ve derleyici kod hızlansın diye komutların sırasını değiştirebilir. Java Bellek Modeli (JMM), "bir iş parçacığının yazdığını diğeri ne zaman kesin görür ve hangi sıra değişikliklerine izin var" sorusunun kural kitabıdır.

### Temel kavramlar

- **İş parçacığı (thread)**: aynı program içinde bağımsız ilerleyen yürütme hattı. İş parçacıkları aynı belleği paylaşır — güç de tehlike de buradan gelir.
- **Paylaşılan durum (shared state)**: birden çok iş parçacığının eriştiği alan ya da nesne.
- **Çekirdek (core) ve önbellek (cache)**: işlemci her okuma için yavaş ana belleğe gitmez; sık kullandığı veriyi kendi küçük ve hızlı önbelleğinde tutar, yazmaları bir yazma tamponunda (store buffer) biriktirebilir. Bu yüzden bir yazma, yapıldığı anda herkese görünmez.
- **Görünürlük (visibility)**: bir iş parçacığının yazdığı değerin, başka bir iş parçacığınca güncel hâliyle görülebilmesi. Hiçbir tipte otomatik değildir.
- **Atomiklik (atomicity)**: bir işlemin bölünmez olması, ortasına başka iş parçacığının girememesi. `sayac++` tek adım gibi görünür ama üç adımdır: oku → 1 ekle → geri yaz (bileşik oku-değiştir-yaz işlemi).
- **Yeniden sıralama (reordering)**: derleyicinin (JIT) ve işlemcinin (sıra-dışı yürütme, yazma tamponu) işlem sırasını değiştirmesi. Kural: **tek bir iş parçacığında** sonucu değiştirmeyen her sıralama serbesttir; amaç yavaşlatmak değil hızlandırmaktır.
- **Veri yarışı (data race)**: iki iş parçacığının aynı alanı senkronizasyon olmadan kullanması ve en az birinin yazması. Sonuç JMM'ce tanımsızdır.
- **`volatile`**: bir alanı "değerini önbellekte saklama, çevresine **bellek bariyeri** (memory barrier — derleyici ve işlemcinin işlemleri öte tarafa taşımasını yasaklayan sınır) koy" diye işaretler. Görünürlük ve sıralama verir; atomiklik vermez.
- **happens-before**: JMM'nin temel ilişkisi. A işlemi B ile bu ilişkideyse, A'nın bellek etkileri B çalışırken **kesin görünür**dür ve arada bu görünürlüğü bozacak yeniden sıralama yapılamaz.

```java
int sayac = 0;      // iki iş parçacığı 100_000'er kez sayac++ yapsın
// beklenen 200000 — gerçek çıktı çoğu koşuda daha küçük, ör. 143728
```

### Nasıl çalışır?

happens-before bir zaman ölçüsü değildir. "A duvar saatine göre önce çalıştı" ya da "A ile B aynı anda yürür" demez. İş parçacıklarının nasıl dağıtılacağı hakkında da bir söz vermez ve **atomiklik sağlamaz**. Yalnızca "hangi yazmalar hangi okumalara görünür" sorusunu belirlenimci biçimde yanıtlar. İlişkiyi kuran kurallar şunlardır:

1. **Program sırası** — tek bir iş parçacığı içinde önce gelen işlem, sonrakine happens-before'dur (yani ilişki aynı iş parçacığında da kurulur).
2. **`volatile`** — bir `volatile` alana yazma, aynı alanın sonraki okumasına.
3. **Kilit (monitor)** — kilidi bırakma, aynı kilidi sonradan alma işlemine.
4. **`Thread.start()`** — start çağrısından önceki her şey, başlatılan iş parçacığının tüm işlemlerine.
5. **`Thread.join()`** — biten iş parçacığının tüm işlemleri, onu bekleyen `join()`'in dönüşüne.

İlişki **geçişlidir** (transitive): A→B ve B→C ise A→C de geçerlidir; uzun zincirler böyle kurulur. İki işlem arasında happens-before YOKSA, birinin diğerinin yazdığını görüp görmeyeceği tanımsızdır.

En sık örnek bayrak (flag) desenidir: bir tarafta `veri = 42; hazir = true;`, diğer tarafta `while (!hazir) {} System.out.println(veri);`. `hazir` `volatile` değilse iki ayrı hata birden çıkar. (1) **Görünürlük** — okuyan iş parçacığı `hazir`'ı kendi önbelleğinde hep `false` görüp sonsuza dek dönebilir. (2) **Sıralama** — `hazir`'ı `true` görse bile `veri = 42` yazması reordering yüzünden henüz görünür olmayabilir; `veri` hâlâ `0` okunur. `hazir` `volatile` yapılınca yazma→okuma bir happens-before kurar ve **`hazir = true`'dan ÖNCEKİ tüm normal yazmalar** okuyana görünür olur. `volatile`'in asıl gücü budur: yalnız kendi alanını değil, kendinden öncekileri de taşır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="happens-before ile iki iş parçacığı arasında görünürlük">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">volatile ile kurulan happens-before</text>
  <rect x="40" y="50" width="240" height="130" rx="12" fill="#EEF3FA" stroke="#2C5B8A" stroke-width="1.5"/>
  <text x="160" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#2C5B8A" font-family="system-ui">İş parçacığı 1</text>
  <text x="160" y="102" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">veri = 42;</text>
  <text x="160" y="126" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">hazir = true;  (volatile yaz)</text>
  <text x="160" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yazmalar bu noktada işlenir</text>
  <rect x="360" y="50" width="240" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">İş parçacığı 2</text>
  <text x="480" y="102" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">if (hazir)  (volatile oku)</text>
  <text x="480" y="126" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">veri == 42  GARANTİ</text>
  <text x="480" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önceki yazmalar görünür</text>
  <path d="M282 115 L358 115" stroke="#B4322B" stroke-width="2" marker-end="url(#jmm-ok)"/>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B4322B" font-family="system-ui">happens-before</text>
  <defs>
    <marker id="jmm-ok" markerWidth="9" markerHeight="9" refX="7" refY="3.5" orient="auto">
      <path d="M0 0 L7 3.5 L0 7 z" fill="#B4322B"/>
    </marker>
  </defs>
  <text x="320" y="205" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Bayrağın görünürlüğü, ondan ÖNCEKİ tüm yazmaları taşır.</text>
</svg>
```

Yeniden sıralamanın klasik tuzağı `nesne = new Nesne();` satırıdır. Bu satır üç adıma ayrılır: bellek ayır, yapıcıyı çalıştır, referansı ata. Sıralama değişip referans ataması yapıcıdan önce görünür olursa, başka bir iş parçacığı **yarı kurulmuş** bir nesne görebilir. Bu yüzden çift kontrollü kilitlemede (double-checked locking) alan `volatile` olmak zorundadır.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="new Nesne() yeniden sıralama tehlikesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">nesne = new Nesne(); üç adıma ayrılır</text>
  <rect x="40" y="46" width="250" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Beklenen sıra</text>
  <text x="165" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1. bellek ayır</text>
  <text x="165" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">2. yapıcıyı çalıştır</text>
  <text x="165" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">3. referansı ata</text>
  <text x="165" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">nesne tam kurulmuş</text>
  <rect x="350" y="46" width="250" height="150" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Reordering sonrası</text>
  <text x="475" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1. bellek ayır</text>
  <text x="475" y="118" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">3. referansı ata (erken!)</text>
  <text x="475" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">2. yapıcıyı çalıştır</text>
  <text x="475" y="170" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">2. iş parçacığı yarı-nesne görür</text>
  <text x="320" y="220" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Çözüm: volatile alan bariyeri (çift kontrollü kilit).</text>
</svg>
```

### Mülakatta ne sorulur?

- **Görünürlük ≠ atomiklik.** `volatile` "en son değeri görürsün" der, "arada kimse giremez" demez; ikisi ayrı ve bağımsız garantidir, biri diğerini kapsamaz. Bir dur bayrağı (stop flag) için `volatile` yeter — tek yazma, tek okuma. `sayac++` için `AtomicInteger` ya da kilit gerekir. "volatile kullandım ama sayacım hâlâ yanlış" tuzağı buradan doğar. Ek ayrıntı: `volatile` olmayan 64 bitlik `long`/`double` yazması iki yarıya bölünebilir — ama bileşik işlem sorunu her tipte vardır ve görünürlük de hiçbir tipte kendiliğinden gelmez.
- **Yeniden sıralamayı kim yapar?** Hem JIT derleyicisi hem işlemci (yazma tamponu, sıra-dışı yürütme); tek iş parçacıklı programda yanlış sonuç üretmez.
- **False sharing (yanlış paylaşım)**: işlemci belleği tek tek baytlarla değil, ~64 baytlık **önbellek satırları** (cache line) hâlinde alır. Mantıken bağımsız iki değişken (ör. dizide yan yana iki sayaç) aynı satıra düşerse, bir çekirdek yazdığında diğer çekirdeğin kopyası bütünüyle geçersiz kılınır. Veri yarışı YOKTUR, sonuç doğrudur, veri de kaybolmaz — ama satır çekirdekler arasında sürekli seğirttiği için performans çöker. Çözüm: dolgu (padding) ile ayrı satırlara yaymak, `@Contended` (JDK dahili) ya da çekirdek başına sayaç (`LongAdder`).
- **`final` alan güvenliği (safe publication)**: yapıcı `final` alanları ayarlayıp **düzgün biterse**, nesne referansını gören her iş parçacığı o alanların başlangıç değerlerini de doğru görür. Koşul, referansın yapıcı içinde dışarı sızmamasıdır; bu sağlanınca ek `volatile` ya da kilit gerekmez. Garanti, yapıcı sonundaki tek seferlik "dondurma"dan (freeze) gelir — her erişimde bariyer konmaz, alanlar önbelleğe alınabilir ve `final` örnek alanları statik bir bölgede değil nesnenin içinde durur. `this` yapıcı bitmeden bir listeye ya da iş parçacığına verilirse garanti bozulur. Bu kural, `String`/`Integer` gibi değişmez (immutable) nesnelerin neden senkronizasyonsuz paylaşılabildiğinin temelidir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Görünürlük ile atomiklik farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Görünürlük ≠ Atomiklik</text>
  <rect x="30" y="42" width="270" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Görünürlük</text>
  <text x="165" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"en son değeri görürsün"</text>
  <text x="165" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">dur bayrağı: volatile hazir</text>
  <text x="165" y="140" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">volatile YETER</text>
  <text x="165" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek yazma, tek okuma</text>
  <rect x="340" y="42" width="270" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="475" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Atomiklik</text>
  <text x="475" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"arada kimse giremez"</text>
  <text x="475" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sayac++ (oku-değiştir-yaz)</text>
  <text x="475" y="140" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">volatile YETMEZ</text>
  <text x="475" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">AtomicInteger / kilit gerekir</text>
</svg>
```

### Tek cümlede

JMM, iş parçacıkları arasında "kim neyi ne zaman görür"ü happens-before ile tanımlar: ilişki kurulmadıkça gördüğün her şey rastlantıdır, kurulduğunda görünürlük garanti olur — ama atomiklik hâlâ ayrı bir iştir.

# jmm-1 | Orta
## Soru
JMM'de "happens-before" ilişkisi ne garanti eder?
## A
A işlemi B ile happens-before ilişkisindeyse, A'nın bellek etkileri (yazmaları) B tarafından kesin görünürdür
🤖 Doğru: happens-before bir görünürlük + sıralama garantisidir.
## B
A ve B'nin duvar saatine göre aynı anda çalışacağını, yani eşzamanlı yürütüleceğini garanti eder
🤖 Zamanlamayla ilgili değil; bir sıralama ve görünürlük ilişkisidir, A gerçekte daha geç bile çalışabilir.
## C
A ve B'nin farklı iş parçacıklarında çalışmasını zorunlu kılar, aynı iş parçacığında kurulamaz
🤖 Tek bir iş parçacığındaki program sırası da bir happens-before kuralıdır; dağılımı zorlamaz.
## D
A ile B arasındaki tüm işlemleri atomik yaparak araya başka iş parçacığının girmesini engeller
🤖 Atomiklik ya da karşılıklı dışlama sağlamaz; yalnızca görünürlük ve sıralama tanımlar.
## Doğru
A
## Açıklama
happens-before, JMM'nin temel ilişkisidir: A işlemi B ile bu ilişkideyse, **A'nın belleğe yazdıkları B çalışırken kesin görünür** ve derleyici/CPU A ile B'yi bu görünürlüğü bozacak biçimde yeniden sıralayamaz. Bu ilişkiyi kuran kurallar vardır — tek iş parçacığında program sırası, `volatile` yaz→oku, kilit bırak→al, `Thread.start`, `join` vb. happens-before **eşzamanlılık** (B) ya da **iş parçacığı dağılımı** (C) hakkında bir şey söylemez; ayrıca **atomiklik de vermez** (D) — sadece "hangi yazmalar hangi okumalara görünür" sorusunu belirlenimci biçimde yanıtlar. İki işlem arasında happens-before YOKSA, bir iş parçacığının diğerinin yazdığını görüp görmeyeceği tanımsızdır (veri yarışı).

# jmm-2 | Orta
## Soru
Görünürlük (visibility) ile atomiklik (atomicity) arasındaki fark nedir?
## A
İkisi aynı şeydir; `volatile` her ikisini de sağlar
🤖 Aynı değildir; `volatile` atomikliği vermez.
## B
Atomiklik görünürlüğü kapsar, tersi geçerli değildir
🤖 Kapsama ilişkisi değil; ayrı garantilerdir.
## C
Görünürlük "bir yazmanın diğer iş parçacıklarınca en güncel haliyle görülmesi"; atomiklik ise "bir işlemin bölünmez tek adım" olmasıdır
🤖 Doğru: iki ayrı ve bağımsız garanti.
## D
Görünürlük her tip için otomatiktir; atomiklik ise yalnızca 64 bitlik `long`/`double` yazmalarında sorun olur
🤖 Görünürlük hiçbir tipte otomatik değildir; atomiklik sorunu her tipte `sayac++` gibi bileşik işlemlerde de vardır.
## Doğru
C
## Açıklama
Bu ayrım eşzamanlılığın kalbidir. **Görünürlük**: bir iş parçacığının yazdığı değerin, başka bir iş parçacığınca güncel haliyle GÖRÜLEBİLMESİ — CPU önbellekleri ve derleyici optimizasyonları yüzünden bu otomatik değildir. **Atomiklik**: bir işlemin BÖLÜNMEZ olması, yani ortasında başka bir iş parçacığının araya girememesi. `volatile` yalnızca görünürlüğü (ve sıralamayı) verir; `sayac++` gibi oku-değiştir-yaz'ı atomik yapmaz. Bir "dur bayrağı" (stop flag) için görünürlük yeterlidir (`volatile`); bir sayaç için atomiklik gerekir (`AtomicInteger` veya kilit). İki garantiyi karıştırmak, "volatile kullandım ama sayacım hâlâ yanlış" tuzağının kaynağıdır.

# jmm-3 | İleri
## Soru
Yeniden sıralama (reordering) neden bir sorun olabilir?
## A
Yeniden sıralama, ek kontroller gerektirdiği için kodu daima yavaşlatır
🤖 Amacı hızlandırmaktır; sorun performans değil, görünürlüktür.
## B
happens-before kısıtı olmadıkça derleyici/CPU işlemleri yeniden sıralayabilir; senkronizasyonsuz okuyan iş parçacığı beklenmedik ara durumlar görebilir
🤖 Doğru: reordering tek iş parçacığında zararsız ama paylaşımlı durumda şaşırtıcıdır.
## C
Yeniden sıralama yalnızca JIT derleyicisinin işidir; işlemci komutları her zaman yazıldığı sırada yürütür, sıra-dışı yürütme yapmaz
🤖 İşlemci de yazma tamponu ve sıra-dışı yürütmeyle sıralamayı değiştirir; iki kaynak da vardır.
## D
Yeniden sıralama, tek iş parçacıklı programlarda bile ifade sırasını bozup yanlış sonuç üretebilir
🤖 Hayır; kural, tek iş parçacığında sonucu değiştirmeyecek sıralamaların serbest olmasıdır.
## Doğru
B
## Açıklama
Derleyici ve işlemci, performans için işlemleri yeniden sıralar veya yazmaları önbellekte geciktirir; kural şudur: **tek bir iş parçacığında** sonucu değiştirmeyecek her sıralama serbesttir. Sorun, paylaşımlı durumu senkronizasyonsuz okuyan İKİNCİ bir iş parçacığında ortaya çıkar. Klasik örnek: `nesne = new Nesne();` üç adıma ayrılır — bellek ayır, yapıcıyı çalıştır, referansı ata. Reordering, referans atamasını yapıcıdan ÖNCE görünür kılarsa, başka bir iş parçacığı henüz TAM kurulmamış bir nesneyi görebilir. happens-before ilişkisi (volatile, kilit, final) bu sıralamalara bariyer koyarak sorunu ortadan kaldırır. Bu yüzden çift kontrollü kilitlemede alan `volatile` olmalıdır.

# jmm-4 | İleri
## Soru
Aşağıdaki senkronizasyonsuz kod neden güvenilir değildir?

```java
boolean hazir = false;   // volatile DEĞİL
int veri = 0;
// İş parçacığı 1:  veri = 42; hazir = true;
// İş parçacığı 2:  while (!hazir) {}  System.out.println(veri);
```
## A
`while` döngüsü sözdizimsel olarak hatalıdır
🤖 Kod derlenir; sorun sözdizimi değil bellek görünürlüğüdür.
## B
`veri` her zaman `0` yazdırır çünkü `int` paylaşılamaz
🤖 `int` paylaşılabilir; sorun "her zaman 0" değil, garantisizliktir.
## C
`veri` yazması `hazir` yazmasından önce yapıldığı için diğer iş parçacığına garanti görünür, kod doğrudur
🤖 Yanlış; `volatile` olmadan bu sıralama/görünürlük garanti DEĞİL.
## D
`hazir` `volatile` olmadığı için happens-before kurulmamıştır; İş parçacığı 2 sonsuz dönebilir ya da `veri`'yi `0` görebilir
🤖 Doğru: iki ayrı garanti (döngü görünürlüğü + veri sıralaması) da eksiktir.
## Doğru
D
## Açıklama
Bu kodda İş parçacığı 1 ile 2 arasında **hiçbir happens-before ilişkisi** kurulmamıştır, çünkü `hazir` `volatile` değil ve kilit yok. İki bağımsız hata çıkar: (1) **Görünürlük** — İş parçacığı 2, `hazir`'ı kendi önbelleğinde `false` görüp değişikliği hiç fark etmeyebilir ve sonsuza dek döner. (2) **Sıralama** — İş parçacığı 2 `hazir`'ı `true` görse bile, `veri = 42` yazması reordering yüzünden henüz görünür olmayabilir; `veri` hâlâ `0` okunabilir. Çözüm `hazir`'ı `volatile` yapmaktır: `volatile` yazma → okuma bir happens-before kurar ve `hazir=true`'dan ÖNCEKİ tüm yazmalar (yani `veri=42`) okuyan iş parçacığına görünür olur. Bu, `volatile`'in yalnızca kendi alanını değil, ondan önceki normal yazmaları da "taşıması" özelliğidir.

# jmm-5 | Uzman
## Soru
"False sharing" (yanlış paylaşım) nedir ve neden performansı düşürür?
## A
İki iş parçacığının AYNI değişkeni kilitsiz güncellemesi ve güncellemelerin birbirini ezmesidir
🤖 Bu veri yarışıdır (race condition); false sharing'te değişkenler farklıdır ve sonuç doğrudur.
## B
Bir iş parçacığının, diğerinin özel (private) alanlarını yanlışlıkla görebilmesi ve önbellek üzerinden okuyabilmesidir
🤖 Bu bir görünürlük/kapsülleme ihlali olurdu; false sharing tümüyle bir önbellek yerleşimi sorunudur.
## C
Farklı iş parçacıklarının güncellediği bağımsız değişkenlerin aynı önbellek satırına (cache line) düşmesi; biri yazınca satır diğer çekirdekte geçersiz kılınır
🤖 Doğru: mantıken bağımsız ama fiziksel olarak aynı satırı paylaşan alanlar sorunu yaratır.
## D
Aynı nesnenin çekirdek başına birer kopyasının tutulması ve kopyalar arasında veri kaybı yaşanmasıdır
🤖 Veri kaybı yoktur; sorun tek bir satırın çekirdekler arasında sürekli el değiştirmesidir.
## Doğru
C
## Açıklama
CPU'lar belleği tek tek baytlarla değil **önbellek satırları** (cache line, tipik olarak 64 bayt) hâlinde önbelleğe alır. False sharing, mantıksal olarak bağımsız iki değişkenin (ör. bir dizide yan yana iki sayaç) aynı önbellek satırına düşmesidir. İki farklı çekirdekteki iş parçacığı kendi değişkenini güncellese bile, donanım tutarlılık protokolü (cache coherence) satırı bir bütün olarak görür: biri yazdığında diğer çekirdeğin kopyası geçersiz kılınır ve o çekirdek satırı yeniden okumak zorunda kalır. Sonuç: veri yarışı YOK (kod doğru) ama çekirdekler arası sürekli satır seğirtmesi yüzünden performans çöker. Çözümler: değişkenleri ayrı satırlara yaymak (padding), `@Contended` (JDK dahili) veya sıcak sayaçları çekirdek başına ayırmak (ör. `LongAdder`). False sharing, "doğru ama yavaş" eşzamanlı kodun klasik gizli sebebidir.

# jmm-6 | Uzman
## Soru
Değişmez (immutable) bir nesnenin `final` alanları, ek senkronizasyon olmadan neden güvenle paylaşılabilir (safe publication)?
## A
`final` alanlar nesnenin değil sınıfın belleğinde, statik bölgede tutulduğu ve paylaşılan tek kopya olduğu için
🤖 `final` örnek alanları da nesnenin içinde durur; garanti bellek bölgesinden değil, JMM'nin final-alan kuralından gelir.
## B
JMM, yapıcı düzgün tamamlanıp referans yayınlandığında `final` alanların doğru değerleriyle görüneceğini garanti eder; yarı-inşa nesne görülmez
🤖 Doğru: final-alan semantiği güvenli yayınlamanın (safe publication) temelidir.
## C
`final` alanlara her erişimde JVM otomatik olarak `volatile` okuması gibi bir bellek bariyeri koyduğu için
🤖 Her erişimde bariyer yoktur; garanti yalnızca yapıcı bitişindeki tek seferlik "dondurma" (freeze) için geçerlidir.
## D
`final` alanlar hiçbir CPU önbelleğine alınmadığı, her okumada doğrudan ana bellekten çekildiği için
🤖 Önbelleğe alınırlar; garanti önbellekleme yasağı değil, yapıcı sonrası yayınlama sırasıdır.
## Doğru
B
## Açıklama
JMM'nin özel bir kuralı vardır: bir nesnenin yapıcısı `final` alanları ayarlar ve yapıcı **düzgün biterse** (referans yapıcı içinde dışarı sızmazsa), o `final` alanların yazımları, nesne referansının yayınlanmasıyla arasında bir görünürlük garantisi taşır. Yani başka bir iş parçacığı nesne referansını görüyorsa, `final` alanların yapıcıdaki başlangıç değerlerini de doğru görür — ek `volatile`/kilit gerekmez. Bu, **güvenli yayınlama** (safe publication) mekanizmasıdır ve değişmez (immutable) nesnelerin (tüm alanlar `final`, yapıcıdan sonra değişmez) neden iş parçacıkları arasında senkronizasyonsuz paylaşılabildiğinin temelidir — `String`, `Integer` gibi. Uyarı: garanti yalnız `final` alanlar ve **yapıcıdan referans kaçmadığı** sürece geçerlidir; `this` yapıcı bitmeden bir listeye/iş parçacığına verilirse garanti bozulur. Bu yüzden değişmezlik, hem tasarımı basitleştirir hem de eşzamanlılığı ücretsiz güvenli kılar.
