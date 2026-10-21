---
slug: resilience-circuit-breaker
baslik: "Resilience4j: Devre Kesici, Retry+Jitter ve Bulkhead"
kademe: İleri
sira: 101
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş merkezinde tek dükkânda yangın çıktığında yangın kapıları kapanır: o dükkân zarar görür ama bina ayakta kalır. Birçok küçük programın ağ üzerinden birbirini çağırdığı sistemlerde de aynı şey gerekir. Çağırdığımız program yavaşlar veya hiç cevap vermezse, bizim programımız onu bekleye bekleye kilitlenir; bizi bekleyenler de kilitlenir ve arıza zincirleme büyür. Buradaki kalıplar o yangın kapılarının yazılımdaki karşılığıdır: arızayı tek odada tutmak ve kendimizi ayakta tutmak.

### Temel kavramlar

- **Servis (service):** ağ üzerinden çağrılabilen, kendi başına çalışan uygulama parçası.
- **Aşağı akış servisi (downstream):** bizim çağırdığımız, bize cevap üretmesi gereken servis; ona bağımlıyız.
- **Hata toleransı / dayanıklılık (fault tolerance / resilience):** bir parça bozulduğunda sistemin tümüyle durmaması, hasarı sınırlı alanda tutabilmesi.
- **Zincirleme çöküş (cascading failure):** tek servisin arızasının onu bekleyen çağıranları sırayla kilitleyip tüm sistemi devirmesi.
- **Zaman aşımı (timeout):** "en fazla şu kadar bekle, sonra vazgeç" sınırı. Sınırsız bekleme en sinsi arızadır: hata vermez, sadece herkesi bekletir.
- **İş parçacığı (thread):** programın işleri aynı anda yürütmesini sağlayan çalışma hattı. Sayısı sınırlıdır; hepsi yavaş bir servisi beklerken tükenirse uygulama yeni istek kabul edemez.
- **Eşzamanlılık (concurrency):** aynı anda kaç işin yürüdüğü. Sınırlamak, tek bir bağımlılığın bütün thread'leri yutmasını önler.
- **Gecikme (latency):** cevabın gelme süresi; hata vermeyen ama çok yavaş servis de arızadır.
- **Geçici hata (transient fault):** kendiliğinden düzelen kısa ömürlü hata, ağ dalgalanması gibi.
- **Idempotent işlem:** aynı isteği bir kez de beş kez de gönderseniz sonucu değişmeyen işlem. `GET`, `PUT`, `DELETE` böyledir; "ödeme al" gibi bir `POST` değildir.
- **Resilience4j:** bu kalıpları hazır sunan, çağrıyı katman katman saran (dekoratör) hafif Java kütüphanesi; 2026'da fiili standart. Netflix'in eski kütüphanesi **Hystrix** 2018'de bakım moduna (maintenance mode) alındı, yeni özellik almıyor. Spring çekirdeği tek başına devre kesici sunmaz; Spring Cloud Circuit Breaker soyutlaması altta Resilience4j'yi kullanır. Kubernetes'in liveness probe'u ayrı katmandır: sağlıksız pod'u yeniden başlatır, tek tek çağrıları reddedemez.

### Nasıl çalışır?

**Devre kesici (circuit breaker)** evdeki sigortanın yazılım karşılığıdır. Üç durumu olan bir **durum makinesidir (state machine)**:

- **Closed (kapalı)**: çağrılar geçer ve izlenir. İzleme, son N çağrıyı tutan bir **kayan pencerede (sliding window)** yapılır. İki oran birden ölçülür: hata oranı (`failureRateThreshold`) ve yavaş-çağrı oranı (`slowCallRateThreshold`). `slowCallDurationThreshold` süresini aşan bir çağrı, hata vermese bile "yavaş" sayılır.
- **Open (açık)**: eşiklerden biri aşılınca bu duruma geçilir. Çağrılar aşağı akıştaki servise hiç gitmez; `CallNotPermittedException` ile anında reddedilir. Buna **hızlı red (fail-fast)** denir. Amaç arızalı servise nefes aldırmaktır.
- **Half-Open (yarı-açık)**: `waitDurationInOpenState` süresi dolunca girilir. Varsayılanda geçiş **tembeldir**: süre dolduktan sonra gelen ilk çağrı kesiciyi bu duruma alır. `automaticTransitionFromOpenToHalfOpenEnabled` açılırsa geçiş çağrı beklemeden zamanlayıcıyla yapılır. Yalnız `permittedNumberOfCallsInHalfOpenState` kadar deneme çağrısı geçirilir. Bu denemelerin hata oranı eşiğin altındaysa Closed'a dönülür; değilse yeniden Open olunur.

