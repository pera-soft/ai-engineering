---
slug: oracle-plsql-temelleri
baslik: "Bankada Oracle: Bind Değişken, Yürütme Planı, Sequence, Sayfalama ve PL/SQL"
kademe: İleri
sira: 227
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kamu kurumuna dilekçe vereceksin. İki yol var: her başvuran için sıfırdan, elle bir dilekçe yazmak; ya da boşlukları doldurulan **matbu bir form** kullanmak. Elle yazılan her dilekçeyi memur baştan okumak, anlamak ve nasıl işleyeceğine karar vermek zorundadır. Matbu formu ise bir kez öğrenir, sonra yalnız boşluklara bakar. Oracle'ın SQL'e bakışı tam olarak budur. Gönderdiğin SQL metni her seferinde farklıysa, veritabanı onu her seferinde baştan ayrıştırıp plan üretir. Metin sabit kalıp yalnız değerler değişiyorsa planı bir kez üretir ve sonra tekrar kullanır. Türk bankalarında çekirdek sistemler büyük ölçüde Oracle üzerinde koşar. Bu yüzden Java tarafındaki geliştiricinin bu "form" mantığını bilmesi beklenir.

Oracle'ın bankalarda hâlâ ayakta olmasının sebebi nostalji değildir. Sebepler şunlardır: yıllar içinde biriken PL/SQL paketleri, gün sonu (EOD) toplu işleri, denetim ve mevzuat gereksinimleri, olgun yüksek erişilebilirlik ile felaket kurtarma araçları. Bir de göç etmenin riski, beklenen kazançtan büyük görülür. Mülakatta "Oracle eski, PostgreSQL'e geçilmeli" demek puan getirmez; **Oracle'ın davranışını bilmek** getirir.

### Temel kavramlar

- **Bind değişken (bind variable)**: SQL metnine değeri gömmek yerine yer tutucu koymak (`WHERE musteri_no = :no`). Değer ayrı gönderilir.
- **Sert ayrıştırma (hard parse)**: Oracle'ın daha önce görmediği bir SQL metni için sözdizimi çözümlemesi + plan üretimi yapması. Pahalıdır.
- **Yumuşak ayrıştırma (soft parse)**: Metin daha önce görülmüşse hazır planın yeniden kullanılması. Ucuzdur.
- **Paylaşımlı havuz (shared pool)**: Ayrıştırılmış SQL metinlerinin ve planlarının tutulduğu bellek alanı.
- **Yürütme planı (execution plan)**: İyileştiricinin (optimizer; maliyet tabanlı iyileştirici, CBO) sorguyu nasıl çalıştıracağına dair kararıdır. Üç şeyi belirler: hangi erişim yolu, hangi birleştirme yöntemi, hangi sıra.
- **İstatistikler (statistics)**: Tablo/indeks büyüklüğü, farklı değer sayısı, veri dağılımı bilgisi. Optimizer kararlarını buna dayandırır.
- **Tam tablo taraması (full table scan)**: Tablonun tamamını okumak. Her zaman kötü değildir.
- **İndeks aralık taraması (index range scan)**: İndeks üzerinden bir değer aralığına erişip oradan tablo satırlarına gitmek.
- **Nested loop / hash join**: İki tabloyu birleştirmenin iki temel yöntemi.
- **Sequence**: Sıralı sayı üreten veritabanı nesnesi; kimlik (id) üretiminin Oracle'daki standart yolu.
- **Örtük tip dönüşümü (implicit conversion)**: Farklı tiplerin karşılaştırılmasında Oracle'ın sessizce yaptığı dönüşüm; indeksleri devre dışı bırakabilir.
- **Bölümleme (partitioning)**: Tek mantıksal tabloyu fiziksel parçalara (partition) ayırmak.
- **Bölüm budama (partition pruning)**: Sorgu koşuluna göre ilgisiz bölümlerin hiç okunmaması.
- **PL/SQL**: Oracle'ın yordamsal dili; saklı yordam (stored procedure), fonksiyon ve **paket** (package) yazmak için kullanılır.

### Nasıl çalışır?

**1) Bind değişken: iki bedeli olan tek bir hata.** Kredi başvurusu sorgusunu `"... WHERE musteri_no = " + no` diye kurarsan iki ayrı bedel ödersin. Birincisi **SQL enjeksiyonu**: değerin içine SQL parçası koyan biri sorgunun anlamını değiştirir. İkincisi **performanstır**. Her farklı müşteri numarası Oracle için **yeni bir SQL metnidir**. Dolayısıyla her çağrıda **sert ayrıştırma** yapılır ve paylaşımlı havuza yeni bir giriş eklenir. Havuz şişer, yararlı planlar havuzdan atılır. Ayrıştırma sırasında alınan içsel kilitler (latch/mutex) etrafında çekişme başlar. Sonuçta sistem "veri az ama işlemci yanıyor" hâline gelir. Bind değişken kullanınca metin tek olur: bir kez sert ayrıştırılır, sonraki milyon çağrı yumuşak ayrıştırmadır.

