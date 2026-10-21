---
slug: cors-preflight-tarayici-siniri
baslik: "CORS ve Preflight: Same-Origin Politikası, OPTIONS Turu ve Neden Sunucunun İşi"
kademe: Orta
sira: 210
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir apartmanda oturduğunu düşün. Kendi dairenin içinde istediğin dolabı açarsın, kimse sormaz. Ama komşunun dairesinden bir şey almak istersen kapıda durursun: komşu "sen kimsin, ne alacaksın?" diye sorar ve **ancak izin verirse** içeri girersin. Tarayıcı da tam böyle çalışır: bir web sayfasında çalışan JavaScript, **kendi geldiği adresten** veri okumakta serbesttir; **başka bir adresten** veri okumak istediğinde ise o adresin açıkça izin vermesi gerekir. Bu izin verme protokolünün adı **CORS**'tur.

Backend geliştirici olarak bu konu senin masana şöyle düşer: ön yüzcü gelir, "senin API'n CORS hatası veriyor, düzeltir misin?" der. Ve haklıdır — çünkü izni **veren taraf sunucudur**, tarayıcı yalnızca kuralı uygular. Bu yüzden CORS, ön yüz konusu gibi görünse de çözümü neredeyse her zaman backend tarafındadır. Mülakatta bu blokta en yüksek olasılıkla sorulacak konu budur.

### Temel kavramlar

- **Köken (origin)**: Bir adresin **şema + alan adı + port** üçlüsü. Yol (path) ve sorgu dizesi kökene dâhil değildir.
- **Aynı köken (same-origin)**: İki adresin bu üç parçasının da birebir aynı olması.
- **Same-Origin Politikası (SOP)**: Tarayıcının varsayılan güvenlik kuralı; bir sayfanın JavaScript'inin başka kökenden gelen **yanıtı okumasını** engeller.
- **CORS (Cross-Origin Resource Sharing — köken ötesi kaynak paylaşımı)**: Sunucunun, belirli kökenlere "benim yanıtımı okuyabilirsin" diyebildiği başlık tabanlı protokol.
- **Basit istek (simple request)**: Preflight gerektirmeyen istek türü; GET/HEAD/POST + izinli `Content-Type` + özel başlık yok.
- **Preflight (ön uçuş)**: Tarayıcının asıl istekten önce attığı, izin soran `OPTIONS` turu.
- **`Origin` başlığı**: İsteği yapan sayfanın kökenini bildiren, tarayıcının otomatik koyduğu istek başlığı.
- **Kimlik bilgisi (credentials)**: Çerez (cookie), `Authorization` başlığı ve TLS istemci sertifikası gibi kimliği taşıyan veriler.
- **Joker (wildcard) köken**: `Access-Control-Allow-Origin: *` — "her kökene açığım" anlamına gelen değer.
- **`Vary: Origin`**: "Bu yanıt isteğin `Origin` değerine göre değişir" diyen, ara önbelleklerin yanlış yanıt servis etmesini önleyen başlık.
- **Ters vekil (reverse proxy)**: Ön yüz ile API'yi tek adres altında toplayan ara sunucu (ör. nginx); köken tekleşince CORS hiç devreye girmez.
- **CSRF (cross-site request forgery)**: Kullanıcının oturumuyla istemediği bir **yazma** işleminin tetiklenmesi; CORS'un çözdüğü sorun bu **değildir**.

### Nasıl çalışır?

