---
slug: pattern-matching
baslik: "Pattern Matching: instanceof, switch ve Record Deconstruction"
kademe: İleri
sira: 39
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Postacı elindeki gönderiye bakar: "Bu bir mektup mu? Öyleyse zarfı posta kutusuna at. Koli mi? Öyleyse boyunu ölç, kapıya bırak." İki iş birlikte yapılır: **ne olduğunu anlamak** ve **içindekini kullanmak**. Eski Java'da bu iki iş ayrı yazılırdı: önce `instanceof` ile tip sorulur, sonra elle döküm (cast) yapılırdı — tekrarlı ve hataya açık. **Pattern matching (desen eşleştirme)**, tip testini ve değeri kullanılabilir hâle getirmeyi tek adımda birleştiren dil özelliğidir.

### Temel kavramlar

- **Desen (pattern):** bir değerin şekline/tipine uyup uymadığını sınayan kalıp. `String s` deseni "bu bir `String` mi?" diye sorar.
- **Bağlama (binding):** desen tuttuğunda değerin doğrudan bir değişkene atanması. `o instanceof String s` içindeki `s` bir **desen değişkenidir**; sıradan bir değişken adıdır, anahtar kelime değildir (istediğin adı verebilirsin).
- **Döküm (cast):** `(String) o` gibi "bu nesneye şu tipten bak" ifadesi. Yanlış tipe dökersen çalışma zamanında `ClassCastException` alırsın. Pattern matching'in kaldırdığı tekrar tam olarak bu satırdır.
- **Guard (`when`):** desen tuttuktan **sonra** değerlendirilen ek bir boolean koşul. `when` bir **bağlam anahtar kelimesidir (contextual keyword)**.
- **Yıkımlama (deconstruction):** bir record'u eşleştirirken bileşenlerini doğrudan yerel değişkenlere ayırmak.
- **Eksiksizlik (exhaustiveness):** `switch`'in tüm olası halleri kapsadığının derleyicice ispatlanması.

Java'da pattern matching üç aşamada olgunlaştı:

- **`instanceof` pattern** (Java 16): `if (o instanceof String s)` — test başarılıysa `s` otomatik olarak `String` tipinde bağlanır; ayrı döküm gerekmez.
- **`switch` pattern** (Java 21): `case String s ->` ile tür üzerinden dallanma; guard ile ek koşul: `case Integer i when i > 100 ->`.
- **Record deconstruction** (Java 21): `case Nokta(int x, int y) ->` ile bileşenleri doğrudan değişkenlere yıkımlama; iç içe de geçer.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="pattern matching aşamaları">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pattern Matching: test + bağlama tek adımda</text>
  <rect x="30" y="46" width="185" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="122" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">instanceof pattern</text>
  <text x="122" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">o instanceof String s</text>
  <text x="122" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tip testi + bağlama</text>
  <text x="122" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">cast YOK</text>
  <rect x="227" y="46" width="185" height="150" rx="12" fill="#EAF1F8" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="319" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#2C6BB3" font-family="system-ui">switch + guard</text>
  <text x="319" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">case Integer i</text>
  <text x="319" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">when i &gt; 100</text>
  <text x="319" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">koşullu dal</text>
  <rect x="424" y="46" width="186" height="150" rx="12" fill="#FBF3E6" stroke="#B5852A" stroke-width="1.5"/>
  <text x="517" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B5852A" font-family="system-ui">record pattern</text>
  <text x="517" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">case Nokta(int x, int y)</text>
  <text x="517" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bileşen yıkımı</text>
  <text x="517" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iç içe geçebilir</text>
  <text x="320" y="224" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">sealed ile: exhaustiveness derleyici tarafından ispatlanır</text>