Dikkat edilecek bir nokta: bekleyen çağrıların Open'dan çıkarken toplu hâlde salıverilmesi diye bir davranış **yoktur**. Olsaydı, yeni toparlanan servisi ilk saniyede tekrar devirirdi.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Devre kesici üç durum: Closed, Open, Half-Open">
  <defs>
    <marker id="r4j-ok" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="r4j-bad" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Devre kesici durum makinesi</text>
  <rect x="30" y="70" width="150" height="70" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="105" y="100" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Closed</text>
  <text x="105" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çağrılar geçer, izlenir</text>
  <rect x="480" y="70" width="150" height="70" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="555" y="100" text-anchor="middle" font-size="14" font-weight="700" fill="#B23B3B" font-family="system-ui">Open</text>
  <text x="555" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">anında reddet (fail-fast)</text>
  <rect x="255" y="70" width="150" height="70" rx="12" fill="#FBF3E2" stroke="#B8860B" stroke-width="1.6"/>
  <text x="330" y="100" text-anchor="middle" font-size="14" font-weight="700" fill="#B8860B" font-family="system-ui">Half-Open</text>
  <text x="330" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sınırlı deneme</text>
  <path d="M180 92 L478 92" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#r4j-bad)"/>
  <text x="330" y="58" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">eşik aşıldı (hata/yavaş oranı)</text>
  <path d="M480 128 L405 118" fill="none" stroke="#B8860B" stroke-width="1.6" marker-end="url(#r4j-bad)"/>
  <text x="440" y="165" text-anchor="middle" font-size="10.5" fill="#B8860B" font-family="system-ui">waitDuration doldu</text>
  <path d="M255 128 L180 118" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#r4j-ok)"/>
  <text x="215" y="165" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">deneme başarılı</text>
  <path d="M330 140 L330 175 L540 175 L540 142" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#r4j-bad)"/>
  <text x="435" y="195" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">deneme başarısız → yeniden Open</text>
