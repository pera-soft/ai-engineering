---
slug: jep491-pinning
baslik: "Pinning ve JEP 491 (JDK 24)"
kademe: Uzman
sira: 57
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Lokanta benzetmesine dönelim: sanal ipliğin bütün marifeti, beklemeye geçtiğinde garsonu (taşıyıcıyı) serbest bırakmasıydı. Peki garson masaya **kelepçelenirse** ne olur? Müşteri beklerken garson da orada çakılı kalır. Elinizde yalnız birkaç garson olduğundan, birkaç kelepçe bütün lokantayı kilitler. İşte **pinning (sabitlenme)** budur; hangi durumlarda oluştuğunu bilmek, sanal iplik kullanan her ekibin işidir.

### Temel kavramlar

- **Pinning (sabitlenme)**: bir sanal ipliğin engelleyici bir işlemde beklerken taşıyıcı platform ipliğinden **unmount olamaması**, yani onu **işgal etmeye devam etmesidir**. Nesnenin bellekte sabit bir adrese çakılması (GC), ipliğin belirli bir çekirdeğe bağlanması (CPU affinity) ya da önceliğinin (priority) sabitlenmesi **değildir**.
- **Monitör (monitor)**: `synchronized` blok/metodun arkasındaki JVM kilididir. Her nesnenin bir monitörü vardır; bloğa giren iplik onu sahiplenir, çıkarken bırakır.
- **`ReentrantLock`**: `java.util.concurrent` içindeki, sıradan Java kodu olarak yazılmış kilit. Sanal-iplik dostudur — beklerken unmount'a izin verir, **pinlemez**.
- **Native/JNI çağrısı**: JVM'in dışına, C/C++ gibi yerel (native) koda yapılan çağrı. **FFM (Foreign Function & Memory) downcall'ı** ise aynı işin modern, JNI'siz yoludur.
- **Açlık (starvation) / kilitlenme (deadlock)**: taşıyıcılar tükendiği için kuyruktaki sanal ipliklerin hiç sıra alamaması.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JDK 24 öncesi synchronized pinlerdi, JDK 24 ile synchronized artık pinlemez; native ve FFM hâlâ pinler">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pinning'in durumu: JDK 24 (JEP 491) öncesi ve sonrası</text>
  <rect x="28" y="46" width="288" height="150" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="172" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#B4322B" font-family="system-ui">JDK 21–23</text>
  <text x="172" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">synchronized + I/O → PİN</text>
  <text x="172" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">native/JNI çağrısı → PİN</text>
  <text x="172" y="140" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">FFM downcall → PİN</text>
  <text x="172" y="170" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">öneri: ReentrantLock'a geç</text>
  <rect x="324" y="46" width="288" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="468" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">JDK 24+ (JEP 491)</text>
  <text x="468" y="96" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">synchronized + I/O → PİN YOK ✓</text>
  <text x="468" y="118" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">native/JNI çağrısı → HÂLÂ PİN</text>
  <text x="468" y="140" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">FFM downcall → HÂLÂ PİN</text>
  <text x="468" y="170" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">ReentrantLock göçü artık şart değil</text>
  <text x="320" y="222" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">synchronized düzeldi; native/FFM sınırı devam ediyor.</text>
