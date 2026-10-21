---
slug: hexagonal-clean-arch
baslik: "Hexagonal / Clean Mimari: Port-Adapter, Bağımlılık Yönü, Domain İzolasyonu ve Test Edilebilirlik"
kademe: İleri
sira: 136
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir hastaneyi düşün: hastalığın nasıl tedavi edileceği bilgisi doktorun kafasındadır; hangi marka tomografi cihazı kullanıldığı, randevunun telefonla mı internetten mi alındığı ise **ayrıntıdır**. Cihaz yenilenince tıp bilgisi çöpe gitmez. Hexagonal (ports & adapters) ile Clean Architecture aynı fikrin iki adıdır: **iş kuralları merkezde, teknoloji kenarda** dursun. Böylece veritabanını, web çerçevesini ya da mesajlaşma altyapısını değiştirmek uygulamanın kalbini yeniden yazmayı gerektirmez. Üstelik o kalbi test etmek için koca bir sistemi ayağa kaldırman da gerekmez.

### Temel kavramlar

- **Katman (layer)**: birbirine benzer sorumlulukları toplayan kod grubu (web, iş mantığı, veri erişimi). Katman **paket/klasör** değil, bir **bağımlılık kuralıdır**: kim kimi çağırabilir?
- **Domain (çekirdek/alan)**: işletmenin gerçek kuralları. "Bakiye eksiye düşemez", "faiz şöyle hesaplanır" gibi, uygulama olmasa da doğru olan bilgiler.
- **Altyapı (infrastructure)**: veritabanı, HTTP, kuyruk, dosya sistemi, çerçeveler. Hepsi **ayrıntıdır**, değişir.
- **Port (bağlantı noktası)**: çekirdeğin dış dünyayla konuştuğu **arayüzdür**, yani bir sözleşmedir. İki türlüdür. **İçeri bakan port (inbound / driving)**: uygulamaya giriş kapısı, bir kullanım senaryosu (use case) arayüzü. **Dışarı bakan port (outbound / driven)**: çekirdeğin dışarıdan ihtiyaç duyduğu yetenek; örneğin `SiparisDeposu`. Bilgisayardaki fiziksel "port" ile ilgisi yoktur.
- **Adapter (adaptör)**: bir port'un **teknolojiye bağlı somut implementasyonu** — REST controller (inbound), JPA repository (outbound), Kafka consumer. **Port'lar sabit ve çekirdeğe aittir; adaptörler değişir ve kenardadır.**
- **Dikiş (seam)**: programı ikiye bölüp arasına başka bir şey koyabildiğin nokta. Portlar tam olarak budur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hexagonal: bağımlılık daima içeriye, domain merkezde">
  <defs>
    <marker id="hex-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bağımlılık daima içeriye akar</text>
  <rect x="70" y="40" width="500" height="160" rx="12" fill="#F4F1FA" stroke="#7A5BA6" stroke-width="1.4"/>
  <text x="150" y="60" text-anchor="middle" font-size="10.5" font-weight="700" fill="#7A5BA6" font-family="system-ui">Adaptörler (web, JPA, Kafka)</text>
  <rect x="150" y="72" width="340" height="112" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="230" y="90" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Uygulama (use case)</text>
  <rect x="230" y="102" width="180" height="72" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="320" y="132" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Domain</text>
  <text x="320" y="152" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">saf iş kuralı</text>
  <line x1="120" y1="150" x2="222" y2="140" stroke="#186399" stroke-width="1.6" marker-end="url(#hex-ok)"/>
  <line x1="520" y1="150" x2="418" y2="140" stroke="#186399" stroke-width="1.6" marker-end="url(#hex-ok)"/>
  <text x="320" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">domain hiçbir dış katmanı bilmez; dış katman domain'e bağlanır</text>
