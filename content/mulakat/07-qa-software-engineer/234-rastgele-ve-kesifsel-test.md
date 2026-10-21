---
slug: rastgele-ve-kesifsel-test
baslik: "Rastgele ve Keşifsel Test: Üç Farklı 'Random', Test Verisi ve Yeniden Üretilebilirlik"
kademe: Orta
sira: 234
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir arabanın muayeneden geçmesi ile o arabayla yola çıkmak farklı iki şeydir. Muayene istasyonunda aynı 40 maddelik kontrol listesi her araca aynı sırayla uygulanır. Bu liste **sonucu belirli (deterministic)**, tekrarlanabilir ve denetlenebilirdir; ama listede olmayan hiçbir şeyi bulmaz. Deneyimli bir sürücü aynı arabayı alıp yola çıktığında ise listede olmayanı bulur: "Şu virajda direksiyon hafif titriyor."

Test otomasyonu muayene listesidir; **bildiğin** hatanın geri gelmediğini korur. Keşifsel test ise yola çıkmaktır; **bilmediğin** hatayı bulur. Bu iki cümle, konunun tamamının özetidir ve mülakatta tek başına belirleyici olabilir.

İşin can sıkıcı tarafı şudur: mülakatta size "random test yapıyor musunuz?" diye sorulduğunda karşınızdaki kişinin kastettiği şey **üç ayrı konudan biri** olabilir. Üstelik bu üçü birbirinden bağımsızdır. (1) Rastgele/keşifsel bir **test yaklaşımı**: keşifsel test, maymun testi, fuzzing. (2) Testlerin kullandığı **rastgele test verisi**: Faker ile üretilen isim, e-posta, adres. (3) Testlerin her koşumda farklı sırada çalıştırılması, yani **rastgele test sırası (random order)**.

Bu yüzden mülakattaki en iyi ilk hamle cevap vermek değil, ayrım yapmaktır: "Üç anlamı da var — hangisini kastediyorsunuz? Üçünü de kullanıyoruz ama farklı gerekçelerle." Bu cümle, konuyu ezberlemiş biriyle anlamış biri arasındaki farkı anında gösterir.

```svg
<svg viewBox="0 0 660 282" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Random teriminin üç ayrı anlamı: test yaklaşımı, test verisi ve test sırası">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"Random test" üç ayrı şey demek olabilir</text>
  <text x="330" y="45" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">üçü bağımsız konudur; biri diğerinin yerini tutmaz</text>

  <rect x="18" y="58" width="196" height="192" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="116" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1) TEST YAKLAŞIMI</text>
  <text x="116" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">keşifsel (exploratory) test</text>
  <text x="116" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ad-hoc, gerilla, hata avı</text>
  <text x="116" y="138" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">maymun testi, fuzzing</text>
  <text x="116" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">senaryo önceden yazılmaz</text>
  <text x="116" y="181" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ama başıboş değildir</text>
  <text x="116" y="198" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">oturum + görev beyanı</text>
  <text x="116" y="224" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">bilinmeyen hatayı bulur</text>
  <text x="116" y="241" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yapan: insan</text>

  <rect x="232" y="58" width="196" height="192" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="330" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">2) TEST VERİSİ</text>
  <text x="330" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Faker / Datafaker</text>
  <text x="330" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">UUID, zaman damgası</text>
  <text x="330" y="138" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">özellik tabanlı test</text>
  <text x="330" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aynı test, farklı girdi</text>
  <text x="330" y="181" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sınır değerleri rastgele</text>
  <text x="330" y="198" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">seçilmez, elle seçilir</text>
  <text x="330" y="224" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">tohum yoksa hata kaybolur</text>
  <text x="330" y="241" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yapan: test kodu</text>

  <rect x="446" y="58" width="196" height="192" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="544" y="82" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">3) TEST SIRASI</text>
  <text x="544" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her koşumda farklı sıra</text>
  <text x="544" y="120" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">JUnit 5 MethodOrderer</text>
  <text x="544" y="138" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">worker dağılımı</text>
  <text x="544" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gizli bağımlılığı</text>
  <text x="544" y="181" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">açığa çıkarır</text>
  <text x="544" y="198" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">flaky üretmez, gösterir</text>
  <text x="544" y="224" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">sıra tohumu raporlanır</text>
  <text x="544" y="241" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yapan: koşum motoru</text>

  <text x="330" y="272" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Mülakatta ilk hamle: "Hangisini kastediyorsunuz?"</text>
</svg>
```

### Temel kavramlar

**Birinci anlam — rastgele/keşifsel test yaklaşımı**

