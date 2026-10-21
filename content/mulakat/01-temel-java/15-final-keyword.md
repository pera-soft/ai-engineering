---
slug: final-keyword
baslik: "final Anahtar Sözcüğü: Değişken, Metot, Sınıf ve Değişmezlik"
kademe: Temel
sira: 15
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir eve taşındığını düşün. Kapıya kilit takarsın, ama neyi kilitlediğin önemlidir: kapının kendisini mi, yoksa evin içindeki eşyaları mı? Kapıyı kilitlemek eşyaların yerinin değişmesini engellemez. Java'da `final` tam olarak böyle bir kilittir: "bu bir daha değişmeyecek" der, ama **neyin** değişmediği kullanıldığı yere göre farklıdır. Amacı, kodu okuyan kişinin bir değerin ya da davranışın sonradan değişmeyeceğinden emin olmasıdır.

### Temel kavramlar

`final` üç yerde kullanılır ve üçünde farklı şey kilitlenir:

- **final değişken**: bir kez atanır, **yeniden atanamaz**. İlkel (primitive, örn. `int`) tipte bu değerin sabitliğidir; **nesne referansında ise yalnızca REFERANS sabittir** — işaret ettiği nesnenin içi (durum / state) yine değişebilir.
- **final metot**: alt sınıf onu **override edemez** (ezemez). Metot normal şekilde çağrılır ve kalıtımla alt sınıfa geçer; yalnızca yeniden tanımlanamaz. Tipik kullanım: güvenlik açısından kritik bir şablon metodun adımlarını kilitlemek.
- **final sınıf**: **kalıtımla genişletilemez** (`extends` edilemez); örn. `String`.

```svg
<svg viewBox="0 0 640 170" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="final üç yerde: değişken, metot, sınıf">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">final'in üç kullanımı</text>
  <rect x="24" y="44" width="184" height="104" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">final değişken</text>
  <text x="116" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yeniden atanamaz</text>
  <text x="116" y="118" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tek kez atanır</text>
  <text x="116" y="135" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(referans sabit)</text>
  <rect x="228" y="44" width="184" height="104" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">final metot</text>
  <text x="320" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">override edilemez</text>
  <text x="320" y="118" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">alt sınıf ezemez</text>
  <rect x="432" y="44" width="184" height="104" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="524" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">final sınıf</text>
  <text x="524" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">extends edilemez</text>
  <text x="524" y="118" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">örn. String</text>
</svg>
```

### Nasıl çalışır?

`final` bir değişken **tam olarak bir kez** atanabilir; ikinci atama **derleme hatasıdır**, çalışma-zamanına kalmaz:

```java
final int x = 5;
x = 6;            // DERLEME HATASI — final değişken yeniden atanamaz
```

Ayrıca `final` bir yerel değişken kullanılmadan önce mutlaka atanmış olmalıdır; hiç atanmadan okunursa derleyici *"variable x might not have been initialized"* der.

Nesne referansında kilit **oktadır, nesnenin kendisinde değil**:

```java
final List<String> liste = new ArrayList<>();
liste.add("a");             // SERBEST — nesnenin içi değişiyor
liste.add("b");             // SERBEST
liste = new ArrayList<>();  // DERLEME HATASI — referans yeniden atanıyor
// liste artık [a, b]
```

Bu, en sık düşülen yanılgıdır: `final` bir koleksiyonun **değiştirilemez (immutable) olduğu anlamına GELMEZ**. Gerçekten değiştirilemez bir liste istiyorsan `List.of(...)` veya `Collections.unmodifiableList(...)` kullanmalısın — bunlarda `add` çağrısı `UnsupportedOperationException` fırlatır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="final referans sabit ama nesnenin içi değişebilir">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">final referans: kilit oktadır, nesnede değil</text>
  <rect x="40" y="60" width="150" height="70" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="115" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">final List liste</text>
  <text x="115" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">referans kilitli</text>
  <rect x="410" y="52" width="200" height="90" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="510" y="82" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">ArrayList nesnesi</text>
  <text x="510" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">liste.add(x) SERBEST</text>
  <text x="510" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">içerik değişebilir</text>
  <path d="M190 95 L410 95" stroke="#186399" stroke-width="2" marker-end="url(#fnArrow)"/>
  <text x="300" y="86" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">bu ok sabit</text>
  <text x="115" y="164" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">liste = new ... YASAK</text>
  <defs>
    <marker id="fnArrow" markerWidth="10" markerHeight="10" refX="8" refY="4" orient="auto">
      <path d="M0 0 L9 4 L0 8 z" fill="#186399"/>
    </marker>
  </defs>
