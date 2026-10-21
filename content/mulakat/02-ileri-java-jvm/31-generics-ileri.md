---
slug: generics-ileri
baslik: "Generics (İleri): Bounded Tipler, PECS ve Type Erasure"
kademe: İleri
sira: 31
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir depoda etiketli kutular düşün. Kutunun üstünde "yalnız kitap" yazıyorsa, içine ayakkabı koymaya çalışan kişiyi daha kapıda durdurursun. Etiketsiz kutuda ise her şey karışır; içinden çıkanı elinle yoklayıp tahmin etmen gerekir ve yanlış tahmin ettiğin an kaza olur. Generics, koleksiyonlara ve sınıflara bu etiketi takar: yanlış tipi koyan kodu derleyici sen daha yazarken reddeder. Ama işin can alıcı yanı şu — bu etiket yalnız depoya girişte okunur, kutu rafa yerleştikten sonra etiket sökülüp atılır. İleri seviye generics sorularının neredeyse tamamı bu tek cümleden doğar.

### Temel kavramlar

**Derleme zamanı (compile-time):** Kaynak kodun `javac` ile `.class` dosyasına çevrildiği an. Burada yakalanan hata, program hiç çalışmadan önlenir.

**Çalışma zamanı (runtime):** JVM'in `.class` dosyasını yürüttüğü an. Hata burada çıkarsa program kullanıcının elinde patlar.

**Tip parametresi (type parameter):** `class Kutu<T>` yazımındaki `T`. Gerçek bir tip değil, yer tutucudur — "buraya sonra bir tip yazılacak" demektir.

**Tip argümanı (type argument):** `Kutu<String>` yazdığında `T`'nin yerine geçen gerçek tip, yani `String`.

**Ham tip (raw type):** Tip argümanı hiç yazılmamış hâl, düz `List`. Derleyici denetimi kapanır.

**Sınırlı tip parametresi (bounded type parameter):** `<T extends Number>` yazarsan `T`, `Number` ve alt tipleriyle sınırlanır. Kazancı şudur: artık `T` üzerinde `Number`'ın metotlarını (`doubleValue()` gibi) çağırabilirsin. Buna ÜST sınır (upper bound) denir; `T` derleme sırasında `Number`'a silinir.

**Joker karakter (wildcard):** `?` işareti — "belirli ama benim bilmediğim bir tip" demektir. Üç biçimi vardır: `? extends X` (üst sınırlı), `? super X` (alt sınırlı), `?` (sınırsız).

**Üretici (producer) / tüketici (consumer):** Bir koleksiyondan veri OKUYORSAN o koleksiyon senin için üreticidir; içine veri YAZIYORSAN tüketicidir. Josh Bloch'un kuralı buradan gelir: **PECS — Producer Extends, Consumer Super**. `? extends X` bir üreticidir: ondan `X` okursun, ama içine `null` dışında hiçbir şey yazamazsın. `? super X` bir tüketicidir: içine `X` yazarsın, ama okuduğunda elinde yalnız `Object` kalır. JDK'nın `Collections.copy` imzası bu kuralın ders kitabı örneğidir:

```java
static <T> void copy(List<? super T> dest, List<? extends T> src)
// okunan taraf extends · yazılan taraf super
```

