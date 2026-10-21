---
slug: spring-boot-test
baslik: "Spring Boot Test: Slice Testleri, MockMvc ve Testcontainers"
kademe: Orta
sira: 90
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Yeni bir bina teslim edilirken her şeyi aynı anda denemezsin. Önce tek bir prizin elektriği ölçülür, sonra bir kattaki tesisat, en sonunda bina tümüyle bir gün boyunca çalıştırılır. Küçük denemeler ucuz ve hızlıdır, tam deneme pahalı ama en gerçekçidir. Spring Boot test araçları tam olarak bu üç seviyeyi verir: saf mantığı sınayan birim testleri, tek bir katmanı ayağa kaldıran **dilim** (slice) testleri ve uygulamanın tamamını başlatan entegrasyon testleri.

### Temel kavramlar

- **Test**: Kodun beklendiği gibi davrandığını otomatik olarak doğrulayan başka bir kod parçası.
- **Uygulama bağlamı (application context)**: Spring'in bütün bean'leri — yani kendi üretip yönettiği nesneleri — oluşturup birbirine bağladığı kap. Ayağa kalkması zaman alır.
- **Birim testi**: Tek bir sınıfın mantığını, Spring bağlamı hiç yüklemeden sınar. En hızlısıdır.
- **Test dilimi (test slice)**: Bağlamın **yalnız bir katmanını** yükleyen test türü. `@WebMvcTest` yalnız web katmanını, `@DataJpaTest` yalnız JPA/repository katmanını, `@JsonTest` yalnız serileştirmeyi getirir.
- **Entegrasyon testi**: Parçaların birlikte çalıştığını sınar; Spring'de `@SpringBootTest` ile tüm bağlam yüklenir.
- **Sahte / mock**: Gerçek bir bağımlılığın yerine geçen, ne döneceğini senin belirlediğin taklit nesne.
- **Profil (profile)**: Ortama göre farklı yapılandırma seti (`test`, `prod`).
- **Testcontainers**: Test sırasında gerçek bir yazılımı (PostgreSQL, Kafka) Docker konteynerinde başlatan kütüphane.

Temel gerilim **kapsam ile hız** arasındadır. `@SpringBootTest` tüm bean'leri, otomatik yapılandırmaları ve katmanları yükler: en gerçekçi, en yavaş, en çok bellek yiyen testtir. Dilim testleri belirgin biçimde hızlıdır ve hata mesajları daha odaklıdır, ama uçtan uca doğrulama yapmaz. İkisi de test ortamında çalışır; "slice üretimde, `@SpringBootTest` yerelde" gibi bir kısıt yoktur. Kural: mümkün olduğunca dilim kullan, tam bağlamı gerçekten uçtan uca doğrulama gerektiğinde aç.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SpringBootTest tam bağlam ile slice testlerinin kapsam-hız dengesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapsam ↔ Hız dengesi</text>
  <rect x="40" y="46" width="230" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="155" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">@SpringBootTest</text>
  <text x="155" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">TÜM bağlam yüklenir</text>
  <text x="155" y="116" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ en gerçekçi (uçtan uca)</text>
  <text x="155" y="136" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">− yavaş, ağır</text>
  <text x="155" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">entegrasyon testi</text>
  <rect x="370" y="46" width="230" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="485" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Slice testleri</text>
  <text x="485" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@WebMvcTest — yalnız web</text>
  <text x="485" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@DataJpaTest — yalnız JPA</text>
  <text x="485" y="132" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ hızlı, odaklı</text>
  <text x="485" y="152" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">− tek katman (uçtan uca değil)</text>
  <text x="485" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">birim/katman testi</text>
  <defs>
    <marker id="test-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**1. Bağlam önbelleği hızın ikinci kaynağıdır.** Spring test çatısı, **aynı yapılandırmaya** sahip test sınıfları için uygulama bağlamını bir kez kurar ve sonrakilerde yeniden kullanır (test context caching). Bu yüzden her sınıfta farklı property, farklı profil veya farklı bir sahte bean kullanmak **yeni bir bağlam** doğurur ve paketi yavaşlatır. Yapılandırma çeşitliliğini az tutmak test süresini gözle görülür biçimde düşürür.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test piramidi: tabanda çok sayıda hızlı birim ve slice testi, tepede az sayıda yavaş entegrasyon testi">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Test piramidi: çoğunlukla hızlı slice, az entegrasyon</text>
  <polygon points="260,44 196,96 324,96" fill="#FBEDED" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="260" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">@SpringBootTest</text>
  <polygon points="196,96 324,96 392,152 128,152" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="260" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Slice testleri</text>
  <text x="260" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@WebMvcTest · @DataJpaTest</text>
  <polygon points="128,152 392,152 456,204 64,204" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="260" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Birim testleri (saf mantık)</text>
  <text x="260" y="192" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağlam yüklemez, en hızlı</text>
  <path d="M520 200 L520 54" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#test2-ar)"/>
  <text x="560" y="70" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">az / yavaş</text>
  <text x="560" y="196" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">çok / hızlı</text>
  <defs>
    <marker id="test2-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**2. Web katmanını `MockMvc` çağırır.** `MockMvc`, controller'ları **gerçek bir sunucu (Tomcat) başlatmadan** test eder; servlet yığınını bellek içinde simüle eder:

```java
mockMvc.perform(get("/kullanicilar/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.ad").value("Ayşe"));
```

Yönlendirme, serileştirme, doğrulama ve hata işleme davranışını hızlıca sınar. Bağlantı havuzu, üretim trafiği veya bellek sızıntısı `MockMvc`'nin konusu değildir. Reaktif (WebFlux) tarafta karşılığı `WebTestClient`'tır; `WebTestClient`, `@SpringBootTest(webEnvironment = RANDOM_PORT)` ile MVC'yi de test edebilir.

**3. Dilimde bağımlılık sahte olmak zorundadır.** `@WebMvcTest` yalnız controller, `@ControllerAdvice`, filtreler ve `MockMvc`'yi yükler; **servis ve repository bean'lerini yüklemez**. Controller bir servise bağımlıysa bağlam "bean bulunamadı" diye başlamaz. Çözüm, bağımlılığı sahte bir bean olarak sağlamaktır — Boot 3.4+ ile **`@MockitoBean`** (eski `@MockBean` kullanımdan kaldırıldı):

```java
@WebMvcTest(KullaniciController.class)
class KullaniciControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean KullaniciServisi servis;
}
```

Bean'leri elle `new` ile üretmek Spring modeliyle uyumsuzdur; test edilen controller'ı mock'lamak ise testi anlamsız kılar.

**4. Veritabanı testinde gerçek motor kullan.** H2 gibi gömülü veritabanları hızlıdır. Ama üretimdeki motoru **tam taklit etmezler**: SQL lehçesi, tip sistemi, kısıtlar, indeks davranışı ve `JSONB`/`ARRAY` gibi özel tipler farklı çalışır. Uyumluluk moduna almak farkları azaltır ama bitirmez. H2'de yeşil olan test üretimde patlayabilir; testin güven verme amacı boşa çıkar. 2026 pratiği **Testcontainers**'tır: üretimdekiyle **aynı** imaj Docker'da başlatılır, test bitince temizlenir. Boot 3.1+ ile `@ServiceConnection` bağlantı özelliklerini otomatik enjekte eder:

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class SiparisRepositoryTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16");
}
```

Testi silmek (kapsamı düşürür) veya üretim veritabanına bağlanmak (veriyi bozar, tehlikelidir) çözüm değildir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="H2 gömülü veritabanı üretimden farklı davranır, Testcontainers testte gerçek veritabanını çalıştırır">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">H2 (gömülü) ile Testcontainers (gerçek DB)</text>
  <rect x="24" y="42" width="290" height="150" rx="12" fill="#FBEDED" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="169" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">H2 gömülü</text>
  <text x="169" y="88" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ hızlı, kurulumsuz</text>
  <text x="169" y="106" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− farklı SQL lehçesi / kısıt / tip</text>
  <rect x="44" y="124" width="250" height="52" rx="8" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.2"/>
  <text x="169" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">H2'de test YEŞİL →</text>
  <text x="169" y="163" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">üretimde (PostgreSQL) patlar</text>
  <rect x="326" y="42" width="290" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Testcontainers</text>
  <rect x="346" y="80" width="250" height="42" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="471" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Docker: gerçek postgres:16</text>
  <text x="471" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">test bitince temizlenir</text>
  <text x="471" y="144" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ üretimle AYNI motor</text>
  <text x="471" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@ServiceConnection ile otomatik</text>
  <text x="471" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sürprizler testte yakalanır</text>
</svg>
```

### Mülakatta ne sorulur?

