---
slug: dokum-eszamanlilik-sql
baslik: "Döküm · Eşzamanlılık ve SQL: Race Condition, volatile, GROUP BY, İndeks ve Parçalama"
kademe: İleri
sira: 275
etiketler: 11
---

## Konu anlatımı

Bu dosya bir tahmin listesi değil. Gerçek bir teknik mülakatın dökümünden çıktı. Sorular mülakatçının sorduğu hâliyle, cevaplar senin o an verdiğin hâliyle duruyor. Elimizde performans verisi var; yapılacak iş iskelet vermek değil, verdiğin cevabı değerlendirip güçlendirmek.

Eşzamanlılık burada ayrı bir uyarıyı hak ediyor. Aynı alan, başka bir mülakatın geri bildiriminde de zayıf çıkmıştı. Bu dökümdeki cevapların hemen hepsi doğru; sorun içerikte değil, uzunlukta. Cevaplar kısa. Kısa ve doğru cevap mülakatçıyı bir üst basamağa çıkarır. Eşzamanlılıkta o basamak çok hızlı gelir: tanımdan bellek modeline, oradan kilit seçimine iki soruda ulaşılır. Bu yüzden aşağıdaki "takip sorusu riski" bölümleri asıl çalışma alanın.

Dosyanın ilk dört sorusu eşzamanlılık, son üçü veri tarafıdır. Veri tarafında da aynı desen var: cevaplar doğru, ama ölçüt cümlesi eksik.

### "Race condition nedir, thread-safe ne demek?"

**Soruldu / Verdiğin cevap**

Mülakatçı önce tanım istedi, sonra ikinci kavramı ekledi.

> Ortak veriyi birden çok iş parçacığı (thread) aynı anda değiştirdiğinde sonucun zamanlamaya bağlı olmasıdır. Thread-safe kod ise eş zamanlı çağrıldığında da tutarlı davranır.

**Değerlendirme**

✅ Yeterli. Tanım doğru ve iki kavramı birden kapsıyor. Ama cevap yirmi saniyede bitiyor. Mülakatçı boşluğu bir sonraki soruyla doldurur; kısalık burada seni bir üst basamağa iter.

**Takip sorusu riski**

- **"Somut bir örnek verir misin?"** → `sayac++` tek satır görünür ama üç adımdır: oku, artır, yaz. İki iş parçacığı aynı eski değeri okur, biri diğerinin yazdığını ezer. Sonuç: iki artırma yapılır, sayaç bir artar (`46`).
- **"Görünürlük ile atomiklik aynı şey mi?"** → Hayır. Görünürlük (visibility) bir iş parçacığının yazdığını diğerinin görebilmesidir. Atomiklik işlemin bölünmeden bitmesidir. `volatile` birincisini verir, ikincisini vermez (`48`, `45`).
- **"Tek çekirdekli makinede de olur mu?"** → Olur. Sorunun kaynağı paralellik değil, araya girme ve yeniden sıralamadır. Derleyici ve JIT komutları yeniden sıralayabilir (`45`).
- **"Değişmez nesne (immutable) thread-safe midir?"** → Evet. Durum hiç değişmiyorsa yarış da yoktur. En ucuz thread güvenliği paylaşılan değiştirilebilir durumu ortadan kaldırmaktır (`42`).

**Güçlendirilmiş cevap**

Tanımı ver, hemen ardından örneği yapıştır: "`sayac++` tek satırdır ama oku-artır-yaz üç adımıdır." Sonra ayrımı kur: yarış iki farklı sebepten çıkar, görünürlük eksikliği ve atomiklik eksikliği. Bunlar farklı araçlarla çözülür, bu yüzden ayrı ayrı adlandırılır. Cümleyi de kapat: thread-safe olmanın en ucuz yolu paylaşılan durumu hiç yaratmamaktır; değişmez nesne ve yerel değişken bedava güvenlik verir. Böylece tanım, örnek ve sınır aynı cevapta durur.

