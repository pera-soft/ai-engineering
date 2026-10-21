---
slug: nio-file-io
baslik: "NIO ve Dosya G/Ç: Path, Buffer, Channel"
kademe: Orta
sira: 44
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir dosyayı okumak "aç ve oku" kadar basit görünür; ama veri diskte, program bellektedir ve arada işletim sistemi durur. Her gidiş-geliş pahalıdır. Suyu kuyudan taşımayı düşün: tek bardakla yüz kez gitmek yerine bir kova doldurup bir kez taşırsın. Java'nın G/Ç (I/O — girdi/çıktı) dünyası da bunu yapar: veriyi bardak bardak değil, **tampon** (kova) dolusu taşır. Bu işin Java'da iki nesli vardır: eski `java.io` (akış tabanlı, `File`) ve modern `java.nio` ("New I/O" — tampon/kanal tabanlı, `Path`/`Files`).

### Temel kavramlar

- **G/Ç (I/O)**: programın dışıyla — dosya, ağ, klavye — veri alışverişi.
- **Akış (stream)**: baytların sırayla aktığı boru; okundukça ilerler, kendiliğinden geri sarılmaz.
- **Bayt akışı ve karakter akışı**: `InputStream`/`OutputStream` ham 8 bitlik baytlarla çalışır (resim, ses, ikili dosya). `Reader`/`Writer` ise bir **karakter kümesiyle** (charset, ör. UTF-8 — baytları harflere eşleyen tablo) çözülmüş metinle çalışır (metin dosyası, JSON, log). İkisi ayrı katmandır; aralarında `InputStreamReader` köprü kurar.
- **Yol (path) ve `Path`**: bir dosya/dizinin konumunu anlatan bilgi. `Path` bunun soyut, işletim sisteminden bağımsız temsilidir — içerik tutmaz, dosya var olmasa bile üretilebilir, kaynak açmaz ve kapatılmaz; göreli (relative) ve mutlak (absolute) yolların ikisini de temsil eder, `resolve` ile birleştirir.
- **`Files`**: okuma/yazma/kopyalama/silme işlerini statik metotlarla yapan yardımcı sınıf. `Path` yolu tanımlar, `Files` işi yürütür.
- **Dosya tanıtıcısı (file handle)**: işletim sisteminin açık her dosya için verdiği numara. Sayısı sınırlıdır; kapatılmayanlar birikirse "too many open files" hatası gelir.
- **Kanal (channel)**: bir dosya ya da sokete (ağ bağlantısı) açılan iki yönlü veri yolu.
- **Tampon (buffer)**: sabit kapasiteli bir bayt kabı. Üç göstergesi vardır: `capacity` (kapasite — sabittir, hiçbir metot büyütmez), `position` (imleç: sıradaki bayt), `limit` (geçerlilik sınırı).
- **İş parçacığı (thread)**: program içinde bağımsız ilerleyen yürütme hattı.
- **Bloklama (blocking)**: bir çağrının, veri gelene kadar iş parçacığını bekletmesi; o iş parçacığı bu sırada başka hiçbir iş yapamaz. Bloklamayan (non-blocking) kipte çağrı, hazır veri yoksa hemen `0` ile döner.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Channel ile Buffer arasında veri akışı ve heap-direct ayrımı">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kanal ↔ Tampon ↔ Bellek</text>
  <rect x="30" y="56" width="140" height="90" rx="12" fill="#EEF3FA" stroke="#2C5B8A" stroke-width="1.5"/>
  <text x="100" y="96" text-anchor="middle" font-size="13" font-weight="700" fill="#2C5B8A" font-family="system-ui">Channel</text>
  <text x="100" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">dosya / soket</text>
  <rect x="250" y="56" width="140" height="90" rx="12" fill="#FFF6E6" stroke="#B5851A" stroke-width="1.5"/>
  <text x="320" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#B5851A" font-family="system-ui">ByteBuffer</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">position/limit/capacity</text>
  <text x="320" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">flip() ile oku-moduna</text>
  <rect x="470" y="52" width="140" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="540" y="80" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">heap (GC)</text>
  <rect x="470" y="104" width="140" height="46" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="540" y="132" text-anchor="middle" font-size="12" font-weight="700" fill="#B4322B" font-family="system-ui">direct (off-heap)</text>
  <path d="M172 101 L248 101" stroke="#5D7789" stroke-width="1.5" marker-end="url(#nio-ok)"/>
  <path d="M392 101 L468 101" stroke="#5D7789" stroke-width="1.5" marker-end="url(#nio-ok)"/>
  <defs>
    <marker id="nio-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="192" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Büyük dosya için memory-mapped; akış için Files.lines.</text>
