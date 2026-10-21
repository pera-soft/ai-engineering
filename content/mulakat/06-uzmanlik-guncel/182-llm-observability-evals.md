---
slug: llm-observability-evals
baslik: "LLM Observability ve Değerlendirme: Evals, LLM-as-Judge, Trace/Span, A/B"
kademe: İleri
sira: 182
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Sıradan bir programda 2+2 her zaman 4'tür; test ya geçer ya kalır. **LLM** (Large Language Model — yazıyı okuyup yazı üreten büyük dil modeli) böyle değildir: **olasılıksaldır (probabilistic)**, aynı soruya iki kez farklı cevap verebilir ve "doğru cevap" çoğu zaman tek bir metin değildir. Bu yüzden "testler yeşil" demek yetmez.

Yerine iki disiplin gelir. **Değerlendirme testleri (evaluation, kısaca eval)**: modelin çıktısını ölçen otomatik sınavlar. **Gözlemlenebilirlik (observability)**: üretimde ne olup bittiğini dışarı verilen izlerden görebilme. Bu bölüm ikisini de sıfırdan anlatır.

### Temel kavramlar

- **İstem (prompt)**: modele verdiğin metin. **Jeton (token)**: modelin metni böldüğü küçük parça; kabaca bir kelime ya da kelimenin bir bölümü.
- **Değerlendirme testi (eval)**: bir çıktı kümesini belirli bir ölçüte göre puanlama işi.
- **Altın küme (golden dataset / eval set)**: elle doğrulanmış, sabit tutulan örnek soru–beklenen çıktı kümesi. Ölçümün terazisi budur.
- **Rubrik**: puanlama ölçütü — "cevap kaynağa dayanıyor mu, soruyu gerçekten yanıtlıyor mu, uydurma bilgi (hallucination) var mı?"
- **Dağıtım öncesi değerlendirme (offline eval)**: dağıtımdan **önce**, sabit altın küme üzerinde koşulur. CI hattındaki geriye gidiş kapısıdır.
- **Canlı değerlendirme (online eval)**: **üretimde**, gerçek trafik üzerinde ölçüm — kullanıcı geri bildirimi ve örneklenmiş otomatik puanlama.
- **Geriye gidiş (regresyon)**: yeni sürümün eskisine göre kötüleşmesi.
- **İzleme kaydı / izleme adımı (trace / span)**: bir isteğin uçtan uca kaydı **izleme kaydıdır**. İçindeki her adım — belge geri getirme (retrieval), istem kurulumu, model çağrısı, araç çağrısı, cevabı ayrıştırma — birer **izleme adımıdır**.
- **Hakem model (LLM-as-judge)**: bir modelin, başka bir modelin çıktısını rubriğe göre puanlaması.
- **A/B testi**: canlı trafiği ikiye bölüp iki sürümü aynı koşulda kıyaslama.

### Nasıl çalışır?

Ölçüm iki yerde yapılır ve ikisi **birbirini tamamlar, birbirinin yerini almaz**. Dağıtım öncesi değerlendirme ucuz, tekrarlanabilir ve otomatiktir; skor eşiğin altına düşerse dağıtım durur. Ama sabit bir kümedir: zamanla kullanıcıların sorduğu şey değişir (**dağılım kayması / distribution shift**) ve donmuş bir altın küme bunu ıskalar. Canlı değerlendirme gerçek dünyayı görür, ama pahalıdır ve seni dağıtımdan önce korumaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Offline eval CI kapısı, online eval üretim ölçümü">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki yerde ölçersin</text>
  <rect x="28" y="44" width="278" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">Offline (dağıtımdan önce)</text>
  <text x="167" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Sabit eval set + rubrik</text>
  <text x="167" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">CI regresyon kapısı:</text>
  <text x="167" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yeni prompt eskisini bozdu mu?</text>
  <text x="167" y="164" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">tekrarlanabilir, ucuz</text>
  <rect x="334" y="44" width="278" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Online (üretimde)</text>
  <text x="473" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Canlı trafik + geri bildirim</text>
  <text x="473" y="122" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">trace/span, örnekleme,</text>
  <text x="473" y="138" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">A/B ile karşılaştırma</text>
  <text x="473" y="164" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">gerçek dünya sinyali</text>
