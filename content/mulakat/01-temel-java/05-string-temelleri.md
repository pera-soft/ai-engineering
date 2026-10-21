---
slug: string-temelleri
baslik: "String Temelleri: Değişmezlik, Havuz, intern ve Derleyici Davranışı"
kademe: Temel
sira: 5
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Şöyle düşün: bir kâğıda adını yazdın ve yüz kişiye dağıttın. Kâğıt **silinemez** cinstense herkes gönül rahatlığıyla aynı kâğıdı paylaşabilir; kimse başkasının kopyasını bozamaz. Silinebilir olsaydı ya herkese ayrı kopya çıkarman ya da sürekli kontrol etmen gerekirdi. Java'da `String` işte o silinemez kâğıttır. Bu tek karar — **değişmezlik** — güvenliği, paylaşımı ve hızı beraberinde getirir; bedelini ise metin birleştirirken ödersin.

### Temel kavramlar

- **Değişmez (immutable)**: yaratıldıktan sonra içeriği **asla değişmeyen** nesne. `String` böyledir; hiçbir metodu mevcut nesnenin içeriğini değiştiremez.
- **Metot yenisini döndürür**: `s.toUpperCase()` var olanı değiştirmez, **yeni** bir `String` üretip döndürür.
- **Literal**: kaynak kodda tırnak içinde doğrudan yazılan sabit metin — `"abc"`.
- **String havuzu (string pool / interning)**: JVM'in aynı içerikli string literallerini tek nesnede paylaştırdığı özel alan.
- **Sabit katlama (constant folding)**: derleyicinin, derleme anında hesaplanabilen ifadeleri (`"ab" + "cd"`) tek bir sabite indirmesi.
- **`==` ve `equals`**: `==` "aynı nesne mi?", `equals` "aynı içerik mi?" diye sorar. (Ayrıntısı ayrı derste; burada `String`'in kendine özgü davranışına odaklan.)

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="String değişmez: metot yeni nesne döndürür">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">String değişmez: metot ESKİYİ değiştirmez, YENİ döndürür</text>
  <rect x="40" y="52" width="200" height="66" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="140" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">s = "abc"</text>
  <text x="140" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değişmez, aynı kalır</text>
  <rect x="400" y="52" width="200" height="66" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">"ABC" (yeni nesne)</text>
  <text x="500" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">s.toUpperCase() sonucu</text>
  <path d="M240 85 L400 85" stroke="#186399" stroke-width="1.8" marker-end="url(#str05)"/>
  <text x="320" y="76" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">toUpperCase()</text>
  <text x="320" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Sonucu bir değişkene atamazsan kaybolur (s değişmedi)</text>
  <defs>
    <marker id="str05" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#186399"/>
    </marker>
  </defs>
</svg>
```

```java
String s = "merhaba";
s.toUpperCase();          // yeni bir "MERHABA" üretilir ama...
System.out.println(s);    // merhaba — dönen değer atanmadığı için kayboldu
```

Kod geçerlidir, `toUpperCase` var olan bir metottur; hata yoktur. İstediğin sonuç için `s = s.toUpperCase();` yazman gerekir. "String metotları nesneyi değiştirmez, yenisini döndürür" kuralı bu dersin en sık test edilen sonucudur.

### Nasıl çalışır?

**1) Değişmezlik neden seçildi?** Dört gerekçe:

1. **Güvenlik**: dosya yolu, ağ bağlantısı gibi hassas değerler elden ele geçerken değiştirilemez.
2. **Havuz paylaşımı**: aynı literali herkes paylaşabilir, çünkü kimse bozamaz.
3. **`hashCode` önbelleklenebilir**: içerik değişmediği için hash bir kez hesaplanır ve saklanır; bu da `String`'i hızlı ve güvenli bir `HashMap` anahtarı yapar — anahtar sonradan değişemeyeceği için kova (bucket) tutarlı kalır.
4. **Thread-safe**: değişmez nesne doğası gereği paylaşılabilir; çok iş parçacıklı (multi-thread) ortamda kilitsiz kullanılır.

Dikkat: değişmezlik `String`'i **ilkel yapmaz**. `String` bir sınıftır, yani referans tipidir; ilkeller `int`, `char` gibi 8 tiptir.

**2) Havuz, `new String` ve `intern`.**

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="String havuzu ve new String farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Literal havuzda paylaşılır, new String heap'te ayrı</text>
  <rect x="50" y="46" width="250" height="118" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">"abc" (literal)</text>
  <text x="175" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">String havuzunda</text>
  <text x="175" y="116" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">tek nesne paylaşılır</text>
  <text x="175" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tüm kullanımlar aynı referans</text>
  <rect x="340" y="46" width="250" height="118" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="465" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">new String("abc")</text>
  <text x="465" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">heap'te taze nesne</text>
  <text x="465" y="116" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">havuza girmez</text>
  <text x="465" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">.intern() ile havuza katılır</text>
</svg>
```

```java
String a = "ab" + "cd";
String b = "abcd";
System.out.println(a == b);   // true
```

`"ab" + "cd"` bir **derleme-zamanı sabit ifadesidir**: iki taraf da literal olduğu için derleyici bunu **sabit katlamayla** tek bir `"abcd"` literaline indirir. O literal havuza girer ve `b` **aynı havuz nesnesini** paylaşır → `==` **true**. Ama bir taraf **değişken** olsaydı sonuç değişirdi:

```java
String x = "ab";
String a2 = x + "cd";               // birleştirme ÇALIŞMA-ZAMANINDA yapılır
System.out.println(a2 == "abcd");   // false (.intern() çağrılmadıkça)
```

Yani `+`'nın havuz davranışı, işlenenlerin derleme-zamanı sabiti olup olmamasına bağlıdır. (İki literali `+` ile birleştirmek geçerli ve çok yaygındır; `+` string bağlamında birleştirmedir, istisna fırlatmaz.)

`new String("abc")` havuza **girmez**; heap'te taze bir nesne üretir. `.intern()` ise şunu yapar: havuzda aynı içerikli (`equals`) bir string varsa **onun referansını** döndürür; yoksa bu içeriği havuza **ekler** ve eklenen referansı döndürür. Bu yüzden `new String("java").intern() == "java"` **true**'dur. `intern` içeriği değiştirmez, nesne silmez, çöp toplama (garbage collection) tetiklemez. Aynı içerikli çok sayıda string varsa bellek tasarrufu sağlayabilir, ama havuz sınırlı bir kaynaktır — dikkatli kullanılmalıdır.

**3) Değişmezliğin bedeli: birleştirme.** Her `+` yeni bir `String` üretip birikmiş içeriğin **tamamını kopyalar**.

