---
slug: load-balancing-stratejileri
baslik: "Yük Dengeleme (Load Balancing): L4 vs L7, Algoritmalar, Sticky Session ve GSLB"
kademe: Orta
sira: 143
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir siteyi açtığında bilgisayarın, uzaktaki başka bir bilgisayara "şu sayfayı bana gönder" diye kısa bir mesaj yollar. Bu mesaja **istek** (request), isteği karşılayan ve hiç kapanmayan bilgisayara da **sunucu** (server) denir. Bir sunucu sonsuz güçlü değildir: saniyede ancak belli sayıda isteğe yetişir ve arızalanırsa site tümüyle kapanır.

Çare, aynı işi yapan birkaç sunucuyu yan yana koymaktır. Ama kullanıcının hangisine gideceğini bilmesi beklenemez. İşte tüm istekleri karşılayıp arkadaki sunuculara paylaştıran ara kata **yük dengeleyici (load balancer, kısaca LB)** denir. Arkadaki sunucuların her birine de **arka uç (backend / upstream)** denir. İki temel kazancı vardır. Birincisi, sunucu ekleyerek kapasiteyi büyütmektir; buna **yatay ölçeklenme** denir. İkincisi, bir sunucu ölünce sistemin ayakta kalmaya devam etmesidir; buna **yüksek erişilebilirlik** denir.

### Temel kavramlar

- **L4 (taşıma katmanı)**: LB yalnız IP adresine ve port numarasına bakar, mesajın içini açmaz. Çok hızlıdır, ucuzdur; ama "hangi sayfa isteniyor" gibi bir bilgiyi göremez.
- **L7 (uygulama katmanı)**: LB HTTP'yi anlar; adresin yolunu (`/api/v2/...`), başlıkları (header) ve çerezleri (cookie) okuyup ona göre karar verir.
- **Algoritma**: isteğin hangi sunucuya verileceğini belirleyen kural (round robin, least connections, hash).
- **Sticky session (oturum yapışkanlığı)**: bir kullanıcıyı hep aynı sunucuya bağlama.
- **Sağlık kontrolü (health check)**: bozulan sunucuyu havuzdan çıkarma mekanizması.
- **GSLB**: kullanıcıyı dünyanın neresinde olursa olsun en yakın veri merkezine yollayan üst katman.

### Nasıl çalışır?

L4 ile L7 arasındaki fark, "zarfın üstüne mi bakıyorsun, mektubu mu okuyorsun" farkıdır. L4 zarfa bakar: hedef IP ve port. TCP/UDP bağlantısını olduğu gibi bir arka uca taşır. Şifreli trafiği açmasına gerek yoktur (pass-through); bu yüzden çok hızlı ve ucuzdur. Ama `/api/v2/` gibi bir yol bilgisini asla göremez.

L7 ise mektubu okur. HTTP ya da gRPC'yi ayrıştırır, TLS'i sonlandırabilir (termination), yola/başlığa/çereze göre farklı havuzlara yönlendirir; yeniden deneme (retry) ve sıkıştırma yapar. Daha zekidir, daha pahalıdır. Aynı 443 portuna gelen iki farklı yolu ayırmak yalnız L7'nin işidir. Mavi-yeşil (blue-green) ve kanarya (canary) dağıtımların mekanizması da budur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="L4 ve L7 yük dengeleyici karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı iki soru, iki katman</text>
  <rect x="28" y="44" width="272" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">L4 — Transport</text>
  <text x="164" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">IP + port'a bakar</text>
  <text x="164" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">TCP/UDP yönlendirir, içeriği açmaz</text>
  <text x="164" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">çok hızlı, ucuz</text>
  <text x="164" y="162" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">URL/header'a göre karar veremez</text>
  <rect x="340" y="44" width="272" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">L7 — Uygulama</text>
  <text x="476" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">HTTP/gRPC'yi anlar</text>
  <text x="476" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">URL/header/cookie'ye göre yönlendirir</text>
  <text x="476" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">TLS sonlandırma, retry, sıkıştırma</text>
  <text x="476" y="162" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">daha zeki, daha maliyetli</text>
