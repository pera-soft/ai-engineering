---
slug: anti-pattern
baslik: Anti-Pattern'ler
aciklama: İyi teknikleri öğrenmek kadar, kötü alışkanlıkları tanımak da önemli. Bunlar prompt'u sessizce bozan yaygın hatalar — ve panzehirleri.
---

### Bu bölümde ne öğreneceksin?

Bu bölüm tersinden çalışır: iyi bir kalıp öğretmek yerine, **sık yapılan altı hatayı** tanımayı ve düzeltmeyi öğreteceğiz. Her hata gerçek bir kötü prompt üzerinden gösteriliyor; senin işin onu iyisine çevirmek.

**Anti-pattern** (Türkçesi *karşı-kalıp*): yaygın olduğu için doğru sanılan, ama düzenli olarak kötü sonuç veren çözüm biçimi. Bu bölümdeki altısı — belirsizlik, aşırı yükleme, çelişki, boş övgü, gömülü varsayım ve boş nezaket — prompt yazarken en çok karşılaşacağın karşı-kalıplardır.

# belirsizlik | Belirsizlik | ai:false

## Görev
Aşağıdaki belirsiz prompt'u, öğrendiğin netlik ilkeleriyle yeniden yaz. Amaç aynı kalsın ama model artık tahmin etmek zorunda kalmasın.

## Kötü örnek
`kodu daha iyi yap`

## İpucu
"İyi", "daha iyi", "güzel", "temiz" — bunlar model için ölçülebilir değil. Neyin iyileşeceğini somut bir eksenle söyle.

## İdeal çözüm
Aşağıdaki metodun **okunabilirliğini** artır: değişken adlarını anlamlı yap, 15 satırdan uzun ise mantıksal parçalara böl, iç içe geçmiş `if`'leri erken dönüşle sadeleştir. Davranışını değiştirme.

```
[kod]
```

## Neden
"Daha iyi yap" prompt'unun sorunu, "iyi"nin **kime göre** belirsiz olması. Model bir eksen seçmek zorunda kalır — belki performansı optimize eder, sen okunabilirlik isterken. Belirsizliğin panzehiri **ölçülebilir eksen**dir: "okunabilirlik", "20 satırın altı", "erken dönüş". Model artık tahmin etmez; senin tanımladığın hedefe koşar. Karşı uç da var: her şeyi milimetrik tarif etmek modeli bir yazıcıya indirger; net olmak, mikro-yönetmek demek değil. Ölçülebilir **hedefi** ver, uygulama detayını modele bırak.

# asiri-yukleme | Aşırı yükleme | ai:false

## Görev
Aşağıdaki prompt tek seferde çok fazla iş istiyor. Onu, modelin gerçekten iyi yapabileceği hale getir.

## Kötü örnek
`bu projeyi analiz et, tüm hataları bul, mimariyi baştan tasarla, testleri yaz, dokümantasyonu oluştur ve deployment planı çıkar`

## İpucu
Bir prompt'a ne kadar çok bağımsız iş koyarsan, model her birini o kadar yüzeysel yapar. Böl.

## İdeal çözüm
(Tek prompt yerine sırayla:)
**1. adım:** Bu projenin mimarisini analiz et ve en kritik 3 sorunu, her biri için tek paragraf gerekçeyle listele. Başka bir şey yapma — önce sorunları netleştirelim, sonra çözümlere geçeceğiz.

## Neden
Bir prompt'a altı bağımsız görev tıkıştırdığında, model **dikkatini böler** ve hepsini vasat yapar — hiçbirini derinlemesine değil. Ayrıca çıktı devasa ve denetlenemez olur. Panzehir: **tek prompt = tek net görev.** Büyük işi adımlara böl, her adımın çıktısını gör, sonrakine öyle geç. Bu hem kaliteyi hem senin kontrolünü artırır. Sınırı: birbirine sıkı bağlı işleri ayırmak da zarar verir — "fonksiyonu yaz ve testini yaz" tek istektir, çünkü ikisi aynı davranışı tanımlar. Bölünecek olan **bağımsız** işlerdir.

# celiski | Çelişkili talimat | ai:false

## Görev
Aşağıdaki prompt kendi içinde çelişiyor. Çelişkiyi bul ve tutarlı hale getir.

## Kötü örnek
`çok kısa ve öz ol ama her detayı kapsamlı şekilde açıkla ve hiçbir şeyi atlama`

