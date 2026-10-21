---
slug: kredi-sistemi-uctan-uca-mimari
baslik: "Kredi Sisteminin Uçtan Uca Mimarisi: Servis Sınırları, Tutarlılık, Batch ve Dağıtım"
kademe: Uzman
sira: 241
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir inşaat şantiyesi düşün. Duvarcı, elektrikçi, tesisatçı ve boyacı ayrı ekiplerdir; her biri kendi işini bilir. Ama binayı ayakta tutan şey, bu ekiplerin tek tek ustalığı değildir. Asıl mesele iki sorudur: kim ne zaman devreye girecek ve bir ekip geç kalırsa ne olacak?

Yazılım mülakatının son yarım saatinde sorulan "bir kredi sistemi tasarla" sorusu tam olarak bunu ölçer. Spring Boot'u, Kafka'yı ve Kubernetes'i tek tek bildiğin zaten varsayılır. Senden istenen, bunları bankacılık gerçeğinde **bir arada** kurgulamandır. Bu konu yeni bir teknoloji anlatmaz. Mevcut konuların bilgisini kredi senaryosunda birleştirir ve hangi kararın neden verildiğini gösterir.

### Servis ayrıştırma: sınırlar nereden geçer?

Kredi işi doğal olarak şu yeteneklere ayrılır: **başvuru**, **skorlama/karar**, **limit/tahsis**, **sözleşme**, **kullandırım**, **taksit-tahakkuk**, **tahsilat**, **bildirim**, **raporlama**. Bu ayrım **iş yeteneği (business capability)** bazlıdır. Yani her servis bir iş sorusunun tam sahibidir. Kendi verisini tutar, kendi kurallarını uygular ve bir değişiklik çoğunlukla tek servisi ilgilendirir.

Yanlış ayrım **katman bazlı** ayrımdır: "veri erişim servisi", "iş kuralları servisi", "web servisi" diye bölmek. Bu ayrımda tek bir iş değişikliği, örneğin yeni bir kredi türü, üç servisi birden değiştirmeyi gerektirir. Üçü birlikte dağıtılmak zorunda kalır. Böylece mikroservisin bağımsız dağıtım kazancı sıfırlanır, üstüne ağ gecikmesi bedava gelir.

Aynı şekilde "her tabloya bir servis" veya "her ekrana bir servis" de yanlıştır. İlki veriyi böler ama işlemi bölmez. İkincisi arayüz değişince mimariyi değiştirir. Ayrıştırmanın alan-güdümlü tasarım (DDD) tarafı `97`, karşılaştırması `95` konusundadır.

Önemli bir dürüstlük notu: **modüler monolitle başlamak savunulabilir bir karardır** (`96`). Sınırlar, iş henüz iyi anlaşılmadan çizilirse yanlış çizilir. Yanlış sınır ise dağıtık sistemde düzeltilmesi en pahalı hatadır. Mülakatta "her şeyi mikroservis yaparım" demek olgun bir cevap değildir. Olgun cevap şudur: "Sınırları önce modül olarak çizerim; iş olgunlaşınca ve bağımsız ölçekleme/dağıtım ihtiyacı doğunca ayırırım."

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kredi sisteminin servis haritası ve legacy core banking entegrasyonu">
  <defs>
    <marker id="kredimim-map-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kredi sistemi: iş yeteneği bazlı servis haritası</text>
  <rect x="180" y="34" width="300" height="32" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="330" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Kredi ekranları / kanal → API gateway</text>
  <rect x="30" y="88" width="190" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="125" y="107" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Başvuru</text>
  <text x="125" y="122" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">durum makinesi, taslak</text>
  <rect x="235" y="88" width="190" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="330" y="107" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Skorlama / Karar</text>
  <text x="330" y="122" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">dış sorgular, yavaş</text>
  <rect x="440" y="88" width="190" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="535" y="107" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Limit / Tahsis</text>
  <text x="535" y="122" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sıcak kayıt, kilit</text>
  <rect x="30" y="142" width="190" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="125" y="161" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sözleşme</text>
  <text x="125" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">metin sürümü, onay</text>
  <rect x="235" y="142" width="190" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="161" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kullandırım</text>
  <text x="330" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">para hareketi, idempotent</text>
  <rect x="440" y="142" width="190" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="535" y="161" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Taksit / Tahakkuk</text>
  <text x="535" y="176" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gün sonu toplu iş</text>
  <rect x="30" y="196" width="190" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="125" y="215" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Tahsilat</text>
  <text x="125" y="230" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">gecikme, takip</text>
  <rect x="235" y="196" width="190" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="215" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Bildirim</text>
  <text x="330" y="230" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">olayla tetiklenir</text>
  <rect x="440" y="196" width="190" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="535" y="215" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Raporlama</text>
  <text x="535" y="230" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">okuma kopyası</text>
  <rect x="150" y="262" width="360" height="42" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.6" stroke-dasharray="5 3"/>
  <text x="330" y="281" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Core banking (eski sistem): hesap + muhasebe</text>
  <text x="330" y="296" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">SOAP / mesaj kuyruğu · kesinti ve zaman aşımı beklenir</text>
  <path d="M330 66 L330 86" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredimim-map-arw)"/>
  <path d="M330 186 L330 260" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredimim-map-arw)"/>
  <text x="330" y="328" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">mavi: senkron istek yolu · sarı: toplu iş · yeşil: olayla beslenen asenkron tüketici</text>
