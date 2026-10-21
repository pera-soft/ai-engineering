---
slug: sozlu-prova-mimari-dagitik
baslik: "Sözlü Prova · Mimari ve Dağıtık Sistemler: Servis Sınırı, Paketleme, Darboğaz ve Saga"
kademe: İleri
sira: 264
etiketler: 11
---

## Konu anlatımı

Bu dosyada dört gerçek mülakat sorusu var. Dördü de açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur. Dört soru aynı eksende ilerler: nereye sınır çizersin, kodu nasıl bölersin, neyi ölçersin, sınır aşılınca tutarlılığı nasıl korursun.

### "Mikroservis mimarisinde servis sınırlarını nasıl belirlersiniz, hangi kriterlerle ayrıştırma yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu soru tanım istemiyor, muhakeme istiyor; görüşmeci mikroservisin ne olduğunu zaten biliyor. Asıl merak ettiği şu: bu aday sınırı hangi eksende çiziyor. Zayıf aday teknik eksende böler, güçlü aday iş ekseninde böler ve bölmenin faturasını da söyler. Ağırlık muhakemededir; deneyim ikinci sıradadır ama varsa değerini iki katına çıkarır. Bir ayrıştırmanın faturasını görmüş aday bunu cümlelerinden belli eder.

**Sözlü cevap iskeleti**

1. **Ekseni ilk cümlede söyle.** Sınır teknik katmandan değil, iş yeteneğinden geçer. Sipariş, ödeme ve stok birer sınırdır; "servis katmanı" bir sınır değildir.
2. **Sınırlı bağlamı (bounded context) getir.** Aynı kelime iki yerde farklı şeye karşılık gelebilir. Müşteri satışta başka bir şeydir, faturalamada başka. Bu ayrım güçlü bir sınır işaretidir (`139`, `138`).
3. **Veri sahipliğini şart koş.** Bir tabloya tek servis yazar. İki servis aynı tabloyu paylaşıyorsa ortada tek servis ve iki dağıtım vardır (`97`).
4. **Değişim birlikteliğini ölçüt yap.** Birlikte değişen şeyler birlikte durmalıdır. Her iş kaleminde iki servisi birden değiştiriyorsan sınır yanlış yerdedir.
5. **Ekip yapısını hesaba kat.** Conway yasası, sistemin onu kuran ekiplerin iletişim yapısına benzediğini söyler. Bir servisin sahibi tek ekip olmalıdır (`95`).
6. **Yanlış ölçütleri say ve bedeli ver.** Katman başına, tablo başına ya da teknoloji başına bölmek yanlıştır. Çıkan şeyin adı dağıtık monolittir; ağın maliyeti alınır, bağımsızlık alınmaz (`96`).

```svg
<svg viewBox="0 0 660 314" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Servis siniri: yanlis eksen, dogru eksen ve sinir saglamasi">
  <defs>
    <marker id="sozlu3-sinir" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sınır teknik katmandan değil, iş yeteneğinden geçer</text>
  <rect x="14" y="36" width="192" height="190" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="110" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış eksen</text>
  <text x="110" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">katman başına servis</text>
  <text x="110" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tablo başına servis</text>
  <text x="110" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">teknoloji başına servis</text>
  <text x="110" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kod büyüdü diye bölmek</text>
  <text x="110" y="170" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">her istek birkaç servisi dolaşır</text>
  <text x="110" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">veri sahibi ortada kalır</text>
  <text x="110" y="206" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">birlikte dağıtım zorunlu olur</text>
  <line x1="208" y1="131" x2="228" y2="131" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu3-sinir)"/>
  <rect x="232" y="36" width="196" height="190" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Doğru eksen</text>
  <text x="330" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş yeteneği: sipariş, ödeme, stok</text>
  <text x="330" y="106" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınırlı bağlam (bounded context)</text>
  <text x="330" y="130" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veri sahipliği tek serviste</text>
  <text x="330" y="154" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">birlikte değişen birlikte durur</text>
  <text x="330" y="178" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek ekip sahipliği (Conway)</text>
  <text x="330" y="205" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">isimler iş dilinden gelir, koddan değil</text>
  <line x1="430" y1="131" x2="450" y2="131" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu3-sinir)"/>
  <rect x="454" y="36" width="192" height="190" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="550" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sınır sağlaması</text>
  <text x="550" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek başına dağıtılabiliyor mu?</text>
  <text x="550" y="106" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kendi verisine sahip mi?</text>
  <text x="550" y="130" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her istekte komşuya soruyor mu?</text>
  <text x="550" y="154" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sözleşmesi dar mı?</text>
  <text x="550" y="182" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">cevaplar tutmuyorsa</text>
  <text x="550" y="200" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sınır erken çizilmiş demektir</text>
  <rect x="14" y="238" width="632" height="62" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="258" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Yanlış sınırın adı: dağıtık monolit</text>
  <text x="330" y="277" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Ağın gecikmesi ve hata olasılığı satın alınır; bağımsız dağıtım ve bağımsız ölçek alınmaz.</text>
  <text x="330" y="293" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Ayrıştırma geri dönmesi pahalı bir karardır; önce modüler monolit denenir.</text>
</svg>
```

**Elenme sebepleri**

