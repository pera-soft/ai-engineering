---
slug: optional
baslik: "Optional: Amaç, map/flatMap/orElse ve Anti-Pattern'ler"
kademe: Orta
sira: 36
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Kargodan bir zarf geldi. Zarfın üstünde "içinde belge OLABİLİR, olmayabilir" yazıyor. Bu ilk bakışta can sıkıcı görünür ama aslında iyilik: zarfı açmadan önce boş olma ihtimalini düşünmek zorunda kalırsın. Oysa üzerinde hiçbir uyarı olmayan bir zarfı güvenle açar, boş çıkınca şaşırırsın — Java'da bu şaşkınlığın adı `NullPointerException`'dır (NPE) ve programı çalışırken durdurur. `Optional<T>`, "değer olabilir ya da olmayabilir" durumunu tip düzeyinde yazan bir sarmalayıcıdır (wrapper); metodun imzasına bakan herkes riski görür ve derleyici de değeri almadan önce bir karar vermeye zorlar.

### Temel kavramlar

**`null`:** "Burada nesne yok" anlamına gelen özel değer. Üzerinde metot çağırırsan `NullPointerException` alırsın.

**Sarmalayıcı (wrapper):** Başka bir değeri içinde taşıyan nesne. `Optional<T>` ya bir değer taşır (dolu) ya da hiçbir şey taşımaz (boş).

**Oluşturma:** `Optional.of(x)` — `x`'in `null` OLMADIĞINDAN eminsen; `null` verirsen hemen NPE atar. `Optional.ofNullable(x)` — `x` `null` olabilir, boş `Optional` üretilir. `Optional.empty()` — bilerek boş.

**Dönüştürme:** `map(fn)` değer varsa fonksiyonu uygular ve sonucu yine `Optional` içinde döndürür, yoksa boş kalır. `filter(p)` koşulu sağlamayan değeri boşa çevirir. `flatMap(fn)` ise YALNIZ fonksiyonun kendisi `Optional` döndürdüğünde kullanılır; iç içe kalacak `Optional<Optional<X>>`'i düzleştirir.

**Değer alma:** `orElse(varsayilan)` yoksa varsayılanı döndürür. `orElseGet(supplier)` yoksa tembel olarak üretir. `orElseThrow(...)` yoksa istisna fırlatır. `ifPresent(consumer)` değer varsa bir işlem yapar, yoksa hiçbir şey. Java 9 ile `ifPresentOrElse(consumer, runnable)`, Java 10 ile argümansız `orElseThrow()` eklendi; ikincisi `get()` ile aynı işi yapar ama niyeti ("burada değer olmalı") adıyla açıkça söyler.

```svg
<svg viewBox="0 0 700 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Optional yaşam döngüsü: oluştur, Optional, dönüştür, değer al aşamaları">
  <defs>
    <marker id="opt-lc-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="350" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Optional yaşam döngüsü</text>
  <rect x="16" y="52" width="150" height="140" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="91" y="76" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">1 · OLUŞTUR</text>
  <text x="91" y="104" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">of(x)</text>
  <text x="91" y="130" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">ofNullable(x)</text>
  <text x="91" y="156" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">empty()</text>
  <rect x="200" y="86" width="118" height="72" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="259" y="116" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#6B3FA0">Optional&lt;T&gt;</text>
  <text x="259" y="136" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">dolu / boş</text>
  <rect x="352" y="52" width="150" height="140" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="427" y="76" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#186399">2 · DÖNÜŞTÜR</text>
  <text x="427" y="104" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">map(fn)</text>
  <text x="427" y="130" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">filter(p)</text>
  <text x="427" y="156" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">flatMap(fn)</text>
  <rect x="536" y="44" width="150" height="156" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="611" y="68" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#B26B00">3 · DEĞER AL</text>
  <text x="611" y="94" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">orElse(v)</text>
  <text x="611" y="118" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">orElseGet(s)</text>
  <text x="611" y="142" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">orElseThrow()</text>
  <text x="611" y="166" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">ifPresent(c)</text>
  <line x1="166" y1="122" x2="198" y2="122" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-lc-ar)"/>
  <line x1="318" y1="122" x2="350" y2="122" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-lc-ar)"/>
  <line x1="502" y1="122" x2="534" y2="122" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-lc-ar)"/>
</svg>
```

