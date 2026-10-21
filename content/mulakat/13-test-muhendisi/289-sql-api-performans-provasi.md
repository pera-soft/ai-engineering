---
slug: sql-api-performans-provasi
baslik: "Sözlü Prova · SQL, API ve Performans Testi: Doğrulama, Sözleşme, Yüzdelik ve Kararsız Test"
kademe: İleri
sira: 289
etiketler: 11
---

## Konu anlatımı

İlanın üç teknik maddesi bu dosyada toplanıyor: orta-iyi SQL, API testleri deneyimi, performans testleri deneyimi. Bu üçünün teorisi modülde zaten var. API testinin araçları ve sözleşme doğrulaması `232`'de, yük ve performans testi türleriyle yüzdelikler `233`'te, SQL temelleri `29`'da işlendi. Burada tekrar edilmeyecek; buranın işi başka. Bu dosya o bilgiyi mülakat masasında **nasıl anlatacağını** verir.

Fark şurada. Bir konuyu bilmek ile o konuyu doksan saniyede, sırası bozulmadan, bedelini de söyleyerek anlatmak iki ayrı beceridir. Görüşmeci senin bildiğini değil, bildiğini kullanıp kullanmadığını görmek ister. Bu yüzden her sorunun altında dört parça var: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı. Senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur.

Beş soru tek bir eksende ilerler: söylediğini neyle doğruluyorsun. SQL sorusunda doğrulama veritabanından gelir, API sorusunda sözleşmeden, performans sorusunda yüzdelikten, kararsız test sorusunda kök nedenden. Dördünde de aynı zayıflık eler: kanıt yerine izlenim sunmak.

### "Test süreçlerinde SQL'i ne için kullanırsın?"

**Soru aslında ne ölçüyor?**

İlan "orta-iyi seviyede SQL" diyor. Görüşmeci burada sorgu yazma hızını ölçmüyor. Merak ettiği şey daha dar: bu aday veritabanını bir doğrulama aracı olarak görüyor mu. Zayıf aday SQL'i geliştiricinin işi sayar. Güçlü aday veritabanını testin ikinci gözü olarak kullanır. Arayüz "işlem başarılı" yazabilir; kayıt yine de yanlış alana, yanlış biçimde ya da eksik yazılmış olabilir. Bu farkı gören testçi, göremeyen testçiden çok daha fazla hata bulur.

**Sözlü cevap iskeleti**

1. **Üç kullanımı baştan adlandır.** Test verisi hazırlama, sonucu doğrulama, koşum sonrası temizlik. Bu üçlü cevabın iskeletidir; önce söyle, sonra aç.
2. **Doğrulamayı öne çıkar.** En değerli kullanım budur: sonucu arayüzden değil **veritabanından** doğrulamak. Arayüz katmanı hatayı gizleyebilir; tablo gizlemez.
3. **Neyi yazabildiğini somutla.** `JOIN` türleri, `GROUP BY` ile `HAVING`, alt sorgu, `COUNT` ve `SUM`, tarih aralığı filtreleme. İsim saymak değil, ne zaman hangisini kullandığını söyle (`29`, `146`).
4. **Temizliği yalıtımla bağla.** Her koşum aynı başlangıç durumunu bulmalıdır. Artık veri bırakan test, bir sonraki koşumu kararsız hâle getirir (`203`).
5. **Güvenlik kuralını ver.** Test ortamında bile `UPDATE` ya da `DELETE` çalıştırmadan önce aynı `WHERE` ile `SELECT` koş. Kaç satır döndüğünü gör, sonra yaz.
6. **İşlem sınırını bil.** Otomatik kesinleştirme açıkken yanlış komut geri alınamaz. İşlem açıp sonucu görmek ve gerekirse geri almak temel bir alışkanlıktır (`224`).
7. **Sınırı söyle.** SQL doğrulama aracıdır, uygulama mantığının yerine geçmez. Kural veritabanında yeniden yazılırsa iki ayrı doğru üretilir.

```svg
<svg viewBox="0 0 660 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Testte SQL: veri hazirlama, veritabanindan dogrulama, temizlik ve yikici komut kurali">
  <defs>
    <marker id="sap-sql-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Testte SQL'in üç işi — asıl değer ortadaki adımdadır</text>
  <rect x="14" y="36" width="196" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Veri hazırlama</text>
  <text x="112" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">senaryonun ihtiyacı olan kayıt</text>
  <text x="112" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınır durumları kurmak</text>
  <text x="112" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arayüzden kurulamayan hâller</text>
  <text x="112" y="148" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arayüzle veri kurmak yavaştır</text>
  <text x="112" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ve testi başka teste bağlar</text>
  <line x1="212" y1="110" x2="228" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sap-sql-ok)"/>
  <rect x="232" y="36" width="196" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · Doğrulama</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sonucu arayüzden değil</text>
  <text x="330" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veritabanından oku</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alan, biçim, ilişki, sayı</text>
  <text x="330" y="148" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">"başarılı" yazısı kaydın</text>
  <text x="330" y="166" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">doğru yazıldığını kanıtlamaz</text>
  <line x1="430" y1="110" x2="446" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sap-sql-ok)"/>
  <rect x="450" y="36" width="196" height="150" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">3 · Temizlik</text>
  <text x="548" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koşum sonrası artık veri</text>
  <text x="548" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her koşum aynı başlangıç</text>
  <text x="548" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test yalıtımının şartı</text>
  <text x="548" y="148" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">artık veri bırakan test</text>
  <text x="548" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonrakini kararsız yapar</text>
  <rect x="14" y="198" width="632" height="100" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="330" y="220" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Yıkıcı komut kuralı — test ortamında bile geçerlidir</text>
  <text x="330" y="243" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Önce aynı WHERE ile SELECT koş; kaç satır döndüğünü gör. Sayı beklediğinden büyükse komutu yazma.</text>
  <text x="330" y="262" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Sonra işlem aç, UPDATE ya da DELETE çalıştır, etkilenen satırı doğrula, ancak o zaman kesinleştir.</text>
  <text x="330" y="285" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">Otomatik kesinleştirme açıkken yanlış WHERE geri alınamaz — mülakatta bunu söylemek puan getirir.</text>
</svg>
```