Java tarafında bunu sen elle yapmazsın. `PreparedStatement` içindeki `?` yer tutucularını JDBC sürücüsü bind değişkene çevirir. JPA/Hibernate de sorgularını `PreparedStatement` üzerinden çalıştırdığı için varsayılan olarak bind eder. İşin bozulduğu yer, JPQL ya da SQL metnini kendin dizge birleştirerek kurduğun satırlardır.

Eski ve düzeltilemeyen uygulamalar için Oracle'da `cursor_sharing` ayarı vardır. Varsayılan değer `EXACT` metni olduğu gibi kullanır. `FORCE` ise metindeki sabit değerleri sistem bind değişkenlerine çevirir. Ama bu bir **yara bandıdır** ve plan kalitesini bozabilir; doğru çözüm kodu düzeltmektir.

```svg
<svg viewBox="0 0 660 246" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dizge birleştirmeli SQL her seferinde sert ayrıştırma yapar, bind değişken planı yeniden kullanır">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dizge birleştirme mi, bind değişken mi?</text>
  <rect x="20" y="40" width="300" height="168" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="170" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">"... WHERE no = " + no</text>
  <text x="170" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">no=1 → ayrı SQL metni</text>
  <text x="170" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">no=2 → ayrı SQL metni</text>
  <text x="170" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her çağrı → SERT AYRIŞTIRMA</text>
  <text x="170" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">paylaşımlı havuz şişer, planlar atılır</text>
  <text x="170" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">+ SQL ENJEKSİYONU açık</text>
  <text x="170" y="192" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işlemci ayrıştırmada tükenir</text>
  <rect x="340" y="40" width="300" height="168" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">"... WHERE no = :no"</text>
  <text x="490" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">metin TEK, değer ayrı gider</text>
  <text x="490" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1. çağrı → sert ayrıştırma</text>
  <text x="490" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sonraki milyon → yumuşak</text>
  <text x="490" y="146" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">havuz sabit, plan yeniden kullanılır</text>
  <text x="490" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">enjeksiyon kapalı</text>
  <text x="490" y="192" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">PreparedStatement / JPA bunu verir</text>
  <text x="330" y="234" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">cursor_sharing=FORCE eski kodda yara bandıdır; çözüm kodu düzeltmektir</text>
</svg>
```

**2) Yürütme planını okumak.** Önce `EXPLAIN PLAN FOR <sorgu>;` yazarsın, ardından `SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);` çalıştırırsın. Bu sana iyileştiricinin **tahmini** planını verir. Dikkat: bu gerçekten çalışmış plan değildir. Gerçek planı ve **gerçek satır sayılarını** görmek için sorguyu çalıştırıp `DBMS_XPLAN.DISPLAY_CURSOR` ile bakarsın. En değerli sinyal, satırlardaki **tahmini satır sayısı ile gerçek satır sayısı arasındaki uçurumdur**. İyileştirici 10 satır sanmış ama 2 milyon satır geliyorsa, plan da yanlıştır.

Planda `TABLE ACCESS FULL` görmek kendiliğinden hata demek değildir. Tablonun büyük bir bölümü zaten dönecekse tam tarama daha ucuzdur; çünkü diskten **ardışık büyük bloklar** hâlinde okunur. İndeksten satır satır tabloya gidip gelmek ise daha pahalıdır.

Kabaca kural şudur. Az sayıda satır seçiyorsan indeks aralık taraması + **nested loop** doğrudur. Çok sayıda satır seçiyorsan tam tarama + **hash join** doğrudur. `nested loop` küçük bir sürücü kümesiyle çalışır ve iç tarafta indeks ister. `hash join` ise büyük kümeleri eşitlik koşuluyla birleştirir: belleğe bir karma tablo kurar ve işi tek geçişte bitirir.

```svg
<svg viewBox="0 0 660 258" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Az satır için indeks ve nested loop, çok satır için tam tarama ve hash join uygundur">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Erişim yolu ve birleştirme: doğrusu duruma bağlı</text>
  <rect x="20" y="40" width="300" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="170" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">AZ satır dönüyor (seçici)</text>
  <text x="170" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">INDEX RANGE SCAN</text>
  <text x="170" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">+ NESTED LOOP</text>
  <text x="170" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">küçük sürücü küme, iç tarafta indeks</text>
  <text x="170" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">örnek: tek müşterinin başvuruları</text>
  <text x="170" y="178" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">satır satır erişim ucuzdur</text>
  <rect x="340" y="40" width="300" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ÇOK satır dönüyor</text>
  <text x="490" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">TABLE ACCESS FULL</text>
  <text x="490" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">+ HASH JOIN</text>
  <text x="490" y="134" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">büyük kümeler, eşitlik koşulu</text>
  <text x="490" y="158" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">örnek: gün sonu tahakkuk taraması</text>
  <text x="490" y="178" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ardışık büyük okuma ucuzdur</text>
  <text x="330" y="216" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Önce tahmini satır sayısı ile gerçeği karşılaştır (DBMS_XPLAN)</text>
  <text x="330" y="238" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Bayat istatistik → yanlış tahmin → yanlış plan. Hint (ipucu) son çaredir.</text>
</svg>
```

