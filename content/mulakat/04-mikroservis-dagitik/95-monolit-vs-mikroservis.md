---
slug: monolit-vs-mikroservis
baslik: "Monolit mi Mikroservis mi: Dağıtık Maliyet, Conway Yasası ve Distributed Monolith Tuzağı"
kademe: Orta
sira: 95
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir lokanta düşün. Başlangıçta tek bir mutfak var: aşçılar aynı odada, birbirine "tabak hazır!" diye seslenir, dolap ortaktır. İş büyüyünce iki yol çıkar: ya aynı mutfağı daha düzenli hale getirirsin, ya da yemeği ayrı binalardaki küçük mutfaklara bölüp aralarında telefonla konuşturursun. İkinci yol ekipleri birbirinden bağımsız kılar; ama artık her "tabak hazır!" bir telefon görüşmesidir — hat meşgul olabilir, karşı taraf açmayabilir, konuşma zaman alır. Yazılımda bu iki yolun adı **monolit** ve **mikroservistir**. Bu konu, hangisinin ne zaman doğru olduğunu ve ikincisinin faturasının tam olarak ne olduğunu anlatır.

### Temel kavramlar

- **Uygulama**: bilgisayarda çalışan bir yazılım bütünü.
- **Süreç (process)**: işletim sisteminin çalıştırdığı tek bir program örneği. Aynı süreç içindeki kod parçaları aynı belleği paylaşır.
- **Fonksiyon çağrısı**: kodun, aynı süreç içinde başka bir kod parçasını çağırması. Nanosaniyeler sürer, "yolda kaybolmaz".
- **Servis**: kendi başına çalıştırılabilen, dışarıya belirli işlemler sunan bağımsız bir uygulama parçası. Ona ancak **ağ üzerinden** ulaşılır.
- **Ağ çağrısı (network call)**: iki ayrı süreç/bilgisayar arasında kablo veya kablosuz üzerinden mesaj gitmesi. Veri baytlara çevrilir (**serileştirme**), gönderilir, karşıda geri çözülür.
- **Protokol**: iki tarafın mesajlaşırken uyduğu ortak kural takımı (ör. HTTP).
- **Gecikme (latency)**: isteğin gidip yanıtın dönmesi arasında geçen süre. Aynı veri merkezinde tipik olarak 0,5–2 milisaniye; fonksiyon çağrısından yaklaşık bir milyon kat yavaş.
- **Kısmi hata (partial failure)**: dağıtık sistemde bir parçanın çalışıp diğerinin çökmesi. Tek süreçte ya hepsi çalışır ya hiçbiri; ağda "belki" diye üçüncü bir hâl vardır.
- **Dağıtım (deployment)**: yazılımın yeni sürümünün sunucuya kurulup çalıştırılması. **Dağıtım birimi**, birlikte kurulan paket.
- **Ölçeklenmek (scaling)**: artan yükü karşılamak için kapasite büyütmek. **Dikey**: makineyi güçlendirmek. **Yatay**: aynı uygulamadan çok sayıda kopya (**örnek / instance**) çalıştırmak.
- **Transaction ve ACID**: birden çok veri değişikliğini "ya hep ya hiç" yapan tek işlem. Tek veritabanı içinde garanti edilir; iki ayrı veritabanı arasında edilmez.
- **Kullanılabilirlik (availability)**: sistemin çalışır olduğu zaman oranı. %99,9 demek yılda yaklaşık 8,8 saat kesinti demektir.
- **Monolit**: tüm işlevlerin tek dağıtım birimi, tek süreç ve genelde tek veritabanı içinde olduğu mimari.
- **Mikroservis**: her biri bağımsız dağıtılan, bağımsız ölçeklenen ve **kendi veritabanına sahip** (database-per-service) küçük servisler; aralarındaki her konuşma bir ağ çağrısıdır.

### Nasıl çalışır?