```svg
<svg viewBox="0 0 660 216" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="PECS: extends üreticiden okumak, super tüketiciye yazmak içindir">
  <defs>
    <marker id="generics-ar-g" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="generics-ar-o" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B26B00"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">PECS — Producer Extends, Consumer Super</text>
  <rect x="24" y="48" width="188" height="98" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="118" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#25804C">ÜRETİCİ · producer</text>
  <text x="118" y="96" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">List&lt;? extends T&gt;</text>
  <text x="118" y="118" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">ondan T OKUrsun</text>
  <text x="118" y="137" text-anchor="middle" font-family="system-ui" font-size="12" fill="#8A2E2E">içine yazamazsın</text>
  <rect x="272" y="70" width="116" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="330" y="95" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">senin</text>
  <text x="330" y="113" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">kodun</text>
  <rect x="448" y="48" width="188" height="98" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="542" y="72" text-anchor="middle" font-family="system-ui" font-size="13" font-weight="700" fill="#B26B00">TÜKETİCİ · consumer</text>
  <text x="542" y="96" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">List&lt;? super T&gt;</text>
  <text x="542" y="118" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">ona T YAZarsın</text>
  <text x="542" y="137" text-anchor="middle" font-family="system-ui" font-size="12" fill="#8A2E2E">okursan Object gelir</text>
  <text x="240" y="90" text-anchor="middle" font-family="system-ui" font-size="11" font-weight="700" fill="#25804C">OKU</text>
  <line x1="214" y1="98" x2="268" y2="98" stroke="#25804C" stroke-width="2.5" marker-end="url(#generics-ar-g)"/>
  <text x="418" y="90" text-anchor="middle" font-family="system-ui" font-size="11" font-weight="700" fill="#B26B00">YAZ</text>
  <line x1="390" y1="98" x2="444" y2="98" stroke="#B26B00" stroke-width="2.5" marker-end="url(#generics-ar-o)"/>
  <text x="330" y="176" text-anchor="middle" font-family="system-ui" font-size="12.5" font-weight="700" fill="#1C3247">copy(List&lt;? super T&gt; dest, List&lt;? extends T&gt; src)</text>
  <text x="330" y="200" text-anchor="middle" font-family="system-ui" font-size="11.5" fill="#5D7789">okunan taraf extends · yazılan taraf super</text>
</svg>
```

**Somutlaşmış / reified tip:** Tip bilgisi çalışma zamanında da duruyorsa o tip reified'dır. Java'da **diziler reified'dır**, **generics değildir**. Bu asimetri, birazdan göreceğin yasakların kaynağıdır.

### Nasıl çalışır?

Derleyici üç adım uygular:

1. Tip argümanlarına bakarak tüm tip denetimini YAPAR — `List<String>` üzerinde `add(5)` tam burada reddedilir.
2. Denetim bitince tip argümanlarını **siler** (tip silme / type erasure): `List<String>` → ham `List`, sınırsız `T` → `Object`, `<T extends Number>` içindeki `T` → `Number`.
3. Silme yüzünden kaybolan bilgiyi telafi etmek için gereken yerlere **cast** ekler.

Yazdığın kod:

```java
List<String> l = new ArrayList<>();
l.add("a");
String s = l.get(0);
```

Bayt koduna inen kod kabaca şudur:

```java
List l = new ArrayList();
l.add("a");
String s = (String) l.get(0);   // cast'ı derleyici koydu
```

```svg
<svg viewBox="0 0 660 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Type erasure: List String ve List Integer derlemeden sonra aynı ham List sınıfına iner">
  <defs>
    <marker id="generics-er-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Tip silme (type erasure)</text>
  <text x="150" y="52" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#186399">DERLEME-ZAMANI</text>
  <text x="510" y="52" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#8A2E2E">ÇALIŞMA-ZAMANI</text>
  <rect x="40" y="66" width="220" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="150" y="94" text-anchor="middle" font-family="system-ui" font-size="14" fill="#1C3247">List&lt;String&gt;</text>
  <rect x="40" y="128" width="220" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="2"/>
  <text x="150" y="156" text-anchor="middle" font-family="system-ui" font-size="14" fill="#1C3247">List&lt;Integer&gt;</text>
  <rect x="410" y="96" width="200" height="78" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="2"/>
  <text x="510" y="128" text-anchor="middle" font-family="system-ui" font-size="15" font-weight="700" fill="#6B3FA0">ham List</text>
  <text x="510" y="152" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">tek Class nesnesi</text>
  <line x1="262" y1="89" x2="406" y2="124" stroke="#5D7789" stroke-width="2" marker-end="url(#generics-er-ar)"/>
  <line x1="262" y1="151" x2="406" y2="146" stroke="#5D7789" stroke-width="2" marker-end="url(#generics-er-ar)"/>
  <text x="335" y="106" text-anchor="middle" font-family="system-ui" font-size="11" fill="#5D7789">tip argümanı SİLİNİR</text>
  <text x="330" y="204" text-anchor="middle" font-family="system-ui" font-size="12" fill="#8A2E2E">getClass() ikisinde AYNI · new T() · T.class · new T[] yasak</text>
</svg>
```

Bunun doğrudan sonuçları:

- `List<String>` ile `List<Integer>` çalışma zamanında AYNI sınıftır; `new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass()` ifadesi `true` döner.
- `instanceof List<String>` yasaktır — JVM'de sınanacak bir tip argümanı yoktur.
- `new T()` ve `T.class` yazılamaz; `T` çalışma zamanında hiçbir tip bilgisi taşımaz.
- `new T[n]` ya da `new List<String>[10]` gibi generic dizi oluşturmak yasaktır. Nedeni bellek modelidir: diziler eleman tipini çalışma zamanında TAŞIR ve yanlış tip yazılınca `ArrayStoreException` fırlatır; generics ise silinir. `new T[]` yaratılabilseydi JVM diziye hangi gerçek tipi gömeceğini bilemezdi. Sınır koymak da kurtarmaz: silme metodun İMZASINDA `T`'yi `Comparable`'a indirir, ama `new T[...]` İFADESİ dil düzeyinde her koşulda yasaktır. Bilinen geçici çözümler: `(T[]) new Object[n]` (denetimsiz/unchecked uyarısıyla) veya `Class<T>` taşıyıp `Array.newInstance(clazz, n)` çağırmak.

**Köprü metot (bridge method):** Silme, kalıtımda bir boşluk bırakır. `class Kutu implements Comparable<Kutu>` yazarsan sen `compareTo(Kutu)` uygularsın, ama silinmiş arayüz `compareTo(Object)` ister. Derleyici bu boşluğu, argümanı `(Kutu)`'ya çevirip senin metoduna yönlendiren sentetik bir köprü metotla kapatır.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bridge method: derleyici Comparable için compareTo Object köprü metodunu üretir">
  <defs>
    <marker id="generics-br-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B26B00"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-family="system-ui" font-size="16" font-weight="700" fill="#1C3247">Köprü metot (bridge method)</text>
  <text x="330" y="46" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">class Kutu implements Comparable&lt;Kutu&gt;</text>
  <rect x="60" y="66" width="240" height="72" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="180" y="92" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#25804C">senin yazdığın</text>
  <text x="180" y="116" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">int compareTo(Kutu o)</text>
  <rect x="360" y="66" width="240" height="72" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="2"/>
  <text x="480" y="92" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#B26B00">derleyici üretir · köprü</text>
  <text x="480" y="116" text-anchor="middle" font-family="system-ui" font-size="13" fill="#1C3247">int compareTo(Object o)</text>
  <line x1="358" y1="102" x2="302" y2="102" stroke="#B26B00" stroke-width="2.5" marker-end="url(#generics-br-ar)"/>
  <text x="330" y="170" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">köprü, silinmiş compareTo(Object) imzasını karşılar;</text>
  <text x="330" y="190" text-anchor="middle" font-family="system-ui" font-size="12" fill="#5D7789">argümanı (Kutu)'ya çevirip gerçek metodunu çağırır</text>
  <text x="330" y="216" text-anchor="middle" font-family="system-ui" font-size="12" font-weight="700" fill="#1C3247">erasure sonrası tipli metotla ham imzayı bağlar</text>