Her şey **köken** tanımıyla başlar. Tarayıcı iki adresi ancak şema, alan adı ve port'un **üçü de** aynıysa aynı köken sayar. `https://a.com` ile `http://a.com` farklı kökendir (şema), `a.com` ile `api.a.com` farklı kökendir (alan adı), `a.com:8080` ile `a.com:443` farklı kökendir (port). Buna karşılık aynı sunucudaki farklı bir **yol** kökeni değiştirmez.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Köken üçlüsü: şema, alan adı ve port aynıysa aynı köken">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Köken (origin) = şema + alan adı + port</text>
  <rect x="24" y="32" width="592" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="36" y="53" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Taban sayfa: https://app.ornek.com  (şema=https, alan=app.ornek.com, port=443)</text>
  <rect x="24" y="78" width="380" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.3"/>
  <text x="36" y="98" font-size="11" fill="#1C3247" font-family="system-ui">https://app.ornek.com/urunler</text>
  <rect x="414" y="78" width="202" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="515" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">AYNI köken (yol sayılmaz)</text>
  <rect x="24" y="114" width="380" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.3"/>
  <text x="36" y="134" font-size="11" fill="#1C3247" font-family="system-ui">http://app.ornek.com</text>
  <rect x="414" y="114" width="202" height="30" rx="7" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.4"/>
  <text x="515" y="134" text-anchor="middle" font-size="11" font-weight="700" fill="#B3261E" font-family="system-ui">FARKLI: şema</text>
  <rect x="24" y="150" width="380" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.3"/>
  <text x="36" y="170" font-size="11" fill="#1C3247" font-family="system-ui">https://api.ornek.com</text>
  <rect x="414" y="150" width="202" height="30" rx="7" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.4"/>
  <text x="515" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#B3261E" font-family="system-ui">FARKLI: alan adı</text>
  <rect x="24" y="186" width="380" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.3"/>
  <text x="36" y="206" font-size="11" fill="#1C3247" font-family="system-ui">https://app.ornek.com:8080</text>
  <rect x="414" y="186" width="202" height="30" rx="7" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.4"/>
  <text x="515" y="206" text-anchor="middle" font-size="11" font-weight="700" fill="#B3261E" font-family="system-ui">FARKLI: port</text>
  <text x="320" y="232" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Üçünden biri farklıysa köken farklıdır; SOP devreye girer.</text>
</svg>
```

**Same-Origin Politikası (SOP)** tarayıcının varsayılan davranışıdır ve bir amacı vardır: kötü niyetli bir sitenin, sen o siteyi açıkken senin bankandaki **oturum çerezinle** isteğin atıp **yanıtı okumasını** engellemek. Yani korunan şey, senin kimliğinle elde edilen verinin başka bir siteye sızmasıdır.

Burada mülakatta ayırt edici olan nüans şudur: **CORS isteği engellemez, yanıtı okumayı engeller.** Basit bir istekte tarayıcı isteği sunucuya gönderir. Sunucu onu **normal biçimde işler** — hatta veri tabanına yazar — ve yanıtı üretir. Tarayıcı yanıtta uygun izin başlığını görmezse yalnızca **JavaScript'in yanıta erişmesini** keser.

Bu yüzden iki sonuç çıkar. Birincisi, **CORS bir güvenlik duvarı değildir**; sunucu tarafındaki kimlik doğrulama ve yetkilendirmenin yerini **tutmaz**. İkincisi, `curl` veya Postman'de CORS diye bir şey yoktur; kural tamamen **tarayıcıya** aittir ve sunucu bir şey engellemez. Konsoldaki CORS hatasını üreten sunucu değil tarayıcıdır; sunucu 200 dönmüş olabilir.

Peki tarayıcı hangi istekte önce izin sorar? Ayrım **basit istek** kavramıyla yapılır. Bir istek şu üç koşulu birden sağlıyorsa basittir: metot `GET`, `HEAD` veya `POST`; `Content-Type` yalnızca `text/plain`, `application/x-www-form-urlencoded` veya `multipart/form-data` değerlerinden biri; ve özel (custom) istek başlığı yok. Backend'çinin en sık çarptığı yer tam burasıdır: **`application/json` gönderdiğin an istek basit olmaktan çıkar** ve preflight tetiklenir. Aynı şekilde `Authorization` veya `X-Trace-Id` gibi bir başlık eklemek de basitliği bozar.

Basit olmayan isteklerde tarayıcı, asıl istekten **önce** bir **`OPTIONS`** turu atar. Bu tura preflight (ön uçuş) denir ve tarayıcı bunda `Origin`, `Access-Control-Request-Method` ve `Access-Control-Request-Headers` başlıklarını gönderir; yani "şu kökenden, şu metotla, şu başlıklarla geleceğim, izin var mı?" diye sorar. Sunucu `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods` ve `Access-Control-Allow-Headers` ile yanıtlar. `Access-Control-Max-Age` verirsen bu izin tarayıcıda **önbelleklenir** ve her istekte iki tur atılmaz.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Basit istekte preflight yok, json veya özel başlıkta OPTIONS turu var">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Preflight ne zaman atılır?</text>
  <rect x="20" y="34" width="286" height="204" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="163" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">BASİT İSTEK: preflight YOK</text>
  <text x="163" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">metot: GET / HEAD / POST</text>
  <text x="163" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Content-Type: text/plain,</text>
  <text x="163" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">form-urlencoded, multipart</text>
  <text x="163" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">özel başlık yok</text>
  <rect x="44" y="146" width="238" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.2"/>
  <text x="163" y="166" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek doğrudan sunucuya gider</text>
  <text x="163" y="196" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Sunucu isteği İŞLER.</text>
  <text x="163" y="214" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Allow-Origin yoksa yalnız</text>
  <text x="163" y="230" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">JS yanıtı OKUYAMAZ</text>
  <rect x="334" y="34" width="286" height="204" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="477" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#B67A1A" font-family="system-ui">application/json / özel başlık</text>
  <rect x="356" y="68" width="242" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="477" y="83" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">1) Tarayıcı → OPTIONS</text>
  <text x="477" y="97" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Origin + Request-Method/-Headers</text>
  <text x="477" y="116" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">↓</text>
  <rect x="356" y="122" width="242" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="477" y="137" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">2) Sunucu → izin başlıkları</text>
  <text x="477" y="151" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Allow-Origin / -Methods / -Headers</text>
  <text x="477" y="170" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">↓</text>
  <rect x="356" y="176" width="242" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="477" y="191" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">3) İzin var → ASIL istek gider</text>
  <text x="477" y="205" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">izin yoksa asıl istek hiç atılmaz</text>
  <text x="477" y="228" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Max-Age: izin tarayıcıda önbelleklenir</text>
</svg>
```

