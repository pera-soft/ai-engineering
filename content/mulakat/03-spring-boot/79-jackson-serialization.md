---
slug: jackson-serialization
baslik: "Jackson Serileştirme: ObjectMapper, Anotasyonlar, Tarih/Zaman ve Jackson 3 Geçişi"
kademe: Orta
sira: 79
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo göndereceksin: elindeki eşyayı kutuya koyup üstüne okunaklı bir etiket yazarsın; karşı taraf kutuyu açıp eşyayı geri çıkarır. Programlar arasında da aynı şey olur — bellekteki bir Java nesnesi ağ üzerinden gönderilemez, önce **metne** çevrilmesi gerekir. Bu çevirinin yaygın biçimi JSON'dur ve Java dünyasında işi yapan kütüphane **Jackson**'dır. Spring Boot bunu varsayılan olarak kullanır; bu konu, kutulama kurallarını (hangi alan hangi adla gider, hangisi hiç gitmez, tarih nasıl yazılır) nasıl kontrol ettiğini anlatır.

### Temel kavramlar

- **JSON:** Veriyi metin olarak yazma biçimi — `{"ad":"Deniz","dogum":"2005-04-01"}`. Süslü parantez nesne, köşeli parantez liste, `"anahtar": değer` çiftleri. Tip olarak yalnız metin, sayı, mantıksal değer, liste ve nesne vardır; **tarih tipi yoktur**.
- **Serileştirme (serialization):** Bellekteki nesneyi metne çevirmek — Java nesnesi → JSON. Türkçesi "diziye/metne dökme".
- **Deserileştirme (deserialization):** Ters yön — gelen JSON metnini Java nesnesine geri kurmak.
- **`ObjectMapper`:** Jackson'ın merkez motoru. `writeValueAsString(nesne)` serileştirir, `readValue(metin, Kullanici.class)` deserileştirir. Spring Boot bir tanesini otomatik yapılandırıp **bean** olarak sunar. Elle `new ObjectMapper()` yazmak yerine bunu **enjekte etmelisin**; aksi hâlde Boot'un ayarları kaybolur. Yapılandırması bittikten sonra `ObjectMapper` **iş parçacığı güvenlidir (thread-safe)**: birçok istek aynı anda kullansa da bozulmaz, bu yüzden uygulama boyunca tek örnek paylaşılır.
- **`@JsonProperty("user_name")`:** Java alan adı ile JSON anahtarı farklıysa eşlemeyi kurar (`kullaniciAdi` alanı ↔ `user_name` anahtarı). Alana, getter'a veya setter'a konabilir.
- **`@JsonIgnore`:** Alanı hem yazmadan hem okumadan **tamamen** dışlar — parola, iç kimlik gibi hassas alanlar için.
- **`@JsonInclude(NON_NULL)`:** Değeri `null` olan alanları çıktıya hiç koymaz.
- **`@JsonCreator`:** Parametresiz yapıcısı olmayan **değişmez** (immutable) tipleri — `record`, `final` alanlı sınıflar — okurken hangi yapıcının veya fabrika metodunun kullanılacağını söyler; parametreler `@JsonProperty` ile adlandırılır.
- **Özel serializer:** Standart eşlemenin yetmediği biçimler için yazılan `JsonSerializer<T>` / `JsonDeserializer<T>` alt sınıfı.
- **Modül (`Module`):** Jackson'a toplu yetenek ekleyen paket; örneğin `java.time` tiplerini bilen **JavaTimeModule** (JSR-310).
- **Polimorfik tip:** Bir arayüzün/soyut sınıfın birden çok somut alt tipi olması (`Odeme` → `KrediKarti`, `Havale`).

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ObjectMapper ile serialize ve deserialize">
  <defs>
    <marker id="json-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/>
    </marker>
    <marker id="json-ar2" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ObjectMapper: iki yön</text>
  <rect x="40" y="70" width="180" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="130" y="98" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Java nesnesi</text>
  <text x="130" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kullanici(ad, dogum)</text>
  <rect x="420" y="70" width="180" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="510" y="98" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">JSON metni</text>
  <text x="510" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">{"ad":..,"dogum":..}</text>
  <line x1="222" y1="92" x2="418" y2="92" stroke="#25804C" stroke-width="1.8" marker-end="url(#json-ar)"/>
  <text x="320" y="84" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">writeValueAsString (serialize)</text>
  <line x1="418" y1="122" x2="222" y2="122" stroke="#186399" stroke-width="1.8" marker-end="url(#json-ar2)"/>
  <text x="320" y="138" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">readValue (deserialize)</text>
  <text x="320" y="178" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">@JsonProperty ad eşle · @JsonIgnore gizle · JavaTimeModule tarih</text>
