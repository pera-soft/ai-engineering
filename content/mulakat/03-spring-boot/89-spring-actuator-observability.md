---
slug: spring-actuator-observability
baslik: "Spring Boot Actuator ve Gözlemlenebilirlik: Health Probe, Micrometer ve OpenTelemetry"
kademe: Orta
sira: 89
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir arabanın gösterge paneli düşün: yakıt seviyesi, motor sıcaklığı, arıza lambası. Motoru açıp bakmadan aracın durumunu anlarsın. Spring Boot Actuator, çalışan uygulamaya bu paneli takar. Uygulama ayaktayken "sağlıklı mısın?", "kaç istek aldın?", "hangi sürümdesin?" sorularına standart adreslerden cevap verir. Böylece izleme sistemleri, yük dengeleyiciler ve Kubernetes uygulamanın iç durumunu kendisi sorabilir.

### Temel kavramlar

- **Gözlemlenebilirlik (observability)**: Bir sistemin dışarıdan bakarak iç durumunu anlayabilme özelliği. Üç ayağı vardır: metrik, log, iz (trace).
- **Uç nokta (endpoint)**: Uygulamanın dışarıya açtığı bir adres. Actuator'ınkiler `/actuator/...` altındadır.
- **Metrik (metric)**: Sayısal, zamana bağlı ölçüm. "Şu ana kadar 1.240 istek geldi", "ortalama yanıt süresi 38 ms" gibi.
  - **`Counter`**: yalnız artan sayaç (toplam istek).
  - **`Timer`**: süre/gecikme ölçer.
  - **`Gauge`**: anlık değer (kuyruk uzunluğu).
  - **`DistributionSummary`**: değer dağılımı.
- **İzleme kaydı / izleme adımı (trace / span)**: Bir isteğin servisler arasında izlediği yolun kaydı. `traceId` isteğin tamamını, `spanId` o yoldaki tek bir adımı işaretler.
- **Yoklama (probe)**: Kubernetes'in belirli aralıklarla "iyi misin?" diye çağırdığı adres.
- **Pod**: Kubernetes'te uygulamanın çalıştığı en küçük dağıtım birimi.

`spring-boot-starter-actuator` bağımlılığı eklenince `/actuator/health` (sağlık), `/actuator/metrics` (metrikler), `/actuator/info` (yapı bilgisi) gibi uçlar **tanımlanır**. Dikkat: tanımlanmak ile dışarı açılmak aynı şey değildir — HTTP üzerinden varsayılan olarak yalnız `/actuator/health` erişilebilir, gerisi `management.endpoints.web.exposure.include` ile açılır. `/actuator/prometheus` ise ayrıca `micrometer-registry-prometheus` bağımlılığını ister. Actuator derlemeyle, veritabanı migrasyonuyla (o Flyway/Liquibase işidir) veya kod formatlamayla ilgilenmez; işi **çalışma anıdır**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Actuator health probe ve Micrometer OpenTelemetry gözlemlenebilirlik akışı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Actuator: sağlık + metrik + iz</text>
  <rect x="30" y="44" width="180" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Health probe</text>
  <text x="120" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">liveness: ölü mü? → restart</text>
  <text x="120" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">readiness: hazır mı? → trafik</text>
  <rect x="230" y="44" width="180" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Micrometer</text>
  <text x="320" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">metrik cephesi (facade)</text>
  <text x="320" y="102" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Counter / Timer / Gauge</text>
  <rect x="430" y="44" width="180" height="70" rx="10" fill="#F1ECF6" stroke="#6B4CA3" stroke-width="1.5"/>
  <text x="520" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#6B4CA3" font-family="system-ui">Micrometer Tracing</text>
  <text x="520" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">OpenTelemetry köprüsü</text>
  <text x="520" y="102" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">Sleuth ÖLDÜ</text>
  <rect x="150" y="150" width="150" height="42" rx="10" fill="#F3EEE6" stroke="#9A6A1E" stroke-width="1.5"/>
  <text x="225" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#9A6A1E" font-family="system-ui">Prometheus / OTLP</text>
  <rect x="340" y="150" width="150" height="42" rx="10" fill="#F3EEE6" stroke="#9A6A1E" stroke-width="1.5"/>
  <text x="415" y="176" text-anchor="middle" font-size="11" font-weight="700" fill="#9A6A1E" font-family="system-ui">İzleme arka ucu</text>
  <path d="M320 114 L250 148" stroke="#5D7789" stroke-width="1.4" marker-end="url(#act-ok)"/>
  <path d="M520 114 L430 148" stroke="#5D7789" stroke-width="1.4" marker-end="url(#act-ok)"/>
  <defs>
    <marker id="act-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**1. Sağlık iki farklı soruyu ayırır.** Biri **ayakta mı** denetimi (liveness), öteki **hazır mı** denetimidir (readiness).

