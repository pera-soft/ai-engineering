---
slug: performans-latency
baslik: "Performans ve Gecikme: p50/p95/p99 Kuyruk Gecikmesi, Little Yasası, Darboğaz ve Tail Amplification"
kademe: İleri
sira: 152
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Sık karıştırılan iki soru vardır. "Bir istek ne kadar sürüyor?" sorusunun cevabı **gecikmedir (latency)**. Gecikme, kullanıcının notu (istek / request) sunucuya gidip cevabın (yanıt / response) geri gelmesi arasında geçen süredir. "Sistem bir saniyede kaç istek bitirebiliyor?" sorusunun cevabı ise **iş hacmidir (throughput)**. İkisi aynı şey değildir. Bir sistem saniyede 10.000 isteği bitirirken, tek bir isteği 2 saniyede tamamlıyor olabilir. Kullanıcı gecikmeyi hisseder; kapasite planlaması verimi konuşur.

Bu konunun asıl mesajı şudur: **ortalama yalan söyler**. 100 isteğin 99'u 10 milisaniyede, biri 3 saniyede bitse ortalama hâlâ "gayet iyi" görünür — ama o tek kullanıcı uygulamayı kapatır. Gerçek deneyimi ölçmek için ortalamaya değil **yüzdeliklere** bakılır.

### Temel kavramlar

- **Yüzdelik (percentile)**: bütün ölçümleri küçükten büyüğe dizip belirli bir noktadaki değeri okumaktır. **p50** (medyan) tam ortadaki istektir. **p99** ise en yavaş %1'in başladığı eşiktir. "p99 = 800ms" demek şudur: isteklerin **%99'u 800ms veya daha hızlıdır**, en yavaş %1'i ise daha yavaştır. Bu ne bir ortalamadır ne de en kötü tek örnektir (maksimum).
- **Kuyruk (tail)**: dağılımın yavaş ucu, yani p95–p99 bölgesi.
- **Darboğaz (bottleneck)**: toplam süreyi domine eden tek aşama.
- **Little Yasası** (`L = λ · W`): sistemde aynı anda uçuşta olan iş sayısı = varış hızı × ortalamada sistemde kalış süresi.
- **Tail latency amplification**: bir isteğin çok sayıda alt çağrıya bölünmesi yüzünden kuyruğa yakalanma olasılığının büyümesi.
- **Profilleme (profiling)**: zamanın gerçekte nerede geçtiğini ölçmek — tahmin etmek değil.

### Nasıl çalışır?

Gecikme dağılımının şekli, sistemin sağlığı hakkında ortalamadan çok daha fazlasını söyler: çoğunluk hızlıdır, sağa doğru uzanan ince bir kuyruk vardır ve kullanıcı şikâyetleri o kuyruktan gelir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gecikme dağılımı: p50 medyan, p99 kuyruk; kuyruk deneyimi belirler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Gecikme dağılımı: ortalama değil kuyruk</text>
  <line x1="60" y1="150" x2="600" y2="150" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="60" y1="150" x2="60" y2="50" stroke="#5D7789" stroke-width="1.5"/>
  <path d="M60,150 C150,150 175,60 230,60 C300,60 330,150 600,150" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <line x1="215" y1="60" x2="215" y2="150" stroke="#25804C" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="215" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">p50</text>
  <line x1="430" y1="128" x2="430" y2="150" stroke="#B4531F" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="430" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#B4531F" font-family="system-ui">p95</text>
  <line x1="510" y1="140" x2="510" y2="150" stroke="#B4531F" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="512" y="170" text-anchor="middle" font-size="11" font-weight="700" fill="#B4531F" font-family="system-ui">p99</text>
  <text x="540" y="96" text-anchor="middle" font-size="10.5" fill="#B4531F" font-family="system-ui">uzun kuyruk</text>
  <text x="540" y="112" text-anchor="middle" font-size="10.5" fill="#B4531F" font-family="system-ui">(tail)</text>
  <text x="120" y="46" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çoğunluk hızlı…</text>
