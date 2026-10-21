---
slug: erp-uctan-uca-veri-butunlugu-testi
baslik: "ERP'de Uçtan Uca Süreç ve Veri Bütünlüğü: Sipariş–Stok–Fatura–İade Zinciri ve Mutabakat"
kademe: İleri
sira: 236
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo paketinin fabrikadan müşterinin kapısına kadar olan yolculuğunu düşünün. Her el değiştirmede bir tutanak imzalanır: depodan çıkarken çıkış fişi, araca yüklenirken yükleme listesi, dağıtımda teslim imzası. Gün sonunda depo şefi tek bir soru sorar: "Sabah 1.240 koli vardı, 380 koli çıktı, şimdi 860 koli mi var?" Sayı tutmuyorsa bir yerde bir tutanak eksiktir ya da iki kez yazılmıştır. ERP testinin en güçlü aracı tam olarak bu sorudur; adı da **mutabakattır (reconciliation)**.

E-ticarette sipariş, sitede "Siparişiniz alındı" ekranıyla bitmez. Asıl hikâye ERP tarafında **başlar**. Stok rezerve edilir, toplama listesi düşer, sevkiyat yapılır, fatura kesilir. Ardından kargo bilgisi işlenir ve tahsilat eşleştirilir. İade gelirse zincir geriye doğru sarılır. Tüm bu adımların muhasebedeki karşılığı da ayrıca oluşur.

Bir önceki konuda (`235`) ERP testinin neden ekran testinden farklı olduğunu gördük. Bu konu ise o farkın **pratik karşılığıdır**. Üç soruya cevap arayacağız: uçtan uca zinciri nasıl kurarız, her adımda neyi doğrularız ve "defter tutuyor mu" sorusunu nasıl sayısal olarak cevaplarız. Mülakatta bu konu, bir QA adayını "ekran testçisi"nden ayıran yerdir. Sebebi şudur: buradaki hataların hiçbiri ekranda kırmızı bir mesaj olarak görünmez. Bu hatalar ay sonunda envanter farkı, kuruş farkı veya vergi dairesine giden yanlış bir belge olarak ortaya çıkar.

### Temel kavramlar

- **Uçtan uca zincir (end-to-end chain).** Site siparişi → ERP'ye aktarım → stok rezervasyonu → toplama/sevkiyat → fatura (e-arşiv/e-fatura) → kargo → tahsilat → (varsa) iade/değişim → muhasebe. Zincirin her halkası bir **belge** üretir ve bu belge bir öncekine referans verir. "Bu faturanın kaynağı hangi sipariş?" sorusunun cevabı işte bu referans zinciridir.
- **Rezervasyon ile stok düşümü farkı.** Sipariş verildiğinde fiziksel stok genelde **düşmez**, **rezerve edilir**. Yani satılabilir stok azalır ama depodaki fiili miktar aynı kalır. Fiili düşüş sevkiyat/irsaliye anında olur. Bu ayrım test için kritiktir. "Sipariş sonrası stok azaldı mı?" diye bakmak yanlış soru olabilir. Doğru soru şudur: "Satılabilir stok azaldı, fiili stok aynı kaldı mı?"
- **Çift taraflı kayıt ve denklik.** Muhasebede her fiş için **borç toplamı = alacak toplamı** olmak zorundadır. Bu, ERP'de yazılabilecek en ucuz ve en yüksek getirili **doğrulamalardan (assertion)** biridir. Denklik bozulmuşsa kesin bir hata vardır; tartışmaya gerek yoktur.
- **Belge tutarlılığı.** Şu eşitlik geçerlidir: `belge toplamı = satır toplamları − iskontolar + KDV`. Satır satır hesaplanan tutarlar, belge başlığındaki tutarla uyuşmalıdır. Uyuşmuyorsa iki ihtimal vardır: hesaplama iki ayrı yerde ayrı ayrı yapılmıştır ya da yuvarlama farkı birikmiştir.
- **Mutabakat (reconciliation) ve kontrol toplamı (control total).** Mutabakat, iki bağımsız kaynaktan gelen sayıların eşitliğini kontrol etmektir. Örnekler: sitedeki sipariş adedi ile ERP'deki sipariş adedi, stok hareketleri toplamı ile stok bakiyesi, tahsilat toplamı ile cari alacak azalışı. Tek tek senaryolar hatanın **varlığını** gösterir; mutabakat ise hatanın **kaçırıldığını** gösterir.
- **Tekrar edilse de sonucu değişmezlik (idempotency).** Aynı isteğin bir kez ya da beş kez gönderilmesi **aynı** sonucu doğurmalıdır. Dağıtık entegrasyonda **yeniden deneme (retry)** kaçınılmazdır. Bu özellik yoksa yeniden deneme çift sipariş, çift stok düşümü ve çift fatura demektir. Uygulama yolu genelde **benzersiz bir iş anahtarıdır**: site sipariş numarası ya da ayrı bir tekrar anahtarı (idempotency key). Bu anahtar veritabanı seviyesinde bir benzersizlik kısıtıyla desteklenir.
- **Kısmi başarı ve telafi (compensation).** Zincirin ortasında bir adım başarısız olabilir; örneğin sipariş ERP'ye düşer ama fatura kesilemez. Böyle bir durum tek bir veritabanı işlemiyle geri alınamaz. Sistem ya **yeniden dener** ya da **telafi edici bir işlem** yürütür (ters kayıt, iptal). Test edilmesi gereken şey mutlu yol değil, bu ara durumlardır.
- **Aşırı satış (overselling).** Son bir adet ürünü iki müşterinin aynı anda satın alabilmesidir. Kaynağı eşzamanlılıktır; çözümü kilitleme veya atomik güncellemedir (`225`). QA'in işi bu durumu **kasten** üretebilmektir.
- **Yuvarlama ve para.** Burada üç soru vardır: iskonto ile KDV hangi sırayla hesaplanır, hangi seviyede hesaplanır (satır mı belge mi) ve kaç basamakta yuvarlanır. Kuruş farkları tek belgede önemsiz görünür. Ama mutabakatta birikip toplamı bozarlar. Para hesabının kod tarafındaki doğru yapılışı `226`'da işlendi.
- **Kur farkı.** Dövizli bir siparişte sipariş anındaki kur ile fatura/tahsilat anındaki kur farklıysa aradaki fark muhasebede ayrı bir kalem olarak doğar.
- **e-Belgeler.** Türkiye'de fatura ve irsaliye elektronik ortamda düzenlenir: e-fatura, e-arşiv, e-irsaliye. Bu belgeler GİB tarafındaki sistemlerle entegre çalışır. Test konusu olan üç şey vardır: belge numaralarının **sıralı ve boşluksuz** olması, belgenin karşı tarafa ulaşıp ulaşmadığı ve reddedilen belgeye ne olduğu.
- **Veri göçü (migration).** Eski sistemden yeni ERP'ye açılış bakiyelerinin, ana verinin ve açık belgelerin taşınmasıdır. Bir kerelik iş gibi görünür. Oysa **en pahalı ve en sık atlanan** testtir.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Siparisten muhasebeye ucdan uca zincir ve her adimda dogrulananlar">
  <defs>
    <marker id="erpe2e-ok" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Zincirin her halkasında ayrı bir doğrulama sorusu vardır</text>
  <rect x="18" y="36" width="118" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="77" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1. Site siparişi</text>
  <line x1="136" y1="56" x2="176" y2="56" stroke="#186399" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="180" y="36" width="118" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="239" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2. ERP'ye aktarım</text>
  <line x1="298" y1="56" x2="338" y2="56" stroke="#186399" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="342" y="36" width="118" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="401" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3. Rezervasyon</text>
  <line x1="460" y1="56" x2="500" y2="56" stroke="#186399" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="504" y="36" width="118" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="563" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">4. Sevkiyat</text>
  <text x="77" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tutar, stok görünürlüğü</text>
  <text x="239" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek belge, doğru satırlar</text>
  <text x="401" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">satılabilir azalır, fiili aynı</text>
  <text x="563" y="92" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">fiili stok düşer, irsaliye</text>
  <rect x="18" y="112" width="118" height="40" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="77" y="137" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">5. Fatura</text>
  <line x1="136" y1="132" x2="176" y2="132" stroke="#186399" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="180" y="112" width="118" height="40" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="239" y="137" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">6. Kargo</text>
  <line x1="298" y1="132" x2="338" y2="132" stroke="#186399" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="342" y="112" width="118" height="40" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="401" y="137" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">7. Tahsilat</text>
  <line x1="460" y1="132" x2="500" y2="132" stroke="#186399" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="504" y="112" width="118" height="40" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="563" y="137" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">8. İade / değişim</text>
  <text x="77" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tutar, KDV, belge no sırası</text>
  <text x="239" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">takip no, durum akışı</text>
  <text x="401" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">cari alacak azalır</text>
  <text x="563" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">zincir geriye sarılır</text>
  <rect x="180" y="190" width="300" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="330" y="207" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">9. Muhasebe — her adımın karşılığı</text>
  <text x="330" y="224" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">her fişte borç toplamı = alacak toplamı</text>
  <line x1="563" y1="152" x2="563" y2="176" stroke="#B23B3B" stroke-width="1.6"/>
  <line x1="563" y1="176" x2="482" y2="176" stroke="#B23B3B" stroke-width="1.6"/>
  <line x1="482" y1="176" x2="482" y2="190" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#erpe2e-ok)"/>
  <rect x="18" y="250" width="624" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="270" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Zincir testinin altın kuralı</text>
  <text x="330" y="289" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Her halkada üç şey doğrulanır: (1) belge doğdu mu, (2) tutar ve miktar doğru mu, (3) bir öncekine referansı doğru mu.</text>
  <text x="330" y="307" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Üçünden biri atlanırsa hata zincirin sonunda değil, ay sonu raporunda ortaya çıkar.</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

