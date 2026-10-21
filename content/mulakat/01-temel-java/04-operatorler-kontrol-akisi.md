---
slug: operatorler-kontrol-akisi
baslik: "Operatörler ve Kontrol Akışı: Kısa Devre, Ternary, switch ve Etiketli break"
kademe: Temel
sira: 4
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Şöyle düşün: kapıdaki görevli önce kimlik ister. Kimlik yoksa listeye bakmasına hiç gerek kalmaz — ikinci kontrolü **yapmaz bile**. Programlar da böyle karar verir: bazı kontroller gereksizse hiç çalıştırılmaz, bazı yollardan biri seçilir, bazı döngüler ortasında kesilir. Operatörler ve kontrol akışı (control flow), "hangi kod ne zaman çalışır" sorusunun cevabıdır. Buradaki ince kurallar bilinmezse kod hata vermeden **sessizce yanlış** çalışır — mülakatın en sevdiği alan tam da bu.

### Temel kavramlar

- **İşlenen (operand)**: operatörün üzerinde çalıştığı değer. `a && b` içinde `a` sol, `b` sağ işlenendir.
- **Yan etki (side effect)**: bir metodun döndürdüğü değerin dışında yaptığı iş — log yazmak, sayaç artırmak, veritabanı okumak. Bir metot çağrılmazsa yan etkisi de gerçekleşmez; aşağıdaki farkın önemi buradan gelir.
- **Kısa devre (short-circuit)**: sol taraf sonucu tek başına belirliyorsa **sağ tarafın hiç değerlendirilmemesi**. `&&`: sol `false` ise sağa bakılmaz. `||`: sol `true` ise sağa bakılmaz.
- **`&` ve `|`**: boolean işlenenlerle de **geçerlidir** (derleme hatası değildir) ama **kısa devre yapmazlar** — her iki tarafı da her zaman değerlendirirler. Tam sayı işlenenlerle ise mantıksal değil **bit düzeyinde** çalışırlar.
- **Ternary (koşullu operatör)** `koşul ? a : b`: değer üreten, tek ifadelik dallanma.
- **Düşme (fall-through)**: klasik `switch`'te bir `case` bloğu `break` ile bitmezse çalışmanın bir sonraki `case`'e akması.
- **Etiket (label)**: bir döngünün başına konan isim; `break`/`continue`'ye hangi döngüyü hedefleyeceğini söyler.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kısa devre değerlendirme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kısa devre: sağ taraf her zaman çalışmaz</text>
  <rect x="50" y="46" width="250" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">&amp;&amp;  (VE)</text>
  <text x="175" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">sol false ise</text>
  <text x="175" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">sağ DEĞERLENDİRİLMEZ</text>
  <text x="175" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">x != null &amp;&amp; x.foo()</text>
  <rect x="340" y="46" width="250" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="465" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">||  (VEYA)</text>
  <text x="465" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">sol true ise</text>
  <text x="465" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">sağ DEĞERLENDİRİLMEZ</text>
  <text x="465" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">x == null || x.bos()</text>
