---
slug: stringbuilder-buffer
baslik: "StringBuilder vs StringBuffer: Değiştirilebilirlik, Thread-Safety ve Kapasite"
kademe: Temel
sira: 6
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Şöyle düşün: bir alışveriş listesi yazıyorsun. `String` ile çalışmak, her yeni maddede kâğıdı buruşturup baştaki her şeyi **yeni bir kâğıda tekrar yazmak** gibidir; 50 maddede israf devasa olur. `StringBuilder` ise silinip üzerine yazılabilen bir tahtadır: yeni maddeyi sonuna eklersin, o kadar. Metni parça parça biriktirdiğin her yerde (rapor, log satırı, SQL, JSON) doğru araç bu tahtadır. `StringBuffer` ise aynı tahtanın, "aynı anda birden fazla kişi yazacaksa" diye kilit takılmış eski modelidir.

### Temel kavramlar

- **Değişmez (immutable)**: içeriği yaratıldıktan sonra değişmeyen nesne — `String` böyledir; her `+` veya metot çağrısı yeni bir nesne üretir.
- **Değiştirilebilir (mutable)**: içeriği yerinde değişebilen nesne — `StringBuilder` ve `StringBuffer` böyledir.
- **Tampon (buffer)**: içeride tutulan, gerektikçe büyüyen karakter dizisi. Java 9+ ile bu dizinin dahili tipi `byte[]`'dir.
- **Kapasite (capacity)**: tamponda **şu ana kadar ayrılmış** yer. **Uzunluk (`length()`)**: içinde gerçekten bulunan karakter sayısı. İkisi farklı şeydir.
- **Senkronizasyon (`synchronized`)**: aynı anda yalnız bir iş parçacığının (thread) metoda girmesini sağlayan kilit.
- **Thread-safe**: birden çok iş parçacığı aynı nesneye eşzamanlı erişse bile verinin bozulmaması.

`StringBuilder` ve `StringBuffer` **aynı API'yi** sunar (`append`, `insert`, `delete`, `reverse`, `toString`); metot sayıları da aynıdır. Tek gerçek fark **thread-safety**'dir:

- **`StringBuilder`** — senkronize DEĞİL. Kilit maliyeti olmadığı için tek iş parçacıklı (single-thread) kullanımda **daha hızlıdır**. Java 5+ ile geldi; modern kodun varsayılan tercihi budur.
- **`StringBuffer`** — metotları `synchronized`. Çok iş parçacıklı erişimde güvenlidir ama her çağrıda kilit alıp bıraktığı için **daha yavaştır**. Eski (legacy) bir sınıftır; **kaldırılmadı**, sadece bugün nadiren gerçekten gerekir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="StringBuilder ve StringBuffer karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı API, tek fark: senkronizasyon</text>
  <rect x="50" y="46" width="250" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">StringBuilder</text>
  <text x="175" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">senkronize DEĞİL</text>
  <text x="175" y="116" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">tek thread'de HIZLI</text>
  <text x="175" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">varsayılan tercih</text>
  <text x="175" y="158" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Java 5+</text>
  <rect x="340" y="46" width="250" height="130" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="465" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">StringBuffer</text>
  <text x="465" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">synchronized metotlar</text>
  <text x="465" y="116" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">thread-safe ama YAVAŞ</text>
  <text x="465" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">eski (legacy)</text>
  <text x="465" y="158" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">nadiren gerçekten gerekir</text>