</svg>
```

### Senkron / asenkron sınırı

Mimarinin en belirleyici kararı, hangi adımın kullanıcı beklerken, hangisinin arkada yürüyeceğidir.

- **Başvuru → skorlama.** Skorlama kendi başına hızlı olabilir. Ama dış sorgular süreyi öngörülemez yapar: kredi kayıt kuruluşu sorgusu, kimlik/adres doğrulama, banka içi geçmiş. Kontrol edemediğin bir gecikmeyi kullanıcının isteğine bağlamak kırılgandır. Bir servis yavaşladığında başvuru servisinin iş parçacıkları dolar ve arıza yayılır. Doğru kurgu üç adımlıdır: isteği **kabul edip bir kimlik dön**, kararı arkada yürüt, ekrana durum sorgulat. Kullanıcı beklerken "başvurunuz değerlendiriliyor" görür; karar hazır olduğunda bildirim gider. Senkron kalması savunulabilecek tek durum, kararın gerçekten kısa olduğu ve tek bir iç servise dayandığı hâldir. O zaman bile zaman aşımı, sınırlı **yeniden deneme (retry)** ve devre kesici zorunludur (`101`).
- **Bildirim, muhasebe yansıması, raporlama** her koşulda asenkrondur. Kullanıcının kullandırımı, kısa mesaj sağlayıcısı yavaş diye gecikmemelidir.
- **Limit ayrımı ve kullandırım** ise gecikmeye toleransı en düşük, doğruluk gereksinimi en yüksek adımlardır. Bunlar kısa tutulur, kilitlenir ve tekrar edilse de sonucu değişmeyecek (idempotent) biçimde yazılır.

Kural olarak: **karar ve para** senkron ve dar; **bilgilendirme ve türetilmiş veri** asenkron ve geniş.

### Tutarlılık: dağıtık işlem olmadan doğru sonuç

Kredi onayından sonra zincir şudur: **limit ayrımı → kullandırım → muhasebe kaydı**. Bu adımlar farklı servislerde, kısmen de eski sistemde durur. Bu yüzden hepsini tek bir veritabanı işlemine almak mümkün değildir.

Klasik cevap, iki aşamalı kesinleştirme (2PC/XA) protokolüdür. Ama pratikte tercih edilmez ve dört sebebi vardır. Koordinatör tek arıza noktasıdır. Kaynaklar karar bekleyene kadar **kilitli kalır**; bankada sıcak limit kayıtları üzerinde bu felakettir. Eski sistemler ve mesajlaşma altyapıları çoğu zaman bu protokole katılamaz (`221`). Kilit süreleri arttıkça verim düşer.

Kullanılan desen **saga**'dır (`126`). Saga, adım adım telafili bir işlem zinciridir. Zincir, her biri kendi veritabanı işlemine sahip yerel adımlara bölünür. Bir adım başarısız olursa önceki adımlar **telafi (compensation)** işlemleriyle geri alınır.

Bankacılıkta telafi şu anlama gelir: yapılmış kayıt **silinmez**, karşısına **ters kayıt** atılır. Ayrılmış limit iade edilir, muhasebeye düzeltici fiş girilir ve denetim izinde her iki hareket de görünür. "Geri aldık, izi de sildik" cevabı mülakatta eleyicidir.

Sagayı ayakta tutan iki mekanizma vardır (`130`):

- **Giden kutusu tablosu (outbox):** İş verisi ile yayınlanacak olay **aynı yerel işlemde** yazılır. Olay, sonra ayrı bir süreçle mesajlaşma altyapısına aktarılır. Böylece "veritabanına yazdım ama olayı yayınlayamadım" durumu ortadan kalkar. Tersi de geçerlidir: "olayı yayınladım ama veritabanına yazamadım" da olmaz.
- **Tekrara dayanıklı tüketici (idempotent consumer):** Mesajlaşma altyapıları pratikte **en az bir kez (at-least-once)** teslim eder; yani aynı olay iki kez gelebilir. Para hareketinde bu, çift kayıt demektir. Çözüm şudur: her iş adımına **benzersiz iş anahtarı** verilir ve veritabanında benzersizlik kısıtıyla korunur. Anahtar örneği: başvuru kimliği + adım türü. İkinci teslim yazamaz ve "zaten işlenmiş" olarak kabul edilir.

```svg
<svg viewBox="0 0 660 252" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kredi zincirinde saga adımları ve telafi olarak ters kayıt">
  <defs>
    <marker id="kredimim-saga-fw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
    <marker id="kredimim-saga-bw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Saga: ileri adımlar ve telafi (ters kayıt)</text>
  <rect x="26" y="44" width="130" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="91" y="63" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kredi onayı</text>
  <text x="91" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">başvuru servisi</text>
  <rect x="190" y="44" width="130" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="255" y="63" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Limit ayrımı</text>
  <text x="255" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">limit servisi</text>
  <rect x="354" y="44" width="130" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="419" y="63" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kullandırım</text>
  <text x="419" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">para hareketi</text>
  <rect x="518" y="44" width="130" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="583" y="63" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Muhasebe</text>
  <text x="583" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">eski sistem</text>
  <path d="M156 66 L188 66" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#kredimim-saga-fw)"/>
  <path d="M320 66 L352 66" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#kredimim-saga-fw)"/>
  <path d="M484 66 L516 66" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#kredimim-saga-fw)"/>
  <rect x="26" y="150" width="130" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="91" y="169" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Başvuru iptali</text>
  <text x="91" y="184" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">durum geri alınır</text>
  <rect x="190" y="150" width="130" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="255" y="169" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Limit iadesi</text>
  <text x="255" y="184" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrılan tutar geri</text>
  <rect x="354" y="150" width="130" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="419" y="169" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Ters kayıt</text>
  <text x="419" y="184" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">silme yok, düzeltme</text>
  <rect x="518" y="150" width="130" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="583" y="169" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Düzeltici fiş</text>
  <text x="583" y="184" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">mutabakata girer</text>
  <path d="M516 172 L486 172" fill="none" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#kredimim-saga-bw)"/>
  <path d="M352 172 L322 172" fill="none" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#kredimim-saga-bw)"/>
  <path d="M188 172 L158 172" fill="none" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#kredimim-saga-bw)"/>
  <path d="M419 88 L419 148" fill="none" stroke="#B23B3B" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#kredimim-saga-bw)"/>
  <text x="435" y="120" text-anchor="start" font-size="10.5" fill="#B23B3B" font-family="system-ui">bir adım başarısız → telafi zinciri geriye işler</text>
  <text x="330" y="222" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Her adım kendi yerel işlemi + outbox ile olay; tüketici benzersiz iş anahtarıyla idempotent</text>
  <text x="330" y="240" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">2PC/XA tercih edilmez: koordinatör tek arıza noktası, sıcak kayıtlar uzun süre kilitli kalır</text>
