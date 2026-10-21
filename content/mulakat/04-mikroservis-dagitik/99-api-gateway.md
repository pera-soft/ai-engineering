---
slug: api-gateway
baslik: "API Gateway: Sorumluluklar, BFF, Gateway vs Service Mesh ve Tek Nokta Arıza"
kademe: Orta
sira: 99
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Büyük bir iş merkezini düşün: içeride yüzlerce ofis var. Ziyaretçilerin her ofisin kapısına tek tek gidip kimlik göstermesi, her ofisin ayrı bir güvenlik görevlisi tutması saçmadır. Bunun yerine binanın **tek bir girişi** olur; danışma kimliği kontrol eder, ziyaretçi kartı verir, günde kaç kişinin gireceğini düzenler ve herkesi doğru kata yönlendirir. Yazılımda bu giriş **API Gateway**'dir. Onlarca servisin önüne konur ve dışarıdan gelen her isteğin tek kapıdan geçmesini sağlar.

### Temel kavramlar

- **Servis**: bağımsız çalışan, kendisine ağ üzerinden erişilen uygulama parçası.
- **İstemci (client)**: servisi kullanan taraf — tarayıcı, mobil uygulama, başka bir sistem.
- **İstek/yanıt**: istemcinin gönderdiği çağrı ve servisin döndürdüğü sonuç.
- **Kimlik doğrulama (authentication)**: "sen kimsin?" sorusunun cevaplanması. **Yetkilendirme (authorization)**: "bunu yapmaya hakkın var mı?".
- **JWT (jeton/token)**: kullanıcı kimliğini taşıyan, imzalı ve doğrulanabilir küçük veri paketi.
- **Hız sınırlama (rate limiting)**: bir istemcinin belirli sürede atabileceği istek sayısını sınırlamak.
- **TLS sonlandırma**: şifreli bağlantının açıldığı nokta; sonrasında iç ağda düz veya yeniden şifreli devam eder.
- **Çapraz kesen işler (cross-cutting concerns)**: her serviste tekrar edecek ortak işler (kimlik, kota, log, izleme).
- **Yük dengeleyici (load balancer)**: gelen istekleri aynı işi yapan birden çok kopya arasında paylaştıran bileşen; sağlıksız kopyaya istek göndermez.
- **Durumsuz (stateless)**: bileşenin istekler arasında kendine özel bilgi saklamaması; bu sayede **herhangi bir kopya** herhangi bir isteği karşılayabilir.
- **Yatay ölçekleme**: aynı bileşenden çok sayıda kopya (örnek) çalıştırmak. **Dikey ölçekleme**: tek makineyi güçlendirmek — kopya sayısını artırmadığı için arıza dayanıklılığı sağlamaz.
- **Tek nokta arıza (single point of failure)**: düştüğünde tüm sistemi durduran bileşen.
- **Sidecar proxy**: her servisin yanına konan, o servisin tüm ağ trafiğini üstlenen küçük yardımcı süreç (ör. Envoy).
- **mTLS**: iki tarafın da sertifikayla kimliğini kanıtladığı karşılıklı şifreli bağlantı.

### Nasıl çalışır?

**API Gateway**, istemci ile iç servisler arasındaki **tek giriş kapısıdır** (single entry point). Tipik sorumlulukları şunlardır:

- **Yönlendirme (routing)**: isteği doğru servise iletir. İstemci iç yerleşimi bilmek zorunda kalmaz.
- **Kimlik doğrulama ve yetkilendirme**: jeton doğrulamayı kenarda, tek yerde toplar.
- **Hız sınırlama (rate limiting) ve kota**: bir istemcinin belirli sürede atabileceği istek sayısını kısıtlar.
- **Diğer çapraz kesen işler**: TLS sonlandırma, loglama, dağıtık izleme başlığı ekleme, sıkıştırma, önbellek, protokol çevirisi (dışa REST, içeri gRPC) ve sınırlı istek toplama.

Gateway veritabanına bağlanmaz, kod derleyip dağıtmaz. O bir **çalışma-zamanı trafik ve politika katmanıdır**.

