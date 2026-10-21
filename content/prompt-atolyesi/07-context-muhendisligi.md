---
slug: context-muhendisligi
baslik: Bağlam Mühendisliği
aciklama: Prompt'un ötesi. 2026'nın kayması: iyi tek bir cümle yazmak değil, modelin her çağrıda GÖRDÜĞÜ her şeyi — sistem talimatı, geçmiş, getirilen belge, araç tanımı, bellek — kasıtlı tasarlamak. Ajanlar prompt hatasından değil, bağlam/durum hatasından çöker.
---

### Bu bölümde ne öğreneceksin?

Şimdiye kadar **ne yazdığını** konuştuk. Bu bölümde **modelin ne gördüğünü** konuşacağız: sistem talimatı, konuşma geçmişi, getirilen belgeler, araç tanımları — hepsi birlikte. Bunları bilinçli seçmeye "bağlam mühendisliği" denir ve uzun süren ajan oturumlarında kaliteyi belirleyen asıl şey budur.

**Bu bölümün yeni terimleri:**

- **Bağlam penceresi** (İng. *context window*) — hatırlatma: modelin tek seferde görebildiği toplam token miktarı. 2026'da bir milyon token'a kadar çıkabiliyor; ama "sığıyor" ile "iyi kullanılıyor" aynı şey değil.
- **Token bütçesi**: bir çağrıda bağlam penceresine neyi koyup neyi koymayacağının bilinçli kararı. Her token para, gecikme ve dikkat demek.
- **Sistem talimatı** (İng. *system prompt*): her çağrıda en başta duran, modelin rolünü ve sınırlarını belirleyen sabit metin.
- **RAG** (İng. *retrieval-augmented generation*, getirmeli üretim): soruya cevap vermeden önce bir arama yapıp bulunan belge parçalarını prompt'a koyma yöntemi. Model böylece kendi bilmediği, sana ait bilgiyle konuşabilir.
- **Sıkıştırma** (İng. *compaction*): uzun konuşma geçmişini bir özete indirip ayrıntıyı atmak. Geçmişin **özü kalır**.
- **Sıfırlama** (İng. *reset*): oturumu tümüyle kapatıp temiz bir bağlamla yeniden başlamak. Geçmişten **hiçbir şey kalmaz**, taşımak istediğini elinle taşırsın.
- **Bağlam zehirlenmesi**: yanlış ya da alakasız bilginin bağlama girip modelin sonraki tüm cevaplarını bozması.

# baglam-butcesi | Bağlam bütçesi | ai:false

## Görev
Bir ajana uzun bir görev verirken, bağlam penceresine (modelin tek seferde görebildiği metnin sınırı) neyi koyup neyi koymayacağına **bilinçli karar veren** bir yaklaşım yaz. Senaryo: 40 dosyalık bir kod tabanında bir hatayı düzelttireceksin.

## Kötü örnek
`işte tüm proje [40 dosyanın tamamını yapıştır], şu hatayı düzelt`

## İpucu
Bağlam penceresi sonsuz değil ve doldurmak bedava değil — hem maliyet hem "ortada kaybolma" artar. Modele HER ŞEYİ değil, işe YARAYANI ver.

## İdeal çözüm
Şu hatayı düzelteceğiz: `[hata]`. Önce **hangi dosyalara ihtiyacın olduğunu söyle** — ben sadece onları vereceğim. Tahmininle başla: hata mesajındaki sınıf + onun doğrudan bağımlılıkları. Gereksiz dosyayı isteme; bağlamı dar ve ilgili tut.

## Neden
Bağlam penceresine ne koyduğun bir **bütçe** kararıdır: her token maliyet demek, ve alakasız bilgi modeli hem yavaşlatır hem "ortada kaybolma" (uzun metnin ortasındaki bilginin gözden kaçması) ile doğru bilgiyi gölgeler. 2026'nın dersi: "daha çok bağlam = daha iyi" yanlış. İyi bağlam mühendisliği, modele **tam gerektiği kadar** ilgili bilgiyi verir — ne eksik (halüsinasyon), ne fazla (gürültü + maliyet). Modele "neye ihtiyacın var?" diye sormak, kör yapıştırmadan iyidir.

