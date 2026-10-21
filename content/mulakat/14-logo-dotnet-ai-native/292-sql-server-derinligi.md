---
slug: sql-server-derinligi
baslik: "SQL Server Derinliği: İzolasyon ve Anlık Görüntü, Kilitlenme, Yürütme Planı Okuma, Sayfalama"
kademe: İleri
sira: 292
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş açık uçlu sözlü prova sorusu var. Her sorunun altında dört parça bulacaksın: soru aslında ne ölçüyor, sözlü cevabın iskeleti, elenme sebepleri ve kendi hikâyenin yeri. Son parça bilerek boş bırakıldı; senin adına deneyim uydurulmaz, onu kendi geçmişinden çıkarman için sorular sorulur.

Bu dosya `283`'ün devamıdır ama tekrarı değildir. Orada uygulamanın tarafı anlatıldı: EF Core izleme (change tracking), `AsNoTracking`, N+1'in ORM tarafı, göç (migration) yönetimi, EF işlem yönetimi, kümelenmiş indeks (clustered index) ayrımının temel tanımı ve parametre koklamasının (parameter sniffing) tanıtımı. Burada anlatılan şey onun bir katman altıdır: veri tabanı motorunun kendisi. Bir mülakatta ORM soruları genellikle bir "veri tabanı derinliği" sorusuyla biter ve ayrım orada çıkar. `Include` cevabını herkes verir; "bu sorgu neden bazı saatlerde bekliyor" sorusuna cevap verebilen az olur. Senin avantajın ve tuzağın aynı yerde duruyor. Oracle biliyorsun (`227`), genel indeksleme bilgin var (`146`), performans teşhisi yapmışsın (`152`). Fikirlerin çoğu doğrudan taşınır: plan okumak, seçicilik düşünmek, işlem sınırını dar tutmak. Ama iki motorun **eşzamanlı okuma modeli** gerçekten farklıdır ve hata tam burada yapılır. Oracle'da okuyucu yazıcıyı beklemez; bu, motorun yapısına gömülü bir davranıştır. SQL Server'ın kutudan çıkan varsayılanı ise kilit tabanlı bir okumadır: okuyucu, kesinleşmemiş bir yazmayı bekler. Oracle'dan gelen bir ekip bu beklemeyi beklemez, "veri tabanı yavaş" diye teşhis koyar ve çareyi `NOLOCK` sanır. Bu dosyanın ana damarı budur.

Bir şerh baştan gelsin: SQL Server'ın sürümleri ve yapılandırmaları arasında davranış ve varsayılanlar değişir. Bir veri tabanında anlık görüntüye dayalı okuma açık olabilir, diğerinde kapalı; bir sürümde çevrimiçi yapılabilen bir bakım işi başka bir sürümde tabloyu kilitler; kilitlenme (deadlock) izlemesi bazı kurulumlarda hazır açıktır, bazılarında değildir. Mülakatta kesin sayı ya da sürüm iddiası kurmak gereksiz bir risktir; yanlış çıktığında bütün cevabın güvenilirliğini götürür. Doğru duruş şudur: mekanizmayı ve bedeli anlat, sonra "bu sürüme ve yapılandırmaya bağlıdır, bağlanmadan önce `sys` görünümlerinden ve dokümandan teyit eder, üretime yakın bir veri kümesinde ölçerim" de. Bu cümle seni zayıf göstermez; ölçen mühendis gösterir.

### "Bu sorgu yavaş, nereden başlarsınız?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi değil **sıra** ölçer. Görüşmecinin duymak istemediği cevap bellidir: "indeks eklerim". Zayıf aday çözümle başlar, güçlü aday ölçmeyle. Sorunun içinde ayrıca gizli bir tuzak var: "yavaş" kelimesi tanımsızdır. Neye göre yavaş, hangi saatte, her parametre için mi? Bu soruları soran aday, sorunu daha çözmeden bir kademe yukarı çıkar. Bu, `152`'deki gecikme teşhisi disiplininin veri tabanı tarafındaki hâlidir.

**Sözlü cevap iskeleti**

1. **Önce "yavaş"ı tanımla.** Kaç milisaniye, hangi yüzdelikte, ne zamandan beri? Her zaman mı yavaş yoksa belirli parametrelerde mi? Bu ayrım tek başına iki farklı teşhis yoluna gider: her zaman yavaşsa yapı sorunu, bazen yavaşsa plan ya da eşzamanlılık sorunu.
2. **Sorgunun gerçekten yavaş olduğunu doğrula.** Uç nokta yavaşsa suçlu her zaman sorgu değildir. Ağ, seri hâle getirme, N+1, bağlantı havuzu darlığı da aynı belirtiyi verir. Veri tabanı tarafındaki bekleme istatistiklerine ve komut süresine bakmadan sorguyu suçlama.
3. **Mantıksal okumayı ölç, süreyi değil.** Süre makinenin yüküne göre oynar; mantıksal okuma (logical reads) sayısı oynamaz. `SET STATISTICS IO ON` ile her tablo için kaç sayfa okunduğu görülür. Bu sayı, iyileştirmenin önce ve sonrasını karşılaştırdığın tek dürüst ölçüdür.
4. **Gerçek planı al, tahmini planı değil.** Tahmini plan (estimated plan) sorguyu çalıştırmadan üretilir; yalnız iyimserliği gösterir. Gerçek plan (actual plan) çalıştırma sonrası gelir ve içinde asıl aradığın şey vardır: her düğümde tahmin edilen satır sayısı ile gerçekleşen satır sayısı.
5. **Planda üç şeye bak.** Birincisi kalan tahmini (cardinality estimate) sapması: tahmin bin, gerçek bir milyon ise plan zaten yanlış varsayımla kurulmuştur. İkincisi tarama (scan) ile arama (seek) ayrımı. Üçüncüsü anahtar arama (key lookup) sayısı ve sıralama ya da karma (hash) adımlarının belleğe sığıp sığmadığı.
6. **Tarama her zaman kötü değildir.** Tablonun yüzde kırkını okuyacaksan tarama doğru seçimdir; arama, satır satır gidip geri döneceği için daha pahalı olur. Ayrıştırıcı cümle şudur: "tarama kötü değildir, **beklenmedik** tarama kötüdür."
7. **Aramayı engelleyen en yaygın hatayı adlandır.** Sütunun üzerine fonksiyon yazmak ya da örtük tip dönüşümü, indeksi devre dışı bırakır. `WHERE YEAR(tarih) = 2026` yerine aralık koşulu yazılır; sütun ile parametrenin tipi aynı tutulur.
8. **Veri hacmini ve istatistikleri sor.** Sorgu dün hızlıydı bugün yavaşsa üç şüpheli vardır: veri büyüdü, istatistikler bayatladı, ya da plan değişti. Parametre koklaması bu tablonun özel bir hâlidir ve tanıtımı `283`'te yapıldı.
9. **Değişikliği ölçerek bitir.** Öncesi ve sonrası mantıksal okuma sayısı söylenmeden hiçbir iyileştirme tamamlanmış sayılmaz. Ve indeks eklemenin yazma tarafına bir bedeli olduğu aynı cümlede söylenir.

