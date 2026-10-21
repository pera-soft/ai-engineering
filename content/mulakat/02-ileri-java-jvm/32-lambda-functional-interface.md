---
slug: lambda-functional-interface
baslik: "Lambda ve Fonksiyonel Arayüz: Yakalama, this ve Yerleşik Tipler"
kademe: Orta
sira: 32
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir arkadaşına "şu paketi kargoya ver" diye not bırakırsın; notta işi yapacak kişinin özgeçmişi değil, yalnız YAPILACAK İŞ yazar. Java 8 öncesinde durum tersiydi: tek satırlık bir davranışı bir metoda geçirmek için koca bir sınıf yazman gerekirdi. Lambda ifadesi tam bu şişkinliği kaldırır: davranışın kendisini, tek satırda, bir değermiş gibi taşırsın. Sonuç sadece kısalık değildir — "ne yapılacağını" veri gibi dolaştırabilmek, Stream ve `Optional` API'lerini mümkün kılan şeydir.

### Temel kavramlar

**Arayüz (interface):** Sınıfların uyacağı sözleşme. İçinde gövdesiz metot imzaları bulunur.

**Soyut metot (abstract method):** Gövdesi OLMAYAN metot; onu uygulayan sınıf gövdeyi yazmak zorundadır.

**`default` ve `static` metot:** Java 8'den beri arayüz içinde GÖVDELİ metot da yazılabilir. `default` metot uygulayan sınıflara hazır gövde miras verir, `static` metot ise arayüzün kendisine aittir.

**Fonksiyonel arayüz (functional interface):** Tam olarak BİR soyut metodu olan arayüz. Buna SAM (Single Abstract Method) da denir. Gövdeli oldukları için `default` ve `static` metotlar bu sayıya GİRMEZ; `Object`'ten miras gelen imzalar (`equals`, `hashCode`, `toString`) da sayılmaz. Bu yüzden `Comparator` hem tek soyut metotludur hem de bir sürü `default`/`static` metot taşır.

**`@FunctionalInterface` anotasyonu:** ZORUNLU DEĞİLDİR — tek soyut metotlu her arayüze lambda atanabilir. Yaptığı iş yalnızca niyeti bildirmek ve yanlışlıkla ikinci bir soyut metot eklenmesini derleme hatasıyla engellemektir. İki soyut metot, arayüzü fonksiyonel olmaktan çıkarır ve ona artık lambda atanamaz.

**Lambda ifadesi:** `(parametreler) -> gövde` biçimi. Kendi başına bir tipi yoktur; atandığı fonksiyonel arayüzden tipini alır — buna **hedef tip** (target typing) denir.

**Yerleşik fonksiyonel arayüzler (`java.util.function`):** `Function<T,R>` (bir girdi → bir çıktı, metodu `apply`), `Consumer<T>` (girdi al, çıktı yok, `accept`), `Supplier<T>` (girdi yok, çıktı üret, `get`), `Predicate<T>` (girdi → `boolean`, `test`). Dördünü metot adı + imza üzerinden ezbere bilmek gerekir; Stream ve `Optional` API'lerinde hangi arayüzün nereye oturduğunu anında görürsün. İkili (`BiFunction`), ilkel (`IntPredicate`) ve tek-tipli (`UnaryOperator`) varyantları da vardır.

```svg
<svg viewBox="0 0 700 244" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Function apply, Consumer accept, Supplier get, Predicate test arayüzleri girdi çıktı tablosu">
  <text x="350" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">java.util.function — dört temel arayüz</text>
  <text x="113" y="52" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Arayüz</text>
  <text x="345" y="52" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Soyut metot</text>
  <text x="540" y="52" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Girdi</text>
  <text x="645" y="52" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Çıktı</text>
  <line x1="18" y1="60" x2="682" y2="60" stroke="#5D7789" stroke-width="1"/>
  <rect x="18" y="68" width="190" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="113" y="90" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#186399">Function&lt;T,R&gt;</text>
  <text x="345" y="90" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">R apply(T)</text>
  <text x="540" y="90" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">T</text>
  <text x="645" y="90" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">R</text>
  <rect x="18" y="110" width="190" height="34" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="113" y="132" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#B26B00">Consumer&lt;T&gt;</text>
  <text x="345" y="132" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">void accept(T)</text>
  <text x="540" y="132" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">T</text>
  <text x="645" y="132" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#8A2E2E">— yok</text>
  <rect x="18" y="152" width="190" height="34" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="113" y="174" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#25804C">Supplier&lt;T&gt;</text>
  <text x="345" y="174" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">T get()</text>
  <text x="540" y="174" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#8A2E2E">— yok</text>
  <text x="645" y="174" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">T</text>
  <rect x="18" y="194" width="190" height="34" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="113" y="216" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#6B3FA0">Predicate&lt;T&gt;</text>
  <text x="345" y="216" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">boolean test(T)</text>
  <text x="540" y="216" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">T</text>
  <text x="645" y="216" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">boolean</text>
</svg>
```

