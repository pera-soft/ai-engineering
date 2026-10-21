---
slug: havacilik-bt-alan-bilgisi
baslik: "Havacılık BT Alan Bilgisi: Yolcunun Yolculuğu, Sistemin Yolculuğu"
kademe: Orta
sira: 247
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir yolcu olarak yaşadığın akış tanıdıktır: uçuş ararsın, birini seçip yer ayırtırsın, ödersin, biletin gelir, uçuş günü check-in yapar ve uçağa binersin. Her adım sana tek bir uygulamanın ekranı gibi görünür. Oysa perdenin arkasında her adımın **ayrı bir sistemi** vardır. Bu sistemler farklı çağlarda yazılmıştır ve farklı standartlarla konuşur.

Havacılık ilanına giren bir Java geliştiricisi mülakatta "sektörü biliyor" izlenimini nasıl bırakır? En kısa yolu, bu akışı **sistem karşılıklarıyla** sayabilmektir: uygunluk sorgusu, PNR, e-bilet, DCS. Bu konu sana o haritayı verir. Kavramların mühendislik karşılığı — eşzamanlılık, önbellek, saga — ise `248` numaralı konudadır.

### Temel kavramlar

- **Uygunluk (availability):** bir uçuşta hangi sınıftan kaç koltuğun satılabilir olduğunu söyleyen sorgu; arama ekranlarının arkasındaki en yoğun trafik budur.
- **PNR (Passenger Name Record):** rezervasyon kaydı — yolcu bilgileri, uçuş bacakları (segment), iletişim ve servis talepleri. **Bilet değildir**; ödeme kanıtı taşımaz.
- **E-bilet (e-ticket):** ödemenin karşılığı olan elektronik biletleme kaydı; her uçuş bacağı için bir **kupon (coupon)** içerir.
- **Kupon durumu:** her kuponun yaşam döngüsü bir durum makinesidir — yaygın modellemede open (kullanılabilir), used/flown (uçuldu), refunded (iade), exchanged (değiştirildi).
- **Time limit (opsiyon süresi):** biletlenmemiş rezervasyonun yaşama süresi; dolarsa rezervasyon düşer ve koltuk envantere döner (yaygın uygulama).
- **No-show:** bileti olduğu halde uçuşa gelmeyen yolcu; iade/değişiklik kuralları bilet kurallarına göre işler.
- **Rezervasyon sınıfı (RBD / fare class):** aynı kabini fiyat kovalarına bölen harf kodları; gelir yönetiminin (revenue management) temel aracı.
- **PSS (Passenger Service System):** havayolunun çekirdek yolcu sistemi — genellikle rezervasyon, envanter ve biletlemeyi kapsar.
- **DCS (Departure Control System):** kalkış kontrol sistemi — check-in, biniş, yük ve denge.
- **GDS (Global Distribution System):** acentelerin tek bağlantıyla çok sayıda havayoluna eriştiği dağıtım kanalı; Amadeus ve Sabre yaygın örneklerdir.
- **NDC (New Distribution Capability):** IATA'nın API tabanlı dağıtım standardı; havayolunun zengin içeriğini aracıya doğrudan sunmasını hedefler.
- **IRROPS (irregular operations):** düzensiz operasyon — rötar, iptal, yönlendirme ve etkilenen yolcuların yeniden yerleştirilmesi.

### Nasıl çalışır?

Yolcunun akışını sistem gözüyle yeniden yürüyelim:

1. **Arama / uygunluk:** yolcu tarih ve rota girer; sistem tarife (schedule) ve koltuk envanterinden satılabilir seçenekleri döndürür. Bu adım okuma-ağırlıklıdır ve tek satış başına yüzlerce arama düşer.
2. **Rezervasyon → PNR:** seçim yapılınca bir **PNR** oluşur: yolcular, uçuş bacakları, iletişim. Koltuk envanterden düşülür ama **henüz ödeme yoktur** — PNR bir söz, bir tutma kaydıdır.
3. **Time limit:** biletlenmemiş PNR süresiz yaşamaz. Tanımlı süre içinde ödeme/biletleme gelmezse rezervasyon düşer, koltuk satışa geri döner. Bu, envanterin "unutulmuş rezervasyonlarla" kilitlenmesini önler.
4. **Ödeme ve biletleme → e-bilet:** ödeme alınınca **e-bilet** üretilir; her uçuş bacağı için bir **kupon** açılır (open). Rezervasyon ile bilet burada ayrışır: PNR "kim, hangi uçuşta" sorusunun, e-bilet "parası ödendi mi, hangi kupon ne durumda" sorusunun kaydıdır.
5. **Check-in → DCS:** uçuş günü kayıt **DCS**'e geçer: koltuk ataması, biniş kartı, yük ve denge. Biniş kapısında kupon kullanılır ve durumu used/flown olur.
6. **Değişiklik ve iade:** tarih değişikliğinde ilgili kupon exchanged işaretlenir ve yerine yeni kupon/bilet üretilir; iadede refunded olur. Uçulmuş kupon geri açılmaz — durum makinesi tek yönlü geçişlerle korunur.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yolcu yolculuğunun sistem karşılığı: aramadan binişe altı adım">
  <defs>
    <marker id="havbt-yol-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yolcunun yolculuğu → sistemin yolculuğu</text>
  <rect x="16" y="44" width="96" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="64" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Arama</text>
  <text x="64" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">availability</text>
  <path d="M114 72 L122 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-yol-arw)"/>
  <rect x="124" y="44" width="96" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Rezervasyon</text>
  <text x="172" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">PNR oluşur</text>
  <path d="M222 72 L230 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-yol-arw)"/>
  <rect x="232" y="44" width="96" height="56" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="280" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Ödeme</text>
  <text x="280" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">time limit içinde</text>
  <path d="M330 72 L338 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-yol-arw)"/>
  <rect x="340" y="44" width="96" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="388" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">E-bilet</text>
  <text x="388" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kuponlar açılır</text>
  <path d="M438 72 L446 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-yol-arw)"/>
  <rect x="448" y="44" width="96" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="496" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Check-in</text>
  <text x="496" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">DCS</text>
  <path d="M546 72 L554 72" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-yol-arw)"/>
  <rect x="556" y="44" width="88" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="600" y="66" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Biniş</text>
  <text x="600" y="84" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kupon: used</text>
  <rect x="70" y="128" width="520" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Rezervasyon ≠ bilet</text>
  <text x="330" y="168" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">PNR ödemesiz bir tutma kaydıdır; time limit dolarsa düşer, koltuk envantere döner</text>
  <text x="330" y="182" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">e-bilet ise ödemenin karşılığıdır; kupon durumları biletin gerçeğini taşır</text>
</svg>
```

### Envanter, rezervasyon sınıfları ve gelir yönetimi

Uçaktaki fiziksel kabin (ekonomi, business) ile satıştaki sınıf aynı şey değildir. Aynı ekonomi kabini, satış tarafında **rezervasyon sınıfı (RBD)** denen harf kodlu **fiyat kovalarına** bölünür. Bunlar en ucuzdan en pahalıya doğru sıralanan katmanlardır ve her biri sınırlı sayıda koltuğa açılır.

Uçuşa erken bakan yolcu genellikle ucuz kovadan alır. Kova dolunca satış bir üst kovadan devam eder. "Uçak fiyatı yükseldi" cümlesinin sistemdeki karşılığı çoğu zaman fiyatın değişmesi değildir; **ucuz kovanın tükenmesidir**.

Hangi kovaya kaç koltuk açılacağını ayarlayan disipline **gelir yönetimi (revenue management)** denir. Talep tahminine göre kovalar açılır, kapanır ve yeniden dengelenir. Geliştirici gözüyle önemli sonuç şudur: koltuk envanteri tek bir sayı değildir. **Sınıf bazında yönetilen ve sürekli oynayan** bir yapıdır. "Kaç koltuk kaldı?" sorusunun cevabı, hangi sınıftan sorduğuna bağlıdır.

### Ekosistem: PSS, GDS, NDC ve aracılar

Havayolunun çekirdeğinde **PSS** durur. Rezervasyon, envanter ve biletlemeyi taşıyan sistem odur; check-in tarafında DCS onunla el sıkışır. Ama biletlerin önemli bir bölümü havayolunun kendi kanalından satılmaz.

**GDS**'ler seyahat acentelerinin dağıtım kanallarıdır; acente tek bağlantıyla yüzlerce havayolunu arayıp satabilir. Amadeus ve Sabre yaygın örneklerdir. Çevrimiçi acenteler (OTA) de büyük ölçüde bu kanalların üzerinde çalışır. Bu geleneksel dağıtım, tarihsel olarak **EDIFACT** tarzı mesaj standartlarıyla kurulmuştur.

**NDC** ise IATA'nın API tabanlı dağıtım standardıdır. Amacı şudur: havayolu kendi zengin içeriğini (koltuk seçimi, bagaj, paket teklifler) aracıya doğrudan sunsun. Bunu kendi ürettiği tekliflerle ve XML/JSON tabanlı servisler üzerinden yapar. Geçiş sektörde kademelidir; iki dünya genellikle uzun süre yan yana yaşar.

Havayolları arası iş birliğinin iki temel kavramı da bilinmelidir. **Interline**, iki havayolunun birbirinin yolcusunu tek bilette taşıma anlaşmasıdır. **Codeshare** ise bir uçuşun birden fazla havayolu koduyla satılmasıdır. İkisi de "tek yolculuk, birden çok havayolu sistemi" demektir ve entegrasyon yükünün önemli kaynaklarındandır.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Havacılık dağıtım ekosistemi: PSS, GDS, NDC ve satış kanalları">
  <defs>
    <marker id="havbt-eko-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dağıtım ekosistemi: bilet hangi yollardan satılır?</text>
  <rect x="24" y="88" width="170" height="76" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="109" y="112" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Havayolu PSS</text>
  <text x="109" y="130" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">rezervasyon · envanter</text>
  <text x="109" y="146" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">biletleme (+ DCS)</text>
  <path d="M194 104 L286 62" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-eko-arw)"/>
  <rect x="288" y="40" width="170" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="373" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kendi kanalı</text>
  <text x="373" y="74" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">web · mobil — doğrudan satış</text>
  <path d="M194 126 L286 126" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-eko-arw)"/>
  <rect x="288" y="104" width="170" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="373" y="122" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">GDS (Amadeus, Sabre)</text>
  <text x="373" y="138" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">geleneksel dağıtım · EDIFACT kökenli</text>
  <path d="M194 148 L286 190" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-eko-arw)"/>
  <rect x="288" y="168" width="170" height="44" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="373" y="186" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">NDC API</text>
  <text x="373" y="202" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">IATA standardı · API tabanlı teklifler</text>
  <path d="M458 126 L546 126" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-eko-arw)"/>
  <path d="M458 190 L546 146" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havbt-eko-arw)"/>
  <rect x="548" y="96" width="96" height="60" rx="10" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.4"/>
  <text x="596" y="120" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Acente / OTA</text>
  <text x="596" y="138" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">satış noktası</text>
  <text x="330" y="238" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">İki dünya yan yana yaşar: EDIFACT kökenli kanal ile API tabanlı NDC geçişi kademelidir (genel eğilim)</text>
</svg>
```

