---
slug: comparable-comparator
baslik: "Comparable vs Comparator: Sıralama Ölçütleri"
kademe: Temel
sira: 25
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Sınıftaki öğrencileri sıralayacaksın. Boya göre mi, ada göre mi, numaraya göre mi? Bazı şeylerin herkesin kabul ettiği **doğal** bir sırası vardır: sayılar küçükten büyüğe, kelimeler alfabetik. Bazı şeylerin ise yoktur — "kişi" nesnesinin tek doğru sırası yoktur, duruma göre değişir.

Java bu ikisini ayırır. **`Comparable`** nesnenin kendi içine gömülü, tek ve varsayılan sırayı tanımlar. **`Comparator`** ise dışarıdan verilen, istediğin kadar çoğaltabileceğin alternatif ölçüttür. Sıralama algoritmasının tek ihtiyacı vardır: "bu ikisinden hangisi önce gelir?" sorusuna cevap. Bu iki arayüz o cevabı verir.

### Temel kavramlar

- **`Comparable<T>`**: sınıfın **içine** yazılır, tek metodu **`compareTo(T o)`**'dur ve tipin **doğal sırasını** (natural ordering) tanımlar. `String`, `Integer` gibi tipler bunu uygular. Bir sınıfın yalnız **bir** doğal sırası olabilir — iki kez uygulanamaz.
- **`Comparator<T>`**: nesnenin **dışında** tanımlanır, tek metodu **`compare(a, b)`**'dir. Aynı tip için istediğin kadar farklı `Comparator` yazabilirsin (ada göre, yaşa göre, tersine...). İkisinde de tip kısıtı yoktur; sayı da string de nesne de sıralanır.
- **Sözleşme (contract)**: dönüş `int`'tir, `boolean` değil — ve yalnız **işareti** anlamlıdır. **Negatif** = `a` önce gelir (`a < b`); **0** = sıra bakımından eşit; **pozitif** = `a` sonra gelir (`a > b`).
- **Anti-simetri**: `sgn(a.compareTo(b)) == -sgn(b.compareTo(a))`. **Geçişlilik (transitivity)**: a > b ve b > c ise a > c.
- **Kararlı sıralama (stable sort)**: karşılaştırıcıya göre **eşit** (compare == 0) elemanların **göreli sırasının** korunması.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Comparable doğal sıra, Comparator dış ölçüt">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki sıralama yolu</text>
  <rect x="30" y="46" width="270" height="122" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Comparable (içeride)</text>
  <text x="165" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">int compareTo(T o)</text>
  <text x="165" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">doğal sıra · tek tanım</text>
  <text x="165" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sınıfa gömülü</text>
  <text x="165" y="156" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">örn: String, Integer</text>
  <rect x="340" y="46" width="270" height="122" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Comparator (dışarıda)</text>
  <text x="475" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">int compare(a, b)</text>
  <text x="475" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">alternatif · çoklu ölçüt</text>
  <text x="475" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">comparing / thenComparing</text>
  <text x="475" y="156" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">reversed()</text>
  <text x="320" y="190" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Dönüş: negatif (a&lt;b) · 0 (eşit) · pozitif (a&gt;b) — anti-simetrik, geçişli.</text>
</svg>
```

### Nasıl çalışır?

`Collections.sort(list)` ya da `list.sort(...)` çağırdığında algoritma elemanları defalarca ikişer ikişer karşılaştırır ve dönen sayının **işaretine** bakarak yerlerini belirler. Kaynak nereden gelir: argümansız çağrıda elemanın kendi `compareTo`'sundan (doğal sıra), `Comparator` verilirse ondan. `TreeSet`/`TreeMap` de argümansız kurulduğunda doğal sırayı ister.

Dönüş değeri **tam olarak** `-1`/`0`/`1` olmak zorunda değildir; `-17` de geçerli bir "önce"dir ve bellek adresiyle hiçbir ilgisi yoktur.

```svg
<svg viewBox="0 0 640 172" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="compareTo dönüş değerinin işareti: negatif, sıfır, pozitif">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">a.compareTo(b) işaretinin anlamı</text>
  <rect x="40" y="56" width="205" height="54" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="245" y="56" width="150" height="54" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.5"/>
  <rect x="395" y="56" width="205" height="54" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="142" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">negatif</text>
  <text x="142" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">a önce gelir (a &lt; b)</text>
  <text x="320" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#5D7789" font-family="system-ui">0</text>
  <text x="320" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sıra eşit</text>
  <text x="497" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">pozitif</text>
  <text x="497" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">a sonra gelir (a &gt; b)</text>
  <text x="320" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Tam olarak -1/0/1 şart değil: -17 de geçerli bir "önce"dir.</text>
  <text x="320" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Taşma tuzağı: return a - b yerine Integer.compare(a, b) kullan.</text>
