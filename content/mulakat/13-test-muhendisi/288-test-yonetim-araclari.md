---
slug: test-yonetim-araclari
baslik: "Test Yönetim Araçları: Jira Hata Yaşam Döngüsü, HP ALM, TFS/Azure DevOps ve Raporlama"
kademe: Orta
sira: 288
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş gerçek mülakat sorusu var. Beşi de açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur.

İlan üç aracı birden sayıyor: Jira, TFS ve HP ALM. Bu liste seni ürkütmesin, üçünü birden ezberlemen beklenmiyor. Beklenen şey daha basit: hangi aracın hangi soruyu kolaylaştırdığını görebilmek. Jira'nın kendisi bu modülde `229`'da işlendi; kayıt tipleri, şiddet ile öncelik ayrımı ve izlenebilirlik matrisi orada duruyor. Burada onu tekrarlamıyoruz. Burada iki şeye bakıyoruz: aynı bilgiyi mülakatta nasıl anlatırsın ve diğer iki araç neyi farklı yapar.

Bütün bölümün tek bir omurgası var, cümleyi şimdiden ezberle: **araç tartışması aslında süreç tartışmasıdır.** Araç kötü bir süreci düzeltmez, onu yalnız daha hızlı işletir. Kayıt disiplini olmayan bir ekip Jira'da da dağınıktır, ALM'de de. Bu cümleyi mülakatta doğru yerde kurarsan, araç listesi sayan adaydan ayrılırsın. Yanlış yerde kurarsan kibirli duyulur; bu yüzden onu her zaman somut bir örnekle beraber söyle.

Son bir uyarı: kullanmadığın aracı kullanmış gibi anlatma. Bu, mülakatın en çabuk yakalanan yalanıdır. Görüşmeci tek bir ekran adı ya da tek bir alan sorar ve cevap gelmez. Kullanmadığın araç için doğru cümle şudur: kavramı biliyorum, karşılığını şu araçta yaptım, öğrenme süresi kısadır. Dürüst köprünün nasıl kurulduğu `256`'da anlatılıyor.

### "Jira'da hata yaşam döngüsünü anlatır mısın?"

**Soru aslında ne ölçüyor?**

Görüşmeci Jira'yı açıp açmadığını sormuyor; bunu zaten varsayıyor. Ölçtüğü şey kaydın hangi elden hangi ele geçtiğini bilip bilmediğin. Zayıf aday durum adlarını sırayla sayar ve susar. Güçlü aday her geçişin sahibini söyler, çünkü asıl disiplin oradadır. İkinci ayrım noktası çözülmeden kapanan kayıtlardır; bunları doğru anlatan aday sahada durmuş demektir.

**Sözlü cevap iskeleti**

1. **Durum adlarının sabit olmadığını en başta söyle.** Jira'da iş akışı yapılandırılabilir; her ekibin adları farklıdır. Sabit olan şey mantıktır: açılış, triyaj, düzeltme, doğrulama, kapanış (`229`).
2. **Akışı sırayla ver.** Açıldı, triyajda öncelik ve şiddet netleşir, geliştiriciye atanır, düzeltilir ve "Çözüldü" olur, testçi doğrular, sonra "Kapandı" olur.
3. **Her geçişin sahibini adlandır.** Kaydı testçi açar, önceliği triyaj verir, çözümü geliştirici yazar, **doğrulamayı ve kapatmayı testçi yapar.** Bu ayrımı söylemek cevabın en değerli yeridir.
4. **"Yeniden açıldı" (Reopened) durumunu açıkla.** Doğrulama başarısız oldu demektir. İki farklı sebebi olabilir: düzeltme eksiktir ya da hata raporu baştan belirsizdir. Bu yüzden yeniden açılma oranı bir süreç göstergesidir.
5. **Çözülmeden kapanan kayıtları anlat.** Won't Fix bilinçli bir risk kabulüdür ve kararı iş tarafı verir. Duplicate aynı kök nedenin ikinci kaydıdır, silinmez, ana kayda bağlanır. Cannot Reproduce bilgi eksikliğidir; kapatmadan önce ortam, sürüm ve veri istenir. Not a Bug ise gereksinim yorumu farkıdır, kabul ölçütüne geri dönülür.
6. **Alanlarla bitir.** Etkilenen sürüm, düzeltildiği sürüm ve bileşen alanları doldurulmazsa sonradan rapor çıkmaz. Kapanış raporu bu alanlardan üretilir (`229`).

