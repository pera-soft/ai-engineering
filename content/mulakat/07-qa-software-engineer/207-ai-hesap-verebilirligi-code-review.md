---
slug: ai-hesap-verebilirligi-code-review
baslik: "AI Hesap Verebilirliği ve Code Review Kültürü"
kademe: Orta
sira: 207
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Vaka çalışmasının (case study) 1.4 kuralı şunu söylüyor: kod asistanları (Cursor, Claude Code, Copilot ve benzerleri) serbesttir, hatta kullanımı teşvik edilir. Ancak üretilen her satırın sahibi sensin.

Kural üç şeyi zorunlu tutuyor. Değişiklik isteğinin (PR) açıklamasında hangi bölümlerin yapay zekâ ile üretildiğini yazacaksın. Yapay zekânın çıktısında neyi değiştirdiğini yazacaksın. Ve neden değiştirdiğini yazacaksın. Ayrıca demo sırasında koddan rastgele seçilecek bir bölümü satır satır açıklaman istenecek.

Bu bir dipnot değildir. Değerlendirme ölçütlerinde **%15 ağırlıkla** ayrı bir başlıktır: "AI hesap verebilirliği ve kod incelemesi". Ölçülen şeyler şunlar: PR'daki yapay zekâ açıklamasının dürüstlüğü ve derinliği, REVIEW.md bulgularının isabeti ve kalitesi, demo performansı. Yani bu eksen, kodun çalışıp çalışmadığından **bağımsız olarak** ölçülür.

### Temel kavramlar

- **PR açıklaması zorunludur**: hangi bölümler yapay zekâ ile üretildi, ne değiştirildi ve **neden** değiştirildi. Üçü de zorunludur, isteğe bağlı değildir.
- **REVIEW.md dört ayrı bölümden oluşur.** (a) *Bilinçli tasarım kararları*: göreli (delta) doğrulama, dinamik ürün seçimi, mükerrer adım yazılmaması. (b) *İncelemede bulunan ve düzeltilen 10 hata*: fiyat ayrıştırma, sepete eklemenin sessiz başarısızlığı, `After` kancasındaki bağlam sızıntısı, Allure biçimlendiricisinin sessizce düşmesi ve diğerleri. (c) *Hâlâ açık 6 zayıflık*. Bunlar sırasıyla şunlardır. Arama için seçilen %70 eşiği deneyseldir. Negatif giriş senaryosu gerçek hesabı kullanır. Sepete ekleme tekrarı çift ekleme riski taşır. Uçtan uca testler paylaşımlı CI makinesinde koşamaz (CloudFront engeli). İzleme kaydı her senaryoda toplanır. Sepet satırı seçimi sıra numarasına dayanır. (d) *Yapılmayanlar*: Docker imajının koşum doğrulaması ve API'nin daha geniş kullanımı.
- **Somut yapay zekâ hatası kategorileri** (demo notlarından alınmış, gerçek): uydurma veya tahmine dayalı seçiciler, vaka metnindeki varsayımlara güvenen senaryo tasarımı, yasaklı API kullanımı (`waitForTimeout`), paralel koşumu hesaba katmayan yaklaşım (karakterleri tek tek yazma).
- **"Bilmiyorum" demek uydurmaktan iyidir**: rastgele bir satır sorulduğunda hatırlamamak, yanlış veya uydurma cevap vermekten her zaman daha iyi bir sinyaldir.

```svg
<svg viewBox="0 0 640 218" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REVIEW.md dort bolum: bilincli tasarim, bulunup duzeltilen, hala acik, yapilmayanlar">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">REVIEW.md: dört bölüm, hiçbiri gizlenmez</text>
  <rect x="20" y="42" width="190" height="120" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="115" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Bilinçli tasarım</text>
  <text x="115" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">göreli doğrulama</text>
  <text x="115" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dinamik ürün seçimi</text>
  <text x="115" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">mükerrer step yok</text>
  <rect x="225" y="42" width="190" height="120" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Bulunup düzeltilen (10)</text>
  <text x="320" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">fiyat parse hatası</text>
  <text x="320" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">context sızıntısı</text>
  <text x="320" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Allure formatter düşmesi</text>
  <rect x="430" y="42" width="190" height="120" rx="11" fill="#F3E1E1" stroke="#B5504F" stroke-width="1.5"/>
  <text x="525" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B5504F" font-family="system-ui">Hâlâ açık (6)</text>
  <text x="525" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">arama eşiği deneysel</text>
  <text x="525" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">negatif login gerçek hesap</text>
  <text x="525" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">trace her senaryoda toplanıyor</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">4. bölüm: Yapılmayanlar — Docker koşum doğrulaması, API'nin daha geniş kullanımı</text>
  <text x="320" y="202" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"her şey mükemmel" değil — ayrı ayrı, adıyla</text>
</svg>
```

