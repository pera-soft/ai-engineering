---
slug: spring-mvc-rest
baslik: "Spring MVC & REST: DispatcherServlet, Binding, Idempotency ve Durum Kodları"
kademe: Orta
sira: 76
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Büyük bir otelin resepsiyonunu düşün: gelen herkes önce tek bir bankoya uğrar, resepsiyonist kimin nereye gideceğine karar verir ve yönlendirir. Web uygulamalarında bu bankonun adı **DispatcherServlet**'tir. Spring MVC gelen her isteği bu tek merkezden geçirip senin yazdığın doğru metoda ulaştırır, dönen sonucu da istemcinin anlayacağı biçime çevirir. Sen yalnız "şu adrese şu metot baksın" dersin; gerisini çatı yapar.

### Temel kavramlar

- **İstemci (client) / sunucu (server):** İstemci veriyi isteyen taraftır (tarayıcı, mobil uygulama, başka bir servis); sunucu sürekli açık durup gelen istekleri karşılayan ve yanıt üreten programdır. Senin Spring Boot uygulaman bir sunucudur.
- **HTTP isteği (request):** İstemcinin sunucuya gönderdiği, kurallı biçimde yazılmış metin mesajı. Dört parçası vardır: **metot** (`GET`, `POST`, `PUT`, `DELETE` — niyet), **yol** (`/urun/42?detay=true` — hedef), **başlıklar (headers)** (`Accept`, `Content-Type` gibi ek bilgiler) ve isteğe bağlı **gövde (body)** (genelde JSON veri). Yanıt da aynı yapıdadır ve bir **durum kodu** taşır (200, 404 …).
- **Servlet:** Java'nın "gelen HTTP isteğini alıp yanıt üreten bileşen" standardı; Tomcat gibi sunucular servlet çalıştırır. Spring MVC tek bir servlet kullanır — **DispatcherServlet**. Tüm trafik önce ondan geçtiği için buna **front controller** (önyüz denetleyici) deseni denir.
- **Controller:** Belirli bir yolu karşılayan metotları barındıran sınıfın; `@GetMapping("/urun/{id}")` gibi anotasyonlarla hangi isteğe baktığı yazılır.
- **`HttpMessageConverter`:** Java nesnesi ↔ JSON/XML çevirisini yapan dönüştürücü.
- **`ViewResolver`:** HTML sayfası döndüren klasik uygulamalarda, metodun döndürdüğü view adını gerçek şablona (JSP/Thymeleaf) çözüp render eden bileşen.
- **REST:** HTTP'yi olduğu gibi kullanan API yaklaşımı: her şey bir **kaynaktır** (`/urun/42`), ne yapılacağını **metot** söyler, sonucu **durum kodu** anlatır.

### Nasıl çalışır?

Zincir şudur: istek → **`HandlerMapping`** (bu yola hangi metot bakıyor?) → **`HandlerAdapter`** (metodu çağırır) → argüman çözümleme (`@PathVariable`/`@RequestParam`/`@RequestBody` değerlerini metot parametrelerine doldurur) → metot çalışır → dönüş değerinin işlenmesi → yanıt.

Son adımda **iki yol** vardır. `@ResponseBody`/`@RestController` varsa dönüş değeri bir `HttpMessageConverter` ile **doğrudan gövdeye** yazılır. Klasik `@Controller` bir görünüm (view) adı döndürüyorsa **`ViewResolver`** devreye girer ve şablonu HTML'e dönüştürür.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="DispatcherServlet istek akışı">
  <defs>
    <marker id="mvc-ok" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir istek DispatcherServlet'ten nasıl geçer</text>
  <rect x="20" y="70" width="110" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="75" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">HTTP isteği</text>
  <text x="75" y="108" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">GET /kullanici/7</text>
  <rect x="160" y="70" width="120" height="50" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="220" y="90" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">HandlerMapping</text>
  <text x="220" y="106" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hangi metot?</text>
  <rect x="310" y="70" width="120" height="50" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="370" y="90" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">HandlerAdapter</text>
  <text x="370" y="106" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">arg. çözümle + çağır</text>
  <rect x="460" y="70" width="120" height="50" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="520" y="90" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">MessageConverter</text>
  <text x="520" y="106" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">nesne → JSON</text>
  <line x1="130" y1="95" x2="158" y2="95" stroke="#186399" stroke-width="1.6" marker-end="url(#mvc-ok)"/>
  <line x1="280" y1="95" x2="308" y2="95" stroke="#186399" stroke-width="1.6" marker-end="url(#mvc-ok)"/>
  <line x1="430" y1="95" x2="458" y2="95" stroke="#186399" stroke-width="1.6" marker-end="url(#mvc-ok)"/>
  <text x="320" y="160" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">@RestController = @Controller + @ResponseBody (view yok, gövdeye yaz)</text>
  <text x="320" y="184" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Idempotent: GET · PUT · DELETE  |  Değil: POST</text>
