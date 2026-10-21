---
slug: deadlock-canlilik
baslik: "Deadlock ve Canlılık (Liveness) Sorunları"
kademe: İleri
sira: 54
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Dar iki sokağın kavşağında iki araba burun buruna gelir; ikisi de "önce sen geç" diye bekler ve kimse geçemez — trafik durur. Başka bir sahnede ikisi de aynı anda sağa kırar, yine karşılaşır, yine sağa kırar: hareket vardır ama ilerleme yoktur. Üçüncü sahnede bir sürücü sıraya girer ama kimse yol vermediği için hiç geçemez. Bu üç durum programlarda sırasıyla deadlock, livelock ve starvation'dır. Konunun amacı, çok iş parçacıklı (thread) bir sistemin neden "çalışıyor ama ilerlemiyor" hâline düştüğünü anlamaktır. İkinci amacı da bunu nasıl önleyeceğini göstermektir.

### Temel kavramlar

- **Kilit (lock)**: Paylaşılan bir veriye aynı anda tek iş parçacığının (thread) dokunmasını sağlayan kapı.
- **Canlılık (liveness)**: Bir sistemin İLERLEME kaydedebilme yeteneği.
- **Kilitlenme (deadlock)**: İki veya daha çok thread'in, her biri diğerinin tuttuğu kilidi beklerken kalıcı olarak tıkanması. Kimse ölmez, kimse ilerlemez; thread'ler `BLOCKED` durumda, CPU tüketmeden asılı kalır.
- **Livelock**: Thread'ler bloklanmaz ama sürekli birbirine tepki verip durum değiştirir; meşguldürler ama hiçbir gerçek iş ilerlemez (ör. ikisi de nezaketen geri çekilip tekrar deneyen iki kişi).
- **Starvation (açlık)**: Bir thread, kaynağa (CPU, kilit) sürekli başkaları tarafından önlendiği için erişemez.

Deadlock'u benzerlerinden ayır: sonsuz döngü CPU yakar, deadlock CPU tüketmez; starvation'da kilit döngüsü yoktur, adaletsiz sıralama vardır; istisnayla ölen bir thread ise çökmedir, deadlock değildir.

Deadlock, **Coffman'ın 4 koşulu** aynı anda sağlanınca oluşur: karşılıklı dışlama (mutual exclusion), tut-ve-bekle (hold and wait), ön alımsızlık (no preemption), döngüsel bekleme (circular wait). Herhangi BİRİNİ kırmak deadlock'u önler — en pratik yol, tüm thread'lerin kilitleri AYNI GLOBAL SIRADA almasıdır (döngüsel beklemeyi kırar).

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Döngüsel bekleme ve lock ordering">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Döngüsel bekleme = deadlock</text>
  <circle cx="150" cy="105" r="42" fill="#E6F0F8" stroke="#1C5A8A" stroke-width="1.5"/>
  <text x="150" y="102" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C5A8A" font-family="system-ui">Thread 1</text>
  <text x="150" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">A tutar, B ister</text>
  <circle cx="490" cy="105" r="42" fill="#FBF3E6" stroke="#B4772B" stroke-width="1.5"/>
  <text x="490" y="102" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B4772B" font-family="system-ui">Thread 2</text>
  <text x="490" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">B tutar, A ister</text>
  <path d="M195 92 L443 92" stroke="#B4322B" stroke-width="2" fill="none"/>
  <polygon points="443,92 432,87 432,97" fill="#B4322B"/>
  <text x="320" y="84" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">B kilidini bekler</text>
  <path d="M445 122 L197 122" stroke="#B4322B" stroke-width="2" fill="none"/>
  <polygon points="197,122 208,117 208,127" fill="#B4322B"/>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">A kilidini bekler</text>
  <text x="320" y="182" text-anchor="middle" font-size="12" font-weight="600" fill="#25804C" font-family="system-ui">Çözüm: herkes kilitleri AYNI sırada alsın (A sonra B).</text>
