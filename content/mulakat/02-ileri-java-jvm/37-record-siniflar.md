---
slug: record-siniflar
baslik: "Record Sınıflar: Değişmez Veri Taşıyıcılar"
kademe: Orta
sira: 37
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo etiketi düşün: üzerinde alıcı adı, adres ve ağırlık yazar. Etiket kendi başına bir iş yapmaz, yalnızca bilgi taşır; bir kez basıldıktan sonra üzerindeki yazı da değişmez. Programlarda da çoğu zaman tam olarak böyle "yalnızca veri tutan" nesnelere ihtiyaç duyarız. Klasik yolda bunun için alanları, kurucuyu, okuma metotlarını, karşılaştırma ve yazdırma metotlarını elle yazmak gerekir: dört bilgi taşımak için altmış satır tekrar. `record` bu tekrarı ortadan kaldırmak için vardır — sen hangi bilgilerin taşınacağını söylersin, kalan her şeyi derleyici yazar.

### Temel kavramlar

- **Veri taşıyıcı sınıf (data carrier):** davranış değil, bir arada duran birkaç değeri temsil eden sınıf. Bir noktanın `x` ve `y`'si, bir kullanıcının adı ve yaşı gibi.
- **Tekrar kod (boilerplate):** her seferinde aynı biçimde yazılan, düşünmeyi değil parmak emeğini isteyen kod.
- **Bileşen (component):** `record Nokta(int x, int y)` yazdığında parantez içindeki `x` ve `y`. Record'un taşıdığı her bilgi parçasına bileşen denir.
- **Değişmezlik (immutability):** nesne kurulduktan sonra durumunun bir daha değişmemesi. Record bileşenleri örtük olarak `private final` alanlardır: değer bir kez atanır, ikinci kez atanamaz.
- **Canonical constructor (asıl kurucu):** tüm bileşenleri sırayla parametre olarak alan kurucu. `record Nokta(int x, int y)` için `Nokta(int x, int y)`. Sen yazmazsan derleyici üretir.
- **Erişimci (accessor):** bileşenin değerini okuyan metot. Record'da adı `getX()` değil, bileşenin kendi adıdır: `x()`.
- **Değere dayalı eşitlik (value-based equality):** iki nesnenin "aynı yerde durduğu" için değil, "aynı değerleri taşıdığı" için eşit sayılması.

`record` Java 16'da kalıcılaştı (Java 14'te önizleme olarak gelmişti). En küçük hâli tek satırdır:

```java
record Nokta(int x, int y) {}

Nokta a = new Nokta(1, 2);
System.out.println(a.x());                      // 1
System.out.println(a);                          // Nokta[x=1, y=2]
System.out.println(a.equals(new Nokta(1, 2)));  // true
```

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="record derleyicinin ürettikleri">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">record Nokta(int x, int y) — derleyici ne üretir?</text>
  <rect x="30" y="46" width="270" height="170" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">OTOMATİK üretilir</text>
  <text x="165" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">canonical constructor</text>
  <text x="165" y="120" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">erişimci: x() , y()</text>
  <text x="165" y="142" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">equals() + hashCode()</text>
  <text x="165" y="164" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">toString()</text>
  <text x="165" y="192" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">alanlar: private final</text>
  <rect x="340" y="46" width="270" height="170" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#B4322B" font-family="system-ui">SINIR / yapılamaz</text>
  <text x="475" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">başka sınıftan extends YOK</text>
  <text x="475" y="120" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">bileşenler değiştirilemez</text>
  <text x="475" y="142" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">ek örnek alanı eklenemez</text>
  <text x="475" y="164" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">örtük final: alt sınıf YOK</text>
  <text x="475" y="192" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">arayüz (interface) implement EDEBİLİR</text>
