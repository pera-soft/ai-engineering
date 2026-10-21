---
slug: olceklenebilirlik-slo
baslik: "Ölçeklenebilirlik ve Güvenilirlik: Yatay/Dikey Ölçek, SLA/SLO/SLI, 9'lar ve Hata Bütçesi"
kademe: İleri
sira: 151
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sistem 100 kullanıcıyla kusursuz çalışırken 10 milyon kullanıcıyla çökebilir. Kullanıcının sunucuya gönderdiği her not bir **istek (request)**, geri gelen cevap bir **yanıttır (response)**. Sistemin bir saniyede bitirebildiği istek sayısına ise **iş hacmi (throughput)** denir. **Ölçeklenebilirlik**, yük artınca bu kapasiteyi de artırabilme yeteneğidir.

İkinci yarısı **güvenilirliktir**: sistem arıza anında bile hizmet verebiliyor mu? Bu ikisi mülakatta hep aynı cümlede geçer. Sebebi şu: kapasiteyi büyütmenin yolu makine eklemektir; makine eklemenin sonucu da "bir makine ölürse ne olur?" sorusudur. Ayrıca "sistemim güvenilir" cümlesi tek başına hiçbir şey ifade etmez; güvenilirlik **sayıyla** konuşulur. Bunun dili SLI/SLO/SLA üçlüsü ve **hata bütçesidir**.

### Temel kavramlar

- **Dikey ölçekleme (scale up)**: mevcut makineyi büyütmek — daha çok işlemci, daha çok bellek. Basittir; ama bir donanım tavanı vardır ve o tek makine **SPOF**'tur (tek arıza noktası).
- **Yatay ölçekleme (scale out)**: yan yana makine eklemektir. Teoride sınırsız kapasite ve **yedeklilik (redundancy)** verir. Karşılığında bir şey ister: düğümler birbirinin yerine geçebilsin diye **durumsuzluk (statelessness)** ve koordinasyon gerekir. Bu yüzden "her zaman daha ucuz ve daha basit" değildir.
- **SLI (gösterge)**: fiilen **ölçülen** sayı — başarılı istek oranı, p99 gecikme.
- **SLO (iç hedef)**: o göstergede tutulmak istenen hedef, ör. %99.9.
- **SLA (sözleşme)**: müşteriyle yapılan, ihlalinde **cezası** olan dış anlaşma; pratikte SLO'dan daha **gevşek** tutulur ki iç hedef önce uyarı versin, sonra ceza gelsin.
- **Hata bütçesi**: `1 − SLO`. %99.9 hedefte %0.1'lik arıza payı "harcanabilir bütçe"dir.
- **Failover**: birincil bileşen düşünce yedeğinin devreye girmesi. Elle mi otomatik mi olduğu her şeyi değiştirir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SLI ölçülür, SLO hedeftir, SLA sözleşmedir; hata bütçesi 1-SLO">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">SLI → SLO → SLA ve hata bütçesi</text>
  <rect x="30" y="44" width="180" height="74" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="120" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">SLI (gösterge)</text>
  <text x="120" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ÖLÇÜLEN: başarı oranı</text>
  <rect x="228" y="44" width="180" height="74" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="318" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">SLO (iç hedef)</text>
  <text x="318" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">HEDEF: %99.9</text>
  <rect x="426" y="44" width="180" height="74" rx="11" fill="#FBEDE4" stroke="#B4531F" stroke-width="1.5"/>
  <text x="516" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#B4531F" font-family="system-ui">SLA (sözleşme)</text>
  <text x="516" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">DIŞ + ceza, daha gevşek</text>
  <rect x="30" y="140" width="521" height="26" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="551" y="140" width="55" height="26" rx="6" fill="#FBEDE4" stroke="#B4531F" stroke-width="1.2"/>
  <text x="290" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">SLO: %99.9 çalışır</text>
  <text x="578" y="158" text-anchor="middle" font-size="10" font-weight="700" fill="#B4531F" font-family="system-ui">%0.1 bütçe</text>
  <text x="320" y="186" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hata bütçesi = 1 − SLO; tükenince riskli değişiklik dondurulur</text>
