---
slug: redis-eviction-ttl
baslik: "Redis Eviction ve TTL: maxmemory Politikaları, LRU vs LFU, Süre Dolumu"
kademe: Orta
sira: 158
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Redis verisini RAM'de tutar. RAM sınırlıdır ve bir gün biter. Peki bellek dolduğunda ne olur?

Cevap, senin verdiğin iki karara bağlıdır. Birincisi **TTL**: bir anahtar ne kadar yaşasın? İkincisi **tahliye (eviction)**: bellek dolunca hangi anahtar atılsın? Bu ikisini bilinçli ayarlamayan ekipler iki sorundan biriyle karşılaşır: ya "yazamıyorum, OOM hatası alıyorum" ya da "belleğim durmadan şişiyor".

**TTL (time to live)** = anahtara verilen ömür. `SET oturum:42 "..." EX 1800` dersen anahtar 30 dakika sonra kendiliğinden silinir. Kritik kural: **TTL vermezsen anahtar kalıcıdır.** Redis'te varsayılan bir ömür yoktur.

**Tahliye (eviction)** = bellek tavanına çarpıldığında Redis'in yer açmak için anahtar atması. TTL'den bağımsız bir mekanizmadır: TTL "zamanı geldi", tahliye "yer kalmadı" der.

### Temel kavramlar

- **`maxmemory`**: Redis'in kullanabileceği bellek tavanı. 0 verilirse sınırsızdır.
- **`maxmemory-policy`**: tavana çarpınca ne yapılacağı — asıl karar budur.
- **LRU (Least Recently Used)**: "en son ne zaman kullanıldı?" — en uzun süredir dokunulmayanı at.
- **LFU (Least Frequently Used)**: "ne sıklıkla kullanılıyor?" — en az kullanılanı at.
- **volatile**: "TTL'i olan" (uçucu) anahtar demektir. **allkeys**: bütün anahtarlar.
- **Cache kirlenmesi (pollution)**: tek seferlik bir taramanın gerçek sıcak anahtarları önbellekten attırması.

### Nasıl çalışır?

**Politikalar** (`maxmemory-policy`):

- **`noeviction`** — **varsayılan politikadır.** Hiçbir anahtar atılmaz. Bellek gerektiren **yazma** komutları `OOM command not allowed when used memory > 'maxmemory'` hatasıyla reddedilir. Buna karşılık **okumalar çalışmaya devam eder.** Süreç çökmez ve Redis kendi başına diske taşıma (swap) yapmaz. Bu, Redis'i birincil veri deposu olarak kullananın isteyeceği güvenli varsayılandır. Ama Redis'i **önbellek** olarak kullanıyorsan yanlış politikadır; yazmaların durur.
- **`allkeys-lru` / `allkeys-lfu`** — **tüm** anahtarlar aday; LRU ya da LFU kuralıyla biri atılır. Önbellek için tipik seçim.
- **`volatile-lru` / `volatile-lfu` / `volatile-ttl` / `volatile-random`** — yalnız **TTL'i olan** anahtarlar aday; TTL'siz anahtara dokunulmaz. Tuzak: anahtarların çoğuna TTL vermezsen atacak aday kalmaz ve yazmalar yine OOM verir.
- **`allkeys-random`** — rastgele atar; ucuz ama kördür.

Politika adındaki önek **aday havuzunu** (`allkeys` mi `volatile` mi), son ek ise **seçim kuralını** (`lru`/`lfu`/`ttl`/`random`) belirler.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="maxmemory dolunca eviction politikası devreye girer">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">maxmemory dolunca ne olur?</text>
  <rect x="40" y="46" width="180" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="130" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">noeviction</text>
  <text x="130" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yazma → OOM hata</text>
  <text x="130" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">okuma → çalışır</text>
  <text x="130" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veri asla atılmaz</text>
  <text x="130" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(varsayılan)</text>
  <rect x="230" y="46" width="180" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="320" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">allkeys-lru / lfu</text>
  <text x="320" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tüm key'lerden atar</text>
  <text x="320" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">LRU: en son kullanım</text>
  <text x="320" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">LFU: kullanım sıklığı</text>
  <text x="320" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yaklaşık (örnekleme)</text>
  <rect x="420" y="46" width="180" height="140" rx="10" fill="#FBEDD9" stroke="#B26B0E" stroke-width="1.4"/>
  <text x="510" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B0E" font-family="system-ui">volatile-*</text>
  <text x="510" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yalnız TTL'li</text>
  <text x="510" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">key'lerden atar</text>
  <text x="510" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">TTL'siz key dokunulmaz</text>
