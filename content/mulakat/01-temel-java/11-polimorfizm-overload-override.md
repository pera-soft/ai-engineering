---
slug: polimorfizm-overload-override
baslik: "Polimorfizm: Overload vs Override, Kurallar ve Dinamik Dispatch"
kademe: Temel
sira: 11
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uzaktan kumandanın "aç" düğmesini düşün: televizyona basılırsa ekranı açar, klimaya basılırsa soğutmayı başlatır. Sen tek bir şey yaparsın; sonuç, karşındaki cihaza göre değişir. Polimorfizm (çok biçimlilik) programda tam olarak budur: tek bir çağrı yazarsın, hangi kodun çalışacağını nesnenin kendisi belirler. Çözdüğü problem, her yeni tür için `if/else` zinciri yazmak ve yeni bir tür eklendiğinde bütün bu zincirleri elden geçirmektir.

### Temel kavramlar

Önce iki "tip" ayrımı; her şey buna dayanır.

- **Bildirilen tip / derleme-zamanı tipi (static type)**: değişkeni tanımlarken yazdığın tip. `Sekil s = new Daire(2);` içinde `s`'nin bildirilen tipi `Sekil`'dir. Derleyici **hangi metotların çağrılabileceğini** buna bakarak denetler.
- **Gerçek tip / çalışma-zamanı tipi (runtime type)**: bellekteki nesnenin asıl tipi — burada `Daire`. **Hangi uygulamanın çalışacağını** bu belirler.

Polimorfizmin iki yüzü vardır:

- **Aşırı yükleme (overloading) = derleme-zamanı (compile-time / static) polimorfizm**: Aynı sınıfta **aynı adlı, farklı parametre imzalı** metotlar. Hangisinin çağrılacağı **derleme zamanında, argümanların bildirilen tipine** göre seçilir.
- **Ezme (overriding) = çalışma-zamanı (runtime / dynamic) polimorfizm**: Alt sınıfın, üst sınıfın metodunu **aynı imzayla** yeniden tanımlaması. Hangisinin çalışacağı **çalışma zamanında, nesnenin gerçek tipine** göre seçilir; buna **dinamik dağıtım (dynamic dispatch)** denir.

Kısaca: overload "aynı ad, farklı imza, tek sınıf, derleme-zamanı"; override "aynı ad, aynı imza, kalıtım, çalışma-zamanı". Bu ikisi aynı şeyin iki adı **değildir** ve overloading yalnız constructor'lara özgü **değildir** — normal metotlarda da olur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Overload vs Override">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Overload (derleme-zamanı) vs Override (çalışma-zamanı)</text>
  <rect x="24" y="44" width="290" height="130" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Overload (aşırı yükleme)</text>
  <text x="169" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">aynı ad, FARKLI imza</text>
  <text x="169" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">derleme zamanında seçilir</text>
  <text x="169" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">argümanın bildirilen tipine göre</text>
  <text x="169" y="158" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">= static / compile-time polymorphism</text>
  <rect x="326" y="44" width="290" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Override (ezme)</text>
  <text x="471" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">aynı ad, AYNI imza</text>
  <text x="471" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">çalışma zamanında seçilir</text>
  <text x="471" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">nesnenin gerçek tipine göre</text>
  <text x="471" y="158" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">= dynamic dispatch / runtime poly.</text>
