---
slug: jvm-bellek-alanlari
baslik: "JVM Bellek Alanları (Heap, Stack, Metaspace)"
kademe: İleri
sira: 60
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir mutfağı düşünün. Ortada büyük bir **ortak tezgâh** var: yemekler orada duruyor, herkes oradan alıp oraya koyuyor ve zaman zaman biri gelip artıkları topluyor. Bir de her aşçının önünde kendi **küçük kesme tahtası** var: yalnız o kullanıyor, iş bitince siliniyor. Duvarda ise tarif defterleri asılı — yemeğin kendisi değil, "nasıl yapılacağı" bilgisi. JVM belleği de böyle bölünür; hangi şeyin nerede durduğunu bilmek, "bu hatayı neden aldım" sorusunun cevabıdır.

### Temel kavramlar

- **Yığın (heap)**: ortak tezgâh. `new` ile üretilen **tüm nesneler ve diziler** burada yaşar; iş parçacıkları arasında **paylaşılır** ve çöp toplayıcının (GC) asıl alanıdır. İçinde nesil (generational) bölgeler vardır: **genç nesil (young: eden + survivor)** ve **yaşlı nesil (old/tenured)**.
- **Çöp toplama (garbage collection, GC)**: artık hiçbir yerden erişilemeyen nesnelerin belleğini otomatik geri alan mekanizma. Java'da bellek elle boşaltılmaz.
- **Yığıt (stack)**: her iş parçacığına **özeldir**. Metot çağrıldıkça üst üste **çerçeveler (frame)** yığılır; çerçeve yerel değişkenleri, referansları ve çağrı bağlamını tutar, metot bitince atılır. Boyutu `-Xss` ile ayarlanır.
- **PC register (program sayacı)**: yine **iplik-özeldir**; o ipliğin yürüttüğü sıradaki JVM komutunun adresini tutar.
- **Native method stack**: yerel (native) metot çağrılarının çerçeveleri için ayrılan, **iplik-özel** alandır.
- **Metaspace**: duvardaki tarif defterleri — sınıf üstverisini (class metadata) tutar; **yerel bellekte (native memory)** yer alır ve Java 8'de **PermGen'in yerini** almıştır.
- **Direct memory (off-heap)**: `ByteBuffer.allocateDirect` gibi yollarla ayrılan, Java yığınının **dışındaki** yerel bellektir.

```svg
<svg viewBox="0 0 640 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JVM bellek alanları: paylaşılan heap, iplik-özel stackler, native Metaspace ve direct memory">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JVM çalışma-zamanı bellek alanları</text>
  <rect x="28" y="40" width="330" height="120" rx="12" fill="#EAF2FB" stroke="#2C6BB3" stroke-width="1.6"/>
  <text x="193" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#2C6BB3" font-family="system-ui">Heap (paylaşılan · GC burada)</text>
  <rect x="44" y="74" width="150" height="70" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="119" y="98" text-anchor="middle" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">Genç nesil</text>
  <text x="119" y="116" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">eden + survivor</text>
  <text x="119" y="132" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">String pool da heap'te</text>
  <rect x="206" y="74" width="136" height="70" rx="8" fill="#FBF1E6" stroke="#B5771F" stroke-width="1.3"/>
  <text x="274" y="98" text-anchor="middle" font-size="11" font-weight="600" fill="#B5771F" font-family="system-ui">Yaşlı nesil</text>
  <text x="274" y="116" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">(old / tenured)</text>
  <rect x="374" y="40" width="238" height="56" rx="10" fill="#F3EDF9" stroke="#7A4EA8" stroke-width="1.4"/>
  <text x="493" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#7A4EA8" font-family="system-ui">Metaspace (native)</text>
  <text x="493" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sınıf üstverisi · PermGen değil</text>
  <rect x="374" y="104" width="238" height="56" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.4"/>
  <text x="493" y="126" text-anchor="middle" font-size="12" font-weight="700" fill="#B4322B" font-family="system-ui">Direct memory (off-heap)</text>
  <text x="493" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">allocateDirect · yığın dışı</text>
  <rect x="28" y="176" width="584" height="56" rx="10" fill="#EEF1F4" stroke="#5D7789" stroke-width="1.3"/>
  <text x="320" y="198" text-anchor="middle" font-size="12" font-weight="700" fill="#3A4A57" font-family="system-ui">Stack (her iplik için AYRI · metot çerçeveleri, yerel değişkenler)</text>
  <text x="320" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">taşarsa StackOverflowError · heap dolarsa OutOfMemoryError</text>
</svg>
```

