---
slug: ileri-ajansal
baslik: İleri / Ajansal
aciklama: Tek prompt'un ötesine geçen teknikler — zincirleme, düşün-eylem döngüsü, meta-prompting. 2026'da ajanlar bunların çoğunu içeride yapıyor; deseni bilmek yönetmek ve hata ayıklamak için gerekli.
---

### Bu bölümde ne öğreneceksin?

Şimdiye kadar tek bir istek yazdın. Bu bölümde birden çok adımdan oluşan kurulumları öğreneceksin: işi zincire bölmek, modele araç kullandırıp topladığı bilgiye göre ilerletmek ve bir işi ne zaman ajana devretmemek gerektiğini bilmek. En önemli ders sonda: **her işe ajan gerekmez.**

**Bu bölümün yeni terimleri:**

- **Ajan** (İng. *agent*) — hatırlatma: bir hedefe ulaşmak için kendi başına adım adım ilerleyen, araç çağırabilen yapay zekâ kurulumu. Tek soru-tek cevaptan farkı, **döngü** kurabilmesidir.
- **Araç** (İng. *tool*): modelin çağırabildiği dış program — dosya okuma, arama, komut çalıştırma, veritabanı sorgusu. Model aracı çağırır, sonucu okur, ona göre devam eder.
- **Düşün–eylem–gözlem** (İng. *ReAct*): ajanın çalışma döngüsü. Ne yapacağını düşünür, bir araç çağırır, sonucu gözler, tekrar düşünür.
- **İz** (İng. *trace*): ajanın attığı bütün adımların kaydı. Bir şey ters gittiğinde nedenini buradan bulursun.
- **Meta-prompting**: prompt'un kendisini modele iyileştirtmek.

# zincirleme | Prompt zincirleme | ai:false

## Görev
Tek bir dev prompt yerine, bir işi **birbirini besleyen adımlara** bölen bir zincir kur. Senaryo: bir metni önce taslak yaz, sonra gözden geçir, sonra düzelt.

## Kötü örnek
`bu API için mükemmel, hatasız, cilalı bir dokümantasyon yaz`

## İpucu
Tek atışta "mükemmel" istemek zayıf sonuç verir. İşi taslak → eleştir → düzelt zincirine böl; her adımın çıktısı bir sonrakinin girdisi olsun.

## İdeal çözüm
(Üç adımlı zincir:)
**1.** Bu API endpoint'i için bir dokümantasyon taslağı yaz — kabaca, cilalamadan.
**2.** (Taslak gelince) Bu taslağı bir okuyucu gözüyle eleştir: hangi bilgi eksik, hangi cümle belirsiz? Sadece eleştir, düzeltme.
**3.** (Eleştiri gelince) Eleştirdiğin noktaları gidererek son hali yaz.

## Neden
Prompt zincirleme, bir işi **birbirini besleyen adımlara** bölerek her adımı tek bir işe odaklar — ve ara çıktıyı senin denetimine açar. "Taslak → eleştir → düzelt" deseni özellikle güçlüdür çünkü modelin kendi işini eleştirmesini bir sonraki adımın girdisi yapar. 2026'da düşünen modeller çok-adımlı muhakemeyi içeride yapabilir; ama zincirleme hâlâ değerli — çünkü **ara adımı görmek ve müdahale etmek** istediğinde tek yol budur.

Bedeli: her halka ayrı bir çağrı, yani ayrı maliyet ve gecikme. Ayrıca zincirin bir halkasındaki hata sonraki halkalara taşınır ve büyür. Kısa işlerde zincir kurmak, kazandırdığından çoğunu geri alır.

# react | Düşün–eylem–gözlem (ReAct) | ai:false

## Görev
Bir ajanın bir araç (İng. *tool* — modelin çağırabildiği dış program) kullanarak bilgi toplayıp sonra karar vermesini istiyorsun. Onu **düşün → eylem → gözlem** döngüsüyle çalışmaya yönlendiren bir prompt yaz.

## Kötü örnek
`veritabanının neden yavaş olduğunu söyle`

## İpucu
Model, elinde veri olmadan tahmin eder. Ona önce düşünmesini, sonra bir araçla bilgi toplamasını, sonra gözleme göre ilerlemesini söyle.

## İdeal çözüm
Veritabanının neden yavaş olduğunu bul. Şu döngüyle ilerle: **Düşün** (olası sebep nedir), **Eylem** (bunu doğrulamak için hangi sorguyu/aracı çalıştıracaksın), **Gözlem** (sonuç ne). Her adımı yaz; bir sebebi elemeden diğerine geçme. Elinde `EXPLAIN` çıktısı (veritabanının bir sorguyu nasıl çalıştırdığını gösteren rapor) ve şema var.

## Neden
ReAct (Reasoning + Acting — muhakeme + eylem), modeli "düşün → bir eylem yap (araç çağır) → sonucu gözle → tekrar düşün" döngüsüne sokar. Böylece model havadan tahmin etmek yerine **gerçek bilgi toplayarak** ilerler. 2026'da kod ajanları bu döngüyü **içeride otomatik** çalıştırıyor — sen her adımı yazmıyorsun. Ama deseni bilmek şart: hem ajana doğru çerçeveyi vermek, hem de bir ajan yanlış gittiğinde onun izini (trace) okuyup hatayı bulmak için.