Sıradaki katman **kimlik bilgisi** taşıyan isteklerdir. Tarayıcı, köken ötesi isteklerde çerezi ve `Authorization` başlığını **varsayılan olarak göndermez**; ön yüzün `fetch(..., { credentials: 'include' })` demesi gerekir. Sunucu tarafında da `Access-Control-Allow-Credentials: true` şarttır. İşte tam bir mülakat sorusu burada: kimlik bilgisi taşıyan bir istekte **`Access-Control-Allow-Origin: *` artık geçersizdir**. Joker kabul edilmez; izin verilen köken **birebir** yazılmalıdır. Bunun yanına `Vary: Origin` konur, çünkü sunucu yanıtı isteğin `Origin` değerine göre değiştiriyorsa, ara önbelleklerin bir kökene ait yanıtı başka kökene servis etmesi (önbellek zehirlenmesi — cache poisoning) engellenmelidir.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kimlik bilgisi taşıyan istekte joker köken geçersizdir, köken birebir yazılmalıdır">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">credentials: 'include' → joker köken çalışmaz</text>
  <rect x="20" y="34" width="290" height="168" rx="10" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.5"/>
  <text x="165" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#B3261E" font-family="system-ui">YANLIŞ kurulum</text>
  <text x="165" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Allow-Origin: *</text>
  <text x="165" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Allow-Credentials: true</text>
  <text x="165" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(istek: credentials: 'include')</text>
  <rect x="44" y="132" width="242" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.2"/>
  <text x="165" y="152" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">tarayıcı yanıtı ENGELLER ✗</text>
  <text x="165" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">joker + kimlik bilgisi</text>
  <text x="165" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">birlikte kullanılamaz</text>
  <rect x="330" y="34" width="290" height="168" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">DOĞRU kurulum</text>
  <text x="475" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Allow-Origin: https://app.ornek.com</text>
  <text x="475" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Allow-Credentials: true</text>
  <text x="475" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Vary: Origin</text>
  <rect x="354" y="132" width="242" height="30" rx="7" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.2"/>
  <text x="475" y="152" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">çerez gider, JS yanıtı okur ✓</text>
  <text x="475" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">köken birebir yazılır;</text>
  <text x="475" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Vary önbellek karışmasını önler</text>
