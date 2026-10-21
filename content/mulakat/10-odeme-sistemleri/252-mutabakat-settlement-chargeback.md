---
slug: mutabakat-settlement-chargeback
baslik: "Mutabakat, Settlement ve Chargeback: Paranın Gerçekten Ne Zaman El Değiştirdiği"
kademe: İleri
sira: 252
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo şirketi düşün. Kurye paketi teslim edip elindeki cihazda "teslim edildi" tuşuna bastığında iş bitmiş görünür. Ama muhasebe için bitmemiştir: o gün teslim edilen üç bin paketin ücreti akşam kapanışta toplanır, iade edilenler düşülür, hasarlı çıkanlar ayrı bir listeye ayrılır ve şirketin hesabına iki gün sonra **tek bir toplu tutar** geçer. Üstelik o tutar, teslim edilen paketlerin fiyat toplamına eşit değildir — aracı komisyonu kesilmiştir. Ertesi sabah birinin oturup "bizim listemizde üç bin paket vardı, gelen parada iki bin dokuz yüz doksan sekiz paket görünüyor, ikisi nerede?" sorusunu sorması gerekir. İşte o soruyu soran işe **mutabakat** denir.

Kart ödemesi de tam olarak böyle çalışır. Müşteri onay ekranını gördüğünde para el değiştirmemiştir. El değiştirmesi **dönemseldir** ve günler sürer (`250`).

Bu gecikmenin sistemde üç somut sonucu vardır; ödeme mülakatının orta bölümü bu üçünün üzerine kuruludur:

- Para henüz hareket etmediyse iptalin adı **void** olur. Hareket ettikten sonraki iptalin adı ise **refund**'dur.
- Komisyon kesildiği için **işlem tutarı ile hesaba geçen tutar aynı sayı değildir**.
- Dışarıdan gelen takas (settlement) dosyası ile kendi kayıtların arasındaki farkı bulan bir işin her gece koşması gerekir.

Bunlara bir de kart hamilinin işleme itiraz ettiği **chargeback** akışı eklenir. Havacılıkta iptal edilen bir uçuş, chargeback'in birinci sebebidir.

### Temel kavramlar

- **clearing (netleştirme):** onaylanmış işlem kayıtlarının taraflar arasında karşılıklı olarak toplanıp netleştirilmesi; para değil **bilgi** hareket eder.
- **settlement (takas):** netleştirilen tutarın taraflar arasında **fiilen** el değiştirmesi. T+1, T+2 bunun işlem gününden kaç gün sonra olduğunu söyler.
- **cut-off (kesim saati):** bir işlemin hangi settlement dönemine gireceğini belirleyen gün sonu saati. Kesimden bir dakika sonra yapılan işlem ertesi günün dosyasındadır.
- **settlement file (mutabakat dosyası):** karşı tarafın (acquirer, PSP, şema, BSP) o dönemde neyi settle ettiğini satır satır bildiren dosya.
- **reconciliation (mutabakat):** kendi kayıtlarınla settlement dosyasının eşleştirilip farkların çıkarılması.
- **exception kuyruğu:** eşleşmeyen kayıtların düştüğü, operasyon ekibinin incelediği liste.
- **void (iptal):** henüz settle olmamış işlemin iptali; para hiç çekilmez.
- **refund (iade):** settle olmuş işlemin **ayrı bir finansal işlemle** geri verilmesi.
- **chargeback (ters ibraz / itiraz):** kart hamilinin işleme itiraz edip parayı issuer aracılığıyla geri alması.
- **representment (yeniden ibraz):** merchant'ın chargeback'e karşı kanıt sunup itirazı reddettirme girişimi.

### Clearing ve settlement: paranın kendi takvimi vardır

Müşteri "ödeme başarılı" ekranını gördüğünde elde olan şey bir **otorizasyon kodudur**; para değildir (`250`). Paranın hareketi ayrı bir takvimde yürür ve üç aşamalıdır. Gün içindeki onaylı işlemler kesim saatine kadar biriktirilir. Kesimden sonra toplu olarak **netleştirmeye (clearing)** gönderilir. Netleşen tutar **T+1** ya da **T+2** günü merchant'ın hesabına geçer.

Kesim saati bu takvimin sessiz ama kritik parçasıdır. Saat 23:00 kesimi olan bir sistemde 22:58'de yapılan işlem bugünün dosyasındadır; 23:02'de yapılan işlem yarının dosyasındadır. İki işlem müşteri açısından neredeyse aynı dakikada yapılmıştır ama farklı takas dönemlerine düşer. Bu da aşağıda göreceğimiz void/refund kararını doğrudan belirler.

İkinci ve daha ağır sonuç şudur: **hesaba geçen tutar, işlem tutarı değildir.** 4.850,00 TL'lik bir bilet satışında yüzde 1,8 komisyonla 87,30 TL kesilir. Merchant'ın hesabına 4.762,70 TL geçer. Bu ikisi birbirinin yerine kullanılabilecek sayılar değildir; **iki ayrı kavramdır**. Sistemde de ayrı alanlar olarak yaşamalıdır: müşteriden tahsil edilen brüt tutar, kesilen komisyon, hesaba geçen net tutar.

Tek bir `amount` alanı tutup takas dosyasından gelen net tutarı onun üzerine yazmak iki şeyi birden bozar. Birincisi, müşteriye ne kadar tahsil edildiği bilgisi kaybolur — oysa iade ve itiraz brüt tutar üzerinden yürür. İkincisi, muhasebedeki komisyon gideri hiç görünmez. Tutarların hepsi ondalık duyarlı tiple taşınır. Kayan noktalı tiple (float/double) biriken kuruş farkları, tam olarak mutabakatın tutmadığı yerdir (`226`).

Üçüncüsü, settle olmuş bir işlem artık **geri alınamaz** bir gerçektir. Bu noktadan sonra yapılan her düzeltme yeni bir finansal işlemdir: refund, ters kayıt ya da chargeback. Ödeme kaydının durumunu geriye çevirerek düzeltme yapan tasarım, tam da bu yüzden yanlıştır (`250`).

