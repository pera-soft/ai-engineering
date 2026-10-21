---
slug: sinif-nesne-constructor
baslik: "Sınıf, Nesne ve Constructor: Aşırı Yükleme, this() Zinciri, Init Sırası"
kademe: Temel
sira: 9
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Kurabiye kalıbını ve ondan çıkan kurabiyeleri düşün. Kalıp tektir; ondan istediğin kadar kurabiye üretirsin ve her kurabiye ayrı bir şeydir — biri ısırılınca diğeri eksilmez. Sınıf kalıptır, nesne kurabiyedir. Peki her kurabiyenin fırından çıkarken doğru biçimde hazırlanmasını kim garanti eder? İşte **constructor** budur: nesne doğarken bir kez çalışan ve onu kullanılabilir bir başlangıç durumuna getiren özel blok. Çözdüğü problem, yarı kurulmuş ve alanları boş nesnelerin ortalıkta dolaşmasıdır.

### Temel kavramlar

- **Sınıf (class)**: şablon/kalıp. Tek bir tanımdır; kendisi bellekte bir "şey" değildir.
- **Nesne (object) / örnek (instance)**: `new` ile üretilen, bellekte yeri olan somut örnek. Her nesnenin kendi alan değerleri vardır, birbirinden bağımsızdır.
- **Alan (field)**: nesnenin verisi.
- **Constructor (kurucu)**: adı **sınıf adıyla aynı** olan, **dönüş tipi olmayan** özel blok; `new` çağrıldığında çalışır ve alanları ilk değerleriyle donatır.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="this constructor zinciri">
  <defs>
    <marker id="sinif-ok2" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">this(...) zinciri: atama tek yerde yaşar</text>
  <rect x="24" y="46" width="210" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="129" y="66" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Dikdortgen()</text>
  <text x="129" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">this(1, 1)</text>
  <rect x="24" y="110" width="210" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="129" y="130" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Dikdortgen(int kare)</text>
  <text x="129" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">this(kare, kare)</text>
  <rect x="380" y="78" width="236" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="498" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Dikdortgen(int en, int boy)</text>
  <text x="498" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">asıl atama: this.en / this.boy</text>
  <path d="M234,68 L376,92" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#sinif-ok2)"/>
  <path d="M234,132 L376,108" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#sinif-ok2)"/>
</svg>
```

- **Varsayılan constructor (default constructor)**: Sınıfta **hiç** constructor yazmazsan derleyici parametresiz boş bir tane ekler. Ama **bir tane bile** constructor yazarsan varsayılan artık **otomatik eklenmez**.

```java
class Urun { public Urun(String ad) { /* ... */ } }
Urun u = new Urun();   // DERLEME HATASI: Urun() diye bir constructor yok
```

Bu bir çalışma-zamanı sorunu değildir (`NullPointerException` değildir); kod hiç derlenmez ve `ad` alanı `null` da olmaz. Parametresiz kullanımı da istiyorsan parametresiz constructor'ı **açıkça** yazmalısın. "Default constructor her zaman vardır" yaygın bir yanılgıdır.

- **Aşırı yükleme (overloading)**: Aynı sınıfta **parametre listesi** (sayı, tip veya sıra) farklı olan birden çok constructor tanımlanabilir; örn. `Nokta()`, `Nokta(int x, int y)`, `Nokta(Nokta baska)`. Ayrım **yalnız parametre imzasından** gelir: constructor'ların **dönüş tipi yoktur** (dönüş tipiyle ayrım yapılamaz) ve **hepsinin adı sınıf adıyla aynıdır** (farklı ad verilemez).
- **`this(...)` zinciri**: Bir constructor, aynı sınıfın başka bir constructor'ını `this(...)` ile çağırıp tekrarı önleyebilir. `this(...)` constructor gövdesinin **ilk ifadesi** olmak zorundadır.
- **`super(...)`**: Üst sınıfın constructor'ını çağırır ve o da **ilk ifade** olmalıdır. Yazmazsan derleyici parametresiz `super()`'i **örtük olarak** ekler; bu yüzden üst sınıf constructor'ı **her zaman** çalışır. İkisi de "ilk ifade" olmak zorunda olduğundan bir constructor `this(...)` ve `super(...)` çağrılarından yalnız **birini** içerebilir. Üst sınıfın parametresiz constructor'ı yoksa açık `super(...)` yazmak zorunludur; yazmazsan derleme hatası olur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Constructor init sirasi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">new çağrısında başlatma sırası</text>
  <rect x="30" y="44" width="580" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="69" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">1) Alanlar varsayılan değere (0 / null / false)</text>
  <rect x="30" y="90" width="580" height="40" rx="8" fill="#FBF0E6" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="115" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">2) super(...) — üst sınıf constructor'ı çalışır</text>
  <rect x="30" y="136" width="580" height="40" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="161" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">3) Alan başlatıcıları + { } blokları (yazım sırasıyla)  →  4) constructor gövdesi</text>
  <text x="320" y="196" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">this(...) ve super(...) ancak constructor'ın İLK ifadesi olabilir</text>
</svg>
```

