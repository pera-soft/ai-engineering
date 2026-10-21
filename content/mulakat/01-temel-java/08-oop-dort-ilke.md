---
slug: oop-dort-ilke
baslik: "OOP'nin Dört İlkesi: Kapsülleme, Kalıtım, Polimorfizm, Soyutlama"
kademe: Temel
sira: 8
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir şehir düşün: evler, dükkânlar, arabalar. Her biri kendi içinde bir "şey"dir; kendi bilgisi (arabanın yakıtı) ve kendi yapabildikleri (gider, durur) vardır. Nesne yönelimli programlama (object-oriented programming, OOP) da programı böyle "şey"lere böler. Çözdüğü problem şudur: program büyüdükçe her şeyin birbirine karıştığı tek bir dev talimat listesi bakılamaz hale gelir. OOP kodu, kendi verisini kendi koruyan ve kendi işini kendi yapan parçalara ayırır; böylece bir parçayı değiştirmek diğerlerini bozmaz. Aşağıdaki dört ilke bu ayırmanın kurallarıdır.

### Temel kavramlar

Önce üç kelime, çünkü geri kalanı bunların üstüne kurulur. **Sınıf (class)** bir şeyin kalıbı/tarifidir ("Araba" tarifi). **Nesne (object)** o kalıptan üretilmiş somut örnektir (senin garajındaki araba). Nesnenin verisine **alan (field)**, yapabildiği işe **metot (method)** denir.

**Kapsülleme (encapsulation)**: Veriyi (alanları) ve o veriyi işleyen davranışı (metotları) tek bir sınıfta toplamak; iç durumu `private` yapıp dışarıya yalnız kontrollü bir arayüz (getter/setter veya iş metotları) sunmak. `private` "yalnız bu sınıfın içinden erişilebilir" demektir. Amaç sınıfın kendi **değişmezini (invariant)** — her zaman doğru kalması gereken kuralını — korumaktır: bir banka hesabının bakiyesi asla negatif olamaz.

```java
class BankaHesabi {
    private double bakiye;                 // dışarıya kapalı
    void paraCek(double tutar) {
        if (tutar > bakiye) throw new IllegalArgumentException("yetersiz bakiye");
        bakiye -= tutar;                   // kural burada korunur
    }
}
```

Dışarıdan kimse `hesap.bakiye = -100;` yazamaz; alan `private` olduğu için tek yol `paraCek`'tir.

**Soyutlama (abstraction)**: Karmaşık gerçekliği **basitleştirilmiş bir modele** indirgemek; "ne yapıldığını" gösterip "nasıl yapıldığını" gizlemek. Direksiyon gibi: çevirirsin, döner; içerideki mekanizmayı bilmene gerek yoktur. Java'da `interface List` "ekle/çıkar/eriş" der; arkasında `ArrayList` mi `LinkedList` mi olduğunu, dizinin nasıl büyüdüğünü bilmezsin. Soyutlama tipik olarak `interface` ve `abstract` sınıflarla ifade edilir.

**Kalıtım (inheritance)**: Bir sınıfın başka bir sınıfın alan ve davranışını devralması. İlişki "**is-a**"dır (bir ...dır): `Kopek extends Hayvan` → köpek **bir** hayvandır.

**Polimorfizm (polymorphism)**: Aynı çağrının, çalışma zamanındaki gerçek nesne tipine göre farklı davranması. `hayvan.sesCikar()` köpekte havlama, kedide miyavlama üretir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="OOP dört ilke">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">OOP'nin dört ilkesi</text>
  <rect x="24" y="44" width="290" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Kapsülleme (encapsulation)</text>
  <text x="169" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">veri + davranış bir arada, iç durum private</text>
  <rect x="326" y="44" width="290" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Soyutlama (abstraction)</text>
  <text x="471" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">"ne" görünür, "nasıl" gizli</text>
  <rect x="24" y="124" width="290" height="70" rx="10" fill="#FBF0E6" stroke="#B4690E" stroke-width="1.5"/>
  <text x="169" y="150" text-anchor="middle" font-size="13" font-weight="700" fill="#B4690E" font-family="system-ui">Kalıtım (inheritance)</text>
  <text x="169" y="172" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">"is-a": Kopek bir Hayvandir</text>
  <rect x="326" y="124" width="290" height="70" rx="10" fill="#F3ECF7" stroke="#7A3E9D" stroke-width="1.5"/>
  <text x="471" y="150" text-anchor="middle" font-size="13" font-weight="700" fill="#7A3E9D" font-family="system-ui">Polimorfizm (polymorphism)</text>
  <text x="471" y="172" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">aynı çağrı, tipe göre farklı davranış</text>