- **Keşifsel test (exploratory testing)**: öğrenme, test tasarımı ve test yürütmenin **eşzamanlı** yapıldığı yaklaşımdır. Testçi ürünü kullanırken öğrenir. Öğrendikçe yeni bir test fikri üretir, o fikri hemen dener ve çıkan sonuç bir sonraki fikri belirler. Senaryo önceden yazılmaz. Ama bu, testin **başıboş olduğu anlamına gelmez**; mülakatta en sık düşülen tuzak budur.
- **Oturum tabanlı test yönetimi (SBTM — session-based test management)**: keşifsel testi denetlenebilir hâle getiren çerçevedir. Dört bileşeni vardır. **Görev beyanı (charter)**: bu oturumda neyin, hangi riske karşı keşfedileceğini yazan 1-2 cümle. **Zaman kutusu (time-box)**: genellikle 60-90 dakikalık kesintisiz bir oturum. **Not tutma**: denenen yollar, gözlemler, sorular ve bulunan hatalar. **Oturum sonu raporu (debrief)**: sürenin ne kadarının kuruluma, ne kadarının test tasarımı ile yürütmeye, ne kadarının hata araştırmasına gittiğini gösteren kısa özet.
- **Keşif-test oranı (session metrics)**: oturum süresinin kabaca üçe bölünmesidir. Üç parça şunlardır: kurulum (setup), test tasarımı ve yürütme, hata araştırması ve raporlaması. Bir oturumun %60'ı hata araştırmasına gitmişse test edilmiş alan aslında çok dardır. Bu oran raporlanır ki "2 saat keşif yaptım" cümlesi ölçülebilir bir şeye dönüşsün.
- **Ad-hoc test**: kaydı, görev beyanı ve zaman kutusu olmayan, tamamen anlık denemedir. Keşifsel testin **disiplinsiz** hâli olarak düşünülebilir. Bazen faydalıdır ama tekrar edilemez ve raporlanamaz. Keşifsel test ile ad-hoc testi eş anlamlı kullanmak mülakatta en yaygın hatadır.
- **Maymun testi (monkey testing)**: arayüze rastgele ve anlamsız etkileşimler uygulayıp uygulamanın çökmemesini bekleyen otomatik yaklaşımdır. Etkileşimler rastgele tıklama, rastgele tuş, rastgele kaydırma olabilir. Amaç doğrulama değil, **dayanıklılık (robustness)** sınamasıdır. "Akıllı maymun" (smart monkey) varyantı geçerli aksiyonlar arasından rastgele seçer; "aptal maymun" (dumb monkey) ise tamamen rastgele davranır.
- **Fuzzing (bulanık test)**: girdi noktalarına rastgele, aşırı uzun, bozuk veya beklenmedik kodlamada veri göndermektir. Amaç çökme, yakalanmamış hata (unhandled exception) veya güvenlik açığı avlamaktır. Maymun testinden farkı, hedefin arayüz etkileşimi değil **girdi alanı veya protokol** olmasıdır. Örnek: adres formuna 10.000 karakter, emoji, tersten yazılan Unicode kontrol karakteri, `%00` ya da çok baytlı bozuk bir UTF-8 dizisi göndermek.
- **Gerilla test / hata avı (bug hunt, bug bash)**: birden çok kişinin aynı anda ürünü kurcaladığı, kısa ve zaman kutulu bir oturumdur. Katılımcılar testçi, geliştirici, ürün sahibi, hatta destek ekibi olabilir. Yeni özelliğin yayına çıkışından önce çok sayıda farklı bakış açısını ucuza toplamanın yoludur.
- **Otomasyonun ve keşifsel testin iş bölümü**: **Otomasyon, bildiğin bir hatanın geri gelmediğini korur; buna regresyon güvencesi denir. Keşifsel test ise henüz bilmediğin hatayı bulur.** Otomatik test yalnızca yazarının aklına gelen şeyi kontrol eder; hiçbir zaman yazarından daha yaratıcı olamaz. Bu yüzden ikisi rakip değil, tamamlayıcıdır.

**İkinci anlam — rastgele test verisi**

- **Sabit (hard-coded) veri ile üretilen (generated) veri dengesi**: Sabit veri okunabilir ve yeniden üretilebilirdir. Ama çakışır — aynı e-postayla ikinci kez üye olunamaz — ve dar bir alanı sınar. Üretilen veri ise çakışmayı çözer ve girdi çeşitliliği sağlar. Buna karşılık **sonucun belirliliğini (determinism)** zayıflatır.
- **Benzersizlik gereken alanlar**: e-posta, kullanıcı adı, sipariş numarası ve kupon kodu gibi alanlarda çakışmayı önlemenin yolu "rastgele bir isim" değildir. Doğru yol **yapısal benzersizliktir**: `UUID` veya zaman damgası (timestamp) eklemek. `test+3f9c1a2e@velzon.test` gibi bir e-posta hem benzersizdir hem de bakılır bakılmaz üretilmiş test verisi olduğu anlaşılır.
- **Rastgele verinin asıl tehlikesi — yeniden üretilemezlik**: test bazen kırılır ama hata mesajı "hangi veriyle kırıldı?" sorusuna cevap vermez. Bir sonraki koşumda başka veri üretileceği için hata bir daha görülmez. Hata kaydı "ara sıra oluyor" diye kapanır ve gerçek bir kusur canlıya sızar.
- **Tohum (seed)**: sözde-rastgele (pseudo-random) üreticiye verilen başlangıç sayısıdır. Aynı tohum, aynı diziyi üretir. Kural nettir: **tohum sabitlenir ve rapora yazılır.** Böylece rastgelelik kaybolmaz. Buna karşılık başarısızlık `-Dtest.seed=1753800000000` ile birebir tekrar üretilebilir hâle gelir. Ayrıca başarısızlık anında **kullanılan verinin kendisi** de rapora eklenir; Allure eki veya bir log satırı olabilir. Tohum dolaylı kanıttır, veri ise doğrudan kanıttır.
- **Sınır değerleri rastgeleye bırakılmaz**: Denklik sınıfı (equivalence partitioning) ve sınır değer analizi (boundary value analysis) ile bulunan değerler **elle ve belirli** seçilir. Örnek: sepette izin verilen en fazla adet 10 ise sınanacak değerler 0, 1, 9, 10 ve 11'dir. Rastgelelik yalnızca sonucu etkilemeyen "önemsiz" alanlarda serbesttir: müşteri adı, adres metni, ürün açıklaması gibi. Sınırı rastgele bir sayıya bırakmak, testin bazen sınırı hiç sınamaması demektir.
- **Özellik tabanlı test (property-based testing)**: rastgeleliği disiplinli kullanmanın en olgun biçimidir. Tek tek örnek yerine bir **özellik** yazılır; örneğin "sepet toplamı, satır tutarlarının toplamına eşittir". Araç yüzlerce rastgele girdi üretip bu özelliği sınar. Özellik kırılırsa **küçültme (shrinking)** ile hatayı üreten en küçük girdiyi bulur ve tekrar koşum için tohumu raporlar. Java tarafında `jqwik` bunu JUnit 5 üzerinde yapar. Not: bu yaklaşım saf iş mantığı ve hesaplama için çok güçlüdür; uçtan uca arayüz testi için ise pahalıdır.
- **Veri temizliği (cleanup) ve çöp kayıt**: üretilen her müşteri, sipariş veya sepet arkada bir kayıt bırakır. Temizlik yapılmazsa test ortamı zamanla şişer, arama sonuçları bozulur ve testler birbirini etkiler. Temizlik testin kendi sorumluluğudur: kendi verisini kurar, kendi verisini toplar.
- **Üretim verisi kopyalamanın sınırı**: "gerçekçi veri" arayışıyla üretim veritabanını test ortamına kopyalamak, Türkiye'de KVKK kapsamında kişisel veri işleme demektir. Ad, telefon, adres ve TCKN gibi alanlar **maskeleme (masking) veya takma adlaştırma (pseudonymization)** olmadan test ortamına taşınamaz. Faker türü üretilmiş veriyi tercih etmenin bir gerekçesi de budur.