```svg
<svg viewBox="0 0 660 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hata yasam dongusu: durumlar, gecisi kim yapar ve cozulmeden kapanan kaydin bicimleri">
  <defs>
    <marker id="tya-akis" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="tya-geri" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Durum adı ekipten ekibe değişir; değişmeyen şey geçişi kimin yaptığıdır</text>
  <rect x="14" y="40" width="92" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="60" y="66" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Açıldı</text>
  <text x="60" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">testçi açar</text>
  <line x1="108" y1="61" x2="120" y2="61" stroke="#5D7789" stroke-width="1.4" marker-end="url(#tya-akis)"/>
  <rect x="122" y="40" width="92" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="168" y="66" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Triyaj</text>
  <text x="168" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">ürün sahibi önceler</text>
  <line x1="216" y1="61" x2="228" y2="61" stroke="#5D7789" stroke-width="1.4" marker-end="url(#tya-akis)"/>
  <rect x="230" y="40" width="92" height="42" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="276" y="66" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Devam ediyor</text>
  <text x="276" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">geliştirici düzeltir</text>
  <line x1="324" y1="61" x2="336" y2="61" stroke="#5D7789" stroke-width="1.4" marker-end="url(#tya-akis)"/>
  <rect x="338" y="40" width="92" height="42" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="384" y="66" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Çözüldü</text>
  <text x="384" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">geliştirici işaretler</text>
  <line x1="432" y1="61" x2="444" y2="61" stroke="#5D7789" stroke-width="1.4" marker-end="url(#tya-akis)"/>
  <rect x="446" y="40" width="92" height="42" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="492" y="66" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Doğrulanıyor</text>
  <text x="492" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">testçi doğrular</text>
  <line x1="540" y1="61" x2="552" y2="61" stroke="#5D7789" stroke-width="1.4" marker-end="url(#tya-akis)"/>
  <rect x="554" y="40" width="92" height="42" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="600" y="66" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Kapandı</text>
  <text x="600" y="98" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">testçi kapatır</text>
  <polyline points="492,110 492,124 276,124 276,112" fill="none" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#tya-geri)"/>
  <text x="384" y="140" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Yeniden açıldı: doğrulama düştü — düzeltme eksik ya da rapor baştan belirsiz</text>
  <rect x="14" y="152" width="632" height="148" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="172" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Çözülmeden kapanan kaydın dört biçimi</text>
  <rect x="26" y="184" width="146" height="102" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="99" y="204" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">Won&#39;t Fix</text>
  <text x="99" y="226" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">risk bilinçli kabul edildi</text>
  <text x="99" y="246" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">kararı iş tarafı verir</text>
  <text x="99" y="266" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">gerekçe kayda yazılır</text>
  <rect x="180" y="184" width="146" height="102" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="253" y="204" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Duplicate</text>
  <text x="253" y="226" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">aynı kök nedenin ikinci kaydı</text>
  <text x="253" y="246" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">ana kayda bağlanır</text>
  <text x="253" y="266" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">silinmez, izi kalır</text>
  <rect x="334" y="184" width="146" height="102" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="407" y="204" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">Cannot Reproduce</text>
  <text x="407" y="226" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">adım, ortam ya da veri eksik</text>
  <text x="407" y="246" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">önce bilgi istenir</text>
  <text x="407" y="266" text-anchor="middle" font-size="8.5" fill="#B23B3B" font-family="system-ui">sayı düşürme aracı değildir</text>
  <rect x="488" y="184" width="146" height="102" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="561" y="204" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">Not a Bug</text>
  <text x="561" y="226" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">davranış gereksinime uygun</text>
  <text x="561" y="246" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">kabul ölçütüne dönülür</text>
  <text x="561" y="266" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">belirsizse gereksinim düzelir</text>
</svg>
```

**Elenme sebepleri**

- Durumları ezberden sayıp geçişlerin sahibini hiç söylememek. Cevap ekran görüntüsü gibi duyulur, süreç bilgisi göstermez.
- Geliştiricinin kendi kaydını kapatmasını olağan saymak. Doğrulama halkası kırılır; kapanan hata sayısı gerçeği anlatmaz.
- Cannot Reproduce ile kapatmayı hızlı bir çözüm gibi sunmak. Bu, bilgi eksikliğini kapatmadan kaydı yok saymaktır.
- Duplicate kaydı silmek. Kayıt silinmez; bağlanır, çünkü ikinci kaydın kendi kanıtı ve raporlayanı vardır.
- Won't Fix'i teknik karar sanmak. Riski kabul etmek iş kararıdır ve gerekçesi yazılı kalmalıdır.
- Sürüm alanlarını doldurmamak. Etkilenen ve düzeltildiği sürüm yoksa sürüm notu ve kapanış raporu elle çıkarılır.
- Yeniden açılma oranını bir gösterge saymamak. Bu sayı, düzeltme kalitesini ve rapor kalitesini birlikte ölçer.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Çalıştığın ekipte hata akışında kaç durum vardı? Adları neydi, kim hangi geçişi yapabiliyordu?
- Kapatma yetkisi kimdeydi? Geliştirici kendi kaydını kapatabiliyor muydu?
- Triyaj toplantısı var mıydı? Kimler katılıyordu, öncelik nasıl belirleniyordu?
- Hiç yeniden açılan bir kayıt gördün mü? Sebep düzeltmenin eksikliği miydi, raporun belirsizliği mi?
- Cannot Reproduce ile kapanan bir kayıt sonradan geri döndü mü? Eksik olan bilgi neydi?
- Düzeltildiği sürüm alanı doldurulur muydu? Sürüm notu nasıl çıkarılıyordu?
- Hiç Won't Fix kararı alındı mı? Kararı kim verdi ve gerekçe nereye yazıldı?

Böyle bir deneyimin yoksa uydurma. `256`'daki dürüst köprüyü kur: kavramı doğru anlat, kendi ekibindeki karşılığını söyle, farkı adlandır. Küçük bir ekipte tek bir tabloyla hata takibi yapmış olmak da anlatılabilir bir cevaptır; yeter ki onu kurumsal bir akış gibi sunma.

### "HP ALM / Quality Center ne işe yarar, Jira'dan farkı ne?"

**Soru aslında ne ölçüyor?**

Bu soru bir uyum sorusudur. Kurumsal ve düzenlemeye tabi ortamlarda çalışan ekipler, adayın bu dünyayı görüp görmediğini merak eder. Görüşmecinin aradığı şey ürünün menü yapısı değil; iki aracın **hangi soruyu merkeze aldığı**. Ayrım tek cümlede kurulur ve o cümleyi kurabilen aday azdır. İkinci ayrıştırıcı nokta denetim izidir; bunu anmayan cevap kurumsal kulakta eksik kalır.

**Sözlü cevap iskeleti**

