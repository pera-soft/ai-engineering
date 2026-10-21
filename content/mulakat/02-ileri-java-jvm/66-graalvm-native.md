---
slug: graalvm-native
baslik: "GraalVM Native Image: AOT Derleme"
kademe: İleri
sira: 66
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

İki tür restoran vardır. Birincisi siparişi aldıktan sonra pişirmeye başlar: ilk tabak geç gelir, ama aşçı sizin damak zevkinizi öğrendikçe yemek giderek iyileşir. İkincisi her şeyi önceden hazırlayıp paketlemiştir: kapıdan girer girmez saniyeler içinde yersiniz, ama menü sabittir, o an bir şey değiştiremezsiniz. Klasik Java birincisidir (JIT), GraalVM Native Image ikincisidir (AOT). Hangisinin doğru olduğu, "ne kadar bekleyebilirsin" ve "ne kadar esneklik gerekiyor" sorularına bağlıdır.

### Temel kavramlar

- **Bayt kodu (bytecode)**: `javac`'ın ürettiği ara dil; makine kodu değildir.
- **Yorumlama (interpretation)**: bayt kodunu adım adım yürütmek — hemen başlar, yavaştır.
- **Derleme (compilation)**: kodu işlemcinin doğrudan çalıştıracağı yerel (native) komutlara çevirmek.
- **JIT (Just-In-Time)**: bu çeviriyi program ÇALIŞIRKEN yapmak. Esnektir, çalışma-zamanı profiline uyarlanır, ama **ısınma (warm-up)** gerektirir.
- **AOT (Ahead-Of-Time)**: çeviriyi ÖNCEDEN, build (derleme) zamanında yapmak. Isınma yoktur, ama çalışma-zamanı profiline uyarlanamaz.
- **Isınma (warm-up)**: JVM'in ilk saniyelerinde kodun henüz tepe hıza ulaşmamış olması.
- **Başlangıç süresi (startup) ve ayak izi (footprint)**: uygulamanın ilk isteği karşılayana kadar geçen süre ve kapladığı bellek.
- **Erişilebilirlik analizi (reachability analysis)**: derleyicinin, `main()`'den başlayarak çağrı grafiğini izleyip hangi sınıf/metodun gerçekten kullanıldığını çıkarması. Ulaşılamayan kod atılır (ölü kod eleme).
- **Yansıma (reflection)**: sınıf/metot adlarını METİN olarak verip çalışma zamanında çağırmak. Statik analize "görünmez" olmasının sebebi budur.

### Nasıl çalışır?

**GraalVM Native Image**, uygulamayı tüm bağımlılıkları ve gerekli çalışma-zamanı parçalarıyla birlikte build zamanında TEK bir yerel çalıştırılabilir ikiliye derler. Çalışırken JIT derleyici yoktur, ısınma yoktur — ikili doğrudan başlar. Bu, kaynağı yorumlamak ya da yalnızca bir sözdizimi denetimi yapmak değildir; gerçekten çalıştırılabilir kod üretilir.

**İki bayrak kazanım:** **milisaniye düzeyinde başlangıç** ve **düşük bellek ayak izi**. Çünkü JIT altyapısı, profil verisi ve fazladan meta veri taşınmaz. Sunucusuz (serverless) işlevler, CLI araçları ve hızlı ölçeklenen konteynerler için idealdir.

Ama bedeli vardır: **kapalı-dünya varsayımı (closed-world assumption)**. Derleyici, ikiliye neyin gireceğine build zamanında karar verir; statik erişilebilirlik analiziyle ulaşılan her şey dahil edilir, ulaşılamayan atılır. Java'nın dinamik özellikleri bunu zorlaştırır:

- **Yansıma (reflection), dinamik vekil nesne (dynamic proxy), JNI, kaynak (resource) yükleme**: build zamanında statik olarak görünmez, dolayısıyla varsayılan olarak DIŞLANIR. Çözüm, hangi sınıf/metodun yansımayla kullanılacağını **erişilebilirlik meta verisi (reachability metadata)** ile önceden bildirmektir — yapılandırma dosyalarıyla ya da uygulamayı JVM'de çalıştırıp erişimleri kaydeden **izleme aracıyla (tracing agent)**.
- **Build-zamanı ilklendirme (build-time initialization)**: bazı sınıflar başlangıç yerine derleme anında ilklendirilebilir; bu, çalışma-zamanı işini image'e taşıyıp başlangıcı daha da hızlandırır. Ama dikkatli kullanılmalıdır — build anındaki DURUM (o an okunmuş bir zaman damgası, ortam değeri, açılmış bir kaynak) image'e **donar** ve her çalıştırmada aynı kalır.
- **Build maliyeti**: native image üretimi tüm kapalı-dünya analizini yaptığı için normal `javac` derlemesinin yanında çok pahalıdır — tipik olarak dakikalar süren ve gigabaytlarca RAM isteyen bir adım. Ayrıca her hedef işletim sistemi/mimari için ayrı build gerekir ve CI süreleri uzar. Bu yüzden pratikte JVM üzerinde geliştirilip test edilir, native image yalnız yayın (release) hattında üretilir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="AOT native image vs JIT: takas">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Native Image (AOT) ↔ JVM (JIT) takası</text>
  <rect x="24" y="48" width="285" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="166" y="74" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Native Image (AOT)</text>
  <text x="166" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">+ milisaniye başlangıç</text>
  <text x="166" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">+ düşük bellek</text>
  <text x="166" y="140" text-anchor="middle" font-size="11" fill="#B4322B" font-family="system-ui">- kapalı-dünya varsayımı</text>
  <text x="166" y="160" text-anchor="middle" font-size="11" fill="#B4322B" font-family="system-ui">- reflection metadata gerekir</text>
  <text x="166" y="178" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tepe throughput JIT'ten düşük olabilir</text>
  <rect x="331" y="48" width="285" height="140" rx="12" fill="#EAF0F6" stroke="#2C5A82" stroke-width="1.5"/>
  <text x="473" y="74" text-anchor="middle" font-size="13.5" font-weight="700" fill="#2C5A82" font-family="system-ui">JVM (JIT)</text>
  <text x="473" y="100" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">+ profil-uyarlamalı tepe hız</text>
  <text x="473" y="120" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">+ tam dinamik (reflection)</text>
  <text x="473" y="140" text-anchor="middle" font-size="11" fill="#B4322B" font-family="system-ui">- ısınma (warm-up) maliyeti</text>
  <text x="473" y="160" text-anchor="middle" font-size="11" fill="#B4322B" font-family="system-ui">- yüksek bellek ayak izi</text>
  <text x="320" y="208" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Serverless / CLI / hızlı ölçek → native; uzun ömürlü yüksek verim → JIT</text>
</svg>
```

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kapalı-dünya: build zamanı erişilebilirlik analizi ve reflection metadata">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kapalı-dünya: build-zamanı erişilebilirlik analizi</text>
  <rect x="30" y="50" width="200" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="130" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">main() çağrı grafiği</text>
  <text x="130" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">statik olarak izlenebilir</text>
  <path d="M230 73 L330 73" stroke="#25804C" stroke-width="2" fill="none" marker-end="url(#graal-cw-ok)"/>
  <text x="280" y="64" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">reachable</text>
  <rect x="330" y="50" width="280" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="470" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">image'e DAHİL edilir</text>
  <rect x="30" y="130" width="200" height="46" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="130" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">reflection / proxy</text>
  <text x="130" y="168" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">JNI / resource</text>
  <path d="M230 153 L330 153" stroke="#8A2E2E" stroke-width="2" stroke-dasharray="5 4" fill="none" marker-end="url(#graal-cw-x)"/>
  <text x="280" y="144" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">görünmez</text>
  <rect x="330" y="130" width="280" height="46" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="470" y="158" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">varsayılan: DIŞLANIR</text>
  <defs>
    <marker id="graal-cw-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#25804C"/>
    </marker>
    <marker id="graal-cw-x" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#8A2E2E"/>
    </marker>
  </defs>
  <text x="320" y="206" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Çözüm: reachability metadata ile önceden bildir</text>
</svg>
```