**Üçüncü anlam — rastgele test sırası**

- **Rastgele sıra (random order)**: testleri her koşumda farklı sırada çalıştırmaktır. Amacı hata üretmek değil, **gizli bağımlılıkları ortaya çıkarmaktır**. Gizli bağımlılık şudur: bir test, kendisinden önce koşan başka bir testin bıraktığı duruma farkında olmadan bel bağlar. O durum bir oturum, bir sepet, bir veritabanı kaydı veya statik bir değişken olabilir. Sabit sırada bu bağımlılık yıllarca görünmez kalır. Testlerden biri silindiği ya da araya yeni bir test girdiği gün her şey kırılır.
- **Kritik yorum farkı**: rastgele sıra, testi **kararsız (flaky) yapmaz; testin zaten kararsız olduğunu gösterir**. Rastgele sırada kırılan bir testi sabit sıraya döndürerek "düzeltmek", termometreyi kırıp ateşi düşürmeye benzer.
- **Rastgele sıra ve paralel koşum**: sıra rastgeleleştiğinde hangi testlerin **aynı anda** koşacağı da değişir. Bu durumda paylaşılan bir kaynak üzerinde yarış koşulu (race condition) ortaya çıkar; kaynak aynı test hesabı, aynı kupon kodu veya aynı stok kalemi olabilir. Çözüm sırayı sabitlemek değildir. Çözüm, her testin **kendi verisini kendisinin kurmasıdır**. İzolasyonun nasıl kurulduğu 203 numaralı konuda ayrıntılı işleniyor.
- **Sıra tohumu da tohumlanır**: rastgele sıra kullanılıyorsa koşum motoru o koşumun sıra tohumunu raporlamalıdır. Aksi hâlde "şu üç testin şu sırada koşması kırıyor" bilgisi tekrar üretilemez. JUnit 5'in `MethodOrderer.Random` uygulaması tam olarak bunun için tohumu log'a yazar. Ayrıca `junit.jupiter.execution.order.random.seed` ile tohumun sabitlenmesine izin verir.
- **Yeniden deneme (retry) tuzağı**: rastgele sıra bir gizli bağımlılığı açığa çıkardığında testi otomatik yeniden denemeyle geçirmek, bulunan bilgiyi çöpe atmaktır. Yeniden denemenin kararsızlığı çözmeyip gizlediği 202 numaralı konuda ayrıntılı işlendi. Burada eklenecek tek şey şudur: yeniden deneme, rastgele sıranın **kazandırdığı** teşhis gücünü doğrudan yok eder.

```svg
<svg viewBox="0 0 660 262" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Oturum tabanlı keşifsel test akışı: görev beyanı, zaman kutusu, not tutma, oturum sonu raporu ve süre dağılımı">
  <defs>
    <marker id="rkt-ok" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Keşifsel test başıboş değildir: oturum tabanlı yönetim</text>
  <text x="330" y="45" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">senaryo önceden yazılmaz, ama oturum planlanır ve raporlanır</text>

  <rect x="14" y="62" width="140" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="84" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Görev beyanı</text>
  <text x="84" y="102" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(charter)</text>
  <text x="84" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"Kupon alanını hatalı</text>
  <text x="84" y="134" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">girdilerle keşfet"</text>

  <rect x="178" y="62" width="140" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="248" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Zaman kutusu</text>
  <text x="248" y="102" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(time-box)</text>
  <text x="248" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">60-90 dk kesintisiz</text>
  <text x="248" y="134" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek oturum</text>

  <rect x="342" y="62" width="140" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="412" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Not tutma</text>
  <text x="412" y="102" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(session notes)</text>
  <text x="412" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">denenen yol, gözlem,</text>
  <text x="412" y="134" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">soru, bulunan hata</text>

  <rect x="506" y="62" width="140" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="576" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Oturum raporu</text>
  <text x="576" y="102" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(debrief)</text>
  <text x="576" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapsanan alan +</text>
  <text x="576" y="134" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">süre dağılımı</text>

  <line x1="154" y1="102" x2="176" y2="102" stroke="#186399" stroke-width="1.6" marker-end="url(#rkt-ok)"/>
  <line x1="318" y1="102" x2="340" y2="102" stroke="#186399" stroke-width="1.6" marker-end="url(#rkt-ok)"/>
  <line x1="482" y1="102" x2="504" y2="102" stroke="#186399" stroke-width="1.6" marker-end="url(#rkt-ok)"/>

  <text x="330" y="176" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Raporlanan keşif-test oranı (90 dakikalık oturum)</text>
  <rect x="60" y="190" width="120" height="30" rx="5" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="120" y="209" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kurulum %20</text>
  <rect x="180" y="190" width="300" height="30" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="209" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">test tasarımı ve yürütme %55</text>
  <rect x="480" y="190" width="120" height="30" rx="5" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="540" y="209" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hata araştırma %25</text>
  <text x="330" y="244" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hata araştırması payı çok büyüdüyse keşfedilen alan dardır — bir sonraki oturum daralır</text>
</svg>
```

### Pratikte nasıl yapılır?

**1) Keşifsel oturumu kurma.** Kampanya sayfası yayına çıkmadan önce yapılacak iş, "bir bakayım" değil, yazılı bir görev beyanıdır:

```
Görev beyanı (charter):
  Black Friday kampanya sayfasında SEPET-KUPON etkileşimini,
  indirim üst sınırı ve stok tükenmesi risklerine karşı keşfet.
Zaman kutusu: 90 dk (14:00-15:30)
Kapsam dışı: ödeme adımı (ayrı oturum), mobil görünüm (ayrı oturum)
Katılımcı: 1 testçi
Oturum notu: notes/2026-07-29-kupon-kesif.md
```

