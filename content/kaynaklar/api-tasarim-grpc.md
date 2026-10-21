---
baslik: "API Tasarımı: REST ve gRPC"
yazar: "Serpiştirme — Mikroservis yanı"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 6
---

### Bu metin ne anlatıyor?
İki program birbirine "şunu yap", "şu bilgiyi ver" derken ortak bir konuşma biçimi seçmek
zorundadır. Bu metin, bugün en çok kullanılan iki konuşma biçimini anlatıyor: **REST** ve
**gRPC**. Önce ikisinin nasıl çalıştığını, sonra hangi durumda hangisinin işe yaradığını
göreceksin. Amaç birini "iyi", ötekini "kötü" ilan etmek değil; hangi işte hangisinin daha
az sorun çıkardığını görmek.

### Bu metinde geçen temel terimler
- **API** (Application Programming Interface, uygulama programlama arayüzü): Bir programın
  dışarıya açtığı kapı. "Bana şunu şu biçimde sor, sana şunu şu biçimde veririm" listesi.
- **İstemci** (client): İsteği gönderen taraf (tarayıcı, telefon uygulaması, başka bir servis).
- **Sunucu** (server): İsteği karşılayıp yanıt üreten taraf.
- **Sözleşme** (contract): İki tarafın uyacağı yazılı kural — hangi çağrılar var, hangi
  alanlar zorunlu, yanıt neye benzer.
- **Protokol** (protocol): Mesajların ağ üzerinde hangi kurallarla taşındığı. Burada geçen
  protokol HTTP'dir (web'in taşıma dili) ve iki sürümü konu olur: HTTP/1.1 ve HTTP/2.

## Neden bu özet
Mikroservisler birbirlerinin içini bilmez; yalnızca **sözleşme** (contract) üzerinden
konuşur. Bir servis diğerine "şu adrese, şu biçimde iste, şu yanıtı al" der. Bu
sözleşmeyi tasarlamanın iki baskın yolu var: **REST** (kaynak ve HTTP fiilleri
üzerinden) ve **gRPC** (protobuf sözleşmesi üzerinden ikili çağrı). İkisi rakip değil,
farklı işlere iyi gelen iki araçtır.

## Türkçe çalışma metni

### 1. REST kaynak modeli
REST'in kalbi şudur: sistemi **kaynaklar** (resource) olarak modelle — kullanıcı,
sipariş, ödeme. Her kaynağın bir adresi (URL) vardır: `/kullanicilar/42`. Kaynağa
ne yapmak istediğini ise **HTTP fiili** söyler; kaynak isim, fiil eylemdir.

- **GET** — oku (kaynağı hiç değiştirmez).
- **POST** — yeni oluştur (koleksiyona ekler).
- **PUT** — var olanı baştan yaz / güncelle (kaydın tamamını gönderirsin).
- **PATCH** — var olanın yalnızca birkaç alanını güncelle (kaydın tamamını göndermezsin).
- **DELETE** — sil.

Sunucu her isteğe bir **durum kodu** (status code) ile yanıt verir; bu, "ne oldu"yu
tek bakışta anlatan ortak dildir: `200` tamam, `201` oluşturuldu, `204` başarılı ama
gövdede dönecek bir şey yok (silme yanıtlarının tipik kodu), `400` istek hatalı,
`404` bulunamadı, `500` sunucu hatası.