</svg>
```

### Core banking / eski sistem entegrasyonu

Türkiye'de bankacılıkta hesap ve muhasebe tarafı çoğunlukla bir çekirdek sistemde durur. Bu sistem uzun yıllardır çalışır; güvenilirliği yüksek, esnekliği düşüktür. Yeni kredi servisleri bu çekirdekle genelde SOAP tabanlı servisler veya mesaj kuyruğu üzerinden konuşur (`221`). Mimari sonuçları şunlardır:

- **Yavaşlık ve kesinti normaldir.** Çekirdek sistemin bakım pencereleri ve gün sonu kapanışı olur. Kredi servisleri bu süreleri "arıza" değil **beklenen durum** olarak modellemelidir. Pratikte bunun karşılığı şudur: istek kuyruğa alınır, sonra işlenir.
- **Yalıtıcı bir uyarlayıcı (adapter) katmanı konur.** Çekirdeğin veri modeli ve alan adları, uygulamanın iç modeline sızmamalıdır. Sızarsa çekirdek değiştiğinde tüm sistem değişir.
- **Boğan incir deseni (strangler fig):** Eski sistemin işlevi bir gecede devralınmaz. Yeni sistem araya bir yönlendirme katmanı koyar ve işlevleri parça parça devralır. Devralınan parçalar yeni servise, devralınmayanlar eskiye gider. Bir gün eski sistem kimse fark etmeden devre dışı kalır. "Hepsini yeniden yazarız" cevabı bankada gerçekçi değildir.

### Gün sonu ve toplu işler

Kredi sisteminin gerçek yükü çoğu zaman gündüzde değil, gün sonundadır. Gün sonu penceresindeki tipik işler şunlardır: **tahakkuk** (o güne düşen faiz/gelir kayıtlarının üretilmesi), **gecikme hesaplama**, **ekstre üretimi** ve **mutabakat dosyalarının** hazırlanıp karşı taraflarla eşleştirilmesi. Bu işler Spring Batch ile kurgulanır (`222`, `223`). Mimari açıdan üç nokta kritiktir:

- **Pencere ve hizmet düzeyi taahhüdü (SLA).** İşin biteceği saat bellidir ve sonraki iş gününün açılışı buna bağlıdır. Bu yüzden toplu işler parçalara bölünüp (partition) paralel çalıştırılır ve süreleri ölçülür.
- **İkinci kez çalışma riski.** İş yarıda kalıp elle yeniden başlatılabilir. Zamanlayıcı da iki kopyayı birden tetikleyebilir. Her iki durumda **çift tahakkuk** oluşabilir. Korunma üç parçalıdır. Birincisi, iş **iş günü + iş türü** gibi bir anahtarla tekilleştirilir. İkincisi, üretilen kayıtlarda benzersizlik kısıtı bulunur. Üçüncüsü, iş yeniden başlatılabilir (restartable) olur: işlenmiş kayıtları tekrar işlemeden kaldığı yerden devam edebilmelidir.
- **Gündüz yüküyle çakışma.** Toplu iş aynı sıcak tablolara yazarsa çevrimiçi işlemler kilit bekler. Bu yüzden okuma tarafı ayrılır ve yazma pencereleri ayrıştırılır.

```svg
<svg viewBox="0 0 660 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gün sonu toplu iş penceresi ve idempotent tekrar">
  <defs>
    <marker id="kredimim-eod-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Gün sonu penceresi: sıra bellidir, bitiş saati taahhüttür</text>
  <line x1="34" y1="150" x2="632" y2="150" stroke="#5D7789" stroke-width="1.4" marker-end="url(#kredimim-eod-arw)"/>
  <rect x="46" y="60" width="136" height="52" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="114" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Tahakkuk</text>
  <text x="114" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">güne düşen kayıtlar</text>
  <rect x="196" y="60" width="136" height="52" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="264" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gecikme hesabı</text>
  <text x="264" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ödenmeyen taksitler</text>
  <rect x="346" y="60" width="136" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="414" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Ekstre üretimi</text>
  <text x="414" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bölümlenip paralel</text>
  <rect x="496" y="60" width="136" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="564" y="82" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Mutabakat</text>
  <text x="564" y="99" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">dosya eşleştirme</text>
  <line x1="40" y1="128" x2="40" y2="172" stroke="#25804C" stroke-width="1.8"/>
  <text x="40" y="190" text-anchor="start" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">gün sonu başlangıcı</text>
  <line x1="626" y1="128" x2="626" y2="172" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="620" y="190" text-anchor="end" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">SLA: ertesi gün açılışı</text>
  <text x="330" y="208" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Her adım iş günü + iş türü anahtarıyla tekil; yeniden başlatma çift tahakkuk üretmemeli</text>