Oturum boyunca not tutulur. Örnek bir not: "Kupon uygulanmış sepette adet artırınca indirim yeniden hesaplanmıyor — 3 kez tekrarlandı, ekran görüntüsü alındı." Sonunda kısa bir rapor yazılır: hangi alanlara dokunuldu, hangilerine dokunulmadı, kaç hata açıldı, süre nasıl dağıldı. Bulunan her hata için — ve bu kritik bir ayrıntıdır — **hata bulunduktan sonra otomatik bir regresyon testi yazılır**. Keşif hatayı bulur; otomasyon o hatanın bir daha dönmediğini korur.

**2) Test verisini üretmek, ama tohumlu.** Java + TestNG tarafında kalıp şudur — rastgelelik var, belirlenimcilik de var:

```java
public class KuponTesti {

  // Tohum dışarıdan verilebilir: -Dtest.seed=1753800000000
  private static final long TOHUM = Long.getLong("test.seed", System.currentTimeMillis());
  private final Faker faker = new Faker(new Locale("tr"), new Random(TOHUM));

  @BeforeSuite(alwaysRun = true)
  public void tohumuRaporaYaz() {
    Reporter.log("Test verisi tohumu (seed): " + TOHUM, true);
  }

  // Sınır değerleri RASTGELE DEĞİL: elle seçilmiş, belirlenimci
  @DataProvider(name = "sepetAdetSiniri")
  public Object[][] sepetAdetSiniri() {
    return new Object[][] { {0, false}, {1, true}, {9, true}, {10, true}, {11, false} };
  }

  // Sonucu etkilemeyen alanlar rastgele olabilir
  private Musteri yeniMusteri() {
    return new Musteri(
        faker.name().fullName(),
        "test+" + UUID.randomUUID() + "@velzon.test",   // benzersizlik: UUID
        faker.address().streetAddress());
  }
}
```

Burada üç karar iç içedir. (a) Sepet adet sınırı `DataProvider` ile **elle** verilmiştir; çünkü 10 ve 11 sınırının her koşumda sınanması gerekir. (b) Müşteri adı ile adresi rastgeledir; çünkü sonucu etkilemezler. (c) E-posta `UUID` ile benzersizdir; çünkü aynı e-postayla ikinci bir üyelik açılamaz. Ayrıca paralel koşumda iki test aynı adresi kullanırsa çakışırlar. Bir uyarı: uzun yıllar standart olan `JavaFaker` kütüphanesi bakımsız kaldı. Ekosistem büyük ölçüde onun bakımı süren çatalına (fork) — **Datafaker**'a — geçti. Yeni projelerde tercih bu yönde yapılır; paket adları farklıdır ama kalıp aynıdır.

Playwright/JavaScript tarafında aynı disiplin `@faker-js/faker`'ın `seed()` fonksiyonuyla kurulur ve tohum koşum ekine yazılır:

```js
const { faker } = require('@faker-js/faker');

const TOHUM = Number(process.env.DATA_SEED || Date.now() % 2147483647);
faker.seed(TOHUM);

test.beforeEach(async ({}, testInfo) => {
  testInfo.annotations.push({ type: 'veri-tohumu', description: String(TOHUM) });
});

test('kupon kodu tanınmayan formatta reddedilir', async ({ page }, testInfo) => {
  const kupon = `TEST-${faker.string.alphanumeric(8).toUpperCase()}`;
  await testInfo.attach('kullanilan-kupon', { body: kupon });   // başarısızlıkta hangi veri?
  // ...
});
```

**3) Başarısızlıkta "hangi veriyle?" sorusunu cevaplamak.** Tohumu raporlamak yeterli değildir; çünkü tohum yalnızca **tüm koşumu** tekrar üretir. Tek bir testin hatasını hızlı anlamak için, o testin kullandığı veri doğrudan rapora eklenir. Allure'da `allure.parameter('kupon', kupon)` ya da bir metin eki kullanılır; TestNG'de ise `Reporter.log`. Kural şöyle özetlenebilir: **rastgele veri kullanan bir testin hata mesajı, veriyi içermiyorsa eksiktir.**

**4) Rastgele sırayı açmak.** JUnit 5 tarafında hem metot hem sınıf düzeyinde rastgele sıra vardır ve ikisi de tohumlanabilir (`src/test/resources/junit-platform.properties`):

```properties
# sınıfların koşum sırası rastgele
junit.jupiter.testclass.order.default = org.junit.jupiter.api.ClassOrderer$Random
# sıra tohumu: verilmezse üretilir ve log'a yazılır, verilirse aynı sıra tekrar üretilir
junit.jupiter.execution.order.random.seed = 1753800000000
```

```java
@TestMethodOrder(MethodOrderer.Random.class)
class SepetHesaplamaTesti { /* ... */ }
```

TestNG'de yerleşik bir "rastgele sırala" anahtarı yoktur. Sıra `preserve-order` ayarı ve bağımlılıklarla yönetilir. Karıştırmak isterseniz `IMethodInterceptor` uygulayan küçük bir sınıf yazarsınız. Bu sınıf metot listesini tohumlu bir `Random` ile karıştırır ve kullanılan tohumu log'a yazar. Playwright ile Cucumber tarafında da doğrudan bir "rastgele sırala" ayarı yoktur. Ancak paralel koşumda senaryoların işçilere (worker) dağılımı koşumdan koşuma değişir. Bu yüzden **fiilen** benzer bir karıştırma etkisi doğar. Bu etki rastgele sıranın yerini tutmaz ama aynı sınıf hataları — yani gizli bağımlılıkları — ortaya çıkarır.

