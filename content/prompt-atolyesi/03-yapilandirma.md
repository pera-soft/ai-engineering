---
slug: yapilandirma
baslik: Yapılandırma
aciklama: Prompt'un ve çıktının biçimini düzenleyen teknikler — bölümleme, bilgi sıralaması, çıktı iskelesi. 2026'da "ortada kaybolma" hâlâ gerçek, structured output ise API seviyesine indi.
---

### Bu bölümde ne öğreneceksin?

Aynı kelimeler, farklı sırayla ve farklı düzende yazıldığında farklı sonuç verir. Bu bölümde prompt'un içindeki parçaları nasıl ayıracağını, hangi bilgiyi nereye koyacağını ve çıktının biçimini nasıl kesin olarak dikte edeceğini öğreneceksin.

**Bu bölümün yeni terimleri:**

- **Sınırlayıcı** (İng. *delimiter*): prompt'un içindeki parçaları birbirinden ayıran işaret — `<kod>…</kod>` gibi etiketler ya da başlıklar. Modelin "bu talimat, şu veri" ayrımını yapmasını sağlar.
- **JSON**: verinin makine tarafından okunabilir biçimde yazıldığı standart bir metin formatı. `{"ad": "değer"}` şeklinde ad-değer çiftlerinden oluşur; hemen her programlama dili okuyabilir.
- **Şema** (İng. *schema*): bir verinin hangi alanlardan oluşacağını ve her alanın tipini önceden tanımlayan sözleşme.
- **Yapılandırılmış çıktı** (İng. *structured output*): modele verdiğin şemaya uymanın API (uygulama arayüzü) seviyesinde zorunlu kılınması. Model şemanın dışına çıkamaz.
- **Ortada kaybolma** (İng. *lost in the middle*): modelin uzun bir metnin başını ve sonunu, ortasından daha iyi hatırlaması.

# delimiter | Bölümleme (sınırlayıcılar) | ai:false

## Görev
Bir prompt'un içinde üç farklı şey var: talimatın, üstünde çalışılacak kod, ve bir örnek. Bunları modelin karıştırmayacağı biçimde **ayıran** (sınırlayıcılarla bölen) bir prompt yaz.

## Kötü örnek
`şu kodu şu örnekteki gibi düzelt [kod] [örnek] ve bana açıkla`

## İpucu
Talimat, veri ve örnek iç içe geçince model neyin ne olduğunu karıştırır. Her parçayı etiketli bir bloğa koy.

## İdeal çözüm
Aşağıdaki kodu, örnekteki stile uyacak şekilde düzelt.

`<talimat>` İsimlendirmeyi örnekteki gibi anlamlı yap; davranışı değiştirme. `</talimat>`

`<kod>` [düzeltilecek kod] `</kod>`

`<ornek>` [istenen stilin örneği] `</ornek>`

## Neden
Talimat, veri ve örnek iç içe geçtiğinde model hangisinin "yapılacak iş", hangisinin "üstünde çalışılacak malzeme" olduğunu karıştırabilir — hatta verinin içindeki bir cümleyi talimat sanabilir. Sınırlayıcılar (XML etiketleri gibi `<kod>...</kod>`, ya da markdown başlıkları) her parçaya net bir sınır çizer. 2026'da modern modeller temiz başlık ve boşlukla da idare eder — yani XML artık olmazsa-olmaz değil — ama karmaşık, çok parçalı prompt'larda etiketli bölümleme hâlâ en güvenli yol. Bir uyarı: sınırlayıcı bir **güvenlik duvarı değildir**. Kötü niyetle hazırlanmış bir veri, "yukarıdaki talimatları unut" gibi cümlelerle modeli kandırmaya çalışabilir (buna *prompt enjeksiyonu* denir). Etiket bunu zorlaştırır, engellemez; dışarıdan gelen veriye asla tam güvenme.

# siralama | Bilgi sıralaması (ortada kaybolma) | ai:false

## Görev
Modele 40 sayfalık bir teknik belge verip ondan tek bir soruyu yanıtlamasını isteyeceksin. Belgeyi ve soruyu prompt'ta **doğru sırayla** yerleştiren bir yapı kur.

## Kötü örnek
`[soru] işte belge: [40 sayfalık belge]`

