---
slug: bulut-temelleri-saglayicidan-bagimsiz
baslik: "Bulut Temelleri: IaaS/PaaS/SaaS, Yönetilen Servis, IAM ve OpenShift"
kademe: Orta
sira: 260
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Ulaşımı düşün. Kendi araban varsa her şey senindir: motor, lastik, sigorta, park yeri.

Kiralık araba tutarsan araç hazır gelir, ama direksiyona sen geçersin ve yakıtı sen koyarsın.

Taksiye binersen sürüşü de bırakırsın; yalnız nereye gideceğini söylersin.

Otobüse binersen güzergâh bile senin değildir; hazır bir hizmete oturursun.

Bulut tam olarak bu dört seçenektir. Sırasıyla: kendi sunucu odan, IaaS, PaaS ve SaaS. Aradaki tek fark şudur — işin ne kadarını sen yaparsın, ne kadarını sağlayıcı yapar.

Bu konu neden gerekli? İş ilanları "AWS, GCP, Azure, OpenShift veya benzeri deneyim" yazıyor. Buradaki anahtar kelime **"veya benzeri"**. İşveren belirli bir satıcının menüsünü ezberlemeni beklemiyor; beklediği şey bulut kavramlarını bilmen ve yeni bir satıcıya hızla uyum sağlaman.

Bu yüzden bu konu **sağlayıcıdan bağımsız** yazıldı. Kavramlar her satıcıda aynıdır; yalnız ürün adları değişir. Konteyner ve Kubernetes ayrıntıları 105, 214, 215 ve 216-219 numaralı konulardadır. Burada tekrarlanmaz; yalnız buluta bakan yüzü anlatılır.

### Temel kavramlar

- **Bulut sağlayıcı (cloud provider):** sunucu, ağ ve depolama kapasitesini kiralayan şirket.
- **Bölge (region):** sağlayıcının veri merkezlerini topladığı coğrafi alan. Örneğin Frankfurt bir bölgedir.
- **Erişilebilirlik alanı (availability zone):** aynı bölge içindeki, birbirinden bağımsız beslenen ayrı veri merkezi. Bir alan çökerse öteki ayakta kalır.
- **IaaS (Infrastructure as a Service):** sana sanal makine, disk ve ağ verilir. İşletim sistemi ve üstü senindir.
- **PaaS (Platform as a Service):** sana çalışma ortamı verilir. Kodunu verirsin, platform çalıştırır.
- **SaaS (Software as a Service):** sana bitmiş uygulama verilir. Yalnız kullanırsın.
- **Paylaşılan sorumluluk modeli (shared responsibility model):** hangi katmanın güvenliği sağlayıcıda, hangisi sende — yazılı ayrım.
- **Yönetilen servis (managed service):** sağlayıcının kurup işlettiği hazır bileşen. Veritabanı, kuyruk, önbellek gibi.
- **Kimlik ve erişim yönetimi (Identity and Access Management, IAM):** kimin neye erişebileceğini tanımlayan sistem.
- **Rol (role):** bir yetki paketinin adı. Kişi ya da uygulama o rolü üstlenir.
- **Politika (policy):** rolün neyi yapabileceğini yazan kural belgesi.
- **İş yükü kimliği (workload identity):** uygulamaya parola vermek yerine, çalıştığı yere bakarak kimlik tanıma yöntemi.
- **Nesne depolama (object storage):** dosyaları anahtar-değer mantığıyla saklayan servis. Klasör ağacı yoktur.
- **Kova (bucket):** nesnelerin toplandığı üst kap. İçindeki her nesnenin bir anahtarı vardır.
- **İmzalı geçici bağlantı (presigned URL):** belirli bir nesneye, belirli bir süre için erişim veren imzalı adres.
- **Dayanıklılık (durability):** verinin kaybolmama garantisi. **Erişilebilirlik (availability)** ise şu an ulaşılabilme garantisidir. İkisi ayrı şeydir.
- **Otomatik ölçekleme (autoscaling):** yüke bakıp kopya sayısını kendiliğinden artırıp azaltmak.
- **Talebe göre ödeme (pay-as-you-go):** peşin donanım almak yerine kullandığın kadar ödemek.
- **Soğuk başlangıç (cold start):** yeni açılan bir kopyanın ilk isteği karşılayana kadar geçen hazırlık süresi.
- **Satıcıya bağlanma (vendor lock-in):** bir sağlayıcının özel servislerine öyle yaslanmak ki çıkmak pahalıya gelir.

### IaaS, PaaS, SaaS ve paylaşılan sorumluluk

Bu üç kısaltma ürün adı değildir. Bir **sorumluluk çizgisidir**. Çizgi yığında nereye çekilirse, model onun adını alır.

**IaaS**'ta sağlayıcı bina, elektrik, ağ, fiziksel sunucu ve sanallaştırmadan sorumludur. Sana boş bir sanal makine düşer; işletim sistemi yamasını, çalışma zamanını, uygulamayı ve veriyi sen yönetirsin. Esneklik en yüksektir; iş yükü de en yüksektir.

**PaaS**'ta sağlayıcı işletim sistemini ve çalışma ortamını da üstlenir. Sen yalnız kodunu ve veriyi verirsin; Java sürümünü yükseltmek ve sunucuyu yamalamak artık senin işin değildir. Karşılığında serbestliğin daralır; platformun izin verdiği şekilde çalışırsın.

**SaaS**'ta uygulama da sağlayıcınındır. E-posta servisi ya da kurumsal İK yazılımı böyledir. Sen yalnız kullanıcısın.

Burada mülakatta en çok kaçırılan nokta şudur: **hiçbir modelde her şey sağlayıcıya geçmez.** Verinin içeriği, kimin hangi veriye eriştiği ve kullanıcı yetkileri her zaman sende kalır. Sağlayıcı "bulutun güvenliğinden" sorumludur, sen "buluttaki verinin güvenliğinden". Bu ayrımın adı paylaşılan sorumluluk modelidir.