`/actuator/health/liveness` "uygulama takıldı/öldü mü?" sorusunu yanıtlar. Başarısız olursa Kubernetes pod'u **öldürüp yeniden başlatır**; kalıcı kilitlenmeden kurtulma yolu budur. `/actuator/health/readiness` ise "bu pod şu an istek almaya hazır mı?" sorusunu yanıtlar. Başarısız olursa Kubernetes pod'u **öldürmez**; yalnızca Service'in yük dengeleme havuzundan geçici olarak çıkarır ve hazır olunca geri alır. İkisi de veri toplamaz, sadece durum bildirir. Bu ayrımı karıştırmak — hazır mı denetimini ayakta mı denetimi gibi ele almak — geçici bir bağımlılık kesintisini kalıcı yeniden başlatma döngüsüne çevirir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Liveness başarısızlığı podu yeniden başlatır, readiness başarısızlığı yalnız trafiği keser">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Liveness ve Readiness: K8s tepkisi farklı</text>
  <rect x="24" y="42" width="290" height="150" rx="12" fill="#FBEDED" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="169" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">liveness probe</text>
  <text x="169" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">/actuator/health/liveness</text>
  <text x="169" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Soru: uygulama öldü mü?</text>
  <rect x="44" y="126" width="250" height="48" rx="8" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.2"/>
  <text x="169" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">başarısız → pod'u ÖLDÜR</text>
  <text x="169" y="164" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">+ yeniden başlat (restart)</text>
  <rect x="326" y="42" width="290" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="471" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">readiness probe</text>
  <text x="471" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">/actuator/health/readiness</text>
  <text x="471" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Soru: trafiğe hazır mı?</text>
  <rect x="346" y="126" width="250" height="48" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="471" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">başarısız → LB'den çıkar</text>
  <text x="471" y="164" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">trafik kesilir, pod YAŞAR</text>
  <text x="320" y="210" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Karıştırma: readiness'i liveness gibi ele almak gereksiz restart döngüsü yaratır</text>
