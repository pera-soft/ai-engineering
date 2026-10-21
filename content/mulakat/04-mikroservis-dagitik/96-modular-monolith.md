---
slug: modular-monolith
baslik: "Modüler Monolit: 2026'nın Varsayılan Başlangıcı, Spring Modulith ve Sınır Zorlama"
kademe: İleri
sira: 96
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Büyük bir evi düşün. Odaları duvarlarla ayırmak (mutfak, yatak odası, banyo) ile her odayı ayrı bir binaya taşımak aynı şey değildir. Duvar ucuzdur ve hayatı düzenler; ayrı bina ise yol, elektrik, su ve her seferinde araba kullanmak demektir. Yazılımda "duvar" **modülerlik**, "ayrı bina" ise **dağıtımdır**. Modüler monolit, evi tek bina olarak tutup içine sağlam duvarlar örmektir: düzeni bugün kazanırsın, taşınma masrafını erteler ve gerçekten gerekirse tek bir odayı kolayca ayrı binaya taşırsın.

### Temel kavramlar

- **Modül**: uygulamanın bir iş alanını (sipariş, stok, ödeme) kapsayan, kendi içine kapalı kod parçası.
- **Açık arayüz (public API)**: bir modülün dışarıya sunduğu, kullanılmasına izin verilen sınıf/metot kümesi.
- **Kapsülleme (encapsulation)**: modülün iç sınıflarının dışarıdan görülmemesi/çağrılmaması.
- **Dağıtım (deployment)**: yazılımın sunucuya kurulup çalıştırılması. **Dağıtım birimi**: birlikte kurulan tek paket.
- **Süreç (process)**: işletim sisteminin çalıştırdığı tek program örneği. Aynı süreçteki modüller **fonksiyon çağrısıyla** (nanosaniye) konuşur.
- **Servis**: ayrı çalışan, yalnız **ağ çağrısıyla** (milisaniye) ulaşılabilen bağımsız uygulama.
- **Monolit**: tüm işlevlerin tek dağıtım biriminde olduğu uygulama.
- **Modüler monolit**: tek dağıtım birimi ve tek süreç, ama içeride **katı modül sınırları**.
- **Büyük çamur topu (big ball of mud)**: sınırların eridiği, her şeyin her şeye bağlandığı kod tabanı.
- **Sınır zorlama (enforcement)**: kuralın niyete değil, **otomatik bir teste** bağlanması; ihlalde derleme/CI kırılır.
- **CI (sürekli entegrasyon)**: her kod gönderiminde testleri otomatik çalıştıran hat.
- **Uygulama olayı (application event)**: bir modülün "şu oldu" diye yayınladığı, kimin dinlediğini bilmediği bildirim.
- **Mesaj kuyruğu (Kafka/RabbitMQ)**: olayları ağ üzerinden, kalıcı biçimde taşıyan altyapı.
- **Çıkarma (extract)**: bir modülü olduğu gibi alıp ayrı bir servise dönüştürmek.

### Nasıl çalışır?

Ana fikir iki kavramı ayırmaktır: **modülerlik** (kodun mantıksal sınırlara bölünmesi) ile **dağıtım** (kodun ayrı süreçlere fiziksel dağıtılması) **aynı şey değildir**. Modülerlik ucuzdur ve her zaman iyidir: net sınırlar, açık arayüz, gizli iç yapı. Dağıtım pahalıdır (ağ gecikmesi, dağıtık tutarlılık, operasyon) ve yalnız ölçek/ekip baskısı zorlayınca değer üretir.

Modüler monolit tek süreçte çalışır. Ama modüller yalnız birbirlerinin **açık arayüzü** üzerinden konuşur; iç sınıflara erişim yasaktır. Böylece monolitin ucuz yanları korunur: yerel fonksiyon çağrısı, tek veritabanında ACID **işlem (transaction)**, tek dağıtım ve kolay hata ayıklama. Üstüne mikroservisin en değerli yanı — **net sınırlar** — bedavaya gelir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Modüler monolit: tek süreç, katı iç sınırlar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Modülerlik ≠ Dağıtım</text>
  <rect x="30" y="42" width="580" height="128" rx="14" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="320" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Tek dağıtım birimi (tek süreç · tek veritabanı · yerel çağrı)</text>
  <rect x="56" y="80" width="150" height="72" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="131" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">siparis</text>
  <text x="131" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">açık API</text>
  <text x="131" y="142" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iç yapı gizli</text>
  <rect x="245" y="80" width="150" height="72" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">stok</text>
  <text x="320" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">açık API</text>
  <text x="320" y="142" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iç yapı gizli</text>
  <rect x="434" y="80" width="150" height="72" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="509" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">odeme</text>
  <text x="509" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">açık API</text>
  <text x="509" y="142" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iç yapı gizli</text>
  <line x1="206" y1="116" x2="245" y2="116" stroke="#186399" stroke-width="2"/>
  <line x1="395" y1="116" x2="434" y2="116" stroke="#186399" stroke-width="2"/>
  <text x="320" y="196" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Sınır test ile zorlanır (Spring Modulith); gerekince modül → mikroservis olarak ÇIKARILIR</text>
