---
slug: temel-sql
baslik: "Temel SQL: SELECT, JOIN, GROUP BY, Index, Normalizasyon ve NULL"
kademe: Temel
sira: 29
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Kocaman bir Excel dosyası düşün. İçinde birçok sayfa var: biri "Çalışanlar", biri "Siparişler". Her sayfada başlıklar (ad, maaş, bölüm) ve altında binlerce satır. Veritabanı (database) bunun disiplinli halidir: veriyi tablolarda saklar, aynı anda yüzlerce kişiye açar ve kaybetmemeyi garantiler. SQL ise bu dosyaya sorduğun sorunun dilidir: "maaşı 5000'in üstünde olan çalışanları bölüme göre grupla ve 3 kişiden kalabalık bölümleri göster." Excel'de bunu elle süzerdin; SQL'de tek cümle yazarsın.

Kilit fark şudur: SQL **bildirimsel** (declarative) bir dildir. "Ne istediğini" yazarsın; "nasıl getirileceğine" veritabanı kendi karar verir.

### Temel kavramlar

- **Tablo (table)**: tek bir konunun verisini tutan ızgara, ör. `calisanlar`.
- **Sütun (column)**: tablonun bir özelliği ve türü, ör. `maas` (sayı). Excel'deki başlık.
- **Satır (row / kayıt)**: tek bir gerçek varlık, ör. bir çalışan. Excel'deki bir satır.
- **Birincil anahtar (primary key / PK)**: satırı benzersiz kimlikleyen sütun. Zorunlu olarak `NOT NULL` ve tekrarsızdır, ör. `siparis_id`.
- **Yabancı anahtar (foreign key / FK)**: başka tablonun PK'sine referans veren sütun; bütünlüğü (referential integrity) zorlar, ör. `siparisler.musteri_id`.
- **NULL**: "değer yok / bilinmiyor". Sıfır değildir, boş metin de değildir.
- **Toplama fonksiyonu (aggregate)**: birçok satırı tek değere indiren fonksiyon — `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`.

Basit bir sorgu:

```sql
SELECT ad, maas FROM calisanlar WHERE maas > 5000 ORDER BY maas DESC;
```

`SELECT` hangi sütunlar, `FROM` hangi tablo, `WHERE` hangi satırlar, `ORDER BY` hangi sıra. `DISTINCT` yinelenen satırları eler.

### Nasıl çalışır?

**Mantıksal çalışma sırası yazım sırasından farklıdır.** Yazarken `SELECT` başa gelir; veritabanı ise şu sırayla düşünür:

`FROM` → `WHERE` → `GROUP BY` → `HAVING` → `SELECT` → `ORDER BY`

- `WHERE` satırları **gruplamadan ÖNCE**, tek tek filtreler; içinde toplama fonksiyonu bulunamaz.
- `GROUP BY` kalan satırları gruplara indirger.
- `HAVING` gruplanmış sonucu, toplama değerine göre filtreler.

```sql
SELECT bolum, COUNT(*) AS calisan_sayisi
FROM calisanlar
WHERE maas > 5000          -- satır koşulu (gruplamadan önce)
GROUP BY bolum
HAVING COUNT(*) > 3;       -- grup koşulu (gruplamadan sonra)
```

Kısaca: satır koşulu → `WHERE`, grup/aggregate koşulu → `HAVING`. İkisi stil tercihi değildir; `HAVING`'in `ORDER BY` ile de bir ilgisi yoktur.

**JOIN** iki tabloyu birleştirir:

- `INNER JOIN` — yalnız iki tarafta da eşleşen satırlar.
- `LEFT JOIN` — sol tablonun TÜM satırları + sağdan eşleşenler; eşleşme yoksa sağ taraf sütunları `NULL` gelir.
- `RIGHT JOIN` — simetriği (sağ tablonun tümü).
- `FULL OUTER JOIN` — iki tarafın tüm satırları; eşleşmeyende karşı taraf `NULL`.

