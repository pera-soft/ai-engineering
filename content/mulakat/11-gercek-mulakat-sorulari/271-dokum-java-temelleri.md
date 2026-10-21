---
slug: dokum-java-temelleri
baslik: "Döküm · Java Temelleri ve Modern Java: Verdiğin Cevapların Değerlendirmesi"
kademe: Orta
sira: 271
etiketler: 11
---

## Konu anlatımı

Bu dosya bir tahmin listesi değil. Elimizde gerçek bir teknik mülakatın dökümü var. Mülakatçının sorduğu sorular ve senin o an verdiğin cevaplar kayıtlı. Yani burada iskelet değil, performans verisi işleniyor. On bir soru döküm sırasıyla ele alınıyor. Her sorunun altında dört parça var: ne soruldu ve ne cevap verdin, cevap yeterli miydi, mülakatçı bundan sonra nereye giderdi, aynı soru daha güçlü nasıl cevaplanırdı. Değerlendirmeler dürüst. İyi cevaba iyi denir, eksik olana eksik denir; şişirilmiş övgü işe yaramaz. Cevabın çöpe atılmaz, üzerine eklenir.

### "Java'da primitive tipler kaç tane, hangileri?"

**Soruldu / Verdiğin cevap**

> "8 tane var: byte, short, int, long, float, double, char, boolean."

**Değerlendirme**

✅ **Yeterli.** Bu bir hatırlama sorusu ve cevap tam. Sekizini de eksiksiz saymışsın, fazladan bir şey eklememişsin. Böyle sorularda mülakatçı doğrulamayı alır almaz bir üst basamağa geçer. Nitekim dökümde de öyle oldu.

**Takip sorusu riski**

- **"Bit boyutları ne?"** → `byte` 8, `short` 16, `int` 32, `long` 64, `float` 32, `double` 64, `char` 16 bit. `boolean` için boyut belirtilmemiştir; JVM'in tercihine bırakılmıştır.
- **"Varsayılan değerleri?"** → Sayısal tipler sıfır, `char` boş karakter, `boolean` `false`. Ama bu yalnız alanlar için geçerlidir. Yerel değişkenin varsayılanı yoktur; atanmadan kullanılırsa derlenmez.
- **"`char` neden işaretsiz?"** → `char` sayı değil, metin birimi tutar. Aralığı 0 ile 65535 arasıdır ve bir UTF-16 kod birimine karşılık gelir.
- **"Kutulama (autoboxing) nedir?"** → Derleyici `int` ile `Integer` arasındaki dönüşümü kendisi ekler. Asıl tuzak burada: küçük değerler için `Integer` nesneleri önbellekten gelir. Bu yüzden `==` karşılaştırması küçük sayılarda doğru, büyük sayılarda yanlış görünür. Kutulanmış tipler daima `equals` ile karşılaştırılır (`3`).

**Güçlendirilmiş cevap**

Sekizi say, sonra tek cümlelik bir katman ekle: "Bunlar değer olarak durur, referans olarak değil; bu yüzden `null` olamazlar." Ardından tuzağı kendin getir. "Kutulanmış karşılıkları `null` olabilir, o yüzden veri tabanından gelen alanlarda `Integer` tercih edilir." Son cümlede önbelleği an: "Küçük değerlerde `Integer` nesneleri önbellekten geldiği için `==` yanıltır; karşılaştırmayı `equals` ile yaparım." Böylece hatırlama sorusunu, muhakeme gösteren bir cevaba çevirirsin. Mülakatçının soracağı takip sorusunu sen sormuş olursun; bu, konuşmanın yönünü sana verir (`2`).

### "`final` anahtar kelimesi nerede kullanılır?"

**Soruldu / Verdiğin cevap**

> "Değişkende değerin değişmesini, metotta override edilmesini, sınıfta extend edilmesini engeller."

**Değerlendirme**

✅ **Yeterli.** Üç kullanım yerini de doğru saymışsın ve her birinin etkisini tek kelimeyle vermişsin. Cevap net, gereksiz uzatma yok. Ama tam burada mülakatçının en sevdiği ayrım duruyor ve sen ona kapıyı açık bırakmışsın.

**Takip sorusu riski**

- **"`final List` içine eleman eklenebilir mi?"** → Evet, eklenebilir. `final` referansı kilitler, nesnenin içeriğini değil. Bu ayrımı bilmeyen aday burada elenir.
- **"O hâlde değişmezlik (immutability) nasıl sağlanır?"** → Alanlar `final` ve `private`, sınıf `final`, koleksiyonlar kurucuda kopyalanır ve dışarı okunur kopya verilir. Yani `final` gerekli şarttır, yeterli şart değildir (`42`).
- **"`final` alan ne zaman atanabilir?"** → Tanımda ya da kurucuda; bir kez. İkinci atama derlenmez. Bu yüzden kurucu enjeksiyonunda alanları `final` yapabilirsin.
- **"`final` performans getirir mi?"** → Doğrudan bir hız vaadi verme. Asıl kazanç okunabilirlik ve iş parçacıkları (thread) arası güvenli yayınlamadır.

**Güçlendirilmiş cevap**

Üç kullanımı saydıktan sonra ayrımı sen kur: "`final` bağı sabitler, içeriği değil." Sonra somut örnek ver: "`final List<String> roller` başka bir listeye bağlanamaz, ama içine eleman eklenebilir." Bir cümle daha ekle: "Gerçek değişmezlik için alanlar `final`, koleksiyonlar kurucuda kopyalanmış ve dışarı okunur biçimde verilmiş olmalı." Kapanışta `record` bağlantısını kur: "`record` bu kalıbı dil düzeyine taşır ama onun değişmezliği de sığdır." Bu son cümle, ilerideki `record` sorusunu senin lehine hazırlar (`15`, `42`).

### "Hataların adı ne? Exception handling nasıl yaparsın?"

**Soruldu / Verdiğin cevap**

> "Try-catch bloğu ile yakalarım, gerekirse loglarım. Finally kullanırım. Mümkün olduğunca spesifik exception yakalamaya çalışırım."

**Değerlendirme**

🟡 **Doğru ama eksik.** Söylediklerin yanlış değil; hatta "mümkün olduğunca spesifik yakalarım" cümlesi iyi bir refleks. Sorun şu: bu cevap Java 6 dönemine ait bir cevap. `finally` ile kaynak kapatmak on yıldan uzun süredir önerilen yol değil. Ayrıca cevapta hiç katman yok. Mülakatçı burada "peki web katmanında ne yapıyorsun" diye sorar ve cevabın devamı gelmezse bilgi eksiği görünür hâle gelir.