</svg>
```

### Nasıl çalışır?

JDK 21–23'te en yaygın pinning kaynağı **`synchronized`** içinde engellenmekti: bir sanal iplik `synchronized` blok/metot içindeyken I/O'da bloklarsa taşıyıcıya **pinlenirdi**, çünkü JVM monitör sahipliğini ipliğin taşıyıcısına bağlıyordu. Bu yüzden erken tavsiye, sıcak yollarda `synchronized` yerine `ReentrantLock` kullanmaktı.

**JEP 491 (JDK 24, Mart 2025)** monitör sahipliğini unmount ile uyumlu hâle getirdi: sanal iplik `synchronized` içindeyken bile taşıyıcıdan normal biçimde ayrılabiliyor, yani **artık pinlemiyor**. `synchronized` kaldırılmadı, yasaklanmadı; yalnız bu davranış düzeltildi ve `ReentrantLock`'a çevirme zorunluluğu **ortadan kalktı**.

Ama pinning tümüyle bitmedi: **native/JNI çağrıları ve FFM downcall'ları** sırasında sanal iplik **hâlâ** pinlenir. Nedeni yapısaldır — iplik yabancı (Java olmayan) bir yığın çerçevesinin (native frame) üzerindeyken JVM onu güvenle askıya alıp sonra devam ettiremez.

Pinning'in neden bu kadar tehlikeli olduğu taşıyıcı sayısının azlığından gelir (tipik olarak çekirdek kadar): pinlenmiş iplikler taşıyıcıları işgal edince kuyrukta bekleyen sanal iplikler aç kalır (starvation), hatta kilitlenme görülebilir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Pinlenmiş sanal iplikler taşıyıcıları işgal eder, kuyrukta bekleyen sanal iplikler taşıyıcı bulamayıp aç kalır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pinning neden ölçeği çökertir</text>
  <rect x="40" y="44" width="220" height="60" rx="10" fill="#F5DADA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="150" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Taşıyıcı 1 — PİNLİ</text>
  <text x="150" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bloklu iplik bırakamıyor</text>
  <rect x="40" y="118" width="220" height="60" rx="10" fill="#F5DADA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="150" y="144" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Taşıyıcı 2 — PİNLİ</text>
  <text x="150" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">taşıyıcı boşalmıyor</text>
  <rect x="300" y="44" width="300" height="134" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="450" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kuyrukta bekleyen sanal iplikler</text>
  <circle cx="330" cy="100" r="8" fill="#5D7789"/><circle cx="362" cy="100" r="8" fill="#5D7789"/><circle cx="394" cy="100" r="8" fill="#5D7789"/><circle cx="426" cy="100" r="8" fill="#5D7789"/><circle cx="458" cy="100" r="8" fill="#5D7789"/><circle cx="490" cy="100" r="8" fill="#5D7789"/><circle cx="522" cy="100" r="8" fill="#5D7789"/><circle cx="554" cy="100" r="8" fill="#5D7789"/>
  <text x="450" y="150" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">hiçbiri taşıyıcı bulamıyor → starvation / deadlock</text>
</svg>
```

Peki pinning'i pratikte nasıl yakalarsınız? Salt CPU grafiğine bakmak yanıltıcıdır: pinlenmiş iplikler beklerken CPU %100 olmayabilir — asıl sorun taşıyıcıların tükenmesidir. Tek seferlik bir `jstack` dökümü de pinlenen yolu güvenilir biçimde göstermez. Doğru araç **JDK Flight Recorder (JFR)** olay akışıdır: yük altında kayıt açılır ve `jdk.VirtualThreadPinned` olayı, bir sanal ipliğin pinlendiği andaki **çağrı yığınını (stack trace)** kaydeder; böylece pinleyen native/FFM çağrısını doğrudan görürsünüz. Eski JDK'lerdeki `-Djdk.tracePinnedThreads=full` bayrağı yerine bugün bu JFR olayı tercih edilir. `-Xmx` gibi bellek ayarlarını büyütmek hiçbir işe yaramaz; bu bir eşzamanlılık sorunudur, bellek sorunu değil.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Pinning teşhisi: JFR kaydı jdk.VirtualThreadPinned olayını yakalar, çağrı yığını pinleyen native veya FFM çağrısını gösterir">
  <defs><marker id="pin-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker></defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pinning'i teşhis etmek (JFR)</text>
  <rect x="24" y="60" width="150" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">JFR kaydı aç</text>
  <text x="99" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yük altında çalıştır</text>
  <rect x="214" y="60" width="180" height="60" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="304" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">jdk.VirtualThreadPinned</text>
  <text x="304" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">olayı + çağrı yığını</text>
  <rect x="434" y="60" width="182" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Nedeni bul</text>
  <text x="525" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">native / FFM çağrısı</text>
  <line x1="174" y1="90" x2="212" y2="90" stroke="#5D7789" stroke-width="1.5" marker-end="url(#pin-arw)"/>
  <line x1="394" y1="90" x2="432" y2="90" stroke="#5D7789" stroke-width="1.5" marker-end="url(#pin-arw)"/>
  <text x="320" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Eski -Djdk.tracePinnedThreads bayrağı yerine JFR olayı tercih edilir.</text>
