---
slug: jvm-mimari-classloader
baslik: "JVM Mimarisi ve ClassLoader"
kademe: İleri
sira: 59
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Yazdığınız `.java` dosyası derlenince `.class` dosyasına dönüşür; içinde işlemcinin değil, **JVM'in (Java Sanal Makinesi)** anladığı komutlar vardır. JVM, bilgisayarınızın içinde çalışan bir tür "sahte bilgisayar"dır ve bu komutları yürütür. Peki bu `.class` dosyaları belleğe nasıl girer? Bir kütüphaneyi düşünün: kitap rafta durur, ama okunabilmesi için birinin gidip alması, sahte/yırtık olup olmadığını kontrol etmesi ve masaya koyması gerekir. **Sınıf yükleme (class loading)** tam olarak budur; bu işi yapan görevlilere de **sınıf yükleyici (ClassLoader)** denir.

### Temel kavramlar

- **Sınıf yükleme (class loading)**: bir `.class` dosyasının baytlarının bulunup okunması ve JVM içinde onu temsil eden bir `Class` nesnesine dönüştürülmesi.
- **Sınıf yolu (classpath)**: JVM'in sınıf dosyalarını arayacağı yerlerin listesi.
- **Statik ilklendirici (`static {}`)**: sınıf ilk kez kullanılırken **bir kez** çalışan blok.
- **Sembolik referans**: bayt kodunda sınıf/metot/alan adlarının yazılı hâli; çözümleme adımında gerçek hedeflere bağlanır.
- **Sınıf yükleyici (ClassLoader)**: sınıf baytlarını bulup yükleyen nesne. Yükleyiciler bir **hiyerarşi** oluşturur ve **ebeveyne devretme (parent delegation)** ilkesiyle çalışır: bir yükleyici, sınıfı kendisi yüklemeden önce isteği **önce ebeveynine** iletir; ebeveyn zinciri bulamazsa kendisi yükler. Zincir **Bootstrap → Platform → Application (System)** biçimindedir; kök Bootstrap'tır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sınıf yükleyici hiyerarşisi ve ebeveyne devretme: istek yukarı çıkar, yükleme aşağı iner">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ebeveyne devretme (parent delegation)</text>
  <rect x="200" y="40" width="240" height="46" rx="10" fill="#E3EEF9" stroke="#2C6BB3" stroke-width="1.5"/>
  <text x="320" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#2C6BB3" font-family="system-ui">Bootstrap (java.base çekirdeği)</text>
  <rect x="200" y="104" width="240" height="46" rx="10" fill="#E9F1EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="132" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Platform (eski: Extension)</text>
  <rect x="200" y="168" width="240" height="46" rx="10" fill="#FBF1E6" stroke="#B5771F" stroke-width="1.5"/>
  <text x="320" y="196" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B5771F" font-family="system-ui">Application (System / uygulama)</text>
  <text x="120" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">istek</text>
  <text x="120" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">YUKARI ↑</text>
  <text x="530" y="120" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yükleme</text>
  <text x="530" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">AŞAĞI ↓</text>
  <text x="320" y="238" text-anchor="middle" font-size="12" font-weight="600" fill="#1C3247" font-family="system-ui">Önce ebeveyne sor; bulamazsa kendin yükle.</text>