```java
String sonuc = "";
for (int i = 0; i < n; i++) {
    sonuc += veri[i];   // her turda yeni String + tüm birikmiş içeriğin kopyası
}
```

i'inci adımda kopyalanan uzunluk yaklaşık i olduğundan toplam iş 1+2+…+n ≈ **O(n²)**'dir; `+=` sabit zamanlı (O(1)) DEĞİLDİR. Üstelik çok sayıda ara nesne üretip çöp toplayıcıyı (GC) zorlar. Doğru araç, değiştirilebilir (mutable) bir tampon olan **`StringBuilder`**'dır: `append` amortize **O(1)** çalışır, toplam maliyet **O(n)** olur; içeride büyüyen bir dizi tutar ve sonunda tek `toString()` ile sonucu üretir. `char[]`'i elle yönetmek de mümkündür ama zahmetli ve hataya açıktır — `StringBuilder` bunu güvenli biçimde kapsar. (Thread-safe gerekiyorsa `StringBuffer` — ayrı derste.)

İnce nokta: derleyici **tek satırlık** `a + b + c` ifadelerini kendisi verimli biçimde derler. Bunu Java 9+ sürümlerinde `invokedynamic`/`StringConcatFactory` ile, öncesinde `StringBuilder` ile yapar. Ama **döngü içindeki** `+=`'yi tek birleştirmeye indiremez; çünkü her tur sıfırdan yeni bir birleştirme başlar. Bu yüzden döngüde açıkça `StringBuilder` kullanmak gerekir.

