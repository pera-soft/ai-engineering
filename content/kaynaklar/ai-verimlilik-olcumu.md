---
baslik: "AI Verimlilik Ölçümü — Algı, Kanıt ve 'Büyüteç' Tezi"
yazar: "METR RCT · DORA 2025 · Stack Overflow 2025 · GitClear — Türkçe kavram özeti"
tarih: "2026-07-17"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 4,7,8,12
---

### Bu metin ne anlatıyor?
Bu metin tek bir soruyu soruyor: **"Yapay zekâ seni hızlandırdı mı — bunu nereden biliyorsun?"**
Cevap sandığından zor, çünkü insanlar hızlandıklarını *hissederken* aslında yavaşlayabiliyor;
bu bir kontrollü deneyle ölçüldü. Metin ayrıca üç farklı ölçüm aletini (deney, anket, kendi
sisteminin kayıtları) birbirinden ayırmayı ve hangisine ne kadar güvenileceğini öğretiyor. Sonuç
tek cümlede: **kimsenin beyanına değil, kendi sayına bak.**

## Neden bu özet
Program boyunca bir varsayım sessizce taşınıyor: **AI araçları geliştirmeyi hızlandırır.** Hafta 7–8 bu
araçları mühendislik disiplinine bağlıyor, Hafta 4 kaliteyi otomatik kapılarla (ArchUnit, SAST)
mühürlüyor, Hafta 12 mimarın karar sorumluluğunu işliyor. Ama hiçbiri şunu doğrudan sormuyor:
**"Hızlandığını nereden biliyorsun?"** Bu özet o açığı kapatır ve dört haftaya birden dokunur:

- **Hafta 4** — kalite kapılarının neden var olduğunun **ampirik** gerekçesi (DORA "büyüteç" tezi),
- **Hafta 7–8** — kendi sisteminden ölçüm alma refleksi (telemetri, eval, maliyet),
- **Hafta 12** — mimarın en tehlikeli girdisi olan **kendi sezgisi**.

İçerik üç iddia üzerine kurulu; üçünün güven seviyesi **farklı** ve bu özetin asıl öğrettiği şey zaten
bu: hangi kanıtın ne kadar ağırlık taşıdığını ayırt etmek.

1. **Algı ile ölçüm ayrışıyor** — geliştiriciler hızlandıklarını hissederken yavaşlayabiliyorlar
   (METR, kontrollü deney).
2. **AI bir büyüteçtir** — mevcut güçlü ve zayıf yönleri büyütür; kontrol sistemi zayıfsa hızı
   kararsızlığa çevirir (DORA, korelasyonel).
3. **Benimseme ile güven makas gibi açılıyor** — kullanım artarken güven düşüyor (Stack Overflow, anket).

> **Tek cümle:** Bu özet bir rakam ezberletmez; **öz-bildirimin (self-report) kanıt olmadığını** ve
> mimarın işinin "AI iyi mi kötü mü" tartışmak değil, **kendi sisteminde ölçülebilir gerçek üretmek**
> olduğunu öğretir.

## Türkçe çalışma metni

### 1. Algı–gerçeklik uçurumu — merkezî bulgu
2025'te **METR** (Model Evaluation & Threat Research — yapay zekâ sistemlerinin yeteneklerini
ölçen bağımsız bir araştırma kuruluşu) bir **rastgele kontrollü deney** (randomized controlled
trial, RCT — katılımcıların hangi grupta olacağının kura ile belirlendiği, tıptaki ilaç
denemelerinden tanıdık deney türü) yürüttü (arXiv:2507.09089). Kurgu, "AI acemileri kandırıyor"
itirazını baştan kesecek biçimde seçilmişti:
katılımcılar **deneyimli açık kaynak geliştiricileriydi**, çalıştıkları depolar **kendi tanıdıkları
büyük repolardı** (oyuncak proje değil), görevler rastgele "AI'lı"/"AI'sız" atandı ve süreler ölçüldü.

Sonuç üç katmanlı ve asıl çarpıcı olan üçüncü katman:

| An | Ne oldu |
|---|---|
| **Öncesinde (beklenti)** | Geliştiriciler AI'nın kendilerini **%24 hızlandıracağını** bekledi. |
| **Ölçüm (gerçek)** | Görevler AI ile **%19 daha uzun** sürdü (güven aralığı: +%2 … +%39). |
| **Sonrasında (inanç)** | Yavaşlamayı **yaşadıktan sonra bile** AI'nın kendilerini **%20 hızlandırdığına** inandılar. |

*(**Güven aralığı**: ölçülen gerçek değerin, makul bir olasılıkla içinde bulunduğu aralık. "+%2 …
+%39" demek: yavaşlama neredeyse kesin, ama büyüklüğü konusunda bu deney tek bir kesin sayı
vermiyor.)*

Kaynağın kendi cümlesiyle:

> *"developers expected AI to speed them up by 24%, and even after experiencing the slowdown, they still
> believed AI had sped them up by 20%"*

Asıl bulgu **-%19 rakamı değil**; **deneyimin kendisinin bile algıyı düzeltmemesidir.** Yavaşladın, saat
tuttun, sonra "hızlandım" dedin. Bu sıradan bir tahmin hatası değil; sistematik ve **dirençli** bir
sapmadır. METR bunu Mayıs 2026'da bağımsız bir anketle yeniden ölçtü ve doğruladı:

> *"people overestimated AI's effect on their time spent on tasks by 40 percentage points on average"*

**Uzmanlar da yanıldı — hem de yön olarak.** Deney öncesi tahmin istenen ekonomistler **%39**, makine
öğrenmesi uzmanları **%38 hızlanma** öngördü; ölçüm yavaşlama çıktı. Hata "biraz abartma" değil,
**işaretin ters olması**ydı.

> **Hafta 12 için ders:** Bu konuda "kıdemli sezgi" **kanıt değildir** — ne senin, ne ekibinin, ne de
> alanın uzmanlarının. Bir mimar "ekip AI ile daha hızlı" dediğinde bu bir **hipotezdir**; ölçülene
> kadar öyle kalır. Bkz. **Diyagram 1**.

### 2. ⚠️ ZORUNLU ŞERH — "-%19"u 2026 gerçeği diye öğrenme
Yukarıdaki tabloyu ezberleyip "AI geliştiricileri %19 yavaşlatır" cümlesini kurarsan, bu özetin amacını
tam tersine çevirmiş olursun. Sebep: **METR'in kendisi bu tahmini geride bıraktı.**