</svg>
```

### Nasıl çalışır?

**Bağımlılık Kuralı (Dependency Rule)** bu mimarinin bel kemiğidir. Kural şudur: **kaynak kod bağımlılıkları yalnız içeriye** işaret edebilir; yani dıştaki ayrıntıdan içteki soyutlamaya doğru. İç katmanlar dış katmanların **varlığından bile habersizdir**. Bu yüzden domain katmanı `org.springframework.*`, `jakarta.persistence` ya da JDBC tiplerini **import edemez**.

Peki domain kaydetmek için dışarıya nasıl "ulaşır"? **Bağımlılığı tersine çevirerek (DIP)**: `SiparisDeposu` **arayüzünü** çekirdek tanımlar (port çekirdeğe aittir), dıştaki JPA adaptörü onu **implemente eder**. Böylece çalışma-zamanı akışı dışa gitse de **derleme-zamanı bağımlılığı içe** kalır.

```svg
<svg viewBox="0 0 640 196" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Inbound ve outbound port ile adaptörler">
  <defs>
    <marker id="hex-port-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki tür port: giren (driving) ve çıkan (driven)</text>
  <rect x="20" y="70" width="120" height="60" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="80" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">REST</text>
  <text x="80" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">controller</text>
  <rect x="240" y="60" width="160" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="320" y="94" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Use case</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ Domain</text>
  <rect x="500" y="70" width="120" height="60" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="560" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">JPA</text>
  <text x="560" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">adapter</text>
  <text x="190" y="82" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">inbound</text>
  <text x="190" y="94" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">port</text>
  <text x="450" y="82" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">outbound</text>
  <text x="450" y="94" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">port</text>
  <line x1="140" y1="100" x2="238" y2="100" stroke="#186399" stroke-width="1.6" marker-end="url(#hex-port-ok)"/>
  <line x1="500" y1="100" x2="402" y2="100" stroke="#186399" stroke-width="1.6" marker-end="url(#hex-port-ok)"/>
  <text x="320" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">port'lar sabit ve çekirdeğe ait; adaptörler değişir ve kenarda</text>
  <text x="320" y="186" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">iki bağımlılık oku da çekirdeğe (soyutlamaya) bakar</text>
</svg>
```

**İç halkalar ikiye ayrılır.** En içte **entities/domain** durur: işletmenin **kurum geneli kuralları**. Bu, en kararlı ve uygulamalar arasında paylaşılabilen özdür. Onun dışında **kullanım senaryosu (use case / application)** katmanı vardır: **bu uygulamaya özgü** iş akışı.

Kullanım senaryosu bir orkestra şefidir. Dışarı bakan portları çağırır ve domain nesnelerini koordine eder; ama **temel iş kuralını kendi içinde taşımaz**. Ayrımın gerekçesi SRP'dir: kurum geneli kural (faiz nasıl hesaplanır) nadiren değişir, uygulama akışı (bu ekranda önce şu sonra bu) daha sık değişir. Domain'i "yalnız getter/setter taşıyan veri" sanmak ise **anemik domain modeli** anti-desenidir; o zaman iş kuralı servislere sızar.

**Çerçeve bağımsızlığı** Spring'i yasaklamak demek değildir; onu **doğru yere koymak** demektir. Spring, JPA ve Kafka **adaptör ve yapılandırma** katmanında serbestçe kullanılır; çekirdek onların tiplerine bağlanmaz. `@Entity` işaretli bir sınıfı hem domain hem kalıcılık modeli yapmaya çalışmak yalıtımı kaybettirir. Bu yüzden çoğu ekip domain modeli ile JPA entity'sini birbirinden ayırır.

```svg
<svg viewBox="0 0 640 188" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test edilebilirlik: outbound port yerine sahte enjekte edilir">
  <defs>
    <marker id="hex-test-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Port bir dikiştir: testte sahte, üretimde gerçek</text>
  <rect x="40" y="66" width="170" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="125" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Use case (saf)</text>
  <text x="125" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">SiparisDeposu'na bağlı</text>
  <rect x="270" y="76" width="120" height="40" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="330" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">«port»</text>
  <text x="330" y="110" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">SiparisDeposu</text>
  <rect x="450" y="42" width="160" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="530" y="60" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Testte: in-memory</text>
  <text x="530" y="75" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">mock/fake — DB yok</text>
  <rect x="450" y="110" width="160" height="40" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="530" y="128" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Üretimde: JPA</text>
  <text x="530" y="143" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gerçek adapter</text>
  <line x1="210" y1="96" x2="268" y2="96" stroke="#186399" stroke-width="1.6" marker-end="url(#hex-test-ok)"/>
  <line x1="390" y1="90" x2="448" y2="66" stroke="#186399" stroke-width="1.4" marker-end="url(#hex-test-ok)"/>
  <line x1="390" y1="102" x2="448" y2="126" stroke="#186399" stroke-width="1.4" marker-end="url(#hex-test-ok)"/>
  <text x="320" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çekirdek somuta değil arayüze bağlı olduğundan konteynersiz hızlı test</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Clean, katmanlı mimarinin yeni adı mı?"** Hayır. Klasik katmanlı mimaride bağımlılık **yukarıdan aşağı**, veritabanına doğru akar: `Servis → Repository (somut) → DB`. Yani iş mantığı kalıcılığa dolaylı olarak bağımlıdır. Clean'de ise ok **daima içeriye** bakar: repository **arayüzü** çekirdeğe aittir, gerçekleştirimi kenara. Paket adlarını `service` yerine `usecase` yapmak bu yönü değiştirmez. Belirleyici olan katmanların varlığı ya da sayısı değil, **bağımlılığın yönüdür**.
