---
slug: spring-boot-autoconfig
baslik: "Spring Boot Auto-Configuration: Starter Mantığı, @EnableAutoConfiguration, Override ve Debug Raporu"
kademe: Orta
sira: 73
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Yeni bir eve taşındığını düşün. Bir seçenek: elektrik, su, internet, mobilya — hepsini tek tek sen ayarlarsın. Diğer seçenek: kapıdan girersin, ışıklar yanıyor, su akıyor, internet bağlı; çünkü biri "herkesin isteyeceği makul varsayılanları" önceden kurmuş. Beğenmediğin bir şey olursa yalnız onu değiştirirsin. Spring Boot'un **auto-configuration**'ı tam olarak bu ikinci evdir: eskiden bir web uygulaması için onlarca ayar elle yazılırken, Boot bunu "sen bir şey demezsen ben makul olanı kurarım, dersen çekilirim" ilkesine indirger.

### Temel kavramlar

- **Kütüphane (library) / bağımlılık (dependency):** Başkasının yazdığı, projene dahil ettiğin hazır kod paketi. Java'da bunlar `.jar` dosyalarıdır. Projenin hangi paketleri kullandığını Maven (`pom.xml`) ya da Gradle dosyasına yazarsın; oraya yazılan her satır bir **bağımlılıktır**.
- **Classpath:** Uygulama çalışırken JVM'in "hangi sınıflar mevcut" diye baktığı liste. Bir bağımlılık eklediğinde onun sınıfları classpath'e girer. Boot'un sihri büyük ölçüde "classpath'te şu sınıf var mı?" sorusuna dayanır.
- **Bean:** Spring'in senin adına yaratıp bellekte tuttuğu ve ihtiyaç duyan yerlere kendisi verdiği nesne. `ObjectMapper` (JSON çevirici) veya `DataSource` (veritabanı bağlantı havuzu) tipik bean'lerdir.
- **Anotasyon (annotation):** `@` ile başlayan, sınıf/metot/alan üstüne yazılan etiket. Kendi başına kod çalıştırmaz; "bu şey şudur" bilgisini taşır, Spring bu etiketleri okuyup davranışını kurar.
- **Starter:** Birlikte kullanılması gereken kütüphaneleri **uyumlu sürümleriyle** tek bir Maven/Gradle koordinatında toplayan **bağımlılık demeti**. `spring-boot-starter-web` çekilince beraberinde Spring MVC, gömülü Tomcat ve Jackson gelir. İçinde **iş kodu yoktur**: `main` metodu barındırmaz, JVM eklentisi değildir, auto-config'i çalıştıran bir motor da değildir. Tek işi bağımlılık yönetimini basitleştirmektir.
- **Auto-configuration:** Classpath'te ne olduğuna bakıp **makul varsayılan bean'leri** senin yerine tanımlayan mekanizma. `@SpringBootApplication` içindeki **`@EnableAutoConfiguration`** bunu tetikler.
- **`@Conditional` ailesi:** Auto-config sınıflarının hepsi koşulludur; her biri hangi durumda devreye gireceğini açıkça beyan eder. Başlıcaları: `@ConditionalOnClass` (ilgili kütüphane classpath'te varsa), `@ConditionalOnMissingBean` (sen o bean'i tanımlamadıysan; senin bean'in önceliklidir), `@ConditionalOnProperty` (bir ayar açıksa). Ayrıca `@ConditionalOnMissingClass` ve `@ConditionalOnWebApplication` gibileri de vardır. Bean yalnız **tüm** koşullar sağlanınca kurulur. Sonuç rastgele değil, aynı girdiyle hep aynıdır (deterministik); profile bağlı değildir ve kullanıcının her bean'i elle açması gerekmez.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Auto-config karar zinciri ve override">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Auto-config bean'i ne zaman koyar?</text>
  <rect x="30" y="42" width="180" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="120" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">@ConditionalOnClass</text>
  <text x="120" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kütüphane classpath'te mi?</text>
  <rect x="230" y="42" width="180" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">@ConditionalOnMissingBean</text>
  <text x="320" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sen tanımlamadın mı?</text>
  <rect x="430" y="42" width="180" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="520" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">varsayılan bean</text>
  <text x="520" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kurulur</text>
  <text x="216" y="72" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <text x="416" y="72" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <text x="320" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#976f00" font-family="system-ui">Override = kendi bean'ini tanımla</text>
  <text x="320" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">senin bean'in varsa @ConditionalOnMissingBean geri çekilir</text>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">--debug → hangi auto-config eşleşti/eşleşmedi raporu</text>
</svg>
```

### Nasıl çalışır?

Ana sınıfın tipik olarak şöyledir:

```java
@SpringBootApplication
public class UygulamaMain {
    public static void main(String[] args) {
        SpringApplication.run(UygulamaMain.class, args);
    }
}
```

`@SpringBootApplication` tek başına iş yapmaz; **üç anotasyonu birleştiren bileşik (composed) bir anotasyondur**:

1. `@SpringBootConfiguration` — bir `@Configuration` çeşidi; sınıfın bean tanımı barındırabileceğini söyler.
2. `@ComponentScan` — senin yazdığın `@Service`, `@Repository`, `@Controller` sınıflarını bulunduğu paketten aşağı doğru tarar.
3. `@EnableAutoConfiguration` — auto-configuration zincirini tetikleyen anotasyon budur.

Yani ana sınıfına `@SpringBootApplication` yazdığın anda auto-configuration **örtük olarak zaten açıktır**. Dikkat: `@Configuration` yalnız bean tanımı içindir, `@ComponentScan` yalnız senin sınıflarını tarar, `@Bean` yalnız tek bir bean üretir — hiçbiri auto-config zincirini başlatmaz.

Sonra sıra koşullara gelir: `spring-boot-starter-web` bağımlılığını eklediğin an zincir şöyle işler.

```svg
<svg viewBox="0 0 640 176" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Starter classpath'e kütüphane getirir, auto-config koşulu tetiklenir">
  <defs>
    <marker id="sbac-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Starter ile auto-config birbirini tamamlar</text>
  <rect x="20" y="52" width="150" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">starter-web</text>
  <text x="95" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımlılık demeti</text>
  <rect x="210" y="52" width="150" height="66" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="285" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">classpath</text>
  <text x="285" y="95" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Tomcat · MVC · Jackson</text>
  <rect x="400" y="52" width="220" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="510" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">@ConditionalOnClass eşleşir</text>
  <text x="510" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">auto-config bean'leri kurulur</text>
  <line x1="170" y1="85" x2="208" y2="85" stroke="#186399" stroke-width="1.7" marker-end="url(#sbac-arw)"/>
  <line x1="360" y1="85" x2="398" y2="85" stroke="#186399" stroke-width="1.7" marker-end="url(#sbac-arw)"/>
  <text x="320" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Starter kod içermez; getirdiği kütüphaneler koşulları tetikler</text>
</svg>
```

**Override (ezme) nasıl yapılır?** Auto-config bean'lerinin çoğu `@ConditionalOnMissingBean` ile korunur: "kullanıcı bu tipte bir bean tanımlamadıysa benimkini kur." Öyleyse Boot'un varsayılan `ObjectMapper`'ı yerine kendi yapılandırdığını kullanmanın en temiz yolu basittir: kendi bean'ini tanımlarsın.

```java
@Configuration
public class JsonAyarlari {
    @Bean
    ObjectMapper objectMapper() {      // senin bean'in
        return JsonMapper.builder().findAndAddModules().build();
    }
}
```

Sen bu bean'i tanımladığın an Boot'un varsayılanı koşulunu sağlamaz ve **geri çekilir**. Bu yol alternatiflerinin hepsinden temizdir. Kütüphanenin JAR'ını kurcalamana gerek kalmaz. `@EnableAutoConfiguration`'ı tümüyle kapatıp diğer yararlı varsayılanları da kaybetmezsin. Belirsiz bir ayar bayrağına (property flag) güvenmek zorunda kalmazsın. Tek bir auto-config sınıfını bütünüyle dışlamak istersen ayrı bir seçenek var: `@SpringBootApplication(exclude = ...)`.

**Boot bu sınıfları nereden bulur?** Auto-config sınıfları senin component-scan paketinin **dışında** yaşar; normal tarama onları bulamaz. Bu yüzden her kütüphane auto-config sınıflarını özel bir kayıt dosyasında ilan eder.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Auto-config kayıt dosyası: spring.factories'ten AutoConfiguration.imports'a geçiş">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Auto-config nereden keşfedilir?</text>
  <rect x="26" y="44" width="278" height="112" rx="11" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">ESKİ (Boot &lt; 2.7)</text>
  <text x="165" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">META-INF/spring.factories</text>
  <text x="165" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EnableAutoConfiguration=...</text>
  <text x="165" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Boot 3.x'te KALDIRILDI</text>
  <rect x="336" y="44" width="278" height="112" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">GÜNCEL (Boot 2.7+/3.x)</text>
  <text x="475" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">META-INF/spring/</text>
  <text x="475" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">...AutoConfiguration.imports</text>
  <text x="475" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her satır bir @AutoConfiguration sınıfı</text>
</svg>
```

Eski yöntem `META-INF/spring.factories` dosyasındaki `org.springframework.boot.autoconfigure.EnableAutoConfiguration=...` anahtarıydı. Bu yöntem **Boot 2.7'de kullanımdan kaldırıldı (deprecated)** ve **Boot 3.x'te tamamen KALDIRILDI**.

Güncel yol `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` dosyasıdır. Buraya **her satıra bir** auto-config sınıfının tam nitelikli adı yazılır; sınıfın kendisi de `@AutoConfiguration` ile işaretlenir. `application.properties`'e `autoconfig.classes=...` gibi bir satır yazmak işe yaramaz; sınıfı `@Component` yapıp taramaya güvenmek de bu kayıt yerine geçmez.

**"Bu bean neden kuruldu, şu neden kurulmadı?"** Tahmin etme; Boot bunun raporunu üretir. Uygulamayı `--debug` argümanıyla çalıştır ya da `application.properties`'e `debug=true` ekle. Çıktıda iki liste görürsün: **Positive matches** (koşulları sağlanıp devreye giren auto-config'ler) ve **Negative matches** (hangi koşul sağlanmadığı için atlananlar).