### Nasıl çalışır?

**`map` zinciri.** `map` güvenli dönüşümün doğru aracıdır: değer VARSA fonksiyonu uygular, YOKSA hiçbir şey yapmadan boş `Optional` döndürür. Böylece NPE riski olmadan zincirleyebilirsin:

```java
String sehir = adresOpt
        .map(Adres::getSehir)          // getSehir düz String döndürür → map
        .map(String::toUpperCase)
        .orElse("BILINMIYOR");
```

Burada `flatMap` kullanmak DERLENMEZ, çünkü `getSehir()` bir `Optional` değil düz `String` döndürür. `flatMap`'in yeri, fonksiyonun kendisinin `Optional` döndürdüğü durumdur. `opt.isPresent() ? opt.get().getSehir() : null` gibi bir üçlü işleç kalıbı ise çalışır ama `null` üreterek `Optional`'ın kazandırdığı güvenliği geri verir.

**`orElse` vs `orElseGet` — değerlendirme zamanı.** `orElse(X)` bir METOT ARGÜMANI alır ve Java'da argümanlar çağrıdan ÖNCE değerlendirilir (eager). Yani `orElse(pahaliUret())`, `Optional` DOLU olsa bile `pahaliUret()`'i HER ZAMAN çalıştırır — sonucu atılacak olsa da. Değer varsa bu boşuna yapılmış pahalı bir iştir (DB sorgusu, ağ çağrısı) ve yan etkisi varsa (loglama, sayaç) hata bile doğurur. `orElseGet(Supplier)` supplier'ı YALNIZ değer yokken çağırır (lazy); dolu `Optional`'da hiç çalıştırmaz — ve istisna fırlatmaz, değeri üretir. Kural: varsayılan sabit veya ucuzsa `orElse` yeterlidir; PAHALI ya da YAN ETKİLİYSE `orElseGet`. Aynı tembellik farkı `orElseThrow(supplier)` için de geçerlidir. Bu farkın tiplerle ilgisi yoktur — her tiple aynı biçimde çalışır.

```svg
<svg viewBox="0 0 660 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="orElse argümanı her zaman hesaplanır, orElseGet supplier yalnız değer yokken çağrılır">
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">orElse vs orElseGet — Optional DOLU iken</text>
  <rect x="24" y="46" width="288" height="140" rx="10" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="2"/>
  <text x="168" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#8A2E2E">orElse(pahali())</text>
  <text x="168" y="98" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">argüman ÖNCEDEN hesaplanır</text>
  <text x="168" y="122" text-anchor="middle" font-family="system-ui" font-size="12" fill="#8A2E2E">pahali() HER ZAMAN çalışır (eager)</text>
  <text x="168" y="146" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">değer varsa sonucu ATILIR</text>
  <text x="168" y="170" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#8A2E2E">boşa iş · yan etki riski ✗</text>
  <rect x="348" y="46" width="288" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="492" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#25804C">orElseGet(() -&gt; pahali())</text>
  <text x="492" y="98" text-anchor="middle" font-family="system-ui" font-size="12" fill="#1C3247">supplier gerektiğinde çağrılır</text>
  <text x="492" y="122" text-anchor="middle" font-family="system-ui" font-size="12" fill="#25804C">değer varsa HİÇ çalışmaz (lazy)</text>
  <text x="492" y="146" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">yalnız değer yokken üretir</text>
  <text x="492" y="170" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">pahalı varsayılanda tercih ✓</text>
</svg>
```

**En büyük tehlike — `get()`.** `Optional.get()` sarmalanan DEĞERİ döndürür; değer yoksa `NoSuchElementException: No value present` fırlatır. `null` döndürmez, boş `Optional` döndürmez, sessizce geçmez — patlar. Varlığı kontrol etmeden `get()` çağırmak bir anti-pattern'dir: `Optional`'ın tüm amacı yokluk durumunu güvenle ele almaktır, kontrolsüz `get()` ise kaçınılmak istenen çalışma-zamanı patlamasını yalnızca başka bir isimle geri getirir.