```svg
<svg viewBox="0 0 660 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="sayac++ uc adimdir; iki thread ayni degeri okur; gorunurluk ve atomiklik ayrimi">
  <defs>
    <marker id="dkmcon-ok-adim" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">sayac++ tek satırdır, ama tek işlem değildir</text>
  <rect x="44" y="36" width="172" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="130" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 · OKU</text>
  <text x="130" y="73" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bellekten sayac alınır (7)</text>
  <line x1="218" y1="59" x2="232" y2="59" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmcon-ok-adim)"/>
  <rect x="234" y="36" width="172" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 · ARTIR</text>
  <text x="320" y="73" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yazmaçta 7 + 1 = 8</text>
  <line x1="408" y1="59" x2="422" y2="59" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmcon-ok-adim)"/>
  <rect x="424" y="36" width="172" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="510" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · YAZ</text>
  <text x="510" y="73" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sayac alanına 8 yazılır</text>
  <rect x="14" y="94" width="632" height="104" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="114" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Araya girince ne oluyor</text>
  <text x="40" y="138" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T1</text>
  <text x="72" y="138" font-size="9.5" fill="#1C3247" font-family="system-ui">okur: 7</text>
  <text x="250" y="138" font-size="9.5" fill="#1C3247" font-family="system-ui">artırır: 8</text>
  <text x="470" y="138" font-size="9.5" fill="#1C3247" font-family="system-ui">yazar: 8</text>
  <line x1="66" y1="146" x2="620" y2="146" stroke="#5D7789" stroke-width="1" stroke-dasharray="3 3"/>
  <text x="40" y="166" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T2</text>
  <text x="150" y="166" font-size="9.5" fill="#1C3247" font-family="system-ui">okur: 7</text>
  <text x="350" y="166" font-size="9.5" fill="#1C3247" font-family="system-ui">artırır: 8</text>
  <text x="545" y="166" font-size="9.5" fill="#1C3247" font-family="system-ui">yazar: 8</text>
  <text x="330" y="188" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">İki artırma yapıldı, sayaç yalnız bir arttı — kayıp güncelleme</text>
  <rect x="14" y="210" width="310" height="116" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="230" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Görünürlük (visibility)</text>
  <text x="169" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">soru: birinin yazdığını diğeri görüyor mu?</text>
  <text x="169" y="272" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bozan şey: önbellek ve yeniden sıralama</text>
  <text x="169" y="292" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">araç: volatile · kilit · atomik sınıf</text>
  <text x="169" y="314" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">volatile burada YETER</text>
  <rect x="336" y="210" width="310" height="116" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="491" y="230" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Atomiklik (atomicity)</text>
  <text x="491" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">soru: işlem bölünmeden bitiyor mu?</text>
  <text x="491" y="272" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bozan şey: oku-değiştir-yaz araya girmesi</text>
  <text x="491" y="292" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">araç: synchronized · ReentrantLock · Atomic</text>
  <text x="491" y="314" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">volatile burada YETMEZ</text>
</svg>
```

### "Race condition olan bir metodu nasıl thread-safe yaparsın?"

**Soruldu / Verdiğin cevap**

> `synchronized` kullanırım. Gerekirse `Lock` ya da `ReentrantLock`, veya atomik sınıflar.

**Değerlendirme**

✅ Yeterli. Üç aracı da saymışsın ve sıraları doğru. Eksik olan tek şey seçim ölçütü: hangisi ne zaman. Mülakatçı liste duyduğunda hep aynı soruyu sorar, çünkü liste ezber de olabilir.

**Takip sorusu riski**

- **"Metodu mu senkronize edersin, bloğu mu?"** → Kritik bölüm ne kadar darsa iş hacmi (throughput) o kadar yüksek olur. `synchronized` metot tüm gövdeyi kilitler; genellikle gereğinden fazlasını kilitler (`46`).
- **"`ReentrantLock` neyi ekliyor?"** → Zaman aşımlı deneme (`tryLock`), kesilebilir bekleme, adil kilit seçeneği ve birden çok koşul nesnesi. Bedeli: kilidi `finally` içinde bırakmayı sen üstlenirsin (`47`).
- **"Kilitlenme (deadlock) nasıl önlenir?"** → Kilitleri her yerde aynı sırada al, zaman aşımı koy, kilit tutarken dış servis çağırma. Kilit altında ağ çağrısı yapmak klasik hatadır.
- **"Sanal iş parçacıklarında (virtual threads) fark var mı?"** → Uzun `synchronized` blok taşıyıcı iş parçacığını tutabilir. Bu ortamda `ReentrantLock` daha güvenli tercihtir (`56`).

**Güçlendirilmiş cevap**

Araç saymak yerine bir merdiven anlat; merdiven ezber gibi duymaz. Birinci basamak: paylaşılan durumu tamamen kaldır, işi yerel değişkende bitir. İkinci basamak: durum paylaşılacaksa değişmez yap. Üçüncü basamak: tek bir değişken güncelleniyorsa `AtomicInteger` gibi atomik sınıf kullan. Dördüncü basamak: birlikte tutarlı olması gereken iki alan varsa kilit gerekir, çünkü atomik sınıflar alanlar arası tutarlılık vermez. Beşinci basamak: paylaşılan yapı bir koleksiyonsa `ConcurrentHashMap` gibi eşzamanlı koleksiyona geç (`51`). Sonda ölçütü söyle: kilit her zaman en son çare, çünkü çekişme altında en pahalı olan odur.

### "Bir integer'ı thread-safe yapmak için ne kullanırsın?"

**Soruldu / Verdiğin cevap**

> `AtomicInteger`. `incrementAndGet` metodu atomiktir.

**Değerlendirme**

✅ Yeterli. Doğru sınıf, doğru metot, tereddütsüz cevap. Bu soru genellikle bir sonraki sorunun rampasıdır; mülakatçı mekanizmayı sorar.

**Takip sorusu riski**

- **"İçeride nasıl çalışıyor?"** → Karşılaştır ve değiştir (compare-and-swap, CAS) ile. Eski değer okunur, beklenen değerle karşılaştırılır, tutuyorsa yazılır. Tutmuyorsa döngü baştan dener. Kilit yoktur, ama çekişme altında yeniden deneme (retry) maliyeti vardır (`51`).
- **"Çekişme çok yüksekse ne olur?"** → Yeniden deneme döngüsü boşa dönmeye başlar. Sayaç yazma yoğunsa hücrelere bölen `LongAdder` genellikle daha iyi ölçeklenir; toplamı okuma anında birleştirir.
- **"İki alanı birlikte güncellemen gerekirse?"** → Atomik sınıf yalnız tek değişken için garanti verir. `bakiye` ile `sonIslemZamani` birlikte tutarlı olacaksa kilit ya da veri tabanı işlemi (transaction) gerekir (`84`).
- **"`volatile int` ile farkı ne?"** → `volatile` yalnız görünürlük verir. `AtomicInteger` görünürlüğe ek olarak oku-değiştir-yaz atomikliğini de verir (`48`).