**Elenme sebepleri**

- "SQL'i geliştirici yazar, ben arayüzden bakarım." İlan SQL istiyor; bu cümle maddeyi doğrudan boş bırakır.
- Yalnız `SELECT *` düzeyinde konuşmak. Birleştirme ve gruplama gelmiyorsa "orta-iyi" iddiası karşılıksız kalır.
- İç birleştirme ile dış birleştirmeyi karıştırmak. Eksik kayıtları arayan testçi için bu ayrım günlük bir araçtır.
- Veritabanından doğrulamayı hiç anmamak. Cevap yalnız veri hazırlamada kalırsa en değerli kullanım kaçırılmış olur.
- Temizlikten söz etmemek. Artık veri bırakan koşum düzeni, kararsız test şikâyetinin sessiz kaynağıdır.
- `UPDATE` ve `DELETE` komutlarını doğrudan koşmak. Önce `SELECT` demeyen aday, ortamı bozma riskini taşıyor sayılır.
- Hassas veriden hiç bahsetmemek. Gerçek kişi verisi kopyalanıyorsa maskeleme sorulmalıdır; bunu sormak olgunluk işaretidir.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Hangi projede veritabanına doğrudan bakma ihtiyacı doğdu? Sebep neydi: eksik kayıt mı, yanlış alan mı, uyuşmayan rapor mu?
- Arayüz "başarılı" derken tablo başka bir şey söyledi mi? O hata nasıl ortaya çıktı?
- Test verisini nasıl hazırlıyordun? Elle mi, betikle mi, yoksa var olan kayıtları kopyalayarak mı?
- Yazdığın en karmaşık sorgu neydi? Kaç tabloyu birleştiriyordu, ne arıyordun?
- Hiç yanlış `WHERE` ile veri bozdun mu ya da bozulduğunu gördün mü? Sonrasında hangi alışkanlık değişti?
- Temizlik nasıl yapılıyordu? Her koşumda mı, gün sonunda mı, hiç mi?
- Hassas veri var mıydı? Maskeleme yapılıyor muydu, kim karar veriyordu?

Bu deneyim yoksa uydurma. `256`'daki dürüst köprüyü kur: bildiğin sorgu düzeyini net söyle, hangi kullanımı yaptığını somutla, eksiği adlandır ve kapatma planını ver. Kendi kurduğun bir veritabanında test verisi hazırlayıp sonucu sorguyla doğrulamak da gerçek bir çalışmadır ve öyle anlatılır.

### "API testinde neye bakarsın?"

**Soru aslında ne ölçüyor?**

Bu soru bir araç sorusu gibi duyulur ama değildir. Görüşmeci adayın Postman ekranını tarif etmesini beklemiyor. Baktığı şey şu: bu aday API testinin **ne kazandırdığını** biliyor mu. Zayıf cevap istek atmayı ve iki yüz kodunu görmeyi anlatır. Güçlü cevap asıl kazançla açar: arayüzden üretilemeyen durumlar API'den doğrudan denenebilir. Arayüz seni kendi kurallarıyla sınırlar; alan doğrulaması geçersiz veriyi zaten göndertmez. API katmanında o sınır yoktur, dolayısıyla sunucunun kendini nasıl savunduğu görülür.

**Sözlü cevap iskeleti**

1. **Kazançla aç.** API testinin asıl değeri hız değil erişimdir: arayüzün üretemediği geçersiz gövde, eksik alan, yetkisiz istek ve sınır değerler buradan denenir.
2. **Cevabın anatomisini say.** Durum kodu, gövde şeması, zorunlu alanlar, veri tipleri, tarih ve sayı biçimleri. Şema doğrulaması geçmeden "geçti" denmez.
3. **Mutlu yolun dışını anlat.** Geçersiz gövde, eksik zorunlu alan, olmayan kayıt, yetkisiz erişim. Kimlik doğrulanmamış istek ile yetkisi olmayan istek farklı karşılık üretmelidir (`98`).
4. **Tekrar güvenliğini (idempotency) ekle.** Aynı isteği iki kez gönder. İkinci istek ikinci kaydı oluşturuyorsa ortada gerçek bir hata vardır ve bu hata arayüzden zor görülür.
5. **Koleksiyon ve ortam değişkenini kullan.** Aynı testler adres ve kimlik bilgisi değiştirilerek farklı ortamlarda koşulur. Anahtar değerler koleksiyonun içine gömülmez (`232`).
6. **Sözleşme testini getir.** Sağlayıcı bir alanın adını ya da tipini değiştirdiğinde tüketici kırılır. Sözleşme testi bunu dağıtımdan önce yakalar (`232`).
7. **Sınırı söyle.** API testi arayüz testinin yerine geçmez. İkisi farklı riski kapatır; bu cümleyi kurmak seni ölçülü gösterir.

**Elenme sebepleri**

- "İstek atarım, iki yüz dönüyorsa geçer." Bu cevap tek başına yeterli değildir; gövde yanlışken de iki yüz dönebilir.
- Şema doğrulamasını atlamak. Alan adı değişmiş bir cevap durum kodunu bozmaz ama tüketiciyi kırar.
- Yalnız mutlu yolu test etmek. Hata durumları test edilmemişse sistemin savunması hiç denenmemiş demektir.
- Kimlik doğrulama ile yetkilendirme ayrımını bilmemek. Bu ayrım API testinin en sık sorulan ayrıntısıdır.
- Belirteci elle kopyalayıp koleksiyona gömmek. Hem sırrı sızdırır hem testi bir günde eskitir.
- API testini arayüz testinin yerine koymak. Kapsama tamamlandı iddiası, uçtan uca riski görmediğini gösterir.
- Üretim ortamına yazan istek koşmak. Test kapsamı sorulmadan üretime dokunmak tek başına eleyicidir.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir. Şu sorular onu çıkarır:

- Hangi projede API testi yaptın? Servisler kendi aralarında mı konuşuyordu, dışarıya mı açıktı?
- Hangi aracı kullandın ve neden? Koleksiyonlar paylaşılıyor muydu, sürüm kontrolünde miydi?
- Arayüzden üretemediğin bir durumu API'den ürettiğin oldu mu? Hangi hatayı böyle buldun?
- Şema doğrulaması yapıyor muydun, yoksa yalnız durum koduna mı bakıyordun?
- Aynı isteği iki kez gönderdiğinde ne oldu? Çift kayıt üreten bir uç noktaya rastladın mı?
- Sağlayıcı tarafında bir alan adı değişti mi? Kırılma ne zaman fark edildi: testte mi, üretimde mi?
- API testleri hatta (pipeline) koşuyor muydu, yoksa elle mi tetikleniyordu?

Deneyimin yoksa uydurma. `256`'daki köprü burada da işler. Açık bir servise koleksiyon yazıp hata durumlarını denemek gerçek bir çalışmadır. Yeter ki bunu kurumsal bir projede yapmış gibi anlatma; nerede yaptığını dürüstçe söyle.

### "Performans testi türlerini birbirinden ayırır mısın?"

**Soru aslında ne ölçüyor?**

Bu soru terim ezberi gibi görünür, oysa amaç ayrımını ölçer. Her performans testi türü **farklı bir soru sorar**; türü karıştıran aday yanlış soruyu sorup doğru cevabı beklemiş olur. Görüşmecinin ikinci baktığı yer daha da ayrıştırıcıdır: aday sonucu ortalamayla mı anlatıyor, yüzdelikle mi. Ortalamayla konuşan aday dağılımın kuyruğunu hiç görmemiştir. Şikâyet eden kullanıcı tam olarak orada durur.

**Sözlü cevap iskeleti**

1. **Ayrım ölçütünü baştan koy.** Türler araçla değil **sorulan soruyla** ayrılır. Bu cümle cevabın tamamını düzenler.
2. **Yük testi (load).** Soru şudur: beklenen yük altında hedefler tutuyor mu. Bu bir doğrulama testidir, keşif testi değildir.
3. **Stres testi.** Soru şudur: nerede kırılıyor ve kırılınca nasıl davranıyor. Kırılma anındaki davranış en az kırılma noktası kadar önemlidir.
4. **Dayanıklılık testi (soak).** Soru şudur: uzun süre koşarsa bir şey birikiyor mu. Bellek sızıntısı ve bağlantı birikmesi ancak burada görünür.
5. **Ani yükleniş testi (spike).** Soru şudur: yük sıfırdan tepeye sıçrarsa ne olur. Kademeli artışta ayakta kalan sistem burada düşebilir.
6. **Ölçeklenebilirlik testi.** Soru şudur: kaynak eklendiğinde kapasite orantılı artıyor mu. Artmıyorsa darboğaz paylaşılan bir yerdedir.
7. **Ortalamayı reddet.** Ortalama dağılımı saklar, yüzdelik (percentile) gösterir. Hedefler p95 ve p99 ile yazılır (`233`).
8. **Ölçmeden darboğaz adı verme.** "Muhtemelen veritabanıdır" demek tahmindir. Nerede beklendiği izleme verisiyle söylenir.

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Performans testi turleri: yuk, stres, dayaniklilik, ani yuklenis ve ortalama ile yuzdelik farki">
  <defs>
    <marker id="sap-perf-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Türler araçla değil, sordukları soruyla ayrılır</text>
  <rect x="14" y="36" width="146" height="128" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="87" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Yük (load)</text>
  <text x="87" y="79" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">beklenen yük</text>
  <text x="87" y="101" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hedefler tutuyor mu?</text>
  <text x="87" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">doğrulama testidir</text>
  <text x="87" y="147" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonuç: geçti / kaldı</text>
  <rect x="176" y="36" width="146" height="128" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="249" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Stres</text>
  <text x="249" y="79" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yük artırılır</text>
  <text x="249" y="101" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">nerede kırılıyor?</text>
  <text x="249" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kırılınca ne yapıyor?</text>
  <text x="249" y="147" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonuç: kırılma noktası</text>
  <rect x="338" y="36" width="146" height="128" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="411" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Dayanıklılık</text>
  <text x="411" y="79" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">uzun süre, sabit yük</text>
  <text x="411" y="101" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bir şey birikiyor mu?</text>
  <text x="411" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sızıntı, açık bağlantı</text>
  <text x="411" y="147" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonuç: zamanla eğilim</text>
  <rect x="500" y="36" width="146" height="128" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="573" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Ani yükleniş</text>
  <text x="573" y="79" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sıfırdan tepeye</text>
  <text x="573" y="101" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sıçramaya dayanır mı?</text>
  <text x="573" y="121" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">toparlanma süresi ne?</text>
  <text x="573" y="147" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonuç: tepki ve iyileşme</text>
  <rect x="14" y="180" width="316" height="126" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="172" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Ortalama saklar</text>
  <text x="172" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek sayı, dağılımı yok eder</text>
  <text x="172" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yavaş azınlık içinde erir</text>
  <text x="172" y="266" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">rapor iyi, şikâyet devam eder</text>
  <text x="172" y="292" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">"ortalama iyi" bir hedef ifadesi değildir</text>
  <line x1="332" y1="243" x2="342" y2="243" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sap-perf-ok)"/>
  <rect x="346" y="180" width="300" height="126" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="496" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Yüzdelik (percentile) gösterir</text>
  <text x="496" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">p95: her yirmi istekten biri bundan yavaş</text>
  <text x="496" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">p99: kuyruğun ucu, en çok şikâyet eden</text>
  <text x="496" y="266" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hedef yüzdelikle yazılır, ölçülebilir olur</text>
  <text x="496" y="292" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">yanında iş hacmi ve hata oranı da raporlanır</text>
