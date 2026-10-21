---
slug: ihracat-finansmani-ve-taahhut-takibi
baslik: "İhracat Finansmanı ve Taahhüt Takibi: İGE Kefaleti, Sevk Öncesi/Sonrası Kredi ve Mahsup Döngüsü"
kademe: Uzman
sira: 245
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir müteahhide "bu binayı iki yıl içinde bitirirsen vergiden muaf olacaksın" dendiğini düşün. Müteahhit parayı bugün alır, binayı sonra teslim eder. Muafiyeti hak etmesi için birinin dört iş yapması gerekir. Sayacı başlatmak, teslim edilen her katı kayda geçirmek, "ne kadarı bitti, ne kadarı kaldı" sorusunu her an cevaplamak ve iki yılın sonunda "bitti / bitmedi" kararını vermek.

Bu defterde iki tehlike vardır. Aynı katı iki kez teslim saydırırsa muafiyet haksız yere korunmuş olur. Süre dolduğunda bina bitmemişse muafiyet geri alınır ve müteahhide bir borç doğar. İhracat finansmanı yazılımı tam olarak bu sayacı, bu defteri ve bu kararı tutan sistemdir.

Bu konu, hedef bankanın imzası sayılabilecek alanı sistem gözüyle anlatır: **ihracat kredileri, İGE kefaleti ve ihracat taahhüdünün takibi**. Buradaki teknik özün adı yenidir ama mekaniği tanıdıktır. Beş bilinen parça vardır: durum makinesi, çoka-çok teminat ilişkisi, **tekrar edilse de sonucu değişmeyen (idempotent)** kayıt, dış kurum entegrasyonu ve gün sonu toplu işi.

### İhracat finansmanının iki zamanı: sevk öncesi ve sevk sonrası

Bankanın ihracat döviz kredisi tanımı, finansman ihtiyacını **"sevk öncesi veya sevk sonrası"** diye ikiye ayırır. Bu ayrım pazarlama dili değil, sistemin veri modelini belirleyen bir ayrımdır.

- **Sevk öncesi finansman.** Mal henüz üretilmemiş ya da gönderilmemiştir. Para hammadde alımına, işçiliğe ve üretim hazırlığına gider. Kredinin geri ödeme kaynağı **henüz doğmamış** bir ihracat bedelidir. Yani bankanın elinde bir alacak yoktur; elinde müşterinin **taahhüdü** vardır. Risk buradadır: ihracat hiç gerçekleşmeyebilir, gecikebilir ya da tutarı beklenenin altında kalabilir. Sistem tarafında karşılığı şudur: kredi kaydı bir **taahhüt kaydına** bağlanır, süre sayacı başlar ve gerçekleşmenin süre dolmadan belgelenmesi izlenir.
- **Sevk sonrası finansman.** Mal fiilen çıkmıştır. Ortada sevk ve gümrük belgeleri vardır, alıcıdan bedel beklenmektedir. Kredi artık **var olan bir alacağa** dayanır. Risk profili farklıdır: "ihracat olacak mı" sorusu yerine "alıcı ödeyecek mi, vadesinde gelecek mi" sorusu vardır. Sistem tarafında kredi kaydı bir **belgeye** bağlanır, doğrulama kuralları belge alanları üzerinde çalışır ve izlenen şey tahsilattır.

Aynı müşteride bu iki zaman art arda gelebilir. Sevk öncesi kullandırılan kredi, sevk gerçekleştiğinde kapanabilir ya da sevk sonrası bir finansmana dönüşebilir. Sistemde bu, **iki kopuk kayıt değil, bağlantılı bir durum geçişi** olarak modellenir. Aksi hâlde aynı ihracatın iki kez finanse edildiği görülmez ve taahhüt tarafında mükerrer mahsup kapısı açılır.

Bu iki zamanın ortak paydası, ürünün **ihracat taahhütlü** olmasıdır. Banka tanımına göre İGE teminatlı krediler doğrudan "ihracat taahhütlü krediler" olarak tarif edilir. İhracat döviz kredilerinde ise şu denir: "24 ay içerisinde ihracat taahhüdünüzü yerine getirmeniz durumunda **vergi, resim ve harç istisnasından** yararlanabilirsiniz." Yani finansmanın fiyatı kadar, **taahhüdün tutulup tutulmadığı** da müşterinin cebini etkiler. Bunu takip eden şey de yazılımdır.

```svg
<svg viewBox="0 0 660 292" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sevk öncesi ve sevk sonrası ihracat finansmanı ile 24 aylık taahhüt süresi">
  <defs>
    <marker id="ihrfin-sevk-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İhracat finansmanının iki zamanı</text>
  <rect x="26" y="36" width="286" height="104" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="58" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Sevk öncesi finansman</text>
  <text x="169" y="78" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">mal henüz gitmedi · üretim ve hazırlık</text>
  <text x="169" y="95" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dayanak: TAAHHÜT (henüz doğmamış alacak)</text>
  <text x="169" y="112" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">risk: ihracat hiç gerçekleşmeyebilir</text>
  <text x="169" y="129" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sistem: taahhüt kaydı + süre sayacı</text>
  <rect x="348" y="36" width="286" height="104" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="58" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Sevk sonrası finansman</text>
  <text x="491" y="78" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">mal gitti · bedel bekleniyor</text>
  <text x="491" y="95" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dayanak: BELGE (gerçekleşmiş alacak)</text>
  <text x="491" y="112" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">risk: alıcı vadesinde ödemeyebilir</text>
  <text x="491" y="129" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sistem: belge eşleştirme + tahsil izleme</text>
  <line x1="26" y1="170" x2="634" y2="170" stroke="#5D7789" stroke-width="1.4" marker-end="url(#ihrfin-sevk-arw)"/>
  <circle cx="90" cy="170" r="6" fill="#186399"/>
  <circle cx="330" cy="170" r="6" fill="#B07D20"/>
  <circle cx="560" cy="170" r="6" fill="#25804C"/>
  <text x="90" y="192" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kullandırım</text>
  <text x="90" y="207" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">taahhüt kaydı açılır</text>
  <text x="330" y="192" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Sevk</text>
  <text x="330" y="207" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gümrük beyannamesi doğar</text>
  <text x="560" y="192" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Mahsup</text>
  <text x="560" y="207" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">taahhüt kapatılır</text>
  <path d="M90 226 L90 236 L560 236 L560 226" fill="none" stroke="#B07D20" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="325" y="254" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">24 ay taahhüt süresi</text>
  <text x="330" y="274" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Süre içinde taahhüt yerine getirilirse vergi, resim ve harç istisnasından yararlanılır</text>
  <text x="330" y="288" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sevk öncesi kredi sevk sonrasına dönüşebilir: sistemde kopuk kayıt değil, bağlı durum geçişi</text>
</svg>
```

### İGE kefaleti: teminat yapısında üçüncü taraf

Bankanın İGE teminatlı kredi tanımı şudur: **İhracatı Geliştirme A.Ş. (İGE)'nin sağlamış olduğu kefalet desteği** ile, ihracatçı ya da ihracat potansiyeli bulunan, döviz kazandırıcı hizmet faaliyetlerinde bulunan **KOBİ ve KOBİ dışı** firmalara yönelik, **24 ay vadeye kadar** sunulan **ihracat taahhütlü** krediler.

Bu cümlenin sistem tarafındaki karşılığı tek bir yapısal farktır: kredinin teminat yapısında **üçüncü bir taraf** vardır. Alışılmış teminat, nakit blokaj, ipotek ya da müşterinin kendi kefaletidir. Farkı şudur: burada teminatı sağlayan kişi, işlemin tarafı değildir. Bunun veri modeline yansımaları şunlardır:

- **Teminat türü ayrı bir kavramdır.** Teminat kaydında dört bilgi bulunur: tür (üçüncü taraf kefaleti), kefil kimliği, kefaletin kapsamı ve **kefalet oranı**. Kefalet oranı, kredinin ne kadarlık kısmının kefaletle örtüldüğüdür. Oranı koda gömmek yanlıştır. Genel uygulamada oran ve kapsam **tarih geçerlilikli parametre** olarak tutulur. Sebebi şudur: program koşulları zaman içinde değişebilir ve geçmiş krediler kendi dönemlerinin koşullarıyla değerlendirilmelidir.
- **Teminat ile kredi arasındaki ilişki çoka-çoktur.** Bir teminat birden fazla krediyi kısmen kapatabilir. Bir kredi de birden fazla teminatla kapatılabilir. Bu yüzden aralarında bir bağlantı kaydı olur ve bağlantının üzerinde **bağlanan tutar** taşınır. Teminatın toplam bağlı tutarı, teminatın kapsamını aşamaz. Bu kontrol, eşzamanlı iki tahsis isteğinde yarışa girer. Dolayısıyla yalnız uygulama içi kontrolle değil, kilit veya sürüm damgasıyla korunmalıdır (`225`).
- **Serbest ve bağlı kısım ayrı izlenir.** "Teminat var mı" sorusu yetmez. Şu soru da cevaplanabilmelidir: "Teminatın ne kadarı halihazırda başka bir kredinin arkasında duruyor?" Bu sorunun cevabı tek bir toplam alanında saklanırsa, o alan sıcak kayıt hâline gelir.
- **Temerrüt akışı bir durum makinesidir.** Borçlu ödemezse sıra şudur: gecikme → temerrüt → kefile **tazmin talebi** → talebin değerlendirilmesi → ödeme veya ret. Bu akışın her adımının belge şartı, süresi ve yetkilisi vardır. Her geçiş **denetim izine** yazılır. Genel uygulamada kefil ödeme yaptığında bankanın alacağı kefile geçer. Sistemde bu, alacağın silinmesi değil **alacaklının değişmesi** olarak modellenir.

Mülakatta en sık düşülen tuzak, "kefalet var, o hâlde risk yok" demektir. Kefalet riski **azaltır**, sıfırlamaz. Üç sebebi vardır. Birincisi, kapsam dışı kalma ihtimali vardır: taahhüdün ihlali, belge eksikliği, süre kaçırma. İkincisi, kefaletle örtülmeyen kısım banka riskidir. Üçüncüsü, tazmin süreci kendi başına zaman alan bir süreçtir. Doğru cevap şudur: "Kefalet, teminat yapısında ayrı bir boyuttur; risk hesabına girer ama limit kullanımını kendiliğinden ortadan kaldırmaz."

### Taahhüt takibi: kayıt, mahsup, kapatma

Bu konunun kalbi burasıdır. Ürün tanımı şunu der: "24 ay içerisinde ihracat taahhüdünüzü yerine getirmeniz durumunda vergi, resim ve harç istisnasından yararlanabilirsiniz." Yazılıma verilen görev de şudur: **taahhüdün yerine getirilip getirilmediğini kanıtlanabilir biçimde takip et.**

**1. Taahhüt kaydı.** Kredi kullandırıldığında bir taahhüt kaydı doğar. Asgari alanlar şunlardır: taahhüt tutarı, **para birimi**, başlangıç tarihi, süre (bu üründe 24 ay), taahhüt türü, bağlı kredi(ler) ve durum. Taahhüt türü üç değerden biridir: ihracat, ihracat sayılan satış ya da döviz kazandırıcı hizmet. Taahhüt ile kredi arasındaki ilişki de her zaman birebir değildir; birden çok kullandırım tek bir taahhüde bağlanabilir.

**2. Mahsup.** Gerçekleşen ihracat, belgelerle taahhüde **mahsup edilir**. Belge derken gümrük beyannamesini ve ilgili sevk/ihracat belgelerini kastediyoruz. Her mahsup ayrı bir kayıttır. Şunları taşır: belgenin benzersiz anahtarı, belge tutarı ve para birimi, mahsup edilen tutar, mahsup tarihi ve **kullanılan kur ile kur tarihi**.

**3. Kısmi mahsup ve kalan.** Bir belge taahhüdün tamamını değil bir kısmını kapatabilir. Bir taahhüt de birçok belgeyle kapanabilir. Bu yüzden `kalan taahhüt = taahhüt tutarı − mahsupların toplamı` her an hesaplanabilir olmalıdır. Burada iki **bozulmaz kural (invariant)** vardır. Birincisi: kalan hiçbir zaman negatif olamaz, yani mahsupların toplamı taahhüdü aşamaz. İkincisi: bir belgenin farklı taahhütlere dağıtılan kullanımlarının toplamı, belgenin kendi tutarını aşamaz. İkinci kural gözden kaçarsa tek bir beyanname birkaç taahhüdü birden kapatır ve kimse fark etmez.

**4. Süre sayacı ve eşik uyarıları.** Başlangıç tarihinden itibaren kalan gün hesaplanır. Belirli eşiklerde, örneğin son X günde, müşteri temsilcisine ve müşteriye uyarı üretilir. Eşikler koda gömülmez, parametreden okunur; iş birimi eşikleri değiştirebilmelidir.

**5. Taahhüt kapatma.** Kalan taahhüt sıfırlandığında ve belgeler tamamlandığında taahhüt kapatılır, istisna korunur. **6. Kapatılamama.** Süre dolduğunda kalan taahhüt hâlâ varsa taahhüt kapatılamaz; istisna geri alınır ve müşteri için bir yükümlülük doğar.

Son iki adımın ortak özelliği kritiktir: **her ikisi de geri alınamaz mali sonuç doğurur.** Bu yüzden mühendislik duruşu şudur:

- Kapatma ve geri alma **otomatik olarak kendiliğinden kesinleşmez**. Sistem koşulları hesaplar ve gerekçesiyle birlikte iş listesine düşürür. Kesinleşme **yetkili onayıyla** olur. Gün sonu işinin kendi başına "istisnayı geri aldım" demesi, kontrolsüz bir mali karar üretmektir.
- Her iki işlem de **tam denetim izi** bırakır: hangi kullanıcı, hangi zaman, hangi belgelerle, hangi kural sürümüyle. Sonradan "bu taahhüt neden kapandı" sorusu tek bir ekrandan cevaplanabilmelidir.
- Hata olursa kayıt **silinmez**. Düzeltme, karşı kayıt veya kayıtlı bir düzeltme işlemiyle yapılır (`241`). "Yanlış kapattık, kaydı temizledik" cevabı bankacılıkta eleyicidir.
- Bu alanın mevzuat tarafı vardır. Mülakatta madde numarası, oran veya tarih söylemeye çalışmak hem risklidir hem gereksizdir. Doğru duruş şudur: **"Kambiyo ve ihracat mevzuatına bağlı kısıtlar vardır; bu kurallar tarih geçerlilikli parametre olarak tutulur, iş biriminden yazılı teyit alınır ve kural sürümü kayda geçer."** Bu duruşun kendisi doğru cevaptır.

