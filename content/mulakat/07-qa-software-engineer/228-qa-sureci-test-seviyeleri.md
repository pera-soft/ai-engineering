---
slug: qa-sureci-test-seviyeleri
baslik: "QA Nedir? Test Seviyeleri, Test Türleri ve Süreçteki Yeri"
kademe: Temel
sira: 228
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Kalabalık bir restoran mutfağı düşün. Mutfağın **hijyen kuralları**, tarif standartları, personel eğitimi ve "malzeme depoya girerken kontrol edilir" prosedürü vardır — bunlar hatayı **oluşmadan önce** engellemeye çalışır. Bir de tabak servise çıkmadan önce şefin tadına bakması, sunumuna göz atması vardır — bu, çıkmış olan **ürünü** denetler ve hatayı **oluştuktan sonra** yakalar. Şefin elindeki kaşık ise bu denetimin **aracıdır**. Yazılımda bu üç şeyin adları sırasıyla **kalite güvence (quality assurance, QA)**, **kalite kontrol (quality control, QC)** ve **test**'tir. Mülakatta "QA nedir?" sorusuna "test yapmaktır" diye cevap vermek, "mutfak yönetimi nedir?" sorusuna "kaşıkla tatmaktır" demeye benzer — teknik olarak temas eder ama kapsamı kaçırır. Bir e-ticaret ekibinde QA'nın işi yalnız kampanya günü sepeti tıklamak değildir; kabul kriterlerinin yazımına katılmak, "bitti" tanımını netleştirmek, sürüm çıkış kriterlerini savunmak ve hatanın hangi seviyede yakalanacağına karar vermektir.

### Temel kavramlar