</svg>
```

**Elenme sebepleri**

- Yük ile stresi aynı şey saymak. Biri hedefi doğrular, diğeri sınırı arar; bu karışıklık ilk cümlede duyulur.
- Dayanıklılık testini hiç anmamak. Sızıntı ve birikme yalnız uzun koşumda görünür; kısa testler bunu kaçırır.
- Yalnız ortalamadan konuşmak. Yüzdeliği anmayan aday kuyruk davranışını hiç incelememiş sayılır.
- Kırılma sonrası davranışı atlamak. Sistemin tamamen düşmesi ile yavaşlayarak ayakta kalması çok farklı sonuçlardır.
- Ölçmeden darboğaz adı vermek. "Kesin veritabanıdır" cümlesi teşhis değil, alışkanlıktır.
- Hata oranını raporlamamak. Yük altında hızlı ama hatalı cevap veren sistem başarılı sayılmaz.
- Test ortamının farkını görmezden gelmek. Sonuç mutlak eşik gibi sunulursa cevabın tamamı güven kaybeder.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir; yeter ki ölçüm içersin. Şu sorular onu çıkarır:

- Hangi sistemde yük konuşuldu? Kampanya, gün sonu, ay sonu ya da mevsimsel bir yoğunluk var mıydı?
- Hangi türde test koştun? Beklenen yükü mü doğruladın, yoksa sınırı mı aradın?
- Hedef nasıl tanımlanmıştı? Bir sayı verilmiş miydi, yoksa "yavaş olmasın" mı denmişti?
- Sonucu neyle raporladın? Ortalama mı, yüzdelik mi, ikisi birden mi?
- Kırılma noktasını gördün mü? Sistem düştü mü, yavaşladı mı, hata mı döndürdü?
- Darboğaz nerede çıktı? Veritabanı, dış servis, bağlantı havuzu, uygulama sunucusu?
- Testten sonra ne değişti? Bir değişiklik yapıldıysa aynı test tekrar koşuldu mu?

Bu deneyim yoksa uydurma. `256`'daki köprüyü kur. Kendi projende bir uç noktaya yük verip yüzdelik değerleri okumak gerçek bir gözlemdir; onu olduğu gibi anlat.

### "Bir performans testini nasıl kurgularsın?"

**Soru aslında ne ölçüyor?**

Önceki soru bilgi ölçüyordu, bu soru yöntem ölçüyor. Görüşmeci burada bir plan duymak ister ve planın **hangi adımla başladığına** bakar. Zayıf aday araçla başlar; hangi aracı kullanacağını anlatır ve senaryoya geçer. Güçlü aday hedefle başlar, çünkü hedef yoksa sonucun geçti mi kaldı mı olduğu bilinemez. İkinci ayrım ortamdadır. Test ortamı üretime benzemiyorsa çıkan sayı bir eşik değil, olsa olsa bir karşılaştırma noktasıdır. Bunu kendiliğinden söylemek adayı ölçülü ve dürüst gösterir.

**Sözlü cevap iskeleti**

1. **Hedefi sayıyla tanımla.** Kaç eşzamanlı kullanıcı, hangi işlem, hangi yanıt süresi eşiği, hangi hata oranı sınırı. "Yavaş olmasın" bir hedef değildir.
2. **Senaryoyu gerçek kullanımdan çıkar.** Hangi uç noktalar hangi oranda çağrılıyor. Yalnız en kolay uç noktaya yük vermek gerçekliği taklit etmez.
3. **Ortamı ve veriyi konuş.** Üretim benzeri ortam ve **gerçekçi veri hacmi** şarttır. Boş tabloda her sorgu hızlıdır; indeks eksikliği ancak hacimle görünür (`146`).
4. **Isınmayı ve kademeli artışı ekle.** İlk saniyelerde önbellekler soğuk, bağlantı havuzu kurulmamıştır. Ölçüm ısınma sonrası başlar.
5. **İzlemeyi üç yerde birden aç.** Uygulama, veritabanı ve altyapı aynı zaman ekseninde izlenir. Yalnız yük aracının çıktısına bakmak nerede beklendiğini söylemez (`193`).
6. **Temel ölçümle (baseline) karşılaştır.** Önce mevcut durumu ölç, sonra tek bir değişkeni değiştir, sonra aynı testi tekrar koş.
7. **Şerhi kendin koy.** Ortam üretimden küçükse sonuç mutlak eşik olarak kullanılmaz; eğilim ve karşılaştırma için kullanılır. Bu cümleyi söylemek zayıflık değil, olgunluktur.

**Elenme sebepleri**

- Araçla başlamak. Hangi aracı kullandığını söylemek hedefi tanımlamanın yerine geçmez.
- Hedefi sayıya bağlamamak. Eşik yoksa sonuç yorumlanamaz; rapor izlenime dönüşür.
- Boş ya da küçük veriyle test etmek ve sonucu üretime taşımak. En sık yapılan ve en pahalı hata budur.
- Isınmayı hesaba katmamak. İlk ölçümler bozuk çıkar, ortalama yanıltıcı biçimde kötüleşir.
- Yalnız yük aracının raporuna bakmak. Sunucu tarafı izlenmezse darboğazın yeri bilinemez.
- Aynı anda birkaç değişiklik yapmak. Sonuç iyileşse bile hangisinin işe yaradığı öğrenilmez.
- Ortam farkını hiç dile getirmemek. Şerh koymayan aday, sonucu olduğundan güçlü sunmuş olur.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası iki sayıdır: önce ne, sonra ne. Şu sorular onları çıkarır:

- Bir performans testi kurgulaman istendi mi? Hedefi kim verdi: iş tarafı mı, mimar mı, sen mi çıkardın?
- Test ortamı üretime ne kadar benziyordu? Fark neredeydi: donanım, veri hacmi, dış servisler?
- Veriyi nasıl hazırladın? Üretimden kopya mı alındı, üretildi mi, maskelendi mi?
- Neyi izlediniz? Yalnız yanıt süresini mi, yoksa veritabanı ve altyapı ölçümlerini de mi?
- Bir temel ölçüm alındı mı? Sonraki koşumlar onunla karşılaştırıldı mı?
- Bulunan darboğaz düzeltildikten sonra test tekrar koşuldu mu? Fark neydi?
- Sonucu kime raporladın? Rapor karar değiştirdi mi, yoksa dosyada mı kaldı?

Böyle bir deneyimin yoksa uydurma. `256`'daki köprüyü kur ve şunu söyle: yük testi kurgusunu adım adım biliyorum, kurumsal ölçekte koşmadım, ilk fırsatta hedef tanımından başlayarak kurarım. Bu cümle, uydurulmuş bir vakadan her zaman güçlüdür.

### "Kararsız (flaky) testlerle nasıl başa çıkarsın?"

**Soru aslında ne ölçüyor?**

İlan otomasyonda orta-iyi seviye istiyor, ama bu soru yine de gelir. Çünkü kararsız test bir otomasyon sorusu değil, bir **disiplin sorusudur**. Görüşmeci tek şeye bakar: aday belirtiyi mi susturuyor, kök nedeni mi arıyor. En sık duyulan cevap yeniden denemedir ve en hızlı eleyen cevap da odur. Kararsız testin asıl zararı zaman kaybı değildir; güven kaybıdır. Takım kırmızı sonucu birkaç kez görmezden geldikten sonra gerçek hatayı da görmez olur (`187`).

**Sözlü cevap iskeleti**

1. **Zararı doğru adlandır.** Kararsız test sinyali gürültüye çevirir. Kırmızıya güvenilmiyorsa gerileme setinin (regression) değeri sıfırlanır.
2. **Önce görünür kıl.** Kararsız testleri işaretle ve say. Ölçülmeyen kararsızlık kalıcılaşır; sayılan kararsızlık gündeme girer.
3. **Kök nedeni sınıflandır.** Dört sınıf işi büyük ölçüde kapatır: bekleme, test verisi paylaşımı, sıra bağımlılığı, dış servis.
4. **Bekleme sınıfını aç.** Sabit süreli uykular yavaş makinede kalır, hızlı makinede boşa bekler. Doğru çözüm koşula bağlı beklemedir (`202`).
5. **Veri paylaşımını aç.** İki test aynı kaydı kullanıyorsa paralel koşumda birbirini bozar. Her test kendi verisini üretmeli ve kendi verisini toplamalıdır (`203`).
6. **Sıra bağımlılığını sına.** Testler rastgele sırayla koşulduğunda kalıyorsa aralarında gizli bir bağ vardır. Bu bağ testin kendisinde değil, kurulum düzenindedir.
7. **Dış servisi sınırla.** Ağdaki dalgalanma teste sızmamalıdır. Alt seviyede taklit (mock) kullan, uçtan uca seviyede ise dış bağımlılığı bilinçli ve az tut.
8. **Yeniden denemeyi doğru yerine koy.** Yeniden deneme (retry) çözüm değil **örtmedir**. Geçici bir kalkan olarak, süresi ve sahibi belli bir karantinayla birlikte kullanılır; kalıcı ayar olarak değil.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kararsiz test: dort kok neden sinifi ve yeniden denemenin neden cozum olmadigi">
  <defs>
    <marker id="sap-flaky-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kararsız test bir sınıflandırma işidir — önce hangi sınıf, sonra çözüm</text>
  <rect x="14" y="36" width="152" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 · Bekleme</text>
  <text x="90" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sabit süreli uyku</text>
  <text x="90" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">makine hızına bağımlılık</text>
  <text x="90" y="124" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çözüm: koşula bağlı</text>
  <text x="90" y="141" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">bekleme, açık zaman aşımı</text>
  <text x="90" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en sık görülen sınıf</text>
  <rect x="176" y="36" width="152" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="252" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">2 · Veri paylaşımı</text>
  <text x="252" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iki test aynı kaydı kullanır</text>
  <text x="252" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">paralel koşumda çakışır</text>
  <text x="252" y="124" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çözüm: her test kendi</text>
  <text x="252" y="141" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">verisini üretir ve toplar</text>
  <text x="252" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">paralelleşince patlar</text>
  <rect x="338" y="36" width="152" height="132" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="414" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Sıra bağımlılığı</text>
  <text x="414" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test öncekinin izine dayanır</text>
  <text x="414" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek başına koşunca kalır</text>
  <text x="414" y="124" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çözüm: rastgele sırayla</text>
  <text x="414" y="141" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">koş, kurulumu teste taşı</text>
  <text x="414" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en geç fark edilen sınıf</text>
  <rect x="500" y="36" width="146" height="132" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="573" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">4 · Dış servis</text>
  <text x="573" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ağ ve üçüncü taraf</text>
  <text x="573" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dalgalanma teste sızar</text>
  <text x="573" y="124" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çözüm: alt seviyede taklit,</text>
  <text x="573" y="141" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">uçtan uçta bilinçli ve az</text>
  <text x="573" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">senin denetiminde değil</text>
  <rect x="14" y="184" width="316" height="130" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="172" y="206" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yeniden deneme: örtme</text>
  <text x="172" y="230" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hat yeşile döner, sorun yerinde kalır</text>
  <text x="172" y="250" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gerçek hata da örtülebilir</text>
  <text x="172" y="270" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kararsızlık ölçülemez hâle gelir</text>
  <text x="172" y="296" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">kalıcı ayar yapılırsa güven sessizce erir</text>
  <line x1="332" y1="247" x2="342" y2="247" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sap-flaky-ok)"/>
  <rect x="346" y="184" width="300" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="496" y="206" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kök neden: çözüm</text>
  <text x="496" y="230" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kararsız testi işaretle ve say</text>
  <text x="496" y="250" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınıfını bul, düzelt, tekrar koş</text>
  <text x="496" y="270" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karantina süreli ve sahipli olsun</text>
  <text x="496" y="296" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">kırmızıya güven geri gelirse set değer kazanır</text>
</svg>
```