</svg>
```

### Nasıl çalışır?

Jackson serileştirirken nesnenin `toString()`'ine bakmaz; **alan alan** dolaşır ve her alan için "adı ne olacak, dâhil edilecek mi, nasıl yazılacak" sorularını anotasyonlara bakarak yanıtlar.

```java
public class Kullanici {
  @JsonProperty("user_name") private String kullaniciAdi; // JSON'da user_name
  @JsonIgnore                private String parola;        // çıktıda hiç yok
  @JsonInclude(JsonInclude.Include.NON_NULL)
                             private String takmaAd;       // null ise atlanır
  private LocalDateTime kayitTarihi;                       // biçimi ayara bağlı
}
```

Toplu bir dönüşüm gerekiyorsa — örneğin tüm alan adları `alt_cizgili` biçimde (snake_case) yazılsın — her alana anotasyon yapıştırmak yerine `PropertyNamingStrategies.SNAKE_CASE` kullanılır; bu çözüm büyüdükçe de ayakta kalır.

Parolayı **kabul edip** çıktıya yazmamak istiyorsan `@JsonProperty(access = WRITE_ONLY)` vardır. Sınıf düzeyinde toplu dışlama için de `@JsonIgnoreProperties` kullanılır. Dikkat edilecek iki nokta var. JPA'nın `@Transient` anotasyonu "bu alan veritabanına yazılmasın" der; Jackson'ı hiç ilgilendirmez, alan JSON'a yine girer. `@JsonInclude(NON_EMPTY)` ise alanı yalnız boşken atlar; dolu parola yine sızar.

Standart eşlemenin yetmediği biçimlerde (`"₺1.250,00"` gibi) uzatma noktası **özel serializer**'dır. `JsonSerializer<T>` alt sınıfının `serialize(...)` metodunda istediğin çıktıyı yazarsın. Sonra bunu ya alana `@JsonSerialize(using = ParaSerializer.class)` ile bağlarsın ya da bir `SimpleModule` üzerinden tüm uygulamaya tanıtırsın. Aynı biçimi geri okumak için simetrik bir `JsonDeserializer<T>` yazılır.

Bu yaklaşım biçim mantığını **tek ve test edilebilir** bir yerde tutar. Elle `StringBuilder` ile JSON kurmak ya da alanı gizleyip yanına biçimlenmiş bir gölge alan koymak kırılgan geçici çözümlerdir.

`java.time` tiplerinde (`LocalDate`, `LocalDateTime`, `Instant`) iki katman vardır. Önce **JavaTimeModule** kayıtlı olmalıdır; Spring Boot bunu otomatik yapar. Sonra biçimi `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS` ayarı belirler. **Açıksa** çıktı epoch sayısı olur (1970'ten beri geçen saniye/milisaniye). **Kapalıysa** insanın okuyabildiği ISO-8601 metni gelir (`2026-07-24T10:15:30`).

Boot varsayılanı kapalıdır. Elle `new ObjectMapper()` yarattığında ya da ayarı ezdiğinde epoch'a düşersin. Çözüm alanın tipini `String`'e veya eski `java.util.Date`'e çevirmek değildir; modülü ve ayarı doğru kurmaktır. En kolayı Boot'un `ObjectMapper`'ını enjekte etmektir.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Jackson anotasyonlarının JSON çıktısına etkisi">
  <defs>
    <marker id="json-anno-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Anotasyon → JSON çıktısına etki</text>
  <text x="150" y="46" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Java alanı + anotasyon</text>
  <text x="500" y="46" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">JSON çıktısı</text>
  <rect x="24" y="56" width="252" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="150" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">kullaniciAdi</text>
  <text x="150" y="92" text-anchor="middle" font-size="10.5" fill="#186399" font-family="monospace">@JsonProperty("user_name")</text>
  <line x1="280" y1="78" x2="360" y2="78" stroke="#5D7789" stroke-width="1.4" marker-end="url(#json-anno-ar)"/>
  <rect x="364" y="56" width="252" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="490" y="82" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">"user_name": "deniz"</text>
  <rect x="24" y="108" width="252" height="44" rx="8" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="150" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">parola</text>
  <text x="150" y="144" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="monospace">@JsonIgnore</text>
  <line x1="280" y1="130" x2="360" y2="130" stroke="#5D7789" stroke-width="1.4" marker-end="url(#json-anno-ar)"/>
  <rect x="364" y="108" width="252" height="44" rx="8" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="490" y="134" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">(çıktıda hiç yok)</text>
  <rect x="24" y="160" width="252" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="150" y="180" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">takmaAd = null</text>
  <text x="150" y="196" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="monospace">@JsonInclude(NON_NULL)</text>
  <line x1="280" y1="182" x2="360" y2="182" stroke="#5D7789" stroke-width="1.4" marker-end="url(#json-anno-ar)"/>
  <rect x="364" y="160" width="252" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="490" y="186" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">(null → atlandı)</text>
  <text x="320" y="222" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ad eşle · alanı gizle · null'ı çıktıdan çıkar</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="WRITE_DATES_AS_TIMESTAMPS ayarına göre tarih biçimi">
  <defs>
    <marker id="json-date-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tarih biçimini belirleyen ayar</text>
  <rect x="200" y="40" width="240" height="46" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="monospace">LocalDateTime alanı</text>
  <text x="320" y="78" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">JavaTimeModule kayıtlı</text>
  <line x1="270" y1="86" x2="170" y2="120" stroke="#5D7789" stroke-width="1.4" marker-end="url(#json-date-ar)"/>
  <text x="150" y="108" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">özellik AÇIK (true)</text>
  <line x1="370" y1="86" x2="470" y2="120" stroke="#5D7789" stroke-width="1.4" marker-end="url(#json-date-ar)"/>
  <text x="490" y="108" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">özellik KAPALI (false)</text>
  <rect x="24" y="124" width="272" height="70" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="160" y="148" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">epoch sayısı</text>
  <text x="160" y="168" text-anchor="middle" font-size="11" fill="#1C3247" font-family="monospace">1721815530</text>
  <text x="160" y="185" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">makine-okunur, insana kapalı</text>
  <rect x="344" y="124" width="272" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="148" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">ISO-8601 metni</text>
  <text x="480" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">"2026-07-24T10:15:30"</text>
  <text x="480" y="185" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Boot varsayılanı · okunaklı</text>
</svg>
```

