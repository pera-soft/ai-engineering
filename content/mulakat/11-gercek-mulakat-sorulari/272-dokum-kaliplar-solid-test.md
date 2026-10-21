---
slug: dokum-kaliplar-solid-test
baslik: "Gerçek Mülakat Dökümü · Tasarım Kalıpları, SOLID ve Test: Verilen Cevapların Değerlendirmesi"
kademe: Orta
sira: 272
etiketler: 11
---

## Konu anlatımı

Bu dosya tahmin değil, kayıt üzerinden çalışır. Gerçek bir teknik mülakatta sorulan altı soru ve o an verdiğin cevaplar burada. Her sorunun altında dört parça var: soru ve cevabın, dürüst bir değerlendirme, mülakatçının bir sonraki adımda soracağı şey, son olarak güçlendirilmiş cevap. Güçlendirme senin cevabını çöpe atmaz; üstüne ekler. Bu bölüm dökümün en iyi geçen bölümlerinden biri. Tanımlar doğru, terimler yerinde. Zayıf nokta tek bir yerde toplanıyor: cevaplar liste hâlinde bitiyor. Liste bilgiyi gösterir, muhakemeyi göstermez.

### "JUnit ve Mockito kullandın mı? Mock nesne ne demek?"

**Soruldu / verdiğin cevap**

> JUnit ile birim testi yazıyorum. Mockito ile bağımlılıkları mock'luyorum. `when(...).thenReturn(...)` ile sahte davranışı tanımlıyorum.

**Değerlendirme**

**✅ Yeterli.** Aracı doğru adlandırmış, kullanım biçimini de somut söylemişsin. `when(...).thenReturn(...)` kalıbını anmak cevabı ezberden ayırır. Eksik olan tek şey sınır: neyi mock'ladığın kadar neyi mock'lamadığın da ölçülür.

**Takip sorusu riski**

- **"`verify` ne işe yarar?"** → Çağrının gerçekten yapıldığını ve kaç kez yapıldığını doğrular. Geriye değer döndürmeyen metotlarda elindeki tek kanıt budur.
- **"Argüman yakalayıcı (ArgumentCaptor) ne zaman gerekir?"** → Mock'a giden nesnenin içeriğini görmek istediğinde. Çağrı yapıldı mı sorusunu, doğru veriyle mi yapıldı sorusuna çevirir.
- **"Neyi mock'lamazsın?"** → Test ettiğin sınıfı ve saf değer nesnelerini. Mock, sistem sınırı içindir: veri tabanı, dış servis, kuyruk, saat.
- **"`@Mock` ile `@MockBean` farkı nedir?"** → `@Mock` sade Mockito'dur, Spring bağlamı açılmaz ve hızlıdır. `@MockBean` bağlamdaki gerçek nesnenin yerine sahtesini koyar; bağlamı yeniden kurdurduğu için testi yavaşlatır (`90`).

**Güçlendirilmiş cevap**

Şunu da eklersen ayrışırsın: mock bir hız aracı değil, bir sınır aracıdır. Kendi iş kuralını mock'larsan testin ölçtüğü şey sahte davranış olur. Dış dünyayı mock'lar, iş kuralını gerçek çalıştırırsın. Doğrulamayı da ikiye ayır: dönüş değeri varsa sonucu, yoksa `verify` ile etkiyi denetlersin. Son cümle olarak bir tasarım gözlemi ekle: bir testte mock sayısı artıyorsa sorun testte değil, sınıfın çok fazla şey bilmesindedir (`187`). Testi önce yazma alışkanlığın varsa bunu da söyle; bu, tasarımı testin şekillendirdiğini gösterir (`261`).

```svg
<svg viewBox="0 0 660 296" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mock siniri: neyi mocklarsin, neyi mocklamazsin, nasil dogrularsin">
  <defs>
    <marker id="dkmpat-mock" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Mock bir hız aracı değil, sınır aracıdır</text>
  <rect x="14" y="36" width="196" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Mock'lanır: sistem sınırı</text>
  <text x="112" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veri tabanı erişimi (repository)</text>
  <text x="112" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dış servis istemcisi</text>
  <text x="112" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">mesaj kuyruğu yayımlayıcısı</text>
  <text x="112" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">saat, rastgelelik, dosya sistemi</text>
  <text x="112" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yavaş, kırılgan ya da denetimsiz olan</text>
  <line x1="212" y1="112" x2="228" y2="112" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmpat-mock)"/>
  <rect x="232" y="36" width="196" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Gerçek çalışır: iş kuralı</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test edilen sınıfın kendisi</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">saf değer nesneleri ve record</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hesaplama ve doğrulama kodu</text>
  <text x="330" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koleksiyon ve yardımcı sınıflar</text>
  <text x="330" y="168" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">testin asıl ölçtüğü şey burasıdır</text>
  <line x1="430" y1="112" x2="446" y2="112" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmpat-mock)"/>
  <rect x="450" y="36" width="196" height="152" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Doğrulama iki yoldan</text>
  <text x="548" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dönüş değeri varsa: sonucu denetle</text>
  <text x="548" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değer yoksa: verify ile çağrıyı</text>
  <text x="548" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">içerik gerekiyorsa: argüman yakala</text>
  <text x="548" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çağrılmamalıysa: never() ile</text>
  <text x="548" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">her mock'u doğrulamak testi çürütür</text>
  <rect x="14" y="200" width="632" height="82" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="220" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Uyarı işaretleri</text>
  <text x="330" y="240" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Test ettiğin sınıfı mock'lamak: testin ölçtüğü şey artık sahte davranıştır, kendi kodun değildir.</text>
  <text x="330" y="258" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Bir testte beş mock: sorun testte değil; sınıf çok fazla şeye bağlı, sorumluluğu dağılmış demektir.</text>
  <text x="330" y="275" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Her testi Spring bağlamıyla açmak: yavaşlar, dilim testi ya da sade birim testi tercih edilir.</text>
</svg>
```

