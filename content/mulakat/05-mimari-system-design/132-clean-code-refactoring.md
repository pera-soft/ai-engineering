---
slug: clean-code-refactoring
baslik: "Clean Code & Refactoring: İsimlendirme, Küçük Fonksiyon, Kokular ve Boy Scout Kuralı"
kademe: Orta
sira: 132
etiketler: 5
---

## Konu anlatımı

### Bu ne işe yarar?

Bir yemek tarifi düşün: malzemeler "x", "y", "z" diye yazılmışsa ve adımlar karmakarışıksa, tarif "çalışıyor" olabilir ama kimse ona güvenip yemek yapamaz. Kod da böyledir; bir kez yazılır, onlarca kez **okunur**. Temiz kod (clean code), makinenin değil **insanın** rahat okuduğu koddur. Okunabilirlik bir zevk meselesi değil, doğrudan para meselesidir. Okunmayan kodda hatalar saklanır, her değişiklik uzar ve ekibin hızı düşer.

### Temel kavramlar

- **Okunabilirlik**: kodu ilk kez gören birinin, çalıştırmadan ne yaptığını anlayabilmesi.
- **Kod kokusu (code smell)**: kodun kendisi hatalı olmayabilir, ama "burada bir sorun var" diye bağıran yapısal bir işaret. Süt bozulmuş olmasa da kokusu şüphe uyandırır. Bilinen kokular şunlardır. **Uzun metot.** **Uzun parametre listesi**: bir metodun aldığı argüman sayısının şişmesi. **Yinelenen kod (duplication)**. **Kıskanç özellik (feature envy)**: bir metodun kendi sınıfının verisinden çok başka sınıfın verisiyle uğraşması. **İlkel takıntısı (primitive obsession)**: para, e-posta gibi iş kavramlarını hep `int` veya `String` ile temsil etmek. **Dağınık cerrahi (shotgun surgery)**: tek bir değişiklik için pek çok dosyaya dokunmak zorunda kalmak.
- **Refactoring**: **davranışı değiştirmeden** kodun iç yapısını iyileştirmek. Davranış değişiyorsa o refactoring değil, yeni bir özellik ya da bir hatadır.
- **Teknik borç (technical debt)**: bugün hızlı gitmek için bilerek bırakılan düzensizlik. Faizi vardır: her yeni değişiklik biraz daha pahalıya gelir.
- **DRY (Don't Repeat Yourself)**: her bilgi parçasının sistemde **tek, yetkili bir temsili** olsun.

### Nasıl çalışır?

**1) Önce güvenlik ağı.** Refactoring'in tanımı "davranışı korumak" olduğuna göre, davranışın korunduğunu **kanıtlayacak** bir şey gerekir: test. Test edilmemiş eski (legacy) kodda önce **karakterizasyon testi (characterization test)** yazılır. Bu test, kodun bugün ne yapıyorsa onu kilitler; "doğru mu yanlış mı" diye sormaz. Testleri yeşile aldıktan sonra küçük ve geri alınabilir adımlarla ilerlersin: değiştir → testi çalıştır → yeşilse commit et. Testsiz "iyileştirme", doğrulanmamış bir davranış değişikliğidir.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Refactoring döngüsü: yeşil test, küçük adım, tekrar yeşil">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Refactoring, testin koruması altında yürür</text>
  <rect x="30" y="54" width="150" height="70" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="105" y="86" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Testler YEŞİL</text>
  <text x="105" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">güvenlik ağı hazır</text>
  <rect x="245" y="54" width="150" height="70" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="82" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Küçük adım</text>
  <text x="320" y="101" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeniden adlandır /</text>
  <text x="320" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metot çıkar</text>
  <rect x="460" y="54" width="150" height="70" rx="12" fill="#FBE7D8" stroke="#B9791E" stroke-width="1.5"/>
  <text x="535" y="82" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B9791E" font-family="system-ui">Tekrar çalıştır</text>
  <text x="535" y="101" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">davranış aynı mı?</text>
  <text x="535" y="115" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeşilse commit</text>
  <path d="M180 89 L245 89" stroke="#186399" stroke-width="1.6"/>
  <path d="M395 89 L460 89" stroke="#186399" stroke-width="1.6"/>
  <path d="M535 124 C535 160, 105 160, 105 124" stroke="#8FA6B4" stroke-width="1.4" fill="none" stroke-dasharray="4 4"/>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">döngü: her adımda davranış korunur</text>