Şubat 2026'da METR bir güncelleme yayımladı (metr.org/blog/2026-02-24-uplift-update/) ve deneyi
tekrarlarken **tasarımın artık yürümediğini** anlattı:

- **Geliştiricilerin %30–50'si AI'sız çalışmayı reddetti.** Bu teknik bir detay değil; deneyin
  belkemiğini kırar. Görev-düzeyi randomizasyon (task-level randomization) ancak insanlar atandıkları
  kola razı olursa geçerlidir; reddedenler örneklemden düşünce karşılaştırma bozulur.
- **Ücret $150/saat'ten $50/saat'e indi** → katılımcı havuzu değişti.
- **Eşzamanlı çoklu-ajan (multi-agent) kullanımı süre ölçümünü bozdu.** Aynı anda üç ajan koşturan bir
  geliştirici için "bu göreve kaç dakika harcadın" sorusunun temiz bir cevabı kalmıyor.

Yeni tahminler **yön değiştirdi**: dönen geliştiricilerde **%18 hızlanma**, yeni gelenlerde
**%4 hızlanma**. AMA METR bunun için kendi ifadesiyle *"only very weak evidence"* (yalnızca çok
zayıf kanıt) diyor — bu rakamları da ezberleme.

> **İşaret kuralı — kafa karışıklığını burada kes.** METR bu etkileri *görev süresindeki değişim*
> olarak yazar: **artı işaret = süre uzadı (yavaşlama)**, **eksi işaret = süre kısaldı
> (hızlanma)**. Ünlü bulgu bu yüzden aslında **"+%19 süre"**, yani **%19 yavaşlama**dır; Şubat
> 2026'nın yeni tahminleri ise METR'in kendi yazımında **"−%18 / −%4"**, yani **hızlanma**dır.
> İnternette (ve aşağıdaki tabloda, kısaltılmış ad olarak) "−%19" ibaresiyle karşılaşırsan, kastedilen
> şey bu ünlü **yavaşlama** bulgusudur. Kural basit: **işaret değil, kelime yaz** — "yavaşlama" mı
> "hızlanma" mı, cümleyle söyle.

Şimdi iki şeyi çok net ayır:

> ⚠️ **METR bir GERİ ÇEKME (retraction) YAPMADI.** Bazı ikincil bloglarda dolaşan "çalışma ölümcül
> kusurluydu / geri çekildi" (*fatally flawed / retracted*) anlatısı **UYDURMADIR**. Birincil kaynak
> (metr.org) doğrulandı: orada yazan şey, **araştırmacıların kendi metodolojik sınırlarını dürüstçe
> raporlaması**dır — bilimsel dürüstlüğün örneğidir, itirafı değil.

Bunu açıkça yazıyoruz çünkü öğrenci internette **kesinlikle** o iddiayla karşılaşacak ve ne yapacağını
bilmeli: **birincil kaynağa git.** METR'in kendi cümlesi şudur:

> *"we believe it is likely that developers are more sped up from AI tools now — in early 2026"*

Yani METR'in kendisi, kendi ünlü rakamının artık güncel durumu tarif etmediğini söylüyor. Geriye ne
kalıyor? İki katmanı ayır:

| Katman | İçerik | Ömrü |
|---|---|---|
| 🔴 **GEÇİCİ** | "%19 yavaşlama" nokta tahmini (ve yeni tahminler: dönenlerde %18, yeni gelenlerde %4 **hızlanma**) | 2025 araçlarına ve o tasarıma çivili — **çürür** |
| 🟢 **KALICI** | **Öz-bildirim güvenilmezdir** — deneyimden sonra bile. | Araçtan bağımsız — **kalır** |
| 🟢 **KALICI** | **AI'sız çalışmak artık geliştiricilerin reddettiği bir karşı-olgudur (counterfactual)** — bu yüzden "AI ne kadar hızlandırıyor" sorusunun ölçümü giderek **zorlaşıyor.** | Yapısal — **kalır** |

İkinci kalıcı ders özellikle derindir: bir müdahalenin etkisini ölçmek için "müdahalesiz" bir kontrol
grubu gerekir. İnsanlar kontrol grubunda olmayı reddediyorsa o karşı-olgu **artık erişilemez**. Ölçüm
zorlaştıkça boşluğu ne dolduruyor? **Algı** — ve algının güvenilmez olduğunu zaten biliyoruz. Kıskaç
budur; mimarın bundan çıkışı **kendi telemetrisidir** (§7).

### 3. Üç ayrı alet, üç ayrı güven seviyesi
Bu özetteki üç kaynak (METR, DORA, Stack Overflow) sık sık aynı torbaya atılır: "araştırmalar diyor ki…"
Bu **hatadır**. Üçü farklı enstrümandır, farklı soruları yanıtlar, farklı ağırlık taşır. "DORA/RCT tarzı
ölçüm" diye **birleştirme.**

İki kelimeyi baştan ayır, çünkü bütün bölüm bunun üstünde duruyor:
**korelasyon** (iki şeyin birlikte hareket etmesi — "AI kullanan ekiplerde kararlılık düşük
çıkıyor") ile **nedensellik** (birinin ötekine *sebep* olması — "AI kararlılığı düşürüyor").
Korelasyon gördüğünde nedensellik demek, sokakta şemsiye sayısıyla yağmuru görüp "şemsiyeler
yağmuru yağdırıyor" demeye benzer.

**1) Rastgele kontrollü deney (RCT) — METR.** Müdahaleyi rastgele atar, sonucu ölçer. **Nedensellik**
iddia edebilir ("AI *yüzünden* yavaşladılar"). Sınırı **dar bağlam**: belirli geliştiriciler, belirli
repolar, belirli bir zamanın araçları. Genellenebilirliği düşük, iç geçerliliği (internal validity)
yüksektir.

**2) Gözlemsel anket — DORA, Stack Overflow.** Geniş bir kitleye ne yaptıklarını ve ne hissettiklerini
sorar, örüntü arar. Yalnızca **korelasyon** iddia edebilir ("AI benimsemesi yüksek ekiplerde kararlılık
düşük"). Sınırı: **öz-beyan** (§1'in tam da güvenilmez dediği şey) ve nedensellik yönünün bilinmemesi.
Gücü: geniş bağlam, çok sektör, çok ekip.

**3) Telemetri — kendi sisteminden.** Senin sisteminde gerçekte ne olduğunu kaydeder: **senin gerçeğin.**
Sınırı: genellenemez, ve neyi ölçmeyi seçtiğinle sınırlıdır. Gücü: öz-beyan değil, başkasının bağlamı
değil — ölçülmüş olan.