**Elenme sebepleri**

- "Yeniden deneme ekleriz, geçer." Bu soruda en hızlı eleyen cevaptır; belirtiyi susturmayı çözüm sanar.
- Sabit süreli uyku eklemek. Testi hem yavaşlatır hem başka bir makinede yine kararsız yapar.
- Kararsız testi silmek. Kapsama sessizce daralır ve silinen testin koruduğu risk kayda geçmez.
- "Ortam sorunu" deyip geçmek. Sınıflandırma yapılmadan verilen bu cevap incelemenin hiç yapılmadığını gösterir.
- Paralel koşumu kapatmayı kalıcı çözüm saymak. Sıra bağımlılığını gizler, koşum süresini de uzatır.
- Kararsızlığı hiç ölçmemek. Sayılmayan sorun önceliklendirilemez ve zamanla normalleşir.
- Karantinayı süresiz bırakmak. Kenara alınan test kimsenin sahibi olmadan yıllarca orada kalır.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile ikna eder. Şu sorular onu çıkarır:

- Kararsız testle karşılaştın mı? Belirti neydi: aynı kodda bir geçip bir kalması mı?
- Hangi sınıfa giriyordu? Bekleme mi, veri paylaşımı mı, sıra bağımlılığı mı, dış servis mi?
- Nasıl teşhis ettin? Tek başına koştun mu, sırayı değiştirdin mi, kayıt ve ekran görüntüsüne baktın mı?
- Takım ne yaptı? Yeniden deneme mi eklendi, test mi düzeltildi, yoksa görmezden mi gelindi?
- Kırmızı sonuca güven kayboldu mu? Kaybolduysa bunun bedelini ne zaman ödediniz?
- Kararsız testler sayılıyor muydu? Bir liste ya da etiket var mıydı?
- Düzeltmeden sonra tekrar kararsızlaştı mı? Kalıcı çözüm neydi?

