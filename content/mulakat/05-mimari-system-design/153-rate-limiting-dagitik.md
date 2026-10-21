---
slug: rate-limiting-dagitik
baslik: "Dağıtık Rate Limiting: Token/Leaky Bucket, Sliding Window, Redis Sayaç ve 429 + Retry-After"
kademe: İleri
sira: 153
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir programın başka bir programa gönderdiği "bana şunu ver / şunu kaydet" notuna **istek** (request), geri gelen cevaba **yanıt** (response) denir. Herkes istediği kadar istek gönderebilseydi ne olurdu? Tek bir hatalı döngü ya da tek bir kötü niyetli istemci sunucuyu doldururdu; diğer bütün kullanıcılar hizmet alamazdı. Bu yüzden servisler **kota** koyar. Kota, belirli bir zaman diliminde bir istemciye tanınan izinli istek sayısıdır; örneğin "dakikada 100 istek". Bu kotayı kapıda uygulayan bileşene **rate limiter** (hız sınırlayıcı) denir.

Hız sınırlayıcı üç işi birden yapar: kapasiteyi korur, maliyeti öngörülebilir kılar ve kullanıcılar arasında adaleti sağlar. Mülakatta bu konu neredeyse her zaman iki katmanda sorulur. Birincisi hangi algoritmayı seçtiğin. İkincisi, o algoritmayı **birden çok sunucuya** yaydığında neyin bozulduğu.

### Temel kavramlar

- **Pencere (window)**: kotanın sayıldığı zaman aralığı, ör. 1 dakika.
- **Patlama (burst)**: kısa bir an içinde yoğunlaşan istek yığını. Bir uygulamanın açılışta 20 isteği birden atması normal bir patlamadır.
- **Jeton kovası (token bucket)**: Kovaya sabit ortalama hızda jeton (token) düşer ve her istek bir jeton harcar. Kova doluysa biriken jetonlar kadar **patlamaya izin verilir**. Kova boşsa istek reddedilir. Uzun vadede ortalama hız, jeton doldurma hızıyla sınırlı kalır.
- **Sızdıran kova (leaky bucket)**: İstekler bir kuyruğa alınır ve kuyruktan **sabit hızda** çıkar. Giriş ne kadar dalgalı olursa olsun çıkış pürüzsüzdür. Yani bu yöntem patlamayı **düzleştirir (smoothing)**.
- **Fixed window**: her pencerede sıfırlanan basit bir sayaç.
- **Sliding window**: gerçekten kayan son N saniyeyi sayan yaklaşım (istek günlüğü ya da iki komşu pencereyi ağırlıklı harmanlayan sayaç).
- **Atomik işlem**: bölünemeyen, araya başka kimsenin giremediği tek adım.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Token bucket patlamaya izin verir, leaky bucket düzleştirir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki kova, iki davranış</text>
  <rect x="30" y="44" width="276" height="132" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="70" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">Token bucket</text>
  <text x="168" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">token'lar ort. hızda dolar</text>
  <text x="168" y="114" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">dolu kova = BURST'e izin</text>
  <text x="168" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">uzun-vade ortalama sınırlı</text>
  <text x="168" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanım: API kotası, burst dostu</text>
  <rect x="334" y="44" width="276" height="132" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="70" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Leaky bucket</text>
  <text x="472" y="94" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">kuyruktan SABİT hızda çıkış</text>
  <text x="472" y="114" text-anchor="middle" font-size="11.5" fill="#B4531F" font-family="system-ui">burst'ü DÜZLEŞTİRİR</text>
  <text x="472" y="134" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">çıkış akışı pürüzsüz</text>
  <text x="472" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanım: kırılgan downstream</text>