</svg>
```

Bir ayrıntı daha var ve gözden kaçar: **yanıt başlığı okuma**. Köken ötesi bir yanıtta JavaScript varsayılan olarak yalnızca birkaç "güvenli" başlığı görebilir (`Cache-Control`, `Content-Language`, `Content-Type`, `Expires`, `Last-Modified`, `Pragma`). Sen sayfalama toplamını `X-Total-Count` diye bir başlıkta dönüyorsan, ön yüz onu `null` görür. Çözüm: **`Access-Control-Expose-Headers: X-Total-Count`**. Dikkat — `Access-Control-Allow-Headers` bununla karıştırılır: o, tarayıcının **isteğe** hangi başlıkları koyabileceğini belirler; **yanıt** başlığının okunmasını `Expose-Headers` açar.

Spring tarafında üç yol vardır. Tek bir uç nokta ya da denetleyici (controller) için `@CrossOrigin`; uygulama geneli için `WebMvcConfigurer` içinde `addCorsMappings`; en esnek ve Spring Security ile uyumlu yol ise bir `CorsConfigurationSource` bean'i (Spring'in yönettiği nesne) tanımlamaktır.

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("https://app.ornek.com")); // joker DEĞİL
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    c.setAllowedHeaders(List.of("Content-Type", "Authorization"));
    c.setExposedHeaders(List.of("X-Total-Count"));         // yanıt başlığı okunabilsin
    c.setAllowCredentials(true);                           // çerez/Authorization gidecekse
    c.setMaxAge(Duration.ofMinutes(30));                   // preflight önbelleği
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/api/**", c);
    return src;
}
```

Spring Security ile birlikte kullanırken klasik bir **sıra tuzağı** vardır. Preflight `OPTIONS` isteği tanım gereği **kimlik bilgisi taşımaz**; güvenlik zincirinde kimlik doğrulama adımına gelirse 401 yer ve tarayıcı asıl isteği hiç atmaz. Bu yüzden CORS, zincirde **kimlik doğrulamadan önce** ele alınmalıdır: güvenlik yapılandırmasında `http.cors(Customizer.withDefaults())` demek, yukarıdaki `CorsConfigurationSource`'u kullanan `CorsFilter`'ı zincirin doğru noktasına yerleştirir. "Geliştirmede çalıştı, prod'da patladı" hikâyelerinin çoğu buradan çıkar. Yalnızca `OPTIONS` metodunu `permitAll` yapmak yeterli değildir: 401 kalkar ama yanıtta izin başlıkları yine olmadığı için tarayıcı engellemeyi sürdürür.

Son olarak kaçış yolları: geliştirmede Vite dev-server'ının **vekil (proxy)** özelliği ön yüz isteklerini kendi üzerinden API'ye taşır, böylece tarayıcı her şeyi aynı köken sanar ve CORS hiç devreye girmez. Üretimde de en temiz çözüm aynıdır: ön yüz ile API'yi bir **ters vekil** (nginx) arkasında **aynı köken** altında toplamak. Bu mümkün değilse izinli kökenleri tek tek yazarsın. `Access-Control-Allow-Origin: *` ile her şeye izin vermek ise hem tembelliktir hem risktir: herkesin API'ni tarayıcıdan çağırabilmesi demektir ve zaten kimlik bilgisi taşıyan isteklerde çalışmaz. Bir de sınırı net çizelim: CORS bir **CSRF** çözümü değildir — CSRF, saldırganın yanıtı okumadan yalnız isteği tetiklemesiyle ilgilidir ve tarayıcıda oturum taşıma konusunun (`211-tarayicida-oturum-tasima`) işidir.

### Mülakatta ne sorulur?

