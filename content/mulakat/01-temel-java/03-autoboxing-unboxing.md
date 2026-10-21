---
slug: autoboxing-unboxing
baslik: "Autoboxing / Unboxing: Integer Önbelleği, == Tuzağı ve NPE"
kademe: Temel
sira: 3
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Şöyle düşün: elinde çıplak bir sayı var, 5. Çoğu yerde onu öylece kullanabilirsin. Ama bazı yerler — örneğin bir liste (`List`) — yalnız **nesne** kabul eder, çıplak değeri almaz. O zaman sayıyı bir "kutuya" koyup öyle vermen gerekir. Java bu kutulama işini senin yerine otomatik yapar; sen fark bile etmezsin. Kolaylık büyük, ama kutuların kendi kimlikleri olduğu için kod sen istemeden davranış değiştirir. Bu ders o görünmez dönüşümü ve doğurduğu üç tuzağı anlatıyor.

### Temel kavramlar

- **İlkel tip (primitive)**: `int`, `long`, `char`, `boolean`… — doğrudan değeri tutar, nesne değildir, `null` olamaz.
- **Sarmalayıcı sınıf (wrapper class)**: her ilkelin nesne karşılığı — `int` → `Integer`, `long` → `Long`, `char` → `Character`, `boolean` → `Boolean`, `byte` → `Byte`, `short` → `Short`. Bunlar birer **nesnedir**: heap'te yaşarlar ve `null` olabilirler.
- **Autoboxing (otomatik kutulama)**: ilkel → sarmalayıcı dönüşümünün derleyici tarafından otomatik eklenmesi. `Integer i = 5;` aslında `Integer i = Integer.valueOf(5);` demektir.
- **Unboxing (kutudan çıkarma)**: sarmalayıcı → ilkel dönüşümü. `int x = i;` aslında `int x = i.intValue();` demektir.
- **Referans kimliği**: nesneler için `==` "aynı içerik mi?" diye sormaz, "**aynı nesne mi?**" diye sorar. Tüm mesele burada.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="autoboxing unboxing dönüşümü ve NPE">
  <rect x="60" y="52" width="180" height="62" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="150" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">int (ilkel)</text>
  <text x="150" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">stack'te değer</text>
  <rect x="400" y="52" width="180" height="62" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Integer (nesne)</text>
  <text x="490" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">heap'te sarmalayıcı</text>
  <path d="M240 72 L400 72" stroke="#186399" stroke-width="1.8" marker-end="url(#box03)"/>
  <text x="320" y="64" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">autobox: valueOf()</text>
  <path d="M400 98 L240 98" stroke="#25804C" stroke-width="1.8" marker-end="url(#box03b)"/>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">unbox: intValue()</text>
  <text x="320" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">null Integer → int unbox = NullPointerException</text>
  <defs>
    <marker id="box03" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#186399"/>
    </marker>
    <marker id="box03b" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#25804C"/>
    </marker>
  </defs>
</svg>
```

İlkel doğrudan nesneye atanamaz; derleyici araya bu dönüşümü koymak **zorundadır**. (Karıştırılan bir isim: `Integer.parseInt` bir **`String`**'i `int`'e çevirir; elinde zaten `int` varken kullanılamaz.)

### Nasıl çalışır?

**1) Integer önbelleği (cache).** `Integer.valueOf`, **-128..127** arasındaki değerleri paylaşılan bir önbellekten döndürür. Yani bu aralıkta kutulanan iki `Integer` **aynı nesnedir**; aralık dışında ise her biri **ayrı nesnedir**.

```java
Integer a = 100, b = 100;   // ikisi de önbellekteki AYNI nesne
Integer c = 200, d = 200;   // her biri ayrı nesne
System.out.println((a == b) + " " + (c == d));   // true false
```

Aynı kural `Byte`, `Short` ve `Long` için de **-128..127**'dir; `Character` **0..127**'yi, `Boolean` ise iki değerini de (`true`/`false`) önbellekler.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Integer önbelleği -128..127">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Integer.valueOf ve -128..127 önbelleği</text>
  <rect x="60" y="48" width="230" height="110" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">-128 .. 127</text>
  <text x="175" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">önbellekten paylaşılır</text>
  <text x="175" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">a == b  →  true</text>
  <text x="175" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">(aynı nesne)</text>
  <rect x="350" y="48" width="230" height="110" rx="12" fill="#FBE9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="465" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#8A2E2E" font-family="system-ui">aralık dışı (ör. 128)</text>
  <text x="465" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">her seferinde yeni nesne</text>
  <text x="465" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">a == b  →  false</text>
  <text x="465" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">(ayrı nesneler)</text>
  <text x="320" y="182" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kural: sarmalayıcıları equals / intValue ile karşılaştır, == ile değil</text>
</svg>
```