**Takip sorusu riski**

- **"Kaynağı nasıl kapatırsın?"** → `try-with-resources` ile. `AutoCloseable` uygulayan kaynak blok bitince kapanır. `finally` içinde kapatmanın iki kusuru var: kod uzar ve `finally` içinde fırlayan hata asıl hatayı bastırabilir (`18`).
- **"Boş `catch` neden yasak?"** → Hata yutulur, kök neden kaybolur. Sistem hatalı çalışmaya devam eder ama kimse haberdar olmaz. Bu, üretimde en pahalı hata sınıfıdır.
- **"İstisnayı sararken neye dikkat edersin?"** → Özgün hatayı `cause` olarak taşırım. `new BusinessException(mesaj, e)` yazarım. `cause` düşerse yığın izi kopar ve hata ayıklama kör kalır.
- **"Web katmanında nasıl yönetiyorsun?"** → `@ControllerAdvice` ile tek yerde toplarım. İstemciye `ProblemDetail` biçiminde standart bir hata gövdesi dönerim. Yığın izini istemciye asla göndermem (`78`).
- **"Kendi istisna sınıfını yazar mısın?"** → İşin anlamı varsa evet. `StokYetersizException` gibi bir ad, çağıran tarafa karar verme imkânı verir. Her şey için yeni sınıf açmak da gereksizdir.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Exception cevabinin eksik kalan yarisi: soylenenler, eksikler ve katman sorumlulugu">
  <defs>
    <marker id="dkmjava-ex-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Cevabın doğru yarısı ile eksik yarısı</text>
  <rect x="14" y="36" width="196" height="196" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Söylediklerin</text>
  <text x="112" y="81" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">try-catch ile yakalama</text>
  <text x="112" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gerekirse loglama</text>
  <text x="112" y="125" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">finally bloğu</text>
  <text x="112" y="147" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">spesifik istisna yakalama</text>
  <text x="112" y="177" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">refleks doğru</text>
  <text x="112" y="195" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ama tanım düzeyinde</text>
  <text x="112" y="213" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kalıyor</text>
  <line x1="212" y1="134" x2="228" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjava-ex-ok)"/>
  <rect x="232" y="36" width="196" height="196" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Eksik kalanlar</text>
  <text x="330" y="81" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">try-with-resources</text>
  <text x="330" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">cause zincirini koruma</text>
  <text x="330" y="125" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">anlamlı özel istisna sınıfı</text>
  <text x="330" y="147" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">checked ile unchecked ayrımı</text>
  <text x="330" y="177" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">bu dördü söylenmeyince</text>
  <text x="330" y="195" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">cevap eski bir Java'ya</text>
  <text x="330" y="213" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">ait gibi duyulur</text>
  <line x1="430" y1="134" x2="446" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjava-ex-ok)"/>
  <rect x="450" y="36" width="196" height="196" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Katman sorumluluğu</text>
  <text x="548" y="81" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alt katman: sar ve yükselt</text>
  <text x="548" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">servis: iş anlamı ver</text>
  <text x="548" y="125" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">web: ControllerAdvice</text>
  <text x="548" y="147" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemciye ProblemDetail</text>
  <text x="548" y="177" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">hata tek yerde biçimlenir</text>
  <text x="548" y="195" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yığın izi istemciye</text>
  <text x="548" y="213" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">gönderilmez</text>
  <rect x="14" y="244" width="632" height="72" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="265" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Üretimde en pahalı iki alışkanlık</text>
  <text x="330" y="285" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Boş catch: hata yutulur, sistem yanlış veriyle çalışmaya devam eder, kimse haberdar olmaz.</text>
  <text x="330" y="303" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">cause taşımadan sarmalama: yığın izi kopar, kök neden aramak saatler alır.</text>
