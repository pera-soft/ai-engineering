---
slug: structured-concurrency
baslik: "Yapısal Eşzamanlılık (Structured Concurrency)"
kademe: İleri
sira: 58
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Markete üç kişi gönderdiğinizi düşünün: biri ekmek, biri süt, biri yumurta alacak. Kapıdan çıktıktan sonra onları izleyemiyorsanız, biri kaybolduğunda kimsenin haberi olmaz; ötekiler boşuna alışverişe devam eder ve kimse kimseye "vazgeçtik, dön" diyemez. Klasik iplik kullanımı böyle dağınıktır. Yapısal eşzamanlılık bu üç kişiyi **tek bir gezi** gibi ele alır: hepsi birlikte çıkar, biri başarısız olursa hepsi geri çağrılır ve herkes dönmeden kapı kapanmaz.

### Temel kavramlar

- **Görev (task) ve alt görev (subtask)**: bir işi parçalayıp aynı anda yürütülen küçük parçalar.
- **Sözcüksel kapsam (lexical scope)**: kodun süslü parantezlerle çizilen sınırı. Yapısal eşzamanlılıkta alt görevler bu sınırın **içinde doğar, biter ve toplanır**.
- **Sızan iplik (leaked thread)**: başlatıldıktan sonra sahibi kalmayan; kimsenin beklemediği, kimsenin iptal etmediği iplik.
- **`StructuredTaskScope`**: bu modelin ana API'sidir; birbirine bağlı görevleri **tek bir birim** gibi tutan kapsam nesnesidir.
- **İptalin yayılması**: kapsam sonlanma kararı verdiğinde iptal **yukarıdan aşağı** alt görevlere yayılır; kapsam kapanmadan bloktan çıkış yoktur.

İki klasik kalıp vardır:

- **ShutdownOnFailure**: alt görevlerden **biri başarısız olursa** diğerleri iptal edilir (hepsi gerekli — "all-or-nothing").
- **ShutdownOnSuccess**: alt görevlerden **biri başarılı olur olmaz** kalanlar iptal edilir (ilk kazanan yeter — "invoke-any").

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yapısal eşzamanlılıkta alt görevler ana kapsamda doğar ve birlikte toplanır, iptal aşağı yayılır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek kapsam: alt görevler birlikte doğar, biter, toplanır</text>
  <rect x="60" y="44" width="520" height="176" rx="14" fill="#EEF6FF" stroke="#2C6BB3" stroke-width="1.6"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#2C6BB3" font-family="system-ui">StructuredTaskScope (ana görev kapsamı)</text>
  <rect x="100" y="92" width="130" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="118" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">alt görev A</text>
  <text x="165" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">fork()</text>
  <rect x="255" y="92" width="130" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="118" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">alt görev B</text>
  <text x="320" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">fork()</text>
  <rect x="410" y="92" width="130" height="60" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="475" y="114" text-anchor="middle" font-size="11.5" font-weight="600" fill="#B4322B" font-family="system-ui">alt görev C</text>
  <text x="475" y="132" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">HATA → iptal yayılır</text>
  <text x="320" y="182" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">join() → hepsi biter · biri patlarsa diğerleri iptal (ShutdownOnFailure)</text>
  <text x="320" y="206" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">iptal yukarıdan aşağı yayılır; kapsam kapanmadan çıkış yok</text>
</svg>
```

### Nasıl çalışır?

Kullanım iskeleti üç adımdır: fork → join → sonucu topla.

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var a = scope.fork(() -> kullaniciGetir());  // kendi sanal ipliğinde başlar
    var b = scope.fork(() -> siparisGetir());
    scope.join();                                // senkron bariyer
    scope.throwIfFailed();                       // varsa ilk hatayı yukarı fırlat
    return birlestir(a.get(), b.get());          // sonuçlar ancak burada güvenli
}
```

`fork(...)` her alt görevi **kendi sanal ipliğinde** başlatır ve bir tutamaç (handle) döndürür. `scope.join()`, tüm alt görevlerin bitmesini — ya da kapsamın iptalle erken sonlanmasını — bekleyen **senkron bariyerdir**; yeni bir iplik havuzu kurmaz, kendisi iptal etmez, hiçbir şeyi veritabanına yazmaz. Alt görev sonuçları ancak `join()` döndükten sonra güvenle okunur. Ardından `ShutdownOnFailure`'da `throwIfFailed()` ilk hatayı yukarı fırlatır, `ShutdownOnSuccess`'te `result()` ilk başarılı sonucu verir.