### Nasıl çalışır?

**Neden bölümler ayrı tutuluyor?** Bulunup düzeltilen hatalar zaten kapatılmıştır; hâlâ açık zayıflıklar ise kapatılmamıştır. İkisini tek listede eritmek, hatta "hâlâ açık" bölümünü hiç yazmamak daha temiz görünebilirdi.

Ama doğrusu tam tersidir. Bir incelemenin hiç kusur bulamaması iki anlama gelir: ya gerçekten kusursuzluk vardır, ya da yeterince derin bakılmamıştır. İkincisi çok daha olasıdır.

Adı konmuş altı açık zayıflık ise ters yönde bir sinyal verir. "Arama sonucu için seçilen %70 eşiği deneyseldir", "negatif giriş senaryosu gerçek hesabı kullanır", "izleme kaydı her senaryoda toplanır" gibi maddeler derinlemesine bakıldığını gösterir. Ayrıca bulunanın saklanmadığını gösterir.

Dördüncü bölüm olan "Yapılmayanlar" ayrı tutulur. Çünkü oradakiler kusur değildir; bilerek kapsam dışı bırakılmış işlerdir. Docker imajının koşum doğrulaması ve API'nin daha geniş kullanımı böyledir. Bunların zayıflıkla karıştırılmaması gerekir. Bu yapı, "her şey mükemmel" iddiasından çok daha güçlü bir dürüstlük kanıtıdır.

**Dört somut yapay zekâ hatası ve mühendisin doğrulama rolü.** Demo notları, yapay zekâ ile üretilen ilk kod tabanının **çalışmadığını** açıkça söylüyor. Sebep basit: yapay zekâ siteyi bilmiyordu. Değiştirilenler şunlar:

1. **Seçiciler.** İlk üretilen seçiciler tahmine dayalıydı. Hesap menüsü gibi bölümler için uydurulmuş id değerleri vardı ve bunlar siteyle uyuşmuyordu. Düzeltme: her birini gerçek tarayıcıda incelemek ve gerçek id, rol veya sınıf değerleriyle değiştirmek.
2. **Senaryo tasarımı.** İlk taslak, vaka metnindeki varsayımlara göre yazılmıştı: e-posta ve şifreyle giriş, bir de "boş sonuç mesajı". Sitede ikisi de yoktu. Giriş telefon ve şifreyle iki adımlıdır. Arama ise hiçbir zaman boş sonuç döndürmez; en anlamsız terimde bile "665 ürün bulundu" der. Düzeltme: senaryoları gözlemlenen gerçek davranışa göre yeniden yazmak.
3. **Bekleme stratejisi.** Yapay zekânın ürettiği ilk yardımcı fonksiyon `waitForTimeout` içeriyordu. Vaka bunu açıkça yasaklıyordu. Düzeltme: koşul tabanlı beklemeye çevirmek (`expect.poll`, görünürlük kontrolü).
4. **Paralel koşumun hesaba katılmaması.** Telefon alanına rakamları tek tek yazan yaklaşım (`pressSequentially`) seri koşumda sorunsuzdu. Paralel koşumda ise haneler karışıyordu, çünkü maskeli alanın imleç konumlandırmasıyla yarışıyordu. Düzeltme: değeri tek işlemde yazmak (`fill`) ve değerin forma işlendiğini doğrulamak.

Ortak desen şudur: yapay zekânın çıktısı **makul görünür**, ama projenin özel kısıtlarını bilmez. Bu kısıtlar vakanın yasakları, sitenin gerçek davranışı ve koşumun gerçek koşullarıdır. Mühendisin işi üretmek değil **doğrulamaktır**: gözlemlemek, karşılaştırmak ve gerçek koşum koşullarında sınamak.

**"Gerçekten sen mi yazdın?" sorusunun doğru çerçevesi.** Sahiplik, hangi elin hangi karakteri yazdığıyla ölçülmez. Doğru cevap şuna benzer: "Üretimin büyük kısmını yapay zekâ ile yaptım ve bunu PR açıklamasında da yazdım. Ama doğrulama, düzeltme ve tasarım kararları bana ait."