Hangisi hangi soruyu cevaplar:

| Soru | Doğru alet |
|---|---|
| "AI bu görev türünde nedensel olarak hızlandırır mı?" | RCT (dar ama nedensel) |
| "Sektörde AI benimsemesi neyle birlikte gidiyor?" | Anket (geniş ama korelasyonel) |
| "**Benim** ekibimde/sistemimde ne oluyor?" | **Telemetri** |

Üçünü karıştırmanın bedeli somut: bir RCT'nin dar bulgusunu ("%19 yavaşlama") sektör yasası sanmak da,
bir anketin korelasyonunu ("AI kararlılığı düşürüyor") nedensel yasa sanmak da aynı hatanın iki yüzüdür.
Bkz. **Diyagram 3**.

### 4. DORA "The Great Amplifier" — bu müfredatın varlık gerekçesi
**DORA** (DevOps Research and Assessment — yazılım ekiplerinin nasıl çalıştığını her yıl geniş
anketlerle ölçen, bugün Google bünyesindeki araştırma programı). 2025 raporu (yaklaşık
**5.000 profesyonel**, 100+ saat nitel araştırma, **78 mülakat**) tek bir tezi merkeze koyuyor ve
o tez bu programın neden böyle kurulduğunu açıklıyor. Bulgu, geçen yılınkini **kısmen tersine
çeviriyor**:

> *"Unlike last year, we observe a positive relationship between AI adoption on both software delivery
> throughput and product performance... AI adoption does continue to have a negative relationship with
> software delivery stability."*

Yani: AI benimsemesi **teslimat hızıyla (throughput — birim zamanda canlıya çıkarılan değişiklik
miktarı) ve ürün performansıyla POZİTİF**, ama **teslimat kararlılığıyla (delivery stability —
çıkan değişikliklerin ne sıklıkla bozulup geri alındığı) NEGATİF** ilişkili. Hız gelmiş,
kararlılık gitmiş.

Mekanizma — raporun kendi açıklaması, ve bu cümle bu özetin kalbi:

> *"AI accelerates software development, but that acceleration can expose weaknesses downstream. Without
> robust control systems, like strong automated testing, mature version control practices, and fast
> feedback loops, an increase in change volume leads to instability."*

Türkçesi: **AI kod yazmayı hızlandırır, ama hızlanan şey akıştaki tek bir adımdır.** Değişim hacmi
(change volume) artar; o hacim akışın aşağısındaki (downstream) her zayıflığa daha büyük bir yükle
çarpar. Test yoksa, sürüm yönetimi olgun değilse, geri besleme yavaşsa — daha fazla değişiklik daha
fazla kararsızlık demektir. AI yeni bir zayıflık **yaratmaz**; var olanı **görünür ve pahalı** kılar.
Raporun genel tezi tam da bu:

> *"AI's primary role is as an amplifier, magnifying an organization's existing strengths and weaknesses."*

Ve çıkarımı:

> *"The greatest returns on AI investment come not from the tools themselves, but from a strategic focus
> on the underlying organizational system."*

**Bu müfredatın tezi budur.** Ve doğrudan **Hafta 4**'e bağlanır: ArchUnit mimari kuralları, SAST
taraması, otomatik test kapıları — bunlar "iyi mühendislik hijyeni" diye öğretilen isteğe bağlı süsler
değil; DORA'nın diliyle tam olarak **"robust control systems"**tir. Yani Hafta 4, AI çağının ampirik
olarak en yüksek getirili yatırımıdır: hızın kararsızlığa dönüşmesini engelleyen katman. Bu projedeki
`MimariTestleri` ve `./mvnw verify` kapısı, "büyüteç" tezinin somut karşılığıdır — büyüteç neyi
büyütecekse, önce o şeyi sağlam yaparsın. Bkz. **Diyagram 2**.

> ⚠️ **ŞERH — bunu nedensel bir yasa gibi okuma.** DORA verisi **öz-beyana dayalı ve KORELASYONELDİR**;
> rapor nedensellik **iddia etmiyor**. Doğru cümle **"AI kararlılığı bozar"** değil, **"AI benimsemesi
> ile kararlılık arasında negatif korelasyon var ve raporun önerdiği makul bir mekanizma mevcut"**tur.
>
> **Ters nedensellik (reverse causality) tamamen makuldür:** belki AI kararlılığı bozmuyordur; belki
> **zaten kararsız, baskı altındaki, teknik borcu yüksek ekipler** çareyi AI'a sarılmakta buluyordur.
> Bu durumda ok ters yöne bakar; veri iki hikâyeyi de aynı ölçüde destekler ve anket tasarımı bunları
> ayıramaz.
>
> Bu yüzden "büyüteç" tezini **"kanıtlanmış nedensel yasa"** olarak değil, **"iyi kanıtlanmış bir
> çerçeve"** olarak sun. Çerçeve olarak değeri yüksektir: geniş veriye dayanır, mekanizması makuldür ve
> eylem önerisi (kontrolleri güçlendir) **her iki nedensellik yönünde de doğrudur** — ters nedensellik
> doğru olsa bile kararsız ekibin ihtiyacı yine güçlü kontrol sistemleridir. Doğru olma ihtimali yüksek
> **ve** yanılsan bile zarar vermeyen bir çerçeve, mühendislikte iyi bir bahistir.

## Diyagram 1 — Algı–gerçeklik uçurumu (METR RCT)
Beklenti, ölçüm ve deney-sonrası inanç yan yana. Uzman tahminleri de gösteriliyor — hata payı değil,
**yön hatası**. Şerh: bu rakamlar 2025 araçlarına çivilidir; kalıcı ders sapmanın kendisidir.