</svg>
```

### Nasıl çalışır?

**1) Yerinde değişme.**

```java
StringBuilder sb = new StringBuilder("abc");
sb.append("def");
System.out.println(sb);   // abcdef
```

`append` yeni nesne üretmez; `sb`'nin **kendi tamponunu** değiştirir ve var olanı silmeden **sonuna** ekler. `String`'in aksine dönen değeri atamana gerek yoktur — zaten `append` kolay zincirleme (`sb.append("a").append("b")`) için `sb`'nin kendisini döndürür. `System.out.println(sb)` ise `println(Object)` aşırı yüklemesini (overload) çağırır ve otomatik olarak `sb.toString()` uygular; kod derlenir ve içeriği yazar.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="döngüde String += O(n kare) vs StringBuilder O(n)">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Döngüde metin biriktirme: neden StringBuilder?</text>
  <rect x="30" y="44" width="275" height="140" rx="12" fill="#FBE9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="167" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">for: s += x</text>
  <text x="167" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">her turda YENİ String</text>
  <text x="167" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">eski içerik kopyalanır</text>
  <text x="167" y="146" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">O(n²) — yavaş</text>
  <text x="167" y="168" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çöp (garbage) üretir</text>
  <rect x="335" y="44" width="275" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">sb.append(x)</text>
  <text x="472" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">aynı char[] tamponu</text>
  <text x="472" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sonda tek toString()</text>
  <text x="472" y="146" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">O(n) — hızlı</text>
  <text x="472" y="168" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tercih edilen yol</text>
</svg>
```

**2) Kapasite ve büyüme.** Boş `new StringBuilder()` **16 karakterlik** kapasiteyle başlar. Tampon dolunca kapasite genellikle **~2 kat + 2** büyür ve mevcut içerik yeni diziye kopyalanır. **Kapasite bir üst sınır DEĞİLDİR** — yalnızca "şu ana kadar ayrılmış yer"dir; aşılınca otomatik büyür, hata vermez. Yaklaşık son boyutu biliyorsan `new StringBuilder(kapasite)` ile başlamak bu yeniden tahsisatları (reallocation) ve kopyalamaları azaltır.

Dikkat — iki farklı yapıcı (constructor) aşırı yüklemesi kolayca karıştırılır:

```java
StringBuilder a = new StringBuilder(100);    // int overload:    BOŞ tampon, kapasite 100, length() = 0
StringBuilder b = new StringBuilder("100");  // String overload: içerik "100",  length() = 3
```

Her iki durumda da **tek** nesne yaratılır.

**3) `equals` ezilmez.**

```java
StringBuilder a = new StringBuilder("hi");
StringBuilder b = new StringBuilder("hi");
System.out.println(a.equals(b));   // false
```

`String.equals` içeriğe bakar — ama `StringBuilder`/`StringBuffer` `equals`'ı **override etmez**; `Object.equals`'ı miras alırlar ve o **referans kimliğine** bakar. `a` ile `b` iki ayrı `new` nesnesi olduğu için sonuç **false**'tur, içerikleri aynı olsa bile. (`equals` `Object`'ten miras geldiği için her nesnede vardır; derleme hatası olmaz. Ayrıca tamponlar `String` gibi havuzlanmaz — her `new` taze bir nesnedir.) İçerik karşılaştırmak için `a.toString().equals(b.toString())` ya da Java 11+ `a.compareTo(b) == 0` kullanılır.

**4) Thread-safety'nin sınırı.** `StringBuffer`'ın güvencesi **metot düzeyindedir**: her **tek** çağrı (`append`, `insert`…) bölünmezdir (atomik), yarıda kesilip bozuk veri yazmaz, iç tampon bozulmaz. Garanti EDİLMEYEN şey, **ardışık iki çağrının birlikte** atomik olmasıdır:

```java
// Paylaşılan sb'ye iki thread aynı anda erişiyor
sb.append("A");   // Thread 1
sb.append("B");   // Thread 2
```

Araya başka bir iş parçacığı girebilir; sonucun `"AB"` mi `"BA"` mı olacağı **belirsizdir**. Bileşik (compound) işlemler için hâlâ **dışarıdan senkronizasyon** gerekir. Kısacası: "her metot güvenli" ≠ "metot dizileri güvenli". Bu, `StringBuffer`'ın ve genel olarak thread-safe koleksiyonların en yanlış anlaşılan noktasıdır.

