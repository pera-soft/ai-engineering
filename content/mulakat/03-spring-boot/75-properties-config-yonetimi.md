---
slug: properties-config-yonetimi
baslik: "Konfigürasyon Yönetimi: yml/properties, @ConfigurationProperties, Profiller ve Öncelik Sırası"
kademe: Orta
sira: 75
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Aynı kahve makinesini evde ve ofiste kullandığını düşün: makine aynı, ama su sertliği ve öğütme ayarı bulunduğu yere göre değişiyor. Yazılımda da aynı uygulama, geliştirici bilgisayarında test veritabanına, üretimde gerçek veritabanına bağlanmalıdır — ama **kod değişmemelidir**. Bunu sağlayan şeye **dış konfigürasyon** (externalized configuration) denir: değişen değerleri koddan çıkarıp dosyaya, ortam değişkenine veya başlatma komutuna taşımak. Spring Boot bu kaynakları katmanlar hâlinde okur ve sabit bir **öncelik sırasıyla** birleştirir.

### Temel kavramlar

- **Yapılandırma / konfigürasyon (configuration):** Uygulamanın davranışını belirleyen, kodun dışında tutulan ayarlar: dinlenecek port, veritabanı adresi, zaman aşımı süresi. `anahtar = değer` çiftleri hâlinde yazılır.
- **`application.properties`:** Her satırı düz `anahtar=deger` olan ayar dosyası. **`application.yml`:** Aynı bilgiyi **hiyerarşik** (girintili) yazan YAML dosyası; iç içe yapılar ve listeler daha okunaklıdır.

```yaml
# application.yml
server:
  port: 8080
uygulama:
  max-baglanti: 20
```

```properties
# application.properties — aynı bilgi, düz sözdizimi
server.port=8080
uygulama.max-baglanti=20
```

- **`Environment`:** Spring'in açılışta tüm kaynakları yükleyip birleştirdiği tek okuma noktası. İki dosya biçimi de aynı `Environment`'a yüklenir. Ayarlar her istekte yeniden yorumlanmaz; bu yüzden aralarında hız farkı da **yoktur**. İkisi de profilleri destekler ve YAML desteği yıllardır vardır; belirli bir Boot sürümüne bağlı değildir. Aynı konumda ikisi birden varsa Spring ikisini de yükler; çakışan anahtarlarda **`.properties`, `.yml`'yi ezer**. Karışıklığı önlemek için ekipçe birini seç.
- **Ortam değişkeni (environment variable):** İşletim sisteminin, başlattığı programa verdiği `AD=deger` çiftleri. Docker/Kubernetes'te ayar geçmenin standart yoludur; kabuk kısıtları yüzünden adlarında **nokta ve tire bulunamaz**.
- **Profil (profile):** Ortama ad vermektir (`dev`, `prod`). `spring.profiles.active=prod` ile seçilir; `application-prod.yml` gibi profil-özel dosyalar ve `@Profile` anotasyonu bu ada göre çalışır.

### Nasıl çalışır?

Aynı anahtar birden çok yerde tanımlıysa Spring hata vermez ve rastgele seçmez. Sabit bir öncelik sırasıyla en yüksek kaynağı kazandırır. Bu sıra **deterministiktir**: aynı girdiyle sonuç hep aynı çıkar.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Konfigürasyon öncelik sırası yukarıdan aşağıya">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kim kazanır? (yukarıdaki alttakini ezer)</text>
  <rect x="150" y="40" width="340" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">1) Komut satırı argümanları (--app.x=..)</text>
  <rect x="150" y="76" width="340" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="96" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2) Ortam değişkenleri / sistem property</text>
  <rect x="150" y="112" width="340" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="132" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">3) application-&lt;profil&gt;.yml (aktif profil)</text>
  <rect x="150" y="148" width="340" height="30" rx="8" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="320" y="168" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">4) application.yml (varsayılan)</text>
  <rect x="150" y="184" width="340" height="30" rx="8" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.5"/>
  <text x="320" y="204" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">5) @PropertySource / kod içi varsayılan</text>
  <text x="320" y="236" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ortam değişkeni, dosyadaki değeri EZER — sır/parola için ideal</text>
