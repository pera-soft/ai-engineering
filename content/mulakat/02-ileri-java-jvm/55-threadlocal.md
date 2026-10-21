---
slug: threadlocal
baslik: "ThreadLocal, Bellek Sızıntısı ve ScopedValue"
kademe: İleri
sira: 55
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sınıfta tek sözlük varsa herkes sırayla kullanır ve sayfalar karışır; her öğrenciye kendi sözlüğü verilirse kimse kimsenin sayfasını çevirmez. `ThreadLocal` budur: aynı değişkeni kullanan her iş parçacığına (thread) o değişkenin KENDİ, izole kopyasını verir. Çözdüğü problem, paylaşılan veriyi kilitlerle korumanın karmaşıklığıdır — kilitle uğraşmak yerine "hiç paylaşma" der. Bedeli şudur: sözlükler ödünç verilip geri alınıyorsa (thread havuzu), silmeyi unutunca bir sonraki öğrenci eski notlarla karşılaşır.

### Temel kavramlar

- **İş parçacığı-yerel (thread-local)**: Değerin thread başına ayrı tutulması; bir thread'in yazdığını başka thread GÖREMEZ.
- **Thread havuzu (thread pool)**: Thread'lerin bir kez kurulup görevden göreve yeniden kullanıldığı yapı; thread'ler UZUN yaşar.
- **Güçlü referans (strong reference)**: Nesneyi canlı tutan, çöp toplayıcının (GC) toplamasını engelleyen bağ.
- **Zayıf referans (weak reference)**: Nesneyi canlı TUTMAYAN bağ; başka güçlü bağ kalmazsa nesne toplanabilir.
- **Bellek sızıntısı (memory leak)**: Artık kullanılmayan nesnelerin bir zincirle canlı tutulup belleği doldurması.

### Nasıl çalışır?

`ThreadLocal<T>` nesnesinin kendisi paylaşılır; ama `get()`/`set()` çağıran her thread yalnız KENDİ değerini görür. Böylece paylaşımsız (kilitsiz) bir thread-başına-durum elde edilir. Bu bir KİLİT değildir, `static` bir alan da değildir — `static` tüm thread'ler için TEK değer tutar.

Tipik kullanım iki başlıkta toplanır. (1) Thread-safe OLMAYAN ve yeniden yaratılması pahalı nesneleri thread başına tutmak. Klasik örnek `SimpleDateFormat`'tır: formatlama sırasında dahili durumu bozulduğu için paylaşılamaz. (2) Bir isteğin bağlamını katmanlar arasında parametre geçmeden taşımak; giriş yapan kullanıcı, transaction ya da MDC/izleme kimliği (trace-id) böyledir. Buna karşılık paylaşılan bir sayaç `AtomicInteger` işidir, thread'ler arası veri iletimi `BlockingQueue` işidir, karşılıklı dışlama (mutual exclusion) ise kilit işidir — hiçbiri `ThreadLocal`'ın alanı değildir.

**En büyük tehlike — thread havuzunda bellek sızıntısı**: İçsel olarak her thread'in bir `ThreadLocalMap`'i vardır; anahtar `ThreadLocal`'a ZAYIF (weak) referanstır ama DEĞER GÜÇLÜ tutulur. Yeni thread yaratılıp bitirilen bir modelde thread ölünce haritası da gider; thread HAVUZUNDA ise thread'ler yeniden kullanılmak üzere UZUN yaşar. `remove()` çağırmazsan değer thread'e yapışıp kalır: nesne GC edilemez (sızıntı) ve aynı thread bir sonraki isteğe atandığında ESKİ değeri görüp yanlış/karışık veriyle — ör. önceki kullanıcının bağlamıyla — çalışır; bu bir güvenlik açığı bile olabilir. Kural: kullanımdan sonra **her zaman `finally` içinde `remove()`**.

```java
try {
    BAGLAM.set(istekBaglami);
    isiYap();
} finally {
    BAGLAM.remove();   // tek güvenilir temizlik
}
```