```svg
<svg viewBox="0 0 680 348" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="IaaS PaaS SaaS sorumluluk katmanlari karsilastirmasi">
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sorumluluk çizgisi nereye çekilirse, model onun adını alır</text>
  <text x="80" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#5D7789" font-family="system-ui">katman</text>
  <text x="212" y="54" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Kendi sunucu odan</text>
  <text x="344" y="54" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">IaaS</text>
  <text x="476" y="54" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">PaaS</text>
  <text x="608" y="54" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">SaaS</text>
  <text x="146" y="80" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">Uygulama</text>
  <text x="146" y="110" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">Veri ve şema</text>
  <text x="146" y="140" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">Çalışma zamanı</text>
  <text x="146" y="170" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">İşletim sistemi</text>
  <text x="146" y="200" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">Sanallaştırma</text>
  <text x="146" y="230" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">Sunucu donanımı</text>
  <text x="146" y="260" text-anchor="end" font-size="10" fill="#1C3247" font-family="system-ui">Ağ, bina, elektrik</text>
  <rect x="152" y="64" width="120" height="210" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="212" y="174" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SEN</text>
  <rect x="284" y="64" width="120" height="120" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="344" y="129" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SEN</text>
  <rect x="284" y="184" width="120" height="90" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="344" y="234" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">SAĞLAYICI</text>
  <rect x="416" y="64" width="120" height="60" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="476" y="99" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">SEN</text>
  <rect x="416" y="124" width="120" height="150" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="204" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">SAĞLAYICI</text>
  <rect x="548" y="64" width="120" height="210" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="608" y="174" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">SAĞLAYICI</text>
  <line x1="152" y1="94" x2="668" y2="94" stroke="#5D7789" stroke-width="0.5"/>
  <line x1="152" y1="124" x2="668" y2="124" stroke="#5D7789" stroke-width="0.5"/>
  <line x1="152" y1="154" x2="668" y2="154" stroke="#5D7789" stroke-width="0.5"/>
  <line x1="152" y1="184" x2="668" y2="184" stroke="#5D7789" stroke-width="0.5"/>
  <line x1="152" y1="214" x2="668" y2="214" stroke="#5D7789" stroke-width="0.5"/>
  <line x1="152" y1="244" x2="668" y2="244" stroke="#5D7789" stroke-width="0.5"/>
  <text x="340" y="298" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Benzetme: kendi araban · kiralık araba (IaaS) · taksi (PaaS) · otobüs (SaaS)</text>
  <text x="340" y="320" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Hiçbir modelde her şey devrolmaz: verinin içeriği ve erişim yetkisi hep sende.</text>
  <text x="340" y="340" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Sağlayıcı bulutun güvenliğinden, sen buluttaki verinin güvenliğinden sorumlusun.</text>
</svg>
```

### Yönetilen servis: neyi kime devrediyorsun?

Diyelim ki uygulaman için PostgreSQL gerekiyor; önünde iki yol var.

**Birinci yol:** sanal makine kiralarsın ve PostgreSQL'i kendin kurarsın. Yedeklemeyi sen kurarsın, güvenlik yamasını sen geçersin, sürüm yükseltmesini sen planlarsın. Ana sunucu ölürse yedeğe geçişi de sen yaparsın; diskin dolmasını sen izlersin.

**İkinci yol:** sağlayıcının yönetilen veritabanı servisini alırsın. Yedekleme otomatik alınır, yama bakım penceresinde uygulanır, yük devretme (failover) çoğu üründe dakikalar içinde kendiliğinden olur. Sen yalnız bağlantı adresini alırsın.

Peki neyi devretmiş olmazsın? Bu soru mülakatta ayırt edicidir. Şema tasarımı, sorgu performansı ve indeks kararı sende kalır. Bağlantı havuzu ayarı ve maliyet kontrolü de sende kalır. Yönetilen servis **işletim yükünü** alır, **tasarım sorumluluğunu** almaz.

Ne zaman kendi kurulumun daha doğrudur? Üç durumda bu tercih makul olur. Birincisi, sağlayıcının sunmadığı bir eklentiye veya çok özel bir ayara ihtiyacın varsa. İkincisi, mevzuat verinin belirli bir yerde durmasını şart koşuyorsa. Üçüncüsü, ölçek çok büyükse ve yönetilen servisin marjı fatura olarak geri dönüyorsa.

Buna karşılık küçük ve orta ölçekte hesap genelde nettir. Yönetilen servisin ek ücreti, bir gece yarısı veri kaybının maliyetinin yanında küçük kalır. Kıdemli cevap şudur: "yönetilen servis parayla insan saati satın alır".

Ödünç alınan sorumluluğun bir bedeli daha vardır: satıcıya bağlanma. Standart bir motor (PostgreSQL, Redis, Kafka gibi) yönetilen halde alınırsa çıkış kolaydır. Sağlayıcıya özgü tescilli bir servise yaslanırsan çıkış zorlaşır. Ölçüt basittir — "yarın başka sağlayıcıya taşınsam bu parçayı nasıl değiştiririm?"

### Kimlik ve yetki: anahtar gömmeyi bırak

Bulutta her şey bir kimlik kontrolünden geçer; bu katmanın genel adı IAM'dir.

Üç kavram vardır. **Kimlik** (identity) kişiyi ya da uygulamayı temsil eder, **rol** bir yetki paketinin adıdır. **Politika** ise o rolün hangi kaynakta hangi işlemi yapabileceğini yazar. Kimlik doğrulama ile yetkilendirme ayrımı 189 numaralı konuda anlatılır; burada bulut tarafına bakılır.

Temel kural **en az yetki ilkesidir** (least privilege). Uygulama işini görecek kadar yetki alır ve bir gram fazlası verilmez; amaç ihlal anındaki etki alanını küçültmektir.

Şimdi asıl meseleye gelelim: uygulamanın nesne deposuna erişmesi gerekiyor. Yaygın ve yanlış çözüm şudur: bir erişim anahtarı üretilir, yapılandırma dosyasına yazılır ve yıllarca orada durur.

Bu üç ayrı sorun doğurur. Birincisi, anahtar bir kez sızdığında süresi dolmadığı için sonsuza kadar geçerlidir. İkincisi, anahtarı değiştirmek elle yapılan riskli bir iştir ve bu yüzden hep ertelenir. Üçüncüsü, kolaylık olsun diye anahtar genelde geniş yetkiyle üretilir.

