---
slug: generics-temel
baslik: "Generics: Tip Güvenliği ve Tip Parametreleri"
kademe: Orta
sira: 26
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir depoda üstünde hiç etiket olmayan kutular düşün. Kutuya kim ne isterse atabiliyor. "Vida" sandığın kutuyu açıyorsun, elinde çekiç kalıyor — ve bunu ancak kutuyu AÇTIĞINDA anlıyorsun. Generic'ler (generics / tip parametreleri) bu kutulara silinmez etiket yapıştırmaktır: "buraya yalnız vida girer". Yanlış şeyi koymaya kalkan, daha kapağı kapatmadan durdurulur. Java'da "kapağı kapatma anı" kodun derlendiği an, "kutuyu açma anı" ise programın kullanıcıda çalıştığı andır. Generic'lerin tek amacı, hatayı ikincisinden birincisine taşımaktır.

### Temel kavramlar

- **Derleme zamanı (compile-time)**: yazdığın kaynak kodun Java'nın çalıştırdığı ara koda (bytecode) çevrildiği an. Burada yakalanan hatayı sen görürsün, program hiç başlamaz.
- **Çalışma zamanı (runtime)**: programın gerçekten çalıştığı an. Buradaki hatayı kullanıcı görür; bu yüzden çok daha pahalıdır.
- **Tür dönüşümü (cast)**: `(String) x` yazımı "bu nesneye `String` gözüyle bak" demektir. Nesne gerçekten `String` değilse çalışma zamanında `ClassCastException` fırlar.
- **Tip parametresi (`<T>`)**: bir sınıfa ya da metoda "gerçek tür sonradan verilecek" diyen değişken. `T` bir tür değil, tür için ayrılmış boş yerdir.
- **Tip argümanı**: o boş yeri dolduran gerçek tür. `List<String>` yazdığında `T` yerine `String` koymuş olursun.
- **Ham tip (raw type)**: açılı parantezleri hiç yazmamak — `List l = new ArrayList();`. Etiketsiz kutu tam olarak budur.

Generic'ler Java 5 ile geldi. Öncesinde koleksiyonlar her şeyi `Object` olarak tutardı:

```java
List isimler = new ArrayList();          // ham tip: etiketsiz kutu
isimler.add("Ada");
isimler.add(42);                         // derleyici İZİN VERİR (yalnız uyarı)
String ilk = (String) isimler.get(1);    // burada patlar
```

Çıktı: `Exception ... ClassCastException: Integer cannot be cast to String`

Aynı kodun etiketli hali:

```java
List<String> isimler = new ArrayList<>();
isimler.add("Ada");
isimler.add(42);                         // DERLEME HATASI — program hiç çalışmaz
```

### Nasıl çalışır?

`List<String>` yazdığında derleyiciye "bu liste yalnız `String` tutar" dersin. Derleyici iki iş yapar: yanlış türü eklemeyi reddeder, ve okurken gereken cast'i senin yerine kendisi ekler. Yani cast kaybolmaz; görünmez ve garantili doğru olur.

Tip parametresi hem sınıfa hem metoda verilebilir:

```java
class Kutu<T> { ... }                                // generic sınıf
<T> T ilk(List<T> liste) { return liste.get(0); }    // generic metot
```

Generic metotta tip parametresi **dönüş tipinden ÖNCE** bildirilir: `<T> T ilk(...)`. Sınıfın kendisi generic değilse `T` başka hiçbir yerde tanıtılmadığı için `T ilk(List<T> l)` derlenmez; `<T> ilk(List<T> l)` de dönüş tipi eksik olduğundan derlenmez. `Object ilk(List<Object> l)` yazmak çözüm değildir: derlenir ama tip güvenliğini `Object`'e düşürür ve aşağıdaki değişmezlik kuralı yüzünden `List<String>` argümanını bile kabul etmez.

**Elmas operatörü** (diamond `<>`, Java 7) sağdaki tip argümanlarını soldaki bildirimden **çıkarım** (inference) ile doldurur:

```java
Map<String, List<Integer>> m = new HashMap<>();   // sağda tekrar yazmaya gerek yok
```