</svg>
```

`@RestController` = `@Controller` + `@ResponseBody`. `@Controller` sınıfı bir web bileşeni yapar. `@ResponseBody` ise dönüş değerinin doğrudan yanıt gövdesine (tipik olarak JSON'a) yazılacağını, **görünüm çözümlenmeyeceğini** söyler.

Klasik `@Controller`'da her metoda ayrı `@ResponseBody` yazman gerekir; `@RestController` bunu sınıf düzeyinde bir kez sağlar. HTML döndüren geleneksel MVC uygulamalarında ise `@ResponseBody`'siz `@Controller` kullanılır. Bunu başka anotasyon birleşimleriyle karıştırma: `@Service` + `@Transactional`, `@Component` + `@Configuration` ya da `@RequestMapping` + `@Bean` — hiçbiri gövdeye yazma davranışını vermez.

**Veri nereden alınır?** Bunu URL'nin yapısı belirler.

```svg
<svg viewBox="0 0 640 196" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İstek verisi kaynakları: PathVariable, RequestParam, RequestBody">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Veri nereden alınır? Bağlama anotasyonları</text>
  <text x="320" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">POST /urun/42?detay=true  { "ad": "..." }</text>
  <rect x="20" y="72" width="196" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="96" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">@PathVariable</text>
  <text x="118" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yol parçası</text>
  <text x="118" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">/urun/{id} → 42</text>
  <rect x="222" y="72" width="196" height="98" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="96" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">@RequestParam</text>
  <text x="320" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sorgu / form</text>
  <text x="320" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">?detay= → true</text>
  <rect x="424" y="72" width="196" height="98" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="522" y="96" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">@RequestBody</text>
  <text x="522" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">gövde (JSON)</text>
  <text x="522" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">JSON → nesne</text>
</svg>
```

```java
@RestController
@RequestMapping("/urun")
class UrunController {

    @GetMapping("/{id}")                              // GET /urun/42?detay=true
    Urun getir(@PathVariable Long id,                 // yol parçası → 42
               @RequestParam boolean detay) { ... }   // sorgu parametresi → true

    @PostMapping
    ResponseEntity<Urun> yarat(@RequestBody UrunDto dto) {   // gövdedeki JSON → nesne
        Urun k = servis.kaydet(dto);
        return ResponseEntity.created(URI.create("/urun/" + k.getId())).body(k);  // 201 + Location
    }
}
```

`@PathVariable` **yol parçasını** (`/urun/{id}` içindeki `42`), `@RequestParam` **sorgu veya form parametresini** (`?detay=true`), `@RequestBody` **istek gövdesindeki** JSON'u bir nesneye, `@RequestHeader` ise bir HTTP başlığını bağlar. GET isteklerinde tipik olarak gövde yoktur; bu yüzden orada `@RequestBody` kullanılmaz. Tasarım kuralı basittir: kaynağı tanımlayan zorunlu değer **yola**, isteğe bağlı filtre ve sayfalama **sorguya** konur.

`ResponseEntity<T>` yanıtın **durum kodunu, başlıklarını ve gövdesini** açıkça belirlemeni sağlar. Doğrudan `return kullanici;` dersen Spring gövdeyi JSON'a çevirir, ama durum kodu hep `200 OK` olur ve başlık ekleyemezsin. `ResponseEntity` ile aynı metot duruma göre `200 OK` (bulundu), `404 Not Found` (yok) veya `201 Created` + `Location` (yaratıldı) döndürebilir. Gövde yine aynı `HttpMessageConverter`'larla çevrilir. Bunun işlem (transaction) yönetimiyle ya da asenkronlukla (`CompletableFuture`/`Mono`/`Callable`) hiçbir ilgisi yoktur.

**İçerik anlaşması (content negotiation):** Sunucu, yanıtı hangi formatta üreteceğine istemcinin **`Accept`** başlığına bakarak karar verir ve ona uygun dönüştürücüyü seçer. `Accept: application/json` → JSON, `Accept: application/xml` → XML (uygun dönüştürücü classpath'teyse). Seçim rastgele değildir ve verinin geldiği veritabanı sütun tipiyle ilgisi yoktur.

Bunu **`Content-Type`** ile karıştırma. `Content-Type` **gönderilen gövdenin** formatını bildirir; yani `@RequestBody`'nin nasıl çözüleceğini belirler. URL uzantısına ya da `format` sorgu parametresine bakan stratejiler de yapılandırılabilir, ama varsayılan da önerilen de değildir. Bu ayrım sayesinde aynı uç nokta (endpoint) farklı istemcilere farklı format sunabilir.

**Idempotency (tekrar edilse de sonucu değişmeme):** Aynı isteği **N kez** göndermek, **1 kez** göndermekle **aynı sunucu durumunu** bırakıyorsa o metot idempotent'tir.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Idempotency: GET PUT DELETE tekrarı aynı durumu bırakır, POST bırakmaz">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Idempotency: N kez = 1 kez mi?</text>
  <rect x="30" y="46" width="270" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Idempotent</text>
  <text x="165" y="96" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">GET · PUT · DELETE</text>
  <text x="165" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">tekrar aynı durumu bırakır</text>
  <text x="165" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ağ hatasında güvenli retry</text>
  <rect x="336" y="46" width="270" height="118" rx="11" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="471" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#8A2E2E" font-family="system-ui">Idempotent DEĞİL</text>
  <text x="471" y="96" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">POST</text>
  <text x="471" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her çağrı yeni kaynak/etki</text>
  <text x="471" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">2 kez → 2 sipariş</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Not: idempotent ≠ yan etkisiz — DELETE durumu değiştirir ama tekrarı aynı</text>
</svg>
```

