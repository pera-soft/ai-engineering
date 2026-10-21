---
slug: bigdecimal-para-hesabi
baslik: "Para Hesabı ve BigDecimal: Kayan Nokta Tuzağı, Ölçek, Yuvarlama ve Karşılaştırma"
kademe: Orta
sira: 226
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

1000 TL'lik bir krediyi üç eşit taksite bölün: 333,33 + 333,33 + 333,33 = 999,99. Bir kuruş ortada kaldı. Bu kuruş kimin? Pastayı üçe bölerken kimse kırıntıyı dert etmez, ama bankada o kırıntı gün sonu mutabakat dosyasında **fark** olarak görünür ve dosya kapanmaz. Para hesabı, "yaklaşık doğru" diye bir şeyin olmadığı tek alandır: 999,99 ile 1000,00 arasındaki fark matematiksel olarak ihmal edilebilir ama muhasebe olarak **hatadır**. Bu yüzden Java'da parayı `double` ile değil `BigDecimal` ile tutarız; kuruş farkını da tesadüfe bırakmaz, açıkça bir yere yazarız.

### Temel kavramlar

- **İkili kayan nokta (binary floating point)**: `double`/`float`'ın kullandığı IEEE 754 gösterimi. Taban 2 olduğu için `0.1`, `0.2`, `0.7` gibi ondalık değerlerin **tam** karşılığı yoktur; yaklaşık saklanır.
- **BigDecimal**: Sayıyı `ölçeksiz değer × 10^(-ölçek)` biçiminde, **ondalık tabanda** ve keyfi büyüklükte tutan sınıf. Kayıp, sen izin vermedikçe olmaz.
- **Değişmezlik (immutable)**: `BigDecimal` nesnesi kurulduktan sonra **asla değişmez**. `add`, `subtract`, `multiply`, `setScale` nesneyi güncellemez; **yeni bir nesne döndürür**.
- **Ölçek (scale)**: Ondalık nokta sonrasındaki hane sayısı. `12345.67` için ölçek `2`.
- **Kesinlik (precision)**: Toplam anlamlı hane sayısı. `12345.67` için kesinlik `7`.
- **Yuvarlama kipi (RoundingMode)**: Atılan hanenin sonucu nasıl etkileyeceğinin kuralı. En çok kullanılan ikisi `HALF_UP` ve `HALF_EVEN`'dır.
- **Bankacı yuvarlaması (banker's rounding)**: `HALF_EVEN`. Tam yarımda her seferinde yukarı değil, **en yakın çift haneye** yuvarlar.
- **Düz gösterim (plain string)**: `toPlainString()` — sayıyı üstel/bilimsel gösterime kaçmadan olduğu gibi yazar.
- **Para nesnesi (Money)**: Tutar ile para birimini (currency) birlikte taşıyan tip; "1000" tek başına bir para değildir, "1000 TRY" paradır.

`0.1 + 0.2` işleminin `double` ile `0.30000000000000004` verdiğini ilkel tipler konusundan biliyorsun. Buradaki asıl mesele o tek işlem değil, **sapmanın birikmesidir**: gün sonunda milyonlarca faiz/tahakkuk satırı toplanınca minik sapmalar üst üste gelir ve mutabakat farkı gözle görülür hale gelir.

```svg
<svg viewBox="0 0 660 225" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Parada double kullanmak sapma biriktirir, BigDecimal ondalık tabanda tam tutar">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Parada double YASAK — BigDecimal ZORUNLU</text>
  <rect x="22" y="42" width="300" height="152" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">double faiz = 0.1;</text>
  <text x="172" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0.1 + 0.2 → 0.30000000000000004</text>
  <text x="172" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her işlemde minik sapma</text>
  <text x="172" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">milyonlarca satırda sapma BİRİKİR</text>
  <text x="172" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">gün sonu mutabakatı TUTMAZ</text>
  <text x="172" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ikili (binary) taban 0.1'i tam tutamaz</text>
  <rect x="338" y="42" width="300" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="488" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">new BigDecimal("0.10")</text>
  <text x="488" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ondalık taban + ölçek (scale)</text>
  <text x="488" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0.10 + 0.20 = 0.30 TAM</text>
  <text x="488" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yuvarlamayı SEN söylersin</text>
  <text x="488" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sapma yok, mutabakat tutar</text>
  <text x="488" y="180" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bedeli: daha yavaş, daha çok bellek</text>
  <text x="330" y="214" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kural: tutar, faiz, oran ve bakiye toplamı → asla double/float</text>
</svg>
```

### Nasıl çalışır?

**1) BigDecimal değişmezdir — dönüş değerini atamazsan hiçbir şey olmaz.** Bu, mülakatın en sevilen tuzağıdır. `bakiye.add(new BigDecimal("50.00"));` satırı derlenir, çalışır, uyarı vermez ve **bakiyeyi değiştirmez**; toplam yeni bir nesnede döner ve o nesne kaybolur. Doğrusu `bakiye = bakiye.add(...)` ya da sonucu yeni bir değişkene atamaktır. Aynı kural `setScale`, `multiply`, `negate` için de geçerlidir. `String` gibi düşün: `String` de değişmezdir ve `metin.trim();` tek başına hiçbir işe yaramaz.

