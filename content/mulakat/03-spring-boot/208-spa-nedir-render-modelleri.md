---
slug: spa-nedir-render-modelleri
baslik: "Tek Sayfa Uygulaması (SPA): CSR, SSR, SSG ve Hydration"
kademe: Temel
sira: 208
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Eski usul bir restoranda her yeni istek için garson mutfağa gidip **tabağın tamamını** yeniden getirir: çorba isteyince masadaki her şey toplanır, yepyeni bir servis kurulur. Klasik sunucu-render (Thymeleaf, JSP) böyle çalışır — her tıklamada sunucu **tam bir HTML sayfası** üretir, tarayıcı sayfayı baştan çizer, ekran bir an beyazlar.

Modern ön yüz ise masayı bir kez kurar, sonra yalnız **eksik malzemeyi** ister. İlk açılışta tarayıcıya neredeyse boş bir kabuk ile bir JavaScript paketi iner; o paket çalışıp arayüzü kurar. Sonrasında her tıklamada sunucudan sayfa değil **veri (JSON)** çekilir ve ekranın yalnız değişen parçası JS ile güncellenir. Buna **tek sayfa uygulaması** (single page application — SPA) denir. Sen arka uç (backend) geliştiricisisin; senden bunu yazman beklenmiyor. Ama sınırda seni doğrudan ilgilendiren üç sonucu var. Birincisi, sunucudan artık HTML değil JSON isteniyor. İkincisi, `curl` ile sayfayı çektiğinde içerik göremiyorsun. Üçüncüsü, derin bağlantılar (deep link) sunucu tarafında özel bir ayar istiyor.

### Temel kavramlar

- **Tek sayfa uygulaması (SPA — single page application)**: Tarayıcıya bir kez yüklenen, sonraki gezinmelerde tam sayfa yenilemeden ekranı JS ile güncelleyen uygulama.
- **Sunucu-render (server-rendered)**: Her istekte HTML'in sunucudaki şablon motoruyla (Thymeleaf/JSP) doldurulup gönderilmesi.
- **CSR (client-side rendering — istemci tarafı render)**: HTML'in tarayıcıda, inen JS paketi tarafından üretilmesi.
- **SSR (server-side rendering — sunucu tarafı render)**: HTML'in istek anında ön yüzün kendi çalışma zamanı sunucusunda (genelde Node) üretilmesi.
- **SSG (static site generation — statik site üretimi)**: HTML'in derleme zamanında bir kez üretilip hazır dosya olarak servis edilmesi.
- **Canlandırma (hydration)**: Sunucudan gelen hazır ama "ölü" HTML'e, tarayıcıda JS olay dinleyicilerinin (event listener) bağlanması.
- **Paket (bundle)**: Yüzlerce kaynak modülün birleştirilip tarayıcıya gönderilebilir hâle getirildiği çıktı dosyası.
- **Derleme (build)**: `npm run build` ile kaynak kodun `dist/` altında yayına hazır dosyalara dönüştürülmesi.
- **Ağaç sallama (tree shaking)**: Derleme sırasında hiç kullanılmayan kodun paketten ayıklanması.
- **Kod bölme (code splitting)**: Paketi parçalara ayırıp her sayfanın yalnız ihtiyacı olan parçayı indirmesi.
- **İstemci tarafı yönlendirme (client-side routing)**: Adres çubuğundaki URL'in sunucuya istek gitmeden JS tarafından değiştirilmesi.
- **Derin bağlantı (deep link)**: Uygulamanın iç bir ekranına doğrudan giden adres (`/urunler/42`) — yer imine eklenen ya da paylaşılan bağlantı.
- **Geri dönüş (fallback)**: Sunucunun tanımadığı bir yola 404 yerine `index.html`'i döndürme kuralı.

### Nasıl çalışır?

Klasik akışta tarayıcı `/urunler` ister, sunucu şablonu veriyle doldurup **dolu HTML** döner; kullanıcı bir bağlantıya tıklayınca aynı tur baştan işler. SPA'da ilk istekte gelen HTML neredeyse boştur: birkaç `<script>` etiketi ve içi boş bir kap.