İki kalıp arasındaki fark, iptalin **ne zaman** tetiklendiğidir: `ShutdownOnFailure` ilk **hatada**, `ShutdownOnSuccess` ilk **başarıda** kalan alt görevleri iptal eder. Hiçbiri otomatik yeniden deneme yapmaz ve hiçbiri hatayı yok saymaz.

```svg
<svg viewBox="0 0 640 216" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ShutdownOnFailure ilk hatada kalanları iptal eder all-or-nothing, ShutdownOnSuccess ilk başarıda kalanları iptal eder invoke-any">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki kalıp: Failure vs Success</text>
  <rect x="28" y="42" width="288" height="162" rx="12" fill="#F5DADA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="172" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">ShutdownOnFailure</text>
  <text x="172" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"hepsi gerekli" (all-or-nothing)</text>
  <circle cx="90" cy="120" r="11" fill="#25804C"/><text x="90" y="150" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">A çalışır</text>
  <circle cx="172" cy="120" r="11" fill="#8A2E2E"/><text x="172" y="150" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">B hata</text>
  <circle cx="254" cy="120" r="11" fill="#B26B00"/><text x="254" y="150" text-anchor="middle" font-size="10" fill="#B26B00" font-family="system-ui">C iptal</text>
  <text x="172" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">biri patlar → kalanlar iptal, hata yukarı</text>
  <rect x="324" y="42" width="288" height="162" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="468" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">ShutdownOnSuccess</text>
  <text x="468" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"ilk kazanan" (invoke-any)</text>
  <circle cx="386" cy="120" r="11" fill="#25804C"/><text x="386" y="150" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">A ilk ✓</text>
  <circle cx="468" cy="120" r="11" fill="#B26B00"/><text x="468" y="150" text-anchor="middle" font-size="10" fill="#B26B00" font-family="system-ui">B iptal</text>
  <circle cx="550" cy="120" r="11" fill="#B26B00"/><text x="550" y="150" text-anchor="middle" font-size="10" fill="#B26B00" font-family="system-ui">C iptal</text>
  <text x="468" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ilk başarı → kalanlar iptal, sonuç döner</text>
</svg>
```

Bu modelin asıl kazancı, yapısal olmayan (unstructured) eşzamanlılıkla karşılaştırınca netleşir: klasik `ExecutorService` ile başlatılan alt görevler başlatan koddan **bağımsız yaşar**, hata yayılımı belirsizdir, sahiplik ağacı yoktur ve iplikler **sızabilir**. Yapısal kapsamda ise hepsi blok kapanmadan toplanır; yaşam döngüsü öngörülebilirdir.

Alt görevlere bağlam taşırken **`ScopedValue`** kullanılır: `ScopedValue.where(ANAHTAR, deger).run(...)` yalnız o çağrı kapsamında geçerli olan, **değiştirilemez (immutable)** ve **sınırlı ömürlü (bounded)** bir değer taşır; kapsam bitince kendiliğinden geçersizleşir. `ThreadLocal` ise değiştirilebilir (mutable), ipliğin tüm ömrü boyunca yaşayabilir ve temizlenmezse sızıntı yapar; üstelik milyonlarca sanal iplikte iplik-başı kopyalar ağır bir bellek yüküdür. `ScopedValue` ne "her ipliğe daha büyük değer kopyalar" ne de global-değiştirilebilir bir kutudur; çekirdek sayısıyla da ilgisi yoktur. **`ScopedValue` Java 25'te kalıcılaşmıştır** (yapısal eşzamanlılığın kendisi ise hâlâ önizlemedir).

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yapısal olmayan modelde alt görevler kapsamdan sızabilir, yapısal modelde kapsam kapanmadan hepsi toplanır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sızan iplikler vs yapısal kapsam</text>
  <rect x="28" y="42" width="288" height="150" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="172" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Klasik ExecutorService</text>
  <text x="172" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">alt görevler bağımsız yaşar</text>
  <text x="172" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hata yayılımı belirsiz</text>
  <text x="172" y="134" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">iplikler sızabilir (leak)</text>
  <text x="172" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sahiplik ağacı yok</text>
  <rect x="324" y="42" width="288" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="468" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">StructuredTaskScope</text>
  <text x="468" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">alt görevler kapsamda doğar</text>
  <text x="468" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">blok kapanmadan hepsi biter</text>
  <text x="468" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hata/iptal ağaç boyunca yayılır</text>
  <text x="468" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">öngörülebilir yaşam döngüsü</text>
