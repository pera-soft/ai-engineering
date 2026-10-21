---
slug: istqb-stlc-test-tasarim
baslik: "Sözlü Prova · ISTQB Temelleri: STLC, Test Tasarım Teknikleri, Yedi İlke ve Test Seviyeleri"
kademe: Orta
sira: 286
etiketler: 11
---

## Konu anlatımı

Bu dosya alanın giriş kapısıdır. İlan açıkça "ISTQB Sertifikası bulunan" diyor, dolayısıyla görüşmede bu terminoloji mutlaka geçer. ISTQB'nin asıl değeri sertifika kâğıdı değildir; değeri **ortak dildir**. Mülakatçı "denklik sınıfı" dediğinde ne kastettiğini bilmek, aynı şeyi kendi kelimelerinle anlatmaktan farklıdır. Kendi kelimelerinle anlatan aday doğru şeyi söyler ama karşı taraf onu eşleştiremez. Bu dosya o dili verir. Sertifikan yoksa dürüst söylersin ve terminolojiye hâkim olduğunu gösterirsin (`256`).

Beş açık uçlu soru var; hepsi sözlü anlatılmak için yazıldı. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz. Onu kendi geçmişinden çıkarman için sorular sorulur. Dosyanın çekirdeği ikinci sorudur: test tasarım teknikleri. Orada teknik adı saymak yetmez; her tekniği somut bir örnekle bağlaman gerekir.

Bir uyarı: ISTQB müfredatının **yapısı** burada anlatılır. Sınav soru sayısı, geçme notu, ücret ve sürüm yılı gibi ayrıntılar zamanla değişir; onları bu dosyadan öğrenme, güncel müfredattan teyit et. Mülakatta da böyle davran; hatırlamadığın bir rakamı söylemek yerine "güncel müfredattan bakarım" demek daha güçlüdür.

### "STLC nedir, SDLC'den farkı ne?"

**Soru aslında ne ölçüyor?**

Bu soru bir tanım sorusu gibi görünür ama değildir. Görüşmeci iki kısaltmanın açılımını duymak istemiyor. Merak ettiği şey şu: bu aday testin ne zaman başladığını nereye koyuyor. Zayıf aday testi kodlamadan sonra gelen bir aşama sanar. Güçlü aday testin gereksinim analizinde başladığını söyler ve nedenini maliyetle açıklar. Ayrım tam burada çıkar ve cevabın ilk otuz saniyesinde belli olur.

**Sözlü cevap iskeleti**

1. **İki döngüyü ayır.** SDLC ürünün yaşam döngüsüdür: fikir, analiz, tasarım, geliştirme, test, sürüm, bakım. STLC ise test faaliyetinin kendi döngüsüdür ve SDLC'nin içinde akar.
2. **STLC aşamalarını sırayla say.** Gereksinim analizi, test planlama, test tasarımı, test ortamı kurulumu, test koşumu, test kapanışı. Her aşamanın kendi girdisi, çıktısı ve çıkış ölçütü vardır.
3. **En değerli cümleyi burada kur.** Test kodlamadan sonra başlamaz; gereksinim analizinde başlar. Gereksinimi okuyup çelişkiyi bulmak da bir test faaliyetidir ve adı statik testtir.
4. **Maliyetle gerekçelendir.** Gereksinimde bulunan hata bir cümle düzeltmesidir. Üretimde bulunan aynı hata veri düzeltmesi, acil sürüm ve itibar kaybı getirir. Erken bulunan hata en ucuz hatadır (`228`).
5. **Çıkış ölçütünü ekle.** Her aşama "bitti" diyebilmek için ölçüt ister. Test tasarımı biter mi? Kabul ölçütlerinin tamamı en az bir senaryoyla karşılanmışsa biter.
6. **Kendi rolünü yerleştir.** Bu döngüde testçinin ilk teması gereksinim incelemesidir. Orada sorduğun soru, sonra yazacağın senaryoyu belirler (`229`).

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SDLC icinde akan STLC asamalari ve hatanin bulundugu yere gore artan maliyet">
  <defs>
    <marker id="istqb-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">STLC, SDLC'nin içinde akar — test kodlamadan sonra değil, gereksinimde başlar</text>
  <rect x="14" y="36" width="632" height="30" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="56" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SDLC · ürünün yaşam döngüsü: fikir, analiz, tasarım, geliştirme, test, sürüm, bakım</text>
  <line x1="330" y1="67" x2="330" y2="74" stroke="#5D7789" stroke-width="1.4" marker-end="url(#istqb-ok)"/>
  <rect x="14" y="76" width="97" height="64" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="62" y="102" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Gereksinim</text>
  <text x="62" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">analizi</text>
  <rect x="121" y="76" width="97" height="64" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="169" y="102" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Test</text>
  <text x="169" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">planlama</text>
  <rect x="228" y="76" width="97" height="64" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="276" y="102" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">3 · Test</text>
  <text x="276" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tasarımı</text>
  <rect x="335" y="76" width="97" height="64" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="383" y="102" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">4 · Ortam</text>
  <text x="383" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kurulumu</text>
  <rect x="442" y="76" width="97" height="64" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="490" y="102" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · Test</text>
  <text x="490" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koşumu</text>
  <rect x="549" y="76" width="97" height="64" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="597" y="102" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">6 · Test</text>
  <text x="597" y="118" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapanışı</text>
  <text x="330" y="157" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">STLC · test faaliyetinin kendi döngüsü: her aşamanın girdisi, çıktısı ve çıkış ölçütü vardır</text>
  <rect x="14" y="168" width="632" height="144" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="190" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Hatanın bulunduğu yer maliyeti belirler — testin erken başlamasının gerekçesi budur</text>
  <line x1="50" y1="290" x2="620" y2="290" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="70" y="274" width="100" height="16" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="120" y="268" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">en ucuz</text>
  <text x="120" y="304" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">gereksinimde</text>
  <rect x="210" y="256" width="100" height="34" rx="3" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="260" y="250" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">artar</text>
  <text x="260" y="304" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tasarımda</text>
  <rect x="350" y="234" width="100" height="56" rx="3" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="400" y="228" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">daha pahalı</text>
  <text x="400" y="304" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kodlama ve testte</text>
  <rect x="490" y="208" width="100" height="82" rx="3" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="540" y="202" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">en pahalı</text>
  <text x="540" y="304" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">üretimde</text>