```svg
<svg viewBox="0 0 660 356" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Taahhüt kaydı, mahsup, kalan taahhüt ve kapatma veya istisnanın geri alınması döngüsü">
  <defs>
    <marker id="ihrfin-tah-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="ihrfin-tah-loop" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B07D20"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Taahhüt döngüsü: kayıt → mahsup → kalan → kapanış</text>
  <rect x="230" y="40" width="200" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Taahhüt kaydı</text>
  <text x="330" y="78" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tutar · para birimi · başlangıç · 24 ay</text>
  <rect x="24" y="110" width="180" height="50" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="114" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Belge</text>
  <text x="114" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gümrük beyannamesi vb.</text>
  <rect x="230" y="110" width="200" height="50" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="330" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Mahsup kaydı</text>
  <text x="330" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kısmi olabilir · kur ve kur tarihi saklanır</text>
  <rect x="456" y="110" width="180" height="50" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="546" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Kalan taahhüt</text>
  <text x="546" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">negatif olamaz</text>
  <path d="M330 90 L330 108" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-tah-arw)"/>
  <path d="M204 135 L228 135" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-tah-arw)"/>
  <path d="M430 135 L454 135" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-tah-arw)"/>
  <path d="M480 160 L480 178 L330 178 L330 162" fill="none" stroke="#B07D20" stroke-width="1.3" stroke-dasharray="4 3" marker-end="url(#ihrfin-tah-loop)"/>
  <text x="404" y="173" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">kalan &gt; 0 → yeni belgeyle mahsup sürer</text>
  <path d="M600 160 L600 218 L434 218" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-tah-arw)"/>
  <rect x="230" y="196" width="200" height="44" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="330" y="214" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Süre sonu değerlendirmesi</text>
  <text x="330" y="231" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sistem hesaplar, yetkili onaylar</text>
  <path d="M330 240 L330 252 L170 252 L170 260" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#ihrfin-tah-arw)"/>
  <path d="M330 240 L330 252 L490 252 L490 260" fill="none" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#ihrfin-tah-arw)"/>
  <rect x="24" y="262" width="292" height="62" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="170" y="282" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Taahhüt kapatma</text>
  <text x="170" y="299" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kalan = 0 ve süre içinde</text>
  <text x="170" y="316" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">vergi, resim ve harç istisnası korunur</text>
  <rect x="344" y="262" width="292" height="62" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="490" y="282" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Taahhüt kapatılamadı</text>
  <text x="490" y="299" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">süre doldu, kalan &gt; 0</text>
  <text x="490" y="316" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istisna geri alınır, yükümlülük doğar</text>
  <text x="330" y="344" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Her iki sonuç da geri alınamaz mali sonuçtur: onay adımı + denetim izi zorunlu, düzeltme silmeyle değil ters kayıtla</text>
</svg>
```

### Mahsupta tekrar güvenliği (idempotency): aynı beyanname iki kez sayılmasın

Mahsup, sistemin en hassas yazma işlemidir. Aynı gümrük beyannamesi iki kez mahsup edilirse taahhüt olduğundan hızlı kapanır. **Haksız yere kapanmış bir taahhüt, haksız yere korunmuş bir istisna demektir.** Bu hata sonradan tespit edildiğinde geri sarılması pahalıdır ve denetimde açıklanması zordur. Mekaniği, `130` konusundaki idempotent tüketici deseninin birebir bankacılık karşılığıdır.

Doğru çözüm katmanlıdır:

1. **Benzersiz iş anahtarı belirlenir.** Belge numarası tek başına yeterli olmayabilir. Genel uygulamada anahtar, belgeyi dünyada tek gösteren bileşenlerden kurulur. Örneğin: belge numarası + düzenleyen idare/kurum kodu + tescil tarihi. Anahtarın bileşimi bir iş kararıdır ve tek bir yerde tanımlanır.
2. **Kısıt veritabanında olur.** Uygulama içinde "önce sorgula, yoksa yaz" yapmak yetmez. İki istek aynı anda geldiğinde ikisi de "yok" görür ve ikisi de yazar. Koruma, `(taahhüt, belge anahtarı)` üzerinde bir **benzersizlik kısıtıdır**; ikinci yazma veritabanı tarafından reddedilir. Uygulama bu ihlali kullanıcıya hata olarak fırlatmaz. Onu **"zaten mahsup edilmiş"** olarak ele alır. İstek tekrar edildiğinde sonuç aynı kalır; tekrar güvenliği (idempotency) tam olarak budur.
3. **Belge tarafında da toplam kontrolü olur.** İş kuralı, bir belgenin birden fazla taahhüde bölünerek kullanılmasına izin verebilir. O zaman `(taahhüt, belge)` kısıtı tek başına yetmez: aynı belge farklı taahhütlerde ayrı satırlar üretir ve kısıt hiç ihlal edilmez. Bu durumda belgenin **toplam kullanılan tutarı** ayrıca izlenir ve belge tutarını aşamaz.
4. **Girdi tarafı da tekilleştirilir.** Belgeler elle girilebildiği gibi toplu dosyayla da gelebilir. Aynı dosyanın iki kez yüklenmesi tipik bir olaydır. Bu yüzden dosya seviyesinde de bir yükleme anahtarı tutulur; böylece tekrar yükleme yeni kayıt üretmez.

Buradaki genel ilke şudur: **tekrarı yasaklamaya çalışma, tekrarı zararsız hâle getir.** Kullanıcı iki kez tıklar, ağ zaman aşımına düşer, toplu iş yeniden başlatılır, mesaj iki kez teslim edilir. Bunların hepsi olağandır. Sistem, ikinci gelişte yeni bir mali sonuç üretmeyecek biçimde kurulur.

### Prefinansman ve üç taraflı protokol

Bankanın bir başka ürünü şöyle tanımlanır: **"T.C. Ticaret Bakanlığı, Türk Ticaret Bankası A.Ş. ve İGE A.Ş. arasında imzalanan protokol çerçevesinde; Proje/Program/Statü bazlı destekler prefinansman modeli uygulamamız kapsamına alınmıştır."** Prefinansman burada şu anlama gelir: müşterinin hak ettiği bir destek ileride ödenecektir; banka o desteği beklemeden finansmanı bugün sağlar.

Yazılım açısından bu, **dış kurumla entegrasyon** problemidir ve zorluğu üç başlıkta toplanır.

**Doğrulama.** Desteğin gerçekten var olduğu, tutarı, hak sahibi ve durumu karşı taraftan doğrulanır. Buradaki temel kural, bankanın kendi kaydını tek gerçek kabul etmemesidir: destek kaydının **kaynağı dışarıdadır**, bizim tarafımızdaki kayıt bir kopyadır ve kopyanın ne zaman, hangi cevaba dayanarak alındığı saklanır.

**Kesinti ve zaman aşımı.** Dış sistem yavaşlayabilir ya da yanıt vermeyebilir. Zaman aşımı, sınırlı **yeniden deneme (retry)** ve devre kesici zorunludur (`101`, `221`). Ama asıl tehlikeli durum başarısızlık değildir; **sonucu bilinmeyen istektir**. Yani şu iki soru cevapsız kalır: istek karşı tarafa ulaştı mı, ulaştıysa işlendi mi? Bu durumda kör yeniden deneme mükerrer kayıt üretir.

Doğru davranış üç adımlıdır. (1) Her istek **benzersiz istek anahtarıyla** gönderilir. (2) Karşı tarafta bir **durum sorgulama** çağrısı varsa, önce onunla gerçek durum netleştirilir. (3) Durum netleşmiyorsa işlem "belirsiz" durumunda bekletilir; otomatik telafi işletilmez ve iş **mutabakatla** çözülür. Otomatik telafi (`126`) ancak durum kesinleştikten sonra tetiklenir. Belirsiz durumda telafi çalıştırmak, aslında başarılı olmuş bir işlemi geri almak anlamına gelebilir.