```html
<!-- CSR bir SPA'nın sunucudan gelen ham yanıtı -->
<div id="root"></div>
<script type="module" src="/assets/app-4f9c1a.js"></script>
```

JS paketi indikten sonra çalışır, arayüzü kurar ve `/api/urunler` gibi uçlardan **JSON** çeker. Bundan sonra her tıklamada tam sayfa gitmez; yalnız veri gider gelir, DOM'un ilgili parçası güncellenir.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Klasik sunucu-render ile SPA istek akışının karşılaştırması">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her tıkta ne gidip geliyor?</text>
  <rect x="18" y="34" width="292" height="188" rx="10" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.4"/>
  <text x="164" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Klasik sunucu-render</text>
  <text x="164" y="74" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Thymeleaf / JSP</text>
  <rect x="36" y="86" width="94" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="83" y="105" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Tarayıcı</text>
  <rect x="198" y="86" width="94" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="245" y="105" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Sunucu</text>
  <path d="M130 96 L198 96" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <text x="164" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">GET /urunler</text>
  <path d="M198 110 L130 110" stroke="#186399" stroke-width="1.3" fill="none"/>
  <text x="164" y="124" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">TAM HTML sayfa</text>
  <text x="164" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her tıkta sayfa baştan çizilir</text>
  <text x="164" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ekran bir an beyazlar</text>
  <text x="164" y="200" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">ilk boyama hızlı, SEO kolay</text>
  <rect x="330" y="34" width="292" height="188" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="476" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SPA</text>
  <text x="476" y="74" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">React / Vue / Angular</text>
  <rect x="348" y="86" width="94" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="395" y="105" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Tarayıcı</text>
  <rect x="510" y="86" width="94" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="557" y="105" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">Sunucu</text>
  <path d="M442 96 L510 96" stroke="#5D7789" stroke-width="1.3" fill="none"/>
  <text x="476" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">GET /api/urunler</text>
  <path d="M510 110 L442 110" stroke="#25804C" stroke-width="1.3" fill="none"/>
  <text x="476" y="124" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">yalnız JSON veri</text>
  <text x="476" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ilk yükte boş kabuk + JS paketi</text>
  <text x="476" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sonra DOM'u JS günceller</text>
  <text x="476" y="200" text-anchor="middle" font-size="10" fill="#B67A1A" font-family="system-ui">paket ağır, ilk boyama gecikir</text>
</svg>
```

**CSR'de HTML'i tarayıcı üretir.** Bunun backend'çiyi doğrudan ilgilendiren sonucu şudur: sayfayı `curl` ile çektiğinde ekranda gördüğün metinlerin **hiçbirini** yanıt gövdesinde bulamazsın — yalnız boş bir `<div id="root">` vardır. "Sayfa çalışmıyor mu?" diye paniklemeden önce bunu bil. Bedeli de burada: arama motoru botu JS çalıştırmazsa içeriği göremez (SEO sorunu) ve kullanıcı, paket inip çalışana kadar boş ekrana bakar (ilk boyama gecikmesi).

**SSR'de HTML'i sunucu üretir** — ama senin Spring sunucun değil, ön yüzün **kendi Node sunucusu**. Backend geliştiricilerin en sık kaçırdığı nokta budur: SSR kullanan bir ön yüz artık "birkaç statik dosya" değildir; ayağa kaldırılması, izlenmesi, yeniden başlatılması gereken **ikinci bir çalışma zamanı süreci**dir. Dağıtım planı yaparken bunu hesaba katmayan ekipler yayına çıkarken şaşırır.

**SSG'de HTML derleme zamanında** üretilir: `npm run build` sırasında her sayfa için hazır bir `.html` dosyası yazılır, bunlar CDN'den servis edilir. İstek anında hiçbir üretim yoktur; en hızlı ve en ucuz seçenektir ama içerik ancak yeni bir derlemeyle tazelenir. Blog, dokümantasyon ve pazarlama sayfaları için biçilmiş kaftandır.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CSR SSR ve SSG karşılaştırması: HTML nerede ve ne zaman üretiliyor">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">HTML nerede üretiliyor?</text>
  <rect x="16" y="34" width="196" height="164" rx="10" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.5"/>
  <text x="114" y="58" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B67A1A" font-family="system-ui">CSR</text>
  <text x="114" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üretim yeri: TARAYICI</text>
  <text x="114" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">zaman: JS çalışınca</text>
  <text x="114" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">curl → boş kabuk</text>
  <text x="114" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">SEO zayıf</text>
  <text x="114" y="172" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">panel / iç uygulama</text>
  <rect x="222" y="34" width="196" height="164" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="58" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">SSR</text>
  <text x="320" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üretim yeri: NODE SUNUCU</text>
  <text x="320" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">zaman: her istekte</text>
  <text x="320" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">curl → dolu HTML</text>
  <text x="320" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrı süreç işletilir</text>
  <text x="320" y="172" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">e-ticaret / dinamik</text>
  <rect x="428" y="34" width="196" height="164" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="526" y="58" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">SSG</text>
  <text x="526" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üretim yeri: DERLEME</text>
  <text x="526" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">zaman: yayından önce</text>
  <text x="526" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">CDN'den hazır dosya</text>
  <text x="526" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">içerik derlemeyle tazelenir</text>
  <text x="526" y="172" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">blog / doküman</text>
</svg>
```

