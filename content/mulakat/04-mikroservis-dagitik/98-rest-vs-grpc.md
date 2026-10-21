---
slug: rest-vs-grpc
baslik: "REST mi gRPC mi: Protokol Takasları, Protobuf/HTTP2, Streaming ve Şema Evrimi"
kademe: Orta
sira: 98
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

İki kişi mektuplaşacak. Birinci yol: her mektubu düz Türkçe, herkesin okuyabileceği biçimde yazmak — herkes anlar, ama zarf kalınlaşır ve okuması zaman alır. İkinci yol: önceden anlaşılmış numaralı bir formu doldurmak — "3 numaralı kutu telefon demektir" — çok kısa ve hızlıdır, ama formu bilmeyen hiçbir şey anlamaz. Yazılımda birinci yol **REST/JSON**, ikinci yol **gRPC/Protobuf**'tur. Bu konu, iki servisin birbiriyle nasıl konuşacağına karar verirken neyi kazanıp neyi kaybettiğini anlatır.

### Temel kavramlar

- **Servis**: bağımsız çalışan, kendisine yalnız ağ üzerinden ulaşılan uygulama parçası.
- **Protokol**: iki tarafın mesajlaşırken uyduğu ortak kural takımı. **HTTP** web'in protokolüdür.
- **HTTP/1.1**: her istek-yanıt çiftinin sırayla işlendiği klasik sürüm. Bağlantı yeniden kullanılabilir (keep-alive) ve gövde sıkıştırılabilir (gzip), ama istekler aynı bağlantıda **sıraya girer**.
- **HTTP/2**: tek bağlantı üzerinde birçok isteğin **eşzamanlı** taşındığı (çoğullama / multiplexing), başlıkların sıkıştırıldığı ve akışın desteklendiği sürüm.
- **Serileştirme**: nesnenin ağdan gönderilebilir baytlara çevrilmesi. **Metin tabanlı** (JSON) insan okur; **ikili (binary)** okunmaz ama küçüktür.
- **JSON**: metin tabanlı, alan adlarını da telde taşıyan yaygın veri biçimi.
- **Protocol Buffers (Protobuf)**: alanların **numaralarla** (field tag) kodlandığı ikili biçim; şeması `.proto` dosyasında yazılır.
- **RPC (uzak yordam çağrısı)**: uzaktaki bir metodu yerel metotmuş gibi çağırma yaklaşımı.
- **Kod üretimi (code generation)**: `.proto` şemasından istemci/sunucu sınıflarının otomatik üretilmesi; sonuç **tip güvenli** koddur.
- **Akış (streaming)**: tek bir çağrı içinde çok sayıda mesajın peş peşe akması.
- **Şema evrimi (schema evolution)**: istemci ve sunucu farklı sürümlerdeyken mesaj biçiminin uyumlu kalması.
- **Geriye/ileriye uyumluluk**: yeni sunucunun eski istemciyi, eski sunucunun yeni istemciyi kırmaması.
- **gRPC-Web**: tarayıcıların gRPC ile konuşabilmesi için araya bir proxy gerektiren uyarlama.
- **CORS**: tarayıcının farklı alan adına istek atmasını düzenleyen güvenlik kuralı — protokol yeteneğiyle ilgisi yoktur.

### Nasıl çalışır?

**REST (HTTP/1.1 + JSON)** kaynak odaklıdır ve insan-okunurdur. Avantajları: her yerde çalışır (tarayıcı, `curl`, her dil), öğrenmesi kolay, keşfedilebilir, ara katmanlar (önbellek, proxy) HTTP semantiğini anlar. Bedelleri: JSON ayrıştırma ve boyut açısından verimsizdir, resmî bir şema zorunlu değildir (OpenAPI opsiyoneldir) ve HTTP/1.1 çift yönlü akış sunmaz.

**gRPC (HTTP/2 + Protobuf)** **sözleşme-önceliklidir (contract-first)**: önce `.proto` dosyasında hizmet ve mesajlar tanımlanır, sonra iki taraf için kod üretilir. Üretilen istemci sınıfına **stub (vekil istemci)** denir; uzaktaki metodu yerel bir metotmuş gibi çağırmanı sağlar. Protobuf'un ikili serileştirmesi JSON'dan belirgin biçimde küçük ve hızlıdır, çünkü telde alan adlarını değil numaraları taşır.

HTTP/2 ise çoğullama, başlık sıkıştırma ve **dört çağrı biçimi** getirir:

- **Unary (tek-tek)**: tek istek, tek yanıt. Klasik çağrı.
- **Server streaming (sunucu akışı)**: tek istek, akıp gelen çok sayıda yanıt.
- **Client streaming (istemci akışı)**: akıp giden çok sayıda istek, tek yanıt.
- **Bidirectional streaming (çift yönlü akış)**: iki tarafın aynı anda akması.

gRPC'nin bedelleri de var. İkili format insan tarafından okunmaz, bu da hata ayıklamayı zorlaştırır. Tarayıcı doğrudan gRPC konuşamaz. Araç ve öğrenme maliyeti yüksektir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REST, gRPC ve GraphQL kullanım yerleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Doğru araç, doğru yer</text>
  <rect x="26" y="42" width="185" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">REST / JSON</text>
  <text x="118" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">HTTP/1.1 · metin</text>
  <text x="118" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ her yerde, okunur</text>
  <text x="118" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ tarayıcı dostu</text>
  <text x="118" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">- verimsiz, akış yok</text>
  <text x="118" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">dış / public API</text>
  <rect x="227" y="42" width="185" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">gRPC / Protobuf</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">HTTP/2 · ikili</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ hızlı, küçük, tipli</text>
  <text x="320" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ streaming (4 biçim)</text>
  <text x="320" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">- okunmaz, tarayıcı zor</text>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">iç servis-servis</text>
  <rect x="428" y="42" width="185" height="150" rx="12" fill="#F4EFE4" stroke="#9A7B2E" stroke-width="1.5"/>
  <text x="520" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#9A7B2E" font-family="system-ui">GraphQL</text>
  <text x="520" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istemci sorgu dili</text>
  <text x="520" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ tam ihtiyaç kadar veri</text>
  <text x="520" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ tek istekte toplama</text>
  <text x="520" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">- önbellek/karmaşıklık</text>
  <text x="520" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#9A7B2E" font-family="system-ui">istemci toplama katmanı</text>
</svg>
```

Dikkat edilecek incelik: HTTP/1.1 de gövde sıkıştırabilir ve bağlantıyı yeniden kullanabilir; gRPC'yi ayıran şey bunlar değil, aynı bağlantıda **eşzamanlı çift yönlü akıştır**.

```svg
<svg viewBox="0 0 640 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="gRPC dört çağrı biçimi: unary, server streaming, client streaming, bidirectional">
  <defs>
    <marker id="rest-vs-grpc-ok" markerWidth="9" markerHeight="9" refX="7.5" refY="3" orient="auto">
      <path d="M0,0 L7.5,3 L0,6 Z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">gRPC / HTTP/2: dört çağrı biçimi</text>
  <rect x="24" y="38" width="290" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="169" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Unary (tek-tek)</text>
  <rect x="42" y="76" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="69" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemci</text>
  <rect x="242" y="76" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="269" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sunucu</text>
  <line x1="98" y1="84" x2="240" y2="84" stroke="#25804C" stroke-width="1.6" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="169" y="80" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">1 istek</text>
  <line x1="240" y1="100" x2="98" y2="100" stroke="#25804C" stroke-width="1.6" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="169" y="116" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">1 yanıt</text>
  <rect x="326" y="38" width="290" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="471" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Server streaming</text>
  <rect x="344" y="76" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="371" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemci</text>
  <rect x="544" y="76" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="571" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sunucu</text>
  <line x1="400" y1="84" x2="542" y2="84" stroke="#25804C" stroke-width="1.6" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="471" y="80" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">1 istek</text>
  <line x1="542" y1="100" x2="400" y2="100" stroke="#25804C" stroke-width="3" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="471" y="116" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">N akış yanıtı</text>
  <rect x="24" y="132" width="290" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="169" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Client streaming</text>
  <rect x="42" y="170" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="69" y="189" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemci</text>
  <rect x="242" y="170" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="269" y="189" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sunucu</text>
  <line x1="98" y1="178" x2="240" y2="178" stroke="#25804C" stroke-width="3" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="169" y="174" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">N akış isteği</text>
  <line x1="240" y1="194" x2="98" y2="194" stroke="#25804C" stroke-width="1.6" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="169" y="210" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">1 yanıt</text>
  <rect x="326" y="132" width="290" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="471" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Bidirectional</text>
  <rect x="344" y="170" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1.1"/>
  <text x="371" y="189" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemci</text>
  <rect x="544" y="170" width="54" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="571" y="189" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sunucu</text>
  <line x1="400" y1="178" x2="542" y2="178" stroke="#25804C" stroke-width="3" marker-end="url(#rest-vs-grpc-ok)"/>
  <line x1="542" y1="194" x2="400" y2="194" stroke="#25804C" stroke-width="3" marker-end="url(#rest-vs-grpc-ok)"/>
  <text x="471" y="212" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">eşzamanlı N ⇄ N akış</text>