- "Sunum, iş kuralı ve veri erişimi ayrı servis olur." Katman ekseninde bölmedir; bu soruda en hızlı eleyen cevaptır.
- Her tabloya bir servis vermek. Servis sayısı artar, bağımsızlık artmaz; her iş için birkaç servis birden değişir.
- Veri sahipliğinden hiç söz etmemek. Paylaşılan tablo, sınırı görünmez biçimde ortadan kaldırır.
- "Mikroservis her zaman daha iyidir" demek. Takas cümlesi kurmayan aday kararı değil modayı anlatıyor sayılır.
- İşletme maliyetini hiç anmamak. Dağıtım, izleme, sürümleme ve hata ayıklama maliyeti sınır kararının parçasıdır.
- Ekip yapısını dışarıda bırakmak. Tek ekibin sahiplendiği beş servis çoğu zaman bir servis gibi davranır.
- Ayrıştırmanın nasıl yapılacağını hiç söylememek. Kademeli geçiş yerine tek seferde bölme planı riskli bulunur.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Hangi sistemde sınır tartışması yaşandı? Yeni bir servis mi çıkarıldı, yoksa var olan bir servis mi bölündü?
- Sınır hangi ölçütle çizildi? İş yeteneği mi, ekip yapısı mı, yoksa yalnız "dosya çok büyüdü" mü?
- Paylaşılan tablo var mıydı? Varsa hangi servis yazıyordu, hangileri okuyordu?
- Bölmeden sonra ne zorlaştı? Yerel hata ayıklama, uçtan uca test, sürüm uyumu, veri tutarlılığı?
- Birlikte dağıtım zorunluluğu doğdu mu? İki servisi aynı anda çıkarmak gerekiyorsa bu bir sınır belirtisidir.
- Geri adım atıldı mı? İki servisi birleştirmek de anlatılabilir bir karardır ve olgunluk gösterir.
- Ne ölçüldü? Dağıtım sıklığı, olay sayısı, uçtan uca gecikme veya bir ekibin bekleme süresi.

Böyle bir deneyimin yoksa uydurma; bunun yerine `256`'daki dürüst köprüyü kur: sınırı söyle, komşu deneyimini somutla, farkı adlandır, kapatma planını ver. Modüler bir monolitte paket sınırı çizmiş olmak da gerçek bir cevaptır. Yeter ki onu mikroservis ayrıştırması gibi anlatma.

### "Bağımlılık yönetimi ve modüler tasarım için nasıl bir paketleme yaklaşımı izlersiniz?"

**Soru aslında ne ölçüyor?**

Soru küçük görünür ama çok şey söyler. Paketleme bir zevk meselesi değildir; bağımlılık yönünü ve değişim maliyetini belirler. Görüşmeci burada iki şeye bakar: aday paket düzenini bir tasarım kararı olarak görüyor mu ve sınırı yalnız isimle mi kuruyor. İkinci soru asıl ayrıştırıcıdır, çünkü isimle kurulan sınır birkaç ay içinde delinir. Ağırlık muhakemededir; cevabın gücü somut örnekten gelir.

**Sözlü cevap iskeleti**

1. **İki seçeneği adlandır.** Katmana göre paketleme `controller`, `service`, `repository` klasörleri üretir. Özelliğe göre paketleme `siparis`, `odeme`, `stok` klasörleri üretir.
2. **Farkı değişim üzerinden anlat.** Yeni bir alan eklendiğinde katmanlı düzende dört klasör açarsın. Özellik düzeninde tek klasörde kalırsın; özelliği silmek de kolaylaşır.
3. **Görünürlüğü kullan.** Her özellik dışarıya dar bir arayüz açar, kalan sınıflar paket içi kalır. `public` olan her sınıf bir sözleşmedir; sayısını bilerek az tut (`137`).
4. **Bağımlılık yönünü tek taraflı kur.** Özellikler birbirine değil ortak çekirdeğe bakar. İş kuralı altyapıya bağlanmaz; bağımlılık içe doğru akar (`136`). Döngüsel bağımlılık yasaktır ve bunu mimari testle zorlarsın.
5. **Modüler monoliti ara yol olarak sun.** Modül sınırı önce pakette çizilir, gerekirse ayrı derleme birimine taşınır. Sınır burada tutuyorsa ayrıştırma sonra da yapılabilir (`96`).
6. **Sürüm yönetimini ekle.** Sürümleri tek yerden yönet: üst proje ve BOM ile `dependencyManagement`. Geçişli bağımlılıklara dikkat et; aynı kütüphanenin iki sürümü çalışma anında patlar.

**Elenme sebepleri**

