---
slug: esitlik-vs-equals
baslik: "== vs equals: Referans Kimliği ve Değer Eşitliği"
kademe: Temel
sira: 7
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Elinde iki kimlik kartı var ve şunu soruyorsun: "aynı mı?" Sorunun iki farklı anlamı vardır. Birincisi: bu ikisi *fiziksel olarak tek bir kart* mı, yoksa iki ayrı kart mı? İkincisi: üstlerinde *aynı bilgiler* mi yazıyor? İki ayrı kart, aynı bilgileri taşıyor olabilir.

Java'da `==` birinci soruyu sorar (aynı nesne mi?), `equals` ikinci soruyu (aynı içerik mi?). Mülakatta en sık düşülen tuzak bu ikisini karıştırmaktır — çünkü Java bazen aynı içeriği tek nesnede paylaştırır ve `==` "yanlışlıkla" doğru sonuç verir.

### Temel kavramlar

- **Nesne (object)**: bellekte yer kaplayan gerçek veri.
- **Referans**: o nesnenin bellekteki adresini tutan değişken. Java'da nesne değişkenleri hep referanstır.
- **İlkel tip (primitive)**: `int`, `boolean`, `char`… — referans değil, doğrudan değer tutarlar.
- **`==`**: nesneler için **referans kimliği** (identity) — iki değişken aynı nesneyi mi gösteriyor? İlkel tiplerde ise düz değer karşılaştırmasıdır.
- **`equals`**: **mantıksal değer eşitliği** — iki nesnenin içeriği aynı mı? Varsayılan `Object.equals` yine referansa bakar; anlamlı olması için sınıfın onu **ezmesi** (override) gerekir.
- **Sarmalayıcı (wrapper)**: ilkel tipin nesne hali — `int` → `Integer`. `Integer a = 5;` yazımına **otomatik kutulama** (autoboxing) denir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="== referansa, equals içeriğe bakar">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki eşitlik, iki soru</text>
  <rect x="30" y="48" width="270" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="78" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">a == b</text>
  <text x="165" y="102" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">"Aynı nesne mi?" (referans)</text>
  <text x="165" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">iki kutu belleğin aynı</text>
  <text x="165" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">adresini mi tutuyor?</text>
  <rect x="340" y="48" width="270" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="78" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">a.equals(b)</text>
  <text x="475" y="102" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">"İçerik aynı mı?" (değer)</text>
  <text x="475" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sınıf ezmişse anlamlı;</text>
  <text x="475" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ezmemişse == gibi davranır</text>