```svg
<svg viewBox="0 0 660 322" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Islem gununden settlement gunune zaman cizgisi cut-off saati ve komisyon kesintisi">
  <defs>
    <marker id="mutab-tl-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="mutab-tl-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Paranın takvimi: T, cut-off ve T+2</text>
  <line x1="34" y1="118" x2="634" y2="118" stroke="#5D7789" stroke-width="1.3" marker-end="url(#mutab-tl-arw)"/>
  <line x1="86" y1="96" x2="86" y2="140" stroke="#186399" stroke-width="2"/>
  <text x="86" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">T · İŞLEM</text>
  <text x="86" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">otorizasyon kodu var</text>
  <text x="86" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">para yok</text>
  <line x1="248" y1="96" x2="248" y2="140" stroke="#B07D20" stroke-width="2" stroke-dasharray="4 3"/>
  <text x="248" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">CUT-OFF · 23:00</text>
  <text x="248" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">22:58 bugünün dosyasında</text>
  <text x="248" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">23:02 yarının dosyasında</text>
  <line x1="410" y1="96" x2="410" y2="140" stroke="#5D7789" stroke-width="2"/>
  <text x="410" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">T+1 · CLEARING</text>
  <text x="410" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bilgi netleşir</text>
  <text x="410" y="170" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">para hâlâ hareket etmedi</text>
  <line x1="572" y1="96" x2="572" y2="140" stroke="#25804C" stroke-width="2"/>
  <text x="572" y="88" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">T+2 · SETTLEMENT</text>
  <text x="572" y="156" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">para fiilen el değiştirir</text>
  <rect x="86" y="110" width="162" height="16" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="167" y="122" text-anchor="middle" font-size="9" font-weight="700" fill="#186399" font-family="system-ui">VOID penceresi</text>
  <rect x="248" y="110" width="380" height="16" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.1"/>
  <text x="438" y="122" text-anchor="middle" font-size="9" font-weight="700" fill="#B07D20" font-family="system-ui">bu noktadan sonra iptal = REFUND (ayrı finansal işlem)</text>
  <rect x="16" y="192" width="304" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="168" y="213" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İşlem tutarı ≠ hesaba geçen tutar</text>
  <text x="32" y="236" font-size="10.5" fill="#1C3247" font-family="system-ui">brüt (müşteriden tahsil)</text>
  <text x="304" y="236" text-anchor="end" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">4.850,00 TL</text>
  <text x="32" y="256" font-size="10.5" fill="#B23B3B" font-family="system-ui">komisyon (%1,8)</text>
  <text x="304" y="256" text-anchor="end" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">− 87,30 TL</text>
  <line x1="32" y1="264" x2="304" y2="264" stroke="#25804C" stroke-width="1.1"/>
  <text x="32" y="281" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">net (hesaba geçen)</text>
  <text x="304" y="281" text-anchor="end" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">4.762,70 TL</text>
  <text x="168" y="297" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">üçü de ayrı alan olarak saklanır</text>
  <rect x="340" y="192" width="304" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="492" y="213" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış tasarım</text>
  <text x="492" y="235" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tek bir amount alanı tutup settlement'tan</text>
  <text x="492" y="250" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">gelen net tutarı üzerine yazmak</text>
  <text x="492" y="270" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">iade ve itiraz brüt üzerinden yürür → kaynak kaybolur</text>
  <text x="492" y="285" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">komisyon gideri muhasebede hiç görünmez</text>
  <text x="492" y="299" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tutarlar ondalık duyarlı tiple taşınır (226)</text>
  <path d="M86 140 L86 182" fill="none" stroke="#25804C" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#mutab-tl-grn)"/>
</svg>
```

### Mutabakat: üç yönlü eşleştirme ve exception kuyruğu

Mutabakat, kendi işlem kayıtlarınla karşı tarafın settlement dosyasını yan yana koyup **farkı bulma** işidir. Somut hali şudur: "biz bugün 10.000 işlem ve 4.200.000 TL gönderdik; banka 9.998 işlem ve 4.190.000 TL settle etmiş — eksik 2 işlem nerede, 10.000 TL'lik fark nereden geliyor?"

Eşleştirme **üç yönlüdür** ve bu üçlü, mülakatta bilinmesi beklenen çekirdektir:

1. **Bizde var, onlarda yok.** Biz işlemi başarılı saymışız ama settlement dosyasında satırı yok. En tehlikeli sınıf budur: hizmet verilmiş olabilir ama para hiç gelmemiştir. Sebepler tipik olarak kesim saatinin öbür tarafına düşmüş bir işlem, capture edilmemiş bir otorizasyon ya da PSP tarafında reddedilip bizde başarılı görünen bir kayıttır.
2. **Onlarda var, bizde yok.** Dosyada satır var ama bizim tarafta karşılığı yok. Genellikle timeout aldığımız ve sonucunu bilmediğimiz işlemlerdir: PSP cevap dönememiş, biz "olmadı" varsaymışız, oysa para çekilmiştir. Bu satırlar `UNKNOWN` durumundaki kayıtların gerçek akıbetini söyleyen tek kaynaktır.
3. **İkisinde de var ama tutar farklı.** Kısmi refund, komisyon kesintisinin yanlış eşlenmesi, çoklu para biriminde kur farkı ya da yuvarlama hatası. Kuruş düzeyindeki bu farklar tek işlemde önemsiz görünür, milyonlarca işlemde birikince mutabakat hiç tutmaz.

Teknik olarak bu iş, **`FULL OUTER JOIN`'in gerçek hayattaki karşılığıdır** (`29`). İki kümeyi de tam tutup eşleşmeyen tarafları `NULL` olarak görmek birinci ve ikinci sınıfı üretir. Eşleşen satırlarda tutar karşılaştırması ise üçüncü sınıfı üretir.

Eşleştirme anahtarını seçmek de sıradan bir detay değildir. Senin kendi kimliğin karşı tarafın dosyasında bulunmayabilir. Bu yüzden PSP/acquirer işlem referansı ödeme kaydında **saklanır ve indekslenir** (`146`). Gece boyunca yüz binlerce satırı tarayan bir iş, indekssiz bir eşleştirme sorgusuyla sabaha yetişmez.

Çalışma biçimi bir **gün sonu toplu işidir** (`222`, `223`). Dosya indirilir, doğrulanır, satır satır okunur, eşleştirilir ve sonuçlar yazılır. Bu işin üç özelliği pazarlık konusu değildir.

- **Yeniden çalıştırılabilir olmalıdır.** Dosya geç gelebilir, iş yarıda kesilebilir, aynı dosya iki kez işlenebilir. İkinci çalıştırma ikinci bir düzeltme kaydı üretmemelidir.
- **Kısmi başarısızlığa dayanmalıdır.** Bozuk bir satır tüm işi düşürmemelidir; o satır atlanıp raporlanmalıdır.
- **Çıktısı bir kuyruktur.** Eşleşmeyen kayıt otomatik düzeltilmez; **istisna (exception) kuyruğuna** düşer ve operasyon ekibi inceler. Para üzerinde otomatik düzeltme yapan bir mutabakat işi, ilk hatalı dosyada gerçek bir zarar üretir.

