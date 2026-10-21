---
slug: yazilim-ozel
baslik: Yazılım-Özel
aciklama: Kod yazdırma, ajan yönetimi ve mimari bağlamında prompt'lama. Bu kategori, tüm programın kalbi: AGENTS.md, spec-driven talimat, test-driven prompt, code review.
---

### Bu bölümde ne öğreneceksin?

Buraya kadarki teknikler her işte geçerliydi. Bu bölüm yalnızca yazılım işine özgü: kod yazan bir yapay zekâ ajanını nasıl yöneteceğin. Ajanın projenin kurallarına uymasını sağlamayı, ona reçete yerine sözleşme vermeyi, değişikliği gerçekten uygulattırmayı ve kapsamı dar tutmayı öğreneceksin.

**Bu bölümün yeni terimleri:**

- **Kod ajanı** (İng. *coding agent*): projenin dosyalarını okuyup değiştirebilen, komut çalıştırabilen yapay zekâ aracı. Claude Code ve Cursor bunun örnekleri.
- **AGENTS.md / CLAUDE.md**: projenin kökünde duran, ajanın her seferinde okuduğu kural dosyası. "Bu projede şöyle çalışılır" der.
- **PR** (İng. *pull request*): bir kod değişikliğini ekibin onayına sunma paketi.
- **Katman** (İng. *layer*): kodun sorumluluklara göre ayrılmış bölümleri (veri erişimi, iş kuralları, web arayüzü gibi) ve aralarındaki bağımlılık yönü.
- **Deterministik**: aynı girdiye her seferinde aynı çıktıyı veren. Aritmetik deterministiktir; model değildir.

# agents-md | Ajan talimat dosyası (AGENTS.md) | ai:false

## Görev
Bir yapay zekâ kod ajanının projende **tutarlı davranması** için bir talimat dosyasına (AGENTS.md / CLAUDE.md) koyacağın bir kural yaz. Kuralı, ajanın gerçekten uyacağı biçimde ifade et.

## Kötü örnek
`Kod temiz ve iyi olmalı. Best practice'lere uy. Kaliteli kod yaz.`

## İpucu
Muğlak "iyi/temiz" kuralları ajanı yönlendirmez. Kural spesifik olsun ve mümkünse bir örnek göstersin.

## İdeal çözüm
```
## Test
- Bir servis metodu eklerken/değiştirirken aynı PR'da birim testi ekle.
- Test adı davranışı anlatsın:
  ör. `bosListedeVarsayilanDoner()` — `test1()` değil.
```

## Neden
2026'da AGENTS.md (ve Claude Code için CLAUDE.md) kod ajanları için **yaygın bir standart** oldu — ajan her seferinde bu dosyayı okuyup kurallara uyar. İyi bir talimat dosyasının üç kuralı var: **kısa tut** (uzun dosya dikkati dağıtır), **kural başına bir örnek koy** ("göster, anlatma"), ve **yalnızca varsayılandan sapan kuralları yaz** (ajanın zaten bildiği genel şeyleri değil). "İyi kod yaz" bir kural değil; "servis değişikliğine aynı PR'da test ekle" bir kuraldır.

Şerh: bu dosya bir garanti değil, bir yönlendirmedir. Ajan kuralı kaçırabilir — özellikle dosya uzadıkça. Gerçekten uyulması şart olan kuralları **makineye zorlatmalısın**: derleme, test, mimari kuralı denetleyen otomatik kontroller. Talimat dosyası niyeti taşır; kapıyı kod tutar.

# spec-driven | Hedef + kısıt + çıktı sözleşmesi | ai:false

## Görev
Modele adım adım reçete vermek yerine, **hedefi, kısıtları ve beklenen çıktıyı** verip detayları ona bırakan bir prompt yaz.

## Kötü örnek
`önce ContractRepo class'ı aç, sonra findById metodu ekle, sonra Optional dön, sonra test yaz, sonra...`

