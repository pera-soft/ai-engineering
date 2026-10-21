---
slug: kalitim-super-this
baslik: "Kalıtım: Constructor Zinciri, Alan Gizleme ve Tekli Kalıtım"
kademe: Temel
sira: 10
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir aile düşün: çocuk, anne babadan bazı özellikleri hazır alır ve üstüne kendininkileri ekler. Kalıtım da budur: yeni bir sınıf, var olan bir sınıfın hazır özelliklerini devralır, sonra bir kısmını değiştirir ya da üstüne yenilerini ekler. Çözdüğü problem, birbirine çok benzeyen sınıflarda aynı kodu tekrar tekrar yazmak ve her düzeltmeyi beş ayrı yerde yapmak zorunda kalmaktır. Ortak kısım tek yerde durur, farklar alt sınıfta yaşar.

### Temel kavramlar

- **Üst sınıf (superclass / taban sınıf)**: özellikleri veren sınıf. **Alt sınıf (subclass)**: onları devralan sınıf.
- **`extends`**: kalıtım anahtar kelimesi. `class Kopek extends Hayvan` → `Kopek`, `Hayvan`'ın alan ve metotlarını devralır.
- **Tekli kalıtım (single inheritance)**: Java'da bir sınıf `extends` ile **yalnız bir** sınıftan türeyebilir. Virgülle birden çok sınıf yazılamaz; "biri veri biri davranış için iki sınıf" diye bir kural yoktur. (Java'da sınıf kalıtımı elbette vardır; "yalnız arayüzler var" da yanlıştır.)
- **`super`**: üst sınıfa erişim anahtarı, iki biçimde kullanılır. (1) `super(...)` üst sınıfın belirli bir constructor'ını çağırır ve alt sınıf constructor'ının **ilk ifadesi** olmalıdır. (2) `super.uye` üst sınıfın alanına ya da alt sınıfta ezilmiş (override) metoduna erişir; örn. `super.hesapla()` alt sınıf `hesapla`'yı ezmiş olsa bile üst sınıfınkini çalıştırır.
- **`this`**: mevcut nesneyi işaret eder. `this.alan` ile aynı adlı parametre tarafından gölgelenen (shadowed) alanı seçersin; `this(...)` ile **aynı** sınıfın başka constructor'ını çağırırsın. Karıştırma: `this(...)` aynı sınıf, `super(...)` **üst** sınıf içindir. `super`'ın bellekle ilgisi yoktur — Java'da bellek çöp toplayıcıyla (garbage collector, GC) yönetilir ve yıkıcı (destructor) yoktur; statik metot çağırmanın da yolu `super` değil, sınıf adıdır.
- **`final`**: `final class` türetilemez (`extends` edilemez); `final` metot **override edilemez** ama normal devralınır ve **çağrılabilir**; `final` alan/değişken bir kez atanınca değiştirilemez. Bunlar birbirinden bağımsızdır: final sınıf, alanlarını otomatik final **yapmaz** ve final sınıftan **sınırsız** nesne yaratılabilir (singleton ile karıştırma).

Neden tek üst sınıf? Çoklu sınıf kalıtımı **elmas problemine (diamond problem)** yol açar: `D` sınıfı hem `B`'den hem `C`'den türese ve bu ikisi ortak üst sınıf `A`'nın `uye()` metodunu farklı biçimde ezmiş olsa, `D`'nin hangisini devralacağı belirsiz kalır. Java bu belirsizliği baştan keser. Birden çok kaynaktan **tip** almak gerekiyorsa `interface` kullanılır: bir sınıf birden çok arayüz `implements` edebilir, çünkü arayüzler geleneksel olarak durum/uygulama taşımaz. (Java 8 `default` metotlarındaki elmas çakışması ise açıkça çözülmek zorundadır.)

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Elmas problemi tekli kalitim">
  <defs>
    <marker id="kalitim-ok3" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Elmas problemi: neden çoklu sınıf kalıtımı yok</text>
  <rect x="260" y="36" width="120" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="58" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">A: uye()</text>
  <rect x="110" y="104" width="120" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="170" y="126" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">B: uye()</text>
  <rect x="410" y="104" width="120" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="470" y="126" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">C: uye()</text>
  <rect x="260" y="172" width="120" height="34" rx="8" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="194" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">D</text>
  <path d="M212,104 L296,70" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#kalitim-ok3)"/>
  <path d="M428,104 L344,70" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#kalitim-ok3)"/>
  <path d="M300,172 L214,140" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#kalitim-ok3)"/>
  <path d="M340,172 L426,140" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#kalitim-ok3)"/>
  <text x="320" y="152" text-anchor="middle" font-size="16" font-weight="700" fill="#B4690E" font-family="system-ui">?</text>
  <text x="320" y="217" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">D, uye()'yi B'den mi C'den mi alır? Java: tek üst sınıf; çoklu tip → interface</text>