</svg>
```

Kapsülleme ile soyutlama sık karıştırılır, çünkü ikisi de "gizler". Fark şudur: **kapsülleme veriyi/erişimi gizler** ("nasıl korunur?"), **soyutlama karmaşıklığı/uygulamayı gizler** ("ne gösterilir?").

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kapsulleme vs soyutlama">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki "gizleme" karıştırılır: kapsülleme ≠ soyutlama</text>
  <rect x="24" y="44" width="290" height="116" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Kapsülleme</text>
  <text x="169" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">VERİYİ / erişimi gizler</text>
  <text x="169" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">private alan + kontrollü metot</text>
  <text x="169" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">soru: "nasıl korunur?"</text>
  <rect x="326" y="44" width="290" height="116" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Soyutlama</text>
  <text x="471" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">KARMAŞIKLIĞI / uygulamayı gizler</text>
  <text x="471" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">interface / abstract ile model</text>
  <text x="471" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">soru: "ne gösterilir?"</text>
</svg>
```

### Nasıl çalışır?

Polimorfizmi adım adım görelim:

```java
class Hayvan { String ses() { return "..."; } }
class Kedi extends Hayvan { String ses() { return "miyav"; } }
class Kopek extends Hayvan { String ses() { return "hav"; } }

Hayvan[] hayvanlar = { new Kedi(), new Kopek(), new Hayvan() };
for (Hayvan h : hayvanlar) System.out.print(h.ses() + " ");   // miyav hav ...
```

1. `Kedi` bir `Hayvan` olduğu için (is-a) `Hayvan[]` dizisine konabilir; bu geçerli bir polimorfik atamadır, derleme hatası değildir.
2. Döngüdeki `h`'nin **bildirilen (derleme-zamanı) tipi** `Hayvan`'dır; bu yalnız hangi metotların çağrılabileceğini belirler.
3. Çalışırken JVM nesnenin **gerçek (çalışma-zamanı) tipine** bakar ve o tipin metodunu çalıştırır. Buna **dinamik dağıtım (dynamic dispatch)** denir.
4. Çıktı `miyav hav ...` olur; üçüncü eleman düz bir `Hayvan` olduğundan kendi `ses()`'i çalışır.

Değeri şudur: yarın bir `Kus` sınıfı eklersen döngü hiç değişmeden çalışır.

