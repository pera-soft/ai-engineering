---
slug: dagitik-tracing
baslik: "Dağıtık İzleme: Trace/Span, W3C Bağlam Taşıma ve OpenTelemetry"
kademe: İleri
sira: 103
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo gönderdiğinizde elinize bir takip numarası verilir. Paket depoya, araca, şubeye ve kuryeye geçerken her durakta aynı numarayla kaydedilir. Sonunda tek ekranda "şu saatte şuradaydı" listesini görürsünüz.

Yazılımda da bir **istek (request)** çoğu zaman tek programda bitmez. İstek, kullanıcının bir düğmeye basıp sunucudan cevap beklediği tek işlemdir; bu tek işlem sekiz ayrı servise uğrayabilir. "Bu istek neden 3 saniye sürdü?" sorusunu tek bir servisin kayıt dosyası yanıtlayamaz, çünkü her servis yalnız kendi parçasını görür. **Dağıtık izleme (distributed tracing)** isteğe bir takip numarası verir ve uğradığı her durağı tek bir zaman çizelgesinde birleştirir.

### Temel kavramlar

- **İz (trace):** Bir isteğin uçtan uca tüm yolculuğu. Ortak kimliği `traceId`'dir ve yolculuk boyunca hiç değişmez.
- **Adım (span):** İz içindeki tek bir iş birimi — bir servisteki işlem, bir veritabanı sorgusu, bir dış çağrı. Kendi `spanId`'si ve başlangıç/bitiş zamanı vardır; bir metin satırı değil, süresi olan bir iş dilimidir.
- **Ebeveyn/çocuk (parent/child):** Her span, kendisini başlatan span'in kimliğini "parent" olarak taşır. Parent'ı olmayan ilk span **kök span**'dir (root span). Bu bağlar span'leri ağaç hâline getirir; ağacın tamamı iz'dir.
- **Başlık (HTTP header):** İstekte gövdeden ayrı taşınan "ad: değer" biçiminde küçük bilgi alanı. Bağlam bununla taşınır.
- **`traceparent`:** W3C Trace Context standardının başlığı. Biçimi `00-<32 onaltılık karakter trace-id>-<16 onaltılık karakter span-id>-<flags>`; baştaki `00` sürüm, sondaki iki karakter bayraktır (`01` = bu iz örneklendi). Yanındaki `tracestate` satıcıya özel ek bilgi taşır. Eski **B3** başlıkları (Zipkin) hâlâ görülür ama artık varsayılan standart değildir.
- **Örnekleme (sampling):** Her isteği saklamak pahalı olduğu için izlerin yalnız bir kısmını tutma kararı.
- **Korelasyon:** Ayrı yerlerde oluşan kayıtları ortak kimlikle eşleştirme. **Log satırı**, uygulamanın "şunu yaptım" diye dosyaya yazdığı tek satırlık metindir; içine `traceId` basılırsa log ile iz bağlanır.
- **MDC (Mapped Diagnostic Context):** SLF4J/Logback'te o an çalışan thread'e iliştirilen küçük anahtar-değer torbası. İzleme kütüphanesi `traceId`'yi buraya koyar, log deseni her satıra basar.
- **OpenTelemetry (OTel):** Satıcı-bağımsız izleme standardı. SDK ölçüm noktalarını üretir, dışa aktarıcı (exporter) OTLP protokolüyle gönderir, **Collector** toplayıp Jaeger, Tempo ya da Zipkin gibi bir arka uca yazar.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir trace, servisler boyunca span'lerden oluşur">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek trace = servisler boyunca span zinciri</text>
  <text x="20" y="52" font-size="10.5" fill="#5D7789" font-family="system-ui">traceId: 4bf9…a1  (tüm span'lerde ortak)</text>
  <rect x="20" y="62" width="620" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="30" y="79" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">span A · api-gateway  (kök span, parent yok)</text>
  <rect x="70" y="96" width="470" height="24" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="80" y="112" font-size="11" fill="#25804C" font-family="system-ui">span B · siparis-servisi  (parent: A)</text>
  <rect x="120" y="128" width="200" height="24" rx="6" fill="#FBF3E2" stroke="#B8860B" stroke-width="1.3"/>
  <text x="130" y="144" font-size="11" fill="#B8860B" font-family="system-ui">span C · db sorgu (parent: B)</text>
  <rect x="335" y="128" width="205" height="24" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="345" y="144" font-size="11" fill="#B23B3B" font-family="system-ui">span D · stok-servisi ⏱ YAVAŞ</text>
  <text x="20" y="182" font-size="10.5" fill="#5D7789" font-family="system-ui">traceparent başlığı (W3C) her ağ atlamasında taşınır → span'ler birbirine bağlanır</text>
  <text x="20" y="199" font-size="10.5" fill="#B23B3B" font-family="system-ui">kök-neden: uzun span D → gecikme stok-servisinde</text>
</svg>
```

### Nasıl çalışır?

1. **İz başlar.** İstek sisteme ilk giren serviste (ör. api-gateway) karşılanır; kütüphane yeni bir `traceId` üretir ve parent'ı olmayan kök span'i açar.
2. **Örnekleme kararı verilir** ve `traceparent`'ın bayrak alanına yazılır — yani karar, kimliğin yanında yolculuğa çıkar.
3. **Bağlam taşınır (context propagation).** Servis başka bir servisi çağırırken giden isteğe `traceparent` başlığını enjekte eder: içinde ortak `traceId` ve kendi `spanId`'si (çocuğun parent'ı) bulunur.
4. **Alan servis başlığı okur** ve kendi span'ini bu ebeveynin altına iliştirir. Bayraktaki örnekleme kararı olduğu gibi taşınır; böylece izin bazı span'leri tutulup bazıları atılmaz.
5. **Span'ler dışa aktarılır.** Biten span'ler biriktirilip toplu hâlde OTel Collector'a gönderilir. Arka uç, aynı `traceId`'li span'leri parent bağlarına göre **tek bir zaman çizelgesinde** (waterfall) birleştirir.
6. **Log'a `traceId` basılır.** Bağlam MDC'ye de yazıldığı için her log satırında `traceId` görünür; hatalı satırdan izin tamamına geçilebilir.

Kritik nokta şudur: bağlam **çağrı anında** başlıkla taşınmalıdır. Sonradan yapılan hiçbir eşleştirme bu bağı kuramaz. Kayıtları veritabanına yazıp gece toplu işle birleştirmek de, IP adresi veya zaman damgası yakınlığına bakmak da hangi çağrının hangisini tetiklediğini söyleyemez.

Elle rastgele bir kimlik üretmek de yetmez. Ortak bir biçim ve span ağacı olmadan farklı dillerdeki kütüphaneler birbirinin ürettiğini okuyamaz. Geriye maliyet sorunu kalır: trafiğin tamamını saklamak pahalıdır. Ne kadarını tutacağımıza örnekleme karar verir.

```svg
<svg viewBox="0 0 660 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="traceparent başlığı servisler arası bağlam taşır">
  <defs>
    <marker id="trc-pp-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bağlam taşıma: W3C traceparent başlığı</text>
  <rect x="30" y="70" width="200" height="70" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="130" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">çağıran servis (B)</text>
  <text x="130" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">span kimliğini enjekte eder</text>
  <rect x="430" y="70" width="200" height="70" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="530" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">alan servis (C)</text>
  <text x="530" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">okur, ebeveynin altına bağlar</text>
  <path d="M230 92 L428 92" fill="none" stroke="#186399" stroke-width="1.5" marker-end="url(#trc-pp-arw)"/>
  <text x="330" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">HTTP çağrısı (aynı traceId)</text>
  <rect x="250" y="98" width="160" height="30" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1"/>
  <text x="330" y="118" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">traceparent: 00-4bf9…a1-…-01</text>
</svg>
```

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Head-based ve tail-based örnekleme karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Örnekleme: head-based vs tail-based</text>
  <rect x="28" y="44" width="290" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="173" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">head-based (baştan)</text>
  <text x="173" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">karar trace BAŞLARKEN verilir</text>
  <text x="173" y="118" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ ucuz, basit dağıtım</text>
  <text x="173" y="140" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− nadir hata/yavaşı kaçırabilir</text>
  <text x="173" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tampon gerekmez</text>
  <rect x="342" y="44" width="290" height="150" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="487" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#6B3FA0" font-family="system-ui">tail-based (kuyruktan)</text>
  <text x="487" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">karar trace BİTİNCE verilir</text>
  <text x="487" y="118" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">+ hatalı/yavaşı tercihen tutar</text>
  <text x="487" y="140" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− tüm span'ler tamponlanır (pahalı)</text>
  <text x="487" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">daha çok bellek/altyapı</text>
</svg>
```

### Mülakatta ne sorulur?

- **Baştan mı, sondan mı örnekleme?** Head-based (baştan) örneklemede karar iz **başlarken** verilir. Ucuzdur ve hiçbir şeyi bellekte biriktirmeyi gerektirmez. Ama karar, sonuç görülmeden verilir; bu yüzden nadir bir hata ya da yavaş istek elden kaçabilir. Tail-based (sondan) örneklemede karar iz **bittikten sonra** verilir: "hatalıysa veya çok yavaşsa tut". Bedeli şudur: karar anına kadar izin **tüm span'lerini** bir toplayıcıda geçici olarak bellekte tutmak gerekir. Bu da daha çok bellek ve daha pahalı altyapı demektir. Kıdemli bir adayın tipik cevabı ikisini birleştirmektir: yüksek hacimli akışta head-based, kritik akışlarda tail-based.
- **Kararın tutarlılığı:** Örnekleme kararı iz boyunca aynı kalmalıdır; her servis kendi zarını atarsa yarım izler oluşur, bu yüzden karar `traceparent` bayrağında taşınır.
- **`traceparent` biçimi:** Farklı ekiplerin farklı dillerdeki kütüphaneleri ancak ortak bir başlık biçimini okuyabilirse span'ler tek iz altında birleşir.
- **Kopuk iz (broken trace):** Başlık isteğe eklenmezse ya da bağlam thread sınırında kaybolursa alt taraf yeni ve ilişkisiz bir iz açar. Klasik tuzak şudur: aktif bağlam `ThreadLocal` gibi thread'e özgü bir alanda durur. İş `@Async`'e, ayrı bir `ExecutorService`'e veya reaktif bir zamanlayıcıya geçtiğinde bu bağlam **otomatik taşınmaz**. Çözüm, bağlamı açıkça yaymaktır: Micrometer `ContextSnapshot`, `TaskDecorator` veya reaktif `Context` bunun içindir.
- **Asenkron ve kuyruk:** Kafka/RabbitMQ'da bağlam HTTP başlığı yerine mesaj başlığına konur; tüketici oradan çıkarıp kendi span'ini bağlar.
- **Log-iz korelasyonu:** MDC ile `traceId` basmak "logdan ize" ve "izden loga" gidişi mümkün kılar.
- **Metrik mi, iz mi?** Hata oranı gibi sayaçlar durumun "ne kadar kötü" olduğunu söyler ama "nerede" olduğunu söylemez. Kök nedeni gösteren şey zaman çizelgesidir. Çizelge her span'in ne kadar sürdüğünü ve **kritik yolu (critical path)**, yani hangi çağrının hangisini beklediğini ortaya koyar.
- **Spring tarafı:** Spring Cloud Sleuth, Spring Boot 3 ile son buldu ve işlevi **Micrometer Tracing**'e devredildi. Yeni bir projede `micrometer-tracing-bridge-otel` köprüsü ve bir OTLP dışa aktarıcı eklenir. Çerçeve tek başına span üretip dışarı göndermez. Son olarak şunu unutmayın: izleme bir **gözlem** aracıdır. Nedeni görünür kılar ama yavaş servisi kendiliğinden onarmaz ve trafikten çıkarmaz; o iş devre kesicinin ve yük dengeleyicinin işidir.

### Tek cümlede

Dağıtık izleme, isteğe kargo takip numarası gibi bir `traceId` verip her duraktaki adımı (span) `traceparent` başlığıyla ebeveyn-çocuk zincirine bağlar; böylece gecikmenin hangi serviste biriktiği tek zaman çizelgesinde görünür.

# trace-1 | İleri
## Soru
Dağıtık izlemede **trace** ile **span** arasındaki ilişki nedir?
## A
Trace bir isteğin uçtan uca yolculuğudur (`traceId`); span onun içindeki tek iş birimidir (`spanId` + parent referansı)
🤖 Doğru: bir trace, ebeveyn-çocuk bağlarıyla ağaç oluşturan birçok span'den meydana gelir.
## B
Span bir isteğin uçtan uca yolculuğudur; trace ise span içindeki tek adımdır — bu yüzden `traceId` her adımda yeniden üretilir
🤖 tanımlar yer değiştirmiş; ayrıca `traceId` yolculuk boyunca sabit kalır, her adımda yeniden üretilmez.
## C
Trace ile span aynı şeyin iki adıdır; ikisi de tek bir servisin ürettiği tek bir zaman damgalı kayıt satırını anlatır
🤖 span bir metin satırı değil, başlangıç/bitiş zamanı olan bir iş birimidir; trace ise bu birimlerin bütünüdür.
## D
Trace yalnız bir servis içinde geçerlidir; servisler arası bağı ise her serviste yeniden üretilen bağımsız span'ler kurar
🤖 trace tam da servis sınırlarını aşan bütündür; ortak kimlik olmadan bağımsız span'ler hiçbir bağ kuramaz.
## Doğru
A
## Açıklama
**Trace**, bir kullanıcı isteğinin sisteme girişinden çıkışına kadar olan tüm yolculuğudur ve benzersiz bir `traceId` ile işaretlenir; bu kimlik yolculuk boyunca değişmez. **Span**, bu yolculuk içindeki **tek bir iş birimidir** (bir servisteki işlem, bir DB sorgusu, bir dış çağrı) ve kendi `spanId`'si, başlangıç/bitiş zamanı ve **parent span** referansı vardır. Parent referansları span'leri bir **ağaca** (kök span → alt span'ler) bağlar; trace bu ağacın tamamıdır. Tanımları tersine çeviren, `traceId`'yi her adımda yenileyen, span'i tek bir log satırına indirgeyen ya da trace'i tek servise hapseden seçenekler yanlıştır. Bu ağaç yapısı, gecikmenin kritik yolda (critical path) nerede biriktiğini görmeyi sağlar.

# trace-2 | İleri
## Soru
İki mikroservis farklı ekiplerce yazıldı ama span'lerinin **tek trace** altında birleşmesi gerekiyor. Bunu standart ve birlikte-çalışır (interoperable) biçimde ne sağlar?
## A
Her servisin gördüğü `traceId`'yi kendi veritabanına yazması ve gece çalışan toplu işin (batch) bu kayıtları sonradan eşleştirmesi
🤖 bağlam çağrı anında başlıkla taşınmazsa alt servis o kimliği hiç görmez; sonradan eşleştirilecek kayıt da oluşmaz.
## B
Her iki servisin de aynı loglama kütüphanesini ve aynı log biçimini kullanması; ek bir HTTP başlığına gerek kalmaz
🤖 log kütüphanesi ne kadar aynı olursa olsun bağlamı ağ üzerinden taşımaz; taşımanın kendisi başlık ister.
## C
W3C Trace Context: bağlam her HTTP çağrısında `traceparent` başlığıyla taşınır, alan servis span'ini ebeveynin altına bağlar
🤖 Doğru: `traceparent`, farklı diller ve kütüphaneler arasında ortak, standart bağlam taşıma biçimidir.
## D
İsteklerin kaynak IP adresine ve zaman damgası yakınlığına bakarak toplayıcının çağrıları sonradan kendiliğinden ilişkilendirmesi
🤖 IP/zaman eşleme kırılgandır (NAT, yük dengeleyici, bağlantı yeniden kullanımı) ve neden-sonuç zincirini kuramaz.
## Doğru
C
## Açıklama
Span'lerin tek trace altında birleşmesi **bağlam taşımayı** (context propagation) gerektirir: `traceId` ve çağıran span'in `spanId`'si her ağ atlamasında (network hop) alan servise iletilmelidir. **W3C Trace Context** bunun satıcı/dil-bağımsız standardıdır — bağlam `traceparent` (ve opsiyonel `tracestate`) HTTP başlığında taşınır; biçimi `00-<32 hex trace-id>-<16 hex span-id>-<flags>` şeklindedir. Çağıran servis giden isteğe kendi span kimliğini enjekte eder, alan servis bunu okuyup kendi span'ini bu **ebeveynin** altına bağlar. Aynı log kütüphanesini kullanmak bağlamı ağ üzerinden taşımaz; veritabanına yazıp gece eşleştirmek gerçek zamanlı değildir ve kimlik alt servise hiç ulaşmaz; IP/zaman damgası yakınlığı ise hangi çağrının hangisini tetiklediğini kanıtlayamaz.

# trace-3 | Orta
## Soru
Spring Boot 3+ tabanlı yeni bir mikroservise dağıtık izleme ekliyorsun. 2026'da doğru bağımlılık seçimi nedir?
## A
Spring Cloud Sleuth eklemek; Sleuth, Spring Boot 3 hattında da bakımdadır ve hâlâ önerilen standart izleme kütüphanesidir
🤖 Sleuth'un geliştirmesi Spring Boot 3 ile sona erdi; yeni bir projede tercih edilmez.
## B
Micrometer Tracing kullanmak: `micrometer-tracing-bridge-otel` köprüsü ve bir OTLP dışa aktarıcı bağımlılığı eklemek
🤖 Doğru: Sleuth'un işlevi Micrometer Tracing'e devredildi; altta OTel ya da Brave köprüsü çalışır.
## C
Ek bir bağımlılık gerekmez; Spring MVC gelen her isteğe kendiliğinden span üretip bunları toplayıcıya dışa aktarır
🤖 ölçüm noktası ve dışa aktarım için köprü/dışa aktarıcı bağımlılıkları açıkça eklenmelidir.
## D
Yalnızca Logback yapılandırıp her istekte rastgele bir `traceId` üretmek ve bunu log satırına basmak yeterlidir
🤖 elle üretilen kimlik ne standart başlıkla taşınır ne de ebeveyn-çocuk span ağacını kurar.
## Doğru
B
## Açıklama
**Spring Cloud Sleuth** Spring Boot 3 ile son buldu; işlevi **Micrometer Tracing**'e taşındı, dolayısıyla "hâlâ önerilen kütüphane" seçeneği yanlıştır. Micrometer Tracing bir **cephe/köprü** (facade/bridge) katmanıdır: altta **OpenTelemetry** (`micrometer-tracing-bridge-otel`) veya Brave kullanır ve span'leri OTLP gibi bir protokolle bir toplayıcıya (collector) dışa aktarır. 2026 seçimi: `micrometer-tracing-bridge-otel` + bir OTLP dışa aktarıcı, gerekirse `micrometer-observation` ile otomatik ölçüm. Spring MVC tek başına span üretip dışa aktarmaz; `traceId`'yi elle üretmek ise ne W3C `traceparent` ile standart taşımayı ne de span ağacını sağlar — en fazla log satırına bir metin basar.

# trace-4 | İleri
## Soru
Yüksek trafikli bir sistemde tüm istekleri izlemek depolama ve performans açısından pahalı. Head-based ve tail-based örnekleme (sampling) arasındaki temel ödünleşim (trade-off) nedir?
## A
Head-based örnekleme kararı trace bittikten sonra verdiği için her zaman daha doğrudur; tail-based ise trace başlarken kör bir yüzde seçer
🤖 adlandırma tersine çevrilmiş — kararın verildiği an ile yöntemin adı bu seçenekte uyuşmuyor.
## B
İkisi de kararı trace başlarken verir; aradaki tek fark kararın istemcide mi yoksa toplayıcıda mı kaydedildiğidir
🤖 fark kaydın yerinde değil, kararın verildiği anda ve bunun getirdiği tampon ihtiyacındadır.
## C
Tail-based ucuzdur ve hiç tampon istemez; span'leri toplayıcıda biriktirip pahalı altyapı gerektiren ise head-based'dir
🤖 maliyet ve tampon ihtiyacı bu seçenekte iki yönteme ters atanmış.
## D
Head-based kararı trace başlarken verir (ucuz, nadir hatayı kaçırabilir); tail-based trace bitince karar verir ama span'leri tamponlar
🤖 Doğru: ucuz-ama-kör (head) ile akıllı-ama-pahalı (tail) arasındaki klasik ödünleşim.
## Doğru
D
## Açıklama
**Örnekleme**, trace'lerin yalnız bir kısmını saklayarak maliyeti düşürür. **Head-based** (baştan) sampling kararı trace **başlarken** verilir (ör. %1'i tut) — ucuzdur ve dağıtımı basittir, ama karar sonucu görmeden verildiği için o %1'e düşmeyen **nadir bir hata veya yavaş trace kaçabilir**. **Tail-based** (kuyruktan) sampling kararı trace **bittikten sonra** verir; böylece "hatalıysa veya yavaşsa tut" gibi akıllı kurallar uygulanır — ama karar için trace'in **tüm span'lerinin** bir toplayıcıda karar anına kadar geçici tamponlanması gerekir, bu da daha fazla bellek/altyapı demektir. İki yöntemin adını, maliyetini veya tampon ihtiyacını yer değiştiren seçenekler yanlıştır; fark "kararın nerede kaydedildiği" değil, **kararın verildiği andır**. Ayrıca karar trace boyunca tutarlı taşınmalıdır (`traceparent` bayrağı), yoksa yarım trace'ler oluşur. Senior tercih genelde: yüksek hacimde head-based + kritik hatalar için tail-based birlikte.

# trace-5 | İleri
## Soru
Bir servis, gelen isteği bir `@Async` metotta / ayrı bir thread havuzunda işliyor ve o thread'den yapılan downstream çağrının trace'i **kopuyor** (yeni, ilişkisiz bir trace başlıyor). En olası neden nedir?
## A
Trace bağlamı thread-yerel (`ThreadLocal`) tutulur; iş yeni thread'e geçerken açıkça taşınmazsa bağlam kaybolur
🤖 Doğru: thread sınırında bağlam elle yayılmazsa yeni thread boş bağlamla başlar ve span ağacı kopar.
## B
Aynı JVM içinde iki thread aynı `traceId`'yi paylaşamaz; bu nedenle ikinci thread zorunlu olarak yeni bir trace açar
🤖 `traceId` sıradan bir değerdir, thread'ler arasında paylaşılmasını engelleyen bir kural yoktur.
## C
Downstream servis W3C Trace Context'i desteklemediği için gelen `traceparent` başlığını yok sayıp yeni bir trace başlatır
🤖 kopma çağıran tarafta oluşur; downstream'in desteği burada belirleyici değildir.
## D
Örnekleme kararı yalnız ana thread için verildiğinden, ikinci thread örneklenmeyip kendi trace'ini yeni bir trace olarak açar
🤖 örneklenmemiş bir iz yeni trace açmaz, yalnızca kaydedilmez; ayrıca karar bağlamla birlikte taşınır.
## Doğru
A
## Açıklama
Trace bağlamı (aktif `traceId` + geçerli `spanId`) çalışırken genelde **`ThreadLocal`** benzeri thread-yerel bir bağlamda tutulur. İş **başka bir thread'e** geçtiğinde (`@Async`, `ExecutorService`, reaktif zamanlayıcı) bu bağlam otomatik **taşınmaz**; yeni thread'de aktif bağlam boştur, dolayısıyla oradan başlayan çağrı yeni ve ilişkisiz bir trace açar (span ağacı kopar). Çözüm bağlamı thread sınırında **açıkça yaymaktır**: Micrometer'ın `ContextSnapshot`/`ContextExecutorService` sarmalayıcıları, `TaskDecorator` veya reaktif `Context` ile. Sorun ne `traceId`'nin thread'ler arasında paylaşılamaması (böyle bir kısıt yoktur), ne downstream'in desteği (bağlam zaten başlığa hiç konmadığı için karşı taraf okuyacak bir şey bulamaz), ne de örnekleme kararıdır (örneklenmemiş iz yeni bir trace açmaz, yalnızca kaydedilmez) — sorun **thread sınırında yayılmayan bağlamdır**.

# trace-6 | İleri
## Soru
Bir kullanıcı isteği 8 servise dokunuyor ve toplam gecikme 3 saniye. Dağıtık izleme kök-nedeni (root cause) bulmayı loglara kıyasla neden hızlandırır?
## A
Tüm servislerin hata mesajlarını tek bir dosyaya kopyalayıp aranacak metin miktarını azalttığı için arama hızlanır
🤖 kök-nedeni gösteren şey hata metnini bir yere yığmak değil, span sürelerini ve sıralarını görmektir.
## B
Her servisin istek adedini ve hata oranını sayaç olarak topladığı için gecikmenin kaynağı doğrudan sayaçlardan okunur
🤖 sayaçlar sorunun büyüklüğünü verir ama tek bir isteğin hangi durakta beklediğini söylemez.
## C
Trace'i tek bir zaman çizelgesi olarak sunar; hangi span'in uzun sürdüğünü ve kritik yolu bir bakışta gösterir
🤖 Doğru: zaman çizelgesindeki uzun span, gecikmenin biriktiği servisi doğrudan işaret eder.
## D
Yavaş bulduğu servisi kendiliğinden trafiğe kapatıp isteği hızlı bir kopyaya yönlendirdiği için sorun ortadan kalkar
🤖 trafiği yönlendirmek devre kesici/yük dengeleyici işidir; izleme yalnız gözlemler, müdahale etmez.
## Doğru
C
## Açıklama
Ayrık loglarla 8 servisin `traceId`'sini elle eşleştirmek yavaş ve hataya açıktır. Dağıtık izleme ise trace'i **tek bir zaman çizelgesi** (span ağacı, waterfall) olarak sunar: her span'in süresi ve ebeveyn-çocuk ilişkisi görünür olduğundan, **uzun bir span** darboğazın (bottleneck) hangi serviste olduğunu ve **kritik yolu** (critical path — hangi çağrı hangisini bekliyor) bir bakışta gösterir. Bu, gecikmenin nerede biriktiğini anında işaret eder. Hata metinlerini tek dosyada toplamak hangi çağrının hangisini beklediğini göstermez; sayaçlar (istek adedi, hata oranı) sorunun büyüklüğünü söyler ama tek bir isteğin hangi durakta takıldığını söylemez. Trace bir **gözlem** aracıdır: nedeni görünür kılar, trafiği yönlendirmez veya servisi kendiliğinden onarmaz — o iş devre kesici/yük dengeleyici katmanınındır. Log'lardan ize geçişi kolaylaştıran şey de MDC ile her satıra basılan `traceId`'dir.