</svg>
```

### Mülakatta ne sorulur?

- **Hangi sorunu çözer?** Amacı, alt görevleri başlatan kodun kapsamına bağlayarak **sızan iplikleri ve dağınık iptali** önlemektir. Amaç ham hız (komutları yeniden sıralayarak hızlanma) **değildir**. Çöp toplamayı hızlandırmak ya da SQL'leri paralel çalıştıran bir veritabanı sürücüsü sağlamak da değildir.
- **`ShutdownOnFailure`.** "Hepsi gerekli" (all-or-nothing): biri başarısız olur olmaz kalanlar **iptal edilir** ve hata `throwIfFailed()` ile yukarı taşınır. Otomatik yeniden deneme yoktur; hata yok sayılıp herkesin bitmesi beklenmez.
- **`ShutdownOnSuccess`.** "İlk yanıt veren kazanır" (invoke-any): birden çok kaynaktan aynı veriyi çekip ilk başarılı sonucu `result()` ile almak, kalanları iptal etmek. Kaynakları tek tek sırayla çağırmak paralellik avantajını yok eder; en yavaş kaynak sırayı tıkar.
- **`join()`'in rolü.** Forklanan tüm alt görevlerin tamamlanmasını (veya kapsamın iptalle sonlanmasını) bekleyen bariyerdir; `a.get()`/`b.get()` ancak ondan sonra güvenlidir.
- **SÜRÜM GERÇEĞİ.** Yapısal eşzamanlılık **JDK 21'den beri önizleme (preview)** JEP'leriyle gelir ve **Java 25'te HÂLÂ preview'dur**: `--enable-preview` gerektirir, API sürümler arasında değişebilir, kararlı/nihai sayılmaz. Java 21'de **final olmadı**, kaldırılmadı ve üçüncü-parti bir kütüphane değildir — JDK'nin `java.util.concurrent` parçasıdır. Yeni biçimde `StructuredTaskScope.open(...)` ve `Joiner` kullanılır; kavramlar aynıdır. Aynı Loom şemsiyesinde kalıcılaşan, yapısal eşzamanlılığın kendisi değil **`ScopedValue`**'dur (Java 25).

### Tek cümlede

Yapısal eşzamanlılık, alt görevleri başlatan kodun kapsamına bağlayıp birlikte doğmalarını, bitmelerini ve gerektiğinde birlikte iptal edilmelerini garanti eden — Java 25'te hâlâ önizleme aşamasındaki — bir eşzamanlılık modelidir.

# struct-1 | İleri
## Soru
Yapısal eşzamanlılığın (structured concurrency) çözmeye çalıştığı temel sorun nedir?
## A
Tek çekirdekte yapılan hesabı, komutları yeniden sıralayarak daha hızlı bitirmek
🤖 Amaç ham hız değil; görev yaşam döngüsünü yapısal olarak yönetmektir.
## B
Alt görevlerin, kendilerini başlatan kodun kapsamında doğup birlikte bitmesini sağlayarak sızan (leaked) iplikleri önlemek
🤖 Doğru: alt görevleri tek birim gibi yöneterek sızıntıyı ve dağınık iptali önler.
## C
Belleği otomatik boşaltmak, yani çöp toplamayı (GC) hızlandırıp duraklamaları kısaltmak
🤖 GC ile ilgisi yoktur; konu görev/iplik yaşam döngüsüdür.
## D
SQL sorgularını paralel çalıştırmak için özel bir veritabanı sürücüsü (driver) sağlamak
🤖 Veritabanına özgü bir mekanizma değil; genel bir eşzamanlılık modelidir.
## Doğru
B
## Açıklama
Geleneksel `ExecutorService` ile başlatılan görevler, başlatan koddan **bağımsız** yaşayabilir: hata durumunda kimin kimi iptal edeceği, kimin sonucu bekleyeceği belirsizdir; iplikler "sızabilir" (leak). **Yapısal eşzamanlılık**, alt görevleri başlatan kodun **sözcüksel kapsamına (lexical scope)** bağlar: alt görevler o blokta doğar, blok kapanmadan hepsi biter/iptal edilir ve sonuçlar toplanır. Böylece görev ağacı, tıpkı yapısal programlamadaki blok yapısı gibi, öngörülebilir olur. A (hız), C (GC) ve D (SQL) konunun dışındadır; mesele **görev yaşam döngüsünün yapısal yönetimi**dir.

# struct-2 | İleri
## Soru
`StructuredTaskScope.ShutdownOnFailure` kalıbı ne yapar?
## A
Alt görevlerden biri başarısız olursa kapsam kalan alt görevleri iptal eder ve hata yukarı taşınır
🤖 Doğru: bir alt görev patlarsa diğerleri iptal edilir; hepsi gerekli senaryosudur.
## B
İlk başarılı alt görev dönünce diğerlerini iptal eder ve o sonucu döndürür
🤖 Bu `ShutdownOnSuccess` davranışıdır; failure değil.
## C
Başarısız görevi, diğerleri beklerken otomatik olarak sonsuz kez yeniden dener
🤖 Otomatik yeniden deneme yoktur; kalan görevleri iptal eder.
## D
Hataları yok sayıp tüm görevlerin bitmesini bekler, hata yukarı taşınmaz
🤖 Hatayı yok saymaz; ilk hatada kalanları iptal eder ve hatayı taşır.
## Doğru
A
## Açıklama
`ShutdownOnFailure`, "**hepsi gerekli**" (all-or-nothing) senaryosu içindir: alt görevlerden biri başarısız olur olmaz kapsam kalan alt görevleri **iptal eder** ve `throwIfFailed()` ile ilk hata çağırana yukarı taşınır. Böylece bir parça başarısızsa gereksiz iş yapılmaz. B tam tersini — ilk **başarıda** durmayı — anlatır ki bu `ShutdownOnSuccess`'tir. Otomatik yeniden deneme (C) bu kalıbın işi değildir. Hatayı yok saymak (D) da yanlıştır; kalıbın amacı tam olarak ilk hatada erken ve düzenli sonlanmaktır. (Not: Java 25 preview API'sinde bu kalıplar `Joiner` tabanlı biçimlere evrilmektedir.)

# struct-3 | İleri
## Soru
Birden çok kaynaktan aynı veriyi çekip **ilk yanıt vereni** kullanmak istiyorsanız hangi kalıp uygundur?
## A
`ShutdownOnFailure`: hepsinin başarılı bitmesini bekler, ilk hatada kalanları iptal eder
🤖 Bu "hepsi gerekli" kalıbıdır; ilk başarıda durmaz, ilk hatada durur.
## B
Hiçbir kalıp uygun değildir; yapısal eşzamanlılık "ilk kazanan" senaryosunu desteklemez
🤖 Destekler; tam olarak `ShutdownOnSuccess` bunun içindir.
## C
Kaynakları tek tek, sırayla çağırıp ilk yanıt gelince durmak
🤖 Seri yürütme paralel "ilk kazanan" avantajını yok eder; en yavaş kaynak sırayı tıkar.
## D
`ShutdownOnSuccess`: ilk başarılı alt görevin sonucunu döndürür, kalanları iptal eder
🤖 Doğru: "ilk başarı yeter" (invoke-any) senaryosu tam olarak budur.
## Doğru
D
## Açıklama
"**İlk yanıt veren kazanır**" (invoke-any) senaryosu `ShutdownOnSuccess` kalıbıdır: birden çok alt görev paralel başlatılır, **ilki başarıyla dönünce** sonucu `result()` ile alınır ve kalan alt görevler **iptal** edilir — böylece gereksiz beklemeyen, hızlı bir "ilk kazanan" davranışı elde edilir. `ShutdownOnFailure` (A) tam tersine hepsinin başarılı olmasını bekleyip ilk **hatada** durur. Görevleri seri çalıştırmak (C) paralellik avantajını yok eder. "Yapılamaz" (B) yanlıştır; bu tam da yapısal eşzamanlılığın sunduğu iki temel kalıptan biridir.

# struct-4 | İleri
## Soru
`ScopedValue`'nun `ThreadLocal`'a göre avantajı nedir?
## A
`ScopedValue` her ipliğe daha büyük bir değer kopyalar, böylece daha çok veri tutar
🤖 Amaç daha çok veri değil; tam tersine hafif ve değiştirilemez bağlamdır.
## B
`ScopedValue` yalnız tek çekirdekli sistemlerde çalışır
🤖 Böyle bir sınır yoktur; çekirdek sayısıyla ilgisi yoktur.
## C
Değiştirilemez (immutable) ve sınırlı ömürlüdür (bounded): yalnız belirli bir kapsamda geçerlidir, çıkışta otomatik temizlenir ve daha hafiftir
🤖 Doğru: immutable + bounded olması sızıntıyı ve iplik-başı bellek yükünü azaltır.
## D
`ScopedValue` değeri istenildiği zaman her yerden değiştirilebilir (mutable global) yapar, böylece bağlam her katmandan güncellenebilir
🤖 Tam tersi; `ScopedValue` değiştirilemezdir, global mutable değildir.
## Doğru
C
## Açıklama
`ThreadLocal` değiştirilebilir (mutable) ve ipliğin tüm ömrü boyunca yaşayabilir; temizlenmezse sızıntıya yol açar ve milyonlarca sanal iplikte iplik-başı kopyalar büyük bellek yükü olur. **`ScopedValue`** ise **değiştirilemez (immutable)** ve **sınırlı ömürlüdür (bounded)**: `ScopedValue.where(K, v).run(...)` ile yalnız o çağrı kapsamında geçerli olur, kapsam bitince otomatik "temizlenir". Bu, alt görevlere (özellikle yapısal eşzamanlılıkta) bağlam geçirmenin güvenli ve hafif yoludur. A ve D `ScopedValue`'yu yanlış tanımlar (ne daha çok veri kopyalar ne de mutable-global'dır); B ise uydurma bir kısıttır. `ScopedValue` Java 25'te kalıcılaşmıştır.

# struct-5 | Uzman
## Soru
Java 25 itibarıyla yapısal eşzamanlılığın (structured concurrency) olgunluk durumu nedir?
## A
Java 21'de kalıcılaştı (final); artık kararlı ve değişmeyen standart bir API'dir
🤖 Yanlış; Java 21'de de önizlemeydi, Java 25'te hâlâ preview ve API evriliyor.
## B
Hâlâ preview (önizleme) aşamasındadır; `--enable-preview` gerektirir ve API sürümler arasında değişebilir
🤖 Doğru: Java 25'te hâlâ önizleme; API kararlı kabul edilmez.
## C
Tamamen kaldırıldı; Java 25'te JDK içinde böyle bir API artık bulunmuyor
🤖 Kaldırılmadı; önizleme olarak gelişmeye devam ediyor.
## D
Yalnız bir üçüncü-parti kütüphanedir; JDK ile birlikte gelmez, ayrıca bağımlılık eklenir
🤖 JDK'nin `java.util.concurrent` parçası olarak (preview) gelir; üçüncü-parti değildir.
## Doğru
B
## Açıklama
Yapısal eşzamanlılık, birkaç JEP turu boyunca olgunlaşmakta olan bir özelliktir ve **Java 25'te hâlâ preview (önizleme)** statüsündedir — yani `--enable-preview` gerektirir, API'si sürümler arasında **değişebilir** ve kararlı/nihai kabul edilmez. Bu yüzden A ("Java 21'de final oldu") yanlıştır; kalıcılaşan, aynı Loom şemsiyesindeki `ScopedValue`'dur, yapısal eşzamanlılığın kendisi değil. C ("kaldırıldı") ve D ("üçüncü-parti") de yanlıştır: özellik JDK'nin bir parçası olarak önizlemede gelişmeyi sürdürmektedir. Üretimde kullanırken API'nin değişebileceği bilinmelidir.

# struct-6 | İleri
## Soru
Aşağıdaki taslak kodda `scope.join()` çağrısının rolü nedir?
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var a = scope.fork(() -> kullaniciGetir());
    var b = scope.fork(() -> siparisGetir());
    scope.join();            // (X)
    scope.throwIfFailed();
    return birlestir(a.get(), b.get());
}
```
## A
`join()` alt görevleri çalıştırmak için yeni bir iplik havuzu oluşturur ve onları başlatır
🤖 Havuz oluşturmaz; forklanan alt görevlerin bitmesini bekler.
## B
Tüm forklanan alt görevlerin tamamlanmasını bekler; sonuçlar ancak `join()`'den sonra güvenle okunur
🤖 Doğru: `join()` alt görevleri toplayan senkron bariyerdir.
## C
Henüz bitmemiş alt görevleri iptal edip kapsamı kapatır
🤖 İptal işi `shutdown`/hata yayılımınındır; `join()` bekler.
## D
Alt görev sonuçlarını veritabanına yazıp kalıcılaştırır
🤖 Kalıcılaştırma yapmaz; yalnız alt görevlerin bitişini bekler.
## Doğru
B
## Açıklama
`fork(...)` alt görevleri başlatır ama sonuçları hemen hazır değildir. `scope.join()`, kapsamdaki **tüm alt görevlerin tamamlanmasını** (ya da `ShutdownOnFailure` altında bir hata olursa kapsamın erken sonlanmasını) bekleyen **senkron bariyerdir**; bu yüzden `a.get()`/`b.get()` ancak `join()`'den sonra güvenle okunur. `join()` yeni bir havuz kurmaz (A), doğrudan iptal etmez (C — iptal, hata durumunda kapsamın kendisi tarafından yayılır) ve hiçbir kalıcılaştırma yapmaz (D). `throwIfFailed()` ise varsa ilk hatayı yukarı fırlatır. (Java 25 preview API'sinde bu akış `Joiner` ve `scope.open(...)` biçimine doğru evrilmektedir; kavram aynıdır.)
