---
slug: exception-handling-web
baslik: "Web Hata Yönetimi: @ExceptionHandler, @ControllerAdvice ve ProblemDetail (RFC 9457)"
kademe: Orta
sira: 78
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir lokantada mutfakta tencere devrilirse garson masaya gelip "üç numaralı ocağın vanası patladı" demez; "bu yemeği çıkaramıyoruz, sipariş numaranız 47" der. Ayrıntı içeride kalır, müşteriye kısa ve anlaşılır bir bilgi gider. Sunucu programlarında da aynı ayrım gerekir: bir şey ters gittiğinde karşı tarafa **ne olduğu**, **kimin sorunu olduğu** ve **ne yapabileceği** hep aynı kalıpla bildirilmelidir. Aksi hâlde her hata başka bir biçimde döner ve karşıdaki program hiçbirini anlayamaz. Bu konu, hataların tek merkezden ve standart bir gövdeyle döndürülmesini anlatır.

### Temel kavramlar

- **Web servisi (REST API):** Ekran değil, **program** için veri döndüren sunucu. İstek gelir, yanıt gider; yanıtın gövdesi genelde JSON'dur.
- **JSON:** Veriyi metin olarak yazma biçimi — `{"ad": "Deniz", "yas": 20}`. Programlar arası ortak dildir.
- **İstisna (exception):** Program çalışırken ortaya çıkan, akışı kesen hata nesnesi. Kod bunu "fırlatır" (throw); yakalanmazsa (catch) istek çöker.
- **HTTP durum kodu (status code):** Yanıtın başındaki üç haneli sayı. `2xx` başarı, `4xx` isteği yapan hatalı, `5xx` sunucu hatalı demektir.
- **Denetleyici (controller):** Gelen HTTP isteğini karşılayan sınıf.
- **Anotasyon (annotation):** `@` ile başlayan etiket; sınıfa veya metoda "seni şöyle işle" der. Spring bunları okuyup davranışı kurar.
- **`@ExceptionHandler`:** Bir metoda konur; "şu istisna tipi fırlarsa yanıtı ben üretirim" anlamına gelir. Bir controller'ın **içine** yazılırsa yalnız o controller'ı kapsar → **yerel** (local).
- **`@ControllerAdvice` / `@RestControllerAdvice`:** Ayrı bir sınıfa konur; içindeki `@ExceptionHandler` metotları **tüm** controller'lar için geçerlidir → **global**. `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`, yani dönüş değeri doğrudan yanıt gövdesine (JSON) yazılır.
- **`ProblemDetail`:** Hata gövdesi için **standart JSON kalıbı** (RFC 7807; güncel hâli **RFC 9457**): `type` (hatayı tanımlayan URI), `title` (kısa başlık), `status` (HTTP kodu), `detail` (insana açıklama), `instance` (bu olaya özgü URI) ve isteğe bağlı **özel genişletmeler** (örn. bir `errors` listesi). Medya tipi `application/problem+json`. Spring 6 / Boot 3'ten beri çekirdekte yerleşiktir (`org.springframework.http.ProblemDetail`).
- **`ResponseEntityExceptionHandler`:** Spring MVC'nin kendi istisnalarını (`MethodArgumentNotValidException`, `HttpMessageNotReadableException` gibi) hazır ele alan temel sınıf.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yerel ve global istisna yakalama, ProblemDetail çıktısı">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İstisna nerede yakalanır?</text>
  <rect x="30" y="40" width="250" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="155" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Yerel: @ExceptionHandler</text>
  <text x="155" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek controller içinde</text>
  <text x="155" y="101" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">daha yakın → önceliklidir</text>
  <rect x="360" y="40" width="250" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="485" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Global: @RestControllerAdvice</text>
  <text x="485" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tüm controller'lar için</text>
  <text x="485" y="101" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek merkez, DRY</text>
  <rect x="130" y="130" width="380" height="70" rx="10" fill="#FBEEE6" stroke="#C0632A" stroke-width="1.5"/>
  <text x="320" y="154" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C0632A" font-family="system-ui">ProblemDetail (RFC 9457)</text>
  <text x="320" y="174" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">type · title · status · detail · instance</text>
  <text x="320" y="190" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">application/problem+json · Spring 6+ yerleşik</text>