```svg
<svg viewBox="0 0 660 372" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yurutme planinda tarama, arama ve anahtar arama yolculugu; kapsayan indeksin bunu nasil duzelttigi">
  <defs>
    <marker id="mssql-plan-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="mssql-plan-kirmizi" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Planda aranan şey maliyet yüzdesi değil, okunan satır sayısıdır</text>
  <rect x="14" y="36" width="200" height="132" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="114" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Tarama (scan)</text>
  <text x="114" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">indeksin ya da tablonun tamamı</text>
  <text x="114" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">baştan sona okunur</text>
  <text x="114" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sebep: uygun indeks yok, ya da</text>
  <text x="114" y="138" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sütuna fonksiyon / tip dönüşümü</text>
  <line x1="216" y1="102" x2="234" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mssql-plan-ok)"/>
  <rect x="238" y="36" width="188" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="332" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Arama (seek)</text>
  <text x="332" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ağaçta doğrudan aralığa inilir</text>
  <text x="332" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız gereken satırlar okunur</text>
  <text x="332" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koşul indeksin ilk sütunuyla</text>
  <text x="332" y="138" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hizalanmışsa mümkündür</text>
  <text x="332" y="159" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">çok satır dönecekse tarama daha ucuz olabilir</text>
  <line x1="428" y1="102" x2="446" y2="102" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#mssql-plan-kirmizi)"/>
  <rect x="450" y="36" width="196" height="132" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Anahtar arama (key lookup)</text>
  <text x="548" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">indekste olmayan sütun için</text>
  <text x="548" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">satırın kendisine geri gidilir</text>
  <text x="548" y="120" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">her satır için bir kez tekrarlanır</text>
  <text x="548" y="138" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">satır sayısıyla birlikte pahalılaşır</text>
  <line x1="548" y1="170" x2="548" y2="188" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mssql-plan-ok)"/>
  <rect x="338" y="192" width="308" height="86" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="492" y="213" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Çare: kapsayan indeks (covering / INCLUDE)</text>
  <text x="492" y="234" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sorgunun istediği sütunlar indekse dahil edilir</text>
  <text x="492" y="252" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">geri gidiş kalkar, mantıksal okuma düşer</text>
  <text x="492" y="270" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">bedel: indeks büyür, yazma maliyeti artar</text>
  <rect x="14" y="192" width="308" height="86" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="168" y="213" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ölçü: SET STATISTICS IO ON</text>
  <text x="168" y="234" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tablo başına mantıksal okuma sayısı verir</text>
  <text x="168" y="252" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">süre makine yüküyle oynar, bu sayı oynamaz</text>
  <rect x="14" y="288" width="632" height="74" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="309" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gerçek plan ile tahmini plan aynı şey değildir</text>
  <text x="330" y="330" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Tahmini plan iyimserliği gösterir; gerçek planda her düğümde tahmin ile gerçekleşen satır sayısı yan yana durur.</text>
  <text x="330" y="350" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Aradaki büyük sapma, planın yanlış varsayımla kurulduğunu söyler: istatistik, çarpık dağılım ya da yazılamayan koşul.</text>
</svg>
```

Ölçüm iki satırla başlar; mülakatta bu iki satırı söyleyebilmek bile fark üretir:

```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
-- sorgu burada çalışır; çıktıda tablo başına "logical reads" sayısı görünür
```

**Elenme sebepleri**

- Doğrudan indeks önermek. Planı açmadan verilen her öneri tahmindir ve çoğu zaman yeni bir yazma maliyeti ekler.
- "Yavaş" tanımını sorgusuz kabul etmek. Her zaman mı, bazen mi sorusu iki ayrı teşhis yoluna gider.
- Tahmini planla yetinmek. Tahmin ile gerçeğin sapmasını göremeyen aday asıl kanıtı elinden kaçırır.
- Süreyi tek ölçü saymak. Süre yükle oynar; mantıksal okuma sayısı iyileştirmenin dürüst kanıtıdır.
- Taramayı otomatik olarak hata saymak. Çok satır dönecek bir sorguda tarama doğru plandır.

**Kendi hikâyen — dürüst köprü**

Bu başlıkta deneyimin büyük olasılıkla Oracle tarafındandır ve olduğu gibi anlatılır. Şu sorular onu çıkarır:

- Yürütme planına bakarak düzelttiğin bir sorgu var mı? Planda seni asıl yere ne götürdü?
- Sütuna yazılan bir fonksiyon ya da tip uyuşmazlığı yüzünden indeksin kullanılmadığı bir durum gördün mü?
- Bir iyileştirmenin önce ve sonra sayılarını verebiliyor musun? Bu iki sayı hikâyenin omurgasıdır.
- Veri hacmi büyüdükçe planı değişen bir sorgu oldu mu? Nasıl fark ettiniz?

Ardından köprüyü tek cümleyle kur: "Teşhis sırası motordan bağımsızdır; SQL Server'da bunun karşılığı gerçek plan ile `SET STATISTICS IO`, Oracle'da plan ve otomatik izleme raporlarıydı."

### "READ COMMITTED ile SNAPSHOT arasındaki farkı anlatır mısınız?"

**Soru aslında ne ölçüyor?**

Bu, bu dosyanın en ayrıştırıcı sorusudur ve ilk bakışta göründüğünden çok daha derindir. Görüşmeci dört seviyenin adını saymanı beklemiyor; onu ezberleyen çok. Aradığı şey şu: aday **hangi anomaliyi kabul ettiğini** bilerek mi seçiyor, ve SQL Server'ın kutudan çıkan davranışının kilit tabanlı olduğunu biliyor mu. Oracle'dan gelen bir aday için burada gerçek bir tuzak var: Oracle'da okuma zaten çok sürümlüdür, o yüzden "okuyucu yazıcıyı beklemez" varsayımı kendiliğinden taşınır ve yanlış çıkar.

**Sözlü cevap iskeleti**

1. **Önce anomalileri adlandır.** Kirli okuma (dirty read): kesinleşmemiş veriyi görmek. Tekrarlanamayan okuma (non-repeatable read): aynı satırı iki kez okuyunca farklı değer görmek. Hayalet kayıt (phantom read): aynı koşulla iki kez okuyunca farklı sayıda satır görmek. İzolasyon seviyeleri bu üç şeyin hangisine izin verildiğinin tablosudur (`224`).
2. **SQL Server'ın varsayılanını söyle; asıl cümle budur.** Varsayılan `READ COMMITTED`'dir ve kutudan çıkan hâli **kilit tabanlıdır**. Okuyucu, kesinleşmemiş bir satıra denk gelirse paylaşımlı kilit için bekler. Yani okuma yazmayı, yazma okumayı bekletebilir.
3. **RCSI'yi burada getir.** Veri tabanı seviyesinde açılan `READ_COMMITTED_SNAPSHOT` ayarı (kısaca RCSI), aynı `READ COMMITTED` seviyesinin davranışını değiştirir: okuma artık kilit beklemez, satırın **ifade başlangıcındaki** kesinleşmiş sürümünü okur. Kod değişmez, tek satırlık bir veri tabanı ayarıyla kilit beklemeleri büyük ölçüde kalkar.
4. **SNAPSHOT ile farkı net çiz — buradan not alınır.** RCSI ifade (statement) düzeyinde tutarlılık verir: aynı işlem içindeki iki ayrı `SELECT` farklı sonuç görebilir. `SNAPSHOT` izolasyonu ise işlem (transaction) düzeyindedir: işlem boyunca tek bir zaman görüntüsü sabit kalır. Biri varsayılanı iyileştirir, diğeri açıkça istenen bir seviyedir.
5. **`SNAPSHOT`'ın bedelini söyle.** İşlem boyunca sabit görüntü, güncelleme çatışması riskini beraberinde getirir: sen okuduktan sonra başkası aynı satırı değiştirip kesinleştirirse, senin güncellemen çatışma hatasıyla düşer. Yani `SNAPSHOT` yazma tarafında yeniden deneme (retry) gerektirir.
6. **tempdb bedelini hiç atlama.** İki mekanizma da eski satır sürümlerini `tempdb`'deki sürüm deposunda tutar. Uzun süren bir işlem sürümlerin temizlenmesini engeller ve depo büyür; `tempdb` dolduğunda sorun tek bir sorguyu değil bütün sunucuyu vurur. Ayrıca satırlara sürüm bilgisi eklendiği için satır boyu bir miktar büyür. "Bedava değildir" cümlesi bu cevabın olgunluk göstergesidir.
7. **Oracle kıyasını yap.** Oracle çok sürümlü okumayı varsayılan olarak yapar; eski sürümü geri alma (undo) alanından okur ve okuyucular yazıcıları engellemez (`227`). SQL Server'da bu davranış bir **tercihtir**, kutudan çıkan hâl değildir. Bu cümle, iki motoru gerçekten karşılaştırabildiğini gösterir.
8. **`REPEATABLE READ` ve `SERIALIZABLE`'ı kısa geç.** İkisi de kilit süresini uzatır; `SERIALIZABLE` aralık kilitleriyle hayalet kaydı da kapatır ama iş hacmini düşürür ve kilitlenme olasılığını artırır. Bunlar dar ve bilinçli kullanılır, varsayılan yapılmaz.
9. **Kararı bir cümleye bağla.** "Okuma yükü yazmayı bekletiyorsa önce RCSI'yi ölçerim; işlem boyunca sabit görüntü gerekiyorsa o işleme özel `SNAPSHOT` seçerim; her iki durumda da `tempdb` büyümesini ve uzun işlemleri izlerim."

