---
slug: veritabani-indeksleme
baslik: "Veritabanı İndeksleme: B-tree/Hash, Composite/Covering, Selectivity, EXPLAIN ve Yazma Maliyeti"
kademe: İleri
sira: 146
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uygulamanın kalıcı bilgileri **veritabanı** (database) denen programda saklanır. İlişkisel bir veritabanında veri bir çizelge gibidir: her **sütun** bir alan (`ad`, `sehir`, `eposta`), her **satır** bir kayıttır (bir kullanıcı). Veritabanına "şehri Ankara olan kullanıcıları getir" dediğini düşün. Elinde başka bir yol yoksa yapabileceği tek şey tabloyu **baştan sona okumaktır**. Buna tam tarama (full scan / sequential scan) denir. Tabloda 20 kayıt varsa sorun yok; 20 milyon kayıt varsa her sorgu diski baştan sona süpürür.

**İndeks** (index), tam da bunu önlemek için tutulan **ek bir veri yapısıdır** — kitabın arkasındaki dizin gibi. Dizin, "yük dengeleme" kelimesinin 143. sayfada geçtiğini söyler; kitabı baştan sona okumana gerek kalmaz. Ama bu kolaylık bedavaya gelmez: kitaba yeni bir sayfa eklediğinde dizini de güncellemen gerekir. İndeks de aynen böyledir: **okumayı hızlandırır, yazmayı yavaşlatır ve yer kaplar**. İndeks bir sihir değil, bilinçli bir **ödünleşimdir**.

### Temel kavramlar

- **B-tree indeks**: değerleri **sıralı** tutan dengeli ağaç; veritabanlarının varsayılan indeksidir.
- **Hash indeks**: değeri bir sayıya çevirip kutulara dağıtır; yalnız "tam eşitlik" araması yapar.
- **Composite (bileşik) indeks**: birden çok sütunu birlikte indeksler, örn. `(sehir, dogum_tarihi)`.
- **Covering indeks / index-only scan**: sorgunun ihtiyacı olan tüm sütunları içeren, tabloya hiç gitmeyi gerektirmeyen indeks.
- **Seçicilik (selectivity)**: bir sütunun ne kadar ayırt edici olduğu; `eposta` çok seçici, `cinsiyet` çok değil.
- **Planlayıcı (query planner) ve `EXPLAIN`**: veritabanının hangi yolu seçtiğini gösteren araç.

### Nasıl çalışır?

**B-tree** değerleri sıralı tutar. Bu yüzden dört işi birden verimli karşılar: `=` eşitliği, aralıklar (`BETWEEN`, `<`, `>`), önek araması (`LIKE 'abc%'`) ve `ORDER BY` sıralaması. Çalışma biçimi şudur: aranan aralığın başlangıcını logaritmik zamanda bulur, sonra ardışık yaprak düğümler boyunca ilerler. **Özet (hash) indeks** ise değeri bir özet fonksiyonuyla kutulara dağıtır. Sıra bilgisi kaybolduğu için yalnız nokta araması (`=`) yapabilir; aralık ya da sıralama yapamaz. Bu yüzden varsayılan hep B-tree'dir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="B-tree ve hash indeks yetenekleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki indeks, iki yetenek kümesi</text>
  <rect x="28" y="44" width="272" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">B-tree (varsayılan)</text>
  <text x="164" y="98" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">= eşitlik ✓</text>
  <text x="164" y="120" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">&lt; &gt; BETWEEN aralık ✓</text>
  <text x="164" y="142" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">ORDER BY / önek ✓</text>
  <text x="164" y="164" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sıralı ağaç yapısı</text>
  <rect x="340" y="44" width="272" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Hash</text>
  <text x="476" y="98" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">= eşitlik ✓ (O(1))</text>
  <text x="476" y="120" text-anchor="middle" font-size="11.5" fill="#B23A48" font-family="system-ui">aralık ✗</text>
  <text x="476" y="142" text-anchor="middle" font-size="11.5" fill="#B23A48" font-family="system-ui">sıralama ✗</text>
  <text x="476" y="164" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yalnız nokta arama</text>