Doğru çözümün adı **rol tabanlı iş yükü kimliğidir**. Fikir şudur: uygulamaya parola verme, nerede çalıştığına bak. Platform, uygulamanın hangi kümede ve hangi hesapla çalıştığını zaten bilir; buna dayanarak ona **kısa ömürlü** bir kimlik belgesi verir. Belge birkaç saatte bir kendiliğinden yenilenir; kimse elle bir şey taşımaz.

Kazanç üç katlıdır. Kodda ve yapılandırmada saklanacak sır kalmaz, sızan belge zaten kısa sürede geçersiz olur. Yetki de rol üzerinden tanımlandığı için tek kovayla sınırlanabilir.

Anahtarı ortam değişkenine taşımak da tam çözüm değildir; sır hâlâ uzun ömürlüdür ve elden ele geçer. Ortam değişkeni yalnız yapılandırmayı koddan ayırır (106). Sır yönetiminin kendisi ayrı bir iştir (189).

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sabit gomulu anahtar ile rol tabanli is yuku kimligi karsilastirmasi">
  <defs>
    <marker id="bulut-ok2" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sabit anahtar mı, rol tabanlı kimlik mi?</text>
  <rect x="14" y="38" width="306" height="206" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="167" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış: gömülü sabit anahtar</text>
  <rect x="34" y="72" width="130" height="40" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="99" y="88" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">uygulama</text>
  <text x="99" y="103" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">anahtar dosyada</text>
  <line x1="166" y1="92" x2="196" y2="92" stroke="#5D7789" stroke-width="1.4" marker-end="url(#bulut-ok2)"/>
  <rect x="200" y="72" width="102" height="40" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="251" y="96" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">bulut servisi</text>
  <text x="167" y="136" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">süresiz geçerli — sızarsa hep açık</text>
  <text x="167" y="158" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">değiştirmek elle iş — hep ertelenir</text>
  <text x="167" y="180" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">kolaylık olsun diye geniş yetkili üretilir</text>
  <text x="167" y="202" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">git geçmişine düşerse geri alınamaz</text>
  <text x="167" y="228" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ortam değişkenine taşımak da çözmez</text>
  <rect x="340" y="38" width="306" height="206" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="493" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Doğru: rol tabanlı iş yükü kimliği</text>
  <rect x="360" y="72" width="118" height="40" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="419" y="88" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">uygulama</text>
  <text x="419" y="103" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">sır taşımaz</text>
  <line x1="480" y1="92" x2="508" y2="92" stroke="#5D7789" stroke-width="1.4" marker-end="url(#bulut-ok2)"/>
  <rect x="512" y="72" width="114" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="569" y="88" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">kimlik katmanı</text>
  <text x="569" y="103" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">rol + politika</text>
  <text x="493" y="136" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">kısa ömürlü belge — saatlerde yenilenir</text>
  <text x="493" y="158" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">kimlik çalıştığı yerden türetilir</text>
  <text x="493" y="180" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">politika tek kovayla sınırlanır</text>
  <text x="493" y="202" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">sızan belge kısa sürede geçersiz olur</text>
  <text x="493" y="228" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">en az yetki ilkesi burada uygulanır (189)</text>
  <rect x="14" y="258" width="632" height="36" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="330" y="281" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kural: uygulamaya parola verme; nerede çalıştığına bakıp kısa ömürlü kimlik ver.</text>