1. **Zinciri halkalara böl, her halkaya kendi doğrulamasını yaz.** Uçtan uca senaryo tek bir dev doğrulamayla bitirilmez. Her adımda üç soru sorulur: belge doğdu mu, tutar/miktar doğru mu, önceki belgeye referansı doğru mu? Sevkiyat adımında irsaliyenin siparişe bağlı olduğunu doğrularsınız. Fatura adımında ise faturanın irsaliyeye bağlı olduğunu doğrularsınız. Bu iki kontrol, zincirin kopmadığının kanıtıdır.
2. **Ekranın bir kat altına in.** Bir sipariş kaydedildiğinde arka planda dört şey oluşmalıdır. **Stok tarafında** bir rezervasyon kaydı; yani satılabilir miktar düşer. **Cari tarafında** bakiye değişimi. **Muhasebe tarafında** dengeli bir fiş. **Belge tarafında** sıralı bir numara. Ayrıca aritmetik de tutmalıdır: `sipariş toplamı = Σ(satır adet × birim fiyat) − iskontolar + KDV`. Bu kontroller veritabanı sorgusuyla, ERP'nin standart raporuyla veya sorgulama ucuyla (API) yapılır.
3. **Mutabakat sorgularını testin bir parçası yap.** Bu, ERP testinin en güçlü ve en çok atlanan aracıdır. Tek tek senaryolar "bu akış çalışıyor mu?" diye sorar. Mutabakat ise "**hiçbir şeyi kaçırdım mı?**" diye sorar. Tipik kontroller:

```sql
-- 1) Sipariş adedi mutabakatı: sitede kaç sipariş oluştu, ERP'ye kaçı düştü, kaçı faturalandı?
SELECT (SELECT COUNT(*) FROM site_siparis  WHERE tarih = :gun)                       AS site_adet,
       (SELECT COUNT(*) FROM erp_siparis   WHERE tarih = :gun)                       AS erp_adet,
       (SELECT COUNT(*) FROM erp_fatura    WHERE tarih = :gun AND iptal = false)     AS fatura_adet;
-- Beklenen: site_adet = erp_adet;  fatura_adet <= erp_adet (sevk edilmemişler hariç, farkı isimlendirebilmelisin)

-- 2) Stok mutabakatı: hareketlerin toplamı bakiyeye eşit mi?
SELECT b.urun_kodu, b.depo_kodu, b.miktar AS bakiye,
       COALESCE(SUM(h.miktar), 0)          AS hareket_toplami
FROM   stok_bakiye b
LEFT JOIN stok_hareket h
       ON h.urun_kodu = b.urun_kodu AND h.depo_kodu = b.depo_kodu
GROUP BY b.urun_kodu, b.depo_kodu, b.miktar
HAVING b.miktar <> COALESCE(SUM(h.miktar), 0);
-- Beklenen: HİÇ satır dönmemeli. Dönen her satır bir veri bütünlüğü hatasıdır.

-- 3) Tahsilat mutabakatı: gün içindeki tahsilat toplamı, cari alacak azalışına eşit mi?
SELECT (SELECT COALESCE(SUM(tutar), 0) FROM tahsilat        WHERE tarih = :gun) AS tahsilat_toplami,
       (SELECT COALESCE(SUM(alacak), 0) FROM cari_hareket   WHERE tarih = :gun) AS cari_alacak_toplami;

-- 4) Muhasebe denkliği: dengesiz fiş var mı?
SELECT fis_no, SUM(borc) AS borc, SUM(alacak) AS alacak
FROM   muhasebe_fis_satir
WHERE  tarih = :gun
GROUP BY fis_no
HAVING SUM(borc) <> SUM(alacak);
-- Beklenen: HİÇ satır dönmemeli.
```

