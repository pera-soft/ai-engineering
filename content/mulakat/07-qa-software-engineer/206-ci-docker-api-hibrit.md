---
slug: ci-docker-api-hibrit
baslik: "CI/CD (GitHub Actions), Docker ve API + UI Hibrit Oturum Stratejisi"
kademe: İleri
sira: 206
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Vaka çalışmasının (case study) bonus maddeleri üç şey istiyor. B1: giriş işlemi API üzerinden yapılacak ve oturum tarayıcıya doğrudan yazılacak; böylece arayüz senaryoları hızlanacak. B2: GitHub Actions ile bir iş hattı kurulacak; testler ekransız koşacak ve Allure raporu indirilebilir çıktı olarak yüklenecek. B3: Dockerfile ile proje tek komutla çalıştırılabilir olacak.

Üçü de projede gerçekten var. Ama değerleri "yapıldı" kutusunu işaretlemekte değildir. Değer, **neden böyle** yapıldığını savunabilmektedir.

Bu konu bir hız katmanı değildir; **güvenilirlik ve dürüstlük** katmanıdır. Dört soruya cevap verir: hangi test ne zaman koşacak, bir altyapı engeli test hatasından nasıl ayırt edilecek, oturum yazma yöntemi nerede güvenli ve neyin hâlâ doğrulanmadığı.

### Temel kavramlar

Önce terimler — hepsi aşağıda tekrar geçiyor, ilk geçtikleri yerde anlamlarını sabitleyelim:

- **İş hattı (pipeline)**: kod değiştiğinde otomatik çalışan adımlar zinciridir — kur, test et, raporu yükle. GitHub Actions'ta tek bir YAML dosyasıyla tanımlanır (`.github/workflows/tests.yml`).
- **Koşum makinesi (runner)**: GitHub'ın testleri koşturduğu bulut makinesidir. *Paylaşımlı* makine GitHub'ın ortak havuzundan gelir ve çıkış IP adresi herkesle ortaktır. **Kendi barındırdığın makine (self-hosted runner)** ise senin kendi bilgisayarındır; IP adresi sana aittir.
- **Secret**: parola/token gibi gizli değerlerin repoda değil GitHub'ın kasasında tutulduğu alan (secret); koşum sırasında ortam değişkeni olarak verilir, loglarda maskelenir.
- **Artifact**: koşum bittikten sonra indirilebilir hâle getirilen çıktı dosyası (artifact) — burada Allure raporu ve ham sonuç JSON'ları.
- **CDN** (içerik dağıtım ağı, content delivery network): siteyi kullanıcıya yakın sunuculardan servis eden ara katman. Siteye gelen tüm trafiği ilk o karşılar; dolayısıyla engelleme de burada yapılır. e-bebek'in önündeki CDN, Amazon'un **CloudFront** hizmetidir.
- **Storefront**: e-ticaret uygulamasının müşteriye görünen ön yüzü (storefront). **Spartacus**, SAP Commerce'in Angular tabanlı hazır storefront'udur — e-bebek bunu kullanır.
- **OAuth**: bir uygulamanın kullanıcı adına yetki jetonu (token) almasını sağlayan standart protokol. `grant_type=password` akışında telefon+şifre doğrudan token uç noktasına gönderilir, karşılığında erişim jetonu (access token) döner.
- **Storage state**: tarayıcının oturumu tuttuğu yer — çerezler ve `localStorage` içeriği. "Oturumu enjekte etmek", UI'dan giriş yapmak yerine bu değeri doğrudan yazmak demektir.

Şimdi projenin somut kararları:

- **İki ayrı CI işi**: `unit` işi `main` dalına her gönderimde ve her değişiklik isteğinde (pull request) koşar; yaklaşık 15 saniye sürer ve tarayıcı gerektirmez. `e2e` işi ise yalnızca elle tetiklenir (`workflow_dispatch`). Bu iş `EBEBEK_PHONE` ve `EBEBEK_PASSWORD` gizli değerlerini okur. `if: always()` sayesinde koşum kırılsa bile rapor indirilebilir çıktı olarak yüklenir.
- **CloudFront engeli**: e-bebek'in önünde bir CDN vardır. GitHub'ın paylaşımlı makinelerinin IP adresleri `403 Request blocked` yanıtıyla reddedilir. Bu bir **altyapı erişim** sorunudur; test kodunun sorunu değildir.
- **Oturum verisinin doğrudan yazılması**: `utils/api-auth.js` dosyası jetonu doğrudan ön yüzün OAuth uç noktasından alır. Sonra bu jetonu Spartacus'un oturumu sakladığı tek `localStorage` anahtarına (`spartacus⚿⚿auth`) yazar ve sayfayı yeniler.
- **`addInitScript` ile tek seferlik yazma farkı**: `addInitScript`, o bağlamdaki **her sayfa geçişinde** yeniden çalışır. Tek seferlik yazma ve ardından sayfa yenileme ise yalnızca **bir kez** yazar.
- **OAuth `client_id`/`client_secret`**: storefront'un herkese açık değerleri — her tarayıcı isteğinde görünürler, kullanıcı sırrı değildirler. Gerçek hesap bilgileri `.env`/GitHub Secrets üzerinden gelir.
- **Docker imaj sürümünün hizalanması**: `Dockerfile`, `mcr.microsoft.com/playwright:v1.61.1-noble` imajını temel alır. Bu sürüm `package.json` içindeki Playwright sürümüyle **bilinçli olarak eşitlenmiştir**; esnek sürüm aralığı (`^1.61`) kullanılmaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CI tetikleyicileri: push/PR yalnizca birim testi, workflow_dispatch uctan uca">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">CI tetikleyicileri</text>
  <rect x="30" y="46" width="180" height="130" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">main'e push / PR</text>
  <text x="120" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">unit job</text>
  <text x="120" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~15 sn, tarayıcı yok</text>
  <text x="120" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her zaman koşar</text>
  <rect x="240" y="46" width="180" height="130" rx="11" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="330" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B5504F" font-family="system-ui">workflow_dispatch</text>
  <text x="330" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">e2e job (elle)</text>
  <text x="330" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">secret kontrolü önce</text>
  <text x="330" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">canlı siteye bağlı</text>
  <text x="330" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hesap kilitlenme riski</text>
  <rect x="450" y="46" width="160" height="130" rx="11" fill="none" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="530" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">gecelik plan</text>
  <text x="530" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">denendi</text>
  <text x="530" y="112" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">403 CloudFront</text>
  <text x="530" y="130" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">kaldırıldı</text>