### Mülakatta ne sorulur?

- **`String` ile `StringBuilder` farkı = değiştirilebilirlik.** Ters söylemek ("StringBuilder immutable") klasik hatadır; ikisi de metin tutar, fark içerik türü değildir.
- **`StringBuilder` ile `StringBuffer` farkı = yalnızca senkronizasyon.** Metot sayısı farkı değildir, değişmezlik farkı da değildir (ikisi de mutable), `StringBuffer` kaldırılmış da değildir.
- **`new StringBuilder(100)` kapasitedir, içerik değil.** Ne 100 karakterlik bir üst sınırdır, ne de 100 ayrı nesne yaratır.
- **`a.equals(b)` → false.** `String` refleksinin yanılttığı klasik tuzak.
- **Bileşik atomiklik.** "`StringBuffer` thread-safe'tir" cümlesinin neyi garanti ETMEDİĞİNİ söyleyebilmek kıdem işaretidir.
- **Neden `StringBuilder` varsayılan?** Çünkü tamponlar pratikte çoğunlukla bir metoda **yerel** (local) değişkenlerdir; tek thread içinde doğar ve ölürler, hiç paylaşılmazlar. Böyle bir durumda `StringBuffer`'ın her metottaki senkronizasyonu **tamamen faydasızdır** — kimse eşzamanlı erişmediği hâlde her `append` kilit alıp bırakır, bu da boşa performans kaybıdır. Gerçekten birden çok thread'in **aynı** tamponu paylaştığı nadir durumda `StringBuffer` (veya dışarıdan senkronizasyon) düşünülür; ama o senaryolarda bile çoğu kez daha iyi tasarım, paylaşımı ortadan kaldırıp her thread'e **ayrı bir `StringBuilder`** vermektir.
- **Döngüde metin biriktirme:** `String` `+=` her turda yeni nesne üretip birikmiş içeriği kopyalar → **O(n²)** ve bol çöp; `sb.append(...)` aynı tamponu kullanır, sonda tek `toString()` → **O(n)**.

### Tek cümlede

`StringBuilder` ve `StringBuffer` aynı API'ye sahip değiştirilebilir metin tamponlarıdır; tek fark `StringBuffer`'ın metot düzeyinde senkronize (dolayısıyla yavaş) olmasıdır ve ikisi de `equals`'ı ezmez.

# sb-1 | Temel
## Soru
`StringBuilder` ile `String` arasındaki temel fark nedir?
## A
`StringBuilder` değişmez (immutable), `String` değiştirilebilir (mutable)
🤖 Tam ters: değişmez olan `String`, değiştirilebilir olan `StringBuilder`'dır.
## B
`StringBuilder` değiştirilebilir (mutable) bir tampondur; `String` değişmezdir (immutable)
🤖 Doğru: StringBuilder yerinde değişir, String her işlemde yeni nesne üretir.
## C
İkisi de değişmezdir, sadece isimleri farklıdır
🤖 `StringBuilder` mutable'dır; `append` mevcut tamponu değiştirir, yeni nesne yaratmaz.
## D
`StringBuilder` yalnız sayılar, `String` yalnız metin tutar
🤖 İkisi de metin tutar; fark değiştirilebilirliktir, içerik türü değil.
## Doğru
B
## Açıklama
Temel fark **değiştirilebilirliktir** (mutability): `String` **değişmezdir** — her `+` veya metot çağrısı yeni bir nesne üretir. `StringBuilder` ise **değiştirilebilir bir tampondur** — `append`, `insert`, `delete` gibi işlemler **aynı nesnenin** iç `char[]` tamponunu yerinde değiştirir, yeni nesne yaratmaz. Bu yüzden çok sayıda metin işlemi (özellikle döngüde) için `StringBuilder` çok daha verimlidir. Seçenekler ters ifade (A), yanlış eşitleme (C) veya alakasız içerik ayrımı (D) içerir. `StringBuilder` de metin tutar (D yanlış).