</svg>
```

**Elenme sebepleri**

- "STLC, SDLC'nin test aşamasıdır." Testi kodlamadan sonraya koyar; erken test kavramını hiç kurmadığını gösterir.
- Aşamaları sayamamak. Sırayı bilmeyen aday süreci yaşamamış gibi duyulur; bu soruda sıra ezberden değil işten gelir.
- Çıkış ölçütünden hiç söz etmemek. Ölçütü olmayan aşama bitmez, uzar; süreç hâkimiyeti tam burada görünür.
- İkisini eş anlamlı sanmak. "Aynı şeyin iki adı" cevabı terminoloji hâkimiyetini tek cümlede siler.
- Statik testi atlamak. Gereksinim ve tasarım incelemesi de testtir; bunu anmayan aday testi yalnız koşum sanır.
- Süreci otomasyona bağlamak. STLC otomasyondan bağımsızdır; manuel süreçte de aynı aşamalar işler.
- Maliyet gerekçesini vermemek. Erken testi savunuyorsan nedenini söylemelisin, yoksa slogan gibi duyulur.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir, yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Çalıştığın ekipte testin ilk teması ne zamandı? Gereksinim toplantısı mı, yoksa kod geldikten sonrası mı?
- Bir gereksinimdeki çelişkiyi yazılmadan önce fark ettiğin oldu mu? Nasıl fark ettin?
- Test planı yazılıyor muydu, yoksa plan sözlü mü kalıyordu? Yazılmayınca ne aksadı?
- Test ortamı kimin sorumluluğundaydı? Ortam gecikmesi hiç koşumu bloke etti mi?
- Bir aşamanın bittiğine nasıl karar veriliyordu? Yazılı bir çıkış ölçütü var mıydı?
- Üretime kaçan bir hatayı hangi aşamada yakalayabilirdiniz? O soruyu ekipçe konuştunuz mu?
- Test kapanışında bir rapor yazılıyor muydu? İçinde ne vardı: sayı mı, risk mi, ikisi de mi?

Böyle bir deneyimin yoksa uydurma. `256`'daki dürüst köprüyü kur: sınırı söyle, komşu deneyimini somutla, farkı adlandır. Örneğin geliştirici olarak kabul ölçütü yazımına katılmış olmak da gerçek bir cevaptır.

### "Test tasarım tekniklerini anlatır mısın?"

**Soru aslında ne ölçüyor?**

Bu dosyanın çekirdeği burasıdır ve mülakatın en ayrıştırıcı sorusudur. Çoğu aday teknik adlarını sayar ve orada durur. Adları saymak ezberi kanıtlar, beceriyi kanıtlamaz. Görüşmecinin gerçekten aradığı şey şudur: bu aday bin olası girdiyi yedi teste indirebiliyor mu ve indirirken neyi kaybettiğini biliyor mu. Bu yüzden **her tekniği tek somut örnekle bağlaman** gerekir. Örneksiz liste zayıf cevaptır; bunu bilerek söylüyorum, çünkü en sık yapılan hata budur.

**Sözlü cevap iskeleti**

1. **Üç aileyi adlandır.** Kara kutu teknikleri iç yapıya bakmadan davranışı sınar. Beyaz kutu teknikleri kodun yapısına bakar. Deneyime dayalı teknikler ise ikisinin bıraktığı boşluğu tarar.
2. **Denklik sınıfıyla (equivalence partitioning) aç.** Girdi uzayını aynı davranışı üreten gruplara bölersin; her gruptan tek temsilci yeterlidir. Yaş alanı 18-65 kabul ediyorsa üç sınıf vardır: 18 altı, 18-65 arası, 65 üstü. Temsilciler 12, 40 ve 70 olabilir.
3. **Sınır değer analizini (boundary value analysis) üstüne koy.** Hata sınıfın ortasında değil, sınırında yaşar. Aynı alanda test edilecek değerler 17, 18, 65 ve 66'dır. Neden? Çünkü kodda `>=` yerine `>` yazılmışsa hata yalnız bu dört değerde görünür.
4. **Karar tablosunu (decision table) kural örneğiyle ver.** Kargo ücreti üyeliğe, sepet tutarına ve kupona bağlıysa koşul birleşimleri satır satır çıkarılır. Her satır bir kuraldır ve her kural bir testtir. Tablo, kimsenin düşünmediği birleşimi görünür kılar.
5. **Durum geçişini (state transition) davranışla anlat.** Nesnenin tepkisi geçmişine bağlıysa bu teknik gerekir. Sipariş "yeni, ödendi, kargoda, teslim, iptal" durumlarında gezer. Geçerli geçişler kadar **yasak geçişler** de test edilir: teslim edilmiş sipariş iptal edilebiliyorsa bu bir hatadır.
6. **Kullanım senaryosunu ve beyaz kutuyu kısa geç.** Kullanım senaryosu uçtan uca iş akışını sınar. Beyaz kutuda satır kapsamı ve karar kapsamı vardır; satırın çalışması, koşulun her iki dalının denendiği anlamına gelmez.
7. **Deneyime dayalıyla kapat.** Hata tahmini (error guessing) geçmişte nerede kırıldığını bilmektir. Keşifsel test (exploratory) ise tasarım ve koşumu aynı anda yapar; tekniklerin bıraktığı boşluğu bulur (`234`).
8. **Takas cümlesini söyle.** Bu teknikler kapsama sağlamaz, **kapsamı yönetir**. Tam test imkânsızdır; teknikler sonlu bir test kümesiyle en çok bilgiyi almaya yarar (`228`).

```svg
<svg viewBox="0 0 660 408" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yas alani 18-65 ornegiyle denklik sinifi, sinir deger analizi, karar tablosu ve durum gecisi">
  <defs>
    <marker id="istqb-sinir" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto"><path d="M0 0 L8 4 L0 8 z" fill="#B07D20"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Örnek: yaş alanı 18-65 kabul ediyor — iki teknik birlikte çalışır</text>
  <text x="130" y="48" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">denklik sınıfı 1 · temsilci 12</text>
  <text x="335" y="48" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">denklik sınıfı 2 · temsilci 40</text>
  <text x="535" y="48" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">denklik sınıfı 3 · temsilci 70</text>
  <rect x="40" y="54" width="180" height="32" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="130" y="75" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0 – 17 · geçersiz</text>
  <rect x="220" y="54" width="230" height="32" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="335" y="75" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">18 – 65 · geçerli</text>
  <rect x="450" y="54" width="170" height="32" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="535" y="75" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">66 ve üstü · geçersiz</text>
  <line x1="220" y1="88" x2="220" y2="102" stroke="#B07D20" stroke-width="1.5" marker-end="url(#istqb-sinir)"/>
  <line x1="450" y1="88" x2="450" y2="102" stroke="#B07D20" stroke-width="1.5" marker-end="url(#istqb-sinir)"/>
  <text x="203" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">17</text>
  <text x="237" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">18</text>
  <text x="433" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">65</text>
  <text x="467" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">66</text>
  <text x="330" y="136" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Sınır değerler tam burada durur; kodda karşılaştırma operatörü yanlışsa hata yalnız bu dördünde görünür</text>
  <rect x="14" y="148" width="310" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="169" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Denklik sınıfı (equivalence partitioning)</text>
  <text x="169" y="192" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı davranışı üreten girdiler tek grup sayılır</text>
  <text x="169" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her gruptan tek temsilci yeterlidir: 12 · 40 · 70</text>
  <text x="169" y="232" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">amaç: test sayısını azaltmak, bilgiyi korumak</text>
  <rect x="336" y="148" width="310" height="96" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="169" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sınır değer analizi (boundary value analysis)</text>
  <text x="491" y="192" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata sınıfın ortasında değil, kenarında yaşar</text>
  <text x="491" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her sınırda geçersiz ve geçerli komşu denenir</text>
  <text x="491" y="232" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">dört test: 17 · 18 · 65 · 66</text>
  <rect x="14" y="256" width="204" height="96" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="116" y="277" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Karar tablosu</text>
  <text x="116" y="299" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">koşul birleşimleri kural üretir</text>
  <text x="116" y="317" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">üyelik × tutar × kupon</text>
  <text x="116" y="335" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">her kural bir testtir</text>
  <rect x="228" y="256" width="204" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="277" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Durum geçişi</text>
  <text x="330" y="299" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tepki geçmişe bağlıysa kullanılır</text>
  <text x="330" y="317" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yeni → ödendi → kargoda</text>
  <text x="330" y="335" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">yasak geçiş de test edilir</text>
  <rect x="442" y="256" width="204" height="96" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="544" y="277" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Deneyime dayalı</text>
  <text x="544" y="299" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hata tahmini (error guessing)</text>
  <text x="544" y="317" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">keşifsel test (exploratory)</text>
  <text x="544" y="335" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tekniklerin bıraktığı boşluğu tarar</text>
  <rect x="14" y="364" width="632" height="32" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="384" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Teknik adlarını saymak zayıf cevaptır — her tekniği tek somut örnekle bağla, yoksa liste ezber gibi duyulur</text>
