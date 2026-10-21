---
slug: spring-data-redis-cacheable
baslik: "Spring Data Redis: @Cacheable, RedisCacheManager, Lettuce ve Stampede"
kademe: Orta
sira: 165
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

**Önbellek (cache)**, pahalı bir işin sonucunu (veritabanı sorgusu, uzak servis çağrısı) bir kenara yazıp bir dahaki sefere işi tekrar yapmadan oradan okumaktır. Spring bunu tek tek Redis komutu yazdırmadan, **anotasyonlarla** yapmanı sağlar: metodun üstüne bir etiket koyarsın, gerisini çatı halleder.

### Temel kavramlar

- **`RedisTemplate`**: Redis komutlarına doğrudan erişim sağlar. Buradaki kritik seçim **serileştiricidir (serializer)**: nesnenin baytlara nasıl çevrileceği. Varsayılan olan **JDK serileştirmesi** insanın okuyamayacağı ikili bayt üretir, sınıftan `Serializable` ister ve veriyi **Java sınıf yapısına sıkıca bağlar**; sınıf değişince eski veri okunamayabilir. **JSON serileştirici** (`GenericJackson2JsonRedisSerializer`) ise `redis-cli` ile okunabilen değer üretir. Bu değer **diller arası taşınabilirdir** ve şema değişimine toleranslıdır. Boyut avantajı garanti değildir; JDK de teknik olarak çalışır, hatta varsayılandır.
- **`@Cacheable`**: Metot ilk çağrıldığında sonucu önbelleğe yazar; aynı **anahtarla** sonraki çağrılarda metodu **hiç çalıştırmadan** önbellekteki değeri döndürür. Anahtar varsayılan olarak metot parametrelerinden üretilir, `key = "#id"` gibi bir ifadeyle özelleştirilebilir.
- **`@CacheEvict`**: İlgili önbellek girdisini **siler**. Genelde güncelleme/silme metoduna konur ki bir sonraki okuma taze veriyi yeniden yüklesin — önbellek geçersizleştirmenin (cache invalidation) temel aracıdır.
- **`@CachePut`**: Metodu **her zaman çalıştırır** ve dönen sonucu önbelleğe yazar/günceller.
- **TTL (Time To Live — yaşam süresi)**: Girdinin ne kadar sonra kendiliğinden silineceğidir. Spring'de TTL **anotasyonda değil**, `RedisCacheManager` yapılandırmasında verilir. `RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))` bir varsayılan kurar. `withCacheConfiguration("urunler", ...)` ile **önbellek adı bazında** ayrı TTL'ler tanımlanır. `@Cacheable`'ın `ttl` özniteliği **yoktur**.
- **İstemci**: Spring Boot'un varsayılanı **Lettuce**'tır. Netty tabanlıdır, eşzamansız çalışır ve bağlantısı **iş parçacığı güvenlidir**; bu yüzden tek bir paylaşılan bağlantı çok sayıda iş parçacığına yetebilir. **Jedis** ise tıkayan (blocking) bir istemcidir ve bağlantısı iş parçacığı güvenli değildir; bu yüzden tipik olarak bir **bağlantı havuzu** ile kullanılır. İkisi de tek düğüm dahil her modda çalışır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Spring Cache soyutlaması ve Redis serializer/istemci">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Spring Cache → Redis</text>
  <rect x="24" y="44" width="188" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="118" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Anotasyonlar</text>
  <text x="118" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">@Cacheable → oku/atla</text>
  <text x="118" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">@CacheEvict → sil</text>
  <text x="118" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">@CachePut → güncelle</text>
  <text x="118" y="160" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sync=true → stampede</text>
  <rect x="224" y="44" width="188" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="318" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Serializer</text>
  <text x="318" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">JDK (varsayılan)</text>
  <text x="318" y="112" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">okunmaz, sınıfa bağlı</text>
  <text x="318" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">JSON (Jackson)</text>
  <text x="318" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">okunur, taşınabilir</text>
  <rect x="424" y="44" width="192" height="140" rx="12" fill="#F4EEF9" stroke="#6A3FA0" stroke-width="1.5"/>
  <text x="520" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#6A3FA0" font-family="system-ui">İstemci</text>
  <text x="520" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Lettuce (varsayılan)</text>
  <text x="520" y="112" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Netty, asenkron, TS</text>
  <text x="520" y="134" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Jedis</text>
  <text x="520" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bloklamalı, havuz</text>