</svg>
```

### Nasıl çalışır?

Coffman koşullarını tek tek okuyalım. (1) **Karşılıklı dışlama**: kaynak bir anda tek sahiplidir. (2) **Tut-ve-bekle**: thread elindekini bırakmadan yeni kaynak bekler. (3) **Ön alımsızlık**: kaynak zorla elinden alınamaz, gönüllü bırakılmalıdır. (4) **Döngüsel bekleme**: thread'ler arasında dairesel bir bekleme zinciri vardır. Dördü AYNI ANDA gerekir; bu yüzden birini kırmak yeter.

```java
// YANLIŞ — kilit sırası çağrıya göre değişir → döngüsel bekleme mümkün
void aktar(Hesap a, Hesap b) { synchronized (a) { synchronized (b) { /* ... */ } } }

// DOĞRU — herkes AYNI global sırada kilitler (ör. id'ye göre)
Hesap once  = a.id() < b.id() ? a : b;
Hesap sonra = (once == a) ? b : a;
synchronized (once) { synchronized (sonra) { /* ... */ } }
```

Lock ordering en yaygın önlemdir çünkü mevcut eşzamanlılığı koruyarak deadlock'u yapısal olarak imkânsızlaştırır. Alternatif "çözümler" aldatıcıdır. Her nesneye bir kilit daha eklemek bekleme ilişkilerini çoğaltır, yani riski büyütür. Kilitleri kaldırıp yalnız `volatile`e güvenmek görünürlük verir ama atomiklik vermez; sonuç yarış koşuludur (race condition). Uygulamayı tek thread'e indirmek deadlock'u bitirir ama paralelliği de bitirir; bu çözüm değil kaçıştır.

Deadlock'un sebebi kaynak baskısı değildir. Thread sayısının artması tek başına deadlock üretmez; tek kilitle deadlock hiç oluşmaz. Yüksek CPU ya da düşük bellek yalnızca yavaşlatır. Uzun bir GC duraklaması ise geçicidir, biter. Deadlock ise kalıcıdır ve kilit ilişkilerinden doğar.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Coffman deadlock dört koşulu, herhangi birini kırmak önler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Coffman: 4 koşul AYNI ANDA → deadlock</text>
  <rect x="22" y="44" width="138" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="91" y="70" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">1 · Karşılıklı</text>
  <text x="91" y="86" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">dışlama</text>
  <text x="91" y="102" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kaynak tek sahipli</text>
  <text x="168" y="82" text-anchor="middle" font-size="14" font-weight="700" fill="#5D7789" font-family="system-ui">∧</text>
  <rect x="176" y="44" width="138" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="245" y="70" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">2 · Tut-ve-bekle</text>
  <text x="245" y="88" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">eldekini bırakmadan</text>
  <text x="245" y="101" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yeni kaynak bekle</text>
  <text x="322" y="82" text-anchor="middle" font-size="14" font-weight="700" fill="#5D7789" font-family="system-ui">∧</text>
  <rect x="330" y="44" width="138" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="399" y="70" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">3 · Ön alımsızlık</text>
  <text x="399" y="88" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">zorla alınamaz,</text>
  <text x="399" y="101" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gönüllü bırakılır</text>
  <text x="476" y="82" text-anchor="middle" font-size="14" font-weight="700" fill="#5D7789" font-family="system-ui">∧</text>
  <rect x="484" y="44" width="134" height="66" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="551" y="70" text-anchor="middle" font-size="11.5" font-weight="600" fill="#B26B00" font-family="system-ui">4 · Döngüsel</text>
  <text x="551" y="86" text-anchor="middle" font-size="11.5" font-weight="600" fill="#B26B00" font-family="system-ui">bekleme</text>
  <text x="551" y="102" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">dairesel zincir</text>
  <rect x="22" y="128" width="596" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="320" y="152" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Herhangi BİRİNİ kır → deadlock imkânsız</text>
  <text x="320" y="174" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">en pratik: ortak kilit sırası (4'ü kırar) · tryLock timeout (2'yi kırar)</text>
</svg>
```

