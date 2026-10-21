---
slug: abstract-interface
baslik: "Abstract Sınıf vs Interface: Ne Zaman Hangisi, Default Metotlar, Diamond"
kademe: Temel
sira: 12
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

İki belge düşün. Birincisi yarım bırakılmış bir ev planı: temel ve duvarlar çizilmiş, çatı ise "buraya kendi çatını koy" diye boş bırakılmış — sana hazır bir iskelet verir. İkincisi bir ehliyet: "bu kişi araç kullanabilir" der, kişinin kim olduğuna hiç karışmaz. **Soyut sınıf** birinci belgedir, **arayüz** ikincisidir. İkisinin de çözdüğü problem aynıdır: kodun geri kalanı, karşısındaki nesnenin tam olarak ne olduğunu bilmeden onunla çalışabilsin. İkisi de doğrudan `new` ile örneklenemez ve alt tiplere bir sözleşme dayatır.

### Temel kavramlar

- **Soyut sınıf (abstract class)**: `abstract` ile işaretlenmiş, örneklenemeyen sınıf. İçinde gövdesiz (`abstract`) metotlar olabilir; ama **constructor'ı, örnek alanları (durum/state) ve her erişim düzeyinde gövdeli metotları** da olabilir. Bir sınıf **yalnız bir** soyut sınıftan türeyebilir. Yeri: alt sınıfların paylaşacağı ortak **durum** ve **kısmi uygulama**, güçlü bir "**is-a**" hiyerarşisi, `protected` yardımcılarla iskelet sunmak (örn. `AbstractList`).
- **Arayüz (interface)**: bir **yeteneği / sözleşmeyi** tanımlar — "bunu yapabilir": `Comparable`, `Runnable`. Aralarında hiçbir akrabalık olmayan sınıflara bile aynı yetenek dayatılabilir. Bir sınıf **birden çok** arayüzü `implements` edebilir; Java'da **çoklu tip kalıtımının** yolu budur. Arayüzler **örnek alanı tutamaz** (yalnız örtük `public static final` sabitler) ve **constructor'ları yoktur**.

Sayıya dökersek: bir sınıf en fazla **1** sınıftan (abstract olsun olmasın) `extends` eder, ama **istediği kadar** arayüzü `implements` eder. Bu, "çoklu **tip** kalıtımı ama tekli **uygulama/durum** kalıtımı" tasarımıdır: arayüzler geleneksel olarak durum taşımadığı için elmas problemi durum düzeyinde hiç oluşmaz. Tipik desen bir abstract temel sınıf + birden çok yetenek arayüzüdür: `class Ordek extends Kus implements Yuzebilir, Ucabilir`.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Abstract vs Interface">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Abstract sınıf mı, interface mi?</text>
  <rect x="24" y="44" width="290" height="140" rx="10" fill="#FBF0E6" stroke="#B4690E" stroke-width="1.5"/>
  <text x="169" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B4690E" font-family="system-ui">Abstract class</text>
  <text x="169" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ortak DURUM + kısmi uygulama</text>
  <text x="169" y="112" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">güçlü "is-a"; constructor + alan olur</text>
  <text x="169" y="132" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yalnız BİR tanesinden türenir</text>
  <text x="169" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">örn. AbstractList</text>
  <rect x="326" y="44" width="290" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="471" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Interface</text>
  <text x="471" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">YETENEK / sözleşme ("can-do")</text>
  <text x="471" y="112" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ilgisiz sınıflara da uygulanır</text>
  <text x="471" y="132" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ÇOKLU implements mümkün</text>
  <text x="471" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">default/static/private metotlar</text>
