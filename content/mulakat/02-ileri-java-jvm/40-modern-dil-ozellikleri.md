---
slug: modern-dil-ozellikleri
baslik: "Modern Dil Özellikleri: Text Block, var, switch Expression"
kademe: Orta
sira: 40
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir mektubu daktiloyla yazdığını düşün: her satır sonunda kolu çekmek, her tırnak işaretini özel bir tuş kombinasyonuyla basmak zorundasın. Sonra bilgisayar geliyor ve yazdığın şey ekranda göründüğü gibi kaydediliyor. Modern Java'nın (11–21) getirdiği söz dizimi yenilikleri de böyle: **anlamı değiştirmez, yazma ve okuma zahmetini azaltır.** Çok satırlı metni olduğu gibi yazmak, değişkenin tipini iki kez tekrarlamamak, `switch`'ten doğrudan bir değer almak — hepsi aynı amaca hizmet eder: koddaki gürültüyü azaltıp niyeti öne çıkarmak.

### Temel kavramlar

- **Değişmez (literal):** kodun içine doğrudan yazılan sabit değer: `42`, `"merhaba"`.
- **Text block** (Java 15): üç tırnakla (`"""`) açılan **çok satırlı String değişmezi**. Kaçış karakteri (`\n`, `\"`) ve `+` birleştirmesi olmadan yazılır.
- **Kaçış karakteri (escape):** metnin içinde özel anlamı olan karakterleri yazabilmek için kullanılan `\` önekli gösterim.
- **Tip çıkarımı (type inference):** derleyicinin, tipi sen yazmadan ifadeden anlaması.
- **`var`** (Java 10): **yerel değişken** tip çıkarımı. Tip sağ taraftaki başlatıcıdan çıkarılır.
- **Deyim (statement) / ifade (expression):** deyim bir iş yapar, ifade bir **değer üretir**. `switch expression` (Java 14), `switch`'i deyimden ifadeye çeviren yeniliktir.
- **Fall-through:** klasik `switch`'te bir `case`'den sonrakine "düşme"; `break` unutulunca oluşan klasik hata kaynağı.
- **`yield`:** blok gövdeli bir `switch` dalından değer döndüren deyim.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="var ne zaman kullanılır ne zaman kullanılmaz">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">var: nerede geçerli, nerede değil</text>
  <rect x="30" y="46" width="270" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">GEÇERLİ: yerel değişken</text>
  <text x="165" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">var liste = new ArrayList&lt;String&gt;();</text>
  <text x="165" y="124" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">for (var e : liste) ...</text>
  <text x="165" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tip sağ taraftan çıkarılır</text>
  <text x="165" y="174" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">başlangıç değeri ZORUNLU</text>
  <rect x="340" y="46" width="270" height="150" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#B4322B" font-family="system-ui">GEÇERSİZ</text>
  <text x="475" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">alan (field) tipinde</text>
  <text x="475" y="124" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">metot parametresinde</text>
  <text x="475" y="150" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">dönüş tipinde</text>
  <text x="475" y="174" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">var x = null; (çıkarım yok)</text>
</svg>
```

### Nasıl çalışır?

