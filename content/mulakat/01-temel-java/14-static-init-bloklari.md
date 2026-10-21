---
slug: static-init-bloklari
baslik: "static Üyeler ve Başlatma (Initialization) Blokları"
kademe: Temel
sira: 14
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir okulu düşün. Her öğrencinin kendi adı, kendi numarası vardır — bunlar öğrenciye özeldir. Ama "okulun adı" tektir; kime sorarsan sor aynı cevabı alırsın. Java'da bu iki farklı sahiplik biçimini ayıran anahtar sözcük `static`'tir. Buna bir de "hazırlık" konusu eklenir: okul yılda bir kez açılış hazırlığı yapar, her öğrenci ise kaydolurken ayrı bir işlemden geçer. Java'nın başlatma (initialization) blokları tam bu iki hazırlığı ayırır; hangi kodun ömür boyu bir kez, hangisinin her nesnede tekrar çalışacağını sen belirlersin.

### Temel kavramlar

**Sınıf (class)** nesnelerin kalıbıdır; **nesne (instance)** `new` ile bu kalıptan üretilen somut örnektir.

**`static` üye** sınıfa aittir: sınıf başına **tek kopya** vardır ve tüm nesneler bu aynı kopyayı paylaşır — tipik örnek, üretilen nesneleri sayan ortak bir sayaç. Nesne yaratmadan `SınıfAdı.alan` biçiminde her yerden erişilir.

**instance üye** (`static` yazılmamış üye) `new` ile üretilen **her nesnede kendi kopyasıyla** yaşar; ona erişmek için önce bir nesne gerekir. Yani karar şudur: "kaç nesne olursa olsun ortak olması gereken veri" → `static`, "her nesneye özgü veri" → instance.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="static alan sınıfta tek kopya paylaşılır, instance alan her nesnede ayrı">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">static: tek kopya paylaşılır — instance: nesne başına kopya</text>
  <rect x="230" y="42" width="180" height="58" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Sinif (class)</text>
  <text x="320" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">static int sayac (1 kopya)</text>
  <rect x="40" y="146" width="170" height="52" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="125" y="168" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">nesne 1</text>
  <text x="125" y="187" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kendi int ad</text>
  <rect x="235" y="146" width="170" height="52" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="168" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">nesne 2</text>
  <text x="320" y="187" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kendi int ad</text>
  <rect x="430" y="146" width="170" height="52" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="515" y="168" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">nesne 3</text>
  <text x="515" y="187" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kendi int ad</text>
  <path d="M295 100 L130 146" stroke="#186399" stroke-width="1.5" marker-end="url(#si2Arrow)"/>
  <path d="M320 100 L320 146" stroke="#186399" stroke-width="1.5" marker-end="url(#si2Arrow)"/>
  <path d="M345 100 L510 146" stroke="#186399" stroke-width="1.5" marker-end="url(#si2Arrow)"/>
  <defs>
    <marker id="si2Arrow" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#186399"/>
    </marker>
  </defs>
</svg>
```

Üç tür hazırlık kodu vardır:

- **static başlatıcı**: `static` alan atamaları ve `static { ... }` blokları.
- **instance başlatıcı**: instance alan atamaları ve başında `static` olmayan `{ ... }` blokları.
- **constructor (kurucu)**: sınıfla aynı adı taşıyan, `new` sırasında çalışan özel blok.

### Nasıl çalışır?

Tek bir nesne üretildiğinde sıra sabittir:

1. **static başlatıcılar** — sınıf **ilk kez kullanıldığında** (sınıf ilklendirmesi / class initialization anında), metin sırasına göre, ömür boyu **yalnız BİR kez**.
2. **instance başlatıcılar** — **her nesne** için, metin sırasına göre.
3. **constructor gövdesi** — instance başlatıcılardan **sonra**.

```java
class A {
    static { System.out.print("S"); }   // static blok
    { System.out.print("I"); }          // instance blok
    A() { System.out.print("C"); }      // constructor gövdesi
}