Bu deneyim yoksa uydurma. `256`'daki köprüyü kur. Kararsızlığın sınıflarını ve her sınıfın çözümünü sırayla anlatabilmek zaten güçlü bir cevaptır; deneyimi olmayan aday burada dürüst konuşup muhakemeyle telafi eder.

### Bu bölümden çıkan ders

Bu beş sorunun ortak ekseni şudur: söylediğini neyle doğruluyorsun. SQL sorusunda kazanan cümle doğrulamanın arayüzden değil veritabanından yapıldığını söylemektir; yanına da yıkıcı komut öncesi `SELECT` kuralını koyarsın. API sorusunda kazanan cümle asıl kazancın erişim olduğudur: arayüzün üretemediği durumlar API'den denenir, şema doğrulanır, tekrar güvenliği sınanır. Performans sorularında kazanan cümle ortalamanın sakladığını, yüzdeliğin gösterdiğini söylemek ve kurguya hedefi sayıyla tanımlayarak başlamaktır; ortam üretime benzemiyorsa şerhi kendin koyarsın. Kararsız test sorusunda kazanan cümle yeniden denemenin çözüm değil örtme olduğudur; kök neden dört sınıfta aranır. Dördünde de aynı disiplin geçerlidir: ölçütü söyle, kanıtı söyle, bedeli söyle. Ve hiçbirinde olmayan deneyim uydurulmaz; `256`'daki dürüst köprü her zaman uydurmadan güçlüdür, çünkü uydurulan vaka ikinci soruda çöker.

# sap-1 | Temel
## Soru
Bir kayıt oluşturma senaryosunda arayüz "işlem başarılı" mesajını gösterdi. Test mühendisinin bundan sonra yapacağı en değerli adım hangisidir?
## A
Veritabanında oluşan satırı sorgulayıp alanların doğru yazıldığını doğrulamak
🤖 Doğru: arayüzün mesajı, kaydın doğru yazıldığını kanıtlamaz.
## B
Aynı senaryoyu bir kez daha koşup başarı mesajının yine göründüğünü görmek
🤖 aynı katmanı tekrar okur; yeni bir bilgi üretmez.
## C
Ekran görüntüsü alıp rapora eklemek ve senaryoyu geçti olarak işaretlemek
🤖 kanıt saklamak iyidir ama doğrulamanın yerine geçmez.
## D
Uygulama logunda hata satırı olmadığını görüp senaryoyu geçti saymak
🤖 sessiz log, verinin doğru yazıldığı anlamına gelmez.
## Doğru
A
## Açıklama
Arayüz bir **sunum katmanıdır** ve mesajı çoğu zaman isteğin kabul edildiğini söyler, kaydın doğru oluştuğunu değil. Alan yanlış eşlenmiş, tarih yanlış saat diliminde yazılmış, ondalık kısım kesilmiş ya da ilişkili tablo hiç güncellenmemiş olabilir. Bunların hiçbiri ekrandan görünmez. Bu yüzden testte SQL'in en değerli kullanımı sonucu **veritabanından doğrulamaktır** (`29`). Doğrulama üç şeye bakar: alanların değeri, biçimi ve ilişkili kayıtların oluşup oluşmadığı. B şıkkı aynı katmanı ikinci kez okur; tekrar, doğrulama değildir. C şıkkı kanıt toplar ve raporlama için gereklidir ama tek başına yeterli değildir; ekran görüntüsü yalnız ekranı gösterir. D şıkkı en aldatıcı olanıdır: uygulama hatayı yakalayıp sessizce yutmuş olabilir, dolayısıyla temiz log bir doğrulama değildir. SQL'in diğer iki kullanımı da cevaba eklenir: senaryonun ihtiyaç duyduğu veriyi hazırlamak ve koşum sonrası temizlik yaparak bir sonraki koşumun aynı başlangıç durumunu bulmasını sağlamak (`203`).