</svg>
```

### Nasıl çalışır?

```java
class Sekil { double alan() { return 0; } }
class Daire extends Sekil {
    double r;
    Daire(double r) { this.r = r; }
    @Override double alan() { return Math.PI * r * r; }
}
Sekil s = new Daire(2);
System.out.printf("%.2f%n", s.alan());     // 12,57
```

1. `Daire` bir `Sekil`'dir (is-a), dolayısıyla atama geçerlidir; derleme hatası yoktur.
2. Derleyici `s.alan()` çağrısını `Sekil`'de `alan()` var mı diye denetler — vardır, geçer. Referans tipinin belirlediği tek şey budur.
3. Çalışma zamanında JVM nesnenin gerçek tipine (`Daire`) bakar ve `Daire.alan()`'ı çalıştırır: π · 2 · 2 ≈ **12,57**. (0 değildir; 2πr ≈ 6,28 ise çevre formülüdür, alan değil.)
4. `printf("%.2f")` biçimlendirmeyi varsayılan yerel ayara (locale) göre yapar: Türkçe locale'de `12,57`, İngilizce bir locale'de `12.57` — değişen yalnız ondalık ayırıcıdır.

Bu ayrım polimorfizmin kalbidir: **referans tipi "ne çağrılabilir"i, gerçek tip "ne çalışır"ı** belirler. Bir `Sekil` dizisi üzerindeki tek bir `alan()` döngüsü, her şekle kendi formülünü uygulatır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dinamik dispatch">
  <defs>
    <marker id="poly-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Dinamik dispatch: Sekil s = new Daire(2); s.alan()</text>
  <rect x="24" y="44" width="290" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Referans tipi = Sekil</text>
  <text x="169" y="90" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">derleme-zamanı: hangi metot</text>
  <text x="169" y="104" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ÇAĞRILABİLİR (kontrol)</text>
  <rect x="326" y="44" width="290" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Gerçek tip = Daire</text>
  <text x="471" y="90" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">çalışma-zamanı: hangi uygulama</text>
  <text x="471" y="104" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ÇALIŞIR (seçim)</text>
  <path d="M320,110 L320,150" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#poly-ok)"/>
  <rect x="180" y="152" width="280" height="38" rx="8" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="176" text-anchor="middle" font-size="12" font-weight="700" fill="#B4690E" font-family="system-ui">Daire.alan() çalışır ≈ 12,57</text>
</svg>
```

**Geçerli bir override'ın kuralları:**

1. Aynı ad + aynı parametre listesi.
2. Dönüş tipi **aynı veya alt-tip** olabilir (**covariant return** — üst `Number` döndürürken alt `Integer` döndürebilir).
3. Erişim belirleyici **daraltılamaz** (üst `public` ise alt `protected`/`private` olamaz), yalnız genişletilebilir.
4. Kontrollü (checked) istisnalar **genişletilemez**: aynısı, daha dar bir alt-tipi, daha azı ya da hiçbiri bildirilebilir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Override kurallari">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Geçerli override (ezme) kuralları</text>
  <rect x="30" y="38" width="580" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">1) Aynı ad + aynı parametre imzası</text>
  <rect x="30" y="78" width="580" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">2) Dönüş tipi: aynı ya da alt-tip (covariant return)</text>
  <rect x="30" y="118" width="580" height="34" rx="7" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="140" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">3) Erişim daraltılamaz — yalnız genişletilebilir</text>
  <rect x="30" y="158" width="580" height="34" rx="7" fill="#F3ECF7" stroke="#7A3E9D" stroke-width="1.5"/>
  <text x="320" y="180" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">4) Checked exception genişletilemez (dar / az / hiç olabilir)</text>
</svg>
```

### Mülakatta ne sorulur?

- **Checked istisna genişletme — neden derlenmez?**

```java
class Ust { public void islem() throws java.io.IOException { } }
class Alt extends Ust {
    @Override
    public void islem() throws Exception { }    // DERLEME HATASI
}
```

`Exception`, `IOException`'ın **üst tipidir**, yani daha geniştir; kural ihlal edilir. Gerekçe sözleşmedir (contract): `Ust` tipli bir referansla `islem()` çağıran kod yalnız `IOException` yakalamaya hazırdır. Bu kısıt **yalnız checked istisnalar** içindir; `RuntimeException` ve alt tipleri imzada bildirilmese bile serbestçe atılabilir. Aynı örnekteki klasik yanlış açıklamalar: `void` metotlar pekâlâ override edilir; `public`'ten `public`'e geçmek erişimi daraltmak değildir ve tamamen geçerlidir; `@Override` yalnız `abstract` metotlara özgü değildir, gövdeli metotları ezerken de kullanılır.
- **`@Override` ne yapar, ne yapmaz?** Çalışma davranışını değiştirmez, sanal çağrı tablosunu atlatıp hızlandırmaz, metodu `public` yapmaz ve **zorunlu değildir** — anotasyonsuz da geçerli bir ezme çalışır. Faydası **derleme-zamanı doğrulamasıdır**: imza gerçekte eşleşmiyorsa (örn. `equals(Object)` yerine yanlışlıkla `equals(MyType)` yazıp farkında olmadan **overload** yaptıysan) derleyici **hata** verir. En iyi pratik: her ezmede yaz.
- **Statik metot gizleme (static method hiding)**:

```java
class Ust { static String kim() { return "Ust"; } }
class Alt extends Ust { static String kim() { return "Alt"; } }

