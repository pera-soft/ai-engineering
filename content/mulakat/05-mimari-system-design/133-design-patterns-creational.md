---
slug: design-patterns-creational
baslik: "Yaratımsal Kalıplar: Singleton (enum), Factory, Builder, Prototype ve DI İlişkisi"
kademe: Orta
sira: 133
etiketler: 6
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoranı düşün: müşteri mutfağa girip tencereyi karıştırmaz, garsona "köfte" der. Yemeğin **nasıl** hazırlandığı mutfağın işidir; müşteri yalnız **ne** istediğini söyler. Yaratımsal (creational) kalıplar yazılımda bu ayrımı kurar: nesnenin **nasıl yaratıldığını** onu kullanan koddan gizler. Yaratım kuralı bir gün değişir: yeni bir tür gelir, kurulum pahalılaşır ya da tek örnek şart olur. Kural tek yerde durduğu için çağıran kodun tamamını dolaşmak zorunda kalmazsın.

### Temel kavramlar

- **Tasarım deseni (design pattern)**: sık karşılaşılan bir tasarım sorununa verilen, adı konmuş ve tekrar tekrar işe yaramış çözüm şablonu. Hazır kod değildir; bir **fikirdir**, her projede yeniden yazılır. Adı olması, ekipçe tek kelimeyle anlaşmayı sağlar.
- **Örnek / nesne (instance)**: bir sınıftan `new` ile üretilen somut kopya.
- **Kurucu (constructor)**: nesne doğarken çalışan, alanlarını dolduran özel metot. Parametreleri arttıkça çağrılar okunmaz hâle gelir.
- **Teleskopik kurucu (telescoping constructor)**: aynı sınıf için giderek uzayan kurucular; `new HttpIstek(url, null, null, 30, null, true, ...)` gibi sıra hatasına açık çağrılar üretir.
- **Klonlama (cloning)**: var olan bir nesnenin kopyasını çıkarmak. Kopya **sığ (shallow)** ise iç nesneler kopyalanmaz, iki nesne aynı iç nesneleri paylaşır. Kopya **derin (deep)** ise iç nesneler de ayrıca kopyalanır. İkisi karıştırılırsa sinsi hatalar doğar: birinden değiştirdiğin veri diğerinde de değişir.

Beş temel kalıp:

- **Singleton** — bir sınıftan **tek örnek** bulunmasını garanti eder.
- **Factory Method** — hangi **somut sınıfın** yaratılacağı kararını bir metodun/alt sınıfın arkasına gizler; çağıran yalnız arayüzü görür.
- **Abstract Factory** — **birlikte kullanılan uyumlu ürün ailelerini** (ör. `WindowsButon` + `WindowsKutucuk`) tek elden üretir. Factory Method tek ürünün tipini seçer; Abstract Factory bir **aileyi** tutarlı tutar.
- **Builder** — çok parametreli, opsiyonel alanlı nesneyi **adı belli adımlarla** kurar; `build()` içinde doğrulama yapılıp değişmez (immutable) nesne döndürülebilir.
- **Prototype** — yeni nesneyi sıfırdan kurmak yerine hazır bir örneği **klonlayarak** üretir; kurulum pahalıysa bu maliyeti bir kez ödersin.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Fabrika, yaratım kararını çağırandan gizler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fabrika: 'ne' ile 'nasıl yaratıldığı' ayrılır</text>
  <rect x="34" y="70" width="150" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="109" y="94" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="109" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">new bilmez</text>
  <rect x="245" y="70" width="150" height="56" rx="10" fill="#FBE7D8" stroke="#B9791E" stroke-width="1.5"/>
  <text x="320" y="94" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B9791E" font-family="system-ui">Fabrika</text>
  <text x="320" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kararı verir</text>
  <rect x="456" y="42" width="150" height="42" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="531" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">PdfRapor</text>
  <rect x="456" y="112" width="150" height="42" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="531" y="138" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">ExcelRapor</text>
  <path d="M184 98 L245 98" stroke="#186399" stroke-width="1.6"/>
  <path d="M395 92 L456 66" stroke="#B9791E" stroke-width="1.4"/>
  <path d="M395 104 L456 130" stroke="#B9791E" stroke-width="1.4"/>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istemci yalnız arayüzü (Rapor) görür</text>