Bu sorguların değeri şuradadır: bir tanesi bile kırmızı dönerse **sistemde bir bütünlük hatası olduğunu kanıtlamış** olursunuz. Hangi senaryonun bozuk olduğunu bilmeseniz bile bu kanıt elinizdedir. Bu sorguları her gece koşan bir kontrol işi (control job) kurmak, ERP QA'inin kurabileceği en yüksek getirili şeydir.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bir siparis kaydedildiginde arka planda olusmasi gerekenler">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Tek tıklama, dört ayrı defterde iz</text>
  <rect x="234" y="34" width="192" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="330" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sipariş kaydedildi</text>
  <text x="330" y="68" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ekranda: "başarıyla oluşturuldu"</text>
  <line x1="330" y1="76" x2="330" y2="92" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="98" y1="92" x2="562" y2="92" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="98" y1="92" x2="98" y2="106" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="252" y1="92" x2="252" y2="106" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="408" y1="92" x2="408" y2="106" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="562" y1="92" x2="562" y2="106" stroke="#5D7789" stroke-width="1.4"/>
  <rect x="20" y="106" width="156" height="94" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="98" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Stok</text>
  <text x="98" y="146" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">rezervasyon kaydı</text>
  <text x="98" y="162" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">satılabilir miktar azalır</text>
  <text x="98" y="178" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">fiili miktar HENÜZ aynı</text>
  <text x="98" y="194" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(düşüş sevkiyatta)</text>
  <rect x="184" y="106" width="146" height="94" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="257" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Cari / finans</text>
  <text x="257" y="146" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">müşteri bakiyesi</text>
  <text x="257" y="162" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ödeme koşulu, vade</text>
  <text x="257" y="180" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tahsilat beklentisi</text>
  <rect x="338" y="106" width="146" height="94" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="411" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Belge</text>
  <text x="411" y="146" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sıralı belge numarası</text>
  <text x="411" y="162" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">boşluk olmamalı</text>
  <text x="411" y="180" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">denetim izi kaydı</text>
  <rect x="492" y="106" width="148" height="94" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="566" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Muhasebe</text>
  <text x="566" y="146" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yevmiye fişi</text>
  <text x="566" y="164" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">borç = alacak</text>
  <text x="566" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">doğru döneme düşmeli</text>
  <rect x="20" y="216" width="620" height="70" rx="9" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.6"/>
  <text x="330" y="236" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Aritmetik de bir assertion'dır</text>
  <text x="330" y="256" text-anchor="middle" font-size="11" fill="#186399" font-family="system-ui">sipariş toplamı  =  Σ(adet × birim fiyat)  −  iskontolar  +  KDV</text>
  <text x="330" y="276" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Bu eşitlik tutmuyorsa hesap iki ayrı yerde ayrı ayrı yapılıyor ya da yuvarlama farkı birikiyordur.</text>
