---
slug: kisitlama
baslik: Kısıtlama & Kontrol
aciklama: Modeli sınırlar içinde tutan teknikler — kapsam, kabul kriteri, "emin değilsen sor". 2026'da yeni bir tehlike: aşırı-emir dili modeli fazla tetikliyor.
---

### Bu bölümde ne öğreneceksin?

Model neyi yapacağını bilmek kadar, **nerede duracağını** da bilmelidir. Bu bölümde işin sınırlarını çizmeyi, "bitti"nin ne demek olduğunu önceden tanımlamayı ve modelin bilmediğini uydurmasını engellemeyi öğreneceksin. Ayrıca 2026'nın ters köşesi: bağırarak yazmak artık işe yaramıyor, hatta zarar veriyor.

**Bu bölümün yeni terimleri:**

- **Kapsam** (İng. *scope*): bir isteğin sınırları — nereye dokunulacak, nereye dokunulmayacak.
- **Kabul kriteri** (İng. *acceptance criteria*): çıktının "kabul edilebilir" sayılması için karşılaması gereken, önceden yazılmış maddeler.
- **Aşırı-tetikleme** (İng. *overtrigger*): modelin, gereğinden fazla iş yapması — gereksiz dosya okuması, gereksiz araç çağırması, küçük bir işi büyütmesi.
- **Üretim** (İng. *production*): yazılımın gerçek kullanıcılara hizmet verdiği canlı ortam. Buradaki hata gerçek zarar üretir.

# kapsam-sinir | Kapsam sınırı | ai:false

## Görev
Modelden bir hatayı düzeltmesini isterken, işi **yalnızca o hatayla** sınırlayan bir prompt yaz — model kapsamı genişletmesin.

## Kötü örnek
`bu dosyadaki 12. satırdaki hatayı düzelt`

## İpucu
"Düzelt" dediğinde model çevredeki başka şeyleri de "iyileştirmeye" kalkabilir. Sınırı açıkça çiz.

## İdeal çözüm
Aşağıdaki dosyanın **yalnızca 12. satırındaki** null kontrolü hatasını düzelt. **Başka hiçbir şeye dokunma:** başka satırları düzenleme, biçimlendirme (format) değiştirme, yeni özellik ekleme. Sadece o tek hatanın en küçük düzeltmesini ver.

```
[dosya]
```

## Neden
Modeller yardımsever olmaya eğilimlidir — istediğin tek düzeltmeyi yaparken çevredeki "iyileştirebileceği" her şeye de el atarlar. Bu, küçük bir düzeltmeyi denetlenemez bir değişiklik yığınına çevirir. Kapsam sınırı ("yalnızca 12. satır", "başka hiçbir şeye dokunma") modeli tam istediğin cerrahi müdahaleyle sınırlar. Özellikle düzeltme, taşıma (migration) ve refactor işlerinde kapsamı çizmek, ne yapılacağını söylemek kadar önemlidir. Karşı taraf da var: kapsamı gereğinden dar çizersen model, düzeltmenin gerçekten gerektirdiği komşu değişikliği de yapmaz ve kod kırık kalır. Sağlam formül: dar kapsam + "ilgisiz ama önemli bir şey görürsen düzeltme, sonda not olarak bildir".

# kabul-kriteri | Kabul kriteri / "bitti" tanımı | ai:false

## Görev
Modelden bir görev isterken, çıktının **ne zaman "kabul edilebilir"** sayılacağını önceden tanımlayan bir prompt yaz.

## Kötü örnek
`bir e-posta doğrulama fonksiyonu yaz`

## İpucu
"Bitti" ne demek, modelin tahminine bırakma. Kabul kriterlerini önceden say.

## İdeal çözüm
Bir e-posta doğrulama fonksiyonu yaz. **Kabul kriterleri:** (1) `@` ve alan adı içermeyen girdiyi reddetsin, (2) boş/null girdide false dönsün, (3) baştaki/sondaki boşluğu tolere etsin, (4) düzenli ifade (İng. *regex* — metin kalıbı eşleştirme yazımı) tek satırda okunabilir olsun. Fonksiyonun bu dört kriteri karşıladığından emin ol.

## Neden
Kabul kriteri (acceptance criteria), "bitti"nin ne olduğunu modelin tahminine bırakmak yerine **önceden ölçülebilir bir sözleşmeye** çevirir. Model artık "sanırım yeterli" demez; kriterleri işaretler. Bu ayrıca sana bir denetim aracı verir — çıktıyı aynı kriterlerle kontrol edebilir, hatta modele "bu kriterlere göre kendini denetle" diyebilirsin. Rubrik gibi, kabul kriteri de belirsizliği ölçülebilir hedefe çevirir. Zayıf yanı aynı: kriter listesi **yalnızca yazdığını** kapsar. Listelemediğin bir durum (mesela çok uzun girdi) sessizce açıkta kalır. Kriter listesi bir taban çizer; asıl güvence testtir.

