---
slug: test-odakli-gelistirme-tdd
baslik: "Test Odaklı Geliştirme (TDD): Kırmızı-Yeşil-Yeniden Düzenle, Tasarım Baskısı ve İki Okul"
kademe: Orta
sira: 261
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Markete listesiz gitmeyi düşün. Sepet dolar, kasada para gider, eve gelince yine eksik çıkar.

Liste önce yazılırsa iş tersine döner. Ne alacağın baştan bellidir. Sepet, listeyi karşılamak için doldurulur.

**Test odaklı geliştirme (Test-Driven Development, TDD)** tam olarak budur. Önce testi yazarsın. Test, kodun ne yapması gerektiğini söyleyen listedir. Kodu sonra, o listeyi karşılamak için yazarsın.

Asıl fikir çoğu adayın kaçırdığı yerdedir. TDD bir test yazma tekniği değildir. Bir **tasarım yöntemidir**. Testler yan çıktıdır; asıl kazanç kodu yazarken aldığın geri bildirimdir.

Bu konu neden ayrı duruyor? Çünkü iş ilanları TDD'yi bir sorumluluk olarak yazıyor. Tipik cümle şudur: "Test odaklı geliştirme süreçlerini takip etmek ve test yazmak". Senden araç bilgisi değil, bir **çalışma düzeni** bekleniyor.

Sınırı baştan çizelim. Test piramidi, sahte nesne türleri ve sözleşme testi 187 numaralı konudadır. JUnit, Mockito, Testcontainers ve mutasyon testi 188 numaradadır. Spring dilim testleri 90 numaradadır. Test izolasyonu ve paralel koşum 203 numaradadır. Burada yalnız TDD'nin kendisi anlatılır.

### Temel kavramlar

- **TDD (Test-Driven Development, test odaklı geliştirme):** testi koddan önce yazan geliştirme döngüsü.
- **Kırmızı (red):** henüz geçmeyen test. İstenen davranışı tanımlar.
- **Yeşil (green):** o testi geçiren en yalın kod.
- **Yeniden düzenleme (refactoring):** davranışı değiştirmeden iç yapıyı iyileştirmek (132).
- **Test-önce (test-first):** test koddan önce yazılır.
- **Test-sonra (test-after):** kod bitince test eklenir. Yaygındır, ama TDD değildir.
- **Tasarım baskısı (design pressure):** testin, kodu daha ayrılabilir yazmaya zorlaması.
- **Durum doğrulaması (state verification):** çağrının sonucuna bakmak.
- **Davranış doğrulaması (behavior verification):** hangi metodun kaç kez çağrıldığına bakmak.
- **Klasik okul (classicist, Chicago):** gerçek nesnelerle çalışır ve durumu doğrular.
- **Taklitçi okul (mockist, London):** işbirlikçileri taklitle değiştirir ve davranışı doğrular.
- **Aşırı taklit (over-mocking):** gereğinden çok taklit nesne kullanmak.
- **Keşif kodu (spike):** öğrenmek için yazılan, sonra atılan geçici kod.
- **Karakterizasyon testi (characterization test):** mevcut davranışı olduğu gibi donduran test (132).
- **Dikiş (seam):** kodu değiştirmeden davranışını dışarıdan değiştirebildiğin nokta.
- **Eski kod (legacy code):** Michael Feathers'ın tanımıyla, testi olmayan kod.

### Döngü: kırmızı, yeşil, yeniden düzenle

Döngünün kısa tanımı 187 numaralı konuda verildi. Burada her adımda **ne yapılmadığı** anlatılır. Mülakatta ayırt edici olan kısım orasıdır.

**Birinci adım: kırmızı.** Tek bir davranış için başarısız bir test yazarsın. Sonra testi çalıştırır ve gerçekten kırmızı yandığını **görürsün**. Bu bakış boşuna değildir; hiç kırmızı olmamış bir test, aslında hiçbir şeyi ölçmüyor olabilir. Sınıf henüz yoksa derleme hatası da kırmızı sayılır.

Bu adımda yapılmayanlar şunlardır. Aynı anda beş test yazılmaz; döngü tek davranışa odaklıdır. Yeşil yanacağını bildiğin bir test yazılmaz. "Şimdilik biraz kod yazayım, testi sonra yazarım" denmez; o an döngüden çıkmış olursun.

**İkinci adım: yeşil.** Testi geçiren **en yalın** kodu yazarsın. Amaç güzellik değil, döngüyü yeşile döndürmektir. Bu adımda kod tekrarı serbesttir; temizlik üçüncü adımın işidir.

Bu adımda yapılmayanlar şunlardır. Hiçbir testin istemediği özellik eklenmez. Erken genelleme yapılmaz; "ileride lazım olur" diye soyutlama açılmaz. Hız ayarı da burada yapılmaz.

**Üçüncü adım: yeniden düzenle.** Testler yeşilken tasarımı temizlersin. Tekrarı kaldırırsın, isimleri düzeltirsin, uzun metodu bölersin (132). Küçük adımlarla ilerlersin ve her adımdan sonra testleri koşarsın.

Bu adımda yapılmayanlar şunlardır. Yeni davranış eklenmez; davranış eklemek için önce kırmızı test gerekir. Test kırmızıyken düzenleme yapılmaz. Ve testin beklentisi, kodu kurtarmak için değiştirilmez.

**En sık atlanan adım üçüncüsüdür.** Sebebi insanidir. Test yeşil yanmıştır, iş "bitmiş" görünür, panoda sıradaki kart beklemektedir. Kimse yeşil bir koda dokunmak istemez.

