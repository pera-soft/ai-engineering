---
slug: tech-lead-davranissal
baslik: "Tech Lead Davranışsal: STAR Yöntemi, Karar Liderliği, Blameless Postmortem ve Teknik Borç Savunması"
kademe: Orta
sira: 195
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Tech lead (teknik ekip lideri) mülakatının yarısı kod değildir. Sana "bir çatışmayı anlat", "zor bir teknik kararı nasıl verdin", "yeni bir mühendise nasıl yol gösterdin" diye sorulur. Bunlara **davranışsal** (behavioral) soru denir. Dayandıkları varsayım basittir: bir insanın geçmişte gerçekten ne yaptığı, gelecekte ne yapacağının en iyi habercisidir. Ölçülen şey teknik bilgi değil, **liderlik olgunluğudur**: karar verebilme, gerekçeni açıklayabilme, anlaşmazlığı yönetebilme, hatadan öğrenme.

### Temel kavramlar

Hiç duymadığını varsayarak:

- **STAR yöntemi:** cevabı dört parçaya bölen anlatım iskeleti. **S**ituation (durum: bağlam neydi), **T**ask (görev: senin sorumluluğun neydi), **A**ction (eylem: **sen** ne yaptın — "biz" değil, "ben"), **R**esult (sonuç: ölçülebilir çıktı ya da öğrenilen ders). Ağırlık Action ve Result'ta olur; Situation ve Task birer-iki cümle kalır.
- **ADR (Architecture Decision Record):** alınan bir mimari kararı yazıya döken kısa belge; bağlamı, seçilen seçeneği, **reddedilen alternatifleri** ve ödünleri içerir.
- **Olay sonrası inceleme (postmortem):** bir kesinti bittikten sonra "ne oldu, neden oldu, tekrarı nasıl önlenir" sorularının yazıldığı belge. Adı tıptan gelir: ölüm sonrası inceleme.
- **Suçsuz (blameless):** o incelemede kişiyi değil sistemi sorgulama ilkesi.
- **Psikolojik güvenlik:** ekipte hata söylemenin, bilmediğini itiraf etmenin cezalandırılmayacağına dair güven.
- **Teknik borç (technical debt):** bugün hızlı ilerlemek için seçilen kestirme çözümün, ileride fazladan iş olarak geri dönmesi. Kredi kartı gibi düşün: borcun **faizi**, o modüle dokunan her yeni işin yavaşlamasıdır.

```svg
<svg viewBox="0 0 640 176" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="STAR yöntemi dört adım">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">STAR — davranışsal cevabın iskeleti</text>
  <rect x="24" y="44" width="140" height="104" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="94" y="70" text-anchor="middle" font-size="15" font-weight="700" fill="#186399" font-family="system-ui">S</text>
  <text x="94" y="92" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Situation</text>
  <text x="94" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağlam / durum</text>
  <rect x="176" y="44" width="140" height="104" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="246" y="70" text-anchor="middle" font-size="15" font-weight="700" fill="#25804C" font-family="system-ui">T</text>
  <text x="246" y="92" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Task</text>
  <text x="246" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">senin görevin</text>
  <rect x="328" y="44" width="140" height="104" rx="12" fill="#FBF0E4" stroke="#C77A17" stroke-width="1.5"/>
  <text x="398" y="70" text-anchor="middle" font-size="15" font-weight="700" fill="#C77A17" font-family="system-ui">A</text>
  <text x="398" y="92" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Action</text>
  <text x="398" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">SEN ne yaptın</text>
  <text x="398" y="130" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">(en ağır kısım)</text>
  <rect x="480" y="44" width="136" height="104" rx="12" fill="#F0E9F4" stroke="#7A4C99" stroke-width="1.5"/>
  <text x="548" y="70" text-anchor="middle" font-size="15" font-weight="700" fill="#7A4C99" font-family="system-ui">R</text>
  <text x="548" y="92" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Result</text>
  <text x="548" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ölçülebilir sonuç</text>
</svg>
```

### Nasıl çalışır?

**STAR'ı doğru kullanmak.** En sık hata, Action'ı "biz karar verdik, biz yaptık" diye anlatmaktır: mülakatçı ekibin değil senin ne yaptığını değerlendirir, "biz" katkını görünmez kılar. İkinci hata Situation'ı uzatıp sinyali boğmaktır. Üçüncüsü Result'ı hiç söylememektir — "gecikme %40 düştü", "kesinti sayısı yarıya indi" gibi bir çıktı olmadan hikâye eksik kalır. STAR yalnız başarı hikâyeleri için de değildir: başarısızlık sorularında Result'ın yerini **öğrenilen ders** alır.