</svg>
```

Çok ölçütlü sıralamanın modern yolu **kombinatörlerdir**: `Comparator.comparing(...)` birincil ölçütü, `.thenComparing(...)` birincil ölçüt **eşit** olduğunda başvurulacak ikincil ölçütü, `.reversed()` ise tersini verir. İlkel tiplerde gereksiz **kutulamadan** (boxing) kaçınmak için `comparingInt`/`comparingLong`/`comparingDouble` vardır.

```java
// önce yaşa göre artan, yaş eşitse ada göre alfabetik
list.sort(Comparator.comparingInt(Kisi::getYas)
                    .thenComparing(Kisi::getAd));
```

### Mülakatta ne sorulur?

- **Taşma (overflow) tuzağı**: `return a - b;` büyük/negatif sayılarda taşabilir ve işareti ters çevirebilir. Doğrusu **`Integer.compare(a, b)`**'dir.
- **`reversed()` kendinden önceki tüm zinciri ters çevirir.** `comparing(...).thenComparing(...).reversed()` ile yalnız tek bir ölçütü terslemek **aynı şey değildir**. Sadece ikincil ölçütü terslemek istersen: `Comparator.comparing(Kisi::getAd).thenComparing(Comparator.comparing(Kisi::getYas).reversed())`. Var olan bir `Comparator`'ın tersi için en temiz yol `adaGore.reversed()`'dır; `compare` gövdesini elle ters yazmak gereksiz ve hataya açıktır, `Collections.reverse(list)` ise yalnız son listeyi çevirir ve çok ölçütlü niyeti bozabilir.
- **`equals` ile tutarlılık kuvvetle önerilir**: ideal olarak `compareTo(b) == 0` ⟺ `equals(b)`. Neden kritik: **`TreeSet` ve `TreeMap` eşitliği `equals` ile değil, `compareTo`/`compare`'in `0` dönmesiyle** belirler ve asla `equals`'e geçmez. `compareTo` yalnız `id`'ye bakıyorsa, `id`'si aynı ama `ad`'ı farklı (yani `equals`'e göre **farklı**) iki nesne "aynı" sayılır: `TreeSet` ikincisini **eklemez**, `TreeMap` var olan anahtarı **ezer**. Bu, `Set` sözleşmesiyle çelişiyormuş gibi görünen sessiz bir hatadır; derleyici bu tutarlılığı zorlamaz, dikkat programcıya aittir.
- **Kararlılık (stability)**: Java'nın **nesne** sıralamaları (`Collections.sort`, `List.sort`, `Arrays.sort(nesne dizisi)` — TimSort) **kararlıdır**: `compare == 0` olan elemanlar girişteki göreli sıralarını korur. Bu, **çok geçişli** sıralamayı öngörülebilir kılar — önce ada, sonra yaşa göre sıralarsan aynı yaştakiler ada göre sıralı kalır — ve `thenComparing` zincirinin de temelidir. Kararlılık; zaman karmaşıklığı, değişmezlik (immutability) veya çökmezlik hakkında **hiçbir şey** söylemez. (İlkel dizilerde `Arrays.sort` quicksort kullanır ve kararlı değildir, ama orada eşit değerler ayırt edilemediği için fark gözlemlenemez.) Yine de sıralamayı iki ayrı `sort` çağrısıyla kurmak, kararlılığa bağımlı ve kırılgan bir hiledir; tek bir `Comparator` zinciri niyeti açıkça yazar.

### Tek cümlede

`Comparable` sınıfın içine gömülü tek doğal sırayı (`compareTo`), `Comparator` ise dışarıdan verilen çoklu alternatif ölçütü (`compare`) tanımlar; ikisi de yalnız **işaretle** konuşur — negatif önce, 0 eşit, pozitif sonra.

# cmp-1 | Temel
## Soru
`Comparable` ile `Comparator` arasındaki temel fark nedir?
## A
`Comparable` doğal sırayı sınıf içinde (`compareTo`), `Comparator` ölçütü sınıf dışında (`compare`) tanımlar
🤖 Doğru: biri gömülü tek doğal sıra, diğeri dışarıdan tanımlı çoklu alternatif ölçüt.
## B
`Comparable` yalnız sayılar için, `Comparator` yalnız stringler için kullanılır
🤖 İkisi de herhangi bir tip için kullanılır; tip kısıtlaması yoktur.
## C
`Comparator` sıralanacak sınıfın içine, `Comparable` ise ayrı bir sınıfta dışarıya yazılır
🤖 Tam ters: `Comparable` içeride (compareTo), `Comparator` dışarıda (compare).
## D
İkisi de tamamen aynıdır, isimleri dışında fark yoktur
🤖 Fark gerçektir: tanım yeri, metot adı ve çoklu-ölçüt yeteneği farklıdır.
## Doğru
A
## Açıklama
**`Comparable<T>`** bir tipin **doğal sırasını** kendi içinde tanımlar — tek metodu `compareTo(T o)`'dur ve sınıfa gömülüdür (ör. `Integer`, `String`). Bir sınıf yalnız **bir** doğal sıraya sahip olabilir. **`Comparator<T>`** ise sıralama ölçütünü nesnenin **dışında** tanımlar — metodu `compare(a, b)`'dir; aynı tip için **istediğin kadar** farklı `Comparator` (ada göre, yaşa göre, tersine...) yazabilirsin. Doğal sıra "tek ve varsayılan", `Comparator` ise "çoklu ve isteğe bağlı" ölçüttür.

# cmp-2 | Temel
## Soru
`a.compareTo(b)` çağrısının dönüş değeri sözleşmeye (contract) göre ne anlama gelir?
## A
`true`/`false` döner: `a` sırada önce ya da eşitse `true`, değilse `false`
🤖 `compareTo` boolean değil `int` döner; işaretiyle sıra bildirir.
## B
Negatif → `a`, `b`'den önce; `0` → sıra bakımından eşit; pozitif → `a`, `b`'den sonra gelir
🤖 Doğru: işaret sırayı belirtir — negatif/sıfır/pozitif = önce/eşit/sonra.
## C
Sözleşme gereği her zaman tam olarak `-1`, `0` veya `1` döndürmek zorundadır
🤖 Yalnız **işaret** önemlidir; -5 veya +42 de geçerlidir, tam olarak ±1 şart değil.
## D
Dönüş değeri iki nesnenin bellek adresleri farkıdır
🤖 Sözleşme mantıksal sırayı ister; bellek adresiyle ilgisi yoktur.
## Doğru
B
## Açıklama
`compareTo`/`compare` sözleşmesi **işarete** dayanır: **negatif** = `a` sırada `b`'den önce (daha küçük), **0** = sıra bakımından eşit, **pozitif** = `a` sırada `b`'den sonra (daha büyük). Önemli incelik: yalnız **işaret** anlamlıdır; metot `-1`/`0`/`1` döndürmek zorunda **değildir** (bu yüzden C yanlış), `-17` de geçerli bir "önce" değeridir. Dönüş bir `int`'tir, `boolean` değil (A yanlış) ve bellek adresiyle hiçbir ilgisi yoktur (D yanlış). Ayrıca sözleşme **anti-simetri** (`sgn(a.compareTo(b)) == -sgn(b.compareTo(a))`) ve **geçişlilik** (transitivity) gerektirir. Yaygın bir hata, taşma (overflow) riski taşıyan `return a - b;` yerine güvenli `Integer.compare(a, b)` kullanmamaktır.

# cmp-3 | Orta
## Soru
Kişileri önce **yaşa** göre artan, yaş eşitse **ada** göre alfabetik sıralamak istiyorsun. Modern ve en okunaklı yol hangisidir?
## A
İki ayrı `sort` çağrısı yapmak: önce ada göre, sonra yaşa göre — sıralamanın kararlılığına güvenerek
🤖 İşe yarar ama kararlılığa bağlı, kırılgan ve okunaksızdır; tek `Comparator` zinciri niyeti açıkça yazar.
## B
`list.sort((a, b) -> a.getYas() - b.getYas())` ile sadece yaşa göre sıralamak
🤖 Ada göre ikincil sıra eksik; ayrıca `-` taşma (overflow) riski taşır.
## C
`list.sort(Comparator.comparingInt(Kisi::getYas).thenComparing(Kisi::getAd))`
🤖 Doğru: comparingInt birincil ölçütü, thenComparing eşitlikte ikincil ölçütü tanımlar.
## D
`Comparable`'ı iki kez uygulamak: biri yaş, biri ad için
🤖 Bir sınıf `Comparable`'ı yalnız **bir kez** (tek doğal sıra) uygulayabilir; iki kez mümkün değil.
## Doğru
C
## Açıklama
Çok ölçütlü sıralamanın modern yolu `Comparator` kombinatörleridir: **`Comparator.comparing(...)`** (veya ilkel için `comparingInt`/`comparingLong`/`comparingDouble`) birincil ölçütü, **`.thenComparing(...)`** birincil ölçüt **eşit** olduğunda başvurulacak ikincil ölçütü tanımlar. `comparingInt(Kisi::getYas).thenComparing(Kisi::getAd)` tam olarak "önce yaş, eşitse ad" der — okunaklı ve doğru. B ikincil ölçütü atlar ve `a - b` taşma riski taşır. A, sıralama **kararlılığına** dayanan kırılgan bir hiledir. D imkânsızdır: bir sınıfın yalnız **tek** doğal sırası olabilir; alternatif ölçütler `Comparator` ile verilir.

# cmp-4 | Orta
## Soru
Var olan bir `Comparator<Kisi> adaGore` sıralamasının **tersini** almak istiyorsun. En temiz yol nedir?
## A
`Kisi` sınıfında `Comparable`'ı ters yönde uygulayıp `adaGore`'yi kullanmayı bırakmak
🤖 Doğal sıra tektir ve onu ters yazmak tüm kodu etkiler; soru ise dıştaki `adaGore`'nin tersini istiyor.
## B
`compare` gövdesinde her `return`'ün işaretini elle ters çevirmek için Comparator'ı baştan yazmak
🤖 İşe yarar ama gereksiz; `reversed()` aynı işi tek çağrıda, hatasız yapar.
## C
Listeyi normal sıralayıp sonra `Collections.reverse(list)` çağırmak
🤖 Yalnız tam ters çevirir; çok ölçütlü sıralamada ikincil ölçütlerle tutarsız sonuç verebilir ve niyeti gizler.
## D
`adaGore.reversed()`
🤖 Doğru: `reversed()` mevcut karşılaştırıcının sırasını tersine çeviren yeni bir Comparator döner.
## Doğru
D
## Açıklama
`Comparator.reversed()`, üzerinde çağrıldığı karşılaştırıcının verdiği sıranın **tam tersini** üreten yeni bir `Comparator` döndürür — `adaGore.reversed()` en temiz ve niyeti açık yoldur. Zincirle de birlikte çalışır: `Comparator.comparing(Kisi::getYas).reversed()` yaşı azalan yapar; yalnız **ikincil** ölçütü tersleyip birincisini korumak istersen `Comparator.comparing(Kisi::getAd).thenComparing(Comparator.comparing(Kisi::getYas).reversed())` yazılır. Dikkat: `.reversed()` kendinden **önceki tüm zinciri** ters çevirir — bu yüzden `comparing(...).thenComparing(...).reversed()` ile yalnız tek bir ölçütü terslemek aynı şey değildir. `Collections.reverse` (C) yalnız son listeyi çevirir ve çok ölçütlü niyeti bozabilir; Comparator'ı elle yeniden yazmak (B) gereksiz ve hataya açıktır.

# cmp-5 | Orta
## Soru
Bir sınıfın `compareTo`'su `equals` ile **tutarsız** (ör. `compareTo` yalnız `id`'ye, `equals` `id`+`ad`'a bakıyor). Bu nesneler `TreeSet`'te kullanılınca ne olur?
## A
Derleme hatası oluşur; derleyici `compareTo` ile `equals`'in uyumunu zorunlu tutar
🤖 Derleyici bu tutarlılığı zorlamaz; sorun çalışma zamanında sessizce davranışsal çıkar.
## B
`TreeSet` eşitliği `compareTo`(==0) ile ölçtüğünden `equals`'e göre farklı iki nesneyi aynı sayıp ikincisini eklemez
🤖 Doğru: TreeSet equals değil compareTo kullanır; tutarsızlık beklenmedik "yutma"ya yol açar.
## C
`TreeSet` `compareTo`==0 durumunda otomatik olarak `equals`'e başvurur ve tutarlı davranır
🤖 `TreeSet`/`TreeMap` eşitliği **her zaman** `compareTo`/`compare` ile ölçer; `equals`'e geçmez.
## D
Hiçbir fark olmaz; sıralı koleksiyonlar da eşitlik için `equals` kullanır
🤖 Sıralı koleksiyonlar `equals` değil `compareTo` kullanır; bu tam da sorunun kaynağıdır.
## Doğru
B
## Açıklama
**`TreeSet` ve `TreeMap` eşitliği `equals` ile değil, `compareTo`/`compare`'in `0` dönmesiyle** belirler. `compareTo` yalnız `id`'ye bakıyorsa, `id`'si aynı ama `ad`'ı farklı (yani `equals`'e göre **farklı**) iki nesne `compareTo`==0 verir; `TreeSet` bunları **aynı** sayar ve ikincisini **eklemez** (veya `TreeMap` var olan anahtarı ezer). Bu, `Set`/`Map`'in `equals` temelli sözleşmesiyle çelişiyormuş gibi görünen sinsi bir hatadır. Bu yüzden `Comparable`/`Comparator` sözleşmesi "doğal sıranın `equals` ile **tutarlı** olması **kuvvetle önerilir**" der: ideal olarak `compareTo(b) == 0` ⟺ `equals(b)`. Derleyici bunu zorlamaz (A yanlış); dikkat programcıya aittir.

# cmp-6 | İleri
## Soru
Java'nın koleksiyon sıralaması (`Collections.sort`, `List.sort`, `Arrays.sort(nesne dizisi)`) **kararlıdır** (stable). Bu ne anlama gelir ve neden önemlidir?
## A
Karşılaştırıcıya göre eşit (compare==0) elemanların girişteki göreli sırası sıralama sonrasında korunur
🤖 Doğru: kararlılık eşitleri yerinde tutar; ardışık sıralamalarda önceki düzen korunur.
## B
Sıralama girdi ne olursa olsun hep aynı süreyi alır; en iyi ve en kötü durum aynıdır
🤖 Kararlılık zaman karmaşıklığı değil, eşit elemanların sıra korunumuyla ilgilidir.
## C
Sıralanan liste bir daha asla değiştirilemez; `sort` çağrısı listeyi immutable yapar
🤖 Kararlılık değişmezlikle ilgili değildir; liste sonrasında değiştirilebilir.
## D
Aynı listeyi iki kez sıralamak çökme (crash) olmadan çalışır
🤖 Bu her sıralama için doğrudur; kararlılığın tanımı değildir.
## Doğru
A
## Açıklama
Bir sıralama **kararlıysa** (stable), karşılaştırıcıya göre **eşit** kabul edilen (`compare`==0) iki eleman, sıralama sonrasında **girişteki göreli sıralarını korur**. Java nesne sıralamaları (TimSort) kararlıdır. Önemi: **çok geçişli** (multi-pass) sıralamayı mümkün kılar — önce ada göre sıralayıp sonra yaşa göre sıralarsan, aynı yaştaki kişiler **ada göre sıralı** kalır (ikinci sıralama eşitleri bozmaz). Bu, `thenComparing` zincirinin de temelidir. Kararlılık; zaman karmaşıklığı (B), değişmezlik (C) veya çökmezlik (D) hakkında bir şey söylemez — yalnız **eşit elemanların göreli düzeninin korunması** demektir. (Not: `Arrays.sort` ilkel dizilerde quicksort kullanır ve kararlılık ilkellerde zaten gözlemlenemez.)
