---
slug: gelir-operasyonlari-lisanslama-faturalama
baslik: "Gelir Operasyonları Alan Bilgisi: Edinim, Sipariş, Lisanslama, Faturalama ve Müşteri Onboarding"
kademe: İleri
sira: 296
etiketler: 11
---

## Konu anlatımı

İlanın görev tanımı tek cümlede duruyor: "Edinim, sipariş, lisanslama, faturalama ve müşteri onboarding sistemlerinin geliştirilmesinde sorumluluk almak" ve "gelir operasyonlarını destekleyen platformların modernizasyonu". Bu cümle bir teknoloji listesi değil, bir **alan** tarifidir. Mülakatta seni ayıracak şey C# ya da SQL bilgin değil, bu beş kelimenin arkasındaki zinciri kurabilmen olacak.

Bu modülde alan bilgisi dosyaları var: kart ödeme akışı `250`, ödeme güvenliği `251`, mutabakat ve takas `252`, kredi sistemleri `239`. Ama **yazılım lisanslama ve abonelik faturalama** hiçbirinde yok. Boşluk bu dosyayla kapanıyor. Deseni `239`'dan alıyoruz: önce kavramlar, sonra zincir, sonra sözlü prova.

Baştan iki şerh. Birincisi: burada Logo Yazılım'ın iç mimarisi hakkında hiçbir iddia yok. Anlatılan şey, ilan metninden çıkarılabilen ve bu işi yapan her şirkette benzer duran **genel yapıdır**. Mülakatta "sizde şöyle çalışıyordur" cümlesi kurma; "bu tip sistemlerde genelde şu ayrım olur, sizde nasıl?" cümlesi kur. İkincisi: kıdemli adayın farkı halkaları saymak değildir. Halkaları herkes sayar. Fark, **halkalar arası tutarsızlığı** konuşabilmektir — "sipariş oldu ama lisans olmadı" cümlesinin arkasındaki tasarımı anlatabilmek.

### Zincir: yedi halka, altı devir

Gelir operasyonu tek bir sistem değildir. Birbirine kayıt devreden halkalar zinciridir: müşteri → edinim (acquisition) → sipariş (order) → lisans/yetkilendirme (entitlement) → faturalama (billing) → tahsilat (collection) → gelir (revenue). Her halkanın iki sorumluluğu vardır: kendi kaydını üretmek ve bir sonrakine devredeceği referansı net bırakmak.

Zincirin en tehlikeli yeri halkaların içi değil, **aralarıdır**. Her devir bir sistem sınırıdır; sistem sınırı olan her yerde kısmi başarısızlık vardır.

```svg
<svg viewBox="0 0 660 392" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gelir operasyonlari zinciri: musteri, onboarding, siparis, lisans, faturalama, tahsilat ve gelir halkalari ile aralarinda devredilen kayitlar">
  <defs>
    <marker id="gelirop-zincir-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Gelir zinciri: her halka bir kayıt üretir, bir referans devreder</text>
  <text x="110" y="38" text-anchor="middle" font-size="9.5" font-weight="700" fill="#5D7789" font-family="system-ui">HALKA</text>
  <text x="330" y="38" text-anchor="middle" font-size="9.5" font-weight="700" fill="#5D7789" font-family="system-ui">BİR SONRAKİNE NE DEVREDER</text>
  <text x="550" y="38" text-anchor="middle" font-size="9.5" font-weight="700" fill="#5D7789" font-family="system-ui">NEREDE TUTARSIZLAŞIR</text>
  <line x1="444" y1="44" x2="444" y2="378" stroke="#C9D4DC" stroke-width="1"/>
  <rect x="14" y="46" width="192" height="36" rx="8" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.3"/>
  <text x="110" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">1 · MÜŞTERİ ADAYI</text>
  <text x="110" y="76" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">deneme, talep, kanal</text>
  <text x="220" y="62" font-size="9.5" fill="#1C3247" font-family="system-ui">aday kaydı ve ilgi verisi</text>
  <text x="220" y="76" font-size="8.5" fill="#5D7789" font-family="system-ui">henüz sözleşme yok, kimlik zayıf</text>
  <text x="452" y="68" font-size="9" fill="#B23B3B" font-family="system-ui">aynı firma iki ayrı aday kaydı</text>
  <path d="M110 82 L110 93" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#gelirop-zincir-arw)"/>
  <rect x="14" y="95" width="192" height="36" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="110" y="111" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">2 · EDİNİM + ONBOARDING</text>
  <text x="110" y="125" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">sözleşme, kurulum, veri taşıma</text>
  <text x="220" y="111" font-size="9.5" fill="#1C3247" font-family="system-ui">tekil müşteri kimliği (master data)</text>
  <text x="220" y="125" font-size="8.5" fill="#5D7789" font-family="system-ui">vergi kimliği, ünvan, adres, kanal</text>
  <text x="452" y="117" font-size="9" fill="#B23B3B" font-family="system-ui">CRM kimliği ≠ fatura kimliği</text>
  <path d="M110 131 L110 142" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#gelirop-zincir-arw)"/>
  <rect x="14" y="144" width="192" height="36" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="110" y="160" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">3 · SİPARİŞ</text>
  <text x="110" y="174" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">teklif → sipariş → onay</text>
  <text x="220" y="160" font-size="9.5" fill="#1C3247" font-family="system-ui">sipariş kalemi: ürün, adet, fiyat, dönem</text>
  <text x="220" y="174" font-size="8.5" fill="#5D7789" font-family="system-ui">değişmez kayıt; değişiklik yeni kayıttır</text>
  <text x="452" y="166" font-size="9" fill="#B23B3B" font-family="system-ui">sipariş güncellenir, iz kaybolur</text>
  <path d="M110 180 L110 191" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#gelirop-zincir-arw)"/>
  <rect x="14" y="193" width="192" height="36" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="110" y="209" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">4 · LİSANS / YETKİLENDİRME</text>
  <text x="110" y="223" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">entitlement + lisans anahtarı</text>
  <text x="220" y="209" font-size="9.5" fill="#1C3247" font-family="system-ui">modül, koltuk sayısı, başlangıç, bitiş</text>
  <text x="220" y="223" font-size="8.5" fill="#5D7789" font-family="system-ui">ürünün "ne kullanabilir" cevabı burada</text>
  <text x="452" y="215" font-size="9" fill="#B23B3B" font-family="system-ui">sipariş var, yetkilendirme yok</text>
  <path d="M110 229 L110 240" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#gelirop-zincir-arw)"/>
  <rect x="14" y="242" width="192" height="36" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="110" y="258" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · FATURALAMA</text>
  <text x="110" y="272" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">dönem, oransal hesap, vergi</text>
  <text x="220" y="258" font-size="9.5" fill="#1C3247" font-family="system-ui">fatura belgesi: numara, tarih, matrah</text>
  <text x="220" y="272" font-size="8.5" fill="#5D7789" font-family="system-ui">resmî belgedir, silinmez</text>
  <text x="452" y="264" font-size="9" fill="#B23B3B" font-family="system-ui">faturalanan ≠ yetkilendirilen</text>
  <path d="M110 278 L110 289" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#gelirop-zincir-arw)"/>
  <rect x="14" y="291" width="192" height="36" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="110" y="307" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">6 · TAHSİLAT</text>
  <text x="110" y="321" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">ödeme, yeniden deneme, askı</text>
  <text x="220" y="307" font-size="9.5" fill="#1C3247" font-family="system-ui">tahsilat kaydı ve fatura eşleşmesi</text>
  <text x="220" y="321" font-size="8.5" fill="#5D7789" font-family="system-ui">brüt / net ayrımı burada da geçerli (285)</text>
  <text x="452" y="313" font-size="9" fill="#B23B3B" font-family="system-ui">tahsil edilmedi, erişim hâlâ açık</text>
  <path d="M110 327 L110 338" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#gelirop-zincir-arw)"/>
  <rect x="14" y="340" width="192" height="36" rx="8" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.3"/>
  <text x="110" y="356" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">7 · GELİR</text>
  <text x="110" y="370" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">gelir tanıma, dönemleme</text>
  <text x="220" y="356" font-size="9.5" fill="#1C3247" font-family="system-ui">hizmetin verildiği döneme yazılan gelir</text>
  <text x="220" y="370" font-size="8.5" fill="#5D7789" font-family="system-ui">peşin tahsilat önce ertelenmiş gelirdir</text>
  <text x="452" y="362" font-size="9" fill="#B23B3B" font-family="system-ui">tahsilatı gelir sanmak</text>
</svg>
```

### Birinci ve ikinci halka: edinim ve müşteri onboarding

**Edinim (acquisition)** aday müşteriyi sözleşmeli müşteriye çeviren süreçtir. Yazılım tarafında en görünür biçimi **deneme sürümüdür (trial)**: süreli, çoğu zaman kısıtlı bir yetkilendirme verilir; süre bitince ya ücretli yetkilendirmeye dönüşür ya da kapanır. Denemenin sistemdeki karşılığı ayrı bir ürün değil, **bitiş tarihi olan ve dönüşüm bekleyen bir yetkilendirme kaydıdır**. Bunu böyle modellemek, "deneme kullanıcısı" için ayrı bir kod yolu açmayı gereksiz kılar.