</svg>
```

### Nasıl çalışır?

```java
@RestControllerAdvice
class GlobalHataYonetimi extends ResponseEntityExceptionHandler {

  @ExceptionHandler(KullaniciBulunamadiException.class)
  ProblemDetail bulunamadi(KullaniciBulunamadiException ex) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setTitle("Kullanıcı bulunamadı");
    return pd;                          // gövde: application/problem+json
  }

  @ExceptionHandler(Exception.class)    // en genel ağ: yakalanmayan her şey
  ProblemDetail beklenmeyen(Exception ex) {
    log.error("izleme=ab12cd", ex);      // AYRINTI SUNUCUDA KALIR
    var pd = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu");
    pd.setProperty("traceId", "ab12cd"); // özel genişletme
    return pd;
  }
}
```

Akış üç adımdır. (1) Controller metodu bir istisna fırlatır. (2) Spring o tipi ele alacak bir işleyici metot (handler) arar; **önce aynı controller içinde**, bulamazsa global advice sınıfında. (3) İşleyicinin döndürdüğü `ProblemDetail` JSON'a çevrilir ve `status` alanı yanıtın HTTP koduna yazılır.

Sınıfı `ResponseEntityExceptionHandler`'dan türetirsen Spring'in kendi istisnaları da aynı `ProblemDetail` kalıbına döner; bozuk JSON gövdesi ve doğrulama hatası gibi. Sen yalnız kendi iş istisnalarını eklersin.

İşleyici seçiminde iki kural işler. Birincisi **yakınlıktır**. Aynı istisna tipi için hem yerel `@ExceptionHandler` hem global advice varsa **yerel kazanır**. "Özel durumu yerelde ele al, gerisini merkeze bırak" deseni bundan doğar.

İkincisi **özgüllüktür**. Spring istisna hiyerarşisine bakıp en dar eşleşmeyi seçer. `IllegalArgumentException` için özel bir işleyici yoksa `RuntimeException`/`Exception` yakalayan genel işleyici devreye girer. "Neden benim advice'ım çalışmıyor?" sorusunun cevabı hemen her zaman bu iki kuraldır.

İşin diğer yarısı **durum kodu eşlemesidir**: istisnayı anlamına uygun HTTP koduna bağlamak. Kodu vermenin üç yolu var. İstisna sınıfına `@ResponseStatus(HttpStatus.NOT_FOUND)` koyabilirsin. `throw new ResponseStatusException(HttpStatus.NOT_FOUND, ...)` fırlatabilirsin. Ya da işleyicide `ProblemDetail`'in `status` alanını doldurabilirsin.

Yanlış eşlemelerin bedeli ağırdır. Bilinen bir "yok" durumunu `500` yapmak gerçek arızaları gürültüde kaybettirir. `200 OK` + boş gövdeyle geçiştirmek REST sözleşmesini bozar, çünkü istemci başarı sanır. `400` isteğin **biçimsel** geçersizliğidir — bozuk JSON, eksik alan. Kaynağın yokluğu ise `400` değil `404`'tür.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İstisna türünü doğru HTTP durum koduna eşleme tablosu">
  <defs>
    <marker id="exweb-map-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İstisnayı doğru HTTP durum koduna eşle</text>
  <rect x="40" y="42" width="300" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="190" y="62" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Kaynak bulunamadı</text>
  <line x1="345" y1="57" x2="425" y2="57" stroke="#5D7789" stroke-width="1.4" marker-end="url(#exweb-map-ar)"/>
  <rect x="430" y="42" width="170" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="515" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">404 Not Found</text>
  <rect x="40" y="80" width="300" height="30" rx="6" fill="#FBEEDC" stroke="#5D7789" stroke-width="1"/>
  <text x="190" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Geçersiz girdi / doğrulama</text>
  <line x1="345" y1="95" x2="425" y2="95" stroke="#5D7789" stroke-width="1.4" marker-end="url(#exweb-map-ar)"/>
  <rect x="430" y="80" width="170" height="30" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="515" y="100" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">400 / 422</text>
  <rect x="40" y="118" width="300" height="30" rx="6" fill="#F3EEF9" stroke="#5D7789" stroke-width="1"/>
  <text x="190" y="138" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Yetki yok</text>
  <line x1="345" y1="133" x2="425" y2="133" stroke="#5D7789" stroke-width="1.4" marker-end="url(#exweb-map-ar)"/>
  <rect x="430" y="118" width="170" height="30" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="515" y="138" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">403 Forbidden</text>
  <rect x="40" y="156" width="300" height="30" rx="6" fill="#E9F4EE" stroke="#5D7789" stroke-width="1"/>
  <text x="190" y="176" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Kaynak çakışması</text>
  <line x1="345" y1="171" x2="425" y2="171" stroke="#5D7789" stroke-width="1.4" marker-end="url(#exweb-map-ar)"/>
  <rect x="430" y="156" width="170" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="515" y="176" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">409 Conflict</text>
  <rect x="40" y="194" width="300" height="30" rx="6" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="190" y="214" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Beklenmeyen / yakalanmamış</text>
  <line x1="345" y1="209" x2="425" y2="209" stroke="#5D7789" stroke-width="1.4" marker-end="url(#exweb-map-ar)"/>
  <rect x="430" y="194" width="170" height="30" rx="6" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="515" y="214" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">500 Server Error</text>
  <text x="320" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@ResponseStatus · ResponseStatusException · ProblemDetail(status=...)</text>
</svg>
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ayrıntı sunucu logunda kalır, istemciye jenerik ProblemDetail döner">
  <defs>
    <marker id="exweb-leak-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ayrıntı içeride, istemciye jenerik</text>
  <rect x="24" y="44" width="270" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="159" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Sunucu logu (içeride)</text>
  <text x="159" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">stack trace + sınıf/paket yolu</text>
  <text x="159" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">SQL / tablo adı</text>
  <text x="159" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">istisna metni</text>
  <text x="159" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="monospace">traceId = ab12cd</text>
  <text x="159" y="176" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">dışarı SIZMAZ</text>
  <line x1="296" y1="150" x2="358" y2="150" stroke="#25804C" stroke-width="1.8" marker-end="url(#exweb-leak-ar)"/>
  <text x="327" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yalnız güvenli gövde</text>
  <rect x="362" y="44" width="254" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="489" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci yanıtı (dışarıda)</text>
  <text x="489" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="monospace">ProblemDetail status=500</text>
  <text x="489" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"Beklenmeyen bir hata oluştu"</text>
  <text x="489" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">teknik ayrıntı YOK</text>
  <text x="489" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="monospace">traceId = ab12cd</text>
  <text x="489" y="176" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">destek için korelasyon</text>
</svg>
```