Neden bu kadar ciddiye alınır? Çünkü **mutabakatı tutmayan bir ödeme sistemi bir yazılım sorunu değil, düzenleyici sorunudur.** Kurum, elindeki paranın nereden geldiğini ve müşteriye ne borçlu olduğunu satır düzeyinde açıklayabilmek zorundadır. "Fark birikti, yıl sonunda toplu düzeltiriz" cümlesi denetimde savunulamaz. Bu yüzden iki şey **operasyonel metrik** olarak izlenir ve eşiği aşınca alarm üretir: mutabakat farkının gün bazlı miktarı ve istisna kuyruğunun uzunluğu (`104`).

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uc yonlu mutabakat eslestirmesi ve exception kuyrugu">
  <defs>
    <marker id="mutab-rec-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="mutab-rec-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç yönlü mutabakat: FULL OUTER JOIN'in gerçek hayattaki hali</text>
  <rect x="16" y="40" width="180" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="106" y="61" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Bizim kayıtlarımız</text>
  <text x="106" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">10.000 işlem · 4.200.000 TL</text>
  <rect x="464" y="40" width="180" height="50" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="554" y="61" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Settlement dosyası</text>
  <text x="554" y="78" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">9.998 işlem · 4.190.000 TL</text>
  <rect x="216" y="40" width="228" height="50" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="330" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Gece mutabakat batch'i</text>
  <text x="330" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yeniden çalıştırılabilir · 222, 223 · anahtar indeksli 146</text>
  <path d="M196 65 L212 65" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mutab-rec-arw)"/>
  <path d="M464 65 L448 65" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mutab-rec-arw)"/>
  <path d="M330 90 L330 108" fill="none" stroke="#25804C" stroke-width="1.5"/>
  <line x1="94" y1="108" x2="566" y2="108" stroke="#25804C" stroke-width="1.5"/>
  <path d="M94 108 L94 122" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#mutab-rec-arw)"/>
  <path d="M252 108 L252 122" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#mutab-rec-arw)"/>
  <path d="M409 108 L409 122" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#mutab-rec-arw)"/>
  <path d="M566 108 L566 122" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#mutab-rec-arw)"/>
  <rect x="16" y="126" width="156" height="78" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="94" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">EŞLEŞTİ</text>
  <text x="94" y="164" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı referans</text>
  <text x="94" y="178" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı tutar</text>
  <text x="94" y="194" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">SETTLED yazılır</text>
  <rect x="174" y="126" width="156" height="78" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="252" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">BİZDE VAR · ONLARDA YOK</text>
  <text x="252" y="164" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hizmet verildi, para gelmedi</text>
  <text x="252" y="180" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">cut-off'un öbür tarafı ·</text>
  <text x="252" y="194" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">capture edilmemiş auth</text>
  <rect x="332" y="126" width="156" height="78" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="410" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">ONLARDA VAR · BİZDE YOK</text>
  <text x="410" y="164" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">para geldi, kaydımız yok</text>
  <text x="410" y="180" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">timeout sonrası UNKNOWN</text>
  <text x="410" y="194" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">akıbeti dosya söyler</text>
  <rect x="490" y="126" width="154" height="78" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="567" y="146" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">TUTAR FARKLI</text>
  <text x="567" y="164" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kısmi refund · komisyon</text>
  <text x="567" y="180" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kur farkı · yuvarlama</text>
  <text x="567" y="194" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kuruşlar birikir (226)</text>
  <path d="M252 204 L252 234" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#mutab-rec-red)"/>
  <path d="M410 204 L410 234" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#mutab-rec-red)"/>
  <path d="M567 204 L567 224 L470 224 L470 234" fill="none" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#mutab-rec-red)"/>
  <rect x="160" y="238" width="484" height="46" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="402" y="258" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">EXCEPTION KUYRUĞU → operasyon ekibi inceler</text>
  <text x="402" y="275" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">otomatik düzeltme yapılmaz · fark tutarı ve kuyruk uzunluğu metriktir (104)</text>
  <rect x="16" y="294" width="628" height="34" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.3"/>
  <text x="330" y="315" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Mutabakatı tutmayan ödeme sistemi bir yazılım sorunu değil, düzenleyici sorunudur</text>