</svg>
```

**Şema evrimi** her iki tarafta da kritiktir. Sebebi şu: servisler bağımsız dağıtıldığı için istemci ile sunucu bir süre **farklı sürümlerde** çalışır.

Protobuf bunu tasarımdan destekler. Alanlar numaralıdır. Yeni bir alan eklemek eski istemciyi kırmaz; eski istemci tanımadığı numarayı sessizce yok sayar. Silinen bir numara `reserved` ile işaretlenir ve **asla yeniden kullanılmaz**. Altın kural şudur: **bir alan numarasını asla değiştirme veya yeniden kullanma, yalnız yeni alan ekle.** Alan adlarını kısaltmak mesaj boyutunu değiştirmez, çünkü telde ad değil numara taşınır.

JSON/REST'te aynı disiplin elle korunur. Yeni alan zorunlu değil, isteğe bağlı eklenir. Mevcut bir alanın anlamı veya tipi değiştirilmez. Tüketici, bilmediği alanı yok sayar; buna **hoşgörülü okuyucu (tolerant reader)** denir. Bu disiplin olmazsa tek çare kalır: her değişiklikte her şeyi aynı anda dağıtmak. Buna **kilitli adım (lock-step) dağıtım** denir ve mikroservis bağımsızlığını yok eder.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Protobuf şema evrimi: yalnız yeni alan ekle, numarayı değiştirme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Protobuf şema evrimi: yalnız ekle, numarayı değiştirme</text>
  <rect x="40" y="42" width="240" height="120" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="160" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">.proto mesajı</text>
  <text x="160" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ad = 1</text>
  <text x="160" y="108" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">eposta = 2</text>
  <text x="160" y="128" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">telefon = 3 (yeni)</text>
  <text x="160" y="150" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">silinen no → reserved</text>
  <rect x="360" y="42" width="240" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="480" y="63" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Yeni istemci (v2)</text>
  <text x="480" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">telefon alanını okur</text>
  <rect x="360" y="106" width="240" height="52" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="480" y="127" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">Eski istemci (v1)</text>
  <text x="480" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">no. 3'ü tanımaz, yok sayar</text>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Aynı tel üzerinden farklı sürümler bağımsız dağıtılabilir</text>
  <text x="320" y="202" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Altın kural: alan numarasını asla değiştirme / yeniden kullanma</text>
</svg>
```

**Tarayıcı kısıtı** ayrı bir tuzaktır. gRPC, HTTP/2'nin düşük seviye çerçevelerine dayanır — mesajın sonuna eklenen ek başlıklara (**trailer**) ve akış kontrolüne. Tarayıcıdaki JavaScript bu seviyeye erişemez. Bu yüzden tarayıcı **ham gRPC konuşamaz**. Mesajları JSON'a çevirmek, CORS başlıklarını açmak veya npm'den bir gRPC kütüphanesi kurmak (bu kütüphaneler Node.js içindir) bunu çözmez. İki gerçek çözüm vardır: **gRPC-Web + proxy** (ör. Envoy) ya da kenarda **REST/JSON veya GraphQL cephesi** sunmak.

### Üçüncü seçenek: GraphQL

**GraphQL** üçlüyü tamamlar ve bambaşka bir soruyu cevaplar. REST **kaynak** odaklıdır: hangi adresin ne döndüreceğine sunucu karar verir. gRPC **sözleşme ve performans** odaklıdır; iç servis-servis trafiği için tasarlanmıştır. GraphQL ise **istemcinin istediği alanı seçmesi** içindir — mobil hafif bir alt küme, web zengin bir küme çeker; aşırı ve eksik veri çekme (over-/under-fetching) böylece biter. Bedeli üç kalemdir. Birincisi **N+1 sorgu riski**: her alanı ayrı bir çözücü (resolver) hesaplar, listedeki her satır için veritabanına yeniden gidilir. Bu, JPA'daki N+1 ile aynı hastalıktır (`82`); ilacı da benzerdir, yani toplu yükleme. İkincisi **önbellekleme**: istekler tek adrese gönderildiği için HTTP seviyesinde önbellek devre dışı kalır. Üçüncüsü **sorgu karmaşıklığı**: derinlik ve maliyet sınırı konmazsa tek bir sorgu sunucuyu meşgul eder. Kural sadedir: dışa açık API'de REST, iç yüksek-hacimli trafikte gRPC, çok çeşitli istemciye tek esnek uç sunan **toplama katmanında** GraphQL.