### Mülakatta ne sorulur?

- **Kapsam farkı.** Controller içindeki `@ExceptionHandler` yalnız o sınıfı kapsar (yerel). `@ControllerAdvice` ise tüm controller'lara uygulanan global bir bileşendir; hata yönetimini tek merkezde toplar, böylece aynı kodu tekrar tekrar yazmazsın (DRY). `@RestControllerAdvice`, `@ControllerAdvice` + `@ResponseBody`'dir.
- **Öncelik.** Aynı tip için ikisi de varsa **yerel işleyici kazanır**. İkisi zincirlenip sırayla çalışmaz; seçim de rastgele değildir.
- **ProblemDetail nedir.** Bir loglama şeması ya da yeniden deneme (retry) mekanizması değildir. **Yanıt gövdesinin standart yapısıdır**: `type`/`title`/`status`/`detail`/`instance` + genişletmeler, medya tipi `application/problem+json`. SOAP fault zarfı gibi XML tabanlı değildir. Standart olması sayesinde mobil uygulama ya da entegrasyon gibi farklı istemciler hataları **tutarlı** biçimde ayrıştırır; her ekip kendi rastgele hata JSON'unu icat etmez.
- **Hangi sürümden beri yerleşik.** **Spring Framework 6 / Spring Boot 3** (2022). Boot 4'e özgü değildir ve Spring 4.3'te yoktu. Artık Zalando Problem gibi üçüncü parti bir kütüphane de gerekmez; çekirdek yeterlidir.
- **Doğru kod eşlemesi.** Bulunamadı → `404`, geçersiz girdi → `400`/`422`, yetki yok → `403`, çakışma → `409`, beklenmeyen → `500`.
- **Bilgi sızıntısı.** Ham hata yığını (stack trace), istisna metni, SQL ya da tablo adı istemciye **dönmez**. Bunlar saldırgana kullandığın teknolojiler, sınıf/paket yolları ve veritabanı yapısı hakkında ipucu verir. Doğru yaklaşım şudur: ayrıntıyı sunucuda **logla**; istemciye `status=500`, güvenli bir `title`/`detail` ("Beklenmeyen bir hata oluştu") ve destek ekibinin kaydı bulmasını sağlayan bir **izleme kimliği** (trace/correlation id) taşıyan genel bir `ProblemDetail` döndür.