Üç metot idempotenttir. **GET** okur, durumu hiç değiştirmez. **PUT** tam değiştirmedir; aynı gövdeyle tekrarı hep aynı hedef duruma götürür. **DELETE**'te ilk çağrı kaynağı siler, sonraki çağrılar zaten yok olanı silmeye çalışır. **POST** ise idempotent değildir: aynı siparişi iki kez göndermek iki sipariş yaratır.

Kritik nüans şu: idempotent olmak "yan etkisiz" demek **değildir**. DELETE sunucu durumunu değiştirir, ama tekrarı aynı sonucu bırakır. Bu ayrım, ağ hatasında güvenli **yeniden deneme (retry)** tasarımının temelidir.

**Durum kodları:** `200 OK`, `201 Created` (kaynak yaratıldı), `204 No Content` (gövdesiz; güncelleme/silme için), `400 Bad Request`, `404 Not Found`, `409 Conflict`, `422 Unprocessable Entity`, `500 Internal Server Error`.

Pratik kural: yaratma → `POST` + `201` + `Location`; tam güncelleme → `PUT`; silme → `DELETE`; bulunamadı → `404`; geçersiz gövde → `400`/`422`. Yaratmada `200 OK` teknik olarak çalışır ama kaynağın nerede olduğunu bildirmez. `204` yeni kaynağı döndüremez. `302 Found` ise tarayıcı yönlendirmesidir, REST yaratma yanıtı değildir.

### Araya girme noktaları: Filter mi Interceptor mı?

İsteğin yolu üzerinde iki ayrı "araya girme" noktası vardır; mülakatta farkları sık sorulur.

**Filter** (servlet filtresi) Java servlet standardına aittir ve **DispatcherServlet'ten önce** çalışır. Ham `HttpServletRequest`/`HttpServletResponse` üzerinde durur; hangi controller metodunun çalışacağını **bilmez**. Spring Security'nin tüm zinciri filtrelerden oluşur, çünkü kimlik doğrulama isteğin Spring MVC'ye girmesinden önce yapılmalıdır.

**Interceptor** (`HandlerInterceptor`) ise Spring MVC'nin **içindedir**. DispatcherServlet isteği karşılayacak metodu (handler) seçtikten sonra devreye girer ve o metodu görebilir. Üç kancası vardır: `preHandle` (controller'dan önce; `false` dönerse istek orada durur), `postHandle` (controller çalıştı, görünüm henüz üretilmedi) ve `afterCompletion` (yanıt tamamlandı; istisna olsa bile çalışır, temizlik için doğru yer).

Sıralama tek cümlede şudur: **Filter → DispatcherServlet → Interceptor → Controller**, dönüşte ters yönde.