</svg>
```

### Nasıl çalışır?

Eski kalıp üç adımdı: `if (o instanceof String) { String s = (String) o; ... }` — test, açık döküm, atama. Yeni hâli tek ifadedir. Desen değişkeni bir **kopya değildir**; aynı nesneye `String` tipiyle bakan bir referanstır.

Desen değişkeninin **kapsamı (scope) akışa duyarlıdır (flow-sensitive)**: `s`, yalnız `o instanceof String` koşulunun kesinlikle DOĞRU olduğu kod yollarında görünür. Test başarısızsa `s` `null` olmaz — hiç var olmaz, kapsam dışıdır; bu yüzden `else` dalında da erişemezsin. Buna karşılık `if (!(o instanceof String s)) return;` yazarsan, o satırdan sonraki yolda `s` **görünür** — çünkü derleyici oraya ulaşıldıysa testin geçmiş olması gerektiğini ispatlar. `s` ne sınıf boyunca ne de metodun tamamında koşulsuz erişilebilirdir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="instanceof pattern açık dökümü ortadan kaldırır">
  <text x="320" y="26" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">instanceof pattern: test + cast + atama → tek satır</text>
  <rect x="30" y="46" width="275" height="145" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">ESKİ: üç adım</text>
  <text x="167" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">if (o instanceof String) {</text>
  <text x="167" y="120" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">String s = (String) o; // cast</text>
  <text x="167" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">s.length(); }</text>
  <text x="167" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tekrarlı + ClassCastException riski</text>
  <rect x="335" y="46" width="275" height="145" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">YENİ: tek adım</text>
  <text x="472" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">if (o instanceof String s) {</text>
  <text x="472" y="124" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">s.length(); // cast YOK</text>
  <text x="472" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">}</text>
  <text x="472" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">s doğru tipte otomatik bağlanır</text>
</svg>
```

`switch` pattern'de `null` da bir desen olabilir: `case null ->`. Bu önemlidir, çünkü klasik `switch`, seçici (selector) `null` olduğunda **`NullPointerException`** fırlatırdı ve switch'ten önce elle `if (o == null)` yazmak gerekirdi. Kritik kural: **tip desenleri `null` ile eşleşmez.** `case String s`, `null` gelirse tutmaz. `case null` (ya da `case null, default`) yazılmamışsa seçici `null` geldiğinde switch yine `NullPointerException` fırlatır. Yani `case null`, null kontrolünü switch'in İÇİNE, bildirimsel biçimde taşır.

Desenlerin **sırası** derleyici tarafından denetlenir: daha genel bir desen, kendisinden daha dar bir deseni **gölgeler (dominance)**. Bu yüzden guard'lı (`when`) ve daha özel dallar, guard'sız/daha genel dallardan **önce** yazılmalıdır; aksi halde ulaşılamayan dal **derleme hatası** verir. `default` her zaman en sonda düşünülür.

```java
static String tanimla(Object o) {
    return switch (o) {
        case null            -> "boş";
        case Integer i when i < 0 -> "negatif tam sayı";
        case Integer i       -> "tam sayı: " + i;
        case String s        -> "metin(" + s.length() + ")";
        default              -> "bilinmeyen";
    };
}
```

Guard'ın alternatifi dal içine `if` yazmaktır; çalışır, ama koşulu `case` üzerinde bildirimsel biçimde ifade etmek okunurluğu ve eksiksizlik analizini korur. `case` etiketinde `&&` geçerli bir söz dizimi **değildir**; `default`'a koşul eklenemez.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="switch desen sırası: null ve guard önce genel dal sonra">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">switch pattern: sıra önemli — dar dal önce</text>
  <line x1="95" y1="48" x2="95" y2="198" stroke="#5D7789" stroke-width="1.5"/>
  <path d="M89,196 L101,196 L95,210 Z" fill="#5D7789"/>
  <rect x="125" y="44" width="400" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="325" y="66" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case null → "boş"  (NPE riski biter)</text>
  <rect x="125" y="90" width="400" height="34" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="325" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case Integer i when i &lt; 0 → guard'lı (dar)</text>
  <rect x="125" y="136" width="400" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="325" y="158" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">case Integer i → guard'sız (genel)</text>
  <rect x="125" y="182" width="400" height="34" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="325" y="204" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">default → diğer her şey</text>
  <text x="325" y="240" text-anchor="middle" font-size="11.5" font-weight="600" fill="#8A2E2E" font-family="system-ui">guard'lı dal genel daldan ÖNCE gelmezse derleme hatası</text>