</svg>
```

### Nesne depolama neden dosya sistemi değildir?

Nesne depolama bulutun en çok kullanılan servisidir ve en çok yanlış anlaşılanıdır.

Görünüşte klasör gibi durur, ama aslında **büyük bir sözlüktür**. Bir kova vardır, içinde anahtarlar vardır ve her anahtar bir bayt yığınına işaret eder. Anahtardaki eğik çizgiler klasör değildir; yalnız isim içinde geçen karakterlerdir ve arayüz onları klasör gibi gösterir.

Bunun üç pratik sonucu vardır. Birincisi, klasör yeniden adlandırmak diye ucuz bir işlem yoktur. Adı değiştirmek nesneyi kopyalayıp eskisini silmek demektir. İkincisi, nesnenin ortasına yazamazsın; nesne bütün olarak yazılır. Üçüncüsü, listeleme sıralı bir tarama olduğundan milyonlarca anahtarda pahalıdır.

Buna karşılık kazandığın şey büyüktür. Nesne depolama birden çok yere kopyalandığı için **dayanıklılığı** çok yüksektir. Kapasitesi pratikte sınırsız, fiyatı da yerel disk fiyatının çok altındadır. Üstelik ölçeklendiğinde senin bir şey ayarlaman gerekmez.

**Peki neden uygulama sunucusunun diskine dosya yazmak yanlıştır?** Sebebi 12 faktör ilkelerinin altıncı maddesidir (106): süreçler durumsuz ve **atılabilir** olmalıdır. Konteynerin diski geçicidir; kopya yeniden başlatılınca dosya kaybolur. Üstelik üç kopya çalışıyorsa dosya yalnız birinin diskindedir ve kullanıcı ikinci kopyaya düşerse onu bulamaz.

Doğru desen ikiye ayırmaktır: **baytlar nesne deposuna, üstveri veritabanına**. Üstveri ad, boyut, sahip ve izinlerdir. Bu ayrım hem maliyeti hem okuma ölçeğini çözer (150).

İndirmeyi de uygulama üzerinden yapmamak gerekir. Uygulama, kullanıcıya **imzalı geçici bağlantı** üretir. Kullanıcı dosyayı doğrudan depodan çeker. Böylece büyük dosya trafiği uygulama sunucusundan tamamen kalkar. Bağlantı belirli bir süre sonra kendiliğinden geçersiz olur.

### Otomatik ölçekleme ve maliyet modeli

Klasik veri merkezinde kapasite peşin alınır: yılın en yoğun gününe göre sunucu satın alırsın ve yılın geri kalanında o kapasite boş durur.

Bulutun ticari vaadi bunu tersine çevirir: **talebe göre ödeme**. Kapasiteyi ihtiyaç anında açar, ihtiyaç geçince kapatırsın; otomatik ölçekleme bu açıp kapamayı senin yerine yapar.

Ölçekleme yatay yapılır: makineyi büyütmek yerine kopya sayısını artırırsın (151). Bunun bir ön şartı vardır ve mülakatta cevabın kalbi budur: kopyalar birbirinin yerine geçebilmelidir. Bu da **durumsuzluk** demektir.

Servis oturumu kendi belleğinde tutuyorsa ne olur? Yeni açılan kopya o oturumu bilmez ve kullanıcı ona düşünce "oturumunuz düştü" hatası alır. Ölçek küçülünce de bellekteki oturumlar tamamen kaybolur. Çözüm durumu paylaşılan bir katmana taşımaktır: oturum önbelleğe, dosya nesne deposuna, veri veritabanına.

İkinci tuzak **soğuk başlangıçtır**. Yeni kopya bir anda hazır olmaz: konteyner imajı çekilir, JVM açılır, bağlantı havuzu kurulur ve önbellek boştur. Bu hazırlık süresi saniyelerle ölçülür. Yani ölçekleme ani yük sıçramasına yetişemeyebilir. Pratikte iki önlem alınır: eşiği erken tetiklemek ve bir miktar yedek kapasiteyi hazır tutmak.

Üçüncü tuzak **salınımdır**. Ölçek yukarı ve aşağı çok hızlı tepki verirse kopya sayısı dakikada bir inip çıkar; bunun önlemi ölçek-aşağı yönüne bir bekleme penceresi koymaktır. Kubernetes tarafındaki karşılığı 218 numaralı konuda anlatılır.

Maliyet tarafında iki not önemlidir. Otomatik ölçekleme yanlış kurulursa faturayı **düşürmez, artırır**. Ve veri merkezinden dışarı çıkan trafik çoğu sağlayıcıda ayrıca ücretlidir; bu kalem çoğu ekibi şaşırtır.

### OpenShift: Kubernetes'in kurumsal dağıtımı

İlanlarda OpenShift adı sık geçer ve adayları tedirgin eder; oysa cevap rahatlatıcıdır.

OpenShift, Red Hat'in **Kubernetes dağıtımıdır**. Altında gerçek Kubernetes çalışır: Pod, Deployment, Service, ConfigMap ve Secret hepsi aynıdır, `kubectl` komutları da çalışır. Yani 105 ve 216-219 numaralı konularda öğrendiğin her şey doğrudan geçerlidir.

Üstüne eklenen şey kurumsal paketleme ve daha katı varsayılanlardır; pratikte beş farkı bilmek yeter.

**Bir: `Route`.** Kubernetes'te dışa açmanın yolu Ingress'tir (217). OpenShift bunu kendinden önce çözdüğü için `Route` adlı kendi nesnesini kullanır. İşi aynıdır: alan adı, yol ve TLS sonlandırma. OpenShift standart Ingress nesnesini de kabul eder ve arka planda onu Route'a çevirir.

**İki: `DeploymentConfig`.** OpenShift'in eski, kendine özgü dağıtım nesnesidir. Standart `Deployment`'tan farkı, tetikleyici (trigger) kavramı ve farklı yayım stratejileridir. Yeni sürümlerde kullanımdan kaldırılmıştır; yeni işlerde standart `Deployment` önerilir. Ama eski kurumsal kümelerde hâlâ karşına çıkar.

**Üç: `BuildConfig` ve S2I.** Kubernetes imaj derlemez; imajı sen dışarıda derler ve depoya itersin (214). OpenShift ise derlemeyi kümenin içine alır. `BuildConfig` kaynak kodun nereden alınacağını ve nasıl derleneceğini yazar. **S2I** (Source-to-Image) ise Dockerfile yazmadan, kaynak koddan doğrudan imaj üretir; Java için hazır bir taban imaj vardır, kodu koyarsın ve imaj çıkar. CI/CD ile ilişkisi 220 numaralı konudadır.

**Dört: entegre imaj deposu.** OpenShift kendi imaj deposuyla gelir. `ImageStream` adlı nesne, imajın yeni sürümünü izler ve dağıtımı tetikleyebilir.

**Beş — ve en çok yaşanan fark: katı güvenlik varsayılanı.** Kubernetes'te bir konteyner varsayılan olarak `root` kullanıcısıyla çalışabilir; OpenShift ise buna izin vermez. Pod'a rastgele ve yüksek numaralı bir kullanıcı kimliği (UID) atanır. Bunu yöneten mekanizmanın adı güvenlik bağlam kısıtıdır (Security Context Constraints, SCC).

Sonuç şudur: kendi bilgisayarında sorunsuz çalışan imaj OpenShift'te başlamaz. En sık iki hata görülür. Birincisi, imaj `root` bekleyen bir işlem yapar ve izin hatası alır. İkincisi, uygulama bir dizine yazmaya çalışır ama o dizin rastgele kimliğe yazılabilir değildir.

Çözüm imajı baştan uyumlu yazmaktır. Konteyner içinde root olarak çalışma ve 1024'ün altındaki portlara bağlanma. Yazılacak dizinlerin grup sahipliğini `0` yap, sonra o gruba yazma izni ver. Bu kurallar aslında iyi konteyner alışkanlıklarıdır (214); OpenShift onları zorunlu kılar.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kubernetes ile OpenShift kavram eslemesi ve varsayilan guvenlik farki">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kubernetes bilgisi taşınır: OpenShift eşlemesi</text>
  <text x="170" y="46" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Kubernetes</text>
  <text x="486" y="46" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">OpenShift</text>
  <rect x="14" y="56" width="312" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="170" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Ingress ile dışa açma (217)</text>
  <text x="333" y="78" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="340" y="56" width="306" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="493" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Route (Ingress da kabul edilir)</text>
  <rect x="14" y="96" width="312" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="170" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Deployment (216)</text>
  <text x="333" y="118" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="340" y="96" width="306" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="493" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Deployment + eski DeploymentConfig</text>
  <rect x="14" y="136" width="312" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="170" y="158" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">imajı dışarıda derle, depoya it (214)</text>
  <text x="333" y="158" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="340" y="136" width="306" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="493" y="158" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">BuildConfig / S2I: küme içinde derleme</text>
  <rect x="14" y="176" width="312" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="170" y="198" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">harici imaj deposu</text>
  <text x="333" y="198" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="340" y="176" width="306" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="493" y="198" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">entegre depo + ImageStream</text>
  <rect x="14" y="216" width="312" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="170" y="238" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kubectl</text>
  <text x="333" y="238" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="340" y="216" width="306" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="493" y="238" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">oc (kubectl de çalışır)</text>
  <rect x="14" y="256" width="312" height="34" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="170" y="278" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">konteyner root çalışabilir</text>
  <text x="333" y="278" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">→</text>
  <rect x="340" y="256" width="306" height="34" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="493" y="278" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">rastgele UID, root yasak (SCC)</text>
  <text x="330" y="310" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Amber satır en sık yaşanan farktır: yerelde çalışan imaj OpenShift'te başlamaz.</text>
  <text x="330" y="326" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Gerisi kavramsal olarak aynıdır; öğrenme maliyeti günlerle ölçülür, aylarla değil.</text>
</svg>
```

