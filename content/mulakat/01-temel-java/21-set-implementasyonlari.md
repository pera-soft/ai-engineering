---
slug: set-implementasyonlari
baslik: "Set Uygulamaları: HashSet, LinkedHashSet, TreeSet"
kademe: Temel
sira: 21
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir düğün davet listesi tutuyorsun ve aynı kişinin iki kez yazılmasını istemiyorsun. Her yeni isimde "bu zaten var mı?" diye bakman gerekir; listeyi baştan sona taramak kalabalıkta yavaştır. `Set` (küme) tam bunu çözer: **aynı elemanı iki kez tutmaz** ve "var mı?" sorusunu taramadan, çok hızlı yanıtlar.

Üç ana `Set` uygulaması aynı sözü verir — tekrar yok — ama farklı bir **sıra** vaadiyle gelir: biri hiç sıra sözü vermez ve en hızlısıdır, biri eklediğin sırayı hatırlar, biri elemanları küçükten büyüğe sıralı tutar.

### Temel kavramlar

- **Küme (Set)**: tekrar (duplicate) kabul etmeyen koleksiyon.
- **`hashCode()`**: bir nesneden üretilen tamsayı "parmak izi". Aynı nesne hep aynı sayıyı verir.
- **Kova (bucket)**: hash'e göre elemanların dağıtıldığı gözler. Doğru gözü hesaplayıp yalnız onun içine bakarsın; tüm koleksiyonu taramaktan kurtulursun.
- **O(1)**: eleman sayısı artsa da süre değişmez. **O(log n)**: eleman sayısı iki katına çıkınca süreye yalnız bir adım eklenir — 1 milyon elemanda ~20 adım. Hâlâ çok hızlı, ama O(1) kadar değil.
- **Doğal sıra (`Comparable`)**: sınıfın kendi içinde tanımlı sıralama. **`Comparator`**: dışarıdan verilen alternatif sıralama ölçütü.
- **Kırmızı-siyah ağaç (red-black tree)**: kendini dengede tutan ikili arama ağacı; sıralı gezinmeyi ve O(log n) aramayı sağlar.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç Set uygulamasının sıralama garantisi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sıralama garantisi ile üç Set</text>
  <rect x="24" y="44" width="188" height="128" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">HashSet</text>
  <text x="118" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">sıra YOK · O(1)</text>
  <text x="118" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hash kovaları (bucket)</text>
  <text x="118" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hashCode/equals'a bağlı</text>
  <text x="118" y="156" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">en hızlı, sırasız</text>
  <rect x="226" y="44" width="188" height="128" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">LinkedHashSet</text>
  <text x="320" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">ekleme sırası · O(1)</text>
  <text x="320" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hash + bağlı liste</text>
  <text x="320" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">öngörülebilir iterasyon</text>
  <text x="320" y="156" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hız + sıra dengesi</text>
  <rect x="428" y="44" width="188" height="128" rx="12" fill="#FBEFE4" stroke="#8A4B1E" stroke-width="1.5"/>
  <text x="522" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#8A4B1E" font-family="system-ui">TreeSet</text>
  <text x="522" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">sıralı · O(log n)</text>
  <text x="522" y="114" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">kırmızı-siyah ağaç</text>
  <text x="522" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Comparable/Comparator</text>
  <text x="522" y="156" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">aralık sorguları (Navigable)</text>
</svg>
```

### Nasıl çalışır?

**`HashSet`** arkada bir `HashMap` tutar. `add(x)` üç adımdır: (1) `x.hashCode()`'dan bir **kova** indeksi hesapla, (2) o kovaya git, (3) kovadaki elemanlarla **`equals`** karşılaştır — eşit bulunursa eleman **eklenmez**. Ortalama ekleme/arama **O(1)**. Hiçbir sıra garantisi yoktur; iterasyon sırası hash dağılımını izler ve JVM sürümüne, kapasiteye, eklenen elemanlara göre değişebilir. Doğruluğu tümüyle elemanın **`hashCode`/`equals` sözleşmesine** bağlıdır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HashSet tekilleştirme: hashCode ile kova, equals ile eşitlik">
  <defs>
    <marker id="hashset-ok" markerWidth="9" markerHeight="9" refX="6" refY="3" orient="auto">
      <path d="M0,0 L6,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">HashSet nasıl tekilleştirir?</text>
  <rect x="24" y="50" width="120" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="71" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">yeni eleman</text>
  <text x="84" y="87" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">add(x)</text>
  <line x1="144" y1="73" x2="196" y2="73" stroke="#5D7789" stroke-width="1.5" marker-end="url(#hashset-ok)"/>
  <rect x="200" y="50" width="150" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="275" y="71" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">hashCode() → kova</text>
  <text x="275" y="87" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hangi bucket?</text>
  <line x1="350" y1="73" x2="402" y2="73" stroke="#5D7789" stroke-width="1.5" marker-end="url(#hashset-ok)"/>
  <rect x="406" y="50" width="210" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="511" y="71" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">kova içinde equals()</text>
  <text x="511" y="87" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eşit bulunursa eklenmez</text>
  <rect x="24" y="118" width="592" height="60" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="141" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Tuzak: eşit nesneler farklı hashCode üretirse farklı kovalara düşer</text>
  <text x="320" y="161" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">→ equals hiç çalışmaz, ikisi de kalır = mükerrer. equals ve hashCode birlikte tutarlı olmalı.</text>
</svg>
```

