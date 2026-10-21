---
slug: design-patterns-structural
baslik: "Yapısal Kalıplar: Adapter, Facade, Proxy (Spring'de), Decorator ve Composite"
kademe: Orta
sira: 134
etiketler: 6
---

## Konu anlatımı

### Bu ne işe yarar?

Bir hediye paketi düşün: içindeki kitap değişmez, ama üstüne kâğıt sararsın, kâğıdın üstüne kurdele takarsın, kurdelenin üstüne kart iliştirirsin. Dışarıdan bakan hâlâ "bir hediye" görür. Yapısal (structural) kalıpların ortak fikri budur: **nesneyi başka bir nesneyle sarmalamak (wrapping)**. Sarmalayan, sarılanın yerine geçer; çağıran kod farkı fark etmez. Bu sayede eski bir sınıfı hiç değiştirmeden yeni bir sisteme takabilirsin. Karmaşık bir yapıyı tek düğmeye indirebilir ya da bir çağrının etrafına güvenlik ve **önbellek (cache)** ekleyebilirsin.

### Temel kavramlar

- **Arayüz (interface)**: gövdesiz metot imzalarından oluşan **sözleşme**. "Bu tipi kullanan kod şu metotları çağırabilir" der.
- **Sarmalama (wrapping) / kompozisyon**: bir nesnenin, başka bir nesneyi **alan olarak içinde tutup** çağrıları ona iletmesi. Kalıtımın (`extends`) aksine bu ilişki **çalışma zamanında** kurulur ve değiştirilebilir.
- **Alt sistem (subsystem)**: birlikte bir işi yapan, ama tek tek çağrılması gereken sınıflar kümesi (stok, ödeme, kargo servisleri gibi).
- **Kesişen ilgi (cross-cutting concern)**: iş mantığına ait olmayan ama her yerde lazım olan işler — kayıt tutma (log), güvenlik, **işlem sınırı (transaction)**, önbellek.

Beş kalıp, tek cümlelik niyetleriyle:

- **Adapter (Adaptör)** — uyumsuz iki arayüzü birbirine **çevirir**. Tek kalıp budur ki **girişteki arayüz ile çıkıştaki arayüz farklıdır**; priz çeviricisi gibi.
- **Facade (Cephe)** — karmaşık bir alt sistemin önüne **basit, tek bir giriş** koyar; sadeleştirir, çevirmez.
- **Proxy (Vekil nesne)** — gerçek nesneye **erişimi kontrol eder**: yetkilendirme, **tembel yükleme (lazy loading — ihtiyaç olunca yükleme)**, önbellek, uzak çağrı.
- **Decorator (Dekoratör)** — aynı arayüzü koruyarak nesneye **yeni davranış katmanları ekler**.
- **Composite (Bileşen)** — ağaç yapısında **tek nesne ile nesne grubunu** aynı arayüzden ele almayı sağlar (dosya ve klasörün ikisi de "dosya sistemi öğesi"dir).

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sarmalama: proxy ve decorator gerçek nesneyi aynı arayüzle sarar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sarmalayanlar aynı arayüzü korur</text>
  <rect x="40" y="60" width="150" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="115" y="86" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="115" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Servis arayüzünü</text>
  <text x="115" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çağırır</text>
  <rect x="245" y="60" width="150" height="80" rx="10" fill="#FBE7D8" stroke="#B9791E" stroke-width="1.5"/>
  <text x="320" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#B9791E" font-family="system-ui">Proxy / Decorator</text>
  <text x="320" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">öncesi/sonrası</text>
  <text x="320" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">davranış ekler</text>
  <rect x="450" y="60" width="150" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="86" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Gerçek nesne</text>
  <text x="525" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">asıl işi yapar</text>
  <path d="M190 100 L245 100" stroke="#186399" stroke-width="1.6"/>
  <path d="M395 100 L450 100" stroke="#B9791E" stroke-width="1.6"/>
  <text x="320" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istemci sarmalamanın farkında olmaz</text>
