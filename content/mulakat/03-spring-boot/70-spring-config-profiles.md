---
slug: spring-config-profiles
baslik: "Spring Yapılandırma: Java Config, @Bean vs @Component, @Profile ve @Conditional"
kademe: Orta
sira: 70
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir tiyatro oyunu düşün: sahnede kimlerin olacağını yönetmen belirler ve kadro provada başka, galada başka olabilir — provada ucuz dekor, galada gerçeği. Spring'de "hangi nesneler var olacak, nasıl kurulacak, hangi ortamda hangisi devreye girecek" sorusunun cevabına **yapılandırma** (configuration) denir. Amaç, ortam farklarını koda serpiştirilmiş `if` bloklarıyla değil, tek bir kadro listesiyle yönetmektir.

### Temel kavramlar

- **Konteyner (container) ve bean**: container Spring'in nesne yöneticisidir; onun kurup yönettiği her nesne bir **bean**'dir.
- **Paket (package)**: Java'da sınıfları gruplayan, klasöre benzeyen isim alanı (`tr.velzon.servis`).
- **Sınıf yolu (classpath)**: uygulamanın çalışırken görebildiği tüm sınıf ve JAR'ların listesi.
- **Üçüncü parti sınıf**: senin yazmadığın, kaynağına anotasyon **ekleyemediğin** kütüphane sınıfı (`ObjectMapper`, `DataSource`, `RestClient`).
- **Stereotype anotasyon**: `@Component` ve türevleri `@Service`, `@Repository`, `@Controller` — "beni bean yap" işareti. Rol belirtirler, temel davranışları aynıdır.
- **Component scan**: Spring'in belirtilen paket ağacını dolaşıp stereotype anotasyonlu sınıfları bulup bean olarak kaydetmesi.
- **Vekil (proxy)**: gerçek nesnenin önüne konan, dışarıdan aynı tip görünen ama çağrıyı yakalayan sarmalayıcı. **CGLIB** bunu hedef sınıfın **alt sınıfını** üreterek yapar.
- **Profil (profile)**: `dev`, `test`, `prod` gibi ortam etiketi.

Bean tanımlamanın iki üslubu vardır:

```svg
<svg viewBox="0 0 640 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="component scan vs Java config bean tanımlama">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bean'i kim tanımlar?</text>
  <rect x="24" y="44" width="288" height="146" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="168" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">@Component + component scan</text>
  <text x="168" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sınıf düzeyi · Spring tarar</text>
  <text x="168" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@Service @Repository @Controller</text>
  <text x="168" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">SENİN sınıfların için</text>
  <text x="168" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaynağını değiştirebildiğin</text>
  <text x="168" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kısa · otomatik keşif</text>
  <rect x="330" y="44" width="286" height="146" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="473" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">@Configuration + @Bean</text>
  <text x="473" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">metot düzeyi · elle kurar</text>
  <text x="473" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@Bean Repo repo() { return ... }</text>
  <text x="473" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ÜÇÜNCÜ PARTİ sınıflar için</text>
  <text x="473" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ObjectMapper, DataSource…</text>
  <text x="473" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">programatik kurulum</text>
</svg>
```

`@Component` **sınıf** düzeyindedir ve taramayla bulunur; kaynağını değiştirebildiğin **kendi** sınıfların içindir. `@Bean` ise bir `@Configuration` sınıfındaki **metot** üzerindedir ve nesneyi **elle** kurup döndürür. Anotasyon ekleyemediğin üçüncü parti sınıflar ve programatik kurulum gerektiren bean'ler için tek yol budur.

İkisi de varsayılan olarak **tek örnekli** (singleton — container başına bir nesne) bean üretir; başka bir kapsam istiyorsan ayrıca belirtirsin. `@Component` gerçek bir bean üretir, salt dokümantasyon işareti değildir. Kendi sınıfların için XML de gerekmez.

### Nasıl çalışır?

Component scan verilen **temel paketlerin** ağacını dolaşır. Yalnız stereotype anotasyonlu sınıfları (`@Configuration` dahil) kaydeder; sınıf yolundaki tüm JAR'ları ya da her public sınıfı değil.