```svg
<svg viewBox="0 0 680 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="get anti-pattern boş Optional'da NoSuchElementException fırlatır, güvenli metotlar tercih edilir">
  <defs>
    <marker id="opt-get-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="340" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">get() anti-pattern · boş Optional</text>
  <rect x="20" y="86" width="150" height="46" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="95" y="114" text-anchor="middle" font-family="system-ui" font-size="12.5" fill="#6B3FA0">Optional.empty()</text>
  <rect x="250" y="46" width="180" height="46" rx="9" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="2"/>
  <text x="340" y="74" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#8A2E2E">get() ✗</text>
  <rect x="250" y="126" width="180" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="340" y="146" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">orElse / orElseThrow</text>
  <text x="340" y="163" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">map / ifPresent ✓</text>
  <line x1="170" y1="100" x2="248" y2="76" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-get-ar)"/>
  <line x1="170" y1="118" x2="248" y2="142" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-get-ar)"/>
  <rect x="470" y="46" width="196" height="46" rx="9" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="2"/>
  <text x="568" y="68" text-anchor="middle" font-family="system-ui" font-size="11.5" font-weight="700" fill="#8A2E2E">NoSuchElementException</text>
  <text x="568" y="84" text-anchor="middle" font-family="system-ui" font-size="11" fill="#8A2E2E">NPE'yi geri getirir</text>
  <rect x="470" y="126" width="196" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="568" y="148" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#186399">güvenli · deyimsel</text>
  <text x="568" y="165" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">yokluk durumu ele alınır</text>
  <line x1="430" y1="69" x2="468" y2="69" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-get-ar)"/>
  <line x1="430" y1="149" x2="468" y2="149" stroke="#5D7789" stroke-width="2" marker-end="url(#opt-get-ar)"/>
</svg>
```

### Mülakatta ne sorulur?

- **Asıl tasarım amacı.** `Optional` öncelikle bir DÖNÜŞ TİPİ aracıdır: `Optional<Kullanici> findById(...)` imzası "sonuç bulunamayabilir" der ve çağıranı bu durumu ele almaya zorlar. Programdaki TÜM `null` kontrollerini kaldırmaz; serileştirme (serialization) için değildir — `Optional` `Serializable` bile değildir; bellek kullanımını da azaltmaz, aksine ek bir sarmalama nesnesi getirir.
- **`get()`'in tehlikesi.** Boş `Optional`'da `NoSuchElementException`. `if (opt.isPresent()) opt.get()` kalıbı bile genelde `ifPresent`/`map` ile daha temiz yazılır; modern JDK'da niyeti belli etmek için `get()` yerine argümansız `orElseThrow()` tercih edilir.
- **`map` mı `flatMap` mı?** Fonksiyon düz değer döndürüyorsa `map`, `Optional` döndürüyorsa `flatMap`.
- **`orElse` / `orElseGet` tuzağı.** Eager vs lazy. Mülakatın klasik sorusudur.
- **Değer varsa yap, yoksa hiçbir şey yapma.** Doğru araç `ifPresent(consumer)`'dır. `orElse(null)` + `if` değeri tekrar `null`'a çevirir; koşulsuz `get()` patlar; `map`'i yan etki için kullanmak teknik olarak çalışır ama yanlış araçtır — `map` DÖNÜŞTÜRME içindir ve burada kimsenin kullanmadığı bir `Optional` üretilerek niyet okuyucudan gizlenir. Java 9+ ayrıca `ifPresentOrElse(consumer, runnable)` sunar.
- **Anti-pattern'ler.** `Optional`'ı **alan** (field) yapmak önerilmez — `Serializable` değildir ve her nesneye sarmalama yükü bindirir. **Metot parametresi** yapmak çağıranı gereksiz `Optional.of(...)` sarmaya zorlar; aşırı yükleme (overload) veya varsayılan değer daha temizdir. **Koleksiyon elemanı** yapmak (`List<Optional<String>>`) gereksizdir; boş değerleri hiç eklememek veya filtrelemek daha sadedir. Boş koleksiyon için `Optional<List>` yerine BOŞ liste döndür. Bir `Optional` değişkenini `null` yapmak ise en büyük çelişkidir. Josh Bloch ve JDK mimarlarının açık tavsiyesi: `Optional`'ı yalnız dönüş değeri olarak kullan.

### Tek cümlede