</svg>
```

Kıdemli mühendis ortalamayı değil kuyruğu izler. Sebebi şudur: tek bir kullanıcı isteği çoğu zaman arkada birçok servise dokunur. Bu servislerden herhangi birinin yavaşlaması tüm isteği yavaşlatır. Ortalama, o birkaç yavaş isteği kalabalık hızlı isteklerin içinde eritir ve "her şey yolunda" yanılsaması verir.

Kapasite sezgisinin çekirdeği **Little Yasası**'dır. Saniyede 500 istek geldiğini (λ = 500/sn) ve her isteğin ortalama 0,2 saniye sistemde kaldığını (W = 0,2 sn) düşün. O zaman her an uçuşta olan iş sayısı `L = 500 × 0,2 = 100` olur. Yani en az 100 isteği aynı anda taşıyabilecek bir kapasiten olmalıdır; bu kapasite iş parçacığı ya da bağlantı havuzudur. Aksi hâlde istekler kuyrukta beklemeye başlar ve **kuyruk gecikmesi** doğar.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Little Yasası: L eşittir lambda çarpı W">
  <defs>
    <marker id="perf-ll-ar" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Little Yasası: L = λ · W</text>
  <text x="70" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">λ = 500/sn</text>
  <text x="70" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">varış hızı</text>
  <line x1="120" y1="80" x2="196" y2="80" stroke="#5D7789" stroke-width="1.6" marker-end="url(#perf-ll-ar)"/>
  <rect x="200" y="52" width="240" height="80" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="320" y="82" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Sistem (uçuştaki iş)</text>
  <text x="320" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">L = 500 × 0.2 = 100</text>
  <text x="320" y="122" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">W = 0.2 sn kalış süresi</text>
  <line x1="440" y1="80" x2="516" y2="80" stroke="#5D7789" stroke-width="1.6" marker-end="url(#perf-ll-ar)"/>
  <text x="565" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">çıkış</text>
  <text x="565" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tamamlanan</text>
  <text x="320" y="164" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">100 iş aynı anda uçuşta → eşzamanlılık kapasiten ≥ 100 olmalı</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">thread / bağlantı havuzu boyutlandırmanın temeli</text>
</svg>
```

Gecikmeyi iyileştirmek istediğinde ilk iş **darboğazı bulmaktır**. Bir istek yolunu düşün: API (5ms) → önbellek (2ms) → veritabanı (80ms) → serileştirme (3ms). Toplam 90ms'nin 80ms'si veritabanındadır. Darboğaz dışındaki üç aşamayı sıfırlasan bile toplam ancak 90ms'den 80ms'ye iner. Buna karşılık veritabanını yarıya indirmek 90ms'yi 50ms'ye çeker. Bu yüzden "ilk aşama olduğu için" veya "en kolayı olduğu için" bir aşamayı optimize etmek, teoride bile getirisizdir.

Dağıtık sistemlerde kuyruğun sinsi bir çarpanı vardır. Bir istek 100 bölmeye dağılıp (scatter) hepsinin yanıtını bekliyorsa (gather), toplam gecikmeyi **en yavaş bölme** belirler. Her bölmenin yalnız %1 ihtimalle kuyruğa düştüğünü varsayalım. Yine de 100 bağımsız çağrıdan **en az birinin** kuyruğa düşme olasılığı `1 − 0,99¹⁰⁰ ≈ %63`'tür. Yani medyanda 1ms olması gereken istek, tipik olarak bölmenin p99 değerine, yaklaşık 10ms'ye çekilir.