</svg>
```

**Bileşik indekste** en kritik kural **en-soldan önek (leftmost prefix)** kuralıdır. `(sehir, dogum_tarihi)` indeksi önce `sehir`'e göre, `sehir` eşitken `dogum_tarihi`'ne göre sıralıdır. Bu yüzden `sehir = 'Ankara'` ve `sehir = 'Ankara' AND dogum_tarihi > ...` sorguları indeksi tam kullanır. Ama `sehir` koşulu **yokken** yalnız `dogum_tarihi` sorgulanırsa ağacın ilk düzeyi belirsiz kalır. İndeks etkili bir daraltma yapamaz ve planlayıcı çoğunlukla tam tarama seçer. Kısacası bileşik indekste **sütun sırası bir tasarım kararıdır**: en sık ve tek başına sorgulanan sütunu başa koy.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bileşik indeks en-soldan önek kuralı ve hangi sorgulara yarar">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bileşik indeks: en-soldan önek kuralı</text>
  <rect x="180" y="42" width="140" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="250" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">sehir</text>
  <rect x="320" y="42" width="180" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="410" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">dogum_tarihi</text>
  <text x="340" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">indeks bu sırada sıralıdır — önce sol sütun</text>
  <text x="40" y="128" font-size="11" fill="#25804C" font-family="system-ui">✓</text>
  <text x="58" y="128" font-size="11" fill="#1C3247" font-family="system-ui">WHERE sehir = 'Ankara'</text>
  <text x="40" y="154" font-size="11" fill="#25804C" font-family="system-ui">✓</text>
  <text x="58" y="154" font-size="11" fill="#1C3247" font-family="system-ui">WHERE sehir = 'Ankara' AND dogum_tarihi &gt; '1990-01-01'</text>
  <text x="40" y="180" font-size="11" fill="#8A2E2E" font-family="system-ui">✗</text>
  <text x="58" y="180" font-size="11" fill="#1C3247" font-family="system-ui">WHERE dogum_tarihi &gt; '1990-01-01'  (sol sütun sehir yok)</text>
</svg>
```

**Covering indeks** ikinci büyük optimizasyondur. Normalde indeks sana yalnız satırın nerede olduğunu söyler. `SELECT` ile istediğin sütunlar indekste yoksa veritabanı eşleşen her satır için asıl tabloya (heap) gitmek zorunda kalır; bu ek erişim pahalıdır. Ama indeks hem filtrelenen hem döndürülen sütunu birlikte kapsıyorsa iş değişir. Örneğin `(sehir, eposta)` ya da `(sehir) INCLUDE (eposta)` indeksinde cevap tümüyle indeksten üretilir; tabloya hiç gidilmez. Buna **yalnız-indeks taraması (index-only scan)** denir. Bu, birincil anahtara özgü bir şey değildir; kapsayıcı olan her indeksle olur. Bedeli şudur: indeks genişler, biraz daha yer kaplar ve yazması biraz daha pahalıya gelir.

```svg
<svg viewBox="0 0 640 180" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Covering indeks ile normal indeks: heap erişimi farkı">
  <defs>
    <marker id="idx146-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Covering indeks: heap'e gitmeden yanıt</text>
  <text x="80" y="66" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">Normal</text>
  <rect x="150" y="44" width="150" height="44" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="225" y="71" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İndeks (sehir)</text>
  <rect x="380" y="44" width="150" height="44" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="455" y="71" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Heap (tablo)</text>
  <line x1="300" y1="66" x2="378" y2="66" stroke="#5D7789" stroke-width="1.6" marker-end="url(#idx146-arw)"/>
  <text x="339" y="58" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">eposta için</text>
  <line x1="530" y1="66" x2="600" y2="66" stroke="#5D7789" stroke-width="1.6" marker-end="url(#idx146-arw)"/>
  <text x="565" y="58" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yanıt</text>
  <text x="80" y="140" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">Covering</text>
  <rect x="150" y="118" width="260" height="44" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="280" y="145" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İndeks (sehir, eposta)</text>
  <line x1="410" y1="140" x2="480" y2="140" stroke="#5D7789" stroke-width="1.6" marker-end="url(#idx146-arw)"/>
  <text x="560" y="144" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yanıt (heap yok)</text>
</svg>
```

