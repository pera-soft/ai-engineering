---
slug: spring-boot-4-yenilikleri
baslik: "Spring Boot 4 / Framework 7: Jakarta 11, Jackson 3, JSpecify ve API Sürümleme"
kademe: İleri
sira: 74
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Telefonunun işletim sistemi büyük bir sürüm atladığında iki tür haber duyarsın: "şu yenilik geldi" ve "şu eski uygulama artık çalışmıyor". Yazılım kütüphanelerinde de aynısı olur. Bazı sürümler yalnız özellik eklemez, altındaki temelleri değiştirir; bunlara **nesil değişimi** denir. Bu konunun amacı, Spring Boot 4'ün neyi getirdiğini ve — daha önemlisi — **neyi kırdığını**, bir önceki neslin kırılımlarıyla karıştırmadan ayırt edebilmektir. Mülakattaki en sık hata, Boot 3'ün kırılımını Boot 4'e yazmaktır.

### Temel kavramlar

- **Sürüm (version) ve LTS:** Yazılımın numaralı yayınıdır. Java'da bazı sürümler **LTS** (Long-Term Support — uzun süreli destek) işaretlidir; kurumlar üretimde genelde LTS kullanır. Java 17, 21 ve 25 LTS'tir.
- **Taban / baseline:** Bir kütüphanenin çalışabildiği **en düşük** Java sürümü. "Önerilen sürüm" ile aynı şey değildir — mülakat tuzağı tam buradadır.
- **Paket (package) / ad alanı (namespace):** Java'da sınıflar `com.sirket.modul` gibi noktalı isim ağaçlarında toplanır. Bir kütüphane paket adını değiştirirse onu kullanan **her `import` satırı** değişmek zorunda kalır; bu yüzden paket adı değişimi tipik bir **kırıcı değişikliktir (breaking change)**.
- **Serileştirme (serialization):** Bellekteki Java nesnesini ağdan gidecek metne (genelde JSON) çevirmek, ters yönde de metni nesneye döndürmek. Spring bunu varsayılan olarak **Jackson** ile yapar; işi yapan sınıf `ObjectMapper`'dır.
- **Null-güvenliği (null-safety):** Java'da bir değişken "hiçbir şey" anlamına gelen `null` tutabilir ve `null` üzerinde işlem `NullPointerException` verir. Kütüphaneler "bu değer null olabilir/olamaz" bilgisini anotasyonla ilan eder ki araçlar hatayı program çalışmadan yakalasın; buna **statik analiz** denir.

**Spring Framework 7.0** ve **Spring Boot 4.0** Kasım 2025'te yayımlandı ve yeni bir **nesil** (generation) başlattı:

- **JDK tabanı:** Minimum **Java 17** korundu; **Java 25** (en yeni LTS) önerilen sürümdür. "Boot 4 Java 25 zorunlu kılar" yaygın bir yanılgıdır. (Java 11 tabanı Boot 2 dönemiydi, Boot 3 ile 17'ye çıkıldı; Java 21 ara LTS'tir ama alt sınır değildir.)
- **Jakarta EE 11:** Servlet 6.1, JPA 3.2, Bean Validation 3.1 tabanı.
- **Jackson 3** varsayılan oldu: paket alanı `com.fasterxml.jackson` → **`tools.jackson`**.
- **JSpecify null-güvenliği:** Spring'in kendi `@Nullable`/`@NonNull`'ı yerine standart JSpecify anotasyonları.
- **API sürümleme (versioning) yerleşik:** sunucuda `@RequestMapping(version = ...)`, istemcide `@HttpExchange`/`RestClient`.
- **Modülerlik + dayanıklılık (resilience):** `@Retryable`, `@ConcurrencyLimit` gibi anotasyonlar çekirdeğe taşındı (eskiden ayrı Spring Retry projesindeydi).

### Nasıl çalışır?