Ust u = new Alt();
System.out.println(u.kim());      // Ust
```

Statik metotlar **override edilmez, gizlenir**. Statikler nesneye değil **sınıfa** bağlıdır ve çağrı **referansın derleme-zamanı tipine** göre çözülür; dinamik dağıtım **yoktur**. Nesnenin gerçek tipi `Alt` olsa bile sonuç `Ust`'tur. Alt sınıfta aynı imzalı static metot tanımlamak serbesttir (bu gizlemedir, derleme hatası değildir) ve ortada bir çalışma-zamanı istisnası da yoktur; ama `@Override` koyarsan derleyici hata verir, çünkü gizleme ezme değildir. (Nesne üzerinden statik çağırmak zaten kötü stildir; derleyici bunu `Ust.kim()` olarak çözer.)
- **Overload çözümü statik tiptedir**:

```java
class Yazici {
    void yaz(Object o) { System.out.println("Object"); }
    void yaz(String s) { System.out.println("String"); }
}
Object x = "merhaba";       // gerçek tip String, bildirilen tip Object
new Yazici().yaz(x);        // Object
```

Derleyici `x`'in **bildirilen** tipine (`Object`) bakar ve `yaz(Object)`'i seçer; nesnenin çalışma-zamanı tipine bakılmaz. Belirsizlik (ambiguous) yoktur ve iki metot birden çağrılmaz. `String` sürümünü istiyorsan ya değişkenin tipini `String` yapmalı ya da `yaz((String) x)` diye cast etmelisin. Bu tuzak "polimorfizm her zaman runtime'da çözülür" yanılgısını kırar: **override runtime, overload compile-time**.
- **Ters çevirme çeldiricisi**: "overriding derleme zamanında argümanın tipine, overloading çalışma zamanında nesnenin gerçek tipine göre çözülür" ifadesi tam tersidir.

### Tek cümlede

Override aynı imzayı kalıtımla yeniden tanımlayıp çağrıyı nesnenin gerçek tipine göre çalışma zamanında çözerken, overload aynı adı farklı imzayla çoğaltıp seçimi argümanın bildirilen tipine göre derleme zamanında yapar.

# poly-1 | Temel
## Soru
Overloading (aşırı yükleme) ile overriding (ezme) arasındaki temel fark aşağıdakilerden hangisidir?
## A
Overloading aynı sınıfta farklı imzalı metotlardır (derleme-zamanı); overriding alt sınıfın aynı imzalı metodu yeniden tanımlamasıdır (çalışma-zamanı)
🤖 Doğru: overload imza farkına, override kalıtım + aynı imzaya dayanır.
## B
İkisi de aynı mekanizmadır; "overload" ve "override" yalnız aynı şeyin iki farklı adıdır
🤖 Köklü farklıdırlar: biri aynı sınıfta farklı imza, diğeri alt sınıfta aynı imzadır.
## C
Overloading yalnız constructor'larda, overriding yalnız normal metotlarda olur
🤖 Overloading normal metotlarda da olur; ayrım "farklı imza" vs "aynı imza + kalıtım"dır.
## D
Overriding derleme zamanında argümanın tipine, overloading çalışma zamanında nesnenin gerçek tipine göre çözülür
🤖 Tam ters: overload derleme-zamanı (static), override çalışma-zamanı (dynamic) çözülür.
## Doğru
A
## Açıklama
**Overloading (aşırı yükleme)**: aynı sınıf içinde aynı adlı ama **farklı parametre imzalı** metotlar; derleyici hangisini çağıracağını **derleme zamanında argümanların tipine** göre seçer (compile-time / static polymorphism). **Overriding (ezme)**: bir alt sınıfın, üst sınıftan devraldığı metodu **aynı imzayla** yeniden tanımlaması; hangisinin çalışacağı **çalışma zamanında nesnenin gerçek tipine** göre belirlenir (runtime / dynamic dispatch). Kısaca: overload "aynı ad, farklı imza, tek sınıf, derleme-zamanı"; override "aynı ad, aynı imza, kalıtım, çalışma-zamanı". D çeldiricisi bu iki çözüm zamanını ters çevirir.

# poly-2 | Temel
## Soru
`@Override` anotasyonunun asıl faydası nedir?
## A
Ezen metodu, sanal çağrı tablosunu atlatarak daha hızlı çalıştırır
🤖 Anotasyon çalışma hızını değiştirmez; yalnız derleyiciye niyeti bildirir.
## B
Ezen metodu otomatik olarak `public` yapar ve erişim daraltma hatasını önler
🤖 Erişim belirleyicisini değiştirmez; onu ayrıca sen yazarsın.
## C
Metodun gerçekten bir üst tip metodunu ezdiğini derleyiciye doğrulatır; imza uyuşmazlığında derleme hatası verir
🤖 Doğru: yanlış imzayla "ezdiğini sanma" hatasını erkenden yakalatır.
## D
Zorunludur; `@Override` yazılmadan bir metot üst sınıfınkini ezemez, yeni bir metot sayılır
🤖 Ezme anotasyonsuz da çalışır; @Override yalnız derleme-zamanı doğrulaması sağlar, zorunlu değildir.
## Doğru
C
## Açıklama
`@Override` **çalışma davranışını değiştirmez** — anotasyonsuz da geçerli bir ezme çalışır (D yanlış). Faydası **derleme-zamanı güvenliğidir**: derleyiciye "bu metot bir üst tip metodunu eziyor" der; eğer imza aslında eşleşmiyorsa (örn. parametre tipini yanlışlıkla değiştirdiysen ve farkında olmadan **overload** yaptıysan) derleyici **hata** verir. Bu, "ezdiğimi sandım ama aslında yeni bir metot tanımladım" sınıfındaki sinsi hataları erkenden yakalar — özellikle `equals(Object)` yerine yanlışlıkla `equals(MyType)` yazmak gibi klasik tuzaklarda hayat kurtarır. En iyi pratik: her ezmede kullan.

# poly-3 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Sekil { double alan() { return 0; } }
class Daire extends Sekil {
    double r;
    Daire(double r) { this.r = r; }
    @Override double alan() { return Math.PI * r * r; }
}
Sekil s = new Daire(2);
System.out.printf("%.2f%n", s.alan());
```
## A
`0,00`
🤖 Referans tipi Sekil olsa da nesne Daire'dir; alan() Daire'nin sürümüne çözülür, 0 değil.
## B
`6,28`
🤖 Bu 2πr (çevre) hesabıdır; alan πr² = π*4 ≈ 12,57'dir.
## C
Derleme hatası; `Sekil` tipli değişkene `Daire` atanamaz
🤖 Daire bir Sekil'dir (is-a), atama geçerlidir; hata yoktur.
## D
`12,57`
🤖 Doğru: dinamik dispatch Daire.alan()'ı çağırır → π*2*2 ≈ 12,57.
## Doğru
D
## Açıklama
Bu **çalışma-zamanı polimorfizmidir**. `s`'nin bildirilen tipi `Sekil` olsa da, `s.alan()` çağrısı nesnenin **gerçek tipine (Daire)** göre çözülür (dynamic dispatch) ve `Daire.alan()` çalışır: `π * 2 * 2 ≈ 12,57`. Çıktı (Türkçe yerel ayarın ondalık virgülüyle) `12,57`. Not: `printf("%.2f")` biçimlendirmeyi varsayılan yerel ayara (locale) göre yapar; İngilizce bir locale'de aynı değer `12.57` görünürdü — değişen yalnız ondalık ayırıcıdır. Referans tipinin `Sekil` olması yalnız **hangi metotların çağrılabileceğini** (derleme-zamanı kontrol) belirler; **hangi uygulamanın çalışacağını** nesnenin gerçek tipi belirler. Bu ayrım polimorfizmin kalbidir: `Sekil` dizisi üzerinde tek bir `alan()` döngüsü, her şekle kendi formülünü uygulatır.