</svg>
```

### Nasıl çalışır?

**Uçtan uca testler neden her gönderimde koşmaz? İki ayrı gerekçe var.** Birincisi: testler canlı siteye karşı koşuyor ve ayrı bir test ortamı yok. Her commit'te gerçek siteye trafik göndermek doğru değil.

İkincisi ve daha önemlisi: site hatalı giriş denemelerini sayıyor ve "Kalan deneme hakkınız: 4" diyor. Negatif giriş senaryosu gerçek hesaba tek bir hatalı deneme gönderir. Bunu her gönderimde yapmak hesabı kilitlerdi.

Bu iki gerekçe birbirinden bağımsızdır. Biri ortam ve trafikle, diğeri hesap güvenliğiyle ilgilidir. İkisinden yalnızca biri olsaydı bile karar aynı kalırdı.

**CloudFront engeli test hatasından nasıl ayırt edildi?** Gecelik bir zamanlanmış plan denendi; koşum `403 Request blocked` hatasıyla düştü. Sorunun testlerde değil, siteye erişimin kendisinde olduğu kolayca doğrulandı. Hata anında otomatik alınan ekran görüntüsü, e-bebek'in herhangi bir sayfasını değil, **doğrudan CloudFront'un kendi hata sayfasını** gösteriyordu.

Doğru çözüm, çıkış IP adresi engellenmeyen kendi barındırdığın bir makinedir. Bu proje kapsamında böyle bir makine bilinçli olarak kurulmadı. Gecelik plan kaldırıldı ve uçtan uca iş yalnızca elle tetiklenir hâle getirildi.

**Gizli değerlerin erken kontrolü.** İş hattında tarayıcı kurulumundan ve asıl koşumdan **önce** bir adım çalışır: "Giriş bilgileri tanımlı mı?" `EBEBEK_PHONE` veya `EBEBEK_PASSWORD` boşsa iş hemen durur. Amaç, eksik bir gizli değeri uzun bir koşumun ortasında değil ilk saniyelerde yakalamaktır. (İşin zaman aşımı `timeout-minutes: 30` olarak ayarlıdır.)

**API + arayüz karması, yalnızca ön koşul olduğu yerde.** Çıkış senaryosunda test edilen şey çıkışın kendisidir; giriş yalnızca bir ön koşuldur. Bu yüzden iki adımlı giriş formu doldurulmaz.

`utils/api-auth.js` içindeki `fetchAccessToken`, ön yüzün OAuth uç noktasına doğrudan istek atar (`grant_type=password`). Dönen jeton, `sessionStorageValue()` yardımıyla Spartacus'un oturumu sakladığı **tek** `localStorage` anahtarına (`spartacus⚿⚿auth`) yazılır. Ardından sayfa yenilenir.

```javascript
async function fetchAccessToken({ phone, password }) {
  const api = await request.newContext({ baseURL: config.apiUrl });
  try {
    const response = await api.post('/authorizationserver/oauth/token', {
      form: { ...OAUTH_CLIENT, grant_type: 'password', username: phone, password, captcha_token: 'null' },  // … kısaltıldı
    });

    if (!response.ok()) {
      const body = (await response.text()).slice(0, 200);
      throw new Error(`API girişi başarısız (HTTP ${response.status()}): ${body}`);
    }
    return response.json();
  } finally {
    await api.dispose();
  }
}
```

Yazma işlemi bilinçli olarak **tek seferliktir**. `addInitScript` ile yapılsaydı, o bağlamdaki **her sayfa geçişinde** jeton yeniden yazılırdı.

Çıkış senaryosunda bu ölümcül olurdu. Çıkış işleminden sonraki ilk sayfa yüklemesinde jeton sessizce geri yazılırdı. Misafir (guest) durumu hiçbir zaman doğrulanamazdı.

Tek seferlik yazma ve ardından açık bir `reload()` çağrısı, uygulamanın oturum açık olarak **başlamasını** sağlar. Sonraki sayfa geçişlerine ise müdahale etmez. Böylece çıkış sonrasındaki gerçek misafir durumu gözlemlenebilir kalır.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="addInitScript her navigasyonda calisir, tek seferlik enjeksiyon bir kez yazar">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Çıkış senaryosu için: neden tek seferlik enjeksiyon?</text>
  <rect x="30" y="42" width="270" height="120" rx="11" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="165" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B5504F" font-family="system-ui">addInitScript (kullanılmadı)</text>
  <text x="165" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her navigasyonda tekrar çalışır</text>
  <text x="165" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çıkış sonrası token geri yazılır</text>
  <text x="165" y="124" text-anchor="middle" font-size="10.5" fill="#B5504F" font-family="system-ui">→ misafir durumu doğrulanamaz</text>
  <rect x="340" y="42" width="270" height="120" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Tek seferlik yazım + reload</text>
  <text x="475" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">localStorage'a bir kez yazılır</text>
  <text x="475" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sonraki navigasyona müdahale etmez</text>
  <text x="475" y="124" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ çıkış sonrası misafir gözlemlenebilir</text>
</svg>
```

**Neden bu karma yöntem her senaryoda kullanılmıyor?** İki durumda arayüz kullanılır. Birincisi, giriş akışının kendisi test edilen davranışsa: `login.feature` dosyasında form gerçekten doldurulur. İkincisi, "misafir sepeti giriş sonrası korunuyor mu?" sorusu doğrulanıyorsa: `cart_persistence.feature` dosyasında giriş bir **eylem olarak** test edilir.

API üzerinden giriş yalnızca girişin sadece bir **ön koşul** olduğu yerde kullanılır; yani çıkış senaryosunda.