</svg>
```

### Nasıl çalışır?

Derleyici bileşen listesini görür görmez dört şey üretir: (1) tüm bileşenleri alan **canonical constructor**, (2) her bileşen için bir **erişimci**, (3) değere dayalı **`equals` ve `hashCode`**, (4) okunur bir **`toString`**. Alanlar örtük `private final`'dır; setter **üretilmez**, çünkü record'un amacı durumun kurulumdan sonra sabit kalmasıdır.

Üretilen `equals` iki koşul arar: iki nesne **aynı record türünden** olacak VE **tüm bileşenleri karşılıklı eşit** olacak (nesne bileşenlerinde `Objects.equals`, ilkellerde `==`). Bu yüzden `new Nokta(1,2)` ile `new Nokta(1,2)` bellekte ayrı iki nesne olsa da (`==` false) `equals` ile eşittir. `hashCode` da bileşenlerden türetilir; aynı değerli iki record aynı kovaya (bucket) düşer. Record'ları güvenli `HashMap` anahtarı yapan tam olarak budur. Dikkat: eşit `hashCode` eşitliğin **gerek** şartıdır, **yeter** şartı değildir — çakışma (collision) olabileceği için `equals` yine de bileşenleri tek tek karşılaştırır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="record değere dayalı eşitlik ve HashMap anahtarı">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Değere dayalı equals → güvenli HashMap anahtarı</text>
  <rect x="40" y="50" width="200" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="140" y="78" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">new Nokta(1, 2)</text>
  <text x="140" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">a</text>
  <rect x="400" y="50" width="200" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="78" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">new Nokta(1, 2)</text>
  <text x="500" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">b (ayrı nesne, a != b)</text>
  <text x="320" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">a.equals(b)</text>
  <text x="320" y="96" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">= true</text>
  <rect x="140" y="140" width="360" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="164" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">aynı bileşen → aynı hashCode → aynı kova (bucket)</text>
  <text x="320" y="184" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">map.get(b) → a ile konan değeri bulur</text>
</svg>
```

Bileşenleri doğrulaman gerekiyorsa **compact constructor** kullanılır. Parametre listesi yazılmaz; içine yalnız doğrulama (`if (alt > ust) throw ...`) ve normalizasyon (`ad = ad.trim()`) yazılır. `this.alt = alt` gibi atamaları derleyici bloğun **sonuna** kendisi ekler:

```java
record Aralik(int alt, int ust) {
    Aralik {                       // compact constructor: parametre listesi YOK
        if (alt > ust) throw new IllegalArgumentException("alt > ust");
    }                              // this.alt = alt; this.ust = ust; otomatik
}
```