</svg>
```

Sunucu seçimini bir **algoritma** yapar. **Sırayla dağıtma (round robin)** istekleri teker teker sıradaki sunucuya verir. Basit ve öngörülebilirdir. Ama eşit *sayıda* istek, eşit *yük* demek değildir. Bazı istekler 10 milisaniye, bazıları 5 saniye sürüyorsa uzun işler tek sunucuda birikir; round robin bunu göremez.

**En az bağlantı (least connections)** o an **en az açık bağlantısı** olan sunucuyu seçer. Uzun süren işler o sunucunun sayacını yüksek tuttuğu için yeni istekler daha boş sunuculara kayar. Süreler değişkense en adil seçenek budur.

**Özet (hash) tabanlı** yönlendirme ise bir anahtarı — kullanıcının IP'si ya da bir veri anahtarı — matematiksel bir fonksiyondan geçirip hep aynı düğüme eşler. Böylece aynı anahtar hep aynı yere gider ve o düğümdeki **önbellek** işe yarar; buna önbellek yerelliği (locality) denir. Düz `hash % düğüm_sayısı` kullanırsan bir düğüm eklenince neredeyse tüm eşlemeler bozulur. Bu yüzden **tutarlı özetleme (consistent hashing)** tercih edilir: düğüm sayısı değişince yalnız komşu anahtarlar yer değiştirir.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç yük dengeleme algoritması: round robin, least connections, hash">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç dağıtım algoritması, üç mantık</text>
  <rect x="14" y="42" width="196" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="68" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">Round Robin</text>
  <text x="112" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sırayla dağıtır</text>
  <text x="112" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eşit SAYIDA istek</text>
  <text x="112" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">basit, öngörülebilir</text>
  <text x="112" y="162" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">süre farkını görmez</text>
  <rect x="222" y="42" width="196" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Least Connections</text>
  <text x="320" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">en az aktif bağlantıya</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">açık bağlantıyı ölçer</text>
  <text x="320" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değişken süreye adil</text>
  <text x="320" y="162" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">süreleri hesaba katar</text>
  <rect x="430" y="42" width="196" height="150" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="528" y="68" text-anchor="middle" font-size="13.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Hash-based</text>
  <text x="528" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">anahtar → sabit düğüm</text>
  <text x="528" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">aynı client hep aynı yer</text>
  <text x="528" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önbellek yerelliği korur</text>
  <text x="528" y="162" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">consistent hash ile stabil</text>
</svg>
```

**Oturum yapışkanlığı (sticky session)**, bir kullanıcıyı genelde bir çerez aracılığıyla hep aynı arka uca bağlar. Oturum bilgisini kendi belleğinde tutan uygulamalar buna mecbur kalır. Ama bedeli ağırdır: durum (state) o sunucuya çakılır. Sunucu ölünce ona bağlı kullanıcıların oturumu kaybolur. Yeni eklenen sunucular hemen yük almaz. Otomatik ölçekleme ve kademeli dağıtım zorlaşır. Dikkat: her sunucu yalnız kendine yapışan kullanıcıların oturumunu tutar; hepsininkini değil.

Doğru çözüm durumu dışarı taşımaktır. Oturumu Redis gibi paylaşılan bir depoda tut ve arka uçları **durumsuz (stateless)** yap. O zaman herhangi bir sunucu her isteği karşılayabilir. Sticky session ile TLS sonlandırma birbirinden bağımsızdır; affinity temel algoritmayı da zorunlu kılmaz.

**Sağlık kontrolü** iki biçimdedir. **Aktif** kontrolde LB düzenli aralıklarla ayrı bir yoklama isteği atar; örneğin `GET /health`. Böylece sorunu, gerçek kullanıcı etkilenmeden yakalar ve iyileşen sunucuyu havuza geri almayı doğrular. Bedeli ek yoklama yüküdür.

**Pasif** kontrolde ayrı bir yoklama yoktur. LB gerçek trafikteki hataları sayar: bağlantı reddi, 5xx yanıtı, zaman aşımı. Eşik aşılınca sunucuyu havuzdan çıkarır. Bedavadır; ama sorunu ancak bazı kullanıcılar etkilendikten sonra fark eder. Pratikte ikisi birlikte kullanılır. İkisi de hem L4'te hem L7'de uygulanabilir. İkisinin de ölçütü CPU/bellek metrikleri değil, isteğe verilen cevaptır.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aktif ve pasif sağlık kontrolü karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sağlık kontrolü: aktif mi, pasif mi?</text>
  <rect x="28" y="44" width="272" height="126" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="70" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">Aktif (proaktif)</text>
  <text x="164" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">LB düzenli prob atar (GET /health)</text>
  <text x="164" y="116" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">sorunu kullanıcıdan önce yakalar</text>
  <text x="164" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geri gelen sunucuyu doğrular</text>
  <text x="164" y="158" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">ek prob yükü yaratır</text>
  <rect x="340" y="44" width="272" height="126" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="70" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Pasif (gözlemsel)</text>
  <text x="476" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">gerçek trafikteki hataları sayar</text>
  <text x="476" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">5xx / timeout / bağlantı reddi</text>
  <text x="476" y="138" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">ek prob yükü yok</text>
  <text x="476" y="158" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">sorunu geç fark eder</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`/api/v2/` ile başlayan istekleri yeni servise yolla" isteniyor; hangi katman?** L7. Yol bilgisi HTTP'nin içindedir; L4 yalnız IP+port gördüğü için aynı 443 portuna gelen iki yolu ayıramaz.