**Karar liderliği.** Kararın kendisi kadar gerekçesi de kalıcı olmalıdır. Sözlü karar unutulur, gizli karar güveni düşürür, ertelenen karar (analiz felci) ekibi kilitler. Doğru refleks **ADR** yazmaktır: altı ay sonra "neden Postgres, neden Mongo değil" sorulduğunda cevap hazırdır ve tartışma yeniden açılmaz.

**Çatışma yönetimi.** Sağlıklı çatışma fikir çatışmasıdır, kişi çatışması değil. Önce anlaşmazlığı nesnelleştir: iddiaları veriyle, küçük bir prototiple veya ölçütle test et. Ama her ayrılık veriyle bitmez; bir noktada karar verilmelidir. Olgun davranışın adı **"katılmasan da arkasında dur" (disagree and commit)**: kendi görüşün seçilmese bile kararı tam destekle, uygulamada sessiz direnç gösterme. Otoriteyle susturmak, sürekli geri çekilmek ve herkes %100 hemfikir olana dek beklemek — üçü de kaliteyi, hızı ya da güveni feda eder.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Suçsuz postmortem: kişiyi değil sistemi sorgula">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Postmortem: kişiyi mi, sistemi mi sorguluyorsun?</text>
  <rect x="26" y="42" width="270" height="144" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="161" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Suçlayıcı ("insan hatası")</text>
  <text x="161" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">mühendisi adlandır, suçla</text>
  <text x="161" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ korku kültürü</text>
  <text x="161" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ hatalar gizlenir</text>
  <text x="161" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">analiz durma noktasında biter</text>
  <rect x="344" y="42" width="270" height="144" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="479" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Blameless (sistemi sorgula)</text>
  <text x="479" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"tek push nasıl üretime gitti?"</text>
  <text x="479" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">guardrail / canary / rollback?</text>
  <text x="479" y="136" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ aksiyon maddeleri (sahip+tarih)</text>
  <text x="479" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">öğrenen organizasyon</text>
</svg>
```

**Suçsuz postmortem.** Bir kesintiden sonra en kolay şey, yanlış komutu yazan kişiyi bulup dosyayı kapatmaktır. Modern **SRE** (Site Reliability Engineering — sistem güvenilirliği mühendisliği) kültürü bunu reddeder. Varsayım şudur: iyi niyetli ve yetkin bir mühendis bu hatayı yapabildiyse, asıl kırılgan olan **sistemdir**. Kişiyi suçlamak korku üretir, korku hatayı gizletir, gizlenen hata daha büyük patlar.

"Kök neden: insan hatası" demek de analizi tam başlaması gereken yerde durdurur. Doğru soru şudur: tek bir yanlış ayar (config) nasıl doğrulanmadan üretime gidebildi? Neden **kademeli yayın (canary)**, koruma bariyeri (guardrail) ya da otomatik geri alma (rollback) yoktu? İncelemeyi hiç yapmamak — "kesinti kısaydı, moral bozulmasın" — ise tekrarı davet eder.

Sağlam akış üç parçalıdır: nesnel bir **zaman çizelgesi**, tek bir kök neden yerine **katkıda bulunan faktörler** listesi, bir de sahibi ile tarihi yazılmış **aksiyon maddeleri**. Hesap verebilirlik sürecin sorumluluğudur, günah keçisinin değil.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teknik borcu iş etkisine tercüme et">
  <defs>
    <marker id="techlead-a" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Teknik borcu iş etkisine tercüme et</text>
  <rect x="24" y="70" width="170" height="70" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="109" y="98" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">"kod çirkin"</text>
  <text x="109" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iş değeri yok, ret</text>
  <line x1="196" y1="105" x2="232" y2="105" stroke="#5D7789" stroke-width="1.5" marker-end="url(#techlead-a)"/>
  <rect x="238" y="44" width="378" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="427" y="69" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hız: her özellik %30 daha uzun (velocity düşüşü)</text>
  <rect x="238" y="90" width="378" height="40" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="427" y="115" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Risk: son çeyrekte 3 üretim incident'i bu modülden</text>
  <rect x="238" y="136" width="378" height="40" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="427" y="161" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Maliyet: onboarding 2 hafta uzuyor → ROI + ödeme planı</text>
</svg>
```