</svg>
```

### Nasıl çalışır?

Kıdemli refleks şudur: **önce durumu kenara al, sonra yatay ölçekle**. Uygulama sunucuları oturum, dosya ve sayaç gibi durumları kendi içinde tutmazsa her düğüm ötekinin aynısı olur. O zaman yük dengeleyici isteği herhangi birine yollayabilir; ölen düğümün yerine de hemen yenisi konabilir. Durum, veritabanı / önbellek / nesne deposu gibi bunun için tasarlanmış katmanlara taşınır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dikey ölçek makineyi büyütür, yatay ölçek makine ekler">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dikey (scale up) vs Yatay (scale out)</text>
  <rect x="24" y="42" width="286" height="152" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="167" y="64" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">Dikey: makineyi büyüt</text>
  <rect x="132" y="150" width="70" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="127" y="118" width="80" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="120" y="82" width="94" height="34" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="167" y="103" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 büyük makine</text>
  <text x="167" y="184" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">donanım tavanı + SPOF; basit</text>
  <rect x="330" y="42" width="286" height="152" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="64" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Yatay: makine ekle</text>
  <rect x="352" y="92" width="58" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="416" y="92" width="58" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="480" y="92" width="58" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="544" y="92" width="58" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3" stroke-dasharray="4 3"/>
  <text x="381" y="116" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">n1</text>
  <text x="445" y="116" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">n2</text>
  <text x="509" y="116" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">n3</text>
  <text x="573" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+…</text>
  <text x="473" y="156" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">durumsuz + değiştirilebilir düğümler</text>
  <text x="473" y="172" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">redundancy; ~sınırsız — koordinasyon ister</text>
</svg>
```

Güvenilirlik tarafında ölçü birimi **erişilebilirliktir** (availability) ve konuşma dili "dokuz"lardır. Bir yıl yaklaşık 8760 saattir; hedefin dışında kalan pay, izin verilen kesinti süresidir. %99.9 hedef, 8760 × 0,001 ≈ 8,76 saat/yıl demektir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Erişilebilirlik dokuzları ve yıllık kesinti">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dokuzlar ve yıllık izin verilen kesinti</text>
  <rect x="40" y="40" width="560" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="130" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Erişilebilirlik</text>
  <text x="320" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yıllık kesinti</text>
  <text x="510" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ad</text>
  <rect x="40" y="72" width="560" height="28" rx="4" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1"/>
  <text x="130" y="90" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">%99</text>
  <text x="320" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">~3.65 gün</text>
  <text x="510" y="90" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">iki dokuz</text>
  <rect x="40" y="102" width="560" height="28" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1"/>
  <text x="130" y="120" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">%99.9</text>
  <text x="320" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">~8.76 saat</text>
  <text x="510" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">üç dokuz</text>
  <rect x="40" y="132" width="560" height="28" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="130" y="150" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">%99.99</text>
  <text x="320" y="150" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">~52.6 dakika</text>
  <text x="510" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">dört dokuz</text>
  <rect x="40" y="162" width="560" height="28" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="130" y="180" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">%99.999</text>
  <text x="320" y="180" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">~5.26 dakika</text>
  <text x="510" y="180" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">beş dokuz</text>
