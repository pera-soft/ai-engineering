---
slug: veri-tipleri-primitifler
baslik: "İlkel Tipler: Boyutlar, Varsayılanlar, Taşma ve Tip Dönüşümü"
kademe: Temel
sira: 2
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Şöyle düşün: alışverişten geldin, aldıklarını kaplara koyacaksın. Zeytin için küçük kavanoz, un için büyük torba. Kap ne kadar büyükse o kadar çok alır ama o kadar da yer kaplar. Java'da sayılar için seçtiğin **ilkel tip** (primitive type) tam olarak bu kaptır. Yanlış kap seçersen ya boşuna yer harcarsın ya da değer kaba sığmaz — ve Java sığmadığında sana haber vermez, sessizce başka bir sayı üretir. Bu ders hangi kapın ne kadar aldığını, sığmayınca ne olduğunu ve kaplar arası aktarmanın kurallarını anlatıyor.

### Temel kavramlar

- **İlkel tip (primitive type)**: doğrudan değerin kendisini tutan, nesne olmayan tip. Java'da tam **8 tane** vardır. `String` ilkel DEĞİLDİR — o bir sınıftır (nesnedir).
- **Bit**: 0 ya da 1 tutan en küçük birim. Bir tipin bit sayısı kaç farklı değer tutabileceğini belirler.
- **İşaretli (signed)**: negatif değer de tutabilen. **İşaretsiz (unsigned)**: yalnız 0 ve pozitif tutan.

Sekiz ilkel tip:

- Tam sayılar — hepsi **işaretlidir**: `byte` (8 bit, -128..127), `short` (16 bit, -32768..32767), `int` (32 bit, -2³¹..2³¹-1 yani -2147483648..2147483647), `long` (64 bit).
- Kesirli sayılar — **IEEE 754** standardı: `float` (32 bit), `double` (64 bit).
- `char` (16 bit, **işaretsiz**, 0..65535; bir **UTF-16 kod birimi** / code unit tutar) ve `boolean` (mantıksal; boyutu JVM gerçeklemesine bırakılmıştır).

Kritik not: bu boyutlar **dil tarafından sabitlenmiştir**, platformdan bağımsızdır. C'de `int` makineye göre değişebilir; Java'da asla değişmez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İlkel tiplerin bit boyutları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">8 ilkel tip ve bit boyutları</text>
  <rect x="40" y="44" width="110" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="95" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">byte</text>
  <text x="95" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">8 bit</text>
  <rect x="165" y="44" width="110" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="220" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">short / char</text>
  <text x="220" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">16 bit</text>
  <rect x="290" y="44" width="110" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="345" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">int / float</text>
  <text x="345" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">32 bit</text>
  <rect x="415" y="44" width="110" height="46" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="470" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">long / double</text>
  <text x="470" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">64 bit</text>
  <text x="320" y="130" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Genişleme (widening): kayıpsız, otomatik</text>
  <text x="320" y="150" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">byte → short → int → long → float → double</text>
  <text x="320" y="176" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Daraltma (narrowing): kayıp riskli, açık cast gerekir</text>
  <text x="320" y="196" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">double → ... → int → ... → byte  ((byte) ile)</text>
</svg>
```

İki terim daha, çünkü sonraki kural bunlara dayanıyor:

- **Alan (field)**: bir sınıfa/nesneye ait değişken.
- **Yerel değişken (local variable)**: bir metodun içinde tanımlanan değişken.

### Nasıl çalışır?

**1) Varsayılan değerler.** **Alanlar** hiç atanmasa bile otomatik ilklenir: sayısal tipler `0` (`long`→`0L`, `float`→`0.0f`, `double`→`0.0d`), `char`→`'\u0000'`, `boolean`→`false`, referans tipler → `null`. Java C değildir; okuduğunda çöp (garbage) değer gelmez, sıfırlama deterministiktir. Buna karşılık **yerel değişkenlerin varsayılanı YOKTUR** — atanmadan okunursa derleyici hata verir. Bu ayrım mülakatta sürekli karıştırılır.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ilkel tip varsayılan değerleri; yerel değişkende yok">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Alan (field) varsayılanları — atanmasa bile geçerli</text>
  <rect x="30" y="42" width="180" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">sayısal tipler</text>
  <text x="120" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">0 · 0L · 0.0f · 0.0d</text>
  <rect x="230" y="42" width="180" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">char / boolean</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">'\u0000' · false</text>
  <rect x="430" y="42" width="180" height="62" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="520" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">referans tipler</text>
  <text x="520" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">null</text>
  <text x="320" y="140" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Yerel değişken (local): VARSAYILAN YOK</text>
  <text x="320" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">atanmadan okunursa → derleme hatası</text>
</svg>
```