</svg>
```

**2. Metrikler Micrometer üzerinden akar.** Micrometer, metrikler için bir **cephedir** (facade) — SLF4J loglama için ne yapıyorsa Micrometer metrikler için onu yapar. Uygulama kodu satıcıdan bağımsız `Counter`/`Timer`/`Gauge` üretir; altta bir veya daha fazla **kayıt** (registry) bunları Prometheus, OTLP, Datadog veya CloudWatch formatına yazar. Böylece izleme arka ucunu değiştirmek kod değil, bağımlılık/konfigürasyon meselesidir. Micrometer'ın kendisi bir izleme **sunucusu değildir** (depolamayı arka uç yapar), yalnız JVM metrikleriyle sınırlı değildir ve bir loglama kütüphanesi de değildir. Actuator, JVM ve HTTP metriklerini otomatik olarak Micrometer üzerinden yayınlar.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Micrometer tek metrik cephesi altında Prometheus OTLP Datadog gibi kayıtlara yazar">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Micrometer: tek API, değişebilir kayıt (registry)</text>
  <rect x="180" y="38" width="280" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="63" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Uygulama kodu: Counter · Timer · Gauge</text>
  <rect x="210" y="98" width="220" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="119" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Micrometer cephesi (facade)</text>
  <text x="320" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">satıcıdan bağımsız</text>
  <path d="M320 78 L320 96" stroke="#5D7789" stroke-width="1.5" marker-end="url(#act2-ar)"/>
  <rect x="40" y="168" width="130" height="40" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="105" y="192" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Prometheus</text>
  <rect x="196" y="168" width="120" height="40" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="256" y="192" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">OTLP</text>
  <rect x="342" y="168" width="120" height="40" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="402" y="192" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Datadog</text>
  <rect x="488" y="168" width="120" height="40" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="548" y="192" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">CloudWatch</text>
  <path d="M300 142 L120 166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#act2-ar)"/>
  <path d="M312 142 L262 166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#act2-ar)"/>
  <path d="M328 142 L398 166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#act2-ar)"/>
  <path d="M340 142 L540 166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#act2-ar)"/>
  <defs>
    <marker id="act2-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**3. Dağıtık izleme el değiştirdi.** Eski **Spring Cloud Sleuth ÖLDÜ** — Spring Boot 3 ile kullanımdan kaldırıldı ve bakımı bırakıldı. Yerini `micrometer-tracing` aldı: `micrometer-tracing-bridge-otel` (OpenTelemetry köprüsü) veya `micrometer-tracing-bridge-brave` eklenir; trace/span'lar otomatik üretilir, MDC'ye `traceId`/`spanId` konur ve OTLP ile bir arka uca (Tempo, Jaeger, ticari APM) gönderilir. Destek kaldırılmamıştır, harici agent şart değildir ve izleme hem MVC hem WebFlux yığınında çalışır. **OTel köprüsü** endüstri standardı olduğu ve satıcı kilidini azalttığı için tercih edilir; eski Sleuth'a sürüm sabitleyip (pin) devam etmek yalnız teknik borç ve güvenlik riski biriktirir.

**4. Kendi uç noktan.** Spring'in yönettiği bir nesneyi (bean) `@Endpoint(id = "...")` ile işaretlersin (yalnız HTTP için `@WebEndpoint`); içine `@ReadOperation` (GET), `@WriteOperation` (POST), `@DeleteOperation` (DELETE) metotları koyarsın. Böyle bir uç nokta Actuator altyapısına **tam katılır**: `exposure` ayarıyla açılıp kapanır, JMX üzerinden de yayınlanır, güvenlik kurallarına tabi olur. Sıradan bir `@RestController`'a `@GetMapping("/actuator/ozel")` yazmak bunların hiçbirine katılmaz; `application.properties`'e tek satır yazmak da yeni bir davranış tanımlamaz (property yalnız var olanı açar/kapar); `web.xml` servlet eşlemesi Boot'un yolu değildir.

### Mülakatta ne sorulur?

- **Liveness (ayakta mı) vs readiness (hazır mı)**: liveness başarısız → pod öldürülür ve **yeniden başlatılır**; readiness başarısız → pod **yaşar**, yalnız yük dengeleyicinin havuzundan çıkarılır. Rolleri ters söylemek klasik tuzaktır.
- **Micrometer nedir?** Metrik **cephesi**; sunucu değil, kod satıcıdan bağımsız kalır.
- **Sleuth nerede?** Öldü; `micrometer-tracing` + OTel/Brave köprüsü.
- **Hassas uçlar varsayılan KAPALIDIR**: HTTP üzerinden varsayılan olarak yalnız `/actuator/health` açıktır. `env`, `beans`, `configprops`, `heapdump`, `threaddump` kapalıdır. Açmak için `management.endpoints.web.exposure.include=health,info,prometheus` gibi bir **liste** verilir; `include=*` hassas uçların tamamını da açar.
- **Güvenlik riski**: `include=*` + kimlik doğrulamasız `/actuator` ciddi bir **bilgi ifşasıdır**. `env`/`configprops` parola ve API anahtarı sızdırabilir, `heapdump` bellekteki token ve oturum verisini indirtir, `threaddump` iç çağrı yığınlarını açar. Asıl risk yavaş başlangıç veya çift metrik toplama değildir; "salt-okunur, zararsız" varsayımı tehlikeli bir yanılgıdır. Doğrusu: yalnız gerekli uçları açmak, `/actuator` altını Spring Security ile korumak, hatta `management.server.port` ile ayrı porta ve iç ağa taşımak.
- **Özel uç nokta**: `@Endpoint` + `@ReadOperation`; `@RestController` yönetim altyapısına katılmaz.

### Tek cümlede

Actuator, çalışan uygulamanın sağlığını (liveness/readiness ayrımıyla), metriklerini (Micrometer cephesi üzerinden) ve izlerini (Sleuth değil, Micrometer Tracing + OpenTelemetry) standart uç noktalardan açar — ve bu uçlar hassas olduğu için varsayılan olarak kapalıdır.

# act-1 | Temel
## Soru
Spring Boot Actuator neyi sağlar?
## A
Uygulamanın derleme süresini kısaltan, build aşamasına takılan bir eklentidir
🤖 Actuator derlemeyle ilgilenmez; çalışma anındaki gözlemlenebilirlik sağlar.
## B
Veritabanı şemasını uygulama açılışında otomatik olarak migrate eder
🤖 Migrasyon Flyway/Liquibase işidir; Actuator'ın görevi değildir.
## C
Kod formatlama kurallarını zorunlu kılıp ihlal olunca build'i kırar
🤖 Formatlama araçları (Spotless vb.) ayrıdır; Actuator ile ilgisi yoktur.
## D
Çalışan uygulama için hazır sağlık, metrik ve bilgi uç noktaları sunar
🤖 Doğru: `/actuator/health`, `/metrics` gibi gözlemlenebilirlik uç noktalarını hazır getirir.
## Doğru
D
## Açıklama
**Spring Boot Actuator**, çalışan bir uygulamaya üretim düzeyinde **gözlemlenebilirlik ve yönetim uç noktaları** ekler. `spring-boot-starter-actuator` bağımlılığı eklenince `/actuator/health` (sağlık), `/actuator/metrics` (metrikler), `/actuator/info` (yapı bilgisi) gibi HTTP uç noktaları **tanımlanır** — ama HTTP'den varsayılan olarak yalnız `/actuator/health` açıktır, gerisi açıkça açılmalıdır; `/actuator/prometheus` ayrıca `micrometer-registry-prometheus` bağımlılığını ister. Amaç, uygulamanın iç durumunu (bellek, veritabanı bağlantısı, thread'ler, özel metrikler) dışarıya standart biçimde açmaktır; böylece izleme sistemleri, yük dengeleyiciler ve Kubernetes uygulamanın durumunu sorgulayabilir. Derleme, migrasyon veya formatlama Actuator'ın konusu değildir.

# act-2 | Temel
## Soru
Kubernetes'te bir pod'un "liveness" ve "readiness" probe'ları arasındaki fark nedir?
## A
İkisi de aynıdır; Kubernetes iki isim kullanır ama sonuçları birebir aynıdır
🤖 Sonuçları farklıdır: biri pod'u öldürür, diğeri yalnız trafiği keser.
## B
Liveness "uygulama ölü mü?"yü, readiness "trafiğe hazır mı?"yı yanıtlar
🤖 Doğru: liveness başarısızlığı restart, readiness başarısızlığı trafik kesme demektir.
## C
Liveness uygulamanın metriklerini, readiness ise loglarını toplayıp dışa aktarır
🤖 İkisi de veri toplamaz; sağlık durumunu bildiren probe'lardır.
## D
Readiness başarısız olunca pod öldürülür, liveness ise yalnız trafiği yönlendirir
🤖 Roller ters; pod'u öldüren liveness, trafiği yöneten readiness'tir.
## Doğru
B
## Açıklama
**Liveness probe** "uygulama takıldı/öldü mü?" sorusuna bakar. Başarısız olursa Kubernetes pod'u **öldürüp yeniden başlatır** — kalıcı bir kilitlenmeden kurtulmanın yolu. **Readiness probe** ise "bu pod şu an istek almaya hazır mı?" sorusuna bakar. Başarısız olursa Kubernetes pod'u **öldürmez**, yalnızca Service'in yük dengeleme havuzundan geçici olarak **çıkarır** (trafik göndermeyi keser); pod hazır olunca geri alır. Spring Boot bunları `/actuator/health/liveness` ve `/actuator/health/readiness` gruplarıyla açar. Bu ayrımı karıştırmak (readiness'i liveness gibi ele almak) geçici bir bağımlılık kesintisini gereksiz restart döngülerine çevirir.

# act-3 | Orta
## Soru
Micrometer'ın Spring Boot metriklerindeki rolü en iyi nasıl tanımlanır?
## A
Metrik üretimini satıcıdan bağımsız kılan bir cephe katmanıdır
🤖 Doğru: Micrometer, SLF4J'in loglama için yaptığını metrikler için yapar — soyut cephe.
## B
Kendi başına bir izleme (monitoring) sunucusudur; ayrıca bir Prometheus kurmaya gerek kalmaz
🤖 Micrometer sunucu değildir; metriği toplar ve bir arka uca aktarır, depolamayı arka uç yapar.
## C
Yalnızca JVM bellek metriklerini toplar; uygulamaya özel metrik üretilemez
🤖 Özel `Counter`/`Timer`/`Gauge` üretmek Micrometer'ın ana kullanımıdır.
## D
Logları yapılandırılmış JSON'a çeviren bir loglama kütüphanesidir
🤖 Bu loglama işidir; Micrometer metriklerle ilgilenir, loglarla değil.
## Doğru
A
## Açıklama
**Micrometer**, metrikler için bir **cephe/soyutlama katmanıdır** — tıpkı SLF4J'in loglama için yaptığı gibi. Uygulama kodu satıcıdan bağımsız API ile metrik üretir: `Counter` (artan sayaç), `Timer` (süre/gecikme), `Gauge` (anlık değer), `DistributionSummary` (dağılım). Altta bir veya daha fazla **kayıt** (registry) bu metrikleri belirli bir sistemin formatına yazar: Prometheus, OTLP, Datadog, CloudWatch. Böylece izleme arka ucunu değiştirmek uygulama kodunu değiştirmeden, yalnızca bağımlılık/konfigürasyon değişimiyle mümkün olur. Spring Boot Actuator, JVM ve HTTP metriklerini otomatik olarak Micrometer üzerinden yayınlar.

# act-4 | Orta
## Soru
Spring Boot'ta özel bir Actuator uç noktası (custom endpoint) nasıl tanımlanır?
## A
`application.properties` içine tek bir satır yazmak yeterlidir; hiç kod yazmak gerekmez
🤖 Property mevcut uç noktaları açıp kapar ama yeni bir uç noktanın davranışını kod olmadan tanımlamaz.
## B
`web.xml` içinde uç nokta için bir servlet eşlemesi tanımlayarak
🤖 Boot uygulamasında Actuator uç noktaları anotasyonla tanımlanır, `web.xml` ile değil.
## C
Bir bean'i `@Endpoint` ile işaretleyip `@ReadOperation` metotları eklemek
🤖 Doğru: `@Endpoint` + operasyon anotasyonları özel Actuator uç noktası oluşturur.
## D
Sıradan bir controller'a `@GetMapping("/actuator/ozel")` yazmak; Actuator onu otomatik tanır
🤖 Bu sıradan bir MVC uç noktası olur; Actuator altyapısına (health grubu, JMX, açık/kapalı yönetimi) katılmaz.
## Doğru
C
## Açıklama
Özel bir Actuator uç noktası, bir bean'i **`@Endpoint(id = "...")`** (ya da yalnız HTTP için `@WebEndpoint`) ile işaretleyip içine **`@ReadOperation`** (GET), **`@WriteOperation`** (POST), `@DeleteOperation` (DELETE) metotları koyarak tanımlanır:

```java
@Component
@Endpoint(id = "ozellikler")
public class OzellikEndpoint {
    @ReadOperation
    public Map<String, Boolean> ozellikler() {
        return Map.of("yeniAkis", true);
    }
}
```

Böyle bir uç nokta Actuator altyapısına tam entegre olur: `management.endpoints.web.exposure` ile açılıp kapatılır, JMX üzerinden de yayınlanır, güvenlik kurallarına tabi olur. Sıradan bir `@RestController` (D) bu yönetim altyapısına katılmaz; sadece property (A) yeni davranış tanımlamaz.

# act-5 | İleri
## Soru
Spring Boot 3.x (2026) ile dağıtık izleme (distributed tracing) kurmak isteyen bir ekip, eski projelerdeki `spring-cloud-sleuth` bağımlılığını arıyor ama bulamıyor. Doğru yaklaşım nedir?
## A
Sleuth'un son sürümünü elle sabitleyip (pin) kullanmaya devam etmek en güvenli yoldur
🤖 Sleuth artık geliştirilmiyor; eski sürüme çakılmak teknik borç ve güvenlik riski biriktirir.
## B
Sleuth kaldırıldı; izleme artık `micrometer-tracing` köprüleriyle yapılır
🤖 Doğru: Sleuth'un yerini Micrometer Tracing + OpenTelemetry (veya Brave) köprüsü aldı.
## C
Spring Boot 3'te dağıtık izleme desteği kaldırıldı; harici bir agent kullanmak şarttır
🤖 Destek kaldırılmadı; Micrometer Tracing ile birinci sınıf destek gelir.
## D
İzleme yalnızca WebFlux uygulamalarında mümkündür; MVC yığınında yapılamaz
🤖 İzleme her iki yığında da çalışır; yığın kısıtı yoktur.
## Doğru
B
## Açıklama
**Spring Cloud Sleuth**, Spring Boot 3 ile birlikte **kullanımdan kaldırıldı ve bakımı bırakıldı**. İzleme sorumluluğu artık **`micrometer-tracing`** projesine taşındı. Ekip, `micrometer-tracing-bridge-otel` (OpenTelemetry köprüsü) veya `micrometer-tracing-bridge-brave` bağımlılığını ekler; trace ve span'lar otomatik üretilir, MDC'ye `traceId`/`spanId` konur ve OTLP ile bir arka uca (Tempo, Jaeger, ticari APM) gönderilir. **OpenTelemetry köprüsü** endüstri standardı olduğu ve satıcı kilidini azalttığı için tercih edilir. Eski Sleuth'a çakılmak (A) yalnızca teknik borç biriktirir.

# act-6 | İleri
## Soru
Bir ekip üretimde tüm Actuator uç noktalarını `management.endpoints.web.exposure.include=*` ile açıp `/actuator`'ı kimlik doğrulamasız bırakmış. En ciddi risk nedir?
## A
Uygulama belirgin biçimde daha yavaş başlar, çünkü tüm uçlar açılışta hazırlanır
🤖 Başlangıç hızı asıl sorun değil; asıl risk hassas bilgi sızıntısıdır.
## B
Metrikler iki kez toplanır ve izleme arka ucundaki değerler ikiye katlanır
🤖 Çift toplama gibi bir sorun oluşmaz; risk güvenlik sızıntısındadır.
## C
Hiçbir risk yok; Actuator uç noktaları salt-okunurdur ve hassas veri döndürmez
🤖 Yanlış; `env`, `heapdump`, `threaddump` gibi uç noktalar hassas veri ve sırlar sızdırabilir.
## D
`env`, `heapdump`, `configprops` uçları sırları ve bellek dökümünü ifşa eder
🤖 Doğru: bu uç noktalar korumasızsa saldırgana sırlar ve iç durum açık olur.
## Doğru
D
## Açıklama
Tüm Actuator uç noktalarını açıp korumasız bırakmak ciddi bir **bilgi ifşası** açığıdır. `/actuator/env` ve `/actuator/configprops` ortam değişkenlerini ve yapılandırma özelliklerini (veritabanı parolaları, API anahtarları dahil, maskeleme yetersizse) gösterebilir. `/actuator/heapdump` tüm JVM heap'ini indirtir — içinde bellekteki token'lar, oturum verileri bulunabilir. `/actuator/threaddump` iç çağrı yığınlarını açar. Doğru yaklaşım: yalnız gerekli uç noktaları açmak (`exposure.include=health,info,prometheus`), `/actuator` altını Spring Security ile kimlik doğrulama/rol arkasına almak, hatta yönetim uç noktalarını ayrı bir porta (`management.server.port`) ve iç ağa taşımak. "Salt-okunur, zararsız" varsayımı (C) tehlikeli bir yanılgıdır.