Thread'in havuza dönmesi bir temizlik olayı DEĞİLDİR; GC bunu kurtarmaz, `finalize()` de kurtarmaz (bu iş için ne çağrılır ne güvenilirdir, zaten kullanımdan kaldırılmıştır). Sorun `InheritableThreadLocal`'a özgü de değildir; sıradan `ThreadLocal`'da da aynen olur.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ThreadLocal thread havuzunda sızıntı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Havuz thread'i yaşar → değer temizlenmezse sızar</text>
  <rect x="30" y="44" width="270" height="128" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="165" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B4322B" font-family="system-ui">remove() UNUTULDU</text>
  <text x="165" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">havuz thread'i geri döner,</text>
  <text x="165" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">ThreadLocalMap değeri tutar</text>
  <text x="165" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yeni istek eski değeri görebilir</text>
  <text x="165" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ sızıntı + veri karışması</text>
  <rect x="340" y="44" width="270" height="128" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">try / finally + remove()</text>
  <text x="475" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">iş bitince değer silinir</text>
  <text x="475" y="112" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">thread temiz döner</text>
  <text x="475" y="138" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sızıntı yok</text>
  <text x="475" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">modern alternatif: ScopedValue</text>
</svg>
```

Peki izolasyon nasıl sağlanıyor? Değer, `ThreadLocal` nesnesinin içinde değil, HER THREAD'in kendi `ThreadLocalMap`'inde saklanır; `ThreadLocal` yalnız o haritadaki ANAHTARDIR. Temel fikir budur: aynı `ThreadLocal` nesnesi paylaşılsa da her thread yalnız kendi değerini görür.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ThreadLocal paylaşılan nesne ama her thread kendi izole değerini görür">
  <defs>
    <marker id="tl-a" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#6B3FA0"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ThreadLocal: paylaşılan nesne, thread'e özel değer</text>
  <rect x="228" y="38" width="184" height="44" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="320" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">ThreadLocal&lt;Ctx&gt; ctx</text>
  <text x="320" y="75" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tek, paylaşılan nesne · get()/set()</text>
  <line x1="270" y1="84" x2="122" y2="118" stroke="#6B3FA0" stroke-width="1.5" marker-end="url(#tl-a)"/>
  <line x1="320" y1="84" x2="320" y2="118" stroke="#6B3FA0" stroke-width="1.5" marker-end="url(#tl-a)"/>
  <line x1="370" y1="84" x2="518" y2="118" stroke="#6B3FA0" stroke-width="1.5" marker-end="url(#tl-a)"/>
  <rect x="40" y="122" width="150" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="115" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Thread 1</text>
  <text x="115" y="167" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">get() → "Ali"</text>
  <rect x="245" y="122" width="150" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="320" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Thread 2</text>
  <text x="320" y="167" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">get() → "Ayşe"</text>
  <rect x="450" y="122" width="150" height="62" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="525" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Thread 3</text>
  <text x="525" y="167" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">get() → "Veli"</text>
</svg>
```