### Mülakatta ne sorulur?

**Kapalı-dünya ne DEĞİLDİR?** Uygulamanın ağa/dış dünyaya erişiminin kapatılması değildir; tek iş parçacığında çalışma zorunluluğu değildir (native image çok iş parçacıklıdır); kaynak kodun kapalı olması ya da lisans/dağıtım biçimi hiç değildir. Tamamen teknik bir **statik analiz** kısıtıdır: derleyici tüm erişilebilir kodu build zamanında görebilmelidir.

**Reflection sorunu ve çözümü.** Yansımayla erişilen sınıf/metot/alanlar bildirilmezse ikiliye girmez ve çalışma zamanında `ClassNotFoundException`/`NoSuchMethodException` gibi hatalar alınır — kod JVM'de sorunsuz çalışıyor olsa bile. Bu bir DERLEME hatası değildir ve çözüm reflection'ı silmek değildir; çözüm **metadata bildirmektir**. Reflection ne otomatik-sınırsız çalışır ne de ikiliyi kendiliğinden iki katına çıkarır; mesele **görünürlüktür**. Modern çerçeveler (Spring Boot, Quarkus, Micronaut) bu metadata'yı büyük ölçüde otomatik üretir.

**GC kalkmaz.** Native image de bir çöp toplayıcı kullanır; "GC tümden kalkar, duraklamalar sıfırlanır" iddiası yanlıştır.

**Ne zaman native, ne zaman DEĞİL?** Saniyede binlerce kez soğuk başlayan, milisaniye başlangıç ve düşük bellek isteyen **sunucusuz işlev** en uygun adaydır; CLI ve kısa ömürlü/hızlı ölçeklenen konteynerler de öyle. Buna karşılık bazı iş yükleri JIT'in profil-uyarlamalı tepe optimizasyonunu ister. Onlarca GB veri işleyen, saatlerce çalışan ve tepe iş hacminin (throughput) kritik olduğu **batch analitik servisi** böyledir; uzun ısınmadan fayda gören **yüksek-frekanslı işlem motoru** da öyle. Native'in ısınmasız ama uyarlanamayan modeli burada daha zayıftır. Yoğun reflection ve çalışma-zamanı sınıf üretimine dayanan eski uygulamalar ise kapalı-dünya kısıtı yüzünden native'e taşınması **en zahmetli** adaylardır.

**Genel takas tek cümleye iner:** native, başlangıç ve bellekte kazanır; tepe verim ve dinamiklikte ödün verir. 2026'da ekosistem (Spring Boot, Quarkus, Micronaut) native'i olgun biçimde destekler; ayrıca **Project Leyden**, AOT kazanımlarını JVM'e daha az ödünle getirmeyi hedefler.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Native hızlı başlar düşük plato; JVM ısınır yüksek platoya çıkar">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Başlangıç ve throughput: Native (AOT) vs JVM (JIT)</text>
  <line x1="60" y1="48" x2="60" y2="190" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="60" y1="190" x2="606" y2="190" stroke="#5D7789" stroke-width="1.5"/>
  <text x="34" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui" transform="rotate(-90 34 120)">throughput</text>
  <path d="M60 120 L200 117 L400 115 L606 114" fill="none" stroke="#25804C" stroke-width="2.5"/>
  <path d="M60 184 L120 180 L200 142 L300 104 L420 84 L606 78" fill="none" stroke="#186399" stroke-width="2.5"/>
  <line x1="262" y1="48" x2="262" y2="190" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>
  <circle cx="262" cy="116" r="3.5" fill="#1C3247"/>
  <text x="262" y="42" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">crossover</text>
  <text x="150" y="106" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Native: hızlı başlar, düşük plato</text>
  <text x="470" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">JVM: ısınır, yüksek plato</text>
  <text x="540" y="208" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman →</text>
  <text x="320" y="230" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Kısa iş → native; uzun/yüksek verim → JVM (crossover sonrası)</text>