Deadlock, canlılığı bozan üç sorundan yalnız biri; livelock ve starvation da ilerlemeyi engeller. Livelock'ta thread'ler PASİF değil AKTİFTİR: bloklanmaz, `RUNNABLE` kalır ve CPU harcar. Ama çakışmadan kaçınmak için ikisi de kilidi bırakıp yeniden dener; böylece hep aynı çakışmayı üretirler. Dar bir koridorda karşılaşan iki kişinin sürekli aynı yöne kaçması gibidir. Genelde "nazik" geri-çekilme (backoff) mantığının senkronize olmasından doğar; çözümü rastgele bekleme (randomized backoff) eklemektir. JVM bu durumda thread'leri algılayıp zorla sonlandırmaz.

Starvation'da ise thread çalışabilir durumdadır ama sırası hiç gelmez. Tipik sebepler: bazı thread'lere aşırı yüksek öncelik verilmesi ve `synchronized`/varsayılan `ReentrantLock`'un ADİL olmaması (kilidi bırakan thread onu çoğu zaman hemen geri kapar). Çare, adil kilit (`new ReentrantLock(true)`) ya da FIFO kuyruk disiplinidir. Adaletin bedeli iş hacminin (throughput) düşmesidir.

```svg
<svg viewBox="0 0 640 195" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Deadlock, livelock ve starvation karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Canlılık sorunları: üç farklı ilerleyememe</text>
  <rect x="22" y="42" width="192" height="132" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Deadlock</text>
  <text x="118" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">PASİF · BLOCKED</text>
  <text x="118" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">CPU tüketmez</text>
  <text x="118" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">karşılıklı kilit</text>
  <text x="118" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">beklemesi</text>
  <rect x="224" y="42" width="192" height="132" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Livelock</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">AKTİF · CPU harcar</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sürekli tepki verir</text>
  <text x="320" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ama iş ilerlemez</text>
  <text x="320" y="150" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çözüm: rastgele backoff</text>
  <rect x="426" y="42" width="192" height="132" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="522" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Starvation</text>
  <text x="522" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çalışabilir ama</text>
  <text x="522" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sıra hiç gelmez</text>
  <text x="522" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">başkaları önceler</text>
  <text x="522" y="150" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">CPU/kilit hep başkasına</text>
</svg>
```

Teşhis ve kurtarma: deadlock bir istisna DEĞİLDİR, sessizdir — `try/catch` ile yakalanmaz, derleyici kilit sırasını analiz edip uyarmaz, uygulama özel loglamadıkça kendiliğinden loglanmaz. Standart yol, çalışan JVM'den thread dump almaktır: `jstack <pid>`, `kill -3 <pid>` ya da JMX/profiler. JVM'in deadlock dedektörü dump'ta "Found one Java-level deadlock" başlığıyla döngüyü açıkça raporlar. `BLOCKED` thread'lerin "waiting to lock" / "locked" satırlarından bekleme zinciri çıkarılır; böylece kilit sıralaması hatası bulunur.

Kurtarma stratejisi olarak `ReentrantLock.tryLock(timeout, unit)` kilidi belirli bir süre içinde almayı dener, alamazsa `false` döner; thread ELİNDEKİLERİ DE bırakıp bir süre bekleyip yeniden dener — bu, tut-ve-bekle koşulunu kırar. Ama kesin çözüm değildir. Geri çekilenler sürekli aynı anda yeniden denerse LIVELOCK doğar; bazıları hep geç kalırsa STARVATION doğar. Bu yüzden rastgele ya da artan bekleme (backoff) eklenir. `synchronized` zaman aşımını desteklemez; doğru ayarlı timeout ise sonsuza dek kilitli kalmaya karşı bir güvenlik supabıdır.

### Mülakatta ne sorulur?

- **Coffman'ın 4 koşulu**: karşılıklı dışlama, tut-ve-bekle, ön alımsızlık, döngüsel bekleme — dördü AYNI ANDA. Herhangi birini kırmak yeter.
- **Hangi koşulu ne kırar**: ortak kilit sırası → döngüsel bekleme (4); `tryLock` + geri çekilme → tut-ve-bekle (2).
- **Deadlock vs livelock**: pasif/`BLOCKED`/CPU tüketmez vs aktif/`RUNNABLE`/CPU harcar ama ilerlemez. Rolleri ters söylemek klasik hatadır.
- **Starvation**: adaletsizlikten doğar (öncelik, adil olmayan kilit); çaresi adil kilit/FIFO, bedeli iş hacmidir (throughput).
- **Teşhis**: thread dump ("Found one Java-level deadlock"); `DeadlockException` diye bir şey YOKTUR, derleyici uyarısı ve otomatik log da yoktur.
- **Sahte çözümler**: daha çok kilit, yalnız `volatile`, tek thread'e inmek, `synchronized`'a timeout vermek.