`Optional`, "sonuç olmayabilir" bilgisini dönüş tipine yazan bir sarmalayıcıdır; doğru kullanımı `map`/`filter`/`ifPresent`/`orElse*` ile fonksiyonel zincirlemek, yanlış kullanımı ise `get()`'e ya da alan/parametre olarak kullanmaya sapmaktır.

# opt-1 | Orta
## Soru
`Optional`'ın asıl tasarım amacı nedir?
## A
"Değer bulunamayabilir" durumunu dönüş tipinde açıkça belirtmek
🤖 Doğru: `Optional` öncelikle "sonuç olmayabilir" dönüşlerini açık kılmak içindir.
## B
Programdaki tüm `null` kontrollerini gereksiz kılıp tamamen kaldırmak
🤖 `Optional` her `null`'ı kaldırmaz; belirli "değer yok" dönüşlerini tip düzeyinde açık yapar.
## C
Nesneleri ağ üzerinden daha hızlı serileştirmek (serialization)
🤖 `Optional` serileştirme için değildir; aslında `Serializable` bile değildir.
## D
Sarmalanan nesnelerin bellek kullanımını azaltmak
🤖 Sarmalama ek bir nesne getirir; bellek azaltmak `Optional`'ın amacı değildir.
## Doğru
A
## Açıklama
`Optional` öncelikle bir **dönüş tipi** aracıdır: bir metodun "sonuç olmayabilir" olasılığını imzasında AÇIKÇA belirtir (`Optional<Kullanici> findById(...)`). Böylece çağıran, `null` gelip gelmediğini tahmin etmek yerine değeri kullanmadan önce varlığını ele almak zorunda kalır — bu, sessiz `NullPointerException`'ları derleme-zamanı görünür bir tasarıma dönüştürür. `Optional` TÜM null'ları kaldırmaz (B) — yerel değişkenler, alanlar ve parametreler için değil, "değer üretmeyebilen dönüşler" için tasarlanmıştır. Serileştirme (C) amacı değildir; `Optional` `Serializable` bile DEĞILDIR, bu yüzden alan olarak kullanımı sakıncalıdır. Bellek (D) ile ilgisi yoktur, aksine ek sarmalama nesnesi getirir.

# opt-2 | Orta
## Soru
`opt.get()` çağrısının en büyük tehlikesi nedir?
## A
Değer olsun ya da olmasın her zaman `null` döndürür
🤖 `get()` `null` döndürmez; değer varsa onu verir, yoksa istisna fırlatır.
## B
Derleme hatası verir; `get()` artık çağrılabilir bir metot değildir
🤖 `get()` geçerli bir metottur; sorun çalışma-zamanında ortaya çıkan istisnadır.
## C
Değer yoksa `Optional.empty()` döndürdüğü için hata sessizce gizlenir
🤖 `get()` bir `Optional` değil, sarmalanan DEĞERI döndürür; boşta sessiz kalmaz, patlar.
## D
Değer YOKSA `NoSuchElementException` fırlatarak programı patlatır
🤖 Doğru: kontrolsüz `get()` bir anti-pattern'dir, boş `Optional`'da patlar.
## Doğru
D
## Açıklama
`Optional.get()`, sarmalanan değer YOKSA `NoSuchElementException: No value present` fırlatır. Varlığı (`isPresent()`) kontrol etmeden `get()` çağırmak bir **anti-pattern**'dir: `Optional`'ın tüm amacı "değer yok" durumunu güvenle ele almaktır, oysa kontrolsüz `get()` tam da kaçınılmak istenen çalışma-zamanı patlamasını geri getirir — yalnız NPE yerine `NoSuchElementException` adıyla. Doğru alternatifler: `orElse(varsayilan)`, `orElseGet(supplier)`, `orElseThrow(() -> new ...)`, ya da değeri dönüştürmek/kullanmak için `map`/`ifPresent`. `if (opt.isPresent()) opt.get()` kalıbı bile genelde `ifPresent`/`map` ile daha temiz yazılır. Modern JDK'da niyeti belli etmek için `get()` yerine `orElseThrow()` (argümansız) tercih edilir.

