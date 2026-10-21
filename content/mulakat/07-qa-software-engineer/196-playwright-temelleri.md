---
slug: playwright-temelleri
baslik: "Playwright Temelleri: Otomatik Bekleme, BrowserContext, Trace"
kademe: Temel
sira: 196
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

**Playwright**, bir web sitesini gerçek bir tarayıcıda (Chromium, Firefox, WebKit) açan bir test aracıdır. Tıklama, yazma ve okuma gibi kullanıcı eylemlerini kod üzerinden yürütür. Bu tür testlere uçtan uca test (end-to-end, e2e) denir.

Playwright, Selenium'un yıllardır çözdüğü işi yapar; ama farklı bir varsayımla kurulmuştur. Varsayım şudur: testin en büyük düşmanı kararsızlıktır. Kararsızlığın en büyük kaynağı da "elemente ne zaman dokunulacağını bilmemektir". Playwright bu soruyu senin yerine cevaplar.

Bu projede (`eriklabs_web_case`) testler e-bebek'in **canlı sitesine** karşı koşuyor; ayrı bir test ortamı yok. Böyle bir ortam Playwright'ı asıl sınavına sokar: gerçek ağ gecikmesi, geç yüklenen bileşenler ve beklenmedik üst katman pencereleri (overlay).

### Temel kavramlar