</svg>
```

### Nasıl çalışır?

**Adapter.** Elinde kaynağını değiştiremediğin `EskiOdemeApi` var, sistemin `OdemeGecidi` bekliyor. Araya bir çevirici sınıf koyarsın:

```java
class EskiApiAdaptoru implements OdemeGecidi {
    private final EskiOdemeApi eski;
    public Sonuc ode(BigDecimal tutar) {
        return eski.makePayment(tutar.doubleValue()); // çeviri
    }
}
```

```svg
<svg viewBox="0 0 640 172" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Adapter: giriş arayüzü ile çıkış arayüzü farklıdır">
  <defs>
    <marker id="dpstruct-adapt-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Adapter: beklenen arayüzü, mevcut sınıfa uydurur</text>
  <rect x="24" y="66" width="150" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="90" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sistem bekler</text>
  <text x="99" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">OdemeGecidi.ode()</text>
  <rect x="245" y="60" width="150" height="72" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Adapter</text>
  <text x="320" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">OdemeGecidi'yi</text>
  <text x="320" y="119" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">implemente eder</text>
  <rect x="466" y="66" width="150" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="541" y="90" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">EskiOdemeApi</text>
  <text x="541" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">makePayment()</text>
  <line x1="174" y1="96" x2="243" y2="96" stroke="#186399" stroke-width="1.6" marker-end="url(#dpstruct-adapt-ok)"/>
  <line x1="395" y1="96" x2="464" y2="96" stroke="#186399" stroke-width="1.6" marker-end="url(#dpstruct-adapt-ok)"/>
  <text x="320" y="156" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">giriş arayüzü ≠ çıkış arayüzü; eski sınıf değişmez</text>
</svg>
```

**Facade.** İstemci `siparisiTamamla()` der; arkada stok, ödeme ve kargo servisleri doğru sırayla çağrılır. İstemci bu sırayı ve sınıfları bilmez, dolayısıyla onlara **kuplaj kurmaz**. Facade alt sistemi yasaklamaz; sadece kolay yolu sunar.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Facade: karmaşık alt sistemin önünde tek basit arayüz">
  <defs>
    <marker id="dpstruct-fac-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#B26B00"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Facade: tek çağrı, alt sistemi koordine eder</text>
  <rect x="30" y="78" width="140" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="100" y="102" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <text x="100" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">siparisiTamamla()</text>
  <rect x="230" y="72" width="150" height="68" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="305" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">SiparisFacade</text>
  <text x="305" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iç sırayı gizler</text>
  <rect x="450" y="42" width="160" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="530" y="64" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">StokServisi</text>
  <rect x="450" y="89" width="160" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="530" y="111" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">OdemeServisi</text>
  <rect x="450" y="136" width="160" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="530" y="158" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">KargoServisi</text>
  <line x1="170" y1="106" x2="228" y2="106" stroke="#186399" stroke-width="1.6"/>
  <line x1="380" y1="100" x2="448" y2="60" stroke="#B26B00" stroke-width="1.4" marker-end="url(#dpstruct-fac-ok)"/>
  <line x1="380" y1="106" x2="448" y2="106" stroke="#B26B00" stroke-width="1.4" marker-end="url(#dpstruct-fac-ok)"/>
  <line x1="380" y1="112" x2="448" y2="152" stroke="#B26B00" stroke-width="1.4" marker-end="url(#dpstruct-fac-ok)"/>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">istemci iç karmaşıklığa kuplaj kurmaz</text>
</svg>
```

**Decorator.** Kalıtımla her kombinasyonu ayrı sınıf yapmak (`SutluSekerliKremaliKahve`) **sınıf patlamasına** yol açar. Decorator'da her katkı, aynı `Icecek` arayüzünü implemente eden ve içinde bir `Icecek` saran bir sarmalayıcıdır:

```java
Icecek icecek = new Krema(new Seker(new Sut(new Kahve())));
icecek.fiyat();   // her katman kendi katkısını ekler
```

Katmanlar istenildiği kadar ve istenilen sırayla dizilir. Kombinasyon sayısı sınırsızdır, ama yazdığın sınıf sayısı katkı sayısı kadar kalır; kombinasyon başına yeni sınıf gerekmez. `java.io` akışları (`BufferedInputStream(new FileInputStream(...))`) bu kalıbın standart kütüphanedeki canlı örneğidir.

**Proxy ve Spring.** Spring, `@Transactional`, `@Cacheable`, `@Async` gibi kesişen ilgileri **vekil nesne tabanlı AOP** ile ekler. Konteyner, hedef **bean**'in (Spring'in yönettiği nesnenin) etrafına aynı tipi taşıyan bir **vekil (proxy)** koyar. Bean'in arayüzü varsa bu vekil **JDK dynamic proxy** ile, yoksa **CGLIB** ile üretilmiş bir alt sınıftır.

