---
slug: spring-ioc-di
baslik: "Spring IoC ve Bağımlılık Enjeksiyonu: Container, DI Türleri ve ApplicationContext"
kademe: Orta
sira: 68
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoran mutfağı düşün. Aşçı yemeğe başlamadan önce pazara gidip et, sebze, tencere aramaz; işe geldiğinde malzemeler tezgâhta hazırdır. Aşçının işi yemek pişirmektir, tedarik değil. Spring de yazılımda bu "tedarikçi"dir: bir sınıfın çalışmak için ihtiyaç duyduğu diğer nesneleri sınıf yerine kendisi kurar ve önüne koyar. Çözdüğü problem şudur: her sınıf kendi yardımcılarını kendi yaratırsa kod birbirine yapışır — bir parçayı değiştirmek için ötekine dokunmak, test etmek için gerçek veritabanını ayağa kaldırmak gerekir.

### Temel kavramlar

- **Çerçeve (framework)**: senin kodunu çağıran hazır iskelet. Kütüphaneyi sen çağırırsın; çerçeve seni çağırır.
- **Nesne (object)**: sınıf tariftir, nesne o tariften üretilmiş çalışan örnektir (`new Kalem()`).
- **Bağımlılık (dependency)**: bir sınıfın işini yapabilmek için ihtiyaç duyduğu başka nesne. `SiparisServisi` veriyi yazmak için `StokRepo`ya ihtiyaç duyuyorsa, `StokRepo` onun bağımlılığıdır.
- **Anotasyon (annotation)**: `@` ile başlayan etiket (`@Service`, `@Autowired`). Kendisi iş yapmaz; çerçeveye "bu sınıfı/alanı şöyle işle" diye not bırakır.
- **Spring kabı / konteyner (container)**: nesneleri kuran, bağlayan, saklayan ve kapanışta yıkan yönetici havuz. Spring'in çekirdeği budur. Bundan sonra kısaca "kap" ya da "container" diyeceğiz.
- **Spring'in yönettiği nesne (bean)**: kabın kurduğu, bağımlılıklarını doldurduğu ve ömrünü yönettiği nesne. Elle `new` ile yarattığın nesne bean değildir.
- **IoC (Inversion of Control — kontrolün tersine çevrilmesi)**: "nesneyi kim kuracak, ne zaman ve hangi bağımlılıklarla?" kararının koddan alınıp container'a devredilmesi. Buradaki "kontrol" iş mantığının çalışma sırası ya da senkron/asenkronluk **değildir**; nesne kurma ve bağlama **sorumluluğudur**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Geleneksel akış vs IoC container">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kontrol kimde?</text>
  <rect x="28" y="46" width="272" height="140" rx="12" fill="#FBECEC" stroke="#B3453B" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#B3453B" font-family="system-ui">Geleneksel</text>
  <text x="164" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">repo = new JdbcRepo(...)</text>
  <text x="164" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sınıf bağımlılığını KENDİ yaratır</text>
  <text x="164" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">somut tipe bağımlı, test zor</text>
  <text x="164" y="166" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">kontrol koddadır</text>
  <rect x="340" y="46" width="272" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">IoC container</text>
  <text x="476" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Service(Repo r) { ... }</text>
  <text x="476" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">container Repo'yu ENJEKTE eder</text>
  <text x="476" y="144" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">arayüze bağımlı, test kolay</text>
  <text x="476" y="166" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">kontrol container'dadır</text>