</svg>
```

### Veri katmanı

Bankada ilişkisel veritabanı, çoğunlukla Oracle, hâlâ gerçeğin merkezidir (`227`). Mimari açıdan dört başlık öne çıkar:

- **Sıcak kayıt ve kilit çekişmesi.** Müşteri limiti, ürün limiti veya gün içi toplam gibi tek satırlar darboğaz üretir; çünkü güncellemeler bu satırlarda yoğunlaşır. Çözüm yönleri şunlardır: işlemi kısa tut, kilit sırasını tüm kodda aynı yap (**karşılıklı kilitlenme / deadlock** önlemi) ve gerekiyorsa toplamı parçalara böl. İzolasyon ve anomaliler `224`, kilit stratejisi `225` konusundadır.
- **Arşivleme ve bölümleme (partitioning).** Taksit, hareket ve denetim izi tabloları hızla büyür. Tarihe göre bölümleme iki fayda verir: eski bölümler arşive alınabilir ve sorgular yalnız ilgili bölüme bakar.
- **Raporlama yükünün ayrılması.** Ay sonu raporu, çevrimiçi kullandırımla aynı veritabanını yormamalıdır. İlk adım bir **okuma kopyasıdır (read replica)**. Daha ileri adım, okuma modelini ayrı tutmaktır; buna CQRS denir (`127`). Okuma modeli **nihai tutarlıdır (eventually consistent)**: rapor birkaç saniye geriden gelebilir. Bu kabul edilebilir. Ama kullandırım kararı asla okuma modelinden verilmez.
- **Para alanları.** Tutarlar ondalık duyarlı türle tutulur ve para birimi ayrı alandır (`226`).

### Dağıtım ve işletme

- **Kesintisiz dağıtım.** Yuvarlanan güncellemede yeni pod hazır olmadan eskisi kapatılmaz (`218`). Ama asıl soru şudur: **para hareketinin ortasında pod kapanırsa ne olur?** Cevap üç parçalıdır. (1) **Zarif kapanış**: pod trafikten çıkarılır, süregelen istekler bitirilir ve buna yeterli süre tanınır. (2) **Giden kutusu tablosu (outbox)**: iş verisi ile olay aynı yerel işlemde yazıldığı için, yarım kalan yayın kapanıştan sonra devam eder. (3) **Tekrara dayanıklı yeniden deneme**: kapanış anında sonucu bilinmeyen adım tekrar denendiğinde, benzersiz iş anahtarı ikinci kaydı engeller. Bu üçü olmadan "kesintisiz dağıtım" yalnız HTTP seviyesinde kesintisizdir, para seviyesinde değildir.
- **Ortam ayrımı ve değişiklik yönetimi.** Geliştirme, test, ön üretim ve üretim ortamları ayrıdır. Aynı imaj ortamdan ortama taşınır; farkı yalnız yapılandırma yapar. Bankada üretime çıkış, onay ve izlenebilirlik gerektirir: kim neyi ne zaman çıkardı, kayıt altındadır (`220`). **Üretim verisine geliştiricinin doğrudan erişimi olmaz.** Test verisi maskelenmiş ya da üretilmiş veridir.
- **Gözlemlenebilirlik.** Teknik metrikler sistemin sağlığını gösterir: gecikme, hata oranı, işlemci/bellek, kuyruk gecikmesi. **İş metrikleri** ise işin sağlığını gösterir: dakikadaki başvuru, onay oranı, kullandırım tutarı, başarısız ödeme sayısı. Aradaki fark önemlidir. Bir hata yayımından sonra tüm teknik metrikler yeşil olabilir ama onay oranı sıfıra düşebilir. Bunu yalnız iş metriği yakalar. Uyarı eşikleri de buna göre kurulur: teknik metrikte mutlak eşik, iş metriğinde **beklenen desenden sapma** (gün/saat bazlı normale göre).
- **İzlenebilirlik.** Bir başvurunun servisler arasındaki yolculuğu, tek bir **izleme kimliğiyle (trace id)** takip edilebilmelidir. Kayıtlarda bu kimlik bulunur, hassas veri bulunmaz.

### Güvenlik ve regülasyon

Yetkilendirme her serviste ve her istekte yapılır. İç servisler arası çağrılar da kimliklendirilir; "ağ içi, güvenli" varsayımı yanlıştır. Sırlar imaja veya depoya konmaz, dışarıdan verilir. Sır derken veritabanı parolasını ve servis anahtarını kastediyoruz.

Denetlenebilirlik zorunludur. Kredi kararının hangi girdilerle ve hangi kural sürümüyle verildiği sonradan gösterilebilmelidir. Veri saklama sürelerinin mevzuatça belirlendiği bilinir. Ama **süreyi ezberden söyleme**. Doğru ve güvenli cevap şudur: "Saklama süresi mevzuata göre belirlenir ve arşivleme politikası buna göre kurulur; kişisel veri KVKK kapsamındadır." Güvenlik mimarisi `189`, güvenli kodlama ve şifreleme `190` konusundadır.

### "Bir kredi sistemi tasarla" sorusuna cevap iskeleti

Mülakatta bu soru geldiğinde hemen kutu çizmeye başlamak en sık hatadır. Genel sistem tasarımı iskeleti `141`'dedir; aşağıda bankaya uyarlanmış hâli var:

1. **Gereksinimi netleştir.** Hangi kredi ürünü? Kim başvuruyor (bireysel/ticari)? Hacim ne? Kararın süresi kabul edilebilir mi? Eski çekirdek sistem var mı?
2. **Aktörleri ve akışları çıkar.** Müşteri, şube/çağrı merkezi personeli, karar sistemi, muhasebe, denetim.
3. **Servis sınırlarını çiz.** İş yeteneği bazlı; hangi sınırın bugün modül, hangisinin ayrı servis olacağını gerekçelendir.
4. **Tutarlılık stratejisini seç.** Nerede güçlü tutarlılık (limit, para), nerede nihai tutarlılık (rapor, bildirim); saga ve telafi adımlarını adlandır.
5. **Veri modelini ve sıcak noktaları göster.** Hangi tablo sıcak, kilit nerede, büyüme nerede, raporlama nasıl ayrılıyor.
6. **Hata ve telafi senaryolarını anlat.** Çekirdek sistem kapalıysa, olay iki kez gelirse, pod kapanırsa, toplu iş iki kez çalışırsa ne olur.
7. **Ölçek ve işletme.** Gün sonu penceresi, ölçekleme, dağıtım, iş metrikleri ve uyarılar.

Bu iskeleti takip eden aday, teknoloji adı sayan adaydan ayrışır. Çünkü sorulan şey "hangi araç" değildir; "hangi kararı neden verdin"dir.

### Mülakatta ne sorulur / tuzaklar

- **"Servisleri nasıl bölerdin?"** İş yeteneği bazlı böl. Katman bazlı bölmenin neden yanlış olduğunu söyle. Modüler monolitle başlamanın savunulabilir olduğunu da ekle.
- **"Kredi onayı, limit ve muhasebe tek işlemde olmalı, nasıl yaparsın?"** Beklenen cevap şudur: saga + giden kutusu tablosu + tekrara dayanıklı tüketici. 2PC/XA'yı neden seçmediğini de gerekçelendir: koordinatör tek arıza noktasıdır, kilitler uzun sürer, eski sistemler protokole katılamaz.
- **"Telafi ne demek?"** Bankada ters kayıt demektir. Silme değil düzeltme yapılır; iz korunur.
- **"Olay iki kez gelirse?"** En az bir kez teslim gerçeğini kabul et. Benzersiz iş anahtarı ve veritabanı kısıtıyla tekilleştir.
- **"Gün sonu işi yarıda kaldı, ne yaparsın?"** Yeniden başlatılabilirlik, iş günü anahtarıyla tekilleştirme ve çift tahakkuk riskine karşı benzersizlik kısıtı. Pencere ile SLA'yı da hatırlat.
- **"Kullandırım sırasında pod kapanırsa?"** Zarif kapanış + giden kutusu tablosu + tekrara dayanıklı yeniden deneme. "Kubernetes hallediyor" cevabı yetersizdir.
- **"Ne izlerdin?"** Teknik metriklerin yanında iş metriklerini de söyle. İkisinin farkını ve iş metriğinin sapma temelli uyarısını açıkla.
- **Sık yapılan hata:** kutuları çizip teknoloji adı saymak. Mimari kutular değildir; **kararlar ve gerekçelerdir**. Her seçimin bedelini de söyleyebilmelisin.

### Tek cümlede

Kredi sistemi, iş yeteneği bazlı servislere bölünür. Karar ve para adımları dar ve senkron tutulur; bilgilendirme ile türetilmiş veri asenkron tutulur. Tutarlılık dağıtık işlemle değil, **saga + giden kutusu tablosu + tekrara dayanıklı tüketici** ile sağlanır; telafi bankada ters kayıt demektir. Eski çekirdek sistemle uyarlayıcı üzerinden konuşulur ve boğan incir deseniyle kademeli devralınır. Gün sonu toplu işleri pencere/SLA ve yeniden başlatılabilirlikle kurgulanır. Dağıtımda zarif kapanış ve tekrara dayanıklı yeniden deneme para hareketini korur. İşletmede ise teknik metriklerin yanında **iş metrikleri** izlenir.

# kredimim-1 | İleri
## Soru
Yeni kredi platformunda servis sınırlarını çizmen isteniyor. Aşağıdaki yaklaşımlardan hangisi doğrudur ve neden?
## A
Katman bazlı bölmek doğrudur: veri erişim servisi, iş kuralları servisi ve web servisi ayrılır; böylece her katman kendi uzmanı tarafından bağımsız geliştirilir
🤖 tek bir iş değişikliği üç servisi birden değiştirmeyi ve birlikte dağıtmayı gerektirir; bağımsız dağıtım kazancı yok olur.
## B
İş yeteneği bazlı bölmek doğrudur: başvuru, skorlama, limit, sözleşme, kullandırım, tahakkuk gibi servisler kendi verisinin sahibidir; sınırlar belirsizken modüler monolitle başlamak da savunulabilir
🤖 Doğru: değişiklik çoğunlukla tek servisi ilgilendirir ve yanlış sınır riski modüler başlangıçla azaltılır.
## C
Her veritabanı tablosuna bir servis düşecek biçimde bölmek doğrudur; böylece veri sahipliği en ince ayrımda netleşir ve hiçbir servis başkasının tablosuna dokunmaz
🤖 bu veriyi böler ama işlemi bölmez; tek bir iş akışı çok sayıda servise dağılır ve her adım ağ çağrısına dönüşür.
## D
Her ekrana bir servis düşecek biçimde bölmek doğrudur; ekran ile servis birebir eşleşince ön yüz ekibi hangi servisi çağıracağını hiç düşünmez
🤖 mimariyi arayüze bağlar; ekran tasarımı değiştiğinde servis sınırlarının yeniden çizilmesi gerekir.
## Doğru
B
## Açıklama
Servis sınırının doğru ölçüsü **iş yeteneğidir**: başvuru, skorlama/karar, limit/tahsis, sözleşme, kullandırım, taksit-tahakkuk, tahsilat, bildirim, raporlama. Her servis bir iş sorusunun tam sahibidir, kendi verisini tutar ve bir iş değişikliği çoğunlukla tek servisi ilgilendirir — mikroservisin asıl kazancı olan **bağımsız dağıtım** ancak böyle elde edilir. **Katman bazlı** bölme (veri/iş kuralı/web) bunun tam tersini üretir: yeni bir kredi türü eklemek üç servisi birden değiştirip birlikte dağıtmayı gerektirir; üstelik katmanlar arası her çağrı artık ağ üzerinden gider, yani bedeli ödenir ama faydası alınmaz. **Tablo başına servis** ayrımı veriyi böler ama iş akışını bölmez; tek bir kullandırım işlemi onlarca servise dağılır, tutarlılık maliyeti fırlar. **Ekran başına servis** ise mimariyi arayüze bağlar; arayüz en sık değişen katmandır, mimari en seyrek değişmesi gerekendir. Son olarak olgun bir cevap **modüler monolit** seçeneğini de içerir (`96`): sınırlar iş yeterince anlaşılmadan çizilirse yanlış çizilir ve yanlış sınırı dağıtık sistemde düzeltmek en pahalı iştir; sınırları önce modül olarak kurup bağımsız ölçekleme veya dağıtım ihtiyacı doğduğunda ayırmak savunulabilir bir stratejidir.

# kredimim-2 | İleri
## Soru
Başvuru servisinden çağrılan skorlama/karar servisi, dış kaynaklardan yaptığı sorgular nedeniyle bazen uzun sürüyor ve zaman zaman yanıt vermiyor. Bu sınır nasıl tasarlanmalıdır?
## A
Çağrı senkron kalmalı ve başvuru servisindeki zaman aşımı süresi, skorlamanın en kötü durumunu karşılayacak kadar uzun tutulmalıdır
🤖 uzun zaman aşımı yavaşlığı gizlemez, yayar; başvuru servisinin iş parçacıkları dolar ve arıza tüm akışa taşar.
## B
Çağrı senkron kalmalı, ancak başarısızlıkta sınırsız yeniden deneme yapılmalı; sonuç alınana kadar denemek en yüksek başarı oranını verir
🤖 sınırsız yeniden deneme yavaşlayan servisin üzerine yük bindirir ve arızayı derinleştirir.
## C
Skorlama çağrısı tümüyle kaldırılıp karar gün sonu toplu işine devredilmeli; böylece çevrimiçi akışta hiçbir dış bağımlılık kalmaz
🤖 kararın ertesi güne kalması ürün gereksinimini bozar; sorun senkronluk değil, yönetilmeyen bağımlılıktır.
## D
Başvuru kabul edilip kimliği hemen dönmeli, karar arkada yürütülüp durum sorgulamayla izlenmeli; skorlama çağrısında zaman aşımı, sınırlı yeniden deneme ve devre kesici bulunmalıdır
🤖 Doğru: öngörülemez süreli dış bağımlılık kullanıcı isteğinden ayrılır ve arızanın yayılması sınırlanır.
## Doğru
D
## Açıklama
Mimarinin en belirleyici kararlarından biri, hangi adımın **kullanıcı beklerken** yürüyeceğidir. Skorlama kendi başına hızlı olsa bile dış sorgular (kredi kayıt kuruluşu, kimlik/adres doğrulama) süreyi öngörülemez yapar; **sahip olmadığın bir gecikmeyi kullanıcının isteğine bağlamak** kırılganlıktır. Doğru kurgu, isteği kabul edip başvuru kimliğini dönmek, kararı arkada yürütmek ve ekrana **durum sorgulatmaktır**; karar hazır olunca bildirim gider. Zaman aşımını uzatmak yavaşlığı çözmez, **yayar**: başvuru servisinin iş parçacıkları bekleyen çağrılarla dolar ve bir bağımlılığın yavaşlığı tüm akışı durdurur. Sınırsız yeniden deneme daha da kötüdür; zaten zorlanan servisin üzerine yük bindirir ve arızayı büyütür — bu yüzden yeniden deneme sayısı sınırlı, aralıklar artan ve rastgeleleştirilmiş olur, üstüne **devre kesici (circuit breaker)** konur (`101`). Kararı tümüyle gün sonu toplu işine devretmek ise teknik bir çözüm değil, ürünü bozan bir geri adımdır: sorun senkronluğun kendisi değil, bağımlılığın yönetilmemesidir. Genel kural: **karar ve para** dar ve kontrollü; **bilgilendirme ve türetilmiş veri** asenkron.

# kredimim-3 | Uzman
## Soru
Kredi onayından sonra limit ayrımı, kullandırım ve muhasebe kaydı farklı servislerde ve kısmen eski çekirdek sistemde gerçekleşiyor. Bu zincirin tutarlılığı nasıl sağlanmalıdır?
## A
Saga ile yerel işlemlere bölünmeli, başarısızlıkta telafi adımları ters kayıt olarak işletilmeli; olaylar outbox ile yayınlanıp tüketiciler benzersiz iş anahtarıyla idempotent yapılmalıdır
🤖 Doğru: uzun kilit tutmadan tutarlılık sağlanır ve en az bir kez teslimin çift kayıt riski anahtarla kapatılır.
## B
İki aşamalı kesinleştirme (2PC/XA) kullanılmalı; bankacılıkta para söz konusu olduğu için tek doğru çözüm dağıtık işlem koordinatörüdür
🤖 koordinatör tek arıza noktasıdır, sıcak kayıtlar karar bekleyene kadar kilitli kalır ve eski sistemler çoğunlukla protokole katılamaz.
## C
Tüm adımlar tek bir servise ve tek veritabanına toplanmalı; muhasebe kayıtları da bu veritabanına kopyalanarak dağıtık tutarlılık sorunu ortadan kaldırılmalıdır
🤖 muhasebenin sahibi çekirdek sistemdir; veriyi kopyalamak sahipliği taşımaz, yalnız iki gerçek kaynağı oluşturur.
## D
Her adım kendi işleminde yürütülmeli, hata olursa yapılmış kayıtlar silinerek başlangıç durumuna dönülmeli ve akış baştan denenmelidir
🤖 bankacılıkta finansal kayıt silinmez; düzeltme ters kayıtla yapılır ve denetim izi korunur.
## Doğru
A
## Açıklama
Zincir (limit ayrımı → kullandırım → muhasebe) farklı servislere ve kısmen eski sisteme yayıldığı için tek veritabanı işlemine alınamaz. **2PC/XA** akla gelen ilk cevaptır ama pratikte tercih edilmez: koordinatör tek arıza noktasıdır, katılımcı kaynaklar karar gelene kadar **kilitli** kalır — limit gibi sıcak kayıtlarda bu doğrudan verim çöküşüdür — ve eski çekirdek sistemler ile mesajlaşma altyapıları çoğu zaman protokole katılamaz (`221`). Kullanılan desen **saga**'dır (`126`): zincir, her biri kendi yerel işlemine sahip adımlara bölünür; bir adım başarısız olursa önceki adımlar **telafi (compensation)** ile geri alınır. Bankacılıkta telafi **silme değil ters kayıttır**: ayrılan limit iade edilir, muhasebeye düzeltici fiş girilir, her iki hareket de denetim izinde durur — bu yüzden "kayıtları silip baştan dene" şıkkı yanlıştır. Sagayı ayakta tutan iki mekanizma **outbox** (iş verisi ile olay aynı yerel işlemde yazılır, sonra yayınlanır) ve **idempotent tüketicidir** (`130`): mesajlaşma altyapıları pratikte **en az bir kez** teslim eder, aynı olay iki kez gelebilir; para hareketinde bunun karşılığı çift kayıttır ve **benzersiz iş anahtarı** üzerindeki veritabanı kısıtıyla engellenir. Her şeyi tek veritabanına toplama önerisi ise sahiplik gerçeğini görmez: muhasebenin sahibi çekirdek sistemdir, veriyi kopyalamak sahipliği taşımaz, yalnız iki ayrı "gerçek kaynağı" yaratıp mutabakatı imkânsızlaştırır.

# kredimim-4 | Orta
## Soru
Gün sonu tahakkuk işi bir gece yarıda kesiliyor ve elle yeniden başlatılıyor. Ertesi gün bazı kredilerde çift tahakkuk kaydı görülüyor. Bu sınıf hataya karşı doğru tasarım nedir?
## A
İş yalnız iş saatleri dışında ve tek bir sunucuda çalıştırılmalı; ikinci bir kopya hiç ayağa kalkmazsa çift kayıt da oluşmaz
🤖 tekrar yalnız ikinci kopyadan doğmaz; aynı sunucuda elle yeniden başlatma da aynı sonucu üretir.
## B
Yarıda kalan iş hiç yeniden başlatılmamalı, eksik kayıtlar ertesi gün elle girilmelidir; otomatik tekrar her zaman çift kayıt riski taşır
🤖 elle giriş hem SLA'yı kaçırır hem insan hatası ekler; tekrar yasaklanmaz, güvenli hale getirilir.
## C
İş, iş günü ve iş türü gibi bir anahtarla tekilleştirilmeli, üretilen kayıtlarda benzersizlik kısıtı bulunmalı ve iş işlenmiş kayıtları atlayarak kaldığı yerden devam edebilmelidir
🤖 Doğru: tekrar güvenli hale gelir; ikinci çalıştırma yeni kayıt üretmez ve iş SLA içinde tamamlanır.
## D
İş bittikten sonra bir kontrol sorgusu çalıştırılıp aynı krediye iki kayıt varsa fazlası silinmelidir; böylece hatalı kayıtlar temizlenmiş olur
🤖 finansal kayıt silinerek düzeltilmez ve temizlik ile üretim arasındaki pencerede yanlış veri kullanılmış olabilir.
## Doğru
C
## Açıklama
Gün sonu işleri (tahakkuk, gecikme hesaplama, ekstre üretimi, mutabakat) dar bir pencerede biter ve bitiş saati bir **hizmet düzeyi taahhüdüdür (SLA)**; ertesi günün açılışı buna bağlıdır. Bu yüzden "yeniden başlatmayalım, elle girelim" yaklaşımı hem pencereyi kaçırır hem insan hatası ekler; "tek sunucuda çalıştıralım" da yetmez, çünkü tekrar yalnız ikinci kopyadan değil **elle yeniden başlatmadan** da doğar. Doğru tasarım tekrarı yasaklamak değil, **güvenli** hale getirmektir: (1) iş çalıştırması **iş günü + iş türü** gibi bir anahtarla tekilleştirilir, (2) üretilen kayıtlarda benzersizlik kısıtı bulunur (aynı kredi + aynı iş günü + aynı kayıt türü ikinci kez yazılamaz), (3) iş **yeniden başlatılabilir (restartable)** kurgulanır — işlenmiş kayıtları tekrar işlemeden kaldığı yerden devam eder; Spring Batch bunun için çalıştırma durumunu kalıcı tutar (`222`, `223`). Sonradan "fazlasını sil" yaklaşımı iki açıdan yanlıştır: finansal kayıt silinerek düzeltilmez (düzeltme **ters kayıtla** yapılır ve iz korunur) ve hatalı kayıtların üretilmesiyle temizlenmesi arasındaki pencerede o veri ekstreye, rapora veya müşteri bildirimine girmiş olabilir. Ek olarak toplu işin gündüz yüküyle aynı sıcak tablolara yazması kilit çekişmesi yaratır; yazma pencereleri ve okuma tarafı ayrıştırılır.

# kredimim-5 | İleri
## Soru
Kubernetes üzerinde çalışan kullandırım servisinin yeni sürümü dağıtılırken, bir pod tam para hareketini işlerken kapatılıyor. Bu senaryoya karşı mimarinin sağlaması gereken nedir?
## A
Dağıtım yalnız gece, işlem hacminin sıfır olduğu saatlerde yapılmalıdır; trafik yokken kapanan pod hiçbir işlemi yarıda bırakmaz
🤖 bu riski azaltır ama kaldırmaz; pod düğüm arızası, ölçek küçültme veya tahliye ile her an kapanabilir.
## B
Zarif kapanışla süregelen istekler bitirilmeli, olay yayını outbox ile iş verisiyle aynı yerel işlemde güvenceye alınmalı ve sonucu bilinmeyen adım benzersiz iş anahtarıyla idempotent biçimde tekrarlanabilmelidir
🤖 Doğru: kesinti hem istek seviyesinde hem veri seviyesinde karşılanır ve tekrar çift kayıt üretmez.
## C
Kullandırım servisi tek kopya çalıştırılmalı ve güncelleme sırasında önce eski kopya tamamen kapatılıp sonra yenisi açılmalıdır; böylece iki sürüm asla aynı anda çalışmaz
🤖 bu kesinti üretir ve yarıda kalan işlemi çözmez; tek kopya ayrıca dayanıklılığı düşürür.
## D
Kullandırım isteği alındığında tüm zincir tek bir uzun veritabanı işleminde tutulmalı, pod kapanırsa işlem geri alınacağı için tutarsızlık oluşmaz
🤖 zincir farklı servislere ve eski sisteme yayıldığı için tek işleme alınamaz; uzun işlem ayrıca sıcak kayıtları kilitler.
## Doğru
B
## Açıklama
Yuvarlanan güncellemede yeni pod hazır olmadan eskisi kapatılmaz (`218`), ama bu yalnız **HTTP seviyesinde** kesintisizliktir; para seviyesinde üç şey daha gerekir. Birincisi **zarif kapanış**: pod trafikten çıkarılır, süregelen istekler bitirilir ve kapanışa yeterli süre tanınır. İkincisi **outbox**: iş verisi ile yayınlanacak olay aynı yerel işlemde yazıldığı için "veritabanına yazdım ama olayı yayınlayamadım" durumu oluşmaz; yayın kapanıştan sonra kaldığı yerden sürer (`130`). Üçüncüsü **idempotent tekrar**: kapanış anında sonucu bilinmeyen adım yeniden denendiğinde, **benzersiz iş anahtarı** üzerindeki kısıt ikinci kaydı engeller. Dağıtımı gece yapmak riski azaltır ama kaldırmaz — pod yalnız dağıtımda değil, düğüm arızasında, ölçek küçültmede veya tahliyede de kapanır; mimari bunu **normal** kabul etmelidir. Tek kopya çalıştırıp önce kapatıp sonra açmak hem kesinti üretir hem yarıda kalan işlemi çözmez, üstelik tek kopya dayanıklılığı düşürür. Zinciri tek uzun veritabanı işlemine almak da mümkün değildir: adımlar farklı servislere ve eski çekirdek sisteme yayılır, ayrıca uzun işlem sıcak limit kayıtlarını kilitleyerek verimi çökertir.

# kredimim-6 | Orta
## Soru
Mülakatta "bir kredi sistemi tasarlar mısın?" sorusu soruluyor ve tahtada başlaman isteniyor. En doğru ilerleyiş hangisidir?
## A
Doğrudan teknoloji seçimleriyle başlanmalı: Spring Boot, Kafka, Oracle, Kubernetes denilip her birinin neden sektör standardı olduğu anlatılmalıdır
🤖 araç adı saymak karar göstermez; mülakatta ölçülen şey seçimlerin gerekçesi ve bedelidir.
## B
Önce veri modeli çizilmeli, tüm tablolar ve alanlar tanımlandıktan sonra servisler bu tablolara göre türetilmelidir
🤖 tablodan türetilen sınırlar iş akışını bölmez; veri modeli servis sınırından sonra netleşir.
## C
Önce hedef mimarinin kutuları çizilmeli, sonra hangi ekibin hangi kutuyu yazacağı ve dağıtım hattı anlatılmalıdır
🤖 kutuyla başlamak gereksinimi atlar; ekip ve dağıtım tasarımın sonucu olmalı, girdisi değil.
## D
Gereksinim netleştirilir, aktör ve akışlar çıkarılır, servis sınırları çizilir, tutarlılık stratejisi seçilir, veri ve sıcak noktalar, hata/telafi senaryoları, en son ölçek ve işletme anlatılır
🤖 Doğru: soru bir karar zinciri ister; bu sıra her kararı bir öncekinin gerekçesine dayandırır.
## Doğru
D
## Açıklama
Bu soruda ölçülen şey teknoloji bilgisi değil, **karar verme sırasıdır** (`141`). Doğru ilerleyiş: (1) **gereksinimi netleştir** — hangi kredi ürünü, kim başvuruyor, hacim ne, karar süresi kabul edilebilir mi, eski çekirdek sistem var mı; (2) **aktörleri ve akışları çıkar** — müşteri, personel, karar sistemi, muhasebe, denetim; (3) **servis sınırlarını çiz** ve neden orada olduğunu gerekçelendir; (4) **tutarlılık stratejisini seç** — nerede güçlü tutarlılık (limit, para), nerede nihai tutarlılık (rapor, bildirim), saga ve telafi adımlarını adlandır; (5) **veri modelini ve sıcak noktaları göster** — hangi tablo sıcak, kilit nerede, raporlama nasıl ayrılıyor; (6) **hata ve telafi senaryolarını** anlat — çekirdek sistem kapalıysa, olay iki kez gelirse, pod kapanırsa, toplu iş iki kez çalışırsa; (7) **ölçek ve işletme** — gün sonu penceresi, dağıtım, iş metrikleri ve uyarılar. Teknoloji adıyla başlamak en sık hatadır: araç adı bir karar değildir ve bedeli konuşulmadığı sürece bilgi göstermez. Veri modeliyle başlamak da yanlıştır; tablolardan türetilen sınırlar veriyi böler ama iş akışını bölmez, veri modeli servis sınırından **sonra** netleşir. Kutuları çizip ekip/dağıtım anlatmak ise gereksinimi hiç sormadan çözüme atlamaktır; ekip yapısı ve dağıtım hattı tasarımın sonucu olmalıdır, girdisi değil.