**2) `new Integer(...)` neden farklı?** Önbelleği **hiç kullanmaz**; her çağrıda heap'te taze bir nesne üretir. Bu yüzden `Integer.valueOf(127) == Integer.valueOf(127)` **true**, ama `new Integer(127) == new Integer(127)` **false**'tur. İkisi de `Integer` döndürür — fark tip farkı değil, **nesne kimliği** farkıdır. Tam bu kafa karışıklığı yüzünden `new Integer(...)` Java 9'dan beri **kullanımdan kaldırılmış** (deprecated), Java 16'dan beri de **"kaldırılmak üzere"** (deprecated for removal) işaretlidir; derlenir ama uyarı verir.

**3) Unboxing ve NPE.**

```java
Integer sayi = null;
int x = sayi;      // NullPointerException
```

Derleyici bu satırı `int x = sayi.intValue();` biçimine çevirir; `sayi` **null** olduğu için çağrı **NullPointerException** ile patlar. Sinsi olan şu: kaynak kodda hiç `.intValue()` görünmez, NPE "havadan" gelmiş gibi durur. `int` bir ilkeldir — ne `null` tutabilir ne de sessizce `0` olur. Kod derlenir; sorun tamamen çalışma zamanındadır. Korunma: ilkele açmadan önce null kontrolü yap ya da `Optional`/varsayılan değer kullan.

**4) Kutulamanın maliyeti.** Her autoboxing bir nesne tahsisatıdır.

```java
Long toplam = 0L;                       // dikkat: ilkel long değil, sarmalayıcı Long
for (long i = 0; i < 1_000_000; i++) {
    toplam += i;                        // her turda: unbox → topla → yeniden box
}
```

Bir milyon tur, **bir milyon gereksiz nesne** demektir: CPU ve çöp toplayıcı (GC) baskısı. Çözüm tek kelime: `long toplam = 0L;` — ilkel kullanınca hiç kutulama olmaz. (Yan notlar: `1_000_000` gibi alt çizgili literaller geçerlidir, yalnız okunabilirlik içindir; `long` bu değerde taşmaz, döngü sonsuza girmez.)

**5) Ternary (koşullu operatör) tuzağı.** Bir dal sarmalayıcı (`Integer`), diğeri ilkel (`int`) ise ifadenin ortak tipi **ilkel `int`'e sabitlenir** (ikili sayısal terfi / binary numeric promotion).

```java
Integer a = null;
Object o = (true ? a : 0);   // NullPointerException
```

Koşul `true` olduğu için `a` dalı seçilir; ama ifadenin tipi `int`'e sabitlendiğinden `a` önce **unbox edilmeye** çalışılır → NPE. Hedef değişkenin `Object` olması kurtarmaz: ifade önce `int`'e açılır, sonra `Object`'e geri kutulanacaktır — patlama daha ilk adımda olur. Bu yüzden `o`'ya ne `0` ne de `null` atanır; kod ise sorunsuz **derlenir**. Korunma: ternary'nin iki dalını da aynı tipte tut (ikisi de sarmalayıcı ya da ikisi de ilkel).