100 müşterinin 30'unun hiç siparişi yoksa, `LEFT JOIN` sonucunda 100 müşterinin hepsi görünür ve o 30'unda sipariş sütunları `NULL` olur (hata olmaz). Bir müşterinin birden çok siparişi varsa toplam satır sayısı 100'ü aşabilir; buradaki nokta tüm müşterilerin GÖRÜNMESİdir. `INNER JOIN` olsaydı yalnız 70 müşteri gelirdi. "Hiç siparişi olmayanlar" ise tam olarak `LEFT JOIN ... WHERE s.id IS NULL` (anti-join) ile bulunur.

```svg
<svg viewBox="0 0 660 202" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="JOIN türleri: INNER, LEFT, RIGHT, FULL OUTER Venn diyagramları">
  <defs><clipPath id="sql-inner"><circle cx="81" cy="108" r="28"/></clipPath></defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JOIN türleri (Venn: sol tablo A, sağ tablo B)</text>
  <circle cx="81" cy="108" r="28" fill="#FFFFFF" stroke="#186399" stroke-width="1.4"/>
  <circle cx="109" cy="108" r="28" fill="#FFFFFF" stroke="#186399" stroke-width="1.4"/>
  <circle cx="109" cy="108" r="28" fill="#25804C" clip-path="url(#sql-inner)"/>
  <circle cx="81" cy="108" r="28" fill="none" stroke="#186399" stroke-width="1.4"/>
  <circle cx="109" cy="108" r="28" fill="none" stroke="#186399" stroke-width="1.4"/>
  <text x="95" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">INNER JOIN</text>
  <text x="95" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yalnız eşleşenler</text>
  <circle cx="241" cy="108" r="28" fill="#25804C" stroke="#186399" stroke-width="1.4"/>
  <circle cx="269" cy="108" r="28" fill="none" stroke="#186399" stroke-width="1.4"/>
  <text x="255" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">LEFT JOIN</text>
  <text x="255" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">A'nın tümü + eşleşen</text>
  <circle cx="429" cy="108" r="28" fill="#25804C" stroke="#186399" stroke-width="1.4"/>
  <circle cx="401" cy="108" r="28" fill="none" stroke="#186399" stroke-width="1.4"/>
  <text x="415" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">RIGHT JOIN</text>
  <text x="415" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">B'nin tümü + eşleşen</text>
  <circle cx="561" cy="108" r="28" fill="#25804C" stroke="#186399" stroke-width="1.4"/>
  <circle cx="589" cy="108" r="28" fill="#25804C" stroke="#186399" stroke-width="1.4"/>
  <text x="575" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">FULL OUTER</text>
  <text x="575" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">iki tarafın tümü</text>
</svg>
```

**Index (dizin)**, bir sütun için tutulan ayrı, sıralı arama yapısıdır (genelde B-tree). `WHERE`/`JOIN`/`ORDER BY`'ı tüm tabloyu taramaktan (full scan) kurtarır — okumada büyük kazanç. Bedeli vardır: tablo her değiştiğinde (`INSERT`/`UPDATE`/`DELETE`) ilgili tüm index'ler de güncellenir ve her index disk yeri tüketir. Bu yüzden "her sütuna index" yanlıştır. Index metin ve tarih dahil çoğu türde çalışır, bir tabloda birden çok index olabilir ve index tabloyu salt-okunur yapmaz — sınır teknik değil, okuma kazancı ile yazma maliyeti arasındaki dengedir. Index sık filtrelenen/birleştirilen ve **seçiciliği** (selectivity: farklı değer çeşitliliği) yüksek sütunlara konur; iki değerli bir bayrak sütununda genelde işe yaramaz.

**Normalizasyon** veri tekrarını ve güncelleme anomalilerini azaltır:

- **1NF** — değerler atomik, tekrar eden grup yok.
- **2NF** — 1NF + kısmi bağımlılık yok: bileşik (composite) PK'nin yalnız bir PARÇASINA bağlı alan olmaz.
- **3NF** — 2NF + geçişli (transitive) bağımlılık yok: anahtar-olmayan bir sütun, başka bir anahtar-olmayan sütuna bağlı olmaz.