</svg>
```

Puanlama üç katmandır. **Kural/kod tabanlı** yöntem (düzenli ifade, JSON şeması, birebir eşleşme) kesin ve neredeyse bedavadır ama dar kapsamlıdır. **Referans tabanlı** yöntem (BLEU, ROUGE, **sayısal vektör temsili (embedding)** benzerliği) anlam yakınlığını ölçer; karşılığında elde hazır bir referans cevap ister.

**Hakem model (LLM-as-judge)** esnek ve ölçeklidir. Ancak hakem de bir modeldir ve **kendi önyargıları** vardır: seçeneklerden ilkini ya da sonuncusunu kayırma (position bias), uzun ve gösterişli cevaba meyil, kendi ürettiği metni kayırma. Bu yüzden hakemin ham puanına körü körüne güvenilmez. Dört önlem gerekir: net ve nesnel bir rubrik, düşük sıcaklık, seçenek sırasını karıştırmak ve **insan etiketli bir alt kümeyle kalibrasyon** — yani hakem insanla ne kadar uyuşuyor, onu ölçmek.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Puanlama yöntemleri: kural tabanlı, referans tabanlı, LLM-as-judge">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Puanlama yöntemleri: katı/dar → esnek/kalibrasyon gerek</text>
  <rect x="34" y="42" width="572" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="52" y="62" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">1) Kural / kod tabanlı</text>
  <text x="52" y="79" font-size="10.5" fill="#1C3247" font-family="system-ui">regex · JSON şema · exact-match — kesin ve ucuz, ama dar kapsam</text>
  <rect x="34" y="94" width="572" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="52" y="114" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2) Referans tabanlı</text>
  <text x="52" y="131" font-size="10.5" fill="#1C3247" font-family="system-ui">BLEU · ROUGE · embedding benzerliği — anlamsal, ama referans gerekir</text>
  <rect x="34" y="146" width="572" height="52" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="52" y="166" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">3) LLM-as-judge</text>
  <text x="52" y="183" font-size="10.5" fill="#1C3247" font-family="system-ui">rubriğe göre puanlar — esnek ve ölçekli, ama önyargılı</text>
  <text x="52" y="196" font-size="10.5" fill="#8A2E2E" font-family="system-ui">→ düşük sıcaklık + net rubrik + insan etiketiyle kalibrasyon</text>
  <text x="320" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Aşağı indikçe esneklik artar, güven için kalibrasyon zorunlulaşır.</text>
</svg>
```

Gözlemlenebilirlik tarafında tek bir "toplam süre" sayısı işe yaramaz. Bir LLM isteği çok adımlıdır ve asıl soru şudur: "8 saniyenin 6'sı nerede geçti?" Uçtan uca akışı bir **izleme kaydı**, her adımı da ayrı bir **izleme adımı** olarak kaydedersen darboğazı nokta atışı bulursun. Her adımda dört şey tutulur: gecikme (latency), harcanan jeton sayısı, girdi/çıktı ve hata.

Bu, dağıtık izlemenin (OpenTelemetry) izleme kaydı/adım modelinin LLM hattına doğrudan uygulanmış hâlidir. Üretimden toplanan izleme kayıtları aynı zamanda altın kümenin yakıtıdır: gerçek başarısızlıklar toplanıp değerlendirme kümesine eklenir, böylece küme canlı kalır.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Trace ve span: her adımın gecikmesi ayrı ölçülür, darboğaz retrieval">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Trace = uçtan uca akış · span = her adım (latency nerede?)</text>
  <line x1="40" y1="128" x2="600" y2="128" stroke="#5D7789" stroke-width="1"/>
  <rect x="40" y="70" width="230" height="40" rx="6" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="2"/>
  <text x="155" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">retrieval — darboğaz</text>
  <rect x="272" y="70" width="46" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <rect x="320" y="70" width="150" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="395" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">model çağrısı</text>
  <rect x="472" y="70" width="90" height="40" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="517" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">araç çağrısı</text>
  <rect x="564" y="70" width="36" height="40" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="155" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">6.0 sn</text>
  <text x="295" y="146" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">prompt</text>
  <text x="395" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1.2 sn</text>
  <text x="517" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">0.6 sn</text>
  <text x="582" y="146" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">parse</text>
  <text x="320" y="182" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Tek toplam süre darboğazı gizler; span kırılımı nokta atışı gösterir.</text>
  <text x="320" y="202" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Her span: latency · token · girdi/çıktı · hata</text>