- **Kendiliğinden bekleme (auto-waiting).** Playwright bir elemente tıklamadan önce dört şartı kendisi bekler: element sayfada var olacak, görünür olacak, etkileşime kapalı olmayacak (`disabled` değil) ve durulmuş olacak (animasyonu bitmiş). Bu davranış `sleep(2000)` gibi sabit beklemeleri büyük ölçüde gereksiz kılar. Sabit bekleme ya gereğinden uzun sürer ve testi yavaşlatır, ya da gereğinden kısa kalır ve testi kararsızlaştırır.
- **`expect(locator)` ve `expect.poll`.** Kendiliğinden bekleme yalnız *eylemler* için geçerlidir; yani tıklama ve yazma için. Bir *durumun* doğru olmasını beklemek ayrı bir araç ister. Örnek durum: "sepet özeti güncellenene kadar bekle". `expect(locator).toBeVisible()` bir seçicinin durumunu bekler. `expect.poll(fn).toBe(deger)` ise herhangi bir eşzamansız ifadenin sonucunu, koşul sağlanana kadar aralıklarla yeniden değerlendirir.
- **BrowserContext (tarayıcı bağlamı).** Tek bir tarayıcı süreci (`Browser`) içinde açılan yalıtılmış bir profildir. Kendi çerezi, oturumu ve `localStorage` alanı vardır; tarayıcıdaki gizli sekme gibi düşünülebilir. Bu projede tarayıcı `BeforeAll` ile **bir kez** açılır, ama her senaryo kendi bağlamını alır ve senaryo bitince o bağlam kapanır. Böylece paralel koşan senaryolar birbirinin çerezini veya sepetini görmez. (Bu konu Kademe 8'de derinleşir; burada yalnız tanıtım var.)
- **İzleme kaydı (trace).** Koşumun tamamını kaydeden özelliktir. Her adımdaki sayfa anlık görüntüsü (snapshot), ağ istekleri ve konsol kayıtları saklanır. Kayıt `context.tracing.start({ screenshots: true, snapshots: true })` ile başlar, `context.tracing.stop({ path: ... })` ile dosyaya yazılır. Bir test kırıldığında koşumu geriye sarıp tam o anda ekranda ne olduğunu görebilirsin.
- **Ekransız / ekranlı mod ve `SLOW_MO`.** Ekransız (headless) modda tarayıcı görünmeden arka planda çalışır; sürekli tümleştirme (CI) sunucusu için hızlıdır. Ekranlı (headed) modda pencere açılır; hata ayıklarken akışı gözle görürsün. `SLOW_MO` her eylem arasına milisaniye cinsinden gecikme koyar ve akışı insan gözüyle izlenebilir kılar.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sabit bekleme yerine otomatik bekleme">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sabit bekleme yerine koşula dayalı bekleme</text>
  <rect x="30" y="46" width="270" height="128" rx="12" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="165" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#B5504F" font-family="system-ui">sleep(2000) / waitForTimeout</text>
  <text x="165" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sabit süre bekler</text>
  <text x="165" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yavaş ağda kısa kalır</text>
  <text x="165" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hızlı ağda gereksiz uzar</text>
  <text x="165" y="160" text-anchor="middle" font-size="11" fill="#B5504F" font-family="system-ui">→ kararsız veya yavaş</text>
  <rect x="340" y="46" width="270" height="128" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Playwright otomatik bekleme</text>
  <text x="475" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">görünür + tıklanabilir olana dek</text>
  <text x="475" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">koşul sağlanınca hemen devam</text>
  <text x="475" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">gerçek durum kontrol edilir</text>
  <text x="475" y="160" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">→ hızlı ve kararlı</text>
</svg>
```

### Nasıl çalışır?

**Kendiliğinden bekleme her şeyi çözmez.** Projenin README dosyasında anlatılan "sepete ekle sessizce başarısız olabiliyor" durumu tam bu sınırı gösterir.

"Sepete Ekle" butonu sayfada görünür ve tıklanabilir durumdadır; Playwright'ın bütün kontrollerini geçer. Ama Angular'ın olay dinleyicisi (event listener) henüz butona bağlanmamıştır. Üstelik butonun üzerindeki `disable` CSS sınıfı hiç kalkmaz. Playwright'ın "etkin mi" kontrolü bu sınıfı görmez; o kontrol `disabled` özniteliğine ve `aria-disabled` değerine bakar. Yani buton Playwright'a hep aktif görünür.

Sorun şudur: Playwright "elemente tıklanabilir mi?" sorusunu yanıtlar. Buradaki asıl soru ise "tıklama bir şey yapıyor mu?" sorusudur. Çözüm kod tarafındadır: tıklamanın sonucunu, yani sitenin kendi açtığı onay penceresini kanıt saymak. Pencere gelmezse tıklamayı tekrarlamak. Bu örnek, "kendiliğinden bekleme her belirsizliği çözer" varsayımının **yanlış** olduğunu gösterir.

**İşlem zaman aşımı ile `expect` zaman aşımı farkı.** Bu projede tarayıcı bağlamı 20 saniyelik işlem zaman aşımıyla kurulur (`config.defaultTimeout`). Ama Playwright'ta `expect` varsayılanı 5 saniyedir.

Bu tutarsızlığın sonucu şuydu: sunucudan dönen bir geçişi doğrulayan adımlar — örneğin giriş sonrası açılan form — sayfa normal çalışırken bile ara ara düşüyordu. Çözüm, `utils/expect.js` içinde `playwrightExpect.configure({ timeout: config.defaultTimeout })` ile ortak bir süre tanımlamak oldu. Doğrulamalar artık işlemlerle aynı bütçeyi paylaşıyor.

**İzleme kaydı neden ekran görüntüsünden fazlasıdır?** Tek bir ekran görüntüsü testin **son anını** gösterir. İzleme kaydı ise testin **tüm geçmişini** saklar: her adımdaki sayfa durumunu, ağ isteklerini ve konsolu.

Bu projede izleme kaydı yalnız başarısız senaryolarda Allure raporuna eklenir. `After` kancasındaki mantık şudur: `if (failed) { ... } else { fs.rmSync(...) }`. Başarılı koşumlarda dosya silinir, çünkü kimsenin bakmayacağı kayıtları biriktirmek boşuna disk maliyetidir.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Trace zaman çizelgesi: adım adım DOM ve ağ kaydı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Trace: koşumun tamamı, geriye sarılabilir</text>
  <line x1="60" y1="100" x2="580" y2="100" stroke="#5D7789" stroke-width="1.6"/>
  <circle cx="120" cy="100" r="7" fill="#186399"/>
  <text x="120" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">goto('/login')</text>
  <circle cx="260" cy="100" r="7" fill="#186399"/>
  <text x="260" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">phone.fill()</text>
  <circle cx="400" cy="100" r="7" fill="#186399"/>
  <text x="400" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">submit.click()</text>
  <circle cx="520" cy="100" r="8" fill="#8A2E2E"/>
  <text x="520" y="126" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">✗ assertion kırıldı</text>
  <text x="520" y="146" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">o anki DOM snapshot'ı incelenebilir</text>
  <text x="320" y="70" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her nokta: ekran görüntüsü + DOM anlık görüntüsü + ağ isteği</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Playwright'ı Selenium'dan ayıran temel fark nedir?"** Beklenen: en büyük fark otomatik bekleme — Selenium'da her eylem öncesi `WebDriverWait` ile elle beklemek yaygın bir gerekliliktir, Playwright bunu varsayılan davranış yapar. Ayrıca tek API ile birden çok tarayıcı motorunu (Chromium/Firefox/WebKit) destekler.
- **"Sabit `sleep` kullanmamanın maliyeti ve kazancı ne?"** Beklenen: kazanç kararlılık ve hızdır; koşul sağlanınca test hemen devam eder. Ama kendiliğinden bekleme yalnız "elemente dokunulabilir mi?" sorusuna cevap verir. "Dokunma bir şey yaptı mı?" sorusuna cevap vermez. Bu yüzden bazı durumlarda ek doğrulama gerekir; olay dinleyicisi geç bağlanan buton buna örnektir.
- **"`expect(locator)` ile `expect.poll` ne zaman hangisi?"** Beklenen: `expect(locator)` doğrudan bir locator'ın durumunu (görünür/gizli/metin) bekler; `expect.poll` locator'la ifade edilemeyen, keyfi bir asenkron hesaplamayı (örn. URL path'inin değişmesi, bir input değerinin normalize edilmiş hâli) koşul sağlanana kadar tekrar tekrar değerlendirir.
- **"Neden headless yerine bazen headed/SLOW_MO ile koşuyorsun?"** Beklenen: CI'da hız ve kaynak tasarrufu için headless; hata ayıklarken veya demo göstermek için headed + `SLOW_MO`, akışı gözle takip edebilmek için.
- **"Trace'i nasıl kullandın, ne işine yaradı?"** Beklenen: `context.tracing.start/stop` ile kayıt; kırılan bir testte trace görüntüleyicide adım adım geriye sarıp, o anki DOM'u ve ağ isteklerini görerek kararsızlığın kaynağını bulmak — sadece son ekran görüntüsüyle yetinmemek.
- **"Testler canlı siteye karşı koşuyor; bunun riski ne, nasıl azalttın?"** Beklenen: riskler gerçek verinin, fiyatın ve stoğun değişkenliği, ağ gecikmesi ve CDN engelidir. Azaltma stratejisi üç adımlıdır: sabit değer beklemek yerine göreli (delta) doğrulama yapmak, üstü örten pencereleri zorlamadan kapatmak, kendiliğinden bekleme ile `expect.poll` kullanarak kararsızlığı emmek.

### Tek cümlede

Playwright, "elemente ne zaman dokunulacağı" sorusunu kendiliğinden bekleme ile senin yerine çözer; `expect` ve `expect.poll` ile durum doğrulaması, `BrowserContext` ile yalıtım, izleme kaydı ile geriye dönük inceleme sağlar. Ama unutma: kendiliğinden bekleme yalnız "tıklanabilir mi" sorusuna cevap verir, "tıklama işe yaradı mı" sorusuna değil.

# pw-1 | Temel
## Soru
Playwright'ta bir elemente tıklamadan önce sabit `sleep(2000)` yerine kütüphanenin kendi "otomatik bekleme" (auto-waiting) davranışına güvenmenin temel gerekçesi nedir?
## A
Sabit bekleme testin toplam süresini öngörülebilir kılar; otomatik bekleme süreyi değişkenleştirip CI'da ölçümü zorlaştırır
🤖 Öngörülebilir süre bir kazanç sayılmaz: sabit süre koşulu değil saati bekler, bu yüzden hem yavaş ağda yetersiz hem hızlı ağda israftır.
## B
Otomatik bekleme koşul sağlanana kadar bekler; sabit süre ya kısa kalıp kararsızlık ya uzun kalıp yavaşlık üretir
🤖 Doğru: koşula dayalı bekleme hem ağ hızından bağımsız çalışır hem de gereksiz süre harcamaz.
## C
Otomatik bekleme yalnızca headless modda çalışır, headed modda devre dışı kalır ve elle beklemek gerekir
🤖 Otomatik bekleme modu (headless/headed) ile ilgili değildir, her ikisinde de aynı şekilde işler.
## D
Sabit bekleme yalnızca Selenium'da vardır; Playwright'ta zaman aşımı (timeout) kavramı hiç yoktur
🤖 Playwright'ta da zaman aşımı kavramı vardır; fark, beklemenin süre değil koşula dayalı olmasıdır.
## Doğru
B
## Açıklama
Sabit bir bekleme (`sleep`/`waitForTimeout`) ağ ve sunucu koşullarından bağımsız, önceden belirlenmiş bir süre bekler: yavaş bir ağda bu süre yetmeyip test kararsızlaşır (flaky), hızlı bir ağda ise gereksiz yere uzayıp testi yavaşlatır. Playwright'ın otomatik beklemesi bunun yerine "eleman DOM'da var mı, görünür mü, etkileşime açık mı, stabil mi" koşulunu sağlanana kadar bekler ve koşul sağlanır sağlanmaz devam eder. Bu, hem daha kararlı hem genelde daha hızlı bir sonuç verir. Ancak bu otomatik bekleme yalnız "elemente dokunulabilir mi" sorusunu çözer — dokunmanın gerçekten bir etki yarattığını garanti etmez (bkz. pw-3).

# pw-2 | Temel
## Soru
Bir e2e test projesinde her senaryo öncesi tek bir `Browser` süreci yeniden başlatılmıyor, bunun yerine tarayıcı bir kez açılıp her senaryoya ayrı bir `BrowserContext` veriliyor. Bu tasarımın senaryolar arası izolasyona katkısı nedir?
## A
Her `BrowserContext` kendi çerez, oturum ve localStorage alanına sahiptir; bir senaryonun durumu diğerine sızmaz
🤖 Doğru: context, tarayıcı sürecini paylaşsa da depolama alanı bakımından bağımsız bir profildir.
## B
`BrowserContext` yalnızca ekran çözünürlüğünü değiştirir, izolasyonla ilgisi yoktur
🤖 Viewport ayarı context üzerinden yapılabilir ama context'in asıl işlevi depolama/oturum izolasyonudur.
## C
Tarayıcıyı her senaryoda yeniden başlatmamak, testler arası veri paylaşımını kasıtlı olarak sağlar
🤖 Tam tersi: context başına izolasyon amacı, veri paylaşımını önlemektir, sağlamak değil.
## D
Context'ler aynı `Browser` sürecini paylaştığı için çerezleri de paylaşır; izolasyon ancak tarayıcıyı yeniden başlatmakla sağlanır
🤖 Süreç paylaşımı depolama paylaşımı anlamına gelmez; her context'in çerez/localStorage alanı zaten ayrıdır.
## Doğru
A
## Açıklama
Tek bir `Browser` süreci içinde açılan her `BrowserContext`, kendi çerezine, oturumuna (localStorage/sessionStorage) ve sekme kümesine sahip bağımsız bir "profil" gibi davranır — gizli sekme açmaya benzer. Tarayıcıyı `BeforeAll` ile bir kez başlatıp her senaryoya ayrı context vermek, süreç başlatma maliyetinden (yavaş) kaçınırken senaryolar arası izolasyonu (kritik) korumanın yoludur: bir senaryonun çerezi, sepeti veya oturumu bir sonrakine sızmaz. Bu tasarım özellikle paralel koşumda önemlidir çünkü aynı anda çalışan senaryolar birbirinin durumunu bozmamalıdır.

# pw-3 | Temel
## Soru
Bir "Sepete Ekle" butonu DOM'da görünür ve Playwright'ın "tıklanabilir" kontrollerinden geçiyor, ama arkasındaki JavaScript olay dinleyicisi henüz bağlanmamış durumda; erken bir tıklama hiçbir hata vermeden sessizce hiçbir şey yapmıyor. Bu durum, otomatik bekleme hakkında hangi gerçeği gösterir?
## A
Otomatik bekleme "elemente dokunulabilir mi" sorusuna cevap verir, "dokunmanın etkisi oldu mu" sorusuna vermez
🤖 Doğru: Playwright görünürlük/etkileşilebilirlik kontrolü yapar, olay dinleyicisinin bağlı olup olmadığını bilemez.
## B
Bu Playwright'ın bir hatasıdır; güncel sürümler olay dinleyicilerinin bağlanmasını da bekleyerek sorunu kökten çözer
🤖 Bu, aracın hatası değil kapsamının dışıdır: hiçbir sürüm sayfanın JavaScript'inin ne zaman hazır olduğunu bilemez.
## C
Sorun yalnız headless modda oluşur; headed modda tarayıcı daha yavaş çalıştığı için buton her zaman tam işlevseldir
🤖 Sorun modla değil, JavaScript olay dinleyicisinin bağlanma zamanlamasıyla ilgilidir.
## D
Otomatik bekleme devre dışı bırakılıp her tıklamadan önce elle `waitForTimeout(3000)` eklenerek kesin çözülür
🤖 Sabit bekleme bu tür zamanlama sorunlarını garanti etmez; kanıta dayalı bekleme (sonucun gerçekleşmesini beklemek) gerekir.
## Doğru
A
## Açıklama
Playwright'ın otomatik beklemesi elementin DOM'da var, görünür, `disabled` olmayan ve konumu stabil olduğunu kontrol eder — bunların hepsi sağlanabilir ama elementin arkasındaki uygulama mantığı (ör. Angular'ın olay dinleyicisini bağlaması) henüz hazır olmayabilir. Bu durumda tıklama "başarılı" görünür ama hiçbir etki yaratmaz. Çözüm otomatik beklemeye daha fazla güvenmek değil, **eylemin sonucunu kanıt saymaktır**: örneğin sitenin kendi onay penceresinin açılmasını beklemek, açılmazsa tıklamayı tekrarlamak. Bu, "otomatik bekleme her belirsizliği çözer" varsayımının sınırını gösteren somut bir örnektir.

# pw-4 | Temel
## Soru
Bir tarayıcı context'i 20 saniyelik aksiyon zaman aşımıyla (action timeout) kuruluyor, fakat Playwright'ın `expect` fonksiyonunun varsayılan zaman aşımı 5 saniye. Sunucudan dönen bir geçişi (örn. giriş sonrası açılan form) doğrulayan adımlar sayfa hâlâ normal çalışırken aralıklı (intermittent) olarak başarısız oluyor. En uygun çözüm nedir?
## A
Tüm assertion'ları kaldırıp yalnızca aksiyonlara (tıklama, yazma) güvenmek
🤖 Assertion'ları kaldırmak testin doğrulama amacını ortadan kaldırır, sorunu gizler çözmez.
## B
`expect`'i, aksiyonlarla aynı zaman aşımı bütçesini kullanacak şekilde yapılandırmak
🤖 Doğru: assertion ve aksiyon zaman aşımlarını eşitlemek, sayfanın normal çalışırken erken "başarısız" sayılmasını önler.
## C
Her adımdan sonra sayfayı elle yeniden yükleyip (reload) doğrulamayı tekrar denemek
🤖 Yeniden yükleme durumu (örn. form doldurma ilerlemesini) sıfırlar; sorunu çözmez, yeni sorunlar yaratır.
## D
`expect` yerine her doğrulamadan önce sabit `waitForTimeout(20000)` çağırıp sonra kontrol etmek
🤖 Sabit uzun bekleme her adımı yavaşlatır ve otomatik beklemenin sağladığı erken-çıkış avantajını kaybettirir.
## Doğru
B
## Açıklama
Aksiyon zaman aşımı (elemente tıklama, yazma gibi eylemler için) ile assertion zaman aşımı (`expect`) Playwright'ta ayrı ayrı yapılandırılabilir; varsayılanları farklıdır (`expect` için 5 sn). Bir context 20 saniyelik aksiyon bütçesiyle kurulmuşsa ama assertion'lar hâlâ 5 saniyede pes ediyorsa, sunucudan dönen normal ama biraz gecikmeli bir geçiş "hata" gibi görünür — oysa sayfa hâlâ çalışmaktadır. Çözüm, `expect`'i aksiyonlarla aynı süreyi kullanacak şekilde merkezi olarak yapılandırmaktır (`playwrightExpect.configure({ timeout })`). Böylece assertion'lar ile aksiyonlar aynı "sabır" bütçesini paylaşır ve aralıklı, yanlış-negatif başarısızlıklar ortadan kalkar.

# pw-5 | Temel
## Soru
Bir test kırıldığında yalnızca son andaki bir ekran görüntüsüne bakmak yerine Playwright'ın trace özelliğini (`context.tracing.start/stop`) kullanmanın kattığı temel değer nedir?
## A
Trace, testi otomatik olarak yeniden koşturup hatayı kendi kendine düzeltir
🤖 Trace bir gözlemlenebilirlik (observability) aracıdır, testi otomatik düzeltme yeteneği yoktur.
## B
Trace yalnızca ağ isteklerini kaydeder; DOM durumu, ekran görüntüsü ve konsol logları trace dosyasına dahil edilmez
🤖 Trace hem DOM snapshot'larını hem ekran görüntülerini hem ağ isteklerini birlikte kaydeder.
## C
Trace koşumun tamamını adım adım saklar; kırılma anına kadar olan süreç geriye sarılıp incelenebilir
🤖 Doğru: trace, tek bir son-an görüntüsünün aksine tüm koşum geçmişini gözlemlenebilir kılar.
## D
Trace yalnız headed modda kayıt yapabilir, headless modda hiç çalışmaz
🤖 Trace kaydı headless modda da çalışır; mod, trace özelliğinin çalışıp çalışmayacağını belirlemez.
## Doğru
C
## Açıklama
Tek bir ekran görüntüsü yalnızca testin **kırıldığı andaki** durumu gösterir — o ana nasıl gelindiğini göstermez. Trace ise `tracing.start({ screenshots: true, snapshots: true })` ile başlatılıp `tracing.stop({ path })` ile dosyaya yazılan, koşumun **tamamını** — her adımdaki DOM anlık görüntüsünü, ağ isteklerini, konsol loglarını — saklayan bir kayıttır. Bir test kırıldığında trace görüntüleyicide geriye sarılıp, kararsızlığın nerede başladığı adım adım izlenebilir. Bu yüzden trace'ler genelde yalnız başarısız senaryolarda saklanır (başarılı koşumlarda kimse bakmayacağı için dosya silinir) — disk maliyeti ile hata ayıklama değeri arasındaki dengeyi gösterir.

# pw-6 | Temel
## Soru
Bir e2e proje, ayrı bir test ortamı yerine doğrudan canlı (production) siteye karşı koşuyor. Bu tercihin getirdiği risklerden biri, testlerin bazen "yanlış" hata vermesidir (site değil, test tasarımı hatalı). Aşağıdakilerden hangisi bu riski **azaltmaya** yönelik doğru bir yaklaşımdır?
## A
Fiyat/stok gibi değişken verilerin beklenen değerlerini test dosyasına sabit yazıp her koşumda bu değerlerle karşılaştırmak
🤖 Sabit değer beklemek, canlı veri değiştiğinde testi gereksiz yere kırar; risk azaltmaz, artırır.
## B
Testleri yalnızca gece yarısı, sitenin en sakin ve verinin en az değiştiği saatte koşturmak
🤖 Koşum saati canlı veri değişkenliğini veya CDN/güvenlik engellerini ortadan kaldırmaz.
## C
Sabit tutar yerine göreli (delta) doğrulama kullanmak: adet artınca ara toplamın birim fiyat kadar arttığını kontrol etmek
🤖 Doğru: göreli doğrulama, canlı fiyatlar değişse bile hesaplama mantığının doğruluğunu sınar, mutlak değere bağımlı kalmaz.
## D
Tüm assertion'ları kaldırıp yalnızca sayfanın açıldığını kontrol etmek
🤖 Assertion'ları kaldırmak testin amacını ortadan kaldırır, sadece "hata görünmüyor" yanılgısı yaratır.
## Doğru
C
## Açıklama
Canlı bir siteye karşı test koşmanın doğal riski, verinin (fiyat, stok, kampanya) her an değişebilmesidir. Testi sabit bir değere ("ara toplam tam olarak 450 TL olmalı") göre yazmak, veri değiştiğinde testi kırar — ama bu kırılma bir **hata değil**, testin kırılgan tasarımıdır. Göreli (delta) doğrulama ise mutlak değere değil **hesaplama mantığına** bakar: "adet bir artınca ara toplam tam olarak birim fiyat kadar arttı mı?" sorusu, fiyat ne olursa olsun sınanabilir bir davranıştır. Bu yaklaşım, canlı ortamın kaçınılmaz değişkenliğini test tasarımının bir parçası olarak kabul eder, onunla savaşmaz.