</svg>
```

### Nasıl çalışır?

Hiyerarşinin her katmanının işi bellidir. **Bootstrap**, `java.base` gibi çekirdek modülleri yükler; çoğunlukla yerel (native) koddur ve Java tarafında `null` olarak görünür. **Platform** yükleyici, Java 9 (JPMS) öncesinde **Extension** yükleyici adını taşıyordu; JDK'nin çekirdek dışı platform sınıflarını yükler. Java 9 hiyerarşiyi **kaldırmadı**, yalnız Extension'ın yerine Platform'u koydu (eski `ext` uzantı dizini mekanizması ise kaldırıldı). **Application (System)** yükleyici, sınıf yolundaki uygulama sınıflarını yükler ve tipik olarak `getClass().getClassLoader()`'ın döndürdüğüdür.

Devretmenin amacı güvenlik ve tutarlılıktır: istek **yukarı** çıkar, yükleme **aşağı** iner. `java.lang.String` gibi çekirdek sınıflar her zaman Bootstrap'tan geldiği için kötü niyetli bir uygulama aynı adla sahte bir çekirdek sınıf enjekte edemez. (Bazı uygulama sunucuları bilinçli olarak tersini — "önce kendisi dener" yani child-first modelini — kullanır; standart JDK modeli parent-first'tür.)

Kendi **özel sınıf yükleyicinizi (custom ClassLoader)** de yazabilirsiniz. `java.lang.ClassLoader`'dan türetilir; ebeveyne devretme bozulmadan genelde **`findClass(String)`** metodu geçersiz kılınır (override). Sınıf baytları istenen kaynaktan okunur: şifreli bir dosya, ağ, veritabanı ya da üretilmiş bayt kodu. Okunan baytlar **`defineClass(name, bytes, 0, bytes.length)`** ile bir `Class` nesnesine dönüştürülür. Bu kalıp eklenti (plugin) sistemlerinde, sıcak yeniden yüklemede (hot reload) ve uygulama sunucularındaki sınıf izolasyonunda kullanılır. Bunun için bir ek açıklama (annotation) ya da `-Xloader` gibi bir JVM bayrağı yoktur; özel yükleyici yalnızca kodla yazılır.

Bir `.class`, kullanılabilir hâle gelene dek **üç aşamadan** geçer ve bu sıra sabittir:

- **Yükleme (loading)**: sınıf baytları bulunur, okunur; JVM içinde bir `Class` nesnesi üretilir.
- **Bağlama (linking)**: kendi içinde üçe bölünür — **doğrulama (verify)**: bayt kodu güvenli mi; **hazırlık (prepare)**: statik alanlara varsayılan değer verilir; **çözümleme (resolve)**: sembolik referanslar bağlanır.
- **İlklendirme (initialization)**: `static {}` blokları ve statik alan atamaları çalışır — ama yalnız **ilk aktif kullanımda**.

İlklendirme **tembeldir (lazy)**. Tetikleyiciler: `new` ile örnekleme, statik metot çağrısı, statik alana erişim, `Class.forName(...)`. Sınıfın yalnızca sınıf yolunda (classpath) bulunması ya da `Ayar a;` gibi bir tip bildirimi ilklendirmeyi **tetiklemez**; tip bildirimi sınıfı olsa olsa yükletir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sınıf hazırlama aşamaları: yükleme, bağlama doğrula hazırla çözümle, ilklendirme">
  <defs><marker id="cls-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker></defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sınıf hazırlama aşamaları</text>
  <rect x="24" y="52" width="120" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1. Yükleme</text>
  <text x="84" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">baytlar → Class</text>
  <rect x="184" y="52" width="256" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="312" y="72" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2. Bağlama (linking)</text>
  <text x="312" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">doğrula → hazırla → çözümle</text>
  <rect x="480" y="52" width="136" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="548" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">3. İlklendirme</text>
  <text x="548" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">static {} çalışır</text>
  <line x1="144" y1="77" x2="182" y2="77" stroke="#5D7789" stroke-width="1.5" marker-end="url(#cls-arw)"/>
  <line x1="440" y1="77" x2="478" y2="77" stroke="#5D7789" stroke-width="1.5" marker-end="url(#cls-arw)"/>
  <rect x="184" y="118" width="256" height="58" rx="8" fill="#EAF1F7" stroke="#5D7789" stroke-width="1"/>
  <text x="312" y="139" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">doğrula: bytecode güvenli mi</text>
  <text x="312" y="156" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hazırla: statik alanlara varsayılan</text>
  <text x="312" y="172" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çözümle: sembolik referanslar bağlanır</text>
  <text x="320" y="198" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">İlklendirme tembeldir: ilk aktif kullanımda çalışır.</text>
</svg>
```

