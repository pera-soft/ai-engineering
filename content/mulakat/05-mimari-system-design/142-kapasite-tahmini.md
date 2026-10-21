---
slug: kapasite-tahmini
baslik: "Kapasite Tahmini: QPS, Depolama, Bant Genişliği ve Büyüklük Mertebesi"
kademe: İleri
sira: 142
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir tasarımın en başında cevaplanması gereken soru şudur: "bu sistem tek bir sunucuyla döner mi, yoksa yüzlerce makine mi gerekir?" Bu sorunun cevabı mimarinin tamamını değiştirir. Ama cevabı bulmak için sistemi yazmaya gerek yoktur; kâğıt kalemle iki dakikada yapılan kaba bir hesap yeter. Buna **kapasite tahmini** (back-of-the-envelope estimation) denir.

**Kapasite**, bir sistemin birim zamanda taşıyabildiği iş miktarıdır; buna **iş hacmi (throughput)** de denir. Tahminin amacı ondalık kesinlik değil, **doğru büyüklük mertebesini** (order of magnitude) bulmaktır. Mimariyi belirleyen soru "günde milyonlar mı milyarlar mı?" sorusudur; "42 mi 47 mi?" değil. Bir mertebe (10 kat) hata bütün kararı yanlış yöne çevirir; %20 hata ise hiçbir şeyi değiştirmez. Bu yüzden iyi bir aday yuvarlak sayılarla, açık varsayımlarla ve hızlı çalışır.

### Temel kavramlar

- **QPS (queries per second — saniyedeki istek sayısı):** sistemin anlık yük ölçüsüdür.
- **Gövde / veri yükü (payload):** bir istek veya yanıtın taşıdığı veri boyutu.
- **Okuma/yazma oranı (read/write ratio):** okuma isteklerinin yazma isteklerine oranı; hangi yolun optimize edileceğini söyler.
- **Tepe (peak) ve ortalama:** ortalama, günün tamamına yayılmış hız; tepe, en yoğun andaki hızdır.
- **Saklama süresi (retention):** verinin kaç gün/yıl tutulacağı. Depolama hesabının çarpanıdır.
- **Kopyalama çarpanı (replication factor):** aynı verinin dayanıklılık için kaç kopya tutulduğu (tipik olarak 3).
- **Yuvarlama tablosu:** 1 gün = 86.400 s ≈ **10^5 s**; 1 yıl ≈ 365 gün; kaba hesapta 1 KB ≈ 10^3, 1 MB ≈ 10^6, 1 GB ≈ 10^9, 1 TB ≈ 10^12 bayt; 1 bayt = 8 bit.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kapasite hesap zinciri ve peak vs average">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hesap zinciri: günlük hacimden kapasiteye</text>
  <rect x="30" y="44" width="130" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Günlük hacim</text>
  <text x="95" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">÷ 86.400</text>
  <rect x="200" y="44" width="130" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="265" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Ortalama QPS</text>
  <text x="265" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">× 2-3 (tepe)</text>
  <rect x="370" y="44" width="130" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="435" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Tepe QPS</text>
  <text x="435" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kapasite temeli</text>
  <text x="182" y="72" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <text x="352" y="72" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="200" y="110" width="300" height="46" rx="9" fill="#FFF6E5" stroke="#B8860B" stroke-width="1.5"/>
  <text x="350" y="132" text-anchor="middle" font-size="11" font-weight="700" fill="#8A6400" font-family="system-ui">QPS × payload → bant genişliği</text>
  <text x="350" y="148" text-anchor="middle" font-size="10" fill="#8A6400" font-family="system-ui">nesne × boyut × süre → depolama</text>
  <text x="435" y="104" text-anchor="middle" font-size="14" fill="#5D7789" font-family="system-ui">↓</text>
