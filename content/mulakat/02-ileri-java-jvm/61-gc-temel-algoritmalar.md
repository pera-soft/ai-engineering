---
slug: gc-temel-algoritmalar
baslik: "Çöp Toplama (GC) Temel Algoritmalar"
kademe: İleri
sira: 61
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir odada çalışıyorsunuz; kâğıtlar, bardaklar, kutular birikiyor. Bazı dillerde her nesneyi çöpe atmak sizin işinizdir: unutursanız oda dolar (bellek sızıntısı), erken atarsanız hâlâ kullandığınız şeyi kaybedersiniz. Java'da bu işi sizin yerinize bir görevli yapar — odayı dolaşır, **artık hiç kimsenin ulaşamadığı** eşyaları toplar, kalanları düzgünce yan yana dizer. Bu görevliye **çöp toplayıcı (garbage collector, GC)** denir. GC'yi anlamak, uygulamanın neden ara ara duraksadığını ve bu duraksamanın nasıl kısaltılacağını anlamaktır.

### Temel kavramlar

- **Çöp toplama (GC)**: **erişilemez (unreachable)** nesnelerin belleğini otomatik geri kazanmak. Java'da bellek `free()` benzeri çağrılarla elle boşaltılmaz.
- **GC kökleri (GC roots)**: erişilebilirliğin başladığı noktalar — yerel değişkenler, statik alanlar, aktif iplik yığıtları vb. Köklerden zincirle ulaşılabilen nesne **canlıdır**.
- **Mark (işaretle)**: köklerden gezerek canlı nesneleri işaretlemek.
- **Sweep (süpür)**: işaretlenmemiş, yani erişilemez nesnelerin belleğini geri almak.
- **Compact (sıkıştır)**: hayatta kalanları belleğin bir ucuna yan yana taşımak; böylece boş alan tek büyük blok olur, **parçalanma (fragmentation)** giderilir ve sonraki ayırmalar hızlanır. Her toplayıcı her turda sıkıştırma yapmaz.
- **Nesil hipotezi (generational hypothesis)**: ampirik bir gözlem — *çoğu nesne genç ölür* ("infant mortality"). Bu yüzden yığın (heap) **genç nesil (young)** ve **yaşlı nesil (old/tenured)** olarak ayrılır; genç nesil sık ve ucuz, yaşlı nesil seyrek toplanır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mark-sweep-compact adımları ve genç/yaşlı nesil ayrımı; G1 heap'i eşit bölgelere ayırır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Mark → Sweep → Compact ve nesil hipotezi</text>
  <rect x="30" y="42" width="176" height="60" rx="10" fill="#EAF2FB" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="118" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#2C6BB3" font-family="system-ui">1. Mark</text>
  <text x="118" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">köklerden erişilebilenler</text>
  <rect x="232" y="42" width="176" height="60" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B4322B" font-family="system-ui">2. Sweep</text>
  <text x="320" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">erişilemezleri boşalt</text>
  <rect x="434" y="42" width="176" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="522" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">3. Compact</text>
  <text x="522" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yaşayanları sıkıştır</text>
  <rect x="30" y="122" width="360" height="72" rx="10" fill="#F3F6F9" stroke="#5D7789" stroke-width="1.3"/>
  <text x="210" y="144" text-anchor="middle" font-size="12" font-weight="700" fill="#3A4A57" font-family="system-ui">Nesil hipotezi: çoğu nesne genç ölür</text>
  <rect x="46" y="154" width="150" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="121" y="174" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Genç (sık · ucuz)</text>
  <rect x="224" y="154" width="150" height="30" rx="6" fill="#FBF1E6" stroke="#B5771F" stroke-width="1.2"/>
  <text x="299" y="174" text-anchor="middle" font-size="10.5" fill="#B5771F" font-family="system-ui">Yaşlı (seyrek)</text>
  <rect x="404" y="122" width="206" height="72" rx="10" fill="#F3EDF9" stroke="#7A4EA8" stroke-width="1.3"/>
  <text x="507" y="144" text-anchor="middle" font-size="12" font-weight="700" fill="#7A4EA8" font-family="system-ui">G1: eşit bölgeler (regions)</text>
  <rect x="420" y="154" width="26" height="26" rx="4" fill="#D9C7EC"/><rect x="450" y="154" width="26" height="26" rx="4" fill="#D9C7EC"/><rect x="480" y="154" width="26" height="26" rx="4" fill="#D9C7EC"/><rect x="510" y="154" width="26" height="26" rx="4" fill="#D9C7EC"/><rect x="540" y="154" width="26" height="26" rx="4" fill="#D9C7EC"/><rect x="570" y="154" width="26" height="26" rx="4" fill="#D9C7EC"/>
  <text x="320" y="224" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Çoğu toplayıcı toplarken kısa bir "dünyayı durdur" (STW) uygular.</text>
