---
slug: exception-temelleri
baslik: "İstisna (Exception) Temelleri: checked/unchecked, try-catch-finally"
kademe: Temel
sira: 16
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir yemek tarifi uyguluyorsun ve ortada bir aksilik çıkıyor: un bitti. İki seçeneğin var — ya orada durup "yapamıyorum" diye bağırırsın, ya da baştan "un biterse markete giderim" planını hazırlarsın. Program da böyledir: dosya bulunamaz, ağ kopar, bölme sıfıra denk gelir. **İstisna (exception)** mekanizması, bu aksilikleri normal akıştan ayırıp özel bir yoldan yukarıya bildirmenin standart yoludur. Böylece hata kodlarını her satırda elle kontrol etmek zorunda kalmazsın.

### Temel kavramlar

**İstisna fırlatmak (throw)**: bir hata nesnesi üretip normal akışı kesmek. **Yakalamak (catch)**: bu nesneyi bir blokla karşılayıp ele almak. Yakalanmayan istisna metot metot yukarı **yayılır (propagate)**; en tepede de yakalanmazsa program o iş parçacığında (thread) sonlanır.

Java'da fırlatılabilen her şeyin atası `Throwable`'dır ve iki ana kola ayrılır:

- **`Error`**: JVM düzeyinde, genelde kurtarılamaz durumlar (`OutOfMemoryError`, `StackOverflowError`) — yakalamaya çalışılmaz.
- **`Exception`**: uygulama düzeyinde hatalar. Altında iki grup vardır:
  - **checked** (denetlenen) istisnalar: `Exception`'ın `RuntimeException` DIŞINDAKİ alt türleri (`IOException`, `SQLException`). Derleyici bunları ya `catch` ile yakalamanı ya da `throws` ile bildirmeni **zorlar**.
  - **unchecked** (denetlenmeyen): `RuntimeException` ve altları (`NullPointerException`, `IllegalArgumentException`). Derleyici zorlamaz; genelde programlama hatasıdır ve her yerde `try-catch` yazmak pratik olmayacağı için serbest bırakılır.

Dikkat: checked de unchecked de **`Exception` altındadır**; `Error` bunlardan tamamen ayrı bir koldur. unchecked istisnalar da `catch` ile yakalanabilir — sadece zorunlu değildir.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Throwable hiyerarşisi: Error ve Exception, checked ve unchecked">
  <rect x="250" y="16" width="140" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="41" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Throwable</text>
  <rect x="90" y="92" width="150" height="40" rx="10" fill="#FBEDEA" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="165" y="117" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Error</text>
  <rect x="400" y="92" width="150" height="40" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="117" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Exception</text>
  <rect x="330" y="168" width="130" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="395" y="188" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">checked</text>
  <text x="395" y="204" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">IOException...</text>
  <rect x="490" y="168" width="140" height="44" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="560" y="188" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">RuntimeException</text>
  <text x="560" y="204" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">unchecked</text>
  <path d="M300 56 L200 92" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exArrow)"/>
  <path d="M340 56 L450 92" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exArrow)"/>
  <path d="M460 132 L420 168" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exArrow)"/>
  <path d="M490 132 L540 168" stroke="#5D7789" stroke-width="1.5" marker-end="url(#exArrow)"/>
  <defs>
    <marker id="exArrow" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

Birbirine sık karıştırılan iki anahtar sözcük vardır. **`throw`** bir *deyimdir* (statement) ve çalışma anında bir istisna nesnesini fiilen fırlatır — `throw new IllegalArgumentException("...")`. **`throws`** ise metot *imzasının* parçasıdır ve metodun hangi istisnaları çağırana yayabileceğini **bildirir** — `void oku() throws IOException`. Kısaca: `throw` eylemi yapar, `throws` sözleşmeyi ilan eder. İkisi de hem checked hem unchecked istisnalarla kullanılabilir; ama bir metot checked bir istisnayı `throw` ediyor ve içeride yakalamıyorsa, onu imzasında `throws` ile bildirmek **zorundadır**.

### Nasıl çalışır?

Temel yapı `try` / `catch` / `finally`'dir:

```java
try {
    System.out.print("T");
    throw new RuntimeException();
} catch (RuntimeException e) {
    System.out.print("C");
} finally {
    System.out.print("F");
}
// çıktı: TCF
```

Akış şöyledir: `try` içinde `T` yazılır, istisna fırlatılır, akış kesilir ve eşleşen `catch` bloğuna atlanır (`C`). En son, istisna olsun olmasın **her durumda çalışan** `finally` bloğu koşar (`F`). İstisna yakalandığı için program sonlanmaz, akış normal biçimde devam eder.

