---
slug: spring-webflux-reactor
baslik: "Spring WebFlux ve Reactor: Mono/Flux, Backpressure ve 2026 Sanal Thread Tartışması"
kademe: İleri
sira: 88
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir lokanta düşün. Klasik düzende her masaya bir garson atanır ve garson mutfağın önünde durup yemeğin pişmesini bekler. Yüz masa varsa yüz garson gerekir; garsonların çoğu beklerken maaş alır. İkinci düzende beş garson vardır: siparişi mutfağa bırakır, hemen başka masaya geçer, yemek hazır olunca zil çalar ve boştaki garson tabağı götürür. Spring WebFlux ikinci düzendir; az sayıda çalışanla çok sayıda isteği taşımayı amaçlar.

### Temel kavramlar

- **Thread (iş parçacığı)**: Programın aynı anda yürüyebilen çalışma hatlarından biri. Her thread kendi belleğini tuttuğu için binlerce thread pahalıdır.
- **Bloke olmak (blocking)**: Bir thread'in sonuç gelene kadar hiçbir iş yapmadan beklemesi. Veritabanı sorgusu, ağ çağrısı ve dosya okuma tipik bekleme noktalarıdır.
- **Non-blocking (bloke olmayan)**: Thread işi başlatır, beklemez, başka işe geçer; iş bitince bir **geri çağrı** (callback) ile haber verilir.
- **Event loop (olay döngüsü)**: Hazır olan olayları sırayla işleyen, "asla bekleme" kuralına tabi az sayıdaki thread.
- **Akış (stream)**: Zaman içinde tek tek gelen öğeler dizisi. Liste "hepsi elimde" demektir; akış "gelecekte gelecekler" demektir.
- **Yayıncı (publisher) / abone (subscriber)**: Öğeleri üreten taraf ve onları tüketen taraf. Reactive Streams bu ikisi arasındaki sözleşmedir.
- **Reaktif (reactive)**: Veriyi çekerek beklemek yerine, veri geldikçe tepki vermeye dayalı programlama biçimi.
- **`Mono<T>`**: 0 veya 1 öğe yayan yayıncı — tek kullanıcı, tek kayıt sonucu, `void` benzeri tamamlanma.
- **`Flux<T>`**: 0..N öğe yayan yayıncı — bir sorgunun tüm satırları, sunucu-gönderimli olaylar (SSE), sürekli veri akışı.
- **Operatör**: Akışa takılan dönüşüm halkası (`map`, `filter`, `flatMap`).
- **Scheduler (zamanlayıcı)**: Operatörlerin hangi thread havuzunda yürüyeceğini belirleyen bileşen.

`Mono` ve `Flux` aynı `Publisher` sözleşmesini uygular; ikisi de asenkrondur, ikisi de hem veri hem hata hem tamamlanma sinyali yayar ve operatörlerin çoğunu paylaşır. Biri eski diğeri yeni değildir, bir arada kullanılırlar. Tek fark **kaç öğe** yaydıklarıdır; dönüş tipi API'nin niyetini belgeler. "`Mono` senkrondur, `Flux` çok thread'lidir" sanısı yanlıştır: thread modelini isim değil, scheduler belirler.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MVC bloke thread ile WebFlux non-blocking event loop karşılaştırması">
  <text x="160" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">MVC: thread-per-request</text>
  <rect x="30" y="40" width="260" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="50" y="60" width="220" height="26" rx="6" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="160" y="78" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Thread 1 — I/O'da BLOKE bekliyor</text>
  <rect x="50" y="94" width="220" height="26" rx="6" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="160" y="112" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Thread 2 — I/O'da BLOKE bekliyor</text>
  <rect x="50" y="128" width="220" height="26" rx="6" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="160" y="146" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Thread 3 — I/O'da BLOKE bekliyor</text>
  <text x="160" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çok istek = çok thread = çok bellek</text>
  <text x="480" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">WebFlux: event loop</text>
  <rect x="350" y="40" width="260" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="370" y="70" width="220" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="480" y="89" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Az thread — I/O'da BLOKE OLMAZ</text>
  <text x="480" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iş bitince geri çağrılır (callback)</text>
  <text x="480" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">backpressure: request(n)</text>
  <text x="480" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">az thread ile çok eş zamanlı istek</text>
  <defs>
    <marker id="flux-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