</svg>
```

**2) İsimler.** Bir ismin birincil işi **niyeti iletmektir**. `int d;` yerine `int gecenGunSayisi;` yazmak yorumu gereksiz kılar. Aşırı kısalık niyeti gizler. Tipi ada gömmek (`strKullaniciAdi` — Macar notasyonu) statik tipli ve IDE destekli Java'da yalnız gürültü üretir. Veritabanı kısaltmalarını (`usr_nm`) koda taşımak da okuyucuya değil, şemaya hizmet eder.

```svg
<svg viewBox="0 0 640 168" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İsimlendirme: belirsiz ad yerine niyeti açıklayan ad">
  <defs>
    <marker id="clean-name-ok" markerWidth="10" markerHeight="10" refX="8" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İyi isim niyeti açıklar, yorumu gereksiz kılar</text>
  <rect x="34" y="50" width="230" height="90" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="149" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Belirsiz ad</text>
  <text x="149" y="100" text-anchor="middle" font-size="13" font-weight="700" fill="#1C3247" font-family="system-ui">int d;</text>
  <text x="149" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bu ne? yorum gerekir</text>
  <rect x="376" y="50" width="230" height="90" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Niyeti açık ad</text>
  <text x="491" y="100" text-anchor="middle" font-size="13" font-weight="700" fill="#1C3247" font-family="system-ui">int gecenGunSayisi;</text>
  <text x="491" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kendini anlatır</text>
  <line x1="264" y1="95" x2="376" y2="95" stroke="#25804C" stroke-width="1.8" marker-end="url(#clean-name-ok)"/>
  <text x="320" y="88" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yeniden</text>
  <text x="320" y="102" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">adlandır</text>
</svg>
```

**3) Fonksiyonlar.** "Küçük olsun" kuralının ölçütü **satır sayısı değildir** — 3, 20 ya da 100 gibi rakamlar keyfîdir. Ölçüt ikilidir: fonksiyon **tek bir iş** yapsın ve **tek bir soyutlama seviyesinde** kalsın. Üst seviye iş akışını (`siparisiOnayla`) düşük seviye ayrıntıyla (bayt kaydırma, metin biçimleme) aynı gövdede karıştıran fonksiyon, kısa olsa bile okunmaz. İki satırlık bir fonksiyon iki iş yapıyorsa kötüdür. Otuz satırlık bir fonksiyon tek işi net anlatıyorsa iyi olabilir.

**4) Yorumlar.** Yorum, kokunun kokusu olabilir. Kodun **ne yaptığını** tekrarlayan yorum (`i++; // i'yi artır`) çoğunlukla kötü isimlendirmenin özrüdür. Üstelik kod değişince güncellenmediği için zamanla **yalan söylemeye** başlar. Kodun **neden** öyle yazıldığını anlatan yorum ise korunur; çünkü koddan çıkarılamayan bilgi taşır. Örnekleri: bir iş kararının gerekçesi, dış kütüphanenin tuhaf davranışına karşı alınan önlem, bilinçli bir performans ödünü, `// HACK` uyarısı.