### Zaman aşımı yayılımı (deadline propagation)

Bu, gRPC'nin en az konuşulan ama en gerçek üstünlüklerinden biridir. İstemci çağrıyı başlatırken bir **son tarih (deadline)** belirler. "Bu iş en geç 300 milisaniyede bitsin" der. Son tarih çağrıya iliştirilir ve çağrı zincirinde **aşağıya taşınır**.

Şöyle düşün. Sipariş servisi ödeme servisini, ödeme servisi de kart servisini çağırıyor. İstemci 300 ms bütçe verdi. Ödeme servisine sıra geldiğinde 120 ms harcanmıştır. Ödeme servisi kalanı görür: 180 ms. Kart çağrısını bu kalan süreyle yapar. Bütçe çoktan bitmişse işe **hiç başlamaz** ve anında `DEADLINE_EXCEEDED` döner.

REST'te durum farklıdır. Her atlama kendi zaman aşımını taşır. Sipariş 300 ms, ödeme 300 ms, kart 300 ms yazar. Toplam bütçeyi kimse tutmaz; en kötü hâlde 900 ms beklenir. İstemci çoktan vazgeçmiştir ama sunucular hâlâ çalışır. Bu boşa giden iş, yük altında sistemi daha da yavaşlatır.

Bağı iki yere kurulur. Birincisi dayanıklılık (`101`): süre dolunca çağrı kesilir, iş parçacığı (thread) serbest kalır, devre kesici (circuit breaker) daha erken tepki verir. İkincisi dağıtık izleme (`103`): son tarih de iz bağlamı gibi çağrı başlıklarıyla taşınır. Aynı boru hattı hem izi hem kalan bütçeyi götürür.

Bir şerh düşelim. Son tarih yayılımı sihir değildir. Ara servis kalan süreyi kendi alt çağrısına **aktarmak zorundadır**. gRPC kütüphaneleri bunu kolaylaştırır, ama bağlamı elle bir iş parçacığı havuzuna atarsan zincir kopar. REST'te de aynı fikir bir başlıkla elle taşınabilir; fark, gRPC'de bunun protokolün standart parçası olmasıdır.

### Hata modeli: gRPC durum kodları

gRPC, HTTP durum kodlarını kullanmaz. Kendi **durum kodu (status code)** kümesi vardır. Her yanıtın yanında bir kod ve bir mesaj taşınır. Sık karşılaşılanlar şunlardır:

- `NOT_FOUND`: aranan kayıt yok.
- `INVALID_ARGUMENT`: istemci hatalı veri gönderdi.
- `FAILED_PRECONDITION`: sistemin şu anki durumu bu işleme uygun değil.
- `DEADLINE_EXCEEDED`: süre doldu.
- `UNAVAILABLE`: sunucuya şu an ulaşılamıyor.
- `RESOURCE_EXHAUSTED`: kota veya hız sınırı doldu.

Bu ayrım neden önemli? Çünkü hangi kodun **yeniden denenebilir** olduğu, yeniden deneme (retry) politikasını doğrudan belirler (`101`). `UNAVAILABLE` geçici bir arızadır; yeniden denemek mantıklıdır. `INVALID_ARGUMENT` kalıcıdır; aynı isteği yüz kez göndersen yüz kez aynı hatayı alırsın. `FAILED_PRECONDITION` de denenmez, çünkü önce sistem durumunun değişmesi gerekir.

`DEADLINE_EXCEEDED` en incelikli olanıdır. Sunucu işi bitirmiş, yalnız yanıtı geç yetişmiş olabilir. Bu yüzden yalnız **tekrar edilse de sonucu değişmeyen (idempotent)** çağrılar güvenle yeniden denenir.

REST tarafında aynı bilgiyi HTTP kodları taşır: 404, 400, 409, 503. Ayrım daha kabadır ve anlamı çoğu zaman gövdedeki metne bakar. gRPC'nin kazancı, kodun anlamının **kütüphaneye gömülü** olmasıdır. Birçok gRPC istemcisi, hangi kodda yeniden deneyeceğini bir yapılandırmadan okuyabilir; bu desteğin olgunluğu dile göre değişir.