### Tek cümlede

Deadlock, Coffman'ın dört koşulu birlikte sağlandığında doğan kalıcı karşılıklı beklemedir; livelock ve starvation ise onun aktif ve adaletsiz akrabalarıdır. Pratikteki panzehir ortak kilit sırası, teşhis aracı ise thread dump'tır.

# dl-1 | Temel
## Soru
Deadlock (kilitlenme) nedir?
## A
Bir thread'in bir hesaplamada sonsuz döngüye girip CPU'yu boşuna meşgul etmesidir
🤖 O bir sonsuz döngüdür; deadlock'ta thread CPU tüketmez, BLOCKED bekler.
## B
Her biri diğerinin tuttuğu kilidi bekleyen thread'lerin kalıcı olarak tıkanmasıdır
🤖 Doğru: karşılıklı, döngüsel kilit beklemesi.
## C
Bir thread'in kaynağa sırası hiç gelmediği için sürekli ertelenip ilerleyememesidir
🤖 O starvation (açlık); orada kilit döngüsü değil, adaletsiz sıralama vardır.
## D
Bir thread'in yakalanmayan bir istisnayla sonlanıp görevini yarım bırakmasıdır
🤖 Çökme deadlock değildir; deadlock'ta thread'ler yaşar ama ilerleyemez.
## Doğru
B
## Açıklama
Deadlock, iki+ thread'in birbirinin elindeki kaynağı (genelde kilidi) beklemesiyle oluşan karşılıklı ve KALICI bir tıkanmadır: Thread 1 A kilidini tutup B'yi bekler, Thread 2 B'yi tutup A'yı bekler — ikisi de asla ilerleyemez. Thread'ler CPU tüketmeden BLOCKED durumda sonsuza dek asılı kalır. Bu, sonsuz döngüden (A — CPU yakar), starvation'dan (C — kaynağa hiç sıra gelmemesi) ve çökmeden (D — istisnayla ölüm) farklıdır. Deadlock'ta kimse ölmez, kimse ilerlemez.

# dl-2 | Orta
## Soru
Deadlock'un oluşması için gereken dört koşul (Coffman koşulları) hangileridir?
## A
Karşılıklı dışlama, tut-ve-bekle, ön alımsızlık ve döngüsel bekleme — dördü AYNI ANDA
🤖 Doğru: klasik dört Coffman koşulu.
## B
İkiden fazla thread'in aynı kilidi kullanması; thread sayısı arttıkça deadlock kaçınılmaz olur
🤖 Thread sayısı tek başına deadlock üretmez; tek kilitle deadlock hiç oluşmaz.
## C
Yüksek CPU kullanımı, düşük boş bellek ve uzun kuyruk bekleme sürelerinin bir araya gelmesi
🤖 Kaynak baskısı yavaşlatır ama deadlock koşulu değildir; sorun kilit ilişkilerindedir.
## D
Uzun bir GC (çöp toplama) duraklamasının kilit tutan bir thread'i durdurması
🤖 GC duraklaması geçici gecikmedir ve biter; deadlock kalıcıdır, kilit döngüsünden doğar.
## Doğru
A
## Açıklama
Deadlock ancak DÖRT koşul aynı anda sağlanınca oluşur (Coffman, 1971): (1) Karşılıklı dışlama — kaynak bir anda tek thread'e ait olabilir; (2) Tut-ve-bekle — thread elindeki kaynağı bırakmadan yeni kaynak bekler; (3) Ön alımsızlık — kaynak zorla elinden alınamaz, gönüllü bırakılmalıdır; (4) Döngüsel bekleme — thread'ler arasında dairesel bir bekleme zinciri vardır. Önemli sonuç: bu koşullardan HERHANGİ BİRİNİ kırmak deadlock'u önler — en pratiği döngüsel beklemeyi kilit sıralamasıyla kırmaktır. Thread sayısı (B), CPU/bellek (C) veya GC (D) bu koşulların yerine geçmez.

