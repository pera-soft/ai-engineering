---
slug: ai-augmented-development
baslik: "AI-Destekli Geliştirme: İş Akışı, Verimlilik Yanılgısı, İnceleme Sorumluluğu ve Güvenlik"
kademe: İleri
sira: 175
etiketler: 7
---

## Konu anlatımı

### Bu ne işe yarar?

Usta bir marangozun eline çok hızlı bir elektrikli testere verdiğini düşün. Testere kesimi saniyeler içinde yapar; ama nereden keseceğine marangoz karar verir ve masa yamuk çıkarsa hesabı müşteriye marangoz verir. Yapay zekâ destekli geliştirme (AI-augmented development) tam olarak böyledir. Bilgisayara "şu işi yapan kodu yaz" diye yazarsın ve birkaç saniyede bir kod önerisi gelir. Ama o kodu projeye koyan, doğruluğundan ve güvenliğinden sorumlu olan yine sensin. Araç taslağı hızlandırır; kararı ve imzayı devralmaz.

### Temel kavramlar

- **Büyük dil modeli (LLM — Large Language Model)**: Devasa miktarda metinle beslenerek "bu metinden sonra hangi parçanın gelme ihtimali yüksek?" sorusunu cevaplamayı öğrenmiş bir program. Anlamaz, **tahmin eder**; bu yüzden çok emin bir tonla yanlış da yazabilir.
- **Token (jeton)**: Modelin metni işlerken kullandığı en küçük parça — kabaca bir kelime veya kelime parçası. Modele giden ve modelden gelen her şey token olarak sayılır; ücret ve hız bu sayıdan hesaplanır.
- **Prompt (istem)**: Modele verdiğin yazılı talimat ve ona eklediğin bilgi. Bir fonksiyonun girdisi gibidir.
- **Kod asistanı**: Bir LLM'i editöre/IDE'ye bağlayan araç (Copilot, Cursor, Claude Code gibi). Açık dosyaları ve yazmakta olduğun satırları kendiliğinden prompt'a katar.
- **Uydurma (hallucination / halüsinasyon)**: Modelin uydurduğu, doğruymuş gibi duran bilgi. Koddaki karşılığı, var olmayan bir kütüphane metodunu çağırmaktır.
- **Yapay zekâ döküntüsü (AI slop)**: Gözden geçirilmeden biriken, ilk bakışta makul görünen ama gereksiz, tutarsız veya hatalı çıktı yığını.
- **İnsan-döngüde doğrulama (human-in-the-loop)**: Her çıktının üretime girmeden önce bir insan kapısından geçmesi.

### Nasıl çalışır?

1. Niyetini yazarsın; ya da kod yazarken asistan çevredeki dosyaları okuyup bağlamı kendisi toplar.
2. Model, eğitimindeki kalıplara bakarak en olası devamı üretir. Doğruluğu değil **olasılığı** hesapladığı için çıktı akıcı ve kendinden emin görünür — akıcılık, doğruluk kanıtı değildir.
3. Sen okur, çalıştırır, test eder, güvenlik açığına bakarsın. İnsan-döngüde doğrulama adımı tam da budur.
4. Kabul edersen commit senin adınla gider: hata da, güvenlik açığı da, bakım yükü de senin sorumluluğundadır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="AI taslak üretir, mühendis sorumlu kalır">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Asistan taslak verir, sorumluluk mühendiste kalır</text>
  <rect x="26" y="52" width="180" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">AI Asistan</text>
  <text x="116" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">hızlı taslak üretir</text>
  <text x="116" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">akıcı ama denetimsiz</text>
  <text x="116" y="150" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">"AI slop" riski</text>
  <path d="M212 112 L266 112" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M266 112 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="272" y="52" width="176" height="120" rx="12" fill="#FBEEE6" stroke="#C8722B" stroke-width="1.5"/>
  <text x="360" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#C8722B" font-family="system-ui">İnceleme Kapısı</text>
  <text x="360" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">doğruluk · güvenlik</text>
  <text x="360" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">telif · test</text>
  <text x="360" y="150" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">insan gözü zorunlu</text>
  <path d="M454 112 L508 112" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M508 112 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="514" y="52" width="112" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="570" y="80" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Teslim</text>
  <text x="570" y="104" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">değer</text>
  <text x="570" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">+ imza sende</text>