</svg>
```

**LRU mu LFU mu?** İkisi farklı soru sorar. LRU "en son ne zaman kullanıldı?" diye sorar (recency). LFU "ne sıklıkla kullanılıyor?" diye sorar (frequency).

Fark, tek seferlik büyük bir taramada ortaya çıkar. Gece çalışan bir rapor işi milyonlarca anahtara birer kez dokunsun. LRU'ya göre bu anahtarlar "en taze" olur ve gerçek sıcak anahtarları attırır; buna **önbellek kirlenmesi** denir. LFU ise erişim sayısını izler — yaklaşık bir sayaçla; `lfu-log-factor` ve `lfu-decay-time` ile ayarlanır. Bu yüzden bir kez dokunulan anahtarı düşük frekanslı görür ve nadiren ama **düzenli** okunan sıcak anahtarı korur.

Özetle: erişim frekansı çarpık (skewed) olan yüklerde LFU, düzgün recency yüklerinde LRU uygundur. LFU'nun ne yazma hızıyla ne de TTL ile ilgisi vardır; yalnız bir tahliye seçim kuralıdır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="LRU son kullanıma LFU sıklığa bakar, tek seferlik tarama LRUyu kirletir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">LRU (recency) vs LFU (frequency)</text>
  <rect x="34" y="46" width="270" height="146" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">LRU: en son ne zaman?</text>
  <text x="169" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sıcak key nadiren ama düzenli okunur</text>
  <text x="169" y="118" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">tek-seferlik tarama gelir:</text>
  <text x="169" y="136" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">yeni ama değersiz key'ler "en yeni" olur</text>
  <rect x="54" y="150" width="230" height="30" rx="6" fill="#F6E4E4" stroke="#8A2E2E" stroke-width="1.3"/>
  <text x="169" y="170" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">sıcak key atılır (cache pollution) ⚠</text>
  <rect x="336" y="46" width="270" height="146" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">LFU: ne sıklıkla?</text>
  <text x="471" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">erişim sayacı tutar (yaklaşık)</text>
  <text x="471" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek-seferlik tarama düşük frekanslı kalır</text>
  <text x="471" y="136" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">düzenli sıcak key yüksek frekanslı</text>
  <rect x="356" y="150" width="230" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="471" y="170" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">taranan key atılır, sıcak key korunur</text>
</svg>
```

**Yaklaşık LRU.** Redis **tam** LRU uygulamaz. Tam LRU, her erişimde güncellenen tam sıralı bir yapı tutmayı gerektirir; yani tüm anahtarlar için bir bağlı liste. Milyonlarca anahtarda bunun bellek ve CPU maliyeti ciddidir.

Redis bunun yerine **örnekleme (sampling)** yapar. Tahliye anında rastgele **`maxmemory-samples`** kadar anahtar seçer (varsayılan 5) ve aralarından kuralına en uygun olanı atar. Sayıyı artırmak — örneğin 10 yapmak — sonucu tam LRU'ya yaklaştırır ama CPU kullanımını artırır. Bu **rastgele atmak değildir**: seçimini kullanım bilgisine göre yapar, yalnızca tüm evreni değil bir örneği görür. Aynı örnekleme LFU için de geçerlidir ve tekil node'da da Cluster'da da böyle çalışır. "Mükemmel değil ama pratikte yeterince iyi ve çok ucuz" tasarım tercihidir.

**TTL süre dolumu iki yolla yürür:**

- **Pasif (lazy)**: anahtara erişildiğinde süresi dolmuşsa o an silinir. Tek başına yetmez — hiç erişilmeyen anahtar bellekte durmaya devam eder.
- **Aktif**: arka planda periyodik olarak TTL'li anahtarlardan rastgele örnek alınır, süresi dolmuş olanlar temizlenir; örnekte dolmuşların oranı yüksekse tur tekrarlanır.