</svg>
```

**Record pattern** üç işi birlikte yapar: değerin `Nokta` olup olmadığını test eder, eşleşirse `x`'e `nokta.x()`, `y`'ye `nokta.y()` değerlerini bağlar, dal gövdesinde bunları doğrudan kullandırır. Yeni nesne **oluşturmaz**, bileşenleri değiştirmez/sıfırlamaz (record zaten değişmezdir) ve tipleri birbirine dönüştürmez. Desenler iç içe geçebilir ve `var` ile tip çıkarımına izin verir: `case Cizgi(Nokta(var x1, var y1), Nokta(var x2, var y2))`.

Son parça `sealed` ile birleşir: bir `switch`, `sealed` bir tipin tüm izinli alt türlerini kapsıyorsa **exhaustive** sayılır, `default` dalına gerek kalmaz ve derleyici eksiksizliği doğrular. İleride `permits` listesine yeni bir alt tür eklenip `switch` güncellenmezse derleme hatası oluşur. Ok (`->`) sözdizimli switch'te fall-through (dallar arası düşme) yoktur; dal seçimi eşleşen desene göre belirlenir, rastgele değildir.

### Mülakatta ne sorulur?

**"`if (o instanceof String s)` içindeki `s` nedir?"** — Testi geçen `o`'nun `String` tipine bağlanmış hâli. Kopya değil, aynı nesne. Test başarısızsa kapsam dışıdır (`null` değil). Sıradan bir değişken adıdır, anahtar kelime değil.

**"Hangi boilerplate kalktı?"** — Tip testinden sonra elle yazılan **açık döküm satırı**. `try-catch`, `import` ya da `equals`/`hashCode` ile ilgisi yoktur.

**"Guard nedir?"** — `case Integer i when i > 100 ->`. Desen tuttuktan sonra değerlendirilir; tutmazsa dal atlanır. Guard'lı (dar) dallar guard'sız (genel) dallardan önce gelmelidir; yoksa dominance nedeniyle derleme hatası.

**"`case null` ne sağlar?"** — `null` girdiyi ayrı bir dalda ele almayı; klasik switch'in null'da NPE fırlatmasını önler. `case null, default ->` birleşimi de mümkündür. Diğer tip desenleri null ile eşleşmez.

**"Record pattern ne yapar?"** — Tip testi + bileşen yıkımı. Reflection gerekmez, `toString` ayrıştırmak gibi kırılgan hilelere hiç gerek yoktur; erişimciyle elle okumak çalışır ama daha az okunurdur.

### Tek cümlede

Pattern matching, "bu değer hangi şekle uyuyor?" testiyle "içindekini bana ver" bağlamasını tek, tip güvenli ifadede birleştirir; `sealed` ile birleşince derleyici hiçbir halin unutulmadığını da ispatlar.

# pat-1 | Orta
## Soru
`if (o instanceof String s) { ... }` yazıldığında `s` değişkeni nedir?
## A
Test başarısız olduğunda `null`, başarılı olduğunda `o` değerini alan; her iki dalda da erişilebilen bir yer tutucudur
🤖 Yanlış; test başarısızsa `s` hiç var olmaz (kapsam dışıdır), `null` değerini almaz.
## B
Tip testi başarılıysa `o`'nun `String` tipine bağlanmış hâli; blok içinde ayrı döküm (cast) gerekmeden kullanılır
🤖 Doğru: pattern değişkeni testi geçince otomatik olarak doğru tipte bağlanır.
## C
`o` nesnesinin savunmacı kopyasıdır (defensive copy); üzerinde yapılan değişiklikler `o`'yu etkilemez
🤖 Kopya değil; `s`, aynı nesneye `String` tipiyle bakan bir referanstır.
## D
Yalnız `switch` içinde geçerli olan, derleyicinin tanıdığı özel bir anahtar kelimedir
🤖 `s` sıradan bir değişken adıdır (istediğini koyabilirsin), anahtar kelime değildir; `if` içinde de çalışır.
## Doğru
B
## Açıklama
`instanceof` pattern, klasik "önce `instanceof` ile test et, sonra elle `(String) o` döküm yap" iki adımını **tek adıma** indirger. `o instanceof String s` testi başarılıysa `s`, `o`'nun `String` olarak bağlanmış hâlidir ve blok içinde doğrudan `s.length()` gibi kullanılabilir — ayrı cast gerekmez (B doğru). `s` bir kopya değil (C yanlış), aynı nesneye tip bilgisiyle bir bağlamadır. Test başarısızsa `s` kapsam dışıdır (`null` değil — A yanlış). `s` sıradan bir değişken adıdır, anahtar kelime değil (D yanlış) ve `if` içinde de çalışır. Pattern değişkeninin **kapsamı (scope)** akışa duyarlıdır: yalnız testin doğru olduğu kod yollarında görünür.

# pat-2 | Orta
## Soru
`instanceof` pattern'in ortadan kaldırdığı temel tekrarlayan kod (boilerplate) nedir?
## A
Tip testinden sonra elle yapılan açık döküm (explicit cast) satırı
🤖 Doğru: pattern, test + cast + atama üçlüsünü tek ifadeye indirir.
## B
`try-catch` bloklarının tamamı
🤖 İstisna işleme ayrı bir konudur; instanceof pattern ile ilgisi yoktur.
## C
`import` bildirimleri
🤖 Import'lar tip erişimiyle ilgilidir; pattern matching bunları etkilemez.
## D
`equals` ve `hashCode` metotlarının yazımı
🤖 Bunlar eşitlik semantiğiyle ilgilidir; instanceof pattern bunları üretmez.
## Doğru
A
## Açıklama
Pattern öncesi klasik kalıp şuydu: `if (o instanceof String) { String s = (String) o; ... }` — yani test, sonra **açık döküm (explicit cast)**, sonra atama. Bu üç adım hem tekrarlı hem de hataya açıktı (yanlış tipe cast → `ClassCastException`). `instanceof` pattern bunu `if (o instanceof String s)` ile tek ifadeye indirger; cast tamamen ortadan kalkar (A doğru). Bu, hem daha kısa hem de daha güvenlidir: derleyici bağlamanın tip güvenliğini garanti eder. `try-catch` (B), `import` (C) ve `equals`/`hashCode` (D) tamamen farklı konulardır ve pattern matching ile ilgileri yoktur. Kaldırılan boilerplate spesifik olarak **döküm satırıdır**.

# pat-3 | İleri
## Soru
`switch` pattern'de bir dala ek bir çalışma zamanı koşulu eklemek için ne kullanılır?
## A
Dalın içine `if` yazıp koşulu orada kontrol etmek — tek yol budur
🤖 Mümkün ama pattern'in guard (`when`) yapısı bu koşulu doğrudan case üzerinde ifade eder.
## B
`case` ifadesine `&&` operatörü eklemek
🤖 `&&` case etiketinde geçerli bir söz dizimi değildir; guard için `when` kullanılır.
## C
`default` dalını koşullu hale getirmek
🤖 `default` koşulsuz "diğer her şey" dalıdır; koşul eklenemez.
## D
Guard: `case Integer i when i > 100 ->` biçiminde `when` koşulu
🤖 Doğru: `when` guard'ı, pattern eşleşmesine ek bir boolean koşul bağlar.
## Doğru
D
## Açıklama
`switch` pattern'de bir dalın yalnız belirli bir koşul sağlandığında çalışmasını istiyorsan **guard** kullanılır: `case Integer i when i > 100 ->`. Buradaki `when`, pattern eşleştikten SONRA değerlendirilen bir boolean koşuldur; sağlanmazsa o dal atlanır ve sıradaki case'lere devam edilir (D doğru). `when` özel bir bağlam anahtar kelimesidir (contextual keyword). `&&` (B) case etiketinde geçerli değildir; `default`'a koşul eklenemez (C yanlış). Dal içinde `if` yazmak (A) çalışır ama guard, koşulu case üzerinde bildirimsel (declarative) biçimde ifade ederek exhaustiveness analizini ve okunabilirliği korur. Guard'lı case'lerin sırası önemlidir: daha dar (guard'lı) dallar, daha genel dallardan önce gelmelidir.

# pat-4 | İleri
## Soru
`case Nokta(int x, int y) ->` biçimindeki record pattern ne yapar?
## A
Eşleşme sırasında `x` ve `y` değerleriyle `Nokta` sınıfının yeni bir örneğini oluşturup dala geçirir
🤖 Yeni nesne yaratmaz; var olan record'u yerinde bileşenlerine ayırır (deconstruct).
## B
`Nokta` tipindeki değeri `int` tipine dönüştürür (cast) ve bileşenleri toplayıp tek bir sayı üretir
🤖 Böyle bir dönüşüm/toplama yoktur; mevcut `int` bileşenleri ayrı ayrı değişkenlere bağlanır.
## C
Değeri `Nokta` mı diye test eder ve eşleşirse bileşenlerini (`x`, `y`) doğrudan yerel değişkenlere bağlar (deconstruction)
🤖 Doğru: record pattern hem tip testi yapar hem de bileşenleri yıkımlayıp bağlar.
## D
Eşleşen `Nokta` nesnesinin bileşenlerini varsayılan değerlerine (`0`) sıfırlar
🤖 Record değişmezdir; desen bileşenleri değiştirmez, yalnız mevcut değerlerini okuyup bağlar.
## Doğru
C
## Açıklama
**Record pattern**, bir record'u eşleştirirken bileşenlerini tek adımda yerel değişkenlere **yıkımlar (deconstruct)**. `case Nokta(int x, int y) ->` şu üç işi birlikte yapar: (1) değerin `Nokta` olup olmadığını test eder, (2) eşleşirse `x`'e `nokta.x()`, `y`'ye `nokta.y()` değerlerini bağlar, (3) dal gövdesinde `x` ve `y` doğrudan kullanılabilir (C doğru). Yeni nesne oluşturmaz (A yanlış), record'u sayıya dönüştürmez (B yanlış) ve bileşenleri sıfırlamaz/değiştirmez — record zaten değişmezdir (D yanlış). Pattern'ler **iç içe** de geçebilir: `case Cizgi(Nokta(var x1, var y1), Nokta(var x2, var y2))` bir bileşik yapıyı tek desende çözer. Bu, veri odaklı kodda karmaşık nesne yapılarını okumak için son derece güçlü bir araçtır ve `var` ile tip çıkarımına da izin verir.

# pat-5 | İleri
## Soru
Bir `switch`, `sealed` bir tipin tüm izinli alt türlerini kapsıyorsa hangi sonuç ortaya çıkar?
## A
`switch` exhaustive (eksiksiz) kabul edilir ve `default` dalına gerek kalmaz
🤖 Doğru: kapalı hiyerarşi + tüm alt türlerin kapsanması exhaustiveness ispatını sağlar.
## B
`switch` yine de `default` istemek zorundadır, aksi halde derlenmez
🤖 Yanlış; sealed + tüm alt türler kapsanınca default gereksizdir.
## C
`switch` çalışma zamanında rastgele bir dalı seçer
🤖 Dal seçimi asla rastgele değildir; eşleşen desene göre belirlenir.
## D
Tüm dallar sırayla çalışır (fall-through)
🤖 Ok (`->`) sözdizimli switch'te fall-through yoktur; yalnız eşleşen dal çalışır.
## Doğru
A
## Açıklama
`sealed` bir tipin tüm izinli alt türleri bir `switch`'te kapsandığında, derleyici olası tüm hallerin ele alındığını **ispatlayabilir** — çünkü hiyerarşi kapalıdır ve alt tür kümesi sonludur. Bu durumda `switch` **exhaustive** sayılır ve `default` dalına GEREK KALMAZ (A doğru, B yanlış). Bunun büyük pratik değeri: ileride `permits` listesine yeni bir alt tür eklenip `switch` güncellenmezse derleme HATASI oluşur — böylece "yeni hal eklendi ama işlenmedi" hatası derleme zamanında yakalanır. Ok sözdizimli (`->`) pattern switch'te fall-through yoktur (D yanlış) ve dal seçimi desene göre belirlenir, rastgele değildir (C yanlış). Bu "sealed + exhaustive switch" birleşimi, güvenli ADT işlemenin temelidir.

# pat-6 | Uzman
## Soru
Java 21 `switch` pattern'de `case null ->` yazmanın sağladığı davranış nedir?
## A
`null` değeri otomatik olarak `0`'a dönüştürülür ve sayısal dal çalıştırılır
🤖 Böyle bir dönüşüm yok; case null yalnız null girdiyi ayrı bir dalda ele alır.
## B
`null` girdi ayrı bir desende ele alınır; `switch` artık `NullPointerException` fırlatmaz
🤖 Doğru: null pattern, null'ı bir case olarak yakalar ve NPE riskini bildirimsel biçimde ortadan kaldırır.
## C
Tüm `case` dalları `null` girdide de çalışır, çünkü `null` her desenle eşleşir
🤖 Yalnız `case null` dalı null'da çalışır; diğer desenler null'la eşleşmez.
## D
`switch` derlenmez; `null` bir desen olamaz, derleyici hata verir
🤖 Yanlış; Java 21'de `case null` geçerli bir desendir.
## Doğru
B
## Açıklama
Klasik `switch`, seçici (selector) `null` olduğunda **`NullPointerException`** fırlatırdı; bu yüzden switch'ten ÖNCE elle `if (o == null)` kontrolü yazmak gerekirdi. Java 21 pattern switch'te **`case null ->`** yazarak `null` girdiyi doğrudan bir dal olarak ele alabilirsin (B doğru). Böylece null kontrolü switch'in İÇİNE, bildirimsel biçimde taşınır ve ayrı bir NPE riski kalmaz. `case null, default ->` biçiminde null'ı default ile birleştirmek de mümkündür. `null`, otomatik bir sayıya dönüşmez (A yanlış); yalnız `case null` dalı null'da çalışır, diğer type pattern'ler null'la EŞLEŞMEZ (C yanlış); ve `case null` Java 21'de tamamen geçerlidir (D yanlış). Bu özellik, null güvenliğini switch'in ifade gücüne dahil eder.