Diamond tip güvenliğini tam korur. `new HashMap()` (açılı parantez hiç yok) ise ham tiptir ve kontrolü kapatır — ikisi birbirinin tam zıddıdır. Çıkarım soldaki hedef bildirimin türünü kullanır; türü `Object`'e genişletmez.

**Tip silme (type erasure)**: generic tür bilgisi yalnız derleyicide yaşar, bytecode'a yazılmaz. Çalışma zamanında `List<String>` ile `List<Integer>` AYNI `List`'tir. Doğrudan sonuçları:

- `new T()`, `T.class`, `new T[]`, `x instanceof T` derlenmez — runtime'da `T`'nin ne olduğu bilinmez.
- Çare tür bilgisini elle taşımaktır: metoda `Class<T> clazz` parametresi geçip `clazz.getDeclaredConstructor().newInstance()` çağırmak.
- Generics runtime'da ne ek hız ne ek bellek kazancı getirir; bytecode neredeyse aynıdır. (Nesne paylaşımıyla gelen bellek kazancı ayrı bir konudur: String havuzu ve Integer önbelleği.)

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tip silme: derleme-zamanı tür denetlenir, bytecode'da tür silinir">
  <defs>
    <marker id="generics-ok" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tip silme (type erasure)</text>
  <rect x="24" y="44" width="244" height="146" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="146" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Derleme-zamanı</text>
  <text x="146" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">derleyici türü bilir ve denetler</text>
  <rect x="46" y="100" width="200" height="32" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="146" y="121" text-anchor="middle" font-size="12.5" fill="#1C3247" font-family="system-ui">List&lt;String&gt;</text>
  <rect x="46" y="144" width="200" height="32" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="146" y="165" text-anchor="middle" font-size="12.5" fill="#1C3247" font-family="system-ui">List&lt;Integer&gt;</text>
  <line x1="276" y1="117" x2="384" y2="117" stroke="#5D7789" stroke-width="1.6" marker-end="url(#generics-ok)"/>
  <text x="330" y="110" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">javac</text>
  <text x="330" y="134" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tür bilgisi silinir</text>
  <rect x="392" y="44" width="244" height="146" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="514" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Bytecode / runtime</text>
  <text x="514" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tür yok — ikisi de aynı</text>
  <rect x="414" y="108" width="200" height="34" rx="6" fill="#FFFFFF" stroke="#B26B00" stroke-width="1.2"/>
  <text x="514" y="130" text-anchor="middle" font-size="12.5" fill="#1C3247" font-family="system-ui">List (ham / Object)</text>
  <text x="514" y="166" text-anchor="middle" font-size="9.5" fill="#B26B00" font-family="system-ui">new T(), T.class, instanceof T yasak</text>