</svg>
```

Pratik kural: **nesneleri karşılaştırırken `equals` kullan** (özellikle `String`, `Integer` gibi sarmalayıcılarda); `==`'yi yalnız ilkel tiplerde veya bilerek referans kimliği ararken kullan.

### Nasıl çalışır?

İki optimizasyon `==`'yi yanıltır.

**String havuzu (string pool / interning).** Aynı derleme zamanı literali tek bir nesnede saklanır ve tüm kullanımlar onu paylaşır:

```java
String a = "merhaba";
String b = "merhaba";
System.out.println(a == b);        // true — aynı havuz nesnesi
```

Ama `new String(...)` havuzu bilerek atlar; heap'te taze, kimseyle paylaşılmayan bir nesne yaratır:

```java
String a = new String("merhaba");
String b = new String("merhaba");
System.out.println(a == b);        // false — iki ayrı nesne
System.out.println(a.equals(b));   // true  — içerik aynı
```

Yani `"x" == new String("x")` → `false`. `new String(...)` pratikte gereksizdir; fazladan nesne yaratıp bellek ve zaman harcar, hız avantajı yoktur. Havuza katmak istersen `.intern()` çağrılır. Dikkat: havuz davranışı literaller içindir — çalışma zamanında birleştirilmiş (concatenated) stringler havuza girmez. Bu yüzden `==` ile string karşılaştırmak KIRILGANDIR.

**Integer önbelleği (cache).** Otomatik kutulama sırasında `-128..127` arasındaki değerler bir önbellekten (`IntegerCache`) paylaştırılır; aralık dışı her değer ayrı nesne olur:

```java
Integer a = 127, b = 127;
Integer c = 128, d = 128;
System.out.println(a == b);   // true  — önbellekten paylaşılır
System.out.println(c == d);   // false — aralık dışı, ayrı nesneler
```

Yani `==` sonucu **değere göre değişir**: bazen doğru, bazen yanlış. Sarmalayıcıları her zaman `equals` ya da `intValue()` ile karşılaştır.

```svg
<svg viewBox="0 0 660 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="String havuzu ve Integer önbelleği paylaşımı == sonucunu yanıltır">
  <defs>
    <marker id="eq-ok" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">== neden bazen "yanlışlıkla" doğru?</text>
  <rect x="20" y="36" width="308" height="184" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="174" y="58" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">String havuzu (pool)</text>
  <rect x="36" y="70" width="90" height="26" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="81" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">a="merhaba"</text>
  <rect x="36" y="104" width="90" height="26" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="81" y="122" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">b="merhaba"</text>
  <rect x="192" y="86" width="122" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="253" y="105" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">tek havuz nesnesi</text>
  <line x1="126" y1="83" x2="190" y2="97" stroke="#5D7789" stroke-width="1.4" marker-end="url(#eq-ok)"/>
  <line x1="126" y1="117" x2="190" y2="105" stroke="#5D7789" stroke-width="1.4" marker-end="url(#eq-ok)"/>
  <text x="174" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">a == b → true</text>
  <text x="174" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ama new String("merhaba")</text>
  <text x="174" y="186" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">→ ayrı nesne → == false</text>
  <text x="174" y="208" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">literaller paylaşılır, new paylaşılmaz</text>
  <rect x="336" y="36" width="304" height="184" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="488" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Integer önbelleği (-128..127)</text>
  <rect x="356" y="74" width="264" height="54" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="488" y="95" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Integer a=127, b=127</text>
  <text x="488" y="115" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">önbellekten paylaşılır → a==b true</text>
  <rect x="356" y="138" width="264" height="54" rx="8" fill="#FFFFFF" stroke="#B26B00" stroke-width="1.3"/>
  <text x="488" y="159" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Integer c=128, d=128</text>
  <text x="488" y="179" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">aralık dışı → ayrı nesne → c==d false</text>
  <text x="488" y="210" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">değere göre değişir → == güvenilmez</text>