**Onboarding**, sözleşme imzalandıktan sonra müşterinin ürünü fiilen kullanmaya başlamasına kadar geçen süreçtir: hesap açılışı, kurulum, kullanıcı tanımları, eski sistemden **veri taşıma (migration)** ve eğitim. Buradaki iş metriği **ilk değere ulaşma süresidir (time to value)** — sözleşmeden ilk anlamlı kullanıma kaç gün. Bu metrik teknik bir metrik gibi durmaz ama doğrudan yenilemeyi etkiler; onboarding'i uzayan müşteri ikinci yıl gitmez.

Bu iki halkanın asıl teknik sorunu **ana veridir (master data)**: aynı müşteri CRM'de, sipariş sisteminde, lisans sunucusunda ve faturada aynı kimlikle mi duruyor? Pratikte cevap çoğu zaman hayırdır. CRM'de "Örnek A.Ş.", faturada "ÖRNEK ANONİM ŞİRKETİ", lisans sunucusunda bir e-posta adresi. Kıdemli adayın burada söylemesi gereken cümle şudur: **her sistemin kendi kimliği olabilir, ama bir tanesi otorite olmalı ve diğerleri ona eşlenmelidir.** Eşleme tablosu (dış sistem adı + dış kimlik → iç müşteri kimliği) küçük bir tablodur; olmadığında zincirin tamamı raporlanamaz hâle gelir.

Türkiye bağlamında tekilleştirmenin doğal anahtar adayı **vergi kimlik numarasıdır** — ama tek başına yetmez: aynı vergi kimliği altında birden çok şube, birden çok sözleşme, farklı fatura adresleri olabilir. Yani müşteri, sözleşme ve fatura hesabı üç ayrı kavramdır; birini diğerinin yerine kullanan model ilk büyük müşteride çöker.

### Üçüncü halka: sipariş

Sipariş zincirin **ticari niyeti** taşıyan halkasıdır. Tipik akış: **teklif (quote)** → sipariş → onay → **gerçekleştirme (fulfilment)**. Teklif pazarlığa açıktır, sipariş değildir. Onay, indirim yetkisi ya da tutar eşiğine bağlı olabilir.

Sipariş kaydının içinde şunlar bulunur: **sipariş kalemi (order line)** — hangi ürün, kaç adet/koltuk, hangi **fiyat listesinden (price book)** hangi birim fiyatla, hangi indirimle; ve **sözleşme dönemi (term)** — başlangıç, bitiş, yenileme biçimi. Fiyat listesinin sürümlü olması önemlidir: bugünkü fiyat değişse bile geçmiş siparişin fiyatı **o günkü listeden** okunabilmelidir.

Buradaki en kritik tasarım kararı şudur: **sipariş değişmez bir olay kaydıdır.** Müşteri altı ay sonra koltuk sayısını artırdığında var olan sipariş satırının adedi güncellenmez; **yeni bir değişiklik siparişi (amendment)** yazılır. Yükseltme (upgrade), düşürme (downgrade) ve iptal aynı mantıkla yeni kayıtlardır. Sebebi muhasebe ve denetimdir: "1 Mart'ta ne satın alınmıştı?" sorusunun cevabı, kaydın üzerine yazan bir sistemde kaybolur. Bu refleks `250` ve `285`'teki refleksin aynısıdır — geçmişi değiştirme, yeni kayıt ekle.

### Dördüncü halka: lisanslama

Bu bölüm dosyanın en özgün yeridir, çünkü modülün başka hiçbir yerinde anlatılmıyor ve ilanın merkezinde duruyor.

**Yetkilendirme kaydı (entitlement) ile lisans anahtarı (license key) aynı şey değildir.** Ayrım şudur:

- **Yetkilendirme kaydı**, sunucu tarafında duran veridir: bu müşteri, bu sözleşme kapsamında, şu modülleri, şu kadar koltukla, şu tarihe kadar kullanabilir. Sorgulanabilir, değiştirilebilir, denetlenebilir. **Tek doğruluk kaynağı budur.**
- **Lisans anahtarı**, o yetkilendirmenin taşınabilir, **imzalı** bir özetidir. Müşteriye verilen, kuruluma girilen, çevrimdışı çalışmayı mümkün kılan şeydir.

Anahtarın imzası **asimetrik** kurulur: üretici özel anahtarla imzalar, ürün genel anahtarla doğrular. Böylece ürünün içindeki doğrulama kodu ele geçse bile yeni anahtar **üretilemez** — yalnızca doğrulanabilir. Simetrik bir sır kullanmak, ürünün her kopyasına anahtar basmak demektir.

Peki doğrulama nerede yapılır? İkisi de yapılır ama otorite tektir. **İstemci tarafındaki doğrulama bir kolaylıktır**: çevrimdışı çalışmayı, hızlı açılışı ve ağ kesintisine dayanmayı sağlar. Ama istemci ortamı düşmanın elindedir — saat geri alınabilir, ikili dosya değiştirilebilir. Bu yüzden **koltuk sayımı, aktivasyon, iptal ve süre uzatma gibi kararlar sunucuda verilir.** Ürün periyodik olarak sunucuya "hâlâ yetkili miyim?" diye sorar; sunucu ulaşılamazsa imzalı anahtarla ve **zarafetle bitiş süresi (grace period)** kadar çalışmaya devam eder, sonra kısıtlı kipe düşer. Doğru tasarımın cümlesi şudur: **çevrimdışı çalışmak sunucuyu otorite olmaktan çıkarmaz, yalnız kararı erteler.**

Diğer kavramlar:

- **Koltuk (seat) yönetimi:** kaç eşzamanlı kullanıcı, kaç tanımlı kullanıcı? İkisi farklı sayımdır ve sözleşmede hangisinin yazdığı bilinmeden kod yazılamaz. Sayım sunucuda tutulur; istemcinin bildirdiği sayıya güvenilmez.
- **Süreli (subscription) ve süresiz (perpetual) lisans:** süresiz lisansta kullanım hakkı biter tarihi yoktur ama **bakım/yükseltme hakkı (maintenance)** ayrı ve sürelidir. Yani "lisansı var" cümlesi tek başına "yeni sürümü kurabilir" demek değildir; bu ikisini tek alanda tutan model klasik hatadır.
- **Aktivasyon ve devretme:** anahtar bir kuruluma bağlanır (makine parmak izi, kurulum kimliği). Donanım değişince **devretme (rehost)** gerekir. Devretme sayısı sınırlı olur; sınırsız olursa lisans anlamsızlaşır, sıfır olursa destek yükü patlar.
- **Modül bazlı yetkilendirme:** ürün tek parça satılmaz; muhasebe modülü var, bordro yok. Yetkilendirme kaydı bu yüzden bir bayrak değil, **bir kümedir**.

Kıdemli adayın kapanış cümlesi: lisansın tek doğruluk kaynağı yetkilendirme kaydıdır; anahtar onun imzalı bir kopyasıdır ve **kopya asla otorite değildir**.

### Beşinci halka: faturalama ve abonelik

Abonelik faturalamanın çekirdeği **ücretlendirme takvimidir (billing cycle)**: aylık mı yıllık mı, dönem başında mı peşin (advance) yoksa dönem sonunda mı (arrears) faturalanıyor, yenileme otomatik mi. Yazılım aboneliğinde yaygın olan, dönem başında peşin faturalamadır — bu tercih gelir tanımayı doğrudan etkiler (aşağıda).

Asıl teknik konu **oransal ücretlendirmedir (proration)**: dönem ortasında koltuk eklenirse ne olur? Cevap üç parçalıdır ve üçünü birden söyleyen aday ayrışır:

1. **Hesap.** Eklenen koltuklar dönemin tamamı için değil, **kalan gün oranında** ücretlendirilir: kalan gün / dönem gün sayısı × birim fiyat × eklenen adet. Gün mü ay mı sayılacağı, ilk günün dâhil olup olmadığı ve kalan günün hangi saat diliminde hesaplandığı **yazılı kural** olmalıdır; varsayıma bırakılırsa iki sistem iki farklı sayı üretir.
2. **Belgeleme.** Bu fark hemen ayrı bir fatura olarak mı kesilir, yoksa bir sonraki dönem faturasına ek satır olarak mı yazılır? İkisi de geçerlidir; **karar iş birimindir**, geliştiricinin değil. Nakit akışı isteyen şirket hemen keser, belge sayısını azaltmak isteyen biriktirir.
3. **Sonraki dönem.** Yenilemede taban artık yeni koltuk sayısıdır. Yani oransal hesap tek seferliktir; kalıcı etki abonelik satırının kendisindedir.

Düşürme (downgrade) simetrik değildir ve bu bir tuzaktır. Koltuk azaltıldığında peşin alınmış para için genelde **hemen iade yapılmaz**; ya alacak/kredi olarak bir sonraki döneme taşınır ya da değişiklik dönem sonunda yürürlüğe girer. "Ekleme oransal, çıkarma dönem sonu" kuralı sektörde çok yaygındır ve bunu bilmek alan içinden konuştuğunu gösterir.

