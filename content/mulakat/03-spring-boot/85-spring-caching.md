---
slug: spring-caching
baslik: "Spring Caching: @Cacheable/@CacheEvict/@CachePut, Anahtar, TTL ve Cache Stampede"
kademe: Orta
sira: 85
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir arkadaşının telefon numarasını her arayışında rehberi baştan taramak yerine bir kez bakıp aklında tutarsın; sonraki aramalarda iş anında biter. **Önbellek** (cache) tam olarak budur: pahalı bir hesabın veya veritabanı sorgusunun sonucunu bir kenara yazmak ve aynı soru tekrar geldiğinde hesabı hiç yapmadan yanıt vermek. Spring'in önbellek soyutlaması bunu koda tek satır önbellek mantığı yazmadan, yalnız anotasyonla yapmanı sağlar.

### Temel kavramlar

- **Önbellek (cache)**: Anahtar–değer çiftlerini tutan hızlı depo.
- **Anahtar (key)**: "Hangi soru soruldu" bilgisini temsil eden değer. Aynı anahtar aynı yanıtı işaret eder.
- **İsabet (hit)**: Anahtar önbellekte bulundu; metot hiç çalışmaz.
- **Iska (miss)**: Anahtar yok; metot çalışır, sonuç önbelleğe yazılır.
- **TTL (time to live — yaşam süresi)**: Bir girdinin ne kadar süre geçerli sayılacağı.
- **Tahliye (eviction)**: Girdinin önbellekten atılması (süresi dolduğu, yer bittiği ya da elle silindiği için).
- **Bayat veri (stale)**: Veritabanı değişmiş ama önbellekte hâlâ eski değerin durması.
- **Vekil nesne (proxy)**: Gerçek nesnenin önüne konan sarmalayıcı; çağrıyı önce o karşılar ve ek işi (önbellek kontrolü) yapar.

### Nasıl çalışır?

Önbellek soyutlaması, tıpkı `@Transactional` gibi **proxy tabanlı AOP** ile çalışır: çalışma zamanında üretilen bir vekil nesne metodu sarar. Çağrı geldiğinde vekil sırasıyla (1) anahtarı hesaplar, (2) önbellekte arar, (3) isabet varsa metodu **hiç çalıştırmadan** değeri döndürür, (4) ıska varsa metodu çalıştırır, dönüş değerini anahtarla önbelleğe yazar ve çağırana verir.

Bu bir çalışma-zamanı mekanizmasıdır; derleme zamanında koda değer gömülmez. Saklanan şey metodun **dönüş değeridir**, veritabanının SQL sorgu önbelleği değil. Vekil tabanlı olduğu için `@Transactional`'daki tuzak burada da geçerlidir. Aynı sınıf içindeki bir metot, `@Cacheable` metodu `this` üzerinden çağırırsa çağrı vekile uğramaz. Buna **nesnenin kendi metodunu kendi içinden çağırması** (self-invocation) denir ve önbellek **atlanır**.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cacheable isabet ve ıska akışı">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">@Cacheable: isabet (hit) mi, ıska (miss) mı?</text>
  <rect x="24" y="44" width="120" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Çağrı (anahtar)</text>
  <text x="168" y="66" text-anchor="middle" font-size="17" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="196" y="44" width="130" height="50" rx="9" fill="#F4F6F8" stroke="#5D7789" stroke-width="1.4"/>
  <text x="261" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Önbellekte var mı?</text>
  <text x="261" y="83" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">anahtar aranır</text>
  <rect x="380" y="30" width="236" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="498" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">İSABET: metot ÇALIŞMAZ</text>
  <text x="498" y="70" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">önbellekteki değer döner</text>
  <rect x="380" y="96" width="236" height="70" rx="9" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.4"/>
  <text x="498" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#B67A1A" font-family="system-ui">IŞKA: metot çalışır</text>
  <text x="498" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sonuç önbelleğe yazılır,</text>
  <text x="498" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sonra çağırana döner</text>
  <path d="M326 62 L380 56" stroke="#25804C" stroke-width="1.4" fill="none"/>
  <path d="M326 74 L380 118" stroke="#B67A1A" stroke-width="1.4" fill="none"/>