**1) `javax` → `jakarta` göçü hangi geçişti?** Oracle, Java EE'yi Eclipse Foundation'a devretti; marka nedeniyle ad alanı `javax.*`'tan `jakarta.*`'a geçti (Jakarta EE 9). Spring bunu **Framework 6 / Boot 3** ile benimsedi. Yani `javax.persistence.Entity` → `jakarta.persistence.Entity` gibi binlerce import değişimi **Boot 2→3** işiydi; Boot 2 hâlâ `javax` kullanıyordu, Framework 5.3 ise yalnız bir bakım sürümüydü. Boot 4 zaten `jakarta` çizgisindedir ve geriye `javax`'a dönüş **yoktur**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Boot 2->3 javax kırılımı, 3->4 nesil yenilenmesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki farklı geçiş, iki farklı iş</text>
  <rect x="24" y="44" width="140" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="94" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Boot 2</text>
  <text x="94" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">javax.*</text>
  <rect x="250" y="44" width="140" height="66" rx="10" fill="#FBEEE6" stroke="#C0632A" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#C0632A" font-family="system-ui">Boot 3</text>
  <text x="320" y="92" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">jakarta.* (KIRILIM)</text>
  <rect x="476" y="44" width="140" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="546" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Boot 4</text>
  <text x="546" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Jakarta 11 · Jackson 3</text>
  <text x="546" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">JSpecify · API sürüm</text>
  <text x="207" y="82" text-anchor="middle" font-size="20" fill="#C0632A" font-family="system-ui">→</text>
  <text x="433" y="82" text-anchor="middle" font-size="20" fill="#25804C" font-family="system-ui">→</text>
  <text x="320" y="150" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Tab an: Java 17 (min) · Önerilen: Java 25 (LTS)</text>
  <text x="320" y="176" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">"Boot 4 = Java 25 zorunlu" YANLIŞ · "javax→jakarta = Boot 4" YANLIŞ (o Boot 3'tü)</text>
</svg>
```

Özet ayrım: **Boot 3** kırılımı paket adıydı (`javax`→`jakarta`); **Boot 4** ise nesil yenilenmesidir (Jakarta EE 11, Jackson 3, JSpecify, yerleşik API sürümleme, kaldırılan deprecated API'ler) ve Java 17 tabanını korur. Bu iki geçişi karıştırmak migrasyon planında yanlış efor tahminine yol açar.

**2) Jackson 3.** Jackson 3, ana paketini `com.fasterxml.jackson`'dan **`tools.jackson`**'a taşıdı; amaç Jackson 2 ile **aynı JVM'de bir arada yaşayabilmekti**. `ObjectMapper` kaldırılmadı (Jackson 3'te `ObjectMapper`/`JsonMapper` vardır), Spring Gson'a geçmedi ve JSON hâlâ doğrudan üretilir — araya XML gibi bir ara format girmez. Değişenler importlar ve bazı varsayılanlardır: mapper artık değişmez (immutable) kurulur ve `JsonMapper.builder()` ile yapılandırılır.

**3) API sürümleme.** Aynı URL'nin zamanla değişmesi gerekir ama eski istemciler kırılmadan.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yerleşik API sürümleme: aynı endpoint'in v1 ve v2'si istek sürümüne göre ayrışır">
  <defs>
    <marker id="sb4v-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yerleşik API sürümleme (Boot 4)</text>
  <rect x="24" y="70" width="176" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">istek + sürüm bilgisi</text>
  <text x="112" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">başlık / param / medya-tipi</text>
  <rect x="416" y="42" width="200" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="516" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">v1 handler</text>
  <text x="516" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@RequestMapping(version="1")</text>
  <rect x="416" y="106" width="200" height="52" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="516" y="128" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">v2 handler</text>
  <text x="516" y="147" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@RequestMapping(version="2")</text>
  <text x="308" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">aynı yol</text>
  <text x="308" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">/urun</text>
  <line x1="200" y1="92" x2="414" y2="66" stroke="#186399" stroke-width="1.7" marker-end="url(#sb4v-arw)"/>
  <line x1="200" y1="100" x2="414" y2="130" stroke="#186399" stroke-width="1.7" marker-end="url(#sb4v-arw)"/>
  <text x="320" y="184" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">İstemci: @HttpExchange / RestClient sürümü taşır — geriye uyum korunur</text>
</svg>
```