</svg>
```

### Nasıl çalışır?

Sık işler için düşük seviyeye inmek gerekmez: `Files.readString(path)` küçük bir dosyanın tamamını tek `String` olarak, `Files.readAllLines(path)` `List<String>` olarak verir. En yalın doğru aracı seçmek kuraldır.

Düşük seviyede veri, **kanal ile tampon arasında** akar. Tampon tek bir `position` göstergesiyle hem yazma hem okuma yaptığı için mod değiştirirken göstergeleri elle ayarlaman gerekir:

```java
ByteBuffer buf = ByteBuffer.allocate(1024); // position=0, limit=capacity=1024
kanal.read(buf);        // YAZMA modu: position okunan bayt kadar ilerler
buf.flip();             // limit = position, position = 0  → OKUMA modu
while (buf.hasRemaining()) System.out.print((char) buf.get());
buf.clear();            // position=0, limit=capacity — veri silinmez, üzerine yazılır
```

`flip()` hiçbir veriyi silmez, kapasiteyi büyütmez ve bayt sırasını (byte order) değiştirmez — o iş `order(ByteOrder...)` metodunundur. Tamponu yeniden yazmaya hazırlamak için `clear()`, henüz okunmamış baytları başa taşıyıp yazmaya devam etmek için `compact()` kullanılır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ByteBuffer flip göstergeleri: yazma modundan okuma moduna">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">flip(): limit=position, position=0</text>
  <text x="40" y="58" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">flip öncesi — yazma modu</text>
  <rect x="40" y="66" width="560" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="40" y="66" width="224" height="30" rx="5" fill="#186399" opacity="0.20"/>
  <path d="M264 96 L264 110" stroke="#25804C" stroke-width="2"/>
  <text x="264" y="124" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">position=5</text>
  <path d="M600 96 L600 110" stroke="#8A2E2E" stroke-width="2"/>
  <text x="592" y="124" text-anchor="end" font-size="10.5" fill="#8A2E2E" font-family="system-ui">limit=capacity=10</text>
  <text x="40" y="158" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">flip sonrası — okuma modu</text>
  <rect x="40" y="166" width="560" height="30" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <rect x="40" y="166" width="224" height="30" rx="5" fill="#25804C" opacity="0.20"/>
  <path d="M40 196 L40 210" stroke="#186399" stroke-width="2"/>
  <text x="52" y="210" text-anchor="start" font-size="10.5" fill="#186399" font-family="system-ui">position=0</text>
  <path d="M264 196 L264 210" stroke="#8A2E2E" stroke-width="2"/>
  <text x="264" y="210" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">limit=5 (okunacak bölge)</text>
</svg>
```

Tamponun nerede durduğu da bir karardır. `ByteBuffer.allocate` tamponu **JVM yığınında** (heap) üretir: bellek çöp toplayıcının (GC) yönetimindedir, tahsisi ucuzdur, ama veri çekirdeğe (kernel) geçerken JVM içinde ara kopya gerekebilir. `allocateDirect` ise tamponu **yığın dışında** (off-heap, native bellek) üretir; işletim sistemi bu belleğe doğrudan erişebildiği için dosya ve soket G/Ç'sinde ara kopya adımı atlanır. Bedeli: tahsis pahalıdır ve GC ile hemen boşalmaz (temizleyici/Cleaner beklenir). Direct tampon dosya kanallarında da çalışır; ikisi de sıfırla başlar. Kural: uzun ömürlü, tekrar kullanılan ve G/Ç ağırlıklı tamponlar direct, kısa ömürlü küçükler heap olsun.

