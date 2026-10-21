---
slug: muhakeme
baslik: Muhakeme
aciklama: Modeli daha iyi düşünmeye yönlendiren teknikler — ve 2026'nın en önemli dersi: düşünen modellerde bu tekniklerin çoğu artık otomatik, bazıları zararlı.
---

### Bu bölümde ne öğreneceksin?

Bu bölüm, modele bir işi nasıl "düşündüreceğinle" ilgili. Adım adım ilerletme, önce plan isteme, işi bölme ve modele kendi çıktısını denetletme tekniklerini öğreneceksin. En önemli ders şu: **"düşün" demek sihirli bir kelime değil** — bazı modellerde işe yarar, bazılarında gereksizdir, bazılarında zarar verir.

**Bu bölümün yeni terimleri:**

- **Muhakeme** (İng. *reasoning*): bir sonuca adım adım, gerekçeyle varmak. Tek atışta cevap uydurmanın karşıtı.
- **Zincirleme düşünce** (İng. *chain-of-thought*): modelden cevabı vermeden önce ara adımları yazmasını istemek. Adımlar görünür olduğu için model kendi önceki adımını okuyarak ilerler.
- **Düşünen model** (İng. *reasoning / thinking model*): cevabı üretmeden önce kendi içinde, sana göstermeden bir muhakeme yürüten model. Bu özelliğin ayarı ürüne göre "düşünme", "thinking" ya da "reasoning" diye geçer ve açılıp kapatılabilir. Ürün ve sürüm adları çok hızlı değişir; ezberleme — **açık mı kapalı mı** olduğunu bil, yeter.
- **Öz-tutarlılık** (İng. *self-consistency*): aynı soruyu birkaç kez sorup en çok tekrar eden cevabı doğru saymak.
- **Öz-denetim** (İng. *self-critique*): modelin kendi ürettiği cevabı, verdiğin kritere göre gözden geçirip düzeltmesi.

# adim-adim | Adım adım düşündürme (klasik) | ai:false

## Görev
Modelden çok adımlı bir hesabı (bir leasing sözleşmesinin toplam maliyetini) yapmasını istiyorsun. **Düşünme özelliği KAPALI**, sıradan bir modelle çalışıyorsun. Modeli, sonucu doğrudan söylemeden önce **ara adımları göstermeye** yönlendiren bir prompt yaz.

## Kötü örnek
`bu sözleşmenin toplam maliyeti ne?`

## İpucu
Sonucu doğrudan isteyince model tek atışta bir sayı uydurabilir. Önce muhakemeyi, sonra cevabı istersen doğruluk artar.

## İdeal çözüm
Aşağıdaki leasing sözleşmesinin 3 yıllık toplam maliyetini hesapla. Önce **adım adım** göster: aylık taksit × ay sayısı, peşinat, sonda kalan değer. Her adımı tek satırda yaz, en sonda toplamı belirt.

Sözleşme: `[fiyat, vade, faiz, peşinat]`

## Neden
Düşünmesi kapalı bir modelde, ara adımları görünür kılmak (zincirleme düşünce — chain-of-thought) doğruluğu artırır: model "sonraki adım" için önceki adımı okur, tek atışta uydurmaz.

Aynı ailenin ikinci tekniği **öz-tutarlılık** (self-consistency): aynı soruyu birkaç kez sorup en çok tekrar eden cevabı almak. Modelin olasılıksal olması sayesinde çalışır — yanıtlar biraz farklılaşır, doğru cevap genelde en sık tekrar edendir. Bedeli ağırdır: sorduğun kadar kat maliyet ve süre. Ve **yalnızca tek doğru cevabı olan işlerde** anlamlıdır (bir sayı, bir sınıflandırma); serbest metinde "en sık cevap" diye bir şey yoktur. 2026'da düşünen modeller bu tekniği çoğu durumda gereksiz kılıyor.

**AMA 2026'nın kritik uyarısı bir sonraki alıştırmada:** zincirleme düşünce, kendi içinde düşünen modellerde gereksiz — hatta zararlı — hale gelir.

# dusunmeyi-birak | Düşünen modele "adım adım" DEME | ai:false

## Görev
Bu sefer **düşünme özelliği AÇIK** bir modelle (üst seviye Claude, GPT ve Gemini modellerinin güncel sürümleri böyle çalışır) aynı karmaşık kararı çözüyorsun. Az önceki "adım adım düşün" alışkanlığını **bırakıp** bu modele uygun bir prompt yaz.

## Kötü örnek
`adım adım düşün ve muhakemeni tek tek açıkla: iki mikroservis arasındaki iletişim REST mi gRPC mi olmalı?`

## İpucu
Düşünen model zaten kendi içinde muhakeme yapıyor. Ona "nasıl düşüneceğini" dikte etme; **ne istediğini ve hangi kısıtlarla** söyle.

## İdeal çözüm
İki yüksek-hacimli iç mikroservis arasındaki iletişim için REST mi gRPC mi seçmeliyim? Kararı **performans, sözleşme netliği ve ekip aşinalığı** ödünleşmeleriyle değerlendir ve net bir öneriyle bitir. İyice düşün.

