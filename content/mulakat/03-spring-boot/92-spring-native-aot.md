---
slug: spring-native-aot
baslik: "Spring Boot AOT & GraalVM Native Image"
kademe: İleri
sira: 92
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir yemeği iki türlü hazırlayabilirsin. Ya siparişi alınca mutfağı ısıtır, malzemeyi çıkarır ve pişirmeye başlarsın — ilk tabak geç gelir ama mutfak ısındıkça hız artar. Ya da her şeyi önceden hazırlayıp donmuş porsiyon hâline getirirsin — tabak saniyeler içinde hazırdır, ama menü artık sabittir, sonradan malzeme ekleyemezsin. Klasik JVM birinci yöntemdir; GraalVM Native Image ikincisi. Amaç, sunucusuz (serverless) ve konteyner dünyasında kritik olan **milisaniye başlangıç** ve **düşük bellek** ayak izidir.

### Temel kavramlar

- **Derleme (compile)**: Kaynak kodu makinenin çalıştırabileceği biçime çevirme.
- **Bytecode**: Java'nın ara dili. JVM bunu çalıştırır.
- **JIT (Just-In-Time)**: JVM'in **çalışma zamanında**, gerçek kullanım profilini gözleyerek sıcak kodu makine koduna derlemesi. Zamanla hızlanır; buna **ısınma** (warm-up) denir.
- **AOT (Ahead-Of-Time)**: İşin **build zamanında** önceden yapılması — tam tersi yön.
- **Yansıma (reflection)**: Programın kendi sınıflarını çalışma anında isimle bulup metot/alanlarına erişmesi. Jackson, JPA ve Spring bunu yoğun kullanır.
- **Vekil nesne (proxy)**: Bir nesnenin etrafına sarılan, çağrıları yakalayan sarmalayıcı. **JDK dinamik proxy** arayüzler için üretilir; **CGLIB** ise çalışma zamanında sınıfın **alt sınıfını** üretir.
- **Reachability metadata (erişilebilirlik meta verisi)**: "Bu sınıfa/metoda yansımayla erişilecek" bilgisinin build'e bildirilmesi.
- **Yerel ikili (native binary)**: Doğrudan işletim sisteminin çalıştırdığı, JVM gerektirmeyen tek dosya.

Zincirleme ama **ayrı** iki aşamayı karıştırmamak gerekir. **Spring AOT işleme (`process-aot`)** build zamanında uygulama bağlamını önden değerlendirir: `@Conditional` koşullarını çalıştırır, hangi bean'lerin oluşacağını çözer ve bu kararları doğrudan üretilmiş Java koduna (bir `ApplicationContextInitializer`) döker. **GraalVM native derleme (`native:compile`)** ise üretilen kodu ve reachability metadata'yı kullanarak tüm ulaşılabilir kodu tek bir yerel ikiliye derler. AOT işleme bytecode küçültme (shrinker), test koşumu veya JIT değildir.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Spring AOT işleme ve GraalVM native derleme zinciri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kaynaktan yerel ikiliye: iki aşama</text>
  <rect x="24" y="46" width="160" height="86" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="104" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Kaynak + bean'ler</text>
  <text x="104" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@Configuration</text>
  <text x="104" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@Conditional koşulları</text>
  <rect x="240" y="46" width="160" height="86" rx="10" fill="#FBF1E3" stroke="#B5791F" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B5791F" font-family="system-ui">Spring AOT</text>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">process-aot</text>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">üretilmiş kod +</text>
  <text x="320" y="123" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">reachability metadata</text>
  <rect x="456" y="46" width="160" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="536" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">GraalVM</text>
  <text x="536" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">native:compile</text>
  <text x="536" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek yerel ikili</text>
  <text x="536" y="123" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ms başlangıç</text>
  <path d="M188 89 L236 89" stroke="#5D7789" stroke-width="1.5" marker-end="url(#nativeArrow)"/>
  <path d="M404 89 L452 89" stroke="#5D7789" stroke-width="1.5" marker-end="url(#nativeArrow)"/>
  <defs>
    <marker id="nativeArrow" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="164" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Kapalı-dünya: build anında görünmeyen reflection/proxy → runtime hint ile bildirilmeli</text>
  <rect x="80" y="182" width="480" height="34" rx="8" fill="#FCEDEC" stroke="#B4322B" stroke-width="1.2"/>
  <text x="320" y="204" text-anchor="middle" font-size="11" fill="#B4322B" font-family="system-ui">@RegisterReflectionForBinding · RuntimeHintsRegistrar · @ImportRuntimeHints</text>