# opt-3 | Orta
## Soru
Bir metot `Optional<Adres>` döndürüyor ve `Adres.getSehir()` bir `String` veriyor. Şehri (varsa) büyük harfe çevirip almak için en uygun zincir hangisidir?
## A
`opt.get().getSehir().toUpperCase()` ile doğrudan zincirlemek
🤖 Kontrolsüz `get()` boş `Optional`'da patlar; güvenli değildir.
## B
`opt.map(Adres::getSehir).map(String::toUpperCase).orElse("BILINMIYOR")`
🤖 Doğru: `map` zinciri değer yoksa güvenle boş kalır, `orElse` varsayılan verir.
## C
`opt.flatMap(Adres::getSehir).orElse("BILINMIYOR")`
🤖 `getSehir` `String` döndürür, `Optional` değil; `flatMap` burada tip uyuşmazlığı verir.
## D
`opt.isPresent() ? opt.get().getSehir() : null`
🤖 Çalışır ama `null` döndürerek Optional'ın amacını boşa çıkarır; ayrıca büyük harfe çevirmez.
## Doğru
B
## Açıklama
`map`, `Optional` üzerinde güvenli dönüşümün doğru aracıdır: değer VARSA fonksiyonu uygular ve sonucu yine `Optional` içinde döndürür; YOKSA hiçbir şey yapmadan boş `Optional` döndürür — NPE riski olmadan zincirlenebilir. `opt.map(Adres::getSehir).map(String::toUpperCase).orElse("BILINMIYOR")` değer yoksa güvenle varsayılana düşer. `flatMap` (C) YALNIZ fonksiyonun kendisi `Optional` döndürdüğünde kullanılır; `getSehir()` düz `String` döndürdüğü için burada `map` doğrudur, `flatMap` derlenmez. Kontrolsüz `get()` (A) boş değerde patlar. `isPresent()`+ternary+`null` (D) hem `null` üreterek Optional felsefesini bozar hem de istenen dönüşümü yapmaz. Bu soru `map`/`flatMap` ayrımını ve güvenli zincirlemeyi sınar.

# opt-4 | İleri
## Soru
`orElse(pahaliVarsayilanUret())` ile `orElseGet(() -> pahaliVarsayilanUret())` arasındaki kritik fark nedir?
## A
İkisi tamamen aynıdır; `orElseGet` yalnızca aynı işin daha kısa bir yazımıdır
🤖 Aynı değil; biri varsayılanı her zaman, diğeri yalnız gerektiğinde üretir.
## B
`orElseGet`, değer bulunamazsa varsayılan üretmek yerine istisna fırlatır
🤖 `orElseGet` istisna fırlatmaz; değer yoksa supplier'ı çağırıp sonucunu döndürür.
## C
`orElse`'in argümanı Optional dolu olsa bile hesaplanır (eager); `orElseGet` tembeldir (lazy)
🤖 Doğru: `orElse` argümanı erken değerlendirir, `orElseGet` yalnız gerektiğinde çağırır.
## D
`orElse` yalnız `String` gibi değişmez (immutable) tiplerle güvenle çalışır
🤖 Tip kısıtı yoktur; `orElse` her tiple çalışır, fark değerlendirme zamanıyla ilgilidir.
## Doğru
C
## Açıklama
Bu, mülakatta sık sorulan ince bir tuzaktır. `orElse(X)` bir METOT ARGÜMANI alır; Java'da argümanlar çağrıdan ÖNCE değerlendirilir (eager). Yani `orElse(pahaliVarsayilanUret())`, `Optional` DOLU olsa bile `pahaliVarsayilanUret()`'i HER ZAMAN çalıştırır — sonucu kullanılmasa bile. Değer varsa bu boşa yapılan pahalı bir iştir (DB sorgusu, ağ çağrısı vb.); yan etkisi varsa (loglama, sayaç) hata bile doğurabilir. `orElseGet(Supplier)` ise supplier'ı YALNIZ değer yokken çağırır (lazy) — dolu `Optional`'da hiç çalıştırmaz. Kural: varsayılan sabit/ucuzsa `orElse` yeterli; varsayılan PAHALI veya YAN ETKILIYSE `orElseGet` kullan. Aynı tembellik farkı `orElseThrow(supplier)` için de geçerlidir.