**1. Zincir yalnızca bir tariftir.** Aşağıdaki kod hiçbir çıktı üretmez:

```java
Flux.just("a", "b", "c")
    .map(String::toUpperCase);   // hiçbir şey olmaz
```

Kod sorunsuz derlenir (`map` hem `Mono`'da hem `Flux`'ta vardır), `just` dolu bir akış üretir, `toUpperCase` `null` döndürmez. Sorun başkadır: yayıncılar **tembeldir** (lazy). Zincir sadece bir işlem tarifi kurar; veri, `subscribe()` çağrılana kadar akmaz. WebFlux controller'ında `Flux` döndürmek yeterlidir, çünkü yanıtı yazarken **framework abone olur**.

**2. Senkron dönüşüm `map`, asenkron dönüşüm `flatMap`.** `map` bire-bir ve senkrondur. Ama `profilServisi.getir(id)` çağrısı `Mono<Profil>` döndürüyorsa, `map` ile yazmak iç içe `Mono<Mono<Profil>>` üretir — kullanılamaz. `flatMap` iç yayıncıya kendisi abone olur ve sonucu **düzleştirerek** dış akışa katar. Sıra korunacaksa `concatMap`, eşzamanlılık sınırlanacaksa `flatMap`'in eşzamanlılık parametresi kullanılır. `filter` yalnız öğe seçer; `block()` ise zinciri bekletir ve çözüm değildir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="map senkron bire-bir dönüşüm yapar; flatMap her öğeyi asenkron yayıncıya çevirip düzleştirir">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">map (senkron) ile flatMap (asenkron) farkı</text>
  <text x="56" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">map</text>
  <rect x="24" y="64" width="64" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="56" y="85" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">öğe T</text>
  <rect x="136" y="64" width="160" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="216" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">map(T → R)</text>
  <text x="216" y="93" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">senkron bire-bir</text>
  <rect x="344" y="64" width="72" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="380" y="85" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">öğe R</text>
  <text x="536" y="85" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">→ Flux&lt;R&gt;</text>
  <path d="M88 81 L134 81" stroke="#5D7789" stroke-width="1.4" marker-end="url(#flux2-ar)"/>
  <path d="M296 81 L342 81" stroke="#5D7789" stroke-width="1.4" marker-end="url(#flux2-ar)"/>
  <text x="56" y="128" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">flatMap</text>
  <rect x="24" y="136" width="64" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="56" y="157" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">öğe T</text>
  <rect x="136" y="136" width="160" height="34" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="216" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">flatMap(T → yayıncı)</text>
  <text x="216" y="165" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">asenkron çağrı</text>
  <rect x="344" y="136" width="120" height="34" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="404" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">iç Mono&lt;R&gt;</text>
  <text x="404" y="165" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düzleştirilir</text>
  <rect x="512" y="136" width="64" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="544" y="157" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">öğe R</text>
  <path d="M88 153 L134 153" stroke="#5D7789" stroke-width="1.4" marker-end="url(#flux2-ar)"/>
  <path d="M296 153 L342 153" stroke="#5D7789" stroke-width="1.4" marker-end="url(#flux2-ar)"/>
  <path d="M464 153 L510 153" stroke="#5D7789" stroke-width="1.4" marker-end="url(#flux2-ar)"/>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">map ile yazılsa Mono&lt;Mono&lt;R&gt;&gt; oluşurdu — asenkronda flatMap gerekir</text>
  <defs>
    <marker id="flux2-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**3. Geri basınç (backpressure) hızları uyumlar.** Hızlı bir üretici (saniyede binlerce olay) yavaş bir tüketiciye (diske yazan) sınırsız öğe iterse ara tamponlar şişer ve `OutOfMemoryError` gelir. Reactive Streams bunu **talep tabanlı** modelle çözer: tüketici `Subscription.request(n)` ile "şu an n öğe kaldırabilirim" der, üretici yalnız o kadar yayar. Reactor'da strateji `onBackpressureBuffer`, `onBackpressureDrop`, `limitRate` ile seçilir. Geri basıncın güvenlikle ya da bağlantı havuzuyla ilgisi yoktur; zinciri de senkronlaştırmaz.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Backpressure: yavaş tüketici request(n) ile talep bildirir, hızlı üretici yalnız o kadar öğe yayar">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Backpressure: talep tabanlı akış</text>
  <rect x="24" y="54" width="190" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="119" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Üretici (hızlı)</text>
  <text x="119" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">saniyede binlerce olay</text>
  <rect x="426" y="54" width="190" height="76" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="521" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Tüketici (yavaş)</text>
  <text x="521" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">diske yazar</text>
  <path d="M214 80 L424 80" stroke="#25804C" stroke-width="1.6" marker-end="url(#flux3-ar)"/>
  <text x="320" y="74" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">veri: yalnız n öğe</text>
  <path d="M424 110 L214 110" stroke="#186399" stroke-width="1.6" stroke-dasharray="5 3" marker-end="url(#flux3-ar)"/>
  <text x="320" y="124" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">request(n): n kadar kaldırabilirim</text>
  <text x="320" y="164" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Talep kadar yayılır → tampon şişmez, OutOfMemoryError önlenir</text>
  <defs>
    <marker id="flux3-ar" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