</svg>
```

### Nasıl çalışır?

**1. Kapalı-dünya varsayımı (closed-world assumption).** GraalVM, build zamanında **ulaşılabilir (reachable) tüm kodu** görebilmek zorundadır. Statik analizde görünmeyen hiçbir sınıf/metot çalışma zamanında "sonradan" eklenemez; bildirilmeyen bir hedefe yansımayla erişmek çalışma anında hata verir. Bu, ağ erişimiyle, sınıfları `final` yapmakla veya tek sınıf/tek giriş noktası zorunluluğuyla **ilgili değildir**; kısıt yalnızca statik erişilebilirlik analiziyle ilgilidir.

**2. Dinamik özellikler ipucuyla (hint) bildirilir.** Yansıma, dinamik vekil, JNI ve kaynak yükleme build anında statik görünmez. Spring, çerçeve tiplerinin çoğu için bu ipucunu **otomatik** üretir; kalanını geliştirici bildirir. Tipik senaryo şudur: Jackson'la serileştirdiğin bir DTO, native image'de yansıma hatasıyla patlar. Çözüm `@RegisterReflectionForBinding(MyDto.class)` yazmaktır; ya da `RuntimeHintsRegistrar` içinde `hints.reflection().registerType(...)` + `@ImportRuntimeHints` kullanmaktır. Bunlar hedef tipin kurucu ve alanlarını yansımaya kaydeder. DTO'yu `static` iç sınıf yapmak (görünürlük değişikliği metadata üretmez), alanları `transient` işaretlemek (yalnız serileştirmeden çıkarır) veya native'den tümüyle vazgeçmek sorunu çözmez.

**3. Proxy'lerde iki ayrı durum.** **CGLIB alt-sınıflama** çalışma zamanında yeni sınıf üretmek demektir ve kapalı dünyada **kısıtlıdır** — serbest değildir. Bu yüzden Spring Boot 3, `@Configuration(proxyBeanMethods = false)` gibi seçeneklerle CGLIB ihtiyacını azaltır. **JDK dinamik proxy'leri** ise, hangi arayüz kümesi için üretilecekleri build anında bildirildiğinde desteklenir (Spring bunu çoğunlukla kendisi bildirir). Yani proxy kavramı tümüyle kalkmaz, ama "hiçbir yapılandırma gerekmez, her şey otomatik" de doğru değildir: uygulama-özel dinamik proxy'ler açık bildirim isteyebilir.

**4. Takas iyi anlaşılmalı.** Native image'in getirisi **hızlı başlangıç ve düşük bellektir**; ısınma yoktur, JIT derleyicisinin ve metadata'nın bellek yükü yoktur. Buna karşılık uzun ömürlü, yüksek-verim servislerde JVM'in profil-uyarlamalı JIT'i zamanla **daha yüksek tepe verime** ulaşır ve ısınma maliyeti bir kez ödenip amortize olur. Native tepe verimde her zaman kazanmaz; kod okunabilirliğiyle de ilgisi yoktur. Bedeli de vardır: native derleme dakikalarca sürer, çok bellek ister ve üretilen ikili **derlendiği işletim sistemi/mimariye özeldir**.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Native anında hazır ve sabit verim verir; JVM ısınma sonrası JIT ile daha yüksek tepe verime ulaşır">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Native vs JVM: başlangıç hızı ile tepe verim takası</text>
  <line x1="60" y1="52" x2="60" y2="184" stroke="#5D7789" stroke-width="1.3"/>
  <line x1="60" y1="184" x2="606" y2="184" stroke="#5D7789" stroke-width="1.3"/>
  <text x="64" y="46" font-size="10.5" fill="#5D7789" font-family="system-ui">verim (throughput)</text>
  <text x="600" y="200" text-anchor="end" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman →</text>
  <path d="M60 92 L200 96 L400 100 L600 104" fill="none" stroke="#25804C" stroke-width="2.4"/>
  <path d="M60 176 L150 152 L235 104 L340 78 L600 60" fill="none" stroke="#186399" stroke-width="2.4"/>
  <line x1="250" y1="60" x2="250" y2="184" stroke="#B26B00" stroke-width="1.2" stroke-dasharray="5 3"/>
  <circle cx="243" cy="99" r="3.5" fill="#B26B00"/>
  <text x="470" y="120" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Native: ms'de hazır, sabit orta verim</text>
  <text x="440" y="52" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">JVM: ısınma sonrası JIT ile yüksek tepe</text>
  <text x="252" y="204" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">kesişim: uzun ömürlü yükte JVM öne geçer</text>
  <text x="70" y="168" font-size="10.5" fill="#186399" font-family="system-ui">JVM: soğuk başlangıç</text>
</svg>
```

