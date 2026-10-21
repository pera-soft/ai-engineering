---
baslik: "Cloud Sağlayıcı Turu: AWS, GCP, Azure"
yazar: "Serpiştirme — Veri/dağıtım yanı"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 11
---

### Bu metin ne anlatıyor?
Eskiden bir program yayına almak için kendi bilgisayarını alıp bir odaya koyman gerekirdi.
Bugün bu makineleri kiralıyorsun: buna **bulut** (cloud) deniyor. Bu metin, bulutu satan üç
büyük şirketin (AWS, Google Cloud, Microsoft Azure) aynı işe nasıl farklı adlar verdiğini ve
altta yatan ortak fikirleri anlatıyor. Amaç ürün adı ezberlemek değil; "bana aslında hangi
yetenek lazım?" diye düşünebilmek ve satıcının reklamıyla gerçeği ayırabilmek.

### Bu metinde geçen temel terimler
- **Bulut sağlayıcı** (cloud provider): Sunucu, depolama ve ağ kapasitesini kiralayan şirket.
- **Sanal makine** (virtual machine, VM): Fiziksel bir bilgisayarın içinde çalışan, sana
  kendi bilgisayarınmış gibi görünen yazılım makine.
- **Yönetilen hizmet** (managed service): Kurulumunu, yedeğini, güncellemesini sağlayıcının
  üstlendiği hazır hizmet (ör. yönetilen bir veritabanı).
- **Bölge ve erişilebilirlik alanı** (region / availability zone): Sağlayıcının makinelerinin
  fiziksel olarak durduğu yer. *Bölge* bir coğrafya (ör. Frankfurt); *erişilebilirlik alanı*
  o bölgedeki birbirinden bağımsız veri merkezidir. Aynı bölgede iki alana yayılmak, tek bir
  binanın elektriği gittiğinde ayakta kalmanı sağlar.
- **SLA** (Service Level Agreement, hizmet düzeyi anlaşması): Sağlayıcının "hizmet şu oranda
  ayakta olacak" taahhüdü ve tutmazsa vereceği bedel (genelde para iadesi/kredi).
- **Çıkış ücreti** (egress): Veriyi buluta yüklemek çoğu zaman ücretsizdir; buluttan **dışarı
  indirmek** ücretlidir. Bu asimetri, ilerideki "taşınma" maliyetinin gizli bir parçasıdır.

## Neden bu özet
İyi bir mimar belirli bir bulut sağlayıcısına (cloud provider) körü körüne bağlanmadan,
sağlayıcıdan bağımsız düşünür: "nesne depolama (object storage) lazım" der, "S3 lazım"
demez. Ama bu bağımsızlığı koruyabilmek için üç büyük sağlayıcının aynı kavramı hangi
isimle sattığını **eşleyebilmek** gerekir. Bu özet o eşlemeyi ve altındaki ortak
kavramları kısaca verir — ezber için değil, "hepsi aynı fikrin farklı etiketi" sezgisini
kurmak için.

## Katmanlar: IaaS → PaaS → SaaS ve serverless
Bulut hizmetleri, senin ne kadarını yönettiğine göre üç ana soyutlama katmanına ayrılır:

- **IaaS** (Altyapı hizmet olarak / Infrastructure as a Service): Sana çıplak sanal makine
  verilir; işletim sistemini, çalışma ortamını, ölçeklemeyi sen kurarsın. En çok kontrol,
  en çok sorumluluk.
- **PaaS** (Platform hizmet olarak / Platform as a Service): Sana hazır bir çalışma
  platformu verilir; sadece kodunu bırakırsın, altyapıyla uğraşmazsın.
- **SaaS** (Yazılım hizmet olarak / Software as a Service): Hazır uygulamayı kullanırsın
  (ör. e-posta servisi); ne altyapı ne kod — sadece veri ve ayar senin.