Sızıntının kökeni bu haritanın referans yapısıdır: anahtar zayıf, değer güçlü tutulur. Girişler `Entry` olarak saklanır; `Entry`, anahtar olan `ThreadLocal` nesnesine ZAYIF referans verir — amaç, `ThreadLocal`a başka yerden erişilemez olunca anahtarın GC edilebilmesidir. DEĞER ise `Entry` içinde GÜÇLÜ referansla tutulur. Sonuç asimetriktir: anahtar zayıf olsa bile thread (dolayısıyla harita) yaşadıkça değer güçlü zincirle canlı kalır ve otomatik temizlenmez. Yani sorun anahtarda değil, değerdedir; `ThreadLocal` kullanmak GC'yi devre dışı bırakmaz, yalnız değeri thread ömrü boyunca canlı tutar. JVM bazı işlemler sırasında "anahtarı null olmuş" (stale) girişleri fırsat buldukça temizler, ama bu bir garanti değildir — tek güvenilir yol açıkça `remove()` çağırmaktır.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ThreadLocalMap zayıf anahtar güçlü değer asimetrisi">
  <defs>
    <marker id="tl-weak" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#5D7789"/></marker>
    <marker id="tl-strong" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#8A2E2E"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">ThreadLocalMap: zayıf anahtar / GÜÇLÜ değer</text>
  <rect x="24" y="52" width="120" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="84" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Havuz thread'i</text>
  <text x="84" y="94" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">uzun yaşar</text>
  <line x1="144" y1="80" x2="196" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#tl-weak)"/>
  <rect x="200" y="46" width="150" height="68" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="275" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">ThreadLocalMap</text>
  <text x="275" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Entry[ anahtar | değer ]</text>
  <text x="275" y="105" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">thread ile aynı ömür</text>
  <line x1="350" y1="66" x2="470" y2="62" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 4" marker-end="url(#tl-weak)"/>
  <text x="410" y="52" text-anchor="middle" font-size="10" font-weight="600" fill="#5D7789" font-family="system-ui">ZAYIF (weak)</text>
  <rect x="474" y="44" width="142" height="36" rx="8" fill="#EAF1F7" stroke="#6B3FA0" stroke-width="1.2"/>
  <text x="545" y="66" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ThreadLocal (anahtar)</text>
  <line x1="350" y1="98" x2="470" y2="104" stroke="#8A2E2E" stroke-width="2" marker-end="url(#tl-strong)"/>
  <text x="410" y="120" text-anchor="middle" font-size="10" font-weight="700" fill="#8A2E2E" font-family="system-ui">GÜÇLÜ (strong)</text>
  <rect x="474" y="90" width="142" height="36" rx="8" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.3"/>
  <text x="545" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Değer (V) — canlı kalır</text>
  <rect x="24" y="146" width="592" height="46" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="320" y="164" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Anahtar zayıf olsa da değer güçlü zincirle thread ömrünce yaşar.</text>
  <text x="320" y="183" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">tek güvenilir çözüm: finally { remove(); }</text>
</svg>
```

`InheritableThreadLocal`, bir thread (ebeveyn) YENİ bir thread (çocuk) OLUŞTURDUĞU anda ebeveynin değerini çocuğa kopyalar — bağlam (ör. trace-id) alt thread'lere miras geçer. Sınırı nettir: bu kopyalama yalnız gerçek bir child thread yaratılırken olur. Havuzda thread'ler önceden oluşturulup yeniden kullanıldığı için görev gönderildiğinde yeni bir çocuk doğmaz; çağıran thread'in değeri beklendiği gibi taşınmaz. Görev atanması bir kopyalama olayı değildir ve değer rastgele thread'lere ya da tüm havuza da yayılmaz; havuz tabanlı ortamlarda bağlam taşımak için görev sarmalama gibi özel çözümler ya da `ScopedValue` gerekir.

Modern alternatif **`ScopedValue`**, `ThreadLocal`'ın iki zaafını hedefler: değiştirilebilirlik ve elle temizlik. `ScopedValue` DEĞİŞMEZDİR (bir kez bağlanır, `set` ile değiştirilemez) ve KAPSAM-BAĞLIDIR: `ScopedValue.where(ANAHTAR, deger).run(() -> ...)` çağrısıyla değer yalnız o `run`/`call` içindeki çağrı ağacında görünür, kapsam bitince OTOMATİK yok olur. `remove()` diye bir metodu yoktur, gerekmez de. Java 21'de önizleme (preview) olarak geldi ve Java 25 ile kalıcı (final) API oldu.

Sanal thread'ler (virtual threads) `ThreadLocal`'ı desteklemeye devam eder — ama artık havuzlanmadıkları, her görev için yeni bir sanal thread doğup öldüğü için "havuz thread'ine yapışan değer" sızıntısı ortadan kalkar. Buna karşılık yeni bir maliyet doğar: milyonlarca sanal thread varsa, her birinin kendi `ThreadLocalMap` kopyasını taşıması ciddi bellek yükü demektir. `ScopedValue` bu senaryoda kopya yerine paylaşılan, değişmez bir bağlam sunduğu için tercih edilir — sanal thread'lerde çalışmamak bir yana, asıl onlar için tasarlanmıştır.

### Mülakatta ne sorulur?

- **`ThreadLocal` ne değildir**: paylaşım aracı değil, kilit değil, `static` alanla aynı şey değil. Thread-başına İZOLASYON sağlar.
- **Yerinde kullanım**: `SimpleDateFormat` gibi thread-safe olmayan pahalı nesneler ve istek bağlamı. Paylaşılan sayaç → `AtomicInteger`, thread'ler arası iletişim → `BlockingQueue`.
- **`remove()` yoksa sızıntı**: havuz thread'i uzun yaşar, değer `ThreadLocalMap`'te asılı kalır → hem bellek sızıntısı hem eski bağlamın sızması. Havuza dönüş, GC veya `finalize()` bunu temizlemez.
- **Referans asimetrisi**: anahtar ZAYIF, değer GÜÇLÜ. Stale girişlerin fırsatçı temizliği garanti değildir.
- **`InheritableThreadLocal`**: yalnız gerçek child thread oluşumunda kopyalar; havuzda çalışmaz.
- **`ScopedValue`**: değişmez + kapsam-bağlı + otomatik temizlik; `ThreadLocal`'ın yeni adı değil, daha kısıtlı ve güvenli bir modeldir.
- **Sanal thread etkisi**: havuz sızıntısı biter, ama thread başına `ThreadLocalMap` kopyası bellek maliyeti getirir; `ScopedValue` bu yüzden tercih edilir.

### Tek cümlede

`ThreadLocal` paylaşmak yerine her thread'e kendi değerini vererek kilitten kurtarır; havuzda `remove()` unutulursa hem bellek hem bağlam sızdırır, `ScopedValue` ise bu riski değişmez ve kapsam-bağlı yapısıyla ortadan kaldırır.

# tl-1 | Temel
## Soru
`ThreadLocal<T>` ne sağlar?
## A
Bir değişkeni tüm thread'lerin ortak görmesini sağlayıp güncellemeleri anında herkese yayar
🤖 Tam tersi; `ThreadLocal` paylaşımı değil, thread-başına izolasyonu sağlar.
## B
Bir kaynağa erişimi kilitleyip aynı anda yalnız tek thread'in girmesini garanti eder
🤖 `ThreadLocal` kilit değildir; senkronizasyon yerine hiç paylaşmamayı önerir.
## C
Her thread'e değişkenin KENDİ izole kopyasını verir; thread'ler birbirinin değerini görmez
🤖 Doğru: paylaşımsız, thread-başına ayrı değer.
## D
`static` bir alanla tamamen aynı işi yapar; yalnızca daha okunur bir yazım biçimi sunar
🤖 `static` TEK paylaşılan değer tutar; `ThreadLocal` thread başına ayrı değer tutar.
## Doğru
C
## Açıklama
`ThreadLocal<T>`, aynı nesne birden çok thread arasında paylaşılsa bile, her thread'in `get()`/`set()` ile eriştiği değeri o thread'e ÖZEL yapar. İç mekanizması, değeri thread'in kendi `ThreadLocalMap`'inde saklamaktır; böylece bir thread'in yazdığını başka thread GÖREMEZ. Bu, PAYLAŞIMIN (A) tam tersidir ve bir KİLİT (B) değildir — amaç, paylaşılan durumu senkronize etmek yerine hiç paylaşmamaktır. `static` (D) tüm thread'ler için TEK bir değer tutar; `ThreadLocal` ise thread başına ayrı değer verdiği için farklıdır. Kilitsiz, thread-başına durum gereken yerlerde kullanılır.

# tl-2 | Orta
## Soru
`ThreadLocal`'ın tipik ve yerinde kullanım örneği hangisidir?
## A
Thread'lerin ortaklaşa arttırdığı global bir sayacı yarış koşulu olmadan tutmak
🤖 Paylaşılan sayaç `AtomicInteger` işidir; `ThreadLocal` her thread'e ayrı sayaç verirdi.
## B
Bir thread'in ürettiği veriyi başka bir thread'e güvenli biçimde iletmek
🤖 `ThreadLocal` izolasyon içindir; iletişim için `BlockingQueue` gerekir.
## C
Kilit (lock) yerine kullanılıp kritik bölgede karşılıklı dışlama (mutual exclusion) sağlamak
🤖 `ThreadLocal` kilit değildir; karşılıklı dışlama sunmaz.
## D
Thread-safe olmayan pahalı nesneleri (`SimpleDateFormat`) veya istek bağlamını thread başına tutmak
🤖 Doğru: thread-başına izole bağlam/nesne için idealdir.
## Doğru
D
## Açıklama
`ThreadLocal`, PAYLAŞILAMAYAN ya da thread başına ayrı olması gereken durumlar içindir. İki klasik kullanım: (1) Thread-safe OLMAYAN ve her seferinde yeniden yaratılması pahalı nesneleri (klasik örnek `SimpleDateFormat`; formatlama sırasında dahili durum bozulabildiği için paylaşılamaz) thread başına önbelleklemek; (2) Bir isteğin bağlamını (giriş yapan kullanıcı, transaction, MDC/trace-id) her metoda parametre olarak geçmeden, çağrı zinciri boyunca örtük taşımak. Paylaşılan sayaç (A → `AtomicInteger`), thread'ler arası iletişim (B → kuyruk) ve karşılıklı dışlama (C → kilit) `ThreadLocal`'ın işi DEĞİLDİR.

# tl-3 | İleri
## Soru
`ThreadLocal`, thread HAVUZUNDA (thread pool) neden bellek sızıntısına yol açabilir?
## A
Havuz thread'leri UZUN yaşar; `remove()` çağrılmazsa değer `ThreadLocalMap`'te asılı kalır
🤖 Doğru: uzun ömürlü thread + temizlenmeyen değer = sızıntı (ve veri karışması).
## B
`ThreadLocal` asla sızmaz; thread havuza geri döndüğünde GC tüm girişleri otomatik temizler
🤖 Havuza dönüş bir temizlik olayı değildir; thread yaşadıkça değer güçlü tutulur.
## C
Sızıntı yalnız `InheritableThreadLocal` ile, değer çocuk thread'lere kopyalandığı için oluşur
🤖 Sızıntı sıradan `ThreadLocal`'da da olur; kopyalamayla ilgisi yoktur.
## D
Sızıntı olmaz; `ThreadLocal` değerleri `finalize()` çağrısıyla her GC turunda temizlenir
🤖 `finalize()` bu iş için ne çağrılır ne de güvenilirdir; zaten kullanımdan kaldırılmıştır.
## Doğru
A
## Açıklama
Yeni thread yaratılıp bitirilen bir modelde thread ölünce `ThreadLocalMap`'i de gider. Ama thread HAVUZUNDA thread'ler yeniden kullanılmak üzere UZUN yaşar. Bir görev `ThreadLocal.set(...)` yapıp `remove()` çağırmadan biterse, değer o havuz thread'inin `ThreadLocalMap`'inde ASILI kalır: (1) nesne GC edilemez → bellek sızıntısı; (2) aynı thread sonraki isteğe atandığında ESKİ değeri görüp yanlış/karışık veri (ör. önceki kullanıcının bağlamı) kullanabilir — güvenlik açığı bile olabilir. Bu yüzden altın kural: değeri `try { set(...) ... } finally { remove(); }` ile mutlaka temizle. GC (B) veya `finalize()` (D) bunu kurtarmaz; sorun `InheritableThreadLocal`'a özgü de değildir (C).

# tl-4 | İleri
## Soru
`ThreadLocalMap`'in referans yapısı, `remove()` çağrısını neden gerekli kılar?
## A
Anahtar olan `ThreadLocal` nesnesine GÜÇLÜ referans tutulduğu için anahtar hiçbir zaman GC edilemez
🤖 Anahtar aslında ZAYIF referanstır; sorun anahtarda değil, güçlü tutulan DEĞERDEDİR.
## B
Anahtar (`ThreadLocal`) ZAYIF referansla, DEĞER ise GÜÇLÜ tutulur; değer otomatik temizlenmez
🤖 Doğru: zayıf anahtar / güçlü değer asimetrisi elle temizliği zorunlu kılar.
## C
Hem anahtar hem değer zayıf referanstır; GC ikisini de toplar, `remove()` yalnızca bunu hızlandırır
🤖 Değer GÜÇLÜ tutulur; thread yaşarken GC ona dokunamaz.
## D
`ThreadLocal` kullanan thread'lerde GC tamamen devre dışı kalır, bellek elle yönetilmek zorundadır
🤖 `ThreadLocal` GC'yi kapatmaz; yalnız değeri thread ömrü boyunca canlı tutar.
## Doğru
B
## Açıklama
Her thread'in `ThreadLocalMap`'inde girişler `Entry` olarak tutulur ve bu `Entry`, anahtar olan `ThreadLocal` nesnesine ZAYIF (weak) referans verir. Amaç, `ThreadLocal` nesnesi başka yerden erişilemez hale gelince anahtarın GC edilebilmesidir. Ancak DEĞER, `Entry` içinde GÜÇLÜ referansla tutulur. Sonuç asimetriktir: anahtar zayıf olsa bile thread (dolayısıyla map) yaşadığı sürece değer GÜÇLÜ zincirle canlı kalır ve otomatik temizlenmez. Havuz thread'lerinde bu, değerin uzun süre asılı kalması demektir. JVM bazı işlemlerde "anahtarı null olmuş" (stale) girişleri fırsat buldukça temizlese de bu garanti değildir; tek güvenilir yol, iş bitince açıkça `remove()` çağırmaktır.

# tl-5 | İleri
## Soru
`InheritableThreadLocal` sıradan `ThreadLocal`'dan nasıl ayrılır?
## A
Değeri, havuzdaki tüm işçi thread'lere görev atandığı anda otomatik olarak yayar
🤖 Görev atama bir kopyalama olayı değildir; kopyalama yalnız thread OLUŞTURULURKEN olur.
## B
Ana thread'in değerini, aynı thread grubundaki rastgele seçilen thread'lere kopyalayarak dağıtır
🤖 Rastgele değil; yalnız o thread'in kendi oluşturduğu çocuk (child) thread'e kopyalanır.
## C
Bir thread YENİ bir çocuk thread oluşturduğunda değeri ona kopyalar; havuzda çocuk doğmaz
🤖 Doğru: kalıtım yalnız gerçek child thread oluşumunda gerçekleşir.
## D
Kalıtımla hiçbir ilgisi yoktur; yalnızca `ThreadLocal`'ın alt sınıflanabilir (subclass) sürümüdür
🤖 İsim davranışı yansıtır; değer ebeveynden çocuğa "miras" kopyalanır.
## Doğru
C
## Açıklama
`InheritableThreadLocal`, bir thread (ebeveyn) YENİ bir thread (çocuk) OLUŞTURDUĞU anda, ebeveynin değerini çocuğa KOPYALAR — böylece bağlam (ör. trace-id) alt thread'lere miras geçer. Kritik sınır: bu kopyalama yalnız gerçek bir child thread YARATILDIĞINDA olur. Thread HAVUZLARINDA thread'ler önceden oluşturulup yeniden kullanıldığından, görev submit edildiğinde yeni bir child doğmaz; dolayısıyla çağıran thread'in değeri beklenildiği gibi taşınmaz (çoğu zaman hiç ya da yanlış taşınır). Bu yüzden havuz tabanlı ortamlarda bağlam yayımı için özel çözümler (ör. görev sarmalama, veya modern `ScopedValue`) gerekir. Değeri tüm havuza yaymak (A) veya rastgele thread'lere kopyalamak (B) `InheritableThreadLocal`'ın davranışı değildir.

# tl-6 | Uzman
## Soru
Java 21+ ile gelen `ScopedValue`, `ThreadLocal`'a modern alternatif olarak hangi avantajı sağlar?
## A
`ScopedValue`, `ThreadLocal`'ın yalnızca yeni adıdır; davranışı birebir aynı olduğu için aynı sızıntı riskini taşır
🤖 Aynı değildir; değişmezlik ve kapsam-bağlılık üzerine kurulu farklı bir modeldir.
## B
`ScopedValue` değiştirilebilir (mutable) olduğundan kullanımdan sonra yine `remove()` çağırmak gerekir
🤖 Tersine; `ScopedValue` DEĞİŞMEZDİR ve `remove()` diye bir metodu yoktur.
## C
`ScopedValue` yalnız platform thread'lerinde çalışır; sanal thread'lerde (virtual threads) desteklenmez
🤖 Tam tersi; özellikle çok sayıda sanal thread senaryosu için tasarlanmıştır.
## D
DEĞİŞMEZ ve KAPSAM-BAĞLIDIR: `where(...).run(...)` kapsamı bitince otomatik temizlenir, `remove()` gerekmez
🤖 Doğru: değişmez, kapsam-bağlı, otomatik temizlenen modern alternatif.
## Doğru
D
## Açıklama
`ScopedValue` (Java 21'de önizleme olarak geldi, Java 25 ile kalıcı/final API oldu), `ThreadLocal`'ın en büyük iki zaafını hedefler: değiştirilebilirlik ve elle temizlik. `ScopedValue` DEĞİŞMEZDİR (bir kez bağlanır, `set` ile değiştirilemez) ve KAPSAM-BAĞLIDIR: `ScopedValue.where(ANAHTAR, deger).run(() -> ...)` çağrısıyla değer yalnız o `run`/`call` içindeki çağrı ağacında görünür, kapsam (blok) bittiğinde OTOMATİK olarak yok olur. Bu yüzden `remove()` çağırma zorunluluğu ve havuz thread'lerindeki sızıntı/veri-karışması riski ortadan kalkar (B yanlış). Kapsam-bağlı yapı, milyonlarca sanal thread (virtual threads) senaryosunda `ThreadLocal`'dan daha hafif ve daha güvenlidir — sanal thread'lerde çalışmamak bir yana, asıl onlar için tasarlanmıştır (C yanlış). Kısaca `ScopedValue`, `ThreadLocal`'ın yeni adı değil (A), daha kısıtlı ve güvenli bir bağlam taşıma modelidir.