- **İstek süreleri çok değişkense hangi algoritma?** Least connections. Round robin sayıyı eşitler, yükü değil; rastgele dağıtım anlık yığılmayı göremez; IP hash yük dengeleme için değil, affinity içindir.
- **Sticky session'ın en ciddi bedeli?** Durum sunucuya çakılır: sunucu ölünce oturum kaybolur, ölçeklenme ve yeniden dengeleme zorlaşır.
- **Aktif mi, pasif mi sağlık kontrolü?** Aktif = düzenli prob; pasif = canlı trafikteki hatalardan çıkarım. Katmanla ilgisi yoktur ve pasif "daha güvenli" değil, daha geçtir.
- **Kullanıcıyı en yakın bölgeye nasıl yollarsın?** **GSLB** ile. İki yolu vardır. Coğrafi DNS: aynı alan adına, sorunun geldiği yere göre farklı bölgenin IP'sini döndürmek. **BGP anycast**: aynı IP'yi birçok bölgeden birden duyurmak. Klasik sıralı (round-robin) DNS sırayla IP dağıtır ama kullanıcının nerede olduğunu bilmez. IP paketinde "ülke kodu" diye bir alan da yoktur.
- **Aynı anahtar hep aynı önbellek düğümüne nasıl gider?** Anahtara göre özet tabanlı yönlendirmeyle; tercihen tutarlı özetleme kullanılarak.

### Tek cümlede

Yük dengeleyici, gelen istekleri sağlıklı sunuculara doğru kurala göre paylaştırarak tek makinenin kapasitesini de tek makinenin arızasını da sistemin sorunu olmaktan çıkarır.

# lb-1 | Orta
## Soru
Bir yük dengeleyicinin `/api/v2/` ile başlayan istekleri "yeni servis" havuzuna, geri kalanı "eski servis" havuzuna yönlendirmesi isteniyor. Hangi katman bunu yapabilir?
## A
Yalnız L4; yönlendirme port numarasına bakılarak yapılır
🤖 L4 yalnız IP ve port görür; iki yol da aynı 443 portuna geldiği için ayıramaz.
## B
Hem L4 hem L7; ikisi de isteğin içeriğini aynı şekilde okuyabilir
🤖 L4 mesajın içini açmaz, yol bilgisini göremez; ikisi bu işte eşit değildir.
## C
Hiçbiri; bu ayrım ancak uygulama kodunun içinde yapılabilir
🤖 L7 LB tam bunun için vardır; kodu değiştirmeden yola göre yönlendirir.
## D
Yalnız L7; karar isteğin HTTP yoluna (path) göre veriliyor
🤖 Doğru: içerik-tabanlı yönlendirme (content-based routing) uygulama katmanı ister.
## Doğru
D
## Açıklama
URL yoluna (`/api/v2/`) göre yönlendirme, isteğin **HTTP içeriğini** okumayı gerektirir. Bu bilgi taşıma katmanında (L4 — yalnız IP/port) görünmez; yalnız **L7 (uygulama katmanı)** LB, HTTP'yi ayrıştırıp yola/başlığa/çereze göre farklı upstream havuzlarına dağıtabilir. Bu, mavi-yeşil (blue-green) ve kanarya (canary) dağıtımların temel mekanizmasıdır. L4 ancak port bazında ayrım yapabilir; aynı 443 portuna gelen iki farklı yolu ayıramaz. Uygulama kodunu değiştirmek de gerekmez — ayrım LB yapılandırmasında yapılır.