**3) İstatistikler ve plan kararlılığı.** Optimizer kararlarını **istatistiklere** dayandırır. Tabloya bir gecede 50 milyon satır girdiğini ve istatistiklerin eski kaldığını düşün. İyileştirici tabloyu hâlâ küçük sanır, nested loop seçer ve sorgu saatlerce sürer. Oracle otomatik istatistik toplama işi çalıştırır; ama yoğun veri yüklemesinden sonra istatistikleri elle toplamak gerekebilir.

Aynı sorgunun planı bir gece değişip üretimi durdurmasın diye **plan kararlılığı** araçları vardır. SQL Plan Baseline ile bilinen iyi plan sabitlenir. **İpucu (hint)** yazmak da mümkündür, ama son çaredir. İpucu planı dondurur; veri dağılımı değişince yanlış plan kalıcı hâle gelir. Üstelik yıllar sonra kimse o ipucunun neden konduğunu hatırlamaz.

**4) Sequence ve JPA.** Oracle'da kimlik üretmenin doğru yolu `sequence`'tir. JPA tarafında `@GeneratedValue(strategy = GenerationType.SEQUENCE)` kullanılır. Burada klasik bir uyumsuzluk vardır. Hibernate'in varsayılan `allocationSize` değeri **50**'dir. Yani her sequence çağrısını "bana 50'lik bir kimlik aralığı verildi" diye yorumlar ve o aralıktaki kimlikleri veritabanına hiç sormadan kendisi üretir.

Veritabanındaki sequence ise `INCREMENT BY 1` ile tanımlıysa iş karışır: aynı sequence'ten okuyan başka bir oturum ya da uygulama **çakışan aralıklar** alır ve benzersizlik ihlali (duplicate key) hataları başlar. Doğru kurulum ikisini eşitlemektir. Ya sequence `INCREMENT BY 50` olacak, ya da `allocationSize = 1` yazılacak; ikincisinin bedeli her eklemede bir gidiş-dönüştür. Kimliklerde boşluk (gap) oluşması normaldir ve sorun değildir; kimlik bir sayaç değildir.

`GenerationType.IDENTITY` ise Oracle'da mümkün olsa bile **toplu eklemeyi (batch insert) bozar**. Sebebi şudur: Hibernate her satırın ürettiği kimliği ekleme anında geri almak zorundadır, bu yüzden JDBC yığın (batch) gönderimini kapatır. Gün sonu işinde 2 milyon satır yazacaksan bu bir felakettir. `SEQUENCE` ile hem kimlikler önceden alınır hem de yığın gönderim çalışmaya devam eder.

**5) İndeksin sessizce devre dışı kalması.** Oracle'a özgü üç tuzak:

- **Sütuna fonksiyon uygulamak.** `WHERE UPPER(musteri_ad) = 'ALI'` sorgusu `musteri_ad` üzerindeki normal indeksi kullanamaz; çünkü indekste `UPPER(...)` sonucu değil, sütunun ham değeri saklıdır. Çözüm ya sorguyu sütunu sarmayacak biçimde yazmak ya da `UPPER(musteri_ad)` üzerine **fonksiyon tabanlı indeks** (function-based index) açmaktır.
- **Örtük tip dönüşümü.** Sütun `VARCHAR2` ama sorguda onu bir sayı ile karşılaştırıyorsan (`WHERE hesap_no = 12345`), Oracle **sütunu** sayıya çevirir. Bu da sütuna fonksiyon uygulamakla aynı şeydir; indeks kullanılamaz. Aynı tuzak tarihlerde `NLS_DATE_FORMAT`'a bel bağlarken de karşına çıkar. Tarih karşılaştırmasını oturum ayarına bırakma; `TO_DATE(..., 'YYYY-MM-DD')` ile açıkça yaz. Aksi hâlde aynı kod farklı oturum ayarında farklı sonuç verebilir.
- **NULL değerler.** Oracle'ın klasik B-tree indeksi, anahtarın tamamı `NULL` olan satırları **indekse hiç yazmaz**. Bu yüzden tek sütunlu bir indeks `WHERE kolon IS NULL` sorgusunda kullanılamaz. Birleşik (composite) indekste ise sütunlardan en az biri doluysa satır indekste yer alır. Birleşik indekste **sütun sırası** da kritiktir. `(musteri_no, tarih)` indeksi yalnız `tarih` ile filtreleyen bir sorguya verimli hizmet etmez; çünkü indeks önce ilk sütuna göre sıralıdır.