### Mülakatta ne sorulur?

- **"`Integer i = 42;` derleyici arka planda ne üretir?"** → `Integer.valueOf(42)`. `new Integer(42)` değil (o önbelleği atlar), `Integer.parseInt(42)` hiç değil (o `String` bekler), "hiçbir dönüşüm" de değil.
- **`true false` klasiği.** 100 önbellekte (aynı nesne), 200 aralık dışı (ayrı nesneler). Bu, sarmalayıcıları `==` ile karşılaştırmanın neden güvenilmez olduğunun kanonik örneğidir: sonuç **değere göre** değişir.
- **Doğru karşılaştırma:** `a.equals(b)` ya da `a.intValue() == b.intValue()` — ikisi de her değerde tutarlı sonuç verir.
- **Null unboxing NPE.** Kaynakta görünmeyen `intValue()` çağrısını görebilmek kıdem işaretidir.
- **Performans.** Sarmalayıcıyı döngüde `+=` ile artırmak her turda unbox + box yapar; performans-kritik sayısal döngülerde ilkel tip kullanılır.
- **Karışık tipli ternary.** Üretimde gerçekten hata çıkaran, en çok elemede kullanılan ince kural budur.

### Tek cümlede

Autoboxing/unboxing ilkel ile sarmalayıcı arasına görünmez `valueOf`/`intValue` çağrıları koyar; bu da -128..127 önbelleği yüzünden `==`'i güvenilmez, `null` sarmalayıcıyı ise gizli bir NPE kaynağı yapar.

# box-1 | Temel
## Soru
`Integer i = 42;` satırında derleyici arka planda hangi çağrıyı üretir?
## A
`new Integer(42)`
🤖 Autoboxing `new` değil, önbellekli `valueOf` kullanır; ayrım kritiktir.
## B
`Integer.parseInt(42)`
🤖 `parseInt` bir `String`'i `int`'e çevirir; burada girdi zaten `int`, uygulanamaz.
## C
`Integer.valueOf(42)`
🤖 Doğru: autoboxing `valueOf`'a çevrilir, bu da önbelleği kullanabilir.
## D
Hiçbir dönüşüm; `int` doğrudan `Integer`'a atanır
🤖 İlkel ve nesne farklı tiptedir; derleyici dönüşüm (boxing) eklemek zorundadır.
## Doğru
C
## Açıklama
Autoboxing sırasında derleyici `Integer i = 42;` ifadesini `Integer i = Integer.valueOf(42);` biçimine çevirir. `valueOf` **önbelleği** (cache) kullandığından -128..127 aralığında paylaşılan nesne döner — bu, `new Integer(42)`'den (A) farkın kalbidir; `new` her zaman taze nesne yaratıp önbelleği atlar (bu yüzden Java 9'dan beri kullanımdan kaldırılmış, Java 16'dan beri de kaldırılmak üzere işaretlidir). `parseInt` (B) `String` girdisi bekler, burada uygulanamaz. İlkel doğrudan nesneye atanamaz; boxing zorunludur (D yanlış).