## Neden
2026'nın en önemli değişimi bu: düşünen modeller muhakemeyi **kendi içinde** yapar; en yeni üst seviye modellerde bu özellik varsayılan olarak açık gelir. Onlara "adım adım düşün" ya da "muhakemeni açıkla" demek **gereksizdir ve bazen performansı düşürür** — modelin kendi ürettiği plan, çoğu zaman senin önceden yazacağından iyidir. Elle zincirleme düşünce artık sadece **düşünme kapalıyken** bir yedek. Düşünen modelde işin, düşünme adımlarını yazmak değil; **hedefi, kısıtları ve çıktı biçimini** net vermek.

Şerh: "hiç düşün deme" de mutlak bir kural değil. Model işi hafife alıp yüzeysel geçiyorsa, işin zorluğunu belirtmek ("bu çok adımlı bir karar, aceleyle cevaplama") hâlâ işe yarar. Fark şu: modele **ne kadar derine ineceğini** söylüyorsun, **hangi adımları izleyeceğini** değil.

# once-planla | Önce planla, sonra üret | ai:false

## Görev
Modelden bir modülün testlerini yazmasını isteyeceksin ama doğrudan koda atlamasını istemiyorsun. Önce bir **plan**, sonra uygulama isteyen bir prompt yaz. (Birim testi = tek bir fonksiyonun beklendiği gibi çalıştığını otomatik doğrulayan küçük kod parçası.)

## Kötü örnek
`bu servis için testleri yaz`

## İpucu
Model doğrudan yazmaya başlarsa kapsamı dağılır. Önce neyi test edeceğini listeletip onaylatmak, sonra yazdırmak daha iyi sonuç verir.

## İdeal çözüm
Aşağıdaki servis için birim testleri yazacağız. **Önce test etmeyi planla:** hangi senaryoları (mutlu yol, sınır, hata) test edeceğini madde madde listele — kod yazma. Planı görünce "devam" dersem testleri yazarsın.

```
[servis]
```

## Neden
"Önce planla, sonra üret" muhakemeyi ikiye böler: model önce **neyi** yapacağını netleştirir, sonra **nasıl** yapacağına geçer. Bu, hem kapsamı kontrol etmeni (planı görüp düzeltebilirsin) hem de sonucun tutarlılığını artırır. Düşünen modellerde bile değerlidir çünkü ara çıktıyı (planı) **senin denetimine** açar — modelin gizli düşüncesini değil, üzerinde anlaşılan bir planı görürsün. Ne zaman gereksiz: tek adımlık, net bir işte plan istemek sadece bir tur gecikme ve fazladan token demektir. Planı, **yanlış giderse pahalıya patlayacak** işler için sakla.

# bol | Büyük problemi böl | ai:false

## Görev
Modele "tüm mimariyi yeniden tasarla" gibi devasa bir iş yerine, o işin **ilk denetlenebilir dilimini** isteyen bir prompt yaz.

## Kötü örnek
`bu monolitik uygulamayı mikroservislere böl ve yeni mimariyi tasarla`

## İpucu
Kocaman bir istek, kocaman ve denetlenemez bir cevap üretir. İşi anlamlı bir ilk parçaya indir.

## İdeal çözüm
Bu monolitik uygulamayı (tek parça halinde çalışan uygulama) ileride mikroservislere böleceğiz. **İlk adım olarak** yalnızca şunu yap: kod tabanındaki bağımsız iş alanlarını (alt-domain) belirle ve her birini tek cümleyle tanımla. Bölme önerisi yapma — önce sınırları netleştirelim.

## Neden
Büyük bir problemi tek prompt'a sığdırmak, modeli yüzeysel ve denetlenemez bir cevaba iter. Bölmek iki şey kazandırır: her dilimin çıktısını **görüp doğrulayabilirsin**, ve bir sonraki dilim önceki onaylanan sonucun üstüne oturur. Bu, "önce planla"nın kardeşidir: muhakemeyi tek dev adım yerine, denetlenebilir küçük adımlara yayarsın. Aşırıya kaçma: her şeyi ufacık parçalara bölmek de bağlamı dağıtır — model bütünü göremez ve parçalar birbirine uymaz. Dilim, **tek başına anlamlı ve doğrulanabilir** olacak kadar büyük olmalı.

# kendini-denetle | Kendini denetlet (self-critique) | ai:false

## Görev
Modelden bir cevap alıp, o cevabı **kendisine gözden geçirtmek** istiyorsun. Bir taslak üretip sonra onu belirli bir kritere göre denetleten bir prompt yaz.

## Kötü örnek
`bu fonksiyonu yaz` (ve olduğu gibi kabul et)

## İpucu
Model ilk denemesinde hata yapabilir. Ona "bitirmeden önce şunu kontrol et" demek, çıktının kalitesini artırır.