</svg>
```

4. **Para ve yuvarlamayı ayrı bir test alanı say.** Aynı sipariş, hesaplama sırası değiştiğinde farklı bir toplam üretebilir. Şu soruları sormak gerekir. İskonto KDV'den **önce** mi uygulanıyor, yani matrahı düşürüyor mu; yoksa sonra mı uygulanıyor? İskonto **satır bazında** mı hesaplanıyor, yoksa **belge bazında** hesaplanıp satırlara mı dağıtılıyor? Yuvarlama **her satırda** mı, yoksa **belge toplamında** mı yapılıyor? Kaç ondalık basamak kullanılıyor? Üç satırlık bir belgede her satırı ayrı ayrı yuvarlamakla toplamı bir kez yuvarlamak arasında bir kuruş fark çıkabilir. Tek belgede önemsiz görünen bu fark, günde on bin belgede birikince mutabakatı bozar. Muhasebeye de "kaynağı bilinmeyen fark" olarak yansır. QA'in işi hangi yolun doğru olduğuna karar vermek değildir. QA'in işi **kuralın yazılı olmasını sağlamak ve uygulamanın o kurala her yerde aynı şekilde uyduğunu doğrulamaktır.** Kod tarafındaki doğru para tipi ve yuvarlama davranışı `226`'da işlendi; burada tekrar edilmiyor.
5. **Kısmi senaryoları mutlu yolun önüne al.** Gerçek hayatta siparişlerin kayda değer bir kısmı düz gitmez. Dört tip kısmi senaryo test edilmelidir. Birincisi **kısmi teslim**: 5 kalemin 3'ü bugün, 2'si stok gelince sevk edilir. Burada fatura bir mi iki mi kesilir, kalan sipariş açık kalır mı? İkincisi **kısmi iade**: 3 üründen 1'i iade edilir. İade tutarı satır bazında hesaplanır. Belge seviyesindeki iskonto, iade edilen satıra **orantılı** dağıtılır. KDV bu tutar üzerinden geri alınır. Stokun **hangi depoya** gireceği de bir sorudur: sağlam ürün satılabilir depoya, hasarlı ürün karantina deposuna girer. Muhasebede de bu iadenin karşılığı oluşur. Üçüncüsü **değişim**: bu işlem bir iade artı bir yeni satış mıdır, yoksa tek belge midir? Dördüncüsü **sipariş iptali**: sevk edilmemişse rezervasyon serbest bırakılır, sevk edilmişse iş iade sürecine döner. Her birinde aynı dört defter (stok, cari, belge, muhasebe) **geriye doğru** ve tutarlı biçimde güncellenmelidir.
6. **Eşzamanlılığı kasten üret.** "Aynı ürünün son adedini iki müşteri aynı anda alırsa ne olur?" sorusu ERP mülakatlarının klasiğidir. Doğru cevap "olmaz" değildir; olur. Beklenen davranış şudur: bir sipariş başarılı olur, diğeri **stok yetersiz** hatasıyla reddedilir. Stok **hiçbir koşulda eksiye düşmez** ve iki rezervasyon birden oluşmaz. Bunu test etmek için istekleri sırayla değil, **aynı anda** göndermek gerekir:

```java
// Java + TestNG: son 1 adet için iki eşzamanlı sipariş — aşırı satış (overselling) avı
@Test
public void sonAdetIcinEszamanliIkiSiparis() throws Exception {
    String urun = testVeri.stokMiktariAyarla("TEST-URUN", "ANA-DEPO", 1);   // tam 1 adet

    var kapi = new CountDownLatch(1);                  // ikisini aynı anda salmak için
    var havuz = Executors.newFixedThreadPool(2);
    Callable<Integer> siparisVer = () -> { kapi.await(); return siparisApi.olustur(urun, 1); };

    var f1 = havuz.submit(siparisVer);
    var f2 = havuz.submit(siparisVer);
    kapi.countDown();                                   // iki istek aynı anda yola çıkar

    List<Integer> kodlar = List.of(f1.get(), f2.get());
    assertThat(kodlar).as("tam olarak biri başarılı olmalı")
                      .containsExactlyInAnyOrder(201, 409);   // biri oluştu, biri stok yetersiz

    assertThat(stokSorgu.fiiliMiktar(urun, "ANA-DEPO"))
        .as("stok hiçbir koşulda eksiye düşmemeli").isGreaterThanOrEqualTo(0);
    assertThat(stokSorgu.rezervasyonAdedi(urun)).as("tek rezervasyon oluşmalı").isEqualTo(1);
    havuz.shutdown();
}
```

Bu testin kırılması bir **kararsızlık (flaky)** belirtisi değil, gerçek bir hatadır. Altında yatan konu iyimser/kötümser kilitleme ve atomik güncelleme kararıdır (`225`). Aynı sorunun ikinci bir yüzü daha vardır: sitenin ERP'den gecikmeli aldığı stok kopyası. Sitede "son 1 adet" yazarken ERP'de stok çoktan bitmiş olabilir. Bu durumda doğru davranış sipariş verilmesini engellemek değildir. Doğru davranış **siparişi kabul etmek, ERP tarafında reddetmek ve müşteriye bunu bildirmektir**. Bu senaryo mutlaka test edilmelidir; çünkü kampanya günlerinde istisna değil, kuraldır.

7. **Entegrasyon hata senaryolarını mutlu yol kadar ciddiye al.** Site ile ERP arasındaki hat asla yüzde yüz ayakta olmaz. Test edilmesi gerekenler:
   - **ERP kapalıyken gelen sipariş.** Üç olası davranış vardır. Sipariş kaybolabilir; bu bir **hatadır**. Kuyruğa alınıp sonra işlenebilir; bu **iyidir**. Müşteriye açık bir hata dönebilir; bu **duruma göre kabul edilebilir**. Sessizce kaybolan sipariş, ERP entegrasyonlarının en pahalı hatasıdır. Üstelik bu hatayı yalnızca mutabakat yakalar.
   - **Çift gönderim ve tekrar güvenliği (idempotency).** Ağ zaman aşımından sonra yapılan yeniden deneme, aynı siparişi ikinci kez gönderir. Koruma yoksa sonuç çift belge, çift stok düşümü ve çift faturadır. Doğru çözümün üç parçası vardır. Birincisi, gönderen taraf **benzersiz bir iş anahtarı** taşır: site sipariş numarası veya ayrı bir tekrar anahtarı. İkincisi, ERP aynı anahtarı ikinci kez gördüğünde **yeni belge yaratmaz, mevcut sonucu döndürür**. Üçüncüsü, bu davranış veritabanı seviyesinde bir benzersizlik kısıtıyla garanti altına alınır. Testi çok basittir ve şaşırtıcı sıklıkta hata bulur: **birebir aynı isteği iki kez gönderin**, sonra tek sipariş, tek stok hareketi ve tek fatura oluştuğunu doğrulayın.
   - **Kısmi başarı ve telafi.** Sipariş ERP'ye düşmüş ama fatura kesilememiş olabilir. Ya da irsaliye oluşmuş ama kargo servisine iletilememiştir. Sistem bu durumda ya yeniden denemeli ya da telafi işlemi yürütmelidir. QA'in sorusu şudur: bu ara durumda kalan kayıtlar **görünür** mü? Yani bir "bekleyen/hatalı" listesi var mı, yoksa kayıtlar kimse fark etmeden orada mı duruyor?
   - **Senkronizasyon gecikmesi.** Sitede görünen stok ve fiyat, ERP'deki gerçeğin gecikmeli bir kopyasıdır. Kabul edilebilir gecikme nedir? Gecikme sırasında verilen sipariş ne olur? Fiyat değiştiyse hangi fiyat geçerlidir? Beklenen davranış şudur: siparişin verildiği andaki fiyat belgeye **yazılmış** olmalıdır. Fiyatın sonradan fiyat listesinden okunması bir hatadır.
8. **Türkiye'ye özgü mevzuat testlerini listeye ekle.** e-Fatura, e-arşiv ve e-irsaliye süreçleri GİB tarafındaki sistemlerle entegre çalışır. Bu yüzden faturalama testi yalnız "tutar doğru mu?" testi değildir. Şunlar da sorulur: belge karşı tarafa gitti mi, kabul mü edildi yoksa red mi, red edilirse süreç ne yapıyor? Ayrıca dört konu daha test edilir. **Belge numaralarının sıralı ve boşluksuz** olması — araya giren başarısız bir denemenin bir numarayı yakıp atlaması bir bulgudur. **KDV oranı değiştiğinde** eski tarihli siparişlerin hangi oranla faturalandığı. **Tevkifat** gibi özel vergi durumlarının doğru hesaplanması. **Dövizli siparişlerde** sipariş anı ile fatura anı arasındaki **kur farkının** ayrı bir kalem olarak doğması. Burada QA için önemli bir meslek disiplini vardır: **oran, tutar ve madde numarası ezberden yazılmaz.** Mevzuat değişir. Doğru davranış şudur: geçerli kuralı mali müşavirden veya finans ekibinden yazılı olarak alın, senaryoya kaynak göstererek ekleyin ve test verisini o kaynağa göre kurun. Mülakatta "KDV oranı şudur" diye kesin konuşmayın. Bunun yerine "bu oranı finans ekibinden teyit alıp senaryoya kaynak olarak bağlarım" demek daha güçlü bir cevaptır.
9. **Dönem sonu ve toplu işleri (batch) test et.** Ay sonu kapanış, toplu faturalama, toplu fiyat güncelleme ve gün sonu mutabakat işleri ERP'nin en kritik parçalarıdır. Aynı zamanda en az test edilen parçalarıdır. Dört soru sorulur. İş **yarıda kalırsa** ne olur; işlenen kayıtlar kalıcı mı, yeniden başlatınca kaldığı yerden mi devam ediyor? İş **iki kez** çalışırsa ne olur; iki kez çalışan bir toplu faturalama işi çift fatura üretmemelidir, yani tekrar güvenliği burada da geçerlidir. **Gün sonu penceresinde** verilen sipariş hangi güne ve döneme düşüyor? **Kapanmış bir döneme** ait iade geldiğinde kayıt hangi döneme atılıyor? Toplu iş çerçevesinin mekaniği — yeniden başlatılabilirlik, parça parça işleme — `223`'te işlendi. Buradaki mesele, o mekaniğin **iş sonucuna** etkisini doğrulamaktır.
10. **Veri göçünü (migration) bir test projesi olarak ele al.** Eski sistemden yeni ERP'ye taşınan şey yalnız tablolar değildir; kurumun geçmişidir. Doğrulama dört katmanlıdır. **(a) Sayı**: aktarılan kayıt adetleri eşleşiyor mu — ürün, cari, açık sipariş, açık fatura? **(b) Kontrol toplamı**: toplam cari bakiye, toplam stok miktarı ve stok değeri, açık fatura toplamı iki sistemde eşit mi? **(c) Örneklem**: rastgele ve **riskli** kayıtlardan seçilen örnekler alan alan karşılaştırılıyor mu? Riskli kayıt örnekleri şunlardır: en büyük bakiyeli cari, en çok hareketi olan ürün, dövizli açık fatura, eksi bakiyeli müşteri. **(d) Durum**: açık siparişler açık, kapalılar kapalı mı geldi; kısmi teslim edilmiş siparişlerin kalan miktarı doğru mu? Buna bir de **tekrar çalıştırılabilirlik** eklenir: göç betiği yarıda kalırsa veya iki kez koşarsa veri iki katına çıkmamalıdır. Göç bir kez yapılır ama **yanlış yapılırsa sonuçları yıllarca sürer**. Buna rağmen zaman baskısı altında en çok kısılan test kalemidir.

```svg
<svg viewBox="0 0 660 296" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mutabakat panosu: iki bagimsiz kaynagin sayilarinin karsilastirilmasi">
  <text x="330" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Mutabakat: senaryolar hatayı bulur, mutabakat kaçırdığını söyler</text>
  <rect x="20" y="36" width="620" height="26" rx="6" fill="#1C3247"/>
  <text x="112" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kontrol</text>
  <text x="300" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Sol taraf</text>
  <text x="470" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Sağ taraf</text>
  <text x="590" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Beklenen</text>
  <rect x="20" y="62" width="620" height="34" fill="#EAF1F7"/>
  <text x="112" y="83" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Sipariş adedi</text>
  <text x="300" y="83" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">site sipariş sayısı</text>
  <text x="470" y="83" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ERP sipariş sayısı</text>
  <text x="590" y="83" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">eşit</text>
  <rect x="20" y="96" width="620" height="34" fill="#FFFFFF"/>
  <text x="112" y="117" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Stok</text>
  <text x="300" y="117" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hareketlerin toplamı</text>
  <text x="470" y="117" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">stok bakiyesi</text>
  <text x="590" y="117" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">eşit</text>
  <rect x="20" y="130" width="620" height="34" fill="#EAF1F7"/>
  <text x="112" y="151" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Tahsilat</text>
  <text x="300" y="151" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">gün içi tahsilat toplamı</text>
  <text x="470" y="151" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">cari alacak azalışı</text>
  <text x="590" y="151" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">eşit</text>
  <rect x="20" y="164" width="620" height="34" fill="#FFFFFF"/>
  <text x="112" y="185" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Muhasebe</text>
  <text x="300" y="185" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">fiş borç toplamı</text>
  <text x="470" y="185" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">fiş alacak toplamı</text>
  <text x="590" y="185" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">eşit</text>
  <rect x="20" y="198" width="620" height="34" fill="#FBE9E9"/>
  <text x="112" y="219" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Belge numarası</text>
  <text x="300" y="219" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">en küçük ile en büyük arası</text>
  <text x="470" y="219" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">mevcut belge adedi</text>
  <text x="590" y="219" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">boşluk yok</text>
  <rect x="20" y="246" width="620" height="42" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="264" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Her satır iki BAĞIMSIZ kaynaktan gelir. Aynı sorgudan iki kez okumak mutabakat değildir.</text>
  <text x="330" y="281" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Gecelik koşan bir kontrol işi hâline getirildiğinde, kimsenin senaryo yazmadığı hataları da yakalar.</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Bir siparişin uçtan uca testini anlat."** Zayıf cevap ekran adımlarını sıralar. Güçlü cevap ise zinciri halkalara böler. Her halkada **hangi belgenin doğduğunu, hangi tutarın doğrulandığını ve önceki belgeye referansın kontrol edildiğini** söyler. Sonunda muhasebe karşılığını ve dört defterin (stok, cari, belge, muhasebe) tutarlılığını anar.