```svg
<svg viewBox="0 0 660 380" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Izolasyon seviyeleri ile okuma anomalileri matrisi ve RCSI'nin nereye dustugu">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Seviye seçmek, hangi anomaliyi kabul ettiğini seçmektir</text>
  <rect x="14" y="32" width="200" height="34" fill="#1C3247"/>
  <rect x="214" y="32" width="108" height="34" fill="#1C3247"/>
  <rect x="322" y="32" width="108" height="34" fill="#1C3247"/>
  <rect x="430" y="32" width="108" height="34" fill="#1C3247"/>
  <rect x="538" y="32" width="108" height="34" fill="#1C3247"/>
  <text x="114" y="54" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Seviye</text>
  <text x="268" y="54" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kirli okuma</text>
  <text x="376" y="49" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Tekrarlanamayan</text>
  <text x="376" y="61" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">okuma</text>
  <text x="484" y="54" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Hayalet kayıt</text>
  <text x="592" y="49" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Okuyucu yazıcıyı</text>
  <text x="592" y="61" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">bekler mi?</text>
  <rect x="14" y="66" width="632" height="32" fill="#FBE9E9" stroke="#C9D4DC" stroke-width="1"/>
  <text x="24" y="87" font-size="10" font-weight="600" fill="#B23B3B" font-family="system-ui">READ UNCOMMITTED (NOLOCK)</text>
  <text x="268" y="87" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="376" y="87" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="484" y="87" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="592" y="87" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">beklemez</text>
  <rect x="14" y="98" width="632" height="32" fill="#FDF3E0" stroke="#C9D4DC" stroke-width="1"/>
  <text x="24" y="119" font-size="10" font-weight="600" fill="#B07D20" font-family="system-ui">READ COMMITTED (kilitli · varsayılan)</text>
  <text x="268" y="119" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="376" y="119" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="484" y="119" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="592" y="119" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">BEKLER</text>
  <rect x="14" y="130" width="632" height="32" fill="#E9F4EE" stroke="#C9D4DC" stroke-width="1"/>
  <text x="24" y="151" font-size="10" font-weight="600" fill="#25804C" font-family="system-ui">READ COMMITTED SNAPSHOT (RCSI)</text>
  <text x="268" y="151" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="376" y="151" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="484" y="151" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="592" y="151" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">beklemez</text>
  <rect x="14" y="162" width="632" height="32" fill="#F2F5F7" stroke="#C9D4DC" stroke-width="1"/>
  <text x="24" y="183" font-size="10" font-weight="600" fill="#1C3247" font-family="system-ui">REPEATABLE READ</text>
  <text x="268" y="183" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="376" y="183" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="484" y="183" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">olur</text>
  <text x="592" y="183" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">bekler (uzun)</text>
  <rect x="14" y="194" width="632" height="32" fill="#F2F5F7" stroke="#C9D4DC" stroke-width="1"/>
  <text x="24" y="215" font-size="10" font-weight="600" fill="#1C3247" font-family="system-ui">SERIALIZABLE</text>
  <text x="268" y="215" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="376" y="215" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="484" y="215" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="592" y="215" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">bekler (en çok)</text>
  <rect x="14" y="226" width="632" height="32" fill="#EAF1F7" stroke="#C9D4DC" stroke-width="1"/>
  <text x="24" y="247" font-size="10" font-weight="600" fill="#186399" font-family="system-ui">SNAPSHOT (işlem boyunca tek görüntü)</text>
  <text x="268" y="247" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="376" y="247" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="484" y="247" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">yok</text>
  <text x="592" y="247" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">beklemez</text>
  <rect x="14" y="268" width="308" height="100" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="289" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">RCSI ile SNAPSHOT aynı şey değildir</text>
  <text x="168" y="310" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">RCSI: tutarlılık ifade (statement) düzeyinde</text>
  <text x="168" y="328" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SNAPSHOT: tutarlılık işlem düzeyinde</text>
  <text x="168" y="348" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">SNAPSHOT güncelleme çatışması üretebilir</text>
  <rect x="338" y="268" width="308" height="100" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="492" y="289" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Bedel: tempdb sürüm deposu</text>
  <text x="492" y="310" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski satır sürümleri tempdb'de tutulur</text>
  <text x="492" y="328" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uzun işlem temizliği engeller, depo büyür</text>
  <text x="492" y="348" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">tempdb dolarsa sorun tüm sunucuyu vurur</text>
  <text x="492" y="364" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Oracle'da karşılığı geri alma alanıdır (227)</text>
</svg>
```

Ayarın kendisi tek satırdır; ama tek satır olması bedelsiz olduğu anlamına gelmez:

```sql
-- veri tabanı seviyesinde; kod değişmez, davranış değişir
ALTER DATABASE Muhasebe SET READ_COMMITTED_SNAPSHOT ON;
-- islem duzeyinde sabit goruntu isteyen ozel akislar icin ayri bir seviye
ALTER DATABASE Muhasebe SET ALLOW_SNAPSHOT_ISOLATION ON;
```

**Elenme sebepleri**

- Dört seviyeyi ezberden saymak ama hangi anomaliyi kabul ettiğini söylememek. Liste bilgi değildir, karar bilgidir.
- SQL Server'ın varsayılanını Oracle gibi çok sürümlü sanmak. Bu, Oracle'dan gelen adayın en sık düştüğü yerdir.
- RCSI ile `SNAPSHOT`'ı aynı şey saymak. Biri ifade düzeyinde, diğeri işlem düzeyinde tutarlılık verir.
- `tempdb` bedelini hiç anmamak. Bedelsiz anlatılan her iyileştirme, bedeli üretimde öğrenilir.
- `SNAPSHOT`'ın güncelleme çatışmasını atlamak. Yeniden deneme yolu yoksa o seviye yazma akışını kırar.

**Kendi hikâyen — dürüst köprü**

Bu başlıkta Oracle deneyimin doğrudan işe yarar; kıyas kurmak zaten istenen şeydir. Şu sorular hikâyeyi çıkarır:

- İzolasyon seviyesini bilinçli olarak değiştirdiğin bir akış oldu mu? Hangi anomaliyi kabul ettin?
- Okuyucunun yazıcıyı beklediği ya da tersinin yaşandığı bir olay gördün mü? Nasıl teşhis edildi?
- "Aynı işlem içinde iki kez okuduk, farklı sonuç geldi" tipinde bir hata yaşadın mı?
- Bir raporun tutarlı bir zaman görüntüsü göstermesi gerekti mi? Nasıl sağladınız?

Köprü cümlen şu olabilir: "Oracle'da çok sürümlü okuma varsayılandı, o yüzden bu tartışmayı hiç yapmadım; SQL Server'da aynı davranışı RCSI ile açık bir tercih olarak kurmak gerektiğini ve bedelinin `tempdb` olduğunu biliyorum."

### "Biz `NOLOCK` kullanıyoruz" denirse ne dersin?

Bu ayrı bir sözlü prova sorusu değil, yukarıdaki sorunun altında neredeyse her zaman gelen bir devam sorusudur; hazırlıksız yakalanma bu dosyada en pahalı yerdir.

`NOLOCK` ipucu, o sorguyu `READ UNCOMMITTED` seviyesinde çalıştırır. Yaygın olmasının sebebi basittir: kilit beklemesini anında bitirir ve rapor hızlanır. Üç bedeli vardır ve üçünü de sayabilmek gerekir. Birincisi kirli okumadır: geri alınacak bir yazmayı okursun, yani hiç var olmamış bir tutarı raporlarsın. İkincisi **atlanan satır**: okuma sırasında sayfa bölünmesi olursa bazı satırlar hiç görülmeyebilir. Üçüncüsü **iki kez okunan satır**: aynı satır sonuca iki kez düşebilir. İkincisi ve üçüncüsü daha az bilinir ve mülakatta söylendiğinde fark üretir, çünkü bunlar "biraz eski veri okurum, olsun" savunmasını çürütür — sorun bayatlık değil, **yanlışlıktır**.

Mülakatta karşı taraf "biz `NOLOCK` kullanıyoruz" derse doğru tavır tartışmak değil, ayrım koymaktır: "Hangi sorgularda? Yaklaşık bir sayaç için kabul edilebilir; mutabakat, bakiye ve tutar raporunda kabul edilemez. Asıl çözüm RCSI'dir: kod değişmeden kilit beklemesi kalkar ve kirli okuma da olmaz." Bu cevap hem takımın kararına saygı gösterir hem doğru yolu adlandırır. `283`'te `NOLOCK`'un bir performans çözümü olmadığı zaten söylendi; buradaki katkı, **doğru alternatifin adını** koymaktır.