# lb-2 | Orta
## Soru
Backend istekleri süre olarak çok değişken (bazıları 10ms, bazıları 5sn). Yükü en adil dağıtan algoritma hangisidir?
## A
Round Robin; sırayla dağıttığı için her sunucuya eşit yük düşer
🤖 Eşit *sayıda* istek ≠ eşit *yük*; uzun istekler bir sunucuda birikir, RR bunu göremez.
## B
Least Connections; o an en az açık bağlantısı olana gönderir
🤖 Doğru: aktif iş yükünü ölçtüğü için değişken süreli isteklerde dengeyi korur.
## C
Rastgele (random); istatistiksel olarak her zaman en adil dağılımdır
🤖 Rastgele uzun vadede dengelense de anlık yığılmaları göz ardı eder.
## D
IP hash; aynı client hep aynı sunucuya gider, bu da yükü eşitler
🤖 IP hash affinity içindir, yük dengeleme için değil; ağır bir client tek sunucuyu ezer.
## Doğru
B
## Açıklama
**Round Robin** istekleri sırayla eşit *sayıda* dağıtır ama süreleri hesaba katmaz: 5 saniyelik birkaç istek aynı sunucuda birikirse o sunucu boğulur ve RR bunu fark etmez. **Least Connections** o an **en az aktif (açık) bağlantısı** olan sunucuya yönlendirir; uzun süren istekler o sunucunun bağlantı sayısını yüksek tutacağından yeni istekler daha boş sunuculara gider. Bu yüzden istek süreleri değişkense Least Connections (veya ağırlıklı çeşidi) daha adildir. Süreler birbirine yakınsa RR'nin basitliği zaten yeterlidir.

# lb-3 | İleri
## Soru
Uygulama oturum durumunu (session state) her sunucunun kendi belleğinde tutuyor ve bu yüzden sticky session (session affinity) kullanılıyor. Bunun en ciddi mimari dezavantajı nedir?
## A
Sticky session TLS sonlandırmayı (termination) imkânsız hale getirir
🤖 İkisi bağımsızdır; affinity ile TLS sonlandırma birlikte kullanılabilir.
## B
Yalnız hash algoritmasıyla çalışır, Round Robin ile birlikte kullanılamaz
🤖 Affinity çoğunlukla çerezle sağlanır; temel algoritmadan bağımsızdır.
## C
Durum sunucuya çakılır; sunucu ölünce oturum kaybolur, ölçeklenme zorlaşır
🤖 Doğru: durum tek sunucuya bağlandığı için hata toleransı ve elastikiyet düşer.
## D
Her sunucunun belleğinde tüm kullanıcıların oturumunu tutmayı zorunlu kılar
🤖 Tam tersi; her sunucu yalnız kendisine yapışmış kullanıcıların oturumunu tutar.
## Doğru
C
## Açıklama
Sticky session, bir client'ı hep aynı backend'e bağlayarak bellekteki oturumu kullanılabilir kılar; ama durumu (state) **o sunucuya çakılar**. Sunucu çökerse ona bağlı tüm kullanıcıların oturumu kaybolur (login düşer). Ayrıca yeni sunucu ekleyince trafik hemen dengelenmez — mevcut affinity'ler eski sunuculara kilitli kalır — bu da otomatik ölçekleme ve kademeli dağıtımı (rolling deploy) zorlaştırır. Doğru çözüm durumu **dışarı taşımaktır**: oturumu Redis/Memcached gibi paylaşılan bir depoda tut, böylece backend'ler durumsuz (stateless) olur, herhangi bir sunucu her isteği karşılayabilir ve sticky session'a gerek kalmaz.

# lb-4 | İleri
## Soru
Aktif sağlık kontrolü (active health check) ile pasif sağlık kontrolü (passive) arasındaki temel fark nedir?
## A
Aktif düzenli aralıklarla prob gönderip test eder; pasif gerçek trafikteki hatalardan çıkarım yapar
🤖 Doğru: biri proaktif prob atar, diğeri canlı isteklerin hatalarını gözler.
## B
Aktif kontrol yalnız L4 LB'lerde, pasif kontrol ise yalnız L7 LB'lerde uygulanabilir
🤖 Katmanla ilgisi yok; ikisi de her iki katmanda uygulanabilir.
## C
Pasif kontrol daha güvenlidir; sunucuya hiç ek yük bindirmediği için tercih edilir
🤖 Pasif kontrol gerçek isteklerin başarısız olmasını bekler; "güvenli" değil, geç fark eder.
## D
Aktif kontrol sunucunun ayakta olmasına, pasif kontrol CPU/bellek metriklerine bakar
🤖 İkisinin de ölçütü metrik değil, isteğe verilen cevaptır; ayrım prob mu, canlı trafik mi?
## Doğru
A
## Açıklama
**Aktif** sağlık kontrolü, LB'nin backend'e **düzenli aralıklarla** ayrı bir prob (örn. `GET /health`) göndermesidir; başarısız olursa sunucu havuzdan çıkarılır — sorun, gerçek kullanıcı etkilenmeden önce yakalanabilir. **Pasif** kontrol ayrı prob atmaz; **gerçek trafikteki** hataları (bağlantı reddi, 5xx, zaman aşımı) sayar ve eşik aşılınca sunucuyu çıkarır — ek yük yaratmaz ama sorunu ancak bazı kullanıcılar etkilendikten sonra fark eder. İkisi de hem L4 hem L7'de uygulanabilir ve ikisi de CPU/bellek metriğine değil isteğin sonucuna bakar. Pratikte birlikte kullanılırlar: pasif hızlı tepki için, aktif ise sunucunun tekrar sağlıklı olduğunu doğrulayıp havuza geri almak için.

