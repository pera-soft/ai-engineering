---
slug: chaos-reliability
baslik: "Chaos Engineering & Güvenilirlik: Hata Enjeksiyonu, Blast Radius, Steady State ve Oyun Günleri"
kademe: İleri
sira: 192
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Birden çok bilgisayarın birlikte çalıştığı sistemlerde **arıza istisna değil, normaldir**: ağ gecikir, disk dolar, bir sunucu ölür, bir bağımlılık yavaşlar. Soru "arıza olur mu?" değil, "**arıza olduğunda ne oluyor?**" sorusudur. Bu sorunun cevabını tahmin etmek yerine ölçmek istersen, arızayı **kendin, bilerek ve kontrollü** biçimde yaratırsın. Buna **hata enjeksiyonu** (fault injection) denir: sisteme yapay bir bozukluk sokmak — bir sunucuyu kapatmak, bir çağrıya 500 ms gecikme eklemek, bir bağımlılığı reddettirmek, iki grup makinenin haberleşmesini kesmek.

**Kasıtlı arıza denemesi (chaos engineering — kaos mühendisliği)** bu enjeksiyonu bir **bilimsel deneye** dönüştürür. Adı yanıltıcıdır: amaç kaos yaratmak değil, sistemin içinde saklanan kırılganlıkları **müşteri fark etmeden önce** bulmaktır. İtfaiye tatbikatı gibi düşün — yangını gerçekten çıkarmadan, çıkışların işleyip işlemediğini öğrenirsin.

### Temel kavramlar

- **Kararlı durum hipotezi (steady state)**: sistemin "sağlıklı" olduğunu gösteren **ölçülebilir** bir çıktı. Örnek: "sipariş başarı oranı > %99.5" ya da "**p99 gecikme (latency)** < 300 ms". (p99, yüz istekten en yavaş biri demektir; yani "isteklerin %99'u 300 ms'den hızlı".) Bu ölçü mümkün olduğunca **iş/kullanıcı düzeyinde** seçilir. CPU gibi iç metrikler kullanıcı deneyimini doğrudan yansıtmaz — CPU düşükken de siparişler düşüyor olabilir. "Sıfır hata" hedefi ise gerçekçi değildir.
- **Etki yarıçapı (blast radius)**: deneyin dokunabileceği en geniş alan. Tek örnek, tek bölge, tüm sistem... Deney daima **en küçük** yarıçapta başlar.
- **Durdurma düğmesi (abort)**: hipotez bozulduğu anda deneyi kesen, önceden hazırlanmış hızlı çıkış.
- **Oyun günü (game day)**: ekibin planlı biçimde bir araya gelip gerçekçi bir arıza senaryosunu birlikte yürüttüğü tatbikat.
- **Basamaklı çöküş (cascading failure)**: tek bir bağımlılığın yavaşlaması, onu çağıran servisi de çökertir.
- **Dayanıklılık desenleri**: **zaman aşımı (timeout)** belirli sürede dönmeyen çağrıyı bırakır; **devre kesici (circuit breaker)** sürekli yavaş ya da hatalı bir bağımlılıkta devreyi açar ve hızlı başarısız olur; **bölme duvarı (bulkhead)** bir bağımlılığın tükettiği kaynağı diğerlerinden ayırır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Blast radius: küçük başla, güvenle büyüt">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Blast radius: küçük başla, güvenle büyüt</text>
  <circle cx="150" cy="120" r="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="150" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">%1</text>
  <text x="150" y="132" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek örnek</text>
  <circle cx="320" cy="120" r="44" fill="#FBF0DE" stroke="#B07D28" stroke-width="1.5"/>
  <text x="320" y="118" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D28" font-family="system-ui">%10</text>
  <text x="320" y="134" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bir bölge</text>
  <circle cx="510" cy="120" r="60" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="510" y="118" text-anchor="middle" font-size="12" font-weight="700" fill="#B5504F" font-family="system-ui">%100</text>
  <text x="510" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tüm sistem</text>
  <text x="320" y="182" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">hipotez her adımda korunuyorsa kapsamı büyüt; bozulursa dur</text>