### "Üretimde kilitlenme (deadlock) alıyoruz, ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu soru üretim olgunluğu ölçer. Kilitlenme, tek başına bir hata değil; iki işlemin birbirinin tuttuğu kaynağı beklemesiyle oluşan bir **tasarım belirtisidir**. Zayıf cevap "yeniden denerim" der ve orada durur. Güçlü cevap üç kata ayırır: hemen ne yapılır, kök sebep nasıl bulunur, kalıcı çözüm nedir. Ödeme ve muhasebe tarafında bu ayrım daha da önemlidir, çünkü kurban seçilen işlem geri alınır ve o iş tekrar çalıştırılacaksa eşgüçlü (idempotent) olmak zorundadır (`130`).

**Sözlü cevap iskeleti**

1. **Mekanizmayı tek cümlede ver.** İki işlem, iki kaynağı **ters sırada** kilitlerse her biri diğerinin bıraktığını bekler. Motor bunu bir bekleme çevrimi (cycle) olarak görür, birini kurban seçer, geri alır ve hata döndürür. Yani kilitlenme kilitlenmeyi kendisi çözer; senin çözmen gereken şey tekrarıdır.
2. **Kurban seçimini söyle.** Motor genellikle geri alması **daha ucuz** olanı kurban seçer; bu davranış bir öncelik ayarıyla etkilenebilir. Yani hangi işlemin öleceğini sen seçmezsen motor seçer, çoğu zaman kısa olan ölür.
3. **Teşhisi kanıta bağla.** Kilitlenme grafiği (deadlock graph) alınır; içinde iki işlemin hangi kaynağı tuttuğu, hangisini beklediği ve hangi sorguları çalıştırdığı yazar. Kaynak genelde iki tablo, bazen aynı tablonun bir indeksi ile kümelenmiş anahtarıdır. Grafiği okumadan yapılan her çözüm tahmindir.
4. **En sık kök sebebi adlandır: tutarsız erişim sırası.** Bir servis önce `Musteri` sonra `Siparis` günceller, diğeri tersini yapar. Çare kültüreldir: kaynaklara **her yerde aynı sırada** dokunulur ve bu bir kod incelemesi kuralı hâline getirilir.
5. **İkinci kök sebebi ekle: uzun işlem.** İşlem içinde dış servis çağırmak, kullanıcı onayı beklemek ya da dosya yazmak kilit süresini uzatır. Kural: işlem dar olur, dış çağrı işlem dışına alınır.
6. **Üçüncü kök sebebi söyle; çoğu aday bunu bilmez.** Eksik indeks kilitlenme üretir. Uygun indeks yoksa güncelleme, bulacağı satırı ararken **fazla satır** kilitler; kilitlenen alan büyüdükçe çakışma olasılığı artar. Yani "kilitlenme sorunu" bazen bir indeks sorunudur.
7. **Yeniden denemenin yerini net koy.** Yeniden deneme kaçınılmazdır ama **çözüm değil, emniyet ağıdır**. Yeri, işlem sınırının dışıdır: geri alınan işlemin tamamı baştan çalıştırılır, kısmi devam edilmez. Üstel geri çekilme (exponential backoff) ve sınırlı deneme sayısı uygulanır, iş eşgüçlü tasarlanır (`130`).
8. **İzolasyonun etkisini de söyle.** RCSI okuma ile yazma arasındaki çakışmayı büyük ölçüde bitirir; ama yazma ile yazma arasındaki kilitlenmeyi bitirmez. Bunu ayırt edebilmek, konuyu gerçekten anladığını gösterir.
9. **Kalıcılığı ölçüye bağla.** Kilitlenme sayısı bir ölçüm (metric) hâline getirilir ve alarm kurulur. Düzeldiğini iddia etmek yetmez; sayının düştüğü gösterilir (`152`).

```svg
<svg viewBox="0 0 660 366" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kilitlenme grafigi: iki islemin capraz kilit beklemesi, kurban secimi ve kacinma desenleri">
  <defs>
    <marker id="mssql-kilit-bekle" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
    <marker id="mssql-kilit-tut" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#25804C"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kilitlenme bir hata değil, ters erişim sırasının belirtisidir</text>
  <rect x="24" y="36" width="180" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="114" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İşlem A</text>
  <text x="114" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">önce Musteri, sonra Siparis</text>
  <rect x="456" y="36" width="180" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="546" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İşlem B</text>
  <text x="546" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">önce Siparis, sonra Musteri</text>
  <rect x="24" y="176" width="180" height="52" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="114" y="197" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Musteri satırı 42</text>
  <text x="114" y="216" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kilidi A tutuyor</text>
  <rect x="456" y="176" width="180" height="52" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="546" y="197" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Siparis satırı 7</text>
  <text x="546" y="216" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kilidi B tutuyor</text>
  <line x1="98" y1="94" x2="98" y2="172" stroke="#25804C" stroke-width="1.6" marker-end="url(#mssql-kilit-tut)"/>
  <text x="76" y="136" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">tutar</text>
  <line x1="562" y1="94" x2="562" y2="172" stroke="#25804C" stroke-width="1.6" marker-end="url(#mssql-kilit-tut)"/>
  <text x="586" y="136" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">tutar</text>
  <line x1="176" y1="94" x2="490" y2="172" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#mssql-kilit-bekle)"/>
  <line x1="484" y1="94" x2="170" y2="172" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#mssql-kilit-bekle)"/>
  <rect x="252" y="112" width="156" height="42" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="129" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">çapraz bekleme çevrimi</text>
  <text x="330" y="146" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">ikisi de diğerini bekler</text>
  <rect x="212" y="176" width="236" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="197" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Motor kurbanı seçer</text>
  <text x="330" y="216" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">geri alması ucuz olan ölür, işlem geri sarılır</text>
  <rect x="14" y="242" width="204" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="263" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kalıcı çözüm</text>
  <text x="116" y="284" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her yerde aynı erişim sırası</text>
  <text x="116" y="303" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işlemi kısa tut, dış çağrı dışarı</text>
  <text x="116" y="322" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">doğru indeks: az satır kilitlensin</text>
  <rect x="228" y="242" width="204" height="112" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="263" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Yeniden deneme</text>
  <text x="330" y="284" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işlem sınırının DIŞINDA durur</text>
  <text x="330" y="303" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tüm iş birimi baştan çalışır</text>
  <text x="330" y="322" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınırlı sayı + üstel geri çekilme</text>
  <text x="330" y="343" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">iş eşgüçlü değilse çift kayıt üretir</text>
  <rect x="442" y="242" width="204" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="544" y="263" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Yanlış çözümler</text>
  <text x="544" y="284" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">NOLOCK eklemek</text>
  <text x="544" y="303" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zaman aşımını uzatmak</text>
  <text x="544" y="322" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız yeniden deneyip susmak</text>
  <text x="544" y="343" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">RCSI yazma-yazma kilitlenmesini bitirmez</text>
</svg>
```

**Elenme sebepleri**

- Kilitlenme ile kilit beklemesini (blocking) karıştırmak. İkisi ayrı olaydır; biri çevrimdir, diğeri sıradan bir sıraya girmedir.
- "Yeniden denerim" deyip durmak. Yeniden deneme belirtiyi susturur, sebebi bulmaz.
- Yeniden denenen işin eşgüçlü olup olmadığını hiç sormamak. Ödeme akışında bu doğrudan çift kayıt demektir (`130`).
- Kilitlenme grafiğini hiç açmamak. Kanıta bakmayan aday, kök sebebi tahmin eder.
- Eksik indeksin kilitlenme ürettiğini bilmemek. Bu bağlantıyı kuran aday burada belirgin biçimde öne çıkar.

**Kendi hikâyen — dürüst köprü**

Kilitlenme motor bağımsız bir konudur; Oracle tarafındaki deneyimin burada birebir geçerlidir. Şu sorular onu çıkarır:

- Üretimde kilitlenme gördün mü? Belirtisi neydi: hata mı, yavaşlık mı, tamamlanmayan iş mi?
- İşlem içinde dış servis çağrısı yapan bir kod düzelttin mi? Süre ne kadar kısaldı?
- Yeniden denenen bir iş çift kayıt üretti mi? Nasıl fark edildi, nasıl kapatıldı?

Köprü cümlen: "Kilitlenmenin mekanizması iki motorda da aynıdır; SQL Server tarafında ek olarak kilitlenme grafiğini okumayı ve RCSI'nin neyi çözüp neyi çözmediğini ayırmayı biliyorum."

### "Kümelenmiş ve kümelenmemiş indeks farkı nedir, ne zaman hangisi?"

**Soru aslında ne ölçüyor?**