</svg>
```

### Nasıl çalışır?

**1) Kısa devre.**

```java
String s = null;
if (s != null && s.length() > 0) {
    System.out.println("dolu");
} else {
    System.out.println("boş");
}
// çıktı: boş
```

Satır satır: `s != null` **false** döner; `&&` sonucun artık kesin `false` olduğunu bildiği için sağdaki `s.length() > 0` **hiç değerlendirilmez**. Bu yüzden `null` üzerinde `.length()` çağrılmaz, NPE oluşmaz ve `else` dalı çalışır. "Önce null kontrol et, sonra kullan" desenini tek satıra sığdıran en yaygın idiom budur; `||` tarafındaki karşılığı `x == null || x.bos()`'tur.

Aynı satırı `&` ile yazsaydık sağ taraf zorla değerlendirilir ve NPE fırlardı. Fark her zaman **sonuçta** görünmez — nihai `boolean` aynı çıkabilir; asıl fark sağdaki metodun **yan etkisinin** gerçekleşip gerçekleşmemesidir:

```java
boolean r1 = kontrol() && digerKontrol();  // kontrol() false ise digerKontrol() HİÇ çağrılmaz
boolean r2 = kontrol() &  digerKontrol();  // digerKontrol() HER ZAMAN çağrılır
```

Bu yüzden null koruması ya da "pahalı/yan-etkili ikinci kontrol" durumlarında daima `&&`/`||` kullanılır.

**2) Bit düzeyinde `&`, `|`, `^`.** Tam sayı işlenenlerle kullanıldığında sayıların **ikili gösterimindeki** her bit konumuna sırasıyla VE / VEYA / ÖZEL-VEYA uygulanır. Kural basit: `&` için ikisi de 1 ise 1, `|` için en az biri 1 ise 1.

- `6 & 3` → `110 & 011` = `010` = **2**
- `6 | 3` → `110 | 011` = `111` = **7**
- `5 & 3` → `101 & 011` = `001` = **1**
- `5 | 3` → `101 | 011` = `111` = **7**

Bu operatörler bayrak (flag) maskeleme, izin bitleri ve düşük seviyeli optimizasyonlarda kullanılır. Metin birleştirmezler, rastgele değer de üretmezler.

**3) Ternary.**

```java
int x = 5;
String s = (x > 3) ? "büyük" : "küçük";
System.out.println(s);   // büyük
```

Ternary koşulun kendisini (`true`) değil, **seçilen dalın değerini** döndürür. Dallar `String` dahil her tipte olabilir; tek kural iki dalın uyumlu bir ortak tipe sahip olmasıdır. Karışık ilkel/sarmalayıcı dallarda ortak tip ilkele sabitlenir ve unboxing tuzağı doğar (ayrı ders). Kısa `if-else` atamalarını tek satıra indirir, ama karmaşık mantıkta okunabilirliği düşürür.

**4) `switch`: iki biçim.**

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="switch klasik iki nokta fall-through vs ok biçimi">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">switch: klasik ':' düşme (fall-through) riski · ok '->' güvenli</text>
  <rect x="30" y="44" width="275" height="150" rx="12" fill="#FBE9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="167" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">klasik: case ... :</text>
  <text x="167" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case 1: islem(); // break YOK</text>
  <text x="167" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case 2: digerIslem();</text>
  <text x="167" y="146" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">1 gelince 2'ye DE düşer</text>
  <text x="167" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(fall-through — sık hata)</text>
  <rect x="335" y="44" width="275" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">ok: case ... -> (Java 14+)</text>
  <text x="472" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case 1 -> islem();</text>
  <text x="472" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case 2 -> digerIslem();</text>
  <text x="472" y="146" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">sadece eşleşen dal çalışır</text>
  <text x="472" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düşme yok · değer döndürebilir</text>
</svg>
```

```java
int gun = 3;
switch (gun) {
    case 1: System.out.print("A");
    case 2: System.out.print("B");
    case 3: System.out.print("C");
    case 4: System.out.print("D");
    default: System.out.print("E");
}
// çıktı: CDE
```

Adım adım: `gun = 3` olduğu için giriş noktası `case 3`'tür — öncesindeki 1 ve 2 **atlanır**. Hiçbir dalda `break` olmadığından **düşme** (fall-through) gerçekleşir: `C` → `D` → `E` sırayla çalışır ve switch sonuna gelince durur (switch döngüsel değildir, başa sarmaz). Yalnız `C` yazdırmak isteseydin `case 3`'ten sonra `break` gerekirdi.

Java 14+ ile gelen **ok biçimi** her dalı **yalıtır**: düşme yoktur, `break` gerekmez.

```java
int r = switch (x) {
    case 1 -> 10;
    default -> 0;
};
```

Bu aynı zamanda **değer döndüren** bir switch **ifadesidir** (switch expression). İki biçim de aynı etiket tiplerini (`int`, `String`, `enum` vb.) ve `default` dalını destekler; aralarında performans farkı yoktur. Tek gerçek fark **fall-through** davranışıdır. Java 21 ile birlikte switch, desen eşleme (pattern matching) de yapabilir.

**5) Etiketli `break` / `continue`.** Sade `break` yalnız **kendi bulunduğu (en içteki)** döngüyü kırar; dıştaki döngü çalışmaya devam eder.

```java
dis: for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
        if (kosul) break dis;   // DIŞ döngüden tek adımda çıkar
    }
}
```

`continue dis;` ise dış döngünün bir sonraki turuna atlar.

### Mülakatta ne sorulur?