### Kod üretimi akışı (codegen)

gRPC'de sözleşme koddan önce gelir. `.proto` dosyası tek doğruluk kaynağıdır. Derleyici (`protoc`) o dosyadan iki taraf için kod üretir: sunucu tarafında genişletilecek soyut bir taban sınıf, istemci tarafında **vekil istemci (stub)**.

Java'da akış şudur. `.proto` dosyası projeye konur. Derleme adımında bir Maven/Gradle eklentisi `protoc`'u çağırır. Üretilen sınıflar derlemeye katılır. Sen yalnız taban sınıfı genişletip iş mantığını yazarsın. Üretilen kodu elle düzenlemek yasaktır; bir sonraki derlemede silinir.

Kritik soru şudur: **`.proto` dosyası nerede durur?** Üç yaygın cevap vardır.

1. **Sunucu deposunda**: en basit yol. İstemci ekipler dosyayı kopyalar ve kopyalar zamanla ayrışır.
2. **Paylaşılan sözleşme deposunda**: tüm `.proto` dosyaları ayrı bir depoda toplanır. Oradan **sürümlenmiş bir yapı (artifact)** üretilir. Ekipler koda değil, o yapıya bağımlılık verir. Orta ölçekte en yaygın seçim budur.
3. **Şema kayıt defterinde (schema registry)**: merkezî bir servis şemaları saklar ve uyumluluğu denetler.

Sözleşme-önce çalışmanın pratik anlamı buradadır. Sözleşme değişince çağıran taraf **derlenmez**. Hata üretimde değil, derleme adımında görünür.

Son halka sürekli bütünleştirme hattıdır (`220`). Boru hattına bir **uyumluluk denetimi** adımı eklenir. Değişen `.proto`, bir önceki sürümle karşılaştırılır. Alan numarası değişmiş veya yeniden kullanılmışsa yapı kırılır. Böylece altın kural insan disiplinine değil, makineye emanet edilir.

### Mülakatta ne sorulur?

- **gRPC'nin yığını**: Protobuf (ikili, şemalı) + HTTP/2. "JSON + HTTP/2" değil.
- **Dört çağrı biçimi** ve hangisinin REST/HTTP/1.1'de doğal olmadığı: çift-yönlü akış. Sıkıştırma ve bağlantı yeniden kullanımı HTTP/1.1'de de vardır.
- **Nerede hangisi**: dışa dönük, tarayıcıdan ve her dilden tüketilecek public API'de REST/JSON; iç, yüksek hacimli, düşük gecikmeli veya akışlı trafikte gRPC.
- **Protobuf altın kuralı**: numarayı değiştirme, yeniden kullanma; sil ve `reserved` yap; yalnız ekle.
- **Son tarih yayılımı**: gRPC'de bütçe zincirde aşağı taşınır, ara servis kalan süreyi görür. REST'te her atlama kendi zaman aşımını tutar ve toplam bütçe kaybolur. Bağı `101` ve `103` iledir.
- **Hata modeli**: gRPC durum kodları HTTP kodlarından ayrıdır. Hangisi yeniden denenir? `UNAVAILABLE` evet, `INVALID_ARGUMENT` hayır; `DEADLINE_EXCEEDED` yalnız idempotent çağrıda.
- **Kod üretimi**: `.proto` nerede durur, nasıl sürümlenir, uyumluluk denetimi boru hattına (`220`) nasıl bağlanır.
- **Tarayıcı problemi**: neden ham gRPC olmaz, gRPC-Web/proxy ya da REST kenarı nasıl çözer.
- **GraphQL'in yeri**: over-/under-fetching çözümü, BFF benzeri toplama katmanı; bedeli N+1 (`82`), önbellek ve sorgu maliyeti.
- **Tuzak**: "gRPC her zaman daha iyidir" — hata ayıklama zorluğu, araç maliyeti ve tarayıcı kısıtı takasa dâhildir.

### Tek cümlede

REST/JSON erişilebilirlik ve okunabilirlik satar; gRPC/Protobuf ise hız, tip güvenliği ve akış satar. 2026'nın tipik mimarisi ikisini birden kullanır: kenarda REST, içeride gRPC. Hangisi seçilirse seçilsin, şema evrimi ilk günden disipline edilir.