### Operasyon tarafı: tarife, IRROPS, slot — ve diğer alanlar

Satış dünyasının yanında bir de **operasyon** dünyası vardır.

**Tarife (schedule)**, havayolunun hangi rotayı hangi gün ve saatte uçacağının planıdır. Sezonluk yayımlanır ve satışın üzerine kurulduğu iskelettir. **Uçuş planı** ise tek bir uçuşun operasyonel ayrıntısıdır: uçak tipi, rota, yakıt, ekip. **Slot**, yoğun havalimanlarında belirli bir saatte kalkış veya iniş yapma hakkıdır; kıt ve değerli bir kaynaktır.

Sektörün en sancılı senaryosu **IRROPS**'tur. Fırtına, arıza veya kapasite sorunu bir anda onlarca uçuşu rötara ya da iptale sürükler. Etkilenen her uçuşun arkasında yüzlerce PNR vardır ve hepsinin **yeniden yerleştirilmesi** (reaccommodation) gerekir. Bağlantılı uçuşlar, interline yolcular ve opsiyon süreleri işi katlar. Bu, havacılık BT'sinin "normal günü hafif, kötü günü çok ağır" yükünün tipik örneğidir.

Resmi tamamlamak için iki alanın varlığını bilmek yeter. **Kargo** sistemleri yük rezervasyonu ve takibini yapar. **MRO** ise bakım-onarım alanıdır; uçağın bakım kayıtlarını ve planlamasını taşır. İkisi de ayrı ve derin BT alanlarıdır.

### Sektör BT'sinin karakteri

Mülakatta "havacılık BT'si neye benzer?" sorusunun dört cevabı vardır.

**7/24 çalışır.** Uçaklar gece de uçar; dünyanın bir yerinde her an check-in vardır. Uzun bakım pencereleri bu dünyada bir lükstür.

**Yük sezonluktur ve tepelidir.** Bayram, tatil dönemi ve kampanya anları normal trafiğin katlarını getirir. Sistemler ortalamaya değil, tepeye göre düşünülür.

**Çok eski ile çok yeni yan yana yaşar.** Onlarca yıllık host/EDIFACT dünyası ile modern API'ler (NDC, JSON servisler) aynı akışın içindedir. Geliştiricinin işi çoğu zaman ikisi arasında köprü kurmaktır.