Spring Boot'ta `@SpringBootApplication` örtük bir `@ComponentScan` taşır ve taramayı **ana sınıfın bulunduğu paketten** başlatır. Bean'lerini bu paketin altına koymazsan hiç keşfedilmezler; klasik "bean bulunamadı" tuzağı buradan çıkar. Taramanın kullanılmayan bean'leri bellekten düşürmek gibi bir işi yoktur; olay yalnızca başlangıçtaki keşiftir.

Java config şöyle görünür:

```java
@Configuration
class Ayar {
    @Bean Repo repo() { return new Repo(); }
    @Bean Servis servis() { return new Servis(repo()); }   // @Bean metodunu İÇERİDEN çağırıyor
}
```

Kritik ayrıntı şu: `@Configuration` sınıflarının önüne CGLIB ile bir **vekil nesne (proxy)** konur (`proxyBeanMethods = true` varsayılan). Vekil, `repo()` çağrısını yakalar. Metot gövdesini yeniden çalıştırmak yerine container'a gidip **var olan tek örneği (singleton)** döndürür. Yani yukarıda **tek** bir `Repo` oluşur; hem `repo` bean'i hem `servis`'in bağımlılığı aynısını paylaşır. Bir `@Bean` metodunu içeriden çağırmak yasak değildir, container sorunsuz başlar.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="@Configuration CGLIB proxy davranışı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@Bean metodunu içeriden çağırınca</text>
  <rect x="30" y="46" width="272" height="132" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="166" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">proxyBeanMethods = true</text>
  <text x="166" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(varsayılan · CGLIB proxy)</text>
  <text x="166" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">config.repo() çağrısı →</text>
  <text x="166" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">container'daki AYNI singleton</text>
  <text x="166" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek örnek paylaşılır</text>
  <rect x="338" y="46" width="272" height="132" rx="12" fill="#FBF4E6" stroke="#B98900" stroke-width="1.5"/>
  <text x="474" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#976f00" font-family="system-ui">proxyBeanMethods = false</text>
  <text x="474" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(proxy YOK · düz çağrı)</text>
  <text x="474" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">config.repo() çağrısı →</text>
  <text x="474" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">HER çağrıda YENİ nesne</text>
  <text x="474" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">singleton garantisi yok</text>
</svg>
```

`@Configuration(proxyBeanMethods = false)` — "hafif (lite) mod" da denir — bu vekili **kapatır**. Kazancı açıktır: vekil üretme maliyeti kalkar, başlangıç hızlanır, bellek düşer. `@Bean` metotları birbirini **çağırmayan** bağımsız yapılandırmalar için idealdir; Boot'un kendi otomatik yapılandırma (auto-configuration) sınıfları çoğunlukla böyle yazılır.

Bedeli de var: iç çağrı artık düz bir Java çağrısıdır. **Her çağrıda yeni nesne** üretir ve tek örnek garantisi kaybolur; yukarıdaki örnekte **iki ayrı** `Repo` oluşur. Bu ayarın başka hiçbir yan etkisi yoktur: erişim belirleyicisini değiştirmez, iş parçacığı güvenliği (thread-safety) sağlamaz, derleme zamanında hiçbir şey sabitlemez. Tek mesele çağrıyı yakalayan vekilin var olup olmamasıdır.

Ortama göre bean seçimi iki mekanizmayla yapılır:

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Profile ortam ayrımı ve Conditional ailesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Koşullu bean kaydı</text>
  <text x="128" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">@Profile ile ortam ayrımı</text>
  <rect x="28" y="64" width="200" height="42" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="128" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@Profile("dev")</text>
  <text x="128" y="99" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">in-memory sahte Repo</text>
  <rect x="28" y="116" width="200" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="128" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@Profile("prod")</text>
  <text x="128" y="151" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gerçek managed Repo</text>
  <text x="128" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">spring.profiles.active seçer</text>
  <text x="128" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eşleşmezse HİÇ kaydedilmez</text>
  <rect x="272" y="52" width="344" height="146" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="444" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">@Conditional ailesi (özelleştirilebilir kural)</text>
  <text x="444" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@ConditionalOnMissingBean → varsayılan ver</text>
  <text x="444" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@ConditionalOnProperty → ayar açıksa</text>
  <text x="444" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@ConditionalOnClass → sınıf classpath'te ise</text>
  <text x="444" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@ConditionalOnBean → başka bean varsa</text>
  <text x="444" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Boot auto-configuration'ın bel kemiği</text>
</svg>
```