# grpc-1 | Temel
## Soru
gRPC, verileri serileştirmek ve taşımak için varsayılan olarak hangi teknolojileri kullanır?
## A
XML ve SOAP (metin tabanlı, önceki kuşak web servis yığını)
🤖 Bunlar eski web servis teknolojileridir; gRPC bunları kullanmaz.
## B
JSON ve HTTP/1.1 (klasik REST yığını, metin tabanlı)
🤖 Bu klasik REST yığınıdır; gRPC ikili Protobuf ve HTTP/2 kullanır.
## C
Protocol Buffers (ikili) ve HTTP/2
🤖 Doğru: gRPC, Protobuf ikili biçimini HTTP/2 taşıması üzerinde kullanır.
## D
JSON ve HTTP/2 (metin tabanlı, çoğullamalı taşıma)
🤖 Taşıma doğru ama serileştirme yanlış: gRPC varsayılan olarak JSON değil, ikili Protobuf kullanır.
## Doğru
C
## Açıklama
gRPC iki temel teknoloji üzerine kuruludur: (1) **Protocol Buffers (Protobuf)** — mesajların `.proto` dosyasında tanımlandığı, **ikili** ve şemalı bir serileştirme biçimi; JSON'a göre çok daha küçük ve hızlıdır çünkü alan adlarını metin olarak taşımaz, numaralı etiketler kullanır. (2) **HTTP/2** — çoğullama (tek bağlantıda eşzamanlı istekler), başlık sıkıştırma (HPACK) ve **çift yönlü akış** sağlayan taşıma katmanı. D şıkkı bu yüzden yarı doğrudur ve tipik bir tuzaktır: taşıma gerçekten HTTP/2'dir, ama serileştirme JSON değil Protobuf'tur. REST'in tipik yığını (JSON + HTTP/1.1) insan-okunur ama daha yavaş ve akış yeteneği sınırlıdır; XML/SOAP ise gRPC'nin yerini aldığı önceki kuşak teknolojilerdir.

# grpc-2 | Temel
## Soru
Aşağıdaki senaryoların hangisinde REST/JSON, gRPC'ye göre daha uygun bir tercihtir?
## A
İki iç mikroservis arasında saniyede on binlerce düşük-gecikmeli çağrı
🤖 Yüksek hacim ve düşük gecikme gRPC'nin güçlü olduğu alandır; REST burada verimsiz kalır.
## B
Sürekli çift-yönlü veri akışı gereken gerçek-zamanlı bir sohbet bağlantısı
🤖 Çift-yönlü akış HTTP/2 ile gRPC'nin işidir; klasik REST bunu doğal desteklemez.
## C
Büyük ikili görüntü verisinin en düşük bant genişliğiyle aktarılması
🤖 İkili verimlilik gerektiren bu durumda Protobuf/gRPC avantajlıdır.
## D
Üçüncü parti geliştiricilerin tarayıcıdan tüketeceği herkese açık bir API
🤖 Doğru: geniş erişilebilirlik ve tarayıcı uyumu gereken public API'de REST/JSON en uygunudur.
## Doğru
D
## Açıklama
Protokol seçimi kullanım yerine bağlıdır. gRPC; yüksek hacim, düşük gecikme, ikili verimlilik ve akış (A, B, C) gerektiren **iç servis-servis** iletişiminde üstündür. REST/JSON ise **dışa dönük, herkese açık API**'lerde (D) en iyi seçimdir çünkü: tarayıcılar gRPC'yi doğrudan konuşamaz (gRPC-Web/proxy gerekir), JSON her dilde ve `curl` gibi araçlarla kolayca tüketilir, HTTP semantiği (durum kodları, önbellek) yaygın olarak anlaşılır ve OpenAPI ile keşfedilebilir dokümantasyon üretilebilir. Üçüncü parti entegrasyonda **erişim kolaylığı ve tanıdıklık**, gRPC'nin performans avantajından daha değerlidir. Bu yüzden tipik mimari kenarda REST, içeride gRPC kullanır.