</svg>
```

### Nasıl çalışır?

Algoritma seçimi **istenen davranışa** göre yapılır. Patlamaya izin vermek ama uzun vadeli ortalamayı sınırlamak istiyorsan token bucket; arkadaki servis dalgalanmayı kaldıramıyor ve sabit bir akış istiyorsan leaky bucket. Bu ikisi zıt amaçlar için vardır: biri patlamayı korur, öteki patlamayı yok eder.

Fixed window ise basitliğiyle cezbeder ama gizli bir kusuru vardır. Diyelim limit dakikada 100. Bir istemci birinci pencerenin **son** anında 100, ikinci pencerenin **ilk** anında yine 100 istek gönderir. Böylece yaklaşık iki saniyelik bir aralıkta 200 istek geçirmiş olur. Buna **sınır patlaması (boundary spike)** denir. Bu, sayaçtaki bir hata değil, algoritmanın doğasıdır; limiti düşürmek de onu ortadan kaldırmaz. Çözüm sliding window'dur: kayan aralık iki komşu pencereyi birbirine karıştırmaz.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fixed window sınır patlaması vs sliding window">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sınır etkisi: fixed vs sliding window (limit 100/dk)</text>
  <text x="30" y="58" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Fixed window</text>
  <rect x="140" y="44" width="180" height="34" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="230" y="65" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Pencere 1 (00:00–01:00)</text>
  <rect x="322" y="44" width="180" height="34" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="412" y="65" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Pencere 2 (01:00–02:00)</text>
  <rect x="286" y="40" width="72" height="42" rx="5" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="322" y="56" text-anchor="middle" font-size="9.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">100+100</text>
  <text x="322" y="72" text-anchor="middle" font-size="9" fill="#8A2E2E" font-family="system-ui">~2 sn'de</text>
  <text x="510" y="65" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">= 200 sızar</text>
  <text x="30" y="124" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sliding window</text>
  <rect x="140" y="110" width="362" height="34" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="321" y="131" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">son 60 sn'yi gerçek zamanlı kayan aralık sayar</text>
  <rect x="286" y="106" width="72" height="42" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="322" y="132" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">≤100</text>
  <text x="510" y="131" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">sınır yok</text>
  <text x="320" y="176" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Fixed: pencere sıfırlanınca komşu uçlar birbirine karışır</text>
  <text x="320" y="194" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sliding: log ya da ağırlıklı sayaç → sınır kümelenmesi biter</text>
</svg>
```

Asıl mülakat tuzağı **dağıtıklıktır**. Rate limiter tek sunucuda değil, 20 uygulama sunucusunda çalışır ve hepsi aynı sayacı paylaşır. Naif uygulama şudur: `GET` ile sayacı oku, limitle karşılaştır, `SET` ile bir artırıp yaz. İki sunucu aynı anda `GET` yaparsa ikisi de aynı değeri okur. İkisi de bir artırıp yazar ve **bir artış kaybolur**. Buna okuma-değiştirme-yazma yarışı (race condition) denir.

Çözüm, işlemi tek bir **bölünmez (atomik)** adıma indirmek ve bu bölünmezliği depoya bırakmaktır. Redis'in `INCR` komutu okuma ile artırmayı sunucu tarafında tek adımda yapar. Daha karmaşık bir mantık gerekiyorsa tek bir **Lua betiği** aynı garantiyi verir. Alternatifler işe yaramaz: sunucu içi (yerel) kilit 20 makineyi koordine etmez, kopyadan okumak bayat veriyle sorunu büyütür, araya beklemeler koymak ise yarışı yalnız seyrekleştirir; ortadan kaldırmaz.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GET-SET yarışı vs atomik INCR">
  <defs>
    <marker id="rate-at-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0,0 L6,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dağıtık sayaç: yarış vs atomiklik</text>
  <rect x="24" y="42" width="286" height="150" rx="12" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="167" y="64" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Naif: GET → kontrol → SET</text>
  <rect x="44" y="78" width="80" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="84" y="98" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Sunucu A</text>
  <rect x="210" y="78" width="80" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="250" y="98" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Sunucu B</text>
  <text x="84" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">GET → 5 okur</text>
  <text x="250" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">GET → 5 okur</text>
  <line x1="84" y1="132" x2="150" y2="152" stroke="#5D7789" stroke-width="1.3" marker-end="url(#rate-at-ar)"/>
  <line x1="250" y1="132" x2="184" y2="152" stroke="#5D7789" stroke-width="1.3" marker-end="url(#rate-at-ar)"/>
  <rect x="132" y="154" width="70" height="28" rx="6" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="167" y="172" text-anchor="middle" font-size="10" font-weight="700" fill="#8A2E2E" font-family="system-ui">SET 6 (5+1)</text>
  <text x="167" y="196" text-anchor="middle" font-size="9.5" fill="#8A2E2E" font-family="system-ui">bir artış KAYBOLUR (6 olmalı, 7 değil)</text>
  <rect x="330" y="42" width="286" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="64" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Atomik: INCR (tek adım)</text>
  <rect x="350" y="78" width="80" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="390" y="98" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Sunucu A</text>
  <rect x="516" y="78" width="80" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="556" y="98" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Sunucu B</text>
  <line x1="390" y1="108" x2="452" y2="150" stroke="#5D7789" stroke-width="1.3" marker-end="url(#rate-at-ar)"/>
  <line x1="556" y1="108" x2="494" y2="150" stroke="#5D7789" stroke-width="1.3" marker-end="url(#rate-at-ar)"/>
  <rect x="428" y="152" width="90" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="473" y="164" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">INCR bölünmez</text>
  <text x="473" y="177" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">5→6→7 doğru</text>