- **Kısa devre = NPE koruması.** `s != null && s.length() > 0` kalıbının neden güvenli olduğunu açıklayabilmek.
- **`&&` ile `&` farkı yan etkilerdedir**, sonuçta değil. "İkisi tamamen aynıdır" ya da "`&` boolean'da kullanılamaz, derleme hatası verir" cevapları yanlıştır. Kısa devre yapan `&&`'dir, `&` değil. `&` boolean bağlamda "mantıksal ama kısa-devresiz"dir ve genelde bit işlemleri için ayrılır.
- **`5 & 3 = 1`, `5 | 3 = 7`.** Bit hesabını tahtada yapabilmek beklenir.
- **`CDE` sorusu.** `break` unutmanın tehlikesini gösteren kanonik örnek; ok biçimli switch bu tuzağı tamamen ortadan kaldırır.
- **Ok biçimi hakkında yanlış iddialar:** "daha yavaştır" (hayır), "yalnız `String` etiketi destekler" (hayır), "`default` desteklemez" (hayır, `default ->` vardır).
- **Dıştaki döngüyü kırmanın en temiz yolu.** `return` tüm metottan çıkar (döngü sonrası kodu da atlar), `System.exit(0)` programı tümden sonlandırır — ikisi de "yalnız dış döngüyü kır" niyetini aşan yıkıcı yan etkilerdir. Doğru araç **etiketli `break`**tir.

### Tek cümlede

`&&`/`||` gerekmedikçe sağ tarafı hiç çalıştırmaz, klasik `switch` `break` yoksa aşağıya düşer, ok biçimi düşmeyi ortadan kaldırıp değer döndürebilir ve etiketli `break` iç içe döngüde dıştakini hedefler.

# op-1 | Temel
## Soru
Aşağıdaki kod çalışınca ne olur?

```java
String s = null;
if (s != null && s.length() > 0) {
    System.out.println("dolu");
} else {
    System.out.println("boş");
}
```
## A
`NullPointerException` fırlatır
🤖 `&&` kısa devredir; sol `false` olunca `s.length()` hiç çağrılmaz, NPE olmaz.
## B
Derleme hatası verir
🤖 Kod geçerlidir; kısa devre tam da bu kalıbı güvenli kılmak içindir.
## C
`dolu` yazar
🤖 `s` null; ilk koşul false olduğundan then dalına girilmez.
## D
`boş` yazar
🤖 Doğru: `s != null` false, kısa devre sağı atlar, else dalı çalışır.
## Doğru
D
## Açıklama
`&&` operatörü **kısa devre** yapar: sol işlenen `s != null` **false** olduğu an, sağdaki `s.length() > 0` **hiç değerlendirilmez**. Bu yüzden `null` üzerinde `.length()` çağrılmaz ve NPE oluşmaz (A yanlış). Koşulun tamamı false olur, `else` dalı çalışır ve **"boş"** yazılır. Bu, "önce null kontrol et, sonra kullan" desenini tek satıra sığdıran en yaygın idiomdur. Eğer `&` (bitwise) kullansaydık sağ taraf da zorla değerlendirilir ve NPE fırlardı — işte bu yüzden mantıksal koşullarda `&&`/`||` tercih edilir.