Yükleme başarısız olduğunda karşılaşılan iki hata mülakatlarda sık karıştırılır; ayrım **ne zaman** ve **nasıl** aranıp bulunamadığındadır. **`ClassNotFoundException`** bir **kontrol edilen istisnadır (checked exception)**: sınıf çalışma zamanında **dinamik olarak** aranırken (`Class.forName("...")`, `ClassLoader.loadClass`, reflection) bulunamamıştır — "adını verdim, yoktu". **`NoClassDefFoundError`** ise bir **`Error`**'dur: sınıf **derleme zamanında vardı** (kod ona doğrudan başvuruyordu) ama çalışma zamanında sınıf yolunda yoktu ya da sınıfın **statik ilklendirmesi patladı** — "derlerken vardı, kayboldu". Bu bir derleme hatası değildir; arayüz (interface) ile sınıf ayrımıyla da ilgisi yoktur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ClassNotFoundException dinamik aramada atılan checked exception, NoClassDefFoundError derlemede var çalışmada yok bir Error">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki "sınıf bulunamadı" hatası</text>
  <rect x="28" y="42" width="288" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">ClassNotFoundException</text>
  <text x="172" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">checked Exception</text>
  <text x="172" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dinamik arama: Class.forName,</text>
  <text x="172" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">reflection ile bulunamayınca</text>
  <text x="172" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"adını verdim, yoktu"</text>
  <rect x="324" y="42" width="288" height="140" rx="12" fill="#F5DADA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="468" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">NoClassDefFoundError</text>
  <text x="468" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bir Error</text>
  <text x="468" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">derlemede vardı, çalışmada yok</text>
  <text x="468" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ya da statik init patladı</text>
  <text x="468" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"derlerken vardı, kayboldu"</text>
