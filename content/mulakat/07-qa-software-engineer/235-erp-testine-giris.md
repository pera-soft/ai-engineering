---
slug: erp-testine-giris
baslik: "ERP Testi Neden Farklıdır: Modüller, Ana Veri, Geri Alınamazlık ve Ortamlar"
kademe: Orta
sira: 235
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Küçük bir mahalle marketini düşünün. Kasadaki para çekmecesi, depodaki raflar ve tezgâhın altındaki veresiye defteri birbirinden bağımsızdır. Bir müşteri bir şey aldığında kasiyer parayı çekmeceye atar. Aynı anda raftan bir kutu eksilir. Müşteri borçluysa deftere bir satır yazılır. Bu üç kayıt akşam saymaya kalkınca genelde tutmaz. Market büyüyüp on şubeye, üç depoya ve bir web sitesine dönüştüğünde bu "üç ayrı defter" düzeni büsbütün çöker.

**ERP (Enterprise Resource Planning, kurumsal kaynak planlaması)** tam olarak bu problemin çözümüdür. Satın almadan depoya, satıştan muhasebeye kadar tüm iş süreçlerini **tek bir veri modelinde** birleştirir. Bir satış kaydedildiğinde stok, cari hesap ve muhasebe defteri aynı işlemin parçası olarak güncellenir. Üçü birbiriyle tutarlı kalır.

Bir e-ticaret firmasında QA olarak ERP'ye bakmak, alışılmış web testinden farklı bir zihin gerektirir. Sitede test ettiğiniz şey çoğunlukla **bir kullanıcının gördüğü ekrandır**. ERP'de test ettiğiniz şey ise **kurumun defterinin doğruluğudur**. Sitede yanlış bir filtre sonucu kullanıcıyı sinirlendirir. ERP'de yanlış bir stok hareketi ay sonunda envanter farkı çıkarır. Yanlış bir muhasebe fişi ise mali müşavirin düzeltmek zorunda kalacağı bir kayıt üretir. Bu yüzden ERP testinde "ekranda yeşil bir mesaj çıktı" bir kanıt değildir; olsa olsa bir başlangıçtır.

Firma **kendi ERP'sini sıfırdan yazacaksa** bu sizin için iyi haberdir. Test ettiğiniz şey sonuçta normal bir web uygulamasıdır ve mevcut otomasyon bilginiz doğrudan geçerlidir. Zorluk araçta değil, uygulamanın **iş alanındadır (domain)**. Bu iş alanı, normal bir web uygulamasınınkinden çok daha acımasızdır.

### Temel kavramlar