**Yerel değişken (local variable) vs örnek alanı (instance field):** Yerel değişken bir metodun içinde tanımlanır ve **yığında** (stack) yaşar; metot bitince yok olur. Örnek alanı ise nesnenin parçasıdır ve **öbekte** (heap) yaşar.

**Yakalama (capture) ve kapatma (closure):** Lambda, çevresindeki değişkenleri içeri "alabilir"; buna yakalama denir. Değişkenleri yakalayan lambdaya kapatma (closure) denir.

**`final` / etkin biçimde final (effectively final):** `final` açıkça yazılmış demektir; effectively final ise "yazmamışsın ama bir kez atanmış ve bir daha hiç değiştirilmemiş" demektir.

### Nasıl çalışır?

Lambda, yakaladığı YEREL değişkeni **değere göre** kopyalayarak alır. Kopya alındığı için kaynağın sonradan değişmesi tutarsızlık üretirdi; bu yüzden dil, yakalanan yerel değişkenin `final` veya effectively final olmasını ZORUNLU tutar. Örnek alanları bu kısıtın DIŞINDADIR: lambda alanın değerini değil, `this` referansını yakalar; alana her erişim `this.alan` üzerinden yapıldığı için onu serbestçe okuyup yazabilirsin (`this.sayac++` geçerlidir). Aynısı `static` alanlar için de geçerlidir.

```svg
<svg viewBox="0 0 660 222" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Lambda yakalama: yerel değişken effectively final olmalı, örnek alan serbestçe değişir">
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Lambda neyi nasıl yakalar?</text>
  <rect x="28" y="46" width="286" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="171" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#25804C">Yerel değişken · stack</text>
  <text x="171" y="98" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">int x = 5;  () -&gt; x</text>
  <text x="171" y="124" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">final / effectively final ZORUNLU</text>
  <text x="171" y="146" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">değere göre yakalanır</text>
  <text x="171" y="176" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#8A2E2E">sonra x++ → derleme HATASI</text>
  <rect x="346" y="46" width="286" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="489" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#186399">Örnek alan · heap</text>
  <text x="489" y="98" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">() -&gt; this.sayac++</text>
  <text x="489" y="124" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">kısıt YOK · serbest değiştir</text>
  <text x="489" y="146" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">lambda 'this' referansını yakalar</text>
  <text x="489" y="176" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">oku + yaz serbest</text>
</svg>
```

Klasik `for` döngüsünün sayacı bu kuralın en ünlü kurbanıdır:

```java
for (int i = 0; i < n; i++) {
    list.add(() -> i);   // DERLEME HATASI: i effectively final değil
}
```

`i` her turda `i++` ile değiştiği için ne `final` ne effectively final'dir. İki çözüm vardır. (1) Tur içinde `int sabit = i;` gibi effectively final bir kopya oluşturup onu yakalamak. (2) Gelişmiş `for` kullanmak: `for (int x : liste)` her iterasyonda YENİ bir effectively final değişken oluşturur ve o yakalanabilir. Burada `i`'nin otomatik `volatile` olması ya da lambdaların "son değeri paylaşması" gibi bir davranış YOKTUR; iş baştan derlemede biter.