</svg>
```

Asıl zorluk şudur: **sınırlar zamanla erir**. Hiçbir dil, bir geliştiricinin başka modülün iç sınıfını `import` etmesini kendiliğinden engellemez. Sınır yalnız konvansiyona ve iyi niyete bırakılırsa, teslim baskısı altında sızıntılar birikir ve birkaç ayda "büyük çamur topu" doğar. Kod incelemesi (code review) yardımcı olur ama ölçeklenmez: gözden kaçar, ekip değişir, acele edilir. Bu yüzden sınır **otomatik olarak zorlanmalıdır**. Java/Spring dünyasında bunun aracı **Spring Modulith**'tir. Dört iş yapar:

- Modülleri **paket yapısından** tanır; ayrı bir tanım dosyası istemez.
- `ApplicationModules.of(App.class).verify()` çağrısıyla izin verilmeyen modüller arası bağımlılıkları yakalar ve **birim testini kırar**.
- Modüller arası **uygulama olaylarını** yönetir (`@ApplicationModuleListener`).
- Modül yapısının dokümantasyonunu üretir.

Aynı işi genel amaçlı **ArchUnit** kuralları da yapabilir. Kritik nokta şudur: sınır artık bir temenni değil, **doğrulanabilir bir sözleşmedir**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Modül sınırı niyetle erir, testle korunur">
  <defs>
    <marker id="modular-monolith-siz" markerWidth="9" markerHeight="9" refX="7.5" refY="3" orient="auto">
      <path d="M0,0 L7.5,3 L0,6 Z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sınır: yalnız niyetle erir, testle korunur</text>
  <rect x="24" y="40" width="290" height="150" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="169" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Yalnız konvansiyon</text>
  <rect x="46" y="76" width="110" height="40" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="101" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">modül X</text>
  <rect x="182" y="76" width="110" height="40" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="237" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">modül Y</text>
  <text x="237" y="108" text-anchor="middle" font-size="9" fill="#8A2E2E" font-family="system-ui">iç sınıf</text>
  <line x1="156" y1="96" x2="180" y2="96" stroke="#8A2E2E" stroke-width="1.8" stroke-dasharray="3 2" marker-end="url(#modular-monolith-siz)"/>
  <text x="169" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">izinsiz import sızar</text>
  <text x="169" y="172" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">büyük çamur topu</text>
  <rect x="326" y="40" width="290" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">verify() testi zorlar</text>
  <rect x="348" y="76" width="110" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="403" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">modül X</text>
  <rect x="484" y="76" width="110" height="40" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="539" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">modül Y</text>
  <text x="539" y="108" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">açık API</text>
  <line x1="458" y1="96" x2="482" y2="96" stroke="#25804C" stroke-width="1.8"/>
  <text x="471" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">izinsiz import → CI build KIRILIR</text>
  <text x="471" y="172" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">sınır doğrulanabilir sözleşme</text>
</svg>
```

Sınır çizmenin bir de **maliyet asimetrisi** vardır; yani yanlış sınırı düzeltmenin bedeli her yerde aynı değildir.

Yanlış çizilmiş bir sınır **modül içindeyse** düzeltmesi ucuzdur. Sınıfları taşırsın; derleyici ve sınır testleri sana yol gösterir. Tek işlem (transaction) hâlâ çalışmaya devam eder.

Aynı sınır bir **servis sınırına** dönüşmüşse pahalıdır. Veri iki ayrı veritabanına ayrılmıştır. İletişim bir ağ sözleşmesine dönmüştür. Üstüne sürüm uyumluluğu ve dağıtık tutarlılık dertleri girer. Erken bölmenin trajedisi budur: sistemi **en az anladığın** anda sınırları **en pahalı** biçimde betona dökersin. Diğer maliyetler (derleme süresi, yeni araç öğrenme, kod tekrarı) gerçektir ama bunun yanında küçük kalır.

