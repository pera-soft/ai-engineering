---
slug: caching-stratejileri
baslik: "Önbellekleme (Caching): Cache-Aside/Write-Through/Write-Back, Eviction, TTL ve Tutarlılık"
kademe: İleri
sira: 144
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir programın sakladığı bilgiler (kullanıcılar, siparişler, yorumlar) elektrik kesilince kaybolmasın diye **veritabanı** (database) denen özel bir programda, diskte tutulur. Diskten okumak güvenilirdir ama yavaştır. Bilgisayarın hızlı belleğine (RAM) göre binlerce kat daha uzun sürer. Üstelik aynı bilgi saniyede binlerce kez istenir; örneğin ana sayfadaki en popüler 10 haber. Her seferinde de aynı yavaş yolculuk baştan tekrarlanır.

**Önbellek** (cache), sık kullanılan verinin **hızlı erişilen bir yerde tutulan kopyasıdır**. Masanızda çalışırken en çok baktığınız kitabı her seferinde rafa götürüp geri getirmek yerine masanın üstünde tutmaya benzer. Kazanç ikilidir: kullanıcı cevabı daha çabuk alır (**gecikme / latency** düşer) ve veritabanı daha az yorulur (arka uç yükü azalır). Bedeli ise şudur: elinizdeki artık **kopyadır**, aslı değişmiş olabilir.

### Temel kavramlar

- **Hit / miss**: aranan veri önbellekte varsa *hit*, yoksa *miss*. Miss bir hata değildir, sadece "burada yok, kaynağa git" demektir.
- **TTL (time-to-live)**: bir kaydın önbellekte kalacağı ömür; süre dolunca kayıt kendiliğinden düşer.
- **Eviction (tahliye)**: bellek dolduğunda hangi kaydın atılacağına karar veren politika (LRU, LFU).
- **Geçersiz kılma (invalidation)**: veritabanındaki asıl veri değişince önbellekteki kopyayı temizlemek.
- **Stale (eski/bayat) veri**: aslı değişmiş olduğu hâlde önbellekte duran eski kopya.
- **CDN**: içeriği kullanıcıya coğrafi olarak yakın "kenar" (edge) sunucularda tutan en dış önbellek katmanı.

### Nasıl çalışır?

**Okuma tarafında** iki temel desen vardır.

**Yanda önbellek (cache-aside / lazy loading)**: uygulama önce önbelleğe bakar. Veri yoksa (miss) veritabanından okur, dönen sonucu önbelleğe yazar — genelde bir TTL ile — ve çağırana döndürür. Doldurma sorumluluğu **uygulamadadır**. Bu yüzden önbellek tamamen çökse bile sistem yavaşlayarak da olsa çalışmaya devam eder.

**Baştan geçen okuma (read-through)**: ıskayı uygulama değil, **önbellek katmanı** çözer. Veritabanını kendisi okuyup önbelleği doldurur; uygulama yalnız önbellekle konuşur.

**Yazma tarafında** da iki uç vardır.

**Baştan geçen yazma (write-through)**: yazma **hem önbelleğe hem veritabanına aynı anda** gider. İşlem başarılıysa veri kalıcıdır; önbellek düğümü çökse bile veri veritabanında durur. Tutarlıdır; ama yazma gecikmesi iki katmanın toplamı kadardır.

**Geriden yazma (write-back / write-behind)**: yazma önce yalnız önbelleğe yapılır ve çağırana hemen dönülür. Veritabanına yazma **eşzamansız** olarak ertelenir. Çok hızlıdır; ama bekleyen yazılar diske inmeden önbellek çökerse o veriler **kalıcı olarak kaybolur**. Bu yüzden write-back metrik/sayaç gibi az kayba toleranslı işlerde tercih edilir, para hareketinde asla.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Write-through ve write-back yazma stratejileri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yazma nereye, ne zaman gider?</text>
  <rect x="28" y="44" width="272" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">Write-through</text>
  <text x="164" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">cache + DB eşzamanlı yazılır</text>
  <text x="164" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">tutarlı, kayıp yok</text>
  <text x="164" y="140" text-anchor="middle" font-size="11" fill="#B23A48" font-family="system-ui">yazma gecikmesi yüksek</text>
  <text x="164" y="162" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">okuma-ağır iş yükü için iyi</text>
  <rect x="340" y="44" width="272" height="140" rx="12" fill="#FBEEE6" stroke="#C25A2E" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#C25A2E" font-family="system-ui">Write-back</text>
  <text x="476" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">cache'e yaz, DB'ye sonra asenkron</text>
  <text x="476" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">çok hızlı yazma</text>
  <text x="476" y="140" text-anchor="middle" font-size="11" fill="#B23A48" font-family="system-ui">cache çökerse veri kaybı</text>
  <text x="476" y="162" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yazma-ağır, kayba toleranslı için</text>