Sahiplik burada üç yetenektir: doğrulama, tasarım kararı ve herhangi bir satırı talep üzerine açıklayabilme. "Neyi yanlış yaptın?" sorusunun cevabı da aynı derse çıkar: "Başta vaka metnindeki varsayımlara güvenip senaryoları öyle yazmaya başladım. Siteyi önce incelemem gerekirdi." Ders şu: yapay zekânın çıktısına güvenmeden önce gözlemle.

**"Bilmiyorum" demek uydurmaktan iyidir.** Demo sırasında rastgele bir satır seçilip açıklanması istenebilir. Önerilen sıra şu: kodu yüksek sesle oku, niyeti anlat, hatırlamıyorsan dürüstçe söyle ve fonksiyonun genel amacını anlat.

Asla uydurma. Çünkü uydurma cevap iki şeyden birini ima eder: ya kodu yazıp anlamamışsındır, ya da baskı altında güvenilmez cevaplar veriyorsundur. İkisi de, yapay zekâ çıktısını inceleyecek bir mühendis için ciddi uyarı işaretidir.

### Mülakatta ne sorulur?

- **"Bu kodu gerçekten sen mi yazdın?"** Beklenen: üretimin büyük kısmı AI, ama doğrulama/düzeltme/tasarım kararları mühendisin; herhangi bir satırı talep üzerine açıklayabilme.
- **"REVIEW.md'de hâlâ açık zayıflıklar bölümü neden var, neden hepsini düzeltip kapatmadın?"** Beklenen: dürüstlük sinyali; "her şey mükemmel" demekten daha güçlü, çünkü gerçek sınırları adıyla gösteriyor.
- **"AI'nin ürettiği selector'larla ilgili ne sorun çıktı?"** Beklenen: tahmine dayalı/uydurma id'ler siteyle uyuşmuyordu, tarayıcıda doğrulanıp gerçek değerlerle değiştirildi.
- **"Case'in yasakladığı bir API AI tarafından mı üretildi?"** Beklenen: evet, `waitForTimeout`; case'in yasağı nedeniyle koşul tabanlı beklemeye çevrildi.
- **"Rastgele bir satır sorulursa ve hatırlamıyorsan ne yaparsın?"** Beklenen: uydurmak yerine dürüstçe söylemek, sonra fonksiyonun/satırın amacını genel hatlarıyla anlatmak.
- **"Neyi yanlış yaptın?"** Beklenen: case metnindeki varsayımlara (e-posta/şifre girişi gibi) siteyi incelemeden güvenmek; ders, gözlemin varsayımdan önce gelmesi gerektiği.

### Tek cümlede

AI hesap verebilirliği "yapay zekâ mı yazdı?" sorusuna cevap aramaz. Şunu sorar: sen doğruladın mı, neden değiştirdin, hâlâ neyin eksik olduğunu biliyor musun? REVIEW.md'nin dört bölümü ayrı tutması — bilinçli taviz, düzeltilen hata, açık zayıflık, yapılmayanlar — bu hesap verebilirliğin somut kanıtıdır. "Bilmiyorum" demeyi uydurmaya tercih etmek de öyle.

# aiacc-1 | Orta
## Soru
Case study'nin 1.4 kuralı "Kod asistanları serbesttir... ama üretilen her satırın sahibi sizsiniz" diyor ve PR açıklamasında AI ile üretilen bölümlerin, neyin değiştirildiğinin ve NEDEN değiştirildiğinin açıklanmasını zorunlu kılıyor. Bu kural en doğru biçimde nasıl yorumlanır?
## A
AI araçlarının kullanımını dolaylı biçimde yasaklıyor, PR'da AI'dan bahsedilmesi aslında caydırıcı bir uyarı
🤖 Kural tam tersini söylüyor: "kullanımı teşvik edilmektedir"; yasak değil, şeffaflık zorunluluğu var.
## B
AI kullanımı serbest, ama mühendis her satırı savunmak ve neyi neden değiştirdiğini anlatmak zorunda
🤖 Doğru: kural AI'yı yasaklamıyor, hesap verebilirliği (üretilen koddan mühendisin sorumlu olmasını) zorunlu kılıyor.
## C
Yalnızca AI'nin ürettiği kısımların PR'da işaretlenmesi yeterli, değişiklik gerekçesi isteğe bağlı bir ayrıntı
🤖 Kural açıkça "neyi değiştirdiğinizi ve NEDEN değiştirdiğinizi açıklamanız zorunludur" diyor; gerekçe isteğe bağlı değil.
## D
Kural yalnızca demo sırasında rastgele bir satırın açıklanmasını kapsıyor, PR açıklamasıyla ilgisi yok
🤖 Kural hem PR açıklamasını hem demo'daki satır açıklamasını birlikte istiyor; ikisi ayrı değil aynı hesap verebilirlik gerekliliğinin parçası.
## Doğru
B
## Açıklama
Kuralın iki cümlesi birlikte okunmalı: AI kullanımı serbest ve teşvik ediliyor (yasak yok), ama "üretilen her satırın sahibi sizsiniz" — yani mühendis kodu savunmakla, hangi kısmı neden değiştirdiğini somut olarak anlatmakla yükümlü. Bu, değerlendirme kriterlerinde ayrı ve %15 ağırlıklı bir başlık olarak da karşımıza çıkıyor: "PR'daki AI açıklamasının dürüstlüğü ve derinliği." Kural bir yasak (A) değil, bir şeffaflık zorunluluğudur; gerekçe isteğe bağlı bir ayrıntı değildir (C); PR açıklaması ile demo'daki satır açıklaması aynı gerekliliğin iki farklı anıdır (D yanlış, ayrı değil).