# grpc-3 | Orta
## Soru
gRPC'nin sunduğu ve klasik REST/HTTP/1.1'in doğal olarak sunmadığı iletişim biçimi hangisidir?
## A
Çift-yönlü akış: istemci ve sunucu tek bağlantıda eşzamanlı olarak mesajlaşabilir
🤖 Doğru: HTTP/2 sayesinde gelen bu biçim klasik REST/HTTP/1.1'de doğal olarak yoktur.
## B
Tek bir istek gönderip tek bir yanıt almak (unary çağrı)
🤖 Tek istek-tek yanıt zaten REST'in temel modelidir; ayırt edici bir özellik değildir.
## C
İstek ve yanıt gövdesini sıkıştırarak ağ üzerinde daha az bayt göndermek
🤖 HTTP/1.1 de gzip ile gövde sıkıştırır; bu gRPC'ye özgü bir yetenek değildir.
## D
Aynı bağlantıyı kapatmadan birden çok isteği sırayla göndermek
🤖 Bu HTTP/1.1'in keep-alive özelliğidir; gRPC'nin farkı eşzamanlılık ve akıştır.
## Doğru
A
## Açıklama
gRPC, HTTP/2 taşıması sayesinde **dört çağrı biçimi** sunar: (1) **unary** (tek istek-tek yanıt, REST'e benzer), (2) **server streaming** (tek istek, sunucudan mesaj akışı), (3) **client streaming** (istemciden mesaj akışı, tek yanıt) ve (4) **bidirectional streaming** (çift-yönlü akış). Bunlardan **çift-yönlü akış** klasik REST/HTTP/1.1'in doğal olarak sağlayamadığıdır: istemci ve sunucu **aynı bağlantı üzerinden eşzamanlı** olarak mesaj gönderip alabilir — canlı sohbet, telemetri, gerçek-zamanlı senkronizasyon için idealdir. REST'te benzeri ancak SSE, WebSocket veya uzun yoklama gibi ek mekanizmalarla, protokolün dışında elle kurulur. Diğer şıklar HTTP/1.1'de zaten vardır: unary çağrı REST'in temel modelidir (B), gövde sıkıştırma gzip ile yapılır (C), bağlantıyı yeniden kullanıp istekleri sırayla göndermek ise keep-alive'dır (D) — HTTP/2'nin farkı bunları **eşzamanlı** yapabilmesidir.

# grpc-4 | Orta
## Soru
Protobuf ile şema evriminde (schema evolution) geriye-uyumluluğu korumanın altın kuralı nedir?
## A
Her yeni sürümde alan numaralarını baştan sıralayıp düzenli hale getirmek
🤖 Numaraları yeniden düzenlemek eski istemcilerin okuduğu her alanı bozar.
## B
Alan numarasını değiştirmemek ve yeniden kullanmamak; yalnız yeni alan eklemek
🤖 Doğru: uyumluluk numaralara dayanır; silinen numara rezerve edilir, yalnız ekleme yapılır.
## C
Alan adlarını her sürümde kısaltarak telde taşınan mesaj boyutunu küçültmek
🤖 Protobuf telde ad değil numara taşır; ad kısaltmak boyutu değiştirmez.
## D
Her değişiklikte tüm istemci ve sunucuları aynı anda yeniden dağıtmak
🤖 Bu lock-step dağıtımdır; şema evriminin amacı tam da buna gerek bırakmamaktır.
## Doğru
B
## Açıklama
Protobuf'un geriye-uyumluluğu tümüyle **alan numaralarına** (field tags) dayanır — mesajlar telde alan adıyla değil **numarayla** kodlanır. Bu yüzden altın kural: **bir alan numarasını asla değiştirme veya yeniden kullanma; yalnızca yeni alanı yeni bir numarayla ekle.** Yeni eklenen alanı eski istemci **tanımaz ve sessizce yok sayar**; eski alanı yeni istemci varsayılan değeriyle okur. Bir alan kaldırılırsa numarası `reserved` ile işaretlenir ki gelecekte yanlışlıkla farklı bir anlamla yeniden kullanılmasın. Numaraları yeniden sıralamak (A) bu sözleşmeyi tamamen bozar. Adları kısaltmak (C) boyutu etkilemez, çünkü ad zaten telde taşınmaz. Bu disiplin, istemci ve sunucunun **farklı sürümlerde bağımsız dağıtılmasını** mümkün kılar; D (lock-step dağıtım) ise tam da şema evriminin ortadan kaldırmaya çalıştığı bağımlılıktır.

# grpc-5 | İleri
## Soru
Ekip, tarayıcı tabanlı bir web uygulamasının doğrudan bir gRPC servisiyle konuşmasını istiyor ama çağrılar başarısız oluyor. En olası neden ve çözüm nedir?
## A
Protobuf tarayıcıda çözülemez; mesajları JSON'a çevirmek sorunu tek başına çözer
🤖 Sorun serileştirme değil taşımadır; JSON'a çevirmek tarayıcının gRPC konuşamamasını gidermez.
## B
Sorun CORS kaynaklıdır; sunucuda CORS başlıklarını açmak yeterli olur
🤖 CORS ayrı bir konudur; başlıklar açılsa da tarayıcı ham gRPC çerçevelerini kuramaz.
## C
Tarayıcı ham gRPC konuşamaz; gRPC-Web proxy'si ya da kenarda REST cephesi gerekir
🤖 Doğru: gRPC düşük seviye HTTP/2 çerçevelerine dayanır; tarayıcı bunlara erişemez.
## D
Tarayıcıya npm'den gRPC istemci kütüphanesi kurmak yeterlidir, proxy gerekmez
🤖 O kütüphaneler Node.js içindir; tarayıcı ortamında ham gRPC yine kurulamaz.
## Doğru
C
## Açıklama
Sorun **tarayıcı kısıtıdır**: gRPC, JavaScript'in tarayıcıda erişemediği **düşük seviye HTTP/2 çerçeveleri** üzerinde çalışır (trailer'lar, akış kontrolü vb.). Bu yüzden bir tarayıcı **ham gRPC** konuşamaz — bu bir serileştirme (A), güvenlik başlığı (B) veya kütüphane kurulumu (D) meselesi değildir; npm'deki gRPC istemcileri Node.js çalışma zamanı içindir, tarayıcı için değil. İki yaygın çözüm vardır: (1) **gRPC-Web** kullanmak — tarayıcı gRPC-Web protokolünü konuşur, araya bir **proxy** (ör. Envoy) veya uyumlu bir sunucu konur ve bu gerçek gRPC'ye çevrilir; (2) kenarda (edge) bir **REST/JSON veya GraphQL cephesi** (API Gateway / BFF) sunup iç servis-servis iletişimini gRPC olarak tutmak. Bu, "kenarda REST, içeride gRPC" tipik mimarisinin de gerekçesidir.