**Seçicilik**, bir indeksin işe yarayıp yaramayacağını belirler. `eposta` gibi neredeyse benzersiz bir sütun çok seçicidir; indeks seni birkaç satıra hızla götürür. `cinsiyet` gibi iki değerli bir sütun ise **düşük seçiciliktedir**. `= 'K'` koşulu kayıtların yarısını döndürür. İndeksle gitmek, eşleşen her satır için tabloda **rastgele erişim (random I/O)** demektir. Bunun toplamı, tabloyu **sıralı** okumaktan pahalıya gelir. Maliyet temelli planlayıcı bunu istatistiklerden hesaplar ve doğru biçimde tam tarama seçer. Bu bir arıza değil, doğru bir karardır.

Bunun karşılığında bir **yazma maliyeti** vardır. Tabloya her `INSERT`, `UPDATE` ya da `DELETE` geldiğinde o tablonun **her bir indeksi de** güncellenmelidir: ağaca ekleme, silme ve olası sayfa bölünmeleri. Sekiz indeksli bir tabloya ekleme yapmak, indekssiz bir tabloya göre çok daha pahalıdır. Ayrıca her indeks depolama ve önbellek alanı tüketir. "Ne olur ne olmaz, her sütuna indeks açalım" bu yüzden bir anti-desendir.

Teşhiste ilk araç **`EXPLAIN (ANALYZE)`**'dır. Planlayıcının seçtiği erişim yolunu gösterir; `ANALYZE` eklediğinde **gerçek** satır sayısını ve süreyi de verir. Diyelim ki birkaç satır beklerken milyonlarca satırlık bir `Seq Scan` görüyorsun. Üç ihtimal var: uygun indeks yoktur, istatistikler bayattır ya da sorgunun kendisi indeksi devre dışı bırakıyordur. Sütun üzerinde fonksiyon veya tip dönüşümü kullanmak (`WHERE UPPER(ad) = ...`) indeksi kullanılamaz hâle getirir. Bu yüzden sorguyu indeks dostu (sargable) yazmak gerekir.

### Mülakatta ne sorulur?

- **Aralık sorgusunu hangi indeks hızlandırır?** B-tree; sıralı olduğu için aralığı tarayabilir. Hash yalnız `=` yapar.
- **`(sehir, dogum_tarihi)` indeksi hangi sorguya yaramaz?** En-soldan önek atlanan sorguya: yalnız `dogum_tarihi` koşullu olan.
- **Index-only scan için ne gerekir?** İndeksin hem filtre hem çıktı sütunlarını kapsaması (covering). Birincil anahtar şartı yoktur, hash'e özgü de değildir.
- **Planlayıcı neden indeksi kullanmıyor?** Çoğunlukla düşük seçicilik: yarım tabloya rastgele erişim, tam taramadan pahalıdır. Bozuk indeks değil, doğru bir maliyet kararıdır.
- **Çok indeksin gizli maliyeti?** Her yazma tüm indeksleri günceller; yazma yavaşlar, depolama artar.
- **`EXPLAIN` çıktısında büyük bir `Seq Scan` gördün, ne yaparsın?** Üç şeye bakarsın: indeks var mı, seçicilik yeterli mi, sorgu indeksi engelliyor mu (fonksiyon ya da tip dönüşümü). Kararı tahminle değil, planla verirsin.

### Tek cümlede

İndeks, okumayı hızlandırmak için yazmadan ve yerden ödün veren bir takastır. Doğru kullanımı "her yere indeks açmak" değildir; gerçek erişim desenini `EXPLAIN` ile ölçüp hedefli indekslemektir.