Aşağıdaki diyagram tek bir kaynağa (`/kullanicilar/{id}`) uygulanan fiilleri ve
tipik durum kodlarını gösterir.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REST kaynak modeli: kullanicilar kaynağına uygulanan HTTP fiilleri ve durum kodları">
  <defs>
    <marker id="api-ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">REST — kaynak isimdir, HTTP fiili eylemdir</text>
  <rect x="220" y="40" width="200" height="46" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="62" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">/kullanicilar/{id}</text>
  <text x="320" y="79" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaynak (resource)</text>
  <g font-family="system-ui">
    <line x1="290" y1="86" x2="120" y2="120" stroke="#186399" stroke-width="1.5" marker-end="url(#api-ok1)"/>
    <line x1="310" y1="86" x2="270" y2="120" stroke="#186399" stroke-width="1.5" marker-end="url(#api-ok1)"/>
    <line x1="330" y1="86" x2="420" y2="120" stroke="#186399" stroke-width="1.5" marker-end="url(#api-ok1)"/>
    <line x1="350" y1="86" x2="560" y2="120" stroke="#186399" stroke-width="1.5" marker-end="url(#api-ok1)"/>
    <rect x="24" y="124" width="132" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
    <text x="90" y="146" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C">GET</text>
    <text x="90" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247">oku</text>
    <text x="90" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789">→ 200</text>
    <rect x="188" y="124" width="132" height="60" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.3"/>
    <text x="254" y="146" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5">POST</text>
    <text x="254" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247">oluştur</text>
    <text x="254" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789">→ 201</text>
    <rect x="352" y="124" width="132" height="60" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.3"/>
    <text x="418" y="146" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17">PUT</text>
    <text x="418" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247">güncelle</text>
    <text x="418" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789">→ 200</text>
    <rect x="516" y="124" width="108" height="60" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
    <text x="570" y="146" text-anchor="middle" font-size="13" font-weight="700" fill="#186399">DELETE</text>
    <text x="570" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247">sil</text>
    <text x="570" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789">→ 204</text>
  </g>
  <text x="320" y="216" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Durum kodları: 2xx başarı · 4xx istemci hatası · 5xx sunucu hatası</text>
  <text x="320" y="234" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">404 bulunamadı · 400 geçersiz istek · 500 sunucu içi hata</text>