</svg>
```

**Yeniden deneme (retry)** geçici hatada işe yarar. Ama sabit aralıkla değil: bekleme süresi her denemede katlanmalıdır. Buna **üstel geri çekilme (exponential backoff)** denir — 200 ms, 400 ms, 800 ms.

Bu tek başına yetmez. Arızadan aynı anda etkilenen binlerce istemci aynı bekleme eğrisini izler. Hepsi geri sayımı aynı anda bitirir ve birlikte yeniden dener. Bu eşzamanlı dalgaya **sürü hücumu (thundering herd)** denir ve yeni toparlanan servisi tekrar devirir. Çözüm, her beklemeye **jitter (rastgele sapma)** eklemektir; Resilience4j'de bunu `ofExponentialRandomBackoff` yapar. Deneme sayısı da sınırlı olmalıdır, çünkü sınırsız yeniden deneme arızalı servisi dövmeye devam eder.

En kritik koşul şudur: **yeniden deneme yalnız idempotent işlemde güvenlidir.** Zaman aşımına uğrayan bir çağrının sunucuda işlenip işlenmediğini bilemeyiz. Idempotent olmayan bir `POST`'u yeniden denemek çift ödeme üretir. Çare, istemcinin başlıkta bir **tekrar-güvenlik anahtarı (idempotency key)** göndermesidir. Sunucu aynı anahtarı ikinci kez görünce işlemi tekrarlamaz, ilk sonucu döndürür.

**Zaman sınırlayıcı (TimeLimiter)** çağrıya üst süre koyar; süre aşılınca çağrı iptal edilir ve "yavaş çağrı" olarak devre kesicinin sayacına yazılır.

**Bölme duvarı (bulkhead)** adını gemilerden alır: su alan bölme, geminin tamamını batırmaz. Yazılımda karşılığı şudur — her bağımlılığa ayrı bir eşzamanlılık bütçesi verilir. Böylece tek bir yavaş bağımlılık bütün thread'leri yutamaz. İki türü vardır.

`SemaphoreBulkhead` yalnız bir sayaç tutar. Sınır aşılırsa yeni çağrı reddedilir. Çağrı **çağıran thread'in içinde** çalışır; bu yüzden hafiftir ve `ThreadLocal`/`SecurityContext` bağlamı olduğu gibi korunur.

`ThreadPoolBulkhead` ise çağrıyı **ayrı bir thread havuzuna ve kuyruğa** taşır. Yavaş bağımlılık artık çağıranın thread'ini bloklamaz; izolasyon tamdır. Bedeli iki kalemdir: thread'ler arası geçişin maliyeti (context switch) ve `ThreadLocal`'a bağlı bağlamın (güvenlik bilgisi, MDC log alanları) otomatik taşınmaması. Bu tür yalnız reaktif akışlara özgü değildir; `CompletableFuture` döndürerek senkron kodda da kullanılır.

**Dekoratör sırası** davranışı doğrudan belirler. Sebebi şu: dekoratörler iç içe sarılan fonksiyonlardır, yani sıraları değiştirilince sonuç da değişir (matematikteki deyimle **değişmeli / commutative** değildirler). Varsayılan sarma sırası dıştan içe şöyledir: `Retry( CircuitBreaker( RateLimiter( TimeLimiter( Bulkhead( çağrı )))))`.

Retry en dışta olduğu için her yeniden deneme devre kesiciden **baştan** geçer. Devre Open ise deneme anında reddedilir ve arızalı servis boşuna dövülmez. Ters sırada ise devre kesici, retry döngüsünün tamamını tek bir çağrı gibi görür; ara denemeler hızlı-red korumasından yararlanamaz. Spring Boot'ta bu sıra `resilience4j.*.aspect-order` ayarlarıyla değiştirilir. Aşağıdaki kod eşikleri ve sarma sırasını gösterir.

```java
var cb = CircuitBreaker.of("odeme", CircuitBreakerConfig.custom()
        .slidingWindowType(SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(20)
        .failureRateThreshold(50)                       // %50 hata → Open
        .slowCallRateThreshold(80)                      // %80 yavaş → Open
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        .waitDurationInOpenState(Duration.ofSeconds(10))
        .permittedNumberOfCallsInHalfOpenState(3)
        .build());

var retry = Retry.of("odeme", RetryConfig.custom()
        .maxAttempts(3)
        .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(  // backoff + jitter
                Duration.ofMillis(200), 2.0, 0.5))
        .build());

// Retry en dışta: her deneme devre kesiciden geçer
Supplier<Yanit> korunan = Retry.decorateSupplier(retry,
        CircuitBreaker.decorateSupplier(cb, () -> istemci.cagir(istek)));
```

```svg
<svg viewBox="0 0 660 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Retry üstel backoff ve jitter zaman ekseni">
  <defs>
    <marker id="r4j-rt-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Retry: üstel backoff + jitter (denemeleri zamana yayar)</text>
  <rect x="150" y="120" width="44" height="30" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1"/>
  <rect x="330" y="120" width="80" height="30" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1"/>
  <line x1="40" y1="150" x2="620" y2="150" stroke="#5D7789" stroke-width="1.4" marker-end="url(#r4j-rt-arw)"/>
  <text x="612" y="170" text-anchor="end" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman →</text>
  <circle cx="70" cy="150" r="6" fill="#8A2E2E"/>
  <text x="70" y="112" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">1. deneme</text>
  <text x="70" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(ilk hata)</text>
  <circle cx="172" cy="150" r="6" fill="#186399"/>
  <text x="172" y="112" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">2. deneme</text>
  <circle cx="370" cy="150" r="6" fill="#186399"/>
  <text x="370" y="112" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">3. deneme (son)</text>
  <text x="121" y="180" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">~200ms</text>
  <text x="271" y="180" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">~400ms (×2 backoff)</text>
  <text x="470" y="135" font-size="10.5" fill="#B26B00" font-family="system-ui">turuncu = ± jitter penceresi</text>
