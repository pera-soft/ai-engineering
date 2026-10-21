---
slug: virtual-threads-loom
baslik: "Virtual Threads (Project Loom)"
kademe: İleri
sira: 56
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir lokanta düşünün: her müşteriye bir garson atanıyor ve garson, müşterinin yemeğini beklerken mutfağın önünde boş boş dikiliyor. Garson tutmak pahalı olduğundan lokanta ancak birkaç yüz kişiye hizmet verebiliyor. Sanal iplikler tam bu israfı bitirir — beklemeye geçen müşteriyi bırakan garson hemen başka masaya koşar. Yazılımda "müşteri" bir istek, "garson" ise işletim sisteminin verdiği pahalı bir iş parçacığıdır. Böylece sade ve okunaklı kodla aynı anda milyonlarca işi yürütebilirsiniz.

### Temel kavramlar

- **İşletim sistemi ipliği (OS thread / iş parçacığı)**: işletim sisteminin tanıdığı "aynı anda ilerleyen iş" birimidir. Her birinin kendi **çağrı yığıtı (stack)** vardır ve bu yığıt tipik olarak ~1 MB yer kaplar; bu yüzden sayıları binlerle sınırlıdır.
- **Bağlam değiştirme (context switch)**: işlemcinin bir iplikten diğerine geçerken o ipliğin durumunu kaydedip ötekinin durumunu yüklemesidir. Bedava değildir; iplik sayısı arttıkça maliyeti büyür.
- **Engelleyici (blocking) işlem**: ağ, veritabanı veya dosya yanıtı gelene dek ipliğin hiçbir şey yapmadan beklediği çağrıdır.
- **Platform iplik (platform thread)**: bir OS ipliğinin ince JVM sarmalayıcısıdır. **1:1** eşleşir, pahalıdır (~1 MB yığın), **işletim sistemi zamanlar** ve pahalı kaynak olduğu için **havuzlanır**.
- **Sanal iplik (virtual thread)**: Java 21'de **kalıcılaşan** (finalized, JEP 444), tamamen **JVM tarafından yönetilen ve JVM tarafından zamanlanan** hafif ipliktir; birkaç yüz bayt yer kaplar ve milyonlarca olabilir.
- **Taşıyıcı iplik (carrier thread)**: sanal ipliği sırtında çalıştıran platform ipliğidir; JDK bunları bir `ForkJoinPool` içinde tutar ve sayıları tipik olarak **çekirdek sayısı kadardır**.
- **mount / unmount**: sanal iplik çalışmak için bir taşıyıcıya **biner (mount)**, engelleyici bir I/O'da **iner (unmount)** ve taşıyıcıyı serbest bırakır. Sanal iplik bir OS ipliğine **kalıcı bağlı değildir**; bu **M:N** eşleme sayesinde bir avuç taşıyıcı milyonlarca sanal ipliği taşır. (İkisi de CPU'da çalışır; GPU ile ilgisi yoktur.)

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sanal iplikler az sayıda taşıyıcı platform ipliği üzerinde mount-unmount ile taşınır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">mount / unmount: milyonlarca sanal iplik, birkaç taşıyıcı</text>
  <rect x="30" y="46" width="580" height="70" rx="10" fill="#EAF2FB" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#2C6BB3" font-family="system-ui">Binlerce sanal iplik (kuyrukta veya I/O beklerken)</text>
  <circle cx="90" cy="94" r="9" fill="#2C6BB3"/><circle cx="130" cy="94" r="9" fill="#2C6BB3"/><circle cx="170" cy="94" r="9" fill="#2C6BB3"/><circle cx="210" cy="94" r="9" fill="#2C6BB3"/><circle cx="250" cy="94" r="9" fill="#9BBEE0"/><circle cx="290" cy="94" r="9" fill="#9BBEE0"/><circle cx="330" cy="94" r="9" fill="#9BBEE0"/><circle cx="370" cy="94" r="9" fill="#9BBEE0"/><circle cx="410" cy="94" r="9" fill="#9BBEE0"/><circle cx="450" cy="94" r="9" fill="#9BBEE0"/><circle cx="490" cy="94" r="9" fill="#9BBEE0"/><circle cx="530" cy="94" r="9" fill="#9BBEE0"/>
  <text x="320" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">mount ↓ (çalışırken)   ·   unmount ↑ (I/O'da bloklanınca taşıyıcı boşalır)</text>
  <rect x="120" y="158" width="120" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="180" y="182" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Taşıyıcı 1</text>
  <text x="180" y="200" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(OS ipliği)</text>
  <rect x="400" y="158" width="120" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="460" y="182" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Taşıyıcı 2</text>
  <text x="460" y="200" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">(OS ipliği)</text>
  <text x="320" y="238" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Taşıyıcı sayısı ≈ CPU çekirdeği; sanal iplik sayısı ≈ sınırsız.</text>