</svg>
```

Aşağıdaki akış, cache-aside'da bir miss'in adım adım nasıl çözüldüğünü gösterir: önce önbellek sorulur, orada yoksa veritabanı okunur, sonuç önbelleğe yazılır ve bir sonraki istek artık hit olur.

```svg
<svg viewBox="0 0 600 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cache-aside deseninde okuma miss akışı">
  <defs>
    <marker id="csd144-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="300" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Cache-aside: miss olunca akış</text>
  <rect x="24" y="68" width="150" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="99" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Uygulama</text>
  <rect x="386" y="26" width="180" height="52" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="476" y="50" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Cache</text>
  <text x="476" y="68" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">(miss — yok)</text>
  <rect x="386" y="112" width="180" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="143" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Veritabanı</text>
  <line x1="174" y1="86" x2="384" y2="55" stroke="#5D7789" stroke-width="1.6" marker-end="url(#csd144-arw)"/>
  <text x="266" y="58" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">① oku → miss</text>
  <line x1="174" y1="104" x2="384" y2="134" stroke="#5D7789" stroke-width="1.6" marker-end="url(#csd144-arw)"/>
  <text x="262" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">② DB'den oku</text>
  <line x1="476" y1="112" x2="476" y2="80" stroke="#5D7789" stroke-width="1.6" marker-end="url(#csd144-arw)"/>
  <text x="556" y="98" text-anchor="end" font-size="10.5" fill="#25804C" font-family="system-ui">③ cache'e yaz</text>