- **"Sipariş kaydedildi. Sen ne kontrol edersin?"** Beklenen liste: rezervasyon/stok hareketi, cari bakiye, dengeli muhasebe fişi, sıralı belge numarası ve `toplam = satırlar − iskonto + KDV` aritmetiği. "Sipariş listesinde göründüğünü kontrol ederim" cevabı zayıftır.
- **"Mutabakat testi nedir, neden önemli?"** Beklenen cevap: iki **bağımsız** kaynaktan gelen sayıların eşitliğini kontrol etmek. Ayırt edici cümle şudur: senaryolar bildiğiniz hataları arar, mutabakat ise **bilmediğiniz** hataları yakalar. Özellikle "sessizce kaybolan kayıt" sınıfını başka hiçbir test yakalamaz.
- **"Aynı ürünün son adedini iki kişi aynı anda alırsa?"** Tuzak, "böyle bir şey olmaz" veya "sıralı iki test koşarım" demektir. Beklenen cevap şudur: istekleri **gerçekten eşzamanlı** gönderirim — ortak bir bariyerle aynı anda salınan iki iş parçacığıyla. Birinin başarılı olmasını, diğerinin stok yetersiz hatasıyla reddedilmesini beklerim. Ayrıca stokun eksiye düşmediğini ve tek rezervasyon oluştuğunu doğrularım.
- **"Aynı sipariş ERP'ye iki kez düşerse?"** Beklenen kavram **tekrar güvenliğidir (idempotency)**: benzersiz iş anahtarı, ikinci istekte yeni belge yaratmama, veritabanı seviyesinde benzersizlik kısıtı. Testi şudur: birebir aynı isteği iki kez gönderin; tek belge, tek stok hareketi ve tek fatura oluştuğunu doğrulayın. "Yeniden denemeyi kapatırız" cevabı yanlıştır. Yeniden deneme dağıtık sistemlerde vazgeçilmezdir; çözüm onu güvenli kılmaktır.
- **"Kısmi iadede neyi test edersin?"** Beklenen cevapta beş madde vardır: tutarın satır bazında hesaplanması, belge iskontosunun iade edilen satıra **orantılı** dağıtılması, KDV'nin bu tutar üzerinden geri alınması, stokun **doğru depoya** (sağlam veya hasarlı) girmesi ve muhasebede ters kaydın oluşması. "Toplamın üçte birini iade ederim" cevabı, iskonto ve KDV dağılımını görmediğinizi gösterir.
- **"Kuruş farkı nereden çıkar?"** Beklenen: iskonto/KDV sırası ve yuvarlamanın satır mı belge mi seviyesinde yapıldığı. Kritik ek cümle: tek belgede önemsiz olan fark, günlük binlerce belgede birikip mutabakatı bozar.
- **"Mevzuatla ilgili ne test edersin?"** Beklenen liste: e-fatura/e-arşiv/e-irsaliye gönderim ile kabul/red akışı, belge numarası sıralılığı ve boşluksuzluğu, KDV oranı değişiminde eski siparişlerin durumu, tevkifat, dövizli siparişte kur farkı. **Tuzak:** kesin bir oran veya madde numarası söylemek. Doğru duruş, kuralı finans ekibinden ya da mali müşavirden yazılı almak ve senaryoya kaynak olarak bağlamaktır.
- **"Veri göçünü nasıl test edersin?"** Beklenen dört katman şudur: kayıt sayısı, kontrol toplamları, örneklem karşılaştırması (rastgele **ve** riskli kayıtlar) ve açık belgelerin durum ile kalan miktar doğruluğu. Buna göçün tekrar çalıştırılabilir olması da eklenir. "Sayılar tutuyorsa tamamdır" cevabı yaygın ve yanlıştır; doğru sayıda yanlış kayıt taşımak mümkündür.