**2) Hangi yapıcı (constructor)?** Üç yol vardır ve üçü aynı şeyi yapmaz:

- `new BigDecimal(0.1)` — `double` yapıcısı. `double`'ın **bozuk** ikili değerini olduğu gibi alır ve `0.1000000000000000055511151231257827021181583404541015625` gibi devasa bir sayı üretir. Parada kullanma.
- `BigDecimal.valueOf(0.1)` — `double`'ı önce metne çevirip (`Double.toString`) oradan kurar; sonuç `0.1`, ölçek `1`. Elinde zaten bir `double` varsa doğru seçim budur; ama o `double` bir hesaplamadan geldiyse **kaybı zaten yapmıştır**, bu yöntem kaybı geri getirmez.
- `new BigDecimal("0.1")` — metinden okur, `double`'a hiç uğramaz. Kod içindeki sabitler, yapılandırma dosyasından gelen oranlar ve metin olarak okunan tutarlar için **doğru** yol budur.

```svg
<svg viewBox="0 0 660 252" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="BigDecimal yapıcı farkları ile ölçek ve kesinlik tanımı">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı "0.1" — üç yapıcı, üç farklı değer</text>
  <rect x="20" y="40" width="200" height="124" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="120" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">new BigDecimal(0.1)</text>
  <text x="120" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">double'ı olduğu gibi alır</text>
  <text x="120" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0.100000000000000005…</text>
  <text x="120" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ölçek: 50'den büyük</text>
  <text x="120" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">PARADA KULLANMA</text>
  <rect x="230" y="40" width="200" height="124" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">BigDecimal.valueOf(0.1)</text>
  <text x="330" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">önce double → metin</text>
  <text x="330" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0.1  (ölçek: 1)</text>
  <text x="330" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">double bozuksa kurtarmaz</text>
  <text x="330" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">elinde double varsa</text>
  <rect x="440" y="40" width="200" height="124" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="540" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">new BigDecimal("0.1")</text>
  <text x="540" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">metinden okur</text>
  <text x="540" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0.1  (ölçek: 1)</text>
  <text x="540" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">double'a hiç uğramaz</text>
  <text x="540" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">SABİTLER İÇİN DOĞRU</text>
  <text x="330" y="192" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ölçek (scale) ve kesinlik (precision)</text>
  <text x="330" y="214" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">12345.67  →  kesinlik = 7 anlamlı hane,  ölçek = 2 ondalık hane</text>
  <text x="330" y="238" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">setScale(2, RoundingMode.HALF_UP) ölçeği sabitler; divide ölçek + yuvarlama ister</text>
</svg>
```