**Mutabakat.** Belirli aralıklarla bizim kayıtlarımız ile karşı kurumun kayıtları karşılaştırılır ve **fark listesi** üretilir. Fark listesi bir hata raporu değil, normal bir işletme çıktısıdır; her farkın bir sahibi, bir açıklaması ve bir kapanış yolu olur. Mutabakat olmadan hiçbir dış entegrasyon güvenilir sayılmaz.

**Kısmi başarı.** Destek tutarının bir kısmı doğrulanıp kalanı reddedilebilir. Bu, "hepsi ya da hiçbiri" varsayımını bozar. Sistem kısmi sonucu temsil edebilmelidir. Doğrulanan tutar ile talep edilen tutar ayrı alanlardır ve fark gerekçesiyle saklanır. Finansman zaten kullandırılmışsa aradaki fark için telafi akışı işler (`126`). Bankacılıkta telafi silme değil **ters kayıttır** (`241`).

**Proje/Program/Statü ayrımının veri modelindeki yeri.** İlk akla gelen çözüm, üç destek türü için üç ayrı tablo ve üç ayrı kod yolu yazmaktır. Bu genellikle yanlıştır. Çünkü üçü de aynı yaşam döngüsünü paylaşır: talep → doğrulama → prefinansman kullandırımı → destek tahsili/kapanış. Sonuçta ortak akışın üç kopyası oluşur. Ortak bir kural değiştiğinde üç yer birden değiştirilmek zorunda kalınır ve biri mutlaka unutulur.

Doğru yaklaşım, tek bir destek kaydı ve **destek türü** ayrımıdır. Türe özgü zorunlu alanlar ve doğrulama kuralları, türden okunan bir şemayla uygulanır. Böylece yeni bir destek türü eklemek yeni bir kod yolu değil, yeni bir tanım olur.

### Döviz boyutu: hangi kurla karşılaştırıyoruz?

İhracat döviz kredileri, banka tanımında **yabancı para cinsinden** kullanılan kredilerdir. Taahhüt de dövizdir. Gerçekleşen ihracat da dövizdir ama her zaman **aynı** döviz olmayabilir. Buradan sinsi bir soru doğar: taahhüt tutarı ile gerçekleşen ihracat **hangi kurla** karşılaştırılacak?

Seçenekler gerçektir ve hepsi savunulabilir görünür. Taahhüdün açıldığı tarihteki kur olabilir. Beyannamenin tescil tarihindeki kur olabilir. Mahsup işleminin yapıldığı andaki kur olabilir. Para birimleri aynıysa hiç çevirmeden doğrudan karşılaştırma da olabilir. Mühendisin yapması gereken, bunlardan birini kendi kanaatiyle seçmek **değildir**. Doğru duruş şudur:

- Kur kuralı bir **iş kuralıdır**. İş biriminden yazılı olarak alınır ve mevzuata bağlı olduğu kabul edilir.
- Kural **tek bir yerde** tanımlanır. Aynı çevrim mantığı ekranda, servis katmanında ve gün sonu işinde ayrı ayrı yazılırsa üç farklı sonuç üretir. O zaman hangisinin doğru olduğu tartışmaya açılır.
- Kural **tarih geçerlilikli parametredir**. Değiştiğinde geçmiş kayıtlar yeniden hesaplanmaz; kendi dönemlerinin kuralıyla kalır.
- Her mahsup kaydında **kullanılan kur ve kur tarihi saklanır.** En sık yapılan hata, çevrimi hesap anında canlı kurdan yapmaktır. O zaman aynı hesap iki farklı zamanda çalıştırıldığında iki farklı sonuç verir, rapor mutabık olmaz ve "dün neden farklı görünüyordu" sorusunun cevabı olmaz. Kur, hesaplanan değil **kaydedilen** bir veridir.
- Tutarlar ondalık duyarlı türle tutulur ve **para birimi ayrı bir alandır** (`226`). Tutarı para biriminden ayrı taşıyan bir alan, er ya da geç yanlış para birimiyle toplanır.

Kredinin kendisinin dönem sonunda değerlenmesi ayrı bir konudur. Kur farkının gelir/gider olarak yansıtılması da öyle; ikisi `243`'te ele alınır. Burada önemli olan iki şeydir: **taahhüt karşılaştırmasının kendi kur kuralına sahip olması** ve bu kuralın kayıtlı olması.

### Limit ve grup riski

Kurumsal bankacılıkta limit, tek bir firmanın limiti değildir. Birbiriyle ilişkili firmalar bir **risk grubu** altında toplanır. İlişki ortaklık, yönetim veya kefalet üzerinden kurulmuş olabilir. Gerekçesi doğrudan risk yönetimidir: aynı ekonomik riskin birkaç ayrı tüzel kişiliğe bölünerek küçük görünmesi engellenir.

Sistemdeki karşılığı bir **hiyerarşidir**: risk grubu → firma → ürün → tek tek işlemler. Ürün seviyesi nakdi, gayrinakdi ya da ihracat kredisi olabilir. Bu hiyerarşinin mühendislik sonuçları şunlardır:

- **Kullanım aşağıdan yukarı toplanır.** Tek bir kullandırım üç limiti birden etkiler: bağlı olduğu ürün limitini, firmanın limitini ve grubun limitini. Alt seviyelerdeki kullanımların toplamı, üst seviyedeki limiti aşamaz. Tahsislerin toplamının üst limiti aşıp aşamayacağı ise ayrı bir iş kuralıdır; varsayılmaz, sorulur.
- **Nakdi ve gayrinakdi risk aynı ağaçta toplanır.** Teminat mektubu gibi gayrinakdi bir ürün bugün nakit çıkışı üretmez. Ama tazmin hâlinde nakde döner; bu yüzden limit ağacının dışında bırakılamaz (`244`).
- **Kilit sırası her yerde aynı olmalıdır.** Grup → firma → ürün sırası bir yerde tersine çevrilirse iki eşzamanlı işlem birbirini bekler ve **karşılıklı kilitlenme (deadlock)** doğar. Eşzamanlı limit düşümündeki yarış koşulunun mekaniği ve iyimser/kötümser kilit tercihi `225` konusundadır.
- **Teminat ile limit ayrı boyutlardır.** İGE kefaleti riski azaltan bir teminattır; limit kullanımını kendiliğinden ortadan kaldırmaz. Bu ikisini karıştırmak, mülakatta bilgi eksikliği olarak okunur.
- **Ağaç sorguları sıcak noktadır.** Bir grup altındaki tüm kullanımların toplamını her istekte baştan hesaplamak pahalıdır. Toplamların tutulduğu satırlar ise klasik sıcak kayıttır. Çözüm yönü şudur: işlemi kısa tut ve toplamı gerektiğinde parçalayarak çekişmeyi dağıt.