**2) Taşma (overflow).** Kaplar sabit genişlikte olduğu için sığmayan değer sessizce **sarar** (wrap-around, iki'ye tümleyen aritmetiği):

```java
int x = Integer.MAX_VALUE;   // 2147483647
System.out.println(x + 1);   // -2147483648  (Integer.MIN_VALUE)
```

Bu satırda hiçbir istisna fırlamaz; değer büyüyüp `2147483648` de olamaz, çünkü `int` 32 bitle sınırlıdır. `ArithmeticException` yalnızca tam sayıyı **sıfıra bölmede** atılır. Taşmayı yakalamak istiyorsan `Math.addExact(x, 1)` kullan — o `ArithmeticException` fırlatır; ya da `(long) x + 1` ile daha geniş kaba geç.

**3) Tip dönüşümü (type conversion).** İki yön vardır. **Genişleme** (widening: `byte → short → int → long → float → double`) kayıpsızdır, derleyici **otomatik** yapar. **Daraltma** (narrowing: örn. `long → int`, `int → byte`) bilgi kaybı riski taşır, bu yüzden **açık dönüşüm (cast) zorunludur**:

```java
long a = 100;        // derlenir: int → long, genişleme, otomatik
int  b = 100L;       // DERLENMEZ: 100L bir long, long → int daraltmadır, cast şart
int  c = (int) 100L; // derlenir
int  d = 100;        // derlenir: 100 zaten int literalidir
```

Daraltma **kırpma (clamp) yapmaz**, üst bitleri **atar**:

```java
byte b = (byte) 130;
System.out.println(b);   // -126
```

Satır satır: `130` ikili yazılışta `1000_0010`'dur. `byte` yalnız en düşük 8 biti tutar → `1000_0010`. `byte` işaretli olduğundan en soldaki bit **işaret bitidir**; iki'ye tümleyen kuralıyla bu değer **-126**'dır — en yakın sınır olan `127`'ye çekilmez. Aynı mantıkla `(byte) 200` sonucu **-56**'dır. Cast'i silip `byte b = 130;` yazarsan bu kez **derleme hatası** alırsın (aralık dışı sabit).

**4) Kayan nokta (floating point).** `float`/`double` **IEEE 754** ikili biçimdedir; `0.1`, `0.2`, `0.3` gibi ondalıkların ikili tabanda **sonsuz açılımı** vardır, yani tam temsil edilemez, **yaklaşık** saklanır:

```java
System.out.println(0.1 + 0.2);          // 0.30000000000000004
System.out.println(0.1 + 0.2 == 0.3);   // false
```

Kod sözdizimsel olarak geçerlidir (derlenir) ve istisna da fırlatmaz; sorun tamamen anlamsal yuvarlamadır.

### Mülakatta ne sorulur?

