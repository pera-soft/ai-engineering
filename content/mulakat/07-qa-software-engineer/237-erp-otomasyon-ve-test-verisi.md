---
slug: erp-otomasyon-ve-test-verisi
baslik: "ERP Test Otomasyonu ve Test Verisi Yönetimi: Piramidi Ayakta Tutmak"
kademe: İleri
sira: 237
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir tiyatro provası düşünün. Her prova öncesi sahne kurulmalıdır: dekor, kostüm ve sahne malzemeleri yerli yerinde olmalıdır. Bu kurulum olmadan oyunun tek bir sahnesi bile denenemez. Şimdi buna tuhaf bir kural ekleyin: **sahneye koyduğunuz bazı eşyalar bir daha kaldırılamıyor**. Her provadan sonra sahnede bir sandalye, bir masa daha kalıyor. Birkaç hafta sonra oyuncular dekorun arasında yürüyemez hâle geliyor.

ERP testi tam olarak böyle bir yerdir. Bir sipariş testi koşabilmek için önce **ana verinin** hazır olması gerekir: ürün, cari (müşteri/tedarikçi), depo, fiyat listesi, KDV oranı ve ödeme koşulu. Bunlar sahne dekorudur. Test koştuğunuzda ortaya çıkan fatura ve muhasebe fişi ise **silinemez**; bunlar da sahnede kalan sandalyelerdir.

Bu konu iki soruyu birlikte cevaplar. (1) ERP'yi hangi katmandan otomatikleştirmek gerekir ki test paketi yavaş ve kırılgan olmasın? (2) Test verisi bu "temizlenemeyen" dünyada nasıl yönetilir ki ortam aylar içinde kullanılamaz hâle gelmesin? ERP'nin genel doğası — ana veri, geri alınamazlık, ortam ayrımı — `235`'te ele alındı. Siparişten muhasebeye uzanan zincirin uçtan uca doğrulanması ve mutabakat ise `236`'da anlatıldı. Buradaki odak **otomasyon mimarisi ve test verisi stratejisidir**.

### Temel kavramlar

- **Tepetaklak piramit (ters piramit / dondurma külahı, ice-cream-cone anti-pattern)**: testlerin çoğunluğunun arayüz (UI) seviyesinde olduğu, birim ve servis seviyesinin ise cılız kaldığı dağılımdır. Test piramidinin kendisi `187`'de anlatıldı. Burada ERP'nin bu piramidi neden özellikle ters çevirdiğine bakıyoruz.
- **Katman ayrımı (ne nerede doğrulanır)**: **iş kuralı** (KDV hesabı, iskonto kademesi, kredi limiti) → birim/servis testi; **süreç** (sipariş → sevkiyat → fatura) → API testi; **kritik kullanıcı yolu** (sipariş girişi ekranı, fatura ekranı) → UI/E2E testi.
- **Servis ucu (endpoint) üzerinden doğrulama**: `POST /api/siparis`, `POST /api/sevkiyat`, `POST /api/fatura` gibi uçların doğrudan çağrılmasıdır. Araçların kendisi — Postman/Newman, REST Assured, JSON şema doğrulaması — `232`'de anlatıldı. Burada bu uçların ERP'deki **rolü** konu ediliyor.
- **Veritabanı doğrulaması (DB assertion)**: testin sonucunu ekrandan değil, veritabanından SQL ile teyit etmesidir. Örnek: muhasebe fişinin borç/alacak denkliği, stok hareketi ile stok bakiyesinin tutarlılığı.
- **Salt-okunur test hesabı (read-only account)**: doğrulama sorgularının, yanlışlıkla bile veri değiştiremeyecek bir veritabanı kullanıcısıyla koşulmasıdır.
- **Kararlı test kimliği (`data-testid`)**: arayüzde otomasyonun tutunacağı, tasarım değişikliğinden etkilenmeyen özniteliktir. **Seçici (locator)** stratejisinin kendisi `197`'de anlatıldı. Buradaki konu, **kendi geliştiricinizden bunu isteyebiliyor olmanın** avantajıdır.
- **Ana veri şablonu (master data template)**: testin ön koşulu olan temel kayıt kümesidir. Koşumlar arasında paylaşılır ve **değiştirilmez**. İçeriği: ürün, cari, depo, fiyat listesi, hesap planı, mali dönem.
- **İşlem verisi (transaction data)**: teste özel üretilen ve testin sahiplendiği kayıtlardır — sipariş, sevk irsaliyesi, fatura, tahsilat.
- **Damgalama (benzersiz ön ek)**: her koşumun ürettiği kayıtlara `TEST-20260729-1432-a3f` gibi ayırt edici bir ön ek koymaktır. Damga; sonradan filtrelemeyi, raporlardan dışlamayı ve sahiplik takibini mümkün kılar.
- **Ters kayıt (iptal fişi, reversal)**: silinemeyen bir mali kaydın etkisini sıfırlayan karşıt kayıttır. Fiziksel silme değil, **mantıksal** temizliktir; iz kalır.
- **Ortam yenileme (refresh)**: test ortamının bilinen bir anlık görüntüden (snapshot) veya üretim kopyasından yeniden kurulmasıdır.
- **Maskeleme (data masking / anonimleştirme)**: üretimden alınan verideki kişisel alanların gerçek olmayan değerlerle değiştirilmesidir. Maskelenen alanlar: ad, soyad, TCKN, adres, telefon, e-posta, kart bilgisi. KVKK açısından pazarlık konusu değildir.
- **Tutarlı maskeleme (referential consistency)**: aynı gerçek müşterinin her tabloda **aynı** takma değere dönüşmesidir. Aksi hâlde ilişkiler kopar ve mutabakat testleri anlamsızlaşır.
- **Sentetik veri**: üretimden hiç kopya almadan, kurallara göre üretilen veridir. Rastgele üretim ve **tohum (seed)** ile yeniden üretilebilirlik `234`'te ayrıntılı ele alındı.
- **Görevler ayrılığı (segregation of duties, SoD)**: aynı kişinin bir işlemi hem başlatıp hem onaylayamaması ilkesidir. Örnek: "Sipariş giren kişi, ödemeyi onaylayamaz."
- **Negatif yetki testi**: bir rolün **yapamaması gereken** işlemi denemesi ve bu denemenin reddedildiğinin doğrulanmasıdır.
- **Denetim izi (audit trail)**: kimin, ne zaman, hangi alanı, hangi eski ve yeni değerle değiştirdiğinin kaydıdır. ERP'de mevzuat gereğidir ve **kendisi de test edilmelidir**.
- **Risk bazlı regresyon seçimi**: her sürümde her şeyi koşmak yerine öncelik belirlemektir. Öncelik verilen yollar: paranın aktığı, mevzuatın dokunduğu ve en çok kullanılan yollar.
- **Duman testi (smoke test, hızlı sağlık kontrolü)**: "Sistem ayakta ve temel akış çalışıyor mu?" sorusunu dakikalar içinde cevaplayan küçük pakettir.
- **Toplu iş penceresi (batch window)**: gece koşan işlerin zaman aralığıdır. Bu işlere örnek: maliyetlendirme, amortisman, kur değerlemesi, e-belge gönderimi.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ERP de tepetaklak piramit ile hedeflenen dagilimin karsilastirmasi">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">ERP'de piramit neden ters döner, doğrusu nasıl olmalı?</text>
  <text x="164" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Sık görülen: tepetaklak piramit</text>
  <rect x="24" y="52" width="280" height="46" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.7"/>
  <text x="164" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">UI / E2E — her şey ekrandan</text>
  <text x="164" y="88" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">yüzlerce senaryo, saatler süren koşum</text>
  <rect x="66" y="102" width="196" height="36" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="164" y="119" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">API / servis — birkaç tane</text>
  <text x="164" y="133" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">"nasılsa ekran testi kapsıyor"</text>
  <rect x="108" y="142" width="112" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="164" y="161" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Birim — neredeyse yok</text>
  <text x="164" y="192" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Sonuç: yavaş geri bildirim, kırılgan paket,</text>
  <text x="164" y="206" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bir alan adı değişince 40 test birden kırmızı,</text>
  <text x="164" y="220" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hatanın hangi katmanda olduğu belirsiz</text>
  <line x1="330" y1="46" x2="330" y2="264" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>
  <text x="496" y="42" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Hedef: doğrulamayı en ucuz katmana indir</text>
  <rect x="440" y="52" width="112" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="496" y="71" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">UI — yalnız kritik yol</text>
  <rect x="398" y="86" width="196" height="38" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="496" y="104" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">API / servis — SÜREÇ</text>
  <text x="496" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sipariş → sevkiyat → fatura zinciri</text>
  <rect x="356" y="128" width="280" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.7"/>
  <text x="496" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Birim / servis — İŞ KURALI</text>
  <text x="496" y="164" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">KDV, iskonto kademesi, kredi limiti, yuvarlama</text>
  <text x="496" y="192" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Sonuç: saniyeler içinde geri bildirim,</text>
  <text x="496" y="206" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hata hangi katmanda çıktıysa orada teşhis edilir,</text>
  <text x="496" y="220" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">UI paketi küçük kaldığı için bakımı ödenebilir</text>
  <rect x="24" y="238" width="612" height="56" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="258" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Ters dönmenin sebebi teknik değil, algısal</text>
  <text x="330" y="276" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ERP süreci ekran ekran ilerlediği için "süreci test etmek" ile "ekranları gezmek" aynı şey sanılır.</text>
  <text x="330" y="290" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Oysa süreç servis çağrılarında; ekran o servisleri tetikleyen kabuktur.</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