</svg>
```

### Void mü refund mu? — cut-off saatinin iki tarafı

İkisi de "müşterinin parasını geri ver" işidir ama teknik ve mali olarak aynı şey değildirler; ayıran çizgi **settlement**'tir.

**Void**, henüz settle olmamış bir işlemin iptalidir. Para hiç çekilmediği için geri verilecek bir şey de yoktur — bloke kalkar, ekstrede satır bile oluşmaz, müşteri çoğu zaman hiçbir şey olmadığını görür. Maliyeti yok denecek kadar azdır ve anında etkilidir.

**Refund**, settle olmuş bir işlemin **ayrı bir finansal işlemle** iade edilmesidir. Yeni bir işlem yaratılır, kendi kimliği, kendi durumu ve kendi settlement takvimi vardır; paranın müşterinin hesabında görünmesi günler sürebilir. Müşterinin ekstresinde **iki satır** kalır: bir tahsilat, bir iade. Bu iki satır çağrı merkezi trafiği üretir ("param çekilmiş, iade görünmüyor") ve bazı komisyon modellerinde ilk işlemin komisyonu geri gelmez.

Havayolu senaryosu bunu somutlaştırır. Bilet **aynı gün** iptal edilirse işlem henüz kesim saatini geçmemiştir; doğru araç **void**'dir — ucuz, temiz, izsiz. **Ertesi gün** iptal edilirse işlem takas olmuştur; tek yol **refund**'dur.

Bu yüzden iptal akışını yazan servis "iptal et" diye tek bir metot çağırmaz. İşlemin durumuna bakar ve **hangi aracın uygulanabilir olduğuna** kendisi karar verir. Bu kararı müşteri temsilcisinin ekranda seçmesine bırakmak, üretimde en sık görülen operasyonel hata kaynaklarından biridir.

İki kural daha vardır ve teknik mülakatta doğrudan sorulur:

- **Kısmi refund desteklenmelidir.** Havacılıkta iade genellikle tam değildir; iptal cezası, servis ücreti ya da kullanılmış bir bacak düşülür. Örnek: 4.850,00 TL'lik biletten 750,00 TL ceza kesilir ve 4.100,00 TL iade edilir. Bunun anlamı şudur: ödeme kaydı "iade edildi mi?" sorusuna evet/hayır (boolean) cevap veremez. Kayıt, **iade edilmiş toplam tutarı** taşımalıdır.
- **Refund tutarı orijinal işlemi aşamaz.** Bu, "olsa iyi olur" türü bir doğrulama değildir; sistemin sınır koşuludur. Kontrol **birikimli** yapılmalıdır: art arda üç kısmi iade yapıldığında toplamları orijinal tutarı geçmemelidir. Dikkat et, aynı anda gelen iki iade isteğinde bu kontrol yarışa açıktır. Bakiyeyi okuyup sonra karar veren kod, iki isteği de geçirebilir. Doğrusu şudur: iade toplamını bölünemez (atomik) biçimde artıran ve sınır aşılırsa hata veren bir yazım. Üstüne her iade isteği kendi tekilleştirme anahtarını (idempotency key) taşır (`253`).

### Chargeback yaşam döngüsü ve kanıt zinciri

Chargeback, kart hamilinin işleme itiraz edip parayı **issuer aracılığıyla** geri almasıdır. Refund'dan farkı şudur: süreci merchant değil, **karşı taraf** başlatır. Tipik gerekçeler ikidir: "bu işlemi ben yapmadım" (dolandırıcılık) ve "hizmeti alamadım". Havacılıkta ikincisi bilhassa yakıcıdır. Çünkü **iptal edilen bir uçuş**, binlerce kart hamiline aynı anda meşru bir itiraz gerekçesi verir.

Döngü dört adımlıdır:

1. **İtiraz.** Kart hamili issuer'ına başvurur.
2. **Geçici iade.** Issuer, kural gereği tutarı müşteriye geri verir ve merchant'tan geri talep eder. Yani para, merchant tarafında **daha savunma yapılmadan** çıkar.
3. **Representment (yeniden ibraz).** Merchant kanıt sunar: işlem gerçekten yapılmıştır, hizmet verilmiştir, kart hamili doğrulanmıştır.
4. **Şema kararı.** Kart şeması kanıtı değerlendirir ve nihai kararı verir (`250` — şema bakiye kararı vermez ama uyuşmazlıkta karar mercii odur).

Merchant'ın maliyeti üç katmanlıdır; üçünü birden söylemek mülakatta fark yaratır. Birincisi **tutarın kaybı**. İkincisi işlem başına sabit bir **chargeback ücreti**; itirazı kazansan bile çoğu modelde bu ücret geri gelmez. Üçüncüsü orandır: chargeback **oranı** belirli bir eşiği aşarsa şemanın izleme programına alınırsın, ceza yersin ve nihayetinde kart kabul edememe riski doğar. Bu yüzden chargeback bir muhasebe kalemi değil, **izlenmesi gereken bir orandır**.

Teknik karşılığı iki maddedir. Birincisi, her işlemin **kanıt zinciri** saklanmalıdır. Zincire şunlar girer: otorizasyon kodu, 3DS doğrulama sonucu ve sorumluluk devri durumu (`251`), işlem zamanı ve IP/cihaz bilgisi, PNR ve bilet numarası (`247`), check-in ve uçuş kullanım kaydı, müşteriye gönderilen onay e-postası. Bu veriler itiraz geldiğinde toplanamaz. İtiraz aylar sonra gelir ve o an elde ne varsa odur. Durum geçişlerinin yalnızca eklenerek tutulması (`250`) tam olarak bu zinciri besler.

İkincisi, **itiraz yönetimi sistemde modellenmelidir**. Chargeback kendi durumları olan bir varlıktır: itiraz alındı → kanıt hazırlanıyor → yeniden ibraz gönderildi → kazanıldı/kaybedildi. Sürecin süre sınırları vardır ve süre kaçırıldığında dava otomatik kaybedilir. E-posta ve tablo dosyasıyla yürütülen bir itiraz süreci, hacim büyüdüğü anda kaybedilen dosyalar üretir.

3DS bağlantısını da kurmak gerekir. Başarılı bir 3DS doğrulaması **dolandırıcılık** temelli chargeback'te sorumluluğu issuer'a kaydırır (`251`). Ama bu bir zırh değildir: "hizmeti alamadım" temelli itirazlarda sorumluluk devri işlemez. İptal edilen uçuş senaryosunda merchant'ı koruyan tek şey, iadeyi kendisinin hızla yapmış olmasıdır. Zamanında yapılmış bir refund, kazanılmış bir chargeback'ten her zaman ucuzdur.

```svg
<svg viewBox="0 0 660 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Chargeback yasam dongusu itiraz gecici iade representment ve sema karari">
  <defs>
    <marker id="mutab-cb-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
    <marker id="mutab-cb-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Chargeback yaşam döngüsü</text>
  <rect x="14" y="42" width="146" height="56" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="87" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · İTİRAZ</text>
  <text x="87" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"ben yapmadım"</text>
  <text x="87" y="92" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"hizmeti alamadım"</text>
  <rect x="178" y="42" width="146" height="56" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="251" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">2 · GEÇİCİ İADE</text>
  <text x="251" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">issuer parayı verir</text>
  <text x="251" y="92" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">para savunmadan önce çıkar</text>
  <rect x="342" y="42" width="146" height="56" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="415" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">3 · REPRESENTMENT</text>
  <text x="415" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">merchant kanıt sunar</text>
  <text x="415" y="92" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">süre sınırlıdır</text>
  <rect x="506" y="42" width="140" height="56" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="576" y="62" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">4 · ŞEMA KARARI</text>
  <text x="576" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uyuşmazlık mercii</text>
  <text x="576" y="92" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">250</text>
  <path d="M160 70 L174 70" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#mutab-cb-arw)"/>
  <path d="M324 70 L338 70" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#mutab-cb-arw)"/>
  <path d="M488 70 L502 70" fill="none" stroke="#B23B3B" stroke-width="1.7" marker-end="url(#mutab-cb-arw)"/>
  <path d="M576 98 L576 118 L440 118 L440 132" fill="none" stroke="#25804C" stroke-width="1.5" marker-end="url(#mutab-cb-grn)"/>
  <path d="M576 98 L576 118 L590 118 L590 132" fill="none" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#mutab-cb-arw)"/>
  <rect x="366" y="136" width="148" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="440" y="152" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">KAZANILDI</text>
  <text x="440" y="168" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tutar geri gelir, ücret gelmez</text>
  <rect x="524" y="136" width="122" height="40" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="585" y="152" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">KAYBEDİLDİ</text>
  <text x="585" y="168" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tutar + ücret gider</text>
  <rect x="14" y="112" width="336" height="94" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="182" y="132" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">KANIT ZİNCİRİ — önceden saklanır</text>
  <text x="30" y="152" font-size="10" fill="#1C3247" font-family="system-ui">· otorizasyon kodu · 3DS sonucu ve liability shift (251)</text>
  <text x="30" y="168" font-size="10" fill="#1C3247" font-family="system-ui">· işlem zamanı · IP / cihaz bilgisi</text>
  <text x="30" y="184" font-size="10" fill="#1C3247" font-family="system-ui">· PNR ve bilet numarası · check-in kaydı (247)</text>
  <text x="182" y="200" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">itiraz aylar sonra gelir: o an elde ne varsa odur</text>
  <rect x="14" y="216" width="632" height="52" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="236" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Maliyet üç katmanlıdır: tutar kaybı + işlem başına chargeback ücreti + oran eşiği aşılırsa şema cezaları</text>
  <text x="330" y="256" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">chargeback bir muhasebe kalemi değil, izlenmesi gereken bir ORANDIR (104)</text>
  <rect x="14" y="280" width="632" height="48" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.3"/>
  <text x="330" y="300" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">İtiraz kendi durum makinesiyle modellenir: alındı → kanıt hazırlanıyor → gönderildi → kazanıldı/kaybedildi</text>
  <text x="330" y="318" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">süre kaçırılırsa dava otomatik kaybedilir · e-posta ve tablo dosyasıyla yürütülen süreç hacimde çöker</text>