</svg>
```

Sıra yukarıdan aşağıya şöyledir: komut satırı argümanları (`java -jar app.jar --server.port=9090`) > işletim sistemi ortam değişkenleri ve JVM sistem property'leri > aktif profilin dosyası (`application-prod.yml`) > varsayılan `application.yml` > `@PropertySource` ile eklenen dosya > kod içi varsayılan (`@Value("${x:varsayilan}")`).

Kod içi varsayılan en zayıf halkadır; yalnız hiçbir kaynak değer vermezse devreye girer. Tam liste sürüme göre daha ayrıntılıdır, ama akılda kalması gereken bu hiyerarşidir.

İki tipik sonuç:

- `application.yml`'de `server.port: 8080` yazsa bile uygulama `SERVER_PORT=9090` ortam değişkeniyle başlatılırsa **9090** dinlenir; bu bir çakışma hatası veya rastgele seçim değildir.
- `spring.profiles.active=prod` iken hem `application.yml` hem `application-prod.yml` `veritabani.url` tanımlıyorsa **profil-özel değer varsayılanı ezer**. Belirleyici olan yükleme sırası değil, önceliktir. Metin (String) değerler **birleştirilmez**, yani virgülle uç uca eklenmez. Liste ve harita gibi yapılarda birleşme kuralları ayarlanabilir; ama varsayılan davranış yine "profil değeri kazanır"dır.

Pratik kural şudur. `application.yml`'de mantıklı geliştirme varsayılanlarını tut. Ortama özel farkları (prod veritabanı adresi, havuz boyutu) profil dosyasına koy. **Parolaları ve erişim anahtarlarını (token) dosyaya değil ortam değişkenine** ver; hem dosyayı ezer hem git'e sızmaz. Tek imajı yeniden derlemeden birçok ortamda çalıştırmanın temeli budur. Bu, 12-Factor App adıyla bilinen modern uygulama ilkelerinden biridir.

Ayarları koda almanın iki yolu vardır.

```svg
<svg viewBox="0 0 640 196" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="@Value tek değer, @ConfigurationProperties tip-güvenli grup bağlama">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@Value vs @ConfigurationProperties</text>
  <rect x="26" y="44" width="278" height="128" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">@Value("${x}")</text>
  <text x="165" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tek bir değer enjekte eder</text>
  <text x="165" y="115" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">SpEL destekli</text>
  <text x="165" y="137" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">grup / validation YOK</text>
  <text x="165" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dağınık / tekil değer için</text>
  <rect x="336" y="44" width="278" height="128" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">@ConfigurationProperties</text>
  <text x="475" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">anahtar grubu → POJO</text>
  <text x="475" y="115" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">tip güvenli + relaxed binding</text>
  <text x="475" y="137" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">@Validated ile doğrulama</text>
  <text x="475" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ilişkili çok ayar için</text>
</svg>
```

```java
@Component
class Ornek {
    @Value("${uygulama.zaman-asimi}")          // tek değer, SpEL destekli
    private int zamanAsimi;
}