Çağrı önce vekile ulaşır. Vekil işlemi (transaction) açar, gerçek metodu çağırır, sonuca göre **kalıcı yazar (commit)** veya **geri alır (rollback)**. Bu iş derleyicinin bytecode'a kod gömmesiyle olmaz; o yöntem AspectJ'nin derleme zamanı **dokuma (weaving)** yaklaşımına özgüdür. JVM sihri ya da JDBC sürücüsünün marifeti de değildir.

### Mülakatta ne sorulur?

- **Decorator ile Proxy farkı.** Yapıları neredeyse aynıdır. İkisi de aynı arayüzü korur ve nesneyi sarar; ikisi de çalışma zamanında kurulur; ikisi de arayüz ya da alt sınıf üzerinden yazılabilir. Fark **niyettedir**. Decorator nesneye **yeni davranış ekler** ve katmanları istemci bilerek seçip dizer. Proxy ise aynı davranışın **erişimini yönetir**; istemci genellikle araya bir vekil girdiğinden habersizdir. Arayüzü değiştiren tek kalıp **Adapter**'dır.
- **Kendi kendini çağırma tuzağı (self-invocation).** Bu, nesnenin kendi metodunu kendi içinden çağırmasıdır. Vekil yalnız **kendi üzerinden geçen** çağrıları yakalar. Aynı sınıf içinden `this.kaydet()` demek vekili **atlar** ve `@Transactional` hiç devreye girmez. Üç çözüm var: metodu ayrı bir bean'e taşımak, sınıfın kendi vekilini enjekte edip çağrıyı onun üzerinden yapmak ya da `TransactionTemplate` ile işlem sınırını elle çizmek.
- **"Hepsi aynı kod değil mi?"** Evet, iskeletleri benzer. Ama kalıbın kimliğini **niyeti** belirler. Adapter *uyumsuzluğu giderir*, Decorator *davranış ekler*, Proxy *erişimi kontrol eder*, Facade *basitleştirir*, Composite *tekil ile grubu eşitler*.

### Tek cümlede

Yapısal kalıplar, nesneyi aynı arayüzü koruyan bir sarmalayıcının içine alır. Böylece ya uyumsuzluğu çevirir, ya karmaşıklığı gizler, ya erişimi denetler, ya da nesneye davranış katmanları ekler.

# dpstruct-1 | Temel
## Soru
Elinizde `EskiOdemeApi` var ama sisteminiz `OdemeGecidi` arayüzü bekliyor; `EskiOdemeApi`'yi değiştiremiyorsunuz. Hangi kalıp bu uyumsuzluğu çözer?
## A
Facade; alt sistemin karmaşıklığını tek bir basit çağrının ardına gizlemek gerekir
🤖 Facade sadeleştirir ama arayüz çevirmez; buradaki sorun tek tek çağrı çokluğu değil, imza uyumsuzluğu.
## B
Proxy; eski API'ye erişimi denetleyen aynı arayüzlü bir vekil sınıf yazılır
🤖 Proxy erişimi kontrol eder ve arayüzü aynen korur; burada arayüzün değiştirilmesi gerekiyor.
## C
Decorator; eski API sarmalanıp üzerine yeni davranış katmanları eklenir
🤖 Yeni davranış istenmiyor; mevcut davranışı beklenen arayüze uydurmak gerekiyor.
## D
Adapter; eski API'yi saran ve beklenen arayüzü implemente eden sınıf yazılır
🤖 Doğru: adaptör iki uyumsuz arayüzü çevirerek eskiyi hiç değiştirmeden entegre eder.
## Doğru
D
## Açıklama
**Adapter (Adaptör)** kalıbı tam olarak **arayüz uyumsuzluğunu** çözmek içindir: mevcut bir sınıfın (`EskiOdemeApi`) sunduğu arayüzü, istemcinin **beklediği** arayüze (`OdemeGecidi`) çevirir — tıpkı bir priz çeviricinin fişi sokete uydurması gibi.

```java
class EskiApiAdaptoru implements OdemeGecidi {
    private final EskiOdemeApi eski;
    public Sonuc ode(BigDecimal tutar) {
        return eski.makePayment(tutar.doubleValue()); // çeviri
    }
}
```