**4) Dahili depolama.** `String` **mantıksal olarak UTF-16 kod birimleri** (`char`, 16 bit) dizisidir; `length()`, `charAt()` bu model üzerinden çalışır. Java 9+ ile gelen **compact strings** optimizasyonu, içeriği tamamen **Latin-1** (ISO-8859-1) aralığına sığan string'leri dahili olarak **1 bayt/karakter** saklar (bir bayrakla işaretlenir); sığmayanlar (çoğu emoji, bazı alfabeler) 2 bayt/karakter (UTF-16) kalır. Çoğu tipik metinde ~%50 bellek tasarrufu sağlanır, üstelik API davranışı hiç değişmeden. Bu tamamen içeride olan bir iştir.

### Mülakatta ne sorulur?

- **`s.toUpperCase();` tek başına hiçbir şey yapmaz.** Dönen değer atanmalıdır.
- **Değişmezliğin gerekçeleri:** havuz paylaşımı, `hashCode` önbelleği/güvenli `HashMap` anahtarı, thread-safety, güvenlik. "String'i ilkel yapar" bir gerekçe DEĞİLDİR ve zaten yanlıştır.
- **`"ab" + "cd" == "abcd"` → true**, ama değişkenli birleştirmede **false**. Ayrımı sabit katlamayla açıklayabilmek beklenir.
- **`.intern()` ne yapar?** Havuzdakini döndürür ya da havuza ekler. İçerik değiştirmez, nesne silmez, `new String`'in kendi havuz-dışı referansını da döndürmez.
- **Döngüde `+=` O(n²)'dir**, `StringBuilder` ile O(n). Sorun sınır (`ArrayIndexOutOfBoundsException`) ya da "döngü hiç çalışmaz" değildir — tamamen karmaşıklık meselesidir.
- **Kodlama gerçekleri (İleri):** Java **UTF-32 kullanmaz**; ASCII ile sınırlı değildir, Türkçe dahil Unicode'u destekler; dahili temsil **platform kodlamasından bağımsızdır**. Platform kodlaması yalnız `getBytes()` / `new String(byte[])` gibi **bayt dönüşümlerinde** devreye girer — orada daima açık bir `Charset` (örn. UTF-8) belirtmek gerekir.

### Tek cümlede

`String` değişmezdir: metotları yeni nesne döndürür, literaller havuzda paylaşılır ve bu güvenliğin bedeli döngüde `+` ile birleştirmenin O(n²) olmasıdır — orada `StringBuilder` kullanılır.