</svg>
```

Adalet ayrı bir tasarım kararıdır. Tek bir küresel sayaç tüm müşterileri aynı havuza koyar. O zaman ağır bir kiracı (tenant) bütçenin tamamını yer ve diğerlerini **aç bırakır (starvation)**. Doğru desen, sayacı **anahtar başına** tutmaktır. Her kiracının, API anahtarının ya da kullanıcının kendi kovası olur; gerekirse plana göre farklı kota ve ağırlık verilir. Global limiti yükseltmek izolasyon getirmez, rastgele istek düşürmek ise ne adil ne öngörülebilirdir.

Son parça **ret sözleşmesidir**. Limiti aşan istek `429 Too Many Requests` ile reddedilir. Yanına `Retry-After` başlığı, ideal olarak da `X-RateLimit-*` başlıkları konur. Böylece iyi davranan istemci ne zaman tekrar deneyeceğini bilir ve gereksiz bir yeniden deneme fırtınası oluşmaz.

Diğer kodlar yanlış anlam taşır. `403` bir yetkilendirme reddidir. `200 OK` dönüp isteği sessizce yok saymak istemciye başarı yalanı söyler. `503` ise servis genelinde bir arızayı ima eder; istemciye özel kota aşımını anlatmaz.

### Mülakatta ne sorulur?

- "Patlamaya izin ver ama ortalamayı sınırla" → token bucket; kova boyutu kadar burst, doldurma hızı kadar ortalama.
- "Kırılgan downstream sabit 100/sn istiyor" → leaky bucket; kuyruktan sabit hızlı çıkış.
- "Limit dakikada 100 ama 2 saniyede 200 geçti" → fixed window sınır patlaması; çözüm sliding window.
- "20 sunucu aynı sayacı güncelliyor" → atomik `INCR` / Lua; yerel kilit çözmez.
- "Bir müşteri diğerlerini aç bırakıyor" → anahtar başına kova + kota/ağırlık.
- "Reddederken ne dönersin?" → `429` + `Retry-After`.

### Tek cümlede

Hız sınırlamada algoritma, istediğin davranışı seçer: patlamaya izin mi, düzleştirme mi? Dağıtık doğruluk bölünmez sayaçtan gelir. Adalet anahtar başına kovadan gelir. Nezaket ise `429` + `Retry-After` cevabından gelir.

# rate-1 | İleri
## Soru
Kısa patlamalara (burst) izin vermek ama uzun-vade ortalama hızı kısıtlamak istiyorsun. Hangi algoritma?
## A
Leaky bucket, çünkü sabit hıza düzleştirir ve patlamayı yasaklar
🤖 Leaky bucket patlamayı KALDIRIR; istenen bu değil.
## B
Token bucket, çünkü jetonlar ortalama hızda dolar ve dolu kova patlamaya izin verir
🤖 Doğru: tam bunun için tasarlandı — burst + ortalama sınırı bir arada.
## C
Fixed window sayacı, çünkü uygulaması en basit ve en ucuz olanı
🤖 Sınırlarda 2× patlama sızdırır ama bu kontrollü değildir; amaç bu değil.
## D
Hiçbir rate limiting algoritması kontrollü patlamaya izin vermez
🤖 Yanlış; token bucket tam olarak patlama için tasarlandı.
## Doğru
B
## Açıklama
**Token bucket** patlama + ortalama-sınır ikilisinin klasik çözümüdür: kovaya token'lar sabit ortalama hızda eklenir, her istek bir token harcar. Kova doluysa biriken token'lar sayesinde kısa bir **patlamaya** izin verilir; kova boşsa istek beklemek/reddedilmek zorundadır. Böylece kısa vadede burst serbest, uzun vadede ortalama token doldurma hızıyla sınırlıdır. **Leaky bucket** (A) tam tersine burst'ü düzleştirir; **fixed window** (C) sınır patlamasını kontrolsüz sızdırır. Burst-dostu kota için token bucket seçilir.

# rate-2 | İleri
## Soru
Bir fixed-window limiter (dakikada 100 istek), istemcinin 2 saniyelik bir aralıkta 200 istek göndermesine izin veriyor. Nedeni ve çözümü nedir?
## A
Sayaç uygulamasında bir hata var; pencereyi daha sık sıfırlamak gerekir
🤖 Hata değil; bu fixed-window'un doğasındaki sınır (boundary) etkisidir.
## B
Redis nihai tutarlı olduğu için; güçlü tutarlı bir depoya geçmek gerekir
🤖 İlgisiz; sorun depoda değil, fixed-window algoritmasında.
## C
Pencere sonundaki 100 ile sonraki pencerenin başındaki 100 kümelenir; sliding window bunu giderir
🤖 Doğru: sınır etkisi; kayan pencere ardışık pencereleri birbirine karıştırmaz.
## D
Limit çok yüksek ayarlanmış; dakikalık kotayı düşürmek sorunu çözer
🤖 Limiti düşürmek 2× sınır patlamasını kaldırmaz.
## Doğru
C
## Açıklama
**Fixed window** sayacı her pencerede (ör. her dakika) sıfırlanır. İstemci ilk pencerenin **son** anında 100, hemen ardından ikinci pencerenin **ilk** anında 100 istek gönderirse, ~2 saniyelik bir aralıkta 200 isteği sınır çevresinde kümeleyebilir — bu **boundary spike**'tır ve limit değeriyle (D) ya da depo tutarlılığıyla (B) ilgili değildir. Çözüm **sliding window**'dur: kayan bir günlük (log) ya da iki komşu pencereyi ağırlıklı harmanlayan bir sayaç, geçmiş son 60 saniyeyi gerçek anlamda kayan bir aralık olarak sayar ve sınır kümelenmesini ortadan kaldırır.

# rate-3 | İleri
## Soru
Bir rate limiter 20 uygulama sunucusunda çalışıp paylaşılan bir Redis sayacını kullanıyor. Naif bir `GET → kontrol → SET` yarış (race) içeriyor. En doğru çözüm nedir?
## A
Kontrol ve artırmayı tek atomik adımda yapan `INCR` (ya da bir Lua script)
🤖 Doğru: okuma-değiştirme-yazma tek atomik operasyona indirgenir; yarış kalkar.
## B
Her uygulama sunucusuna kendi süreç-içi (yerel) kilidini eklemek
🤖 Yerel kilit 20 sunucu arasında koordinasyon sağlamaz.
## C
Yükü azaltmak için sayaç değerini Redis replikasından okumak
🤖 Replikalar bayattır; yarışı daha da kötüleştirir.
## D
Her `SET` işleminden önce 10ms bekleyip çakışma ihtimalini düşürmek
🤖 Yarışı kaldırmaz; sadece gecikme ekler.
## Doğru
A
## Açıklama
Dağıtık sayaçta sorun **okuma-değiştirme-yazma yarışıdır**: iki sunucu aynı anda `GET` yapıp aynı değeri okur, ayrı ayrı artırıp yazar ve bir artış kaybolur. Çözüm operasyonu **tek atomik adıma** indirmektir: Redis `INCR` (veya daha karmaşık mantık için tek bir **Lua script**) kontrol ve artırmayı sunucu tarafında bölünmez biçimde yapar. Yerel kilit (B) yalnız o sunucu içinde çalışır, 20 sunucuyu koordine etmez; replikadan okuma (C) bayat veriyle yarışı büyütür; uyumak (D) yarışı kaldırmaz. Dağıtık durumun sırrı: **atomiklik depoya delege edilir.**

# rate-4 | İleri
## Soru
Tek bir kiracının (tenant) trafiği, tek global limit arkasında diğerlerini aç bırakıyor. Adalet için en doğru tasarım nedir?
## A
Tüm kiracılar için tek bir global sayaç ve tek limit
🤖 Sorunun ta kendisi; izolasyon yok.
## B
Kiracıya bakmadan istekleri rastgele düşürmek (random drop)
🤖 Adil değil ve deterministik değil; kiracıları izole etmez.
## C
Global limiti belirgin biçimde yükseltip herkese daha çok pay vermek
🤖 Ağır kiracı yine diğerlerini aç bırakır; izolasyon gelmez.
## D
Kiracı başına (anahtar başına) ayrı kova, plana göre farklı kota ve ağırlıkla
🤖 Doğru: per-key izolasyon adaleti sağlar.
## Doğru
D
## Açıklama
Adil kullanım (fair use) **izolasyon** gerektirir. Tek global sayaç (A) tüm kiracıları aynı havuza koyar; ağır bir kiracı tüm bütçeyi tüketip diğerlerini aç bırakır (starvation). Çözüm sayacı **anahtar başına** (per-tenant/per-key) tutmaktır: her kiracının kendi kovası olur, gerekirse plana göre farklı kota/ağırlık verilir ve bir kiracının davranışı diğerini etkilemez. Limiti yükseltmek (C) izolasyon getirmez; rastgele düşürme (B) hem adaletsiz hem öngörülemezdir. İlke: **paylaşılan kaynağı anahtar bazında bölümle** ki bir aktör diğerini bozmasın.

# rate-5 | İleri
## Soru
Limiti aşan bir isteği reddederken doğru HTTP cevabı hangisidir?
## A
Hiçbir ek başlık koymadan `403 Forbidden` dönmek
🤖 403 yetkilendirmedir; yanlış anlam ve geri-çekilme (backoff) ipucu yok.
## B
`200 OK` dönüp isteği sessizce yok saymak
🤖 İstemciye yalan söyler; hemen tekrar dener.
## C
Yönlendirme başlığı olmadan `503 Service Unavailable` dönmek
🤖 503 servis-geneli bir sorundur; istemci-özel limiti ya da ne zaman deneneceğini iletmez.
## D
`429 Too Many Requests` ile birlikte `Retry-After` başlığını dönmek
🤖 Doğru: doğru semantik + backoff yönlendirmesi.
## Doğru
D
## Açıklama
Rate limit reddinin standardı **429 Too Many Requests**'tir ve yanına **Retry-After** başlığı (ve ideal olarak `X-RateLimit-*` başlıkları) konur; böylece iyi davranan istemci ne zaman tekrar deneyeceğini bilir ve gereksiz retry fırtınası oluşmaz. **403** (A) yetkilendirme reddidir, anlamı yanlıştır; **200 + sessiz yok sayma** (B) istemciye başarı yalanı söyler ve derhal yeniden denemeye yol açar; **503** (C) servis-geneli arızayı ima eder, istemci-özel limiti iletmez. Doğru sözleşme: doğru durum kodu **artı** backoff yönlendirmesi.

# rate-6 | İleri
## Soru
Yalnız sabit 100 istek/sn'ye (patlamasız) dayanabilen kırılgan bir downstream'i korumalısın. Hangi limiter şekli uygundur?
## A
Leaky bucket, çünkü istekler kuyruktan sabit hızda çıkar ve akış pürüzsüzleşir
🤖 Doğru: çıkış hızı sabittir; downstream tam da bunu ister.
## B
Beklenen en büyük patlamaya göre boyutlandırılmış bir token bucket
🤖 Patlamaya izin verir; downstream'in kaldıramadığı şey tam bu.
## C
Limiter koymamak; downstream kendi aşırı yükünü kendisi yönetsin
🤖 Kırılgan downstream ezilir.
## D
Limiti yüksek tutulmuş basit bir fixed window sayacı
🤖 Sınır patlaması sızdırır; downstream yine sıçrar.
## Doğru
A
## Açıklama
Kırılgan downstream **patlama tolere etmiyor**, sabit bir akış istiyor. **Leaky bucket** tam bunu yapar: gelen istekler bir kuyruğa alınır ve kuyruktan **sabit hızda** (ör. 100/sn) çıkar; giriş ne kadar dalgalı olursa olsun çıkış pürüzsüz bir akıştır. **Token bucket** (B) tasarımı gereği burst'e izin verir — bu senaryonun tam istemediği şey; **limiter'sız** (C) downstream ezilir; **fixed window** (D) sınır patlaması sızdırıp yine sıçrama üretir. Kural: **burst dostu ise token bucket, smoothing gerekiyorsa leaky bucket** — ikisi zıt amaçlar için vardır.
