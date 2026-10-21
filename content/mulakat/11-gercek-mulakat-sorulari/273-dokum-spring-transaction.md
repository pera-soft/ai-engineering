---
slug: dokum-spring-transaction
baslik: "Mülakat Dökümü · Spring Çekirdeği ve Transaction: Vekil Nesne, Propagation, Isolation ve Self-Invocation"
kademe: İleri
sira: 273
etiketler: 11
---

## Konu anlatımı

Bu dosya bir tahmin listesi değil. Elimizde gerçek bir mülakatın dökümü var: mülakatçının sorduğu sorular ve senin o an verdiğin cevaplar. Spring ve transaction bölümü dökümün en uzun kısmıdır. Uzun olmasının sebebi de dökümde açıkça görünüyor.

Mülakatçının yöntemi şuydu: `@Transactional` sorusuna doyurucu cevap verdin, hemen propagation geldi. Propagation'ı doğru anlattın, isolation geldi. Isolation'ı sayınca veritabanı anomalileri, oradan commit zamanlaması, oradan self-invocation geldi. Yani **her doğru cevap seni bir üst basamağa çıkardı**. Cevap zayıf gelseydi mülakatçı konuyu bırakır, başka yere geçerdi. Derinleşme bir cezalandırma değil, bir onaydır.

Bu gözlem hazırlığın tamamını değiştirir. Tanımı bilmek soruyu kapatmaz, yalnız bir sonrakini açar. Bu yüzden aşağıdaki her başlıkta **takip sorusu riski** bölümü var; dosyanın asıl değeri orasıdır. Bir konuda zincirin üç halkasını hazırlamak, on konuda tanım ezberlemekten güçlüdür.

İkinci gözlem daha keskin. Dökümde "teorik olarak biliyorum" diye açtığın tek Spring sorusu vekil nesne (proxy bean) sorusuydu. Mülakat tam oradan devam etti ve zincirin sonunda self-invocation sorusu vardı. İkisi aynı mekanizmadır. Bu dosyanın en çok emek isteyen bölümü o yüzden üçüncü başlıktır.

```svg
<svg viewBox="0 0 660 366" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mulakat merdiveni: her dogru cevap bir ust basamagi acar">
  <defs>
    <marker id="dkmspr-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dökümdeki sıra: her doyurucu cevap bir üst basamağı açtı</text>
  <rect x="270" y="48" width="340" height="32" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="282" y="69" font-size="10.5" fill="#1C3247" font-family="system-ui">6 · Self-invocation nedir, neden davranış kaybolur?</text>
  <rect x="220" y="92" width="340" height="32" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="232" y="113" font-size="10.5" fill="#1C3247" font-family="system-ui">5 · Metot bitmeden commit olur mu?</text>
  <line x1="280" y1="92" x2="280" y2="82" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-ok)"/>
  <rect x="170" y="136" width="340" height="32" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="182" y="157" font-size="10.5" fill="#1C3247" font-family="system-ui">4 · Hangi veritabanı anomalilerini biliyorsun?</text>
  <line x1="230" y1="136" x2="230" y2="126" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-ok)"/>
  <rect x="120" y="180" width="340" height="32" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="132" y="201" font-size="10.5" fill="#1C3247" font-family="system-ui">3 · Isolation nedir, seviyeleri neler?</text>
  <line x1="180" y1="180" x2="180" y2="170" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-ok)"/>
  <rect x="70" y="224" width="340" height="32" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="82" y="245" font-size="10.5" fill="#1C3247" font-family="system-ui">2 · Propagation neden var, hangisini kullanırsın?</text>
  <line x1="130" y1="224" x2="130" y2="214" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-ok)"/>
  <rect x="20" y="268" width="340" height="32" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="32" y="289" font-size="10.5" fill="#1C3247" font-family="system-ui">1 · @Transactional neden kullanılır?</text>
  <line x1="80" y1="268" x2="80" y2="258" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-ok)"/>
  <rect x="14" y="310" width="632" height="46" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="330" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Tanım bilmek soruyu kapatmaz, yalnız bir sonrakini açar. Derinleşme bir onaydır, ceza değil.</text>
  <text x="330" y="347" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">Hazırlık ölçütü: bir konuda zincirin üç halkası, on konuda tanım ezberinden güçlüdür.</text>
</svg>
```

### 1 · IoC'yi bilmeyen birine nasıl anlatırsın?

**Soruldu / Verdiğin cevap**

Mülakatçı kontrolün tersine çevrilmesini (IoC) bilmeyen birine anlatmanı istedi.

> "Nesne yaratma ve yönetme sorumluluğu Spring Container'a geçer. Biz bağımlılığı tanımlarız, Spring bağımlılık enjeksiyonu (DI) ile onu verir."

**Değerlendirme**

**✅ Yeterli.** Sorunun çekirdeği sorumluluğun yer değiştirmesidir ve sen bunu ilk cümlede söylemişsin. Eksik bir bilgi yok. Tek zayıflık şu: soru "bilmeyen birine" diyerek bir benzetme davet ediyordu, sen teknik dille cevapladın.

**Takip sorusu riski**

- **IoC ile DI aynı şey mi?** IoC bir ilkedir, DI onun en yaygın uygulamasıdır. Servis arayıcı (service locator) da bir IoC uygulamasıdır ama DI değildir.
- **Tam olarak neyin kontrolü tersine dönüyor?** Nesnenin yaşam döngüsünün ve bağımlılık çözümünün kontrolü. Kodun akış kontrolü sende kalır.
- **Container ne zaman devreye girer?** Uygulama açılışında. Bileşen taraması ve yapılandırma sınıfları okunur, bean tanımları çıkarılır, sonra nesneler kurulur.
- **Bu ne kazandırır, bedeli nedir?** Kazanç gevşek bağlılık ve test edilebilirliktir. Bedeli açılış maliyeti ve hata ayıklamada araya giren çerçeve katmanıdır.

**Güçlendirilmiş cevap**

Cevabına bir benzetme eklersen ayrışırsın: `new` yazmak malzemeyi kendin almaktır, DI ise ihtiyacını listeleyip mutfağa vermektir. Sonra ilke ile uygulamayı ayır — "IoC ilkedir, DI onun uygulamasıdır" cümlesi bir sonraki soruyu baştan kapatır. Ardından somutlaştır: bağımlılık yapıcıda ilan edilir, container onu açılışta çözer, sen `new` yazmazsın. Kapanışta bedeli söyle; bağımlılık grafiği artık kodda değil yapılandırmadadır, bu yüzden açılış hataları çalışma anına kayabilir. Bu takas cümlesi cevabı reklam olmaktan çıkarır (`68`).

### 2 · Constructor injection neden önerilir?

**Soruldu / Verdiğin cevap**

Mülakatçı yapıcı enjeksiyonunun (constructor injection) neden önerildiğini sordu.

> "Bağımlılıklar açık olur, nesne doğru kurulmuş hâlde doğar ve testte sahte nesne (mock) vermek kolaylaşır."

**Değerlendirme**

**✅ Yeterli.** Üç gerekçenin üçü de doğru ve birbirini tamamlıyor. Ama bu cevap "alan enjeksiyonu neden kötü" sorusuna zemin hazırlıyor ve sen o karşılaştırmayı kurmamışsın. Mülakatçı çoğu zaman tam oradan devam eder.

**Takip sorusu riski**