1. **Merkez farkını ilk cümlede söyle.** HP ALM klasik olarak **test yönetimi merkezlidir**; Jira **iş takibi merkezlidir**. Diğer her şey bu farkın sonucudur.
2. **ALM'in modül mantığını ver.** Gereksinimler, test planı yani senaryo kütüphanesi, test laboratuvarı yani koşum kaydı ve hata modülü aynı üründe durur. Bu dördü birbirine bağlıdır ve bağ ürünle birlikte gelir.
3. **Jira tarafını dürüst tarif et.** Jira'nın çekirdeği iş öğesi ve hatadır; iş akışı esnektir, geliştirme akışına yakındır. Test senaryosu ve koşum kaydı ise eklentiyle gelir; Xray ve Zephyr bu türün bilinen örnekleridir (`229`).
4. **Kurumsal tercih sebebini adlandır.** Düzenlemeye tabi ortamda değerli olan şey **denetim izidir**: kim, neyi, ne zaman değiştirdi ve kim onayladı. Denetçiye "bu gereksinim şu senaryoyla test edildi, sonucu şudur" diyebilmek gerekir.
5. **Takas cümlesini kur.** Yapının sıkı olması regülasyonda güven verir, hızlı değişen bir üründe yük olur. Jira'nın esnekliği hız verir ama disiplini ekibe bırakır.
6. **İddia üretme, soru sor.** Sürüm, lisans ve fiyat hakkında konuşma; bunlar değişir ve yanlış söylenirse pahalıya patlar. Bunun yerine sor: hangi modüller kullanılıyor, iş akışı nasıl yapılandırılmış, denetim gereksinimi var mı.
7. **Kapanış cümlesi.** İki araçta da izlenebilirliği ürün değil disiplin üretir. Her senaryoyu bir gereksinime bağlama alışkanlığı yoksa hiçbir araç bunu kendiliğinden yapmaz.

```svg
<svg viewBox="0 0 660 302" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="HP ALM ve Jira karsilastirmasi: test yonetimi merkezli ile is takibi merkezli yaklasim">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fark ürün listesinde değil, merkeze alınan soruda</text>
  <rect x="14" y="36" width="306" height="196" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="167" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">HP ALM / Quality Center</text>
  <text x="167" y="76" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">merkez: test yönetimi</text>
  <text x="167" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gereksinim modülü</text>
  <text x="167" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test planı: senaryo kütüphanesi</text>
  <text x="167" y="144" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test laboratuvarı: koşum kaydı</text>
  <text x="167" y="166" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata modülü aynı üründe</text>
  <text x="167" y="192" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">bağlar ürünle birlikte gelir</text>
  <text x="167" y="212" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">denetim izi ve onay adımı güçlü</text>
  <rect x="340" y="36" width="306" height="196" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="493" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Jira</text>
  <text x="493" y="76" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">merkez: iş takibi</text>
  <text x="493" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş öğesi ve hata çekirdekte</text>
  <text x="493" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş akışı serbestçe yapılandırılır</text>
  <text x="493" y="144" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geliştirme akışına yakın durur</text>
  <text x="493" y="166" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test yönetimi eklentiyle gelir</text>
  <text x="493" y="192" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">esneklik ve hız yüksek</text>
  <text x="493" y="212" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bağları kuran ekibin disiplinidir</text>
  <rect x="14" y="244" width="632" height="48" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="264" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Ortak payda: izlenebilirliği araç değil disiplin üretir</text>
  <text x="330" y="283" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Her senaryo bir gereksinime bağlanmıyorsa iki üründe de kapsama boşluğu görünmez kalır.</text>
</svg>
```

**Elenme sebepleri**

- ALM'i "eski Jira" diye tanımlamak. İki ürün farklı merkezlerde durur; biri diğerinin önceki sürümü değildir.
- İkisini de yalnız hata takip aracı saymak. Bu tanım ALM'in test planı ve laboratuvar tarafını tamamen siler.
- Jira'nın kutudan test yönetimi verdiğini söylemek. Bu yanlış, çünkü senaryo ve koşum kaydı eklentiden gelir.
- Sürüm, lisans ya da fiyat hakkında iddia üretmek. Doğrulanamayan bilgi, bilinmeyen bir alanda güveni tek cümlede bitirir.
- Denetim izinden hiç söz etmemek. Kurumsal ilanda bu, cevabın en çok işe yarayan parçasıdır.
- "Araç fark etmez" demek. Fark eder; hangi sorunun kolay cevaplandığı araca göre değişir.
- Kullanmadığın aracı kullanmış gibi anlatmak. Tek bir modül adı sorulduğunda cevap gelmez ve güven kaybı geri dönmez.

**Kendi hikâyen**

Bu soruda hikâye kısa olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Hangi araçlarda gerçekten kayıt açtın? Yalnız hata mı açtın, senaryo da yazdın mı?
- Test senaryoların nerede duruyordu? Araçta mı, elektronik tabloda mı, wiki sayfasında mı?
- Senaryolar gereksinimlere bağlanıyor muydu? Bağı kim kuruyordu, ne zaman kuruluyordu?
- Koşum kaydı tutuluyor muydu? Bir sürümde hangi senaryonun koştuğunu sonradan bulabiliyor muydun?
- Hiç denetim ya da dış inceleme yaşadın mı? Senden hangi kayıt istendi?
- Onay adımı var mıydı? Test planını kim imzalıyor ya da onaylıyordu?
- Aracın hangi kısmı işini kolaylaştırdı, hangi kısmı yavaşlattı?

Bu araçları hiç kullanmadıysan uydurma. Doğru cümle şudur: kavramı biliyorum, karşılığını şu araçla yaptım, ürünü öğrenmek kısa sürer. `256`'daki köprü tam olarak bu cümleyi kurmayı öğretir. Bilmediğini söyleyip kavramı doğru anlatan aday, bildiğini iddia edip takılan adaydan her zaman önde biter.

### "TFS / Azure DevOps tarafında ne yaparsın?"

**Soru aslında ne ölçüyor?**

Bu soru genellikle şirketin kendi ortamını tarif etmek için sorulur; cevabın onların günlük akışına oturup oturmadığına bakılır. Görüşmeci iki şeyi dinler. Birincisi, ürünün parçalarını ayırabiliyor musun: iş takibi ayrı, test planı ayrı, hat ayrı. İkincisi, test sonucunu iş öğesine bağlama fikrini kuruyor musun. Bu ikinci nokta izlenebilirliğin bu üründeki karşılığıdır.

**Sözlü cevap iskeleti**