# sap-2 | Orta
## Soru
Görüşmeci "API testini neden yapıyoruz" diye soruyor. Bu soruda en güçlü gerekçe hangisidir?
## A
API testi arayüz testinden daha hızlı koşar; asıl gerekçe koşum süresinin kısalmasıdır
🤖 hız gerçek bir kazançtır ama tek başına asıl gerekçe değildir.
## B
API testi geçtiğinde arayüz testine gerek kalmaz; kapsama böylece tamamlanmış sayılır
🤖 iki katman farklı riski kapatır; biri diğerinin yerine geçmez.
## C
Arayüzün üretemediği durumlar — geçersiz gövde, yetkisiz istek, sınır değer — API'den denenebilir
🤖 Doğru: arayüz kendi kurallarıyla seni sınırlar, API katmanı sınırlamaz.
## D
API testi ölçümü kolaylaştırdığı için performans testinin yerini alabilecek bir yöntemdir
🤖 işlevsel doğrulama ile yük altındaki davranış ayrı konulardır.
## Doğru
C
## Açıklama
Arayüz kendi doğrulama kurallarıyla seni sınırlar. Bir alan yalnız sayı kabul ediyorsa oraya harf gönderemezsin; zorunlu alan boş bırakılamıyorsa eksik gövdeyi hiç üretemezsin. Oysa sunucunun bu durumlara nasıl karşılık verdiği tam olarak test edilmesi gereken şeydir, çünkü istemci dışındaki çağrılar bu kuralları hiç uygulamaz. API testinin asıl kazancı budur: **erişim**. Geçersiz gövde, eksik zorunlu alan, yetkisiz istek ve sınır değerler doğrudan denenir (`232`). Cevaba üç ayrıntı daha eklenir. Birincisi şema doğrulaması: durum kodu doğru olsa bile alan adı veya tipi değişmişse tüketici kırılır. İkincisi kimlik doğrulama ile yetkilendirme ayrımı; ikisi ayrı karşılık üretmelidir (`98`). Üçüncüsü tekrar güvenliği: aynı istek iki kez gönderildiğinde ikinci kayıt oluşmamalıdır. A şıkkındaki hız gerçektir ama gerekçenin kendisi değildir. B şıkkı kapsama iddiasını yanlış kurar; arayüz testi kullanıcı yolunu doğrular, API testi sözleşmeyi. D şıkkı iki ayrı test türünü karıştırır.

# sap-3 | Orta
## Soru
Bir ekip şunu öğrenmek istiyor: "Sistem hangi eşzamanlı kullanıcı sayısında kırılıyor ve kırıldığında nasıl davranıyor?" Bu soruyu hangi performans testi türü yanıtlar?
## A
Yük testi; beklenen yük altında hedeflerin tutup tutmadığını doğrular
🤖 hedefi doğrular, sınırı aramaz; soru sınırı soruyor.
## B
Stres testi; yükü artırarak kırılma noktasını ve sonrasındaki davranışı arar
🤖 Doğru: kırılma noktası ve kırılma sonrası davranış bu türün konusudur.
## C
Dayanıklılık testi; uzun süreli sabit yükte birikme ve sızıntı olup olmadığına bakar
🤖 zamanla biriken sorunları arar; kırılma noktasını vermez.
## D
Ani yükleniş testi; yükün sıfırdan tepeye sıçramasının etkisini ve toparlanmayı ölçer
🤖 sıçrama tepkisini ölçer; kapasite sınırını sistemli biçimde aramaz.
## Doğru
B
## Açıklama
Performans testi türleri araçla değil **sordukları soruyla** ayrılır; aynı araçla hepsi koşulabilir. Yük testi bir doğrulama testidir: beklenen yük altında hedefler tutuyor mu. Stres testi ise bir keşif testidir; yük kademeli olarak artırılır ve sistemin nerede kırıldığı bulunur. Kırılma noktası kadar önemli olan ikinci bilgi kırılma **biçimidir**: sistem tamamen mi düşüyor, yavaşlayarak ayakta mı kalıyor, yoksa düzgün hata mı döndürüyor. Dayanıklılık testi uzun süreli sabit yükte bellek sızıntısı ve bağlantı birikmesi gibi zamanla oluşan sorunları arar; kısa koşumlar bunları kaçırır. Ani yükleniş testi sıfırdan tepeye sıçramanın etkisini ve toparlanma süresini ölçer. Bunlara ölçeklenebilirlik testi de eklenir: kaynak eklendiğinde kapasite orantılı artıyor mu (`233`). Türü doğru seçmek kadar önemli olan bir şey daha vardır: sonuç ortalamayla değil yüzdelikle (p95, p99) raporlanır, yanına iş hacmi ve hata oranı konur.

# sap-4 | İleri
## Soru
Bir yük testi raporunda yalnız ortalama yanıt süresi yer alıyor ve değer hedefin altında. Buna rağmen kullanıcılardan yavaşlık şikâyeti gelmeye devam ediyor. Rapordaki asıl eksik nedir?
## A
Koşum süresi kısadır; ortalamanın anlamlı olması için test daha uzun koşulmalıdır
🤖 süre uzasa da ortalama yine dağılımı saklar; eksik başka yerde.
## B
Eşzamanlı kullanıcı sayısı düşüktür; sayı artırılırsa ortalama da yükselecektir
🤖 makul bir öneri ama şikâyet ile rapor arasındaki farkı açıklamaz.
## C
Hata oranı raporlanmamıştır; gelen şikâyetlerin kaynağı aslında hatalı cevaplardır
🤖 hata oranı raporlanmalı, ama yavaşlık şikâyetini bu tek başına açıklamaz.
## D
Ortalama dağılımı saklar; yüzdelik (p95, p99) verilmeden hedefin tuttuğu söylenemez
🤖 Doğru: şikâyet eden kullanıcı dağılımın kuyruğunda durur.
## Doğru
D
## Açıklama
Ortalama tek bir sayıdır ve **dağılımı yok eder**. İsteklerin büyük çoğunluğu çok hızlı dönerken küçük bir azınlık çok yavaş dönebilir; ortalama yine iyi görünür, çünkü hızlı çoğunluk yavaş azınlığı içinde eritir. Şikâyet eden kullanıcı tam olarak o azınlıktır. Bu yüzden performans hedefleri **yüzdelik dilimle** yazılır: p95, her yirmi istekten birinin bu değerden yavaş olduğunu söyler; p99 kuyruğun ucunu gösterir (`233`). Yüzdelik olmadan "hedef tuttu" cümlesi kurulamaz. A şıkkı süreyi tartışır; koşum uzasa bile ortalamanın saklama özelliği değişmez. B şıkkı yük seviyesini artırmayı önerir, bu ayrı bir testtir ve mevcut çelişkiyi açıklamaz. C şıkkındaki hata oranı gerçekten de her performans raporunda bulunmalıdır, ama sorudaki belirti yavaşlıktır. Cevabın devamında iki ekleme yapılır: raporda iş hacmi (throughput) ve hata oranı da yer almalı, ayrıca test ortamı üretimden farklıysa sayılar mutlak eşik değil karşılaştırma noktası olarak sunulmalıdır.