**BFF (Backend for Frontend)**, gateway fikrinin özelleşmiş biçimidir: **her istemci türü için ayrı bir cephe**. Web BFF zengin veri toplar, mobil BFF hafif ve seçili veri gönderir; tek "herkese uyan" gateway'in her istemciyi yarı memnun etmesi önlenir. Ek fayda: her BFF'i ilgili arayüz ekibi sahiplenir, bu da Conway Yasası ile uyumludur.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="API Gateway kuzey-güney, service mesh doğu-batı trafiği">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki eksen: kuzey-güney vs doğu-batı</text>
  <rect x="250" y="36" width="140" height="34" rx="8" fill="#F4EFE4" stroke="#9A7B2E" stroke-width="1.4"/>
  <text x="320" y="58" text-anchor="middle" font-size="11.5" fill="#9A7B2E" font-family="system-ui">Dış istemciler (web/mobil)</text>
  <line x1="320" y1="70" x2="320" y2="92" stroke="#9A7B2E" stroke-width="2"/>
  <rect x="200" y="92" width="240" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="320" y="112" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">API Gateway (kuzey-güney)</text>
  <text x="320" y="127" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">routing · auth · rate limit · TLS</text>
  <line x1="250" y1="134" x2="150" y2="168" stroke="#186399" stroke-width="1.8"/>
  <line x1="320" y1="134" x2="320" y2="168" stroke="#186399" stroke-width="1.8"/>
  <line x1="390" y1="134" x2="490" y2="168" stroke="#186399" stroke-width="1.8"/>
  <rect x="86" y="168" width="128" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="150" y="190" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">siparis</text>
  <text x="150" y="208" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">+ sidecar proxy</text>
  <rect x="256" y="168" width="128" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="190" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">stok</text>
  <text x="320" y="208" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">+ sidecar proxy</text>
  <rect x="426" y="168" width="128" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="190" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">odeme</text>
  <text x="490" y="208" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">+ sidecar proxy</text>
  <line x1="214" y1="205" x2="256" y2="205" stroke="#B23B3B" stroke-width="1.8" stroke-dasharray="5 3"/>
  <line x1="384" y1="205" x2="426" y2="205" stroke="#B23B3B" stroke-width="1.8" stroke-dasharray="5 3"/>
  <text x="320" y="244" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">kesikli = Service Mesh (doğu-batı: mTLS, retry, circuit breaker)</text>