**Güçlendirilmiş cevap**

Sınıf adını söyledikten sonra mekanizmayı bir cümleyle aç: "İçeride kilit değil, karşılaştır ve değiştir döngüsü var." Bu cümle mülakatçıya bilginin ezber olmadığını gösterir. Ardından kapsamı çiz: atomik sınıflar tek değişkende harikadır, iki alanın birlikte tutarlılığında hiçbir şey yapmaz. Son olarak bir ölçüt bırak: okuma ağırlıklı sayaçta `AtomicInteger`, yazma çok yoğunsa `LongAdder` düşünülür. Üç cümlede tanım, mekanizma ve sınır verilmiş olur.

### "volatile nedir?"

**Soruldu / Verdiğin cevap**

> Görünürlük sağlar. Ama `i++` gibi bileşik bir işlemi atomik yapmaz.

**Değerlendirme**

✅ **Tam doğru — ve bu dosyanın en iyi cevabı.** Bu soru bir eleme sorusudur; adayların çoğu burada `volatile`'ı hafif bir kilit sanır ve "sayaçta kullanırım" der. Sen tersini yaptın: ayrımı tek cümlede kurdun ve sınırı kendin söyledin. Mülakatta bu netliği koru, çünkü aynı cümle bellek modeli bildiğinin de işaretidir. Şişirmeye gerek yok; cevap olduğu gibi güçlü.

**Takip sorusu riski**

- **"happens-before ne demek?"** → İki işlem arasındaki sıra garantisidir. Bir `volatile` alana yazma, o alanı sonradan okuyan iş parçacığı için "önce olmuş" sayılır. Yazmadan önceki tüm etkiler de o okumaya görünür olur (`45`).
- **"Döngüyü durduran bayrak `volatile` olmalı mı?"** → Evet. `volatile` olmayan bayrak okuması döngü dışına taşınabilir; döngü hiç bitmeyebilir. Bu, `volatile`'ın en tipik doğru kullanımıdır.
- **"Çift kontrollü kilitlemede (double-checked locking) `volatile` neden zorunlu?"** → Nesnenin kurulması ile referansın atanması yeniden sıralanabilir. Alan `volatile` değilse başka bir iş parçacığı boş olmayan ama yarı kurulmuş bir referans görür. `volatile` bu sıralamayı yasaklar ve deseni geçerli kılar (`133`, `45`).
- **"Kilit yerine `volatile` ne zaman yeter?"** → Yeni değer eski değere bağlı değilse yeter. Bayrak, yapılandırma referansı, son okunan zaman damgası gibi. `sayac++` bu kümeye girmez.

**Güçlendirilmiş cevap**

Mevcut cevabını koru, üstüne iki şey ekle. Birincisi mekanizmanın adı: `volatile` bir bellek engeli koyar ve happens-before ilişkisi kurar, yani yalnız o alanın değil ondan önceki yazmaların da görünmesini sağlar. İkincisi iki uçlu örnek: doğru kullanım bayraktır, yanlış kullanım sayaçtır. Ardından ustalık cümlesini bırak: çift kontrollü kilitlemede alan `volatile` olmazsa yarı kurulmuş nesne sızabilir. Bu cümleyi söyleyen aday sayısı azdır ve burası seni doğrudan ayrıştırır.

```svg
<svg viewBox="0 0 660 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="volatile ne verir ne vermez; cift kontrollu kilitlemede volatile zorunlulugu">
  <defs>
    <marker id="dkmcon-ok-vol" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">volatile görünürlük ve sıra verir, atomiklik vermez</text>
  <rect x="14" y="36" width="310" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Verdiği garantiler</text>
  <text x="169" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">okuma daima en son yazılan değeri görür</text>
  <text x="169" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yazma ile okuma arasında happens-before</text>
  <text x="169" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yazmadan önceki etkiler de görünür olur</text>
  <text x="169" y="138" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bu alan üzerinde yeniden sıralama yasak</text>
  <text x="169" y="162" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">doğru kullanım: durdurma bayrağı</text>
  <rect x="336" y="36" width="310" height="140" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="491" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Vermediği garantiler</text>
  <text x="491" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">i++ hâlâ oku-artır-yaz üç adımıdır</text>
  <text x="491" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kontrol et sonra uygula yarışı sürer</text>
  <text x="491" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iki alanın birlikte tutarlılığı korunmaz</text>
  <text x="491" y="138" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karşılıklı dışlama (kilit) sağlamaz</text>
  <text x="491" y="162" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">yanlış kullanım: paylaşılan sayaç</text>
  <rect x="14" y="188" width="632" height="142" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="208" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Çift kontrollü kilitleme (double-checked locking)</text>
  <rect x="30" y="222" width="132" height="40" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="96" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 · örnek boş mu?</text>
  <line x1="164" y1="242" x2="178" y2="242" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmcon-ok-vol)"/>
  <rect x="180" y="222" width="132" height="40" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="246" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">2 · kilidi al</text>
  <line x1="314" y1="242" x2="328" y2="242" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmcon-ok-vol)"/>
  <rect x="330" y="222" width="132" height="40" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="396" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">3 · tekrar boş mu?</text>
  <line x1="464" y1="242" x2="478" y2="242" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmcon-ok-vol)"/>
  <rect x="480" y="222" width="150" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="555" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">4 · nesneyi kur ve ata</text>
  <text x="330" y="284" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">Alan volatile değilse: kurulum ile atama yeniden sıralanabilir</text>
  <text x="330" y="302" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Başka bir iş parçacığı 1. adımda boş olmayan ama yarı kurulmuş referans görür.</text>
  <text x="330" y="320" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">Alan volatile ise bu sıralama yasaklanır; desen ancak o zaman geçerlidir.</text>
</svg>
```