Temel tanım `283`'te verildi ve o kadarı çoğu adayda var. Bu soruda ayrım daha derinde: aday indeksi bir **fiziksel yapı** olarak görüyor mu, yoksa "arama hızlandıran sihirli şey" olarak mı. `146`'daki genel indeksleme bilgisinin üstüne SQL Server'a özgü üç gerçek eklenir ve fark orada çıkar.

**Sözlü cevap iskeleti**

1. **Fiziksel gerçeği söyle.** SQL Server'da kümelenmiş indeks tabloya eklenen bir yapı değildir; **tablonun kendisidir**. Yaprak seviyesinde satırların tamamı, kümeleme anahtarı sırasına göre durur. Bu yüzden bir tabloda yalnız bir tane olabilir.
2. **Yığın tabloyu (heap) karşısına koy.** Kümelenmiş indeksi olmayan tabloya yığın denir; satırların garantili bir sırası yoktur ve indeksler satıra fiziksel bir tanımlayıcıyla işaret eder. Yığın, toplu yükleme gibi dar durumlarda tercih edilir; işlem yükü taşıyan bir tabloda genellikle istenmez.
3. **Kümelenmemiş indeksin taşıdığı yükü söyle; bu cümle ayrıştırıcıdır.** Kümelenmemiş indeksin yaprağında yalnız kendi anahtarı yoktur, **kümeleme anahtarı da vardır** — satıra dönüş bilgisi odur. Sonucu şudur: kümeleme anahtarı geniş seçilirse tablodaki **bütün** kümelenmemiş indeksler şişer. Bu, tek bir kararın bütün indekslere yayılması demektir.
4. **Anahtar seçimini buradan türet.** İyi bir kümeleme anahtarı dar, değişmeyen, tekil ve mümkünse artan olur. Rastgele bir tanımlayıcı seçilirse araya ekleme yapılır, sayfa bölünmesi (page split) artar ve parçalanma hızlanır. Artan anahtar bu sorunu çözer ama çok yazıcılı sistemlerde sayfanın sonunda bir sıcak nokta üretebilir; ikisi arasındaki dengeyi söylemek olgunluk gösterir.
5. **Anahtar aramayı ve kapsayan indeksi bağla.** Kümelenmemiş indeks sorgunun istediği bir sütunu taşımıyorsa satıra geri gidilir; buna anahtar arama denir ve satır başına tekrarlanır. Çare, o sütunları indekse `INCLUDE` ile eklemektir: kapsayan indeks (covering index). Böylece sorgu indeksten çıkmadan cevaplanır.
6. **Anahtar sütunu ile dahil sütunu ayır.** Anahtar sütunları sıralamayı ve aramayı belirler; `INCLUDE` sütunları yalnız yaprakta taşınır, aramaya katılmaz. Bu ayrımı bilmek, kapsayan indeksi ezberden değil anlayarak kurduğunu gösterir.
7. **Sütun sırasının önemini söyle.** Çok sütunlu indekste sıra kritiktir: koşul indeksin ilk sütunuyla hizalanmıyorsa arama yerine tarama olur. Eşitlik koşulları önce, aralık koşulu sonra gelir.
8. **Parçalanma ve dolgu faktörünü kısa tut.** Zamanla sayfalar bölünür, mantıksal sıra ile fiziksel sıra ayrışır; buna parçalanma (fragmentation) denir. Dolgu faktörü (fill factor), sayfalarda ekleme için pay bırakır: bölünmeyi azaltır ama sayfa sayısını ve okunan veriyi artırır. Bakım eşikleri ve yeniden düzenleme ile yeniden kurma arasındaki tercih sürüme ve tablo boyutuna bağlıdır; ezber sayı vermek yerine ölçüldüğü söylenir.
9. **Yazma maliyetini kapanışa koy.** Her indeks bir kopya demektir: her ekleme, güncelleme ve silme onu da günceller. Beş indeksli bir tabloya yazmak, tek indeksli bir tabloya yazmaktan pahalıdır. Bu yüzden kullanılmayan indeksler bir kazanç değil, sürekli ödenen bir vergidir; kullanım istatistiklerine bakılarak temizlenir.

**Elenme sebepleri**

- Kümelenmiş indeksi "sadece daha hızlı bir indeks" sanmak. O, tablonun fiziksel düzenidir.
- Kümelenmemiş indeksin kümeleme anahtarını taşıdığını bilmemek. Geniş anahtarın bütün indeksleri şişirdiğini görmeyen aday burada durur.
- Her sorgu için bir indeks önermek. Okuma kazancı, yazma maliyetiyle birlikte konuşulmadıysa öneri eksiktir.
- Anahtar sütunu ile `INCLUDE` sütununu aynı şey saymak. Biri aramaya girer, diğeri yalnız taşınır.

**Kendi hikâyen — dürüst köprü**

- Rastgele bir tanımlayıcıyı anahtar yapmanın maliyetini yaşadın mı? Belirti neydi?
- İndeks ekleyerek bir sorguyu hızlandırdın; yazma tarafında bir yavaşlama ölçüldü mü?
- Gereken sütunları indekse ekleyerek geri gidişi kaldırdığın bir örnek var mı?

Köprü cümlen: "İndeks tasarımının mantığı motordan bağımsız; SQL Server'a özgü olan, kümeleme anahtarının bütün kümelenmemiş indekslerde tekrar etmesi ve bunun anahtar seçimini bir tasarım kararına çevirmesi."

### "Sayfalı listeleme ekranı derin sayfalarda yavaşlıyor. Çözüm?"

**Soru aslında ne ölçüyor?**

Bu, gerçek üretim sorunudur ve kurumsal ürün geliştiren bir ekipte neredeyse her listede karşılaşılır. Ölçtüğü şey şudur: aday `OFFSET`'in **neden** yavaşladığını mekanizma düzeyinde anlatabiliyor mu, ve alternatifin bedelini biliyor mu. Ayrıca sinsi bir doğruluk sorunu barındırır: kararsız sıralama ile sayfalama, kullanıcıya bazı kayıtları hiç göstermez.

**Sözlü cevap iskeleti**

1. **Mekanizmayı ver.** `OFFSET 100000 ROWS FETCH NEXT 20 ROWS ONLY` demek, veri tabanına "yüz bin satırı üret, at, sonraki yirmiyi ver" demektir. Atılan satırlar da okunur ve sıralanır. Yani maliyet sayfa numarasıyla birlikte **doğrusal** büyür; ilk sayfa ucuz, beş yüzüncü sayfa felakettir.
2. **Toplam sayı sorgusunu ayrı bir suçlu olarak göster.** Çoğu ekranda sayfalamanın yanında bir `COUNT(*)` çalışır ve o, filtrenin tamamını taramak zorunda kalır. Derin sayfa yavaşlığının bir kısmı çoğu zaman buradan gelir; yaklaşık sayı ya da "daha fazla var" göstergesi çoğu ekranda yeterlidir.
3. **Anahtar-kümesi sayfalamayı (keyset / seek pagination) anlat.** Sayfa numarası yerine **son görülen satırın anahtarı** taşınır. Sonraki sayfa "bu anahtardan büyük ilk yirmi satır" olarak istenir. Böylece indeks üzerinde doğrudan aramaya inilir ve maliyet sayfa derinliğinden bağımsız hâle gelir.
4. **Sıralamanın kararlı olması gereğini vurgula.** Sıralama sütunu tekil değilse (örneğin yalnız tarih) eşit değerli satırların sırası çağrılar arasında değişebilir; kullanıcı bir kaydı iki kez görür, başka bir kaydı hiç görmez. Çare, sıralamaya tekil bir sütunu ikinci ölçüt olarak eklemektir. Aynı sütun çifti indeksin de anahtarı olur.
5. **Bedeli dürüstçe söyle.** Anahtar-kümesi sayfalama "37. sayfaya git" özelliğini vermez; ileri geri gezinmeye uygundur. Sonsuz kaydırma, "daha fazla göster" ve dışa aktarım için doğal, klasik numaralı sayfalama için değildir. Ürün tarafıyla konuşulacak bir tasarım kararıdır.
6. **Dışa aktarımı ayır.** Yüz binlerce satır dışa aktarılacaksa sayfalama zaten yanlış araçtır; anahtara göre parçalanan, durdurulabilir ve devam ettirilebilir bir toplu iş yazılır.
7. **Önbelleği yanlış çözüm olarak işaretle.** İlk sayfaların önbelleğe alınması (`144`) toplam yükü azaltabilir ama derin sayfa sorununu çözmez; derin sayfalar zaten seyrek istenir ve her biri yine pahalıdır. Önbellek, yanlış sorguyu ucuzlatmaz.