- **Serverless** (Sunucusuz): Aslında sunucu vardır ama sen görmezsin; kodun yalnızca
  bir olay (event) geldiğinde çalışır, çalıştığı milisaniye kadar ödersin. Ölçekleme
  sağlayıcının işidir. PaaS'ın uç bir biçimi gibi düşünülebilir.

## Çekirdek hizmet aileleri
Sağlayıcı ne olursa olsun, ürün kataloğu birkaç çekirdek aile etrafında döner:

- **Hesaplama** (Compute): kodun çalıştığı sanal makineler / işlemci gücü.
- **Nesne depolama** (Object storage): dosya/blob saklama — resim, yedek, log; klasör
  değil "kova" (bucket) mantığı.
- **Veritabanı** (Database): yönetilen (managed) ilişkisel ya da NoSQL veri depoları.
- **Serverless işlev** (Serverless function): olayla tetiklenen küçük kod parçaları.
- **Ağ** (Networking): sanal özel ağ (VPC — Virtual Private Cloud, sana ayrılmış izole ağ
  bölmesi), yük dengeleyici (load balancer — gelen isteği birden çok sunucuya dağıtan
  bileşen), DNS (alan adını IP adresine çeviren defter), CDN (İçerik Dağıtım Ağı — statik
  dosyaları kullanıcıya coğrafi olarak yakın sunuculardan veren katman) — parçaları
  birbirine bağlayan ve dış dünyaya açan katman.
- **Kimlik ve erişim yönetimi** (IAM — Identity and Access Management): kimin neye
  erişebileceğini tanımlayan kural katmanı. Bulutta güvenlik hatalarının çoğu burada,
  fazla geniş verilmiş yetkilerde doğar.

## Üç sağlayıcı eşlemesi
Aynı çekirdek aileler, üç sağlayıcıda farklı ürün adlarıyla satılır. Aşağıdaki tablo
en sık karşılaşacağın dört aileyi yan yana koyar — bir sütunu bilmek, çoğu zaman
diğerlerini tahmin etmeye yeter.

```svg
<svg viewBox="0 0 680 272" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç bulut sağlayıcısının hizmet eşleme tablosu: hesaplama, nesne depolama, veritabanı ve serverless için AWS, GCP ve Azure karşılıkları">
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hizmet Eşleme Tablosu</text>
  <text x="340" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Aynı kavram · üç farklı ürün adı</text>
  <rect x="16" y="58" width="150" height="32" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="91" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Kavram</text>
  <rect x="170" y="58" width="160" height="32" rx="5" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.3"/>
  <text x="250" y="78" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">AWS</text>
  <rect x="334" y="58" width="160" height="32" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="414" y="78" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">GCP</text>
  <rect x="498" y="58" width="160" height="32" rx="5" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.3"/>
  <text x="578" y="78" text-anchor="middle" font-size="12.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Azure</text>
  <rect x="16" y="94" width="150" height="38" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="91" y="118" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Hesaplama</text>
  <rect x="170" y="94" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#C77A17" stroke-width="1"/>
  <text x="250" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">EC2</text>
  <rect x="334" y="94" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="414" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Compute Engine</text>
  <rect x="498" y="94" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1"/>
  <text x="578" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Virtual Machines</text>
  <rect x="16" y="136" width="150" height="38" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="91" y="160" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Nesne Depolama</text>
  <rect x="170" y="136" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#C77A17" stroke-width="1"/>
  <text x="250" y="160" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">S3</text>
  <rect x="334" y="136" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="414" y="160" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Cloud Storage</text>
  <rect x="498" y="136" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1"/>
  <text x="578" y="160" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Blob Storage</text>
  <rect x="16" y="178" width="150" height="38" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="91" y="202" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Veritabanı</text>
  <rect x="170" y="178" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#C77A17" stroke-width="1"/>
  <text x="250" y="202" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">RDS</text>
  <rect x="334" y="178" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="414" y="202" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Cloud SQL</text>
  <rect x="498" y="178" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1"/>
  <text x="578" y="202" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Azure SQL</text>
  <rect x="16" y="220" width="150" height="38" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="91" y="244" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Serverless</text>
  <rect x="170" y="220" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#C77A17" stroke-width="1"/>
  <text x="250" y="244" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Lambda</text>
  <rect x="334" y="220" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="414" y="244" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Cloud Functions</text>
  <rect x="498" y="220" width="160" height="38" rx="5" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1"/>
  <text x="578" y="244" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Azure Functions</text>
</svg>
```