**Teknik borcu savunmak.** Bu bir iletişim problemidir. Ürün yöneticisi mühendislik estetiğiyle değil iş sonuçlarıyla konuşur; "bu kod berbat" cümlesi onun dilinde "iş değeri yok" diye karşılık bulur. Borcu tercüme et: **hız** ("bu modüle dokunan her özellik %30 daha uzun sürüyor"), **risk** ("son çeyrekteki üç üretim kesintisi buradan çıktı"), **maliyet** ("yeni mühendisin uyum süresi iki hafta uzuyor"). Sonra borcu ödemeyi bir yatırım gibi sun: ölçüt, beklenen kazanç ve gerçekçi bir plan (her sprint'in %20'si ya da hedefli bir borç sprint'i). Borcu gizlice tahminlerin içine saklamak şeffaflığı bozar; tümden yok saymak ise borcu bileşik faizle büyütür.

**Mentorluk.** Amaç özerklik üretmektir: yeni mühendis benzer sorunları ileride **sensiz** çözebilmelidir. Hazır cevap vermek de, hatayı sen çözüp göstermek de kısa vadede hızlıdır ama bağımlılık yaratır. Tümüyle yalnız bırakmak ise hüsrana ve güven kaybına yol açar.

Etkili yol **sorularla yönlendirmektir**: "Hatayı ilk nerede fark ettin? Hangi girdide oluyor? Kayıtlarda (log) ne görüyorsun? Nasıl izole ederdin?" Böylece sistematik hata ayıklamayı örnekleyerek öğretmiş olursun. Kod incelemesinde de "şunu değiştir" deme, **"neden"** olduğunu açıkla; zamanla desteğini kademeli azalt.

### Mülakatta ne sorulur?

Altı klasik soru vardır:

- STAR ile anlatılan bir zorluk hikâyesi (en sık tuzak: "biz" dili ve eksik Result).
- Zor bir teknik kararı nasıl verip nasıl belgelediğin.
- Kıdemli biriyle yaşadığın görüş ayrılığını nasıl kapattığın.
- Bir üretim kesintisinin ardından olay sonrası incelemeyi nasıl yürüttüğün.
- Takılan bir mühendise nasıl yol gösterdiğin.
- Teknik borç için nasıl kapasite kopardığın.

Her birinde aynı sinyal aranır: somut eylem, açık gerekçe, ölçülebilir sonuç.

### Tek cümlede

Tech lead davranışsal mülakatı, kararı verip gerekçesini yazabildiğini, anlaşmazlığı kişiselleştirmeden kapatabildiğini, hatayı kişide değil sistemde arayabildiğini ve teknik borcu iş diline çevirebildiğini STAR iskeletiyle somut örnekler üzerinden kanıtlamaktır.

# techlead-1 | Orta
## Soru
Davranışsal mülakatta "Zor bir sorunu nasıl çözdüğünü anlat" sorusuna **STAR yöntemiyle** cevap verirken en sık yapılan hata nedir?
## A
Action kısmını "biz" diye anlatıp kişisel katkını belirsiz bırakmak; ağırlık Action ve Result'ta olmalıdır
🤖 Doğru: "biz" tuzağı bireysel katkıyı gizler; STAR senin eylemini ve sonucunu ölçmek içindir.
## B
Situation'ı olabildiğince uzun anlatıp teknik derinlik göstermek; bağlam ne kadar ayrıntılıysa mülakatçı o kadar iyi değerlendirir
🤖 Aksine, uzun bağlam sinyali boğar; Situation ve Task kısa olmalı, ağırlık Action'da kalmalıdır.
## C
Result'ı hiç söylememek; sonucu vurgulamak övünme sayılır, alçakgönüllülük daha olumlu bir izlenim bırakır
🤖 Result olmadan cevap eksiktir; ölçülebilir sonuç değerlendirmenin ana sinyalidir.
## D
STAR yalnız başarı hikâyelerinde işe yarar; başarısızlık ve öğrenilen ders sorularında bambaşka bir çerçeve kullanmak gerekir
🤖 STAR başarısızlık ve öğrenme sorularında da geçerlidir; Result yerine "öğrenilen ders" konur.
## Doğru
A
## Açıklama
**STAR** (Situation, Task, Action, Result) davranışsal cevabı yapılandırır, ama işe yaramasının koşulu **Action**'ı birinci tekil şahısla anlatmaktır: mülakatçı ekibin değil, **senin** ne yaptığını değerlendirir. "Biz karar verdik, biz yaptık" ifadeleri katkıyı silikleştirir — kıdem sinyalini zayıflatır. Doğru denge: Situation ve Task birer-iki cümle (yalnız gerekli bağlam), ağırlık **Action** (aldığın kararlar, uzlaştırdığın taraflar) ve **Result** (metrik: gecikme %X düştü, incident yarıya indi, veya "şunu öğrendim") üzerinde. B (uzun bağlam), C (sonucu atlama), D (yalnız başarı) STAR'ın çalışma biçimini yanlış anlar; başarısızlık sorularında bile Result yerine "öğrenilen ders" konur.

# techlead-2 | Orta
## Soru
Ekip iki veritabanı teknolojisi arasında kararsız. Tech lead olarak kararı verdikten sonra en sağlam liderlik davranışı hangisidir?
## A
Kararı stand-up'ta sözlü söyleyip geçmek; yazıya dökmek bürokrasidir, ekip zaten toplantıda duyduğunu hatırlar
🤖 Sözlü karar unutulur ve "neden" kaybolur; yazısız karar altı ay sonra baştan tartışılır.
## B
Kararı paylaşmadan uygulamayı yönlendirmek; gerekçeyi açıklamak kararı tartışmaya açar ve otoriteyi zayıflatır
🤖 Şeffaf gerekçe otoriteyi güçlendirir; gizli karar güveni ve sahiplenmeyi düşürür.
## C
Kararı bir ADR ile yazmak: bağlam, seçilen seçenek, reddedilen alternatifler ve ödünler — böylece "neden" kalıcı olur
🤖 Doğru: ADR kararın gerekçesini ve reddedilen alternatifleri kalıcılaştırır, kurumsal hafıza olur.
## D
Kararı ertelemek ve daha çok veri toplamayı sürdürmek; belirsizlik varken karar vermek sorumsuzluk sayılır, önce tam netlik gerekir
🤖 Analiz felci (analysis paralysis) liderlik değildir; belirsizlik altında zamanında karar vermek gerekir.
## Doğru
C
## Açıklama
Teknik karar liderliğinin özü **kararın kendisi kadar gerekçesini de kalıcılaştırmaktır**. **ADR** (Architecture Decision Record) tam bunu yapar: kısa bir belge — bağlam, değerlendirilen seçenekler, **seçilen** seçenek ve kritik olarak **reddedilen alternatifler ve nedenleri** + trade-off'lar. Faydası: (1) "neden Postgres, neden Mongo değil" sorusu altı ay sonra tekrar açıldığında cevap hazırdır (karar churn'ü azalır); (2) yeni ekip üyesi bağlamı okuyarak öğrenir; (3) karar kişiye değil gerekçeye bağlanır, tartışma nesnelleşir. A (sözlü/uçucu), B (gizli karar), D (analiz felci) hem şeffaflığı hem zamanında kararlılığı ıskalar.