</svg>
```

**Güçlendirilmiş cevap**

Söylediklerini koru, sonra üç katman ekle. Birincisi kaynak: "Kaynak kapatmayı `finally` ile değil `try-with-resources` ile yaparım; blok bitince kaynak kapanır ve bastırılan hata sorunu doğmaz." İkincisi bilgi kaybı: "İstisnayı sararken özgün hatayı `cause` olarak taşırım, boş `catch` yazmam." Üçüncüsü katman: "Alt katmanda sarar yükseltirim, servis katmanında işe ait anlam veririm, web katmanında `@ControllerAdvice` ile tek yerde toplayıp `ProblemDetail` dönerim." Kapanış cümlesi seni ayrıştırır: "Her istisnayı yakalamam; ele alamayacağım hatayı yukarı bırakırım." Bu cümle, hata yönetimini bir kural değil bir karar olarak gördüğünü gösterir (`16`, `17`, `18`, `78`).

### "Encapsulation nedir, bir örnek verir misin?"

**Soruldu / Verdiğin cevap**

> "Verinin dışarıdan doğrudan erişime kapatılması. Örneğin BankAccount sınıfında balance private olur, para yatırma ve çekme metotlarıyla kurallı şekilde değişir."

**Değerlendirme**

✅ **Yeterli, örnek de iyi.** Tanımı verip hemen örneğe geçmen doğru bir refleks. Seçtiğin örnek de isabetli: bakiye, kural olmadan değişmemesi gereken klasik alandır. Cevap bu hâliyle geçer. Bir üst basamağa çıkmak için tek şey eksik: kapsüllemenin en yaygın ihlalini kendin söylemek.

**Takip sorusu riski**

- **"Her alana getter ve setter yazmak kapsülleme midir?"** → Hayır. Bu, alanı `private` yapıp aynı kapıyı yeniden açmaktır. `setBalance` yazdığın anda bakiye kuralsız değişebilir hâle gelir. Kapsülleme veriyi gizlemek değil, **kararı gizlemektir**.
- **"Nesnenin içindeki listeyi getter ile dışarı verirsen ne olur?"** → Çağıran o listeyi değiştirebilir; nesnenin kuralları devre dışı kalır. Çözüm okunur kopya döndürmektir.
- **"Değişmez nesne ile ilişkisi ne?"** → Değişmezlik kapsüllemenin en sert hâlidir. Hiç değiştirme kapısı olmayınca kural ihlali de olmaz (`42`).
- **"Erişim belirleyicilerinden hangisini varsayılan sayarsın?"** → En dar olanı. Paket içi görünürlük çoğu sınıf için yeterlidir; `public` olan her şey bir sözleşmedir (`13`).

**Güçlendirilmiş cevap**

Örneğini anlattıktan sonra kuralı adlandır: "Bakiye yalnız `paraYatir` ve `paraCek` üzerinden değişir; ikisi de negatif tutarı ve yetersiz bakiyeyi reddeder." Sonra en yaygın hatayı sen söyle: "Her alana getter ve setter yazmak kapsülleme değildir; `setBakiye` koyarsam kuralı yeniden delmiş olurum." Bir cümle daha ekle: "İçerideki koleksiyonu doğrudan döndürmem, okunur kopya veririm." Kapanışta ölçütü ver: "Ölçüm şu; nesnenin geçersiz bir duruma düşmesi dışarıdan mümkün mü, değil mi." Bu cümle tanım ezberleyen adaydan seni ayırır (`13`, `42`).

### "Abstract class ile interface farkı ne? Java'da çoklu kalıtım var mı?"

**Soruldu / Verdiğin cevap**

> "Bir sınıf yalnız tek bir class'ı extend eder ama birden fazla interface implement edebilir. Abstract class ortak state ve kod taşıyabilir, interface daha çok sözleşme tanımlar."

**Değerlendirme**

✅ **Yeterli.** İki soruyu tek cevapta birleştirmişsin ve ayrımı doğru kurmuşsun. "Ortak durum taşır" ile "sözleşme tanımlar" ifadeleri tam yerinde. Çoklu kalıtım kısmını da doğru sınırlamışsın: sınıfta yok, arayüzde var. Cevap sağlam; mülakatçı buradan doğrudan bir sonraki basamağa geçer.

**Takip sorusu riski**

- **"Java 8'den sonra arayüz de kod taşıyor; fark ne kaldı?"** → Durum kaldı. Arayüz davranış verebilir ama alan tutamaz. Bir de niyet farkı var: soyut sınıf "bu bir çeşit şudur" der, arayüz "bu şunu yapabilir" der.
- **"İki arayüz aynı `default` metodu verirse ne olur?"** → Derleme hatası alırsın. Elmas sorunu (diamond problem) burada karşına çıkar ve dil onu sessizce çözmez. Uygulayan sınıf metodu açıkça geçersiz kılmak zorundadır. İçeriden birini seçmek için `Arayuz.super.metot()` yazılır.
- **"Hangisini ne zaman seçersin?"** → Ortak durum ve kurucu mantığı varsa soyut sınıf. Yalnız yetenek tanımlıyorsan arayüz. Şüphede kalırsan arayüz seç; kalıtım hiyerarşisi geri dönmesi pahalı bir karardır.
- **"Kalıtım yerine bileşim (composition) ne zaman?"** → Neredeyse her zaman. Kalıtım en sıkı bağdır; alt sınıf üst sınıfın iç davranışına bağımlı hâle gelir.

**Güçlendirilmiş cevap**

Cevabını koru, üstüne şu üç cümleyi ekle. Birincisi güncelleme: "Java 8'den sonra arayüz de `default` ve `static` metotla kod taşıyabiliyor; kalan asıl fark durum tutamamasıdır." İkincisi elmas sorunu: "İki arayüz aynı `default` metodu verirse derleme hatası alırım; uygulayan sınıf metodu açıkça geçersiz kılmalı." Üçüncüsü tercih ölçütü: "Ortak durum varsa soyut sınıf, yalnız yetenek tanımlıyorsam arayüz seçerim." Kapanışta bir takas cümlesi kur: "Kalıtım en sıkı bağdır; mümkün olduğunca bileşimi tercih ederim." Bu dört cümle, tanımı bilen adaydan tasarım tercihi yapan adaya geçişi işaret eder (`12`, `32`).

### "Interface'e gövdeli metot ne zaman geldi?"

**Soruldu / Verdiğin cevap**

> "Java 8 ile default ve static metotlar geldi, Java 9 ile private metotlar eklendi."

**Değerlendirme**

✅ **Doğru.** İki sürümü de doğru eşleştirmişsin. Bu tam bir doğrulama sorusudur ve mülakatçı burada yalnız "takip ediyor mu" bakar. Kısa ve net cevap doğru davranıştır; sürüm sorularında uzatmak fayda getirmez.

**Takip sorusu riski**

- **"`default` metot niye eklendi?"** → Geriye dönük uyum için. Java 8'de `Collection` arayüzüne `stream` gibi metotlar eklenecekti. `default` olmasaydı, o arayüzü uygulayan tüm mevcut kod derlenmez hâle gelirdi.
- **"Arayüzdeki `private` metot ne işe yarar?"** → İki `default` metodun paylaştığı ortak kodu dışarı açmadan toplamak için. Yani sözleşmeyi büyütmeden tekrarı azaltır.
- **"`static` metot arayüzde nerede kullanılır?"** → Fabrika ve yardımcı metotlarda. `List.of` bu kalıbın en bilinen örneğidir.
- **"Bu, soyut sınıfı gereksiz kıldı mı?"** → Hayır. Arayüz hâlâ durum tutamaz ve kurucu çalıştıramaz.

**Güçlendirilmiş cevap**

Sürümleri söyledikten sonra tek cümlelik gerekçe ekle: "`default`, var olan arayüzlere yeni metot eklerken mevcut uygulamaları bozmamak için geldi." Sonra somut örneği ver: "Java 8'de `Collection` arayüzüne `stream` eklenebilmesinin sebebi budur." Ardından `private` metodun yerini açıkla: "Java 9'daki `private` metot, iki `default` metodun ortak kodunu sözleşmeyi büyütmeden paylaşmak içindir." Kapanış cümlesi sınırı çizer: "Buna rağmen arayüz durum tutamaz; soyut sınıfın yeri hâlâ duruyor." Sürüm sorusuna gerekçe eklemek, ezberden değil kullanarak öğrendiğini gösterir.

### "Lambda kullanmak için nasıl bir interface gerekir?"

**Soruldu / Verdiğin cevap**

> "Functional interface gerekir, tek bir abstract metodu olmalı."

**Değerlendirme**

✅ **Yeterli.** Tanım doğru ve kısa. "Tek soyut metot" ifadesi sorunun tam çekirdeğidir. Bu cevap geçer, ama üzerine hiçbir şey eklenmediği için mülakatçı hemen bir alt ayrıntıya iner. Dökümde de böyle oldu; sıra hazır arayüzlere ve `flatMap` sorusuna geçti.

**Takip sorusu riski**

- **"`@FunctionalInterface` ne işe yarar?"** → Zorunlu değildir. Derleme zamanında bir denetim koyar: arayüze ikinci bir soyut metot eklenirse derleme hatası alırsın. Yani niyeti belgeler ve kazayla bozulmayı engeller.
- **"`default` metotlar bu sayıya girer mi?"** → Hayır. `default` ve `static` metotlar soyut sayılmaz. Bu yüzden `Comparator` onlarca metoduna rağmen lambda ile yazılabilir.
- **"Hangi hazır arayüzleri kullanırsın?"** → `Function` dönüştürür, `Supplier` üretir, `Consumer` tüketir, `Predicate` koşul döner. Sonuncusu `filter` içinde kullanılır.
- **"Lambda içinde `this` neyi gösterir?"** → Çevreleyen sınıfı. Anonim sınıfta ise anonim nesnenin kendisini gösterir. Bu fark, lambdanın ayrı bir kapsam açmamasından gelir.
- **"Metot referansı ne zaman?"** → Lambda gövdesi tek bir çağrıdan ibaretse. `s -> s.toUpperCase()` yerine `String::toUpperCase` daha okunur olur (`33`).

**Güçlendirilmiş cevap**

Tanımı verdikten sonra sayım kuralını netleştir: "Tek soyut metot şartı sağlanmalı; `default` ve `static` metotlar bu sayıya girmez." Sonra anotasyonun yerini söyle: "`@FunctionalInterface` zorunlu değil ama koyarım; ikinci bir soyut metot eklenirse derleme anında yakalanır." Ardından pratikte ne kullandığını göster: "Genelde hazır olanları kullanırım; `Function`, `Supplier`, `Consumer` ve `Predicate` çoğu ihtiyacı karşılar." Kapanışta ince bir ayrıntı bırak: "Lambda ayrı bir kapsam açmaz, bu yüzden içindeki `this` çevreleyen sınıfı gösterir." Bu son cümle, lambdayı yalnız sözdizimi olarak değil semantik olarak bildiğini söyler (`32`, `33`).

### "İç içe listede 'A' ile başlayan isimleri nasıl bulursun?"

**Soruldu / Verdiğin cevap**

> "flatMap ile tek stream'e indiririm, sonra filter ile startsWith kontrolü yapıp collect ederim."

**Değerlendirme**

✅ **Yeterli.** Üç adımı doğru sırada söylemişsin ve doğru operatörü seçmişsin. Bu soruda çoğu aday `map` der ve tipte takılır; sen doğrudan `flatMap` demişsin. Cevap kısa ama eksiksiz. Mülakatçı burada genellikle "neden `map` değil" diye sorar; hazır olmalısın.

**Takip sorusu riski**

- **"`map` ile `flatMap` farkını tek cümlede?"** → `map` her elemanı bire bir dönüştürür; `flatMap` her elemanı bir akışa açar ve o akışları tek akışta birleştirir. `map` kullansaydın elinde akış içinde akış kalırdı.
- **"Akış tembel midir?"** → Evet. `filter` ve `map` ara işlemdir, hiçbir şey yapmaz. `collect` gibi bir uç işlem gelene kadar veri akmaz. Bu sayede tek geçişte birleşik çalışırlar.
- **"`collect(toList())` ile `toList()` farkı?"** → Java 16 ile gelen `toList` daha kısadır ve değiştirilemez bir liste döner. `Collectors.toList` ise değiştirilebilirliği garanti etmez ama pratikte değiştirilebilir bir liste verir.
- **"Küçük harfle başlayanları da yakalamak isteseydin?"** → Karşılaştırmayı normalleştirirdim. Dil duyarlı büyük harf çevrimlerinde `Locale` vermek gerekir; aksi hâlde Türkçe `i` harfi sorun çıkarır.
- **"`null` eleman gelirse?"** → Akış `null` elemanı sevmez. Kaynağı temizlerim ya da `filter` ile önceden ayıklarım.

**Güçlendirilmiş cevap**

Çözümü söyledikten sonra farkı sen açıkla: "`map` bire bir dönüştürür, `flatMap` bir seviye düzler; `map` kullansaydım elimde akış içinde akış kalırdı." Sonra tembelliği an: "`filter` ara işlemdir, `collect` gelene kadar hiçbir eleman akmaz; bu yüzden zincir tek geçişte çalışır." Ardından çıktı tipini netleştir: "Java 16 sonrası `toList` kullanırım, değiştirilemez liste döner." Kapanışta bir sınır cümlesi kur: "Akış her yerde daha iyi değildir; basit döngüde okunabilirlik bazen daha yüksektir." Bu cümle, aracı savunmak yerine seçtiğini gösterir (`34`, `35`).

### "Sealed class kullandın mı? permits ne yapar?"

**Soruldu / Verdiğin cevap**

> "Teorik olarak biliyorum. Hangi sınıfların extend edebileceğini sınırlar. Java 17'de kalıcı hâle geldi."

**Değerlendirme**

🟡 **Doğru ama sunumu cevabı zayıflatıyor.** İçerik doğru: `permits` gerçekten izin verilen alt tipleri sınırlar ve özellik Java 17'de kalıcı oldu. Sorun bilgide değil, cümlenin **sırasında**.

"Teorik olarak biliyorum" diye açtığında dinleyici geri kalan her şeyi o filtreden geçirir. Doğru söylediğin iki cümle bile "ezberden söylüyor" gibi duyulur. Oysa aynı bilgiyi önce anlatıp sınırı sona bıraksaydın, cevap güçlü ve dürüst olurdu. Bu dosyadaki en önemli ders budur ve teknik bir eksiklik değildir.

Yanlış anlaşılmasın: **dürüstlük doğru karardır.** Kullanmadığın bir şeyi kullandım demek en hızlı elenme yoludur, çünkü takip sorusu mutlaka gelir. Mesele dürüst olup olmamak değil, dürüstlüğü nereye koyduğun. Sınırı **sonda** söylersin. Önce bildiğini net anlat, sonra "üretimde henüz kullanma fırsatım olmadı, kendi projemde denedim" dersin. Aynı bilgi, aynı dürüstlük, çok farklı izlenim (`256`).

Bir de içerik eksiği var. Cevabın `sealed` özelliğini tek başına anlatıyor. Oysa `sealed` yalnız bir kısıt değil; asıl değeri `record` ve desen eşleştirmeli `switch` ile birleştiğinde çıkıyor. O üçlü bir arada **tam kapsama** (exhaustiveness) verir. Bu, mülakatta ayrıştırıcı olan kısımdı ve söylenmedi.

**Takip sorusu riski**

- **"Alt tipler ne yapmak zorunda?"** → Her biri `final`, `sealed` ya da `non-sealed` olmalı. Yani hiyerarşi açık uçlu bırakılamaz; kapalılık aşağı doğru taşınır.
- **"`sealed` ne kazandırır?"** → Derleyici alt tiplerin tam listesini bilir. Bu sayede `switch` içinde tüm durumlar karşılanmışsa `default` yazmak gerekmez.
- **"`default` yazmamak neden iyi?"** → Yeni bir alt tip eklendiğinde derleyici seni uyarır. `default` koyarsan yeni tip sessizce oraya düşer ve hata çalışma anında, üretimde ortaya çıkar.
- **"Nerede kullanırdın?"** → Kapalı bir küme modellerken. Ödeme türleri, sipariş durumları, hesaplama sonucu gibi. Sonuç tipi için `Basarili` ve `Hata` alt tipleri klasik örnektir.
- **"`enum` ile farkı ne?"** → `enum` sabit örnekler tanımlar. `sealed` hiyerarşi ise her alt tipe kendi alanlarını taşıma imkânı verir (`38`, `39`).

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sealed, record ve desen eslestirmeli switch birlikte tam kapsama verir">
  <defs>
    <marker id="dkmjava-seal-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Asıl değer üçlünün birlikte çalışmasında</text>
  <rect x="184" y="36" width="292" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="61" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">sealed interface Odeme permits Kart, Havale, Kapida</text>
  <line x1="330" y1="78" x2="330" y2="92" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjava-seal-ok)"/>
  <rect x="24" y="96" width="184" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="116" y="117" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">record Kart(String no)</text>
  <text x="116" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">final · değişmez taşıyıcı</text>
  <rect x="238" y="96" width="184" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="117" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">record Havale(String iban)</text>
  <text x="330" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">final · değişmez taşıyıcı</text>
  <rect x="452" y="96" width="184" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="544" y="117" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">record Kapida(int tutar)</text>
  <text x="544" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">final · değişmez taşıyıcı</text>
  <line x1="116" y1="150" x2="300" y2="172" stroke="#5D7789" stroke-width="1.2" marker-end="url(#dkmjava-seal-ok)"/>
  <line x1="330" y1="150" x2="330" y2="170" stroke="#5D7789" stroke-width="1.2" marker-end="url(#dkmjava-seal-ok)"/>
  <line x1="544" y1="150" x2="360" y2="172" stroke="#5D7789" stroke-width="1.2" marker-end="url(#dkmjava-seal-ok)"/>
  <rect x="150" y="180" width="360" height="66" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="330" y="201" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">switch (odeme) — desen eşleştirmesi</text>
  <text x="330" y="221" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">case Kart k · case Havale h · case Kapida x</text>
  <text x="330" y="239" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">default yazmaya gerek yok: liste kapalı</text>
  <rect x="14" y="258" width="316" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="279" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Yeni alt tip eklenince</text>
  <text x="172" y="299" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">derleyici switch'i eksik bulur ve durdurur</text>
  <text x="172" y="316" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata derleme zamanında görünür</text>
  <rect x="344" y="258" width="302" height="66" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="279" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">default konursa kazanç kaybolur</text>
  <text x="495" y="299" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni tip sessizce default dalına düşer</text>
  <text x="495" y="316" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata üretimde, çalışma anında çıkar</text>
</svg>
```