# idx-1 | Orta
## Soru
`WHERE olusturma_tarihi BETWEEN '2026-01-01' AND '2026-03-31'` gibi bir aralık sorgusunu hızlandırmak için hangi indeks türü uygundur?
## A
B-tree; sıralı yapısı aralık ve sıralama sorgularını verimli karşılar
🤖 Doğru: B-tree sıralı olduğu için aralığın başını bulup ardışık yaprakları tarar.
## B
Hash indeks; her değeri O(1) bulduğu için aralık taraması da hızlıdır
🤖 Hash yalnız eşitlik içindir; değerler dağıtıldığından aralık taraması yapamaz.
## C
Hiçbir indeks aralığa yardım edemez; bu sorgu mutlaka full scan ister
🤖 B-tree tam olarak aralık sorgularına yarar; full scan zorunlu değildir.
## D
Yalnız covering indeks aralığı hızlandırır; alttaki indeks türü fark etmez
🤖 Covering bir *ek* optimizasyondur; aralık yeteneği B-tree'nin sıralı yapısından gelir.
## Doğru
A
## Açıklama
Aralık (range) sorguları — `BETWEEN`, `<`, `>`, `LIKE 'abc%'` ve `ORDER BY` — **B-tree** indeksin güçlü alanıdır: B-tree değerleri **sıralı** tuttuğundan, aralığın başlangıcını logaritmik zamanda bulur ve oradan ardışık yaprak düğümleri boyunca ilerleyerek aralığı verimli tarar. **Hash** indeks değerleri hash fonksiyonuyla dağıttığından sıra bilgisi yoktur; yalnız `=` (nokta arama) yapabilir, aralık/sıralama yapamaz. Bu yüzden çoğu ilişkisel veritabanının varsayılan indeksi B-tree'dir; hash indeks yalnız katı eşitlik erişimi ve özel durumlar için kullanılır. Covering (index-only) ise ayrı bir konudur: aralık yeteneği katmaz, yalnız tabloya gitmeyi ortadan kaldırır.

# idx-2 | İleri
## Soru
`(sehir, dogum_tarihi)` üzerinde composite (bileşik) bir B-tree indeks var. Aşağıdaki sorgulardan hangisi bu indeksten **verimli faydalanamaz**?
## A
`WHERE sehir = 'Ankara' AND dogum_tarihi > '1990-01-01'` (iki sütun da koşulda)
🤖 Bu indeksin ideal kullanımıdır; önce `sehir`, sonra `dogum_tarihi` — indeks tam devrede.
## B
`WHERE sehir = 'Ankara'` (yalnız birinci sütun koşulda)
🤖 En-soldan önek (`sehir`) kullanıldığı için indeks verimli çalışır.
## C
`WHERE dogum_tarihi > '1990-01-01'` (yalnız ikinci sütun koşulda)
🤖 Doğru: en-soldan önek atlandığı için indeks bu sorguya verimli hizmet edemez.
## D
`WHERE sehir = 'Ankara' AND dogum_tarihi = '1990-05-01'` (iki sütun da eşitlikte)
🤖 Her iki sütun da en-soldan sırayla kullanıldığından indeks tam verimli çalışır.
## Doğru
C
## Açıklama
Bileşik indekste **en-soldan önek (leftmost prefix)** kuralı geçerlidir: `(sehir, dogum_tarihi)` indeksi, `sehir`'in **her zaman** koşulda bulunmasını gerektirir. İndeks önce `sehir`'e göre, `sehir` eşitken `dogum_tarihi`'ne göre sıralıdır. Bu yüzden `sehir = ...` ve `sehir = ... AND dogum_tarihi ...` sorguları verimlidir. Ancak `sehir` koşulu **yokken** yalnız `dogum_tarihi` sorgulanırsa, indeksin ilk düzeyi belirsiz kaldığından ağaç etkili biçimde daraltılamaz — planlayıcı çoğunlukla tam tarama seçer. Bu yüzden bileşik indekste **sütun sırası kritik bir tasarım kararıdır**: en sık ve tek başına sorgulanan sütunu başa koy.

# idx-3 | İleri
## Soru
Aşağıdaki sorgunun tabloya (heap) hiç gitmeden yalnız indeksten yanıtlanabilmesi (index-only scan) için ne gerekir?