# op-2 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
int x = 5;
String s = (x > 3) ? "büyük" : "küçük";
System.out.println(s);
```
## A
`büyük`
🤖 Doğru: koşul `x > 3` true olduğundan ternary ilk dalı (`"büyük"`) döndürür.
## B
`küçük`
🤖 Koşul true; ikinci dal yalnız koşul false iken seçilir.
## C
`true`
🤖 Ternary koşulu değil, seçilen dalın değerini döndürür; sonuç `"büyük"`.
## D
Derleme hatası; ternary `String` döndüremez
🤖 Ternary her tipte değer döndürebilir; `String` dalları geçerlidir.
## Doğru
A
## Açıklama
Ternary (koşullu) operatör `koşul ? a : b` bir **değer üretir**: koşul `true` ise `a`, `false` ise `b`. Burada `x = 5`, `x > 3` **true**, bu yüzden ilk dal `"büyük"` seçilir ve `s`'e atanır. Ternary koşulun kendisini (`true`, C) değil, **seçilen dalın değerini** döndürür. Dallar `String` de dahil her tipte olabilir (D yanlış); tek kural iki dalın uyumlu bir ortak tipe sahip olmasıdır. Ternary, kısa `if-else` atamalarını tek satıra indirir ama karmaşık mantıkta okunabilirliği düşürebilir.

# op-3 | Temel
## Soru
Klasik `switch` (`case ... :`) ile ok biçimli `switch` (`case ... ->`) arasındaki en önemli davranış farkı nedir?
## A
Ok biçimi klasik biçimden daha yavaş çalışır ama okunabilirliği daha yüksektir
🤖 Performans farkı yok; asıl fark otomatik düşmenin (fall-through) olmamasıdır.
## B
Klasik biçim yalnız `int`, ok biçimi yalnız `String` etiketlerini destekler
🤖 İkisi de aynı etiket tiplerini destekler; fark düşme davranışıdır, tip değil.
## C
Ok biçiminde otomatik düşme (fall-through) yoktur; klasik biçimde `break` unutulursa sonraki case'lere düşülür
🤖 Doğru: `->` her dalı yalıtır; klasik `:` `break` yoksa aşağı akar.
## D
Ok biçimi `default` dalını desteklemez; varsayılan durum yalnız klasik biçimde yazılabilir
🤖 Ok biçimi de `default ->` destekler; fark düşme davranışındadır.
## Doğru
C
## Açıklama
Klasik `switch`'te her `case` bloğu bir `break` ile bitmezse, çalışma **bir sonraki case'e düşer** (fall-through) — `break` unutmak klasik ve sinsi bir hatadır. Java 14+ ile gelen **ok biçimi** (`case X -> ...;`) her dalı **yalıtır**: düşme yoktur, `break` gerekmez, kod daha güvenlidir. Ayrıca ok biçimi bir **değer döndürebilen** switch ifadesi (switch expression) olarak da kullanılabilir (`int r = switch(x){ case 1 -> 10; default -> 0; };`). Performans farkı yoktur (A); ikisi de aynı tipleri (`int`, `String`, `enum` vb.) ve `default`'u destekler (B, D yanlış). Tek gerçek fark **fall-through** davranışıdır.

# op-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
int gun = 3;
switch (gun) {
    case 1: System.out.print("A");
    case 2: System.out.print("B");
    case 3: System.out.print("C");
    case 4: System.out.print("D");
    default: System.out.print("E");
}
```
## A
`C`
🤖 Yalnız `C` için `case 3`'ten sonra `break` gerekirdi; yok, o yüzden aşağı düşülür.
## B
`CDE`
🤖 Doğru: `case 3`'ten girilir, `break` olmadığından 4 ve default'a düşülür.
## C
`ABCDE`
🤖 `gun=3`; başlangıç `case 3`'tür, 1 ve 2 atlanır; ABC değil CDE yazılır.
## D
`CDEABC` (döngüsel)
🤖 switch döngüsel değildir; sonuna varınca durur, başa sarmaz.
## Doğru
B
## Açıklama
`gun = 3` olduğu için giriş noktası `case 3`'tür. Hiçbir `case`'de **`break` olmadığından** klasik switch **düşme** (fall-through) yapar: `case 3` (`C`) → `case 4` (`D`) → `default` (`E`) sırayla çalışır ve switch sonuna gelince durur. Sonuç **`CDE`**. Sadece `C` yazılması (A) için `case 3`'ten sonra `break` gerekirdi. `ABCDE` (C) yanlış çünkü giriş `case 3`'tür, öncesindeki 1 ve 2 atlanır. switch döngüsel değildir, başa sarmaz (D). Bu, `break` unutmanın neden tehlikeli olduğunu gösteren kanonik örnektir — ok biçimli switch bu tuzağı tamamen ortadan kaldırır.

# op-5 | Orta
## Soru
İç içe iki döngüde, belirli bir koşulda **dıştaki** döngüyü tek adımda kırmak için en temiz yol nedir?
## A
İçteki döngüde `break` kullanmak
🤖 Sade `break` yalnız içteki döngüyü kırar; dıştaki döngü devam eder.
## B
`System.exit(0)` çağırmak
🤖 Program tümden sonlanır; döngü kırmak için aşırı ve yıkıcı bir yol.
## C
`return` kullanmak
🤖 `return` tüm metottan çıkar; sadece dış döngüyü kırmak isteyeni aşar, yan etki fazla.
## D
Dıştaki döngüye bir etiket (label) koyup içeriden `break etiket;` demek
🤖 Doğru: etiketli break dıştaki döngüyü hedefleyip tek adımda kırar.
## Doğru
D
## Açıklama
Sade `break` yalnızca **kendi bulunduğu (en içteki) döngüyü** kırar; dıştaki döngü çalışmaya devam eder (A yetersiz). Dıştaki döngüyü tek adımda kırmak için **etiketli break** (labeled break) kullanılır: dış döngünün önüne `dis:` gibi bir etiket konur ve içeriden `break dis;` çağrılır — kontrol doğrudan etiketli döngünün dışına çıkar. `return` (C) tüm metottan çıkar (döngü sonrası kodu da atlar), `System.exit` (B) programı bitirir — ikisi de "sadece dış döngüyü kır" niyetini aşan yan etkilerdir. Benzer şekilde `continue etiket;` dış döngünün bir sonraki iterasyonuna atlar.