Bir `try` bloğuna birden çok `catch` yazılabilir; bloklar **yukarıdan aşağıya** denenir ve **ilk eşleşen** çalışır. Bu yüzden `catch` blokları **en özelden en genele** sıralanmalıdır. Genel tip önce yazılırsa alttaki özel tipe hiç sıra gelmez:

```java
try { riskli(); }
catch (Exception e)   { }   // genel — her IOException'ı da yakalar
catch (IOException e) { }   // ULAŞILAMAZ → derleme hatası
// "exception IOException has already been caught"
```

Doğrusu `IOException` önce, `Exception` en sonda olacak şekilde sıralamaktır. Not: bu bir sıralama hatasıdır — çoklu `catch` yasaktır ya da `Exception` soyut (abstract) olduğu için yakalanamaz gibi bir durum söz konusu değildir.

`finally` bloğu, istisna olsun olmasın — hatta `try` içinde `return` yapılsa bile — **hemen hemen her zaman** çalışır; bu yüzden kaynak kapatmanın (dosya, bağlantı) doğal yeridir. Ama içine `return` (ya da `throw`) yazmak tehlikeli bir tuzaktır:

```java
static int f() {
    try { return 1; }
    finally { return 2; }   // 2 döner!
}
```

`finally`, `try` içindeki `return 1` değerlendirildikten SONRA ama metot geri dönmeden ÖNCE çalışır; oradaki `return`, `try`'ın döndüreceği değeri **ezer**. Daha kötüsü: `try` bir istisna fırlatmış olsaydı `finally`'deki `return` onu **yutar** ve metot sessizce normal dönüş yapardı. Kod geçerli derlenir — yani derleyici seni uyarmaz. Kural: `finally` içinde ASLA `return` veya `throw` yazma; `finally` yalnız temizlik içindir.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="try-catch-finally akışı: finally her yolda çalışır">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">try → (istisna?) → catch → finally HER yolda çalışır</text>
  <rect x="40" y="46" width="150" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="115" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">try bloğu</text>
  <text x="115" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">riskli kod</text>
  <rect x="245" y="46" width="150" height="48" rx="10" fill="#FBEDEA" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">catch</text>
  <text x="320" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">istisnayı yakalar</text>
  <rect x="450" y="46" width="150" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">finally</text>
  <text x="525" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kaynak kapatma</text>
  <path d="M190 70 L245 70" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#exc16a)"/>
  <text x="217" y="62" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">hata</text>
  <path d="M395 70 L450 70" stroke="#25804C" stroke-width="1.5" marker-end="url(#exc16a)"/>
  <path d="M115 94 C115 148, 525 148, 525 94" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#exc16a)"/>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hata YOKSA: try → doğrudan finally (catch atlanır)</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">finally içinde return YAPMA — catch'teki istisnayı yutar</text>
  <defs>
    <marker id="exc16a" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Mülakatta ne sorulur?

**checked / unchecked ayrımının özü derleme-zamanı zorlamasıdır** — performans farkı değildir. checked istisnada derleyici çağıranı `try-catch` ya da `throws` yapmaya mecbur bırakır; unchecked'te bırakmaz.

**`catch (Throwable t)` neredeyse her zaman bir anti-patterndir.** `Throwable` hiyerarşinin tepesidir: hem `Exception`'ları hem `Error`'ları yakalar, hiçbirini atlamaz. `Error` teknik olarak yakalanabilir (derleme hatası vermez) — sorun teknik değil, tasarımsaldır: `OutOfMemoryError`, `StackOverflowError` gibi kurtarılamaz durumları yutup "devam etmek" uygulamayı tutarsız bir durumda çalıştırır ve gerçek arızayı maskeler. Genel kural: yalnızca **anlamlı şekilde ele alabileceğin en özel** istisna tipini yakala, `Error`'ları yayılmaya bırak.

Sık çıkan diğer noktalar: `finally` `catch` çalışmasa bile koşar (hata yoksa `try` → doğrudan `finally`); `Exception` soyut bir sınıf değildir; bir metotta birden çok `catch` yazmak serbesttir, kısıt yalnızca sıralamadadır.

### Tek cümlede

İstisna, hatayı normal akıştan ayırıp yukarı taşır; checked'i derleyici zorlar, `catch` en özelden en genele sıralanır ve `finally` her yolda çalışır — ama içine asla `return` yazılmaz.