`@Profile("prod")`, bir bean'i (veya tüm bir `@Configuration` sınıfını) **yalnız** o profil aktifken kaydeder. Eşleşme yoksa bean **hiç** oluşturulmaz. Aktif profil `spring.profiles.active` ile belirlenir; bunu properties dosyasından, ortam değişkeninden veya komut satırından verebilirsin.

Anlamı "bu profilde aktif"tir, dışlama değil. Dışlamak için `@Profile("!prod")` yazılır; bu da "prod dışındaki her profilde" demektir. Bu bir Java anotasyonudur: properties dosyasındaki ayarları filtrelemez, kod düzeyinde bean kaydını yönetir. Böylece aynı arayüzün dev'de sahte (bellek içi — in-memory), prod'da gerçek implementasyonunu koşulsuz `if` yazmadan seçersin.

`@Conditional` ailesi koşulu daha ince, **özelleştirilebilir** kurallara bağlar: `@ConditionalOnMissingBean` (aynı tipte başka bean yoksa kaydet), `@ConditionalOnProperty` (ayar açıksa), `@ConditionalOnClass` (sınıf classpath'te ise), `@ConditionalOnBean` (başka bir bean varsa).

Kütüphane yazarken sık kullanılan bir desen var: "makul varsayılanı ver, ama kullanıcı kendi bean'ini tanımlarsa **geri çekil**". Bunun karşılığı tam olarak `@ConditionalOnMissingBean`'dir. `@Primary` bunun tersini yapar; çakışmada seninkini öne alıp kullanıcının bean'ini ezer. Aynı tipi iki yerde tanımlayıp Spring'in doğrusunu seçmesini beklemek ise yalnızca belirsizlik üretir. Spring Boot'un otomatik yapılandırmasının tamamı bu aileye dayanır; hazır paketler (starter) `DataSource`, `ObjectMapper` gibi bean'ler için tam böyle davranır.

### Mülakatta ne sorulur?

- **`@Component` vs `@Bean`** → sınıf düzeyi + tarama vs metot düzeyi + elle kurulum; `@Bean` üçüncü parti ve programatik kurulum için.
- **Component scan nereden başlar?** → `@SpringBootApplication`'ın bulunduğu paketten; dışarıdaki bean'ler bulunmaz.
- **`@Configuration` içinden `@Bean` çağırınca kaç örnek?** → varsayılanda **tek** (CGLIB proxy singleton'a yönlendirir); `proxyBeanMethods = false` ile **her çağrıda yeni**.
- **`proxyBeanMethods = false` ne zaman?** → `@Bean` metotları birbirini çağırmayan config'lerde; başlangıç maliyetini düşürür, iç çağrı varsa tehlikelidir.
- **`@Profile` vs `@Conditional`** → biri profil aktifliğine, diğeri özelleştirilebilir kurala bakar; "kullanıcı vermediyse varsayılanı ver" = `@ConditionalOnMissingBean`.
- **`@Profile("!prod")`** → "prod dışındaki her profilde".

### Tek cümlede

Yapılandırma "hangi bean var olacak ve nasıl kurulacak" sorusunu yanıtlar; `@Component` kendi sınıfların, `@Bean` yabancı sınıflar, `@Profile`/`@Conditional` ise koşullu kadro içindir.

# cfg-1 | Temel
## Soru
`@Component` ile `@Bean` arasındaki temel fark nedir?
## A
`@Component` sınıf düzeyindedir ve taramayla bulunur; `@Bean` nesneyi elle kuran bir metottur
🤖 Doğru: biri kendi sınıfını tarayarak, diğeri metotla elle bean üretir.
## B
İkisi de yalnız üçüncü parti sınıflar için kullanılır; kendi yazdığın sınıfları bean yapmak için XML gerekir
🤖 `@Component` tam da kendi sınıfların için vardır; kısıt yanlış.
## C
`@Bean` yalnız prototype kapsamlı bean üretir; `@Component` yalnız singleton üretir ve kapsamı değiştirilemez
🤖 İkisi de varsayılan singleton üretir; scope ayrı olarak belirtilir.
## D
`@Component` bean üretmez, yalnız dokümantasyon amaçlı bir işarettir; kaydı `@Bean` yapar
🤖 `@Component` component scan ile gerçek bir bean üretir; işlevseldir.
## Doğru
A
## Açıklama
`@Component` (ve türevleri `@Service`/`@Repository`/`@Controller`) **sınıf** düzeyinde durur; component scan sırasında Spring bu sınıfı bulup örnekleyerek bean yapar — kaynağını **değiştirebildiğin** kendi sınıfların için idealdir. `@Bean` ise bir `@Configuration` sınıfındaki **metot** üzerinde durur; metodun döndürdüğü nesne bean olur. `@Bean`, anotasyon ekleyemeyeceğin **üçüncü parti** sınıflar (ör. `ObjectMapper`, `RestClient`) ve programatik kurulum gerektiren durumlar için doğru araçtır. İkisi de varsayılan singleton üretir (C yanlış), `@Component` gerçek bir bean üretir (D yanlış), kendi sınıflarında da kullanılır (B yanlış).

# cfg-2 | Temel
## Soru
Component scan (`@ComponentScan`) tam olarak ne yapar?
## A
Yalnız `@Configuration` sınıflarındaki `@Bean` metotlarını tarar; `@Component` sınıflarını yok sayar
🤖 Tam tersi: taradığı şey `@Component` ve türevleridir.
## B
Sınıf yolundaki (classpath) tüm JAR'larda bulunan public sınıfları otomatik olarak bean yapar
🤖 Tüm JAR'ları değil, yalnız belirtilen paketlerdeki stereotype-anotasyonlu sınıfları bean yapar.
## C
Uygulama çalışırken kullanılmayan bean'leri tarayıp bellekten düşürür
🤖 Bellek yönetimiyle ilgisi yok; başlangıçtaki bean keşfiyle ilgilidir.
## D
Belirtilen paketler altındaki stereotype anotasyonlu sınıfları bean olarak kaydeder
🤖 Doğru: stereotype sınıfları tarar; Boot ana paketten otomatik başlatır.
## Doğru
D
## Açıklama
Component scan, verilen **temel paket(ler)** ağacını dolaşıp `@Component` ve türevi (`@Service`, `@Repository`, `@Controller`, `@Configuration`) anotasyonlu sınıfları keşfeder ve container'a bean olarak kaydeder. Spring Boot'ta `@SpringBootApplication` içinde örtük bir `@ComponentScan` vardır ve **ana sınıfın bulunduğu paketten** başlar — bu yüzden bean'lerini ana sınıfın altındaki paketlere koyman gerekir, yoksa taranmazlar (klasik "bean bulunamadı" tuzağı). Tüm classpath'i (B) veya JAR'ları değil, yalnız stereotype sınıflarını tarar; `@Bean` metotlarıyla (A) veya bellekle (C) ilgisi yoktur.

# cfg-3 | Orta
## Soru
`@Profile("prod")` ile işaretli bir bean için doğru olan hangisidir?
## A
Bean her ortamda kaydedilir; `@Profile` yalnız log'a bir etiket yazar, kaydı etkilemez
🤖 `@Profile` gerçek bir koşuldur; eşleşmezse bean HİÇ kaydedilmez.
## B
Bean yalnız `prod` profili aktifken container'a kaydedilir
🤖 Doğru: profil eşleşmesi bean'in kaydını koşullar.
## C
Bean `prod` dışındaki tüm profillerde kaydedilir; `prod` aktifken hariç tutulur
🤖 Anlamı ters: `@Profile("prod")` prod'da AKTİF eder, dışlamaz.
## D
`@Profile` yalnız `application.properties` içindeki ayarları filtreler; Java'daki bean tanımlarına etkisi yoktur
🤖 `@Profile` bir Java anotasyonudur; kod düzeyinde bean seçimini yönetir.
## Doğru
B
## Açıklama
`@Profile("prod")`, bir bean'in (veya tüm `@Configuration` sınıfının) **yalnız** ilgili profil aktifken kaydedilmesini sağlar. Aktif profil `spring.profiles.active` (properties/env/CLI) ile belirlenir. Böylece aynı arayüzün dev'de sahte (in-memory), prod'da gerçek (managed) implementasyonunu koşulsuz `if` yazmadan seçebilirsin. Eşleşme olmazsa bean **hiç** oluşturulmaz (A yanlış). Anlamı "bu profilde AKTİF" olup dışlama değildir — dışlamak için `@Profile("!prod")` yazılır (C yanlış). Bu bir Java anotasyonudur, kod düzeyinde çalışır (D yanlış). Daha ince koşullar için `@Conditional` ailesi kullanılır.

# cfg-4 | Orta
## Soru
Aşağıdaki `@Configuration` sınıfında `servis()` bean'i `repo()` metodunu çağırıyor. Kaç `Repo` örneği oluşur?

```java
@Configuration
class Ayar {
    @Bean Repo repo() { return new Repo(); }
    @Bean Servis servis() { return new Servis(repo()); }  // repo() çağrısı
}
```
## A
İki ayrı `Repo` — `repo()` her çağrıldığında gövdesi yeniden çalışır, bean'in kendi örneği ayrı kalır
🤖 Metot gövdesi yeniden çalışmaz; CGLIB proxy'si çağrıyı var olan singleton'a yönlendirir.
## B
Sıfır — bir `@Bean` metodunu elle çağırmak yasaktır; Spring başlangıçta hata verir ve container ayağa kalkmaz
🤖 Container sorunsuz başlar; proxy sayesinde çağrı singleton'a yönlenir.
## C
Tek bir `Repo` — CGLIB proxy'si `repo()` çağrısını container'daki singleton'a yönlendirir
🤖 Doğru: proxyBeanMethods varsayılan true; içeriden çağrı singleton'a gider.
## D
`servis()` her çağrıldığında yeni bir `Repo` üretilir; örnek sayısı çağrı sayısı kadar olur
🤖 servis() de singleton olarak bir kez kurulur; hem çağrı hem bean singleton'dır.
## Doğru
C
## Açıklama
`@Configuration` sınıfları Spring tarafından **CGLIB ile proxy'lenir** (`proxyBeanMethods = true` varsayılan). Bu proxy, bir `@Bean` metodunun **config içinden** çağrılmasını yakalar ve düz `new` çalıştırmak yerine container'a yönlendirip **var olan singleton'ı** döndürür. Bu yüzden `servis()` içindeki `repo()` çağrısı yeni nesne üretmez; ortada **tek** bir `Repo` singleton'ı olur ve hem `repo` bean'i hem de `servis`'in bağımlılığı aynısını paylaşır. Eğer bu proxy'yi `proxyBeanMethods = false` ile kapatsaydın, `repo()` düz Java çağrısı gibi davranır ve **iki ayrı** `Repo` oluşurdu — bir sonraki sorunun konusu.

# cfg-5 | İleri
## Soru
Bir kütüphane yazıyorsun; kullanıcı kendi `Serializer` bean'ini tanımlamadıysa senin varsayılan implementasyonunu, tanımladıysa onunkini kullanmak istiyorsun. Hangi mekanizma tam bu amaç içindir?
## A
Varsayılan bean metodunu `@ConditionalOnMissingBean` ile işaretlemek
🤖 Doğru: "kullanıcı sağlamadıysa varsayılanı ver" için tam da bu koşul vardır.
## B
Bean'i `@Profile("default")` ile işaretleyip yalnız varsayılan profilde kaydedilmesini sağlamak
🤖 `@Profile` profil aktifliğine bakar, "başka bean var mı" sorusuna değil; amacı karşılamaz.
## C
Varsayılan bean'i `@Primary` yapmak; böylece kullanıcı kendi bean'ini tanımlasa bile seçim netleşir
🤖 `@Primary` çakışmada SENİN bean'ini öne alır — istediğinin tam tersi; kullanıcınınki ezilir.
## D
Bean'i hem kütüphanede hem kullanıcı tarafında tanımlayıp Spring'in uygun olanı seçmesini beklemek
🤖 Aynı tipten iki tanım çakışma/belirsizlik üretir; kontrollü geri-çekilme sağlamaz.
## Doğru
A
## Açıklama
`@ConditionalOnMissingBean`, `@Conditional` ailesinin en çok kullanılan üyesidir: bir bean, **aynı tipte başka bir bean henüz tanımlı değilse** kaydedilir; kullanıcı kendi bean'ini verdiyse senin varsayılanın **geri çekilir**. Bu, "makul varsayılan ver ama kullanıcı ezmek isterse yol aç" desenidir ve Spring Boot auto-configuration'ının bel kemiğidir (starter'lar `DataSource`, `ObjectMapper` vb. için tam böyle davranır). `@Profile` (B) profil aktifliğine bakar, bean varlığına değil; `@Primary` (C) çakışmada seninkini öne alarak kullanıcının bean'ini ezer — istenenin tam tersi; aynı tipi iki kez tanımlamak (D) belirsizlik/çakışma üretir. Aile üyeleri: `@ConditionalOnClass`, `@ConditionalOnProperty`, `@ConditionalOnBean` vb.

# cfg-6 | İleri
## Soru
Bir yapılandırma sınıfında `proxyBeanMethods = false` (ör. `@Configuration(proxyBeanMethods = false)`) ne zaman doğru tercihtir ve etkisi nedir?
## A
Her durumda tercih edilmelidir; proxy kalktığı için bean'ler thread-safe hale gelir
🤖 Thread-safety ile ilgisi yok; ayrıca inter-bean çağrısı olan config'lerde tehlikelidir.
## B
`@Bean` metotlarını private erişime çevirir; config sınıfı dışından çağrılmalarını engeller
🤖 Erişim belirleyicisini değiştirmez; CGLIB proxy'yi kapatır.
## C
Bean tanımlarını çalışma zamanı yerine derleme zamanında sabitler ve reflection kullanımını tamamen kaldırır
🤖 Derleme-zamanı sabitleme yapmaz; yalnız çağrı-yakalama proxy'sini devre dışı bırakır.
## D
İç çağrı yoksa proxy maliyetini atlatır; iç çağrı varsa her çağrıda yeni nesne üretilir
🤖 Doğru: proxy kalkar, "lite" mod; inter-bean çağrısı olan config'de tehlikeli.
## Doğru
D
## Açıklama
`proxyBeanMethods = false` ("lite" mod), `@Configuration` sınıfı için CGLIB alt-sınıf proxy'sini **kapatır**. Kazanç: proxy oluşturma maliyeti kalkar, başlangıç biraz hızlanır ve bellek düşer — bu yüzden **`@Bean` metotları birbirini çağırmayan**, bağımsız bean tanımları için idealdir (Boot'un kendi auto-config sınıfları çoğunlukla böyle yazılır). Bedeli: bir `@Bean` metodunu config içinden çağırırsan artık container'a yönlenmez, **düz Java çağrısı** olur ve **her çağrıda yeni nesne** üretir → singleton garantisi kaybolur (bir önceki soruyla tam zıt davranış). Thread-safety (A), erişim belirleyici (B) veya derleme-zamanı sabitleme (C) ile ilgisi yoktur; olay yalnız çağrı-yakalama proxy'sinin varlığıdır.