</svg>
```

### Nasıl çalışır?

`@Cacheable`'ın çalışma mantığı bir **isabet/ıska (hit/miss)** dallanmasıdır. İstek geldiğinde önce önbelleğe bakılır. İsabet varsa metot hiç çağrılmadan önbellekteki değer döner. Iska varsa metot çalışır, sonucu önbelleğe yazılır ve çağırana döndürülür:

```svg
<svg viewBox="0 0 640 176" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cacheable cache hit miss akışı">
  <defs>
    <marker id="rspring-flow" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">@Cacheable: cache hit / miss akışı</text>
  <rect x="14" y="66" width="94" height="44" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="61" y="93" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">istek</text>
  <rect x="132" y="60" width="130" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="197" y="84" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">önbellekte</text>
  <text x="197" y="100" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">var mı?</text>
  <line x1="108" y1="88" x2="130" y2="88" stroke="#5D7789" stroke-width="2" marker-end="url(#rspring-flow)"/>
  <rect x="300" y="34" width="326" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="463" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">HIT → önbellekten döndür</text>
  <text x="463" y="68" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metot HİÇ çalışmaz</text>
  <rect x="300" y="98" width="326" height="52" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="463" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">MISS → metodu çalıştır</text>
  <text x="463" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonucu önbelleğe yaz → döndür</text>
  <line x1="262" y1="76" x2="298" y2="60" stroke="#5D7789" stroke-width="2" marker-end="url(#rspring-flow)"/>
  <text x="284" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">hit</text>
  <line x1="262" y1="100" x2="298" y2="118" stroke="#5D7789" stroke-width="2" marker-end="url(#rspring-flow)"/>
  <text x="284" y="118" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">miss</text>
</svg>
```

Üretimde iki tuzak vardır.

Birincisi **önbellek delinmesidir (cache penetration)**. Veritabanında **karşılığı olmayan** anahtarlar önbelleğe hiç yazılmaz. Bu yüzden o anahtarla gelen her istek önbelleği atlayıp doğrudan veritabanına vurur. Rastgele ya da kötü niyetli anahtarlarla veritabanı ezilir. Çözüm **yokluğu da önbelleklemektir (null caching)**. Spring, `RedisCacheConfiguration` ile null değerlerin önbelleklenmesine izin verir. Bu "kayıt yok" girdilerine **kısa bir TTL** verilir ki veri sonradan eklenirse önbellek hızla tazelensin. Sonsuz TTL, her okumada `@CacheEvict` çağırmak ya da serileştirici/anahtar değiştirmek bu sorunu çözmez.

İkincisi **önbellek izdihamıdır (cache stampede / thundering herd)**. Sıcak bir anahtarın TTL'i dolduğu an yüzlerce istek aynı anda ıska alır ve pahalı metodu hep birlikte çalıştırır. `@Cacheable(sync = true)` bunu tek anahtar için **tek iş parçacığına** indirir; kalanlar sonucu bekleyip paylaşır. Diğer seçenekler işe yaramaz: TTL'i 0 yapmak önbelleği etkisizleştirir, `unless` yalnız yazma koşuludur, `@CachePut` ise metodu her çağrıda çalıştırır:

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cache stampede sync true olmadan ve olduğunda">
  <defs>
    <marker id="rspring-herd" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Cache stampede: sync=true etkisi (TTL doldu)</text>
  <rect x="14" y="36" width="300" height="164" rx="12" fill="#FBEDE9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="164" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">sync yok</text>
  <text x="52" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek 1</text>
  <text x="52" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek 2</text>
  <text x="52" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek 3</text>
  <text x="52" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek 4</text>
  <rect x="212" y="88" width="84" height="66" rx="9" fill="#FFFFFF" stroke="#8A2E2E" stroke-width="1.4"/>
  <text x="254" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">DB</text>
  <text x="254" y="136" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">4 çağrı</text>
  <line x1="80" y1="88" x2="210" y2="100" stroke="#5D7789" stroke-width="1.6" marker-end="url(#rspring-herd)"/>
  <line x1="80" y1="108" x2="210" y2="112" stroke="#5D7789" stroke-width="1.6" marker-end="url(#rspring-herd)"/>
  <line x1="80" y1="128" x2="210" y2="128" stroke="#5D7789" stroke-width="1.6" marker-end="url(#rspring-herd)"/>
  <line x1="80" y1="148" x2="210" y2="142" stroke="#5D7789" stroke-width="1.6" marker-end="url(#rspring-herd)"/>
  <text x="164" y="188" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">hepsi DB'ye → sürüyü ezme</text>
  <rect x="326" y="36" width="300" height="164" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">sync = true</text>
  <text x="364" y="92" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">istek 1 (hesaplar)</text>
  <text x="364" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istek 2 (bekler)</text>
  <text x="364" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istek 3 (bekler)</text>
  <text x="364" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istek 4 (bekler)</text>
  <rect x="524" y="96" width="84" height="66" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="566" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">DB</text>
  <text x="566" y="144" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1 çağrı</text>
  <line x1="452" y1="92" x2="522" y2="120" stroke="#5D7789" stroke-width="1.8" marker-end="url(#rspring-herd)"/>
  <text x="476" y="188" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">tek thread hesaplar, diğerleri sonucu paylaşır</text>
</svg>
```