- **"Kaç ilkel tip var, `int` kaç bit?"** → 8 ve 32. `String`'i sayıp 9 demek klasik hatadır; `char` ile `boolean`'ı unutup 7 demek de öyle. 64 bit olan `long`'dur.
- **Alan mı yerel mi?** Atanmamış `int` **alan** `0`'dır — `null` değildir (`null` yalnız referanslar için) ve çöp değer içermez. Aynı kural yerel değişkende geçerli değildir.
- **Taşma sessizdir.** İstisna bekleyen aday elenir.
- **Genişleme/daraltma derlenir mi?** `long a = 100;` evet, `int b = 100L;` hayır.
- **`(byte) 130` = -126.** Java kırpmaz, bit atar. En sık verilen yanlış cevap `127`'dir.
- **`double` karşılaştırma.** `==` yanıltıcıdır; küçük bir tolerans (epsilon) ile `Math.abs(a - b) < 1e-9` kullanılır, para gibi kesinlik gereken yerlerde ise `BigDecimal`.
- **`char` ince noktaları.** 16 bit ve **işaretsizdir** (0..65535) — işaretli 16-bit olan `short`'tur (-32768..32767), karıştırma. 8 bit ve ASCII sınırlı değildir (ASCII 7-bittir). Sayısal bağlamda tam sayı gibi kullanılabilir: `int k = 'A';` → 65, `char c = (char) 66;` → `'B'`. `String` ile aynı şey değildir: `char` bir ilkel değer, `String` bir nesnedir. En ince nokta: BMP dışındaki karakterler (çoğu emoji) tek `char`'a sığmaz, iki `char`'lık **surrogate çifti** gerektirir — bu yüzden `char` "bir karakter" değil, "bir **kod birimi**"dir.

### Tek cümlede

İlkel tipler platformdan bağımsız sabit genişlikte kaplardır: sığmayan değer sessizce sarar, daraltma kırpmaz bit atar ve kesirli sayılar hep yaklaşıktır.

# prim-1 | Temel
## Soru
Java'da kaç ilkel (primitive) tip vardır ve `int` kaç bittir?
## A
9 ilkel tip; `int` 16 bit
🤖 `String` ilkel değildir; tip sayısı 8, `int` ise 32 bittir.
## B
7 ilkel tip; `int` 32 bit
🤖 `int` boyutu doğru ama tip sayısı 8'dir (char ve boolean dahil).
## C
8 ilkel tip; `int` 64 bit
🤖 Tip sayısı doğru ama `int` 32 bittir; 64 bit olan `long`'dur.
## D
8 ilkel tip; `int` 32 bit
🤖 Doğru: byte/short/int/long/float/double/char/boolean; int 32 bit.
## Doğru
D
## Açıklama
Java'nın tam olarak **8 ilkel tipi** vardır: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean`. `int` **32 bit** işaretli bir tam sayıdır (aralık: -2^31 .. 2^31-1). 64 bit olan `long`'dur (C tuzağı). `String` bir ilkel değil, bir sınıftır (nesnedir); onu saymak (A gibi 9'a çıkarmak) klasik hatadır. `char` ve `boolean` de ilkel oldukları için toplam 7 değil 8'dir (B yanlış).

# prim-2 | Temel
## Soru
Bir sınıfın `int` tipindeki alanı (field) hiç atanmadan bırakılırsa varsayılan değeri nedir?
## A
`null`
🤖 `null` yalnız referans tipleri içindir; ilkel `int` null olamaz.
## B
Tanımsızdır; okumaya çalışınca çöp (garbage) değer gelir
🤖 Java C değildir; alanlar deterministik biçimde sıfırlanır, çöp değer olmaz.
## C
`0`
🤖 Doğru: sayısal ilkel alanların varsayılanı 0'dır (long 0L, double 0.0).
## D
Derleme hatası verir; her alan açıkça atanmalıdır
🤖 Alanlar (field) otomatik varsayılan alır; zorunlu atama kuralı yerel değişkenler içindir.
## Doğru
C
## Açıklama
Nesne/sınıf **alanları** (field) otomatik olarak **varsayılan değere** ilklenir: sayısal ilkeller `0` (`long`→`0L`, `float`→`0.0f`, `double`→`0.0d`), `char`→`'\u0000'`, `boolean`→`false`. Bu yüzden atanmamış `int` alan `0`'dır — `null` değildir (A; `null` sadece referanslar için) ve C/C++'taki gibi çöp değer içermez (B). Kritik ayrım: bu otomatik varsayılan **yalnız alanlar** için geçerlidir; **yerel değişkenlerin** (metot içi) varsayılanı yoktur, atanmadan kullanılırsa derleyici hata verir (D bu kuralı yanlış yere uygular).

# prim-3 | Temel
## Soru
Aşağıdaki kod ne yazdırır?

```java
int x = Integer.MAX_VALUE;
System.out.println(x + 1);
```
## A
`ArithmeticException` fırlatır
🤖 Tam sayı taşması istisna fırlatmaz; `ArithmeticException` yalnız 0'a bölmede olur.
## B
`Integer.MIN_VALUE` (yani -2147483648)
🤖 Doğru: taşma en büyük değerden en küçüğe sarar (wrap-around).
## C
`2147483648` (değer büyür)
🤖 `int` 32 bitle sınırlı; bu değeri tutamaz, taşar ve sarar.
## D
`0`
🤖 Taşma sıfıra değil, en küçük negatif değere sarar; sonuç MIN_VALUE'dur.
## Doğru
B
## Açıklama
Java'da tam sayı aritmetiği **sabit genişliklidir** ve taşma (overflow) **sessizce sarar** (wrap-around, iki'ye tümleyen aritmetiği): `Integer.MAX_VALUE + 1`, `Integer.MIN_VALUE`'ya (-2147483648) döner — hiçbir istisna fırlamaz. `ArithmeticException` yalnızca tam sayıyı **sıfıra bölmede** (`/ 0`) atılır (A yanlış). Değer büyüyüp `2147483648` olamaz çünkü `int` 32 bitle sınırlıdır (C). Taşmayı yakalamak istersen `Math.addExact(x, 1)` kullan — o durumda `ArithmeticException` fırlatır. `long`'a genişletmek de (`(long) x + 1`) taşmayı önler.