### "Şehir bazında ortalama maaşı nasıl bulursun?"

**Soruldu / Verdiğin cevap**

Mülakatçı bir çalışan tablosu tarif etti ve sorguyu istedi.

> `SELECT city, AVG(salary) FROM employee GROUP BY city;`

**Değerlendirme**

✅ Yeterli. Doğru sorgu, hızlı cevap. Bu soru zaten bir ısınma sorusudur; asıl ölçüm bundan sonraki iki soruda yapılır.

**Takip sorusu riski**

- **"Ortalaması belli bir eşiğin üstünde olan şehirler?"** → `HAVING AVG(salary) > 50000` eklenir. Fark önemlidir: `WHERE` gruplama öncesinde satırı eler, `HAVING` gruplama sonrasında grubu eler (`29`).
- **"`AVG` boş maaşları nasıl sayar?"** → Toplama fonksiyonları `NULL` değerleri atlar; payda da o kadar küçülür. Boş değerin sıfır sayılmasını istiyorsan `COALESCE` ile açıkça dönüştürürsün.
- **"Her şehirdeki en yüksek maaşlı kişinin adını istesem?"** → Burada `GROUP BY` yetmez, çünkü gruba ait olmayan bir kolonu seçemezsin. Doğru araç pencere fonksiyonudur: `ROW_NUMBER() OVER (PARTITION BY city ORDER BY salary DESC)` ile numaralandırıp dış sorguda birinci satırı alırsın. **Bu takip sorusu senin başka bir mülakatında gerçekten geldi.**
- **"Tablo büyükse bu sorgu ne yapar?"** → Gruplama tam tarama ve sıralama gerektirebilir. `city` üzerindeki indeks yardımcı olabilir; karar yürütme planına bakılarak verilir (`146`).

**Güçlendirilmiş cevap**

Sorguyu yaz, sonra kendin bir basamak yukarı çık. "Eşik istenirse `HAVING` gerekir, çünkü `WHERE` gruplamadan önce çalışır" cümlesi tek başına ayrıştırıcıdır. Ardından pencere fonksiyonunu kendin an: "Ortalama değil de her şehrin en yüksek maaşlısı istenseydi `GROUP BY` yetmezdi, `ROW_NUMBER` ile bölüm bazında sıralardım." Böylece mülakatçının hazırladığı ikinci soruyu sen sormuş olursun. Son cümlede `NULL` davranışını ekle; toplama fonksiyonlarının boş değeri atladığını bilmek küçük ama nadir bir ayrıntıdır.

### "İndekslemenin dezavantajı nedir?"

**Soruldu / Verdiğin cevap**

> `SELECT` hızlanır ama `INSERT`, `UPDATE` ve `DELETE` yavaşlar. Ayrıca disk alanı kullanır.

**Değerlendirme**

✅ Yeterli. Takası doğru kurmuşsun ve yazma maliyetini atlamamışsın. Bu soruda çoğu aday yalnız "yer kaplar" der; sen okuma-yazma dengesini söyledin.

**Takip sorusu riski**

- **"Bileşik indekste kolon sırası önemli mi?"** → Çok önemli. `(city, salary)` indeksi soldan ek kuralına uyar: yalnız `salary` ile filtreleyen sorgu bu indeksten yararlanamaz. Sıra, sorgu desenine göre seçilir (`146`).
- **"Seçicilik (selectivity) ne demek?"** → Kolonun kaç farklı değer taşıdığıdır. İki değerli bir kolonda indeks çoğu zaman işe yaramaz; veri tabanı tam taramayı daha ucuz bulur.
- **"İndeksi devre dışı bırakan bir yazım var mı?"** → Kolonu fonksiyona sokan koşullar. `WHERE UPPER(email) = ?` düz indeksi kullandırmaz. Çözüm ya fonksiyon tabanlı indeks ya da koşulu kolonun ham hâline çevirmektir.
- **"Kaç indeks fazladır?"** → Sayı değil kullanım belirler. Hiç kullanılmayan indeks yalnız yazma maliyeti ve disk demektir; kullanım istatistiğine ve yürütme planına bakılır (`227`).

**Güçlendirilmiş cevap**

Takasa bir ölçüt cümlesi ekle: indeks kararı okuma-yazma oranına ve seçiciliğe bakılarak verilir. Sonra somut bir tuzak ver, çünkü tuzak bilgisi kitap bilgisinden ayrılır: fonksiyona sokulan kolon indeksi kullanılmaz hâle getirir. Bileşik indekste soldan ek kuralını da an; bu iki cümle mülakatçıya üretimde plan okumuş biri izlenimi verir. Kapanışta yöntemi söyle: indeks tahminle değil yürütme planıyla eklenir, eklendikten sonra da yazma süresi ölçülür (`146`).