</svg>
```

Üç temel anotasyon bu akışın üç farklı noktasına dokunur:

- **`@Cacheable`**: İlk çağrıda metot çalışır, dönüş değeri anahtara göre yazılır; sonraki aynı-anahtarlı çağrılarda metot **hiç çalıştırılmaz**. Okuma metotları için.
- **`@CachePut`**: Metodu **her zaman çalıştırır** ve dönüş değeriyle önbelleği **günceller** (yaz-üstüne). Güncelleme metotlarında hem veritabanını hem önbelleği taze tutar.
- **`@CacheEvict`**: Bir girdiyi önbellekten **siler**; `allEntries = true` ile tüm önbelleği temizler. Veri değişince/silinince bayat girdiyi düşürmek için.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cacheable metodu atlar, CachePut her zaman çalıştırır, CacheEvict siler">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç anotasyon, üç davranış</text>
  <rect x="20" y="42" width="196" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="118" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">@Cacheable</text>
  <text x="118" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">isabet → metot ATLANIR</text>
  <text x="118" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ıska → çalışır + yazar</text>
  <text x="118" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">okuma metotları</text>
  <rect x="222" y="42" width="196" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">@CachePut</text>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">metodu HER ZAMAN</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çalıştırır</text>
  <text x="320" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dönüşle günceller</text>
  <text x="320" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">güncelleme metotları</text>
  <rect x="424" y="42" width="196" height="132" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="522" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">@CacheEvict</text>
  <text x="522" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">girdiyi SİLER</text>
  <text x="522" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">allEntries=true → tümü</text>
  <text x="522" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bayatlamayı önler</text>
</svg>
```

Geriye ölçekli sistemlerde belirleyici olan üçüncü sorun kalır: çok istenen bir anahtarın süresi dolduğu anda **yüzlerce eşzamanlı isteğin hep birlikte ıskalayıp** aynı pahalı metodu aynı anda çalıştırması. Buna **önbellek izdihamı** (cache stampede; "thundering herd" da denir) adı verilir. Veritabanı bu ani yükle boğulur.

```svg
<svg viewBox="0 0 640 212" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cache stampede: TTL dolunca çok istek aynı metodu koşturur; sync=true tekilleştirir">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Cache stampede ve sync = true</text>
  <text x="165" y="44" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">sync YOK</text>
  <rect x="24" y="54" width="282" height="140" rx="10" fill="#FBECEC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="165" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">TTL dolar → popüler anahtar ıskalanır</text>
  <rect x="44" y="88" width="52" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="70" y="105" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">istek</text>
  <rect x="140" y="88" width="52" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="166" y="105" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">istek</text>
  <rect x="236" y="88" width="52" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="262" y="105" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">istek</text>
  <text x="165" y="140" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">HEPSİ pahalı metodu koşturur</text>
  <text x="165" y="160" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">DB boğulur ✗</text>
  <text x="165" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(thundering herd)</text>
  <text x="487" y="44" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">sync = true</text>
  <rect x="334" y="54" width="282" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="354" y="88" width="52" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="380" y="105" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">istek</text>
  <rect x="450" y="88" width="52" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="476" y="105" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">bekler</text>
  <rect x="546" y="88" width="52" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="572" y="105" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">bekler</text>
  <text x="487" y="140" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yalnız BİR thread hesaplar</text>
  <text x="487" y="160" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">metot TEK kez çalışır ✓</text>
  <text x="487" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">diğerleri hazır değeri alır</text>
</svg>
```

Bildirimsel çare `@Cacheable(sync = true)`'dur: aynı anahtar için yalnız **bir** iş parçacığının (thread) değeri hesaplamasına izin verilir, diğerleri bekleyip hazır değeri alır. Böylece pahalı metot N kez değil **bir** kez çalışır. `sync = true` önbelleği kapatmaz ve TTL'yi uzatmaz. İzdiham yalnız Redis'e özgü de değildir; yerel önbellekte de olur. Daha ileri stratejiler şunlardır: süre dolmadan arka planda tazeleme (refresh-ahead), üzerine rastgele sapma (jitter) eklenmiş TTL ve olasılıksal erken yeniden hesaplama.

### Mülakatta ne sorulur?