### "Bulut deneyiminiz var mı?" sorusuna dürüst köprü

Bu bölüm bilgi değil strateji anlatır; ama mülakatın sonucunu belirleyen çoğu zaman burasıdır.

Durum şudur: ilan bulut istiyor, ama sen yönetilen bir bulutta çalışmadın. Kubernetes kurdun, konteyner yazdın, yatay ölçekledin ve sır yönettin; yalnız faturası gelen bir bulut hesabın hiç olmadı.

**Uydurmak yasaktır.** Sebebi ahlaki olduğu kadar pratiktir: mülakatçı bir alt soruyla anlar. "Hangi bölgede çalışıyordunuz?" ya da "IAM politikasını kim yazıyordu?" gibi bir soru yeter. Yakalandığın anda yalnız o soruyu değil, tüm görüşmeyi kaybedersin.

Doğru yapı üç adımlıdır.

**Adım bir: sınırı net söyle.** Örneğin: "Yönetilen bir bulut sağlayıcısında üretim iş yükü işletmedim." Tek cümle kurarsın; özür de savunma da gerekmez. Bu açıklık sana güvenilirlik kazandırır.

**Adım iki: taşınan deneyimi bağla.** Burada somut olman gerekir. Önce yeri söyle: kendi kurduğunuz Kubernetes kümesinde servisleri işlettiniz. Sonra işi bağla: konteyner imajı üretmek, sağlık kontrolü ve zarif kapanış, yatay ölçekleme, yapılandırmayı dışarıdan vermek ve sırları kasada tutmak. Bu işlerin hepsi bulutta da aynı adla ve aynı mantıkla vardır.

**Adım üç: farkın nerede olduğunu bildiğini göster.** Bu adım seni ezberci adaydan ayırır. Eksik kısmı kendin adlandır: sağlayıcıya özgü kimlik modeli, yönetilen servis seçimi ve maliyet yönetimi. Sonra somut bir örnek ver. Bulutta anahtar gömmek yerine rol tabanlı iş yükü kimliği kullanılır. Kavramı bildiğini, ama o sağlayıcının arayüzünü kullanmadığını söyle.

Kapanış cümlesi şöyle olabilir: "Kavramlar aynı, satıcı arayüzü farklı. Öğrenme maliyetini haftalarla ölçüyorum."

Aynı yapı OpenShift için de işler. "OpenShift kullanmadım; Kubernetes kullandım. Route, S2I ve rastgele kullanıcı kimliği farklarını biliyorum" cümlesi, "evet kullandım" demekten çok daha güçlüdür.

Son bir uyarı: bu köprü ancak arkasında gerçek deneyim varsa taşır. Kubernetes'i de bilmiyorsan köprünün öteki ucu yoktur ve o zaman doğru cevap öğrenme planını anlatmaktır.

### Mülakatta ne sorulur?

- **"IaaS, PaaS, SaaS farkı nedir?"** Sorumluluk çizgisinin yığında nereye çekildiğini anlat. Fiyat kademesi değildir.
- **"Paylaşılan sorumluluk ne demek?"** Sağlayıcı bulutun güvenliğinden, sen buluttaki verinin güvenliğinden sorumlusun. Hiçbir modelde erişim yetkisi sağlayıcıya geçmez.
- **"Yönetilen veritabanı mı, kendi kurulumun mu?"** Yönetilen servis işletim yükünü alır: yedekleme, yama, yük devretme. Şema, sorgu ve maliyet sende kalır.
- **"Uygulama bulut deposuna nasıl erişmeli?"** Sabit anahtar gömme; rol tabanlı iş yükü kimliği kullan, kısa ömürlü belge al, en az yetki uygula (189).
- **"Yüklenen dosyaları nereye yazarsın?"** Nesne deposuna. Baytlar depoda, üstveri veritabanında, indirme imzalı geçici bağlantıyla (106, 150).
- **"Otomatik ölçekleme neyi şart koşar?"** Durumsuzluğu. Oturum bellekte durursa ölçekleme kullanıcıyı düşürür (151, 218).
- **"OpenShift ile Kubernetes farkı ne?"** OpenShift bir Kubernetes dağıtımıdır. Route, DeploymentConfig, BuildConfig/S2I, entegre depo ve katı güvenlik varsayılanı eklenir.
- **"Neden imajım OpenShift'te başlamadı?"** Büyük olasılıkla root bekliyordur ya da rastgele kullanıcı kimliğinin yazamadığı bir dizine yazıyordur.
- **"Bulut deneyiminiz var mı?"** Sınırı net söyle, taşınan deneyimi bağla, farkın nerede olduğunu göster. Uydurma.

### Tek cümlede

Bulut, işin ne kadarını sağlayıcının yaptığına göre IaaS, PaaS ve SaaS diye ayrılır; ama veri ve erişim sorumluluğu her modelde sende kalır. Yönetilen servis işletim yükünü satın alır, tasarım sorumluluğunu değil. Uygulama sabit anahtar taşımaz, rol tabanlı kısa ömürlü kimlik alır. Dosya yerel diske değil nesne deposuna yazılır; ölçekleme durumsuzluk ister. OpenShift ise kurumsal bir Kubernetes dağıtımıdır — Route, S2I ve root yasağı dışında bildiğin her şey aynen geçerlidir.