Çok büyük dosyalarda iki güçlü araç var. `FileChannel.map(...)` dosyanın bir bölgesini `MappedByteBuffer` üzerinden **doğrudan sanal belleğe eşler** (memory-mapped file): erişim sıradan bellek erişimi gibidir, işletim sistemi gerekli sayfaları talep üzerine (demand paging) getirir; rastgele erişim için idealdir. `Files.readAllBytes` ise dosyayı bütünüyle yığına yükler — gigabaytlık dosyada `OutOfMemoryError` demektir. `Files.lines(path)` satırları **tembel** (lazy) bir `Stream<String>` olarak verir: dosyayı belleğe almadan, işlenen satır kadar okur; ama altta açık bir dosya tanıtıcısı vardır ve akış `AutoCloseable`'dır.

```java
try (Stream<String> satirlar = Files.lines(path)) {
    long hataSayisi = satirlar.filter(s -> s.contains("ERROR")).count();
} // dosya tanıtıcısı burada güvenle kapanır
```

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Byte akışı ile karakter akışı ayrımı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Byte akışı vs karakter akışı</text>
  <rect x="30" y="48" width="270" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Byte akışı</text>
  <text x="165" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">InputStream / OutputStream</text>
  <text x="165" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ham 8-bit bayt</text>
  <text x="165" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">resim, ses, ikili dosya</text>
  <rect x="340" y="48" width="270" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Karakter akışı</text>
  <text x="475" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Reader / Writer</text>
  <text x="475" y="118" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">charset ile çözülmüş metin</text>
  <text x="475" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metin dosyası, JSON, log</text>
  <text x="320" y="188" text-anchor="middle" font-size="11.5" font-weight="600" fill="#B26B00" font-family="system-ui">InputStreamReader köprüsü charset ile bayttan karaktere geçirir.</text>