</svg>
```

**Değişmezlik (invariance)**: `String`, `Object`'in alt türüdür — ama `List<String>`, `List<Object>`'in alt türü DEĞİLDİR.

```java
List<String> isimler = new ArrayList<>();
List<Object> nesneler = isimler;   // DERLENMEZ
```

İzin verilseydi `nesneler.add(42)` ile aynı listeye `Integer` sızar, sonraki `String` okuması `ClassCastException` verirdi. Bu kasıtlı bir güvenlik kararıdır. Sınıf hiyerarşisindeki normal genişletme (`Object o = "x";`) hâlâ serbesttir; yasak olan yalnız generic parametrede kovaryanstır.

Esneklik gerekince **joker** (wildcard) kullanılır: kaynaktan yalnız OKUYACAKSAN `List<? extends Sayi>` (kovaryans; `get` var, `add` yok), hedefe yalnız YAZACAKSAN `List<? super Integer>` (kontravaryans; `add` var, `get` `Object` döner). Kısaca **PECS**: Producer Extends, Consumer Super.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Generic değişmezlik ve PECS kuralı: Producer Extends, Consumer Super">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Değişmezlik (invariance) ve PECS</text>
  <rect x="24" y="40" width="612" height="66" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="40" y="62" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Değişmez: List&lt;String&gt;, List&lt;Object&gt;'in alt türü DEĞİLDİR</text>
  <text x="40" y="82" font-size="10.5" fill="#1C3247" font-family="system-ui">String, Object'in alt türü olsa da List&lt;String&gt; → List&lt;Object&gt; ATANAMAZ</text>
  <text x="40" y="98" font-size="10" fill="#5D7789" font-family="system-ui">(izin verilseydi listeye Integer sızar, tip güvenliği kırılırdı)</text>
  <rect x="24" y="120" width="298" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="173" y="144" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Producer Extends</text>
  <text x="173" y="166" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">List&lt;? extends Sayi&gt;</text>
  <text x="173" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaynaktan OKU (üretici)</text>
  <text x="173" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">get ✓  ·  add ✗</text>
  <rect x="338" y="120" width="298" height="112" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="487" y="144" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Consumer Super</text>
  <text x="487" y="166" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">List&lt;? super Integer&gt;</text>
  <text x="487" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hedefe YAZ (tüketici)</text>
  <text x="487" y="210" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">add ✓  ·  get → Object</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Generics hangi problemi çözer?"** → Tip hatasını runtime'dan compile-time'a taşır. Performans ya da bellek kazancı DEĞİLDİR; runtime tür bilgisine erişim de değildir (tam tersine silinir).
- **"Ham tip ne yapar?"** → Tüm generic tip kontrolünü kapatır; derleyici yalnız uyarı verir, kod generics öncesi kırılganlığa döner. Diamond ile ham tipi karıştırmak en sık tuzaktır.
- **"`new T()` neden derlenmez?"** → Tip silme; `T.class`, `new T[]`, `instanceof T` de aynı sebeple yasaktır.
- **"Generic metot nasıl bildirilir?"** → `<T> T ad(List<T> l)`; `<T>` dönüş tipinden önce gelir.
- **"`List<String>` neden `List<Object>`'e atanamaz?"** → Değişmezlik; tip güvenliğini korumak için. Esneklik joker + PECS ile alınır.

### Tek cümlede

Generics, "bu kap yalnız şu türü tutar" sözünü derleyiciye verdirip yanlış tür hatasını kullanıcının karşısından senin ekranına taşır — ama bu söz çalışma zamanında silinir (type erasure).

# gen-1 | Temel
## Soru
Generic'lerin (generics) çözmek için tasarlandığı temel problem nedir?
## A
Tip hatalarını çalışma-zamanından derleme-zamanına taşıyıp tür güvenliğini derleyicide zorlamak
🤖 Doğru: yanlış tür kullanımı `ClassCastException` olarak patlamadan, derlemede yakalanır.
## B
Programın çalışma-zamanı (runtime) performansını artırmak
🤖 Generics derleme-zamanı özelliğidir; tip silme (type erasure) nedeniyle runtime'da ek hız getirmez.
## C
Bellek kullanımını azaltmak için nesneleri paylaştırmak
🤖 Generics nesne paylaşımıyla ilgili değildir; bu String havuzu/Integer önbelleği konusudur.
## D
Yansıma (reflection) olmadan çalışma-zamanında tür bilgisine erişmek
🤖 Aksine; tip silme yüzünden generic tür bilgisi runtime'da SİLİNİR, erişilemez.
## Doğru
A
## Açıklama
Generics öncesi `List` `Object` tutardı: yanlış tür eklenirse hata ancak okuma anında `ClassCastException` ile ortaya çıkardı. `List<String>` ile derleyici yanlış türü eklemeyi derlerken reddeder ve okurken cast'i otomatik ekler. Yani generics **compile-time tip güvenliği** sağlar (A). Performans (B) kazandırmaz — tip silme sonrası bytecode neredeyse aynıdır. Runtime tür bilgisi (D) ise tam tersine silinir.

# gen-2 | Temel
## Soru
Aşağıdaki satırdaki `<>` (diamond / elmas operatörü) ne yapar?

```java
Map<String, List<Integer>> m = new HashMap<>();
```
## A
Boş bir map yaratır, tip sonradan atanır
🤖 Tip zaten sol taraftan bellidir; diamond boş bırakmaz, o türü çıkarır.
## B
Ham tip (raw type) bir `HashMap` yaratır ve tip kontrolünü kapatır
🤖 Ham tip, açılı parantez hiç yazılmadığında (`new HashMap()`) oluşur; diamond tam tersine tip güvenliğini korur.
## C
Sağ taraftaki tip argümanlarını sol taraftaki bildirimden çıkarım (inference) ile doldurur
🤖 Doğru: tekrar yazmadan `<String, List<Integer>>` türünü soldan çıkarır, tip güvenliğini korur.
## D
Tip argümanlarını `HashMap<Object, Object>`'e genişletir
🤖 Çıkarım soldaki hedef bildirimin türünü kullanır; `Object`'e genişletmez.
## Doğru
C
## Açıklama
Diamond (Java 7) sağ taraftaki tip argümanlarını sol taraftaki bildirimden **çıkarım** (type inference) ile doldurur; `<String, List<Integer>>`'i tekrar yazmaktan kurtarır ama tip güvenliğini tam korur (C). `new HashMap()` (açılı parantez hiç yazılmadan) yazsaydın **ham tip** (raw type) olurdu ve tip kontrolü kapanırdı — diamond bunun tam ZITTIDIR (B yanlış). Çıkarım soldaki hedef bildirimin türünü kullanır, `Object`'e genişletmez (D yanlış).

# gen-3 | Orta
## Soru
Aşağıdaki kodun en olası sonucu nedir?

```java
List isimler = new ArrayList();   // ham tip
isimler.add("Ada");
isimler.add(42);
String ilk = (String) isimler.get(1);
```
## A
3. satır derleme hatası verir çünkü `List` `Integer` kabul etmez
🤖 Ham tip tip kontrolünü kapatır; `add(42)` sorunsuz derlenir.
## B
Kod derlenir; 4. satırda çalışma-zamanında `ClassCastException` fırlar
🤖 Doğru: ham tip yüzünden hata runtime'a ötelenir, `(String)` cast'i Integer'da patlar.
## C
2. satır derleme hatası; ham `List`'e `String` eklenemez
🤖 Ham tipe her tür eklenebilir; sorun eklemede değildir.
## D
Hiç sorun olmaz; `get(1)` Integer'ı otomatik String'e çevirir
🤖 Otomatik çevrim yok; Integer'ı String'e cast runtime'da patlar.
## Doğru
B
## Açıklama
Ham tip (`List`, yani açılı parantez hiç yazılmadan) generic tip kontrolünü tamamen kapatır — derleyici `add(42)`'ye izin verir (yalnız uyarı). Hata çalışma-zamanına ötelenir: `get(1)` bir `Integer` döndürür, `(String)` cast'i `ClassCastException` fırlatır (B). Bu, generics'in engellemek için var olduğu klasik senaryodur. `List<String>` yazsaydın `add(42)` DERLEMEDE reddedilirdi — sorun hiç doğmazdı.

# gen-4 | Orta
## Soru
Generic OLMAYAN bir sınıf içinde, "herhangi bir tür listesinin ilk elemanını o türde döndüren" bir metodu DOĞRU tanımlayan bildirim hangisidir?
## A
`T ilk(List<T> liste)`
🤖 `T` tanıtılmamış; metot kendi tip parametresini dönüş tipinden önce `<T>` ile bildirmeli.
## B
`<T> ilk(List<T> liste)`
🤖 Dönüş tipi eksik; `<T>` yalnız tip parametresini bildirir, dönüş türü ayrıca yazılmalı.
## C
`Object ilk(List<Object> liste)`
🤖 Derlenir ama tip güvenliğini kaybeder: `List<String>` bile geçemez, dönüş `Object` olur.
## D
`<T> T ilk(List<T> liste)`
🤖 Doğru: `<T>` tip parametresini bildirir, sonra dönüş türü `T` ve `List<T>` argümanı gelir.
## Doğru
D
## Açıklama
Generic metotta tip parametresi, dönüş tipinden ÖNCE `<T>` ile bildirilir: `<T> T ilk(List<T> liste)`. Böylece çağrı `String ad = ilk(isimListesi)` biçiminde tür güvenli çalışır — derleyici `T`'yi argümandan çıkarır (D). A'da `<T>` bildirimi yoktur; sınıf da generic olmadığından `T` hiçbir yerde tanıtılmamıştır ve derlenmez. C tip güvenliğini `Object`'e düşürür ve `List<String>` argümanını bile kabul etmez. B'de dönüş türü eksiktir.

# gen-5 | Orta
## Soru
Aşağıdaki satır neden derlenmez?

```java
class Kutu<T> {
    T ornekYarat() { return new T(); }   // ??
}
```
## A
`T` yerine bir arayüz (interface) geçilebilir ve arayüzler `new` ile örneklenemez
🤖 Sebep daha temeldir: `T`'nin gerçek türü runtime'da hiç yoktur.
## B
Generic sınıflar `new` ile nesne örnekleyemez, yalnız statik fabrika metotları vardır
🤖 Generic sınıflar normal örneklenir; kısıt yalnız tip parametresinin kendisiyle ilgilidir.
## C
Tip silme (type erasure) nedeniyle `T`'nin gerçek türü runtime'da yoktur; ne örnekleneceği bilinemez
🤖 Doğru: `new T()`, `T.class`, `x instanceof T` hepsi aynı sebeple yasaktır.
## D
`T` bir tip parametresi olduğundan derleyici onu örtük olarak `abstract` sayar
🤖 Böyle bir kural yok; sorun `T`'nin runtime'da bilinmemesidir.
## Doğru
C
## Açıklama
Java generics **tip silme** (type erasure) ile uygulanır: derlemeden sonra `T` bilgisi silinir; bytecode'da `Kutu<String>` ile `Kutu<Integer>` aynıdır. Bu yüzden runtime'da `T`'nin ne olduğu bilinmez ve `new T()`, `T.class`, `new T[]`, `x instanceof T` derlenmez (C). Çözüm: `Class<T>` parametresi geçip `clazz.getDeclaredConstructor().newInstance()` çağırmak — yani tür bilgisini elle taşımaktır. Tip silme, generics'in en çok şaşırtan sonucudur.

# gen-6 | İleri
## Soru
Aşağıdaki ikinci satır neden derlenmez?

```java
List<String> isimler = new ArrayList<>();
List<Object> nesneler = isimler;   // ??
```
## A
Generic tipler değişmezdir (invariant): `List<String>`, `List<Object>`'in alt türü DEĞİLDİR
🤖 Doğru: izin verilseydi listeye `Integer` sızabilir, tip güvenliği kırılırdı.
## B
`String` `final` bir sınıf olduğundan `Object`'in alt türü sayılmaz
🤖 `String` `Object`'in alt türüdür; sorun türlerde değil, generic'in değişmezliğindedir (invariance).
## C
`ArrayList`, `List<Object>`'e atanamaz; önce cast gerekir
🤖 Sorun ArrayList/List değil; yasak olan `List<String>` → `List<Object>` ilişkisidir.
## D
Java referans atamada asla otomatik genişletme (widening) yapmaz
🤖 Sınıf hiyerarşisinde widening olur (`Object o = "x"`); yasak olan generic parametrelerde kovaryanstır.
## Doğru
A
## Açıklama
Generic tipler Java'da **değişmezdir** (invariant): `S`, `T`'nin alt türü olsa bile `List<S>`, `List<T>`'nin alt türü sayılmaz. Bu kasıtlı bir güvenlik önlemidir — izin verilseydi `nesneler.add(42)` ile `isimler` listesine `Integer` sızar, sonraki `String` okuması `ClassCastException` verirdi (A). Esneklik gerektiğinde joker (wildcard) kullanılır: okuma için `List<? extends Object>` (kovaryans), yazma için `List<? super String>` (kontravaryans) — PECS kuralı (Producer Extends, Consumer Super). B yanlıştır çünkü `String` gerçekten `Object`'in alt türüdür.