### Mülakatta ne sorulur?

- **Hangi anotasyon ne yapar.** `@JsonProperty` adı eşler (gizlemez), `@JsonIgnore` alanı tümüyle dışlar, `@JsonInclude` hangi değerlerin çıktıya gireceğini belirler. `@Column`/`@Transient` JPA tarafıdır, JSON ile ilgisi yoktur. Hassas alan sızıntısı gerçek bir güvenlik açığıdır; bu yüzden API DTO'ları entity'den ayrı tutulur.
- **Tarih neden sayı çıkıyor.** JavaTimeModule kayıtlı ama `WRITE_DATES_AS_TIMESTAMPS` açık kalmıştır. JSON'da tarih tipi olmadığı için değer metin ya da sayı olabilir; yaygın ve okunaklı tercih ISO-8601 metnidir.
- **Özel biçim nasıl verilir.** `JsonSerializer<T>` + `@JsonSerialize(using = ...)` ya da `Module` kaydı. Jackson otomatik `toString()` kullanmaz.
- **`@JsonCreator` ne zaman.** `record` veya `final` alanlı değişmez tiplerde parametresiz yapıcı olmadığından, okuma sırasında kullanılacak yapıcı/fabrika metodu `@JsonCreator` ile işaretlenir ve parametreleri `@JsonProperty` ile adlandırılır.
- **Polimorfik okuma.** Yazma yönünde sorun yoktur; asıl sorun **okuma** yönündedir. `{"tutar":100}` metninin `KrediKarti` mı `Havale` mi olduğu bilinemez. Çözüm, JSON'a bir **tip ayırt edici** (type discriminator) gömmektir: taban tipe `@JsonTypeInfo(use = Id.NAME, property = "tip")` ve `@JsonSubTypes({@Type(value = KrediKarti.class, name = "kredi"), ...})` yazılır. **Güvenlik uyarısı:** ayırt edici olarak sınıf adını (`Id.CLASS` / `@class`) kullanıp güvenilmeyen girdiyi okumak, saldırganın sunucuda kod çalıştırmasına (uzaktan kod çalıştırma — RCE) yol açar. Bu yüzden mantıksal ad + kapalı bir `@JsonSubTypes` listesi (yalnız izin verilen tipler) tercih edilir. Alt tipleri tek sınıfta birleştirmek ya da `String`'e düşürüp elle ayrıştırmak tip güvenliğini yok eder.
- **Jackson 3 geçişi.** Boot 4'ün varsayılanı Jackson 3'tür. En görünür değişiklik, ana paketin `com.fasterxml.jackson`'dan **`tools.jackson`**'a taşınmasıdır; böylece Jackson 2 ile aynı JVM'de bir arada yaşayabilir. Geçişte `import` satırları, özel `JsonSerializer`/`JsonDeserializer`/`Module` kayıtları ve `ObjectMapper`/`JsonMapper` yapılandırmaları yenilenir. Artık `JsonMapper.builder()` tercih edilir ve bazı varsayılanlar değişmiştir (null ve tip işleme daha katıdır). Önemli bir ayrıntı: taşınan asıl paketler `databind` ve `core` tarafıdır. **Anotasyon paketi `com.fasterxml.jackson.annotation` olarak kalmıştır**, yani `@JsonProperty`/`@JsonIgnore` import'ları aynen sürer. Jackson kaldırılmadı, anotasyonlar duruyor ve `ObjectMapper` hâlâ iş parçacığı güvenlidir. Üçüncü parti kütüphaneler hâlâ Jackson 2'ye bağımlıysa iki sürüm geçici olarak bir arada bulunabilir.