new A();   // çıktı: SIC
```

Derleyici instance bloklarını constructor'ın en başına — ama `super(...)` çağrısından hemen sonrasına — taşır. Bu yüzden `I` her zaman `C`'den önce yazılır ve `ISC` ya da `SCI` gibi bir sıra mümkün değildir. Tasarımın nedeni açık: constructor gövdesi çalışmaya başladığında alanlar zaten kurulmuş olsun.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Başlatma sırası: static blok, instance blok, constructor">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Başlatma sırası (tek nesne)</text>
  <rect x="24" y="52" width="176" height="104" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">1. static blok</text>
  <text x="112" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sınıf yüklenince</text>
  <text x="112" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ömür boyu 1 kez</text>
  <rect x="232" y="52" width="176" height="104" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="320" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#6B3FA0" font-family="system-ui">2. instance blok</text>
  <text x="320" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">her nesne için</text>
  <text x="320" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">alan atamalarıyla</text>
  <rect x="440" y="52" width="176" height="104" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="528" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">3. constructor</text>
  <text x="528" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">gövdesi en son</text>
  <text x="528" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bloklardan sonra</text>
  <path d="M200 104 L232 104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#siArrow)"/>
  <path d="M408 104 L440 104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#siArrow)"/>
  <defs>
    <marker id="siArrow" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

İkinci bir nesne üretilirse `static` blok **tekrarlanmaz**: sınıf zaten yüklüdür. Yalnızca instance blok ve constructor yeniden koşar.

```java
new A();   // S I C  → sınıf yüklenir, sonra nesne kurulur
new A();   // I C    → sınıf zaten yüklü
// toplam çıktı: SICIC
```

Bu davranış, "masraflı hazırlık" (yapılandırma okuma, önbellek doldurma) için `static` bloğun neden uygun olduğunu da açıklar: iş bir kez yapılır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="static bir kez, instance blok ve constructor her new için tekrar çalışır">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">static 1 kez — instance blok + constructor her new'de</text>
  <rect x="30" y="44" width="180" height="52" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">sınıf yüklenir</text>
  <text x="120" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">S (yalnız 1 kez)</text>
  <rect x="240" y="44" width="170" height="52" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="325" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">new A() #1</text>
  <text x="325" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">I C</text>
  <rect x="440" y="44" width="170" height="52" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">new A() #2</text>
  <text x="525" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">I C</text>
  <path d="M210 70 L240 70" stroke="#5D7789" stroke-width="1.5" marker-end="url(#si3Arrow)"/>
  <path d="M410 70 L440 70" stroke="#5D7789" stroke-width="1.5" marker-end="url(#si3Arrow)"/>
  <rect x="175" y="132" width="290" height="46" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="153" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">toplam çıktı: S I C I C</text>
  <text x="320" y="170" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">S tekrarlanmaz; her new yalnız I C ekler</text>
  <defs>
    <marker id="si3Arrow" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Mülakatta ne sorulur?

**static metotlar override edilmez, GİZLENİR** (method hiding). Override (ezme) yalnızca instance metotlarda çalışır ve nesnenin çalışma-zamanı tipine göre geç bağlanır (dynamic dispatch). `static` metot çağrısı ise referansın **derleme-zamanındaki bildirilen tipine** göre çözülür:

```java
class Ust { static String kim() { return "Ust"; } }
class Alt extends Ust { static String kim() { return "Alt"; } }