1. **Adı doğru kur ve tarih verme.** TFS zamanla Azure DevOps'a evrildi; bulut ve şirket içi kurulum ayrı ayrı anılır. Sürüm ya da yıl söyleme, "evrildi" demek yeterlidir.
2. **Parçaları ayır.** İş öğesi (work item) takibi, kod deposu, hat (pipeline), test planı ve yapıt yönetimi ayrı bileşenlerdir. "Azure DevOps kullandım" cümlesi bu yüzden çok geniştir; hangi bileşen olduğunu söyle.
3. **İş öğesi türlerini say.** Epic, Feature, User Story, Task ve Bug. Hata da bir iş öğesidir; ayrı bir dünyada durmaz, aynı panoda akar.
4. **Test tarafını anlat.** Test planı altında test paketleri (suite), paket altında senaryolar durur. Senaryo bir gereksinim iş öğesine bağlanır; koşum sonucu kayda geçer ve koşum ekranından doğrudan hata açılır.
5. **Bağın neden önemli olduğunu söyle.** Koşumdan açılan hata senaryoya ve gereksinime bağlı doğar. Sonradan elle bağlamak zahmetlidir ve genellikle yapılmaz; bağ o an kurulmazsa hiç kurulmaz.
6. **Hattı ekle.** Otomatik testler hatta koşar, sonuçlar yapı kaydına düşer ve başarısız test hangi değişiklikle geldi sorusu izlenebilir olur. Kalite kapıları da burada tanımlanır (`191`).
7. **Kendi rolünü dürüst söyle.** Gerileme setini paket olarak sabitlemek, koşumu yürütmek ve sonucu işaretlemek manuel testçinin günlük işidir. Yapmadığın parçayı yapmış gibi anlatma.

**Elenme sebepleri**

- TFS ile Azure DevOps'u alakasız iki ürün gibi anlatmak. Aynı çizginin iki adı olduğunu bilmemek dikkat çeker.
- Tarih, sürüm ya da yol haritası uydurmak. Doğrulanamayan tek cümle, doğru anlatılan on cümleyi gölgeler.
- Yalnız "iş takibi yaptık" deyip test planı tarafına hiç girmemek. Test mühendisi ilanında bu eksik büyük görünür.
- Test sonucunu iş öğesine bağlamamak. Bağ kurulmazsa hangi gereksinimin test edildiği sonradan bulunamaz.
- Hattı yalnız derleme aracı sanmak. Test koşumu ve kalite kapısı da orada tanımlanır.
- Kullanmadığın bileşeni kullanmış gibi anlatmak. Görüşmeci tek bir ekran sorar ve cevap gelmez.
- Manuel koşumun bu üründe kaydının tutulabileceğini bilmemek. Manuel test kayıtsız yapılıyorsa raporlama da yoktur.

**Kendi hikâyen**

Bu soruda hikâye günlük işten çıkar. Şu sorular onu bulur:

- Hangi ürünü kullandın: TFS mi, Azure DevOps mu, ikisi de mi? Hangi bileşenine girdin?
- Test planı ve test paketi oluşturdun mu? Gerileme setini nasıl sabitlediniz?
- Koşum sonuçlarını nereye işaretliyordun? Ekran görüntüsü ekliyor muydun?
- Hatayı koşum ekranından mı açıyordun, ayrı bir kayıttan mı? Bağ kuruluyor muydu?
- Otomatik testler hatta koşuyor muydu? Sonuçlarını sen mi inceliyordun?
- Bir yapı başarısız olduğunda kim bakıyordu? Sen hangi noktada devreye giriyordun?
- Gereksinimler nerede duruyordu? İş öğesi olarak mı, ayrı bir belgede mi?

Bu ürünü hiç kullanmadıysan uydurma. Kavram karşılıklarını kur: iş öğesi Jira'daki kayda, test planı senaryo kütüphanesine, hat CI hattına karşılık gelir. Karşılık kurabilen aday, o ürünü kısa sürede öğreneceğini kanıtlamış olur. `256` bu köprünün cümlelerini veriyor.

### "Araç değişikliği yaşadın mı, yeni bir araca nasıl geçilir?"

**Soru aslında ne ölçüyor?**

Bu bir değişim yönetimi sorusudur ve çoğu aday hazırlıksız yakalanır. Görüşmecinin baktığı şey teknik göç adımları değil; **aracı değiştirmenin neyi çözdüğünü** sorgulayıp sorgulamadığın. Sen olmayan bir aracın gelmesiyle düzelmeyecek şeyleri sayabiliyorsan, süreç düşünen bir mühendis olarak duyulursun. İkinci ayrım noktası tarihsel veri kararıdır; bu karar bilinçli mi alınıyor, yoksa sessizce mi geçiştiriliyor.

**Sözlü cevap iskeleti**

1. **Önce sebebi sor.** Neden geçiyoruz sorusunun cevabı "yeni araç daha modern" ise durum kötüdür. Araç kötü süreci düzeltmez; kötü süreç yeni araca olduğu gibi taşınır.
2. **Çözülecek sorunu yazıya dök.** Örnek: kayıtlar gereksinime bağlanmıyor, koşum geçmişi tutulmuyor, rapor elle hazırlanıyor. Bunlar yazılmazsa göçün başarısı ölçülemez.
3. **Eşlemeyi çıkar.** Durumlar, şiddet ve öncelik değer kümeleri, özel alanlar, bileşenler, kullanıcı hesapları ve yetkiler tek tek eşlenir. **Eşlenemeyen alanların listesi kararın kendisidir.**
4. **Tarihsel veri kararını bilinçli ver.** Her kaydı taşımak pahalıdır ve çoğu kapalı kayıt için gereksizdir. Sık verilen karar şudur: açık kayıtlar taşınır, kapalılar salt okunur bir arşivde bırakılır. Denetim gereği varsa arşive erişim belgelenir.
5. **Ekleri unutma.** Ekran görüntüsü, log dosyası ve yorumlar göçte en sık kaybolan veridir. Kayıt gelir, kanıt gelmez; hata yeniden üretilemez hâle gelir.
6. **Paralel dönemi kısa ve tarihli tut.** İki araca birden yazmak sürdürülemez. Kesme anı (cutover) tek cümleyle duyurulur ve eski araç yazmaya kapatılır.
7. **Göçü sayıyla doğrula ve eğitimi atlama.** Kaç kayıt taşındı, kaçı hata verdi, ekler geldi mi. Sonra kısa bir eğitim: yeni araçta iyi kayıt neye benzer. İnsan eski alışkanlıkla yazarsa araç değişir, kalite değişmez.