1. **Süreci uçlara böl, her doğrulamayı en ucuz katmana indir.** Önce şu soruyu sorun: "Bu doğrulama gerçekten tarayıcı gerektiriyor mu?" KDV oranının doğru uygulanıp uygulanmadığı bir hesaplama kuralıdır; birim testi işidir. Siparişin sevkiyata, sevkiyatın faturaya dönüşmesi bir süreç doğrulamasıdır; servis çağrılarıyla test edilir. "Sipariş giriş ekranında müşteri seçilince fiyat listesi otomatik geliyor mu?" ise gerçekten bir arayüz davranışıdır; orada UI testi haklıdır.
2. **Ana veri şablonlarını bir kez kur, koşumların ona dokunmasını yasakla.** Şunlar testin ön koşuludur: ürün, cari, depo, fiyat listesi, KDV oranı, ödeme koşulu, hesap planı ve mali dönem. Her testin bunların kendi kopyasını üretmesi hem pahalı hem gereksizdir. Bu küme **salt-okunur bir sözleşmedir**. Hiçbir test paylaşılan ürünün fiyatını değiştirmez; paylaşılan carinin risk limitini oynatmaz.
3. **Teste özel işlem verisini test kendi kursun — ama arayüzden değil.** Sipariş, sevkiyat, fatura gibi kayıtları testin kendisi üretir. Üretimi ekranlardan tıklayarak değil, servis uçlarından yapar. On kalemlik bir siparişi ekrandan girmek dakikalar sürer; ekranın her değişiminde de kurulum kodu kırılır. Aynı siparişi servisten kurmak ise saniyeler sürer ve arayüzden bağımsızdır.
4. **Kurulumu ERP'nin kendi servisinden yap, SQL'e doğrudan yazmaktan kaçın.** Veritabanına elle satır eklemek hızlıdır ama **iş kurallarını atlar**. Örneğin stok hareketi olmadan bakiye yazarsanız, gerçekte hiç oluşamayacak bir durum kurmuş olursunuz. O zaman testiniz, üretimde asla yaşanmayacak bir senaryoyu doğrular. Kural şudur: **kurulum servisten, doğrulama SQL'den**.
5. **Her kaydı damgala.** Kurulan her kayda benzersiz bir ön ek koyun: `TEST-20260729-1432-a3f-URUN-01`. Damga üç işe yarar. Aynı anda koşan iki testin birbirinin kaydını görmesini önler (`203`). Raporlardan ve mutabakatlardan test kaydını dışlamayı mümkün kılar. Aylar sonra "bu kayıt kimin, nereden geldi?" sorusunu cevaplar.
6. **Doğrulamayı katmanlı yap.** Sıra şudur: servis cevabı (durum kodu, gövde) → veritabanı durumu (fiş denkliği, stok tutarlılığı) → yalnız kritik akışlarda ekran. Üçü aynı şeyi farklı derinlikte söyler; biri diğerinin yerine geçmez.
7. **Temizliği ikiye ayır.** Silinebilenler test sonunda silinir: taslak sipariş, sepet, geçici rezervasyon. Silinemeyenler için — fatura, muhasebe fişi, stok hareketi — **temizlik hedefi terk edilir**. Onun yerine izolasyon ve yenileme stratejisi konur.
8. **Regresyon paketini risk bazlı seç, koşum takvimini ortamın takvimine göre kur.** Her commit'te birim + servis; gecelik API regresyonu (toplu işler bittikten sonra); haftalık tam paket + UI E2E.

**Katman katman: ne, nerede doğrulanır?**

**(a) API / servis seviyesi — paketin belkemiği.** ERP süreci aslında bir servis çağrıları zinciridir: sipariş oluştur → stok rezerve et/düş → sevkiyat oluştur → fatura kes → muhasebe fişi üret → tahsilat işle. Bu zincir tarayıcı olmadan koşulabilir, saniyeler sürer ve arayüz değişikliklerinden etkilenmez. Araç tarafı `232`'de ayrıntılı anlatıldı: Postman/Newman koleksiyonları, REST Assured, JSON şema doğrulaması, kimlik doğrulama akışı. Burada eklenmesi gereken tek şey şudur: ERP'de API testi bir "birim testi tamamlayıcısı" değil, **paketin ana gövdesidir**. UI testlerinin sayısı onlarla, API testlerinin sayısı yüzlerle ifade edilmelidir.

```javascript
// Test verisi kurulumu: UI'dan değil, ERP'nin kendi servis uçlarından.
// Her koşum kendi damgasını üretir; kurulan her kayıt bu damgayı taşır.
const damga = `TEST-${new Date().toISOString().slice(0, 16).replace(/[-:T]/g, '')}-${Math.random().toString(36).slice(2, 7)}`;

async function siparisKur(api, { musteriKodu, kalemler }) {
  const yanit = await api.post('/api/siparis', {
    data: {
      referans: `${damga}-SIP`,          // damga: filtreleme ve sahiplik takibi için
      musteriKodu,                        // paylaşılan ana veriden, DEĞİŞTİRİLMEZ
      kalemler,                           // teste özel işlem verisi
      kaynak: 'OTOMASYON',
    },
  });
  if (!yanit.ok()) throw new Error(`Sipariş kurulamadı: ${yanit.status()} ${await yanit.text()}`);
  return (await yanit.json()).siparisNo;
}

// Kurulum servisten geçtiği için iş kuralları (kredi limiti, fiyat listesi, KDV) çalışır;
// veritabanına doğrudan INSERT edilseydi bu kuralların hiçbiri devreye girmezdi.
```

**(b) Veritabanı doğrulaması — ERP testinin ayırt edici silahı.** Bir e-ticaret sitesinde "sepet toplamı doğru mu?" sorusu ekrandan cevaplanabilir. ERP'de ise doğrulanması gereken şeylerin bir kısmı **hiçbir ekranda görünmez**. Örnekler: Bir siparişin muhasebeleşmesi sonucu oluşan fişte borç toplamı alacak toplamına eşit mi? Stok hareket satırlarının toplamı stok bakiyesiyle tutuyor mu? Fatura tutarı ile muhasebe fişindeki tutar aynı mı? Bunlar SQL ile doğrulanır.

```sql
-- 1) Muhasebe fişi denkliği: her fişin borç toplamı alacak toplamına eşit olmalı.
--    Bu sorgu HİÇ SATIR DÖNDÜRMEMELİ; dönen her satır bozuk bir fiştir.
SELECT f.fis_no,
       SUM(s.borc)   AS borc_toplam,
       SUM(s.alacak) AS alacak_toplam
FROM   muhasebe_fisi  f
JOIN   muhasebe_satir s ON s.fis_id = f.id
WHERE  f.kaynak_belge = :siparisNo
GROUP  BY f.fis_no
HAVING SUM(s.borc) <> SUM(s.alacak);

-- 2) Stok hareketi ile stok bakiyesi tutarlılığı: hareketlerin net toplamı bakiyeye eşit olmalı.
SELECT b.urun_kodu, b.depo_kodu, b.miktar AS bakiye,
       COALESCE(SUM(CASE WHEN h.yon = 'GIRIS' THEN h.miktar ELSE -h.miktar END), 0) AS hareket_neti
FROM   stok_bakiye b
LEFT   JOIN stok_hareket h
       ON  h.urun_kodu = b.urun_kodu AND h.depo_kodu = b.depo_kodu
WHERE  b.urun_kodu LIKE :damga || '%'
GROUP  BY b.urun_kodu, b.depo_kodu, b.miktar
HAVING b.miktar <> COALESCE(SUM(CASE WHEN h.yon = 'GIRIS' THEN h.miktar ELSE -h.miktar END), 0);
```

Bu yaklaşımın **avantajı** büyüktür. Hiçbir ekranın göstermediği tutarsızlıkları yakalar, hızlıdır ve ERP'nin asıl vaadini — defterin tutmasını — doğrudan sınar. **Tehlikesi** de aynı ölçüde gerçektir. Mülakatta bu tehlikeleri kendiliğinden söylemek puan getirir:

- **Şemaya sıkı bağlanma.** Tablo veya kolon adı değişince testler kırılır. Panzehir şudur: sorguları test kodunun içine dağıtmayın. Sorguları tek bir doğrulama katmanında toplayın (repository veya yardımcı sınıf); şema değişince tek noktadan düzeltirsiniz. Bu, `198`'deki Page Object fikrinin veritabanı tarafındaki karşılığıdır: kırılgan detayı tek bir yere hapsetmek.
- **Yetki riski.** Doğrulama hesabı **salt-okunur** olmalıdır. Yazma yetkisi olan bir test hesabı, bir gün yanlış yazılmış bir `UPDATE` ile ortamı bozar.
- **Yanlış güven.** Veritabanı doğru olabilir ama kullanıcı ekranda yanlış rakam görüyor olabilir. Sebebi biçimlendirme, yuvarlama, para birimi veya yetki filtresi olabilir. Veritabanı doğrulaması, arayüz doğrulamasının yerine geçmez; onu **tamamlar**.
- **İç yapıyı test etmek.** Doğrulamayı iş anlamı taşıyan değişmezler üzerinden kurun: fiş denkliği, bakiye tutarlılığı gibi. "Şu kolonda şu iç durum kodu var" gibi uygulama detayına bağlanan sorgular, her yeniden yapılandırmada kırılır.