**`this` kimi gösterir?** Lambda kendi kapsamını (scope) AÇMAZ; içindeki `this`, lambdanın yazıldığı ÇEVRELEYEN nesneyi gösterir — sanki metodun gövdesinin devamıymış gibi. Anonim sınıf (anonymous class) ise gerçek bir nesnedir ve KENDİ `this`'ine sahiptir; oradaki `this` anonim örneğin kendisidir, çevreleyene ulaşmak için `DışSınıf.this` yazman gerekir. Lambda içinde `this` kullanmak yasak değildir, yalnız anlamı farklıdır. Aynı fark gölgelemede (shadowing) de görülür: lambda parametresi çevreleyen bir yerel değişkenle aynı adı taşıyamaz, anonim sınıf ise gölgeleyebilir.

```svg
<svg viewBox="0 0 660 212" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="this: lambdada çevreleyen nesne, anonim sınıfta anonim örnek">
  <defs>
    <marker id="lambda-this-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">“this” kimi gösterir?</text>
  <rect x="48" y="46" width="246" height="50" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="171" y="68" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#6B3FA0">LAMBDA</text>
  <text x="171" y="88" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">() -&gt; println(this)</text>
  <line x1="171" y1="96" x2="171" y2="136" stroke="#5D7789" stroke-width="2" marker-end="url(#lambda-this-ar)"/>
  <rect x="48" y="140" width="246" height="54" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="171" y="164" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#186399">Çevreleyen nesne</text>
  <text x="171" y="184" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">this = DışSınıf örneği</text>
  <rect x="366" y="46" width="246" height="50" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="489" y="68" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#6B3FA0">ANONİM SINIF</text>
  <text x="489" y="88" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">new Runnable(){ this }</text>
  <line x1="489" y1="96" x2="489" y2="136" stroke="#5D7789" stroke-width="2" marker-end="url(#lambda-this-ar)"/>
  <rect x="366" y="140" width="246" height="54" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="489" y="164" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#B26B00">Anonim örnek</text>
  <text x="489" y="184" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">this = kendisi · DışSınıf.this gerekir</text>
</svg>
```

**Perde arkası.** Anonim sınıf, derleme sırasında `DışSınıf$1.class` gibi AYRI bir sınıf dosyası üretir ve her `new` ifadesinde yeni bir nesne yaratır. Lambda ise ayrı bir `.class` üretmez: derleyici gövdeyi aynı sınıfın içinde sentetik (synthetic) bir private metoda koyar ve çağrı yerine bir `invokedynamic` bayt kodu bırakır; asıl bağlamayı çalışma zamanında `LambdaMetafactory` yapar. Bu bağlama çağrı yeri başına BİR KEZ olur, her çağrıda değil.

### Mülakatta ne sorulur?

- **Fonksiyonel arayüzün tek şartı nedir?** Tam olarak bir soyut metot. `default` metot varlığı arayüzü fonksiyonellikten çıkarmaz; anotasyon zorunlu değildir; iki soyut metot ise fonksiyonelliği bozar.
- **Dört temel arayüzün imzaları.** `Supplier<T>` → `T get()` (girdi yok, çıktı var; tembel değer üretimi, `orElseGet(() -> ...)`, gecikmeli loglama, nesne fabrikaları). `Consumer<T>` → `void accept(T)`. `Predicate<T>` → `boolean test(T)`. `Function<T,R>` → `R apply(T)`.
- **Döngü sayacını yakalama.** Effectively final değil → derleme hatası. Sebebini ve iki çözümü söyleyebilmelisin.
- **Lambda'da `this`.** Çevreleyen nesne. Anonim sınıfta anonim örnek. Bu ikisini ters söylemek klasik elemedir.
- **Yerel değişkeni değiştiremiyorum ama alanı değiştirebiliyorum, neden?** Yığın/öbek ve değere-göre-yakalama ayrımı. `volatile` ilan etmek ya da `static` yapmak bir koşul DEĞİLDİR. (Not: çok iş parçacıklı bağlamda alanı değiştirmek görünürlük/atomiklik sorunu getirir — o ayrı bir konudur.)
- **Lambda mı anonim sınıf mı hızlı?** İkisi ÖZDEŞ bayt koda derlenmez. Anonim sınıf ayrı `.class` üretir; lambda `invokedynamic` + `LambdaMetafactory` ile çalışma zamanında bağlanır. Somut fayda: hiçbir değişken YAKALAMAYAN (non-capturing) lambda genellikle tek bir örnek olarak önbelleğe alınıp yeniden kullanılır; yakalama yapan (capturing) lambda ise her değerlendirmede yeni örnek gerektirebilir. Pratikte fark çoğu zaman ölçülemeyecek kadar küçüktür.