**Güçlendirilmiş cevap**

Aynı bilgiyi şu sırayla söyle. Önce tanım: "`sealed`, bir tipin hangi alt tipler tarafından genişletilebileceğini `permits` ile sınırlar; alt tipler `final`, `sealed` ya da `non-sealed` olmak zorundadır." Sonra asıl değer: "Kazanç yalnız kısıt değil; derleyici alt tiplerin tam listesini bildiği için desen eşleştirmeli `switch` tam kapsama denetimi yapabiliyor." Sonra somut sonuç: "`record` alt tiplerle birlikte kullanınca `default` yazmam gerekmiyor; yeni bir alt tip eklendiğinde derleyici beni durduruyor." Sonra kullanım yeri: "Ödeme türü ya da işlem sonucu gibi kapalı kümeler için biçilmiş kaftan." **En sona** sınırı koy: "Üretimde henüz kullanma fırsatım olmadı; kendi projemde denedim." Aynı dürüstlük, çok daha güçlü bir cevap (`38`, `39`, `37`).

### "Record nedir, DTO yerine kullanılabilir mi?"

**Soruldu / Verdiğin cevap**

> "Evet, kullanılabilir. Immutable veri taşıyıcı; constructor, accessor, equals, hashCode ve toString otomatik geliyor."