# aiacc-2 | Orta
## Soru
REVIEW.md dört bölüme sahip: bilinçli tasarım kararları, incelemede bulunup düzeltilen 10 hata, hâlâ açık 6 zayıflık ve "Yapılmayanlar". Düzeltilen hatalar zaten kapatıldığına göre, "hâlâ açık zayıflıklar" bölümünü ayrıca tutmanın değeri nedir?
## A
Bu bölüm yalnızca gelecekteki bir sprint planlaması için tutulan referans listesi; mülakat değerlendirmesiyle doğrudan ilgisi yok
🤖 Case study'nin %15 ağırlıklı kriteri doğrudan "REVIEW.md bulgularının isabet ve kalitesini" değerlendiriyor; bölüm mülakatla doğrudan ilgili.
## B
Zayıflıkları listelemek, ekibin bu hataları asla düzeltmeyeceğini önceden ilan etmek anlamına gelir
🤖 Listelemek bir taahhüt değil, mevcut durumun dürüst tarifi; "asla düzeltilmeyecek" anlamı çıkarılamaz.
## C
"Her şey mükemmel" demek yerine gerçek sınırları adıyla söylemek, daha güçlü bir dürüstlük ve derinlik sinyali verir
🤖 Doğru: bir raporun kusur bulamaması ya gerçekten kusursuzluk ya da yetersiz inceleme anlamına gelir; ikincisi çok daha olasıdır ve adlandırılmış zayıflıklar tam tersi bir sinyal — derinlemesine bakıldığını gösterir.
## D
Zayıflıkları ayrı tutmak yalnızca REVIEW.md dosyasının okunabilirliğini artırır, biçimsel bir tercihtir
🤖 Biçim faydası yan etkidir; asıl amaç değerlendirici için dürüstlük ve derinlik sinyali üretmektir.
## Doğru
C
## Açıklama
Bir gözden geçirmenin hiçbir zayıflık bulamaması iki ihtimalden birine işaret eder: proje gerçekten kusursuzdur (nadiren doğru) ya da inceleme yeterince derin yapılmamıştır (çok daha olası). "Arama sonucu %70 eşiği deneysel", "negatif giriş gerçek hesabı kullanıyor" gibi somut, adı konmuş zayıflıklar tam tersini kanıtlar: yazar projeyi eleştirel biçimde incelemiş ve bulduğunu saklamamıştır. Case study'nin değerlendirme kriteri de bunu doğrudan ölçüyor — "REVIEW.md bulgularının isabet ve kalitesi." Bu bölüm bir taahhüt (B) değil bir durum tespitidir; biçimsel okunabilirlik (D) yan etkidir, asıl amaç değildir.