**Entegrasyon yoğundur.** GDS'ler, diğer havayolları (interline/codeshare), havalimanları, ödeme sağlayıcıları ve devlet sistemleri devrededir. Tek bir yolculuk, düzinelerce sistemin el sıkışmasıdır.

Bu dört karakterin mühendislik sonuçları `248` numaralı konuda işlenir: önbellek, eşzamanlılık, saga ve yük düzleştirme.

```svg
<svg viewBox="0 0 660 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="E-bilet kupon durum makinesi: open, used, exchanged, refunded geçişleri">
  <defs>
    <marker id="havbt-kupon-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">E-bilet kuponu bir durum makinesidir</text>
  <rect x="60" y="86" width="130" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="125" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">open</text>
  <text x="125" y="124" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kullanılabilir kupon</text>
  <path d="M190 96 L280 62" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#havbt-kupon-arw)"/>
  <text x="234" y="66" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">uçuş gerçekleşti</text>
  <rect x="284" y="40" width="130" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="349" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">used / flown</text>
  <text x="349" y="74" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">geri açılmaz</text>
  <path d="M190 110 L280 110" fill="none" stroke="#B07D20" stroke-width="1.4" marker-end="url(#havbt-kupon-arw)"/>
  <text x="234" y="102" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">tarih/uçuş değişti</text>
  <rect x="284" y="88" width="130" height="44" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="349" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">exchanged</text>
  <text x="349" y="122" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yeni kupon üretilir</text>
  <path d="M414 110 L504 110" fill="none" stroke="#B07D20" stroke-width="1.4" marker-end="url(#havbt-kupon-arw)"/>
  <text x="459" y="102" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">yeni bilette</text>
  <rect x="508" y="88" width="120" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="568" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">open (yeni)</text>
  <text x="568" y="122" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yeni tarih için</text>
  <path d="M190 124 L280 158" fill="none" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#havbt-kupon-arw)"/>
  <text x="234" y="158" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">iade edildi</text>
  <rect x="284" y="140" width="130" height="44" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="349" y="158" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">refunded</text>
  <text x="349" y="174" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">para iade sürecine</text>
  <text x="330" y="204" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Geçişler tek yönlüdür: used kupon geri açılmaz; değişiklik iptal değil, exchange + yeni kupondur</text>
</svg>
```

### Mülakatta ne sorulur?

- **"PNR nedir, bilet midir?"** Eleyici sorudur. PNR rezervasyon kaydıdır, bilet değildir; ödeme kanıtı e-bilettir. İkisini eş anlamlı kullanan aday sektörü hiç görmemiş sayılır.
- **"Rezervasyon var ama bilet yoksa ne olur?"** Time limit anlatılır: süre dolunca rezervasyon düşer, koltuk envantere döner. No-show ise ayrı bir kavramdır — bileti olan yolcunun gelmemesi.
- **"Aynı kabinde neden bin lira fark var?"** RBD/fiyat kovaları ve gelir yönetimi; "fiyat arttı" değil "ucuz kova bitti".
- **"GDS ile NDC farkı?"** GDS aracı dağıtım kanalıdır (acente ↔ çok havayolu), NDC ise IATA'nın API tabanlı dağıtım **standardıdır**; kesin oran/tarih iddiasına girmeden "geçiş kademeli, iki dünya yan yana" demek doğru duruştur.
- **"Büyük bir iptal dalgasında sistemde ne olur?"** IRROPS: etkilenen uçuşlardaki tüm PNR'lar yeniden yerleştirilir; bağlantılar ve interline işi katlar. Teknik karşılığı (toplu iş + kuyruk) `248`.
- **Alan deneyimin yoksa** uydurma; bu konudaki akışı sırasıyla sayıp en yakın gerçek deneyimini (stok/rezervasyon benzeri eşzamanlılık, durum makinesi, dış sistem entegrasyonu) köprüyle bağla (`246`).

### Tek cümlede

Havacılık BT'sinde yolcunun akışı, sistemlerin akışıdır. Uygunluk sorgusu envanteri okur. Rezervasyon **PNR** üretir ama bu bir bilet değildir. Ödeme, **e-biletin kuponlarını** açar. DCS ise check-in ve binişi yürütür. Kabin **RBD kovalarına** bölünerek gelir yönetimiyle fiyatlanır ve satış PSS-GDS-NDC ekosistemine dağılır. Bu dünya 7/24 çalışır, tepe yüklüdür ve eski ile yeni sistemleri yan yana yaşatır; en ağır günü de **IRROPS** günüdür.

# havbt-1 | Temel

