---
slug: gozlemlenebilirlik-uc-sutun
baslik: "Gözlemlenebilirlik: Üç Sütun, OpenTelemetry, Cardinality Maliyeti ve SLO Tabanlı Alerting"
kademe: İleri
sira: 193
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Kapalı bir kutunun içinde bir makine çalışıyor; kutuyu açman yasak. Elinde yalnız kutunun dışarı verdiği sesler, ışıklar ve fişler var. **Gözlemlenebilirlik** (observability) budur: bir yazılımın dışarı verdiği izlere bakarak içeride ne olduğunu anlayabilme derecesi.

Neden gerekir? Üretimde (production — gerçek kullanıcıların kullandığı ortam) sorunlar öngördüğün biçimde çıkmaz: "dün gece 03:00'te yalnız mobil kullanıcıların ödemesi neden yavaşladı?" sorusu hazır panolarda yoktur. Gözlemlenebilirlik, **önceden aklına gelmemiş soruları sonradan sorabilmektir**.

### Temel kavramlar

- **Log (günlük kaydı):** programın "şu an şunu yaptım" diye tuttuğu, zaman damgalı tek tek not satırları — defter gibi. En zengin bilgiyi verir ("tam olarak ne oldu"), ama hacim arttıkça saklaması pahalanır. Düz cümle yerine alan–değer çiftleri (JSON) hâlinde yazılana **yapılandırılmış log** (structured log) denir; makine onu kolayca arar.
- **Metrik (ölçüm):** sayı. "Son bir dakikada 1200 istek", "hata oranı %2". Aynı ölçümün zamana göre dizilmiş hâli **zaman serisi**dir (time series). Ucuzdur, maliyeti neredeyse sabittir; grafikler ve alarmlar buradan kurulur, ama "hangi kullanıcı" ayrıntısını taşımaz.
- **İzleme kaydı (trace):** tek bir isteğin servisten servise dolaşırken bıraktığı yol haritası. Kargo takip numarası gibi düşün. Bu yoldaki her durak bir **izleme adımıdır (span)** — örneğin "veri tabanı sorgusu 40 ms". "Nerede yavaşladı" sorusunu yanıtlar.

Bu üçüne **üç sütun** (three pillars) denir: metrik "ne kadar", izleme kaydı "nerede", log "tam olarak ne" sorusunu yanıtlar. Dört kavram daha var. **Etiket (label)**, metriğe iliştirilen ayırt edici bilgidir (`route=/odeme`, `status=500`). **Hizmet göstergesi (SLI)**, kullanıcının hissettiği ölçümdür (başarı oranı, p99 gecikme). **Hizmet hedefi (SLO)**, o göstergeye koyduğun hedeftir (%99,9). **Hata bütçesi (error budget)** ise hedefin izin verdiği başarısızlık payıdır (%0,1). Koda bu sinyalleri üretecek satırları eklemeye **enstrümantasyon** denir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç sütun ve korelasyon ekseni">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç sütun — asıl değer korelasyonda</text>
  <rect x="26" y="44" width="180" height="96" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Metrikler</text>
  <text x="116" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"kaç / ne kadar"</text>
  <text x="116" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ucuz · sabit maliyet</text>
  <text x="116" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">alarm + dashboard</text>
  <rect x="230" y="44" width="180" height="96" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">İzler (traces)</text>
  <text x="320" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"nerede yavaş"</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">span'ler · servis yolu</text>
  <text x="320" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dağıtık röntgen</text>
  <rect x="434" y="44" width="180" height="96" rx="12" fill="#FBF0E4" stroke="#C77A17" stroke-width="1.5"/>
  <text x="524" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17" font-family="system-ui">Loglar</text>
  <text x="524" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"tam ne oldu"</text>
  <text x="524" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">zengin bağlam</text>
  <text x="524" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hacimle pahalı</text>
  <rect x="26" y="160" width="588" height="34" rx="10" fill="#F0F3F5" stroke="#5D7789" stroke-width="1.2"/>
  <text x="320" y="182" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">ortak eksen: trace_id (metrik exemplar → trace → structured log)</text>