# dl-3 | Orta
## Soru
Birden çok kilit alan bir sistemde deadlock'u önlemenin en pratik ve yaygın yolu hangisidir?
## A
Her paylaşılan nesneye bir kilit daha ekleyip koruma katmanlarını çoğaltmak
🤖 Daha çok kilit, daha çok bekleme ilişkisi demektir; deadlock olasılığını artırır.
## B
Kilitleri tamamen kaldırıp senkronizasyonu yalnızca `volatile` alanlara bırakmak
🤖 `volatile` görünürlük verir, atomiklik vermez; yarış koşulları (race condition) doğar.
## C
Uygulamayı tek thread'e indirip paralelliği tümüyle kapatmak
🤖 Deadlock biter ama paralellik de biter; bu bir çözüm değil kaçıştır.
## D
Tüm thread'lerin kilitleri AYNI, GLOBAL bir sırada alması (lock ordering)
🤖 Doğru: tutarlı kilit sıralaması döngüsel beklemeyi kırar.
## Doğru
D
## Açıklama
Deadlock'u önlemenin en yaygın ve pratik yolu, dört koşuldan DÖNGÜSEL BEKLEMEYİ kırmaktır: tüm thread'lerin birden çok kilidi HER ZAMAN aynı GLOBAL sırada (ör. her zaman önce hesap-A sonra hesap-B, kimlik/hash sırasına göre) alması sağlanır. Böylece dairesel bir bekleme zinciri hiç oluşamaz. Daha çok kilit eklemek (A) bekleme ilişkilerini çoğaltarak sorunu büyütür; kilitleri kaldırıp yalnız `volatile`e güvenmek (B) görünürlük sağlar ama atomiklik sağlamaz, yarış koşulları getirir; tek thread'e inmek (C) paralelliği yok eder. Lock ordering, mevcut eşzamanlılığı koruyarak deadlock'u yapısal olarak imkânsızlaştırdığı için tercih edilir.

# dl-4 | İleri
## Soru
Çalışan bir uygulamada deadlock şüphesini teşhis etmenin standart yolu nedir?
## A
Kilit alan bloğu `try-catch` ile sarıp fırlatılan `DeadlockException`'ı yakalamak
🤖 Böyle bir istisna yoktur; deadlock sessizdir, try-catch ile yakalanamaz.
## B
Derleyicinin kilit sırası uyumsuzluğu için verdiği uyarıları derleme çıktısında incelemek
🤖 Derleyici kilit sırasını analiz etmez; sorun yalnız çalışma-zamanında görünür.
## C
Thread dump almak (jstack / `kill -3`) ve JVM'in raporladığı kilit döngüsünü incelemek
🤖 Doğru: thread dump deadlock teşhisinin standart aracıdır.
## D
Ek işlem gerekmez; JVM deadlock'u algıladığı anda uygulama loguna otomatik olarak yazar
🤖 Uygulama özel loglamadıkça deadlock otomatik loglanmaz; dump almak gerekir.
## Doğru
C
## Açıklama
Deadlock bir istisna DEĞİLDİR — thread'ler sessizce BLOCKED durumda asılı kalır; bu yüzden `try-catch` (A) veya derleyici uyarısı (B) işe yaramaz, otomatik da loglanmaz (D). Standart teşhis: çalışan JVM'den thread dump almak — `jstack <pid>`, `kill -3 <pid>` ya da JMX/profiler. JVM'in deadlock dedektörü dump'ta "Found one Java-level deadlock" başlığıyla döngüyü açıkça raporlar: hangi thread hangi kilidi TUTUYOR ve hangisini BEKLİYOR. BLOCKED thread'lerin "waiting to lock" / "locked" satırlarından bekleme zinciri çıkarılır ve kilit sıralaması hatası tespit edilir.