- **"Köken nedir?"** → **Şema + alan adı + port** üçlüsü. Üçünden biri farklıysa köken farklıdır; yol farkı kökeni değiştirmez. `https://a.com` ile `http://a.com`, `a.com` ile `api.a.com`, `a.com:8080` ile `a.com:443` hep farklı kökendir.
- **"CORS hatası aldığımda istek sunucuya ulaşmış mıdır?"** → Basit istekte **evet, ulaşır ve işlenir**; tarayıcı yalnız JS'in **yanıtı okumasını** keser. Preflight'ta ise izin verilmezse asıl istek hiç atılmaz. Hatayı üreten tarayıcıdır, sunucu 200 dönmüş olabilir. Bu yüzden CORS güvenlik duvarı değildir, sunucu tarafı yetkilendirmenin yerini tutmaz ve `curl`/Postman'de yoktur.
- **"Preflight ne zaman tetiklenir?"** → İstek "basit" değilse. Basit istek = GET/HEAD/POST + `Content-Type` yalnız `text/plain`/`application/x-www-form-urlencoded`/`multipart/form-data` + özel başlık yok. **`application/json` göndermek preflight'ı tetikler** — en sık karşılaşılan durum budur.
- **"Preflight'ta hangi başlıklar gidip gelir?"** → Tarayıcı `Origin`, `Access-Control-Request-Method`, `Access-Control-Request-Headers` gönderir; sunucu `Access-Control-Allow-Origin`, `-Allow-Methods`, `-Allow-Headers` ile yanıtlar. `Access-Control-Max-Age` izni tarayıcıda önbellekler, böylece her istekte iki tur atılmaz.
- **"Çerezle çalışan bir API'de `Allow-Origin: *` neden yetmez?"** → Kimlik bilgisi taşıyan isteklerde (`credentials: 'include'` + `Access-Control-Allow-Credentials: true`) **joker geçersizdir**; köken birebir yazılmalı, yanına `Vary: Origin` konmalıdır.
- **"Özel yanıt başlığımı ön yüz neden okuyamıyor?"** → JS varsayılanda yalnız birkaç başlığı görür; `X-Total-Count` gibi başlıklar için **`Access-Control-Expose-Headers`** gerekir. `Allow-Headers` bunun karşılığı değildir — o **istek** başlıklarını yönetir.
- **"Spring Security ekleyince preflight neden 401 alıyor?"** → `OPTIONS` isteği kimlik bilgisi taşımaz; CORS zincirde **kimlik doğrulamadan önce** ele alınmalıdır (`http.cors(...)` ile `CorsFilter` doğru noktaya girer). Yalnız `OPTIONS`'a `permitAll` vermek yetmez, çünkü izin başlıkları yine üretilmez.
- **"CORS'tan tümüyle nasıl kurtulursun?"** → Ön yüz ile API'yi **aynı köken** altında toplayarak: geliştirmede Vite dev-server vekili, üretimde nginx gibi bir ters vekil. Köken tekleşince kural hiç devreye girmez.

### Tek cümlede

CORS, tarayıcının Same-Origin Politikası'nı gevşetmek için sunucunun başlıklarla verdiği bir **izin protokolüdür**; isteği değil **yanıtın okunmasını** yönetir, izni veren taraf her zaman **sunucudur**.

# cors-1 | Temel
## Soru
`https://app.ornek.com` sayfasında çalışan JavaScript için aşağıdaki adreslerden hangisi **aynı köken (same-origin)** sayılır?
## A
`http://app.ornek.com/liste`
🤖 Şema farklı (http ↔ https); alan adı aynı olsa da köken farklıdır.
## B
`https://api.ornek.com/liste`
🤖 Alan adı farklı; üst alan adı ortak olsa da köken farklıdır.
## C
`https://app.ornek.com:8080/liste`
🤖 Port farklı (8080 ↔ 443); köken farklıdır.
## D
`https://app.ornek.com/liste/2`
🤖 Doğru: şema, alan adı ve port aynı; yol kökene dâhil değildir.
## Doğru
D
## Açıklama
Köken (origin) **şema + alan adı + port** üçlüsüdür ve üçünün de birebir aynı olması gerekir. D'de üç parça da taban sayfayla aynıdır; farklı olan tek şey **yol**tur (`/liste/2`) ve yol kökene dâhil değildir — bu yüzden aynı kökendir, SOP hiç devreye girmez. A'da şema (`http` ↔ `https`), B'de alan adı (`api` alt alan adı ayrı bir kökendir), C'de ise port (`8080` ↔ varsayılan `443`) farklıdır; üçü de **farklı köken** sayılır ve o adreslere yapılan isteklerde CORS kuralları uygulanır. Alt alan adının ortak olması ya da aynı fiziksel sunucuda durmak kökeni aynı yapmaz.