- **Dilim vs tam bağlam**: `@SpringBootTest` tüm bağlamı (yavaş/gerçekçi), `@WebMvcTest`/`@DataJpaTest` tek katmanı (hızlı/odaklı) yükler. Aralarındaki fark yalnız isim değildir.
- **`MockMvc` ne yapar?** Sunucu başlatmadan controller'a istek simüle edip durum kodunu, gövdeyi ve başlıkları doğrular.
- **`@MockitoBean` vs `@Mock`**: `@MockitoBean` sahteyi **Spring bağlamına bean olarak** koyar — o tipte gerçek bir bean varsa yerini alır, yoksa ekler; `@Autowired`/constructor ile enjekte edilen her yerde bu sahte kullanılır. Mockito'nun `@Mock`'u ise yalnız test sınıfında yerel bir sahte nesnedir, konteynere girmez. Yığın (MVC/reaktif) kısıtı yoktur.
- **Testcontainers vs H2**: gerçek motor, lehçe/kısıt sürprizlerini test aşamasında yakalar.
- **Bağlam önbelleği**: aynı yapılandırma paylaşılırsa bağlam bir kez kurulur; yapılandırma çeşitliliği paketi yavaşlatır.
- **Ortam ayrımı**: `@ActiveProfiles("test")` + `src/test/resources/application-test.yml`; bean bazında `@Profile("test")`. Spring varsayılan `application.yml` ile `-test` dosyasını birleştirir, test değerleri üsttedir. `application.properties`'i elle düzenleyip geri almak kırılgan ve yarışa açıktır; her test sınıfı için ayrı uygulama yazmak gereksizdir.

### Tek cümlede

Hızlı dilim testlerini (`@WebMvcTest` + `MockMvc` + `@MockitoBean`) çoğunlukta tut ve bunları gerçek motorlu (`Testcontainers`) az sayıda `@SpringBootTest` entegrasyon testiyle tamamla; yapılandırma çeşitliliğini düşük tutarsan bağlam önbelleğinden de faydalanırsın.

# test-1 | Temel
## Soru
`@SpringBootTest` ile bir slice testi (`@WebMvcTest` gibi) arasındaki temel fark nedir?
## A
`@SpringBootTest` yalnız web katmanını yükler; slice testi ise tüm bağlamı ayağa kaldırır
🤖 Ters; tüm bağlamı yükleyen `@SpringBootTest`, tek katmanı yükleyen slice testidir.
## B
`@SpringBootTest` tüm bağlamı, slice testi yalnız ilgili katmanı yükler
🤖 Doğru: biri tam bağlam (yavaş/gerçekçi), diğeri tek katman (hızlı/odaklı).
## C
İkisi de aynı hızda çalışır; aralarındaki tek fark anotasyonun ismidir
🤖 Slice testi çok daha az bean yüklediği için belirgin biçimde hızlıdır.
## D
Slice testleri üretim ortamında, `@SpringBootTest` yalnız yerel makinede çalışır
🤖 İkisi de test ortamında çalışır; böyle bir ortam kısıtı yoktur.
## Doğru
B
## Açıklama
**`@SpringBootTest`** tüm uygulama bağlamını (context) ayağa kaldırır — tüm bean'ler, otomatik yapılandırmalar, katmanlar. Bu en gerçekçi ama en yavaş ve en ağır testtir; uçtan uca entegrasyon senaryoları için uygundur. **Slice testleri** (`@WebMvcTest`, `@DataJpaTest`, `@JsonTest`…) bağlamın **yalnız bir dilimini** yükler: örneğin `@WebMvcTest` yalnız web katmanını (controller, filtreler, `MockMvc`) getirir, servis/repository katmanını yüklemez (onları mock'larsınız). Sonuç: çok daha hızlı başlangıç ve daha odaklı hata mesajları. İyi bir test stratejisi, çoğunlukla hızlı slice testleri + az sayıda tam `@SpringBootTest` entegrasyon testi kullanır.

# test-2 | Temel
## Soru
`MockMvc` bir web testinde ne işe yarar?
## A
Gerçek sunucu başlatmadan controller'a istek simüle edip yanıtı doğrular
🤖 Doğru: MockMvc sunucusuz olarak MVC yığınına istek gönderip durum kodunu, gövdeyi ve başlıkları test eder.
## B
Veritabanı bağlantı havuzunun boyutunu ve sızıntılarını yük altında test eder
🤖 MockMvc web katmanı içindir; bağlantı havuzu testinin aracı değildir.
## C
Üretim trafiğini kademeli olarak gerçek kullanıcılara yönlendirir
🤖 MockMvc bir test aracıdır; üretim trafiği yönlendirmez.
## D
JVM'deki bellek sızıntılarını heap analiziyle tespit eder
🤖 Bellek analizi ayrı araçların işidir; MockMvc'nin amacı değildir.
## Doğru
A
## Açıklama
**`MockMvc`**, Spring MVC controller'larını **gerçek bir sunucu (Tomcat) başlatmadan** test etmeyi sağlar. Servlet yığınını bellek içinde simüle eder; isteği (`get`, `post`, başlıklar, gövde) verirsiniz ve yanıtı akıcı bir API ile doğrularsınız:

```java
mockMvc.perform(get("/kullanicilar/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.ad").value("Ayşe"));
```

Genellikle `@WebMvcTest` slice'ı ile birlikte kullanılır; controller'ın yönlendirme, serileştirme, doğrulama ve hata işleme davranışını hızlıca sınar. Gerçek port açmadığı için hızlıdır. Reaktif (WebFlux) tarafta karşılığı `WebTestClient`'tır.

# test-3 | Orta
## Soru
Bir `@WebMvcTest` içinde controller'ın bağımlı olduğu servis katmanını nasıl ele almak gerekir?
## A
Servisi elle `new` ile oluşturup controller'ın kurucusuna parametre olarak geçmek
🤖 Spring bağlamında bean'ler elle `new` ile değil konteyner/mock ile sağlanır; bu slice modeliyle uyumsuzdur.
## B
`@WebMvcTest` tüm servis katmanını gerçek hâliyle yükler; ek bir şey gerekmez
🤖 `@WebMvcTest` servis/repository bean'lerini YÜKLEMEZ; sağlanmazsa bağlam başlamaz.
## C
Servisi `@MockitoBean` ile sahte bir bean olarak sağlamak
🤖 Doğru: slice yalnız web katmanını yükler, servis mock'lanarak izole test edilir.
## D
Controller'ı tümüyle mock'layıp yalnız servisin davranışını doğrulamak
🤖 Test edilen zaten controller'dır; onu mock'lamak testi anlamsız kılar.
## Doğru
C
## Açıklama
`@WebMvcTest` yalnız web katmanını (controller, `@ControllerAdvice`, filtreler, `MockMvc`) yükler; **servis ve repository bean'lerini yüklemez**. Controller bir servise bağımlıysa, o bağımlılık **sahte (mock) bir bean** olarak sağlanmalıdır, yoksa bağlam "bean bulunamadı" ile başlamaz. Bunun için Boot 3.4+ (2026) ile **`@MockitoBean`** anotasyonu kullanılır (eski `@MockBean` kullanımdan kaldırıldı):

```java
@WebMvcTest(KullaniciController.class)
class KullaniciControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean KullaniciServisi servis;

    @Test void kullaniciDoner() throws Exception {
        given(servis.getir(1L)).willReturn(new Kullanici("Ayşe"));
        mockMvc.perform(get("/kullanicilar/1")).andExpect(status().isOk());
    }
}
```

Böylece test yalnız controller davranışını (yönlendirme, serileştirme, durum kodu) izole biçimde doğrular; servisin gerçek mantığı ayrı bir birim testinde sınanır.