- **`final` alan neden önemli?** Yapıcı enjeksiyonunda bağımlılık `final` olabilir. Kurulduktan sonra değişmez; bu hem güvenli yayımlama hem de okunabilirlik kazandırır.
- **Alan enjeksiyonu (field injection) tam olarak neyi bozar?** Bağımlılık gizlenir, sınıf çerçeve olmadan kurulamaz, `final` yapılamaz ve test için yansıma (reflection) gerekir.
- **Döngüsel bağımlılık ne olur?** Yapıcı enjeksiyonunda döngü açılışta hata verir. Alan enjeksiyonunda sessizce çalışır ve tasarım hatası gizlenir. Yaygın uygulamada artık döngüye varsayılan olarak izin verilmez.
- **Yapıcıda yedi parametre birikirse?** Bu bir koku sayılır. Sınıf çok iş yapıyordur; bağımlılık listesi tasarımın ölçüsüdür.
- **Setter enjeksiyonu hiç kullanılmaz mı?** İsteğe bağlı bağımlılıkta kullanılabilir. Zorunlu bağımlılık yapıcıdan verilir.

**Güçlendirilmiş cevap**

Şunu eklersen fark yaratırsın: yapıcı enjeksiyonu bir tasarım geri bildirimidir. Sınıfın bağımlılık sayısı yapıcıda görünür hâle gelir ve şişen sınıf kendini ele verir. İkinci ekleme `final` alan olsun; nesne kurulduktan sonra bağımlılığı değişmez, bu da eşzamanlı erişimde güven verir. Üçüncüsü döngüsel bağımlılık: yapıcı enjeksiyonu döngüyü açılışta yüzüne çarpar, alan enjeksiyonu ise gizler. Kapanışta test tarafını somutla — çerçeveyi hiç kurmadan `new Servis(sahteDepo)` yazabilmek en güçlü kanıttır (`68`, `71`).

### 3 · Vekil nesne (proxy bean) duydun mu?

**Soruldu / Verdiğin cevap**

Mülakatçı vekil nesneyi (proxy bean) duyup duymadığını sordu.

> "Teorik olarak biliyorum. `@Transactional`, `@Cacheable` ve AOP çağrıları vekil üzerinden geçer."

**Değerlendirme**

**🟡 Doğru ama eksik.** İki ayrı sorun var. Birincisi içerik: söylediğin doğru fakat tek cümle, mekanizmanın nasıl kurulduğunu anlatmıyor. İkincisi sunum: "teorik olarak biliyorum" ifadesi doğru bilgiyi de zayıf gösterir. Bildiğin kısmı net anlat, sınırı en sonda söyle.

Bu başlık dosyanın en kritik yeridir. Dökümde mülakat bu sorudan sonra derinleşti ve zincirin sonunda self-invocation vardı. İkisi aynı mekanizmanın iki yüzüdür.

**Takip sorusu riski**

- **Vekili kim ve ne zaman üretir?** Container açılışta bean'i sarar. Sen `SiparisServisi` enjekte ettiğini sanırsın, elindeki nesne aslında onun vekilidir.
- **JDK dinamik vekil ile CGLIB farkı nedir?** JDK vekili arayüz (interface) gerektirir ve aynı arayüzü uygulayan yeni bir nesne üretir. CGLIB arayüz istemez, çalışma anında hedef sınıfın alt sınıfını üretir. Spring Boot'ta yaygın varsayılan sınıf tabanlı vekildir.
- **`final` sınıf ya da `final` metot ne olur?** CGLIB alt sınıf üretemez, `final` metodu da geçersiz kılamaz. `final` metottaki `@Transactional` sessizce çalışmaz. `private` metot da aynı sebeple sarılmaz.
- **Peki self-invocation neden çalışmaz?** Aynı kök: `this` vekil değil, hedef nesnedir. İç çağrı sarmalayıcıya hiç uğramaz.
- **Bu sessizlik neden tehlikeli?** Hata fırlamaz, günlükte iz kalmaz. Sorun aylar sonra tutarsız veri olarak görünür.

```svg
<svg viewBox="0 0 660 372" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Vekil nesne mekanizmasi: JDK dinamik vekil, CGLIB alt sinifi ve self-invocation bagi">
  <defs>
    <marker id="dkmspr-cagri" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Davranış vekilde yaşar; vekile uğramayan çağrıda davranış yoktur</text>
  <rect x="14" y="42" width="150" height="46" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="89" y="70" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Çağıran (başka bean)</text>
  <line x1="166" y1="65" x2="184" y2="65" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-cagri)"/>
  <rect x="186" y="42" width="176" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="274" y="63" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Vekil nesne (proxy)</text>
  <text x="274" y="79" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">işlem burada açılır ve kapanır</text>
  <line x1="364" y1="65" x2="382" y2="65" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmspr-cagri)"/>
  <rect x="384" y="42" width="150" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="459" y="63" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Hedef bean</text>
  <text x="459" y="79" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">senin yazdığın sınıf</text>
  <text x="596" y="60" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">commit ve geri alma</text>
  <text x="596" y="76" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kararı vekilde verilir</text>
  <rect x="14" y="104" width="316" height="130" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="126" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">JDK dinamik vekil</text>
  <text x="172" y="148" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arayüz (interface) gerektirir</text>
  <text x="172" y="168" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı arayüzü uygulayan yeni nesne üretir</text>
  <text x="172" y="188" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız arayüzdeki metotlar sarılır</text>
  <text x="172" y="208" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">arayüzde olmayan metot sarılmaz</text>
  <text x="172" y="226" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">hedef nesne vekilin içinde tutulur</text>
  <rect x="344" y="104" width="302" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="126" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">CGLIB alt sınıfı</text>
  <text x="495" y="148" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arayüz gerekmez, alt sınıf üretilir</text>
  <text x="495" y="168" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Spring Boot'ta yaygın varsayılan</text>
  <text x="495" y="188" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">final sınıf alt sınıfa açılamaz</text>
  <text x="495" y="208" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">final ve private metot geçersiz kılınamaz</text>
  <text x="495" y="226" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">metotlar geçersiz kılınarak sarılır</text>
  <rect x="14" y="246" width="632" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="268" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Aynı kök, iki ayrı tuzak — ikisi de sessizdir</text>
  <text x="330" y="291" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1 · final ya da private metotta @Transactional hiç devreye girmez; sarmalanacak metot yoktur.</text>
  <text x="330" y="311" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">2 · this.metot() çağrısı hedef nesnede kalır; vekile uğramaz, işlem açılmaz (self-invocation).</text>
  <text x="330" y="331" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Hata fırlamaz, günlüğe satır düşmez; sonuç aylar sonra tutarsız veri olarak görünür.</text>
  <text x="330" y="350" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Aynı kural @Cacheable, @Async, @Retryable ve @PreAuthorize için de geçerlidir.</text>
</svg>
```

**Güçlendirilmiş cevap**

Bu soruda önce açılışı düzelt: "teorik olarak biliyorum" deme, doğrudan mekanizmayı anlat. Şöyle kur: Spring bean'i sarmalar, sen enjekte ettiğinde elindeki nesne hedef sınıf değil onun vekilidir. Sonra ikiye ayır — arayüz varsa JDK dinamik vekili aynı arayüzü uygular, yoksa CGLIB çalışma anında alt sınıf üretir; Spring Boot'ta yaygın varsayılan sınıf tabanlı olandır. Ardından sınırı söyle: alt sınıf üretmek `final` sınıfta ve `final` metotta mümkün değildir, bu yüzden oradaki `@Transactional` sessizce çalışmaz. Kapanışta zinciri kendin bağla — "aynı sebeple `this.metot()` çağrısı da vekile uğramaz" cümlesi, mülakatçının bir sonraki sorusunu senin söylemen demektir. Deneyim sınırın varsa en sonda tek cümleyle söyle: "üretimde bu tuzağa denk gelmedim ama mekanizmayı böyle okuyorum" (`72`, `83`).