# cors-2 | Temel
## Soru
Tarayıcı konsolunda bir CORS hatası görüyorsun. Bu, isteğin sunucuya hiç ulaşmadığı ve sunucu tarafında hiç işlenmediği anlamına mı gelir?
## A
Evet; CORS bir güvenlik duvarı gibi çalışır ve köken ötesi isteği daha ağa çıkmadan her koşulda durdurur.
🤖 CORS güvenlik duvarı değildir; basit istek sunucuya ulaşır ve işlenir.
## B
Hayır; basit istek genelde sunucuya ulaşıp işlenir, tarayıcı yalnız JS'in yanıtı okumasını keser.
🤖 Doğru: engellenen şey isteğin kendisi değil, yanıtın okunmasıdır.
## C
Hayır; istek ulaşır ama sunucu `Origin` başlığını denetleyip yanıtı 403 ile reddettiği için hata alırsın.
🤖 Hatayı sunucu değil tarayıcı üretir; sunucu 200 bile dönmüş olabilir.
## D
Evet; sunucu isteği alsa bile CORS gereği yanıt gövdesini boş göndermek zorunda kaldığı için hata çıkar.
🤖 Sunucu yanıtı normal üretir; kısıtlama tamamen tarayıcı tarafındadır.
## Doğru
B
## Açıklama
CORS **isteği engellemez, yanıtı okumayı engeller**. Basit bir istekte tarayıcı isteği gönderir, sunucu onu normal biçimde işler (yan etkisi varsa gerçekleşir bile) ve yanıtı üretir; tarayıcı yanıtta uygun `Access-Control-Allow-Origin` başlığını göremezse yalnızca **JavaScript'in yanıta erişmesini** keser ve konsola hata basar. Bu yüzden A ve D yanlıştır: engelleme ağ katmanında değil, tarayıcının yanıt teslim adımındadır. C de yanlıştır — hatayı üreten sunucu değil tarayıcıdır; sunucunun durum kodu 200 olabilir. Buradan iki sonuç çıkar: CORS bir güvenlik duvarı **değildir** ve sunucu tarafı yetkilendirmenin yerini tutmaz; ayrıca `curl` veya Postman'de CORS diye bir şey yoktur, çünkü kural tarayıcıya aittir.

# cors-3 | Orta
## Soru
Ön yüz `fetch` ile `POST` atıyor ve `Content-Type: application/json` gönderiyor. Ağ sekmesinde asıl istekten hemen önce bir `OPTIONS` isteği belirdi. Bunun sebebi nedir?
## A
`fetch` API'si tasarımı gereği her istekten önce bir `OPTIONS` turu atar; `XMLHttpRequest` kullanılsaydı bu tur hiç olmazdı.
🤖 Preflight fetch'e özgü değildir; belirleyici olan isteğin basit olup olmamasıdır.
## B
`POST` metodu köken ötesinde her zaman preflight gerektirir; yalnız `GET` istekleri doğrudan gönderilebilir.
🤖 POST, izinli Content-Type ile basit istek olabilir; belirleyici metot değil başlıklardır.
## C
`application/json` izinli Content-Type listesinde yoktur; istek basit olmaktan çıkar ve preflight tetiklenir.
🤖 Doğru: json Content-Type basit istek koşulunu bozar ve OPTIONS turunu getirir.
## D
Sunucu `OPTIONS` metodunu açık bıraktığı için tarayıcı fırsat bulup ek bir tur atmıştır; metot kapatılırsa tur kalkar.
🤖 Turu tarayıcı zorunlu atar; OPTIONS kapatılırsa istek tamamen başarısız olur.
## Doğru
C
## Açıklama
Tarayıcı yalnızca **basit istek** olmayan çağrılarda preflight atar. Bir istek basit sayılmak için üç koşulu birden sağlamalıdır: metot `GET`/`HEAD`/`POST`; `Content-Type` yalnızca `text/plain`, `application/x-www-form-urlencoded` veya `multipart/form-data`; ve özel istek başlığı yok. `application/json` bu listede **olmadığı** için istek basitlikten çıkar ve `OPTIONS` turu zorunlu hale gelir — backend geliştiricinin en sık çarptığı nokta budur. B yanlıştır: `POST` izinli bir `Content-Type` ile pekâlâ basit istek olabilir. A yanlıştır: preflight `fetch`'e özgü değildir, `XMLHttpRequest` ile de aynı kural işler. D yanlıştır: tur sunucunun bir "açığı" değildir; `OPTIONS`'ı kapatırsan preflight başarısız olur ve asıl istek hiç gönderilmez.