Sonucu ise ağırdır. Test paketi birikir, ama tasarım bozulmaya devam eder. Elinde bir güvenlik ağı olur; buna karşılık düzeltmesi giderek zorlaşan bir kod da olur. TDD'yi değersizleştiren şey tam olarak budur. Üçüncü adım atlanırsa geriye kalan şey, tören eklenmiş bir test yazma alışkanlığıdır.

Son bir ölçü: **adım büyüklüğü**. Bilindik bir işte adım büyük olabilir. Belirsiz bir işte adım küçültülür. Döngünün tamamı dakikalarla ölçülmelidir.

```svg
<svg viewBox="0 0 680 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="TDD dongusunde her adimda ne yapilir ve ne yapilmaz">
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Döngünün üç adımı: ne yapılır, ne YAPILMAZ</text>
  <text x="92" y="48" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">adım</text>
  <text x="299" y="48" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">yapılır</text>
  <text x="547" y="48" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">yapılmaz</text>
  <rect x="14" y="58" width="156" height="66" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="92" y="84" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">1. Kırmızı</text>
  <text x="92" y="103" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">davranışı tanımla</text>
  <rect x="178" y="58" width="242" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="299" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tek davranış için başarısız test</text>
  <text x="299" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kırmızı yandığını gözünle gör</text>
  <rect x="428" y="58" width="238" height="66" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="547" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">aynı anda beş test yazmak</text>
  <text x="547" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hemen yeşil yanan test yazmak</text>
  <rect x="14" y="132" width="156" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="92" y="158" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">2. Yeşil</text>
  <text x="92" y="177" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">en yalın kodu yaz</text>
  <rect x="178" y="132" width="242" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="299" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">testi geçiren en kısa yol</text>
  <text x="299" y="176" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tekrar etmek burada serbest</text>
  <rect x="428" y="132" width="238" height="66" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="547" y="156" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">testin istemediği özellik eklemek</text>
  <text x="547" y="176" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">erken genelleme, erken hız ayarı</text>
  <rect x="14" y="206" width="156" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="92" y="232" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">3. Yeniden düzenle</text>
  <text x="92" y="251" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tasarımı temizle</text>
  <rect x="178" y="206" width="242" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="299" y="230" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tekrarı kaldır, isimleri düzelt</text>
  <text x="299" y="250" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">testler yeşilken, küçük adımlarla</text>
  <rect x="428" y="206" width="238" height="66" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="547" y="230" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yeni davranış eklemek</text>
  <text x="547" y="250" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kodu kurtarmak için beklenti değiştirmek</text>
  <rect x="14" y="282" width="652" height="42" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="340" y="300" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">En sık atlanan adım üçüncüsüdür: yeşil yanınca sıradaki karta geçilir.</text>
  <text x="340" y="317" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Sonuç: test paketi birikir, tasarım bozulmaya devam eder — TDD değersizleşir.</text>
</svg>
```

### Neden "önce test" farklı bir şeydir?

Şimdi konunun kalbine geliyoruz. Aynı testleri kod bittikten sonra da yazabilirsin. Peki fark nerede?

Fark, testin **ne zaman** yazıldığında değil, **neyi zorladığındadır**.

**Birinci fark: gizli bağımlılıklar ortaya çıkar.** Bir metot kendi içinde `new VeritabaniBaglantisi()` çağırıyorsa, o bağımlılığı dışarıdan değiştiremezsin. `LocalDate.now()` veya `static` bir yardımcı çağrısı da aynıdır. Test yazmaya kalkınca bu duvarlara hemen çarparsın.

Sonuç şudur: **test yazılamayan kod, bağımlılıkları gizlenmiş koddur.** Test-önce yazınca bunu ilk dakikada görürsün. Çözüm de bellidir: bağımlılığı dışarıdan ver. Bu, bağımlılıkları ters çevirme ilkesinin (DIP) günlük hayattaki karşılığıdır (131).

**İkinci fark: arayüzü çağıran gözünden tasarlarsın.** Testte yazdığın ilk satır, sınıfın nasıl çağrılacağıdır. Yani sınıfın ilk kullanıcısı sen olursun. Kurulumu yedi satır sürüyorsa, o sınıfın kullanımı zordur ve bunu **kodu yazmadan** öğrenirsin.

**Üçüncü fark: test-sonra, var olan tasarımı sorgulamaz.** Kod bittiğinde tasarım artık sabittir. Test yazmak zorlaşınca kimse tasarımı değiştirmez; onun yerine testi zorlar. Belirtileri tanıdıktır: her şeyin taklit edilmesi, yansıma (reflection) ile özel alanlara uzanmak, `static` çağrıyı taklit eden eklentiler kurmak.

**Dördüncü fark: kapsam kendiliğinden gelir.** Test-sonra yazarken kolay olan yerler test edilir; zor dallar atlanır. TDD'de ise her davranış zaten bir testten doğar. Yine de dikkat: yüksek kapsam bir yan çıktıdır, hedef değildir. Testin gerçekten ölçtüğünü mutasyon testi gösterir (188).