# techlead-3 | Orta
## Soru
Bir mimari tartışmada kıdemli bir mühendisle güçlü bir görüş ayrılığına düştün; ekibin ilerlemesi gerekiyor. Sağlıklı çatışma yönetimi hangisidir?
## A
Kıdemli olduğun için kararı dayatıp tartışmayı kapatmak; hiyerarşi anlaşmazlığı en hızlı çözen mekanizmadır
🤖 Otoriteyle susturmak kısa vadede ilerletir ama sahiplenmeyi ve güveni yıkar; sürdürülemez.
## B
Görüşleri veri veya prototiple nesnelleştir; karar çıkınca kendi görüşün seçilmese de "disagree and commit" ile tam destekle
🤖 Doğru: veri odaklı tartışma ve disagree-and-commit hem kaliteyi hem hızı hem güveni korur.
## C
Anlaşmazlıktan kaçınmak için görüşünü hemen geri çekip her seferinde karşı tarafa uymak; uyum ekip moralini korur ve herkese zaman kazandırır
🤖 Sürekli geri çekilmek (conflict avoidance) daha iyi kararı feda eder; sağlıklı çatışma bilgi üretir.
## D
Tartışmayı herkes %100 aynı fikirde olana dek sürdürmek; tam uzlaşma olmadan alınan karar ekipçe uygulanmaz
🤖 Tam uzlaşma (consensus) çoğu kez imkânsızdır ve ilerlemeyi kilitler; karar ve commit gerekir.
## Doğru
B
## Açıklama
Sağlıklı teknik çatışma **fikir çatışmasıdır, kişi çatışması değil**. Önce anlaşmazlığı **nesnelleştir**: iddiaları veriyle, küçük bir prototiple veya ölçütle (benchmark) test et — böylece tartışma egodan bilgiye kayar. Ama her ayrılık veriyle çözülmez; bir noktada karar sahibinin (veya mutabık karar mekanizmasının) sonuca varması gerekir. Burada olgun davranış Amazon'un da ilkeselleştirdiği **"disagree and commit"**: kararı sen vermemiş/önerin seçilmemiş olsa bile, alınan kararı **tam destekle**, pasif-agresif direnç veya sessiz sabotaj gösterme. A (otoriter dayatma), C (sürekli teslim/kaçınma), D (tam-uzlaşma beklentisi) ya kaliteyi ya hızı ya da güveni feda eder.