# aiacc-3 | Orta
## Soru
Demo notlarına göre AI'nin ürettiği ilk kod tabanında hesap menüsü gibi bazı elementler için "uydurulmuş id'ler" vardı ve siteyle uyuşmuyordu. Bu bulgunun kök nedeni ve düzeltme yaklaşımı nedir?
## A
AI, id'leri kasıtlı olarak yanlış üretti çünkü siteye erişimi yoktu ve rastgele değer döndürdü
🤖 "Kasıtlı yanlış üretim" doğru çerçeve değil; AI olası görünen ama gözlemlenmemiş değerler üretti — bu farklı bir mekanizma.
## B
Sorun id'lerde değil, dinamik üretilen CSS sınıf isimlerindeydi; id değerleri baştan doğruydu
🤖 Belgelenen bulgu doğrudan id'lerle ilgili ("uydurulmuş id'ler"), CSS sınıflarıyla değil.
## C
Düzeltme, id yerine XPath zincirine geçilerek yapıldı, böylece selector kırılganlığı tamamen ortadan kalktı
🤖 Proje tam tersini tercih ediyor: XPath zincirine ihtiyaç duyulmadığı, id/rol tabanlı stratejinin yeterli olduğu belgelenmiş.
## D
AI siteyi incelemeden olası görünen locator'lar üretti; mühendis hepsini tarayıcıda doğrulayıp değiştirdi
🤖 Doğru: AI çıktısı "tahmine dayalı"ydı çünkü siteyi bilmiyordu; doğrulama ve düzeltme adımı mühendisin sorumluluğuydu.
## Doğru
D
## Açıklama
Demo notlarındaki ifade net: "İlk üretilen locator'lar tahmine dayalıydı — mesela hesap menüsü için uydurulmuş id'ler vardı, siteyle uyuşmuyordu. Hepsini tarayıcıda inceleyip gerçek olanlarla değiştirdim." AI, siteyi ziyaret edip gözlemlemeden, benzer projelerde sık görülen isimlendirme kalıplarına dayanarak "olası" locator'lar üretmiştir — bu kasıtlı bir yanlış üretim değildir (A), gözlem eksikliğidir. Düzeltme id yerine XPath'e geçmek değil (proje zaten XPath'e ihtiyaç duymadığını belgeliyor, C yanlış), gerçek id/rol/sınıf değerlerini tarayıcıda doğrulayıp yerine koymaktır.

# aiacc-4 | Orta
## Soru
AI'nin ürettiği ilk bekleme yardımcı fonksiyonu `waitForTimeout` içeriyordu; case study bu API'yi açıkça yasaklıyor. Bu bulgu ne anlatıyor?
## A
AI, projenin kendi kısıtlarını bilmeden yaygın bir kalıp üretebilir; mühendisin görevi bunu kurallara göre denetlemektir
🤖 Doğru: `waitForTimeout` yaygın ama kırılgan bir kalıptır; case'in özel kısıtını bilmeyen AI çıktısı, mühendis tarafından kural bilgisiyle düzeltildi.
## B
`waitForTimeout` aslında case tarafından yasaklanmamıştı, düzeltme gereksiz bir temkinlilikti
🤖 Case açıkça bu API'yi yasaklıyor; düzeltme gereksiz değil, kurala uyum için zorunluydu.
## C
Sorun yalnızca fonksiyon isimlendirmesindeydi; `waitForTimeout` kendi adıyla değil, bir yardımcı içine sarmalanarak bırakılabilirdi
🤖 Sorun isimlendirme değil, sabit süreli bekleme kalıbının kendisiydi; isim değişse davranış aynı kalırdı.
## D
AI bu API'yi hiç kullanmadı, bulgu proje geçmişinde hiç yaşanmamış kurgusal bir örnek
🤖 Bulgu gerçek: demo notlarında AI'nin ilk ürettiği yardımcı fonksiyonun `waitForTimeout` içerdiği açıkça belirtiliyor.
## Doğru
A
## Açıklama
AI, projeye özgü kısıtları (case metninin `waitForTimeout`/`sleep` yasağı gibi) kendiliğinden bilmez; genel yazılım pratiğinde yaygın ama bu proje için kabul edilemez bir kalıp üretmiştir. Mühendisin katkısı burada üretim değil **denetimdir**: kod tabanında `waitForTimeout` veya `sleep` geçmediği iddia edilebilir hale gelmiştir, çünkü sabit süreli bekleme koşul tabanlı bir yapıya (`expect.poll`, görünürlük kontrolü) çevrilmiştir. İsim değişikliği (C) veya yasağın var olmadığı iddiası (B) sorunu çözmez; bulgunun kendisi de kurgusal değildir (D).

# aiacc-5 | Orta
## Soru
AI'nin ürettiği bir yardımcı, telefon alanına rakamları tek tek yazıyordu (`pressSequentially`). Bu kod seri koşumda sorunsuz çalışıyordu ama paralel koşumda haneler karışıyordu. Bu bulgunun mühendislik dersi nedir?
## A
AI'nin ürettiği kod her zaman paralel koşumda test edilmeli, seri koşumda test etmek anlamsızdır
🤖 Aşırı genelleme; ders "her zaman paralel test et" değil, kodun gerçek koşum koşullarında (bu projede paralel) doğrulanması gerektiği.
## B
Kod tek bir koşum biçiminde doğru görünse bile projenin gerçek koşum koşulunda (paralel worker) farklı davranabilir
🤖 Doğru: seri koşumda geçen bir kod, paralel koşumdaki gerçek riski (maskeli alanla yarışan tek-tek yazma) gizliyordu; doğrulama gerçek koşum koşuluna göre yapılmalı.
## C
Sorun AI ile ilgisizdi, aynı hata insan tarafından yazılsa da ortaya çıkardı; bu yüzden AI hesap verebilirliğiyle bağlantısı yok
🤖 Hatanın kaynağı (insan/AI) ayrı bir tartışma; burada önemli olan mühendisin bunu AI çıktısında bulup düzeltmiş olması — hesap verebilirliğin tam da bu doğrulama adımı olması.
## D
`pressSequentially` Playwright'ta zaten kullanımdan kaldırılmış bir API olduğu için hata kaçınılmazdı
🤖 `pressSequentially` geçerli bir Playwright API'sidir; sorun API'nin kaldırılmışlığı değil, maskeli alanla yarışması.
## Doğru
B
## Açıklama
Kod, tek karakter tek karakter yazan (`pressSequentially`) bir yaklaşımla seri koşumda hatasız çalışıyordu — ama projenin gerçek çalışma biçimi iki worker'lı paralel koşumdur, ve o koşulda maskenin imleç konumlandırmasıyla yarışıp haneler yer değiştiriyordu (`5551234567` yerine `5551234657` gibi). Ders şu: "çalışıyor" ifadesi hangi koşulda test edildiğine bağlıdır; mühendis AI çıktısını yalnız ilk denemede geçti diye kabul etmemeli, projenin **gerçek** koşum koşullarında (burada paralel) sınamalıdır. Düzeltme, değeri tek işlemde yazmak (`fill`) ve tıklamadan önce alandaki değerin forma işlendiğini doğrulamaktı. AI/insan ayrımı (C) burada konu dışıdır; API'nin geçerliliği (D) sorunun kaynağı değildir.

# aiacc-6 | Orta
## Soru
Demo sırasında kodda rastgele bir satır seçilip satır satır açıklaman istenirse ve o satırı tam hatırlamıyorsan, önerilen yaklaşım nedir?
## A
Satırı hatırlıyormuş gibi makul bir açıklama üretip mülakatı akıcı tutmak, küçük ayrıntı hatası önemli değildir
🤖 Uydurma cevap, "bilmiyorum" cevabından daha kötü kabul ediliyor; akıcılık dürüstlüğün önüne geçmemeli.
## B
Konuyu değiştirip başka bir bölüme geçmeyi teklif etmek, o satırın önemsiz olduğunu ima etmek
🤖 Konu değiştirmek soruyu yanıtsız bırakır ve hazırlıksızlık izlenimi güçlendirir; önerilen yaklaşım değil.
## C
Dürüstçe "şu an tam hatırlamıyorum" demek, sonra fonksiyonun amacını genel hatlarıyla anlatmak
🤖 Doğru: "bilmiyorum" cevabı yanlış cevaptan iyidir; niyeti anlatmak hafızayı telafi eder, uydurma güveni kalıcı biçimde sarsar.
## D
Sessiz kalıp mülakatçının o satırı geçip başka bir soruya yönelmesini beklemek
🤖 Sessizlik bir cevap değildir ve hazırlıksız görünmeyi güçlendirir; en azından niyeti anlatan bir cevap tercih edilmeli.
## Doğru
C
## Açıklama
Önerilen sıra üç adımdır: kodu yüksek sesle oku, "buradaki amaç şu" diye niyeti anlat, ve eğer bilmiyorsan "bu satırı şu an tam hatırlamıyorum, ama fonksiyonun amacı şu" de. Asla uydurma — "bilmiyorum" cevabı yanlış cevaptan iyidir. Bunun sebebi teknik değil güvenle ilgilidir: bir mülakatçı, adayın AI çıktısını gözden geçirirken de aynı basıncı yaşayacağını bilir; "emin değilim" diyebilen biri, emin olmadığı halde ikna edici konuşan birinden daha güvenilirdir. Akıcı ama uydurma bir cevap (A), konu değiştirmek (B) veya sessizlik (D) bu güveni kurmaz.