Şerh: bu yöntem bir tahmine dayanır ve tahmin yanlış olabilir. Model isteyeceği dosyayı bilmiyorsa dar bağlam, eksik bağlam olur ve hata kök nedeni gözden kaçar. Bu yüzden ajanlara arama araçları verilir: dosyayı önceden seçmek yerine, ajan gerektikçe kendi çeker.

# bellek | Konuşma belleği ve durum | ai:false

## Görev
Çok adımlı bir ajan oturumunda, modelin **hangi geçmişi hatırlaması** gerektiğini yöneten bir yaklaşım yaz. Senaryo: 20 mesajlık bir hata ayıklama oturumundasın ve bağlam şişti.

## Kötü örnek
(Tüm 20 mesajı her çağrıda olduğu gibi taşımak — ta ki pencere taşana kadar)

## İpucu
Ajanlar sohbet botlarından farklı çöker: sorun genelde kötü prompt değil, **durum yönetimi**. Neyi hatırlamalı, neyi özetleyip atmalı?

## İdeal çözüm
Bu hata ayıklama oturumunu sürdürüyoruz. Devam etmeden önce **şu ana kadarki bulguları tek bir özet nota sıkıştır** (denenen şeyler, elenen sebepler, şu anki hipotez) — sonra ayrıntılı geçmişi bırakıp bu özetle ilerleyeceğiz. Özeti ver.

## Neden
Ajan mimarisinde en yaygın hata **durum (İng. *state*) yönetimi** hatasıdır — prompt hatası değil. Uzun bir oturumda tüm geçmişi taşımak pencereyi taşırır, maliyeti şişirir ve modeli eski/ilgisiz ayrıntıya boğar. Çözüm bir bellek stratejisidir: önemli olanı **özetleyip sakla** (denenen, elenen, mevcut hipotez), gerisini at. Buna **sıkıştırma** (compaction) denir; 2026'da birçok araç bunu otomatik yapıyor.

İki yolu ayırt et:
- **Sıkıştırma (compaction):** geçmişi özete indirir, iş kaldığı yerden sürer. Avantajı süreklilik; riski, özetin **yanlış şeyi** saklaması — atılan ayrıntı bir daha geri gelmez ve model kaybettiğini bilmez.
- **Sıfırlama (reset):** oturumu kapatıp temiz başlarsın; taşımak istediğini sen yazarsın. Avantajı en temiz bağlam; bedeli, taşımayı unuttuğun bilginin kaybolması.

Kural: iş aynı işse ve süreklilik gerekiyorsa sıkıştır; konu değişiyorsa ya da oturum yanlış yola sapmışsa **sıfırla**. Kirlenmiş bir bağlamı sıkıştırmak, kiri özete taşımaktan başka işe yaramaz.

# retrieval-orkestrasyon | Neyi getir, neyi at | ai:false