</svg>
```

### Nasıl çalışır?

İplik türü artık açıkça seçilir: `Thread.ofVirtual()` bir **sanal iplik kurucusu (builder)**, `Thread.ofPlatform()` ise platform ipliği kurucusu döndürür; ikisinde de `.name(...)` ve `.start(Runnable)` zinciri kullanılır. `start` normal biçimde bir `Thread` döndürür, istisna fırlatmaz.

```java
Thread t = Thread.ofVirtual().name("isci").start(() -> islemYap());   // sanal iplik
Thread p = Thread.ofPlatform().name("agir").start(() -> islemYap());  // platform iplik
```

`Executors.newVirtualThreadPerTaskExecutor()` sabit boyutlu bir havuz **değildir**; gönderilen **her görev için yeni bir sanal iplik** açar. 10.000 görev gönderirseniz 10.000 sanal iplik doğar — 10.000 OS ipliği değil — ve bunlar birkaç taşıyıcı üzerinde mount/unmount ile çoğullanır. Sabit havuzlu bir executor'daki gibi "çekirdek sayısı kadarı başlar, kalanı kuyrukta bekler" davranışı yoktur; görevler tek bir iplikte sırayla da çalışmaz.

Kritik ilke: sanal iplikleri **havuzlamayın (don't pool)**. Havuzun amacı pahalı bir kaynağı paylaştırmaktır; sanal iplik pahalı değildir, ucuz ve tek-kullanımlıktır. Sayılarını çekirdek sayısına sabitlemek de gereksizdir — o, platform ipliği mantığıdır.

Bir sanal ipliğin ömrü, "bekleme neden ucuz" sorusunun cevabıdır. İplik taşıyıcıya **mount** olup (binip) çalışır. Engelleyici bir I/O'da **unmount** olur, yani taşıyıcıdan iner ve onu serbest bırakır. Taşıyıcı bu boşlukta başka bir sanal ipliğe hizmet eder; I/O bitince iplik **yeniden mount** edilir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sanal iplik yaşam döngüsü: taşıyıcıya mount olur, çalışır, I/O'da unmount olur, taşıyıcı boşalır, iş bitince yeniden mount olur">
  <defs><marker id="vt-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker></defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir sanal ipliğin yaşam döngüsü</text>
  <rect x="24" y="70" width="120" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="84" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">1) mount</text>
  <text x="84" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">taşıyıcıda çalışır</text>
  <rect x="176" y="70" width="128" height="54" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="240" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">2) I/O bloklar</text>
  <text x="240" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">unmount olur</text>
  <rect x="336" y="70" width="128" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="400" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3) taşıyıcı boş</text>
  <text x="400" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">başka ipliğe koşar</text>
  <rect x="496" y="70" width="120" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="556" y="94" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">4) yeniden mount</text>
  <text x="556" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">I/O bitince</text>
  <line x1="144" y1="97" x2="174" y2="97" stroke="#5D7789" stroke-width="1.5" marker-end="url(#vt-arw)"/>
  <line x1="304" y1="97" x2="334" y2="97" stroke="#5D7789" stroke-width="1.5" marker-end="url(#vt-arw)"/>
  <line x1="464" y1="97" x2="494" y2="97" stroke="#5D7789" stroke-width="1.5" marker-end="url(#vt-arw)"/>
  <text x="320" y="160" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Bekleme süresi taşıyıcıyı işgal etmez — ölçeğin sırrı budur.</text>
</svg>
```