### 4 · Singleton varken neden prototype gerekir?

**Soruldu / Verdiğin cevap**

Mülakatçı varsayılan singleton kapsamı varken prototype kapsamının neden gerektiğini sordu.

> "Her kullanımda bağımsız durum (state) gerekiyorsa prototype kullanılır."

**Değerlendirme**

**✅ Yeterli.** Ölçüt doğru: paylaşılan durum varsa singleton uygun değildir. Cevap kısa ama soru da kısaydı. Buradaki risk cevabın yanlışlığı değil, hemen ardından gelen klasik tuzak sorusudur.

**Takip sorusu riski**

- **Singleton bean'e prototype bean enjekte edersen ne olur?** Klasik tuzak budur. Enjeksiyon bir kez, açılışta yapılır. Singleton hep aynı örneği kullanır ve prototype davranışı kaybolur.
- **Çözüm nedir?** Her çağrıda taze örnek almak için `ObjectProvider` ya da `Provider` kullanılır. Arama metodu (`@Lookup`) ve kapsam vekili de aynı işi görür.
- **Prototype'ın yıkım geri çağırımı çalışır mı?** Hayır. Container prototype'ı üretir ve bırakır; kapatma sorumluluğu çağırana kalır. Bu yüzden kaynak tutan bean prototype yapılmaz.
- **Başka kapsamlar var mı?** Web ortamında request, session ve application kapsamları vardır.
- **Singleton bean'de alan tutmak neden riskli?** Tek örneği çok iş parçacığı (thread) kullanır. Paylaşılan değiştirilebilir alan yarış durumu üretir.

**Güçlendirilmiş cevap**

Cevabına önce net bir kural ekle: bean durumsuz olmalıdır, durum gerekiyorsa ya metot yerelinde tutulur ya da kapsam değişir. Sonra tuzağı kendin söyle — singleton'a prototype enjekte etmek prototype'ı etkisiz kılar, çünkü bağımlılık açılışta bir kez çözülür. Çözümü de ver: `ObjectProvider` üzerinden her ihtiyaçta yeni örnek istenir. Kapanışta prototype'ın yıkım geri çağırımının çalışmadığını ekle; bu ayrıntıyı söyleyen aday sayısı azdır. Son olarak pratik tercihi belirt: çoğu durumda doğru cevap prototype değil, durumu bean'den çıkarmaktır (`69`).

### 5 · AOP, interceptor ve kesişen ilgiler

**Soruldu / Verdiğin cevap**

Mülakatçı AOP ve interceptor deneyimini, kesişen ilgilerin (cross-cutting concerns) nasıl ele alındığını sordu.

> ".NET tarafında kullandım. Loglama, güvenlik, performans ölçümü ve transaction gibi ortak işleri iş kodundan ayırmak için. Interceptor çağrının öncesine ve sonrasına girer."

**Değerlendirme**

**✅ Yeterli.** Bu iyi bir dürüst köprü örneği. Deneyimin başka platformda, sen bunu saklamadan söylemişsin ve kavramı doğru adlandırmışsın. Tanım da yerinde: ortak ilgi iş kodundan ayrılır. Eksik olan tek şey Spring'in kendi sözcükleridir.

**Takip sorusu riski**

- **Spring AOP terimleri neler?** Kesit (aspect) davranışı taşır, öğüt (advice) ne zaman çalışacağını söyler, kesim noktası (pointcut) nereye uygulanacağını seçer, birleşim noktası (join point) somut çağrıdır.
- **Filter, Interceptor ve AOP sırası nedir?** Filter en dışta, servlet seviyesindedir. HandlerInterceptor MVC seviyesinde, denetleyiciden hemen önce girer. AOP en içte, bean metodu seviyesindedir.
- **Spring AOP ile AspectJ farkı?** Spring AOP vekil tabanlıdır; yalnız bean metotlarını ve yalnız dışarıdan gelen çağrıları yakalar. AspectJ bayt kodu dokur; `private` metodu ve iç çağrıyı da yakalar.
- **`@Around` ne kazandırır?** Çağrıyı sarar, `proceed()` ile devam ettirir. Süre ölçme, yeniden deneme (retry) ve önbellek mantığı burada kurulur.
- **AOP'un bedeli nedir?** Akış kodda görünmez. Aşırı kullanılırsa hata ayıklamak zorlaşır ve davranış "sihir" gibi durur.

**Güçlendirilmiş cevap**

Köprüyü aynen koru, üstüne Spring sözlüğünü ekle: kesit, öğüt, kesim noktası, birleşim noktası. Sonra sırayı söyle — Filter dışta, Interceptor ortada, AOP içte; bu üçlü sıralamayı söyleyen aday konuyu haritalamış sayılır. Ardından Spring AOP'un sınırını belirt: vekil tabanlı olduğu için yalnız dışarıdan gelen bean çağrılarını yakalar. Bu cümle üçüncü ve on birinci başlıkla aynı köke bağlanır ve mülakatçıya tutarlı bir zihin haritası gösterir. Kapanışta bedeli ver: kesişen ilgi çoğaldıkça akış koddan okunamaz hâle gelir, bu yüzden AOP az ve seçici kullanılır (`72`, `76`).

### 6 · `@Transactional` neden kullanılır?

**Soruldu / Verdiğin cevap**

Mülakatçı `@Transactional` işaretini neden kullandığını sordu.

> "Birden çok veritabanı işlemini tek bir transaction içinde yönetmek için. Hepsi başarılıysa commit edilir, hata varsa geri alınır."

**Değerlendirme**

**✅ Yeterli.** Tanım doğru ve tam. Bu cevabın önemi dökümde ayrıca görünüyor: mülakatçı burada durmadı, doğrudan propagation'a geçti. Yani bu cevap "geçti" ve merdiven başladı.

**Takip sorusu riski**

- **İşlem sınırı nerede olmalı?** Servis katmanında, tek bir iş kuralına karşılık gelen metotta. Depo (repository) seviyesinde sınır çizmek işi parçalar.
- **Bildirimsel ile programatik fark nedir?** İşaretle yönetim bildirimseldir. `TransactionTemplate` ile programatik yönetim sınırı elle çizer ve dar sınır gerektiğinde işe yarar.
- **`readOnly = true` ne kazandırır?** Kirli kontrolü (dirty checking) atlanabilir ve bazı kurulumlarda okuma kopyasına yönlendirme yapılır. Yazma yapmadığın metotta bu işaret ucuz bir kazançtır.
- **Metot içinde istisnayı yakalayıp yutarsan?** Geri alma tetiklenmez. Yarım kalmış iş sessizce kesinleşir; bu en tehlikeli hâldir.
- **Sonra ne gelir?** Dökümde propagation geldi. Bu soruyu bitirirken propagation'a köprü kurmaya hazır ol.

**Güçlendirilmiş cevap**

Tanımdan sonra sınırı konuşursan ayrışırsın: işlem sınırı servis metodudur ve bu metot tek bir iş kuralına karşılık gelmelidir. İkinci ekleme dörtlü güvence olsun — atomiklik, tutarlılık, izolasyon ve kalıcılık; bunların hepsini veritabanı sağlar, çerçeve yalnız sınırı çizer. Üçüncüsü pratik bir ayrıntı: okuma yapan metotlarda `readOnly` işaretini kullanmak hem niyeti belgeler hem ufak kazanç sağlar. Kapanışta bir sonraki basamağı kendin aç: "bu metot başka bir transactional metodu çağırırsa ne olacağı propagation ile belirlenir" cümlesi zinciri senin yönettiğini gösterir.