**Text block** bir **derleme zamanı** özelliğidir: sonuçta sıradan, değişmez bir `String` üretir. Şifreleme yapmaz, çalışma zamanında hız kazandırmaz ve String'e yeni bir değişmezlik katmaz — Java'da tüm String'ler zaten değişmezdir. Kazanç tamamen okunabilirliktir: gömülü JSON, SQL ya da HTML'de `\n` ve `\"` kalabalığı ortadan kalkar. Girinti (indentation) otomatik ayarlanır: kapanış `"""` işaretinin konumuna göre gereksiz baştaki boşluklar (incidental whitespace) çıkarılır.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="text block kaçış cehennemini ortadan kaldırır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Text block: kaçış cehennemi → okunur çok satır</text>
  <rect x="30" y="46" width="275" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Klasik String</text>
  <text x="167" y="100" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">"{\n \"ad\": \"Ada\",\n" +</text>
  <text x="167" y="122" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">" \"yas\": 30\n}"</text>
  <text x="167" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaçış ve + kalabalığı</text>
  <rect x="335" y="46" width="275" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Text block (üç tırnak)</text>
  <text x="472" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">{</text>
  <text x="472" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">  "ad": "Ada", "yas": 30</text>
  <text x="472" y="138" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">}</text>
  <text x="472" y="164" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kaçış YOK, olduğu gibi yazılır</text>
</svg>
```

**`var`**, değişkenin tipini sağ taraftaki başlatıcı ifadeden çıkarır ve **derleme anında sabitler**. `var liste = new ArrayList<String>();` satırında `liste`'nin tipi tam olarak `ArrayList<String>`'tir — `Object` değildir (döküm gerekmez), dinamik tip de değildir: Java statik tipli bir dil olmaya devam eder ve `liste`'ye sonradan `int` atayamazsın. `var` otomatik `final` de **değildir**; değişken yeniden atanabilir, `final` istiyorsan `final var` yazarsın.

Kullanım sınırları nettir: `var` **yalnız yerel değişkenlerde** geçerlidir — alanlarda (field), metot parametrelerinde ve dönüş tiplerinde kullanılamaz. Başlatıcı **zorunludur** (`var x;` geçersiz) ve tipin çıkarılamadığı durumlar geçersizdir (`var x = null;`).

`var`'ın altın kuralı okunurluktur: **tip sağ taraftan açıkça anlaşılıyorsa kullan, anlaşılmıyorsa kullanma.** `var m = new HashMap<String, Integer>()`, `var s = new TreeSet<String>()` ve `for (var e : liste)` güvenlidir; tip zaten gözle görülür, `var` yalnız tekrarı kaldırır. Ama `var x = servis.getir();` gibi dönüş tipi isminden anlaşılmayan çağrılarda tip bilgisi okuyucudan gizlenir ve bakım zorlaşır; orada açık tip bildirimi yeğdir. Ayrı bir tuzak: `var`'ı elmas operatörüyle (diamond) birleştirmek. `var m = new HashMap<>();` **derlenir** ama tip `HashMap<Object, Object>` olarak çıkarılır; burada tip argümanlarını açıkça yazmak gerekir.

**switch expression**, `switch`'i bir **değer üreten** yapıya dönüştürür ve doğrudan bir değişkene atanabilir. Ok sözdiziminde (`case A, B -> ...`) her dal bağımsızdır: **fall-through yoktur**, dolayısıyla `break` de gerekmez. Birden çok etiket virgülle birleştirilebilir. Dal gövdesi bir **blok** ise değeri `yield` ile döndürürsün:

```java
int gunSayisi = switch (ay) {
    case OCAK, MART, MAYIS, TEMMUZ, AGUSTOS, EKIM, ARALIK -> 31;
    case NISAN, HAZIRAN, EYLUL, KASIM -> 30;
    case SUBAT -> {
        int g = artikYil ? 29 : 28;
        yield g;                    // blok gövdeden değer döndür
    }
};
```

Blok gövdede "son ifade örtük olarak döner" diye bir davranış **yoktur**; değeri açıkça `yield` etmelisin. `return` switch'ten değil **metottan** çıkar, bu amaçla kullanılamaz. Değer taşıyan `break deger;` biçimi Java 12–13 önizlemelerinde denenmiş ama terk edilmiştir; bugün geçersizdir, yerini tam olarak `yield` almıştır. switch expression yalnız `int` ile sınırlı değildir: `String`, `enum` ve Java 21'de tip desenleriyle de çalışır. Performansı klasik `switch`'le benzerdir; "daima daha yavaş" iddiası yanlıştır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="klasik switch deyimi ile switch expression karşılaştırması">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">switch deyimi vs switch expression</text>
  <rect x="30" y="46" width="275" height="145" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Klasik deyim (statement)</text>
  <text x="167" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">case A: x = 1; break;</text>
  <text x="167" y="120" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">break UNUTULURSA fall-through</text>
  <text x="167" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">değer üretmez, dışta atanır</text>
  <text x="167" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sık hata kaynağı</text>
  <rect x="335" y="46" width="275" height="145" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Expression (->)</text>
  <text x="472" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">case A -> 1;</text>
  <text x="472" y="120" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">fall-through YOK, break YOK</text>
  <text x="472" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">DEĞER üretir; blokta yield</text>
  <text x="472" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">int x = switch(...) { ... };</text>
</svg>
```

**Enhanced `instanceof`** ise tip testi ile bağlamayı tek adımda yapar: `if (o instanceof String s)`. Buradaki `s`'nin kapsamı **akışa duyarlıdır (flow-sensitive)**: yalnız `o instanceof String` koşulunun kesinlikle DOĞRU olduğu kod yollarında görünür. `if (o instanceof String s) { /* s burada var */ }` içinde kullanılabilir. Ayrıca `if (!(o instanceof String s)) return; /* buradan sonra s var */` gibi erken dönüşlerde, negatif testten sonraki yolda da görünür. Çünkü derleyici o noktada testin geçmiş olması gerektiğini ispatlayabilir. `s` sınıf boyunca ya da metodun tamamında koşulsuz erişilebilir değildir ve `else` dalında (testin başarısız olduğu yol) görünmez.

### Mülakatta ne sorulur?

**"Text block hangi sorunu çözer?"** — Çok satırlı metni kaçış karakterleri ve `+` olmadan okunur yazmayı. Şifreleme, hız ya da yeni bir değişmezlik değil.

**"`var` dinamik tip mi?"** — Hayır. Statik tip, derleme anında çıkarılıp sabitlenir; `Object` değildir; `final` anlamına gelmez; başlatıcı zorunludur.

**"`var`'ı ne zaman kullanmazsın?"** — Sağ taraf tipi belirsizken (`var x = servis.getir();`) ve elmas operatörüyle (`var m = new HashMap<>();` → `HashMap<Object,Object>`).

**"Ok sözdizimli switch'in farkı?"** — DEĞER üretir, fall-through yoktur, `break` gerekmez. Klasik deyimde `break` unutulunca bir sonraki `case`'e düşülür; ok sözdiziminde böyle bir risk kalmaz.

**"Blok gövdeden değer nasıl döner?"** — Yalnız `yield` ile. `return` metottan çıkar, `break deger;` terk edilmiştir, örtük dönüş yoktur.

**"Pattern değişkeninin kapsamı?"** — Akışa duyarlı; yalnız testin garantili geçtiği yollarda görünür.

### Tek cümlede

Text block, `var`, switch expression ve enhanced `instanceof`, Java'nın anlamını değil yazım gürültüsünü değiştiren yeniliklerdir; her birinin kazancı okunurluk, her birinin bedeli ise kuralını bilmemektir (`var`'ın elmas tuzağı, `yield` zorunluluğu, akışa duyarlı kapsam gibi).