### Mülakatta ne sorulur?

- **AOT işleme ne yapar?** Build zamanında bağlamı çözer, `@Conditional`'ları çalıştırır ve bean tanımlarını üretilmiş Java koduna döker.
- **AOT'nin native'siz faydası var mı?** Evet: önden üretilmiş `ApplicationContextInitializer`, **JVM üzerinde de** yansıma-ağırlıklı tarama ve koşul değerlendirmesini azaltıp başlangıcı hızlandırır. GC'yi kapatmakla, çok-thread'liliğe geçirmekle veya tembel (lazy) başlatmayla karıştırılmamalıdır — onlar ayrı mekanizmalardır.
- **Kapalı-dünya ne zorunlu kılar?** Yansıma/proxy ile erişilecek her şey **build anında bildirilmelidir**.
- **Reflection hatası nasıl çözülür?** `@RegisterReflectionForBinding` / `RuntimeHintsRegistrar` / `@ImportRuntimeHints`.
- **CGLIB vs JDK proxy**: CGLIB çalışma-zamanı sınıf üretimi kısıtlı → `proxyBeanMethods=false`; JDK proxy'ler bildirilen arayüz kümesiyle desteklenir.
- **Hangi iş yükü?** **En güçlü aday**: serverless/lambda işlevleri, CLI araçları, hızlı ölçeklenen kısa ömürlü konteynerler. **En zayıf aday**: uzun ömürlü yüksek-verim servisler; saatlerce süren gece batch işlerinde de ısınma amortize olduğu için JIT avantajlıdır.
- **Build maliyeti**: dakikalar süren, bellek yiyen derleme + platforma özgü ikili.

### Tek cümlede

Spring AOT, bağlamı build zamanında çözerek JVM'de bile başlangıcı hızlandırır. GraalVM native derleme ise bunun üstüne kapalı-dünya varsayımıyla milisaniyelik başlangıç ve düşük bellek verir; bedeli yansıma ile vekillerin önceden bildirilmesi, uzun derleme ve tepe verimden feragattir.