Döngünün kendi riski de var: model yanlış bir hipoteze kilitlenip aynı türden araç çağrılarını tekrarlayabilir ve dönüp durur. Panzehir: bir adım sınırı ("en fazla 5 deneme") ve "aynı sebebi ikinci kez doğrulamaya çalışma" kısıtı.

# gorev-bol | Çok-adımlı görevi böl | ai:false

## Görev
Bir ajana verilen büyük, bulanık bir görevi, ajanın **kaybolmadan** yürüteceği net alt-görevlere bölen bir prompt yaz.

## Kötü örnek
`kullanıcı kayıt sistemini baştan sona kur`

## İpucu
Büyük bulanık görev, ajanın kapsamı kaçırmasına ya da yanlış yerden başlamasına yol açar. Alt-görevleri ve sırayı sen çiz.

## İdeal çözüm
Kullanıcı kayıt akışını kuracağız. Şu sırayla, **her adımı bitirince durup bana göster:** (1) veri modeli (types), (2) depo katmanı + testi, (3) servis + doğrulama kuralları, (4) web endpoint'i. Bir adımı onaylamadan sonrakine geçme. İlk adımla başla.

## Neden
Büyük, bulanık bir görevi tek seferde bir ajana vermek, onu kapsamı kaçırmaya, yanlış yerden başlamaya ya da yarıda dağılmaya iter. Görevi net alt-adımlara bölüp **her adımda durup göstermesini** istemek, hem ajanı yolda tutar hem sana her aşamada müdahale şansı verir. Bu, "büyük problemi böl"ün ajansal hali: fark, adımlar arasına **onay kapıları** koymandır.

Ödünleşmeyi bil: her onay kapısı senin zamanını harcar. Yeni modeller uzun ve otonom (kendi başına) koşularda iyi sonuç veriyor; düşük riskli işlerde kapı sayısını azaltıp sonucu topluca denetlemek daha verimli olabilir. Kapıları **hatanın pahalı olduğu** yerlere koy.

# meta-prompt | Meta-prompting | ai:false

## Görev
Elindeki zayıf bir prompt'u, modele **kendi prompt'unu iyileştirterek** güçlendiren bir yaklaşım yaz.