# mdl-1 | Temel
## Soru
Text block (`"""..."""`) temel olarak hangi sorunu çözer?
## A
Metinleri otomatik olarak şifreler (encryption), kaynak koddaki gizli değerleri korur
🤖 Şifreleme yapmaz; text block yalnız çok satırlı metin yazımını kolaylaştırır.
## B
String'leri çalışma zamanında daha hızlı birleştirir, `+` zincirinin maliyetini ortadan kaldırır
🤖 Performans amacı değil; asıl fayda çok satırlı okunur metin yazımıdır.
## C
String'i değişmez (immutable) yapar, böylece içeriği sonradan değiştirilemez
🤖 Java'da tüm String'ler zaten değişmezdir; text block bunu değiştirmez.
## D
Çok satırlı metinleri, kaçış karakterleri (`\n`, `\"`) ve birleştirme (`+`) olmadan okunur biçimde yazmayı sağlar
🤖 Doğru: text block, gömülü JSON/SQL/HTML'yi kaçış cehennemi olmadan yazmak içindir.
## Doğru
D
## Açıklama
**Text block**, üç tırnakla (`"""`) açılan çok satırlı bir String değişmezidir (literal). Temel faydası, çok satırlı metinleri **kaçış karakterleri ve `+` birleştirmesi olmadan** doğrudan, okunur biçimde yazmaktır (D doğru): gömülü JSON, SQL, HTML gibi metinlerde `\n` ve `\"` kalabalığı ortadan kalkar. Girinti (indentation), kapanış `"""`'nin konumuna göre otomatik ayarlanır (incidental whitespace çıkarılır). Text block bir **derleme zamanı** özelliğidir; sonuçta sıradan, değişmez bir `String` üretir — şifreleme (A), çalışma zamanı hız kazancı (B) ya da yeni bir değişmezlik (C — String zaten değişmez) sağlamaz. Yani kazanım tamamen okunabilirliktir.