```svg
<svg viewBox="0 0 640 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="METR rastgele kontrollü deneyinde algı ve gerçeklik uçurumu: geliştiriciler yüzde 24 hızlanma bekledi, ölçüm yüzde 19 yavaşlama çıktı, yavaşlamayı yaşadıktan sonra bile yüzde 20 hızlandıklarına inandılar; ekonomistler yüzde 39 ve makine öğrenmesi uzmanları yüzde 38 hızlanma öngörerek yön olarak yanıldı; rakamlar 2025 araçlarına çivilidir">
  <defs>
    <marker id="vo-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="vo-1b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Algı–Gerçeklik Uçurumu — METR RCT (2025)</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Deneyimli geliştiriciler · tanıdıkları büyük repolar · süre ölçüldü</text>

  <line x1="60" y1="150" x2="600" y2="150" stroke="#5D7789" stroke-width="1.2"/>
  <text x="606" y="153" font-size="9" fill="#5D7789" font-family="system-ui">0</text>
  <text x="52" y="118" text-anchor="end" font-size="8.5" fill="#25804C" font-family="system-ui">hızlanma</text>
  <text x="52" y="186" text-anchor="end" font-size="8.5" fill="#B3402F" font-family="system-ui">yavaşlama</text>

  <rect x="76" y="102" width="86" height="48" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="119" y="132" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">+%24</text>
  <text x="119" y="168" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">BEKLENTİ</text>
  <text x="119" y="182" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">deney öncesi</text>

  <rect x="186" y="150" width="86" height="40" rx="4" fill="#F7E4E1" stroke="#B3402F" stroke-width="1.8"/>
  <text x="229" y="175" text-anchor="middle" font-size="13" font-weight="700" fill="#B3402F" font-family="system-ui">+%19</text>
  <text x="229" y="206" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B3402F" font-family="system-ui">ÖLÇÜM</text>
  <text x="229" y="219" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">daha UZUN sürdü</text>
  <text x="229" y="232" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">(GA: +%2 … +%39)</text>

  <rect x="296" y="110" width="86" height="40" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="339" y="136" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">+%20</text>
  <text x="339" y="168" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">İNANÇ</text>
  <text x="339" y="182" text-anchor="middle" font-size="9" fill="#C77A17" font-family="system-ui">yavaşlamayı</text>
  <text x="339" y="194" text-anchor="middle" font-size="9" fill="#C77A17" font-family="system-ui">YAŞADIKTAN SONRA</text>

  <path d="M229,150 C250,84 280,84 300,108" fill="none" stroke="#B3402F" stroke-width="1.6" stroke-dasharray="5 4" marker-end="url(#vo-1b)"/>
  <text x="268" y="80" text-anchor="middle" font-size="9" font-weight="700" fill="#B3402F" font-family="system-ui">deneyim algıyı DÜZELTMEDİ</text>

  <line x1="410" y1="70" x2="410" y2="250" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>
  <text x="524" y="88" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Uzman tahminleri</text>
  <text x="524" y="103" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(deney öncesi soruldu)</text>
  <rect x="432" y="116" width="184" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="524" y="136" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Ekonomistler: +%39 hızlanma</text>
  <rect x="432" y="152" width="184" height="30" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="524" y="172" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ML uzmanları: +%38 hızlanma</text>
  <rect x="432" y="190" width="184" height="34" rx="6" fill="#F7E4E1" stroke="#B3402F" stroke-width="1.5"/>
  <text x="524" y="204" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">Hata payı değil — YÖN hatası.</text>
  <text x="524" y="217" text-anchor="middle" font-size="9.5" fill="#B3402F" font-family="system-ui">"Kıdemli sezgi" kanıt değildir.</text>

  <rect x="40" y="262" width="560" height="60" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="52" y="280" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">⚠ ŞERH — rakamlar 2025 araçlarına ÇİVİLİ:</text>
  <text x="52" y="296" font-size="9.5" fill="#1C3247" font-family="system-ui">METR (Şubat 2026) tasarımı değiştirdi; yeni tahminler yön değiştirdi ama "only very weak evidence".</text>
  <text x="52" y="312" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">KALICI olan: öz-bildirimin güvenilmezliği. GEÇİCİ olan: −%19 nokta tahmini. GERİ ÇEKME YOK.</text>
</svg>
```

### 5. Benimseme–güven makası
Stack Overflow 2025 geliştirici anketi (**n = 49.009**) tek başına bakıldığında çelişkili görünen bir
tablo veriyor — ama çelişki değil, **makas**:

- **Benimseme YÜKSELDİ:** AI araçlarını kullanan/kullanmayı planlayan oranı **%76 → %84**.
- **Olumlu duygu (sentiment) DÜŞTÜ:** **%70+ → %60**.
- **Güven düşük:** **%45,7** çıktının doğruluğuna **aktif olarak güvenmiyor**; yalnızca **%3,1** yüksek
  güven bildiriyor.

Yani insanlar aracı **daha çok kullanıyor ama daha az seviyor ve daha az güveniyor.** İki eğri ters
yönde açılıyor.

**En çarpıcı kesit — deneyim ekseni.** Sezgi "deneyimliler daha iyi kullanır, dolayısıyla daha çok
güvenir" der; veri tersini söylüyor:

> **10+ yıl deneyimli geliştiriciler EN şüpheci grup:** %20,7 yüksek güvensizlik, yalnızca %2,5 yüksek
> güven.

Bunu §1 ile birlikte oku; ilginç bir gerilim çıkar: deneyimliler **güvenmiyor**, ama METR deneyinde
deneyimliler yine de **hızlandıklarını sanıyordu**. Şüphecilik, algı sapmasına karşı bağışıklık
**vermiyor**. "Bu araca güvenmiyorum" demekle "bu araç beni yavaşlatıyor" diyebilmek ayrı şeylerdir.

**En büyük şikâyet ne?** Rakam burada mekanizmayı ele veriyor:

> **%66:** *"neredeyse doğru ama tam değil"* (almost right, but not quite) çözümler.

Bu tesadüfi bir sıralama değil — **tam olarak en pahalı hata türüdür**. Açıkça yanlış kod ucuzdur:
derlenmez, test kırılır, saniyeler içinde atarsın. "Neredeyse doğru" kod ise incelemeyi (review)
sömürür: makul görünür, derlenir, çoğu durumda çalışır — hatayı bulmak için onu **gerçekten okumak**
zorundasındır ve okumak yazmaktan yavaştır. METR'in ölçtüğü yavaşlamanın makul bir mekanizması da
buradadır: kazanılan yazma süresi **inceleme ve doğrulama süresine** aktarılır.