### "Open-Closed Principle nedir?"

**Soruldu / verdiğin cevap**

> Geliştirmeye açık, değişikliğe kapalı demek. Yeni bir özellik gerektiğinde mevcut kodu değiştirmek yerine genişletmeye çalışırım.

**Değerlendirme**

**✅ Yeterli.** Tanım doğru ve "genişletmek" kelimesi tam yerinde kullanılmış. Cevabın eksiği içerik değil, kanıt: prensibi nasıl uyguladığın ve bedelinin ne olduğu söylenmemiş. Mülakatçı tanımı zaten biliyor; ölçtüğü şey nerede durduğunu bilip bilmediğin.

**Takip sorusu riski**

- **"Bunu kodda somut olarak nasıl yaparsın?"** → Değişen kısmı bir arayüzün arkasına alırsın. Yeni davranış yeni sınıf olarak eklenir; `switch` bloğuna dokunulmaz (`131`).
- **"OCP'nin bedeli nedir?"** → Her yeri önceden soyutlamak dolaylılık üretir ve okunabilirliği düşürür. Bu, gereksiz genelleme yasağının (YAGNI) ihlalidir.
- **"O zaman ne zaman soyutlarsın?"** → İkinci varyant geldiğinde. Bir varyantta soyutlama tahmin, iki varyantta gözlemdir.
- **"DIP ile ilişkisi nedir?"** → Genişleme noktası ancak üst seviye kod somut sınıfa değil soyutlamaya bağlıysa çalışır. OCP'nin hedefi, DIP onu tutan mekanizmadır.
- **"Prensibi ihlal ettiğini nasıl anlarsın?"** → Her yeni tipte aynı dosyayı açıyorsan. Değişiklik kaydı bunu açıkça gösterir.

**Güçlendirilmiş cevap**

Tanımdan sonra bir örnekle bağla: ödeme yöntemi eklenirken `if type == KREDI_KARTI` zincirine yeni dal yazmak yerine yeni bir sınıf eklersin. Kazanç iki tanedir. Birincisi çalışan kod dosyası hiç açılmaz, dolayısıyla var olan davranış bozulmaz. İkincisi her varyant kendi başına test edilir. Sonra bedeli söyle: bu prensip bedava değildir, her genişleme noktası bir dolaylılık katmanıdır. Bu yüzden soyutlamayı ikinci varyantta açarsın, ilkinde değil. Son cümle prensipleri bağlar: OCP hedefi tarif eder, bağımlılığı tersine çevirme onu ayakta tutar (`131`).

```svg
<svg viewBox="0 0 660 310" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Open-Closed: kosul zinciri yerine genisleme noktasi ve bedeli">
  <defs>
    <marker id="dkmpat-ocp" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Genişletmeye açık, değişikliğe kapalı — ama bedeli vardır</text>
  <rect x="14" y="36" width="286" height="158" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="157" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Değişikliğe açık: koşul zinciri</text>
  <rect x="34" y="70" width="246" height="30" rx="6" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="157" y="89" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">if kredi kartı … else if havale … else if cüzdan</text>
  <text x="157" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni yöntem = aynı dosyayı yeniden aç</text>
  <text x="157" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çalışan davranış her seferinde risk altında</text>
  <text x="157" y="160" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek sınıf, tek dev test, dallanma kalabalığı</text>
  <text x="157" y="182" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ihlalin işareti: her yeni tipte aynı dosya</text>
  <line x1="302" y1="112" x2="322" y2="112" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmpat-ocp)"/>
  <rect x="326" y="36" width="320" height="158" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="486" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Genişlemeye açık: soyutlama noktası</text>
  <rect x="346" y="70" width="280" height="28" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="486" y="88" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arayüz: OdemeYontemi.ode(tutar)</text>
  <rect x="346" y="106" width="88" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="390" y="123" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">KrediKarti</text>
  <rect x="442" y="106" width="88" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="486" y="123" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Havale</text>
  <rect x="538" y="106" width="88" height="26" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="582" y="123" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Cüzdan</text>
  <text x="486" y="152" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni yöntem = yeni sınıf; eski dosya açılmaz</text>
  <text x="486" y="172" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her varyant kendi başına test edilir</text>
  <rect x="14" y="206" width="632" height="90" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="226" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Bedeli söylenmeyen prensip reklam gibi duyulur</text>
  <text x="330" y="246" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Her genişleme noktası bir dolaylılık katmanıdır; akışı izlemek zorlaşır, dosya sayısı artar.</text>
  <text x="330" y="264" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kural: soyutlamayı ikinci varyant gelince aç. Bir varyantta soyutlama tahmindir, gözlem değildir.</text>
  <text x="330" y="284" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">Genişleme noktasını ayakta tutan şey DIP'tir: üst seviye kod somuta değil soyutlamaya bağlanır.</text>
</svg>
```