```svg
<svg viewBox="0 0 660 318" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Risk grubu, firma, ürün ve işlem seviyelerinden oluşan limit hiyerarşisi">
  <defs>
    <marker id="ihrfin-lim-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Limit hiyerarşisi: grup → firma → ürün → işlem</text>
  <rect x="220" y="38" width="220" height="42" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="330" y="56" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Risk grubu limiti</text>
  <text x="330" y="72" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ilişkili firmalar tek çatıda</text>
  <rect x="110" y="102" width="150" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="185" y="120" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Firma A</text>
  <text x="185" y="135" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">firma limiti</text>
  <rect x="400" y="102" width="150" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="475" y="120" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Firma B</text>
  <text x="475" y="135" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">firma limiti</text>
  <path d="M330 80 L330 92 L185 92 L185 100" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M330 80 L330 92 L475 92 L475 100" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <rect x="24" y="164" width="150" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="99" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Nakdi</text>
  <text x="99" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ihracat döviz kredisi</text>
  <rect x="190" y="164" width="150" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="265" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gayrinakdi</text>
  <text x="265" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">teminat mektubu</text>
  <rect x="356" y="164" width="140" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="426" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Nakdi</text>
  <text x="426" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">işletme sermayesi</text>
  <rect x="512" y="164" width="124" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="574" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gayrinakdi</text>
  <text x="574" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">aval / garanti</text>
  <path d="M185 142 L185 154 L99 154 L99 162" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M185 142 L185 154 L265 154 L265 162" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M475 142 L475 154 L426 154 L426 162" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M475 142 L475 154 L574 154 L574 162" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M99 208 L99 220" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M265 208 L265 220" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M426 208 L426 220" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <path d="M574 208 L574 220" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ihrfin-lim-arw)"/>
  <rect x="24" y="222" width="612" height="34" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="330" y="243" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">İşlem: tek kullandırım · tek mektup — kullanım yukarıya doğru toplanır</text>
  <rect x="24" y="266" width="612" height="30" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="330" y="286" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Teminat ve kefalet (İGE) ayrı bir boyuttur: riski azaltır, limit kullanımını kendiliğinden ortadan kaldırmaz</text>
  <text x="330" y="312" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Kilit sırası her kod yolunda aynı olmalı: grup → firma → ürün; ters sıra kilitlenme (deadlock) üretir</text>
</svg>
```

### Gün sonu işleri

İhracat finansmanının kendine özgü gün sonu yükü vardır ve genel toplu iş disipliniyle (`222`, `223`) yürütülür:

- **Taahhüt süresi kontrolü.** Her açık taahhüt için kalan gün hesaplanır, eşik aşıldıysa uyarı üretilir, süresi dolanlar işaretlenir.
- **Faiz ve komisyon tahakkuku.** Kredinin o güne düşen gelir kayıtları üretilir.
- **Kur değerleme.** Döviz kredileri ve döviz taahhütleri dönem kurlarıyla değerlenir (`243`).
- **Dış kurum mutabakatı.** Prefinansman tarafında karşı kurumla kayıt karşılaştırması ve fark listesi üretimi.

Bu işlerin ortak riski, **iki kez çalışmalarıdır**. İş yarıda kalıp elle yeniden başlatılabilir; zamanlayıcı iki kopyayı birden tetikleyebilir. Sonucu en hafifiyle mükerrer uyarı bildirimi, en ağırıyla mükerrer mali kayıttır.

Korunma üç parçalıdır. Birincisi, iş çalıştırması **iş günü + iş türü** anahtarıyla tekilleştirilir. İkincisi, üretilen kayıtlarda **benzersizlik kısıtı** bulunur. Üçüncüsü, iş **yeniden başlatılabilir** kurgulanır: işlenmişleri tekrar işlemeden kaldığı yerden devam edebilir.

Bu konuya özgü ek bir kural daha vardır: **gün sonu işi mali kararı kendisi vermez.** Süresi dolan taahhüdü işaretler ve gerekçesiyle iş listesine düşürür. İstisnanın geri alınması gibi geri alınamaz sonuçlar yetkili onayıyla kesinleşir. Toplu işe böyle bir kararı otomatik verdirmek, bir hata durumunda binlerce müşteriye aynı anda yanlış yükümlülük yansıtmak demektir.

### Mülakatta ne sorulur / tuzaklar

- **"Sevk öncesi ile sevk sonrası kredinin farkı ne?"** İkisinin farkı vade değil **dayanaktır**. Sevk öncesi, henüz doğmamış bir alacağa yani taahhüde dayanır. Sevk sonrası, gerçekleşmiş bir alacağa ve belgeye dayanır. Risk profili ve sistemde bağlandıkları kayıt bu yüzden farklıdır.
- **"Taahhüt takibini nasıl kurgularsın?"** Taahhüt kaydı → belgelerle mahsup (kısmi olabilir) → kalan taahhüt → süre sonunda kapatma veya istisnanın geri alınması. Mali sonucun onaylı akışla kesinleştiğini ve denetim izini mutlaka söyle.
- **"Aynı beyanname iki kez mahsup edilirse?"** Benzersiz belge anahtarı + veritabanı benzersizlik kısıtı; ikinci gelişte hata değil "zaten işlenmiş". Uygulama içi sorgu-sonra-yaz kontrolünün eşzamanlılıkta yetmediğini ekle. Ayrışmak istersen şunu da söyle: belge farklı taahhütlere dağıtılabiliyorsa belge tarafında toplam kontrolü de gerekir.
- **"İGE kefaleti sistemde nereye oturur?"** Teminat yapısında **üçüncü taraf** olarak oturur. Kefalet oranı ve kapsamı parametredir, teminat-kredi ilişkisi çoka-çoktur, temerrütte tazmin talebi bir durum makinesidir. "Kefalet var, risk yok" deme.
- **"Dış kurum çağrısı zaman aşımına düştü, ne yaparsın?"** Asıl mesele başarısızlık değil, **sonucu bilinmeyen istektir**. Sıra şudur: benzersiz istek anahtarı, durum sorgulama, netleşmiyorsa belirsiz durumda bekletme ve mutabakat. Kör yeniden deneme ve erken telafi yanlıştır.
- **"Destek tutarının bir kısmı reddedilirse?"** Kısmi başarı modellenir; talep edilen ile doğrulanan ayrı alanlardır, fark gerekçesiyle saklanır, gerekiyorsa ters kayıtla telafi edilir.
- **"Taahhüdü hangi kurla karşılaştırırsın?"** Bunu kendin seçme. İş kuralıdır, iş biriminden yazılı alınır, tek yerde tanımlanır, tarih geçerlilikli tutulur ve **kullanılan kur kayda geçer**. Canlı kurdan anlık çevirme cevabı yanlıştır.
- **"Risk grubu ne demek?"** İlişkili firmaların tek limit çatısı altında toplanması; hiyerarşi grup → firma → ürün → işlem, nakdi ve gayrinakdi aynı ağaçta, kilit sırası her yerde aynı.
- **Mevzuat tuzağı.** Madde numarası, oran ya da tarih söylemeye çalışma. "Kambiyo ve ihracat mevzuatına bağlıdır; kural tarih geçerlilikli parametreden okunur, iş biriminden yazılı teyit alınır, kural sürümü kayda geçer" cevabı hem doğru hem güvenlidir.

### Tek cümlede

İhracat finansmanı, sevk öncesinde **taahhüde** ve sevk sonrasında **belgeye** dayanan iki farklı riski aynı üründe birleştirir. İGE kefaleti teminat yapısına **üçüncü taraf** ekler; riski azaltır ama sıfırlamaz. Sistemin kalbi şu döngüdür: **taahhüt kaydı → kısmi mahsup → kalan → kapatma ya da istisnanın geri alınması.** Bu döngüde aynı beyannamenin iki kez sayılmaması, **benzersiz belge anahtarı + veritabanı kısıtıyla** garanti edilir. Üç taraflı protokol entegrasyonunda sonucu bilinmeyen istek mutabakatla çözülür ve kısmi başarı modellenir. Kur karşılaştırması bir iş kuralıdır; tek yerde tanımlanır ve kullanılan kur kaydedilir. Limit ise grup → firma → ürün → işlem hiyerarşisinde toplanır. Mali sonuç doğuran her adım, onay ile denetim izi ister.