</svg>
```

### Mülakatta ne sorulur?

- **Tanım.** Pinning = engelleyici işlemde taşıyıcıdan unmount olamamak. GC'nin nesneyi bellekte sabitlemesi, CPU affinity ve iplik önceliği farklı kavramlardır.
- **JEP 491 ne değiştirdi?** JDK 24'ten itibaren `synchronized` içinde bloklamak **pinlemez**; iplik taşıyıcıdan normal biçimde unmount olur. "Hiçbir şey değişmedi", "`synchronized` kaldırıldı" ya da "daha çok pinlediği için yasaklandı" cevapları yanlıştır.
- **Hâlâ ne pinler?** Yalnız **native/JNI** çağrıları ve **FFM downcall'ları**. `ReentrantLock.lock()` pinlemez; `synchronized` metot içindeki `sleep` de JDK 24'ten sonra pinlemez; engelleyici soket okuması (blocking socket read) ise zaten sanal-iplik farkındalıklıdır — orası tam olarak unmount'un gerçekleştiği yerdir.
- **Sürüm farkı sorusu.** `synchronized (kilit) { httpClient.send(...); }` kodu JDK 23'te de JDK 24'te de **derlenir** ve **istisna fırlatmaz**; fark tamamen çalışma zamanındadır: JDK 23 pinler, JDK 24 pinlemez.
- **Göç gerekli mi?** JDK 24'e geçen bir ekip için **yalnızca pinning'den kaçınmak amacıyla** yapılacak `synchronized → ReentrantLock` göçü artık **şart değildir**; mevcut kod olduğu gibi kalabilir. (`tryLock`, zaman aşımı, adillik/fairness gibi özellikler için `ReentrantLock` hâlâ değerli olabilir — ama bunlar pinning'den bağımsız gerekçelerdir. "Her durumda 10 kat hız" iddiası mesnetsizdir; `ReentrantLock` pinlediği için geri `synchronized`'a dönmek de gerekmez.)
- **Teşhis.** JFR'nin `jdk.VirtualThreadPinned` olayı. CPU grafiğine bakmak, `-Xmx` artırmak ve tek seferlik `jstack` almak yanlış yollardır.

### Tek cümlede

Pinning, sanal ipliğin beklerken taşıyıcısını bırakamayıp ölçeği çökertmesidir; JDK 24 (JEP 491) `synchronized` kaynaklı pinlemeyi bitirdi, native/JNI ve FFM sınırı ise sürüyor ve teşhis aracı JFR'nin `jdk.VirtualThreadPinned` olayıdır.

# pin-1 | Uzman
## Soru
Sanal iplik bağlamında "pinning" (sabitlenme) nedir?
## A
Sanal ipliğin çöp toplayıcı tarafından bellekte sabit bir adrese sabitlenip taşınamaz hâle gelmesi
🤖 Pinning bir GC/bellek adresi olayı değil, taşıyıcıdan ayrılamama olayıdır.
## B
Sanal ipliğin belirli bir CPU çekirdeğine bağlanıp (CPU affinity) yalnız o çekirdekte çalışması
🤖 CPU affinity ayrı bir kavramdır; pinning taşıyıcı ipliğe takılıp kalmaktır.
## C
Sanal ipliğin engelleyici bir işlemde beklerken taşıyıcı platform ipliğinden unmount olamayıp onu işgal etmesi
🤖 Doğru: bloklarken taşıyıcıyı bırakamama pinning'dir.
## D
Sanal ipliğin öncelik (priority) değerinin sabitlenip çalışma boyunca değiştirilemez olması
🤖 Öncelikle ilgisi yok; pinning taşıyıcıdan ayrılamamaktır.
## Doğru
C
## Açıklama
Sanal ipliğin sağladığı kazanç, engelleyici bir işlemde beklerken taşıyıcı platform ipliğinden **unmount** olup onu başka işe bırakabilmesidir. **Pinning**, bu ayrılmanın olamadığı, yani sanal ipliğin beklerken taşıyıcıyı **işgal etmeye devam ettiği** durumdur. Taşıyıcı sayısı az olduğundan (tipik olarak çekirdek sayısı kadar), pinlenmiş iplikler taşıyıcıları tüketirse tüm sanal-iplik havuzu tıkanır ve hatta kilitlenme (starvation/deadlock) görülebilir. A (GC/bellek adresi), B (CPU affinity) ve D (öncelik) farklı kavramlardır; pinning yalnızca "taşıyıcıdan ayrılamama" anlamına gelir.

# pin-2 | Uzman
## Soru
JDK 24 ve JEP 491 ile `synchronized` bloklarındaki pinning konusunda ne değişti?
## A
`synchronized` tamamen kaldırıldı; artık yalnız `ReentrantLock` derlenir
🤖 `synchronized` kaldırılmadı; yalnızca pinleme davranışı düzeltildi.
## B
`synchronized` artık daha çok pinliyor, bu yüzden kullanımı yasaklandı
🤖 Tam tersi; JEP 491 pinlemeyi kaldırdı, yasak yok.
## C
Değişiklik yok; `synchronized` JDK 24'te de eskisi gibi pinlemeye devam ediyor
🤖 Yanlış; asıl haber JDK 24'te bu pinlemenin düzeltilmesidir.
## D
Sanal iplik `synchronized` içinde I/O'da bloklarsa artık pinlemiyor; taşıyıcıdan normal biçimde unmount olabiliyor
🤖 Doğru: JEP 491 monitör sahipliğini unmount ile uyumlu yaptı.
## Doğru
D
## Açıklama
JDK 21–23'te bir sanal iplik `synchronized` blok/metot içindeyken I/O'da bloklarsa taşıyıcıya **pinlenirdi**; bu, `synchronized`'ı ağır I/O yollarında `ReentrantLock`'a çevirmenin başlıca gerekçesiydi. **JEP 491 (JDK 24)** JVM'in monitör (monitor) sahipliğini sanal iplik unmount olurken doğru yönetmesini sağladı; artık `synchronized` içinde bloklamak **pinlemiyor** ve iplik taşıyıcıdan normal biçimde ayrılabiliyor. `synchronized` kaldırılmadı (A) ve yasaklanmadı (B); "hiçbir şey değişmedi" (C) de yanlıştır — değişiklik tam olarak bu pinlemenin ortadan kalkmasıdır.

# pin-3 | Uzman
## Soru
JDK 24'ten sonra sanal ipliklerde pinning HÂLÂ hangi durumda oluşur?
## A
Bir native/JNI çağrısı veya FFM (Foreign Function & Memory) downcall'ı yürütülürken
🤖 Doğru: yabancı yığın çerçevesi üzerinden güvenli unmount yapılamadığından pin sürer.
## B
Bir `synchronized` metot içinde `sleep` çağrıldığında
🤖 Bu JDK 24 ile düzeldi; `synchronized` içinde bloklamak artık pinlemez.
## C
Basit bir `ReentrantLock.lock()` çağrısında
🤖 `ReentrantLock` zaten sanal-iplik-dostudur; pinlemez.
## D
Herhangi bir engelleyici soket okumasında (blocking socket read)
🤖 Sanal iplik farkındalıklı I/O tam da unmount noktasıdır; pinlemez.
## Doğru
A
## Açıklama
JEP 491 `synchronized` pinlemesini çözse de pinning tamamen bitmedi: **native/JNI çağrıları** ve **FFM (Foreign Function & Memory) downcall'ları** sırasında sanal iplik hâlâ pinlenir. Bunun nedeni, yabancı (Java olmayan) yığın çerçevesi üzerindeyken JVM'in ipliği güvenle askıya alıp devam ettirememesidir. B artık düzelen `synchronized` durumudur; C `ReentrantLock` zaten unmount'a izin verdiği için pinlemez; D ise sanal-iplik-farkındalıklı I/O olduğundan tam da unmount'un gerçekleştiği yerdir. Dolayısıyla kalan tek gerçek sınır native/FFM sınırıdır.

# pin-4 | İleri
## Soru
Uygulamanızda pinning olup olmadığını nasıl teşhis edersiniz?
## A
Yalnız CPU kullanımına bakmak yeterlidir; pinning her zaman %100 CPU demektir
🤖 Pinning mutlaka CPU'yu doldurmaz; çoğu zaman iplikler beklerken taşıyıcılar tükenir.
## B
JDK Flight Recorder'ın `jdk.VirtualThreadPinned` olayını (eski flag `jdk.tracePinnedThreads` de vardı) izleyerek
🤖 Doğru: JFR'deki pinned olayı pinlenen çağrı yığınını gösterir.
## C
`-Xmx` değerini artırıp pinning'in kaybolmasını beklemek
🤖 Yığın boyutu pinning'i etkilemez; bu bir eşzamanlılık sorunudur, bellek değil.
## D
Yalnız `jstack` ile tek seferlik bir iplik dökümü almak; pinning orada net görünür
🤖 Anlık döküm pinlenen çağrı yolunu güvenilir göstermez; JFR olayı asıl teşhis aracıdır.
## Doğru
B
## Açıklama
Pinning'in doğru teşhisi **JDK Flight Recorder (JFR)** üzerindendir: `jdk.VirtualThreadPinned` olayı, bir sanal ipliğin pinlendiği yerdeki çağrı yığınını (stack trace) yakalar; böylece hangi `synchronized`/native/FFM çağrısının pinlediğini görürsünüz. (Eski JDK'lerde `-Djdk.tracePinnedThreads=full` bayrağı da vardı ama artık JFR olayı tercih edilir.) Salt CPU kullanımı (A) yanıltıcıdır çünkü pinlenmiş iplikler beklerken CPU %100 olmayabilir — sorun taşıyıcıların tükenmesidir. Yığın boyutunu artırmak (C) bir eşzamanlılık sorununu çözmez. Tek seferlik `jstack` (D) pinlenen yolu güvenilir yakalamaz; sürekli JFR kaydı gerekir.

# pin-5 | Uzman
## Soru
JDK 24'e (JEP 491) geçen bir ekip için `synchronized` → `ReentrantLock` göçü hakkında ne söylenebilir?
## A
Göç artık zorunludur; JDK 24'te `synchronized` sanal ipliklerle hiç çalışmaz
🤖 Yanlış; `synchronized` çalışır ve artık pinlemez, göç zorunlu değildir.
## B
Göç her durumda performansı 10 kat artırır, bu yüzden şarttır
🤖 Abartılı ve yanlış; pinleme düzeldiği için asıl gerekçe ortadan kalktı.
## C
`synchronized` artık pinlemediği için, yalnız pinning'den kaçınmak amacıyla yapılan `ReentrantLock` göçü artık şart değildir
🤖 Doğru: JEP 491'den sonra pinning gerekçeli göç gereksizleşti.
## D
`ReentrantLock` sanal ipliklerde pinlediğinden geri `synchronized`'a dönmek gerekir
🤖 `ReentrantLock` pinlemez; böyle bir geri dönüş gerekçesi yoktur.
## Doğru
C
## Açıklama
JDK 21–23'te `synchronized` I/O'da pinlediği için, sıcak yollarda `synchronized`'ı sanal-iplik-dostu `ReentrantLock`'a çevirmek yaygın bir öneriydi. **JEP 491 (JDK 24)** bu pinlemeyi ortadan kaldırdığından, **yalnızca pinning'i önlemek amacıyla** yapılan `ReentrantLock` göçü artık **şart değildir** — mevcut `synchronized` kodu olduğu gibi bırakılabilir. (`ReentrantLock`'un tryLock/timeout/fairness gibi başka özellikleri hâlâ değerli olabilir; ama bunlar pinning'den bağımsız gerekçelerdir.) A ("hiç çalışmaz") ve D ("ReentrantLock pinler") kavram hatasıdır; B ise mesnetsiz bir performans iddiasıdır.

# pin-6 | Uzman
## Soru
Aşağıdaki kod JDK 23 ile JDK 24'te farklı davranır. Fark nedir?
```java
synchronized (kilit) {
    var yanit = httpClient.send(istek, ...); // I/O'da bloklar
}
```
## A
JDK 23'te `synchronized` içindeki bloklayan çağrı derleme hatası verir, JDK 24'te derlenir
🤖 İkisinde de derlenir; fark çalışma-zamanı pinleme davranışındadır.
## B
JDK 24'te pinleme yasaklandığı için bir istisna (exception) fırlatır, JDK 23'te fırlatmaz
🤖 İstisna yok; fark yalnız pinleme davranışıdır.
## C
İki sürümde de tamamen aynı davranır; JEP 491 çalışma zamanını etkilemediği için hiçbir fark yoktur
🤖 Fark vardır: JDK 23 pinler, JDK 24 pinlemez.
## D
JDK 23'te `send` I/O'da bloklarken taşıyıcı platform ipliğine pinlenir; JDK 24'te (JEP 491) pinlenmeden unmount olur
🤖 Doğru: aynı kod JDK 23'te pinler, JDK 24'te pinlemez.
## Doğru
D
## Açıklama
Kod her iki sürümde de **derlenir ve istisna fırlatmaz** (A, B yanlış). Fark tamamen **çalışma-zamanı pinleme davranışındadır**: JDK 23'te sanal iplik `synchronized` blok içinde `httpClient.send` ile I/O'da bloklarken taşıyıcı platform ipliğine **pinlenir** ve o taşıyıcı meşgul kalır. **JDK 24**'te JEP 491 sayesinde aynı iplik `synchronized` içinde olmasına rağmen taşıyıcıdan **unmount** olabilir, yani **pinlemez** ve taşıyıcı başka işlere hizmet eder. Bu yüzden "hiçbir fark yok" (C) yanlıştır — davranış farkı tam olarak pinlemenin kalkmasıdır.