</svg>
```

### Nasıl çalışır?

Genç nesil kendi içinde bir **Eden** ve iki **survivor (S0/S1)** alanına bölünür. Yeni nesneler Eden'de doğar ve çoğu daha ilk toplamayı göremeden ölür. Bir **minor GC**'de sağ kalanlar survivor alanları arasında kopyalanır (S0 ↔ S1) ve her turda **yaşları bir artar**. Belirli bir yaş eşiğini (`-XX:MaxTenuringThreshold`) geçen nesneler yaşlı nesle **terfi eder (promotion)**.

Toplama türlerinin adları buradan gelir: **minor GC** genç nesli, **major GC** yaşlı nesli toplar; **full GC** ise genç + yaşlı nesli birlikte kapsayan bütünsel toplamadır. Major ve full toplamalar minor'a kıyasla çok daha pahalı ve seyrektir — toplayıcı, işinin çoğunu küçük ve ucuz genç toplamalarla halleder.

Toplamanın bazı aşamaları (kökleri taramak, nesneleri taşımak) nesne grafiğinin **tutarlı** olmasını gerektirir; hareket hâlindeki bir kalabalığı saymaya benzer. Bu yüzden JVM uygulama (mutator) ipliklerini bir **güvenli noktada (safepoint)** kısa süre duraklatır — buna **dünyayı durdur (stop-the-world, STW)** denir. Bu sırada yalnız GC iplikleri çalışır. STW bir çökme (crash) ya da kalıcı donma değildir, ağ bağlantılarının kesilmesiyle de ilgisi yoktur: iş bitince iplikler kaldığı yerden devam eder. Duraklamanın uzaması, gecikmeye duyarlı sistemlerde asıl derttir.

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kuşaklı yığında nesne yaşlanması Eden survivor old">
  <defs>
    <marker id="gcalg-f" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0,0 L9,4.5 L0,9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Nesne yaşlanması: Eden → Survivor → Old</text>
  <rect x="24" y="70" width="130" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="89" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Eden</text>
  <text x="89" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yeni nesneler</text>
  <text x="89" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">çoğu burada ölür</text>
  <line x1="156" y1="105" x2="212" y2="105" stroke="#5D7789" stroke-width="1.8" marker-end="url(#gcalg-f)"/>
  <text x="184" y="97" text-anchor="middle" font-size="9.5" font-weight="600" fill="#25804C" font-family="system-ui">minor GC</text>
  <rect x="216" y="60" width="150" height="90" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="291" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Survivor</text>
  <rect x="228" y="90" width="58" height="46" rx="6" fill="#ffffff" stroke="#186399" stroke-width="1.2"/>
  <text x="257" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">S0</text>
  <text x="257" y="127" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">↔ kopya</text>
  <rect x="296" y="90" width="58" height="46" rx="6" fill="#ffffff" stroke="#186399" stroke-width="1.2"/>
  <text x="325" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">S1</text>
  <text x="325" y="127" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">her tur</text>
  <line x1="368" y1="105" x2="424" y2="105" stroke="#5D7789" stroke-width="1.8" marker-end="url(#gcalg-f)"/>
  <text x="396" y="96" text-anchor="middle" font-size="9" font-weight="600" fill="#B26B00" font-family="system-ui">yaş eşiği</text>
  <text x="396" y="122" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">terfi (promotion)</text>
  <rect x="428" y="70" width="150" height="70" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="503" y="98" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Old (tenured)</text>
  <text x="503" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">uzun yaşayanlar</text>
  <text x="503" y="132" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">major/full GC (seyrek)</text>
  <text x="320" y="176" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Genç toplama SIK ve UCUZ; sağ kalan her turda S0↔S1 kopyalanır.</text>
  <text x="320" y="197" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">belli tur (yaş) hayatta kalan nesne Old'a terfi eder</text>
</svg>
```