### Mülakatta ne sorulur?

(1) "Neden JSON serileştirici?" — Okunabilirlik, diller arası taşınabilirlik ve sınıf bağımlılığının azalması için. "Her zaman daha küçük" olduğu için değil.

(2) "`@Cacheable` ile `@CacheEvict` farkı nedir?" — Biri sonucu önbelleğe yazıp çağrıyı atlatır; diğeri girdiyi geçersiz kılar.

(3) "TTL nereye yazılır?" — `entryTtl` ile önbellek yöneticisinin yapılandırmasına. Hem genel varsayılan hem önbellek adı bazlı olabilir.

(4) "Lettuce mı Jedis mi?" — Lettuce eşzamansız ve iş parçacığı güvenlidir; Jedis tıkayan bir istemcidir ve havuzla kullanılır.

(5) Delinme ile izdiham senaryoları — Sırasıyla null önbellekleme ve `sync = true`. Not: `sync = true` yalnız **tek uygulama örneği içinde** etkilidir; çok örnekli dağıtımda dağıtık kilit gerekebilir.

### Tek cümlede

Spring'de Redis önbelleği anotasyonlarla kurulur: `@Cacheable` okur ve çağrıyı atlatır, `@CacheEvict` siler, `@CachePut` günceller. TTL ise `entryTtl` ile yapılandırmadan verilir. Serileştirici seçimi okunabilirliği belirler; `sync = true` ise izdihama dayanıklılığı belirler.

# rspring-1 | Orta
## Soru
`RedisTemplate`'te varsayılan JDK serileştirme yerine JSON serileştirici (ör. `GenericJackson2JsonRedisSerializer`) tercih etmenin tipik nedeni nedir?
## A
JDK serileştirme zaten insan-okunur çıktı ürettiğinden JSON'a hiç gerek kalmaz
🤖 Tersine; JDK çıktısı okunmaz bir bayt dizisidir, okunabilir olan JSON'dur.
## B
JSON okunabilir ve diller-arası taşınabilirdir, Java sınıf yapısına daha az bağımlıdır
🤖 Doğru: okunabilirlik, taşınabilirlik ve sınıf bağımlılığının azalması JSON'un avantajıdır.
## C
JSON her durumda JDK'den daha küçük bayt çıktısı ürettiği için yer kazandırır
🤖 Boyut her zaman küçük olmaz; asıl neden okunabilirlik ve taşınabilirliktir.
## D
JDK serileştirme Redis'te teknik olarak çalışmadığından JSON kullanmak zorunludur
🤖 JDK serileştirme Redis'te çalışır, hatta varsayılandır; tercih nedeni işlevsizlik değildir.
## Doğru
B
## Açıklama
Varsayılan **JDK serileştirme** (`JdkSerializationRedisSerializer`) insan-**okunmaz** ikili bayt üretir, `Serializable` gerektirir ve saklanan verinin **Java sınıf yapısına sıkıca bağlı** olmasına yol açar (sınıf değişince eski veriler okunamayabilir). **JSON serializer** ise okunabilir, `redis-cli` ile incelenebilir, **diller-arası** (başka servisler/diller) taşınabilir ve şema evrimine daha toleranslı değer üretir (B). Boyut avantajı garanti değildir (C yanlış); JDK Redis'te pekâlâ çalışır, hatta varsayılandır (D yanlış); "daha okunur" olan JSON'dur, JDK değil (A yanlış).

