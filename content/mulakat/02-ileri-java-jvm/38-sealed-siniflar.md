---
slug: sealed-siniflar
baslik: "Sealed Sınıflar: Kapalı Hiyerarşiler ve Toplam Tipler"
kademe: İleri
sira: 38
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir trafik lambası düşün: yeşil, sarı, kırmızı. Dördüncü bir renk yoktur ve olmayacağını herkes bilir. Bu "kapalı liste" bilgisi çok değerlidir: lambayı yorumlayan biri üç ihtimali de ele aldıysa işini **eksiksiz** bitirmiştir. Java'da klasik kalıtımda böyle bir güvence yoktur — bir arayüzü dünyanın herhangi bir yerindeki herhangi bir sınıf implement edebilir, dolayısıyla "kaç ihtimal var?" sorusunun cevabı hiçbir zaman kesin değildir. `sealed`, "bu tip yalnız şu türlerden biri olabilir" demeni ve **derleyicinin bunu bilmesini** sağlar.

### Temel kavramlar

- **Hiyerarşi (hierarchy):** bir üst tip ve onu genişleten (`extends`) ya da uygulayan (`implements`) alt tipler ağacı.
- **Açık hiyerarşi:** alt tür kümesi sonsuzdur; her an yeni bir sınıf eklenebilir. Java'nın varsayılanı budur.
- **Kapalı hiyerarşi (closed hierarchy):** izin verilen alt türler önceden sayılmıştır, listeye dışarıdan ekleme yapılamaz. `sealed`'in verdiği şey budur.
- **`permits`:** izin verilen **doğrudan alt türleri** tek tek sayan yan tümce. Paketle ya da "kim çağırabilir" iznine ilgisi yoktur; yalnız **kimin genişletebileceğini** belirler.
- **Eksiksizlik / exhaustiveness:** bir `switch`'in olası tüm halleri kapsadığının **derleme zamanında ispatlanması**.
- **Toplam tip (sum type / ADT — algebraic data type):** fonksiyonel dillerden gelen kavram: "bir değer sonlu, kapalı bir alternatif kümesinden **tam olarak biridir**." Örneğin bir `Sonuc` ya `Basari(deger)`'dir ya `Hata(mesaj)`; üçüncü ihtimal yoktur.

`sealed` Java 17'de kalıcılaştı. `final` ile karıştırma: **`final` hiçbir** alt türe izin vermez; **`sealed` seçili** bir kümeye izin verir.

```java
sealed interface Odeme permits Kart, Havale {}
record Kart(String no) implements Odeme {}
record Havale(String iban) implements Odeme {}
```

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="sealed kapalı hiyerarşi ve izinli alt türler">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">sealed interface Sekil permits Daire, Kare, Ucgen</text>
  <rect x="240" y="44" width="160" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Sekil (sealed)</text>
  <rect x="40" y="150" width="150" height="46" rx="10" fill="#EAF1F8" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="115" y="178" text-anchor="middle" font-size="12.5" fill="#1C3247" font-family="system-ui">Daire (final)</text>
  <rect x="245" y="150" width="150" height="46" rx="10" fill="#EAF1F8" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="320" y="178" text-anchor="middle" font-size="12.5" fill="#1C3247" font-family="system-ui">Kare (final)</text>
  <rect x="450" y="150" width="150" height="46" rx="10" fill="#EAF1F8" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="525" y="178" text-anchor="middle" font-size="12.5" fill="#1C3247" font-family="system-ui">Ucgen (final)</text>
  <line x1="320" y1="90" x2="115" y2="150" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="320" y1="90" x2="320" y2="150" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="320" y1="90" x2="525" y2="150" stroke="#5D7789" stroke-width="1.5"/>
  <text x="320" y="228" text-anchor="middle" font-size="12" font-weight="600" fill="#B4322B" font-family="system-ui">Listede olmayan 4. tür EKLENEMEZ → switch exhaustive olabilir</text>