</svg>
```

**Gateway vs Service Mesh** en sık karışan ayrımdır. Rakip değildirler; farklı **trafik eksenlerini** yönetirler. **API Gateway → kuzey-güney (north-south)**: dışarıdan sisteme giren/çıkan trafik. **Service Mesh (Istio, Linkerd) → doğu-batı (east-west)**: sistem içindeki servis-servis trafiği. Her servisin yanına bir **sidecar proxy** konur. Bu vekil sayesinde mTLS, **yeniden deneme (retry)**, **zaman aşımı (timeout)**, **devre kesici (circuit breaker)** ve gözlemlenebilirlik **uygulama koduna hiç dokunmadan** sağlanır. Trafiğin küçük bir yüzdesini yeni sürüme yönlendirme (**canary**, kademeli yayın) da bu katmanda yapılır. Büyük sistemlerde ikisi birlikte kullanılır. Mesh, gateway'in yerini almaz; dış istemcinin gireceği bir kapı yine gerekir.

Kritik risk: gateway **tek nokta arıza** ve darboğaz adayıdır — tüm dış trafik ondan geçer. Gateway düşerse istekler kendiliğinden servislere dağılmaz; sistem dışarıya kapanır. Azaltma yolu gateway'i kaldırmak da değildir (o zaman kimlik/kota merkezi kaybolur ve istemci iç topolojiye bağlanır). Doğru yol: gateway'i **durumsuz** tut, **yatay ölçekle** (çok örnek), önüne bir **yük dengeleyici** koy ve yüksek erişilebilir (HA, çok bölge, sağlık kontrollü) kurgula. Tek güçlü makineye taşımak (dikey ölçekleme) arızayı ortadan kaldırmaz, çünkü hâlâ tek kopya vardır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gateway tek nokta arıza riski: yük dengeleyici arkasında çok örnek">
  <defs>
    <marker id="api-gateway-ok" markerWidth="9" markerHeight="9" refX="7.5" refY="3" orient="auto">
      <path d="M0,0 L7.5,3 L0,6 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek kapı mantıksal tek, fiziksel yedekli</text>
  <rect x="250" y="38" width="140" height="32" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="320" y="59" text-anchor="middle" font-size="11" fill="#B26B00" font-family="system-ui">dış istemciler</text>
  <line x1="320" y1="70" x2="320" y2="86" stroke="#186399" stroke-width="1.8" marker-end="url(#api-gateway-ok)"/>
  <rect x="238" y="88" width="164" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="320" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Yük dengeleyici (HA)</text>
  <line x1="270" y1="118" x2="150" y2="140" stroke="#186399" stroke-width="1.6" marker-end="url(#api-gateway-ok)"/>
  <line x1="320" y1="118" x2="320" y2="140" stroke="#186399" stroke-width="1.6" marker-end="url(#api-gateway-ok)"/>
  <line x1="370" y1="118" x2="490" y2="140" stroke="#186399" stroke-width="1.6" marker-end="url(#api-gateway-ok)"/>
  <rect x="96" y="142" width="108" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="150" y="164" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">gateway #1</text>
  <rect x="266" y="142" width="108" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="320" y="164" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">gateway #2</text>
  <rect x="436" y="142" width="108" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="490" y="164" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">gateway #3</text>
  <text x="320" y="198" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">durumsuz + yatay ölçek → biri düşse sistem ayakta</text>
</svg>
```

Gateway'e **iş mantığı koymak** ayrı bir anti-desendir. Alan kuralları oraya sızdıkça dört sonuç doğar:

- Gateway **gizli bir monolite** dönüşür.
- Kuralın bir parçası dışarıda kaldığı için, kuralın sahibi servis bağımsız evrilemez.
- Her kural değişikliği gateway'in yeniden dağıtımını gerektirir; gateway bir **dağıtım kilidi** olur.
- "Bu kural nerede yazılı?" sorusunun cevabı belirsizleşir.

Bu yaklaşımın kod tekrarını azalttığı görüntüsü yanıltıcıdır. Sorun testle de çözülmez; mesele kodun kalitesi değil, sorumluluğun yanlış yerde durmasıdır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="BFF: her istemci türü için ayrı cephe">
  <defs>
    <marker id="api-gateway-bff" markerWidth="9" markerHeight="9" refX="7.5" refY="3" orient="auto">
      <path d="M0,0 L7.5,3 L0,6 Z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">BFF: her istemciye özel cephe</text>
  <rect x="70" y="40" width="150" height="32" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="145" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Web istemci</text>
  <rect x="420" y="40" width="150" height="32" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="495" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Mobil istemci</text>
  <line x1="145" y1="72" x2="145" y2="92" stroke="#6B3FA0" stroke-width="1.6" marker-end="url(#api-gateway-bff)"/>
  <line x1="495" y1="72" x2="495" y2="92" stroke="#6B3FA0" stroke-width="1.6" marker-end="url(#api-gateway-bff)"/>
  <rect x="60" y="94" width="170" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="145" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Web BFF</text>
  <text x="145" y="127" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">zengin, çok alanlı veri</text>
  <rect x="410" y="94" width="170" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="495" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Mobil BFF</text>
  <text x="495" y="127" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hafif, seçili veri</text>
  <line x1="150" y1="134" x2="300" y2="160" stroke="#186399" stroke-width="1.4"/>
  <line x1="490" y1="134" x2="340" y2="160" stroke="#186399" stroke-width="1.4"/>
  <rect x="270" y="162" width="100" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">iç servisler</text>
