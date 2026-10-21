---
slug: method-reference
baslik: "Metot Referansları: Dört Tür ve Lambda'ya Çevirme"
kademe: Orta
sira: 33
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Birine yolu tarif ederken "şu köşeden sağa, sonra düz, sonra..." demek yerine bazen parmağınla göstermek yeter. Metot referansı da budur: zaten VAR OLAN bir işi baştan anlatmak yerine ona doğrudan işaret etmek. `x -> System.out.println(x)` yazmak, "al bu değeri, sonra ekrana bas" diye tarif etmektir; `System.out::println` ise aynı işi parmakla göstermektir. Yeni bir yetenek kazandırmaz — lambdanın kısaltmasıdır — ama okuyanın gözünde gürültüyü siler.

### Temel kavramlar

**Örnek metodu (instance method):** Bir NESNE üzerinde çağrılan metot: `"abc".toUpperCase()`. Çalışmak için mutlaka bir nesneye ihtiyaç duyar.

**Statik metot (static method):** Sınıfın kendisine ait metot, nesne gerekmez: `Integer.parseInt("12")`.

**Alıcı (receiver):** Bir örnek metodunun ÜZERİNDE çalıştığı nesne. `"abc".toUpperCase()` çağrısında alıcı `"abc"`tir. Bu terim metot referanslarının tamamını anlamanın anahtarıdır.

**Metot referansı:** `::` operatörüyle var olan bir metoda işaret etme biçimi. Dört türü vardır:

- **Statik metot referansı:** `Integer::parseInt` → `s -> Integer.parseInt(s)`.
- **Belirli bir nesnenin örnek metodu (bound instance):** `System.out::println` → `x -> System.out.println(x)`. Alıcı ÖNCEDEN bellidir ve hiç değişmez.
- **Rastgele bir nesnenin örnek metodu (unbound / arbitrary object):** `String::toLowerCase` → `s -> s.toLowerCase()`. Buradaki `String` bir NESNE değil, bir TİP'tir; alıcı çalışma zamanında ilk argüman olarak gelir. En çok karıştırılan türdür.
- **Yapıcı referansı (constructor reference):** `ArrayList::new` → `() -> new ArrayList<>()`.

```svg
<svg viewBox="0 0 700 248" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Metot referansının dört türü ve lambda karşılıkları tablosu">
  <text x="350" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Metot referansı — dört tür</text>
  <text x="113" y="50" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Tür</text>
  <text x="345" y="50" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Örnek</text>
  <text x="562" y="50" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#5D7789">Lambda karşılığı</text>
  <line x1="18" y1="58" x2="682" y2="58" stroke="#5D7789" stroke-width="1"/>
  <rect x="18" y="66" width="190" height="36" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="113" y="89" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#186399">Statik</text>
  <text x="345" y="89" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">Integer::parseInt</text>
  <text x="562" y="89" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#5D7789">s -&gt; Integer.parseInt(s)</text>
  <rect x="18" y="110" width="190" height="36" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="113" y="133" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#25804C">Bağlı örnek · bound</text>
  <text x="345" y="133" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">System.out::println</text>
  <text x="562" y="133" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#5D7789">x -&gt; System.out.println(x)</text>
  <rect x="18" y="154" width="190" height="36" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="113" y="177" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#B26B00">Bağsız örnek · unbound</text>
  <text x="345" y="177" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">String::toLowerCase</text>
  <text x="562" y="177" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#5D7789">s -&gt; s.toLowerCase()</text>
  <rect x="18" y="198" width="190" height="36" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="113" y="221" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#6B3FA0">Yapıcı · ::new</text>
  <text x="345" y="221" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">ArrayList::new</text>
  <text x="562" y="221" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#5D7789">() -&gt; new ArrayList&lt;&gt;()</text>
</svg>
```

**Hedef tip (target type):** Metot referansının tipi kendinden değil, atandığı fonksiyonel arayüzden gelir. `String::length` bir `Function<String,Integer>` beklenen yere konursa oraya uyar.

### Nasıl çalışır?

Kilit ayrım `nesne::metot` (bound) ile `Tip::metot` (unbound) arasındadır. Birincisinde metot HEP aynı nesne üzerinde çağrılır. İkincisinde alıcı sabit değildir; fonksiyonel arayüzün İLK parametresi alıcı olur, KALAN parametreler metodun kendi parametrelerine gider.