Anahtar-kümesi sayfalamanın iskeleti şudur; ikinci sıralama sütunu kararlılık için oradadır:

```sql
-- ilk sayfa
SELECT TOP (20) Id, MusteriId, Tutar, OlusmaTarihi
FROM Siparis
WHERE MusteriId = @musteri
ORDER BY OlusmaTarihi DESC, Id DESC;

-- sonraki sayfa: son gorulen satirin (tarih, id) ikilisi tasinir
SELECT TOP (20) Id, MusteriId, Tutar, OlusmaTarihi
FROM Siparis
WHERE MusteriId = @musteri
  AND (OlusmaTarihi < @sonTarih
       OR (OlusmaTarihi = @sonTarih AND Id < @sonId))
ORDER BY OlusmaTarihi DESC, Id DESC;
```

Bu sorgunun hızlı olması indekse bağlıdır: `MusteriId, OlusmaTarihi, Id` sırasıyla kurulmuş bir indeks, filtreyi ve sıralamayı birlikte karşılar; ekranda gösterilen tutar sütunu `INCLUDE` ile eklenirse anahtar arama da ortadan kalkar.

**Elenme sebepleri**

- `OFFSET`'in neden yavaşladığını açıklayamamak. "Yavaş" demek yetmez; atılan satırların da okunduğu söylenmelidir.
- Toplam sayı sorgusunu hiç anmamak. Ekranın yavaşlığının bir kısmı çoğu zaman oradadır.
- Sıralamanın kararlılığını atlamak. Bu bir performans değil, **doğruluk** sorunudur; atlanan kayıt üretir.
- Anahtar-kümesi sayfalamanın kısıtını gizlemek. "37. sayfaya git" özelliğinin kalkacağı baştan söylenmelidir.
- Çözümü önbelleğe bağlamak. Pahalı sorgu önbelleklenince ucuzlamaz, yalnız bazen gizlenir (`144`).

**Kendi hikâyen — dürüst köprü**

- Derin sayfada yavaşlayan bir liste ekranı gördün mü? Kaç kayıttan sonra hissedildi?
- Kararsız sıralama yüzünden atlanan ya da iki kez görünen kayıt şikâyeti yaşandı mı?
- Toplam sayı sorgusunu kaldırdığınız ya da yaklaşık hâle getirdiğiniz oldu mu? Ürün tarafı ne dedi?

Köprü cümlen: "Bu, motor bağımsız bir sayfalama sorunu; SQL Server'da sözdizimi `OFFSET/FETCH` ve `TOP` ile kurulur, çözüm yine anahtar-kümesi sayfalamadır."

### Yan başlıklar: CTE, pencere fonksiyonları, tempdb ve saklı yordam tartışması

Ortak tablo ifadesi (CTE, common table expression) hakkında en sık duyulan yanılgı şudur: "CTE geçici bir tablo gibidir, bir kez hesaplanır ve tekrar kullanılır." Bu doğru değildir. CTE çoğu durumda sorguya **satır içi genişletilir**; aynı CTE'ye iki kez atıfta bulunursan altındaki iş iki kez çalışabilir. CTE'nin asıl faydası okunabilirliktir: iç içe geçmiş alt sorguları isimlendirilmiş adımlara böler. Gerçekten bir kez hesaplanıp saklanması gerekiyorsa doğru araç geçici tablo ya da tablo değişkenidir; ikisi de `tempdb`'de yer tutar ve bu bir maliyettir. Özyinelemeli CTE ise dar ama gerçek bir kullanım alanı taşır: hiyerarşi gezinmesi — hesap planı, ürün ağacı, organizasyon şeması. Özyinelemede sınırın (`MAXRECURSION`) düşünülmesi gerekir; döngü içeren bir veri sonsuz özyineleme üretir. Pencere fonksiyonları (window functions), Oracle tarafında zaten kullandığın araçlardır ve SQL Server'da aynı biçimde çalışır. `ROW_NUMBER() OVER (PARTITION BY ... ORDER BY ...)` her grubun ilk kaydını seçmek için; `SUM(...) OVER (ORDER BY ... ROWS BETWEEN ...)` yürüyen toplam için kullanılır. Bunların değeri şudur: grup başına tek satır seçmek ya da yürüyen toplam almak, pencere fonksiyonu olmadan ya iç içe alt sorgularla ya da uygulama tarafında döngüyle yapılır; ikisi de daha pahalıdır. Mülakatta "grup başına en son kaydı nasıl alırsın" sorusuna `ROW_NUMBER` ile cevap vermek beklenen düzeydir. `tempdb` bu dosyanın birçok yerinde karşımıza çıktı ve bir yerde toplanmayı hak ediyor: sürüm deposu, geçici tablolar, büyük sıralama ve karma işlemlerinin belleğe sığmayan kısmı, hepsi oraya düşer. Yani `tempdb` sunucunun ortak kaynağıdır ve tek bir kötü sorgu ya da tek bir unutulmuş uzun işlem onu doldurabilir. Bunu bilen aday, "RCSI açalım" derken aynı cümlede "ve `tempdb`'yi izleyelim" der.

Son olarak saklı yordam (stored procedure) tartışması. Kurumsal .NET dünyasında iş mantığının önemli bir kısmı tarihsel olarak saklı yordamlarda yaşar. Savunulabilir tarafı vardır: plan yeniden kullanımı, ağ üzerinden az veri geçmesi, veri tabanına dar bir yüzeyden erişim izni verilebilmesi. Bedeli de gerçektir ve mülakatta bunu dengeli anlatmak gerekir: sürüm kontrolü ve kod incelemesi çoğu ekipte zayıf kalır, birim testi zordur, hata ayıklama sınırlıdır, aynı kural hem yordamda hem uygulamada iki kez yazılır ve zamanla ayrışır. İlan "teknik borcun azaltılması" diyorsa (`293`), doğru tavır ideolojik değildir: küme işlemleri ve toplu veri hareketleri veri tabanında kalır, iş kuralları uygulamaya taşınır, taşıma **davranış testiyle korunarak** kademeli yapılır. "Hepsini silelim" cümlesi bir ekip mülakatında iyi karşılanmaz; "önce sözleşmesini test altına alır, sonra taşırım" cümlesi karşılanır.

### Bu bölümden çıkan ders

Bu dosyanın beş sorusu tek bir cümlede toplanır: **veri tabanı bir kara kutu değildir, davranışı ölçülebilir ve seçilebilir.** İzolasyon seviyesi bir varsayılan değil, hangi anomaliyi kabul ettiğine dair bir karardır; SQL Server'ın kutudan çıkan hâli kilit tabanlıdır ve RCSI bunu tek satırla değiştirir, bedeli `tempdb`'dir. Kilitlenme bir hata değil, ters erişim sırasının belirtisidir; yeniden deneme emniyet ağıdır, çözüm değildir. Yürütme planında aranan şey maliyet yüzdesi değil, tahmin ile gerçeğin sapmasıdır. İndeks bir sihir değil, fiziksel bir yapıdır ve SQL Server'da kümeleme anahtarı bütün indekslerde tekrar eder. Sayfalama derinleştikçe `OFFSET` doğrusal olarak pahalanır; anahtar-kümesi sayfalama bunu sabitler, karşılığında sayfa numarasından vazgeçer. Bütün bunların üstünde tek bir duruş vardır ve mülakatta en çok o duruş konuşur: sürüm ve yapılandırma davranışı değiştirir, bu yüzden kesin sayı iddia etmek yerine ölçtüğünü söyleyen aday, ezberden konuşan adayın önüne geçer.