### Tek cümlede

ERP'de uçtan uca test; zincirin her halkasında doğan belgeyi, tutarı ve referansı doğrulamak, sonucu stok–cari–belge–muhasebe dörtlüsünde **veri katmanında** kanıtlamak ve kısmi, eşzamanlı, hatalı yolları mutlu yol kadar ciddiye almaktır. Nihayetinde gün sonunda **mutabakat** sorularıyla "hiçbir şey kaybolmadı, hiçbir şey iki kez sayılmadı" diyebilmektir.

# erpakis-1 | Orta
## Soru
Kendi yazılan ERP'de bir satış siparişi kaydedildikten sonra QA'in yapması gereken **arka plan** doğrulaması aşağıdakilerden hangisidir?
## A
Sipariş tablosunda yeni bir satır oluştuğunu görmek yeterlidir; diğer modüller bu satırdan türeyeceği için ayrıca kontrol gerekmez
🤖 satırın var olması, ondan türemesi gereken kayıtların gerçekten ve doğru biçimde oluştuğunu göstermez.
## B
Stok hareketi/rezervasyon, cari bakiye, dengeli muhasebe fişi (borç = alacak) ve sıralı belge numarasının oluştuğu; ayrıca belge toplamı aritmetiğinin tuttuğu doğrulanır
🤖 Doğru: tek bir kayıt işlemi dört ayrı defterde iz bırakır ve aritmetiğin kendisi de doğrulanması gereken bir sonuçtur.
## C
Sipariş durum alanının "Onaylandı" değerine geçtiğini kontrol etmek, arka plandaki tüm işlemlerin başarıyla tamamlandığının kanıtıdır
🤖 durum alanı bir etikettir; alt işlemler kısmen başarısız olsa bile aynı değere geçmiş olabilir.
## D
Kargo takip numarasının üretilmiş olması yeterlidir; numara varsa zincirin tüm önceki adımları doğru çalışmış demektir
🤖 kargo numarası zincirin geç bir halkasıdır ve stok, cari veya muhasebe tarafındaki bir hatayı hiçbir şekilde ele vermez.
## Doğru
B
## Açıklama
Bir satış siparişi ERP'de tek bir tabloya yazılan kayıt değildir: stok tarafında rezervasyon (veya sevkiyatta hareket), finans tarafında cari bakiye değişimi, muhasebe tarafında dengeli bir yevmiye fişi ve belge tarafında sıralı bir numara birlikte doğmalıdır. Bunlara ek olarak aritmetik de bir doğrulama konusudur: belge toplamı, satır toplamlarından iskontolar düşülüp KDV eklenerek elde edilen değere eşit olmalıdır; eşit değilse hesap büyük olasılıkla iki ayrı yerde ayrı ayrı yapılıyordur ya da yuvarlama farkı birikiyordur. Sipariş satırının varlığını (A) veya durum alanının değerini (C) kanıt saymak, kısmi başarı durumlarını tümüyle gözden kaçırır — bu durumlarda ana kayıt oluşup alt kayıtlar oluşmayabilir. Kargo takip numarası (D) zincirin geç bir halkasıdır ve muhasebe ya da stok tarafındaki bir tutarsızlık hakkında hiçbir bilgi vermez.

# erpakis-2 | Orta
## Soru
ERP testinde "mutabakat (reconciliation) testi" denen şey nedir ve neden bu kadar değerlidir?
## A
Her ekrandaki zorunlu alanların ve alan biçimlerinin (tarih, tutar, uzunluk) kurallara uygunluğunun tek tek doğrulanmasıdır
🤖 bu bir alan/girdi doğrulama testidir, iki kaynağın sayısal tutarlılığını karşılaştırmaz.
## B
Aynı test senaryosunun iki farklı tarayıcıda koşturulup sonuçların birbiriyle karşılaştırılmasıdır
🤖 bu bir tarayıcı uyumluluk kontrolüdür; ERP'nin veri bütünlüğüyle ilgisi yoktur.
## C
Test ortamı ile üretim ortamının sürüm numaralarının ve konfigürasyon dosyalarının eşleştiğinin doğrulanmasıdır
🤖 bu bir ortam/yapılandırma kontrolüdür, verinin tutarlılığını ölçmez.
## D
İki bağımsız kaynaktan gelen sayıların eşitliğinin kontrolüdür; değeri, senaryosu hiç yazılmamış hataları ve sessizce kaybolan kayıtları da yakalamasıdır
🤖 Doğru: senaryolar bilinen akışları sınar, mutabakat ise sistemin bütününde kaçırılmış olanı ortaya çıkarır.
## Doğru
D
## Açıklama
Mutabakat, birbirinden bağımsız iki kaynağın aynı gerçeği aynı sayıyla anlatıp anlatmadığını kontrol eder: gün sonunda sitedeki sipariş adedi ERP'deki sipariş adedine, stok hareketlerinin toplamı stok bakiyesine, tahsilat toplamı cari alacak azalışına eşit olmalıdır; muhasebede ise her fişin borç ve alacak toplamı eşitlenmelidir. Bu testlerin ayırt edici gücü şudur: tek tek senaryolar yalnızca **düşündüğünüz** hataları arar, mutabakat ise **düşünmediğiniz** hataları yakalar — özellikle "kayıt sessizce kayboldu" ve "kayıt iki kez sayıldı" sınıfını pratikte başka hiçbir test görmez. Alan biçimi doğrulaması (A), tarayıcı karşılaştırması (B) ve ortam sürümü kontrolü (C) tümüyle başka test türleridir. Önemli bir ayrıntı: karşılaştırılan iki taraf gerçekten bağımsız olmalıdır; aynı sorgudan iki kez okumak mutabakat sayılmaz.