</svg>
```

### Tek cümlede

GraalVM Native Image, uygulamayı kapalı-dünya varsayımı altında build zamanında yerel bir ikiliye derler; karşılığında milisaniye başlangıç ve düşük bellek verir, ama yansıma için metadata ister, pahalı bir build gerektirir ve uzun ömürlü yüklerde JIT'in tepe verimine ulaşamayabilir.

# graal-1 | İleri
## Soru
GraalVM Native Image'in temel yaklaşımı klasik JVM'den nasıl ayrılır?
## A
Bayt kodunu çalışma zamanında JIT ile derler, tıpkı HotSpot gibi
🤖 Bu klasik JVM davranışı; native image'in ayırt edici yönü değil.
## B
Uygulamayı build (derleme) zamanında AOT ile doğrudan yerel bir çalıştırılabilir ikiliye derler
🤖 Doğru: native image AOT'tur; çalışma-zamanı JIT'i yerine önceden derlenmiş ikili üretir.
## C
Kaynağı yorumlar ve hiç derlemez
🤖 Yorumlama değil; native image tam tersine önceden (AOT) derler.
## D
Yalnız bir sözdizimi denetleyicisidir, kod üretmez
🤖 Kod üretir — çalıştırılabilir bir native ikili; denetleyici değildir.
## Doğru
B
## Açıklama
Klasik JVM bayt kodunu çalışma zamanında **JIT** ile makine koduna çevirir; native image ise **AOT (Ahead-Of-Time)** yaklaşımını benimser: uygulamayı, tüm bağımlılıkları ve gerekli çalışma-zamanı parçalarıyla birlikte **build zamanında** tek bir yerel çalıştırılabilir ikiliye derler. Sonuçta çalışırken JIT derleyici yoktur, ısınma yoktur — ikili doğrudan başlar. Bu; çalışma-zamanı JIT (A → klasik JVM), yorumlama (C) veya sözdizimi denetimi (D) değildir. AOT'a geçmek hızlı başlangıç ve düşük bellek getirir ama dinamik esneklikte ödün ister — bu takas native image'in tüm karakterini belirler.

# graal-2 | İleri
## Soru
Native image'in sunumu en çok parlattığı iki somut kazanım nedir?
## A
Milisaniye düzeyinde başlangıç ve düşük bellek ayak izi
🤖 Doğru: JIT ısınması ödenmediği için native image hızlı başlar ve az bellek kullanır.
## B
Sınırsız reflection ve çalışma zamanında tam dinamik sınıf yükleme
🤖 Tam tersi; native image dinamik özelliklerde kısıtlıdır, metadata ister.
## C
Uzun ömürlü servislerde ulaşılan en yüksek tepe throughput
🤖 Tepe throughput genelde JIT'in güçlü olduğu alandır; native image'in vaadi bu değil.
## D
Çöp toplamanın tümden kalkması ve GC duraklamalarının sıfırlanması
🤖 Native image de bir çöp toplayıcı kullanır; GC ortadan kalkmaz.
## Doğru
A
## Açıklama
Native image'in iki bayrak kazanımı **hızlı başlangıç** ve **düşük bellek** ayak izidir. Çünkü JIT yoktur: ısınma maliyeti ödenmez, ikili milisaniyeler içinde tepe (kendi) hızında başlar; ayrıca JIT altyapısı, profil verisi ve fazladan meta veri taşınmadığı için bellek ayak izi belirgin küçülür. Bu, **sunucusuz işlevler ve CLI** için idealdir. Buna karşılık native image, sınırsız reflection (B) sunmaz — tersine kapalı-dünya kısıtı vardır; uzun ömürlü servislerde tepe throughput (C) genelde JIT'in üstünlüğüdür; ve GC ortadan kalkmaz (D). Kazanımları doğru bilmek, "ne zaman native" kararının temelidir.

# graal-3 | İleri
## Soru
Native image'in "kapalı-dünya varsayımı (closed-world assumption)" ne anlama gelir?
## A
Uygulamanın çalışırken ağa/dış dünyaya erişiminin kapatılması, yalnız yerel kaynak kullanabilmesi
🤖 Ağ erişimiyle ilgisi yok; varsayım derleyicinin TÜM kodu build zamanında görebilmesiyle ilgilidir.
## B
Uygulamanın tek iş parçacığında çalışması, eşzamanlılığın image'de desteklenmemesi
🤖 Native image çok iş parçacıklıdır; kastedilen build-zamanı erişilebilirlik analizidir.
## C
İkiliye girecek TÜM erişilebilir kodun build zamanında görülebilmesi gerektiği; çalışma zamanında keşfedilen kod varsayılan olarak dışlanır
🤖 Doğru: statik analizle ulaşılamayan yansıma/proxy/kaynak yolları önceden bildirilmelidir.
## D
Uygulamanın kapalı kaynak olması, bağımlılıkların ikili biçimde gömülmesi zorunluluğu
🤖 Lisans/dağıtım biçimiyle ilgisi yok; teknik bir statik analiz kısıtıdır.
## Doğru
C
## Açıklama
**Kapalı-dünya varsayımı**, native image derleyicisinin ikiliye neyin gireceğine build zamanında karar vermesidir: statik **erişilebilirlik (reachability) analizi** ile ulaşılabilen tüm sınıf/metot dahil edilir, ulaşılamayan atılır (dead code elimination). Sorun, Java'nın **çalışma zamanında** keşfedilen dinamik yollarıdır — **reflection, dinamik proxy, JNI, kaynak yükleme** — bunlar statik analize "görünmez" olduğundan varsayılan olarak dahil edilMEZ. Çözüm: hangi dinamik öğelerin gerekli olduğunu **reachability metadata** ile önceden bildirmek. Bu; ağ erişimi (A), iş parçacığı sayısı (B) veya kaynak kodun kapalılığı (D) ile ilgili değildir — tamamen derleyicinin tüm kodu önceden görme gereğidir ve native image'in en önemli pratik kısıtıdır.

# graal-4 | İleri
## Soru
Bir kütüphane native image altında reflection kullandığında sık karşılaşılan sorun ve çözümü nedir?
## A
Sorun yoktur; reflection native image'de otomatik ve sınırsız çalışır
🤖 Otomatik değildir; bildirilmeyen reflection çalışma-zamanında hata verir.
## B
Reflection'ı kullanmak derleme hatası üretir, tek çözüm reflection'ı silmektir
🤖 Derleme hatası değil; metadata ile desteklenebilir, silmek gerekmez.
## C
Reflection kullanan her kütüphane native image'in boyutunu otomatik iki katına çıkarır
🤖 Böyle bir otomatik büyüme yok; sorun görünmezlik, çözüm metadata bildirimidir.
## D
Yansımayla erişilen öğeler statik analize görünmediğinden ikiliye girmeyebilir; çözüm metadata bildirmektir
🤖 Doğru: bu öğeler önceden bildirilmeli, aksi halde çalışma zamanında hata alınır.
## Doğru
D
## Açıklama
Native image'in kapalı-dünya analizi, reflection ile erişilen sınıf/metot/alanları statik olarak **göremez**. Bildirilmezlerse ikiliye dahil edilmez ve çalışma zamanında `ClassNotFoundException`/`NoSuchMethodException` gibi hatalarla karşılaşılır — kod JVM'de sorunsuz çalışıyor olsa bile. Çözüm reflection'ı silmek (B) değil, **erişilebilirlik meta verisi (reachability metadata)** sağlamaktır: hangi öğelerin yansımayla kullanılacağını yapılandırma dosyalarıyla ya da JVM'de çalıştırıp erişimleri kaydeden **izleme aracıyla (tracing agent)** bildirmek. Modern çerçeveler (Spring Boot, Quarkus, Micronaut) bu metadata'yı büyük ölçüde otomatik üretir. Reflection ne otomatik-sınırsızdır (A) ne de ikiliyi kendiliğinden büyütür (C); mesele **görünürlük** ve onu metadata ile sağlamaktır.

# graal-5 | İleri
## Soru
Aşağıdaki iş yüklerinden hangisi GraalVM native image için en uygun adaydır?
## A
Onlarca GB veri işleyen, saatlerce çalışan, tepe throughput'un kritik olduğu bir batch analitik servisi
🤖 Uzun ömürlü + yüksek verim: JIT'in profil-uyarlamalı tepe optimizasyonu burada kazanır.
## B
Saniyede binlerce kez soğuk başlayan, milisaniye başlangıç ve düşük bellek isteyen bir sunucusuz (serverless) işlev
🤖 Doğru: hızlı başlangıç ve düşük bellek native image'in tam güçlü olduğu senaryo.
## C
Yoğun reflection ve çalışma-zamanı sınıf üretimine dayanan, dinamikliği yüksek eski bir uygulama
🤖 Kapalı-dünya kısıtı bunu zorlaştırır; native'e taşımak yüksek çaba ister, en uygun aday değil.
## D
Tepe performansa ulaşmak için uzun ısınma süresine ihtiyaç duyan yüksek-frekanslı işlem motoru
🤖 Uzun ısınmadan fayda gören yük JIT'i ister; native'in ısınmasız modeli buraya oturmaz.
## Doğru
B
## Açıklama
Native image, **başlangıç süresi ve bellek** kritik olduğunda parlar; **sunucusuz işlevler** tam bu profildir (B): sık soğuk başlatma, milisaniye başlangıç beklentisi, düşük bellekle yoğun ölçek. Buna karşılık, uzun ömürlü yüksek-verim batch (A) ve uzun ısınmadan fayda gören işlem motoru (D), **JIT'in profil-uyarlamalı tepe optimizasyonundan** yararlanır — native'in ısınmasız ama uyarlanamayan modeli burada daha zayıftır. Yoğun reflection/dinamik sınıf üreten eski uygulama (C) ise kapalı-dünya kısıtı yüzünden native'e taşınması **en zahmetli** adaydır. Doğru karar iş yükünün ömrüne ve dinamiklik ihtiyacına bakmaktır: kısa ömürlü/hızlı ölçek → native; uzun ömürlü/yüksek verim/dinamik → JVM.

# graal-6 | İleri
## Soru
Native image'de "build-zamanı ilklendirme (build-time initialization)" ve genel takas için hangi ifade doğrudur?
## A
Başlangıcı hızlandırır; ancak build anındaki durum image'e "donduğu" için dikkatli kullanılmalıdır
🤖 Doğru: build-time init başlangıcı hızlandırır ama o anki durumu image'e kalıcı gömer.
## B
Her durumda güvenlidir; ilklendirmenin ne zaman yapıldığı davranışı hiç değiştirmez
🤖 Güvenli değil; build anında okunan zaman/ortam değeri ya da açılan kaynak image'e donar.
## C
Başlangıcı hızlandırır ve ayrıca native image'i tepe throughput'ta JIT'in üstüne çıkarır
🤖 İlk yarısı doğru, ikinci yarısı yanlış: uzun ömürlü yükte JIT genelde tepe verimde öndedir.
## D
İşi çalışma zamanından build'e taşıdığı için uygulamanın başlangıcını yavaşlatır
🤖 Tersi; işi build'e taşımanın amacı çalışma-zamanı başlangıcını HIZLANDIRMAKTIR.
## Doğru
A
## Açıklama
**Build-zamanı ilklendirme**, normalde başlangıçta yapılacak bazı sınıf ilklendirmelerini **derleme anına** kaydırır; böylece bu iş image'e gömülür ve çalışma-zamanı başlangıcı daha da hızlanır (D yanlış — hızlandırır, yavaşlatmaz). Ancak bir tuzağı vardır: build anındaki **durum** (o an açılmış bir kaynak, okunmuş bir zaman damgası, ortam değeri) image'e **donar** ve her çalıştırmada aynı kalır — bu yüzden her sınıf için güvenli değildir (B yanlış). Genel takasta da native image, uzun ömürlü iş yüklerinde **tepe throughput** açısından JIT'in gerisinde kalabilir (C yanlış), çünkü çalışma-zamanı profiline uyarlanamaz. Doğru bakış: native, başlangıç ve bellekte kazanır, tepe verim ve dinamiklikte ödün verir — build-time init bu dengede güçlü ama dikkatli kullanılması gereken bir araçtır.