`spring-boot-actuator` varsa aynı bilgiyi `/actuator/conditions` uç noktası (endpoint) da verir. Bu bir **çalışma anı** kararıdır: yeniden derlemek onu göstermez, hata ayıklayıcıyla (debugger) bean'leri tek tek izlemek de doğru araç değildir.

### Mülakatta ne sorulur?

- **Starter nedir?** Bağımlılık demeti — içinde kod yok, `main` yok, motor yok. Getirdiği kütüphaneler auto-config koşullarını tetikler.
- **Auto-config'i ne tetikler?** `@EnableAutoConfiguration`; pratikte `@SpringBootApplication` içinde gelir. Üç bileşenini sayabil: `@SpringBootConfiguration` + `@ComponentScan` + `@EnableAutoConfiguration`.
- **Seçici davranışın kaynağı?** `@Conditional` ailesi; rastgelelik veya profil bağımlılığı yoktur.
- **Override'ın idiomatik yolu?** Kendi `@Bean`'ini tanımla — `@ConditionalOnMissingBean` sayesinde varsayılan geri çekilir. Tümden dışlama için `exclude`.
- **Kendi starter'ında kayıt dosyası?** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. `spring.factories` Boot 3'te **kaldırıldı** — en sık düşülen tuzak budur.
- **Teşhis aracı?** `--debug` → conditions report (positive/negative matches), ya da `/actuator/conditions`.

