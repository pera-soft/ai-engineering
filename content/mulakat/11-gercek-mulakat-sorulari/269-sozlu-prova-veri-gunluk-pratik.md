---
slug: sozlu-prova-veri-gunluk-pratik
baslik: "Sözlü Prova · Veri, Entegrasyon ve Günlük Pratik: SQL, Loglama, İşlem, Git, SOLID ve Dil Seçimi"
kademe: Orta
sira: 269
etiketler: 11
---

## Konu anlatımı

Bu dosyada altı gerçek mülakat sorusu var. Altısı da açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur. Bu altı soru mimari değil günlük pratik ölçer. Sırayla şunu sorarlar: yavaş sorguyu nasıl buldun, entegrasyonu nasıl izliyorsun, işlem sınırını nereye koyuyorsun, dalları nasıl yönetiyorsun, tasarımı örnekle anlatabiliyor musun, dili neye göre seçiyorsun. Bu sorular kolay görünür ve tam bu yüzden hazırlıksız yakalar.

### "SQL'de performans sorunu yaşadığınız bir sorguyu nasıl analiz edip iyileştirdiniz?"

**Soru aslında ne ölçüyor?**

Bu soru indeks tanımı istemiyor, yöntem istiyor. Görüşmecinin merak ettiği tek şey şu: bu aday yavaş sorguyla karşılaşınca ilk nereye bakıyor. Zayıf aday çözüm sayar ve hemen indeks eklemeyi önerir. Güçlü aday yürütme planından başlar, sonra ölçtüğü sayıyı söyler. Sorunun içinde bir de deneyim çengeli var; "yaşadığınız" kelimesi hikâye istiyor. Ölçülen bir önce-sonra değeri bu cevabı tek cümlede ayrıştırır.

**Sözlü cevap iskeleti**

1. **Ölçümle başla.** Hangi sorgu, hangi sıklıkta, ne kadar sürüyor. Yavaş sorgu tahminle değil, veri tabanının yavaş sorgu kaydıyla bulunur.
2. **Yürütme planına bak.** `EXPLAIN` çıktısı hangi erişim yolunun seçildiğini gösterir. Tam tablo taraması mı var, indeks taraması mı; birleştirme türü hangisi (`146`).
3. **Seçiciliği ve indeks sırasını konuş.** İndeks, satırların küçük bir kısmını süzüyorsa kazandırır. Bileşik indekste eşitlikle süzülen kolon önce, aralık koşulu sonra gelir.
4. **Planı bozan kalıpları say.** Kolonu fonksiyona sokmak düz indeksi devre dışı bırakır. `SELECT *` gereksiz kolon taşır ve kapsayan indeks şansını yok eder (`29`).
5. **Sayfalamayı ve istatistiği ekle.** Derin `OFFSET` her sayfada baştan sayar; imleç sayfalama son anahtardan devam eder. İstatistik eskiyse planlayıcı yanlış plan seçer (`227`).
6. **Bedeli ve sonucu söyle.** Her indeks yazma işlemine yük bindirir. Kapanış cümlesi iki sayı içermeli: önceki süre ve sonraki süre.

```svg
<svg viewBox="0 0 660 318" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yavas sorgu analizi: olcum, yurutme plani, indeks karari ve plani bozan kaliplar">
  <defs>
    <marker id="sozlu8-sql" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yavaş sorgu: önce ölç, sonra plana bak, en son indeksi değiştir</text>
  <rect x="14" y="36" width="196" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Ölçüm</text>
  <text x="112" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yavaş sorgu kaydı</text>
  <text x="112" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">süre ve çağrı sıklığı</text>
  <text x="112" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">okunan satır sayısı</text>
  <text x="112" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dönen satır sayısı</text>
  <text x="112" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">okunan ile dönen arasındaki</text>
  <text x="112" y="180" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">fark ilk ipucudur</text>
  <line x1="212" y1="110" x2="228" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu8-sql)"/>
  <rect x="232" y="36" width="196" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · Yürütme planı</text>
  <text x="330" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tam tarama mı, indeks mi</text>
  <text x="330" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">birleştirme türü ve sırası</text>
  <text x="330" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tahmini ile gerçek satır farkı</text>
  <text x="330" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sıralama ve geçici alan</text>
  <text x="330" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tahmin gerçekten çok saparsa</text>
  <text x="330" y="180" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">istatistikler eskimiştir</text>
  <line x1="430" y1="110" x2="446" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu8-sql)"/>
  <rect x="450" y="36" width="196" height="150" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · İndeks kararı</text>
  <text x="548" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">seçicilik yeterli mi</text>
  <text x="548" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eşitlik önce, aralık sonra</text>
  <text x="548" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">okunan kolon indekse girsin</text>
  <text x="548" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">var olan indeksle birleştir</text>
  <text x="548" y="166" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">her yeni indeks yazma</text>
  <text x="548" y="180" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">işlemine yük bindirir</text>
  <rect x="14" y="198" width="316" height="106" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="219" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Planı sessizce bozan kalıplar</text>
  <text x="172" y="241" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kolonu fonksiyona sokmak: UPPER(ad) = ?</text>
  <text x="172" y="261" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kolonun soluna işlem koymak: fiyat * 2 &gt; ?</text>
  <text x="172" y="281" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SELECT * ile gereksiz kolon taşımak</text>
  <text x="172" y="297" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">üçü de indeksi kullanılamaz hâle getirir</text>
  <rect x="344" y="198" width="302" height="106" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="495" y="219" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sayfalama: offset yerine imleç</text>
  <text x="495" y="241" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">derin OFFSET her sayfada baştan sayar</text>
  <text x="495" y="261" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">imleç son görülen anahtardan devam eder</text>
  <text x="495" y="281" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">süre sayfa numarasıyla artmaz</text>
  <text x="495" y="297" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">kapanış cümlesi iki sayı içerir: önce ve sonra</text>
</svg>
```

**Elenme sebepleri**