> ⚠️ **ŞERH — mutlak rakamları kullanma, TRENDİ kullan.** Stack Overflow anketi **kendi kendini seçen
> (self-selection) bir örneklemdir**: katılımcıyı Stack Overflow kullanıcıları arasından, kendi
> istekleriyle alır. Bu, geliştirici nüfusunun temsilî bir kesiti **değildir**. "%45,7 güvenmiyor"
> cümlesini "dünyadaki geliştiricilerin %45,7'si" diye okuma.
>
> Ama şu kurtarılır: **yanlılık her yıl kabaca aynı yönde ve büyüklükte** olduğu için **yıllar arası
> değişim** anlamlıdır. "Duygu %70'ten %60'a düştü" ifadesi "%60" ifadesinden **çok daha güvenilirdir**.
> Kural: **seviyeye değil, eğime bak.**

## Diyagram 2 — Büyüteç (amplifier) tezi
Aynı AI girdisi, iki farklı organizasyonda iki farklı sonuç üretir. Farkı yaratan AI değil, **kontrol
sistemidir**. Bu, Hafta 4'ün (ArchUnit/SAST/test kapıları) ampirik gerekçesidir.

```svg
<svg viewBox="0 0 640 360" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="DORA büyüteç tezi: aynı AI girdisi değişim hacmini artırır; güçlü kontrol sistemi olan organizasyonda otomatik test, olgun sürüm yönetimi ve hızlı geri besleme sayesinde hem hız hem kararlılık elde edilir; kontroller zayıfsa hız gelir ama kararsızlık oluşur. AI mevcut güçlü ve zayıf yönleri büyütür. Veri korelasyoneldir, nedensellik iddia edilmez, ters nedensellik makuldür">
  <defs>
    <marker id="vo-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="vo-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="vo-2c" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"The Great Amplifier" — AI mevcut durumu BÜYÜTÜR</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">DORA 2025 · ~5.000 profesyonel · 78 mülakat · Farkı yaratan AI değil, kontrol sistemi</text>

  <rect x="14" y="140" width="96" height="56" rx="10" fill="#EAF1F7" stroke="#1C3247" stroke-width="1.6"/>
  <text x="62" y="163" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">AI girdisi</text>
  <text x="62" y="180" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">değişim hacmi ↑</text>

  <polygon points="130,168 176,132 176,204" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="158" y="172" text-anchor="middle" font-size="9" font-weight="700" fill="#C77A17" font-family="system-ui">büyüteç</text>
  <line x1="110" y1="168" x2="128" y2="168" stroke="#5D7789" stroke-width="1.8" marker-end="url(#vo-2a)"/>

  <rect x="200" y="66" width="200" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="300" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">GÜÇLÜ kontrol sistemi</text>
  <text x="212" y="105" font-size="9.5" fill="#1C3247" font-family="system-ui">• otomatik test (ArchUnit, birim)</text>
  <text x="212" y="121" font-size="9.5" fill="#1C3247" font-family="system-ui">• olgun sürüm yönetimi</text>
  <text x="212" y="137" font-size="9.5" fill="#1C3247" font-family="system-ui">• hızlı geri besleme döngüsü</text>
  <path d="M176,150 C188,120 190,110 198,108" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#vo-2b)"/>

  <rect x="200" y="196" width="200" height="86" rx="10" fill="#F7E4E1" stroke="#B3402F" stroke-width="1.8"/>
  <text x="300" y="216" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B3402F" font-family="system-ui">ZAYIF kontrol sistemi</text>
  <text x="212" y="235" font-size="9.5" fill="#1C3247" font-family="system-ui">• test boşlukları / kapı yok</text>
  <text x="212" y="251" font-size="9.5" fill="#1C3247" font-family="system-ui">• dağınık sürüm pratiği</text>
  <text x="212" y="267" font-size="9.5" fill="#1C3247" font-family="system-ui">• yavaş geri besleme</text>
  <path d="M176,186 C188,220 190,232 198,238" fill="none" stroke="#B3402F" stroke-width="1.8" marker-end="url(#vo-2c)"/>

  <rect x="428" y="76" width="196" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="526" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">HIZ + KARARLILIK</text>
  <text x="526" y="119" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">throughput ↑ · ürün performansı ↑</text>
  <text x="526" y="133" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kararlılık korunur</text>
  <line x1="400" y1="109" x2="426" y2="109" stroke="#25804C" stroke-width="2" marker-end="url(#vo-2b)"/>

  <rect x="428" y="206" width="196" height="66" rx="10" fill="#F7E4E1" stroke="#B3402F" stroke-width="1.8"/>
  <text x="526" y="230" text-anchor="middle" font-size="12" font-weight="700" fill="#B3402F" font-family="system-ui">HIZ + KARARSIZLIK</text>
  <text x="526" y="249" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">throughput ↑ · kararlılık ↓</text>
  <text x="526" y="263" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">zayıflık aşağı akışta patlar</text>
  <line x1="400" y1="239" x2="426" y2="239" stroke="#B3402F" stroke-width="2" marker-end="url(#vo-2c)"/>

  <text x="320" y="304" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Hafta 4 (ArchUnit · SAST · test kapıları) = DORA'nın dediği "robust control systems"</text>
  <rect x="40" y="314" width="560" height="38" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="52" y="330" font-size="9.5" font-weight="700" fill="#C77A17" font-family="system-ui">⚠ ŞERH: veri ÖZ-BEYAN ve KORELASYONEL — DORA nedensellik iddia ETMİYOR.</text>
  <text x="52" y="345" font-size="9.5" fill="#1C3247" font-family="system-ui">Ters nedensellik makul: zaten kararsız ekipler AI'a sarılıyor olabilir. "Yasa" değil, iyi kanıtlanmış ÇERÇEVE.</text>
</svg>
```

### 6. "Yayılma vergisi" (propagation tax) — tek kaynak, dikkatli tut
GitClear, 600 milyondan fazla değişen kod satırını (commit) analiz eden bir veri seti üzerinde
**blok tekrarını** (block duplication — 5 veya daha fazla ardışık anlamlı satırın bir kopyası) ölçüyor.
Bildirdiği eğilim:

> **Milyon değişen satır başına tekrarlanan blok: 40,3 (2023) → 73,0 (2026 YTD) = ~%81 artış** —
> kayıttaki en yüksek seviye.

*(**YTD** = year to date, "yıl başından bugüne": 2026 rakamı tam bir yılı değil, yılın geçen
kısmını kapsar — aşağıdaki üçüncü şerh bunun neden önemli olduğunu anlatıyor.)*

Asıl değerli olan rakam değil, **kavram**. GitClear'ın tanımı şu:

> *"A duplicated block imposes a propagation tax: when a developer changes one copy of a five-line block,
> they inherit the obligation to find and evaluate every sibling — across files and domains they may not
> know"*

