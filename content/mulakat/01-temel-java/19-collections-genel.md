---
slug: collections-genel
baslik: "Collections Framework: Arayüz Hiyerarşisi ve Doğru Seçim"
kademe: Temel
sira: 19
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Evdeki eşyaları düşün. Alışveriş listesi sıralıdır ve aynı ürünü iki kez yazabilirsin. Anahtarlık ise farklıdır: aynı anahtarın iki kopyasını taşımanın anlamı yoktur. Telefon rehberin ise bambaşka çalışır — bir **isimden** numaraya gidersin. Java Collections Framework, veri gruplarını tutmanın bu farklı biçimlerini standart arayüzler ve hazır gerçekleştirimler (implementation) olarak sunar. Böylece her projede kendi liste/küme yapını yazmak zorunda kalmazsın; doğru olanı **seçersin**.

### Temel kavramlar

**Arayüz (interface)** "ne yapılabilir"i, **gerçekleştirim (implementation)** ise "nasıl yapıldığını" tanımlar. `List` bir arayüzdür; `ArrayList` onun bir gerçekleştirimidir.

Framework'ün **iki ayrı kökü** vardır:

- **`Collection`**: tek tek öğelerin topluluğu. Başlıca alt arayüzler:
  - **`List`**: **sıralı** (ekleme sırası korunur), **indeksli** (`get(i)` ile konuma erişilir), **tekrar (duplicate) kabul eden** dizi (`ArrayList`, `LinkedList`).
  - **`Set`**: küme semantiği — **tekrar KABUL ETMEZ**, aynı eleman en fazla bir kez bulunur (`HashSet` sırasız, `LinkedHashSet` ekleme sıralı, `TreeSet` doğal sıraya göre sıralı).
  - **`Queue` / `Deque`**: uçlardan işlem gören sıra/kuyruk (`ArrayDeque`, `LinkedList`); FIFO/LIFO.
- **`Map`**: **`Collection` DEĞİLDİR** — onu genişletmez (`extends` etmez), ayrı bir hiyerarşidir. Çünkü tek tek eleman değil, **anahtar → değer** çiftleri tutar (`HashMap`, `LinkedHashMap`, `TreeMap`). Anahtarlar **benzersizdir**; aynı anahtara tekrar `put` yapmak yeni eleman eklemez, eski değeri **günceller**. Erişim indeksle değil anahtarla olur: `get(anahtar)`.

Hepsi generic'tir, yani her nesne tipini tutabilir; "biri ilkel (primitive) tip, diğeri nesne tutar" gibi bir ayrım yoktur.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Collection ve Map ayrı kökler; List, Set, Queue alt arayüzleri">
  <rect x="60" y="16" width="200" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="160" y="41" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Collection (arayüz)</text>
  <rect x="400" y="16" width="200" height="40" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="500" y="35" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Map (ayrı kök)</text>
  <text x="500" y="50" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">anahtar → değer</text>
  <rect x="20" y="120" width="130" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="85" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">List</text>
  <text x="85" y="157" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sıralı, tekrarlı</text>
  <rect x="165" y="120" width="130" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="230" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Set</text>
  <text x="230" y="157" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tekrarsız</text>
  <rect x="310" y="120" width="130" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="375" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Queue</text>
  <text x="375" y="157" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">uçtan işlem</text>
  <path d="M120 56 L85 120" stroke="#5D7789" stroke-width="1.5" marker-end="url(#colArrow)"/>
  <path d="M160 56 L230 120" stroke="#5D7789" stroke-width="1.5" marker-end="url(#colArrow)"/>
  <path d="M210 56 L375 120" stroke="#5D7789" stroke-width="1.5" marker-end="url(#colArrow)"/>
  <text x="320" y="200" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Map, Collection'ı GENİŞLETMEZ — ayrı hiyerarşi</text>
  <defs>
    <marker id="colArrow" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