- **"`@Cacheable` metodu ikinci çağrıda ne olur?"** → İsabet varsa metot **çalışmaz**, önbellekteki değer döner. Iska varsa metot çalışır ve sonuç yazılır.
- **"`@CachePut` ile `@CacheEvict` farkı?"** → `@CachePut` metodu **her zaman çalıştırır** ve önbelleği günceller; `@CacheEvict` girdiyi **siler**. İkisi de `@Cacheable`'ın aksine metodu atlamaz. Sık desen: okuma metoduna `@Cacheable`, yazma metoduna `@CachePut` veya `@CacheEvict`.
- **"Anahtar varsayılan olarak nasıl üretilir?"** → `SimpleKeyGenerator` anahtarı **metot parametrelerinden** üretir: parametresizse sabit bir anahtar, tek parametrede parametrenin kendisi, çok parametrede bir `SimpleKey` bileşimi. Farklı argümanlar farklı girdi oluşturur. `key` vermek zorunlu değildir; gerekirse SpEL ile özelleştirilir — `@Cacheable(value = "kullanici", key = "#id")` ya da `key = "#kullanici.email"` — veya uygulama geneli bir `KeyGenerator` nesnesi (bean — Spring'in üretip yönettiği nesne) tanımlanır. Anahtar metot adından tek başına üretilmez ve **dönüş değerinden üretilemez** (değer çağrıdan önce bilinmez).
- **"10 dakikalık TTL'i nasıl verirsin?"** → `@Cacheable`'ın `ttl` diye bir özniteliği **YOKTUR**. Süre, en büyük boyut ve tahliye politikası **sağlayıcıda** yapılandırılır: Caffeine'de `Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).maximumSize(...)`, Redis'te `RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))`. Farklı önbelleklere farklı TTL verilebilir. Spring'in "her beş dakikada bir hepsini sil" gibi bir varsayılanı yoktur. Kısaca: **ne saklanacağı anotasyonda, ne kadar süreyle saklanacağı sağlayıcıda.**
- **"Çok örnekli dağıtımda Caffeine mi Redis mi?"** → **Caffeine** JVM sürecinin içinde (in-process) çalışan çok hızlı bir **yerel** önbellektir. Ağ gecikmesi yoktur, ama **her örnek kendi kopyasını** tutar. Bu yüzden bir örnekteki `@CacheEvict` diğerlerinin yerel önbelleğini temizlemez ve örnekler arasında tutarsızlık doğar. **Redis** ise uygulamanın dışında, ağ üzerinden erişilen **paylaşımlı/dağıtık** bir sunucudur; tüm örnekler aynı önbelleği görür ve bir güncelleme herkes için geçerlidir. Bedeli ağ gidiş-dönüşü ve serileştirmedir. İkisi eşdeğer değildir; bazı mimariler ikisini birlikte kullanır (yerel L1 + dağıtık L2).
- **"Cache stampede nedir, `sync = true` ne yapar?"** → Süresi dolan popüler anahtarda eşzamanlı ıskaların pahalı metodu birlikte koşturmasıdır; `sync = true` hesaplamayı aynı anahtar için **tek thread'e** indirir.

### Tek cümlede

Spring önbelleği, metot dönüş değerini bir anahtara göre saklayıp aynı soruya hesap yapmadan yanıt verir; **ne saklanacağını** anotasyonlar, **ne kadar süreyle** saklanacağını sağlayıcı belirler.

# cache-1 | Temel
## Soru
`@Cacheable("urunler")` ile işaretli bir metodu aynı argümanla ikinci kez çağırdığında ne olur?
## A
Metot her çağrıda yeniden çalıştırılır; `@Cacheable` yalnızca sonucu bir günlük (log) dosyasına yazarak ölçüm yapmanı sağlar.
🤖 Amaç tam tersi: ikinci çağrıda metot çalıştırılmaz, önbellekten dönülür.
## B
`@Cacheable` yalnız sabit veriler için derleme zamanında değeri koda gömer; çalışma zamanında hiçbir etkisi kalmaz.
🤖 @Cacheable çalışma-zamanı proxy ile çalışır; derleme-zamanı gömme yapmaz.
## C
Metodun ürettiği SQL sorguları otomatik olarak veritabanının sorgu önbelleğine yazılır ve ikinci çağrı oradan yanıtlanır.
🤖 @Cacheable metot DÖNÜŞ değerini önbelleğe alır, SQL sorgu önbelleğiyle ilgisi yoktur.
## D
İlk çağrıda metot çalışır ve sonuç önbelleğe yazılır; ikinci çağrıda metot çalışmaz, değer önbellekten döner.
🤖 Doğru: isabet (hit) durumunda metot atlanır, önbellek değeri döner.
## Doğru
D
## Açıklama
`@Cacheable`, proxy üzerinden metodu sarar: çağrı geldiğinde önce **anahtarı** hesaplar ve önbellekte arar. **Iska (miss)** ise metot çalışır, dönüş değeri anahtarla önbelleğe yazılır ve çağırana döner. **İsabet (hit)** ise metot **hiç çalıştırılmaz**; doğrudan önbellekteki değer döner — pahalı hesaplama/sorgu atlanır. Bu, metot dönüş değerini saklar; veritabanının SQL sorgu önbelleğiyle (C) ilgisi yoktur, her çağrıda çalışmaz (A) ve derleme-zamanı gömme değildir (B). Not: proxy tabanlı olduğu için, aynı sınıf içindeki bir metot `@Cacheable` metodu `this` üzerinden çağırırsa önbellek atlanır (self-invocation).

# cache-2 | Temel
## Soru
`@CachePut` ile `@CacheEvict` arasındaki temel fark nedir?
## A
`@CachePut` metodu atlayıp değeri doğrudan önbellekten okur; `@CacheEvict` ise metodu çalıştırıp sonucu önbelleğe yazar.
🤖 Ters tanım: CachePut metodu çalıştırır ve yazar; CacheEvict siler.
## B
`@CachePut` metodu HER ZAMAN çalıştırır ve dönüşle önbelleği günceller; `@CacheEvict` girdiyi önbellekten SİLER.
🤖 Doğru: CachePut günceller, CacheEvict siler.
## C
İkisi de metodu atlayıp yalnızca önbellek üzerinde çalışır; veritabanına hiç dokunmadıkları için yazma işlemlerinde kullanılamazlar.
🤖 CachePut metodu çalıştırır; ikisi de metodu atlamaz.
## D
`@CacheEvict` önbelleğe yeni bir değer ekler; `@CachePut` ise var olan girdiyi silerek bir sonraki okumayı ıskaya zorlar.
🤖 Tam ters: Evict siler, Put ekler/günceller.
## Doğru
B
## Açıklama
İki anotasyon zıt amaçlara hizmet eder ama ikisi de `@Cacheable`'dan farklı olarak metodu atlamaz. `@CachePut` metodu **her zaman çalıştırır** ve dönen değeri önbelleğe **yazar/günceller** — bir kaydı güncelleyen (`update`) metotlarda, hem veritabanını hem önbelleği taze tutmak için idealdir. `@CacheEvict` ise bir girdiyi (veya `allEntries = true` ile tüm önbelleği) **siler** — veri silindiğinde/değiştiğinde bayat (stale) girdiyi düşürmek için. Sık desen: yazma metoduna `@CachePut` veya `@CacheEvict`, okuma metoduna `@Cacheable`. A ve D tanımları ters çevirir; C ise CachePut'un metodu çalıştırdığı gerçeğini atlar.

# cache-3 | Orta
## Soru
`@Cacheable("kullanici")` bir metotta önbellek anahtarı (key) varsayılan olarak nasıl belirlenir?
## A
Anahtar her zaman yalnız metot adından üretilir; parametreler anahtara hiç katılmaz, bu yüzden farklı argümanlar aynı girdiyi paylaşır.
🤖 Varsayılan anahtar parametrelerden üretilir; farklı argümanlar farklı anahtar verir.
## B
Anahtar otomatik üretilemez; her `@Cacheable` metotta `key` özniteliğini elle vermezsen uygulama açılışta hata verir.
🤖 SimpleKeyGenerator varsayılan anahtar üretir; key elle vermek zorunlu değildir.
## C
Varsayılanda anahtar metot PARAMETRELERİNDEN üretilir; SpEL veya özel bir `KeyGenerator` ile değiştirilebilir.
🤖 Doğru: varsayılan parametrelerden türer, SpEL/özel generator ile değiştirilebilir.
## D
Anahtar metodun DÖNÜŞ değerinden üretilir; böylece aynı sonucu veren farklı çağrılar tek bir önbellek girdisinde birleşir.
🤖 Dönüş değeri çağrıdan önce bilinmez; anahtar ondan üretilemez.
## Doğru
C
## Açıklama
Spring, varsayılan olarak `SimpleKeyGenerator` kullanır: anahtarı **metot parametrelerinden** üretir — parametresizse sabit bir anahtar, tek parametreyse o parametrenin kendisi, çoklu parametrede bir `SimpleKey` bileşimi. Bu yüzden farklı argümanlar farklı önbellek girdisi oluşturur. İhtiyaç halinde anahtar **SpEL** ile özelleştirilir: `@Cacheable(value = "kullanici", key = "#id")` veya `key = "#kullanici.email"`; ya da uygulama geneli bir `KeyGenerator` bean'i tanımlanır. Anahtar dönüş değerinden üretilemez (D — değer çağrıdan önce bilinmez); yalnız metot adından da üretilmez (A) ve `key` vermek zorunlu değildir (B). Doğru anahtar tasarımı önbellek isabet oranını doğrudan belirler.

# cache-4 | Orta
## Soru
Önbellek girdilerinin 10 dakika sonra otomatik dolmasını (TTL) istiyorsun. Bunu Spring'de nasıl yaparsın?
## A
TTL `@Cacheable`'ın bir özniteliği DEĞİLDİR; süre ve tahliye politikası önbellek SAĞLAYICISINDA ayarlanır.
🤖 Doğru: TTL anotasyonda değil, sağlayıcı yapılandırmasında verilir.
## B
`@Cacheable(ttl = 600)` yazarak saniye cinsinden süre verilir; Spring bu değeri sağlayıcıdan bağımsız olarak kendisi uygular.
🤖 @Cacheable'da ttl özniteliği yoktur; süre sağlayıcıda ayarlanır.
## C
TTL yalnız `@CachePut` üzerinde ayarlanabilir; `@Cacheable` ile yazılan girdilerin süresi hiçbir zaman dolmaz.
🤖 TTL anotasyon düzeyinde değil sağlayıcı düzeyindedir; CachePut'a özel değildir.
## D
Spring önbelleği varsayılan olarak her beş dakikada bir tüm girdileri temizler; bunu uzatmak için zamanlanmış bir görev yazılır.
🤖 Böyle bir varsayılan otomatik temizleme yoktur; süre sağlayıcı yapılandırmasına bağlıdır.
## Doğru
A
## Açıklama
Spring'in önbellek **soyutlaması**, kasıtlı olarak TTL/boyut/tahliye politikası içermez — bu, sağlayıcıdan sağlayıcıya değişen bir ayrıntıdır ve soyutlamanın dışında bırakılmıştır. Bu yüzden `@Cacheable`'da `ttl` gibi bir öznitelik **yoktur** (B). Süre ve tahliyeyi **önbellek sağlayıcısında** yapılandırırsın: **Caffeine** için `Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).maximumSize(...)`; **Redis** için `RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))`. Farklı önbellekler için farklı TTL bile tanımlanabilir. TTL `@CachePut`'a özel değildir (C) ve Spring'in "her 5 dakikada sil" gibi bir varsayılanı yoktur (D). Kısacası: **ne saklanacağı anotasyonda, ne kadar süreyle saklanacağı sağlayıcıda** belirlenir.

# cache-5 | İleri
## Soru
Uygulaman yük dengeleyici arkasında **birden çok örnekte** (instance) çalışacak. Önbellek sağlayıcısı olarak Caffeine yerine Redis seçmenin temel gerekçesi nedir?
## A
Caffeine dağıtık ve paylaşımlı bir önbellektir; Redis ise yalnız tek bir JVM içinde çalışan yerel bir önbellek olduğu için ölçeklenmez.
🤖 Ters: Caffeine JVM-içi yereldir, Redis dağıtık/paylaşımlıdır.
## B
Caffeine JVM-içi yereldir, her örnek KENDİ kopyasını tutar; Redis paylaşımlıdır, tüm örnekler AYNI önbelleği görür.
🤖 Doğru: yerel-örnek-başı vs paylaşımlı-tek-kaynak ayrımı temel gerekçedir.
## C
İkisi de yalnız Spring Boot'a özeldir ve uygulama dışındaki süreçlerden erişilemez; aralarındaki fark yalnızca isimlendirmededir.
🤖 Redis bağımsız bir sunucudur ve dışarıdan erişilebilir; fark isimsel değildir.
## D
Sağlayıcı seçimi ne performansı ne tutarlılığı etkiler; Caffeine ile Redis birbirinin yerine geçebilen eşdeğerlerdir.
🤖 Yerel ve dağıtık önbellek tutarlılık/performans açısından çok farklıdır.
## Doğru
B
## Açıklama
**Caffeine**, JVM sürecinin **içinde** (in-process) çalışan, çok hızlı bir yerel önbellektir; ağ gecikmesi yoktur ama **her uygulama örneği kendi kopyasını** tutar. Tek örnekli bir uygulamada mükemmeldir; ama birden çok örnekte, bir örnekte yapılan `@CacheEvict` diğer örneklerin yerel önbelleğini **temizlemez** → örnekler arasında **tutarsızlık** (stale data) oluşur. **Redis**, uygulamanın dışında, ağ üzerinden erişilen **paylaşımlı/dağıtık** bir önbellektir; tüm örnekler **aynı** önbelleği görür, bir örnekteki güncelleme/eviction herkes için geçerlidir — çok örnekli dağıtımda tutarlılık bunu gerektirir. Bedeli: ağ gidiş-dönüşü ve serileştirme maliyeti. A tanımı ters çevirir; Redis bağımsız erişilebilir bir sunucudur (C) ve iki sağlayıcı eşdeğer değildir (D). Bazı mimariler ikisini birlikte (yerel L1 + dağıtık L2) kullanır.

# cache-6 | İleri
## Soru
Çok popüler bir önbellek anahtarının süresi dolduğu anda, yüzlerce eşzamanlı istek aynı anda önbelleği ıskalayıp hepsi birden pahalı metodu çalıştırıyor ve veritabanını boğuyor. Bu soruna ne denir ve `@Cacheable(sync = true)` nasıl yardımcı olur?
## A
Buna "cache stampede" denir; `sync = true` önbelleği tümüyle devre dışı bırakıp her isteği doğrudan veritabanına göndererek sorunu bitirir.
🤖 sync=true önbelleği kapatmaz; eşzamanlı hesaplamayı tekilleştirir.
## B
Buna "cache stampede" denir ama `sync = true` yalnızca anahtarın TTL'sini uzatır; eşzamanlı çağrıların sayısıyla hiçbir ilgisi yoktur.
🤖 sync=true TTL uzatmaz; aynı anahtar için tek hesaplamayı garanti eder.
## C
Bu sorun yalnız Redis gibi dağıtık önbelleklerde görülür; Caffeine'de yapısal olarak imkânsız olduğundan `sync = true` gereksizdir.
🤖 Stampede yerel önbellekte de olur; sağlayıcıya özgü değildir.
## D
Buna "cache stampede" (thundering herd) denir; `sync = true` hesaplamayı aynı anahtar için tek iş parçacığına indirir.
🤖 Doğru: sync=true aynı anahtarda hesaplamayı tekilleştirir, sürüyü önler.
## Doğru
D
## Açıklama
**Cache stampede** (veya "thundering herd"): sık istenen bir anahtarın süresi dolduğu (ya da ilk kez ıskalandığı) anda, çok sayıda eşzamanlı istek **hep birlikte** önbelleği ıskalar ve hepsi aynı pahalı metodu (ör. ağır bir sorgu) aynı anda çalıştırır — veritabanı ani yükle boğulur. `@Cacheable(sync = true)`, aynı anahtar için yalnız **bir** iş parçacığının değeri hesaplamasına izin verir; diğer iş parçacıkları o hesaplama bitene kadar **bekler** ve sonra hazır değeri alır. Böylece pahalı metot N kez değil **bir** kez çalışır. `sync = true` önbelleği kapatmaz (A) veya TTL'yi uzatmaz (B); stampede yerel önbellekte de olur, Redis'e özgü değildir (C). Daha ileri stratejiler: süre dolmadan önce arka planda tazeleme (refresh-ahead), rastgele jitter'lı TTL ve olasılıksal erken yeniden hesaplama.