# op-6 | İleri
## Soru
`kontrol()` ve `digerKontrol()`, `boolean` döndüren ve yan etkisi (log yazma, sayaç artırma) olan metotlar olsun. Aşağıdaki iki satır birbirinden nasıl farklı davranır?

```java
boolean r1 = kontrol() && digerKontrol();
boolean r2 = kontrol() &  digerKontrol();
```
## A
İkisi de tamamen aynıdır; `&&` ve `&` boolean bağlamında birebir özdeş davranır
🤖 Sonuç aynı olsa da yan etki farkı var; `&` sağı hep çalıştırır, `&&` koşullu.
## B
`&` boolean işlenenlerle kullanılamaz; bu yüzden ikinci satır derleme hatası verir
🤖 `&` boolean'larda geçerli (mantıksal-ama-kısa-devresiz); hata değildir.
## C
`&&` kısa devredir (sol `false` ise `digerKontrol()` çağrılmaz); `&` her iki metodu da her zaman çağırır
🤖 Doğru: fark yan etkilerde; `&` sağ metodu koşulsuz çalıştırır, `&&` gerekmezse atlar.
## D
`&&` her iki metodu da çağırır; `&` ise yalnızca soldaki metodu çağırıp sağdakini atlar
🤖 Ters: kısa devre yapan (bazen sağı atlayan) `&&`'dir, `&` değil.
## Doğru
C
## Açıklama
`&&` **kısa devre** (short-circuit) yapar: `kontrol()` `false` dönerse `digerKontrol()` **hiç çağrılmaz**. `&` (boolean bağlamında geçerli bir operatördür, B yanlış) ise **kısa devre yapmaz**: sonuç ne olursa olsun **her iki metodu da** her zaman çağırır. Nihai boolean sonuç aynı çıkabilir, ama metotların **yan etkileri** (side effects: log yazma, sayaç artırma, veritabanı okuma) farklıdır — `&` ile `digerKontrol()`'ün yan etkisi hep gerçekleşir, `&&` ile gerçekleşmeyebilir. Bu yüzden null koruması veya "pahalı/yan-etkili ikinci kontrol" durumlarında daima `&&`/`||` kullanılır. `&` genelde bit işlemleri için ayrılır; boolean'da bilerek "her iki tarafı da değerlendir" istendiğinde nadiren kullanılır.

# op-7 | İleri
## Soru
`5 & 3` ve `5 | 3` bitwise işlemlerinin sonuçları nedir?
## A
`5 & 3 = 8`, `5 | 3 = 0`
🤖 Değerler ters/yanlış; & bit-VE küçültür (1), | bit-VEYA (7) verir.
## B
`5 & 3 = 1`, `5 | 3 = 7`
🤖 Doğru: 101 & 011 = 001 (1); 101 | 011 = 111 (7).
## C
`5 & 3 = 15`, `5 | 3 = 2`
🤖 Bit işlemleri böyle çalışmaz; & = 1, | = 7 olmalı.
## D
`5 & 3 = 53`, `5 | 3 = 53` (birleştirme)
🤖 `&`/`|` string birleştirmez; bit düzeyinde çalışır, sonuç 1 ve 7'dir.
## Doğru
B
## Açıklama
Bitwise operatörler sayıların **ikili gösterimi** üzerinde bit bit çalışır. `5` = `101`, `3` = `011`. **Bit-VE (`&`)**: her konumda ikisi de 1 ise 1 → `101 & 011 = 001` = **1**. **Bit-VEYA (`|`)**: en az biri 1 ise 1 → `101 | 011 = 111` = **7**. Bu yüzden `5 & 3 = 1` ve `5 | 3 = 7`. Bit operatörleri bayrak (flag) maskeleme, izin bitleri, düşük seviyeli optimizasyonlarda kullanılır. Onları boolean `&&`/`||` ile karıştırmamak gerekir: boolean bağlamda `&`/`|` mantıksal çalışır ama kısa devre yapmaz; tam sayı bağlamında ise bit düzeyinde işlem yaparlar. `&` string birleştirmez (D) veya rastgele değer üretmez (C).