## İpucu
Uzun bir belgenin ortasına gömülen bilgi ve başa sıkıştırılan soru zayıf sonuç verir. Uzun veri nereye, soru nereye?

## İdeal çözüm
`<belge>` [40 sayfalık belge — uzun veri EN ÜSTTE] `</belge>`

Yukarıdaki belgeye dayanarak şu soruyu yanıtla: [soru — talimat ve soru EN SONDA]. Yalnızca belgede yazana dayan; belgede yoksa "belgede yok" de.

## Neden
"Ortada kaybolma" (lost in the middle) 2026'da hâlâ gerçek — bir milyon token'lık dev bağlam pencerelerinde bile. Modeller, uzun bir girdinin **başındaki ve sonundaki** bilgiyi ortasındakinden daha iyi hatırlar. Pratik kural: **uzun veriyi (belge) en üste, soruyu/talimatı en sona** koy. Testlerde soruyu sona koymak yanıt kalitesini belirgin biçimde artırabiliyor. Kritik bir bilgiyi asla uzun bir metnin tam ortasına gömme. Etkinin büyüklüğü modele ve işe göre değişir: kısa girdilerde fark neredeyse yoktur, çok uzun girdilerde belirgindir. Sıralama bir ilk savunma; asıl çözüm metni kısaltmak ve yalnızca ilgili kısmı vermektir (bkz. 07. bölüm).

# iskele | Çıktı iskeleti ver | ai:false

## Görev
Modelden bir mimari karar kaydı (İng. *ADR* — bir teknik kararı, nedenini ve sonuçlarını kaydeden kısa belge) yazmasını isterken, çıktının **tam olarak hangi başlıklarla** geleceğini bir iskeletle dikte eden bir prompt yaz.

## Kötü örnek
`bu karar için bir ADR yaz`

## İpucu
Serbest bırakırsan model kendi başlıklarını uydurur. İstediğin iskeleti doğrudan ver, model doldursun.

## İdeal çözüm
Aşağıdaki karar için bir ADR yaz. **Tam olarak şu iskeleti doldur, başlıkları değiştirme:**

```
# Başlık
## Durum: (öneri / kabul / reddedildi)
## Bağlam: (hangi problem, neden şimdi)
## Karar: (ne kararlaştırdık)
## Sonuçlar: (artılar ve eksiler)
```

Karar: `[karar]`

## Neden
Çıktı iskeleti (şablon), modele **doldurulacak boşluklar** vererek biçimi tamamen senin kontrolüne alır. Model başlık uydurmaz, sıra şaşırmaz — sadece boşlukları doldurur. Bu, "çıktı formatı belirtme"nin en katı halidir ve tekrar eden belgelerde (ADR, rapor, PR açıklaması) tutarlılığı garantiler. Bonus: çıktı öngörülebilir olduğu için onu bir sonraki adımda program olarak işlemek de kolaylaşır. Bedeli: iskelet, düşünmeyi de kalıba sokar. Konuya uymayan bir başlık varsa model onu **boş bırakmak yerine doldurmaya** çalışır ve gereksiz içerik uydurur. Çare: iskelete "ilgili değilse 'yok' yaz" izni eklemek.

# sema | Yapılandırılmış çıktı (şema) | ai:false

## Görev
Bir listeyi (koddaki güvenlik risklerini) modelden **makine tarafından işlenebilir** biçimde, kesin bir alan yapısıyla istiyorsun. Şema dikte eden bir prompt yaz.

## Kötü örnek
`güvenlik açıklarını bul ve bana anlat`

## İpucu
Çıktıyı bir programa vereceksen, serbest metin işe yaramaz. Her kaydın hangi alanlardan oluşacağını söyle.

## İdeal çözüm
Aşağıdaki kodda en fazla 3 güvenlik riski bul. Çıktıyı **yalnızca şu JSON şemasıyla** ver, başka metin yazma:

```json
[{"risk": "kısa ad", "seviye": "düşük|orta|yüksek", "duzeltme": "tek cümle"}]
```

```
[kod]
```

## Neden
Çıktıyı bir programa besleyeceksen, kesin bir şema (JSON) şart. 2026'da bu artık çoğu sağlayıcıda **yapılandırılmış çıktı** (structured output) olarak sağlanıyor — model şemaya uymaya API seviyesinde zorlanıyor, yani biçim hatası ihtimali ortadan kalkıyor. Bu, sadece prompt'ta "JSON ver" demekten güvenilirdir; imkânın varsa onu kullan.