# lb-5 | İleri
## Soru
Global kullanıcıları en yakın veri merkezine (bölge) yönlendirmek için kullanılan yaklaşım hangisidir?
## A
Least Connections; en boş bölgeyi otomatik olarak seçip oraya yönlendirir
🤖 Least Connections tek havuz içi dağıtım içindir; coğrafi yakınlığı bilmez.
## B
GSLB; coğrafi DNS veya BGP anycast ile kullanıcı en yakın bölgeye çıkar
🤖 Doğru: bölgeler-arası yönlendirme DNS/anycast düzeyinde yapılır.
## C
Sticky session; kullanıcıyı ilk girdiği bölgeye kalıcı olarak sabitler
🤖 Affinity bölge içi bir kavramdır; ilk seçimi coğrafyaya göre yapmaz.
## D
Round-robin DNS; her sorguya sırayla farklı bir bölgenin IP'si döndürülür
🤖 Round-robin DNS sırayla IP dağıtır ama kullanıcının nerede olduğunu bilmez.
## Doğru
B
## Açıklama
Tek bir veri merkezi içindeki dağıtım (RR, Least Connections) ile **bölgeler-arası** dağıtım farklı katmanlardır. **GSLB (Global Server Load Balancing)** kullanıcının coğrafi konumuna ve bölgelerin sağlığına göre yönlendirir; iki yaygın mekanizması vardır: (1) **coğrafi DNS** — aynı isme farklı bölgelerden gelen sorgulara en yakın bölgenin IP'sini döndürür; (2) **BGP anycast** — aynı IP birden çok bölgeden duyurulur, ağ kullanıcıyı en yakın noktaya taşır. Klasik round-robin DNS ise IP'leri sırayla dağıtır, coğrafyayı hesaba katmaz. Bölge içi LB'ler bu üst katmanın altında çalışır; bir bölge tamamen düşerse GSLB trafiği sağlıklı bölgelere kaydırır (failover).

# lb-6 | Orta
## Soru
Bir dağıtık önbellek (distributed cache) önündeki LB'de, aynı anahtar (key) için gelen isteklerin **hep aynı** cache düğümüne gitmesi isteniyor. Bunun için uygun yaklaşım hangisidir?
## A
Round Robin; her istek sırayla bir sonraki düğüme gider, dağılım eşit olur
🤖 RR aynı anahtarı farklı düğümlere dağıtır; cache isabet oranı (hit rate) çöker.
## B
Least Connections; istek o an en az bağlantısı olan düğüme gönderilir
🤖 En boş düğüm sürekli değişir; aynı anahtar farklı düğümlere düşer, önbellek anlamsızlaşır.
## C
Anahtara göre hash tabanlı yönlendirme (tutarlı hash / consistent hashing)
🤖 Doğru: aynı anahtar hep aynı düğüme eşlenir, önbellek yerelliği (locality) korunur.
## D
Rastgele yönlendirme; istatistiksel dağılım önbelleği kendiliğinden dengeler
🤖 Rastgele, aynı anahtarı her seferinde farklı düğüme atar; yerellik kaybolur.
## Doğru
C
## Açıklama
Önbellek yerelliği (cache locality) için aynı anahtarın hep aynı düğüme gitmesi şarttır; aksi halde her düğüm her anahtarın kopyasını tutmaya çalışır, isabet oranı düşer ve bellek israf olur. **Hash tabanlı** yönlendirme anahtarı (veya client IP'sini) hash'leyip bir düğüme deterministik olarak eşler. Düğüm eklenip çıktığında en az yeniden dağıtım için **tutarlı hash (consistent hashing)** kullanılır: düz `hash % N`, düğüm sayısı değişince neredeyse tüm anahtarların yerini değiştirirken, tutarlı hash yalnız komşu anahtarları taşır. RR, Least Connections ve rastgele dağıtım bu senaryoda önbelleği işlevsiz bırakır.