Monolitte "sipariş" modülü "stok" modülünü çağırdığında bu bir **fonksiyon çağrısıdır**: pratikte bedava, tip güvenli, ve hata olursa tek transaction geri alınır. Bedeli kod büyüyünce görünür: modüller birbirine sızar (coupling), tek bir kütüphane yükseltmesi herkesi etkiler, ekipler aynı dağıtım kuyruğunda sıraya girer.

Mikroservise bölünce **aynı çağrı ağ üzerinden** yapılır. Kazanç açıktır: her servis ayrı ölçeklenir, ayrı dağıtılır, ayrı ekip sahiplenir. Bedel ise üç başlıkta toplanır:

1. **Ağ**: gecikme, kısmi hata ve serileştirme maliyeti eklenir.
2. **Tutarlılık**: tek veritabanının ACID garantisi kaybolur.
3. **Operasyon**: izleme, servis keşfi, servis başına ayrı CI/CD hattı ve dağıtık iz sürme gerekir.

İki ek not. Bellek kullanımı da artar, çünkü her servis kendi çalışma zamanını (JVM, kütüphaneler) ayrıca taşır. Derleme işi de ortadan kalkmaz; servis başına çoğalır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Monolit, mikroservis ve distributed monolith karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç mimari, üç maliyet profili</text>
  <rect x="24" y="44" width="185" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Monolit</text>
  <rect x="52" y="82" width="128" height="34" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="116" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tek süreç · tek DB</text>
  <text x="116" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yerel çağrı = ucuz</text>
  <text x="116" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">büyürken sızma riski</text>
  <rect x="228" y="44" width="185" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Mikroservis</text>
  <rect x="248" y="82" width="46" height="26" rx="5" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <rect x="298" y="82" width="46" height="26" rx="5" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <rect x="348" y="82" width="46" height="26" rx="5" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="320" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımsız dağıtım</text>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ağ + operasyon maliyeti</text>
  <text x="320" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağımsız EKİP gerekir</text>
  <rect x="432" y="44" width="185" height="140" rx="12" fill="#FBEAEA" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="524" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B23B3B" font-family="system-ui">Distributed Monolith</text>
  <rect x="452" y="82" width="46" height="26" rx="5" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1"/>
  <rect x="548" y="82" width="46" height="26" rx="5" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1"/>
  <line x1="498" y1="95" x2="548" y2="95" stroke="#B23B3B" stroke-width="2.5"/>
  <text x="524" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıkı bağlı, birlikte</text>
  <text x="524" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dağıtılmak zorunda</text>
  <text x="524" y="168" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">her ikisinin en kötüsü</text>