</svg>
```

### Nasıl çalışır?

**Constructor zinciri.** Her constructor'ın ilk işi üst sınıfın constructor'ını çağırmaktır; açık `super(...)` yazmazsan derleyici parametresiz `super()`'i örtük olarak ekler.

```java
class Ust { Ust() { System.out.print("Ust "); } }
class Orta extends Ust { Orta() { System.out.print("Orta "); } }
class Alt extends Orta { Alt() { System.out.print("Alt "); } }
new Alt();      // Ust Orta Alt
```

`Alt()` girer → önce `super()` = `Orta()` → o da önce `super()` = `Ust()` → o da `super()` = `Object()`. Zincir en tepede tamamlanınca gövdeler **yukarıdan aşağıya** yazar: `Ust ` → `Orta ` → `Alt `. Yani nesne **temelden yukarıya** inşa edilir; bir alt sınıf constructor'ının gövdesi çalışmaya başladığında üst katmanlar çoktan hazırdır. Zincir ne en tepede durur ne de yalnız alt constructor çalışır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Constructor zinciri super akisi">
  <defs>
    <marker id="kalitim-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
    <marker id="kalitim-ok2" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">new Alt(): zincir en tepeden tamamlanır</text>
  <rect x="245" y="42" width="150" height="38" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Ust</text>
  <rect x="245" y="100" width="150" height="38" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="124" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Orta</text>
  <rect x="245" y="158" width="150" height="38" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="182" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Alt</text>
  <path d="M232,188 L232,58" stroke="#186399" stroke-width="1.5" fill="none" marker-end="url(#kalitim-ok)"/>
  <text x="150" y="112" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">super() ↑</text>
  <text x="150" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önce üstü çağırır</text>
  <path d="M408,52 L408,182" stroke="#25804C" stroke-width="1.5" fill="none" marker-end="url(#kalitim-ok2)"/>
  <text x="512" y="112" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">gövde ↓</text>
  <text x="512" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Ust → Orta → Alt</text>
</svg>
```

**`super.metot()` ile genişletme.**

```java
class Hesaplayici { int hesapla(int n) { return n * 2; } }
class LoglayanHesaplayici extends Hesaplayici {
    @Override
    int hesapla(int n) { return super.hesapla(n) + 1; }   // üst sürümü çağır, sonra ekle
}
Hesaplayici h = new LoglayanHesaplayici();
System.out.println(h.hesapla(10));    // 21
```

`h`'nin bildirilen tipi `Hesaplayici` olsa da nesnenin **gerçek tipi** `LoglayanHesaplayici`'dir; metotlar çalışma-zamanı tipine göre çözüldüğünden alt sınıfın ezen sürümü çalışır. O da `super.hesapla(10)` ile üst sınıfın **orijinal** metodunu çağırır (`10 * 2 = 20`), üstüne `+ 1` ekler → **21**. `super.metot()` bir override içinde tamamen geçerlidir; üst davranışı **tümüyle atmadan genişletmenin** standart yoludur. `@Override` anotasyonu ise derleyiciye "bu bir ezmedir" diyerek imza uyuşmazlığı gibi hataları erken yakalatır.

### Mülakatta ne sorulur?

- **Alan gizleme (field hiding)** — en sevilen tuzak:

```java
class Ust { int x = 1; }
class Alt extends Ust { int x = 2; }

Ust u = new Alt();
Alt a = new Alt();
System.out.println(u.x + " " + a.x);   // 1 2
```