</svg>
```

### Nasıl çalışır?

Hesaplar birbirini besleyen dört adımdır.

**1. Ortalama QPS** = günlük istek sayısı ÷ 86.400. Zihinsel hesapta 86.400 yerine 10^5 kullanılır: günde 8,6 milyar istek ≈ 8,6×10^9 ÷ 10^5 ≈ 10^5, yani ~100.000 QPS.

**2. Tepe QPS** = ortalama × tepe çarpanı. Trafik gün içinde (öğle, akşam) ve mevsimsel olarak dalgalanır. Yaygın kaba kural şudur: tepe ≈ ortalamanın **2-3 katı**. Bir başka pratik yaklaşım **80/20 kuralıdır**: trafiğin yaklaşık %80'i günün yaklaşık %20'sinde gerçekleşir. Bu da yaklaşık 4 katlık bir tepe demektir. Ölçüm varsa tabii gerçek oran kullanılır. Kritik kural şudur: **kapasite ortalamaya değil tepeye göre planlanır.** Üstüne bir de güvenlik payı (headroom) ve otomatik ölçekleme eklenir. Ortalamaya göre boyutlandırılan sistem, tam da en çok ihtiyaç duyulduğu anda çöker.

**3. Depolama** = nesne sayısı × nesne boyutu × saklama süresi. Günde 100 milyon kayıt × 1 KB = 100 GB/gün; 5 yıl ≈ 1.825 gün → ~180 TB. Bu **ham** sayıdır. Üzerine kopyalama çarpanı (ör. ×3), indeksler, üstveri (metadata) ve büyüme eğrisi eklenir. Yani ham sonucu bir **alt sınır** gibi okumak gerekir.

**4. Bant genişliği** = QPS × ortalama gövde boyutu. Örnek: 10.000 QPS × 200 KB ≈ 2 GB/s. Ağ hızları bit cinsinden konuşulduğu için ×8 yaparsın: ~16 Gbps eder. Tek bir sunucunun ağ kartı tipik olarak 10-25 Gbps taşır; yani burada sınıra dayanırsın ve CDN ile kenarda önbellekleme kaçınılmaz olur. Gelen (ingress) ve giden (egress) trafik ayrı hesaplanır. Medya servislerinde giden trafik baskındır.

**Okuma/yazma oranı** ise pusuladır. Okuma ağırlıklı sistemler (URL yönlendirme, akış/feed; sıklıkla 100:1) **önbellek, okuma kopyası ve CDN** ister. Yazma ağırlıklı sistemler (metrik ve log toplama) ise **veriyi parçalara bölmeyi (sharding)**, kuyruk kullanmayı ve LSM tabanlı bir depo ister.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Okuma yazma oranı mimariyi yönlendirir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Okuma/yazma oranı: optimizasyon pusulası</text>
  <rect x="40" y="48" width="250" height="132" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Okuma-ağırlıklı (100:1)</text>
  <text x="165" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ör. URL yönlendirme, feed</text>
  <text x="165" y="122" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">önbellek + okuma replikası</text>
  <text x="165" y="144" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">CDN; yazma yolu sade</text>
  <rect x="350" y="48" width="250" height="132" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Yazma-ağırlıklı</text>
  <text x="475" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ör. metrik / log toplama</text>
  <text x="475" y="122" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">sharding + kuyruk</text>
  <text x="475" y="144" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">LSM tabanlı depo</text>
</svg>
```

### Mülakatta ne sorulur?

Dört hesap doğrudan istenir: QPS, depolama, bant genişliği ve tepe yük. Beklenen davranış, sayıyı bulmak kadar onu **yorumlamaktır**: "~100 bin QPS tek sunucuyla dönmez; yük dengeleme, yatay ölçekleme ve önbellek gerekir." Varsayımlar sesli söylenir ("kayıt başına 1 KB varsayıyorum"). Yuvarlamalar gerekçelendirilir. Sonucun ham mı, yoksa kopyalar dahil mi olduğu ayrıca belirtilir. En sık iki hata şudur: **mertebe kaçırmak** (bir sıfır fazla ya da eksik) ve **tepeyi unutup ortalamaya göre plan yapmak**. Ayrıca "neden yuvarlıyorsun?" sorusunun cevabı da beklenir: hedef kesinlik değil, kararı belirleyen mertebedir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Trafik eğrisi: tepe ortalamanın 2-3 katı, kapasite tepeye göre">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tepe yüke göre boyutlandır</text>
  <line x1="60" y1="170" x2="600" y2="170" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="60" y1="46" x2="60" y2="170" stroke="#5D7789" stroke-width="1.5"/>
  <text x="52" y="58" text-anchor="end" font-size="10.5" fill="#5D7789" font-family="system-ui">QPS</text>
  <text x="596" y="188" text-anchor="end" font-size="10.5" fill="#5D7789" font-family="system-ui">saat →</text>
  <line x1="60" y1="70" x2="600" y2="70" stroke="#25804C" stroke-width="2" stroke-dasharray="6 4"/>
  <text x="452" y="64" text-anchor="start" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">kapasite = tepe (2-3x)</text>
  <line x1="60" y1="132" x2="600" y2="132" stroke="#B26B00" stroke-width="1.5" stroke-dasharray="3 3"/>
  <text x="452" y="126" text-anchor="start" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">ortalama</text>
  <path d="M60,150 C160,150 180,92 240,90 C300,88 320,148 380,150 C440,152 470,76 520,78 C560,80 580,122 600,130" fill="none" stroke="#186399" stroke-width="2.5"/>
  <text x="250" y="82" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">öğle</text>
  <text x="516" y="70" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">akşam tepesi</text>