# prim-4 | Orta
## Soru
Aşağıdaki iki satırdan hangisi/hangileri derlenir?

```java
long a = 100;        // (1)
int  b = 100L;       // (2)
```
## A
Sadece (1) derlenir
🤖 Doğru: int→long genişlemedir (otomatik); long→int daraltmadır, cast ister.
## B
İkisi de derlenir
🤖 (2) daraltmadır (long→int), açık cast olmadan derlenmez.
## C
Sadece (2) derlenir
🤖 Ters: (1) genişleme olduğu için otomatik geçer, (2) daralma cast gerektirir.
## D
İkisi de derlenmez
🤖 (1) geçerli bir genişletmedir; en az biri derlenir.
## Doğru
A
## Açıklama
**Genişleme** (widening) — küçük tipten büyüğe — kayıpsız olduğu için **otomatiktir**: `int` (100) değeri `long`'a sorunsuz atanır, (1) derlenir. **Daraltma** (narrowing) — büyükten küçüğe — bilgi kaybı riski taşıdığından **açık dönüşüm (cast) gerektirir**: `100L` bir `long`'tur ve `int b`'ye atanması daraltmadır; `int b = (int) 100L;` gerekir, aksi halde (2) derlenmez. Not: `int b = 100;` (sondaki `L` olmadan) elbette derlenir çünkü `100` zaten `int` literalidir; buradaki tuzak `100L`'in `long` oluşudur.