## İdeal çözüm
Aşağıdaki gereksinim için bir fonksiyon yaz. **Bitirmeden önce kendi çıktını şu kritere göre denetle:** (1) boş/null girdiyi ele alıyor mu, (2) fonksiyon adı ne yaptığını söylüyor mu, (3) bir kenar durumu (İng. *edge case* — nadir ama mümkün girdi) kaçırdın mı? Denetimde sorun bulursan düzelt, sonra son hali ver.

Gereksinim: `[gereksinim]`

## Neden
Kendini denetletme (öz-denetim / self-critique), modelin ilk taslağını bir kalite süzgecinden geçirir ve çoğu işte ölçülebilir kazanç sağlar. **Ama 2026 uyarısı:** doğrulaması zor ya da kritik alanlarda model, hatasını düzeltmek yerine **rasyonalize edebilir** — yanlış cevabı savunur. Bu yüzden denetimi **somut, kontrol edilebilir kriterlere** bağla ("boş girdiyi ele alıyor mu"), muğlak "iyi mi" sorusuna değil.

İkinci şerh: en yeni modellerin bir kısmı kendi çıktısını **sen söylemeden zaten** denetliyor. Böyle bir modelde "kendini kontrol et" demek fazladan tur ve token demek olabilir; önce denetim talimatı olmadan dene, sonuç kötüyse ekle. Kritik yerlerde ise modelin öz-denetimine hiç güvenme: harici doğrulama (test çalıştırmak, gerçek veriyle denemek) tek güvenilir yoldur.

# rubrik | Rubrik / başarı kriteri ver | ai:false

## Görev
Modelden bir metin (bir API'nin dokümantasyonu) üretmesini isterken, "iyi"nin ne demek olduğunu **ölçülebilir bir rubrikle** tanımlayan bir prompt yaz. (Rubrik = "iyi çıktı"nın hangi maddeleri karşılaması gerektiğini sayan kontrol listesi.)

## Kötü örnek
`bu endpoint için iyi bir dokümantasyon yaz`

## İpucu
"İyi" model için belirsiz. İyi çıktının hangi kutucukları işaretlemesi gerektiğini sen listele.

## İdeal çözüm
Aşağıdaki REST endpoint'i (bir API'nin tek bir adresi/işlevi) için dokümantasyon yaz. İyi bir dokümantasyon **şu kutucukları işaretlemeli:** (1) ne işe yaradığı tek cümlede, (2) her parametre: ad + tip + zorunlu mu, (3) örnek istek, (4) örnek başarılı yanıt, (5) olası hata kodları. Her maddeyi kapsadığından emin ol.

```
[endpoint]
```

## Neden
Rubrik (başarı kriteri), "iyi"yi modelin tahminine bırakmak yerine **ölçülebilir bir kontrol listesine** çevirir. Model artık senin kafandaki "iyi"yi kestirmeye çalışmaz; kutucukları işaretler. Bu hem çıktıyı öngörülebilir kılar hem de sen çıktıyı **aynı rubrikle denetleyebilirsin**. Rubrik vermek, kendini denetletmenin de yakıtıdır: modele "şu rubriğe göre kendini kontrol et" diyebilirsin. Zayıf yanı: rubrik, yalnızca **listelediğin** şeyi garanti eder. Listede olmayan kalite (akıcılık, uygun ton, doğru öncelik) kendiliğinden gelmez — rubrik bir taban çizer, tavan değil.

# muhakeme-birlestir | Muhakemeyi birleştir | ai:true

## Görev
Öğrendiğin muhakeme tekniklerini birleştir. Senaryo: **düşünme özelliği açık** bir modele, dağınık bir eski fonksiyonu güvenli biçimde yeniden düzenletmek (refactor) istiyorsun — ama denetimi elinde tutarak. En iyi prompt'u yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
Düşünen modelde "adım adım düşün" deme. Onun yerine: önce plan iste (onaylayacaksın), net kısıt koy (davranış değişmesin), ve bitişte kendi kendini somut kriterle denetletmesini iste.

## İdeal çözüm
Aşağıdaki fonksiyonu okunabilirlik için yeniden düzenleyeceğiz. **Önce bir plan ver** (hangi değişiklikleri yapacaksın, madde madde) — kodu henüz yazma; planı onaylayınca uygularsın. Kısıt: davranışı ve imzayı değiştirme. Uygulamayı yazınca, **bitirmeden önce** şunu doğrula: davranış aynı mı, imza aynı mı, her isim ne yaptığını söylüyor mu. İyice düşün.

```
[fonksiyon]
```

## Neden
İyi muhakeme yönlendirmesi 2026'da şu dengeyi kurar: modele **nasıl düşüneceğini** dikte etme (düşünen model bunu senden iyi yapar), ama muhakemenin **denetlenebilir noktalarını** talep et — önceden bir plan (sen onaylarsın), net kısıtlar, ve sonda somut kriterlerle öz-denetim. Böylece modelin gücünü kullanır ama kontrolü kaybetmezsin. Unutma: bu üç katman da bedava değil. Küçük ve düşük riskli bir değişiklikte plan + öz-denetim istemek, işi iki katına çıkarıp hiçbir şey kazandırmaz. Katmanları **riskle orantılı** kullan.