Şimdi dürüst sınırı da koyalım. TDD iyi bir **mimari** garanti etmez. Sana sınıf ve metot düzeyinde geri bildirim verir. Modül sınırları, veri sahipliği ve dağıtık tasarım ayrı bir iştir (131, 132).

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test sonra ile test once yaklasiminin tasarima etkisi">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı testler, farklı tasarım sonucu</text>
  <rect x="14" y="38" width="306" height="204" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="167" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Test-sonra: tasarımı doğrular</text>
  <text x="167" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1. kod yazılır, tasarım sabitlenir</text>
  <text x="167" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">2. test sonradan eklenmeye çalışılır</text>
  <text x="167" y="130" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">3. zorlanınca tasarım değil test zorlanır</text>
  <text x="167" y="156" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">gizli bağımlılık kalır: new, static, now()</text>
  <text x="167" y="178" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">yansıma ve aşırı taklit devreye girer</text>
  <text x="167" y="200" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">kolay dallar test edilir, zor dallar atlanır</text>
  <text x="167" y="228" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">sonuç: var olan tasarım onaylanır</text>
  <rect x="340" y="38" width="306" height="204" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="493" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Test-önce: tasarıma baskı yapar</text>
  <text x="493" y="86" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1. önce çağrı biçimi yazılır</text>
  <text x="493" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">2. sınıfın ilk kullanıcısı sen olursun</text>
  <text x="493" y="130" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">3. kullanışsız arayüz aynı dakikada görünür</text>
  <text x="493" y="156" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">bağımlılık dışarıdan verilir (131, DIP)</text>
  <text x="493" y="178" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">saat ve rastgelelik dışarı alınır</text>
  <text x="493" y="200" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">her davranış bir testten doğar</text>
  <text x="493" y="228" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">sonuç: tasarım sorgulanır</text>
  <rect x="14" y="252" width="632" height="36" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="330" y="275" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kural: test yazılamayan kod, bağımlılıkları gizlenmiş koddur.</text>
</svg>
```

### Döngünün hızı: hangi testle çalışırsın?

TDD'nin işlemesi tek bir şarta bağlıdır. Döngü **saniyeler** sürmelidir.

Sebebi basittir. Geri bildirim gecikirse akış kopar. Kod yazmayı bitirip testin bitmesini bekleyen geliştirici, bir süre sonra döngüyü hiç çalıştırmaz.

Bu yüzden test seviyesi seçimi TDD'nin doğrudan parçasıdır. Piramidin kendisi 187 numaralı konudadır; burada yalnız döngüye bakan yüzü var.

- **Birim testi:** milisaniyeler sürer. TDD'nin iç döngüsü budur.
- **Entegrasyon testi:** saniyeler sürer. Dış döngüde, daha seyrek koşulur.
- **Uçtan uca test:** dakikalar sürer. İç döngüde asla kullanılmaz.

Yaygın bir çalışma biçimi **iki halkalı döngüdür**. Dış halkada, iş isteğini anlatan daha büyük bir test durur ve uzun süre kırmızı kalır. İç halkada hızlı birim testleriyle küçük adımlar atılır. Dış halka en sonunda yeşile döner.

Java tarafında somut bir tuzak vardır. `@SpringBootTest` tüm uygulama bağlamını yükler ve saniyeler alır. Bunu iç döngüde her kayıtta çalıştıramazsın. Dilim testleri daha hızlıdır (90). En hızlısı ise Spring'e hiç dokunmayan düz birim testidir.

Buradan pratik bir tasarım kuralı çıkar. İş kuralını çerçeveden bağımsız, saf sınıflara çek. O sınıflar milisaniyede test edilir. Çerçeveye bağlı kısım ince kalır ve daha seyrek sınanır.

Döngü yavaşlıyorsa önce kök nedeni ara. Paralel koşum toplam süreyi kısaltır, ama iç döngüyü hızlandırmaz (203). Kararsız testler de döngüyü öldürür; kırmızının anlamı belirsizleşir (187).

### İki okul: klasik ve taklitçi

TDD'nin tek bir uygulaması yoktur. İki okul vardır ve mülakatta bu ayrım kıdem göstergesidir.

**Klasik okul** (classicist, Chicago) gerçek nesnelerle çalışır. Yalnız sınırdaki bağımlılıkları değiştirir: veritabanı, ağ çağrısı, saat, dosya sistemi. Doğrulama **durum** üzerindendir. Yani "sonuç ne oldu?" diye sorar. Bu okulda "birim" bir sınıf değil, bir davranıştır; bir davranış birkaç sınıfı birlikte kullanabilir.

**Taklitçi okul** (mockist, London) her işbirlikçiyi taklit nesneyle değiştirir. Sınanan sınıf tam olarak yalıtılır. Doğrulama **davranış** üzerindendir. Yani "hangi metot kaç kez, hangi argümanla çağrıldı?" diye sorar. Tasarım dıştan içe ilerler; henüz yazılmamış işbirlikçinin arayüzü test yazarken belirlenir.

Peki ne zaman hangisi? Ölçüt, doğrulanacak şeyin türüdür.

- **İş kuralı ve hesaplama çekirdeği** için klasik okul daha iyidir. Testler "ne yapıyor" der, "nasıl yapıyor" demez.
- **Yan etkili sınır** için davranış doğrulaması gerekir. E-posta göndermek, mesaj yayınlamak veya ödeme çekmek böyledir; bakılacak bir dönüş değeri yoktur.
- **Henüz var olmayan bir işbirlikçinin arayüzünü keşfetmek** için taklitçi yaklaşım işe yarar.

Şimdi asıl bedele gelelim. **Aşırı taklidin faturası üç kalemdir.**

Birincisi, test kodu üretim kodunun aynası olur. Her `verify` çağrısı bir gerçekleme ayrıntısını sabitler. Test artık davranışı değil, kodun içini anlatır.

İkincisi, yeniden düzenleme testleri kırar. İki metodu birleştirirsin, davranış hiç değişmez, yine de on beş test kırmızı yanar. Bu noktada test bir güvenlik ağı olmaktan çıkar ve prangaya döner. En kötü sonucu şudur: ekip yeniden düzenlemeyi bırakır.

Üçüncüsü, yanlış güven doğar. Taklit nesne senin **varsayımını** sınar. Gerçek işbirlikçinin o varsayımı karşıladığını sınamaz. Servisler arasında bunun cevabı sözleşme testidir (187).

Pratik bir ölçüt vardır. Kendine şunu sor: "Bu testleri bozmadan sınıfın içini değiştirebiliyor muyum?" Cevap hayırsa test, kod hakkında fazla şey biliyordur.

Yaygın orta yol da nettir. **Sınırda taklit, çekirdekte gerçek nesne.** Bellek içi çalışan basit bir sahte gerçekleme (fake), çoğu durumda taklit nesneden daha iyi bir seçimdir (187).

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Klasik Chicago okulu ile taklitci London okulunun karsilastirmasi">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki okul: klasik (Chicago) ve taklitçi (London)</text>
  <text x="100" y="48" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">ölçüt</text>
  <text x="308" y="48" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Klasik / Chicago</text>
  <text x="540" y="48" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Taklitçi / London</text>
  <rect x="14" y="58" width="172" height="36" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="100" y="81" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">neyi değiştirir</text>
  <rect x="194" y="58" width="228" height="36" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="308" y="81" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yalnız sınırı: veritabanı, ağ, saat</text>
  <rect x="430" y="58" width="216" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="538" y="81" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">her işbirlikçiyi taklit eder</text>
  <rect x="14" y="98" width="172" height="36" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="100" y="121" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">neyi doğrular</text>
  <rect x="194" y="98" width="228" height="36" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="308" y="121" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">durum: sonuç ne oldu</text>
  <rect x="430" y="98" width="216" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="538" y="121" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">davranış: kim kaç kez çağrıldı</text>
  <rect x="14" y="138" width="172" height="36" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="100" y="161" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">güçlü olduğu yer</text>
  <rect x="194" y="138" width="228" height="36" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="308" y="161" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">iş kuralı ve hesaplama çekirdeği</text>
  <rect x="430" y="138" width="216" height="36" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="538" y="161" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yan etkili sınır, arayüz keşfi</text>
  <rect x="14" y="178" width="172" height="36" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="100" y="201" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">riski</text>
  <rect x="194" y="178" width="228" height="36" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <text x="308" y="201" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kırılınca hata yeri geniş görünür</text>
  <rect x="430" y="178" width="216" height="36" rx="7" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="538" y="201" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">test üretim kodunun aynası olur</text>
  <rect x="14" y="224" width="632" height="30" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="330" y="244" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Aşırı taklidin bedeli: davranış değişmeden yapılan düzenleme testleri kırar.</text>
  <rect x="14" y="260" width="632" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="330" y="280" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Pratik orta yol: sınırda taklit, çekirdekte gerçek nesne.</text>
</svg>
```