Ama bir uyarı var: sırf JSON'a zorlamak bazen **muhakeme kalitesini düşürebilir** ("format vergisi"). Model hem düşünüp hem katı biçime uymaya çalışınca ikisi de zarar görebilir. Ayrıca şema, alanların **doğru doldurulduğunu** garanti etmez — sadece alanların var olduğunu. `"seviye": "yüksek"` biçimsel olarak geçerlidir, doğru olduğu anlamına gelmez. Çözüm bir sonraki alıştırmada: önce düşündür, sonra formatla.

# iki-asama | Önce düşün, sonra formatla | ai:false

## Görev
Karmaşık bir analiz isteyip sonucu katı JSON istemek "format vergisi"ne yol açıyor. Bunu **iki aşamaya** bölen bir yaklaşım yaz.

## Kötü örnek
`bu sistemin performans darboğazlarını derinlemesine analiz et ve sonucu şu 8 alanlı JSON şemasında ver`

## İpucu
Modeli aynı anda hem derin düşünmeye hem katı biçime uymaya zorlarsan ikisi de zayıflar. İki işi ayır.

## İdeal çözüm
(İki adımda:)
**1. adım:** Bu sistemin performans darboğazlarını serbest biçimde, derinlemesine analiz et — biçim kaygısı yok, sadece iyi düşün.
**2. adım (analiz gelince):** Şimdi yukarıdaki analizini şu JSON şemasına dök: `[{"darbogaz": "...", "etki": "...", "cozum": "..."}]`. Yeni bilgi ekleme, sadece formatla.

## Neden
"Format vergisi" (format tax) 2026'da bilinen bir tuzak: modeli aynı anda hem zor bir muhakeme yapmaya hem de katı bir çıktı biçimine uymaya zorlarsan, dikkatini böler ve genelde **ikisi de zayıflar** — özellikle büyük/karmaşık şemalarda. Çözüm işi ikiye ayırmaktır: **önce serbest muhakeme** (biçim kaygısı yok), **sonra formatlama** (yeni düşünme yok, sadece dönüştürme). Böylece her adım tek işe odaklanır. Ne zaman gereksiz: şema küçükse (2-3 alan) ya da iş zaten kolaysa, iki aşama sadece iki kat maliyet ve gecikme demektir. Düşünen modellerde de etki azalır, çünkü muhakeme zaten biçimlendirmeden ayrı bir adımda yapılır. Kuralı basit tut: **zor iş + karmaşık şema** olduğunda böl.

# yasak-degil-yap | "Yapma" yerine "yap" de | ai:false

## Görev
Modelin çıktısındaki istemediğin bir davranışı **yasaklayarak** değil, istediğini **olumlu tarif ederek** düzelten bir prompt yaz.

## Kötü örnek
`cevabında markdown kullanma, madde işareti koyma, başlık atma`

## İpucu
Modele ne YAPMAYACAĞINI saymak, ne YAPACAĞINI söylemekten zayıftır. İstediğin biçimi olumlu tarif et.

## İdeal çözüm
Cevabını **akıcı düz paragraflar** halinde yaz; her fikir bir cümleyle bir öncekine bağlansın, birbiri ardına gelen kısa bölümler halinde ilerlesin.

## Neden
Modele bir sürü "yapma" saymak, ona istediğin **olumlu hedefi** vermez — sadece bir mayın tarlası çizer, ve model o tarlanın dışında ne yapacağını yine tahmin eder. Biçim kontrolünde en etkili yol olumlu tariftir: "markdown kullanma" yerine "akıcı paragraflarla yaz". Olumlu tarif modele gidilecek yönü gösterir; yasak listesi sadece gidilmeyecek yönleri.

Sınırı da bil: bu "olumlu her zaman üstündür" demek değildir. **Kapsam ve güvenlik** sınırlarında negatif kısıt vazgeçilmezdir — "başka dosyaya dokunma", "yeni bağımlılık ekleme" ifadelerinin olumlu karşılığı yoktur. Kural: *biçim* için olumlu tarif, *kapsam* için açık yasak.

# tam-iskelet | Katmanlı tam prompt (kanonik sıra) | ai:false