Alanlar **override edilmez, gizlenir**. `Alt` içindeki `x`, `Ust`'un `x`'ini ezmez; her nesnede **iki ayrı `x` alanı** birlikte var olur. Hangisinin görüldüğünü **referansın derleme-zamanı (statik) tipi** belirler: `u`'nun tipi `Ust` → **1**, `a`'nın tipi `Alt` → **2**. Bu, metotların davranışından **köklü biçimde farklıdır** (metotlar nesnenin gerçek tipine göre, dinamik dağıtımla çözülür). Tutarsızlık kafa karıştırdığı için pratikte alan gizlemeden kaçınılır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Alan gizleme vs metot override">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Alan gizleme ≠ Metot override</text>
  <rect x="24" y="44" width="290" height="130" rx="10" fill="#FBF0E6" stroke="#B4690E" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#B4690E" font-family="system-ui">Alan (field)</text>
  <text x="169" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">GİZLENİR (hidden)</text>
  <text x="169" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">referansın derleme-zamanı</text>
  <text x="169" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tipine göre çözülür</text>
  <text x="169" y="158" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ust u = new Alt() → Ust'un alanı</text>
  <rect x="326" y="44" width="290" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Metot (method)</text>
  <text x="471" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">OVERRIDE edilir</text>
  <text x="471" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">nesnenin gerçek (runtime)</text>
  <text x="471" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tipine göre çözülür</text>
  <text x="471" y="158" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ust u = new Alt() → Alt'ın metodu</text>
</svg>
```

- **`final` incelikleri.** `String`, `Integer`, `LocalDate` gibi sınıflar `final`'dir; genelde güvenlik ve değişmezlik (immutability) garantisini korumak için. `final` metot alt sınıfta **çağrılabilir**, yalnızca ezilemez. `final`'i performans için değil, **tasarım niyetini belgelemek** için kullan: `final` çağrının tek olası hedefi olduğunu garanti eder, ama modern JIT derleyicisi bu çıkarımı çoğu durumda kendi de yapar.
- **`super` ne değildir?** Aynı sınıfın constructor'ını çağırmaz (o `this(...)`'tir ve ilk ifade olmalıdır); bellek serbest bırakma/yıkıcı çalıştırma aracı değildir; statik metot çağırma kısayolu değildir.
- **Neden tekli kalıtım?** Cevap "elmas problemi belirsizliği"dir; çoklu **tip** ihtiyacı arayüzlerle karşılanır.
- **Zincir sırası** sorularında cevap her zaman "en tepeden aşağıya tamamlanır"dır.

### Tek cümlede

Kalıtım, bir sınıfın tek bir üst sınıftan alan ve davranış devralmasıdır; constructor zinciri nesneyi tepeden aşağı kurar, metotlar çalışma-zamanı tipine göre çözülürken alanlar bildirilen tipe göre çözülüp yalnızca gizlenir.

# inh-1 | Temel
## Soru
Java'da bir sınıf en fazla kaç sınıftan (class) doğrudan kalıtım (extends) alabilir ve bunun temel nedeni nedir?
## A
En fazla bir sınıftan; çünkü çoklu kalıtım elmas problemi (diamond problem) doğurur
🤖 Doğru: tekli kalıtım, aynı üyeyi iki üst sınıftan devralma belirsizliğini önler.
## B
Sınırsız; virgülle ayrılıp birden çok sınıf `extends` edilebilir
🤖 Java çoklu sınıf kalıtımını desteklemez; `extends` yalnız tek sınıf alır.
## C
En fazla iki sınıftan; biri veri, biri davranış için
🤖 Böyle bir kural yoktur; Java'da tek üst sınıf sınırı vardır.
## D
Hiç; Java'da kalıtım yoktur, yalnız arayüzler vardır
🤖 Java'da sınıf kalıtımı vardır (`extends`); yalnız tek üst sınıfla sınırlıdır.
## Doğru
A
## Açıklama
Java **tekli kalıtımı (single inheritance)** benimser: bir sınıf `extends` ile yalnız **bir** sınıftan türeyebilir. Neden? Çoklu sınıf kalıtımı **elmas problemine (diamond problem)** yol açar: `D` sınıfı hem `B`'den hem `C`'den türese ve bu ikisi ortak üst sınıf `A`'nın `uye()` metodunu farklı biçimde ezmiş olsa, `D`'nin hangisini devralacağı belirsiz kalır. Java bu belirsizlikten kaçınmak için sınıf kalıtımını teke indirir; birden çok kaynaktan davranış almak gerekiyorsa `interface` kullanılır (bir sınıf birden çok arayüz `implements` edebilir, çünkü arayüzler geleneksel olarak durum/uygulama taşımaz — Java 8 default metotlarında elmas çakışması ise açıkça çözülmek zorundadır).

# inh-2 | Temel
## Soru
`super` anahtar kelimesinin işlevi aşağıdakilerden hangisinde doğru verilmiştir?
## A
Aynı sınıfın başka bir constructor'ını, gövdenin herhangi bir yerinden çağırmak
🤖 Aynı sınıfın constructor'ını çağırmak `this(...)`'in görevidir ve ilk ifade olmalıdır; `super(...)` ÜST sınıfın constructor'ını çağırır.
## B
Nesnenin belleğini elle serbest bırakmak (deallocate) ve yıkıcıyı (destructor) çalıştırmak
🤖 Java'da bellek çöp toplayıcıyla (GC) yönetilir, yıkıcı da yoktur; super'ın bellekle ilgisi bulunmaz.
## C
Üst sınıfın constructor'ını (`super(...)`) veya gizlenmiş/override edilmiş üyesini (`super.uye`) çağırmak
🤖 Doğru: super üst sınıfa erişim sağlar — hem constructor hem üye düzeyinde.
## D
Statik (static) bir metodu sınıf adı olmadan çağırmak
🤖 super örnek (instance) düzeyinde çalışır; statik çağrı için sınıf adı kullanılır.
## Doğru
C
## Açıklama
`super`, alt sınıftan **üst sınıfa** erişim anahtarıdır ve iki biçimde kullanılır: (1) `super(...)` — üst sınıfın belirli bir constructor'ını çağırır ve alt sınıf constructor'ının **ilk ifadesi** olmalıdır; (2) `super.uye` — üst sınıfın alanına veya (alt sınıfta override edilmiş) metoduna erişir, örn. `super.hesapla()` alt sınıf `hesapla`'yı ezmiş olsa bile üst sınıfınkini çalıştırır. `this(...)` ile karışır (A): `this(...)` **aynı** sınıfın başka constructor'ını, `super(...)` **üst** sınıfın constructor'ını çağırır.

# inh-3 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Ust { Ust() { System.out.print("Ust "); } }
class Orta extends Ust { Orta() { System.out.print("Orta "); } }
class Alt extends Orta { Alt() { System.out.print("Alt "); } }
new Alt();
```
## A
`Alt Orta Ust`
🤖 Ters sıra; constructor zinciri en tepeden (Ust) alta doğru tamamlanır.
## B
`Ust`
🤖 Zincir en tepede durmaz; Orta ve Alt constructor'ları da sırayla çalışır.
## C
`Alt`
🤖 Kalıtımda yalnız alt constructor değil, tüm zincir çalışır (örtük super çağrılarıyla).
## D
`Ust Orta Alt`
🤖 Doğru: her constructor önce super()'i çalıştırır, en tepe önce tamamlanır.
## Doğru
D
## Açıklama
Her constructor'ın ilk işi (açık `super(...)` yoksa örtük `super()` ile) üst sınıf constructor'ını çağırmaktır. `new Alt()` → `Alt()` girer, önce `super()` = `Orta()` çağırır → `Orta()` önce `super()` = `Ust()` çağırır → `Ust()` önce `super()` = `Object()` çağırır. Zincir en tepede tamamlanınca gövdeler **yukarıdan aşağıya** yazdırır: `Ust ` → `Orta ` → `Alt `. Çıktı: `Ust Orta Alt`. Yani nesne **temelden yukarıya** inşa edilir: bir alt sınıf constructor'ı gövdesine girdiğinde, üst kısımları çoktan kurulmuştur.