- Soruyu klasör tercihi sanmak. "Alışkanlık meselesi" cevabı, bağımlılık yönü kavramını hiç kurmadığını gösterir.
- Her sınıfı `public` yapmak ve buna hiç dikkat etmemek; görünürlük daralmadan modülerlikten söz edilemez.
- Özelliğe göre paketlemeyi savunup örnek verememek. Ad bilmek yetmez; hangi klasörde ne durur, söylenmeli.
- Bağımlılık döngüsünü zararsız saymak. Döngü, iki modülü tek modüle çevirir ve ayrıştırmayı imkânsız kılar.
- Sürümleri her modülde ayrı yazmak. Çakışan sürümler derlemede değil, üretimde kendini gösterir.
- Katmanlı düzeni kötülemek ve mutlak konuşmak. Küçük ve tek amaçlı serviste katmanlı düzen hâlâ makuldür.
- Kuralı yalnız belgeye yazmak. Denetlenmeyen mimari kuralı birkaç ay içinde delinir.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Şu an çalıştığın projede paketler neye göre ayrılmış? Katmana göre mi, özelliğe göre mi, karışık mı?
- Yeni bir özellik eklerken kaç ayrı klasöre dokunuyorsun? Bu sayı cevabının en somut kanıtıdır.
- Hiç düzeni değiştirdin mi? Neyi taşıdın, taşırken hangi bağımlılık seni durdurdu?
- Döngüsel bağımlılıkla karşılaştın mı? Nasıl fark ettin, nasıl kırdın?
- `public` olmaması gereken bir sınıf dışarıdan kullanıldı mı? Sonucunda ne oldu?
- Sürüm çakışması yaşadın mı? Belirti neydi: derleme hatası mı, çalışma anında `NoSuchMethodError` mü?
- Kuralı denetleyen bir şey var mı? Mimari test, statik analiz kuralı ya da inceleme alışkanlığı.

Deneyimin yoksa uydurma. `256`'daki köprü burada da işler. Kendi küçük projende paketleri özelliğe göre kurup farkı gözlemlemek gerçek bir gözlemdir ve mülakatta dürüstçe öyle anlatılır.

### "Yüksek trafikli bir uygulamada darboğazları nasıl tespit eder, iyileştirmeleri nasıl önceliklendirirsiniz?"

**Soru aslında ne ölçüyor?**

Bu soru iki beceriyi birden ölçer: teşhis ve karar. Teşhis kısmında görüşmeci tek şeye bakar; bu aday tahminle mi çalışıyor, ölçerek mi. Önceliklendirme kısmı ise daha az adayın hazırlıklı geldiği yerdir. Bulduğun üç sorunu sıraya koyamıyorsan zamanı en görünür olana harcarsın. Ağırlık yöntemdedir; anlattığın sıra, bildiğin araç listesinden daha çok konuşur.

**Sözlü cevap iskeleti**

1. **Kuralı en başta koy.** Önce ölç, sonra değiştir. Ölçmeden yapılan iyileştirme bir tahmindir ve çoğu zaman yeni sorun üretir.
2. **Hedefi tanımla.** Hangi uç nokta, hangi yük altında, hangi hedef. "Yavaş" bir duygudur; "p95 300 ms altında olmalı" bir ölçüttür (`151`).
3. **Ölçüm sırasını anlat.** Metrik nerede yavaş olduğunu söyler, iz (trace) hangi adımda kaybedildiğini gösterir, profilleyici hangi kodun yediğini bulur (`104`, `103`, `64`).
4. **Ortalamayı reddet.** Ortalama saklar, yüzdelik dilim gösterir. Şikâyet eden kullanıcı p99'dadır; hedefler yüzdelik dilimle yazılır (`152`).
5. **En sık üç darboğazı say.** Veri tabanı erişimi, N+1 gibi getirme hataları, kilit ve havuz çekişmesi. Genellikle sorun kodun hızında değil, bekleme sürelerindedir.
6. **Önceliklendirme ölçütünü ver.** Etki bölü maliyet. Bir seferde tek değişken değiştirilir, sonra aynı ölçüm tekrarlanır. Kalıcı çözüm bir alarmla ya da testle kilitlenir.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Performans teshisi: metrik, iz ve profilleyici sirasi, yuzdelik dilim ve onceliklendirme">
  <defs>
    <marker id="sozlu3-olcum" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önce ölç, sonra değiştir — her adım aramayı daraltır</text>
  <rect x="14" y="36" width="196" height="124" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Metrik</text>
  <text x="112" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">nerede yavaş?</text>
  <text x="112" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uç nokta, hata oranı, süre</text>
  <text x="112" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">havuz doluluğu, kuyruk boyu</text>
  <text x="112" y="144" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arama alanı: tüm sistem</text>
  <line x1="212" y1="98" x2="228" y2="98" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu3-olcum)"/>
  <rect x="232" y="36" width="196" height="124" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · İz (trace)</text>
  <text x="330" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hangi adım yavaş?</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">servis servis süre dağılımı</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">correlation-id ile uçtan uca</text>
  <text x="330" y="144" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arama alanı: tek servis</text>
  <line x1="430" y1="98" x2="446" y2="98" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu3-olcum)"/>
  <rect x="450" y="36" width="196" height="124" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Profilleyici</text>
  <text x="548" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hangi kod, hangi kilit?</text>
  <text x="548" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işlemci, ayırma, kilit çekişmesi</text>
  <text x="548" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yığın dökümü ve GC logu</text>
  <text x="548" y="144" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arama alanı: tek çağrı yolu</text>
  <rect x="14" y="176" width="316" height="110" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Ortalama saklar, yüzdelik dilim gösterir</text>
  <text x="172" y="220" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ortalama 120 ms iken p99 iki saniye olabilir</text>
  <text x="172" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">şikâyet eden kullanıcı kuyruğun ucundadır</text>
  <text x="172" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hedef p95 ve p99 ile yazılır</text>
  <text x="172" y="278" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek sayı yerine dağılıma bak</text>
  <rect x="344" y="176" width="302" height="110" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="495" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Önceliklendirme: etki bölü maliyet</text>
  <text x="495" y="220" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">en sık üç darboğaz: veri tabanı, N+1, kilit</text>
  <text x="495" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bir seferde tek değişken değiştirilir</text>
  <text x="495" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değişiklikten sonra aynı ölçüm tekrarlanır</text>
  <text x="495" y="278" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">kalıcı çözüm bir alarm ya da testle kilitlenir</text>