**`client_id` ve `client_secret` gizli bilgi değildir.** Bunlar ön yüzün herkese açık değerleridir ve her tarayıcı isteğinde açıkça görünürler. Asıl gizli bilgi olan telefon ve şifre `.env` dosyasından gelir; CI ortamında ise GitHub Secrets üzerinden gelir. Bunlar depoda yer almaz.

**Docker: sürüm hizalaması ve dürüst itiraf.** `Dockerfile` içindeki taban imaj etiketi (`v1.61.1-noble`), `package.json` içindeki Playwright sürümüyle kasıtlı olarak eşitlenmiştir. Bu yüzden Playwright sürümü de esnek aralıkla değil, sabit tutulur. Aksi hâlde imajdaki tarayıcılar ile kurulu kütüphane zamanla birbirinden uzaklaşırdı.

Ama REVIEW.md'nin "Yapılmayanlar" bölümü açıkça bir eksiği söylüyor: imaj hiç derlenip içinde test koşturulmadı, çünkü geliştirme makinesinde Docker kurulu değildi. Bu tutum, "her şey test edildi" demek yerine neyin doğrulanmadığını adıyla söylemektir.

### Mülakatta ne sorulur?

- **"CI neden her push'ta uçtan uca koşmuyor?"** Beklenen: iki ayrı gerekçe — canlı siteye trafik + negatif senaryonun gerçek hesabı kilitleme riski.
- **"Gecelik plan neden kaldırıldı?"** Beklenen: CloudFront paylaşımlı runner IP'lerini 403 ile reddediyor; hata ekran görüntüsü CloudFront sayfasını gösteriyor — test hatası değil erişim hatası.
- **"API girişini neden her senaryoda kullanmıyorsun?"** Beklenen: login akışının kendisi test edilen davranış olduğu senaryolarda (S1, cart_persistence) UI sürülür; hibrit yalnız ön koşul olduğu yerde (çıkış).
- **"Neden `addInitScript` değil de tek seferlik enjeksiyon?"** Beklenen: `addInitScript` her navigasyonda tekrar yazar; çıkış sonrası misafir durumunu doğrulamak imkânsızlaşırdı.
- **"`client_secret` repoda mı?"** Beklenen: storefront'un public client bilgisi, sır değil; gerçek hesap bilgisi `.env`/GitHub Secrets'ta.
- **"Docker'ı test ettin mi?"** Beklenen: hayır, dürüstçe REVIEW.md'de yazıldı; imaj sürümü Playwright ile hizalandı ama build+koşum doğrulanmadı.

### Tek cümlede

CI, Docker ve API karması üçlüsü bir hız katmanı değil, **güvenilirlik ve dürüstlük** katmanıdır. Uçtan uca testler bilinçli olarak seyrek koşar ve elle tetiklenir. Bir altyapı engeli (CloudFront) test hatasından ayırt edilir. Oturum yazma yalnızca gerçekten ön koşul olan yerde ve tam olarak bir kez kullanılır. Neyin hâlâ doğrulanmadığı (Docker imajının koşum testi) açıkça söylenir.

# ci-1 | İleri
## Soru
CI pipeline'ında uçtan uca job'ı yalnızca `workflow_dispatch` ile elle tetikleniyor, her push'ta koşmuyor. Demo notları ve REVIEW.md iki ayrı gerekçe veriyor. Bu gerekçeler nedir?
## A
Yalnızca GitHub Actions'ın ücretsiz dakika kotasını aşmamak için; uçtan uca koşum kotanın büyük kısmını tüketiyor
🤖 Maliyet gerekçe olarak belgelenmiyor; asıl sebepler canlı site bağımlılığı ve hesap kilitlenme riski.
## B
Playwright tarayıcı kurulumunun her push'ta çok uzun sürmesi ve runner kotasını doldurması
🤖 Kurulum birkaç dakika sürer ama bu, tetikleme kararının belgelenen gerekçesi değil.
## C
Canlı siteye her commit'te trafik göndermek doğru değil; ayrıca negatif senaryo gerçek hesabı kilitleyebilir
🤖 Doğru: iki ayrı, birbirinden bağımsız gerekçe — biri trafik/ortam, diğeri hesap güvenliği.
## D
Cucumber tag ifadelerinin GitHub Actions'ın push tetikleyicisiyle uyumlu çalışmaması
🤖 Tag ifadeleri CI'da sorunsuz çalışır; tetikleme kararının sebebi teknik uyumsuzluk değil.
## Doğru
C
## Açıklama
Demo notları iki gerekçeyi açıkça ayırıyor: "Birincisi canlı siteye bağlıyız; her commit'te gerçek siteye trafik göndermek doğru değil. İkincisi ve daha önemlisi: site hatalı giriş denemelerini sayıyor. Negatif senaryom gerçek hesaba bir hatalı deneme gönderiyor; her push'ta yapmak hesabı kilitlerdi." REVIEW.md de hesap kilitlenme riskini ayrı bir zayıflık maddesi olarak (madde 2) kayda geçiriyor; README ise CloudFront engelini belgeliyor. Bu iki gerekçe birbirinden bağımsızdır — biri test ortamı/trafik meselesi, diğeri hesap güvenliği meselesidir. Maliyet (A), kurulum süresi (B) veya tag uyumsuzluğu (D) belgelenen gerekçeler değildir.