Seçim kuralı da buradan çıkar. İsteğin tamamını ilgilendiren ve Spring'e girmeden yapılması gereken işler filtreye yazılır: güvenlik, tarayıcı kaynak paylaşımı kuralları (CORS), sıkıştırma, ham gövde okuma. Hangi metodun çağrıldığını bilmeyi gerektiren işler ise interceptor'a yazılır: metot bazlı yetki kontrolü, süre ölçümü, isteği izleme numarası (`correlation-id`) yerleştirme, `ThreadLocal` temizliği.

Üçüncü bir alternatif de **AOP**'dur. AOP HTTP katmanına değil, herhangi bir bean metoduna uygulanır (`72`). Üçü arasında seçim yaparken tek soru yeter: "bu iş HTTP isteğine mi bağlı, yoksa bir servis metoduna mı?" Ayrıca `afterCompletion`'ın istisna durumunda da çalıştığını bilmek, `ThreadLocal` sızıntısı sorularında (`55`) doğrudan işe yarar.

### Mülakatta ne sorulur?

- **DispatcherServlet akışı?** `HandlerMapping` → `HandlerAdapter` → controller metodu → `HttpMessageConverter` (REST) **veya** `ViewResolver` (HTML).
- **Filter ile Interceptor farkı?** Filter servlet seviyesindedir ve DispatcherServlet'ten **önce** çalışır, handler'ı bilmez (Spring Security); Interceptor Spring MVC içindedir, handler'a erişir, `preHandle`/`postHandle`/`afterCompletion` kancaları vardır. Sıra: Filter → DispatcherServlet → Interceptor → Controller.
- **`@RestController` neye eşit?** `@Controller` + `@ResponseBody`.
- **`/urun/42?detay=true` nasıl bağlanır?** `42` → `@PathVariable`, `true` → `@RequestParam`.
- **Yaratma yanıtı?** `201 Created` + `Location` (`ResponseEntity.created(uri).body(...)`).
- **`ResponseEntity`'nin faydası?** Durum kodu + başlık + gövdeyi açıkça kontrol etmek.
- **Yanıt formatını ne belirler?** İsteğin `Accept` başlığı; `Content-Type` gövdenin formatıdır.
- **Hangi metotlar idempotent?** GET, PUT, DELETE; POST değil — ve idempotent ≠ yan etkisiz.

### Tek cümlede

Spring MVC her isteği DispatcherServlet'ten geçirip doğru controller metoduna bağlar; senin işin doğru HTTP metodunu, doğru bağlama anotasyonunu ve doğru durum kodunu seçmektir.

# mvc-1 | Temel
## Soru
`@RestController` anotasyonu neye eşittir?
## A
`@Service` + `@Transactional`
🤖 `@RestController` web katmanı içindir; servis/işlem yönetimiyle ilgisi yoktur.
## B
`@Component` + `@Configuration`
🤖 Bunlar bean tanımı/konfigürasyonla ilgilidir; controller'ın gövde-yazma davranışını vermez.
## C
`@Controller` + `@ResponseBody`
🤖 Doğru: her metot dönüşü doğrudan yanıt gövdesine (genelde JSON'a) yazılır, view çözümlenmez.
## D
`@RequestMapping` + `@Bean`
🤖 `@RequestMapping` eşlemedir, `@Bean` fabrika metodudur; `@RestController`'ın kısayolu bu değil.
## Doğru
C
## Açıklama
`@RestController`, `@Controller` ve `@ResponseBody`'nin birleşimidir. `@Controller` sınıfı bir web bileşeni yapar; `@ResponseBody` ise metot dönüş değerinin **doğrudan HTTP yanıt gövdesine** (bir `HttpMessageConverter` ile, tipik olarak JSON'a) yazılacağını, bir **view** (JSP/Thymeleaf) çözümlenmeyeceğini söyler. Klasik `@Controller`'da her metoda ayrı `@ResponseBody` gerekirken, `@RestController` bunu sınıf düzeyinde bir kez sağlar. REST API'leri için standart tercihtir; HTML sayfası döndüren geleneksel MVC uygulamalarında ise `@ResponseBody`'siz `@Controller` kullanılır.