### TDD ne zaman zarar verir?

"Her yerde TDD" cevabı mülakatta ezber izlenimi verir. Kıdemli aday sınırı da söyler.

**Keşif kodu (spike).** Yeni bir kütüphaneyi veya dış servisi tanımıyorsun. Henüz doğru cevabın ne olduğunu bilmiyorsun. Burada test yazmak, bilmediğin bir varsayımı dondurmaktır. Doğrusu şudur: hızlıca dene, öğren, kodu **at**. Sonra öğrendiğinle TDD döngüsüne dön.

**Kullanıcı arayüzü düzeni.** Buton nerede duracak, boşluk kaç piksel olacak? Bunlar teste dökülmez. Geri bildirim gözle alınır. Arayüzün altındaki durum mantığı ise pekâlâ TDD ile yazılabilir.

**Performans ayarı.** Burada hedef bir davranış değil, bir ölçümdür. Aracın profil çıkarma ve kıyaslama (benchmark) olması gerekir. "Şu çağrı 50 milisaniyeden hızlı olsun" testi genelde kararsız çıkar; makine yükü sonucu değiştirir.

**Şeması belirsiz entegrasyon.** Dış sistemin gerçek cevabını görmeden yazacağın test, yalnız senin tahminini kaydeder. Önce gerçek cevabı al, sonra onu sabitle.

**Ömrü kısa kod.** Tek kullanımlık betik veya atılacak prototip için TDD'nin bakım kazancı yoktur.

Bir de sık karşılaşılan zorluk var: kod zaten test edilebilir değilse. Eski koda TDD uygulamak mümkündür, ama önce bir hazırlık adımı gerekir. Sıradaki bölüm tam olarak onu anlatır.

### Eski koda TDD: önce dondur, sonra değiştir

Michael Feathers'ın tanımı nettir: **testi olmayan kod eski koddur**. Ölçü yaş değildir, güvenlik ağıdır. Dün yazılmış testsiz sınıf da eski koddur.

Burada bir kısır döngü vardır. Değiştirmek için teste ihtiyacın var. Test yazmak içinse kodu değiştirmen gerekiyor.

Çıkış yolu **karakterizasyon testidir** (132). Fikir şudur: kodun bugün ne yaptığını olduğu gibi kaydet. "Doğru mu?" diye sorma. Şaşırtıcı bir çıktı görsen bile onu beklenen değer olarak yaz. Çünkü birileri o davranışa bağımlı olabilir.

Pratik yolu da basittir. Beklenen değeri bilmiyorsan rastgele bir değer yaz. Test kırmızı yanar ve sana gerçek çıktıyı söyler. O çıktıyı beklenen değer olarak yerine koyarsın.