</svg>
```

### Nasıl çalışır?

Deney dört adımlıdır. **(1) Hipotezi yaz:** "Bir örnek ölse bile sipariş başarı oranı %99.5'in üstünde kalır." **(2) Hatayı enjekte et:** gerçek dünyada olabilecek bir olayı taklit et. **(3) Gözlemle:** steady state korundu mu? **(4) Öğren:** korunmadıysa bir kırılganlık bulundun — düzelt ve aynı deneyi bir **regresyon** olarak tekrar koş, bir daha geri gelmesin.

**Blast radius disiplini** bu yöntemin güvenlik kemeridir. "Tüm örnekleri aynı anda öldürelim" bir deney değil, **kontrolsüz bir gerçek kesintidir**. Doğrusu en küçükte başlamaktır: tek örnek veya %1 trafik. Hipotez korunuyorsa kapsam kademeli büyütülür; bozulursa etki küçük kalır ve abort düğmesiyle deney anında sonlandırılır.

**Üretimde test etmek** neden meşru? Çünkü gerçek davranış yalnız gerçek trafikte, gerçek veride, gerçek ölçekte ve gerçek ayarlarda ortaya çıkar. Test ortamı (staging) bunları asla tam kopyalayamaz; bazı kırılganlıklar ancak üretimde görünür.

Ama bu, "üretimi bilerek bozmak" demek değildir. Dört kontrolle yapılır: küçük etki yarıçapı, önceden tanımlı kararlı durum ölçüsü, sıkı izleme ve her an basılabilir durdurma düğmesi. Katı yasak değerli sinyali kaybettirir; kontrolsüz deney ise gerçekten sorumsuzdur. Hiçbir araç riski tek başına sıfırlamaz — güvenlik **süreçten** gelir.

**Tipik bir bulgu.** Bir bağımlılığa 500 ms gecikme enjekte edersin. Üst servis bu gecikmeyi zaman aşımıyla sınırlamaz, çağrıları kuyrukta biriktirir. İş parçacığı havuzu (thread pool) tükenir ve servis tümüyle yanıt veremez hâle gelir.

Bu bir **basamaklı çöküştür**. Deneyin başarısızlığı değil, tam da **başarısıdır**: gizli kırılganlık müşteri fark etmeden bulunmuştur. Öncelikli düzeltme zaman aşımı + devre kesici + bölme duvarıdır. Daha fazla iş parçacığı eklemek yalnız çöküşü geciktirir. Senaryo da gerçekçidir; gerçek bağımlılıklar saniyelerce yavaşlayabilir.

**Oyun günü ne ekler?** Otomatik araçlar **sistemin** teknik dayanıklılığını sınar. Oyun günü ise sosyo-teknik bir tatbikattır; **insanı ve süreci** de sınar. Sorular şunlardır: doğru alarm tetiklendi mi, **olay kılavuzu (runbook — olay anında adım adım ne yapılacağını yazan belge)** güncel ve izlenebilir mi, kim hangi rolü aldı, üst kademeye taşıma (escalation) ve iletişim nasıl işledi? Gerçek olaylarda sistem kadar müdahale hazırlığı da belirleyicidir. En iyi tasarlanmış sistem bile eskimiş bir olay kılavuzu yüzünden uzun kesintiye dönüşebilir.

**Program nasıl olgunlaşır?** Küçük ve düşük riskli deneylerle başla. Kanıtlanmış senaryoları otomatikleştirip dağıtım boru hattına ve düzenli oyun günlerine göm. İzleme (monitoring) bunun yerine geçmez: izleme **tepkiseldir**, olan biteni gösterir. Kasıtlı arıza denemesi ise gizli zaafı **önceden** ortaya çıkarır. İkisi birbirini tamamlar.

Pratik kural: önce bir **steady state hipotezi** yaz, en küçük **blast radius**'ta **hata enjekte et**, hipotez korunuyorsa büyüt; korunmuyorsa hemen durdur ve kırılganlığı düzelt.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Chaos deneyi kontrollü bilimsel döngü">
  <defs>
    <marker id="chaos-arrow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Chaos deneyi = kontrollü bilimsel döngü</text>
  <rect x="18" y="52" width="140" height="78" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="88" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">1 Steady state</text>
  <text x="88" y="99" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iş çıktısı ölç</text>
  <text x="88" y="117" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"sipariş &gt; %99.5"</text>
  <rect x="180" y="52" width="140" height="78" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="250" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">2 Hata enjekte</text>
  <text x="250" y="99" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">örnek öldür,</text>
  <text x="250" y="117" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gecikme / partition</text>
  <rect x="342" y="52" width="140" height="78" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="412" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">3 Gözlemle</text>
  <text x="412" y="99" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hipotez korundu</text>
  <text x="412" y="117" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">mu?</text>
  <rect x="504" y="52" width="118" height="78" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="563" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">4 Öğren</text>
  <text x="563" y="99" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kırılganlığı</text>
  <text x="563" y="117" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düzelt</text>
  <line x1="160" y1="91" x2="176" y2="91" stroke="#5D7789" stroke-width="1.5" marker-end="url(#chaos-arrow)"/>
  <line x1="322" y1="91" x2="338" y2="91" stroke="#5D7789" stroke-width="1.5" marker-end="url(#chaos-arrow)"/>
  <line x1="484" y1="91" x2="500" y2="91" stroke="#5D7789" stroke-width="1.5" marker-end="url(#chaos-arrow)"/>
  <path d="M563 130 L563 172 L88 172 L88 132" fill="none" stroke="#5D7789" stroke-width="1.5" marker-end="url(#chaos-arrow)"/>
  <text x="326" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düzelt → küçük blast radius'ta yeniden sına (regresyon)</text>
</svg>
```

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Basamaklı çöküş ve dayanıklılık desenleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bağımlılık yavaşlarsa: çöküş mü, izolasyon mu?</text>
  <rect x="26" y="42" width="270" height="146" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="161" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Desen yok</text>
  <text x="161" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağımlılık 500 ms yavaşlar</text>
  <text x="161" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çağrılar kuyrukta birikir</text>
  <text x="161" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">thread havuzu tükenir</text>
  <text x="161" y="152" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">→ üst servis de çöker</text>
  <text x="161" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(cascading failure)</text>
  <rect x="344" y="42" width="270" height="146" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="479" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Desen var</text>
  <text x="479" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">timeout: geç dönerse bırak</text>
  <text x="479" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">circuit breaker: devreyi aç</text>
  <text x="479" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bulkhead: kaynağı izole et</text>
  <text x="479" y="152" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">→ arıza yayılmaz</text>
  <text x="479" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hızlı başarısız ol</text>