**`LinkedHashSet`** aynı hash tablosunun üzerine, elemanları **eklenme sırasında** birbirine bağlayan çift bağlı bir liste örer. Hash hızı (**O(1)**) korunur, iterasyon **öngörülebilir** olur.

**`TreeSet`** hash kullanmaz; elemanları bir **kırmızı-siyah ağaçta** sıralı tutar. Ekleme/arama, ağaçta karşılaştıra karşılaştıra aşağı inmektir → **O(log n)**. Eşitliği `equals`/`hashCode` ile değil, **karşılaştırma sonucuyla** ölçer. Sıralama ölçütü ya elemanın doğal sırasından (`Comparable`) ya da yapıcıya verilen `Comparator`'dan gelir.

### Mülakatta ne sorulur?

- **Karşılaştırma yolu yoksa ne olur?** `TreeSet<Kisi>` derlenir ama `Kisi` `Comparable` uygulamıyorsa ve `Comparator` da verilmemişse **ilk `add`'de `ClassCastException`** fırlar. Neden ilk `add`? Çünkü arkadaki `TreeMap`, boş ağaca ilk girişi koyarken bilerek bir tip denetimi yapar (`compare(anahtar, anahtar)`); hata ikinci elemana ertelenmez. Çözüm: `implements Comparable` ya da `new TreeSet<>(comparator)`.
- **`null` kuralları**: doğal sıralı `TreeSet` `null` elemanı karşılaştıramaz → **`NullPointerException`**. `HashSet`/`LinkedHashSet` **tek bir `null`** elemana izin verir.
- **`Comparator` sırayı tümüyle belirler, ekleme sırası önemsizdir.** `new TreeSet<>(Comparator.reverseOrder())` içine `elma, armut, kiraz` eklersen çıktı `[kiraz, elma, armut]` olur (azalan alfabetik).
- **Bozuk `hashCode`/`equals` tuzağı**: mantıken eşit iki nesne **farklı** `hashCode` üretiyorsa **farklı kovalara** düşer; `equals` hiç çalıştırılmaz ve ikisi de kümede kalır → **mükerrer** görünür. Derleyici bu tutarlılığı zorlamaz, sorun çalışma zamanında sessizce çıkar; `HashSet` kendiliğinden `TreeSet`'e de dönüşmez.
- **`NavigableSet` navigasyonu** (`TreeSet` bunu uygular); aşağıdaki işlemlerin hepsi **O(log n)**'dir. `ceiling(x)` = x'e eşit ya da x'ten **büyük en küçük** eleman. `floor(x)` = x'e eşit ya da x'ten **küçük en büyük** eleman. `higher(x)`/`lower(x)` = kesin büyük/küçük komşu. `first()`/`last()` = uçlar; `headSet`/`tailSet`/`subSet` = aralık görünümleri. Aranan yoksa `null` döner. Aynı işi elemanları listeye alıp `for` ile aramak **O(n)**'dir ve ağacı kullanmanın amacını boşa çıkarır.
- **Seçim kuralı**: sıra umurunda değil ve hız istiyorsan `HashSet`; **ekleme sırası** gerekiyorsa `LinkedHashSet`; **sıralı** tutmak veya **aralık/komşu** sorgusu yapmak gerekiyorsa `TreeSet`. `ArrayList` bir `Set` değildir — tekrarı engellemez.

### Tek cümlede

`HashSet` hash kovalarıyla sırasız ama O(1), `LinkedHashSet` aynı hızda ekleme sırasını korur, `TreeSet` ise kırmızı-siyah ağaçla O(log n) karşılığında sıralı erişim ve aralık sorguları verir.