</svg>
```

### Nasıl çalışır?

**Singleton'ın tuzakları.** Java'da en sağlam yol tek elemanlı **`enum`**'dur:

```java
public enum Yapilandirma { INSTANCE; public String getDeger() { /* ... */ } }
```

`enum` üç korumayı tek başına verir. Birincisi, JVM bir sınıfı yalnız bir kez yüklediği için başlatma **iş parçacığı güvenlidir (thread-safe)**; iki iş parçacığı aynı anda girse bile ikinci örnek doğmaz. İkincisi, nesne diske veya ağa yazılıp geri okunsa bile (**serileştirme**) aynı örnek geri gelir; `readResolve` yazmana gerek kalmaz. Üçüncüsü, **yansıma (reflection)** — programın kendi sınıflarını çalışma anında inceleyip `newInstance` ile nesne üretmesi — `enum` üzerinde çalışmaz.

Elle yazılan alternatifler eksik kalır. `public static final` bir alan tekliği sağlar; ama nesne serileştirmede kopyalanabilir, yansımayla ikinci kez üretilebilir. `getInstance()` metodunu `synchronized` yapmak doğru sonuç verir; ama her okumada gereksiz kilit maliyeti getirir. **Çift kontrollü kilitleme (double-checked locking)** ise alan `volatile` değilse **bozuktur**: başka bir iş parçacığı yarım kurulmuş nesneyi görebilir.

Asıl bedel mimaridir. Statik singleton **gizli, global bir bağımlılıktır**: testte yerine sahte bir nesne koyamazsın ve bir testin bıraktığı durum diğerine sızar.

```svg
<svg viewBox="0 0 640 182" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Builder: teleskopik kurucu yerine okunur adım adım kurulum">
  <defs>
    <marker id="crea-build-ok" markerWidth="10" markerHeight="10" refX="8" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Builder: teleskopik kurucu cehennemini çözer</text>
  <rect x="34" y="50" width="252" height="96" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="160" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Teleskopik kurucu</text>
  <text x="160" y="98" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">new HttpIstek(url, null,</text>
  <text x="160" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">null, 30, null, true, ...)</text>
  <text x="160" y="134" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">okunmaz, sıra hatası kolay</text>
  <rect x="354" y="50" width="252" height="96" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="480" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Builder — adlı adımlar</text>
  <text x="372" y="96" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">.url("/api").zamanAsimi(30)</text>
  <text x="372" y="112" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">.yenidenDene(true).build()</text>
  <text x="480" y="134" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">okunur + doğrulama + değişmez</text>
  <line x1="286" y1="98" x2="354" y2="98" stroke="#25804C" stroke-width="1.8" marker-end="url(#crea-build-ok)"/>
</svg>
```

**Builder ne çözer?** Zorunlu ve opsiyonel alanları karışık bir nesnede teleskopik kurucu okunmaz olur. Builder her alanı **isimlendirilmiş bir adımla** verir:

```java
HttpIstek istek = HttpIstek.builder()
    .url("/api").zamanAsimi(30).yenidenDene(true)
    .build();            // burada doğrulama + değişmez nesne
```

**DI konteyneri aslında bir fabrikadır.** Spring, **kendi yönettiği nesneleri (bean)** kendisi üretir ve yaşam döngülerini yönetir. Varsayılan `singleton` **kapsamı (scope)** "uygulama başına tek örnek" demektir. Yani tekliği **konteyner** sağlar. Ama bu örnek kurucudan (constructor) **enjekte edildiği** için testte yerine bir sahte nesne (mock/fake) geçirilebilir. Statik singleton'ın test zararları böylece ortadan kalkar.

```svg
<svg viewBox="0 0 640 186" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="DI konteyneri bir fabrikadır: singleton scope test edilebilir kalır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">DI konteyneri = fabrika; teklik test edilebilir kalır</text>
  <rect x="34" y="46" width="256" height="112" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="162" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Statik singleton</text>
  <text x="162" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">X.getInstance()</text>
  <text x="162" y="114" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">gizli global bağımlılık</text>
  <text x="162" y="134" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">mock zor, test kirlenir</text>
  <rect x="350" y="46" width="256" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="478" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Konteyner singleton bean</text>
  <text x="478" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">constructor'dan enjekte</text>
  <text x="478" y="114" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">teklik konteynerce sağlanır</text>
  <text x="478" y="134" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">testte mock/fake geçirilir</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Singleton neden koku sayılır?"** Global durum + gizli bağımlılık + test edilemezlik. Modern Spring kodunda elle yazılmış statik singleton bir tasarım sorunudur.