İki iplik türünün özellikleri de doğrudan bu modelden çıkar: platform iplik bir OS ipliğiyle 1:1 eşleşir, pahalıdır, OS zamanlar ve havuzlanır; sanal iplik M:N eşlenir, ucuzdur, JVM zamanlar ve görev başına açılır.

```svg
<svg viewBox="0 0 640 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Platform iplik ve sanal iplik karşılaştırması: eşleme, maliyet, sayı, zamanlayıcı ve kullanım kalıbı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Platform iplik vs Sanal iplik</text>
  <rect x="28" y="42" width="288" height="176" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Platform iplik</text>
  <text x="172" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1:1 OS ipliği eşlemesi</text>
  <text x="172" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">~1 MB yığın (pahalı)</text>
  <text x="172" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">binlerle sınırlı</text>
  <text x="172" y="164" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">OS zamanlar</text>
  <text x="172" y="192" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">havuzlanır (pahalı kaynak)</text>
  <rect x="324" y="42" width="288" height="176" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="468" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Sanal iplik</text>
  <text x="468" y="92" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">M:N — taşıyıcıya mount</text>
  <text x="468" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">birkaç yüz bayt (ucuz)</text>
  <text x="468" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">milyonlarca olabilir</text>
  <text x="468" y="164" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">JVM zamanlar</text>
  <text x="468" y="192" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">havuzlanmaz (görev başına)</text>
</svg>
```

### Mülakatta ne sorulur?

- **Temel fark.** Sanal iplik JVM-zamanlıdır ve bir OS ipliğine kalıcı bağlı değildir; çalışırken taşıyıcıya mount, bloklarken unmount olur. "Daha hızlı bir OS ipliği" değildir — bire bir OS eşlemesi platform ipliğin özelliğidir.
- **Havuzlama.** Sanal iplik **havuzlanmaz**. Doğru kalıp görev başına bir ipliktir: `Executors.newVirtualThreadPerTaskExecutor()` ya da `Thread.ofVirtual().start(...)`. Bir `ThreadPoolExecutor` içinde 200 sanal iplik tutmak anti-kalıptır; havuzlama ucuzluk avantajını boşa çıkarır.
- **Hangi iş yükü?** Kazanç **I/O-bağımlı (I/O-bound)** işlerdedir: çok sayıda ağ/veritabanı çağrısında iplik beklerken taşıyıcı boşalır ve "istek başına bir iplik" gibi yalın senkron kod milyonlarca bağlantıya ölçeklenir. **CPU-bağımlı (CPU-bound)** işte (matris çarpımı, bellek içi sıralama) iplikler hiç beklemediğinden unmount fırsatı doğmaz; darboğaz çekirdek sayısıdır, iplik türü değil. Beklemesi olmayan tek-iplikli toplu iş (batch) de eşzamanlılık modelinden etkilenmez.
- **`synchronized` ve pinning.** Sanal iplikler `synchronized` içinde de çalışır. JDK 21–23'te `synchronized` içinde bloklamak taşıyıcıya **pinlerdi**; **JEP 491 ile JDK 24'ten itibaren pinlemez**. Native/JNI çağrıları ve FFM downcall'ları ise **hâlâ pinler**.
- **`ThreadLocal` maliyeti.** `ThreadLocal` sanal iplikte de çalışır (derlenir, istisna atmaz), otomatik olarak paylaşılan tek bir değere dönüşmez ve bedava değildir: her iplik kendi kopyasını tutar. Platform ipliklerde sayı binlerle sınırlı olduğundan maliyet küçüktür; **milyonlarca** sanal iplikte iplik-başı kopyalar ciddi bellek yüküne dönüşür ve çöp toplamayı zorlar. Hafif alternatif, Java 25'te kalıcılaşan **`ScopedValue`**'dur: değiştirilemez (immutable) ve sınırlı ömürlüdür (bounded).