```java
class Dikdortgen {
    int en, boy;
    Dikdortgen() { this(1, 1); }
    Dikdortgen(int kare) { this(kare, kare); }
    Dikdortgen(int en, int boy) { this.en = en; this.boy = boy; }
    int alan() { return en * boy; }
}
System.out.println(new Dikdortgen().alan() + " " + new Dikdortgen(5).alan());   // 1 25
```

`new Dikdortgen()` → `this(1, 1)` → `en=1, boy=1` → alan **1**. `new Dikdortgen(5)` → `this(5, 5)` → alan **25** (5 değil). Alanlar gerçekten atandığı için varsayılan 0'da kalmazlar. Böylece asıl atama mantığı tek yerde, `Dikdortgen(int, int)` içinde yaşar.

### Nasıl çalışır?

`new` yazdığında JVM şu sırayı izler:

1. Alanlar **varsayılan** değerlerine ayarlanır: sayılar `0`, `boolean` `false`, referanslar `null`.
2. Üst sınıf constructor'ı çalışır (`super(...)` — açık ya da örtük). Zincir `Object`'e kadar gider.
3. **Örnek alan başlatıcıları** (`int x = 10;`) ve **örnek `{}` blokları**, **yazım sırasıyla** çalışır.
4. Constructor gövdesinin kalanı çalışır.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sinif sablon nesne ornek">
  <defs>
    <marker id="sinif-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sınıf = şablon (kalıp); nesne = new ile üretilen örnek</text>
  <rect x="24" y="60" width="200" height="90" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="124" y="86" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Urun (class)</text>
  <text x="124" y="110" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">şablon: ad, fiyat</text>
  <text x="124" y="130" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tek tanım, bellekte yok</text>
  <path d="M224,105 L404,76" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#sinif-ok)"/>
  <path d="M224,105 L404,144" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#sinif-ok)"/>
  <text x="300" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">new</text>
  <rect x="410" y="48" width="206" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="513" y="72" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Nesne #1: ad="Kalem"</text>
  <text x="513" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bellekte somut örnek</text>
  <rect x="410" y="116" width="206" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="513" y="140" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Nesne #2: ad="Defter"</text>
  <text x="513" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bağımsız kendi durumu</text>