**(c) UI otomasyonu — az ama kararlı.** Kendi yazdığınız ERP web tabanlıysa Playwright doğrudan çalışır. Ek bir eklenti, özel bir sürücü ya da ticari lisans gerekmez. Buradaki asıl kazanç teknik değil, **örgütseldir**: geliştiriciler sizin ekibinizde. Kritik ekranlara `data-testid` gibi kararlı test kimlikleri konmasını bir rica olarak bırakmayın. Bunu **tanımlı bitiş kriteri (definition of done)** hâline getirebilirsiniz. Seçici (locator) stratejisinin ayrıntısı `197`'de anlatıldı. ERP'ye özgü kısım şudur: ERP ekranları çok alanlı, çok sekmeli ve dinamik satırlıdır — örneğin sipariş kalem tablosu. Bu yüzden test kimlikleri **satır bazlı** olmalıdır: `data-testid="siparis-kalem-satir-3-miktar"` gibi. Sekme adına veya sütun sırasına dayanan bir seçici, kolon eklendiği gün kırılır.

```javascript
// UI'da yalnız kritik yol: sipariş girişi ekranı.
// Ön koşul veri API'den kuruldu; ekran testi SADECE ekran davranışını doğruluyor.
test('sipariş giriş ekranı: müşteri seçilince fiyat listesi ve KDV otomatik gelir', async ({ page, request }) => {
  const musteriKodu = await cariKur(request, { fiyatListesi: 'BAYI-2026', kdvMuaf: false });

  await page.goto('/siparis/yeni');
  await page.getByTestId('siparis-musteri-kodu').fill(musteriKodu);
  await page.getByTestId('siparis-musteri-ara').click();

  await expect(page.getByTestId('siparis-fiyat-listesi')).toHaveText('BAYI-2026');
  await expect(page.getByTestId('siparis-kalem-satir-1-kdv-orani')).toHaveText('%20');
});
```

Dikkat: bu testte ön koşul veri **API'den** kuruldu, ekrandan değil. Ekran testi yalnız ekranın kendi işini doğruluyor. Bekleme stratejisi ve **kararsız (flaky)** test yönetimi `202`'de ele alındığı için burada tekrarlanmıyor.

**Paket ERP'lerde otomasyon neden zor, sizde neden değil?**

Bu karşılaştırmayı bilmek mülakatta işe yarar, çünkü "ERP otomasyonu" denince karşınızdaki kişinin aklına genellikle paket ERP dünyasının zorlukları gelir.