Buna **kuyruk gecikmesi çoğullanması (tail latency amplification)** denir. Üç çaresi vardır: aynı işi ikinci bir kopyaya da sorup ilk geleni almak (hedged request), zaman aşımı koyup yedek cevap dönmek ve dağıtım genişliğini küçültmek.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tail amplification: hepsini bekle en yavaş shard'ı bekler">
  <defs>
    <marker id="perf-ta-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0,0 L6,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Scatter-gather: en yavaş shard belirler</text>
  <rect x="272" y="40" width="96" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İstek</text>
  <line x1="300" y1="80" x2="150" y2="108" stroke="#5D7789" stroke-width="1.3" marker-end="url(#perf-ta-ar)"/>
  <line x1="315" y1="80" x2="290" y2="108" stroke="#5D7789" stroke-width="1.3" marker-end="url(#perf-ta-ar)"/>
  <line x1="330" y1="80" x2="430" y2="108" stroke="#5D7789" stroke-width="1.3" marker-end="url(#perf-ta-ar)"/>
  <line x1="345" y1="80" x2="560" y2="108" stroke="#5D7789" stroke-width="1.3" marker-end="url(#perf-ta-ar)"/>
  <rect x="112" y="112" width="76" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="150" y="130" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">shard 1</text>
  <text x="150" y="143" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">1ms</text>
  <rect x="252" y="112" width="76" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="290" y="130" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">shard 2</text>
  <text x="290" y="143" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">1ms</text>
  <rect x="392" y="112" width="76" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="430" y="130" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">…</text>
  <text x="430" y="143" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">1ms</text>
  <rect x="522" y="112" width="76" height="36" rx="7" fill="#F7E9E9" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="560" y="130" text-anchor="middle" font-size="10" font-weight="700" fill="#8A2E2E" font-family="system-ui">shard 100</text>
  <text x="560" y="143" text-anchor="middle" font-size="9.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">10ms (tail)</text>
  <text x="320" y="176" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">P(en az biri kuyrukta) = 1 − 0.99¹⁰⁰ ≈ %63</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">medyan ~1ms olmalıyken istek tipik olarak ~10ms'ye çekilir</text>