# techlead-4 | İleri
## Soru
Bir dağıtımdan (deployment) sonra üretim 40 dakika kesintiye uğradı; nedeni bir mühendisin yanlış config push'u. Postmortem'i nasıl yönetirsin?
## A
Hatayı yapan mühendisi incelemede açıkça adlandırıp sorumlu tutmak; hesap verebilirlik için birinin sonucu üstlenmesi gerekir
🤖 Kişiyi suçlamak korku kültürü yaratır; insanlar hata saklamaya başlar, sistem asıl kırılganlığı öğrenemez.
## B
İnceleme yapmamak; kesinti kısa sürdü ve üstüne gitmek ekibin moralini gereksiz yere bozar
🤖 İncelemeyi atlamak öğrenmeyi ve tekrarın önlenmesini engeller; her anlamlı kesinti bir ders kaynağıdır.
## C
Kök nedeni "insan hatası" olarak kaydedip kapatmak; config'i yanlış yazan kişi bulunduğuna göre analiz tamamlanmıştır
🤖 "İnsan hatası" bir kök neden değil, durma noktasıdır; asıl soru sistemin tek bir yanlış değişikliğe neden bu kadar kırılgan olduğudur.
## D
Suçsuz (blameless) bir inceleme yürüt: kişiyi değil sistemi sorgula, sahipli aksiyon maddeleri çıkar
🤖 Doğru: suçsuz yaklaşım psikolojik güvenlik ve sistemik iyileştirme sağlar; öğrenen organizasyonun temelidir.
## Doğru
D
## Açıklama
**Blameless (suçsuz) postmortem** modern SRE kültürünün temelidir. Varsayım: iyi niyetli, yetkin bir mühendis bu hatayı yaptıysa, **sistem** o hataya izin verecek kadar kırılgandır — asıl düzeltilecek şey budur. Kişiyi suçlamak (A) korku üretir, korku hata gizlemeye yol açar, gizlenen hatalar daha büyük patlar; "insan hatası" demek (C) analizi tam da başlaması gereken yerde durdurur. Doğru akış: nesnel **zaman çizelgesi**, **katkıda bulunan faktörler** (contributing factors, tek "kök neden" değil), ve sahibi/tarihi olan **aksiyon maddeleri** (config değişikliğine review zorunluluğu, canary dağıtım, otomatik rollback, guardrail). Postmortem'i atlamak (B) tekrarı davet eder. Sonuç: hesap verebilirlik **sürecin**dir, günah keçisinin değil.