- **SAP GUI for Windows** bir masaüstü istemcidir. Selenium veya Playwright gibi tarayıcı otomasyon araçları onu göremez; çünkü ortada bir DOM (belge nesne modeli) yoktur. Bu istemci için SAP'nin kendi betik arayüzü (SAP GUI Scripting) kullanılır. Bu arayüzün hem sunucu hem istemci tarafında ayrıca **etkinleştirilmesi** gerekir. Birçok kurumda ise güvenlik gerekçesiyle kapalıdır.
- **SAP Fiori / UI5** web tabanlıdır; yani tarayıcı araçları teknik olarak çalışır. Ancak bu çatı, öğe kimliklerini çoğu zaman **otomatik üretir** (`__button12` gibi). Bu kimlikler sayfa yapısı değiştiğinde kayabilir. Bu yüzden UI5 için çatıya özel araçlar tercih edilir: OPA5, wdi5.
- Bu kısıtlar yüzünden paket ERP dünyasında **ticari test araçları** yaygındır: Tricentis Tosca, Worksoft Certify, OpenText UFT One (uzun süre Micro Focus adıyla anıldı). Bu araçların iki iddiası vardır: teknoloji çeşitliliğini (masaüstü + web + terminal) tek yerden ele almak ve hazır iş süreci kütüphanesi sunmak. **Şerh:** satıcıların "model tabanlı test, bakım maliyetini şu kadar azaltır" biçimindeki oranlarını olduğu gibi almayın. Bu sayılar satıcının seçtiği vaka üzerinden üretilir. Lisans maliyeti ve araca kilitlenme (vendor lock-in) riski de ayrıca değerlendirilmelidir.
- Paket dünyada arayüzden kaçmanın yolu **servis seviyesi arayüzlerdir**: BAPI/RFC (uzak fonksiyon çağrısı), OData (Fiori'nin altındaki servis katmanı) ve IDoc (belge değişim formatı).
- **Sizde durum farklı.** Kendi yazdığınız web tabanlı ERP'de bu kısıtların hiçbiri yoktur: ne masaüstü istemci kısıtı, ne otomatik üretilen kimlik sorunu, ne de ticari araç zorunluluğu. Playwright doğrudan çalışır. Kararlı test kimliklerini geliştiriciden isteyebilirsiniz. API sözleşmesini test edilebilirlik gözüyle etkileyebilirsiniz. Test verisi kurulumu için özel uçlar açtırabilirsiniz. Bunu mülakatta **bilinçli bir avantaj** olarak dile getirmek işe yarar; konuya paket dünyanın refleksleriyle bakmadığınızı gösterir. Madalyonun diğer yüzü de dürüstçe söylenmelidir: hazır süreç kütüphanesi ve hazır test varlıkları da yoktur. İş kuralı bilgisini ve test verisi altyapısını sıfırdan siz kurarsınız.

### Test verisi yönetimi: bu konunun kalbi

```svg
<svg viewBox="0 0 660 322" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ERP test verisi yasam dongusu ve temizlenemeyen kayit stratejileri">
  <defs>
    <marker id="erpotom-ok" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Test verisi yaşam döngüsü: temizlik her zaman mümkün değildir</text>
  <rect x="22" y="34" width="140" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="92" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">1. Kurulum</text>
  <text x="92" y="69" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">servis ucundan, damgalı</text>
  <text x="92" y="81" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(SQL ile INSERT değil)</text>
  <rect x="186" y="34" width="140" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="256" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">2. Koşum</text>
  <text x="256" y="69" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">süreç servisten ilerler</text>
  <text x="256" y="81" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">doğrulama: cevap + SQL</text>
  <rect x="350" y="34" width="140" height="52" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="420" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">3. Temizlik?</text>
  <text x="420" y="72" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kayıt silinebilir mi?</text>
  <line x1="162" y1="60" x2="184" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#erpotom-ok)"/>
  <line x1="326" y1="60" x2="348" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#erpotom-ok)"/>
  <rect x="514" y="34" width="124" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="576" y="52" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">EVET: taslak sipariş,</text>
  <text x="576" y="66" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">sepet, rezervasyon</text>
  <text x="576" y="80" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">test sonunda silinir</text>
  <line x1="490" y1="60" x2="512" y2="60" stroke="#5D7789" stroke-width="1.4" marker-end="url(#erpotom-ok)"/>
  <rect x="350" y="98" width="288" height="46" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.7"/>
  <text x="494" y="117" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">HAYIR: fatura, muhasebe fişi, stok hareketi</text>
  <text x="494" y="133" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">mevzuat gereği silinemez → ortam zamanla kirlenir</text>
  <line x1="420" y1="86" x2="420" y2="96" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#erpotom-ok)"/>
  <text x="330" y="166" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Silinemeyen kayıtla yaşama stratejileri (birlikte kullanılır)</text>
  <rect x="22" y="178" width="150" height="82" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="97" y="197" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Damgalama</text>
  <text x="97" y="214" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">TEST-20260729-a3f</text>
  <text x="97" y="228" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">filtrelenebilir, raporlardan</text>
  <text x="97" y="240" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dışlanabilir, sahibi bellidir</text>
  <text x="97" y="254" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">paralel koşumu da korur</text>
  <rect x="180" y="178" width="150" height="82" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="255" y="197" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">Ayrı kap</text>
  <text x="255" y="214" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">test şirketi / şube / depo</text>
  <text x="255" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">veya ayrı mali dönem</text>
  <text x="255" y="242" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">gerçek defteri kirletmez</text>
  <text x="255" y="254" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en temiz izolasyon</text>
  <rect x="338" y="178" width="150" height="82" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="413" y="197" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">Ters kayıt + yenileme</text>
  <text x="413" y="214" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">iptal fişiyle etkiyi sıfırla</text>
  <text x="413" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">periyodik ortam refresh</text>
  <text x="413" y="242" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">takvime bağlanmalı:</text>
  <text x="413" y="254" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">habersiz refresh testi düşürür</text>
  <rect x="496" y="178" width="142" height="82" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="567" y="197" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">Maskeleme (KVKK)</text>
  <text x="567" y="214" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">üretim kopyası alınacaksa</text>
  <text x="567" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">ad, adres, telefon, kart</text>
  <text x="567" y="242" text-anchor="middle" font-size="9" font-weight="700" fill="#B23B3B" font-family="system-ui">ham veri ASLA taşınmaz</text>
  <text x="567" y="254" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tutarlı ve geri döndürülemez</text>
  <text x="330" y="284" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Sonuç: ERP'de hedef "ortamı test öncesi hâline döndürmek" değil, kirlenmeyi ÖNGÖRÜLEBİLİR ve İZOLE kılmaktır.</text>
  <text x="330" y="304" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"Her test kendi verisini kurar ve sonunda temizler" ilkesi burada yarısıyla uygulanır: kurar, evet — temizler, her zaman değil.</text>
</svg>
```

**Ana veri şablonları: testin ön koşulu.** Bir sipariş testi koşabilmek için en az şunlar hazır olmalıdır: ürün kartı (birim, KDV oranı, barkod), cari kart (fiyat listesi, ödeme koşulu, risk limiti), depo, fiyat listesi, hesap planı ve **açık bir mali dönem**. Bu küme, bir e-ticaret sitesindeki "bir kullanıcı hesabı"ndan kat kat ağırdır. Her testin bu kümenin kendi kopyasını kurması iki soruna yol açar. Birincisi, koşum süresini uçurur. İkincisi, ana veri kurulumunun kendisi uzun bir süreç olduğu için testi kırılganlaştırır.

**"Her test kendi verisini kursun" ilkesi ERP'de neden tam uygulanamaz?** Üç sebep:

1. **Ana verinin maliyeti.** Hesap planı, mali dönem, KDV tanımları, numaralandırma serileri kurumsal ayarlardır; test başına yeniden kurulmaz.
2. **Tekil ve global nesneler.** Şunlar sistem geneline aittir: mali dönemin açık/kapalı durumu, belge numaralandırma serisi, kur tablosu, stok maliyet yöntemi. Bir test mali dönemi kapatırsa, o anda koşan bütün testler düşer.
3. **Silinemezlik.** Kurulan işlem verisinin bir kısmı geri alınamaz. Yani "kurdum, kullandım, sildim" döngüsü tamamlanamaz.

Bunun pratik karşılığı **iki katmanlı bir veri modelidir**. (i) Paylaşılan, salt-okunur **ana veri havuzu**: koşumların değiştirmediği, ortam kurulumunda bir kez hazırlanan taban. (ii) Teste özel, damgalı **işlem verisi**: her testin kendi ürettiği ve sahiplendiği kayıtlar. Bir test paylaşılan bir ürünün fiyatını değiştirirse ne olur? Aynı ürünü kullanan diğer testlerin beklentisi bozulur. Üstelik paralel koşumda test sonucu, o testin hangi testlerle aynı anda koştuğuna bağlı hâle gelir. `203`'te anlatılan izolasyon ilkesinin ERP'deki en sert görünümü budur. Fiyat değişikliğini test etmesi gereken senaryo, paylaşılan ürünü değil **kendi ürettiği ürünü** değiştirmelidir.

**Temizlenemeyen kayıt sorunu.** Fatura kesildikten sonra silinemez. Muhasebe fişi silinemez. Stok hareketi silinemez. Bunlar mevzuat ve denetlenebilirlik gereğidir; ERP'nin hatası değil, **tasarım amacıdır**. Sonuçları ağırdır. Test ortamı her koşumda biraz daha dolar. Belge numaraları hızla ilerler. Raporlar test kayıtlarıyla karışır. Veri hacmi büyüdükçe koşum yavaşlar. Bir süre sonra "gerçek" veriyle "test" verisini ayırt etmek imkânsızlaşır. Aşağıdaki stratejiler birlikte kullanılır:

- **Benzersiz ön ek / damga.** Kurulan her kayıt `TEST-<zaman damgası>-<rastgele>` biçiminde bir referans taşır. Bu damga üç işi kolaylaştırır: kayıtları raporlardan dışlamak, toplu sorgulamak ve paralel koşumda çakışmayı önlemek. Rastgele bileşenin **tohumla (seed) yeniden üretilebilir** olması, bir hatayı yeniden canlandırırken işe yarar (`234`).
- **Ayrı test şirketi / şube / depo.** ERP'ler genellikle çok şirketlidir. "TEST A.Ş." adında ayrı bir şirket açabilirsiniz; ayrı bir şube veya depo da olur. Böylece testin ürettiği faturalar gerçek şirketin defterine hiç dokunmaz. Bu, damgalamadan daha güçlü bir izolasyondur; çünkü ayrım veri seviyesinde değil, **yapısal** seviyededir. Bedeli şudur: ana verinin o şirket için de kurulması gerekir.
- **Ayrı mali dönem.** Testler geleceğe ait veya özel olarak açılmış bir döneme yazar. Böylece gerçek dönemin raporları temiz kalır.
- **Ters kayıt (iptal fişi).** Kaydı silmez ama etkisini sıfırlar. Bakiye ve stok doğru kalır, kayıt izi korunur. Not: ters kaydın kendisi de bir işlemdir ve **o da test edilmelidir**. İptal edilen faturanın muhasebe etkisinin gerçekten sıfırlandığını doğrulayan test, ERP paketinin en değerli testlerinden biridir.
- **Periyodik ortam yenileme (refresh).** Ortam belirli aralıklarla bilinen bir anlık görüntüden yeniden kurulur. İki şartı vardır. Birincisi **takvime bağlanmalıdır**; habersiz bir yenileme, o sırada koşan testleri açıklanamaz biçimde düşürür. İkincisi, **yenileme sonrası ana veri havuzu otomatik olarak yeniden kurulmalıdır**; elle kurulan bir taban her yenilemede günler kaybettirir.
- **Üretimden kopya + maskeleme.** Gerçek veri hacmi ve gerçek veri dağılımı çok değerlidir; performans (`233`) ve uç durum testleri bunu gerektirir. Ama kişisel veri **ham hâliyle asla** test ortamına taşınmaz. Ad, soyad, TCKN, adres, telefon, e-posta ve kart bilgileri maskelenir. Doğru maskelemenin dört özelliği vardır. **Tutarlıdır**: aynı gerçek müşteri her tabloda aynı takma değere dönüşür; yoksa ilişkiler kopar ve mutabakat testleri anlamsızlaşır. **Geri döndürülemezdir**: takma değerden gerçek değere dönülemez. **Biçimi korur**: telefon yine telefon biçiminde kalır ki doğrulama kuralları çalışsın. **Güvenli bir aracı adımda yapılır**: maskeleme üretim ortamında değil, ayrı bir adımda gerçekleşir. Maskelenmemiş bir kopyanın test ortamına bir an bile düşmesi ihlaldir. Alternatif yol, hiç kopya almadan **sentetik veri** üretmektir. Sentetik veride gizlilik riski yoktur. Ama gerçek verinin tuhaflıklarını da barındırmaz: eksik alanlar, tarihî anomaliler, garip karakterler gibi.

**Veri kurulumu arayüzden değil, API/SQL'den.** Bunun üç gerekçesi vardır. **Hız**: dakikalar yerine saniyeler. **Kararlılık**: ekran değişse bile kurulum ayakta kalır. **Niyet netliği**: test doğrulamak istediği şeye odaklanır, kurulum gürültüsü senaryodan çıkar. Buradaki ince ayrım şudur: **kurulum servis ucundan, doğrulama SQL'den**. Doğrudan SQL ile kayıt eklemek iş kurallarını atlar ve gerçekte oluşamayacak durumlar yaratır. O zaman testiniz, üretimde hiç yaşanmayacak bir senaryoyu doğrulamış olur. Daha kötüsü, gerçek bir hatayı da maskeleyebilir.

### Yetki ve rol testleri

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Rol ve islem yetki matrisi, gorevler ayriligi ve negatif testler">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yetki matrisi: her hücre bir testtir, çoğu NEGATİF testtir</text>
  <rect x="30" y="32" width="150" height="26" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="105" y="49" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Rol \ İşlem</text>
  <rect x="180" y="32" width="112" height="26" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="236" y="49" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">Sipariş gir</text>
  <rect x="292" y="32" width="112" height="26" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="348" y="49" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">Sevk et</text>
  <rect x="404" y="32" width="112" height="26" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="460" y="49" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">Fatura kes</text>
  <rect x="516" y="32" width="114" height="26" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="573" y="49" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">Ödeme onayla</text>
  <rect x="30" y="58" width="150" height="30" fill="#FFFFFF" stroke="#5D7789" stroke-width="0.9"/>
  <text x="40" y="78" font-size="9.5" fill="#1C3247" font-family="system-ui">Satış temsilcisi</text>
  <rect x="180" y="58" width="112" height="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="236" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <rect x="292" y="58" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="348" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="404" y="58" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="460" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="516" y="58" width="114" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="573" y="78" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="30" y="88" width="150" height="30" fill="#FFFFFF" stroke="#5D7789" stroke-width="0.9"/>
  <text x="40" y="108" font-size="9.5" fill="#1C3247" font-family="system-ui">Depo görevlisi</text>
  <rect x="180" y="88" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="236" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="292" y="88" width="112" height="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="348" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <rect x="404" y="88" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="460" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="516" y="88" width="114" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="573" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="30" y="118" width="150" height="30" fill="#FFFFFF" stroke="#5D7789" stroke-width="0.9"/>
  <text x="40" y="138" font-size="9.5" fill="#1C3247" font-family="system-ui">Muhasebe</text>
  <rect x="180" y="118" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="236" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="292" y="118" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="348" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="404" y="118" width="112" height="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="460" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <rect x="516" y="118" width="114" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="573" y="138" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="30" y="148" width="150" height="30" fill="#FFFFFF" stroke="#5D7789" stroke-width="0.9"/>
  <text x="40" y="168" font-size="9.5" fill="#1C3247" font-family="system-ui">Finans onaycısı</text>
  <rect x="180" y="148" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="236" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="292" y="148" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="348" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="404" y="148" width="112" height="30" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1"/>
  <text x="460" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">✕</text>
  <rect x="516" y="148" width="114" height="30" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="573" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="330" y="196" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">16 hücrenin 4'ü pozitif (yapabilmeli), 12'si NEGATİF testtir (yapamamalı). Kalın çerçeveli hücreler görevler ayrılığının kalbidir.</text>
  <rect x="30" y="208" width="292" height="76" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="176" y="228" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Yetersiz doğrulama</text>
  <text x="176" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"Depo görevlisi ekranında fatura butonu</text>
  <text x="176" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">görünmüyor, demek ki yetki doğru."</text>
  <text x="176" y="276" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Buton gizlemek bir yetki denetimi değildir.</text>
  <rect x="338" y="208" width="292" height="76" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="484" y="228" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Doğru doğrulama</text>
  <text x="484" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Depo görevlisinin belirtecisiyle (token)</text>
  <text x="484" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">POST /api/fatura doğrudan çağrılır → 403</text>
  <text x="484" y="276" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">ayrıca: denetim izinde reddedilen deneme var mı?</text>
</svg>
```

ERP'de yetki, "kullanıcı hangi menüyü görüyor" meselesi değildir; bir **iç kontrol** meselesidir. En önemli ilke **görevler ayrılığıdır (segregation of duties)**: aynı kişi bir işlemi hem başlatıp hem onaylayamamalıdır. Klasik örnekler şunlardır. Sipariş giren kişi ödemeyi onaylayamaz. Stok sayım farkını giren kişi o farkı onaylayamaz. Tedarikçi kartı açan kişi o tedarikçiye ödeme yapamaz. Bu kurallar denetimin de ilk baktığı yerdir.

Bunun test tarafındaki karşılığı şudur: **yetki testlerinin çoğunluğu negatif testtir**. Yukarıdaki 4×4 matriste 4 hücre "yapabilmeli", 12 hücre "yapamamalı" der. Çoğu ekip yalnızca yeşil hücreleri test eder; çünkü kırmızı hücrelerin "zaten olmadığı" varsayılır. Oysa bir yetki hatası tam olarak orada saklanır.

**Kritik ayrım: arayüzde butonu gizlemek yetki denetimi değildir.** Butonun görünmemesi bir kullanıcı deneyimi iyileştirmesidir. Asıl denetim sunucu tarafında olmalıdır. Yetkisiz bir rolün belirteciyle (token) uç doğrudan çağrıldığında sistem **403** dönmelidir. Bunun iki alt başlığı vardır:

- **Dikey yetki yükseltme:** düşük yetkili bir rolün, üst yetki gerektiren bir işlemi çağırmasıdır. Örnek: depo görevlisinin fatura kesmesi.
- **Yatay yetki ihlali (nesne seviyesi yetki):** doğru role sahip bir kullanıcının, **başkasına ait** bir kayda erişmesidir. Örnek soru: "İstanbul şubesi kullanıcısı, Ankara şubesinin sipariş numarasını yazarak o siparişi görebiliyor mu?" Bu sınıfı çoğu ekip atlar. Oysa gerçek hayatta en sık sızıntıya yol açan sınıf budur.

```java
// Negatif yetki testi (Java + TestNG): yetkisiz rol, ucu DOĞRUDAN çağırır.
// Ekranda butonun gizli olması bu testin yerine geçmez.
public class YetkiNegatifTestleri {

    @DataProvider(name = "faturaKesemeyecekRoller")
    public Object[][] faturaKesemeyecekRoller() {
        return new Object[][] { {"DEPO_GOREVLISI"}, {"SATIS_TEMSILCISI"}, {"FINANS_ONAYCISI"} };
    }

    @Test(dataProvider = "faturaKesemeyecekRoller")
    public void yetkisizRolFaturaKesemez(String rol) {
        String belirtec = TestKullanicilari.belirtecAl(rol);   // rol başına ayrı test kullanıcısı: ana veridir

        given()
            .header("Authorization", "Bearer " + belirtec)
            .contentType(ContentType.JSON)
            .body(Map.of("sevkiyatNo", ortakSevkiyatNo))
        .when()
            .post("/api/fatura")
        .then()
            .statusCode(403);                                  // 200 dönerse ciddi bir iç kontrol açığıdır
    }

    @Test
    public void baskaSubeninSiparisiGorulemez() {              // yatay yetki (nesne seviyesi)
        String istanbulBelirteci = TestKullanicilari.belirtecAl("SATIS_TEMSILCISI_IST");

        given().header("Authorization", "Bearer " + istanbulBelirteci)
        .when().get("/api/siparis/" + ankaraSiparisNo)
        .then().statusCode(anyOf(is(403), is(404)));           // 200 + veri dönmesi yatay yetki ihlalidir
    }
}
```

**Denetim izi de test edilir.** ERP'de "kim, ne zaman, hangi alanı, hangi eski ve yeni değerle değiştirdi" kaydı mevzuat gereğidir. Testi basittir ama çoğu ekipte yoktur. Şöyle yapılır: bir fiyatı değiştirin; sonra denetim tablosunda kaydın oluştuğunu doğrulayın. Kullanıcı kimliğinin, zaman damgasının ve eski/yeni değerin doğru yazıldığını da kontrol edin. Reddedilen yetkisiz denemelerin de iz bırakması beklenir.

Yetki testlerinin bir de **veri boyutu** vardır. Her rol için ayrı bir test kullanıcısı gerekir ve bu kullanıcılar ana verinin parçasıdır. Ortam yenilendiğinde bu kullanıcı/rol havuzu da otomatik olarak yeniden kurulmalıdır. Aksi hâlde yetki paketi her yenileme sonrası günlerce koşamaz.

### Regresyon paketi seçimi ve koşum takvimi

ERP'de "her sürümde her şeyi koş" mümkün değildir. Süreç sayısı yüksektir ve uçtan uca akışlar uzundur. Ayrıca bazı adımlar doğaları gereği yavaştır: toplu iş, e-belge gönderimi, dönem kapanışı gibi. Seçim yapmak zorunludur ve seçimin ölçütü **risktir**:

- **Paranın aktığı yollar:** fiyat, iskonto, KDV, kur, tahsilat, muhasebeleşme. Buradaki bir hata doğrudan para kaybı veya yanlış beyandır.
- **Mevzuata dokunan yollar:** e-fatura/e-irsaliye, KDV oran değişiklikleri, beyanname çıktıları. Mevzuat değiştiğinde bu paket **tam** koşulur.
- **En çok kullanılan süreçler:** günde yüzlerce kez koşan sipariş girişi, sevkiyat, tahsilat. Kullanım sıklığı, hatanın etki alanını büyütür.
- **Son değişiklikten etkilenen alan:** değişen modül ve ona bağımlı modüller.
- **Geçmişte en çok hata çıkan modüller:** hata yoğunluğu kümelenir; geçmiş, gelecek için iyi bir tahmincidir.

Bunun üstüne **duman testi (smoke test) paketi** oturur. Sorduğu soru şudur: "Gün içinde bir sipariş uçtan uca geçiyor mu?" Bu paket, sipariş → sevkiyat → fatura → muhasebe fişi → tahsilat zincirinin tek bir örneğini dakikalar içinde koşar. Böylece ortamın ayakta olduğunu ve temel akışın çalıştığını söyler. Zincirin doğrulama ayrıntıları ve mutabakat kontrolleri `236`'da ele alınıyor.

Katmanlı bir koşum takvimi pratikte şöyle olur. **Her commit'te** birim ve servis testleri koşar; dakikalar sürer. **Her dağıtımda** duman paketi koşar. **Her gece**, toplu işler bittikten sonra API regresyonu ve toplu iş doğrulaması koşar. **Haftalık** olarak tam paket ve UI uçtan uca testleri koşar. **Sürüm yükseltmesi, mevzuat değişikliği veya altyapı/veritabanı değişiminde** ise tam regresyon koşulur.

Son bir uyarı: "her şeyi otomatikleştirelim" hedefi ERP'de özellikle pahalıya patlar. Otomasyona en çok değen testler **sık koşulan, sonucu belirli ve nadiren değişen** testlerdir. Şöyle bir senaryo düşünün: ayda bir koşuluyor, her sürümde ekranı değişiyor ve kurulumu yarım saat sürüyor. Böyle bir senaryoyu otomatikleştirmek, bakım maliyetiyle kazandığından fazlasını götürür. O senaryo elle koşulur; otomasyon bütçesi başka yere harcanır.

### Ortam ve zamanlama gerçekleri

- **Toplu iş penceresi (batch window).** Gece koşan işler veriyi topluca değiştirir: maliyetlendirme, amortisman, kur değerlemesi, yeniden değerleme ve e-belge gönderimi. Bu pencerede koşan bir test kilit bekleyebilir, yarı işlenmiş veri görebilir veya açıklanamaz biçimde düşebilir. Bu düşüş çoğu zaman **kararsız (flaky) test** diye yanlış etiketlenir (`202`). Doğrusu, koşumu toplu işin bitiş sinyaline bağlamaktır.
- **Toplu işin kendisi de test edilir.** Şu soruları cevaplayan doğrulama testleri gerekir: "Gece işi çalıştı mı? Kaç kayıt işledi? Çıktı beklenen mi? Hata kuyruğunda ne var?" Bu testler ERP paketinin sessiz kahramanlarıdır. Sebebi şudur: toplu işin sessizce yanlış çalışması ekranda hiç görünmez, ama aylar sonra mutabakatta patlar.
- **Dönem sonu kilidi.** Ay ve yıl kapanışında ortam kilitlenir; kayıt girilemez. "Dönem kapalı" hatası testin değil, takvimin sonucudur. Test takvimi mali takvimi tanımalıdır.
- **Zamana bağlı davranış.** Vade hesapları, gecikme faizi, kampanya tarihleri ve yaşlandırma raporları tarihe bağlıdır. Sistem saatini ileri almak tüm ortamı ve o sırada koşan diğer testleri etkiler. Doğru çözümün iki parçası vardır. Uygulama zamanı dışarıdan verilebilir bir parametre olarak ele almalıdır. Test ise tarihi **veri seviyesinde** kurmalıdır: dünkü tarihli fatura üretmek, ileri vadeli sipariş girmek gibi.
- **Performans yanılsaması.** Bin ürünlük bir test ortamında her sorgu hızlıdır. Üretim benzeri hacim olmadan ölçülen performans, üretimdeki asıl darboğazı tanım gereği göremez (`233`). ERP tarafındaki özel biçimi şudur: yıl büyüdükçe muhasebe hareket tablosu şişer ve dönem raporları yavaşlar. Bu ancak yıllara yayılmış hacimli veriyle görülür.

### Mülakat cevabı kalıbı

**Zayıf cevap:** "ERP'yi Playwright ile otomatikleştiririm, ekranları gezip kontrol ederim." Bu cevap üç şeyi ıskalar: hangi katmanda ne doğrulanacağını, test verisinin nereden geleceğini ve sonucun defterde tutup tutmadığını.

**Güçlü cevap üç ayak üzerinde durur:**

1. **Katman ayrımı.** "İş kuralını birim/servis testinde, süreci API seviyesinde, yalnız kritik kullanıcı yolunu UI'da doğrularım; ERP'de piramit kolayca ters döner, bunu bilinçli olarak engellerim."
2. **Test verisi stratejisi.** "Paylaşılan ana veriyi salt-okunur tutarım. İşlem verisini her test kendi damgasıyla servis ucundan kurar. Fatura ve fiş silinemediği için ayrı test şirketi ile ayrı dönem kullanır, buna periyodik yenileme eklerim. Üretimden veri alınacaksa maskeleme şarttır."
3. **Mutabakat doğrulaması.** "Testin başarısı, ekranda 'başarılı' yazması değildir. Başarı; siparişin sevkiyata, sevkiyatın faturaya, faturanın muhasebe fişine tutarlı biçimde yansımasıdır. Bunu SQL ile, salt-okunur bir hesapla doğrularım."

Bu üçlüye kendi bağlamınızı da ekleyin: "ERP'yi kendimiz yazdığımız için SAP GUI veya otomatik üretilen kimlik gibi kısıtlarımız yok. Geliştiriciden kararlı test kimliği ve test verisi kurulum ucu isteyebiliyoruz. Bu, paket ERP dünyasına göre ciddi bir avantaj."

### Mülakatta ne sorulur / tuzaklar

- **"ERP'yi nasıl otomatikleştirirsin?"** Beklenen üçlü: katman ayrımı, test verisi stratejisi ve mutabakat doğrulaması. "Playwright ile ekranları otomatikleştiririm" tek başına zayıf bir cevaptır.
- **"Test verisini nasıl yönetirsin?"** Beklenen: paylaşılan salt-okunur ana veri artı teste özel damgalı işlem verisi. Kurulum servis ucundan, doğrulama SQL'den yapılır.
- **"Fatura silinemiyor, ortamı nasıl temiz tutuyorsun?"** Beklenen: temizlik hedefinden vazgeçip izolasyona geçmek. Araçlar şunlardır: damgalama, ayrı test şirketi ve dönemi, ters kayıt, periyodik yenileme. "Test sonunda faturayı silerim" derseniz ERP bilmediğiniz anlaşılır.
- **"Üretim verisini test ortamına alır mısın?"** Beklenen: hacim ve gerçekçilik için değerlidir, ama **maskelenmeden asla**. Maskeleme tutarlı ve geri döndürülemez olmalı, güvenli bir aracı adımda yapılmalıdır. "Yedeği açar test ederiz" cevabı KVKK ihlalidir ve adayı eler.
- **"SQL ile doğrulama yapar mısın?"** Beklenen: evet. Fiş denkliği ve stok/bakiye tutarlılığı gibi ekranda görünmeyen değişmezler için yapılır. Salt-okunur bir hesapla koşulur, sorgular tek bir katmanda toplanır ve şemaya bağlanma riskinin farkında olunur.
- **"Her test kendi verisini kursun ilkesi ERP'de geçerli mi?"** Beklenen: yarısıyla geçerlidir. Test verisini kurar, ama her zaman temizleyemez. Ana veri paylaşılır; işlem verisi teste özeldir.
- **"Depo görevlisi fatura kesemiyor, nasıl test edersin?"** Beklenen: ekranda butonun gizli olması yetmez. O rolün belirteciyle uç doğrudan çağrılır ve 403 beklenir. Ayrıca yatay yetki (başka şubenin kaydı) denenir ve denetim izi doğrulanır.
- **"Her sürümde tam regresyon koşar mısın?"** Beklenen: hayır. Risk bazlı seçim ve duman paketi kullanılır. Tam regresyon ise sürüm yükseltmesinde ve mevzuat değişikliğinde koşulur. "Her şeyi koşarım" cevabı ERP ölçeğini bilmemektir.
- **"Gece koşan testleriniz neden ara ara kırmızı?"** Beklenen: önce toplu iş penceresiyle çakışma ihtimali elenir. Her düşüşü "kararsız test" diye etiketlemek teşhis değil, teşhisten kaçmaktır.
- **"Paket ERP'lerde neden özel araçlar kullanılıyor?"** Beklenen: masaüstü istemci (SAP GUI) ve otomatik üretilen dinamik kimlikler yüzünden. Bu yüzden servis seviyesi arayüzler tercih edilir: BAPI/RFC, OData, IDoc. Ardından "kendi ERP'mizde bu kısıt yok" avantajı dile getirilir.

### Tek cümlede

ERP test otomasyonu; süreç ekran ekran ilerlediği için ters dönmeye çok yatkın olan test piramidini bilinçli olarak ayakta tutmaktır. Yani iş kuralı birim/servis, süreç API, yalnız kritik yol UI seviyesinde doğrulanır; ekranda görünmeyen değişmezler ise salt-okunur bir hesapla SQL'den teyit edilir. Bunu sürdürülebilir kılan tek şey, "silinemeyen kayıt" gerçeğiyle barışık bir test verisi stratejisidir: paylaşılan salt-okunur ana veri, damgalı teste özel işlem verisi, ayrı test şirketi ve dönemi, periyodik yenileme, maskelenmiş üretim kopyası.

# erpotom-1 | Temel
## Soru
Bir ERP ekibi, siparişten faturaya uzanan tüm süreci sipariş ekranı, sevkiyat ekranı ve fatura ekranı üzerinden tıklayarak test eden 180 senaryoluk bir Playwright paketi kurmuş. Paket 5 saat sürüyor ve bir alan adı değiştiğinde 40 test birden kırmızı oluyor. Bu tabloyu en doğru tanımlayan ve doğru düzeltmeyi öneren şık hangisidir?
## A
Paket doğru kurulmuş; sorun Playwright'ın yavaşlığıdır, Selenium'a geçilirse koşum süresi kısalır
🤖 koşum süresini belirleyen şey aracın markası değil, doğrulamaların hangi katmanda yapıldığıdır; araç değiştirmek 180 UI senaryosunun maliyetini ortadan kaldırmaz.
## B
Sorun senaryoların paralel koşmamasıdır; worker sayısı artırılırsa hem süre düşer hem kırılganlık ortadan kalkar
🤖 paralellik süreyi kısaltabilir ama bir alan adı değişikliğinin 40 testi birden kırması kırılganlık sorunudur ve paralellikle ilgisi yoktur.
## C
Piramit tepetaklak dönmüştür; doğrulamaların çoğu iş kuralı ve süreç seviyesine (birim/servis ve API) indirilmeli, UI'da yalnız kritik kullanıcı yolları bırakılmalıdır
🤖 Doğru: ERP süreci ekran ekran ilerlediği için "süreci test etmek" ile "ekranları gezmek" karıştırılır; süreç aslında servis çağrılarında olduğu için doğrulama en ucuz katmana indirilmelidir.
## D
UI testlerinin sayısı doğru; asıl eksik, her ekran için ayrı bir Page Object sınıfı yazılmamış olmasıdır
🤖 Page Object bakım maliyetini azaltır ama 180 UI senaryosunun yavaşlığını ve katman yanlışını gidermez; sorun testlerin nasıl yazıldığı değil, hangi katmanda olduğudur.
## Doğru
C
## Açıklama
ERP'de süreç kullanıcıya ekran ekran göründüğü için ekipler "süreci test etmek" ile "ekranları gezmek" arasındaki farkı kolayca kaybeder ve piramit tepetaklak döner: yüzlerce UI senaryosu, birkaç API testi, neredeyse hiç birim testi. Sonuç tam da tarif edilen tablodur — saatler süren koşum, tek bir arayüz değişikliğinde toplu kırılma ve hatanın hangi katmanda olduğunun belirsizleşmesi. Doğru düzeltme katman ayrımıdır: KDV, iskonto kademesi, kredi limiti gibi **iş kuralları** birim/servis testine; sipariş → sevkiyat → fatura **süreci** API testine; yalnız sipariş girişi ve fatura ekranı gibi **kritik kullanıcı yolları** UI'a bırakılır. Araç değiştirmek (A) katman sorununu çözmez; paralellik (B) süreyi kısaltır ama kırılganlığa dokunmaz; Page Object (D) bakımı kolaylaştırır ama 180 senaryonun yanlış katmanda olduğu gerçeğini değiştirmez.

# erpotom-2 | Orta
## Soru
ERP test paketinizde sipariş testlerinin ön koşulu olarak ürün, cari ve sipariş kayıtları kurulması gerekiyor. Kurulum için üç seçenek tartışılıyor: (i) ekranlardan tıklayarak, (ii) ERP'nin kendi servis uçlarından, (iii) doğrudan veritabanına `INSERT` ile. Doğru yaklaşım ve gerekçesi nedir?
## A
Kurulum servis uçlarından yapılır: hızlı ve arayüzden bağımsızdır, ayrıca iş kuralları çalıştığı için kurulan veri gerçekte oluşabilecek bir durumu temsil eder; SQL yalnız salt-okunur doğrulama için kullanılır
🤖 Doğru: doğrudan `INSERT` iş kurallarını atlayarak üretimde asla oluşamayacak durumlar yaratır, UI kurulumu ise hem yavaştır hem her ekran değişiminde kırılır.
## B
Kurulum ekranlardan yapılır: kullanıcı gerçekte veriyi böyle girdiği için en gerçekçi kurulum budur ve ekran testleri de böylece ısınmış olur
🤖 on kalemlik bir siparişi ekrandan girmek dakikalar sürer ve kurulum kodu her arayüz değişiminde kırılır; kurulumun gerçekçiliği, testin doğrulamak istediği şeyle karıştırılmamalıdır.
## C
Kurulum doğrudan veritabanına `INSERT` ile yapılır: en hızlı yol budur ve iş kuralları zaten ayrı testlerle doğrulandığı için atlanmasının sakıncası yoktur
🤖 iş kurallarını atlayan kurulum, stok hareketi olmadan bakiye yazmak gibi gerçekte oluşamayacak durumlar üretir ve test o zaman üretimde hiç yaşanmayacak bir senaryoyu doğrular.
## D
Kurulum yöntemi önemsizdir; önemli olan verinin damgalanmasıdır, damga varsa hangi yolla kurulduğu sonucu etkilemez
🤖 damgalama izolasyon ve izlenebilirlik sağlar ama kurulum yolunun iş kurallarını çalıştırıp çalıştırmadığı sorusunu cevaplamaz; ikisi farklı problemlerdir.
## Doğru
A
## Açıklama
ERP'de test verisi kurulumunun altın kuralı şudur: **kurulum servis ucundan, doğrulama SQL'den**. Servis ucundan kurmak üç şey kazandırır — hız (dakikalar yerine saniyeler), arayüz değişikliklerinden bağımsızlık ve en önemlisi iş kurallarının çalışması: fiyat listesi uygulanır, KDV hesaplanır, kredi limiti denetlenir, stok hareketi ile bakiye birlikte güncellenir. Doğrudan `INSERT` (C) bu kuralların hepsini atlar ve gerçekte oluşamayacak bir durum kurar; böyle bir taban üzerinde koşan test, üretimde hiç yaşanmayacak bir senaryoyu doğruladığı gibi gerçek bir hatayı da maskeleyebilir. Ekrandan kurulum (B) yavaşlığı ve kırılganlığı yüzünden paketin en büyük bakım kalemine dönüşür. Damgalama (D) ayrı ve tamamlayıcı bir konudur: kayıtların sahibini ve izolasyonunu sağlar, kurulum yolunun doğruluğunu değil.

# erpotom-3 | Orta
## Soru
Test ortamınızda üç aydır otomasyon koşuyor ve ortam gözle görülür biçimde kirlendi: binlerce test faturası, muhasebe fişi ve stok hareketi birikti, raporlar okunamaz hâle geldi. Ekip "test sonunda oluşturduğumuz faturaları silelim" öneriyor. En doğru yaklaşım hangisidir?
## A
Test sonunda fatura ve muhasebe fişleri veritabanından `DELETE` ile silinir; ortam böylece her koşum sonrası temiz kalır
🤖 bu kayıtlar mevzuat ve denetlenebilirlik gereği silinemez olacak şekilde tasarlanmıştır; veritabanından zorla silmek de bakiye ve numaralandırma tutarlılığını bozar.
## B
Ortam her koşumdan önce üretim yedeğinden yeniden kurulur; böylece hem temizlik hem gerçekçi veri sağlanmış olur
🤖 her koşum öncesi yenileme pratikte imkânsız ölçüde maliyetlidir ve üretim yedeğinin maskelenmeden test ortamına açılması ayrıca KVKK ihlalidir.
## C
Yalnızca duman paketi koşulur, geri kalan testler elle yapılır; böylece ortamda biriken kayıt sayısı düşük tutulur
🤖 bu, kirlenme sorununu test kapsamını feda ederek çözmeye çalışmaktır; otomasyonun sağladığı regresyon güvencesi kaybedilir.
## D
Silme hedefinden vazgeçilir; damgalı ön ek, ayrı test şirketi veya mali dönem, ters kayıt ve takvime bağlı periyodik ortam yenilemesi birlikte kullanılır
🤖 Doğru: ERP'de hedef ortamı test öncesi hâline döndürmek değil, kirlenmeyi öngörülebilir ve izole kılmaktır.
## Doğru
D
## Açıklama
Fatura, muhasebe fişi ve stok hareketi ERP'de bilinçli olarak silinemez kılınmıştır — bu bir eksik değil, denetlenebilirlik gereğidir. Dolayısıyla "test sonunda sil" (A) hem mümkün değildir hem de veritabanı üzerinden zorlandığında bakiye tutarlılığını ve belge numaralandırmasını bozar. Her koşum öncesi ortam yenilemek (B) maliyet olarak gerçekçi değildir ve üretim yedeğini maskelemeden kullanmak ayrı bir ihlaldir. Kapsamı daraltmak (C) sorunu çözmez, otomasyonun değerini yok eder. Doğru yaklaşım stratejileri **birlikte** kullanmaktır: her kayda `TEST-<zaman damgası>-<rastgele>` biçiminde damga koymak (filtreleme, raporlardan dışlama, paralel koşumda çakışmama), ayrı bir test şirketi/şube veya ayrı bir mali dönem kullanarak gerçek defteri hiç kirletmemek, gerektiğinde ters kayıtla mali etkiyi sıfırlamak ve ortamı **takvime bağlı** olarak periyodik yenilemek. Yenilemenin takvime bağlanması şarttır: habersiz bir refresh, o sırada koşan testleri açıklanamaz biçimde düşürür.

# erpotom-4 | İleri
## Soru
Performans ve uç durum testleri için üretim verisinin bir kopyasının test ortamına alınmasına karar verildi. Aşağıdakilerden hangisi doğru uygulamayı tarif eder?
## A
Üretim yedeği test ortamına açılır, ardından test ortamında müşteri adları rastgele değerlerle güncellenir; test ortamı zaten iç ağda olduğu için bu yeterlidir
🤖 maskelenmemiş kopyanın test ortamına bir an bile düşmesi ihlaldir; ayrıca yalnız ad alanını değiştirmek adres, telefon, e-posta ve kart bilgilerini korumasız bırakır.
## B
Maskeleme güvenli bir aracı adımda yapılır; kişisel alanlar geri döndürülemez biçimde değiştirilir, aynı gerçek müşteri her tabloda aynı takma değere dönüşür ve alanların biçimi korunur
🤖 Doğru: tutarlılık olmadan ilişkiler kopar ve mutabakat testleri anlamsızlaşır; biçim korunmadan da uygulamanın doğrulama kuralları çalışmaz.
## C
Her tabloda her kişisel alan bağımsız olarak rastgele bir değerle değiştirilir; rastgelelik ne kadar yüksekse gizlilik o kadar güçlü olur
🤖 bağımsız rastgeleleştirme aynı müşteriyi tablolar arasında farklı değerlere dönüştürür, ilişkileri kopardığı için sipariş-fatura-tahsilat zinciri ve mutabakat testleri çalışmaz hâle gelir.
## D
Kişisel alanlar geri döndürülebilir bir şifreleme ile maskelenir; hata araştırması gerektiğinde gerçek değere dönülebilmesi önemlidir
🤖 geri döndürülebilir dönüşüm, anahtara erişen herkes için verinin hâlâ kişisel veri olması demektir; test ortamında bu risk kabul edilemez.
## Doğru
B
## Açıklama
Üretim kopyası, gerçek veri hacmi ve gerçek veri dağılımı sağladığı için performans ve uç durum testlerinde çok değerlidir; ama kişisel veri (ad, soyad, TCKN, adres, telefon, e-posta, kart bilgisi) ham hâliyle test ortamına **hiç** taşınmaz. Maskeleme üretim ortamında ya da test ortamında değil, ikisinin arasındaki **güvenli bir aracı adımda** yapılır — maskelenmemiş kopyanın test ortamında bir an bile bulunması ihlaldir (A). Doğru maskelemenin dört özelliği vardır: geri döndürülemezlik (D'nin aksine, anahtarla gerçek değere dönülememesi), **tutarlılık** (aynı gerçek müşteri her tabloda aynı takma değere dönüşmeli — bağımsız rastgeleleştirme (C) ilişkileri koparır ve sipariş-fatura-tahsilat zinciri doğrulanamaz hâle gelir), biçim korunumu (telefon yine telefon biçiminde olsun ki uygulamanın doğrulama kuralları çalışsın) ve kapsam bütünlüğü (tek bir alan değil, kişisel veri niteliğindeki tüm alanlar). Üretim kopyası hiç istenmiyorsa alternatif, kurallara göre üretilen sentetik veridir: gizlilik riski yoktur ama gerçek verinin tuhaflıklarını barındırmaz.

# erpotom-5 | İleri
## Soru
Görevler ayrılığı gereği depo görevlisi fatura kesememelidir. Bir test uzmanı bu kuralı, depo görevlisi hesabıyla giriş yapıp fatura ekranında "Fatura Kes" butonunun görünmediğini doğrulayarak test etmiş ve senaryoyu geçti olarak işaretlemiş. Bu doğrulamanın eksiği nedir?
## A
Eksik yoktur; butonun görünmemesi kullanıcının o işlemi yapamayacağının kanıtıdır, ek bir doğrulama gerekmez
🤖 butonun gizlenmesi yalnızca arayüz davranışıdır ve sunucunun aynı isteği reddedeceğini kanıtlamaz.
## B
Eksik, testin Page Object kullanmamasıdır; yetki testleri de sayfa nesneleri üzerinden yazılmalıdır
🤖 bu bir kod düzeni tercihidir ve doğrulamanın yetersizliğiyle ilgisi yoktur; Page Object kullanılsaydı da aynı eksik sürerdi.
## C
Buton gizlemek yetki denetimi değildir; depo görevlisinin belirteciyle fatura ucu doğrudan çağrılıp sunucunun 403 döndüğü doğrulanmalıdır
🤖 Doğru: yetki denetimi sunucu tarafında olmalıdır; arayüzde butonun gizlenmesi yalnızca kullanıcı deneyimi iyileştirmesidir.
## D
Eksik, testin yalnızca tek bir rolle koşulmasıdır; aynı senaryo tüm roller için tekrarlanırsa doğrulama tamamlanır
🤖 rol sayısını artırmak aynı yetersiz doğrulamayı çoğaltmaktan ibarettir; her rol için de yalnız butonun görünmediği kontrol edilmiş olur.
## Doğru
C
## Açıklama
ERP'de yetki bir arayüz meselesi değil, iç kontrol meselesidir. Arayüzde butonun görünmemesi kullanıcı deneyimi iyileştirmesidir; asıl denetim sunucu tarafında olmak zorundadır. Doğru negatif test, depo görevlisinin belirteciyle (token) `POST /api/fatura` ucunun **doğrudan** çağrılması ve **403** beklenmesidir — buradan 200 dönüyorsa arayüzde buton gizli olsa bile ciddi bir iç kontrol açığı vardır, çünkü isteği elle üretmek zor değildir. Doğrulama iki yönde derinleşir: dikey yetki yükseltme (düşük yetkili rolün üst yetki gerektiren işlemi çağırması) ve yatay yetki ihlali (doğru role sahip kullanıcının başka bir şubenin/kaydın verisine erişmesi). Buna denetim izi kontrolü eklenir: reddedilen denemenin de iz bırakması beklenir. A seçeneği tam da elenmeye yol açan cevaptır; B konuyu kod düzenine kaydırır; D ise yetersiz doğrulamayı çoğaltmaktan öteye geçmez.

# erpotom-6 | Uzman
## Soru
Gecelik ERP regresyon paketiniz zamanla 6 saate çıktı; koşum artık gece toplu işlerinin (maliyetlendirme, kur değerlemesi, e-belge gönderimi) penceresine taşıyor ve sabahları paketin farklı testleri açıklanamaz biçimde kırmızı geliyor. Paketin %80'i UI senaryolarından oluşuyor ve testlerin çoğu paylaşılan birkaç ürün ile tek bir test carisi üzerinden koşuyor. En doğru müdahale planı hangisidir?
## A
Doğrulamaların çoğu API/servis ve SQL katmanına indirilip UI'da yalnız kritik yollar bırakılır; koşum toplu işlerin bitiş sinyaline bağlanır; her test kendi damgalı ürün/carisini servis ucundan kurar; kalan paket risk bazlı ayrıştırılıp paralelleştirilir
🤖 Doğru: üç kök neden (yanlış katman, toplu işle çakışma, paylaşılan veri üzerinden çalışma) ayrı ayrı ve doğru sırayla ele alınır.
## B
Worker sayısı 2'den 8'e çıkarılır; koşum süresi dörtte birine ineceği için hem toplu iş penceresi sorunu hem kırmızı testler ortadan kalkar
🤖 testler paylaşılan ürün ve cari üzerinden koştuğu için paralelliği artırmak çakışmayı büyütür; ayrıca süre kısalsa da katman ve toplu iş bağımlılığı sorunları çözülmez.
## C
Kırmızı gelen testler kırılgan (flaky) kabul edilip otomatik yeniden deneme (retry) ile üç kez tekrarlanır; üç denemede geçen test başarılı sayılır ve 6 saatlik süre kabul edilir
🤖 toplu işle çakışma gerçek bir zamanlama sorunudur, yeniden deneme onu gizler; her düşüşü flaky etiketlemek teşhis değil, teşhisten kaçmaktır.
## D
Gecelik koşum tamamen kaldırılıp tam regresyon haftalığa alınır; böylece toplu iş penceresiyle çakışma ortadan kalkar
🤖 geri bildirim döngüsü bir haftaya çıkar ve gecelik duman güvencesi kaybolur; çakışma çözülür ama bedeli hatanın günlerce fark edilmemesidir.
## Doğru
A
## Açıklama
Tabloda üç ayrı kök neden iç içe geçmiştir ve her biri kendi çözümünü gerektirir. **Birincisi katman yanlışı:** paketin %80'i UI senaryosuysa süre tanım gereği uzundur; KDV/iskonto gibi iş kuralları birim/servis testine, sipariş → sevkiyat → fatura süreci API testine indirilir, UI'da yalnız sipariş girişi ve fatura ekranı gibi kritik yollar kalır — süreyi asıl kısaltan budur. **İkincisi zamanlama:** gece toplu işleri veriyi topluca değiştirir ve kilit tutar; o pencerede koşan test yarı işlenmiş veri görüp düşer. Bu bir kırılganlık değil bağımlılıktır, çözümü koşumu toplu işin bitiş sinyaline bağlamaktır — ayrıca toplu işin kendisi de doğrulanmalıdır. **Üçüncüsü veri paylaşımı:** paylaşılan birkaç ürün ve tek cari üzerinden koşan testler birbirinin stoğunu ve bakiyesini ezer; ana veri salt-okunur tutulmalı, her test kendi damgalı işlem verisini servis ucundan kurmalıdır — paralelleştirme ancak bu yapıldıktan sonra güvenlidir. B seçeneği tam da bu sırayı ters çevirdiği için paralelliği artırmak çakışmayı büyütür; C sorunu yeniden denemeyle örter; D ise çakışmayı çözerken geri bildirim döngüsünü bir haftaya çıkarıp gecelik güvenceyi feda eder.