</svg>
```

- **DI (Dependency Injection — bağımlılık enjeksiyonu)**: IoC'yi hayata geçiren teknik; container bean'in bağımlılıklarını dışarıdan verir (enjekte eder).
- **Gevşek bağlılık (loose coupling)**: sınıf somut sınıfa değil **arayüze** (interface) bağlanır. Üretimde gerçek, testte sahte (mock) implementasyon verilebilir; DI'nin ana kazancı budur.

DI'nin ne **olmadığını** da baştan söyleyelim. DI bir hız optimizasyonu değildir ve `import` satırlarını yok etmez. Bean'leri sihirle iş parçacığı güvenli (thread-safe — birden çok iş parçacığı aynı anda kullanınca bozulmayan) hâle de getirmez. Kapta bir tipten tek örnek tutulur (singleton — tek örnek); o tek örnekte paylaşılan değişken durum hâlâ senin sorumluluğundadır.

### Nasıl çalışır?

Container bağımlılığı üç yoldan verebilir.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Constructor vs setter vs field injection karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç enjeksiyon yolu</text>
  <rect x="20" y="44" width="196" height="176" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="118" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Constructor</text>
  <text x="118" y="86" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">2026 tercihi</text>
  <text x="118" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis(Repo r)</text>
  <text x="118" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">final alan olur</text>
  <text x="118" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eksiksiz kurulur</text>
  <text x="118" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">zorunlu bağımlılık</text>
  <text x="118" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">container'sız test</text>
  <rect x="222" y="44" width="196" height="176" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Setter</text>
  <text x="320" y="86" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">opsiyonel için</text>
  <text x="320" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">setRepo(Repo r)</text>
  <text x="320" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">final OLAMAZ</text>
  <text x="320" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonradan değişir</text>
  <text x="320" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değiştirilebilir bağ</text>
  <text x="320" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yarı-kurulum riski</text>
  <rect x="424" y="44" width="196" height="176" rx="12" fill="#F3EEF9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="522" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Field</text>
  <text x="522" y="86" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">kaçınılır</text>
  <text x="522" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">@Autowired alan</text>
  <text x="522" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">final OLAMAZ</text>
  <text x="522" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kısa görünür</text>
  <text x="522" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">test için container</text>
  <text x="522" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gizli bağımlılık</text>
</svg>
```

**Constructor injection** (yapıcı üzerinden), 2026'nın tercihi:

```java
@Service
class SiparisServisi {
    private final StokRepo stokRepo;             // final olabilir
    SiparisServisi(StokRepo stokRepo) {          // @Autowired YOK
        this.stokRepo = stokRepo;
    }
}
```

Üç somut üstünlüğü var. (1) Alan `final` olabilir; bu da değişmezlik (immutability) ve güvenli yayınlama demektir. (2) Zorunlu bağımlılık yapıcıda verilir, bu yüzden nesne asla **yarı-kurulmuş** var olamaz; eksik bağımlılık hatası (`NullPointerException`) çalışma anına değil kurulum anına çekilir. (3) `new SiparisServisi(sahteRepo)` yazarak nesneyi **kap olmadan** kurabilirsin; birim testi kolaylaşır.

Spring 4.3'ten beri şu kolaylık var: sınıfın **tek** yapıcısı varsa `@Autowired` yazmak gereksizdir. Kap, tek adayı kendiliğinden enjeksiyon noktası kabul eder. Birden çok yapıcı varsa hangisinin kullanılacağını `@Autowired` ile işaretlemelisin. Parametresiz (no-arg — hiç parametre almayan) yapıcı hiç gerekmez; `final` alan sorun değil, tam tersine amaçtır.

**Setter injection** (ayarlayıcı metot üzerinden): opsiyonel ya da sonradan değişebilen bağımlılıklar için. Alan `final` olamaz, yarı-kurulum riski vardır.

**Field injection** (doğrudan alana enjeksiyon — `@Autowired private StokRepo stokRepo;`): kısa görünür ama bağımlılığı dışarıdan verecek bir **giriş noktası yoktur**; ne yapıcı parametresi ne setter. Alanı yalnız kap doldurabilir. Bunu, çalışma anında sınıfın içine bakıp alanı zorla yazan yansıma (reflection) mekanizmasıyla yapar.