</svg>
```

Son bir ayrım: klasik bloklayan G/Ç'de `read` veri gelene kadar iş parçacığını bekletir, yani bağlantı başına bir iş parçacığı gerekir. NIO kanalları bloklamayan kipe alınabilir; hazır veri yoksa `read` hemen `0` döner ve tek bir iş parçacığı `Selector` üzerinden binlerce kanalı izleyebilir.

### Mülakatta ne sorulur?

- **`flip()` tam olarak ne yapar?** `limit = position`, `position = 0`. Veriyi silmez (`clear()` de silmez, sadece göstergeleri sıfırlar), kapasiteyi büyütmez, endian dönüşümü yapmaz.
- **`allocate` vs `allocateDirect`**: heap/GC + ucuz tahsis, karşısında off-heap + ara kopyasız G/Ç ama pahalı tahsis ve gecikmeli boşalma.
- **Çok büyük dosyada rastgele erişim**: `FileChannel.map`. `readAllBytes` bellek yetmez, `BufferedInputStream` ile baştan taramanın maliyeti dosya boyutuyla büyür, bayt başına akış açıp kapatmak felakettir. Uyarı: eşlemenin serbest bırakılması JVM'de deterministik değildir ve işletim sistemine bağlıdır.
- **`Files.lines` kuralı**: `try-with-resources` şart; otomatik kapanmaz. Bu, `List.stream()` gibi bellek-içi akışlardan farklı olarak kaynağa-bağlı akışların ayırt edici kuralıdır.
- **`Path` vs `File`**: `Path` yalnızca yolu temsil eder, tanıtıcı değildir; eski `File` hem yolu tutup hem iş yapardı, hataları `boolean` ile bildirirdi ve sembolik bağlantı (symlink) desteği eksikti. NIO'da `IOException` alt türleri anlamlı hata verir.

### Tek cümlede

NIO'da `Path` yolu, `Files` işi, kanal taşımayı ve tampon (position/limit/capacity + `flip`) veriyi tutar; doğru araç dosyanın boyutuna ve erişim biçimine göre seçilir.

# nio-1 | Temel
## Soru
`java.nio.file.Path`, eski `java.io.File`'a göre temel olarak neyi temsil eder?
## A
Dosyanın içeriğini bellekte tutan, okundukça ilerleyen bir tampondur
🤖 `Path` hiç içerik tutmaz; yalnızca yol bilgisidir, dosya var olmasa bile üretilebilir.
## B
Yalnızca mutlak (absolute) yolları temsil eder; göreli (relative) yollar için hâlâ `File` gerekir
🤖 `Path` göreli ve mutlak yolların ikisini de temsil eder, `resolve` ile birleştirir.
## C
Bir dosya/dizin yolunun işletim sisteminden bağımsız, soyut temsilidir; `Files` yardımcı sınıfıyla birlikte modern G/Ç API'sini oluşturur
🤖 Doğru: `Path` yolu tanımlar, işlemleri `Files` yürütür.
## D
Açılmış bir dosyanın işletim sistemi tanıtıcısıdır (file handle); kullanıldıktan sonra kapatılması gerekir
🤖 `Path` bir kaynak tutmaz, kapatılmaz; tanıtıcıyı `Files`/`FileChannel` açar ve kapatır.
## Doğru
C
## Açıklama
`Path`, bir dosya veya dizin yolunun **soyut, platformdan bağımsız** temsilidir — içeriği değil, konumu tanımlar. Kendisi dosyayı okumaz/yazmaz; asıl işlemleri statik `Files` yardımcı sınıfı yürütür (`Files.readAllLines`, `Files.copy`, `Files.delete` vb.). Bu ayrım eski `File`'dan önemli bir ilerlemedir: `File` hem yolu temsil eder hem de bazı işlemleri yapardı ama hata bildirimi zayıf (metotlar `boolean` döndürürdü), symlink desteği eksikti. NIO'da `Path` + `Files` ikilisi net roller, zengin işlemler ve anlamlı `IOException` alt türleri sunar.

# nio-2 | Temel
## Soru
Küçük bir metin dosyasının tamamını tek çağrıda okumak için hangi yaklaşım en yalındır?
## A
`Files.readString(path)` veya `Files.readAllLines(path)` kullanmak
🤖 Doğru: tek çağrıda tüm içeriği/ satırları verir, yalın ve nettir.
## B
Bir `FileChannel` açıp `ByteBuffer` ile döngüde okuyup `flip` çevirmek
🤖 İşe yarar ama tek çağrı değil; küçük dosya için gereksiz karmaşık, düşük seviyeli bir yoldur.
## C
`System.in` üzerinden okumak
🤖 `System.in` standart giriştir, dosya değil.
## D
Dosyayı `Serializable` yapıp `readObject` çağırmak
🤖 Serileştirme nesne durumu içindir, düz metin dosyası okuma için değil.
## Doğru
A
## Açıklama
NIO, sık G/Ç senaryoları için `Files` üzerinde yüksek seviyeli kısayollar sunar: `Files.readString(path)` dosyanın tamamını tek bir `String` olarak, `Files.readAllLines(path)` ise `List<String>` olarak döndürür (küçük/orta dosyalar için idealdir). `FileChannel` + `ByteBuffer` yaklaşımı (B) doğrudur ama düşük seviyelidir; büyük veya akışsal işlemler için anlamlıdır, küçük bir dosyayı okumak için gereksiz ayrıntı ve hata yüzeyi ekler. Kural: en yalın doğru aracı seç — küçük dosya için `Files` kısayolları, çok büyük/akışsal veri için kanal veya `Files.lines`.

# nio-3 | Orta
## Soru
Bir `ByteBuffer`'a yazdıktan sonra içindekileri okumaya geçerken neden `flip()` çağrılır?
## A
Tamponu sıfırlar ve içine yazılmış tüm baytları silerek yeniden kullanıma hazırlar
🤖 `flip()` hiçbir veriyi silmez; yalnızca göstergeleri okuma için ayarlar (o iş `clear()`'ın bile değil, `clear` da veriyi silmez).
## B
Tampon dolduğunda kapasiteyi (capacity) iki katına çıkararak taşmayı önler
🤖 `ByteBuffer` kapasitesi sabittir; `flip` de dâhil hiçbir metot onu büyütmez.
## C
Baytların sırasını büyük-endian'dan küçük-endian'a çevirir (byte order dönüşümü)
🤖 Bayt sırası `order(ByteOrder...)` ile ayarlanır; `flip`'in bununla ilgisi yoktur.
## D
`limit`'i mevcut `position`'a, `position`'ı `0`'a ayarlar; böylece yazılan bölge baştan okunabilir hale gelir
🤖 Doğru: yazma-modundan okuma-moduna geçişin standart adımıdır.
## Doğru
D
## Açıklama
`ByteBuffer` tek bir `position` göstergesiyle hem yazma hem okuma yapar; bu yüzden mod değiştirirken göstergeleri elle ayarlaman gerekir. Yazma sırasında `position` yazılan bayt sayısına kadar ilerler. `flip()` çağrıldığında: `limit = position` (yazılan son nokta artık okuma sınırı olur), `position = 0` (okuma baştan başlar). Böylece kanaldan tampona yazdığın veriyi, `flip` sonrası baştan okuyabilir veya başka bir kanala yazabilirsin. `flip` veriyi SİLMEZ (A yanlış); temizlemek için `clear()`, okunmamışı başa taşımak için `compact()` kullanılır. Bu gösterge yönetimi NIO tampon modelinin en sık karıştırılan noktasıdır.

```java
ByteBuffer buf = ByteBuffer.allocate(1024);
kanal.read(buf);   // yazma modu: position ilerler
buf.flip();        // okuma moduna geç: limit=position, position=0
while (buf.hasRemaining()) System.out.print((char) buf.get());
```

# nio-4 | Orta
## Soru
`ByteBuffer.allocate` ile `ByteBuffer.allocateDirect` arasındaki temel fark nedir?
## A
`allocate` daha hızlıdır ve her zaman tercih edilmelidir
🤖 Genelleme yanlış; G/Ç ağırlıklı işte direct genelde daha hızlıdır.
## B
`allocate` tamponu JVM yığınında (heap) üretir; `allocateDirect` yığın dışında (off-heap/native) üretir ve G/Ç'de ekstra kopyayı atlar
🤖 Doğru: yer (heap vs off-heap) ve maliyet dengesi belirleyicidir.
## C
`allocateDirect` yalnızca soket kanallarıyla kullanılabilir; dosya kanallarında `allocate` zorunludur
🤖 Böyle bir kısıt yok; direct tampon dosya kanallarında da çalışır ve orada da avantajlıdır.
## D
İkisi de belleği aynı yerde ayırır; fark yalnızca `allocateDirect`'in tamponu sıfırlarla doldurmasıdır
🤖 İkisi de sıfırla başlar; asıl fark belleğin heap içinde mi dışında mı ayrıldığıdır.
## Doğru
B
## Açıklama
`allocate`, tamponu **JVM yığınında** (heap) üretir; bellek GC tarafından yönetilir, tahsisi ucuzdur ama çekirdeğe (kernel) veri geçerken JVM içi ara kopya gerekebilir. `allocateDirect`, tamponu **yığın dışında** (off-heap, native bellek) üretir; işletim sistemi bu belleğe doğrudan erişebildiği için dosya/soket G/Ç'sinde ara kopya adımı atlanır ve yoğun G/Ç'de daha hızlıdır. Bedeli: tahsis/serbest bırakma pahalıdır, GC ile hemen boşalmaz (temizleyici/Cleaner beklenir). Bu yüzden direct buffer, **uzun ömürlü ve tekrar kullanılan**, G/Ç ağırlıklı tamponlar için mantıklıdır; kısa ömürlü küçük tamponlar için heap buffer daha uygundur.

# nio-5 | İleri
## Soru
Çok büyük bir dosyanın rastgele bölgelerine verimli erişmek için hangisi uygundur?
## A
Her bayt için ayrı bir `FileInputStream` açıp kapatmak
🤖 Tanıtıcı açma/kapama maliyeti çok yüksek; verimsiz.
## B
Dosyanın tamamını `Files.readAllBytes` ile belleğe almak
🤖 Çok büyük dosyada bellek yetmez (`OutOfMemoryError` riski).
## C
`FileChannel.map` ile memory-mapped file: dosya bölgesi doğrudan sanal belleğe eşlenir
🤖 Doğru: bellek-eşlemeli erişim büyük dosyalarda güçlü bir araçtır.
## D
Dosyayı `BufferedInputStream` ile baştan sona tarayıp istenen bölgeye ulaşmak
🤖 Rastgele erişimde her istek için baştan taramak demektir; maliyeti dosya boyutuyla büyür.
## Doğru
C
## Açıklama
`FileChannel.map(...)`, dosyanın bir bölgesini bir `MappedByteBuffer` üzerinden **doğrudan sanal belleğe eşler** (memory-mapped file). Erişim sıradan bellek erişimi gibidir; işletim sistemi gerekli sayfaları talep üzerine (demand paging) belleğe getirir. Avantajları: dosyanın tamamını yığına almadan çok büyük dosyalarda rastgele erişim, işletim seviyesinde önbellekleme ve kopya maliyetinin azalması. `Files.readAllBytes` (B) dosyayı bütünüyle yığına yükler; gigabaytlık dosyalarda `OutOfMemoryError` üretir. Bellek-eşleme özellikle veri tabanları, dizin (index) dosyaları ve büyük ikili varlıklar için tercih edilir. Dikkat: eşleme kaynağı işletim sistemine bağlıdır ve serbest bırakılması JVM'de deterministik değildir.

# nio-6 | İleri
## Soru
`Files.lines(path)` ile satır satır işlerken hangi kurala dikkat edilmelidir?
## A
Altta açık bir dosya tanıtıcısı vardır; `Stream` mutlaka `try-with-resources` içinde kapatılmalıdır
🤖 Doğru: `Files.lines` kapatılabilir bir kaynağa bağlı tembel akıştır.
## B
`Files.lines` dosyanın tamamını belleğe alır, bu yüzden büyük dosyalarda kullanılamaz
🤖 Tam tersi; tembeldir, satırları talep üzerine üretir, belleğe almaz.
## C
`Files.lines` yalnızca ikili (binary) dosyalarla çalışır
🤖 Satır tabanlı metin içindir; ikili dosya için uygun değildir.
## D
Akış otomatik kapandığı için `try-with-resources` gereksizdir
🤖 Otomatik kapanmaz; elle veya try-with-resources ile kapatılmalıdır.
## Doğru
A
## Açıklama
`Files.lines(path)`, satırları **tembel** (lazy) bir `Stream<String>` olarak verir: dosyanın tamamını belleğe almadan, işlenen satır kadar okur — bu yüzden büyük dosyalar için idealdir (B yanlış). Ancak bu akış, altta **açık bir dosya tanıtıcısına** (file handle) bağlıdır ve `AutoCloseable`'dır. Terminal işlem bittiğinde akış otomatik kapanmaz; kapatılmazsa dosya tanıtıcısı sızar ve zamanla "too many open files" hatasına yol açabilir. Doğru kullanım akışı `try-with-resources` içine almaktır. Bu, `List.stream()` gibi bellek-içi akışlardan farklı olarak kaynağa-bağlı akışların ayırt edici kuralıdır.

```java
try (Stream<String> satirlar = Files.lines(path)) {
    long hataSayisi = satirlar.filter(s -> s.contains("ERROR")).count();
} // dosya tanıtıcısı burada güvenle kapanır
```