# mssql-1 | Orta
## Soru
Bir ekip, kilit beklemelerini bitirmek için bütün rapor sorgularına `WITH (NOLOCK)` eklemiş. Raporlar arasında günlük mutabakat ve bakiye dökümü de var. En yerinde itiraz hangisidir?
## A
`NOLOCK` yalnız biraz eski veri okutur; mutabakat raporunda birkaç saniyelik gecikme kabul edilebilir bir bedeldir
🤖 sorun bayatlık değil yanlışlıktır: geri alınacak veri okunabilir, satır atlanabilir.
## B
`NOLOCK` kesinleşmemiş veri okutur, satır atlayabilir ya da aynı satırı iki kez döndürebilir; doğru çözüm RCSI'yi açmaktır
🤖 Doğru: kilit beklemesi kalkar ama kirli okuma da olmaz.
## C
`NOLOCK` yerine izolasyon seviyesi `SERIALIZABLE` yapılmalıdır; en yüksek seviye tüm anomalileri kapatır
🤖 kilit süresini uzatır, iş hacmini düşürür; sorunu tersine büyütür.
## D
Sorun `NOLOCK` değil indekslerdir; her rapor sorgusuna kapsayan indeks eklenirse kilit beklemesi ortadan kalkar
🤖 indeks beklemeyi azaltabilir ama kilit tabanlı okuma davranışını değiştirmez.
## Doğru
B
## Açıklama
`NOLOCK` ipucu, o sorguyu `READ UNCOMMITTED` seviyesinde çalıştırır ve üç ayrı sorun üretir. Birincisi kirli okumadır (dirty read): geri alınacak bir yazmayı okursun, yani hiç var olmamış bir tutarı raporlarsın. İkincisi atlanan satırdır: okuma sırasında sayfa bölünmesi olursa bazı satırlar hiç görülmeyebilir. Üçüncüsü tekrarlanan satırdır: aynı satır sonuca iki kez düşebilir. Son iki madde önemlidir, çünkü "biraz eski veri okurum, olsun" savunmasını çürütür — ortaya çıkan şey bayat veri değil **yanlış** veridir ve mutabakatta bunun karşılığı doğrudan para farkıdır. Doğru çözüm veri tabanı seviyesinde `READ_COMMITTED_SNAPSHOT` ayarını (RCSI) açmaktır: okuma artık kilit beklemez, satırın ifade başındaki kesinleşmiş sürümünü okur. Kod değişmez, davranış değişir. Bedeli de söylenmelidir: eski satır sürümleri `tempdb`'de tutulur, uzun süren işlemler bu depoyu şişirir. C şıkkı seviyeyi yükselterek kilit süresini uzatır ve kilitlenme olasılığını artırır. D şıkkındaki kapsayan indeks gerçek bir iyileştirmedir ama kilit tabanlı okuma modelini değiştirmez (`283`, `146`).

# mssql-2 | İleri
## Soru
Bir rapor, tek bir işlem içinde art arda üç `SELECT` çalıştırıyor ve üçünün de aynı zaman görüntüsünü görmesi gerekiyor. Veri tabanında RCSI zaten açık. Doğru değerlendirme hangisidir?
## A
RCSI açık olduğu için üç sorgu zaten aynı görüntüyü görür; ek bir şey yapmaya gerek yoktur
🤖 RCSI tutarlılığı ifade düzeyindedir; her sorgu kendi başlangıcındaki görüntüyü okur.
## B
RCSI ifade düzeyinde tutarlılık verir; işlem boyunca tek görüntü için `SNAPSHOT` izolasyonu gerekir, bedeli güncelleme çatışması ve tempdb'dir
🤖 Doğru: ifade düzeyi ile işlem düzeyi ayrımı burada belirleyicidir.
## C
Üç sorgu tek bir sorguya birleştirilmelidir; başka türlü hiçbir seviye tutarlı görüntü veremez
🤖 birleştirme bir seçenek olabilir ama tek çözüm değildir; işlem düzeyi izolasyon tam da bunun içindir.
## D
İzolasyon `REPEATABLE READ` yapılmalıdır; okunan satırlar üzerindeki kilitler işlem sonuna kadar tutulunca görüntü sabitlenir
🤖 hayalet kaydı kapatmaz ve kilitleri uzatarak iş hacmini düşürür; anlık görüntü ile aynı şey değildir.
## Doğru
B
## Açıklama
Bu sorunun tamamı tek bir ayrımda düğümlenir: **ifade düzeyi mi, işlem düzeyi mi?** RCSI, `READ COMMITTED` seviyesinin davranışını değiştirir ve okumayı satır sürümlerinden yaptırır; ama verdiği tutarlılık her bir ifadenin başlangıcı içindir. Aynı işlemde art arda çalışan üç `SELECT`, üç ayrı zaman görüntüsü görebilir. İşlem boyunca sabit tek bir görüntü isteniyorsa gereken şey `SNAPSHOT` izolasyonudur; o, işlemin başındaki görüntüyü sonuna kadar sabit tutar. İki bedeli birlikte söylenmelidir. Birincisi güncelleme çatışmasıdır: `SNAPSHOT` içinde okuduğun bir satırı başkası değiştirip kesinleştirirse senin güncellemen çatışma hatasıyla düşer, yani yazma tarafı yeniden denemeye hazır olmalıdır (`130`). İkincisi `tempdb`'dir: eski sürümler orada tutulur, uzun işlem temizliği engeller ve sürüm deposu büyür. D şıkkındaki `REPEATABLE READ` farklı bir mekanizmadır; okunan satırların kilitlerini işlem sonuna kadar tutar, hayalet kaydı kapatmaz ve iş hacmini düşürür. Oracle'dan gelen bir aday için buradaki asıl ders şudur: Oracle'ın varsayılan davranışı bu ayrımı hiç sordurmaz, SQL Server'da ise seviye açık bir tercihtir (`227`, `224`).

# mssql-3 | Orta
## Soru
Bir sorgu planında kümelenmemiş indeks üzerinde arama (seek) var, ancak yanında binlerce kez tekrarlanan bir anahtar arama (key lookup) görünüyor. `SET STATISTICS IO` çıktısında mantıksal okuma sayısı çok yüksek. En doğru düzeltme hangisidir?
## A
Sorgunun döndürdüğü sütunlar indekse `INCLUDE` ile eklenerek kapsayan indeks kurulur; geri gidiş ortadan kalkar
🤖 Doğru: sorgu indeksten çıkmadan cevaplanır, mantıksal okuma düşer.
## B
İndeks kaldırılır; tarama (scan) tek geçişte okuduğu için anahtar aramadan her zaman ucuzdur
🤖 tarama az satır dönen sorguda çok daha pahalıdır; genelleme yanlıştır.
## C
Sorguya `NOLOCK` eklenir; kilit alınmayınca geri gidişin maliyeti de ortadan kalkar
🤖 kilit ile geri gidiş ilgisiz konulardır; okunan sayfa sayısı değişmez.
## D
Kümelenmiş indeks bu sütunlar üzerine yeniden kurulur; tabloda ikinci bir kümelenmiş indeks açılır
🤖 bir tabloda yalnız bir kümelenmiş indeks olabilir; ayrıca fiziksel düzeni değiştirmek pahalıdır.
## Doğru
A
## Açıklama
Anahtar arama, kümelenmemiş indeksin sorgunun istediği bir sütunu taşımadığı durumda satırın kendisine geri gidilmesidir. Tek bir satır için ucuzdur; binlerce satır için satır başına tekrarlandığı için pahalıdır ve mantıksal okuma sayısını hızla büyütür. Çare kapsayan indekstir: sorgunun döndürdüğü sütunlar `INCLUDE` ile indeksin yaprağına eklenir, böylece sorgu indeksten hiç çıkmadan cevaplanır. Burada iki ayrımı bilmek gerekir. Anahtar sütunları sıralamayı ve aramayı belirler; `INCLUDE` sütunları yalnız yaprakta taşınır ve arama koşuluna katılmaz. Bedeli de söylenmelidir: indeks büyür, her yazma işlemi onu da günceller ve `INCLUDE` listesini kontrolsüz büyütmek yazma tarafında ölçülebilir bir maliyet üretir. B şıkkı taramayı genel olarak ucuz sayar; az satır dönen bir sorguda tarama çok daha pahalıdır. C şıkkı ilgisiz bir konuyu karıştırır; kilit davranışı okunan sayfa sayısını değiştirmez. D şıkkı SQL Server'ın temel kuralını ihlal eder: kümelenmiş indeks tablonun kendisidir, bir tabloda yalnız bir tane olabilir (`283`, `146`).