# emin-degilsen-sor | "Emin değilsen sor / uydurma" | ai:false

## Görev
Modelin, bilmediği bir şeyi uydurmasını (halüsinasyon) önlemek için ona bir **kaçış kapısı** veren bir prompt yaz.

## Kötü örnek
`bu kütüphanenin foo() fonksiyonu ne yapıyor?`

## İpucu
Model bilmese bile ikna edici bir cevap uydurabilir. Ona "bilmiyorsan bilmiyorum de" iznini açıkça ver.

## İdeal çözüm
Aşağıdaki kütüphane koduna dayanarak `foo()` fonksiyonunun ne yaptığını açıkla. **Yalnızca verdiğim kodda görünene dayan.** Koddan emin olarak çıkaramadığın bir şey varsa uydurma; "bu koddan anlaşılmıyor" de ve neyin eksik olduğunu belirt.

```
[kod]
```

## Neden
Modeller boşluğu doldurmaya eğilimlidir — bilmedikleri şeyi bile ikna edici biçimde "uydururlar" (halüsinasyon). Buna karşı en basit ve güçlü savunma, modele **bir kaçış kapısı** vermektir: "emin değilsen uydurma, 'bilmiyorum' de." Bu izni açıkça vermezsen, model sessizce tahmin eder ve sen bunu gerçek sanırsın. "Yalnızca verdiğim veriye dayan" kısıtı ile "emin değilsen söyle" izni birlikte, uydurmayı büyük ölçüde keser.

Ama **tamamen kesmez** ve bunu bilmek hayati: model bazen yanlış olduğundan haberdar bile değildir, dolayısıyla "emin değilsen söyle" tetiklenmez. Aşırıya kaçmanın da bedeli var — çok baskılarsan model cevabı bildiği hâlde "bilmiyorum" demeye başlar. Kritik bilgiyi daima dış kaynakla doğrula.

# asiri-emir | Aşırı-emir dilinden kaçın | ai:false

## Görev
Aşağıdaki, her cümlesi bağıran prompt'u, modern bir modeli **fazla tetiklemeyecek** sakin ve net bir dille yeniden yaz.

## Kötü örnek
`ÇOK ÖNEMLİ!!! MUTLAKA her seferinde testleri çalıştır! ASLA atlamana İZİN YOK! KRİTİK: şüphedeysen KESİNLİKLE dosyayı oku!`

## İpucu
Bağıran, "MUTLAKA/ASLA/KRİTİK" dolu talimatlar yeni modellerde ters teper — model gereğinden fazla tetiklenir. Normal, koşullu dile çevir.

## İdeal çözüm
Bir kod değişikliği yaptıktan sonra ilgili testleri çalıştır. Bir dosyanın içeriğinden emin değilsen, değiştirmeden önce oku. Testler kırmızıysa, düzeltmeden işi bitmiş sayma.

## Neden
2026'nın ince tuzağı: yeni modeller talimatlara çok duyarlı hale geldi, ve eski "MUTLAKA / ASLA / KRİTİK / her zaman" tarzı bağıran dil artık **aşırı-tetikleme** (overtrigger) yapıyor — model gereksiz yere araç çağırır, gereğinden fazla dosya okur, basit işi büyütür. Bu dil, modellerin daha isteksiz olduğu eski dönemde onları harekete geçirmek için doğmuştu; o ihtiyaç ortadan kalktı, alışkanlık kaldı.

Aynı talimatı sakin, koşullu dille vermek ("şu durumda şunu yap") daha isabetli davranış üretir. Güç, ünlem sayısında değil, **koşulun netliğinde**. Şerh: vurgu tümüyle yasak değil — gerçekten yıkıcı bir sınır için (örn. "veritabanını silme") tek bir net vurgu yerindedir. Kural şu: vurgu **nadir** olmalı ki anlamı kalsın.

# uzunluk-ton | Uzunluk ve ton | ai:false

## Görev
Modelin cevabının **uzunluğunu ve tonunu** hedef kitleye göre ayarlayan bir prompt yaz.

## Kötü örnek
`bu teknik kararı açıkla`

## İpucu
"Açıkla" tek başına uzunluk ve ton içermez. Kime, ne kadar uzun, hangi tonda?

## İdeal çözüm
Bu teknik kararı (neden PostgreSQL yerine Redis seçtik) **teknik olmayan bir yöneticiye**, **en fazla 3 cümlede**, **jargon kullanmadan** açıkla. Amaç: kararın iş değerini anlaması.