</svg>
```

```java
class A {
    int x = 10;
    { System.out.print("blok "); }
    A() { System.out.print("ctor(x=" + x + ") "); }
}
new A();      // blok ctor(x=10)
```

Adım adım gidelim. Önce `x` 0 olur (1). Sonra `Object()` çalışır (2). Ardından `int x = 10;` ve `{ }` bloğu yazım sırasıyla çalışır; ekrana "blok " yazılır ve `x` artık 10'dur (3). En son constructor gövdesi "ctor(x=10) " yazar (4). Yani örnek blok constructor gövdesinden **önce** çalışır ve o noktada `x` çoktan 10'dur. Bu sıra, constructor gövdesine girildiğinde alan başlatıcılarının uygulanmış olduğunu garanti eder.

### Mülakatta ne sorulur?

- **Constructor'dan ezilebilir metot çağırma tuzağı** — klasik ileri soru:

```java
class Ust {
    Ust() { yazdir(); }                    // constructor, override edilen metodu çağırıyor
    void yazdir() { System.out.println("Ust"); }
}
class Alt extends Ust {
    int deger = 42;
    void yazdir() { System.out.println("Alt deger=" + deger); }
}
new Alt();     // Alt deger=0
```

`new Alt()` önce `Ust()`'u çalıştırır; o da `yazdir()`'i çağırır. Metot çağrısı **çalışma-zamanı tipine (`Alt`)** göre çözüldüğü için `Ust.yazdir` değil `Alt.yazdir` devreye girer — ama bu noktada `Alt`'ın alan başlatıcıları (`deger = 42`) **henüz** çalışmamıştır (alan başlatma, üst constructor bittikten sonradır). Bu yüzden `deger` varsayılan değeri **0**'dır. Java bunu yasaklamaz; kod derlenir (en fazla uyarı alırsın) ve tehlikeli sonucu sessizce üretir. Kural: constructor yalnız `private`, `static` veya `final` — yani **ezilemeyen** — metotları güvenle çağırabilir; aksi halde alt sınıf **yarı kurulmuş (partially constructed)** durumu görür.
- **Varsayılan constructor kuralı**: bir constructor yazıldığı an parametresiz olan kaybolur.
- **Constructor aşırı yükleme**: ayrım imzadadır; dönüş tipi yoktur, ad değiştirilemez, "sınıfta tek constructor olur" yanlıştır.
- **`this(...)` / `super(...)` ilk ifade zorunluluğu** ve ikisinin aynı constructor'da bulunamayacağı. `this(...)` kullanan bir constructor'da `super` çağrısı zincirin ucundaki constructor'a bırakılır — ama üst sınıf yine de mutlaka kurulur.
- **Başlatma sırası** sorularında cevabın anahtarı hep aynıdır: alan başlatıcıları ve `{}` blokları constructor gövdesinden **önce**, üst sınıf constructor'ından **sonra** çalışır.

### Tek cümlede

Sınıf kalıptır, nesne `new` ile üretilen örnektir, constructor da nesneyi tek seferde geçerli bir başlangıç durumuna getiren bloktur; sırası (varsayılan değerler → `super` → alan başlatıcıları/bloklar → gövde) bilinmezse `0`/`null` gören sinsi hatalar çıkar.

# ctor-1 | Temel
## Soru
Bir sınıfta yalnızca `public Urun(String ad)` constructor'ı tanımlanmış, parametresiz constructor yok. Aşağıdaki hangisi olur?

```java
class Urun { public Urun(String ad) { /* ... */ } }
Urun u = new Urun();
```
## A
Derleme hatası verir; parametresiz constructor mevcut değildir
🤖 Doğru: `Urun(String)` yazıldığı için `Urun()` yoktur ve `new Urun()` derlenmez.
## B
Kod derlenir; derleyici parametresiz varsayılan constructor'ı otomatik ekler
🤖 Bir constructor yazıldığı an, varsayılan (default) constructor ARTIK otomatik eklenmez.
## C
Kod derlenir ve `ad` alanı `null` olur
🤖 Çağrılabilecek parametresiz constructor olmadığından kod hiç derlenmez.
## D
Çalışma-zamanında `NullPointerException` fırlatır
🤖 Sorun çalışma zamanında değil derleme zamanındadır; kod derlenmez bile.
## Doğru
A
## Açıklama
Derleyici parametresiz varsayılan constructor'ı **yalnız sınıfta hiç constructor yoksa** ekler. `Urun` sınıfı `Urun(String ad)` tanımladığı andan itibaren derleyici artık otomatik `Urun()` üretmez. Dolayısıyla `new Urun()` çağrısı çağrılacak bir constructor bulamaz ve **derleme hatası** verir. Bu, mülakatta sık test edilen bir inceliktir: parametresiz kullanımı da istiyorsan, parametresiz constructor'ı **açıkça** yazman gerekir. Çeldirici B, "default constructor her zaman vardır" yanılgısıdır.

# ctor-2 | Temel
## Soru
Constructor aşırı yükleme (overloading) için aşağıdakilerden hangisi doğrudur?
## A
Aynı sınıfta yalnızca tek bir constructor tanımlanabilir; ikincisi derleme hatası verir
🤖 Aksine; parametre imzaları farklı olduğu sürece birden çok constructor tanımlanabilir.
## B
Constructor'lar yalnız dönüş tipleri farklıysa aşırı yüklenebilir (örn. biri `void`, diğeri sınıf tipi)
🤖 Constructor'ların dönüş tipi YOKTUR; ayrım yalnız parametre listesiyle yapılır.
## C
Aynı sınıfta, parametre listeleri (sayı/tip/sıra) farklı olan birden çok constructor tanımlanabilir
🤖 Doğru: aşırı yükleme imzanın farklı olmasına dayanır.
## D
Aşırı yüklenen constructor'ların adları birbirinden farklı olmalıdır (örn. `Nokta` ve `NoktaXY`)
🤖 Tüm constructor'ların adı sınıf adıyla aynıdır; ayrım addan değil parametre imzasından gelir.
## Doğru
C
## Açıklama
**Constructor aşırı yükleme (overloading)**, aynı sınıfta **parametre listesi farklı** (parametre sayısı, tipleri veya sırası) birden çok constructor tanımlamaktır. Örneğin `Nokta()`, `Nokta(int x, int y)`, `Nokta(Nokta baska)` bir arada bulunabilir. Ayrım yalnızca parametre imzasından yapılır: constructor'ların **dönüş tipi yoktur** (B yanlış) ve hepsinin **adı sınıf adıyla aynıdır** (D yanlış). Bu, nesneyi farklı bilgi setleriyle kurma esnekliği sağlar; genellikle biri diğerini `this(...)` ile çağırarak tekrar önlenir.

# ctor-3 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Dikdortgen {
    int en, boy;
    Dikdortgen() { this(1, 1); }
    Dikdortgen(int kare) { this(kare, kare); }
    Dikdortgen(int en, int boy) { this.en = en; this.boy = boy; }
    int alan() { return en * boy; }
}
System.out.println(new Dikdortgen().alan() + " " + new Dikdortgen(5).alan());
```
## A
`1 25`
🤖 Doğru: parametresiz this(1,1) → alan 1; Dikdortgen(5) → this(5,5) → alan 25.
## B
`0 0`
🤖 this() zinciri alanları gerçekten atar; alanlar varsayılan 0'da kalmaz.
## C
`1 5`
🤖 Dikdortgen(5) tek parametreli, this(5,5) çağırır; alan 5 değil 5*5=25 olur.
## D
Derleme hatası; `this(...)` constructor içinde kullanılamaz
🤖 `this(...)` ilk ifade olarak constructor'lar arası çağrı için geçerlidir; hata yoktur.
## Doğru
A
## Açıklama
**`this(...)` constructor zinciri**, bir constructor'ın aynı sınıftaki başka bir constructor'ı çağırarak tekrarı önlemesini sağlar. `new Dikdortgen()` → `this(1, 1)` → `en=1, boy=1` → alan **1**. `new Dikdortgen(5)` → `this(5, 5)` → `en=5, boy=5` → alan **25**. Çıktı: `1 25`. Böylece asıl atama mantığı tek yerde (`Dikdortgen(int, int)`) yaşar; diğerleri ona delege eder. Kritik kural: `this(...)` çağrısı constructor gövdesinin **ilk ifadesi** olmalıdır, aksi halde derleme hatası olur.