Toplayıcı seçimi bir denge (trade-off) işidir: verim mi, düşük gecikme mi? **Parallel GC** tüm çekirdekleri toplamaya vererek **verimi (throughput)** en üst düzeye çıkarır, ama duraklamaları uzayabilir — batch/analitik gibi gecikmeye duyarsız yükler için iyidir. **G1 (Garbage-First)** yığını çok sayıda **eşit boyutlu bölgeye (region)** böler; her bölge duruma göre eden, survivor veya old rolü üstlenir. Adının ima ettiği gibi **en çok çöp içeren bölgeleri önce** toplar. Bir yandan da kullanıcının verdiği **duraklama hedefini** (`-XX:MaxGCPauseMillis`) tutturmaya çalışır. Genç toplamaların yanı sıra **karma (mixed) toplamalarla** yaşlı nesil bölgelerini de artımlı toplar; yani ne tek parça bir yığında hep full GC yapar ne de yaşlı nesli büyümeye bırakır. **JDK 9'dan beri varsayılandır.** Gecikmenin kritik olduğu yerlerde ise **ZGC** kullanılır: duraklamaları çoğunlukla **milisaltı** tutar ve **nesilli (generational) ZGC JDK 23'ten beri varsayılan moddur** (nesilsiz mod kullanımdan kaldırılmaktadır).

Ayırmanın kendisini azaltan bir yol daha vardır: **kaçış analizi (escape analysis)**. JIT derleyicisi bazen bir nesnenin **referansının** oluşturulduğu metodun (ya da iş parçacığının) dışına "kaçmadığını" kanıtlar. Bu durumda nesneyi heap yerine **yığıta (stack) ayırabilir**. Ya da nesneyi tümden ortadan kaldırıp alanlarını doğrudan yerel değişkenlere/yazmaçlara açar; buna **skaler değişim (scalar replacement)** denir. Sonuç: daha az heap ayırması, daha az çöp, daha az GC duraklaması; ayrıca kilit elemesi (lock elision) gibi kazanımlar. Nesneleri diske taşımaz, GC'yi devre dışı bırakmaz, hata da yakalamaz — bir **ayırma/GC optimizasyonudur**.

```svg
<svg viewBox="0 0 640 195" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GC toplayıcı seçimi verim ve gecikme dengesi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Toplayıcı seçimi: verim ↔ düşük gecikme</text>
  <line x1="60" y1="66" x2="580" y2="66" stroke="#5D7789" stroke-width="2"/>
  <text x="60" y="52" text-anchor="start" font-size="10.5" font-weight="600" fill="#B26B00" font-family="system-ui">yüksek verim (throughput)</text>
  <text x="580" y="52" text-anchor="end" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">düşük gecikme (latency)</text>
  <circle cx="90" cy="66" r="6" fill="#B26B00"/>
  <circle cx="320" cy="66" r="6" fill="#186399"/>
  <circle cx="550" cy="66" r="6" fill="#25804C"/>
  <rect x="46" y="82" width="176" height="90" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="134" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Parallel GC</text>
  <text x="134" y="128" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">en yüksek verim</text>
  <text x="134" y="145" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">uzun duraklamalar</text>
  <text x="134" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">batch/analitik iş</text>
  <rect x="232" y="82" width="176" height="90" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">G1</text>
  <text x="320" y="128" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dengeli, öngörülebilir</text>
  <text x="320" y="145" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">bölge tabanlı</text>
  <text x="320" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">JDK 9+ varsayılan</text>
  <rect x="418" y="82" width="176" height="90" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="506" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ZGC</text>
  <text x="506" y="128" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">milisaltı duraklama</text>
  <text x="506" y="145" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">nesilli ZGC</text>
  <text x="506" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">JDK 23+ varsayılan mod</text>
</svg>
```

### Mülakatta ne sorulur?