### "Strategy deseniyle çalıştın mı?"

**Soruldu / verdiğin cevap**

> Doğrudan adıyla uygulamadığım yerler olsa da, interface üzerinden farklı iş kurallarını ayrı sınıflarda uyguladık.

**Değerlendirme**

**✅ Yeterli — ve bu cevabın biçimi içeriğinden bile değerli.** Burada dürüst köprüyü doğru kurmuşsun: yapmadığın kısmı saklamadın, sonra en yakın gerçek deneyimi bağladın. Bu davranış mülakatta iki şey kazandırır. Birincisi güven: bilmediğini söyleyen adayın söylediklerine inanılır. İkincisi cevabın devamı: konuşma kapanmaz, kendi zeminine taşınır.

Cümlenin kurulumu da doğru. Sınırı önce, deneyimi sonra söylemişsin ve deneyimle bitirmişsin. Cevabın son cümlesi akılda kalır; oraya güçlü olanı koymak gerekir. Bunun tersi de dökümde görülüyor: "teorik olarak biliyorum" diye açılan cevaplar, arkasından doğru bilgi gelse bile zayıf duyuluyor. Aynı bilgi, sıra değişince farklı ağırlık kazanıyor (`256`).

Tek eksik şu: kurduğun yapının zaten Strategy olduğunu söylememişsin. Arayüz artı ayrı sınıflar artı çalışma anında seçim, bu desenin tanımıdır. Adı koymak cevabı bir basamak yukarı taşırdı.

**Takip sorusu riski**

- **"Spring'de doğru stratejiyi nasıl seçersin?"** → `Map<String, Strategy>` enjekte edersin; Spring anahtar olarak bean adını koyar. Alternatifi `List<Strategy>` ve her stratejide bir `supports(...)` metodudur (`135`).
- **"Strategy yerine lambda kullanılır mı?"** → Arayüz tek metotluysa evet. Durumsuz ve kısa kurallarda sınıf açmaya gerek yoktur.
- **"Bu, if-else'i başka yere taşımak değil mi?"** → Seçim tek bir yerde toplanırsa değil. Çağıran tarafta hâlâ tip kontrolü yapıyorsan desen kurulmamıştır.
- **"Strategy ile State farkı nedir?"** → Stratejiyi dışarıdaki kod seçer ve akış sabittir. State'te nesne kendi durumunu ve bir sonraki geçişi kendi yönetir.

**Güçlendirilmiş cevap**

Cevabın yapısını koru, yalnız adı ekle: "Adını koyarak uygulamadım ama kurduğumuz yapı Strategy'nin ta kendisiydi; ortak bir arayüz, iş kuralı başına bir sınıf ve çalışma anında seçim." Sonra kazancı iki maddede ver: yeni kural yeni sınıf olarak gelir, mevcut dosya açılmaz; her kural ayrı test edilir. Ardından bir uygulama ayrıntısı ekle: Spring'de stratejileri `Map` olarak enjekte edip anahtarla seçmek yaygın yoldur. Kapanışta sınırı söyle: seçim mantığı çağırana sızarsa desen bozulur, çünkü koşul zinciri yalnız yer değiştirmiş olur.

### "Başka hangi kalıpları kullandın? Factory kullandın mı?"

**Soruldu / verdiğin cevap**

> Singleton ve Factory kullandım. Spring'in içinde de dolaylı olarak bunları kullanıyoruz.

**Değerlendirme**

**✅ Yeterli.** İki kalıbı doğru adlandırmış ve Spring bağını kurmuşsun; "dolaylı olarak" ifadesi doğru bir sezgi. Ama cevap liste hâlinde bitiyor. Mülakatçı burada kalıbın adını değil, hangi problemi çözdüğünü duymak ister. Bir cümlelik bir bağlam bu cevabı iki katına çıkarır.

**Takip sorusu riski**