- **ERP modülleri.** Modüller, kurumun iş süreçlerinin yazılımdaki karşılıklarıdır. Tipik modüller şunlardır: **satın alma** (tedarikçiden mal alımı, sipariş ve mal kabul), **stok/depo** (envanter, depolar arası transfer, sayım), **satış ve sipariş yönetimi**, **sevkiyat** (toplama, paketleme, kargo), **finans/cari** (müşteri ve tedarikçi borç-alacak bakiyeleri, tahsilat, ödeme), **muhasebe** (hesap planı, yevmiye fişleri, dönem), **fiyatlandırma ve kampanya** (fiyat listeleri, iskonto kuralları), **raporlama**. Bu modüller ayrı ekranlar gibi görünür. Oysa hepsi **aynı veriyi paylaşır** — ERP'yi ERP yapan da budur.
- **Sistem sınırı (site ile ERP arasındaki hat).** Bu sınır, "hangi verinin **sahibi** kim" sorusudur. Tipik bir e-ticaret kurulumunda ürün kartı, fiyat listesi ve stok **ERP'de** doğar; site bunların bir **kopyasını** gösterir. Sepet, oturum, arama ve öneri **sitede** yaşar. ERP bunları bilmez, bilmesine de gerek yoktur. Sipariş ise **sitede oluşur ve ERP'ye aktarılır**. O andan sonra hayatının geri kalanını ERP'de yaşar. QA'in ilk sorusu şudur: "Bu alanın **tek doğruluk kaynağı (single source of truth)** neresi?" Çünkü iki yerde tutulan her veri, er ya da geç iki yerde farklı olur.
- **Ana veri (master data) ve hareket verisi (transaction data).** Ana veri, işlemlerin üzerine kurulduğu **uzun ömürlü tanım** verisidir. Örnekleri: ürün kartı, cari (müşteri/tedarikçi) kartı, depo tanımı, fiyat listesi, KDV oranı tanımı, hesap planı, ödeme koşulu, kargo firması, birim (adet/koli). Hareket verisi ise **bir olayı** kaydeder: sipariş, irsaliye, fatura, stok hareketi, tahsilat, muhasebe fişi. Ana veri nispeten az değişir ama her şeyi etkiler. Hareket verisi ise sürekli üretilir ama tekildir. Bir ana veri hatası — örneğin yanlış tanımlanmış bir KDV oranı — bin faturayı birden bozar. Bir hareket verisi hatası yalnız tek bir belgeyi bozar.
- **Belge zinciri (document flow).** ERP'de belgeler birbirinden türer: sipariş → irsaliye → fatura → tahsilat. Her belge bir öncekine **referans** verir. Bu zincir, "bu faturanın kaynağı hangi siparişti" sorusunun cevabını verir. ERP testinde en çok bakılan yapı da budur.
- **Geri alınamazlık (irreversibility) ve ters kayıt.** Muhasebe ve fatura kayıtları **silinmez**. Yanlış kesilen bir fatura, kaydı yok edilerek düzeltilmez. Bunun yerine fatura **iptal edilir**, **iade faturası** kesilir ya da muhasebede **ters kayıt (storno)** atılır. Sebebi mevzuat ve denetlenebilirliktir: silinebilen bir defter, defter değildir. Testteki sonucu şudur — testleriniz kalıcı iz bırakır. "Temizleyip baştan koşmak" ERP'de web uygulamasındaki kadar kolay değildir.
- **Denetim izi (audit trail).** Denetim izi, kaydı kimin, ne zaman, hangi değerden hangi değere çevirdiğinin saklanmasıdır. ERP'de bu bir "güzel olsa iyi olur" özelliği değildir; **test edilmesi gereken bir gereksinimdir**. Yetkisiz bir kullanıcı fiyat değiştirebiliyorsa veya değiştirdiği iz bırakmıyorsa bu bir hatadır.
- **Yetki ve görev ayrılığı (segregation of duties).** Siparişi giren kişi ile onu onaylayan kişi aynı olmamalıdır. Aynı şekilde faturayı kesen ile tahsilatı işleyen kişi de ayrı olmalıdır. ERP'de yetki testi "menü görünüyor mu" testi değildir. Asıl test şudur: **yetkisiz kullanıcı doğrudan istek atarak işlemi yapabiliyor mu?**
- **Muhasebe dönemi ve tarih.** Her kaydın bir **belge tarihi** ve bir **muhasebeleştirme tarihi** vardır. Kayıt bir **döneme** (genelde bir aya) düşer. Dönem kapatıldıktan sonra o döneme kayıt atılamaz. Testte bu iki soruyu doğurur: "Ayın son günü saat 23:55'te girilen sipariş hangi döneme düşüyor?" ve "Kapalı döneme iade geldiğinde ne oluyor?"
- **Ortamlar.** **DEV** (geliştirme), **TEST** (QA'in koştuğu), **UAT** (kullanıcı kabul), **PROD** (canlı). ERP'de bu ayrım diğer uygulamalardan daha katıdır. Sebebi basittir: PROD'da yapılan bir deneme gerçek bir fatura üretebilir.
- **Veri maskeleme (data masking) ve KVKK.** Üretim verisinin test ortamına kopyalanması ERP testinde çok değerlidir, çünkü gerçek verinin çeşitliliğini getirir. Ama kişisel veri (ad, adres, telefon, TCKN) **maskelenmeden** taşınamaz. Bu, KVKK kapsamında bir uyum zorunluluğudur; tercih değildir.
- **Ortam yenileme (refresh).** Test/UAT ortamının, maskelenmiş güncel üretim verisiyle belirli aralıklarla yeniden kurulması. ERP testinde birikmiş çöp veriyi temizlemenin pratikte tek gerçekçi yolu çoğu zaman budur.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="E-ticaret sitesi ile ERP arasindaki sinir ve ERP modulleri">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sınır sorusu: bu verinin tek doğruluk kaynağı neresi?</text>
  <rect x="20" y="38" width="176" height="228" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="108" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">E-ticaret sitesi</text>
  <text x="108" y="82" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sepet (yalnız burada)</text>
  <text x="108" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üyelik / oturum</text>
  <text x="108" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">arama, filtre, öneri</text>
  <text x="108" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">vitrin ve kampanya ekranı</text>
  <text x="108" y="168" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ürün + fiyat: KOPYA</text>
  <text x="108" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">stok: KOPYA (gecikmeli)</text>
  <text x="108" y="214" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">sipariş burada DOĞAR</text>
  <text x="108" y="234" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ama burada yaşamaz</text>
  <line x1="216" y1="50" x2="216" y2="266" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="5 4"/>
  <text x="216" y="286" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">entegrasyon sınırı</text>
  <text x="266" y="112" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">sipariş →</text>
  <line x1="230" y1="122" x2="302" y2="122" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="266" y="176" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">← ürün/fiyat/stok</text>
  <line x1="302" y1="186" x2="230" y2="186" stroke="#25804C" stroke-width="1.8"/>
  <rect x="312" y="38" width="328" height="228" rx="10" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.8"/>
  <text x="476" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">ERP — tek veri modeli, tek defter</text>
  <rect x="326" y="72" width="148" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="400" y="93" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Satın alma</text>
  <rect x="482" y="72" width="146" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="555" y="93" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Stok / Depo</text>
  <rect x="326" y="114" width="148" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="400" y="135" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Satış ve sipariş</text>
  <rect x="482" y="114" width="146" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="555" y="135" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Sevkiyat / kargo</text>
  <rect x="326" y="156" width="148" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="400" y="177" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Finans / cari</text>
  <rect x="482" y="156" width="146" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="555" y="177" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Muhasebe</text>
  <rect x="326" y="198" width="148" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="400" y="219" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Fiyat / kampanya</text>
  <rect x="482" y="198" width="146" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="555" y="219" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Raporlama</text>
  <text x="476" y="252" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">ayrı ekranlar, AYNI veri — bir satış üçünü birden değiştirir</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

1. **Süreç haritasını çıkarın, ekran listesini değil.** ERP'de test edilebilir birim bir ekran değil bir **süreçtir**: "müşteri sipariş verir → depo toplar → sevk edilir → fatura kesilir → tahsil edilir". Haritayı çıkarırken her adımda üç soruyu yazın: bu adımı **kim** (hangi rol) yapar, bu adımda **hangi belge** doğar, bu adım **hangi kayıtları** değiştirir.
2. **Ana veri setini bir test varlığı olarak kurun.** ERP testinde işin gerçek maliyeti buradadır. Yeni başlayanları en çok şaşırtan şey de budur: **tek bir sipariş testi için yarım düzine ana veri gerekir.** Basit bir "sepete ürün ekle, sipariş ver" senaryosunu koşabilmek için önce şunların var olması gerekir. (a) Bir **ürün kartı**: stok kodu, birim, KDV oranı, ağırlık/desi. (b) O ürünün bulunduğu bir **depo** ve o depoda **yeterli stok**. (c) Ürünün yer aldığı bir **fiyat listesi** ve geçerli bir fiyat kaydı. (d) Bir **cari kart** (müşteri) ve ona bağlı **fatura ile teslimat adresi**. (e) Bir **ödeme koşulu** ve bir tahsilat yöntemi. (f) Muhasebe tarafında ürünün ve carinin bağlandığı **hesap planı** kayıtları. (g) Sevkiyat için bir **kargo/taşıyıcı tanımı**. Bunlardan biri eksik veya yanlışsa test düşer. Düşen test "hata buldum" gibi görünür, oysa asıl sebep **veri hazırlığı eksikliğidir**. Bu yüzden olgun ERP test ekipleri ana veriyi ekran ekran elle girmez. Ana veriyi **veri hazırlama betikleri veya kurulum uçlarıyla (setup API)** üretirler; senaryonun kendisi yalnız hareket verisine odaklanır.
3. **Rol bazlı test kullanıcıları hazırlayın.** ERP süreçleri çok rollüdür: satış temsilcisi siparişi girer, depo görevlisi toplama listesini kapatır, muhasebeci faturayı keser. Bir uçtan uca senaryo bu rollerin **hepsini** dolaşmak zorundadır. Tek bir "her yetkiye sahip yönetici" kullanıcısıyla koşulan test, süreci test etmiş olmaz. Üstelik böyle bir test yetki hatalarını da gizler.
4. **Doğrulamayı veri katmanına indirin.** Ekranda "Sipariş başarıyla oluşturuldu" yazması bir kanıt değildir. Bu mesaj; arka planda stok hareketinin oluştuğunu, cari bakiyenin değiştiğini veya muhasebe fişinin dengeli olduğunu göstermez. ERP'de **doğrulama (assertion)** ekrandan **bir kat aşağıda** yapılır: veritabanı sorgusuyla, ERP'nin kendi standart raporuyla ya da modülün sorgulama ucuyla (API). Bir örnek:

```sql
-- Sipariş kaydedildikten sonra "ekranda yazan" değil, "defterde oluşan" doğrulanır
SELECT s.siparis_no,
       s.toplam_tutar,
       (SELECT COALESCE(SUM(sh.miktar), 0) FROM stok_hareket sh
          WHERE sh.kaynak_belge = s.siparis_no)          AS stok_hareket_toplami,
       (SELECT COALESCE(SUM(mf.borc), 0) FROM muhasebe_fis_satir mf
          WHERE mf.belge_no = s.siparis_no)              AS fis_borc,
       (SELECT COALESCE(SUM(mf.alacak), 0) FROM muhasebe_fis_satir mf
          WHERE mf.belge_no = s.siparis_no)              AS fis_alacak
FROM siparis s
WHERE s.siparis_no = :siparisNo;
-- Beklenen: stok_hareket_toplami sipariş satır adetleriyle eşit, fis_borc = fis_alacak
```

5. **Geri alınamazlıkla baş edin, onunla savaşmayın.** Bir web uygulamasında testler arası izolasyon genelde "veritabanını geri sar" veya "kaydı sil" ile çözülür (bu konu `203`'te işlendi). ERP'de bu yol büyük ölçüde kapalıdır: kesilen fatura silinmez, atılan muhasebe fişi yok edilmez, belge numaraları geri alınmaz. Pratik strateji üç ayaklıdır. (a) **Her koşum kendi verisini üretir**: rastgele son ekli ürün kodları, koşuma özel cari kartlar, ayrılmış bir test deposu. Böylece koşumlar birbirinin bakiyesini bozmaz. (b) **Belgeyi silmek yerine sürecin kendi geri alma yolunu kullanın**: iptal, iade faturası, ters kayıt. Bunlar zaten test edilmesi gereken davranışlardır; böylece testin temizliğini gerçek bir senaryoya dönüştürmüş olursunuz. (c) **Ortam yenileme takvimi kurun**: biriken çöp veriyi belirli aralıklarla maskelenmiş üretim kopyasıyla sıfırlayın. Veritabanından doğrudan `DELETE` atan bir "temizlik betiği" cazip görünür ama iki nedenle tehlikelidir. Birincisi, denetim izini bozar. İkincisi, **üretimde asla mümkün olmayan bir yolu** test ortamının normali hâline getirir; testleriniz gerçek hayatta bulunmayan bir başlangıç durumuna alışır.
6. **Ortamları ciddiye alın.** DEV, geliştiricinin bozup düzelttiği yerdir. TEST, QA'in koştuğu yerdir; bu yüzden **kararlı olmak zorundadır**. UAT, iş kullanıcısının kabul için girdiği ortamdır ve **üretime en çok benzemesi gereken** yer burasıdır. PROD ise canlıdır; orada test yapılmaz. Kritik nokta şudur: **üretim benzeri veri olmadan ERP testi yanıltıcıdır.** 200 ürünlü, 30 carili bir test ortamında her rapor hızlı çalışır, her arama isabetli döner ve hiçbir aykırı ana veri yoktur. Üretim ise böyle değildir: 400 bin ürün, 5 yıllık hareket, yarısı eksik doldurulmuş cari kartlar, birimi "koli" olan ürünler ve KDV oranı sonradan değişmiş kalemler vardır. Bu yüzden test/UAT ortamı düzenli olarak **maskelenmiş** üretim kopyasıyla yenilenir. Maskelemede ad-soyad, adres, telefon, kimlik ve kart bilgisi dönüştürülür. Tutar, tarih, miktar ve ilişkiler ise **korunur**; çünkü testin ihtiyacı olan şey tam da bu yapısal gerçekliktir. "Prod'dan olduğu gibi kopyalayalım, nasılsa iç ortam" cümlesi bir kolaylık değil, KVKK açısından bir uyum ihlalidir.
7. **UAT'yi organize edin, sahiplenmeyin.** UAT'nin tanımı `228`'de verildi. Buradaki asıl mesele, ERP'de UAT'nin neden **merkezî** olduğudur. Sebebi şudur: ERP'nin doğruluk ölçütü yazılı gereksinim değil, **kurumun gerçekte nasıl çalıştığıdır**. Bir iskonto sırasının doğru olup olmadığını en iyi satın almacı bilir. Bir stok hareketinin mantıklı olup olmadığını depo şefi, bir muhasebe fişinin doğru hesaba düştüğünü ise muhasebeci bilir. QA bu kişilerin yerine geçemez. QA'in rolü şudur: **senaryoyu yazmak, veriyi hazırlamak, kullanıcıya koşturacak sağlam bir ortam vermek ve çıkan bulguları triyaj edip gerçek hatayı yanlış anlaşılmadan ayırmak.** "QA regresyonu geçti, o hâlde kabul edildi" cümlesi ERP'de yanlıştır; kabul kararını süreci yaşayan kullanıcı verir.
8. **İş kurallarını öğrenin; bu isteğe bağlı değildir.** ERP'de test yazabilmek için birkaç kuralı bilmek şarttır. İskonto sırası nedir — satır iskontosu mu önce gelir, kampanya iskontosu mu; vade farkı nereye girer? KDV nasıl hesaplanır? Stok rezervasyonu nasıl çalışır; sipariş verildiğinde stok düşer mi, yoksa yalnız rezerve mi edilir? Cari bakiyenin işareti ne anlama gelir? Dönem kavramı neyi kısıtlar? Bunları bilmeyen bir testçi yalnızca **ekranın çökmediğini** test edebilir. Mülakatta en güçlü cümlelerden biri şudur: "İşin kuralını bilmeden beklenen sonucu yazamam; bu yüzden senaryoyu yazmadan önce süreci sahibiyle konuşurum."

```svg
<svg viewBox="0 0 660 290" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek bir siparis testi icin gereken ana veri agaci">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Bir tek sipariş testinin görünmeyen maliyeti: ana veri</text>
  <rect x="238" y="34" width="184" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.7"/>
  <text x="330" y="52" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">HAREKET VERİSİ</text>
  <text x="330" y="70" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">1 adet sipariş (test etmek istediğim)</text>
  <line x1="330" y1="78" x2="330" y2="98" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="112" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">önce şunların HEPSİ var olmalı:</text>
  <rect x="18" y="124" width="150" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="93" y="144" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Ürün kartı</text>
  <text x="93" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kod, birim, KDV, desi</text>
  <rect x="178" y="124" width="150" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="253" y="144" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Depo + stok</text>
  <text x="253" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yeterli miktar olmalı</text>
  <rect x="338" y="124" width="150" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="413" y="144" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Fiyat listesi</text>
  <text x="413" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">geçerli tarihli fiyat</text>
  <rect x="498" y="124" width="144" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="570" y="144" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Cari kart</text>
  <text x="570" y="162" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">müşteri + adresler</text>
  <rect x="18" y="188" width="150" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="93" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Ödeme koşulu</text>
  <text x="93" y="226" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">vade, tahsilat yolu</text>
  <rect x="178" y="188" width="150" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="253" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Hesap planı</text>
  <text x="253" y="226" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ürün/cari bağlantısı</text>
  <rect x="338" y="188" width="150" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="413" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kargo tanımı</text>
  <text x="413" y="226" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">taşıyıcı, teslim süresi</text>
  <rect x="498" y="188" width="144" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="570" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Rol / yetki</text>
  <text x="570" y="226" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">satış, depo, muhasebe</text>
  <rect x="60" y="252" width="540" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="272" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Eksik ana veri = "hata" gibi görünen sahte başarısızlık. Bu yüzden ana veri betikle üretilir, ekrandan girilmez.</text>
</svg>
```

**Paket ERP ile kendi yazılan ERP: test yaklaşımı farkı.** Piyasada SAP, Logo (Tiger/Netsis), Nebim V3, Mikro, Microsoft Dynamics gibi hazır ERP ürünleri vardır. Bunlarda testin odağı satıcının çekirdek kodu **değildir**; çekirdeği satıcı test eder. Siz **konfigürasyonu** (parametreler, iş akışı ayarları), **özelleştirmeleri** (kuruma özel eklenen kod ve ekranlar) ve **entegrasyonları** test edersiniz. Üstelik bu ürünlerin arayüzleri — özellikle klasik masaüstü istemcileri — standart web otomasyon araçlarıyla sürülemez. Bu yüzden ayrı ve pahalı araçlar gündeme gelir.

Sizin firmanızın durumu bunun tersidir. **Kendi yazılan (in-house) bir ERP, sonuçta bir web uygulamasıdır**: HTTP API'leri, HTML arayüzü ve kendi veritabanı vardır. Arayüzünü Playwright/Selenium ile, servislerini REST Assured/Postman ile test edebilirsiniz. Kodun tamamı sizin sorumluluğunuzdadır. Bu yüzden birim testinden entegrasyon testine, oradan uçtan uca teste kadar bilinen test piramidi (bkz. `187`) doğrudan uygulanır. Yani araç bilginiz aynen geçerlidir. Öğrenmeniz gereken şey araç değil, **iş alanıdır**.

```svg
<svg viewBox="0 0 660 264" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ortamlar DEV TEST UAT PROD ve maskelenmis veri akisi">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kod ileri gider, veri geri gelir — ama maskelenerek</text>
  <rect x="20" y="42" width="140" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">DEV</text>
  <text x="90" y="86" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">geliştirici bozar</text>
  <text x="90" y="100" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">az ve yapay veri</text>
  <rect x="180" y="42" width="140" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="250" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">TEST</text>
  <text x="250" y="86" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">QA koşar, kararlı olmalı</text>
  <text x="250" y="100" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">otomasyon burada</text>
  <rect x="340" y="42" width="140" height="66" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="410" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">UAT</text>
  <text x="410" y="86" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">iş kullanıcısı kabul eder</text>
  <text x="410" y="100" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">PROD'a en çok benzeyen</text>
  <rect x="500" y="42" width="140" height="66" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.7"/>
  <text x="570" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">PROD</text>
  <text x="570" y="86" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gerçek para, gerçek fatura</text>
  <text x="570" y="100" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">burada test yapılmaz</text>
  <line x1="160" y1="75" x2="178" y2="75" stroke="#25804C" stroke-width="2"/>
  <line x1="320" y1="75" x2="338" y2="75" stroke="#25804C" stroke-width="2"/>
  <line x1="480" y1="75" x2="498" y2="75" stroke="#25804C" stroke-width="2"/>
  <text x="330" y="132" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">↑ kod ve konfigürasyon bu yönde ilerler</text>
  <line x1="560" y1="152" x2="220" y2="152" stroke="#186399" stroke-width="2" stroke-dasharray="6 4"/>
  <text x="390" y="146" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">↓ ortam yenileme: veri bu yönde kopyalanır</text>
  <rect x="20" y="168" width="300" height="80" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="170" y="188" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Maskelenir (KVKK)</text>
  <text x="170" y="208" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ad-soyad, adres, telefon, e-posta</text>
  <text x="170" y="224" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kimlik no, kart bilgisi</text>
  <text x="170" y="240" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gerçek kişi tanınabilir kalmamalı</text>
  <rect x="340" y="168" width="300" height="80" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="490" y="188" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Korunur (testin değeri burada)</text>
  <text x="490" y="208" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tutar, miktar, tarih, dönem</text>
  <text x="490" y="224" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ilişkiler, aykırı ana veri, hacim</text>
  <text x="490" y="240" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yapı bozulursa kopya işe yaramaz</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"ERP testi normal web testinden nasıl ayrılır?"** Beklenen cevap beş başlık içerir. (1) Doğrulama ekranda değil **veri katmanındadır**. (2) İşlemlerin çoğu **geri alınamaz**; düzeltme silme ile değil, iptal/iade/ters kayıtla yapılır. (3) Süreçler **çok adımlı ve çok rollüdür**, tek kullanıcıyla test edilemez. (4) **Denetim izi ve yetki** testin konusudur, süsü değil. (5) **Dönem/tarih** kavramı vardır; aynı işlem farklı tarihte farklı sonuç doğurur. "Aslında aynı şey, sadece daha büyük bir uygulama" derseniz ERP'yi hiç görmediğiniz anlaşılır.
- **"Bir sipariş testi yazacaksın, ilk işin ne?"** Tuzak, doğrudan "sepete ürün ekle adımını yazarım" demektir. Beklenen: önce **ana veri hazırlığı** — ürün, stok, fiyat, cari, ödeme koşulu, roller. ERP testinde işin yarısı senaryoyu koşulabilir hâle getirmektir.
- **"Test verisini nasıl temizliyorsun?"** Bu soru bir tuzaktır. "Veritabanından siliyorum" cevabı ERP bağlamında zayıftır. Fatura ve muhasebe kaydı silinemez, denetim izi bozulur ve üretimde imkânsız bir yol test ortamının normali hâline gelir. Beklenen cevap üç parçalıdır: **her koşum kendi verisini üretir**; geri alma için sürecin kendi mekanizması (iptal/iade/ters kayıt) kullanılır; biriken veri **ortam yenilemesiyle** sıfırlanır.
- **"Prod verisini test ortamına alır mısın?"** Beklenen: evet, çünkü üretim benzeri veri olmadan ERP testi yanıltıcıdır — ama **maskeleyerek**. Kişisel veri dönüştürülür, tutar/tarih/ilişki/hacim korunur. "Nasılsa iç ağ, olduğu gibi alalım" cevabı KVKK açısından yanlıştır ve mülakatta doğrudan olumsuz not aldırır.
- **"Paket ERP mi test ettin, kendi yazdığınızı mı?"** Ayrımı bilmek beklenir. Pakette satıcının çekirdeği değil, **konfigürasyon + özelleştirme + entegrasyon** test edilir; ayrıca arayüz otomasyonu zordur. Kendi yazılan ERP ise normal bir web uygulamasıdır; bilinen test piramidi ve mevcut araçlar doğrudan geçerlidir.
- **"UAT'yi sen mi yapıyorsun?"** Doğru cevap "hayır, ben organize ediyorum"dur. Kabul kararını muhasebeci, depocu, satın almacı verir; QA senaryoyu yazar, veriyi hazırlar, ortamı ayakta tutar, bulguları triyaj eder (hata mı, yanlış anlama mı, gereksinim değişikliği mi). "QA regresyonu geçtiyse kabul de geçmiştir" demek ERP'de yanlıştır.
- **"İş kurallarını bilmiyorsan ne yaparsın?"** Beklenen: beklenen sonucu **uydurmam**. Süreç sahibine sorarım, kuralı yazılı hâle getiririm, senaryoya kaynak olarak eklerim. ERP'de "beklenen sonuç" bilinmeden yazılan test, yalnızca uygulamanın çökmediğini ölçer.
- **"Ekranda başarı mesajı çıktı, test geçti mi?"** Tek başına hayır. Geçti diyebilmek için arka planda oluşması gerekenlerin (stok hareketi, cari bakiye, muhasebe fişi, belge numarası) doğrulanması gerekir — bu zincirin detayı bir sonraki konunun (`236`) konusudur.

### Tek cümlede

ERP, kurumun tüm süreçlerini tek veri modelinde birleştiren "kurumun defteri"dir; onu test etmek ekran doğrulamak değil, **ana veriyi kurmak, çok rollü süreci uçtan uca yürütmek ve sonucu veri katmanında kanıtlamak** demektir. Kayıtların çoğu geri alınamaz olduğu için veri hazırlığı, ortam ve maskeleme stratejisi de bu işin ayrılmaz parçasıdır.

# erp-1 | Temel
## Soru
Bir ERP'de aşağıdakilerden hangisi **ana veri (master data)** ile **hareket verisi (transaction data)** ayrımını doğru tarif eder?
## A
Ana veri geçmiş yıllara ait arşiv kayıtlarıdır; hareket verisi ise yalnız içinde bulunulan aya ait kayıtlardır
🤖 ayrım zamana göre değil, verinin rolüne göredir; ana veri de hareket verisi de eski veya yeni olabilir.
## B
Ana veri her işlemde yeniden üretilen değişken veridir; hareket verisi ise kurulumda bir kez girilip bir daha değişmeyen sabit veridir
🤖 tanım tam ters çevrilmiş — üretilen ve çoğalan hareket verisi, nispeten kalıcı olan ana veridir.
## C
Ana veri ürün kartı, cari kart, depo, fiyat listesi ve hesap planı gibi tanımlardır; hareket verisi sipariş, fatura, stok hareketi ve tahsilat gibi olay kayıtlarıdır
🤖 Doğru: ana veri işlemlerin üzerine kurulduğu uzun ömürlü tanımlar, hareket verisi bu tanımlar üzerinde gerçekleşen olaylardır.
## D
Ana veri yalnız muhasebe modülünde bulunur; diğer modüllerdeki her kayıt tanımı gereği hareket verisidir
🤖 ürün, depo ve fiyat listesi gibi ana veriler stok ve satış modüllerinde de yaşar, muhasebeye özgü değildir.
## Doğru
C
## Açıklama
Ana veri, işlemlerin üzerine kurulduğu uzun ömürlü tanımlardır: ürün kartı, cari (müşteri/tedarikçi) kartı, depo, fiyat listesi, KDV oranı tanımı, hesap planı. Hareket verisi ise bu tanımlar üzerinde gerçekleşen tekil olayları kaydeder: sipariş, irsaliye, fatura, stok hareketi, tahsilat, muhasebe fişi. Ayrım zamansal değildir (A yanlış) ve yönü ters çevrilemez (B yanlış): ana veri az değişir ama çok şeyi etkiler, hareket verisi sürekli üretilir ama tekildir. Ana veri hiçbir modülün tekelinde değildir (D yanlış); tam tersine modüller arasında paylaşıldığı için ERP'yi bütünleşik kılan şey odur. Bu ayrım test için kritiktir: bir ana veri hatası bin belgeyi birden bozarken, bir hareket verisi hatası tek belgeyi etkiler.

# erp-2 | Orta
## Soru
Kendi yazılan ERP'de bir satış siparişi kaydediliyor ve ekranda "Sipariş başarıyla oluşturuldu" mesajı çıkıyor. Deneyimli bir QA neden bu mesajı testin geçtiğine dair yeterli kanıt saymaz?
## A
Çünkü ERP'de asıl doğrulama veri katmanındadır: stok hareketinin, cari bakiyenin ve muhasebe fişinin gerçekten ve tutarlı biçimde oluştuğu ayrıca kontrol edilmelidir
🤖 Doğru: ekran mesajı yalnız isteğin kabul edildiğini gösterir, defterde doğru kayıtların oluştuğunu göstermez.
## B
Çünkü başarı mesajlarının metni sürümden sürüme değişebilir; bu yüzden mesaj yerine ekran görüntüsü karşılaştırması yapılmalıdır
🤖 sorun mesajın metni değil, mesajın arka plandaki kayıtlar hakkında hiçbir şey söylememesidir.
## C
Çünkü ERP ekranları yavaş açılır; mesajı görmek yerine sayfa yükleme süresini ölçmek daha güvenilir bir doğrulamadır
🤖 süre ölçümü performansla ilgilidir, işlevsel doğruluğun kanıtı olamaz.
## D
Çünkü ERP'de hiçbir arayüz doğrulaması anlamlı değildir; testler yalnız veritabanına doğrudan yazıp okuyarak yapılmalıdır
🤖 arayüz doğrulaması değersiz değildir, tek başına yetersizdir; uygulamayı atlayıp veritabanına yazmak ise iş kurallarını hiç test etmemek olur.
## Doğru
A
## Açıklama
Ekrandaki başarı mesajı, isteğin uygulama tarafından kabul edildiğini gösterir; siparişin arkasında oluşması gereken kayıtların doğruluğunu göstermez. ERP'de bir satış kaydedildiğinde stok hareketi (veya rezervasyon), cari bakiye değişimi, muhasebe fişi ve belge numarası birlikte doğmalıdır — bunların biri eksik veya tutarsızsa ekran yine "başarılı" der. Bu yüzden ERP'de assertion bir kat aşağıda, veritabanı sorgusu, standart rapor veya sorgulama uç noktası üzerinden yapılır. Mesaj metninin değişkenliği (B) ya da sayfa süresi (C) bu sorunun konusu değildir. Öte yandan çözüm arayüzü tamamen atlamak da değildir (D): uygulamayı baypas edip veritabanına doğrudan yazmak, test edilmesi gereken iş kurallarını hiç çalıştırmamak anlamına gelir.

# erp-3 | Orta
## Soru
Firma hazır bir paket ERP almak yerine kendi ERP'sini sıfırdan yazmaya karar verdi. Bu kararın QA'in test yaklaşımına etkisi en doğru biçimde nasıl özetlenir?
## A
Kod kurum içinde yazıldığı için geliştiricinin birim testleri yeterlidir; ayrı bir QA otomasyonuna gerek kalmaz
🤖 kodun kime ait olduğu test ihtiyacını azaltmaz; kendi yazılan sistemde hata sorumluluğu tümüyle kuruma geçtiği için test ihtiyacı artar.
## B
Paket ERP'de test hiç yapılmaz çünkü satıcı ürünü zaten test etmiştir; kendi yazılan üründe ise yalnız arayüz testi yapılır
🤖 pakette de konfigürasyon, özelleştirme ve entegrasyon mutlaka test edilir; kendi yazılanda ise test arayüzle sınırlı kalamaz.
## C
Kendi yazılan bir ERP ancak paket ERP'lere özel ticari test araçlarıyla otomatikleştirilebilir; mevcut web otomasyon bilgisi geçersizdir
🤖 o araçlar paket ürünlerin kendine özgü arayüzleri için vardır; kendi yazılan web tabanlı ERP standart araçlarla sürülür.
## D
Pakette satıcının çekirdeği değil konfigürasyon, özelleştirme ve entegrasyonlar test edilir; kendi yazılan ERP ise bir web uygulamasıdır, bilinen araçlar doğrudan geçerlidir
🤖 Doğru: iki dünyanın farkı test edilen yüzeyde ve araç erişilebilirliğindedir; kendi yazılan üründe kapsam genişler ama araçlar tanıdıktır.
## Doğru
D
## Açıklama
Paket ERP'lerde (SAP, Logo, Nebim, Mikro, Dynamics gibi) satıcının çekirdek kodunu test etmek müşterinin işi değildir; test edilen şey kuruma özgü **konfigürasyon**, eklenen **özelleştirmeler** ve dış sistemlerle **entegrasyonlardır** — üstelik bu ürünlerin klasik arayüzleri standart web otomasyon araçlarıyla sürülemediği için ayrı ve pahalı araçlar gerekir. Kendi yazılan bir ERP'de ise durum tersine döner: kodun tamamı kurumun sorumluluğundadır, yani test kapsamı genişler, ama ürün sonuçta HTTP API'leri ve HTML arayüzü olan bir web uygulamasıdır; bilinen test piramidi (`187`) ve mevcut araçlar doğrudan uygulanır. Kod kurum içinde yazıldı diye QA otomasyonundan vazgeçmek (A) veya paket üründe hiç test yapılmadığını sanmak (B) yaygın ama yanlış inanışlardır; kendi yazılan bir web uygulaması için paket-ERP'ye özel araçlar aramak da (C) gereksizdir.

# erp-4 | İleri
## Soru
ERP regresyon paketi ilk koşumda yeşil, aynı gün ikinci koşumda ise kırmızı dönüyor: birinci koşumda oluşturulan siparişler, faturalar ve stok hareketleri ortamda kalmış, cari bakiyeler ve depo miktarları kaymış durumda. Faturalar ve muhasebe fişleri silinemiyor. Bu ortamda tekrar çalıştırılabilirliği sağlamanın en sağlıklı yolu hangisidir?
## A
Koşum sonunda veritabanına doğrudan `DELETE` atan bir temizlik betiği yazmak ve fatura ile muhasebe fişi tablolarını boşaltmak
🤖 denetim izini bozar ve üretimde asla mümkün olmayan bir yolu test ortamının normali hâline getirir.
## B
Her koşumun kendi verisini benzersiz anahtarlarla üretmesi, geri almayı sürecin kendi yoluyla (iptal, iade, ters kayıt) yapmak, birikimi ortam yenilemesiyle sıfırlamak
🤖 Doğru: izolasyonu veri üretimiyle sağlar, geri almayı gerçek iş akışıyla yapar ve birikimi ortam yenilemesine bırakır.
## C
Regresyon paketini günde yalnız bir kez, sabah ortam yeniden kurulduktan hemen sonra çalıştırmak ve gün içinde bir daha koşmamak
🤖 koşum sıklığını kısıtlamak sorunu çözmez, yalnız geri bildirim döngüsünü yavaşlatarak saklar.
## D
Testleri hep aynı sabit ürün ve cari kartları üzerinde çalıştırıp beklenen bakiyeleri her koşumda testin içinde güncellemek
🤖 paylaşılan sabit kayıtlar koşumları birbirine bağımlı kılar ve beklenen değeri sürekli güncellemek testi doğrulama aracı olmaktan çıkarır.
## Doğru
B
## Açıklama
ERP'de test izolasyonunun web uygulamalarındaki klasik çözümü (kaydı sil, veritabanını geri sar — bkz. `203`) büyük ölçüde kapalıdır, çünkü fatura ve muhasebe kayıtları mevzuat gereği silinmez. Sağlıklı strateji üç ayaklıdır: her koşum **kendi verisini** üretir (koşuma özel ürün kodları, cari kartlar, ayrılmış test deposu), böylece koşumlar birbirinin bakiyesini bozmaz; geri alma gerektiğinde **sürecin kendi yolu** kullanılır (iptal, iade, ters kayıt) — bu zaten test edilmesi gereken bir davranış olduğu için temizlik gerçek bir senaryoya dönüşür; biriken veri ise **ortam yenilemesiyle** periyodik olarak sıfırlanır. Doğrudan `DELETE` atmak (A) denetim izini bozar ve üretimde bulunmayan bir başlangıç durumuna alışmış testler üretir. Koşum sıklığını kısmak (C) sorunu gizler. Paylaşılan sabit kayıtlar üzerinde beklenen değerleri sürekli güncellemek (D) ise testi, doğruluğu ölçen bir araç olmaktan çıkarıp mevcut durumu onaylayan bir aynaya çevirir.

# erp-5 | İleri
## Soru
Test ortamında 200 ürün, 30 cari ve birkaç günlük hareket verisi var; üretimde ise 400 bin ürün, on binlerce cari ve beş yıllık hareket bulunuyor. Bu farkın ERP testi açısından yarattığı asıl risk nedir ve doğru karşılık hangisidir?
## A
Hacim ve veri çeşitliliğine bağlı hatalar küçük ortamda hiç görünmez; doğru karşılık test/UAT ortamını düzenli olarak maskelenmiş üretim kopyasıyla yenilemektir
🤖 Doğru: küçük ve düzenli veri, hem performans hem de işlevsel aykırılıkları sistematik olarak gizler.
## B
Risk yoktur; işlevsel doğruluk veri hacminden bağımsızdır, hacim yalnız yük testinin konusudur
🤖 hacim işlevsel sonucu da değiştirir — sayfalama, arama isabeti, toplu iş süresi ve rapor doğruluğu veriyle birlikte bozulabilir.
## C
Doğru karşılık üretim veritabanını hiçbir dönüşüm yapmadan olduğu gibi test ortamına kopyalamaktır; en gerçekçi test budur
🤖 gerçekçilik doğru hedeftir ama kişisel veriyi maskelemeden taşımak KVKK açısından uyum ihlalidir.
## D
Doğru karşılık test ortamını daha da küçültüp yalnız birkaç örnek kayıtla çalışmaktır; böylece testler hızlanır ve kararlılık artar
🤖 ortamı küçültmek yürütmeyi hızlandırır ama testin gerçeği yansıtma gücünü daha da azaltır.
## Doğru
A
## Açıklama
Küçük ve derli toplu bir test ortamı yanıltıcı bir güven üretir: her rapor hızlı çalışır, her arama isabetli döner, hiçbir ürünün birimi "koli" değildir, hiçbir cari kartın vergi bilgisi eksik değildir. Üretimde ise hacim (sorgu planlarını, sayfalamayı, toplu işlerin süresini değiştirir) ve çeşitlilik (yarım doldurulmuş kayıtlar, sonradan değişmiş KDV oranları, alışılmadık birimler) birlikte gelir; bu ikisi yalnız performansı değil işlevsel sonucu da bozar (B yanlış). Doğru karşılık, test ve UAT ortamlarını düzenli aralıklarla **maskelenmiş** üretim kopyasıyla yenilemektir: ad, adres, telefon, kimlik ve kart bilgisi dönüştürülür; tutar, tarih, miktar, ilişkiler ve hacim korunur — testin değeri tam da bu korunan yapıdadır. Üretim verisini hiç dönüştürmeden taşımak (C) KVKK açısından ihlaldir; ortamı daha da küçültmek (D) ise hızlanmak uğruna testin gerçeklikle bağını koparır.

# erp-6 | İleri
## Soru
Yeni yazılan ERP'nin ilk sürümü öncesi UAT (kullanıcı kabul testi) aşamasına gelindi. ERP bağlamında UAT'nin rolü ve QA'in bu aşamadaki sorumluluğu nasıl tanımlanmalıdır?
## A
UAT, QA ekibinin sürüm öncesi son regresyon koşumudur; kullanıcıların katılımı isteğe bağlı bir gösterimden ibarettir
🤖 UAT bir QA koşumu değildir; kabul kararını süreci fiilen yürüten iş kullanıcısı verir.
## B
UAT, geliştiricilerin birim ve entegrasyon testlerini üretim benzeri ortamda yeniden çalıştırmasıdır
🤖 bu bir teknik doğrulama tekrarıdır; kabul, iş gerçeğine uygunluğun kullanıcı tarafından onaylanmasıdır.
## C
UAT'de bulunan her bulgu tanımı gereği sürümü engelleyen bir hatadır ve tamamı kapatılmadan sürüm çıkamaz
🤖 UAT bulgularının bir kısmı yanlış anlama, veri hazırlığı eksiği veya yeni istek çıkar; hepsini engelleyici saymak süreci kilitler.
## D
Kabul kararını süreci yaşayan iş kullanıcıları verir; QA'in işi senaryoyu yazmak, ortamı ve ana veriyi hazırlamak, bulguları triyaj etmektir
🤖 Doğru: ERP'nin doğruluk ölçütü kurumun gerçekte nasıl çalıştığıdır; bunu en iyi süreci yürüten kullanıcı bilir, QA ise bu doğrulamayı mümkün kılar.
## Doğru
D
## Açıklama
ERP'de doğruluğun ölçütü yalnız yazılı gereksinim değil, kurumun gerçekte nasıl çalıştığıdır: bir iskonto sırasının makul olup olmadığını satın almacı, bir stok hareketinin mantıklı olup olmadığını depo şefi, bir fişin doğru hesaba düştüğünü muhasebeci bilir. Bu yüzden kabul kararı bu kişilere aittir; QA onların yerine geçemez (A yanlış). UAT teknik testlerin üretim benzeri ortamda tekrarı da değildir (B yanlış) — teknik doğrulama zaten daha alt seviyelerde yapılmıştır. QA'in ERP'deki UAT sorumluluğu güçlü ve somuttur: senaryoyu yazmak, gereken ana veriyi ve rolleri hazırlamak, ortamı kararlı tutmak, koşumu gözlemlemek ve gelen bulguları triyaj ederek gerçek hatayı yanlış anlamadan, veri eksiğinden ve yeni istekten ayırmak (bu ayrım ve hata yaşam döngüsü `229`'da işlendi). Her UAT bulgusunu otomatik olarak engelleyici saymak (C) ise süreci kilitler; öncelik kararı bulgunun türüne ve iş etkisine göre verilir.