### "Partition ile sharding arasındaki fark nedir?"

**Soruldu / Verdiğin cevap**

> Bölümleme (partition) aynı veritabanı içinde tabloyu parçalara ayırmaktır. Parçalama (sharding) ise veriyi farklı düğümlere dağıtmaktır; yatay ölçek sağlar. Örneğin coğrafyaya göre parçalama yapılabilir.

**Değerlendirme**

✅ **Doğru ve örnekli.** Ayrımın özünü, yani aynı düğüm ile farklı düğüm karşıtlığını yakalamışsın. Örnek vermen de iyi; örnek veren cevap tanım veren cevaptan her zaman güçlüdür.

**Takip sorusu riski**

- **"Bölüm anahtarını nasıl seçersin?"** → İki şart aranır: dağılım düzgün olmalı ve anahtar sorguların çoğunda bulunmalı. Artan bir kimlik seçilirse yeni kayıtlar tek düğüme yığılır ve sıcak nokta (hotspot) doğar (`147`).
- **"İki farklı düğümdeki veriyi birleştirmen gerekirse?"** → Birleştirme (join) uygulama katmanına düşer ve pahalıdır. Daha ağırı işlemdir: tek düğümde kalmayan bir güncelleme artık ACID garantisi almaz.
- **"Düğüm eklemek gerekirse ne olur?"** → Basit `hash % N` yönteminde neredeyse tüm anahtarlar yer değiştirir. Tutarlı özetleme (consistent hashing) ve sanal düğüm, yeniden dengelemede taşınan veriyi azaltır (`147`).
- **"Parçalamadan önce ne denenir?"** → İndeks ve sorgu iyileştirme, okuma kopyası, dikey bölme, önbellek. Parçalama en son adımdır, çünkü geri dönüşü pahalıdır (`157`).

**Güçlendirilmiş cevap**

Tanımı verdikten sonra bedeli söyle; bu soruda ayrım orada çıkar. Bölümleme tek düğümün sınırlarını kaldırmaz, yalnız taranan veriyi ve bakım penceresini küçültür. Parçalama sınırı kaldırır ama karşılığında üç yeni sorun getirir: bölüm anahtarı seçimi, çapraz düğüm birleştirme ve dağıtık işlem. Ardından karar sırasını ver: önce sorgu ve indeks, sonra okuma kopyası, en sonda parçalama. Kapanışta anahtar ölçütünü tekrar et; dağılımı düzgün ve sorgularda hazır bulunan bir alan seçilir, artan kimlik seçilmez.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bolumleme tek dugumde tabloyu boler; parcalama veriyi ayri dugumlere dagitir">
  <defs>
    <marker id="dkmcon-ok-veri" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bölüm aynı düğümde kalır, parça ayrı düğüme gider</text>
  <rect x="14" y="36" width="310" height="182" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Bölümleme (partitioning)</text>
  <rect x="34" y="66" width="270" height="72" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="169" y="82" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek veritabanı düğümü</text>
  <rect x="46" y="90" width="78" height="38" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="85" y="113" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">2024 bölümü</text>
  <rect x="130" y="90" width="78" height="38" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="169" y="113" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">2025 bölümü</text>
  <rect x="214" y="90" width="78" height="38" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="253" y="113" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">2026 bölümü</text>
  <text x="169" y="158" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">kazanç: taranan veri ve bakım penceresi küçülür</text>
  <text x="169" y="176" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">eski bölümü tek işlemle düşürmek kolaydır</text>
  <text x="169" y="198" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">sınır: tek düğümün diski ve işlemcisi aynı kalır</text>
  <rect x="336" y="36" width="310" height="182" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Parçalama (sharding)</text>
  <rect x="352" y="80" width="88" height="48" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="396" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">düğüm 1</text>
  <text x="396" y="117" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">A - H</text>
  <rect x="447" y="80" width="88" height="48" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="491" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">düğüm 2</text>
  <text x="491" y="117" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">I - P</text>
  <rect x="542" y="80" width="88" height="48" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="586" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">düğüm 3</text>
  <text x="586" y="117" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">R - Z</text>
  <text x="491" y="158" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">kazanç: disk, işlemci ve iş hacmi yatay ölçeklenir</text>
  <text x="491" y="176" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bedel: çapraz düğüm birleştirme uygulamaya düşer</text>
  <text x="491" y="198" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bedel: tek düğümü aşan işlem ACID garantisi vermez</text>
  <rect x="14" y="230" width="632" height="94" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="250" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Bölüm anahtarı seçimi kararın en pahalı parçasıdır</text>
  <text x="330" y="272" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">İyi anahtar: dağılımı düzgün, sorguların çoğunda hazır bulunan alan.</text>
  <text x="330" y="290" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Kötü anahtar: artan kimlik — yeni kayıtlar tek düğüme yığılır, sıcak nokta doğar.</text>
  <text x="330" y="310" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Sıra: önce sorgu ve indeks, sonra okuma kopyası, en sonda parçalama.</text>