### Tek cümlede

Hataları tek merkezde (`@RestControllerAdvice`) yakala, her istisnayı doğru HTTP durum koduna eşle ve istemciye ayrıntı sızdırmayan standart bir `ProblemDetail` gövdesi döndür.

# exweb-1 | Temel
## Soru
`@ControllerAdvice` (veya `@RestControllerAdvice`) ile `@ExceptionHandler`'ı bir controller içinde tek başına kullanmak arasındaki temel fark nedir?
## A
`@ControllerAdvice` globaldir; controller içindeki `@ExceptionHandler` yalnız o controller'a yereldir
🤖 Doğru: advice global merkezdir; controller içi handler yalnız o sınıfı kapsar.
## B
`@ControllerAdvice` yalnız `500` hatalarını yakalar; diğer durum kodları için ayrı bir mekanizma gerekir
🤖 Advice her istisna tipini ele alabilir; belirli bir duruma kilitli değildir.
## C
`@ExceptionHandler` yalnız `@ControllerAdvice` sınıfı içinde çalışır; controller'ın kendi içine yazılırsa yok sayılır
🤖 `@ExceptionHandler` controller içinde de çalışır; advice'a bağımlı değildir.
## D
İkisi de aynı işi yapar; aradaki tek fark isimlendirmedir, kapsamları özdeştir
🤖 Fark kapsamdır: biri global, diğeri tek controller yereli.
## Doğru
A
## Açıklama
Kapsam farkıdır. Bir controller'ın içine konan **`@ExceptionHandler`** yalnız **o controller'daki** istisnaları yakalar (yerel). **`@ControllerAdvice`** (veya REST için `@RestControllerAdvice`) ise `@ExceptionHandler` metotlarını **tüm controller'lara** uygulayan **global** bir bileşendir — hata yönetimini tek merkezde toplar (DRY). `@RestControllerAdvice`, `@ControllerAdvice` + `@ResponseBody`'dir; dönüş değerini doğrudan gövdeye (JSON) yazar. Tipik mimari: ortak hataları global advice'ta ele al, yalnızca bir controller'a özgü nadir bir durumu o controller'ın yerel handler'ında yönet.

# exweb-2 | Temel
## Soru
RFC 9457 (eski RFC 7807) `ProblemDetail`, HTTP hata yanıtları için ne sağlar?
## A
Hataları veritabanına loglamak için kullanılan hazır bir tablo şeması ve DAO katmanı
🤖 ProblemDetail loglama şeması değil; istemciye dönen standart hata gövdesi formatıdır.
## B
İstisna fırlatan istekleri otomatik olarak yeniden deneyen (retry) bir mekanizma
🤖 Retry farklı bir konudur; ProblemDetail yanıt gövdesinin standart yapısını tanımlar.
## C
Hata yanıtları için standart JSON gövdesi: `type`, `title`, `status`, `detail`
🤖 Doğru: ProblemDetail bu standart alanlarla makine-okunur, tutarlı hata gövdesi verir.
## D
XML tabanlı SOAP hata zarfı (fault envelope) biçimi; REST API'lerde de aynen kullanılır
🤖 ProblemDetail JSON (`application/problem+json`) tabanlıdır; SOAP zarfı değildir.
## Doğru
C
## Açıklama
**ProblemDetail** (RFC 7807, güncel hâli **RFC 9457**), HTTP API hataları için **standart, makine-okunur bir gövde formatıdır**: `type` (hatayı tanımlayan URI), `title` (kısa özet), `status` (HTTP kodu), `detail` (insana yönelik açıklama), `instance` (bu olaya özgü URI) alanları ve isteğe bağlı **özel genişletmeler** (örn. `errors` listesi). Medya tipi **`application/problem+json`**'dır. Standart olması, farklı istemcilerin (mobil, entegrasyon) hataları **tutarlı** şekilde ayrıştırmasını sağlar — her ekibin kendi rastgele hata JSON'unu icat etmesini önler. Spring bunu yerleşik olarak destekler.