Çıkarma yolu ise olay tabanlı tasarımla kolaylaşır. "Sipariş" modülü "bildirim" modülünün metodunu doğrudan çağırırsa ona **derleme-zamanı bağımlılığıyla** bağlanır. Bunun yerine `SiparisOlusturuldu` olayını yayınlarsa kimin dinlediğini bilmez; tüketici bağımsızca abone olur. Modül sonradan servise çıkarıldığında süreç-içi olay doğal olarak bir **mesaj kuyruğuna** dönüşür. Dikkat: süreç-içi olay tek başına teslim garantisi vermez. Uygulama olayı işledikten sonra çökerse olay kaybolabilir. Spring Modulith bunun için yayınlanan olayları veritabanına yazan bir **olay yayın defteri (event publication registry)** sunar; teslim edilmemiş olay yeniden denenir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Modülü mikroservise çıkarma: süreç-içi olay mesaj kuyruğuna dönüşür">
  <defs>
    <marker id="modular-monolith-cik" markerWidth="10" markerHeight="10" refX="8" refY="3.5" orient="auto">
      <path d="M0,0 L8,3.5 L0,7 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çıkarma yolu: süreç-içi olay → mesaj kuyruğu</text>
  <rect x="30" y="44" width="250" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="155" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Modüler monolit (tek süreç)</text>
  <rect x="50" y="80" width="90" height="60" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="95" y="106" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">siparis</text>
  <text x="95" y="124" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">olay yayınlar</text>
  <rect x="170" y="80" width="90" height="60" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="215" y="106" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">bildirim</text>
  <text x="215" y="124" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dinler</text>
  <line x1="140" y1="110" x2="168" y2="110" stroke="#186399" stroke-width="1.6" marker-end="url(#modular-monolith-cik)"/>
  <line x1="288" y1="104" x2="352" y2="104" stroke="#186399" stroke-width="2.2" marker-end="url(#modular-monolith-cik)"/>
  <text x="320" y="96" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">çıkar</text>
  <rect x="360" y="44" width="250" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="485" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Ayrı servis (dağıtık)</text>
  <rect x="378" y="80" width="80" height="60" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="418" y="106" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">siparis</text>
  <text x="418" y="124" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">servisi</text>
  <rect x="512" y="80" width="80" height="60" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="552" y="106" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">bildirim</text>
  <text x="552" y="124" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">servisi</text>
  <rect x="466" y="92" width="38" height="36" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <text x="485" y="107" text-anchor="middle" font-size="8" fill="#B26B00" font-family="system-ui">Kafka</text>
  <text x="485" y="120" text-anchor="middle" font-size="8" fill="#B26B00" font-family="system-ui">kuyruk</text>
  <text x="320" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Olay tabanlı yapı, geçişi minimum kod değişikliğiyle mümkün kılar</text>