- **"Factory Method ile Abstract Factory farkı?"** İlki **tek bir ürünün** somut tipini seçer; ikincisi **birbiriyle uyumlu ürün ailelerini** üretir ve karışık aile kullanılmasını engeller.
- **"Prototype ne zaman?"** Kurulumu pahalı bir nesneden çok sayıda benzer kopya gerektiğinde. Java'da `Cloneable`/`clone()` kullanılabilir ama sığ/derin kopya ayrımı tehlikelidir; çoğu ekip **kopya kurucusu (copy constructor)** tercih eder.
- **"Kalıp bedava mı?"** Değil. Her kalıp bir **dolaylılık (indirection)** ve okuyana bilişsel yük ekler. Ancak somut bir esneklik ihtiyacını karşıladığında değere döner. Ortalıkta tek somut tip varken her sınıfa fabrika + arayüz sarmak **aşırı mühendisliktir (over-engineering)**. Bunun asıl maliyeti çalışma hızı değil, kodu **anlama ve değiştirme** maliyetidir. Kural **YAGNI** (You Aren't Gonna Need It — "buna ihtiyacın olmayacak"): kalıbı ihtiyaç gerçekten doğduğunda ekle.

### Tek cümlede

Yaratımsal kalıplar, "nesne nasıl doğar" kararını çağıran koddan alıp tek bir yere toplayarak yaratım kuralı değiştiğinde sistemin dört bir yanını dolaşmanı önler.

# crea-1 | Temel
## Soru
Java'da bir Singleton'ı serileştirme (serialization) ve yansıma (reflection) saldırılarına karşı en sağlam ve en az kodla uygulamanın yolu nedir?
## A
Tek elemanlı bir `enum` sabiti tanımlayıp örneği onun üzerinden vermek
🤖 Doğru: enum, tekliği, serileştirme güvenliğini ve reflection korumasını ek kod yazmadan verir.
## B
`public static final` bir alanı sınıf yüklenirken bir kez ilklendirip paylaşmak
🤖 Teklik sağlar ama serileştirmede kopyalanmaya ve reflection ile ikinci örnek üretilmeye açıktır.
## C
`getInstance()` metodunu `synchronized` yapıp her çağrıda kilit almak
🤖 Doğru sonuç verir ama her okumada gereksiz kilit maliyeti taşır; serileştirmeye karşı da korumasızdır.
## D
Alanı `volatile` yapmadan çift kontrollü kilitleme (double-checked locking) yazmak
🤖 `volatile` olmadan DCL bozuktur: başka bir iş parçacığı yarım kurulmuş nesneyi görebilir.
## Doğru
A
## Açıklama
Josh Bloch'un (Effective Java) önerdiği gibi tek elemanlı **`enum`**, Java'da Singleton uygulamanın en sağlam yoludur:

```java
public enum Yapilandirma {
    INSTANCE;
    public String getDeger() { /* ... */ }
}
```

`enum`, JVM'in **sınıf yükleme tekliği** sayesinde thread-safe başlatmayı, `readResolve` gerektirmeden **serileştirme güvenliğini** ve `newInstance` ile ikinci örnek üretilmesini engelleyen **reflection korumasını** kutudan çıktığı gibi verir. Alan tabanlı singleton (B) tekliği sağlar ama iki saldırı vektörüne de açıktır ve kapatmak için ek kod ister. `synchronized getInstance()` (C) doğru çalışır fakat her okumada kilit alır ve yine serileştirme/reflection korumasız kalır — soru "en sağlam ve en az kodla" dediği için elenir. `volatile` olmayan çift kontrollü kilitleme (D) ise klasik bir hatadır: yeniden sıralama (reordering) nedeniyle başka bir iş parçacığı **henüz kurucusu bitmemiş** nesneyi görebilir. Not: modern uygulamalarda singleton ihtiyacı çoğunlukla **DI konteynerinin `singleton` scope**'u ile karşılanır.

# crea-2 | Temel
## Soru
Factory Method (Fabrika Metodu) kalıbının temel amacı nedir?
## A
Bir sınıftan yalnız tek örnek bulunmasını uygulama boyunca garanti altına almak
🤖 Bu Singleton'ın amacıdır; Factory Method örnek sayısını değil, hangi tipin doğacağını konu alır.
## B
Var olan hazır bir nesneyi klonlayarak ondan yeni örnekler türetmek
🤖 Bu Prototype'tır; Factory Method kopyalamaz, somut tip seçimini soyutlar.
## C
Hangi somut sınıfın yaratılacağı kararını çağırandan alıp bir metodun ardına gizlemek
🤖 Doğru: çağıran `new SomutTip()` yazmaz; yalnız arayüzü görür, somut tip fabrikada seçilir.
## D
Birlikte kullanılan uyumlu nesne ailelerini tek elden ve tutarlı biçimde üretmek
🤖 Bu Abstract Factory'nin işidir; Factory Method tek bir ürünün somut tipini seçer.
## Doğru
C
## Açıklama
Factory Method'un özü, **hangi somut sınıfın örnekleneceği** kararını çağıran koddan alıp bir **fabrika metodunun/alt sınıfın** arkasına saklamaktır. Çağıran yalnız soyut tipi (arayüzü) görür: `new PdfRapor()` yazmaz, `raporUret(tur)` çağırır ve geriye `Rapor` arayüzü döner. Böylece yeni bir somut tür eklendiğinde çağıran kod değişmez (OCP ile uyumlu) ve bağımlılık somuta değil soyutlamaya olur (DIP ile uyumlu). Tek örnek garantisi (A) Singleton'ın, klonlama (B) Prototype'ın işidir. En kritik ayrım D ile olandır: **Abstract Factory**, birbiriyle **uyumlu ürün ailelerini** (ör. `WindowsButon` + `WindowsKutucuk`) tek elden üretip karışık aile kullanılmasını engeller; Factory Method ise **tek bir ürünün** somut tipini seçer. Ailenin tutarlılığı gerekmiyorsa Abstract Factory gereksiz ağırlıktır.

# crea-3 | Orta
## Soru
Beş zorunlu, yedi opsiyonel alanı olan bir `HttpIstek` nesnesini `new HttpIstek(a, null, null, b, ...)` gibi teleskopik kuruculardan kurtaracak en uygun kalıp hangisidir ve temel faydası nedir?
## A
Singleton; tek bir istek nesnesini paylaşmak parametre yükünü tamamen ortadan kaldırır
🤖 İstekler birbirinden farklı ve çok sayıdadır; paylaşılan tek örnek bu ihtiyaca uymaz.
## B
Prototype; hazır bir isteği klonlayıp alanlarını değiştirmek çağrıları okunur kılar
🤖 Klonlama kurulum maliyetini çözer, opsiyonel alanların okunabilirlik sorununu değil.
## C
Abstract Factory; farklı istek türlerini üreten uyumlu bir ürün ailesi arayüzü tanımlanır
🤖 Burada tek tipte bir nesnenin kurulumu var; ortada bir ürün ailesi yok.
## D
Builder; nesne, adı belli adımlarla okunur biçimde ve doğrulanarak kurulur
🤖 Doğru: Builder teleskopik kurucu cehennemini çözer; alanlar isimlenir, çağrı okunur olur.
## Doğru
D
## Açıklama
Çok sayıda parametreli — özellikle **opsiyonel** alanları olan — nesneler için **Builder** doğru seçimdir. Teleskopik kurucu yaklaşımı `new HttpIstek(url, null, null, 30, null, true, ...)` gibi okunmaz, sıra hatasına açık çağrılar üretir; üstelik aynı tipte iki parametrenin yeri değişirse derleyici bunu yakalamaz. Builder her alanı **isimlendirilmiş bir adımla** verir:

```java
HttpIstek istek = HttpIstek.builder()
    .url("/api")           // zorunlu
    .zamanAsimi(30)        // opsiyonel
    .yenidenDene(true)     // opsiyonel
    .build();              // burada doğrulama + değişmez nesne
```

`build()` içinde **doğrulama** yapılabilir ve sonuç **değişmez (immutable)** olabilir. Singleton (A) tekliği hedefler ve farklı istekler gerektiğinde uygun değildir; Prototype (B) pahalı kurulumu klonla amorti eder ama opsiyonel alan okunabilirliğine çözüm getirmez; Abstract Factory (C) ürün ailelerini konu alır, burada aile yoktur. Not: Java `record` + kompakt kurucu, alan sayısı azken Builder ihtiyacını azaltır.

# crea-4 | Orta
## Soru
Kurulumu pahalı (ör. büyük bir şablon nesnesi ağır bir ilk yüklemeyle hazırlanan) nesnelerden çok sayıda benzer örnek üretmek gerektiğinde hangi kalıp doğaldır?
## A
Factory Method; fabrika her istekte doğru somut tipi seçip nesneyi sıfırdan kurar
🤖 Sıfırdan kurulum pahalı hazırlığı her seferinde tekrarlar; kaçınmak istediğimiz maliyet buydu.
## B
Prototype; hazır kurulmuş bir örneği klonlayarak ağır hazırlığı tekrarlamaz
🤖 Doğru: klon, pahalı ilk kurulumu bir kez yapıp kopyalayarak maliyeti amorti eder.
## C
Singleton; pahalı nesne bir kez kurulup tüm çağıranlar arasında paylaşılır
🤖 Paylaşılan tek örnek, birbirinden bağımsız çok sayıda kopya ihtiyacını karşılamaz.
## D
Builder; nesne adım adım kurulduğu için ağır hazırlık adımlara yayılıp hafifler
🤖 Adımlara bölmek toplam maliyeti azaltmaz; her örnekte aynı pahalı hazırlık yine yapılır.
## Doğru
B
## Açıklama
**Prototype** kalıbı, yeni nesneyi **sıfırdan kurmak yerine** hazır bir örneği (prototip) **klonlayarak** üretir. Kurulumu pahalı olan (uzun başlatma, ağır konfigürasyon yükleme, dış kaynaktan besleme) nesnelerden çok sayıda benzer kopya gerektiğinde bu ağır hazırlığı **bir kez** yapıp ucuz klonlarla çoğaltmak maliyeti amorti eder. Factory Method (A) ve Builder (D) her örneği yeniden kurar; Builder'da işi adımlara bölmek toplam maliyeti değiştirmez, yalnız okunabilirliği artırır. Singleton (C) ilk bakışta cazip görünür — pahalı nesne bir kez kurulur — ama senaryo "çok sayıda **bağımsız** benzer örnek" ister; tek paylaşılan örnek üzerinde herkesin değişiklik yapması ayrıca yarış koşulu üretir. Java'da klonlama `Cloneable`/`clone()` ile yapılabilir ama **sığ/derin kopya** ayrımına dikkat gerekir; pratikte çoğu ekip `clone()` yerine bir **kopya kurucusu (copy constructor)** ya da kopya fabrika metodu tercih eder.

# crea-5 | İleri
## Soru
Spring gibi bir DI konteyneri kullanıldığında klasik statik Singleton kalıbına neden çoğunlukla gerek kalmaz?
## A
Spring, singleton kalıbını güvenlik gerekçesiyle yasakladığı için bu kalıp kullanılamaz
🤖 Yasaklama yok; aksine varsayılan bean scope'u zaten `singleton`'dur.
## B
Konteyner her enjeksiyonda yeni bean ürettiğinden teklik zaten hiç sağlanamaz
🤖 `singleton` scope'ta konteyner tek örneği tutar ve aynı örneği dağıtır.
## C
Konteyner tekliği kendisi sağlar; örnek enjekte edildiği için testte değiştirilebilir
🤖 Doğru: teklik korunur ama statik global durumun test ve kuplaj zararları gelmez.
## D
Statik singleton daha hızlı çalıştığı için Spring onu arka planda zaten kullanmaktadır
🤖 Hız gerekçesi yanlış; asıl fark test edilebilirlik ve yaşam döngüsünün yönetilmesidir.
## Doğru
C
## Açıklama
Bir DI konteyneri özünde bir **nesne fabrikasıdır** ve bean yaşam döngüsünü yönetir. Spring'de varsayılan `singleton` scope, konteynerin **uygulama başına tek örnek** tuttuğu anlamına gelir — tekliği konteyner sağlar (B'nin iddiasının tersi). Klasik statik singleton'a göre kritik fark **test edilebilirliktir**: statik `getInstance()` bir **gizli, global bağımlılıktır**; onu kullanan sınıfı izole test etmek zordur ve testler arası durum sızıntısı yaratır. DI ile aynı tek örnek **constructor'dan enjekte** edilir; testte yerine bir sahte (mock/fake) geçirebilirsin. Yani teklik korunur ama global statik durumun zararları (gizli bağımlılık, test kirliliği, sıkı kuplaj) ortadan kalkar. Spring singleton'ı yasaklamaz (A) ve tercih gerekçesi hız değildir (D) — asıl kazanç, yaşam döngüsünün yönetilmesi ve bağımlılığın görünür hâle gelmesidir. Bu yüzden modern Spring kodunda elle yazılmış statik singleton bir **koku** sayılır.