## Soru
Havacılık sistemlerinde PNR (Passenger Name Record) nedir?

## A
Ödemenin yapıldığını kanıtlayan elektronik uçuş biletidir; PNR numarası bilet numarasının kısaltılmış halidir
🤖 ödeme kanıtı e-bilettir; PNR biletlenmemiş de var olabilir ve bilet numarası PNR kodundan ayrı bir kayıttır.

## B
Check-in sırasında üretilen biniş belgesinin numarasıdır; uçuş günü DCS tarafından oluşturulur ve öncesinde yoktur
🤖 PNR rezervasyon anında oluşur; check-in ve biniş kartı çok daha sonraki DCS adımının çıktısıdır.

## C
Havayolunun bir uçuş için tuttuğu operasyonel plan kaydıdır: uçak tipi, rota, yakıt ve ekip bilgilerini içerir
🤖 anlatılan uçuş planıdır; PNR uçuşun değil yolcunun kaydıdır ve operasyonel ayrıntı içermez.

## D
Yolcu, uçuş bacakları ve iletişim bilgilerini taşıyan rezervasyon kaydıdır; bilet değildir ve ödeme kanıtı taşımaz
🤖 Doğru: PNR "kim, hangi uçuşta" sorusunun kaydıdır; ödemenin karşılığı ayrı bir kayıt olan e-bilettir.

## Doğru
D

## Açıklama
PNR, rezervasyon yapıldığı anda oluşan **rezervasyon kaydıdır**: yolcu bilgileri, uçuş bacakları (segment), iletişim ve servis talepleri. En kritik nokta, PNR'ın **bilet olmamasıdır**. Bir PNR ödemesiz de var olabilir — koltuk tutulmuştur ama para alınmamıştır; tanımlı time limit içinde biletlenmezse rezervasyon düşer ve koltuk envantere döner. Ödemenin karşılığı olan kayıt **e-bilettir**: her uçuş bacağı için bir kupon içerir ve biletin gerçek durumunu (open, used, refunded, exchanged) kuponlar taşır. Bilet cevabını seçmek tam da mülakattaki eleyici hatadır: PNR ile e-bilet ayrımı, "sektörü görmüş mü?" sorusunun ilk turnusoludur. Biniş belgesi cevabı akışın sırasını bozar — PNR rezervasyonda doğar, biniş kartı ise uçuş günü DCS'in ürettiği çok sonraki çıktıdır. Uçuş planı cevabı ise kaydın öznesini karıştırır: uçuş planı **uçuşun** operasyonel kaydıdır (uçak tipi, rota, yakıt, ekip), PNR ise **yolcunun** satış tarafındaki kaydıdır. Akışın tamamı için bu konudaki altı adımı sırasıyla sayabilmek yeterlidir.

# havbt-2 | Orta

## Soru
Yolcu rezervasyon yaptı, PNR oluştu ve koltuk envanterden düşüldü; ama ödeme yapılmadı. Yaygın uygulamada sistem bu durumu nasıl yönetir?

## A
Rezervasyona bir time limit tanımlanır; süre içinde biletleme gelmezse rezervasyon düşer ve koltuk satışa geri döner
🤖 Doğru: time limit, ödemesiz tutma kayıtlarının envanteri süresiz kilitlemesini önleyen mekanizmadır.

## B
Rezervasyon koltuğu süresiz tutar; iptal ancak yolcunun kendi talebiyle veya uçuşun kapanmasıyla gerçekleşir
🤖 süresiz tutma, envanterin ödemesiz kayıtlarla kilitlenmesi demektir; satılabilir koltuklar boş yere bloke kalır.

## C
PNR otomatik olarak bilete dönüştürülür ve ödeme check-in sırasında tahsil edilir; böylece koltuk garanti altına alınır
🤖 biletleme ödemenin karşılığıdır ve otomatik gerçekleşmez; ödemeyi check-in'e ötelemek satış akışının tanımına aykırıdır.

## D
Koltuk uçuş gününe kadar bloke kalır ve yolcu gelmezse no-show işlemi uygulanır; ödemesiz rezervasyon da bu kapsamdadır
🤖 no-show bileti olan yolcunun uçuşa gelmemesidir; ödemesiz rezervasyonun düşmesi ayrı bir mekanizmadır (time limit).

## Doğru
A