# ihrfin-1 | Orta
## Soru
Bir müşteriye ihracat döviz kredisi kullandırılacak. Ekip, kredinin "sevk öncesi" mi yoksa "sevk sonrası" mı olduğunu ayırt eden alanı veri modeline eklemek konusunda tartışıyor. Bu ayrımın sistem açısından asıl anlamı nedir?
## A
Ayrım yalnızca vadeyi belirler: sevk öncesi krediler kısa, sevk sonrası krediler uzun vadelidir; veri modelinde ayrı bir alan tutmak yerine vadeye bakmak yeterlidir
🤖 vade bir sonuçtur, ayrımın kendisi değildir; iki türde de vade müşteriye ve işleme göre değişebilir.
## B
Ayrım yalnızca muhasebe kodlamasını etkiler: her iki kredi de aynı biçimde izlenir, farklı olan tek şey gelir kaleminin hangi hesaba yazıldığıdır
🤖 muhasebe kodu ayrımın küçük bir yansımasıdır; asıl fark kredinin dayandığı kaynağın ve doğrulama kurallarının farklı olmasıdır.
## C
Sevk öncesi kredi henüz doğmamış bir alacağa, yani taahhüde dayanır ve süre sayacıyla izlenir; sevk sonrası kredi gerçekleşmiş bir alacağa ve belgeye dayanır, izlenen şey tahsilattır
🤖 Doğru: ayrım kredinin dayanağını değiştirir, dayanak da veri modelini ve izlemeyi belirler.
## D
Ayrım tamamen ürün pazarlamasına aittir; teknik olarak ikisi de aynı kredi kaydıdır ve tek bir ekranla yönetilebilir, ayrı bir alan gereksiz karmaşıklık üretir
🤖 iki türün doğrulama kuralları ve izleme ihtiyacı farklıdır; tek kayıt varsaymak sevk öncesindeki taahhüt takibini görünmez kılar.
## Doğru
C
## Açıklama
Bankanın ihracat döviz kredisi tanımı finansman ihtiyacını **"sevk öncesi veya sevk sonrası"** diye ayırır ve bu ayrım doğrudan veri modelini belirler. **Sevk öncesinde** mal henüz gitmemiştir; kredinin geri ödeme kaynağı **henüz doğmamış** bir ihracat bedelidir, yani bankanın elinde bir alacak değil müşterinin **taahhüdü** vardır. Bu yüzden kredi bir taahhüt kaydına bağlanır, süre sayacı başlar ve gerçekleşmenin belgelenmesi izlenir. **Sevk sonrasında** mal fiilen çıkmıştır; ortada belge vardır, kredi **var olan bir alacağa** dayanır ve izlenen şey tahsilattır. Vadeye indirgemek yanlıştır: vade bir sonuçtur, ayrımın kendisi değil; her iki türde de vade işleme göre değişir. Muhasebe koduna indirgemek de eksiktir; kod ayrımın küçük bir yansımasıdır, asıl fark zorunlu alanların ve doğrulama kurallarının farklılaşmasıdır. "Teknik olarak aynı kayıt" demek ise en tehlikelisidir: sevk öncesindeki **taahhüt takibi** görünmez hâle gelir, süre sayacı kurulmaz ve süre dolduğunda vergi, resim ve harç istisnasının geri alınması sürpriz olur. Pratikte iki tür birbirine bağlanabilir de: sevk öncesi kredi sevk gerçekleşince kapanabilir ya da sevk sonrasına dönüşebilir; bu, iki kopuk kayıt değil **bağlantılı bir durum geçişi** olarak modellenir, aksi hâlde aynı ihracatın iki kez finanse edildiği fark edilmez.

# ihrfin-2 | Orta
## Soru
İhracat taahhütlü bir kredide taahhüt takibi modülünü tasarlıyorsun. Taahhüdün yaşam döngüsünü aşağıdakilerden hangisi doğru kurar?
## A
Taahhüt kaydı açılır; gerçekleşen ihracat belgeleriyle kısmi mahsuplar işlenir ve kalan taahhüt her an hesaplanabilir olur; süre sonunda kalan sıfırsa kapatılır, değilse istisna geri alınır — her iki sonuç da onaylı akışla kesinleşir
🤖 Doğru: döngü kısmi gerçekleşmeyi, süre sınırını ve mali sonucun kontrollü kesinleşmesini birlikte karşılar.
## B
Taahhüt kaydı açılır ve süre sonunda tek seferde değerlendirilir; ara dönemde mahsup kaydı tutulmaz, çünkü taahhüdün tutulup tutulmadığı ancak sürenin sonunda belli olur
🤖 ara mahsup tutulmazsa kalan taahhüt bilinmez, eşik uyarısı üretilemez ve müşteri süre bitene kadar durumunu göremez.
## C
Taahhüt kaydı açılır; ilk ihracat belgesi geldiğinde taahhüt tutarına bakılmaksızın kapatılır, çünkü ihracatın gerçekleştiği belgeyle kanıtlanmıştır
🤖 belgenin varlığı taahhüdün tamamının karşılandığını göstermez; kısmi gerçekleşme taahhüdü kapatmaz.
## D
Taahhüt kaydı açılır; gün sonu toplu işi süre dolduğunda istisnayı otomatik olarak geri alır ve müşteriye yükümlülüğü doğrudan yansıtır, böylece insan gecikmesi ortadan kalkar
🤖 geri alınamaz mali sonucu bir toplu işe otomatik verdirmek, tek bir hatada binlerce müşteriye yanlış yükümlülük yansıtır.
## Doğru
A
## Açıklama
Ürün tanımı "24 ay içerisinde ihracat taahhüdünüzü yerine getirmeniz durumunda **vergi, resim ve harç istisnasından** yararlanabilirsiniz" dediğinde yazılıma verilen görev, taahhüdün tutulup tutulmadığını **kanıtlanabilir** biçimde takip etmektir. Döngü şudur: taahhüt kaydı (tutar, para birimi, başlangıç, süre) → gerçekleşen ihracatın belgelerle **mahsubu** → `kalan = taahhüt − mahsuplar toplamı` → süre sonunda **kapatma** ya da **istisnanın geri alınması**. Ara mahsup tutmamak (B) sistemi kör bırakır: kalan taahhüt bilinmediği için eşik uyarısı üretilemez, müşteri temsilcisi durumu göremez ve sürenin son gününde herkes hazırlıksız yakalanır. İlk belgede kapatmak (C) kısmi gerçekleşmeyi tam gerçekleşme saymaktır; bir belge çoğu zaman taahhüdün yalnız bir kısmını karşılar, bu yüzden **kısmi mahsup** ve **kalan** kavramları vardır. Gün sonu işine kararı otomatik verdirmek (D) ise mühendislik açısından en tehlikelisidir: taahhüt kapatma ve istisnanın geri alınması **geri alınamaz mali sonuç** doğurur; doğru kurgu sistemin koşulları hesaplayıp gerekçesiyle iş listesine düşürmesi, kesinleşmenin **yetkili onayıyla** olmasıdır. Her iki sonuçta da hangi kullanıcı, hangi belgelerle, hangi kural sürümüyle işlem yaptı bilgisi **denetim izinde** durur; hata olursa kayıt silinmez, kayıtlı bir düzeltmeyle giderilir.