</svg>
```

### Mülakatta ne sorulur?

- **`getClass()` sınavı.** Farklı tip argümanlı iki koleksiyon aynı `Class` nesnesini döndürür (ör. `ArrayList.class`); `getClass()` elemanların değil koleksiyonun sınıfını verir ve hata da vermez. Bu, generics'in reified OLMADIĞININ en doğrudan kanıtıdır.
- **`List<? extends Number>` referansına neden `add(1)` yapılamaz?** Liste gerçekte `List<Double>` olabilir; derleyici gerçek eleman tipini bilemediği için `null` dışında hiçbir değere izin vermez. Sorun tip hiyerarşisi değildir (`Integer` elbette `Number`'ın alt tipidir) ve `add` metodunun yokluğu da değildir; sorun jokerin bilinmezliğidir. Liste sihirli biçimde salt-okunur bir kopyaya da dönüşmez — kısıt tamamen derleme-zamanı tip garantisidir. Yazmak istiyorsan `? super Number` kullan.
- **PECS imzası.** `copy(List<? super T> dest, List<? extends T> src)`. Ters yazılırsa imza tek başına derlenir ama gövde çalışmaz: `? extends T` bir hedefe yazmaya izin vermez. `List<T>, List<T>` çalışır fakat `List<Object>` hedefine `List<Integer>` kopyalamayı engelleyecek kadar katıdır; sınırsız `List<?>` hedefine ise hiç yazılamaz.
- **Silinmiş imza çakışması (erasure clash).** `void f(List<String>)` ile `void f(List<Integer>)` aynı sınıfta birlikte tanımlanamaz; ikisi de `f(List)`'e iner, imzalar ÖZDEŞLEŞİR ve derleyici "same erasure" hatası verir. İki tuzağa dikkat: dönüş tipi Java'da zaten aşırı yükleme (overloading) ayrımı sağlamaz; ve generic parametreli aşırı yükleme genel olarak yasak değildir — `f(List<String>)` ile `f(Set<String>)` sorunsuz birlikte yaşar. Çözüm metot adını değiştirmektir.
- **Tip token (type token).** Çalışma zamanında `T`'nin gerçek tipi gerekiyorsa onu AÇIKÇA taşımalısın: `<T> T oku(Class<T> tip, ...)`. `T.class` derlenmez, reflection erasure'ı geri getirmez (somut argüman bayt koda hiç yazılmaz) ve `getClass()` elindeki NESNE üzerinde çalışır — `T`'nin kendisini vermez, üstelik koleksiyon boşsa elinde nesne bile yoktur. Bu deseni JDK'da `Collections.checkedList`, Jackson'da `readValue(json, Kisi.class)` kullanır; karmaşık generic tipler için `TypeReference`/`TypeToken` alt-sınıflama numarası silinen bilgiyi kısmen kurtarır.

### Tek cümlede

Generics yalnız derleyicinin uyguladığı bir sözleşmedir; JVM'e inerken tip argümanları silindiği için esnekliği joker karakterlerle (PECS), çalışma-zamanı tip bilgisini ise açıkça taşıdığın `Class` token'larıyla elde edersin.

# gen-ileri-1 | Orta
## Soru
`List<String>` ve `List<Integer>` için `list.getClass()` çağrılırsa ne olur?
## A
Sırasıyla `List<String>.class` ve `List<Integer>.class` sınıf nesneleri döner
🤖 Böyle ayrı sınıf nesneleri yoktur; tip argümanı çalışma-zamanında mevcut değildir.
## B
Biri eleman tipi olan `String`, diğeri `Integer` sınıfını döndürür
🤖 `getClass()` elemanların değil, koleksiyonun sınıfını döndürür; eleman tipiyle ilgisi yok.
## C
İkisi de AYNI `Class` nesnesini döner (ör. `ArrayList.class`)
🤖 Doğru: tip silme (type erasure) yüzünden iki liste çalışma-zamanında aynı sınıftır.
## D
Derleme hatası verir; `getClass()` generic tipli referanslarda çağrılamaz
🤖 `getClass()` her nesnede çağrılabilir; hata olmaz, sonuç aynı sınıftır.
## Doğru
C
## Açıklama
**Tip silme (type erasure)** gereği `List<String>` ve `List<Integer>` çalışma-zamanında tamamen aynı ham (raw) sınıftır. `getClass()` her ikisinde de aynı `Class` nesnesini (örn. `ArrayList.class`) döndürür ve `new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass()` ifadesi `true`'dur. Tip argümanı yalnız derleyicinin gördüğü bir bilgidir; bayt koduna yansımaz. Bu, generics'in reified OLMAMASININ en doğrudan kanıtıdır ve `instanceof List<String>` gibi kontrollerin neden yasak olduğunu açıklar.

# gen-ileri-2 | İleri
## Soru
`Collections.copy` benzeri bir metotta PECS ilkesine göre imza nasıl olmalıdır?
## A
`copy(List<? extends T> dest, List<? super T> src)`
🤖 Ters çevrilmiş: hedefe yazılır (super), kaynaktan okunur (extends); bu imza tam tersini söylüyor.
## B
`copy(List<? super T> dest, List<? extends T> src)`
🤖 Doğru: tüketici hedef `super`, üretici kaynak `extends` — Producer Extends, Consumer Super.
## C
`copy(List<T> dest, List<T> src)`
🤖 Çalışır ama gereksiz katıdır; `Object` hedefe `Number` kopyalanmasını engeller, esneklik kaybolur.
## D
`copy(List<?> dest, List<?> src)`
🤖 Sınırsız joker ile hedefe yazamazsın; derleyici `add` çağrısını reddeder.
## Doğru
B
## Açıklama
**PECS: Producer Extends, Consumer Super.** Kaynak (`src`) veri ÜRETIR — ondan okuruz — bu yüzden `? extends T`. Hedef (`dest`) veri TÜKETIR — ona yazarız — bu yüzden `? super T`. Böylece `List<Object>` hedefine `List<Integer>` kaynağından kopyalama gibi esnek çağrılar mümkün olur. A şıkkı jokerleri ters kullanır: imzası tek başına derlenir ama gövdesi işe yaramaz — ters yönde `dest.add(src.get(0))` yazmak derleme hatası verir, çünkü `? extends T` bir hedefe yazmaya izin vermez. `List<T>` (C) çalışır ama esnekliği yok eder. Sınırsız `List<?>` (D) hedefe yazmayı imkânsızlaştırır çünkü derleyici hangi tipin güvenli olduğunu bilemez. Bu imza `Collections.copy`'nin gerçek JDK imzasıdır.

# gen-ileri-3 | İleri
## Soru
`static <T extends Comparable<T>> void islem(int size)` metodunun gövdesinde `T[] dizi = new T[size];` yazılırsa ne olur?
## A
Derleme hatası: generic dizi oluşturma (generic array creation) yasaktır
🤖 Doğru: `T` silindiği için JVM diziye gömülecek gerçek tipi bilemez, derleyici reddeder.
## B
Sorunsuz derlenir ve `Object[]` gibi davranır
🤖 Reddedilir; derleyici bunu sessizce `Object[]`'e çevirmez, açıkça hata verir.
## C
Çalışır ama `NullPointerException` fırlatır
🤖 Kod derlenmediği için çalışma-zamanına hiç ulaşılmaz.
## D
Sınır (`extends Comparable<T>`) sayesinde derleyici ifadeyi `new Comparable[size]`'a çevirir, hata olmaz
🤖 Silme metot İMZASINDA olur; `new T[...]` ifadesi sınır olsun olmasın her durumda yasaktır.
## Doğru
A
## Açıklama
`new T[size]` her zaman **derleme hatasıdır** (generic array creation). Nedeni diziler ile generics'in bellek modelidir: diziler **reified**'dir — çalışma-zamanında eleman tipini taşırlar ve yanlış tip yazılınca `ArrayStoreException` fırlatırlar. Generics ise **silinir** (erasure). `new T[]` yaratılabilseydi, JVM diziye hangi gerçek tipi gömeceğini bilemezdi. Sınırlı bir tip parametresi de kurtarmaz (D yanlış): silme, metodun İMZASINDA `T`'yi `Comparable`'a indirir ama `new T[...]` İFADESI dil düzeyinde her koşulda yasaktır. Standart geçici çözüm `(T[]) new Object[size]` (denetimsiz/unchecked uyarıyla) ya da `Array.newInstance(clazz, size)` ile `Class<T>` token'ı geçmektir. Bu, generics ile dizilerin neden "kötü karıştığının" temel örneğidir.

# gen-ileri-4 | Orta
## Soru
`? extends Number` tipli bir `List<? extends Number> list` referansına neden `list.add(1)` yapılamaz?
## A
`Integer` sınıfı `Number`'ın alt tipi olmadığından derleyici bu dönüşümü reddeder
🤖 `Integer` gerçekte `Number`'ın alt tipidir; sorun tip hiyerarşisi değil, jokerin bilinmezliğidir.
## B
Joker tipli bir referansta `add` metodu `List` arayüzünde hiç tanımlı olmadığı için çağrı bulunamaz
🤖 `add`, `List`'te tanımlıdır ve çağrı bulunur; engel metodun yokluğu değil, joker tip güvenliğidir.
## C
`? extends` yazılan her liste otomatik olarak salt-okunur (immutable) bir kopyaya dönüştüğü için
🤖 Böyle bir otomatik kopyalama yoktur; kısıt nesneden değil, derleme-zamanı tip garantisinden gelir.
## D
Derleyici gerçek eleman tipini bilemez (liste `List<Double>` de olabilir), güvenle yazamaz
🤖 Doğru: `? extends` üreticidir; okuma güvenli, yazma güvensizdir (yalnız `null` eklenebilir).
## Doğru
D
## Açıklama
`List<? extends Number>` "eleman tipi `Number` VEYA bir alt tipi olan, ama HANGISI olduğu bilinmeyen bir liste" demektir. Liste gerçekte `List<Double>` olabilir; buna `Integer` eklemek bozulmaya yol açardı. Derleyici bu belirsizlik yüzünden `null` dışında hiçbir değerin `add`'ine izin vermez — bu bilinçli bir tip güvenliği kararıdır. `? extends` bir ÜRETICIDIR: ondan güvenle `Number` OKUYABILIRSIN ama içine güvenle YAZAMAZSIN. Yazmak istiyorsan `? super Number` (tüketici) kullanmalısın. Bu, PECS kuralının pratikte neden gerekli olduğunu gösterir.

# gen-ileri-5 | İleri
## Soru
Silme (erasure) yüzünden AŞAĞIDAKI iki metot neden aynı sınıfta birlikte tanımlanamaz? `void f(List<String> l)` ve `void f(List<Integer> l)`
## A
İki metodun da dönüş tipi `void` olduğu, yani imzalar dönüş tipiyle ayrışamadığı için
🤖 Dönüş tipi Java'da aşırı yükleme (overload) ayrımı için zaten kullanılmaz; asıl sorun bu değil.
## B
Parametreler ham tipe (raw type) indiği için derleyici onları FARKLI imzalar sayar ve karar veremez
🤖 Ham tipe inmek imzaları farklılaştırmaz, tam tersine ikisini de `List`'te ÖZDEŞLEŞTIRIR.
## C
Silme sonrası iki imza da `f(List)` olur; bu silinmiş imza çakışması derleme hatası verir
🤖 Doğru: `List<String>` ve `List<Integer>` `List`'e silinince imzalar özdeşleşir (erasure clash).
## D
Java, parametresi generic olan metotlarda aşırı yüklemeye (overloading) hiçbir zaman izin vermediği için
🤖 İzin verir: `f(List<String>)` ile `f(Set<String>)` sorunsuz birlikte tanımlanır; sorun bu özel çakışmadadır.
## Doğru
C
## Açıklama
Aşırı yükleme (overloading) için iki metodun imzaları FARKLI olmalıdır. Ancak **type erasure** sonrası `f(List<String>)` ve `f(List<Integer>)` ikisi de `f(List)` imzasına iner — birebir çakışırlar (name clash / erasure clash). JVM bayt kodunda iki farklı metot üretilemeyeceği için derleyici bunu reddeder. Bu, "generics çalışma-zamanında yoktur" ilkesinin doğrudan bir sonucudur: derleyici için farklı görünen imzalar, JVM için özdeştir. Çözüm metot adlarını farklılaştırmaktır (örn. `fStrings`, `fInts`).

# gen-ileri-6 | İleri
## Soru
`static <T> T ilkEleman(List<T> list)` gibi generic bir metot içinde `T`'nin çalışma-zamanı `Class` nesnesine erişmek için ne gerekir?
## A
`T.class` yazmak yeterlidir
🤖 `T.class` derlenmez; `T` silindiği için çalışma-zamanında böyle bir tip token'ı yoktur.
## B
Ayrıca bir `Class<T> tip` parametresi geçmek gerekir (tip token)
🤖 Doğru: silme yüzünden gerçek tipi ancak dışarıdan bir `Class<T>` token'ı taşıyabilir.
## C
`getClass()` çağrısı doğrudan `T`'yi verir
🤖 `getClass()` eleman/nesne üzerinde çalışır; `T`'nin kendisini statik olarak vermez, ayrıca liste boşsa eleman da yoktur.
## D
Reflection ile `T` otomatik reified hale gelir
🤖 Reflection erasure'ı geri getirmez; `T`'nin somut argümanı bayt kodunda hiç saklanmaz.
## Doğru
B
## Açıklama
Generics silindiği için (**not reified**) `T`, çalışma-zamanında hiçbir tip bilgisi taşımaz; `T.class` veya `new T()` yazılamaz. Gerçek tipi öğrenmenin standart yolu **tip token** desenidir: metoda ayrıca `Class<T> tip` parametresi geçilir (örn. `<T> T oku(Class<T> tip, ...)`). Bu, JDK'da `Collections.checkedList`, Jackson'ın `readValue(json, MyType.class)` ve Spring'in birçok API'sinde kullanılan yaygın çözümdür. Alternatif olarak, karmaşık generic tipler için `TypeReference`/`TypeToken` (Gson, Jackson) alt-sınıflama numarasıyla silinmiş bilgiyi kısmen kurtarır. Özet: silinen tip bilgisini geri getirmenin tek yolu onu AÇIKÇA taşımaktır.