### Tek cümlede

Lambda, tek soyut metotlu bir arayüzün kısa yazımıdır: yerel değişkenleri yalnız effectively final oldukları için değere göre yakalar, `this`'i çevreleyen nesneden ödünç alır ve ayrı bir sınıf dosyası yerine `invokedynamic` ile çalışma zamanında bağlanır.

# lam-1 | Orta
## Soru
Bir arayüzün `@FunctionalInterface` sayılması için hangi koşul gerekir?
## A
Tam olarak bir soyut (abstract) metodu olmalı; `default`/`static` metotlar sayıya dâhil değildir
🤖 Doğru: tek soyut metot yeter; default ve static metotlar serbestçe eklenebilir.
## B
Hiç `default` metodu olmamalı; gövdeli bir metot arayüzü fonksiyonel olmaktan çıkarır
🤖 `default` metotlar serbesttir; soyut metot sayısını etkilemezler, arayüz yine fonksiyoneldir.
## C
`@FunctionalInterface` anotasyonu mutlaka yazılmalı, yoksa lambda atanamaz
🤖 Anotasyon zorunlu değil; tek soyut metotlu her arayüze lambda atanabilir, anotasyon sadece derleme kontrolü ekler.
## D
En az iki soyut metodu olmalı ki lambda hangi metodu uyguladığını seçebilsin
🤖 İki soyut metot fonksiyonelliği BOZAR; lambda yalnız tek soyut metotlu arayüze bağlanır.
## Doğru
A
## Açıklama
Fonksiyonel arayüzün tek şartı **tam olarak bir soyut metot** (SAM — Single Abstract Method) içermesidir. `default` ve `static` metotlar gövdeye sahip olduğundan bu sayıya girmez; `Object`'ten gelen `equals`/`hashCode`/`toString` gibi imzalar da sayılmaz (örn. `Comparator` hem tek soyut metotludur hem birçok default/static metot taşır). `@FunctionalInterface` anotasyonu ZORUNLU değildir (C yanlış) — yalnız derleyiciye niyeti bildirir ve yanlışlıkla ikinci soyut metot eklenmesini derleme hatasıyla engeller. İki soyut metot (D) arayüzü fonksiyonel olmaktan çıkarır ve ona lambda atanamaz.

# lam-2 | Orta
## Soru
Bir döngü içinde `for (int i = 0; i < n; i++) { list.add(() -> i); }` yazılırsa ne olur?
## A
Her lambda, döngü bittikten sonraki son `i` değerini paylaşarak yakalar
🤖 Java'da böyle bir "son değeri paylaşma" davranışı yok; asıl sorun `i`'nin final olmamasıdır.
## B
Sorunsuz derlenir; her lambda kendi bağımsız `i` kopyasını tutar
🤖 Derlenmez; döngü değişkeni `i` her turda değiştiği için effectively final değildir.
## C
`i` otomatik olarak `volatile` yapılır ve lambda güncel değeri görür
🤖 Böyle bir otomatik dönüşüm yoktur; sorun görünürlük değil, final-lik kısıtıdır.
## D
Derleme hatası: `i` effectively final olmadığı için yakalanamaz
🤖 Doğru: `i++` ile değiştiği için `i` ne final ne effectively final'dir, yakalanamaz.
## Doğru
D
## Açıklama
Lambda yalnız `final` veya **effectively final** yerel değişkenleri yakalayabilir. Klasik `for` döngüsünün sayaç değişkeni `i`, her turda `i++` ile değiştiği için effectively final DEĞILDIR; bu yüzden `() -> i` **derleme hatası** verir. Çözümler: (1) tur içinde `int sabit = i;` gibi effectively final bir kopya oluşturup onu yakalamak, ya da (2) `for (int x : liste)` gibi gelişmiş `for`'un döngü değişkenini kullanmak (her iterasyonda YENI bir effectively final değişken oluşur, bu yakalanabilir). Bu kısıt, kapatmanın (closure) değeri kopyalayarak yakalamasından ve değişen bir değişkenin tutarsızlık yaratmasını önlemekten kaynaklanır.