Örnek: `siparisler(siparis_id PK, musteri_id, musteri_sehri)`. Burada `musteri_sehri`, PK'ye değil anahtar-olmayan `musteri_id`'ye bağlıdır — bu **3NF ihlalidir** (şehir atomik olduğu için 1NF sorunu değildir; PK tek sütun olduğu için 2NF sorunu da değildir). Sonuç: aynı müşterinin her siparişinde şehir tekrar saklanır ve müşteri taşınınca tüm satırları güncellemek gerekir; biri atlanırsa tutarsızlık doğar. Çözüm: şehri ayrı bir `musteriler` tablosuna taşıyıp `musteri_id` FK'siyle bağlamak.

**NULL ve üç değerli mantık.** NULL "bilinmiyor" demektir, bu yüzden SQL'de mantık TRUE/FALSE değil **TRUE/FALSE/UNKNOWN**'dır. `bonus = NULL` karşılaştırması TRUE ya da FALSE değil UNKNOWN üretir (bilinmeyen bir değerin bir şeye eşit olup olmadığı da bilinemez). `WHERE` yalnız koşulu TRUE olan satırları geçirdiği için UNKNOWN satırlar sessizce elenir → sorgu boş döner. Söz dizimi geçerlidir, sütun türüyle de ilgisi yoktur; NULL'lar tabloda gerçekten saklanır. Doğrusu `WHERE bonus IS NULL` / `IS NOT NULL` kullanmaktır. Aynı sebeple `NULL = NULL` de TRUE değildir.

NULL'un ikinci sonucu toplama fonksiyonlarındadır: `COUNT(*)` satırları sayar ve içeriğe hiç bakmaz; `COUNT(sutun)` ise o sütunun yalnız **NULL OLMAYAN** değerlerini sayar. 10 satırın 4'ünde `bonus` NULL ise `COUNT(*)` → 10, `COUNT(bonus)` → 6. `SUM`, `AVG`, `MIN`, `MAX` de NULL'ları görmezden gelir (`AVG` NULL'ları paydaya katmaz). İkisini karıştırmak sessiz bir mantık hatası üretir.

```svg
<svg viewBox="0 0 680 152" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SQL mantıksal çalışma sırası: FROM WHERE GROUP BY HAVING SELECT ORDER BY">
  <defs>
    <marker id="sql-ok" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
      <path d="M0,0 L8,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="340" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Mantıksal çalışma sırası (yazım sırasından farklı)</text>
  <rect x="12" y="44" width="96" height="42" rx="7" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="60" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">FROM</text>
  <line x1="110" y1="65" x2="120" y2="65" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sql-ok)"/>
  <rect x="122" y="44" width="96" height="42" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="170" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">WHERE</text>
  <line x1="220" y1="65" x2="230" y2="65" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sql-ok)"/>
  <rect x="232" y="44" width="96" height="42" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="280" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">GROUP BY</text>
  <line x1="330" y1="65" x2="340" y2="65" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sql-ok)"/>
  <rect x="342" y="44" width="96" height="42" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="390" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">HAVING</text>
  <line x1="440" y1="65" x2="450" y2="65" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sql-ok)"/>
  <rect x="452" y="44" width="96" height="42" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="500" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">SELECT</text>
  <line x1="550" y1="65" x2="560" y2="65" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sql-ok)"/>
  <rect x="562" y="44" width="106" height="42" rx="7" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="615" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">ORDER BY</text>
  <text x="170" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">tekil SATIR filtreler</text>
  <text x="170" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">aggregate içeremez</text>
  <text x="390" y="112" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">GRUP filtreler</text>
  <text x="390" y="128" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">aggregate (COUNT&gt;3) olur</text>
</svg>
```

### Mülakatta ne sorulur?

- **"WHERE ile HAVING farkı?"** → WHERE gruplamadan önce tekil satır (aggregate içeremez), HAVING gruplamadan sonra grup.
- **"LEFT JOIN ne döndürür?"** → Sol tablonun tümü; eşleşmeyende sağ sütunlar NULL. Anti-join için `WHERE sag.id IS NULL`.
- **"Her sütuna index koyalım mı?"** → Hayır; her yazma işleminde güncellenir ve yer kaplar, düşük seçicilikte kazanç yoktur.
- **"`= NULL` neden boş döner?"** → Üç değerli mantıkta UNKNOWN; `IS NULL` kullanılır.
- **"`COUNT(*)` ile `COUNT(sutun)` farkı?"** → İlki satır sayar, ikincisi NULL olmayanları.
- **"Bu tablo hangi normal formu ihlal ediyor?"** → Geçişli bağımlılık 3NF, bileşik PK parçasına bağımlılık 2NF, atomik olmayan değer 1NF.