```sql
SELECT eposta FROM kullanici WHERE sehir = 'Izmir';
```
## A
`sehir` üzerinde bir indeks yeterlidir; `eposta` her durumda indeksten okunur
🤖 Yalnız `sehir` indeksliyse `eposta` için tabloya gidilir; index-only olmaz.
## B
İndeks hem filtre hem çıktı sütununu içermeli: `(sehir, eposta)` covering indeks
🤖 Doğru: çıktı sütunu da indekste varsa DB heap'e gitmeden yanıtı üretir.
## C
Index-only scan yalnız birincil anahtar (primary key) sorgularında mümkündür
🤖 Herhangi bir covering indeks index-only scan sağlayabilir; PK ile sınırlı değildir.
## D
Hash indeks kurmak gerekir; index-only tarama yalnız hash ile desteklenir
🤖 Aksine, covering/index-only genelde B-tree ile yapılır; hash'e özgü değildir.
## Doğru
B
## Açıklama
**Index-only scan (covering index)**, sorgunun ihtiyaç duyduğu **her sütunun** (hem `WHERE` filtresi hem `SELECT` çıktısı) indekste bulunmasıyla mümkün olur. Burada `sehir` ile filtreleyip `eposta` döndürüyoruz; indeks yalnız `(sehir)` içeriyorsa DB eşleşen her satır için asıl tabloya (heap) gidip `eposta`'yı okur — bu ek erişim maliyetlidir. Ama indeks `(sehir, eposta)` veya `(sehir) INCLUDE (eposta)` şeklinde **çıktı sütununu da kapsıyorsa**, yanıt tamamen indeksten üretilir, heap'e hiç gidilmez — çok daha hızlı ve daha az I/O. Bedeli: indeks daha geniş olur (daha fazla yer, biraz daha yavaş yazma). Bu, okuma-yoğun sıcak sorgular için klasik bir optimizasyondur.