</svg>
```

**Elenme sebepleri**

- Yalnız teknik adlarını saymak. Örnek vermeyen aday kitap okumuş sayılır, uygulamış sayılmaz; bu soruda en sık eleyen davranıştır.
- Denklik sınıfı ile sınır değeri karıştırmak. Biri girdiyi gruplar, diğeri grupların kenarını sınar; ikisi birbirinin yerini tutmaz.
- Sınır değerde yalnız geçerli ucu denemek. 18 ve 65 test edilip 17 ile 66 atlanırsa operatör hatası görünmez.
- Geçersiz sınıfları hiç anmamak. Yalnız mutlu yolu test eden aday hata bulamaz; geçersiz girdi asıl kırılma yeridir.
- Karar tablosunu gerektiren kuralda tek tek koşul denemek. Birleşimler denenmezse kimsenin düşünmediği kural sessiz kalır.
- Durum geçişinde yasak geçişleri atlamak. Teslim edilmiş siparişin iptal edilebilmesi tam bu testle yakalanır.
- Beyaz kutuda satır kapsamını yeterli saymak. Satırın çalışması, koşulun her iki dalının denendiği anlamına gelmez.
- "Her şeyi test ederim" demek. Tam test imkânsızdır; bu cümle tekniklerin varlık sebebini anlamadığını gösterir.

**Kendi hikâyen**

Bu soruda hikâyeyi bir alandan çıkarabilirsin; büyük bir proje gerekmez:

- Test ettiğin bir formda hangi alanın sınırı vardı? Tutar, tarih, adet, yaş, karakter uzunluğu?
- O sınırda hiç hata buldun mu? Belirti neydi: eşit değer kabul edilmedi mi, yoksa fazlası mı geçti?
- Koşul birleşiminden doğan bir hata gördün mü? Kampanya, indirim, kargo, yetki kuralı gibi?
- Bir nesnenin durumu yanlış yönde ilerledi mi? Örneğin iptal edilmiş kayıt yeniden işlendi mi?
- Test senaryolarını hangi yöntemle çıkarıyordun? Sezgiyle mi, yoksa bir teknikle mi?
- Kaç senaryo yazdın ve kaçı gerçekten farklı bir şey sınıyordu? Bu ikinci sayı asıl sayıdır.
- Bir tekniği öğrendikten sonra senaryo sayın azaldı mı? Azalma iyi bir işarettir ve anlatılabilir.

Deneyimin yoksa uydurma. Bu teknikler kâğıt üstünde çalışılabilir ve dürüstçe öyle anlatılır. Kendi küçük projendeki bir doğrulama kuralına denklik sınıfı çıkarmak gerçek bir alıştırmadır; `256`'daki köprüyle bunu olduğu gibi söylersin.

### "Yedi test ilkesi nedir, hangisi pratikte en çok ihlal edilir?"

**Soru aslında ne ölçüyor?**

İlk kısım ezber ölçer, ikinci kısım olgunluk ölçer. Görüşmeci yedi maddeyi duymak ister ama kararı ikinci kısımda verir. "Hangisi en çok ihlal edilir" sorusu, adayın gerçek bir ekipte bulunup bulunmadığını ele verir. Sahada bulunmuş aday cevabı tereddütsüz verir, çünkü o ihlali yaşamıştır. Bu yüzden ilkeleri sayarken hız yap, tartışmayı ikinci kısma sakla.

**Sözlü cevap iskeleti**

1. **Yediyi hızlı say.** Test hatanın varlığını gösterir, yokluğunu değil. Tam test imkânsızdır. Erken test kazandırır. Hatalar kümelenir. Testler zamanla körelir. Test bağlama bağlıdır. Hata yokluğu safsatadır.
2. **Birinci ilkeyi öne çıkar.** Yeşil geçen bir test kümesi "hata yok" demez; yalnız "bu testler geçti" der. Bu ayrım tüm test stratejisinin temelidir (`187`).
3. **İkinci ilkeyi maliyetle bağla.** Tam test imkânsızdır, çünkü girdi uzayı sonsuza yakındır. Bu yüzden risk bazlı seçim yaparsın; teknikler de zaten bunun için vardır.
4. **Böcek ilacı paradoksunu (pesticide paradox) somutla.** Aynı gerileme testleri tekrarlandıkça yeni hata bulma gücünü yitirir. Böcekler o ilaca direnç kazanır. Çözüm seti gözden geçirmek, yenilemek ve yeni tekniklerle beslemektir.
5. **Kümelenmeyi ölçüye bağla.** Hatalar birkaç modülde toplanır. Hata yoğunluğu (defect density) bunu gösterir ve bir sonraki turda nereye bakacağını söyler.
6. **En çok ihlal edilene karar ver ve gerekçe ver.** Pratikte en sık ihlal edilen ilke birincisidir. "Testler geçti, sürüm hazır" cümlesi tam bu ihlaldir. İkinci sırada hata yokluğu safsatası gelir: ürün şartnameye uyar ama kullanıcının işine yaramaz.
7. **Bağlama bağlılıkla kapat.** Bir bankacılık uygulamasıyla bir kampanya sitesinin test yaklaşımı aynı olamaz. Bu ilke, "her yerde şu araç kullanılır" iddiasının panzehiridir (`228`).

**Elenme sebepleri**

- Yedi ilkeyi ezber gibi sıralayıp durmak. Soru iki parçalıdır; ikinci parçaya geçmeyen aday olgunluk sinyali vermez.
- "Testler geçtiyse hata yoktur" demek. Birinci ilkenin doğrudan ihlalidir ve bu soruda anında yakalanır.
- Böcek ilacı paradoksunu duymamış olmak. Gerileme setinin neden yenilenmesi gerektiğini açıklayamayan aday seti dondurur.
- Hata yokluğu safsatasını atlamak. Şartnameye uyan ama işe yaramayan ürün gerçek bir başarısızlıktır.
- Tam testin imkânsızlığını kabul etmemek. "Yeterli zaman olsa her şeyi test ederdim" cümlesi girdi uzayını hiç düşünmediğini gösterir.
- Kümelenmeyi ölçmeden savunmak. İlkeyi biliyorsan hangi ölçütle takip edildiğini de söylemelisin.
- İlkeleri günlük işten kopuk anlatmak. Her ilkeyi tek cümlelik bir sonuca bağlamayan cevap teorik kalır.

**Kendi hikâyen**

Bu soruda en güçlü cevap yaşanmış bir ihlaldir. Şu sorular onu bulur:

- Hiç "testler yeşil" diye çıkılan bir sürümde üretimde hata çıktı mı? Hata neredeydi ve neden testte yoktu?
- Gerileme setiniz ne kadar süredir aynıydı? Son ne zaman bir senaryo çıkarıldı veya eklendi?
- En çok hata hangi modülden çıkıyordu? Bu bilgi test önceliğini değiştirdi mi?
- Şartnameye uyan ama kullanıcının şikâyet ettiği bir özellik gördün mü? Nasıl fark edildi?
- Zaman daralınca neyi test etmemeye karar verdiniz? Bu kararı kim verdi ve neye göre?
- Test kapsamı hakkında bir sayı paylaşılıyor muydu? O sayı davranışı nasıl etkiledi?
- Aynı hatanın ikinci kez çıktığı oldu mu? Sonrasında sete bir senaryo eklendi mi?

Bu deneyimlerin yoksa uydurma. İlkeler kâğıttan öğrenilir ve bunu söylemek zarar vermez. `256`'daki köprüyü kur: ilkeyi anlat, sonra kendi projende gözlemlediğin küçük bir karşılığını ver.

### "Test seviyeleri ve türleri nelerdir?"

**Soru aslında ne ölçüyor?**

Bu soru ısınma sorusu gibi başlar ama içinde bir tuzak taşır. Seviyeleri herkes sayar. Ayrım, gerileme testi (regression) ile yeniden test (retest) arasındaki farkta çıkar. Bu ikisini karıştırmak klasik elenme sebebidir ve şaşırtıcı biçimde yaygındır. Duman testi (smoke) ile akıl sağlığı testi (sanity) ayrımı da aynı işi görür. Görüşmeci burada terminolojinin sende oturmuş olup olmadığına bakar.

**Sözlü cevap iskeleti**

1. **Dört seviyeyi sırayla ver.** Birim testi tek parçayı, entegrasyon testi parçaların birleşimini, sistem testi bütün ürünü, kabul testi ise işin beklentisini sınar. Seviye yükseldikçe kapsam genişler, geri bildirim yavaşlar (`187`).
2. **Türleri ikiye ayır.** İşlevsel testler "ne yapıyor" sorusunu sorar. İşlevsel olmayan testler "nasıl yapıyor" sorusunu sorar: başarım, güvenlik, kullanılabilirlik, taşınabilirlik, uyumluluk.
3. **Yeniden testi tanımla.** Bildirdiğin hata düzeltildi mi, ona bakar. Kapsamı dardır: yalnız o hatanın yeniden üretim adımları koşulur.
4. **Gerileme testini tanımla.** Bu düzeltme çalışan başka bir şeyi bozdu mu, ona bakar. Kapsamı geniştir: etkilenen alan artı kritik akışlar. Otomasyonun ilk adayı budur.
5. **Farkı tek cümlede kilitle.** Yeniden test düzeltmeye bakar, gerileme testi yan etkiye bakar. İkisi de gerekir; biri diğerinin yerine geçmez.
6. **Duman ile akıl sağlığını ayır.** Duman testi geniş ama sığdır: yapı ayakta mı, temel akışlar açılıyor mu. Başarısızsa sürüm reddedilir ve derine inilmez. Akıl sağlığı testi dar ve sığdır: belirli bir düzeltme mantıklı çalışıyor mu.
7. **Değişim analizini ekle.** Gerileme kapsamını rastgele seçmezsin; hangi kodun neyi etkilediğini izlenebilirlik matrisiyle (traceability matrix) çıkarırsın (`229`).

```svg
<svg viewBox="0 0 660 338" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yeniden test ile gerileme testi farki, duman ve akil sagligi testi ayrimi">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yeniden test ile gerileme testi aynı şey değildir — karıştırmak klasik elenme sebebidir</text>
  <rect x="14" y="36" width="310" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Yeniden test (retest / confirmation)</text>
  <text x="169" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Soru: bildirdiğim hata gerçekten düzeldi mi?</text>
  <text x="169" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Kapsam: yalnız o hatanın koşum adımları</text>
  <text x="169" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Zaman: düzeltme geldikten hemen sonra</text>
  <text x="169" y="148" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Dar kapsam, kısa koşum, tek karar</text>
  <text x="169" y="170" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Atlanırsa: kapatılan hata aslında açık kalır</text>
  <rect x="336" y="36" width="310" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="491" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gerileme testi (regression)</text>
  <text x="491" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Soru: bu değişiklik çalışan bir yeri bozdu mu?</text>
  <text x="491" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Kapsam: etkilenen alan artı kritik akışlar</text>
  <text x="491" y="126" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Zaman: her anlamlı değişiklikten sonra</text>
  <text x="491" y="148" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Geniş kapsam, otomasyonun ilk adayı</text>
  <text x="491" y="170" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Atlanırsa: yan etki üretime kaçar</text>
  <rect x="14" y="200" width="204" height="82" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="116" y="221" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Duman testi (smoke)</text>
  <text x="116" y="241" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">soru: yapı ayakta mı?</text>
  <text x="116" y="257" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">geniş ama sığ</text>
  <text x="116" y="273" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">başarısızsa sürüm reddedilir</text>
  <rect x="228" y="200" width="204" height="82" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="221" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Akıl sağlığı testi (sanity)</text>
  <text x="330" y="241" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">soru: bu düzeltme mantıklı mı?</text>
  <text x="330" y="257" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">dar ve sığ</text>
  <text x="330" y="273" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">derin koşumdan önce ön kontrol</text>
  <rect x="442" y="200" width="204" height="82" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="544" y="221" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Gerileme (regression)</text>
  <text x="544" y="241" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">soru: eski davranış korundu mu?</text>
  <text x="544" y="257" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">geniş ve derin</text>
  <text x="544" y="273" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">set zamanla yenilenir</text>
  <rect x="14" y="294" width="632" height="32" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="314" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Böcek ilacı paradoksu: aynı gerileme seti zamanla hata bulmaz olur — set gözden geçirilir ve yenilenir</text>
