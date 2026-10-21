---
slug: temel-yapi
baslik: Temel Yapı
aciklama: Bir prompt'un iskeleti — net görev, bağlam, rol, çıktı formatı, sınır ve örnek. Bu altı taş oturmadan gerisi havada kalır.
---

### Bu bölümde ne öğreneceksin?

Yapay zekâya iş yaptırmanın tek yolu, ne istediğini yazıyla anlatmaktır. Bu bölümde o yazının altı yapı taşını öğreneceksin: net görev, bağlam, rol, çıktı biçimi, sınır ve örnek. Sonunda altısını tek bir istekte birleştireceksin.

**Önce sözlük.** Aşağıdaki kelimeler bu atölyenin her yerinde geçiyor. Hiçbirini önceden bilmene gerek yok; hepsi burada tanımlı.

- **Model** (İng. *model*): metin okuyup metin üreten yapay zekâ programı. Bir sonraki kelimeyi olasılık hesabıyla tahmin ederek yazar. Bu yüzden "olasılıksal"dır: aynı soruya her seferinde birebir aynı cevabı vermeyebilir.
- **Prompt** (Türkçesi *istem*): modele yazdığın metnin tamamı — görev, açıklama, veri, hepsi birlikte. Bu atölyenin konusu bu metni iyi yazmaktır.
- **Token**: modelin metni işlerken kullandığı en küçük parça; kabaca bir kelimenin parçası. Türkçe bir kelime ortalama 2–3 token eder. Ücret ve hız token sayısıyla ölçülür.
- **Bağlam penceresi** (İng. *context window*): modelin tek seferde görebildiği toplam token miktarı — yani aynı anda okuyabildiği metnin sınırı. Sınırın dışında kalan hiç görülmez.
- **Ajan** (İng. *agent*): tek cevap verip susmak yerine, bir hedefe ulaşmak için kendi başına adım adım ilerleyen — dosya okuyan, komut çalıştıran, sonuca göre karar veren — yapay zekâ kurulumu. Kod asistanları böyle çalışır.
- **Araç çağırma** (İng. *tool calling*): modelin, kendi başına yapamadığı bir işi (dosya okuma, hesap, veritabanı sorgusu) dışarıdaki bir programa yaptırıp sonucu geri alması.
- **Sıcaklık** (İng. *temperature*): modelin ne kadar serbest yazacağını ayarlayan sayı. 0'a yakın değer daha kararlı ve tekrarlanabilir, yüksek değer daha çeşitli ama daha savruk çıktı verir. Bu bir prompt tekniği değil, kodda verilen bir ayardır; prompt yazarak değiştirilemez. (2026 notu: en yeni üst seviye modellerin bir kısmında bu ayar tümüyle kaldırıldı — davranışı artık yalnız prompt'la yönlendirirsin.)
- **Halüsinasyon** (İng. *hallucination*): modelin bilmediği bir şeyi "bilmiyorum" demek yerine kendinden emin biçimde uydurması.

Bir uyarı: buradaki hiçbir kalıp "her zaman çalışan" bir formül değil. Her tekniğin ne zaman işe yaramadığını da yazdık.

# net-gorev | Net görev verme | ai:false

## Görev
Bir yapay zekâ ajanına (kendi başına adım adım iş yapabilen yapay zekâ kurulumu), elindeki bir Java metodunun ne işe yaradığını açıklatmak istiyorsun. Bunu isteyen bir prompt (modele yazdığın metnin tamamı) yaz — ama çıktının **kime**, **ne uzunlukta** ve **hangi amaçla** olduğunu belirt.

## Kötü örnek
`şunu açıkla: [kod]`

## İpucu
"Açıkla" tek başına belirsizdir: kime açıklayacak (uzmana mı, yeni başlayana mı)? Ne kadar uzun? Ne için? Bu üç şeyi söyle.

## İdeal çözüm
Aşağıdaki Java metodunun ne yaptığını, **yazılıma yeni başlamış** bir kişiye **tek cümlede**, jargon kullanmadan açıkla. Amaç: kod incelemesinde (İng. *code review* — bir değişikliğin başka bir geliştirici tarafından gözden geçirilmesi) hızlı bir özet.