</svg>
```

**Elenme sebepleri**

- Doğrudan çözüme atlamak. "Önbellek koyarım" cevabı, ölçmeden karar veren bir mühendis izlenimi bırakır.
- Yalnız ortalamadan konuşmak. Yüzdelik dilimi hiç anmayan aday kuyruk davranışını görmüyor demektir.
- Sunucu büyütmeyi ilk seçenek yapmak. Kaynak eklemek belirtiyi öteler, kök nedeni gizler ve maliyeti kalıcılaştırır.
- Aynı anda birkaç değişikliği birden yapmak. Sonuç iyileşse bile hangisinin işe yaradığı bilinmez.
- Ölçümü yalnız yerelde yapmak. Yerel makinede veri kümesi ve eşzamanlılık üretimden farklıdır.
- Bulguları sıraya koymamak. Önceliklendirme ölçütü söylenmezse cevap teknik ama yönsüz kalır.
- Sonucu sayı ile söylememek. "Belirgin biçimde hızlandı" cümlesi ölçüm değil, izlenimdir.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası iki sayıdır: önce ne, sonra ne. Onları bul:

- Hangi uygulamada yük konuşuldu? Kampanya, gün sonu, ay sonu ya da toplu iş penceresi var mıydı?
- Belirti neydi? Yanıt süresinde tırmanma mı, zaman aşımı mı, kuyruk birikmesi mi, hata oranında artış mı?
- Nasıl fark edildi? Alarm mı, gösterge panosu mu, çağrı merkezine gelen şikâyet mi?
- Neye baktın ve hangi sırayla? Metrik, log, iz, profilleyici; hangisi seni asıl yere götürdü?
- Kök neden neydi? Eksik indeks, N+1, kilit çekişmesi, küçük bağlantı havuzu, dış servisin yavaşlaması?
- Hangi seçeneği seçtin ve neyi ertelemeye karar verdin? Ertelediğin işi neden ertelediğini söylemek güçlü bir cümledir.
- Ne ölçüldü? p95 kaçtan kaça indi, hata oranı ne oldu, sunucu sayısı değişti mi?

Üretimde böyle bir vakan yoksa uydurma. Bu soruda gerçek gözlem üretmek kolaydır. Kendi projende bir uç noktaya yük ver, p95 değerini ölç, bir değişiklik yap ve tekrar ölç. Gördüğün fark gerçek bir ölçümdür. `256`'daki köprüyle bunu dürüstçe anlatabilirsin.

### "Dağıtık sistemlerde veri tutarlılığı için hangi stratejiler; saga desenini nasıl kurgularsınız?"

**Soru aslında ne ölçüyor?**

Bu dosyanın en ayrıştırıcı sorusu budur. Saganın tanımını çoğu aday söyler. Ayrım üç yerde çıkar ve üçü de tanımdan sonra gelir. Birincisi, tutarlılık seviyesinin teknik değil bir iş kararı olduğunu söyleyebilmek. İkincisi, telafi işlemini geri alma sanmamak ve aradaki farkı adlandırmak. Üçüncüsü, saganın tek başına yetmediğini bilmek; giden kutusu ve tekrar güvenliği olmadan saga sessizce bozulur. Ağırlık muhakemededir.

**Sözlü cevap iskeleti**

1. **Kararı işe bağla.** Önce sorulacak soru şudur: bu veri kaç saniye eski kalabilir. Cevap iş tarafından gelir; stok, bakiye ve kampanya için farklıdır.
2. **Sınırı çiz.** Tek veri tabanında işlem (transaction) ile ACID alırsın. Sınır aşılınca elindeki araç değişir; ağ, gecikme ve kısmi başarısızlık denkleme girer (`128`).
3. **2PC'yi neden seçmediğini söyle.** İki fazlı kesinleştirme kilitleri uzun tutar, koordinatörü tek arıza noktası yapar ve iş hacmini düşürür. Bu üç bedel yüzünden mikroservis ortamlarında yaygın bir tercih değildir.
4. **Sagayı tanımla.** Saga, yerel işlemler zinciridir. Her adım kendi veri tabanında kesinleşir; bir adım başarısız olursa öncekiler telafi edilir (`126`).
5. **Telafiyi doğru anlat.** Telafi bir `ROLLBACK` değildir. Yeni bir iş işlemidir: iade kaydı, iptal kaydı, stok geri alma. Geçmiş silinmez, yalnız etkisi ters yönde bir kayıtla dengelenir.
6. **Koreografi ile orkestrasyonu ayır.** Az adımda koreografi hızlıdır; olayı dinleyen servisler kendi işini yapar. Adım sayısı ve dallanma arttıkça orkestrasyon okunur kalır, çünkü akış tek yerde durur.
7. **Sagayı ayakta tutan iki şeyi ekle.** Giden kutusu tablosu (outbox) yazma ile olayı aynı işleme alır (`116`). Tekrar edilse de sonucu değişmeyen (idempotent) tüketici aynı mesajı iki kez işlemez (`130`). Ayrıca saga izolasyon vermez: ara durum dışarıdan görünür, bu yüzden iş tarafına "beklemede" gibi bir durum konur (`127`).

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Saga: yerel islem zinciri, telafi adimlari, koreografi ve orkestrasyon secimi">
  <defs>
    <marker id="sozlu3-saga" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="sozlu3-saga-geri" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Saga: yerel işlemler zinciri, geri dönüş yolu telafi ile kurulur</text>
  <rect x="23" y="36" width="140" height="42" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="93" y="62" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1 · sipariş oluştur</text>
  <line x1="165" y1="57" x2="179" y2="57" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu3-saga)"/>
  <rect x="181" y="36" width="140" height="42" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="251" y="62" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">2 · ödeme al</text>
  <line x1="323" y1="57" x2="337" y2="57" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu3-saga)"/>
  <rect x="339" y="36" width="140" height="42" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="409" y="62" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">3 · stok düş — başarısız</text>
  <line x1="481" y1="57" x2="495" y2="57" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="4 3"/>
  <rect x="497" y="36" width="140" height="42" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="567" y="62" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">4 · kargo aç (koşmaz)</text>
  <rect x="23" y="104" width="140" height="42" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="93" y="130" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">telafi: siparişi iptal et</text>
  <rect x="181" y="104" width="140" height="42" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="251" y="130" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">telafi: ödemeyi iade et</text>
  <line x1="179" y1="125" x2="167" y2="125" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#sozlu3-saga-geri)"/>
  <line x1="395" y1="125" x2="325" y2="125" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#sozlu3-saga-geri)"/>
  <text x="409" y="98" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">buradan geriye telafi</text>
  <text x="510" y="130" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">telafi geri alma değildir;</text>
  <text x="510" y="146" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">etkiyi dengeleyen yeni iştir</text>
  <rect x="14" y="164" width="316" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="185" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Koreografi</text>
  <text x="172" y="207" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her servis olayı dinler, kendi adımını atar</text>
  <text x="172" y="227" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">az adımda hızlı ve gevşek bağlı</text>
  <text x="172" y="247" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">akış tek yerde görünmez, izlemek zorlaşır</text>
  <rect x="344" y="164" width="302" height="98" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="185" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Orkestrasyon</text>
  <text x="495" y="207" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koordinatör adımları sırayla çağırır</text>
  <text x="495" y="227" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">dallanma ve telafi sırası görünür kalır</text>
  <text x="495" y="247" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">koordinatör yeni bir bileşendir, o da izlenir</text>
  <rect x="14" y="270" width="632" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="290" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Bu ikisi olmadan saga sessizce bozulur</text>
  <text x="330" y="310" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">giden kutusu: yazma ile olay aynı işlemde · tekrar güvenliği: aynı mesaj iki kez gelirse sonuç değişmez</text>
</svg>
```