- **Kalite güvence (quality assurance, QA).** **Süreç odaklıdır ve önleyicidir.** Sorusu şudur: "Bu ekibin çalışma biçimi, hatalı ürün üretmeyi zorlaştırıyor mu?" QA faaliyetleri şunlardır: kod gözden geçirme (code review) zorunluluğu, kabul kriteri şablonu, sürüm kontrol listesi ve otomatik derleme hattı (CI) kuralları. Çıktısı bir hata listesi değil, **daha az hata üreten bir süreçtir**.
- **Kalite kontrol (quality control, QC).** **Ürün odaklıdır ve tespit edicidir.** Sorusu şudur: "Bu çıkan sürüm şartları karşılıyor mu?" Ürünü inceler, ölçer, kanıt üretir. Çıktısı bulgulardır.
- **Test.** QC'nin araçlarından **biridir** — tek aracı değil. Gözden geçirme (review), statik analiz, denetim (audit) de QC kapsamına girebilir. Test, yazılımı çalıştırıp veya inceleyip gözlem yapmak ve bu gözlemi beklenenle karşılaştırmaktır.
- **Doğrulama (verification).** "Ürünü **doğru mu** yapıyoruz?" Yani çıktı, kendi şartnamesine/tasarımına uygun mu? Birim, entegrasyon ve sistem testleri ağırlıklı olarak doğrulamadır.
- **Geçerleme (validation).** "**Doğru ürünü** mü yapıyoruz?" Yani şartnamenin kendisi kullanıcının gerçek ihtiyacını karşılıyor mu? Kabul testi (UAT), kullanılabilirlik testi ve gerçek kullanıcı geri bildirimi geçerlemedir. Şartname yanlışsa bütün doğrulama testleri yeşil yanar, ürün yine de başarısız olur.
- **Test seviyeleri (test levels).** Testin **neyi** kapsadığını belirler: birim (unit) → entegrasyon (integration) → sistem (system) → kabul (acceptance/UAT). Seviye, testin *büyüklüğü* ve *sahibi* ile ilgilidir.
- **Test türleri (test types).** Testin **niçin** koşulduğunu belirler: duman (smoke), akıl sağlığı (sanity), regresyon, yeniden test (retest), keşifsel (exploratory), kabul. Seviye ile tür birbirinden bağımsızdır: regresyon testi hem birim hem sistem seviyesinde olabilir.
- **Fonksiyonel / fonksiyonel olmayan (functional / non-functional) test.** Fonksiyonel: "kupon uygulanınca toplam doğru hesaplanıyor mu?" Fonksiyonel olmayan: performans, yük, güvenlik, kullanılabilirlik, erişilebilirlik, uyumluluk (tarayıcı/cihaz), taşınabilirlik. E-ticarette kampanya günü çöken sepet servisi fonksiyonel olarak "doğru" çalışıyordu; kaybedilen şey fonksiyonel olmayan bir gereksinimdi.
- **Statik / dinamik test.** Dinamik test yazılımı **çalıştırır**. Statik test çalıştırmaz: gereksinim gözden geçirme, kod gözden geçirme, statik analiz araçları. **Gözden geçirme de bir test faaliyetidir** ve en ucuz hata bulma yöntemidir, çünkü hatayı henüz kod yazılmadan yakalar.
- **Giriş / çıkış kriterleri (entry / exit criteria).** Giriş kriteri: test başlamadan önce sağlanması gerekenler (ortam hazır, duman testi geçti, kabul kriterleri yazılı). Çıkış kriteri: testin bitmiş sayılması için gerekenler (planlanan senaryoların %X'i koşuldu, açık kritik/yüksek şiddetli hata yok, regresyon paketi yeşil).
- **"Bitti" tanımı (definition of done, DoD).** Ekibin ortak sözleşmesi: bir iş ne zaman bitmiş sayılır? İçinde genelde "birim testleri yazıldı", "kod gözden geçirildi", "kabul kriterleri doğrulandı", "regresyon paketine eklendi" maddeleri bulunur. DoD tek bir iş için, çıkış kriteri ise bütün bir test seviyesi/sürümü için geçerlidir.
- **Kapsam (coverage).** Testin ne kadarını dokunduğunun ölçüsü: satır kapsamı, dal (branch) kapsamı, gereksinim kapsamı. **%100 kapsam "hatasız" demek değildir** — kapsam yalnız kodun **çalıştırıldığını** ölçer, doğru şeyin **doğrulandığını** ölçmez.

```svg
<svg viewBox="0 0 660 262" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kalite güvence, kalite kontrol ve test kavramlarının iç içe kapsamı">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">QA, QC ve test: iç içe geçmiş üç ayrı kavram</text>
  <rect x="30" y="42" width="600" height="200" rx="14" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="330" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B07D20" font-family="system-ui">Kalite güvence (QA) — SÜREÇ odaklı, ÖNLEYİCİ</text>
  <text x="330" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">standart, kabul kriteri şablonu, gözden geçirme kuralı, CI kapıları, eğitim</text>
  <rect x="70" y="96" width="520" height="134" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="120" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Kalite kontrol (QC) — ÜRÜN odaklı, TESPİT EDİCİ</text>
  <text x="330" y="138" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çıkan sürümün şartları karşıladığının denetlenmesi, bulgu üretimi</text>
  <rect x="118" y="150" width="424" height="66" rx="10" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="174" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Test — QC'nin araçlarından biri</text>
  <text x="330" y="194" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yazılımı çalıştırıp veya inceleyip beklenenle karşılaştırmak</text>
  <text x="330" y="210" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(gözden geçirme ve statik analiz de QC'nin araçlarındandır)</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

**1. Test seviyeleri: hangi hata nerede yakalanır.** Her seviyenin ayrı bir sahibi, ayrı bir amacı ve ayrı bir maliyet profili vardır.

1. **Birim testi (unit).** Sahibi **geliştiricidir**. Tek bir sınıf/fonksiyonun mantığını izole sınar (ör. `KuponHesaplayici.indirimUygula()`). Saniyeler sürer, hata konumu nettir.
2. **Entegrasyon testi (integration).** İki veya daha çok bileşenin **birbiriyle konuşmasını** sınar (sepet servisi ↔ stok servisi, servis ↔ veritabanı). Burada yakalanan tipik hata "tek tek doğru, birlikte yanlış" hatasıdır: alan adı uyuşmazlığı, para birimi/ondalık farkı, zaman aşımı.
3. **Sistem testi (system).** Bütün ürünü, gerçek konuşlandırılmış hâline yakın bir ortamda, uçtan uca sınar. Sahibi genelde **QA**'dır. "Ürünü listele → sepete ekle → kupon uygula → ödeme adımına geç" akışı burada koşar.
4. **Kabul testi (acceptance / UAT).** Sahibi **iş birimi / müşteri**dir. Sorusu teknik uygunluk değil, **iş ihtiyacının karşılanıp karşılanmadığıdır**. Sistem testi doğrulama, kabul testi ağırlıklı olarak geçerlemedir.

Bu dosya seviyelerin *terminolojisini* anlatır; hangi seviyeye kaç test yazılacağı, test double kullanımı, sözleşme (contract) testi ve kararsız (flaky) test yönetimi ayrı bir konudur — `187 Test Stratejisi`'ne bak.

```svg
<svg viewBox="0 0 660 302" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yapım aşamaları ile test seviyelerinin eşleşmesi ve doğrulama-geçerleme ayrımı">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her yapım aşamasının bir test seviyesi karşılığı vardır</text>
  <text x="155" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Yapım aşaması</text>
  <text x="505" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Karşılık gelen test seviyesi</text>
  <rect x="30" y="62" width="250" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="155" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gereksinim / kabul kriteri</text>
  <text x="155" y="98" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iş biriminin ihtiyacı</text>
  <rect x="380" y="62" width="250" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="505" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Kabul testi (UAT)</text>
  <text x="505" y="98" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sahip: iş birimi · geçerleme</text>
  <line x1="284" y1="84" x2="376" y2="84" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="5 4"/>
  <rect x="30" y="114" width="250" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="155" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sistem tasarımı</text>
  <text x="155" y="150" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">uçtan uca akışlar</text>
  <rect x="380" y="114" width="250" height="44" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="505" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sistem testi</text>
  <text x="505" y="150" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sahip: QA · doğrulama</text>
  <line x1="284" y1="136" x2="376" y2="136" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="5 4"/>
  <rect x="30" y="166" width="250" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="155" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Modül / arayüz tasarımı</text>
  <text x="155" y="202" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">servisler arası sözleşme</text>
  <rect x="380" y="166" width="250" height="44" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="505" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Entegrasyon testi</text>
  <text x="505" y="202" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sahip: geliştirici + QA</text>
  <line x1="284" y1="188" x2="376" y2="188" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="5 4"/>
  <rect x="30" y="218" width="250" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="155" y="238" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kodlama</text>
  <text x="155" y="254" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sınıf ve fonksiyonlar</text>
  <rect x="380" y="218" width="250" height="44" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="505" y="238" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Birim testi (unit)</text>
  <text x="505" y="254" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sahip: geliştirici</text>
  <line x1="284" y1="240" x2="376" y2="240" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="5 4"/>
  <text x="330" y="284" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Hata ne kadar yukarıda (geç) bulunursa düzeltme maliyeti o kadar yüksektir.</text>
</svg>
```

**2. Test türleri: aynı testin niçin koşulduğu.**

1. **Duman testi (smoke).** "Bu sürüm test edilmeye değer mi?" Çok kısa, geniş ve sığ bir kontrol: site açılıyor mu, giriş yapılıyor mu, ürün listeleniyor mu, sepete eklenebiliyor mu. **Test sürecinin giriş kriteridir**; kalırsa sürüm geri çevrilir, ayrıntılı teste hiç başlanmaz.
2. **Akıl sağlığı testi (sanity).** Dar ve derin: yalnız değişen/düzeltilen alana odaklanan hızlı kontrol. Duman geniş-sığ, akıl sağlığı dar-derindir.
3. **Yeniden test (retest / confirmation).** Bir hatanın **düzeldiğinin** kanıtı. Hata kaydındaki **aynı adımlar aynı veriyle** tekrar koşulur. Sonucu ikili: hata gitti veya gitmedi.
4. **Regresyon testi (regression).** Düzeltmenin veya yeni özelliğin **başka bir yeri bozup bozmadığının** kanıtı. Kapsamı, düzeltilen hatanın adımları değil, etkilenmiş olabilecek alanlardır. **Mülakat tuzağı:** retest ile regresyonu karıştırmak. Retest "hata gitti mi?", regresyon "başka bir şeyi kırdık mı?" sorusunu sorar; ikisi birbirinin yerine geçmez.
5. **Keşifsel test (exploratory).** Önceden yazılmış senaryo olmadan, öğrenme-tasarım-yürütmenin eşzamanlı yapıldığı, zaman kutulu (time-boxed) test. Yazılı senaryoların göremediği hataları bulur; "rastgele tıklamak" değildir, bir görev tanımı (charter) ve not tutma disiplini vardır.
6. **Kabul testi (acceptance).** İşin sahibinin "bunu kabul ediyorum" dediği testtir.

**3. Test süreci: altı adım.**

1. **Planlama.** Kapsam, riskler, ortam, roller, **giriş ve çıkış kriterleri**, zaman ve araç kararları. Çıktısı test planıdır.
2. **Analiz.** "Neyi test edeceğiz?" Gereksinimler ve kabul kriterleri incelenir, test edilebilirlik sorgulanır, test koşulları çıkarılır. Bu adımda bulunan gereksinim belirsizliği, en ucuz hatadır.
3. **Tasarım.** "Nasıl test edeceğiz?" Test senaryoları (test case) ve test verileri tasarım teknikleriyle üretilir.
4. **Gerçekleme (implementation).** Senaryolar koşulabilir hâle getirilir: veri hazırlanır, ortam kurulur, otomasyon kodu yazılır, koşum sırası belirlenir.
5. **Yürütme (execution).** Testler koşulur, sonuçlar (geçti/kaldı/bloke) kaydedilir, sapmalar hata kaydına dönüşür, gerekirse yeniden test yapılır.
6. **Kapanış (closure).** Çıkış kriterleri kontrol edilir, açık hatalar ve riskler raporlanır, artefaktlar arşivlenir, çıkarılan dersler (retrospektif) yazılır.

**4. Test senaryosu (test case) anatomisi.** İyi bir senaryo, onu yazan kişi izinde olmadan da aynı sonucu üretebilmelidir:

```
Kimlik      : TC-KUPON-014 · Kupon alt limitinde indirim uygulanır
Ön koşul    : Üye girişi yapılmış · sepet boş · KUPON250 kampanyası aktif
Test verisi : Ürün A birim fiyat 250,00 TL · adet 1
Adımlar     : 1) Ürün A'yı sepete ekle
              2) Kupon alanına KUPON250 yaz
              3) "Uygula" düğmesine bas