**5) Fuzzing'i küçük ve hedefli tutmak.** Adres formuna karşı kısa bir bozuk girdi listesi hazırlayın: 10.000 karakterlik metin, emoji, `<script>` benzeri işaretleme, `%00`, çok baytlı bozuk UTF-8 ve negatif ya da aşırı büyük sayılar. Bu listeyi uçtan uca test paketine koymayın; **ayrı ve gece koşan (nightly) bir işe** koyun. Beklenti "doğru sonuç" değildir. Beklenti şudur: uygulama 500 dönmemeli, sunucu log'una yakalanmamış hata (unhandled exception) düşmemeli ve kullanıcıya anlaşılır bir doğrulama mesajı gösterilmelidir.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tohumsuz rastgelelik hatayı kaybettirir, tohumlu rastgelelik hatayı yeniden ürettirir">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Rastgeleliğin bedeli: yeniden üretilebilirlik</text>

  <rect x="16" y="44" width="308" height="206" rx="11" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="170" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Tohumsuz rastgele veri</text>
  <text x="170" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Koşum 1: kupon "A7X-91" → KIRMIZI</text>
  <text x="170" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Koşum 2: kupon "K2M-04" → YEŞİL</text>
  <text x="170" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Koşum 3: kupon "Q8T-55" → YEŞİL</text>
  <line x1="44" y1="146" x2="296" y2="146" stroke="#B23B3B" stroke-width="1" stroke-dasharray="4,3"/>
  <text x="170" y="167" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">"Hangi veriyle kırıldı?" → bilinmiyor</text>
  <text x="170" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hata tekrar üretilemez</text>
  <text x="170" y="206" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kayıt "ara sıra oluyor" diye kapanır</text>
  <text x="170" y="232" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">gerçek kusur canlıya sızar</text>

  <rect x="336" y="44" width="308" height="206" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="490" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Tohumlu rastgele veri</text>
  <text x="490" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Koşum 1: tohum 1753800000000 → KIRMIZI</text>
  <text x="490" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tohum ve kupon "A7X-91" rapora yazıldı</text>
  <text x="490" y="132" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">-Dtest.seed=1753800000000 → aynı veri</text>
  <line x1="364" y1="146" x2="616" y2="146" stroke="#25804C" stroke-width="1" stroke-dasharray="4,3"/>
  <text x="490" y="167" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">hata birebir yeniden üretilir</text>
  <text x="490" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kök neden bulunur, düzeltilir</text>
  <text x="490" y="206" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">o veri artık sabit regresyon testidir</text>
  <text x="490" y="232" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">rastgelelik korunur, kanıt kaybolmaz</text>