</svg>
```

### Mülakatta ne sorulur?

- **Modülerlik ≠ dağıtım**: modülerliği bugün al, dağıtımı erteleyebildiğin kadar ertele.
- **Modüler monolit vs klasik monolit**: ikisi de tek süreç, tek dağıtım birimi; fark iç sınırların katılığı ve arayüz disiplini. Ayrı repo/ayrı CI ya da modül başına veritabanı şartı yoktur.
- **2026 varsayılanı**: "modüler monolitle başla, gerekince çıkar" (start with a modular monolith, extract when needed).
- **Spring Modulith kapsamı**: modül tanıma, `verify()` ile sınır testi, uygulama olayları, event publication registry, dokümantasyon. Kod bölmez, dağıtmaz, mesh kurmaz, önbellek değildir.
- **Neden test?** İnsan disiplini ve kod incelemesi ölçeklenmez; kural CI'da kırılmıyorsa er ya da geç ihlal edilir.
- **Maliyet asimetrisi**: modül içi sınır ucuz taşınır, servis sınırı ağ + ayrı veritabanı + sözleşme yüzünden pahalı taşınır.
- **Olay tabanlı bağlaşım**: derleme-zamanı bağımlılığını keser, çıkarmayı ucuzlatır; ama teslim garantisi ayrı bir iştir.
- **Tuzak**: "Modüler monolit mikroservisin ucuz taklidi mi?" Hayır — farklı bir takas noktasıdır; sınır kalitesini korur, dağıtım maliyetini ödemez.

### Tek cümlede

Modüler monolit, mikroservisin en değerli parçasını — net sınırları — tek dağıtım birimi içinde kazanmaktır; bunu otomatik testlerle zorlayarak yapar. Dağıtımın ağ, tutarlılık ve operasyon faturasını ise gerçekten gerekene kadar erteler.

# modmono-1 | Temel
## Soru
Modüler monolit ile klasik (katmanlı ama modülsüz) monolit arasındaki temel fark nedir?
## A
Modüler monolit birden fazla ayrı süreçte çalışır, klasik monolit tek süreçte çalışır
🤖 İkisi de tek süreçte çalışır; fark dağıtımda değil, içerideki sınırların katılığındadır.
## B
Modüler monolit her modül için ayrı bir veritabanı ve ayrı şema kullanmak zorundadır
🤖 Modüler monolit genelde tek veritabanı kullanır; ayrım fiziksel değil mantıksaldır.
## C
Tek süreçte kalır ama modüller katı sınırlıdır; yalnız açık arayüzden konuşurlar
🤖 Doğru: fark iç yapıdadır — net modül sınırları ve gizlenmiş iç yapı, tek dağıtım biriminde.
## D
Modüler monolitte her modülün ayrı kod deposu ve ayrı dağıtım hattı bulunur
🤖 Ayrı repo/hat şartı yoktur; modüler monolit tek depo ve tek dağıtım hattıyla çalışır.
## Doğru
C
## Açıklama
Her iki mimari de **tek süreçte, tek dağıtım birimi** olarak çalışır — dağıtım açısından aynıdırlar; ayrı repo, ayrı hat (D) ya da modül başına veritabanı (B) gerektirmezler. Fark **içeridedir**: klasik monolit katmanlıdır (controller/service/repository) ama iş alanları birbirine sızabilir; herhangi bir sınıf başka bir alanın iç sınıfını çağırabilir ve zamanla "büyük çamur topu" oluşur. **Modüler monolit** ise iş alanlarını **modüllere** ayırır; her modülün bir **açık arayüzü** (public API) ve gizlenmiş bir iç yapısı vardır, modüller yalnız bu arayüz üzerinden haberleşir. Böylece kodun anlaşılırlığı, bağımsız evrimi ve gerektiğinde çıkarılabilirliği (extractability) monolitin basitliğinden ödün vermeden kazanılır.

# modmono-2 | Temel
## Soru
2026'da yeni bir sisteme başlarken yaygın olarak önerilen yaklaşım hangisidir?
## A
Her iş alanı için baştan ayrı bir mikroservis oluşturup bağımsız dağıtmak
🤖 Erken bölme, sınırlar belirsizken yanlış sınırları betona döker; önerilen yol bu değildir.
## B
Katmanlı ama modülsüz klasik monolit yazmak; sınırları ihtiyaç doğunca sonradan çizmek
🤖 Modülerliği ertelemek ucuz olanı ertelemektir; sınırlar sonradan çok daha zor çizilir.
## C
Baştan Kubernetes üzerinde service mesh'li dağıtık bir kurulum hazırlamak
🤖 Erken altyapı karmaşıklığı ekibi ürün yerine operasyonla meşgul eder.
## D
Modüler monolitle başlamak; baskı oluşunca ilgili modülleri mikroservis olarak çıkarmak
🤖 Doğru: "modüler monolitle başla, gerekince çıkar" 2026'da uzlaşılan varsayılandır.
## Doğru
D
## Açıklama
Endüstrinin 2020'lerdeki mikroservis geri tepmesinden çıkardığı ders: **erken bölme, sistem hakkında en az bilgiye sahip olduğun anda mimariyi betona dökmektir**. Önerilen yol "**modüler monolitle başla, gerekince çıkar**" (start with a modular monolith, extract when needed). Bu sayede modülerliğin faydasını (net sınırlar) ucuza alırsın ama dağıtımın maliyetini (ağ, tutarlılık, operasyon) ertelersin. Sınırlar gerçek kullanımla olgunlaşınca ve gerçek bir ölçek/ekip baskısı doğunca doğru yerden bölersin. A erken bölmenin, C erken altyapı karmaşıklığının tuzağıdır. B ise ters yönde hata yapar: modülerlik ucuz ve her zaman değerlidir; onu ertelemek sınırların hiç oluşmamasına ve çamur topuna yol açar.

# modmono-3 | Orta
## Soru
Spring Modulith'in projeye kattığı temel değer nedir?
## A
Modülleri paket yapısından tanır ve izinsiz bağımlılıkları bir testte kırar
🤖 Doğru: Modulith sınırları doğrulanabilir kılar; ihlal test aşamasında yakalanır.
## B
Monoliti otomatik olarak mikroservislere bölüp ayrı süreçlerde çalıştırır
🤖 Modulith kodu bölüp dağıtmaz; modülerliği tek süreç içinde tanımlar ve doğrular.
## C
Uygulamayı Kubernetes'e dağıtır ve servisler arasına sidecar proxy yerleştirir
🤖 Dağıtım ve service mesh onun kapsamı değildir; ilgilendiği alan süreç-içi modül yapısıdır.
## D
Modüller arası çağrıları önbelleğe alarak veritabanı sorgularını hızlandırır
🤖 Önbellek aracı değildir; amacı sınır doğrulama ve modüller arası olay yönetimidir.
## Doğru
A
## Açıklama
**Spring Modulith**, modüler monolitin en büyük riskini — **sınırların zamanla erimesi** — doğrudan hedefler. Sağladıkları: (1) modülleri **paket yapısından** tanır; (2) `ApplicationModules.of(App.class).verify()` çağrısıyla **izin verilmeyen çapraz-modül bağımlılıklarını bir birim testinde kırar** — bir geliştirici başka modülün iç paketine bağımlılık eklerse test kırmızıya döner; (3) modüller arası gevşek bağlaşım için **uygulama olayları** (`@ApplicationModuleListener`) ve teslim dayanıklılığı için **event publication registry** sunar; (4) modül yapısının dokümantasyonunu ve C4 diyagramlarını üretir. Kritik nokta: sınır **niyetle değil, testle** zorlanır. Modulith kod bölmez, dağıtmaz (B), Kubernetes/mesh kurmaz (C), bir önbellek katmanı değildir (D); kapsamı tek süreç içindeki modülerliktir.

# modmono-4 | Orta
## Soru
Modüler monolitte modül sınırlarının "test ile zorlanması" (enforced) neden kritiktir?
## A
Sınır testi olmadan uygulama derlenmez, çünkü modüller birbirini göremez
🤖 Sınır testi olmadan da uygulama derlenir; test derlemeyi değil mimari kuralı korur.
## B
Yalnız niyete bırakılan sınırlar zamanla erir; otomatik test ihlali daha ilk anda yakalar
🤖 Doğru: insan disiplini ölçeklenmez; ihlal otomatik yakalanmazsa sınırlar kaçınılmaz olarak çözülür.
## C
Kod incelemesi (code review) ihlali zaten yakalar; test yalnızca aynı işi tekrarlar
🤖 İnceleme yardımcı olur ama ölçeklenmez: gözden kaçar, ekip değişir, acele edilir.
## D
Sınır testleri modüller arası çağrıları hızlandırarak çalışma-zamanı performansını artırır
🤖 Performans testlerin konusu değildir; sınır testi hızı değil yapı bütünlüğünü korur.
## Doğru
B
## Açıklama
Modüler monolitin başarısı tümüyle **sınırların korunmasına** bağlıdır, ama hiçbir dil bir geliştiricinin başka modülün iç sınıfını `import` etmesini kendiliğinden engellemez. Sınırlar yalnız **konvansiyona ve niyete** bırakılırsa, zaman baskısı altında sızıntılar birikir ve birkaç ay içinde modüller birbirine dolanır — "büyük çamur topu" (big ball of mud). Kod incelemesine güvenmek (C) kısmi bir çözümdür: insan gözü yorulur, ekip değişir, acele edilir; kural yalnızca **otomatik** çalıştığında güvenilirdir. **Otomatik zorlama** erozyonu önler: Spring Modulith'in `verify()` testi veya ArchUnit kuralları, izin verilmeyen bir çapraz-modül bağımlılığı eklendiği anda CI'da build'i kırar. Böylece mimari kural bir **doğrulanabilir sözleşmeye** dönüşür ve modülü sonradan mikroservise çıkarmak da güvenli hale gelir, çünkü bağımlılıklar hep bilinir ve temizdir.

# modmono-5 | İleri
## Soru
Bir ekip modüler monoliti mikroservise erken bölerse ortaya çıkan en pahalı hata türü hangisidir?
## A
Servis sayısı arttığı için toplam kod satırının ve derleme süresinin büyümesi
🤖 Gerçek ama ikincil bir maliyettir; asıl pahalı olan sınırların yanlış yerde donmasıdır.
## B
Ekibin konteyner, izleme ve dağıtım araçlarını öğrenmek için zaman harcaması
🤖 Bir kerelik öğrenme maliyetidir; yanlış sınırın kalıcı bedeliyle kıyaslanamaz.
## C
Yanlış çizilen sınırın dağıtık hale gelip düzeltilmesinin çok pahalılaşması
🤖 Doğru: modül içi sınır ucuz taşınır; servis sınırı ağ, ayrı veritabanı ve sözleşmelerle betona döker.
## D
Servisler arasında kod tekrarı oluşması ve ortak kütüphanenin çoğalması
🤖 Yönetilebilir bir tasarım sorunudur; sınır hatası gibi geri dönülmez bir maliyet üretmez.
## Doğru
C
## Açıklama
Sınır çizme kararının **maliyet asimetrisi** kritik konudur. Bir sınır **modül içindeyse** ve yanlış çizildiyse düzeltmek ucuzdur: sınıfları bir modülden diğerine taşırsın, derleyici ve sınır testleri sana yol gösterir, tek transaction hâlâ çalışır. Aynı sınır bir **servis sınırına** dönüşmüşse düzeltmek çok pahalıdır: veri iki ayrı veritabanına bölünmüştür (birleştirmek/taşımak zor), iletişim ağ sözleşmesine (API contract) dönmüştür, sürüm uyumluluğu ve dağıtık tutarlılık devreye girmiştir. A, B ve D gerçek maliyetlerdir ama telafi edilebilir; sınır hatası ise geri dönüşü en pahalı olandır. Erken bölmenin trajedisi tam da budur: sistemi **en az anladığın** başlangıç anında sınırları **en pahalı** biçimde (dağıtık) betona dökersin. Bu yüzden modülerlik yatırımı erken, dağıtım kararı geç yapılır.

# modmono-6 | İleri
## Soru
Modüler monolitte modüller arası bağlaşımı gevşetmek için "senkron çağrı yerine uygulama olayı (application event) yayınlama" yaklaşımının avantajı nedir?
## A
Olaylar veritabanını ortadan kaldırır, modüller tüm durumu bellekte tutabilir
🤖 Olaylar kalıcılık ihtiyacını kaldırmaz; sağladıkları şey gevşek bağlaşımdır.
## B
Olaylar teslim garantisi sağladığı için hata yönetimine artık hiç gerek kalmaz
🤖 Süreç-içi olay tek başına teslim garantisi vermez; bunun için event publication registry gerekir.
## C
Olaylar senkron çağrılardan daha yavaştır ve bu yavaşlık istenen bir özelliktir
🤖 Yavaşlık avantaj değildir; süreç-içi olaylar zaten hızlıdır, amaç hız değil gevşek bağlaşımdır.
## D
Yayıncı tüketiciye bağlı kalmadan onu tetikler; olay sonradan kuyruğa dönüşebilir
🤖 Doğru: olay tabanlı iletişim derleme-zamanı bağımlılığını keser ve çıkarmayı kolaylaştırır.
## Doğru
D
## Açıklama
Modüller arası **senkron çağrı**, yayıncı modülü tüketicinin arayüzüne **derleme-zamanı bağımlılığıyla** bağlar: "siparis" modülü "bildirim" modülünün metodunu doğrudan çağırırsa ona bağımlı hale gelir. **Uygulama olayı** yaklaşımı bunu tersine çevirir: "siparis" yalnız `SiparisOlusturuldu` olayını yayınlar, hangi modüllerin dinlediğini bilmez; tüketiciler `@ApplicationModuleListener` ile bağımsızca abone olur. Bu **gevşek bağlaşım** iki değer üretir: (1) modüller birbirinin iç yapısına bağlı kalmadan evrilebilir; (2) tüketici modül ileride mikroservise çıkarılırken süreç-içi olay **doğal olarak bir mesaj kuyruğuna** (Kafka/RabbitMQ) dönüştürülebilir — kod zaten olay tabanlı olduğundan geçiş minimum değişiklikle olur. Dikkat: süreç-içi olay kendi başına **teslim garantisi vermez** (B'nin yanılgısı); Spring Modulith bunun için olay yayınlarını kalıcılaştıran bir **event publication registry** sunar.