# mdl-2 | Temel
## Soru
`var liste = new ArrayList<String>();` satırında `var` ne yapar?
## A
`liste` değişkenini dinamik tipli yapar; daha sonra ona `String` ya da `int` gibi başka tipte değerler de atanabilir
🤖 Yanlış; Java statik tipli kalır, `liste`'nin tipi sağdan çıkarılıp derleme anında sabitlenir.
## B
`liste`'yi `Object` tipinde bildirir; eleman almak için her seferinde `(String)` dökümü gerekir
🤖 Yanlış; çıkarılan tip `Object` değil, tam olarak `ArrayList<String>`'tir; döküm gerekmez.
## C
Değişkenin tipini sağ taraftaki ifadeden çıkarır (type inference); tip yine statiktir ve `ArrayList<String>` olur
🤖 Doğru: var derleme zamanında tipi çıkarır, çalışma zamanı davranışı değişmez.
## D
Değişkeni örtük olarak değişmez (final) yapar; `liste`'ye ikinci kez atama yapılamaz
🤖 `var` final anlamına gelmez; değişken yeniden atanabilir (final istersen `final var` yazılır).
## Doğru
C
## Açıklama
`var`, **yerel değişken tip çıkarımıdır (local variable type inference)**: derleyici, değişkenin tipini SAĞ taraftaki başlatıcı ifadeden çıkarır ve sabitler. `var liste = new ArrayList<String>()` satırında `liste`'nin tipi tam olarak `ArrayList<String>` olur — dinamik tip (A) değil, `Object` (B) değil (C doğru). Java statik tipli bir dil olmaya devam eder; `var` yalnız yazımı kısaltır, çalışma zamanı davranışını değiştirmez. `var` otomatik `final` DEĞİLDİR (D yanlış); değişken yeniden atanabilir. `var`'ın çalışması için başlatıcı ZORUNLUDUR (`var x;` geçersiz) ve `var x = null;` gibi tipin çıkarılamadığı durumlar da geçersizdir. Okunurluk için `var` en iyi, sağ taraf tipi açıkça belli olduğunda kullanılır.

# mdl-3 | Orta
## Soru
`var` kullanımı aşağıdaki durumların hangisinde okunurluğu DÜŞÜRÜR ve kaçınılması önerilir?
## A
Sağ taraf açık bir constructor çağrısıyken (`var m = new HashMap<String, Integer>()`)
🤖 Burada tip zaten sağda görünür; var okunurluğu düşürmez, iyi bir kullanımdır.
## B
Sağ taraf tipi belirsiz bir metot çağrısıyken (`var x = servis.getir();`) — okuyan kişi `x`'in tipini göremez
🤖 Doğru: dönüş tipi isimden anlaşılmıyorsa var okunurluğu düşürür, açık tip yeğdir.
## C
`for-each` döngüsünde koleksiyon elemanı üzerinde (`for (var e : liste)`)
🤖 Eleman tipi koleksiyondan bellidir; var burada güvenli ve okunurdur.
## D
Tip argümanları sağda açıkça yazılıyken (`var s = new TreeSet<String>()`)
🤖 Tam tip sağda yazılı olduğundan okuyucu `s`'nin ne olduğunu görür; var burada netliği bozmaz.
## Doğru
B
## Açıklama
`var`'ın altın kuralı: **tip sağ taraftan açıkça anlaşılıyorsa kullan, anlaşılmıyorsa kullanma.** `var x = servis.getir();` gibi bir satırda `getir()`'in dönüş tipi isminden belli değilse, okuyan kişi `x`'in ne olduğunu göremez; bu okunurluğu DÜŞÜRÜR (B doğru) ve burada açık tip bildirimi tercih edilmelidir. Buna karşılık `var m = new HashMap<String, Integer>()` (A), `for (var e : liste)` (C) ve `var s = new TreeSet<String>()` (D) durumlarında tip zaten gözle görülür; `var` yalnız tekrarı kaldırır ve güvenlidir. Yani `var` bir "her yerde kullan" özelliği değil, okuyucunun tipi kolayca çıkarabildiği yerlerde tekrarı azaltan bir araçtır. Kötü kullanımı, koddaki tip bilgisini gizleyerek bakımı zorlaştırır. Ayrı bir tuzak: `var m = new HashMap<>();` gibi elmas operatörüyle (diamond) yazıldığında tip `HashMap<Object, Object>` olarak çıkarılır — burada tip argümanlarını açıkça yazmak gerekir.