**Tabloya iki şerh.** (1) Ürün adları değişir: Google, sunucusuz işlev hizmetini bir dönem
"Cloud Functions" diye satarken sonradan "Cloud Run functions" adı altında topladı; AWS ve
Azure'un da eş anlamlı görünen birden çok ürünü vardır. Adı ezberleme, **aileyi** ezberle;
adı gerektiğinde sağlayıcının kendi belgesinden doğrula. (2) Eşleme **kavram düzeyindedir,
özellik düzeyinde değil**: S3, Cloud Storage ve Blob Storage "nesne depolama" ailesindendir
ama tutarlılık davranışları, sürüm yönetimi, fiyat basamakları ve erişim kuralları birebir
aynı değildir. "Karşılığı var" demek "taşımak bedava" demek değildir.

## Sorumluluk merdiveni: IaaS → PaaS → SaaS
Katman yükseldikçe (IaaS'tan SaaS'a) **senin yönettiğin azalır, sağlayıcının yönettiği
artar**. Aşağıdaki iki renkli barlar bu takası gösterir: mavi = senin sorumluluğun,
yeşil = sağlayıcının sorumluluğu.

```svg
<svg viewBox="0 0 680 264" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sorumluluk merdiveni: IaaS'tan SaaS'a doğru senin yönettiğin kısım küçülür, sağlayıcının yönettiği kısım büyür">
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sorumluluk Merdiveni</text>
  <text x="340" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Sağa gittikçe: sen ↓ · sağlayıcı ↑</text>
  <rect x="470" y="52" width="14" height="14" rx="3" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="490" y="63" font-size="11" fill="#1C3247" font-family="system-ui">Sen yönetirsin</text>
  <rect x="580" y="52" width="14" height="14" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="600" y="63" font-size="11" fill="#1C3247" font-family="system-ui">Sağlayıcı</text>
  <rect x="70" y="80" width="130" height="105" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="135" y="137" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SEN</text>
  <rect x="70" y="185" width="130" height="45" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="135" y="212" text-anchor="middle" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">SAĞLAYICI</text>
  <text x="135" y="252" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">IaaS</text>
  <rect x="275" y="80" width="130" height="60" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="340" y="114" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SEN</text>
  <rect x="275" y="140" width="130" height="90" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="340" y="189" text-anchor="middle" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">SAĞLAYICI</text>
  <text x="340" y="252" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">PaaS</text>
  <rect x="480" y="80" width="130" height="26" rx="4" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="545" y="98" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">SEN</text>
  <rect x="480" y="106" width="130" height="124" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="545" y="172" text-anchor="middle" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">SAĞLAYICI</text>
  <text x="545" y="252" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">SaaS</text>
</svg>
```

## Paylaşılan sorumluluk (shared responsibility)
Buluta geçmek "her şey sağlayıcının işi" demek değildir. Sağlayıcı bulutun **kendisinin**
güvenliğinden sorumludur (donanım, ağ, fiziksel tesis); sen buluttaki **kendi işinin**
güvenliğinden sorumlusun (verin, kimin eriştiği, uygulama ayarların). Bir S3 kovasını
herkese açık bırakırsan bu senin hatan, sağlayıcının değil.