Sonraki adım **dikiş açmaktır**. Dikiş, kodu değiştirmeden davranışını dışarıdan değiştirebildiğin noktadır. Üç yaygın yolu vardır: bağımlılığı yapıcı metoda parametre olarak çıkarmak, gizli bir çağrıyı korumalı bir metoda almak, somut sınıfı arayüz arkasına koymak.

Dikiş açıldıktan sonra artık normal TDD döngüsüne girebilirsin. Yeni davranış için kırmızı test, sonra yeşil, sonra düzenleme.

Eski kodda TDD'ye **en kolay giriş hata düzeltmedir**. Sıra şudur: önce hatayı gösteren kırmızı bir test yaz. Sonra hatayı düzelt ve testi yeşile al. Bu tek alışkanlık iki iş birden yapar. Hatayı belgeler ve aynı hatanın geri dönmesini engeller.

### Mülakatta "TDD yapar mısınız?"

Bu soruya verilen cevap, teknik bilgiden çok olgunluk ölçer.

Önce yanlış cevapları görelim. **"Her zaman, istisnasız TDD yaparım."** Bu cevabın iki riski var. Birincisi, tek bir alt soruyla çöker: "Son ne zaman yeniden düzenleme adımını atladınız?" İkincisi, çoğu ekipte TDD kısmen uygulanır ve mülakatçı bunu bilir. Ezber izlenimi doğar.

**"TDD'yi denedik, yavaşlattı, bıraktık."** Bu cevap deneyim anlatmaz. Yalnız bir sonucu bildirir.

**"Ekibin süreci neyse onu uygularım."** Bu da sorumluluğu başkasına devreder.

**Uydurmak ise en kötüsüdür.** "Evet, TDD ile geliştirdik" dersen alt soru gelir: "Peki hangi noktada test yazmayı bıraktınız?" Cevap yoksa tüm görüşmenin güveni gider.

Güçlü cevabın üç parçası vardır.

**Bir: nerede yaptığını söyle.** Somut ol. Örneğin hata düzeltmede, iş kurallarında, sınır durumlarında ve saf hesaplamalarda.

**İki: nerede yapmadığını ve nedenini söyle.** Örneğin tanımadığın bir kütüphaneyle çalışırken önce keşif kodu yazarsın. Arayüz düzeninde ve şeması belirsiz entegrasyonda test-önce gitmezsin.

**Üç: neyi koruduğunu söyle.** Döngüyü hızlı tutmak ve yeniden düzenleme adımını atlamamak. Bu cümle senin TDD'yi tören olarak değil, araç olarak gördüğünü gösterir.

Örnek bir cevap şöyle kurulabilir. "Hata düzeltmede istisnasız test-önce giderim; önce hatayı gösteren kırmızı testi yazarım. Yeni iş kuralında da genelde test-önce çalışırım. Tanımadığım bir kütüphaneyle uğraşırken önce keşif kodu yazar, sonra atıp döngüye dönerim. En çok zorlandığım yer üçüncü adım; onu atlamamak için kendime kural koydum."

Bu cevap "her zaman yaparım" cümlesinden daha güçlüdür. Çünkü doğrulanabilir ve deneyim kokar.

### Mülakatta ne sorulur?

- **"TDD döngüsünün üçüncü adımı neden atlanır ve bedeli nedir?"** Beklenen: yeşil yanınca sıradaki işe geçilir; test birikir ama tasarım bozulmaya devam eder.
- **"Testi sonradan yazsam ne değişir?"** Beklenen: test-önce arayüzü çağıran gözünden tasarlatır ve gizli bağımlılıkları açığa çıkarır; test-sonra var olan tasarımı onaylar.
- **"TDD döngüsünde hangi testleri koşarsın?"** Beklenen: iç döngüde milisaniyelik birim testleri; `@SpringBootTest` ve uçtan uca testler dış döngüde (90, 187).
- **"Klasik ve taklitçi okul farkı nedir?"** Beklenen: klasik durumu doğrular ve gerçek nesne kullanır; taklitçi davranışı doğrular ve işbirlikçileri taklit eder.
- **"Aşırı taklidin bedeli nedir?"** Beklenen: test üretim kodunun aynası olur; davranış değişmeden yapılan düzenleme testleri kırar ve ekip düzenlemeyi bırakır.
- **"TDD'nin uygun olmadığı yer var mı?"** Beklenen: keşif kodu, arayüz düzeni, performans ayarı, şeması belirsiz entegrasyon. "Her yerde" cevabı ezber okunur.
- **"Testsiz eski koda nasıl başlarsın?"** Beklenen: karakterizasyon testiyle mevcut davranışı dondur, dikiş aç, sonra TDD döngüsüne gir (132).
- **"TDD yüksek kapsam garanti eder mi?"** Beklenen: kapsam yan çıktıdır, hedef değildir; testin gerçekten ölçtüğünü mutasyon testi gösterir (188).
- **"TDD yapar mısınız?"** Beklenen: nerede yaptığını, nerede yapmadığını ve nedenini anlat. Uydurma.

### Tek cümlede

TDD bir test tekniği değil, testi koddan önce yazarak tasarıma baskı uygulayan bir çalışma düzenidir. Döngü üç adımdır ve değeri en çok atlanan üçüncü adımdan gelir. İç döngü saniyeler sürmelidir; bu yüzden iş kuralı çerçeveden bağımsız ve hızlı test edilebilir olmalıdır. Doğrulamayı çekirdekte durum üzerinden, sınırda davranış üzerinden yaparsın; aşırı taklit testi prangaya çevirir. Eski kodda önce karakterizasyon testiyle davranışı dondurur, sonra dikiş açıp döngüye girersin. Mülakatta ise "her zaman yaparım" demek yerine nerede yaptığını ve nerede yapmadığını anlatmak daha güçlüdür.