**6) Sayfalama.** Oracle'da `ROWNUM` mülakatın favori tuzağıdır. `ROWNUM`, satırlar sonuç kümesine **alındığı anda** atanır; `ORDER BY` ise **daha sonra** uygulanır. Yani `SELECT ... FROM basvuru WHERE ROWNUM <= 20 ORDER BY basvuru_tarihi DESC` sorgusu "en yeni 20 başvuru" anlamına gelmez. Anlamı şudur: "gelişigüzel gelen 20 başvurunun tarihe göre sıralanmış hâli". Doğru yol, sıralamayı bir alt sorguya (inline view) alıp `ROWNUM`'u dışarıda uygulamaktır. Oracle 12c'den beri standart sözdizimi de vardır ve tercih edilir: `ORDER BY basvuru_tarihi DESC OFFSET 40 ROWS FETCH FIRST 20 ROWS ONLY`.

Ama `OFFSET` derin sayfalamada pahalıdır. 200.000. sayfaya gitmek için veritabanı önceki tüm satırları **üretip atmak** zorundadır. Sayfa numarası büyüdükçe sorgu yavaşlar.

Yüksek hacimli listelerde **anahtar-kümesi sayfalama** (keyset pagination) kullanılır. Mantığı şudur: "şu tarihten ve şu kimlikten sonrakileri getir" (`WHERE (basvuru_tarihi, id) < (:son_tarih, :son_id) ORDER BY basvuru_tarihi DESC, id DESC FETCH FIRST 20 ROWS ONLY`). Böylece her sayfa aynı maliyette olur. Bedeli ise rastgele bir sayfa numarasına atlayamamandır.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ROWNUM sıralamadan önce atanır, OFFSET derin sayfada pahalıdır, keyset sabit maliyetlidir">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sayfalama: üç yol, üç sonuç</text>
  <rect x="18" y="40" width="200" height="150" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="118" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">WHERE ROWNUM &lt;= 20</text>
  <text x="118" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ORDER BY ile birlikte</text>
  <text x="118" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ROWNUM önce atanır</text>
  <text x="118" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sıralama SONRA yapılır</text>
  <text x="118" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">YANLIŞ SONUÇ</text>
  <text x="118" y="174" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sıralamayı alt sorguya al</text>
  <rect x="230" y="40" width="200" height="150" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">OFFSET … FETCH FIRST</text>
  <text x="330" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">12c ve sonrası, standart</text>
  <text x="330" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sonuç DOĞRU</text>
  <text x="330" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">atlanan satırlar üretilip atılır</text>
  <text x="330" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">derin sayfa = pahalı</text>
  <text x="330" y="174" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ilk sayfalar için ideal</text>
  <rect x="442" y="40" width="200" height="150" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="542" y="63" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Anahtar-kümesi (keyset)</text>
  <text x="542" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"son gördüğümden sonrası"</text>
  <text x="542" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">indeksten doğrudan gider</text>
  <text x="542" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her sayfa AYNI maliyet</text>
  <text x="542" y="150" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yüksek hacim için doğru</text>
  <text x="542" y="174" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bedeli: sayfaya atlayamazsın</text>
  <text x="330" y="224" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Sayfalama sıralaması benzersiz olmalı; eşit tarihli satırlar için kimlik de sıraya eklenir</text>