# crea-6 | İleri
## Soru
Bir projede her basit veri sınıfı için fabrika, her fabrika için de arayüz açılmış; tek somut tip olmasına rağmen katmanlar dolaylılıkla (indirection) dolu. Senior bir gözle bu durumun asıl sorunu nedir?
## A
Kalıpların gereksiz kullanımı; esneklik ihtiyacı yokken eklenen dolaylılık maliyet üretir
🤖 Doğru: tek somut tip varken fabrika/arayüz katmanı esneklik getirmeden bilişsel yük ekler.
## B
Ortada sorun yok; kalıp sayısı arttıkça kod profesyonelleşir ve bakımı kolaylaşır
🤖 Kalıp sayısı kalite ölçüsü değildir; karşılığı olmayan kalıp bakımı zorlaştırır.
## C
Kalıplar az kullanılmış; her fabrikanın üstüne bir soyut fabrika daha eklenmelidir
🤖 Ters teşhis: zaten fazla dolaylılık var, bir katman daha eklemek derdi büyütür.
## D
Sorun performans; her fabrika çağrısı fazladan nesne yaratıp sistemi belirgin yavaşlatır
🤖 Asıl maliyet çalışma hızı değil, gereksiz karmaşıklığın anlama ve değiştirme yüküdür.
## Doğru
A
## Açıklama
Tasarım kalıpları **bedava değildir**: her kalıp bir **dolaylılık (indirection)** ve bilişsel yük ekler; bu maliyet ancak somut bir esneklik ihtiyacını (birden çok somut tip, değişen yaratım kuralı, test için yer değiştirme) karşıladığında değere dönüşür. **Tek somut tip** varken her sınıfa fabrika + arayüz sarmak, hiçbir esneklik kazandırmadan yalnızca gezinilecek katman sayısını artırır — bu **over-engineering / gereksiz soyutlama** kokusudur. "Ne kadar çok kalıp o kadar iyi" (B) ve "daha fazla soyutlama ekle" (C) yanlış içgüdülerdir; ikincisi mevcut hastalığı ilaç sanır. Performans savunması (D) ise yanlış ekseni tartışır: birkaç ek nesne yaratımı modern JVM'de ölçülebilir bir yük değildir, asıl fatura **anlama ve değiştirme** maliyetidir — yeni gelen geliştirici, tek bir alanı görmek için beş dosya açar. Kural: **YAGNI** (You Aren't Gonna Need It) — kalıbı ihtiyaç gerçekten ortaya çıktığında ekle; erken soyutlama, yanlış soyutlamaya dönüşme riski taşır ve yanlış soyutlamayı sökmek, hiç soyutlamamaktan pahalıdır.