# sb-2 | Temel
## Soru
`StringBuilder` ve `StringBuffer` arasındaki ASIL fark nedir?
## A
`StringBuffer` daha fazla metot içerir
🤖 API'leri neredeyse özdeştir; fark metot sayısı değil, senkronizasyondur.
## B
`StringBuilder` değişmez, `StringBuffer` değiştirilebilir
🤖 İkisi de değiştirilebilir (mutable); fark senkronizasyondadır, değişmezlik değil.
## C
`StringBuffer`'ın metotları `synchronized`'dır; `StringBuilder` senkronize değildir
🤖 Doğru: tek fark thread-safety; StringBuffer kilitli/güvenli-yavaş, StringBuilder kilitsiz/hızlı.
## D
`StringBuffer` yalnız eski Java'da vardır, modern Java'da kaldırıldı
🤖 `StringBuffer` hâlâ mevcuttur (kaldırılmadı); sadece çoğu durumda tercih edilmez.
## Doğru
C
## Açıklama
`StringBuilder` ve `StringBuffer` **aynı API'yi** sunar (aynı metotlar); tek gerçek fark **thread-safety**'dir. `StringBuffer`'ın metotları **`synchronized`** anahtarıyla kilitlenir; birden çok iş parçacığı aynı nesneye eşzamanlı erişse bile tutarlıdır — ama her çağrıda kilit alma/bırakma maliyeti onu **yavaşlatır**. `StringBuilder` senkronize değildir; kilit maliyeti olmadığından **tek iş parçacıklı** kullanımda daha hızlıdır ve modern kodun varsayılanıdır. İkisi de mutable'dır (B yanlış), metot sayıları aynıdır (A), ve `StringBuffer` kaldırılmamıştır (D) — yalnızca çoğu senaryoda `StringBuilder` tercih edilir.

# sb-3 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
StringBuilder sb = new StringBuilder("abc");
sb.append("def");
System.out.println(sb);
```
## A
`abcdef`
🤖 Doğru: `append` mevcut StringBuilder'a ekler, sonuç "abcdef" olur.
## B
`abc`
🤖 `append` tamponu yerinde değiştirir; `sb` artık "abcdef"tir.
## C
`def`
🤖 `append` var olanı silmez, sonuna ekler; başındaki "abc" kalır.
## D
Derleme hatası; `println` bir `StringBuilder` alamaz
🤖 `println(Object)` overload'ı `toString`'i çağırır; kod derlenir ve içeriği yazar.
## Doğru
A
## Açıklama
`StringBuilder` **değiştirilebilir** olduğundan `sb.append("def")` mevcut nesnenin tamponuna `"def"`'i **ekler** — yeni nesne yaratmaz, `sb`'nin kendi içeriği `"abcdef"` olur. `String`'in aksine dönen değeri atamaya gerek yoktur (zaten `append` kolay zincirleme için `sb`'nin kendisini döndürür). `append` var olanı silmez, **sonuna** ekler (C yanlış). `System.out.println(sb)` `println(Object)` aşırı yüklemesini (overload) çağırır ve otomatik `sb.toString()` uygular; derlenir ve **"abcdef"** yazar (D yanlış).

# sb-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
StringBuilder a = new StringBuilder("hi");
StringBuilder b = new StringBuilder("hi");
System.out.println(a.equals(b));
```
## A
`true`; `StringBuilder`, `String` gibi `equals`'ı ezer ve içeriğe göre karşılaştırma yapar
🤖 `StringBuilder` `equals`'ı ezmez; içerik değil referans karşılaştırılır, sonuç false.
## B
`true`; aynı içerikli tüm StringBuilder'lar havuzlanıp tek nesnede paylaşıldığı için eşittir
🤖 StringBuilder havuzlanmaz; her `new` ayrı nesnedir, paylaşım yoktur.
## C
Derleme hatası verir; `StringBuilder` sınıfında `equals` metodu tanımlı değildir
🤖 `equals` `Object`'ten miras alınır; her nesnede vardır, hata olmaz.
## D
`false`; `StringBuilder` `equals`'ı ezmediğinden `Object.equals` (referans kimliği) çalışır
🤖 Doğru: StringBuilder equals'ı override etmez; farklı nesneler → false.
## Doğru
D
## Açıklama
Bu, `String` refleksinin yanılttığı klasik bir tuzaktır: `String.equals` içeriğe bakar, ama **`StringBuilder` `equals`'ı EZMEZ** — `Object.equals`'ı miras alır ki o **referans kimliğine** bakar. `a` ve `b` iki ayrı `new` nesnesi olduğundan `a.equals(b)` **false**'tur, içerikleri aynı olsa bile. İçerik karşılaştırmak için önce `toString()` alınıp `String.equals` kullanılır: `a.toString().equals(b.toString())` → true. `equals` `Object`'ten miras olarak her nesnede vardır (C yanlış); `StringBuilder` nesneleri havuzlanmaz, her `new` taze nesnedir (B yanlış). Ders: mutable tampon sınıflarında `equals` içerik karşılaştırması yapmaz.