</svg>
```

### Nasıl çalışır?

**Korelasyon (bağ kurma).** Üç sütunu ayrı ayrı toplamak yetmez; güç aralarında gidip gelebilmektir. Her isteğe başında benzersiz bir kimlik verilir: `trace_id`. Bu kimlik hem yapılandırılmış logların içine alan olarak yazılır, hem metriklere **exemplar** (örnek nokta) olarak iliştirilir. Böylece şu akış saniyeler sürer: grafikte sıçramayı gördüm → o noktanın örnek izine tıkladım → yavaş izleme adımını buldum → aynı `trace_id`'li logları okudum.

Panoları yan yana açıp saat damgasıyla gözle eşleştirmek yüksek trafikte işlemez. Üç sinyali aynı satıcının ürününde toplamak da tek başına bağlamaz; bağlayan şey paylaşılan kimliktir.

**OpenTelemetry (OTel).** Üç sinyali de tek ve satıcı-bağımsız (vendor-neutral) bir standartla üreten API/SDK, artı bir ara katman: **Collector**. Uygulama veriyi OTLP ile Collector'a gönderir; Collector süzer, zenginleştirir, örnekler ve istediğin ürüne (Datadog, Grafana, Jaeger…) iletir. OTel bir depolama ya da pano ürünü **değildir**, veriyi saklamaz, uygulamayı hızlandırmaz; işi enstrümantasyonu backend'den ayırmaktır. Sağlayıcı değiştirmek Collector ayarını değiştirmektir, binlerce satır kodu değil.

**Etiket çeşitliliği patlaması (cardinality).** Etiket çeşitliliği, bir etiketin alabileceği farklı değer sayısıdır. Bir metriğin maliyeti, ürettiği zaman serisi sayısıyla orantılıdır; bu sayı da etiket değerlerinin çarpımıdır.

`route(5) × status(4) × region(3) = 60` seri rahattır. Üstüne `user_id` (2 milyon farklı değer) eklersen 120 milyon seri olur; bellek ve disk çöker. Kural şudur: metriğe yalnız az değerli etiketler konur. Kullanıcı ya da istek kimliği gibi çok değerli bağlam bilgisi log ile izleme kaydına aittir. Histogram kovaları (bucket) da seri ürettiği için sorunu çözmez, yalnız başka yere taşır.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cardinality patlaması: seri sayısı label değerlerinin çarpımıdır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Cardinality: seri sayısı = label değerlerinin çarpımı</text>
  <rect x="26" y="44" width="588" height="60" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="46" y="68" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Güvenli (düşük kardinalite)</text>
  <text x="46" y="92" font-size="11" fill="#1C3247" font-family="system-ui">route(5) × status(4) × region(3) = 60 seri → TSDB rahat</text>
  <rect x="26" y="116" width="588" height="66" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="46" y="140" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Tehlikeli (yüksek kardinalite)</text>
  <text x="46" y="162" font-size="11" fill="#1C3247" font-family="system-ui">... × user_id(2.000.000) = 120 milyon seri → bellek / disk patlar</text>
  <text x="46" y="178" font-size="10.5" fill="#5D7789" font-family="system-ui">yüksek-kardinalite bağlam metriğe değil, trace / log'a ait</text>
</svg>
```