# idx-4 | İleri
## Soru
`cinsiyet` sütununda yalnız 2 değer var (kayıtların ~%50'si her biri). Bu sütuna indeks açtın ama planlayıcı `WHERE cinsiyet = 'K'` sorgusunda indeksi kullanmayıp full scan yapıyor. En olası sebep nedir?
## A
İndeks bozulmuştur; `REINDEX` ile yeniden oluşturmak sorunu ortadan kaldırır
🤖 Bozukluk değil; düşük seçicilikte full scan bilinçli ve genelde doğru bir karardır.
## B
Planlayıcıda bir hata vardır; bu sorgu her koşulda indeksi kullanmalıydı
🤖 Hata değil; maliyet-temelli planlayıcı doğru davranıyor, indeks burada pahalıdır.
## C
Seçicilik düşüktür; yarım tabloya rastgele erişim, sıralı tam taramadan pahalıdır
🤖 Doğru: düşük seçicilikte indeks fayda getirmez, planlayıcı maliyete göre tarar.
## D
Hash indeks açsaydın planlayıcı bu sorguda kesinlikle indeksi tercih ederdi
🤖 Sorun indeks türü değil, seçicilik; %50 eşleşmede her türde full scan daha ucuzdur.
## Doğru
C
## Açıklama
Bir indeksin faydası sütunun **seçiciliğine (selectivity)** bağlıdır: seçicilik, ayırt edici değer oranıdır. `eposta` gibi neredeyse benzersiz bir sütun çok seçicidir, indeks az sayıda satıra hızla götürür. `cinsiyet` gibi yalnız 2 değerli bir sütun **düşük seçiciliktedir**; `= 'K'` kayıtların ~yarısını döndürür. İndeksi kullanmak, eşleşen her satır için tabloda **rastgele erişim (random I/O)** demektir; bu kadar çok satır için rastgele erişimin toplamı, tabloyu baştan sona **sıralı okumaktan (sequential scan)** daha pahalıdır. Maliyet-temelli planlayıcı bunu istatistiklerden hesaplar ve doğru olarak **full scan** seçer. Ders: düşük seçicilikli sütunlara tek başına indeks açmak çoğu zaman boşa yer ve yazma cezasıdır. (Kısmi/partial index veya bileşik indeksin sonraki sütunu olarak değer kazanabilir.)

# idx-5 | Orta
## Soru
Bir tabloya çok sayıda indeks eklemenin gözden kaçan maliyeti nedir?
## A
İndeksler yalnız yer kaplar; performans üzerinde hiçbir olumsuz etkileri yoktur
🤖 Yerin ötesinde her yazma ilgili tüm indeksleri de güncellemek zorundadır.
## B
Her `INSERT/UPDATE/DELETE` tüm indeksleri de güncellediği için yazma yavaşlar
🤖 Doğru: indeks okumayı hızlandırır ama her yazmada bakım maliyeti doğurur.
## C
Okumaları da yavaşlatırlar; planlayıcı her sorguda hepsini taramak zorundadır
🤖 Fazla indeks planlama süresini biraz artırır; asıl ve büyük maliyet *yazma* tarafındadır.
## D
Yalnız `SELECT`leri etkilerler; yazma maliyeti indeks sayısından bağımsızdır
🤖 Tam tersi; yazma maliyeti doğrudan indeks sayısıyla birlikte artar.
## Doğru
B
## Açıklama
İndeks **okuma-yazma ödünleşimidir**: okumayı hızlandırır ama **bedava değildir**. Tabloya her satır eklendiğinde/güncellendiğinde/silindiğinde, o tablonun **her bir indeksi de** senkron olarak güncellenmelidir (B-tree'ye ekleme/silme, olası düğüm bölünmeleri). Dolayısıyla 8 indeksli bir tabloya `INSERT`, indekssiz tabloya göre çok daha pahalıdır — yazma-yoğun tablolarda bu ciddi bir yavaşlamadır. Ayrıca her indeks ek **depolama** tüketir ve bellekte önbellek yeri kaplar. Bu yüzden "ne olur ne olmaz her sütuna indeks" anti-desendir; yalnız gerçekten sorgulanan erişim desenlerini, ölçüp doğrulayarak indeksle. Kullanılmayan indeksleri (`pg_stat_user_indexes` vb. ile tespit edip) kaldırmak da bir bakım işidir.

# idx-6 | İleri
## Soru
Yavaş bir sorguyu teşhis ederken `EXPLAIN (ANALYZE)` çıktısında `Seq Scan on siparis (rows=2000000)` görüyorsun, oysa yalnız birkaç satır bekliyordun. Bu bulgu ne anlatır ve ilk adımın ne olmalıdır?
## A
`Seq Scan` her zaman en hızlı yoldur; bu plan zaten en iyisi, değişecek bir şey yok
🤖 Birkaç satır beklenen bir filtre için milyonlarca satırı taramak pahalı bir yoldur.
## B
`Seq Scan`, indeks bozukken görülen bir uyarıdır; `REINDEX` sonrası kendiliğinden kaybolur
🤖 `Seq Scan` bir uyarı değil, tabloyu baştan sona okuma erişim yöntemidir.
## C
`ANALYZE` çıktısı yalnız tahmini satır sayısını verir; gerçek çalışmayla ilgisi yoktur
🤖 `ANALYZE` ile plan *gerçekten* çalıştırılır; gerçek satır ve süre görünür.
## D
Uygun indeks yok ya da kullanılamıyor; indeks, seçicilik ve sargability incelenmelidir
🤖 Doğru: Seq Scan + yüksek satır çoğunlukla eksik/kullanılamayan indekse işaret eder.
## Doğru
D
## Açıklama
`EXPLAIN (ANALYZE)`, planlayıcının seçtiği **erişim yolunu** ve `ANALYZE` ile **gerçek** satır/süreyi gösterir. `Seq Scan` (sequential scan), DB'nin tabloyu **baştan sona** okuduğu anlamına gelir; küçük tablolarda veya çok satır dönen sorgularda normaldir, ama **birkaç satır beklerken milyonlarca satırlık bir Seq Scan** genelde bir sorun işaretidir. Olası nedenler ve ilk adımlar: (1) `WHERE` sütununda **indeks yok** → uygun indeks ekle; (2) indeks var ama **seçicilik düşük** ya da istatistikler bayat → istatistikleri güncelle; (3) sorgu indeksi **engelliyor** — sütun üzerinde fonksiyon/tip dönüşümü (`WHERE UPPER(ad)=...` veya `WHERE metin_sutun = 123`) indeksi devre dışı bırakır; sorguyu sargable (indeks-dostu) hâle getir. Kısaca: EXPLAIN oku, sonra indeks/sorgu kararını ver — tahminle değil, planla.