# exweb-3 | Orta
## Soru
Spring'te `ProblemDetail` desteği hangi sürümden beri **yerleşiktir** (ek kütüphane gerekmez)?
## A
Yalnız Spring Boot 4 ile geldi, öncesinde yoktu
🤖 ProblemDetail Boot 4'ten önce, Spring 6 / Boot 3 ile geldi; Boot 4'e özgü değil.
## B
Spring Framework 6 / Spring Boot 3'ten beri
🤖 Doğru: yerleşik ProblemDetail ve RFC 7807 desteği Spring Framework 6 / Boot 3 ile geldi.
## C
Hiç yerleşik değildir; her zaman üçüncü-parti kütüphane gerekir
🤖 Aksine, Spring 6+ yerleşik `ProblemDetail` tipi ve `ResponseEntityExceptionHandler` entegrasyonu sunar.
## D
Spring 4.3'ten beri vardır; XML tabanlı hata yapılandırmasının yerini almıştır
🤖 4.3 çok eski; ProblemDetail o dönemde yoktu, Spring 6 ile geldi.
## Doğru
B
## Açıklama
Yerleşik **`ProblemDetail`** (`org.springframework.http.ProblemDetail`) ve RFC 7807 desteği **Spring Framework 6 / Spring Boot 3** ile geldi (2022). Bu, öncesinde Zalando Problem gibi üçüncü-parti kütüphanelerle yapılan işi çekirdeğe taşıdı: `ResponseEntityExceptionHandler` yerleşik Spring MVC istisnalarını otomatik olarak `ProblemDetail`'e çevirebilir ve `@ExceptionHandler` metotları doğrudan `ProblemDetail` döndürebilir. Boot 4'te de (RFC 9457'nin güncellemeleriyle) desteklenir ama **Boot 4'e özgü değildir** (A yanlış). Senior için önemi: standart hata formatına geçmek için artık ek bağımlılık gerekmez, çekirdek yeterlidir.

# exweb-4 | Orta
## Soru
Aynı istisna tipi için hem controller içinde yerel bir `@ExceptionHandler` hem de global `@ControllerAdvice`'ta bir handler varsa, hangisi çalışır?
## A
Global `@ControllerAdvice` her zaman kazanır; yerel handler yok sayılır
🤖 Tersine: daha yakın (controller-yereli) handler önceliklidir, global değil.
## B
Önce yerel handler, ardından global advice olmak üzere ikisi de sırayla çalışır
🤖 Bir istisna tek handler ile ele alınır; ikisi zincirlenmez.
## C
Uygulama açılışında rastgele biri seçilir; davranış çalıştırmadan kestirilemez
🤖 Seçim rastgele değil; en yakın (yerel) handler deterministik olarak seçilir.
## D
Controller içindeki yerel `@ExceptionHandler` kazanır
🤖 Doğru: yerel handler global advice'a göre daha spesifik/yakındır ve öncelik alır.
## Doğru
D
## Açıklama
Spring, bir istisna için handler ararken **en yakın/en spesifik** olanı seçer: aynı tip için **controller içindeki yerel `@ExceptionHandler`**, global `@ControllerAdvice`'takine göre önceliklidir. Yalnız yerel handler yoksa global advice devreye girer. Bu, "özel durumları yerelde ele al, geri kalan her şeyi global merkeze bırak" desenini mümkün kılar. Ayrıca handler seçimi istisna tipi hiyerarşisine de bakar: `IllegalArgumentException` için özel handler yoksa `RuntimeException`/`Exception` yakalayan daha genel handler devreye girer (en spesifik eşleşme önce). Bu öncelik kuralını bilmek, "neden benim advice'ım çalışmıyor?" hatasını çözer.

# exweb-5 | Orta
## Soru
Bir kaynak bulunamadığında (`KullaniciBulunamadiException`) istemciye dönmesi gereken doğru HTTP durum kodu ve yaklaşım hangisidir?
## A
`404 Not Found` — istisnayı bu duruma açıkça eşle
🤖 Doğru: kaynağın yokluğu 404'tür; istisnayı bu koda açıkça eşlemek gerekir.
## B
`200 OK` ile boş bir gövde döndürmek; istemci boşluğu kendisi yorumlar
🤖 200 "başarılı" demektir; bulunamayan kaynağı 200 ile bildirmek REST semantiğini bozar.
## C
Her durumda `500 Internal Server Error`; kaynak yoksa bu bir sunucu hatasıdır
🤖 500 beklenmeyen sunucu hatasıdır; "yok" bilinen bir durumdur, 404 ile ifade edilir.
## D
`400 Bad Request`, çünkü istemci kaynaklı tüm hatalar tek bir kodda toplanır
🤖 400 geçersiz istek biçimidir; kaynağın yokluğu için doğru kod 404'tür.
## Doğru
A
## Açıklama
Kaynağın **yokluğu** için doğru kod **`404 Not Found`**'dur. İstisnayı bu koda eşlemenin yolları: istisna sınıfına **`@ResponseStatus(HttpStatus.NOT_FOUND)`** koymak, `throw new ResponseStatusException(HttpStatus.NOT_FOUND, ...)` fırlatmak, ya da bir `@ExceptionHandler`'da **`ProblemDetail`** (`status=404`) döndürmek. **500** (C) beklenmeyen/yakalanmamış sunucu hataları içindir — bilinen "yok" durumu 500 olmamalı (aksi hâlde gerçek arızalar gürültüde kaybolur). **400** (D) isteğin **biçimsel** geçersizliğidir (bozuk JSON, eksik alan); kaynağın yokluğu değildir. Doğru durum kodu eşlemesi, istemcinin hatayı doğru yorumlaması (yeniden dene? kullanıcıya "bulunamadı" göster?) için kritiktir.

# exweb-6 | İleri
## Soru
Global bir `@RestControllerAdvice`'ta beklenmeyen istisnaları ele alırken güvenlik açısından hangisi doğrudur?
## A
Hata ayıklamayı kolaylaştırmak için ham stack trace'i her zaman yanıt gövdesine koy
🤖 Ham stack trace bilgi sızıntısıdır (sınıf/yol/kütüphane sürümleri); istemciye dönmemeli.
## B
Yakalanmayan her istisnayı `200 OK` yanıtına çevirip gövdede boş bir nesne döndür
🤖 Hatayı 200 ile maskelemek REST semantiğini bozar ve istemciyi yanıltır.
## C
Ayrıntıyı sunucuda logla, istemciye jenerik bir `ProblemDetail` döndür
🤖 Doğru: ayrıntı sunucu logunda kalır; istemci sızıntısız, tutarlı, izlenebilir bir yanıt alır.
## D
İstisna mesajını SQL ve tablo adları dâhil olduğu gibi `detail` alanına yaz
🤖 Ham istisna metni (SQL, tablo adı, iç yol) sızabilir; `detail`e kontrolsüz istisna metni konmamalı.
## Doğru
C
## Açıklama
Beklenmeyen istisnalarda doğru yaklaşım **bilgi sızıntısını önlemektir**: ayrıntılı hata (stack trace, istisna mesajı, SQL) **sunucu logunda** kalmalı (izleme/korelasyon kimliğiyle), istemciye ise **kontrollü, jenerik bir `ProblemDetail`** dönmeli — `status=500`, güvenli bir `title/detail` ("Beklenmeyen bir hata oluştu") ve istemcinin destek için verebileceği bir **izleme kimliği** (trace/correlation id). Ham stack trace veya istisna metnini döndürmek (A, D) saldırgana teknoloji yığını, sınıf/paket yolları, veritabanı yapısı gibi ipuçları verir. Hatayı `200`'e çevirmek (B) ise istemciyi yanıltır ve REST sözleşmesini bozar. Bu, "hataları içeride ayrıntılı, dışarıda kontrollü ele al" ilkesidir.