# erpakis-3 | İleri
## Soru
Bir üründen depoda son 1 adet kalmıştır ve iki müşteri neredeyse aynı anda o ürünü sipariş etmektedir. QA olarak bu durumu nasıl test eder ve hangi sonucu beklersiniz?
## A
İki isteği ortak bir bariyerle aynı anda salarım; tam olarak birinin başarılı diğerinin stok yetersiz hatasıyla reddedilmesini, stokun eksiye düşmemesini ve tek rezervasyon oluşmasını beklerim
🤖 Doğru: aşırı satış ancak istekler gerçekten eşzamanlı gönderildiğinde ortaya çıkar ve beklenen davranış tek başarı ile stokun negatife düşmemesidir.
## B
İki siparişi arka arkaya sırayla veririm; ilki geçerse ikincisi zaten reddedilir, bu yeterli bir eşzamanlılık testidir
🤖 sıralı istekler yarış durumunu hiç oluşturmaz, bu yüzden aşırı satış hatası bu yöntemle asla görünmez.
## C
Stokun kısa süreliğine eksiye düşmesi normaldir; gece çalışan düzeltme işi bakiyeyi tekrar sıfıra çektiği için bu bir hata sayılmaz
🤖 eksi stok müşteriye satılmış ama var olmayan mal demektir; gece düzeltmesi hatayı gizler, gerçekleşmiş satışı geri getirmez.
## D
Yük testi aracıyla yüksek istek sayısı üretip toplam istek/saniye değerini ve yanıt sürelerini ölçerim; sayılar iyiyse eşzamanlılık sorunu yoktur
🤖 performans metrikleri veri bütünlüğü hakkında bilgi vermez; hızlı bir sistem de iki kez satış yapabilir.
## Doğru
A
## Açıklama
Aşırı satış (overselling) bir yarış durumudur (race condition) ve yalnızca istekler **gerçekten aynı anda** işlendiğinde ortaya çıkar; bu yüzden istekleri sırayla göndermek (B) test etmemek demektir. Doğru kurgu, iki isteği ayrı iş parçacıklarında hazırlayıp ortak bir bariyerle (örneğin bir `CountDownLatch`) aynı anda salmak ve sonra iki şeyi birden doğrulamaktır: sonuç kodlarından tam olarak biri başarı, diğeri stok yetersizlik hatası olmalı; stok hiçbir koşulda eksiye düşmemeli ve tek bir rezervasyon oluşmalıdır. Stokun geçici olarak eksiye düşmesini normal saymak (C) ciddi bir hatadır — bu, var olmayan bir malın müşteriye satılması demektir ve gece çalışan bir düzeltme işi yalnızca izi siler, gerçekleşmiş satışı geri almaz. Yük testi metrikleri (D) tümüyle başka bir soruyu cevaplar; sistem hızlı olduğu hâlde bütünlüğü bozabilir. Altta yatan tasarım kararı iyimser/kötümser kilitleme ve atomik güncellemedir (`225`).

# erpakis-4 | İleri
## Soru
Site ile ERP arasındaki entegrasyonda ağ zaman aşımı yaşanıyor ve gönderim katmanı isteği yeniden deniyor; sonuçta aynı sipariş ERP'ye iki kez ulaşıyor, çift stok düşümü ve çift fatura oluşuyor. Bu sınıf hataya karşı doğru tasarım ve doğru test hangisidir?
## A
Yeniden deneme mekanizmasını tümüyle kapatmak; istek başarısız olursa hiç tekrar gönderilmemeli, hata olarak bırakılmalıdır
🤖 dağıtık entegrasyonda yeniden deneme vazgeçilmezdir; kapatmak çift kaydı değil, kaybolan siparişi getirir.
## B
Oluşan fazla kayıtları gece çalışan bir temizlik işiyle tespit edip silmek ve raporlardaki tutarları buna göre düzeltmek
🤖 fatura ve muhasebe kayıtları silinemez; üstelik bu yaklaşım nedeni değil belirtiyi ele alır.
## C
Gönderenin benzersiz bir iş anahtarı taşıması ve ERP'nin aynı anahtarı ikinci kez gördüğünde yeni belge yaratmaması; testi, aynı isteği iki kez gönderip tek kayıt oluştuğunu doğrulamaktır
🤖 Doğru: yeniden denemeyi ortadan kaldırmak yerine güvenli kılan çözüm idempotentliktir ve testi doğrudan aynı isteği tekrar göndermektir.
## D
İstemci tarafındaki zaman aşımı süresini yeterince uzatmak; süre uzayınca yeniden deneme hiç tetiklenmeyeceği için sorun kökten çözülür
🤖 zaman aşımını uzatmak yarış penceresini daraltır ama ortadan kaldırmaz ve ağ kopmalarında yine çift gönderim olur.
## Doğru
C
## Açıklama
Dağıtık bir entegrasyonda yeniden deneme kaçınılmazdır: istek karşı tarafa ulaşmış ama cevabı dönerken bağlantı kopmuş olabilir ve gönderen bunu ayırt edemez. Bu yüzden doğru yaklaşım yeniden denemeyi kaldırmak (A) veya süreyi uzatarak pencereyi daraltmak (D) değil, tekrarlı gönderimi **zararsız** kılmaktır — yani idempotentlik. Uygulaması nettir: gönderen her sipariş için benzersiz bir iş anahtarı taşır, ERP bu anahtarı daha önce gördüyse yeni belge üretmez, ilk işlemin sonucunu döner ve bu davranış veritabanı seviyesinde bir benzersizlik kısıtıyla garanti altına alınır. QA açısından en değerli kısmı testinin ne kadar basit olmasıdır: birebir aynı isteği iki kez gönderin ve tek sipariş, tek stok hareketi, tek fatura oluştuğunu doğrulayın — bu test şaşırtıcı sıklıkta gerçek hata bulur. Fazla kayıtları sonradan silmek (B) hem ERP'de mümkün değildir (fatura ve muhasebe kaydı silinmez) hem de nedeni değil belirtiyi ele alır.