# ctor-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
class A {
    int x = 10;
    { System.out.print("blok "); }
    A() { System.out.print("ctor(x=" + x + ") "); }
}
new A();
```
## A
`ctor(x=10) blok`
🤖 Örnek blok, constructor gövdesinden ÖNCE (alan başlatıcılarıyla birlikte, yazım sırasıyla) çalışır.
## B
`ctor(x=0) blok`
🤖 Hem sıra hem değer yanlış: blok önce çalışır ve x o noktada 10'dur.
## C
`blok ctor(x=0)`
🤖 x alanı, constructor gövdesi çalışmadan önce başlatıcısıyla 10 olur; 0 kalmaz.
## D
`blok ctor(x=10)`
🤖 Doğru: önce alan init + örnek blok, sonra constructor gövdesi; x zaten 10 atanmış olur.
## Doğru
D
## Açıklama
`new A()` çağrısında başlatma sırası şudur: önce alanlar varsayılana (x=0) ayarlanır, sonra (üst sınıf `Object` constructor'ından sonra) **örnek alan başlatıcıları ve örnek `{}` blokları yazım sırasıyla** çalışır, EN SON constructor gövdesi. Burada `int x = 10;` ve `{ ... }` bloğu constructor gövdesinden önce çalışır: önce "blok " yazılır ve x=10 atanmış olur; sonra constructor "ctor(x=10) " yazar. Çıktı: `blok ctor(x=10)`. Bu sıra, constructor gövdesine girildiğinde alan başlatıcılarının çoktan uygulanmış olduğunu garanti eder.

# ctor-5 | Orta
## Soru
`super(...)` çağrısı için aşağıdakilerden hangisi doğrudur?
## A
Alt sınıf constructor'ında `super(...)` yazılmazsa, üst sınıf constructor'ı hiç çalışmaz
🤖 Aksine; yazmazsan derleyici parametresiz super()'i otomatik ekler, üst constructor mutlaka çalışır.
## B
`super(...)` yalnız bir arayüz (interface) implemente eden sınıflarda, arayüzün constructor'ını çağırmak için gereklidir
🤖 super, üst SINIF constructor çağrısıdır; arayüzlerin constructor'ı yoktur, ilişkisizdir.
## C
Bir constructor hem `this(...)` hem `super(...)` çağrısını aynı anda içerebilir
🤖 İkisi de "ilk ifade" olmak zorunda olduğundan aynı constructor'da yalnız biri bulunabilir.
## D
`super(...)` alt sınıf constructor'ının ilk ifadesi olmalıdır; yazılmazsa parametresiz `super()` örtük olarak çağrılır
🤖 Doğru: constructor zinciri kökten (Object) tabana doğru çalışır, super/this ilk ifade olmalı.
## Doğru
D
## Açıklama
Her constructor'ın ilk işi, üst sınıfın bir constructor'ını çağırmaktır. Açıkça `super(...)` yazmazsan, derleyici **parametresiz `super()`'i örtük olarak** ilk satıra ekler; bu yüzden üst sınıf constructor'ı her zaman çalışır (A yanlış). Hem `super(...)` hem `this(...)` **ilk ifade** olmak zorunda olduğundan bir constructor bunlardan yalnız **birini** içerebilir (C yanlış) — `this(...)` kullanırsan super çağrısı zincirin ucundaki constructor'a bırakılır. Sonuç: nesne kurulurken constructor zinciri en tepeden (`Object`) alt sınıfa doğru çalışır. Dikkat: üst sınıfın parametresiz constructor'ı yoksa ve sen açık `super(...)` yazmazsan derleme hatası olur.

# ctor-6 | İleri
## Soru
Aşağıdaki kod ne yazdırır? (Constructor'da polimorfizm tuzağı)

```java
class Ust {
    Ust() { yazdir(); }               // constructor override edilen metodu çağırıyor
    void yazdir() { System.out.println("Ust"); }
}
class Alt extends Ust {
    int deger = 42;
    void yazdir() { System.out.println("Alt deger=" + deger); }
}
new Alt();
```
## A
`Alt deger=42`
🤖 Tuzak tam burada: yazdir() üst constructor'dan çağrılırken deger henüz atanmamıştır.
## B
`Alt deger=0`
🤖 Doğru: dinamik dispatch Alt.yazdir'ı çağırır ama deger=42 henüz uygulanmadığı için 0'dır.
## C
`Ust`
🤖 Metotlar çalışma-zamanı tipine göre çözülür; nesne Alt olduğundan Alt.yazdir çağrılır, Ust.yazdir değil.
## D
Derleme hatası; constructor içinden override edilen metot çağrılamaz
🤖 Java bunu yasaklamaz (yalnız uyarır olabilir); kod derlenir ve tehlikeli sonuç üretir.
## Doğru
B
## Açıklama
Bu klasik **constructor'da polimorfizm tuzağıdır**. `new Alt()` önce `Ust()` constructor'ını çalıştırır; o da `yazdir()`'i çağırır. Metot çağrısı **çalışma-zamanı tipine (Alt)** göre çözüldüğü için `Alt.yazdir()` devreye girer — AMA bu noktada `Alt`'ın alan başlatıcıları (`deger = 42`) HENÜZ çalışmamıştır (alan başlatma, üst constructor bittikten sonradır). Bu yüzden `deger` hâlâ varsayılan değeri **0**'dır. Çıktı: `Alt deger=0`. Ders: **constructor içinden override edilebilir (overridable) metot çağırma** — alt sınıf, kendisi tam kurulmadan önce çağrılır ve yarı-başlatılmış (partially constructed) durumu görür. Constructor'lar yalnız `private`, `static` veya `final` (override edilemeyen) metotları güvenle çağırabilir.