Boot 4 öncesinde bu iş elle yapılırdı: özel `HandlerMapping` yazmak, başlığı kendin ayrıştırmak. Artık çekirdektedir. Sunucuda `@RequestMapping(version = "1.1")` gibi bir öznitelik verilir; aynı yolun farklı sürümleri istek **başlığı**, **parametre** ya da **medya tipi** stratejisiyle ayrışır. İstemci tarafında ise `@HttpExchange` ve `RestClient` sürüm bilgisini taşır. Bu, uygulamanın Maven/Gradle sürüm numarasıyla, veritabanı şema göçüyle (Flyway/Liquibase) veya JVM bytecode sürümüyle ilgili **değildir**; tümüyle HTTP katmanındadır. Değeri: geriye uyumluluğu bozmadan API evrimi — v1 istemcileri çalışırken v2'yi paralel yayına almak.

**4) JSpecify.** Framework 7 çekirdeği `org.jspecify.annotations` anotasyonlarına geçti.

```svg
<svg viewBox="0 0 640 186" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JSpecify: jenerik ve dizi elemanı düzeyinde nullness ifade edebilir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Null-güvenliği: JSpecify'ın farkı</text>
  <rect x="26" y="44" width="278" height="118" rx="11" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Eski Spring @Nullable</text>
  <text x="165" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yalnız alan / parametre / dönüş</text>
  <text x="165" y="116" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">List&lt;String&gt; içi eleman?</text>
  <text x="165" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">ifade EDİLEMEZ</text>
  <rect x="336" y="44" width="278" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">JSpecify (Framework 7)</text>
  <text x="475" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ekosistem çapında standart</text>
  <text x="475" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">jenerik + dizi/vararg elemanı</text>
  <text x="475" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">nullness ifade EDİLİR</text>
</svg>
```

Farkı şudur. Spring'in eski kendi anotasyonları "null olabilir mi" bilgisini (nullness) yalnız alan, parametre ve dönüş düzeyinde ifade edebiliyordu. JSpecify ise bunu **jenerik tip argümanları ve dizi/değişken sayılı parametre (vararg) elemanları** için de ifade eder. Örnek: `List<String>` içindeki elemanın null olup olamayacağı. Ekosistem çapında ortak bir standart olduğu için Kotlin ve NullAway gibi statik analiz araçları Spring API'lerini daha doğru anlar; NPE'ler derleme/analiz zamanında yakalanır. Alternatifler neden değil: Lombok'un `@NonNull`'ı **çalışma zamanında** kontrol üretir, Checker Framework güçlüdür ama seçilen ekosistem standardı o olmadı, `Optional<T>` ise alan/parametre null'ını ifade etmez ve Spring bunu zorunlu kılmadı.

**5) Geçişin (migrasyon) gerçek riski.** Boot 3'ten 4'e geçişte en somut iş Jackson 2'den 3'e geçmektir. Ekosistemdeki bazı üçüncü taraf kütüphaneler hâlâ Jackson 2'ye (`com.fasterxml.jackson`) bağımlı olabilir; bu yüzden iki sürüm geçici olarak bir arada bulunur. Projenin kendi tarafında ise **özel dönüştürücü (serializer) ve `Module` kayıtları ile import satırları** yeni pakete güncellenir.

### Mülakatta ne sorulur?