</svg>
```

### Toplu iade: uçuş iptal oldu, 200 yolcuya para dönecek

Havacılıkta chargeback'ten kaçınmanın en etkili yolu, iadeyi müşteri itiraz etmeden yapmaktır. Bu da tipik bir toplu iş problemi doğurur: bir uçuş iptal edildi, 200 yolcunun parası iade edilecek. Tasarımın beş bileşeni vardır.

**Her iade tekrar edilse de sonucu değişmez (idempotent).** İade kaydı tekil bir anahtarla korunur; anahtarın seçimi ürüne bağlıdır.

Uçuş iptali gibi **tam iade** senaryosunda anahtar, `(ticket_id)` üzerinde bir benzersizlik kısıtıdır. Aynı bilet için ikinci bir iade denemesi veritabanı seviyesinde reddedilir. Ürün **kısmi iadeye** izin veriyorsa durum değişir; örneğin ceza kesintili iptal ya da yalnızca ek hizmet bedelinin iadesi. Böyle bir üründe tek başına `ticket_id` yanlış anahtardır, çünkü meşru ikinci iadeyi de engeller. Doğru anahtar `(ticket_id, iade_talep_no)` olur. Toplam iade tutarının orijinal tahsilatı aşmadığı da **aynı işlem içinde** kontrol edilir.

Her iki durumda da veritabanı kısıtı, uygulama kodundaki "önce bak, yoksa yaz" kontrolünün yerini tutmaz; onu **gereksiz kılar**. İki iş kopyası aynı anda koşsa bile çifte iade fiziken imkânsız hale gelir (`253`).

**Toplu iş yeniden başlatılabilirdir.** 200 kaydın 137'si işlendikten sonra süreç düşerse, yeniden başlatma 137'yi tekrar işlememelidir. Bunu iki şey sağlar: yukarıdaki tekillik kısıtı ve toplu iş altyapısının ilerleme takibi. Spring Batch'te bunun karşılığı yeniden başlatma (restart) ile atlama/yeniden deneme (skip/retry) politikalarıdır (`222`, `223`).

**Gönderim hızı kontrollüdür.** 200 iade isteğini PSP'ye aynı anda göndermek, karşı tarafın hız sınırına takılmakla sonuçlanır ve yarısı reddedilir. Üstelik bu redler gerçek hata gibi görünür ve kuyruğu kirletir. Doğru davranış iki adımlıdır: hedefin kabul ettiği hızı bilerek gönderim yapmak ve HTTP 429 alındığında `Retry-After` değerine uyup geri çekilmek (`153`).

**Başarısızlıklar kaybolmaz.** Kalıcı olarak başarısız olan iadeler ölü mektup kuyruğuna (DLQ) ya da istisna kuyruğuna düşer, raporlanır ve operasyon ekibi tek tek ele alır. Bir toplu işin "199 başarılı, 1 başarısız" sonucunu sessizce yutması şu demektir: o bir yolcu birkaç hafta sonra chargeback açacak.

**Muhasebede ayrı bir kalem olarak izlenir.** Toplu iade, o günün takas dosyasında normal satışlardan farklı bir görünüm yaratır ve mutabakat farkı gibi okunabilir. Bu yüzden iadeler kendi mutabakat kalemi olarak takip edilir. "Bugün 1.240.000 TL eksik geldi" alarmının cevabı "uçuş iptali nedeniyle 200 iade yapıldı" olabilmelidir. Tutarlar burada da ondalık duyarlı tiple hesaplanır; kısmi iadelerde kesilen ceza ayrı bir kalem olarak yazılır (`226`).

### Mülakatta ne sorulur?

- **"Settlement nedir, T+1/T+2 ne demek?"** Onaylanmış işlemlerin parasının fiilen el değiştirmesidir; işlem anında değil dönemseldir ve işlem gününden bir-iki gün sonra gerçekleşir. Devamını mutlaka söyle: **cut-off saati** işlemin hangi döneme gireceğini belirler ve komisyon kesildiği için **işlem tutarı ile hesaba geçen tutar ayrı kavramlardır**.
- **"Mutabakat nasıl yapılır?"** Karşı tarafın settlement dosyası indirilir, kendi kayıtlarınla **üç yönlü** eşleştirilir — bizde var/onlarda yok, onlarda var/bizde yok, ikisinde var tutar farklı. `FULL OUTER JOIN` mantığıdır (`29`), gece toplu işi olarak koşar (`222`, `223`), farklar **exception kuyruğuna** düşer ve otomatik düzeltilmez.
- **"Void ile refund farkı nedir?"** Settle olmadan iptal void'dir, para hiç çekilmez, ekstrede iz kalmaz; settle sonrası refund'dur, ayrı bir finansal işlemdir ve günler sürer. Havayolunda aynı gün iptal void, ertesi gün refund. Kısmi refund desteklenmeli ve iade toplamı orijinali **aşamamalıdır**.
- **"Chargeback süreci nasıl işler, maliyeti nedir?"** İtiraz → issuer geçici iade → merchant representment → şema kararı. Maliyet: tutar + chargeback ücreti + oran eşiği aşılırsa şema cezaları. Teknik gereklilik: kanıt zinciri önceden saklanır ve itiraz yönetimi kendi durum makinesiyle modellenir.
- **"3DS beni chargeback'ten korur mu?"** Kısmen. Başarılı 3DS **fraud** temelli itirazda sorumluluğu issuer'a kaydırır (`251`); "hizmeti alamadım" temelli itirazda korumaz.
- **"Uçuş iptal oldu, 200 yolcuya nasıl iade yaparsın?"** Bilet bazında tekil kayıtla idempotent iade, yeniden başlatılabilir toplu iş (`222`, `223`), kontrollü gönderim hızı (`153`), başarısızlar için DLQ/exception kuyruğu, muhasebede ayrı mutabakat kalemi.
- **"Mutabakat farkı çıktı, ne yaparsın?"** Yanlış cevap: "kodda düzeltirim" ya da "farkı otomatik kapatırım". Doğru cevap: farkı sınıflandır, exception kuyruğuna al, kaynağını tespit et (cut-off, timeout, kısmi iade, kur), düzeltmeyi **yeni bir kayıtla** yap ve fark metriğini izle.
- **Sık yapılan hata:** "para çekildi" ile "para hesabımıza geçti" cümlelerini aynı şey sanmak. Bu ikisi arasında günler, bir komisyon kesintisi ve bir mutabakat işi vardır.

### Tek cümlede

Kart ödemesinde para işlem anında değil **dönemsel olarak** el değiştirir. Kesim saati işlemin hangi takas dönemine gireceğini belirler; takas çizgisi ise iptalin **void** mü **refund** mu olacağını belirler. Komisyon kesildiği için **işlem tutarı ile hesaba geçen tutar iki ayrı kavramdır**. Karşı tarafın dosyasıyla kendi kayıtlarını **üç yönlü** eşleştiren bir gece işi farkları istisna kuyruğuna düşürür; çünkü mutabakatı tutmayan bir ödeme sistemi düzenleyici sorunudur. Kart hamili itiraz ettiğinde savunmayı yapan şey kod değil, işlem anında saklanmış **kanıt zinciridir**. Toplu iadede ise tek koruma, her iadenin bilet bazında tekrar edilse de sonucu değişmeyecek biçimde yazılmasıdır.

# mutab-1 | Orta

## Soru
Müşteri sabah 10:00'da 4.850 TL'lik bilet aldı ve aynı gün saat 16:00'da, sistemin 23:00 kesim saatinden önce iptal etti. Çağrı merkezi ekranında "iptal et" tuşuna basıldığında sistemin uygulaması gereken işlem hangisidir?

## A
Refund işlemi başlatılır; iade her koşulda aynı akışla yürütüldüğünde süreç tek tipleşir ve operasyon ekibi hangi durumda ne yapacağını düşünmek zorunda kalmaz
🤖 settle olmamış bir işlemi refund'a sokmak gereksiz maliyet üretir: müşteri ekstresinde iki satır oluşur, komisyon geri gelmeyebilir ve iade günler sürer.

## B
İşlem kaydı silinip müşteriye yeni bir satın alma akışı sunulur; ortada henüz settle olmamış bir kayıt bulunduğu için finansal iz oluşmamıştır
🤖 finansal kayıt hiçbir koşulda silinmez; kayıt yok edilirse otorizasyon çözülmeden kalır ve mutabakatta karşılıksız bir hareket görünür.

## C
İşlem henüz settle olmadığı için void uygulanır; bloke kalkar, müşterinin ekstresinde satır oluşmaz ve karara ekran operatörü değil sistem, işlemin durumuna bakarak varır
🤖 Doğru: void ile refund arasındaki çizgiyi settlement belirler ve bu kararı iş kuralı verir.

## D
Otorizasyon serbest bırakılmadan bekletilir; geçerlilik penceresi dolduğunda issuer blokeyi kendiliğinden çözeceği için ek bir işlem yapmaya gerek yoktur
🤖 müşterinin limiti günlerce gereksiz yere bloke kalır ve sistemde kapatılmamış bir işlem, mutabakatta eşleşmeyen kayıt olarak görünür.

## Doğru
C

## Açıklama
Void ile refund arasındaki ayrımı belirleyen tek şey **settlement**'tir. İşlem henüz settle olmadıysa — yani kesim saatini geçip clearing'e gitmediyse — doğru araç **void**'dir: bloke kalkar, para hiç çekilmemiş olur, müşterinin ekstresinde satır bile oluşmaz. Settle olduktan sonra ise tek yol **refund**'dur; refund ayrı bir finansal işlemdir, kendi kimliği ve kendi settlement takvimi vardır, müşterinin hesabında görünmesi günler sürer ve ekstrede bir tahsilat + bir iade olmak üzere **iki satır** bırakır. Bu iki satır doğrudan çağrı merkezi trafiği ve bazı komisyon modellerinde geri gelmeyen bir maliyet demektir; dolayısıyla "her iptali refund yapalım, süreç tek tipleşsin" cevabı basitliği maliyetle satın alır. Kaydı silmek en zararlı seçenektir: otorizasyon çözülmeden kalır ve settlement dosyasında karşılığı olmayan bir hareket görünür — mutabakatın "onlarda var, bizde yok" sınıfı tam olarak böyle doğar. Otorizasyonun kendiliğinden düşmesini beklemek de kabul edilebilir bir tasarım değildir: müşterinin limiti günlerce gereksiz bloke kalır ve kapatılmamış işlem exception kuyruğuna düşer. Kritik tasarım noktası ise şıkta gizlidir: hangi aracın kullanılacağına ekrandaki operatör değil, işlemin durumuna bakan **iş kuralı** karar vermelidir.

# mutab-2 | Orta

## Soru
Muhasebe ekibi, satış raporundaki günlük ciro ile banka hesabına geçen tutarın hiçbir gün birbirini tutmadığını bildiriyor. İnceleyince ödeme kaydında tek bir `amount` alanı olduğu ve settlement dosyası işlenirken bu alanın gelen net tutarla güncellendiği görülüyor. Doğru düzeltme nedir?

## A
Ödeme kaydı brüt işlem tutarını, kesilen komisyonu ve hesaba geçen net tutarı ayrı alanlarda tutar; settlement dosyası net tutarı yazar ama brüt tutar hiçbir zaman değiştirilmez
🤖 Doğru: işlem tutarı ile hesaba geçen tutar iki ayrı kavramdır ve ikisi de kaynağıyla birlikte saklanmalıdır.

## B
Satış raporu komisyon oranıyla çarpılarak beklenen net tutar hesaplanır ve rapor bu değerle karşılaştırılır; böylece ek alan açmadan tutarsızlık ortadan kalkar
🤖 komisyon oranı işlem tipine, karta ve taksite göre değişir; hesaplanmış tahmin gerçek kesintinin yerine geçmez ve mutabakatta kanıt değeri taşımaz.

## C
Ciro raporu doğrudan settlement dosyasından üretilir; banka ne settle ettiyse gerçek odur ve iki kaynak yerine tek kaynak kullanmak tutarsızlığı kökten çözer
🤖 müşteriden ne tahsil edildiği bilgisi kaybolur; iade ve itiraz brüt tutar üzerinden yürüdüğü için bu bilgi olmadan kısmi refund sınırı bile doğrulanamaz.

## D
Fark kuruş düzeyinde kaldığı için bir tolerans eşiği tanımlanır ve eşiğin altındaki sapmalar mutabakatta otomatik kapatılır
🤖 buradaki fark yuvarlamadan değil komisyondan doğar ve yapısaldır; gerçek bir gider kalemini tolerans sayarak gizlemek muhasebeyi ve denetim izini bozar.

## Doğru
A

## Açıklama
Komisyon kesildiği için **işlem tutarı ile hesaba geçen tutar aynı sayı değildir** ve bu bir hata değil, ödeme sisteminin doğasıdır: 4.850,00 TL'lik bir satışta yüzde 1,8 komisyonla 87,30 TL kesilir ve hesaba 4.762,70 TL geçer. Sistemin bunları **iki ayrı kavram** olarak taşıması gerekir; doğru model üç alandır — brüt işlem tutarı, kesilen komisyon, net tutar — ve settlement dosyası yalnızca son ikisini doldurur, brüt tutara asla dokunmaz. Tek bir alanı net tutarla ezmek iki şeyi birden yok eder: müşteriden fiilen ne tahsil edildiği bilgisi kaybolur (iade, kısmi iade sınırı ve chargeback savunması brüt üzerinden yürür) ve komisyon gideri muhasebede hiçbir yerde görünmez. Komisyonu oranla hesaplayıp tahmin etmek de yeterli değildir: oran işlem tipine, kart tipine ve taksit sayısına göre değişir; mutabakatta kanıt olan şey hesaplanan değer değil, dosyada gelen değerdir. Raporu tamamen settlement dosyasından üretmek sorunu kaynaktan çözmez, sadece diğer tarafı kör eder. Farkı tolerans eşiğiyle örtmek ise en tehlikelisidir: buradaki sapma yuvarlamadan değil **gerçek bir gider kaleminden** doğar ve yapısaldır; tolerans mekanizması gerçek anomalileri de birlikte yutar. Tutarların ondalık duyarlı tiple taşınması (`226`) bu modelin ayrılmaz parçasıdır.

# mutab-3 | İleri

## Soru
Gece koşan mutabakat işi, PSP'nin settlement dosyasını kendi ödeme kayıtlarıyla karşılaştırıyor. Bir sabah dosyada olup bizde karşılığı bulunmayan 43 satır çıkıyor; kontrol edildiğinde bu işlemlerin hepsinde PSP çağrısının timeout aldığı ve kaydın `UNKNOWN` durumunda kaldığı görülüyor. Sistem bu satırları nasıl ele almalıdır?

## A
Dosyada olup bizde olmayan satırlar karşı tarafın hatası kabul edilir ve PSP'ye toplu itiraz açılır; kendi tarafımızda kayıt bulunmadığı için işlem yapılamaz, düzeltilmiş dosya gelene kadar bu satırlar mutabakat kapsamı dışında tutulur
🤖 timeout, işlemin gerçekleşmediği anlamına gelmez; para gerçekten çekilmiş olabilir ve dosya bu akıbeti bildiren en güvenilir kaynaktır.

## B
Eşleşmeyen satırlar için ödeme kayıtları otomatik oluşturulup doğrudan `SETTLED` işaretlenir; dosya nihai gerçek olduğu için insan incelemesine gerek kalmaz ve exception kuyruğu gereksiz yere şişmemiş olur
🤖 mutabakat çıktısı üzerinde otomatik düzeltme yapmak, ilk hatalı veya çift gönderilmiş dosyada gerçek bir mali zarar üretir.

## C
Satırlar bir sonraki günün dosyasında eşleşme ihtimaline karşı beklemeye alınır; birkaç gün üst üste eşleşmezse kayıt kapatılır ve kalan fark, dönem sonunda tek kalemde muhasebeleştirilecek bir sapma olarak raporlanır
🤖 cut-off kaynaklı gecikmeler için makul bir bekleme penceresi olabilir ama tek başına politika değildir; bu satırların çoğu hizmeti verilmiş ve parası alınmış işlemlerdir, kapatılamaz.

## D
Satırlar "onlarda var, bizde yok" sınıfına alınıp exception kuyruğuna düşer; `UNKNOWN` kayıtların akıbeti dosyayla netleştirilir, düzeltme yeni kayıtlarla ve operasyon onayıyla yapılır, iş yeniden çalıştırılabilir olduğu için aynı dosya iki kez işlense de ikinci bir düzeltme üretmez
🤖 Doğru: mutabakat sınıflandırır ve kuyruğa düşürür; nihai düzeltme insan onaylı ve idempotenttir.

## Doğru
D

## Açıklama
Mutabakatın çıktısı üç sınıftır ve buradaki 43 satır ikinci sınıfa girer: **onlarda var, bizde yok**. Bu sınıfın en yaygın kaynağı tam olarak senaryodaki durumdur — PSP çağrısı timeout almış, biz sonucu bilemediğimiz için kaydı `UNKNOWN` bırakmışız, oysa işlem karşı tarafta başarıyla tamamlanmıştır. Burada hatırlanması gereken temel kural şudur: **timeout "işlem olmadı" demek değil, "sonucu bilmiyorum" demektir**; sorgulama ucu netleştiremediğinde son sözü settlement dosyası söyler. Dolayısıyla dosyayı karşı tarafın hatası sayıp itiraz açmak yanlış yöne bakmaktır. Öte yandan dosyayı okuyup kayıtları otomatik `SETTLED` işaretlemek de kabul edilebilir değildir: para üzerinde otomatik düzeltme yapan bir iş, ilk hatalı ya da yanlışlıkla iki kez gönderilmiş dosyada gerçek bir zarar üretir. Doğru davranış, farkları **sınıflandırıp exception kuyruğuna düşürmek** ve düzeltmeyi operasyon onayıyla, mevcut kaydın üzerine yazarak değil **yeni kayıtlarla** yapmaktır. Bekleme penceresi kavramı tümüyle yanlış değildir — cut-off sınırında kalan işlemler ertesi günün dosyasında görünebilir — ama tek başına bir politika olamaz ve süre sonunda kaydı kapatmak parası alınmış müşteriyi görünmez kılar. İşin yeniden çalıştırılabilir olması ayrı bir zorunluluktur (`222`, `223`): dosya geç gelir, iş yarıda kesilir, aynı dosya tekrar işlenir; ikinci koşu ikinci bir düzeltme üretmemelidir.

# mutab-4 | İleri

## Soru
Havayolu bir uçuşu iptal etti ve iadeleri iki hafta içinde tamamladı. Buna rağmen dört ay sonra "hizmeti alamadım" gerekçesiyle 60 chargeback geldi. Ekip, işlemlerin 3DS ile doğrulanmış olmasına güvenerek itirazların otomatik kazanılacağını düşünüyor. Bu tablonun doğru okuması ve yapılması gereken nedir?

## A
3DS doğrulaması yapıldığı için sorumluluk issuer'a geçmiştir; itirazlar için ayrı bir kanıt hazırlamaya gerek yoktur, şema kararı merchant lehine çıkar
🤖 liability shift yalnızca fraud temelli itirazlarda işler; "hizmeti alamadım" gerekçesinde 3DS koruma sağlamaz.

## B
Liability shift fraud temelli itirazlar içindir ve burada işlemez; her itiraza işlem anında saklanmış kanıt zinciriyle representment yapılır, süreç kendi durum makinesi ve süre sınırlarıyla sistemde yönetilir
🤖 Doğru: koruma 3DS'ten değil, önceden biriktirilmiş kanıttan ve süresi kaçırılmayan bir itiraz sürecinden gelir.

## C
İadeler zaten yapıldığı için itirazlar cevapsız bırakılır; şema iki kez ödeme yapılmayacağını kendi kayıtlarından görür ve dosyaları kendiliğinden düşürür
🤖 itiraza süresi içinde cevap verilmezse dava otomatik kaybedilir; yapılmış iadenin kanıtını sunmak merchant'ın sorumluluğudur.

## D
Chargeback tutarları gider yazılıp süreç kapatılır; itirazla uğraşmanın operasyonel maliyeti kaybedilen tutardan yüksektir
🤖 kaybedilen her dosya chargeback oranını yükseltir ve oran eşiği aşıldığında şema izleme programı, cezalar ve kart kabul edememe riski devreye girer.

## Doğru
B

## Açıklama
3D Secure'ün sağladığı **liability shift**, doğrulanmış işlemlerde **fraud** temelli chargeback sorumluluğunu merchant'tan issuer'a taşır (`251`). Ama itiraz gerekçesi "bu işlemi ben yapmadım" değil **"hizmeti alamadım"** olduğunda bu koruma devreye girmez; iptal edilen uçuş senaryosunda 3DS'e güvenmek yanlış bir güven duygusu üretir. Bu itirazlarda merchant'ı kurtaran tek şey, işlem anında biriktirilmiş **kanıt zinciridir**: otorizasyon kodu, iade kaydı ve iade tarihi, PNR ve bilet numarası (`247`), müşteriye gönderilen iptal ve iade bildirimi. Bu veriler itiraz geldiğinde toplanamaz — itiraz aylar sonra gelir ve o an elde ne varsa odur; durum geçişlerinin append-only tutulması (`250`) tam olarak bunu besler. İtirazı cevapsız bırakmak en pahalı seçenektir: sürelerin kaçırılması davanın **otomatik kaybedilmesi** anlamına gelir ve şema, merchant'ın iade yaptığını kendiliğinden araştırmaz; kanıtı sunmak merchant'ın yükümlülüğüdür. Tutarları doğrudan gider yazıp süreci kapatmak da kısa vadeli bir hesaptır: kaybedilen her dosya **chargeback oranını** yükseltir ve oran eşiği aşıldığında şemanın izleme programı, işlem başına cezalar ve nihayetinde kart kabul edememe riski gelir — yani chargeback bir muhasebe kalemi değil, izlenen bir orandır. Sürecin sistemde kendi durumlarıyla (alındı → kanıt hazırlanıyor → representment gönderildi → sonuç) modellenmesi, altmış dosyalık bir yükün e-posta ve tablo dosyasıyla yönetilemeyeceğini bilmenin sonucudur.

# mutab-5 | İleri

## Soru
Bilet iptallerinde ceza kesilerek kısmi iade yapılıyor. 4.850,00 TL'lik bir bilette önce 2.000,00 TL, ardından 2.100,00 TL iade edildi. Sonra bir operasyon hatasıyla 1.500,00 TL'lik üçüncü bir iade isteği, aynı anda gelen bir tekrar isteğiyle birlikte sisteme ulaştı. Bu akışı doğru koruyan tasarım hangisidir?

## A
Ödeme kaydında `refunded` boolean alanı tutulur; ilk iade yapıldığında alan işaretlenir ve sonraki iade istekleri reddedilir, böylece aynı bilete ikinci kez para gitmesi kesin olarak engellenir
🤖 kısmi iade desteklenemez hale gelir; havacılıkta ceza kesintili iade normdur ve tek bir bilette birden çok kısmi iade meşrudur.

## B
İade isteği geldiğinde mevcut iade toplamı okunur, orijinal tutarı aşıp aşmadığı kontrol edilir ve uygunsa yeni iade kaydı yazılır; kontrol servis katmanında tek bir metotta toplandığı için tüm çağrı yolları aynı kuraldan geçer
🤖 oku-karar ver-yaz dizisi eşzamanlı iki istekte yarışa açıktır; iki istek de kontrolü geçip toplamı orijinalin üzerine çıkarabilir.

## C
Refund tutarı kontrolü PSP'ye bırakılır; karşı taraf orijinal işlemi bildiği için aşan iadeyi zaten reddeder, aynı kontrolü iki yerde tutmak gereksiz tekrardır ve kuralın tek sahibi olması bakımı da kolaylaştırır
🤖 iş kuralının doğrulamasını dış sisteme devretmek, kendi kayıtlarını tutarsız bırakır ve mutabakatta açıklanamayan tutar farkları üretir.

## D
Kayıt iade edilmiş birikimli tutarı taşır; sınır kontrolü tek atomik yazımda yapılır ve aşım durumunda işlem hata verir, ayrıca her iade isteği kendi idempotency anahtarını taşıdığı için tekrar gelen istek yeni bir iade üretmez
🤖 Doğru: hem kısmi iade desteklenir, hem sınır yarışa kapalı biçimde korunur, hem de tekrar eden istek yan etkisiz kalır.

## Doğru
D

## Açıklama
Havacılıkta iade nadiren tamdır: iptal cezası, servis ücreti veya kullanılmış bir bacak düşülür, dolayısıyla **kısmi refund** birinci sınıf bir gereksinimdir ve tek bir bilette birden çok kısmi iade meşrudur. Bu yüzden ödeme kaydı "iade edildi mi?" sorusuna boolean cevap veremez; **iade edilmiş birikimli tutarı** taşımak zorundadır. İkinci kural, refund toplamının orijinal işlemi **aşamaması**dır ve senaryodaki sayılar bunu somutlaştırır: 2.000,00 + 2.100,00 = 4.100,00 TL yapılmışken 1.500,00 TL'lik bir iade toplamı 5.600,00 TL'ye çıkarır ve orijinal 4.850,00 TL'yi aşar. Bu kontrolün en kritik yanı **nerede** yapıldığıdır: iade toplamını okuyup karar veren ve sonra yazan bir kod, iki istek aynı anda geldiğinde ikisini de geçirir — klasik oku-karar ver-yaz yarışı. Doğrusu, sınırı yazımın kendisinde koruyan atomik bir güncelleme kullanmak ve aşım durumunda işlemi hata ile bitirmektir. Senaryodaki tekrar isteği ise ayrı bir savunma katmanı gerektirir: her iade isteği kendi **idempotency anahtarını** taşımalı ve aynı anahtarla gelen ikinci istek yeni bir iade üretmeden ilk sonucu döndürmelidir (`253`). Kontrolü tamamen PSP'ye devretmek de doğru değildir: kendi kayıtların kısmi iade toplamını bilmiyorsa mutabakatta "tutar farklı" sınıfına düşen satırların kaynağını hiçbir zaman açıklayamazsın.

# mutab-6 | Uzman

## Soru
Uçuş iptal edildi, 200 yolcuya iade yapılacak. Yazılan job iadeleri döngüyle PSP'ye gönderiyor. İlk denemede 137. kayıttan sonra süreç düşüyor; yeniden başlatıldığında PSP'nin hız sınırına takılıp bir kısmı HTTP 429 alıyor ve gün sonunda muhasebe, o günün settlement toplamının beklenenden 1.240.000 TL düşük geldiğini bildiriyor. Bu işi baştan doğru kuran tasarım hangisidir?

## A
Job tek bir veritabanı işlemi içinde 200 iadeyi birden yazıp sonunda hepsini PSP'ye gönderir; böylece ya hepsi olur ya hiçbiri olmaz, kısmi durum hiç oluşmaz ve yeniden başlatma sorunu tanım gereği ortadan kalkar. Herhangi bir çağrı hata verirse işlem geri alınır, tablo baştaki hâline döner ve iş ertesi gece sıfırdan koşar
🤖 dış sistem çağrıları veritabanı işleminin atomikliğine dahil edilemez; uzun süren tek bir işlem kilit tutar ve PSP'ye gönderilmiş bir iade geri alınamaz.

## B
İadeler yalnızca başarısız olanlar için tekrar çalıştırılır ve tekrar listesi her koşudan sonra elle güncellenir; böylece hangi kaydın işlendiği operasyon tarafından denetlenmiş olur. Liste paylaşılan bir tablo dosyasında tutulur, gece koşusundan sonra ekip satırları gözden geçirip bir sonraki koşuya girecek biletleri işaretler
🤖 insan eliyle tutulan bir işlenmişlik listesi hata ve gecikme üretir; tekillik garantisi kayıt düzeyinde ve veritabanında olmalıdır.

## C
Uçuş iptalinde bilet başına tek ve tam iade yapıldığı için her iade `(ticket_id)` üzerinde benzersizlik kısıtı olan bir kayıtla korunur (kısmi iadeye açık bir üründe anahtar iade talebini de içerirdi), job yeniden başlatılabilir biçimde kurgulanır, gönderim PSP'nin kabul ettiği hızda yapılıp HTTP 429 alındığında geri çekilir, kalıcı başarısızlıklar exception kuyruğuna düşer ve toplu iade muhasebede ayrı bir mutabakat kalemi olarak izlenir
🤖 Doğru: idempotency veritabanı seviyesinde garanti edilir, kısmi ilerleme korunur, hız kontrol edilir ve mali etki mutabakatta açıklanabilir kalır.

## D
Job her başlatıldığında iade tablosu temizlenip 200 kayıt sıfırdan işlenir; böylece yarım kalmış kayıtlardan doğan tutarsızlık ortadan kalkar ve süreç deterministik olur. Aynı bilete ikinci kez iade gitmesi riski için PSP'nin kendi tarafında tekrarı eleyeceği varsayılır, dolayısıyla bizde ayrı bir tekillik kontrolü tutulmaz
🤖 gerçekleşmiş finansal işlemlerin kaydı silinemez ve zaten iade edilmiş yolculara ikinci kez iade gönderilir; bu, çifte iadenin en doğrudan yoludur.

## Doğru
C

## Açıklama
Toplu iade, ödeme sistemlerindeki batch ve idempotency problemlerinin aynı anda göründüğü yerdir ve doğru tasarımın beş bileşeni vardır. **Tekillik veritabanında garanti edilir:** her iade `(ticket_id)` üzerinde benzersizlik kısıtı taşıyan bir kayıtla korunur; ikinci deneme uygulama kodundaki bir kontrole değil, kısıta çarpar — 137. kayıttan sonra düşen job yeniden başlatıldığında ilk 137'yi tekrar işlemez (`253`). **Job yeniden başlatılabilir kurgulanır**, kısmi ilerleme korunur ve bozuk bir kayıt tüm işi düşürmez (`222`, `223`). **Gönderim hızı kontrol edilir:** 200 isteği aynı anda göndermek karşı tarafın sınırına çarpar; HTTP 429 alındığında `Retry-After` değerine uyulup geri çekilmelidir, aksi halde redler gerçek hata gibi görünüp kuyruğu kirletir (`153`). **Kalıcı başarısızlıklar kaybolmaz**, DLQ veya exception kuyruğuna düşer ve tek tek ele alınır — sessizce yutulan bir başarısız iade, birkaç hafta sonra bir chargeback olarak geri döner. Son olarak **muhasebe tarafı**: senaryodaki 1.240.000 TL'lik sapma bir hata değil, toplu iadenin settlement dosyasındaki normal yansımasıdır; iadeler ayrı bir mutabakat kalemi olarak izlenirse "bugün eksik geldi" alarmının cevabı hazır olur. Tüm iadeleri tek bir veritabanı işlemine sokmak eleyici bir cevaptır: dış sistem çağrıları işlem atomikliğine dahil edilemez ve gönderilmiş bir iade geri alınamaz. İşlenmişlik listesini elle tutmak insan hızına ve insan hatasına bağımlılık üretir. Tabloyu her koşuda temizlemek ise en zararlısıdır — finansal kayıt silinmez ve iadesi yapılmış yolculara ikinci kez para gönderilir.