@Validated
@ConfigurationProperties(prefix = "uygulama")  // anahtar grubu → tip-güvenli POJO
public class UygulamaAyarlari {
    @NotBlank private String url;
    private int maxBaglanti;
    // getter / setter
}
```

`@Value("${...}")` **tek bir değeri** enjekte eder ve Spring'in ifade dili SpEL'i destekler. Örnek (instance) alanlarına ve metot parametrelerine çalışır; `static` alanlara doğrudan enjekte edilmez.

`@ConfigurationProperties(prefix = "...")` ise **bir anahtar grubunu** (`uygulama.zaman-asimi`, `uygulama.url` …) tek bir **tip-güvenli düz Java nesnesine (POJO)** bağlar. Yanında iç içe yapı ve liste desteği, editör otomatik tamamlaması, `@Validated` + `jakarta.validation` ile **doğrulama** ve **esnek eşleme (relaxed binding)** gelir.

Kıdemli tercihi nettir: birbiriyle ilişkili çok sayıda ayar için `@ConfigurationProperties`, tek ve dağınık değer için `@Value`. İkisi yalnız isim farkı olan eşdeğerler **değildir**; fark davranıştadır.

**Esnek eşleme (relaxed binding)** aynı ayarın farklı yazımlarını tek alana bağlar.

```svg
<svg viewBox="0 0 640 194" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Relaxed binding: dört farklı yazım aynı POJO alanına bağlanır">
  <defs>
    <marker id="prop-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Relaxed binding: farklı yazım, tek alan</text>
  <rect x="20" y="44" width="230" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="135" y="64" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">app.maxBaglanti</text>
  <rect x="20" y="80" width="230" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="135" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">app.max-baglanti</text>
  <rect x="20" y="116" width="230" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="135" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">app.max_baglanti</text>
  <rect x="20" y="152" width="230" height="30" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="135" y="172" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">APP_MAXBAGLANTI (env)</text>
  <rect x="400" y="86" width="220" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="510" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">int maxBaglanti</text>
  <text x="510" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">POJO alanı</text>
  <line x1="250" y1="59" x2="398" y2="104" stroke="#186399" stroke-width="1.6" marker-end="url(#prop-arw)"/>
  <line x1="250" y1="95" x2="398" y2="110" stroke="#186399" stroke-width="1.6" marker-end="url(#prop-arw)"/>
  <line x1="250" y1="131" x2="398" y2="116" stroke="#186399" stroke-width="1.6" marker-end="url(#prop-arw)"/>
  <line x1="250" y1="167" x2="398" y2="122" stroke="#186399" stroke-width="1.6" marker-end="url(#prop-arw)"/>