```svg
<svg viewBox="0 0 660 216" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bağlı ile bağsız metot referansı: alıcı sabit mi yoksa ilk argümandan mı gelir">
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Bağlı (bound) vs Bağsız (unbound)</text>
  <rect x="24" y="46" width="288" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="168" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#25804C">nesne::metot  (bound)</text>
  <text x="168" y="98" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">System.out::println</text>
  <text x="168" y="124" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">alıcı SABİT: hep System.out</text>
  <text x="168" y="146" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">her çağrı aynı nesnede</text>
  <text x="168" y="176" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#1C3247">x -&gt; System.out.println(x)</text>
  <rect x="348" y="46" width="288" height="150" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="492" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#B26B00">Tip::metot  (unbound)</text>
  <text x="492" y="98" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#1C3247">String::toLowerCase</text>
  <text x="492" y="124" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">alıcı İLK ARGÜMANdan gelir</text>
  <text x="492" y="146" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">her çağrı farklı nesnede</text>
  <text x="492" y="176" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#1C3247">s -&gt; s.toLowerCase()</text>
</svg>
```

Bu "alıcı da bir argüman sayılır" kuralı, ilk bakışta imzalar uyuşmuyormuş gibi görünen durumları açıklar. `compareToIgnoreCase(String other)` tek parametreli bir ÖRNEK metodudur; unbound biçimde yazıldığında:

```java
// 1 alıcı + 1 parametre = toplam 2 argüman, dönüş int
Comparator<String> c = String::compareToIgnoreCase;
// eşdeğeri:
Comparator<String> c2 = (a, b) -> a.compareToIgnoreCase(b);
```

Yani tek parametreli bir örnek metodu, unbound referans olarak İKİ argümanlı bir fonksiyonel arayüze oturabilir. `Comparator` bir fonksiyonel arayüz olduğu için bunu kabul eder; `Comparator.comparing` ve `sorted` gibi API'lerin sade yazımını mümkün kılan da tam olarak budur.

```svg
<svg viewBox="0 0 680 178" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bağsız referansta alıcı da bir argüman sayılır; tek parametreli metot iki argümanlı Comparator olur">
  <defs>
    <marker id="mref-ub-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Bağsız: alıcı da bir argüman sayılır</text>
  <rect x="16" y="52" width="200" height="72" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="116" y="82" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">String::</text>
  <text x="116" y="100" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">compareToIgnoreCase</text>
  <text x="116" y="118" text-anchor="middle" font-family="system-ui" font-size="10.5" fill="#5D7789">tek parametreli örnek metot</text>
  <rect x="248" y="52" width="212" height="72" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="354" y="86" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">(a, b) -&gt; a.compareTo-</text>
  <text x="354" y="104" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">IgnoreCase(b)</text>
  <text x="354" y="120" text-anchor="middle" font-family="system-ui" font-size="10.5" fill="#5D7789">2 argüman → int</text>
  <rect x="492" y="52" width="172" height="72" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="578" y="86" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#25804C">Comparator&lt;String&gt;</text>
  <text x="578" y="106" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">compare(a, b)</text>
  <line x1="216" y1="88" x2="246" y2="88" stroke="#5D7789" stroke-width="2" marker-end="url(#mref-ub-ar)"/>
  <line x1="460" y1="88" x2="490" y2="88" stroke="#5D7789" stroke-width="2" marker-end="url(#mref-ub-ar)"/>
  <text x="340" y="152" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">ilk argüman = alıcı (receiver) · kalan = metodun parametresi</text>
</svg>
```

Yapıcı referansları da hedef arayüzün imzasına göre eşleşir. `Supplier<List<String>> t = ArrayList::new;` parametresiz `ArrayList()` yapıcısına bağlanır ve her `t.get()` çağrısı YENİ ve boş bir liste döndürür — tek bir paylaşılan örneğe bağlanma söz konusu değildir. Hedef parametreliyse (örn. `Function<Integer,List<String>>`) aynı `ArrayList::new` bu kez `new ArrayList<>(kapasite)` yapıcısına bağlanır. `Collectors.toCollection(ArrayList::new)` bu desenin en yaygın kullanımıdır. Dikkat: `new` bir metot değil, nesne yaratma operatörüdür — yapıcı referansı bir statik metot referansı DEĞİLDİR.

### Mülakatta ne sorulur?