# prim-5 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
byte b = (byte) 130;
System.out.println(b);
```
## A
`130`
🤖 `byte` en fazla 127 tutar; 130 daraltmada olduğu gibi kalamaz.
## B
`127` (en yakın sınıra kırpılır / clamp)
🤖 Java kırpma (clamp) yapmaz; fazla bitleri atar, sonuç -126 olur.
## C
`-126`
🤖 Doğru: 130'un düşük 8 biti alınır, işaretli byte olarak -126 verir.
## D
Derleme hatası; 130 `byte` aralığında değil
🤖 Açık cast var; derlenir, çalışma-zamanında bit kırpma sonucu -126 üretir.
## Doğru
C
## Açıklama
`(byte)` daraltma dönüşümü (narrowing cast) fazla üst bitleri **atar**, en düşük 8 biti tutar — kırpma (clamp) yapmaz. `130` = `1000_0010` (ikili). En düşük 8 bit `1000_0010`; `byte` işaretli olduğundan en yüksek bit işaret bitidir → iki'ye tümleyen ile bu `-126`'dır. Bu yüzden sonuç **-126**, `127`'ye kırpma (B) veya `130` olarak kalma (A) değildir. Açık cast bulunduğu için derleme hatası da olmaz (D); casti kaldırıp `byte b = 130;` yazsaydık O ZAMAN derleme hatası olurdu (aralık dışı sabit).

# prim-6 | İleri
## Soru
Aşağıdaki kod ne yazdırır ve nedeni nedir?

```java
System.out.println(0.1 + 0.2 == 0.3);
```
## A
`true`; ondalık aritmetik her zaman kesin olduğundan toplam tam olarak `0.3` çıkar
🤖 `double` IEEE 754'tür; 0.1 ve 0.2 ikili tabanda tam temsil edilemez.
## B
`ArithmeticException` fırlatır; kayan noktada kesinlik kaybı istisnayla bildirilir
🤖 Kayan nokta işlemleri istisna fırlatmaz; sessizce yaklaşık sonuç üretir.
## C
Derleme hatası verir; kayan nokta değerleri `==` operatörüyle karşılaştırılamaz
🤖 Sözdizimsel olarak geçerlidir, derlenir; sorun anlamsal (yuvarlama), sözdizimsel değil.
## D
`false`; `0.1` ve `0.2` ikili kayan noktada tam temsil edilemediğinden toplam `0.3`'ten azıcık sapar
🤖 Doğru: IEEE 754 yuvarlama hatası, toplam 0.30000000000000004 civarıdır.
## Doğru
D
## Açıklama
`float`/`double`, **IEEE 754** ikili kayan nokta biçimidir; `0.1`, `0.2`, `0.3` gibi ondalıklar ikili tabanda **sonsuz açılıma** sahip olduğundan tam temsil edilemez, yaklaşık saklanır. `0.1 + 0.2` sonucu `0.30000000000000004` gibi çıkar ve `0.3`'ün saklanan yaklaşığına **eşit olmadığından** karşılaştırma **false**'tur. Bu yüzden kayan noktalı sayıları `==` ile karşılaştırmak hatalıdır; küçük bir tolerans (epsilon) ile `Math.abs(a-b) < 1e-9` denir veya para/kesin ondalık için `BigDecimal` kullanılır. Derleme geçerlidir (C), istisna atmaz (B) — sorun tamamen anlamsal yuvarlamadır.

# prim-7 | İleri
## Soru
`char` tipi hakkında aşağıdakilerden hangisi DOĞRUDUR?
## A
`char` işaretsiz 16 bitlik bir UTF-16 kod birimidir; sayısal bağlamda 0..65535 değeri gibi kullanılabilir
🤖 Doğru: char 16-bit işaretsizdir, aritmetikte kod birimi değeri gibi davranır.
## B
`char` 8 bittir ve yalnızca tek bir ASCII karakteri tutabilir; Unicode için yetersizdir
🤖 `char` 16 bittir; ASCII değil UTF-16 kod birimi tutar.
## C
`char` işaretli 16 bittir; aralığı `short` ile aynı, yani -32768..32767'dir
🤖 `char` işaretsizdir (0..65535); işaretli 16-bit olan `short`'tur.
## D
`char` aslında `String` ile aynı tiptir; tek karakterlik kısa bir metin nesnesidir
🤖 `char` bir ilkeldir (nesne değil); `String` bir sınıftır, ikisi farklı tiptedir.
## Doğru
A
## Açıklama
`char` **16 bit işaretsiz** bir ilkeldir; bir **UTF-16 kod birimini** (code unit, 0..65535) temsil eder. Sayısal bağlamda tam sayı gibi kullanılabilir: `int k = 'A';` → 65; `char c = (char) 66;` → `'B'`. İşaretli değildir, bu yüzden aralığı 0..65535'tir — `short`'un işaretli -32768..32767 aralığıyla karıştırılmamalıdır (C). 8 bit ve ASCII sınırlı değildir (B; ASCII 7-bittir, `char` çok daha geniştir). `String`'ten farklıdır: `char` ilkel bir değer, `String` bir nesnedir (D). İnce nokta: BMP dışındaki karakterler (emoji gibi) tek `char`'a sığmaz, iki `char`'lık surrogate çifti gerektirir — bu yüzden `char` "bir karakter" değil "bir kod birimi"dir.