Böylece `EskiOdemeApi`'yi (belki üçüncü parti, kaynağı elinde olmayan) **hiç değiştirmeden** sistemine entegre edersin. Diğer üç şık da sarmalama yapar ama farklı niyetlerle: Facade (A) çok sınıflı bir alt sistemi tek çağrıya indirir, arayüz **çevirmez**; Proxy (B) erişimi denetler ve sarmaladığı nesnenin arayüzünü **aynen korur**; Decorator (C) yine aynı arayüzü koruyup davranış **ekler**. Adapter'ın ayırt edici imzası tektir: **girişteki arayüz ile çıkıştaki arayüz farklıdır.**

# dpstruct-2 | Temel
## Soru
Facade (Cephe) kalıbının temel amacı nedir?
## A
İki uyumsuz arayüzü birbirine çevirip eski sınıfı olduğu gibi kullanılabilir kılmak
🤖 Bu Adapter'dır; Facade arayüz çevirmez, çok parçalı bir alt sistemi sadeleştirir.
## B
Karmaşık bir alt sistemin önüne basit, tek bir giriş koymak
🤖 Doğru: Facade birçok bileşeni tek, kolay kullanılan bir giriş noktası ardında toplar.
## C
Nesneye çalışma zamanında yeni davranış katmanlarını üst üste sararak eklemek
🤖 Bu Decorator'dır; Facade davranış katmaz, var olan alt sistemi kolay kullanılır kılar.
## D
Gerçek nesneye erişimi denetlemek; yetki, tembel yükleme ve önbellek eklemek
🤖 Bu Proxy'dir; Facade erişim kontrolü değil, sadeleştirme yapar.
## Doğru
B
## Açıklama
**Facade**, çok sayıda sınıftan ve adımdan oluşan **karmaşık bir alt sistemin** önüne **basit, üst seviye tek bir arayüz** koyar. İstemci, alt sistemin iç bileşenlerini ve hangi sınıfın hangi sırada çağrılacağını bilmek zorunda kalmaz; tek bir `siparisiTamamla()` çağrısı arkada stok, ödeme ve kargo servislerini koordine eder. Amaç **bağımlılığı ve öğrenme yükünü azaltmaktır** — istemci alt sistemin ayrıntılarına kuplaj kurmaz, dolayısıyla alt sistemin içi değişince istemci kırılmaz. Diğer şıklar kardeş yapısal kalıpların doğru tanımlarıdır: Adapter (A) arayüz çevirir, Decorator (C) davranış ekler, Proxy (D) erişimi kontrol eder. Önemli bir nüans: Facade alt sistemi **gizlemez ya da yasaklamaz**; sadece kolay yolu sunar, ihtiyaç duyan ileri kullanıcı yine alt bileşenlere doğrudan erişebilir.

# dpstruct-3 | Orta
## Soru
Bir `Kahve` nesnesine çalışma zamanında, sınıf patlaması yaşamadan "süt", "şeker", "krema" gibi katkıları esnek biçimde eklemek istiyorsunuz. Hangi kalıp uygundur?
## A
Adapter; her katkı için ayrı bir arayüz çevirisi sınıfı yazılıp zincire eklenir
🤖 Katkı eklemek arayüz uyumsuzluğu değildir; çeviriyle çözülecek bir sorun yok.
## B
Proxy; her katkı, kahveye erişimi denetleyen bir vekil katmanı olarak eklenir
🤖 Amaç erişimi denetlemek değil, fiyat ve davranışa yeni katkılar eklemek.
## C
Decorator; her katkı, aynı arayüzü koruyup kahveyi saran bir sınıf olur
🤖 Doğru: dekoratörler çalışma zamanında istenildiği kadar üst üste sarılarak esnek kombinasyon üretir.
## D
Template Method; katkı adımları üst sınıfın sabit iskeletinde tanımlanıp doldurulur
🤖 Sabit bir iskelet yok; katkıların sayısı ve sırası çalışma zamanında serbestçe değişmeli.
## Doğru
C
## Açıklama
**Decorator**, bir nesneye **çalışma zamanında**, aynı arayüzü (`Icecek`) koruyarak yeni davranış/özellik **katmanlamak** için idealdir. Alternatif olan kalıtımla her kombinasyonu ayrı sınıf yapmak (`SutluSekerliKremaliKahve`) **sınıf patlamasına** yol açar: N bağımsız katkı 2^N sınıf demektir ve seçim derleme zamanında donar. Decorator'da her katkı, `Icecek` implemente eden ve içinde bir `Icecek` saran bir sarmalayıcıdır:

```java
Icecek icecek = new Krema(new Seker(new Sut(new Kahve())));
icecek.fiyat();   // her katman kendi katkısını ekler
```

Katmanlar **istenildiği kadar ve istenilen sırayla** üst üste konabilir; kombinasyon sayısı sınırsızken sınıf sayısı doğrusal kalır. Adapter (A) arayüz çevirir, Proxy (B) erişimi denetler, Template Method (D) ise sabit bir iskelette önceden belirlenmiş adımları doldurur — hiçbiri çalışma zamanında serbest kombinasyon üretmez. `java.io` akışları (`BufferedInputStream(new FileInputStream(...))`) bu kalıbın standart kütüphanedeki canlı örneğidir.

# dpstruct-4 | Orta
## Soru
Spring'de bir bean üzerindeki `@Transactional` metodu çağrıldığında transaction'ın başlaması/bitmesi hangi mekanizmayla sağlanır?
## A
Spring bean'i saran bir proxy üretir; transaction'ı proxy yönetir
🤖 Doğru: kesişen ilgi, proxy tabanlı AOP ile metot çağrısının etrafına dokunur.
## B
Java derleyicisi anotasyonu görüp transaction kodunu doğrudan bytecode'a gömer
🤖 Derleyici bunu yapmaz; derleme-zamanı bytecode dokuma AspectJ'ye özgü ayrı bir yaklaşımdır.
## C
JVM, anotasyonlu metotları çalışma zamanında otomatik olarak transaction'a alır
🤖 JVM anotasyona bakıp transaction yönetmez; bu tamamen Spring altyapısının işidir.
## D
JDBC sürücüsü anotasyonu okuyup transaction sınırını kendisi belirler
🤖 Sürücü Spring anotasyonlarını görmez; sınırı çizen, çağrıyı karşılayan proxy'dir.
## Doğru
A
## Açıklama
Spring, `@Transactional`, `@Cacheable`, `@Async` gibi **kesişen ilgileri (cross-cutting concerns)** **proxy tabanlı AOP** ile ekler. Konteyner, hedef bean'in etrafına aynı arayüzü/tipi taşıyan bir **vekil (proxy)** koyar: arayüzü olan bean için **JDK dynamic proxy**, olmayan için **CGLIB** alt sınıf proxy'si üretilir. Çağrı önce proxy'ye ulaşır; proxy metot etrafında transaction'ı **açar**, gerçek metodu çağırır, sonuca göre **commit/rollback** yapar. Bu mekanizma bir derleyici hilesi değildir (B — bytecode'a derleme zamanında kod dokumak AspectJ'nin compile-time weaving yaklaşımıdır ve ayrı bir araç zinciri ister), JVM'in anotasyonlara bakıp kendiliğinden transaction açması da söz konusu değildir (C); JDBC sürücüsü ise Spring anotasyonlarından tamamen habersizdir (D) — sürücü yalnız kendisine söylenen `commit`/`rollback` çağrılarını uygular. Yani transaction sınırını **Spring'in ürettiği vekil** çizer.

