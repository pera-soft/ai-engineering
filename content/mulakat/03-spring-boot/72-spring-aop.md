---
slug: spring-aop
baslik: "Spring AOP: Aspect/Advice/Pointcut, Proxy Mekaniği, Self-Invocation ve AspectJ Farkı"
kademe: İleri
sira: 72
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir iş merkezi düşün: her ofisin kapısına ayrı güvenlik görevlisi koymak yerine binanın girişine tek turnike koyarsın — kim girdi, kim çıktı, yetkisi var mı, hepsi tek noktada tutulur. Yazılımda da loglama, yetki kontrolü, işlem (transaction) sınırı, önbellek ve metrik gibi işler **yüzlerce metotta** tekrar eder. Bunları her metodun içine elle yazarsan iş mantığı görünmez olur ve kuralı değiştirmek için yüzlerce yeri açman gerekir. AOP bu tekrarları tek yerde toplar ve iş mantığından ayırır.

### Temel kavramlar

- **Kesişen ilgi (cross-cutting concern)**: tek bir sınıfa ait olmayan, kodun her yerinden geçen ihtiyaç — log, güvenlik, transaction, cache, metrik.
- **AOP (Aspect-Oriented Programming — en-boyuta yönelik programlama)**: bu ilgileri iş mantığından ayırıp ayrı modüllerde toplama yaklaşımı. Bir bellek optimizasyonu, bir paralellik tekniği ya da arayüzü somut sınıfa çeviren bir araç **değildir**; tamamen **ilgi ayrımı** (separation of concerns) meselesidir.
- **Join point**: advice'in bağlanabileceği program noktası. Spring AOP'de bu **bir metot çağrısıdır**.
- **Pointcut**: hangi join point'lerin eşleşeceğini seçen ifade — "**nerede**" sorusu (`execution(* tr.velzon..servis..*(..))`).
- **Advice**: eşleşen noktada çalışan kod — "**ne yapılacak**" sorusu. Türleri: `@Before` (öncesinde), `@AfterReturning` (başarılı dönüşte), `@AfterThrowing` (istisna atınca), `@Around` (çağrıyı tümüyle sarar, en güçlüsü).
- **Aspect (`@Aspect`)**: pointcut + advice'i bir arada tutan modül.
- **Vekil (proxy)**: gerçek nesnenin önüne konan, dışarıdan aynı tip görünen sarmalayıcı nesne.
- **Interceptor**: proxy içinde çağrıyı yakalayıp öncesinde/sonrasında iş yapan halka.
- **Bytecode**: derleyicinin ürettiği, JVM'in çalıştırdığı ara kod.

```svg
<svg viewBox="0 0 640 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aspect pointcut advice join point sözlüğü">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">AOP sözlüğü</text>
  <rect x="24" y="42" width="592" height="150" rx="14" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="44" y="64" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Aspect (@Aspect) — pointcut + advice'i bir arada tutan modül</text>
  <rect x="44" y="76" width="264" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="176" y="96" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Pointcut — "NEREDE"</text>
  <text x="176" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">execution(* servis..*(..))</text>
  <rect x="332" y="76" width="264" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="464" y="96" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Advice — "NE YAPILACAK"</text>
  <text x="464" y="114" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">@Before @AfterReturning @Around</text>
  <rect x="44" y="132" width="552" height="46" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="320" y="152" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Join point — advice'in bağlanabileceği nokta</text>
  <text x="320" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Spring AOP'de: bir metot çağrısı</text>
</svg>
```

### Nasıl çalışır?

Spring AOP **vekil (proxy) tabanlıdır**. Spring'in yönettiği bir nesneye (bean) advice uygulanacaksa, Spring kabın (container) içine gerçek nesne yerine bir **vekil** koyar. Çağrılar önce proxy'ye gelir, advice çalışır, sonra gerçek nesneye devredilir.