- **`String::toUpperCase` hangi lambdadır?** `s -> s.toUpperCase()` (unbound). `() -> String.toUpperCase()` olamaz, çünkü metot statik değildir ve bir alıcı ister; `s -> String.toUpperCase(s)` de olamaz, çünkü `String` sınıfında böyle bir statik metot yoktur. `toUpperCase` argüman almadığı için iki parametreli bir imzaya da uymaz. Karşılaştır: `"sabit"::toUpperCase` bound olurdu ve HEP aynı metin üzerinde çalışırdı; unbound biçimde ise `stream.map(String::toUpperCase)` her elemanı kendi üzerinde işler.
- **`list.forEach(System.out::println)` hangi türdendir?** Bound. `System.out` zaten var olan belirli bir `PrintStream` nesnesidir ve `println` hep onun üzerinde çalışır. `System` sınıfının statik olması yanıltmasın — çağrılan şey, statik ALAN olan `out` nesnesinin ÖRNEK metodudur. Unbound olsaydı `PrintStream::println` yazılır ve alıcı argümandan gelirdi.
- **`map` içinde doğru referansı seçmek.** `String`'i uzunluğuna çevirmek için `String::length` (unbound, `Function<String,Integer>`; `int` otomatik kutulanır). `Integer::parseInt` metnin SAYISAL içeriğini ayrıştırır (`"12" → 12`), uzunluğu vermez ve `"abc"` için `NumberFormatException` atar. `String::valueOf` tersine bir şeyi metne çevirir. `String::isEmpty` `boolean` üretir; `Stream<Integer>` beklenen yere konursa derleme hatası verir.
- **Ne zaman metot referansına çevirmelisin?** Yalnız lambda gövdesi var olan bir metodu, argümanları AYNEN ve SIRASINI bozmadan, ek işlem yapmadan çağırıyorsa. Argüman sırasını değiştiren, sonuca bir şey ekleyen (`x -> f(x) + 1`) veya birden çok ifade içeren blok gövdeli lambdalar çevrilemez. Ayrıca metot referansı HER ZAMAN daha okunabilir değildir: bağlam belirsizse (`this::islem` neyi çağırıyor?) açık lambda niyeti daha net gösterir. Kural: dönüşüm mümkün VE anlam kaybı yoksa metot referansını tercih et.

### Tek cümlede

Metot referansı, gövdesi yalnız var olan bir metodu çağıran lambdanın `::` ile yazılmış hâlidir. Türünü ayırt etmenin yolu alıcının nereden geldiğine bakmaktır: sabit bir nesneden (bound), ilk argümandan (unbound), hiç yoktan (statik) ya da `new` ile (yapıcı).

# mref-1 | Orta
## Soru
`String::toUpperCase` metot referansı hangi lambda ifadesine karşılık gelir?
## A
`() -> String.toUpperCase()`
🤖 `toUpperCase` statik değildir; parametresiz çağrılamaz, bir alıcı nesne gerekir.
## B
`s -> s.toUpperCase()`
🤖 Doğru: `Tip::örnekMetot` biçiminde ilk argüman alıcı olur (unbound).
## C
`s -> String.toUpperCase(s)`
🤖 `String` sınıfında böyle statik bir metot yok; `toUpperCase` bir örnek metodudur.
## D
`(a, b) -> a.toUpperCase(b)`
🤖 `toUpperCase` argüman almaz; iki parametreli bir imzaya uymaz.
## Doğru
B
## Açıklama
`String::toUpperCase`, "rastgele bir nesnenin örnek metodu" (unbound / arbitrary object) türüdür. Burada `String` bir NESNE değil, bir TIP'tir; metodun çağrılacağı gerçek nesne, fonksiyonel arayüzün İLK parametresi olarak gelir. Yani `s -> s.toUpperCase()` demektir ve bir `Function<String,String>` (veya `UnaryOperator<String>`) beklentisine uyar. Bu türü, `nesne::metot` (bound) türüyle karıştırmamak gerekir: `"sabit"::toUpperCase` her seferinde AYNI `"sabit"` üzerinde çalışırdı. Unbound biçimde ise alıcı her çağrıda değişir — `stream.map(String::toUpperCase)` her elemanı kendi üzerinde işler.