- "İndeks eklerim" deyip plandan hiç söz etmemek. Ölçmeden atılan adım tahmindir ve çoğu zaman tutmaz.
- Her kolona indeks açmayı savunmak. Her indeks ekleme ve güncelleme işlemini yavaşlatır, ayrıca yer tutar.
- Bileşik indekste sıranın önemsiz olduğunu söylemek. Sıra yanlışsa indeks pek çok sorguda kullanılamaz.
- Fonksiyonlu kolonu fark etmemek. `UPPER(ad) = ?` yazımı düz indeksi devre dışı bırakan en yaygın kalıptır.
- `SELECT *` alışkanlığını savunmak. Gereksiz kolon hem ağ trafiği hem kapsayan indeks kaybı demektir.
- Sayfalamayı hiç konuşmamak. Liste ekranlarındaki yavaşlığın büyük kısmı derin `OFFSET` yüzünden çıkar.
- Sonucu sayı ile söylememek. "Çok hızlandı" bir izlenimdir; iki ölçümü yan yana koymak kanıttır.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; şu sorular seni kendi geçmişine götürsün:

- Hangi ekranda ya da hangi toplu işte yavaşlık konuşuldu? Belirti neydi: bekleme mi, zaman aşımı mı?
- Yavaş sorguyu nasıl buldun? Veri tabanı kaydı mı, uygulama logu mu, kullanıcı şikâyeti mi?
- Plana baktın mı? Baktıysan ne gördün: tam tablo taraması, yanlış birleştirme sırası, sıralama maliyeti?
- Kök neden neydi? Eksik indeks mi, yanlış sıralı bileşik indeks mi, fonksiyonlu koşul mu, eski istatistik mi?
- Ne değiştirdin? Yalnız indeks mi, yoksa sorgunun kendisi ya da veri modeli de mi değişti?
- Yazma tarafında bir bedel çıktı mı? Toplu yükleme veya gece işleri yavaşladı mı?
- Ölçüm ne oldu? Süre kaçtan kaça indi, kaç kayıt okunuyordu, sonra kaç kayıt okundu?

Böyle bir vakan yoksa uydurma. Bu soruda gerçek gözlem üretmek kolaydır. Kendi veri tabanında bir tabloyu çoğalt, sorguyu ölç, indeksi ekle ve tekrar ölç. Gördüğün fark gerçek bir ölçümdür ve `256`'daki dürüst köprüyle dürüstçe anlatılır.

### "API entegrasyonlarında hata ayıklama ve loglama yaklaşımınız nedir, hangi metrikleri izlersiniz?"

**Soru aslında ne ölçüyor?**

Bu soru iki ayrı şeyi tek cümlede sorar ve çoğu aday ikisini karıştırır. Hata ayıklama tek bir vakayı anlatır; metrik bütün davranışı anlatır. Bu ayrımı ilk cümlede yapan aday hemen ayrışır. Görüşmecinin ikinci beklentisi gizlilik disiplinidir. Dış entegrasyonda loglanan gövde çoğu zaman hassas veri taşır. Ağırlık yöntemdedir, araç isimlerinde değil.

**Sözlü cevap iskeleti**

1. **İki soruyu ayır.** Log tek isteğin hikâyesini anlatır. Metrik binlerce isteğin eğilimini anlatır. Hata ayıklarken loga, sağlığı izlerken metriğe bakarsın.
2. **İstek kimliğini uçtan uca taşı.** Her isteğe bir correlation-id üretilir, dış çağrının başlığına konur ve her log satırına basılır (`103`).
3. **Logu yapılandır ve maskele.** Log satırı JSON gibi ayrıştırılabilir olmalı. Kart numarası, token ve kimlik bilgisi asla ham yazılmaz (`251`).
4. **Ne loglanır, ne loglanmaz.** Uç nokta, durum kodu, süre, deneme sayısı loglanır. Tam gövde loglanmaz; gerekiyorsa örneklenir ve kısa süre saklanır.
5. **RED metriklerini say.** İstek sayısı, hata oranı ve süre dağılımı. Her dış servis ayrı etiketle izlenir; süre ortalama değil yüzdelik dilimle okunur (`104`).
6. **Dayanıklılık metriklerini ekle.** Devre kesici durumu, yeniden deneme sayısı, zaman aşımı sayısı ve havuz doluluğu (`101`). Alarm ham sayıya değil, kullanıcıya yansıyan orana kurulur.

**Elenme sebepleri**

- İstek ve yanıt gövdesini olduğu gibi loglamak. Hem hassas veri sızar hem log maliyeti hızla büyür.
- Correlation-id'den hiç söz etmemek. Kimliksiz logda tek bir isteği baştan sona takip etmek neredeyse imkânsızdır.
- Yalnız hataları loglamak. Başarılı çağrının süresi tutulmazsa yavaşlama fark edilmez.
- Üretimde log seviyesini sürekli `DEBUG` tutmak. Gürültü artar, maliyet artar, asıl satır kaybolur.
- Metrik yerine "loglara bakarım" demek. Log tek vakayı gösterir, eğilimi göstermez.
- Etiketlere kullanıcı kimliği gibi yüksek çeşitlilikli değer koymak. Metrik deposu şişer ve sorgular yavaşlar.
- Dış servisin süresini kendi süresinden ayırmamak. Ayrım yoksa yavaşlığın sahibi bulunamaz.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir. Şu sorular onu çıkarır:

- Hangi dış servisle entegrasyon kurdun? Ödeme, kimlik doğrulama, kargo, kurum sorgusu, bildirim?
- Bir hatayı takip ederken zorlandığın oldu mu? Neyi bulamadın: isteği mi, yanıtı mı, zamanı mı?
- Logda arama neyle yapılıyordu? İstek kimliği mi, müşteri numarası mı, yoksa saat aralığı mı?
- Hassas veri loga düştü mü? Fark edilince ne yapıldı, maskeleme sonradan mı eklendi?
- Dış servis yavaşladığında bunu nereden anladınız? Alarm mı vardı, şikâyet mi geldi?
- Yeniden deneme var mıydı? Varsa denemelerin sayısı ölçülüyor muydu?
- Bugün aynı hata tekrar olsa kaç dakikada bulursun? Bu sayı, kurduğun düzenin gerçek karnesidir.