Ust r = new Alt();
System.out.println(r.kim());   // "Ust" — nesne Alt olsa bile
```

Bu kod geçerlidir; gizleme yasal bir durumdur, derleme hatası değildir. Yanıltıcı olduğu için `static` metodu nesne referansı üzerinden değil, her zaman `SınıfAdı.metot()` biçiminde çağır.

**static import** (`import static java.lang.Math.*;`) bir sınıfın `static` üyelerini sınıf adı yazmadan kullanmayı sağlar — `Math.max(a, b)` yerine `max(a, b)`, `Math.PI` yerine `PI`. Nesne yaratmaz, kalıtım kurmaz, sınıfın niteliğini (`final` vb.) değiştirmez; yalnızca derleyicinin isim çözümlemesini etkiler. Aşırı kullanımı ismin hangi sınıftan geldiğini belirsizleştirir, bu yüzden `Math`, `Assertions` gibi bilinen yardımcılarla sınırlı tutulur.

**Utility (yardımcı) sınıf tasarımı**: yalnızca `static` metot barındıran, durum tutmayan sınıflar (`java.util.Collections`, `java.lang.Math`) hiç örneklenmemelidir. Sağlam desen: sınıfı **`final`** yap (kalıtım kapansın) ve **private bir constructor** ekle (hem `new` engellenir hem varsayılan public constructor'ın otomatik oluşması önlenir). `abstract` yapmak yanlıştır — alt sınıf türetip onu örneklemeye kapı bırakır. Public constructor bırakmak anlamsız örneklemeye izin verir. `interface` + `default` metot ise instance metot semantiği getirir, `static` yardımcı niyetine terstir.

Sık karşılaşılan diğer noktalar: `static` bir alana nesne yaratmadan erişilir, "yalnızca constructor'dan erişilir" gibi bir kısıt yoktur; `static` blok kaç nesne üretilirse üretilsin ikinci kez çalışmaz ama sınıf kullanıldığında en az bir kez çalışır.

### Tek cümlede

`static` üye sınıfa ait tek kopyadır ve static başlatıcısı ömür boyu bir kez koşar; instance başlatıcı ve constructor ise her `new` için sırayla tekrar çalışır.

# static-1 | Temel
## Soru
`static` bir alan (field) ile instance (nesne) alanı arasındaki temel fark nedir?
## A
`static` alan her nesnede ayrı kopya tutar, instance alan tek kopyadır
🤖 Tam tersi: `static` paylaşılan tek kopyadır, instance alan her nesnede ayrıdır.
## B
İkisi de aynı davranışı gösterir; `static` yalnızca okunabilirlik için eklenen bir kod stilidir
🤖 Anlamlı bir davranış farkı vardır: paylaşım ve erişim biçimi değişir.
## C
`static` alan sınıfa aittir, tüm nesneler tek kopyayı paylaşır; instance alan her nesnede ayrıdır
🤖 Doğru: `static` = sınıf-düzeyi tek kopya, instance = nesne başına kopya.
## D
`static` alanlara yalnızca constructor içinden, instance alanlara ise her yerden erişilebilir
🤖 Böyle bir kısıt yok: `static` alana nesne yaratmadan `Sınıf.alan` ile her yerden erişilir.
## Doğru
C
## Açıklama
`static` bir alan **sınıfa** aittir: sınıf başına tek kopya vardır ve tüm nesneler bu aynı kopyayı paylaşır (örneğin bir sayaç). Instance alan ise `new` ile üretilen **her nesnede** kendi kopyasıyla yaşar. `static` alana nesne yaratmadan `SınıfAdı.alan` ile erişilir; instance alana erişmek için önce bir nesne gerekir. Bu ayrım, "kaç nesne olursa olsun ortak olması gereken veri" (static) ile "her nesneye özgü veri" (instance) tasarım kararının temelidir.

# static-2 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
class A {
    static { System.out.print("S"); }
    { System.out.print("I"); }
    A() { System.out.print("C"); }
}
// main içinde:
new A();
```
## A
`SIC`
🤖 Doğru: static blok → instance blok → constructor sırasıyla çalışır.
## B
`CIS`
🤖 Ters sıra; başlatma static'ten başlar, constructor en sonda çalışır.
## C
`ISC`
🤖 static blok, instance bloğundan ÖNCE (sınıf yüklenince) çalışır.
## D
`SCI`
🤖 constructor gövdesi instance bloğundan SONRA çalışır, arada yer değişmez.
## Doğru
A
## Açıklama
Sıra sabittir: önce sınıf yüklendiğinde **static blok** çalışır (`S`), sonra nesne üretilirken **instance blok** (`I`), en son **constructor gövdesi** (`C`) → `SIC`. instance başlatıcılar her zaman constructor gövdesinden ÖNCE koşar; derleyicinin instance blokları constructor'ın en başına (ama `super(...)` çağrısından hemen sonrasına) taşıdığını düşünebilirsin. Bu yüzden `ISC` ve `SCI` yanlıştır.