# ihrfin-3 | İleri
## Soru
Taahhüt mahsubu ekranında, aynı gümrük beyannamesinin iki kez mahsup edildiği bir vaka çıktı. Mevcut kodda kayıt öncesi "bu belge daha önce girilmiş mi" diye bir sorgu yapılıyor. Bu sınıf hatayı kalıcı olarak nasıl kaparsın?
## A
Mahsup ekranında kaydet düğmesi ilk tıklamadan sonra pasifleştirilmeli ve kullanıcıya uyarı gösterilmelidir; böylece ikinci kayıt hiç oluşmaz
🤖 bu yalnız kullanıcı arayüzü önlemidir; ağ zaman aşımı, sekme tekrarı veya toplu dosya yüklemesi aynı kaydı yine üretebilir.
## B
Mahsup kayıtları gün sonunda taranıp aynı belgeye ait mükerrer satırlar silinmeli, silinen her satır bir düzeltme raporuna yazılmalıdır
🤖 mükerrer kayıt üretilip sonra silinene kadar geçen sürede taahhüt yanlış kapanmış ve rapora girmiş olabilir; ayrıca finansal kayıt silinerek düzeltilmez.
## C
Kayıt öncesi yapılan sorgu, işlem yalıtım düzeyi yükseltilerek tekrarlanabilir okuma seviyesine çekilmeli; böylece iki eşzamanlı istek birbirinin kaydını görebilir
🤖 yalıtım düzeyini yükseltmek henüz yazılmamış satırı görünür kılmaz; bu, kısıt yerine geçebilecek bir çözüm değildir.
## D
Belge için benzersiz bir iş anahtarı tanımlanmalı, mahsup tablosunda bu anahtar üzerinde veritabanı benzersizlik kısıtı bulunmalı ve kısıt ihlali hata olarak fırlatılmak yerine "zaten mahsup edilmiş" biçiminde ele alınmalıdır
🤖 Doğru: koruma veritabanı seviyesine indiği için eşzamanlı istekler de kapsanır ve tekrar zararsız hâle gelir.
## Doğru
D
## Açıklama
Bu sorunun teknik özü `130` konusundaki **idempotency** desenidir; buradaki bankacılık karşılığı ağırdır, çünkü aynı beyannamenin iki kez sayılması taahhüdü olduğundan hızlı kapatır — yani **haksız yere korunmuş bir vergi, resim ve harç istisnası** üretir. Mevcut koddaki "önce sorgula, yoksa yaz" yaklaşımı eşzamanlılıkta çalışmaz: iki istek aynı anda geldiğinde ikisi de "yok" görür ve ikisi de yazar. Çözüm, korumayı **veritabanına** indirmektir: belgeyi tek gösteren bileşenlerden (belge numarası, düzenleyen idare kodu, tescil tarihi gibi) bir **benzersiz iş anahtarı** kurulur ve mahsup tablosunda bu anahtar üzerinde **benzersizlik kısıtı** bulunur; ikinci yazma veritabanınca reddedilir ve uygulama bunu hata olarak değil "zaten mahsup edilmiş" olarak ele alır — istek tekrarlandığında sonuç değişmez. Arayüzde düğmeyi pasifleştirmek (A) yalnız bir kullanıcı deneyimi önlemidir; ağ zaman aşımı, ikinci sekme veya toplu dosyanın yeniden yüklenmesi aynı kaydı yine üretir. Sonradan silmek (B) iki açıdan yanlıştır: mükerrer kaydın üretilmesiyle temizlenmesi arasında taahhüt yanlış kapanmış ve rapora girmiş olabilir, ayrıca finansal kayıt silinerek değil kayıtlı bir düzeltmeyle giderilir. Yalıtım düzeyini yükseltmek (C) henüz yazılmamış bir satırı görünür kılmaz ve kısıtın yerine geçmez (`224`). Ek bir incelik: iş kuralı bir belgenin birden fazla taahhüde bölünerek kullanılmasına izin veriyorsa `(taahhüt, belge)` kısıtı tek başına yetmez; belgenin **toplam kullanılan tutarı** da ayrıca izlenmeli ve belge tutarını aşmamalıdır.

# ihrfin-4 | İleri
## Soru
İGE kefaletli bir kredide müşteri ödemelerini aksatmaya başladı. Kefaletin sistemdeki yeri ve temerrüt sonrası akış hakkında hangi ifade doğrudur?
## A
Kefalet kredinin teminatı olduğu için borçlu temerrüde düştüğünde kredi bakiyesi doğrudan sıfırlanmalı, alacak takibi tümüyle kefile devredilmelidir
🤖 kefalet alacağı kendiliğinden ortadan kaldırmaz; bakiyenin sıfırlanması kefil ödeme yapmadan gerçekleşmez ve kayıt silinerek yürütülmez.
## B
Kefalet, teminat yapısında üçüncü taraf olarak durur; oranı ve kapsamı parametreden okunur, teminat-kredi ilişkisi çoka-çok olabilir ve temerrütte kefile tazmin talebi bir durum makinesi olarak yürütülür
🤖 Doğru: kefaletin ayrı bir taraf olması hem veri modelini hem temerrüt akışını belirler ve risk sıfırlanmaz.
## C
Kefalet bulunan kredilerde teminat takibi gereksizdir; kefil bir kurum olduğu için kredi risksiz kabul edilir ve limit kullanımı da hesaba katılmaz
🤖 kefalet riski azaltır ama sıfırlamaz; kapsam dışı kalma ihtimali vardır ve limit kullanımı teminattan bağımsız bir boyuttur.
## D
Kefalet, kredi kaydının içinde tek bir oran alanı olarak tutulmalıdır; ayrı bir teminat kaydı açmak veri modelini gereksiz karmaşıklaştırır ve raporlamayı zorlaştırır
🤖 tek alan, bir teminatın birden çok krediyi kısmen kapatabilmesini ve serbest/bağlı kısım ayrımını temsil edemez.
## Doğru
B
## Açıklama
Bankanın tanımına göre İGE teminatlı krediler, **İhracatı Geliştirme A.Ş.'nin sağladığı kefalet desteği** ile ihracatçı ya da ihracat potansiyeli bulunan, döviz kazandırıcı hizmet faaliyetlerinde bulunan **KOBİ ve KOBİ dışı** firmalara **24 ay vadeye kadar** sunulan **ihracat taahhütlü** kredilerdir. Bu cümlenin sistemdeki tek yapısal karşılığı, teminat yapısında bir **üçüncü tarafın** bulunmasıdır: teminatı sağlayan, işlemin tarafı olan müşteri değildir. Veri modeline yansıması nettir — teminat kaydında tür, kefil kimliği, kapsam ve **kefalet oranı** bulunur; oran ve kapsam koda gömülmez, **tarih geçerlilikli parametre** olarak tutulur. Teminat ile kredi arasındaki ilişki **çoka-çoktur**: bir teminat birden çok krediyi kısmen kapatabilir, bir kredi birden çok teminatla kapatılabilir; bu yüzden bağlantı kaydında **bağlanan tutar** taşınır ve teminatın serbest kısmı ile bağlı kısmı ayrı izlenir — tek bir oran alanı (D) bunu temsil edemez. Temerrüt sonrası akış bir **durum makinesidir**: gecikme → temerrüt → kefile **tazmin talebi** → değerlendirme → ödeme veya ret; her adımın belge şartı, süresi ve yetkilisi vardır, her geçiş denetim izine yazılır. Bakiyeyi doğrudan sıfırlayıp takibi devretmek (A) yanlıştır; genel uygulamada kefil ödeme yaptığında alacak ortadan kalkmaz, **alacaklı değişir** ve bu bir kayıt silme işlemi değildir. "Kefalet var, risk yok" (C) ise en klasik tuzaktır: kapsam dışı kalma ihtimali vardır (taahhüt ihlali, belge eksikliği, süre kaçırma), kefaletle örtülmeyen kısım banka riskidir, tazmin süreci zaman alır ve teminat ile **limit kullanımı** ayrı boyutlardır.