# native-1 | Temel
## Soru
Spring Boot bağlamında "AOT işleme" (`process-aot`) temelde ne yapar?
## A
Uygulamayı çalışma zamanında JIT ile derleyip tepe performansı artırır
🤖 JIT çalışma-zamanı derlemesidir; AOT işleme tam tersine build zamanında çalışır.
## B
Build zamanında bağlamı çözüp bean tanımlarını Java koduna döker
🤖 Doğru: Spring AOT motoru bağlamı build anında çözer ve `ApplicationContextInitializer` kodu üretir.
## C
Kullanılmayan sınıfları silerek bytecode'u küçültür; davranış aynı kalır
🤖 Bu bir "shrinker/minifier" tanımı; Spring AOT'nin işi bağlamı önden hazırlamaktır.
## D
Testleri build sırasında otomatik çalıştırıp sonuçlarını raporlar
🤖 Test koşumu ayrı bir aşamadır; AOT işleme bağlam hazırlığıyla ilgilidir.
## Doğru
B
## Açıklama
Spring AOT işleme (`spring-boot:process-aot`), uygulama bağlamını **build zamanında** kurup değerlendirir: `@Conditional` koşullarını çalıştırır, hangi bean'lerin oluşacağını çözer ve bu kararları doğrudan **üretilmiş Java koduna** (bir `ApplicationContextInitializer`) döker. Böylece çalışma zamanında yapılan yansıma-ağırlıklı tarama ve koşul değerlendirmesi azalır; bu hem native image'i mümkün kılar hem de JVM üzerinde bile başlangıcı hızlandırabilir. JIT (A) çalışma-zamanı olayıdır, bytecode küçültme (C) veya test koşumu (D) AOT işlemenin işi değildir.

# native-2 | Temel
## Soru
GraalVM Native Image'e derlenmiş bir Spring Boot uygulamasının klasik JVM'e göre en belirgin çalışma-zamanı kazanımı nedir?
## A
Tepe verim (throughput) her zaman JVM'den yüksektir
🤖 Aksine; uzun ömürlü yükte JIT'in profil optimizasyonu genelde daha yüksek tepe veriminde kazanır.
## B
Yansıma ve dinamik proxy'ler sınırsız ve yapılandırmasız çalışır
🤖 Tam ters: kapalı-dünya nedeniyle bunlar kısıtlanır ve çoğu zaman hint gerektirir.
## C
Kod daha okunaklı hale gelir
🤖 Okunabilirlik derleme moduyla ilgisizdir; kazanım çalışma-zamanı metriklerindedir.
## D
Milisaniye ölçeğinde başlangıç ve düşük bellek ayak izi
🤖 Doğru: ısınma olmadan başlar ve JIT/metadata yükü olmadığından bellek düşer.
## Doğru
D
## Açıklama
Native image'in ana getirisi **hızlı başlangıç** (milisaniyeler) ve **düşük bellek** ayak izidir; çünkü uygulama önceden derlenmiştir, JIT ısınması (warm-up) ve derleyici/metadata bellek yükü yoktur. Bu, sunucusuz işlevler ve hızlı ölçeklenen konteynerler için idealdir. Ancak bu bir **takas**tır: uzun ömürlü, yüksek-verim servislerde JVM'in profil-uyarlamalı JIT'i genelde daha yüksek tepe verimine ulaşır (A yanlış). Yansıma/proxy kapalı-dünya nedeniyle kısıtlanır (B yanlış), okunabilirlik ilgisizdir (C).

# native-3 | Temel
## Soru
Native image'in "kapalı-dünya varsayımı" (closed-world assumption) pratikte neyi zorunlu kılar?
## A
Yansıma/proxy ile erişilecek kod build anında bildirilmelidir
🤖 Doğru: build zamanında ulaşılamayan kod ikiliye dahil edilmez, çalışma zamanında "sonradan" bulunamaz.
## B
Uygulamanın ağ erişimi olmadan, internete kapalı biçimde çalışması gerekir
🤖 "Kapalı-dünya" ağ erişimi değil, build anında tüm ulaşılabilir kodun bilinmesi demektir.
## C
Tüm sınıfların ve metotların `final` olarak işaretlenmesi gerekir
🤖 `final` zorunluluğu yoktur; kısıt kodun build anında görünür olmasıyla ilgilidir.
## D
Uygulamanın tek bir sınıf ve tek bir giriş noktasından oluşması gerekir
🤖 Sınıf sayısı serbesttir; kısıt statik erişilebilirlik analiziyle ilgilidir.
## Doğru
A
## Açıklama
**Kapalı-dünya varsayımı**, GraalVM derleyicisinin build zamanında **ulaşılabilir (reachable)** tüm kodu görebilmesi gerektiği anlamına gelir. Java'nın dinamik yolları (yansıma, dinamik proxy, JNI, kaynak yükleme) statik analizde "görünmez" olduğundan, çalışma zamanında bunlarla erişilecek her sınıf/metot **önceden bildirilmelidir** (reachability metadata). Bildirilmeyen bir hedefe yansımayla erişmek çalışma zamanında hata verir. Bu, ağ erişimiyle (B), `final` ile (C) veya sınıf sayısıyla (D) ilgili değildir.