# str-1 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
String s = "merhaba";
s.toUpperCase();
System.out.println(s);
```
## A
`merhaba`
🤖 Doğru: `toUpperCase` yeni string döndürür ama atanmadığı için `s` aynı kalır.
## B
`MERHABA`
🤖 String değişmezdir; `toUpperCase` `s`'i değiştirmez, dönen değer atanmadı.
## C
Derleme hatası verir
🤖 Kod geçerli; `toUpperCase` var olan bir metottur, çağrı meşrudur.
## D
Boş satır yazar
🤖 `s` hâlâ orijinal değeri tutar; boş değil "merhaba" yazar.
## Doğru
A
## Açıklama
`String` **değişmezdir** (immutable): hiçbir metot mevcut nesnenin içeriğini değiştiremez. `s.toUpperCase()` yeni bir `String` (`"MERHABA"`) **üretir ve döndürür**, ama bu dönen değer bir değişkene **atanmadığı** için kaybolur; `s` orijinal `"merhaba"` değerini korur. Sonuç: **"merhaba"**. `MERHABA` istenseydi `s = s.toUpperCase();` yazılmalıydı. Bu, değişmezliğin en sık test edilen sonucudur — "String metotları nesneyi değiştirmez, yenisini döndürür" kuralını içselleştirmek gerekir.

# str-2 | Temel
## Soru
`String` sınıfının değişmez (immutable) olmasının bir GEREKÇESİ aşağıdakilerden hangisi DEĞİLDİR?
## A
String havuzunda güvenli paylaşım sağlar
🤖 Bu geçerli bir gerekçedir; değişmezlik paylaşımı güvenli kılar.
## B
hashCode'un önbelleklenmesini ve HashMap anahtarı olarak güvenli kullanımı mümkün kılar
🤖 Bu da geçerli bir gerekçedir; değişmez içerik = değişmez hash.
## C
String'lerin ilkel (primitive) tip olmasını sağlar
🤖 Doğru (aranan yanlış): String ilkel değildir, bir sınıftır; değişmezlik bunu değiştirmez.
## D
Çok iş parçacıklı (thread) ortamda kilitsiz güvenli paylaşımı mümkün kılar
🤖 Bu geçerli bir gerekçedir; değişmez nesne doğası gereği thread-safe'tir.
## Doğru
C
## Açıklama
Değişmezliğin gerçek gerekçeleri: **havuz paylaşımı güvenliği** (A), **hashCode önbelleklenmesi ve güvenli HashMap anahtarı** (B) — anahtar sonradan değişemeyeceği için kova (bucket) tutarlı kalır — ve **thread-safety** (D) — değişmez nesne doğası gereği paylaşılabilir, kilit gerekmez. Ayrıca güvenlik (hassas parametrelerin değiştirilememesi) de bir nedendir. **String'in ilkel tip olması (C)** bir gerekçe değildir ve zaten yanlıştır: `String` bir **sınıftır** (nesnedir), değişmezlik onu ilkel yapmaz. İlkeller `int`, `char` gibi 8 tiptir; `String` referans tipidir. Bu yüzden aranan "gerekçe olmayan" seçenek C'dir.

# str-3 | Temel
## Soru
`String` içeriğini değiştiren bir metot olmadığına göre, çok sayıda parça birleştirmek için (`+` yerine) hangi sınıf tercih edilir?
## A
`String` yeterlidir; `+=` ile döngüde birleştirmek en verimlidir
🤖 Döngüde `+=` her turda yeni String yaratır; verimsizdir, StringBuilder tercih edilir.
## B
`char[]` dizisini elle yönetmek tek seçenektir
🤖 Elle dizi yönetimi zahmetli ve hataya açık; StringBuilder bunu zaten kapsar.
## C
`Integer` sarmalayıcısı
🤖 `Integer` sayısaldır, metin birleştirmeyle ilgisi yoktur.
## D
`StringBuilder` (değiştirilebilir / mutable bir tampon)
🤖 Doğru: StringBuilder mutable tampondur, tekrarlı birleştirmede yeni nesne yaratmaz.
## Doğru
D
## Açıklama
`String` değişmez olduğu için her `+` birleştirme **yeni bir nesne** üretir; bir döngüde bu, çok sayıda ara nesne ve O(n²) kopyalama maliyeti yaratır (A yanlış). Bunun için **değiştirilebilir (mutable)** bir tampon olan **`StringBuilder`** kullanılır: içeride büyüyen bir `char[]` tutar, `append` ile her seferinde yeni `String` yaratmadan ekleme yapar, sonunda tek `toString()` ile sonucu üretir. `Integer` (C) sayısaldır, konuyla ilgisizdir. `char[]`'i elle yönetmek (B) mümkün ama zahmetli ve hataya açıktır — `StringBuilder` bunu güvenli biçimde kapsar. (Thread-safe gerekiyorsa `StringBuffer` — ayrı derste.)

# str-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
String a = "ab" + "cd";
String b = "abcd";
System.out.println(a == b);
```
## A
`false`; `a` çalışma-zamanında birleştiği için havuz dışında ayrı bir nesnedir
🤖 `"ab"+"cd"` sabit ifadedir; derleyici tek literale katlar, `a` da havuz nesnesidir.
## B
`true`; iki literalin `+`'sı derleme anında tek `"abcd"` literaline katlanır ve havuzda paylaşılır
🤖 Doğru: sabit katlama (constant folding) sonucu `a` da havuzdaki tek "abcd" nesnesini gösterir.
## C
Derleme hatası verir; iki string literali `+` operatörüyle birleştirilemez
🤖 Literaller `+` ile birleştirilebilir; bu geçerli ve çok yaygın bir kullanımdır.
## D
`ArithmeticException` fırlatır; `+` operatörü yalnız sayısal işlenenlerle çalışır
🤖 String bağlamında `+` birleştirmedir; istisna fırlamaz.
## Doğru
B
## Açıklama
`"ab" + "cd"` bir **derleme-zamanı sabit ifadesidir** (compile-time constant); iki taraf da literal olduğundan derleyici bunu **sabit katlama** (constant folding) ile derleme anında tek bir `"abcd"` literaline dönüştürür. Bu literal string havuzuna girer ve `b = "abcd"` **aynı havuz nesnesini** paylaşır → `a == b` **true**'dur. Kritik ayrım: eğer bir taraf **değişken** olsaydı (`String x = "ab"; String a = x + "cd";`), birleştirme **çalışma-zamanında** yapılır, sonuç havuza girmez ve `==` **false** olurdu (`.intern()` çağrılmadıkça). Yani `+`'nın havuz davranışı, işlenenlerin derleme-zamanı sabiti olup olmamasına bağlıdır.