</svg>
```

### Mülakatta ne sorulur?

- **"İlk deneyi tüm üretimde, tüm örnekleri öldürerek yapalım mı?"** Beklenen: hayır — blast radius kontrolü yok; tek örnek/%1 ile başla, abort düğmesi hazırla. Hata "üretimde test etmek" değil, kapsamın kontrolsüzlüğüdür.
- **"Steady state için hangi metriği seçersin?"** Beklenen: ölçülebilir bir iş çıktısı (sipariş başarı oranı, p99). CPU iç metriktir; "sıfır hata" gerçekçi değildir; kod kapsamı ilgisizdir.
- **"Oyun gününün otomatik araçtan farkı?"** Beklenen: insanı ve süreci de sınar — alarm, runbook, roller, iletişim. Üretim testinin yerine geçmez.
- **"'Üretimi bilerek bozmak sorumsuzluk' diyene ne dersin?"** Beklenen: kontrollerle (küçük kapsam, izleme, abort) yapıldığında sorumlu ve değerlidir; katı yasak da sınırsız serbestlik de yanlıştır.
- **"Gecikme enjekte ettik, servis çöktü — ne öğrendik?"** Beklenen: basamaklı çöküş kırılganlığı; timeout, circuit breaker, bulkhead ekle. Thread eklemek kök nedeni çözmez.
- **"Programı nasıl sürdürülebilir kılarsın?"** Beklenen: küçük-sık-otomatik deneyler + oyun günleri + bulguları regresyon olarak koşmak.

### Tek cümlede

Kasıtlı arıza denemesi (chaos engineering), önce bir **kararlı durum hipotezi** yazıp en küçük **etki yarıçapında** **hata enjekte ederek** sistemin dayanıklılığını deneyle sınamaktır; bulunan kırılganlık düzeltilir ve deney kalıcı bir **regresyon testine** çevrilir.

# chaos-1 | İleri
## Soru
Bir ekip ilk chaos deneyini doğrudan üretimin tamamında, tüm örnekleri aynı anda öldürerek yapmayı öneriyor. Chaos engineering ilkeleri açısından temel hata nedir?
## A
Blast radius kontrolü yok; deney en küçük kapsamda başlayıp güven arttıkça büyümeli
🤖 Doğru: kontrolsüz geniş kapsam gerçek bir kesinti riskidir; kaos mühendisliği kontrollü ve kademelidir.
## B
Deney yalnız test ortamında yapılmalı, üretimde test hiçbir koşulda kabul edilemez
🤖 Üretimde test, küçük blast radius ve durdurma düğmesiyle meşru bir tekniktir; asıl hata kapsamın kontrolsüzlüğü.
## C
Hata enjeksiyonu yerine önce kapsamlı bir yük testi koşulup darboğaz aranmalı
🤖 Yük testi farklı bir şeydir; sorun deneyin türü değil, kontrolsüz büyük kapsamı.
## D
Steady state yerine sunucuların CPU kullanımına bakılarak karar verilmeli
🤖 Metrik seçimi ayrı bir konu; buradaki asıl kusur blast radius kontrolünün olmaması.
## Doğru
A
## Açıklama
Chaos engineering'in tanımı **kontrollü** deneydir; "tüm örnekleri aynı anda öldür" kontrolsüz bir **gerçek kesinti** riskidir, deney değil. İlke **blast radius**'u (etki yarıçapı) en küçükte başlatmaktır: tek örnek veya %1 trafik. Hipotez korunuyorsa kapsam kademeli büyütülür; bozulursa etki küçük kalır ve **hızlı durdurma** (abort) düğmesiyle deney anında sonlandırılır. Üretimde test etmenin kendisi yasak değildir (B) — doğru yapıldığında en gerçekçi sinyali verir; asıl hata **kapsamın kontrolsüzlüğüdür**. Yük testi (C) veya metrik seçimi (D) bu senaryonun temel kusuru değildir.

# chaos-2 | İleri
## Soru
Chaos deneyi tasarlarken "steady state hipotezi" için hangi ölçüt en uygundur?
## A
Deney boyunca sunucu CPU kullanımının kesintisiz olarak %70'in altında kalması
🤖 CPU iç bir metriktir; kullanıcı deneyimini doğrudan yansıtmaz, düşük CPU'yla da sipariş düşebilir.
## B
Deney sırasında uygulama günlüklerinde hiçbir hata satırı görülmemesi
🤖 Bazı hatalar normal gürültüdür; mutlak sıfır hata gerçekçi değildir ve iş çıktısını ölçmez.
## C
Projenin kod kapsamının (coverage) belirlenen eşiğin üstünde kalmaya devam etmesi
🤖 Kapsam bir test metriğidir, çalışan sistemin sağlığını göstermez; steady state ile ilgisizdir.
## D
Sistemin sağlığını gösteren ölçülebilir bir iş çıktısı (ör. sipariş başarı oranı)
🤖 Doğru: steady state, kullanıcı/iş düzeyinde ölçülebilir normal davranıştır; deney bunu bozarsa kırılganlık bulunmuştur.
## Doğru
D
## Açıklama
**Steady state hipotezi**, sistemin "normal/sağlıklı" olduğunu gösteren **ölçülebilir** bir çıktıdır ve mümkün olduğunca **iş/kullanıcı düzeyinde** tanımlanır: sipariş başarı oranı, p99 gecikme, saniyedeki başarılı işlem gibi. Neden iş metriği? Çünkü CPU (A) gibi **iç** metrikler kullanıcı deneyimini doğrudan yansıtmaz — CPU düşükken de siparişler düşüyor olabilir. "Sıfır hata" (B) gerçekçi değildir; sistemlerde bir miktar hata gürültüsü normaldir. Kod kapsamı (C) bir test-zamanı metriğidir, çalışan sistemin sağlığıyla ilgisizdir. Deney bu iş metriğini bozuyorsa gerçek bir kırılganlık bulunmuş demektir.

# chaos-3 | İleri
## Soru
Bir "oyun günü" (game day) tatbikatının, yalnız otomatik chaos araçlarıyla arıza enjekte etmeye kıyasla sağladığı ek değer nedir?
## A
Otomatik araçlardan daha fazla ve daha çeşitli arıza türü enjekte edebilmesi
🤖 Arıza çeşitliliği araç meselesidir; oyun gününün asıl katkısı insan/süreç boyutu.
## B
Ekibin olay müdahale sürecini (alarm, runbook, roller, iletişim) birlikte sınaması
🤖 Doğru: oyun günü sosyo-teknik bir tatbikattır; teknik dayanıklılık kadar müdahale hazırlığını da ölçer.
## C
Üretimde test etme ihtiyacını tümüyle ortadan kaldırıp onun yerine geçmesi
🤖 Oyun günü üretim testinin yerini almaz; onu tamamlar ve genelde onunla birlikte yapılır.
## D
Kod kalitesini statik analiz araçlarıyla ölçüp ekibe raporlaması
🤖 Statik analiz ayrı bir uygulamadır; oyun günü çalışan sistem ve ekip müdahalesiyle ilgilidir.
## Doğru
B
## Açıklama
Otomatik chaos araçları **sistemin** teknik dayanıklılığını sınar. **Oyun günü** ise sosyo-teknik bir tatbikattır: ekip planlı biçimde bir araya gelir, gerçekçi bir arıza senaryosunu yürütür ve bu sırada **insan+süreç** boyutunu test eder — doğru alarm tetiklendi mi, runbook güncel ve izlenebilir mi, kim hangi rolü aldı, iletişim ve tırmandırma (escalation) nasıl işledi? Çünkü gerçek olaylarda sistem kadar **müdahale hazırlığı** da belirleyicidir; en iyi tasarlanmış sistem bile eskimiş runbook ve karışık iletişimle uzun kesintiye dönüşebilir. Oyun günü, üretim testinin (C) yerini almaz, onu tamamlar; arıza çeşitliliği (A) veya statik analiz (D) onun ayırt edici değeri değildir.

# chaos-4 | İleri
## Soru
"Üretimde test" (testing in production) fikrine karşı çıkan bir mühendis, "üretimi bilerek bozmak sorumsuzluktur" diyor. En dengeli yanıt nedir?
## A
Mühendis haklı; hiçbir koşulda üretim ortamında bilerek deney yapılmamalıdır
🤖 Katı yasak, gerçek davranışı yalnız üretimde görülebilen sınıf sorunları gözden kaçırır.
## B
Haksız; üretimde her şey serbestçe denenmeli, staging ortamı tümüyle gereksizdir
🤖 Kontrolsüz üretim deneyi de sorumsuzdur; mesele denge ve kontroldür, sınırsız serbestlik değil.
## C
Gerçek davranış yalnız üretimde görülür; küçük kapsam, izleme ve abort ile sorumludur
🤖 Doğru: amaç bozmak değil, kırılganlığı kontrollü ortaya çıkarmak; kontroller riski sınırlar.
## D
Sorun üretim değil araç seçimidir; doğru araçla bu riskler tamamen sıfırlanır
🤖 Hiçbir araç riski sıfırlamaz; güvenlik kontrollerden (kapsam, izleme, durdurma) gelir, tek başına araçtan değil.
## Doğru
C
## Açıklama
Denge burada anahtardır. Bir yandan **gerçek** güvenilirlik yalnız gerçek trafik, veri, ölçek ve yapılandırmada görünür; staging bunları asla tam kopyalayamaz — bu yüzden bazı kırılganlıklar ancak **üretimde** bulunur. Öte yandan kontrolsüz üretim deneyi gerçekten sorumsuzdur (B yanlış). Doğru çözüm ikisinin ortası: üretimde test **kontrollerle** yapılır — küçük **blast radius**, önceden tanımlı **steady state**, sıkı **izleme** ve her an basılabilir bir **durdurma (abort) düğmesi**. Amaç üretimi "bozmak" değil, kırılganlığı müşteri fark etmeden **kontrollü** ortaya çıkarmaktır. Katı yasak (A) değerli sinyali kaybettirir; hiçbir araç (D) riski tek başına sıfırlamaz — güvenlik süreçten gelir.

# chaos-5 | İleri
## Soru
Bir chaos deneyi, bir bağımlılığa 500 ms yapay gecikme enjekte edince, üst servis bu gecikmeyi zaman aşımıyla sınırlamak yerine kuyrukta biriktirip tüm thread havuzunu tüketti ve tümüyle yanıt veremez oldu. Bu deney ne kanıtladı ve öncelikli düzeltme nedir?
## A
Gizli bir basamaklı çöküş bulundu: timeout, circuit breaker ve bulkhead eklenmeli
🤖 Doğru: deney tam da bulması gereken şeyi buldu — dayanıklılık desenlerinin eksikliği; çözüm bunları eklemek.
## B
Deney başarısız oldu; enjekte edilen gecikme fazla yüksekti, 50 ms ile denenmeli
🤖 Deney başarılıdır — gizli kırılganlığı ortaya çıkardı; sorun gecikme miktarı değil, üst servisin dayanıksızlığı.
## C
Sonuç önemsiz; gerçek bağımlılıklar 500 ms kadar yavaşlamaz, senaryo gerçekçi değil
🤖 Gerçek sistemlerde bağımlılıklar saniyelerce yavaşlayabilir; senaryo gerçekçidir ve gerçek bir zaafı gösterdi.
## D
Üst servise daha fazla thread eklenmeli; kök neden yetersiz havuz boyutudur
🤖 Daha fazla thread yalnız çöküşü geciktirir; kök neden zaman aşımı/izolasyon eksikliğidir, kaynak miktarı değil.
## Doğru
A
## Açıklama
Deney tam da amacına ulaştı: **gizli bir kırılganlığı** müşteri fark etmeden ortaya çıkardı. Bulgu bir **basamaklı çöküştür** (cascading failure) — tek bir bağımlılığın yavaşlaması üst servisin tüm thread havuzunu tüketip onu da çökertiyor. Öncelikli düzeltme klasik **dayanıklılık desenleridir**: (1) **timeout** — bir çağrı belirli sürede dönmezse bırak; (2) **circuit breaker** — bağımlılık sürekli yavaş/hatalıysa devreyi aç, hızlı başarısız ol; (3) **bulkhead/izolasyon** ve **sınırlı kuyruk** — bir bağımlılığın kaynak tüketimini sınırla, gerisini etkilemesin. Daha fazla thread (D) yalnız çöküşü erteler, kök nedeni çözmez. Deney başarısız değildir (B); senaryo gerçekçidir (C) — bağımlılıklar gerçekten saniyelerce yavaşlayabilir.

# chaos-6 | İleri
## Soru
Chaos engineering programını olgunlaştırmak isteyen bir ekip için en sürdürülebilir yaklaşım hangisidir?
## A
Yılda bir kez büyük ve sürprizli bir "her şeyi kır" tatbikatı düzenlemek
🤖 Seyrek ve sürprizli büyük tatbikat öğrenmeyi ve güveni zayıflatır; küçük, sık, otomatikleşen deneyler daha sürdürülebilir.
## B
Yalnız kıdemli mühendislerin ara sıra elle deney koşup sonucu paylaşması
🤖 Bilgi birkaç kişiye sıkışır ve süreklilik olmaz; deneyler otomatikleşip boru hattına girmeli.
## C
Chaos deneylerini tümüyle bırakıp yalnız daha çok izleme (monitoring) eklemek
🤖 İzleme olanı gösterir ama gizli kırılganlığı proaktif ortaya çıkarmaz; deney ile izleme birbirini tamamlar.
## D
Küçük ve sık deneylerle başlayıp kanıtlanmış senaryoları otomatikleştirmek
🤖 Doğru: küçük-sık-otomatik döngü + oyun günleri, dayanıklılığı kalıcı ve ekip çapında bir pratiğe dönüştürür.
## Doğru
D
## Açıklama
Sürdürülebilir bir chaos programı, tek seferlik kahramanlıklar değil **sürekli bir pratiktir**. Doğru yol: küçük ve düşük riskli deneylerle başlamak, güven arttıkça **kanıtlanmış senaryoları otomatikleştirip** boru hattına ve düzenli **oyun günlerine** gömmek, ve her bulunan kırılganlığı düzelttikten sonra o deneyi bir **regresyon** olarak sürekli koşmak (bir daha geri gelmesin diye). Yılda bir "her şeyi kır" (A) sürprizi öğrenmeyi ve psikolojik güveni baltalar. Bilgiyi birkaç kişiye sıkıştırmak (B) süreklilik vermez. İzlemeyi artırmak (C) değerlidir ama izleme **reaktiftir** — olanı gösterir; chaos ise gizli zaafı **proaktif** ortaya çıkarır. İkisi birbirini tamamlar, biri diğerinin yerine geçmez.