- **"Spring'in singleton'ı, Singleton deseni midir?"** → Aynı şey değil. Spring'in yönettiği nesne (bean) varsayılan olarak bağlam başına tektir. Klasik desende sınıf kendi örneğini kendi tutar ve dışarıdan değiştirilemez.
- **"Singleton deseni neden eleştirilir?"** → Küresel durum üretir, bağımlılığı gizler ve testte yerine sahte koymayı zorlaştırır. Bağımlılık enjeksiyonu aynı ihtiyacı sorunsuz karşılar (`133`).
- **"Builder kullandın mı?"** → Çok parametreli ve isteğe bağlı alanı olan nesnelerde. Okunabilirlik ve değişmezlik kazandırır.
- **"Spring'in içinde başka hangi kalıplar var?"** → Fabrika (`BeanFactory`), vekil nesne (proxy), şablon metot (`JdbcTemplate` gibi) ve gözlemci (olay yayını).

**Güçlendirilmiş cevap**

Listeyi problemle eşle: "Factory'yi, çağıran tarafın hangi somut sınıfı kuracağını bilmesini istemediğimde kullanıyorum. Singleton'ı ise Spring hallettiği için kendim yazmıyorum." Bu iki cümle bilgi listesini karar anlatısına çevirir. Sonra Spring bağını netleştir: `ApplicationContext` bir fabrikadır, `@Bean` yazdığın metot da bir fabrika metodudur. Kalıbı bilmeden kullanıyor olmak buradan gelir. Kapanışta bir sınır cümlesi ekle: kalıp bir çözümdür, önce çözdüğü problem ortaya çıkmalıdır. Problemi olmayan yere kalıp koymak karmaşıklığı bedava artırır (`134`).

### "Factory ile Strategy arasındaki fark nedir?"

**Soruldu / verdiğin cevap**

> Factory nesne yaratmaya odaklanır, Strategy davranış seçmeye. Factory uygun strategy'yi üretebilir de.

**Değerlendirme**

**✅ Güçlü cevap — bu dosyanın en iyi cevabı.** İki şeyi birden yapmışsın. Önce ayrımı doğru eksende kurmuşsun: yaratma karşısında davranış seçme. Sonra ilişkiyi de söylemişsin. Çoğu aday ilk kısmı bilir, ikinci kısımda takılır; iki kalıbı ya karıştırır ya da tamamen ayrı iki dünya sanır. Senin cümlen ikisinin birlikte çalıştığını gösteriyor, bu da kalıpları ezberden değil kullanımdan bildiğini duyurur.

**Takip sorusu riski**

- **"Factory Method ile Abstract Factory farkı nedir?"** → Factory Method tek bir ürün üretir; hangi somut tipin geleceğine alt sınıf karar verir. Abstract Factory birbiriyle uyumlu bir ürün ailesini birlikte üretir.
- **"Spring'in kendisi bir fabrika mıdır?"** → Evet. `BeanFactory` adı bunu açıkça söyler; nesne yaratmayı ve yaşam döngüsünü üstlenir (`133`).
- **"Fabrika olmadan strateji seçilebilir mi?"** → Evet. Spring'de stratejileri `Map` olarak enjekte etmek zaten fabrika işini görür.
- **"İkisini birleştiren örnek verir misin?"** → Ödeme yöntemi kodunu alan bir fabrika, o koda karşılık gelen ödeme stratejisini döndürür. Çağıran ne somut sınıfı bilir ne de koşul yazar.

**Güçlendirilmiş cevap**

Cevabına tek bir örnek ekle, gerisi zaten yerinde: "Fabrika `hangi nesne` sorusunu, strateji `hangi davranış` sorusunu yanıtlar. Ödeme akışında fabrika yöntem koduna göre stratejiyi döndürür, çağıran hiçbir somut sınıfı görmez." Ardından sınırı söyle: fabrika yalnız yaratmayı saklar, davranış farkını taşımaz. İki ayrı sorumluluk olduğu için ikisi birbirinin yerine geçmez. Kapanışta Spring bağını kur: bağımlılık enjeksiyonu kullanan bir projede fabrikayı çoğu zaman kendin yazmazsın, çünkü bağlam zaten bir fabrikadır (`133`, `135`).

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Factory ile Strategy ayrimi ve birlikte calismasi">
  <defs>
    <marker id="dkmpat-fs" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fabrika "hangi nesne", strateji "hangi davranış" sorusunu yanıtlar</text>
  <rect x="14" y="36" width="310" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Factory — yaratma sorumluluğu</text>
  <text x="169" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">amaç: somut sınıf adını çağırandan saklamak</text>
  <text x="169" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">soru: bu girdiye karşılık hangi nesne kurulmalı?</text>
  <text x="169" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değişen şey: kurulacak tip</text>
  <text x="169" y="146" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Spring'de ApplicationContext ve @Bean metodu</text>
  <text x="169" y="162" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bu işi zaten üstlenir</text>
  <rect x="336" y="36" width="310" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Strategy — davranış sorumluluğu</text>
  <text x="491" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">amaç: aynı işi farklı kuralla yaptırmak</text>
  <text x="491" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">soru: bu iş hangi kuralla yürütülmeli?</text>
  <text x="491" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değişen şey: algoritmanın kendisi</text>
  <text x="491" y="146" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ortak arayüz, kural başına bir sınıf,</text>
  <text x="491" y="162" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">seçim çalışma anında yapılır</text>
  <rect x="14" y="182" width="632" height="60" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Birlikte çalışırlar: fabrika uygun stratejiyi döndürür</text>
  <rect x="34" y="210" width="126" height="24" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="97" y="226" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">çağıran: yöntem kodu</text>
  <line x1="162" y1="222" x2="182" y2="222" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmpat-fs)"/>
  <rect x="186" y="210" width="126" height="24" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="249" y="226" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">fabrika: seçer ve kurar</text>
  <line x1="314" y1="222" x2="334" y2="222" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmpat-fs)"/>
  <rect x="338" y="210" width="126" height="24" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="401" y="226" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">strateji: işi yürütür</text>
  <line x1="466" y1="222" x2="486" y2="222" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmpat-fs)"/>
  <rect x="490" y="210" width="136" height="24" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <text x="558" y="226" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">çağıran somut tipi görmez</text>
  <rect x="14" y="254" width="632" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="274" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Karıştırmanın işareti</text>
  <text x="330" y="294" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Fabrika davranış farkını taşımaz, strateji nesne kurmaz; seçim çağırana sızarsa ikisi de bozulur.</text>