```svg
<svg viewBox="0 0 680 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Paylaşılan sorumluluk modeli: üstte sen veri, erişim ve uygulamadan sorumlusun; altta sağlayıcı donanım, ağ ve fiziksel güvenlikten sorumlu">
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Paylaşılan Sorumluluk Modeli</text>
  <text x="340" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Çizginin üstü sen · altı sağlayıcı</text>
  <rect x="90" y="58" width="500" height="60" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="340" y="82" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">SEN — bulutTAKİ güvenlik</text>
  <text x="340" y="103" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">veri · erişim yönetimi (IAM) · uygulama · yapılandırma</text>
  <line x1="90" y1="133" x2="590" y2="133" stroke="#C77A17" stroke-width="2" stroke-dasharray="6 4"/>
  <text x="340" y="130" text-anchor="middle" font-size="10" font-weight="600" fill="#C77A17" font-family="system-ui">sorumluluk hattı</text>
  <rect x="90" y="138" width="500" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="340" y="164" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">SAĞLAYICI — bulutUN güvenliği</text>
  <text x="340" y="186" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">donanım · ağ altyapısı · sanallaştırma · fiziksel tesis</text>
</svg>
```

## Bölge ve erişilebilirlik alanı — "bulut hiç çökmez" efsanesi
Bulut, tek bir dev makine değildir. Sağlayıcı makinelerini **bölgelere** (region) ayırır;
her bölgenin içinde birbirinden bağımsız elektrik ve ağ beslemesi olan **erişilebilirlik
alanları** (availability zone) bulunur.

Bunun mimari sonucu şudur: dayanıklılık otomatik gelmez, **sen tasarlarsan** gelir.
Uygulamanı tek bir alanda çalıştırırsan, o alan çökünce sen de çökersin — sağlayıcı yine de
SLA'sını tutmuş sayılır, çünkü bölgenin geri kalanı ayaktaydı. İki alana yayılmak (multi-AZ)
bir yapılandırma tercihi ve ek maliyettir.

Aynı şey bölge düzeyinde de geçerlidir: büyük sağlayıcıların **bölge çapında kesintileri
gerçekten yaşanmıştır**. "Buluta taşıdım, artık kesinti derdim yok" cümlesi teknik olarak
yanlıştır; doğrusu "kesinti olasılığını para ve karmaşıklık karşılığında düşürebilirim"dir.

## Satıcı iddialarına şerh — reklamı mimariden ayır
Bulut belgelerinin çoğunu satan taraf yazar. Aşağıdakiler sık duyacağın mutlak iddialar ve
her birinin gerçek karşılığıdır:

- **"Sınırsız ölçeklenir."** Hiçbir hizmet sınırsız değildir. Her hesabın **kota** (quota)
  ve **oran sınırı** (rate limit) vardır; sunucusuz işlevlerin eşzamanlılık tavanı, depolama
  hizmetlerinin saniyedeki istek tavanı vardır. Doğru okuma: "senin ölçeğinde muhtemelen
  sınıra çarpmazsın", "sınır yok" değil.
- **"%99,99 kullanılabilirlik."** Bu bir **taahhüt değil, geri ödeme koşuludur**. Tutulmazsa
  aldığın şey genelde faturandan bir kredidir — kaybettiğin iş değil. Ayrıca oran hizmet
  hizmet, bölge bölge ve yapılandırmaya göre değişir; tek alanda çalışan kurulumun SLA'sı
  çok alanlıdan düşüktür.
- **"Sunucusuz = sunucu yok."** Sunucu var, sen görmüyorsun. Bunun bedeli: soğuk başlangıç
  (cold start) gecikmesi, çalışma süresi tavanı, sınırlı yerel disk ve hata ayıklamanın
  zorlaşması.
- **"Bulut daha ucuz."** Bazen doğru, bazen değil. Kiralamak sabit ve öngörülebilir yükte
  satın almaktan pahalıya gelebilir; ayrıca **çıkış ücreti** (egress) ve yönetilen hizmet
  primleri faturada sonradan görünür. Ucuzluk bir ölçüm sonucudur, bir slogan değil.