## Görev
Bir RAG (getirmeli üretim — soruya cevap vermeden önce arama yapıp bulunan parçaları prompt'a koyma) uygulamasında modele belge getirirken, **getirilen parçaların kalitesini** kontrol eden bir yaklaşım yaz. Senaryo: arama 10 belge parçası döndürdü, hepsi eşit alakalı değil.

## Kötü örnek
`işte arama sonucu 10 parça [hepsini yapıştır], soruyu yanıtla`

## İpucu
Getirilen her şey ilgili değildir. Alakasız parça modeli yanlış yöne çeker (bağlam zehirlenmesi). Getir ≠ kullan.

## İdeal çözüm
Aşağıda bir arama 10 belge parçası döndürdü. **Önce her parçanın soruyla gerçekten alakalı olup olmadığını değerlendir** (evet/hayır + tek cümle gerekçe); yalnızca alakalı olanları kullanarak yanıtla. Alakasızları görmezden gel. Hiçbiri alakalı değilse "belgelerde yok" de.

Soru: `[soru]`
Parçalar: `[10 parça]`

## Neden
Getirme (İng. *retrieval*) körlemesine güvenilecek bir kaynak değildir: arama alakasız ya da yanıltıcı parçalar döndürebilir ve bunları prompt'a gömmek **bağlam zehirlenmesine** yol açar — model yanlış bilgiye dayanır ve emin görünür. İyi bağlam mühendisliği, getirmeyi bir **orkestrasyon** olarak görür: getir, **filtrele / yeniden sırala** (İng. *rerank* — bulunan parçaları alaka düzeyine göre yeniden dizmek), yalnızca gerçekten yararlı olanı modele ver. "Getirdim" ile "kullanmalıyım" aynı şey değildir; aradaki eleme adımı kaliteyi belirler.

Ödünleşme: eleme adımı da bir model çağrısıdır — maliyet ve gecikme ekler, ve kendisi de yanılıp doğru parçayı eleyebilir. Az sayıda, kaliteli sonuç dönen bir aramada eleme gereksizdir; asıl kazanç, aramanın gürültülü olduğu durumlardadır.

# arac-tanimi | Araç tanımı tasarımı | ai:false

## Görev
Bir ajana bir araç (İng. *tool/function* — modelin çağırabildiği dış işlev) verirken, ajanın onu **doğru zamanda, doğru şekilde** çağırması için o aracın tanımını (açıklamasını) iyi yazan bir örnek yaz.

## Kötü örnek
`Araç: veritabani. Açıklama: veritabanı işlemleri yapar.`

## İpucu
Ajan aracı, senin verdiğin AÇIKLAMAYA bakarak seçer ve çağırır. Muğlak açıklama = yanlış zamanda/yanlış parametreyle çağrı. Açıklama ajanın "kullanım kılavuzu"dur.

## İdeal çözüm
```
Araç: musteri_ara
Açıklama: Bir müşteriyi e-posta adresine göre bulur. YALNIZCA tam e-posta
adresi elindeyse kullan; isim/telefonla arama yapmaz. Bulunamazsa boş döner
(hata değil). Parametre: eposta (zorunlu, geçerli e-posta formatı).
```

## Neden
Ajansal sistemlerde araç tanımı (İng. *tool description*) aslında bir **prompt'tur** — ajan hangi aracı, ne zaman, hangi parametreyle çağıracağını bu açıklamadan çıkarır. "Veritabanı işlemleri yapar" gibi muğlak bir açıklama, ajanı yanlış araç seçimine, eksik parametreye ve hata döngülerine sokar. İyi araç tanımı üç şeyi net söyler: **ne yapar**, **ne zaman kullanılmalı (ve kullanılmamalı)**, **parametreler ve sınır durumları**. Bağlam mühendisliğinin en gözden kaçan ama en etkili parçasıdır.

Bir de sayı meselesi var: her araç tanımı bağlamda yer kaplar ve seçenekleri çoğaltır. Otuz aracı olan bir ajan, beş aracı olandan daha kararsızdır. Araç setini göreve göre dar tut; gerekmeyeni hiç verme.

# gurultu-at | Gürültüyü at | ai:false

## Görev
Bir prompt'ta modele verdiğin bağlamdan **işe yaramayan kısmı temizleyen** bir örnek yaz. Senaryo: bir hata için ilgili kodu veriyorsun ama araya alakasız yorumlar, ölü kod, uzun log'lar karışmış.

## Kötü örnek
`[500 satırlık dosya — 480 satırı hatayla ilgisiz]` şu hatayı düzelt

## İpucu
İlgisiz bağlam sadece maliyet değil; modeli yanlış yöne çeker. İşe yarayan sinyali gürültüden ayır.

## İdeal çözüm
Şu hata `[hata]` bu fonksiyonda oluşuyor. İlgili **yalnızca şu bölümü** veriyorum (hatalı fonksiyon + çağırdığı iki yardımcı); dosyanın gerisi alakasız olduğu için çıkardım. Bu bağlamla kök nedeni bul.

```
[sadece ilgili ~20 satır]
```

## Neden
Modele verdiğin her şey onun dikkatini paylaşır — alakasız 480 satır, ilgili 20 satırı gölgeler ve modeli yan yollara çeker (ve token yakar). Bağlam mühendisliğinin karşı-sezgisel dersi: bazen **daha az bağlam daha iyi sonuç** verir. Sinyali gürültüden ayırıp yalnızca ilgili parçayı vermek, hem doğruluğu hem hızı artırır. "Her ihtimale karşı hepsini vereyim" refleksi, modeli boğar.

Ama eleme **senin** yargına dayanır ve yanılabilirsin: hatanın gerçek nedeni, alakasız sandığın 480 satırın içindeyse dar bağlam seni yanlış cevaba götürür ve bunu fark etmezsin. Bu yüzden kısıtlı bağlamda ilerlerken modele "bu bağlam yetmiyorsa hangi bilgiyi istediğini söyle" iznini vermek iyi bir alışkanlıktır.

# baglam-hijyeni | Bağlam hijyeni | ai:false

## Görev
Uzun bir ajan oturumunda, konu değiştiğinde ya da bir alt-görev bittiğinde bağlamı **temiz tutan** bir yaklaşım yaz.

## Kötü örnek
(Bir görevi bitirip, aynı şişmiş oturumda hiç temizlemeden yeni ve alakasız bir göreve geçmek)

## İpucu
Biten bir işin ayrıntıları, yeni işe taşındığında gürültüdür ve modeli önceki bağlamla karıştırır. Yeni göreve temiz başla.

## İdeal çözüm
İlk görevi bitirdik. **Yeni ve ayrı bir göreve geçiyoruz; önceki görevin ayrıntılarını unut.** Sadece taşınması gereken tek şey şu sonuç: `[önceki görevin tek satırlık çıktısı]`. Yeni görev: `[yeni görev]`. Temiz başla.

## Neden
"Bağlam hijyeni", bir oturum boyunca bağlamı **kasıtlı temiz tutmaktır**. Biten bir alt-görevin tüm ayrıntısını yeni bir göreve taşımak, modeli iki iş arasında karıştırır (önceki görevin varsayımlarını yenisine sızdırır) ve pencereyi gereksiz doldurur. Disiplin: bir iş bitince yalnızca **taşınması gereken sonucu** al, gerisini bırak. Bu, uzun ajan oturumlarında kalitenin zamanla düşmesini (İng. *drift* — yavaş yavaş konudan sapma) önler.

Önemli ayrıntı: prompt içinde "önceki görevi unut" demek, geçmişi **gerçekten silmez** — metin hâlâ bağlamdadır, model yalnızca onu görmezden gelmeye yönlendirilir. Gerçek temizlik için oturumu sıfırlaman (yeni oturum açman) gerekir. Konu tamamen değişiyorsa sıfırlama, unutma talimatından her zaman daha güvenilirdir.

# context-birlestir | Bağlam mühendisliğini birleştir | ai:true

## Görev
Öğrendiğin bağlam mühendisliği tekniklerini birleştir. Senaryo: bir RAG-tabanlı destek ajanı kuruyorsun; kullanıcı sorusu geliyor, sen belge getiriyorsun, ajanın bir de araç erişimi var. Modele verilecek bağlamı baştan sona tasarlayan bir yaklaşım yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
Bağlam bütçesini düşün (ne kadar), getirmeyi orkestrayla (filtrele/rerank), araç tanımını net yaz, gürültüyü at, ve belleği/durumu yönet. Hepsini senaryoya göre dengele — tıkıştırma.

## İdeal çözüm
Destek ajanının her çağrısında bağlamı şöyle kur: **(1) Sistem talimatı** — rol, sınırlar, "yalnızca getirilen belgeye dayan, uydurma". **(2) Getirme** — soruyla en alakalı 3 parçayı ver (10 getir, ilgisizi ele, en iyi 3'ü sırala); alakalı yoksa "belgede yok" de. **(3) Araç tanımları** — yalnızca bu görevde gereken araçlar, her biri ne zaman kullanılacağıyla. **(4) Bellek** — önceki turların tek satırlık özeti (tam geçmiş değil). **(5) Kullanıcı sorusu** — en sonda. Alakasız hiçbir şey ekleme.

## Neden
Bağlam mühendisliği, prompt yazmanın üstündeki katmandır: modelin bir çağrıda **gördüğü her şeyin** — sistem talimatı, getirilen belge, araç tanımı, bellek, kullanıcı girdisi — kasıtlı tasarımı. 2026'da ajanların çoğu kötü prompt'tan değil, **kötü bağlamdan** (şişmiş, gürültülü, yanlış durumla) çöker. İyi bağlam mühendisi her parçayı bilinçli seçer: tam gerektiği kadar, doğru sırada, temiz. Prompt teknikleri bu katmanın içindeki tek bir öğedir.

Son bir uyarı: bu tasarım tek seferlik bir iş değil. Kullanıcı soruları, belgeler ve araçlar değiştikçe bağlam kurgusu eskir. Bağlam mühendisliği, ölçüp düzeltmeye devam ettiğin sürece işe yarar — bir kez kurup unutulacak bir şablon değil.
