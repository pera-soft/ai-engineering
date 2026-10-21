---
slug: validation
baslik: "Doğrulama (Validation): @Valid vs @Validated, Kısıtlar, Gruplar ve Özel Validator"
kademe: Orta
sira: 77
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kuruma dilekçe verirken memur formu alır almaz bakar: ad boş mu, numara 11 hane mi, tarih geçerli mi? Eksikse formu **daha içeri göndermeden** geri verir. Yazılımda buna **doğrulama (validation)** denir: dışarıdan gelen veriyi, iş mantığına girmeden önce kurallara uyup uymadığı açısından denetlemek. Dışarıdan gelen hiçbir veri güvenilir değildir — kullanıcı yanlış yazabilir, başka bir sistem bozuk veri gönderebilir, kötü niyetli biri kasten uygunsuz girdi deneyebilir. Doğrulamayı en dış sınırda yaparsan hata mesajı net olur ve bozuk veri veritabanına hiç ulaşmaz.

### Temel kavramlar

- **DTO (Data Transfer Object):** İstek gövdesindeki JSON'un çevrileceği düz Java nesnesi; alanları dışarıdan beklediğin veriyi temsil eder.
- **Kısıt (constraint):** Bir alanın uyması gereken kuraldır ve alanın üstüne anotasyonla yazılır: `@NotNull` (null olamaz), `@NotBlank` (String: null/boş/yalnız-boşluk olamaz), `@NotEmpty` (koleksiyon veya String boş olamaz), `@Size(min, max)`, `@Min`/`@Max`, `@Email`, `@Pattern` (düzenli ifade), `@Positive` (pozitif sayı) …
- **Jakarta Bean Validation:** Bu kısıtların Java standardıdır (eski adıyla JSR-380, paketi `jakarta.validation`). Spring kendi doğrulama dilini icat etmez, bu standardı kullanır.
- **Tetikleyici:** Kısıtlar kendiliğinden çalışmaz. Bir nesnenin doğrulanması için `@Valid` ya da `@Validated` ile **açıkça tetiklenmesi** gerekir; tetikleyici yoksa kısıtlar DTO'da tanımlı olsa bile hiçbiri kontrol edilmez.
- **`@Valid` (Jakarta standardı):** Nesnenin doğrulamasını tetikler ve **iç içe (nested/cascade)** doğrulamayı yayar — `@Valid` konmuş alt nesneler de doğrulanır. **Doğrulama gruplarını desteklemez.**
- **`@Validated` (Spring'e özgü):** `@Valid`'in yaptıklarına ek olarak **doğrulama gruplarını** destekler ve sınıfa konduğunda **metot-düzeyi doğrulamayı** etkinleştirir.

Yön sık karıştırılır: standart olan `@Valid`, Spring'e özgü olan `@Validated`'tır — tersi değil. İkisi de alan tipine göre (sayısal/String) ayrım yapmaz ve aralarındaki fark bir isim tercihi değildir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="@Valid ile @Validated farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@Valid vs @Validated</text>
  <rect x="30" y="44" width="270" height="130" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">@Valid (Jakarta)</text>
  <text x="165" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">nesne doğrula</text>
  <text x="165" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">iç içe (cascade) yayar</text>
  <text x="165" y="140" text-anchor="middle" font-size="11" fill="#C0632A" font-family="system-ui">grup YOK</text>
  <text x="165" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@RequestBody + alan/nested</text>
  <rect x="340" y="44" width="270" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">@Validated (Spring)</text>
  <text x="475" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">nesne doğrula +</text>
  <text x="475" y="118" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">GRUP destekler</text>
  <text x="475" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">metot-düzeyi doğrulama</text>
  <text x="475" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sınıf/metot parametreleri</text>
</svg>
```

### Nasıl çalışır?

Controller sınırında tipik kullanım şudur:

```java
public class KullaniciDto {
    @NotBlank                 private String ad;
    @Email @NotBlank          private String eposta;
    @Size(min = 8, max = 64)  private String parola;
    @Valid                    private AdresDto adres;   // iç içe doğrulama yayılır
}

@RestController
class KullaniciController {
    @PostMapping("/kullanici")
    Yanit kaydet(@RequestBody @Valid KullaniciDto dto) { ... }
}
```

`@RequestBody` gövdedeki JSON'u nesneye çevirir (deserialization — JSON'dan nesneye okuma), `@Valid` ise nesnedeki kısıtları tetikler. İhlal varsa Spring MVC **`MethodArgumentNotValidException`** fırlatır; Spring Boot bunu varsayılan olarak **`400 Bad Request`**'e çevirir.

`@Valid` yazılmazsa gövde bağlanır ama **doğrulanmaz**. `@PathVariable` yol parçası içindir, JSON gövdesini bağlamaz. Hiç anotasyon koymamak ise ne bağlamayı ne doğrulamayı garanti eder.

Servis katmanında farklı bir mekanizma çalışır. Sınıfı **`@Validated`** ile işaretlersen Spring, metot düzeyi doğrulama için bean'in önüne bir **vekil nesne (AOP proxy)** koyar. Parametrelerdeki `@NotNull`/`@Size` her çağrıda kontrol edilir; ihlalde **`ConstraintViolationException`** fırlar. Bu, controller'daki `MethodArgumentNotValidException`'dan **farklı bir tiptir**.

Bu doğrulama kendiliğinden açılmaz ve vekil tabanlı olduğu için sınırları vardır. Metot **public**, bean de **Spring tarafından yönetilen** olmalıdır. `static` metotlar ve nesnenin kendi metodunu kendi içinden çağırması (self-invocation) vekili atlar; o çağrılarda doğrulama çalışmaz. `@RequestBody` ise web katmanına özgüdür; servis parametresi doğrulamasında geçersizdir.

Sık karıştırılan üçlüyü ezberden değil tablodan öğren.

```svg
<svg viewBox="0 0 640 204" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="NotNull NotEmpty NotBlank hangi girdiyi reddeder">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@NotNull vs @NotEmpty vs @NotBlank</text>
  <text x="150" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">girdi</text>
  <text x="330" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">@NotNull</text>
  <text x="430" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">@NotEmpty</text>
  <text x="545" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">@NotBlank</text>
  <text x="40" y="90" font-size="11" fill="#1C3247" font-family="system-ui">null</text>
  <text x="330" y="90" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">RED</text>
  <text x="430" y="90" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">RED</text>
  <text x="545" y="90" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">RED</text>
  <text x="40" y="120" font-size="11" fill="#1C3247" font-family="system-ui">"" (boş)</text>
  <text x="330" y="120" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">geçer</text>
  <text x="430" y="120" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">RED</text>
  <text x="545" y="120" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">RED</text>
  <text x="40" y="150" font-size="11" fill="#1C3247" font-family="system-ui">"   " (boşluk)</text>
  <text x="330" y="150" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">geçer</text>
  <text x="430" y="150" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">geçer</text>
  <text x="545" y="150" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">RED</text>
  <line x1="30" y1="98" x2="610" y2="98" stroke="#5D7789" stroke-width="0.8"/>
  <line x1="30" y1="128" x2="610" y2="128" stroke="#5D7789" stroke-width="0.8"/>
  <text x="320" y="184" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">"Gerçekten dolu" String için @NotBlank; koleksiyon için @NotEmpty</text>
</svg>
```

`@NotNull` yalnız `null`'ı reddeder; `""` ve `"   "` geçer. `@NotEmpty` null'ı ve boşu (`length == 0`) reddeder, ama uzunluğu sıfırdan büyük olduğu için `"   "` yine **geçer**. `@NotEmpty`'nin asıl yeri koleksiyon ve dizilerdir: "en az bir eleman olsun" demektir.

`@NotBlank` **yalnız String** içindir. Null'ı, boşu ve baştaki/sondaki boşluklar kırpıldığında (trim) boş kalan değeri reddeder. Kullanıcı adı, e-posta gibi "gerçekten dolu olmalı" alanlarda doğru seçim budur; bu incelik `"   "` gibi görünmez-boş girdilerin üretime sızmasını engeller.

İki yakın tuzak var. `@Size(min = 1)` yalnız uzunluğa bakar (`"   "` geçer) ve `null` değeri **geçerli sayar**. `@Positive` ise sayısal alanlar içindir, String içeriğini doğrulamaz.

**Doğrulama grupları (validation groups)** aynı DTO'yu senaryoya göre farklı kısıt alt kümeleriyle doğrulamayı sağlar. Klasik örnek: `id` alanı **güncellemede** dolu olmalı, **oluşturmada** boş. Gruplar genelde boş işaretçi (marker) arayüzlerdir:

```java
interface Olustur {}
interface Guncelle {}

public class KullaniciDto {
    @NotNull(groups = Guncelle.class) private Long id;
    @NotBlank                         private String ad;   // grupsuz = Default grubu
}

@PostMapping("/kullanici")
Yanit yarat(@RequestBody @Validated(Olustur.class) KullaniciDto dto) { ... }
```

Grup belirtilmeyen kısıtlar varsayılan (`Default`) gruba aittir. `@Valid` grup desteklemediği için bu senaryoda `@Validated` şarttır. Grupların tek işi, hangi kısıtların ne zaman çalışacağını seçmektir. Bir hızlandırma aracı, çoklu hata mesajı gösterme yöntemi ya da hataları toplayıp e-posta bildirimi atma mekanizması **değildirler**.

**Özel (custom) kısıt** iki parçadan oluşur: (1) `@Constraint(validatedBy = ...)` ile işaretlenmiş anotasyon — `message`, `groups`, `payload` öznitelikleriyle; (2) `ConstraintValidator<A, T>` arayüzünü uygulayan sınıf. Mantık bu sınıfın `isValid` metodunda yaşar:

```java
public class TcKimlikValidator implements ConstraintValidator<TcKimlik, String> {
    @Override
    public boolean isValid(String deger, ConstraintValidatorContext ctx) {
        return deger != null && deger.matches("\\d{11}") && kontrolHanesiDogru(deger);
    }
}
```

`true` geçerli, `false` ihlal demektir. Bu tasarım doğrulamayı **yeniden kullanılabilir ve bildirimsel (declarative — "nasıl" değil "ne" yazarsın)** kılar. Mantık tek yerde durur; controller içindeki `if` bloklarına, bir ayar dosyasındaki düzenli ifade (regex) satırına ya da veritabanı tetikleyicisine (trigger) dağılmaz. Hata mesajını üç yerden özelleştirebilirsin: anotasyonun `message` özniteliği, `messages.properties` dosyası ya da `isValid` içinde `ctx` nesnesi.

Controller sınırındaki akışın tamamı şöyle özetlenir.

```svg
<svg viewBox="0 0 640 158" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Controller doğrulama akışı: RequestBody Valid geçersizse 400">
  <defs>
    <marker id="val-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Controller doğrulama akışı</text>
  <rect x="16" y="52" width="150" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="91" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">@RequestBody</text>
  <text x="91" y="95" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">JSON → DTO</text>
  <rect x="190" y="52" width="150" height="56" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="265" y="76" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">@Valid</text>
  <text x="265" y="95" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kısıtları tetikler</text>
  <rect x="364" y="52" width="160" height="56" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="444" y="72" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">ihlal varsa</text>
  <text x="444" y="90" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">MethodArgument-</text>
  <text x="444" y="102" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">NotValidException</text>
  <rect x="548" y="52" width="80" height="56" rx="10" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="588" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#8A2E2E" font-family="system-ui">400</text>
  <text x="588" y="96" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">Bad Request</text>
  <line x1="166" y1="80" x2="188" y2="80" stroke="#186399" stroke-width="1.7" marker-end="url(#val-arw)"/>
  <line x1="340" y1="80" x2="362" y2="80" stroke="#186399" stroke-width="1.7" marker-end="url(#val-arw)"/>
  <line x1="524" y1="80" x2="546" y2="80" stroke="#186399" stroke-width="1.7" marker-end="url(#val-arw)"/>
  <text x="320" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">@Valid olmadan kısıtlar çalışmaz — gövde bağlanır ama doğrulanmaz</text>
</svg>
```

### Mülakatta ne sorulur?

- **`@NotNull` / `@NotEmpty` / `@NotBlank` farkı?** `null` → üçü de reddeder; `""` → yalnız `@NotNull` geçirir; `"   "` → yalnız `@NotBlank` reddeder.
- **Gövdeyi doğrulayan doğru imza?** `kaydet(@RequestBody @Valid KullaniciDto dto)` — tetikleyici olmadan kısıtlar çalışmaz.
- **`@Valid` vs `@Validated`?** `@Valid` Jakarta standardıdır, cascade yayar, **grup yoktur**; `@Validated` Spring'e özgüdür, **grup + metot-düzeyi doğrulama** getirir.
- **Hangi istisna, hangi durum kodu?** Controller'da `MethodArgumentNotValidException` → **400 Bad Request**; `@Validated` bean metot parametresinde `ConstraintViolationException`.
- **Gruplar ne işe yarar?** Aynı DTO'yu oluşturma/güncelleme gibi senaryolara göre farklı kısıt alt kümeleriyle doğrulamak.
- **Özel kısıt mantığı nerede yazılır?** `ConstraintValidator`'ı uygulayan sınıfın `isValid(...)` metodunda.
- **Servis metot parametreleri nasıl doğrulanır?** Sınıfa `@Validated`; proxy gereği metot public ve bean yönetilen olmalı — `static` ve self-invocation doğrulamayı atlar.

### Tek cümlede

Kısıtlar veriyi tarif eder, `@Valid`/`@Validated` onları tetikler; ihlal controller'da `MethodArgumentNotValidException` → 400 olarak, `@Validated` bean'inde ise `ConstraintViolationException` olarak geri döner.

# val-1 | Temel
## Soru
Bir String alanının `null`, boş (`""`) veya yalnız boşluk karakterlerinden oluşmamasını garanti eden kısıt hangisidir?
## A
`@NotNull`
🤖 `@NotNull` yalnız null'ı engeller; `""` veya `"   "` yine geçer.
## B
`@Size(min = 1)`
🤖 `@Size` yalnız uzunluğa bakar: `"   "` uzunluğu ≥1 olduğu için geçer, `null` değeri ise `@Size` geçerli sayar.
## C
`@Positive`
🤖 `@Positive` sayısal değerler içindir; String içerik doğrulamaz.
## D
`@NotBlank`
🤖 Doğru: `@NotBlank` null, boş ve yalnız-boşluk String'i reddeder; String'e özgüdür.
## Doğru
D
## Açıklama
Üç benzer kısıt karıştırılır: **`@NotNull`** yalnız `null`'ı reddeder (`""` geçer); **`@NotEmpty`** null ve boş (`length==0`) olanı reddeder ama `"   "` (yalnız boşluk, uzunluk >0) **geçer**; **`@NotBlank`** ise **yalnız String** içindir ve null'ı, boşu ve **trim sonrası boş kalan** (yalnız boşluk) değeri reddeder. Kullanıcı adı, e-posta gibi "gerçekten dolu olmalı" alanlarda doğru seçim **`@NotBlank`**'tir. `@NotEmpty` daha çok koleksiyon/dizi ("en az bir eleman") için uygundur. Bu incelik, üretimde `"   "` gibi görünmez-boş girdilerin sızmasını engeller.

# val-2 | Temel
## Soru
Bir controller metodunda gelen JSON gövdesini doğrulamak için hangi imza doğrudur?
## A
`public Yanit kaydet(@RequestBody @Valid KullaniciDto dto)`
🤖 Doğru: `@RequestBody` gövdeyi bağlar, `@Valid` doğrulamayı tetikler; geçersizse MethodArgumentNotValidException.
## B
`public Yanit kaydet(@RequestBody KullaniciDto dto)` — doğrulama otomatik çalışır
🤖 `@Valid`/`@Validated` olmadan kısıtlar tetiklenmez; gövde bağlanır ama doğrulanmaz.
## C
`public Yanit kaydet(@PathVariable @Valid KullaniciDto dto)`
🤖 `@PathVariable` bir yol parçasını bağlar, JSON gövdesini değil; nesne bağlama için yanlış.
## D
`public Yanit kaydet(KullaniciDto dto)` — Spring her parametreyi doğrular
🤖 Anotasyon olmadan ne bağlama ne doğrulama garanti değildir; kısıtlar çalışmaz.
## Doğru
A
## Açıklama
Controller'da JSON gövdesini bir DTO'ya bağlayıp doğrulamak için **`@RequestBody @Valid`** birlikte kullanılır: `@RequestBody` gövdeyi nesneye çevirir (deserialization), **`@Valid`** ise nesnedeki `jakarta.validation` kısıtlarını tetikler. Kısıt ihlali varsa Spring MVC **`MethodArgumentNotValidException`** fırlatır (varsayılanı `400 Bad Request`). Kritik nokta: **`@Valid` olmadan doğrulama çalışmaz** (B, D yanlış) — kısıt anotasyonları DTO'da tanımlı olsa bile tetikleyici gerekir. `@PathVariable` (C) yol parçası içindir, gövde bağlama için değil. Bu, "girdi asla güvenli değildir" ilkesinin controller sınırındaki uygulamasıdır.

# val-3 | Orta
## Soru
`@Valid` ile `@Validated` arasındaki asıl fark nedir?
## A
`@Valid` Spring'e özgüdür, `@Validated` Jakarta standardıdır
🤖 Tam tersi: `@Valid` Jakarta standardı, `@Validated` Spring'e özgüdür.
## B
`@Validated` doğrulama gruplarını destekler; `@Valid` desteklemez
🤖 Doğru: gruplar ve metot-düzeyi doğrulama Spring'in `@Validated`'ıyla gelir; `@Valid`'te grup yok.
## C
`@Valid` yalnız sayısal alanları doğrular, `@Validated` ise yalnız String alanları doğrular
🤖 İkisi de tip ayrımı yapmaz; fark grup/metot-düzeyi desteğidir, alan tipiyle ilgisi yok.
## D
İkisi tamamen aynıdır; hangisini yazacağın bir isim tercihidir
🤖 Fark gerçektir: gruplar ve metot-düzeyi doğrulama yalnız `@Validated`'ta vardır.
## Doğru
B
## Açıklama
**`@Valid`** Jakarta Bean Validation standardıdır; nesneyi doğrular ve iç içe (cascade) doğrulamayı yayar ama **doğrulama gruplarını desteklemez**. **`@Validated`** ise Spring'e özgüdür ve iki ek yetenek getirir: (1) **doğrulama grupları** (`@Validated(Olustur.class)` ile yalnız o gruba ait kısıtları çalıştırma), (2) sınıf düzeyine konulduğunda **metot-düzeyi doğrulamayı** etkinleştirme (servis metodu parametrelerini/dönüşlerini doğrulama). Basit gövde doğrulamasında ikisi de iş görür; ancak aynı DTO'nun **senaryoya göre** (oluşturmada id boş, güncellemede dolu) farklı kurallara tabi olması gerekince `@Validated` + gruplar şarttır.

# val-4 | Orta
## Soru
Doğrulama **grupları** (validation groups) hangi ihtiyacı karşılar?
## A
Aynı DTO'yu senaryoya göre farklı kısıt alt kümeleriyle doğrulamak
🤖 Doğru: kısıta `groups = ...` verilir; `@Validated(Guncelle.class)` ile yalnız o senaryonun (ör. oluşturma/güncelleme) kuralları çalışır.
## B
Doğrulama hatalarını konularına göre gruplayıp tek bir e-posta bildiriminde toplamak
🤖 Gruplar bildirimle ilgili değil; hangi kısıtların ne zaman çalışacağını belirler.
## C
Bir nesnedeki kısıtları performans kazanmak için paralel iş parçacıklarında çalıştırmak
🤖 Gruplar paralelleştirme değil, kısıt seçimidir; performansla ilgisi yok.
## D
Aynı alana birden çok hata mesajı atayıp hepsini sırayla göstermek
🤖 Çoklu mesaj ayrı bir konudur; grubun amacı senaryoya göre kısıt seçmektir.
## Doğru
A
## Açıklama
**Doğrulama grupları**, aynı sınıfın farklı **kullanım senaryolarında** farklı kısıt alt kümelerini uygulamayı sağlar. Klasik örnek: `KullaniciDto`'da `id` alanı **güncellemede** `@NotNull(groups = Guncelle.class)` olmalı ama **oluşturmada** boş olmalı. Kısıtlara `groups = ...` verilir; controller'da `@Validated(Guncelle.class)` denince yalnız o gruba ait kurallar çalışır. Grup belirtilmeyen kısıtlar varsayılan (`Default`) gruba aittir. Bu mekanizma tek DTO'yu birden çok endpoint'te güvenle kullanmayı sağlar; `@Valid` grup desteklemediği için bu senaryoda `@Validated` gerekir. Gruplar genelde işaretçi (marker) arayüzler olarak tanımlanır.

# val-5 | Orta
## Soru
`@Constraint(validatedBy = TcKimlikValidator.class)` ile tanımlı özel bir kısıtın doğrulama mantığı nerede yazılır?
## A
Controller metodunun içinde elle yazılan bir `if` bloğu olarak, her endpoint'te yeniden
🤖 Özel kısıt mantığı controller'a gömülmez; yeniden kullanılabilir bir validator sınıfına konur.
## B
`application.yml` içinde `dogrulama.tc-kimlik.regex` gibi bir anahtara yazılan düzenli ifade olarak
🤖 Basit kalıp `@Pattern` ile olabilir ama özel kısıt mantığı bir Java sınıfında (`isValid`) yazılır.
## C
`ConstraintValidator` uygulayan sınıfın `isValid(...)` metodunda
🤖 Doğru: özel kısıt, `ConstraintValidator<TcKimlik, String>` arayüzünü uygulayan sınıfın `isValid` metodunda mantığı barındırır.
## D
Veritabanı tarafında tanımlanmış bir trigger (tetikleyici) içinde
🤖 DB trigger uygulama-dışı ve tipe bağlı değildir; Bean Validation mantığı `isValid`'te yaşar.
## Doğru
C
## Açıklama
Özel bir kısıt iki parçadan oluşur: (1) `@Constraint(validatedBy = ...)` ile işaretlenmiş **anotasyon** (mesaj, groups, payload öznitelikleriyle), (2) **`ConstraintValidator<A, T>`** arayüzünü uygulayan sınıf. Doğrulama mantığı bu sınıfın **`isValid(T deger, ConstraintValidatorContext ctx)`** metodunda yazılır; `true` geçerli, `false` ihlal demektir. Örneğin bir TC kimlik numarası algoritması burada çalışır. Bu tasarım, doğrulamayı **yeniden kullanılabilir ve bildirimsel** (declarative) kılar — mantık controller'a (A) veya DB'ye (D) dağılmaz. Mesaj `isValid` içinde `ctx` ile özelleştirilebilir veya anotasyonun `message` özniteliği / `messages.properties` üzerinden gelir.

# val-6 | İleri
## Soru
Bir `@Service` bean'inin metot parametresine `@NotNull`/`@Size` gibi kısıtlar koyup bunların çalışmasını istiyorsak ne gerekir?
## A
Hiçbir şey; Spring, yönetilen tüm bean'lerin metot parametrelerini kendiliğinden doğrular
🤖 Metot-düzeyi doğrulama otomatik değildir; sınıfın `@Validated` ile işaretlenmesi gerekir.
## B
Sınıfı `@Validated` ile işaretleyip metot-düzeyi doğrulamayı açmak
🤖 Doğru: `@Validated` sınıf düzeyinde metot parametre/dönüş doğrulamasını açar; ihlalde `ConstraintViolationException` fırlar.
## C
Her parametreyi ayrıca `@RequestBody` ile işaretleyip metodu controller üzerinden çağırmak
🤖 `@RequestBody` web katmanına özgüdür; servis metodu parametre doğrulaması için geçersizdir.
## D
Metodu `static` yapmak
🤖 `static` metotlar Spring AOP proxy'sinden geçmez; doğrulama proxy'e bağlıdır, `static` işi bozar.
## Doğru
B
## Açıklama
Controller dışında, **servis metodu parametrelerini** doğrulamak için sınıfın **`@Validated`** ile işaretlenmesi gerekir. Bu, Spring'in metot-düzeyi doğrulama **AOP proxy'sini** devreye sokar; parametreye konan `@NotNull`, `@Size` gibi kısıtlar her çağrıda kontrol edilir ve ihlalde **`ConstraintViolationException`** fırlar (controller'daki `MethodArgumentNotValidException`'dan farklı tip). Doğrulama proxy tabanlı olduğundan: metot **public** ve bean **Spring tarafından yönetilen** olmalı; `static` (D) veya aynı sınıf içi self-invocation proxy'yi atlar ve doğrulama çalışmaz. Bu, iş katmanında girdi güvencesi sağlamanın (defensive programming) bildirimsel yoludur; controller doğrulamasını tekrarlamak yerine kritik servis sınırında da uygulanabilir.