## İpucu
Her ara adımı yazmak modeli boğar ve senin işini artırır. Ne istediğini (hedef), sınırları (kısıt) ve nasıl teslim edileceğini (çıktı) söyle; nasılını modele bırak.

## İdeal çözüm
**Hedef:** Sözleşmeleri kimliğine göre getiren bir depo (İng. *repository* — veriye erişimden sorumlu katman) ekle. **Kısıtlar:** mevcut katman yönüne uy (repo yalnızca types'a bağlı), bulunamayan kimlikte boş Optional dön, yeni bağımlılık ekleme. **Çıktı:** repo arayüzü + implementasyon + bir birim testi. Ara adımları bana sormana gerek yok; bu sözleşmeye uyduğun sürece uygulama detayı sana ait.

## Neden
Modern, düşünen modeller detayı doldurmakta iyidir — onlara her ara adımı dikte etmek (mikro-yönetim) hem zaman kaybı hem de modelin kendi iyi çözümünü engeller. 2026 önerisi **hedefe dayalı** (spec-driven) çalışmak: net bir hedef, güçlü kısıtlar, açık bir çıktı sözleşmesi ver; "nasıl"ı modele bırak. Sen sonucu **sözleşmeye göre** denetlersin. Bu, "önce planla" ile birleşir: büyük işte önce planı onaylat, sonra sözleşmeyle serbest bırak.

Ne zaman ters teper: hedef gerçekten belirsizse ya da doğru çözüm senin bildiğin ama modelin göremeyeceği bir yerel kısıta bağlıysa, serbest bırakmak yanlış yöne hızlı gitmek olur. Böyle durumlarda önce planı gör, sonra serbest bırak.

# emir-kipi | Kod ajanına emir kipi | ai:false

## Görev
Bir kod ajanının bir değişikliği gerçekten **uygulamasını** (sadece önermesini değil) istiyorsun. Doğru kiple bir prompt yaz.

## Kötü örnek
`bu fonksiyonu nasıl daha hızlı yapabiliriz, bir bakar mısın?`

## İpucu
"Bakar mısın / önerir misin" dersen model sadece önerir, dosyaya dokunmaz. Uygulatmak istiyorsan emir ver.

## İdeal çözüm
Aşağıdaki fonksiyonu, gereksiz döngüyü tek geçişe indirerek hızlandır ve **değişikliği doğrudan uygula**. Davranışı ve imzayı koru. Değişiklikten sonra ilgili testi çalıştır.

```
[fonksiyon]
```

## Neden
2026'da modeller talimat kipine çok duyarlı hale geldi. "Bir bakar mısın", "önerir misin", "nasıl yapabiliriz" gibi ifadeler modele **sadece öneri** ürettirir — dosyaya dokunmaz. Bir değişikliğin gerçekten uygulanmasını istiyorsan **emir kipi** kullan: "değiştir", "uygula", "ekle". Bu ayrım özellikle kod ajanlarında kritik: yanlış kip, "neden hiçbir şey değişmedi?" şaşkınlığının en sık nedenidir.

Ters yönü de bilinçli kullan: henüz karar vermediğin bir konuda emir kipi kullanma. "Bakar mısın" bir hata değil, **danışma kipidir**; keşif aşamasında doğru araçtır. Hata, uygulanmasını isterken danışma kipi kullanmaktır.

# test-driven | Testle tanımlanmış görev | ai:false

## Görev
Modele bir davranışı tarif etmek yerine, o davranışı **bir testle** tanımlayıp koda döktüren bir prompt yaz.

## Kötü örnek
`bir indirim hesaplama fonksiyonu yaz, işte kurallar: [uzun muğlak paragraf]`

## İpucu
Muğlak paragraf yerine, istediğin davranışı somut girdi→çıktı örnekleriyle (test gibi) tanımla; sonra o testleri geçen kodu iste.

## İdeal çözüm
Şu testleri geçen bir indirim fonksiyonu yaz:
- `hesapla(100, "yok") == 100`
- `hesapla(100, "yuzde10") == 90`
- `hesapla(100, "gecersiz")` → hata fırlatır

Önce bu davranışı karşıladığından emin ol, sonra kodu ver.

## Neden
Bir davranışı düzyazıyla tarif etmek belirsizliğe açıktır; aynı paragrafı iki kişi (ve model) farklı yorumlar. Testle tanımlamak (girdi→beklenen çıktı) davranışı **kesin ve doğrulanabilir** kılar — model tam olarak neyi karşılaması gerektiğini görür, ve sen sonucu aynı testlerle denetlersin. Bu, hem prompt'u netleştirir hem de çıktıyı otomatik doğrulanabilir yapar. "Kabul kriteri"nin kod dünyasındaki en keskin halidir.

Bir tehlike var: test listesi eksikse model **yalnızca o testleri geçen** ama gerçek işi yapmayan kod yazabilir — en uç örneği, girdiye bakıp sabit değer döndüren bir fonksiyondur. Panzehir: testlerin davranışın sınırlarını (boş girdi, negatif değer, üst sınır) kapsaması ve "testleri geçmek için özel durum kodlaması yazma" kısıtı.

# review-iste | Kod incelemesi (review) isteme | ai:false

## Görev
Modelden bir kod parçasını incelemesini isterken, **neye bakacağını** net bir çerçeveyle söyleyen bir prompt yaz.

## Kötü örnek
`bu koda bir bakar mısın, iyi mi?`

## İpucu
"İyi mi" muğlak; model rastgele yorumlar döker. İnceleme eksenlerini sen belirle.

## İdeal çözüm
Aşağıdaki değişikliği incele ve **yalnızca gerçek sorunları** raporla, şu üç eksende: (1) doğruluk/hata (kenar durumları, null, yarış durumu), (2) güvenlik, (3) mevcut desene uymayan yerler. Her bulgu için: dosya:satır, sorun tek cümle, önerilen düzeltme. Sorun yoksa "temiz" de — sorun uydurma.

```
[diff]
```

## Neden
"Bu iyi mi?" muğlak sorusu, modeli rastgele ve genelde yüzeysel yorumlar dökmeye iter — hatta olmayan sorunları "bulur". İnceleme eksenlerini (doğruluk, güvenlik, desen uyumu) ve çıktı biçimini (dosya:satır, tek cümle, düzeltme) sen belirlersen, model odaklanır. "Sorun yoksa temiz de, uydurma" izni, gereksiz gürültüyü keser. İyi bir review prompt'u, aslında bir rubrik + uydurma-önleyici kısıtın birleşimidir.

Dikkat edilecek ödünleşme: "yalnızca gerçek sorunları raporla" gibi bir süzgeç, yeni modellerde **fazla iyi** çalışıyor — model, emin olmadığı gerçek bir hatayı da eleyip susabilir. Kapsamlı tarama istiyorsan tersini söyle: "emin olmadıklarını da bildir, her bulguya güven düzeyi (yüksek/orta/düşük) ekle" — eleme işini sen sonra yap.

# llm-dostu-hata | LLM-dostu hata mesajı | ai:false

## Görev
Bir kod ajanının okuyup **kendi başına düzeltebileceği** bir hata mesajı yazmasını iste. Yani mesaj, bir yapay zekânın kök nedene ulaşmasına yetecek bağlamı taşısın. (LLM = büyük dil modeli, bu atölyede kısaca "model" dediğimiz şey.)

## Kötü örnek
`throw new Exception("Hata");`  ← ajan bununla ne yapsın?

## İpucu
"Hata oluştu" bir insana bile yaramaz, bir ajana hiç. İyi hata mesajı: ne oldu, hangi girdiyle, ne bekleniyordu — bunları içerir.

## İdeal çözüm
Bu doğrulama koduna, bir yapay zekâ ajanının okuyup düzeltebileceği bir hata mesajı ekle. Mesaj şunları içersin: **ne** oldu (hangi kural çiğnendi), **hangi girdiyle** (sorunlu değerin kendisi), **ne bekleniyordu**. Örnek biçim: `"Geçersiz vade: 0 ay geldi; vade 12–60 ay aralığında olmalı."`

```
[doğrulama kodu]
```

## Neden
2026'da kodunun bir kısmını yapay zekâ ajanları okuyup onaracak. "Hata oluştu" ya da `Exception("null")` gibi mesajlar ajana (ve insana) hiçbir ipucu vermez — ajan kök nedeni bulmak için körlemesine dosya okumak zorunda kalır, token ve zaman harcar, yanlış yeri düzeltir. İyi hata mesajı üç şeyi taşır: **ne** çiğnendi, **hangi değerle**, **ne bekleniyordu**. Bu, hata mesajını bir suçlamadan bir **tanıya** çevirir — hem insan hem ajan doğrudan çözüme gider.

Bir sınır: mesaja her şeyi koymak da doğru değil. Kullanıcıya dönen mesajlarda hassas veri (parola, kişisel bilgi, tam sorgu) **görünmemeli**; ayrıntıyı log'a yaz, kullanıcıya sade mesajı göster.

# hesap-koda | Deterministik işi koda yönlendir | ai:false

## Görev
Modelden bir harcama listesinin analizini istiyorsun — toplamlar, ortalamalar ve anormallikler.
Sayısal kısmın **kesin** olmasını garanti eden bir prompt yaz.

## Kötü örnek
`şu harcama listesinin toplamını, ortalamasını ve dikkat çeken kalemleri söyle: [veri]`

## İpucu
Model olasılıksal bir metin üreticidir; aritmetik ise deterministik iş ister. Kafadan hesap bazen
tutar, bazen **ikna edici biçimde yanlış** olur — en tehlikeli birleşim. İki işi ayır: hesabı kim
yapmalı, yorumu kim?

## İdeal çözüm
Aşağıdaki harcama listesini analiz et. **Tüm toplam ve ortalamaları kod yazarak hesapla ve kodu
çalıştır — kafadan hesaplama.** Sonra, kodun ürettiği kesin sayılar üzerinden olağandışı görünen
kalemleri gerekçesiyle yorumla. Veri: `[veri]`

## Neden
Modelin iki farklı yeteneğini karıştırmamak, üretim yapay zekâsının en önemli mimari derslerinden biridir:
**yorum/muhakeme modele, deterministik hesap koda (araca).** Model aritmetikte yanılabilir ve
yanlış sonucu kendinden emin açıklar — halüsinasyonun sayısal yüzü. "Kod yazarak hesapla ve çalıştır"
talimatı işi deterministik bir yürütücüye devreder; model sonra o **kesin** sonucu yorumlar. Bu,
büyük ölçekte araç çağırmanın (tool calling / MCP) varlık nedenidir: araçlar, modelin olasılıksal
dünyasına deterministik adalar açar. Kural: sayı, tarih, para — **asla kafadan; daima koda.**

Şerh: bu, hatayı yok etmez, **yerini değiştirir**. Model yanlış formülü kodlarsa sonuç yine yanlış olur —
ama artık yanlışlık görünür ve denetlenebilir bir yerdedir. Ayrıca araç çağırmanın da bedeli vardır:
her çağrı zaman ve token demek. İki sayıyı toplatmak için kod çalıştırmaya gerek yok.

# cerrahi-degisiklik | Cerrahi değişiklik (kapsam sınırlama) | ai:false

## Görev
Bir kod ajanına mevcut bir hatayı düzelttirirken, **yalnızca gerekli satırlara dokunmasını**
garanti eden bir prompt yaz. Senaryo: doğrulama fonksiyonunda boş e-posta çökmeye yol açıyor.

## Kötü örnek
`validator'daki boş email bug'ını düzelt — bu arada kodu da genel olarak toparlayabilirsin`

## İpucu
"Bu arada toparla" daveti, ajanı istenmeyen elden-geçirmeye (İng. *drive-by refactoring*) iter: yorumlar
değişir, istenmeyen doğrulamalar eklenir, değişiklik yığını şişer. Kapsamı SEN çiz; ilgisiz bulgular için ayrı
bir kanal tanımla.

## İdeal çözüm
Şu hatayı düzelt: boş e-posta `validate_user`'ı çökertiyor. **Kapsam:** yalnızca bu hatayı gideren
satırlar değişsin — yorumlara, biçime, komşu koda, başka doğrulamalara DOKUNMA; mevcut stili koru.
Test: **değişen her satır bu isteğe iz sürülebilir olmalı.** Düzeltme sırasında ilgisiz bir sorun
(ölü kod, eksik doğrulama) görürsen düzeltme — sonda tek satırlık not olarak bildir.

## Neden
Ajanlar "madem açtım, iyileştireyim" eğilimindedir; sonuç şişmiş bir değişiklik yığını, artan inceleme (review)
yükü ve bulanık bir proje geçmişidir. Kapsam sınırlama
(İng. *scope fencing*) bunu prompt seviyesinde keser: net hedef + dokunulmayacaklar listesi + iz-sürülebilirlik
testi + ilgisiz bulgular için ayrı kanal ("bildir, düzeltme"). Emir kipiyle birleşince,
ajan yönetiminin en çok değişiklik küçülten tekniklerinden biridir.

Şerhi de koy: "en küçük düzeltme" her zaman doğru düzeltme değildir. Bazı hatalar gerçekten yapısaldır ve
yamayla kapatmak borcu büyütür. Kapsam sınırlama, **kararı sana geri getirmek** içindir — ajan sana "burada
asıl sorun şu" diye not bırakır, büyük düzeltmeyi ayrı bir işte, bilerek yaparsın.

# yazilim-birlestir | Yazılım tekniklerini birleştir | ai:true

## Görev
Öğrendiklerini birleştir. Senaryo: bir kod ajanına, mevcut bir servise yeni bir metot ekletmek istiyorsun — projenin kurallarına uygun, test edilmiş, doğrudan uygulanmış. En iyi prompt'u yaz. Bu alıştırmayı AI Öğretmen değerlendirecek.

## İpucu
Hedef+kısıt+çıktı sözleşmesi ver (spec-driven), emir kipi kullan (uygula, öner değil), davranışı testle tanımla, ve projenin talimat dosyasındaki kurala (test ekle) uymasını iste.

## İdeal çözüm
**Hedef:** `RaporService`'e, bir haftanın tamamlanma yüzdesini dönen bir metot ekle. **Kısıtlar:** mevcut katman yönüne uy, yeni bağımlılık ekleme, imzayı `int haftaYuzdesi(int haftaNo)` yap. **Çıktı sözleşmesi:** metot + aynı PR'da davranışını doğrulayan bir birim testi (boş hafta → 0, tam hafta → 100). Değişikliği **doğrudan uygula**, sonra testi çalıştır. Ara adımları sormana gerek yok.

## Neden
Gerçek kod işinde teknikler birlikte çalışır: spec-driven ile hedefi/kısıtı/çıktıyı verirsin (mikro-yönetmeden), emir kipiyle uygulatırsın (öneri değil), testle davranışı kesinleştirirsin, ve projenin talimat dosyasındaki kurallara (test ekle, katmana uy) uymasını istersin. Bu birleşim, kod ajanını güvenle yönetmenin özüdür — programın 12 haftada öğrettiği her şeyin prompt seviyesindeki karşılığı.

Son hatırlatma: bu kalıp iyi bir varsayılan, evrensel bir reçete değil. Keşif işinde (henüz ne istediğini bilmiyorsun) sözleşme yazamazsın; orada önce konuşup öğrenirsin. Kalıbı, **hedefin net olduğu** işlerde kullan.