Doğrulamayı kurucuya koymanın anlamı şudur: geçersiz bir record **hiç oluşamaz**. Aynı işi ayrı bir `validate()` metoduna bırakırsan garanti kurulum anına bağlanmaz; `static` başlatma bloğuna koyarsan hiç çalışmaz, çünkü `static` blok sınıf yüklenirken bir kez çalışır ve örnek bileşenlerini göremez.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="canonical constructor ile compact constructor karşılaştırması">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Canonical vs Compact Constructor</text>
  <rect x="30" y="46" width="270" height="160" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Canonical (tam yaz)</text>
  <text x="165" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Aralik(int alt, int ust) {</text>
  <text x="165" y="118" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">this.alt = alt; // ELLE</text>
  <text x="165" y="138" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">this.ust = ust; // ELLE</text>
  <text x="165" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">}</text>
  <text x="165" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">parametre + atama tekrarı</text>
  <rect x="340" y="46" width="270" height="160" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Compact (kısa)</text>
  <text x="475" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Aralik {  // param listesi YOK</text>
  <text x="475" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">if (alt &gt; ust) throw ...</text>
  <text x="475" y="138" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">// atamalar OTOMATİK</text>
  <text x="475" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">}</text>
  <text x="475" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yalnız doğrulama/normalizasyon</text>
</svg>
```

Record'un asıl sınırı **kalıtımdır**: her record örtük olarak `java.lang.Record`'u genişletir; Java'da tekil kalıtım olduğu için başka bir sınıftan `extends` **yapamaz**, örtük `final` olduğu için alt sınıfı da **olamaz**. Buna karşılık arayüz (interface) **implement edebilir**, `static` alan/metot ve normal örnek metotları içerebilir, hatta jenerik olabilir: `record Cift<A, B>(A ilk, B ikinci)`. Yani record "veri + davranış" taşıyabilir; yalnız sınıf hiyerarşisine giremez.

Java 21'den itibaren record'lar **record pattern** ile parçalarına ayrılabilir (deconstruction). `if (o instanceof Nokta(int x, int y))` ya da `case Nokta(int x, int y) ->` yazıldığında bileşenler doğrudan yerel değişkenlere bağlanır. Erişimciyi ayrıca çağırmaya gerek kalmaz; reflection gibi dolaylı yollara ise hiç gerek yoktur. Desenler iç içe de geçer: `case Cizgi(Nokta(var x1, var y1), Nokta(var x2, var y2))`.

### Mülakatta ne sorulur?

**"Derleyici tam olarak ne üretir?"** — Canonical constructor, bileşen başına erişimci, `equals`, `hashCode`, `toString`. Alanlar `private final`; setter yok.

**"Doğrulamayı nereye yazarsın?"** — Compact constructor'a: parametre listesi yazılmaz, atamalar sona otomatik eklenir. `static` blok örnek doğrulaması yapamaz, `volatile` ise tamamen ayrı bir konudur (eşzamanlılıkta görünürlük).

**"İki record ne zaman eşittir?"** — Aynı tür + tüm bileşenler eşit. Sadece tür eşitliği yetmez; `==` (referans kimliği) ise başka bir şeydir; eşit `hashCode` tek başına eşitliği kanıtlamaz.

**"Record gerçekten değişmez mi?"** — Değişmezlik **sığdır (shallow)**. Bileşen referansı yeniden atanamaz, ama bileşen `List`, `Date` ya da dizi (`byte[]`) gibi değiştirilebilir bir tipse o nesnenin içi dışarıdan değiştirilebilir; çünkü record gelen referansı **kopyalamaz**. Derin değişmezlik için compact constructor'da **savunmacı kopya (defensive copy)** alınır: `Kayit { veri = List.copyOf(veri); }` ya da dizide `icerik = icerik.clone();`.

**"`record VeriKutusu(byte[] icerik)` neden tuzaktır?"** — Derleme hatası değildir; record her tipte bileşen alabilir. Ama üretilen `equals`/`hashCode` diziyi `Objects.equals` üzerinden karşılaştırır ve dizinin varsayılan `equals`'i **referans** temellidir: aynı içerikli iki `byte[]` **eşit sayılmaz** (içerik karşılaştırması için `Arrays.equals` gerekir). `toString` de `[B@1a2b3c` gibi anlamsız bir çıktı verir. Çözüm: defensive copy + `equals`/`hashCode`/`toString`'i elle geçersiz kılmak (override).

### Tek cümlede

`record`, yalnız veri taşıyan bir sınıfın kurucusunu, erişimcilerini ve `equals`/`hashCode`/`toString`'ini derleyiciye yazdıran; bileşenleri `final` olduğu için **sığ** değişmez, kalıtıma kapalı ama arayüze ve jeneriğe açık bir sınıf türüdür.

# rec-1 | Temel
## Soru
Bir `record` tanımladığında Java derleyicisi aşağıdakilerden hangisini SENİN İÇİN otomatik üretir?
## A
Canonical constructor, her bileşen için erişimci, `equals`, `hashCode` ve `toString`
🤖 Doğru: record'un varlık nedeni tam olarak bu tekrarlayan kodu ortadan kaldırmaktır.
## B
Yalnız argümansız (boş) bir constructor; erişimcileri ve `equals`/`hashCode`'u elle yazman gerekir
🤖 Erişimciler (`x()`, `y()`) ve üç `Object` metodu da otomatik üretilir; elle yazmak gerekmez.
## C
Setter metotları (`setX`, `setY`) ve bunlarla güncellenebilen değiştirilebilir (mutable) alanlar
🤖 record değişmezdir; setter üretilmez, bileşen alanları örtük olarak `private final`'dır.
## D
Hiçbir şey; `record` yalnız `class` yerine kullanılan bir söz dizimsel kısaltmadır, hiçbir üye üretmez
🤖 Tam tersine derleyici canonical constructor, erişimciler ve üç `Object` metodunu üretir.
## Doğru
A
## Açıklama
`record`, "sadece veri tutan sınıf" için tasarlanmış özel bir sınıf türüdür. Bileşen listesinden (`record Nokta(int x, int y)`) derleyici şunları **otomatik üretir**: (1) tüm bileşenleri alan **canonical constructor**, (2) her bileşen için bir **erişimci** (`x()`, `y()` — `getX` değil, bileşen adının kendisi), (3) değere dayalı **`equals`/`hashCode`**, (4) okunabilir **`toString`**. Alanlar örtük `private final`'dır (C yanlış: setter yok). B ve D, record'un ürettiği bu üyeleri yok saydığı için yanlıştır. Bu üretim, mülakatta "record neden var?" sorusunun özüdür: boilerplate azaltma + değişmezlik.

# rec-2 | Temel
## Soru
Bir `record`'un bileşenlerini doğrulamak (validation) için hangi yapı kullanılır?
## A
Ayrı bir `validate()` metodu yazıp her erişimde çağırmak
🤖 Gerekmez; record doğrulaması constructor anında bir kez yapılır, her erişimde değil.
## B
Compact constructor: parametre listesi olmadan yalnız doğrulama yazılır
🤖 Doğru: compact constructor tam olarak doğrulama/normalizasyon için vardır.
## C
`static` başlatma bloğu (static initializer) içinde kontrol yapmak
🤖 `static` blok sınıf yüklenirken bir kez çalışır; örnek bileşenlerini doğrulayamaz.
## D
Bileşenleri `volatile` işaretleyip runtime'da kontrol etmek
🤖 `volatile` eşzamanlılık görünürlüğüyle ilgilidir; record bileşen doğrulamasıyla ilgisi yoktur.
## Doğru
B
## Açıklama
**Compact constructor**, record'a özgü bir söz dizimidir: `Aralik { ... }` şeklinde parametre listesi YAZILMAZ. İçinde yalnız doğrulama (`if (alt > ust) throw ...`) ve normalizasyon (`name = name.trim()`) yapılır; `this.alt = alt` gibi atamaları derleyici bloğun SONUNA otomatik ekler. Bu, canonical constructor'ı tam yazmadan (ve tüm atamaları elle tekrarlamadan) iş mantığı eklemenin yoludur. `static` blok (C) örnek değil sınıf düzeyindedir; `volatile` (D) tamamen alakasızdır; ayrı `validate()` (A) değişmezlik garantisini kurulum anına bağlamaz. Doğrulamayı constructor'a koymak, geçersiz bir record'un HİÇ oluşamamasını sağlar.

# rec-3 | Orta
## Soru
İki `record` örneğinin `equals` ile eşit sayılması için koşul nedir?
## A
Aynı bellek referansına (identity) sahip olmaları
🤖 Bu `==` davranışıdır; record `equals`'i referansa değil bileşen değerlerine bakar.
## B
Aynı sınıftan olmaları yeterlidir, bileşen değerlerine bakılmaz
🤖 Değerlere bakılır; sadece tür eşitliği yetmez.
## C
Aynı tür olup TÜM bileşenlerinin karşılıklı eşit olması
🤖 Doğru: record'un ürettiği equals bileşen bileşen değer karşılaştırması yapar.
## D
`hashCode` değerlerinin eşit olması tek başına yeterlidir
🤖 Eşit hashCode eşitliği garanti etmez; equals ayrıca bileşenleri karşılaştırır.
## Doğru
C
## Açıklama
Record'un otomatik ürettiği `equals`, **değere dayalı (value-based)** eşitlik tanımlar: iki örnek ancak (1) aynı record türündense VE (2) tüm bileşenleri karşılıklı eşitse (`Objects.equals` ile, ilkellerde `==`) eşit sayılır. Bu, `==` referans karşılaştırmasından (A) farklıdır. Sadece tür eşitliği (B) yetmez; bileşen değerleri de eşleşmelidir. Eşit `hashCode` (D) eşitliğin GEREK şartıdır ama YETER şartı değildir — çakışma (collision) olabilir. Bu davranış record'ları güvenli `HashMap` anahtarı yapar: aynı değerli iki record aynı kovaya (bucket) düşer ve eşit bulunur.

# rec-4 | Orta
## Soru
`record` sınıfları hakkında aşağıdaki sınırlardan hangisi DOĞRUDUR?
## A
Hiçbir arayüz (interface) implement edemezler; `implements` yazmak derleme hatası verir
🤖 Yanlış; record arayüz implement edebilir, sadece sınıf extends edemez.
## B
İçlerinde `static` metot tanımlanamaz; yalnız bileşen erişimcileri bulunur
🤖 Yanlış; record'lar static metot, static alan ve normal örnek metotları içerebilir.
## C
Jenerik (generic) olamazlar; tip parametreli record tanımlanamaz
🤖 Yanlış; `record Cift<A, B>(A ilk, B ikinci)` gibi jenerik record yazılabilir.
## D
Başka bir sınıftan `extends` ile kalıtım alamazlar ve örtük olarak `final`'dırlar
🤖 Doğru: record örtük olarak java.lang.Record'u genişletir, başka sınıftan türeyemez ve alt sınıfı olamaz.
## Doğru
D
## Açıklama
Record'un temel sınırı **kalıtımdır**: her record örtük olarak `java.lang.Record`'u genişletir, bu yüzden başka bir sınıftan `extends` YAPAMAZ (Java tekil kalıtım). Ayrıca record'lar örtük `final`'dır — alt sınıfları olamaz. Bu, değişmezlik ve değere dayalı eşitlik garantilerini korur. Ancak record'lar arayüz **implement edebilir** (A yanlış), `static`/örnek metot içerebilir (B yanlış) ve jenerik olabilir (C yanlış). Yani record "veri + davranış" taşıyabilir; yalnız sınıf hiyerarşisine giremez. Bu sınırı bilmek, record'u ne zaman kullanıp ne zaman normal sınıfa döneceğini belirler.

# rec-5 | İleri
## Soru
Java 21'de bir `record`'u `switch` veya `instanceof` içinde parçalarına ayıran (deconstruction) yapı nedir?
## A
Refleksiyon (reflection) ile bileşen alanlarını tek tek okumak
🤖 Reflection gerekmez; record pattern derleme zamanında bileşenleri doğrudan bağlar.
## B
Record pattern: `case Nokta(int x, int y)` ile bileşenler doğrudan değişkenlere bağlanır
🤖 Doğru: record pattern bileşenleri yıkımlayıp (destructure) yerel değişkenlere bağlar.
## C
Her bileşen için ayrı `if` ve erişimci (`p.x()`, `p.y()`) çağrısı — başka yol yoktur
🤖 Erişimci çağrısı hâlâ mümkün ama record pattern bunu tek adımda ve tip kontrollü yapar.
## D
`record` bileşenleri yalnız `toString` çıktısını ayrıştırarak elde edilir
🤖 Metin ayrıştırma kırılgan bir hiledir; dilin sağladığı record pattern varken kullanılmaz.
## Doğru
B
## Açıklama
**Record pattern** (Java 21), bir record'u eşleştirirken bileşenlerini tek adımda yerel değişkenlere **yıkımlar (deconstruct)**: `if (o instanceof Nokta(int x, int y))` ya da `case Nokta(int x, int y) -> ...`. Burada `x` ve `y` doğrudan kullanılabilir; ayrı erişimci çağrısı gerekmez. Pattern'ler **iç içe** de geçebilir: `case Cizgi(Nokta(var x1, var y1), Nokta(var x2, var y2))`. Bu, tip kontrolü + döküm (cast) + bileşen çıkarımını tek, güvenli ifadede birleştirir. Reflection (A) ve toString ayrıştırma (D) kırılgan alternatiflerdir; erişimciyle elle okuma (C) çalışır ama record pattern daha okunur ve exhaustiveness denetimine uygundur.

# rec-6 | İleri
## Soru
`record VeriKutusu(byte[] icerik)` gibi bir dizi (array) bileşeni tutan record'da hangi tuzak vardır?
## A
Üretilen `equals`/`hashCode` diziyi içeriğe değil referansa göre karşılaştırır; aynı içerikli iki dizi eşit sayılmaz
🤖 Doğru: dizinin kendi equals/hashCode'u referans temellidir, record da onu çağırır; içerik karşılaştırılmaz.
## B
Dizi bileşeni derleme hatası verir; record yalnız ilkel tipleri ve `String` tutabilir, referans tipi olan diziyi tutamaz
🤖 Yanlış; record her tipte bileşen tutabilir, dizi dahil; sorun derlemede değil eşitlik semantiğindedir.
## C
Derleyici dizi bileşenini canonical constructor'da otomatik klonlar, bu yüzden dışarıdan değiştirilemez ve hiçbir tuzak kalmaz
🤖 Yanlış; record dizi bileşenini KOPYALAMAZ, gelen referansı paylaşır; içerik dışarıdan değiştirilebilir.
## D
Üretilen `toString` dizinin tüm elemanlarını `[1, 2, 3]` biçiminde okunur olarak yazdırır
🤖 Yanlış; dizinin varsayılan toString'i `[B@1a2b3c` gibi anlamsız bir referans dizesi yazar.
## Doğru
A
## Açıklama
Dizi bileşenleri record'un iki üstü kapalı garantisini bozar. Birincisi **eşitlik**: üretilen `equals`, dizi bileşenini `Objects.equals(a, b)` ile karşılaştırır — bu da dizinin varsayılan `equals`'ini (referans kimliği) kullanır. Sonuç: aynı içerikli iki `byte[]` EŞİT SAYILMAZ (A doğru); içerik karşılaştırması için `Arrays.equals` gerekir. `toString` de diziyi anlamsız `[B@...` biçiminde yazar (D bu yüzden yanlış). İkincisi **değişmezlik sızıntısı**: record bileşeni kopyalamaz, aynı dizi referansını tutar; çağıran dizinin içeriğini sonradan değiştirebilir (C yanlış). Dizi bileşeni tanımlamak derleme hatası DEĞİLDİR — record her tipte bileşen alabilir (B yanlış). Çözüm: canonical/compact constructor'da defensive copy (`icerik.clone()`) yapmak ve gerekiyorsa `equals`/`hashCode`/`toString`'i elle geçersiz kılmak (override). Bu, "record ile dizi" mülakat tuzağının tam cevabıdır.