- **Boot 4'ün minimum JDK'sı?** **Java 17**; Java 25 önerilir, zorunlu değildir — "25 olmazsa derlenmez" yanlıştır.
- **`javax` → `jakarta` hangi geçişteydi?** **Boot 2→3** (Jakarta EE 9, Framework 6). Boot 4 değil, Boot 2 değil, Framework 5.3 hiç değil.
- **Jackson 3'ün en görünür işareti?** Paket alanı `com.fasterxml.jackson` → **`tools.jackson`**.
- **Framework 7'nin null-güvenlik standardı?** **JSpecify** — jenerik ve dizi elemanı düzeyinde nullness.
- **Yerleşik API sürümleme ne sağlar?** Aynı endpoint'in farklı sürümlerini **istek sürüm bilgisine** göre ayrıştırmak.
- **Gerçekçi Boot 3→4 riski?** Jackson 2'ye bağımlı kütüphaneler yüzünden iki sürümün bir arada bulunması. Yanlış cevaplar: `javax`'a geri dönmek, Java 25 zorunluluğu ve "Spring MVC kaldırıldı, WebFlux'a geçin" — **MVC kaldırılmadı**, MVC ve WebFlux ikisi de desteklenir.

### Tek cümlede

Boot 3 paket adını kırdı (`javax`→`jakarta`); Boot 4 ise Java 17 tabanını koruyarak nesli yeniledi: Jakarta EE 11, Jackson 3 (`tools.jackson`), JSpecify null-güvenliği ve çekirdeğe giren API sürümleme.

# boot4-1 | Orta
## Soru
Spring Boot 4.0 için **minimum** JDK tabanı (baseline) nedir?
## A
Java 17; Java 25 önerilir ama zorunlu değildir
🤖 Doğru: Boot 4 tabanı Java 17'de kaldı, Java 25 (en yeni LTS) hedeflenen/önerilen sürümdür — ama alt sınır 17.
## B
Java 25; daha düşük JDK sürümleriyle uygulama hiç derlenmez
🤖 Yaygın yanılgı: 25 önerilendir, taban değil. Boot 4 Java 17 ile de çalışır.
## C
Java 21; Java 17 desteği tamamen kaldırıldı
🤖 21 ara LTS'tir ama Boot 4'ün alt sınırı değil; taban 17 olarak korundu.
## D
Java 11; Spring bu sürüm için hâlâ geniş geriye-uyum sağlar
🤖 Java 11 tabanı Boot 2 dönemiydi; Boot 3 ile 17'ye çıktı, Boot 4 de 17'de.
## Doğru
A
## Açıklama
Spring Framework 7 / Boot 4, **Java 17** tabanını (baseline) korur ve **Java 25**'i (en yeni LTS) önerir. Yani 17 alt sınır, 25 hedeflenen sürümdür. "Boot 4 Java 25 zorunlu kılar" ifadesi mülakatta sık düşülen tuzaktır. Bağlam: Boot 2 tabanı Java 8/11, Boot 3 ile 17'ye çıkıldı; Boot 4 bu 17 tabanını sürdürüp üst uçta 25'i kucaklar. Sürüm politikasını bilmek, üretim ortamında hangi JDK ile deploy edileceğini doğru planlamayı sağlar.

# boot4-2 | Orta
## Soru
`javax.*` → `jakarta.*` paket göçü hangi sürüm geçişinin kırıcı (breaking) değişikliğiydi?
## A
Spring Boot 4'e geçiş (Jakarta EE 11 nedeniyle)
🤖 Boot 4 zaten `jakarta` çizgisindedir; paket adı kırılımı bir önceki (Boot 3) nesilde yaşandı.
## B
Spring Boot 2'ye geçiş
🤖 Boot 2 hâlâ `javax.*` kullanıyordu; kırılım sonraki sürümde (3) geldi.
## C
Spring Framework 5.3 yamasında
🤖 5.3 bir bakım sürümüydü; paket göçü Framework 6 / Boot 3 ile geldi.
## D
Spring Boot 3'e geçiş (Jakarta EE 9+ nedeniyle)
🤖 Doğru: Boot 2→3 geçişi tüm `javax.persistence`/`javax.servlet` importlarını `jakarta.*`'a taşımayı gerektirdi.
## Doğru
D
## Açıklama
Oracle'ın Java EE'yi Eclipse Foundation'a devretmesiyle namespace `javax.*`'tan **`jakarta.*`**'a değişti (Jakarta EE 9). Spring bu değişimi **Framework 6 / Boot 3** ile benimsedi; bu yüzden Boot 2→3 yükseltmesinde `javax.persistence.Entity` → `jakarta.persistence.Entity` gibi binlerce import değişir. **Boot 4** ise zaten `jakarta` çizgisindedir (Jakarta EE 11); onun getirdiği kırılımlar farklıdır (Jackson 3, kaldırılan deprecated API'ler). Bu iki geçişi karıştırmak, migrasyon planında yanlış efor tahminine yol açar.