# static-3 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
class A {
    static { System.out.print("S"); }
    { System.out.print("I"); }
    A() { System.out.print("C"); }
}
// main içinde:
new A();
new A();
```
## A
`SICSIC`
🤖 static blok her nesnede tekrar çalışsaydı doğru olurdu; ama static yalnız bir kez koşar.
## B
`SICIC`
🤖 Doğru: static bir kez (`S`), sonra her nesne için `IC` → `SIC` + `IC`.
## C
`SSICIC`
🤖 static blok sınıf başına bir kez çalışır; iki `S` olamaz.
## D
`ICIC`
🤖 static blok en az bir kez (sınıf yüklenince) çalışır; `S` eksik.
## Doğru
B
## Açıklama
**static başlatıcılar sınıf belleğe yüklenince yalnız BİR kez** çalışır — kaç nesne üretilirse üretilsin tekrarlanmaz. instance başlatıcı ve constructor ise **her `new` için** yeniden koşar. İlk `new A()` sınıfı yükler (`S`) ve nesneyi kurar (`IC`); ikinci `new A()` sınıf zaten yüklü olduğundan yalnızca `IC` üretir. Sonuç: `S` + `IC` + `IC` = `SICIC`. Bu, "static blok neden yapılandırma/önbellek ilklendirmesi için uygundur" sorusunun da cevabıdır: masraflı hazırlık bir kez yapılır.

# static-4 | Orta
## Soru
`import static java.lang.Math.*;` satırı ne sağlar?
## A
`Math` sınıfının tüm nesnelerini otomatik olarak yaratır
🤖 static import nesne yaratmaz; yalnızca isimlere niteliksiz erişim verir.
## B
`Math` sınıfını uygulamaya kalıtım (inheritance) yoluyla ekler
🤖 import ile kalıtım kurulmaz; `extends` ayrı bir kavramdır.
## C
`Math` sınıfının static üyelerine sınıf adı yazmadan (`max`, `PI` gibi) erişmeyi sağlar
🤖 Doğru: static import, static üyeleri niteliksiz kullanmayı sağlar.
## D
`Math` sınıfını `final` yaparak değiştirilmesini engeller
🤖 import bir sınıfın niteliklerini değiştirmez; sadece isim çözümlemesini etkiler.
## Doğru
C
## Açıklama
Normal `import` bir **tipi** (sınıf/arayüz) kısa adıyla kullanılabilir kılar. **static import** ise bir sınıfın `static` üyelerini (metot ve alanlar) **sınıf adı yazmadan** kullanmayı sağlar: `Math.max(a, b)` yerine `max(a, b)`, `Math.PI` yerine `PI`. Nesne yaratmaz (A), kalıtım kurmaz (B), sınıfın niteliğini değiştirmez (D) — yalnızca derleyicinin isim çözümlemesini etkiler. Aşırı kullanımı okunabilirliği bozabilir (hangi sınıftan geldiği belirsizleşir), bu yüzden `Math`, `Assertions` gibi bilinen yardımcılarla sınırlı tutulur.

# static-5 | İleri
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Ust {
    static String kim() { return "Ust"; }
}
class Alt extends Ust {
    static String kim() { return "Alt"; }
}
// main içinde:
Ust r = new Alt();
System.out.println(r.kim());
```
## A
`Alt`
🤖 static metotlar geç bağlanmaz (override edilmez); çalışma-zamanı tipine bakılmaz.
## B
`Ust`
🤖 Doğru: static metot çağrısı bildirilen (derleme-zamanı) tipe göre çözülür.
## C
Derleme hatası verir
🤖 Kod geçerli; static metot gizleme (hiding) yasal bir durumdur.
## D
`NullPointerException` fırlatır
🤖 `r` null değil; ayrıca static metot referans üzerinden değil tip üzerinden çözülür.
## Doğru
B
## Açıklama
static metotlar **override edilmez, GİZLENİR** (method hiding). Override (ezme) yalnızca instance metotlarda çalışır ve çalışma-zamanı tipine göre geç bağlanır (dynamic dispatch). static metot çağrısı ise **derleme-zamanındaki bildirilen tip** üzerinden çözülür: `r`'nin tipi `Ust` olduğu için `Ust.kim()` çağrılır → `"Ust"`. `r` bir `Alt` nesnesi olsa da fark etmez. Bu yüzden static metotları nesne referansı üzerinden (`r.kim()`) çağırmak yanıltıcıdır; her zaman `Ust.kim()` gibi sınıf adıyla çağırmak daha nettir.

# static-6 | İleri
## Soru
Yalnızca `static` yardımcı metotlar içeren bir utility (yardımcı) sınıfı için en doğru tasarım hangisidir?
## A
Sınıfı `abstract` yap; böylece hiçbir şekilde örneklenemez ve niyet açıkça belli olur
🤖 `abstract` yapmak alt sınıf türetip onu örneklemeye açık kapı bırakır; niyet utility için uygun değil.
## B
Sınıfı bir interface'e çevir ve tüm metotları `default` olarak tanımla; interface örneklenemez
🤖 interface örneklenemez ama `default` metotlar instance metottur; static utility niyetine ters.
## C
Sınıfı olduğu gibi bırak, public constructor kalsın; metotlar zaten `static` olduğu için sorun olmaz
🤖 Anlamsız örnekleme (new'lenme) mümkün kalır; utility niyetini zayıflatır.
## D
Sınıfı `final` yap ve private bir constructor ekle; kalıtım da örnekleme de kapansın
🤖 Doğru: `final` + private constructor, hem örneklemeyi hem alt sınıflamayı kapatır.
## Doğru
D
## Açıklama
Bir utility sınıfının **hiç örneklenmemesi** gerekir (durum tutmaz, yalnızca `static` metotlar sunar; örn. `java.util.Collections`, `java.lang.Math`). En sağlam desen: sınıfı `final` yap (kalıtımla genişletilmesin) ve **private bir constructor** ekle (dışarıdan `new` engellensin, ayrıca varsayılan public constructor'ın otomatik oluşması önlensin). `abstract` (A) yanlıştır çünkü alt sınıflamaya davet çıkarır. Public constructor (C) gereksiz örneklemeye izin verir. interface + `default` (B) ise instance metot semantiği getirir, static utility amacına ters düşer.