### Tek cümlede

Jackson nesneyi alan alan JSON'a çevirir; adı, gizlemeyi, tarih biçimini ve polimorfik tip bilgisini anotasyon + modül ayarlarıyla sen belirlersin — ve Boot'un yapılandırdığı `ObjectMapper`'ı yeniden yaratmayıp enjekte edersin.

# json-1 | Temel
## Soru
Jackson'da bir Java alanının JSON'daki anahtar adını (örn. `kullaniciAdi` → `user_name`) değiştiren anotasyon hangisidir?
## A
`@JsonIgnore`
🤖 `@JsonIgnore` alanı tamamen dışlar; ad değiştirmez.
## B
`@JsonProperty`
🤖 Doğru: alan ile JSON anahtarı arasındaki ad eşlemesini `@JsonProperty` sağlar.
## C
`@JsonInclude(NON_NULL)`
🤖 `@JsonInclude` hangi alanların (örn. null olmayanların) dâhil edileceğini belirler; ad değiştirmez.
## D
`@Column(name = "user_name")`
🤖 `@Column` JPA/veritabanı eşlemesidir; JSON serileştirmeyle ilgisi yoktur.
## Doğru
B
## Açıklama
**`@JsonProperty("user_name")`**, bir Java alanını (veya getter/setter'ı) JSON'daki farklı bir **anahtar adına** eşler; Java'da `kullaniciAdi` iken JSON'da `user_name` üretilir/okunur. Bu, Java isimlendirme geleneği (camelCase) ile dış API sözleşmesi (snake_case) arasındaki farkı köprüler. **`@JsonIgnore`** (A) alanı tamamen dışlar; **`@JsonInclude`** (C) hangi değerlerin (örn. sadece null-olmayanların) çıktıya gireceğini belirler; **`@Column`** (D) ise JPA'nın veritabanı sütun eşlemesidir, JSON'la ilgisizdir. Not: tüm alanlar için toplu snake_case dönüşümü gerekiyorsa `PropertyNamingStrategies.SNAKE_CASE` daha ölçeklenebilir bir çözümdür.

# json-2 | Temel
## Soru
Bir DTO'daki `parola` alanının JSON çıktısına **hiç** dâhil edilmemesi için hangisi kullanılır?
## A
`@JsonProperty("parola")`
🤖 `@JsonProperty` alanı dâhil eder ve adlandırır; gizlemez.
## B
`@Transient`
🤖 `@Transient` JPA'da kalıcılık dışıdır; JSON serileştirmeyi doğrudan engellemez.
## C
`@JsonInclude(NON_EMPTY)`
🤖 Bu yalnız boş değilse dâhil eder; dolu parola yine sızar, tam gizleme değildir.
## D
`@JsonIgnore`
🤖 Doğru: `@JsonIgnore` alanı serileştirme ve deserileştirmenin dışında bırakır.
## Doğru
D
## Açıklama
**`@JsonIgnore`**, bir alanı hem serileştirmeden hem deserileştirmeden **tamamen dışlar** — parola, iç kimlik gibi hassas/istenmeyen alanlar için doğru araçtır. `@JsonProperty` (A) tersine alanı dâhil eder; `@Transient` (B) JPA'ya "bu alan DB'ye yazılmasın" der ama Jackson'ı ilgilendirmez (JSON'a yine girebilir); `@JsonInclude(NON_EMPTY)` (C) yalnız boş olduğunda atlar, dolu parola sızar. Alternatifler: yalnız yazma/okuma yönünü kısmak için `@JsonProperty(access = WRITE_ONLY)` (parolayı kabul et ama çıktıya yazma) veya sınıf düzeyinde `@JsonIgnoreProperties`. Hassas alan sızıntısı gerçek bir güvenlik açığıdır; API DTO'ları entity'den ayrı tutulur.

# json-3 | Orta
## Soru
Bir `LocalDateTime` alanı `2026-07-24T10:15:30` gibi ISO-8601 metni yerine büyük bir sayı (epoch) olarak serileştiriliyorsa, en olası neden/çözüm nedir?
## A
Jackson `java.time` tiplerini hiç destekleyemez; alan `String` tipine çevrilmelidir
🤖 Jackson `java.time`'ı JavaTimeModule ile destekler; String'e çevirmek gereksiz bir geçici çözümdür.
## B
`LocalDateTime` tipi desteklenmez; alan eski `java.util.Date` tipine çevrilmelidir
🤖 Eski `Date`'e dönmek bir gerileme; sorun modül/ayar, tip değil.
## C
JavaTimeModule kayıtlı ama `WRITE_DATES_AS_TIMESTAMPS` açık kalmış
🤖 Doğru: bu özellik açıkken tarihler epoch sayısı, kapalıyken ISO-8601 metni olur.
## D
JSON standardı tarihleri yalnız sayı (epoch) olarak taşımaya izin verir
🤖 JSON'da tarih tipi yoktur; metin veya sayı olabilir — ISO-8601 metni yaygın ve okunaklı tercihtir.
## Doğru
C
## Açıklama
`java.time` (JSR-310) tiplerinin Jackson'da düzgün işlenmesi için **JavaTimeModule** gerekir (Spring Boot bunu otomatik kaydeder). Modül kayıtlıyken bile çıktı biçimi **`SerializationFeature.WRITE_DATES_AS_TIMESTAMPS`** ayarına bağlıdır: **açıksa** tarihler makine-okunur bir **epoch sayısı** olarak, **kapalıysa** insan-okunur **ISO-8601 metni** (`2026-07-24T10:15:30`) olarak yazılır. Spring Boot varsayılanı bu özelliği kapatır (ISO-8601), ama elle `new ObjectMapper()` yaratıldığında veya ayar ezildiğinde epoch'a düşülebilir. Doğru çözüm tipi değiştirmek değil (B, A), modülü + ayarı doğru yapılandırmaktır. Boot'un otomatik yapılandırdığı `ObjectMapper`'ı enjekte etmek bu tuzaktan korur.

# json-4 | Orta
## Soru
Standart Jackson eşlemesinin yetmediği özel bir JSON biçimi (örn. para birimini `"₺1.250,00"` olarak yazmak) için doğru yaklaşım nedir?
## A
`JsonSerializer<Para>` yazıp `@JsonSerialize(using = ...)` ile alana bağlamak
🤖 Doğru: özel biçim için `JsonSerializer` yazılıp anotasyon/Module ile devreye alınır.
## B
Sınıfın `toString()` metodunu istenen biçimi üretecek şekilde değiştirmek yeterlidir
🤖 Jackson serileştirmede otomatik `toString` kullanmaz; alan alan eşler, özel biçim için serializer gerekir.
## C
JSON çıktısını `StringBuilder` ile elle birleştirip metin olarak döndürmek
🤖 Elle JSON kurmak kırılgan ve hataya açıktır; Jackson'ın serializer mekanizması bunun içindir.
## D
Alanı `@JsonIgnore` ile gizleyip biçimlenmiş metni tutan ayrı bir alan eklemek
🤖 Gizleyip elle alan eklemek dolaylı bir kaçış; temiz çözüm özel serializer'dır.
## Doğru
A
## Açıklama
Standart eşlemenin yetmediği özel biçimler için Jackson'ın uzatma noktası **özel serializer**'dır: `JsonSerializer<T>` (Jackson 3'te `tools.jackson...`) alt sınıfının `serialize(...)` metodunda istenen çıktı yazılır ve alana **`@JsonSerialize(using = ParaSerializer.class)`** ile ya da bir **`Module`** (örn. `SimpleModule`) üzerinden global olarak bağlanır. Simetrik okuma için `JsonDeserializer<T>` yazılır. Bu yaklaşım biçim mantığını **tek, test edilebilir** bir yerde tutar. `toString()`'e güvenmek (B) yanlıştır çünkü Jackson alan-alan eşler; elle JSON kurmak (C) veya `@JsonIgnore` + gölge alan (D) kırılgan geçici çözümlerdir. Özel serializer, alan-adı (`@JsonProperty`) ve gizleme (`@JsonIgnore`) ile karşılanamayan biçim ihtiyaçları içindir.

# json-5 | Orta
## Soru
Bir `List<Odeme>` içinde `KrediKarti` ve `Havale` alt tiplerini JSON'a yazıp **geri okurken** somut tip bilgisinin korunması için ne gerekir?
## A
Hiçbir şey; Jackson somut alt tipi çalışma-zamanında kendiliğinden bilir
🤖 Deserialize'da JSON düz veridir; tip bilgisi gömülmezse Jackson somut alt tipi bilemez.
## B
`@JsonTypeInfo` + `@JsonSubTypes` ile JSON'a tip ayırt edici alan gömmek
🤖 Doğru: polymorphic tipler için ayırt edici alan gömülür; okuma sırasında doğru alt tip seçilir.
## C
Tüm alt tipleri tek bir sınıfta birleştirip bir `tur` alanını elle yönetmek
🤖 Bu polimorfizmi yok eder; tasarımı bozar, çözüm değildir.
## D
Alt tipleri JSON'a `String` olarak yazıp okurken elle ayrıştırmak
🤖 String'e düşürmek tip güvenliğini kaybettirir; doğru çözüm tip bilgisini gömmektir.
## Doğru
B
## Açıklama
Polymorphic (çok-biçimli) serileştirmede sorun **deserialize** yönündedir: JSON düz metindir, `{"tutar":100}` bir `KrediKarti` mı `Havale` mi olduğunu içermez. Çözüm, Jackson'a bir **tip ayırt edici** (type discriminator) gömdürmektir: taban tipe **`@JsonTypeInfo(use = Id.NAME, property = "tip")`** ve **`@JsonSubTypes({@Type(value = KrediKarti.class, name = "kredi"), ...})`** konur; JSON'a `"tip":"kredi"` alanı yazılır ve okuma sırasında doğru somut sınıf seçilir. **Güvenlik uyarısı:** ayırt edici olarak sınıf adı/`@class` (`Id.CLASS`) kullanıp güvenilmeyen girdiyi deserialize etmek **uzaktan kod çalıştırma** (RCE) açığıdır; bu yüzden mantıksal ad + kapalı `@JsonSubTypes` listesi (izinli tipler) tercih edilir.

# json-6 | İleri
## Soru
Spring Boot 4 ile gelen Jackson 3 geçişinde kod tabanında en somut değişiklik nedir?
## A
Jackson tamamen kaldırıldı; Boot 4'te JSON yalnız elle string birleştirilerek üretilir
🤖 Jackson kaldırılmadı; hâlâ varsayılan JSON kütüphanesidir, yalnız ana sürümü 3 oldu.
## B
`ObjectMapper` artık thread-safe değildir, her istekte yeniden yaratılmalıdır
🤖 Yapılandırılmış `ObjectMapper` (Jackson 2 ve 3'te) thread-safe'tir; sürüm bunu değiştirmedi.
## C
Anotasyonlar tamamen kaldırıldı; artık yalnız XML konfigürasyonu var
🤖 `@JsonProperty` gibi anotasyonlar Jackson 3'te de vardır; XML'e geçiş yoktur.
## D
Ana paket `tools.jackson` oldu; import'lar ve özel modül kayıtları güncellenir
🤖 Doğru: Jackson 3'ün ana paketi `tools.jackson`; geçişte import ve özel bileşen kayıtları yenilenir.
## Doğru
D
## Açıklama
Jackson **3.x**'in en görünür değişikliği **paket ad alanının** `com.fasterxml.jackson`'dan **`tools.jackson`**'a taşınmasıdır (Jackson 2 ile aynı JVM'de bir arada yaşayabilsin diye). Spring Boot 4 varsayılan olarak Jackson 3 kullandığından, projedeki **import satırları**, özel **`JsonSerializer`/`JsonDeserializer`/`Module`** kayıtları ve `ObjectMapper`/`JsonMapper` yapılandırmaları yeni pakete göre güncellenir; ayrıca `JsonMapper.builder()` gibi builder-temelli kurulum tercih edilir ve bazı varsayılan davranışlar (daha katı null/tip işleme) değişti. Not: taşınan asıl paketler `databind`/`core` tarafıdır; **anotasyon paketi `com.fasterxml.jackson.annotation` olarak kalmıştır**, yani `@JsonProperty`/`@JsonIgnore` import'ları aynen sürer. Jackson kaldırılmadı (A), `ObjectMapper` hâlâ thread-safe (B), anotasyonlar duruyor (C). Migrasyonda dikkat: üçüncü-parti kütüphaneler hâlâ Jackson 2'ye bağımlıysa iki sürüm geçici olarak bir arada bulunabilir.