</svg>
```

`uygulama.maxBaglanti`, `uygulama.max-baglanti`, `uygulama.max_baglanti` ve ortam değişkeni biçimi **`UYGULAMA_MAXBAGLANTI`** — hepsi POJO'daki `maxBaglanti` alanına bağlanır.

Ortam değişkenleri nokta ve tire içeremez. Bu yüzden Spring'in belgelediği **standart biçim büyük harf + alt çizgidir** (`ONEK_AYARADI`). Kubernetes ve Docker'da ayarlar bu nedenle `UYGULAMA_MAXBAGLANTI=50` diye verilir.

Ortam değişkenleri de birer ayar kaynağıdır: `@ConfigurationProperties` nesnelerine bağlanırlar ve dosyayı ezecek önceliktedirler. Tip dönüşümü (`String` → `int`) bağlama sırasında otomatik yapılır; değer geçersizse bağlama hatası fırlar. Aynı kural `SERVER_PORT` → `server.port` eşlemesini de açıklar.

### Mülakatta ne sorulur?

- **`.yml` vs `.properties`?** Fark yalnız sözdizimi; ikisi de aynı `Environment`'a yüklenir, ikisi de profilleri destekler, çakışmada `.properties` `.yml`'yi ezer.
- **`@Value` vs `@ConfigurationProperties`?** Tek değer + SpEL — karşısında tip-güvenli grup bağlama + relaxed binding + doğrulama.
- **Öncelik sırası?** Komut satırı > ortam değişkeni / sistem property > profil dosyası > `application.yml` > `@PropertySource` > kod içi varsayılan. En tepede komut satırı argümanları vardır.
- **`SERVER_PORT=9090` + dosyada 8080 → hangisi?** 9090; deterministik önceliğin sonucudur.
- **Profil dosyası varsayılanı ezer mi?** Evet, üzerine biner; String'ler birleştirilmez.
- **Ortam değişkeninden POJO alanı nasıl beslenir?** `PREFIX_ALANADI` (`UYGULAMA_MAXBAGLANTI`) biçiminde; noktalı veya camelCase yazım ortam değişkeninde kullanılamaz.

### Tek cümlede

Spring Boot ayarları birçok kaynaktan okuyup sabit bir öncelik sırasıyla birleştirir; ilişkili ayarları `@ConfigurationProperties` POJO'suna bağlayıp sırları ortam değişkeniyle geçtiğinde tek kod tabanı her ortamda doğru davranır.

# prop-1 | Temel
## Soru
`application.yml` ile `application.properties` arasındaki temel fark nedir?
## A
`.yml` çalışma-zamanında daha hızlı okunur; `.properties` her istekte yeniden yorumlanır
🤖 İkisi de açılışta aynı Environment'a yüklenir; istek başına yorumlama yoktur, fark biçimseldir.
## B
İkisi de aynı konfigürasyonu ifade eder; fark yalnız sözdizimidir
🤖 Doğru: YAML hiyerarşik/okunaklı, `.properties` düz anahtar=değer; taşıdıkları bilgi aynıdır.
## C
`.properties` profilleri desteklemez; profil dosyaları yalnız `.yml` biçiminde yazılabilir
🤖 İki biçim de `application-<profil>.properties/.yml` deseniyle profilleri destekler.
## D
`.yml` yalnız Spring Boot 4 ve sonrasında çalışır
🤖 YAML desteği yıllardır vardır; sürümle ilgisi yok.
## Doğru
B
## Açıklama
`application.yml` ve `application.properties` **aynı** dış konfigürasyonu ifade eder; fark yalnız **sözdizimidir**. YAML hiyerarşiktir (iç içe anahtarlar, listeler daha okunaklı), `.properties` ise düz `anahtar=deger` satırlarıdır. İkisi de açılışta aynı `Environment`'a yüklenir ve ikisi de profilleri (`-dev`, `-prod`) destekler; aynı konumda ikisi birden varsa çakışan anahtarlarda `.properties` `.yml`'yi ezer. Bu karışıklığı yaşamamak için ekipçe **birini** seçmek iyi bir pratiktir. YAML karmaşık yapılarda, `.properties` basit düz listelerde tercih edilir.

# prop-2 | Temel
## Soru
`@ConfigurationProperties(prefix = "uygulama")` ile `@Value("${uygulama.zaman-asimi}")` arasındaki en doğru fark hangisidir?
## A
`@Value` bir anahtar grubunu tip-güvenli bir POJO'ya bağlar, `@ConfigurationProperties` ise tek bir değeri okur
🤖 Ters: grup bağlama `@ConfigurationProperties`'in işi; `@Value` tek değer enjekte eder.
## B
İkisi tamamen aynıdır, aralarında yalnızca isim farkı vardır
🤖 Fark davranışsaldır: biri tek değer (SpEL), diğeri tip-güvenli grup bağlama + doğrulama.
## C
`@ConfigurationProperties` bir anahtar grubunu POJO'ya bağlar; `@Value` tek bir değeri enjekte eder
🤖 Doğru: grup + tip güvenliği + relaxed binding + validation `@ConfigurationProperties`'te; `@Value` tekil ve SpEL'lidir.
## D
`@Value` yalnız `static` alanlarda çalışır, örnek alanlarına enjekte edilemez
🤖 `@Value` örnek (instance) alanlarına/parametrelerine enjekte eder; `static` alanlarda ise doğrudan çalışmaz.
## Doğru
C
## Açıklama
`@ConfigurationProperties(prefix = "uygulama")` bir **anahtar grubunu** (`uygulama.zaman-asimi`, `uygulama.url` ...) tek bir **tip-güvenli POJO'ya** bağlar; **relaxed binding** (`zamanAsimi`/`zaman-asimi`/`ZAMAN_ASIMI` hepsi eşleşir), **doğrulama** (`@Validated` + `jakarta.validation`) ve iç içe yapı/liste desteği sağlar. `@Value("${...}")` ise **tek bir değeri** enjekte eder ve SpEL ifadelerini destekler. Senior tercihi: birbiriyle ilişkili çok sayıda ayar için `@ConfigurationProperties` (tip güvenliği + IDE otomatik tamamlama + doğrulama), tek/dağınık değer için `@Value`.

# prop-3 | Orta
## Soru
`application.yml`'de `server.port: 8080` var, ama uygulama `SERVER_PORT=9090` ortam değişkeniyle başlatılıyor. Uygulama hangi portu dinler?
## A
8080 — dosyadaki değer her zaman kazanır
🤖 Tersine: ortam değişkeni dosyadaki değerden daha yüksek önceliktedir.
## B
Başlatma hatası verir — aynı anahtarın iki kaynakta tanımlanması çakışma sayılır
🤖 Çakışma hata değildir; Spring öncelik sırasına göre birini seçer, hata atmaz.
## C
Rastgele biri seçilir
🤖 Seçim rastgele değil, deterministik öncelik sırasına göredir.
## D
9090 — ortam değişkeni `application.yml`'deki değeri ezer
🤖 Doğru: OS ortam değişkeni, application.yml'den daha yüksek önceliklidir; relaxed binding ile `SERVER_PORT` → `server.port`.
## Doğru
D
## Açıklama
Spring Boot öncelik sırasında **OS ortam değişkenleri**, `application.yml`/`.properties` dosyalarından **daha yüksektir**. Bu yüzden `SERVER_PORT=9090`, dosyadaki `server.port: 8080` değerini **ezer** ve uygulama **9090**'ı dinler. Buradaki ikinci incelik **relaxed binding**'dir: `SERVER_PORT` (büyük harf + alt çizgi) otomatik olarak `server.port` anahtarına eşlenir. Bu davranış, aynı imajı farklı ortamlarda (dev/prod) yeniden derlemeden yapılandırmayı ve **sırları** (parola, token) ortam değişkeniyle güvenle geçirmeyi mümkün kılar — 12-Factor App ilkesidir.

# prop-4 | Orta
## Soru
Öncelik sırasında en yüksek (diğerlerini ezen) konfigürasyon kaynağı hangisidir?
## A
Komut satırı argümanları (ör. `--server.port=9090`)
🤖 Doğru: komut satırı argümanları en yüksek önceliktedir; ortam değişkenini ve dosyaları ezer.
## B
`application.yml` dosyasında tanımlanmış varsayılan değer
🤖 Varsayılan dosya en alt seviyelerdedir; komut satırı ve ortam değişkeni onu ezer.
## C
Kod içi `@Value` varsayılanı (`${x:varsayilan}`)
🤖 Kod içi varsayılan en zayıf halkadır; yalnız hiçbir kaynak değer vermezse devreye girer.
## D
`@PropertySource` ile eklenen dosya
🤖 `@PropertySource` düşük önceliktedir; komut satırı ve ortam değişkeni onu ezer.
## Doğru
A
## Açıklama
Spring Boot'un dış konfigürasyon öncelik sırasında en tepede (kabaca) **komut satırı argümanları** bulunur: `java -jar app.jar --server.port=9090` her şeyi ezer. Onu sırasıyla OS ortam değişkenleri / sistem property'leri, aktif profil dosyası (`application-<profil>.yml`), varsayılan `application.yml`, `@PropertySource` ve en altta kod içi varsayılanlar izler. Bu deterministik sıra, üretimde davranışı öngörülebilir kılar: "değer neden bu geldi?" sorusunun cevabı her zaman **en yüksek öncelikli kaynağın kazanmasıdır**. (Tam liste sürüme göre daha ayrıntılıdır; kritik olan bu genel hiyerarşidir.)

# prop-5 | Orta
## Soru
`spring.profiles.active=prod` iken hem `application.yml` hem `application-prod.yml` `veritabani.url` tanımlıyorsa ne olur?
## A
İki değer birleştirilip virgülle eklenir
🤖 String property'ler birleştirilmez; profil-özel değer varsayılanı bütünüyle ezer.
## B
`application-prod.yml`'deki değer varsayılanı ezer
🤖 Doğru: aktif profil dosyası daha yüksek önceliklidir; aynı anahtar için varsayılanın üzerine biner.
## C
`application.yml` kazanır, çünkü profil dosyasından önce yüklenir ve sonraki dosyalar yok sayılır
🤖 Yükleme sırası değil öncelik belirleyicidir; profil-özel dosya varsayılandan yüksektir.
## D
Uygulama aynı anahtar iki dosyada tanımlandığı için hiç başlamaz
🤖 Aynı anahtarın iki dosyada olması hata değildir; öncelik sırasıyla çözülür.
## Doğru
B
## Açıklama
Aktif profil dosyası (`application-prod.yml`), varsayılan `application.yml`'den **daha yüksek** önceliktedir. Aynı anahtar (`veritabani.url`) her ikisinde tanımlıysa, **profil-özel değer** varsayılanı **ezer** (üzerine biner). Bu, tipik desendir: `application.yml`'de mantıklı/dev varsayılanları tut, ortama özel farkları (`prod` DB URL'si, havuz boyutu) `application-prod.yml`'de belirt. Not: String değerler **birleştirilmez** (A yanlış); liste/harita gibi yapılarda birleşme kuralları ayarlanabilir olsa da varsayılan davranış "profil değeri kazanır"dır. Bu mekanizma, tek imajı çok ortamda çalıştırmanın temelidir.

# prop-6 | İleri
## Soru
`@ConfigurationProperties(prefix="uygulama")` ile bağlanan bir POJO'daki `int maxBaglanti` alanı, bir ortam değişkeninden (environment variable) nasıl beslenir?
## A
`uygulama.maxBaglanti` adıyla; ortam değişkeninde de birebir camelCase yazım şarttır
🤖 Ortam değişkeni adı nokta içeremez; relaxed binding burada `UYGULAMA_MAXBAGLANTI` biçimini bekler.
## B
Hiçbir biçimde beslenemez; ortam değişkenleri `@ConfigurationProperties`'e hiç bağlanmaz
🤖 ortam değişkenleri de bir property kaynağıdır ve relaxed binding ile POJO alanlarına bağlanır.
## C
Yalnız `application.yml` içinden beslenebilir; ortam değişkeni bu bağlamada desteklenmez
🤖 Ortam değişkenleri desteklenir ve dosyayı ezecek önceliktedir; alan bağlama da geçerlidir.
## D
`UYGULAMA_MAXBAGLANTI` adıyla, büyük harf ve alt çizgi kullanılarak
🤖 Doğru: relaxed binding `uygulama.max-baglanti`/`maxBaglanti`/`UYGULAMA_MAXBAGLANTI`'yı aynı alana eşler; ortam değişkeni için kanonik biçim budur.
## Doğru
D
## Açıklama
**Relaxed binding**, aynı property'nin farklı yazımlarını tek alana eşler: `uygulama.maxBaglanti`, `uygulama.max-baglanti`, `uygulama.max_baglanti` ve ortam değişkeni biçimi **`UYGULAMA_MAXBAGLANTI`** hepsi POJO'daki `maxBaglanti` alanına bağlanır. Ortam değişkenleri kabuk/işletim sistemi kısıtları nedeniyle nokta ve tire içeremediğinden, Spring'in belgelediği **kanonik biçim büyük harf + alt çizgidir** (`PREFIX_PROPERTYNAME`). Bu yüzden Kubernetes/Docker ortamlarında ayarlar `UYGULAMA_MAXBAGLANTI=50` biçiminde verilir ve tip dönüşümü (`String` → `int`) bağlama sırasında otomatik yapılır; geçersiz değerde bağlama hatası fırlar.