Türkçesi: tekrarlanan bir blok bir **yayılma vergisi** dayatır. Beş satırlık bir bloğun bir kopyasını
değiştiren geliştirici, **her kardeş kopyayı bulma ve değerlendirme yükümlülüğünü miras alır** — üstelik
tanımadığı dosyalarda ve alanlarda.

**Bu neden bir mimarlık meselesi?** Çünkü DRY (Don't Repeat Yourself) ihlalini bir **stil tercihi**
olmaktan çıkarıp **ölçülmüş bir bakım vergisine** çevirir. "Tekrar etme" artık estetik bir öğüt değil;
her kopya, gelecekteki her değişikliğe eklenen bir vergi kalemidir.

Dahası, bugün herkesin şikâyet ettiği **inceleme darboğazının (review bottleneck)** yapısal kaynağını
açıklar: incelemecinin işi "bu 5 satır doğru mu" değil, "bu 5 satırın başka nerelerde kardeşi var ve
onlar da güncellenmeli mi" sorusudur. §5'in "%66 neredeyse doğru" bulgusuyla birleşince tablo tamamlanır:
AI hem daha çok kod, hem daha çok tekrar, hem de daha dikkatli okunması gereken kod üretir — üçü birden
aynı darboğaza akar.

> ⚠️ **AĞIR ŞERH — bu bölümü sektör gerçeği diye aktarma.**
>
> 1. **GitClear kod kalitesi analitiği SATAN bir şirkettir.** Bu verinin yanlış olduğu anlamına gelmez;
>    ama "kod kalitesi bozuluyor" bulgusunun, kod kalitesi ölçen ürün satan şirketten gelmesi bir
>    **çıkar hizalanmasıdır** ve ağırlığı düşürür.
> 2. **Görünürdeki "bağımsız doğrulamalar" DÖNGÜSELDİR.** Bu rakamları tekrarlayan yazılar izlendiğinde
>    hepsi **GitClear'ın kendi raporuna** geri döner; çok kaynak gibi görünen şey tek kaynağın
>    yankısıdır. **Hiçbir bağımsız veri seti 73,0'ı teyit etmiyor.**
> 3. **2026 YTD kısmi bir yıldır** ve tam yıllarla (2023) kıyaslanıyor — mevsimsel ve bileşim
>    etkilerine açıktır.
>
> **Kural: "GitClear'ın ölçümüne göre…" diye atfet; "sektör gerçeği" ASLA deme.** Kavramı (yayılma
> vergisi) al — güçlüdür, mantığı kendi başına ayakta durur. Rakamı ise kaynağıyla birlikte taşı.
>
> ⚠️ **Ayrıca:** aynı raporun **diğer metrikleri çürütüldü** — "moved code" (taşınan kod) düşüşü,
> "function connectivity" (fonksiyon bağlantılılığı) ve "legacy bakım durdu / eski kod güncellenmiyor"
> iddiası. **Bunları kullanma.** Bunu söylüyoruz çünkü öğrenci internette o rakamlarla **karşılaşacak**
> ve bir raporun bir metriğinin ayakta kalmasının diğerlerini kurtarmadığını bilmeli: her metriği ayrı
> ayrı tartmak zorundasın.

## Diyagram 3 — Üç alet, üç güven seviyesi
Aynı torbaya atma. Her enstrüman farklı bir soruyu, farklı bir kesinlikle cevaplar.

```svg
<svg viewBox="0 0 640 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç ölçüm aleti ve güven seviyeleri: rastgele kontrollü deney dar bağlamda nedensellik iddia edebilir; gözlemsel anket geniş bağlamda korelasyon verir ve öz-beyana dayanır; kendi sisteminden telemetri senin gerçeğini verir ancak genellenemez. Hangi soruyu cevapladıkları farklıdır ve birleştirilmemelidir">
  <defs>
    <marker id="vo-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç Ayrı Alet — Üç Ayrı Güven Seviyesi</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">"DORA/RCT tarzı ölçüm" diye BİRLEŞTİRME — ayrı enstrümanlar</text>

  <rect x="14" y="62" width="194" height="176" rx="10" fill="#EAF1F7" stroke="#1C3247" stroke-width="1.8"/>
  <text x="111" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">RCT (deney)</text>
  <text x="111" y="99" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">METR</text>
  <text x="26" y="122" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">+ NEDENSELLİK iddia edebilir</text>
  <text x="26" y="140" font-size="9.5" fill="#1C3247" font-family="system-ui">+ müdahale rastgele atanır</text>
  <text x="26" y="158" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">− DAR bağlam</text>
  <text x="26" y="176" font-size="9.5" fill="#1C3247" font-family="system-ui">− o anın araçlarına çivili</text>
  <rect x="26" y="190" width="170" height="36" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="111" y="205" text-anchor="middle" font-size="9" font-weight="700" fill="#5D7789" font-family="system-ui">Cevapladığı soru:</text>
  <text x="111" y="219" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">"Bu görevde AI hızlandırır mı?"</text>

  <rect x="222" y="62" width="196" height="176" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.8"/>
  <text x="320" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">Gözlemsel anket</text>
  <text x="320" y="99" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">DORA · Stack Overflow</text>
  <text x="234" y="122" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">+ GENİŞ bağlam, çok ekip</text>
  <text x="234" y="140" font-size="9.5" fill="#1C3247" font-family="system-ui">+ mekanizma hipotezi verir</text>
  <text x="234" y="158" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">− sadece KORELASYON</text>
  <text x="234" y="176" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">− ÖZ-BEYAN (bkz. §1)</text>
  <rect x="234" y="190" width="172" height="36" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="320" y="205" text-anchor="middle" font-size="9" font-weight="700" fill="#5D7789" font-family="system-ui">Cevapladığı soru:</text>
  <text x="320" y="219" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">"Sektörde ne ile birlikte gidiyor?"</text>

  <rect x="432" y="62" width="194" height="176" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="2.2"/>
  <text x="529" y="84" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Telemetri</text>
  <text x="529" y="99" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kendi sisteminden</text>
  <text x="444" y="122" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">+ SENİN GERÇEĞİN</text>
  <text x="444" y="140" font-size="9.5" fill="#1C3247" font-family="system-ui">+ öz-beyan değil, ölçüm</text>
  <text x="444" y="158" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">− genellenemez</text>
  <text x="444" y="176" font-size="9.5" fill="#1C3247" font-family="system-ui">− ölçmeyi seçtiğinle sınırlı</text>
  <rect x="444" y="190" width="170" height="36" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="529" y="205" text-anchor="middle" font-size="9" font-weight="700" fill="#5D7789" font-family="system-ui">Cevapladığı soru:</text>
  <text x="529" y="219" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">"BENİM sistemimde ne oluyor?"</text>

  <line x1="529" y1="238" x2="529" y2="258" stroke="#25804C" stroke-width="2" marker-end="url(#vo-3a)"/>
  <rect x="150" y="258" width="476" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="388" y="276" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Mimarın kararı için tek dayanak: TELEMETRİ</text>
  <text x="388" y="293" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Diğer ikisi hipotez ve çerçeve verir; karar senin verinle alınır.</text>
  <text x="14" y="284" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">Bunları</text>
  <text x="14" y="298" font-size="9.5" font-weight="700" fill="#B3402F" font-family="system-ui">karıştırma →</text>
</svg>
```

### 6.1. Tek metrik tuzağı — satır sayısı ve PR sayısı neden "verimlilik" değildir
Ölçmeye karar verdiğinde ikinci bir tuzak hazır bekliyor: **kolay sayılan şeyi ölçüp, ölçtüğün
şeyi verimlilik sanmak.** En sık kullanılan iki sayı budur:

- **Yazılan kod satırı sayısı.** Ölçtüğü şey değer değil **hacimdir**; ve AI'ın ucuzlattığı tam
  olarak hacimdir. Dahası §6'nın yayılma vergisi bulgusu ters yöne işaret eder: satırların bir
  kısmı **kopya** olabilir — yani satır sayısı arttıkça bakım borcu da artmış olabilir. Aynı işi
  yapan daha kısa kod genelde **daha iyi** koddur; bu metrik onu **cezalandırır**.
- **Birleştirilen PR sayısı.** Bir PR bir günlük iş de olabilir, tek satırlık yazım düzeltmesi de.
  Ekip metriğe göre değerlendirildiğini fark ettiği an, aynı işi daha çok PR'a bölmek serbest ve
  bedavadır — sayı yükselir, üretilen değer aynı kalır.

İkisi de **vekil metriktir** (proxy metric): asıl ilgilendiğin şeyi (çalışan, bakımı yapılabilen,
kullanıcıya değer veren yazılım) doğrudan değil, dolaylı ölçer. Vekil metriklerin klasik arızası
**Goodhart yasasıdır**:

> **Bir ölçüt hedefe dönüştüğü an, iyi bir ölçüt olmaktan çıkar.**

Sebep insan kötülüğü değil, mekanizmadır: hedef ilan edilen sayıyı yükseltmenin, asıl işi
iyileştirmekten **çok daha ucuz** yolları vardır ve sistem doğal olarak ucuz yolu bulur.

**Peki ne yapılır?** İki kural:

1. **Tek sayıya asla bakma; en az bir hız ve bir kalite metriğini birlikte oku.** Hız tek başına
   yanıltır (DORA'nın bulgusu bu: hız artarken kararlılık düşebiliyor). Bu depodaki karşılığı:
   token/işlem hacmi **yanında** eval puanı ve `./mvnw verify` kırmızı sayısı.
2. **Metriği sonuca bağla.** "Kaç PR" değil, "birleştirilen değişikliğin ne kadarı geri alındı",
   "başarılı çıktı başına maliyet ne yönde gidiyor" (§7).

> **Bunu okuduğun her yerde uygula.** [`harness-engineering-openai.md`](harness-engineering-openai.md)
> §1'in "1 milyon satır, günde 3,5 PR" sayıları tam bu sınıftandır: etkileyicidirler, ama ölçtükleri
> şey hacimdir ve AI kullanan bir ekipte kendiliğinden şişerler. Sayıyı reddetme — **sınıfını
> etiketle**: bu bir *vekil hacim metriği*, bir verimlilik ölçümü değil.

### 7. Mimar ne yapar — bunu pratiğe çevir
Buraya kadar olan her şey bir tek eyleme çıkar: **öz-bildirime değil telemetriye bak.** Ekibin "AI ile
daha hızlıyız" demesi §1'e göre bilgi değil, gürültüdür. Peki bunun yerine ne var?

**1) Kendi sisteminde ne ölçebilirsin.** AI Öğretmen'de altyapı zaten kısmen kurulu:

- **`AI_KULLANIM` tablosu / `AiKullanim`** — her LLM çağrısı: işlem, model, giriş/çıkış token'ı.
  Öz-beyan değil **kayıt**tır.
- **`KullanimService.istatistik()`** — toplam kullanım, `GET /api/kullanim` üzerinden görünür: "ne kadar
  harcadım" sorusunun ölçülmüş cevabı.
- **Eval puanları** (`AtolyeDegerlendirme.puan`, `UretilenQuiz` şema geçerliliği) — "ne kadar iyi"
  sorusunun ölçülmüş cevabı.
- **`MimariTestleri` / `./mvnw verify`** — kapı geçti mi, kaç kere kırmızı yandı: kararlılık ekseninin
  ham verisi.

Bu dördü tek tek az şey söyler. **Birleştirildiğinde** DORA'nın iki eksenini kendi sisteminde yeniden
üretirsin: token/işlem hacmi = **hız ekseni**, eval puanı + kapı kırmızıları = **kararlılık ekseni**.
Büyütecin sende neyi büyüttüğünü ancak böyle görürsün.

**2) "Başarılı çıktı başına maliyet" köprüsü.** Token maliyetini tek başına düşürmek yanıltıcıdır: ucuz
ama eval'ı geçmeyen çıktı, pahalıdan daha maliyetlidir. Doğru metrik maliyet ile kaliteyi **aynı
paydada** birleştirir — ayrı iki özetin konusudur:

- [Değerlendirme-Güdümlü Geliştirme (EDD) ve LLM Gözlemlenebilirliği](evals-llm-observability.md) —
  "ne kadar iyi" nasıl ölçülür, eval nasıl CI kapısına konur, eval sonucu nasıl bir telemetri
  özniteliğine dönüşür.
- [FinOps ve AI Tokenomiği](finops-ai-tokenomics.md) — "ne kadara mal oluyor" nasıl ölçülür ve başarılı
  çıktı başına maliyet nasıl hesaplanır.

**3) Kararlılık ekseni mimarın işidir.** DORA'nın tezi hız değil, **kararlılık** tarafında görev
tanımlıyor: otomatik test, guardrail (koruma bandı), hızlı geri besleme — yani **Hafta 4**. Çerçeveyi
tersine çevirince mimarın rolü netleşir:

> **AI'nın getirdiği hız, kontrol sisteminin kalitesine uygulanan bir çarpandır.** Çarpan büyürken
> çarpılan negatifse sonuç daha hızlı batmaktır. Mimarın işi çarpanı büyütmek değil — o kendiliğinden
> oluyor — **çarpılanı pozitif tutmaktır.**

> **"AI bizi hızlandırdı mı?" diye sorma — ölçülemez ve sana yalan söyler. Bunun yerine sor: "Değişim
> hacmim arttı mı? Kapılarım hâlâ tutuyor mu? Başarılı çıktı başına maliyetim ne yönde gidiyor?"
> Üçünün de cevabı senin telemetrinde yazılı.**