- **"Test edilebilirlik nereden geliyor?"** Sihirden değil, **geometriden**. Çekirdek dış dünyaya somut sınıflarla değil **portlarla** bağlıdır; bu yüzden testte portların yerine hızlı sahteler konur (mock, fake, bellek içi gerçekleştirim). Domain saf Java olduğu için Spring konteyneri hiç ayağa kalkmadan milisaniyelik birim testleri çalışır. Gerçek veritabanına bağlanmak testi yavaşlatır ve kırılganlaştırır; hexagonal mimari tam da bunu gereksiz kılar.
- **Ölçü.** Hexagonal bedava değildir. Küçük bir CRUD servisinde port/adaptör tabakası **gereksiz tören** olabilir. Değeri, iş kuralının **karmaşık ve uzun ömürlü** olduğu ve teknolojinin değişmesi beklenen sistemlerde ortaya çıkar. Java'da katman yönünü söz vererek korumak da yetmez; **ArchUnit** ile mekanik olarak zorlanmalıdır.

### Tek cümlede

Hexagonal/Clean mimari, iş kurallarını merkeze koyar ve tüm kaynak kod bağımlılıklarını içeriye çevirir. Böylece teknolojiyi değiştirilebilir bir ayrıntıya, çekirdeği de hızlı test edilebilir bir öze dönüştürür.

# hex-1 | Orta
## Soru
Hexagonal mimaride "port" ve "adapter" kavramları ne anlama gelir?
## A
Port bir ağ portu ya da veritabanı bağlantısı, adapter ise onu süren bir sürücüdür
🤖 Bunlar altyapı terimleri; hexagonal'da port/adapter fiziksel bağlantı değil, mimari roldür.
## B
Port çekirdeğin dışa açılan arayüzü, adapter onun somut implementasyonudur
🤖 Doğru: port = sözleşme (çekirdeğe ait), adapter = teknolojiye bağlı, değiştirilebilir uygulama.
## C
Port bir GoF tasarım kalıbı, adapter ise Spring'in sağladığı hazır bir anotasyondur
🤖 İkisi de mimari roldür; hiçbiri çerçeveye ya da belirli bir kalıp kataloğuna bağlı değildir.
## D
Port ile adapter aynı şeydir; ikisi de altyapı katmanındaki somut sınıflardır
🤖 Aynı değiller: port soyut arayüz ve çekirdeğe ait, adapter onun kenardaki somut karşılığı.
## Doğru
B
## Açıklama
Hexagonal (ports & adapters) mimaride **port**, çekirdeğin (domain + use case) dış dünyayla iletişim kurduğu bir **arayüzdür** — bir sözleşme. İki tür vardır: **inbound/driving port** (uygulamaya giriş, ör. bir use case arayüzü) ve **outbound/driven port** (çekirdeğin dışa ihtiyaç duyduğu yetenek, ör. `SiparisDeposu`, `OdemeGecidi`). **Adapter** ise bir port'un **somut, teknolojiye bağlı implementasyonudur**: bir REST controller (inbound adapter) ya da bir JPA repository (outbound adapter). Kritik ayrım şudur: **port'lar sabit ve çekirdeğe aittir; adapter'lar değişebilir ve kenardadır.** Böylece PostgreSQL'den MongoDB'ye ya da REST'ten gRPC'ye geçtiğinde yalnız adapter değişir, domain ve port'lar dokunulmaz kalır. "Port" kelimesi buradaki anlamıyla bir ağ portu ya da veritabanı bağlantısı değildir (A); kavram herhangi bir çerçeveye ya da anotasyona bağlı değildir (C); ve port ile adapter birbirinin yerine geçmez (D) — biri soyutlama, diğeri onun somut karşılığıdır.