**Elenme sebepleri**

- "Yeni araç daha iyi" deyip geçmek. Sebebi bir süreç sorununa bağlamayan cevap, kararı moda gibi gösterir.
- Tüm tarihsel veriyi taşımayı varsayılan saymak. Maliyet ve eşleme sorunu görülmemiş olur.
- Paralel dönemi süresiz bırakmak. Belirsiz süre, kalıcı biçimde iki sistem demektir ve kayıtlar bölünür.
- Ekleri ve yorumları hesaba katmamak. Kanıtsız hata kaydı, kapatılmayı bekleyen ölü bir satırdır.
- Göç sonrası sayım yapmamak. "Taşıdık" bir izlenimdir; taşınan kayıt sayısı bir ölçümdür.
- Yetki ve kullanıcı eşlemesini unutmak. Kim hangi geçişi yapabilir sorusu ilk gün sorun çıkarır.
- Eğitimi gereksiz saymak. Aracın alanları boş doldurulursa raporlama eskisinden de kötü olur.

**Kendi hikâyen**

Bu deneyimi yaşadıysan çok değerlidir, çünkü az kişide vardır. Şu sorular onu çıkarır:

- Hiç araç değiştiren bir ekipte bulundun mu? Hangi araçtan hangisine geçildi?
- Karar kimden geldi? Gerekçe neydi, yazılı mıydı?
- Eski kayıtlara ne oldu? Taşındı mı, arşivde mi kaldı, kayboldu mu?
- Ekler ve yorumlar geldi mi? Gelmediyse ne zaman fark ettiniz?
- Paralel dönem oldu mu? Ne kadar sürdü, kim hangi araca yazdı?
- Geçişten sonra hangi alışkanlık geri geldi? Aynı dağınıklık yeni araçta tekrarlandı mı?
- Bir eğitim ya da kısa kılavuz verildi mi? Kayıt kalitesi değişti mi?

Böyle bir göç yaşamadıysan uydurma; bu, uydurmanın en çabuk yakalandığı sorulardan biridir, çünkü ayrıntı sorulur. Bunun yerine akıl yürütmeni anlat: "yaşamadım ama olsa şu sırayla ilerlerdim" demek dürüst ve güçlü bir cevaptır. `256` bu geçişi nasıl kuracağını gösteriyor. Küçük bir örnek de işe yarar: elektronik tablodan bir araca geçmiş olmak da bir göçtür.

### "Test raporlamasını nasıl yaparsın?"

**Soru aslında ne ölçüyor?**

İlanda "iş takibi, raporlama, sözlü ve yazılı iletişim" maddesi var; bu soru o maddenin karşılığıdır ve ciddiye alınmalıdır. Görüşmeci iki şeye bakar. Birincisi izleyici farkındalığı: aynı raporu herkese gönderiyor musun. İkincisi ve asıl ayrıştırıcı olanı, hangi sayıyı anlamlı saydığın. Seçtiğin ölçüt, kalite anlayışını tek cümlede ele verir (`268`).

**Sözlü cevap iskeleti**

1. **İzleyiciyi ayır.** Geliştiriciye hata detayı ve yeniden üretim adımları gider. Takım liderine ilerleme, engel ve risk gider. Yönetime tek sayfalık durum ve karar gereken nokta gider.
2. **Sıklığı ve biçimi söyle.** Günlük kısa durum, sprint sonu özet, sürüm öncesi kalite raporu. Rapor bir ritimdir; düzensiz rapor okunmaz.
3. **Anlamlı ölçütleri say.** Açılan ve kapanan hata trendi yani birikme var mı, şiddete göre dağılım, **kaçan hata (defect leakage)** yani üretimde bulunanın toplama oranı, yeniden açılma oranı, gerileme koşum süresi, kapsanmayan gereksinim sayısı.
4. **Sahte ölçütleri adlandır ve bozdukları davranışı söyle.** Yazılan senaryo sayısı ölçülürse senaryolar bölünerek çoğaltılır. Bulunan hata sayısı ölçülürse önemsiz kayıt açılır ve geliştiriciyle ilişki bozulur. Kapatılan hata sayısı ölçülürse aceleyle kapatmak cazip olur. **Çıktı ölçen metrik davranışı bozar** (`268`).
5. **Sayıyı çıplak bırakma.** "Kırk iki hata açık" bir bilgi değildir. "Kritiklerin sayısı iki haftadır düşmüyor, sebebi tek test ortamının paylaşılması" bir bilgidir. Yorum olmadan sayı karar üretmez.
6. **Raporu bir isteğe bağla.** Her raporun sonunda "senden istediğim şu" cümlesi olsun: ortam, veri, karar ya da süre. Karar istemeyen rapor okunmaz.
7. **Sözlü ile yazılıyı ayır.** Kötü haber önce sözlü verilir, sonra yazılı kayda geçer. Kötü haberi raporun ortasına gömmek en hızlı güven kaybettiren davranıştır (`195`).

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test raporlamasi: izleyiciye gore rapor ve anlamli olcut ile sahte olcut ayrimi">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önce kime yazdığını, sonra hangi sayıyı yazdığını seç</text>
  <rect x="14" y="36" width="200" height="92" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="114" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Geliştiriciye</text>
  <text x="114" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata detayı ve adımlar</text>
  <text x="114" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ortam, sürüm, veri</text>
  <text x="114" y="120" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kanıt: log ve ekran görüntüsü</text>
  <rect x="230" y="36" width="200" height="92" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Takım liderine</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ilerleme ve kalan iş</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">engeller ve riskler</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ortam ve veri sorunları</text>
  <rect x="446" y="36" width="200" height="92" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="546" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Yönetime</text>
  <text x="546" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek sayfalık durum</text>
  <text x="546" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sürüme hazır mıyız</text>
  <text x="546" y="120" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">karar gereken tek nokta</text>
  <rect x="14" y="142" width="316" height="124" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="163" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Anlamlı ölçüt: sonucu ölçer</text>
  <text x="172" y="185" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">açılan ve kapanan hata trendi</text>
  <text x="172" y="205" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">şiddete göre dağılım</text>
  <text x="172" y="225" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kaçan hata (defect leakage)</text>
  <text x="172" y="245" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeniden açılma oranı</text>
  <text x="172" y="261" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">gerileme koşum süresi</text>
  <rect x="344" y="142" width="302" height="124" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="163" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Sahte ölçüt: çıktıyı ölçer</text>
  <text x="495" y="185" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yazılan senaryo sayısı</text>
  <text x="495" y="205" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bulunan hata sayısı</text>
  <text x="495" y="225" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapatılan hata sayısı</text>
  <text x="495" y="245" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">senaryo bölünür, önemsiz kayıt açılır</text>
  <text x="495" y="261" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">acele kapatma ödüllendirilmiş olur</text>
  <rect x="14" y="278" width="632" height="44" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="297" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Çıktı ölçen metrik davranışı bozar</text>
  <text x="330" y="314" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Sayıyı yorumla ve raporu bir isteğe bağla; karar istemeyen rapor okunmaz.</text>
