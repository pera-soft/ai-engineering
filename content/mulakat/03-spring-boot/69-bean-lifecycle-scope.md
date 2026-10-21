---
slug: bean-lifecycle-scope
baslik: "Bean Yaşam Döngüsü ve Kapsam: Singleton/Prototype, init/destroy ve Enjeksiyon Tuzağı"
kademe: Orta
sira: 69
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir otel düşün. Lobideki piyano tektir, bütün misafirler aynı piyanoyu kullanır. Havlu ise her misafire yeni verilir; kullanıldıktan sonra onu takip etmek otelin işi değildir. "Bu şeyden kaç tane olacak ve ne kadar yaşayacak?" sorusunun cevabına Spring'de **kapsam** (scope) denir. Bir de her nesnenin bir doğum ve bir ölüm anı vardır: bağlantıyı doğumda açar, ölümde kapatırsın. Bu iki konu birleşince mülakatların en verimli tuzak alanı çıkar: "her seferinde yeni sanıyordum, hep aynısı geldi."

### Temel kavramlar

- **Konteyner (container)**: nesneleri kuran, bağlayan, saklayan ve kapanışta yıkan Spring çekirdeği (`ApplicationContext`).
- **Bean**: container'ın yönettiği nesne.
- **Kapsam (scope)**: container'ın o bean'den **kaç örnek** tuttuğunu ve örneğin **ne kadar yaşadığını** belirleyen kural.
- **Yaşam döngüsü (lifecycle)**: bean'in kuruluşundan yıkımına kadarki sıralı adımlar.
- **Geri çağrı / kanca (callback)**: "şu an gelince şu metodumu çağır" diye çerçeveye bıraktığın not; metodu sen yazarsın, çağırmayı container yapar.
- **İş parçacığı (thread)**: aynı anda ilerleyen iş yolu. Aynı nesneyi iki thread paylaşıyorsa içindeki değişken durum bozulabilir; buna karşı güvenli olmaya **thread-safe** denir.
- **Vekil (proxy)**: gerçek nesnenin önüne konan, dışarıdan aynı tip görünen ama çağrıyı yakalayıp yönlendiren sarmalayıcı nesne.

Kapsamlar örnek sayısını ve yaşam süresini belirler:

```svg
<svg viewBox="0 0 640 224" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="singleton prototype request session kapsam karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapsam: kaç örnek, ne kadar yaşar?</text>
  <rect x="20" y="44" width="196" height="164" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="118" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">singleton</text>
  <text x="118" y="86" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">varsayılan</text>
  <text x="118" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">container başına 1</text>
  <text x="118" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">herkes paylaşır</text>
  <text x="118" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">durumsuz olmalı</text>
  <text x="118" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tam yaşam döngüsü</text>
  <text x="118" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">init + destroy</text>
  <rect x="222" y="44" width="196" height="164" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">prototype</text>
  <text x="320" y="86" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">her istekte yeni</text>
  <text x="320" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her çözümde 1</text>
  <text x="320" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">init çağrılır</text>
  <text x="320" y="156" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">destroy ÇAĞRILMAZ</text>
  <text x="320" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">container izlemez</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">temizlik sana ait</text>
  <rect x="424" y="44" width="196" height="164" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="522" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">request / session</text>
  <text x="522" y="86" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">yalnız web</text>
  <text x="522" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek / oturum başına 1</text>
  <text x="522" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istekle doğar/ölür</text>
  <text x="522" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istek-özel durum tutar</text>
  <text x="522" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">web bağlamı gerekir</text>
  <text x="522" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">singleton'a proxy ile</text>
</svg>
```

- **singleton** — varsayılan. Container başına **tek** örnek. Klasik GoF singleton deseninden farkı: "JVM başına tek" değil, "**ApplicationContext başına tek**". Aynı tipi kaç yere enjekte edersen et hepsi **aynı** nesneyi paylaşır; bu yüzden singleton bean durumsuz (stateless) veya thread-safe olmalıdır.
- **prototype** — her istendiğinde yeni örnek.
- **request** — yalnız web; her HTTP isteğinde taze örnek, istek bitince yıkılır. İsteğe özgü durumu güvenle taşır: kullanıcı bilgisi, isteği uçtan uca izlemeye yarayan ilişkilendirme numarası (correlation id) gibi.
- **session** — yalnız web; oturum başına tek örnek, aynı kullanıcının tüm istekleri paylaşır.