### Tek cümlede

Starter uyumlu kütüphaneleri classpath'e taşır, auto-configuration `@Conditional` koşullarıyla "sen tanımlamadıysan makul varsayılanı" kurar ve sen kendi bean'ini yazdığın an sessizce geri çekilir.

# auto-1 | Temel
## Soru
`spring-boot-starter-web` gibi bir "starter" tam olarak nedir?
## A
Uygulamayı başlatan `main` metodunu ve `SpringApplication.run` çağrısını içeren özel bir sınıftır
🤖 Starter bir sınıf değil; bir bağımlılık demetidir. `main` ayrı bir konudur.
## B
Birlikte kullanılan kütüphaneleri tek koordinatta toplayan bir bağımlılık demetidir
🤖 Doğru: starter = uyumlu bağımlılıkların hazır demeti (dependency descriptor); içinde iş kodu yoktur.
## C
Uygulamanın açılış hızını artırmak için JVM'e takılan bir eklentidir
🤖 JVM eklentisi değildir; bağımlılık yönetimi kolaylığıdır.
## D
Auto-configuration sınıflarını sırayla çalıştıran runtime motorudur
🤖 Motor değildir; auto-config ayrı bir mekanizmadır, starter yalnız bağımlılık getirir.
## Doğru
B
## Açıklama
Bir **starter**, içinde genelde **hiç kod bulunmayan**, yalnızca birlikte kullanılması mantıklı kütüphaneleri (ve uyumlu sürümlerini) tek bir Maven/Gradle koordinatında toplayan bir **bağımlılık tanımıdır**. Örneğin `spring-boot-starter-web` çekildiğinde beraberinde Spring MVC, gömülü Tomcat, Jackson gibi bağımlılıklar gelir — sen tek satır ekler, uyumlu bir web yığınına kavuşursun. Starter'ın kendisi `main` içermez (A), JVM eklentisi değildir (C) ve auto-config'i "çalıştıran motor" da değildir (D). Ama classpath'e getirdiği kütüphaneler, auto-configuration'ın `@ConditionalOnClass` koşullarını **tetikleyen** şeydir — starter ile auto-config birbirini böyle tamamlar.