# boot4-3 | İleri
## Soru
Spring Boot 4'te Jackson **3** varsayılan oldu. Bu geçişin en görünür teknik işareti nedir?
## A
JSON artık XML tabanlı bir ara formata çevrilir
🤖 Böyle bir şey yok; Jackson 3 hâlâ doğrudan JSON üretir/okur, ara XML katmanı yoktur.
## B
`ObjectMapper` kaldırıldı; yerine yalnız Gson gelir
🤖 Jackson 3'te de `ObjectMapper`/`JsonMapper` vardır; Spring Gson'a geçmedi.
## C
Paket alanı `com.fasterxml.jackson`'dan `tools.jackson`'a değişti
🤖 Doğru: Jackson 3'ün ana paketi `tools.jackson`; importlar ve bazı API'ler buna göre güncellenir.
## D
Jackson tamamen çekirdekten çıkarıldı; JSON serileştirme desteği kalmadı
🤖 Aksine, Jackson hâlâ varsayılan JSON kütüphanesidir; yalnız ana sürümü 3 oldu.
## Doğru
C
## Açıklama
Jackson **3.x**, paket ad alanını `com.fasterxml.jackson`'dan **`tools.jackson`**'a taşıdı (Jackson 2 ile aynı JVM'de bir arada yaşayabilsin diye). Spring Boot 4 varsayılan olarak Jackson 3 kullanır; bu yüzden özel serializer/`Module` kayıtları ve import satırları yeni pakete göre güncellenir. Bazı varsayılanlar da değişti: mapper artık değişmez (immutable) kurulur ve `JsonMapper.builder()` ile yapılandırılır. Migrasyonda, üçüncü-parti kütüphanelerin hâlâ Jackson 2'ye bağımlı olabileceği ve iki sürümün geçici olarak bir arada bulunabileceği unutulmamalıdır.

# boot4-4 | İleri
## Soru
Spring Framework 7'nin null-güvenliği (null-safety) için Spring'in kendi `@Nullable`/`@NonNull` anotasyonları yerine benimsediği **standart** nedir?
## A
Lombok'un `@NonNull`'ı
🤖 Lombok runtime null-kontrolü üretir; Framework 7'nin benimsediği statik-analiz standardı bu değil.
## B
Checker Framework'ün özel tip niteleyicileri
🤖 Checker güçlüdür ama Spring'in seçtiği ekosistem standardı o değil; JSpecify seçildi.
## C
`Optional<T>`'yi her alanda zorunlu kılmak
🤖 `Optional` alan/parametre null'ını ifade etmez; ayrıca Spring bunu zorunlu kılmadı.
## D
JSpecify null-güvenlik anotasyonları
🤖 Doğru: Framework 7 çekirdeği JSpecify'a geçti; jenerik ve dizi elemanı düzeyinde nullness ifade edilir.
## Doğru
D
## Açıklama
Spring Framework 7, kod tabanını **JSpecify** (`org.jspecify.annotations`) null-güvenlik anotasyonlarına taşıdı. JSpecify, ekosistem çapında ortak bir standarttır ve **jenerik tip argümanları ile dizi/vararg elemanlarının** nullness'ını da ifade edebilir — Spring'in eski kendi anotasyonlarının yapamadığı bir incelik. Bu, Kotlin ve statik analiz araçlarının (NullAway vb.) Spring API'lerini daha doğru anlamasını sağlar. Senior açısından önemi: yeni kodun sözleşmelerini (contract) araçların denetleyebilmesi, NPE'leri derleme/analiz zamanında yakalamaktır.

# boot4-5 | Uzman
## Soru
Spring Boot 4'ün **yerleşik API sürümleme** (API versioning) desteği ne sağlar?
## A
Uygulamanın Maven/Gradle sürüm numarasını çalışma-zamanında otomatik yükseltmek
🤖 API sürümleme yapı/derleme sürümüyle ilgili değil; HTTP endpoint sürümlerini ayrıştırmakla ilgilidir.
## B
Veritabanı şema göçlerini (Flyway/Liquibase) sürüm sırasına göre uygulamak
🤖 Şema göçü ayrı bir konudur; Boot 4'ün API sürümlemesi HTTP katmanındadır.
## C
Aynı endpoint'in farklı sürümlerini istek sürüm bilgisine göre ayrıştırmak
🤖 Doğru: sunucuda `@RequestMapping(version=...)`, istemcide `@HttpExchange`/`RestClient` ile sürüm eşleme çekirdeğe girdi.
## D
JVM bytecode sürümünü her endpoint için ayrı ayrı değiştirmek
🤖 Bytecode sürümü tüm modül için tektir; endpoint başına değişemez, konuyla ilgisizdir.
## Doğru
C
## Açıklama
Boot 4 / Framework 7, **HTTP API sürümlemesini** çekirdeğe getirdi: sunucu tarafında `@RequestMapping(version = "1.1")` gibi bir öznitelikle ve istek başlığı/parametre/medya-tipi stratejisiyle aynı yolun farklı sürümleri ayrıştırılır; istemci tarafında `@HttpExchange` ve `RestClient` sürüm bilgisini taşır. Bu, önceden elle (özel `HandlerMapping`, başlık ayrıştırma) yapılan işi standartlaştırır. Senior için değeri: geriye-uyumluluğu (backward compatibility) bozmadan API evrimi — v1 istemcileri çalışırken v2'yi paralel yayına almak.

# boot4-6 | Uzman
## Soru
Bir ekip Boot 3'ten Boot 4'e geçiyor. Aşağıdakilerden hangisi **gerçekçi** bir migrasyon riski/işidir?
## A
Tüm `jakarta.*` importlarını tekrar `javax.*`'a çevirmek gerekir
🤖 Ters yön yanlış: Boot 4 `jakarta` çizgisindedir; geri `javax`'a dönüş yoktur.
## B
Jackson 2'ye bağımlı üçüncü-parti kütüphaneler yüzünden iki sürüm bir arada bulunabilir
🤖 Doğru: Jackson 3 (`tools.jackson`) geçişi karışık bağımlılık ve özel serializer/modül güncellemesi gerektirir.
## C
Java 25'e zorunlu geçiş yapılmazsa uygulama hiç derlenmez ve hiç ayağa kalkmaz
🤖 Boot 4 tabanı Java 17'dir; 25 önerilir ama derlemeyi engellemez.
## D
Spring MVC kaldırıldığı için tüm controller'lar WebFlux'a ve reaktif tiplere taşınmalıdır
🤖 Spring MVC kaldırılmadı; MVC ve WebFlux ikisi de Boot 4'te desteklenir.
## Doğru
B
## Açıklama
Gerçek Boot 3→4 migrasyon işlerinden biri **Jackson 2→3** geçişidir: Boot 4 varsayılanı Jackson 3'tür (`tools.jackson`), ama ekosistemdeki bazı kütüphaneler hâlâ Jackson 2'ye (`com.fasterxml.jackson`) bağımlı olabilir. Bu durumda iki sürüm geçici olarak bir arada bulunur ve projenin **özel serializer/`Module` kayıtları ile import satırları** yeni pakete güncellenir. Diğer şıklar tipik yanılgılardır: `javax`'a geri dönüş yoktur (A), Java 25 zorunlu değildir (C), Spring MVC kaldırılmamıştır (D). Doğru migrasyon planı bu ayrımlara dayanır.