</svg>
```

**6) Rastgeleliği CI'da nereye koymak.** Doğru mimari, "rastgele test" adında bir aşama kurmak **değildir**. Doğru mimari şudur. Her PR'da koşan regresyon paketi tamamen belirlidir: sabit sınır değerleri, sabit tohum. Gece koşan ayrı bir iş ise rastgeleliği artırır; yeni tohum, rastgele sıra, fuzzing listesi ve özellik tabanlı testler burada koşar. Bu iş bulduğu her hatayı tohumuyla birlikte raporlar. Gece işi bir hata bulduğunda o vaka **sabit bir örneğe dönüştürülüp** PR paketine eklenir. Böylece rastgelelik bir keşif kanalı olur, kapı bekçisi değil. Çünkü kapı bekçisinin yargısı tekrar edilebilir olmak zorundadır.

### Mülakatta ne sorulur / tuzaklar

- **"Random test yapıyor musunuz?"** Beklenen ilk hamle ayrım yapmaktır: yaklaşım mı (keşifsel/fuzzing), veri mi (Faker/tohum), sıra mı (random order)? Üçünü de kısaca tanımlayıp hangisini kullandığınızı anlatın.
- **Tuzak: "Keşifsel test = plansız test, dokümantasyon yok."** Bunu derseniz elenirsiniz. Keşifsel testin senaryosu yoktur ama **görev beyanı, zaman kutusu, notu ve oturum raporu vardır**. Dokümantasyonsuz olan ad-hoc testtir.
- **Tuzak: "Otomasyonumuz iyi olduğu için keşifsel teste ihtiyacımız yok."** Otomasyon yalnızca yazarının aklına geleni kontrol eder. Bilinen hatanın dönmediğini korur; bilinmeyen hatayı bulmaz. Doğru cümle şudur: ikisi farklı işleri yapar.
- **Tuzak: "Her testte rastgele veri kullanırsak kapsam artar."** Kapsam artmaz; **sonucun belirliliği kaybolur**. Rastgele veri girdi çeşitliliği sağlar. Ama sınır değerlerini garanti etmez ve başarısızlığı yeniden üretilemez kılar.
- **"Rastgele veri kullanan bir test bazen kırılıyor, ne yaparsınız?"** Beklenen sıra şudur: önce tohumu sabitleyip rapora yazmak, başarısızlıkta kullanılan veriyi ek olarak koymak, hatayı yeniden üretmek ve kök nedeni bulmak. Sonra o veriyi **sabit bir regresyon vakasına** çevirmek. Beklenmeyen cevaplar: "yeniden deneme ekleriz" veya "sabit veriye döneriz".
- **"Test sırasını neden rastgeleleştirelim ki, testler zaten yeşil?"** Beklenen: yeşil olmaları bağımsız oldukları anlamına gelmez. Rastgele sıra **gizli bağımlılığı ortaya çıkarır**. Tuzak cevabı şudur: "Rastgele sıra testleri kararsız yapar." Hayır; zaten kararsız olduklarını gösterir.
- **"Rastgele sırada bir test kırıldı, sabit sıraya dönelim mi?"** Hayır; bu, termometreyi kırmaktır. Doğru hamle, testin kendi verisini ve durumunu kendisinin kurmasını sağlamaktır. Buna izolasyon denir.
- **"Maymun testi ile fuzzing aynı şey mi?"** Aynı değildir. Maymun testi **arayüz etkileşimini** rastgeleleştirir: rastgele tıklama, rastgele tuş. Fuzzing ise **girdi veya protokol** verisini bozar: uzun metin, bozuk kodlama, kontrol karakteri. Ortak yanları, doğrulama değil dayanıklılık aramalarıdır.
- **"Gerçekçi test verisi için üretim veritabanını kopyalayalım mı?"** Beklenen şerh: KVKK kapsamında kişisel veri; maskeleme/takma adlaştırma olmadan taşınamaz. Üretilmiş veri (Faker/Datafaker) bu riski hiç doğurmaz.
- **"Rastgele testleri CI'da nereye koyarsınız?"** Beklenen: PR kapısı belirlenimci kalır; rastgelelik gece koşan ayrı bir işe konur, bulduğu vaka sabit örneğe çevrilip PR paketine eklenir.

### Tek cümlede

"Rastgele test" üç ayrı şeydir: keşifsel **yaklaşım** (bilinmeyen hatayı insan bulur, ama oturum, görev beyanı ve rapor disipliniyle), rastgele **veri** (çeşitlilik ile benzersizlik sağlar, ama sınır değerleri elle seçilir ve tohum raporlanır) ve rastgele **sıra** (gizli bağımlılığı açığa çıkarır, ama sıra tohumu da raporlanır). Üçünde de altın kural aynıdır: rastgeleliği kontrollü ve tohumlu kullan. Çünkü **yeniden üretilemeyen bir başarısızlık, bulunmamış bir hataya eşdeğerdir**.

# rastgele-1 | Temel
## Soru
Mülakatta size "sizin ekipte random test yapılıyor mu?" diye soruldu. En güçlü ilk cevap hangisidir?
## A
"Terimin üç ayrı anlamı var — keşifsel yaklaşım, rastgele veri, rastgele koşum sırası; hangisini kastediyorsunuz?" diye sormak
🤖 Doğru: terim gerçekten üç farklı ve birbirinden bağımsız konuyu işaret eder; ayrımı yapmak konuyu ezberlemiş biriyle anlamış biri arasındaki farkı gösterir.
## B
"Evet, Faker ile rastgele veri üretiyoruz" deyip doğrudan test verisi üretimini anlatmaya başlamak
🤖 Sorulan bu olabilir ama olmayabilir de; tek anlamı varsayıp anlatmak, sorulanın keşifsel test veya rastgele koşum sırası olduğu durumda konuyu tamamen ıskalar.
## C
"Hayır, biz belirlenimci test yazarız; rastgelelik testte istenmeyen bir şeydir" demek
🤖 Rastgelelik kontrollü kullanıldığında (tohumlu veri, rastgele sıra, keşifsel oturum) değerli bir kalite aracıdır; toptan reddetmek eksik bir duruştur.
## D
"Evet, testleri her koşumda rastgele sırada çalıştırıyoruz" deyip yalnızca koşum sırasını anlatmak
🤖 Bu da üç anlamdan yalnızca biridir; tek başına verilen cevap, sorulan şeyin keşifsel test veya test verisi olması hâlinde konuyu karşılamaz.
## Doğru
A
## Açıklama
"Random test" ifadesi mülakatta üç ayrı konudan birini kastedebilir: (1) rastgele/keşifsel **test yaklaşımı** — keşifsel test, maymun testi, fuzzing; (2) rastgele **test verisi** — Faker/Datafaker ile üretilen isim, adres, kupon kodu; (3) rastgele **test sırası** — testlerin her koşumda farklı sırada çalıştırılması. Üçü bağımsız konulardır ve biri diğerinin yerini tutmaz, bu yüzden tek bir anlamı varsayıp anlatmak (B ve D) sorunun yarısını cevaplamak demektir. Rastgeleliği toptan reddetmek (C) de doğru değildir: tohumlanmış rastgele veri, rastgele koşum sırası ve planlı keşifsel oturumlar belirlenimcilikten feragat etmeden kalite kazandırır. En güçlü hamle önce ayrımı yapmak, sonra hangisini nasıl kullandığınızı anlatmaktır.

# rastgele-2 | Orta
## Soru
Bir ekip arkadaşınız "keşifsel test zaten plansız test demek, dokümante edilemez" diyor. Keşifsel testi ad-hoc testten ayıran şey nedir?
## A
Keşifsel test yalnızca otomasyon araçlarıyla yapılır; ad-hoc test elle yapılır
🤖 Keşifsel test tanımı gereği insanın öğrenip anında yeni test tasarladığı bir yaklaşımdır; araç kullanılabilir ama ayrım el/otomasyon ekseninde değildir.
## B
Keşifsel testte test senaryoları önceden yazılır ve adım adım takip edilir; ad-hoc testte yazılmaz
🤖 Tam tersi: keşifsel testin senaryosu önceden yazılmaz — önceden yazılmış adımları takip etmek betimlenmiş (scripted) testtir.
## C
Keşifsel test oturum tabanlı yönetilir: görev beyanı, zaman kutusu, not tutma ve oturum sonu raporu vardır; ad-hoc testte bunların hiçbiri yoktur
🤖 Doğru: senaryo önceden yazılmaz ama oturum planlanır ve raporlanır — keşifsel testi denetlenebilir kılan tam olarak bu çerçevedir.
## D
Keşifsel test yalnızca üretim ortamında, ad-hoc test yalnızca test ortamında yapılabilir
🤖 İki yaklaşım da ortamdan bağımsızdır; ayrım koşulan ortamla değil, oturumun yönetilip yönetilmediğiyle ilgilidir.
## Doğru
C
## Açıklama
Keşifsel test, öğrenme, test tasarımı ve test yürütmenin eşzamanlı yapılmasıdır — senaryolar önceden yazılmaz, testçi ürünü kullandıkça öğrenir ve öğrendikçe yeni test fikri üretir. Ama bu "başıboş" demek değildir: oturum tabanlı test yönetimi (SBTM) dört bileşen getirir — bu oturumda neyin, hangi riske karşı keşfedileceğini yazan **görev beyanı (charter)**, genellikle 60-90 dakikalık **zaman kutusu**, denenen yolların ve gözlemlerin **notu**, ve sürenin kuruluma/test yürütmeye/hata araştırmasına nasıl dağıldığını gösteren **oturum sonu raporu**. Dokümantasyonu, planı ve raporu olmayan; yalnızca anlık deneme olan yaklaşım **ad-hoc testtir** — bazen faydalıdır ama tekrar edilemez ve raporlanamaz. B seçeneği keşifsel testi betimlenmiş (scripted) testle karıştırır; A ve D ise ayrımı yanlış eksene (araç, ortam) taşır.

# rastgele-3 | Orta
## Soru
Sepette bir üründen alınabilecek maksimum adet 10. Ekip, sepet adet doğrulamasını test ederken adet değerini `faker.number().numberBetween(1, 50)` ile rastgele üretmeyi öneriyor. Bu yaklaşımın temel sorunu nedir?
## A
Faker'ın ürettiği sayılar gerçekten rastgele değil sözde-rastgeledir; bu yüzden test verisi olarak kullanılamaz
🤖 Sözde-rastgelelik bir kusur değil, tam tersine tohumlanabilirliği sağlayan özelliktir; sorun rastgeleliğin türü değil, nerede kullanıldığıdır.
## B
Sınır değerleri (0, 1, 9, 10, 11) rastgeleye bırakılamaz; bu değerler denklik sınıfı ve sınır değer analiziyle elle seçilmelidir
🤖 Doğru: rastgele bir sayı koşumların çoğunda sınıra hiç dokunmaz, yani asıl riskli değerler sınanmamış olur; sınır değerleri belirlenimci verilmelidir.
## C
Faker sayı üretirken paralel koşumda thread güvenliği sorunu çıkarır, bu yüzden sayısal alanlarda kullanılmamalıdır
🤖 Buradaki sorun eşzamanlılık değil test tasarımıdır; sınır değerlerin garanti edilmemesi, thread güvenliğinden bağımsız bir kusurdur.
## D
Rastgele sayı kullanmak testin koşum süresini öngörülemez hale getirir ve CI bütçesini aşar
🤖 Adet değerinin rastgele olması koşum süresini anlamlı biçimde değiştirmez; sorun süre değil, sınanan değerlerin garanti edilmemesidir.
## Doğru
B
## Açıklama
Sepet adet sınırı gibi bir kuralda hatanın saklandığı yer tam olarak sınırdır: 9 kabul edilmeli, 10 kabul edilmeli, 11 reddedilmeli, 0 reddedilmelidir. 1-50 arasından rastgele seçilen bir sayı koşumların çoğunda bu değerlerin hiçbirine denk gelmez — yani test yeşil yanar ama asıl riskli davranışı hiç sınamamıştır. Denklik sınıfı (equivalence partitioning) ve sınır değer analizi (boundary value analysis) ile bulunan değerler bu yüzden **elle ve belirlenimci** verilir; TestNG'de `DataProvider`, Playwright'ta parametreli test bunun içindir. Rastgelelik ise sonucu etkilemeyen "önemsiz" alanlarda serbesttir: müşteri adı, adres metni, ürün açıklaması. A ve C seçenekleri Faker'ın teknik özelliklerini sorun gibi gösterir (sözde-rastgelelik aslında tohumlamayı mümkün kılan iyi bir özelliktir), D ise ilgisiz bir maliyet gerekçesi uydurur.

# rastgele-4 | İleri
## Soru
Rastgele üretilmiş kupon kodlarıyla çalışan bir test haftada bir-iki kez kırılıyor; hata mesajı yalnızca "kupon uygulanamadı" diyor ve bir sonraki koşumda test yeşil yanıyor. Bu durumu kalıcı olarak çözen yaklaşım hangisidir?
## A
Testi rastgele veriden tamamen arındırıp tek bir sabit kupon koduyla çalıştırmak
🤖 Çakışma ve dar kapsam sorunlarını geri getirir; asıl eksik olan rastgeleliğin kendisi değil, başarısızlığın yeniden üretilebilirliğidir.
## B
Teste yeniden deneme (retry) eklemek; kırıldığında ikinci denemede geçtiği için CI yeşil kalır
🤖 Retry semptomu gizler, kök nedeni gizler; üstelik rastgeleliğin ortaya çıkardığı bilgiyi (hangi veri kırıyor) tamamen çöpe atar.
## C
Kupon kodunu her koşumda daha uzun ve daha karmaşık üreterek çakışma ihtimalini düşürmek
🤖 Sorunun çakışma olduğu doğrulanmamıştır; hangi verinin kırdığı bilinmediği sürece bu yalnızca bir tahmin üzerine yapılan değişikliktir.
## D
Tohumu (seed) sabitleyip rapora yazmak ve başarısızlıkta kullanılan kuponu eklemek; hatayı aynı tohumla yeniden üretmek
🤖 Doğru: önce başarısızlığı yeniden üretilebilir hale getirmek gerekir; tohum tüm koşumu, ek olarak kaydedilen veri ise tek testi tekrar üretmeyi sağlar.
## Doğru
D
## Açıklama
Rastgele veri kullanan bir testin en büyük riski, başarısızlığın **yeniden üretilemez** hale gelmesidir: bir sonraki koşumda başka veri üretileceği için hata bir daha görülmez, kayıt "ara sıra oluyor" diye kapanır ve gerçek kusur canlıya sızar. Çözümün ilk adımı rastgeleliği kaldırmak değil, onu **tohumlamaktır**: üreticiye verilen tohum sabitlenir, rapora yazılır (`Reporter.log`, Allure parametresi/eki) ve `-Dtest.seed=...` ile aynı koşum birebir tekrar üretilir. Buna ek olarak başarısızlık anında **kullanılan verinin kendisi** rapora eklenir — tohum tüm koşumu, veri ise tek testi doğrudan açıklar. Hata yeniden üretilip kök neden bulununca o vaka artık sabit bir regresyon örneğine çevrilir. A çözümü çakışma ve dar kapsam sorunlarını geri getirir, B (retry) kararsızlığı gizleyip teşhis bilgisini yok eder, C ise doğrulanmamış bir varsayım üzerine yapılan kör bir değişikliktir.

# rastgele-5 | İleri
## Soru
Ekip, yeşil koşan test paketini rastgele sırada (random order) çalıştırmayı denedi ve iki test kırıldı. Bir geliştirici "rastgele sıra testleri flaky yapıyor, kapatalım" diyor. Doğru yorum ve doğru hamle hangisidir?
## A
Geliştirici haklı: rastgele sıra ek bir belirsizlik kaynağıdır, sabit sıra testlerin güvenilirliğini artırır
🤖 Sabit sıra güvenilirlik değil, yalnızca mevcut bağımlılıkların görünmezliğini sağlar; sorun ortadan kalkmaz, ertelenir.
## B
Rastgele sıra açıkken kırılan testlere yeniden deneme (retry) eklenmeli; ikinci denemede genellikle doğru sırada koşacakları için geçerler
🤖 Retry sıra bağımlılığını çözmez ve rastgele sıranın kazandırdığı teşhis bilgisini yok eder; ayrıca deneme, sırayı yeniden düzenlemez.
## C
Rastgele sıra flaky üretmedi, var olan gizli bağımlılığı açığa çıkardı; kırılan testler kendi verisini/durumunu kendisi kuracak şekilde düzeltilmeli
🤖 Doğru: sabit sırada görünmeyen bağımlılık her zaman oradaydı; çözüm sırayı sabitlemek değil, testleri birbirinden bağımsız hale getirmektir.
## D
Kırılan iki testin sırası `@Order` anotasyonuyla sabitlenmeli, geri kalan testler rastgele sırada koşmaya devam etmeli
🤖 Bu, bağımlılığı düzeltmek yerine kod içinde kalıcılaştırır; testler yine birbirine bağımlı kalır ve bir test silindiğinde paket yine kırılır.
## Doğru
C
## Açıklama
Testleri her koşumda farklı sırada çalıştırmanın amacı hata üretmek değil, **gizli bağımlılıkları (hidden/inter-test dependency) ortaya çıkarmaktır**: bir testin, kendisinden önce koşan başka bir testin bıraktığı duruma (oturum, sepet içeriği, veritabanı kaydı, statik değişken) farkında olmadan bel bağlaması. Bu bağımlılık sabit sırada yıllarca görünmez kalır ve testlerden biri silindiği ya da araya yeni bir test girdiği gün toplu kırılmaya yol açar. Dolayısıyla rastgele sıra testi flaky **yapmaz**, zaten flaky olduğunu **gösterir**; sabit sıraya dönmek (A) veya sırayı anotasyonla sabitlemek (D) termometreyi kırıp ateşi düşürmeye benzer, üstelik D bağımlılığı kod içinde resmîleştirir. Retry (B) hem sırayı düzeltmez hem de rastgele sıranın kazandırdığı teşhis gücünü yok eder. Doğru hamle her testin **kendi verisini ve durumunu kendisinin kurmasını** sağlamak, yani izolasyonu düzeltmektir.

# rastgele-6 | Uzman
## Soru
Bir e-ticaret ekibi kalite artırmak için CI'a "rastgele test" adında yeni bir aşama eklemek istiyor: her PR'da rastgele tohumla üretilen veri, rastgele koşum sırası ve adres formuna fuzzing uygulayan testler koşacak; aşama kırmızıysa PR birleştirilemeyecek. Bu tasarımın temel kusuru ve doğru mimari nedir?
## A
PR kapısının yargısı tekrar edilebilir olmak zorundadır; rastgelelik gece koşan ayrı bir işe alınmalı, bulduğu vaka sabit bir örneğe çevrilip PR paketine eklenmelidir
🤖 Doğru: her PR'da farklı girdiyle koşan bir kapı, aynı kodun bir seferde geçip bir seferde kalmasına yol açar; rastgelelik keşif kanalı olmalı, kapı bekçisi değil.
## B
Fuzzing ve rastgele sıra aynı aşamada koşamaz; ikisi ayrı aşamalara bölünürse tasarım sorunsuz çalışır
🤖 İkisini ayırmak asıl sorunu (PR kapısının belirlenimciliğini kaybetmesi) çözmez; hangi aşamada koştuklarından bağımsız olarak yargı tekrar edilemez kalır.
## C
Sorun performanstır: rastgele veri ve fuzzing PR süresini uzatır; testler paralel koşum ve sharding ile hızlandırılırsa tasarım uygundur
🤖 Süre ikincil bir maliyettir; asıl kusur, aynı kodun farklı koşumlarda farklı sonuç vermesi ve kapının güvenilmez hale gelmesidir.
## D
Rastgelelik kalite katmaz; ekip bu aşamayı hiç kurmayıp tüm test verisini sabitlemeli ve keşifsel testi de bırakmalıdır
🤖 Rastgelelik kontrollü kullanıldığında (tohumlu veri, rastgele sıra, keşifsel oturum) bilinmeyen hataları bulan asıl kanaldır; toptan reddetmek kaliteyi düşürür.
## Doğru
A
## Açıklama
Bir PR kapısının (gate) işlevi, "bu değişiklik bilinen davranışı bozdu mu?" sorusuna **tekrar edilebilir** bir cevap vermektir. Her koşumda tohum, sıra ve fuzzing girdisi değişirse aynı kod bir seferde geçer, bir seferde kalır; geliştirici "tekrar dene" alışkanlığı kazanır ve kapı kısa sürede güvenilmez hale gelir — gerçek regresyonlar da bu gürültünün içinde kaybolur. Doğru mimari ikiye ayırmaktır: her PR'da koşan regresyon paketi **belirlenimcidir** (elle seçilmiş sınır değerleri, sabit tohum), rastgelelik ise gece koşan (nightly) ayrı bir işte artırılır — yeni tohum, rastgele sıra, fuzzing listesi, özellik tabanlı testler — ve bulduğu her hatayı tohumuyla, kullanılan verisiyle birlikte raporlar. O vaka sonra **sabit bir örneğe çevrilip** PR paketine eklenir. Böylece rastgelelik keşif kanalı olur, kapı bekçisi değil. B ve C gerçek ama ikincil sorunlara (aşama bölünmesi, süre) odaklanır; D ise rastgeleliği toptan reddederek bilinmeyen hataları bulan tek mekanizmayı ortadan kaldırır.