</svg>
```

### Nasıl çalışır?

Java 8 ile arayüzlere üç ek metot türü geldi:

- **`default` metot (Java 8)**: Arayüzde **gövdeli** varsayılan uygulama. Asıl motivasyonu **geriye dönük uyumluluktur (backward compatibility)**: Java 8, `Collection` gibi yaygın arayüzlere `stream()`, `forEach()` eklemek istedi; bunları gövdesiz (soyut) eklemek, o arayüzü implemente eden **milyonlarca mevcut sınıfı** derlenemez hale getirirdi. `default` sayesinde yeni metot bir varsayılan davranışla gelir, eski sınıflar hiç değişmeden çalışır, isteyen sınıf ezer. Dikkat: `default` metot arayüze gövde ekler ama arayüzü **örneklenebilir yapmaz**, **örnek alan tutturmaz** ve **constructor kazandırmaz**.
- **`static` metot (Java 8)**: Tipe bağlı **yardımcı (utility) işlev**; `Arayuz.metot()` ile çağrılır — `List.of(...)`, `Comparator.comparing(...)` gibi. İmplemente eden sınıfa **devralınmaz**; bu yüzden ne ezilebilir ne gizlenebilir.
- **`private` metot (Java 9)**: Birden çok `default`/`static` metodun paylaştığı **ortak iç mantığı** kapsülleyip tekrarı önler. Yalnız arayüzün **kendi içinde** görünür — implemente eden sınıfa bile kapalıdır, dışarıya sözleşme olarak sızmaz.

Böylece arayüz üç görünürlük katmanına sahip olur: public soyut/default = sözleşme, `static` = tip yardımcısı, `private` = iç detay.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Arayuz metot katmanlari">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Java 8+ arayüzünde metot katmanları</text>
  <rect x="30" y="38" width="580" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">public abstract → sözleşme; implemente eden sınıf ezmek zorunda</text>
  <rect x="30" y="78" width="580" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">default (Java 8) → gövdeli; geriye dönük uyumluluk, ezilebilir</text>
  <rect x="30" y="118" width="580" height="34" rx="7" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="140" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">static (Java 8) → tip yardımcısı Arayuz.metot(); devralınmaz</text>
  <rect x="30" y="158" width="580" height="34" rx="7" fill="#F3ECF7" stroke="#7A3E9D" stroke-width="1.5"/>
  <text x="320" y="180" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">private (Java 9) → iç yardımcı; dışarı sızmaz</text>
</svg>
```

**Default metot elması.** Aynı imzalı `default` metot iki farklı arayüzden gelirse ne olur?

```java
interface A { default String selam() { return "A"; } }
interface B { default String selam() { return "B"; } }
class C implements A, B {
    // ...
}                                  // DERLEME HATASI
```

Sorun çoklu `implements` değildir (o serbesttir); aynı imzalı **iki gövdenin çakışmasıdır**. Java "listedeki ilk arayüzü seç" gibi bir öncelik kuralı uygulamaz; belirsizliği **reddeder**. Arayüzlerden birinde metodu `abstract` (gövdesiz) yapmak da tek başına çözmez — `C` yine somut bir uygulama vermek zorunda kalır. Çözüm, `C`'nin metodu **açıkça ezip** niyetini belirtmesidir:

```java
class C implements A, B {
    @Override public String selam() { return A.super.selam(); }   // veya B, veya ikisini birleştir
}
```

`Arayuz.super.metot()` sözdizimi, belirli bir üst arayüzün default uygulamasına erişmenin yoludur. Java böylece durum içermeyen arayüzlerde bile çakışmayı **programcıya açıkça çözdürerek** güvende kalır. (Sınıf kalıtımında bu belirsizlik hiç oluşmaz, çünkü tek üst sınıf vardır.)

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Default metot elmasi">
  <defs>
    <marker id="abs-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Default metot elması ve çözümü</text>
  <rect x="70" y="38" width="180" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="160" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">interface A</text>
  <text x="160" y="78" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">default selam() → "A"</text>
  <rect x="390" y="38" width="180" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="480" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">interface B</text>
  <text x="480" y="78" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">default selam() → "B"</text>
  <rect x="230" y="112" width="180" height="42" rx="10" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="138" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">class C implements A, B</text>
  <path d="M180,86 L296,112" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#abs-ok)"/>
  <path d="M460,86 L344,112" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#abs-ok)"/>
  <text x="320" y="104" text-anchor="middle" font-size="15" font-weight="700" fill="#B4690E" font-family="system-ui">?</text>
  <text x="320" y="180" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Çakışma! C açıkça ezmeli:</text>
  <text x="320" y="198" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">@Override selam() { return A.super.selam(); }</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Hangisini seçerim?"** Alt sınıfların paylaşacağı ortak **durum** + kısmi uygulama + güçlü "is-a" hiyerarşisi → **abstract sınıf**. İlgisiz birçok sınıfa tek bir **yetenek** dayatmak ya da bir sınıfa **çoklu tip** kazandırmak → **interface**. Yalnız bir sabit grubunu paylaştırmak → **ikisi de değil**.