# dpstruct-5 | İleri
## Soru
Decorator ile Proxy yapı olarak neredeyse aynıdır: ikisi de aynı arayüzü koruyup nesneyi sarar. Aralarındaki temel fark nedir?
## A
Decorator sarmaladığı nesnenin arayüzünü değiştirip yerine yeni bir arayüz sunar
🤖 Arayüzü değiştiren kalıp Adapter'dır; Decorator sarmaladığı tipin arayüzünü aynen korur.
## B
Niyet farkı: Decorator davranış ekler, Proxy ise erişimi yönetir
🤖 Doğru: aynı iskelet, farklı amaç; istemci dekoratörü bilerek seçer, vekilden habersizdir.
## C
Decorator derleme zamanında bağlanır; Proxy ise yalnız çalışma zamanında oluşturulabilir
🤖 İkisi de çalışma zamanında kurulur; dekoratör zinciri de nesneler sarılırken runtime'da bağlanır.
## D
Decorator yalnız arayüzle, Proxy ise yalnız kalıtımla (alt sınıfla) uygulanabilir
🤖 İkisi de arayüzle ya da alt sınıfla yazılabilir; Spring bile hem JDK proxy hem CGLIB kullanır.
## Doğru
B
## Açıklama
Bu iki kalıbın **sınıf diyagramı** neredeyse aynıdır: her ikisi de hedefle aynı arayüzü implemente eder, hedefi bir alanda tutar ve çağrıları ona iletir. Ayıran şey **niyettir**. **Decorator**, sarmaladığı nesneye **yeni davranış/özellik ekler** (fiyata katkı, akışa tamponlama, çıktıya sıkıştırma) ve katmanları **istemci bilinçli olarak seçip dizer** — `new Krema(new Sut(new Kahve()))` yazan kişi kaç katman istediğini bilir. **Proxy** ise aynı davranışın **erişimini yönetir**: yetki kontrolü, tembel yükleme, önbellek, uzak çağrı, transaction. Vekil genellikle çerçeve tarafından üretilir ve istemci onun varlığından **habersizdir**; elindeki referansın gerçek nesne mi vekil mi olduğunu bilmez. Diğer şıklar teknik olarak yanlıştır: arayüzü değiştirmek (A) Adapter'ın işidir; ikisi de **çalışma zamanında** kurulur (C); ve her ikisi de arayüz üzerinden ya da alt sınıf türeterek yazılabilir (D) — Spring'in aynı işi hem JDK dynamic proxy hem CGLIB ile yapabilmesi bunun kanıtıdır. Pratik ayrım sorusu: "sarmalayıcı yeni bir yetenek mi katıyor, yoksa aynı yeteneğe geçiş mi denetliyor?"

# dpstruct-6 | İleri
## Soru
Bir geliştirici, aynı Spring bean'i içindeki bir metottan `this.kaydet()` (kaydet `@Transactional`) çağırıyor ve transaction'ın neden başlamadığına şaşırıyor. Bunun nedeni ve önerilen çözüm hangisidir?
## A
`@Transactional` yalnız `void` dönen metotlarda çalışır; metodun dönüş tipi kaldırılmalıdır
🤖 Dönüş tipiyle ilgisi yok; sorun çağrının vekili baypas etmesidir.
## B
Aynı sınıftaki iki metot aynı transaction'ı paylaşamaz; ikinci bir bağlantı açılmalıdır
🤖 Böyle bir kısıt yok; sorun bağlantı sayısı değil, çağrının proxy'den geçmemesidir.
## C
Anotasyon metotta değil sınıf düzeyinde tanımlanmadığı için etkisizdir; sınıfa taşınmalıdır
🤖 Sınıfa taşımak da kurtarmaz; iç çağrı yine vekile uğramadan hedefe gider.
## D
İç çağrı proxy'yi atlar; çağrının proxy üzerinden geçmesi sağlanmalıdır
🤖 Doğru: self-invocation vekili baypas eder; sınırı proxy'den geçen bir çağrıyla kurmak gerekir.
## Doğru
D
## Açıklama
Spring'in proxy tabanlı AOP'si yalnız **proxy üzerinden geçen** çağrıları yakalayabilir. `this.kaydet()` çağrısı **doğrudan hedef nesnenin** metodunu çalıştırır — vekili tamamen **atlar** — bu yüzden `@Transactional` (ya da `@Cacheable`, `@Async`) tavsiyesi hiç devreye girmez. Buna **self-invocation (kendi kendine çağrı) tuzağı** denir ve proxy mekanizmasının doğrudan bir sonucudur; anotasyonu sınıf düzeyine taşımak (C) da bunu değiştirmez, çünkü sorun anotasyonun yeri değil **çağrının yoludur**. Çözümler: (1) `@Transactional` metodu **başka bir bean'e** taşımak ve onu enjekte edip çağırmak — çağrı böylece o bean'in proxy'sinden geçer; (2) sınıfın **kendi proxy'sini** enjekte edip (`self` referansı veya `AopContext.currentProxy()`) onun üzerinden çağırmak; (3) programatik olarak `TransactionTemplate` kullanıp sınırı elle çizmek. Dönüş tipi (A) ya da bağlantı sayısı (B) ile ilgisi yoktur; kök neden **çağrının vekili baypas etmesidir.**