# cors-4 | Orta
## Soru
Ön yüz `credentials: 'include'` ile çerez göndererek istek atıyor. Sunucu `Access-Control-Allow-Origin: *` ve `Access-Control-Allow-Credentials: true` dönüyor, ama tarayıcı yanıtı yine okutmuyor. Sebep nedir?
## A
Kimlik bilgisi taşıyan isteklerde joker köken geçersizdir; izin verilen köken birebir yazılmalı, yanına `Vary: Origin` konmalıdır.
🤖 Doğru: `*` ile kimlik bilgisi birlikte kullanılamaz, köken açıkça yazılır.
## B
`Access-Control-Allow-Credentials` yalnız preflight yanıtında anlamlıdır; asıl yanıtta bulunmasının hiçbir etkisi yoktur.
🤖 Bu başlık asıl yanıtta da gereklidir; buradaki sorun joker kökendir.
## C
Çerezler köken ötesi isteklerde hiçbir yapılandırmayla gönderilemez; tek yol kimlik jetonunu istek gövdesinde taşımaktır.
🤖 Böyle mutlak bir yasak yoktur; uygun kurulumla çerez pekâlâ gönderilebilir.
## D
`Access-Control-Max-Age` verilmediği için izin önbelleğe alınamamış ve tarayıcı elindeki yanıtı düşürmek zorunda kalmıştır.
🤖 Max-Age yalnız preflight'ın önbelleklenmesini etkiler, izni geçersiz kılmaz.
## Doğru
A
## Açıklama
Kimlik bilgisi (çerez, `Authorization`) taşıyan köken ötesi isteklerde `Access-Control-Allow-Origin: *` **kabul edilmez**; tarayıcı jokeri görünce yanıtı bloke eder. İzin verilen köken **birebir** yazılmalıdır (`Access-Control-Allow-Origin: https://app.ornek.com`) ve `Access-Control-Allow-Credentials: true` ile birlikte gönderilmelidir. Yanıt isteğin `Origin` değerine göre değiştiği için `Vary: Origin` de eklenir; aksi halde ara önbellekler bir kökene ait yanıtı başka bir kökene servis edebilir. B yanlıştır: `Allow-Credentials` asıl yanıtta da gereklidir. C yanlıştır: doğru kurulumla çerez pekâlâ gönderilir. D yanlıştır: `Max-Age` yalnızca preflight iznini önbellekler, iznin geçerliliğiyle ilgisi yoktur.