```svg
<svg viewBox="0 0 640 176" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yorumlar: ne yorumu silinir, neden yorumu korunur">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"Ne"yi kod söyler, "neden"i yorum korur</text>
  <rect x="34" y="48" width="264" height="104" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="166" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">"NE" yorumu — SİL</text>
  <text x="52" y="98" text-anchor="start" font-size="11" fill="#5D7789" font-family="system-ui">i++; // i'yi bir artır</text>
  <text x="52" y="120" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">kodu tekrarlar, gürültü;</text>
  <text x="52" y="136" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">değişince yalan söyler</text>
  <rect x="342" y="48" width="264" height="104" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="474" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">"NEDEN" yorumu — KORU</text>
  <text x="360" y="98" text-anchor="start" font-size="11" fill="#5D7789" font-family="system-ui">// API 0 döndüğünde</text>
  <text x="360" y="118" text-anchor="start" font-size="11" fill="#5D7789" font-family="system-ui">// yeniden dene (bilinen bug)</text>
  <text x="360" y="138" text-anchor="start" font-size="10.5" fill="#25804C" font-family="system-ui">koddan çıkarılamayan bilgi</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Yinelenen kod neden en zararlı kokudur?"** Çünkü iş kuralı değişince **her kopyayı** bulup aynı biçimde güncellemen gerekir. Biri unutulursa sistem tutarsız davranır ve bu tür hatalar sinsidir. Karşı denge şudur: **tesadüfi benzerlik** — aynı görünen ama farklı nedenlerle değişen kod — DRY adına birleştirilmemelidir. Yanlış soyutlama, yinelemeden daha pahalıya patlar.
- **"Refactoring'e nereden başlarsın?"** Beklenen cevap "testten". Sıfırdan yeniden yazmak refactoring değildir ve mevcut davranıştaki gizli iş kurallarını kaybetme riski taşır.
- **"İyi kod kendini belgeler, yorum gereksizdir" iddiası.** Olgun cevap ikisini de reddetmez: **"ne" yorumunu sil, "neden" yorumunu koru.**
- **İzci Kuralı (Boy Scout Rule) ve sınırı.** "Kampı bulduğundan biraz daha temiz bırak" der: dokunduğun dosyayı ufak bir iyileştirmeyle geride bırak. Ama iyileştirme **küçük, güvenli ve gözden geçirilebilir** kalmalıdır. Hata düzeltmesiyle karışan devasa bir değişiklik yığını kod incelemesini zorlaştırır ve riski artırır. Büyük temizlik **kendi PR'ında** yapılır.
- **Ölçü.** Temiz kod bir ahlak dersi değil **ekonomidir**; ama "temizlik" adına erken soyutlama da tuzaktır. Önce çalışsın, sonra netleşsin.

### Tek cümlede

Temiz kod, niyeti anlatan isimler ve tek iş yapan fonksiyonlarla yazılan; testin koruması altında, küçük adımlarla sürekli iyileştirilen koddur.

# clean-1 | Temel
## Soru
İyi bir değişken/metot isminin birincil amacı nedir?
## A
Mümkün olan en kısa olmak; kısa isimler kodu daha hızlı okunur kılar
🤖 Kısalık amaç değil; `d` gibi isimler niyeti gizler ve okuyucuyu yoruma muhtaç bırakır.
## B
Niyeti açıkça iletmek, böylece ayrıca yorum yazma ihtiyacı bırakmamak
🤖 Doğru: iyi isim "bu nedir, ne işe yarar"ı kendisi anlatır, yorumu gereksiz kılar.
## C
Değişkenin tipini adın başına eklemek; strKullaniciAdi gibi Macar notasyonu kullanmak
🤖 Statik tipli ve IDE destekli Java'da tip zaten bellidir; tipi ada gömmek gürültü üretir.
## D
Veritabanı sütun adıyla birebir aynı kalmak; alan adı usr_nm ise kodda da öyle olmalıdır
🤖 Şema kısaltmasını koda taşımak niyeti gizler; ad, şemaya değil kodun okuyucusuna hitap etmeli.
## Doğru
B
## Açıklama
Bir ismin **birincil işi niyeti iletmektir**: "bu değişken neyi temsil ediyor, bu metot ne yapıyor?" sorusuna ismin kendisi yanıt vermeli. `int d;` yerine `int gecenGunSayisi;` yazmak, yorum satırına olan ihtiyacı ortadan kaldırır — "isim yorum gerektiriyorsa, isim niyeti açıklamıyor demektir." Aşırı kısalık (A) hızlı yazdırır ama okumayı yavaşlatır; kod bir kez yazılıp defalarca okunduğu için bu kötü bir takas. Macar notasyonu (C) statik tipli bir dilde tekrar üretir ve tip değişince yalan söyler. Veritabanı kısaltmalarını koda taşımak (D) ise ismi şemanın diline hapseder; kalıcı kural şudur: **ad, kodu okuyan insana hitap eder.** İyi isim ayrıca arama-değiştir dostu, telaffuz edilebilir ve yanıltmayan olmalıdır.

# clean-2 | Temel
## Soru
"Fonksiyonlar küçük olmalı" ilkesinin ölçütü aşağıdakilerden hangisidir?
## A
Fonksiyon en fazla üç satır olmalı, uzayan her gövde mutlaka bölünmelidir
🤖 Katı satır sayısı keyfîdir; üç satırda iki iş yapan fonksiyon yine kötüdür.
## B
Fonksiyon en fazla yüz satır olmalı; bu sınırın altındaki her gövde güvenlidir
🤖 Rakam vermek yanıltıcı; 20 satır tek iş yapmıyorsa "sınırın altında" olması işe yaramaz.
## C
Fonksiyon döngü ve iç içe koşul içermemeli, yalnız düz bir akış sunmalıdır
🤖 Döngü yasağı yok; küçüklük denetim yapılarıyla değil sorumlulukla ölçülür.
## D
Fonksiyon tek bir iş yapmalı ve tek bir soyutlama seviyesinde kalmalıdır
🤖 Doğru: küçüklüğün ölçütü satır değil, "tek iş + tek seviye"dir.
## Doğru
D
## Açıklama
"Küçük fonksiyon" ilkesinin ölçütü **satır sayısı değil**, **tek bir şey yapmak** ve **tek bir soyutlama seviyesinde** kalmaktır. Bir fonksiyon hem üst seviye iş akışını (`siparisiOnayla`) hem de düşük seviye ayrıntıyı (byte kaydırma, string biçimleme) karıştırıyorsa, farklı seviyeler iç içe geçtiği için okuması zorlaşır — bu "tek seviye" kuralının ihlalidir. Sabit rakamlar (A: 3, B: 100) keyfîdir ve yanlış güven verir; 2 satırlık bir fonksiyon iki farklı iş yapıyorsa kötü, 30 satırlık bir tanesi tek işi net yapıyorsa iyi olabilir. Döngü yasağı (C) ise ölçütü tamamen yanlış yere koyar; bir döngü tek işin doğal parçası olabilir. Pratik test: fonksiyonu anlamlı biçimde **daha küçük parçalara ayıramıyorsan** (extract method yapacak doğal bir sınır yoksa), muhtemelen zaten tek iş yapıyordur.

# clean-3 | Orta
## Soru
Aynı iş mantığı üç ayrı serviste kopyala-yapıştır tekrar ediyor. Bu hangi kod kokusudur ve neden tehlikelidir?
## A
Yinelenen kod; kural değişince kopyalardan biri kolayca unutulur ve sistem tutarsızlaşır
🤖 Doğru: duplication tek gerçeğin çok yerde yaşamasıdır; değişimde tutarsızlık riski taşır.
## B
Uzun parametre listesi; metotlar çok fazla argüman aldığı için çağrılar okunmaz hâle gelir
🤖 Bu ayrı bir kokudur ve burada argüman sayısından değil, mantığın kopyalanmasından söz ediliyor.
## C
Kıskanç özellik; metot kendi verisinden çok başka bir sınıfın verisiyle uğraşmaktadır
🤖 Feature envy sorumluluğun yanlış sınıfta olmasıdır; burada sorumluluk değil, kodun kendisi çoğaltılmış.
## D
İlkel takıntısı; para ve e-posta gibi iş kavramları hep ilkel tiplerle temsil edilmiştir
🤖 İlkel takıntısı tip modellemesiyle ilgilidir; buradaki dert tekrar eden mantıktır.
## Doğru
A
## Açıklama
Aynı mantığın birden çok yerde kopyalanması **yinelenen kod (duplication)** kokusudur ve genellikle "en zararlı koku" sayılır. Tehlikesi şudur: iş kuralı değiştiğinde (örneğin KDV oranı ya da bir doğrulama koşulu) kuralın **her kopyasını** bulup aynı şekilde güncellemen gerekir; **biri unutulursa** sistem tutarsız davranır ve bu tür hatalar sinsidir — bir yerde düzelir, diğerinde kalır. DRY (Don't Repeat Yourself) ilkesi tam da bunu hedefler: her bilgi parçasının **tek, yetkili bir temsili** olsun. Çözüm ortak mantığı paylaşılan bir metoda, sınıfa ya da domain nesnesine çıkarmaktır. Diğer şıklar gerçek kokuların doğru tanımlarıdır ama bu senaryoya ait değildir: uzun parametre listesi (B) argüman şişkinliği, feature envy (C) sorumluluğun yanlış sınıfta olması, ilkel takıntısı (D) iş kavramlarının `String`/`int` ile temsil edilmesidir. Dikkat: **tesadüfi benzerlik** (aynı görünen ama farklı nedenlerle değişen kod) DRY adına birleştirilmemeli — yanlış soyutlama duplication'dan da pahalıya patlar.

# clean-4 | Orta
## Soru
Karmaşık bir legacy metodu refactor etmeden önce atılması gereken ilk ve en kritik adım nedir?
## A
Metodu silip aynı işi sıfırdan, bu kez temiz bir tasarımla yeniden yazmak
🤖 Sıfırdan yazmak refactoring değil, yüksek riskli bir yeniden yazımdır; gizli iş kuralları kaybolur.
## B
Tüm yerel değişkenleri sınıf düzeyine taşıyıp metotlar arası erişimi kolaylaştırmak
🤖 Paylaşılan değişken durumu artırmak kaliteyi düşürür ve davranışı korumaz.
## C
Mevcut davranışı kilitleyen karakterizasyon testlerini yazıp yeşile almak
🤖 Doğru: refactoring davranışı korumaktır; koruyacak testin yoksa güvenle değiştiremezsin.
## D
Ölü kodu ve yorumları temizleyip metodu görsel olarak kısaltmak
🤖 Görsel kısalma davranışın korunduğunu kanıtlamaz; asıl güvence testtir.
## Doğru
C
## Açıklama
Refactoring'in tanımı **"davranışı değiştirmeden iç yapıyı iyileştirmek"** olduğuna göre, davranışın **korunduğunu kanıtlayacak** bir güvenlik ağına ihtiyaç vardır — bu da **testtir**. Legacy (test edilmemiş) kod için önce mevcut davranışı olduğu gibi yakalayan **karakterizasyon testleri (characterization / golden master)** yazılır ve yeşile alınır; ancak o zaman küçük adımlarla yeniden yapılandırma güvenli olur. Testler olmadan yapılan "iyileştirme" aslında **doğrulanmamış bir davranış değişikliğidir** ve sessizce hata getirebilir. Sıfırdan yeniden yazmak (A) refactoring değildir; eski metodun içinde yıllar içinde birikmiş, kimsenin hatırlamadığı iş kuralları vardır ve testsiz yeniden yazımda bunlar kaybolur. Değişkenleri sınıf düzeyine taşımak (B) paylaşılan durumu artırıp işi zorlaştırır; ölü kod/yorum temizliği (D) faydalı olabilir ama davranışın korunduğuna dair hiçbir kanıt üretmez. Sıra nettir: **testle sabitle → küçük adımla değiştir → yeşil kaldığını doğrula → commit.**

# clean-5 | İleri
## Soru
"İyi kod kendini belgeler, yorum gereksizdir" iddiasına en dengeli senior yaklaşımı hangisidir?
## A
Kesinlikle doğru; her yorum bir başarısızlıktır ve kod tabanındaki tüm yorumlar silinmelidir
🤖 Aşırı uç: "neden" yorumları, dış API tuhaflıkları ve bilinçli ödün notları koddan çıkarılamaz.
## B
Kesinlikle yanlış; yorum ne kadar çoksa kodun bakımı o kadar kolaylaşır ve her satır yorumlanmalıdır
🤖 Diğer aşırı uç: "ne" yorumları gürültü üretir ve kod değişince güncellenmeyip yalan söyler.
## C
Yorum yalnız kıdemsiz geliştiriciler içindir; deneyimli ekipler tamamen yorumsuz kod yazar
🤖 Kıdemle ilgisi yok; bir kararın gerekçesi en deneyimli okuyucuya bile lazımdır.
## D
"Ne" yorumu kötü isimlendirmenin belirtisidir ve kaldırılmalı; "neden" yorumu değerlidir ve kalmalı
🤖 Doğru: yorumu topyekûn övmek ya da reddetmek yerine "ne" ile "neden"i ayırmak dengeli tavırdır.
## Doğru
D
## Açıklama
Olgun görüş, yorumları topyekûn övmek ya da reddetmek değil, **"ne" yorumu ile "neden" yorumunu ayırmaktır**. Kodun **ne yaptığını** tekrarlayan yorumlar (`i++; // i'yi artır`) genellikle kötü isimlendirmenin ya da karmaşık bir ifadenin özrüdür; bunları **kodu netleştirerek** (iyi isim, extract method) ortadan kaldırmak gerekir. Ayrıca bu yorumlar kod değişip yorum güncellenmediğinde **yalan söylemeye** başlar ki yanlış yorum, hiç yorum olmamasından kötüdür. Ama kodun **neden** öyle yazıldığını anlatan yorumlar — bir iş kararının gerekçesi, bir dış kütüphanenin tuhaf davranışına karşı önlem, bilinçli bir performans ödünü, `// TODO`/`// HACK` uyarıları — koddan **çıkarılamayan** bilgi taşır ve çok değerlidir. Her iki uç (A ve B) tek bir kurala indirgeyerek bu ayrımı kaybeder; yorumu kıdeme bağlamak (C) ise konuyu tümden ıskalar. Kısaca: **"neden"i yaz, "ne"yi kodun kendisi söylesin.**