**4. Event loop bloke edilmemelidir.** Zincirin ortasındaki JDBC, `RestTemplate`, `.block()` veya `Thread.sleep` çağrısı bir event loop thread'ini I/O boyunca kilitler. Kod derlenir; Spring bunu otomatik olarak başka havuza **taşımaz**. Elde birkaç thread olduğu için birkaç eş zamanlı bloke çağrı havuzu tüketir ve **iş hacmi (throughput)** çöker. Çözüm uçtan uca non-blocking bileşenlerdir (R2DBC, `WebClient`). Bloke çağrı zorunluysa `subscribeOn(Schedulers.boundedElastic())` ile izole edilir — ama bu reactive'in avantajını törpüler.

### Mülakatta ne sorulur?

- **`Mono` mu `Flux` mu?** 0..1 ise `Mono`, 0..N ise `Flux`. İkisi de tembel, asenkron ve `Publisher`.
- **"Neden hiçbir şey olmuyor?"** Abone olunmamıştır. *Nothing happens until you subscribe.*
- **`map` vs `flatMap`**: senkron dönüşüm `map`; yayıncı döndüren asenkron çağrı `flatMap` (yoksa `Mono<Mono<T>>`).
- **Geri basınç (backpressure)**: `request(n)` ile talep tabanlı akış; hızlı üretici yavaş tüketiciyi boğmaz.
- **Event loop'ta bloklama YASAK**: JDBC/`block()` iş hacmini çökertir; otomatik kurtarma yoktur.
- **`subscribeOn` vs `publishOn`**: `subscribeOn` zincirin **abonelik/kaynak** tarafının hangi thread'te başlayacağını belirler ve nereye yazılırsa yazılsın **tüm yukarı akışı** etkiler. `publishOn` yalnız **kendisinden sonraki** operatörleri değiştirir ve zincirde birden çok kez kullanılabilir.
- **2026 sanal thread tartışması**: Sanal thread'ler (Project Loom) MVC'de "her isteğe bir thread"i neredeyse bedava kıldı; bloke eden basit kodla yüksek eş zamanlılık mümkün. Bu, reactive'in tarihsel **ölçek** gerekçesini büyük ölçüde zayıflattı. Sanal thread'ler reactive'e özel değildir, asıl MVC tarzı bloke kodu ucuzlatır; ikisi aynı probleme farklı yanıtlardır. Reactive **akış, backpressure, olay birleştirme** senaryolarında değerini korur; basit CRUD için sanal thread'li MVC yeter.