# set-1 | Temel
## Soru
`HashSet` iterasyon (dolaşma) sırası hakkında ne söylenebilir?
## A
Elemanları her zaman doğal sırasına (küçükten büyüğe) göre döner
🤖 Bu `TreeSet` davranışıdır; `HashSet` sıralı değildir.
## B
Elemanları eklenme sırasına göre döner
🤖 Bu `LinkedHashSet` davranışıdır; `HashSet` ekleme sırasını korumaz.
## C
Belirli bir sıra garantisi yoktur; iterasyon sırası öngörülemez
🤖 Doğru: `HashSet` hash dağılımına dayanır, hiçbir sıra sözü vermez.
## D
Elemanları eklenme sırasının tersine döner
🤖 Ters sıra da bir garantidir; `HashSet` hiçbir sıra garantisi vermez.
## Doğru
C
## Açıklama
`HashSet` elemanları hash değerlerine göre **kovalara** (bucket) dağıtır; iterasyon bu iç düzeni izler ve **hiçbir sıra garantisi** vermez — sıra JVM sürümüne, kapasiteye ve eklenen elemanlara göre değişebilir. Sıralı çıktı istiyorsan doğal sıra için `TreeSet`, ekleme sırası için `LinkedHashSet` kullanılır. `HashSet`'in iterasyon sırasına bel bağlayan kod kırılgandır.

# set-2 | Temel
## Soru
Elemanların **eklendiği sırayla** dolaşılmasını (predictable iteration) garanti eden, aynı zamanda hash tabanlı O(1) hızı koruyan `Set` uygulaması hangisidir?
## A
`HashSet`
🤖 `HashSet` hızlıdır ama ekleme sırasını korumaz.
## B
`LinkedHashSet`
🤖 Doğru: hash tablosuna örülmüş bağlı liste ekleme sırasını korur, hız O(1) kalır.
## C
`TreeSet`
🤖 `TreeSet` ekleme sırasını değil, sıralı (sorted) sırayı korur ve O(log n)'dir.
## D
`ArrayList`
🤖 `ArrayList` bir `Set` değildir; tekrarları engellemez.
## Doğru
B
## Açıklama
`LinkedHashSet`, `HashSet`'in üzerine elemanları **ekleme sırasında** birbirine bağlayan çift bağlı bir liste ekler. Böylece hash tablosunun **O(1)** ekleme/arama hızı korunurken iterasyon **öngörülebilir** (ekleme sırası) olur. `TreeSet` de öngörülebilir bir sıra verir ama bu **doğal/sıralı** sıradır (ekleme sırası değil) ve maliyeti O(log n)'dir. Tekrarları engellemeyen `ArrayList` bir `Set` değildir.

# set-3 | Orta
## Soru
`TreeSet<Kisi>` oluşturuyorsun ama `Kisi` sınıfı `Comparable` uygulamıyor ve yapıcıya (constructor) bir `Comparator` da vermedin. İlk `add` çağrısında ne olur?
## A
Derleme hatası verir
🤖 Kod derlenir: `TreeSet<Kisi>` tip düzeyinde geçerlidir, sorun çalışma zamanında ortaya çıkar.
## B
Sessizce çalışır ama elemanlar rastgele sıralanır
🤖 `TreeSet` sıralamadan çalışamaz; sessizce rastgele sıralamaz, istisna fırlatır.
## C
`ClassCastException` fırlatır çünkü elemanları karşılaştıracak bir yol yoktur
🤖 Doğru: `TreeSet` elemanı `Comparable`'a cast etmeye çalışır, sınıf uygulamadığı için cast başarısız olur.
## D
`null` döner ve eleman eklenmez
🤖 `add` istisna fırlatır; sessizce null dönmez.
## Doğru
C
## Açıklama
`TreeSet` elemanları **sıralı** tutmak zorundadır; sıralama için ya elemanın `Comparable` uygulaması (doğal sıra) ya da yapıcıya verilen bir `Comparator` gerekir. İkisi de yoksa, `TreeSet` eklenen elemanı `Comparable`'a **cast** etmeye çalışır; `Kisi` bu arayüzü uygulamadığından çalışma zamanında **`ClassCastException`** fırlar. Önemli detay: istisna **daha ilk `add`'de** çıkar — çünkü arkadaki `TreeMap`, boş ağaca ilk girişi koyarken bilerek bir **tip denetimi** yapar (`compare(anahtar, anahtar)`), böylece hata ikinci elemana kadar ertelenmez. Çözüm: `Kisi implements Comparable` yap (doğal sıra) veya `new TreeSet<>(comparator)` kullan.