</svg>
```

### 2. İyi bir REST nasıl olur
Fiil ve durum kodu bilmek yetmez; olgun bir REST API birkaç alışkanlığı gözetir:

- **Kaynak-merkezli tasarım.** URL'ler isim olmalı, fiil değil. `/kullanicilar/42`
  doğru; `/kullaniciSil?id=42` yanlış — eylem zaten HTTP fiilinde saklı. İç içe
  ilişkiler adreste okunur: `/kullanicilar/42/siparisler`.
- **İdempotentlik** (idempotency). Bir isteği yanlışlıkla iki kez göndermek zarar
  vermemeli. GET, PUT ve DELETE **idempotent**tir: aynı isteği N kez yapmak, bir kez
  yapmakla aynı sonucu bırakır. POST idempotent değildir (iki POST = iki kayıt);
  bu yüzden ağ koptuğunda güvenle tekrar denemek için ek önlem (ör. idempotency
  anahtarı) gerekir.
- **Versiyonlama** (versioning). Sözleşme zamanla değişir; eski istemcileri kırmamak
  için sürüm açıkça taşınır: `/v1/kullanicilar` → `/v2/kullanicilar`. Böylece yeni
  sürüm yayınlanırken eski istemciler çalışmaya devam eder.
- **Sayfalama** (pagination). "Tüm kullanıcıları getir" milyon kayıtta çöker. Yanıt
  parçalara bölünür: `/kullanicilar?sayfa=3&adet=50`. İstemci parça parça ilerler,
  sunucu bellek ve bant genişliğini korur.
- **Yazılı sözleşme.** REST'te sözleşme protokolün kendisinde saklı değildir; ayrı bir
  belgeyle yazılır. Yaygın biçim **OpenAPI**'dir (eski adıyla Swagger): hangi adres, hangi
  fiil, hangi alanlar, hangi yanıt — makine okunur bir dosyada durur. gRPC'nin `.proto`
  dosyasından farkı: OpenAPI **zorunlu değildir**, yazılmazsa API yine çalışır (bu yüzden
  REST'in sözleşmesine "gevşek" denir).

### 3. gRPC — sözleşme-önce, ikili, hızlı
gRPC farklı bir felsefeden gelir. Önce **sözleşme yazılır**: `.proto` dosyasında
(protobuf, Protocol Buffers) hangi çağrıların (metotların) olduğu, hangi girdi ve
çıktı mesajlarını aldığı tanımlanır. Bu tanımdan hem istemci hem sunucu kodu
**otomatik üretilir** — iki taraf da aynı sözleşmeye uymak zorundadır.

- **Sözleşme-önce** (contract-first): `.proto` tek gerçek kaynaktır; koddan önce gelir.
- **İkili biçim** (binary): mesajlar JSON gibi metin değil, sıkı ikili paket olarak
  gider. İnsan okuyamaz ama küçüktür ve hızlı çözülür.
- **Tip güvenliği** (type safety): alanın türü sözleşmede yazar (sayı mı, metin mi).
  Yanlış türü göndermek, ağa çıkmadan derleme sırasında hata verir.

#### 3.1. Taşıma katmanı: HTTP/2 ne getiriyor
gRPC her zaman **HTTP/2** üzerinde çalışır. HTTP/2, web'in taşıma dilinin yeni sürümüdür
ve üç şey kazandırır:

- **Çoğullama** (multiplexing): Tek bir ağ bağlantısında birden çok istek-yanıt aynı anda
  akar. HTTP/1.1'de her bağlantı sırayla çalışırdı; öndeki yavaş istek arkadakileri
  bekletirdi (buna "baş engelleme", head-of-line blocking denir). HTTP/2 bunu ortadan
  kaldırır.
- **Başlık sıkıştırma** (header compression): Her istekte tekrarlanan başlık bilgileri
  (kimlik jetonu, içerik türü…) sıkıştırılarak gönderilir; tekrar eden veri tasarrufu.
- **İki yönlü akış**: Bağlantı açık kalır ve iki taraf da istediği an mesaj gönderebilir.
  gRPC'nin akış özelliği doğrudan buna dayanır.

#### 3.2. Dört çağrı biçimi
gRPC'de "bir istek → bir yanıt" tek seçenek değildir. Dört biçim vardır:

1. **Tekil çağrı** (unary): Bir istek, bir yanıt. Klasik fonksiyon çağrısı gibi.
   Örnek: `KullaniciGetir(id) → Kullanici`.
2. **Sunucu akışı** (server streaming): Bir istek, **çok** yanıt. İstemci bir kez sorar,
   sunucu peş peşe mesaj gönderir. Örnek: "şu siparişin tüm durum güncellemelerini bana
   akıt".
3. **İstemci akışı** (client streaming): **Çok** istek, bir yanıt. İstemci peş peşe mesaj
   gönderir, sunucu en sonda tek yanıt verir. Örnek: telefonun konum ölçümlerini sürekli
   yollaması, sunucunun sonunda tek bir özet dönmesi.
4. **Çift yönlü akış** (bidirectional streaming): Çok istek, çok yanıt; ikisi birbirini
   beklemez. Örnek: canlı sohbet, oyun durumu senkronizasyonu.

REST'te doğal karşılığı yalnızca 1. biçimdir; diğer üçü için ek teknolojiler (WebSocket,
sunucu-gönderimli olaylar) devreye sokman gerekir.

#### 3.3. Alan numarası ve şema evrimi
Protobuf mesajında her alanın bir **alan numarası** (field tag) vardır:

```
message Kullanici {
  string ad   = 1;
  string mail = 2;
  int32  yas  = 3;
}
```

Ağda giden ikili pakette alanın **adı taşınmaz** — yalnızca numarası taşınır. Bu, hem
paketi küçültür hem de sözleşmenin zamanla güvenle değişmesini (**şema evrimi**, schema
evolution) mümkün kılar. Kuralları şunlardır:

- **Yeni alan eklemek güvenlidir**: yeni ve kullanılmamış bir numara ver. Eski istemci o
  numarayı tanımaz, sessizce yok sayar; çalışmaya devam eder. Buna **geriye dönük uyum**
  (backward compatibility) denir.
- **Alanın numarasını değiştirmek yıkıcıdır**: karşı taraf `2` numarayı hâlâ "mail" sanır,
  sen başka bir şey gönderirsin. Sessiz veri bozulması olur — hata bile vermez.
- **Silinen numarayı asla yeniden kullanma**: alan kaldırıldığında numarası `reserved`
  (ayrılmış) ilan edilir; kimse o numarayı bir daha kullanamaz.
- **Alanın adını değiştirmek** ikili biçimde sorun yaratmaz (ad taşınmıyor), ama üretilen
  kodu ve JSON karşılığını etkiler.

Bu yüzden gRPC'de sürümleme, REST'teki `/v1` → `/v2` gibi adres değiştirmekle değil, çoğu
zaman **aynı sözleşmeyi uyumlu biçimde büyüterek** yapılır. Uyumu kıran bir değişiklik
gerekiyorsa yeni bir servis sürümü tanımlanır.

#### 3.4. Tarayıcı kısıtı ve grpc-web
gRPC'nin en bilinen sınırı şudur: **tarayıcı bir gRPC çağrısını doğrudan yapamaz.** Sebep,
tarayıcıdaki JavaScript'in HTTP/2 çerçevelerine (frame) o düzeyde erişememesidir; gRPC'nin
akış ve başlık davranışı tarayıcı ağ arayüzünün verdiğinden fazlasını ister.

Çözüm **grpc-web**'dir: tarayıcı, gRPC'nin tarayıcı-uyumlu bir varyantını konuşur; araya
konan bir **vekil** (proxy, ör. Envoy) bu çağrıyı gerçek gRPC'ye çevirip iç servise iletir.
Bedeli var: grpc-web bu yazının yazıldığı sürümlerde **istemci akışını ve çift yönlü akışı
desteklemez** — yalnızca tekil çağrı ve sunucu akışı çalışır. Yani tarayıcı tarafında
gRPC'nin dört biçiminden ikisini kaybedersin.

Diğer bedeller: ikili biçim gözle okunmaz — `curl` (komut satırından HTTP isteği atan
yaygın araç) ile bakınca anlamlı bir metin görmezsin, ayrı araçlar (`grpcurl`) gerekir;
sözleşme değişince kod yeniden üretilir. Kazancı: az bant genişliği, düşük gecikme,
tip güvenliği.

### 4. REST vs gRPC — hangisi ne zaman
Kural basit: **kim seninle konuşuyor?**

- **Dışarısı / tarayıcı / üçüncü taraf → REST.** Herkesin bildiği, metinle okunan,
  tarayıcının ve her dilin doğrudan desteklediği ortak dil. Açık (public) API'ler,
  web ön yüzü, iş ortağı entegrasyonları burada.
- **İç servis-servis / düşük gecikme / yüksek hacim → gRPC.** Senin kontrolündeki
  mikroservisler kendi aralarında konuşurken hız, küçük paket ve sıkı tip sözleşmesi
  kazandırır; insanın gözle okuması gerekmez.
- **Tarayıcıdan gRPC'ye mecbursan → grpc-web + vekil.** Bu bir kaçış yolu, varsayılan
  değil: ek bir bileşen (proxy) kurmayı ve iki akış biçimini kaybetmeyi göze alıyorsun
  demektir (§3.4).
- **Uzun süreli, iki yönlü canlı bağlantı → gRPC akışı.** Canlı konum, telemetri, canlı
  log gibi işlerde REST'in istek-yanıt kalıbı doğal durmaz.

Aşağıdaki diyagram iki yaklaşımın somut farklarını yan yana koyar. Diyagramda REST'in
taşıması "HTTP/1.1" yazıyor; bu **tipik durumu** anlatır, zorunluluğu değil. REST bugün
HTTP/2 (hatta HTTP/3) üzerinden de servis edilebilir — fark, gRPC'nin HTTP/2'yi *şart*
koşması, REST'in ise hangi HTTP sürümüyle taşındığını umursamamasıdır.

```svg
<svg viewBox="0 0 640 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REST ve gRPC karşılaştırması: biçim, taşıma, okunabilirlik ve tipik kullanım">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">REST — gRPC: yan yana</text>
  <rect x="30" y="42" width="278" height="222" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="15" font-weight="700" fill="#186399" font-family="system-ui">REST</text>
  <g font-family="system-ui" font-size="12" fill="#1C3247">
    <text x="169" y="104" text-anchor="middle">Biçim: JSON / metin</text>
    <text x="169" y="132" text-anchor="middle">Taşıma: HTTP/1.1</text>
    <text x="169" y="160" text-anchor="middle">İnsan okur (readable)</text>
    <text x="169" y="188" text-anchor="middle">Sözleşme: gevşek (OpenAPI)</text>
  </g>
  <rect x="54" y="208" width="230" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="169" y="228" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">Tipik: tarayıcı · açık API</text>
  <text x="169" y="243" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dış istemci ile konuşma</text>
  <rect x="332" y="42" width="278" height="222" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="15" font-weight="700" fill="#4F46E5" font-family="system-ui">gRPC</text>
  <g font-family="system-ui" font-size="12" fill="#1C3247">
    <text x="471" y="104" text-anchor="middle">Biçim: protobuf / ikili</text>
    <text x="471" y="132" text-anchor="middle">Taşıma: HTTP/2 (+ akış)</text>
    <text x="471" y="160" text-anchor="middle">Hızlı · küçük paket</text>
    <text x="471" y="188" text-anchor="middle">Sözleşme: sıkı (.proto)</text>
  </g>
  <rect x="356" y="208" width="230" height="42" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.2"/>
  <text x="471" y="228" text-anchor="middle" font-size="11.5" font-weight="600" fill="#C77A17" font-family="system-ui">Tipik: servis-servis · iç ağ</text>
  <text x="471" y="243" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düşük gecikme · yüksek hacim</text>