</svg>
```

Son olarak güvenlik. Kimlik gateway'de doğrulansa bile, iç servislerin bunu tamamen görmezden gelmesi risklidir. Buna "sert kabuk, yumuşak çekirdek" modeli denir ve üç durumda çöker. Birincisi: bir saldırgan iç ağa sızıp yanlamasına ilerler. Buna **lateral movement (yanal hareket)** denir; saldırgan ilk girdiği makineden diğerlerine atlar. İkincisi: bir yol yanlış yapılandırılır. Üçüncüsü: bir çağrı gateway'i hiç uğramadan atlar.

Doğrusu **katmanlı savunma (defense in depth)** ve **sıfır güven (zero trust)** ilkesidir. Gateway kimliği doğrular ve isteğe **iletilen bir jeton** ekler. İç servisler bu jetonu **kendileri de doğrular** ve yetki kararını kendileri verir. Servis-servis düzeyinde **mTLS**, çağıranın kimliğini kriptografik olarak garanti eder. Yalnız ağ seviyesinde izolasyon (özel ağ, güvenlik duvarı) yeterli değildir. Tekrar doğrulama, jetonun kötüye kullanım riskini artırmaz; imza ve süre kontrolü sayesinde tam tersine azaltır.

### Mülakatta ne sorulur?

- **Gateway sorumlulukları**: yönlendirme, kimlik/yetki, hız sınırlama, TLS, log/izleme, protokol çevirisi. Değil: iş mantığı, veritabanı erişimi, derleme/dağıtım.
- **BFF neden var?** Web ve mobilin veri ihtiyacı farklıdır; tek cephe ikisini de tam memnun edemez.
- **Gateway vs mesh**: kuzey-güney vs doğu-batı; sidecar proxy; ikisi tamamlayıcıdır, mesh gateway'i gereksiz kılmaz.
- **SPOF sorusu**: "Gateway tek nokta arıza değil mi?" Doğru cevap: evet, ama durumsuzluk + yatay ölçek + yük dengeleyici + HA ile azaltılır. Dikey ölçekleme çözüm değildir; kaldırmak hiç değildir.
- **Gateway monoliti**: iş mantığı sızmasının dört sonucu — gizli monolit, bağımsızlık kaybı, dağıtım kilidi, sınır bulanıklığı.
- **Zero trust**: kenarda doğrulama tek başına yetmez; iletilen JWT'nin servislerde de doğrulanması ve mTLS.

### Tek cümlede

API Gateway, dış dünyanın sisteme girdiği tek mantıksal kapıdır; yönlendirmeyi ve kenar politikalarını tek yerde toplar. Ama iş mantığından uzak, durumsuz ve çok örnekli tutulmazsa iki tehlike doğar: hem gizli bir monolite hem de tek nokta arızaya dönüşür.

# gw-1 | Temel
## Soru
API Gateway'in mikroservis mimarisindeki temel rolü nedir?
## A
Tüm dış isteklerin tek giriş kapısı olarak yönlendirme, kimlik ve kota işlerini üstlenmek
🤖 Doğru: gateway istemci ile servisler arasında tek cephedir; ortak işleri kenarda toplar.
## B
Her mikroservisin iş mantığını kendi içinde barındırıp servisler arası kuralları uygulamak
🤖 İş mantığı servislere aittir; gateway'e taşınması bilinen bir anti-desendir.
## C
Servislerin veritabanlarına doğrudan bağlanıp sorguları çalıştırarak veri döndürmek
🤖 Gateway veritabanına erişmez; isteği sahibi servise yönlendirir.
## D
Kaynak kodu derleyip servislerin sürüm ve dağıtım süreçlerini yönetmek
🤖 Bu CI/CD işidir; gateway bir çalışma-zamanı trafik ve politika katmanıdır.
## Doğru
A
## Açıklama
**API Gateway**, çok sayıda mikroservis varken dış istemcilere sunulan **tek giriş kapısıdır**. Değeri, her serviste tekrarlanacak **çapraz kesen işleri** kenarda **tek yerde** toplamaktır: istekleri doğru servise **yönlendirme**, **kimlik doğrulama/yetkilendirme**, **hız sınırlama ve kota**, TLS sonlandırma, loglama, izleme başlıkları, protokol çevirisi ve sınırlı istek toplama. Bu sayede istemci iç topolojiyi bilmez ve her servis güvenlik/kota mantığını yeniden yazmaz. Gateway bir **yönlendirme ve politika** katmanıdır — iş mantığı (B), veritabanı erişimi (C) ve derleme/dağıtım (D) onun sorumluluğu değildir. İş mantığını gateway'e koymak onu gizli bir monolite çevirir ve servislerin bağımsızlığını bozar.

# gw-2 | Temel
## Soru
BFF (Backend for Frontend) deseninin temel fikri nedir?
## A
Tüm istemciler için tek dev bir ortak gateway kurup hiç özelleştirme yapmamak
🤖 BFF tam tersidir: "tek beden herkese" yerine istemciye özel cepheler kurar.
## B
Her istemci türü için ihtiyaçlarına uyarlanmış ayrı bir gateway/cephe sağlamak
🤖 Doğru: BFF her frontend için ona özel bir arka-uç cephesi kurar; veri şekli istemciye göre uyarlanır.
## C
Frontend kodunu backend'e taşıyıp tek bir sunucu uygulamasında birleştirmek
🤖 BFF bir cephe desenidir; frontend ile backend'i tek uygulamada birleştirmez.
## D
Her mikroservise kendi tarayıcı arayüzünü ekleyip ortak istemciyi tamamen kaldırmak
🤖 Bu arayüzü servislere dağıtmaktır; BFF ise istemcinin önünde tek bir cephe sunar.
## Doğru
B
## Açıklama
**BFF (Backend for Frontend)**, API Gateway fikrinin özelleşmiş halidir: tek "herkese uyan" gateway yerine **her istemci türü için ayrı bir cephe** kurulur. Gerekçe: web ile mobilin ihtiyaçları farklıdır — web zengin, çok alanlı veri isterken mobil bant genişliğini korumak için hafif ve seçili veri ister. Tek gateway ikisini de tam memnun edemez (biri fazla veri alır, diğeri eksik). **Web BFF** ve **mobil BFF** ayrı ayrı, kendi istemcisinin ihtiyacına göre veriyi toplar (aggregation) ve şekillendirir. Ek fayda: her BFF'i ilgili frontend ekibi sahiplenir (Conway ile uyumlu), böylece arayüz değişiklikleri diğer istemcileri etkilemeden hızlı ilerler. BFF, frontend'i backend'e birleştirmek (C) ya da arayüzü servislere dağıtmak (D) değildir.

# gw-3 | Orta
## Soru
API Gateway ile Service Mesh arasındaki temel ayrım hangisidir?
## A
API Gateway servis-servis trafiğini, Service Mesh dış istemci trafiğini yönetir
🤖 Eksenler ters yazılmış: gateway dış trafiğin, mesh iç servis-servis trafiğinin katmanıdır.
## B
Service Mesh gateway'in yerini tamamen alır; mesh varsa gateway'e gerek kalmaz
🤖 Mesh iç trafiği yönetir; dış istemcinin gireceği kenar kapısı yine gereklidir.
## C
Gateway kuzey-güney (dış ↔ sistem), mesh doğu-batı (servis ↔ servis) trafiğini yönetir
🤖 Doğru: gateway kenardaki giriş/çıkış cephesidir; mesh iç trafiği sidecar'larla yönetir.
## D
Service Mesh yalnızca REST'i, API Gateway yalnızca gRPC'yi destekler
🤖 İkisi de protokol-agnostiktir; ayrım protokolde değil trafik ekseninde.
## Doğru
C
## Açıklama
Bu ikisi rakip değil, **farklı trafik eksenlerini** yöneten tamamlayıcı katmanlardır. **API Gateway → kuzey-güney (north-south)**: dış dünya ile sistem arasındaki trafiğin **tek cephesidir**; kimlik, kota, yönlendirme gibi kenar politikalarını uygular. **Service Mesh → doğu-batı (east-west)**: sistemin **içindeki servis-servis** trafiğini yönetir. Her servis örneğinin yanına bir **sidecar proxy** (ör. Envoy) konur; bu proxy'ler **mTLS**, yeniden deneme, zaman aşımı, devre kesici, ince trafik yönlendirme (canary/A-B) ve gözlemlenebilirliği **uygulama koduna dokunmadan** sağlar. Eksenleri ters kurmak (A) yaygın bir karışıklıktır. Mesh gateway'i gereksiz de kılmaz (B): iç trafiği ne kadar iyi yönetirse yönetsin, dış istemcinin gireceği bir kapı gerekir. Büyük sistemlerde ikisi **birlikte** kullanılır: gateway kenarda, mesh içeride; ayrım protokol (D) değil, yönetilen trafik eksenidir.

# gw-4 | Orta
## Soru
"API Gateway tek nokta arıza (single point of failure) olabilir" endişesi neden doğrudur ve nasıl azaltılır?
## A
Doğru değildir; gateway düşerse istemciler otomatik olarak servislere doğrudan bağlanır
🤖 Böyle bir otomatik geçiş yoktur; gateway düşerse dış trafik tamamen kesilir.
## B
Doğrudur ama çözüm gateway'i kaldırıp her istemciyi tüm servislere doğrudan bağlamaktır
🤖 Gateway'i kaldırmak kimlik/kota merkezini yok eder ve istemciyi iç topolojiye bağlar.
## C
Doğrudur; çözüm gateway'i tek bir çok güçlü makineye taşıyıp dikey ölçeklemektir
🤖 Dikey ölçekleme kapasiteyi artırır ama tek kopya kaldığı için arıza riskini kaldırmaz.
## D
Doğrudur; gateway durumsuz tutulur, yatay ölçeklenir ve yük dengeleyici arkasında HA kurulur
🤖 Doğru: risk gerçektir; çözüm gateway'i çoğaltıp durumsuz ve yük dengeli kurmaktır.
## Doğru
D
## Açıklama
Tüm dış trafik gateway'den geçtiği için, gateway düşerse **sistem dışarıya tamamen kapanır** — bu onu klasik bir **tek nokta arıza** ve potansiyel darboğaz yapar; istemcilerin kendiliğinden servislere yönelmesi diye bir mekanizma yoktur (A). Endişe gerçektir ama çözüm gateway'i kaldırmak (B) değildir; bu, merkezîleştirdiği tüm değeri (kimlik, kota, yönlendirme) yok eder ve istemcileri iç topolojiye sıkı bağlar. Dikey ölçekleme (C) de yetmez: makineyi güçlendirmek kapasiteyi artırır ama ortada hâlâ **tek bir kopya** vardır, o kopya düştüğünde sistem yine kapanır. Doğru azaltma: gateway'i **durumsuz** tasarla (böylece herhangi bir örnek herhangi bir isteği karşılar), **yatay ölçekle** (birden çok örnek), önüne bir **yük dengeleyici** koy ve **yüksek erişilebilir** (çok bölge, sağlık kontrolü) kurgula. Böylece "tek kapı" mantıksal olarak tek kalır ama fiziksel olarak yedeklidir.

# gw-5 | İleri
## Soru
Bir ekip, birden fazla servisten veri toplayan karmaşık iş kurallarını ve alan mantığını (domain logic) yavaş yavaş API Gateway'in içine yazmaya başladı. Bu neden bir anti-desendir?
## A
Gateway gizli bir monolite dönüşür; servisler bağımsız evrilemez, gateway dağıtım kilidi olur
🤖 Doğru: iş mantığı sızınca "gateway monoliti" oluşur; bağımsızlık ve sınır netliği bozulur.
## B
Gateway tüm servislere eriştiğinden kural tekrarı azalır; tek sakınca ek gecikmedir
🤖 Kural tekrarını azaltıyor görünmesi yanıltıcıdır; asıl bedel gecikme değil sahiplik kaybıdır.
## C
Sorun yalnızca gateway kodunun test edilememesidir; iyi testlerle sakınca ortadan kalkar
🤖 Mesele kodun kalitesi değil, sorumluluğun yanlış katmanda durmasıdır.
## D
Alan mantığı gateway'e taşınınca sistem otomatik hızlanır, bu istenen bir sonuçtur
🤖 Merkezî darboğaz ve bağımlılık doğar; kazanç değil mimari borç birikir.
## Doğru
A
## Açıklama
API Gateway bir **ince politika/yönlendirme katmanı** olarak kalmalıdır. **İş/alan mantığı** oraya taşınmaya başlayınca birkaç sorun birden doğar: (1) gateway giderek **gizli bir monolite** dönüşür — tüm alanların mantığı tek bileşende toplanır; (2) alan mantığı sahibi servisin dışına kaçtığı için o servis **bağımsız evrilemez**; (3) her mantık değişikliği gateway'in yeniden dağıtımını gerektirir, gateway **dağıtım kilidi ve darboğaz** olur; (4) **sorumluluk sınırı bulanır** — "bu kural nerede?" sorusu belirsizleşir ve tek nokta arıza riski büyür. Kod tekrarını azaltıyor gibi görünmesi (B) bu bedelleri karşılamaz ve sorun daha iyi testlerle (C) çözülmez, çünkü mesele kodun kalitesi değil sorumluluğun yeridir. Doğrusu: toplama gerekiyorsa ince tut veya ayrı bir **toplama/BFF servisine** al; iş kuralları **sahibi servisin içinde** kalsın.

# gw-6 | İleri
## Soru
Kimlik doğrulama (authentication) API Gateway'de merkezîleştirilse bile, iç servislerin isteklerin kimliğini ve yetkisini tümüyle görmezden gelmesi neden risklidir (defense in depth açısından)?
## A
İç servisler güvenli ağda çalıştığı için hiçbir doğrulamaya ihtiyaç duymaz
🤖 İç ağa körü körüne güvenmek klasik bir zafiyettir; iç servisler de doğrulama gerektirir.
## B
İstek gateway'i atlayabilir; sıfır güven gereği servisler de kimliği ve yetkiyi doğrulamalıdır
🤖 Doğru: tek kenar katmanına güvenmek kırılgandır; katmanlı savunma iç doğrulamayı gerektirir.
## C
Ağ seviyesinde izolasyon (özel ağ, güvenlik duvarı) yeterlidir; uygulama doğrulaması gereksizdir
🤖 Ağ izolasyonu iyi bir katmandır ama tek başına yetmez; içeri sızan trafik yine doğrulanmalıdır.
## D
Servislerde tekrar doğrulama yapmak jetonun yeniden kullanım (replay) riskini artırır
🤖 Tersine, imza ve geçerlilik süresi kontrolü bu riski azaltır; tekrar doğrulama zafiyet yaratmaz.
## Doğru
B
## Açıklama
Kimliği yalnız gateway'de doğrulayıp iç servisleri **tümüyle güvenilir** saymak, "sert kabuk, yumuşak çekirdek" modelidir ve modern güvenlikte kırılgan kabul edilir. Riskler: iç ağa sızan bir saldırgan (lateral movement), yanlış yapılandırılmış bir yönlendirme veya gateway'i atlayan bir yol, doğrulanmamış isteklerin iç servislere ulaşmasına yol açabilir — bu yüzden "iç ağ güvenli" varsayımı (A) ve yalnız ağ izolasyonuna güvenmek (C) yetersizdir. **Katmanlı savunma** (defense in depth) ve **sıfır güven** (zero trust) ilkesi der ki: her servis kendisine geleni de doğrulamalı. Pratikte gateway kimliği doğrular ve **iletilen bir kimlik jetonu** (propagated JWT) ekler; iç servisler bu jetonu **kendileri de doğrular** ve yetki kararını verir. Tekrar doğrulama bir zafiyet üretmez (D): imza ve geçerlilik süresi kontrolü replay riskini tam tersine azaltır. Servis-servis düzeyinde **mTLS** (service mesh ile) çağıranın kimliğini kriptografik olarak garanti eder; böylece güvenlik tek kenar katmanına değil çok katmana dağılır.