Spring'in yerleşik bir "thread başına" varsayılan kapsamı yoktur.

### Nasıl çalışır?

Kurulum ve yıkım anlarına kanca takmanın modern yolu `@PostConstruct` ve `@PreDestroy` anotasyonlarıdır; `InitializingBean`/`DisposableBean` arayüzleriyle aynı işi görür ama sınıfını Spring tiplerine bağlamaz.

```java
@Component
class OnbellekIsitici {
    private final UrunRepo repo;
    OnbellekIsitici(UrunRepo repo) { this.repo = repo; }

    @PostConstruct          // bağımlılıklar enjekte edildikten sonra, hizmete girmeden önce, BİR kez
    void isit() { /* önbelleği doldur */ }

    @PreDestroy             // container kapanırken (graceful shutdown), BİR kez
    void kapat() { /* bağlantıları kapat, flush et */ }
}
```

Bu iki kancanın ne olmadığına dikkat et. Metot başına çalışan sarmalayıcılar değildir, asenkron değildir ve `@Transactional` gibi işlem sınırı açmazlar. Yaşam döngüsünde **birer kez** çalışan senkron kancalardır. Yapıcı çalışırken bağımlılıklar henüz enjekte edilmemiş olabilir; bu yüzden önbellek ısıtma, bağlantı açma gibi hazırlık işleri `@PostConstruct`'a konur.

Kuruluş sırası şudur: container bean'i **örnekler** (yapıcı) → bağımlılıkları **enjekte eder** → `BeanNameAware`/`ApplicationContextAware` gibi `*Aware` geri çağrılarını yapar → `BeanPostProcessor.postProcessBeforeInitialization` → `@PostConstruct` → `InitializingBean.afterPropertiesSet()` → `@Bean(initMethod = ...)` → `postProcessAfterInitialization`. Vekil nesneler (AOP proxy) **genelde tam bu son adımda** devreye girer. Sonra bean **kullanımdadır**.

Kapanış sırası ise kısadır: `@PreDestroy` → `DisposableBean.destroy()` → `destroyMethod`.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bean yaşam döngüsü adımları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir bean'in hayatı</text>
  <rect x="20" y="70" width="115" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="77" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 · Örnekle</text>
  <text x="77" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(yapıcı)</text>
  <rect x="150" y="70" width="115" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="207" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 · Enjekte</text>
  <text x="207" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bağımlılıklar</text>
  <rect x="280" y="70" width="120" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="340" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">3 · @PostConstruct</text>
  <text x="340" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">hazır/kullanımda</text>
  <rect x="415" y="70" width="120" height="66" rx="10" fill="#FBF4E6" stroke="#B98900" stroke-width="1.4"/>
  <text x="475" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#976f00" font-family="system-ui">4 · @PreDestroy</text>
  <text x="475" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">container kapanır</text>
  <text x="320" y="176" text-anchor="middle" font-size="11" fill="#B3453B" font-family="system-ui">prototype: adım 4 ÇAĞRILMAZ — temizlik sana ait</text>
  <text x="320" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">singleton varsayılan · her enjeksiyon aynı nesne</text>