# hex-2 | Orta
## Soru
Clean/Hexagonal mimarinin "Bağımlılık Kuralı" (Dependency Rule) neyi zorunlu kılar?
## A
Katmanlar ihtiyaç duydukları her katmana serbestçe bağlanabilmelidir
🤖 Serbest bağ tam da kuralın engellediği şeydir; bağımlılık tek yönlü olmak zorundadır.
## B
Domain katmanı, veritabanı ve web katmanlarına doğrudan bağımlı olmalıdır
🤖 Ters: domain dışa bağlanamaz; dış katmanlar domain'e bağlanır.
## C
Bağımlılık dıştan içe olmalı, ama iç katman dıştaki somut sınıfı çağırabilmelidir
🤖 Karışık ve yanlış: iç katman dış somut tipi çağırırsa bağımlılık zaten dışa dönmüş olur.
## D
Kaynak-kod bağımlılıkları yalnız içeriye, merkeze doğru işaret etmelidir
🤖 Doğru: bağımlılık oku hep merkeze bakar; domain hiçbir dış ayrıntıya bağımlı olamaz.
## Doğru
D
## Açıklama
**Bağımlılık Kuralı**, mimarinin bel kemiğidir: **kaynak-kod bağımlılıkları yalnızca içeriye**, yani dıştaki ayrıntıdan (web, DB, framework) içteki soyutlamaya (use case, domain) doğru işaret edebilir. İç katmanlar dış katmanların **varlığından bile habersizdir** — domain, JPA'yı, Spring'i ya da REST'i import edemez. Peki domain dışa nasıl "ulaşır" (ör. kaydetmek için)? **Bağımlılığı tersine çevirerek (DIP)**: çekirdek bir `SiparisDeposu` **arayüzü** tanımlar (port, çekirdeğe aittir), dıştaki JPA adaptörü bu arayüzü **implemente eder**. Böylece çalışma-zamanı akışı dışa gitse de **derleme-zamanı bağımlılığı içe** kalır — C'nin karıştırdığı nokta tam budur: iç katman dıştaki **somut** tipi çağırırsa kural çoktan kırılmıştır; iç katman yalnız **kendi tanımladığı arayüzü** çağırır. Serbest bağ (A) ve domain'in DB'ye bağlanması (B) kuralı doğrudan ihlal eder. Bu kural Java'da **ArchUnit** ile test aşamasında mekanik olarak zorlanabilir; sözle verilen mimari sözü, ilk teslim baskısında bozulur.

# hex-3 | İleri
## Soru
Domain (çekirdek) katmanının "izole" olması pratikte hangi kod disiplinini gerektirir?
## A
Domain sınıfları hiçbir çerçeve/altyapı tipini import etmemeli, saf Java kalmalıdır
🤖 Doğru: izolasyon = domain'in dış teknolojiye sıfır derleme-zamanı bağımlılığıdır.
## B
Domain sınıfları performans için sorgularını doğrudan JDBC ile çalıştırmalıdır
🤖 JDBC bir dış ayrıntıdır; domain'e sızması izolasyonu ve test edilebilirliği bozar.
## C
Domain, ihtiyaç duyduğu dış servisleri statik singleton'lar üzerinden çağırmalıdır
🤖 Statik dış çağrı gizli bir global bağımlılıktır; bağımlılık port'lanıp enjekte edilmelidir.
## D
Domain sınıfları Spring bean'i olmalı ve bağımlılıklarını @Autowired ile almalıdır
🤖 Çerçeveye bağlanmak izolasyonun tersidir; domain Spring'in varlığını bilmemelidir.
## Doğru
A
## Açıklama
Domain izolasyonu, çekirdeğin **hiçbir dış teknoloji tipine derleme-zamanı bağımlılığı olmaması** demektir. Pratikte bu şu disiplindir: domain sınıfları `jakarta.persistence` (JPA anotasyonları), `org.springframework.*`, HTTP/servlet tipleri ya da JDBC/SQL sürücülerini **import edemez**; yalnız saf Java ve gerekirse kendi tanımladığı **port arayüzlerini** kullanır. Neden? Çünkü bu tipler **ayrıntıdır** ve değişir; domain'e sızarlarsa iş kuralını teknolojiye zincirler, testi yavaşlatır ve DB/çerçeve değişiminde çekirdeği kırar. `@Entity`'li bir sınıf hem domain hem persistence modeli olmaya çalışırsa izolasyon çoktan kaybedilmiştir — bu yüzden çoğu ekip **domain modeli ile JPA entity'sini ayırır**. JDBC çağrısı (B) performans bahanesiyle ayrıntıyı merkeze taşır; statik singleton (C) gizli global bağımlılık üretip testte yer değiştirmeyi imkânsızlaştırır; `@Autowired` (D) ise çekirdeği doğrudan çerçeveye bağlar. Kural mekaniktir: ArchUnit ile "domain paketi şu paketlere bağımlı olmasın" testi yazılır.