**3) Yuvarlama: HALF_UP mu HALF_EVEN mi?** `setScale(2, RoundingMode.HALF_UP)` tam yarımı (`2.345`) **her zaman yukarı** yuvarlar; insanın okulda öğrendiği kural budur ve müşteriye gösterilen tek bir tutarda beklenen davranıştır. `HALF_EVEN` (bankacı yuvarlaması) ise tam yarımı **en yakın çift** haneye çeker: `2.345 → 2.34`, `2.355 → 2.36`. Fark tek bir işlemde önemsizdir; **çok sayıda tutarı toplarken** önemlidir. `HALF_UP` yarımların hepsini aynı yöne ittiği için toplamda sistematik bir **yukarı sapma (bias)** üretir; `HALF_EVEN` yarımları yaklaşık yarı yarıya iki yöne dağıttığı için bu sapmayı büyük ölçüde söndürür. Bu yüzden faiz/tahakkuk gibi toplu hesaplarda `HALF_EVEN` tercih edilir. Dikkat: `HALF_EVEN` sapmayı **azaltır**, "toplam her zaman tutar" diye bir garanti **vermez**.

**4) Bölme (divide) yuvarlama olmadan patlar.** `new BigDecimal("1000.00").divide(new BigDecimal("3"))` sessizce yuvarlamaz; `ArithmeticException: Non-terminating decimal expansion` fırlatır. Çünkü sonuç sonsuz ondalık açılıma sahiptir ve `BigDecimal` senin adına karar vermeyi reddeder. Doğrusu ölçeği ve kipi vermektir: `divide(bolen, 2, RoundingMode.HALF_EVEN)`. Bir `MathContext` verirsen kesinlik tabanlı yuvarlama da yapılabilir, ama bu **varsayılan davranış değildir**. Toplama, çıkarma ve çarpmada bu sorun yoktur (sonuç ölçekleri belirlidir), yalnız bölme risklidir.

**5) `equals` ile `compareTo` aynı şey değildir.** `new BigDecimal("2.0").equals(new BigDecimal("2.00"))` sonucu **`false`**'tur; çünkü `equals` yalnız sayısal değeri değil **ölçeği de** karşılaştırır. `compareTo` ise yalnız sayısal değere bakar ve `0` (eşit) döndürür. `hashCode` da `equals` ile tutarlıdır, yani `2.0` ile `2.00` **farklı özet değerine** sahiptir. Sonuç: `BigDecimal`'i `HashSet` üyesi veya `HashMap` anahtarı yaparsan aynı parayı iki farklı kayıt sanırsın. Aynı tuzağın en sık görülen biçimi sıfır kontrolüdür: `tutar.equals(BigDecimal.ZERO)` ifadesi `0.00` için `false` verir; doğrusu `tutar.compareTo(BigDecimal.ZERO) == 0`'dır.