# bulut-1 | Temel
## Soru
Bir iş ilanı "AWS, GCP, OpenShift, Azure veya benzeri deneyim" istiyor. Mülakatta "IaaS, PaaS ve SaaS arasındaki fark nedir?" diye soruluyor. En doğru cevap hangisidir?
## A
Üçü aynı hizmetin farklı satıcılardaki ticari adıdır; teknik olarak aralarında bir fark yoktur
🤖 satıcı adı değil, sorumluluk modeli ayrımıdır; aynı satıcı üç modeli birden sunar.
## B
Üçü bir fiyat kademesidir: IaaS ucuz, PaaS orta, SaaS pahalı sunucu kiralama paketi anlamına gelir
🤖 fiyat sonuçtur, tanım değil; PaaS bazı yüklerde IaaS'tan ucuza gelebilir.
## C
IaaS yalnız sanal sunucu, PaaS yalnız veritabanı, SaaS ise yalnız hazır uygulama satan ayrı pazarlardır
🤖 PaaS veritabanı pazarı değildir; kod çalıştıran bir platform katmanıdır.
## D
Üçü, sorumluluğun yığında nereye kadar sağlayıcıya geçtiğini belirler; işletim sistemi, çalışma ortamı ve uygulama sırayla el değiştirir
🤖 Doğru: ayrım katman ayrımıdır — IaaS'ta işletim sistemi sende, PaaS'ta değil.
## Doğru
D
## Açıklama
Bu üç kısaltma ürün adı değil, bir **sorumluluk çizgisidir**. Çizgi yığında nereye çekilirse model onun adını alır. **IaaS**'ta sağlayıcı bina, elektrik, ağ, fiziksel sunucu ve sanallaştırmayı üstlenir; sana boş bir sanal makine kalır. İşletim sistemi yaması, çalışma zamanı, uygulama ve veri senindir. **PaaS**'ta sağlayıcı işletim sistemini ve çalışma ortamını da alır; sen kod ile veriyi verirsin. **SaaS**'ta uygulama da sağlayıcınındır; sen yalnız kullanıcısın. Benzetme şudur: kendi araban, kiralık araba, taksi, otobüs. A şıkkı ayrımı satıcı markasına indirger; oysa aynı satıcı üç modeli birden satar. B şıkkı fiyat kademesi sanır; fiyat modelin sonucudur, tanımı değildir. C şıkkı her modele tek bir ürün türü yakıştırır; PaaS bir veritabanı pazarı değil, kod çalıştıran platform katmanıdır. Cevaba mutlaka eklenmesi gereken bir cümle daha vardır: **hiçbir modelde her şey devrolmaz**. Verinin içeriği, kimin neye eriştiği ve kullanıcı yetkileri her zaman sende kalır. Bunun adı paylaşılan sorumluluk modelidir — sağlayıcı bulutun güvenliğinden, sen buluttaki verinin güvenliğinden sorumlusun.

# bulut-2 | Orta
## Soru
Ekip bulutta PostgreSQL kullanacak. İki seçenek var: sanal makineye kendi elleriyle kurmak ya da sağlayıcının yönetilen veritabanı servisini almak. Yönetilen servisi seçmenin anlamı hangi seçenekte doğru verilmiştir?
## A
Sorgu ayarı ve indeks kararı da sağlayıcıya geçer; performans sorunları artık uygulama ekibinin işi değildir
🤖 şema, sorgu ve indeks kararları her zaman uygulama ekibinde kalır.
## B
Yedekleme, yama, sürüm yükseltme ve yük devretme sağlayıcıya geçer; şema, sorgu ve maliyet kararları sende kalır
🤖 Doğru: yönetilen servis işletim yükünü alır, tasarım sorumluluğunu almaz.
## C
Yönetilen servis her ölçekte daha ucuzdur; kendi kurulumun yalnız eski sistemlerde tercih edilen bir yöntemdir
🤖 çok büyük ölçekte hizmet marjı faturaya biner; karar ölçeğe ve mevzuata bağlıdır.
## D
Yönetilen servis veritabanını kendiliğinden sınırsız ölçekler; kötü sorgu yazsan bile darboğaz oluşmaz
🤖 yönetilen olmak fizik yasalarını değiştirmez; kötü sorgu yine kilitler ve yavaşlatır.
## Doğru
B
## Açıklama
Yönetilen servis **işletim yükünü** satın alır. Kendi kurulumunda yedeklemeyi sen kurarsın, güvenlik yamasını sen geçersin, sürüm yükseltmesini sen planlarsın, ana sunucu ölünce yedeğe geçişi sen yaparsın. Yönetilen serviste bunlar sağlayıcıya geçer; yedek otomatik alınır, yama pencerede uygulanır, yük devretme çoğu üründe otomatiktir. Devretmediğin şey ise **tasarım sorumluluğudur**: şema, indeks, sorgu performansı, bağlantı havuzu ayarı ve maliyet kontrolü sende kalır. A şıkkı tam bu sınırı kaçırır; sağlayıcı senin sorgunu yazmaz. D şıkkı "yönetilen = sınırsız" yanılgısıdır; yönetilen olmak kötü bir sorgunun kilitlenmesini önlemez. C şıkkı kararı tek yöne sabitler; oysa üç durumda kendi kurulumun daha doğrudur — sağlayıcının sunmadığı bir eklentiye ihtiyaç varsa, mevzuat verinin yerini şart koşuyorsa, ya da ölçek büyüdükçe hizmet marjı faturaya ağır biniyorsa. Kıdemli özet şudur: **yönetilen servis parayla insan saati satın alır**. Ek olarak satıcıya bağlanma (vendor lock-in) sorulur. Standart bir motoru (PostgreSQL, Redis, Kafka) yönetilen halde almak çıkışı kolay bırakır; sağlayıcıya özgü tescilli bir servise yaslanmak çıkışı pahalı yapar.