</svg>
```

### Nasıl çalışır?

Derleyici `permits` listesini bir sözleşme gibi uygular. Listede olmayan bir sınıf `Odeme`'yi implement etmeye kalkarsa derleme hatası alır. Bunun karşılığında derleyici, kod içindeki her yerde `Odeme`'nin **yalnız** `Kart` ya da `Havale` olabileceğini bilir.

Bu bilginin ayakta kalması için izin verilen **her alt tür**, kendi dalının ne kadar açık olduğunu beyan etmek zorundadır; üç seçenekten biri şarttır:

1. **`final`** — kendi altına hiç türetmeye izin vermez, dal burada kapanır.
2. **`sealed`** — kendi altını da sınırlı bir `permits` listesine kilitler, kapalılık sürer.
3. **`non-sealed`** — "buradan itibaren hiyerarşi yeniden AÇIK" der; bilinçli bir çıkış kapısıdır.

Bu zorunluluk olmasaydı bir alt türün altından habersizce yeni türler türeyebilir ve eksiksizlik ispatı çökerdi. Erişim değiştiricileri (`public`, `protected`) ve `abstract` bu kuralla ilgisizdir; izin verilen alt türlerin soyut olma zorunluluğu yoktur.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="izinli alt tür için zorunlu üç değiştirici seçeneği">
  <text x="320" y="26" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">İzinli alt tür ZORUNLU olarak biri: final / sealed / non-sealed</text>
  <rect x="24" y="50" width="192" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="78" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">final</text>
  <text x="120" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">altına türetme YOK</text>
  <text x="120" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dal burada kapanır</text>
  <text x="120" y="176" text-anchor="middle" font-size="22" fill="#25804C" font-family="system-ui" font-weight="700">■</text>
  <rect x="224" y="50" width="192" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="78" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">sealed</text>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kendi permits listesi</text>
  <text x="320" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kapalılık sürer</text>
  <text x="320" y="176" text-anchor="middle" font-size="22" fill="#186399" font-family="system-ui" font-weight="700">◧</text>
  <rect x="424" y="50" width="192" height="150" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="520" y="78" text-anchor="middle" font-size="13.5" font-weight="700" fill="#B26B00" font-family="system-ui">non-sealed</text>
  <text x="520" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeniden AÇIK</text>
  <text x="520" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">herkes genişletebilir</text>
  <text x="520" y="176" text-anchor="middle" font-size="22" fill="#B26B00" font-family="system-ui" font-weight="700">□</text>
</svg>
```

İzinli alt türlerin **nerede durabileceği** de kısıtlıdır: sealed tip isimlendirilmiş bir modüldeyse (named module) alt türler **aynı modülde**, isimsiz modüldeyse (unnamed module — klasik classpath) **aynı pakette** bulunmalıdır. Amaç basittir: derleyici tüm alt türleri görebilmelidir. Alt türlerin hepsi sealed tiple **aynı kaynak dosyadaysa** `permits` yan tümcesini yazmak bile gerekmez; derleyici listeyi kendisi çıkarır.