</svg>
```

prototype'ta container bu döngünün **yarısını** yönetir: örnekler, bağımlılıkları enjekte eder, `@PostConstruct`'ı çağırır. Sonra örneği çağırana teslim edip **unutur**. Referans tutmadığı için hangi örneklerin yaşadığını bilmez. Bu yüzden `@PreDestroy`/`destroy` **hiçbir** prototype örneği için çağrılmaz; ilk yaratılan için bile. Temizlik (bağlantı kapatma vb.) tamamen kullanan kodun sorumluluğudur; gerekirse örneği `ObjectProvider` ile alıp elle `close()` çağırırsın.

request/session bean'ler **aktif bir web istek bağlamı** ister; bağlam yokken çözümleme başarısız olur. Ömrü daha uzun bir singleton'a enjekte edilirken araya **kapsam vekili** (scoped proxy) konur. Bu vekil nesne dışarıya normal bean gibi görünür, ama her çağrıda o anki isteğe ait gerçek örneği bulup çağrıyı ona iletir:

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class IstekBaglami { /* isteğe özgü durum */ }
```

Sık düşülen tuzak: singleton bir bean'e prototype bir bean enjekte etmek.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="singletona prototype enjekte tuzağı ve ObjectProvider çözümü">
  <defs>
    <marker id="bean-arrow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tuzak vs çözüm</text>
  <rect x="24" y="40" width="288" height="150" rx="12" fill="#F3EEF9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="168" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Alan enjeksiyonu (tuzak)</text>
  <text x="168" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">final Gorev gorev;</text>
  <text x="168" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">enjeksiyon anında 1 kez çözülür</text>
  <text x="168" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek örnek SABİTLENİR</text>
  <text x="168" y="152" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">her isle() → AYNI Gorev</text>
  <text x="168" y="174" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eager injection</text>
  <rect x="330" y="40" width="286" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="473" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ObjectProvider (çözüm)</text>
  <text x="473" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ObjectProvider&lt;Gorev&gt; p;</text>
  <text x="473" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">isle() { p.getObject() }</text>
  <text x="473" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanım anında lookup</text>
  <text x="473" y="152" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">her isle() → TAZE Gorev</text>
  <text x="473" y="174" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">lazy lookup · alt: @Lookup</text>