```svg
<svg viewBox="0 0 660 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="BigDecimal equals ölçeği de karşılaştırır, compareTo yalnız sayısal değere bakar">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">2.0 ile 2.00 eşit mi? Sorduğun metoda bağlı</text>
  <rect x="140" y="40" width="170" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="225" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">new BigDecimal("2.0")</text>
  <text x="225" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ölçek = 1</text>
  <rect x="355" y="40" width="170" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="440" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">new BigDecimal("2.00")</text>
  <text x="440" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ölçek = 2</text>
  <path d="M225 84 L225 96" stroke="#5D7789" stroke-width="1.4" fill="none"/>
  <path d="M440 84 L440 96" stroke="#5D7789" stroke-width="1.4" fill="none"/>
  <path d="M175 96 L490 96" stroke="#5D7789" stroke-width="1.4" fill="none"/>
  <path d="M175 96 L175 110" stroke="#5D7789" stroke-width="1.4" fill="none" marker-end="url(#paradec-ok)"/>
  <path d="M490 96 L490 110" stroke="#5D7789" stroke-width="1.4" fill="none" marker-end="url(#paradec-ok)"/>
  <rect x="40" y="116" width="270" height="76" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="175" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">equals → false</text>
  <text x="175" y="162" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">değeri VE ölçeği karşılaştırır</text>
  <text x="175" y="180" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hashCode değerleri de farklıdır</text>
  <rect x="355" y="116" width="270" height="76" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="490" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">compareTo → 0 (eşit)</text>
  <text x="490" y="162" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yalnız sayısal değere bakar</text>
  <text x="490" y="180" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tutar karşılaştırmada bunu kullan</text>
  <text x="330" y="216" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Tuzak: BigDecimal'i HashSet üyesi / HashMap anahtarı yapma</text>
  <text x="330" y="232" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">2.0 ile 2.00 farklı anahtardır; tutar.equals(BigDecimal.ZERO) de 0.00 için false verir</text>
  <defs>
    <marker id="paradec-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**6) Yazdırma tuzağı: `stripTrailingZeros` ve bilimsel gösterim.** `new BigDecimal("600.00").stripTrailingZeros()` sondaki sıfırları atarken ölçeği negatife düşürür ve `toString()` çıktısı `6E+2` olur. Ekranda ya da mutabakat dosyasında "6E+2 TL" görmek istemiyorsan **`toPlainString()`** kullan. Kural: `toString()` gösterim için güvenli değildir, `toPlainString()` güvenlidir.

**7) Veritabanı eşlemesi.** Oracle'da `NUMBER(19,2)`, standart SQL'de `DECIMAL(19,2)` — `p` toplam hane, `s` ondalık hanedir. JPA tarafında `@Column(precision = 19, scale = 2)` yazılır; ama bu bilgi **yalnız şema üretiminde (DDL)** kullanılır. Şemayı veritabanı yöneticisinin betikleri kuruyorsa bu anotasyon çalışma anında hiçbir şey doğrulamaz. Kolonun ölçeğinden daha çok ondalık haneye sahip bir değer yazılırsa veritabanı **sessizce yuvarlar** — uygulama istisna görmez, fark ancak mutabakatta ortaya çıkar. Bu yüzden ölçeği veritabanına bırakmayıp uygulamada `setScale` ile açıkça sabitlemek gerekir. Kolonu `FLOAT`/`BINARY_DOUBLE` yapmak ise `double` hatasının veritabanı sürümüdür.

**8) JSON'da para taşımak.** Tutarı JSON'da **sayı** olarak gönderirsen, tarayıcı tarafındaki JavaScript onu 64 bit ikili kayan noktaya çevirir; JavaScript'in tek sayı tipi budur. Büyük tutarlarda böylece hassasiyet kaybolur ve sunucuda doğru olan değer ekranda kayar. Güvenli yol tutarı **dizge (string)** olarak taşımaktır: `"tutar": "1250.75"`. Sunucu tarafında Jackson'ın `WRITE_BIGDECIMAL_AS_PLAIN` ayarı da açılır; böylece değerler JSON'a `1.25075E+3` gibi bilimsel gösterimle değil düz yazılır.

**9) Alternatif: en küçük birimde `long`.** Tutarı kuruş cinsinden `long` tutmak yaygın bir yaklaşımdır: `1250.75 TL → 125075`. Artısı hızlıdır, toplama/çıkarma **tam** doğrudur, ölçek karmaşası yoktur ve bellek dostudur. Eksisi üç tanedir. Her yerde 100'e bölme/çarpma disiplinini korumak gerekir; bir yerde unutulursa 100 kat hata olur. Faiz gibi bölmeli hesaplarda yuvarlama sorunu **yine** karşına çıkar. Ayrıca tüm para birimlerinin iki ondalık hanesi olduğu varsayımı yanlıştır: Japon Yeni'nde 0, Kuveyt Dinarı'nda 3 hane vardır. Kural olarak: sadece toplama/çıkarma yapan yüksek hacimli sistemlerde `long`, faiz/oran/taksit hesabı olan iş mantığında `BigDecimal`.

**10) Taksit kuruş farkı.** 1000,00 TL üç taksite bölününce her taksit 333,33 çıkar, toplam 999,99 olur; 1 kuruş eksik kalır. Sektörün standart çözümü: taksitleri hesapla, **anapara ile taksitler toplamı arasındaki farkı bul**, farkı **son taksite** (bazı ürünlerde ilk taksite) ekle → `333,33 + 333,33 + 333,34 = 1000,00`. Bu bir yuvarlama kipi seçimiyle çözülmez, **açık bir düzeltme adımıdır** ve ödeme planı ekranında da böyle görünür.

**11) Para birimi tutardan ayrılmaz.** `BigDecimal` tek başına "1000" der, "1000 TRY" demez. Tutarı ve para birimini birlikte taşıyan bir **Money** tipi kurmak (içinde `BigDecimal` + `Currency`), farklı para birimlerini toplamaya çalışan kodu daha derleme/çalışma anında durdurur. Bunu yapmazsan, dolar hesabıyla lira hesabını toplayan bir satır kimseye çarpmadan üretime çıkar.

### Mülakatta ne sorulur / tuzaklar

- **"Neden `double` değil?"** → İkili kayan nokta ondalık kesirleri tam tutamaz, sapma **birikir**, mutabakat tutmaz. "Yuvarlarım olur biter" demek elenme sebebidir; sapma yuvarlamadan önce zaten oluşmuştur.
- **"`bakiye.add(x);` satırından sonra bakiye kaç?"** → **Değişmemiştir.** `BigDecimal` değişmezdir; dönüş değerini atamadın.
- **"`new BigDecimal(0.1)` ile `new BigDecimal("0.1")` farkı?"** → İlki `double`'ın bozuk değerini kopyalar, ikincisi tam `0.1`'dir. Sabitler için **her zaman** metin yapıcısı.
- **"`equals` mi `compareTo` mu?"** → Tutar eşitliği için **`compareTo`**. `equals` ölçeği de karşılaştırdığı için `2.0` ile `2.00`'ı farklı sayar; `HashSet`/`HashMap` anahtarı olarak `BigDecimal` kullanmak bu yüzden hatadır.
- **"`divide` neden istisna fırlattı?"** → Sonsuz ondalık açılım. Ölçek + `RoundingMode` vermek zorundasın; 34 hanelik kesinlik ancak açıkça bir `MathContext` verirsen devreye girer.
- **"HALF_UP mu HALF_EVEN mi?"** → Tekil gösterimde `HALF_UP` beklenen davranıştır; büyük hacimli toplamlarda `HALF_EVEN` sistematik yukarı sapmayı azaltır. Ama sapmayı azaltmak, "toplam her zaman anaparayı tutar" demek değildir.
- **"Taksitler toplamı anaparayı tutmuyor, ne yaparsın?"** → Kuruş farkını hesaplayıp son taksite yazarım; bunu yuvarlama kipiyle çözmeye çalışmam.
- **"API'de tutarı nasıl dönersin?"** → Dizge olarak; JSON sayısı tarayıcıda 64 bit kayan noktaya çevrilir ve hassasiyet kaybolabilir.
- **"Kuruşu `long` tutmak yeterli değil mi?"** → Toplama/çıkarma için evet, faiz/taksit gibi bölmeli hesaplarda yuvarlama sorunu geri gelir; ayrıca her para biriminin 2 ondalık hanesi yoktur.

### Tek cümlede

Para `double` ile tutulmaz; `BigDecimal` ile tutulur, **değişmez** olduğu için dönüş değeri atanır, bölmede ölçek ve yuvarlama açıkça verilir, eşitlik `compareTo` ile sorulur ve artan kuruş tesadüfe bırakılmayıp açıkça bir taksite yazılır.

# para-1 | Temel
## Soru
Kredi bakiyesini `BigDecimal bakiye = new BigDecimal("1000.00");` diye tuttun ve ardından `bakiye.add(new BigDecimal("50.00"));` satırını yazdın. Bu satırdan sonra `bakiye` değeri ne olur?
## A
1050.00 olur; `add` nesneyi yerinde günceller ve aynı referansı geri döndürür
🤖 `BigDecimal` değişmezdir, `add` mevcut nesneyi güncellemez.
## B
Derleme hatası alınır; Java `add` sonucunun kullanılmasını zorunlu kılar
🤖 dönüş değerini yok saymak derleme hatası değildir, kod sorunsuz derlenir.
## C
1000.00 kalır; `BigDecimal` değişmez (immutable) olduğu için `add` yeni bir nesne döndürür ve o nesne atanmadığı için kaybolur
🤖 Doğru: değişmez tipte dönüş değeri atanmazsa sonuç kaybolur.
## D
1000.00 kalır ama ölçek 2'den 4'e yükselir; `add` yan etki olarak iki nesnenin ölçeğini birleştirir
🤖 nesne hiç değişmediği için ölçeği de değişmez.
## Doğru
C
## Açıklama
`BigDecimal` **değişmez** (immutable) bir tiptir: kurulduktan sonra içindeki değer asla değişmez. `add`, `subtract`, `multiply`, `setScale` gibi metotlar mevcut nesneyi güncellemez, **yeni bir nesne döndürür**. Dönüş değeri bir değişkene atanmazsa sonuç sessizce kaybolur; derleyici hata da vermez (B yanlış). Doğrusu `bakiye = bakiye.add(new BigDecimal("50.00"));` yazmaktır. Nesne hiç değişmediği için ne değeri ne de ölçeği (scale) etkilenir (A ve D yanlış). Aynı mantık `String` için de geçerlidir: `metin.trim();` tek başına hiçbir işe yaramaz. Bu, para hesaplarında en sık görülen ve en sessiz kalan hatalardan biridir.

# para-2 | Orta
## Soru
Kodun içine gecikme faiz oranını sabit olarak yazacaksın. Aşağıdakilerden hangisi doğrudur?
## A
`new BigDecimal("0.1")` metinden kurulduğu için `double`'a hiç uğramaz ve değeri tam olarak `0.1` saklar
🤖 Doğru: metin yapıcısı ikili kayan noktaya uğramadan tam ondalık değeri kurar.
## B
`new BigDecimal(0.1)` en kesin seçenektir; `double` yapıcısı değeri en yüksek hassasiyetle aktarır
🤖 `double` yapıcısı ikili gösterimin bozuk değerini kopyalar, `0.1` yerine çok uzun bir sayı üretir.
## C
`new BigDecimal(0.1f)` tercih edilmelidir; `float` daha az bit kullandığı için gösterim daha temiz olur
🤖 daha az bit daha az hassasiyet demektir, `float` sorunu büyütür.
## D
`BigDecimal.valueOf(0.1)` ile `new BigDecimal(0.1)` aynı sonucu verir; ikisi de değeri tam olarak saklar
🤖 `valueOf` önce metne çevirdiği için `0.1` üretir, `double` yapıcısı bozuk ikili değeri alır.
## Doğru
A
## Açıklama
Sabit oranlar ve tutarlar için **metin yapıcısı** kullanılır: `new BigDecimal("0.1")` değeri ondalık tabanda, ölçeği `1` olacak biçimde **tam** kurar. `new BigDecimal(0.1)` ise `double`'ın IEEE 754 ikili karşılığını olduğu gibi alır ve `0.1000000000000000055511151231257827…` gibi bir sayı üretir; parada kullanılmaz (B yanlış). `float`'a geçmek hassasiyeti daha da düşürür (C yanlış). `BigDecimal.valueOf(0.1)` `double`'ı önce `Double.toString` ile metne çevirdiği için `0.1` verir; yani `double` yapıcısıyla **aynı sonucu vermez** (D yanlış). `valueOf`, elinde hazır bir `double` varken makul seçenektir; ama o `double` bir hesaplamadan geldiyse kayıp çoktan olmuştur ve `valueOf` onu geri getirmez.

# para-3 | Orta
## Soru
`new BigDecimal("1000.00").divide(new BigDecimal("3"))` çağrısı çalıştırıldığında ne olur?
## A
`333.33` döner; Java 8'den beri `divide` sonsuz açılımı algılayıp `HALF_UP` ile otomatik yuvarlar
🤖 `divide` senin adına yuvarlama kipi seçmez, böyle bir otomatik davranış yoktur.
## B
`333.3333333333333333333333333333333` döner; `divide` varsayılan olarak 34 anlamlı hanelik kesinlik kullanır
🤖 34 hanelik kesinlik ancak açıkça bir `MathContext` verilirse geçerlidir, varsayılan değildir.
## C
`333.33` döner; `divide` bölünenin ölçeğini (burada 2) koruyup sonucu ona göre keser
🤖 bölünenin ölçeği korunmaz, sonuç tam bölünemiyorsa çağrı hiç sonuç üretmez.
## D
`ArithmeticException` fırlatır; sonuç sonsuz ondalık açılıma sahiptir ve ölçek + `RoundingMode` verilmesi zorunludur
🤖 Doğru: tam bölünemeyen durumda ölçek ve yuvarlama kipi verilmezse istisna fırlar.
## Doğru
D
## Açıklama
`1000.00 / 3` sonucu `333.333…` biçiminde **sonsuz ondalık açılıma** sahiptir. `BigDecimal` bu durumda senin adına bir yuvarlama kararı vermeyi reddeder ve `ArithmeticException: Non-terminating decimal expansion` fırlatır. Doğru kullanım ölçeği ve kipi açıkça vermektir: `divide(bolen, 2, RoundingMode.HALF_EVEN)`. Otomatik `HALF_UP` yuvarlaması diye bir davranış yoktur (A yanlış); 34 anlamlı hanelik kesinlik ancak açıkça bir `MathContext` verildiğinde devreye girer (B yanlış); bölünenin ölçeğinin korunması da geçerli bir kural değildir (C yanlış). Toplama, çıkarma ve çarpmada bu sorun çıkmaz çünkü sonuç ölçekleri baştan bellidir; risk yalnız bölmededir.

# para-4 | İleri
## Soru
`new BigDecimal("2.0")` ve `new BigDecimal("2.00")` nesneleri için `equals` ve `compareTo` çağrıları hangi sonuçları verir?
## A
`equals` `true` döner, `compareTo` `0` döner; her ikisi de yalnız sayısal değere baktığı için sonuçlar tutarlıdır
🤖 `equals` yalnız sayısal değere bakmaz, ölçeği de karşılaştırır.
## B
`equals` `false` döner (ölçek de karşılaştırılır), `compareTo` `0` döner (yalnız sayısal değere bakılır)
🤖 Doğru: `equals` ölçek dahil karşılaştırır, `compareTo` yalnız değeri karşılaştırır.
## C
`equals` `true` döner, `compareTo` pozitif bir değer döner; `2.00` daha çok ondalık hane taşıdığı için büyük sayılır
🤖 hane sayısı büyüklük belirlemez, `compareTo` sayısal eşitlik bulur.
## D
Her ikisi de eşit değil sonucu verir; `compareTo` da ölçeği hesaba kattığı için `0` dışında bir değer döner
🤖 `compareTo` ölçeği yok sayar ve eşit değerler için `0` döndürür.
## Doğru
B
## Açıklama
`BigDecimal.equals` yalnız sayısal değeri değil **ölçeği (scale) de** karşılaştırır; bu yüzden `2.0` (ölçek 1) ile `2.00` (ölçek 2) `equals` açısından **eşit değildir**. `compareTo` ise ölçeği yok sayıp yalnız sayısal değere baktığı için `0` (eşit) döndürür. `hashCode` da `equals` ile tutarlıdır, yani iki nesnenin özet değerleri farklıdır — bu nedenle `BigDecimal`'i `HashSet` üyesi veya `HashMap` anahtarı yapmak, aynı tutarı iki ayrı kayıt sanmaya yol açar. En sık görülen biçimi sıfır kontrolüdür: `tutar.equals(BigDecimal.ZERO)` ifadesi `0.00` için `false` verir, doğrusu `tutar.compareTo(BigDecimal.ZERO) == 0`'dır. A ve C `equals`'ı sayısal karşılaştırma sanır, D ise `compareTo`'ya ölçek duyarlılığı yükler; üçü de yanlıştır.

# para-5 | İleri
## Soru
1000,00 TL anapara üç eşit taksite bölünüyor. Her taksit `setScale(2, RoundingMode.HALF_UP)` ile 333,33 çıkıyor ve taksitlerin toplamı 999,99 oluyor; 1 kuruş eksik. Sektörde kabul gören çözüm nedir?
## A
Her taksit yukarı yuvarlanır: 333,34 × 3 = 1000,02; müşteriden 2 kuruş fazla tahsil etmek kabul edilebilir sayılır
🤖 anaparadan fazla tahsilat, eksik tahsilat kadar hatalıdır ve mutabakatı yine bozar.
## B
Taksitler `double` ile hesaplanıp en sonda `BigDecimal`'e çevrilir; böylece ara adımlarda kuruş farkı hiç oluşmaz
🤖 `double` ondalık kesirleri tam tutamadığı için sapmayı çözmez, büyütür.
## C
Anapara ile taksitler toplamı arasındaki fark hesaplanıp son taksite eklenir: 333,33 + 333,33 + 333,34 = 1000,00
🤖 Doğru: kuruş farkı açık bir düzeltme adımıyla belirli bir taksite yazılır.
## D
Yuvarlama kipi `HALF_EVEN` yapılır; bankacı yuvarlaması kullanıldığında taksitler toplamı anaparayı her zaman tutar
🤖 `HALF_EVEN` sistematik sapmayı azaltır ama toplamın anaparaya eşitliğini garanti etmez.
## Doğru
C
## Açıklama
Bölme tam çıkmadığında oluşan kuruş farkı bir **yuvarlama kipi seçimiyle** çözülmez; **açık bir düzeltme adımı** ister. Standart desen şudur: taksitleri hesapla, anapara ile taksitler toplamı arasındaki farkı bul, farkı **son taksite** (bazı ürünlerde ilk taksite) ekle. Böylece ödeme planındaki taksitlerin toplamı anaparaya birebir eşitlenir. Her taksidi yukarı yuvarlamak fazla tahsilat üretir ve bu da bir hatadır (A yanlış). `double` kullanmak sapmayı çözmez, ondalık kesirleri tam tutamadığı için daha da kötüleştirir (B yanlış). `HALF_EVEN` (bankacı yuvarlaması) yarımları iki yöne dağıtarak **toplamdaki sistematik sapmayı azaltır** ve bu yüzden toplu faiz/tahakkuk hesaplarında tercih edilir; ama "toplam her zaman anaparayı tutar" garantisi vermez (D yanlış).

# para-6 | İleri
## Soru
REST servisin kredi tutarını JSON'da sayı olarak dönüyor. Sunucuda `BigDecimal` ile doğru hesaplanan büyük tutarlar tarayıcıdaki ekranda kuruş sapmasıyla görünüyor. Sebep ve doğru çözüm nedir?
## A
JavaScript'in tek sayı tipi 64 bit ikili kayan noktadır; büyük tutarlarda hassasiyet kaybolur — tutar dizge (string) taşınmalı, sunucuda düz gösterim açılmalıdır
🤖 Doğru: kayıp tarayıcıda sayının çözümlenmesinde olur, çözüm tutarı dizge olarak taşımaktır.
## B
JSON standardı ondalık sayıyı desteklemez; tutarlar tam sayı gönderilmek zorundadır ve ondalık kısım ayrı bir alanda taşınır
🤖 JSON ondalık sayıyı destekler, sorun standartta değil tarayıcının sayı tipindedir.
## C
Sorun sunucudadır; Jackson `BigDecimal` alanlarını serileştirirken `double`'a düşürür, çözüm ondalık hane sayısını bir biçimlendirme anotasyonuyla sabitlemektir
🤖 Jackson `BigDecimal`'i `double`'a düşürmez ve hane sabitlemek tarayıcıdaki kaybı önlemez.
## D
Sorun veritabanı sütununun `DECIMAL` yerine `FLOAT` tanımlı olmasıdır; sütun `DECIMAL(19,2)` yapıldığında tarayıcıdaki sapma da ortadan kalkar
🤖 değer sunucuda doğru hesaplanıyor, sapma taşıma katmanında oluştuğu için sütun tipi çözüm değildir.
## Doğru
A
## Açıklama
JavaScript'in tek sayı tipi 64 bit **ikili kayan noktadır** (IEEE 754). JSON'daki bir sayı tarayıcıda bu tipe çözümlendiği için, sunucuda `BigDecimal` ile tam hesaplanmış büyük tutarlar tarayıcıda **hassasiyet kaybına** uğrar; hata sunucuda değil taşıma ve çözümleme aşamasında oluşur. Güvenli çözüm tutarı **dizge (string)** olarak taşımaktır (`"tutar": "1250.75"`) ve sunucu tarafında Jackson'ın `WRITE_BIGDECIMAL_AS_PLAIN` ayarını açarak değerin bilimsel gösterimle değil düz yazılmasını sağlamaktır. JSON ondalık sayıyı destekler, dolayısıyla sorun standartta değildir (B yanlış). Jackson `BigDecimal`'i `double`'a düşürmez ve ondalık hane sayısını sabitlemek tarayıcıdaki çözümleme kaybını engellemez (C yanlış). Veritabanı sütunu `FLOAT` olsaydı bu ayrı ve gerçek bir hata olurdu, ama burada değer sunucuda doğru olduğu için sapmanın kaynağı o değildir (D yanlış).