# techlead-5 | Orta
## Soru
Bir junior mühendis takıldığı bir bug için sürekli sana gelip "cevabı" istiyor. Uzun vadede en etkili mentorluk yaklaşımı hangisidir?
## A
Her seferinde doğru cevabı hızlıca vermek; en verimli yol budur, junior da işi zamanında bitirmiş olur
🤖 Hazır cevap kısa vadede hızlıdır ama bağımlılık yaratır; junior problem çözmeyi öğrenemez, hep sana döner.
## B
Hiç yardım etmeyip bug'ı tek başına çözmesini beklemek; zorlanarak öğrenilen şey en kalıcı olanıdır
🤖 Tümüyle yalnız bırakmak hüsran ve güven kaybı yaratır; mentorluk yönlendirmedir, terk etme değil.
## C
Cevap yerine sorularla yönlendirmek ("logda ne var, nasıl izole edersin"); kod incelemesinde "değiştir" değil "neden"i açıklamak
🤖 Doğru: sorularla yönlendirmek ve "neden"i açıklamak junior'ın özerkliğini ve muhakemesini büyütür.
## D
Bug'ı sen çözüp commit etmek; junior bitmiş çözümü okuyarak doğru yaklaşımı örnek üzerinden öğrenir
🤖 Bitmiş çözümü okumak, çözme sürecini yaşamanın yerini tutmaz; sistematik hata ayıklama böyle edinilmez.
## Doğru
C
## Açıklama
Mentorluğun hedefi **özerklik** (autonomy) üretmektir: junior'ın gelecekte benzer sorunları **sensiz** çözebilmesi. Hazır cevap vermek (A) veya sen çözüp göstermek (D) kısa vadede hızlıdır ama bağımlılık yaratır ve muhakeme kasını çalıştırmaz; tümüyle yalnız bırakmak (B) ise hüsran ve güven erozyonudur. Etkili yol **Sokratik yönlendirmedir**: doğru sorularla junior'ın kendi düşünme sürecini yürütmesini sağla ("logda ne var, nasıl izole edersin, hangi hipotezi test ettin"), böylece **sistematik hata ayıklama** yöntemini modelle. PR review'da da "şunu değiştir" yerine **"neden"i** açıkla — kural değil, ilke öğret. Zamanla müdahaleni azalt (scaffolding'i geri çek). Bu, ölçeklenen bir ekip için "balık tutmayı öğretmek" ilkesidir.

# techlead-6 | İleri
## Soru
Ürün yöneticisi (PM) sürekli yeni özellik istiyor; sen bir refactoring/teknik borç ödemesi için sprint kapasitesi talep ediyorsun ama "iş değeri yok" diye reddediliyor. En etkili savunma stratejisi nedir?
## A
Borcu iş etkisine tercüme et: hız kaybını, üretim riskini ve uyum süresini sayıyla göster, ödemeyi planlı bir yatırım olarak sun
🤖 Doğru: iş diline çevrilmiş, metrikle desteklenen borç argümanı karar vericiyi ikna eder.
## B
"Bu kod berbat, mühendis olarak buna dayanamıyorum" diyerek estetik ve duygusal gerekçeyle ısrar etmek; kalite kaygısı kendi başına yeterli bir gerekçedir
🤖 Estetik gerekçe iş önceliğine çevrilemez; PM bunu "iş değeri yok" diye reddetmekte haklı görünür.
## C
İzin istemeden borcu özellik tahminlerinin (estimate) içine gizleyip sessizce ödemek; kimse fark etmezse öncelik tartışması da çıkmaz
🤖 Gizli iş şeffaflığı ve güveni bozar; büyük borç bu yolla sürdürülebilir biçimde ödenemez.
## D
Teknik borcu yok sayıp yalnız yeni özelliğe odaklanmak; sistem büyüdükçe yeniden yazım zaten kendiliğinden gündeme gelir
🤖 Borç birikir ve bileşik faiz gibi büyür; yok saymak ilerleme hızını giderek çökertir.
## Doğru
A
## Açıklama
Teknik borç savunması bir **iletişim ve çerçeveleme** problemidir. Karar verici (PM/işletme) mühendislik estetiğiyle değil, **iş sonuçlarıyla** konuşur; bu yüzden borcu onun diline çevirmelisin: (1) **hız** — "bu borç teslim hızımızı (velocity) %X yavaşlatıyor, her özellik daha pahalı"; (2) **risk** — "üretim incident'lerinin Y'si bu kırılgan modülden çıktı"; (3) **maliyet** — "yeni mühendisin onboarding'i bu karmaşa yüzünden uzuyor". Sonra ödemeyi bir **yatırım** olarak sun: somut ölçüt, tahmini kazanç ve gerçekçi bir ödeme planı (her sprint'in %20'si, veya hedefli bir "borç sprint'i"). Mümkünse SLO/metrikle destekle. B (duygusal), C (gizli/şeffaf değil), D (yok sayma) ya ikna edemez ya güveni bozar ya da borcu bileşik biçimde büyütür. Kıdem, doğru olanı savunabilmek kadar onu **doğru dilde** savunabilmektir.