- **"Bulut daha güvenli."** Paylaşılan sorumluluk gereği sağlayıcının tarafı güvenli
  olabilir; **senin tarafın** varsayılan olarak güvenli değildir. Sektörün en bilinen veri
  sızıntılarının bir kısmı, yanlış yapılandırılmış açık depolama kovalarından doğdu.
- **"Çoklu bulut (multi-cloud) seni kilitlenmeden kurtarır."** Çoğu zaman iki sağlayıcının
  da en zayıf ortak paydasını kullanmak ve iki ekip bilgisi taşımak demektir — kilitlenmeyi
  azaltırken karmaşıklığı ve maliyeti artırır. Bilinçli bir takas olabilir; otomatik bir
  kazanç değildir.

**Genel kural:** bir bulut iddiasını okurken sor — *bunu kim söylüyor, hangi ölçümle
söylüyor, hangi koşulda geçerli?* Sağlayıcının kendi karşılaştırma sayfası bir kanıt değil,
bir pazarlama argümanıdır; bağımsız ölçüm veya kendi yükünle yaptığın deneme kanıttır.

## Vendor lock-in uyarısı
Bir sağlayıcının **kendine özgü** (proprietary) yönetilen hizmetlerine ne kadar
derinden bağlanırsan, başka sağlayıcıya taşınman o kadar zorlaşır — buna **satıcıya
kilitlenme** (vendor lock-in) denir. Örneğin uygulamanı doğrudan AWS Lambda + DynamoDB +
SQS üçlüsüne örersen, GCP'ye geçiş neredeyse yeniden yazma demektir.

Denge şudur: kilitlenmeyi tümden reddetmek (her şeyi elle, taşınabilir kurmak) çoğu
zaman fazla maliyetlidir; ama kritik parçaları **standart arayüzlerin arkasına**
(ör. nesne depolamayı soyut bir arayüzle sarmak, PostgreSQL gibi taşınabilir bir
veritabanı seçmek) koyarak çıkış maliyetini bilinçli yönetebilirsin. Mimar kararı
sağlayıcı özelliğinin verdiği hız ile ileride ödenecek taşınma bedeli arasında tartılır.

## Öz-sınav (kendini test et)
1. IaaS, PaaS ve SaaS arasında "senin yönettiğin" ile "sağlayıcının yönettiği" nasıl
   değişir? Serverless bu merdivenin neresine düşer?
2. Nesne depolama (object storage) için AWS, GCP ve Azure'un ürün adları nedir?
3. Paylaşılan sorumluluk modelinde herkese açık bırakılmış bir veri kovasının güvenlik
   açığı kimin sorumluluğundadır — sen mi, sağlayıcı mı? Neden?
4. Vendor lock-in nedir ve bir mimar çıkış maliyetini azaltmak için hangi tasarım
   hamlelerini yapabilir?
5. "Sağlayıcıdan bağımsız düşün ama eşlemeyi bil" cümlesi pratikte ne demek? Bir örnekle
   açıkla.
6. Bölge (region) ile erişilebilirlik alanı (availability zone) arasındaki fark nedir?
   Uygulamanı tek alanda çalıştırırsan hangi arıza seni düşürür?
7. Bir sağlayıcı "%99,99 SLA" diyor. Bu cümle **ne taahhüt eder, ne taahhüt etmez**?
8. "Bulut her zaman daha ucuzdur" iddiasına iki karşı argüman üret. Faturada sonradan
   görünen hangi kalem çoğu zaman hesaba katılmaz?
9. Eşleme tablosundaki "S3 ↔ Cloud Storage ↔ Blob Storage" satırı neden birebir bir denklik
   değildir? Bu ayrımı bilmemek taşınma planında hangi hataya yol açar?
10. "Çoklu bulut kullanırsak kilitlenmeden kurtuluruz" diyen bir ekip arkadaşına hangi
    soruları sorarsın?
