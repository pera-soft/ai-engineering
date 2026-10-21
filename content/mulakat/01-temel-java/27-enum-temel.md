---
slug: enum-temel
baslik: "Enum: Tip Güvenli Sabitler, Davranış ve Singleton"
kademe: Temel
sira: 27
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir asansör paneli düşün: bina kaç katlıysa o kadar düğme vardır, ne bir eksik ne bir fazla. "17. kat" diye bir düğme yoktur, çünkü bina 5 katlıdır — kimse yanlışlıkla oraya basamaz. Enum (enumeration / numaralandırma) programda tam olarak bunu yapar: bir değerin alabileceği bütün seçenekleri önceden, sınırlı bir liste olarak tanımlar. Alternatifi olan "katı sayı olarak tut" yaklaşımında 999 da yazılabilir ve bunu kimse durdurmaz; hata ancak asansör tavana çarpınca, yani program çalışırken anlaşılır.

### Temel kavramlar

- **Sabit (constant)**: program boyunca değişmeyen değer.
- **Tip güvenliği (type safety)**: derleyicinin, bir yere yalnız uygun türde değer konmasını zorlaması.
- **Enum**: sınırlı sabit kümesini kendi türü olarak tanımlayan yapı — `enum Yon { KUZEY, GUNEY, DOGU, BATI }`. Buradaki `Yon` artık `int` ya da `String` gibi gerçek bir türdür.
- **Örnek (instance)**: bir sınıftan üretilmiş nesne. Enum'da her sabit, o enum sınıfının TEK bir örneğidir.
- **Constructor (yapıcı)**: nesne kurulurken çalışan özel metot.
- **Singleton**: bir sınıftan programda yalnız tek bir örnek bulunması deseni.

Eski yol ile enum yan yana:

```java
public static final int KUZEY = 0;      // eski yol: int sabiti
void ayarla(int yon) { ... }
ayarla(999);                            // DERLENİR — hata çalışma zamanına kalır

enum Yon { KUZEY, GUNEY, DOGU, BATI }   // enum yolu
void ayarla(Yon yon) { ... }
ayarla(999);                            // DERLEME HATASI
```