</svg>
```

**`equals` / `hashCode` kontratı.** `equals`'i ezersen `hashCode`'u da ezmen gerekir. Kural: `a.equals(b)` doğruysa `a.hashCode() == b.hashCode()` OLMALIDIR.

Neden? `HashMap`/`HashSet` bir anahtarı ararken iki adım işletir: önce `hashCode` ile hangi kovaya (bucket) bakacağını seçer, sonra o kovanın içinde `equals` ile doğrular. `equals` ezilip `hashCode` ezilmezse, mantıken eşit iki nesne `Object.hashCode`'un kimlik tabanlı farklı değerlerini üretir; `put` ile konan anahtar `get` sırasında BAŞKA bir kovada aranır ve bulunamaz. `HashMap`'in "otomatik referans kimliğine dönüp tutarlı çalışması" diye bir davranış yoktur, "yalnız `equals` kullanır" da yanlıştır. Derleyici bu kontratı zorlamaz — sessiz bir mantık hatasıdır.

**Null güvenliği.** `equals`'i **null olamayacak tarafta** çağır:

```java
"admin".equals(kullaniciGirdisi)    // güvenli: girdi null olsa bile false döner
kullaniciGirdisi.equals("admin")    // girdi null ise NullPointerException
kullaniciGirdisi.toString()...      // toString de null üzerinde NPE fırlatır
```

`==` NPE atmaz ama referansa baktığından içerik karşılaştırması için yanlıştır. İki taraf da null olabiliyorsa `java.util.Objects.equals(a, b)` her ikisini de null-güvenli karşılaştırır.

```svg
<svg viewBox="0 0 660 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="equals hashCode kontratı ve HashMap neden ikisini de gerektirir">
  <defs>
    <marker id="eqhc-ok" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">equals / hashCode kontratı</text>
  <rect x="24" y="34" width="612" height="40" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="59" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">a.equals(b) doğruysa → a.hashCode() == b.hashCode() OLMALI</text>
  <rect x="24" y="88" width="150" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="99" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">map.get(anahtar)</text>
  <line x1="174" y1="108" x2="214" y2="108" stroke="#5D7789" stroke-width="1.5" marker-end="url(#eqhc-ok)"/>
  <rect x="218" y="88" width="192" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="314" y="106" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">1) hashCode() → kova seç</text>
  <text x="314" y="121" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hangi bucket?</text>
  <line x1="410" y1="108" x2="450" y2="108" stroke="#5D7789" stroke-width="1.5" marker-end="url(#eqhc-ok)"/>
  <rect x="454" y="88" width="182" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="545" y="106" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">2) equals() → doğrula</text>
  <text x="545" y="121" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kova içinde eşleştir</text>
  <rect x="24" y="142" width="612" height="60" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="330" y="164" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">equals ezilip hashCode ezilmezse (kontrat kırık)</text>
  <text x="330" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">eşit sayılan iki nesne FARKLI kovaya düşer → put edilen anahtar</text>
  <text x="330" y="198" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">get sırasında bulunamaz (sessiz mantık hatası)</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`new String("x")` ile `"x"` farkı?"** → İlki havuz dışında taze nesne, ikincisi havuzda paylaşılan nesne; `==` sonucu değişir, `equals` ikisinde de içeriğe bakar.
- **"`Integer a=127,b=127` ve `c=128,d=128`; `==` ne döner?"** → `true` ve `false`; sebep `-128..127` önbelleği.
- **"`equals` ezildi, `hashCode` ezilmedi — ne olur?"** → Eşit nesneler farklı kovalara düşer, `map.get` konan değeri bulamaz. Derleme hatası değil, sessiz mantık hatası.
- **"Null olabilecek girdiyi nasıl karşılaştırırsın?"** → `"admin".equals(girdi)` ya da `Objects.equals(a, b)`.
- **"`==` ne zaman doğru araçtır?"** → İlkel tiplerde ve bilerek "aynı nesne mi?" sorulduğunda (ör. enum karşılaştırması).

### Tek cümlede

`==` "aynı nesne mi?", `equals` "aynı içerik mi?" diye sorar; String havuzu ve Integer önbelleği `==`'yi bazen kazara doğru gösterdiği için nesnelerde daima `equals` kullanılır — ve `equals` ezilince `hashCode` da ezilmelidir.