**Değerlendirme**

✅ **Yeterli.** Hem soruya net cevap vermişsin hem de derleyicinin ürettiklerini eksiksiz saymışsın. "Değişmez veri taşıyıcı" ifadesi tam yerinde. Bu cevapla mülakatçı `record` konusunu kapatabilir; nitekim dökümde bir sonraki soruya geçildi. Yine de iki bilinen tuzak söylenmedi.

**Takip sorusu riski**

- **"`record` içinde `List` varsa gerçekten değişmez mi?"** → Hayır. Değişmezlik sığdır. Alan referansı sabitlenir, listenin içeriği değişebilir. Kapatmak için sıkı kurucuda (compact constructor) kopya alınır ve okunur biçimde saklanır.
- **"JPA varlığı olarak kullanılır mı?"** → Uygun değildir. JPA parametresiz kurucu ve alan yazımı bekler; `record` ikisini de vermez. Kimlik karşılaştırması da farklıdır. `record` çıkış tarafında, yani projeksiyon ve yanıt nesnesi olarak parlar (`81`).
- **"Doğrulamayı nereye koyarsın?"** → Sıkı kurucuya. Orada boş alan, negatif tutar gibi kuralları kontrol eder, gerekirse istisna fırlatırım.
- **"`equals` neye göre çalışır?"** → Tüm bileşen alanlarına göre. Bu yüzden `record` bir sözlük anahtarı olarak rahatça kullanılabilir.

**Güçlendirilmiş cevap**

Cevabına iki cümle ekle. Birincisi sığ değişmezlik: "Değişmezliği sığdır; içinde `List` varsa referans sabitlenir ama içerik değişebilir, o yüzden sıkı kurucuda kopyalarım." İkincisi sınır: "Varlık sınıfı olarak kullanmam; JPA parametresiz kurucu ve alan yazımı bekler, `record` bunları vermez." Sonra kendi kullanım hattını çiz: "İstek ve yanıt nesnelerinde, sorgu projeksiyonlarında ve olay yükünde kullanırım." Kapanışta doğrulamanın yerini söyle: "Alan kurallarını sıkı kurucuda kontrol ederim." Bu ekler, `record`u yalnız kısayol olarak değil, bir tasarım aracı olarak kullandığını gösterir (`37`, `81`).