# mref-2 | Orta
## Soru
`list.forEach(System.out::println)` ifadesindeki metot referansı hangi türdendir?
## A
Statik metot referansı (`Tip::statikMetot`)
🤖 `println` statik değil; `System.out` bir `PrintStream` örneğidir, metot ona ait örnek metodudur.
## B
Rastgele bir nesnenin örnek metodu (unbound / arbitrary object)
🤖 Unbound olsaydı tip üzerinden yazılırdı (`PrintStream::println`) ve alıcı argümandan gelirdi; burada nesne sabittir.
## C
Belirli bir nesnenin örnek metodu (bound instance)
🤖 Doğru: `System.out` önceden belirli bir nesnedir; `println` hep onun üzerinde çağrılır.
## D
Yapıcı (constructor) referansı (`Tip::new`)
🤖 `new` yok; nesne üretilmiyor, var olan `System.out` üzerinde metot çağrılıyor.
## Doğru
C
## Açıklama
`System.out::println` **belirli bir nesnenin örnek metodu** (bound instance) türüdür: `System.out` ZATEN var olan belirli bir `PrintStream` nesnesidir ve `println`, her çağrıda HEP bu nesne üzerinde çalışır. Lambda karşılığı `x -> System.out.println(x)`'tir. Bunu unbound türden (`PrintStream::println`) ayıran şey, alıcının sabit olmasıdır: unbound'da alıcı her çağrıda ilk argümandan gelirdi. `System` sınıfının statik olması yanıltmasın — çağrılan metot statik alan olan `out` NESNESININ örnek metodudur, bu yüzden statik metot referansı DEĞILDIR.

# mref-3 | Orta
## Soru
`Stream<String> s` üzerinde `s.map(...)` ile her `String`'i uzunluğuna (`Integer`) çevirmek için hangi metot referansı doğrudur?
## A
`String::valueOf`
🤖 `valueOf` bir `String` döndürür ve tersine iş yapar; uzunluk (int) elde etmez.
## B
`Integer::parseInt`
🤖 `parseInt` bir `String`'i sayıya çevirir ama içeriği ayrıştırır; "uzunluk" hesaplamaz, "abc" için hata atar.
## C
`String::isEmpty`
🤖 `isEmpty` bir `boolean` döndürür; `map` sonucu `Stream<Boolean>` olur, `Stream<Integer>` değil.
## D
`String::length`
🤖 Doğru: unbound örnek metot referansı; her `String`'in `length()` sonucunu (int) verir.
## Doğru
D
## Açıklama
`String::length`, unbound örnek metot referansıdır ve `s -> s.length()`'e eşdeğerdir. `map` her `String` elemanı alıcı yapar, `length()` çağrılır ve `int` (otomatik kutulanarak `Integer`) üretilir — tam olarak `Function<String,Integer>` beklentisine uyar. `Integer::parseInt` (B) metnin SAYISAL içeriğini ayrıştırır (`"12" → 12`), uzunluğu değil; sayısal olmayan metinde `NumberFormatException` atar. `String::valueOf` (A) tersine bir şeyi metne çevirir. `String::isEmpty` (C) ise `boolean` üretir; `Stream<Integer>` beklenen yere konursa derleme hatası verir. Bu soru, "hangi metot referansı hangi dönüşümü yapar" ayrımını ve unbound biçimin `map` içindeki tipik kullanımını sınar.

# mref-4 | İleri
## Soru
`Supplier<List<String>> tedarikci = ArrayList::new;` ifadesi ne yapar ve türü nedir?
## A
Yapıcı referansıdır; her `get()` çağrısında yeni bir `ArrayList` üretir
🤖 Doğru: `Tip::new` yapıcı referansıdır, `Supplier.get()` her çağrıda yeni nesne döndürür.
## B
Var olan tek bir `ArrayList` örneğine referans tutar; her `get()` aynı listeyi döndürür
🤖 `::new` her çağrıda YENI nesne üretir; tek bir örneğe bağlanma yapmaz.
## C
`ArrayList` sınıfındaki statik bir `new` metodunu çağıran statik metot referansıdır
🤖 `new` bir metot değil, nesne yaratma operatörüdür; bu bir statik metot referansı değildir.
## D
Derleme hatası verir: `Supplier` yapıcı referansı kabul etmez, yalnız lambda alır
🤖 `Supplier` parametresiz üretici olduğundan parametresiz yapıcıya tam uyar; hata olmaz.
## Doğru
A
## Açıklama
`ArrayList::new` bir **yapıcı referansıdır** (constructor reference) ve `() -> new ArrayList<>()` lambdasına eşdeğerdir. `Supplier<List<String>>` parametresiz bir üretici olduğundan, parametresiz `ArrayList()` yapıcısına birebir uyar; her `tedarikci.get()` çağrısı YENI ve boş bir `ArrayList` döndürür — tek bir paylaşılan örnek değil (B yanlış). Yapıcı referansları, hedef fonksiyonel arayüzün imzasına göre uygun yapıcıyı seçer: parametreli bir arayüz (örn. `Function<Integer,List<String>>`) `new ArrayList<>(kapasite)` gibi parametreli yapıcıya bağlanabilir. Bu desen, `Collectors.toCollection(ArrayList::new)` ve nesne fabrikalarında yaygındır.