</svg>
```

Son olarak satıcı iddiaları. "Modelimiz sizin görevinizde %95 doğru" cümlesi, **hangi veri kümesinde, hangi metrikle, hangi dağılımda** ölçüldüğü bilinmeden bağlayıcı değildir; benchmark sızıntısı ve seçmeci raporlama (cherry-picking) yaygındır. Kural: kendi altın kümende, kendi rubriğinle, kendi kullanıcı dağılımında ölçmediğin bir metrik seninle ilgili değildir.

### Mülakatta ne sorulur?

Sık sorulanlar: "Dağıtım öncesi ile canlı değerlendirme farkı nedir, hangisi ne zaman?" "Altın küme nasıl kurulur ve nasıl güncellenir?" "Hakem modele ne kadar güvenirsin?" "İstem değişikliğinin geriye gidiş yapmadığını nasıl kanıtlarsın?" "Gecikme darboğazını nasıl bulursun?" (izleme kaydı ve adımları). "İki istem sürümünü gerçek kullanıcıda nasıl kıyaslarsın?" (A/B).

Yanlış cevap kalıpları: tek örnekte elle denemek, sıcaklığı oynatmak, modeli büyütmek, satıcının metriğini olduğu gibi kabul etmek.

### Tek cümlede

Olasılıksal bir sistemi ancak ölçerek yönetebilirsin: dağıtımdan önce altın küme üzerinde değerlendirme testleri, üretimde izleme kaydı/adım kırılımı ve canlı değerlendirme; hakem model kullanıyorsan onu da insan etiketiyle kalibre et.

# llmeval-1 | Orta
## Soru
Yeni bir prompt sürümünü üretime almadan önce, eski sürüme göre bir gerileme (regresyon) olup olmadığını CI'da yakalamak için en uygun yöntem hangisidir?
## A
Üretimde canlı kullanıcıların şikâyet etmesini bekleyip ona göre düzeltmek
🤖 Bu reaktif bir sinyaldir; dağıtımdan önce regresyonu yakalamaz, zararı üretimde çekersin.
## B
Sabit bir altın küme üzerinde offline eval koşup skoru önceki sürümle kıyaslamak
🤖 Doğru: sabit kümede offline eval, dağıtım öncesi tekrarlanabilir bir regresyon kapısıdır.
## C
Modeli iki üç örnekte elle deneyip "iyi görünüyor" diyerek yayına almak
🤖 Birkaç örnek istatistiksel değildir; olasılıksal sistemde regresyonu güvenilir ölçmez.
## D
Yeni prompt'u önce yalnız geliştiricilerin kullandığı ortamda birkaç gün bekletmek
🤖 Bekleme süresi ölçüm değildir; kimse skor almadıysa regresyon yine görünmez.
## Doğru
B
## Açıklama
Dağıtımdan önce regresyonu yakalamanın doğru yolu **offline eval**'dir: sabit, temsili bir **altın küme (golden dataset)** üzerinde yeni sürümü koşup skoru önceki sürümle karşılaştırmak — tıpkı bir CI test kapısı gibi. Tekrarlanabilir, ucuz ve otomatiktir; skor eşiğin altına düşerse dağıtım durur. Canlı şikâyet beklemek (A) reaktiftir; birkaç örnekte elle bakmak (C) olasılıksal bir sistemde istatistiksel güç taşımaz; bir süre bekletmek (D) bir ölçüm üretmez, yalnızca kararı erteler. Kapının değerini belirleyen şey, kümenin gerçekten temsili olmasıdır.

# llmeval-2 | İleri
## Soru
LLM-as-judge (bir modelin başka bir modelin çıktısını puanlaması) kullanırken en kritik güven açığı ve doğru yaklaşım nedir?
## A
Judge modeli değerlendirilenden küçük seçmek yeterlidir; başka bir önlem gerekmez
🤖 Küçük judge daha güvenilir değildir; asıl mesele judge'ın önyargısını insan etiketiyle kalibre etmektir.
## B
Judge yanılmaz kabul edilir; verdiği skor doğrudan gerçek (ground truth) sayılır
🤖 Judge kendi hatalarına ve önyargısına sahiptir; körü körüne gerçek saymak yanıltır.
## C
Judge tümüyle gereksizdir; her çıktının insan tarafından elle puanlanması gerekir
🤖 İnsan altın standarttır ama ölçeklenmez; kalibre edilmiş judge ölçekli değerlendirmeyi sağlar.
## D
Judge'ın kendi önyargısı vardır; net rubrik, düşük sıcaklık ve insan etiketiyle kalibrasyon şarttır
🤖 Doğru: judge kalibre edilip önyargısı ölçülmeli, ham skoruna körü körüne güvenilmemeli.
## Doğru
D
## Açıklama
**LLM-as-judge** ölçeklenebilir ve esnektir, ama bir model olduğu için **kendi hataları ve önyargıları** vardır: konum yanlılığı (position bias — ilk veya son seçeneği kayırma), uzun ve gösterişli cevaba meyil, kendi ürettiği metni kayırma. Doğru kullanım: net ve nesnel bir **rubrik**, tutarlılık için **düşük sıcaklık**, seçenek sırasının karıştırılması ve skorların **insan etiketli bir alt kümeyle kalibrasyonu** — yani judge'ın insanla ne kadar uyuştuğunu ölçmek. Judge'ı yanılmaz saymak (B) yanıltır; boyut (A) tek başına güveni belirlemez; tümüyle insana dönmek (C) ölçeklenmez. Judge, insan denetimiyle kalibre edilmiş bir tahmin aracıdır.

# llmeval-3 | İleri
## Soru
Üretimde bir LLM ajanı yanıtları bazen çok yavaş dönüyor. "Gecikme retrieval'da mı, model çağrısında mı, araç çağrısında mı?" sorusunu doğrudan hangi observability yapısı yanıtlar?
## A
İsteği bir trace, her adımı ayrı span olarak kaydetmek; span süreleri darboğazı gösterir
🤖 Doğru: trace/span kırılımı her adımın latency'sini ayırır ve darboğazı nokta atışı bulur.
## B
Yalnız toplam istek süresini tek bir sayı olarak loglayıp ortalamasına bakmak
🤖 Tek toplam süre hangi adımın yavaş olduğunu söylemez; kırılım gerekir.
## C
Sunucunun CPU ve bellek grafiklerine bakıp en yüksek tepe noktasını aramak
🤖 Altyapı metrikleri dış servis beklemesini göstermez; süre çoğunlukla ağda geçer.
## D
Daha büyük bir model kullanmak; güçlü model adımları daha hızlı tamamlar
🤖 Büyük model genelde daha yavaştır; üstelik darboğazın yerini yine söylemez.
## Doğru
A
## Açıklama
Bir LLM isteği çok adımlıdır: retrieval → prompt kurulumu → model çağrısı → araç çağrısı → ayrıştırma. **Trace** uçtan uca akışı, **span**'lar içindeki her adımı temsil eder; her span'ın süresi, token'ı, girdi/çıktısı ve hatası kaydedilir. Böylece "toplam 8 saniyenin 6'sı retrieval'da" gibi bir darboğaz **nokta atışı** bulunur. Tek toplam süreyi loglamak (B) sorunun yerini gizler; CPU/bellek grafikleri (C) dış servis beklemelerini göstermez, çünkü süre işlemcide değil ağda geçer; model büyütmek (D) tanı aracı değildir ve latency'yi genelde kötüleştirir. Bu, OpenTelemetry'nin dağıtık izleme modelinin LLM hattına uygulanmış hâlidir.

# llmeval-4 | Orta
## Soru
İki farklı prompt sürümünün gerçek kullanıcılar üzerindeki etkisini (ör. çözüm oranı, memnuniyet) karşılaştırmak için üretimde en uygun yöntem hangisidir?
## A
Sürümlerden birini seçip diğerini hiç denememek; kararı ürün sahibine bırakmak
🤖 Kıyas olmadan hangisinin daha iyi olduğu bilinemez; bu bir test değildir.
## B
Yalnız offline eval setinde tek seferlik skor alıp üretim davranışına hiç bakmamak
🤖 Offline skor gerçek kullanıcı davranışını her zaman yansıtmaz; online kıyas gerekir.
## C
Trafiği bölüp iki sürümü canlıda paralel koşturan A/B testiyle metrikleri kıyaslamak
🤖 Doğru: A/B canlı trafiği bölüp iki varyantı aynı koşulda ölçer, gerçek etkiyi verir.
## D
İki sürümü farklı haftalarda arka arkaya yayınlayıp haftalık toplamları kıyaslamak
🤖 Ardışık haftalar farklı koşullar taşır; sonuç mevsimsellik ve trafik değişimiyle karışır.
## Doğru
C
## Açıklama
Gerçek kullanıcı etkisini kıyaslamanın doğru yolu **A/B testidir (online)**: canlı trafiği rastgele böl, A ve B sürümlerini **aynı koşullarda** göster, ilgilendiğin metrikleri (çözüm oranı, memnuniyet, latency, maliyet) istatistiksel anlamlılıkla karşılaştır. Offline eval (B) prompt kalitesini ölçer ama gerçek kullanıcı davranışını her zaman yansıtmaz; ikisi tamamlayıcıdır. Tek sürüm çalıştırmak (A) bir kıyas üretmez. Ardışık haftalarda yayınlamak (D) klasik bir tuzaktır: iki hafta arasındaki trafik bileşimi, mevsimsellik ve diğer değişiklikler sonuca karışır, farkın prompt'tan geldiğini kanıtlayamazsın. Ayrıca A/B'de yeterli örneklem ve süre yoksa sonuç gürültü olabilir; anlamlılık ve etki büyüklüğü birlikte değerlendirilir.

# llmeval-5 | İleri
## Soru
Bir satıcı "modelimiz sizin göreviniz için %95 doğru" diyor. Senior mühendis olarak doğru tutum nedir?
## A
İddiayı olduğu gibi kabul edip mimari dokümana referans olarak yazmak
🤖 Bağımsız doğrulanmamış satıcı metriği bağlayıcı değildir; kendi bağlamında ölçülmeli.
## B
Kendi altın kümende, kendi rubriğinle ve kendi üretim dağılımında ölçüp doğrulamak
🤖 Doğru: metrik ancak senin görev dağılımında ve senin rubriğinle doğrulanınca anlam taşır.
## C
Doğruluk yüksek olduğu için kendi eval altyapını hiç kurmamak, bütçeyi başka yere ayırmak
🤖 Eval kurmamak regresyonu ve gerçek performansı görünmez kılar; en riskli seçim.
## D
Satıcının yayımladığı genel benchmark tablosuna bakıp en yüksek skorlu modeli seçmek
🤖 Genel benchmark senin görev dağılımın değildir; sızıntı ve seçmeci raporlama yaygındır.
## Doğru
B
## Açıklama
Satıcı doğruluk iddiaları **hangi veri kümesinde, hangi metrikle, hangi dağılımda** ölçüldüğüne bağlıdır ve senin üretim trafiğinle örtüşmeyebilir. Senior tutum: iddiayı **kendi altın kümende, kendi rubriğinle, kendi kullanıcı dağılımında** yeniden ölçmek ve metriğin tanımını sorgulamak. İddiayı olduğu gibi kabul etmek (A) veya eval'i atlamak (C) doğrulama değildir. Genel benchmark tablosuna bakmak (D) da yeterli değildir: yayımlanmış benchmark'lar eğitim verisine sızmış (benchmark contamination) olabilir, seçmeci raporlanmış olabilir ve her hâlükârda senin görev dağılımını temsil etmez. Kendi bağlamında ölçülmeyen metrik bağlayıcı değildir.

# llmeval-6 | Uzman
## Soru
Bir ekibin altın kümesi yalnız kolay ve tipik örneklerden oluşuyor ve altı aydır hiç güncellenmedi. Bu kümeyle alınan yüksek skorun temel sorunu nedir?
## A
Küme çok küçük; örnek sayısını on katına çıkarmak tek başına sorunu çözer
🤖 Aynı kolay dağılımdan on kat örnek çoğaltmak temsil sorununu çözmez, sadece büyütür.
## B
Kolay örneklerde alınan yüksek skor modelin üretimde de iyi olduğunu kanıtlar
🤖 Kolay dilimde yüksek skor, zor ve uç vakalardaki davranış hakkında bilgi vermez.
## C
Altın küme bir kez kurulup dondurulmalıdır; güncellemek karşılaştırılabilirliği tümüyle bozar
🤖 Küme sürümlenerek büyütülür; dondurmak dağılım kaymasına karşı körlük yaratır.
## D
Küme üretim dağılımını temsil etmiyor: uç ve gerçek hata vakaları eksik, sürümlenip beslenmeli
🤖 Doğru: temsil etmeyen küme yüksek skoru anlamsız kılar; küme trace'lerle canlı tutulmalı.
## Doğru
D
## Açıklama
Bir altın kümenin değeri örnek sayısından değil, **üretim dağılımını ne kadar temsil ettiğinden** gelir. Yalnız kolay ve tipik örneklerden oluşan bir küme, zor sorular, belirsiz istekler, uzun bağlamlar ve gerçek hata vakaları hakkında hiçbir şey söylemez; alınan yüksek skor sahte bir güven verir. Altı ay güncellenmemiş olması ikinci sorunu doğurur: kullanıcıların sorduğu şey zamanla değişir (**dağılım kayması**) ve donmuş küme bunu göremez. Doğru pratik, üretim trace'lerinden gelen gerçek başarısızlıkları elle etiketleyip kümeye eklemek ve kümeyi **sürümlemektir** — böylece hem karşılaştırılabilirlik korunur hem küme canlı kalır. Örnek sayısını aynı kolay dağılımdan çoğaltmak (A) sorunu büyütür; kümeyi kalıcı dondurmak (C) körlüğü kalıcılaştırır.