# mdl-4 | Orta
## Soru
Ok sözdizimli (`case ... ->`) switch expression'ın klasik `switch` deyimine göre en önemli farkı nedir?
## A
Bir DEĞER üretir, fall-through yoktur (her dal bağımsız) ve `break` gerektirmez
🤖 Doğru: switch expression değer döndürür, ok dalları arası düşme (fall-through) olmaz, break gereksizdir.
## B
Yalnız `int` tipiyle çalışır, başka tip kabul etmez
🤖 Yanlış; String, enum ve pattern dahil birçok tiple çalışır.
## C
Çalışma zamanında klasik switch'ten daima daha yavaştır
🤖 Performans farkı iddiası yanlış; asıl fark söz dizimi ve değer üretmesidir.
## D
Her `case`'den sonra mutlaka `break` yazılmasını zorunlu kılar
🤖 Tam tersi; ok sözdiziminde break YAZILMAZ, fall-through zaten yoktur.
## Doğru
A
## Açıklama
**switch expression**, `switch`'i bir deyimden (statement) bir **ifadeye (expression)** dönüştürür: bir DEĞER üretir ve doğrudan bir değişkene atanabilir. Ok sözdiziminde (`case A, B -> ...`) her dal bağımsızdır; klasik switch'in en sık hata kaynağı olan **fall-through** (bir case'den diğerine düşme) YOKTUR ve bu yüzden `break` gerekmez (A doğru, D yanlış). Birden çok etiket virgülle birleştirilebilir. Blok gövdeli dallarda değer döndürmek için `yield` kullanılır. switch expression `int` dışında `String`, `enum` ve Java 21'de type pattern ile de çalışır (B yanlış). Performansı klasik switch'le benzerdir; "daima daha yavaş" iddiası (C) yanlıştır. Asıl kazanım: daha güvenli (kazara fall-through yok), daha kısa ve exhaustiveness denetimine açık kod.

# mdl-5 | İleri
## Soru
switch expression'da bir `case` dalının gövdesi bir blok (`{ }`) ise, o bloktan değer döndürmek için ne kullanılır?
## A
`return` deyimi
🤖 `return` metottan çıkar; switch dalından değer döndürmez, yanlış davranış üretir.
## B
`break deger;` biçiminde değer taşıyan bir `break`
🤖 Bu, önizleme sürümlerinde denenip terk edilmiş bir kalıptır; bugün geçersizdir, yerini `yield` almıştır.
## C
`yield deger;` deyimi
🤖 Doğru: blok gövdeli switch dalından değer döndürmenin yolu `yield`'dir.
## D
Sadece dalın son satırına değeri yazmak yeterlidir
🤖 Blok gövdede örtük dönüş yoktur; değeri açıkça `yield` ile döndürmen gerekir.
## Doğru
C
## Açıklama
switch expression'da tek ifadelik dallar `case X -> ifade` biçiminde doğrudan değer verir. Ancak dal gövdesi bir **blok** (`{ ... }`) ise — örneğin ara hesaplama gerektiğinde — değeri döndürmek için **`yield`** kullanılır: `yield hesaplananDeger;` (C doğru). `return` (A) switch'ten değil METOTTAN çıkar ve switch expression içinde bu amaçla kullanılamaz. Değer taşıyan `break deger;` (B) Java 12–13 önizlemelerinde denenmiş ama terk edilmiştir; bugün geçersizdir ve yerini tam olarak `yield` almıştır. Blok gövdede örtük "son ifade döner" davranışı YOKTUR (D yanlış); değeri açıkça `yield` etmelisin. Kural nettir: tek ifadelik dal `->` ile doğrudan, blok gövdeli dal ise yalnız `yield` ile değer üretir.

# mdl-6 | İleri
## Soru
Enhanced `instanceof` (`if (o instanceof String s)`) ile bağlanan `s` değişkeninin kapsamı (scope) hakkında hangisi doğrudur?
## A
`s`, tüm sınıf boyunca, diğer metotlarda bile her yerde görünür
🤖 Yanlış; pattern değişkeni akışa duyarlı olarak yalnız testin doğru olduğu yerlerde görünür.
## B
`s`, yalnız `else` dalında, yani testin başarısız olduğu yolda kullanılabilir
🤖 Tam tersi; `s`, testin BAŞARILI olduğu (then) yolda görünür, else'te değil.
## C
`s`, metodun geri kalanında, `if` bloğundan sonra da her koşulda erişilebilir olur
🤖 Yanlış; kapsamı akış analizine bağlıdır, test başarısız yollarda erişilemez.
## D
`s`, akışa duyarlı (flow-sensitive) biçimde yalnız testin DOĞRU olduğu kod yollarında görünür
🤖 Doğru: pattern değişkeninin kapsamı, testin kesinlikle geçtiği kod yollarıyla sınırlıdır.
## Doğru
D
## Açıklama
Enhanced `instanceof` pattern değişkeninin kapsamı **akışa duyarlıdır (flow-sensitive)**: `s`, yalnız `o instanceof String` koşulunun kesinlikle DOĞRU olduğu kod yollarında görünür (D doğru). Örneğin `if (o instanceof String s) { /* s burada var */ }` içinde kullanılabilir; ayrıca `if (!(o instanceof String s)) return; /* buradan sonra s var */` gibi erken dönüşlerde, negatif testten sonraki yolda da görünür — çünkü derleyici o noktada testin geçmiş olması gerektiğini ispatlar. `s` sınıf boyunca (A) veya metodun tamamında koşulsuz (C) görünmez; ayrıca `else` dalında (test başarısız) da erişilemez (B yanlış). Bu akış duyarlı kapsam, pattern değişkenlerini hem güvenli hem de doğal kılar: yalnız tipin garantili olduğu yerlerde erişilebilirler.