- **Mark-sweep-compact.** İşaretle (köklerden erişilebilen canlıları), süpür (işaretlenmeyenlerin belleğini geri al), sıkıştır (canlıları yan yana taşı, parçalanmayı gider). Diskle, CPU önbelleğiyle ya da "önce hepsini sil" mantığıyla ilgisi yoktur.
- **Nesil hipotezi.** *Çoğu nesne genç ölür*; bu yüzden sık ve ucuz toplanan **genç** nesildir. "Eski nesneler daha hızlı ölür", "nesneler hiç ölmez" ya da "canlı sayısı sabittir" ifadeleri hipotezi ters çevirir.
- **Eden / S0 / S1 ve terfi.** Yeni nesne Eden'de doğar; minor GC'de sağ kalan S0 ↔ S1 arasında kopyalanır ve yaşı artar; yaş eşiğini geçen Old'a **terfi eder (promotion)**.
- **Minor / major / full.** Sırasıyla genç nesil, yaşlı nesil, ikisi birlikte. Minor sık ve ucuz; major/full seyrek ve pahalı.
- **Safepoint ve STW.** Uygulama (mutator) ipliklerinin güvenli bir noktada **kısa ve planlı** duraklatılması; çökme değil, kalıcı donma değil, ağ kesintisi değil.
- **G1'in imzası.** Bölge tabanlı (region) ve duraklama-hedefli tasarım; karma (mixed) toplamalarla yaşlı nesli de artımlı toplar; JDK 9'dan beri varsayılan.
- **Toplayıcı seçimi.** Verim öncelikse **Parallel GC**; dengeli ve öngörülebilir duraklama için **G1**; çok düşük gecikme gerekiyorsa **ZGC** (nesilli ZGC JDK 23'ten beri varsayılan mod). "Her yük için tek doğru toplayıcı var" ya da "seçim performansı etkilemez" yanlıştır; ZGC ile Parallel'in rolleri de ters çevrilmemelidir.
- **Kaçış analizi.** Kaçmayan nesne için yığıta ayırma veya skaler değişim.

### Tek cümlede

Çöp toplama, köklerden erişilemeyen nesneleri işaretle-süpür-sıkıştır döngüsüyle geri kazanır; "çoğu nesne genç ölür" hipotezi üzerine kurulu nesilli yığın sayesinde işin çoğunu ucuz genç toplamalarla yapar ve toplayıcı seçimi verim ile düşük gecikme arasındaki dengeye göre değişir.

# gcalg-1 | İleri
## Soru
Klasik "mark-sweep-compact" çöp toplama adımlarının işlevi nedir?
## A
Mark: nesneleri diske yazar; Sweep: disktekileri siler; Compact: kalanları yeniden belleğe yükler
🤖 Disk ile ilgisi yoktur; adımlar bellek içi erişilebilirlik ve düzenlemeyle ilgilidir.
## B
Mark: tüm nesneleri siler; Sweep: yerlerine yenilerini oluşturur; Compact: onları sırayla dizer
🤖 Mark silmez, işaretler; adımların tanımları karışmış.
## C
Mark: köklerden erişilebilen canlı nesneleri işaretler; Sweep: işaretlenmeyenlerin belleğini boşaltır; Compact: canlıları yan yana taşır
🤖 Doğru: canlıları işaretle, ölüleri süpür, kalanları sıkıştır.
## D
Üç adım da yalnız CPU önbelleğini (cache) temizler; heap'e hiç dokunulmaz
🤖 Önbellekle ilgisi yok; heap'teki canlı/ölü nesne yönetimidir.
## Doğru
C
## Açıklama
**Mark (işaretle)**: GC köklerinden (yerel değişkenler, statik alanlar, aktif iplik yığıtları vb.) başlayarak erişilebilen tüm **canlı** nesneler gezilir ve işaretlenir. **Sweep (süpür)**: işaretlenmeyen, yani artık erişilemez nesnelerin kapladığı bellek geri alınır. **Compact (sıkıştır)**: hayatta kalan nesneler belleğin bir ucuna yan yana taşınır; böylece boş alan tek büyük blok hâline gelir ve **parçalanma (fragmentation)** giderilir, sonraki ayırmalar hızlanır. Sıkıştırma her toplayıcıda her turda yapılmaz, ama kavramsal üçlü budur. A (disk), B (mark siler) ve D (önbellek) adımların anlamını yanlış tanımlar.

# gcalg-2 | İleri
## Soru
"Nesil hipotezi (generational hypothesis)" nedir ve GC tasarımını nasıl etkiler?
## A
Nesneler pratikte hiç ölmez; bu yüzden heap'i nesillere bölmek de GC de gereksizdir
🤖 Tam tersine çoğu nesne çabuk ölür; GC bu yüzden nesilli tasarlanır.
## B
Eski nesneler yeni nesnelerden daha hızlı ölür; bu yüzden sık toplanan yaşlı nesildir
🤖 Tersine, genç nesneler daha çabuk ölür; sık toplanan genç nesildir.
## C
Canlı nesne sayısı zamanla sabit kalır; bu yüzden nesil ayrımının bir faydası yoktur
🤖 Sayı sabit değildir; hipotez nesnelerin ölüm oranıyla ilgilidir.
## D
Çoğu nesne genç ölür; bu yüzden heap genç/yaşlı diye ayrılır, genç nesil sık ve ucuz toplanır
🤖 Doğru: "genç ölüm" gözlemi nesilli heap ayrımını motive eder.
## Doğru
D
## Açıklama
**Nesil hipotezi**, ampirik bir gözlemdir: *çoğu nesne oluşturulduktan kısa süre sonra erişilemez hâle gelir* ("infant mortality" — çoğu nesne genç ölür). Bunu istismar etmek için heap **genç nesil (young: eden + iki survivor)** ve **yaşlı nesil (old/tenured)** olarak ayrılır. Genç nesil küçük ve çabuk dolduğundan **sık ama ucuz** (minor GC) toplanır; genç toplamalardan sağ çıkan (belli sayıda tur yaşayan) nesneler yaşlı nesle **terfi eder (promotion)** ve yaşlı nesil çok daha **seyrek** toplanır (major/full GC). Böylece toplayıcı işinin çoğunu küçük ve ucuz genç toplamalarla halleder. A, B ve C hipotezi ters ya da yanlış ifade eder.

# gcalg-3 | İleri
## Soru
G1 (Garbage-First) toplayıcısının belirleyici tasarım özelliği nedir?
## A
Yığını eşit boyutlu bölgelere (regions) ayırır; en çok çöp içeren bölgeleri önceleyip duraklama hedefini tutturmaya çalışır
🤖 Doğru: bölge tabanlı, duraklama-hedefli tasarım G1'in imzasıdır.
## B
Tek ve bölünmemiş bir heap üzerinde her toplamada tam (full) toplama yapar
🤖 G1 tam da bunun tersini yapar: bölgeler üzerinde artımlı çalışır.
## C
Çöp toplamayı tamamen kaldırır; bellek `free()` benzeri çağrılarla elle boşaltılır
🤖 GC'yi kaldırmaz; Java'da bellek elle boşaltılmaz.
## D
Yalnız genç nesli toplar; yaşlı nesil hiç toplanmaz, süresiz olarak büyümeye bırakılır
🤖 G1 hem genç hem (karma/mixed toplamalarla) yaşlı nesli toplar.
## Doğru
A
## Açıklama
**G1**, heap'i çok sayıda **eşit boyutlu bölgeye (region)** böler; her bölge duruma göre eden, survivor ya da old rolü üstlenebilir. G1, adının ("Garbage-First") ima ettiği gibi **en çok çöp içeren bölgeleri önce** toplayarak en yüksek getiriyi hedefler ve kullanıcının belirlediği **duraklama süresi hedefini** (`-XX:MaxGCPauseMillis`) tutturmaya çalışır. Genç toplamaların yanı sıra **karma (mixed) toplamalarla** yaşlı nesil bölgelerini de artımlı toplar. JDK 9'dan beri **varsayılan** toplayıcıdır. B (tek heap, hep full), C (GC yok) ve D (yalnız genç) G1'in bölge-tabanlı, duraklama-hedefli doğasına aykırıdır.

# gcalg-4 | İleri
## Soru
"Stop-the-world" (STW) duraklaması ne anlama gelir?
## A
Uygulamanın açık ağ bağlantılarının GC sırasında kesilip yeniden kurulması
🤖 Ağ ile ilgisi yoktur; STW uygulama ipliklerinin duraklamasıdır.
## B
JVM sürecinin tamamen çökmesi (crash) ve uygulamanın yeniden başlatılması
🤖 Çökme değil; kısa süreli, planlı bir duraklamadır.
## C
GC'nin bazı aşamalarında uygulama (mutator) ipliklerinin güvenli bir noktada kısa süre duraklatılması
🤖 Doğru: STW, GC'nin güvenli çalışması için uygulama ipliklerini kısaca durdurur.
## D
Uygulamanın kilitlenip sonsuza dek donması; kurtulmanın tek yolu süreci öldürmektir
🤖 Kalıcı donma değil; iş bitince iplikler kaldığı yerden devam eder.
## Doğru
C
## Açıklama
Çöp toplamanın bazı aşamaları (ör. kökleri taramak, nesneleri taşımak) nesne grafiğinin tutarlı olmasını gerektirir; bu yüzden JVM, uygulama (mutator) ipliklerini bir **güvenli noktada (safepoint)** kısa süreliğine **duraklatır** — buna **stop-the-world (STW)** denir. Bu sırada yalnız GC iplikleri çalışır, iş bitince uygulama kaldığı yerden devam eder. Duraklama uzun olursa gecikmeye duyarlı sistemlerde sorun olur; bu yüzden G1 duraklama hedefi koyar, ZGC ise duraklamaları **milisaltı** düzeyde tutmaya çalışır. A (ağ), B (crash) ve D (kalıcı donma) STW'yi yanlış tanımlar; STW **kısa ve planlı** bir duraklamadır.

# gcalg-5 | Uzman
## Soru
Verim (throughput) en yüksek öncelikken bazı saniyelik duraklamalar kabul edilebilirse hangi toplayıcı uygundur; gecikme kritikse ne seçilir?
## A
Her iş yükü için tek bir doğru toplayıcı vardır; seçim uygulamanın hedefine göre değişmez
🤖 Yanlış; GC seçimi tam olarak iş yükü hedefine (verim mi, gecikme mi) bağlıdır.
## B
Verim öncelikse **Parallel GC**; gecikme kritikse **G1**, çok düşük duraklama gerekiyorsa **ZGC** tercih edilir
🤖 Doğru: throughput→Parallel, düşük gecikme→G1/ZGC ayrımı standarttır.
## C
Verim için **ZGC**, gecikme kritikse **Parallel GC** seçilir; ZGC toplam işi maksimize eder
🤖 Ters; ZGC düşük gecikme, Parallel yüksek verim içindir.
## D
GC seçimi performansı hiç etkilemez; toplayıcıların hepsi aynı duraklama profilini verir
🤖 Toplayıcı seçimi duraklama ve verim profilini belirgin biçimde değiştirir.
## Doğru
B
## Açıklama
GC seçimi bir **trade-off**'tur: **Parallel GC** tüm çekirdekleri toplamaya vererek **verimi (throughput)** en üst düzeye çıkarır, ama duraklamaları uzun olabilir — batch/analitik işler gibi gecikmeye duyarsız yüklerde iyidir. **G1** dengeli ve öngörülebilir duraklamalar sunar ve genel amaçlı varsayılandır. Gecikmenin kritik olduğu (düşük ve tutarlı duraklama gereken) sistemlerde **ZGC** kullanılır; ZGC duraklamaları çoğunlukla milisaltı tutar ve **nesilli (generational) ZGC JDK 23'ten beri varsayılan moddur** (nesilsiz mod kullanımdan kaldırılmaktadır). A ve D seçimin önemini yadsır; C, ZGC ile Parallel'in rollerini ters çevirir. Doğru eşleme: **verim→Parallel, düşük gecikme→G1/ZGC**.

# gcalg-6 | Uzman
## Soru
"Kaçış analizi (escape analysis)" bir JIT optimizasyonu olarak ne sağlar?
## A
Tüm nesneleri diske taşıyarak uygulamanın RAM kullanımını neredeyse sıfıra indirir, böylece heap hiç dolmaz
🤖 Diskle ilgisi yoktur; kaçış analizi nesnenin görünürlük kapsamını inceler.
## B
Çöp toplamayı tamamen devre dışı bırakır; bellek bundan sonra elle boşaltılır
🤖 GC'yi kapatmaz; yalnız bazı nesnelerin ayrılma biçimini optimize eder.
## C
Programın tüm hatalarını çalışma zamanında yakalayıp geliştiriciye raporlar
🤖 Hata yakalamayla ilgisi yoktur; bir bellek/derleme optimizasyonudur.
## D
Nesnenin oluşturulduğu metodun dışına "kaçmadığını" kanıtlarsa onu heap yerine yığıta ayırabilir ya da hiç ayırmayabilir
🤖 Doğru: kaçmayan nesnede stack ayırma / skaler değişim ile heap ve GC yükü düşer.
## Doğru
D
## Açıklama
**Kaçış analizi**, JIT derleyicisinin bir nesnenin **referansının**, oluşturulduğu metodun (ya da ipliğin) dışına "kaçıp kaçmadığını" (escape) analiz etmesidir. Nesne dışarı **kaçmıyorsa** (başka metoda/alana/iplige sızmıyorsa), derleyici onu heap'e ayırmak yerine **yığıta (stack) ayırabilir** ya da nesneyi tümden ortadan kaldırıp alanlarını doğrudan yerel değişkenlere/yazmaçlara açabilir — buna **skaler değişim (scalar replacement)** denir. Sonuç: daha az heap ayırması, dolayısıyla daha az çöp ve daha az GC duraklaması; ayrıca kilit elemesi (lock elision) gibi ek kazanımlar. A (disk), B (GC kapatma) ve C (hata yakalama) tamamen ilgisizdir; kaçış analizi bir **ayırma/GC optimizasyonudur**.