# sb-5 | Orta
## Soru
`new StringBuilder(100)` ifadesindeki `100` argümanı ne anlama gelir?
## A
Tampona başlangıç içeriği olarak `"100"` metnini koyar; `length()` değeri 3 olur
🤖 `int` argümanı içerik değil, başlangıç KAPASİTESİDİR; metin için `String` overload'ı gerekir.
## B
Başlangıç kapasitesini (capacity) 100 karakter yapar; içerik boştur, tampon dolana dek yeniden tahsisat olmaz
🤖 Doğru: int argümanı başlangıç kapasitesidir; erken yeniden tahsisatları önler.
## C
StringBuilder'ı 100 karakterle sınırlar; bu sınır aşılırsa çalışma-zamanı hatası verir
🤖 Kapasite bir üst sınır değil; dolunca otomatik büyür, hata vermez.
## D
Aynı anda 100 tane boş StringBuilder nesnesi yaratıp bir dizide tutar
🤖 Tek nesne yaratılır; 100 yalnız o nesnenin başlangıç kapasitesidir.
## Doğru
B
## Açıklama
`StringBuilder(int capacity)` yapıcısı **başlangıç kapasitesini** ayarlar: iç `char[]` tampon 100 karakterlik yer ayırır, ama **içerik boştur** (`length()` = 0). Kapasite bir **üst sınır değildir** — dolunca tampon otomatik büyür (C yanlış), ama önceden yeterli kapasite ayırmak, büyüme sırasındaki **yeniden tahsisat ve kopyalama** maliyetlerini azaltır; son boyutu tahmin edebiliyorsan performans kazandırır. `int` argümanı **içerik değildir** — `new StringBuilder("100")` (String overload'ı) `"100"` metnini koyar, ama `new StringBuilder(100)` (int overload'ı) 100 kapasiteli boş tampon verir (A yanlış). Tek nesne yaratılır (D yanlış).

# sb-6 | İleri
## Soru
`StringBuffer` "thread-safe"tir denir. Aşağıdaki senaryoda bu güvenlik neyi GARANTİ ETMEZ?