# box-2 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
Integer a = 100, b = 100;
Integer c = 200, d = 200;
System.out.println((a == b) + " " + (c == d));
```
## A
`true true`
🤖 200 önbellek aralığı (-128..127) dışında; ikinci karşılaştırma true olamaz.
## B
`false true`
🤖 Ters: küçük değer paylaşılır (true), büyük değer paylaşılmaz (false).
## C
`false false`
🤖 100 önbellektedir; ilk karşılaştırma true olmalı.
## D
`true false`
🤖 Doğru: 100 önbellekli (aynı nesne), 200 aralık dışı (ayrı nesneler).
## Doğru
D
## Açıklama
`valueOf` önbelleği **-128..127** aralığını paylaştırır. `a` ve `b` (100) aynı önbellek nesnesini gösterir → `a == b` **true**. `c` ve `d` (200) aralık dışı olduğu için her biri ayrı kutulanır → `c == d` **false**. Sonuç: `true false`. Bu, sarmalayıcıları `==` ile karşılaştırmanın neden güvenilmez olduğunun kanonik örneğidir: sonuç **değere** bağlı değişir. Doğrusu `a.equals(b)` veya `a.intValue() == b.intValue()` kullanmaktır — ikisi de her değerde tutarlı sonuç verir.

# box-3 | Temel
## Soru
Aşağıdaki kod çalışınca ne olur?

```java
Integer sayi = null;
int x = sayi;
```
## A
`NullPointerException` fırlatır
🤖 Doğru: `sayi.intValue()` null referans üzerinde çağrıldığından NPE olur.
## B
`x` değeri `0` olur
🤖 Unboxing null üzerinde 0 üretmez; `intValue()` çağrısı NPE fırlatır.
## C
Derleme hatası verir
🤖 Sözdizimi geçerli; sorun çalışma-zamanında ortaya çıkar, derleme geçer.
## D
`x` değeri `null` olur
🤖 `int` ilkeldir, `null` tutamaz; atama denemesi NPE ile sonuçlanır.
## Doğru
A
## Açıklama
`int x = sayi;` satırı unboxing gerektirir; derleyici bunu `int x = sayi.intValue();` biçimine çevirir. `sayi` **null** olduğundan `null.intValue()` çağrısı **NullPointerException** fırlatır. Bu, autoboxing'in en sinsi tuzağıdır: kaynak kodda hiç `.intValue()` görünmez, NPE "havadan" gelir gibi durur. `int` bir ilkeldir ve `null` (D) veya sessiz `0` (B) tutamaz. Derleme geçerlidir (C yanlış); sorun tamamen çalışma-zamanındadır. Koruma: sarmalayıcıyı ilkele açmadan önce null kontrolü yap veya `Optional`/varsayılan değer kullan.

# box-4 | Orta
## Soru
`Integer.valueOf(127) == Integer.valueOf(127)` `true`, ama `new Integer(127) == new Integer(127)` `false` verir. Neden?
## A
`valueOf` önbellekten aynı nesneyi döndürür; `new` her çağrıda ayrı nesne yaratır
🤖 Doğru: valueOf 127'yi önbellekten paylaşır; new önbelleği atlayıp taze nesne verir.
## B
`new Integer` derleme hatasıdır; bu satır hiç çalışmaz, karşılaştırma yapılamaz
🤖 `new Integer` (eski API) uyarıyla da olsa derlenir; hata değildir.
## C
`valueOf` `int`, `new` `Integer` döndürdüğü için tip farkı vardır
🤖 İkisi de `Integer` döndürür; fark tip değil, önbellek kullanımıdır.
## D
İkisi de aynı sonucu verir; soru yanlış
🤖 Sonuçlar farklıdır; valueOf önbellekli (true), new taze nesne (false).
## Doğru
A
## Açıklama
`Integer.valueOf(127)` önbellek aralığındaki (-128..127) **paylaşılan nesneyi** döndürür; iki çağrı aynı referansı verdiğinden `==` **true**'dur. `new Integer(127)` ise önbelleği **hiç kullanmaz**, her çağrıda **heap'te taze bir nesne** yaratır; iki ayrı nesne olduğundan `==` **false**'tur. Fark, tip farkı (C) değil — ikisi de `Integer` döndürür — sadece nesne kimliği farkıdır. `new Integer(...)` Java 9'dan beri kullanımdan kaldırılmış (deprecated), Java 16'dan beri de "kaldırılmak üzere" işaretlidir; tam da bu kafa karışıklığını önlemek için `valueOf` önerilir. Ama her iki durumda da doğru karşılaştırma `equals` iledir.

# box-5 | Orta
## Soru
Aşağıdaki döngünün performans açısından temel sorunu nedir?

```java
Long toplam = 0L;
for (long i = 0; i < 1_000_000; i++) {
    toplam += i;
}
```
## A
Sorun yok; bu döngü bir milyon toplama işlemi için yazılabilecek en verimli biçimdir
🤖 `toplam` sarmalayıcı `Long`; her adımda unbox+box olur, milyonlarca gereksiz nesne doğar.
## B
`1_000_000` alt çizgili literal geçersiz olduğundan kod derlenmez, düzeltilmesi gerekir
🤖 Alt çizgi (underscore) içeren literaller geçerlidir; okunabilirlik içindir, sorun değil.
## C
`i < 1_000_000` karşılaştırması `long` taşmasına yol açar ve döngü sonsuza girer
🤖 `long` bu değeri rahat tutar; taşma yok, sorun kutulamadır.
## D
`toplam`'ın `Long` (sarmalayıcı) olması her turda unboxing + autoboxing yapıp milyonlarca ara nesne yaratır
🤖 Doğru: `toplam += i` her turda kutu açıp yeni Long kutular; ilkel `long` bunu önler.
## Doğru
D
## Açıklama
`toplam` ilkel `long` değil, sarmalayıcı **`Long`** olarak bildirilmiş. `toplam += i` her iterasyonda: (1) `toplam`'ı ilkele **açar** (unbox), (2) toplar, (3) sonucu tekrar `Long`'a **kutular** (box). Bir milyon tur için bu **bir milyon gereksiz nesne tahsisi** demektir — CPU ve çöp toplayıcı (GC) baskısı yaratır. Çözüm: `long toplam = 0L;` (ilkel) kullanmak; hiç kutulama olmaz. Taşma yoktur (C; `long` yeterince geniş), alt çizgili literal geçerlidir (B; `1_000_000` okunabilirlik içindir). Ders: performans-kritik sayısal döngülerde ilkel tip kullan, sarmalayıcıdan kaçın.

# box-6 | İleri
## Soru
Aşağıdaki kod çalıştırılınca ne olur?

```java
Integer a = null;
Object o = (true ? a : 0);
```
## A
`o` değişkenine sorunsuzca `null` atanır; hiçbir istisna oluşmaz
🤖 Koşullu ifadenin tipi `int`'e sabitlenir; `a` unbox edilir ve NPE fırlar.
## B
`NullPointerException` fırlatılır ve atama hiç tamamlanmaz
🤖 Doğru: bir dal `int` olduğu için tüm ifade unboxing yapar; null `a` NPE üretir.
## C
`o` değişkenine, ikinci daldaki değer olan `0` atanır
🤖 Koşul `true` olduğundan seçilen dal `a`'dır; üstelik unboxing zaten NPE atar.
## D
Derleme hatası verir; `Integer` ve `int` dalları aynı ternary'de kullanılamaz
🤖 Kod derlenir; sorun çalışma-zamanındaki unboxing NPE'sidir.
## Doğru
B
## Açıklama
Bu, ternary (koşullu) operatörün ince bir kuralıdır: bir dal `Integer` (sarmalayıcı), diğeri `int` (ilkel) olduğunda, koşullu ifadenin **ortak tipi ilkel `int`'e** sabitlenir (binary numeric promotion). Bu yüzden seçilen dal `a` (koşul `true`) olsa bile, ifade `a`'yı **unbox** etmeye çalışır: `a.intValue()`. `a` **null** olduğundan **NullPointerException** fırlar — üstelik hedef değişkenin `Object` olması bu kuralı değiştirmez: ifade önce `int`'e açılır, sonra `Object`'e geri kutulanacaktır; ama unboxing daha ilk adımda patlar. Bu yüzden `o`'ya ne `0` (C) ne de `null` (A) atanabilir; kod derlenir (D yanlış). Bu tuzak gerçek üretim hatalarının kaynağıdır: karışık tipli ternary'lerde iki dalı da aynı tipte (ikisi de sarmalayıcı veya ikisi de ilkel) tutmak gerekir.