Kazanç bellekte değildir (her enum sabiti bir nesnedir, `int`'ten büyük yer kaplar), hızda da değildir. Kazanç **tip güvenliğidir**: geçersiz değer derlemede reddedilir. Yanında anlamlı `toString`, tam `switch` desteği ve ayrı bir isim alanı (namespace) da gelir. Veritabanına yazma kolaylığı enum'a özgü bir avantaj değildir (aksine `name()`/`ordinal()` kararı gerektirir).

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="int sabiti tip güvensiz, enum tip güvenli">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">int sabiti mi, enum mı?</text>
  <rect x="24" y="44" width="300" height="146" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="174" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">int sabiti (tip güvensiz)</text>
  <rect x="44" y="82" width="260" height="30" rx="6" fill="#FFFFFF" stroke="#B26B00" stroke-width="1.1"/>
  <text x="174" y="102" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">void ayarla(int yon)</text>
  <text x="174" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ayarla(999) → DERLENİR</text>
  <text x="174" y="156" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">hata çalışma-zamanına kalır</text>
  <text x="174" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her int geçer, anlamsız log</text>
  <rect x="336" y="44" width="300" height="146" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="486" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">enum (tip güvenli)</text>
  <rect x="356" y="82" width="260" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="486" y="102" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">void ayarla(Yon yon)</text>
  <text x="486" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ayarla(999) → DERLEME HATASI</text>
  <text x="486" y="156" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yalnız Yon sabitleri geçer</text>
  <text x="486" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">geçersiz değer derlemede yakalanır</text>
</svg>
```

### Nasıl çalışır?

Java'da enum yarım bir yapı değil, **tam bir sınıftır**: alan (field), metot ve constructor taşıyabilir.

```java
enum Gezegen {
    DUNYA(5.97e24), MARS(6.42e23);
    private final double kutle;
    Gezegen(double kutle) { this.kutle = kutle; }
    double kutle() { return kutle; }
}
```

`DUNYA(5.97e24)` satırı, `DUNYA` örneğini bu constructor ile kurar. `kutle` alanı `static` DEĞİLDİR; her sabit kendi değerini taşır. Enum constructor'ı asla `public` olamaz ve dışarıdan `new Gezegen(...)` yazmak derleme hatasıdır — örnekleri yalnız JVM, enum sınıfı yüklenirken bir kez yaratır.

Derleyicinin bedava verdiği metotlar:

- `values()` — tüm sabitleri **tanım sırasıyla** dizi olarak döndürür.
- `valueOf("KUZEY")` — isimden sabite çevirir. TAM eşleşme arar ve büyük/küçük harfe duyarlıdır ("kuzey" de patlar). Bulamazsa `null` DÖNDÜRMEZ, `IllegalArgumentException` fırlatır — "en yakın sabit" diye bir davranış yoktur. Güvenli arama için ya try/catch ya da önceden hazırlanmış bir `Map<String, Yon>` kullanılır.
- `name()` — sabitin ismi. `ordinal()` — tanım sırasındaki indeks (0'dan başlar).

**`ordinal()` tuzağı**: bu değer aynı kod için sabittir ve asla -1 dönmez; sorun kodun DEĞİŞMESİdir. Sabitleri yeniden sıralar ya da araya yeni bir sabit eklersen sonraki tüm indeksler kayar. Veritabanına `2` yazdıysan o `2` artık başka bir sabittir — veri sessizce bozulur (`ordinal` hâlâ dilde vardır, kaldırılmadı; yalnız kalıcılık için uygun değildir). Kalıcılık için `name()` ya da elle tanımlanmış, değişmez bir `kod` alanı saklanır. `ordinal()` yalnız geçici, bellek-içi kullanımlarda güvenlidir.

**EnumMap / EnumSet**: anahtar ya da eleman bir enum ise bunları kullan. `EnumMap` her sabitin `ordinal()` değerini doğrudan dizi indeksi olarak kullanır; değerleri düz bir dizide tutar. Hash hesabı, kova (bucket) ve çarpışma (collision) yönetimi yoktur — `HashMap`'ten hem belirgin hızlı hem az bellekli çalışır ve enum tanım sırasını korur. `EnumSet` aynı mantıkla bir bit vektörü tutar. İki uyarı: `EnumMap` null ANAHTAR kabul etmez (`HashMap` eder), ve ikisi de thread-safe değildir — avantaj verimliliktir, eşzamanlılık değil.

**Sabite-özgü metot gövdeleri** (constant-specific method bodies): her sabit kendi davranışını taşıyabilir; enum içinde `abstract` metot tanımlamak geçerlidir.

```java
enum Islem {
    TOPLA { int uygula(int a, int b) { return a + b; } },
    CIKAR { int uygula(int a, int b) { return a - b; } };
    abstract int uygula(int a, int b);
}
```

Bu, dallanmayı (`switch`) veri yapısının kendisine gömen **strateji** (strategy) ve **durum makinesi** (state machine) desenidir. Her çağrıda yeni örnek yaratılmaz; sabitler tekildir.

```svg
<svg viewBox="0 0 660 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Enum sabitleri JVM'in yarattığı tekil örneklerdir, new yasaktır">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her sabit = tek örnek (singleton)</text>
  <rect x="24" y="42" width="280" height="160" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">enum Gezegen</text>
  <rect x="46" y="80" width="236" height="44" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="164" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">DUNYA</text>
  <text x="164" y="117" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kutle = 5.97e24</text>
  <rect x="46" y="134" width="236" height="44" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="164" y="154" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">MARS</text>
  <text x="164" y="171" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kutle = 6.42e23</text>
  <text x="164" y="196" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">constructor yalnız enum içinde çağrılır</text>
  <rect x="336" y="42" width="300" height="160" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="486" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">JVM garantisi</text>
  <text x="356" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">• Her sabit sınıf yüklenince BİR kez yaratılır</text>
  <text x="356" y="114" font-size="10.5" fill="#1C3247" font-family="system-ui">• new Gezegen(...) → yasak (derleme hatası)</text>
  <text x="356" y="136" font-size="10.5" fill="#1C3247" font-family="system-ui">• Serileştirme name() ile → yeni örnek YOK</text>
  <text x="356" y="158" font-size="10.5" fill="#1C3247" font-family="system-ui">• Reflection açığı kapalı</text>
  <text x="356" y="184" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">→ en güvenli singleton yolu</text>
</svg>
```

**En güvenli singleton**: JVM her sabiti sınıf yüklenirken bir kez yaratır, `new` derleme hatasıdır, enum serileştirmesi (serialization) `name()` üzerinden çalıştığı için deserialize sırasında YENİ örnek üretilmez ve reflection açığı kapalıdır. Elle yazılan klasik singleton'ın bu iki açığı enum'da yoktur — Josh Bloch'un "tek elemanlı enum en iyi singleton" önerisinin dayanağı budur.

### Mülakatta ne sorulur?

- **"int sabiti yerine enum neden?"** → Derleme zamanı tip güvenliği; bellek/hız motivasyon değildir.
- **"`valueOf` bulamazsa ne olur?"** → `IllegalArgumentException` (null değil); harf duyarlıdır; derleme hatası değildir çünkü derleyici string'in içeriğini bilmez.
- **"`ordinal()` neden veritabanına yazılmaz?"** → Sıra değişince eski kayıtlar yanlış sabite eşlenir, veri sessizce bozulur.
- **"Enum alan/metot/constructor içerebilir mi?"** → Evet, tam sınıftır; ama constructor dışarıdan çağrılamaz ve örnek alanı her sabitte ayrıdır.
- **"`EnumMap` neden `HashMap`'ten iyi?"** → Dizi tabanlı erişim; hash ve çarpışma maliyeti yok. `EnumSet` bit vektörü.
- **"Neden enum en iyi singleton?"** → JVM örnek tekliği + serileştirme güvenliği + reflection açığının kapalı olması.

### Tek cümlede

Enum, "bu değer yalnız şu sayılı seçenekten biri olabilir" kuralını derleyiciye zorlatan tam bir sınıftır ve her sabiti JVM'in tek kez yarattığı bir örnektir.

# enum-1 | Temel
## Soru
`int` sabitleri (`public static final int ...`) yerine enum kullanmanın en önemli avantajı nedir?
## A
Bellek kazancı: enum sabitleri `int` sabitlerinden daha az yer kaplar
🤖 Tersine, her enum sabiti bir nesnedir; `int` sabitleri zaten daha küçüktür, kazanç bellekte değildir.
## B
Kalıcılık kolaylığı: enum değerleri veritabanına `int`'ten daha kolay yazılır
🤖 Kalıcılıkta ek kolaylık yoktur (hatta `name()`/`ordinal()` kararı gerekir); asıl fayda tip güvenliğidir.
## C
Hız kazancı: enum karşılaştırması çalışma-zamanında `int` karşılaştırmasından daha hızlıdır
🤖 Hız avantajı yoktur; fark derleme-zamanı güvenliğidir.
## D
Tip güvenliği: geçersiz/ilgisiz bir değer derleme-zamanında engellenir, metot yalnız tanımlı sabitleri kabul eder
🤖 Doğru: `ayarla(Yon)` yalnız `Yon` sabitlerini alır, geçersiz değer derlemede reddedilir.
## Doğru
D
## Açıklama
`void ayarla(int yon)` imzasında her `int` geçerlidir — `ayarla(999)` derlenir, hata runtime'a kalır. `void ayarla(Yon yon)` ise yalnız `Yon` sabitlerini kabul eder; geçersiz değer DERLEMEDE reddedilir (D). Ayrıca enum'lar anlamlı `toString`, tam `switch` desteği ve namespace sağlar. Bellek (A) ve hız (C) motivasyon değildir; kalıcılık (B) enum'a özgü bir avantaj değildir.

# enum-2 | Temel
## Soru
Son satır ne yapar?

```java
enum Renk { KIRMIZI, YESIL, MAVI }
Renk r = Renk.valueOf("MOR");
```
## A
`IllegalArgumentException` fırlatır çünkü "MOR" tanımlı bir sabit değildir
🤖 Doğru: `valueOf` tam eşleşme arar, bulamazsa istisna atar.
## B
`null` döndürür
🤖 `valueOf` null döndürmez; eşleşme yoksa istisna fırlatır.
## C
Derleme hatası verir
🤖 "MOR" bir string; derleyici içeriğini bilmez, hata çalışma-zamanındadır.
## D
En yakın sabiti (`MAVI`) döndürür
🤖 `valueOf` tam eşleşme arar; "en yakın" diye bir davranış yoktur.
## Doğru
A
## Açıklama
`valueOf(String)` verilen isimle TAM eşleşen sabiti döndürür; eşleşme yoksa `IllegalArgumentException` fırlatır (null değil) (A). İsim büyük/küçük harf duyarlıdır — "kirmizi" de patlar. `values()` ise tüm sabitleri tanım sırasıyla bir dizi olarak verir. Güvenli arama gerekiyorsa `valueOf`'u try/catch ile sarmalar ya da önceden bir `Map<String, Renk>` hazırlarsın.

# enum-3 | Orta
## Soru
Bir enum sabitinin veritabanına kalıcı olarak yazılması gerekiyor. `ordinal()` değerini saklamak neden tehlikelidir?
## A
`ordinal()` değeri JVM'in sabitleri yükleme sırasına bağlıdır, her çalışmada farklı çıkar
🤖 `ordinal` aynı kod için sabittir; sorun kodun DEĞİŞMESİdir.
## B
`ordinal()` 0'dan değil -1'den başlar, bu yüzden saklanan değerler negatif olur
🤖 `ordinal` 0'dan başlayan geçerli bir indekstir; -1 döndürmez.
## C
Sabitlerin sırası değişir ya da araya sabit eklenirse eski ordinal değerleri artık farklı sabitlere karşılık gelir
🤖 Doğru: kayan indeksler eski kayıtları yanlış sabitlere eşler.
## D
`ordinal()` metodu Java 17'de kullanımdan kaldırıldı, artık çağrılamaz
🤖 `ordinal` hâlâ vardır; kaldırılmadı, yalnız kalıcılık için uygun değildir.
## Doğru
C
## Açıklama
`ordinal()` sabitin tanım sırasındaki indeksidir (0'dan başlar) ve kod içinde sabittir. AMA sabitleri yeniden sıralar ya da araya bir sabit eklersen tüm sonraki ordinal'ler kayar — veritabanındaki eski `2` artık başka bir sabittir, veri sessizce bozulur (C). Bu yüzden kalıcılık için `name()` (string) veya açık, değişmez bir `kod` alanı saklanır. `ordinal()` yalnız EnumMap/EnumSet gibi geçici, bellek-içi kullanımlar için güvenlidir.

# enum-4 | Orta
## Soru
Bu enum hakkında hangisi DOĞRUdur?

```java
enum Gezegen {
    DUNYA(5.97e24), MARS(6.42e23);
    private final double kutle;
    Gezegen(double kutle) { this.kutle = kutle; }
    double kutle() { return kutle; }
}
```
## A
Constructor `public` olmalı ki `new Gezegen(...)` çağrılabilsin
🤖 Enum constructor'ı dışarıdan çağrılamaz; `new` yasaktır, örnekleri yalnız JVM yaratır.
## B
Her sabit, constructor ile başlatılan, kendi `kutle` değerini taşıyan tekil bir örnektir
🤖 Doğru: `DUNYA(5.97e24)` o örneği constructor ile kurar, her sabit kendi değerini taşır.
## C
`kutle` alanı tüm sabitlerde aynı değeri paylaşır (static gibi)
🤖 Alan static değil; her sabit ayrı örnek olduğundan kendi değerini tutar.
## D
Enum alan ve metot içeremez; yalnız sabit listesi tutabilen özel bir tiptir
🤖 Enum tam bir sınıftır; alan, metot ve constructor içerebilir.
## Doğru
B
## Açıklama
Enum tam bir sınıftır: örnek alanları, metotları ve constructor'ı olabilir. `DUNYA(5.97e24)` yazımı, `DUNYA` örneğini bu constructor ile oluşturur; her sabit KENDİ `kutle` değerini taşır (B). Constructor asla `public` olamaz ve dışarıdan `new Gezegen(...)` yasaktır (A yanlış) — örnekleri yalnız JVM, enum yüklenirken yaratır. Alan `static` olmadığından her sabitte ayrıdır (C yanlış).

# enum-5 | Orta
## Soru
Anahtarları bir enum olan bir eşleme için `HashMap<Yon, X>` yerine `EnumMap<Yon, X>` tercih etmenin temel nedeni nedir?
## A
`EnumMap` anahtarları rastgele (unordered) sırada gezer; bu da yineleme (iteration) maliyetini düşürür
🤖 Aksine `EnumMap` enum tanım sırasını korur; kazanç sıralamadan değil dizi tabanlı erişimden gelir.
## B
`EnumMap` iş parçacığı güvenlidir (thread-safe), `HashMap` ise dış senkronizasyon ister
🤖 İkisi de senkronize değildir; avantaj thread-safety değil verimliliktir.
## C
`EnumMap` null anahtar ve null değere izin verir, `HashMap` hiçbirine izin vermez
🤖 Tersine `EnumMap` null ANAHTAR kabul etmez; `HashMap` ise null anahtara izin verir. Fark performanstadır.
## D
`EnumMap` arka planda bir dizi (array) kullanır; hash hesabı ve çarpışma (collision) yönetimi olmadan daha hızlı ve az bellekle çalışır
🤖 Doğru: `ordinal()` dizi indeksi olarak kullanılır, hash/kova maliyeti ortadan kalkar.
## Doğru
D
## Açıklama
`EnumMap` anahtarların bir enum olduğunu bilir; her sabitin `ordinal()`'ini dizi indeksi olarak kullanıp değerleri düz bir dizide tutar. Böylece hash hesabı, kova (bucket) ve çarpışma yönetimi olmadan çalışır — `HashMap`'ten belirgin daha hızlı ve az bellekli, üstelik enum tanım sırasını korur (D). Aynı mantıkla `EnumSet` bir bit vektörü kullanır. Kural: anahtar/eleman bir enum ise `EnumMap`/`EnumSet` kullan.

# enum-6 | İleri
## Soru
Bu desen (her sabitin metodu override etmesi) neyi sağlar ve enum singleton olarak neden güvenlidir?

```java
enum Islem {
    TOPLA { int uygula(int a, int b) { return a + b; } },
    CIKAR { int uygula(int a, int b) { return a - b; } };
    abstract int uygula(int a, int b);
}
```
## A
Her sabit kendi davranışını taşır (strategy); JVM tek örnekliği garanti ettiğinden enum en güvenli singleton'dır
🤖 Doğru: davranış veri yapısına gömülür ve enum singleton reflection/serileştirme açıklarını kapatır.
## B
Sadece söz dizimsel şıklık; derleyici bunu normal bir `switch`'e çevirir, hiçbir ek garanti yok
🤖 Fark var: davranış her sabite gömülür (strategy) ve JVM örnek tekliğini garanti eder.
## C
`abstract` metot enum'da derlenmez; enum örtük olarak `final` olduğundan soyut olamaz
🤖 Enum, sabite-özgü gövdelerle `abstract` metot içerebilir; kod geçerlidir.
## D
Bu desen her çağrıda yeni bir `Islem` örneği yaratır, bu yüzden singleton değildir
🤖 Sabitler tekildir; her çağrıda yeni örnek yaratılmaz.
## Doğru
A
## Açıklama
Sabite-özgü gövdeler (constant-specific method bodies) her sabitin farklı davranışı override etmesini sağlar — bu, dallanmayı (switch) veri yapısına gömen bir **strategy / state machine** desenidir (A). Enum singleton güvenliğinin sebebi: JVM her sabiti sınıf yüklenirken tek kez yaratır, `new` yasaktır ve enum serileştirmesi `name()` üzerinden çalıştığından deserialize sırasında YENİ örnek üretilmez (klasik singleton'ın reflection/serileştirme açıklarını kapatır). Josh Bloch'un "tek elemanlı enum en iyi singleton" önerisinin temeli budur.