</svg>
```

**Elenme sebepleri**

- Gerileme ile yeniden testi eş anlamlı kullanmak. Bu soruda en hızlı eleyen hatadır; ikisi farklı soruyu sorar.
- "Yeniden test manuel, gerileme otomatiktir" demek. Ayrım koşum biçiminde değil, kapsam ve amaçtadır.
- Duman testini derin sanmak. Duman geniş ve sığdır; derinleşirse sürüm kapısı olmaktan çıkar.
- Seviyeleri sayıp geri bildirim süresini anmamak. Piramidin mantığı tam bu takasla kurulur (`187`).
- İşlevsel olmayan testleri unutmak. Başarım ve güvenlik testi anılmazsa kapsam eksik görünür.
- Gerileme kapsamını "hepsini koşarım" diye tanımlamak. Süre büyüdükçe set çalışmaz olur; seçim gerekir.
- Değişim analizinden söz etmemek. Neyin etkilendiğini bilmeden seçilen gerileme kapsamı tahmindir (`229`).

**Kendi hikâyen**

Bu soruda gerçek bir koşum hatırası yeter. Şu sorular onu çıkarır:

- Bir düzeltme geldiğinde önce neyi koşuyordun? Yalnız hatanın adımlarını mı, daha fazlasını mı?
- Gerileme setiniz var mıydı? Kaç senaryodan oluşuyordu ve koşumu ne kadar sürüyordu?
- Set çok uzayınca ne yaptınız? Bölme, önceliklendirme, otomasyon, yoksa atlama mı?
- Bir düzeltmenin başka bir yeri bozduğu oldu mu? Nasıl fark edildi: testte mi, üretimde mi?
- Sürüm öncesi bir duman testi var mıydı? Başarısız olunca sürüm gerçekten durdurulur muydu?
- Kapatılan bir hatanın aslında düzelmediği ortaya çıktı mı? Sonrasında süreç değişti mi?
- Hangi testleri otomasyona ilk aldınız? Seçim ölçütü neydi: tekrar sıklığı mı, risk mi?

Böyle bir deneyimin yoksa uydurma. Geliştirici olarak kendi değişikliğinden sonra var olan testleri koşmuş olmak da gerçek bir gerileme deneyimidir. `256`'daki köprüyle bunu olduğu gibi anlat.

### "ISTQB sertifikan var mı?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi ölçmez, iki şey ölçer: dürüstlük ve terminoloji hâkimiyeti. İlan sertifikayı açıkça istiyor, dolayısıyla soru mutlaka gelir. Sertifikan varsa cevap kısadır ve devamı önemlidir. Sertifikan yoksa asıl sınav burada başlar. Uydurmanın maliyeti asimetriktir: kazanılan şey tek bir soru, kaybedilen şey mülakatın tamamıdır. Kıdemli bir mülakatçı bunu birkaç takip sorusuyla yakalar ve o andan sonra doğru verdiğin cevaplara da güvenmez (`256`).

**Sözlü cevap iskeleti**

1. **Varsa doğrudan söyle.** Hangi seviye olduğunu ve ne zaman aldığını belirt, sonra tek cümleyle ne kattığını ekle. Kâğıdın kendisi değil, kazandırdığı ortak dil anlatılır.
2. **Yoksa sınırı ilk cümlede söyle.** "Sertifikam yok" cümlesi kısa ve net kurulur. Etrafından dolaşmak, olmayan bir şeyi varmış gibi duyurmaya çalışmak izlenimi bırakır.
3. **Hemen köprüyü kur.** Sınırı söyledikten sonra durma. Terminolojiye hâkim olduğunu **göstererek** söyle; iddia etmek yetmez.
4. **Göstermeyi somut yap.** Tek bir teknikle göster: "Örneğin 18-65 kabul eden bir alanda denklik sınıfları 18 altı, aralık ve 65 üstüdür; sınır değerler 17, 18, 65 ve 66'dır." Bu cümle sertifikadan daha çok şey söyler.
5. **Planı ver, tarih uydurma.** Almayı planlıyorsan söyle. Hangi seviyeyle başlayacağını söyleyebilirsin. Sınav ücreti, soru sayısı ve geçme notu gibi ayrıntılara girme; bunlar değişir ve yanlış söylersen aleyhine döner.
6. **Kalibrasyonu ekle.** Emin olmadığın bir müfredat ayrıntısını sorarlarsa "bunu güncel müfredattan teyit ederim" de. Bildiğini ve tahmin ettiğini ayırabilen aday güven verir.

**Elenme sebepleri**

- Olmayan sertifikayı var göstermek. Yakalanması kolaydır ve mülakatın tamamını götürür; bu, geri dönüşü olmayan tek hatadır.
- Kuru "hayır" deyip susmak. Dürüsttür, zarar vermez ama hiçbir bilgi üretmez; bildiğin terminoloji hiç görünmez.
- Sertifikayı küçümsemek. "Kâğıt önemli değil, saha deneyimi önemli" cümlesi ilanın açık maddesine karşı savunmacı okunur.
- Sertifikası olup içeriğini anlatamamak. Kâğıdı olan adaya takip sorusu gelir; denklik sınıfını örnekleyemeyen aday daha kötü duruma düşer.
- Sınav ayrıntısı hakkında rakam vermek. Soru sayısı, geçme notu ve ücret değişir; yanlış rakam bilgiyi değil dikkatsizliği gösterir.
- Aldığın eğitimi abartmak. "Kursa gittim" ile "müfredatı çalıştım" farklıdır; hangisiyse o söylenir.
- Planı belirsiz bırakmak. "Bir ara alırım" cümlesi ilgisizlik gibi duyulur; hangi seviyeyle başlayacağını söylemek yeter.

**Kendi hikâyen**

Bu soruda hikâye değil, dürüst bir konum gerekir. Şu sorular onu netleştirir:

- Sertifikan var mı, kısmen mi, hiç mi? Cevabı önceden tek cümleyle hazırla.
- Terminolojiyi nereden öğrendin? Müfredat, kitap, ekip içi kullanım, yoksa iş üstünde mi?
- Hangi kavramları örnekle anlatabiliyorsun? En az üçünü seç ve hazır cümle kur.
- Hangi kavramları duydun ama tam oturmadı? Onları önceden bil ve sorulursa kalibrasyon cümlesi kur.
- Almayı planlıyor musun? Hangi seviyeyle başlarsın ve neden?
- Sertifikasız yaptığın hangi iş sertifikayı telafi eder? Yazdığın test planı, kurduğun gerileme seti, raporladığın hata?
- Bu terminolojiyi ekip içinde kullanan var mıydı? Ortak dil eksikliği hiç yanlış anlaşılma yarattı mı?

Cevabın "yok" ise bu bir kayıp değildir; sorun yalnız kötü teslim edilirse kayba döner. `256`'daki üç parçalı yapıyı kur: sınır, köprü, kalibrasyon. Sınırı söyle, terminolojiyi somut örnekle göster, plana bağla. Bu üçlü, uydurulmuş bir sertifikadan her zaman güçlüdür.

### Bu bölümden çıkan ders

Bu beş soru bir sertifikayı değil, ortak dili ölçer. Birincisinde kazanan cümle en baştadır: test kodlamadan sonra başlamaz, gereksinim analizinde başlar ve orada bulunan hata en ucuz hatadır. İkincisi dosyanın çekirdeğidir ve tek kuralı vardır: teknik adı saymak zayıf cevaptır, her tekniği somut bir örnekle bağla — 18-65 kabul eden bir alanda denklik sınıfları üç tanedir, sınır değerler 17, 18, 65 ve 66'dır. Üçüncüde ilkeleri saymak yetmez; hangisinin en çok ihlal edildiğine karar vermek olgunluk gösterir ve cevap çoğu ekipte birincidir, çünkü "testler geçti" cümlesi "hata yok" demek değildir. Dördüncüde ayrım tek cümleyle kilitlenir: yeniden test düzeltmeye bakar, gerileme testi yan etkiye bakar; bu ikisini karıştırmak klasik elenme sebebidir. Beşincide ise ölçülen şey bilgi değil dürüstlüktür; sertifikan yoksa sınırı söyle, terminolojiyi göstererek köprü kur, planı ver ve emin olmadığın müfredat ayrıntısını uydurma. Beşinde de aynı disiplin geçerlidir: kavramı adlandır, örneği ver, sınırını söyle. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# istqb-1 | Temel
## Soru
STLC ile SDLC arasındaki ilişki hakkında en doğru ifade hangisidir?
## A
STLC, SDLC'nin kodlama tamamlandıktan sonra başlayan son aşamasıdır; test bu yüzden bir teslim öncesi kontroldür
🤖 test gereksinim analizinde başlar; kodlamayı beklemek en pahalı hatayı üretir.
## B
STLC test faaliyetinin kendi döngüsüdür ve SDLC'nin içinde gereksinim aşamasından itibaren akar
🤖 Doğru: gereksinim incelemesi de bir test faaliyetidir.
## C
STLC ile SDLC aynı sürecin iki farklı adıdır; fark yalnız hangi ekibin hangi adı kullandığındadır
🤖 biri ürünün, diğeri test faaliyetinin döngüsüdür; kapsamları farklıdır.
## D
STLC yalnız otomasyon projelerinde tanımlıdır; manuel test süreçlerinde böyle bir aşama ayrımı yapılmaz
🤖 STLC otomasyondan bağımsızdır; manuel süreçte de aynı aşamalar işler.
## Doğru
B
## Açıklama
SDLC **ürünün** yaşam döngüsüdür: fikir, analiz, tasarım, geliştirme, test, sürüm, bakım. STLC ise **test faaliyetinin** kendi döngüsüdür ve SDLC'nin içinde akar. Aşamaları şunlardır: gereksinim analizi, test planlama, test tasarımı, ortam kurulumu, koşum ve kapanış. Her aşamanın kendi girdisi, çıktısı ve çıkış ölçütü vardır. A şıkkı en yaygın yanlıştır; testi kodlamadan sonraya koyar. Oysa STLC'nin ilk aşaması gereksinim analizidir ve gereksinimi okuyup çelişkiyi bulmak da bir test faaliyetidir; adı statik testtir. Bunun pratik gerekçesi maliyettir: gereksinimde bulunan hata bir cümle düzeltmesiyle kapanır, üretimde bulunan aynı hata veri düzeltmesi ve acil sürüm getirir (`228`). C şıkkı iki döngüyü eşitler; kapsamları farklıdır. D şıkkı süreci araca bağlar; STLC otomasyondan bağımsızdır ve tamamen manuel bir ekipte de aynı aşamalar işler. Mülakatta bu sorunun kazanan cümlesi tektir: test kodlamadan sonra değil, gereksinimde başlar (`187`).

# istqb-2 | Temel
## Soru
Bir formdaki yaş alanı 18 ile 65 arasını (iki uç dâhil) kabul ediyor. Sınır değer analizi (boundary value analysis) hangi değerleri seçer?
## A
0, 18, 65, 99 — alanın veri türünün izin verdiği en küçük ve en büyük sayılar da birer sınır sayılır
🤖 bunlar veri türünün sınırı; kuralın sınırı 18 ile 65'tir.
## B
18 ve 65 — kabul edilen aralığın iki ucu test edilirse sınır davranışı yeterince kapsanmış olur
🤖 geçersiz komşular denenmezse `>` ile `>=` karışıklığı görünmez.
## C
1'den 100'e kadar tüm tam sayılar — sınır değer analizi aralığın tamamını sistematik olarak tarar
🤖 bu tam test denemesidir; teknik zaten sayıyı azaltmak için vardır.
## D
17, 18, 65, 66 — her sınırın hem geçersiz hem geçerli komşusu denenir
🤖 Doğru: hata tam sınırda, karşılaştırma operatöründe yaşar.
## Doğru
D
## Açıklama
Denklik sınıfı (equivalence partitioning) girdiyi aynı davranışı üreten gruplara böler: 18 altı geçersiz, 18-65 geçerli, 65 üstü geçersiz. Her gruptan tek temsilci yeter; örneğin 12, 40 ve 70. Sınır değer analizi bunun üstüne oturur ve farklı bir soruyu sorar: hata nerede yoğunlaşır? Cevap kenardadır. Kodda `age >= 18` yerine `age > 18` yazılmışsa bu hata **yalnız** 18 değerinde görünür; 12 ve 40 ile test edenler onu asla yakalayamaz. Bu yüzden her sınırın iki komşusu denenir: 17 ile 18, 65 ile 66. B şıkkı yalnız geçerli ucu dener; operatör hatasının yarısını kaçırır. A şıkkı kuralın sınırıyla veri türünün sınırını karıştırır; 0 ve 99 zaten geçersiz sınıfın içindedir, sınır değil. C şıkkı tekniğin varlık sebebine aykırıdır; tam test imkânsızdır ve teknikler test sayısını azaltmak için vardır (`228`). Mülakatta bu örnek ezberlenecek kadar değerlidir: sayıyı söyleyerek anlatan aday, teknik adını sayan adaydan ayrılır (`234`).

# istqb-3 | Orta
## Soru
Bir kargo ücreti kuralı üç koşula bağlı: üyelik var mı, sepet tutarı eşiği aştı mı, kupon girildi mi. Bu kuralı sınamak için birinci seçenek hangi tekniktir?
## A
Karar tablosu (decision table); koşulların birleşimleri kural olarak çıkarılır ve her kural bir test olur
🤖 Doğru: iş kuralı koşul birleşimine bağlıysa tablo eksiği görünür kılar.
## B
Durum geçişi (state transition); üç koşul sistemin durumlarını tanımlar ve aralarındaki geçişler test edilir
🤖 burada geçmişe bağlı bir durum yok; koşullar eşzamanlı değerlendirilir.
## C
Sınır değer analizi; her koşulun sınırı ayrı ayrı test edilirse koşul birleşimleri de kapsanmış olur
🤖 sınır tek değişkeni sınar; birleşimdeki boşluğu göstermez.
## D
Hata tahmini (error guessing); deneyimli bir testçi bu tür kuralların nerede bozulduğunu zaten bilir
🤖 deneyim tekniği tamamlar, yerini almaz; sistematik kapsama vermez.
## Doğru
A
## Açıklama
Karar tablosu, çıktının **koşulların birleşimine** bağlı olduğu durumlar için tasarlanmıştır. Üç koşulun her biri iki değer alıyorsa tablo sekiz satır üretir; her satır bir kuraldır ve her kural bir testtir. Tekniğin asıl kazancı budur: kimsenin konuşmadığı birleşimi görünür kılar. "Üye değil, tutar eşiğin altında ama kuponlu" gibi bir satır çoğu ekipte tartışılmamıştır ve hata tam orada çıkar. B şıkkı yanlış tekniği seçer; durum geçişi, nesnenin tepkisi **geçmişine** bağlıysa kullanılır — sipariş "yeni, ödendi, kargoda" durumlarında gezerken olduğu gibi. Burada koşullar eşzamanlı değerlendirilir, sıralı bir durum yoktur. C şıkkı yanlış değil ama eksiktir: sepet tutarı eşiği için sınır değer analizi elbette gerekir ve karar tablosunu **tamamlar**; ancak tek başına birleşimleri kapsamaz. D şıkkı deneyimi tekniğin yerine koyar; hata tahmini boşluk tarar, sistematik kapsama üretmez (`234`). Mülakatta doğru cevabın gücü örnekten gelir: tabloyu sözle bir satır okuyabilen aday tekniği kullanmış sayılır.

# istqb-4 | Orta
## Soru
Bildirilen bir hata düzeltildi ve yeni yapı test ortamına geldi. Yeniden test (retest) ile gerileme testi (regression) arasındaki fark nedir?
## A
İkisi aynı işi anlatır; yeniden test manuel yapılanın, gerileme testi ise otomatik koşulanın adıdır
🤖 ayrım koşum biçiminde değil, sorulan soruda ve kapsamdadır.
## B
Yeniden test tüm kritik akışları kapsar, gerileme testi ise yalnız düzeltilen hatanın adımlarını tekrarlar
🤖 tanımlar ters verilmiş; iki kapsam yer değiştirmiş.
## C
Yeniden test hatanın düzeldiğini doğrular, gerileme testi düzeltmenin başka bir yeri bozmadığını arar
🤖 Doğru: biri düzeltmeye, diğeri yan etkiye bakar.
## D
Gerileme testi yalnız sürüm öncesinde yapılır, yeniden test ise her gece otomatik olarak koşturulur
🤖 zamanlama ekibin kararıdır; tanım farkı buradan gelmez.
## Doğru
C
## Açıklama
Bu iki terimi karıştırmak mülakatta klasik bir elenme sebebidir. **Yeniden test** tek bir soruya bakar: bildirdiğim hata gerçekten düzeldi mi? Kapsamı dardır; yalnız o hatanın yeniden üretim adımları koşulur ve çıktısı tek bir karardır. **Gerileme testi** başka bir soruya bakar: bu düzeltme çalışan bir yeri bozdu mu? Kapsamı geniştir; etkilenen alan artı kritik akışlar koşulur. Yeniden test atlanırsa kapatılmış görünen hata aslında açık kalır. Gerileme testi atlanırsa yan etki üretime kaçar. İkisi birbirinin yerine geçmez. A şıkkı ayrımı koşum biçimine bağlar; oysa her ikisi de manuel veya otomatik koşulabilir. Gerileme testinin otomasyonun ilk adayı olması bir eğilimdir, tanım değildir. B şıkkı tanımları ters verir. D şıkkı zamanlamayı tanım sanar; zamanlama ekibin kararıdır. Cevabın devamında iki şey daha söylenir: gerileme kapsamı rastgele değil değişim analiziyle seçilir (`229`), ve aynı set zamanla körelir, bu yüzden yenilenir (`187`).

# istqb-5 | İleri
## Soru
Bir gerileme seti aylardır yeşil geçiyor ve hiç yeni hata bulmuyor. Ekip "ürün olgunlaştı, hata kalmadı" diyor. Bu yorumun kusuru hangi ilkeyle en iyi açıklanır?
## A
Erken test ilkesi; setin geliştirme sürecinde çok daha erken koşulmaya başlanması gerekirdi
🤖 koşum zamanı sorunu değil; set aynı yerlere bakmayı sürdürüyor.
## B
Kusur kümelenmesi ilkesi; hatalar genellikle birkaç modülde toplanır ve öncelikle orada aranır
🤖 doğru bir ilke ama buradaki asıl neden setin körelmesidir.
## C
Bağlama bağlılık ilkesi; test yaklaşımı ürünün bağlamına göre değişmek zorundadır
🤖 genel olarak doğru; yeşil setin neden hata bulmadığını açıklamaz.
## D
Böcek ilacı paradoksu; aynı testler tekrarlandıkça yeni hata bulma gücünü yitirir
🤖 Doğru: set gözden geçirilmeli, yenilenmeli ve yeni tekniklerle beslenmelidir.
## Doğru
D
## Açıklama
Böcek ilacı paradoksu (pesticide paradox) şunu söyler: aynı testler tekrar tekrar koşulursa bir noktadan sonra yeni hata bulmaz olur. Böcekler o ilaca direnç kazanır. Sebep açıktır; o testlerin baktığı yerlerdeki hatalar zaten bulunmuş ve düzeltilmiştir. Set hiç değişmediği için yeni eklenen kod ve yeni birleşimler hiç sınanmaz. Çözüm seti gözden geçirmek, senaryo eklemek, işe yaramayanı çıkarmak ve keşifsel testle beslemektir (`234`). Ekibin yorumu ayrıca birinci ilkeyi de ihlal eder: **test hatanın varlığını gösterir, yokluğunu değil.** Yeşil bir set "hata yok" demez; yalnız "bu testler geçti" der (`187`). A şıkkı zamanlama sorunu görür; oysa set ne kadar erken koşulursa koşulsun aynı yerlere bakar. B şıkkı doğru bir ilkedir ve nereye bakılacağını söyler, ama setin neden körelmiş olduğunu açıklamaz. C şıkkı en genel ilkedir; buradaki somut mekanizmayı adlandırmaz. Mülakatta bu soru olgunluk ölçer: ilkeyi adlandırmak kadar, gerileme setini yenileme kararını söylemek de değerlidir.

# istqb-6 | İleri
## Soru
Görüşmeci soruyor: "ISTQB sertifikan var mı?" Sertifikan yok ve ilan sertifika istiyor. En güçlü cevap hangisidir?
## A
"Var" demek ve konu derinleşirse sertifikanın eski olduğunu söyleyip konuyu hızla değiştirmek
🤖 uydurmanın maliyeti asimetriktir; yakalanınca doğru cevaplara da güven kalmaz.
## B
Sertifikanın olmadığını söylemek, terminolojiye hâkimiyeti somut bir teknikle göstermek ve alma planını vermek
🤖 Doğru: sınır, köprü ve plan birlikte verilir.
## C
Yalnızca "hayır" deyip susmak; dürüst bir cevap kısa olduğunda her zaman en güçlü cevaptır
🤖 dürüst ama nötr; bildiğin terminoloji hiç görünmez.
## D
Sertifikanın gereksiz olduğunu, asıl belirleyici olanın saha deneyimi olduğunu ayrıntılı anlatmak
🤖 ilanın açık bir maddesini küçümsemek savunmacı okunur.
## Doğru
B
## Açıklama
Bu soru bilgi değil, iki şey ölçer: dürüstlük ve terminoloji hâkimiyeti. A şıkkı en pahalı seçenektir. Uydurmanın maliyeti asimetriktir; kazanılan şey tek bir soru, kaybedilen şey mülakatın tamamıdır. Kıdemli bir mülakatçı birkaç takip sorusuyla bunu yakalar ve o andan sonra doğru verdiğin cevaplara da güvenmez (`256`). C şıkkı zarar vermez ama nötrdür; dürüstlük tek başına bilgi üretmez ve bildiğin terminoloji hiç görünmez. D şıkkı ilanın açık bir maddesini küçümser; savunmacı okunur ve gereksiz yere karşıtlık üretir. Doğru yapı üç parçalıdır. **Sınır:** "Sertifikam yok." **Köprü:** terminolojiyi iddia ederek değil göstererek — "18-65 kabul eden bir alanda denklik sınıfları 18 altı, aralık ve 65 üstüdür; sınır değerler 17, 18, 65 ve 66'dır." **Plan:** hangi seviyeyle başlayacağını söylemek. Bir dördüncü parça daha vardır: kalibrasyon. Müfredat ayrıntısı sorulursa "güncel müfredattan teyit ederim" demek, uydurulmuş bir rakamdan güçlüdür. Bildiğini ve tahmin ettiğini ayırabilen aday, üretimde en az sürpriz çıkaran adaydır.