Testte `new SiparisServisi()` dersen alan `null` kalır. Sahte repo koyabilmek için ya Spring kabını ayağa kaldırırsın ya `ReflectionTestUtils`/yansıma ile zorlarsın. Alan `final` olamaz ve bağımlılık dışarıdan görünmez. Yasak değildir, derlenir ve çalışır — sadece önerilmez. (Enjekte edilen tipin arayüz olma zorunluluğu da yoktur; somut sınıf da enjekte edilebilir. `@Service` derleme koşulu değil, tarama koşuludur.)

Constructor injection'ın bir yan etkisi var. İki bean birbirini yapıcıda isterse (**döngüsel bağımlılık** — circular dependency) hiçbiri diğerinden önce kurulamaz. Constructor injection bunu **çözmez**, erken teşhis eder. Spring Boot 2.6'dan beri döngüler **varsayılan olarak yasaktır** (`spring.main.allow-circular-references=false`). Böylece hatayı canlıda değil, uygulama ayağa kalkarken görürsün.

Container'ın iki yüzü vardır:

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ApplicationContext BeanFactory'yi genişletir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Container'ın iki yüzü</text>
  <rect x="34" y="44" width="572" height="150" rx="14" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="52" y="68" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">ApplicationContext (üst küme · pratikte standart)</text>
  <rect x="54" y="86" width="228" height="92" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="168" y="110" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">BeanFactory</text>
  <text x="168" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çıplak çekirdek</text>
  <text x="168" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bean tanımları · lazy</text>
  <text x="168" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istenince kurar</text>
  <text x="308" y="110" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">+ event yayınlama</text>
  <text x="308" y="132" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">+ i18n MessageSource</text>
  <text x="308" y="154" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">+ otomatik BeanPostProcessor</text>
  <text x="308" y="176" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">+ eager singleton (erken hata)</text>