## Açıklama
Rezervasyon ile bilet ayrımının pratik sonucu budur: PNR bir **tutma kaydıdır** ve envanterden koltuk düşürür, ama para gelmemiştir. Sistem bu araladaki riski **time limit** (opsiyon süresi) ile yönetir: rezervasyona tanımlı süre içinde biletleme gelmezse rezervasyon otomatik düşer ve koltuk satışa geri döner. Bu, gelir yönetiminin çalışabilmesi için de zorunludur — ödemesiz kayıtların süresiz koltuk tutması, satılabilir envanteri fiilen küçültür ve uçuş kapanana kadar geri gelmez. Süresiz tutma cevabı tam bu yüzden yanlıştır. PNR'ın otomatik bilete dönüşmesi diye bir mekanizma yoktur; biletleme **ödemenin karşılığıdır** ve e-bilet ancak tahsilatla üretilir — ödemeyi check-in'e ötelemek satış akışını baştan tanımsız bırakır. No-show cevabı ise iki ayrı kavramı birleştirir: no-show, **bileti olan** yolcunun uçuşa gelmemesidir ve bilet kurallarına göre (iade/değişiklik/hak kaybı) işlenir; ödemesiz rezervasyonun düşmesi ise bilet öncesi aşamanın mekanizmasıdır. Mülakatta bu iki kavramı ayırabilmek, PNR/e-bilet ayrımını gerçekten anladığının işaretidir. Süre ve kural ayrıntıları havayolundan havayoluna değişir; kesin süre iddiası vermek yerine mekanizmayı anlatmak doğru duruştur.

# havbt-3 | Orta

## Soru
Aynı uçuşun aynı ekonomi kabininde iki yolcu çok farklı ücretler ödemiş. Bu farkın sistemdeki temel açıklaması nedir?

## A
Fark satın alma anındaki döviz kuru hareketlerinden doğar; ücret aynıdır ama tahsil edilen tutar kur çevriminde farklılaşır
🤖 kur çevrimi tutarı etkileyebilir ama kabin içi büyük ücret farklarının sistemsel kaynağı fiyat kovalarıdır, kur değildir.

## B
Bu bir fiyatlama tutarsızlığıdır; aynı kabindeki koltuklar aynı üründür ve doğru kurgulanmış bir sistemde aynı fiyattan satılır
🤖 aynı kabin tek ürün değildir; satış tarafında kabin bilinçli olarak farklı fiyat katmanlarına bölünür, bu hata değil tasarımdır.

## C
Kabin, rezervasyon sınıfı (RBD) denen fiyat kovalarına bölünmüştür; ucuz kovalar dolunca satış üst kovadan sürer, farkı gelir yönetimi kurgular
🤖 Doğru: aynı fiziksel kabin, satışta harf kodlu kovalara ayrılır; "fiyat arttı" görünen şey çoğu zaman ucuz kovanın tükenmesidir.

## D
Fark yalnız ek hizmetlerden (bagaj hakkı, koltuk seçimi, ikram) doğar; uçuşun taban ücreti tüm kabinde aynıdır
🤖 ek hizmetler fiyatı farklılaştırır ama taban ücretin kendisi de kovadan kovaya değişir; farkın asıl motoru RBD yapısıdır.

## Doğru
C

## Açıklama
Fiziksel kabin ile satış sınıfı ayrı kavramlardır. Aynı ekonomi kabini, satış tarafında **rezervasyon sınıfı (RBD / fare class)** denen harf kodlu **fiyat kovalarına** bölünür: her kova belirli sayıda koltuğa, belirli bir ücret ve kural setiyle (iade edilebilirlik, değişiklik hakkı) açılır. Erken alan yolcu genellikle ucuz kovadan alır; kova tükenince satış bir üst kovadan devam eder — dışarıdan "fiyat yükseldi" gibi görünen davranışın sistemdeki karşılığı çoğu zaman budur. Hangi kovaya kaç koltuk açılacağını **gelir yönetimi (revenue management)** ayarlar: talep tahminine göre kovalar açılır, kapanır, dengelenir. Bu yüzden fark bir tutarsızlık değil, tasarımın kendisidir — "aynı kabin aynı fiyat olmalı" varsayımı sektörün temel modelini kaçırır. Kur çevrimi ve ek hizmetler tutarı etkileyen ikincil etkenlerdir ama kabin içi büyük farkların ana motoru değildir; taban ücretin kendisi kovadan kovaya değişir. Geliştirici gözüyle kritik sonuç şudur: "kaç koltuk kaldı?" sorusu sınıf bazında sorulur, envanter tek bir sayı değildir ve kova durumu sürekli oynar — bu da uygunluk sorgusunun neden bu kadar dinamik ve yoğun olduğunu açıklar.

# havbt-4 | Orta

## Soru
Seyahat acenteleri üzerinden satışta GDS ile NDC arasındaki ilişki nasıl tanımlanır?