Deneyimin yoksa uydurma. `256`'daki köprü burada da işler. Kendi projende bir dış çağrıya correlation-id ve süre ölçümü eklemek gerçek bir uygulamadır ve mülakatta öyle anlatılır.

### "Veri tutarlılığı ve işlem (transaction) yönetimi için hangi stratejileri kullanırsınız?"

**Soru aslında ne ölçüyor?**

Bu soru ACID tanımı istemiyor; sınır çizebilme becerisi arıyor. İlk ayrım şudur: tek veri tabanı mı var, birden fazla sistem mi. Cevap bu ayrımdan sonra ikiye ayrılır ve ayrımı yapmayan aday iki dünyayı karıştırır. İkinci ayrım daha incedir. `@Transactional` yazmak bir işlem yönetimi stratejisi değildir; asıl karar sınırın nerede başlayıp nerede bittiğidir. Ağırlık muhakemededir.

**Sözlü cevap iskeleti**

1. **Önce ölçeği sor.** Tek veri tabanında mıyız, yoksa birden fazla sistem mi güncelleniyor. Araçlar bu cevapla değişir.
2. **Tek veri tabanında sınırı yerleştir.** İşlem sınırı iş akışının bittiği yerdedir; genellikle servis metodudur. Depo katmanına işlem koymak sınırı parçalar (`83`).
3. **Anotasyonun incelikleri.** Aynı sınıf içinden yapılan çağrı vekil nesneyi atlar ve işlem başlamaz. Geri alma varsayılanı yalnız denetlenmeyen istisnalar içindir (`83`).
4. **İşlemin içine dış çağrı sokma.** HTTP isteği, kuyruk yayımı ve e-posta işlem dışında kalır. Yoksa bağlantı kilitli bekler, havuz tükenir ve gönderilen istek geri alınamaz.
5. **Yayılım ve izolasyonu gerekçeyle seç.** `REQUIRES_NEW` denetim kaydı gibi ayrı kalması gereken işler içindir. Çoğu iş akışında iyimser kilitleme yeterlidir (`84`).
6. **Sınır aşılınca aracı değiştir.** İki fazlı kesinleştirme yaygın tercih değildir. Giden kutusu tablosu, tekrar güvenliği ve saga bu işi devralır (`116`, `130`, `126`).

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Islem siniri: iceride kalanlar, disarida kalanlar ve sinir asilinca kullanilan araclar">
  <defs>
    <marker id="sozlu8-islem" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İşlem sınırı: içeride ne durur, dışarıda ne kalır</text>
  <rect x="14" y="36" width="310" height="164" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İşlemin içinde kalır</text>
  <text x="169" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı veri tabanına yapılan yazmalar</text>
  <text x="169" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">okuma ve doğrulama, tek iş birimi</text>
  <text x="169" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">giden kutusu tablosuna olay yazma</text>
  <text x="169" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sürüm kolonuyla iyimser kilit denetimi</text>
  <text x="169" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kural: kısa tut, tek kaynağa dokun</text>
  <text x="169" y="184" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sınır servis metodunda başlar ve biter</text>
  <rect x="336" y="36" width="310" height="164" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="491" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">İşlemin dışında kalmalı</text>
  <text x="491" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dış servise HTTP çağrısı</text>
  <text x="491" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kuyruğa doğrudan mesaj yayımlama</text>
  <text x="491" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">e-posta, kısa mesaj, dosya yükleme</text>
  <text x="491" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uzun süren hesap ve rapor üretimi</text>
  <text x="491" y="166" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">bağlantı kilitli bekler, havuz tükenir</text>
  <text x="491" y="184" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">gönderilmiş istek geri alınamaz</text>
  <line x1="330" y1="212" x2="330" y2="230" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu8-islem)"/>
  <rect x="14" y="236" width="310" height="74" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="257" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Tek veri tabanı: ACID yeter</text>
  <text x="169" y="278" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınır servis metodu, geri alma otomatiktir</text>
  <text x="169" y="297" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dikkat: kendi kendine çağrı vekili atlar</text>
  <rect x="336" y="236" width="310" height="74" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="491" y="257" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Sınır aşılınca: araç değişir</text>
  <text x="491" y="278" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">giden kutusu, tekrar güvenliği, saga</text>
  <text x="491" y="297" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tutarlılık seviyesi bir iş kararıdır</text>