</svg>
```

Bölmenin en sinsi hatası **senkron zincir** kurmaktır: A çağırır B'yi, B çağırır C'yi, C çağırır D'yi ve hepsi yanıtı bekler. Burada iki matematik birden aleyhine işler.

Birincisi, **gecikmeler toplanır** — en yavaşa eşitlenmez. A'nın toplam süresi B, C ve D'nin sürelerinin toplamını içerir. İkincisi, **kullanılabilirlikler çarpılır**. Her biri %99,9 ise zincirin tamamı 0,999⁴ ≈ %99,6'ya iner. Üstelik tek bir halka düşerse tüm istek başarısız olur; arıza zincir boyunca yayılır, buna **zincirleme çöküş (cascading failure)** denir.

Panzehir dörttür: eşzamansız (asenkron) olay tabanlı iletişim, **zaman aşımı (timeout)**, **devre kesici (circuit breaker)** ve zinciri düzleştirmek.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Senkron çağrı zincirinde kullanılabilirlik çarpımı ve gecikme toplamı">
  <defs>
    <marker id="monolit-vs-mikroservis-ok" markerWidth="9" markerHeight="9" refX="7.5" refY="3" orient="auto">
      <path d="M0,0 L7.5,3 L0,6 Z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Senkron zincir: kullanılabilirlik çarpılır, gecikme toplanır</text>
  <rect x="34" y="60" width="118" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="93" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">A</text>
  <text x="93" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">%99.9</text>
  <rect x="188" y="60" width="118" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="247" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">B</text>
  <text x="247" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">%99.9</text>
  <rect x="342" y="60" width="118" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="401" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">C</text>
  <text x="401" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">%99.9</text>
  <rect x="496" y="60" width="118" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="555" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">D</text>
  <text x="555" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">%99.9</text>
  <line x1="154" y1="86" x2="186" y2="86" stroke="#8A2E2E" stroke-width="2" marker-end="url(#monolit-vs-mikroservis-ok)"/>
  <line x1="308" y1="86" x2="340" y2="86" stroke="#8A2E2E" stroke-width="2" marker-end="url(#monolit-vs-mikroservis-ok)"/>
  <line x1="462" y1="86" x2="494" y2="86" stroke="#8A2E2E" stroke-width="2" marker-end="url(#monolit-vs-mikroservis-ok)"/>
  <text x="320" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">0.999⁴ ≈ %99.6 · gecikmeler toplanır · tek halka düşerse zincir düşer</text>
  <text x="320" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Bu, distributed monolith'in tipik işaretidir</text>
</svg>
```

Mikroservisin faturası aslında teknik değil **örgütseldir**. **Conway Yasası** (1967): *"Bir sistem tasarlayan örgütler, kaçınılmaz olarak kendi iletişim yapılarının kopyası olan tasarımlar üretir."* Yani mimari sınırlar er ya da geç ekip sınırlarına oturur. Bağımsız dağıtım yapabilen bağımsız ekipler yoksa mikroservis tüm dağıtık maliyeti ödetir ama örgütsel faydayı vermez. Bu yüzden modern uygulamada "Ters Conway Manevrası" (Inverse Conway Maneuver) ile önce istenen mimariye uygun ekipler kurulur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Conway Yasası: mimari ekip iletişim yapısını yansıtır">
  <defs>
    <marker id="monolit-vs-mikroservis-yansi" markerWidth="10" markerHeight="10" refX="8" refY="3.5" orient="auto">
      <path d="M0,0 L8,3.5 L0,7 Z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Conway Yasası: mimari, ekiplerin iletişimini yansıtır</text>
  <text x="128" y="50" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Örgüt (ekipler)</text>
  <rect x="48" y="60" width="160" height="30" rx="7" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="128" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Ekip A</text>
  <rect x="48" y="100" width="160" height="30" rx="7" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="128" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Ekip B</text>
  <rect x="48" y="140" width="160" height="30" rx="7" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="128" y="160" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Ekip C</text>
  <text x="512" y="50" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Mimari (servisler)</text>
  <rect x="432" y="60" width="160" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="512" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Servis A</text>
  <rect x="432" y="100" width="160" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="512" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Servis B</text>
  <rect x="432" y="140" width="160" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="512" y="160" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Servis C</text>
  <line x1="216" y1="115" x2="424" y2="115" stroke="#6B3FA0" stroke-width="2.2" marker-end="url(#monolit-vs-mikroservis-yansi)"/>
  <text x="320" y="107" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">kopyalar / yansır</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Bağımsız dağıtan bağımsız ekip yoksa mikroservis örgütsel fayda üretmez</text>