- **Constant interface anti-pattern.** Yalnız sabit (constant) tutmak için arayüz tanımlayıp sınıfların sabitlere kısa adla erişmek için onu `implements` etmesi kötü pratiktir. Teknik olarak mümkündür — arayüz alanları örtük `public static final`'dır, yani yeniden atanamazlar — sorun teknik değil **tasarımsaldır**: (1) arayüz bir **sözleşmedir**, "ne yapabilir" der; sabitler ise **uygulama detayıdır**. (2) `implements` eden sınıf bu sabitleri **kendi public API'sine kalıtımla taşır** ve alt sınıflara da yayar; nereden geldikleri kaybolur, isim çakışması riski doğar. Performansla ilgisi yoktur; derleyici sabitin değerini zaten kullanım yerine gömer. Doğrusu: sabitleri bir `enum`'da, ilgili sınıfın kendi `static final` alanlarında ya da yapıcısı gizli bir `final` yardımcı sınıfta tutmak, gerekirse `static import` ile kısaltmak.
- **Kaç tanesinden türenir?** En fazla 1 (abstract) sınıf + sınırsız arayüz. "Birden çok abstract sınıf" ya da "en fazla 1 interface" seçenekleri yanlıştır.
- **Sık yanlışlar.** "Default metotlar arayüze örnek alan kazandırır" — hayır, kazandırmaz. "Default metotla arayüz `new` ile örneklenir" — hayır; arayüz yalnız somut sınıf ya da lambda üzerinden kullanılır. "Arayüzde constructor tanımlanabilir" ve "`static` arayüz metodu devralınıp ezilebilir" — ikisi de yanlıştır. "`private` arayüz metoduna implemente eden sınıf erişebilir" — bu da yanlıştır.

### Tek cümlede

Abstract sınıf ortak durum ve kısmi uygulamayı tek bir "is-a" hiyerarşisinde paylaştırır; interface ise durum taşımadan bir yetenek sözleşmesi dayatıp çoklu tipe izin verir ve Java 8+ `default`/`static`/`private` metotlarıyla mevcut kodu bozmadan zenginleşir.

# abs-1 | Temel
## Soru
Aşağıdaki durumlardan hangisinde **interface** yerine **abstract sınıf** tercih etmek daha doğrudur?
## A
Alt sınıfların paylaşacağı ortak alan (state) ve kısmi uygulama sunmak, güçlü bir "is-a" hiyerarşisi kurmak istediğinde
🤖 Doğru: durum + kısmi uygulama + tek hiyerarşi tam olarak abstract sınıfın alanıdır.
## B
İlgisiz birçok sınıfa yalnız bir "yetenek" (örn. `Comparable`) dayatmak istediğinde
🤖 Bu tam olarak interface'in işidir; ilgisiz sınıflara ortak yetenek arayüzle verilir.
## C
Bir sınıfın aynı anda birden çok tipten davranış devralmasını istediğinde
🤖 Çoklu tip devralımı yalnız interface ile mümkündür; abstract sınıf teklidir.
## D
Yalnızca bir sabit (constant) grubunu tanımlayıp birçok sınıfa paylaştırmak istediğinde
🤖 Sabitler için enum ya da final yardımcı sınıf kullanılır; ne abstract sınıf ne arayüz uygundur.
## Doğru
A
## Açıklama
**Abstract sınıf** şu durumlarda doğrudur: alt sınıfların **ortak durumu (örnek alanları)** ve **kısmi uygulamayı** paylaşması gerekiyorsa, aralarında güçlü bir "**is-a**" ilişkisi varsa ve constructor/korumalı (protected) yardımcı metotlarla bir iskelet sağlamak istiyorsan (örn. `AbstractList`). **Interface** ise bir **yeteneği** ilgisiz sınıflara dayatmak (B) ve **çoklu tip** kazandırmak (C) için uygundur; bir sınıf tek abstract sınıftan ama birçok arayüzden türeyebilir. Yalnız sabit tutmak (D) ikisinin de işi değildir — bu, "constant interface" anti-pattern'ına düşmektir; sabitler `enum` ya da `final` bir yardımcı sınıfta durmalıdır.