</svg>
```

**Elenme sebepleri**

- İşlem yönetimini anotasyon bilgisine indirgemek. Asıl karar sınırın nerede çizildiğidir, anotasyonun yazıldığı yer değil.
- Dış çağrıyı işlem içine koymak ve bunu sorun görmemek. Bu, üretimde havuz tükenmesinin en sık sebeplerindendir.
- Kendi kendine çağrı tuzağını bilmemek. Aynı sınıftan çağrılan metotta işlem başlamaz ve kimse fark etmez.
- Geri alma varsayılanını yanlış bilmek. Denetlenen istisnada varsayılan davranış geri alma değildir.
- İzolasyon seviyesini gerekçesiz yükseltmek. Yüksek seviye kilit ve çekişme üretir, iş hacmini düşürür.
- Dağıtık ortamda hâlâ tek işlemden söz etmek. İki ayrı veri tabanı tek işlemle güvenceye alınmaz.
- Tutarlılık seviyesini teknik karar saymak. Verinin kaç saniye eski kalabileceğini iş tarafı söyler.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Hangi akışta birden fazla tabloyu tek seferde güncellemek gerekti? Sipariş, muhasebe kaydı, bakiye?
- İşlem sınırını nereye koydun? Denetleyici mi, servis mi, yoksa hiç düşünmeden depo mu?
- Hiç yarım kalmış kayıt gördün mü? Belirti neydi: eksik satır mı, uyuşmayan iki tablo mu?
- Bağlantı havuzu tükendi mi? Tükendiyse sebebi uzun işlem miydi, yavaş dış çağrı mıydı?
- Aynı kaydı iki kullanıcı aynı anda güncellediğinde ne oluyordu? Kayıp güncelleme yaşandı mı?
- Elle düzeltme yapıldı mı? Yapıldıysa bir daha olmaması için ne eklendi?
- İki ayrı sistemi güncellemek gerektiğinde ne yaptınız? Kuyruk mu, gece mutabakatı mı, elle kontrol mü?

Deneyimin yoksa uydurma. Tek veri tabanında işlem sınırı yönetmiş olmak da anlatılabilir bir temeldir. `256`'daki köprüyle şunu söylersin: burada şu aracı kullandım, dağıtıkta bu yetmez, çünkü şu değişir.

### "Git ile çalışma düzeniniz nedir; dallanma ve kod inceleme yaklaşımınız?"

**Soru aslında ne ölçüyor?**

Bu soru araç bilgisi ölçmüyor; ekip alışkanlığını ölçüyor. Komut listesi saymak kimseyi etkilemez, çünkü komutlar herkeste aynıdır. Görüşmecinin baktığı şey şu: bu aday dal düzenini bir teslimat kararı olarak görüyor mu. İkinci bakılan yer inceleme kültürüdür. İnceleme sürecini "kontrol" gibi anlatan aday ile "ortak sahiplik" gibi anlatan aday farklı puan alır. Ağırlık deneyimdedir.

**Sözlü cevap iskeleti**

1. **Amacı ilk cümlede ver.** Dal modeli bir zevk meselesi değildir. İki şeyi belirler: ana dalın sağlığı ve teslimat hızı.
2. **Dalı kısa ömürlü tut.** Bir iş kalemi, birkaç gün. Uzun dal sapar; sapma süresi uzadıkça birleştirme acısı büyür.
3. **Ana dalı hep yeşil tut.** Her itmede derleme ve test koşar. Kırık ana dal yalnız seni değil, bütün ekibi durdurur (`220`).
4. **Küçük istek gönder.** İnceleme kalitesi istek büyüdükçe düşer. Birkaç yüz satırı geçen istekte inceleme yüzeyselleşir (`191`).
5. **Geçmişi okunur bırak.** Commit mesajı ne yaptığını değil neden yaptığını anlatır. Kendi dalında yeniden temellendirme (rebase) geçmişi düzeltir; paylaşılan dalın geçmişi yeniden yazılmaz.
6. **İncelemeyi ikiye ayır.** Biçim ve statik analiz otomatiktir, tartışılmaz. İnsan sınır durumuna, teste ve isme bakar. Yorumu engelleyen ve öneri diye ayır.

**Elenme sebepleri**

- Dal modelini yalnız isimle anlatmak. Model adı bilmek yetmez; ana dalın nasıl korunduğu sorulur.
- Haftalarca açık kalan dalları normal saymak. Büyük birleştirme çatışması bir kaza değil, düzenin sonucudur.
- Ana dala doğrudan itmeyi savunmak. Otomatik kontrol yoksa kırık ana dal kaçınılmazdır.
- Paylaşılan dalın geçmişini yeniden yazmak. Diğerlerinin yereli bozulur ve güven kaybolur.
- Commit mesajını "düzeltme" gibi yazmak. Altı ay sonra o commit'in sebebi hiçbir yerde kalmaz.
- İncelemeyi kişisel eleştiri gibi yürütmek. Yorum koda değil kişiye dönerse süreç savunmaya döner.
- Otomatikleştirilebilecek şeyi incelemede tartışmak. Biçim tartışması gerçek hatayı gölgede bırakır.

**Kendi hikâyen**

Bu soru en kolay somutlanan sorulardan biridir. Şu sorular hikâyeni çıkarır:

- Şu anki projede dal düzeni ne? İsim veremesen bile akışı adım adım anlatabiliyor musun?
- En son büyük bir birleştirme çatışması ne zaman oldu? Sebebi neydi ve ne kadar sürdü?
- Ana dal hiç kırıldı mı? Kırılınca ne oldu, kaç kişi bekledi, nasıl toparlandı?
- Senin gönderdiğin istekler ne kadar büyük? Bir inceleme kaç saatte kapanıyor?
- İncelemede aldığın en faydalı yorum neydi? Nasıl ifade edilmişti?
- Sen inceleme yaparken neye bakıyorsun? Testi kontrol ediyor musun, sınır durumunu soruyor musun?
- Düzeni değiştirmeyi hiç önerdin mi? Önerdiysen ne değişti ve sonuç ölçüldü mü?

Kurumsal bir akış içinde çalışmadıysan uydurma. Kendi projende bile dal açma, küçük istek gönderme ve mesaj disiplini gerçek bir alışkanlıktır. `256`'daki köprüyle bunu olduğu gibi anlatırsın.

### "SOLID prensiplerini bir örnek üzerinden nasıl uyguladığınızı açıklar mısınız?"

**Soru aslında ne ölçüyor?**

Bu soru görünüşte bilgi sorar, aslında anlatım ölçer. Beş harfi neredeyse her aday sayabilir; bu yüzden tanım turu seni ayrıştırmaz, sıraya sokar. Sorunun içindeki asıl talep "bir örnek üzerinden" kısmındadır. Güçlü cevap tek bir somut sahne seçer ve o sahnede iki-üç prensibi birden gösterir. Tanım saymak zayıf, örnekte göstermek güçlüdür. Ağırlık örnektedir.

**Sözlü cevap iskeleti**

1. **Tanım turunu atla.** Beş harfi sırayla saymaya başlama. Tek cümlede çerçeveyi ver: bu prensiplerin ortak amacı değişimi tek yere hapsetmektir.
2. **Tek örnek seç ve sahneyi kur.** Ödeme yöntemine göre dallanan bir `if-else` zinciri iyi bir sahnedir. Her yeni yöntemde aynı sınıf yeniden açılır.
3. **Sorunu prensiple adlandır.** Yeni davranış için var olan sınıfı değiştirmek zorunda kalmak, açık-kapalı prensibinin ihlalidir.
4. **Dönüşümü anlat.** Her yöntem ortak bir arayüzü uygular. Yeni yöntem eklemek yeni bir sınıf yazmaktır; eski sınıfa dokunulmaz (`135`).
5. **İkinci prensibi aynı sahneden çıkar.** Servis artık somut sağlayıcıyı değil soyutlamayı tanır. Bu, bağımlılığın ters çevrilmesidir ve testi de kolaylaştırır (`131`).
6. **Üçüncüsünü sınırla ve bedeli söyle.** Arayüz şişerse her uygulayıcı boş metot yazar; arayüz ayrımı bunu önler. İki dallı bir koşul için strateji kurmak ise gereksiz karmaşıklıktır (`133`).

```svg
<svg viewBox="0 0 660 326" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="if zincirinden strateji sinifina gecis ve bu gecisin gosterdigi SOLID prensipleri">
  <defs>
    <marker id="sozlu8-solid" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tanım sayma, tek örnekte iki-üç prensibi birden göster</text>
  <rect x="14" y="36" width="272" height="164" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="150" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Önce: if-else zinciri</text>
  <text x="150" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">OdemeServisi.ode(yontem)</text>
  <text x="150" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">if kart ... else if havale ...</text>
  <text x="150" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">else if cüzdan ... else hata</text>
  <text x="150" y="146" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">yeni yöntem = aynı sınıf yeniden açılır</text>
  <text x="150" y="164" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">test dosyası her seferinde büyür</text>
  <text x="150" y="182" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">servis tüm sağlayıcıları tanır</text>
  <line x1="290" y1="118" x2="312" y2="118" stroke="#5D7789" stroke-width="1.6" marker-end="url(#sozlu8-solid)"/>
  <text x="301" y="106" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">taşı</text>
  <rect x="318" y="36" width="328" height="164" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="482" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sonra: strateji sınıfları</text>
  <rect x="336" y="68" width="292" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="482" y="87" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arayüz: OdemeYontemi.ode(tutar)</text>
  <rect x="336" y="106" width="92" height="28" rx="6" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="382" y="124" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">KartOdeme</text>
  <rect x="436" y="106" width="92" height="28" rx="6" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="482" y="124" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">HavaleOdeme</text>
  <rect x="536" y="106" width="92" height="28" rx="6" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="582" y="124" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">CuzdanOdeme</text>
  <text x="482" y="154" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">yeni yöntem = yeni sınıf, servis değişmez</text>
  <text x="482" y="172" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">servis yalnız arayüzü tanır</text>
  <text x="482" y="190" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">testte sahte uygulama geçirilir</text>
  <rect x="14" y="212" width="632" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="232" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Aynı sahne, üç prensip</text>
  <text x="330" y="251" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">OCP: davranış eklenir, var olan sınıf değişmez · DIP: servis somut sınıfa değil soyutlamaya bakar</text>
  <text x="330" y="267" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ISP: arayüz dar tutulur, uygulayıcı boş metot yazmaz</text>
  <rect x="14" y="280" width="632" height="34" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="301" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Takası da söyle: iki dallı bir koşul için strateji kurmak gereksiz karmaşıklıktır.</text>