</svg>
```

### Bu bölümden çıkan ders

Bu yedi cevabın ortak özelliği şu: hiçbiri yanlış değil. Bilgi yerinde, kavramlar doğru adlandırılmış. Ortak zayıflık bilgide değil, cevabın uzunluğunda. Cevapların çoğu tanımda bitiyor; örnek, ölçüt ve bedel eklenmiyor. Bu eşzamanlılıkta özellikle pahalıdır, çünkü buradaki takip zinciri kısa ve dik: tanım, bellek modeli, kilit seçimi. İki soruda derinliğin sınırına gelinir. `volatile` cevabın istisnaydı ve nedeni tam olarak buydu; orada tanımla yetinmedin, sınırı da kendin söyledin. Aynı kalıbı diğer altısına uygula: tanım, bir örnek, bir sınır cümlesi. Veri tarafında da ölçüt cümlesi eksikti; indeks sorusunda takası kurdun, aynı refleksi bölümleme ve parçalama kararında da göster.

# dkmcon-1 | Temel
## Soru
Bir sınıfta `private int sayac;` alanı var ve `artir()` metodu yalnız `sayac++;` satırını içeriyor. Metot iki iş parçacığından çağrılınca sayaç beklenenden düşük kalıyor. Temel sebep nedir?
## A
Alan `private` olduğu için ikinci iş parçacığı güncel değeri okuyamıyor ve kendi kopyasını sıfırdan başlatıyor
🤖 erişim belirleyici eşzamanlılıkla ilgili değildir; kopyalama da olmaz.
## B
`int` yerine `Integer` kullanılmadığı için kutulama sırasında bazı artırmalar sessizce kayboluyor
🤖 `int` alanda kutulama gerçekleşmez; sorun tip seçiminde değildir.
## C
`sayac++` tek satır olsa da oku-artır-yaz üç adımıdır; iki iş parçacığı aynı eski değeri okuyup birbirinin yazmasını ezer
🤖 Doğru: bileşik işlem araya girmeye açıktır.
## D
İşletim sistemi iş parçacıklarını farklı çekirdeklere dağıttığı için toplama işleminde yuvarlama hatası oluşuyor
🤖 tam sayı toplamada yuvarlama yoktur; sorun araya girmedir.
## Doğru
C
## Açıklama
`sayac++` kaynak kodda tek satırdır, ama çalışma anında üç adıma açılır: değeri oku, bir artır, geri yaz. Bu üç adımın arasına başka bir iş parçacığı girebilir. İki iş parçacığı da `7` değerini okur, ikisi de `8` hesaplar, ikisi de `8` yazar. İki artırma yapılmıştır ama sayaç yalnız bir artmıştır; buna **kayıp güncelleme** denir (`46`). A şıkkı erişim belirleyiciyi eşzamanlılıkla karıştırır; `private` yalnız görünürlük kuralıdır, bellek davranışını değiştirmez. B şıkkı kutulamayı sebep sanar; ilkel `int` alanda kutulama yoktur. D şıkkı tam sayı toplamada olmayan bir hatayı öne sürer. Çözüm iki yönlüdür ve ayrı ayrı düşünülür. Atomiklik için `synchronized` blok, `ReentrantLock` ya da `AtomicInteger` kullanılır (`47`, `51`). Görünürlük için `volatile` yeter ama atomiklik için yetmez; bu ayrım bu dosyanın omurgasıdır (`48`, `45`).

# dkmcon-2 | Orta
## Soru
Bir sayacı `AtomicInteger` yaparak yarışı çözdün. Şimdi aynı nesnede `bakiye` ve `sonIslemZamani` alanları her işlemde birlikte güncellenecek ve dışarıya tutarlı görünmeli. En doğru yaklaşım hangisidir?
## A
İki alanı tek kilit altında birlikte güncellemek; atomik sınıflar yalnız tek değişken garantisi verir, alanlar arası tutarlılık vermez
🤖 Doğru: bileşik durum için kilit ya da işlem sınırı gerekir.
## B
Her iki alanı ayrı birer atomik sınıfa çevirmek; iki atomik güncelleme arka arkaya yapıldığında bütün de atomik olur
🤖 iki atomik işlem arasında başka bir iş parçacığı araya girebilir.
## C
Her iki alanı `volatile` yapmak; görünürlük garanti edildiğinde alanlar arası tutarlılık da kendiliğinden sağlanır
🤖 `volatile` görünürlük verir; iki alanın birlikteliğini korumaz.
## D
İki alanı da `final` yapmak; değişmez alanlar eşzamanlı erişimde hiçbir zaman sorun çıkarmaz
🤖 senaryo güncelleme istiyor; `final` alan yeniden atanamaz.
## Açıklama yerine önce şık listesi biter
## Doğru
A
## Açıklama
Atomik sınıflar tek bir değişken üzerinde oku-değiştir-yaz işlemini bölünmez kılar. Bunu karşılaştır ve değiştir (compare-and-swap) döngüsüyle yapar: eski değeri okur, beklenen değerle karşılaştırır, tutuyorsa yazar, tutmuyorsa baştan dener (`51`). Bu garanti **tek değişkenle sınırlıdır**. İki alan birlikte tutarlı olacaksa iki ayrı atomik işlem yetmez; birinci güncelleme ile ikinci güncelleme arasında başka bir iş parçacığı nesneyi yarı güncellenmiş hâlde okuyabilir. B şıkkının hatası tam olarak budur. C şıkkı `volatile`'ı tutarlılık aracı sanar; `volatile` yalnız görünürlük ve sıra garantisi verir, karşılıklı dışlama sağlamaz (`48`). D şıkkı senaryoyu değiştirir; güncellenmesi gereken alan `final` yapılamaz. Doğru yaklaşım iki alanı tek kritik bölümde güncellemektir. Kritik bölümü dar tut, çünkü kilit ne kadar geniş olursa iş hacmi (throughput) o kadar düşer (`46`, `47`). Aynı sorun veri tabanında da çıkar; orada karşılığı işlem sınırıdır (`84`).

# dkmcon-3 | Orta
## Soru
`volatile` anahtar kelimesi hakkında hangi ifade doğrudur?
## A
`volatile` alan üzerindeki `i++` işlemi atomik hâle gelir; bu yüzden paylaşılan sayaçlarda ayrıca kilit gerekmez
🤖 `i++` üç adım olarak kalır; `volatile` onu atomik yapmaz.
## B
`volatile` alanı her okuma arka planda bir kilit alır; bu nedenle maliyeti `synchronized` ile aynıdır
🤖 kilit alınmaz; maliyet bellek engelinden gelir, karşılıklı dışlamadan değil.
## C
`volatile` yalnız çok çekirdekli makinelerde anlamlıdır; tek çekirdekli ortamda hiçbir etkisi olmaz
🤖 derleyici ve JIT yeniden sıralaması tek çekirdekte de sorun üretir.
## D
`volatile` yazılan değerin diğer iş parçacıklarına görünürlüğünü garanti eder, ama oku-değiştir-yaz işlemini atomik yapmaz
🤖 Doğru: görünürlük ayrı, atomiklik ayrı bir garantidir.
## Doğru
D
## Açıklama
`volatile` iki şey verir. Birincisi görünürlüktür: alana yazılan değer diğer iş parçacıkları tarafından hemen görülür. İkincisi sıra garantisidir: `volatile` yazma ile sonraki `volatile` okuma arasında happens-before ilişkisi kurulur, yani yazmadan önceki tüm etkiler de o okumaya görünür olur (`45`). Vermediği şey atomikliktir. `i++` hâlâ oku-artır-yaz üç adımıdır ve araya girmeye açıktır; sayaç için `AtomicInteger` ya da kilit gerekir (`51`). A şıkkı bu dosyanın uyardığı klasik hatadır. B şıkkı maliyeti yanlış açıklar; `volatile` kilit almaz, bellek engeli koyar ve genellikle kilitten ucuzdur. C şıkkı sorunu yalnız donanıma bağlar; oysa derleyici ve JIT yeniden sıralaması tek çekirdekte de bayrak döngülerini bozar. `volatile`'ın doğru kullanım alanı dardır: yeni değerin eski değere bağlı olmadığı durumlar, örneğin durdurma bayrağı. İleri düzey bir kullanım daha vardır: çift kontrollü kilitlemede alan `volatile` değilse yarı kurulmuş nesne sızabilir (`133`).

# dkmcon-4 | İleri
## Soru
`employee(id, name, city, salary)` tablosunda **her şehirdeki en yüksek maaşlı çalışanın adı** isteniyor. Hangi yaklaşım doğrudur?
## A
`SELECT city, name, MAX(salary) FROM employee GROUP BY city` yazmak; gruplama zaten en yüksek maaşlı satırı seçer
🤖 `MAX` yalnız değeri döner; `name` o satıra ait olmak zorunda değildir.
## B
`ROW_NUMBER() OVER (PARTITION BY city ORDER BY salary DESC)` ile numaralandırıp dış sorguda birinci sıradaki satırları almak
🤖 Doğru: pencere fonksiyonu satırı korur, grup dışı kolon seçilebilir.
## C
`GROUP BY city` yazıp `HAVING salary = MAX(salary)` koşulu eklemek; koşul her grupta tek satır bırakır
🤖 `HAVING` grup üzerinde çalışır; gruplanmamış satır kolonu böyle kullanılamaz.
## D
`SELECT DISTINCT city, name FROM employee ORDER BY salary DESC` yazıp ilk satırı almak; sıralama en yükseği başa taşır
🤖 tek satır döner; her şehir için ayrı sonuç üretmez.
## Doğru
B
## Açıklama
`GROUP BY` her grup için tek satır üretir ve seçilebilecek kolonları sınırlar: ya gruplama kolonu ya da bir toplama fonksiyonu. `MAX(salary)` en yüksek maaşı verir ama o maaşın kime ait olduğunu vermez; A şıkkının hatası budur ve bazı veri tabanlarında sorgu hata bile döndürür. C şıkkı `HAVING` cümlesini satır süzgeci sanır; `HAVING` gruplama sonrasında grup üzerinde çalışır (`29`). D şıkkı hem `DISTINCT` ile sıralamayı karıştırır hem de tek satır döndürür. Doğru araç **pencere fonksiyonudur**. `ROW_NUMBER() OVER (PARTITION BY city ORDER BY salary DESC)` satırları gruplamadan, her şehir içinde sıralayarak numaralandırır. Dış sorguda numarası bire eşit olan satırlar alınır ve `name` kolonu doğal olarak seçilebilir. Eşit maaşlar önemliyse `RANK` kullanılır; o zaman beraberlik hâlinde birden fazla satır döner. Basit hâli, yani şehir başına ortalama, `GROUP BY city` ile `AVG(salary)` şeklinde yazılır ve eşik süzgeci `HAVING` ile eklenir (`29`, `146`).

# dkmcon-5 | İleri
## Soru
`users(email)` üzerinde indeks var, ama `WHERE UPPER(email) = ?` sorgusu tam tarama yapıyor. Ayrıca `(city, salary)` bileşik indeksi, yalnız `salary` ile filtreleyen sorgularda hiç kullanılmıyor. Bu iki gözlemin ortak açıklaması nedir?
## A
İndeksler yalnız sıralama ve birleştirme için çalışır; süzgeç koşullarında veri tabanı zaten tam tarama yapmayı tercih eder
🤖 indeks tam olarak süzgeç koşulu için vardır; tercih koşulun biçimine bağlıdır.
## B
İndeks istatistikleri bayatlamıştır; istatistikler yenilenirse her iki sorgu da mevcut indeksleri kullanmaya başlar
🤖 istatistik planı etkiler, ama buradaki iki durumun sebebi yapısaldır.
## C
Her iki indeks de bozulmuştur; indeksleri silip yeniden oluşturmak iki sorunu birden ortadan kaldırır
🤖 bozulma yok; erişim yolu koşulun yazımı yüzünden kapanıyor.
## D
İndeks kolonun ham değerine göre sıralıdır; fonksiyonlu koşul ve soldan ek kuralı erişim yolunu kapatır
🤖 Doğru: fonksiyon tabanlı indeks ya da doğru kolon sırası gerekir.
## Doğru
D
## Açıklama
İndeks, kolonun **ham değerine** göre sıralanmış bir yapıdır. `UPPER(email)` yazdığında aradığın şey artık kolonun kendisi değil, bir fonksiyonun sonucudur; sıralı yapı bu sonuç için geçerli değildir ve veri tabanı tam taramaya döner. Çözüm ya fonksiyon tabanlı indeks tanımlamak ya da koşulu kolonun ham hâline çevirmektir. Bileşik indekste ise **soldan ek** kuralı geçerlidir: `(city, salary)` indeksi önce `city`, sonra her `city` içinde `salary` sırasındadır. Yalnız `salary` ile filtreleyen sorgu bu sıralamadan yararlanamaz, çünkü aradığı değerler indeks boyunca dağılmıştır. İki durumun ortak kökü aynıdır: erişim yolu, koşulun indeksin sıralama düzenine oturmasına bağlıdır (`146`). B şıkkı istatistiği tek sebep sanar; istatistik planı etkiler ama bu iki durum yapısaldır. C şıkkı bozulma varsayar; indeksler sağlamdır. İndeks kararı yürütme planına bakılarak verilir ve karşılığında yazma maliyeti kabul edilir (`227`).

# dkmcon-6 | Uzman
## Soru
Sipariş tablosu tek düğümde büyümüş ve ekip parçalamayı (sharding) tartışıyor. Bölüm anahtarı olarak artan sipariş kimliği öneriliyor. En yerinde itiraz hangisidir?
## A
Parçalama yerine bölümleme her zaman yeterlidir; veri tek veritabanında kaldığı sürece ölçek sorunu zaten ortaya çıkmaz
🤖 bölümleme taranan veriyi küçültür, tek düğümün sınırını kaldırmaz.
## B
Sipariş kimliği fazla seçici olduğu için anahtar olamaz; bölüm anahtarı az sayıda farklı değer taşıyan bir kolondan seçilmelidir
🤖 tersi doğrudur; az değerli kolon dağılımı bozar ve düğümleri dengesizleştirir.
## C
Artan kimlik yeni kayıtları tek düğüme yığar; sıcak nokta doğar ve düğüm eklendiğinde yeniden dengeleme pahalıya gelir
🤖 Doğru: anahtar hem düzgün dağılmalı hem sorgularda bulunmalı.
## D
Parçalamadan sonra çapraz düğüm birleştirme ve dağıtık işlem sorunu ortadan kalkar; bu yüzden anahtar seçimi kritik değildir
🤖 tersi olur; bu iki sorun parçalamayla birlikte gelir.
## Doğru
C
## Açıklama
Bölüm anahtarı parçalamanın en pahalı kararıdır, çünkü sonradan değiştirmek verinin tamamını taşımak demektir. İyi anahtarın iki şartı vardır. Birincisi dağılımın düzgün olmasıdır; ikincisi anahtarın sorguların çoğunda hazır bulunmasıdır. Artan kimlik ikinci şartı sağlar ama birincisini bozar: yeni kayıtların tamamı sıradaki tek düğüme yazılır ve **sıcak nokta** oluşur (`147`). Yazma yükü dağılmadığı için parçalamanın asıl kazancı alınmamış olur. Düğüm eklendiğinde de sorun büyür; basit `hash % N` yönteminde neredeyse tüm anahtarlar yer değiştirir. Tutarlı özetleme (consistent hashing) ve sanal düğüm, yeniden dengelemede taşınan veriyi azaltır. A şıkkı bölümlemeyi parçalamanın yerine koyar; bölümleme aynı düğümde kalır ve disk ile işlemci sınırını kaldırmaz. B şıkkı seçiciliği ters yorumlar. D şıkkı en tehlikeli yanılgıdır: çapraz düğüm birleştirme ve dağıtık işlem sorunları parçalamanın bedelidir, kazancı değil. Bu yüzden sıra bellidir: önce sorgu ve indeks, sonra okuma kopyası ve önbellek, en sonda parçalama (`146`, `157`).