### Tek cümlede

WebFlux, hiç bloke edilmemesi gereken az sayıda thread üzerinde çalışır. `Mono`/`Flux` yayıncılarıyla talep tabanlı (geri basınçlı) asenkron akışlar kurar; 2026'da ölçek için değil, **akış modeli** için tercih edilir.

# flux-1 | Temel
## Soru
Reactor'da `Mono` ile `Flux` arasındaki fark nedir?
## A
`Mono` 0 veya 1 öğe yayar; `Flux` 0..N öğe yayar
🤖 Doğru: tekil sonuç için Mono, çoklu/akış için Flux kullanılır.
## B
`Mono` senkron çalışır; `Flux` ise her zaman çok thread'li bir havuzda yürütülür
🤖 İkisi de asenkron yayıncıdır; thread modelini scheduler belirler, isim değil.
## C
`Mono` yalnızca hata sinyali yayar; `Flux` ise yalnızca veri yayar, hata yaymaz
🤖 Her ikisi de hem veri hem hata/tamamlanma sinyali yayabilir.
## D
`Flux` eski API'dir; `Mono` onun yerini alan yeni sürümdür
🤖 İkisi de güncel ve bir arada kullanılır; biri diğerinin yerini almaz.
## Doğru
A
## Açıklama
Reactor iki temel yayıncı (publisher) tipi sunar. **`Mono<T>`** en fazla bir öğe yayar (0 veya 1) — tek bir kullanıcı çekmek, bir kayıt işleminin sonucu, `void` benzeri tamamlanma gibi durumlar için. **`Flux<T>`** ise 0'dan N'e öğe yayan bir **akıştır** — bir sorgunun tüm satırları, sunucu-gönderimli olaylar (SSE), sürekli bir veri akışı. İkisi de Reactive Streams `Publisher` sözleşmesini uygular, tembeldir (abone olunana dek çalışmaz) ve aynı operatörlerin çoğunu paylaşır. Doğru tipi seçmek API'nin niyetini belgeler: dönüş tipi `Mono` ise çağıran "tek sonuç", `Flux` ise "akış" bekler.

# flux-2 | Temel
## Soru
Aşağıdaki Reactor zinciri neden hiçbir şey yapmaz (hiçbir çıktı üretmez)?