**Elenme sebepleri**

- Telafiyi geri alma sanmak. "Saga dağıtık rollback'tir" cümlesi bu soruda en sık eleyen hatadır.
- Tutarlılık seviyesini teknik karar saymak. Hangi verinin ne kadar eski kalabileceğini iş tarafı belirler.
- Giden kutusundan hiç söz etmemek. Veri tabanına yazıp sonra mesaj yayımlamak sessiz veri kaybı üretir.
- Tekrar güvenliğini atlamak. Mesaj altyapıları genellikle en az bir kez teslim eder; aynı mesaj iki kez gelir.
- Koreografi ile orkestrasyonu ölçütsüz seçmek. Seçim adım sayısına, dallanmaya ve izlenebilirlik ihtiyacına bağlanmalı.
- Saganın izolasyon verdiğini sanmak. Ara durum dışarıdan görünür; iş akışında bunun karşılığı tanımlanmalıdır.
- Telafisi olmayan adımı hesaba katmamak. Gönderilen e-posta geri alınamaz; böyle adımlar zincirin sonuna konur.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir. Şu sorular onu çıkarır:

- Hangi akışta iki ayrı sistemi birlikte güncellemek gerekti? Ödeme, stok, muhasebe kaydı, bildirim?
- Bir taraf yazıp diğeri yazamadığında ne oluyordu? Belirti neydi: eksik kayıt mı, çift kayıt mı, mutabakat farkı mı?
- Nasıl fark edildi? Gün sonu mutabakatı mı, müşteri şikâyeti mi, uyuşmayan iki rapor mu?
- Nasıl onarıldı? Elle düzeltme mi yapıldı, yoksa telafi eden bir iş mi yazıldı?
- Mesaj kuyruğu var mıydı? Olay yayımlama veri tabanı yazmasıyla aynı işlemde miydi?
- Aynı mesajın iki kez işlendiği oldu mu? Tekrarı hangi anahtarla engellediniz?
- Kullanıcıya ara durum gösterildi mi? "Beklemede" ya da "onay bekliyor" gibi bir durum var mıydı?