# grpc-6 | İleri
## Soru
Bir mimari, dış istemcilere (web + mobil) tek bir esnek uç nokta üzerinden tam ihtiyaçları kadar veri sunmak istiyor; farklı istemciler farklı alan kümelerine ihtiyaç duyuyor ve aşırı/eksik veri çekme (over-/under-fetching) sorun oluyor. Hangi teknoloji bu spesifik sorunu en doğrudan çözer?
## A
Servisler arası tüm iletişimi gRPC'ye çevirip ikili serileştirmeye geçmek
🤖 gRPC iç verimliliği artırır ama istemcinin alan-bazlı esnek sorgu ihtiyacını çözmez.
## B
Her istemci türü için ayrı REST uç noktaları çoğaltıp elle bakımını yapmak
🤖 Uç nokta patlaması ve bakım yükü getirir; esnek sorgu ihtiyacını yapısal çözmez.
## C
Tüm veriyi tek büyük JSON olarak gönderip filtrelemeyi istemciye bırakmak
🤖 Bu aşırı-veri çekmeyi kötüleştirir; özellikle mobilde bant genişliği israfıdır.
## D
GraphQL: istemci tek istekte tam ihtiyaç duyduğu alanları sorgular
🤖 Doğru: GraphQL istemci-yönlü alan seçimi ve tek istekte toplama için tasarlanmıştır.
## Doğru
D
## Açıklama
Anlatılan sorun — **çeşitli istemcilerin farklı alan kümelerine ihtiyacı** ve **over-/under-fetching** — tam olarak **GraphQL'in** çözmek için tasarlandığı problemdir. GraphQL'de istemci, sunucunun sabit yanıt şeklini değil **tam istediği alanları** bir sorgu diliyle belirtir ve ilişkili verileri **tek istekte** toplar. Böylece mobil hafif bir alt küme, web zengin bir küme çekebilir; ne fazla ne eksik veri gider. gRPC (A) iç verimlilik içindir ama istemci-yönlü esnek sorguyu çözmez; uç nokta çoğaltma (B) ölçeklenmez ve bakım yükü getirir; her şeyi gönderip istemcide filtrelemek (C) over-fetching'i kötüleştirir. Not: GraphQL'in bedeli vardır — HTTP önbellekleme zorlaşır, karmaşık sorgular sunucuda maliyetli olabilir (N+1 riski, sorgu derinliği ve karmaşıklık sınırı gerekir). Bu yüzden GraphQL genelde **istemci-yönlü toplama katmanında** (BFF benzeri) kullanılır, iç yüksek-hacimli servis-servis iletişiminde değil.