### Tek cümlede

Sanal iplik, engellendiğinde taşıyıcı platform ipliğini serbest bırakabildiği için ucuza milyonlarca olabilen JVM-yönetimli bir ipliktir; havuzlanmaz, görev başına açılır ve asıl faydası I/O-bağımlı işlerdedir.

# vthread-1 | İleri
## Soru
Sanal iplik (virtual thread) ile platform iplik (platform thread) arasındaki temel fark nedir?
## A
Sanal iplik JVM tarafından zamanlanır; çalışırken bir taşıyıcı platform ipliğine mount edilir, bloklandığında unmount olur
🤖 Doğru: JVM-zamanlı, taşıyıcıya mount/unmount olan hafif iplik tanımı budur.
## B
Sanal iplik daha hızlı bir işletim sistemi ipliğidir; her sanal iplik yine bire bir bir OS ipliğine karşılık gelir
🤖 Tam tersi: sanal iplikler OS ipliğine bire bir bağlı değildir; çoktan-aza (M:N) eşlenir.
## C
Sanal iplik yalnız GPU üzerinde çalışır, platform iplik CPU'da çalışır
🤖 GPU ile ilgisi yoktur; ikisi de CPU'da, taşıyıcı ipliği üzerinden çalışır.
## D
Sanal iplik `synchronized` bloklarında çalışamaz, platform iplik çalışabilir
🤖 Sanal iplikler `synchronized` içinde de çalışır; JDK 24'ten itibaren `synchronized` kaynaklı pinning de kalkmıştır.
## Doğru
A
## Açıklama
Sanal iplik, işletim sistemi ipliğinin ince bir sarmalayıcısı OLAN platform ipliğinin aksine, tamamen **JVM tarafından yönetilen** hafif bir ipliktir. Çalışabilmek için bir **taşıyıcı ipliğe (carrier thread)** — ForkJoinPool içindeki bir platform ipliğine — **mount** edilir; engelleyici bir işlemde (ör. soket okuması) **unmount** olarak taşıyıcıyı serbest bırakır ve iş bekleyen başka bir sanal iplik o taşıyıcıya binebilir. Bu M:N eşleme sayesinde milyonlarca sanal iplik, çekirdek sayısı kadar taşıyıcıyla taşınır. B yanlış çünkü bire bir OS eşlemesi platform ipliğin özelliğidir; C ve D kavram hatasıdır.

# vthread-2 | İleri
## Soru
Sanal iplikler için doğru kullanım kalıbı hangisidir?
## A
Performans için bir `ThreadPoolExecutor` içinde 200 sanal iplik havuzlamak
🤖 Sanal iplikler havuzlanmaz; havuzlama onların ucuzluk avantajını boşa çıkarır.
## B
Her görev için yeni bir sanal iplik açmak (ör. `Executors.newVirtualThreadPerTaskExecutor()`), havuzlamamak
🤖 Doğru: sanal iplik ucuz ve tek-kullanımlıktır; görev başına bir tane açılır.
## C
Sanal iplikleri yalnız CPU-bağımlı ağır hesaplamalar için kullanmak
🤖 CPU-bağımlı işte fayda yok; darboğaz çekirdek sayısıdır, iplik türü değil.
## D
Sanal iplik sayısını çekirdek sayısıyla sınırlamak
🤖 Bu platform ipliği mantığıdır; sanal iplikte sayıyı çekirdeğe bağlamak gereksizdir.
## Doğru
B
## Açıklama
Sanal ipliklerin tüm cazibesi **ucuz ve bol** olmalarıdır; oluşturmak neredeyse bedavadır ve engellendiklerinde taşıyıcıyı bırakırlar. Bu yüzden onları bir havuzda "yeniden kullanmak" (A) anti-kalıptır — havuzun amacı pahalı kaynağı paylaşmaktır, oysa sanal iplik pahalı değildir. Doğru kalıp **görev başına bir sanal iplik**tir: `Executors.newVirtualThreadPerTaskExecutor()` veya doğrudan `Thread.ofVirtual().start(...)`. Sayıyı çekirdeğe sabitlemek (D) de gereksizdir; asıl fayda I/O beklerken binlerce ipliğin ucuza var olabilmesidir. CPU-bağımlı iş (C) darboğazı çekirdek olduğundan iplik türünden bağımsız kalır.