### "Parallel stream her zaman hızlı mıdır, thread-safe midir?"

**Soruldu / Verdiğin cevap**

> "Hayır, her zaman hızlı değil. Küçük işlerde paralelleştirme maliyeti kazançtan fazla olabilir. Ayrıca kodu otomatik thread-safe yapmaz."

**Değerlendirme**

✅ **Bu dosyanın en güçlü cevabı.** İki ayrı tuzağı birden görmüşsün. Çoğu aday birincisini söyler, ikincisini atlar. "Otomatik güvenli yapmaz" cümlesi burada ayrıştırıcıdır, çünkü paralel akışın en sık yapılan hatası tam olarak budur: paylaşılan bir listeye ya da sayaca akış içinden yazmak. Cevap kısa ama iki doğru noktaya birden basıyor. Övgü hak ediyor; bu cümleleri aynen koru.

**Takip sorusu riski**

- **"Paralel akış hangi havuzu kullanır?"** → Varsayılan olarak ortak ForkJoinPool'u. Bu havuz JVM genelinde paylaşılır ve iş parçacığı sayısı genellikle işlemci çekirdek sayısına bağlıdır.
- **"Bu neden tehlikeli?"** → Bloklayan iş koyarsan havuz dolar. Aynı havuzu kullanan diğer paralel akışlar da bekler. Yani tek bir yavaş HTTP çağrısı, ilgisiz kodu yavaşlatabilir.
- **"O hâlde ağ çağrısını nasıl paralelleştirirsin?"** → Kendi havuzumla. Bloklayan işi ayrı bir `ExecutorService` üzerinde çalıştırırım; ortak havuzu iş parçacığı bekletmek için kullanmam (`49`).
- **"Ne zaman gerçekten kazandırır?"** → Büyük veri kümesinde, işlemci yoğun ve yan etkisiz işlemde. Kaynağın kolay bölünebilmesi de şart; dizi ve `ArrayList` bölünür, bağlı liste kötü bölünür.
- **"Nasıl doğrularsın?"** → Ölçerek. Sıralı ve paralel sürümü aynı yük altında karşılaştırırım; tahminle karar vermem.

```svg
<svg viewBox="0 0 660 318" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Paralel akis: ne zaman kazandirir, ne zaman zarar verir ve ortak havuz riski">
  <defs>
    <marker id="dkmjava-par-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Paralel akış bir hız düğmesi değil, bir takastır</text>
  <rect x="14" y="36" width="316" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kazandırdığı yer</text>
  <text x="172" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">büyük veri kümesi, on binlerce eleman</text>
  <text x="172" y="101" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işlemci yoğun hesap, beklemesiz iş</text>
  <text x="172" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yan etkisiz işlem, paylaşılan durum yok</text>
  <text x="172" y="143" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kolay bölünen kaynak: dizi, ArrayList</text>
  <text x="172" y="168" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">karar tahminle değil ölçümle verilir</text>
  <rect x="344" y="36" width="302" height="152" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Zarar verdiği yer</text>
  <text x="495" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">küçük koleksiyon: bölme maliyeti ağır basar</text>
  <text x="495" y="101" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bloklayan iş: ağ çağrısı, disk, veri tabanı</text>
  <text x="495" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">paylaşılan değişken duruma yazma</text>
  <text x="495" y="143" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zor bölünen kaynak: bağlı liste, akarsu</text>
  <text x="495" y="168" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">paralel olmak güvenli olmak demek değildir</text>
  <rect x="14" y="200" width="632" height="104" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="221" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Ortak ForkJoinPool: JVM genelinde tek havuz</text>
  <rect x="40" y="234" width="150" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="115" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">rapor işi · paralel akış</text>
  <rect x="206" y="234" width="150" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="281" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arama işi · paralel akış</text>
  <line x1="360" y1="251" x2="376" y2="251" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjava-par-ok)"/>
  <rect x="380" y="234" width="240" height="34" rx="7" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="500" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bloklayan HTTP çağrısı havuzu doldurur</text>
  <text x="330" y="288" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Bir iş havuzu bekletince ilgisiz kod da yavaşlar; bloklayan iş kendi ExecutorService'ine verilir.</text>
</svg>
```

**Güçlendirilmiş cevap**

İki cümleni koru, üstüne mekanizmayı ekle. Önce havuzu adlandır: "Paralel akış varsayılan olarak ortak ForkJoinPool'u kullanır ve o havuz JVM genelinde paylaşılır." Sonra riski somutla: "Bloklayan bir çağrıyı paralel akışa koyarsam havuz dolar; aynı havuzu kullanan ilgisiz işler de bekler." Sonra çözümü ver: "Ağ ya da veri tabanı çağrılarını kendi `ExecutorService`'imde çalıştırırım." Ardından kazandığı yeri tarif et: "Paralel akışı büyük, işlemci yoğun ve yan etkisiz işlerde tercih ederim; kaynak da kolay bölünmeli." Kapanış cümlesi kararı yönteme bağlar: "Ve kararı ölçerek veririm; sıralı sürümle karşılaştırmadan paralel yazmam." Bu cevap seni "duymuş" adaydan "kullanmış" adaya taşır (`53`, `49`).

### Bu bölümden çıkan ders

Bu on bir cevabın ortak gücü tanım doğruluğu; ortak zayıflığı ise erken bitmek. Neredeyse her cevapta ilk cümle doğru, ikinci cümle eksik. Mülakatçı doyurucu bir cevap alınca durmuyor, bir üst basamağa çıkıyor; dökümde bu açıkça görülüyor. Bu yüzden hazırlık tanımda bitmez, zincirin bir sonraki halkasını da kapsar. Somut örüntü şu: `final` dedin ama `final List` ayrımını bırakmadın, encapsulation dedin ama setter tuzağını sen söylemedin, `sealed` dedin ama tam kapsamayı anmadın. Üç yerde de eksik bilgi değil, eksik **cümle** vardı. İki istisna öne çıkıyor: exception cevabı gerçekten güncelliğini yitirmiş, orada `try-with-resources` ve `cause` çalışılmalı. Paralel akış cevabı ise iki tuzağı birden gördüğü için bu dosyanın en güçlü anı. Ve en önemlisi sunum dersi: "Teorik olarak biliyorum" diye açmak, doğru bilgiyi bile zayıf duyurur. Dürüst ol, ama sınırı en sona koy.