</svg>
```

Bellek sınırlıdır, o yüzden dolunca bir şeyler atılmalıdır. **LRU (Least Recently Used)** en uzun süredir kullanılmayanı atar. Yakın geçmişi (recency) iyi yakalar; ama **taramaya dirençli değildir**. Çok sayıda tek seferlik erişim — bir "tarama" — sıralamayı doldurur ve gerçekten popüler olan, ama son anda istenmemiş kayıtları dışarı atar. Buna önbellek kirlenmesi denir.

**LFU (Least Frequently Used)** erişim **sıklığını** sayar. Bu yüzden popüler kayıtları tarama selinde korur. Ama onun da bir zaafı vardır: eskiden popüler olup artık istenmeyen kayıtlar yüksek sayaçlarıyla önbelleğe "yapışır". Bunun için yaşlandırma (aging) ya da melez politikalar gerekir. **TTL** ayrı bir emniyet supabıdır: eski veriyi otomatik düşürür, ama çok kısa TTL isabet oranını düşürür.

Asıl zor konu **geçersiz kılmadır**. Önbellek ile veritabanı **iki ayrı sistemdir** ve güncellemeleri **bölünmez (atomik) değildir**. "Önce veritabanını yaz, sonra önbelleği sil" ile "önce önbelleği sil, sonra veritabanını yaz" seçenekleri arasında yarış durumları (race condition) vardır. Araya giren bir okuma, önbelleği eski değerle yeniden doldurabilir; o değer de TTL dolana kadar orada yapışır.

Pratik kural şudur: yazınca önbelleği **güncellemek yerine sil**. Bir sonraki okuma taze değeri kendisi doldurur. Ayrıca değişiklik akışını (CDC / binlog) dinleyip geçersiz kılmayı eşzamansız yapmak da yaygındır.

Bir başka klasik tuzak **önbellek izdihamıdır (cache stampede / thundering herd)**. Sıcak bir anahtarın TTL'i dolar dolmaz, o anda gelen isteklerin hepsi ıska alır ve aynı anda veritabanına saldırır. Üç çözüm vardır. **Tek doldurucu (single-flight / mutex)** ile yalnız bir isteğin doldurmasına izin verip diğerlerini bekletmek. Yenileme sürerken eski değeri sunmak (stale-while-revalidate). Bir de TTL dolmadan önce **erken ve olasılığa dayalı yenileme** yapmak.

Önbellek tek katmandan da ibaret değildir. Üç katman üst üste konur: süreç içi (in-process), mikrosaniyelik küçük bir L1; paylaşımlı Redis'ten oluşan bir L2; ve coğrafi bir CDN katmanı. Her katman ıska verince bir sonraki, daha yavaş katmana inilir.

```svg
<svg viewBox="0 0 640 172" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Çok katmanlı önbellek hiyerarşisi: in-process, Redis, CDN, veritabanı">
  <defs>
    <marker id="csd144b-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çok katmanlı önbellek: hızlıdan yavaşa</text>
  <rect x="10" y="52" width="132" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="76" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">L1 in-process</text>
  <text x="76" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">µs · tek sunucu</text>
  <rect x="170" y="52" width="132" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="236" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">L2 Redis</text>
  <text x="236" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ms · paylaşımlı</text>
  <rect x="330" y="52" width="132" height="62" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="396" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">L3 CDN edge</text>
  <text x="396" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">coğrafi · statik</text>
  <rect x="490" y="52" width="140" height="62" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="560" y="78" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">DB / origin</text>
  <text x="560" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaynak · en yavaş</text>
  <line x1="142" y1="83" x2="168" y2="83" stroke="#5D7789" stroke-width="1.6" marker-end="url(#csd144b-arw)"/>
  <line x1="302" y1="83" x2="328" y2="83" stroke="#5D7789" stroke-width="1.6" marker-end="url(#csd144b-arw)"/>
  <line x1="462" y1="83" x2="488" y2="83" stroke="#5D7789" stroke-width="1.6" marker-end="url(#csd144b-arw)"/>
  <text x="320" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her katman miss verince bir sonraki (daha yavaş) katmana iner</text>