## A
GDS yalnız kargo rezervasyonu taşır, NDC ise yolcu tarafını taşır; ikisi farklı iş alanlarının sistemleridir
🤖 GDS'in işi yolcu dağıtımıdır; kargo ayrı sistemlerin alanıdır ve ayrım iş alanı üzerinden değildir.

## B
GDS, acentelerin çok havayoluna eriştiği aracı dağıtım kanalıdır; NDC ise IATA'nın API tabanlı dağıtım standardıdır
🤖 Doğru: biri kanal (Amadeus, Sabre yaygın örnek), diğeri standart; iki dünya genellikle uzun süre yan yana yaşar.

## C
NDC, GDS firmalarından biridir ve Amadeus ile Sabre'nin rakibi olan üçüncü büyük dağıtım şirketidir
🤖 NDC bir şirket değil IATA'nın tanımladığı bir standarttır; GDS firmalarıyla aynı kategoride sayılamaz.

## D
GDS havayolunun kendi iç rezervasyon sistemidir (PSS ile aynı şey), NDC ise bu sistemin yeni sürümünün adıdır
🤖 PSS havayolunun çekirdek sistemi, GDS ise havayolları dışındaki dağıtım kanalıdır; NDC de kimsenin "yeni sürümü" değildir.

## Doğru
B

## Açıklama
Ayrımın anahtarı kategoridir: **GDS bir kanaldır, NDC bir standarttır.** GDS'ler (Amadeus ve Sabre yaygın örneklerdir) seyahat acentelerinin tek bağlantı üzerinden yüzlerce havayolunu arayıp rezervasyon yapabildiği aracı dağıtım kanallarıdır; tarihsel olarak EDIFACT tarzı mesaj standartları üzerine kuruludur ve çevrimiçi acentelerin (OTA) önemli bölümü bu altyapının üzerinde çalışır. **NDC (New Distribution Capability)** ise IATA'nın tanımladığı **API tabanlı dağıtım standardıdır**: havayolunun teklifi (koltuk, bagaj, paketler dahil) kendi üretip aracıya modern servisler üzerinden sunmasını hedefler. Yanlış şıklar kategorileri karıştırır: GDS kargo sistemi değildir (işi yolcu dağıtımıdır); NDC bir şirket değildir (standarttır); GDS de havayolunun iç sistemi değildir — havayolunun çekirdeği **PSS**'tir, GDS onun dışındaki dağıtım halkasıdır. Mülakattaki olgun duruş, geçiş hakkında kesin oran veya tarih iddiasına girmemektir: "geçiş sektörde kademeli ilerliyor, EDIFACT kökenli kanal ile API tabanlı NDC genellikle yan yana yaşıyor ve entegrasyon işinin önemli bölümü bu iki dünya arasında köprü kurmak" demek hem doğru hem temkinli bir çerçevedir.

# havbt-5 | İleri

## Soru
Yolcunun iki bacaklı e-bileti var. İlk bacağı uçtu; ikinci bacağın tarihini değiştirdi. Kupon durum makinesinde yaygın modellemeyle ne olur?

## A
İlk kupon used/flown kalır; ikinci kupon exchanged olur ve yeni tarih için yeni bir kupon (open) üretilir
🤖 Doğru: gerçekleşmiş uçuşun kuponu dondurulur; değişiklik iptal değil, exchange ve yeni kupon üretimidir.

## B
Bilet bütün olarak refunded yapılır ve iki bacak için sıfırdan yeni bir bilet kesilir; kısmi işlem kupon bütünlüğünü bozar
🤖 uçulmuş bacağı iade etmek gerçekleşmiş olayı geri almak demektir; kupon modeli tam da kısmi işlem için bacak bazlı kurulmuştur.

## C
Kupon durumları hiç değişmez; tarih değişikliği yalnız PNR'daki uçuş bacağını günceller, bilet tarafında kayıt gerekmez
🤖 rezervasyon ve bilet ayrı kayıtlardır; PNR'ı güncelleyip bileti değiştirmemek ikisini tutarsız bırakır ve check-in'de patlar.

## D
İkinci kupon used işaretlenir; sistem gözünde "işlem görmüş" her kupon kullanılmış sayılır ve yeni kupon açılmaz
🤖 used durumu uçuşun gerçekleşmesini temsil eder; değişiklik işlemini kullanım saymak yolcunun hakkını yok eder.

## Doğru
A