# dkmjava-1 | Temel
## Soru
Bir sınıfta `private final List<String> roller = new ArrayList<>();` alanı var. Bu alan hakkında hangisi doğrudur?
## A
Alan `final` olduğu için listeye eleman eklenemez; `add` çağrısı derleme hatası verir
🤖 `final` yalnız referansı kilitler; listenin kendi metotları çalışmaya devam eder.
## B
`final` alan Java'da otomatik olarak değişmez bir kopya döndürür, dışarıya sızmaz
🤖 böyle bir davranış yoktur; getter neyi verirse o dışarı çıkar.
## C
Referans başka bir listeye bağlanamaz, ama listenin içeriği değiştirilebilir
🤖 Doğru: `final` bağı sabitler, içeriği değil.
## D
`final` alan yalnız kurucuda değil, sonradan her metotta yeniden atanabilir
🤖 `final` alan bir kez atanır; ikinci atama derlenmez.
## Doğru
C

## Açıklama
`final`, bir **bağı** sabitler. Alan bir kez atanır ve bir daha başka nesneye bağlanamaz. Ama bağlandığı nesnenin iç durumu üzerinde hiçbir söz söylemez. Bu yüzden `roller.add("ADMIN")` sorunsuz çalışır; `roller = yeniListe` ise derlenmez. Mülakatta en sık karıştırılan ayrım budur ve `final` sorusunun hemen ardından gelir. Gerçek değişmezlik (immutability) için birkaç şart birden gerekir: alanlar `private` ve `final` olmalı, koleksiyonlar kurucuda kopyalanmalı, getter dışarıya okunur bir görünüm vermeli, sınıf `final` olmalı (`42`). Yani `final` gerekli şarttır ama yeterli değildir. A şıkkı `final` etkisini nesnenin içine taşır. B şıkkı dilde olmayan bir davranış uydurur. D şıkkı `final` alanın tek atanma kuralını yok sayar. Aynı ayrım `record` için de geçerlidir: `record` alanları `final`dır ama içindeki liste hâlâ değiştirilebilir (`15`, `37`).

# dkmjava-2 | Temel
## Soru
Bir arayüzü lambda ile kullanabilmenin şartı nedir ve `@FunctionalInterface` anotasyonu ne yapar?
## A
Arayüzde tek soyut metot olmalı; anotasyon bu kuralı derleme zamanında denetler
🤖 Doğru: lambdanın hedefi tek soyut metotlu arayüzdür.
## B
Anotasyon zorunludur; onsuz tanımlanan bir arayüz lambda ile kullanılamaz
🤖 anotasyon isteğe bağlıdır; kural metot sayısından gelir.
## C
Arayüzde hiç `default` metot bulunmamalı; bir tane bile varsa lambda yazılamaz
🤖 `default` ve `static` metotlar soyut sayılmaz, sayıya girmezler.
## D
Anotasyon lambdayı ortak havuzda paralel çalıştırır ve çağrı maliyetini düşürür
🤖 anotasyonun çalışma anında hiçbir etkisi yoktur.
## Doğru
A

## Açıklama
Lambda bir **hedef tip** ister ve bu tip tek soyut metotlu bir arayüzdür. Sayıma yalnız soyut metotlar girer; `default` ve `static` metotlar hariçtir. `Comparator` bunun en iyi örneğidir: onlarca metodu vardır ama tek soyut metodu olduğu için lambda ile yazılabilir. `@FunctionalInterface` bu kuralın kendisini kurmaz, yalnız **denetler**. Anotasyonu koyduğun arayüze ikinci bir soyut metot eklenirse derleme hatası alırsın. Yani anotasyon niyeti belgeler ve kazayla bozulmayı engeller; koymazsan da lambda çalışır. B şıkkı bu ilişkiyi ters kurar. C şıkkı `default` metotları soyut sayar; oysa `default` metotlar arayüzlerin geriye dönük uyumu için eklenmiştir. D şıkkı anotasyona olmayan bir çalışma anı davranışı yükler; anotasyonun paralellikle hiçbir bağı yoktur. Pratikte çoğu ihtiyaç hazır arayüzlerle karşılanır: `Function`, `Supplier`, `Consumer`, `Predicate` (`32`, `33`).

# dkmjava-3 | Orta
## Soru
Bir servis metodunda `catch (Exception e) { throw new BusinessException("İşlem başarısız"); }` yazılmış ve dosya akışı `finally` içinde elle kapatılıyor. Buradaki iki temel kusur nedir?
## A
Tek kusur `Exception` yakalamaktır; `finally` ile kapatmak her koşulda doğru yaklaşımdır
🤖 kapatma da kusurlu; `finally` içindeki hata asıl hatayı bastırabilir.
## B
Kusur yoktur; özel istisna fırlatmak ve `finally` ile kapatmak önerilen kalıptır
🤖 özgün hata kayboluyor, kalıbın kendisi eksik kalıyor.
## C
Tek kusur mesajın Türkçe olmasıdır; istisna mesajları daima İngilizce yazılmalıdır
🤖 dil bir kusur değildir; asıl sorun kaybolan bilgidir.
## D
Özgün hata `cause` olarak taşınmıyor ve kapatma `try-with-resources` ile yapılmalı
🤖 Doğru: hem yığın izi kopuyor hem kaynak elle yönetiliyor.
## Doğru
D

## Açıklama
İki ayrı kusur var. Birincisi **bilgi kaybı**: yeni istisna fırlatılırken yakalanan `e` aktarılmıyor. Doğrusu `new BusinessException("İşlem başarısız", e)` yazmaktır. `cause` düştüğünde yığın izi kopar, kök neden görünmez olur ve hata ayıklama saatlere yayılır. İkincisi **kaynak yönetimi**: `finally` içinde elle kapatmak eski kalıptır. `try-with-resources` bloğu, `AutoCloseable` uygulayan kaynağı blok bitince kapatır ve kapatma sırasında oluşan hatayı asıl hatanın üstüne yazmaz, bastırılmış olarak taşır (`18`). `finally` içinde `close` çağrısı hata fırlatırsa asıl hata kaybolabilir; bu, gözden kaçan klasik bir tuzaktır. A şıkkı kusurlardan yalnız birini görür. B şıkkı ikisini de normal sayar. C şıkkı konuyu tamamen kaçırır. Cevabın devamında iki nokta daha söylenir: boş `catch` yazılmaz, çünkü hata yutulur; ele alınamayan istisna yukarı bırakılır ve web katmanında `@ControllerAdvice` ile tek yerde `ProblemDetail` olarak biçimlenir (`16`, `17`, `78`).