İkisi de yalnız **TTL verilmiş** anahtarlar için çalışır. Bu yüzden TTL vermeden önbellek anahtarı yazmanın klasik sonucu **anahtar sızıntısıdır**: eski anahtarlar birikir ve bellek dolar. Politika `noeviction` ise yazmalar OOM hatasıyla durur. `allkeys-lru` ya da `allkeys-lfu` ise sürekli tahliye baskısı ve gecikmede dalgalanma yaşanır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="TTL dolumu pasif erişimde ve aktif arka plan örneklemesiyle temizlenir">
  <defs>
    <marker id="revict-ttl-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">TTL süre dolumu: pasif + aktif</text>
  <rect x="34" y="46" width="270" height="130" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="169" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Pasif (lazy)</text>
  <text x="169" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">key'e erişilince kontrol edilir</text>
  <text x="169" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">süresi dolmuşsa o an silinir</text>
  <text x="169" y="144" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">hiç erişilmezse silinmeden kalır</text>
  <text x="169" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ tek başına yetmez</text>
  <rect x="336" y="46" width="270" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Aktif (arka plan)</text>
  <text x="471" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">periyodik olarak TTL'li key'lerden</text>
  <text x="471" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">rastgele örnek alır</text>
  <text x="471" y="140" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">dolmuşları temizler, erişim beklemez</text>
  <line x1="304" y1="150" x2="336" y2="150" stroke="#6B3FA0" stroke-width="1.4" marker-end="url(#revict-ttl-ok)"/>
  <text x="320" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">İkisi de yalnız TTL VERİLMİŞ key'ler için çalışır</text>