</svg>
```

**Elenme sebepleri**

- Beş prensibi ezberden sayıp örnek vermemek. Soru zaten örnek istiyordu; tanım listesi cevabın girişidir, kendisi değil.
- Her prensibe ayrı ve yapay örnek uydurmak. Anlatım dağılır ve dinleyici tek bir sahne göremez.
- Tek sorumluluğu "bir sınıf tek iş yapar" diye bırakmak. Ölçüt değişim sebebidir; sebep söylenmezse ölçüt yoktur.
- Liskov'u yalnız tanımıyla geçmek. İhlalin belirtisi söylenmeli: alt tip konunca çağıran kod bozulur.
- Bağımlılığın ters çevrilmesini enjeksiyon sanmak. Enjeksiyon bir tekniktir; ters çevirme bağımlılık yönü kararıdır.
- Prensipleri mutlak kural gibi sunmak. Her yere arayüz açmak da bedel üretir ve okunabilirliği düşürür.
- Sonucu sıfatla anlatmak. "Daha temiz oldu" cümlesi ölçüt vermez; "yeni yöntem eklerken servise dokunmadım" verir.

**Kendi hikâyen**

Bu soruda örnek senin kodundan gelirse cevap iki kat güçlenir. Şu sorular onu çıkarır:

- Hiç uzun bir koşul zincirini sınıflara taşıdın mı? Zincir neye göre dallanıyordu?
- Aynı dosyayı her yeni gereksinimde açtığın oldu mu? O dosyanın adı neydi, kaç satırdı?
- Bir arayüzü sonradan böldüğün oldu mu? Bölmeden önce hangi uygulayıcı boş metot yazıyordu?
- Test yazmayı zorlaştıran bir bağımlılık gördün mü? Onu nasıl dışarı çıkardın?
- Bir kalıbı gereksiz yere uyguladığın oldu mu? Sonradan geri aldın mı?
- Değişiklikten sonra ne kolaylaştı? Yeni davranış eklerken kaç dosyaya dokunuyordun, sonra kaç oldu?
- Ekipten itiraz geldi mi? "Bu fazla soyut" itirazına ne cevap verdin?

Böyle bir dönüşüm yaptığın bir kod yoksa uydurma. Küçük bir örneği kendin yazıp farkı gözlemleyebilirsin. Bu da gerçek bir gözlemdir; `256`'daki köprüyle dürüstçe anlatılır.

### "Günlük geliştirme sürecinizde kullandığınız diller ve neden tercih ettiğiniz?"

**Soru aslında ne ölçüyor?**

Görünüşte sohbet sorusudur, aslında yargı sorusudur. "Java severim, çünkü güçlü bir dil" cevabı zayıftır; tercihi duyguya bağlar ve ölçüt vermez. Görüşmeci iki şeye bakar. Birincisi, bu aday aracı işe göre seçebiliyor mu. İkincisi, kendi ana dilinin zayıf yanını söyleyebiliyor mu. İkincisi daha ayrıştırıcıdır, çünkü aracını eleştirebilen aday takas kurabilir.

**Sözlü cevap iskeleti**

1. **Ölçütle başla, dille değil.** Seçim dört şeye bakar: ekosistem olgunluğu, ekibin bilgisi, işletme maliyeti ve bakım süresi.
2. **Günlük yığını somut söyle.** Hangi dil hangi iş için: servis tarafında Java, veri tarafında SQL, otomasyonda kabuk betiği, arada yapılandırma dosyaları.
3. **Java tercihini işe bağla.** Uzun ömürlü kurumsal sistemde kütüphane ekosistemi, araç olgunluğu ve geriye uyumluluk disiplini ağır basar.
4. **Zayıf yanını da söyle.** Java uzun yazımlıdır; kısa betik işlerinde ağır kalır. Başlangıç süresi ve bellek ayak izi kısa ömürlü işlevlerde dezavantajdır.
5. **Karşı örnek ver.** Aynı işi bir betik dili daha ucuza yapıyorsa dili değiştirmek olgunluktur. Tek dile bağlılık bir erdem değildir.
6. **Öğrenme eğrisiyle kapat.** Dil değil kavram taşınır: eşzamanlılık, bellek, tip sistemi. Yeni bir dile ne kadar sürede üretken geçtiğini söyle (`256`).

**Elenme sebepleri**

- "Java severim" diyip cümleyi orada bitirmek. Duygu bir ölçüt değildir; görüşmeci gerekçe bekler.
- Başka dilleri küçümsemek. Bu tavır ekip çalışmasında sürtünme işareti sayılır.
- Java'nın hiçbir zayıf yanını söyleyememek. Aracını eleştiremeyen aday takas cümlesi de kuramaz.
- Hiç kullanmadığı dilleri listeye eklemek. Ayrıntı sorusu mutlaka gelir ve cevap orada çöker.
- Seçimi yalnız hıza indirgemek. İşletme maliyeti ve ekip bilgisi çoğu zaman hızdan daha belirleyicidir.
- SQL'i dil saymamak. Günlük işin önemli bir kısmı SQL'dir; listede yoksa iş tanımı eksik görünür.
- Moda olmayı gerekçe saymak. "Herkes geçiyor" cümlesi bir mühendislik ölçütü değildir.

**Kendi hikâyen**

Bu soruda hikâye zaten senin günlük hayatındadır. Şu sorular onu görünür kılar:

- Son bir ayda hangi dillerde kod yazdın? Her birine yaklaşık ne kadar zaman ayırdın?
- Java dışında bir dille çözdüğün bir iş oldu mu? Neden o dili seçtin?
- Java'da seni en çok yoran şey ne? Yazım uzunluğu mu, derleme süresi mi, yapılandırma mı?
- Yeni bir dile ya da sürüme geçtin mi? Ne kadar sürede üretken oldun, neyi zor buldun?
- Ekipte dil tartışması yaşandı mı? Karar hangi ölçütle verildi?
- Bir aracı bırakıp başkasına geçtiğin oldu mu? Geçişin sebebi ölçülebilir bir şey miydi?
- Bugün yeni bir servis kursan hangi dili seçerdin? Cevabını gerekçesiyle bir cümlede kurabiliyor musun?

Bu soruda uydurma cazip görünür ama gereksizdir; cevabın hepsi zaten senin günlük işindedir. Kullanmadığın bir dili biliyormuş gibi anlatma. `256`'daki köprü burada da kurtarır: ne bildiğini söyle, ne bilmediğini söyle, nasıl öğreneceğini söyle.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Önce iskeleti okuma. Soruyu yüksek sesle oku, sonra dosyayı kapat.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek prova sayılmaz; ağız ile beyin aynı hızda çalışmaz.
- Sonra iskeletle karşılaştır. Hangi adımı atladın, işaretle.
- Bu altı soruda en sık atlanan adım örnektir. Yöntem anlatıp örnek vermezsen cevap ders gibi duyulur.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- SOLID sorusunu ayrı çalış. Tek sahneyi seç ve o sahneyi ezberle; prensip adlarını değil.
- Altı soruyu üst üste yapma. Arada ara ver; yorgun ses ikna etmez.
- Ölçüt şudur: bir sorunun adımlarını kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Tek cümlede

Bu altı soru bilgi değil, günlük pratiği anlatma becerisi ölçer. Birincisinde kazanan cümle en baştadır: önce ölç, sonra plana bak, en son indeksi değiştir. İkincisinde ayrım şudur: log tek vakayı, metrik bütün davranışı anlatır. Üçüncüsünde asıl karar anotasyon değil, işlem sınırının nerede bittiğidir. Dördüncüsünde dal düzeni bir zevk değil, ana dalın sağlığı kararıdır. Beşincisinde tanım saymak zayıftır; tek örnekte iki-üç prensip göstermek güçlüdür. Altıncısında dil işe göre seçilir ve kendi dilinin zayıf yanı da söylenir. Altısında da aynı disiplin geçerlidir: ölçütü söyle, örneği ver, bedeli söyle, ölçülen sonucu söyle. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# sozlu8-1 | Temel
## Soru
Bir sorgu `WHERE UPPER(musteri_ad) = ?` koşuluyla çalışıyor. `musteri_ad` kolonunda düz bir indeks var, ama yürütme planı tam tablo taraması gösteriyor. En olası sebep hangisidir?
## A
Kolon bir fonksiyona sokulduğu için düz indeks kullanılamıyor
🤖 Doğru: indeks ham değeri saklar, fonksiyonun sonucunu değil.
## B
İndeks henüz oluşturulmadığı için planlayıcı onu göremiyor
🤖 indeks var; sorun görünürlük değil, koşulun biçimidir.
## C
İndeksler metin kolonlarında çalışmaz; yalnız sayısal kolonlarda etkilidir
🤖 metin kolonlarında da sıralı erişim verir; kısıt böyle değildir.
## D
Sorguda bağ değişkeni kullanıldığı için plan önbelleğe alınamıyor
🤖 bağ değişkeni planı bozmaz; tersine tekrar kullanımı kolaylaştırır.
## Doğru
A
## Açıklama
İndeks, kolonun **ham değerini** sıralı biçimde saklar. Koşulda kolon bir fonksiyona sokulunca aranan şey artık o ham değer değildir; planlayıcı indeksi kullanamaz ve tabloyu baştan sona tarar. Aynı tuzak aritmetikle de kurulur: `fiyat * 2 > ?` yazımı da indeksi devre dışı bırakır. Kural basittir: kolonu koşulun solunda çıplak bırak, işlemi sağ tarafa taşı. Çözüm iki yoldan biridir. Ya sorgu, kolonu olduğu gibi kullanacak biçimde yeniden yazılır. Ya da fonksiyonun sonucu için ayrı bir indeks tanımlanır; bazı veri tabanları bunu doğrudan destekler (`146`). Üçüncü bir yol veri modelindedir: aranan değerin normalleştirilmiş hâli ayrı bir kolonda tutulur ve o kolon indekslenir. B şıkkı soruyla çelişir, çünkü indeksin var olduğu söylenmiştir. C şıkkı yanlıştır. D şıkkı ise bağ değişkenini sorun sanar; oysa bağ değişkeni plan tekrar kullanımını kolaylaştırır (`227`, `29`).

# sozlu8-2 | Orta
## Soru
Bir ekipte özellik dalları iki-üç hafta açık kalıyor. Birleştirme sırasında büyük çatışmalar çıkıyor ve incelemeler yüzeyselleşiyor. En yerinde düzeltme hangisidir?
## A
Birleştirme yerine yalnız yeniden temellendirme kullanmak; çatışmalar böylece ortadan kalkar
🤖 rebase çatışmayı kaldırmaz, yalnız zamanını ve biçimini değiştirir.
## B
İnceleme zorunluluğunu kaldırıp dalları doğrudan ana dala birleştirmek
🤖 çatışma sürer, üstüne kalite denetimi de kaybolur.
## C
İşi küçük parçalara bölmek, dalları kısa ömürlü tutmak ve ana dalı sık sık dala almak
🤖 Doğru: çatışmanın asıl kaynağı sapma süresidir.
## D
Bütün dalları sürüm gününe kadar bekletip tek seferde birleştirmek
🤖 sapma süresi uzar; çatışma ve risk tek güne yığılır.
## Doğru
C
## Açıklama
Birleştirme çatışması bir araç sorunu değil, bir **sapma süresi** sorunudur. Dal ana daldan ne kadar uzun ayrı kalırsa, iki taraf o kadar çok değişir ve kesişme o kadar büyür. Çözüm çatışmayı daha iyi çözmek değil, çatışacak yüzeyi küçültmektir. Bunun iki somut karşılığı vardır. Birincisi işi bölmektir: bir dal bir iş kalemi taşır ve birkaç gün içinde kapanır. İkincisi ana dalı düzenli olarak dala almaktır; böylece fark küçük parçalar hâlinde karşılanır. Küçük dal aynı zamanda küçük inceleme demektir ve inceleme kalitesi doğrudan boyutla ilgilidir (`191`). A şıkkı aracı çözüm sanar; yeniden temellendirme geçmişi düzeltir ama farkı yok etmez. B şıkkı belirtiyi kaldırmak için denetimi kaldırır. D şıkkı sorunu büyütür: tek güne yığılan birleştirme hem çatışmayı hem riski yoğunlaştırır. Bu düzenin ayakta kalması için ana dalda her itmede derleme ve test koşmalıdır (`220`).

# sozlu8-3 | Temel
## Soru
Bir ekip, dış bir ödeme servisiyle kurulan entegrasyonda hata ayıklamayı kolaylaştırmak istiyor. Aşağıdakilerden hangisi en yerinde ilk adımdır?
## A
İstek ve yanıt gövdelerini olduğu gibi, tam hâliyle loga yazmak
🤖 hassas veri sızar ve log maliyeti hızla büyür.
## B
Her isteğe bir correlation-id üretip dış çağrıya taşımak ve her log satırına basmak
🤖 Doğru: tek istek uçtan uca izlenebilir hâle gelir.
## C
Üretimde log seviyesini `DEBUG` yapıp bütün satırları görünür kılmak
🤖 gürültü artar, maliyet artar, asıl satır kaybolur.
## D
Yalnız hata durumunda log yazmak; başarılı çağrılar için hiç log tutmamak
🤖 başarılı çağrının süresi olmadan yavaşlama fark edilmez.
## Doğru
B
## Açıklama
Entegrasyonda hata ayıklamanın ilk şartı, **tek bir isteği baştan sona takip edebilmektir**. Bunu sağlayan şey correlation-id'dir: istek girişinde üretilir, dış çağrının başlığına konur ve bütün log satırlarına yazılır. Böylece bir müşteri şikâyeti geldiğinde arama tek anahtarla yapılır; saat aralığı tahmin etmeye gerek kalmaz. Aynı kimlik dağıtık izleme altyapısıyla birleştiğinde adım adım süre dağılımı da görünür olur (`103`). Log içeriği ise seçilerek yazılır: uç nokta, durum kodu, süre ve deneme sayısı yeterlidir. A şıkkı tam gövdeyi loglar; ödeme akışında bu, kart bilgisi ve kimlik verisi sızdırma riskidir ve maskeleme zorunludur (`251`). C şıkkı gürültü üretir; `DEBUG` üretimde sürekli açık tutulmaz. D şıkkı yalnız hatayı loglar; oysa yavaşlama başarılı çağrıların süresinde görünür ve o süre RED metriklerinin temelidir (`104`).

# sozlu8-4 | Orta
## Soru
`@Transactional` işaretli bir servis metodu önce veri tabanına yazıyor, sonra aynı metot içinde dış bir REST servisine HTTP çağrısı yapıyor. Bu tasarımın temel sakıncası nedir?
## A
Dış çağrı veri tabanı işlemine dâhil olur ve hata hâlinde birlikte geri alınır
🤖 dış çağrı işlem kapsamına girmez; gönderilen istek geri alınmaz.
## B
Spring işlem içindeki HTTP çağrılarını engeller ve çalışma anında hata fırlatır
🤖 böyle bir engel yoktur; kod sessizce çalışır.
## C
Sakınca yalnız performanstadır; işlevsel açıdan bir sorun oluşmaz
🤖 asıl risk kaynak tükenmesi ve tutarsızlıktır, yalnız hız değil.
## D
Bağlantı yavaş dış çağrı boyunca kilitli kalır; havuz tükenir ve tutarlılık da garanti değildir
🤖 Doğru: işlem süresi dış servise bağlanmış olur.
## Doğru
D
## Açıklama
Bu kalıp üretimde iki ayrı arıza üretir. Birincisi kaynak tarafındadır: işlem açıkken veri tabanı bağlantısı elde tutulur. Dış servis yavaşlarsa bağlantı o süre boyunca kilitli kalır ve eşzamanlı istek arttıkça havuz tükenir. Sonuç, dış serviste başlayan yavaşlığın bütün uygulamayı durdurmasıdır. İkincisi tutarlılık tarafındadır: dış çağrı işlem kapsamında değildir. Veri tabanı işlemi geri alınsa bile gönderilmiş HTTP isteği geri alınamaz; karşı tarafta işlem gerçekleşmiş olur. A şıkkı tam bu noktayı yanlış bilir. B şıkkı var olmayan bir koruma varsayar; çerçeve böyle bir engel koymaz ve kod sessizce çalışır. C şıkkı sorunu hıza indirger. Doğru tasarımda işlem yalnız veri tabanı yazmalarını kapsar, kısa tutulur ve dış etkileşim sınırın dışına alınır. Olay yayımlanacaksa aynı işlemde giden kutusu tablosuna yazılır, yayımlamayı ayrı bir süreç üstlenir (`83`, `116`, `130`).

# sozlu8-5 | İleri
## Soru
Bir `OdemeServisi` sınıfı, ödeme yöntemine göre uzun bir `if-else` zinciri içeriyor ve her yeni yöntemde bu sınıf yeniden açılıyor. Zincir, ortak bir arayüzü uygulayan ayrı sınıflara taşınıyor. Bu dönüşüm hangi prensipleri birlikte gösterir?
## A
Yalnız tek sorumluluk prensibini; sınıf küçüldüğü için tek iş yapar hâle gelir
🤖 küçülme tek başına tek sorumluluğun kanıtı değildir.
## B
Açık-kapalı ve bağımlılığın ters çevrilmesi prensiplerini birlikte
🤖 Doğru: davranış eklenir, servis soyutlamaya bağlanır.
## C
Yalnız Liskov yerine geçme prensibini; alt tipler üst tipin yerine geçebilir
🤖 burada varsayımdır; dönüşümün kazandırdığı asıl şey değildir.
## D
Hiçbirini; strateji bir tasarım kalıbıdır, prensiplerle ilişkisi yoktur
🤖 kalıplar prensipleri uygulamanın somut yollarıdır.
## Doğru
B
## Açıklama
Bu dönüşüm iki prensibi aynı sahnede gösterir ve mülakatta güçlü olan cevap tam budur. Birincisi **açık-kapalı** prensibidir: yeni bir ödeme yöntemi eklemek artık yeni bir sınıf yazmak demektir, var olan servis dosyasına dokunulmaz. Kod genişlemeye açık, değişikliğe kapalı hâle gelir. İkincisi **bağımlılığın ters çevrilmesidir**: servis artık somut sağlayıcı sınıflarını değil ortak arayüzü tanır. Bağımlılık oku üst seviye iş kuralından ayrıntıya değil, ikisinden de soyutlamaya döner. Bunun somut karşılığı testte görülür; sahte bir uygulama geçirmek kolaylaşır (`131`). Kullanılan yapının adı strateji kalıbıdır ve kalıp burada prensibin uygulanma biçimidir (`135`, `133`). A şıkkı küçülmeyi tek sorumluluk sanır; ölçüt boyut değil değişim sebebidir. C şıkkı Liskov'u öne çıkarır; oysa yerine geçebilirlik burada zaten varsayılır. Cevabın sonunda takas da söylenmeli: iki dallı bir koşul için strateji kurmak gereksiz karmaşıklıktır.

# sozlu8-6 | İleri
## Soru
Sık koşan bir sorgu şu koşulu kullanıyor: `WHERE musteri_id = ? AND islem_tarihi BETWEEN ? AND ?`. Sonuçta yalnız `tutar` kolonu okunuyor. En yerinde indeks hangisidir?
## A
`(islem_tarihi, musteri_id)` biçiminde bileşik indeks
🤖 aralık kolonu önde olunca sonraki kolon etkin süzme yapamaz.
## B
`musteri_id` ve `islem_tarihi` için iki ayrı tekli indeks
🤖 iki tekli indeksin birleşimi bileşik indeks kadar dar süzmez.
## C
`(musteri_id, islem_tarihi, tutar)` biçiminde bileşik ve kapsayan indeks
🤖 Doğru: eşitlik önde, aralık sonra, okunan kolon indekste.
## D
`tutar` kolonu üzerinde tek kolonlu bir indeks
🤖 `tutar` süzmede kullanılmıyor; tek başına indekslenmesi kazandırmaz.
## Doğru
C
## Açıklama
Bileşik indekste kolon sırası bir ayrıntı değil, indeksin işe yarayıp yaramamasını belirleyen karardır. Kural şudur: **eşitlikle süzülen kolon önce, aralık koşulu sonra**. `musteri_id` önde olduğunda indeks doğrudan o müşterinin bloğuna iner; `islem_tarihi` o blok içinde sıralı olduğu için tarih aralığı bitişik bir parça olarak okunur. A şıkkında sıra terstir: tarih aralığı geniş bir bölge açar ve `musteri_id` o bölge içinde dağınık durduğu için etkin süzme yapamaz. Üçüncü kolon olan `tutar` ise süzme için değil, **kapsayan indeks** için eklenir. Sorgunun ihtiyacı olan bütün kolonlar indekste bulunduğundan tabloya hiç gidilmez; bu, okuma maliyetini belirgin biçimde düşürür (`146`). B şıkkı iki ayrı indeks önerir; bazı veri tabanları bunları birleştirebilir ama sonuç genellikle tek bileşik indeks kadar iyi olmaz. D şıkkı süzmede kullanılmayan bir kolonu indeksler. Bedeli de söylenmeli: üç kolonlu indeks yazma işlemini ve disk alanını artırır (`29`, `227`).