# dkmjava-4 | Orta
## Soru
`List<List<String>> gruplar` yapısında, tüm iç listelerdeki "A" ile başlayan adları tek listede toplayacaksın. Neden `map` değil `flatMap` gerekir?
## A
`flatMap` elemanları alfabetik sıraya sokar, `map` ise gelen sırayı bozar
🤖 ikisi de sıralama yapmaz; sıralama `sorted` işidir.
## B
`map` `Stream<Stream<String>>` üretir; `flatMap` iç akışları tek akışta düzler
🤖 Doğru: bir seviye azalır, filtre doğrudan `String` üzerinde çalışır.
## C
`map` yalnız sayısal tiplerde çalışır; metin listelerinde `flatMap` zorunludur
🤖 `map` her tipte çalışır, tip kısıtı yoktur.
## D
`flatMap` akışı paralel hâle getirir, iç içe yapılarda bu yüzden gereklidir
🤖 paralellik `parallel` ile gelir; `flatMap` ile ilgisi yoktur.
## Doğru
B

## Açıklama
`map` bire bir dönüşüm yapar: her elemana karşılık tek eleman üretir. Girdi `List<String>` olduğu için çıktı da akış içinde akış olur ve `filter` doğrudan `String` üzerinde çalışamaz. `flatMap` ise her elemanı bir akışa açar ve o akışları **tek akışta birleştirir**; sonuçta iç içelik bir seviye azalır. Çözüm zinciri şudur: `gruplar.stream().flatMap(List::stream).filter(a -> a.startsWith("A")).toList()`. Burada iki ayrıntı daha söylenmeye değer. Birincisi tembellik: `flatMap` ve `filter` ara işlemdir, hiçbir eleman `toList` gibi bir uç işlem gelene kadar akmaz; bu sayede zincir tek geçişte çalışır (`34`). İkincisi çıktı tipi: Java 16 ile gelen `toList` değiştirilemez bir liste döner, `Collectors.toList` ise böyle bir garanti vermez (`35`). A şıkkı sıralamayı yanlış operatöre bağlar. C şıkkı `map` için olmayan bir tip kısıtı uydurur. D şıkkı düzleştirme ile paralelliği karıştırır.

# dkmjava-5 | İleri
## Soru
`sealed interface Odeme permits Kart, Havale, Kapida` biçiminde bir hiyerarşi var; üç alt tip `record`. Bir `switch` deseni üç tipi de karşılıyor ve `default` dalı yok. Dördüncü bir alt tip `permits` listesine eklenirse ne olur?
## A
Kod derlenmez; `switch` artık tüm durumları karşılamadığı için derleme hatası alınır
🤖 Doğru: tam kapsama denetimi derleme zamanında yapılır.
## B
Kod derlenir; dördüncü tip geldiğinde ilgili dal bulunamaz ve sessizce atlanır
🤖 `default` yokken derleyici izin vermez; sessiz atlama olmaz.
## C
Kod derlenir ve eşleşmeyen tip için derleyici otomatik `null` sonucu üretir
🤖 derleyici böyle bir varsayılan üretmez.
## D
`sealed` yalnız belgeleme amaçlıdır; derleyicinin davranışını etkilemez
🤖 `permits` listesi derleyici tarafından zorlanır.
## Doğru
A

## Açıklama
`sealed` bildiriminin asıl kazancı burada görünür. `permits` listesi kapalı olduğu için derleyici alt tiplerin **tam listesini** bilir. Desen eşleştirmeli `switch` bu listeyle karşılaştırılır; hepsi karşılanmışsa `default` dalı gerekmez. Dördüncü tip eklendiği anda karşılanmayan bir durum doğar ve derleme durur. Bu, çalışma anında ortaya çıkacak bir hatanın derleme zamanına çekilmesidir; asıl değer budur (`38`, `39`). Bu yüzden böyle bir `switch`'e alışkanlıkla `default` koymak kazancı yok eder: yeni tip sessizce oraya düşer ve sorun üretimde görünür. `sealed` ile `enum` farkı da burada netleşir; `enum` sabit örnekler tanımlar, `sealed` hiyerarşide her alt tip kendi alanlarını taşır (`37`). Alt tiplerin `final`, `sealed` ya da `non-sealed` olma zorunluluğu da bu kapalılığı aşağı doğru taşır. B ve C şıkları derleyici denetimini çalışma anına erteler. D şıkkı `sealed` bildirimini yorum satırı sayar.

# dkmjava-6 | İleri
## Soru
Bir uç nokta, 20 elemanlı liste için her elemanda dış servise HTTP çağrısı yapıyor ve bu döngü `parallelStream()` ile yazılmış. Yük altında uygulamanın ilgisiz bölümleri de yavaşlıyor. En olası sebep nedir?
## A
`parallelStream` her çağrıda yeni bir iş parçacığı havuzu açar; havuz sayısı hızla artar
🤖 yeni havuz açılmaz; varsayılan olarak ortak havuz kullanılır.
## B
`parallelStream` başarısız çağrıları otomatik yeniden dener; istek sayısı katlanır
🤖 akışlarda yeniden deneme davranışı yoktur.
## C
Bloklayan çağrılar ortak ForkJoinPool'u dolduruyor; aynı havuzu kullanan her iş bekliyor
🤖 Doğru: bu havuz JVM genelinde paylaşılır.
## D
`parallelStream` sonuçları senkronize ettiği için liste erişimi tek tek sıraya giriyor
🤖 toplama adımı bu ölçekte darboğaz oluşturmaz.
## Doğru
C

## Açıklama
Paralel akış, aksi belirtilmedikçe **ortak ForkJoinPool** üzerinde çalışır. Bu havuz JVM genelinde tektir ve iş parçacığı sayısı genellikle çekirdek sayısına bağlıdır. Bloklayan bir iş, örneğin HTTP çağrısı, iş parçacığını hesap yapmadan bekletir. Yirmi çağrı havuzu doldurduğunda aynı havuzu kullanan ilgisiz paralel akışlar da sıraya girer; belirti "hiç dokunmadığımız yer neden yavaşladı" biçiminde gelir. Doğru çözüm bloklayan işi kendi havuzuna vermektir: ayrı bir `ExecutorService` ya da sanal iş parçacıklarına dayalı bir yürütücü (`49`). İkinci nokta ölçektir; 20 elemanlı bir listede paralelleştirmenin bölme ve toplama maliyeti kazancı zaten yer (`53`). Paralel akış büyük veri kümesinde, işlemci yoğun ve yan etkisiz işlerde kazandırır; kaynağın kolay bölünmesi de gerekir. A şıkkı havuz yaratımını yanlış tarif eder. B şıkkı akışlarda olmayan bir davranış varsayar. D şıkkı darboğazı yanlış adıma koyar.