# dl-5 | İleri
## Soru
Livelock ile deadlock arasındaki fark nedir?
## A
Fark yoktur; ikisinde de thread'ler BLOCKED durumda birbirinin kilidini bekler
🤖 Aynı değildir; livelock'ta thread'ler hiç bloklanmaz, RUNNABLE kalır.
## B
Livelock'ta thread'ler BLOKLANMAZ; sürekli çalışır ama gerçek iş ilerlemez
🤖 Doğru: livelock aktif ama ilerlemesiz, deadlock ise pasif beklemedir.
## C
Livelock'ta thread'ler hiç CPU kullanmaz; deadlock'ta ise CPU kullanımı %100'e çıkar
🤖 Roller ters: CPU yakan livelock'tur, deadlock'ta thread'ler CPU tüketmeden bekler.
## D
Livelock, JVM'in kilitlenmeyi algılayıp ilgili thread'leri zorla sonlandırmasıdır
🤖 JVM böyle bir şey yapmaz; livelock'ta thread'ler çalışır ama ilerleyemez.
## Doğru
B
## Açıklama
Deadlock'ta thread'ler PASİFTİR: BLOCKED durumda, CPU tüketmeden birbirini bekler. Livelock'ta ise thread'ler AKTİFTİR ve CPU harcar (C yanlış) — ama sürekli birbirlerine tepki vererek durumlarını değiştirdikleri için hiçbir gerçek İŞ ilerlemez. Klasik örnek: dar bir koridorda karşılaşan iki kişinin ikisinin de aynı yöne kaçıp durması gibi, çakışmadan kaçınmak için ikisi de kilidi bırakıp yeniden denerken hep aynı çakışmayı üretmesidir. Livelock genelde "nazik" geri-çekilme (backoff) mantığının senkronize olmasından doğar; rastgele bekleme (randomized backoff) sık kullanılan çözümdür. Çökme (D) veya bloklanma (A) livelock değildir.

# dl-6 | Uzman
## Soru
Kilit alırken deadlock riskini azaltmak için "timeout stratejisi" nasıl uygulanır ve sınırı nedir?
## A
`Lock.tryLock(timeout)` ile süreli kilit alıp alınamazsa geri çekilmek; sınırı livelock riskidir
🤖 Doğru: süreli kilit tut-ve-bekle'yi kırar ama backoff dikkatli kurgulanmalıdır.
## B
`synchronized` bloğuna bir zaman aşımı parametresi vermek; süre dolunca JVM kilidi otomatik bırakır
🤖 `synchronized` timeout desteklemez; süreli kilit için `Lock`/`tryLock` gerekir.
## C
`tryLock` kullanmak deadlock'u matematiksel olarak İMKÂNSIZ kılar; hiçbir yan etkisi ya da sınırı yoktur
🤖 Abartı; kötü kurgulanmış geri-çekilme livelock/starvation üretebilir, garanti değildir.
## D
Zaman aşımı her koşulda gecikme ekleyip verimi düşürdüğü için pratikte hiç kullanılmaz
🤖 Yanlış; doğru ayarlı timeout, sonsuza dek kilitli kalmaya kıyasla sistemi kurtarır.
## Doğru
A
## Açıklama
`ReentrantLock.tryLock(timeout, unit)`, kilidi belirli bir süre içinde almayı dener; alamazsa `false` döner. Strateji: bir thread ihtiyaç duyduğu kilitleri süreli almaya çalışır, biri alınamazsa ELİNDEKİLERİ DE bırakıp bir süre bekleyip yeniden dener. Böylece "tut-ve-bekle" koşulu kırılır — thread sonsuza dek beklemez, deadlock'tan kurtulur. Ancak bu kesin bir çözüm değildir (C yanlış): geri çekilen thread'ler sürekli aynı anda yeniden denerse LIVELOCK, bazı thread'ler hep geç kalırsa STARVATION oluşabilir; bu yüzden rastgele/artan bekleme (backoff) eklenir. `synchronized` bu yeteneği sunmaz (B); timeout doğru ayarlanınca faydasız değil, kilitli kalmaya karşı bir güvenlik supabıdır (D).