</svg>
```

```svg
<svg viewBox="0 0 660 262" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Resilience4j dekoratör sarma sırası, dıştan içe">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dekoratör sarma sırası (dıştan içe)</text>
  <rect x="20" y="40" width="620" height="200" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="330" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Retry (en dışta)</text>
  <rect x="48" y="66" width="564" height="166" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">CircuitBreaker</text>
  <rect x="76" y="92" width="508" height="140" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="330" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">RateLimiter</text>
  <rect x="104" y="118" width="452" height="114" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="330" y="136" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">TimeLimiter</text>
  <rect x="132" y="144" width="396" height="88" rx="9" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="162" text-anchor="middle" font-size="12" font-weight="700" fill="#5D7789" font-family="system-ui">Bulkhead</text>
  <rect x="230" y="176" width="200" height="44" rx="9" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.5"/>
  <text x="330" y="203" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">çağrı (downstream)</text>
  <text x="330" y="252" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Retry dışta → devre Open iken her deneme hızlıca reddedilir, downstream boşa dövülmez</text>
</svg>
```

### Mülakatta ne sorulur?

Kıdemli seviyede soyut cevap kabul edilmez; eşiklerin adı ve gerekçesi istenir.

- **Üç durum ve geçiş koşulları** (`failureRateThreshold`, `slowCallRateThreshold`, `waitDurationInOpenState`, `permittedNumberOfCallsInHalfOpenState`) ezbere değil sebebiyle anlatılmalı. Örnek gerekçe: Half-Open, yarı toparlanmış bir servisi tam trafikle ezmemek için kontrollü yoklama yapar.
- **Jitter neden şart?** Üstel backoff denemeleri zamana yayar ama istemcileri birbiriyle senkron bırakır. Rastgeleliği tek başına jitter getirir.
- **Yeniden deneme hangi işlemde açılır?** Yalnız idempotent olanda. `POST` ancak idempotency anahtarıyla güvenli hale gelir.
- **Semaphore mi, ThreadPool bulkhead mı?** Kısa çağrılarda sayaç yeter ve bağlam korunur. Yavaş bağımlılıkta havuz izolasyonu seçilir.
- **Sıra neden Retry en dışta?** Devre açıkken her denemenin ucuza reddedilebilmesi için.

### Tek cümlede

Devre kesici arızalı bağımlılığa gitmeyi durdurur. Jitter'lı ve sayısı sınırlı yeniden deneme, geçici hataları toparlar — ama yalnız işlem idempotentse. Zaman aşımı ile bölme duvarı, beklemenin ve eşzamanlılığın faturasını sınırlar. Bunların hepsi ancak doğru sırayla sarıldığında birlikte çalışır.

# r4j-1 | İleri
## Soru
Bir Resilience4j devre kesici (circuit breaker) `Open` durumuna geçti ve `waitDurationInOpenState` süresi doldu. Bundan sonra ne olur?
## A
Kesici doğrudan `Closed`'a döner; sayaçlarını sıfırlar ve tüm trafik anında downstream'e akmaya başlar
🤖 aradaki Half-Open atlanmaz; kesici önce sınırlı denemeyle downstream'in toparlandığını doğrular.
## B
Süre dolsa da otomatik geçiş olmaz; kesici bir operatör `reset()` çağırana kadar `Open` durumunda bekler
🤖 `waitDurationInOpenState` zamanlıdır, süresi dolunca Half-Open'a geçilir; manuel müdahale gerekmez.
## C
`Half-Open`'a geçer; sınırlı deneme geçer, hata oranı eşiğin altındaysa `Closed`'a, değilse yeniden `Open`'a
🤖 Doğru: Half-Open, downstream'i kontrollü yoklayan ara durumdur; denemelerin sonucu geçişi belirler.
## D
`Open` süresince biriken bekleyen çağrıların tümü tek seferde kuyruktan boşaltılıp aynı anda downstream'e gönderilir
🤖 böyle bir toplu boşaltma yoktur; olsaydı toparlanmakta olan servisi anında yeniden devirirdi.
## Doğru
C
## Açıklama
Devre kesici bir **durum makinesidir** (state machine). `Open` durumunda çağrılar `CallNotPermittedException` ile hızlıca reddedilir (downstream'e nefes aldırılır). `waitDurationInOpenState` dolunca kesici kendini **`Half-Open`**'a alır: yalnız `permittedNumberOfCallsInHalfOpenState` kadar deneme çağrısı geçirir. Bu denemelerin sonucu (hata oranı ve yavaş-çağrı oranı) eşiğin altındaysa `Closed`'a döner; değilse yeniden `Open`. Half-Open, "servis düzeldi mi?" sorusunu **kontrollü** yoklar ve yarı toparlanmış bir downstream'i tam trafikle yeniden ezmeyi engeller; bu yüzden ne doğrudan tam açılma ne de biriken çağrıların toplu boşaltılması söz konusudur. Ayrıntı: varsayılan yapılandırmada geçiş tembeldir — süre dolduktan sonra gelen ilk çağrı kesiciyi Half-Open'a alır; `automaticTransitionFromOpenToHalfOpenEnabled` açılırsa geçiş çağrı beklemeden zamanlayıcıyla yapılır. Her iki durumda da manuel `reset()` gerekmez.

# r4j-2 | Orta
## Soru
2026'da yeni bir mikroservise devre kesici (circuit breaker) ekliyorsun. Kütüphane seçimi hakkında hangisi doğrudur?
## A
Hystrix 2018'de bakım moduna alındı; Resilience4j fiili standarttır ve Spring Cloud Circuit Breaker onu sarmalar
🤖 Doğru: yeni projede Resilience4j seçilir; Hystrix aktif olarak geliştirilmiyor.
## B
Hystrix hâlâ önerilen çözümdür; Resilience4j onun deneysel bir çatalıdır (fork) ve üretimde kullanılmaz
🤖 bakım modundaki taraf Hystrix'tir; Resilience4j deneysel değil, üretimde yaygın kullanılan standarttır.
## C
Devre kesiciye gerek kalmadı; Kubernetes liveness probe'u sağlıksız pod'u yeniden başlatarak çağrı düzeyinde aynı korumayı sağlar
🤖 probe süreç sağlığıyla ilgilenir; çağrı bazında hızlı-red ve kayan pencereli durum makinesi sunmaz.
## D
Spring Boot çekirdeği yerleşik bir devre kesici sunar; harici kütüphaneye veya ek bağımlılığa gerek yoktur
🤖 çekirdek devre kesici içermez; Spring Cloud Circuit Breaker soyutlaması altta bir sağlayıcıya muhtaçtır.
## Doğru
A
## Açıklama
Netflix **Hystrix** 2018'de resmen bakım moduna alındı ve yeni özellik almıyor; bu yüzden yeni projelerde tavsiye edilmez. **Resilience4j**, Java 8+ fonksiyonel arayüzleriyle çalışan hafif bir kütüphanedir ve Spring Cloud Circuit Breaker soyutlamasının varsayılan sağlayıcısıdır. Kubernetes probe'ları süreç/pod düzeyinde sağlık yönetir — çağrı düzeyinde hızlı-red, kayan pencere ve durum makinesi sağlamaz; tamamlayıcı ama farklı bir katmandır. Spring çekirdeği ise devre kesici içermez; soyutlamayı çalıştıran şey altındaki sağlayıcıdır.

# r4j-3 | İleri
## Soru
Bir istemcide otomatik yeniden deneme (retry) yapılandırıyorsun. Retry'ı hangi işlemlerde güvenle uygulayabilirsin?
## A
Her işlemde güvenle; retry'ın doğruluğu, çağrının idempotent olup olmamasıyla ilgili değildir
🤖 non-idempotent bir çağrının tekrarı yan etkiyi ikiye katlar; güvenlik doğrudan idempotentliğe bağlıdır.
## B
Yalnız `GET` isteklerinde; `PUT` ve `DELETE` dahil diğer bütün metotlarda retry yasaktır
🤖 Yanlış, fazla dar: `PUT` ve `DELETE` de idempotenttir, tekrarları aynı sonucu verdiği için retry edilebilir.
## C
Non-idempotent `POST` çağrılarında da serbestçe retry yapılabilir; sunucu yinelenen istekleri kendiliğinden ayıklar
🤖 böyle bir otomatik ayıklama yoktur; idempotency anahtarı olmadan `POST` retry'ı çift ödeme üretir.
## D
Yalnız idempotent işlemlerde — `GET`, `PUT`, `DELETE` gibi; `POST`'ta retry çift yan etki riski taşır
🤖 Doğru: idempotentlik retry güvenliğinin ön koşuludur; `POST` ancak idempotency anahtarıyla güvenli olur.
## Doğru
D
## Açıklama
Yeniden deneme, ilk çağrının **etkisiz kalmış** olabileceği (timeout ama sunucu belki işledi) belirsizliğinde çalışır. Eğer işlem **idempotent** ise — aynı isteği N kez göndermek 1 kez göndermekle aynı sonucu verir (`GET`, `PUT`, `DELETE`) — retry güvenlidir. **Non-idempotent** bir `POST` (ör. "ödeme al") timeout sonrası retry edilirse iki ödeme oluşabilir; çünkü ilk isteğin sunucuda işlenip işlenmediği bilinmez. `POST`'u güvenli kılmak için istemci bir **idempotency anahtarı** (idempotency key) üretip başlıkta gönderir; sunucu aynı anahtarı ikinci kez görünce işlemi tekrarlamaz. Kuralı yalnız `GET`'e daraltmak da yanlıştır: `PUT` ve `DELETE` de idempotenttir ve retry edilebilir.

# r4j-4 | İleri
## Soru
Retry'da üstel geri çekilme (exponential backoff) kullanıyorsun ama binlerce istemci aynı downstream arızasından etkilendi. Sabit üstel backoff neden hâlâ yetersiz kalır ve çözüm nedir?
## A
Üstel backoff tek başına yeterlidir; istemci sayısı artsa da denemeler kendiliğinden zamana yayıldığından ek önlem gerekmez
🤖 aynı anda arızalanan istemciler aynı backoff eğrisini izler, bu yüzden denemeleri yayılmaz, üst üste biner.
## B
İstemciler geri sayımı aynı anda bitirip birlikte dener (thundering herd); çözüm denemelere jitter eklemektir
🤖 Doğru: rastgele sapma denemeleri zaman ekseninde yayarak senkron tepe yükünü düzler.
## C
Backoff çarpanını 1.0'a indirip sabit aralıklı retry'a dönmek senkron dalgayı ortadan kaldırır
🤖 Yanlış, tersi olur: sabit aralık senkronizasyonu güçlendirir; eksik olan şey rastgeleliktir, düzenlilik değil.
## D
Retry üst sınırını kaldırıp sınırsız denemeye geçmek; downstream er geç toparlandığında istekler tamamlanır
🤖 sınırsız retry arızalı servisi sürekli döver, toparlanmasını geciktirir ve istemci kaynağını tüketir.
## Doğru
B
## Açıklama
Üstel backoff tek bir istemci için gecikmeyi artırır, ama **eşzamanlı** arızalanan binlerce istemci **aynı** backoff eğrisini izlediğinden hepsi 200ms, sonra 400ms, sonra 800ms'de **birlikte** yeniden dener — bu senkron tepe (thundering herd) yeni toparlanan downstream'i tekrar devirir. Çözüm **jitter**: her denemeye rastgele bir sapma ekleyip denemeleri zaman eksenine yaymak (Resilience4j'de `ofExponentialRandomBackoff`). Ayrıca retry mutlaka **sınırlı** olmalıdır: sınırsız deneme arızalı servisin toparlanmasını engeller. Sabit aralığa dönmek ise senkronizasyonu azaltmaz, artırır.

# r4j-5 | İleri
## Soru
Resilience4j'de `SemaphoreBulkhead` ile `ThreadPoolBulkhead` arasındaki temel fark nedir?
## A
İkisi de çağrıyı ayrı bir thread havuzunda çalıştırır; aralarındaki tek fark isimlendirme ve yapılandırma anahtarlarının yazımıdır
🤖 Semaphore ayrı havuz açmaz, çağrıyı çağıran thread'de yürütür; iki mekanizma temelden farklıdır.
## B
`SemaphoreBulkhead` ayrı havuz ve kuyruk açar, `ThreadPoolBulkhead` yalnız sayaç tutar; isimleri işlevlerine terstir
🤖 ayrı havuz ve kuyruğu olan ThreadPoolBulkhead'dir, sayaç tutan Semaphore'dur; roller karıştırılmış.
## C
`SemaphoreBulkhead` sayaçla eşzamanlı çağrıyı sınırlar, çağıran thread'de çalışır; `ThreadPoolBulkhead` ayrı havuzla izole eder
🤖 Doğru: biri hafif sayaçtır ve bağlamı korur, diğeri havuz izolasyonu sağlar ama bağlamı taşımaz.
## D
`ThreadPoolBulkhead` senkron kodda kullanılamaz; yalnız reaktif akışlarda (Reactor/RxJava) çalışabilen bir yapıdır
🤖 `ThreadPoolBulkhead` senkron kodda `CompletableFuture` döndürerek de kullanılır, reaktifle sınırlı değildir.
## Doğru
C
## Açıklama
**Bulkhead**, bir bağımlılığın tüm eşzamanlılık kaynağını tüketmesini önler. **`SemaphoreBulkhead`** basit bir sayaçtır: eşzamanlı çağrı sayısı sınırı aşarsa yeni çağrı reddedilir/bekletilir, ama çağrı **çağıran thread'de** çalışır — hafiftir ve `ThreadLocal`/`SecurityContext` bağlamı korunur. **`ThreadPoolBulkhead`** çağrıyı **ayrı bir thread havuzuna** ve kuyruğa taşır: downstream tamamen izole edilir (yavaş bağımlılık çağıran thread'i bloklamaz), fakat context-switch maliyeti vardır ve `ThreadLocal`'a bağlı bağlam (güvenlik, MDC) otomatik taşınmaz. Senior tercih: kısa/hızlı çağrılar için Semaphore, izolasyonu kritik yavaş çağrılar için ThreadPool.

# r4j-6 | Orta
## Soru
Bir istemci hem `Retry` hem `CircuitBreaker` ile sarılıyor. Resilience4j'de önerilen dekoratör sırası (dıştan içe) nedir ve neden?
## A
`Retry( CircuitBreaker( çağrı ) )` — Retry en dışta; her yeniden deneme devre kesiciden yeniden geçer
🤖 Doğru: bu sırada devre açıkken denemeler hızlıca reddedilir, downstream ve istemci kaynağı boşa harcanmaz.
## B
`CircuitBreaker( Retry( çağrı ) )` — CB en dışta; retry döngüsünün tamamı ona tek bir çağrı gibi görünür
🤖 ara denemeler kesiciden geçmediği için hızlı-red koruması kaybolur; önerilen varsayılan sıra bu değildir.
## C
Sıra fark etmez; Resilience4j dekoratörleri değişmeli (commutative) olduğundan sonuç her hâlde aynı olur
🤖 dekoratörler iç içe sarılan fonksiyonlardır, sıra davranışı belirgin biçimde değiştirir.
## D
İkisi hiç birleştirilmemelidir; Retry ve CircuitBreaker ayrı ayrı, birbirinden habersiz uygulanmalıdır
🤖 dekoratörler bilinçli olarak birleştirilir; asıl mesele hangi sırayla sarıldıklarıdır.
## Doğru
A
## Açıklama
Resilience4j dekoratörleri **iç içe sarılan fonksiyonlardır**, bu yüzden sıra davranışı belirler; değişmeli (commutative) değildirler ve birlikte kullanılmak üzere tasarlanmışlardır. Önerilen varsayılan sıra dıştan içe: `Retry( CircuitBreaker( RateLimiter( TimeLimiter( Bulkhead( çağrı )))))`. **Retry en dışta** olduğunda her yeniden deneme devre kesiciden **yeniden** geçer; devre `Open` ise deneme `CallNotPermittedException` ile hızlıca reddedilir ve retry arızalı downstream'i gereksiz yere dövmez. Ters sırada (`CircuitBreaker( Retry( ... ) )`) devre kesici tüm retry döngüsünü tek bir çağrı gibi görür, ara denemeler hızlı-red korumasından yararlanamaz. Spring Boot'ta bu sıra `resilience4j.*.aspect-order` property'leriyle ayarlanır.