# erpakis-5 | İleri
## Soru
Müşteri, belge seviyesinde iskonto uygulanmış 3 kalemlik bir siparişin yalnız 1 kalemini iade ediyor. Kısmi iade senaryosunda doğrulanması gerekenler en doğru biçimde hangi seçenekte verilmiştir?
## A
Sipariş toplamının üçte birinin müşteriye iade edilmesi yeterlidir; kalem sayısı eşit olduğu için oransal hesap doğru sonucu verir
🤖 kalemlerin birim fiyatları ve KDV oranları farklı olabilir, ayrıca iskonto tutara göre dağılır; eşit paylaştırma yanlış tutar üretir.
## B
Kısmi iade yalnız stok tarafını ilgilendirir; ürün depoya geri girdikten sonra cari bakiye, KDV ve muhasebe kayıtları etkilenmez
🤖 iade satışın geri alınmasıdır; cari bakiye, KDV ve muhasebe kayıtları da geriye doğru düzeltilmek zorundadır.
## C
İade edilen tutarın KDV'si hiçbir koşulda geri alınmaz; yalnız KDV hariç tutar müşteriye ödenir, stok düzeltilir ve muhasebede ayrıca bir işlem gerekmez
🤖 iade faturasıyla KDV de düzeltilir; vergiyi sabit bırakmak hem müşteriye eksik ödeme hem hatalı beyan üretir.
## D
İade tutarı satır bazında hesaplanmalı, belge iskontosu iade edilen satıra orantılı dağıtılmalı, KDV bu tutar üzerinden geri alınmalı, ürün doğru depoya girmeli ve muhasebede karşılık kaydı oluşmalıdır
🤖 Doğru: kısmi iade dört defteri birden geriye doğru etkiler ve iskonto dağıtımı ile depo yönlendirmesi en sık atlanan iki noktadır.
## Doğru
D
## Açıklama
Kısmi iade, satışın kısmen geri alınmasıdır ve bu yüzden satışın dokunduğu her yeri geriye doğru etkiler. İade tutarı satır bazında hesaplanır; belge seviyesinde uygulanmış bir iskonto varsa iade edilen satıra **tutarına orantılı** olarak dağıtılmalıdır — kalem sayısına göre eşit bölmek (A) farklı fiyat ve KDV oranlarında yanlış sonuç verir. KDV, iade edilen net tutar üzerinden düzeltilir; vergiyi sabit bırakmak (C) hem müşteriye eksik ödeme hem hatalı beyan anlamına gelir. İadenin yalnız stok meselesi olduğunu düşünmek (B) ise en yaygın yanılgıdır: cari bakiye azalmalı, muhasebede karşılık kaydı oluşmalı, belge zinciri iade faturasıyla tamamlanmalıdır. Sık atlanan bir başka nokta ürünün **hangi depoya** döndüğüdür: sağlam ürün satılabilir stoğa, hasarlı ürün karantina/hasarlı deposuna girmelidir; ikisini ayırmayan bir sistem satılamaz malı satılabilir gösterir. Son olarak, orantılı dağıtımda çıkan kuruş farklarının toplam mutabakatını bozmadığı kontrol edilmelidir.

# erpakis-6 | Uzman
## Soru
Eski sistemden yeni yazılan ERP'ye veri göçü (migration) yapılacak: ana veri, açılış bakiyeleri ve açık belgeler taşınacak. Göçün doğrulanması için en sağlam test yaklaşımı hangisidir?
## A
Eski sistem kapatılıp yeni sisteme geçilir; kullanıcılar günlük işlerinde bir tutarsızlık fark ederse bildirir ve düzeltmeler canlıda yapılır
🤖 göç hatası canlıda fark edildiğinde geri dönüş çok pahalıdır ve hatalı açılış bakiyeleri yıllarca sürüklenir.
## B
Dört katmanlı doğrulama yapılır: kayıt adetleri, kontrol toplamları, riskli kayıtları da içeren örneklem karşılaştırması ve açık belgelerin durumu; ayrıca göç betiği tekrarında veriyi çiftlememelidir
🤖 Doğru: sayı, tutar, örneklem ve durum katmanları birbirinin körlüğünü kapatır; tekrar çalıştırılabilirlik ise yarıda kalan göçün kurtarılabilmesini sağlar.
## C
Kayıt adetleri iki sistemde eşitse veri doğru aktarılmış demektir; tutar ve alan bazlı karşılaştırma gereksiz bir maliyettir
🤖 doğru sayıda ama yanlış içerikli kayıt taşımak fazlasıyla mümkündür; adet eşitliği yalnız kayıp olmadığını gösterir.
## D
Göç bir kerelik bir iş olduğu için tekrar çalıştırılamaz; bu nedenle doğrulama ancak üretime geçildikten sonra, gerçek kullanım üzerinden yapılabilir
🤖 göç prova ortamlarında defalarca koşulmalı ve tekrar çalıştırılabilir tasarlanmalıdır; aksi hâlde yarıda kalan bir göç kurtarılamaz.
## Doğru
B
## Açıklama
Veri göçü, en pahalı sonuçları olan ve zaman baskısı altında en çok kısılan test kalemidir; çünkü hatalı taşınan bir açılış bakiyesi yıllarca sürüklenir ve canlıda düzeltilmesi son derece pahalıdır (A ve D bu yüzden yanlıştır — göç prova ortamlarında defalarca koşulmalı, doğrulaması üretime geçmeden bitmelidir). Sağlam doğrulama dört katmanlıdır ve katmanlar birbirinin körlüğünü kapatır: **adet** kaybı yakalar, **kontrol toplamları** (toplam cari bakiye, toplam stok miktarı ve değeri, açık fatura toplamı) tutar bozulmasını yakalar, **örneklem** karşılaştırması alan eşlemesi hatalarını yakalar — örneklemin yalnız rastgele değil **riskli** kayıtları da içermesi gerekir (en büyük bakiyeli cari, en çok hareketli ürün, dövizli açık fatura, eksi bakiye) — ve **durum** kontrolü açık siparişlerin açık, kısmi teslim edilmişlerin doğru kalan miktarla geldiğini doğrular. Yalnız adet karşılaştırmasıyla yetinmek (C) yaygın bir yanılgıdır: doğru sayıda yanlış kayıt taşımak fazlasıyla mümkündür. Son olarak göç betiği idempotent olmalı, yarıda kalıp yeniden başlatıldığında veriyi çiftlememelidir.