</svg>
```

**7) PL/SQL: iş mantığı nerede dursun?** PL/SQL Oracle'ın yordamsal dilidir; kod **paket** (package) içinde toplanır, paket bir başlık (spec) ve gövdeden (body) oluşur. Java'dan çağırmanın yolları: Spring JDBC ile `SimpleJdbcCall`, Spring Data JPA ile `@Procedure`, saf JPA ile `StoredProcedureQuery`. Bankada iki tarafın da gerçek gerekçeleri vardır:

- **Veritabanında dursun** diyenlerin gerekçesi: milyonlarca satırı güncelleyen küme tabanlı (set-based) bir iş, veriyi ağ üzerinden uygulamaya taşımadan **veriye yakın** çalışır. Gidiş-dönüş sayısı ve aktarılan veri hacmi çok azalır. Gün sonu tahakkuk, mutabakat ve toplu limit güncelleme işlerinde bu fark saatlerle ölçülür.
- **Uygulamada dursun** diyenlerin gerekçesi: Java koduna birim testi yazılabilir, kod git'te sürümlenir ve sürekli tümleştirme (CI) hattından geçer. Ayrıca taşınabilirdir ve geliştirici araçları olgundur. PL/SQL ise çoğu kurumda veritabanına elle uygulanır; kimin ne zaman değiştirdiği izlenemez hâle gelir.

Mülakatta doğru cevap taraf tutmak değil, **ayrımı yapabilmektir**. Veri yoğun küme işlemleri PL/SQL'de kalabilir. Karar kuralları, doğrulama ve iş akışı ise Java'da durmalıdır. Ayrıca PL/SQL de git'te sürümlenmeli ve otomatik testten geçirilmelidir; "veritabanı kodu test edilmez" demek elenme sebebidir.

**8) Bölümleme ve arşivleme.** Hareket tablosu yıllarca büyüyünce tek parça tutmak zorlaşır. **Bölümleme** (partitioning) ile tablo örneğin aya göre (RANGE) parçalara ayrılır. Sorguda tarih koşulu varsa Oracle yalnız ilgili bölümleri okur — buna **bölüm budama** (partition pruning) denir ve okunan veri hacmini büyük ölçüde düşürür. Arşivleme de kolaylaşır. Eski verileri milyonlarca `DELETE` ile silmek yerine ilgili bölümü `DROP PARTITION` ile bir anda düşürürsün. Ya da `EXCHANGE PARTITION` ile arşiv tablosuna geçirirsin. Uyarı: bölümleme, Oracle'da ayrıca lisanslanan bir Enterprise Edition seçeneğidir. "Önce lisans var mı?" sorusu teknik sorudan önce gelir.

**9) Okuyucu-yazar davranışı.** Oracle çok sürümlü tutarlılık (multiversion read consistency) kullanır. Kural şudur: **okuyucular yazarları bekletmez, yazarlar da okuyucuları bekletmez.** Bir satır güncellenirken onu okuyan sorgu, sorgunun başladığı andaki tutarlı görüntüyü **undo** alanından okur. Beklemez ve yarım veri görmez. Bekleme yalnız **yazar-yazar** çakışmasında olur: aynı satırı iki oturum güncellerse ikincisi bekler.

Bunun iki sonucu vardır. (a) "SELECT yüzünden kilitlendi" açıklaması Oracle'da doğru değildir. (b) Çok uzun süren bir sorgu, ihtiyaç duyduğu eski görüntüyü undo alanında bulamazsa `ORA-01555 snapshot too old` hatası alabilir. Bu bir kilitlenme değil, geçmişe erişememe hatasıdır.

### Mülakatta ne sorulur / tuzaklar

- **"Bind değişken neden önemli?"** → İki bedeli vardır. Birincisi SQL enjeksiyonu riski. İkincisi her farklı metin için sert ayrıştırma ve paylaşımlı havuzun şişmesi. `PreparedStatement` ve JPA bunu varsayılan olarak verir.
- **"Planda TABLE ACCESS FULL var, ne yaparsın?"** → Önce dönen satır oranına bakarım. Tablonun büyük kısmı dönüyorsa tam tarama zaten doğrudur. Sonra tahmini ve gerçek satır sayılarını karşılaştırırım. İpucu (hint) yazmak son çaredir.
- **"Aynı sorgu dün hızlıydı bugün yavaş, neden?"** → En olası sebep **bayat istatistikler** yüzünden planın değişmesidir; plan kararlılığı (baseline) ile bilinen iyi plan sabitlenebilir.
- **"JPA ile Oracle sequence kullanırken nelere dikkat edersin?"** → `allocationSize` ile sequence'in `INCREMENT BY` değeri **eşit** olmalı; eşit değilse kimlik çakışması olur. `IDENTITY` toplu eklemeyi bozduğu için gün sonu işlerinde kullanılmaz.
- **"İndeks var ama kullanılmıyor."** → Dört ihtimal var. Sütuna fonksiyon uygulanmış olabilir; o zaman fonksiyon tabanlı indeks gerekir. Örtük tip dönüşümü olabilir. `IS NULL` sorgulanıyor olabilir. Ya da birleşik indekste sütun sırası uygun değildir.
- **"ROWNUM ile ilk 20 kaydı nasıl alırsın?"** → `ROWNUM` sıralamadan **önce** atandığı için doğrudan yazılamaz; sıralama alt sorguya alınır veya `OFFSET ... FETCH FIRST` kullanılır.
- **"Sayfa 5000'e gidince yavaşlıyor."** → `OFFSET` atlanan satırları yine de üretip atar; anahtar-kümesi (keyset) sayfalamaya geçilir.
- **"İş mantığı PL/SQL'de mi Java'da mı?"** → Küme tabanlı ağır veri işleri veriye yakın kalabilir; kurallar ve akış test edilebilirlik ve sürümleme için Java'da durur. PL/SQL de sürümlenip test edilmelidir.
- **"Oracle'da SELECT satırı kilitler mi?"** → Hayır. Okuyucular yazarları, yazarlar okuyucuları bloklamaz; bloklama yazar-yazar çakışmasındadır.

### Tek cümlede

Oracle'da iyi bir Java geliştiricisi değeri SQL metnine gömmez, bind değişken kullanır. Planın **neden** o plan olduğunu istatistiklerden okur. Kimliği sequence ile ve `allocationSize` uyumlu üretir. Sayfalamayı `ROWNUM` tuzağına düşmeden kurar. İş mantığını da PL/SQL ile Java arasında körü körüne değil, ölçerek paylaştırır.

# ora-1 | Orta
## Soru
Kredi başvurusu sorgusunu Java tarafında `"SELECT ... WHERE musteri_no = " + no` biçiminde dizge birleştirerek kuruyorsun. Bunun Oracle üzerindeki iki ayrı bedeli nedir?
## A
Yalnız performans bedeli vardır; Oracle birleştirilmiş SQL metnini de otomatik olarak bind değişkene çevirdiği için enjeksiyon riski oluşmaz
🤖 Oracle varsayılan ayarda metni bind'e çevirmez ve çevirse bile enjeksiyonu engellemez.
## B
SQL enjeksiyonu riski doğar; ayrıca her farklı değer yeni bir SQL metni ürettiği için her çağrıda sert ayrıştırma yapılır ve paylaşımlı havuz şişer
🤖 Doğru: birleştirmenin biri güvenlik biri performans olmak üzere iki ayrı bedeli vardır.
## C
Yalnız güvenlik bedeli vardır; ayrıştırma maliyeti değişmez çünkü Oracle planı SQL metnine değil sorgulanan tabloya göre önbelleğe alır
🤖 plan SQL metnine göre paylaşılır, metin değişince yeniden ayrıştırma gerekir.
## D
Hiçbir bedeli yoktur; `PreparedStatement` kullanılsa bile Oracle her çalıştırmada sert ayrıştırma yaptığı için iki yöntem arasında fark oluşmaz
🤖 `PreparedStatement` bind ürettiği için ilk çağrıdan sonrakiler yumuşak ayrıştırmadır.
## Doğru
B
## Açıklama
Değeri SQL metnine gömmenin **iki ayrı** bedeli vardır. Birincisi **SQL enjeksiyonu**: değerin içine SQL parçası koyan biri sorgunun anlamını değiştirebilir. İkincisi **ayrıştırma maliyeti**: Oracle planları **SQL metnine** göre paylaşımlı havuzda (shared pool) tutar; her farklı müşteri numarası farklı bir metin ürettiği için her çağrı **sert ayrıştırma** (hard parse) olur, havuz binlerce tek kullanımlık girişle şişer, yararlı planlar dışarı atılır ve ayrıştırma sırasındaki içsel kilitler etrafında çekişme başlar. Bind değişkenle metin tek kalır: bir sert ayrıştırma, sonrası yumuşak ayrıştırma (A ve C birer bedeli görmezden gelir; D ise `PreparedStatement`'ın bind ürettiğini yok sayar). Java tarafında `PreparedStatement` ve JPA bunu varsayılan olarak sağlar; bozulduğu yer metni elle birleştirdiğin satırlardır.

# ora-2 | İleri
## Soru
Bir sorgunun yürütme planında `TABLE ACCESS FULL` görüyorsun. Doğru yaklaşım hangisidir?
## A
Tam tablo taraması her zaman hatadır; sorgudaki her sütuna indeks açıp planı indeks erişimine zorlamak doğru reflekstir
🤖 çok satır dönen sorgularda tam tarama indeksten daha ucuz olabilir.
## B
Hemen bir ipucu (hint) yazıp indeks erişimine zorlarsın; ipucu Oracle'da ilk başvurulacak ayar yöntemidir
🤖 ipucu planı dondurur ve veri dağılımı değişince yanlış kalır, son çaredir.
## C
`EXPLAIN PLAN` gerçek çalışan planı ve gerçek satır sayılarını gösterdiği için başka bir doğrulamaya gerek kalmaz
🤖 `EXPLAIN PLAN` yalnız tahmini plandır, gerçek plan için `DBMS_XPLAN.DISPLAY_CURSOR` gerekir.
## D
Tablonun ne kadarının döndüğüne bakarsın; büyük bölümü dönüyorsa tam tarama, indeks + nested loop'tan ucuz olabilir
🤖 Doğru: tam tarama seçici olmayan sorgularda doğru erişim yoludur.
## Doğru
D
## Açıklama
`TABLE ACCESS FULL` tek başına bir hata göstergesi değildir. Sorgu tablonun küçük bir bölümünü seçiyorsa indeks aralık taraması + **nested loop** doğrudur; büyük bölümünü döndürüyorsa tam tarama **ardışık büyük okumalarla** yapıldığı için indeksten satır satır tabloya gidip gelmekten ucuzdur ve genellikle **hash join** ile eşleşir. Bu yüzden önce "kaç satır dönüyor?" sorusu sorulur (A yanlış). `EXPLAIN PLAN` yalnız optimizer'ın **tahminini** verir; gerçek planı ve gerçek satır sayılarını görmek için `DBMS_XPLAN.DISPLAY_CURSOR` kullanılır ve asıl değerli sinyal tahmin ile gerçek arasındaki uçurumdur (C yanlış). İpucu (hint) yazmak planı dondurduğu, veri dağılımı değişince yanlış kaldığı ve gerekçesi zamanla unutulduğu için **son çaredir**; önce istatistikler ve gerekirse plan kararlılığı (baseline) değerlendirilir (B yanlış).

# ora-3 | İleri
## Soru
JPA varlığında `@GeneratedValue(strategy = GenerationType.SEQUENCE)` kullanıyorsun. Hibernate'in `allocationSize` değeri varsayılan 50, veritabanındaki sequence ise `INCREMENT BY 1` tanımlı. Sonuç ne olur?
## A
Hibernate her sequence çağrısını 50'lik bir kimlik aralığı tahsisi sayar; veritabanı birer birer artırdığı için aralıklar üst üste biner ve benzersizlik ihlali riski doğar
🤖 Doğru: iki tarafın adım büyüklüğü uyuşmayınca üretilen kimlik aralıkları çakışır.
## B
Sorun oluşmaz; Hibernate uygulama açılışında sequence'in `INCREMENT BY` değerini okuyup `allocationSize` ayarını otomatik olarak ona eşitler
🤖 böyle bir otomatik eşitleme yoktur, uyum geliştiricinin sorumluluğudur.
## C
Kimlikler 50'şer atlayarak ilerler; oluşan tek etki numaralarda boşluk kalmasıdır, veri bütünlüğü açısından bir risk yoktur
🤖 asıl sorun boşluk değil, aralıkların çakışmasından doğan çakışan kimliklerdir.
## D
Kimlik üretimi yavaşlar; çözüm `GenerationType.IDENTITY`ye geçmektir çünkü IDENTITY toplu eklemede daha yüksek başarım verir
🤖 `IDENTITY` yığın (batch) eklemeyi kapattığı için toplu işte daha yavaştır.
## Doğru
A
## Açıklama
Hibernate `SEQUENCE` stratejisinde varsayılan `allocationSize = 50` ile çalışır: sequence'ten aldığı her değeri **50'lik bir kimlik aralığının tahsisi** sayar ve o aralıktaki kimlikleri veritabanına hiç sormadan kendi üretir. Veritabanındaki sequence `INCREMENT BY 1` ise, aynı sequence'ten okuyan başka bir oturum ya da uygulama **çakışan aralıklar** alır ve benzersizlik ihlali (duplicate key) hataları başlar. Doğru kurulum ikisini eşitlemektir: sequence `INCREMENT BY 50` yapılır ya da `allocationSize = 1` yazılır (her ekleme için bir veritabanı gidiş-dönüşü bedeliyle). Böyle bir otomatik eşitleme Hibernate tarafından yapılmaz (B yanlış). Kimlik numaralarında boşluk oluşması normaldir ve tek başına zararsızdır — asıl risk boşluk değil çakışmadır (C yanlış). `IDENTITY`ye kaçmak durumu kötüleştirir: Hibernate her satırın kimliğini ekleme anında geri almak zorunda kaldığı için JDBC yığın gönderimini kapatır ve toplu ekleme yavaşlar (D yanlış).

# ora-4 | Orta
## Soru
`musteri_ad` sütununda indeks olmasına rağmen `WHERE UPPER(musteri_ad) = 'ALI'` sorgusu bu indeksi kullanmıyor. Sebep ve çözüm nedir?
## A
İndeks bozulmuştur; `ALTER INDEX ... REBUILD` ile yeniden oluşturulunca sorgu indeksi kullanmaya başlar
🤖 indeks sağlamdır, sorun sorgunun sütunu bir fonksiyonla sarmasıdır.
## B
Oracle metin sütunlarında B-tree indeksi kullanmaz; metin araması için ayrı bir tam metin arama indeksi kurmak gerekir
🤖 metin sütunlarında B-tree indeksi normal biçimde kullanılır.
## C
Sütun bir fonksiyonla sarıldığı için normal indeks kullanılamaz; `UPPER(musteri_ad)` üzerine fonksiyon tabanlı indeks açmak ya da sütunu sarmadan sorgulamak gerekir
🤖 Doğru: indekste sütunun ham değeri saklıdır, fonksiyonun sonucu değil.
## D
İstatistikler bayattır; istatistikler yeniden toplandığında optimizer fonksiyonu sütundan ayırıp indeksi kullanmaya başlar
🤖 istatistik tazelemek fonksiyonla sarılmış sütunu indekslenebilir hale getirmez.
## Doğru
C
## Açıklama
Klasik bir B-tree indeksi sütunun **ham değerini** saklar. `UPPER(musteri_ad)` yazdığında aradığın şey artık sütunun kendisi değil, bir fonksiyonun sonucudur; indekste öyle bir değer bulunmadığı için Oracle indeksi kullanamaz ve tam tablo taramasına döner. İki çözüm vardır: sorguyu sütunu sarmayacak biçimde yazmak, ya da `UPPER(musteri_ad)` ifadesi üzerine bir **fonksiyon tabanlı indeks** (function-based index) açmak. İndeks bozulmuş değildir, yeniden oluşturmak bir şey değiştirmez (A yanlış); Oracle metin sütunlarında B-tree indeksini normal biçimde kullanır (B yanlış); istatistikleri tazelemek de fonksiyonla sarılmış bir sütunu indekslenebilir yapmaz (D yanlış). Aynı tuzağın sessiz biçimi **örtük tip dönüşümüdür**: `VARCHAR2` bir sütunu sayı ile karşılaştırınca Oracle sütunu sayıya çevirir ve indeks yine devre dışı kalır.

# ora-5 | İleri
## Soru
`SELECT basvuru_no, basvuru_tarihi FROM basvuru WHERE ROWNUM <= 20 ORDER BY basvuru_tarihi DESC` sorgusu neden "en yeni 20 başvuru" sonucunu vermez?
## A
`ROWNUM` 1'den değil 0'dan başladığı için ilk satır listeye hiç girmez ve sonuç bir satır eksik döner
🤖 `ROWNUM` 1'den başlar, sorun numaralandırmanın başlangıcı değildir.
## B
Oracle `ROWNUM` ile `ORDER BY`ın aynı sorguda kullanılmasına izin vermez; sorgu çalışmadan söz dizimi hatası verir
🤖 sorgu sorunsuz çalışır, yalnız beklenen sonucu vermez.
## C
`ROWNUM` sıralamadan sonra atanır, ancak filtre satır satır uygulandığı için sonuçta 20'den az satır döner
🤖 `ROWNUM` sıralamadan sonra değil önce atanır ve 20 satır döner.
## D
`ROWNUM` sıralamadan önce atanır: önce gelen ilk 20 satır alınır, sıralama sonra bu 20 satıra uygulanır
🤖 Doğru: filtre sıralamadan önce çalıştığı için yanlış küme seçilir.
## Doğru
D
## Açıklama
`ROWNUM` satırlara, satırlar sonuç kümesine **alındığı anda** atanır; `ORDER BY` ise bu adımdan **sonra** uygulanır. Dolayısıyla sorgu önce tablodan gelen ilk 20 satırı alır, sonra yalnız o 20 satırı tarihe göre sıralar — "en yeni 20 başvuru" değil, "rastgele 20 başvurunun sıralanmışı" döner. Doğru yazım sıralamayı bir alt sorguya (inline view) alıp `ROWNUM` filtresini dışarıda uygulamak, ya da Oracle 12c ve sonrasında standart sözdizimini kullanmaktır: `ORDER BY basvuru_tarihi DESC FETCH FIRST 20 ROWS ONLY`. `ROWNUM` 1'den başlar (A yanlış), `ORDER BY` ile birlikte kullanılması söz dizimi hatası değildir — sorgu sessizce yanlış sonuç verir, tehlikesi de budur (B yanlış) ve dönen satır sayısı 20'dir (C yanlış).

# ora-6 | Uzman
## Soru
Gün sonu (EOD) tahakkuk işi milyonlarca satırı güncelliyor. Ekipte "her şeyi PL/SQL paketine taşıyalım" ile "her şeyi Java'ya alalım" görüşleri çarpışıyor. Mülakatta en sağlam duruş hangisidir?
## A
Her şey Java'ya alınmalıdır; PL/SQL 2026'da geçersiz bir teknolojidir ve bankalar da kullanmayı büyük ölçüde bırakmıştır
🤖 PL/SQL bankaların çekirdek sistemlerinde hâlâ yaygın olarak kullanılmaktadır.
## B
Küme tabanlı ağır veri işi veriye yakın kalabilir; iş kuralları ise test edilebilirlik ve sürümleme için Java'da durur, PL/SQL de sürümlenip test edilir
🤖 Doğru: karar veri hacmi ile test edilebilirlik dengesine göre verilir, ideoloji ile değil.
## C
Her şey PL/SQL'e taşınmalıdır; veritabanında çalışan kod her koşulda daha hızlıdır ve veri bütünlüğü sağladığı için ayrıca birim testi gerektirmez
🤖 veritabanı kodu her koşulda hızlı değildir ve test gereksinimi ortadan kalkmaz.
## D
Karar tamamen keyfidir; performans farkı önceden ölçülemeyeceği için ekip hangi dili daha iyi biliyorsa onu seçmelidir
🤖 fark ölçülebilir; gidiş-dönüş sayısı ve aktarılan veri hacmi somut ölçütlerdir.
## Doğru
B
## Açıklama
Doğru duruş taraf tutmak değil **ayrımı yapabilmektir**. Milyonlarca satırı güncelleyen **küme tabanlı** (set-based) bir iş, veriyi ağ üzerinden uygulamaya taşımadan **veriye yakın** çalıştığında gidiş-dönüş sayısı ve aktarılan veri hacmi çok azalır; gün sonu tahakkuk ve mutabakat işlerinde fark saatlerle ölçülür. Buna karşılık karar kuralları, doğrulama ve iş akışı Java'da durduğunda birim testi yazılabilir, git'te sürümlenir, sürekli tümleştirme hattından geçer ve taşınabilir kalır. Bu yüzden çözüm "hepsi şurada" değil, işin türüne göre paylaştırmadır — ve PL/SQL de git'te sürümlenip otomatik testten geçirilmelidir. PL/SQL'in geçersiz olduğunu söylemek bankacılık gerçeğine aykırıdır (A yanlış); "veritabanı kodu her zaman hızlıdır ve test istemez" iddiası hem yanlış hem de tehlikelidir (C yanlış); farkın ölçülemeyeceğini söylemek de doğru değildir, gidiş-dönüş sayısı ve veri hacmi somut ölçütlerdir (D yanlış).