# eq-1 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
String a = new String("merhaba");
String b = new String("merhaba");
System.out.println(a == b);
System.out.println(a.equals(b));
```
## A
`true` ve `true`
🤖 `==` referansa bakar; `new String` her seferinde AYRI nesne yarattığı için ilk satır `true` olamaz.
## B
`false` ve `false`
🤖 İçerikleri aynı; `String.equals` içeriğe bakar, ikinci satır `true` olmalı.
## C
`false` ve `true`
🤖 Doğru: iki ayrı nesne (`==` false) ama aynı içerik (`equals` true).
## D
`true` ve `false`
🤖 Tam ters: `new String` ile referanslar farklı, içerik ise aynı.
## Doğru
C
## Açıklama
`new String("merhaba")` her çağrıda havuzdan (pool) bağımsız, YENİ bir nesne yaratır. Bu yüzden `a` ve `b` farklı nesnelerdir → `a == b` **false**. Ancak `String.equals` karakter dizisinin içeriğini karşılaştırır; içerikler aynı olduğundan `a.equals(b)` **true**. Çeldiriciler `==`'nin içeriğe baktığını (A, D) veya `String.equals`'in referansa baktığını (B) varsayan klasik yanılgılardır.

# eq-2 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
String a = "merhaba";
String b = "merhaba";
System.out.println(a == b);
```
## A
`true`
🤖 Doğru: derleme-zamanı literalleri string havuzunda (pool) tek nesnede paylaşılır.
## B
`false`
🤖 `new` yok; iki literal aynı havuz nesnesini gösterir, bu yüzden referanslar eşittir.
## C
Derleme hatası verir
🤖 Kod geçerli; `String` literalleri normal atamadır.
## D
Çalışma-zamanı istisnası (exception) fırlatır
🤖 Hiçbir istisna yok; sade bir karşılaştırma.
## Doğru
A
## Açıklama
Aynı derleme-zamanı `String` literali **string havuzunda** (string pool / interning) tek bir nesnede saklanır ve tüm kullanımlar bu tek nesneyi paylaşır. Bu yüzden `a` ve `b` aynı referanstır → `a == b` **true**. Dikkat: bu davranış literaller için geçerlidir; `new String(...)` veya çalışma-zamanında birleştirilmiş (concatenated) stringler havuza girmez. Bu yüzden `==` ile string karşılaştırmak KIRILGANDIR — her zaman `equals` kullanılmalıdır.

# eq-3 | Orta
## Soru
`new String("x")` ile `"x"` literali arasındaki temel fark nedir?
## A
İkisi de string havuzundaki aynı nesneyi gösterir
🤖 `new String` bilerek havuz DIŞINDA yeni nesne yaratır; aynı olamazlar.
## B
`new String("x")` her zaman havuz dışında yeni bir nesne yaratır; literal ise havuzdaki paylaşılan nesnedir
🤖 Doğru: `new` heap'te taze nesne verir, literal havuzda paylaşılır.
## C
`new String("x")` daha hızlıdır çünkü havuzu atlar
🤖 Aksine; gereksiz nesne yaratıp bellek ve zaman harcar, hız avantajı yoktur.
## D
İkisi arasında hiçbir fark yoktur, derleyici ikisini de aynı bytecode'a çevirir
🤖 Fark davranışsaldır: biri paylaşılan havuz nesnesi, biri taze nesne — `==` sonucu değişir.
## Doğru
B
## Açıklama
`"x"` literali **string havuzundaki** paylaşılan nesneyi verir; aynı literali kullanan herkes aynı referansı alır. `new String("x")` ise `x` içeriğiyle **heap'te taze bir nesne** yaratır — havuza girmez, kimseyle paylaşılmaz. Bu yüzden `"x" == new String("x")` **false** olur. `new String(...)` pratikte gereksizdir (fazladan nesne yaratır); istisnai olarak bir referansın havuzdan kesin bağımsız olmasını istediğin nadir durumlar dışında kullanılmaz. Havuza katmak istersen `.intern()` çağrılır.