# poly-4 | Orta
## Soru
Aşağıdaki override girişimi neden **derlenmez**?

```java
class Ust { public void islem() throws java.io.IOException { } }
class Alt extends Ust {
    @Override
    public void islem() throws Exception { }   // <-- burada
}
```
## A
Dönüş tipi `void` olan metotlar override edilemez; ezme için metodun bir değer döndürmesi gerekir
🤖 void metotlar pekâlâ override edilir; sorun dönüş tipi değildir.
## B
Override eden metot üst metodun erişim düzeyini genişletemez; alt metot `public` yerine `protected` olmalıdır
🤖 Erişim daraltılamaz ama genişletilebilir; üst `public` iken alt `public` kalması tamamen geçerlidir.
## C
`@Override` yalnız `abstract` metotları uygularken kullanılabilir, gövdeli metotları ezerken kullanılamaz
🤖 @Override somut (gövdeli) metotları ezmede de kullanılır; böyle bir sınırlama yoktur.
## D
Override eden metot, üst metodunkinden daha GENİŞ bir kontrollü (checked) istisna bildiremez
🤖 Doğru: `Exception`, `IOException`'ın üst tipidir; checked istisna daraltılabilir ama genişletilemez.
## Doğru
D
## Açıklama
Override kuralı: eden metot, üst metodun bildirdiği **kontrollü (checked) istisnaları genişletemez** — yalnız **aynısını, daha dar bir alt-tipini veya daha azını** (hatta hiç) bildirebilir. Üst metot `IOException` atarken alt metot `Exception` (ki `IOException`'ın **üst tipi**, yani daha geniş) atmaya kalkarsa kural ihlal edilir ve **derleme hatası** olur. Gerekçe: polimorfik kullanımda `Ust` tipli bir referansla `islem()` çağıran kod yalnız `IOException` yakalamaya hazırdır; alt sınıf daha geniş bir istisna atarsa bu sözleşme (contract) bozulur. Not: bu kısıt yalnız **checked** istisnalar içindir — `RuntimeException` ve alt tipleri serbestçe atılabilir (imzada bildirilmese bile).

# poly-5 | Orta
## Soru
Aşağıdaki kod ne yazdırır? (Statik metot gizleme / static method hiding)

```java
class Ust { static String kim() { return "Ust"; } }
class Alt extends Ust { static String kim() { return "Alt"; } }

Ust u = new Alt();
System.out.println(u.kim());
```
## A
`Alt`
🤖 Bu, statik metodun override edildiğini (runtime tipiyle çözüldüğünü) varsayar; statikler gizlenir.
## B
`Ust`
🤖 Doğru: static metotlar override edilmez, gizlenir ve referansın derleme-zamanı tipine (Ust) göre çözülür.
## C
Derleme hatası; static metot alt sınıfta yeniden tanımlanamaz
🤖 Aynı imzalı static metot alt sınıfta tanımlanabilir (gizleme); hata yoktur.
## D
Çalışma-zamanı istisnası fırlatır
🤖 İstisna yoktur; sade bir statik metot çözümüdür.
## Doğru
B
## Açıklama
**Statik (static) metotlar override edilmez, GİZLENİR (method hiding)**. Statikler nesneye değil **sınıfa** bağlıdır ve çağrı **referansın derleme-zamanı (statik) tipine** göre çözülür — dinamik dispatch YOKTUR. `u`'nun bildirilen tipi `Ust` olduğundan `u.kim()` = `Ust.kim()` = **"Ust"**, nesnenin gerçek tipi `Alt` olsa bile. Çıktı: `Ust`. (Nesne üzerinden statik metot çağırmak zaten kötü stildir; derleyici `Ust.kim()` diye çözer.) Bu, örnek metotların davranışının **tam tersidir**: örnek metotlar runtime tipiyle çözülüp `Alt`'ı verirdi. `@Override`'ı statik metoda koyarsan derleyici hata verir — çünkü gizleme, ezme değildir.

# poly-6 | Orta
## Soru
Aşağıdaki kod ne yazdırır? (Overload çözümü ve statik tip)

```java
class Yazici {
    void yaz(Object o) { System.out.println("Object"); }
    void yaz(String s) { System.out.println("String"); }
}
Object x = "merhaba";      // gerçek tip String, bildirilen tip Object
new Yazici().yaz(x);
```
## A
`String`
🤖 Overload çözümü çalışma zamanı tipine değil, argümanın BİLDİRİLEN tipine (Object) göre yapılır.
## B
`Object`
🤖 Doğru: overload derleme zamanında çözülür; x'in bildirilen tipi Object olduğundan yaz(Object) seçilir.
## C
Derleme hatası; belirsiz (ambiguous) çağrı
🤖 Belirsizlik yok; bildirilen tip Object olduğu için tek uygun overload yaz(Object)'tir.
## D
Her iki metot da çağrılır, iki satır yazar
🤖 Tek bir overload seçilir; iki metot birden çağrılmaz.
## Doğru
B
## Açıklama
Bu, override ile overload arasındaki en önemli ayrımı gösterir. **Overload çözümü derleme zamanında, argümanın BİLDİRİLEN (statik) tipine göre** yapılır — nesnenin çalışma-zamanı tipine bakılmaz. `x`'in gerçek tipi `String` olsa da **bildirilen tipi `Object`** olduğundan derleyici `yaz(Object)`'i seçer ve çıktı **"Object"** olur. Eğer `String`'i istiyorsan ya değişkenin tipini `String` yapmalı ya da çağrıda `yaz((String) x)` gibi cast etmelisin. Karşılaştır: override olsaydı (aynı imza + kalıtım), çözüm runtime tipine göre yapılırdı. Bu tuzak, "polimorfizm her zaman runtime'da çözülür" yanılgısını kırar: **override runtime, overload compile-time**.