```java
@Aspect
@Component
@Order(1)                                       // aynı noktada birden çok aspect varsa sırayı belirler
class SureOlcer {
    @Around("execution(* tr.velzon..servis..*(..))")
    Object olc(ProceedingJoinPoint jp) throws Throwable {
        long t0 = System.nanoTime();
        try { return jp.proceed(); }            // gerçek metoda devret
        finally { /* geçen süreyi logla */ }
    }
}
```

Aynı join point'e birden çok advice bağlanırsa çalışma sırasını aspect üzerindeki `@Order` (veya `Ordered` arayüzü) belirler.

Proxy türü **kurala** göre seçilir, rastgele veya sınıfın büyüklüğüne göre değil:

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JDK dynamic proxy vs CGLIB proxy seçimi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Proxy türü nasıl seçilir?</text>
  <text x="320" y="46" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Bean bir arayüz uyguluyor mu?</text>
  <rect x="28" y="60" width="288" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="172" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">EVET → JDK dynamic proxy</text>
  <text x="172" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">proxy arayüzü implemente eder</text>
  <text x="172" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">arayüz tipiyle enjekte et</text>
  <text x="172" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">somut tip → ClassCastException riski</text>
  <text x="172" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">JDK reflection tabanlı</text>
  <rect x="330" y="60" width="286" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="473" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">HAYIR → CGLIB proxy</text>
  <text x="473" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bean'in ALT SINIFI üretilir</text>
  <text x="473" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sınıf/metot final OLMAMALI</text>
  <text x="473" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">final metot override edilemez</text>
  <text x="473" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Boot varsayılanı çoğunlukla CGLIB</text>
</svg>
```

Bean en az bir **arayüz** uyguluyorsa **JDK dynamic proxy** üretilir. Vekil o arayüzü implemente eder; bu yüzden bean'i **arayüz tipiyle** enjekte etmelisin. Somut tiple istersen `ClassCastException` riski doğar.

Arayüz yoksa **CGLIB** ile bean'in **alt sınıfı** üretilir. Bu yüzden sınıf ve advice alacak metotlar `final` **olmamalıdır**; `final` metot geçersiz kılınamaz (override), dolayısıyla advice de uygulanamaz. `private` metotlar da vekilden geçmediği için advice almaz. Spring Boot, tutarlılık uğruna çoğu senaryoda `proxyTargetClass = true` diyerek **CGLIB'i varsayılan** yapar.

Bu mekaniğin en can yakan sonucu **self-invocation** sınırıdır. Self-invocation, nesnenin kendi metodunu kendi içinden çağırmasıdır:

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Proxy çağrıyı sarar, self-invocation proxy'yi atlar">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çağrı nereden geçer?</text>
  <rect x="24" y="44" width="90" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="69" y="72" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Çağıran</text>
  <rect x="150" y="44" width="120" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="210" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">PROXY</text>
  <text x="210" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">advice burada</text>
  <rect x="306" y="44" width="120" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="366" y="66" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Gerçek bean</text>
  <text x="366" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">a()  b()</text>
  <text x="126" y="72" font-size="15" fill="#5D7789" font-family="system-ui">→</text>
  <text x="282" y="72" font-size="15" fill="#5D7789" font-family="system-ui">→</text>
  <text x="320" y="130" text-anchor="middle" font-size="12" font-weight="700" fill="#B3453B" font-family="system-ui">Self-invocation tuzağı</text>
  <text x="320" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">a() içinden this.b() → proxy ATLANIR</text>
  <text x="320" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">b()'nin @Transactional/@Cacheable'ı ÇALIŞMAZ</text>
</svg>
```

Bir bean **kendi** metodunu `this.b()` diye çağırırsa çağrı gerçek nesnenin içinde kalır; araya vekil girmez. Dolayısıyla `b()` üzerindeki `@Transactional`/`@Cacheable` **hiç tetiklenmez**. Sorun metodun public olması ya da anotasyonun yanlış yazılması değildir. İşlem otomatik olarak dış metoda da taşınmaz; hiç başlamaz.