</svg>
```

`SingletonServis` container ömründe **bir kez** kurulur; yapıcısı bir kez çağrılır ve o an tek bir prototype örneği çözülüp alana **sabitlenir**. prototype "her **enjeksiyon** noktasında yeni" demektir, "her **kullanımda** yeni" değil. `@Scope` yok sayılmaz, alanın `final` olması sorun değildir ve Spring bean'in durum tutup tutmadığına bakıp kapsamı değiştirmez.

Çözüm, örneği enjeksiyon anında değil **kullanım anında** istemektir:

```java
@Service
class SingletonServis {
    private final ObjectProvider<Gorev> saglayici;
    SingletonServis(ObjectProvider<Gorev> saglayici) { this.saglayici = saglayici; }
    void isle() { saglayici.getObject().calistir(); }   // her çağrıda TAZE prototype
}
```

`jakarta.inject.Provider<Gorev>` aynı işi görür. Bir başka yol da `@Lookup` ile işaretlenmiş soyut metottur; Spring bu metodu çalışma anında CGLIB kütüphanesiyle üretilen bir alt sınıfta doldurur ve her çağrıda taze bean döndürür.

Yanlış çözümler de var. Servisi prototype yapmak yanlış katmanı değiştirir. Bean'i `@Component` yerine `@Bean` metoduyla tanımlamak ya da `@Autowired` yerine `@Resource` kullanmak da kapsam davranışına dokunmaz. Kilit fikir tek cümlede: bean'i **kullanım anında iste** (lazy lookup — geç arama), enjeksiyon anında peşinen alıp saklama (eager injection).

### Mülakatta ne sorulur?

- **Varsayılan kapsam** → singleton; "container başına tek", JVM başına değil. Paylaşıldığı için durumsuz olmalı.
- **Yaşam döngüsünün tam sırası** → yapıcı → enjeksiyon → `*Aware` → BPP-before → `@PostConstruct` → `afterPropertiesSet()` → `initMethod` → BPP-after (AOP proxy burada) → kullanım → `@PreDestroy` → `destroy()` → `destroyMethod`.
- **prototype'ta destroy** → **çağrılmaz**, hiçbiri için; container prototype'ı izlemez.
- **Singleton'a prototype enjeksiyonu** → tek kez çözülür ve sabitlenir; çözüm `ObjectProvider`/`Provider`/`@Lookup`.
- **request bean'ini singleton'a enjekte etmek** → `proxyMode = ScopedProxyMode.TARGET_CLASS` ile kapsam proxy'si.
- **`@PostConstruct` neden yapıcı yerine?** → yapıcı çalışırken enjeksiyon henüz tamamlanmamış olabilir.

### Tek cümlede

Kapsam bean'den kaç örnek olacağını, yaşam döngüsü kancaları o örneğe ne zaman dokunabileceğini söyler; prototype'ta container yalnız doğuma karışır, ölüme karışmaz.

# bean-1 | Temel
## Soru
Bir Spring bean'i için varsayılan kapsam (scope) ve bunun anlamı nedir?
## A
prototype — her `@Autowired` noktasına ve her kullanıma ayrı bir örnek enjekte edilir
🤖 Varsayılan prototype değildir; bu davranış ancak scope'u prototype yaparsan olur.
## B
request — her HTTP isteği için ayrı bir örnek oluşturulur; web dışı uygulamada da aynı şekilde geçerlidir
🤖 request web'e özgü ve varsayılan değildir; ayrıca web dışı uygulamada anlamsızdır.
## C
singleton — container başına tek örnek; tüm enjeksiyon noktaları aynı nesneyi paylaşır
🤖 Doğru: Spring bean'i varsayılan olarak container-başına-tek örnektir.
## D
thread — her iş parçacığı (thread) için ayrı örnek tutulur, böylece durum paylaşımı sorunu doğmaz
🤖 Spring'in yerleşik varsayılanı thread-scope değildir; böyle bir standart kapsam yok.
## Doğru
C
## Açıklama
Spring'de varsayılan kapsam **singleton**'dır ama bu klasik GoF singleton deseninden farklıdır: "JVM başına tek" değil, **container (ApplicationContext) başına tek** demektir. Aynı tipi kaç yere enjekte edersen et, hepsi **aynı** örneği paylaşır. Bunun pratik sonucu: singleton bean **durumsuz** (stateless) veya thread-safe olmalıdır, çünkü tüm istekler onu paylaşır. prototype (A), request (B) ve thread-scope (D) varsayılan değildir; bunlar bilinçli olarak seçilir.

# bean-2 | Temel
## Soru
`@PostConstruct` ve `@PreDestroy` anotasyonları ne işe yarar?
## A
`@PostConstruct` enjeksiyondan sonra çalışan init, `@PreDestroy` yıkımdan önce çalışan temizlik kancasıdır
🤖 Doğru: biri kurulum-sonrası init, diğeri yıkım-öncesi temizlik.
## B
`@PostConstruct` bean'in her metot çağrısından sonra, `@PreDestroy` her çağrısından önce çalışır; bir tür otomatik sarmalayıcıdır
🤖 Bunlar metot-başına değil, bean yaşam döngüsünde BİR kez çalışan kancalardır.
## C
Yapıcının içindeki kodu asenkron olarak arka planda çalıştırır; böylece başlangıç bloke olmaz
🤖 Asenkronlukla ilgisi yok; senkron yaşam döngüsü kancalarıdır.
## D
Bean'in `@Transactional` işlem sınırını açar ve kapatır; commit/rollback bu kancalarda olur
🤖 İşlem (transaction) sınırıyla ilgili değildir; bunlar init/destroy kancasıdır.
## Doğru
A
## Açıklama
`@PostConstruct` işaretli metot, container **yapıcıyı çağırıp tüm bağımlılıkları enjekte ettikten sonra**, bean hizmete girmeden hemen önce **bir kez** çalışır — yapıcıda henüz enjekte edilmemiş alanlara dokunamayacağın init işleri (önbellek ısıtma, bağlantı açma) buraya konur. `@PreDestroy` ise container kapanırken (graceful shutdown) bean yıkılmadan önce **bir kez** çalışır — kaynak kapatma, flush gibi temizlikler için. Metot-başına (B), asenkron (C) veya transaction (D) semantiği yoktur; bunlar tek seferlik yaşam döngüsü kancalarıdır.

# bean-3 | Orta
## Soru
Kapsamı `prototype` olan bir bean'e `@PreDestroy` metodu koydun. Container kapanırken ne olur?
## A
Container yarattığı tüm prototype örneklerini kayıt altında tutar ve kapanışta her biri için `@PreDestroy`'u çağırır
🤖 Container prototype örneklerini izlemez; hepsi için çağırma yapamaz.
## B
`@PreDestroy` prototype kapsamlı bir bean'de derleme hatası verir; yalnız singleton'da yazılabilir
🤖 Anotasyon derlenir; sorun derleme değil, çalışma zamanı davranışıdır.
## C
Yalnız ilk yaratılan prototype örneği için çağrılır; container yalnız o örneğin referansını tutar
🤖 İlki için de çağrılmaz; container hiçbir prototype örneğini yıkım için izlemez.
## D
Hiçbir prototype örneği için çağrılmaz; temizlik kullanan kodun sorumluluğudur
🤖 Doğru: container prototype'ı kurup teslim eder, sonrasını izlemez.
## Doğru
D
## Açıklama
prototype bean'de container **yarım** bir yaşam döngüsü yönetir: örnekler, bağımlılıkları enjekte eder ve `@PostConstruct`'ı çağırır — sonra örneği **çağırana teslim edip unutur**. Container prototype örneklerine referans tutmadığı için hangi örneklerin yaşadığını bilmez; dolayısıyla `@PreDestroy`/`destroy` callback'lerini **hiçbiri** için çağıramaz. Temizlik (bağlantı kapatma vb.) tamamen kullanan kodun sorumluluğudur — gerekirse `ObjectProvider` ile alıp elle `close()` çağırırsın veya `DisposableBeanAdapter` benzeri bir desen kurarsın. Bu, prototype scope'un en sık gözden kaçan gerçeğidir.

# bean-4 | Orta
## Soru
Web uygulamasında `@Scope("request")` bir bean'in davranışı için doğru ifade hangisidir?
## A
Uygulama başına tek örnek yaratılır; tüm istekler aynı nesneyi ve içindeki durumu paylaşır
🤖 Bu singleton davranışıdır; request scope her istekte ayrı örnek demektir.
## B
Her HTTP isteği için ayrı örnek yaratılır ve istek bitince yıkılır
🤖 Doğru: yaşam süresi tek bir HTTP isteğiyle sınırlıdır.
## C
Her oturum (session) için tek örnek yaratılır; aynı kullanıcının tüm istekleri onu paylaşır
🤖 Bu `session` scope'un tarifi; `request` istek-başına örnektir.
## D
İstek kapsamlı bean'ler, aktif web bağlamı olmayan testlerde de sorunsuz biçimde singleton gibi çalışır
🤖 request scope aktif bir web isteği bağlamı ister; onsuz enjeksiyon hata verir.
## Doğru
B
## Açıklama
`request` scope, bean'in yaşamını **tek bir HTTP isteğine** bağlar: istek geldiğinde örnek kurulur, istek bitince yıkılır; her yeni istek **taze** bir örnek görür. Bu yüzden istek-özel durumu (o isteğe ait kullanıcı, correlation id vb.) güvenle tutabilir. `singleton` (A) uygulama-başına tek, `session` (C) oturum-başına tektir — karıştırma. request/session bean'ler aktif bir web istek bağlamı gerektirdiği için, onları düz singleton'a enjekte ederken proxy (`proxyMode`) kullanmak gerekir ve web bağlamı yoksa (D) çözümleme başarısız olur.

# bean-5 | İleri
## Soru
Aşağıdaki kodda `singletonServis.isle()` her çağrıldığında **yeni** bir `Gorev` (prototype) kullanmayı bekliyorsun ama hep aynısını görüyorsun. Sebep nedir?

```java
@Component @Scope("prototype")
class Gorev { /* durum tutar */ }

