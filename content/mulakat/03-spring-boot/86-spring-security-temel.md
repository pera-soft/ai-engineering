---
slug: spring-security-temel
baslik: "Spring Security Temelleri: Filtre Zinciri, Kimlik Doğrulama ve Yetkilendirme"
kademe: İleri
sira: 86
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir konser alanının kapısını düşün. Görevli önce kimliğine bakar: "Sen gerçekten bu bilette yazan kişi misin?" Kimliğin doğrulanınca içeri girersin. Ama sahne arkasına geçmek istediğinde ikinci bir kontrol vardır: "Senin bilekliğin sahne arkasına giriyor mu?" İki ayrı sorudur bunlar — biri **kim olduğun**, diğeri **neye izinli olduğun**. Spring Security uygulamaya gelen her isteği tam olarak bu iki kapıdan geçirir ve bu işi iş mantığından tamamen ayrı tutar; controller kodun güvenlikten habersiz yazılabilir.

### Temel kavramlar

- **Kimlik doğrulama (authentication)**: "Sen kimsin?" Kullanıcının iddia ettiği kimliği kanıtlaması (parola, token, sertifika).
- **Yetkilendirme (authorization)**: "Bunu yapmaya iznin var mı?" Doğrulanmış kimliğin belirli bir kaynağa erişip erişemeyeceğinin kararı.
- **Süzgeç (filter)**: İsteğe controller'a varmadan önce dokunan ara katman. Sırayla dizilmişlerine **süzgeç zinciri (filter chain)** denir. Metinde kısaca "filtre zinciri" de deriz.
- **`Authentication` nesnesi**: Doğrulama sonucunu (kim olduğun ve yetkilerin) taşıyan nesne; `SecurityContext` içine konur.
- **`GrantedAuthority` / rol**: Kullanıcıya verilen yetki etiketi (`ROLE_ADMIN`, `SCOPE_read` gibi).
- **Hash**: Bir metni geri çözülemeyecek biçimde sabit bir dizeye çeviren **tek yönlü** işlem. Parola karşılaştırması, girilen parolayı aynı işlemden geçirip sonuçları kıyaslayarak yapılır.
- **401 vs 403**: `401 Unauthorized` = kimliğini hiç kanıtlamadın. `403 Forbidden` = kimliğin belli ama bu kaynağa yetkin yok.
- **CSRF (siteler arası istek sahteciliği)**: Kurbanın tarayıcısına, farkında olmadan hedef siteye istek yaptırma saldırısı.

### Nasıl çalışır?

Spring Security servlet konteynerine tek bir filtre (`DelegatingFilterProxy`) olarak kaydolur; arkasında `FilterChainProxy`, `SecurityFilterChain` içindeki sıralı filtreleri çalıştırır. Her filtre tek bir sorumluluk taşır: `UsernamePasswordAuthenticationFilter` form-login kimliğini çıkarır, `CsrfFilter` CSRF jetonunu (token) doğrular, `AuthorizationFilter` erişim kararını verir. İstek ancak bu zinciri geçerse controller'a ulaşır.

Yani güvenlik ne controller metotlarının içine derleme zamanında enjekte edilir, ne veritabanı trigger'ıyla uygulanır, ne de istek başına ayrı bir JVM süreci açılır — hepsi aynı süreçte, servlet filtre zincirinde olur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Spring Security filtre zinciri: kimlik doğrulama sonra yetkilendirme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İstek filtre zincirinden geçer</text>
  <rect x="20" y="46" width="110" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="75" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İstek</text>
  <text x="75" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">HTTP + kimlik</text>
  <rect x="160" y="46" width="150" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="235" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Kimlik doğrulama</text>
  <text x="235" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"Sen kimsin?"</text>
  <text x="235" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">UserDetailsService + encoder</text>
  <rect x="340" y="46" width="150" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="415" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Yetkilendirme</text>
  <text x="415" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"İznin var mı?"</text>
  <text x="415" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">rol / authority kontrolü</text>
  <rect x="520" y="46" width="100" height="66" rx="10" fill="#F3EEE6" stroke="#9A6A1E" stroke-width="1.5"/>
  <text x="570" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#9A6A1E" font-family="system-ui">Controller</text>
  <text x="570" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş mantığı</text>
  <path d="M130 79 L158 79" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sec-ok)"/>
  <path d="M310 79 L338 79" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sec-ok)"/>
  <path d="M490 79 L518 79" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sec-ok)"/>
  <text x="320" y="150" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Yapılandırma: SecurityFilterChain bean'i + lambda DSL</text>
  <text x="320" y="172" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">WebSecurityConfigurerAdapter — Spring Security 6'da KALDIRILDI</text>
  <defs>
    <marker id="sec-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