SSR ve SSG'nin ortak sorusu şudur: sunucudan gelen HTML **görünür** ama henüz **ölüdür** — üzerindeki butonun hangi fonksiyonu çağıracağı tarayıcıda bilinmiyordur. JS paketi indikten sonra çerçeve, ekrandaki mevcut HTML'i dolaşıp olay dinleyicilerini bağlar; bu adıma **canlandırma (hydration)** denir. Aradaki pencerede kullanıcı butonu **görür ama tıklaması hiçbir şey yapmaz**. (Kademe 7'de Playwright testlerinin ara ara başarısız olmasının kök nedeni de tam olarak budur: test, buton göründüğü an tıklar, canlandırma bitmediği için tıklama düşer.)

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hydration zaman çizgisi: HTML geldi, görünür ama tepkisiz, JS yüklendi, canlandı">
  <text x="320" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Canlandırma (hydration) zaman çizgisi</text>
  <rect x="24" y="40" width="592" height="34" rx="8" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="24" y="40" width="150" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="99" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">HTML geldi</text>
  <rect x="180" y="40" width="270" height="34" rx="8" fill="#FCEDEC" stroke="#B3261E" stroke-width="1.3"/>
  <text x="315" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3261E" font-family="system-ui">görünür ama TEPKİSİZ</text>
  <rect x="456" y="40" width="160" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="536" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">canlandı</text>
  <path d="M24 92 L616 92" stroke="#5D7789" stroke-width="1.2" fill="none"/>
  <path d="M99 84 L99 100" stroke="#186399" stroke-width="1.4" fill="none"/>
  <path d="M456 84 L456 100" stroke="#25804C" stroke-width="1.4" fill="none"/>
  <text x="99" y="116" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">ekranda içerik var</text>
  <text x="456" y="116" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">JS paketi indi ve çalıştı</text>
  <text x="315" y="140" text-anchor="middle" font-size="10.5" fill="#B3261E" font-family="system-ui">bu pencerede tıklama DÜŞER: olay dinleyicileri henüz bağlı değil</text>
  <text x="320" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">görünürlük ile etkileşilebilirlik AYNI AN değildir</text>
  <text x="320" y="188" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">otomatik testlerdeki kırılganlığın (flaky) sık nedeni</text>
</svg>
```

Yayına çıkarken kaynak kod olduğu gibi gitmez; **derleme** adımından geçer. `npm run build` komutu Vite ya da webpack gibi bir derleyiciyi çalıştırır. Bu araç dört iş yapar: yüzlerce modülü tek pakete birleştirir (bundling), hiç kullanılmayan kodu ayıklar (**ağaç sallama**), paketi sayfa başına parçalara böler (**kod bölme**) ve çıktıyı küçültür. Sonuç `dist/` klasöründe, adında bir özet (hash) taşıyan dosyalardır:

```bash
dist/
  index.html            # tek giriş noktası
  assets/app-4f9c1a.js  # hash değişince tarayıcı yeni dosyayı indirir
  assets/app-8b2e07.css
```

Dosya adındaki hash tesadüfi değildir: içerik değişince ad da değişir, böylece tarayıcı eski sürümü önbellekten sunmaya devam edemez (önbellek tazeleme — cache busting). Çıktı yine tarayıcının çalıştırdığı sıradan JS'tir; şifrelenmiş ya da ikili (binary) bir biçim değildir.

Son ve mülakatta en çok sorulan parça: **istemci tarafı yönlendirme**. SPA'da bir bağlantıya tıkladığında adres çubuğu `/urunler/42` olur ama **sunucuya hiçbir istek gitmez** — yolu tarayıcıdaki yönlendirici (router) çözer. Sorun kullanıcı o sayfadayken **F5'e bastığında** çıkar: bu kez tarayıcı gerçekten sunucuya `GET /urunler/42` gönderir, sunucu böyle bir yol tanımadığı için **404** döner. Uygulama içinden gezinmek çalışırken yenilemenin patlaması klasik belirtidir. Çözüm sunucu tarafında bir **geri dönüş (fallback)** kuralıdır: bilinen API yolları ve statik dosyalar dışındaki her isteğe `index.html` döndürülür, gerisini tarayıcıdaki yönlendirici halleder.

```bash
# nginx tarafında klasik geri dönüş kuralı
location / {
  try_files $uri $uri/ /index.html;
}
```

Peki her ekran SPA olmak zorunda mı? Hayır. Tek bir form, basit bir CRUD yönetim ekranı ya da içerik ağırlıklı bir sayfa için sunucu-render hâlâ tamamen meşrudur ve genelde daha ucuzdur. SPA'nın bedeli gerçektir: paket boyutu, ayrı bir derleme hattı, ayrı ekip/dil, SEO ve canlandırma dertleri. Ölçüt basittir — arayüzde çok sayıda **durum** (state) ve sayfa yenilemeden akan yoğun bir etkileşim varsa SPA kendini öder; yoksa ödemez. (SPA ile sunucu arasındaki API sözleşmesi ve durum kodları ayrı bir konudur; burada girmiyoruz.)

### Mülakatta ne sorulur?

- **"SPA nedir, klasik sunucu-render'dan farkı ne?"** → Klasikte her tıkta sunucu **tam HTML** döner; SPA'da ilk yükte kabuk + JS paketi iner, sonrasında sunucudan **veri (JSON)** çekilir ve DOM'u JS günceller. Yanlış cevap kalıbı: "SPA sitenin tek sayfadan oluşması" — hayır, ekran sayısı sınırsızdır, **tam sayfa yenileme** olmamasıdır kastedilen.
- **"CSR, SSR, SSG farkı?"** → Tek eksende cevapla: **HTML nerede ve ne zaman üretiliyor.** CSR → tarayıcıda, JS çalışınca. SSR → ön yüzün Node sunucusunda, her istekte. SSG → derleme zamanında, bir kez. Bu üçlüyü tek cümlede toparlaman kıdem işareti sayılır.
- **"SSR kullanıyoruz dediler, altyapıda ne değişir?"** → Ön yüz artık salt statik dosya değildir; **çalışan bir Node süreci** vardır — dağıtım, izleme, sağlık kontrolü, yeniden başlatma gerekir. Backend'çilerin en sık kaçırdığı nokta budur.
- **"CSR bir sayfayı `curl` ile çekince ne görürsün?"** → Boş kabuk: `<div id="root">` ve `<script>` etiketleri. İçerik yok. Sonuçları: arama motoru botu JS çalıştırmıyorsa içeriği göremez, kullanıcı da paket inene kadar bekler.
- **"Hydration nedir?"** → Sunucudan gelen hazır HTML'e tarayıcıda **olay dinleyicilerinin bağlanması**. Kritik ayrıntı: canlandırma bitene kadar sayfa **görünür ama tepkisizdir**; görünürlük ile etkileşilebilirlik aynı an değildir. Otomatik testlerin ara ara başarısız olmasının sık nedeni budur.
- **"SPA'da `/urunler/42` yenilendiğinde 404 alıyoruz, neden?"** → URL'i **istemci tarafı yönlendirici** yönetir; yenilemede istek gerçekten sunucuya gider ve sunucu o yolu tanımaz. Çözüm **geri dönüş**: bilinmeyen yolları `index.html`'e döndür (nginx'te `try_files $uri $uri/ /index.html`). Bu, backend'e sorulan en somut SPA sorusudur. Yanlış cevap kalıbı: "CORS sorunu" ya da "önbellek sorunu" demek.
- **"`npm run build` ne yapıyor?"** → Modülleri birleştirir, kullanılmayan kodu ayıklar (ağaç sallama), paketi parçalara böler (kod bölme), küçültür ve `dist/` altına adında hash taşıyan dosyalar yazar; hash **önbellek tazeleme** içindir. Çıktı yine tarayıcının çalıştırdığı JS'tir.
- **"Her yerde SPA mı kullanmalı?"** → Hayır. Basit form/CRUD ekranında sunucu-render meşru ve ucuzdur. SPA'nın bedeli (paket boyutu, karmaşıklık, SEO, ayrı derleme hattı) yalnız zengin ve durum yoğun arayüzlerde kendini öder.

### Tek cümlede

SPA, tarayıcıya bir kez yüklenen ve sonrasında sunucudan sayfa değil veri çeken bir uygulamadır. Farkı anlamanın anahtarı **HTML'in nerede üretildiğidir**: CSR'de tarayıcıda, SSR'de Node sunucusunda, SSG'de derleme anında. Arka uca düşen en somut ödev ise şudur: derin bağlantı yenilemelerini `index.html`'e döndüren geri dönüş kuralını kurmak.

# spa-1 | Temel
## Soru
Tamamen istemci tarafında render edilen (CSR) bir SPA'nın adresini `curl` ile çektiğinde yanıt gövdesinde ne görürsün?
## A
Sunucunun şablon motoruyla doldurduğu, ürün listesinin tamamını içeren hazır HTML; tarayıcıya yalnız çizmek kalır.
🤖 Bu klasik sunucu-render davranışıdır; CSR'de HTML'i sunucu doldurmaz.
## B
Hiçbir şey; CSR sayfaları HTML değil yalnız JSON döndürdüğü için `curl` boş bir yanıt ya da hata mesajı alır.
🤖 Sunucu yine bir HTML belgesi döndürür; yalnızca içeriği boştur.
## C
Neredeyse boş bir kabuk: birkaç `<script>` etiketi ve içi boş bir `<div id="root">`; metinlerin hiçbiri yok.
🤖 Doğru: HTML'i tarayıcıdaki JS ürettiği için ham yanıtta içerik bulunmaz.
## D
İçeriğin tamamı, ancak sunucu tarafından sıkıştırılmış ikili (binary) bir biçimde; yalnız tarayıcı bunu açabilir.
🤖 Yanıt sıradan metin HTML'dir; tarayıcıya özel gizli bir ikili biçim yoktur.
## Doğru
C
## Açıklama
CSR'de HTML'i **tarayıcı** üretir: sunucu yalnızca bir kabuk gönderir, içeriği inen JS paketi çalıştıktan sonra kurar. Bu yüzden `curl` gibi JS çalıştırmayan bir istemci sayfayı çektiğinde ekranda gördüğün metinlerin hiçbirini bulamaz — elinde boş bir `<div id="root">` ve birkaç `<script>` etiketi kalır. Sunucu-render (A) tam tersini yapar, HTML'i veriyle doldurup gönderir. Yanıt boş ya da hatalı değildir (B); geçerli bir HTML belgesidir, sadece içi boştur. Sıkıştırma varsa da bu standart HTTP içerik kodlamasıdır, tarayıcıya özel bir ikili biçim yoktur (D). Bunun iki bedeli vardır: JS çalıştırmayan arama motoru botu içeriği göremez (SEO) ve kullanıcı paket inene kadar boş ekrana bakar.

# spa-2 | Temel
## Soru
Ön yüz ekibi "uygulamayı SSR ile çalıştırıyoruz" dedi. Bunun altyapı açısından backend geliştiriciyi ilgilendiren asıl sonucu nedir?
## A
Ön yüzün de bir çalışma zamanı sunucusu (genelde Node) vardır; HTML'i o üretir, ayağa kaldırılıp izlenmesi gerekir.
🤖 Doğru: SSR'de ön yüz artık salt statik dosya yığını değildir.
## B
Ön yüz yalnızca statik dosyalardan ibaret kalır; tek gereken derlenmiş dosyaları bir CDN'e ya da nginx'e koymaktır.
🤖 Bu CSR/SSG dağıtımıdır; SSR'de sürekli çalışan bir sunucu süreci vardır.
## C
HTML'i artık Java uygulaması üretmeye başlar; Spring tarafında Thymeleaf şablonlarını baştan yazman gerekir.
🤖 SSR'de HTML'i ön yüzün kendi Node sunucusu üretir, Spring değil.
## D
Tarayıcı hiç JavaScript indirmez; sayfanın tamamı sunucudan geldiği için istemcide JS paketi hiç çalışmaz.
🤖 SSR'den sonra da paket inip sayfayı canlandırır (hydration).
## Doğru
A
## Açıklama
SSR'de HTML **istek anında sunucuda** üretilir — ama bu, senin Spring sunucun değil, ön yüzün **kendi Node çalışma zamanıdır**. Altyapı açısından sonuç nettir: dağıtılacak, izlenecek, sağlık kontrolü yapılacak ve yeniden başlatılacak **ikinci bir süreç** doğar; "ön yüz sadece birkaç statik dosyadır" varsayımı artık geçersizdir ve bu, backend geliştiricilerin en sık kaçırdığı noktadır. B, CSR ya da SSG dağıtımını tarif eder (orada gerçekten hazır dosyalar servis edilir). Spring'in Thymeleaf'ine geçiş söz konusu değildir (C); iki dünya birbirinin yerine geçmez. JS de ortadan kalkmaz (D): sunucudan gelen HTML görünür ama ölüdür, paket inip canlandırma (hydration) yapıldıktan sonra etkileşimli olur.

# spa-3 | Orta
## Soru
SPA'da `/urunler/42` sayfasındayken kullanıcı F5'e basınca 404 alıyor; oysa aynı sayfaya uygulama içinden tıklayarak gitmek sorunsuz çalışıyor. Sebep ve çözüm nedir?
## A
Sebep tarayıcı önbelleğidir; çözüm sunucu yanıtlarına `Cache-Control: no-store` başlığı ekleyip yenilemeyi zorlamaktır.
🤖 Sorun önbellek değil; sunucunun o yolu hiç tanımamasıdır.
## B
Sebep CORS engelidir; çözüm sunucuda o kaynağa `Access-Control-Allow-Origin` başlığını ekleyip ön isteği açmaktır.
🤖 CORS kaynaklar arası isteklerle ilgilidir; buradaki hata yol çözümlemesidir.
## C
Sebep JS paketinin geç yüklenmesidir; çözüm paketi `<head>` içinde eşzamanlı (senkron) bir etiketle indirmektir.
🤖 404 yanıtı JS hiç çalışmadan sunucudan gelir; yükleme sırası bunu değiştirmez.
## D
Sunucu bu yolu tanımaz; bilinmeyen yolları `index.html`'e döndüren bir geri dönüş kuralı tanımlanır.
🤖 Doğru: sunucu tarafı fallback olmadan derin bağlantı yenilemesi 404 verir.
## Doğru
D
## Açıklama
Uygulama içinden tıklayarak gezinirken URL'i **istemci tarafı yönlendirici** değiştirir ve sunucuya hiçbir istek gitmez — bu yüzden çalışır. F5'te ise tarayıcı gerçekten `GET /urunler/42` gönderir; sunucu böyle bir yol tanımadığı için **404** döner. Çözüm sunucu tarafında bir **geri dönüş (fallback)** kuralıdır: API yolları ve statik dosyalar dışındaki istekler `index.html` ile yanıtlanır, yolu tarayıcıdaki yönlendirici çözer (nginx'te `try_files $uri $uri/ /index.html`). Önbellek (A) burada rol oynamaz; sayfa hiç önbelleğe girmemiş olsa da aynı 404 gelir. CORS (B) kaynaklar arası isteklerin izin mekanizmasıdır, burada tek kaynak vardır. JS'in yükleme sırası (C) da fark etmez: 404 yanıtı tarayıcı tek satır JS çalıştırmadan önce gelir.

# spa-4 | Orta
## Soru
Bir pazarlama sitesinin sayfaları günde bir kez değişiyor ve arama motorunda üst sırada çıkması isteniyor. SSG'yi (statik site üretimi) diğer modellerden ayıran temel özellik nedir?
## A
HTML her istekte ön yüz sunucusunda yeniden üretilir; böylece her ziyaretçi en güncel veriyi görmüş olur.
🤖 Bu SSR'nin tanımıdır; SSG'de üretim istekten önce bitmiştir.
## B
HTML derleme zamanında bir kez üretilir ve hazır dosya olarak servis edilir; istek anında üretim yoktur.
🤖 Doğru: SSG'de üretim isteğe değil derlemeye bağlıdır.
## C
HTML tarayıcıda JS tarafından üretilir; sunucu yalnız boş kabuğu ve paketi gönderdiği için yanıt çok küçüktür.
🤖 Bu CSR'dir; SSG'de dosyanın içinde hazır HTML bulunur.
## D
HTML hiç üretilmez; arama motoru botları sayfayı JS çalıştırarak kendisi kurduğu için SEO sorunu ortadan kalkar.
🤖 SSG tam da hazır HTML üretmeye dayanır; işi bota bırakmaz.
## Doğru
B
## Açıklama
Üç modeli tek eksende ayırmak gerekir: **HTML nerede ve ne zaman üretiliyor.** SSG'de üretim **derleme zamanında** olur — `npm run build` sırasında her sayfa için hazır bir HTML dosyası yazılır ve bunlar CDN'den servis edilir; istek geldiğinde hiçbir şablon çalıştırılmaz. Bu yüzden hem en hızlı hem en ucuz seçenektir ve içerik hazır olduğu için SEO sorunsuzdur; bedeli, içeriğin ancak yeni bir derlemeyle tazelenmesidir — günde bir değişen bir pazarlama sitesi için bu tam uyar. A, SSR'yi tarif eder (her istekte Node sunucusunda üretim). C, CSR'yi tarif eder (üretim tarayıcıda). D ise SSG'nin varlık sebebini tersine çevirir: SSG işi bota bırakmaz, tam tersine botun doğrudan okuyabileceği hazır HTML'i önceden üretir.

# spa-5 | İleri
## Soru
SSR ile üretilen sayfa çok hızlı görünüyor ama kullanıcılar "ilk saniyelerde butona bastım, hiçbir şey olmadı" diyor. Bu davranışın teknik adı ve nedeni nedir?
## A
Canlandırma (hydration) bitmemiştir; HTML görünür ama JS olay dinleyicileri bağlanmadığı için tıklama düşer.
🤖 Doğru: görünürlük ile etkileşilebilirlik aynı an değildir.
## B
Sunucu yanıtı gecikmiştir; HTML tarayıcıya hiç ulaşmadan kullanıcı tıkladığı için istek oluşmamıştır.
🤖 Kullanıcı butonu görüyorsa HTML çoktan gelmiştir; sorun sonrasındadır.
## C
Tarayıcı CSS yüklenene kadar butonları devre dışı tutar; stil dosyası inince tıklamalar çalışmaya başlar.
🤖 CSS yalnız görünümü etkiler; olay dinleyicilerini CSS bağlamaz.
## D
Sunucu oturumu (session) henüz açılmamıştır; çerez yazılana kadar sunucu tıklama isteklerini reddeder.
🤖 Tıklama sunucuya hiç ulaşmıyor; sorun tümüyle istemci tarafındadır.
## Doğru
A
## Açıklama
SSR (ve SSG) sayfayı **erken görünür** kılar, çünkü hazır HTML gelir — ama o HTML "ölüdür": hangi butonun hangi fonksiyonu çağıracağı tarayıcıda henüz bilinmiyordur. JS paketi inip çalıştıktan sonra çerçeve mevcut HTML'i dolaşıp **olay dinleyicilerini bağlar**; bu adım **canlandırmadır (hydration)**. Arada kalan pencerede kullanıcı butonu görür ama tıklaması hiçbir şey yapmaz. Kilit cümle: **görünürlük ile etkileşilebilirlik aynı an değildir.** B yanlıştır, çünkü kullanıcı butonu görüyorsa HTML zaten gelmiştir. CSS (C) yalnız görünümü belirler; olay dinleyicisi bağlamaz. Oturum/çerez (D) da rol oynamaz, zira tıklama sunucuya hiç ulaşmaz. Aynı olgu otomatik arayüz testlerinin ara ara başarısız olmasının (flaky) sık nedenidir: test, buton göründüğü an tıklar.

# spa-6 | İleri
## Soru
`npm run build` çıktısında `dist/` içinde `app-4f9c1a.js` gibi adında özet (hash) taşıyan dosyalar görüyorsun. Vite/webpack gibi derleyicilerin yaptığı iş için en doğru ifade hangisidir?
## A
Kaynak dosyaları hiç değiştirmeden tek bir klasöre kopyalarlar; addaki hash yalnızca sürüm numarası yerine geçen bir etikettir.
🤖 Derleyici modülleri birleştirip dönüştürür; salt kopyalama yapmaz.
## B
JavaScript'i sunucuda çalışacak bir ikiliye (binary) derlerler; tarayıcı bu çıktıyı doğrudan çalıştıramaz, önce açması gerekir.
🤖 Çıktı yine tarayıcının doğrudan çalıştırdığı JS'tir; ikili derleme yoktur.
## C
Kodu şifreleyip yalnız kendi çalışma zamanlarının çözebileceği biçime getirirler; asıl amaç kaynak kodu rakiplerden gizlemektir.
🤖 Küçültme kodu okunaksızlaştırır ama şifreleme değildir; amaç boyut ve hızdır.
## D
Modülleri birleştirir, kullanılmayan kodu ayıklar ve parçalara böler; hash ise önbellek tazelemesi içindir.
🤖 Doğru: birleştirme, ağaç sallama ve kod bölme derleyicinin asıl işidir.
## Doğru
D
## Açıklama
Derleme adımı kaynak kodu yayına hazır hâle getirir: yüzlerce modülü **birleştirir** (bundling), hiç kullanılmayan kodu **ayıklar** (ağaç sallama — tree shaking), paketi sayfa/rota başına parçalara **böler** (kod bölme — code splitting) ve çıktıyı küçültür. Dosya adındaki **hash** tesadüfi değildir: içerik değişince ad da değişir, böylece tarayıcı eski sürümü önbellekten sunmaya devam edemez — buna önbellek tazeleme (cache busting) denir. Bu yüzden A yanlıştır: ortada salt kopyalama yoktur ve hash elle verilen bir sürüm etiketi değildir. B yanlıştır, çünkü çıktı hâlâ tarayıcının doğrudan yorumladığı JavaScript'tir; ikili bir biçim üretilmez. C de yanlıştır: küçültme (minification) kodu okunaksızlaştırır ama bu şifreleme değildir ve amaç gizlilik değil, indirilen bayt miktarını ve yükleme süresini azaltmaktır.