# ci-2 | İleri
## Soru
Gecelik (scheduled) plan denenip kaldırıldı; koşum `403 Request blocked` hatasıyla düştü. Bunun test kodundaki bir hata değil, altyapı erişim engeli olduğu nasıl kanıtlandı?
## A
Allure raporundaki adım süreleri sıfıra yakın çıktı; bu da testlerin hiç çalışmadan düştüğünü gösterdi
🤖 Adım süresi tek başına erişim engelini kanıtlamaz; kanıt görsel oldu, süre değil.
## B
GitHub Actions loglarında `ETIMEDOUT` hatası art arda üç koşumda da tekrarladı
🤖 Belgelenen hata mesajı `403 Request blocked`; zaman aşımı hatası değil.
## C
Testler yerelde aynı tag ifadesiyle koşulduğunda hiç hata vermedi, bu farkı kanıtladı
🤖 Yerelde geçmesi tek başına kanıt değil; kesin kanıt koşum sırasında otomatik alınan görsel oldu.
## D
Hata anında alınan ekran görüntüsü, e-bebek sayfası yerine CloudFront'un hata sayfasını gösterdi
🤖 Doğru: ekran görüntüsü siteye hiç ulaşılamadığını, CDN katmanında reddedildiğini doğrudan gösterdi.
## Doğru
D
## Açıklama
e-bebek'in önünde CloudFront var ve GitHub'ın paylaşımlı runner IP'lerini reddediyor. Bunu doğrulamak kolaydı, çünkü hata anında `After` hook'unun otomatik aldığı ekran görüntüsü doğrudan **CloudFront'un hata sayfasını** gösteriyordu — e-bebek'in herhangi bir ekranı değil. Bu, sorunun test mantığında değil, siteye erişimin kendisinde olduğunu doğrudan kanıtlıyor. Adım süresi (A) veya farklı bir hata mesajı (B) bu kanıtı sağlamaz; yerelde geçmesi (C) tek başına altyapı engelini ispatlamaz, kesin kanıt görsel oldu.