</svg>
```

### Mülakatta ne sorulur?

- **Aşama sırası.** **Yükleme → Bağlama → İlklendirme**; bu sıra sabittir, çünkü bir sınıf ilklendirilmeden önce mutlaka yüklenmiş ve bağlanmış olmalıdır. Bağlamanın alt adımları: doğrula → hazırla (statik alanlara varsayılan değer) → çözümle (sembolik referanslar).
- **Tembel ilklendirme.** `Ayar.YOL` gibi bir statik alana **ilk erişim** aktif kullanımdır: sınıf tam o anda ilklendirilir ve `static {}` bloğu **bir kez** çalışır. Sınıf yolunda bulunmak veya `Ayar a;` diye tip bildirmek yetmez; "hiç ilklendirilmez, statik bloklar hiç çalışmaz" da yanlıştır. (İnce nokta: değeri derleme zamanında sabitlenen bir `static final` sabite erişim, sabit katlaması nedeniyle ilklendirmeyi tetiklemeyebilir; değer çalışma zamanında hesaplanıyorsa tetikler.)
- **Devretme yönü.** Önce ebeveyne sor, bulunmazsa kendin yükle. Application, Bootstrap'tan öncelikli **değildir**; `java.lang` çekirdeğini Bootstrap yükler. Yükleyiciler rastgele seçilmez; hiyerarşi Java 9 ile de kaldırılmadı.
- **İki hata.** `ClassNotFoundException` = checked exception + dinamik arama; `NoClassDefFoundError` = `Error` + derlemede var/çalışmada yok (ya da statik ilklendirme hatası). Aynı şey değildirler, aralarındaki fark yalnız isim değildir.
- **Özel yükleyici.** `ClassLoader`'dan türet → `findClass` override et → `defineClass` çağır.

### Tek cümlede

JVM her sınıfı **yükle → bağla → (ilk aktif kullanımda tembelce) ilklendir** sırasıyla hazırlar; bu işi yapan sınıf yükleyiciler **Bootstrap → Platform → Application** zincirinde önce ebeveyne devrederek çalışır.

# clsload-1 | İleri
## Soru
Bir sınıfın yüklenmesindeki aşamalar hangi doğru sırayla gerçekleşir?
## A
İlklendirme (initialization) → Bağlama (linking) → Yükleme (loading)
🤖 Ters sıra; önce yükleme, sonra bağlama, en son ilklendirme gelir.
## B
Bağlama (linking) → İlklendirme (initialization) → Yükleme (loading)
🤖 Yükleme her zaman ilk aşamadır; bu sıra yanlış.
## C
Yükleme (loading) → İlklendirme (initialization) → Bağlama (linking)
🤖 İlklendirme bağlamadan sonra gelir; sıra karışık.
## D
Yükleme (loading) → Bağlama (linking) → İlklendirme (initialization)
🤖 Doğru: load → link → init sırası standarttır.
## Doğru
D
## Açıklama
JVM bir sınıfı üç aşamada hazırlar: (1) **Yükleme (loading)** — sınıf baytları bulunur ve bir `Class` nesnesi oluşturulur; (2) **Bağlama (linking)** — kendi içinde **doğrulama (verify)**, **hazırlık (prepare, statik alanlara varsayılan/default değer)** ve **çözümleme (resolve, sembolik referansların bağlanması)** adımlarını içerir; (3) **İlklendirme (initialization)** — statik bloklar ve statik atamalar sınıfın **ilk aktif kullanımında** çalışır. Bu sıra sabittir: bir sınıf ilklendirilmeden önce mutlaka yüklenmiş ve bağlanmış olmalıdır. A, B, C sırayı bozduğu için yanlıştır.

# clsload-2 | İleri
## Soru
"Ebeveyne devretme (parent delegation)" ilkesi nasıl çalışır?
## A
Bir yükleyici, sınıfı kendisi yüklemeden önce isteği ebeveynine iletir; ebeveyn zinciri bulamazsa sınıfı kendisi yükler
🤖 Doğru: önce yukarı devret, bulunmazsa aşağıda yükle.
## B
Her yükleyici sınıfı önce kendisi yüklemeye çalışır; yalnızca başaramazsa ebeveynine sorar
🤖 Bu "child-first" modelidir (bazı uygulama sunucuları bilinçli kullanır); standart JDK modeli parent-first'tür.
## C
Sınıflar rastgele seçilen bir yükleyici tarafından yüklenir; yükleyiciler arasında bir hiyerarşi yoktur
🤖 Hiyerarşi vardır ve devretme sırası belirlidir; rastgele değildir.
## D
Application yükleyici Bootstrap'tan önceliklidir; `java.lang` gibi çekirdek sınıfları da o yükler
🤖 Çekirdek (`java.base`) sınıflarını Bootstrap yükler; Application değil.
## Doğru
A
## Açıklama
**Ebeveyne devretme**, güvenlik ve tutarlılık için tasarlanmış temel ilkedir: bir yükleyici bir sınıf istendiğinde, önce isteği **ebeveynine** iletir; o da kendi ebeveynine... zincir Bootstrap'a kadar çıkar. Ebeveynlerden hiçbiri bulamazsa istek geri iner ve yükleyici sınıfı **kendisi** yükler. Bu sayede `java.lang.String` gibi çekirdek sınıflar her zaman **Bootstrap** tarafından yüklenir ve kötü niyetli bir uygulama, aynı adla sahte bir çekirdek sınıf enjekte edemez. B (önce kendisi), C (hiyerarşi yok) ve D (Application öncelikli) bu ilkeye aykırıdır.

# clsload-3 | İleri
## Soru
Standart JDK sınıf yükleyici hiyerarşisi (yukarıdan aşağıya) hangisidir?
## A
Application (System) → Platform → Bootstrap; kök uygulama yükleyicisidir
🤖 Ters; Bootstrap en üstte, Application en altta olmalı.
## B
Platform → Bootstrap → Application (System); kök Platform yükleyicidir
🤖 Bootstrap köktür; ondan önce Platform gelemez.
## C
Bootstrap → Platform → Application (System); kök Bootstrap yükleyicidir
🤖 Doğru: kök Bootstrap, sonra Platform, en altta Application.
## D
Tek bir yükleyici vardır; hiyerarşi Java 9 ile kaldırıldı
🤖 Hiyerarşi yerinde duruyor; Java 9 (JPMS) ile Extension yükleyicinin yerini Platform yükleyici aldı.
## Doğru
C
## Açıklama
Hiyerarşi kökten yaprağa doğru **Bootstrap → Platform → Application** biçimindedir. **Bootstrap**, `java.base` gibi çekirdek modülleri yükler (çoğunlukla yerel/native koddur, `null` olarak görünür). **Platform** yükleyici, Java 9 öncesinde **Extension** yükleyici olarak adlandırılıyordu; JDK'nin çekirdek dışı platform sınıflarını yükler. **Application (System)** yükleyici, sınıf yolundaki (classpath) uygulama sınıflarını yükler ve tipik olarak `getClass().getClassLoader()`'ın döndürdüğüdür. A ters sırayı verir; B Bootstrap'ı köke koymaz; D ise hiyerarşinin kaldırıldığını iddia eder — oysa Java 9 (JPMS) hiyerarşiyi korudu, yalnızca eski **Extension** yükleyicinin yerini **Platform** yükleyici aldı (eski `ext` uzantı dizini mekanizması ise kaldırıldı).

# clsload-4 | Uzman
## Soru
`ClassNotFoundException` ile `NoClassDefFoundError` arasındaki fark nedir?
## A
İkisi de aynı durumu bildirir; aralarında yalnızca isim farkı vardır, davranışları özdeştir
🤖 Farklıdırlar: biri checked exception, diğeri Error'dur ve farklı senaryolarda oluşur.
## B
`ClassNotFoundException` dinamik aramada (`Class.forName`, reflection) atılan checked exception'dır; `NoClassDefFoundError` derlemede var olan sınıf çalışma zamanında yoksa atılan Error'dur
🤖 Doğru: biri dinamik arama (checked exception), diğeri derleme-var/çalışma-yok (Error).
## C
`NoClassDefFoundError` bir derleme zamanı hatasıdır; sınıf eksikse kod hiç derlenmez, çalışma zamanına kalmaz
🤖 Derleme hatası değildir; çalışma zamanında atılan bir Error'dur.
## D
`ClassNotFoundException` yalnız arayüzler (interface), `NoClassDefFoundError` yalnız sınıflar bulunamayınca atılır; ayrım tamamen tür çeşidine dayanır
🤖 Böyle bir arayüz/sınıf ayrımı yoktur; ayrım senaryoya dayanır.
## Doğru
B
## Açıklama
İkisi de "sınıf bulunamadı" der ama **farklı senaryolara** işaret eder. **`ClassNotFoundException`** bir **checked exception**'dır ve sınıf **açıkça, dinamik olarak** aranırken (`Class.forName("...")`, `ClassLoader.loadClass`, reflection) bulunamadığında atılır — yani "adını verip yüklemeye çalıştım, yoktu". **`NoClassDefFoundError`** ise bir **`Error`**'dur: sınıf **derleme zamanında mevcuttu** (kod ona doğrudan başvuruyordu) ama çalışma zamanında sınıf yolunda yoktu ya da sınıfın **statik ilklendirmesi başarısız oldu** — yani "derlerken vardı, çalışırken kayboldu/patladı". A (aynı), C (derleme hatası) ve D (interface/class ayrımı) yanlıştır; asıl ayrım **checked exception (dinamik arama)** ile **Error (derleme-var/çalışma-yok)** arasındadır.

# clsload-5 | İleri
## Soru
Kendi özel sınıf yükleyicinizi (custom ClassLoader) yazmak için tipik olarak ne yaparsınız?
## A
`ClassLoader`'dan türetip `findClass(String)` metodunu geçersiz kılar, baytları `defineClass` ile `Class`'a dönüştürürsünüz
🤖 Doğru: `findClass`'ı override edip `defineClass` çağırmak standart kalıptır.
## B
`main` metoduna `@ClassLoader` ek açıklaması (annotation) koyup yükleyici sınıfını belirtirsiniz
🤖 Böyle bir annotation yoktur; özel yükleyici sınıf türeterek yazılır.
## C
`Thread` sınıfını genişletir, `run` metodunu geçersiz kılıp sınıfları orada yüklersiniz
🤖 İplik ile ilgisi yoktur; `ClassLoader` türetilir.
## D
JVM bayrağı `-Xloader` ile yükleyici sınıfını belirtir, hiç kod yazmazsınız
🤖 Böyle bir bayrak yoktur; özel yükleyici kodla tanımlanır.
## Doğru
A
## Açıklama
Özel bir sınıf yükleyici yazmanın standart yolu, `java.lang.ClassLoader`'dan **türetmek** ve ebeveyne devretme ilkesini bozmadan **`findClass(String name)`** metodunu geçersiz kılmaktır. Bu metotta sınıf baytlarını istediğiniz kaynaktan (şifreli dosya, ağ, veritabanı, üretilmiş bayt kodu...) okur ve **`defineClass(name, bytes, 0, bytes.length)`** ile bir `Class` nesnesine dönüştürürsünüz. Bu kalıp; eklenti sistemleri (plugin), sıcak yeniden yükleme (hot reload) ve uygulama sunucularındaki (app server) sınıf izolasyonu için kullanılır. B, C, D uydurma mekanizmalardır; özel yükleyici yalnızca `ClassLoader` türetilerek yazılır.

# clsload-6 | İleri
## Soru
Aşağıdaki kodda hangi olay sınıfın **ilklendirilmesini (initialization)** tetikler?
```java
class Ayar {
    static final String YOL;
    static { YOL = hesapla(); System.out.println("init"); }
}
// ...
String p = Ayar.YOL;   // (ilk erişim)
```
## A
Sınıf sınıf yolunda göründüğü anda, hiç kullanılmadan önce ilklendirilir
🤖 Sadece görünmek yetmez; ilklendirme ilk aktif kullanıma kadar ertelenir.
## B
`Ayar` tipinde bir değişken **bildirmek** (ör. `Ayar a;`) ilklendirmeyi tetikler
🤖 Yalnız tip bildirimi ilklendirmeyi tetiklemez; aktif kullanım gerekir.
## C
Statik alana ilk kez erişildiğinde (`Ayar.YOL`) sınıf ilklendirilir; `static {}` bloğu bu noktada çalışır
🤖 Doğru: statik alana ilk aktif erişim ilklendirmeyi tetikler.
## D
Sınıf hiçbir zaman ilklendirilmez; `static` bloklar hiç çalışmaz
🤖 Yanlış; ilk aktif kullanımda statik blok bir kez çalışır.
## Doğru
C
## Açıklama
JVM sınıf ilklendirmesini **tembel (lazy)** yapar: bir sınıf, **ilk aktif kullanımına** kadar ilklendirilmez. Bu koddaki `Ayar.YOL` erişimi böyle bir aktif kullanımdır ve tam o anda `static {}` bloğu **bir kez** çalışır (`"init"` yazdırılır, `YOL` hesaplanır). Yalnızca sınıfın sınıf yolunda bulunması (A) ya da `Ayar` tipinde bir değişken bildirmek (B) ilklendirmeyi tetiklemez — tip bildirimi sınıfı yalnız yükletebilir, ilklendirmez. D yanlıştır çünkü ilk aktif kullanımda statik blok kesinlikle çalışır. (Diğer tetikleyiciler: `new` ile örnekleme, statik metot çağrısı, `Class.forName` varsayılan biçimi. Not: derleme-zamanı sabiti olan `static final` bir sabite erişim ise ilklendirmeyi tetiklemeyebilir; burada değer çalışma zamanında `hesapla()` ile üretildiğinden sabit katlaması olmaz ve ilklendirme tetiklenir.)