@Service
class SingletonServis {
    private final Gorev gorev;         // prototype enjekte edildi
    SingletonServis(Gorev gorev) { this.gorev = gorev; }
    void isle() { gorev.calistir(); }
}
```
## A
Yapıcı parametresi olarak istendiğinde prototype bean'ler otomatik singleton'a dönüştürülür; `@Scope` yok sayılır
🤖 `@Scope` yok sayılmaz; sorun scope'un yanlış anlaşılmasında değil, enjeksiyon anında sabitlenmesinde.
## B
`gorev` alanı `final` olduğu için Spring her çağrıda yeniden atayamaz; bu yüzden derleme hatası olur
🤖 Derleme hatası yok; `final` beklenen davranış, sorun runtime'da tek örneğin sabitlenmesidir.
## C
`Gorev` durum tuttuğu için Spring onu güvenlik gereği tek örneğe indirger; durumsuz yapılırsa her çağrıda yenisi gelir
🤖 Spring bean'in durum tutup tutmadığına bakmaz; örnek sayısını kapsam ve enjeksiyon anı belirler.
## D
Singleton bir kez kurulduğu için prototype bağımlılığı da yalnız bir kez çözülür ve sabitlenir
🤖 Doğru: singleton'ın kurulumu tek olduğundan prototype de tek kez enjekte edilir.
## Doğru
D
## Açıklama
Bu, "**singleton'a prototype enjekte etme tuzağı**"dır. `SingletonServis` container ömründe **bir kez** kurulur; yapıcısı bir kez çağrılır ve o an bir tek `Gorev` örneği çözülüp `gorev` alanına **sabitlenir**. prototype scope "her *enjeksiyon* noktasında yeni" demektir, "her *kullanımda* yeni" değil — singleton'a enjekte edilince tek enjeksiyon olur, dolayısıyla tek örnek. `@Scope` yok sayılmaz (A), Spring bean'in durum tutup tutmadığına bakıp kapsamı değiştirmez (C), `final` de sorun değil (B). Çözüm bir sonraki soruda: kullanım anında taze örnek istemek.

# bean-6 | İleri
## Soru
Bir önceki tuzağı (singleton içinden **her çağrıda taze** prototype örneği elde etme) çözmenin idiomatik yolu hangisidir?
## A
`SingletonServis`'i de `@Scope("prototype")` yaparak servisin her çağrıda yeniden kurulmasını sağlamak
🤖 Servisi prototype yapmak farklı sorunlar açar ve her çağrıda taze görev vermez; yanlış katmanı değiştirir.
## B
`Gorev`'i `@Component` yerine `@Configuration` içindeki bir `@Bean` metoduyla tanımlamak
🤖 Tanım biçimi (`@Component` / `@Bean`) kapsam semantiğini değiştirmez; bağımlılık yine tek kez çözülür.
## C
`ObjectProvider<Gorev>` enjekte edip her çağrıda `getObject()` ile taze örnek istemek
🤖 Doğru: kullanım anında container'dan yeni prototype istenir.
## D
`@Autowired` yerine `@Resource` kullanarak enjeksiyonu ada göre çözdürmek
🤖 Anotasyonu değiştirmek scope semantiğini değiştirmez; hâlâ tek kez çözülür.
## Doğru
C
## Açıklama
Doğru çözüm, prototype örneğini **enjeksiyon anında** değil **kullanım anında** istemektir. `ObjectProvider<Gorev>` (Spring) veya `jakarta.inject.Provider<Gorev>` enjekte edilir; `isle()` her çalıştığında `provider.getObject()` çağrısı container'dan **taze** bir prototype `Gorev` ister — böylece "her çağrıda yeni" beklentisi karşılanır. Alternatif `@Lookup` işaretli soyut metottur (Spring, CGLIB ile override eder). Servisi prototype yapmak (A) yanlış katmanı değiştirir, bean'i `@Bean` metoduyla tanımlamak (B) kapsam semantiğine dokunmaz, farklı anotasyon (D) scope davranışını değiştirmez. Kilit fikir: **lazy lookup**, eager injection değil.