## İpucu
"Kısa ol" ile "her detayı kapsa" aynı anda olmaz. Model çelişkide birini seçer — hangisini seçeceğini bilemezsin.

## İdeal çözüm
Şu konuyu **en fazla 5 madde** halinde özetle; her madde tek cümle olsun. Detaya girme — bu bir hızlı bakış (özet). Derinlik gerekirse ayrıca isteyeceğim.

## Neden
Çelişkili talimatta model bir tarafı seçmek zorunda kalır ve **hangisini seçeceği öngörülemez** — bazen kısa, bazen uzun, aynı prompt'la farklı sonuç. Çelişki çoğu zaman gizlidir: "kısa ama kapsamlı", "yaratıcı ama kurallara sıkı sıkıya bağlı", "hızlı ama çok dikkatli". Panzehir: bir öncelik seç ve netleştir. Gerçekten iki şey istiyorsan, ikisini iki ayrı istekle al. Bir ayrım yap: gerçek bir **ödünleşme** (güvenlik vs. hız — ikisi de gerekli, bazen çakışır) çelişki değildir; orada çözüm, isteği düzeltmek değil öncelik sırası vermektir (bkz. 04. bölüm, `oncelik-ver`).

# bos-ovgu | Boş övgü | ai:false

## Görev
Aşağıdaki prompt, işe yaramayan bir "yağ çekme" cümlesiyle başlıyor. O cümleyi, gerçekten işe yarayan bir yönlendirmeyle değiştir.

## Kötü örnek
`Sen dünyanın en zeki, en yetenekli yapay zekâsısın. Lütfen bu SQL sorgusunu optimize et.`

## İpucu
"Sen bir dahisin" cümlesi modelin çıktısını neredeyse hiç değiştirmez — boşa token. Onun yerine somut bir çerçeve/rol koy.

## İdeal çözüm
**Performans odaklı bir veritabanı uzmanı gibi davran.** Aşağıdaki SQL sorgusunu optimize et: hangi indekslerin eksik olduğunu, gereksiz tam-tablo taramalarını ve olası N+1 sorununu belirt. Her önerini, neden hızlandıracağını tek cümleyle gerekçelendir. (N+1 = tek sorguyla alınabilecek veriyi, kayıt başına ayrı sorgu atarak alma hatası.)

## Neden
"Sen bir dahisin", "elinden gelenin en iyisini yap" gibi boş övgüler modern modellerde çıktıyı anlamlı biçimde iyileştirmez — sadece yer kaplar ve seni "bir şey yaptım" yanılsamasına sokar. İşe yarayan şey övgü değil **yönlendirme**dir: somut bir rol, net bir görev, ölçülebilir kriterler. Modeli pohpohlamak yerine, ona hangi gözle bakacağını ve neyi arayacağını söyle. Ayrımı kaçırma: "sen bir dahisin" boştur, ama "kıdemli bir veritabanı uzmanı gibi davran" boş değildir — ikincisi övgü değil, **çerçeveleme**dir; modelin hangi bilgiyi öne çıkaracağını yönlendirir.

# gomulu-varsayim | Gömülü varsayım | ai:false

## Görev
Aşağıdaki prompt, söylenmemiş bir varsayıma dayanıyor ve bu yüzden yanlış cevaba yol açabilir. Varsayımı açığa çıkar ve prompt'u sağlamlaştır.

## Kötü örnek
`kullanıcı tablosuna neden bu kadar yavaş erişiyoruz, düzelt`

## İpucu
Prompt "yavaşlığın sebebi bellidir ve tek bir düzeltmesi vardır" diye varsayıyor. Model körlemesine bir sebep uydurabilir. Modele önce teşhis ettir.

## İdeal çözüm
Kullanıcı tablosuna erişim yavaş. **Önce olası sebepleri sırala** (eksik indeks, tam-tablo taraması, kilitlenme, N+1, ağ), her biri için nasıl doğrulanacağını söyle. Kesin bir düzeltme önermeden önce hangi bilginin gerektiğini belirt. Elimdeki veri: `[sorgu / şema / EXPLAIN çıktısı]`.

## Neden
"Düzelt" dediğinde, prompt gizlice "sebep tek ve belli" diye varsayar — model de bu varsayımı yutup ilk akla geleni "kesin sebep" gibi sunar. Gömülü varsayımlar en sinsi hatadır çünkü ne sen ne model onları fark eder. Panzehir: varsayımı açığa çıkar ("sebebi henüz bilmiyoruz") ve modeli **önce teşhise**, sonra çözüme yönlendir. Her varsayımı sökmek de gerekmez: sebebi gerçekten biliyorsan teşhis istemek boşuna bir tur olur. Kendine tek soru sor: *"Bunu gerçekten biliyor muyum, yoksa varsayıyor muyum?"*