### Tek cümlede

SQL'de "ne istediğini" yazarsın ve sonucu üç şey belirler: sorgunun mantıksal çalışma sırası, JOIN'in hangi tarafın satırlarını koruduğu, ve NULL'un eşitliği değil "bilinmiyor"u temsil etmesi.

# sql-1 | Temel
## Soru
Bu sorguda `WHERE` ile `HAVING` arasındaki fark nedir?

```sql
SELECT bolum, COUNT(*) AS calisan_sayisi
FROM calisanlar
WHERE maas > 5000
GROUP BY bolum
HAVING COUNT(*) > 3;
```
## A
İkisi de aynı işi yapar; hangisinin kullanılacağı yalnız bir yazım/stil tercihidir
🤖 Aynı değildir: `WHERE` gruplamadan önce tekil satırı, `HAVING` gruplamadan sonra grubu filtreler.
## B
`WHERE` gruplamadan ÖNCE tekil satırları, `HAVING` gruplama SONRASI grupları toplama sonucuna göre filtreler
🤖 Doğru: satır koşulu WHERE'de, grup/aggregate koşulu HAVING'de olur.
## C
`WHERE` gruplamadan sonra, `HAVING` ise gruplamadan önce çalışır; sıra tam tersidir
🤖 Sıra ters; `WHERE` gruplamadan önce, `HAVING` sonra çalışır.
## D
`HAVING` yalnız `ORDER BY` ile birlikte kullanılabilir; sıralama olmadan yazılamaz
🤖 `HAVING`, `GROUP BY` ile gruplanmış sonucu filtreler; `ORDER BY` ile bir bağı yoktur.
## Doğru
B
## Açıklama
Mantıksal çalışma sırası `FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY`'dır. `WHERE` gruplama YAPILMADAN önce her satırı tek tek eler (ve toplama fonksiyonu içeremez) — burada maaşı 5000 altındakiler daha gruplanmadan atılır. `GROUP BY` kalanları bölümlere indirger; `HAVING` ise bu grupları toplama sonucuna göre süzer (`COUNT(*) > 3`) (B). Kısaca: satır koşulu → `WHERE`, grup/aggregate koşulu → `HAVING`.

# sql-2 | Temel
## Soru
`musteriler` tablosunda 100 müşteri var; bunların 30'unun hiç siparişi yok. `LEFT JOIN` sonucu bu 100 müşteri ve siparişleri hakkında ne söylenir?

```sql
SELECT * FROM musteriler m
LEFT JOIN siparisler s ON m.id = s.musteri_id;
```
## A
Yalnız siparişi olan 70 müşteri döner; siparişsizler sonuçta hiç görünmez
🤖 O `INNER JOIN` davranışıdır; `LEFT JOIN` sol tablonun tümünü korur.
## B
Hata verir; eşleşmeyen satırlar JOIN'i bozar
🤖 `LEFT JOIN` eşleşmeyeni NULL ile doldurur, hata olmaz.
## C
Yalnız 30 satır döner; sadece siparişsiz müşteriler
🤖 `LEFT JOIN` tümünü verir, yalnız eşleşmeyenleri değil.
## D
100 müşterinin tamamı görünür; siparişi olmayan 30'unda `s` (sağ tablo) sütunları `NULL` gelir
🤖 Doğru: `LEFT JOIN` sol tablonun her satırını korur, eşleşme yoksa sağ taraf NULL olur.
## Doğru
D
## Açıklama
`LEFT JOIN` sol (ilk) tablonun TÜM satırlarını korur; sağ tabloda eşleşme bulunmayan satırlarda sağ sütunlar `NULL` ile doldurulur (D). Yani 100 müşterinin hepsi listelenir, siparişsiz 30'unda `s.*` alanları NULL olur (bir müşterinin birden çok siparişi varsa toplam satır sayısı 100'ü aşabilir; buradaki nokta tüm müşterilerin GÖRÜNMESİdir). `INNER JOIN` olsaydı yalnız siparişi olan 70 müşteri gelirdi (A). "Siparişi olmayan müşteriler" ise tam olarak `LEFT JOIN ... WHERE s.id IS NULL` (anti-join) ile bulunur.