</svg>
```

İki incelik daha vardır. **Blank final**, bildirildiği yerde değer atanmayan (`private final int id;`) ama **constructor bitmeden kesinlikle bir kez atanan** `final` alandır. Derleyici tüm constructor yollarında alanın **tam olarak bir kez** atandığını zorlar; hiç atanmazsa ya da iki kez atanırsa derleme hatası verir. Böylece hem esneklik (değeri kurulumda belirlemek) hem `final` güvencesi birlikte elde edilir. Blank final bir instance alanıdır; `static` blok gerektirmez ve varsayılan değerde (`0`/`null`) kalmaz.

**Effectively final**, açıkça `final` yazılmadığı hâlde ilk atamadan sonra hiç yeniden atanmayan yerel değişkendir. Lambda'lar ile anonim (anonymous) ve iç (inner) sınıflar, çevreleyen metodun yerel değişkenlerini **yalnızca effectively final iseler** yakalayabilir (capture):

```java
int sayac = 0;
Runnable r = () -> System.out.println(sayac);
sayac = 5;   // DERLENMEZ — sayac artık effectively final değil
```

Sebep, lambda'nın değişkenin bir **kopyasını (snapshot)** yakalamasıdır; sonradan değişen bir değeri tutarlı biçimde yakalayamaz. Çözüm: değeri hiç değiştirme, ya da tek elemanlı dizi veya `AtomicInteger` gibi bir sarmalayıcı kullan.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="lambda yalnız effectively final yerel değişkeni yakalar">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">effectively final: lambda değeri anlık (snapshot) yakalar</text>
  <rect x="30" y="66" width="180" height="60" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">yerel: int sayac</text>
  <text x="120" y="111" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bir daha atanmazsa OK</text>
  <rect x="360" y="66" width="250" height="60" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="485" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">lambda / iç sınıf</text>
  <text x="485" y="111" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değerin kopyasını yakalar</text>
  <path d="M210 96 L360 96" stroke="#186399" stroke-width="2" marker-end="url(#fn2Arrow)"/>
  <text x="285" y="88" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">capture</text>
  <text x="320" y="160" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C0392B" font-family="system-ui">sonradan sayac = 5 → effectively final bozulur, DERLENMEZ</text>
  <defs>
    <marker id="fn2Arrow" markerWidth="10" markerHeight="10" refX="8" refY="4" orient="auto">
      <path d="M0 0 L9 4 L0 8 z" fill="#186399"/>
    </marker>
  </defs>
</svg>
```

### Mülakatta ne sorulur?

**`final` metot ve `abstract` bir arada kullanılamaz.** `abstract` "alt sınıf doldurmalı", `final` "kimse değiştiremez" der; derleyici bu çelişkiyi reddeder. `final` metodun tanımlanabilmesi için sınıfın `final` olması da gerekmez — tersine, `final` bir sınıfın metotlarını ayrıca `final` yapmak gereksizdir, çünkü zaten alt sınıf türetilemez.

**`final` tek başına değişmezlik (immutability) garantisi vermez.** Bu, "gerekli ama yeterli değil" tipik sorusudur. `final` alan değiştirilebilir bir nesneyi (`Date`, `List`) gösteriyorsa ve bu referans constructor'a dışarıdan alınıp doğrudan saklanırsa ya da getter'la dışarı verilirse, çağıran kod nesnenin içini değiştirerek "değişmez" sınıfın durumunu bozar. Sağlam immutability reçetesi:

1. sınıfı `final` yap,
2. tüm alanları `private final` yap,
3. kurulumdan sonra durumu değiştiren metot koyma,
4. giren ve çıkan değiştirilebilir nesneleri **savunmacı kopyala (defensive copy)**.

`final` **derinlemesine (deep) immutability** sağlamaz; gösterdiği nesneyi otomatik olarak değişmez yapmaz. Alanları `static final` yapmak da çözüm değildir: `static` paylaşımla ilgilidir, değişmezlikle değil.

Diğer sık tuzaklar: `final` bir öneri değil bağlayıcı bir kuraldır ve ihlali derleme zamanında yakalanır (`IllegalStateException` gibi bir çalışma-zamanı hatası beklenmez); `final` bir listede `add` çağrı sayısı sınırlanmaz, hepsi serbesttir.

### Tek cümlede

`final` yeniden atamayı, ezmeyi ve kalıtımı kapatır — ama bir nesnenin içini kilitlemez; gerçek değişmezlik için savunmacı kopya da gerekir.