```java
// Paylaşılan sb'ye iki thread aynı anda erişiyor
sb.append("A");   // Thread 1
sb.append("B");   // Thread 2
```
## A
Her bir `append` çağrısının kendi içinde atomik (bölünmez) olmasını
🤖 Bunu garanti EDER; her metot synchronized, tek append bölünmez. Aranan bu değil.
## B
İç `char[]` tamponunun bozulmadan (corruption olmadan) kalmasını
🤖 Bunu garanti EDER; senkronizasyon iç veri yapısını tutarlı tutar.
## C
İki ayrı `append` çağrısının araya başka thread girmeden birlikte (compound) çalışmasını
🤖 Doğru: her çağrı ayrı ayrı kilitlenir; iki çağrı arası atomik değildir, sıra karışabilir.
## D
`append`'in yarıda kesilip bozuk veri yazmamasını
🤖 Bunu garanti EDER; tek metot çağrısı kilit altında bütün çalışır.
## Doğru
C
## Açıklama
`StringBuffer`'ın thread-safety'si **metot düzeyindedir**: her `append`/`insert` çağrısı `synchronized` olduğundan **tek başına atomiktir** — iç tampon bozulmaz (B), çağrı yarıda kesilmez (D), her append bölünmezdir (A). Bunların hepsi **garanti edilir**. Ancak garanti edilmeyen şey, **birden çok işlemin birlikte atomik olmasıdır** (C): iki ayrı `append` çağrısı arasında başka bir thread araya girebilir, bu yüzden sonucun `"AB"` mi `"BA"` mı olacağı **belirsizdir** (sıra garanti edilmez). Yani "compound" (bileşik) işlemler için hâlâ dışarıdan senkronizasyon gerekir. Bu, `StringBuffer`'ın (ve genel olarak thread-safe koleksiyonların) en yanlış anlaşılan noktasıdır: "her metot güvenli" ≠ "metot dizileri güvenli". Zaten bu incelik ve kilit maliyeti yüzünden, gerçekten paylaşımlı mutable durum yerine genelde her thread'e **ayrı `StringBuilder`** vermek tercih edilir.

# sb-7 | İleri
## Soru
Modern Java'da `StringBuffer` yerine neredeyse her zaman `StringBuilder` önerilmesinin gerekçesi nedir?
## A
`StringBuffer` modern Java sürümlerinden kaldırıldığı için artık hiç kullanılamaz
🤖 `StringBuffer` hâlâ mevcuttur; gerekçe kaldırılma değil, gereksiz senkronizasyon maliyeti.
## B
`StringBuffer` thread-safe olmadığından paylaşımlı kullanımda veri bozulmasına yol açar
🤖 Tersine; `StringBuffer` thread-safe'tir, `StringBuilder` değildir. Gerekçe bu değil.
## C
`StringBuilder` daha fazla metot sunduğu için `StringBuffer`'dan daha yetenekli bir sınıftır
🤖 İkisinin API'si aynıdır; fark yetenek değil, senkronizasyon maliyetidir.
## D
Tamponlar çoğunlukla tek thread'e yerel olduğundan senkronizasyon gereksizdir; `StringBuffer`'ın kilit maliyeti boşa gider
🤖 Doğru: yerel/paylaşılmayan kullanımlarda senkronizasyon faydasızdır; StringBuilder o maliyeti taşımaz.
## Doğru
D
## Açıklama
Pratikte metin tamponları çoğunlukla **bir metoda yerel** (local) değişkenlerdir ve tek bir thread içinde yaşayıp ölürler — hiç paylaşılmazlar. Böyle bir durumda `StringBuffer`'ın her metottaki **senkronizasyonu tamamen faydasızdır**: kimse eşzamanlı erişmediği hâlde her `append` kilit alıp bırakır, bu da boşa **performans kaybıdır**. `StringBuilder` bu maliyeti taşımadığından varsayılan doğru tercihtir. Gerçekten birden çok thread'in **aynı** tamponu paylaştığı nadir durumda `StringBuffer` (veya dışarıdan senkronizasyon) düşünülür — ama o senaryolarda bile çoğu kez daha iyi tasarım, paylaşımı ortadan kaldırmaktır. `StringBuffer` kaldırılmadı (A), API'ler aynı (C), ve `StringBuffer` thread-safe'tir (B yanlış — tam tersini söyler).