# hex-4 | İleri
## Soru
Clean Architecture'da "use case" (application) katmanının domain (entities) katmanından ayrı tutulmasının gerekçesi nedir?
## A
Use case iş kurallarını tutar; domain ise yalnız getter/setter taşıyan bir veri sınıfıdır
🤖 Bu anemik model tuzağıdır; kurumsal kural domain'de olmalı, use case onu yalnız orkestralar.
## B
İkisi aynı sorumluluğu taşır; ayrım yalnız paket adında kalan bir biçimselliktir
🤖 Sorumlulukları farklıdır: biri kurumsal kural, diğeri uygulamaya özgü akış.
## C
Domain kurumsal kuralı tutar, use case ise uygulamaya özgü akışı orkestralar
🤖 Doğru: farklı değişim nedenleri olduğu için (SRP) ayrı katmanlarda dururlar.
## D
Use case veritabanı şemasını, domain ise kullanıcı arayüzünü tanımlayan katmandır
🤖 İkisi de altyapı değildir; şema ve arayüz kenardaki adaptörlerin konusudur.
## Doğru
C
## Açıklama
Clean Architecture iç halkaları iki seviyeye ayırır: en içte **entities/domain** — işletmenin **kurumsal (enterprise-wide) kuralları** ve en kararlı, uygulamalar arası paylaşılabilen özü; onun dışında **use case (application)** — **bu uygulamaya özgü** iş akışları. Use case bir **orkestra şefidir**: outbound port'ları çağırır (`depo.bul(...)`), domain nesnelerini koordine eder, akışı yürütür ama **temel iş kuralını içermez** — o domain'dedir. Ayrımın gerekçesi SRP'dir: bu iki katmanın **değişme nedenleri farklıdır**. Kurumsal kural (faiz nasıl hesaplanır) nadiren ve iş gereği değişir; uygulama akışı (bu ekranda önce şunu sonra bunu yap) daha sık ve uygulamaya özgü değişir. Domain'i "sadece getter/setter taşıyan veri" sanmak (A) **anemik domain modeli** anti-pattern'idir; iş kuralları o zaman servislere sızar ve domain değerini kaybeder. Ayrımı biçimsellik saymak (B) bu iki farklı değişim eksenini görmezden gelir; şema ve kullanıcı arayüzü (D) ise iç halkaların değil, kenardaki adaptörlerin konusudur.