### 7 · Propagation neden var?

**Soruldu / Verdiğin cevap**

Mülakatçı propagation'ın ne işe yaradığını ve hangisini kullandığını sordu.

> "Çoğunlukla REQUIRED kullanıyoruz. Var olan bir transaction varsa ona katılır, yoksa yenisini açar."

**Değerlendirme**

**✅ Yeterli.** Varsayılanı doğru bilmişsin ve davranışı iki cümleyle tam anlatmışsın. Bu soruda çoğu aday burada takılır. Ama cevap tek seçenekle sınırlı kaldı; mülakatçı doğal olarak "peki başka ne var" diye sorar.

**Takip sorusu riski**

- **`REQUIRES_NEW` ne zaman gerekir?** Ana iş geri alınsa bile kalması gereken kayıtta. Denetim (audit) kaydı klasik örnektir. Dış işlem askıya alınır, iç işlem ayrı bağlantıda kesinleşir.
- **`REQUIRES_NEW` bedeli nedir?** Aynı anda iki bağlantı tutulur. Havuz sıkışabilir. Dış işlem bir satırı kilitlediyse iç işlem o satırı beklerken kendi kendine kilitlenme doğar.
- **`NESTED` farkı nedir?** Kayıt noktası (savepoint) kullanır ve aynı bağlantıda kalır. İç kısım tek başına geri alınabilir, ama dış işlem geri alınırsa iç kısım da gider.
- **Diğer seçenekler ne işe yarar?** `MANDATORY` çağıranın işlem açmasını şart koşar, `SUPPORTS` varsa katılır, `NOT_SUPPORTED` askıya alır, `NEVER` işlem varsa hata verir.
- **İç metotta istisna çıkıp dışarıda yutulursa?** İşlem geri alınacak diye işaretlenir. Dışarıda istisnayı yutsan bile kesinleştirme `UnexpectedRollbackException` ile başarısız olur.

**Güçlendirilmiş cevap**

Cevabına ikinci seçeneği ve gerekçesini eklersen soru kapanır: `REQUIRES_NEW`, ana iş geri alınsa bile kalması gereken kayıtlar içindir. Hemen ardından bedelini söyle — ayrı bağlantı açılır, havuz baskısı artar ve aynı satıra dokunuyorsan kilit beklemesi doğar. Üçüncü olarak `NESTED` ile farkı netleştir: `NESTED` kayıt noktasıyla aynı bağlantıda çalışır, dış işleme bağımlıdır. Kapanışta ince ayrıntıyı bırak: iç işlem geri alınacak diye işaretlendiğinde dışarıda istisnayı yutmak seni kurtarmaz. Bu son cümle propagation sorusunu tanım seviyesinden çıkarır (`84`).

### 8 · Isolation ve seviyeleri

**Soruldu / Verdiğin cevap**

Mülakatçı izolasyon seviyelerini sordu.

> "READ UNCOMMITTED, READ COMMITTED, REPEATABLE READ ve SERIALIZABLE. Seviye yükseldikçe tutarlılık artar, performans düşer."

**Değerlendirme**

**✅ Yeterli.** Dördünü de doğru saymışsın ve asıl önemlisi takası söylemişsin. Takas cümlesi bu soruda ayrıştırıcıdır; listeyi ezberleyip bedelini söylemeyen aday çok olur. Eksik olan tek şey seviyelerin hangi anomaliyi kapattığıdır.

**Takip sorusu riski**

- **Hangi seviye hangi anomaliyi engeller?** Aşağıdaki tabloyu ezberle; mülakatçının en sık istediği şey budur.
- **Varsayılan seviye nedir?** Veritabanına göre değişir. Yaygın kurulumlarda READ COMMITTED varsayılandır, bazı motorlarda REPEATABLE READ kullanılır.
- **MVCC nedir?** Çok sürümlü eşzamanlılık denetimi. Okuyucu yazarı, yazar okuyucuyu bloklamaz; her işlem verinin bir anlık görüntüsünü görür.
- **SERIALIZABLE'ın bedeli nedir?** Ya kilit ya da serileştirme hatası. İkinci durumda uygulama işlemi yeniden denemek zorundadır.
- **Seviye yükseltmek yerine ne yapılır?** Çoğu vakada uygulama tarafı çözüm daha ucuzdur: sürüm alanıyla iyimser kilit ya da hedefli satır kilidi.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Izolasyon seviyeleri ile veritabani anomalileri matrisi">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi seviye hangi anomaliyi kapatır</text>
  <rect x="14" y="40" width="176" height="34" rx="6" fill="#1C3247"/>
  <text x="102" y="62" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">İzolasyon seviyesi</text>
  <rect x="194" y="40" width="148" height="34" rx="6" fill="#1C3247"/>
  <text x="268" y="62" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kirli okuma</text>
  <rect x="346" y="40" width="148" height="34" rx="6" fill="#1C3247"/>
  <text x="420" y="62" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Tekrarlanamaz okuma</text>
  <rect x="498" y="40" width="148" height="34" rx="6" fill="#1C3247"/>
  <text x="572" y="62" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Hayalet okuma</text>
  <rect x="14" y="78" width="176" height="40" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="102" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">READ UNCOMMITTED</text>
  <rect x="194" y="78" width="148" height="40" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="268" y="103" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">olur</text>
  <rect x="346" y="78" width="148" height="40" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="420" y="103" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">olur</text>
  <rect x="498" y="78" width="148" height="40" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="572" y="103" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">olur</text>
  <rect x="14" y="122" width="176" height="40" rx="6" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="102" y="147" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">READ COMMITTED</text>
  <rect x="194" y="122" width="148" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="268" y="147" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">engellenir</text>
  <rect x="346" y="122" width="148" height="40" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="420" y="147" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">olur</text>
  <rect x="498" y="122" width="148" height="40" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="572" y="147" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">olur</text>
  <rect x="14" y="166" width="176" height="40" rx="6" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="102" y="191" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">REPEATABLE READ</text>
  <rect x="194" y="166" width="148" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="268" y="191" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">engellenir</text>
  <rect x="346" y="166" width="148" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="420" y="191" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">engellenir</text>
  <rect x="498" y="166" width="148" height="40" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="572" y="185" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">standartta olur</text>
  <text x="572" y="199" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">motora göre kapanabilir</text>
  <rect x="14" y="210" width="176" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="102" y="235" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SERIALIZABLE</text>
  <rect x="194" y="210" width="148" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="268" y="235" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">engellenir</text>
  <rect x="346" y="210" width="148" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="420" y="235" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">engellenir</text>
  <rect x="498" y="210" width="148" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="572" y="235" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">engellenir</text>
  <rect x="14" y="262" width="632" height="64" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="282" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Bu tabloda olmayan anomali: kayıp güncelleme (lost update)</text>
  <text x="330" y="301" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Oku, değiştir, yaz döngüsünde ikinci işlem birincinin yazdığını ezer; seviye yükseltmek çözmez.</text>
  <text x="330" y="318" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Çözüm sürüm alanıyla iyimser kilit, satır kilidi ya da tek adımlık atomik güncelleme cümlesidir.</text>