</svg>
```

`BeanFactory` çıplak çekirdektir: bean tanımlarını tutar ve nesneyi ancak istendiğinde kurar. Buna tembel yükleme (lazy — ihtiyaç olunca kurma) denir.

`ApplicationContext` onu **genişletir** (extends); rakip değil üst kümedir. Üstüne şunları koyar: uygulama olayları (`ApplicationEvent`), uluslararasılaştırma (i18n `MessageSource`), kaynak yükleme ve `BeanPostProcessor`/`BeanFactoryPostProcessor` sınıflarının **otomatik** tespiti. Bir de tek örnekli (singleton) bean'leri peşinen kurar — buna hevesli yükleme (eager) denir — ve böylece hataları başlangıçta yakalar. Pratikte neredeyse her zaman `ApplicationContext` kullanılır; ikisi arasında "XML mi anotasyon mu" ya da "web mi konsol mu" gibi bir iş bölümü yoktur.

### Mülakatta ne sorulur?

- **"Neden constructor injection?"** → `final` alan + yarı-kurulumun imkânsızlığı + container'sız test. "Daha az bellek kullanır" yanlıştır; ikisi de kurulumda reflection kullanabilir.
- **Spring 4.3+ kuralı** → tek yapıcı: `@Autowired` örtük; çok yapıcı: biri işaretlenmeli.
- **Döngüsel bağımlılık** → Boot 2.6+ varsayılan **yasak**; uygulama açılışta hemen hata verir (fail-fast). Doğru çözüm ayarı `true` yapmak değil, tasarımı düzeltmek; son çare `@Lazy` ile tembel yükleme.
- **Field injection yasak mı?** → Hayır, derlenir ve çalışır; kötü olan test edilebilirlik ve değişmezliktir.
- **`BeanFactory` vs `ApplicationContext`** → alt küme / üst küme; eager singleton, event, i18n, otomatik BeanPostProcessor farkı.
- **Sık çeldiriciler** → "IoC kod akışını tersten çalıştırır", "DI bean'leri thread-safe yapar", "constructor injection döngüyü otomatik çözer": üçü de yanlış.

### Tek cümlede

IoC "nesnemi kim kurar?" kararını container'a devretmek, DI ise container'ın bu bağımlılıkları — tercihen yapıcı üzerinden — dışarıdan vermesidir.

# ioc-1 | Temel
## Soru
Inversion of Control (kontrolün tersine çevrilmesi) prensibi Spring bağlamında neyi ifade eder?
## A
Uygulama sınıfları ihtiyaç duydukları bağımlılıkları `new` ile kendileri yaratır ve kullanacakları somut tipi doğrudan kendileri seçer
🤖 Bu IoC'nin tam TERSİ — bağımlılığı sınıfın kendisinin yaratması geleneksel akıştır.
## B
Nesne yaratma ve bağımlılık kurma sorumluluğu koddan alınıp container'a devredilir
🤖 Doğru: "kontrolün tersine çevrilmesi" tam olarak bu devri anlatır.
## C
İş mantığının çalışma sırası (kontrol akışı) tersten, sondan başa doğru işletilir
🤖 IoC kod akış yönüyle değil, nesne kurma sorumluluğunun kime ait olduğuyla ilgilidir.
## D
Metot çağrıları senkron yerine asenkron sıraya alınır; çağıran taraf sonucu beklemez
🤖 IoC eşzamanlılık/asenkronlukla ilgili değildir; bağımlılık sahipliğiyle ilgilidir.
## Doğru
B
## Açıklama
IoC, "nesnelerimi ben mi kurarım, yoksa çerçeve mi kurup bana verir?" sorusunun cevabını **çerçeveye** çevirir. Geleneksel kodda sınıf, iş birliği yaptığı nesneleri (collaborators) `new` ile kendisi yaratır — kontrol koddadır (A). IoC'de bu sorumluluk **container'a** devredilir; sınıf yalnız neye ihtiyacı olduğunu bildirir, container onu kurar ve enjekte eder. "Control" burada iş mantığının çalışma sırası (C) veya asenkronluk (D) değil, **nesne yaratma/bağlama sorumluluğudur**.

# ioc-2 | Temel
## Soru
Bağımlılık enjeksiyonunun (DI) sağladığı en temel mimari fayda aşağıdakilerden hangisidir?
## A
Uygulamanın çalışma zamanı hızını doğrudan artırır çünkü container tüm nesneleri önbelleğe alıp yeniden kullanır
🤖 DI bir hız optimizasyonu değildir; amacı bağlantı yapısını gevşetmektir.
## B
Sınıfların birbirini `import` etmesini tamamen ortadan kaldırır, paketler arası bağı sıfırlar
🤖 Arayüzler hâlâ import edilir; DI import'u değil, somut tipe SIKI bağlılığı azaltır.
## C
Sınıfı somut tipe değil arayüze bağlar; gevşek bağlılık ve test edilebilirlik sağlar
🤖 Doğru: gevşek bağlılık (loose coupling) ve mock ile test DI'nin ana kazancıdır.
## D
Tüm bean'leri otomatik olarak thread-safe (iş parçacığı güvenli) yapar, paylaşılan durumu güvenli kılar
🤖 DI eşzamanlılık güvenliği vermez; singleton bean'lerde durum paylaşımı hâlâ tehlikelidir.
## Doğru
C
## Açıklama
DI'nin özü **gevşek bağlılıktır** (loose coupling): sınıf, iş birlikçisini kendisi yaratmak yerine dışarıdan (constructor/setter) aldığı için somut sınıfa değil **arayüze** bağlanabilir. Bu, üretimde gerçek implementasyonu, testte ise sahte (mock/stub) bir implementasyonu enjekte etmeyi mümkün kılar — test edilebilirlik doğrudan buradan gelir. DI bir performans hilesi (A) değildir, import'ları yok etmez (B) ve bean'leri sihirli biçimde thread-safe yapmaz (D) — singleton bean'de paylaşılan değişken durum hâlâ senin sorumluluğundadır.

# ioc-3 | Orta
## Soru
2026'da yeni bir Spring servisi yazarken constructor injection'ın field injection'a tercih edilmesinin en güçlü teknik gerekçesi nedir?
## A
Alan `final` olabilir, nesne yarı-kurulmuş var olamaz ve container'sız `new` ile kurulabilir
🤖 Doğru: değişmezlik + zorunlu bağımlılık garantisi + container'sız kurulabilirlik.
## B
Constructor injection daha az bellek kullanır çünkü Spring alanları reflection ile doldurmak zorunda kalmaz
🤖 İkisi de kurulumda reflection kullanabilir; tercih sebebi bellek değil.
## C
Field injection Spring Boot 3.x'te derleme hatası verir; `@Autowired` artık alan üzerinde kullanılamaz
🤖 Field injection hâlâ derlenir ve çalışır; sadece önerilmez, yasak değildir.
## D
Constructor injection döngüsel bağımlılıkları (circular dependency) otomatik çözer, container döngüyü kendiliğinden kırar
🤖 Aksine; constructor injection döngüde container'ı BAŞLATMADAN patlatır, çözmez.
## Doğru
A
## Açıklama
Constructor injection'ın üç somut üstünlüğü vardır: (1) bağımlılık alanı `final` olabilir — **değişmezlik** (immutability) ve yayınlama güvenliği; (2) zorunlu bağımlılık **yapıcıda** verildiği için nesne asla yarı-kurulmuş halde var olamaz — NPE riski erkene, kurulum anına çekilir; (3) nesne salt `new Service(mockRepo)` ile, **container olmadan** kurulabilir → birim testi kolaylaşır. Field injection'da alan `final` olamaz ve test için ya reflection ya da container gerekir. Field injection yasak değildir (C yanlış); sadece önerilmez. Döngüsel bağımlılığı constructor injection çözmez, aksine erken teşhis eder (D yanlış).

# ioc-4 | Orta
## Soru
Aşağıdaki `@Autowired` field injection kullanan servis birim testinde neden sorun çıkarır?

```java
@Service
class SiparisServisi {
    @Autowired
    private StokRepo stokRepo;   // field injection
    // ...
}
```
## A
`@Service` anotasyonu olmadan sınıf hiç derlenmez; stereotype anotasyon bir derleme koşuludur
🤖 `@Service` derleme değil, tarama (component scan) için gereklidir; kod derlenir.
## B
`stokRepo` alanı `final` olduğu için testte sahte (mock) bir repo ile yeniden atanamaz
🤖 Field injection'da alan zaten `final` OLAMAZ; sorun bu değil.
## C
`StokRepo` mutlaka bir arayüz olmak zorundadır; somut sınıf `@Autowired` ile enjekte edilemez
🤖 Somut sınıf da enjekte edilebilir; kısıt bu değil.
## D
Testte `new SiparisServisi()` ile kurulan nesnede `stokRepo` `null` kalır
🤖 Doğru: dışarıdan verecek yapıcı/setter yok, alan container dışında `null` kalır.
## Doğru
D
## Açıklama
Field injection'da bağımlılığı dışarıdan verecek bir **giriş noktası yoktur** (yapıcı parametresi veya setter). Alanı yalnız container reflection ile doldurur. Birim testinde `new SiparisServisi()` dediğinde `stokRepo` **null** kalır ve testte sahte bir repo koyabilmek için ya Spring container'ı ayağa kaldırman ya da `ReflectionTestUtils`/reflection ile alanı zorla enjekte etmen gerekir — ikisi de kırılgan ve ağırdır. Constructor injection'da ise `new SiparisServisi(mockRepo)` yeterlidir. A ve C konudan bağımsız yanlışlar; B ise field injection'ın `final` olamama gerçeğiyle çelişir.

# ioc-5 | İleri
## Soru
`BeanFactory` ile `ApplicationContext` arasındaki ilişki ve fark için en doğru ifade hangisidir?
## A
İkisi tamamen ayrı, birbirini bilmeyen container'lardır; `BeanFactory` XML, `ApplicationContext` anotasyon yapılandırması içindir
🤖 `ApplicationContext`, `BeanFactory`'yi GENİŞLETİR; ayrı/rakip değildir, yapılandırma biçimiyle de ayrılmazlar.
## B
`ApplicationContext`, `BeanFactory`'yi genişletir; event, i18n ve eager singleton gibi kurumsal ekler getirir
🤖 Doğru: ApplicationContext = BeanFactory + kurumsal ekler.
## C
`BeanFactory` daha yeni ve önerilen API'dir; `ApplicationContext` geriye dönük uyumluluk için duran eski arayüzdür
🤖 Tersine: BeanFactory çekirdek/eski, ApplicationContext üst küme ve pratikte önerilendir.
## D
`ApplicationContext` yalnız web uygulamalarında, `BeanFactory` yalnız konsol uygulamalarında kullanılabilir
🤖 Böyle bir kısıt yok; ApplicationContext her tür uygulamada standarttır.
## Doğru
B
## Açıklama
`ApplicationContext`, `BeanFactory` arayüzünü **genişleten** (extends) bir üst kümedir. `BeanFactory` container'ın çıplak çekirdeğidir: bean tanımlarını tutar, istendiğinde (lazy) kurar. `ApplicationContext` bunun üstüne kurumsal yetenekler koyar: uygulama olayları (`ApplicationEvent`), uluslararasılaştırma (i18n `MessageSource`), kaynak yükleme, `BeanPostProcessor`/`BeanFactoryPostProcessor`'ların **otomatik** tespiti ve varsayılan olarak singleton'ları **erken (eager)** kurup başlangıçta hataları yakalama. Bu yüzden pratikte neredeyse her zaman `ApplicationContext` kullanılır. A/C ilişkiyi ters kurar, D uydurma bir kısıt getirir.

# ioc-6 | İleri
## Soru
Spring 4.3+ ve Spring Boot 3.x'te aşağıdaki sınıf için doğru olan ifade hangisidir?

```java
@Service
class RaporServisi {
    private final SablonMotoru motor;
    RaporServisi(SablonMotoru motor) {   // @Autowired YOK
        this.motor = motor;
    }
}
```
## A
Tek yapıcı olduğu için `@Autowired` gereksizdir; Spring bu yapıcıyı otomatik seçer
🤖 Doğru: tek constructor ise @Autowired örtük kabul edilir (Spring 4.3+).
## B
`@Autowired` olmadığı için Spring parametresiz yapıcı arar; bulamayınca `BeanInstantiationException` verir
🤖 Parametresiz yapıcı zorunlu değil; tek yapıcı zaten enjeksiyon için seçilir.
## C
`motor` alanı `final` olduğu için Spring onu dolduramaz ve container başlangıçta hata verir
🤖 `final` alan constructor injection'ın istenen halidir; sorun çıkarmaz.
## D
Bu yapı yalnız XML yapılandırmayla çalışır; component scan ile bulunan bean'lerde `@Autowired` zorunludur
🤖 Anotasyon/component-scan bağlamında sorunsuz çalışır.
## Doğru
A
## Açıklama
Spring 4.3'ten beri bir bean sınıfının **tek bir yapıcısı** varsa, o yapıcıda `@Autowired` yazmak **gereksizdir** — Spring onu tek aday olduğu için otomatik enjeksiyon noktası kabul eder. Bu, modern (Boot 3.x/4) constructor injection kodunu daha temiz yapar: `final` alan + tek yapıcı + `@Autowired` yok. Birden çok yapıcı olsaydı Spring hangisini kullanacağını bilemez, o zaman birini `@Autowired` ile işaretlemen gerekirdi. `final` alan burada sorun değil, tam tersine amaçtır (C yanlış). Parametresiz yapıcı gerekmez (B yanlış), yapı anotasyon bağlamında da çalışır (D yanlış).