</svg>
```

### Tek cümlede

Kapasite tahmini; günlük hacimden QPS'e, oradan tepe yüke, depolamaya ve bant genişliğine giden kaba bir hesap zinciridir. Amacı ondalık kesinlik değil, mimariyi belirleyen doğru büyüklük mertebesini bulmak ve sistemi ortalamaya değil tepe yüke göre boyutlandırmaktır.

# kapasite-1 | İleri
## Soru
Bir servis günde yaklaşık 8,6 milyar istek alıyor. Ortalama QPS'i (saniyedeki istek) kabaca ne olur?
## A
~1.000 QPS
🤖 Çok düşük; 8,6 milyarı ~10^5 saniyeye bölünce 10^5 mertebesi çıkar, 10^3 değil.
## B
~10.000 QPS
🤖 Bir mertebe düşük; doğru sonuç ~100.000 QPS civarındadır.
## C
~100.000 QPS
🤖 Doğru: 8,6×10^9 ÷ 86.400 ≈ 10^5, yani ~100 bin QPS.
## D
~1.000.000 QPS
🤖 Bir mertebe yüksek; 8,6 milyar/gün ~100 bin QPS'e denk gelir, milyona değil.
## Doğru
C
## Açıklama
QPS = günlük istek ÷ bir gündeki saniye sayısı. Bir gün = 86.400 s ≈ **10^5 s** (hızlı zihinsel yaklaşım). 8,6×10^9 ÷ 86.400 ≈ **10^5 = ~100.000 QPS**. Bu tür hesaplarda amaç ondalık kesinlik değil **doğru büyüklük mertebesidir**; 86.400'ü 10^5 kabul etmek pratikte yeterlidir ve zihinsel hesabı hızlandırır. Sonuç mimariyi doğrudan etkiler: ~100 bin QPS tek sunucuyla karşılanamaz; yük dengeleme, yatay ölçekleme ve büyük olasılıkla önbellek gerektirir. Üstelik bu yalnız **ortalamadır**; kapasite planı için tepe çarpanıyla (tipik 2-3 kat) büyütülmesi gerekir. A ve B bir-iki mertebe düşük, D bir mertebe yüksektir; mertebe hatası mimari kararı tümüyle yanlış yöne çeker.

# kapasite-2 | İleri
## Soru
Bir sistem günde 100 milyon yeni kayıt üretiyor, her kayıt ortalama 1 KB. Replikasyon ve indeks yükü göz ardı edilirse **5 yıllık** ham depolama kabaca ne olur?
## A
~180 GB
🤖 Yalnızca ~2 günlük veriye denk; 5 yıl için binlerce kat eksiktir.
## B
~1,8 TB
🤖 Yaklaşık 18 günlük veri; 5 yıllık toplama göre çok düşük kalır.
## C
~18 TB
🤖 Yaklaşık 6 aylık veriye denk gelir; 5 yıl bunun ~10 katıdır.
## D
~180 TB
🤖 Doğru: günde ~100 GB × ~1.825 gün ≈ 180 TB mertebesindedir.
## Doğru
D
## Açıklama
Depolama = nesne sayısı × nesne boyutu × saklama süresi. Günlük: 100×10^6 kayıt × 1 KB = **10^8 KB = 100 GB/gün**. 5 yıl ≈ 5 × 365 ≈ **1.825 gün**. 100 GB × 1.825 ≈ **182.500 GB ≈ ~180 TB**. Gerçek sistemde bunun üstüne **replikasyon çarpanı** (ör. ×3), indeksler, metadata ve büyüme eğrisi eklenir; yani gerçek ihtiyaç bu ham sayının katıdır ve tahmin bir **alt sınır** gibi okunmalıdır. A, B ve C sırasıyla günler ve aylar mertebesinde kalıp saklama süresini hesaba katmayan yaygın hatalardır. Depolama tahmini, tek düğümün yeteceğini mi yoksa sharding ve dağıtık depo mu gerektiğini belirlediği için mimariyi doğrudan etkiler.

# kapasite-3 | İleri
## Soru
Bir video-önizleme servisi ~10.000 QPS ile istek başına ortalama 200 KB yanıt döndürüyor. Gereken **giden (egress) bant genişliği** kabaca nedir?
## A
~2 GB/s
🤖 Doğru: 10^4 × 200 KB = 2×10^6 KB/s, yani ~2 GB/s.
## B
~200 MB/s
🤖 Bir mertebe düşük; 10.000 × 200 KB çarpımı 2×10^6 KB/s = ~2 GB/s eder.
## C
~20 MB/s
🤖 İki mertebe düşük; QPS ile payload'ın çarpımı bundan çok daha büyüktür.
## D
~200 GB/s
🤖 İki mertebe yüksek; doğru sonuç ~2 GB/s mertebesindedir.
## Doğru
A
## Açıklama
Bant genişliği = QPS × ortalama yük (payload) boyutu. 10.000 QPS × 200 KB = 2×10^6 KB/s = **2×10^3 MB/s = ~2 GB/s**. Ağ genellikle bit cinsinden konuşulduğundan 2 GB/s ≈ **16 Gbps** eder (×8). Bu ölçekte tek bir sunucunun ağ kartı (tipik 10-25 Gbps) sınıra dayanır; **CDN**, kenar önbellekleme (edge caching) ve içerik dağıtımı devreye girer — yani bant genişliği tahmini doğrudan bir mimari karar doğurur. Gelen (ingress) ve giden (egress) ayrı hesaplanır; medya servislerinde egress baskındır. B bir, C iki mertebe düşük, D iki mertebe yüksektir; hepsi mertebe hatasıdır ve yanlış altyapı kararına götürür.

# kapasite-4 | İleri
## Soru
Bir URL kısaltma servisinde okuma/yazma oranının yaklaşık **100:1** (yönlendirme çok, kısaltma az) olduğunu tahmin ettin. Bu oran mimariyi nasıl yönlendirir?
## A
Yazma yolunu ağır biçimde optimize et; bu oranda okuma yolu önemsizdir
🤖 Ters öncelik; baskın olan okumadır, asıl optimizasyon okuma yolundadır.
## B
Okuma yolunu önbellek ve okuma replikalarıyla optimize et; yazma yolu sade kalabilir
🤖 Doğru: okuma-ağırlıklı sistemde yatırım okuma yoluna, önbelleğe ve replikaya yapılır.
## C
Oran mimariyi etkilemez; okuma ve yazma her zaman birlikte simetrik ölçeklenir
🤖 Yanlış; okuma/yazma dengesi depo, önbellek ve replikasyon kararlarını doğrudan değiştirir.
## D
Güçlü tutarlılık için bütün okumaları doğrudan ana veritabanına (primary) yönlendir
🤖 Okuma ağırlıkta primary'yi her okumayla dövmek darboğaz yaratır; replika/önbellek gerekir.
## Doğru
B
## Açıklama
**Okuma/yazma oranı** mimariyi büyük ölçüde belirler. 100:1 okuma-ağırlıklı bir sistemde (bir URL bir kez yazılır, milyonlarca kez yönlendirilir) asıl yük **okuma yolundadır**; bu yol **önbellek** (sık erişilen URL'ler için çok yüksek isabet oranı), **okuma replikaları** ve CDN ile optimize edilir, yazma yolu ise nispeten sade kalabilir. Yazmayı ağır optimize etmek (A) yanlış yere yatırımdır; oranın mimariyi etkilemediğini söylemek (C) temelden yanlıştır; her okumayı primary'ye yöneltmek (D) primary'yi darboğaza sokar ve ölçeklenmeyi tıkar. Tersi durumda — yazma ağırlıklı metrik/log toplamada — sharding, kuyruk ve LSM tabanlı depo öne çıkar. Yani oran, doğru optimizasyon ekseninin pusulasıdır.

# kapasite-5 | İleri
## Soru
Kapasite tahmininde `86.400` yerine `10^5`, `1 GB` için `10^9 bayt` gibi yuvarlak sayılar kullanmak neden **kabul edilebilir hatta tercih edilir**?
## A
Çünkü kesin sayılar yanıltıcıdır; yalnızca yuvarlak sayılar doğru sonucu verir
🤖 Kesin sayılar yanlış değil, yalnızca bu bağlamda gereksiz; iddia abartılıdır.
## B
Çünkü mülakatçı aritmetik beceriyi değil, yalnızca ezberlenmiş sayıları ölçer
🤖 Aksine muhakeme ölçülür; yuvarlama muhakemeyi hızlandırmak içindir, ezber için değil.
## C
Çünkü yuvarlama sonucu her zaman iki katına çıkarır ve böylece güvenlik payı sağlar
🤖 Yuvarlama sistematik biçimde ikiye katlamaz; amaç hız ve mertebe doğruluğudur.
## D
Çünkü hedef ondalık kesinlik değil doğru büyüklük mertebesidir; yuvarlama hızlandırır
🤖 Doğru: mimari kararı mertebe belirler; yuvarlama sonucu doğru mertebe içinde tutar.
## Doğru
D
## Açıklama
Kapasite tahmininin amacı **ondalık kesinlik değil, doğru büyüklük mertebesidir** (order of magnitude): mimari kararı "milyon mu milyar mı?" belirler, "43 mü 47 mi?" değil. `86.400 ≈ 10^5`, `2^10 ≈ 10^3`, `1 GB ≈ 10^9 bayt` gibi yuvarlamalar zihinsel aritmetiği hızlandırır ve sonucu doğru mertebe içinde tutar — mülakatçı hızlı ve gerekçeli **muhakeme** görmek ister, hatasız uzun bölme değil. Yuvarlamanın kesin sayıları "yanlış" kıldığı (A), yalnızca ezberin ölçüldüğü (B) veya sonucun hep ikiye katlandığı (C) iddiaları yanlıştır. Doğru pratik: yuvarlak sayılarla hızlı ilerle, varsayımları açıkça söyle, gerekirse sonucu sonradan hassaslaştır.

# kapasite-6 | İleri
## Soru
Bir sistem ortalama 20.000 QPS alıyor ama trafik akşam saatlerinde yoğunlaşıyor. Kapasiteyi hangi sayıya göre planlamalısın?
## A
Tepe (peak) yüke göre; ortalamanın 2-3 katı alınıp ona göre boyutlandırılır
🤖 Doğru: sistem ortalamada değil tepe anında çöker; kapasite tepeye göre planlanır.
## B
Ortalama QPS'e göre; 20.000 sayısı planlama için yeterli bir temel oluşturur
🤖 Ortalamaya göre boyutlanan sistem tepe saatte kapasiteyi aşar ve çöker.
## C
En düşük gece trafiğine göre; maliyeti en aza indirmek her şeyden önceliklidir
🤖 Vadiye göre boyutlamak tepe anında felaketle sonuçlanır; maliyet çöküşü haklı çıkarmaz.
## D
Yıllık toplam istek sayısına göre; en kapsayıcı ve en güvenli sayı odur
🤖 Yıllık toplam anlık kapasiteyi söylemez; eşzamanlı yük tepe QPS ile ölçülür.
## Doğru
A
## Açıklama
Kapasite **ortalamaya değil tepe (peak) yüke** göre planlanır, çünkü sistem tam da tepe anında yıkılma riski taşır. Trafik gün içinde (akşam yoğunluğu) ve mevsimsel/etkinlik bazlı dalgalanır; yaygın kaba kural **tepe ≈ ortalamanın 2-3 katıdır**, bir başka pratik yaklaşım da 80/20 kuralıdır (trafiğin ~%80'i günün ~%20'sinde, yani ~4 kat). Ölçüm varsa gerçek oran kullanılır. 20.000 ortalama QPS için ~40.000-60.000 tepe QPS'e göre boyutlandırmak gerekir; ayrıca ani sıçramalara karşı **otomatik ölçekleme** ve güvenlik payı (headroom) eklenir. Ortalamaya (B) veya en düşük trafiğe (C) göre planlamak tepe saatte çöküş demektir; yıllık toplam (D) anlık eşzamanlı yükü hiç ölçmez. Maliyet optimizasyonu tepeyi karşılamanın *içinde*, elastik ölçeklemeyle yapılır — tepeyi görmezden gelerek değil.