# bulut-3 | Orta
## Soru
Bir Spring Boot servisi kullanıcı belgelerini kabul ediyor ve dosyaları uygulama sunucusunun yerel diskine yazıyor. Servis bulutta üç kopya halinde çalışıyor ve kopyalar sık sık yeniden başlatılıyor. Kullanıcılar yükledikleri dosyayı bazen bulamıyor. Doğru yaklaşım hangisidir?
## A
Kullanıcıyı oturum yapışkanlığıyla hep aynı kopyaya sabitlemek; böylece dosyayı yazan kopya onu yine bulur
🤖 kopya yeniden başlayınca disk zaten silinir; yapışkanlık ölçeklemeyi de bozar.
## B
Dosyayı ikili veri olarak ilişkisel veritabanına yazmak; yedekleme ve tutarlılık tek yerden yönetilmiş olur
🤖 büyük baytlar veritabanını şişirir, yedeği ve önbelleği bozar; ölçek pahalıya gelir.
## C
Baytları nesne depolamaya yazmak, üstverisini veritabanında tutmak, indirmeyi imzalı geçici bağlantıyla vermek
🤖 Doğru: baytlar ile üstveriyi ayırmak buluttaki temel dosya desenidir.
## D
Üç kopyanın diskini gece çalışan toplu bir işle eşitlemek; böylece sabaha üç kopyada da aynı dosyalar bulunur
🤖 eşitleme gecikmeli ve kırılgandır; gün içinde yüklenen dosya yine kaybolur.
## Doğru
C
## Açıklama
Sorunun kökü 12 faktör ilkelerinin altıncı maddesidir (106): süreçler **durumsuz ve atılabilir** olmalıdır. Konteynerin diski geçicidir; kopya yeniden başlayınca yazılan dosya kaybolur. Üstelik üç kopya varsa dosya yalnız birinin diskindedir ve kullanıcı ikinci kopyaya düşünce dosyayı bulamaz. Doğru desen ikiye ayırmaktır: **baytlar nesne deposuna, üstveri veritabanına** (150). Üstveri ad, boyut, sahip ve izinlerdir. İndirme de uygulama üzerinden yapılmaz; uygulama **imzalı geçici bağlantı** üretir, kullanıcı dosyayı doğrudan depodan çeker ve bağlantı süresi dolunca geçersiz olur. Böylece büyük dosya trafiği uygulama sunucusundan tamamen kalkar. A şıkkı yapışkan oturum önerir; bu hem sorunu çözmez (yeniden başlayan kopyanın diski zaten silinir) hem yük dengesini ve ölçeklemeyi bozar. B şıkkı baytları ilişkisel veritabanına koyar; veritabanı şişer, yedekleme süresi uzar, önbellek verimi düşer ve maliyet hızla artar. D şıkkı gecikmeli eşitleme kurar; gün içinde yüklenen dosya hâlâ tek kopyadadır ve senkron tutarlılık yoktur. Son not: nesne depolama bir dosya sistemi değildir. Kova içinde anahtar-değer mantığıyla çalışır; eğik çizgi klasör değil, isim içindeki karakterdir.

# bulut-4 | İleri
## Soru
Bir uygulama bulut nesne deposuna erişiyor. Erişim anahtarı yapılandırma dosyasına yazılmış, iki yıldır değişmemiş ve tam yetkili bir hesaba ait. Güvenlik incelemesinde bu bulgu çıkıyor. Doğru düzeltme hangisidir?
## A
İş yüküne bağlı rol tabanlı kimliğe geçmek; uygulama kısa ömürlü belge alsın, rolün politikası tek kovayla sınırlansın
🤖 Doğru: sır ortadan kalkar, belge kendiliğinden yenilenir, yetki daraltılır.
## B
Anahtarı ortam değişkenine taşımak; koddan çıkarıldığı için sır artık güvenli sayılır ve bulgu kapanır
🤖 ortam değişkeni yapılandırmayı ayırır; uzun ömürlü sır sorunu aynen durur.
## C
Anahtarı üç ayda bir elle değiştirmek; geçerlilik süresi kısaldığı için sızma riski tamamen ortadan kalkar
🤖 elle rotasyon iyileştirir ama unutulur; üç ay boyunca sır yine geçerli kalır.
## D
Anahtarı base64 ile kodlayıp depoda saklamak; okunamaz hale geldiği için sızsa bile kullanılamaz
🤖 base64 kodlamadır, şifreleme değil; herkes tek komutla geri çevirir (189).
## Doğru
A
## Açıklama
Sabit gömülü anahtarın üç ayrı sorunu vardır. Birincisi süresizdir; bir kez sızarsa sonsuza kadar geçerlidir. İkincisi değiştirmek elle yapılan riskli bir iştir, bu yüzden hep ertelenir. Üçüncüsü kolaylık olsun diye genelde geniş yetkiyle üretilir; ihlal anındaki etki alanı büyür. Doğru çözümün adı **rol tabanlı iş yükü kimliğidir**. Fikir şudur: uygulamaya parola verme, **nerede çalıştığına bak**. Platform, uygulamanın hangi kümede ve hangi hesapla çalıştığını zaten bilir; buna dayanarak ona **kısa ömürlü** bir kimlik belgesi verir. Belge birkaç saatte bir kendiliğinden yenilenir ve kimse elle sır taşımaz. Yetki de rol ve politika üzerinden tanımlandığı için **en az yetki ilkesine** (189) uygun biçimde tek kovayla sınırlanabilir. B şıkkı kısmi bir iyileştirmedir: ortam değişkeni yapılandırmayı koddan ayırır (106), ama uzun ömürlü sırrın kendisi aynen durur. C şıkkı da kısmi iyileştirmedir; elle rotasyon pratikte unutulur ve iki rotasyon arasında sır tam yetkiyle geçerlidir. D şıkkı klasik yanılgıdır — base64 bir kodlamadır, şifreleme değildir ve tek komutla geri çevrilir. Ek olarak şu da söylenmelidir: anahtar git geçmişine düştüyse artık yanmış sayılır ve mutlaka iptal edilir.