Beklenen    : Kupon kabul edilir · "%10 indirim" satırı görünür · genel toplam 225,00 TL
Gerçekleşen : (yürütme sırasında doldurulur)
Sonuç       : Geçti / Kaldı / Bloke
```

Dikkat: **beklenen sonuç, senaryo koşulmadan ÖNCE yazılır.** Yürütme sırasında "ekranda ne çıktıysa o beklenendir" demek, testin bütün anlamını yok eder.

**5. Test tasarım teknikleri.** Bütün girdileri denemek (tükenmeli test, exhaustive testing) imkânsızdır; teknikler, az sayıda veriyle çok sayıda riski kapsamayı sağlar.

1. **Denklik sınıfları (equivalence partitioning).** Aynı davranışı üreteceği varsayılan girdileri bir sınıfa toplar ve her sınıftan **bir** temsilci seçer. Kupon 250–1000 TL arasında geçerliyse üç sınıf vardır: 250 altı (geçersiz), 250–1000 (geçerli), 1000 üstü (geçersiz). 300 ile 700'ü ayrı ayrı denemek yeni bilgi vermez.
2. **Sınır değer analizi (boundary value analysis).** Hatalar sınıfların ortasında değil **kenarlarında** yaşar, çünkü `<` yerine `<=` yazmak en sık yapılan hatadır. Her sınırın iki yanı denenir: 249 / 250 ve 1000 / 1001. Stokta da aynısı geçerlidir: **0, 1** ve son adedi sepete ekleme anı.
3. **Karar tablosu (decision table).** Birden çok koşulun birleşimi kural üretiyorsa kullanılır. Örnek: "üye mi?" × "kupon geçerli mi?" × "kargo bedava eşiği aşıldı mı?" — 3 koşul, 8 birleşim, her birinin beklenen sonucu tabloya yazılır ve unutulan birleşim görünür hâle gelir.
4. **Durum geçişi (state transition).** Nesnenin bir durumu ve izinli geçişleri varsa kullanılır. Sipariş: `Oluşturuldu → Ödendi → Hazırlanıyor → Kargoda → Teslim`. Asıl değerli testler **izinsiz geçişlerdir**: "Teslim edilmiş siparişi iptal edebiliyor muyum?"

```svg
<svg viewBox="0 0 660 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kupon tutar sınırı için denklik sınıfları ve sınır değerler">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Denklik sınıfları + sınır değerler — kupon: sepet 250–1000 TL</text>
  <rect x="40" y="42" width="176" height="46" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="128" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Geçersiz sınıf</text>
  <text x="128" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tutar &lt; 250</text>
  <rect x="228" y="42" width="204" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Geçerli sınıf</text>
  <text x="330" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">250 ≤ tutar ≤ 1000</text>
  <rect x="444" y="42" width="176" height="46" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="532" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Geçersiz sınıf</text>
  <text x="532" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tutar &gt; 1000</text>
  <line x1="40" y1="126" x2="620" y2="126" stroke="#5D7789" stroke-width="1.6"/>
  <line x1="222" y1="112" x2="222" y2="140" stroke="#B07D20" stroke-width="2"/>
  <line x1="438" y1="112" x2="438" y2="140" stroke="#B07D20" stroke-width="2"/>
  <circle cx="204" cy="126" r="5.5" fill="#B23B3B"/>
  <circle cx="240" cy="126" r="5.5" fill="#25804C"/>
  <circle cx="420" cy="126" r="5.5" fill="#25804C"/>
  <circle cx="456" cy="126" r="5.5" fill="#B23B3B"/>
  <text x="204" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">249</text>
  <text x="240" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">250</text>
  <text x="420" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">1000</text>
  <text x="456" y="158" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">1001</text>
  <text x="330" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hata sınıfın ortasında değil kenarında yaşar: `&lt;` yerine `&lt;=` yazmak en sık yapılan hatadır.</text>