Üç çözüm var: (1) metodu **başka bir bean'e** taşıyıp onu enjekte etmek — dış çağrı vekilden geçer; (2) bean'in **kendi vekilini** enjekte edip (`self`/`ObjectProvider` ya da `AopContext.currentProxy()`) çağrıyı onun üzerinden yapmak; (3) vekil yerine **AspectJ** dokumasını (weaving) kullanmak.

`@Transactional`, `@Cacheable`, `@Async` gibi anotasyonların tümü bu altyapıyla çalışır. Bir metot **vekil üzerinden** çağrıldığında `@Transactional` için `TransactionInterceptor` devreye girer. Metottan önce işlemi başlatır ya da var olana katılır. Metot **normal dönerse** değişiklikleri kalıcı yapar (commit). Kontrol edilmeyen bir istisna (runtime exception) atarsa varsayılan davranışla değişiklikleri **geri alır** (rollback). Burada derleyicinin ürettiği özel bytecode ya da ayrı bir iş parçacığı yoktur; anotasyon salt dokümantasyon işareti de değildir.

**AspectJ** ise farklı bir mekanik kullanır. Aspect kodunu hedefin **bytecode'una** derleme anında (compile-time) ya da sınıf yüklenirken (load-time) **dokur**; bu işleme dokuma (weaving) denir.

Sonucu şudur: self-invocation dahil her çağrıyı yakalayabilir. **Metot dışı** join point'leri de görür — yapıcının çalışması, alan okuma/yazma gibi. Container'ın yönetmediği, bean olmayan nesnelerde bile çalışır ve tüm advice türlerini destekler. Bedeli, derleme/yükleme aşamasında ek kurulumdur (dokuyucu — weaver — ya da JVM ajanı). Spring AOP ise yalnız **container'ın yönettiği bean'lere dıştan gelen metot çağrılarını** sarar. Buna karşılık kurulumu basittir, ek derleme adımı istemez ve vakaların çoğu için yeterlidir.

### Mülakatta ne sorulur?

- **Pointcut vs advice** → biri "nerede" (seçici ifade), diğeri "ne yapılacak" (çalışan kod); ikisini `@Aspect` birleştirir.
- **JDK proxy mi CGLIB mi?** → arayüz varsa JDK, yoksa CGLIB; **Spring Boot varsayılanı çoğunlukla CGLIB** (`proxyTargetClass=true`).
- **CGLIB kısıtları** → sınıf/metot `final` olmamalı, `private` metot advice almaz.
- **Self-invocation** → `this.b()` proxy'yi **atlar**, `@Transactional` çalışmaz; çözüm başka bean / kendi proxy'si / AspectJ.
- **`@Transactional` mekaniği** → proxy + `TransactionInterceptor`; normal dönüşte commit, runtime exception'da rollback.
- **Birden çok advice sırası** → aspect üzerindeki `@Order` / `Ordered`.
- **Spring AOP vs AspectJ** → runtime proxy vs bytecode weaving; AspectJ daha geniş join point kümesi ve bean dışı nesneler, karşılığında ek kurulum.

### Tek cümlede

Spring AOP, kesişen ilgileri aspect'lerde toplayıp bean'in önüne koyduğu bir proxy üzerinden uygular; gücü de sınırı da buradan gelir — çağrı proxy'den geçmezse advice hiç çalışmaz.