# vthread-3 | İleri
## Soru
Sanal iplikler en çok hangi tür iş yükünde kazanç sağlar?
## A
Sürekli matris çarpımı yapan CPU-bağımlı bir hesaplamada
🤖 CPU-bağımlı işte kazanç yok; iplikler beklemediği için taşıyıcı hiç boşalmaz.
## B
Tek iş parçacıklı, hiç beklemesi olmayan bir toplu iş (batch) döngüsünde
🤖 Beklemesiz tek-iplik işte eşzamanlılık modeli fark yaratmaz.
## C
Çok sayıda ağ/veritabanı çağrısında bloklayan, I/O-bağımlı (I/O-bound) isteklerde
🤖 Doğru: I/O beklerken sanal iplik unmount olur, taşıyıcı başka işe koşar.
## D
Bellek içi sıralama (in-memory sort) yapan saf hesap yükünde
🤖 Bu da CPU-bağımlıdır; beklemesi olmadığından sanal iplik ek fayda vermez.
## Doğru
C
## Açıklama
Sanal ipliklerin kilit kazancı, iş **engelleyici I/O**'da beklerken taşıyıcıyı serbest bırakabilmeleridir. Binlerce eşzamanlı ağ/veritabanı çağrısı yapan bir sunucuda, her istek kendi sanal ipliğinde bloklarken taşıyıcı platform iplikleri başka isteklere hizmet eder — böylece "iplik başına bir istek" gibi yalın, senkron kod milyonlarca eşzamanlı bağlantıya ölçeklenir (I/O-bound). CPU-bağımlı işlerde (A, D) iplikler hiç beklemediğinden unmount fırsatı doğmaz; darboğaz çekirdek sayısıdır ve sanal iplik ek fayda getirmez. Beklemesiz tek-iplik iş (B) zaten eşzamanlılıktan etkilenmez.

# vthread-4 | Uzman
## Soru
Sanal ipliklerle ilgili `ThreadLocal` kullanımının maliyeti nedir?
## A
`ThreadLocal` sanal ipliklerde hiç desteklenmez; kullanılırsa kod derlenmez ve hata verir
🤖 Çalışır ve derlenir; sorun yasak değil, ölçekteki bellek maliyetidir.
## B
`ThreadLocal` sanal iplikte otomatik olarak tüm iplikler arasında paylaşılan tek bir değere dönüşür
🤖 Hayır; her sanal iplik yine kendi değerini tutar, otomatik paylaşım olmaz.
## C
`ThreadLocal` yalnız platform ipliklerde bellek harcar; sanal iplikte kopya tutulmadığı için maliyeti sıfırdır
🤖 Tam tersi; milyon sanal iplik varsa milyon kopya birikir, bedava değildir.
## D
İplik başına tutulan `ThreadLocal` kopyaları, milyonlarca sanal iplikte büyük bir bellek yüküne dönüşebilir
🤖 Doğru: ölçek büyüdükçe iplik-başı kopyalar birikir; hafif alternatif `ScopedValue`'dur.
## Doğru
D
## Açıklama
`ThreadLocal` her iplik için ayrı bir değer kopyası tutar. Platform ipliklerde sayı binlerle sınırlı olduğundan bu maliyet küçük kalır; ama sanal iplikler **milyonlarca** olabildiğinden, her birine takılan `ThreadLocal` girdisi ciddi bir bellek yüküne dönüşebilir ve çöp toplamayı zorlar. Ayrıca sanal iplikler kısa ömürlü ve çok sayıda olduğu için "iplik başına bağlam" fikri ölçeklenmez. Java 25'te kalıcılaşan **`ScopedValue`**, değiştirilemez (immutable) ve sınırlı ömürlü (bounded) bir bağlam taşıma mekanizmasıdır ve bu senaryoda `ThreadLocal`'a hafif alternatiftir. A, B, C kavram hatasıdır: `ThreadLocal` çalışır, paylaşımlıya dönmez ve bedava değildir.