</svg>
```

### Mülakatta ne sorulur?

**"Asistanın faydasını nasıl ölçersin?"** Kabul edilen öneri sayısı veya üretilen satır sayısı yanıltıcı ölçütlerdir: kod bir varlık değil **yükümlülüktür** (liability); her satır test, inceleme ve bakım maliyeti getirir. Anlamlı ölçütler sonuç odaklıdır: özellik başına teslim süresi (lead time), üretime kaçan hata oranı ve incelemede yakalanan sorun oranı. Satır saymak "daha çok kod = daha çok değer" gibi tehlikeli bir teşvik yaratır ve döküntü birikimini gizler.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kod bir varlık değil yükümlülüktür: satır saymak yerine sonuç ölç">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kod bir varlık değil, yükümlülüktür</text>
  <rect x="28" y="48" width="250" height="132" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="153" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Yanıltıcı ölçüt</text>
  <text x="153" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">kabul edilen öneri sayısı</text>
  <text x="153" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">üretilen satır sayısı</text>
  <text x="153" y="140" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">çok kod → çok bakım yükü</text>
  <text x="153" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"AI slop"u gizler</text>
  <path d="M286 114 L326 114" stroke="#5D7789" stroke-width="2" fill="none"/>
  <path d="M326 114 l-9 -5 l0 10 z" fill="#5D7789"/>
  <rect x="330" y="48" width="282" height="132" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="471" y="72" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Anlamlı ölçüt (sonuç)</text>
  <text x="471" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">teslim (lead) süresi</text>
  <text x="471" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">üretim hata (defect) oranı</text>
  <text x="471" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">inceleme etkinliği</text>
  <text x="471" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">teslim edilen değeri ölçer</text>
</svg>
```

**"Üretkenlik iddialarına ne diyorsun?"** Satıcı sunumları "%50 daha hızlı" der. 2025'te yapılan kontrollü bir ölçümde (METR çalışması), tanıdıkları kod tabanında çalışan deneyimli açık kaynak geliştiricileri asistanla işlerini **daha yavaş** bitirmiş, buna rağmen kendilerini daha hızlı sanmıştır. Ders nettir: **algılanan hız, gerçek hız değildir**; kendi ekibinde ölçmeden hiçbir iddiaya inanma.

**"Güvenlik tarafı ne durumda?"** Veracode'un 2025 raporu, güvenlik-duyarlı görevlerde üretilen kodun yaklaşık **%45'inin** bilinen bir güvenlik açığı içerdiğini ve daha büyük/yeni modellerin bu oranı belirgin biçimde düşürmediğini ölçtü. En klasik örnek, kullanıcı girdisinin string birleştirmeyle SQL cümlesine sokulmasıdır (SQL injection); doğrusu `PreparedStatement` ile parametreli sorgudur. Sonuç şudur: yapay zekâ çıktısı **varsayılan olarak güvensiz** kabul edilir. Statik kod analizi (SAST), bağımlılık taraması ve insan eliyle güvenlik incelemesi pazarlık konusu olmayan kapılardır. Ayrıca telif ve lisans riski de vardır; çıktı, eğitim verisindeki lisanslı koda benzeyebilir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Veracode 2025: üretilen kodun yaklaşık yüzde 45i güvenlik açığı içerir, model büyüklüğü çözmez">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Veracode 2025: model büyüklüğü açığı çözmez</text>
  <line x1="80" y1="178" x2="580" y2="178" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="80" y1="68" x2="580" y2="68" stroke="#8A2E2E" stroke-width="1.5" stroke-dasharray="6 5"/>
  <text x="574" y="60" text-anchor="end" font-size="10.5" fill="#8A2E2E" font-family="system-ui">~%45 açık — sabit hat</text>
  <rect x="150" y="68" width="120" height="110" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="210" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">%45</text>
  <text x="210" y="196" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Küçük / eski model</text>
  <rect x="390" y="68" width="120" height="110" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="450" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">%45</text>
  <text x="450" y="196" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Büyük / yeni model</text>