</svg>
```

Pratikte ikisi bir arada yaşar: dünyaya açılan kapı REST konuşur, kapının arkasındaki
iç servisler kendi aralarında gRPC ile hızlanır. Aşağıdaki diyagram bu tipik yerleşimi
gösterir: dış istemci **API Ağ Geçidi**ne (gateway) REST/JSON ile gelir; ağ geçidi
isteği içeride gRPC ile mikroservislere dağıtır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Servis-servis yerleşim: dış istemci REST ile ağ geçidine, ağ geçidi gRPC ile iç mikroservislere bağlanır">
  <defs>
    <marker id="api-ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="api-ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#4F46E5"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dışarıda REST, içeride gRPC</text>
  <rect x="20" y="78" width="150" height="60" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="104" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Dış istemci</text>
  <text x="95" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tarayıcı · mobil</text>
  <rect x="245" y="70" width="150" height="76" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="100" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">API Ağ Geçidi</text>
  <text x="320" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(gateway)</text>
  <text x="320" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çeviri noktası</text>
  <rect x="470" y="44" width="150" height="52" rx="11" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="545" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Kullanıcı servisi</text>
  <text x="545" y="84" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iç mikroservis</text>
  <rect x="470" y="120" width="150" height="52" rx="11" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="545" y="144" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Sipariş servisi</text>
  <text x="545" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iç mikroservis</text>
  <line x1="170" y1="108" x2="241" y2="108" stroke="#186399" stroke-width="1.8" marker-end="url(#api-ok2)"/>
  <text x="205" y="98" text-anchor="middle" font-size="10" font-weight="600" fill="#186399" font-family="system-ui">REST/JSON</text>
  <line x1="395" y1="98" x2="466" y2="72" stroke="#4F46E5" stroke-width="1.8" marker-end="url(#api-ok3)"/>
  <line x1="395" y1="118" x2="466" y2="142" stroke="#4F46E5" stroke-width="1.8" marker-end="url(#api-ok3)"/>
  <text x="430" y="112" text-anchor="middle" font-size="10" font-weight="600" fill="#4F46E5" font-family="system-ui">gRPC</text>
  <text x="320" y="196" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ağ geçidi dış dünyanın dilini (REST) iç dünyanın diline (gRPC) çevirir</text>
</svg>
```