2026'da (Spring Security 6 / Boot 3.x) zincirin kuralları tek bir `@Bean` metodunda yazılır. Bu metot bir `SecurityFilterChain` nesnesi döndürür; nesneyi Spring üretip yönetir, o yüzden ona **Spring'in yönettiği nesne (bean)** denir. Kurallar `HttpSecurity` üzerinden **lambda DSL** ile kurulur:

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/genel/**").permitAll()
            .anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .build();
}
```

Eski `WebSecurityConfigurerAdapter` sınıfı Spring Security 6'da **tamamen KALDIRILDI** — ondan türetmek artık derlenmez. Zincirleme `.and()` çağrıları 6.1'de kullanımdan kaldırıldı (deprecated) ve sonraki sürümlerde silindi. `web.xml` ile elle filtre sıralaması modern Boot uygulamasında kullanılmaz; `application.properties` yalnız temel ayarları verir, zincirin kurallarını değil.

Kimlik doğrulama adımının kendisi iki bileşene dayanır ve rolleri karıştırılmamalıdır.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kimlik doğrulama akışı: UserDetailsService kaydı yükler, PasswordEncoder parolayı karşılaştırır">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kimlik doğrulama: iki bileşen, iki iş</text>
  <rect x="24" y="46" width="120" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Giriş</text>
  <text x="84" y="92" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kullanıcı + parola</text>
  <rect x="182" y="46" width="176" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="270" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">UserDetailsService</text>
  <text x="270" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kaydı yükler (hash dahil)</text>
  <text x="270" y="101" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">loadUserByUsername</text>
  <rect x="396" y="46" width="176" height="60" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="484" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">PasswordEncoder</text>
  <text x="484" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">matches(ham, hash)</text>
  <text x="484" y="101" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">parola karşılaştırır</text>
  <path d="M144 76 L180 76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sec2-ok)"/>
  <path d="M358 76 L394 76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sec2-ok)"/>
  <rect x="182" y="130" width="390" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="377" y="154" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">eşleşirse → Authentication + SecurityContext</text>
  <text x="377" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">parola asla düz metin saklanmaz; {bcrypt} öneki algoritmayı işaretler</text>
  <path d="M484 106 L410 130" stroke="#5D7789" stroke-width="1.4" fill="none" marker-end="url(#sec2-ok)"/>
  <defs>
    <marker id="sec2-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

`UserDetailsService.loadUserByUsername(...)` kullanıcı adına karşılık gelen `UserDetails` kaydını — kullanıcı adı, **hash'lenmiş parola** ve yetkiler — tipik olarak veritabanından yükler. `PasswordEncoder` ise girişte gelen ham parolayı saklanan hash ile `matches(raw, encoded)` çağrısıyla karşılaştırır. Biri kaydı **getirir**, diğeri parolayı **doğrular**; ikisi de kimlik doğrulama aşamasının parçasıdır, yetki kararının değil.

Parolalar **tek yönlü** ve kasıtlı olarak **yavaş** algoritmalarla saklanır. Varsayılan `DelegatingPasswordEncoder`, hash'in başına algoritma önekini koyar: `{bcrypt}$2a$10$...`. Bunun iki faydası var: doğrulamada hangi algoritmanın kullanılacağı hash'in kendisinden okunur ve ileride Argon2 veya scrypt gibi daha güçlü bir algoritmaya geçiş, eski kayıtları bozmadan yapılabilir. Yanlış seçenekler nettir. MD5/SHA-1 hem hızlıdır hem de kriptografik olarak kırıktır; bu yüzden kaba kuvvet denemelerine açıktır. Parolayı düz metin saklamak, tek bir sızıntıda tüm hesapları açık eder. AES gibi **geri çözülebilir** şifreleme de parola için yanlıştır: hedef, çözülebilir bir sır değil, doğrulanabilir tek yönlü bir hash'tir.

Son bir ayrım da isteğin kimliğini **nerede taşıdığıyla** ilgilidir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CSRF: oturum çerezi tarayıcıca otomatik eklenir, Bearer başlığı elle eklenir">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">CSRF zemini kimliğin NEREDE taşındığına bağlı</text>
  <rect x="24" y="44" width="286" height="150" rx="10" fill="#FBECEC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="167" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Oturum çerezi (stateful)</text>
  <text x="167" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tarayıcı çerezi OTOMATİK ekler</text>
  <text x="167" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">saldırgan sitesi istek yaptırır</text>
  <text x="167" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek "kimlikli" görünür</text>
  <text x="167" y="158" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">CSRF zemini VAR</text>
  <text x="167" y="178" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">→ CSRF korumasını AÇIK tut</text>
  <rect x="330" y="44" width="286" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Bearer token (stateless)</text>
  <text x="473" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Authorization başlığında taşınır</text>
  <text x="473" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tarayıcı bunu OTOMATİK eklemez</text>
  <text x="473" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">JS elle koyar</text>
  <text x="473" y="158" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">CSRF zemini YOK</text>
  <text x="473" y="178" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">→ csrf.disable() makul</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Authentication ve authorization farkı?"** → Önce "sen kimsin" (sonuç: `SecurityContext` içindeki `Authentication` nesnesi), sonra "iznin var mı" (`GrantedAuthority`/rol kontrolü). Sıra önemlidir ve ikisi aynı şey değildir: doğrulanmış bir kullanıcı yine de yetkisiz olabilir. Kimliği doğrulanmamış istek **401**, doğrulanmış ama yetkisiz istek **403** alır — bu ayrım çok sorulur. İkisi de performans/önbellek meselesi değildir.
- **"Spring Security isteği nasıl işler?"** → `DelegatingFilterProxy` → `FilterChainProxy` → sıralı `SecurityFilterChain` filtreleri. Her filtre tek bir güvenlik görevi üstlenir; istek zinciri geçince controller'a ulaşır.
- **"2026'da yapılandırma nasıl yazılır?"** → `SecurityFilterChain` bean'i + `HttpSecurity` lambda DSL. `WebSecurityConfigurerAdapter` **kaldırıldı**, ondan türetmek derlenmez; `.and()` zincirlemesi de silindi. `web.xml` ya da yalnız property ile filtre kuralı tanımlanmaz.
- **"`UserDetailsService` ve `PasswordEncoder` ne yapar?"** → İlki kullanıcı kaydını (hash ve yetkiler dahil) yükler, ikincisi ham parolayı hash ile karşılaştırır. Roller ters çevrilmez; parolayı `UserDetailsService` şifrelemez, kullanıcıyı `PasswordEncoder` çekmez.
- **"Parola nasıl saklanır?"** → Varsayılan `DelegatingPasswordEncoder` ile; hash'in `{bcrypt}` gibi öneki algoritmayı işaretler ve ileride algoritma geçişini eski kayıtları bozmadan mümkün kılar. Argon2 ve scrypt de desteklenir. MD5, düz metin ve geri çözülebilir şifreleme yanlıştır.
- **"Durum tutmayan (stateless) JWT API'de neden `csrf.disable()` makul?"** → CSRF, tarayıcının **oturum çerezini otomatik eklemesini** sömürür. Saldırganın sitesi kurbanın tarayıcısından hedefe istek yaptırır; çerez otomatik eklendiği için istek "kimlikli" görünür. Durum tutmayan API'de kimlik çerezde değil, JavaScript'in elle koyduğu `Authorization: Bearer ...` başlığındadır; tarayıcı bu başlığı kendiliğinden eklemez, dolayısıyla saldırının dayandığı zemin yoktur. Bu bir performans kararı değildir ve Spring CSRF'i zorla açık tutmaz — `csrf(csrf -> csrf.disable())` ile kapatılır. **DİKKAT:** token bir çerezde tutuluyorsa CSRF yeniden gündeme gelir; ayrıca oturum çerezi kullanan klasik form-login uygulamasında CSRF'i kapatmak ciddi bir açıktır. Karar, "kimlik nerede taşınıyor" sorusuna bağlıdır. (Not: gerekçe JWT'nin şifreli olması değildir — JWT genelde imzalıdır, şifreli olması şart değildir.)

### Tek cümlede

Spring Security her isteği bir süzgeç zincirinden geçirir; önce "kimsin" (401), sonra "iznin var mı" (403) sorusunu yanıtlar. 2026'da bu zincir `SecurityFilterChain` bean'iyle yapılandırılır ve parolalar tek yönlü, önekli hash'lerle saklanır.

# sec-1 | Temel
## Soru
Spring Security'de "kimlik doğrulama" (authentication) ile "yetkilendirme" (authorization) arasındaki temel fark nedir?
## A
Kimlik doğrulama isteğin hızını ölçer; yetkilendirme ise yanıtı önbelleğe alarak sonraki istekleri hızlandırır
🤖 İkisi de performansla ilgili değildir; güvenlik kararlarıdır.
## B
Kimlik doğrulama parolaları şifreler; yetkilendirme ise gerektiğinde bu parolaları geri çözer
🤖 Parola şifreleme `PasswordEncoder`'ın işidir; bu iki kavramın tanımı değil.
## C
Kimlik doğrulama "sen kimsin?"; yetkilendirme "bu kaynağa iznin var mı?" sorusunu yanıtlar
🤖 Doğru: önce kimlik kanıtlanır, sonra o kimliğin yetkisi kontrol edilir.
## D
İkisi aynı şeydir; Spring tarihsel nedenlerle iki ayrı isim kullanır ama arka planda tek bir kontrol çalışır
🤖 Ayrı adımlardır: doğrulanmış bir kullanıcı yine de bir kaynağa yetkisiz olabilir.
## Doğru
C
## Açıklama
**Kimlik doğrulama** kullanıcının iddia ettiği kimliği kanıtlamasıdır (parola, token, sertifika) ve sonucu `SecurityContext` içine konan bir `Authentication` nesnesidir. **Yetkilendirme** ise bu doğrulanmış kimliğin sahip olduğu yetkilere (`GrantedAuthority`/rol) göre belirli bir kaynağa erişimin kararıdır. Sıra önemlidir: önce "kimsin", sonra "iznin var mı". Doğrulanmış ama yetkisiz bir kullanıcı `401` değil `403` alır — bu ayrım mülakatta sık sorulur.

# sec-2 | Temel
## Soru
Spring Security gelen HTTP isteklerini nasıl işler?
## A
İsteği, her biri tek bir güvenlik görevi üstlenen sıralı bir filtre zincirinden geçirerek
🤖 Doğru: `FilterChainProxy` içindeki sıralı filtreler kimlik, oturum, CSRF ve yetkiyi işler.
## B
Her controller metodunun içine gerekli güvenlik kodunu derleme zamanında otomatik enjekte ederek
🤖 Güvenlik controller'a gömülmez; istek daha controller'a varmadan filtrelerde işlenir.
## C
Yalnızca veritabanı katmanına kurulan bir trigger ile, sorgu çalışmadan hemen önce
🤖 Güvenlik web katmanındaki servlet filtreleriyle uygulanır, veritabanı trigger'ıyla değil.
## D
Her istek için ayrı bir JVM süreci başlatıp isteği o süreçte yalıtarak
🤖 Böyle bir şey yok; filtreler aynı süreçte servlet zincirinde çalışır.
## Doğru
A
## Açıklama
Spring Security, servlet konteynerine tek bir `DelegatingFilterProxy` olarak bağlanır ve arkasında **sıralı filtrelerden oluşan bir zincir** (`FilterChainProxy` → `SecurityFilterChain`) çalıştırır. Her filtre tek bir sorumluluk taşır: örneğin `UsernamePasswordAuthenticationFilter` form-login kimliğini çıkarır, `AuthorizationFilter` erişim kararını verir, `CsrfFilter` CSRF token'ını doğrular. İstek bu zinciri geçtikten sonra controller'a ulaşır. Bu yüzden güvenlik iş mantığından ayrık kalır — controller kodu güvenlikten habersiz yazılabilir.

# sec-3 | Orta
## Soru
Spring Security 6 ile bir güvenlik yapılandırması yazarken 2026'da izlenen doğru yaklaşım hangisidir?
## A
`WebSecurityConfigurerAdapter` sınıfından türetip `configure(HttpSecurity)` metodunu ezerek kuralları yazmak
🤖 Bu sınıf Spring Security 6'da KALDIRILDI; ondan türetmek artık derlenmez.
## B
Bir `SecurityFilterChain` bean'i tanımlayıp `HttpSecurity`'yi lambda DSL ile yapılandırmak
🤖 Doğru: 2026'da standart yol bean tabanlı yapılandırma ve lambda DSL'dir.
## C
`web.xml` içinde filtreleri elle sıralamak
🤖 XML tabanlı elle filtre sıralaması modern Boot uygulamasında kullanılmaz.
## D
Güvenliği yalnızca `application.properties` içindeki anahtarlarla açıp kapamak
🤖 Temel ayarlar property ile verilebilir ama filtre zinciri kuralları Java yapılandırmasıyla tanımlanır.
## Doğru
B
## Açıklama
Spring Security 6 (Boot 3.x, 2026) ile yapılandırma **bir `SecurityFilterChain` bean'i döndüren `@Bean` metodu** üzerinden yapılır ve `HttpSecurity` **lambda DSL** ile ayarlanır:

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .build();
}
```

Eski `WebSecurityConfigurerAdapter` **tamamen kaldırıldığı** için (A) derlenmez. Lambda DSL standart hale geldi; zincirleme `.and()` çağrıları 6.1'de kullanımdan kaldırıldı (deprecated) ve sonraki sürümlerde silindi. `web.xml` ile elle filtre sıralaması (C) modern Boot uygulamasında kullanılmaz; property'ler (D) yalnız temel ayarları verir, kuralları değil.

# sec-4 | Orta
## Soru
`UserDetailsService` ve `PasswordEncoder` bir kimlik doğrulama akışında hangi rolleri üstlenir?
## A
`UserDetailsService` girilen parolayı şifreler; `PasswordEncoder` ise kullanıcı kaydını veritabanından çekip döndürür
🤖 Roller ters verilmiş; kullanıcıyı çeken `UserDetailsService`, parolayı işleyen `PasswordEncoder`'dır.
## B
İkisi de yalnızca yetkilendirme (authorization) aşamasında, erişim kararı verilirken devreye girer
🤖 Her ikisi de kimlik DOĞRULAMA aşamasının parçasıdır, yetki kararının değil.
## C
`PasswordEncoder` kullanıcı adının doğruluğunu; `UserDetailsService` ise girilen parolanın doğruluğunu denetler
🤖 Karışık; kullanıcıyı `UserDetailsService` yükler, parola eşleşmesini encoder yapar.
## D
`UserDetailsService` kullanıcı kaydını (hash dahil) yükler; `PasswordEncoder` parolayı hash ile karşılaştırır
🤖 Doğru: biri kaydı getirir, diğeri parola eşleşmesini doğrular.
## Doğru
D
## Açıklama
`UserDetailsService.loadUserByUsername(...)` verilen kullanıcı adına karşılık gelen `UserDetails` kaydını (kullanıcı adı, **şifrelenmiş parola**, yetkiler) döndürür — tipik olarak veritabanından. `PasswordEncoder` ise girişte gelen düz parolayı, saklanan hash ile `matches(raw, encoded)` çağrısıyla karşılaştırır. Parola asla düz metin saklanmaz; `DelegatingPasswordEncoder` (varsayılan) hash'in başına `{bcrypt}` gibi bir önek koyarak hangi algoritmayla üretildiğini işaretler ve doğrulamada onu kullanır. Bu iki bileşen birlikte kimlik doğrulama aşamasını oluşturur.

# sec-5 | İleri
## Soru
Bir Spring Boot uygulamasında parolaları saklamak için hangi yaklaşım 2026'da önerilir?
## A
Varsayılan `DelegatingPasswordEncoder` ile saklamak; hash'in öneki algoritmayı işaretler
🤖 Doğru: önek algoritmayı işaretler ve zamanla farklı algoritmalara sorunsuz geçişi sağlar.
## B
Parolaları MD5 ile hash'leyip veritabanına yazmak; MD5 çok hızlı olduğu için giriş gecikmesi de azalır
🤖 MD5 kriptografik olarak kırık ve hızlıdır (kaba kuvvete zayıf); parola için uygun değildir.
## C
Parolaları düz metin saklayıp yalnızca veritabanı erişimini sıkı yetkilerle kısıtlamakla yetinmek
🤖 Düz metin parola, sızıntıda tüm hesapları açık eder; asla kabul edilmez.
## D
Parolaları AES gibi tersine çevrilebilir bir şifreleme ile saklamak ki gerektiğinde çözülüp okunabilsin
🤖 Parolalar geri çözülebilir olmamalı; hedef doğrulanabilir tek yönlü hash'tir, çözülebilir şifre değil.
## Doğru
A
## Açıklama
Modern Spring Security'de varsayılan `PasswordEncoder`, **`DelegatingPasswordEncoder`**'dır. Sakladığı hash'in başına algoritma önekini koyar: `{bcrypt}$2a$10$...`. Bunun iki faydası vardır: (1) doğrulamada hangi algoritmanın kullanılacağı hash'in kendisinden okunur; (2) ileride bcrypt'ten argon2/scrypt gibi daha güçlü bir algoritmaya geçiş, eski hash'leri bozmadan yapılabilir. Parolalar **tek yönlü** ve **yavaş** (bcrypt gibi) hash'lenmeli; MD5/SHA-1 (hızlı, kırık) ve düz metin ya da geri çözülebilir şifreleme yanlıştır.

# sec-6 | İleri
## Soru
Tamamen stateless (JWT taşıyan, sunucu oturumu tutmayan) bir REST API'de CSRF korumasını kapatmak neden makul kabul edilir?
## A
CSRF koruması her istekte ek bir token doğrulaması yaptığı için performansı düşürür; bu yüzden bütün API'lerde kapatılması önerilir
🤖 CSRF performans meselesi değil güvenlik meselesidir; her yerde kapatmak yanlış olur.
## B
Klasik CSRF, tarayıcının otomatik eklediği oturum çerezini sömürür; stateless API'de kimlik `Authorization` başlığındadır
🤖 Doğru: token başlıkta taşınır ve tarayıcı onu otomatik eklemez, dolayısıyla CSRF zemini kalkar.
## C
JWT'nin içeriği şifreli olduğundan saldırgan token'ı okuyamaz ve geçerli bir istek üretemez; bu yüzden CSRF imkânsız hale gelir
🤖 JWT genelde imzalıdır ama şifreli olması şart değil; CSRF'in kalkma nedeni de bu değil.
## D
Spring Security stateless modda CSRF filtresini otomatik olarak zorla açık tutar; yapılandırmayla elle kapatmaya izin vermez
🤖 Aksine; CSRF elle `csrf(csrf -> csrf.disable())` ile kapatılabilir.
## Doğru
B
## Açıklama
CSRF (Cross-Site Request Forgery) saldırısı, tarayıcının bir siteye giderken **oturum çerezini otomatik olarak** eklemesini sömürür: saldırgan sitesi kurbanın tarayıcısından hedefe istek yaptırır, çerez otomatik eklendiği için istek "kimlikli" görünür. **Stateless JWT API'de** kimlik çerezde değil, JavaScript'in elle koyduğu `Authorization: Bearer ...` başlığında taşınır; tarayıcı bu başlığı kendiliğinden eklemez. Dolayısıyla CSRF'in dayandığı "otomatik kimlik ekleme" mekanizması yoktur ve `csrf.disable()` güvenlidir. DİKKAT: eğer token bir çerezde tutuluyorsa CSRF yeniden gündeme gelir; kapatma kararı "kimlik nerede taşınıyor" sorusuna bağlıdır. Oturum çerezli klasik uygulamalarda CSRF'i kapatmak ciddi açıktır.