## Görev
Karmaşık bir görev için **tam bir prompt** kur: rol, ton, veri, kurallar, örnek ve asıl istek —
hepsi var, ama hangi parça **nereye** konacak? Kanonik (yerleşik/standart) sırayı uygulayan bir prompt yaz.

## Kötü örnek
`şu raporu özetle bu arada sen bir analistsin işte veri [veri] kısa olsun ha bir de şu kurallara uy [kurallar] örnek de vereyim [örnek]` (her şey tek karışık paragrafta, istek en başta)

## İpucu
Model prompt'u baştan sona okur; parçaların YERİ sonucu etkiler. Klasik şablon sıralıdır:
kimlik → ton → veri → kurallar → örnek → **asıl istek** → çıktı formatı. Asıl istek nereye?

## İdeal çözüm
(Katmanlı kurulum — her katman kendi bölümünde:)
**1.** Sen kıdemli bir finans analistisin. **2.** Ton: yönetici özeti — kısa, yorumlu, jargonsuz.
**3.** Arka plan verisi: `[çeyrek raporu]` **4.** Kurallar: yalnız verideki rakamları kullan; tahmin
yürütüyorsan açıkça belirt. **5.** Örnek çıktı: `[geçen çeyreğin özeti]` **6.** **Asıl istek: bu
çeyreği aynı yapıda özetle.** **7.** Format: 5 madde + tek cümlelik sonuç.

## Neden
Katmanların her birini ayrı alıştırmalarda öğrendin (rol, bölümleme, sıralama, iskelet, few-shot) —
bu alıştırma **tarifi** verir: malzeme bilmek ile yemeği kurmak farklı becerilerdir. Kritik ilke,
**asıl isteğin en sona konması**: model tüm kimliği, veriyi ve kuralları kurduktan sonra göreve
odaklanır — "ortada kaybolma"nın (bkz. `siralama`) tersinden kullanımı: en önemli şey en güçlü
pozisyonda.

Bu sıra bir yasa değil, iyi bir varsayılan; kısa bir istekte yedi katmanı kurmak gereksiz tören olur.
2026 notu: klasik şablonlarda bir katman daha görürsün — "ön doldurulmuş yanıt (İng. *prefill*)",
yani modelin cevabının ilk kelimelerini senin yazman. **Bilerek dışarıda**: modern Claude
modellerinde (4.6 ve sonrası) bu özellik kaldırıldı; çıktıyı yönlendirmenin güncel yolu format +
iskelet + şemadır (bkz. `iskele`, `sema`). Eskimiş tekniği ezberleme — eskime nedenini bil.

# yapilandirma-birlestir | Yapılandırmayı birleştir | ai:true

## Görev
Öğrendiğin yapılandırma tekniklerini birleştir. Senaryo: modele uzun bir hata günlüğü (İng. *log* — programın çalışırken yazdığı olay kaydı) verip, içinden en kritik 3 hatayı yapılandırılmış biçimde çıkarttırmak istiyorsun. En iyi prompt'u yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
Uzun veriyi (log) nereye koyacaksın, soruyu nereye? Parçaları nasıl böleceksin? Çıktı şemasını nasıl dikte edeceksin?

## İdeal çözüm
`<log>` [uzun hata günlüğü — EN ÜSTTE, sınırlayıcı içinde] `</log>`

Yukarıdaki günlükte en kritik 3 hatayı bul (talimat ve şema EN SONDA). Kritiklik ölçütü: sistemi durduran > veri kaybettiren > yavaşlatan. Çıktıyı yalnızca şu JSON ile ver, başka metin yazma: `[{"hata": "...", "kritiklik": "yüksek|orta|düşük", "olası_neden": "..."}]`

## Neden
İyi yapılandırma birkaç tekniği birlikte kullanır: uzun veriyi **başa** koy (ortada kaybolma), talimat ve şemayı **sona** koy, parçaları **sınırlayıcılarla** ayır, çıktıyı **şema** ile dikte et. Böylece hem model doğru bilgiyi bulur, hem çıktı doğrudan bir programa beslenebilir hale gelir. Son bir şerh: yapılandırma çıktının **biçimini** garanti eder, **doğruluğunu** değil. Kusursuz JSON içinde yanlış bir "olası_neden" gelebilir; şema, gözle ya da testle doğrulamanın yerini tutmaz.