Para tarafında `285`'in kuralları aynen geçerlidir: tutar ondalık duyarlı tiple taşınır (`decimal` / `BigDecimal`), tutar ile para birimi birlikte taşınan bir **değer nesnesidir**, ondalık basamak sayısı para biriminden okunur ve yuvarlamanın hangi adımda yapılacağı iş kararıdır (`226`). Oransal hesapta yuvarlama özellikle acıtır: satır satır yuvarlayıp toplamak ile toplayıp yuvarlamak farklı sonuç verir ve fark faturada görünür.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Abonelik donemi zaman cizelgesi: donem basi pesin fatura, donem ortasinda koltuk eklenmesi ve oransal hesap, donem sonunda yenileme">
  <defs>
    <marker id="gelirop-abone-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Abonelik dönemi: peşin fatura, dönem ortası değişiklik, yenileme</text>
  <line x1="40" y1="76" x2="618" y2="76" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gelirop-abone-arw)"/>
  <line x1="60" y1="58" x2="60" y2="94" stroke="#25804C" stroke-width="2.2"/>
  <text x="60" y="50" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">DÖNEM BAŞI</text>
  <text x="60" y="110" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">10 koltuk</text>
  <text x="60" y="123" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">peşin faturalandı</text>
  <line x1="330" y1="58" x2="330" y2="94" stroke="#B07D20" stroke-width="2.2" stroke-dasharray="4 3"/>
  <text x="330" y="50" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">DÖNEM ORTASI · +5 KOLTUK</text>
  <text x="330" y="110" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yetkilendirme anında güncellenir</text>
  <text x="330" y="123" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">faturalama takvimi değişmez</text>
  <line x1="600" y1="58" x2="600" y2="94" stroke="#186399" stroke-width="2.2"/>
  <text x="600" y="50" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">YENİLEME</text>
  <text x="600" y="110" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">taban 15 koltuk</text>
  <rect x="14" y="140" width="204" height="94" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="116" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Oransal hesap</text>
  <text x="28" y="181" font-size="9.5" fill="#1C3247" font-family="system-ui">kalan gün ÷ dönem günü</text>
  <text x="28" y="198" font-size="9.5" fill="#1C3247" font-family="system-ui">× birim fiyat × eklenen adet</text>
  <text x="28" y="217" font-size="9" fill="#5D7789" font-family="system-ui">gün sayma kuralı yazılı olmalı</text>
  <text x="28" y="230" font-size="9" fill="#B23B3B" font-family="system-ui">yuvarlama tek yerde uygulanır</text>
  <rect x="228" y="140" width="204" height="94" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Belgeleme: iki seçenek</text>
  <text x="242" y="181" font-size="9.5" fill="#1C3247" font-family="system-ui">· hemen ara fatura kesilir</text>
  <text x="242" y="198" font-size="9.5" fill="#1C3247" font-family="system-ui">· sonraki faturaya ek satır</text>
  <text x="242" y="217" font-size="9" fill="#5D7789" font-family="system-ui">ikisi de geçerli; seçim</text>
  <text x="242" y="230" font-size="9" fill="#5D7789" font-family="system-ui">iş biriminin kararıdır</text>
  <rect x="442" y="140" width="204" height="94" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="544" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Muhasebe etkisi</text>
  <text x="456" y="181" font-size="9.5" fill="#1C3247" font-family="system-ui">tahsilat peşin, gelir değil</text>
  <text x="456" y="198" font-size="9.5" fill="#1C3247" font-family="system-ui">kalan aylara yayılır</text>
  <text x="456" y="217" font-size="9" fill="#5D7789" font-family="system-ui">ek koltuk da kalan döneme</text>
  <text x="456" y="230" font-size="9" fill="#5D7789" font-family="system-ui">yayılarak tanınır</text>
  <rect x="14" y="246" width="632" height="42" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="265" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Simetri yoktur: ekleme oransal ücretlenir, çıkarma çoğu sözleşmede dönem sonunda yürürlüğe girer</text>
  <text x="330" y="281" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">peşin alınmış tutar için iade yerine alacak kaydı taşınır — kural sözleşmeden okunur, koda gömülmez</text>
</svg>
```

### Türkiye bağlamı: e-fatura, e-arşiv ve belgenin dokunulmazlığı

Bu bölüm Logo'nun asıl işine en yakın yerdir, o yüzden ayrı ağırlık taşıyor. Ama önce şerh: **Türkiye'de e-belge mevzuatı ve GİB kuralları değişir.** Kapsam eşikleri, zorunluluk tarihleri, senaryo adları ve teknik şemalar dönem dönem güncellenir. Mülakatta kesin mevzuat iddiası kurma; kavramı doğru anlat ve "güncel kural GİB tebliğinden ve entegratörün teknik dokümanından okunur" de. Bu cümle bilgisizlik değil, alanla çalışmış olmanın işaretidir — `285`'te kesim saati için kurulan duruşun aynısıdır.

Kavram düzeyinde bilinmesi gerekenler:

- **e-fatura ile e-arşiv fatura ayrımı.** Kabaca: alıcı da sistemde kayıtlıysa belge doğrudan taraflar arasında elektronik olarak dolaşır; kayıtlı değilse belge yine elektronik üretilir ve saklanır ama alıcıya farklı yolla iletilir. Uygulamada bunun anlamı şudur: **fatura kesmeden önce alıcının durumunu sorgulaman gerekir** ve bu bir dış servis çağrısıdır — zaman aşımı, yeniden deneme ve önbellek konuları buradadır.
- **Belge numarası bir dizidir.** Fatura numaralandırma serbest değildir; seri ve sıra düzeni vardır, boşluk bırakmak sorun üretir. Bu yüzden numara üretimi rastgele kimlik üretimi gibi düşünülemez; **eşzamanlılık altında boşluksuz sıra** üretmek gerçek bir tasarım problemidir ve çoğu zaman veritabanı düzeyinde çözülür.
- **Fatura silinmez.** Yanlış kesilen belge kayıttan kaldırılmaz. Süresi içinde ve şartları uygunsa reddedilir/iptal edilir; değilse **iade faturası** ya da düzeltme belgesi ile ters kayıt oluşturulur. Bu tam olarak `285`'teki iade mantığıdır: düzeltme geçmişi değiştirerek değil, yeni belge ekleyerek yapılır.
- **Muhasebe dönemi kapanır.** Kapanmış bir döneme geriye dönük kayıt atılamaz; düzeltme cari döneme yazılır. Yazılımcı açısından sonucu şudur: "tarihi geriye alıp yeniden hesaplayalım" çözümü bu alanda **yasak bir çözümdür**.
- **Entegratör bağımlılığı.** Belge çoğu zaman özel entegratör ya da doğrudan GİB servisleri üzerinden gönderilir. Bu dış sistemdir: yavaştır, kesilir, kuyruk biriktirir. Gönderim **eşgüçlü (idempotent)** olmalı ve gönderim durumu kendi tarafında ayrı bir durum makinesiyle izlenmelidir — "gönderildi mi?" sorusunun cevabı asla tek bir boolean değildir.

### Altıncı ve yedinci halka: tahsilat ve gelir

**Tahsilat** başarılı olduğunda sorun yoktur; alanı öğreten şey başarısız olduğunda ne yapıldığıdır. Bunun adı **dunning**: başarısız ödemenin kademeli takibi. İyi kurulmuş bir dunning takvimi şu parçalardan oluşur — belirli aralıklarla yeniden deneme (kart limiti geçici olabilir), her denemede müşteriye bildirim, belirli bir gecikmeden sonra **askıya alma (suspension)**, daha sonra **kapatma (termination)**. Kritik ayrım: askıya alma yetkilendirmeyi **geçici olarak** kapatır ve veriyi korur; kapatma sözleşmeyi bitirir. Bu ikisini tek durumla modellemek, ödeyip geri dönen müşteriyi kaybetmek demektir.

**Gelir tanıma (revenue recognition)** kıdemli adayı ayıran kavramdır. Kural basit ama etkisi büyüktür: **paranın tahsil edilmiş olması, o parayı gelir yazmak anlamına gelmez.** Ocak ayında peşin alınan on iki aylık abonelik bedeli, ocak ayının geliri değildir. Tahsil edildiği anda büyük bölümü **ertelenmiş gelirdir (deferred revenue)**; hizmet verildikçe, yani aylar geçtikçe gelire dönüşür. Müşteri altıncı ayda iptal ederse kalan kısım gelir olarak hiç yazılmamıştır.

Bunun yazılıma yansıması şudur: fatura tablosu tek başına gelir raporu üretemez. Faturadan bağımsız bir **tanıma planı** gerekir — hangi tutar, hangi dönemlere, hangi oranla yayılacak. Mülakatta "biz bunu muhasebeye bırakırız" demek zayıf cevaptır; doğru cevap "tanıma planını veri modelinde ayrı tutarız, muhasebe kuralını iş birimi verir" demektir.

### Halkalar arası tutarsızlık — teknik omurga

Şimdi asıl mesele. Senaryo: **sipariş oluştu, lisans üretilemedi.** Bu, bu alandaki mülakatların en sık sorulan tasarım sorusudur ve senin Java/mikroservis geçmişin tam burada avantaj.

Zayıf cevap: "iki işlemi tek dağıtık işleme alırım." Bu cevap zayıftır çünkü sipariş servisi ile lisans servisi ayrı sınırlardır; iki fazlı işlem her iki tarafı da birbirine kilitler, ortak koordinatöre bağımlı kılar ve tek bir servisin yavaşlaması diğerini durdurur. Dış servis (entegratör, ödeme, kimlik) araya girdiğinde zaten mümkün de değildir.

Güçlü cevap **olay tabanlı telafi** üzerine kurulur ve parçaları modülde zaten anlatılmıştır — burada onları tekrar anlatmıyoruz, **gelir zincirine uyguluyoruz**:

- **Eşgüçlülük anahtarı (idempotency key)** — `130`, `253`. Lisans üretim isteği "sipariş numarası + kalem numarası" gibi doğal bir anahtar taşır. Aynı istek beş kez gelse tek yetkilendirme oluşur. Tekillik uygulama kontrolüyle değil, **veritabanı benzersizlik kısıtıyla** garanti edilir; "önce bak, yoksa yaz" iki eşzamanlı tüketicide yarışa açıktır.
- **Giden kutusu (outbox)** — `130`, `116`. "Siparişi yaz, sonra olay yayımla" arada süreç düşerse iki kaynağı ayırır. Olay, sipariş kaydıyla **aynı veritabanı işleminde** giden kutusu tablosuna yazılır; ayrı bir süreç okuyup yayımlar.
- **Saga ve telafi işlemi** — `126`. Zincir uzun ve her adım geri alınabilir olmadığı için, geri alma yerine **telafi** yazılır: lisans üretilemezse sipariş "lisans bekliyor" durumuna alınır, faturalama tetiklenmez, operasyona düşer. Fatura kesilmişse artık silinemez — telafi bir iade/düzeltme belgesidir.
- **Yeniden deneme ve ölü mektup kuyruğu** — `122`. Geçici hata yeniden denenir, kalıcı hata ölü mektup kuyruğuna düşer ve **sahibi tanımlı** bir kuyruğa dönüşür. Bakılmayan kuyruk kuyruk değil birikimdir (`285`).
- **Nihai tutarlılık ve görünürlük.** Sipariş ile lisans arasında saniyeler sürebilen bir boşluk olması kabul edilebilir; **boşluğun görünmemesi** kabul edilemez. Bu yüzden zincirin her adımında durum alanı ve zaman damgası tutulur, uçtan uca bir **ilişkilendirme kimliği (correlation id)** taşınır.
- **Denetim izi (audit trail).** Kim, ne zaman, hangi kaydı neden değiştirdi. Bu alanda denetim izi bir "güzel özellik" değil, faturanın ve lisansın hukuki dayanağıdır.

```svg
<svg viewBox="0 0 660 356" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Siparis olustu lisans uretilemedi senaryosunun telafi akisi: giden kutusu, esguclu istek, yeniden deneme, olu mektup kuyrugu ve telafi islemi">
  <defs>
    <marker id="gelirop-telafi-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="gelirop-telafi-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"Sipariş oldu, lisans olmadı" — telafi akışı</text>
  <rect x="14" y="34" width="272" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="150" y="53" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">SİPARİŞ SERVİSİ · TEK İŞLEM</text>
  <rect x="26" y="62" width="120" height="42" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="86" y="80" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">sipariş kaydı</text>
  <text x="86" y="95" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">değişmez, sürümlü</text>
  <rect x="154" y="62" width="120" height="42" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="214" y="80" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">giden kutusu satırı</text>
  <text x="214" y="95" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">aynı işlemde (116)</text>
  <path d="M286 74 L308 74" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#gelirop-telafi-arw)"/>
  <rect x="312" y="46" width="148" height="56" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="386" y="68" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">YAYIMCI SÜREÇ</text>
  <text x="386" y="86" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">okur, yayımlar, işaretler</text>
  <path d="M460 74 L482 74" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#gelirop-telafi-arw)"/>
  <rect x="486" y="42" width="160" height="64" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="566" y="62" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">LİSANS SERVİSİ</text>
  <text x="566" y="79" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">anahtar: sipariş no + kalem</text>
  <text x="566" y="94" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">benzersizlik kısıtı (130)</text>
  <path d="M566 106 L566 128" fill="none" stroke="#25804C" stroke-width="1.6" marker-end="url(#gelirop-telafi-arw)"/>
  <rect x="470" y="132" width="176" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="558" y="150" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">BAŞARILI · yetkilendirme oluştu</text>
  <text x="558" y="164" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">faturalama tetiklenir</text>
  <path d="M496 106 L360 128" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#gelirop-telafi-red)"/>
  <rect x="150" y="132" width="204" height="40" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="252" y="150" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">BAŞARISIZ · yeniden deneme</text>
  <text x="252" y="164" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">üstel geri çekilme, sınırlı sayıda</text>
  <path d="M252 172 L252 192" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#gelirop-telafi-red)"/>
  <rect x="150" y="196" width="204" height="40" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="252" y="214" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">ÖLÜ MEKTUP KUYRUĞU (122)</text>
  <text x="252" y="228" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">sahibi tanımlı, izlenen kuyruk</text>
  <path d="M150 216 L128 216" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#gelirop-telafi-red)"/>
  <rect x="14" y="132" width="128" height="104" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="78" y="152" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B07D20" font-family="system-ui">TELAFİ (126)</text>
  <text x="24" y="171" font-size="8.5" fill="#1C3247" font-family="system-ui">· sipariş "lisans bekliyor"</text>
  <text x="24" y="187" font-size="8.5" fill="#1C3247" font-family="system-ui">· faturalama tetiklenmez</text>
  <text x="24" y="203" font-size="8.5" fill="#1C3247" font-family="system-ui">· operasyona görev düşer</text>
  <text x="24" y="219" font-size="8.5" fill="#B23B3B" font-family="system-ui">· fatura kesildiyse iade</text>
  <text x="24" y="232" font-size="8" fill="#5D7789" font-family="system-ui">belgesiyle, silmeyle değil</text>
  <rect x="14" y="248" width="632" height="44" rx="10" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.4"/>
  <text x="330" y="267" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Uçtan uca ilişkilendirme kimliği + denetim izi: her adımda durum, zaman damgası ve sebep</text>
  <text x="330" y="283" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">boşluk olması kabul edilebilir; boşluğun görünmemesi kabul edilemez</text>
  <rect x="14" y="300" width="632" height="44" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="319" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Neden dağıtık işlem (iki fazlı taahhüt) değil?</text>
  <text x="330" y="335" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ayrı sınırları birbirine kilitler, koordinatöre bağımlı kılar ve dış entegratör devredeyken zaten kurulamaz</text>