</svg>
```

Her ek dokuz izin verilen kesintiyi kabaca 10 kat kısar ve maliyeti hızla büyütür; bu yüzden dokuz sayısı "ne kadar çok o kadar iyi" değil, bilinçli bir maliyet/mühendislik takasıdır.

Hata bütçesi, bu tablodaki payı bir **mühendislik kaynağına** çevirir. Bütçe varken ekip risk alabilir: hızlı sürüm çıkarır, deney yapar, göç planlar. Bütçe tükendiğinde ise riskli değişiklikler dondurulur ve güvenilirliğe odaklanılır. Kesintiler bütçeyi **tüketir**, büyütmez. Bütçe, SLA ihlalinde ödenen ceza parası da değildir. Bu mekanizma "her hata felakettir" baskısını, hız ile güvenilirlik arasında **sayıya dayalı** bir karara dönüştürür.

Durumlu (stateful) katmanda "sıfıra yakın kesinti" cümlesi **otomatik nöbet devrini (failover)** zorunlu kılar. Diğer seçenekler yetmez. Sık yedeği olan tek büyük sunucu, geri yükleme (restore) süresi kadar kesinti demektir ve hâlâ tek arıza noktasıdır. Elle nöbet devri dakikalar ve nöbetçi bir insan ister. Soğuk yedek (cold standby) ise açılış ve ısınma gecikmesi taşır.

Doğru desen şudur: başka bir erişilebilirlik bölgesinde (AZ) hazır bekleyen bir kopya, sağlık kontrolüyle **otomatik terfi** edilir. Böylece kesinti saniyeler mertebesinde kalır.

Aşırı yük anında hedef "hiç bozulmamak" değildir; **zarif bozulmaktır (graceful degradation)**. Bunun üç aracı vardır: temel olmayan özellikleri kapatmak, bayat önbelleği sunmak ve fazla yükü bilinçli olarak atmak (load shedding). Amaç çekirdek işlevi ayakta tutmaktır. Tüm isteklere 500 dönmek zarif bozulma değil, sert arızadır. Başarısız isteği agresif biçimde yeniden denemek ise **yeniden deneme fırtınası** yaratır ve zincirleme arızayı (cascading failure) hızlandırır.

### Mülakatta ne sorulur?

- "Yatay mı dikey mi?" → Dikeyin donanım tavanı ve tek arıza noktası sorunu var; yatayın ise durumsuzluk ve koordinasyon bedeli var. "Yatay her zaman daha ucuz ve daha basit" tuzağına düşme.
- "SLI/SLO/SLA farkı nedir?" → gösterge → iç hedef → cezalı dış sözleşme; SLA, SLO'dan gevşektir.
- "%99.9 yılda ne kadar kesintidir?" → ~8,76 saat. Referans: %99 ≈ 3,65 gün, %99.99 ≈ 52,6 dakika, %99.999 ≈ 5,26 dakika.
- "Tek AZ arızasını sıfıra yakın kesintiyle nasıl atlatırsın?" → çoklu-AZ replika + sağlık kontrollü otomatik terfi.
- "Hata bütçesi ne işe yarar?" → hız/güvenilirlik dengesinde niceliksel karar; bütçe bitince riskli değişiklik dondurulur.
- "Aşırı yükte ne yaparsın?" → yük atma + özellik kapatma; retry fırtınasından kaçınma.

### Tek cümlede

Ölçeklenebilirlik, durumu kenara alıp makine eklemektir. Güvenilirlik ise hedefi bir sayıya çevirmek (SLO) ve geri kalan payı bilinçli biçimde harcamaktır (hata bütçesi).

# scale-1 | Orta
## Soru
"Yatay ölçekle" (scale out) ile "dikey ölçekle" (scale up) hakkında hangi ifade doğrudur?
## A
Dikey ölçekleme (daha büyük makine) üst sınırı ve SPOF'u olmayan bir yaklaşımdır
🤖 Tam tersi: donanım tavanı vardır ve tek makine = tek arıza noktası (SPOF).
## B
Yatay ölçekleme makine ekler; redundancy sağlar ama durumsuzluk/koordinasyon ister
🤖 Doğru: düğümler değiştirilebilir olmalı, durum kenara alınmalı; koordinasyon karmaşıklığı gelir.
## C
Yatay ölçekleme her zaman dikeyden daha ucuz ve daha basittir
🤖 Dağıtık sistem karmaşıklığı ekler; her zaman daha basit/ucuz değildir.
## D
Yatay ve dikey ölçekleme pratikte aynı şeyi anlatan iki addır
🤖 Değil; biri düğümü büyütür, diğeri düğüm ekler.
## Doğru
B
## Açıklama
**Dikey** ölçekleme tek makineyi büyütür: basittir ama bir **donanım tavanı** vardır ve o tek makine **SPOF**'tur. **Yatay** ölçekleme makine ekler: teorik olarak sınırsız kapasite ve **redundancy** verir, ama düğümlerin değiştirilebilir olması için **durumsuzluk** (durumu kenara alma) ve koordinasyon (yük dağıtımı, tutarlılık) gerekir. Bu karmaşıklık nedeniyle yatay her zaman "daha basit/ucuz" (C) değildir; ikisi de aynı şey (D) değildir. Kıdemli tasarım genelde durumu dışarı alıp yatay ölçeklenebilirliği açar.

# scale-2 | Orta
## Soru
SLI, SLO ve SLA'yı hangisi doğru tanımlar?
## A
SLI sözleşmedeki cezadır, SLO ham metriktir, SLA iç hedeftir
🤖 Tanımlar ters çevrilmiş; hiçbiri yerinde değil.
## B
SLA, dayandığı iç SLO'dan daha sıkı bir hedef koyar
🤖 Genelde tersi: SLA, iç SLO'dan daha gevşektir (tampon bırakılır).
## C
SLI = ölçülen gösterge, SLO = o gösterge üzerindeki iç hedef, SLA = dış sözleşme (cezalı)
🤖 Doğru: gösterge → iç hedef → dış sözleşme katmanlaması.
## D
Üçü de aynı şeydir; yalnızca farklı ekiplerin verdiği adlar değişir
🤖 Üçü ayrı katmandır; eşanlamlı değildir.
## Doğru
C
## Açıklama
Üç kavram bir katmanlamadır: **SLI** (Service Level Indicator) fiilen **ölçülen** göstergedir (ör. başarılı istek oranı, p99 gecikme). **SLO** (Objective) o gösterge üzerindeki **iç hedeftir** (ör. %99.9). **SLA** (Agreement) müşteriyle yapılan, ihlalinde **cezası** olan dış sözleşmedir ve pratikte SLO'dan daha **gevşek** tutulur ki iç hedef ihlali müşteri cezasına dönüşmeden erken uyarı olsun. Tanımları ters çevirmek (A), SLA'yı SLO'dan sıkı sanmak (B) ve üçünü eşitlemek (D) klasik yanılgılardır.

# scale-3 | İleri
## Soru
"Üç dokuz" (99.9%) erişilebilirlik yılda kabaca ne kadar kesinti (downtime) bırakır?
## A
Yılda ~8.76 saat
🤖 Doğru: 8760 saatin %0.1'i ≈ 8.76 saat.
## B
Yılda ~52 dakika
🤖 O dört dokuzdur (99.99%).
## C
Yılda ~3.65 gün
🤖 O iki dokuzdur (99%).
## D
Yılda ~5 dakika
🤖 O ~beş dokuzdur (99.999% ≈ 5.26 dk).
## Doğru
A
## Açıklama
Bir yıl ≈ 8760 saattir. **%99.9** ("üç dokuz") kesinti payı %0.1'dir → 8760 × 0.001 ≈ **8.76 saat/yıl**. Referans için: %99 (iki dokuz) ≈ 3.65 gün; %99.99 (dört dokuz) ≈ 52.6 dakika; %99.999 (beş dokuz) ≈ 5.26 dakika. Her ek "dokuz" izin verilen kesintiyi ~10 kat azaltır ve maliyeti hızla artırır — bu yüzden hedef dokuz sayısı bir mühendislik/maliyet takasıdır, "ne kadar çok o kadar iyi" değildir.

# scale-4 | İleri
## Soru
Tek bir erişilebilirlik bölgesi (AZ) arızasında, durumsal (stateful) bir DB için sıfıra yakın kesintiyle hayatta kalmak istiyorsun. En uygun desen hangisidir?
## A
Sık yedeği (backup) alınan tek bir büyük instance
🤖 Geri yükleme (restore) zaman alır; sıfıra yakın kesinti değil, ayrıca SPOF.
## B
Elle (manuel) failover ile yönetilen active-passive kurulum
🤖 Arızayı atlatır ama elle geçiş = dakikalarca kesinti + insan bağımlılığı.
## C
Arıza anında ayağa kaldırılan soğuk yedek (cold standby)
🤖 Boot + ısınma gecikmesi; sıfıra yakın değil.
## D
Çoklu-AZ replika (hot standby) ve sağlık kontrollü otomatik terfi
🤖 Doğru: sağlık kontrolü arızayı görüp yedeği otomatik terfi eder; kesinti saniyeler.
## Doğru
D
## Açıklama
"Sıfıra yakın kesinti" ifadesi **otomatik** failover'ı zorunlu kılar. Sağlık kontrollü bir mimaride primary'nin senkron/yarı-senkron replikası (hot standby) başka bir AZ'de hazır bekler; primary düşünce sistem bunu tespit edip replikayı **otomatik terfi** eder — kesinti saniyeler mertebesinde kalır. Yedekli tek instance (A) restore süresi + SPOF demektir; elle failover (B) dakikalar ve insan gerektirir; soğuk yedek (C) boot/ısınma gecikmesi taşır. Yüksek erişilebilirlik = redundancy + **otomatik**, sağlık-kontrollü terfi.

# scale-5 | İleri
## Soru
Aşırı yük (overload) altında "graceful degradation" (zarif bozulma) ne demektir?
## A
Temel olmayan özellikleri kapatıp fazla yükü atarak çekirdek işlevi ayakta tutmak
🤖 Doğru: kısmi hizmet, tam çöküşten iyidir — çekirdek korunur.
## B
Veritabanını korumak için gelen tüm isteklere anında 500 dönmek
🤖 Bu zarif bozulma değil, sert (hard) arızadır.
## C
Hiçbir özellik bozulmasın diye otomatik ölçeklemeyi sınırsız açık bırakmak
🤖 Bu bozulma değildir; ayrıca gerçekçi de değildir.
## D
Başarısız olan her isteği başarılı olana dek agresifçe yeniden denemek
🤖 Retry fırtınası yükü artırır (zincirleme arıza / cascading failure).
## Doğru
A
## Açıklama
**Graceful degradation**, sistem baskı altındayken **kısmi ama çalışan** bir hizmet sunmasıdır: temel olmayan özellikler (öneriler, kişiselleştirme, zengin içerik) kapatılır veya bayat önbellek servis edilir ki **çekirdek işlev** ayakta kalsın. Tüm isteklere 500 dönmek (B) sert arızadır; sonsuz ölçekleme (C) ne bozulmadır ne gerçekçidir; agresif retry (D) yükü büyütüp **retry fırtınası** ve zincirleme arıza üretir. İlke: tamamen çökmektense yeteneği düşürerek ayakta kal (load shedding, fallback).

# scale-6 | İleri
## Soru
%99.9 erişilebilirlik SLO'suyla "hata bütçesi" (error budget) kavramı ne der?
## A
Hedef %100'dür; yaşanan her hata doğrudan bir SLO ihlali sayılır
🤖 SLO %100 değildir; bütçenin var olma sebebi tam da budur.
## B
Hata bütçesi, SLA ihlalinde müşteriye ödenen ceza tutarıdır
🤖 Bu SLA cezasıyla karıştırmadır; bütçe kavramı bu değildir.
## C
İzin verilen %0.1'lik pay, riske harcanabilen bir bütçedir; tükenince riskli değişiklik durur
🤖 Doğru: bütçe bir mühendislik kaynağıdır; varken risk alınır, bitince frozen.
## D
Hata bütçesi yaşanan her kesintiden sonra biraz daha büyür
🤖 Kesinti bütçeyi TÜKETİR, büyütmez.
## Doğru
C
## Açıklama
**Hata bütçesi**, `1 − SLO`'dur: %99.9 hedef, %0.1 "izin verilen arıza" demektir ve bu pay bir **harcanabilir bütçe** gibi görülür. Bütçe varken ekip risk alabilir (hızlı sürüm, deneme); bütçe **tükenince** riskli değişiklikler dondurulur ve güvenilirliğe odaklanılır. Bu, "her hata felakettir" (A) baskısını kaldırıp hız ile güvenilirlik arasında **niceliksel** bir karar mekanizması kurar. Bütçe SLA cezası parası (B) değildir ve kesintiler onu tüketir, büyütmez (D).