## Öz-sınav (kendini test et)
1. `/kullanicilar/42` adresine hangi HTTP fiili "bu kullanıcıyı sil" demektir ve
   tipik hangi durum kodunu bekleriz?
2. GET, PUT ve DELETE'i "idempotent" yapan nedir; POST neden bu gruba girmez?
3. Bir REST API'de neden `/v1/...` ve `/v2/...` gibi sürüm taşırız — hangi sorunu
   çözer?
4. gRPC'de "sözleşme-önce" ne demek; `.proto` dosyasının rolü nedir?
5. Dışa açık web ön yüzü için REST'i, iç servis-servis trafiği için gRPC'yi seçmenin
   gerekçesi nedir? (En az iki fark say.)
6. gRPC'nin dört çağrı biçimini say ve her birine bir günlük örnek ver. Bunlardan hangisi
   REST'in klasik istek-yanıt kalıbına birebir karşılık gelir?
7. HTTP/2'nin "çoğullama" (multiplexing) özelliği HTTP/1.1'in hangi problemini çözer?
8. Protobuf mesajında alan numarası (field tag) ne işe yarar? Bir alana yeni numara vermek
   neden güvenli, var olan bir alanın numarasını değiştirmek neden tehlikelidir?
9. Bir tarayıcı neden doğrudan gRPC çağrısı yapamaz? grpc-web bunu nasıl çözer ve karşılığında
   hangi yeteneği kaybedersin?
10. `/kullanicilar/42` kaydının yalnızca e-posta alanını değiştireceksin. PUT mu PATCH mi
    kullanırsın, neden?