# ihrfin-5 | Uzman
## Soru
Prefinansman ürününde, üç taraflı protokol kapsamındaki bir desteğin doğrulanması için dış kuruma çağrı yapıldı ancak çağrı zaman aşımına düştü; isteğin karşı tarafa ulaşıp ulaşmadığı bilinmiyor. Doğru davranış nedir?
## A
Çağrı hemen tekrarlanmalı ve sonuç alınana kadar denenmelidir; doğrulama olmadan prefinansman kullandırılamayacağı için önceliğimiz sonucu almaktır
🤖 sonucu bilinmeyen bir istekte kör tekrar, karşı tarafta işlenmiş bir talebin ikinci kez oluşmasına yol açabilir.
## B
İşlem başarısız kabul edilip telafi akışı hemen tetiklenmeli, varsa yapılmış kayıtlar ters kayıtla geri alınmalıdır
🤖 durum belirsizken telafi işletmek, aslında başarılı olmuş bir işlemi geri almak anlamına gelebilir.
## C
Doğrulama adımı atlanıp kullandırım yapılmalı, destek kaydı sonradan gelen mutabakat dosyasıyla düzeltilmelidir; böylece müşteri beklemez
🤖 doğrulanmamış bir desteğe dayanarak kullandırım yapmak riski müşteri deneyimi uğruna kabul etmektir.
## D
İstek benzersiz bir anahtarla gönderilmiş olmalı; önce durum sorgulama çağrısıyla gerçek durum netleştirilmeli, netleşmiyorsa işlem belirsiz durumda bekletilip mutabakatla çözülmelidir
🤖 Doğru: belirsizlik ayrı bir durum olarak modellenir, mükerrer kayıt da yanlış telafi de önlenir.
## Doğru
D
## Açıklama
Banka, **T.C. Ticaret Bakanlığı, Türk Ticaret Bankası A.Ş. ve İGE A.Ş. arasında imzalanan protokol** çerçevesinde Proje/Program/Statü bazlı destekleri prefinansman modeli kapsamına almıştır; yazılım açısından bu bir **dış kurum entegrasyonu** problemidir (`221`). Zaman aşımı, sınırlı yeniden deneme ve devre kesici zaten gereklidir (`101`), ama bu senaryodaki asıl mesele başarısızlık değil **sonucu bilinmeyen istektir**. Kör tekrar (A) en yaygın hatadır: istek karşı tarafa ulaşmış ve işlenmişse ikinci deneme mükerrer bir talep üretir. Hemen telafi işletmek (B) simetrik hatadır: durum belirsizken telafi, aslında başarılı olmuş bir işlemi geri almak anlamına gelebilir; telafi (`126`) yalnız durum **kesinleştiğinde** tetiklenir. Doğrulamayı atlayıp kullandırmak (C) ise mühendislik değil risk kabulüdür — desteğin varlığı ve tutarı doğrulanmadan finansman verilmesi demektir. Doğru davranış üç adımlıdır: (1) her istek **benzersiz istek anahtarıyla** gönderilir, böylece karşı taraf idempotent davranabilir; (2) karşı tarafta bir **durum sorgulama** çağrısı varsa gerçek durum önce onunla netleştirilir; (3) netleşmiyorsa işlem **belirsiz** durumunda bekletilir, otomatik karar üretilmez ve periyodik **mutabakatla** çözülür — bizim kayıtlarımız ile karşı kurumun kayıtları karşılaştırılıp fark listesi üretilir. Bu senaryonun kardeşi **kısmi başarıdır**: destek tutarının bir kısmı doğrulanıp kalanı reddedilebilir, dolayısıyla "hepsi ya da hiçbiri" varsayımı yanlıştır; talep edilen ile doğrulanan tutar ayrı alanlarda tutulur, fark gerekçesiyle saklanır ve finansman zaten kullandırılmışsa aradaki fark ters kayıtla telafi edilir (`241`).

# ihrfin-6 | İleri
## Soru
Kurumsal müşteri, birbiriyle ortaklık ilişkisi bulunan üç firmadan oluşuyor. Ekip, ihracat kredisi limitlerinin nasıl kontrol edileceğini tasarlıyor. Hangi yaklaşım doğrudur?
## A
Limitler risk grubu → firma → ürün → işlem hiyerarşisinde tutulmalı, kullanımlar yukarı doğru toplanmalı, nakdi ve gayrinakdi riskler aynı ağaçta yer almalıdır
🤖 Doğru: ilişkili firmaların toplam riski görünür kalır, gayrinakdi risk dışarıda bırakılmaz ve sabit kilit sırası kilitlenmeyi önler.
## B
Her firmanın limiti bağımsız tutulmalıdır; firmalar ayrı tüzel kişilikler olduğu için birinin kullanımı diğerinin limitini etkilememelidir
🤖 aynı ekonomik riskin ayrı tüzel kişiliklere bölünerek küçük görünmesi tam olarak risk grubu kavramının engellemek istediği durumdur.
## C
Limit yalnızca nakdi krediler için tutulmalı; teminat mektubu gibi gayrinakdi ürünler nakit çıkışı üretmediği için ayrı bir listede izlenmelidir
🤖 gayrinakdi risk tazmin hâlinde nakde döner; limit ağacının dışında bırakılması toplam riski olduğundan küçük gösterir.
## D
İGE kefaleti bulunan krediler limit hesabından düşülmelidir; kefaletle örtülen kısım banka riski olmadığı için limiti işgal etmemelidir
🤖 teminat ile limit ayrı boyutlardır; kefalet riski azaltır ama limit kullanımını kendiliğinden ortadan kaldırmaz.
## Doğru
A
## Açıklama
Kurumsal bankacılıkta limit tek bir firmanın limiti değildir: ortaklık, yönetim veya kefalet ilişkisiyle bağlı firmalar bir **risk grubu** altında toplanır. Gerekçesi doğrudan risk yönetimidir — aynı ekonomik riskin birkaç ayrı tüzel kişiliğe bölünerek küçük görünmesi engellenir; bu yüzden "her firma bağımsız" (B) yaklaşımı risk grubu kavramının engellemek için var olduğu durumu yeniden üretir. Sistemdeki karşılık bir hiyerarşidir: **risk grubu → firma → ürün → işlem**; tek bir kullandırım bağlı olduğu ürün limitini, firmanın limitini ve grubun limitini birlikte etkiler ve alt seviyelerdeki kullanımların toplamı üst seviyedeki limiti aşamaz. Gayrinakdi ürünleri ayrı listeye almak (C) yanlıştır: teminat mektubu bugün nakit çıkışı üretmez ama tazmin hâlinde nakde döner (`244`), dolayısıyla aynı ağaçta durmalıdır — dışarıda bırakılırsa toplam risk olduğundan küçük görünür. Kefaletli krediyi limitten düşmek (D) ise teminat ile limiti karıştırmaktır: İGE kefaleti riski azaltan bir teminattır ve teminat yapısında ayrı bir boyut olarak durur, limit kullanımını kendiliğinden ortadan kaldırmaz. Son olarak eşzamanlılık: aynı grup altında iki kullandırım aynı anda limit düşerse yarış koşulu doğar ve doğru koruma seçilmelidir (`225`); ayrıca **kilit sırası her kod yolunda aynı** olmalıdır — grup → firma → ürün sırası bir yerde tersine çevrilirse iki işlem birbirini bekleyerek kilitlenir (deadlock). Ağaç toplamlarının tutulduğu satırlar klasik sıcak kayıttır; işlem kısa tutulur ve gerekiyorsa toplam parçalanarak çekişme dağıtılır.