### Nasıl çalışır?

En temel ayrım, bir alanın her iplik için ayrı mı yoksa tüm iplikler arasında paylaşılan mı olduğudur. **İplik-özel** olanlar (PC register, JVM stack, native method stack) iplik doğunca oluşur, ölünce gider. **Paylaşılan** olanlar (heap, Metaspace) ise GC'nin ve senkronizasyonun anlamlı olduğu yerdir.

Bundan çıkan altın kural: **nesne heap'te, referans stack'te.** `Kullanici k = new Kullanici();` yazdığınızda nesnenin gövdesi heap'te ayrılır; `k` yerel değişkeni yalnızca ona işaret eden bir **referanstır** ve ipliğin yığıtında durur. Nesne gövdesi ne CPU yazmaçlarında (register) ne de Metaspace'te tutulur. (Tek ince istisna: JIT'in **kaçış analiziyle** skaler değişime uğrattığı bazı nesneler yığıta ayrılabilir; bu bir optimizasyondur, kavramsal kural yine "nesne heap'te"dir.)

**PermGen → Metaspace.** Java 8'de kalıcı nesil (**PermGen**) tamamen **kaldırıldı**, yerini **Metaspace** aldı. Metaspace de sınıf üstverisini tutar, ama artık JVM yığınının sabit boyutlu bir bölgesi değil, **yerel bellektedir** ve varsayılan olarak ihtiyaca göre **otomatik büyür** (üst sınır `-XX:MaxMetaspaceSize`). Bu değişiklik klasik `OutOfMemoryError: PermGen space` derdini ve elle boyutlandırma zahmetini bitirdi. Üstveri hâlâ gereklidir; yalnız durduğu yer değişti — iplik-özel stack'te değil, Metaspace'te.

**String havuzu (string pool)**, aynı içerikli string sabitlerinin (literal) tek bir örneği paylaşmasını sağlayan intern tablosudur. Java 6 ve öncesinde **PermGen**'deydi ve aşırı `intern()` kullanımı `OutOfMemoryError`'a yol açabiliyordu; **Java 7 ile heap'e taşındı**, normal çöp toplamaya tabi oldu ve boyutu esnedi. Paylaşılan **tek** bir yapıdır: iplik başına ayrı tutulmaz, diskte saklanmaz, JVM kapanınca kaybolur.

**Direct memory**, çekirdek (kernel) ile Java heap'i arasındaki fazladan kopyayı azaltabildiği için özellikle dosya/soket I/O'sunda verimlidir. Java heap'ini **şişirmez** ve heap nesneleriyle birlikte sıkıştırılmaz; serbest bırakılması `Cleaner`/referans mekanizmalarına bağlıdır. Aşırı kullanımı `OutOfMemoryError: Direct buffer memory` verir (sınır `-XX:MaxDirectMemorySize`). Yalnız statik alanlara özgü bir kısıtı yoktur; her bağlamda ayrılabilir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JVM bellek alanları iplik-özel ve paylaşılan ayrımı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her iplik için AYRI mı, PAYLAŞILAN mı?</text>
  <rect x="22" y="42" width="292" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="64" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İplik-özel (thread-private)</text>
  <text x="42" y="90" font-size="11" fill="#1C3247" font-family="system-ui">• PC register — sıradaki komut adresi</text>
  <text x="42" y="116" font-size="11" fill="#1C3247" font-family="system-ui">• JVM Stack — metot çerçeveleri,</text>
  <text x="52" y="132" font-size="10.5" fill="#5D7789" font-family="system-ui">yerel değişkenler, referanslar</text>
  <text x="42" y="158" font-size="11" fill="#1C3247" font-family="system-ui">• Native method stack</text>
  <text x="168" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her thread doğunca oluşur, ölünce gider</text>
  <rect x="326" y="42" width="292" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="64" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Paylaşılan (tüm iplikler)</text>
  <text x="346" y="90" font-size="11" fill="#1C3247" font-family="system-ui">• Heap — tüm nesneler + diziler</text>
  <text x="356" y="106" font-size="10.5" fill="#5D7789" font-family="system-ui">(genç/yaşlı nesil, String pool)</text>
  <text x="346" y="132" font-size="11" fill="#1C3247" font-family="system-ui">• Metaspace — sınıf üstverisi</text>
  <text x="356" y="148" font-size="10.5" fill="#5D7789" font-family="system-ui">(native bellek)</text>
  <text x="472" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">GC ve senkronizasyon burada anlamlı</text>
</svg>
```

Hangi alanın tükendiği, hangi hatayı göreceğinizi belirler. **`StackOverflowError`**, bir ipliğin **yığıt derinliği** aşılınca atılır: her metot çağrısı bir çerçeve ekler, sonsuz ya da çok derin özyineleme (recursion) çerçeveleri biriktirir ve `-Xss` ile belirlenen sınır aşılır. JVM özyinelemeyi belli bir derinlikten sonra kendiliğinden durdurmaz; hata `NullPointerException` değildir ve heap'le ilgisi yoktur. **`OutOfMemoryError`** ise bir **bellek alanına yer ayrılamayınca** atılır; alt mesajı hangi alanın tükendiğini söyler: `Java heap space`, `Metaspace`, `Direct buffer memory`, `unable to create new native thread`. İkisi de çalışma zamanında oluşur ve JVM aralarında rastgele seçim yapmaz.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="StackOverflowError ve OutOfMemoryError farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi alan tükenirse hangi Error?</text>
  <rect x="22" y="42" width="292" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">StackOverflowError</text>
  <text x="168" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">bir ipliğin STACK derinliği aşıldı</text>
  <text x="168" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">neden: derin/sonsuz özyineleme</text>
  <text x="168" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iplik-özel · -Xss ile ayarlanır</text>
  <text x="168" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">her çağrı bir çerçeve ekler</text>
  <rect x="326" y="42" width="292" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">OutOfMemoryError</text>
  <text x="472" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">bir bellek ALANINA yer ayrılamadı</text>
  <text x="472" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Java heap space · Metaspace</text>
  <text x="472" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Direct buffer memory · native thread</text>
  <text x="472" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">alt mesaj hangi alanın tükendiğini söyler</text>
</svg>
```

### Mülakatta ne sorulur?

- **Nesne nerede?** `new` ile üretilen nesne ve dizi **heap'tedir**; yerel değişken yalnız ona bir **referans** tutar ve o referans **stack'tedir**. Nesne gövdesi ne stack'te, ne Metaspace'te, ne de CPU yazmaçlarında saklanır.
- **PermGen.** Java 8'de **kaldırıldı**, yerine **Metaspace** geldi; Metaspace **yerel bellektedir (native memory)** ve otomatik büyür. "Değişmedi", "stack'e taşındı" veya "üstveri artık tutulmuyor" yanlıştır.
- **String pool.** Java 7'den beri **heap'tedir** (Java 6 ve öncesinde PermGen'deydi); paylaşılan tek yapıdır, diskte tutulmaz.
- **Direct memory.** `allocateDirect` off-heap yerel bellek ayırır; I/O için verimlidir, Java heap'ini şişirmez, heap ile birlikte sıkıştırılmaz ve kendi sınırında `OutOfMemoryError: Direct buffer memory` verir.
- **`StackOverflowError` vs `OutOfMemoryError`.** Biri bir ipliğin **yığıt derinliğinin** aşılması (tipik neden: sonsuz özyineleme; iplik-özel, `-Xss`), diğeri bir **bellek alanına yer ayrılamaması**. `OutOfMemoryError` yalnız Metaspace'e özgü değildir — heap dâhil birçok alan için atılır. İkisi de derleme zamanı hatası değildir.
- **İplik-özel mi, paylaşılan mı?** İplik-özel: PC register, JVM stack, native method stack. Paylaşılan: heap (String pool dâhil), Metaspace.

### Tek cümlede

JVM belleği; paylaşılan ve çöp toplanan **heap** (nesneler), iplik başına ayrı **stack** (çerçeveler ve referanslar), yerel bellekteki **Metaspace** (sınıf üstverisi) ve yığın dışı **direct memory** olarak bölünür — hangi alanın tükendiği hangi hatayı alacağınızı belirler.

# jvmmem-1 | İleri
## Soru
Java'da bir nesneyi `new` ile oluşturduğunuzda, nesnenin kendisi nerede yer alır?
## A
Her zaman ilgili iş parçacığının yığıtında (stack), diğer ipliklerden yalıtılmış olarak
🤖 Yığıtta yalnız yerel değişkenler/referanslar durur; nesnenin gövdesi heap'tedir.
## B
Metaspace'te, o sınıfın üstverisinin (metadata) hemen yanında
🤖 Metaspace sınıf üstverisi içindir; nesne örnekleri orada tutulmaz.
## C
CPU yazmaçlarında (register); nesne ömrü boyunca orada tutulur
🤖 Yazmaçlar geçici işlem alanıdır; nesne gövdesi orada saklanmaz.
## D
Yığında (heap); yerel değişken yalnız bu nesneye bir referans tutar, o referans ise yığıttadır
🤖 Doğru: nesne heap'te, ona işaret eden referans stack'te.
## Doğru
D
## Açıklama
`new` ile oluşturulan nesne örnekleri ve diziler **yığında (heap)** yaşar; heap iş parçacıkları arasında paylaşılır ve çöp toplayıcının yönettiği alandır. Metot içindeki yerel değişken ise yalnızca bu nesneye bir **referans** tutar ve bu referans, ilgili iş parçacığının **yığıtında (stack)** bulunur. Yani "nesne heap'te, referans stack'te" ayrımı temeldir. A yalnız referansı doğru konumlar ama nesnenin kendisini yanlış yere koyar; B Metaspace'i (sınıf üstverisi) örneklerle karıştırır; C yazmaçları kalıcı nesne deposu sanar. (İstisna: kaçış analizi ile skaler değişime uğrayan bazı nesneler yığıta ayrılabilir; bu bir JIT optimizasyonudur, kavramsal kural yine "nesne heap'te"dir.)

# jvmmem-2 | İleri
## Soru
Java 8 ile birlikte **PermGen** yerine ne gelmiştir ve temel fark nedir?
## A
Metaspace gelmiştir; sınıf üstverisini yerel bellekte (native memory) tutar ve varsayılan olarak otomatik büyür
🤖 Doğru: Metaspace native bellektedir, PermGen'in sabit sınırını ortadan kaldırır.
## B
PermGen aynen korunmuştur; Java 8'de bu alanda hiçbir değişiklik yapılmamıştır
🤖 Yanlış; Java 8'de PermGen kaldırılıp yerine Metaspace getirildi.
## C
Sınıf üstverisi artık her iş parçacığının yığıtında (stack) tutulmaktadır
🤖 Stack iplik-özel çağrı çerçeveleri içindir; sınıf üstverisi orada durmaz.
## D
Sınıf üstverisi tamamen kaldırılmıştır; JVM çalışma zamanında sınıf üstverisi tutmaz
🤖 Üstveri hâlâ gereklidir; yalnızca tutulduğu alan (Metaspace) değişti.
## Doğru
A
## Açıklama
Java 8'de kalıcı nesil (**PermGen**) tamamen kaldırıldı ve yerini **Metaspace** aldı. Metaspace de sınıf üstverisini (class metadata) tutar, ama artık JVM yığınının sabit-boyutlu bir bölgesi değil, **yerel bellektedir (native memory)** ve varsayılan olarak ihtiyaca göre **otomatik büyür** (üst sınır `-XX:MaxMetaspaceSize` ile konur). Bu değişiklik, PermGen'in klasik `OutOfMemoryError: PermGen space` sorunlarını ve elle boyutlandırma zahmetini azalttı. B (değişmedi), C (stack'te) ve D (üstveri yok) yanlıştır: üstveri hâlâ gereklidir ve artık Metaspace'te, native bellekte tutulur.

# jvmmem-3 | İleri
## Soru
String havuzu (string intern pool) modern Java'da (Java 7+) nerede yer alır?
## A
PermGen'de; kalıcı nesil string sabitlerinin bugün de asıl yeridir
🤖 Java 6 ve öncesinde PermGen'deydi; Java 7 ile heap'e taşındı, ayrıca PermGen Java 8'de kaldırıldı.
## B
Stack'te; her iş parçacığı kendi string havuzunu ayrı tutar
🤖 String pool paylaşılan tek bir yapıdır; iplik-özel stack'te olamaz.
## C
Heap'te; Java 7 ile PermGen'den heap'e taşınmıştır
🤖 Doğru: string pool Java 7'den beri heap'tedir.
## D
Disk üzerinde bir önbellek dosyasında; JVM yeniden başlasa da korunur
🤖 Bellek içi bir yapıdır; diskte tutulmaz ve JVM kapanınca kaybolur.
## Doğru
C
## Açıklama
**String havuzu (string pool)**, aynı içerikli string sabitlerinin (literal) tek bir örneğini paylaşmasını sağlayan intern tablosudur. Java 6 ve öncesinde bu havuz **PermGen** içindeydi ve sınırlı PermGen alanı yüzünden aşırı `intern()` kullanımı `OutOfMemoryError`'a yol açabiliyordu. **Java 7** ile string pool **heap'e** taşındı; böylece normal çöp toplamaya tabi oldu ve boyut esnedi. B (iplik-özel stack) yanlıştır çünkü havuz paylaşılan tek bir yapıdır; D (disk) ise bellek-içi bir yapıyı yanlış konumlandırır. Bugün doğru cevap: **heap**.

# jvmmem-4 | Uzman
## Soru
`ByteBuffer.allocateDirect(...)` ile ayrılan "direct memory" hakkında hangisi doğrudur?
## A
Bu bellek normal Java yığınının (heap) içindedir ve GC tarafından diğer nesnelerle birlikte sıkıştırılır
🤖 Direct memory yığın dışıdır; heap sıkıştırmasına tabi değildir.
## B
Direct memory ayrılamaz; JDK'de yığın dışı tampon ayıran böyle bir API bulunmaz
🤖 API vardır; `allocateDirect` tam da off-heap tampon ayırır.
## C
Direct memory yalnız statik alanlarda (static field) tutulan tamponlar için kullanılabilir
🤖 Böyle bir kısıt yoktur; herhangi bir bağlamda ayrılabilir.
## D
Yığın DIŞI (off-heap) yerel bellektir; Java heap'ini şişirmez ama aşırı kullanımı `OutOfMemoryError: Direct buffer memory` verebilir
🤖 Doğru: off-heap'tir, I/O için verimlidir ama kendi sınırında OOM verebilir.
## Doğru
D
## Açıklama
`allocateDirect`, JVM yığınının **dışında (off-heap)**, işletim sistemine yakın **yerel bellekte** bir tampon ayırır. Avantajı: özellikle dosya/soket I/O'sunda çekirdek ile Java heap'i arasındaki fazladan kopyayı azaltabilmesidir. Ancak bu bellek Java heap'ini **şişirmez** ve normal GC tarafından doğrudan sıkıştırılmaz; serbest bırakılması `Cleaner`/referans mekanizmalarına bağlıdır ve aşırı kullanımı `OutOfMemoryError: Direct buffer memory` ile sonuçlanabilir (sınır `-XX:MaxDirectMemorySize`). A direct memory'yi heap içi sanar (yanlış); B API'nin varlığını yadsır; C uydurma bir kısıt koyar. Doğru tanım: **off-heap, I/O için verimli, kendi sınırında OOM riski taşıyan** bellek.

# jvmmem-5 | İleri
## Soru
Sonsuz özyineleme (infinite recursion) çalışan bir metot tipik olarak hangi hatayı üretir?
## A
`StackOverflowError`; biriken çağrı çerçeveleri (frame) ipliğin yığıt sınırını aşar
🤖 Doğru: biriken çağrı çerçeveleri stack sınırını aşınca StackOverflowError atılır.
## B
`OutOfMemoryError: Java heap space`; çünkü her özyineleme adımı heap'i doldurur
🤖 Özyineleme çerçeveleri stack'i doldurur, heap'i değil; hata StackOverflowError'dır.
## C
`NullPointerException`; çok derin çağrılarda referanslar boşa (null) düşer
🤖 Boş referansla ilgisi yok; sorun yığıt derinliğinin aşılmasıdır.
## D
Hiçbir hata; JVM belli bir derinlikten sonra özyinelemeyi kendiliğinden durdurur
🤖 Otomatik durdurma yoktur; yığıt dolunca StackOverflowError atılır.
## Doğru
A
## Açıklama
Her metot çağrısı, iş parçacığının **yığıtına (stack)** yeni bir **çerçeve (frame)** ekler; çerçeve yerel değişkenleri ve dönüş bağlamını tutar. Sonsuz (ya da çok derin) özyineleme çerçeveleri biriktirir ve yığıt sınırı (`-Xss` ile ayarlanabilir) aşılınca JVM **`StackOverflowError`** atar. Bu hata **stack** kaynaklıdır, heap değil; bu yüzden B (heap OOM) yanlıştır. C (`NullPointerException`) tamamen ilgisizdir; D ise JVM'in özyinelemeyi sessizce durdurduğunu varsayar — böyle bir otomatik durdurma yoktur. Heap'in dolması ise ayrı bir senaryodur ve `OutOfMemoryError: Java heap space` üretir.

# jvmmem-6 | İleri
## Soru
`StackOverflowError` ile `OutOfMemoryError` arasındaki fark en doğru nasıl özetlenir?
## A
İkisi de aynı durumu bildirir; JVM ikisinden birini rastgele seçerek atar
🤖 Farklı alanların tükenmesini gösterirler; rastgele seçim yoktur.
## B
`StackOverflowError` bir ipliğin yığıt (stack) derinliği aşılınca; `OutOfMemoryError` bir bellek alanına yer ayrılamayınca atılır
🤖 Doğru: biri stack derinliği, diğeri bellek alanı tükenmesidir.
## C
`StackOverflowError` yalnız derleme zamanında oluşur; çalışma zamanında görülmez
🤖 Çalışma zamanında oluşur; derleyici böyle bir hata üretmez.
## D
`OutOfMemoryError` yalnız Metaspace tükendiğinde atılır; heap dolduğunda çöp toplayıcı yer açtığı için asla atılmaz
🤖 Heap dahil birçok alan için atılabilir; yalnız Metaspace'e özgü değildir.
## Doğru
B
## Açıklama
İkisi de `Error`'dur ama **farklı kaynakların** tükendiğini bildirir. **`StackOverflowError`**, bir iş parçacığının **yığıt (stack) derinliği** aşıldığında atılır — tipik nedeni çok derin/sonsuz özyinelemedir; her iplik kendi stack'ine sahip olduğundan bu iplik-özeldir. **`OutOfMemoryError`** ise bir **bellek alanı** için yeni yer ayrılamadığında atılır ve alt mesajı hangi alanın tükendiğini söyler: `Java heap space` (heap), `Metaspace`, `Direct buffer memory` (off-heap), `unable to create new native thread` vb. A (aynı/rastgele), C (derleme zamanı) ve D (yalnız Metaspace) yanlıştır; doğru özet **stack derinliği vs. bellek alanı ayırma başarısızlığı** ayrımıdır.