## Öz-sınav (kendini test et)
1. METR RCT'sindeki üç sayıyı (beklenti +%24, ölçüm +%19 daha uzun, sonraki inanç +%20) sırala. Hangisi
   **asıl** rahatsız edici olandır: "yavaşladılar" mı, "yavaşladıktan sonra bile hızlandıklarını
   sandılar" mı — ve neden?
2. Ekonomistlerin %39, ML uzmanlarının %38 hızlanma öngörmesi neden basit bir tahmin hatası değil? Bu
   bulgu Hafta 12'nin "kıdemli sezgiye güven" refleksine ne yapar? Ekibinde "AI ile daha hızlıyız" diyen
   kıdemli bir geliştiriciye ne cevap verirsin?
3. METR Şubat 2026 güncellemesinde tasarımı bozan üç şeyi say. "Geliştiricilerin %30–50'si AI'sız
   çalışmayı reddetti" neden sadece bir katılım sorunu değil, **ölçüm biliminin** yapısal sorunudur
   (ipucu: karşı-olgu / counterfactual)?
4. Bir blog "METR çalışması ölümcül kusurluydu ve geri çekildi" diyor. Bu iddianın nesi yanlış, METR
   gerçekte ne yaptı, ve bu ayrımı kuramamanın maliyeti ne olurdu?
5. METR bulgusundan **kalıcı** iki dersi ve **geçici** bir rakamı ayır. Bu ayrımı yapamayan öğrenci
   2027'de hangi hataya düşer?
6. RCT, gözlemsel anket ve telemetriyi güven seviyesine göre sırala; şu soruları doğru alete eşle:
   (a) "AI bu görev türünde nedensel olarak hızlandırır mı?" (b) "Sektörde benimseme neyle birlikte
   gidiyor?" (c) "Benim ekibimde ne oluyor?" Üçünü "araştırmalar diyor ki" diye birleştirmenin somut
   zararı nedir?
7. DORA 2025'in iki yönlü bulgusunu (throughput/ürün performansı ile **pozitif**, delivery stability ile
   **negatif**) ve mekanizmasını (değişim hacmi ↑ → zayıf kontrol sistemi → kararsızlık) kendi
   cümlelerinle anlat. Bu, Hafta 4'ün (`MimariTestleri`, ArchUnit, SAST, `./mvnw verify` kapısı)
   varlığını nasıl gerekçelendirir?
8. "AI kararlılığı bozar" cümlesini DORA verisi neden **desteklemez**? Ters nedensellik senaryosunu kur.
   Ters nedensellik doğru olsa bile mimarın eylemi (kontrolleri güçlendir) değişir mi — neden?
9. Benimseme %76→%84 çıkarken duygunun %70+→%60 düşmesi çelişki mi? 10+ yıl deneyimlilerin en şüpheci
   grup olması (%20,7 yüksek güvensizlik, %2,5 yüksek güven), METR'in "deneyimliler yine de
   hızlandıklarını sandı" bulgusuyla nasıl bir arada durur? Bu anketin mutlak rakamı yerine neden
   **trendini** kullanmalısın?
10. **Tek metrik tuzağı:** yöneticin "verimliliği ölçelim, aylık PR sayısına bakalım" diyor. Bu
    metriğin iki arızasını göster (neyi ölçüyor, nasıl kolayca şişirilir) ve **Goodhart yasasını**
    kendi cümlenle ifade et. Yerine hangi **ikili** okumayı önerirsin — bu depodaki somut
    karşılıklarıyla söyle. Aynı eleştiriyi
    [`harness-engineering-openai.md`](harness-engineering-openai.md)'nin "günde 3,5 PR" ve
    "1 milyon satır" sayılarına uygula: bu sayıları reddetmek yerine hangi **sınıfa** etiketlersin?
11. "Yayılma vergisi"ni somut kod üzerinden anlat: `KullanimService.istatistik()` içindeki beş satırlık
    bir bloğun üç kopyası varsa bir değişiklik sana ne borçlandırır? Sonra tersini yap — **73,0 rakamını
    neden "sektör gerçeği" diye aktaramazsın?** Üç şerhi say (çıkar hizalanması, döngüsel doğrulama,
    kısmi yıl) ve aynı raporun çürütülmüş metriklerini (moved code, function connectivity, legacy bakım)
    neden kullanmadığımızı açıkla.

## Kaynaklar
- **METR RCT (birincil çalışma):** arXiv:2507.09089 — *Measuring the Impact of Early-2025 AI on Experienced
  Open-Source Developer Productivity*
- **METR duyuru yazısı:** metr.org/blog/2025-07-10-early-2025-ai-experienced-os-dev-study/
- **METR güncelleme (tasarım değişikliği, yeni tahminler, "very weak evidence"):**
  metr.org/blog/2026-02-24-uplift-update/
- **METR AI kullanım anketi (Mayıs 2026, 40 puanlık sapma):** metr.org/blog/2026-05-11-ai-usage-survey/
- **DORA 2025 raporu ("The Great Amplifier"):** dora.dev/dora-report-2025/
- **Stack Overflow Developer Survey 2025 (AI bölümü, n=49.009):** survey.stackoverflow.co/2025/ai
- **GitClear (blok tekrarı, yayılma vergisi):** gitclear.com/the_ai_code_quality_maintainability_gap

**İlgili özetler (aynı dizin):**
- [Değerlendirme-Güdümlü Geliştirme (EDD) ve LLM Gözlemlenebilirliği](evals-llm-observability.md)
- [FinOps ve AI Tokenomiği](finops-ai-tokenomics.md)