# inh-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır? (Alan gizleme / field hiding)

```java
class Ust { int x = 1; }
class Alt extends Ust { int x = 2; }

Ust u = new Alt();
Alt a = new Alt();
System.out.println(u.x + " " + a.x);
```
## A
`2 2`
🤖 Bu, alanların metotlar gibi override edildiğini varsayar; alanlar gizlenir, override edilmez.
## B
`2 1`
🤖 Tam ters: Ust tipli referans üst alanı (1), Alt tipli referans alt alanı (2) görür.
## C
`1 1`
🤖 a'nın tipi Alt olduğundan a.x, Alt'ın gizleyen alanını (2) verir; ikisi de 1 olmaz.
## D
`1 2`
🤖 Doğru: alan erişimi referansın derleme-zamanı tipine göre çözülür — u tipi Ust, a tipi Alt.
## Doğru
D
## Açıklama
Alanlar **override edilmez, gizlenir (field hiding)**. `Alt` içindeki `int x = 2`, `Ust`'un `x`'ini ezmez; her nesnede **iki ayrı x alanı** birlikte var olur. Hangisinin görüldüğünü **referansın derleme-zamanı (statik) tipi** belirler: `u`'nun tipi `Ust` olduğundan `u.x` = `Ust`'un alanı = **1**; `a`'nın tipi `Alt` olduğundan `a.x` = `Alt`'ın alanı = **2**. Çıktı: `1 2`. Bu, metotların davranışından **köklü biçimde farklıdır**: metotlar nesnenin **gerçek (runtime) tipine** göre çözülür (dinamik dispatch), alanlar ise **bildirilen tipe** göre. Bu tutarsızlık kafa karıştırıcı olduğundan, pratikte alan gizlemeden kaçınılır.