</svg>
```

**"Ölçekte ne bozulur?"** Üretim ucuzlayınca darboğaz insan incelemesine kayar. Doğru cevap "incelemeyi kaldıralım" değil, **üretim hızını incelenebilir hacme sınırlamak** ve kapıları (test, statik analiz, güvenlik taraması) otomatikleştirmektir. AI çıktısını, işe yeni başlamış bir geliştiricinin PR'ı gibi ele al: hızlıdır, ama denetlenmesi şarttır.

### Tek cümlede

AI kod asistanı hızlı bir taslak makinesidir; kazandırdığı hız, çıktısını insan gözüyle süzebildiğin oranda gerçektir — imza her zaman mühendisindir.

# aiaug-1 | Orta
## Soru
Bir ekip lideri, "AI asistanının verimliliğini" ölçmek için metrik seçiyor. Aşağıdakilerden hangisi EN yanıltıcı metriktir?
## A
Kabul edilen (accepted) AI önerisi satır sayısı
🤖 Doğru: üretilen/kabul edilen satır sayısı üretkenlik değil bakım yükü demektir; çok kod iyi kod anlamına gelmez.
## B
Özellik başına teslim (lead) süresi
🤖 Bu anlamlı bir sonuç metriğidir; teslim hızını doğrudan ölçer, yanıltıcı değildir.
## C
Üretime çıkan hata (defect) oranı
🤖 Kalite sonucunu ölçer; AI kullanımının gerçek etkisini görmek için geçerli bir metriktir.
## D
İnceleme (review) sırasında yakalanan sorun oranı
🤖 Sürecin sağlığını gösteren makul bir metrik; satır saymaktan çok daha anlamlıdır.
## Doğru
A
## Açıklama
"Kabul edilen satır" veya "üretilen kod miktarı" klasik **verimlilik yanılgısıdır**. Kod bir varlık değil, bir yükümlülüktür (liability): her satır bakım, test ve inceleme maliyeti getirir. AI'ın gerçek katkısı ancak **sonuç metrikleriyle** ölçülür — teslim süresi (B), hata oranı (C), inceleme etkinliği (D). Satır saymak "daha çok kod = daha çok değer" gibi tehlikeli bir teşvik yaratır. Üstelik algı da yanıltır: 2025'teki kontrollü METR ölçümünde geliştiriciler asistanla daha yavaş çalıştıkları hâlde kendilerini daha hızlı sanmıştır — bu yüzden ölçüm sonuç tarafında yapılır.

# aiaug-2 | Orta
## Soru
AI asistanının ürettiği bir fonksiyonu PR'a eklerken en doğru sorumluluk anlayışı hangisidir?
## A
Kodu AI ürettiği için sorunların sorumluluğu araç sağlayıcısına aittir
🤖 Sorumluluk devredilemez; kodu commit eden ve merge eden mühendis çıktının sahibidir.
## B
İnceleme insan kodundakiyle aynı çıtada yapılır; sorumluluk mühendistedir
🤖 Doğru: asistan taslak üretir, imza ve sorumluluk her zaman insanda kalır.
## C
AI çıktısı istatistiksel olarak üretildiği için ayrı bir incelemeye gerek yoktur
🤖 Akıcılık doğruluk değildir; denetimsiz AI kodu tam da "slop" riskinin kaynağıdır.
## D
Testler yeşil olduğu sürece kodun satır satır okunmasına gerek yoktur
🤖 Testlerin kapsamı sınırlıdır; güvenlik açığı ve tasarım sorunları çoğu zaman yeşil testin altından geçer.
## Doğru
B
## Açıklama
AI-destekli geliştirmenin altın kuralı: **asistan bir taslak aracıdır, sorumluluğu devretmez**. Kodu commit eden mühendis, onun doğruluğundan, güvenliğinden ve bakımından sorumludur (B). Sorumluluğu sağlayıcıya yıkmak (A), incelemeyi atlamak (C) veya sadece yeşil teste güvenmek (D) hepsi "AI slop"un ve güvenlik açıklarının üretime sızma yollarıdır. Pratik: her AI çıktısını, işe yeni başlamış bir geliştiricinin PR'ı gibi ele al — hızlı ama denetlenmesi şart.

# aiaug-3 | İleri
## Soru
"AI slop" terimi bir kod tabanında neyi ifade eder?
## A
Asistanın yavaş öneri üretmesinden doğan bekleme süresini ve akış kopmasını
🤖 Slop bir hız/gecikme sorunu değildir; üretilen çıktının kalitesi ve akla-yatkın-ama-yanlış olmasıyla ilgilidir.
## B
Modelin bağlam sınırına takılıp hiç öneri üretememesi durumunu
🤖 Aksine, slop bolca üretilen ama özensiz çıktının birikmesidir; üretememe değil, denetimsiz üretmedir.
## C
Gözden geçirilmeden biriken, makul görünen ama hatalı çıktı yığınını
🤖 Doğru: akla yatkın göründüğü için inceleme gevşetilir ve bakımı zorlaştıran yığın kod sızar.
## D
Eğitim verisindeki lisanslı koda benzeyen çıktının doğurduğu telif riskini
🤖 Telif ayrı ve gerçek bir risktir; slop ise çıktının kalite ve tutarlılık sorununu anlatır.
## Doğru
C
## Açıklama
**AI slop**, modellerin akıcı ve kendinden emin üretimi sayesinde **yüzeyde makul görünen ama özünde gereksiz, tutarsız veya hatalı** olan ve incelenmeden kod tabanına biriken düşük kaliteli çıktıdır (C). Tehlikesi tam da akla-yatkınlığındadır: gözden geçiren kişi "iyi görünüyor" deyip gevşer. Zamanla teknik borç, kopuk soyutlamalar ve gizli açıklar birikir. Panzehir: AI çıktısını insan çıktısıyla aynı inceleme çıtasına tabi tutmak ve gereksiz kodu acımasızca budamaktır.

# aiaug-4 | İleri
## Soru
Veracode'un 2025 araştırmasının, AI ürettiği kodun güvenliği hakkındaki temel bulgusu hangisidir?
## A
Model büyüdükçe açık oranı düzenli olarak düşer; büyük modellerde açık neredeyse sıfırdır
🤖 Bulgu tersi yöndedir: model büyümesi güvenlik açığı oranını anlamlı biçimde düşürmemiştir.
## B
Üretilen kodun yaklaşık yarısı açık içerir; model büyüklüğü bunu düzeltmez
🤖 Doğru: ~%45 açık oranı ve model ölçeğinin bunu çözmediği bulgusu raporun temel uyarısıdır.
## C
AI kodu insan kodundan istatistiksel olarak daima daha güvenlidir; ayrıca taramaya gerek yoktur
🤖 Araştırma böyle bir güvence vermez; aksine ciddi bir açık oranına ve tarama zorunluluğuna dikkat çeker.
## D
Açıklar yalnızca eski (deprecated) modellerde görülür; güncel sürümlerde sorun kalmamıştır
🤖 Açıklar güncel ve büyük modellerde de gözlenmiştir; sürüm yeniliği tek başına çözmez.
## Doğru
B
## Açıklama
Veracode'un 2025 bulgusunun çarpıcı yanı: güvenlik-duyarlı senaryolarda LLM üretimi kodun **yaklaşık %45'i** bir güvenlik açığı barındırıyordu ve **model büyüklüğü/yeniliği bu oranı belirgin şekilde düşürmüyordu** (B). Yani "daha büyük/akıllı model = daha güvenli kod" beklentisi doğrulanmadı. Mühendislik sonucu nettir: AI çıktısı **varsayılan olarak güvensiz kabul edilmeli**, statik/dinamik güvenlik taramaları (SAST/DAST) ve insan güvenlik incelemesi zorunlu kapı olmalıdır. Ölçek iyimserliği (A), koşulsuz güven (C) ve "sadece eski modeller" varsayımı (D) yanlıştır.

# aiaug-5 | İleri
## Soru
Aşağıdaki AI-üretimi Java kodu bir isteğe (request) göre veritabanı sorgusu yapıyor. Temel sorun nedir?

```java
String ad = request.getParameter("ad");
String sql = "SELECT * FROM kullanici WHERE ad = '" + ad + "'";
stmt.executeQuery(sql);
```
## A
`SELECT *` gereksiz sütun çektiği için asıl sorun performanstır, güvenlik değil
🤖 `SELECT *` bir koku olsa da buradaki kritik ve öncelikli sorun güvenlik açığıdır, performans değil.
## B
Kod derlenmez; `executeQuery` metodu String parametre kabul etmez
🤖 `executeQuery(String)` geçerli bir imzadır; kod derlenir, sorun çalışma-zamanı güvenliğidir.
## C
Kullanıcı girdisi doğrudan SQL'e ekleniyor: SQL injection açığı var
🤖 Doğru: dış girdi string birleştirmeyle sorguya giriyor; çözüm parametreli sorgudur.
## D
Sorun yok; parametre String tipinde olduğu için girdi zaten güvenli sayılır
🤖 Tip String olması güvenli yapmaz; tam tersine denetimsiz string birleştirme injection'ın kaynağıdır.
## Doğru
C
## Açıklama
Bu, AI asistanlarının ürettiği kodda en sık görülen açıklardan biridir: **kullanıcı girdisinin string birleştirmeyle (concatenation) SQL'e sokulması** → **SQL injection** (C). Saldırgan `ad` parametresine `' OR '1'='1` gibi bir değer vererek sorgunun mantığını ele geçirebilir. Doğru çözüm `PreparedStatement` ile parametreli sorgudur: `WHERE ad = ?` ve `stmt.setString(1, ad)`. `SELECT *` (A) ikincil bir kokudur; kod derlenir (B); "String olduğu için güvenli" (D) tehlikeli bir yanılgıdır. Bu örnek, güvenlik-duyarlı görevlerdeki yüksek açık oranını somutlaştırır: akıcı görünen AI kodu varsayılan olarak taranmalıdır.

# aiaug-6 | Uzman
## Soru
Bir ekip, AI asistanını "büyük çaplı üretim" (bulk generation) için kullanıp inceleme kapasitesini aşan hacimde PR açmaya başladı. En sağlıklı sistemik yaklaşım hangisidir?
## A
Üretim hızını incelenebilir hacme sınırlamak; kalite kapılarını otomatikleştirmek
🤖 Doğru: darboğaz üretim değil güvenilir incelemedir; hız incelenebilirliğe göre ayarlanır, kapılar mekanikleşir.
## B
İnceleme darboğaz olduğundan kaldırılmalı; asistanın kendi çıktısını onaylaması yeterlidir
🤖 İncelemeyi kaldırmak "slop"u doğrudan üretime taşır; öz-onay güvenlik ve telif açıklarını görmez.
## C
Daha fazla kod her zaman ilerleme olduğundan üretim hacmini artırmaya devam etmek
🤖 Kod yükümlülüktür; incelenmeyen hacim teknik borç ve güvenlik riskini büyütür, ilerleme değildir.
## D
Tüm PR'ları otomatik merge edip ortaya çıkan sorunları üretimde düzeltmek
🤖 Üretimde düzeltme en pahalı ve riskli yoldur; güvenlik açıkları bu arada kullanıcıya ulaşır.
## Doğru
A
## Açıklama
Buradaki sistemik ilke şudur: **darboğaz kod üretimi değil, güvenilir incelemedir**. AI üretimi ucuzladıkça asıl kısıt insan inceleme kapasitesidir; bu yüzden üretim hızı **incelenebilir hacme** göre ayarlanmalı ve inceleme mekanikleştirilmelidir — CI'da statik analiz (SAST), test kapıları, güvenlik taraması ve odaklı insan incelemesi (A). İncelemeyi kaldırmak (B), koşulsuz hacim (C) veya otomatik merge (D) hepsi "AI slop" ve güvenlik açıklarını üretime taşır. Özet: AI çıktısının değeri, onu güvenle süzebildiğin oranda gerçektir.