# sql-3 | Orta
## Soru
Bir sütuna index (dizin) eklemek okuma sorgularını hızlandırır. Bu neden "her sütuna index koy" anlamına GELMEZ?
## A
Her index `INSERT`/`UPDATE`/`DELETE` sırasında güncellenmeli ve disk yeri tüketir; yazma maliyeti artar
🤖 Doğru: index'in okuma kazancı, yazma ve depolama maliyetiyle dengelenir.
## B
Index yalnız sayısal sütunlarda çalışır; metin ve tarih sütunlarında hiç etkisi olmaz
🤖 Index metin/tarih dahil çoğu türde çalışır; asıl kısıt sütun türü değil, yazma ve depolama maliyetidir.
## C
Index eklenen tablo salt-okunur (read-only) hale gelir, artık güncellenemez
🤖 Index tabloyu salt-okunur yapmaz; yalnız yazma işlemlerine ek maliyet bindirir.
## D
Bir tabloda en fazla bir index olabildiğinden ikinci index ilkini geçersiz kılar
🤖 Bir tabloda birden çok index olabilir; sınır teknik değil, okuma kazancı–yazma maliyeti dengesidir.
## Doğru
A
## Açıklama
Index ayrı, sıralı bir arama yapısıdır (genelde B-tree) ve `WHERE`/`JOIN`/`ORDER BY`'ı tam taramadan kurtarır — okumada büyük kazanç. Ama bedava değildir: tablo her değiştiğinde (`INSERT`/`UPDATE`/`DELETE`) ilgili tüm index'ler de güncellenmeli ve her index disk yeri tüketir (A). Bu yüzden index sık filtrelenen/birleştirilen, seçiciliği (selectivity) yüksek sütunlara konur; nadir sorgulanan ya da çok az farklı değeri olan (düşük seçicilik) sütunlara index genellikle zarardan başka işe yaramaz.

# sql-4 | Orta
## Soru
`bonus` sütununda NULL değerler olmasına rağmen bu sorgu neden hiç satır döndürmez?

```sql
SELECT * FROM calisanlar WHERE bonus = NULL;
```
## A
`= NULL` söz dizimsel bir hatadır; veritabanı sorguyu reddettiği için boş küme döner
🤖 Söz dizimi geçerlidir, sorgu çalışır; sorun sonucun üç-değerli mantıkta UNKNOWN olmasıdır.
## B
NULL değerler tabloda fiziksel olarak saklanmaz, bu yüzden hiçbir koşulla eşleşemezler
🤖 NULL da saklanır (ve `IS NULL` ile bulunur); sorun karşılaştırmanın UNKNOWN dönmesidir.
## C
`bonus = NULL` üç-değerli mantıkta (three-valued logic) UNKNOWN üretir; `WHERE` ise yalnız TRUE satırları geçirir
🤖 Doğru: bu yüzden NULL kontrolü `IS NULL` ile yapılır.
## D
`=` operatörü yalnız metin sütunlarında NULL karşılaştırabilir; sayısal sütunlarda çalışmaz
🤖 Sütun türüyle ilgisi yok; `=` her türde çalışır, NULL'a özgü davranış üç-değerli mantıktan gelir.
## Doğru
C
## Açıklama
NULL "bilinmiyor" demektir ve SQL üç-değerli mantık kullanır: `bonus = NULL` sonucu TRUE ya da FALSE değil, **UNKNOWN**'dır (bilinmeyen bir değerin bir şeye eşit olup olmadığı da bilinemez). `WHERE` yalnız koşulu TRUE olan satırları döndürdüğünden, UNKNOWN üreten tüm satırlar (yani tüm NULL'lar) elenir → boş sonuç (C). Doğrusu `WHERE bonus IS NULL` kullanmaktır; `IS NULL`/`IS NOT NULL` tam olarak bu durum için vardır. Aynı sebeple `NULL = NULL` de TRUE değildir.