# inh-5 | Orta
## Soru
`final` anahtar kelimesinin sınıf ve metotlarla kullanımı için aşağıdakilerden hangisi doğrudur?
## A
`final class` türetilemez (extends edilemez); `final` metot override edilemez
🤖 Doğru: final sınıf kalıtımı, final metot ise ezilmeyi engeller.
## B
`final class` yalnız bir kez örneklenebilir (tek nesne)
🤖 Bu singleton kavramıyla karışıyor; final sınıftan istediğin kadar nesne yaratabilirsin.
## C
`final` metot alt sınıfta çağrılamaz
🤖 Aksine; final metot devralınır ve çağrılabilir, yalnız OVERRIDE edilemez.
## D
`final class` içindeki tüm alanlar otomatik olarak `final` olur
🤖 Sınıfın final olması alanları final yapmaz; alan finalliği ayrı ayrı belirtilir.
## Doğru
A
## Açıklama
`final` üç bağlamda farklı anlam taşır. **`final class`**: sınıf türetilemez, yani `extends` edilemez — `String`, `Integer`, `LocalDate` bu şekildedir; genelde güvenlik ve değişmezlik (immutability) garantisini korumak için. **`final` metot**: alt sınıflarda **override edilemez**, ama normal devralınır ve çağrılabilir (C yanlış). **`final` alan/değişken**: bir kez atanınca değiştirilemez. Bunlar bağımsızdır: final sınıf, alanlarını otomatik final yapmaz (D yanlış) ve final sınıftan sınırsız nesne yaratılabilir (B yanlış). `final` sınıf/metot çağrının tek olası hedefi olduğunu garanti eder; modern JIT derleyicisi bu çıkarımı çoğu durumda kendi de yaptığı için `final`'i performans için değil, **tasarım niyetini belgelemek** için kullan.

# inh-6 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Hesaplayici {
    int hesapla(int n) { return n * 2; }
}
class LoglayanHesaplayici extends Hesaplayici {
    @Override
    int hesapla(int n) {
        return super.hesapla(n) + 1;   // üst sınıfınkini çağır, sonra ekle
    }
}
Hesaplayici h = new LoglayanHesaplayici();
System.out.println(h.hesapla(10));
```
## A
`20`
🤖 Referans tipi Hesaplayici olsa da metot runtime tipine (LoglayanHesaplayici) göre çözülür; yalnız 20 değil.
## B
`21`
🤖 Doğru: LoglayanHesaplayici.hesapla çalışır, super.hesapla(10)=20 + 1 = 21.
## C
`11`
🤖 super.hesapla(10) 10*2=20 döndürür (10 değil); sonuç 20+1=21 olmalı.
## D
Derleme hatası; override edilen metot içinde `super` kullanılamaz
🤖 `super.metot()` override eden metot içinde üst sürümü çağırmanın standart yoludur; hata yoktur.
## Doğru
B
## Açıklama
`h`'nin bildirilen tipi `Hesaplayici` olsa da nesnenin **gerçek tipi** `LoglayanHesaplayici`'dir; metotlar runtime tipine göre çözüldüğünden `h.hesapla(10)` alt sınıfın override eden sürümünü çalıştırır. O da `super.hesapla(n)` ile **üst sınıfın orijinal** metodunu çağırır: `10 * 2 = 20`, üstüne `+ 1` → **21**. Çıktı: `21`. Bu, `super.metot()`'un tipik ve doğru kullanımıdır: alt sınıf, üst davranışı **tümüyle atmadan genişletir** (extend) — üst sonucu alıp üzerine ekler. `@Override` anotasyonu ise derleyiciye "bu bir ezmedir" diyerek imza uyuşmazlığı gibi hataları erken yakalatır.