# set-4 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
Set<String> s = new TreeSet<>(Comparator.reverseOrder());
s.add("elma");
s.add("armut");
s.add("kiraz");
System.out.println(s);
```
## A
`[armut, elma, kiraz]`
🤖 Bu doğal (artan) alfabetik sıradır; verilen `Comparator` tersini ister.
## B
`[armut, kiraz, elma]`
🤖 Böyle bir sıra ne artan ne azalan alfabetiktir; tutarlı bir kurala uymaz.
## C
`[elma, armut, kiraz]`
🤖 Bu ekleme sırasıdır; `TreeSet` ekleme sırasını değil karşılaştırıcının sırasını uygular.
## D
`[kiraz, elma, armut]`
🤖 Doğru: `reverseOrder()` azalan alfabetik sıra verir → kiraz, elma, armut.
## Doğru
D
## Açıklama
`TreeSet`, verilen **`Comparator`** ne diyorsa ona göre sıralar; ekleme sırası önemsizdir. `Comparator.reverseOrder()` doğal sıranın tersini uygular; String'ler için doğal sıra artan alfabetiktir, tersi **azalandır** → `kiraz, elma, armut`. Bu, `TreeSet`'in gücüdür: bir `Comparator` vererek sıralama ölçütünü tümüyle sen belirlersin (ör. uzunluğa, alana, tersine göre).

# set-5 | Orta
## Soru
Bir `Kisi` sınıfının `equals`/`hashCode`'u bozuk (mantıken eşit iki nesne farklı `hashCode` üretiyor). Bu nesneler `HashSet`'e konunca en olası sonuç nedir?
## A
Hiçbir sorun olmaz; `HashSet` her zaman doğru tekilleştirme yapar
🤖 `HashSet` doğruluğu tam da `hashCode`/`equals` sözleşmesine dayanır; bozuksa tekilleştirme bozulur.
## B
`HashSet` otomatik olarak `TreeSet`'e döner
🤖 Böyle bir dönüşüm yoktur; `HashSet` iç yapısını değiştirmez.
## C
Derleme hatası oluşur
🤖 Derleyici `hashCode`/`equals` tutarlılığını zorlamaz; sorun çalışma zamanında sessizce görünür.
## D
Mantıken "aynı" olan iki nesne farklı kovalara düşüp mükerrer (duplicate) görünür
🤖 Doğru: farklı hashCode farklı kova demek; `Set` onları farklı eleman sanır, tekilleştirme kaçar.
## Doğru
D
## Açıklama
`HashSet` bir elemanı önce **`hashCode`** ile bir **kovaya** yerleştirir, sonra o kova içinde **`equals`** ile eşitlik arar. Mantıken eşit iki nesne **farklı `hashCode`** üretiyorsa **farklı kovalara** düşer; `Set` onları hiç karşılaştırmadan iki ayrı eleman olarak tutar → **mükerrer** görünür ve tekilleştirme sözü tutulmaz. Bu yüzden bir sınıfı hash tabanlı koleksiyonlarda kullanacaksan `equals` ve `hashCode`'u **birlikte, tutarlı** uygulaman şarttır (eşit nesneler eşit hash üretmeli).

# set-6 | İleri
## Soru
Sıralı bir `NavigableSet<Integer>` (ör. `TreeSet`) üzerinde "verilen değerden **büyük-eşit en küçük** elemanı" almak istiyorsun. Hangi metot bunu O(log n)'de yapar?
## A
`ceiling(x)`
🤖 Doğru: `ceiling(x)`, x'e eşit ya da x'ten büyük en küçük elemanı döner (yoksa null).
## B
`floor(x)`
🤖 `floor(x)`, x'e eşit ya da x'ten küçük en büyük elemanı döner — istenenin tersi yön.
## C
`contains(x)`
🤖 `contains` yalnız varlık/yokluk (boolean) döner; komşu eleman vermez.
## D
Elemanları bir listeye alıp `for` ile aramak
🤖 Bu O(n) doğrusal taramadır; ayrıca ağacın O(log n) navigasyon gücünü boşa harcar.
## Doğru
A
## Açıklama
`NavigableSet` (ki `TreeSet` bunu uygular) sıralı yapının üzerine **komşuluk/aralık** sorguları ekler: `ceiling(x)` = x'e eşit veya büyük **en küçük** eleman; `floor(x)` = x'e eşit veya küçük **en büyük** eleman; ayrıca `higher`/`lower` (kesin büyük/küçük), `first`/`last`, `headSet`/`tailSet`/`subSet`. Bunların hepsi ağaç navigasyonuyla **O(log n)**'dir. Elle doğrusal tarama (D) hem yavaştır (O(n)) hem de `TreeSet`'i kullanmanın asıl sebebini boşa çıkarır. Bu tür "en yakın büyük/küçük" ve "aralık" ihtiyaçları `TreeSet`'i `HashSet`/`LinkedHashSet`'e tercih etmenin başlıca gerekçesidir.