# aop-1 | Orta
## Soru
Spring AOP sözlüğünde **pointcut** ve **advice** kavramları sırasıyla neyi ifade eder?
## A
Pointcut eşleşecek join point'leri seçen ifade, advice ise çalışan koddur
🤖 Doğru: pointcut "nerede", advice "ne yapılacak" sorusunu yanıtlar.
## B
Pointcut eşleşme anında çalışan koddur; advice ise hangi metotların eşleşeceğini seçen ifadedir
🤖 Ters tanım: seçen pointcut, çalışan advice'tir.
## C
İkisi de aynı şeydir; `pointcut` XML yapılandırmadaki, `advice` anotasyondaki addır
🤖 Ayrı kavramlardır: biri seçici ifade, diğeri çalışan davranış.
## D
Pointcut advice'in uygulanacağı bean tipini, advice ise o bean'in kapsamını (scope) belirtir
🤖 İkisi de bean/scope kavramı değil; AOP'ye özgü seçici ve davranıştır.
## Doğru
A
## Açıklama
AOP'de **pointcut** "**nerede**" sorusunu yanıtlar: bir ifade (ör. `execution(* tr.velzon..servis..*(..))`) ile hangi join point'lerin (Spring AOP'de metot çağrılarının) eşleşeceğini seçer. **Advice** ise "**ne yapılacak**" sorusunu yanıtlar: eşleşen noktada çalışacak koddur ve zamanlaması türüne göre değişir — `@Before` (öncesinde), `@AfterReturning` (başarılı dönüşte), `@AfterThrowing` (istisna atınca), `@Around` (çağrıyı tümüyle sarar, en güçlüsü). Bu ikisini bir **aspect** (`@Aspect`) modülü bir araya getirir. B tanımı ters, C ikisini karıştırır, D ise bean/scope ile ilgisiz kavramlara bağlar.

# aop-2 | Orta
## Soru
AOP'nin çözmeyi amaçladığı temel problem nedir?
## A
Nesnelerin bellek kullanımını azaltmak; ortak kod tek proxy'de tutulduğu için örnek başına maliyet düşer
🤖 AOP bir bellek optimizasyonu değildir; kod organizasyonu problemini çözer.
## B
İşaretlenen metotları otomatik olarak paralel/asenkron çalıştırıp yanıt süresini kısaltmak
🤖 Paralellik AOP'nin amacı değildir; kesişen ilgileri ayırmakla ilgilidir.
## C
Kesişen ilgileri (cross-cutting concerns) iş mantığından ayırıp tek yerde toplamak
🤖 Doğru: dağınık tekrarlı ilgiler tek aspect'te toplanır.
## D
Arayüzleri çalışma zamanında somut sınıflara dönüştürüp `new` ihtiyacını ortadan kaldırmak
🤖 AOP arayüz/sınıf dönüşümü yapmaz; ilgi ayrımı sağlar.
## Doğru
C
## Açıklama
Loglama, yetkilendirme, işlem sınırı, önbellek, metrik gibi ilgiler **birçok** metotta tekrar eder ve iş mantığının içine serpiştirilirse hem kodu kirletir hem de tek noktadan değiştirilemez hale gelir. Bunlara **kesişen ilgiler** (cross-cutting concerns) denir. AOP bu ilgileri ayrı **aspect**'lerde toplayıp, pointcut'larla hangi metotlara uygulanacağını bildirerek iş mantığından **ayırır** — kod tekrarı biter, ilgi tek yerden yönetilir. Bu bir bellek (A) veya paralellik (B) tekniği değildir, arayüz dönüşümüyle (D) de ilgisizdir; tamamen **ilgi ayrımı** (separation of concerns) meselesidir.

# aop-3 | İleri
## Soru
Spring AOP proxy oluştururken JDK dynamic proxy ile CGLIB arasında nasıl seçim yapar?
## A
Her zaman JDK dynamic proxy kullanır; CGLIB yalnız Spring 4 öncesinde kullanılırdı
🤖 Arayüzsüz bean'lerde CGLIB kullanılır; "her zaman JDK" yanlış.
## B
Bean'in metot sayısına göre seçer; küçük sınıflarda JDK, büyük sınıflarda CGLIB kullanır
🤖 Seçim sınıfın büyüklüğüne değil, bean'in arayüz uygulayıp uygulamamasına dayanır.
## C
Rastgele birini seçer; iki proxy türünün davranışı birebir aynı olduğu için seçim önemsizdir
🤖 Seçim rastgele değildir; kurala bağlıdır ve `final` kısıtı gibi farklar önemlidir.
## D
Arayüz uygulayan bean'de JDK proxy, uygulamayanda CGLIB proxy'si üretir
🤖 Doğru: arayüz varsa JDK, yoksa CGLIB; Boot varsayılanı çoğunlukla CGLIB.
## Doğru
D
## Açıklama
Klasik kural: bean en az bir **arayüz** uyguluyorsa Spring **JDK dynamic proxy** üretir (proxy o arayüzü implemente eder, bu yüzden bean'i **arayüz tipiyle** enjekte etmelisin — somut tiple istersen `ClassCastException` riski doğar). Arayüz yoksa **CGLIB** ile bean'in **alt sınıfı** üretilir; bu yüzden CGLIB'de sınıf ve metotlar `final` **olmamalıdır** (final metot override edilemez, advice uygulanamaz). Spring Boot, tutarlılık için birçok senaryoda **CGLIB'i varsayılan** yapar (`proxyTargetClass=true`). Seçim rastgele (C) veya sınıfın büyüklüğüne bağlı (B) değildir; "her zaman JDK" (A) da yanlıştır.

# aop-4 | İleri
## Soru
`@Transactional`'ın çalışma mekanizması için doğru ifade hangisidir?
## A
Java derleyicisi `@Transactional` metodu için özel bytecode üretir, çalışma zamanında proxy gerekmez
🤖 Derleyici böyle bir kod üretmez; işlem yönetimi runtime proxy/interceptor ile olur.
## B
Proxy'deki bir interceptor işlemi başlatır; sonunda commit ya da rollback yapar
🤖 Doğru: proxy + interceptor; commit/rollback bu sarmalayıcıda olur.
## C
Her `@Transactional` metot, işlemi izole etmek için ayrı bir thread'de çalıştırılır
🤖 İşlem ayrı thread gerektirmez; aynı thread'de proxy interceptor yönetir.
## D
`@Transactional` yalnız bir dokümantasyon işaretidir; işlem sınırını JPA sağlayıcısı kendi başına yönetir
🤖 Davranışı gerçekten değiştirir: işlem sınırını proxy üzerinden yönetir.
## Doğru
B
## Açıklama
`@Transactional` de tıpkı diğer Spring anotasyonları gibi **proxy tabanlı** çalışır. Spring, ilgili bean'in yerine bir proxy koyar; `@Transactional` bir metot **proxy üzerinden** çağrıldığında bir işlem interceptor'ı (`TransactionInterceptor`) devreye girer: metottan önce bir işlem (transaction) başlatır/katılır, metot **normal dönerse commit**, bir **runtime exception** atarsa (varsayılan) **rollback** yapar. Bu yüzden davranışı gerçektir (D yanlış), derleyici sihri (A) veya ayrı thread (C) söz konusu değildir. Ve tam da proxy'ye dayandığı için **self-invocation** ile atlanır — bir sonraki sorunun konusu.

# aop-5 | İleri
## Soru
Aşağıdaki bean'de `disKayit()` çağrıldığında `icKayit()` üzerindeki `@Transactional` neden **devreye girmez**?

```java
@Service
class Kayit {
    public void disKayit() {
        this.icKayit();          // aynı bean'in metodu
    }
    @Transactional
    public void icKayit() { /* db yaz */ }
}
```
## A
`@Transactional` yalnız `public` metotta çalışır, `icKayit()` public olsa da iç metot sayılır
🤖 Sorun public/iç ayrımı değil; çağrının proxy'yi atlamasıdır.
## B
Anotasyon yanlış yazılmış; doğrusu `@Transaction` olmalıydı, `@Transactional` yalnız sınıf düzeyinde geçerlidir
🤖 Anotasyon adı doğru ve metot düzeyinde geçerli; sorun yazımda değil, çağrı yolunda.
## C
Aynı bean içinden çağrıldığı için işlem otomatik olarak dış metot `disKayit()`'in sınırına taşınır
🤖 Böyle bir otomatik taşıma yok; iç çağrı proxy'siz olduğu için hiç işlem başlamaz.
## D
`this.icKayit()` çağrısı proxy'yi atlayıp doğrudan gerçek nesneye gider
🤖 Doğru: iç çağrı proxy'den geçmez, transaction interceptor çalışmaz.
## Doğru
D
## Açıklama
Advice yalnız çağrı **proxy'den geçtiğinde** çalışır. `disKayit()` içindeki `this.icKayit()` çağrısı **gerçek nesnenin** kendi metoduna doğrudan gider — araya proxy girmez, dolayısıyla `@Transactional` interceptor'ı **hiç tetiklenmez** ve `icKayit()` işlemsiz çalışır. Buna **self-invocation** denir ve proxy tabanlı AOP'nin en sinsi sınırıdır. Çözümler: (1) metodu **başka bir bean'e** taşıyıp onu enjekte etmek (dış çağrı proxy'den geçer); (2) bean'in **kendi proxy'sini** enjekte edip (`self`/`ObjectProvider` veya `AopContext.currentProxy()`) onun üzerinden çağırmak; (3) proxy yerine **AspectJ** weaving kullanmak. Sorun public/iç metot (A), yazım (B) veya otomatik taşıma (C) değildir.

# aop-6 | Uzman
## Soru
Spring AOP (proxy tabanlı) ile AspectJ (weaving) arasındaki temel fark ve AspectJ'nin sağladığı ek yetenek nedir?
## A
Spring AOP runtime proxy kullanır; AspectJ bytecode weaving yapar, daha çok join point yakalar
🤖 Doğru: proxy vs weaving; AspectJ daha geniş join point kümesi + self-invocation'ı çözer.
## B
AspectJ yalnız Spring bean'lerinde çalışır; Spring AOP ise container dışındaki her nesneyi de sarabilir
🤖 Tersine: Spring AOP yalnız container bean'lerinde çalışır; AspectJ bean dışı nesneleri de dokuyabilir.
## C
İkisi de aynı teknolojidir; AspectJ, Spring AOP'nin yeni sürümdeki adıdır ve mekanikleri birebir aynıdır
🤖 Ayrı teknolojilerdir: biri runtime proxy, diğeri bytecode weaving.
## D
AspectJ daha zayıftır; yalnız `@Before` advice'ini destekler, `@Around` gibi sarmalayıcı advice'leri desteklemez
🤖 AspectJ daha güçlüdür ve tüm advice türlerini destekler; kısıt yanlış.
## Doğru
A
## Açıklama
İki AOP yaklaşımı temelden farklı mekanikler kullanır. **Spring AOP** *runtime proxy* tabanlıdır: yalnız **container'ın yönettiği bean'lerin** dıştan gelen **metot** çağrılarını sarar; bu yüzden self-invocation'da atlanır ve constructor/alan erişimi gibi noktaları göremez — ama kurulumu basittir, ek derleme adımı istemez ve vakaların çoğu için yeterlidir. **AspectJ** ise *weaving* yapar: aspect kodunu hedefin **bytecode'una** derleme anında (compile-time) veya sınıf yüklenirken (load-time) **dokur**; sonuç olarak self-invocation dahil her çağrıyı, ayrıca **metot dışı** join point'leri (constructor çalışması, alan okuma/yazma) de yakalayabilir ve bean olmayan nesnelerde bile çalışır. Bedeli: derleme/yükleme aşamasında ek kurulum (weaver/agent). B ilişkiyi ters kurar, C ikisini eşitler, D AspectJ'yi haksızca kısıtlar.