# mssql-4 | İleri
## Soru
Bir liste ekranı `OFFSET @sayfa * 20 ROWS FETCH NEXT 20 ROWS ONLY` ile sayfalıyor. İlk sayfalar hızlı, beş yüzüncü sayfa çok yavaş. Ayrıca kullanıcılar bazı kayıtların hiç görünmediğinden şikâyetçi. Doğru teşhis ve çözüm hangisidir?
## A
`OFFSET` atılan satırları da okur, maliyet derinlikle doğrusal büyür; sıralama kararsızsa kayıt atlanır — anahtar-kümesi sayfalama ve tekil ikinci sıralama sütunu gerekir
🤖 Doğru: biri performans, diğeri doğruluk sorunu; ikisinin de kaynağı aynı sorgudur.
## B
İlk sayfalar önbelleğe alınmalıdır; önbellek ısındıktan sonra derin sayfalar da hızlanır
🤖 derin sayfalar seyrek istenir ve önbellekte olmaz; pahalı sorgu önbellekle ucuzlamaz.
## C
Sayfa boyutu yirmiden beşe düşürülmelidir; daha az satır döndükçe derin sayfa maliyeti de azalır
🤖 maliyet atlanan satır sayısından gelir; sayfa boyutu bunu değiştirmez.
## D
Sorgu `NOLOCK` ile çalıştırılmalıdır; kilit beklemesi kalkınca hem derin sayfa hızlanır hem eksik kayıt sorunu biter
🤖 kirli okuma eksik kayıt sorununu çözmez, üstüne yenilerini ekler.
## Doğru
A
## Açıklama
Soruda iki ayrı sorun aynı sorgudan çıkıyor. Birincisi performanstır: `OFFSET`, atlanacak satırları da üretip atmak zorundadır. Beş yüzüncü sayfada on bin satır okunur, sıralanır ve çöpe atılır; maliyet sayfa derinliğiyle doğrusal büyür. İkincisi doğruluktur: sıralama sütunu tekil değilse eşit değerli satırların birbirine göre sırası çağrılar arasında değişebilir, bu yüzden bir kayıt iki sayfada görünürken başka bir kayıt hiç görünmez. Çözüm ikisini birden kapatır. Sıralamaya tekil bir sütun ikinci ölçüt olarak eklenir ve sıralama kararlı hâle gelir. Sayfa numarası yerine son görülen satırın anahtarı taşınır; sonraki sayfa "bu anahtardan sonraki ilk yirmi satır" olarak istenir. Uygun indeks varsa maliyet sayfa derinliğinden bağımsız hâle gelir. Bedeli dürüstçe söylenmelidir: "37. sayfaya git" özelliği kalkar; bu yöntem ileri geri gezinmeye ve sonsuz kaydırmaya uygundur, keyfi sayfa atlamaya değil. Ekrandaki toplam sayı sorgusu da ayrı bir maliyettir ve çoğu zaman yaklaşık bir gösterge yeterlidir. B şıkkındaki önbellek toplam yükü azaltabilir ama pahalı sorguyu ucuzlatmaz (`144`, `152`).

# mssql-5 | Uzman
## Soru
Gün sonu işinde iki servis aynı anda çalışırken sık sık kilitlenme (deadlock) hatası alınıyor. Ekip, hatayı yakalayıp işlemi işlem bloğunun içinde yeniden denemeyi öneriyor. En yerinde itiraz hangisidir?
## A
Yeniden deneme sayısı artırılmalıdır; yeterince deneme yapılırsa iki işlemden biri er geç geçer
🤖 belirtiyi susturur, çevrimi üreten erişim sırasını değiştirmez.
## B
Kurban seçilen işlem geri alınmıştır; yeniden deneme işlem sınırının dışında, tüm iş birimini kapsayacak biçimde yapılmalı ve asıl çözüm tutarlı erişim sırası olmalıdır
🤖 Doğru: geri alınmış bir işlemin ortasından devam edilemez; kök sebep erişim sırasıdır.
## C
İzolasyon seviyesi RCSI yapılmalıdır; anlık görüntüye geçince kilitlenme tamamen ortadan kalkar
🤖 RCSI okuma-yazma çakışmasını azaltır, yazma-yazma kilitlenmesini bitirmez.
## D
Kilitlenme zaman aşımı süresi uzatılmalıdır; motora daha çok süre verilirse çevrim kendiliğinden çözülür
🤖 çevrim bekleyerek çözülmez; yalnız hatanın gelmesi gecikir.
## Doğru
B
## Açıklama
Kilitlenme, iki işlemin iki kaynağı ters sırada kilitlemesiyle oluşan bir bekleme çevrimidir. Motor çevrimi görür, geri alması daha ucuz olanı kurban seçer ve o işlemi **tamamen geri alır**. Buradaki kritik ayrım şudur: geri alınmış bir işlemin ortasından devam edilemez, dolayısıyla yeniden deneme mantığı işlem sınırının dışında durmalı ve tüm iş birimini baştan çalıştırmalıdır. Bu da bir zorunluluk getirir: yeniden çalıştırılan iş eşgüçlü (idempotent) olmalıdır, yoksa gün sonunda çift kayıt üretilir ve mutabakat farkı olarak geri döner (`130`). Ama yeniden deneme bir emniyet ağıdır, çözüm değildir. Kalıcı çözüm üç yerden gelir. Birincisi tutarlı erişim sırasıdır: bütün kod yolları kaynaklara aynı sırada dokunur ve bu bir kod incelemesi kuralı hâline getirilir. İkincisi işlemi kısaltmaktır; dış servis çağrısı ve kullanıcı beklemesi işlem dışına alınır. Üçüncüsü çoğu adayın atladığı maddedir: eksik indeks kilitlenme üretir, çünkü uygun indeks yokken güncelleme gereğinden fazla satır kilitler ve çakışma alanı büyür. C şıkkı yaygın bir yanılgıdır; RCSI okuma ile yazma arasındaki engellemeyi büyük ölçüde kaldırır ama iki yazıcının çapraz beklemesini etkilemez. D şıkkı yalnız hatanın gelme anını geciktirir.

# mssql-6 | Uzman
## Soru
Bir raporun gerçek yürütme planında, bir düğümde tahmin edilen satır sayısı 120, gerçekleşen satır sayısı 2 400 000 görünüyor; planda küçük veri için uygun bir birleştirme (join) yöntemi seçilmiş. Sorgunun `WHERE` koşulunda `CONVERT(date, HareketZamani) = @gun` var. En olası açıklama ve ilk adım hangisidir?
## A
Sütuna uygulanan dönüşüm indeksi ve iyi bir tahmini engelliyor; koşul aralık biçimine çevrilip tahmin ile gerçek yeniden karşılaştırılır
🤖 Doğru: sarılmış sütun hem aramayı hem istatistik kullanımını bozar.
## B
Kalan tahmini sapması normaldir; plan zaten çalıştığı için değiştirilmemeli, yalnız sunucuya bellek eklenmelidir
🤖 bu ölçekte sapma normal değildir; plan yanlış varsayımla kurulmuştur.
## C
Sorun parçalanmış indekstir; indeks yeniden kurulunca tahmin ile gerçek arasındaki fark kapanır
🤖 parçalanma okuma maliyetini etkiler, tahmin sapmasını bu ölçekte açıklamaz.
## D
Sorgu `SNAPSHOT` izolasyonunda çalıştırılmalıdır; sürüm okuması sayesinde iyileştirici daha doğru tahmin üretir
🤖 izolasyon seviyesi tahmin üretimini değiştirmez; ilgisiz bir kaldıraçtır.
## Doğru
A
## Açıklama
Gerçek planın en değerli bilgisi her düğümde tahmin edilen ve gerçekleşen satır sayısının yan yana durmasıdır. Yüz yirmiye karşı iki buçuk milyon gibi bir sapma tesadüf değildir: iyileştirici (optimizer) küçük bir sonuç kümesi varsaymış ve ona göre bir birleştirme yöntemi ile bellek ayırması seçmiştir; gerçek hacim gelince o seçim felakete dönüşür. Sapmanın en yaygın sebeplerinden biri sütunun bir fonksiyon ya da dönüşümle sarılmasıdır. `CONVERT(date, HareketZamani) = @gun` yazıldığında motor sütunun kendisine ait istatistikleri doğrudan kullanamaz ve indeks üzerinde arama (seek) yapamaz; sonuçta hem tahmin bozulur hem tarama başlar. Doğru yazım aralık koşuludur: sütun çıplak bırakılır, karşılaştırma `>= @gunBaslangic AND < @gunSonu` biçiminde kurulur. Aynı hata örtük tip dönüşümünde de olur; sütun ile parametrenin tipi farklıysa motor sütunu dönüştürür ve aynı sonuç doğar. İlk adım her zaman aynıdır: koşulu düzelt, sorguyu yeniden çalıştır, `SET STATISTICS IO` çıktısındaki mantıksal okuma sayısını ve plandaki tahmin-gerçek farkını yeniden karşılaştır. Sapma hâlâ büyükse sıradaki şüpheliler bayat istatistikler, çarpık veri dağılımı ve parametre koklamasıdır (`283`). B şıkkı sapmayı normalleştirir, C şıkkı farklı bir sorunun çaresini önerir, D şıkkı ilgisiz bir kaldıraca sarılır. Ölçüm olmadan yapılan her düzeltme bir tahmindir (`152`, `146`).