</svg>
```

Veri tarafında ise **servis başına veritabanı (database-per-service)** ilkesi tek veritabanı ACID'ini elinden alır. İki servisi birden ilgilendiren bir iş akışı (sipariş + stok) artık tek işleme (transaction) sığmaz.

Dağıtık işlem (2PC/XA) teoride mümkündür. Ama kilit tutar, ölçeklenmez ve koordinatör arızalanınca bloke olur; bu yüzden genelde kaçınılır.

Yerine **Saga (adım adım telafili işlem zinciri)** kullanılır. Akış bir dizi **yerel işleme** bölünür. Bir adım başarısız olursa, önceki adımların etkisini geri alan **telafi işlemleri (compensating transactions)** çalıştırılır. Sonuç **nihai tutarlılıktır (eventual consistency)**: anlık değildir, ama iş kurallarını koruyarak zamanla doğru duruma yakınsar. Saga iki biçimde kurulur: **koreografi** (servisler birbirini olaylarla tetikler) veya **orkestrasyon** (adımları merkezî bir koordinatör yönetir).

### Mülakatta ne sorulur?

- **En temel takas**: yerel çağrı → ağ çağrısı. Buna değecek somut bir fayda yoksa bölme.
- **Sayılar**: 0,999⁴ ≈ %99,6; senkron zincirde gecikmeler toplanır, kullanılabilirlikler çarpılır.
- **Conway Yasası**: tam ifadesi ve mikroservis kararına etkisi; Ters Conway Manevrası.
- **Distributed monolith**: servislere bölünmüş ama sıkı bağlı; belirtileri paylaşılan veritabanı, senkron zincirler, birlikte dağıtım zorunluluğu (lock-step deployment). Monolitin tüm bağımlılığı + ağın tüm maliyeti = her iki dünyanın en kötüsü.
- **Meşru gerekçeler**: bağımsız ölçeklenme, bağımsız dağıtan çok ekip, teknoloji/hata izolasyonu. Meşru olmayanlar: moda, CV, "yeni proje olduğu için", tek geliştirici.
- **Tutarlılık**: 2PC neden varsayılan değil, Saga ve telafi işlemleri nasıl çalışır.
- **Tuzak sorusu**: "Mikroservis daha hızlı mıdır?" Hayır — ağ eklendiği için tek istek genelde yavaşlar; kazanç bağımsız ölçek ve bağımsız dağıtımdır.

### Tek cümlede

Mikroservis, ucuz fonksiyon çağrılarını pahalı ağ çağrılarına çevirir; karşılığında bağımsız dağıtım ve bağımsız ölçek satın alır. Bu takas ancak bağımsız ekipler ve gerçek bir ölçek baskısı varsa kârlıdır. Aksi hâlde elde kalan şey dağıtık monolittir.

# mono-1 | Temel
## Soru
Monolitten mikroservise geçince "bedava" olan hangi şey artık maliyetli hale gelir?
## A
Modüller arası iletişim: yerel fonksiyon çağrısının yerini ağ çağrısı alır
🤖 Doğru: süreç-içi çağrı ağa taşınınca gecikme, kısmi hata ve serileştirme bedeli doğar.
## B
Bellek kullanımı: servisler ayrıldığı için toplam bellek tüketimi kendiliğinden düşer
🤖 Tam tersi olur: her servis kendi çalışma zamanını taşıdığı için toplam bellek genelde artar.
## C
Derleme: mikroserviste kod derlenmez, her servis doğrudan yorumlanarak çalıştırılır
🤖 Her servis yine derlenir; bölünme derlemeyi kaldırmaz, servis başına çoğaltır.
## D
Veri sahipliği: her servis ortak tek bir veritabanına yazmak zorunda kalır
🤖 Mikroservisin ilkesi kendi veritabanına sahip olmaktır; ortak veritabanı kaçınılan bir desendir.
## Doğru
A
## Açıklama
Monolitte modüller aynı süreçte olduğu için aralarındaki iletişim **yerel fonksiyon çağrısıdır** — nanosaniyeler sürer, yolda kaybolmaz, tip güvenlidir. Mikroservise bölünce bu iletişim **ağ çağrısına** dönüşür: gecikme (latency) milisaniyelere çıkar, karşı taraf yavaşlayabilir veya düşebilir (kısmi hata / partial failure), veri serileştirilip (JSON/Protobuf) tekrar çözülmelidir. "Dağıtık hesaplamanın yanılgıları" (fallacies of distributed computing) tam da bunu anlatır: ağ güvenilir değildir, gecikme sıfır değildir. Diğer şıklar yanlış yönü gösterir: bellek azalmaz, her servis kendi çalışma zamanını taşıdığı için artar; derleme ortadan kalkmaz, servis başına çoğalır; ortak veritabanı ise mikroservisin kuralı değil, kaçındığı desendir. Bu yüzden bölme kararı, ağ maliyetini kaldıracak somut bir fayda gerektirir.

# mono-2 | Temel
## Soru
Conway Yasası mimari kararları nasıl etkiler?
## A
Bir sistemdeki servis sayısı her zaman ekip sayısının iki katı olmalıdır
🤖 Böyle sayısal bir kural yoktur; Conway Yasası oran değil, yapı yansıması hakkındadır.
## B
Bir örgüt, kendi iletişim yapısının kopyası olan bir tasarım üretir
🤖 Doğru: Conway'in özgün ifadesi budur; ekip iletişim yapısı mimariye yansır.
## C
Mikroservis mimarisi, ekip yapısından bağımsız olarak monolitten daha performanslıdır
🤖 Bu Conway Yasası değil; üstelik ağ maliyeti nedeniyle tek istek performansı genelde düşer.
## D
Bir yazılım eskidikçe hata oranı artar, bu yüzden düzenli olarak yeniden yazılmalıdır
🤖 Bu "yazılım çürümesi" hakkında ayrı bir gözlemdir; Conway Yasası örgüt-mimari ilişkisini anlatır.
## Doğru
B
## Açıklama
**Conway Yasası** (1967): *"Bir sistem tasarlayan örgütler, kaçınılmaz olarak kendi iletişim yapılarının kopyası olan tasarımlar üretir."* Bunun mikroservis kararına doğrudan etkisi vardır: mimari sınırlar er ya da geç **ekip sınırlarına** oturur. Dolayısıyla mikroservisin asıl kazancı teknik değil **örgütseldir** — bağımsız dağıtım yapabilen bağımsız ekiplerin paralel çalışabilmesi. Tek ekip çok sayıda servis işletiyorsa mimari örgüte uymaz; tüm dağıtık maliyet ödenir ama örgütsel fayda alınmaz. Yasa sayısal bir oran (A) vermez, performans hakkında (C) bir iddia içermez ve kod yaşlanmasıyla (D) ilgili değildir. Modern uygulamada "Ters Conway Manevrası" (Inverse Conway Maneuver) ile önce istenen mimariye uygun ekipler kurulur, mimarinin o yapıyı izlemesi beklenir.

# mono-3 | Orta
## Soru
Bir ekip senkron REST çağrılarıyla A→B→C→D zinciri kuran mikroservisler yazdı. Bu tasarımın en büyük gizli riski nedir?
## A
Servisler farklı programlama dillerinde yazılamaz, hepsi aynı dile mahkûm olur
🤖 REST diller arası çalışır; dil çeşitliliği bu tasarımın sorunu değildir.
## B
Zincir yapısı yüzünden servisler kendi veritabanına sahip olamaz, ortak veritabanı zorunlu olur
🤖 Zincir, veri sahipliğini belirlemez; sorun bağımsızlık değil senkron zincirin kırılganlığıdır.
## C
Tek bir halkanın gecikmesi ya da hatası tüm zinciri etkiler; kullanılabilirlik çarpımsal düşer
🤖 Doğru: senkron zincirde kullanılabilirlikler çarpılır, gecikmeler toplanır, tek arıza hepsini düşürür.
## D
Zincirin toplam gecikmesi en yavaş servisin gecikmesine eşit kalır ve bu ölçeklemeyi engeller
🤖 Senkron zincirde gecikmeler toplanır, en yavaşa eşitlenmez; asıl risk de çarpımsal kırılganlıktır.
## Doğru
C
## Açıklama
Senkron çağrı zincirinde **kullanılabilirlik çarpılır**: her servis %99,9 uptime'a sahipse, 4'lü zincirin toplam kullanılabilirliği 0,999⁴ ≈ %99,6'ya düşer. **Gecikmeler ise toplanır** — A'nın yanıt süresi B+C+D'nin toplamını içerir, D'nin (en yavaşın) süresine eşitlenmez. Dahası, zincirin herhangi bir halkası düşerse tüm istek başarısız olur ve **kaskad hatalar** (cascading failures) oluşur. Bu aslında bir **distributed monolith** işaretidir: servisler fiziksel olarak ayrı ama senkron olarak sıkı bağlıdır. Çözüm yönü: asenkron/olay tabanlı mesajlaşma, zaman aşımı + **devre kesici** (circuit breaker), ve zinciri düzleştirip bağımlılık sayısını azaltmaktır.

# mono-4 | Orta
## Soru
"Distributed monolith" (dağıtık monolit) anti-deseninin tanımı aşağıdakilerden hangisidir?
## A
Tek süreçte çalışan, çok modüllü ve iyi katmanlanmış bir uygulama
🤖 Bu sadece iyi yapılandırılmış bir monolit; ne dağıtıktır ne de anti-desendir.
## B
Her servisin bağımsız dağıtıldığı, asenkron haberleşen olgun bir mikroservis mimarisi
🤖 Bu sağlıklı mikroservis hedefidir; distributed monolith bunun sıkı bağlı tersidir.
## C
Çok sayıda küçük servisin tek bir fiziksel sunucu üzerinde birlikte çalıştırılması
🤖 Aynı makinede çalışmak sorun değildir; belirleyici olan bağımsız dağıtılıp dağıtılamadıklarıdır.
## D
Servislere bölünmüş ama sıkı bağlı olduğundan hepsinin birlikte dağıtılması gereken yapı
🤖 Doğru: fiziksel olarak ayrı, mantıksal olarak bağlı — bağımsız dağıtım imkânsızdır.
## Doğru
D
## Açıklama
**Distributed monolith**, mikroservise geçişin en pahalı başarısızlığıdır: sistem servislere **fiziksel olarak** bölünmüştür ama **mantıksal olarak** hâlâ sıkı bağlıdır (tight coupling). Belirtileri: paylaşılan veritabanı veya ortak tablolar, senkron çağrı zincirleri, bir servisin API değişikliğinin diğerlerini kırması ve servislerin **birlikte dağıtılmak** zorunda olması (lock-step deployment). Belirleyici ölçüt bu son maddedir — servislerin aynı makinede çalışması (C) tek başına sorun değildir, bağımsız dağıtılamamaları sorundur. Sonuç: monolitin tüm bağımlılık sıkıntısı korunur, üstüne ağ gecikmesi, kısmi hata ve operasyonel yük eklenir — "her iki dünyanın en kötüsü". Kaçınmak için: gerçek veri sahipliği (database-per-service), asenkron/olay tabanlı iletişim ve geriye uyumlu (backward-compatible) API sözleşmeleri gerekir.

# mono-5 | İleri
## Soru
Aşağıdakilerden hangisi mikroservise geçişi gerçekten haklı çıkaran bir gerekçedir?
## A
Bileşenlerin bağımsız ölçeklenmesi ve bağımsız dağıtan çok sayıda ekibin paralel çalışması
🤖 Doğru: bağımsız ölçek ve bağımsız dağıtan çok ekip, dağıtık maliyeti karşılayan gerçek gerekçedir.
## B
CV'de "mikroservis deneyimi" yazması ve mimarinin modern görünmesi isteği
🤖 Bu teknik ya da örgütsel bir fayda değildir; yalnızca maliyet getirir.
## C
Sıfırdan başlayan, henüz kullanıcısı ve netleşmiş alan sınırları olmayan yeni bir proje
🤖 Sınırlar belirsizken bölmek yanlış sınırları betona döker; önce monolitle başlanır.
## D
Tek geliştiricinin yazıp işlettiği, kod tabanını küçük parçalarda tutmak istediği proje
🤖 Tek geliştiricide operasyonel yük ezicidir; küçük dosya isteği bölme gerekçesi değildir.
## Doğru
A
## Açıklama
Mikroservisin dağıtık maliyeti (ağ, tutarlılık, operasyon) ancak **somut bir kısıt** onu zorunlu kılınca değer üretir. Meşru gerekçeler: (1) **bağımsız ölçeklenme** — bir bileşen (ör. ödeme, arama) diğerlerinden farklı oranda ölçeklenmeli; (2) **örgütsel ölçek** — çok sayıda ekip birbirini beklemeden bağımsız dağıtabilmeli (Conway); (3) **teknoloji/dayanıklılık izolasyonu** — bir bileşenin farklı bir çalışma zamanı veya hata izolasyonu gerektirmesi. B "modaya uyma", C "erken optimizasyon" (sistem hakkında en az bilginin olduğu anda sınır çizme) ve D "gereksiz operasyon yükü" tuzaklarıdır; kodu küçük parçalarda tutmak isteniyorsa doğru araç mikroservis değil, modüler monolittir. Genel bilgelik: **"monolitle başla, ihtiyaç kanıtlanınca parçala"** (start with a monolith).

# mono-6 | İleri
## Soru
Mikroservislerde her servis kendi veritabanına sahipse (database-per-service), sipariş oluşturma ile stok düşme gibi iki servisi kapsayan bir iş akışında tutarlılık nasıl sağlanır?
## A
İki veritabanını kapsayan dağıtık ACID transaction (2PC) modern mikroserviste varsayılan yoldur
🤖 2PC kilit tutar, ölçeklenmez ve koordinatör arızasında bloke olur; varsayılan değil, kaçınılan yoldur.
## B
Nihai tutarlılık ve Saga: her adım yerel transaction yapar, hata olursa telafi işlemi çalışır
🤖 Doğru: servisler arası tutarlılık Saga ile sağlanır; başarısızlık telafi işlemleriyle geri alınır.
## C
Tutarlılık gerekmez; mikroserviste kalıcı veri tutarsızlığı normal sayılıp göz ardı edilir
🤖 Tutarsızlık göz ardı edilmez; nihai tutarlılıkla yönetilir, aksi hâlde iş kuralları bozulur.
## D
Tüm servisler tek bir paylaşılan veritabanına yazarak tutarlılığı garanti eder
🤖 Paylaşılan veritabanı database-per-service ilkesini bozar ve distributed monolith üretir.
## Doğru
B
## Açıklama
Database-per-service, tek veritabanının **ACID** garantisini ortadan kaldırır; iki servisi kapsayan işlem artık tek transaction'a sığmaz. Klasik çözüm dağıtık transaction (2PC/XA) olabilirdi ama 2PC **kilit tutar, ölçeklenmez ve koordinatör arızasında bloke olur** — bu yüzden modern mikroserviste genelde kaçınılır. Yerine **Saga deseni** kullanılır: iş akışı bir dizi **yerel transaction**'a bölünür; her servis kendi adımını commit eder, bir adım başarısız olursa önceki adımları geri alan **telafi işlemleri** (compensating transactions) çalışır. Sonuç **nihai tutarlılıktır** (eventual consistency) — anlık değil ama iş kurallarını koruyarak yakınsayan tutarlılık; bu, tutarsızlığı görmezden gelmekle (C) aynı şey değildir. Saga iki biçimde kurgulanır: koreografi (olaylarla) veya orkestrasyon (merkezi koordinatör). Paylaşılan veritabanı (D) sorunu "çözer" gibi görünse de mimarinin temel ilkesini bozar.