# hex-5 | İleri
## Soru
"Clean Architecture aslında klasik katmanlı (layered) mimarinin yeni adıdır" iddiası neden yanlıştır?
## A
Klasik katmanlı mimaride katman kavramı yoktur; katmanları ilk kez Clean tanıtmıştır
🤖 Katmanlı mimarinin tanımı zaten katmandır; fark katmanın varlığında değil, bağımlılığın yönündedir.
## B
Fark yalnız isimlendirmededir; "servis katmanı" artık "use case" diye anılmaktadır
🤖 Ad değiştirmek yön değiştirmez; paketi yeniden adlandıran bir proje hâlâ DB'ye bağımlı olabilir.
## C
Clean mimaride halka sayısı daima dört olmak zorundadır, katmanlıda ise değişkendir
🤖 Halka sayısı zorunlu değildir; belirleyici olan sayı değil, Bağımlılık Kuralı'dır.
## D
Klasik katmanlıda bağımlılık aşağı, veritabanına doğru akar; Clean'de daima içeriye akar
🤖 Doğru: ayırt edici ölçüt bağımlılığın yönü ve çekirdeğin ayrıntıdan bağımsız kalmasıdır.
## Doğru
D
## Açıklama
Ayırt edici ölçüt **katmanın varlığı değil, bağımlılığın yönüdür**. Klasik katmanlı mimaride ok yukarıdan aşağı, **veritabanına doğru** akar: `Controller → Servis → Repository (somut) → DB`. Servis, somut bir repository sınıfına ya da JPA tiplerine bağımlıdır; yani iş mantığı kalıcılığa **transitif olarak** bağlıdır ve DB değişimi yukarı doğru dalgalanır. **Clean/Hexagonal**'da ok **daima içeriye** bakar: repository **arayüzü** (port) çekirdeğe aittir, JPA implementasyonu (adapter) kenardadır ve **içeriye** bağlanır — DIP sayesinde çalışma-zamanı akışı dışa gitse de derleme-zamanı bağımlılığı içe kalır. Bu yüzden paket adlarını `service`'ten `usecase`'e çevirmek hiçbir şey değiştirmez (B): domain hâlâ `@Entity` ve `JpaRepository` import ediyorsa mimari katmanlıdır, clean değildir. Katmanların Clean ile icat edildiğini söylemek (A) tarihsel olarak yanlıştır; halka sayısını bir kural sanmak (C) ise Uncle Bob'ın açıkça reddettiği bir okumadır — halka sayısı serbesttir, değişmeyen tek şey **Bağımlılık Kuralı**'dır.

# hex-6 | Uzman
## Soru
Hexagonal mimarinin en çok öne çıkarılan pratik faydası "test edilebilirlik"tir. Bu fayda mimarinin hangi yapısal özelliğinden **doğrudan** kaynaklanır?
## A
Test kolaylığı mimariden değil, kullanılan test çerçevesinin gücünden gelir
🤖 Test çerçevesi yazmayı kolaylaştırır ama bağımlılıkları koparamaz; koparan mimaridir.
## B
Çekirdek dışa somut sınıfla değil portla bağlı olduğundan sahte konabilir
🤖 Doğru: bağımlılığın arayüze çevrilmesi, çekirdeği dünyadan ayıran dikişi yaratır.
## C
Tüm kod tek bir modülde toplandığı için test kurulumları basitleşir
🤖 Hexagonal kodu tek modülde toplamaz; tersine çekirdeği kenardan ayırır.
## D
Testler gerçek veritabanına bağlandığı için sonuçlar daha güvenilir olur
🤖 Gerçek DB'ye bağlanmak testi yavaşlatır ve kırılganlaştırır; hexagonal bunu gereksiz kılar.
## Doğru
B
## Açıklama
Test edilebilirlik, hexagonal'ın **bağımlılık yapısının doğrudan bir sonucudur** — sihir değil, geometrinin getirisidir. Çekirdek (domain + use case), dış dünyaya **somut sınıflar üzerinden değil, port (arayüz) üzerinden** bağlıdır (Bağımlılık Kuralı + DIP). Bu iki şeyi mümkün kılar: (1) Bir use case'i test ederken outbound port'ların (`SiparisDeposu`, `OdemeGecidi`) yerine **hızlı sahteler** (mock/fake/in-memory) enjekte edersin — gerçek DB, ağ ya da mesaj kuyruğu gerekmez. (2) Domain saf Java olduğu için **Spring konteynerini ayağa kaldırmadan**, milisaniyeler süren birim testleriyle iş kurallarını izole sınarsın. Sonuç: **hızlı, deterministik, dış sistemlerden bağımsız** bir test piramidinin geniş tabanı. Test çerçevesi (A) yalnız yazımı kolaylaştırır, bağımlılığı koparamaz; hexagonal kodu tek modülde toplamaz, tersine **ayırır** (C); gerçek veritabanına bağlanmak (D) entegrasyon testinde yerinde olsa da birim testte yavaşlık ve kırılganlık getirir. Kısaca: **portlar dikiştir (seam)** — testin çekirdeği dünyadan ayırdığı yerler tam da bu arayüzlerdir.