# str-5 | Orta
## Soru
`new String("java")` ile oluşturulan bir string üzerinde `.intern()` çağırmak ne yapar?
## A
Havuzda aynı içerikli nesne varsa onun referansını, yoksa içeriği havuza ekleyip yeni referansı döndürür
🤖 Doğru: intern havuz üyeliğini sağlar, paylaşılan havuz referansını verir.
## B
Nesneyi yerinde değiştirir ve tuttuğu metni büyük harfe çevirerek havuza yazar
🤖 `intern` içerik değiştirmez; havuzdaki referansı döndürür, büyük harfle ilgisi yok.
## C
Nesneyi bellekten siler ve o an için çöp toplamayı (garbage collection) tetikler
🤖 `intern` silme yapmaz; havuza katma/havuzdakini döndürme işlemidir.
## D
Her zaman `new String` ile yaratılan nesnenin kendi (havuz dışı) referansını döndürür
🤖 Aksine; `new String` havuz dışıdır, `intern` havuzdaki (farklı) referansı döndürür.
## Doğru
A
## Açıklama
`.intern()`, bir string'i **string havuzuna** kaydeder: havuzda aynı içerikte (`equals`) bir string zaten varsa **onun referansını** döndürür; yoksa bu içeriği havuza **ekler** ve eklenen referansı döndürür. Yani `new String("java").intern() == "java"` **true**'dur — intern edilen değer, havuzdaki literalle aynı referansa gelir. `intern` içeriği değiştirmez (B), nesne silmez (C), ve `new String`'in kendi (havuz-dışı) referansını döndürmez (D) — tam tersine havuzdaki paylaşılan referansı verir. Kullanım: aynı içerikli çok sayıda string varsa bellek tasarrufu sağlayabilir, ama havuz sınırlı bir kaynaktır, dikkatli kullanılmalıdır.