**Alarm.** İki çerçeve sık karıştırılır: **RED** istek-güdümlü servisler içindir (Rate = istek hızı, Errors = hata oranı, Duration = süre); **USE** kaynaklar içindir (Utilization = kullanım, Saturation = doygunluk, Errors = hata). RED belirtiyi, USE nedeni gösterir; ikisi de metrikten hesaplanır. "CPU %90'ı geçti" gibi **nedene** alarm kurmak boş yere alarm çaldırır: yüksek CPU her zaman kullanıcıyı etkilemez, düşük CPU da sorunsuzluğu garanti etmez. Doğrusu **hizmet hedefine (SLO) dayalı alarmdır**: hizmet göstergesine bak ve hata bütçesinin **tüketim hızına** (burn rate) göre alarm ver.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SLO hata bütçesi ve burn rate tabanlı alarm">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">SLO alarmı: nedene değil, bütçe tüketim hızına bak</text>
  <text x="40" y="52" font-size="11" fill="#1C3247" font-family="system-ui">Hata bütçesi (SLO'nun izin verdiği başarısızlık payı)</text>
  <rect x="40" y="60" width="560" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="40" y="60" width="150" height="26" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.2"/>
  <text x="115" y="78" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">tüketilen</text>
  <rect x="40" y="106" width="270" height="70" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="175" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Hızlı burn</text>
  <text x="175" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1 saatte bütçenin %2'si</text>
  <text x="175" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ hemen sayfala (page)</text>
  <rect x="330" y="106" width="270" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="465" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Yavaş burn</text>
  <text x="465" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">günlerce sürecek tüketim</text>
  <text x="465" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ ticket aç (çok-pencere)</text>
</svg>
```

Google SRE deseni **çok-pencereli, çok-oranlı** (multi-window multi-burn-rate) alarmdır: 1 saatte bütçenin %2'si gibi hızlı tükeniş anında telefon çaldırır (page), günlere yayılan yavaş tükeniş ticket açar. Alarm ortadan kalkmaz; gürültüsüz ve aciliyetle orantılı olur.

**Örnekleme (sampling).** Her izleme kaydını saklamak pahalıdır. **Baştan örneklemede (head-based)** karar, izin ilk adımında ve sonucu bilmeden verilir ("%1'ini tut"). Ucuzdur ve hafızada durum tutmaz; ama nadir olduğu hâlde değerli izleri (hata, p99 yavaşlık) kör şansa bırakır.

**Sondan örneklemede (tail-based)** ise Collector, izin tüm adımlarını iz tamamlanana kadar bekletir. Kararı sonra verir; kural şöyle olabilir: hatalı ya da 2 saniyeden uzun her iz kalsın. Bedeli vardır. Collector durum tutmak zorunda kalır, bellek harcar ve aynı izin bütün adımları aynı Collector örneğine yönlendirilmelidir. İki yöntemin farkı oran değil, **kararın ne zaman verildiğidir**.

### Mülakatta ne sorulur?

Altı başlık sorulur:

- Üç sütunun farkı ve RED/USE çerçevelerinin nereye oturduğu.
- "Metriğe `user_id` etiketi ekleyelim mi" tuzağı. Refleks cevap: etiket çeşitliliği patlar.
- Hizmet hedefi ve bütçe tüketim hızı alarmının, CPU eşiğine neden üstün olduğu.
- `trace_id` ile örnek nokta üzerinden korelasyon deseni.
- OTel'in ne olduğu ve ne olmadığı.
- Baştan ve sondan örneklemenin ödünü.

Kıdem sinyali, her cevapta maliyeti ve ödünü kendiliğinden söylemektir.

### Tek cümlede

Gözlemlenebilirlik, metrik–izleme kaydı–log'u ortak `trace_id` ekseninde birleştirip etiket çeşitliliğini ve örnekleme bütçesini bilinçle yönetmek, alarmı ise nedene değil kullanıcıyı etkileyen belirtiye ve hata bütçesinin tüketim hızına bağlamaktır.

# obs3-1 | Orta
## Soru
Üretim izlemede **RED** ve **USE** metodolojileri sık karıştırılır. İkisinin doğru kapsamı hangisidir?
## A
RED altyapı kaynakları içindir (CPU, disk, ağ); USE ise istek-güdümlü servislerin uç-nokta sağlığını ölçer
🤖 Tam ters eşleştirme: RED istekler için, USE kaynaklar içindir.
## B
RED istek-güdümlü servisler içindir (Rate, Errors, Duration); USE ise kaynaklar içindir (Utilization, Saturation, Errors)
🤖 Doğru: RED servis/uç-nokta sağlığına, USE altyapı kaynağı doygunluğuna bakar.
## C
İkisi de aynı çerçevedir; RED, USE'nin yeni adıdır ve tam olarak aynı üç ölçümü farklı harflerle anlatır
🤖 Farklı odakları olan iki ayrı çerçevedir; biri diğerinin yeni adı değildir.
## D
Ayrımı hangi sütundan beslendikleri belirler: RED yalnız loglardan, USE yalnız izlerden (trace) hesaplanır
🤖 İkisi de temelde metriktir; sütun kaynağıyla tanımlanmazlar.
## Doğru
B
## Açıklama
**RED** (Rate = istek/sn, Errors = hata oranı, Duration = gecikme dağılımı) istek-güdümlü (request-driven) servisler ve uç-noktalar için "kullanıcı ne yaşıyor" sorusuna odaklanır. **USE** (Utilization, Saturation, Errors) ise Brendan Gregg'in kaynak (resource) merkezli çerçevesidir: her kaynak (CPU, bellek, disk, ağ kuyruğu) için kullanım, doygunluk ve hata bakılır. İkisi tamamlayıcıdır: RED belirtiyi (servis yavaş) gösterir, USE nedeni (disk doygun) daraltır. A ters eşleştirmedir; C ve D çerçevelerin doğasını yanlış tanımlar.

# obs3-2 | İleri
## Soru
Bir ekip hata ayıklamayı kolaylaştırmak için Prometheus metriğine `user_id` label'ı eklemek istiyor (aktif kullanıcı ~2 milyon). Kıdemli olarak temel itirazın nedir?
## A
Her benzersiz label kombinasyonu ayrı bir zaman serisi yaratır; `user_id` bu sayıyı patlatıp metrik veri tabanını çökertir
🤖 Doğru: kardinalite patlaması metrik sisteminin bir numaralı maliyet/kararlılık riskidir.
## B
Sorun yok; Prometheus'un maliyeti metrik adı sayısına bağlıdır, label değerleri yalnız sorgu yazımını uzatır, seri sayısını değiştirmez
🤖 maliyet metrik adında değil, label değer kombinasyonlarının (seri) sayısındadır.
## C
Bu yalnızca bir gizlilik sorunudur; kullanıcı kimliği kişisel veri olduğu için metriğe yazılmamalı, ama sistemin performansına ve maliyetine hiçbir etkisi olmaz
🤖 Gizlilik ayrı bir sorun olabilir; ama asıl teknik itiraz kardinalite patlamasıdır.
## D
Label yerine bunu histogram bucket'ı yap; histogram önceden toplandığı (aggregate) için kardinalite üretmez ve maliyet sabit kalır
🤖 Histogram da bucket başına seri üretir; kullanıcı kimliğini oraya koymak sorunu çözmez, taşır.
## Doğru
A
## Açıklama
Prometheus gibi boyutlu (dimensional) metrik sistemlerinde bir metriğin maliyeti, **etkin zaman serisi sayısıyla** (active time series) orantılıdır ve bu sayı label değerlerinin **kartezyen çarpımıdır**. `user_id` gibi milyonlarca ayrı değer alan bir label, tek bir metriği milyonlarca seriye böler — bellek (her seri RAM'de tutulur), disk, sorgu süresi ve WAL hepsi patlar. Yüksek-kardinalite veri metriğe değil, **log veya trace'e** (yüksek-kardinalite ama sorgu-zamanı indekslenen sütunlar) aittir. Modern yaklaşım: metrikte düşük-kardinalite label (route, status, region), yüksek-kardinalite bağlamı ise trace exemplar'ı ve structured log ile bağla.

# obs3-3 | İleri
## Soru
"CPU %90'ı geçince alarm" yerine kıdemli mühendis **SLO tabanlı** alerting öneriyor. Bunun temel avantajı nedir?
## A
SLO alarmları eşiği düşürdüğü için her zaman daha erken ateşler; böylece daha çok bildirim (page) alır ve hiçbir olayı kaçırmazsın
🤖 Amaç daha fazla değil, daha az ve daha anlamlı bildirimdir; "daha erken/daha çok" hedef değil.
## B
SLO tabanlı alarm metrik altyapısı gerektirmez; SLI'ları doğrudan log satırlarını sayarak hesaplar, bu yüzden ucuzdur
🤖 SLI genelde metrikten hesaplanır; log şart değil, avantaj da bu değil.
## C
Nedene (CPU) değil kullanıcı-etkili belirtiye (SLI) bakar ve hata bütçesi tüketim hızına (burn rate) göre çok-pencereli alarm kurar
🤖 Doğru: belirti + burn rate, yanlış-pozitifi keser ve bildirimi etkiyle orantılar.
## D
Alarm eşiği diye bir şey kalmaz; ekip yalnız SLO panosuna bakar ve ihlali kendi gözüyle fark eder
🤖 Alarmı kaldırmaz; alarmı belirti ve bütçe tüketimine bağlar.
## Doğru
C
## Açıklama
Neden-tabanlı alarmlar (CPU %90) **yanlış-pozitif** üretir: yüksek CPU her zaman kullanıcıyı etkilemez, düşük CPU da sorunu garanti etmez. **SLO tabanlı alerting** ise doğrudan kullanıcı deneyimini ölçen **SLI**'ya (başarı oranı, p99 gecikme) bakar ve **hata bütçesi** (SLO'nun izin verdiği başarısızlık payı) tüketim hızına — **burn rate** — göre ateşler. Google SRE deseni **çok-pencereli, çok-oranlı** (multi-window multi-burn-rate) alarmdır: hızlı tükeniş (örn. 1 saatte bütçenin %2'si) hemen sayfalar (page), yavaş tükeniş bir ticket açar. Böylece bildirim hem gürültüsüz hem aciliyetle orantılı olur. A alarmın amacını "daha çok bildirim" sanır, B SLI'nın kaynağını karıştırır, D ise alarmın tümden kalktığını varsayar — üçü de belirti + bütçe hızı fikrini ıskalar.

# obs3-4 | İleri
## Soru
Metrik dashboard'unda bir gecikme sıçraması gördün ve hemen o isteğin trace'ine ve ilgili loglarına inmek istiyorsun. Bunu mümkün kılan **korelasyon** deseni hangisidir?
## A
Üç sütunun panolarını yan yana açıp aynı saat damgasına (timestamp) bakarak olayları gözle eşleştirmek; ortak kimliğe gerek yoktur
🤖 Saniyede binlerce istekte aynı saniyeye yüzlerce olay düşer; damgayla eşleştirme tesadüfe kalır.
## B
Metrik, log ve iz'i aynı satıcının (vendor) ürününde toplamak yeterlidir; tek üründe duran veriler kendiliğinden birbirine bağlanır
🤖 Aynı ürün olması bağ kurmaz; bağı kuran, sinyallerin taşıdığı paylaşılan kimliktir.
## C
Log ve metrikleri tümden bırakıp yalnız iz (trace) toplamak; iz zaten her isteğin tüm bilgisini içerir
🤖 Sütunları teke indirmek bağlamı ve ucuz alarmı kaybettirir; korelasyon en az iki sütun ister.
## D
Ortak bir `trace_id` üret; yapılandırılmış log'a alan olarak yaz ve metriğe exemplar olarak iliştir
🤖 Doğru: paylaşılan trace_id (ve exemplar) üç sütunu tek eksende birleştirir.
## Doğru
D
## Açıklama
Korelasyonun anahtarı **paylaşılan kimliktir**: OpenTelemetry bağlam yayılımı (context propagation) her isteğe bir `trace_id`/`span_id` verir. Bu kimlik (1) **structured log**'a alan olarak yazılır (log satırından trace'e atlarsın), (2) metriklere **exemplar** olarak iliştirilir (bir histogram bucket'ındaki örnek isteğin trace_id'sini taşır — dashboard'daki noktadan trace'e tıklarsın). Böylece "metrikte sıçrama → o zaman diliminin exemplar trace'i → trace'teki yavaş span → o span'in trace_id'li logları" akışı saniyeler sürer. A saat damgasıyla gözle eşleştirmeye güvenir (yüksek trafikte imkânsız), B tek satıcıda toplamayı bağ sanır, C sütunları teke indirip bağlamı kaybeder; hiçbiri kimliğe dayalı korelasyonu kurmaz.

# obs3-5 | Orta
## Soru
Bir sistemi OpenTelemetry ile enstrümante etmenin (instrumentation), doğrudan Datadog SDK'sı kullanmaya kıyasla temel mimari avantajı nedir?
## A
Satıcı-bağımsız (vendor-neutral) tek bir API/SDK ve Collector sağlar; sağlayıcıyı değiştirmek uygulama kodunu değiştirmez
🤖 Doğru: OTel'in çekirdek değeri taşınabilirlik ve backend'den ayrışmadır (decoupling).
## B
OTel aynı zamanda bir depolama ve pano (dashboard) ürünüdür; kurunca ayrıca bir gözlemlenebilirlik backend'ine ihtiyaç kalmaz
🤖 OTel veriyi saklamaz; toplama/işleme/aktarma standardıdır, depolama backend'i ve maliyeti yine gerekir.
## C
OTel enstrümantasyonu uygulamayı hızlandırır; otomatik enstrümantasyon gereksiz çağrıları eleyip CPU kullanımını düşürür
🤖 Enstrümantasyon gözlem verisi üretir; performans iyileştirme aracı değildir, hafif ek yük getirir.
## D
OTel yalnız iz (trace) standardıdır; metrik ve loglar için yine her satıcının kendi SDK'sını kurman gerekir
🤖 OTel üç sinyali de (metrik, log, iz) tek standartta kapsar; tek sinyalle sınırlı değildir.
## Doğru
A
## Açıklama
**OpenTelemetry** (OTel), metrik/log/trace için tek bir **satıcı-bağımsız** enstrümantasyon standardı (API + SDK) ve merkezi bir **Collector** sunar. Uygulaman OTLP protokolüyle Collector'a veri gönderir; Collector veriyi işler (örnekleme, filtreleme, zenginleştirme) ve istediğin backend'e (Datadog, Grafana Tempo/Mimir, Jaeger, Honeycomb...) aktarır. Bu ayrım **satıcı kilidini** (vendor lock-in) kırar: backend değiştirmek Collector konfigürasyonunu değiştirmektir, binlerce satır enstrümantasyon kodunu değil. B OTel'i depolama/pano ürünü sanar (OTel veriyi saklamaz), C onu performans aracı sanar, D ise yalnız iz standardı olduğunu sanır — OTel üç sinyali de kapsar.

# obs3-6 | Uzman
## Soru
Yüksek trafikli bir serviste her trace'i saklamak pahalı. Ekip **head-based** (baştan, rastgele oranla) yerine **tail-based sampling** öneriyor. Tail-based'in temel üstünlüğü nedir ve maliyeti nedir?
## A
Tail-based örnekleme iz toplamaz; span'lerden yalnız metrik üretip atar, bu yüzden depolama maliyeti sıfırdır
🤖 İz toplar; kararı yalnız toplama sonrasına erteler — "hiç iz saklanmaz" yanlıştır.
## B
Tail-based durumsuzdur (stateless) ve head-based'den daha az bellek kullanır; kararı tek span'e bakarak anında verir
🤖 Tersine: iz tamamlanana dek span'leri tamponladığı için durumludur, bellek/ölçek maliyeti daha yüksektir.
## C
Karar iz tamamlandıktan sonra verilir; hatalı ve yavaş izler bilerek tutulur. Bedeli: Collector span'leri tamponlar, durumlu olur
🤖 Doğru: değer, sonuç bilindikten sonra seçmek; bedel, span'leri tutmanın durumlu maliyeti.
## D
İkisi de kararı iz'in ilk span'inde verir; tail-based'in tek farkı örnekleme oranını daha düşük tutup depolamadan daha fazla tasarruf etmesidir
🤖 Fark oran değil, kararın zamanıdır: head-based baştan, tail-based iz bittikten sonra karar verir.
## Doğru
C
## Açıklama
**Head-based sampling** kararı trace'in ilk span'inde, sonucu bilmeden verir (örn. %1'i tut) — ucuz ve durumsuzdur ama nadir-değerli trace'leri (p99 yavaşlık, 500 hatası) kör şansa bağlı olarak kaybeder. **Tail-based sampling** ise tüm span'leri Collector'da **tamponlar**, trace tamamlanınca (tüm span'ler gelince) kurala göre karar verir: "hatalı veya >2sn süren her trace'i tut, gerisinden %1". Böylece örnekleme bütçesini bilgi-yoğun trace'lere harcarsın. Bedeli mimaridir: Collector durumlu (stateful) olur, açık trace'lerin span'lerini bellekte tutar ve aynı trace'in tüm span'lerini aynı Collector örneğine yönlendirmek için tutarlı-hash tabanlı yük dağıtımı (load balancing) gerekir. A iz'in hiç saklanmadığını sanır, B durum/bellek maliyetini ters çevirir, D ise farkı "oran" sanar — oysa fark **kararın ne zaman verildiğidir**.