## Açıklama
E-bilet, bacak başına bir **kupon** taşır ve her kupon kendi durumunu yaşar — modelin bütün gücü bu bacak bazlı ayrıştırmadadır. İlk bacak uçulduğunda o kupon **used/flown** olur ve dondurulur: gerçekleşmiş bir olaydır, geri açılmaz. İkinci bacağın tarih değişikliği ise **iptal değil exchange'dir**: mevcut kupon **exchanged** işaretlenir, yeni tarih için yeni bir bilet/kupon üretilir ve o **open** durumunda doğar; eski kayıt silinmez, yeni kayda bağlanır — denetim izi ve iade hesapları bu zincire dayanır. Bileti bütün olarak iade etmek yanlıştır çünkü uçulmuş bacak geri alınamaz; kupon modeli tam da "yarısı kullanılmış biletin kalanıyla işlem yapma" ihtiyacı için vardır. Yalnız PNR'ı güncelleyip bilet tarafını değiştirmemek, rezervasyon ile bilet kayıtlarını tutarsız bırakır — check-in'de DCS kuponu yeni uçuşla eşleştiremez. Değişikliği "kullanım" saymak ise durum makinesinin anlamını bozar: used uçuşun gerçekleştiğini temsil eder, işlem görmeyi değil. Geçişlerin tek yönlü ve anlamlarının keskin olması, bu modelin bir **durum makinesi** olarak korunmasının nedenidir; benzer durum makinelerinin mühendislik tarafı `248` numaralı konudadır.

# havbt-6 | İleri

## Soru
Büyük bir fırtına nedeniyle onlarca uçuş iptal edildi (IRROPS). Bu olayın sistemlere bindirdiği asıl yük nedir ve sektör BT'sinin hangi karakterini görünür kılar?

## A
Asıl yük yolculara bildirim göndermektir; rezervasyon ve bilet verisi değişmediği için işlem hacmi düşük kalır
🤖 bildirim işin küçük parçasıdır; asıl yük her etkilenen PNR'ın ve biletin yeniden düzenlenmesidir, veri yoğun biçimde değişir.

## B
Yük yalnız operasyon ekibinin ekranlarına düşer; yolcu sistemleri iptalden etkilenmez çünkü satış ve operasyon ayrı dünyalardır
🤖 iki dünya ayrı olsa da iptal doğrudan satış tarafına taşar; her iptal edilen uçuşun arkasında yeniden yerleştirilecek PNR'lar vardır.

## C
İptal edilen uçuşların kayıtları silinir ve etkilenen yolcuların yeniden rezervasyon yapması beklenir; sistemin ek işi yoktur
🤖 kayıt silmek denetim izini ve yolcu haklarını yok eder; yeniden yerleştirme havayolunun sorumluluğudur ve sistemce yürütülür.

## D
Binlerce PNR'ın yeniden yerleştirilmesi gerekir; bu tepe yük, 7/24 çalışan ve entegrasyon-yoğun sektör BT'sinin karakterini görünür kılar
🤖 Doğru: bir iptal dalgası, normal günün kat kat üstünde işlem üretir; bağlantılar ve interline yolcular işi daha da katlar.

## Doğru
D

## Açıklama
IRROPS, havacılık BT'sinin "normal günü hafif, kötü günü çok ağır" karakterinin en net örneğidir. Bir uçuş iptal edildiğinde arkasındaki yüzlerce PNR **yeniden yerleştirilmelidir** (reaccommodation): alternatif uçuş bulunur, PNR'daki bacaklar değiştirilir, biletler exchange edilir, bağlantılı uçuşları olan yolcuların tüm zinciri yeniden kurulur; interline/codeshare yolcularında başka havayollarının sistemleriyle de mutabakat gerekir. Onlarca uçuşluk bir dalga, normal günün kat kat üstünde işlem hacmi üretir — üstelik tam da herkesin aynı anda uygulamaya ve çağrı merkezine yüklendiği anda. Bu senaryo sektörün dört karakterini aynı anda görünür kılar: sistem **7/24** ayakta kalmalı, **tepe yüke** göre düşünülmeli, **eski ve yeni** sistemler (host dünyası ile modern API'ler) birlikte çalışmalı ve düzinelerce dış sistemle **entegrasyon** yürümelidir. Bildirim göndermek işin yalnız küçük parçasıdır; kayıt silmek ise denetim izini ve yolcu haklarını yok eden, gerçek sistemlerde yeri olmayan bir yaklaşımdır. Operasyon-satış ayrımı da yükü izole etmez: iptal operasyon dünyasında doğar ama faturası satış dünyasının verisine kesilir. Bu yükün mühendislik karşılığı — toplu iş ve kuyrukla yük düzleştirme — `248` numaralı konuda işlenir.