Seçim iki eksende yapılır. Birincisi **semantik**: sıra + indeks + tekrar → `List`; benzersizlik → `Set`; anahtar-değer → `Map`; FIFO/LIFO uç işlemleri → `Deque`. Varsayılan tercihler genelde `ArrayList`, `HashSet`, `HashMap`, `ArrayDeque`'tir.

İkinci eksen **maliyettir (big-O)** — bir işlemin, eleman sayısı `n` büyüdükçe nasıl yavaşladığını anlatan gösterim. `O(1)` "eleman sayısından bağımsız sabit", `O(n)` "eleman sayısıyla orantılı", `O(log n)` ise ikisinin arasıdır.

`ArrayList` bir **dinamik dizi** üzerine kuruludur:

- `get(i)` doğrudan dizi indekslemesidir → **O(1)**.
- Sona ekleme (`add(e)`) genelde sabittir; yalnızca iç dizi dolduğunda daha büyük bir diziye kopyalama (yeniden boyutlandırma, O(n)) gerekir. Bu seyrek olduğu için ortalamada **amortize O(1)**'dir — "her zaman O(n)" değildir.
- Başa veya ortaya ekleme-silme, sonraki tüm elemanları kaydırmayı gerektirdiği için **O(n)**'dir.

`LinkedList`'te tam tersi geçerlidir: uçlara ekleme-silme O(1), indeksle erişim O(n). Bu yüzden sık konumsal erişimde `ArrayList`, sık baş/orta ekleme-silmede `LinkedList` düşünülür.

`HashMap` / `HashSet` ortalama **O(1)** `get`/`put`/`contains` sunar ama sıralama tutmaz: `hashCode` ile doğrudan kovaya (bucket) gider. (Çok kötü bir hash dağılımı ya da yoğun çakışma bunu nadiren bozabilir.) `TreeMap` / `TreeSet` anahtarları **sıralı** tutar, buna karşılık kırmızı-siyah ağaç yapısı yüzünden erişim **O(log n)**'dir — sıralama gerekmiyorsa gereksiz maliyettir.

Hash tabanlı yapılar hem benzersizliği hem kova yerleşimini `hashCode()` ve `equals()` ile belirler; bu yüzden bu yapılara konan tipler ikisini de **tutarlı biçimde** ezmelidir. Var olan bir elemanı `Set`'e ikinci kez eklemek istisna fırlatmaz:

```java
Set<String> s = new HashSet<>();
s.add("a");
s.add("b");
s.add("a");        // sessizce false döner, eleman katılmaz
// s.size() == 2
```

```svg
<svg viewBox="0 0 620 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="List Set Queue Map hızlı karşılaştırma tablosu">
  <text x="310" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi koleksiyon? — sıra / tekrar / tipik seçim</text>
  <rect x="20" y="36" width="580" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="95" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Tür</text>
  <text x="255" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sıralı?</text>
  <text x="400" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Tekrar?</text>
  <text x="535" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Tipik</text>
  <text x="95" y="92" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">List</text>
  <text x="255" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">indeksli, evet</text>
  <text x="400" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">✓ kabul</text>
  <text x="535" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ArrayList</text>
  <text x="95" y="122" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Set</text>
  <text x="255" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hash: hayır</text>
  <text x="400" y="122" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">✗ tekrarsız</text>
  <text x="535" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">HashSet</text>
  <text x="95" y="152" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Queue</text>
  <text x="255" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">FIFO/LIFO</text>
  <text x="400" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">✓ kabul</text>
  <text x="535" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ArrayDeque</text>
  <text x="95" y="182" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Map</text>
  <text x="255" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">anahtara göre</text>
  <text x="400" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">key benzersiz</text>
  <text x="535" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">HashMap</text>
  <path d="M20 106 L600 106" stroke="#E3E9EF" stroke-width="1"/>
  <path d="M20 136 L600 136" stroke="#E3E9EF" stroke-width="1"/>
  <path d="M20 166 L600 166" stroke="#E3E9EF" stroke-width="1"/>
</svg>
```