# sql-5 | Orta
## Soru
Bir `siparisler` tablosunda sütunlar: `siparis_id` (PK), `musteri_id`, `musteri_sehri`. Burada `musteri_sehri`, `musteri_id`'ye bağlıdır (`siparis_id`'ye değil). Bu tasarım hangi normal formu ihlal eder ve neden sorundur?
## A
1NF; çünkü `musteri_sehri` atomik olmayan, birden çok bilgi taşıyan bir sütundur
🤖 Şehir atomik bir değerdir; sorun atomiklik değil, geçişli bağımlılıktır.
## B
Hiçbirini; her sütun doğrudan PK'ye bağlı olduğundan tasarım tamamen normalizedir
🤖 Geçişli bağımlılık 3NF'i ihlal eder; tasarım normal değildir.
## C
3NF; `musteri_sehri` anahtar-olmayan `musteri_id`'ye bağlı (geçişli bağımlılık), bu da güncelleme anomalisi yaratır
🤖 Doğru: anahtar-olmayan → anahtar-olmayan bağımlılık 3NF'i ihlal eder.
## D
2NF; çünkü tablonun bileşik (composite) primary key'i var ve kısmi bağımlılık oluşuyor
🤖 PK tek sütundur (`siparis_id`); 2NF kısmi bağımlılıkla ilgilidir, buradaki sorun geçişlidir.
## Doğru
C
## Açıklama
3NF, anahtar-olmayan bir sütunun başka bir anahtar-olmayan sütuna bağlı olmamasını (geçişli/transitive bağımlılık yasağı) ister. Burada `musteri_sehri`, PK olan `siparis_id` yerine anahtar-olmayan `musteri_id`'ye bağlıdır — bu 3NF ihlalidir (C). Sonuç: aynı müşterinin her siparişinde şehir tekrar saklanır (yer israfı) ve müşteri taşınınca tüm satırları güncellemek gerekir; biri atlanırsa tutarsızlık (güncelleme anomalisi) doğar. Çözüm: `musteri_sehri`'ni ayrı bir `musteriler` tablosuna taşıyıp `musteri_id` FK'siyle bağlamaktır. 2NF (D) bileşik PK'nin PARÇASINA bağımlılıkla ilgilidir; burada PK tek sütundur.

# sql-6 | İleri
## Soru
Tabloda 10 satır var, `bonus` sütununun 4'ü NULL. Bu sorgu ne döndürür?

```sql
SELECT COUNT(*), COUNT(bonus) FROM calisanlar;
```
## A
10 ve 10
🤖 `COUNT(bonus)` NULL'ları saymaz; ikinci değer 10 olamaz.
## B
10 ve 4
🤖 `COUNT(bonus)` NULL'ları değil, dolu olanları sayar; sonuç 6 olmalı.
## C
6 ve 6
🤖 `COUNT(*)` satır sayar, NULL'a bakmaz; ilk değer 10 olmalı.
## D
10 ve 6
🤖 Doğru: `COUNT(*)` tüm satırları (10) sayar; `COUNT(bonus)` yalnız NULL olmayanları (6) sayar.
## Doğru
D
## Açıklama
`COUNT(*)` satır sayısını sayar; içerikteki NULL'lara bakmaz → 10. `COUNT(sutun)` ise o sütunun yalnız **NULL olmayan** değerlerini sayar; 4 NULL sayılmaz → 6 (D). Bu ayrım aggregate fonksiyonların genel kuralıdır: `SUM`, `AVG`, `MIN`, `MAX` de NULL'ları görmezden gelir (ör. `AVG` NULL'ları paydaya katmaz). Bu yüzden "kaç çalışanın bonusu tanımlı?" için `COUNT(bonus)`, "kaç çalışan var?" için `COUNT(*)` kullanılır. İkisini karıştırmak sessiz bir mantık hatası üretir.