# str-6 | İleri
## Soru
`String`'in içeride kullandığı karakter kodlaması ve depolama biçimi hakkında modern Java'da (9+) hangisi doğrudur?
## A
String'in her karakteri, içeriği ne olursa olsun her zaman 4 bayt (UTF-32) olarak saklanır
🤖 Java UTF-32 kullanmaz; mantıksal model UTF-16, depolama ise compact string'tir.
## B
String'ler platformun varsayılan kodlamasında saklanır; dahili temsil makineye göre değişir
🤖 Dahili temsil platformdan bağımsızdır; platform kodlaması yalnız bayt dönüşümünde devreye girer.
## C
String yalnızca ASCII tutabilir; Türkçe'ye özgü karakterler saklanamaz, kayba uğrar
🤖 Java Unicode destekler; Türkçe dahil tüm karakterler saklanır, ASCII sınırı yok.
## D
Mantıksal olarak UTF-16 kod birimi dizisidir; Java 9+ "compact strings" ile Latin-1'e sığanları 1 bayt/karakter saklar
🤖 Doğru: mantıksal UTF-16, fiziksel depolama Latin-1'e sığıyorsa 1 bayttır.
## Doğru
D
## Açıklama
`String` **mantıksal olarak UTF-16 kod birimleri** (`char`, 16 bit) dizisidir; `charAt`, `length` gibi API'ler bu model üzerinden çalışır. Java 9+ ile gelen **compact strings** optimizasyonu, içeriği tamamen **Latin-1** (ISO-8859-1) aralığına sığan string'leri dahili olarak **1 bayt/karakter** ile saklar (bir bayrakla işaretlenir); sığmayan (örn. çoğu emoji, bazı alfabeler) string'ler ise 2 bayt/karakter (UTF-16) kalır. Böylece çoğu tipik metin için ~%50 bellek tasarrufu sağlanır — API davranışı değişmeden. Java UTF-32 kullanmaz (A), ASCII ile sınırlı değildir Unicode destekler (C), ve dahili temsil platform kodlamasından bağımsızdır (B) — platform kodlaması yalnız `getBytes()`/`new String(byte[])` gibi **bayt dönüşümlerinde** devreye girer ki bu yüzden orada daima açık `Charset` (UTF-8 gibi) belirtmek önerilir.

# str-7 | İleri
## Soru
Aşağıdaki döngünün karmaşıklık (complexity) açısından sorunu nedir?

```java
String sonuc = "";
for (int i = 0; i < n; i++) {
    sonuc += veri[i];   // String birleştirme
}
```
## A
Sorun yok; her `+=` sabit zamanlıdır (O(1)), döngü toplamda O(n) çalışır
🤖 String değişmez olduğundan her `+=` mevcut içeriği kopyalar; O(1) değil.
## B
`veri[i]` her zaman `ArrayIndexOutOfBoundsException` fırlatır
🤖 Sınır `i < n` doğru kuruluysa istisna yok; sorun performanstır, sınır değil.
## C
Her `+=` birikmiş içeriği yeni nesneye kopyalar; toplam maliyet yaklaşık O(n²) olur
🤖 Doğru: her adımda giderek büyüyen içerik yeniden kopyalanır, karesel maliyet doğar.
## D
Döngü hiç çalışmaz çünkü `sonuc` boş başlar
🤖 Boş başlaması döngüyü engellemez; `+=` boş string'e ekler, döngü çalışır.
## Doğru
C
## Açıklama
`String` değişmez olduğu için `sonuc += veri[i]` her iterasyonda **yeni bir String** yaratır ve o ana kadar birikmiş **tüm içeriği yeni nesneye kopyalar**. i'inci adımda kopyalanan uzunluk ~i olduğundan toplam iş 1+2+...+n ≈ **O(n²)** olur — büyük `n` için ciddi yavaşlar ve çok sayıda ara nesne üretip GC'yi zorlar. Doğru çözüm `StringBuilder` kullanmaktır: `append` amortize O(1)'dir, toplam **O(n)**. `+=` sabit zamanlı değildir (A yanlış); sınır doğruysa istisna yoktur (B); boş başlaması döngüyü engellemez (D). Not: derleyici **tek satırlık** `a + b + c` ifadelerini tek seferde verimli biçimde birleştirir (Java 9+ `invokedynamic`/`StringConcatFactory`, öncesinde `StringBuilder` ile), ama **döngü içindeki** `+=`'yi tek birleştirmeye indiremez — her tur sıfırdan yeni bir birleştirme yapılır — bu yüzden döngüde açıkça `StringBuilder` kullanmak gerekir.