Bu deneyim yoksa uydurma; burası uydurmanın en çabuk yakalandığı sorudur. `256`'daki köprüyü kur. Tek veri tabanında işlem sınırı yönetmiş olmak da anlatılabilir bir temeldir. Ondan sonra "dağıtıkta bu araç yetmez, çünkü şu değişir" diyerek muhakemeni gösterirsin.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Önce iskeleti okuma. Soruyu yüksek sesle oku, sonra dosyayı kapat.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek prova sayılmaz; ağız ile beyin aynı hızda çalışmaz.
- Sonra iskeletle karşılaştır. Hangi adımı atladın, işaretle.
- Bu dört soruda en sık atlanan adım takas cümlesidir. Seçtiğin şeyin bedelini söylemezsen cevap reklam gibi duyulur.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- Sayıyı ezberle, cümleyi ezberleme. Ezberlenmiş cümle mülakatta ezber gibi duyulur.
- Dört soruyu üst üste yapma. Arada ara ver; yorgun ses ikna etmez.
- Ölçüt şudur: bir sorunun adımlarını kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Tek cümlede

Bu dört soru mimari bilgiyi değil, mimari kararı anlatma becerisini ölçer. Birincisinde kazanan cümle en baştadır: sınır teknik katmandan değil, iş yeteneğinden geçer. İkincisinde paketleme bir zevk değil, bağımlılık yönü kararı olarak sunulur. Üçüncüsünde yöntem sırası her araç listesinden güçlüdür: metrik, iz, profilleyici. Dördüncüsünde ayrım telafinin geri alma olmadığını ve saganın giden kutusuyla tekrar güvenliğine dayandığını söylemekle kurulur. Dördünde de aynı disiplin geçerlidir: seçeneği söyle, ölçütü söyle, bedeli söyle, ölçülen sonucu söyle. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# sozlu3-1 | Orta
## Soru
Bir ekip monoliti bölmek istiyor ve şunu öneriyor: "Sunum, iş kuralı ve veri erişimi ayrı birer servis olsun." Bu önerinin temel kusuru nedir?
## A
Bölme ekseni teknik katmandır; her istek üç servisi dolaşır, veri sahibi belirsizleşir
🤖 Doğru: katman sınırı bir iş sınırı değildir; bağımsızlık kazanılmaz.
## B
Bölme doğrudur ama servis sayısı azdır; en az beş servise çıkılması beklenir
🤖 servis sayısı bir ölçüt değildir; eksen yanlışsa sayı artınca sorun büyür.
## C
Sorun yalnızca isimlendirmededir; katman adları iş terimleriyle değiştirilirse öneri düzelir
🤖 ad değişikliği bağımlılık yönünü ve veri sahipliğini değiştirmez.
## D
Öneri doğrudur; katmanların ayrı dağıtılması bağımlılıkları kendiliğinden azaltır ve ölçeklemeyi kolaylaştırır
🤖 ayrı dağıtım bağımlılığı kaldırmaz, yalnız araya ağ koyar.
## Doğru
A
## Açıklama
Bu öneri katman eksenindedir ve mikroservis sınırının en yaygın yanlışıdır. Sınır **iş yeteneği** ekseninde çizilir: sipariş, ödeme, stok. Katman ekseninde bölünürse her kullanıcı isteği üç servisi birden dolaşır. Bu, gecikmeyi ve kısmi başarısızlık ihtimalini artırır ama karşılığında hiçbir bağımsızlık vermez. Daha ağır sonuç veri tarafındadır: veriye yalnız bir servis erişir, iş kuralı ona sormak zorunda kalır ve sahiplik dağılır (`97`). Çıkan yapının adı dağıtık monolittir; üç servis birlikte değişir, birlikte dağıtılır (`95`). C şıkkı sorunu isimlendirmeye indirger; oysa kusur bağımlılık yönündedir. D şıkkı ayrı dağıtımı bağımsızlık sanır. Doğru yaklaşım sınırlı bağlamları bulmak, veri sahipliğini tek serviste toplamak ve ayrıştırmadan önce modüler monoliti denemektir (`96`, `139`).

# sozlu3-2 | İleri
## Soru
Bir Spring Boot projesinde paketler `controller`, `service`, `repository`, `dto` biçiminde ayrılmış. Ekip modülerliği artırmak istiyor. En yerinde değişiklik hangisidir?
## A
Paket sayısını azaltıp tüm sınıfları tek pakette toplamak; böylece bağımlılık yönü sadeleşir
🤖 tek paket sınır bırakmaz; her sınıf her sınıfı görür.
## B
Her katmanı ayrı bir derleme modülüne taşımak; katman sınırı böylece derleyiciyle zorlanmış olur
🤖 yanlış ekseni sertleştirir; katman sınırı iş sınırı değildir.
## C
Paketleri özelliğe göre kurmak; her özellik kendi paketinde, dışa yalnız dar bir arayüz açar
🤖 Doğru: değişiklik tek klasörde kalır, görünürlük daralır.
## D
Tüm sınıfları `public` bırakıp aralarındaki çağrıları yalnız kod incelemesinde denetlemek
🤖 insana bağlı denetim kalıcı sınır üretmez; kural birkaç ayda delinir.
## Doğru
C
## Açıklama
Katmana göre paketlemede tek bir özelliğin parçaları dört ayrı klasöre dağılır. Yeni bir alan eklendiğinde dört yere dokunursun; özelliği silmek ise neredeyse imkânsızlaşır. **Özelliğe göre paketlemede** ise `siparis` paketi kendi denetleyicisini, servisini ve deposunu içinde tutar. Kazanç iki tanedir. Birincisi, değişiklik tek klasörde kalır. İkincisi, sınıfların çoğu paket içi (package-private) kalabilir; dışa yalnız dar bir arayüz açılır. `public` olan her sınıf bir sözleşmedir ve sayısı bilerek az tutulur (`137`). B şıkkı ekseni değiştirmeden sınırı sertleştirir; katmanları ayrı modüllere koymak yanlış bölmeyi kalıcı hâle getirir. A şıkkı sınırı tamamen kaldırır. D şıkkı denetimi insana bırakır. Doğru düzende bağımlılık yönü tek taraflıdır: özellikler birbirine değil ortak çekirdeğe bakar, iş kuralı altyapıya bağlanmaz (`136`). Sınır pakette tutuyorsa aynı yapı sonradan modüler monolite ve gerekirse ayrı servise taşınabilir (`96`).