# lam-3 | Orta
## Soru
`Supplier<String>` fonksiyonel arayüzünün soyut metodu ve şekli nedir?
## A
`void accept(String t)` — girdi alır, çıktı vermez
🤖 Bu `Consumer`'ın imzasıdır; `Supplier` girdi almaz.
## B
`String get()` — girdi almaz, bir değer üretir
🤖 Doğru: `Supplier` parametresizdir ve `get()` ile değer döndürür.
## C
`boolean test(String t)` — girdiyi sınar
🤖 Bu `Predicate`'in imzasıdır; `boolean` döndürür, `Supplier` değildir.
## D
`String apply(String t)` — girdiyi çıktıya dönüştürür
🤖 Bu `Function`/`UnaryOperator` imzasıdır; `Supplier` girdi almaz.
## Doğru
B
## Açıklama
`Supplier<T>` "girdi YOK, çıktı VAR" biçimindedir: soyut metodu `T get()`'tir. Tembel değer üretimi (lazy supply), varsayılan değer sağlama (`orElseGet(() -> ...)`), gecikmeli loglama ve nesne fabrikaları için kullanılır. Diğerlerini ayırt etmek kritiktir: `Consumer<T>` → `void accept(T)` (girdi var, çıktı yok); `Predicate<T>` → `boolean test(T)` (girdi → boolean); `Function<T,R>` → `R apply(T)` (girdi → çıktı). Dördünü metot adı + imza üzerinden ezbere bilmek, stream ve `Optional` API'lerinde hangi arayüzün nereye uyduğunu anında görmeni sağlar.

# lam-4 | İleri
## Soru
Bir sınıfın metodu içinde yazılan lambda ile anonim sınıf (anonymous class) arasında `this` anahtar kelimesinin anlamı nasıl farklıdır?
## A
İkisinde de `this`, oluşturulan lambdanın/anonim sınıfın kendi örneğini gösterir
🤖 Yalnız anonim sınıf için doğru; lambda kendi `this`'ini hiç oluşturmaz.
## B
Lambda içinde `this` kullanmak yasaktır; derleyici bunu hata olarak işaretler
🤖 Lambda içinde `this` kullanılabilir; yalnızca çevreleyen nesneyi gösterir.
## C
Lambda'da `this` çevreleyen (enclosing) nesneyi, anonim sınıfta ise anonim örneğin kendisini gösterir
🤖 Doğru: lambda kendi kapsamını açmaz, anonim sınıf yeni bir `this` bağlamı yaratır.
## D
Lambda'da `this` anonim örneği, anonim sınıfta ise çevreleyen (enclosing) nesneyi gösterir
🤖 Tam tersi: kendi `this`'ine sahip olan anonim sınıftır; lambda çevreleyenin `this`'ini kullanır.
## Doğru
C
## Açıklama
Bu, lambda ile anonim sınıf arasındaki en ince ama en önemli anlamsal (semantic) farktır. **Lambda kendi kapsamını (scope) açmaz**: içindeki `this`, lambdanın YAZILDIĞI çevreleyen (enclosing) nesneyi gösterir — sanki o metodun gövdesinin devamıymış gibi. **Anonim sınıf** ise gerçek bir nesnedir ve KENDI `this`'ine sahiptir; `this` anonim örneğin kendisini gösterir, çevreleyen nesneye ulaşmak için `DışSınıf.this` gerekir. Aynı fark değişken gölgelemede (shadowing) de görülür: lambda parametresi çevreleyen bir yerel değişkenle aynı adı taşıyamaz, anonim sınıf ise gölgeleyebilir. Pratikte "lambda = kod bloğu, anonim sınıf = ayrı nesne" sezgisi doğru sonucu verir.