# tdd-1 | Temel
## Soru
Bir ekip TDD'ye geçti. Kırmızı ve yeşil adımları uyguluyorlar, ama test yeşil yanar yanmaz sıradaki karta geçiyorlar; üçüncü adımı hiç yapmıyorlar. Bunun en doğru sonucu hangisidir?
## A
Döngü hızlanır; üçüncü adım isteğe bağlı bir süs olduğu için kazanç aynen korunur
🤖 üçüncü adım süs değildir; TDD'nin tasarım kazancının büyük kısmı oradan gelir.
## B
Testler zamanla kendiliğinden geçersizleşir ve yeşilken bile yanlış pozitif üretmeye başlar
🤖 testler kendiliğinden bozulmaz; sorun testlerde değil, temizlenmeyen tasarımdadır.
## C
Test paketi birikir ama tasarım bozulmaya devam eder; TDD tasarım faydasını kaybeder
🤖 Doğru: güvenlik ağı oluşur, buna karşılık kod giderek zor değiştirilir hale gelir.
## D
Satır kapsamı düşer, çünkü yeni testler yalnız üçüncü adımda ekleniyordu
🤖 testler kırmızı adımda yazılır; üçüncü adımda yeni test eklenmez.
## Doğru
C
## Açıklama
Üçüncü adım en sık atlanan adımdır ve sebebi insanidir. Test yeşil yanmıştır, iş bitmiş görünür, panoda sıradaki kart bekler. Kimse çalışan koda dokunmak istemez. Bedeli ise şudur: **test paketi birikir, tasarım bozulmaya devam eder**. Elinde bir güvenlik ağı olur, ama düzeltmesi giderek zorlaşan bir kod da olur. TDD'yi değersizleştiren şey tam olarak budur. Üçüncü adımda ne yapılır? Testler yeşilken tekrar kaldırılır, isimler düzeltilir, uzun metot bölünür (132). Ne yapılmaz? Yeni davranış eklenmez; davranış eklemek için önce kırmızı bir test gerekir. A şıkkı bu adımı isteğe bağlı sanır. B şıkkı testlerin kendiliğinden bozulacağını varsayar; oysa bozulan şey tasarımdır. D şıkkı adımları karıştırır — yeni test kırmızı adımda yazılır, üçüncü adımda değil. Not: yüksek kapsam TDD'nin hedefi değil, yan çıktısıdır (188).

# tdd-2 | Orta
## Soru
Bir aday şunu söylüyor: "Testi kod bittikten sonra da yazsam sonuç aynı olur; önemli olan sonunda testin var olmasıdır." Test-önce yaklaşımının bundan farkı en doğru nasıl anlatılır?
## A
Test-önce daha yüksek satır kapsamı üretir; iki yaklaşım arasındaki tek gerçek fark kapsam yüzdesidir
🤖 kapsam bir yan çıktıdır; asıl fark tasarıma uygulanan baskıdır.
## B
Test-önce arayüzü çağıran gözünden tasarlatır ve gizli bağımlılıkları hemen açığa çıkarır
🤖 Doğru: testin ilk satırı sınıfın çağrılma biçimidir; kullanışsızsa aynı dakikada görürsün.
## C
Test-sonra yazılan testler daha yavaş koşar, çünkü kod tamamlandıktan sonra bağlanırlar
🤖 koşum hızı testin seviyesine bağlıdır, ne zaman yazıldığına değil.
## D
Aralarında fark yoktur; ikisi de aynı testleri üretir, yalnız yazma sırası değişmiş olur
🤖 sıra değişince tasarım kararları değişir; test-sonra var olan tasarımı yalnız onaylar.
## Doğru
B
## Açıklama
Fark, testin ne zaman yazıldığında değil, **neyi zorladığındadır**. Test-önce yazınca sınıfın ilk kullanıcısı sen olursun; testte yazdığın ilk satır, sınıfın nasıl çağrılacağıdır. Kurulum yedi satır sürüyorsa o arayüz zordur ve bunu **kodu yazmadan** öğrenirsin. İkinci kazanç gizli bağımlılıklardır: metot içinde `new VeritabaniBaglantisi()` ya da `LocalDate.now()` varsa test yazarken hemen çarparsın. Çözüm bağımlılığı dışarıdan vermektir; bu, DIP'in günlük karşılığıdır (131). Test-sonra yazarken tasarım artık sabittir. Test zorlaşınca kimse tasarımı değiştirmez, testi zorlar: her şeyi taklit etmek, yansıma ile özel alanlara uzanmak, `static` çağrıyı taklit eden eklenti kurmak. A şıkkı kapsamı ölçüt sanır; kapsam yan çıktıdır ve testin gerçekten ölçtüğünü ancak mutasyon testi gösterir (188). C şıkkı koşum hızını yazma sırasına bağlar; hız test seviyesinin sonucudur (187). D şıkkı adayın yanılgısını tekrarlar. Dürüst sınır da eklenmelidir: TDD iyi mimari garanti etmez, yalnız sınıf ve metot düzeyinde geri bildirim verir.