# sozlu3-3 | İleri
## Soru
Bir uç noktanın yavaş olduğu bildirildi. Gösterge panosunda ortalama yanıt süresi 120 ms görünüyor, ama şikâyetler sürüyor. İlk adım ne olmalıdır?
## A
Sunucu sayısını artırıp yeniden ölçmek; bu tür şikâyetlerin kaynağı genellikle yetersiz kaynaktır
🤖 kaynak eklemek belirtiyi öteler; kök neden ölçülmeden bilinemez.
## B
Yüzdelik dilimlere bakmak; ortalama iyi görünürken p95 ve p99 çok kötü olabilir
🤖 Doğru: şikâyet eden kullanıcı dağılımın ucundadır.
## C
Kodu okuyup en karmaşık metodu bulmak ve orayı yeniden yazmak
🤖 karmaşıklık yavaşlığın kanıtı değildir; ölçüm olmadan tahmin edilir.
## D
Veri tabanına indeks eklemek; yavaş uç noktaların kaynağı çoğunlukla eksik indekstir
🤖 sık bir neden olması ilk adım olmasını gerektirmez; önce ölçülür.
## Doğru
B
## Açıklama
Ortalama, dağılımı **saklayan** bir sayıdır. İsteklerin yüzde doksan beşi 40 ms sürerken yüzde biri iki saniye sürebilir; ortalama yine iyi görünür. Şikâyet eden kullanıcı tam olarak o uçtadır. Bu yüzden hedefler yüzdelik dilimle yazılır: "p95 300 ms altında" ölçülebilir bir ifadedir (`151`, `152`). Yüzdelik dilime baktıktan sonra ölçüm sırası daralarak ilerler. Metrik nerede yavaşladığını söyler, iz (trace) hangi adımda zamanın kaybolduğunu gösterir, profilleyici hangi kodun ya da hangi kilidin yediğini bulur (`104`, `103`, `64`). A ve D şıkları ölçmeden çözüme atlar; ikisi de bazen işe yarar ama ikisi de tahmindir. A ayrıca maliyeti kalıcılaştırır. C şıkkı karmaşıklığı yavaşlıkla karıştırır; çoğu gecikme kodun hızından değil beklemeden gelir. Sözlü cevapta bu soru tek cümleyle açılır: önce ölç, sonra değiştir.

# sozlu3-4 | İleri
## Soru
Bir saga zincirinde üçüncü adım başarısız oldu ve önceki adımların etkisi geri alınacak. Telafi işlemi (compensation) hakkında en doğru ifade hangisidir?
## A
Telafi, veri tabanı `ROLLBACK` komutunun dağıtık sürümüdür; önceki adımların izleri tamamen silinir
🤖 yerel işlemler çoktan kesinleşti; silinecek bir işlem kalmadı.
## B
Telafi yalnız son başarılı adım için yazılır; daha önceki adımlar kalıcı olduğu için dokunulmaz
🤖 zincirin tamamı geriye doğru telafi edilir, tek adım yetmez.
## C
Telafi gerekmez; her adım kendi işlemini açık tutarsa saga sonunda tek seferde geri alınır
🤖 işlemi adımlar boyunca açık tutmak sagayı dağıtık kilide çevirir.
## D
Telafi yeni bir iş işlemidir; etkiyi iptal kaydıyla dengeler, geçmişi silmez
🤖 Doğru: iade, iptal ve stok geri alma birer iş işlemidir.
## Doğru
D
## Açıklama
Sagada her adım kendi veri tabanında **kesinleşir**. Kesinleşmiş bir işlemi geri almak mümkün değildir; bu yüzden telafi bir `ROLLBACK` değildir. Telafi yeni bir iş işlemidir: ödeme iade kaydı, sipariş iptal kaydı, rezerve stokun serbest bırakılması. Geçmiş silinmez, etkisi dengelenir. Bu ayrım muhasebe diliyle de örtüşür ve denetlenebilirliği artırır. A şıkkı en sık duyulan hatadır. B şıkkı telafinin zincir boyunca geriye doğru ilerlediğini kaçırır. C şıkkı işlemleri adımlar boyunca açık tutmayı önerir; bu, iki fazlı kesinleştirmenin bilinen sorunlarını geri getirir: uzun süren kilitler, tek arıza noktası ve düşen iş hacmi (`128`). Cevabın devamında iki nokta daha söylenir. Birincisi, telafisi olmayan adımlar zincirin sonuna konur; gönderilmiş bir e-posta geri alınamaz. İkincisi, saga izolasyon vermez: ara durum dışarıdan görünür ve iş akışında "beklemede" gibi bir durumla karşılanır (`126`, `127`).