# clean-6 | İleri
## Soru
Bir hata düzeltmek için açtığın dosyada, konuyla ilgisiz ama kolayca düzeltilebilir kötü bir isimlendirme fark ettin. Boy Scout Kuralı'nın önerdiği davranış ve sınırı nedir?
## A
Tüm dosyayı baştan sona yeniden yaz; madem açtın, biriken tüm eksikleri birlikte gider
🤖 Büyük yeniden yazım kuralın kapsamı değil; devasa, riskli ve gözden geçirilmesi zor bir diff üretir.
## B
Küçük ve güvenli bir iyileştirme yap; kapsamı gözden geçirilebilir sınırda tut
🤖 Doğru: kuralın ruhu küçük ve sürekli iyileştirme, sınırı ise kapsamı denetim altında tutmaktır.
## C
Hiçbir şeye dokunma; sahibi olmadığın kodu değiştirmek her zaman gereksiz risk üretir
🤖 Bu, çürümeyi besleyen pasif tutum; Boy Scout Kuralı tam tersini önerir.
## D
Düzeltmeyi backlog'a yaz; temizlik işleri ayrı bir sprint'te toplu hâlde yapılmalıdır
🤖 Toplu temizliğe ertelemek kuralın amacına aykırı; küçük iyileştirme "şimdi ve burada" yapılır.
## Doğru
B
## Açıklama
Boy Scout Kuralı — "kampı bulduğundan **biraz daha temiz** bırak" — kod çürümesine (rot) karşı **sürekli ve küçük** bir bakım disiplinidir: her dokunduğun dosyayı ufak bir iyileştirmeyle (bir ismi düzelt, bir sihirli sayıyı sabite çıkar, küçük bir metot ayıkla) geride bırakırsın. Ama kuralın **sınırı da vardır**: iyileştirme **küçük, güvenli ve gözden geçirilebilir** kalmalı. Tüm dosyayı yeniden yazmak (A) hata düzeltmesiyle karışan devasa bir diff üretir, code review'ı imkânsızlaştırır ve riski artırır — bu artık "boy scout" değil, ayrı bir refactoring işidir ve **kendi PR'ında** yapılmalıdır. Hiç dokunmamak (C) çürümeyi besler; her temizliği toplu bir sprint'e ertelemek (D) ise pratikte hiç yapılmayan bir "büyük temizlik" hayaline dönüşür ve teknik borcun faizini biriktirir. Denge: **konuyla ilgili ve küçük** olanı hemen yap, büyük olanı ayır.