# exc-1 | Temel
## Soru
checked (denetlenen) ve unchecked (denetlenmeyen) istisna arasındaki temel fark nedir?
## A
Derleyici checked istisnaları `catch` veya `throws` ile ele almaya zorlar; unchecked için zorlamaz
🤖 Doğru: temel ayrım derleme-zamanı zorlaması olup olmamasıdır.
## B
checked istisnalar daha hızlı çalışır, unchecked daha yavaştır
🤖 Fark performans değil; derleyicinin ele almayı zorlayıp zorlamamasıdır.
## C
checked istisnalar `Error`, unchecked istisnalar `Exception` altındadır
🤖 İkisi de `Exception` altındadır; `Error` ayrı bir koldur.
## D
unchecked istisnalar yakalanamaz, yalnızca loglanabilir
🤖 unchecked istisnalar da `catch` ile yakalanabilir; sadece zorunlu değildir.
## Doğru
A
## Açıklama
Ayrım **derleme-zamanı zorlaması** üzerinedir. **checked** istisnalar (`Exception`'ın `RuntimeException` dışındaki alt türleri, örn. `IOException`) için derleyici, çağıran kodun ya bunu `try-catch` ile yakalamasını ya da metodun imzasında `throws` ile bildirmesini **zorlar**. **unchecked** istisnalar (`RuntimeException` ve altları) için böyle bir zorlama yoktur; bunlar genelde programlama hatalarıdır (null erişim, geçersiz argüman) ve her yerde `try-catch` yazmak pratik olmayacağından derleyici serbest bırakır. İkisi de `Exception` altındadır; `Error` ayrı bir koldur (C yanlış).

# exc-2 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
try {
    System.out.print("T");
    throw new RuntimeException();
} catch (RuntimeException e) {
    System.out.print("C");
} finally {
    System.out.print("F");
}
```
## A
`TCF`
🤖 Doğru: try başlar (T), istisna yakalanır (C), finally her hâlde çalışır (F).
## B
`TF`
🤖 İstisna yakalandığı için `catch` bloğu da çalışır; `C` eksik.
## C
`TC`
🤖 `finally` istisna olsa da olmasa da çalışır; `F` eksik.
## D
`T` ve ardından istisna programı sonlandırır
🤖 İstisna `catch` ile yakalandığından program sonlanmaz; akış devam eder.
## Doğru
A
## Açıklama
Akış şöyle ilerler: `try` içinde `T` yazdırılır, sonra `RuntimeException` fırlatılır. Bu istisna hemen eşleşen `catch (RuntimeException e)` tarafından yakalanır ve `C` yazdırılır. En son, istisna olsun olmasın **her durumda çalışan `finally`** bloğu `F` yazdırır. Sonuç: `TCF`. `finally`, kaynak temizliği (dosya/bağlantı kapatma) için tam da bu "ne olursa olsun çalış" garantisi yüzünden kullanılır.

# exc-3 | Orta
## Soru
`throw` ile `throws` arasındaki fark nedir?
## A
İkisi de aynıdır; `throws` yalnızca `throw`'un çoğul yazımıdır
🤖 Farklı kavramlardır: biri fiil (fırlatma), biri bildirim (imza).
## B
`throw` bir istisnayı fiilen fırlatır (deyim); `throws` metodun hangi istisnaları fırlatabileceğini imzada bildirir
🤖 Doğru: `throw` = eylem, `throws` = imza bildirimi.
## C
`throw` derleme zamanında, `throws` çalışma zamanında etki eder
🤖 İkisi de kaynağın anlamıyla ilgilidir; böyle bir zaman ayrımı yanlış.
## D
`throw` yalnızca checked, `throws` yalnızca unchecked istisnalarla kullanılır
🤖 İkisi de her iki tür istisnayla kullanılabilir; böyle bir kısıt yok.
## Doğru
B
## Açıklama
`throw` bir **deyimdir** (statement): çalışma anında bir istisna nesnesini fiilen fırlatır — `throw new IllegalArgumentException("...")`. `throws` ise metot **imzasının** parçasıdır: metodun hangi (özellikle checked) istisnaları çağırana yayabileceğini **bildirir** — `void oku() throws IOException`. Yani `throw` eylemi yapar, `throws` sözleşmeyi ilan eder. Bir metot bir checked istisnayı `throw` ediyor ve içeride yakalamıyorsa, bunu imzasında `throws` ile bildirmek zorundadır.

# exc-4 | Orta
## Soru
Aşağıdaki kod ne döndürür?

```java
static int f() {
    try {
        return 1;
    } finally {
        return 2;
    }
}
```
## A
`1`
🤖 `finally` içindeki `return`, try'daki `return`'ü EZER; sonuç 1 olamaz.
## B
Derleme hatası; iki `return` çelişir
🤖 Kod geçerli derlenir; `finally`'de `return` yasal (ama tehlikeli) bir kalıptır.
## C
`2`
🤖 Doğru: `finally` içindeki `return` try'daki değeri geçersiz kılar ve 2 döner.
## D
Önce `1` döner sonra `2` çalışır ama görünmez
🤖 Metot tek değer döndürür; `finally`'deki `return` nihai değeri belirler.
## Doğru
C
## Açıklama
`finally` bloğu, `try` içindeki `return 1` değerlendirildikten SONRA ama metot geri dönmeden ÖNCE çalışır. `finally` içinde de bir `return 2` varsa, bu **try'ın döndüreceği değeri tamamen ezer** ve metot `2` döndürür. Daha da kötüsü: `try` bir istisna fırlatıyor olsaydı bile, `finally`'deki `return` o istisnayı **yutar** ve normal dönüş yapar — bu, hataların sessizce kaybolmasına yol açan tehlikeli bir tuzaktır. Kural: `finally` içinde ASLA `return` (veya `throw`) yazma; `finally` yalnız temizlik içindir.

# exc-5 | Orta
## Soru
Aşağıdaki `catch` sıralaması neden derlenmez?

```java
try {
    riskli();
} catch (Exception e) {
    // ...
} catch (IOException e) {
    // ...
}
```
## A
`IOException` bir `catch` bloğuyla yakalanamaz; yalnızca `throws` ile bildirilebilir
🤖 `IOException` elbette yakalanabilir; sorun blok sıralamasıdır.
## B
Daha genel tip (`Exception`) daha özel tipten (`IOException`) önce geldiği için ikinci `catch` erişilemez (unreachable) olur
🤖 Doğru: üst tip önce yakalanınca alt tipe hiç sıra gelmez; derleyici reddeder.
## C
Bir `try` bloğunda birden fazla `catch` yazmak yasaktır; en fazla bir tane olabilir
🤖 Çoklu `catch` yasaldır; sorun sayıda değil, sıralamadadır.
## D
`Exception` soyut (abstract) bir sınıf olduğu için `catch` içinde tip olarak kullanılamaz
🤖 `Exception` soyut değildir ve yakalanabilir; hata sıralamadan gelir.
## Doğru
B
## Açıklama
`catch` blokları **yukarıdan aşağıya** denenir ve ilk eşleşen çalışır. `IOException` zaten `Exception`'ın alt türü olduğundan, `catch (Exception e)` her `IOException`'ı da yakalar. Bu yüzden ikinci `catch (IOException e)` bloğuna **hiçbir zaman sıra gelmez** ve derleme *"exception IOException has already been caught"* hatasıyla başarısız olur. Kural: `catch` bloklarını **en özelden en genele** doğru sırala (`IOException` önce, `Exception` en sonda). Bu, hem derlenir hem de her istisna türüne uygun işlem yapmanı sağlar.

# exc-6 | İleri
## Soru
Aşağıdaki `Error` yakalama denemesi için en doğru değerlendirme hangisidir?

```java
try {
    // ... derin özyineleme
} catch (Throwable t) {
    // devam et
}
```
## A
İyi bir pratiktir; `Throwable` yakalamak uygulamanın her hatayı zarifçe ele almasını sağlar
🤖 `Throwable` yakalamak `Error`'ları da yutar; bu genelde tehlikelidir.
## B
`Throwable` yalnızca checked istisnaları yakalar; `Error` ve `RuntimeException` bu bloğa uğramaz
🤖 `Throwable` her şeyi (`Error` + `Exception`) yakalar; hiçbirini atlamaz.
## C
Derleme hatası verir; `Error` ve alt türleri `catch` bloğunda yakalanamaz
🤖 `Error` teknik olarak yakalanabilir (`Throwable` altı); sorun teknik değil, tasarımsaldır.
## D
Kurtarılamaz `Error`'ları (`OutOfMemoryError`, `StackOverflowError`) da yutar; kaçınılması gereken bir anti-patterndir
🤖 Doğru: `Error`'lar JVM'in ciddi durumlarıdır; yutup "devam etmek" sistemi tutarsız bırakır.
## Doğru
D
## Açıklama
`Throwable`, hiyerarşinin en tepesidir; onu yakalamak hem `Exception`'ları hem de `Error`'ları (`OutOfMemoryError`, `StackOverflowError` gibi) yakalar. `Error`'lar JVM'in **kurtarılamaz** kabul edilen ciddi durumlarıdır; onları yakalayıp "devam etmek" uygulamayı tutarsız/bozuk bir durumda çalışmaya zorlar ve gerçek sorunu maskeler. Bu yüzden `catch (Throwable)` (veya `catch (Error)`) neredeyse her zaman bir **anti-patterndir**. Genel kural: yalnızca **anlamlı şekilde ele alabileceğin** en özel istisna tipini yakala; `Error`'ları yayılmaya bırak.