## Kötü örnek
(Zayıf prompt'u olduğu gibi kullanmak) `bir blog yazısı yaz`

## İpucu
Model, iyi prompt'un neye benzediğini bilir. Ona doğrudan cevap ürettirmeden önce, prompt'unu nasıl iyileştireceğini sorabilirsin.

## İdeal çözüm
Sana birazdan bir görev vereceğim ama önce prompt'u birlikte iyileştirelim. Şu isteği daha iyi bir prompt haline getir: "bir blog yazısı yaz". **Hangi bilgileri sana vermem gerektiğini** (konu, kitle, uzunluk, ton, amaç) bir kontrol listesi olarak çıkar — henüz yazma. Listeyi doldurunca yazarsın.

## Neden
Meta-prompting, modeli **kendi prompt'unu iyileştirmek** için kullanır: model, iyi bir prompt'un hangi bilgileri içermesi gerektiğini zaten bilir, ona sorarsın. Bu, ne istediğinden emin olmadığında ya da bir prompt'u sistematik güçlendirmek istediğinde işe yarar. 2026'da bu fikir otomatik prompt optimizasyonuna kadar uzandı: araçlar prompt'ları kendileri iyileştiriyor.

Şerh: modelin ürettiği "daha iyi prompt" genelde **daha uzun** prompt olur ve uzunluk kalite değildir. Ayrıca model senin bağlamını bilmez; ürettiği kontrol listesi seni gerçekten önemli olan yerine genel geçer maddelere sürükleyebilir. Listeyi bir başlangıç kabul et, sonuç değil.

# ajan-gerekmez | Her işe ajan gerekmez | ai:false

## Görev
Basit bir iş için ajansal (çok-adımlı, araç-kullanan) bir kurulum yerine **düz, tek adımlı** bir prompt'un yeterli olduğunu gösteren bir örnek yaz.

## Kötü örnek
`bir ajan kur: önce dosyayı oku, sonra analiz et, sonra bir araçla kontrol et, sonra rapor üret — bu değişkenin adını camelCase yap`

## İpucu
Ufak, tek adımlı bir iş için çok-adımlı ajan kurmak zaman, token ve para israfıdır. İşi olduğu gibi, tek prompt'ta iste.

## İdeal çözüm
Şu değişken adını camelCase yap: `user_first_name`. (Tek prompt, tek adım — ajan, araç, döngü yok.)

## Neden
2026'da güçlü bir sapma var: her işi ajansal (çok-adımlı, araç-kullanan, kendi kendine planlayan) bir kuruluma sokmak moda oldu — ama basit bir iş için bu **token, zaman ve para israfı**, üstelik daha kırılgan: ne kadar çok adım, o kadar çok kırılma noktası. Ajan mimarisi; işin çok adımlı, önceden tam tarif edilemeyen, keşif gerektiren bir iş olduğu durumlar içindir. Tek atışta net cevabı olan bir iş (isim dönüştürme, kısa bir çeviri, tek bir sınıflandırma) için düz bir prompt hem daha ucuz hem daha güvenilirdir.

Karar için dört soru: iş çok adımlı mı? adımlar önceden yazılamayacak kadar belirsiz mi? sonuç, ek maliyeti hak edecek kadar değerli mi? hata olursa yakalanıp geri alınabilir mi? Dördüne birden "evet" diyemiyorsan ajan kurma. Ustalık, ne zaman ajana gerek **olmadığını** bilmektir.

# zero-shot-once | Düşünen modelde önce örneksiz dene | ai:false

## Görev
Bir düşünen modelden bir çıktı istiyorsun ve refleksle beş örnek (few-shot) yığmak üzeresin. Bunun yerine **önce örneksiz** deneyen, gerekirse tek örnek ekleyen bir yaklaşım yaz.

## Kötü örnek
`şu tarzda commit mesajı yaz: [örnek1] [örnek2] [örnek3] [örnek4] [örnek5]` (kendi içinde muhakeme yapan bir modele)

## İpucu
Düşünen modeller örneklerdeki her ayrıntıya çok dikkat eder; fazla ya da tutarsız örnek yanlış deseni öğretebilir. Önce net talimatla örneksiz dene.

## İdeal çözüm
(İki aşamalı yaklaşım:)
**1.** Önce örneksiz: "Şu değişiklik için commit mesajı yaz: tek satır, emir kipi, başta köşeli parantezli faz etiketi."
**2.** Çıktı biçimi tam oturmazsa, **tek** net örnek ekle: "Şuna benzesin: `[faz-2] Token tablosunu ekle`."

## Neden
2026 nüansı: klasik "ne kadar çok örnek o kadar iyi" varsayımı düşünen modellerde geçerli değil. Bu modeller örneklerdeki **her ayrıntıya** çok dikkat eder — talimatla çelişen ya da gereksiz örnek, istemediğin bir deseni öğretebilir ve çıktıyı bozabilir. Önerilen yol: **önce örneksiz** (zero-shot: net talimat, örneksiz) dene; yalnızca çıktı biçimi tam oturmazsa **bir** temiz örnek ekle. Örnek bir çözüm değil, son çare bir ince ayardır.

Tersini de bil: örnek hâlâ vazgeçilmez olduğu yerler var — kelimeyle tarif edilemeyen bir tarz, kuruma özgü bir etiket sistemi, alışılmadık bir çıktı düzeni. Orada bir-iki **tutarlı** örnek en kısa yoldur. Kural "örnek kötüdür" değil; **örneği ihtiyaç kanıtlanınca ekle**.

# ileri-birlestir | İleri teknikleri birleştir | ai:true

## Görev
Öğrendiklerini birleştir. Senaryo: bir kod ajanına, mevcut bir hatayı bulup düzeltmesini istiyorsun — ama önce araştırıp anlamasını, sonra plan yapmasını, sonra uygulamasını, her aşamada durmasını istiyorsun. En iyi prompt'u yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
Görevi denetlenebilir adımlara böl (araştır → planla → uygula), araçla bilgi toplamasını iste (düşün-eylem-gözlem), her adımda durup göstermesini iste, ve emir kipi kullan.

## İdeal çözüm
Şu hatayı bulup düzelteceğiz: `[hata]`. Şu sırayla ilerle ve **her adımı bitirince durup göster:** (1) **Araştır** — ilgili kodu ve log'u oku, kök nedeni düşün-eylem-gözlem döngüsüyle daralt; henüz düzeltme. (2) **Planla** — düzeltme planını madde madde ver; henüz kod yazma. (3) **Uygula** — planı onaylayınca en küçük düzeltmeyi doğrudan uygula, testi çalıştır. Kök nedenden emin olmadan düzeltmeye geçme; emin değilsen hangi bilginin gerektiğini söyle.

## Neden
İleri/ajansal prompt'lamanın özü, tek dev istek yerine **denetlenebilir bir akış** kurmaktır: araştır (bilgi topla) → planla (onaylat) → uygula (emir kipiyle). Düşün-eylem-gözlem döngüsü kök nedeni gerçek veriyle daraltır; adımlar arasındaki durak noktaları sana müdahale şansı verir; "emin değilsen sor" uydurmayı engeller. 2026'da ajanlar bu akışı içeride yürütse de, onu doğru çerçevelemek ve yanlış gittiğinde izini sürmek senin işin.

Ölçüyü kaçırma: bu üç aşamalı akış, kök nedeni belirsiz gerçek hatalar içindir. Tek satırlık, nedeni bariz bir düzeltmede "araştır → planla → uygula" töreni, işi üç katına çıkarır ve hiçbir şey kazandırmaz.