# kibar-ise-yaramaz | Kibar ama işe yaramaz | ai:false

## Görev
Aşağıdaki prompt kibar ve uzun ama modele hiçbir işe yarar bilgi vermiyor. Nezaketi at, işe yarar içeriği koy.

## Kötü örnek
`Merhaba! Umarım iyisindir. Senden küçük bir ricam olacak, çok zahmet olmazsa eğer müsaitsen şu kodla ilgili bana yardımcı olabilir misin acaba? Şimdiden çok teşekkürler!`

## İpucu
Modelin morali yok; nezaket cümleleri sadece token harcar ve asıl isteği geciktirir. İsteği net ve doğrudan söyle.

## İdeal çözüm
Aşağıdaki fonksiyonda bir `NullPointerException` alıyorum, 12. satırda. Girdi olarak boş bir liste geldiğinde oluşuyor. Sebebi bul ve boş liste durumunu güvenli biçimde ele alan bir düzeltme öner.

```
[fonksiyon]
```

## Neden
İnsanlara karşı nezaket değerlidir; ama modele karşı "rica etsem acaba müsait misin" cümleleri **hiçbir işe yaramaz** — model zaten cevap verecek. Bu cümleler asıl isteği gömer, token harcar ve bazen modeli asıl sorudan uzaklaştırır. İyi prompt kaba değildir; sadece **doğrudandır**: ne olduğu, ne zaman olduğu, ne istediğin. Enerjiyi nezakete değil, bağlam ve netliğe harca. Küçük bir şerh: bir "lütfen" ya da "teşekkürler" ölçülebilir bir zarar vermez — sorun kibarlık değil, **isteğin kendisini geciktiren uzun giriş**tir.

# duzelt-hepsini | Hepsini uygula: kötü prompt'u kurtar | ai:true

## Görev
Aşağıda, bu bölümdeki neredeyse her hatayı birden yapan gerçekçi-kötü bir prompt var. Onu, öğrendiğin her şeyi kullanarak baştan yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## Kötü örnek
`Selam! Sen çok yeteneklisin, senden ricam şu harika projede kodu genel olarak daha iyi, daha temiz ve çok kısa ama aynı zamanda çok detaylı yap, tüm sorunları çöz ve neden yavaş olduğunu da düzelt lütfen, teşekkürler!`

## İpucu
Bu prompt'ta belirsizlik ("daha iyi"), aşırı yükleme ("tüm sorunları çöz"), çelişki ("kısa ama detaylı"), boş övgü ("çok yeteneklisin"), gömülü varsayım ("neden yavaş belli") ve boş nezaket bir arada. Hepsini teker teker temizle: tek net görev seç, ölçülebilir kriter koy, bağlam ver, sınır çiz.

## İdeal çözüm
Aşağıdaki `RaporService.rapor()` metodunun **okunabilirliğini** artır: uzun metodu mantıksal özel (private) metotlara böl, değişken adlarını anlamlı yap, davranışı değiştirme, imzayı değiştirme. Bu ilk adım — performansı ayrıca ele alacağız, şimdi ona girme. Bağlam: Spring Boot servis sınıfı, metot ~40 satır.

```
[metot]
```

## Neden
Gerçek dünyada kötü prompt'lar tek bir hata yapmaz — hepsini birden yapar ve sonuç, modelin ne yapacağını bilememesidir. Kurtarmanın yolu, hataları teker teker sökmektir: belirsizliği ölçülebilir eksene çevir, altı işi tek işe indir, çelişkiyi çöz, övgüyü rolle değiştir, varsayımı açığa çıkar, nezaketi bağlamla değiştir. Geriye kalan şey kısa, net, denetlenebilir bir istek olur — ve model tam da onu yapar.

Bitirirken atölyenin genel dersi: buradaki hiçbir kalıp evrensel değildir. Model kuşakları değiştikçe kurallar da değişti — "adım adım düşün" bir zamanlar altın kuraldı, bugün düşünen modellerde gereksiz; "MUTLAKA" diye bağırmak bir zamanlar gerekliydi, bugün ters tepiyor. Ezberlenecek şey kalıp değil, **alışkanlık**: prompt'unu değiştir, sonucu ölç, işe yaramayanı at.