# rspring-2 | Orta
## Soru
Spring Cache soyutlamasında `@Cacheable` ile `@CacheEvict`'in rolleri nedir?
## A
İkisi de metodu çalıştırıp dönen sonucu aynı anahtarla önbelleğe yazar
🤖 `@CacheEvict` önbelleğe yazmaz, girdiyi siler; ikisi aynı işi yapmaz.
## B
`@Cacheable` önbellekten girdi siler, `@CacheEvict` metot sonucunu önbelleğe alır
🤖 Roller ters verilmiş; `@Cacheable` sonucu önbelleğe alır, `@CacheEvict` siler.
## C
`@CacheEvict` metot sonucunu okuyup döndürür, `@Cacheable` ise önbelleği temizler
🤖 Yine ters; okuyup önbelleğe alan `@Cacheable`, silen `@CacheEvict`'tir.
## D
`@Cacheable` sonucu önbelleğe alıp çağrıyı atlatır, `@CacheEvict` girdiyi siler
🤖 Doğru: biri sonucu önbellekleyip çağrıyı atlatır, diğeri girdiyi geçersizleştirir.
## Doğru
D
## Açıklama
`@Cacheable`: metod ilk çağrıldığında sonucu **önbelleğe yazar**; aynı anahtarla sonraki çağrılarda metodu **çalıştırmadan** önbellekteki değeri döndürür (D). `@CacheEvict`: genellikle bir güncelleme/silme metoduna konur ve ilgili önbellek **girdisini siler**, böylece bir sonraki okuma taze veriyi yeniden yükler — bu, önbellek ile kaynak arasındaki tutarlılığın (cache invalidation) temel aracıdır. (`@CachePut` ise metodu çalıştırır ve sonucu önbelleğe **yazar/günceller**.) A, B ve C rolleri karıştırır.

# rspring-3 | Orta
## Soru
Spring'de Redis önbelleğine **cache-başına TTL** (yaşam süresi) nasıl verilir?
## A
`@Cacheable(ttl = 60)` biçiminde doğrudan anotasyon özniteliği üzerinden verilir
🤖 `@Cacheable`'ın `ttl` özniteliği yoktur; TTL cache manager yapılandırmasında ayarlanır.
## B
TTL yalnız tek bir global değer olabilir; cache adı bazında ayrı ayrı verilemez
🤖 Aksine; `withCacheConfiguration` ile her cache adına ayrı TTL verilebilir.
## C
`RedisCacheManager` yapılandırmasında `RedisCacheConfiguration.entryTtl(...)` ile ayarlanır
🤖 Doğru: `entryTtl` ile hem varsayılan hem cache-başına TTL tanımlanır.
## D
Redis önbelleğinde TTL desteklenmez; girdiler elle silinene dek sonsuza kadar kalır
🤖 Redis TTL'i yerel olarak destekler; Spring de bunu `entryTtl` ile yönetir.
## Doğru
C
## Açıklama
Spring'de önbellek TTL'i **anotasyonda değil**, `RedisCacheManager` **yapılandırmasında** ayarlanır: `RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))` ile bir **varsayılan** verilir, `withCacheConfiguration("urunler", ...)` gibi çağrılarla da **cache-adı bazında** farklı TTL'ler tanımlanabilir (C). `@Cacheable`'ın `ttl` özniteliği yoktur (A yanlış); TTL global tek değere mahkûm değildir, cache-başına ayarlanabilir (B yanlış); Redis TTL'i doğal olarak destekler (D yanlış).

# rspring-4 | Orta
## Soru
Spring Boot'ta varsayılan Redis istemcisi **Lettuce**'ın **Jedis**'e göre öne çıkan yanı nedir?
## A
Netty tabanlı, asenkron ve thread-safe'tir; tek paylaşılan bağlantı çok thread'e yeter
🤖 Doğru: Lettuce'ın asenkron ve thread-safe modeli, Jedis'in bloklamalı modelinden ayrılır.
## B
Jedis asenkron ve thread-safe iken Lettuce bloklamalı çalışıp bağlantı havuzu gerektirir
🤖 Ters verilmiş; asenkron ve thread-safe olan Lettuce, bloklamalı olan Jedis'tir.
## C
İkisi de teknik olarak birebir aynı mimaridir; aralarında yalnız isim farkı vardır
🤖 Mimarileri farklıdır (Netty asenkron ile bloklamalı+havuz); eşdeğer değildirler.
## D
Lettuce yalnızca Redis Cluster modunda çalışır, tek node kurulumda kullanılamaz
🤖 Lettuce tek node dahil her modda çalışır; Cluster'a özgü değildir.
## Doğru
A
## Açıklama
**Lettuce** Netty üzerine kuruludur: **asenkron/reaktif**tir ve bağlantısı **thread-safe**'tir; bu yüzden çoğu senaryoda **tek paylaşılan bağlantı** çok sayıda thread'e hizmet verebilir, ağır bir havuz şart değildir (A). **Jedis** ise **bloklamalı**dır ve bağlantısı thread-safe olmadığından tipik olarak bir **bağlantı havuzu** ile kullanılır. Spring Boot varsayılanı Lettuce'tır. B yönleri ters çevirir; C iki farklı mimariyi eşitler; D Lettuce'ı yanlışça yalnız Cluster'a bağlar.