### Mülakatta ne sorulur?

**"`Map`, `Collection`'ı genişletir mi?"** Hayır — en sık sorulan tuzak budur. `Map` ayrı köktür, `List` de değildir, indeksle erişilmez. Yine de koleksiyon **görünümleri** sunar: `keySet()` anahtarları `Set` olarak, `values()` değerleri `Collection` olarak, `entrySet()` ise anahtar-değer çiftlerini verir.

**Senaryodan yapıya geçiş** klasik sorudur. "Ekleme sırası korunsun + indeksle erişeyim + tekrar olabilir" → üç şartı birden yalnızca bir `List`, pratikte `ArrayList` karşılar; `HashSet`/`TreeSet` tekrar tutmaz ve indekslenmez, `HashMap` ise konumsal indeks sunmaz. "Anahtardan değere ortalama O(1) erişim, sıralama önemsiz" → `HashMap`; `TreeMap` sıralı tutar ama O(log n) maliyet getirir, `ArrayList`/`LinkedList` ise anahtar-değer eşlemesi sunmaz (arama O(n) lineer tarama olur). Ekleme sırasını da korumak gerekiyorsa `LinkedHashMap` iyi bir orta yoldur.

**İnce ayrımlar:** `HashSet` sırasızdır, `TreeSet` **ekleme sırasını değil doğal sırayı** tutar; `LinkedHashSet` ekleme sırasını korur. `Set`'e tekrar eklemek istisna atmaz, `add` `false` döner. `List`'in tekrar kabul etmesi bir kusur değil, tanımının parçasıdır — `Set`, `List`'in yeni sürümdeki adı değildir; ikisi farklı semantik taşır.

### Tek cümlede

`Collection` (List/Set/Queue) tek tek öğeleri, ayrı bir kök olan `Map` ise benzersiz anahtar → değer çiftlerini tutar; doğru yapıyı semantik (sıra/tekrar) ve maliyet (big-O) birlikte belirler.

# coll-1 | Temel
## Soru
`List` ile `Set` arasındaki temel fark nedir?
## A
`List` sıralı ve indekslidir, tekrar kabul eder; `Set` tekrar kabul etmez (benzersiz elemanlar)
🤖 Doğru: `List` = sıralı/tekrarlı, `Set` = benzersiz.
## B
`List` tekrar (duplicate) kabul etmez; `Set` aynı elemanı birden çok kez tutabilir
🤖 Tam tersi: `List` tekrarlıdır, `Set` benzersiz eleman tutar.
## C
`List` yalnızca ilkel (primitive) tipleri, `Set` yalnızca nesneleri tutabilir
🤖 İkisi de generic'tir ve her nesne tipini tutar; fark tip değil, tekrar/sıra semantiğidir.
## D
İkisi de aynı davranışa sahiptir; `Set`, `List`'in yeni sürümlerdeki adıdır
🤖 Anlamlı semantik fark vardır: tekrar ve indeks davranışı ayrışır; biri diğerinin adı değildir.
## Doğru
A
## Açıklama
`List` **sıralı** (elemanların ekleme sırası korunur), **indekslidir** (`get(i)` ile konuma erişilir) ve **tekrar (duplicate) elemanlara izin verir** — aynı değer birden çok kez bulunabilir (`ArrayList`, `LinkedList`). `Set` ise bir **küme** semantiği taşır: **tekrar kabul etmez**, aynı eleman en fazla bir kez bulunur (`HashSet`, `TreeSet`). "Bu değer zaten var mı?" veya "benzersiz olsun" gerektiğinde `Set`, "sıralı liste, indeksli erişim, tekrar olabilir" gerektiğinde `List` seçilir.

# coll-2 | Temel
## Soru
Aşağıdaki koddan sonra `s.size()` kaçtır?