</svg>
```

**CDN**, bu hiyerarşinin en dış halkasıdır. Statik ya da seyrek değişen içeriği — görsel, video, JS/CSS, yazı tipi — kullanıcıya en yakın kenar düğümünde tutar. Böylece hem gidiş-dönüş süresi (RTT) hem de merkez (origin) sunucunun yükü düşer. Kişiye özel dinamik sayfalar genelde önbelleklenmez.

### Mülakatta ne sorulur?

- **Cache-aside'da miss akışı nedir?** Uygulama veritabanından okur, sonucu önbelleğe yazar, çağırana döndürür. Miss'i önbellek katmanının kendisinin çözmesi read-through'dur.
- **Baştan geçen yazma mı, geriden yazma mı?** Kritik fark **dayanıklılıktır**. Baştan geçen yazma (write-through) veritabanına aynı anda yazdığı için kayıp olmaz. Geriden yazmada (write-back) ise bekleyen yazılar önbellekle birlikte yok olabilir.
- **LRU mu LFU mu?** Araya tek-seferlik erişim seli giren senaryoda LRU popüler kayıtları atabilir; LFU frekansı saydığı için korur — ama LFU'da eski popülerlik yapışır.
- **TTL dolunca binlerce istek veritabanına saldırıyor.** Bu **önbellek izdihamıdır (cache stampede)**. Çözüm tek doldurucu + kilit/bekletme ya da erken (olasılığa dayalı) yenilemedir; TTL'i kaldırmak değil.
- **CDN ne yapar?** Statik içeriği coğrafi olarak yakın kenar düğümlerinde önbelleyip gecikmeyi ve origin yükünü düşürür.
- **Geçersiz kılma neden zor?** İki ayrı sistem, bölünmez olmayan güncelleme, yarış durumları ve kısmi hatalar yüzünden. TTL yalnız bayatlığın süresini sınırlar; sorunu ortadan kaldırmaz.

### Tek cümlede

Önbellek, sık istenen verinin hızlı bir kopyasını tutarak gecikmeyi ve arka uç yükünü düşürür. Asıl mühendislik zorluğu hızda değildir; o kopyanın ne zaman ve nasıl geçersiz kılınacağındadır.

# csd-1 | Orta
## Soru
Cache-aside (lazy loading) deseninde bir okuma **miss** (cache'te yok) olduğunda akış nasıldır?
## A
Uygulama DB'den okur, veriyi cache'e yazar ve sonucu çağırana döndürür
🤖 Doğru: miss'te uygulama DB'yi okur, sonucu cache'e doldurur, sonraki okumalar hızlanır.
## B
Cache katmanı DB'yi kendisi okur; uygulama veritabanını hiç görmez
🤖 Bu read-through davranışıdır; cache-aside'da DB okumasını *uygulama* yapar.
## C
İstek hata (error) döner; miss durumunda veri kaynağına hiç gidilmez
🤖 Miss bir hata değildir; sadece "cache'te yok" demektir, kaynağa gidilir.
## D
Yazma DB'ye, okuma cache'e gider; bu desende miss diye bir durum oluşmaz
🤖 Miss her zaman mümkündür (TTL, eviction, ilk erişim); bu tanım yanlıştır.
## Doğru
A
## Açıklama
**Cache-aside**'da önbelleği doldurma sorumluluğu **uygulamadadır**: önce cache'e bakılır, yoksa (miss) uygulama DB'den okur, dönen veriyi cache'e yazar (genelde TTL ile) ve çağırana döndürür; sonraki okumalar cache'ten (hit) gelir. Farkı **read-through**'dadır: orada miss'i **cache katmanı** kendisi DB'den doldurur, uygulama yalnız cache ile konuşur. Cache-aside'ın avantajı basitliği ve cache çökse bile uygulamanın DB ile çalışmaya devam edebilmesidir; dezavantajı ilk erişimlerdeki ek gecikme ve invalidation'ı elle yönetme zorunluluğudur.

# csd-2 | İleri
## Soru
Write-through ve write-back (write-behind) arasındaki en kritik dayanıklılık (durability) farkı nedir?
## A
Write-back her zaman daha tutarlıdır, çünkü tek doğru kaynak yine veritabanıdır
🤖 Tam tersi; write-back'te DB güncellemesi gecikir, cache ile DB geçici olarak uyumsuz kalır.
## B
Write-through yazmayı yalnız cache'e yapar; DB'yi güncellemediği için kalıcılık sağlamaz
🤖 Aksine, write-through *hem* cache *hem* DB'ye eşzamanlı yazar; DB'yi atlamaz.
## C
İki strateji arasında dayanıklılık farkı yoktur; aralarındaki tek fark yazma hızıdır
🤖 Fark tam da dayanıklılıktadır: asenkron kuyruk boşalmadan cache çökerse veri kaybolur.
## D
Write-through DB'ye de eşzamanlı yazar; write-back'te bekleyen yazılar cache çökünce kaybolur
🤖 Doğru: write-through eşzamanlı yazar (kalıcı), write-back gecikmeli yazar (kayıp riski).
## Doğru
D
## Açıklama
**Write-through** yazmayı **senkron olarak hem cache'e hem DB'ye** uygular; işlem başarılıysa veri kalıcıdır, cache düğümü çökse bile DB'de mevcuttur — tutarlıdır ama yazma gecikmesi iki katmanın toplamıdır. **Write-back** yazmayı önce **yalnız cache'e** yapıp çağırana hemen döner, DB'ye yazmayı **asenkron** (kuyruk/batch) erteler — çok hızlıdır ama kuyruk boşalmadan cache çökerse o yazılar **kalıcı olarak kaybolur**. Bu yüzden write-back yalnız yazma hızının kritik ve az miktarda kayba tolerans gösterilebildiği durumlarda (örn. metrik/sayaç) tercih edilir; finansal işlemler gibi kayba tahammülsüz veride write-through veya doğrudan DB yazımı gerekir.

# csd-3 | İleri
## Soru
Bir haber sitesinde birkaç "her zaman popüler" makale sürekli okunuyor, ama araya çok sayıda tek-seferlik makale erişimi giriyor. LRU önbellek burada hangi sorunu yaşayabilir?
## A
Tek-seferlik erişim seli popüler makaleleri atabilir; LFU frekansı saydığı için korur
🤖 Doğru: LRU yalnız "en son"a bakar, tarama popüler ama biraz eski girdileri tahliye eder.
## B
LRU frekansa baktığı için popüler makaleleri atmaz; ek bir önlem gerekmez
🤖 LRU frekansa değil, en son kullanıma (recency) bakar; frekansı yakalayan LFU'dur.
## C
LFU her zaman LRU'dan üstündür; bu yüzden her senaryoda LFU seçilmelidir
🤖 LFU'nun da zaafı var: eski popülerlik yapışır, yaşlandırma (aging) gerekir.
## D
Sorun tahliye politikasında değil, yalnız TTL süresinin yanlış ayarlanmasındadır
🤖 TTL ömür verir ama tarama-kaynaklı tahliye (cache pollution) bir eviction sorunudur.
## Doğru
A
## Açıklama
**LRU (Least Recently Used)** en son kullanılmayanı atar; **zamansal yerelliği** (recency) yakalar ama **tarama direncine (scan resistance)** sahip değildir: çok sayıda tek-seferlik erişim (bir "scan") LRU sıralamasını doldurup gerçekten popüler ama son anda erişilmemiş girdileri tahliye eder — buna önbellek kirlenmesi (pollution) denir. **LFU (Least Frequently Used)** erişim **sıklığını** saydığından bu popüler girdileri korur. Ancak LFU'nun kendi zaafı vardır: eskiden popüler olup artık istenmeyen girdiler yüksek sayaçları yüzünden "yapışır"; bunu çözmek için yaşlandırma (aging) veya LRU-LFU melezleri (örn. Redis'in LFU'su, W-TinyLFU) kullanılır.

# csd-4 | İleri
## Soru
Yoğun trafikli bir anahtarın TTL'i dolduğu anda binlerce istek aynı anda cache miss alıp hepsi birden DB'ye gidiyor ve DB'yi çökertiyor. Bu soruna ne denir ve tipik çözümü nedir?
## A
Buna false sharing denir; çözümü sıcak anahtarın TTL'ini tamamen kaldırmaktır
🤖 False sharing bir CPU önbellek hattı kavramıdır; TTL'i kaldırmak da eskimeyi bozar.
## B
Buna cache stampede denir; çözüm tek doldurucu (single-flight) veya erken yenilemedir
🤖 Doğru: eşzamanlı miss seli tek doldurucu + kilit ya da erken yenileme ile engellenir.
## C
Buna write amplification denir; çözüm write-back'e geçip yazmaları toplu hale getirmektir
🤖 Write amplification bir depolama-yazma kavramıdır; bu senaryo bir okuma stampede'idir.
## D
Bu normaldir ve önlenemez; tek çözüm veritabanını dikey ölçekleyip donanımı büyütmektir
🤖 Önlenebilir; single-flight/kilit ve erken yenileme gibi bilinen desenler vardır.
## Doğru
B
## Açıklama
Bu klasik soruna **cache stampede** (veya thundering herd / dogpile) denir: sıcak bir anahtarın TTL'i dolar dolmaz eşzamanlı isteklerin hepsi miss alır ve aynı anda DB'yi yeniden doldurmaya kalkar, arka ucu ezer. Tipik çözümler: (1) **single-flight / mutex** — yalnız bir isteğin yeniden doldurmasına izin ver, diğerleri o dolana kadar beklesin veya eski değeri (stale-while-revalidate) alsın; (2) **erken/olasılıksal yenileme (probabilistic early expiration)** — TTL dolmadan biraz önce rastgele bir istek arka planda yeniler; (3) **istek birleştirme (request coalescing)**. TTL'i kaldırmak eskimeyi bozar, çözüm değildir.

# csd-5 | Orta
## Soru
Bir CDN (Content Delivery Network) tipik olarak neyi, nerede önbellekler ve asıl faydası nedir?
## A
Veritabanı sorgu sonuçlarını uygulama sunucusunun RAM'inde önbellekler
🤖 Bu in-process/uygulama cache'idir; CDN'in yaptığı iş bu değildir.
## B
Yalnız yazma işlemlerini kuyruğa alır; okuma tarafına hiç dokunmaz
🤖 CDN okuma/dağıtım için bir kenar önbelleğidir; yazma kuyruğu değildir.
## C
Statik içeriği kullanıcıya coğrafi olarak yakın kenar (edge) düğümlerinde tutar
🤖 Doğru: içerik kullanıcıya yakın edge'de tutulur; RTT ve origin yükü düşer.
## D
Kişiye özel dinamik sayfaları edge'de tutup herkese aynı sayfayı gösterir
🤖 Kişiye özel dinamik içerik genelde önbelleklenmez; bu kişiselleştirmeyi bozar.
## Doğru
C
## Açıklama
**CDN**, coğrafi olarak dağıtılmış kenar (edge / PoP) sunuculardan oluşan bir katmandır; **statik veya seyrek değişen içeriği** (görsel, video, JS/CSS bundle, yazı tipi) kullanıcının ağ olarak en yakın edge düğümünde önbelleyerek iki fayda sağlar: (1) **gidiş-dönüş süresi (RTT / latency)** düşer, çünkü içerik uzaktaki origin yerine yakındaki edge'den gelir; (2) **origin (merkez) sunucu yükü** azalır, çünkü tekrarlı istekler edge'de karşılanır. Dinamik/kişiye özel içerik genelde önbelleklenmez (ya da kısa TTL / edge-side kişiselleştirme ile). CDN, çok katmanlı önbellekleme hiyerarşisinin (in-process → Redis → CDN) en dış halkasıdır.

# csd-6 | İleri
## Soru
"Bilgisayar bilimindeki iki zor şey: cache invalidation ve isimlendirme." DB güncellendiğinde cache'i geçersiz kılma (invalidation) neden zordur?
## A
Cache invalidation aslında kolaydır; anahtara TTL koymak her sorunu tümüyle çözer
🤖 TTL yardımcıdır ama TTL boyunca eski veri servis edilir; güçlü tutarlılıkta yetmez.
## B
DB ile cache güncellemesi atomik değildir; araya giren yarışlar eski veri bırakabilir
🤖 Doğru: iki ayrı sistemi tutarlı tutmak dağıtık bir tutarlılık problemidir.
## C
Cache hiçbir zaman eski veri tutmaz; her okuma zaten doğrudan veritabanına gider
🤖 O zaman cache'in anlamı kalmaz; cache veriyi bir süre kopyalar, staleness kaçınılmazdır.
## D
Sorun Redis'e özgüdür; `DEL` komutu atomik olmadığı için silmede yarış doğar
🤖 `DEL` zaten atomiktir; invalidation her önbellekte geçerli genel bir problemdir.
## Doğru
B
## Açıklama
Invalidation zordur çünkü cache ve DB **iki ayrı sistemdir** ve güncellemeleri **atomik değildir**: "DB'yi yaz, sonra cache'i sil" ile "cache'i sil, sonra DB'yi yaz" arasında yarış durumları (race conditions) vardır — araya giren bir okuma cache'i eski değerle yeniden doldurabilir ve bu değer TTL'e kadar yapışır. Dağıtık ortamda kısmi hatalar (cache silindi ama DB yazımı başarısız, veya tersi) de tutarsızlık bırakır. Yaklaşımlar: TTL ile nihai tutarlılığı kabul etmek; yazınca cache'i **güncellemek yerine silmek** (sonraki okuma taze doldurur); değişiklik akışını (CDC / binlog) dinleyip cache'i asenkron geçersiz kılmak. Hiçbiri bedelsiz değildir; bu yüzden invalidation "zor" olarak anılır.