Kalıtımın alternatifi **bileşimdir (composition)**: sınıf, başka bir nesneyi **üye alan** olarak tutar ve işi ona devreder (delegasyon). İlişki "**has-a**"dır (bir ...'ı vardır): `Araba` bir `Motor`'a *sahiptir*, ama bir motor *değildir*.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="is-a vs has-a">
  <defs>
    <marker id="oop-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"is-a" (kalıtım) vs "has-a" (bileşim)</text>
  <text x="150" y="48" text-anchor="middle" font-size="12" font-weight="700" fill="#B4690E" font-family="system-ui">is-a → extends</text>
  <rect x="80" y="58" width="140" height="40" rx="8" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="150" y="83" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Hayvan</text>
  <rect x="80" y="150" width="140" height="40" rx="8" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="150" y="175" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Kopek</text>
  <path d="M150,150 L150,100" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#oop-ok)"/>
  <text x="150" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bir Hayvandır</text>
  <text x="490" y="48" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">has-a → alan (field)</text>
  <rect x="420" y="58" width="140" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="490" y="83" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Araba</text>
  <rect x="420" y="150" width="140" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="490" y="175" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Motor</text>
  <path d="M490,98 L490,148" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#oop-ok)"/>
  <text x="490" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bir Motora sahiptir</text>
</svg>
```

Doğru soruyu sormak tasarımı belirler: "X bir Y midir?" evetse kalıtım; "X'in bir Y'si var mı?" evetse bileşim. Arabayı motordan türetmek mantıksız ve kırılgan hiyerarşiler üretir.

### Mülakatta ne sorulur?

- **"Composition over inheritance" (kalıtım yerine bileşim) neden?** Gerekçe performans **değildir**; delegasyon çağrı sayısını azaltmaz. Kalıtım alt sınıfı üst sınıfın uygulamasına sıkıca bağlar (**kırılgan taban sınıf / fragile base class**: üst sınıf değişince alt sınıflar sessizce bozulabilir) ve ilişkiyi derleme zamanında sabitler. Bileşim gevşek bağlıdır; sarmalanan nesne çalışma zamanında değiştirilebilir (örn. bir strateji nesnesini takas etmek). İlke kalıtımı **yasaklamaz** — gerçek "is-a" için kalıtım hâlâ doğrudur; yalnız varsayılan tercihi bileşimden yana koyar. Bileşim kapsüllemeyi de **güçlendirir**: sarmalanan nesne `private` alanda durur, yalnız gereken iş dışarı açılır. Java kütüphanesi bunu bolca kullanır: `Collections.unmodifiableList` sarmalar, türetmez.
- **Sırf kod paylaşmak için kalıtım.** İlgisiz iki sınıfı ortak metotları paylaşsınlar diye `extends` ile bağlamak kod sorunsuz **derlenir** — derleyici "is-a" anlamlılığını denetlemez; sorun tasarımsaldır. Sahte "is-a" ilişkisi **Liskov Yerine Koyma İlkesini (Liskov Substitution Principle, LSP)** ihlal eder. Bu ilke şudur: alt tip, üst tipin beklendiği her yerde sorunsuz kullanılabilmelidir. İlke bozulunca polimorfik kullanımda sözleşme (contract) tutmaz ve kod beklenmedik biçimde bozulur. Devralınan alanlar **bellek sızıntısı yaratmaz** (nesne erişilemez olunca çöp toplayıcı/GC toplar); sorun sızıntı değil, kırılgan ve yanlış hiyerarşidir. Doğru çözüm bileşim (ortak davranışı ayrı sınıfa alıp delege etmek) ya da bir yardımcı (utility) sınıftır.
- **Statik çözülme yanılgısı.** "Çağrı bildirilen tipe göre çözülür" yalnız `static` metotlar ve **alanlar** için doğrudur; örnek (instance) metotları çalışma-zamanı tipine göre çözülür.
- **Soyutlamayla karıştırılan yanlış tanımlar**: nesnenin kopyasını bellekte çoğaltmak (bu klonlamadır), tüm alanları `public` yapmak (bu kapsüllemenin tam tersidir), alan/metot devralmak (bu kalıtımdır).
- **Kapsülleme mi soyutlama mı?** `private` alan + kontrollü metot sorusu geldiğinde cevap kapsüllemedir; "arayüz sunup uygulamayı gizlemek" sorusunda cevap soyutlamadır.

### Tek cümlede

OOP'nin dört ilkesi, veriyi koruyarak (kapsülleme), karmaşıklığı gizleyerek (soyutlama), gerçek "is-a" ilişkilerini devralarak (kalıtım) ve aynı çağrıyı tipe göre farklı çalıştırarak (polimorfizm) büyük bir programı birbirini bozmayan parçalara ayırma disiplinidir.

# oop-1 | Temel
## Soru
Bir `BankaHesabi` sınıfında `bakiye` alanını `private` yapıp para çekmeyi yalnız `paraCek(tutar)` metodundan izin vermek, hangi OOP ilkesinin uygulanmasıdır?
## A
Kapsülleme (encapsulation)
🤖 Doğru: veriyi gizleyip erişimi kontrollü metotla sağlamak tam olarak kapsüllemedir.
## B
Kalıtım (inheritance)
🤖 Kalıtım bir sınıfın diğerinden türemesidir; burada türeme yok, sadece erişim kontrolü var.
## C
Polimorfizm (polymorphism)
🤖 Polimorfizm aynı çağrının tipe göre farklı davranmasıdır; burada tek bir davranış var.
## D
Soyutlama (abstraction)
🤖 Yakın ama değil: soyutlama karmaşıklığı basit bir modele indirger; buradaki asıl amaç iç veriyi korumaktır.
## Doğru
A
## Açıklama
`bakiye`'yi `private` yapıp değişimini yalnız `paraCek`/`paraYatir` gibi metotlara bırakmak **kapsüllemedir**: veri ve onu işleyen davranış tek sınıfta toplanır, iç durum dışarıya kapatılır. Böylece sınıf kendi **değişmezini (invariant)** koruyabilir — örn. bakiye negatif olamaz kontrolünü `paraCek` içinde yaparsın; dışarıdan kimse `hesap.bakiye = -100` yapamaz. Soyutlama (D) çeldiricidir çünkü ikisi de "gizleme" içerir; fark şudur: kapsülleme **veriyi/erişimi** gizler, soyutlama **karmaşıklığı/uygulamayı** gizler.

# oop-2 | Temel
## Soru
"is-a" ve "has-a" ilişkileri aşağıdakilerden hangisinde doğru eşleştirilmiştir?
## A
`Araba extends Motor` (is-a); `Kopek` bir `Hayvan` içerir (has-a)
🤖 Ters: araba bir motor DEĞİLDİR (motora sahiptir); köpek bir hayvana sahip değil, bir hayvandır.
## B
İkisi de kalıtımla ("is-a") modellenir
🤖 has-a ilişkisi kalıtımla değil, üye alan (field) olarak, yani composition ile modellenir.
## C
İkisi de üye alanla ("has-a") modellenir
🤖 "Köpek bir hayvandır" gerçek bir tür ilişkisidir; bunu alan olarak değil kalıtımla modelleriz.
## D
`Araba` bir `Motor` içerir (has-a); `Kopek extends Hayvan` (is-a)
🤖 Doğru: araba motora sahiptir (composition), köpek bir hayvandır (inheritance).
## Doğru
D
## Açıklama
**"is-a"** bir tür/alt-tür ilişkisidir ve **kalıtımla (inheritance)** modellenir: `Kopek extends Hayvan` çünkü köpek *bir* hayvandır — her yerde bir `Hayvan` beklenirken `Kopek` verilebilir. **"has-a"** bir sahiplik/parça ilişkisidir ve **composition** (üye alan) ile modellenir: `Araba` bir `Motor`'a *sahiptir*, ama araba bir motor DEĞİLDİR. Doğru soruyu sormak tasarımı belirler: "X bir Y midir?" evetse kalıtım, "X'in bir Y'si var mı?" evetse composition. Yanlış seçim (arabayı motordan türetmek) mantıksız ve kırılgan hiyerarşiler üretir.

# oop-3 | Temel
## Soru
Soyutlama (abstraction) ilkesinin özü aşağıdakilerden hangisidir?
## A
Karmaşık gerçekliği, yalnız gerekli davranışı sunan basitleştirilmiş bir modele indirgemek
🤖 Doğru: "ne yapıldığını" göster, "nasıl" ayrıntısını gizle — soyutlamanın tanımı budur.
## B
Bir nesnenin kopyasını bellekte çoğaltmak
🤖 Bu klonlama/kopyalamayla ilgilidir, OOP ilkesi olan soyutlamayla değil.
## C
Nesnenin tüm iç alanlarını `public` yaparak dışarıya tam erişim vermek
🤖 Bu kapsüllemenin TAM TERSİDİR; soyutlama iç ayrıntıyı gizler, sergilemez.
## D
Bir sınıfın başka bir sınıftan alan ve metot devralması
🤖 Bu kalıtımın (inheritance) tanımıdır, soyutlamanın değil.
## Doğru
A
## Açıklama
**Soyutlama**, karmaşık bir sistemi yalnız **ilgili davranışları** açığa çıkaran basit bir arayüze indirger; "nasıl yapıldığı" gizlenir. `List` arayüzü `add`/`get`/`remove` der; arkasında `ArrayList` mı yoksa `LinkedList` mi olduğunu, dizinin nasıl büyüdüğünü bilmene gerek yoktur. Java'da soyutlama tipik olarak `interface` ve `abstract` sınıflarla ifade edilir. Çeldirici D (kalıtım) ve C (kapsüllemenin tersi) klasik karışıklıklardır: soyutlama *modelleme/karmaşıklık gizleme*, kapsülleme *veri/erişim gizleme*, kalıtım *tür türetme* ile ilgilidir.

# oop-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Hayvan { String ses() { return "..."; } }
class Kedi extends Hayvan { String ses() { return "miyav"; } }
class Kopek extends Hayvan { String ses() { return "hav"; } }

Hayvan[] hayvanlar = { new Kedi(), new Kopek(), new Hayvan() };
for (Hayvan h : hayvanlar) System.out.print(h.ses() + " ");
```
## A
`... ... ...`
🤖 Bu, çağrının derleme-zamanı tipine (Hayvan) göre çözüldüğünü varsayar; Java'da metotlar çalışma-zamanı tipine göre çözülür.
## B
Derleme hatası; `Hayvan[]` içine `Kedi` konamaz
🤖 Kedi bir Hayvan olduğundan (is-a) diziye konabilir; bu geçerli polimorfik atamadır.
## C
`miyav hav ...`
🤖 Doğru: her nesnenin gerçek tipi çağrılan metodu belirler (dinamik dispatch).
## D
`miyav hav miyav`
🤖 Üçüncü eleman düz bir Hayvan'dır, onun kendi ses() metodu "..." döndürür.
## Doğru
C
## Açıklama
Bu **çalışma-zamanı polimorfizmidir (dynamic dispatch)**. `hayvanlar` dizisinin bildirilen tipi `Hayvan` olsa da, `h.ses()` çağrısı her elemanın **gerçek (runtime) tipine** göre çözülür: `Kedi` → "miyav", `Kopek` → "hav", düz `Hayvan` → "...". Çıktı: `miyav hav ...`. Bu, polimorfizmin gücüdür: aynı çağrı kodu (`h.ses()`), farklı nesne tipleriyle farklı davranır ve yeni bir alt sınıf eklendiğinde döngü değişmeden çalışır. Çeldirici A, metodun derleme-zamanı tipine göre statik çözüldüğü yanılgısıdır — bu Java'da yalnız `static` metotlar ve alanlar için geçerlidir, örnek (instance) metotları için değil.

# oop-5 | Orta
## Soru
"Composition over inheritance" (kalıtım yerine bileşim) ilkesinin temel gerekçesi nedir?
## A
Bileşim, delegasyon sayesinde sanal metot çağrısını ortadan kaldırdığı için kalıtımdan hızlı çalışır
🤖 Gerekçe performans değildir; üstelik delegasyon çağrıyı azaltmaz. Fark tasarımın esnekliğindedir.
## B
Kalıtım alt sınıfı üst sınıfın uygulamasına sıkıca bağlar; bileşim davranışı gevşek bağlı ve çalışma zamanında değiştirilebilir kurar
🤖 Doğru: bileşim gevşek bağlı ve değiştirilebilir; kalıtım üst sınıfa sıkıca bağlar.
## C
Java çoklu kalıtımı desteklemediğinden kalıtım artık kullanılmamalı, her hiyerarşi bileşimle kurulmalıdır
🤖 Kalıtım pekâlâ kullanılır; ilke onu yasaklamaz, yalnız varsayılan tercihi bileşimden yana koyar.
## D
Bileşim, sarmalanan nesnenin alanlarına doğrudan erişim verdiği için kodu kısaltır ve kapsüllemeyi gevşetir
🤖 Tersine; bileşim sarmalanan nesneyi private alan olarak tutup yalnız gereken işi dışarı açar, kapsüllemeyi güçlendirir.
## Doğru
B
## Açıklama
**Kalıtım güçlü ama katı bir araçtır**: alt sınıf üst sınıfın uygulamasına sıkıca bağlanır (fragile base class problem — üst sınıf değişince alt sınıflar sessizce bozulabilir) ve ilişki derleme zamanında sabitlenir. **Bileşim (composition)** ise davranışı başka nesneleri **üye alan** olarak tutup onlara delege ederek kurar; bu ilişki gevşek bağlıdır, çalışma zamanında değiştirilebilir (örn. bir strateji nesnesini takas etmek) ve "is-a" olmadığında daha doğru modeldir. İlke kalıtımı yasaklamaz — gerçek "is-a" ilişkileri için kalıtım hâlâ doğrudur; sadece **varsayılan tercihi bileşimden yana** koyar. Java standart kütüphanesi bunu bolca kullanır (örn. `Collections.unmodifiableList` sarmalar, türetmez).

# oop-6 | İleri
## Soru
Bir ekip, kod tekrarını azaltmak için ilgisiz iki sınıfın ortak birkaç metodunu paylaşmak amacıyla birini diğerinden `extends` ile türetiyor (aralarında gerçek bir "is-a" ilişkisi yok). Bu yaklaşımın en olası uzun vadeli sorunu nedir?
## A
Kod derlenmez; derleyici iki sınıf arasında anlamlı bir "is-a" ilişkisi bulunmadığını görüp hata verir
🤖 Java derleyicisi "is-a" anlamlılığını denetlemez; kod sorunsuz derlenir, sorun tasarımsaldır.
## B
Sahte bir "is-a" kurulur; Liskov Yerine Koyma İlkesi (LSP) ihlal edilir ve kırılgan, yanlış bir hiyerarşi oluşur
🤖 Doğru: sahte "is-a", polimorfik kullanımda beklenmedik davranışa ve sıkı bağlılığa yol açar.
## C
Alt sınıf üst sınıfın alanlarını da taşıdığı için nesneler serbest bırakılamaz ve bellek sızıntısı (memory leak) oluşur
🤖 Devralınan alanlar sızıntı yaratmaz; nesne erişilemez olunca çöp toplayıcı (GC) onu toplar.
## D
Hiçbir sorun yoktur; kod tekrarını önlemenin en iyi yolu her zaman kalıtımdır
🤖 Kod paylaşımı tek başına kalıtım gerekçesi değildir; bileşim ya da yardımcı sınıf daha doğrudur.
## Doğru
B
## Açıklama
Kalıtım yalnız kod paylaşımı için değil, **gerçek bir "is-a" ilişkisi** olduğunda kullanılmalıdır. İlgisiz sınıfları sırf ortak metotları paylaşmak için türetmek sahte bir hiyerarşi kurar ve **Liskov Yerine Koyma İlkesini (Liskov Substitution Principle, LSP)** ihlal etme riskini doğurur: üst tip beklenen bir yerde alt tip konduğunda sözleşme (contract) tutmaz, kod beklenmedik biçimde bozulur. Ayrıca alt sınıf üst sınıfa sıkıca bağlanır; üst sınıftaki değişiklik alt sınıfı sessizce etkiler. Kod tekrarını önlemenin doğru yolu burada **bileşim** (ortak davranışı ayrı bir sınıfa alıp delege etmek) veya bir yardımcı/utility sınıftır. "Kod paylaşmak için kalıtım" en yaygın OOP tasarım kokularından biridir.