# mvc-2 | Temel
## Soru
`GET /urun/42?detay=true` isteğinde `42` ve `true` değerlerini yakalamak için doğru anotasyonlar hangileridir?
## A
İkisi de `@RequestBody`
🤖 `@RequestBody` istek gövdesi içindir; GET'te gövde yoktur, yol/sorgu parçalarını okumaz.
## B
İkisi de `@RequestParam`
🤖 `42` yol parçasıdır (`/urun/{id}`), `@RequestParam` değil `@PathVariable` ile alınır.
## C
`42` için `@RequestHeader`, `true` için `@PathVariable`
🤖 İkisi de ters; `42` yol değişkeni, `true` sorgu parametresidir, başlık değil.
## D
`42` için `@PathVariable`, `true` için `@RequestParam`
🤖 Doğru: `{id}` yol parçası → `@PathVariable`; `?detay=` sorgu parametresi → `@RequestParam`.
## Doğru
D
## Açıklama
URL yapısı bağlamayı belirler: `/urun/{id}` içindeki **yol parçası** (`42`) **`@PathVariable`** ile, `?detay=true` içindeki **sorgu parametresi** (`true`) **`@RequestParam`** ile alınır. `@RequestBody` yalnız istek **gövdesindeki** (genelde POST/PUT'ta JSON) veriyi bir nesneye bağlar; GET isteklerinde tipik olarak gövde yoktur. `@RequestHeader` ise HTTP başlıklarını okur. Bu ayrımı bilmek, endpoint imzasını doğru tasarlamanın temelidir: kaynağı tanımlayan zorunlu değer yola (`@PathVariable`), isteğe bağlı filtre/sayfalama ise sorguya (`@RequestParam`) konur.

# mvc-3 | Orta
## Soru
Bir kaynağı başarıyla **yaratan** bir `POST` endpoint'i için en doğru yanıt hangisidir?
## A
`201 Created` ve `Location` başlığı
🤖 Doğru: yaratmada standart yanıt 201'dir ve `Location` başlığı yeni kaynağın URI'sini verir.
## B
`200 OK`; gövde boş bırakılır ve hiçbir başlık eklenmez
🤖 200 kabul edilebilir olsa da yaratma için ideal değil; kaynağın nerede olduğunu (Location) bildirmez.
## C
Her durumda `204 No Content`
🤖 204 "gövde yok" demektir; yeni kaynağı ve adresini döndürmesi gereken yaratma için uygun değil.
## D
`302 Found` ile istemciyi ana sayfaya yönlendirmek
🤖 302 tarayıcı yönlendirmesidir; REST kaynak yaratma yanıtı değildir.
## Doğru
A
## Açıklama
REST semantiğinde kaynak **yaratma** için doğru yanıt **`201 Created`**'tır ve yanıt **`Location`** başlığında yeni kaynağın URI'si (`/kullanici/7`) döndürülür; gövdede genelde yaratılan kaynağın temsili yer alır. Spring'te bu `ResponseEntity.created(uri).body(kaynak)` ile ifade edilir. `200 OK` (B) teknik olarak çalışır ama "nerede yaratıldı" bilgisini iletmez; `204` (C) gövdesiz yanıttır, güncelleme/silme için uygundur; `302` (D) tarayıcı yönlendirmesidir, API için yanlıştır. Doğru durum kodu + `Location`, istemcinin yeni kaynağa ek istek olmadan erişmesini sağlar.

# mvc-4 | Orta
## Soru
HTTP metotlarının **idempotency** (aynı isteği tekrarlamanın sunucu durumunu değiştirmemesi) özelliği için hangisi doğrudur?
## A
POST, PUT, DELETE hepsi idempotenttir
🤖 POST idempotent değildir; her çağrı yeni kaynak/etki yaratabilir.
## B
GET, PUT ve DELETE idempotenttir; POST değildir
🤖 Doğru: aynı PUT/DELETE'i tekrarlamak aynı durumu bırakır; POST her tekrarında yeni etki üretebilir.
## C
Yalnız GET idempotenttir; PUT ve DELETE değildir
🤖 PUT ve DELETE de idempotenttir: tekrarları sunucu durumunu değiştirmez.
## D
Idempotency yalnız "yan etkisi olmayan" metotları kapsar; DELETE dışta kalır
🤖 Idempotency ≠ yan etkisiz; DELETE durumu değiştirir ama tekrarı aynı sonucu bıraktığından idempotenttir.
## Doğru
B
## Açıklama
**Idempotency**: aynı isteği **N kez** göndermek, **1 kez** göndermekle **aynı sunucu durumunu** bırakır. **GET** (okuma, durumu değiştirmez), **PUT** (tam değiştirme — aynı gövdeyle tekrarı aynı hedef duruma götürür) ve **DELETE** (silme — ilkinde kaynak gider, tekrarlar zaten yok olanı silmeye çalışır, durum değişmez) idempotenttir. **POST** değildir: her çağrı yeni bir kaynak/işlem yaratabilir (aynı siparişi iki kez göndermek iki sipariş). Kritik nüans (D): idempotency "yan etki yok" demek **değildir** — DELETE durumu değiştirir ama tekrarı aynı sonucu verir. Bu, ağ hatasında güvenli **yeniden deneme** (retry) tasarımının temelidir.

# mvc-5 | Orta
## Soru
`ResponseEntity<T>` kullanmanın, doğrudan nesne döndürmeye (`return kullanici;`) göre asıl faydası nedir?
## A
Otomatik olarak veritabanı işlemini (transaction) yönetir
🤖 İşlem yönetimi `@Transactional`/servis katmanının işidir; `ResponseEntity` bunu yapmaz.
## B
JSON serileştirmeyi devre dışı bırakır
🤖 Aksine, `ResponseEntity` gövdesi de aynı message converter'larla serileştirilir.
## C
Durum kodunu, başlıkları ve gövdeyi açıkça belirlemeyi sağlar
🤖 Doğru: `ResponseEntity` ile 201/404/`Location` gibi ayrıntılar duruma göre programatik olarak belirlenir.
## D
Metodu asenkron (non-blocking) hâle getirip isteği ayrı bir iş parçacığında çalıştırır
🤖 Asenkronluk `CompletableFuture`/`Mono`/`Callable` ile ilgilidir; `ResponseEntity` senkron/asenkrondan bağımsızdır.
## Doğru
C
## Açıklama
`ResponseEntity<T>`, yanıtın **durum kodunu, başlıklarını ve gövdesini** programatik olarak belirlemeyi sağlar. Doğrudan `return kullanici;` dediğinizde Spring gövdeyi serileştirir ama durum kodu varsayılan `200 OK` olur ve başlıkları elle set edemezsiniz. `ResponseEntity` ile aynı metot duruma göre `200 OK` (bulundu), `404 Not Found` (yok), `201 Created` + `Location` (yaratıldı) döndürebilir. İçerik yine aynı `HttpMessageConverter`'larla serileştirilir (B yanlış), işlem yönetimiyle (A) veya asenkronlukla (D) ilgisi yoktur. Doğru durum kodu iletmek REST sözleşmesinin bir gereğidir; `ResponseEntity` bunun aracıdır.

# mvc-6 | İleri
## Soru
İçerik anlaşması (content negotiation) sayesinde sunucu yanıt formatını (JSON/XML) tipik olarak neye göre seçer?
## A
İstemcinin gönderdiği `Accept` başlığına göre
🤖 Doğru: standart strateji, `Accept` başlığındaki tercih edilen medya tipine (ör. `application/xml`) göre uygun converter'ı seçmektir.
## B
Sunucunun her istekte rastgele belirlediği bir formata göre
🤖 Seçim rastgele değildir; istemci tercihine (`Accept`) ve yapılandırılmış stratejiye dayanır.
## C
Her zaman isteğin `Content-Type` başlığında bildirdiği gövde tipine göre
🤖 `Content-Type` isteğin gövde formatını anlatır; yanıt formatını asıl belirleyen `Accept` başlığıdır.
## D
Veriyi taşıyan veritabanı sütununun tipine göre
🤖 Yanıt formatı veri modeliyle değil, HTTP içerik anlaşması mekanizmasıyla belirlenir.
## Doğru
A
## Açıklama
**İçerik anlaşması** (content negotiation), sunucunun istemcinin **`Accept`** başlığındaki tercih edilen medya tipine göre uygun `HttpMessageConverter`'ı seçmesidir: `Accept: application/json` → JSON, `Accept: application/xml` → XML (uygun converter/kütüphane sınıf yolundaysa). Spring bunu varsayılan olarak `Accept` başlığı üzerinden yapar; ek olarak URL uzantısı veya `format` sorgu parametresi stratejileri de yapılandırılabilir ama bunlar varsayılan/önerilen değildir. **`Content-Type`** (C) ise isteğin **gövdesinin** formatını anlatır — sunucunun `@RequestBody`'yi nasıl çözeceğini belirler, yanıt formatını değil. Bu ayrım, aynı endpoint'in farklı istemcilere (tarayıcı, mobil, entegrasyon) farklı format sunmasını sağlar.