# lam-5 | İleri
## Soru
Bir lambda içinde çevreleyen sınıfın örnek alanını (instance field) değiştirmek (`this.sayac++`) mümkün müdür?
## A
Evet; effectively final kısıtı yalnız YEREL değişkenlere uygulanır, alanlar serbesttir
🤖 Doğru: kısıt yerel değişkenleredir; alanlar `this` üzerinden erişildiği için serbesttir.
## B
Hayır; lambda gövdesi hiçbir değişkeni değiştiremez, yalnız okuyabilir
🤖 Örnek alanlar değiştirilebilir; kısıt sadece yakalanan yerel değişkenleri kapsar.
## C
Yalnız alan `volatile` ilan edilmişse mümkündür, aksi halde derleme hatası olur
🤖 `volatile` görünürlük içindir; alanı değiştirmek için gereken bir koşul değildir.
## D
Yalnız alan `static` ise mümkündür; örnek (instance) alanları lambda içinde yazılamaz
🤖 `static` şart değil; örnek alanlarını da `this` üzerinden değiştirebilirsin.
## Doğru
A
## Açıklama
Effectively final kısıtı YALNIZ **yakalanan yerel değişkenlere** (ve metot parametrelerine) uygulanır. Örnek alanlar (instance fields) bu kısıtın DIŞINDADIR: lambda aslında alanın değerini değil, `this` referansını yakalar; alana her erişim `this.alan` üzerinden yapılır, bu yüzden alanı serbestçe okuyup yazabilirsin (`this.sayac++` geçerlidir). Aynı şey `static` alanlar için de geçerlidir. Bu ayrım, "yerel değişkeni değiştiremiyorum ama alanı değiştirebiliyorum" şaşkınlığının kaynağıdır ve bilinçli bir tasarımdır: yerel değişkenler yığın (stack) üzerinde yaşar ve lambda onları değere göre yakalar; alanlar ise öbekteki (heap) nesnede yaşar ve referansla erişilir. (Not: çok iş parçacıklı bağlamda alanı değiştirmek görünürlük/atomiklik sorunları getirebilir — ayrı bir konu.)

# lam-6 | İleri
## Soru
Aynı işi yapan bir lambda ile eşdeğer anonim sınıf arasında, performans/uygulama açısından hangi ifade daha doğrudur?
## A
Lambda her çağrıda yeni bir sınıf dosyası (.class) üretip yükler, bu yüzden anonim sınıftan yavaştır
🤖 `LambdaMetafactory` çağrı yerine karşılık sınıfı BIR KEZ, çalışma-zamanında üretir; her çağrıda değil.
## B
İkisi tamamen özdeş bayt koduna derlenir; aradaki fark yalnızca kaynak kodun yazım biçimindedir
🤖 Özdeş değil; lambda `invokedynamic` kullanır, anonim sınıf ise ayrı bir tip üretir.
## C
Anonim sınıf `invokedynamic` ile bağlanır; lambda ise derlemede ayrı bir `.class` dosyası üretir
🤖 Ters ifade; `invokedynamic` lambdaya, ayrı `.class` dosyası anonim sınıfa aittir.
## D
Anonim sınıf derlemede ayrı bir `.class` üretir; lambda ise `invokedynamic` ile çalışma-zamanında bağlanır
🤖 Doğru: lambda ayrı sınıf dosyası üretmez, `LambdaMetafactory` ile bağlanır.
## Doğru
D
## Açıklama
Anonim sınıf, derleme sırasında `DışSınıf$1.class` gibi AYRI bir sınıf dosyası üretir ve her `new` ifadesinde yeni bir nesne oluşturulur. Lambda ise farklı derlenir: `invokedynamic` bayt kodu + `LambdaMetafactory` ile çalışma-zamanında bağlanır; derlemede ayrı bir `.class` üretmez (özel bir private metot üretir). Bunun somut faydası: **durum tutmayan** (hiçbir değişken yakalamayan) bir lambda genellikle TEK bir örnek olarak önbelleğe alınıp yeniden kullanılır — sürekli yeni nesne yaratmaz. Yakalama yapan (capturing) lambdalar ise her seferinde örnek gerektirebilir. Pratikte bu fark genelde ölçülemeyecek kadar küçüktür; ama "lambda daha hafif ve daha az sınıf dosyası üretir" ifadesi doğrudur ve `invokedynamic` mekanizması bunun nedenidir.