```java
[metodu buraya yapıştır]
```

## Neden
"Şunu açıkla" prompt'u, modeli boşlukları kendi varsayımlarıyla doldurmaya iter — bazen üç paragraf teknik jargon döker, sen tek cümle isterken. İyi bir görev tanımı üç boşluğu kapatır: **hedef kitle** (yeni başlayan), **biçim/uzunluk** (tek cümle) ve **amaç** (code review özeti). Model artık tahmin etmez; hizalanır. Sınırı da bil: netlik, modelin *bilmediği* bir şeyi bilmesini sağlamaz. Kodun ne yaptığı verdiğin metinden çıkmıyorsa, en net görev tanımı bile uydurmayı engellemez — bunun ilacı bağlam ve "emin değilsen söyle" iznidir (bkz. 04. bölüm).

# baglam | Bağlam verme | ai:false

## Görev
Bir ajanın, projenin bir yerindeki hata mesajını daha iyi hale getirmesini istiyorsun. Ama ajan projeni tanımıyor. Ona **gerekli bağlamı** veren bir prompt yaz. (Bağlam = modelin işi yapabilmesi için bilmesi gereken, prompt'a senin koyduğun bilgi.)

## Kötü örnek
`bu hata mesajını düzelt: "Hata oluştu"`

## İpucu
Model senin kafandakini göremez. Hangi dilde yazıldığı, mesajın kim tarafından okunacağı, projenin tonu gibi bağlamı prompt'un içine koy.

## İdeal çözüm
Aşağıdaki hata mesajı, bir Spring Boot uygulamasının (Java ile web servisi yazmakta kullanılan çatı) kullanıcıya dönen yanıtında görünüyor. Mesajı okuyan kişi **son kullanıcı** (teknik değil). Mesajı, **ne olduğunu ve ne yapması gerektiğini** söyleyecek şekilde Türkçe olarak yeniden yaz; teknik ayrıntı (hata izi, sınıf adı) verme.

Mevcut mesaj: `"Hata oluştu"`

## Neden
Model, senin projenin dilini, kullanıcısını ve tonunu **bilmez** — sadece verdiğin metni bilir. Bağlamı prompt'a koymazsan, model "genel" bir cevap üretir ve o cevap senin bağlamına oturmaz. Bağlam, modeli senin dünyana çeken halattır: kim okuyacak, nerede görünecek, hangi kısıtlar var. Ama "daha çok bağlam daima daha iyi" değildir: alakasız bilgi yığmak modeli yanlış yöne çeker ve token yakar. Doğru ölçü, **işe yarayan kadarı** — nedenini 07. bölümde göreceksin.

# rol | Rol / persona verme | ai:false

## Görev
Bir tasarım kararını (bir API'yi REST mi gRPC mi yapmalı) tartışmak istiyorsun. Modele bir **rol** vererek cevabın perspektifini ve derinliğini ayarla. (API = iki programın birbiriyle konuşma arayüzü; REST ve gRPC bu konuşmanın iki farklı biçimi.)

## Kötü örnek
`REST mi gRPC mi kullanmalıyım?`

## İpucu
Aynı soruya "acemi bir öğrenci" ile "20 yıllık bir mimar" farklı cevap verir. Modele hangi gözle bakmasını istediğini söyle.

## İdeal çözüm
**Kıdemli bir yazılım mimarı gibi davran.** İki servis arasındaki bir iç iletişim için REST mi yoksa gRPC mi seçmeliyim? Kararı **ödünleşmelerle** (performans, sözleşme netliği, araç desteği, ekip aşinalığı) gerekçelendir ve net bir öneriyle bitir. Bağlam: yüksek hacimli, düşük gecikme isteyen iki iç mikroservis.

## Neden
Rol (persona), modele bir **bakış açısı ve derinlik seviyesi** verir. "Kıdemli mimar gibi davran" demek, modelin ödünleşme diliyle konuşmasını, yüzeysel bir liste yerine gerekçeli bir karar üretmesini tetikler. Rol bir sihir değil, bir **çerçeveleme**dir: modelin hangi bilgi katmanını öne çıkaracağını yönlendirir. Sınırı net: rol vermek modele **yeni bilgi eklemez**. "Kıdemli mimar" dediğinde model daha çok şey bilmez, sadece bildiğini farklı seçer ve farklı anlatır. Ölçülebilir kriter yazmak (bkz. 02. bölüm, `rubrik`) çoğu işte rolden daha etkilidir; ikisini birlikte kullanmak en iyisidir.

# format | Çıktı formatını belirtme | ai:false

## Görev
Bir kod tabanındaki üç ayrı güvenlik riskini modele buldurup, sonucu **doğrudan işine yarayacak bir biçimde** almak istiyorsun. Çıktı formatını dikte eden bir prompt yaz.

## Kötü örnek
`bu koddaki güvenlik açıklarını bul`

## İpucu
Serbest metin okumak ve ayıklamak zaman alır. Modelden çıktıyı hazır kullanabileceğin bir yapıda (tablo, liste, JSON) iste.

## İdeal çözüm
Aşağıdaki kodda en fazla 3 güvenlik riski bul. Her biri için **şu üç sütunlu bir tablo** üret: `Risk | Neden tehlikeli | Somut düzeltme`. Tablo dışında açıklama yazma.

```
[kod]
```

## Neden
Model ne isterse o biçimde cevap verir; biçimi sen söylemezsen model seçer — genelde uzun, dağınık düzyazı. Çıktı formatını dikte etmek iki işe yarar: cevabı **doğrudan kullanılabilir** kılar (kopyala-yapıştır, tabloya dök) ve modeli **disipline eder** (üç satırlık tablo istedin, roman yazamaz). "Tablo dışında açıklama yazma" gibi bir sınır, gevezeliği keser. Bedeli de var: zor bir muhakemeyi (akıl yürütmeyi) çok katı bir biçime sıkıştırmak, düşünme kalitesini düşürebilir — "format vergisi" denen bu tuzağı ve çözümünü 03. bölümde göreceksin.

# negatif | "Ne yapma" — negatif kısıt | ai:false

## Görev
Bir ajandan bir fonksiyonu yeniden düzenlemesini (İng. *refactor* — davranışı değiştirmeden kodun iç yapısını iyileştirme) isteyeceksin, ama davranışını **değiştirmesini istemiyorsun**. Modele ne yapmayacağını da söyleyen bir prompt yaz.

## Kötü örnek
`bu fonksiyonu iyileştir`

## İpucu
"İyileştir" model için davetkârdır — imzayı değiştirir, yeni özellik ekler, kütüphane getirir. Sınırları açıkça çiz: neye dokunmasın?

## İdeal çözüm
Aşağıdaki fonksiyonun **okunabilirliğini** artır (isimlendirme, erken dönüş, gereksiz iç içe geçmeyi azalt). **Şunları YAPMA:** genel imzayı (parametreler, dönüş tipi) değiştirme, yeni bağımlılık ekleme, davranışı değiştirme. Sadece iç yapıyı sadeleştir.

```
[fonksiyon]
```

## Neden
Modeller yardımsever olmaya eğilimlidir — bu yüzden istemediğin "iyilikleri" de yaparlar: imzayı değiştirir, kütüphane önerir, kapsamı genişletir. Negatif kısıt (ne YAPMA) bu yardımseverliği frenler. Özellikle refactor, taşıma (migration) ve düzeltme gibi "sınırlı müdahale" işlerinde, neye dokunulmayacağını söylemek neye dokunulacağını söylemek kadar önemlidir. Sınırı da bil: yasak listesi uzadıkça etkisi azalır ve model neyi *yapacağını* yine tahmin eder. Kural şu: **kapsam ve güvenlik** için negatif kısıt gerekli; **biçim** için olumlu tarif ("akıcı paragraflarla yaz") daha güçlüdür (bkz. 03. bölüm, `yasak-degil-yap`).

# few-shot | Örnekle öğretme (few-shot) | ai:false

## Görev
Ajanın, commit mesajlarını senin ekibinin tam istediği tarzda yazmasını istiyorsun: tek satır, emir kipi, önüne köşeli parantezde faz etiketi. Bunu **örnek göstererek** öğreten bir prompt yaz. (Commit mesajı = bir kod değişikliğini özetleyen tek satırlık kayıt.)

## Kötü örnek
`commit mesajı yaz`

## İpucu
Tarzı kelimelerle tarif etmek zordur; bir-iki örnek göstermek çoğu zaman tarif etmekten daha nettir. Modele istediğin biçimin örneklerini ver.

## İdeal çözüm
Aşağıdaki değişiklik için bir commit mesajı yaz. Tarz **şu örneklerdeki gibi** olsun (tek satır, emir kipi, başta köşeli parantezli etiket):

```
[faz-2] Token kullanım tablosunu ekle
[icerik] Hafta 3 quiz sorularını güncelle
[fix] Boş API anahtarında çökmeyi önle
```

Değişiklik: `[değişikliğin özeti]`

## Neden
Bazı şeyleri tarif etmek, göstermekten zordur — tarz, ton, biçim gibi. "Tek satır, emir kipi, etiketli" desen bile model senin kafandaki "etiket"i tam kestiremez. Üç örnek gösterdiğin an desen kristalleşir: model örneklerdeki **ortak yapıyı** çıkarır ve tekrarlar. Buna **az örnekle öğretme** (İng. *few-shot*) denir; örneksiz, sadece talimatla istemeye de **örneksiz sorma** (İng. *zero-shot*) denir. Ne zaman işe yaramaz: "ne kadar çok örnek o kadar iyi" bir efsanedir. Birbiriyle çelişen ya da talimatla uyuşmayan örnekler yanlış deseni öğretir; kendi içinde muhakeme yapan yeni modellerde örnek yığmak çıktıyı bozabilir. Doğru sıra: **önce örneksiz dene, tutmazsa tek temiz örnek ekle** (bkz. 06. bölüm, `zero-shot-once`).

# birlestir | Altı taşı birleştir | ai:true

## Görev
Şimdi öğrendiğin altı öğeyi (net görev, bağlam, rol, format, negatif kısıt, örnek) **tek bir prompt'ta** birleştir. Senaryo: bir yapay zekâ ajanının, projenin `README.md` dosyasına "Kurulum" bölümü yazmasını istiyorsun. Elinden gelen en iyi, eksiksiz prompt'u yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
İyi bir birleşik prompt genelde şu katmanları içerir: bir rol (opsiyonel), net görev, gerekli bağlam, çıktı formatı, sınırlar (ne yapma) ve gerekirse bir örnek. Hepsini tıkıştırma — senaryoya gerçekten gerekli olanları koy.

## İdeal çözüm
**Deneyimli bir teknik yazar gibi davran.** Bu proje için `README.md` dosyasına bir **"Kurulum"** bölümü yaz. Bağlam: proje bir Spring Boot (Java 17) uygulaması, Maven Wrapper (`./mvnw`) ile derleniyor, çalışması için bir ortam değişkeni (API anahtarı) gerekiyor, yerelde 8090 portunda açılıyor. Çıktı: numaralı adımlar halinde, her adım tek bir komut ya da tek bir eylem olsun; komutları kod bloğunda ver. **Şunları YAPMA:** projeyi tanıtan pazarlama cümleleri ekleme, var olmayan adım uydurma. Tarz şu örnekteki gibi kısa ve emir kipinde olsun: "1. Depoyu klonla: `git clone ...`".

## Neden
Gerçek işlerde teknikler tek tek değil, **birlikte** kullanılır. İyi bir prompt bir katmanlar bütünüdür: rol perspektifi ayarlar, görev hedefi verir, bağlam modeli senin dünyana çeker, format çıktıyı kullanılabilir kılar, negatif kısıt gevezeliği keser, örnek tarzı sabitler. Ustalık, hepsini koymakta değil — **senaryoya gerçekten gerekeni** seçip dengelemekte. Altı katmanı da her prompt'a doldurursan istek şişer, model önemliyi önemsizden ayıramaz ve sonuç kötüleşir. Kısa bir işte tek cümlelik net bir görev, altı katmanlı bir metinden iyidir.