```java
Set<String> s = new HashSet<>();
s.add("a");
s.add("b");
s.add("a");
```
## A
`2`
🤖 Doğru: `"a"`, `"b"` benzersiz; ikinci `"a"` yok sayılır, boyut 2.
## B
`3`
🤖 `Set` tekrarları saymaz; ikinci `"a"` yeni eleman eklemez.
## C
`1`
🤖 İki farklı eleman (`a`, `b`) var; boyut 1 olamaz.
## D
Çalışma-zamanı istisnası; `Set` tekrar eklenince hata verir
🤖 Tekrar ekleme istisna atmaz; `add` sessizce `false` döner ve boyut değişmez.
## Doğru
A
## Açıklama
`Set` benzersizlik garantisi verir: aynı elemanı ikinci kez eklemeye çalışmak **sessizce başarısız olur** (`add` `false` döner) ve kümeye yeni eleman katılmaz. İstisna fırlatılmaz (D yanlış). Burada `"a"` ve `"b"` eklenir (boyut 2), ikinci `s.add("a")` ise zaten var olduğu için yok sayılır. Bu yüzden `s.size()` **2**'dir. Benzersizlik `equals`/`hashCode` ile belirlenir; bu yüzden `Set`'e konan tipler bu ikisini tutarlı biçimde ezmelidir.

# coll-3 | Temel
## Soru
`Map` (örn. `HashMap`) için hangisi doğrudur?
## A
`Map`, `Collection` arayüzünü genişletir (extends)
🤖 `Map` `Collection`'ı genişletmez; ayrı bir hiyerarşidir.
## B
`Map` bir `List`'tir; indeksle erişilir
🤖 `Map` indeksle değil anahtarla erişilir; `List` değildir.
## C
`Map`, `Collection`'dan ayrı bir hiyerarşidir; anahtarlar benzersizdir
🤖 Doğru: `Map` ayrı köktür; benzersiz anahtarları değerlere eşler.
## D
`Map` tekrar eden anahtarlara izin verir; aynı anahtar iki kez saklanır
🤖 Anahtarlar benzersizdir; aynı anahtara `put` eski değeri günceller.
## Doğru
C
## Açıklama
`Map`, Collections Framework'ün **ayrı bir köküdür** — `Collection` arayüzünü GENİŞLETMEZ (A yanlış). Bunun nedeni yapısının farklı olmasıdır: tek tek eleman değil, **anahtar → değer** çiftleri tutar. Anahtarlar **benzersizdir**; aynı anahtara tekrar `put` yapmak yeni eleman eklemez, eski değeri **günceller** (D yanlış). İndeksle değil anahtarla erişilir (`get(anahtar)`). Yine de `Map`, koleksiyon görünümleri sunar: `keySet()` (anahtarlar `Set` olarak), `values()` (değerler `Collection`), `entrySet()` (çiftler). Bir şeyi başka bir şeye eşlemek (isim→telefon, id→nesne) gerektiğinde `Map` seçilir.

# coll-4 | Orta
## Soru
"Elemanların ekleme sırasını korumak istiyorum ve indeksle (konumla) erişeceğim, tekrarlar olabilir." Hangi yapı en uygundur?
## A
`HashSet`
🤖 `HashSet` sırasızdır, indeksle erişim yoktur ve tekrar tutmaz; üç gereksinimi de karşılamaz.
## B
`TreeSet`
🤖 `TreeSet` elemanları sıralar (ekleme sırasını değil), tekrar tutmaz, indekslemez.
## C
`HashMap`
🤖 `HashMap` anahtar-değer tutar, konumsal indeks yoktur; senaryoya uymaz.
## D
`ArrayList`
🤖 Doğru: `ArrayList` sıralı, indeksli (`get(i)`) ve tekrarlıdır.
## Doğru
D
## Açıklama
Gereksinimler: **ekleme sırası korunmalı**, **indeksle konumsal erişim** olmalı, **tekrarlara izin** verilmeli. Bunların üçünü birden karşılayan yapı bir `List`, pratikte de **`ArrayList`**'tir: dizi tabanlıdır, `get(i)` ile O(1) konumsal erişim sunar, ekleme sırasını korur ve tekrar elemanları kabul eder. `HashSet`/`TreeSet` (A, B) tekrar tutmaz ve indekslenemez; ayrıca `HashSet` sırasız, `TreeSet` doğal-sıraya göre dizilidir (ekleme sırası değil). `HashMap` (C) konumsal indeks değil anahtar-değer erişimi sunar. Sık konumsal erişim varsa `ArrayList`, sık baş/orta ekleme-silme varsa `LinkedList` düşünülebilir.