# auto-2 | Temel
## Soru
Auto-configuration mekanizmasını tetikleyen anotasyon hangisidir ve tipik uygulamada nerede bulunur?
## A
`@EnableAutoConfiguration`; pratikte `@SpringBootApplication` içinde yer alır
🤖 Doğru: @SpringBootApplication = @EnableAutoConfiguration + @ComponentScan + @SpringBootConfiguration.
## B
`@Configuration`; bir sınıf yapılandırma sınıfı olarak işaretlendiği anda auto-config zinciri de açılır
🤖 `@Configuration` bean tanımı içindir; auto-config'i açan `@EnableAutoConfiguration`'dır.
## C
`@ComponentScan`; paket taraması başladığı anda auto-config de kendiliğinden devreye girer
🤖 Component scan kendi bean'lerini tarar; auto-config'i tetikleyen ayrı anotasyondur.
## D
`@Bean`; ilk bean tanımlandığı anda auto-config zinciri başlar
🤖 `@Bean` tek bir bean üretir; auto-config zincirini başlatmaz.
## Doğru
A
## Açıklama
Auto-configuration'ı **`@EnableAutoConfiguration`** açar. Modern uygulamada bunu doğrudan yazmazsın; ana sınıftaki **`@SpringBootApplication`** üç anotasyonu birleştirir: `@SpringBootConfiguration` (bir `@Configuration` çeşidi), `@ComponentScan` (kendi bean'lerini tarar) ve `@EnableAutoConfiguration` (auto-config'i tetikler). `@Configuration` (B) yalnız bean tanımı için, `@ComponentScan` (C) kendi sınıflarını taramak için, `@Bean` (D) tek bir bean üretmek içindir — hiçbiri auto-config zincirini başlatmaz. Kısaca: `@SpringBootApplication` yazınca `@EnableAutoConfiguration` örtük olarak zaten aktiftir.

# auto-3 | Orta
## Soru
Auto-configuration, classpath'te bir kütüphane olsa bile ilgili bean'i her zaman kurmuyor. Bu seçici davranışı sağlayan mekanizma nedir?
## A
Auto-config sınıfları rastgele yarısını kurar, dengeyi böyle sağlar
🤖 Rastgele değildir; her adım açıkça bir koşula bağlıdır.
## B
Kullanıcı ihtiyaç duyduğu her bean'i `application.properties`'te tek tek açmak zorundadır, hiçbiri kendiliğinden gelmez
🤖 Çoğu varsayılan otomatik gelir; tek tek açma zorunluluğu yoktur.
## C
Her auto-config sınıfı `@Conditional` ailesiyle işaretlidir; bean yalnız koşulları sağlanınca kurulur
🤖 Doğru: koşullu kayıt auto-config'in çekirdeğidir (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`…).
## D
Bean'ler yalnız `prod` profilinde kurulur, diğer profillerde tümüyle atlanır
🤖 Profil bağımlı değildir; koşullar classpath/property/mevcut bean'e bakar.
## Doğru
C
## Açıklama
Auto-configuration'ın tamamı **`@Conditional` ailesiyle** yönetilir. Her auto-config sınıfı/bean'i, "hangi durumda devreye gireceğini" açıkça beyan eder: `@ConditionalOnClass` (ilgili kütüphane classpath'te varsa), `@ConditionalOnMissingBean` (kullanıcı o bean'i tanımlamadıysa), `@ConditionalOnProperty` (bir ayar açıksa), `@ConditionalOnMissingClass`, `@ConditionalOnWebApplication` vb. Bean yalnız **tüm** koşulları sağlanınca kurulur. Bu yüzden davranış deterministiktir (A yanlış), kullanıcının her şeyi elle açmasına gerek yoktur (B yanlış) ve profile bağlı değildir (D yanlış). Bu koşullu tasarım, hem "makul varsayılan" hem de "kullanıcı ezebilir" özelliğini aynı anda mümkün kılar.

# auto-4 | Orta
## Soru
Spring Boot senin için varsayılan bir `ObjectMapper` kuruyor ama sen özel yapılandırdığın kendi `ObjectMapper`'ını kullanmak istiyorsun. En doğru yol nedir?
## A
Boot'un JAR'ından auto-config sınıfını silmek
🤖 Kütüphaneyi kurcalamak gereksiz ve tehlikeli; override çok daha temiz bir yolu var.
## B
`@EnableAutoConfiguration`'ı tamamen kapatmak
🤖 Tümünü kapatmak diğer tüm varsayılanları da kaybettirir; aşırı ve yanlış çözüm.
## C
Aynı özelliği `application.properties`'te bir flag ile devre dışı bırakmayı ummak
🤖 Genel bir flag garanti değildir; asıl idiomatik yol kendi bean'ini tanımlamaktır.
## D
Kendi `@Bean ObjectMapper` tanımını uygulamaya eklemek
🤖 Doğru: `@ConditionalOnMissingBean` sayesinde senin bean'in varsa auto-config varsayılanı devreye girmez.
## Doğru
D
## Açıklama
Auto-config bean'lerinin çoğu `@ConditionalOnMissingBean` ile korunur: "kullanıcı bu tipte bir bean **tanımlamadıysa** benimkini kur." Dolayısıyla override etmenin idiomatik yolu, basitçe **kendi bean'ini tanımlamaktır** — sen bir `ObjectMapper` `@Bean`'i yazınca Boot'un varsayılanı koşulu sağlamadığı için **geri çekilir** ve seninki kullanılır. Bu, kütüphaneyi kurcalamaktan (A) veya tüm auto-config'i kapatıp (B) diğer yararlı varsayılanları da kaybetmekten çok daha temizdir; belirsiz bir property flag'e güvenmek (C) de gerekmez. Belirli bir auto-config'i tümüyle dışlamak istersen `@SpringBootApplication(exclude = ...)` ayrı bir seçenektir, ama tekil override için kendi bean'in yeter.

# auto-5 | İleri
## Soru
Kendi Spring Boot starter'ını yazıyorsun. Güncel Boot sürümlerinde (2.7 ve sonrası) auto-configuration sınıflarını Boot'un keşfetmesi için hangi dosyayı kullanırsın?
## A
`META-INF/spring.factories` içinde `EnableAutoConfiguration` anahtarına sınıfı eklemek — bu dosya bugün de auto-config keşfinin tek resmî yoludur
🤖 Bu ESKİ yöntem; 2.7'de kullanımdan kalktı, 3.x'te tamamen kaldırıldı.
## B
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` dosyasına sınıf adını yazmak
🤖 Doğru: Boot 2.7 ve sonrasının (3.x/4.x) auto-config keşif dosyası budur; her satıra bir sınıfın tam adı yazılır.
## C
`application.properties`'e `autoconfig.classes=...` satırı eklemek
🤖 Böyle bir property yok; keşif properties'ten değil imports dosyasından yapılır.
## D
Sınıfı `@Component` ile işaretleyip component scan'e güvenmek
🤖 Auto-config kullanıcının tarama paketi dışındadır; imports dosyasıyla kaydedilir.
## Doğru
B
## Açıklama
Auto-config sınıfları, kullanıcının component-scan paketinin **dışında** olduğu için normal tarama ile bulunmaz; Boot onları özel bir kayıt dosyasından okur. Eski yöntem `META-INF/spring.factories` içindeki `org.springframework.boot.autoconfigure.EnableAutoConfiguration=...` anahtarıydı (A) — ancak bu **Boot 2.7'de deprecated** oldu ve **Boot 3.x'te kaldırıldı**. Güncel yol: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` dosyasına, **her satıra bir** auto-config sınıfının tam nitelikli adını yazmaktır. Sınıf ayrıca `@AutoConfiguration` ile işaretlenir. properties (C) veya `@Component`+scan (D) bu kayıt için kullanılmaz.

# auto-6 | İleri
## Soru
"Neden bu bean kuruldu / şu bean neden kurulmadı?" sorusunu Spring Boot'ta hangi araç yanıtlar?
## A
Uygulamayı yeniden derlemek; auto-config kararları yalnız derleyici log'unda görünür
🤖 Kararlar derleme değil çalışma zamanı olayıdır; derleyici log'unda görünmez.
## B
`@Autowired` alanlarına breakpoint koyup debugger ile bean'leri tek tek izlemek
🤖 Mümkün ama araç bu değil; Boot'un hazır bir koşul raporu vardır.
## C
Uygulamayı `--debug` ile çalıştırıp conditions report çıktısını okumak
🤖 Doğru: `--debug` (veya `debug=true`) koşul raporunu açar; "positive/negative matches" listelenir.
## D
`application.properties`'ten tüm auto-config'i kapatıp tek tek açarak deneme-yanılma yapmak
🤖 Kaba ve bilgi vermeyen bir yöntem; hazır rapor zaten cevabı verir.
## Doğru
C
## Açıklama
Spring Boot, auto-configuration kararlarını açıklayan hazır bir **conditions report** (auto-configuration report) üretir. Uygulamayı `--debug` argümanıyla veya `application.properties`'e `debug=true` ekleyerek çalıştırdığında, çıktıda **Positive matches** (koşulları sağlanıp devreye giren auto-config'ler) ve **Negative matches** (hangi koşul sağlanmadığı için atlananlar) listelenir — böylece "bean neden var/yok" sorusunu tahminle değil, kanıtla yanıtlarsın. Ayrıca `spring-boot-actuator` varsa `/actuator/conditions` endpoint'i aynı bilgiyi verir. Yeniden derleme (A) çalışma zamanı kararını göstermez, debugger (B) araç değil son çare, tümünü kapatıp deneme (D) ise bilgi vermeyen kaba bir yaklaşımdır.