# vthread-5 | İleri
## Soru
`Executors.newVirtualThreadPerTaskExecutor()` ile 10.000 görev gönderirseniz ne olur?
## A
Her görev kendi sanal ipliğinde çalışır; bunlar az sayıda taşıyıcı platform ipliği üzerinde mount/unmount edilerek yürütülür
🤖 Doğru: görev başına bir sanal iplik, birkaç taşıyıcı üzerinde çoğullanır.
## B
Yalnız işlemci çekirdeği kadar (ör. 8) görev aynı anda başlar; kalanlar sırası gelene dek kuyrukta bekler
🤖 Bu sabit havuzlu executor davranışıdır; sanal-iplik-per-görev'de her göreve iplik açılır.
## C
10.000 işletim sistemi ipliği oluşur ve sistem büyük olasılıkla belleği tüketir
🤖 Sanal iplikler OS ipliği değildir; 10.000 OS ipliği oluşmaz.
## D
Görevler tek bir platform ipliğinde sırayla, tek tek çalışır
🤖 Seri değil; her görev kendi sanal ipliğinde eşzamanlı ilerler.
## Doğru
A
## Açıklama
`newVirtualThreadPerTaskExecutor()`, gönderilen **her görev için yeni bir sanal iplik** açar — sabit boyutlu bir havuz değildir. 10.000 görev → 10.000 sanal iplik; bunlar ForkJoinPool tabanlı küçük bir **taşıyıcı** kümesi (tipik olarak çekirdek sayısı kadar platform ipliği) üzerinde mount/unmount edilerek çoğullanır. I/O'da bloklayan görevler taşıyıcılarını bırakır, böylece 10.000 iş birkaç OS ipliğiyle eşzamanlı ilerler. B sabit-havuz davranışını (yanlışlıkla) tarif eder; C sanal ipliği OS ipliğiyle karıştırır; D seri yürütme varsayar — hiçbiri doğru değildir.

# vthread-6 | Orta
## Soru
Aşağıdaki kod hangi tür iplik oluşturur?
```java
Thread t = Thread.ofVirtual().name("isci").start(() -> islemYap());
```
## A
Yeni bir işletim sistemi ipliği; `ofVirtual()` yalnız isim verir
🤖 `ofVirtual()` bir OS ipliği değil, sanal iplik kurucusu döndürür.
## B
Bir sanal iplik (virtual thread); `Thread.ofVirtual()` sanal iplik kurucusunu (builder) döndürür
🤖 Doğru: `ofVirtual()` sanal iplik builder'ıdır, `start` sanal ipliği başlatır.
## C
Bir daemon platform ipliği
🤖 Bu bir platform ipliği değildir; `ofPlatform()` olsaydı platform ipliği olurdu.
## D
Hiçbir iplik; `start` çağrısı bir istisna (exception) fırlatır
🤖 İstisna fırlatmaz; sanal iplik normal biçimde başlar.
## Doğru
B
## Açıklama
`Thread.ofVirtual()`, Java 21'de kalıcılaşan API'de bir **sanal iplik kurucusu (builder)** döndürür; `.name(...)` ipliğe ad verir ve `.start(Runnable)` sanal ipliği başlatıp bir `Thread` nesnesi döndürür. Platform ipliği istenseydi `Thread.ofPlatform()` kullanılırdı (C yanlış). `ofVirtual()` yalnız isimlendirme yapmaz (A yanlış) ve `start` normal biçimde çalışır, istisna fırlatmaz (D yanlış). Bu builder API'si, `new Thread(...)`'a kıyasla iplik türünü ve niteliklerini açıkça seçmenin modern yoludur.