# final-1 | Temel
## Soru
`final int x = 5;` tanımından sonra `x = 6;` yazılırsa ne olur?
## A
Derleme hatası verir; `final` değişken yeniden atanamaz
🤖 Doğru: `final` bir değişkene yapılan ikinci atama derleme zamanında reddedilir.
## B
`x` sessizce `6` olur; `final` yalnızca bir öneridir
🤖 `final` bağlayıcıdır; yeniden atama sessizce geçmez.
## C
Çalışma-zamanında `IllegalStateException` fırlatır
🤖 Hata derleme zamanında yakalanır; çalışma-zamanına kalmaz.
## D
`x` çift değer (`5` ve `6`) tutar
🤖 Böyle bir davranış yok; değişken tek değer tutar ve yeniden atama yasaktır.
## Doğru
A
## Açıklama
`final` bir değişken **tam olarak bir kez** atanabilir; ikinci atama **derleme hatasıdır** (çalışma-zamanına kalmaz). Bu, kodun okunmasını kolaylaştırır: bir `final` değişkeni görünce değerinin bir daha değişmeyeceğini bilirsin. İlkel (`int`) tipte bu, değerin sabitliği demektir. Not: `final` bir yerel değişken kullanılmadan önce mutlaka atanmış olmalıdır; hiç atanmadan okunmaya çalışılırsa yine derleme hatası alınır ("variable x might not have been initialized").

# final-2 | Temel
## Soru
`final` bir metot ile ilgili aşağıdakilerden hangisi doğrudur?
## A
Alt sınıf tarafından override (ezme) edilemez
🤖 Doğru: `final` metodun amacı ezilmeyi engellemektir.
## B
Hiç çağrılamaz, yalnızca tanımlanır
🤖 `final` metot normal çağrılır; yalnız override kapatılır.
## C
Yalnızca `final` sınıfların içinde tanımlanabilir
🤖 Böyle bir kısıt yok; ayrıca `final` sınıfta metotları ayrıca `final` yapmak zaten gereksizdir.
## D
Otomatik olarak `abstract` sayılır ve alt sınıfta doldurulmak zorundadır
🤖 `final` ve `abstract` çelişir; `abstract` metot ezilmek zorundadır, `final` ezilemez.
## Doğru
A
## Açıklama
`final` bir metot **alt sınıfta override edilemez** — davranışının değişmeden kalacağını garanti etmek için kullanılır (örn. güvenlik açısından kritik bir şablon metodun adımlarını kilitlemek). Metot normal şekilde çağrılabilir ve kalıtımla alt sınıfa geçer; sadece yeniden tanımlanamaz. `final` ile `abstract` birlikte kullanılamaz (D): `abstract` "alt sınıf doldurmalı" der, `final` "kimse değiştiremez" der — mantıken çelişirler, derleyici de reddeder.

# final-3 | Orta
## Soru
Aşağıdaki kod derlenir mi, çalışır mı?

```java
final List<String> liste = new ArrayList<>();
liste.add("a");
liste.add("b");
```
## A
Derleme hatası; `final` liste değiştirilemez, `add` yasaktır
🤖 `final` sadece referansı kilitler; `add` nesnenin içini değiştirir, serbesttir.
## B
Çalışma-zamanında `UnsupportedOperationException` fırlatır
🤖 `ArrayList` değiştirilebilir; bu istisna değiştirilemez (immutable) listelerde olur.
## C
Sorunsuz derlenir ve çalışır; liste `[a, b]` olur
🤖 Doğru: referans sabit ama nesnenin iç durumu (eleman ekleme) değişebilir.
## D
İlk `add` çalışır, ikincisi `final` yüzünden reddedilir
🤖 `final` çağrı sayısını sınırlamaz; iki `add` de sorunsuz çalışır.
## Doğru
C
## Açıklama
Nesne referansında `final` yalnızca **referansı** kilitler: `liste = new ArrayList<>()` gibi yeniden atama yasaktır. Ama işaret edilen nesnenin **iç durumu** değişebilir — `liste.add(...)` nesnenin içeriğini değiştirir, referansı değil. Bu yüzden kod sorunsuz çalışır ve liste `[a, b]` olur. Bu ayrım kritik bir yanılgı kaynağıdır: `final` bir koleksiyonun **değiştirilemez** (immutable) olduğu anlamına GELMEZ. Gerçekten değiştirilemez istiyorsan `List.of(...)` veya `Collections.unmodifiableList(...)` kullanmalısın.

# final-4 | Orta
## Soru
Aşağıdaki kod neden derlenmez?