</svg>
```

**Güçlendirilmiş cevap**

Seviyeleri saydıktan sonra tabloyu sözle ver: READ COMMITTED kirli okumayı kapatır, REPEATABLE READ tekrarlanamaz okumayı da kapatır, hayalet okuma için standart olarak SERIALIZABLE gerekir. Sonra pratik gerçeği ekle — çoğu üretim sistemi READ COMMITTED ile çalışır ve tutarlılığı uygulama tarafında kurar. Üçüncü olarak MVCC'yi an: okuyucu ile yazar birbirini bloklamaz, her işlem bir anlık görüntü görür; bu cümle seviye tartışmasını gerçek dünyaya bağlar. Kapanışta karar ölçütünü ver: seviyeyi yükseltmeden önce sorunun hangi anomali olduğunu adlandır, çünkü kayıp güncelleme seviyeyle çözülmez (`84`, `224`).

### 9 · Veritabanı anomalileri

**Soruldu / Verdiğin cevap**

Mülakatçı hangi veritabanı anomalilerini bildiğini sordu.

> "Dirty Read, Non-Repeatable Read ve Phantom Read."

**Değerlendirme**

**✅ Yeterli.** Üçünü de doğru saymışsın ve bir önceki cevapla tutarlısın. Ama yalnız ad saymak bu basamakta sınırda kalır. Mülakatçı genellikle tanım ister, sonra listede olmayanı sorar.

**Takip sorusu riski**

- **Tanımları tek cümlede verebilir misin?** Kirli okuma kesinleşmemiş veriyi okumaktır. Tekrarlanamaz okuma aynı satırı iki kez okuyup farklı değer görmektir. Hayalet okuma aynı koşulla iki kez sorgulayıp farklı sayıda satır görmektir.
- **Listede olmayan bir anomali var mı?** Kayıp güncelleme (lost update). İki işlem aynı satırı okur, değiştirir ve yazar; ikincisi birincinin yazdığını ezer.
- **Kayıp güncellemeyi ne çözer?** Sürüm alanıyla iyimser kilit, kötümser satır kilidi ya da tek adımlık atomik güncelleme cümlesi. Seviye yükseltmek tek başına yetmez.
- **İyimser kilit nasıl davranır?** Sürüm uyuşmazsa istisna fırlar ve işlem yeniden denenir. Çekişme düşükse bu yol daha ucuzdur.
- **Yazma çarpıklığı (write skew) nedir?** İki işlem farklı satırları okuyup yazar ama birlikte bir iş kuralını bozar. Standartta yalnız SERIALIZABLE kapatır.

**Güçlendirilmiş cevap**

Üç anomaliyi sayarken her birine dört kelimelik bir tanım iliştirirsen cevap iki kat güçlenir. Asıl ayrışma ise şu ekleme ile gelir: "bu üçü izolasyon seviyeleriyle ilgilidir, ama kayıp güncelleme onların dışındadır". Sonra çözümü ver — sürüm alanıyla iyimser kilit, çekişme yüksekse satır kilidi. Üçüncü olarak somut örnek koy: iki istek aynı stok satırını okuyup birer azaltırsa stok bir eksilir, iki değil. Kapanışta karar ölçütünü söyle; çekişme düşükse iyimser, yüksekse kötümser kilit tercih edilir (`224`, `225`).

### 10 · Transactional metot commit için bitmeyi bekler mi?

**Soruldu / Verdiğin cevap**

Mülakatçı işaretli metodun kesinleşmek için bitmesini bekleyip beklemediğini sordu.

> "Evet. Vekil metodu sarar; metot başarıyla biterse kesinleştirilir."

**Değerlendirme**

**✅ Yeterli.** Cevap doğru ve mekanizmayı da doğru yere bağlamışsın. Vekilden söz etmen üçüncü başlıkla tutarlı olduğunu gösteriyor. Eksik olan şey sonucudur: işlem metot boyunca açık kalıyorsa metodun içinde ne yaptığın kritik hâle gelir.

**Takip sorusu riski**

- **Kesinleşme tam olarak nerede olur?** Vekilin sarmalayıcısında, hedef metot döndükten sonra. Metot içindeki `save()` çağrısı kesinleşme demek değildir.
- **JPA'da `save()` neden hemen SQL üretmeyebilir?** Kalıcı bağlam (persistence context) yazmaları erteler. Boşaltma (flush) genellikle kesinleşme anında ya da sorgu öncesinde olur.
- **Kesinleşme sonrası iş nasıl yapılır?** İşlem eşzamanlaması ile ya da kesinleşme sonrasına bağlı olay dinleyicisiyle. Klasik hata olayı kesinleşmeden önce yayımlamaktır.
- **İşlem içinden dış servis çağırmak neden yanlış?** Bağlantı ve kilitler HTTP yanıtı boyunca tutulur. Dış servis yavaşlarsa bağlantı havuzu tükenir ve uygulama tamamen durur.
- **Dış çağrı geri alınabilir mi?** Hayır. Veritabanı geri alınır ama gönderilen istek gönderilmiştir; ödeme çekilmiş, e-posta gitmiş olabilir.
- **Doğrusu nedir?** İşlem kısa tutulur, dış çağrı kesinleşme sonrasına alınır, gerekiyorsa giden kutusu tablosu (outbox) kullanılır.

**Güçlendirilmiş cevap**

Cevabına süre boyutunu eklersen ayrışırsın: işlem metot süresince açıktır, dolayısıyla metodun uzunluğu doğrudan bağlantı ve kilit süresidir. İkinci ekleme JPA davranışı olsun — `save()` çağrısı SQL üretmeyebilir, yazmalar kesinleşme anında boşaltılır. Üçüncüsü en değerli olanıdır: işlem içinde dış servis çağırmam, çünkü işlem süresini başka bir sistemin hızına bağlamak olur. Bedelini de somutla; havuzdaki bağlantılar dış servis yavaşlayınca biter ve tek bir yavaş bağımlılık tüm uygulamayı durdurur. Kapanışta çözümü ver: dış çağrı kesinleşme sonrasına alınır ya da giden kutusu üzerinden yürütülür (`83`, `84`).

### 11 · Self-invocation nedir?

**Soruldu / Verdiğin cevap**

Mülakatçı self-invocation kavramını sordu.

> "`this.metot()` şeklinde aynı sınıf içinden çağırırsan vekilden geçmez, dolayısıyla davranış devreye girmez."

**Değerlendirme**

**✅ Yeterli, hatta bu bölümün en net cevaplarından biri.** Hem olguyu hem sebebini tek cümlede söylemişsin. Not düşülecek tek şey şu: bu soru üçüncü başlıktaki vekil sorusunun devamıdır. Orada "teorik olarak biliyorum" demişken burada mekanizmayı doğru anlatman, aslında o bilgiyi zaten taşıdığını gösteriyor.

**Takip sorusu riski**

- **Neden vekilden geçmez?** Vekil hedef nesneyi sarar. Hedef nesnenin içindeki `this` vekil değildir, gerçek nesnedir. Çağrı sarmalayıcıya hiç uğramaz.
- **Nasıl çözülür?** En sağlıklı yol metodu ayrı bir bean'e taşımak ve o bean'i enjekte edip çağırmaktır. Çağrı böylece dışarıdan gelir.
- **Diğer yollar nelerdir?** Kendini enjekte etmek ya da vekile çalışma anında erişmek. İkisi de çalışır ama çerçeveyi iş koduna sızdırır ve son çare sayılır.
- **Aynı tuzak başka nerede çıkar?** `@Cacheable`, `@Async`, `@Retryable` ve `@PreAuthorize` işaretlerinin hepsi vekil tabanlıdır. Aynı hata hepsinde geçerlidir.
- **Bu hata testle yakalanır mı?** Birim testinde çoğu zaman yakalanmaz. Gerçek bean üzerinden çalışan bir entegrasyon testi gerekir.

**Güçlendirilmiş cevap**

Cevabını koru, üstüne çözüm sıralaması ekle: önce metodu ayrı bir bean'e taşımak, sonra diğer yollar. Tercih gerekçeni de söyle — ayrı bean çözümü çerçeveyi iş koduna sızdırmaz ve sınıfın sorumluluğunu da netleştirir. İkinci ekleme kapsam olsun: aynı tuzak önbellek, eşzamansız çalıştırma ve yeniden deneme işaretlerinde de aynı biçimde işler. Üçüncüsü tehlikeyi adlandırsın; bu hata istisna fırlatmaz, günlüğe satır düşmez ve ancak veri tutarsızlığıyla fark edilir. Kapanışta testi an: gerçek bean üzerinden bir entegrasyon testi bu sınıf hataları için tek güvenilir ağdır (`83`).

### 12 · Checked ile unchecked farkı

**Soruldu / Verdiğin cevap**

Mülakatçı checked ve unchecked istisna farkını, ardından dosya işlemindeki istisnanın hangisi olduğunu sordu.

> "Checked istisna derleme zamanında yakalanması ya da bildirilmesi zorunlu olandır. Unchecked ise `RuntimeException` türevidir, böyle bir zorunluluk yoktur. Dosya işleminde `IOException` checked'dır."

**Değerlendirme**

**✅ Yeterli.** Tanım doğru, örnek de doğru. Bu sorunun burada sorulmuş olması tesadüf değil; bir sonraki soru `rollbackFor` idi. Mülakatçı zemini hazırlıyordu ve sen sağlam cevap verdiğin için son basamağa geçti.

**Takip sorusu riski**

- **`Error` nerede durur?** `Throwable` altında, `Exception` ile kardeştir. `OutOfMemoryError` gibi durumlar yakalanmaz; uygulama toparlanamaz.
- **Kendi istisnanı checked mı unchecked mı tasarlarsın?** Çağıran anlamlı bir eylem yapabiliyorsa checked. Yapamıyorsa unchecked; zorunlu yakalama yalnız gürültü üretir.
- **Yakalayıp yutmak neden yasaktır?** Boş `catch` bloğu hatayı görünmez kılar. En azından günlüğe yazılır ya da sarmalanarak yukarı taşınır.
- **Sarmalarken neye dikkat edilir?** Asıl sebep (`cause`) korunmalıdır. Sebebi düşürmek yığın izini kesip hata ayıklamayı imkânsızlaştırır.
- **Kaynak kapatma nasıl yapılır?** `try-with-resources` ile. `finally` içinde elle kapatmak hata payı yüksektir.
- **Bunun transaction ile ilgisi ne?** Varsayılan geri alma kuralı tam olarak bu ayrıma dayanır. Bir sonraki başlık budur.

**Güçlendirilmiş cevap**

Tanımdan sonra tasarım ölçütünü eklersen ayrışırsın: çağıran anlamlı bir şey yapabiliyorsa checked, yapamıyorsa unchecked. İkinci ekleme `Error` olsun; `Throwable` altında `Exception` ile kardeştir ve yakalanmaz. Üçüncüsü uygulama alışkanlığı: boş `catch` bloğu yazılmaz, sarmalarken asıl sebep korunur, kaynaklar `try-with-resources` ile kapatılır. Kapanışta köprüyü kendin kur — "bu ayrım Spring'de doğrudan işe yarar, çünkü varsayılan geri alma kuralı unchecked istisnalara bakar". Bu cümle bir sonraki soruyu senin açman demektir (`17`).

### 13 · `rollbackFor` biliyor musun?

**Soruldu / Verdiğin cevap**

Mülakatçı `rollbackFor` ayarını bilip bilmediğini sordu.

> "Varsayılan olarak `RuntimeException` ve `Error` durumunda geri alınır. Checked istisnada geri alınmasını istiyorsan `rollbackFor` vermen gerekir."

**Değerlendirme**

**✅ Yeterli ve dökümün en ince teknik cevabı.** Hem varsayılan davranışı hem de nasıl değiştirileceğini bilmişsin. Bu ayrıntıyı bilen aday oranı düşüktür. Cevap doğru, eklenecek şey yalnız derinlik.

**Takip sorusu riski**

- **`noRollbackFor` ne işe yarar?** Ters yönde çalışır. Belirli bir unchecked istisnada geri alınmasını istemiyorsan kullanılır; iş akışında beklenen durumlar için uygundur.
- **Bu varsayılan neden böyle?** Checked istisna tarihsel olarak "beklenen iş durumu" sayılmıştır. Unchecked istisna ise programlama hatası kabul edilir ve geri alınması istenir.
- **İstisnayı metot içinde yakalarsan?** Geri alma tetiklenmez. Yarım kalmış iş sessizce kesinleşir; bu en sinsi hatalardan biridir.
- **İç metot geri alma işaretledi, dış metot yuttu?** Kesinleştirme `UnexpectedRollbackException` ile başarısız olur. Yutmak seni kurtarmaz.
- **`rollbackFor = Exception.class` her yere yazılmalı mı?** Takım kararı olabilir ve savunulabilir. Önemli olan kararın açık olması; sessiz varsayılana güvenmek riskli olandır.

**Güçlendirilmiş cevap**

Cevabına neden ekleyerek bitirirsen soru tamamen kapanır: varsayılan böyledir çünkü checked istisna beklenen iş durumu, unchecked istisna programlama hatası sayılır. İkinci ekleme `noRollbackFor` olsun; ters yönde aynı işi görür. Üçüncüsü en değerli uyarıdır — istisnayı metot içinde yakalayıp yutarsan geri alma hiç tetiklenmez ve yarım iş kesinleşir. Dördüncü olarak `UnexpectedRollbackException` durumunu an; iç işlem geri alınacak diye işaretlendiyse dışarıda yutmak sonucu değiştirmez. Kapanışta takım kararını söyle: sessiz varsayılana güvenmek yerine kuralı açıkça yazmak tercih edilir (`83`, `17`).

### Bu bölümden çıkan ders

Bu bölümde cevapların büyük çoğunluğu doğruydu ve bazıları gerçekten ince bilgi taşıyordu; `rollbackFor` ve self-invocation cevapları bunun kanıtıdır. Ortak zayıflık bilgi eksikliği değil, cevabın kısa kesilmesidir. Neredeyse her başlıkta tanım verilmiş, ardından gelen "peki bedeli ne, peki ya şu durumda" halkası boş bırakılmıştır. Mülakatçı da tam o boşluğu doldurmak için bir üst basamağı sormuştur. İkinci ders sunumla ilgilidir: doğru bildiğin bir konuyu "teorik olarak biliyorum" diye açmak, bilgiyi olduğundan zayıf gösterir. Dürüstlük değerlidir ama yerini bilir; önce bildiğini net anlat, sınırı en sonda tek cümleyle söyle. Üçüncü ders zincirdir. Vekil nesne, `final` metot ve self-invocation tek bir mekanizmadır; bunlardan birini anlatırken diğer ikisini kendin bağlarsan mülakatçının sıradaki sorusunu sen sormuş olursun.

# dkmspr-1 | Orta
## Soru
Bir ekip alan enjeksiyonundan (field injection) yapıcı enjeksiyonuna (constructor injection) geçiyor. Bu geçişin en somut teknik kazancı hangisidir?
## A
Uygulamanın açılış süresi kısalır, çünkü Spring artık yansıma (reflection) kullanmak zorunda kalmaz
🤖 açılış süresi bu kararın ölçütü değildir; kazanç tasarım tarafındadır.
## B
Bean'ler otomatik olarak prototype kapsamına geçer ve iş parçacıkları arasında durum paylaşımı biter
🤖 enjeksiyon biçimi kapsamı (scope) değiştirmez; ikisi ayrı konudur.
## C
Döngüsel bağımlılıklar sessizce çözülür, uygulama her koşulda sorunsuz açılır ve hata gizlenir
🤖 tersi olur; yapıcı enjeksiyonunda döngü açılışta hata verir.
## D
Bağımlılıklar `final` olabilir, nesne eksiksiz kurulur ve döngüsel bağımlılık açılışta yakalanır
🤖 Doğru: bağımlılık listesi görünür olur, gizli bağımlılık kalmaz.
## Doğru
D
## Açıklama
Yapıcı enjeksiyonunun kazancı üç başlıkta toplanır. Birincisi **görünürlük**: sınıfın neye ihtiyaç duyduğu yapıcı imzasında yazar, gizli bağımlılık kalmaz. Yapıcıda parametre sayısı şişerse bu bir tasarım geri bildirimidir; sınıf muhtemelen çok iş yapıyordur. İkincisi **değişmezlik**: bağımlılık `final` yapılabilir, nesne kurulduktan sonra değişmez ve eşzamanlı erişimde güvenli kalır. Üçüncüsü **döngü yakalama**: iki bean birbirini yapıcıdan istiyorsa uygulama açılışta hata verir, alan enjeksiyonunda ise döngü sessizce çalışır ve tasarım hatası gizlenir (`68`). C şıkkı tam olarak bu davranışı ters çevirdiği için yanlıştır. B şıkkı enjeksiyon biçimi ile kapsamı karıştırır; kapsam ayrı bir karardır (`69`). A şıkkı ölçütü performansa taşır, oysa fark ölçülebilir düzeyde değildir. Testte kazanç da buradan gelir: çerçeveyi hiç kurmadan `new Servis(sahteDepo)` yazılabilir (`71`).

# dkmspr-2 | İleri
## Soru
Bir servis sınıfındaki `@Transactional` işaretli metot `final` olarak tanımlanmış ve sınıf hiçbir arayüz uygulamıyor. Uygulama hatasız açılıyor, ama geri alma hiç çalışmıyor. Sebep nedir?
## A
`final` metotlar Spring tarafından hiç taranmaz, bu yüzden bu bean container'a kaydedilmemiştir
🤖 bean normal biçimde kaydedilir; sorun kayıtta değil sarmalamadadır.
## B
CGLIB alt sınıf üretip metodu geçersiz kılamaz; çağrı doğrudan hedefe gider, vekil devrede olmaz
🤖 Doğru: sarmalanamayan metotta işlem yönetimi sessizce kaybolur.
## C
Arayüz olmadığı için JDK vekili kurulamaz ve Spring bu durumda işlemi programatik olarak yönetir
🤖 arayüz yoksa sınıf tabanlı vekile düşülür; programatik yönetim kendiliğinden gelmez.
## D
`final` metot yalnız salt okunur işlem alabilir; yazma işlemleri çerçeve tarafından reddedilir
🤖 böyle bir kural yoktur; ortada reddedilen değil hiç açılmayan bir işlem vardır.
## Doğru
B
## Açıklama
Spring'de `@Transactional` davranışı **vekil nesnede** yaşar. Container açılışta bean'i sarar ve enjekte edilen nesne aslında bu vekildir. Sınıf bir arayüz uygulamıyorsa JDK dinamik vekili kullanılamaz, sınıf tabanlı vekile (CGLIB) düşülür. CGLIB hedef sınıfın alt sınıfını üretir ve metotları geçersiz kılarak sarar. `final` metot geçersiz kılınamaz; `private` metot da aynı sebeple sarılamaz. Sonuç şudur: metot sarılmaz, çağrı doğrudan hedefe gider ve işlem hiç açılmaz. En tehlikeli yanı sessiz olmasıdır — istisna fırlamaz, günlüğe satır düşmez ve sorun aylar sonra tutarsız veri olarak görünür (`72`, `83`). C şıkkı arayüz yokluğunun sonucunu yanlış kurar; çerçeve programatik yönetime kendiliğinden geçmez. A şıkkı bean kaydı ile sarmalamayı karıştırır. D şıkkı var olmayan bir kural uydurur. Aynı mekanizma `@Cacheable`, `@Async` ve `@Retryable` için de geçerlidir.

# dkmspr-3 | İleri
## Soru
`SiparisServisi` içindeki `hepsiniIsle()` metodu, aynı sınıftaki `@Transactional` işaretli `tekiniIsle()` metodunu `this` üzerinden çağırıyor ve her kayıt ayrı işlemde yönetilsin isteniyor. Geri alma çalışmıyor. En sağlıklı çözüm hangisidir?
## A
`hepsiniIsle()` metoduna da `@Transactional` eklemek; böylece iç çağrı da işlem kapsamına girer
🤖 dış metot sarılır ama artık tek büyük işlem olur; kayıt başına yönetme niyeti kaybolur.
## B
`tekiniIsle()` metodunu `public` yerine `protected` yapmak ve sınıfı `final` olmaktan çıkarmak
🤖 görünürlük değişikliği çözmez; sorun çağrının vekile hiç uğramamasıdır.
## C
`tekiniIsle()` metodunu ayrı bir bean'e taşımak ve o bean'i enjekte edip onun üzerinden çağırmak
🤖 Doğru: çağrı dışarıdan gelir, vekilden geçer, çözüm çerçeveye sızmaz.
## D
Vekile çalışma anında erişip metodu onun üzerinden çağırmak ve bunun için ek ayarı açmak
🤖 çalışır ama ek ayar ister ve Spring'i iş koduna sızdırır; son çare sayılır.
## Doğru
C
## Açıklama
Bu **self-invocation** tuzağıdır. Vekil hedef nesneyi sarar; hedef nesnenin içindeki `this` vekil değil, gerçek nesnedir. Bu yüzden `this.tekiniIsle()` çağrısı sarmalayıcıya hiç uğramaz ve işlem açılmaz. Çözüm çağrıyı dışarıdan gelir hâle getirmektir: metot ayrı bir bean'e taşınır ve o bean enjekte edilip çağrılır (`83`). A şıkkı belirtiyi örter ama soruda istenen davranışı bozar; dış metoda işaret koyunca tek büyük işlem oluşur ve bir kaydın hatası tüm partiyi geri alır. B şıkkı görünürlükle vekil mekanizmasını karıştırır; hatta `protected` metot sınıf tabanlı vekilde sarılabilse de sorun çağrının yönündedir. D şıkkı gerçekten çalışan bir yoldur, ama çerçeveyi iş koduna sızdırır ve ek yapılandırma ister; son çare olarak anılır. Aynı tuzak `@Cacheable`, `@Async` ve `@PreAuthorize` için de geçerlidir ve birim testinde çoğu zaman görünmez; gerçek bean üzerinden entegrasyon testi gerekir (`72`).

# dkmspr-4 | İleri
## Soru
Bir sipariş metodu iş kaydını yazıyor ve ayrıca bir denetim (audit) kaydı atıyor. İş kaydı geri alınsa bile denetim kaydının veritabanında kalması isteniyor. Hangi yaklaşım doğrudur?
## A
Denetim kaydını `REQUIRES_NEW` ile ayrı işlemde yazmak; bu işlem dıştaki geri almadan etkilenmez
🤖 Doğru: dış işlem askıya alınır, iç işlem kendi başına kesinleşir.
## B
Denetim kaydını `NESTED` ile yazmak; kayıt noktası sayesinde dış geri alma iç kaydı etkilemez
🤖 NESTED kayıt noktası kullanır ama dış geri alma iç kısmı da beraberinde götürür.
## C
Kaydı aynı `REQUIRED` işleminde bırakıp `noRollbackFor` eklemek; kayıt bu sayede korunmuş olur
🤖 `noRollbackFor` istisna türünü etkiler; tek işlem yine bütün hâlinde geri alınır.
## D
Denetim kaydını `readOnly = true` ile yazmak; salt okunur işlem geri alma kapsamının dışındadır
🤖 salt okunur işlem yazma amaçlı değildir; kaydı korumakla ilgisi yoktur.
## Doğru
A
## Açıklama
İstenen şey iki işin **farklı kaderleri** olmasıdır: iş kaydı geri alınabilir, denetim kaydı kalıcı olmalıdır. Bunu sağlayan tek propagation seçeneği `REQUIRES_NEW`'dir. Dış işlem askıya alınır, iç iş ayrı bir bağlantıda kendi başına kesinleşir ve dış geri almadan etkilenmez (`84`). Bedeli de söylenmelidir: aynı anda iki bağlantı tutulur, bu da havuz baskısı yaratır. Dış işlem denetim tablosundaki bir satırı kilitlemişse iç işlem onu beklerken kendi kendine kilitlenme doğabilir. B şıkkı `NESTED`'i yanlış okur; kayıt noktası aynı bağlantıda çalışır ve dış işlem geri alınırsa iç kısım da gider. C şıkkı `noRollbackFor` ile kapsamı karıştırır; bu ayar hangi istisnada geri alınacağını belirler, tek işlemi ikiye bölmez. D şıkkı `readOnly` işaretini yanlış kullanır; bu işaret okuma amaçlı metotlar içindir. Uygulamada denetim kaydı çoğu zaman ayrı işlemde ya da giden kutusu (outbox) benzeri bir yolla yazılır (`83`).

# dkmspr-5 | Uzman
## Soru
İki eşzamanlı istek aynı ürünün stoğunu okuyup birer azaltıp geri yazıyor. Stok 10'dan 9'a düşüyor, oysa 8 olması gerekiyordu. İzolasyon seviyesini REPEATABLE READ yapmak bu sorunu çözer mi?
## A
Evet; REPEATABLE READ aynı satırın tekrar okunmasını garanti ettiği için ikinci yazma engellenir
🤖 tekrar okuma garantisi yazma sırasını düzeltmez; ezme yine gerçekleşir.
## B
Hayır; bu kayıp güncellemedir, sürüm alanıyla iyimser kilit ya da satır kilidi gerekir
🤖 Doğru: anomali oku-değiştir-yaz döngüsünde doğar, seviye tek başına yetmez.
## C
Evet; seviye yükseldikçe bütün anomaliler kapandığı için bu durum da kendiliğinden ortadan kalkar
🤖 tablodaki üç anomali kapanır; kayıp güncelleme o tabloda hiç yoktur.
## D
Hayır; tek çözüm izolasyonu SERIALIZABLE yapmaktır, uygulama tarafında yapılabilecek bir şey yoktur
🤖 SERIALIZABLE bir seçenektir ama tek çözüm değildir ve bedeli ağırdır.
## Doğru
B
## Açıklama
Buradaki anomalinin adı **kayıp güncellemedir** (lost update) ve klasik izolasyon tablosunda yer almaz. O tablo üç anomaliyi kapsar: kirli okuma, tekrarlanamaz okuma ve hayalet okuma (`224`). Kayıp güncelleme farklı bir kökten doğar: iki işlem aynı satırı okur, değeri bellekte değiştirir ve geri yazar; ikinci yazma birincinin sonucunu ezer. Seviye yükseltmek okuma tutarlılığını artırır, ama iki ayrı oku-değiştir-yaz döngüsünü sıraya sokmaz. Uygulanabilir üç çözüm vardır. Birincisi **iyimser kilit**: satırda bir sürüm alanı tutulur, sürüm uyuşmazsa istisna fırlar ve işlem yeniden denenir; çekişme düşükse en ucuz yoldur. İkincisi **kötümser kilit**: satır okunurken kilitlenir, ikinci işlem bekler; çekişme yüksekse tercih edilir (`225`). Üçüncüsü tek adımlık atomik güncelleme cümlesidir; azaltmayı veritabanına yaptırmak yarışı tamamen ortadan kaldırır. D şıkkı SERIALIZABLE'ı tek çözüm sayar; işe yarar ama iş hacmini düşürür ve yeniden deneme gerektirir.

# dkmspr-6 | Uzman
## Soru
`@Transactional` işaretli bir metot önce veritabanına yazıyor, sonra aynı metot içinde dış ödeme servisine HTTP çağrısı yapıp yanıtı bekliyor. Bu tasarımın en ağır sonucu nedir?
## A
Dış çağrı işlem içinde olduğu için başarısız olduğunda o çağrı da geri alınır ve yan etki kalmaz
🤖 HTTP çağrısı geri alınamaz; veritabanı geri alınır, dış etki yerinde kalır.
## B
Vekil dış çağrıyı göremediği için `@Transactional` bu metotta hiç devreye girmez ve kesinleşme atlanır
🤖 dış çağrı vekili etkilemez; işlem normal biçimde açılır ve kapanır.
## C
İzolasyon seviyesi otomatik olarak SERIALIZABLE'a yükselir ve okuma başarımı belirgin biçimde düşer
🤖 böyle bir yükseltme yoktur; sorun izolasyonda değil işlemin süresindedir.
## D
Bağlantı ve kilitler HTTP yanıtı boyunca tutulur; dış servis yavaşlarsa bağlantı havuzu tükenir
🤖 Doğru: işlem süresi dış sistemin hızına bağlanmış olur.
## Doğru
D
## Açıklama
İşlem, metot boyunca **açık kalır**. Bu da veritabanı bağlantısının ve alınan kilitlerin HTTP yanıtı gelene kadar tutulması demektir. Dış servis yavaşladığında bekleyen her istek bir bağlantı işgal eder ve havuz kısa sürede tükenir. O andan itibaren o servise hiç uğramayan istekler bile bağlantı bulamaz; tek bir yavaş bağımlılık tüm uygulamayı durdurur (`84`). İkinci sorun geri alınamazlıktır: işlem geri alınsa bile gönderilmiş HTTP isteği geri alınmaz. Ödeme çekilmiş, e-posta gitmiş olabilir; veritabanı temiz, dış dünya kirli kalır. A şıkkı tam olarak bunu ters kurar. B şıkkı vekil mekanizmasını yanlış anlar; dış çağrının varlığı sarmalamayı etkilemez. C şıkkı izolasyon ile süreyi karıştırır. Doğru tasarım üç adımlıdır: işlemi kısa tut, dış çağrıyı kesinleşme sonrasına al, güvence gerekiyorsa niyeti aynı işlemde bir giden kutusu tablosuna (outbox) yaz ve ayrı bir süreçle gönder (`83`).