# ci-3 | İleri
## Soru
Workflow dosyasında "Giriş bilgileri tanımlı mı" adı verilen bir kontrol, tarayıcı kurulumundan ve asıl test koşumundan (job zaman aşımı 30 dk) ÖNCE çalışıyor. Bu sıralamanın amacı nedir?
## A
Eksik bir secret'ı uzun ve pahalı koşumun ortasında değil, ilk saniyelerde ucuz bir adımda ortaya çıkarmak
🤖 Doğru: fail-fast — eksik secret 20 dakikalık koşumun sonunda değil ilk saniyelerde fark edilir.
## B
GitHub Actions'ın secret doğrulamayı yalnızca ilk adımda yapmasına izin veren bir platform kısıtı olduğu için
🤖 Böyle bir platform kısıtı yok; sıralama projenin kendi tercihi.
## C
Tarayıcı kurulumunun secret'lara bağımlı olması, `EBEBEK_PHONE` olmadan `npx playwright install` çalışmaması
🤖 Tarayıcı kurulumu giriş bilgilerinden bağımsızdır; sıralamanın sebebi bu değil.
## D
Allure raporunun secret bilgisi olmadan üretilememesi, `environment.properties`'in bu adımı gerektirmesi
🤖 `environment.properties` baseUrl/headless gibi bilgilerle yazılır, secret gerektirmez.
## Doğru
A
## Açıklama
Workflow'un yorum satırı da bu gerekçeyi açıkça söylüyor: "Eksik secret'i 20 dakikalik bir kosumun ortasinda degil, hemen soyle." (Yorumdaki "20 dakika" kabaca bir tahmindir; job'a tanınan gerçek zaman aşımı `timeout-minutes: 30`.) Kontrol, tarayıcı kurulumundan (`npx playwright install --with-deps chromium`) ve asıl test koşumundan önce çalışır; `EBEBEK_PHONE`/`EBEBEK_PASSWORD` boşsa iş anında ve açık bir mesajla durur. Bu, uzun ve kaynak tüketen bir işin sonunda değil, en ucuz adımda başarısız olma (fail-fast) prensibidir. Platform kısıtı (B), teknik bağımlılık (C) veya Allure gereksinimi (D) belgelenen gerekçeler değildir.

# ci-4 | İleri
## Soru
`utils/api-auth.js` çıkış (logout) senaryosunda oturumu API üzerinden hazırlıyor: token alınıp Spartacus'un `localStorage` anahtarına yazılıyor ve sayfa yenileniyor. Bu enjeksiyon neden `addInitScript` ile değil, sayfa yenilemesiyle tek seferlik yapılıyor?
## A
`addInitScript` yalnızca `chromium.launch()` seviyesinde tanımlanabilir, senaryo başına context'te kullanılamaz
🤖 `addInitScript` context/page seviyesinde kullanılabilir; kısıtlama teknik erişilebilirlik değil.
## B
`addInitScript` her navigasyonda yeniden çalışır; token çıkış sonrası geri yazılır, misafir durumu doğrulanamazdı
🤖 Doğru: çıkışın kendisini doğrulamak için oturumun gerçekten sonlanması gerekiyor; sürekli yeniden enjeksiyon bunu imkânsızlaştırırdı.
## C
Sayfa yenilemesi olmadan Playwright `localStorage` yazımını hiç desteklemiyor, yazılan değer kalıcı olmuyor
🤖 Playwright, context açıkken de `localStorage`'a yazabilir; sorun destek değil, yeniden enjeksiyonun test hedefini bozması.
## D
`addInitScript` yalnızca senaryo başına bir kez çağrılabildiği için zaten tek seferlik enjeksiyonla aynı sonucu verir
🤖 Tam tersi: `addInitScript` tanımlandığı context'teki HER navigasyonda tekrar çalışır, tek seferlik değildir.
## Doğru
B
## Açıklama
README bunu açıkça gerekçelendiriyor: "Enjeksiyon bilinçli olarak tek seferlik: `addInitScript` ile yapılsaydı token her navigasyonda geri yazılır ve çıkış sonrası misafir durumu hiç doğrulanamazdı." `addInitScript`, tanımlandığı context'teki her sayfa yüklemesinde yeniden çalışan bir mekanizmadır — bu, çıkıştan hemen sonraki ilk navigasyonda token'ı sessizce geri yazıp testin doğrulamak istediği "artık misafirsin" durumunu ortadan kaldırırdı. Bunun yerine token bir kez `localStorage`'a yazılır, sayfa bir kez yenilenir; sonraki navigasyonlara müdahale edilmez. A ve C teknik olarak yanlış iddialar, D ise `addInitScript`'in davranışını tam tersine çeviriyor.