Kapalılığın en somut karşılığı `switch`'te ortaya çıkar. Bir `switch` sealed tipin tüm izinli alt türlerini kapsıyorsa **exhaustive** sayılır ve `default` dalına **gerek kalmaz** — derleyici zaten başka ihtimal olmadığını ispatlamıştır. Asıl kazanç ise ileride görülür: `permits` listesine yeni bir tür eklenip `switch` güncellenmezse **derleme hatası** alırsın. Yani "yeni bir hal eklendi ama işlenmedi" hatası, üretime çıkmadan derleme anında yakalanır. `default` yazmak bu güvenlik ağını ortadan kaldırır, çünkü unutulan hal sessizce `default`'a düşer.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="permits listesine yeni tür eklenince switch derleme hatası verir">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yeni alt tür eklendi ama switch güncellenmedi</text>
  <rect x="40" y="48" width="250" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">permits Daire, Kare, Ucgen</text>
  <text x="165" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">switch: Daire islenir</text>
  <text x="165" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">switch: Kare islenir</text>
  <text x="165" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">switch: Ucgen islenir</text>
  <text x="165" y="172" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">exhaustive → default GEREKMEZ</text>
  <rect x="350" y="48" width="250" height="150" rx="12" fill="#F3E4E4" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">permits ..., Kalp (yeni)</text>
  <text x="475" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">switch: Kalp dalı YOK</text>
  <text x="475" y="128" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">DERLEME HATASI</text>
  <text x="475" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"not exhaustive"</text>
  <text x="475" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hata derleme anında yakalanır</text>
  <text x="320" y="222" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Kapalı hiyerarşi = unutulan hal derleyici tarafından yakalanır</text>