# native-4 | Orta
## Soru
Jackson'la serileştirdiğiniz bir DTO, native image'de `ClassNotFoundException`/yansıma hatasıyla patlıyor. En yerinde çözüm hangisidir?
## A
DTO'yu `static` bir iç sınıf (inner class) hâline getirmek
🤖 Görünürlük değişikliği reachability metadata'yı üretmez; sorun yansıma bilgisinin eksikliğidir.
## B
Uygulamayı JVM'de çalıştırmaya dönüp native derlemeden tümüyle vazgeçmek
🤖 Sorun çözülmeden kaçınmak; küçük bir hint eklemek yeterliyken native'i bırakmak gereksiz.
## C
DTO için `@RegisterReflectionForBinding` ile yansıma hint'i bildirmek
🤖 Doğru: hedef tipin alan/kurucularını yansımaya kaydeder, GraalVM metadata'yı image'e ekler.
## D
DTO'nun alanlarını `transient` olarak işaretleyip serileştirmeden çıkarmak
🤖 `transient` alanı serileştirmeden çıkarır; eksik reachability metadata sorununu çözmez.
## Doğru
C
## Açıklama
Jackson DTO alanlarına **yansımayla** eriştiğinden, native image'de bu tipin yansıma meta verisi image'e girmelidir. `@RegisterReflectionForBinding(MyDto.class)` (ya da `RuntimeHintsRegistrar` içinde `hints.reflection().registerType(...)` + `@ImportRuntimeHints`), hedef tipin kurucu ve alanlarını yansıma için kaydederek GraalVM'in doğru metadata'yı üretmesini sağlar. Görünürlük değiştirmek (A) veya `transient` (D) metadata üretmez; native'den vazgeçmek (B) sorunu çözmek yerine kaçınmaktır. Spring çoğu çerçeve tipini otomatik hint'ler; bu tür uygulama-özel tiplerde açık bildirim gerekir.

# native-5 | İleri
## Soru
Native image'de dinamik proxy/CGLIB alt-sınıflama ile ilgili aşağıdakilerden hangisi doğrudur?
## A
CGLIB alt-sınıfları çalışma zamanında serbestçe ve sınırsızca üretilebilir
🤖 Kapalı-dünya nedeniyle çalışma-zamanı sınıf üretimi kısıtlıdır; build anında ele alınmalıdır.
## B
`proxyBeanMethods=false` ile CGLIB azalır; JDK proxy arayüzleri bildirilir
🤖 Doğru: native uyumu için proxy'ler ya elenir ya da hedef arayüzler önceden hint'lenir.
## C
Native image proxy kavramını tümüyle kaldırır; hiçbir proxy tipi çalışmaz
🤖 Proxy tümden kalkmaz; JDK dinamik proxy'ler bildirilen arayüz kümesiyle desteklenir.
## D
Proxy davranışı için hiçbir yapılandırma gerekmez; native image her şeyi otomatik halleder
🤖 Spring çoğunu otomatikleştirse de uygulama-özel dinamik proxy'ler bildirim gerektirebilir.
## Doğru
B
## Açıklama
Native image, çalışma zamanında **yeni sınıf üretimini** (CGLIB alt-sınıflama) kapalı-dünya nedeniyle kısıtlar. Bu yüzden Spring Boot 3, native uyumu için CGLIB'e bağımlılığı azaltır: örneğin `@Configuration(proxyBeanMethods=false)` bean-metodu proxy'lemesini kapatır. **JDK dinamik proxy'leri** ise, hangi arayüz kümesi için proxy üretileceği build anında bildirildiğinde (Spring bunu çoğunlukla otomatik yapar) desteklenir. Proxy'ler tümüyle kalkmaz (C), CGLIB çalışma-zamanı üretimi serbest değildir (A) ve uygulama-özel dinamik proxy'ler bazen açık hint gerektirir (D yanlış).