```java
int sayac = 0;
Runnable r = () -> System.out.println(sayac);
sayac = 5;
```
## A
Lambda gövdesinden `System.out.println` gibi void dönen metotlar çağrılamaz
🤖 `println` lambda gövdesinde serbestçe çağrılır; sorun o değil.
## B
Yakalanan yerel değişken effectively final olmalıdır; `sayac = 5` bu koşulu bozar
🤖 Doğru: yakalanan yerel değişken ilk atamadan sonra bir daha atanmamalı; `sayac = 5` bunu bozar.
## C
`Runnable` fonksiyonel arayüz olmadığı için lambda ile örneklenemez
🤖 `Runnable` tek soyut metotlu bir fonksiyonel arayüzdür; lambda ile yazılması olağandır.
## D
Lambda içinde kullanılan yerel değişkenin `int` değil `long` olması zorunludur
🤖 Böyle bir tip zorunluluğu yok; sorun değişkenin yeniden atanmasıdır.
## Doğru
B
## Açıklama
Lambda'lar ile anonim (anonymous) ve iç (inner) sınıflar, içinde bulundukları metodun yerel değişkenlerini yalnızca **effectively final** iseler yakalayabilir (capture): yani ya açıkça `final` olmalı ya da ilk atamadan sonra hiç değiştirilmemeli. Burada `sayac` lambda oluşturulduktan sonra `sayac = 5` ile yeniden atandığı için effectively final DEĞİLDİR ve derleyici lambda satırını reddeder. Sebep, lambda'nın değişkenin bir kopyasını (snapshot) yakalamasıdır; sonradan değişen bir değeri tutarlı yakalayamaz. Çözüm: değeri değiştirme, ya da tek elemanlı dizi / `AtomicInteger` gibi bir sarmalayıcı kullan.

# final-5 | Orta
## Soru
"Blank final" alan nedir?
## A
Değeri her zaman `null` veya `0` olan bir `final` alan
🤖 Blank final varsayılan değerde kalmaz; kesin bir kez atanır.
## B
Yalnızca `static` bloklarda kullanılabilen özel bir alan türü
🤖 Blank final instance alanıdır ve constructor'da atanır; static blok gerektirmez.
## C
`final` olmayan ama isimlendirme geleneği gereği büyük harfle yazılan alan
🤖 Bu bir stil konusudur; blank final gerçek bir `final` alandır.
## D
Tanımında atanmayıp constructor içinde (tam bir kez) atanan `final` alan
🤖 Doğru: bildirimde boş bırakılıp constructor'da kesin atanan `final` alandır.
## Doğru
D
## Açıklama
**Blank final**, bildirildiği yerde değer atanmayan (`private final int id;`) ama **constructor bitmeden kesinlikle bir kez atanan** bir `final` alandır. Bu, değeri constructor parametresine bağlı olan değişmez alanlar için idealdir: `this.id = uretilenId;`. Derleyici tüm constructor yollarında alanın **tam olarak bir kez** atandığını zorlar; atanmazsa veya iki kez atanırsa derleme hatası verir. Bu sayede hem esneklik (değeri kurulumda belirlemek) hem `final` güvencesi (sonradan değişmezlik) birlikte elde edilir.

# final-6 | İleri
## Soru
Bir sınıfın gerçekten değişmez (immutable) olması için `final` alanlar yeterli midir?
## A
Evet; tüm alanları `final` yapmak, başka hiçbir önlem almadan immutability için yeterlidir
🤖 Yeterli değil; `final` bir referansın işaret ettiği değiştirilebilir nesne yine sızabilir.
## B
Hayır; `final` alanın gösterdiği değiştirilebilir nesne dışarı sızarsa durum yine değişir — savunmacı kopya (defensive copy) şarttır
🤖 Doğru: `final` referansı kilitler, nesnenin içini değil; sızan referans immutability'yi bozar.
## C
Hayır; immutability için tüm alanların `static final` olarak tanımlanması gerekir
🤖 `static` paylaşımla ilgilidir, değişmezlikle değil; alanlar static olmak zorunda değil.
## D
Evet; `final` bir alanın gösterdiği nesneyi de otomatik olarak değişmez (immutable) yapar
🤖 `final` derinlemesine (deep) immutability sağlamaz; iç nesneler yine değişebilir.
## Doğru
B
## Açıklama
`final` alanlar immutability'nin **gerekli ama yeterli olmayan** parçasıdır. `final` yalnızca referansı kilitler; alan değiştirilebilir bir nesneyi (örn. `Date`, `List`) gösteriyorsa ve bu referans constructor'a dışarıdan alınıp doğrudan saklanır ya da getter'la dışarı verilirse, çağıran kod nesnenin içini değiştirerek "değişmez" sınıfın durumunu bozabilir. Sağlam immutability için: sınıfı `final` yap, tüm alanları `private final` yap, kurulumdan sonra durumu değiştiren metot koyma ve **giren/çıkan değiştirilebilir nesneleri savunmacı kopyala** (defensive copy). Bu yüzden yalnız `final` yazmak tek başına yetmez.