# opt-5 | İleri
## Soru
Aşağıdakilerden hangisi `Optional`'ın önerilen (uygun) kullanımıdır?
## A
Bir metodun DÖNÜŞ tipi olarak, "sonuç bulunamayabilir" durumunu belirtmek için
🤖 Doğru: `Optional`'ın tasarlandığı asıl yer "değer olmayabilir" dönüşleridir.
## B
Bir sınıfın örnek alanı (instance field) olarak, `null` değer tutmak yerine
🤖 Önerilmez; `Optional` `Serializable` değildir ve alan olarak ek sarmalama yükü getirir.
## C
Bir metodun parametresi olarak, çağıranı `Optional` sarmaya zorlamak için
🤖 Anti-pattern; aşırı yükleme (overload) veya varsayılan değerle ele almak daha temizdir.
## D
Bir koleksiyon elemanı olarak (`List<Optional<String>>`), eksik değerleri işaretlemek için
🤖 Gereksiz sarmalama; boş değerleri filtrelemek veya listeye hiç eklememek daha iyidir.
## Doğru
A
## Açıklama
`Optional`'ın ONAYLANMIŞ kullanımı, "değer üretmeyebilen" bir metodun DÖNÜŞ tipidir (örn. `findByEmail` → `Optional<Kullanici>`) — böylece çağıran yokluk durumunu tip düzeyinde ele almak zorunda kalır. Diğer üç seçenek yaygın anti-pattern'lerdir: (B) **alan** olarak — `Optional` `Serializable` değildir, her nesneye ek sarmalama ve bellek yükü bindirir; alan `null` olabiliyorsa bunu tasarımla çözmek gerekir. (C) **parametre** olarak — çağıranı gereksiz `Optional.of(...)` sarmaya zorlar; bunun yerine metot aşırı yüklemesi veya varsayılan değer daha temizdir. (D) **koleksiyon elemanı** olarak — `List<Optional<String>>` yerine boş değerleri hiç eklememek ya da filtrelemek daha sadedir. Josh Bloch ve JDK mimarlarının açık tavsiyesi: `Optional`'ı yalnız dönüş değeri olarak kullan.

# opt-6 | Orta
## Soru
Değer VARSA bir işlem yapmak, YOKSA hiçbir şey yapmamak için en uygun `Optional` metodu hangisidir?
## A
`orElse(null)` ile değeri alıp ardından `if` ile `null` kontrolü yapmak
🤖 Çalışır ama `null`'a geri dönerek `Optional`'ın amacını bozar; daha temiz bir yol var.
## B
Doğrudan `get()` çağırıp dönen değeri kullanmak
🤖 Değer yoksa `get()` istisna fırlatır; koşulsuz çağrı güvenli değildir.
## C
`map(deger -> { islem(deger); return deger; })` ile işlemi `map` içinde yapmak
🤖 `map` dönüştürmek içindir; yan etki için kullanmak niyeti gizler ve sonucu atılan bir `Optional` üretir.
## D
`ifPresent(deger -> ...)` — değer varsa consumer'ı çalıştırır, yoksa hiçbir şey yapmaz
🤖 Doğru: `ifPresent` tam bu senaryo için tasarlanmıştır, güvenli ve deyimseldir.
## Doğru
D
## Açıklama
`ifPresent(Consumer)` tam bu senaryo içindir: `Optional` doluysa verilen consumer'ı değerle çalıştırır, boşsa HIÇBIR şey yapmaz — `isPresent()` kontrolü + `get()` ikilisinin güvenli ve deyimsel (idiomatic) karşılığıdır. `if (opt.isPresent()) { kullan(opt.get()); }` kalıbı hem daha uzundur hem de kontrolsüz `get()` alışkanlığını besler. `orElse(null)` + `if` (A) değeri tekrar `null`'a çevirerek `Optional`'ın kazandırdığı güvenliği geri verir. Kontrolsüz `get()` (B) boş değerde `NoSuchElementException` fırlatır. `map`'i yan etki için kullanmak (C) teknik olarak çalışır ama yanlış araçtır: `map` DÖNÜŞTÜRME içindir; burada sonucu kimsenin kullanmadığı bir `Optional` üretilir ve niyet okuyucudan gizlenir. Java 9+ ayrıca "değer varsa şunu, yoksa bunu yap" için `ifPresentOrElse(consumer, runnable)` sunar. Genel ilke: `Optional`'ı `if/get` ile değil, `map`/`filter`/`ifPresent`/`orElse*` gibi fonksiyonel metotlarla ele almak.