</svg>
```

**6. Shift-left ve QA'nın sprint içindeki yeri.** **Shift-left**, test faaliyetlerini yaşam döngüsünün **soluna**, yani erken aşamalarına taşımak demektir: gereksinimi gözden geçirmek, kabul kriterini geliştiriciyle birlikte yazmak, tasarımı sorgulamak. Gerekçesi ekonomiktir — analiz aşamasında bulunan bir eksik kural bir cümlelik düzeltmedir; aynı eksik canlıda bulunursa hata kaydı, acil sürüm, müşteri şikâyeti ve muhtemelen iade maliyeti üretir. Scrum'da bunun pratik adı **üç amigo (three amigos)** toplantısıdır: iş analisti (ne istiyoruz), geliştirici (nasıl yapacağız) ve QA (nasıl yanlış gidebilir) bir story'yi başlamadan önce birlikte konuşur. QA'nın sprint içindeki yeri "sprint sonunda kalan üç gün" değil, sprintin ilk gününden itibaren süren bir katılımdır. Shift-left'in tamamlayıcısı **shift-right**'tır: canlıda izleme, kademeli açılış (canary), gerçek kullanıcı geri bildirimi. İkisi rakip değil, aynı zincirin iki ucudur.

### Mülakatta ne sorulur / tuzaklar

- **"QA ile testin farkı nedir?"** — En sık sorulan giriş sorusu. "Aynı şey" demek elenme sebebidir. Beklenen: QA süreç odaklı ve önleyici, QC ürün odaklı ve tespit edici, test ise QC'nin araçlarından biri.
- **"Doğrulama (verification) ile geçerleme (validation) farkı?"** — Beklenen kısa cevap: "ürünü doğru mu yapıyoruz" / "doğru ürünü mü yapıyoruz". Somutlaştır: şartnameye birebir uyan ama kullanıcının işine yaramayan bir kupon ekranı doğrulamayı geçer, geçerlemeyi geçemez.
- **"Yeniden test (retest) ile regresyon testinin farkı?"** — Klasik tuzak. Retest hatanın gittiğini kanıtlar (aynı adımlar, aynı veri); regresyon düzeltmenin başka yeri bozmadığını kanıtlar (farklı, etkilenmiş alanlar).
- **"Duman (smoke) testi ile akıl sağlığı (sanity) testi farkı?"** — Duman geniş ve sığ, sürümün teste kabul edilip edilmeyeceğini belirler (giriş kriteri); akıl sağlığı dar ve derin, değişen alana odaklanır.
- **"%100 test kapsamı hatasızlık demek mi?"** — Hayır. Kapsam yalnız kodun **çalıştırıldığını** ölçer; assertion'ın anlamlılığını, eksik gereksinimi, fonksiyonel olmayan riskleri ölçmez. Assertion'sız bir test bile kapsamı yükseltir. "Evet, %100 kapsam güvenlidir" demek elenme sebebidir.
- **"Gözden geçirme (review) test midir?"** — Evet, statik testtir ve en ucuz hata bulma yöntemidir; kod hiç çalıştırılmadan hata bulur.
- **"Bir sürümü ne zaman 'test edildi' sayarsın?"** — Beklenen: önceden yazılı **çıkış kriterlerine** göre — "içime sindi" değil. Kriter örnekleri: planlanan senaryoların koşulma oranı, açık kritik/yüksek şiddetli hata sayısı sıfır, regresyon paketi yeşil, bilinen kalan riskler yazılı olarak kabul edilmiş.
- **"Hiç test edilmemiş bir özelliği yarın canlıya çıkarmamız gerekiyor, ne yaparsın?"** — Beklenen cevap "olmaz" değil, **risk temelli** bir cevaptır. Üç adım söylenir. Önce en yüksek iş riski taşıyan akışları önceliklendirip zaman kutulu keşifsel test yaparsınız; bu akışlar ödeme, sepet toplamı ve stoktur. Sonra kalan riski yazılı olarak karar vericiye bildirirsiniz. Son olarak geri alma (rollback) planını doğrularsınız.
- **Terminoloji tuzağı:** seviye ile türü karıştırmak. "Regresyon testi bir seviye midir?" — Hayır, türdür; her seviyede koşulabilir.

### Tek cümlede

QA, süreci iyileştirerek hatayı **önlemeye** çalışır; QC ise ürünü denetleyerek hatayı **bulmaya** çalışır ve test QC'nin aracıdır. Seviyeler (birim → entegrasyon → sistem → kabul) testin *neyi* sınadığını belirler; türler (duman, akıl sağlığı, yeniden test, regresyon, keşifsel) ise testin *niçin* koşulduğunu belirler. Süreç planlama ile başlar ve yazılı giriş/çıkış kriterleriyle kapanır. Değişmeyen kural şudur: hata ne kadar geç bulunursa o kadar pahalıya mal olur.

# qasurec-1 | Temel
## Soru
Bir e-ticaret ekibinde "kalite güvence (QA)", "kalite kontrol (QC)" ve "test" kavramlarının ilişkisi en doğru nasıl tarif edilir?
## A
QA ve QC aynı şeyin iki farklı adıdır; test ise bu ikisinin dışında, sürümden hemen önce yapılan bağımsız bir aşamadır
🤖 QA ile QC farklı odaklara sahiptir (süreç/ürün) ve test, dışarıda ayrı bir aşama değil QC'nin bir aracıdır.
## B
QA ürünün son hâlindeki hataları bulmayı, QC ise bulunan bu hataları düzeltmeyi hedefler
🤖 hata bulmak QC'nin işidir, düzeltmek ise geliştirmenin; QA zaten ürünü değil süreci hedefler.
## C
QA süreç odaklı ve önleyicidir, QC ürün odaklı ve tespit edicidir, test ise QC'nin araçlarından biridir
🤖 Doğru: QA hatayı üretmeyi zorlaştıran süreci kurar, QC çıkan ürünü denetler, test bu denetimin araçlarından biridir.
## D
QA otomasyon yazan kişinin, QC ise manuel test yapan kişinin unvanıdır; ikisi de aynı işi farklı araçlarla yapar
🤖 bunlar kişi unvanı değil faaliyet türüdür; otomasyon/manuel ayrımı QA-QC ayrımıyla ilgisizdir.
## Doğru
C
## Açıklama
Kalite güvence (QA) **süreç odaklı ve önleyicidir**: kod gözden geçirme kuralı, kabul kriteri şablonu, CI kapıları gibi düzenlemelerle hatalı ürün üretmeyi zorlaştırır. Kalite kontrol (QC) **ürün odaklı ve tespit edicidir**: çıkan sürümü inceleyip bulgu üretir. Test ise QC'nin araçlarından **biridir** — gözden geçirme (review), statik analiz ve denetim de QC kapsamına girer. A şıkkı QA ile QC'yi eşitler ve testi süreçten kopuk bir aşamaya indirger; B şıkkı ikisinin odaklarını tersine çevirir ve ayrıca hata düzeltmeyi QC'ye yükler; D şıkkı ise faaliyet türlerini kişi unvanına çevirir, oysa aynı kişi hem QA hem QC faaliyeti yürütebilir ve otomasyon/manuel ayrımı bu sınıflandırmayla ilgisizdir.

# qasurec-2 | Orta
## Soru
Bir ekip kupon ekranını şartnamedeki her maddeye birebir uygun geliştirdi; yazılı bütün kabul kriterleri karşılandı, sistem testleri de yeşil. Kampanya günü müşteriler "kuponu kargoya da uygulayabileceğimizi sanıyorduk, bu bizim istediğimiz indirim değil" diye şikâyet etti. Bu durum hangi ayrımı gösterir?
## A
Doğrulama (verification) başarılı, geçerleme (validation) başarısız: ürün doğru yapıldı ama doğru ürün yapılmadı
🤖 Doğru: çıktı kendi şartnamesine uygundur (doğrulama), fakat şartnamenin kendisi gerçek ihtiyacı karşılamamıştır (geçerleme).
## B
Geçerleme (validation) başarılı, doğrulama (verification) başarısız: ihtiyaç doğru anlaşılmış ama kod şartnameye uymamış
🤖 kod şartnameye birebir uyuyor, dolayısıyla başarısız olan taraf doğrulama değil geçerlemedir.
## C
Doğrulama ile geçerleme aynı faaliyetin iki adıdır; buradaki fark yalnızca terminolojiktir
🤖 ikisi farklı soruları yanıtlar — "ürünü doğru mu yapıyoruz" ile "doğru ürünü mü yapıyoruz" aynı soru değildir.
## D
Bütün kabul kriterleri geçtiği için burada bir test sorunu yoktur; sorumluluk tamamen şartnameyi yazan iş biriminindir
🤖 gereksinimi gözden geçirmek (statik test) QA'nın işidir; eksik kural analiz aşamasında sorgulanmalıydı.
## Doğru
A
## Açıklama
**Doğrulama (verification)** "ürünü doğru mu yapıyoruz?" sorusunu sorar ve çıktının kendi şartnamesine/tasarımına uygunluğunu sınar; birim, entegrasyon ve sistem testleri ağırlıklı olarak budur. **Geçerleme (validation)** ise "doğru ürünü mü yapıyoruz?" sorusunu sorar ve şartnamenin gerçek kullanıcı ihtiyacını karşılayıp karşılamadığına bakar; kabul testi (UAT) ve gerçek kullanıcı geri bildirimi buraya girer. Örnekte kod şartnameye uygundur, yani doğrulama başarılıdır — bu yüzden B tersine çevrilmiş bir okumadır. C ikisini eşitler, oysa şartname yanlışsa bütün doğrulama testleri yeşil yanarken ürün yine de başarısız olur; tam olarak burada yaşanan da budur. D ise sorumluluğu tamamen dışarı atar: gereksinimi gözden geçirmek statik bir test faaliyetidir ve shift-left yaklaşımında QA'nın asıl katkısı, bu eksik kuralı kod yazılmadan önce sormaktır.

# qasurec-3 | Orta
## Soru
"Sepette kupon uygulanınca genel toplam güncellenmiyor" hatası düzeltildi ve yeni sürüm test ortamına geldi. Yeniden test (retest) ile regresyon testi arasındaki fark nedir?
## A
Retest, düzeltmeden etkilenmeyen alanların hâlâ çalıştığını; regresyon ise düzeltilen hatanın gerçekten gittiğini kontrol eder
🤖 tanımlar yer değiştirilmiş; hatanın gittiğini kanıtlayan retest, çevreyi kontrol eden regresyondur.
## B
İkisi de aynı testin iki adıdır; aradaki tek fark testin sürümden önce mi sonra mı koşulduğudur
🤖 farkları koşum zamanı değil kapsam ve amaçtır — biri hatanın kendisine, diğeri çevresine bakar.
## C
Retest her zaman otomatik, regresyon ise her zaman manuel koşulan bir test türüdür
🤖 otomatik/manuel ayrımı bir yürütme tercihidir, bu iki türü tanımlayan ölçüt değildir.
## D
Retest hata kaydındaki adımları aynı veriyle tekrar koşup hatanın gittiğini kanıtlar; regresyon düzeltmenin başka alanları bozup bozmadığını sınar
🤖 Doğru: retest hatanın kendisine odaklanır, regresyon düzeltmenin yan etkisini arar.
## Doğru
D
## Açıklama
**Yeniden test (retest / confirmation testing)**, hata kaydında yazılı **aynı adımların aynı veriyle** tekrar koşulmasıdır ve tek bir soruyu yanıtlar: hata gitti mi? **Regresyon testi** ise tamamen farklı bir soruyu yanıtlar: bu düzeltme veya yeni özellik, daha önce çalışan başka bir yeri bozdu mu? Kupon hesaplaması düzeltilirken kargo bedava eşiği veya taksit tutarı hesabı bozulmuş olabilir; bunu yakalayan regresyondur. A şıkkı iki tanımı birebir yer değiştirmiştir ve mülakatta en sık düşülen tuzaktır. B, farkı koşum zamanına indirger; oysa ayrım kapsam ve amaçtadır. C ise otomatik/manuel yürütme tercihini tanım sanır — regresyon paketi genellikle otomatikleştirilir, retest çoğu zaman elle yapılır, yani iddia pratikte de terstir.

# qasurec-4 | İleri
## Soru
Bir kupon yalnızca sepet tutarı 250 TL ile 1000 TL arasındayken (uç değerler dahil) geçerlidir. Sınır değer analizi (boundary value analysis) ile mümkün olan en az test verisiyle sınırlardaki hataları yakalamak istiyorsun. Hangi veri kümesi bu tekniğe uygundur?
## A
100, 500 ve 2000 — her denklik sınıfından birer temsilci
🤖 bu denklik sınıfı temsilcisidir, hiçbir sınıra dokunmadığı için `<` / `<=` türü sınır hatalarını yakalamaz.
## B
249, 250, 1000 ve 1001 — her sınırın hemen iki yanı
🤖 Doğru: hata sınıfların ortasında değil kenarlarında yaşar, sınırın iki yanını denemek bunları açığa çıkarır.
## C
250'den 1000'e kadar 10 TL'lik adımlarla üretilen bütün değerler
🤖 bu tükenmeli teste yaklaşan bir yaklaşımdır; maliyeti yüksek, ek bilgi getirisi neredeyse sıfırdır.
## D
Yalnız 500 — geçerli sınıfın ortasındaki tek bir temsilci yeterlidir
🤖 tek temsilci geçerli sınıfın çalıştığını gösterir ama sınır hatalarını ve geçersiz sınıfları hiç sınamaz.
## Doğru
B
## Açıklama
**Denklik sınıfları (equivalence partitioning)** girdileri aynı davranışı üreteceği varsayılan kümelere ayırır ve her kümeden bir temsilci seçer; **sınır değer analizi (boundary value analysis)** ise bu kümelerin **kenarlarına** odaklanır, çünkü en sık yapılan kodlama hatası `<` yerine `<=` (veya tersi) yazmaktır ve bu hata yalnızca sınırda görünür. 250 ve 1000 sınırlarının hemen iki yanını — 249/250 ve 1000/1001 — denemek bu hatayı açığa çıkarır. A şıkkı yalnızca denklik sınıfı temsilcileri seçer, hiçbiri sınıra değmediği için sınır hatasını göremez. C, aradaki bütün değerleri denediği için pahalıdır ve aynı denklik sınıfı içindeki ek değerler yeni bilgi üretmez. D ise hem sınırları hem geçersiz sınıfları tamamen atlar; "kupon 100 TL'lik sepette de uygulanıyor" gibi bir hata bu kümeyle asla yakalanamaz.

# qasurec-5 | İleri
## Soru
Bir ekip, "birim test satır kapsamımız (line coverage) %100" gerekçesiyle sürüm öncesi regresyon paketini tamamen kaldırmayı öneriyor. Kapsam kavramı açısından en doğru değerlendirme nedir?
## A
Öneri doğrudur: %100 satır kapsamı, kodda çalıştırılmamış hiçbir yol kalmadığını ve dolayısıyla hata bulunmadığını kanıtlar
🤖 satır kapsamı yalnız satırın çalıştırıldığını gösterir, doğru sonucun doğrulandığını değil.
## B
Kapsam ölçümü hiçbir bilgi taşımaz; bu yüzden hiç ölçülmemeli ve kararlarda kullanılmamalıdır
🤖 kapsam faydalı bir sinyaldir — özellikle hiç dokunulmayan alanları gösterir; sorun tek başına yeterli sayılmasıdır.
## C
%100 satır kapsamı yalnız satırların çalıştırıldığını gösterir; assertion'ın anlamlılığını, eksik gereksinimi ve entegrasyon/sistem seviyesindeki riskleri kapsamaz
🤖 Doğru: kapsam bir çalıştırılma ölçüsüdür, doğrulanma ölçüsü değildir; regresyon paketi farklı bir riski karşılar.
## D
Kapsam yalnız sistem testi seviyesinde anlamlıdır; birim seviyesinde ölçülen kapsam değerleri yanıltıcı sayıldığı için dikkate alınmaz
🤖 kapsam en yaygın olarak birim seviyesinde ölçülür; sorun seviyenin kendisi değil, ölçünün yanlış yorumlanmasıdır.
## Doğru
C
## Açıklama
Kapsam (coverage), testlerin kodun ne kadarını **çalıştırdığını** ölçer — o kodun **doğru davrandığının doğrulandığını** ölçmez. Hiç `assert` içermeyen bir test bile satır kapsamını yükseltir. Ayrıca %100 satır kapsamı, hiç yazılmamış bir gereksinimi (eksik kural), dal (branch) birleşimlerini, servisler arası entegrasyon hatalarını ve performans/güvenlik gibi fonksiyonel olmayan riskleri kapsamaz — regresyon paketi tam olarak bu farklı riskleri karşılar, bu yüzden kaldırılamaz. A şıkkı "çalıştırıldı" ile "doğrulandı"yı eşitleyerek mülakattaki en klasik hatayı yapar. B ters yöne savrulur: kapsam, özellikle hiç dokunulmamış alanları ortaya çıkardığı için faydalı bir sinyaldir, sorun onu tek başına kalite kanıtı saymaktır. D ise ölçüm seviyesini yanlış konumlandırır; kapsam en çok birim seviyesinde ölçülür, mesele seviye değil yorumdur.

# qasurec-6 | İleri
## Soru
Kampanya sürümü öncesi QA, "sprintin son üç gününde test ederiz" düzeni yerine **shift-left** yaklaşımını öneriyor. Shift-left'in doğru tanımı ve gerekçesi hangisidir?
## A
Test faaliyetlerini (gereksinim gözden geçirme, kabul kriteri yazımı, statik inceleme) yaşam döngüsünün erken aşamalarına taşımaktır; gerekçesi hatanın geç bulundukça maliyetinin artmasıdır
🤖 Doğru: erken bulunan eksik kural bir cümlelik düzeltmedir, canlıda bulunan aynı eksik acil sürüm ve müşteri maliyeti üretir.
## B
Testleri üretim ortamına taşıyıp gerçek kullanıcı davranışını izleyerek doğrulamaktır; gerekçesi test ortamlarının gerçeği yansıtmamasıdır
🤖 bu shift-right'ın (canlıda izleme, kademeli açılış) tanımıdır, shift-left'in değil.
## C
Manuel testleri tamamen kaldırıp yerlerine uçtan uca otomasyon koymaktır; gerekçesi otomasyonun daha hızlı koşmasıdır
🤖 shift-left testin ne zaman yapıldığıyla ilgilidir, manuel/otomatik tercihiyle değil.
## D
Test seviyelerini birleştirip birim ve entegrasyon testlerini kaldırmak, her şeyi tek bir uçtan uca test setinde doğrulamaktır
🤖 bu, hatayı en pahalı seviyeye ötelemek demektir — shift-left'in tam tersidir.
## Doğru
A
## Açıklama
**Shift-left**, test faaliyetlerini yaşam döngüsünün soluna, yani erken aşamalara taşımaktır: gereksinimi gözden geçirmek, kabul kriterini geliştiriciyle birlikte yazmak, tasarımı test edilebilirlik açısından sorgulamak. Scrum'daki pratik karşılığı **üç amigo (three amigos)** toplantısıdır — iş analisti, geliştirici ve QA bir story'ye başlamadan önce "ne istiyoruz / nasıl yapacağız / nasıl yanlış gidebilir" sorularını birlikte cevaplar. Gerekçe ekonomiktir: analizde bulunan eksik bir kupon kuralı bir cümlelik düzeltmeyken, aynı eksik canlıda bulunursa hata kaydı, acil sürüm ve müşteri kaybı maliyeti üretir. B şıkkı **shift-right**'ı tarif eder; o da değerlidir ama shift-left'in rakibi değil tamamlayıcısıdır. C, shift-left'i bir araç tercihine indirger — oysa mesele testin *ne zaman* yapıldığıdır, manuel mi otomatik mi olduğu değil. D ise birim ve entegrasyon seviyelerini kaldırarak hatanın yakalanmasını en geç ve en pahalı noktaya öteler, yani tam olarak shift-left'in engellemeye çalıştığı şeyi yapar.