# abs-2 | Temel
## Soru
Bir Java sınıfının aynı anda kaç abstract sınıftan ve kaç interface'ten türeyebileceği doğru verilmiştir?
## A
Birden çok abstract sınıf, birden çok interface
🤖 Çoklu sınıf kalıtımı Java'da yoktur; yalnız tek üst sınıf olabilir.
## B
Birden çok abstract sınıf, en fazla 1 interface
🤖 Ters: sınıf kalıtımı tekildir, arayüz uygulaması çoğuldur.
## C
En fazla 1 abstract sınıf, birden çok interface
🤖 Doğru: tekli sınıf kalıtımı + çoklu arayüz uygulaması Java'nın modelidir.
## D
En fazla 1 abstract sınıf, en fazla 1 interface
🤖 Arayüz sayısı 1 ile sınırlı değildir; bir sınıf istediği kadar arayüz implemente edebilir.
## Doğru
C
## Açıklama
Java **tekli sınıf kalıtımı** kullanır: bir sınıf (abstract olsun olmasın) `extends` ile **en fazla bir** üst sınıftan türeyebilir. Ama **`implements` ile birden çok arayüz** uygulayabilir. Bu, "çoklu **tip** kalıtımı ama tekli **uygulama/durum** kalıtımı" tasarımıdır: arayüzler geleneksel olarak durum (örnek alanı) taşımadığı için elmas problemi durum düzeyinde oluşmaz. Bu yüzden desen tipik olarak "bir abstract temel sınıf + birden çok yetenek arayüzü" biçiminde kurulur (örn. `class Ordek extends Kus implements Yuzebilir, Ucabilir`).

# abs-3 | Temel
## Soru
Java 8 ile gelen arayüz `default` metotlarının başlıca amacı nedir?
## A
Arayüzlerin örnek alan (instance field) tutabilmesini sağlamak
🤖 default metotlar gövde ekler ama arayüzler yine örnek alan tutamaz; yalnız static final sabitler olur.
## B
Arayüzlerin doğrudan `new` ile örneklenebilmesini sağlamak
🤖 Arayüzler default metotla da örneklenemez; yalnız somut sınıf/lambda üzerinden kullanılır.
## C
Mevcut bir arayüze, onu implemente eden sınıfları bozmadan yeni metot ekleyebilmek
🤖 Doğru: geriye dönük uyumluluk — eski sınıflar yeni metodu ezmek zorunda kalmaz.
## D
Arayüzlerde constructor tanımlayıp alanları başlangıç değerleriyle donatabilmek
🤖 Arayüzlerin constructor'ı yoktur; default metot bunu değiştirmez.
## Doğru
C
## Açıklama
`default` metotlar, arayüze **gövdeli bir varsayılan uygulama** koymayı sağlar. Asıl motivasyon **geriye dönük uyumluluktu (backward compatibility)**: Java 8, `Collection` gibi yaygın arayüzlere `stream()`, `forEach()` gibi metotlar eklemek istedi; bunları soyut (gövdesiz) eklemek, o arayüzü implemente eden **milyonlarca mevcut sınıfı** derlenemez hale getirirdi. `default` sayesinde yeni metot bir varsayılan davranışla gelir; eski sınıflar hiçbir değişiklik yapmadan çalışmaya devam eder, isteyen sınıf ezer. Arayüzler yine örnek alan tutamaz (A yanlış) ve örneklenemez (B yanlış).

# abs-4 | Orta
## Soru
Aşağıdaki kod **derlenmez**. Neden ve doğru çözüm nedir? (Diamond problem)

```java
interface A { default String selam() { return "A"; } }
interface B { default String selam() { return "B"; } }
class C implements A, B {
    // ...
}
```
## A
Java çoklu arayüz uygulamasına izin vermez; bir sınıf en fazla tek bir arayüz implemente edebilir
🤖 Çoklu implements serbesttir; sorun aynı imzalı iki default metodun çakışmasıdır.
## B
Arayüzlerden birinde `selam()`'ı `abstract` (gövdesiz) yapmak çakışmayı kendiliğinden çözer
🤖 Bu tek başına yetmez; `C` yine somut bir uygulama sağlamak zorunda kalır.
## C
Sorun yoktur; derleyici `implements` listesindeki ilk arayüzün (`A`) uygulamasını seçer
🤖 Böyle bir öncelik kuralı yoktur; Java belirsizliği reddedip programcıya açıkça çözdürür.
## D
`C`, çakışan `selam()`'ı açıkça ezmeli ve `A.super.selam()` ya da `B.super.selam()` ile niyetini belirtmelidir
🤖 Doğru: default metot elması, açık override + Arayuz.super çağrısıyla çözülür.
## Doğru
D
## Açıklama
Bu, default metotların yol açtığı **elmas problemidir (diamond problem)**: `C` hem `A` hem `B`'den **aynı imzalı** (`selam()`) default metot devralır ve Java hangisinin geçerli olacağını **kendiliğinden seçmez** — belirsizliği reddedip derleme hatası verir. Çözüm: `C` metodu **açıkça ezer** ve niyetini belirtir:

```java
class C implements A, B {
    @Override public String selam() { return A.super.selam(); } // veya B, veya ikisini birleştir
}
```

`Arayuz.super.metot()` sözdizimi, belirli bir üst arayüzün default uygulamasına erişmenin yoludur. Böylece Java, durum içermeyen arayüzlerde bile çakışmayı **programcıya açıkça çözdürerek** güvenli kalır. (Sınıf kalıtımında bu belirsizlik hiç oluşmaz çünkü tek üst sınıf vardır.)

# abs-5 | Orta
## Soru
"Constant interface" (yalnız sabit tutan arayüz) neden bir anti-pattern sayılır?
## A
Arayüzler sabit (constant) tutamaz; arayüz içinde alan tanımlamak derleme hatası verir
🤖 Teknik olarak tutabilir (örtük `public static final`); sorun teknik değil tasarımsaldır.
## B
Sabitler, implemente eden sınıfın public API'sine kalıtımla sızar; uygulama detayı olması gereken şey tipin parçası olur
🤖 Doğru: sabitler sözleşme değildir; arayüzle yayılması API kirliliği ve sızıntı yaratır.
## C
Arayüz üzerinden sabite erişim, sınıf üzerinden erişime göre ölçülebilir biçimde yavaştır
🤖 Sorun performans değil; derleyici sabitin değerini zaten çağrı yerine gömer.
## D
Arayüzde tanımlanan sabitler `final` olamaz; implemente eden sınıflar onları yeniden atayabilir
🤖 Arayüz alanları örtük olarak `public static final`dır; yeniden atanamazlar.
## Doğru
B
## Açıklama
Bir arayüzü **yalnız sabit (constant) grubu tutmak** için tanımlayıp sınıfların onu `implements` etmesi (sabitlere kısa adla erişmek için) **anti-pattern'dır**. Nedenleri: (1) Bir arayüz bir **sözleşmedir (contract)** — "ne yapabilir" der; sabitler ise **uygulama detayıdır**, sözleşme değil. (2) `implements` eden sınıf, bu sabitleri **kendi public API'sine kalıtımla taşır** ve alt sınıflara da yayar; nereden geldikleri kaybolur, isim çakışması riski doğar. (3) Sabitlerin nereden geldiği bir **uygulama detayı olmalıyken** tipe sızar. Doğru yaklaşımlar: sabitleri bir `enum`'da, ilgili sınıfın kendi `static final` alanlarında ya da yapıcısı gizli bir `final` yardımcı sınıfta tutmak; kullanırken `static import` ile kısaltmak.

# abs-6 | Orta
## Soru
Java 8+ arayüzlerinde `static` ve `private` metotların rolü için aşağıdakilerden hangisi doğrudur?
## A
`static` ve `private` metotların ikisi de arayüzün genel (public) sözleşmesinin parçasıdır
🤖 `private` tanımı gereği sözleşme dışıdır; `static` de kalıtımla dayatılan bir sözleşme üyesi değildir.
## B
`static` arayüz metodu implemente eden sınıfa devralınır ve orada override edilebilir
🤖 Arayüzün static metodu sınıfa devralınmaz; bu yüzden ne ezilebilir ne gizlenebilir, yalnız `Arayuz.metot()` ile çağrılır.
## C
`private` arayüz metodu, arayüzü implemente eden sınıfların içinden çağrılabilir
🤖 `private` metot yalnız arayüzün kendi içinde erişilebilir; implemente eden sınıfa bile kapalıdır.
## D
`static` metot tipe bağlı yardımcı işlev sunar; `private` metot (Java 9+) default'ların ortak iç mantığını saklar
🤖 Doğru: static = tip düzeyi yardımcı, private = default'ların paylaşılan iç yardımcısı.
## Doğru
D
## Açıklama
Java 8 arayüzlere **`static` metot** getirdi: tipe bağlı **yardımcı (utility) işlevler** — `Arayuz.metot()` ile çağrılır, implemente eden sınıflar bunları devralmaz veya ezemez (B yanlış); örn. `List.of(...)`, `Comparator.comparing(...)`. Java 9 ise **`private` (ve `private static`) metotları** ekledi: bunlar arayüzün **yalnız kendi içinde** görünür (C yanlış) ve amacı, birden çok `default`/`static` metodun paylaştığı **ortak iç mantığı kapsülleyip tekrarı önlemektir** — dışarıya sözleşme olarak sızmadan. Yani arayüz artık üç görünürlük katmanına sahiptir: public soyut/default (sözleşme), static (tip yardımcıları), private (iç detay).