# sap-5 | İleri
## Soru
Bir performans testi, üretimin küçültülmüş bir kopyası olan ortamda ve neredeyse boş bir veritabanıyla koşuldu. Sonuçlar hedeflerin çok altında, yani iyi çıktı. Bu sonuç nasıl yorumlanmalıdır?
## A
Sonuç üretim için geçerlidir; ortam küçük olsa da yanıt süreleri oransal olarak korunur
🤖 ölçekleme oransal değildir; veri hacmi davranışı doğrusal olmayan biçimde değiştirir.
## B
Sonuç mutlak eşik sayılamaz; veri hacmi ve ortam farkı davranışı değiştirebilir
🤖 Doğru: gerçekçi veri hacmi olmadan sayılar karşılaştırma değeri taşır.
## C
Sonuç tümüyle geçersizdir; performans testi yalnız üretim ortamında koşulabilir
🤖 aşırı katı; küçük ortam eğilim ve karşılaştırma için hâlâ değerlidir.
## D
Sonuç yeterlidir; veri hacminin yanıt süresine etkisi yalnız toplu işlerde görülür
🤖 tekil sorgular da hacimle yavaşlar; eksik indeks ancak hacimle görünür.
## Doğru
B
## Açıklama
Boş bir veritabanında neredeyse **her sorgu hızlıdır**. Tablolar küçük olduğu için eksik indeks bir sorun üretmez, tarama ucuza gelir, önbellek her şeyi tutar. Aynı sorgu gerçek hacimde çok farklı davranır ve bu değişim doğrusal değildir (`146`). Ortam farkı da aynı yöndedir: daha az çekirdek, farklı disk, taklit edilen dış servisler ve farklı ağ gecikmesi sonucu birlikte kaydırır. Bu yüzden küçük ortamdan çıkan sayı **mutlak eşik** olarak kullanılamaz. Kullanılabileceği yer bellidir: temel ölçüm (baseline) alıp tek bir değişkeni değiştirdikten sonra aynı testi tekrarlamak ve eğilimi görmek. C şıkkı ise ters yönde aşırıya kaçar; sonucu tümüyle çöpe atmak da yanlıştır, çünkü karşılaştırmalı ölçüm gerçek bilgi üretir. A ve D şıkları farkı küçümser. Doğru kurgu şöyle özetlenir: hedefi sayıyla tanımla, gerçekçi veri hacmi kur, ısınma süresi bırak, uygulamayı ve veritabanını birlikte izle (`193`), sonucu temel ölçümle karşılaştır. Ortam üretime benzemiyorsa bu şerhi raporun içine kendin yaz.

# sap-6 | Uzman
## Soru
Gerileme setindeki birkaç test aynı kodda bazen geçiyor, bazen kalıyor. Takım hattı yeşile döndürmek için başarısız testleri otomatik olarak iki kez daha deneyecek kalıcı bir ayar ekliyor. Bu kararın asıl sakıncası nedir?
## A
Yeniden deneme belirtiyi susturur; kök neden yerinde kalır ve gerçek hata da örtülebilir
🤖 Doğru: hat yeşile döner ama sinyal kaybolur; asıl bedel budur.
## B
Yeniden deneme koşum süresini uzatır; asıl sakınca hattın yavaşlamasıdır
🤖 süre gerçekten uzar ama bu ikincil bir maliyettir, asıl sakınca değil.
## C
Yeniden deneme yalnız arayüz testlerinde işe yarar; API testlerinde hiçbir etkisi olmaz
🤖 katman ayrımı yanlış; sorun katmanda değil, belirtiyi örtmede.
## D
Sakınca yoktur; kararsızlık ortamdan gelir ve yeniden deneme yerleşik bir çözümdür
🤖 kararsızlığın sınıfı incelenmeden ortama fatura kesilemez.
## Doğru
A
## Açıklama
Kararsız (flaky) testin asıl bedeli zaman değil **güvendir**. Yeniden deneme kalıcı ayar hâline gelirse hat yeşile döner, ama testin verdiği sinyal kaybolur. İki tehlike birden doğar. Birincisi, kök neden yerinde kalır ve zamanla yayılır. İkincisi, gerçek bir hata da ikinci denemede tesadüfen geçebilir ve böylece örtülür; bu, gerileme setinin varlık sebebini ortadan kaldırır (`187`). Doğru yaklaşım sınıflandırmadır. Bekleme kaynaklı kararsızlıkta sabit süreli uyku yerine koşula bağlı bekleme kullanılır (`202`). Test verisi paylaşımında her test kendi verisini üretir ve toplar (`203`). Sıra bağımlılığı testleri rastgele sırayla koşarak açığa çıkarılır. Dış servis kaynaklı dalgalanma alt seviyede taklitle sınırlanır. B şıkkındaki süre maliyeti gerçektir ama ikincildir. C şıkkı sorunu katman meselesi sanır. D şıkkı incelemeden ortamı suçlar. Yeniden denemenin meşru tek kullanımı geçicidir: süresi ve sahibi belli bir karantinayla birlikte, kök neden bulunana kadar.