# ci-5 | İleri
## Soru
Proje yalnızca çıkış senaryosunun ön koşulunu API ile hazırlıyor; login (S1) ve misafir sepeti (cart_persistence) senaryolarında hâlâ UI formu sürülüyor. Bu ayrımın gerekçesi nedir?
## A
API girişi yalnızca çıkış senaryosunun kullandığı tarayıcı profiliyle uyumlu, diğer senaryolar farklı profil kullanıyor
🤖 Profil uyumluluğu gibi bir kısıt belgelenmiyor; ayrım hangi davranışın test edildiğiyle ilgili.
## B
UI formu sürmek API'den daha hızlı olduğu için hız kazandıran senaryolarda UI tercih ediliyor
🤖 Tam tersi: API girişi UI'dan hızlıdır; hız burada ayrımın sebebi değil.
## C
Giriş akışının kendisi o senaryolarda test edilen davranış; API girişi yalnız ön koşul olduğu yerde kullanılıyor
🤖 Doğru: S1'de giriş formunun kendisi, cart_persistence'ta "giriş yapınca sepet korunuyor mu" doğrulanıyor; ikisinde de giriş, atlanacak bir ön koşul değil.
## D
Misafir sepeti senaryosu teknik olarak API girişini desteklemiyor, Spartacus API'si misafir sepetini kabul etmiyor
🤖 Belgelenen sebep teknik bir API kısıtı değil; hangi davranışın doğrulandığıyla ilgili bilinçli bir tasarım tercihi.
## Doğru
C
## Açıklama
Kural nettir: "Çünkü giriş akışının kendisi test edilmesi gereken bir şey. S1 giriş senaryosunda formu gerçekten sürüyorum. API girişini sadece ön koşul olduğu yerde kullanıyorum — çıkış senaryosunda test edilen şey çıkış, giriş değil. Misafir sepeti senaryosunda da UI girişi kullanıyorum, çünkü orada test edilen şey tam olarak 'giriş yapınca sepet korunuyor mu'." Hibrit yaklaşım bir hız optimizasyonu değil, "bu senaryoda gerçekten neyi doğruluyorum" sorusuna verilen bir cevaptır — test edilen davranışın kendisi asla kısayoldan geçirilmez.

# ci-6 | İleri
## Soru
Dockerfile, `mcr.microsoft.com/playwright:v1.61.1-noble` imajını temel alıyor ve bu sürüm `package.json`'daki Playwright sürümüyle bilinçli olarak sabitlenmiş (floating range değil). Docker ile ilgili REVIEW.md'de ayrıca ne itiraf ediliyor?
## A
İmajın Chromium yerine yanlışlıkla Firefox içerdiği ve bunun sürüm hizalaması sırasında düzeltilmediği
🤖 Böyle bir hata belgelenmiyor; imaj resmi Playwright imajı, doğru tarayıcıyı içerir.
## B
Docker'da koşan testlerin CI'dakinden farklı sonuç verdiği ve bu tutarsızlığın nedeninin bulunamadığı
🤖 Böyle bir tutarsızlık belgelenmiyor; itiraf edilen şey testin hiç koşturulmamış olması.
## C
`.env` dosyasının yanlışlıkla imaja gömüldüğü ve yayınlanırsa secret sızıntısı riski taşıdığı
🤖 `.env` `.dockerignore` ile hariç tutuluyor; belgelenen itiraf bu değil.
## D
İmajın hiç build edilip içinde koşturulmadığı, bu yüzden gerçekten çalıştığının doğrulanmadığı
🤖 Doğru: REVIEW.md "Yapılmayanlar" bölümünde bu açıkça yazılı; sürüm hizalaması yapıldı ama çalıştırma doğrulanmadı.
## Doğru
D
## Açıklama
REVIEW.md'nin "Yapılmayanlar" bölümü şunu açıkça söylüyor: "Docker imajının koşum doğrulaması: Dockerfile yazıldı ve temel alınan Playwright imajının sürümü projeyle hizalandı (bu yüzden Playwright sürümü sabit), ancak imaj build edilip içinde tam senaryo seti koşturulmadı." Bu itiraf, sürüm hizalamasının (imaj etiketinin Playwright sürümüyle sabitlenmesi) yapıldığını ama en temel doğrulamanın — imajı gerçekten build edip içinde testleri koşturmak — hiç yapılmadığını dürüstçe belirtiyor; sebep geliştirme makinesinde Docker'ın kurulu olmamasıydı. Bu, "her şey çalışıyor" demek yerine neyin doğrulanmadığını adlandırmaktır.