# eq-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
Integer a = 127, b = 127;
Integer c = 128, d = 128;
System.out.println(a == b);
System.out.println(c == d);
```
## A
`true` ve `true`
🤖 128 önbellek aralığının (-128..127) dışında; ikinci satır `true` olamaz.
## B
`false` ve `false`
🤖 127 Integer önbelleğindedir; ilk satır `true` olmalı.
## C
`true` ve `false`
🤖 Doğru: 127 önbellekli (aynı nesne), 128 aralık dışı (ayrı nesneler).
## D
`false` ve `true`
🤖 Tam ters: küçük değer paylaşılır, büyük değer paylaşılmaz.
## Doğru
C
## Açıklama
`Integer` otomatik kutulama (autoboxing) sırasında **-128..127** arasındaki değerleri bir önbellekten (`IntegerCache`) paylaştırır. `a` ve `b` (127) aynı önbellek nesnesini gösterir → `a == b` **true**. `c` ve `d` (128) aralığın dışında olduğu için her biri AYRI nesne kutulanır → `c == d` **false**. Bu, `Integer`'ları `==` ile karşılaştırmanın neden tehlikeli olduğunun klasik örneğidir: değere göre bazen doğru, bazen yanlış sonuç verir. Sarmalayıcı (wrapper) tipleri her zaman `equals` veya `intValue()` ile karşılaştır.

# eq-5 | İleri
## Soru
Bir sınıfta `equals` ezildi ama `hashCode` ezilmedi. Bu nesneler `HashMap` anahtarı olarak kullanılınca en olası sonuç nedir?
## A
Derleme hatası; `equals` ezince `hashCode` da zorunlu olur
🤖 Derleyici bunu zorlamaz; sorun çalışma-zamanında sessizce ortaya çıkar.
## B
`HashMap` otomatik olarak referans kimliğine döner ve tutarlı çalışır
🤖 Böyle bir geri dönüş yok; tutarsız `hashCode` bozuk davranış üretir.
## C
Hiçbir sorun olmaz; `HashMap` yalnız `equals` kullanır
🤖 `HashMap` önce `hashCode` ile kovayı seçer; bu yüzden `hashCode` şarttır.
## D
Eşit sayılan iki nesne farklı kovalara (bucket) düşebilir; `map.get` konan değeri bulamaz
🤖 Doğru: kontrat kırıldığı için arama yanlış kovaya bakar.
## Doğru
D
## Açıklama
`equals`/`hashCode` **kontratı**: iki nesne `equals`'e göre eşitse `hashCode`'ları da eşit OLMALIDIR. `equals` ezilip `hashCode` ezilmezse, mantıken eşit iki nesne `Object.hashCode`'un (kimlik tabanlı) farklı değerlerini üretir. `HashMap` önce `hashCode` ile kovayı seçtiğinden, `put` ile konan anahtar `get` sırasında farklı bir kovada aranır ve **bulunamaz**. Derleyici bunu zorlamaz (C/A yanlış); sessiz bir mantık hatasıdır. Kural: `equals`'i ezersen `hashCode`'u da ez (ikisini birlikte, tutarlı üret).

# eq-6 | İleri
## Soru
`kullaniciGirdisi` `null` olabilir. Aşağıdakilerden hangisi `"admin"` ile karşılaştırmayı `NullPointerException` riski olmadan yapar?
## A
`kullaniciGirdisi.equals("admin")`
🤖 `kullaniciGirdisi` null ise burada NPE fırlar — sol taraf null olamaz.
## B
`"admin".equals(kullaniciGirdisi)`
🤖 Doğru: sabit literal üzerinde çağrı; argüman null olsa bile `equals` false döner, NPE olmaz.
## C
`kullaniciGirdisi == "admin"`
🤖 NPE atmaz ama `==` referansa bakar; içerik eşitliğini güvenilir ölçmez.
## D
`kullaniciGirdisi.toString().equals("admin")`
🤖 `toString` de null üzerinde NPE fırlatır; sorun çözülmez.
## Doğru
B
## Açıklama
`equals`'i **null olamayacak tarafta** çağırmak en sağlam desendir: `"admin".equals(kullaniciGirdisi)` — literal asla null değildir ve `equals` argümanı null ise sessizce `false` döner, NPE olmaz. `kullaniciGirdisi.equals(...)` (A) ve `kullaniciGirdisi.toString()...` (D) null referans üzerinde çağrı yaptığı için NPE riski taşır. `==` (C) NPE atmaz ama referans kimliğine baktığından içerik karşılaştırması için yanlıştır. Alternatif olarak `java.util.Objects.equals(a, b)` her iki tarafı da null-güvenli karşılaştırır.