# tdd-3 | Orta
## Soru
Ekip TDD'ye geçti, ama iç döngü ağırlaştı. Her kırmızı-yeşil adımında `@SpringBootTest` ile tüm bağlam yükleniyor ve tek koşum 40 saniye sürüyor. Geliştiriciler döngüyü çalıştırmayı bıraktı. En doğru düzeltme nedir?
## A
Testleri paralel koşup toplam süreyi düşürmek; seviye dağılımını olduğu gibi bırakmak
🤖 paralellik toplam süreyi kısaltır ama tek testin iç döngüdeki gecikmesini çözmez (203).
## B
İç döngüyü korumak için tüm testleri yalnız gün sonunda toplu olarak koşmak
🤖 geri bildirim geciktikçe TDD anlamını yitirir; hata saatler sonra fark edilir.
## C
Gerçek veritabanı yerine bellek içi bir motor koyup bağlam yükleme süresini kısaltmak
🤖 asıl maliyet Spring bağlamıdır; ayrıca sahte motor davranış sadakatini bozar (188).
## D
İş kuralını Spring'siz saf sınıflara çekip iç döngüde düz birim testi koşmak
🤖 Doğru: çerçeveden bağımsız çekirdek milisaniyede sınanır, döngü saniyelere iner.
## Doğru
D
## Açıklama
TDD'nin işlemesi tek şarta bağlıdır: **döngü saniyeler sürmelidir**. Geri bildirim gecikirse geliştirici döngüyü çalıştırmayı bırakır; bu ekipte olan tam olarak budur. Çözüm test aracını değiştirmek değil, **tasarımı ayırmaktır**. İş kuralını çerçeveye bağımlı olmayan saf sınıflara çekersin. O sınıflar milisaniyede sınanır ve iç döngüye girer. Çerçeveye bağlı ince kısım ise dış döngüde, daha seyrek koşulur. Sıralama şudur: birim testi milisaniyeler, entegrasyon testi saniyeler, uçtan uca test dakikalar sürer (187). Uçtan uca test iç döngüde asla kullanılmaz. Spring tarafında ara bir seçenek de vardır: dilim testleri tüm bağlamı yüklemez ve daha hızlıdır (90). A şıkkı paralelliği çözüm sanır; paralellik toplam süreyi kısaltır ama tek koşumun 40 saniyesini değiştirmez (203). B şıkkı geri bildirimi günün sonuna erteler ve döngüyü tümden öldürür. C şıkkı yanlış yerde optimizasyon yapar; ağırlığın kaynağı bağlam yüklemesidir ve bellek içi sahte motor üretim davranışından sapıp yanlış güven üretir (188).

# tdd-4 | İleri
## Soru
Bir serviste her işbirlikçi Mockito ile taklit ediliyor ve testler ağırlıklı olarak `verify(...)` çağrılarından oluşuyor. Sınıfın içinde davranış hiç değişmeden yapılan bir yeniden düzenleme on beş testi kırmızıya düşürdü. Bu tablonun doğru okuması nedir?
## A
Testler gerçekleme ayrıntısına bağlanmış; davranış yerine çağrı biçimini dondurmuş
🤖 Doğru: her `verify` bir iç ayrıntıyı sabitler; test güvenlik ağı olmaktan çıkar.
## B
Yeniden düzenleme aslında davranışı da değiştirmiş olmalı; testler doğru bir uyarı veriyor
🤖 senaryoda davranış değişmedi; kırmızı yanan şey davranış değil, çağrı biçimidir.
## C
Mockito sürümü eskidir; yeni sürümlerde `verify` çağrıları çok daha esnek eşleşir
🤖 sorun kütüphane sürümü değil, testin neyi doğruladığı seçimidir (188).
## D
Test sayısı fazladır; on beş birim testi yerine tek bir uçtan uca test yeterli olurdu
🤖 uçtan uca teste kaçmak piramidi ters çevirir ve geri bildirimi yavaşlatır (187).
## Doğru
A
## Açıklama
Bu tablo **aşırı taklidin** klasik belirtisidir. Taklitçi okul (mockist, London) her işbirlikçiyi taklitle değiştirir ve **davranış doğrulaması** yapar: kim kaç kez, hangi argümanla çağrıldı. Bunun bedeli şudur: her `verify` bir gerçekleme ayrıntısını sabitler. Test artık "ne yapıyor" demez, "nasıl yapıyor" der. Sonuç senaryodaki gibidir — davranış değişmediği hâlde düzenleme testleri kırar. En kötü etkisi de budur: ekip bir süre sonra yeniden düzenlemeyi tümden bırakır. Pratik ölçüt şudur: "Bu testleri bozmadan sınıfın içini değiştirebiliyor muyum?" Cevap hayırsa test, kod hakkında fazla şey biliyordur. Orta yol nettir: **sınırda taklit, çekirdekte gerçek nesne**. Klasik okul (Chicago) gerçek nesnelerle çalışır ve **durumu** doğrular; yalnız veritabanı, ağ ve saat gibi sınırları değiştirir. Davranış doğrulaması ise yan etkili sınırda gerekir; e-posta göndermek veya mesaj yayınlamak gibi işlerde bakılacak dönüş değeri yoktur. Bellek içi basit bir sahte gerçekleme (fake) çoğu durumda taklit nesneden iyidir (187). B şıkkı senaryoyu yanlış okur. C şıkkı sorunu kütüphaneye yükler. D şıkkı ise doğrulamayı en yavaş seviyeye taşır (187).