# test-4 | Orta
## Soru
`@MockitoBean` (eski `@MockBean`) ile Mockito'nun sıradan `@Mock` anotasyonu arasındaki fark nedir?
## A
İkisi tamamen aynıdır; Spring yalnızca `@Mock`'u yeniden adlandırmıştır
🤖 Aynı değiller; biri Spring bağlamına girer, diğeri girmez.
## B
`@Mock` bağlamdaki gerçek bean'in yerini alır; `@MockitoBean` yalnız yerel kalır
🤖 Ters; bağlamdaki bean'i değiştiren `@MockitoBean`'dir.
## C
`@MockitoBean` yalnız reaktif testlerde, `@Mock` yalnız MVC testlerinde çalışır
🤖 Böyle bir yığın kısıtı yoktur; fark bağlama girip girmemededir.
## D
`@MockitoBean` sahteyi Spring bağlamına bean olarak koyar; `@Mock` koymaz
🤖 Doğru: `@MockitoBean` konteynere enjekte edilir, `@Mock` yalnız yerel bir sahte nesnedir.
## Doğru
D
## Açıklama
Sıradan Mockito **`@Mock`**, yalnız test sınıfında bir sahte nesne oluşturur; Spring bağlamından habersizdir ve konteynere enjekte edilmez. Test edilen sınıf bağımlılığını `@Autowired` ile alıyorsa `@Mock` işe yaramaz. **`@MockitoBean`** (Spring Boot'un sağladığı; Boot 3.4 öncesi adı `@MockBean`) ise sahte nesneyi **Spring uygulama bağlamına bir bean olarak** yerleştirir: bağlamda o tipte gerçek bir bean varsa **onun yerini alır**, yoksa yeni bir bean ekler. Böylece `@Autowired`/constructor ile enjekte edilen her yerde bu sahte kullanılır. Kısaca: `@Mock` = yerel sahte nesne; `@MockitoBean` = bağlama enjekte edilen sahte bean. Slice ve entegrasyon testlerinde bağımlılıkları izole etmek için `@MockitoBean` kullanılır.

# test-5 | İleri
## Soru
Bir ekip repository testlerinde H2 gömülü veritabanı kullanıyor; testler yeşil ama üretimde (PostgreSQL) bazı sorgular patlıyor. 2026'da önerilen çözüm nedir?
## A
Testte üretimdekiyle aynı veritabanını Testcontainers ile çalıştırmak
🤖 Doğru: gerçek motorla test, lehçe/kısıt farklarından doğan üretim sürprizlerini önler.
## B
H2'yi PostgreSQL uyumluluk moduna almak her durumda yeterli bir çözümdür
🤖 Uyumluluk modu farkları azaltır ama tam taklit etmez; lehçe/kısıt/işlev farkları kalır.
## C
Repository testlerini tamamen kaldırıp yalnız servis testlerine güvenmek
🤖 Testi silmek sorunu çözmez, görünmez kılar; kapsama düşer.
## D
Üretim veritabanına bağlanıp testleri doğrudan orada çalıştırmak
🤖 Üretim verisine test yazmak veriyi bozar ve tehlikelidir; asla yapılmaz.
## Doğru
A
## Açıklama
H2 gibi gömülü veritabanları hızlıdır ama üretimdeki motorun (**PostgreSQL/MySQL**) SQL lehçesini, tip sistemini, kısıtlarını, indeks davranışını, `JSONB`/`ARRAY` gibi özel tiplerini ve fonksiyonlarını **tam taklit etmez**. Bu yüzden H2'de geçen bir test, gerçek veritabanında başarısız olabilir — testin güven verme amacı boşa çıkar. 2026 pratiği **Testcontainers**'tır: test sırasında **üretimdekiyle aynı** veritabanı imajını bir Docker konteynerinde başlatır, test bitince temizler. Spring Boot 3.1+ ile `@ServiceConnection` sayesinde bağlantı özellikleri otomatik enjekte edilir:

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
class SiparisRepositoryTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16");
}
```

Böylece test, üretim veritabanıyla aynı davranış üzerinde çalışır. Sürprizler test aşamasında yakalanır.

# test-6 | İleri
## Soru
Testlerde üretimden farklı yapılandırma (örn. test veritabanı, kapalı planlayıcı) kullanmanın temiz yolu nedir?
## A
Test sırasında `application.properties`'i elle düzenleyip test sonrası geri almak
🤖 Elle düzenleme kırılgan, yarışa açık ve otomasyona uygun değildir; profil mekanizması bunun için vardır.
## B
Üretim yapılandırmasını test için değiştirmek mümkün değildir; testler üretim ayarıyla çalışmak zorundadır
🤖 Yanlış; Spring profilleri ve test kaynakları tam da bu ayrımı sağlar.
## C
Bir "test" profili tanımlayıp `@ActiveProfiles("test")` ile etkinleştirmek
🤖 Doğru: `application-test.yml` içindeki ortama özgü yapılandırma temiz ve tekrarlanabilir biçimde ayrılır.
## D
Her test sınıfı için ayrı bir Spring Boot uygulaması yazmak
🤖 Aşırı ve gereksiz; profiller tek uygulamada ortam ayrımını zaten sağlar.
## Doğru
C
## Açıklama
Spring'in **profil** (profile) mekanizması, ortama özgü yapılandırmayı temiz biçimde ayırır. Test için özel değerleri `src/test/resources/application-test.yml` (veya `application-test.properties`) içine koyarsınız — test veritabanı URL'i, kapalı planlayıcı, sahte dış servis adresleri gibi. Test sınıfında **`@ActiveProfiles("test")`** ile bu profili etkinleştirirsiniz; Spring hem varsayılan `application.yml`'i hem de `-test` ekli dosyayı birleştirir ve test değerleri üsttedir. Belirli bean'leri yalnız bir profilde oluşturmak için ilgili yapılandırmaya `@Profile("test")` konur. Bu, dosyaları elle düzenlemekten (A, yarış ve kirlilik riski) çok daha güvenli ve tekrarlanabilirdir; ayrıca CI'da deterministik çalışır.