# rspring-5 | Orta
## Soru
Sık sorulan ama veritabanında **var olmayan** bir anahtar için önbellek sürekli baypas edilip her istek DB'ye vuruyor (cache penetration). Spring'de pratik bir önlem nedir?
## A
İlgili cache'in TTL'ini sonsuza ayarlayıp girdilerin hiç silinmemesini sağlamak
🤖 Süresiz TTL var olan girdileri tutar; hiç yazılmayan "kayıt yok" durumunu çözmez.
## B
`null` sonuçları da kısa yaşam süreli birer girdi olarak önbelleğe yazmak
🤖 Doğru: yokluğu da önbelleklemek her isteğin DB'ye gitmesini keser.
## C
Her okuma metoduna `@CacheEvict` ekleyip önbelleği sürekli tazelemeye zorlamak
🤖 `@CacheEvict` girdileri siler; penetration'ı azaltmaz, tersine önbelleği boşaltır.
## D
Önbellek anahtarını hash'leyip daha kısa bir anahtar alanına indirgemek
🤖 Anahtarı kısaltmak isabet oranını değiştirmez; sorun yokluğun hiç önbelleklenmemesidir.
## Doğru
B
## Açıklama
**Cache penetration**: sonucu **var olmayan** (DB'de karşılığı olmayan) anahtarlar önbelleğe hiç yazılmadığından her istek önbelleği baypas edip **DB'ye** vurur — kötü niyetli veya rastgele anahtarlarla DB ezilir. Pratik önlem **null/boş sonucu da önbelleklemek**tir: Spring'de `RedisCacheConfiguration` null değer önbelleklemeye izin verir (Spring varsayılanı boş değeri sarmalayarak önbellekler); genelde bu "yok" kayıtlarına **kısa bir TTL** verilir ki veri sonradan eklenince önbellek kısa sürede tazelensin (B). Sonsuz TTL (A) yalnız var olan girdileri tutar; her okumada `@CacheEvict` (C) önbelleği boşaltır; anahtarı kısaltmak (D) ise isabet oranını hiç değiştirmez — hiçbiri bu sorunu çözmez.

# rspring-6 | Orta
## Soru
Popüler bir anahtarın TTL'i dolduğu anda aynı anda yüzlerce istek metodu tetikleyip DB'yi yüklüyor (cache stampede / thundering herd). `@Cacheable`'da bunu hafifleten seçenek nedir?
## A
`@CachePut` kullanıp değeri her çağrıda yeniden hesaplayıp önbelleğe yazdırmak
🤖 `@CachePut` metodu her seferinde çalıştırır; eşzamanlı yeniden-hesaplamayı önlemez.
## B
Cache TTL'ini 0 yapıp girdinin hiç eskimemesini ve yenilenmemesini sağlamak
🤖 TTL 0 önbelleği etkisizleştirir; stampede'i çözmek yerine her isteği DB'ye yükler.
## C
`@Cacheable`'a bir `unless` koşulu ekleyip sonucun yazılmasını koşula bağlamak
🤖 `unless` yalnız sonucun önbelleğe alınıp alınmayacağını belirler; eşzamanlılığı serileştirmez.
## D
`@Cacheable(sync = true)`: aynı anahtarı yalnız bir thread hesaplar, diğerleri bekler
🤖 Doğru: `sync = true`, aynı anahtardaki hesaplamayı tek thread'e serileştirir.
## Doğru
D
## Açıklama
**Cache stampede** (thundering herd): sıcak bir anahtarın TTL'i dolunca, aynı anda gelen çok sayıda istek önbellekte değeri bulamaz ve **hepsi birden** pahalı metodu/DB'yi çalıştırır. `@Cacheable(sync = true)` bunu hafifletir: aynı anahtar için **yalnız bir thread** değeri hesaplar, geri kalan thread'ler **bekleyip** hesaplanan sonucu paylaşır (D). `@CachePut` her çağrıda metodu çalıştırır (A); TTL 0 önbelleği etkisiz kılar (B); `unless` yalnız yazma koşuludur, eşzamanlılığı serileştirmez (C). (Not: `sync=true` tek uygulama örneği içinde etkilidir; çok örnekli dağıtımda dağıtık kilit gerekebilir.)