# tdd-5 | İleri
## Soru
On yıllık, hiç testi olmayan bir `FaturaHesaplayici` sınıfına yeni bir indirim kuralı eklenecek. Sınıfın bugünkü davranışı hiçbir yerde yazılı değil ve bağımlılıkları metot içinde üretiliyor. Doğru ilerleme sırası nedir?
## A
Önce sınıfı temiz bir tasarımla sıfırdan yazmak, sonra yeni kural için TDD döngüsüne girmek
🤖 testsiz yeniden yazımda yıllarca birikmiş kurallar sessizce kaybolur (132).
## B
Mevcut davranışı olduğu gibi donduran karakterizasyon testleri yazmak, sonra dikiş açıp TDD'ye geçmek
🤖 Doğru: önce davranışı sabitlersin, sonra bağımlılığı dışarı çıkarıp döngüye girersin.
## C
Yeni kuralı doğrudan ekleyip elle deneyerek doğrulamak; test borcunu ayrı bir işe bırakmak
🤖 elle doğrulama regresyonu yakalamaz ve ertelenen test borcu pratikte hiç kapanmaz.
## D
Yalnız yeni kural için birim testi yazmak; eski davranışı uçtan uca testlerin koruduğunu varsaymak
🤖 senaryoda hiç test yok; var olsa bile uçtan uca kapsama böyle bir garanti vermez.
## Doğru
B
## Açıklama
Michael Feathers'ın tanımı nettir: **testi olmayan kod eski koddur**. Ölçü yaş değil, güvenlik ağıdır. Burada bir kısır döngü vardır — değiştirmek için teste ihtiyaç duyarsın, test yazmak içinse kodu değiştirmen gerekir. Çıkış yolu **karakterizasyon testidir** (132): kodun bugün ne yaptığını olduğu gibi kaydedersin. "Doğru mu?" diye sormazsın; şaşırtıcı bir çıktıyı bile beklenen değer olarak yazarsın, çünkü birileri ona bağımlı olabilir. Pratik yolu şudur: beklenen değeri bilmiyorsan rastgele bir değer yaz, test kırmızı yansın ve gerçek çıktıyı sana söylesin. İkinci adım **dikiş açmaktır**: bağımlılığı yapıcı metoda parametre olarak çıkar, gizli çağrıyı korumalı metoda al ya da somut sınıfı arayüz arkasına koy. Ancak bundan sonra normal TDD döngüsüne girilir. A şıkkı sıfırdan yazmayı önerir; eski metodun içinde kimsenin hatırlamadığı kurallar vardır ve testsiz yazımda kaybolurlar (132). C şıkkı elle doğrulamaya güvenir; regresyon güvenliği üretmez. D şıkkı var olmayan bir güvenlik ağına yaslanır. Not: eski kodda TDD'ye en kolay giriş hata düzeltmedir — önce hatayı gösteren kırmızı testi yazarsın, sonra düzeltirsin.

# tdd-6 | Uzman
## Soru
Mülakatçı "TDD uygular mısınız?" diye soruyor. Gerçek durumun şu: hata düzeltmelerinde ve yeni iş kurallarında test-önce gidiyorsun, ama tanımadığın kütüphanelerle çalışırken önce keşif kodu yazıp atıyorsun. En güçlü cevap hangisidir?
## A
"Her zaman ve istisnasız TDD uygularım; test yazmadan tek satır kod yazmam"
🤖 tek alt soruyla çöker ve ezber izlenimi verir; çoğu ekipte TDD kısmen uygulanır.
## B
"TDD'yi denedik, yavaşlattığı için bıraktık; testleri artık kod bittikten sonra yazıyoruz"
🤖 bir sonucu bildirir ama deneyim anlatmaz; nerede işe yaradığını hiç göstermez.
## C
"Hata düzeltmede ve iş kuralında test-önce giderim; keşif kodunda gitmem, nedeni de şu"
🤖 Doğru: sınırı ve gerekçeyi söylemek, doğrulanabilir ve deneyim kokan cevaptır.
## D
"Ekibin süreci neyse onu uygularım; TDD kararı benim vereceğim bir şey değil"
🤖 sorumluluğu başkasına devreder; kendi çalışma düzenin hakkında bilgi vermez.
## Doğru
C
## Açıklama
Bu soru teknik bilgiden çok olgunluk ölçer. Güçlü cevabın üç parçası vardır. **Bir: nerede yaptığını söyle** — hata düzeltme, iş kuralları, sınır durumları, saf hesaplamalar. **İki: nerede yapmadığını ve nedenini söyle** — keşif kodu (spike), arayüz düzeni, performans ayarı, şeması belirsiz entegrasyon. Bu dört yerde test-önce gitmemenin gerekçesi vardır: keşif kodunda doğru cevabı henüz bilmezsin, arayüz düzeni teste dökülmez, performans ölçümle çalışır, belirsiz şemada test yalnız tahminini dondurur. **Üç: neyi koruduğunu söyle** — döngüyü saniyeler içinde tutmak ve yeniden düzenleme adımını atlamamak. A şıkkı en riskli olanıdır; "son ne zaman üçüncü adımı atladınız?" sorusu onu tek hamlede yıkar. B şıkkı yalnız bir sonucu bildirir ve TDD'nin nerede işe yaradığını göstermez. D şıkkı sorumluluğu ekibe devreder. Uydurmak ise en kötüsüdür: "TDD ile geliştirdik" dendiğinde gelen alt soru cevapsız kalırsa tüm görüşmenin güveni gider. Kapanış şöyle kurulabilir: "En çok zorlandığım yer üçüncü adım; onu atlamamak için kendime kural koydum." Bu cümle TDD'yi tören olarak değil araç olarak gördüğünü gösterir.