# native-6 | İleri
## Soru
Aşağıdaki iş yüklerinden hangisi native image'e geçmek için en güçlü aday, hangisi en zayıf adaydır?
## A
En güçlü: serverless işlev; en zayıf: uzun ömürlü yüksek-verim servis
🤖 Doğru: native başlangıç/bellekte kazanır ama tepe veriminde JIT uzun ömürlü yükte öne geçer.
## B
En güçlü: uzun ömürlü yüksek-verim servis; en zayıf: kısa ömürlü CLI aracı
🤖 Tam ters: yüksek-verim uzun servis JIT'i tercih eder; CLI native'in ideal alanıdır.
## C
İkisi de eşit adaydır; derleme modu iş yükü karakterini hiç etkilemez
🤖 Derleme modu başlangıç, bellek ve tepe verimini belirgin biçimde etkiler.
## D
En güçlü: saatlerce süren gece batch işi; en zayıf: lambda işlevi
🤖 Uzun batch işlerinde ısınma amortize olur, JIT avantajlıdır; lambda ise native'in güçlü alanıdır.
## Doğru
A
## Açıklama
Native image, **başlangıç süresi ve bellek** kritik olduğunda kazanır: sunucusuz işlevler (soğuk başlangıç), CLI araçları, hızlı ölçeklenen kısa ömürlü konteynerler. Buna karşılık **uzun ömürlü, yüksek-verim** servislerde JIT'in profil-uyarlamalı tepe optimizasyonu zamanla native'in önüne geçer ve ısınma maliyeti bir kez ödenip amortize olur — burada native'in getirisi zayıflar. Dolayısıyla serverless en güçlü, uzun-ömürlü throughput servisi en zayıf adaydır. B ve D tam tersini söyler; C ise derleme modunun iş yükünü etkilemediğini varsayar (yanlış).

# native-7 | İleri
## Soru
Spring AOT işlemenin native derlemeden bağımsız olarak JVM üzerinde bile sağladığı fayda nedir?
## A
Çalışma zamanında bytecode'u yeniden yazarak çöp toplayıcıyı (GC) devre dışı bırakır
🤖 AOT işleme GC ile ilgilenmez ve bytecode'u çalışma zamanında yeniden yazmaz.
## B
Uygulamayı otomatik olarak çok iş parçacıklı (multi-thread) hâle getirir
🤖 AOT işleme eşzamanlılık modelini değiştirmez; bağlam hazırlığıyla ilgilidir.
## C
Tüm bean'leri tembel (lazy) başlatmaya çevirerek bellek kullanımını azaltır
🤖 Lazy başlatma ayrı bir seçenektir; AOT işlemenin özü önden üretilmiş bağlam kodudur.
## D
Bağlam kurulumunu önden üretilmiş koda taşıyıp başlangıcı hızlandırır
🤖 Doğru: JVM'de de önden üretilmiş `ApplicationContextInitializer` yansıma-ağırlıklı başlangıç işini hafifletir.
## Doğru
D
## Açıklama
Spring AOT işleme yalnızca native image için değildir: ürettiği **önden hesaplanmış bağlam kodu** (bean tanımları, çözülmüş koşullar) JVM üzerinde çalıştırıldığında da başlangıç anındaki **yansıma-ağırlıklı tarama ve koşul değerlendirmesini** azaltır, dolayısıyla başlangıç süresini düşürür. Bu, GC'yi kapatmakla (A), eşzamanlılıkla (B) veya lazy başlatmayla (C) ilgili değildir — o seçenekler ayrı mekanizmalardır. Kısacası AOT işleme native derlemenin ön koşulu olsa da, tek başına JVM modunda da ölçülebilir bir başlangıç kazancı sunar.