# bulut-5 | İleri
## Soru
Bir servis bulutta otomatik ölçeklemeye alındı. Yük artınca kopya sayısı ikiden ona çıkıyor. Ama kullanıcılar rastgele "oturumunuz düştü" hatası almaya başladı ve fatura beklenenin üstünde geldi. Kök sebep ve doğru düzeltme nedir?
## A
Ölçekleme eşiği fazla düşük ayarlanmış; eşiği yükseltmek hem oturum hatasını hem de yüksek faturayı çözer
🤖 eşik faturayı etkiler ama oturum kaybının sebebi eşik değil, bellekteki durumdur.
## B
Kopya sayısı hâlâ yetersiz kalıyor; üst sınırı yirmiye çıkarmak oturum hatalarını tamamen ortadan kaldırır
🤖 kopya artırmak sorunu büyütür; her yeni kopya oturumu yine bilmez.
## C
Servis oturumu kendi belleğinde tutuyor; durum paylaşılan bir katmana taşınmalı ve ölçek-aşağı yumuşatılmalıdır
🤖 Doğru: yatay ölçekleme durumsuzluk ister; salınım için bekleme penceresi gerekir.
## D
Bulut sağlayıcısının yük dengeleyicisi arızalı çalışıyor; yapışkan oturum açmak bunun kalıcı çözümüdür
🤖 yapışkanlık belirtiyi örter; kopya ölünce oturum yine kaybolur ve yük dengesizleşir.
## Doğru
C
## Açıklama
Yatay ölçeklemenin tek ön şartı vardır: **kopyalar birbirinin yerine geçebilmelidir** (151). Bu da durumsuzluk demektir. Servis oturumu kendi belleğinde tutuyorsa yeni açılan kopya o oturumu bilmez; kullanıcı ona düştüğünde "oturumunuz düştü" hatası alır. Ölçek küçülünce bellekteki oturumlar tamamen kaybolur. Çözüm durumu paylaşılan bir katmana taşımaktır: oturum önbelleğe, dosya nesne deposuna, veri veritabanına. Faturanın şişmesi ise ikinci bir belirtidir ve genelde **salınımdan** gelir: ölçek yukarı ve aşağı çok hızlı tepki verirse kopya sayısı dakikada bir inip çıkar, her açılış yeniden ücretlenir. Önlemi ölçek-aşağıya bir bekleme penceresi koymaktır; Kubernetes tarafındaki karşılığı 218 numaralı konudadır. A şıkkı yalnız maliyet tarafına dokunur ve oturum kaybının sebebini ıskalar. B şıkkı sorunu ters yönde büyütür; kopya arttıkça kullanıcının "yanlış" kopyaya düşme olasılığı da artar. D şıkkı belirtiyi örter: yapışkan oturum kullanıcıyı bir kopyaya bağlar, ama o kopya ölünce oturum yine gider ve yük dengesizleşir. Üçüncü bir gerçek de not edilmelidir: **soğuk başlangıç**. Yeni kopya bir anda hazır olmaz — imaj çekilir, JVM açılır, havuz kurulur, önbellek boştur. Bu yüzden ölçeklemenin ani sıçramaya yetişmesi için eşik erken tetiklenir ve bir miktar yedek kapasite hazır tutulur.

# bulut-6 | Uzman
## Soru
İlanda OpenShift deneyimi isteniyor. Sen kendi kurduğunuz Kubernetes kümesinde iki yıl servis işlettin; OpenShift'e hiç dokunmadın. Mülakatçı "OpenShift deneyiminiz var mı?" diye soruyor. En iyi cevap hangisidir?
## A
"Evet, OpenShift kullandım" deyip Kubernetes deneyimini OpenShift adıyla anlatmak; kavramlar aynı olduğu için fark edilmez
🤖 tek alt soruda çöker; Route veya SCC sorulunca cevap yoktur ve güven biter.
## B
"Hayır" deyip konuyu kapatmak; bilmediğin bir ürün üzerine konuşmak riskli olacağı için sessiz kalmak
🤖 dürüst ama eksik; taşınabilir deneyimini anlatmadan fırsatı kendin kapatırsın.
## C
OpenShift ile Kubernetes arasında hiçbir fark olmadığını söyleyip soruyu geçersiz saymak ve konuyu değiştirmek
🤖 fark vardır (Route, S2I, SCC); yok demek bilgisizlik olarak okunur.
## D
OpenShift kullanmadığını açıkça söylemek; Kubernetes deneyiminin taşındığını ve Route, S2I, rastgele kullanıcı kimliği gibi farkları bildiğini anlatmak
🤖 Doğru: sınırı söyle, taşınan deneyimi bağla, farkı bildiğini göster.
## Doğru
D
## Açıklama
Doğru yapı üç adımlıdır. **Bir: sınırı net söyle** — "OpenShift kullanmadım". Tek cümle, özür yok. Bu cümle güvenilirlik kazandırır. **İki: taşınan deneyimi bağla** — kendi kurduğun Kubernetes kümesinde konteyner imajı üretmek, sağlık kontrolü ve zarif kapanış, yatay ölçekleme, yapılandırmayı dışarıdan vermek, sırları kasada tutmak (105, 214, 216-219). Bunların hepsi OpenShift'te aynı adla ve aynı mantıkla vardır; çünkü OpenShift bir Kubernetes **dağıtımıdır** ve altında gerçek Kubernetes çalışır. **Üç: farkın nerede olduğunu göster** — `Route` (Ingress yerine), `DeploymentConfig` (eski, artık kullanımdan kaldırılan dağıtım nesnesi), `BuildConfig`/S2I (derlemenin kümenin içine alınması), entegre imaj deposu ve en önemlisi katı güvenlik varsayılanı. OpenShift konteyneri varsayılan olarak `root` çalıştırmaz; pod'a rastgele yüksek numaralı bir kullanıcı kimliği atanır ve bunu SCC yönetir. En sık yaşanan hata da budur: yerelde çalışan imaj OpenShift'te başlamaz, çünkü root bekler ya da yazamadığı bir dizine yazmaya çalışır. A şıkkı en tehlikelisidir; uydurma tek bir alt soruyla çöker ve yalnız o soruyu değil tüm görüşmeyi kaybettirir. B şıkkı dürüsttür ama eksiktir; taşınabilir deneyim anlatılmadığı için fırsat kapanır. C şıkkı bilgisizlik olarak okunur — farklar gerçektir ve günlük işi doğrudan etkiler. Kapanış cümlesi şu olabilir: "Kavramlar aynı, satıcı arayüzü farklı; öğrenme maliyetini haftalarla ölçüyorum." Bu köprünün taşıması için arkasında gerçek Kubernetes deneyimi bulunmalıdır; yoksa doğru cevap öğrenme planını anlatmaktır.