</svg>
```

Not: ok (`->`) sözdizimli `switch`'te yalnız eşleşen dalın çalışması ve `break` gerekmemesi **sealed'e özgü değildir**; bu, ok sözdizimli her `switch`'in davranışıdır. Sealed'in kattığı şey performans da değildir — kattığı şey derleme zamanı ispatıdır.

### Mülakatta ne sorulur?

**"`sealed` tam olarak neyi kısıtlar?"** — Kimin genişletebileceğini/implement edebileceğini. Paket erişimini, metot override'ını ya da alan değişmezliğini değil; bunlar sırasıyla erişim değiştiricileri, `final` metot ve `final` alanların işidir.

**"`permits` ne demektir?"** — "Bu tipi yalnız şu türler implement/extend edebilir." Alt türlerin `abstract` olması gerekmez; `final`, `sealed` ya da `non-sealed` olmaları gerekir.

**"Alt tür kuralları nelerdir?"** — Üçünden biri zorunlu: `final` / `sealed` / `non-sealed`. Ayrıca aynı modül (named module) ya da aynı paket (classpath) kuralı; aynı dosyadaysa `permits` isteğe bağlı.

**"Ne kazandırır?"** — Exhaustiveness: `default` gerekmez, eksik hal derleme hatasıdır. Bu, ADT modellemesinin kalbidir.

**"Hangi fonksiyonel kavramın Java karşılığıdır?"** — Toplam tip (sum type / ADT). Yüksek dereceli fonksiyonlar (lambda), tembel değerlendirme (`Supplier`/stream) ve kuyruk özyinelemesi başka kavramlardır; sealed ile ilgileri yoktur.

**"`sealed` + `record` + pattern matching birlikte ne verir?"** — `sealed` olası tür kümesini kapatır, `record` her alternatifi değişmez ve yıkımlanabilir (deconstructable) yapar, `switch` pattern matching her alternatifi bileşenlerine ayırarak işler. Sonuç: `default`'suz, tip güvenli, **derleyici-denetimli davranış dağıtımı (dispatch)** — `instanceof` zincirlerine ve visitor desenine modern alternatif. Bu üçlü serileştirmeyi (serialization) otomatikleştirmez, hiyerarşiyi genişletilebilir yapmaz (tam tersini yapar) ve çöp toplayıcı (GC) davranışını değiştirmez.

### Tek cümlede

`sealed`, bir tipin alt tür kümesini `permits` ile kapatarak — her alt türün `final`/`sealed`/`non-sealed` olması ve aynı modül/pakette bulunması şartıyla — derleyicinin `switch`'in eksiksizliğini ispatlamasını sağlar; yani Java'ya toplam tipi (sum type) kazandırır.

# seal-1 | Orta
## Soru
`sealed` bir sınıf/arayüz temel olarak neyi kısıtlar?
## A
Sınıfın örneklenmesini (instantiation) yalnız aynı pakete izin vererek kısıtlar
🤖 sealed paket erişimini değil, KİMİN genişletebileceğini kısıtlar.
## B
Sınıfın metotlarının override edilmesini engeller
🤖 Metot override'ı `final` metotla engellenir; sealed sınıf hiyerarşisini kısıtlar.
## C
Sınıfı/arayüzü hangi türlerin genişletebileceğini/implement edebileceğini `permits` ile açıkça sınırlar
🤖 Doğru: sealed'in işi izin verilen alt türleri kapalı bir listeye kilitlemektir.
## D
Sınıfın alanlarını değişmez (immutable) yaparak durumu kilitler
🤖 Değişmezlik `final` alanlarla sağlanır; sealed hiyerarşiyle ilgilidir, alanlarla değil.
## Doğru
C
## Açıklama
`sealed`, bir tipin **genişletilebilir alt tür kümesini** açıkça sınırlar: `permits` listesinde sayılmayan hiçbir sınıf bu tipi genişletemez/implement edemez. Yani soru "bu tip kaç farklı şey olabilir?" ve cevabı derleyicinin bildiği kapalı bir küme haline gelir. Bu, `final` ile karıştırılmamalıdır: `final` HİÇBİR alt türe izin vermez; `sealed` ise SEÇİLİ bir kümeye izin verir. Paket erişimi (A), metot override'ı (B) ve alan değişmezliği (D) tamamen farklı mekanizmalardır. sealed'in değeri, bu kapalılığın exhaustive `switch` gibi güçlü derleyici garantilerini mümkün kılmasıdır.

# seal-2 | Orta
## Soru
`sealed interface Sekil permits Daire, Kare {}` tanımında `permits` ne anlama gelir?
## A
`Sekil`'in yalnız `Daire` ve `Kare` paketlerinde kullanılabileceğini bildirir
🤖 `permits` paket değil, izinli ALT TÜRLERİ listeler.
## B
`Daire` ve `Kare`'nin `Sekil`'i çağırabilecek tek sınıflar olduğunu bildirir
🤖 Çağırma iznini değil, genişletme (implement/extends) iznini belirler.
## C
`Daire` ve `Kare`'nin soyut (abstract) olması gerektiğini bildirir
🤖 İzinli alt türler soyut olmak zorunda değil; final/sealed/non-sealed olmalıdır.
## D
`Sekil`'i yalnız `Daire` ve `Kare`'nin implement edebileceğini, başka türün edemeyeceğini bildirir
🤖 Doğru: permits, izin verilen alt tür kümesini kapalı biçimde sayar.
## Doğru
D
## Açıklama
`permits`, sealed tipin **izin verilen doğrudan alt türlerini** açıkça listeler. `permits Daire, Kare` demek: "`Sekil`'i yalnız `Daire` ve `Kare` implement edebilir; başka hiçbir tür edemez." Bu bir **genişletme** kısıtıdır (A yanlış: paketle ilgisi yok; B yanlış: çağırmayla değil türetmeyle ilgili). İzinli alt türler soyut olmak zorunda DEĞİLDİR (C yanlış); tersine kapalılığı korumak için `final`, `sealed` ya da `non-sealed` olmalıdırlar. Not: izinli alt türler, sealed tip isimlendirilmiş bir modüldeyse aynı **modülde**, isimsiz modüldeyse (classpath) aynı **pakette** bulunmak zorundadır. Alt türler sealed tiple aynı kaynak dosyadaysa `permits` yazmak bile gerekmez; derleyici listeyi kendisi çıkarır.

# seal-3 | İleri
## Soru
`sealed` bir tipin izin verilen (permitted) her alt türü hangi değiştiricilerden birini almak ZORUNDADIR?
## A
`final`, `sealed` veya `non-sealed`
🤖 Doğru: her alt tür kapalılığı sürdürmek için bu üçünden birini seçmelidir.
## B
Yalnız `final`; başka seçenek yoktur
🤖 `final` seçeneklerden biridir ama `sealed` ve `non-sealed` de geçerlidir.
## C
`public` veya `protected`
🤖 Bunlar erişim değiştiricisidir; sealed hiyerarşi kuralıyla ilgisi yoktur.
## D
`abstract`
🤖 `abstract` zorunlu değildir; zaten hiyerarşinin açık/kapalı durumunu belirlemez.
## Doğru
A
## Açıklama
Sealed hiyerarşinin kapalılığı, alt türlerin de "ne kadar açık olduklarını" beyan etmesiyle korunur. İzin verilen her alt tür üç seçenekten birini almak zorundadır: (1) **`final`** — kendi altına hiç türetmeye izin vermez; (2) **`sealed`** — kendi altını da sınırlı bir `permits` listesine kilitler; (3) **`non-sealed`** — "buradan itibaren hiyerarşi yeniden AÇIK" der ve herkesin genişletmesine izin verir. Bu zorunluluk, derleyicinin tüm olası alt türleri bilmesini garanti eder; aksi halde exhaustiveness ispatlanamazdı. Erişim değiştiricileri (C) ve `abstract` (D) bu kuralla ilgisizdir. `non-sealed` seçeneği, kapalılıktan bilinçli çıkış kapısıdır.

# seal-4 | İleri
## Soru
Sealed bir tip üzerinde `switch` yazarken, tüm izinli alt türler kapsanmışsa hangi avantaj doğar?
## A
`switch` çalışma zamanında daha hızlı olur çünkü JIT tüm dalları satır içine alır
🤖 Asıl kazanım performans değil, derleme zamanı exhaustiveness güvencesidir.
## B
`default` dalı yazmaya GEREK KALMAZ; derleyici tüm hallerin kapsandığını ispatlar (exhaustiveness)
🤖 Doğru: kapalı hiyerarşi sayesinde derleyici eksiksizliği doğrular, default gereksizleşir.
## C
`switch` yalnız ilk eşleşen dalı çalıştırıp gerisini atlar, böylece `break` yazmaya gerek kalmaz
🤖 Bu, ok (`->`) sözdizimli her switch'in davranışıdır; sealed olmakla kazanılmaz.
## D
Alt türler otomatik olarak `Comparable` implement eder
🤖 sealed olmak Comparable ile ilgisizdir; sıralama sağlamaz.
## Doğru
B
## Açıklama
Sealed tipin en güçlü faydası **exhaustiveness (eksiksizlik) denetimidir**. Hiyerarşi kapalı olduğu için derleyici tüm olası alt türleri bilir; `switch` bu türlerin HEPSİNİ kapsıyorsa `default` dalı GEREKMEZ (B doğru). Dahası, ileride `permits` listesine yeni bir tür eklersen ve `switch`'i güncellemezsen **derleme hatası** alırsın — bu, "yeni bir hal eklendi ama işlenmedi" hatasını derleme zamanında yakalar. Bu, `default` ile sessizce yutulan hatalara karşı büyük bir güvenlik ağıdır. Performans (A) veya "yalnız eşleşen dal çalışır" davranışı (C — bu, ok sözdizimli her switch'te vardır) sealed'e özgü değildir; `Comparable` (D) tamamen alakasızdır. "sealed + exhaustive switch" kombinasyonu, ADT modellemesinin kalbidir.

# seal-5 | İleri
## Soru
Sealed tipler fonksiyonel programlamadaki hangi kavramı Java'da modellemeye yarar?
## A
Yüksek dereceli fonksiyonları (higher-order functions), yani fonksiyonu parametre alıp döndürmeyi
🤖 Yüksek dereceli fonksiyonlar lambda/functional interface ile sağlanır; sealed ile değil.
## B
Tembel değerlendirmeyi (lazy evaluation), yani değerin ancak gerektiğinde hesaplanmasını
🤖 Tembellik `Supplier`/stream ile ilgilidir; sealed hiyerarşi modelleme aracıdır.
## C
Kuyruk özyinelemesini (tail recursion), yani özyinelemeli çağrının döngüye dönüştürülmesini
🤖 Java kuyruk özyineleme optimizasyonu yapmaz; sealed ile de ilgisizdir.
## D
Toplam tipi (sum type / ADT): "bir değer sonlu, kapalı bir olasılık kümesinden tam olarak biridir"
🤖 Doğru: sealed, kapalı alternatif kümesini modelleyen sum type'ın Java karşılığıdır.
## Doğru
D
## Açıklama
Sealed tipler, fonksiyonel dillerdeki **toplam tip (sum type)** veya **cebirsel veri tipini (ADT)** Java'ya taşır. Bir sum type, "bir değer şu kapalı alternatiflerden TAM OLARAK biridir" der: örneğin bir `Sonuc`, ya `Basari(deger)` ya da `Hata(mesaj)`'dır — üçüncü ihtimal yoktur. sealed + record kombinasyonu bunu doğrudan ifade eder: `sealed interface Sonuc permits Basari, Hata`. Kapalılık, `switch` ile her alternatifi eksiksiz işlemeyi (exhaustive matching) mümkün kılar. Yüksek dereceli fonksiyonlar (A), tembellik (B) ve kuyruk özyineleme (C) farklı fonksiyonel kavramlardır ve sealed ile modellenmezler. "Kapalı alternatif kümesi = sum type" eşleştirmesi, sealed'in var oluş amacını özetler.

# seal-6 | Uzman
## Soru
`sealed` + `record` + `switch` pattern matching bir arada kullanıldığında elde edilen en güçlü tasarım avantajı nedir?
## A
Alt tür kümesi kapalı olduğu için nesneler `Serializable` bildirimine gerek kalmadan otomatik serileştirilir (serialization)
🤖 Serileştirme bu üçlüden bağımsızdır; sealed/record/switch otomatik serileştirme sağlamaz.
## B
Hiyerarşinin dışarıdaki kütüphaneler tarafından sınırsızca genişletilebilmesi; yeni alt türler mevcut `switch`'leri bozmadan eklenir
🤖 Tam tersine sealed hiyerarşiyi KAPATIR; yeni alt tür eklenirse switch'ler bilerek derleme hatası verir.
## C
Kapalı bir tür kümesi üzerinde, exhaustive ve record-deconstruction'lı `switch` ile tip güvenli, derleyici-denetimli davranış dağıtımı (dispatch)
🤖 Doğru: üçlü, "kapalı küme + eksiksiz eşleştirme + bileşen yıkımı" ile tip güvenli dispatch verir.
## D
Kapalı hiyerarşideki nesnelerin çöp toplayıcı (garbage collector) tarafından ayrı bir havuzda tutulup hiç toplanmaması
🤖 GC davranışı bu dil özellikleriyle değişmez; sealed/record bellek yönetimini etkilemez.
## Doğru
C
## Açıklama
Bu üçlü, nesne yönelimli `instanceof` zincirlerine ve `visitor` desenine güçlü bir alternatiftir. **sealed** olası tür kümesini kapatır; **record** her alternatifi değişmez ve yıkımlanabilir (deconstructable) yapar; **switch pattern matching** ise `case Basari(var d) -> ...`, `case Hata(var m) -> ...` ile her alternatifi bileşenlerine ayırarak işler. Kapalılık sayesinde derleyici `switch`'in **exhaustive** olduğunu ispatlar — yeni bir alt tür eklenip switch güncellenmezse derleme kırılır. Sonuç: `default` dalı olmadan, tip güvenli, derleyici-denetimli bir davranış **dağıtımı (dispatch)** elde edilir (C doğru). Otomatik serileştirme (A), sınırsız genişletilebilirlik (B — sealed tam tersini yapar) ve GC davranışı (D) bu avantajla ilgisizdir. Bu desen, modern Java'da veri odaklı programlamanın (data-oriented programming) temel taşıdır.