</svg>
```

### Modernizasyon bağı

İlanın ikinci yarısı "gelir operasyonlarını destekleyen platformların modernizasyonu" diyor. Bu platformlar tipik olarak eskidir ve eski olmalarının bir sebebi vardır: para ve belge üretirler, bu yüzden kimse kolay kolay dokunmaz. Modernizasyon stratejisi, kesme kenarı (strangler fig), sözleşme testleri ve aşamalı geçiş konuları `293`'te; veri tarafındaki karşılıkları `292`'de; kod incelemesi ve denetlenebilirlik tarafı `295`'te. Hazırlık haritasının bütünü `291`'de.

Mülakatta bu bağı kurmanın en güçlü cümlesi şudur: **"Bu alanda modernizasyonun zor kısmı teknoloji değil, kayıt sürekliliğidir."** Yeni sistem eskisinin ürettiği fatura numarasını, lisans anahtarını ve sözleşme dönemini tanımak zorundadır; tanımıyorsa geçiş bir teknoloji projesi değil, bir mutabakat felaketidir.

### "Gelir operasyonları zincirini anlatır mısınız?"

**Soru aslında ne ölçüyor?**

Bu, alanın açılış sorusudur ve genellikle ilk sorulur. Görüşmeci tanım duymak istemiyor; zinciri **uçtan uca görmüş** birini arıyor. Ayrım ilk otuz saniyede çıkar. Zayıf aday modülleri sayar: "CRM var, sipariş var, fatura var." Güçlü aday halkaları **devirlerle** anlatır: her halkanın ne ürettiğini ve bir sonrakine ne bıraktığını söyler. İkinci ayrıştırıcı, adayın zincirin sonuna kadar gidip gitmediğidir — tahsilatta durup gelir tanımaya hiç değinmeyen aday, zincirin son halkasını görmemiş demektir.

**Sözlü cevap iskeleti**

1. **Zinciri tek nefeste say.** Müşteri, edinim ve onboarding, sipariş, lisans/yetkilendirme, faturalama, tahsilat, gelir. Yedi halka, altı devir.
2. **Her halkanın çıktısını adlandır.** Onboarding tekil müşteri kimliği üretir, sipariş sipariş kalemi üretir, lisans yetkilendirme kaydı üretir, faturalama belge üretir, tahsilat ödeme kaydı üretir, gelir dönemlere yayılan bir kayıt üretir.
3. **İki kavramı erken ayır.** Yetkilendirme kaydı ile lisans anahtarı ayrı şeylerdir; biri otorite, diğeri imzalı kopyadır. Bu ayrımı ilk iki dakikada söylemek alan içinden konuştuğunu gösterir.
4. **Müşteri kimliğini sorun olarak koy.** Aynı müşteri CRM'de, siparişte ve faturada aynı kimlikle mi duruyor? Bir sistem otorite olmalı, diğerleri eşlenmelidir.
5. **Zincirin kırıldığı yeri kendin söyle.** "Halkaları herkes sayar; asıl mesele aralarındaki devirlerin kısmi başarısızlığıdır" cümlesini sen kur. Görüşmecinin bir sonraki sorusunu böylece sen seçmiş olursun.
6. **Son halkayı unutma.** Tahsilat gelir değildir. Peşin yıllık ödeme aylara yayılarak gelire dönüşür. Bu cümle kıdemli ile orta seviyeyi ayırır.

**Elenme sebepleri**

- Halkaları modül adı olarak saymak. Modül listesi ürün broşüründen okunur; devirler ancak sistemi bilenden duyulur.
- Yetkilendirme kaydı ile lisans anahtarını aynı şey sanmak. Bu, alanın en temel ayrımıdır ve karıştıran aday hemen ayrışır.
- Zinciri faturada bitirmek. Tahsilat ve gelir tanımaya hiç değinmemek, süreci muhasebeye kadar izlememiş olmak demektir.
- Müşteri kimliği tekilliğini hiç anmamak. Gerçek hayatta zincirin en sık kırıldığı yer burasıdır.
- Logo'nun iç yapısı hakkında iddia kurmak. Bilmediğin bir mimariyi anlatmak, ilk ayrıntı sorusunda dağılır.

**Kendi hikâyen**

Bu soruda anlatım gücün, zincirin hangi halkasına gerçekten dokunduğuna bağlı. Şu sorular onu çıkarır:

- Sözleşmeli bir müşteri kavramı olan bir sistemde çalıştın mı? Müşteri kaydı nerede otoriteydi?
- Sipariş, talep, başvuru ya da iş emri gibi bir "niyet kaydı" ürettin mi? Sonradan değiştirilebiliyor muydu, yoksa yeni kayıt mı açılıyordu?
- Kullanıcı sayısı, paket ya da yetki seviyesi kavramı olan bir üründe çalıştın mı? Yetkiler nerede tutuluyordu?
- Fatura, dekont, makbuz ya da resmî belge üreten bir akışa dokundun mu? Belge numarası nasıl üretiliyordu?
- Aynı müşterinin iki kayıtla göründüğü bir sorun yaşandı mı? Nasıl çözüldü?

Bu temasların hiçbiri yoksa uydurma. Zinciri kavram olarak temiz anlatmak ve "benim doğrudan çalıştığım halka şuydu" diyerek sınırı kendin çizmek, şişirilmiş bir iddiadan her zaman güçlüdür (`256`).

### "Sipariş oluştu ama lisans üretilemedi. Sistem ne yapmalı?"

**Soru aslında ne ölçüyor?**

Bu, bu ilan için sorulacak **en olası tasarım sorusudur** ve senin en güçlü olduğun yerdir. Ölçtüğü şey dağıtık sistem refleksidir: aday tutarsızlığı önlemeye mi çalışıyor, yönetmeye mi. Yazılım kariyerinin başında olan aday "işlemi tek transaction'a alırım" der. Kıdemli aday, sınırların ayrı olduğunu kabul eder ve tutarsızlığı **görünür, telafi edilebilir ve denetlenebilir** hâle getirir. Yukarıdaki diyagram bu cevabın omurgasıdır.

**Sözlü cevap iskeleti**

1. **Kabulle başla.** İki ayrı servis, iki ayrı veritabanı. Tek bir atomik işlem yok. Bu bir kusur değil, sınırın kendisi.
2. **Dağıtık işlemi neden seçmediğini söyle.** İki fazlı taahhüt iki tarafı birbirine kilitler, ortak koordinatöre bağımlı kılar ve dış entegratör devredeyken kurulamaz. Bunu *reddederek* söylemek, bilmediğinden değil, tercih ettiğinden söylediğini gösterir.
3. **Giden kutusunu getir.** Sipariş kaydı ile "lisans üret" olayı **aynı işlemde** yazılır; ayrı bir süreç yayımlar. Böylece "sipariş var, olay yok" durumu imkânsızlaşır (`116`, `130`).
4. **Eşgüçlülüğü doğal anahtara bağla.** Lisans üretim isteği sipariş numarası ve kalem numarasıyla tekilleşir; tekilliği veritabanı benzersizlik kısıtı garanti eder, uygulama kontrolü değil (`253`).
5. **Yeniden denemeyi sınırla.** Üstel geri çekilmeyle sınırlı sayıda dene; kalıcı hatayı ölü mektup kuyruğuna al ve o kuyruğun **sahibini** tanımla (`122`).
6. **Telafiyi anlat.** Sipariş "lisans bekliyor" durumuna geçer, faturalama tetiklenmez, operasyona görev düşer. Fatura çoktan kesildiyse silinmez; iade/düzeltme belgesiyle telafi edilir (`126`).
7. **Görünürlükle kapat.** Uçtan uca ilişkilendirme kimliği, her adımda durum ve zaman damgası, "lisans bekleyen sipariş sayısı" metriği ve yaş alarmı (`104`). Boşluk olabilir; boşluğun görünmemesi olamaz.

**Elenme sebepleri**

- "Dağıtık işlem açarım" deyip orada durmak. Alternatifleri anmayan aday, sınırları hiç yönetmemiş görünür.
- Yeniden denemeyi eşgüçlülük olmadan önermek. Tekilleşmeyen tekrar, çift lisans ve çift fatura üretir.
- Giden kutusu olmadan "önce yaz, sonra olay yayımla" demek. Süreç arada düşerse kayıt kalır, olay kaybolur.
- Ölü mektup kuyruğu tanımlamamak ya da tanımlayıp sahibini söylememek. Bakılmayan kuyruk sorunu geciktirir.
- Telafiyi "kaydı geri alırım" diye anlatmak. Fatura kesilmişse geri alınamaz; bu alanda geri alma değil ters kayıt vardır.

**Kendi hikâyen**

Bu soruda deneyimin büyük ihtimalle var; onu gelir zincirinin diline çevirmek yeterli:

- İki servis arasında "biri oldu, öbürü olmadı" durumu yaşadın mı? Nasıl fark edildi: müşteri, alarm, rapor?
- Giden kutusu deseni kullandın mı? Kullanmadıysanız çift yazma sorununu neyle kapattınız?
- Eşgüçlülük anahtarı ürettiğin bir uç nokta yazdın mı? Anahtar neydi, ne kadar süre saklanıyordu?
- Ölü mektup kuyruğunuz var mıydı? Oraya düşen kayıtlara kim, ne sıklıkta bakıyordu?
- Bir işlemi geri almak yerine telafi kaydı yazdığınız bir akış oldu mu?

Cevaplarından çıkan her "evet" bu soruda doğrudan kullanılabilir; alan farklı olsa da desen aynıdır ve bunu söylemek dürüst bir köprüdür (`256`).

### "Abonelikte dönem ortasında koltuk eklenirse faturalama nasıl çalışır?"

**Soru aslında ne ölçüyor?**

Bu soru abonelik faturalamasının turnusol testidir. Görüşmeci adayın "oransal ücretlendirme" kelimesini bilip bilmediğine değil, kararı **kaç parçaya böldüğüne** bakar. Zayıf aday tek bir formül söyler ve durur. Güçlü aday üç ayrı soruyu ayırır: hesap nasıl yapılır, belge ne zaman kesilir, muhasebeye nasıl yansır. Üçüncü ayrıştırıcı nokta simetri sorusudur: koltuk azaltılırsa aynı kural işler mi?

**Sözlü cevap iskeleti**

1. **Yetkilendirmeyi faturalamadan ayır.** Koltuk eklendiği anda yetkilendirme kaydı güncellenir ve kullanıcı hemen çalışır. Faturalama takvimi bundan bağımsızdır. Bu ayrımı yapmayan sistem, ödeme gelene kadar müşteriyi bekletir.
2. **Oransal hesabı ver.** Kalan gün / dönem gün sayısı × birim fiyat × eklenen adet. Formülü söylerken **gün sayma kuralının yazılı olması gerektiğini** ekle: ilk gün dâhil mi, hangi saat dilimi, ay bazlı mı gün bazlı mı.
3. **Yuvarlamayı iş kararı ilan et.** Satır satır yuvarlayıp toplamak ile toplayıp yuvarlamak farklı sonuç verir; kural tek yerde uygulanır (`226`, `285`).
4. **Belgeleme seçeneğini ikiye ayır.** Ya hemen ara fatura kesilir ya da bir sonraki dönem faturasına ek satır yazılır. İkisi de geçerlidir; seçim iş biriminindir. Bu cümle, kararı sahiplenmeden fark ettiğini gösterir.
5. **Sonraki dönemi söyle.** Oransal hesap tek seferliktir; yenilemede taban artık yeni koltuk sayısıdır.
6. **Simetriyi boz.** Düşürme çoğu sözleşmede oransal iade üretmez; ya alacak olarak taşınır ya da dönem sonunda yürürlüğe girer. Bunu bilmek alan içinden konuştuğunun işaretidir.
7. **Gelire bağla.** Ek koltuğun bedeli tahsil edildiği anda gelir değildir; kalan döneme yayılarak tanınır.

**Elenme sebepleri**

- Tek formül söyleyip durmak. Hesap, belge ve muhasebe üç ayrı karardır.
- Gün sayma kuralını varsaymak. İki sistem farklı sayarsa fatura ile yetkilendirme tutmaz.
- Yuvarlamayı hiç anmamak. Oransal hesap küsurat üretir; kural yoksa fark faturaya yansır.
- Ekleme ile çıkarmayı simetrik sanmak. "Koltuk azalınca iade ederiz" cümlesi sözleşmeyi okumamış izlenimi verir.
- Kararı kendi başına vermek. Belge biçimi, iade politikası ve eşikler iş birimi kararıdır.

**Kendi hikâyen**

- Dönemsel ücretlendirme yapan bir sistemde çalıştın mı? Aylık mı yıllık mı, peşin mi sonradan mı?
- Kullanıcı sayısına ya da kullanıma göre değişen bir fiyatlandırma kurdun mu?
- Fiyat listesi değiştiğinde geçmiş kayıtların fiyatı korunuyor muydu? Sürümleme var mıydı?
- Küsurat ya da yuvarlama yüzünden bir şikâyet aldın mı? Kök neydi?
- Ara dönem değişikliği yapılan bir sözleşme akışı gördün mü? Değişiklik yeni kayıt mıydı, güncelleme mi?

### "Lisans doğrulamasını nerede yaparsınız, neden?"

**Soru aslında ne ölçüyor?**

Bu soru güvenlik ile kullanılabilirlik arasındaki dengeyi ölçer. İki uçtaki cevap da zayıftır. "Her şeyi istemcide doğrularım" diyen aday tehdit modelini görmemiştir; "her açılışta sunucuya sorarım, ağ yoksa çalışmasın" diyen aday müşteriyi kaybettirir. Doğru cevap, **otorite ile kolaylığı ayırmaktır**. Görüşmeci ayrıca imzanın nasıl kurulduğuna bakar: simetrik sır öneren aday, ürünün her kopyasına anahtar bastığını fark etmemiştir.

**Sözlü cevap iskeleti**

1. **Otoriteyi ilan et.** Tek doğruluk kaynağı sunucudaki yetkilendirme kaydıdır. Lisans anahtarı onun imzalı kopyasıdır; kopya otorite değildir.
2. **İmzayı asimetrik kur.** Üretici özel anahtarla imzalar, ürün genel anahtarla doğrular. Ürünün içindeki doğrulama kodu okunsa bile yeni anahtar üretilemez.
3. **İstemci doğrulamasının işini tarif et.** İstemci imzayı ve kapsamı kontrol eder; bu bir **kolaylık ve dayanıklılık** katmanıdır, güvenlik sınırı değildir. İstemci ortamı düşmanın elindedir: saat geri alınabilir, ikili dosya değiştirilebilir.
4. **Kararı sunucuya bırak.** Koltuk sayımı, aktivasyon, devretme, iptal ve süre uzatma sunucuda kararlaştırılır. İstemcinin bildirdiği sayıya güvenilmez.
5. **Çevrimdışıyı çöz.** Ürün periyodik olarak sunucuya sorar; sunucu ulaşılamazsa imzalı anahtarla **zarafetle bitiş süresi** kadar çalışır, sonra kısıtlı kipe düşer. Süre bir parametredir, koda gömülmez.
6. **Kısıtlı kipi tarif et.** Doğru davranış aniden kapanmak değildir: veriye erişim korunur, yeni işlem kısıtlanır ve kullanıcı uyarılır. Müşteriyi ağ kesintisinde işsiz bırakan tasarım desteği patlatır.
7. **Denetimi ekle.** Aktivasyon, devretme ve doğrulama denemeleri kayıt altına alınır. Bu hem lisans ihlali tespiti hem destek için gerekir.

**Elenme sebepleri**

- "İstemcide doğrularım, yeter" demek. Bu, lisansın kolayca aşılabileceğini kabul etmektir.
- "Her açılışta sunucuya sorarım" deyip çevrimdışı senaryoyu düşünmemek. Kurumsal müşteride ağ her zaman yoktur.
- Simetrik sır ile anahtar üretmek. Sır ürünle birlikte dağıtılmış olur; ilk tersine mühendislikte biter.
- Zarafetle bitiş süresini hiç anmamak. Sert kapanış, gerçek müşteride veri kaybı ve destek krizi üretir.
- Süre, deneme sayısı gibi değerleri koda gömmek. Bunlar sözleşmeye göre değişen parametrelerdir.

**Kendi hikâyen**

- İmzalı token ya da imzalı veri üretip doğrulayan bir akış kurdun mu? Simetrik miydi, asimetrik mi?
- Çevrimdışı ya da ağ kesintili senaryo düşünmen gereken bir istemci oldu mu? Nasıl davranıyordu?
- Bir yetkinin süresinin bitmesi gereken bir sistem yaptın mı? Süre bitince ne oluyordu: sert kapanış mı, kademeli mi?
- Kurulum ya da cihaz bağlama kavramı olan bir üründe çalıştın mı?

### "Faturayı yanlış kestik, ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi değil **refleks** ölçer ve en hızlı eleyen sorudur — `285`'teki mutabakat farkı sorusunun kardeşidir. Görüşmeci tek bir şeye bakar: aday kaydı düzeltmeye mi koşuyor, belge disiplinini biliyor mu. "Faturayı güncelleriz" ya da "sileriz" cümlesi bu alanda anında elenmedir. İkinci ayrıştırıcı, adayın hatanın **nerede** olduğunu sorup sormadığıdır: tutar mı yanlış, alıcı mı yanlış, dönem mi yanlış — üçünün tedavisi farklıdır.

**Sözlü cevap iskeleti**

1. **Önce durdur, sonra sınıflandır.** Aynı hata başka faturalarda da var mı? Tek belge mi, toplu bir koşu mu? Toplu koşuysa ilk iş koşuyu durdurmaktır.
2. **Hatanın türünü ayır.** Yanlış tutar, yanlış alıcı, yanlış dönem, yanlış vergi oranı. Tedavileri farklıdır ve bazıları belge iptali gerektirmez, düzeltme belgesi gerektirir.
3. **Belgenin silinmeyeceğini net söyle.** Fatura resmî bir kayıttır. Kaydı silmek veya üzerine yazmak yoktur; süresi ve şartları uygunsa iptal/ret, değilse **iade faturası** ya da düzeltme belgesiyle ters kayıt oluşturulur.
4. **Mevzuata karşı doğru duruşu al.** "Süre ve şartlar mevzuata bağlıdır; güncel kural GİB tebliğinden ve entegratörün dokümanından okunur" de. Bu cümle, ezberden yanlış tarih vermekten çok daha güçlüdür.
5. **Muhasebe dönemini hatırlat.** Kapanmış döneme geriye dönük kayıt atılmaz; düzeltme cari döneme yazılır. "Tarihi geri alıp yeniden hesaplayalım" bu alanda yasak bir çözümdür.
6. **Zinciri geri besle.** Fatura yanlışsa kökü çoğu zaman daha yukarıdadır: sipariş kalemi, fiyat listesi ya da yetkilendirme. Belgeyi düzeltip kökü bırakmak aynı hatayı bir ay sonra tekrar üretir.
7. **Denetim izini kapat.** Kim, ne zaman, hangi gerekçeyle düzeltti; hangi belge hangi belgeyi iptal ediyor. İlişki iki yönlü kurulur ve raporlanabilir olur.

**Elenme sebepleri**

- "Faturayı güncelleriz" ya da "sileriz" demek. Bu alanda tek cümlelik elenme sebebidir.
- Mevzuat konusunda kesin ve yanlış iddia kurmak. Süre ve şart ezberden söylenmez.
- Kapanmış döneme geriye dönük kayıt önermek. Muhasebe disiplinini hiç görmemiş olmayı gösterir.
- Yalnız belgeyi düzeltip kökü aramamak. Aynı hata bir sonraki koşuda geri gelir.
- Düzeltme belgesi ile asıl belge arasında ilişki kurmamak. Denetimde iki ayrı belge, açıklamasız iki ayrı tutar olur.

**Kendi hikâyen**

- Resmî ya da yarı resmî bir belge üreten sisteme dokundun mu? Belge sonradan değiştirilebiliyor muydu?
- Yanlış üretilmiş bir kaydın düzeltilmesi gereken bir olay yaşadın mı? Güncelleme mi yapıldı, ters kayıt mı?
- Toplu bir koşunun hatalı çıktı ürettiği oldu mu? Nasıl fark edildi, koşu durduruldu mu?
- Denetim izi olan bir sistemde çalıştın mı? "Bunu kim, neden değiştirdi" sorusu cevaplanabiliyor muydu?

Bu soruda hikâyen küçük olabilir; yeter ki refleksin doğru olsun. "Kaydı silmedik, ters kayıt attık ve izini bıraktık" cümlesi bu alanda tek başına güçlü bir referanstır (`256`).

### Bu bölümden çıkan ders

Gelir operasyonu yedi halkalı bir zincirdir ve her halka bir sonrakine bir kayıt devreder: onboarding tekil müşteri kimliğini, sipariş değişmez bir sipariş kalemini, lisans halkası yetkilendirme kaydını, faturalama silinmeyen bir belgeyi, tahsilat ödeme kaydını, gelir halkası ise dönemlere yayılan bir tanıma kaydını üretir. Bu zincirin sözlüğünde üç ayrım kıdemi ele verir. Birincisi **yetkilendirme kaydı ile lisans anahtarının** ayrılması: biri sunucudaki otorite, diğeri imzalı ve çevrimdışı çalışmayı mümkün kılan kopyadır — ve kopya asla otorite olmaz. İkincisi **faturalama ile yetkilendirmenin** ayrılması: koltuk eklendiğinde yetki anında açılır, para oransal hesapla ayrı bir takvimde yürür ve ekleme ile çıkarma simetrik değildir. Üçüncüsü **tahsilat ile gelirin** ayrılması: peşin alınan yıllık bedel o ayın geliri değil, hizmet verildikçe tanınacak ertelenmiş gelirdir. Belge tarafında tek bir refleks vardır ve `285`'ten tanıdıktır: kayıt silinmez, geçmiş değiştirilmez, düzeltme yeni belgeyle yapılır; mevzuat sorulduğunda ezber tarih değil, "güncel kural GİB ve entegratör dokümanından okunur" denir. Zincirin kırıldığı yer ise halkaların içi değil aralarıdır; oraya dağıtık işlem değil, giden kutusu, eşgüçlülük anahtarı, telafi işlemi, ölü mektup kuyruğu ve denetim izi konur (`130`, `126`, `116`, `253`). Senin Java ve mikroservis geçmişin tam burada avantaja dönüşür: alan yeni olabilir ama halkalar arası tutarsızlığı yönetme disiplini yeni değil — ve mülakatta seni ayıracak cümle de bu olacak.

# gelirop-1 | Orta

## Soru
Müşterinin siparişi onaylandı ve faturası kesildi, ama kullanıcılar ürünün bordro modülünü açamıyor. Zincirin hangi halkası ve hangi kavram bu sorunun merkezindedir?

## A
Faturalama halkası eksik çalışmıştır; fatura kesilmiş ama ödeme alınmadığı için modül kilitli kalmıştır
🤖 tahsilat erişimi askıya alabilir ama bu ayrı bir karardır; burada modülün hiç yetkilendirilmemesi söz konusudur.

## B
Sipariş halkası eksiktir; sipariş kaydı olmadan hiçbir modül açılamaz, önce sipariş kalemi yazılmalıdır
🤖 sipariş zaten onaylanmış; sorun siparişin varlığında değil, siparişten türeyen kayıtta.

## C
Lisans/yetkilendirme halkasıdır; ürünün "ne kullanabilir" cevabı yetkilendirme kaydındaki modül kümesinden gelir
🤖 Doğru: siparişin ticari niyeti, ürünün okuyacağı yetkilendirme kaydına dönüşmeden modül açılmaz.

## D
Onboarding halkasıdır; kullanıcı tanımları tamamlanmadığı için modül görünmez, kurulum adımı tekrarlanmalıdır
🤖 kullanıcı tanımı ayrı bir eksikliktir; modülün tamamının kapalı olması yetkilendirme kapsamına işaret eder.

## Doğru
C

## Açıklama
Zincirde her halka bir kayıt üretir ve bir sonrakine devreder. Siparişin ürettiği şey **ticari niyettir**: hangi ürün, kaç koltuk, hangi dönem. Ürünün çalışma anında okuduğu şey ise sipariş değil, **yetkilendirme kaydıdır (entitlement)**: bu müşteri şu modülleri, şu koltuk sayısıyla, şu tarihe kadar kullanabilir. Modül bazlı yetkilendirme bu yüzden bir bayrak değil bir **kümedir** — "lisansı var" cümlesi tek başına "bordro modülünü açabilir" demek değildir. Fatura kesilmiş olması da bu tabloyu değiştirmez; faturalama ile yetkilendirme ayrı halkalardır ve ayrı takvimlerde yürür. Klasik arıza tam olarak budur: sipariş oldu, fatura oldu, aradaki yetkilendirme üretimi başarısız oldu ve kimse fark etmedi. Bu yüzden zincirde "faturalanan ≠ yetkilendirilen" durumu bir metrik olarak izlenir. A şıkkı tahsilat kaynaklı askıya almayı tarif eder; o ayrı ve bilinçli bir karardır. B şıkkı zaten var olan halkayı eksik sayar. D şıkkı kullanıcı tanımı eksikliğini işaret eder ama bu, modülün tamamının kapalı olmasını açıklamaz.

# gelirop-2 | Orta

## Soru
Aynı kurumsal müşteri; CRM'de "Örnek A.Ş.", sipariş sisteminde farklı bir müşteri numarasıyla, faturada ise resmî ünvanıyla kayıtlı. Gelir raporu üç sistemde üç farklı sonuç veriyor. En yerinde çözüm hangisidir?

## A
Bir sistem müşteri kimliğinde otorite ilan edilir; diğer sistemler dış kimlik → iç kimlik eşleme tablosuyla ona bağlanır
🤖 Doğru: her sistemin kendi kimliği olabilir, ama otorite tek olmalı ve eşleme açıkça tutulmalıdır.

## B
Üç sistemde de müşteri adı aynı biçimde yazılacak şekilde bir isim standardı tanımlanır ve eşleştirme ad üzerinden yapılır
🤖 ad bir doğal anahtar değildir; ünvan değişir, yazım farklılaşır ve iki farklı firma aynı ada sahip olabilir.

## C
Vergi kimlik numarası tek anahtar kabul edilip müşteri, sözleşme ve fatura hesabı tek kayıtta birleştirilir
🤖 aynı vergi kimliği altında birden çok sözleşme ve fatura hesabı olabilir; üç kavramı tek kayda indirmek ilk büyük müşteride kırılır.

## D
Raporlama katmanında her üç kaynaktan gelen veriler adres ve iletişim bilgisine göre eşleştirilir; kaynak sistemlere dokunulmaz
🤖 raporda yapılan tahmini eşleştirme sipariş, lisans ve faturayı birbirine bağlamaz; sorun kaynakta kalır.

## Doğru
A

## Açıklama
Bu, gelir zincirinin en sık kırıldığı yerdir ve adı **ana veri (master data)** sorunudur. Zincirdeki her halkanın kendi kimliği olması normaldir; kabul edilemez olan, hangisinin **otorite** olduğunun belirsiz kalmasıdır. Doğru tasarım iki parçalıdır: bir sistem müşteri kimliğinde otorite ilan edilir ve diğerleri için küçük ama açık bir eşleme tablosu tutulur — dış sistem adı, dış kimlik, iç müşteri kimliği. Bu tablo olmadan "bu müşteri bize ne kadar ödedi" sorusunun tek bir cevabı olmaz. Türkiye bağlamında **vergi kimlik numarası** güçlü bir doğal anahtar adayıdır ama tek başına yetmez: aynı vergi kimliği altında birden çok şube, birden çok sözleşme ve farklı fatura adresleri bulunabilir. Bu yüzden **müşteri, sözleşme ve fatura hesabı üç ayrı kavramdır**; C şıkkının hatası bu üçünü tek kayda indirmesidir. B şıkkı adı anahtar sayar; ad değişken bir alandır ve tekillik garantisi vermez. D şıkkı sorunu raporlama katmanına iterek kaynağı olduğu gibi bırakır; sipariş, lisans ve fatura kayıtları hâlâ birbirine bağlanamaz.

# gelirop-3 | İleri

## Soru
Yıllık peşin faturalanan bir abonelikte, dönemin 100. gününde 5 koltuk ekleniyor. Sistem nasıl davranmalıdır?

## A
Yeni koltuklar yenileme tarihine kadar ücretsiz kullanılır; fark bir sonraki yılın faturasında tam yıl olarak yansır
🤖 kalan dönem için hizmet verilmiştir; ücretlendirilmemesi geliri de sözleşmeyi de yanlış gösterir.

## B
Yıllık dönem kapatılıp yeni koltuk sayısıyla yeni bir yıllık dönem başlatılır; eski fatura iptal edilip yenisi kesilir
🤖 dönem sınırı korunur; peşin kesilmiş faturayı iptal etmek gereksiz bir belge hareketi ve muhasebe yükü üretir.

## C
Ek koltuklar için ayrı bir abonelik açılır ve kendi yıllık dönemiyle faturalanır; böylece oransal hesap gerekmez
🤖 dönemleri ayrışan koltuklar yenileme, iptal ve raporlamayı kalıcı olarak karmaşıklaştırır; oransal hesaptan kaçış bedeli ağırdır.

## D
Yetkilendirme anında güncellenir; kalan gün oranında ücret hesaplanır, belgeleme biçimi iş kuralına göre seçilir, yenilemede taban 15 koltuk olur
🤖 Doğru: yetki hemen, para oransal, belge kararı iş biriminin, sonraki dönem yeni tabandan.

## Doğru
D

## Açıklama
Dönem ortası değişiklik üç ayrı karardır ve üçünü ayıran aday ayrışır. **Birincisi yetkilendirme:** koltuk eklendiği anda yetkilendirme kaydı güncellenir ve kullanıcılar hemen çalışır; faturalama takvimi bunu beklemez. **İkincisi hesap:** eklenen koltuklar dönemin tamamı için değil, kalan gün oranında ücretlendirilir — kalan gün / dönem gün sayısı × birim fiyat × adet. Gün sayma kuralı (ilk gün dâhil mi, hangi saat dilimi) ve yuvarlamanın hangi adımda uygulanacağı **yazılı olmalıdır**; satır satır yuvarlayıp toplamak ile toplayıp yuvarlamak farklı sonuç verir (`226`, `285`). **Üçüncüsü belgeleme:** fark hemen ara faturayla mı kesilecek, yoksa bir sonraki dönem faturasına ek satır olarak mı yazılacak? İkisi de geçerlidir ve seçim iş biriminindir; nakit akışı isteyen hemen keser, belge sayısını azaltmak isteyen biriktirir. Oransal hesap tek seferliktir; kalıcı etki abonelik satırındadır, yani yenilemede taban 15 koltuktur. A şıkkı verilen hizmeti ücretsiz sayar. B şıkkı dönem sınırını bozar ve kesilmiş belgeyi gereksiz yere iptal eder. C şıkkı oransal hesaptan kaçarken dönemleri parçalar; yenileme ve iptal yönetimi kalıcı olarak zorlaşır.

# gelirop-4 | İleri

## Soru
Masaüstü kurulan bir ürünün lisans doğrulaması tasarlanıyor. Müşterilerin bir kısmı üretim sahasında, ağ erişimi kesintili ortamlarda çalışıyor. En yerinde tasarım hangisidir?

## A
Otorite sunucudaki yetkilendirme kaydıdır; ürün asimetrik imzalı anahtarla çevrimdışı çalışır, süre dolunca kısıtlı kipe düşer
🤖 Doğru: karar sunucuda, dayanıklılık istemcide; zarafetle bitiş süresi bir parametredir.

## B
Ürün her açılışta sunucuya sorar; sunucuya ulaşılamıyorsa çalışmayı reddeder, böylece hiçbir yetkisiz kullanım oluşmaz
🤖 güvenliği sağlar ama kesintili sahada ürünü kullanılamaz kılar; bu tasarım gerçek müşteride geri alınır.

## C
Doğrulama tamamen istemcide yapılır; anahtar ürünle birlikte dağıtılan bir sır ile üretilip aynı sırla doğrulanır
🤖 simetrik sır her kopyayla dağıtılmış olur; ilk tersine mühendislikte anahtar üretimi de mümkün hâle gelir.

## D
Anahtar üretimi ve doğrulaması sunucuda yapılır, istemci hiç doğrulama yapmaz; her işlem öncesi sunucudan izin alınır
🤖 her işlemde ağ bağımlılığı üretir; kesintili sahada ürün pratikte durur ve sunucu tek arıza noktası olur.

## Doğru
A

## Açıklama
Doğru tasarım **otorite ile kolaylığı ayırır**. Tek doğruluk kaynağı sunucudaki **yetkilendirme kaydıdır (entitlement)**; lisans anahtarı onun imzalı bir kopyasıdır ve kopya asla otorite olmaz. İmza **asimetrik** kurulur: üretici özel anahtarla imzalar, ürün genel anahtarla doğrular. Böylece ürünün ikili dosyası incelense bile yeni anahtar üretilemez — C şıkkının hatası tam buradadır; simetrik sır ürünün her kopyasıyla birlikte dağıtılmış demektir. İstemci tarafındaki doğrulama bir güvenlik sınırı değil, **dayanıklılık katmanıdır**: istemci ortamı düşmanın elindedir, saat geri alınabilir. Bu yüzden koltuk sayımı, aktivasyon, devretme ve iptal gibi kararlar sunucuda verilir; ürün periyodik olarak sorar, sunucu ulaşılamazsa **zarafetle bitiş süresi (grace period)** kadar imzalı anahtarla çalışır ve süre dolunca kısıtlı kipe düşer. Kısıtlı kip aniden kapanmak değildir: veriye erişim korunur, yeni işlem kısıtlanır, kullanıcı uyarılır. Süre bir parametredir; sözleşmeye göre değişir ve koda gömülmez. B ve D şıkları güvenliği kullanılabilirliğin tamamı pahasına satın alır; kesintili sahada ürünü durdurur ve sunucuyu tek arıza noktasına çevirir.

# gelirop-5 | Uzman

## Soru
Sipariş servisi siparişi yazdı, ancak lisans servisi yetkilendirme kaydını üretemedi ve hata kalıcı görünüyor. Fatura koşusu ise gece çalışacak. En yerinde tasarım hangisidir?

## A
Sipariş ve lisans yazma işlemleri iki fazlı taahhütle tek dağıtık işleme alınır; böylece tutarsızlık hiç oluşmaz
🤖 ayrı sınırları birbirine kilitler, ortak koordinatöre bağımlı kılar ve dış entegratör devredeyken zaten kurulamaz.

## B
Lisans üretimi başarısız olunca sipariş kaydı silinir ve müşteriden siparişi tekrar vermesi istenir; böylece kayıtlar tutarlı kalır
🤖 ticari niyeti yok eder ve denetim izini siler; bu alanda geri alma değil telafi ve ters kayıt vardır.

## C
Olay giden kutusuna sipariş kaydıyla aynı işlemde yazılır, istek doğal anahtarla eşgüçlüdür, kalıcı hata ölü mektuba düşer ve telafi siparişi "lisans bekliyor" durumuna alır
🤖 Doğru: tutarsızlık önlenmez, görünür ve telafi edilebilir hâle getirilir.

## D
Fatura koşusu her gece çalıştığı için sorun kendiliğinden kapanır; lisans üretimi bir sonraki koşuda tekrar denenir ve fatura zaten kesilmiş olur
🤖 yetkilendirmesi olmayan bir sipariş faturalanırsa telafi artık belge iptali gerektirir; sorun ucuzken pahalıya dönüşür.

## Doğru
C

## Açıklama
Bu, ilanın alanındaki en olası tasarım sorusudur ve doğru cevap tutarsızlığı **önlemek** değil **yönetmek** üzerine kuruludur. Sipariş servisi ile lisans servisi ayrı sınırlardır; iki fazlı taahhüt her iki tarafı da kilitler, ortak koordinatöre bağımlı kılar ve araya dış entegratör girdiğinde kurulamaz — A şıkkının zayıflığı bilgi eksikliği değil, tercih hatasıdır. Doğru omurga dört parçadır. **Giden kutusu (outbox):** "lisans üret" olayı sipariş kaydıyla aynı veritabanı işleminde yazılır, ayrı bir süreç yayımlar; böylece "sipariş var, olay yok" durumu imkânsızlaşır (`116`, `130`). **Eşgüçlülük:** istek sipariş numarası ve kalem numarası gibi doğal bir anahtar taşır ve tekillik veritabanı benzersizlik kısıtıyla garanti edilir; "önce bak, yoksa yaz" iki eşzamanlı tüketicide yarışa açıktır (`253`). **Yeniden deneme ve ölü mektup kuyruğu:** geçici hata üstel geri çekilmeyle sınırlı sayıda denenir, kalıcı hata sahibi tanımlı bir kuyruğa düşer (`122`). **Telafi:** sipariş "lisans bekliyor" durumuna alınır, faturalama tetiklenmez ve operasyona görev düşer (`126`). Kritik ayrıntı D şıkkındadır: yetkilendirmesi olmayan bir siparişi faturalamak, ucuz bir telafiyi belge iptali gerektiren pahalı bir telafiye çevirir. B şıkkı ise kaydı silerek denetim izini yok eder; bu alanda geri alma değil ters kayıt vardır (`285`). Kapanışta görünürlük gelir: uçtan uca ilişkilendirme kimliği, "lisans bekleyen sipariş" metriği ve kuyruktaki en eski kaydın yaşı için alarm (`104`).

# gelirop-6 | Uzman

## Soru
1 Ekim'de bir müşteriden 12 aylık abonelik bedeli peşin tahsil edildi. Finans ekibi ekim ayı gelir raporunu istiyor. Sistemin bu tutarı nasıl ele alması gerekir?

## A
Tahsilat gerçekleştiği için tutarın tamamı ekim ayının geliri olarak yazılır; iptal olursa sonraki ayda iade kaydıyla düzeltilir
🤖 tahsilat ile gelir aynı şey değildir; hizmet verilmeden gelir yazmak raporu ve iptal senaryosunu birlikte bozar.

## B
Tutarın tamamı ertelenmiş gelir olarak kaydedilir ve hizmet verildikçe aylara yayılarak gelire dönüşür
🤖 Doğru: peşin tahsilat önce yükümlülüktür; gelir, hizmetin verildiği döneme yazılır.

## C
Gelir, faturanın kesildiği değil ödemenin banka hesabına geçtiği tarihte ve tek seferde yazılır; dönemleme yalnız faturalama tarafını ilgilendirir
🤖 paranın hesaba geçme tarihi nakit akışını ilgilendirir; gelir tanıma hizmetin verildiği döneme bakar.

## D
Gelir hiç kaydedilmez, tutar sözleşme bitene kadar bekletilir ve on ikinci ayın sonunda tek kalemde gelire alınır
🤖 hizmet her ay veriliyor; geliri sona ertelemek de en az başa yazmak kadar yanlış bir dönemlemedir.

## Doğru
B

## Açıklama
**Gelir tanıma (revenue recognition)** bu alanda kıdemli adayı en net ayıran kavramdır ve kuralı tek cümledir: **paranın tahsil edilmiş olması, o parayı gelir yazmak anlamına gelmez.** Ekim ayında peşin alınan on iki aylık bedel ekim ayının geliri değildir; tahsil edildiği anda büyük bölümü **ertelenmiş gelirdir (deferred revenue)** — yani müşteriye karşı bir yükümlülüktür. Hizmet verildikçe, aylar geçtikçe bu tutar gelire dönüşür. Müşteri altıncı ayda iptal ederse kalan kısım gelir olarak hiç yazılmamış olur; A şıkkının ürettiği sorun tam budur, gelir önce şişer sonra iade kaydıyla geri alınır ve iki dönemin raporu birden bozulur. C şıkkı nakit akışı ile geliri karıştırır: paranın hesaba geçme tarihi ayrı bir gerçektir. D şıkkı ters yönde aynı hatayı yapar; hizmet her ay verildiği için geliri sona ertelemek de yanlış dönemlemedir. Bunun yazılıma yansıması nettir: **fatura tablosu tek başına gelir raporu üretemez.** Faturadan bağımsız bir **tanıma planı** gerekir — hangi tutar, hangi dönemlere, hangi oranla yayılacak. Dönem ortası koltuk eklemesinin oransal bedeli de aynı mantıkla kalan döneme yayılır. Mülakatta "bunu muhasebe halleder" demek zayıf cevaptır; güçlü cevap "tanıma planını veri modelinde ayrı tutarız, kuralı iş birimi verir" demektir.