# sozlu3-5 | Uzman
## Soru
Bir servis önce veri tabanına yazıyor, sonra mesaj kuyruğuna olay yayımlıyor. Veri tabanı yazması başarılı oluyor, yayımlama sırasında süreç düşüyor. Bu sorunu kalıcı çözen yaklaşım hangisidir?
## A
Yayımlamayı veri tabanı yazmasından önce yapmak; sıra tersine çevrilince tutarsızlık ortadan kalkar
🤖 sıra ters çevrilince bu kez olay var, kayıt yok; sorun yön değiştirir.
## B
Yayımlamayı sonsuz yeniden denemeye almak; er geç başarılı olacağı için tutarsızlık kendiliğinden kapanır
🤖 süreç düştüğünde yeniden deneme durumu da kaybolur; bellekte tutulan kuyruk uçar.
## C
Olayı aynı işlemde giden kutusu tablosuna yazmak; ayrı bir süreç tablodan okuyup yayımlar
🤖 Doğru: yazma ile olay tek işlemde atomik olur.
## D
İki kaynağı iki fazlı kesinleştirme ile tek işleme almak; kuyruk ve veri tabanı birlikte kesinleşir
🤖 kilitleri uzatır, tek arıza noktası ekler; yaygın tercih değildir.
## Doğru
C
## Açıklama
Bu, dağıtık sistemlerin klasik **çift yazma** sorunudur. İki farklı kaynağa arka arkaya yazarsın ve arada süreç düşerse ikisi uyuşmaz. A şıkkı yalnız yönü değiştirir: bu kez olay yayımlanır ama kayıt oluşmaz; tüketiciler var olmayan bir siparişi işlemeye çalışır. B şıkkı yeniden denemeyi çözüm sanar; oysa süreç düştüğünde bekleyen yeniden deneme de yok olur. Doğru çözüm **giden kutusu tablosudur** (outbox): olay, iş verisiyle aynı veri tabanı işleminde bir tabloya yazılır. Böylece "kayıt var ama olay yok" durumu imkânsızlaşır. Ayrı bir süreç bu tabloyu okur ve mesajı yayımlar; başarısız olursa tekrar dener (`116`). Bu tasarım en az bir kez teslim üretir, dolayısıyla tüketici tarafında tekrar güvenliği zorunludur: mesaj anahtarı saklanır ve aynı anahtar ikinci kez gelirse işlem tekrarlanmaz (`130`). D şıkkı iki fazlı kesinleştirmeyi önerir; kilitleri uzattığı ve koordinatörü tek arıza noktası yaptığı için mikroservislerde genellikle tercih edilmez (`128`).

# sozlu3-6 | Uzman
## Soru
Yedi adımlı ve koşullu dallanmaları olan bir sipariş akışı saga ile kurulacak. Ekip koreografi mi orkestrasyon mu kullanacağını tartışıyor. En yerinde gerekçe hangisidir?
## A
Orkestrasyon uygun; akış tek yerde görünür olur ve dallanma ile telafi sırası izlenebilir kalır
🤖 Doğru: adım ve dallanma arttıkça okunabilirlik belirleyici olur.
## B
Koreografi uygun; olay sayısı arttıkça akışın bütününü okumak kendiliğinden kolaylaşır
🤖 tersi olur; olay arttıkça akış hiçbir yerde bütün hâlinde durmaz.
## C
Fark yoktur; ikisi de aynı olayları ürettiği için seçim tamamen ekibin zevkine kalmıştır
🤖 seçim ölçüte bağlıdır: adım sayısı, dallanma ve izlenebilirlik ihtiyacı.
## D
Orkestrasyon uygun; merkezî koordinatör tek nokta olduğu için telafi adımları gereksiz hâle gelir
🤖 koordinatör telafiyi kaldırmaz, yalnız sırasını yönetir.
## Doğru
A
## Açıklama
Koreografide merkezî bir yönetici yoktur; her servis olayı dinler ve kendi adımını atar. Az adımlı akışlarda bu yaklaşım hızlıdır ve servisleri gevşek bağlı tutar. Ancak adım sayısı ve dallanma arttıkça akış **hiçbir yerde bütün hâlinde durmaz**; sıra ancak kod okunarak çıkarılır ve telafi sırasını izlemek zorlaşır. Yedi adım ve koşullu dallanma tam bu eşiğin üstündedir. Orkestrasyonda ise bir koordinatör adımları sırayla çağırır, kararları ve telafi sırasını tek yerde tutar. Bedeli de söylenmelidir: koordinatör yeni bir bileşendir, kendi durumunu kalıcı tutmalı ve kendisi de izlenmelidir (`126`). D şıkkı yanlış bir sonuç çıkarır; koordinatör telafiyi ortadan kaldırmaz, yalnız hangi telafinin ne zaman çalışacağını yönetir. C şıkkı kararı zevke indirger; oysa ölçüt bellidir: adım sayısı, dallanma, izlenebilirlik ve sahiplik. Hangi yaklaşım seçilirse seçilsin, giden kutusu ve tekrar güvenliği olmadan saga güvenilir çalışmaz (`116`, `130`).