</svg>
```

Son ilke ölçmeye dairdir. Bir servis yavaş ama işlemci kullanımı düşükse, sistem çalışmıyor demektir; **bekliyordur**. Bekleme sebebi disk/ağ (G/Ç) beklemesi, kilit çekişmesi (lock contention) ya da yavaş bir alt servistir. Doğru ilk adım, dağıtık **izleme kaydıyla (tracing)** veya alev grafiğiyle (flame graph) **duvar saati zamanının nerede geçtiğini görmektir**.

Ölçmeden müdahale etmenin üç klasik biçimi vardır: tahmine dayanarak fonksiyon yeniden yazmak, zaten boşta duran işlemciye kapasite eklemek ve iş parçacığı havuzunu kanıtsız büyütmek. Sonuncusu çekişmeyi daha da artırabilir.

### Mülakatta ne sorulur?

- "p99 = 800ms ne demek?" → isteklerin %99'u ≤ 800ms; ortalama ve maksimumla karıştırma.
- "Neden ortalama değil p99?" → ortalama kuyruğu gizler; kullanıcı isteği çok servise dokunur.
- "λ = 500/sn, W = 0,2 sn iken L kaçtır?" → `L = λ·W = 100`; eşzamanlılık kapasitesi bundan az olamaz.
- "Tek aşamayı optimize etme şansın var, hangisi?" → darboğaz; gerekçeyi toplam içindeki payla anlat.
- "100 bölmeye dağılan istek ne kadar sürer?" → En yavaş bölme kadar. Kuyruk gecikmesi çoğullanmasını ve `1 − 0,99¹⁰⁰` hesabını anlat.
- "Yavaş ama CPU düşük; ilk adım ne?" → Profilleme ve izleme. Sebep büyük ihtimalle G/Ç beklemesi ya da kilit çekişmesidir.

### Tek cümlede

Performans işi üç adımdır: kuyruğu ölçmek, darboğazı ölçerek bulmak ve yalnız onu iyileştirmek. Ortalamaya bakmak ile tahmin yürütmek ise bu işteki en pahalı iki alışkanlıktır.

# perf-1 | Orta
## Soru
"p99 gecikme = 800ms" ne anlama gelir?
## A
Ortalama bir istek 800ms sürüyor demektir
🤖 O ortalamadır (mean), p99 değil.
## B
İsteklerin %99'u 800ms'den daha yavaş tamamlanır
🤖 Ters; ifade tam tersini söyler.
## C
İsteklerin %1'i 800ms'den yavaştır (%99'u ≤ 800ms)
🤖 Doğru: p99, dağılımın 99. yüzdelik noktasıdır.
## D
Şimdiye kadar ölçülen en yavaş istek 800ms sürmüştür
🤖 O maksimumdur (max), p99 değil.
## Doğru
C
## Açıklama
**p99**, gecikme dağılımının 99. yüzdeliğidir: isteklerin **%99'u ≤ 800ms**, en yavaş **%1'i > 800ms**. Bu ortalama (A) değildir — ortalama kuyruğu gizler; "%99'u yavaş" (B) ifadenin tersidir; maksimum (D) ise tek en kötü örnektir, p99 istatistiksel bir eşiktir. Yüzdelikler, birkaç aşırı örnek yerine kullanıcıların büyük çoğunluğunun ne gördüğünü ölçtüğü için performans hedeflerinin doğru dilidir.

# perf-2 | İleri
## Soru
Kıdemli mühendisler neden ortalama gecikme yerine p99'u izler?
## A
Ortalamayı hesaplamak p99'a göre daha zordur
🤖 Ortalama önemsizdir; sebep bu değil.
## B
Ortalama kuyruğu gizler; istek birçok servise dokunduğu için deneyimi kuyruk belirler
🤖 Doğru: fanout ve zincirleme çağrılarla kuyruk deneyimi belirler.
## C
p99 pratikte her zaman ortalamaya çok yakın çıkar
🤖 Yanlış; ikisinin ayrışması zaten meselenin özüdür.
## D
Ortalama gerçek zamanlı olarak izlenemez, p99 izlenebilir
🤖 İzlenebilir; konuyla ilgisiz.
## Doğru
B
## Açıklama
Ortalama, birkaç yavaş isteği kalabalık hızlı isteklerin içinde **eritir** ve "her şey yolunda" yanılsaması verir. Oysa gerçek bir kullanıcı isteği çoğu zaman birçok alt servise dokunur; bu servislerden herhangi birinin kuyruğa düşmesi tüm isteği yavaşlatır. Dolayısıyla **kuyruk** (p95/p99) gerçek kullanıcı deneyimini belirler. p99 ile ortalamanın ayrışması (C yanlış) tam da izlenmesi gereken şeydir; hesaplama zorluğu (A) veya izlenebilirlik (D) konuyla ilgisizdir. "Ortalaman iyi olabilir ama kuyruğun kullanıcılarını kaybettirir."

# perf-3 | İleri
## Soru
Bir servis λ=500 istek/sn işliyor, ortalama sistemde-kalış süresi W=0.2 sn. Little Yasası'na göre ortalama eşzamanlı (uçuştaki) istek sayısı L kaçtır?
## A
100
🤖 Doğru: L = λ·W = 500 × 0.2 = 100.
## B
2500
🤖 Bu λ/W'dir; yanlış formül.
## C
500
🤖 W'yi yok sayar; bu yalnız λ'dır.
## D
0.2
🤖 Bu yalnız W'dir.
## Doğru
A
## Açıklama
**Little Yasası**: `L = λ · W`, yani sistemdeki ortalama iş (eşzamanlı istek) sayısı = varış hızı × ortalamada sistemde kalış süresi. Burada `L = 500 × 0.2 = 100` eşzamanlı istek. Bu, thread havuzu / bağlantı havuzu boyutlandırma ve kuyruk sezgisi için temeldir: 100 iş aynı anda uçuştaysa en az o kadar eşzamanlılık kapasiten olmalı. λ/W (B) formülü ters; yalnız λ (C) veya yalnız W (D) çarpımı atlar. Yasa varış sürecinden bağımsız olarak (kararlı sistemde) geçerlidir — bu yüzden bu kadar güçlüdür.

# perf-4 | İleri
## Soru
Bir istek yolu: API(5ms) → cache(2ms) → DB(80ms) → serialize(3ms). Tek bir aşamayı optimize etme bütçen var. Hangisi ve neden?
## A
API, çünkü zincirin ilk aşaması ve baştaki kazanç en değerlidir
🤖 Sıra etkiyi belirlemez; API zaten çok küçük (5ms).
## B
Serileştirme, çünkü saf CPU işi ve optimize etmesi en kolay aşama
🤖 3ms ihmal edilebilir; efor/etki oranı zayıf.
## C
Önbellek, çünkü her istekte devreye giren kritik bir katman
🤖 2ms zaten çok küçük; anlamlı kazanç yok.
## D
Veritabanı, çünkü 90ms'lik toplamın 80ms'si orada geçiyor
🤖 Doğru: 80ms toplamın ~%90'ı; kazanç ancak buradan gelir.
## Doğru
D
## Açıklama
Toplam gecikme 90ms'nin **80ms'si DB'de**; yani DB **darboğazdır** (bottleneck). Amdahl mantığı: darboğaz-dışı aşamaları (API 5ms, cache 2ms, serialize 3ms) ne kadar hızlandırırsan hızlandır, hepsini sıfırlasan bile toplam en fazla 90→80ms olur. DB'yi %50 iyileştirmek ise toplamı 90→50ms yapar. "İlk olduğu için" (A), "CPU işi kolay olduğu için" (B) veya "önbellekler önemli olduğu için" (C) optimize etmek eforu yanlış yere harcar. Kural: **önce darboğazı bul, sadece onu optimize et** — gerisi teoride bile getirisizdir.

# perf-5 | İleri
## Soru
Bir istek 100 arka-uç shard'ına dağılıp hepsinin yanıtını bekliyor. Her shard'ın p99=10ms, p50=1ms. İsteğin toplam gecikmesini ne belirler?
## A
Medyan (p50=1ms), çünkü shard'ların büyük çoğunluğu 1ms'de yanıtlar
🤖 hepsini beklediğin için en yavaş shard belirler, çoğu değil.
## B
En yavaş shard; 100 paralel çağrıda tail'e çarpmak neredeyse kesindir (tail amplification)
🤖 Doğru: "hepsini bekle" en yavaşı bekler; 100 çağrıda tail'e çarpma neredeyse kesindir.
## C
Tüm shard gecikmelerinin toplamı, çünkü çağrılar sırayla yapılır
🤖 Paralel çalışırlar; toplanmaz.
## D
Yalnız ağ gecikmesi; shard'ın kendi işlem süresi belirleyici değildir
🤖 Tam da shard kuyruğu domine eder.
## Doğru
B
## Açıklama
"Hepsini bekle" (scatter-gather) deseninde isteğin gecikmesi = **en yavaş shard**ın gecikmesi. Her shard'ın %1 kuyruk olasılığı olsa bile, 100 bağımsız shard'da **en az birinin** kuyruğa düşme olasılığı `1 − 0.99^100 ≈ %63`'tür. Yani medyanda ~1ms olması gerekirken istek tipik olarak ~10ms'ye (shard p99'una) yaklaşır — bu **tail latency amplification**'dır. Medyan (A) tek shard için doğru olurdu; paralel çağrılar toplanmaz (C); domine eden şey ağ değil (D) shard kuyruğudur. Çözüm: hedged request, timeout+fallback, fanout'u küçültme.

# perf-6 | Orta
## Soru
Bir servis yavaş ama CPU düşük. Nedeni bulmak için en doğru ilk adım nedir?
## A
Tahmin ettiğin en sıcak fonksiyonu ölçmeden yeniden yazmak
🤖 Ölçmeden tahminle iş; "önce ölç" ilkesini çiğner.
## B
Daha fazla CPU çekirdeği ve yeni instance ekleyip kapasiteyi büyütmek
🤖 CPU zaten düşük; CPU eklemek I/O/bekleme darboğazını çözmez.
## C
Thread havuzunu kanıt aramadan iki katına çıkarmak
🤖 Çekişmeyi (contention) artırabilir; kanıt yok.
## D
İsteği profillemek/izlemek (tracing, flame graph) ve zamanın nerede geçtiğini görmek
🤖 Doğru: düşük CPU + yavaşlık = bekleme; önce nerede beklendiğini ölç.
## Doğru
D
## Açıklama
Düşük CPU + yüksek gecikme klasik olarak **bekleme** (wait) demektir: I/O beklemesi, kilit çekişmesi (lock contention) veya downstream gecikmesi — CPU'ya bağlı değil. Doğru ilk adım **profilleme/izleme**dir: dağıtık tracing veya flame graph ile isteğin duvar-saati (wall-clock) zamanının nerede geçtiğini görürsün. Tahminle fonksiyon yeniden yazmak (A), CPU eklemek (B — zaten düşük) veya thread havuzunu körlemesine büyütmek (C — çekişmeyi artırabilir) hepsi ölçmeden müdahaledir. İlke: **önce gözlemle, sonra düzelt** — çıktı yalan söylemez, varsayım söyler.