# cors-5 | İleri
## Soru
Geliştirme ortamında sorunsuz çalışan API, Spring Security devreye girince tarayıcıdan çağrıldığında patlıyor: preflight `OPTIONS` isteği kimlik doğrulamaya takılıp 401 dönüyor. Doğru teşhis ve çözüm nedir?
## A
Güvenlik zincirinde yalnızca `OPTIONS` metoduna `permitAll` vermek yeterlidir; ayrıca bir CORS yapılandırmasına gerek kalmaz.
🤖 401 kalkar ama yanıtta izin başlıkları üretilmediği için tarayıcı yine engeller.
## B
CORS zincirde kimlik doğrulamadan önce ele alınmalıdır; `http.cors(...)` ile `CorsFilter` doğru noktaya yerleşir.
🤖 Doğru: preflight kimlik taşımaz, doğrulamadan önce yanıtlanması gerekir.
## C
Preflight isteğine `Authorization` başlığı eklenmelidir; ön yüz bunu gönderirse `OPTIONS` turu da doğrulanmış olur.
🤖 Preflight tanım gereği kimlik bilgisi taşımaz; o başlık isteğe eklenemez.
## D
Sorun sunucuda değil tarayıcıdadır; `Access-Control-Max-Age` büyütülüp preflight önbelleklenirse 401 kendiliğinden kalkar.
🤖 Önbellek yalnız tur sayısını azaltır; ilk preflight yine 401 alır.
## Doğru
B
## Açıklama
Preflight `OPTIONS` isteği tanım gereği **kimlik bilgisi taşımaz** — tarayıcı ona çerez veya `Authorization` koymaz. Güvenlik filtre zincirinde kimlik doğrulama adımına ulaşırsa 401 döner, tarayıcı da asıl isteği hiç göndermez. Bu yüzden CORS, zincirde **kimlik doğrulamadan önce** ele alınmalıdır: güvenlik yapılandırmasında `http.cors(Customizer.withDefaults())` demek, `CorsConfigurationSource` bean'ini kullanan `CorsFilter`'ı zincirin doğru noktasına yerleştirir ve preflight daha doğrulamaya varmadan izin başlıklarıyla yanıtlanır. A yetersizdir: `permitAll` 401'i kaldırır ama izin başlıkları üretilmediği için tarayıcı engellemeyi sürdürür. C imkânsızdır: preflight'a başlık eklenemez. D konuyu ıskalar: `Max-Age` yalnızca izin verilmiş bir preflight'ı önbellekler.

# cors-6 | İleri
## Soru
API, sayfalama toplamını `X-Total-Count` yanıt başlığında dönüyor. Yanıt 200, gövde sorunsuz okunuyor, ama ön yüzdeki JavaScript bu başlığı `null` görüyor. Eksik olan nedir?
## A
Başlık `Access-Control-Allow-Headers` listesine eklenmelidir; bu liste yanıt başlıklarının okunabilirliğini yönetir.
🤖 `Allow-Headers` İSTEK başlıklarını yönetir; yanıt okuma iznini vermez.
## B
Özel yanıt başlıkları köken ötesi isteklerde hiçbir şekilde okunamaz; değer yanıt gövdesine taşınmalıdır.
🤖 `Expose-Headers` ile okunabilir hale gelir; böyle mutlak bir yasak yoktur.
## C
Başlık `Access-Control-Expose-Headers` ile açıkça beyan edilmelidir; JS varsayılanda yalnız birkaç başlığı görür.
🤖 Doğru: özel yanıt başlığını okutmak için `Expose-Headers` gerekir.
## D
`Access-Control-Allow-Origin: *` yerine kökenin birebir yazılması gerekir; joker değer başlık okumayı kapatır.
🤖 Joker yalnız kimlik bilgisiyle sorun çıkarır; yanıt başlığı okumayı belirlemez.
## Doğru
C
## Açıklama
Köken ötesi bir yanıtta JavaScript **varsayılan olarak** yalnızca birkaç güvenli başlığı görebilir: `Cache-Control`, `Content-Language`, `Content-Type`, `Expires`, `Last-Modified`, `Pragma`. `X-Total-Count` gibi özel bir başlık bu listede olmadığı için sunucu onu göndermiş olsa bile ön yüz `null` okur. Çözüm sunucunun başlığı açıkça beyan etmesidir: `Access-Control-Expose-Headers: X-Total-Count` (Spring'de `CorsConfiguration.setExposedHeaders(...)`). A klasik bir karışıklıktır: `Access-Control-Allow-Headers` tarayıcının **isteğe** hangi başlıkları koyabileceğini belirler, yanıt okumayla ilgisi yoktur. B yanlıştır: yasak mutlak değildir, beyan edilince okunur. D yanlıştır: joker köken kimlik bilgisi taşıyan isteklerde sorun çıkarır, ama yanıt başlığının okunabilirliğini belirleyen şey `Expose-Headers`'tır.