```java
Flux.just("a", "b", "c")
    .map(String::toUpperCase);
```
## A
`map` yalnızca `Mono` üzerinde tanımlıdır, `Flux` üzerinde derleme hatası verir
🤖 Kod geçerli derlenir; `map` her iki tipte de vardır — sorun çalışma anındaki davranıştadır.
## B
`Flux.just` çağrısı boş bir akış üretir, dolayısıyla yayılacak hiç öğe yoktur
🤖 `just` verilen öğeleri yayan dolu bir akış üretir; sorun burada değil.
## C
Yayıncılar tembeldir; `subscribe` çağrılmadan akış başlamaz
🤖 Doğru: abone olunmadan zincir yalnızca bir "tarif"tir, veri akmaz.
## D
`toUpperCase` `null` döndürdüğü için akış ilk öğede sessizce sonlanır
🤖 `toUpperCase` null döndürmez; ayrıca sorun operatörde değil aboneliğin olmamasında.
## Doğru
C
## Açıklama
Reactor yayıncıları **tembeldir** (lazy). `Flux.just(...).map(...)` yalnızca bir **işlem tarifi** kurar; hiçbir veri, `subscribe()` çağrılana (ya da WebFlux'ta framework abone olana) kadar akmaz. Yukarıdaki kod bir `Flux` nesnesi üretir ama kimse abone olmadığından `map` hiç çalışmaz ve çıktı olmaz. Düzeltme: `.subscribe(System.out::println)` eklemek — veya WebFlux controller'ında `Flux`'ı döndürmek, çünkü framework yanıtı yazarken abone olur. "Nothing happens until you subscribe" Reactor'ın temel kuralıdır.

# flux-3 | Orta
## Soru
Bir kullanıcının id'siyle başka bir asenkron servisten (dönüşü `Mono<Profil>`) profil çekmek için Reactor zincirinde hangi operatör kullanılmalıdır?
## A
`map`, çünkü her öğeyi bire-bir dönüştürüp yeni bir değere çevirir
🤖 `map` senkron dönüşüm içindir; asenkron `Mono` döndüren fonksiyonla iç içe `Mono<Mono<Profil>>` üretir.
## B
`filter`, çünkü koşula uyan öğeyi seçip diğerlerini eler
🤖 `filter` öğe seçer, dönüşüm/asenkron çağrı yapmaz; burada uygun değildir.
## C
`block`, çünkü sonucu beklemeden zincirin devamı yazılamaz
🤖 `block` reaktif zinciri bloke eder; event loop'ta felakettir, çözüm değildir.
## D
`flatMap`, çünkü asenkron yayıncıyı düzleştirerek zincire katar
🤖 Doğru: asenkron çağrıyı zincirlemenin doğru yolu `flatMap`'tir.
## Doğru
D
## Açıklama
Bir öğeyi **asenkron** bir işleme (başka bir `Mono`/`Flux` döndüren) çevirmek gerektiğinde **`flatMap`** kullanılır. `map(id -> profilServisi.getir(id))` yazsaydınız, `getir` bir `Mono<Profil>` döndürdüğü için sonuç `Mono<Mono<Profil>>` olur — iç içe, kullanılamaz. `flatMap` bu iç yayıncıya kendisi abone olur ve sonucu **düzleştirerek** dış akışa katar: `Mono<Profil>`. Kural: dönüşüm **senkron** ise `map`, **asenkron (yayıncı döndürüyor)** ise `flatMap`. Sıralamanın korunması gerekiyorsa `concatMap`, eşzamanlılık sınırı gerekiyorsa `flatMap`'in eşzamanlılık parametresi kullanılır.

# flux-4 | Orta
## Soru
Reactive Streams'te "backpressure" (geri basınç) neyi çözer?
## A
Token'ların imza doğrulamasını hızlandırıp güvenlik kontrolünü ucuzlatır
🤖 Backpressure güvenlikle ilgili değildir; akış hız kontrolüdür.
## B
Hızlı üreticinin yavaş tüketiciyi bellek taşmasıyla boğmasını engeller
🤖 Doğru: tüketici `request(n)` ile kapasitesini bildirir, üretici o sınıra uyar.
## C
Kullanılmayan veritabanı bağlantılarını otomatik kapatıp havuzu küçültür
🤖 Bağlantı yönetimi ayrı bir konudur; backpressure akış hızını düzenler.
## D
Reaktif zinciri senkron hâle getirip operatörleri sırayla çalıştırır
🤖 Backpressure zinciri senkronlaştırmaz; asenkron akışta hız uyumu sağlar.
## Doğru
B
## Açıklama
**Backpressure**, asenkron akışlarda **üretici hızının tüketici kapasitesini aşması** problemini çözer. Hızlı bir kaynak (örn. saniyede binlerce olay) yavaş bir tüketiciye (örn. diske yazan) sınırsız öğe iletirse, ara tamponlar şişer ve `OutOfMemoryError` gelir. Reactive Streams sözleşmesi bunu **talep tabanlı** (demand-driven) modelle çözer: tüketici `Subscription.request(n)` ile "şu an n öğe daha kaldırabilirim" der; üretici yalnızca talep edildiği kadar yayar. Reactor'da `onBackpressureBuffer`, `onBackpressureDrop`, `limitRate` gibi operatörlerle strateji seçilir. Bu, reaktif modelin bloke etmeden akışı dengede tutma yeteneğinin temelidir.

# flux-5 | İleri
## Soru
Bir WebFlux (non-blocking) controller içinde reaktif zincirin ortasında bloke eden bir JDBC çağrısı yapılırsa ne olur?
## A
Sorun olmaz; Spring bloke çağrıyı otomatik olarak ayrı bir thread havuzuna taşır
🤖 Böyle bir otomatik taşıma yoktur; bloke çağrı event loop thread'ini kilitler.
## B
Derleme hatası oluşur; WebFlux bağımlılığı varken JDBC sürücüsü kullanılamaz
🤖 Derlenir (JDBC sınıfları erişilebilir); sorun derlemede değil çalışma anındaki throughput çöküşündedir.
## C
Bir event loop thread'i I/O boyunca kilitlenir ve throughput çöker
🤖 Doğru: az sayıdaki event loop thread'i bloke edilince eş zamanlılık avantajı yok olur.
## D
Yanıt daha hızlı döner, çünkü senkron çağrı bekleme adımını kısaltır
🤖 Aksine; bloke çağrı non-blocking modelin tüm faydasını yok eder.
## Doğru
C
## Açıklama
WebFlux, çok sayıda isteği **az sayıda** event loop thread'iyle yürütür; bu thread'lerin asla bloke olmaması varsayımına dayanır. Reaktif zincirin ortasında **bloke eden** bir çağrı (JDBC, `RestTemplate`, `.block()`, `Thread.sleep`) yapılırsa, o çağrı bir event loop thread'ini I/O süresince **kilitler**. Elde yalnızca birkaç thread olduğundan, birkaç eş zamanlı bloke çağrı tüm thread havuzunu tüketir ve sunucu yeni istekleri işleyemez — throughput çöker. Çözüm: uçtan uca non-blocking bileşenler (R2DBC, `WebClient`). Bloke bir çağrı zorunluysa `subscribeOn(Schedulers.boundedElastic())` ile ayrı bir thread havuzuna izole edilmelidir — ama bu, reactive'in avantajını törpüler.

# flux-6 | İleri
## Soru
2026'da sanal thread'lerin (virtual threads, Project Loom) yaygınlaşmasının, "reactive'e geçme" kararına etkisi en doğru nasıl özetlenir?
## A
Ölçek gerekçesi zayıfladı; reactive akış ve backpressure için değerini korur
🤖 Doğru: ölçek gerekçesi zayıfladı ama streaming/backpressure için reactive üstünlüğünü korur.
## B
Sanal thread'ler reactive'i tümüyle gereksiz kıldı; artık hiçbir senaryoda WebFlux kullanılmamalı
🤖 Aşırı genelleme; akış ve backpressure senaryolarında reactive hâlâ üstündür.
## C
Sanal thread'ler yalnız reactive kodla çalışır; MVC yığınında etkinleştirilemez
🤖 Tam tersi; sanal thread'ler asıl bloke eden MVC tarzı kodu ucuzlatır.
## D
İkisi ilgisiz konulardır; aralarında hiçbir kavramsal örtüşme veya alternatiflik yoktur
🤖 İlgisiz değiller; ikisi de yüksek eş zamanlılık problemine farklı yanıtlardır, doğrudan alternatiftirler.
## Doğru
A
## Açıklama
**Sanal thread'ler**, "her isteğe bir thread" (thread-per-request) modelinde thread'i neredeyse bedava kıldı: bloke eden **basit, okunması kolay** senkron kod yazıp yine on binlerce eş zamanlı isteği taşımak mümkün oldu. Reactive'in tarihsel ana satış argümanı **"az kaynakla yüksek eş zamanlılık"** olduğundan, bu argüman sanal thread'li MVC ile büyük ölçüde karşılandı — basit CRUD servisleri için reactive'in bilişsel maliyetine (zor hata ayıklama, öğrenme eğrisi, uçtan uca non-blocking zorunluluğu) katlanmak çoğu ekip için artık gereksiz. Ancak reactive'in **akış işleme, backpressure, olay birleştirme, geriye-basınçlı veri boru hatları** gibi senaryolardaki ifade gücü sanal thread'lerle birebir karşılanmaz. 2026 dengesi: "ölçek için değil, akış modeli için reactive seç."