</svg>
```

Önbellek olarak Redis'in tipik reçetesi: bir `maxmemory` tavanı koy, `allkeys-lru` ya da `allkeys-lfu` seç ve her anahtara mantıklı bir TTL ver.

### Mülakatta ne sorulur?

- "Bellek dolunca ne olur?" → `maxmemory-policy`'ye bağlıdır; varsayılan `noeviction` yazmayı reddeder, okuma sürer.
- "`allkeys-lru` ile `volatile-lru` farkı?" → aday havuzu: tüm anahtarlar mı, yalnız TTL'liler mi.
- "LFU ne zaman?" → tek seferlik taramaların sıcak anahtarları süpürmesini istemediğinde.
- "Neden yaklaşık LRU?" → tam LRU'nun bellek ve CPU maliyetinden kaçınmak; `maxmemory-samples` ile örnekleme.
- "TTL'ler nasıl siliniyor?" → pasif (erişim anında) ve aktif (arka planda örnekleme) birlikte.
- "TTL vermezsem?" → anahtar kalıcıdır; sızıntı, bellek şişmesi ve OOM.

### Tek cümlede

`maxmemory` bir bellek tavanı koyar. `maxmemory-policy` tavana çarpınca kimin gideceğini söyler. TTL ise her anahtara kendi son kullanma tarihini verir. Önbellek için doğru reçete, bu üçünü birden bilinçli ayarlamaktır.

# revict-1 | Temel
## Soru
`maxmemory` sınırı dolduğunda Redis'in ne yapacağını belirleyen ayar hangisidir?
## A
`maxmemory-policy`
🤖 Doğru: sınır dolunca hangi anahtarların atılacağını (ya da hata döneceğini) bu belirler.
## B
`appendonly`
🤖 AOF kalıcılık ayarıdır; bellek dolunca tahliye davranışıyla ilgisi yoktur.
## C
`timeout`
🤖 Boşta bekleyen bağlantının kapatılma süresidir; tahliye politikasını belirlemez.
## D
`databases`
🤖 Mantıksal veritabanı sayısıdır (0-15); tahliye kararıyla alakası yoktur.
## Doğru
A
## Açıklama
`maxmemory` bir bellek tavanı koyar; **`maxmemory-policy`** ise tavana ulaşılınca ne olacağını belirler: `noeviction` (yazmalar hata döner), `allkeys-lru/lfu`, `volatile-*` vb. `appendonly` (B) AOF kalıcılığını, `timeout` (C) boşta bağlantı süresini, `databases` (D) mantıksal veritabanı sayısını ayarlar — hiçbiri tahliye kararını yönetmez. Önbellek kullanımında bu ikili (`maxmemory` + `maxmemory-policy`) mutlaka bilinçli ayarlanmalıdır; aksi hâlde varsayılan `noeviction` ile bellek dolunca yazmalar reddedilir.

# revict-2 | Temel
## Soru
Varsayılan `noeviction` politikasında bellek dolduğunda yazma komutları ne yapar?
## A
Bellekteki en eski anahtarı silip yazma işlemini normal biçimde tamamlar
🤖 Bu `allkeys-lru` davranışıdır; `noeviction` hiçbir anahtar silmez.
## B
Hata döner (OOM command not allowed); yazmalar reddedilir, okumalar sürer
🤖 Doğru: `noeviction` veri atmaz, yalnız yeni yazmayı reddeder.
## C
Redis süreci bellek yetersizliğiyle çöker ve otomatik olarak yeniden başlar
🤖 Çökmez; yalnız yazma komutlarına hata döndürüp çalışmayı sürdürür.
## D
Veriyi diske taşıyıp (swap) bellekte yer açar ve yazmaya devam eder
🤖 Redis kendi başına diske taşıma yapmaz; `noeviction`'da yazma reddedilir.
## Doğru
B
## Açıklama
**`noeviction`** politikası hiçbir anahtar atmaz. Bellek dolunca bellek gerektiren **yazma** komutları `OOM command not allowed when used memory > 'maxmemory'` hatasıyla reddedilir; **okuma** ve bellek gerektirmeyen komutlar çalışmaya devam eder. Süreç çökmez (C), otomatik disk swap yapmaz (D), anahtar silmez (A — o `allkeys-lru` olurdu). Bu, Redis'i **birincil veri deposu** olarak kullananların istediği güvenli varsayılandır; ama Redis'i **önbellek** olarak kullanıyorsan bu politika yanlıştır — yazmaların durur, `allkeys-lru`/`allkeys-lfu` seçmelisin.

# revict-3 | Orta
## Soru
`allkeys-lru` ile `volatile-lru` arasındaki fark nedir?
## A
İkisi tamamen aynıdır; yalnız eski ve yeni isimlendirmeyi temsil ederler
🤖 Aday havuzları farklıdır: biri tüm anahtarlar, biri yalnız TTL'liler.
## B
`volatile-lru` tüm anahtarları aday sayar, `allkeys-lru` ise yalnız TTL'lileri
🤖 Tam tersi; "volatile" TTL'li anahtar demektir ve havuzu daha dardır.
## C
`allkeys-lru` tüm anahtarlar arasından, `volatile-lru` yalnız TTL'liler arasından atar
🤖 Doğru: "allkeys" tümü, "volatile" yalnız süresi atanmış olanlar demektir.
## D
`allkeys-lru` yalnız TTL'li anahtarları atar, `volatile-lru` ise TTL'siz olanları
🤖 İsimlendirme tersine anlaşılmış; allkeys tümü, volatile TTL'lilerdir.
## Doğru
C
## Açıklama
Politikadaki önek **aday havuzunu** belirler: **`allkeys-*`** tüm anahtarları atmaya aday sayar; **`volatile-*`** yalnız **TTL'i olan** (expire atanmış) anahtarları aday sayar ve TTL'siz anahtarlara hiç dokunmaz. İkisinde de `-lru` son eki, aday havuzundan **en uzun süredir dokunulmayanı** seçme kuralıdır. Dikkat çeken tuzak: `volatile-*` seçip anahtarların çoğuna TTL vermezsen atacak aday bulunamaz ve yazmalar yine OOM hatası verebilir. Önbellek senaryosunda genelde `allkeys-lru`/`allkeys-lfu` daha güvenlidir; `volatile-*` ise "bazı anahtarlar kalıcı, bazıları geçici" karışık kullanımda anlamlıdır.

# revict-4 | Orta
## Soru
LRU yerine LFU (`allkeys-lfu`) ne zaman daha iyi bir seçimdir?
## A
Asla; LRU her iş yükünde LFU'dan üstündür, LFU yalnız geriye dönük uyumluluk içindir
🤖 Hayır; erişim sıklığı çarpık yüklerde LFU sıcak anahtarları daha iyi korur.
## B
Yazma ağırlıklı yüklerde, çünkü LFU yazma komutlarını hızlandıran bir kısayol kullanır
🤖 LFU bir tahliye seçim kuralıdır; yazma hızıyla hiçbir ilgisi yoktur.
## C
Yalnız TTL hiç kullanılmadığında, çünkü LFU çalışabilmek için TTL'siz anahtar ister
🤖 LFU TTL gerektirmez; tahliye politikası ile TTL birbirinden bağımsız kavramlardır.
## D
Tek seferlik büyük taramaların sıcak anahtarları süpürmesini istemediğin yüklerde
🤖 Doğru: LFU frekans saydığı için bir kez dokunulan anahtarı düşük değerli görür.
## Doğru
D
## Açıklama
**LRU** yalnız "en son ne zaman erişildi" (recency) sorusunu sorar; bu yüzden tek seferlik büyük bir **tarama**, yeni ama değersiz anahtarları "en taze" yapıp gerçek sıcak anahtarları attırabilir — buna **cache kirlenmesi (pollution)** denir. **LFU** ise "ne sıklıkla erişildi" (frequency) sayar (yaklaşık bir sayaçla; `lfu-log-factor`/`lfu-decay-time` ile ayarlanır) ve nadiren ama **düzenli** kullanılan sıcak anahtarları, bir kez taranan anahtarlara tercih eder. LFU yazma hızıyla (B) veya TTL ile (C) ilgili değildir ve LRU her koşulda üstün değildir (A). Genel öneri: erişim frekansı çarpık (skewed) yüklerde LFU, düzgün recency yüklerinde LRU.

# revict-5 | İleri
## Soru
Redis neden **tam** LRU yerine "yaklaşık LRU" (approximate LRU) kullanır?
## A
Tam LRU tüm anahtarlar için sıralı yapı tutmayı gerektirir; Redis örnekleme yapar
🤖 Doğru: örnekleme, tam sıralı liste tutmanın bellek ve CPU maliyetinden kaçınır.
## B
Redis LRU kavramını hiç desteklemez; "yaklaşık" sözcüğü yalnız pazarlama amaçlıdır
🤖 Destekler; "yaklaşık" olan uygulama biçimidir, kavramın kendisi değildir.
## C
Yaklaşık LRU aslında rastgele atar ve hiçbir kullanım bilgisi tutmaz
🤖 Rastgele değildir; örneklenen anahtarlar arasından en eski erişimliyi seçer.
## D
Yaklaşık LRU yalnız Redis Cluster modunda çalışır; tekil node'da tam LRU vardır
🤖 Cluster ile ilgisi yoktur; tekil node'da da yaklaşık LRU kullanılır.
## Doğru
A
## Açıklama
Tam LRU, her erişimde güncellenen **tam sıralı bir yapı** (ör. tüm anahtarlar için bağlı liste) tutmayı gerektirir; bu, milyonlarca anahtarda ciddi bellek ve CPU maliyetidir. Redis bunun yerine **örnekleme (sampling)** yapar: tahliye anında rastgele **`maxmemory-samples`** (varsayılan 5) anahtar alır ve aralarından en uzun süredir dokunulmayanı atar. Sayıyı artırmak (ör. 10) tam LRU'ya yaklaştırır ama CPU'yu artırır. Bu rastgele atma değildir (C) — kullanım bilgisiyle seçer; Cluster'a özel de değildir (D), tekil node'da da böyle çalışır. Aynı örnekleme LFU için de geçerlidir.

# revict-6 | İleri
## Soru
Önbellek anahtarlarını TTL vermeden yazmanın klasik sonucu nedir?
## A
Redis bu anahtarları varsayılan 24 saatlik ömür sonunda otomatik olarak siler
🤖 Varsayılan TTL yoktur; açıkça verilmeyen anahtar kalıcıdır, kendiliğinden silinmez.
## B
Anahtarlar kalıcı birikir; bellek dolar ve `noeviction` ise yazmalar OOM verir
🤖 Doğru: TTL'siz önbellek anahtarları temizlenmez, zamanla belleği tüketir.
## C
Performans artar, çünkü Redis süre dolumu kontrolü yapmak zorunda kalmaz
🤖 Mikro kazanç birikmenin yanında anlamsızdır; asıl sonuç bellek şişmesidir.
## D
Redis TTL'siz yazmayı reddeder; her önbellek anahtarına TTL vermek zorunludur
🤖 TTL zorunlu değildir; TTL'siz yazma kabul edilir, sorun da tam budur.
## Doğru
B
## Açıklama
Redis'te bir anahtara **açıkça TTL verilmezse kalıcıdır** ve kendiliğinden silinmez (A, D yanlış). Önbellek olarak kullanırken TTL vermeyi unutmak **anahtar sızıntısına** yol açar: eski ve kullanılmayan anahtarlar birikir, bellek dolar. Politika `noeviction` ise yazmalar OOM hatasıyla durur; `allkeys-lru/lfu` ise sürekli tahliye baskısı ve gecikme dalgalanması olur. Süre dolumu iki mekanizmayla yürür — **pasif** (anahtara erişildiğinde dolmuşsa silinir) ve **aktif** (arka planda periyodik TTL örneklemesi) — ama ikisi de yalnız TTL **verilmiş** anahtarlar için çalışır. Kural: her önbellek anahtarına mantıklı bir TTL ata.