</svg>
```

### "Bu yapıları kurarken hangi araçları kullanırsın?"

**Soruldu / verdiğin cevap**

> Interface, abstract class, kalıtım, polimorfizm ve dependency injection kullanıyorum.

**Değerlendirme**

**✅ Yeterli.** Araçların hepsi doğru ve eksiksiz sayılmış. Zayıf yan aynı: bu bir liste, tercih ölçütü yok. Mülakatçı listeyi duyduğu anda ölçüt sorar; en olası soru kalıtım ile bileşim arasındaki seçimdir. Cevabına tek bir tercih cümlesi eklemek listeyi yaklaşıma çevirir.

**Takip sorusu riski**

- **"Kalıtım mı bileşim mi, hangisini seçersin?"** → Varsayılan bileşimdir. Kalıtım yalnız gerçek bir "bir türüdür" ilişkisi varsa kullanılır (`134`).
- **"Neden bileşim varsayılan?"** → Kalıtım en sıkı bağdır. Üst sınıf değiştiğinde tüm alt sınıflar kırılabilir; buna kırılgan taban sınıf sorunu denir. Bileşim ise davranışı çalışma anında değiştirmene izin verir.
- **"Abstract class'ı ne zaman arayüze tercih edersin?"** → Alt sınıflar ortak durum ve ortak kod paylaşıyorsa. Yalnız sözleşme gerekiyorsa arayüz yeterlidir.
- **"Bu listede DI'ın rolü nedir?"** → Genişleme noktasını dışarıdan doldurur. Soyutlamayı kimin gerçekleştireceği kararı sınıfın içinden çıkar (`131`).

**Güçlendirilmiş cevap**

Listeyi bir kuralla kapat: "Bu araçların arasında varsayılan tercihim bileşimdir; kalıtımı yalnız gerçekten bir tür ilişkisi varsa kullanırım." Sonra nedenini tek cümlede ver: kalıtım en sıkı bağdır ve üst sınıftaki değişiklik alt sınıfları sessizce kırar. Ardından her aracı işine bağla: arayüz sözleşmeyi, soyut sınıf ortak kodu, polimorfizm koşul zincirinin kaldırılmasını, bağımlılık enjeksiyonu ise bağlamayı sağlar. Kapanışta ölçütü söyle: hangi aracı seçtiğini değil, neyin değişmesini beklediğini anlat. Kalıpların tamamı değişecek olan kısmı yalıtma çabasıdır.

### Bu bölümden çıkan ders

Bu bölüm dökümün güçlü tarafını gösteriyor: altı sorunun altısında da tanım doğru, terim yerinde ve cümleler kısa. Ayrıca burada bir davranış ödüllendirilmeli. Strategy sorusunda yapmadığın şeyi saklamayıp en yakın gerçek deneyimi bağlaman, mülakatta uydurmaktan da susmaktan da güçlüdür. Ortak zayıflık ise tek bir yerde toplanıyor: cevaplar liste hâlinde bitiyor. Kalıp adı, prensip tanımı ve araç listesi bilgiyi kanıtlar ama kararı göstermez. Her cevabın sonuna bir cümle daha eklemek bunu düzeltir. O cümle ya bir örnek olmalı, ya bir bedel, ya da bir tercih ölçütü. Mülakatçı ne bildiğini ilk yarım dakikada anlıyor; kalan sürede nasıl karar verdiğini ölçüyor.

# dkmpat-1 | Temel
## Soru
Bir servis sınıfının birim testi yazılıyor. Sınıf; bir repository, bir dış servis istemcisi ve bir indirim hesaplama kuralı kullanıyor. Mockito ile neyin mock'lanması doğrudur?
## A
Test edilen servis sınıfı da dahil hepsi mock'lanmalı; böylece test dış etkenlerden tamamen arınır
🤖 test edilen sınıf mock'lanırsa test kendi kodunu değil sahte davranışı ölçer.
## B
Repository ve dış servis istemcisi mock'lanır; indirim hesabı ve test edilen sınıf gerçek çalışır
🤖 Doğru: mock sistem sınırında kullanılır, iş kuralı gerçek çalıştırılır.
## C
Yalnız dış servis istemcisi mock'lanır; veri tabanına giden çağrılar birim testte de gerçek yapılmalıdır
🤖 gerçek veri tabanı testi yavaşlatır ve birim testi tümleşik teste çevirir.
## D
Hiçbiri mock'lanmaz; tüm bağımlılıklar gerçek nesnelerle kurulursa test daha güvenilir olur
🤖 bu bir tümleşik testtir; birim testin hızı ve hata yalıtımı kaybolur.
## Doğru
B
## Açıklama
Mock bir hız aracı değil, **sınır aracıdır**. Yavaş, kırılgan ya da denetlenemeyen şeylerin yerine konur: veri tabanı erişimi, dış servis, kuyruk, saat. Test edilen sınıf ve saf iş kuralı gerçek çalışmalıdır, çünkü testin ölçtüğü şey odur. A şıkkı en sık yapılan hatadır; test edilen sınıfı mock'larsan sahte davranışı doğrulamış olursun. C şıkkı veri tabanını birim testin içine sokar; bu testi yavaşlatır ve hatanın nerede olduğunu bulanıklaştırır. D şıkkı tümleşik testi tarif eder; o testin de yeri vardır ama piramidin farklı katındadır (`187`). Doğrulamayı ikiye ayır: dönüş değeri varsa sonucu denetlersin, yoksa `verify` ile çağrının yapıldığını doğrularsın. Argümanın içeriği önemliyse argüman yakalayıcı kullanılır. Son olarak bir tasarım işareti: bir testte mock sayısı sürekli artıyorsa sorun testte değil, sınıfın çok fazla şeye bağlı olmasındadır (`90`, `261`).

# dkmpat-2 | Orta
## Soru
Bir ekip, Open-Closed Principle'a uymak için projedeki her sınıfın arkasına önceden birer arayüz koydu. Sınıfların çoğunun tek gerçeklemesi var. Bu yaklaşımın temel sorunu nedir?
## A
Arayüz sayısı arttığı için uygulamanın çalışma anı başarımı belirgin biçimde düşer
🤖 asıl maliyet başarımda değil, okunabilirlik ve bakımdadır.
## B
Sorun yok; OCP zaten her sınıfın bir arayüz arkasına alınmasını gerektirir
🤖 OCP genişleme noktası ister, her yere soyutlama koymayı değil.
## C
Arayüzler yerine soyut sınıf kullanılmalıydı; soyut sınıf her durumda daha esnektir
🤖 soyut sınıf da bir tercihtir; sorun aracın türü değil, gereksiz soyutlama.
## D
Tek gerçeklemesi olan soyutlama gereksiz dolaylılıktır; genişleme noktası varyant görülünce açılır
🤖 Doğru: soyutlama gözleme dayanır, tahmine değil.
## Doğru
D
## Açıklama
OCP, kodun genişletilerek büyümesini ister: yeni davranış yeni sınıf olarak gelir, çalışan dosya açılmaz. Ama prensip bedava değildir. Her genişleme noktası bir **dolaylılık katmanıdır**; akışı izlemek zorlaşır, dosya sayısı artar ve hata ayıklama yavaşlar. Tek gerçeklemesi olan arayüz bu bedeli öder ama karşılığında hiçbir esneklik vermez. Sağlıklı kural şudur: soyutlamayı ikinci varyant geldiğinde aç. Bir varyantta soyutlama bir tahmindir; iki varyantta ise gözleme dayanır. Ayrıca yanlış yerde açılan soyutlama, gerçek varyant geldiğinde çoğu zaman uymaz ve yeniden yazılır. A şıkkı maliyeti yanlış yere koyar; sanal çağrının bedeli bu tartışmanın konusu değildir. C şıkkı aracı değiştirir, sorunu değiştirmez. B şıkkı prensibi kuralın kendisi sanır. Cevapta prensibin ölçütünü de söyle: genişleme noktası, üst seviye kodun somuta değil soyutlamaya bağlanmasıyla ayakta durur (`131`).

# dkmpat-3 | Orta
## Soru
Spring Boot uygulamasında altı farklı ödeme yöntemi var ve her biri `OdemeStratejisi` arayüzünü gerçekleyen ayrı bir sınıf. Çalışma anında doğru stratejiyi seçmenin en yerinde yolu hangisidir?
## A
Stratejileri `Map<String, OdemeStratejisi>` olarak enjekte edip yöntem koduyla haritadan seçmek
🤖 Doğru: Spring haritayı bean adlarıyla doldurur, seçim tek satıra iner.
## B
Servis içinde `switch` bloğu yazıp her dalda ilgili strateji sınıfını `new` ile kurmak
🤖 hem koşul zinciri geri gelir hem de bean yönetimi devre dışı kalır.
## C
Her strateji sınıfına statik bir örnek alanı koyup çağıran tarafın bu alana erişmesini sağlamak
🤖 küresel duruma döner, testte yerine sahte koymak zorlaşır.
## D
Tek bir strateji sınıfı bırakıp yöntem farklarını o sınıf içinde bayrak alanlarıyla yönetmek
🤖 altı kuralı tek sınıfa toplar; deseni tamamen ortadan kaldırır.
## Doğru
A
## Açıklama
Spring, bir arayüzü gerçekleyen tüm nesneleri (bean) bir `Map` ya da `List` içine kendisi doldurur. `Map<String, OdemeStratejisi>` enjekte ettiğinde anahtar olarak bean adı gelir; yöntem kodunu anahtar yapmak seçimi tek satıra indirir. Alternatif yol `List<OdemeStratejisi>` enjekte edip her stratejiye bir `supports(...)` metodu koymaktır; anahtar yerine kuralın kendisi karar verir (`135`). B şıkkı iki hata yapar: koşul zincirini geri getirir ve `new` ile kurduğu nesneyi Spring'in yönetiminden çıkarır. Böylece o stratejiye bağımlılık enjekte edilemez. C şıkkı klasik Singleton yaklaşımına döner; küresel durum üretir ve testte yerine sahte koymayı zorlaştırır (`133`). D şıkkı deseni tamamen iptal eder. Sözlü cevapta bir sınır cümlesi de ekle: seçim mantığı tek yerde toplanmalıdır. Çağıran tarafta hâlâ tip kontrolü varsa desen kurulmamış, koşul zinciri yalnız taşınmıştır.

# dkmpat-4 | Orta
## Soru
Bir projede yüzlerce birim testi var ve test süresi çok uzun. İnceleme sonucunda testlerin neredeyse tamamının bağımlılıkları `@MockBean` ile değiştirdiği görülüyor. En yerinde teşhis hangisidir?
## A
`@MockBean` yavaş çalışan bir kütüphanedir; `@Mock` aynı işi yapan daha hızlı bir alternatiftir
🤖 ikisi farklı iş yapar; fark kütüphane hızında değil, bağlam kurulumunda.
## B
Sorun mock sayısındadır; mock yerine gerçek bağımlılıklar kullanılırsa testler hızlanır
🤖 gerçek bağımlılık genellikle daha yavaştır; teşhis ters yöndedir.
## C
`@MockBean` Spring bağlamını gerektirir ve tanım değiştikçe bağlam yeniden kurulur; bağlam gerekmeyen testler `@Mock` ile yazılmalı
🤖 Doğru: maliyet mock'ta değil, yeniden kurulan bağlamdadır.
## D
Test sayısı fazladır; testlerin bir kısmı silinerek toplam süre kabul edilebilir seviyeye indirilmelidir
🤖 test silmek süreyi düşürür ama güvenceyi de düşürür; teşhis değildir.
## Doğru
C
## Açıklama
İki anotasyon farklı katmanlarda çalışır. `@Mock` sade Mockito'dur: Spring bağlamı hiç açılmaz, nesneyi kendin kurar ve bağımlılıkları elinle verirsin. Çok hızlıdır ve saf iş kuralı testleri için doğru araçtır. `@MockBean` ise Spring bağlamındaki gerçek nesnenin yerine sahtesini koyar. Bunun bedeli şudur: mock kümesi değiştikçe Spring **bağlamı yeniden kurulur** ve önbelleğe alınmış bağlamdan yararlanılamaz. Yüzlerce testte bu, sürenin baskın kalemi hâline gelir. Doğru düzen katmanlıdır: iş kuralı testleri sade birim testi olur, Spring'e ihtiyaç duyan testler dilim testi olarak yazılır, tümleşik testler ise sayıca az tutulur (`90`, `187`). A şıkkı farkı kütüphane hızı sanır. B şıkkı teşhisi tersine çevirir; gerçek bağımlılık çoğunlukla daha yavaştır. D şıkkı belirtiyi susturur, nedeni bırakır. Yeni Spring Boot sürümlerinde aynı iş için `@MockitoBean` adı kullanılır; davranış ve maliyet mantığı aynıdır.

# dkmpat-5 | İleri
## Soru
Bir ekip, "fabrika kalıbını kullandık, artık strateji kalıbına gerek yok" diyor. Fabrikaları yöntem koduna göre farklı sınıflar üretiyor ama bu sınıfların hepsi aynı arayüzün farklı hesaplama kurallarını taşıyor. Bu ifade neden hatalıdır?
## A
Hatalı değildir; fabrika nesne ürettiği için davranış seçimi de kendiliğinden fabrikanın işidir
🤖 fabrika hangi nesne sorusunu yanıtlar, hangi davranış sorusunu değil.
## B
Hatalıdır; fabrika kalıbı yalnız Abstract Factory biçiminde kullanılabilir, tek ürün üretemez
🤖 Factory Method tek ürün üretir; ifade yanlış bir kısıt koyuyor.
## C
Hatalıdır; strateji kalıbı yalnız lambda ile yazılabilir, sınıf kullanan çözüm strateji sayılmaz
🤖 strateji sınıfla da lambda ile de yazılır; biçim kalıbı belirlemez.
## D
Hatalıdır; ekip zaten iki kalıbı birlikte kullanıyor — fabrika nesneyi kurar, farklı kurallar strateji kalıbıdır
🤖 Doğru: iki sorumluluk ayrıdır ve burada birlikte çalışmaktadır.
## Doğru
D
## Açıklama
İki kalıbın ekseni farklıdır. Fabrika **hangi nesne** sorusunu yanıtlar; amacı somut sınıf adını çağırandan saklamaktır. Strateji **hangi davranış** sorusunu yanıtlar; aynı işin farklı kurallarla yürütülmesini sağlar. Ekip aynı arayüzü gerçekleyen farklı hesaplama kuralları yazdığı anda strateji kalıbını kurmuştur; fabrika yalnız hangisinin kurulacağına karar verir. Yani biri diğerinin yerine geçmez, ikisi arka arkaya çalışır: çağıran yöntem kodunu verir, fabrika uygun stratejiyi döndürür, strateji işi yürütür. Bu ilişkiyi söyleyebilmek mülakatta ayrıştırıcıdır. Ayrım derinleşirse iki nokta daha gelir. Factory Method tek bir ürün üretir ve somut tipe alt sınıf karar verir; Abstract Factory ise birbiriyle uyumlu bir ürün ailesini birlikte üretir. İkincisi, bağımlılık enjeksiyonu kullanan bir projede fabrikayı çoğu zaman kendin yazmazsın: `ApplicationContext` zaten bir fabrikadır ve `@Bean` yazdığın metot bir fabrika metodudur (`133`, `135`).

# dkmpat-6 | İleri
## Soru
Bir rapor dışa aktarma sınıfına PDF, Excel ve CSV desteği eklenecek. Bir geliştirici, ortak bir soyut sınıf yazıp her biçim için ondan türetmeyi öneriyor. Hangi değerlendirme daha yerindedir?
## A
Öneri doğrudur; kalıtım her zaman bileşimden daha az kod üretir ve bakımı kolaylaştırır
🤖 kalıtım en sıkı bağdır; az kod ile kolay bakım aynı şey değildir.
## B
Varsayılan tercih bileşim olmalı; biçimler bir arayüzün ayrı gerçeklemeleridir, ortak kod çıkarsa soyut sınıf sonra eklenir
🤖 Doğru: kalıtım gerçek bir "bir türüdür" ilişkisi varsa kullanılır.
## C
İkisi de yanlıştır; biçim farkı tek sınıf içinde bayrak alanlarıyla yönetilmelidir
🤖 üç kuralı tek sınıfa toplar; koşul zinciri ve sıkı bağ geri gelir.
## D
Öneri doğrudur ama soyut sınıf yerine somut bir üst sınıf kullanılmalı, alt sınıflar gerekli metotları ezmelidir
🤖 somut üst sınıf kalıtımı daha da kırılganlaştırır; varsayılan davranış sessizce sızar.
## Doğru
B
## Açıklama
Varsayılan tercih **bileşimdir**; kalıtım yalnız gerçek bir "bir türüdür" ilişkisi varsa kullanılır. Nedeni bağın sıkılığıdır: alt sınıf üst sınıfın gerçeklemesine bağlanır, üst sınıftaki bir değişiklik tüm alt sınıfları sessizce kırabilir. Bunun adı kırılgan taban sınıf sorunudur. Rapor biçimleri örneğinde asıl ihtiyaç ortak kod paylaşmak değil, aynı işi farklı biçimlerde yapmaktır. Bu yüzden doğru yapı bir `RaporDisaAktarici` arayüzü ve biçim başına bir gerçeklemedir; bu aynı zamanda strateji kalıbının kendisidir (`135`). Gerçekten paylaşılan bir kod ortaya çıkarsa soyut sınıf sonradan da eklenebilir; bu karar erken verilmez. A şıkkı kalıtımı mutlaklaştırır ve kod satırını ölçüt sayar. C şıkkı üç kuralı tek sınıfa toplayarak koşul zincirini geri getirir. D şıkkı en riskli seçenektir; somut üst sınıftan türemek varsayılan davranışın alt sınıflara sızmasına yol açar. Sözlü cevapta ölçütü söyle: hangi aracı seçtiğini değil, neyin değişmesini beklediğini anlat (`131`, `134`).