## Neden
Uzunluk ve ton söylenmezse model varsayar — genelde uzun ve teknik yazar, sen kısa ve sade isterken. Bunları açıkça sınırlamak ("en fazla 3 cümle", "teknik olmayan yöneticiye", "jargonsuz") çıktıyı hedef kitleye oturtur. Bu bir kısıtlamadır ama aynı zamanda bir netlik aracı: model kime yazdığını bilince doğru kelimeleri seçer.

İki pratik not: (1) Sayı sınırları yaklaşıktır — "en fazla 3 cümle" genelde tutar, "tam 250 kelime" tutmaz; model kelime saymaz. Kesin uzunluk gerekiyorsa çıktıyı sen kırp. (2) Kısalık her zaman iyi değildir: karmaşık bir kararı üç cümleye sıkıştırmak, gerekli ödünleşmeleri düşürebilir. Uzunluğu **okuyucunun ihtiyacına** göre seç, alışkanlıktan değil.

# oncelik-ver | Çakışan kısıtlara öncelik ver | ai:false

## Görev
Bir işte gerçekten gerekli iki kısıt çakışabilir (ör. güvenlik vs. hız). Bunları yasaklamak yerine, çakıştığında **hangisinin kazanacağını** söyleyen bir prompt yaz.

## Kötü örnek
`hem çok güvenli hem çok hızlı olsun`

## İpucu
"Çelişkili talimat" ile karıştırma: bazen iki kısıt da gerçekten gerekli. O zaman modele bir öncelik SIRASI ver — çakışınca neyi feda edeceğini bilsin.

## İdeal çözüm
Bu doğrulama fonksiyonunu yaz. **Öncelik sırası (çakışırsa üstteki kazanır):** 1. güvenlik (hiçbir geçersiz girdi geçmesin), 2. okunabilirlik, 3. performans. Yani okunabilirlik ile güvenlik çakışırsa güvenliği seç; performans için okunabilirliği feda etme.

## Neden
Bazen iki kısıt aynı anda gerçekten gereklidir ve zaman zaman çakışırlar — bu bir çelişki değil, gerçek bir ödünleşmedir. Model bunu tek başına çözerken hangisini feda edeceğini tahmin eder ve yanlış tahmin edebilir. Çözüm, kısıtları yasaklamak değil, onlara bir **öncelik sırası** vermektir: "çakışırsa üstteki kazanır." Böylece model kararı senin değer sıralamana göre verir, kendi varsayımına göre değil. Bu, çelişkiyi çözmenin olgun halidir. Sınırı: sıralama, gerçekten çakışan kısıtlar içindir. Aslında **çelişkili** olan ("kısa ol ama her detayı yaz") bir istekte öncelik vermek sorunu gizler — orada yapılacak şey isteği düzeltmektir (bkz. 08. bölüm, `celiski`).

# kisitlama-birlestir | Kısıtlamayı birleştir | ai:true

## Görev
Öğrendiğin kısıtlama tekniklerini birleştir. Senaryo: bir yapay zekâ ajanına, üretimdeki (production — gerçek kullanıcılara hizmet veren canlı ortam) bir hatayı düzeltecek küçük ve güvenli bir yama (İng. *patch* — küçük düzeltme) yazdırmak istiyorsun; risk düşük olmalı. En iyi prompt'u yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
Kapsamı daralt (yalnızca bu hata), kabul kriteri koy (ne "bitti" sayılır), uydurmayı engelle (emin değilsen sor), ve bunu sakin-net dille yaz — bağırma.

## İdeal çözüm
Üretimde şu hata var: `[hata özeti]`. **Yalnızca bu hatayı** gideren en küçük yamayı yaz. Kabul kriterleri: (1) mevcut davranışın geri kalanını değiştirme, (2) yeni bağımlılık ekleme, (3) düzeltme tek dosyada kalsın. Hatanın kök nedeninden emin değilsen, yama önermeden önce hangi bilginin (log, girdi, şema) gerektiğini söyle. Değişikliğin neden güvenli olduğunu tek cümleyle gerekçelendir.

## Neden
İyi kısıtlama birkaç sınırı birlikte kurar: kapsamı daraltır (yalnızca bu hata, tek dosya), "bitti"yi tanımlar (kabul kriterleri), uydurmayı engeller (emin değilsen sor), ve bunu aşırı-emir diline düşmeden sakin-net yazar. Özellikle riskli işlerde (üretim yaması), modeli neyi yapmayacağı konusunda sınırlamak, ne yapacağını söylemek kadar hayatidir. Son söz: hiçbir prompt kısıtı bir **güvence** değildir. Model kısıtı çiğneyebilir; üretime giden bir değişikliği daima testle ve insan gözüyle doğrula. Prompt kısıtları hata olasılığını düşürür, sıfırlamaz.