# coll-5 | Orta
## Soru
`ArrayList` için indeksle erişim (`get(i)`) ve sona ekleme (`add`) tipik big-O maliyetleri nedir?
## A
`get(i)` O(n), sona `add` O(n)
🤖 Dizi tabanlı `ArrayList`'te `get(i)` sabittir; O(n) değil.
## B
`get(i)` O(1), sona `add` amortize O(1)
🤖 Doğru: dizi indeksleme sabit; sona ekleme ortalama sabit (ara sıra yeniden boyutlanır).
## C
`get(i)` O(log n), sona `add` O(log n)
🤖 `ArrayList` ağaç değildir; logaritmik maliyet dengeli ağaç yapılarına özgüdür.
## D
`get(i)` O(1), sona `add` her zaman O(n)
🤖 Sona ekleme genelde sabittir; O(n) yalnız ara sıra (yeniden boyutlandırma) olur, amortize sabittir.
## Doğru
B
## Açıklama
`ArrayList` bir **dinamik dizi** üzerine kuruludur. `get(i)` doğrudan dizi indekslemesidir → **O(1)** (sabit). Sona ekleme (`add(e)`) genelde sabit zamanlıdır; yalnızca iç dizi dolduğunda daha büyük bir diziye kopyalama (yeniden boyutlandırma, O(n)) gerekir. Bu maliyet seyrek olduğundan ortalamada **amortize O(1)**'dir. Buna karşılık başa/ortaya ekleme-silme, sonraki tüm elemanları kaydırmayı gerektirdiği için O(n)'dir. Karşılaştırma: `LinkedList`'te uçlara ekleme O(1) ama indeksle erişim O(n)'dir — bu yüzden sık konumsal erişimde `ArrayList` tercih edilir.

# coll-6 | Orta
## Soru
Bir arama (lookup) için "verilen anahtardan değere ortalama O(1) erişim" gerekiyor ve sıralama önemsiz. Hangi yapı en uygundur?
## A
`ArrayList`
🤖 `ArrayList`'te değere göre arama O(n) tarama gerektirir; anahtar-değer eşlemesi yoktur.
## B
`HashMap`
🤖 Doğru: `HashMap` anahtardan değere ortalama O(1) erişim sunar, sıralama tutmaz.
## C
`TreeMap`
🤖 `TreeMap` anahtarları sıralı tutar ama erişimi O(log n)'dir; O(1) gerekliyse fazladan maliyet.
## D
`LinkedList`
🤖 `LinkedList` konumsal/sıralı listedir; anahtar-değer araması O(n)'dir.
## Doğru
B
## Açıklama
"Anahtardan değere hızlı erişim" bir `Map` işidir; sıralama önemsizse ve ortalama **O(1)** erişim isteniyorsa doğru seçim **`HashMap`**'tir: `hashCode` ile kovaya (bucket) doğrudan gider, ortalama sabit zamanda `get`/`put` yapar (kötü hash dağılımı veya çok çakışma nadiren bunu bozar). `TreeMap` (C) anahtarları **sıralı** tutar ama bu kırmızı-siyah ağaç yapısı yüzünden erişim **O(log n)**'dir — sıralama gerekmiyorsa gereksiz maliyettir. `ArrayList`/`LinkedList` (A, D) anahtar-değer eşlemesi sunmaz; bir değeri aramak lineer tarama (O(n)) gerektirir. Ekleme sırasını da korumak istersen `LinkedHashMap` uygun bir orta yoldur.