# mref-5 | Orta
## Soru
Bir lambda'yı metot referansına çevirmenin GENEL olarak uygun olduğu durum hangisidir?
## A
Lambda gövdesi yalnızca var olan bir metodu, argümanları aynen ileterek çağırıyorsa
🤖 Doğru: metot referansı tam da "sadece bir metodu olduğu gibi çağıran" lambdalar içindir.
## B
Lambda, argümanların sırasını değiştirerek metoda iletiyorsa
🤖 Metot referansı argüman sırasını değiştiremez; böyle bir durumda lambda kalmalıdır.
## C
Lambda birden çok ifade ve ek hesaplama içeren bir blok gövdeye sahipse
🤖 Metot referansı tek bir çağrıya indirgenir; çok ifadeli gövde referansa çevrilemez.
## D
Lambda, çağrının sonucuna sabit bir değer ekliyorsa (`x -> f(x) + 1`)
🤖 Ek işlem (`+ 1`) var; bu tek bir metot çağrısı değildir, referansa çevrilemez.
## Doğru
A
## Açıklama
Metot referansı, lambda gövdesi SADECE var olan bir metodu — argümanları aynen, sırasını bozmadan, ek işlem yapmadan — çağırıyorsa uygundur ve okunabilirliği artırır: `x -> foo(x)` → `Foo::foo`. Argüman sırasını değiştiren (B), ek hesaplama yapan (`x -> f(x)+1`, D) veya birden çok ifade içeren (C) lambdalar metot referansına ÇEVRILEMEZ; bunlarda lambda daha açık ve zaten zorunludur. Önemli bir uyarı: metot referansı HER ZAMAN daha okunabilir değildir — bağlam belirsizse (`this::islem` neyi çağırıyor?) açık bir lambda niyeti daha net gösterebilir. Kural: dönüşüm mümkün VE anlam kaybı yoksa metot referansını tercih et.

# mref-6 | İleri
## Soru
`stream.sorted(String::compareToIgnoreCase)` ifadesinde `String::compareToIgnoreCase` neden `Comparator<String>` olarak çalışır?
## A
`String.compareToIgnoreCase` statik bir metottur ve zaten iki `String` argümanı alır
🤖 `compareToIgnoreCase` bir örnek metodudur; statik değildir ve tek açık argüman alır.
## B
Yapıcı referansı olduğu için karşılaştırılacak iki nesneyi kendisi üretir
🤖 `new` yok; bu bir yapıcı referansı değil, var olan bir karşılaştırma metodudur.
## C
Unbound referanstır: ilk argüman alıcı, ikincisi metodun parametresi olur
🤖 Doğru: unbound biçimde ilk parametre alıcı olduğu için imza `(a, b)`'ye genişler.
## D
`Comparator` arayüzü metot referanslarını kabul etmediğinden bu ifade aslında çalışmaz
🤖 `Comparator` fonksiyonel arayüzdür; uygun imzalı metot referansını kabul eder ve çalışır.
## Doğru
C
## Açıklama
Bu, unbound (rastgele nesne) metot referanslarının en zarif ve en çok karıştırılan kullanımıdır. `String::compareToIgnoreCase` biçiminde `String` bir TIP'tir; unbound kuralı gereği İLK argüman alıcı (receiver) olur, KALAN argümanlar metodun parametresi olur. `compareToIgnoreCase(String other)` tek parametrelidir; ilk argüman alıcı + ikinci argüman parametre = toplam İKI argüman → `int` döner. Bu tam olarak `Comparator<String>.compare(a, b)` imzasıdır: `(a, b) -> a.compareToIgnoreCase(b)`. Yani tek parametreli bir örnek metodu, unbound referans olarak İKI argümanlı bir fonksiyonel arayüze uyabilir — çünkü alıcı da bir "argüman" sayılır. Bu prensip `Comparator.comparing`, `sorted` ve benzeri API'lerin sade yazımını mümkün kılar.