</svg>
```

**Elenme sebepleri**

- Yalnız sayı listelemek. Yorum ve karar önerisi olmayan rapor, panoyu ekran görüntüsüyle göndermekten farksızdır.
- Bulunan hata sayısını başarı ölçütü diye sunmak. Bu cevap, çıktı ölçen metriğin zararını görmediğini gösterir.
- Aynı raporu geliştiriciye, lidere ve yönetime birlikte göndermek. Üçü farklı karar verir, farklı ayrıntı ister.
- Kaçan hatayı hiç anmamak. Testin gerçek etkisini gösteren en dürüst ölçüt budur ve sorulmadan söylenmelidir.
- Kötü haberi yazılı raporun ortasına gömmek. Sürüm günü öğrenilen risk, güveni kalıcı biçimde zedeler.
- Raporlamayı araç işine indirgemek. Gösterge panosunun varlığı, sayının yorumlandığı anlamına gelmez.
- "Sıfır hata" hedefi vaat etmek. Test hatanın yokluğunu gösteremez; bu cümle temel ilkeyle çelişir.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir, yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Kime rapor veriyordun? Aynı raporu birden fazla kişiye mi gönderiyordun?
- Rapor hangi sıklıkta çıkıyordu? Günlük mü, sprint sonu mu, sürüm öncesi mi?
- Hangi sayıları yazıyordun? O sayıları kim istemişti ve ne için kullanıyordu?
- Hiç ölçüt yüzünden davranış değiştiğini gördün mü? Örneğin kayıt açma alışkanlığı değişti mi?
- Üretimde çıkan bir hata oldu mu? Testte neden kaçtığı konuşuldu mu, yoksa suçlu mu arandı?
- Kötü bir haberi nasıl verdin? Önce sözlü mü söyledin, doğrudan yazılı mı gönderdin?
- Raporun sonunda bir istek oluyor muydu? İstek karşılandı mı?

Düzenli rapor hazırlamadıysan uydurma. Kendi işinde tuttuğun küçük bir kayıt bile anlatılabilir: kaç senaryo koştun, kaçı düştü, hangisi ortam yüzünden engellendi. Bu gerçek bir ölçümdür ve dürüstçe öyle anlatılır. `256`'daki köprü bu cevabı zayıflatmadan kurmanı sağlar.

### Bu bölümden çıkan ders

Bu beş sorunun ortak cevabı şudur: araç bilgisi bir liste değil, bir süreç anlayışının kanıtıdır. Jira sorusunda kazandıran şey durum adları değil, geçişin sahibini söyleyebilmektir; kapatma yetkisi doğrulayandadır ve çözülmeden kapanan kaydın dört farklı biçimi vardır. HP ALM sorusunda ayrım tek cümlede kurulur: ALM test yönetimi merkezlidir, Jira iş takibi merkezlidir; düzenlemeye tabi ortamda ALM'i öne çıkaran şey denetim izidir. Azure DevOps sorusunda değerli olan bileşenleri ayırmak ve test sonucunu iş öğesine bağlamaktır. Göç sorusunda kazandıran cümle bu bölümün omurgasıdır: araç tartışması aslında süreç tartışmasıdır, çünkü kötü süreç yeni araca aynen taşınır. Raporlama sorusunda ise ayrım ölçütün türündedir; çıktı ölçen sayı davranışı bozar, sonuç ölçen sayı karar üretir. Beşinde de aynı disiplin geçerlidir: aracı söyle, ne için kullandığını söyle, bedelini söyle ve kullanmadığın şeyi kullanmış gibi anlatma.

# tya-1 | Temel
## Soru
Bir geliştirici hatayı düzeltip durumu "Çözüldü" yaptı, ardından aynı kayıtta durumu "Kapandı"ya çekti. Süreç açısından temel kusur nedir?
## A
Kusur yok; düzeltmeyi yapan kişinin kaydı kapatması akışı hızlandırır
🤖 hız kazanılır ama doğrulama halkası tamamen kaybolur.
## B
Kusur önceliğin güncellenmemesindedir; kapanış öncesi öncelik yeniden hesaplanmalıdır
🤖 öncelik triyaj alanıdır; buradaki eksik doğrulamadır.
## C
Kusur, kaydın Duplicate olarak işaretlenmemiş olmasıdır
🤖 duplicate ayrı bir durumdur; burada tekrar eden kayıt yok.
## D
Kapatma yetkisi doğrulayan tarafta olmalıdır; geliştirici kendi işini doğrulamış olur
🤖 Doğru: "Çözüldü" geliştiriciden, "Kapandı" testçiden gelir.
## Doğru
D
## Açıklama
Hata yaşam döngüsünde durum adları ekipten ekibe değişir, ama **geçişlerin sahipliği** değişmez. Kaydı testçi açar, önceliği triyaj verir, düzeltmeyi geliştirici yazar ve "Çözüldü" olarak işaretler. Kapanış ise doğrulamadan sonra gelir ve doğrulamayı yapan taraf testçidir. Geliştirici kendi kaydını kapatırsa halka kırılır; kapanan hata sayısı artar ama düzeltmenin gerçekten çalıştığını kimse görmemiş olur. Bu, sonradan üretimde çıkan hataların en sık kaynaklarından biridir. B şıkkı önceliğe takılır; oysa öncelik bir triyaj alanıdır ve kapanış kararının sahibi değildir (`229`). C şıkkı olmayan bir durumu getirir; Duplicate yalnız aynı kök nedenin ikinci kaydı için kullanılır. A şıkkı hızı gerekçe sayar; süreçte hız, doğrulamayı atlayarak kazanılmaz. Doğru akış şudur: geliştirici çözer, testçi doğrular, doğrulama düşerse kayıt "Yeniden açıldı" durumuna geçer.

# tya-2 | Temel
## Soru
Testçinin açtığı bir hatayı geliştirici yeniden üretemedi. Kaydı "Cannot Reproduce" ile kapatmadan önce en doğru davranış hangisidir?
## A
Kaydı silip testçiden yeni bir kayıt açmasını istemek
🤖 kayıt silinmez; geçmiş ve sayım bozulur.
## B
Ortam, sürüm, veri ve adımların netleştirilmesini isteyip yeniden üretilebilirliği kurmak
🤖 Doğru: kapatma son adımdır, ilk adım değil.
## C
Önceliği en düşüğe çekip kaydı açık bırakmak ve sürüm sonunu beklemek
🤖 kayıt açık kalır ama eksik bilgi yine tamamlanmaz.
## D
Kaydı "Won't Fix" olarak kapatmak; yeniden üretilemeyen hata düzeltilemez
🤖 Won't Fix bilinçli risk kabulüdür, bilgi eksikliği değil.
## Doğru
B
## Açıklama
"Cannot Reproduce" bir sonuç değil, bir **bilgi eksikliği** işaretidir. Doğru davranış kapatmadan önce eksiği kapatmaya çalışmaktır: hangi ortam, hangi sürüm, hangi kullanıcı, hangi veri, hangi adım sırası. Çoğu zaman fark ortamdadır; test verisi ya da yapılandırma farklıdır. Bu bilgi netleştiğinde kayıt ya yeniden üretilir ya da gerçekten kapatılabilir hâle gelir. A şıkkı kaydı silmeyi önerir; kayıt silinmez, çünkü raporlayan, tarih ve kanıt izlenebilir kalmalıdır. C şıkkı sorunu erteler: öncelik düşürmek eksik bilgiyi tamamlamaz, kayıt yalnız görünmez olur. D şıkkı iki kavramı karıştırır; **Won't Fix** bilinçli bir risk kabulüdür ve kararı iş tarafı verir, oysa burada karar verilecek bir risk bile tanımlı değildir (`229`). Bu ayrımı bilmek, "Cannot Reproduce" ile kapatmayı hata sayısını düşüren bir alışkanlığa dönüştürmeyi engeller.

# tya-3 | Orta
## Soru
Düzenlemeye tabi bir kurumda HP ALM'in neden tercih edildiği soruluyor. En yerinde açıklama hangisidir?
## A
ALM daha yeni bir üründür ve Jira'nın yerini almak üzere tasarlanmıştır
🤖 iki ürün farklı merkezlerde durur; biri diğerinin yenisi değildir.
## B
ALM otomasyon testlerini kendiliğinden yazar, bu yüzden kurumsal ekipler onu seçer
🤖 hiçbir yönetim aracı senaryoyu kendiliğinden üretmez.
## C
Gereksinim, test planı, koşum ve hata tek üründe bağlı durur; denetim izi kayıtta kalır
🤖 Doğru: denetlenebilirlik asıl tercih sebebidir.
## D
Jira'da hata kaydı tutulamaz, bu yüzden düzenlemeye tabi kurumlar ALM'e yönelir
🤖 hata kaydı Jira'nın çekirdeğidir; eklentiyle gelen test yönetimidir.
## Doğru
C
## Açıklama
İki aracın farkı özellik listesinde değil, **merkeze aldıkları soruda**. HP ALM klasik olarak test yönetimi merkezlidir: gereksinim modülü, test planı yani senaryo kütüphanesi, test laboratuvarı yani koşum kaydı ve hata modülü aynı üründe durur, aralarındaki bağ ürünle birlikte gelir. Jira ise iş takibi merkezlidir; hata ve iş öğesi çekirdektedir, test senaryosu ile koşum kaydı eklentiden gelir (`229`). Düzenlemeye tabi ortamda belirleyici olan şey **denetim izidir**: kim neyi ne zaman değiştirdi, kim onayladı, hangi gereksinim hangi senaryoyla test edildi. Denetçiye bu zinciri gösterebilmek ALM'i öne çıkarır. A şıkkı iki ürünü aynı çizgiye koyar. B şıkkı bir yönetim aracına otomasyon üretme yeteneği atfeder. D şıkkı yanlış yönde bir eksik varsayar. Mülakatta sürüm, lisans ya da fiyat hakkında iddia üretme; bunun yerine hangi modüllerin kullanıldığını sor.

# tya-4 | Orta
## Soru
Azure DevOps'ta manuel bir gerileme koşumu yürütüyorsun ve bir adım başarısız oldu. İzlenebilirlik açısından en doğru davranış hangisidir?
## A
Hatayı koşum ekranından açmak; kayıt senaryoya ve ilgili iş öğesine bağlı doğsun
🤖 Doğru: bağ ancak o an kurulursa kurulur.
## B
Hatayı ayrı bir araçta açmak; test aracıyla hata aracını karıştırmamak daha temizdir
🤖 bağ kopar; hangi senaryonun düştüğü sonradan bulunamaz.
## C
Koşumu başarısız işaretleyip hata açmadan sprint sonunu beklemek
🤖 gecikme kanıtı ve ortam bilgisini eskitir.
## D
Tüm test paketini yeniden koşup sonucu tek satırda özetlemek
🤖 tekrar koşum bilgiyi artırmaz; kayıt yine oluşmaz.
## Doğru
A
## Açıklama
Bu üründe test planı altında test paketleri, paket altında senaryolar durur ve senaryolar gereksinim iş öğelerine bağlanır. Koşum sırasında bir adım düştüğünde hatayı **koşum ekranından** açarsan kayıt senaryoya ve gereksinime bağlı olarak doğar. Ortam, koşum kimliği ve adım bilgisi de o anda kayda geçer. Sonradan elle bağ kurmak zahmetlidir ve pratikte genellikle yapılmaz; yani bağ o an kurulmazsa hiç kurulmaz. B şıkkı kaydı başka bir araca taşır ve izlenebilirlik zincirini keser; sürüm sonunda "bu gereksinim test edildi mi" sorusu cevapsız kalır. C şıkkı hatayı bekletir; ortam değişir, veri tazelenir ve yeniden üretilebilirlik kaybolur. D şıkkı koşumu tekrarlar ama kayıt üretmez. Otomatik testlerde aynı bağ hat üzerinden kurulur: sonuçlar yapı kaydına düşer ve başarısız test hangi değişiklikle geldi sorusu izlenebilir olur (`191`).

# tya-5 | İleri
## Soru
Bir ekip hata takibini HP ALM'den Jira'ya taşımaya karar verdi. Göç planında en kritik ilk adım hangisidir?
## A
Tüm tarihsel kayıtları eksiksiz taşımak; veri kaybı olmadan başlamak tek doğru yaklaşımdır
🤖 eksiksiz taşıma pahalıdır ve kapalı kayıtların çoğu için gereksizdir.
## B
Yeni araçta varsayılan iş akışını olduğu gibi kullanmak; yapılandırmayı sonraya bırakmak
🤖 eşlenmemiş durumlar ilk haftada kayıt kalitesini bozar.
## C
Neden geçildiğini süreçle tanımlamak, sonra durum, alan ve yetki eşlemesini çıkarmak
🤖 Doğru: eşleme çıkmadan taşınacak şey belirsizdir.
## D
İki aracı süresiz paralel çalıştırmak; ekip alışana kadar ikisine birden yazmak
🤖 çift yazma sürdürülemez; kayıtlar iki yerde bölünür.
## Doğru
C
## Açıklama
Araç göçünde ilk adım teknik değildir. Önce şu soru cevaplanır: **hangi süreç sorununu çözüyoruz.** Cevap "yeni araç daha modern" ise göç, mevcut dağınıklığı yeni bir arayüze taşımaktan ibaret kalır; araç kötü süreci düzeltmez. Sebep yazıya döküldükten sonra eşleme çıkarılır: durumlar, şiddet ve öncelik değer kümeleri, özel alanlar, bileşenler, kullanıcı hesapları ve yetkiler. Eşlenemeyen alanların listesi kararın kendisidir. A şıkkı tarihsel veriyi varsayılan olarak taşır; sık verilen bilinçli karar ise açık kayıtları taşımak, kapalıları salt okunur arşivde bırakmaktır. Ayrıca ekler ve yorumlar göçte en çok kaybolan veridir; kanıtı gelmeyen kayıt yeniden üretilemez. B şıkkı yapılandırmayı erteler ve ilk günden kayıt kalitesini düşürür. D şıkkı paralel dönemi süresiz bırakır; bu dönem kısa ve tarihli olmalı, kesme anından sonra eski araç yazmaya kapatılmalıdır. Göç sonunda taşınan kayıt sayısı sayımla doğrulanır.

# tya-6 | İleri
## Soru
Bir yönetici test ekibinin başarısını "ay içinde bulunan hata sayısı" ile ölçmek istiyor. En yerinde itiraz hangisidir?
## A
Ölçüt doğrudur ama bir eşik konmalı; ayda belirli bir sayının altına düşülmemelidir
🤖 eşik çıktı baskısını artırır; sorunu büyütür.
## B
Ölçüt doğrudur; bulunan hata sayısı test kapsamının en doğrudan göstergesidir
🤖 sayı kapsamı göstermez; kolay alanlarda şişer.
## C
Ölçüt yanlıştır çünkü hata sayısını yalnız geliştiriciler etkiler, testçiler etkilemez
🤖 gerekçe yanlış; sorun sorumlulukta değil ölçütün türünde.
## D
Çıktı ölçen bir sayıdır; önemsiz kayıt açmayı ödüllendirir, kaçan hata daha anlamlı olur
🤖 Doğru: çıktı metriğinden sonuç metriğine geçilir.
## Doğru
D
## Açıklama
"Bulunan hata sayısı" bir **çıktı** ölçütüdür: harcanan eforu sayar, elde edilen kaliteyi değil. Ölçülmeye başlandığı anda davranışı bozar; önemsiz kayıtlar açılır, tek bir sorun birkaç kayda bölünür ve testçiyle geliştirici arasındaki ilişki karşılıklı savunmaya döner. Aynı bozulma "yazılan senaryo sayısı" ve "kapatılan hata sayısı" için de geçerlidir. Doğru yön **sonuç** ölçütleridir: kaçan hata (defect leakage) yani üretimde bulunanın toplama oranı, yeniden açılma oranı, şiddete göre dağılım, açılan ile kapanan trendi ve gerileme koşum süresi. Bunlar testin gerçek etkisini gösterir ve kolayca şişirilemez (`268`). A şıkkı ölçütü koruyup eşik ekler; bu, çıktı baskısını artırarak sorunu büyütür. B şıkkı hata sayısını kapsam göstergesi sanır; oysa sayı, kolay erişilen alanlarda şişer ve dokunulmayan alanı hiç göstermez. C şıkkının sonucu doğru ama gerekçesi yanlıştır. Rapor yazarken sayıyı yorumla ve bir isteğe bağla; karar istemeyen rapor okunmaz (`195`).
