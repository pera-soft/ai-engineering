---
slug: redis-veri-yapilari
baslik: "Redis Veri Yapıları: Doğru Yapıyı Seçmek, Encoding ve Big Key Tuzağı"
kademe: Orta
sira: 156
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Redis'te veriyi bir **anahtarın (key)** altına koyarsın. Ama o anahtarın altında ne durduğu senin seçimindir: düz bir metin mi, bir liste mi, bir küme mi, yoksa skora göre sıralı bir tablo mu?

Redis'i basit bir "ad→değer sözlüğünden" ayıran şey budur: hazır **veri yapıları (data structures)** sunar. **Veri yapısı**, verinin bellekte nasıl düzenlendiğidir; hangi işlemin ucuz olacağını da o düzen belirler. Aynı 1000 sayıyı düşün. Listede tutarsan "sıraya diz" işini ucuza yaparsın. Kümede tutarsan "tekrarı engelle" işini. Sıralı kümede tutarsan "skora göre sırala" işini.

Bu yüzden doğru yapıyı seçmek bir performans ayarı değil, bir tasarım kararıdır. Yanlış yapı seçen kişi yine doğru sonucu alır; ama on kat bellek ve yüz kat süre öder.

### Temel kavramlar

- **Anahtar (key)**: verinin adı — `kullanici:42`. **Üye (member)** / **alan (field)**: yapının içindeki tek tek parçalar.
- **O(1)**: veri büyüse de süre sabittir. **O(log N)**: süre çok yavaş büyür. **O(N)**: süre veri kadar büyür. Tehlikeli olan budur; çünkü tek iş parçacığını uzun süre meşgul eder.
- **Kardinalite**: bir kümedeki **benzersiz** eleman sayısı ("kaç farklı ziyaretçi?").
- **Encoding (iç gösterim)**: Redis'in aynı yapıyı bellekte tutmak için seçtiği fiziksel biçim.
- **Big key**: tek anahtar altında çok büyümüş (yüz binlerce/milyonlarca üyeli) yapı.

### Nasıl çalışır?

Temel repertuar ve tipik işi:

- **String**: metin, sayı, JSON, ham byte. Sayaç için atomik `INCR`; süreli yazma `SET k v EX 60`.
- **Hash**: tek anahtar altında **alan-değer** çiftleri tutar; yani bir nesnenin alanlarını. Örnek: `HSET user:1 ad Ali yas 30`. Tek bir alanı `HSET user:1 yas 31` ile, diğerlerine hiç dokunmadan güncellersin. Aynı veriyi String içinde JSON olarak tutarsan, tek bir alan için tüm belgeyi okuyup yeniden yazman gerekir.
- **List**: sıralı, iki uçlu; kuyruk ya da yığın (`LPUSH`, `RPOP`). Ekleme sırasını tutar, skora göre sıralamaz.
- **Set**: sırasız, **benzersiz** üyeler; kesişim/birleşim (`SADD`, `SINTER`).
- **Sorted Set (Sıralı Küme / ZSet)**: her üyeye bir **skor** bağlar ve üyeleri bu skora göre sıralı tutar. Skor tablosunun (leaderboard) doğal aracıdır. `ZADD board 1500 ali` komutu O(log N) çalışır; `ZREVRANGE board 0 9 WITHSCORES` ile ilk 10'u alırsın. İçeride iki yapı birlikte çalışır: hashtable (üye→skor, O(1)) ve skiplist (sıralı aralık sorgusu).
- **Stream**: sona eklenen (append-only) olay günlüğü, tüketici grupları (consumer group) ile.
- **Bitmap**: kullanıcı kimliğini bit konumu olarak kullanır; kişi başına yalnız **1 bit** harcar. `SETBIT active:2026-07-24 1234 1` ile işaretlersin, `BITCOUNT` ile sayarsın, `BITOP AND` ile iki günü kesiştirirsin. Tek koşulu vardır: kimlikler yoğun ve küçük tam sayılar olmalı. Seyrek kimliklerde devasa boşluk oluşur.
- **HyperLogLog**: **yaklaşık** benzersiz sayım yapar. Üyeleri saklamaz; kardinaliteyi yaklaşık **%0,81** standart hatayla tahmin eder. Üye sayısından bağımsız olarak yaklaşık **12 KB** yer tutar (`PFADD`, `PFCOUNT`, `PFMERGE`). Set kesin sayar; ama 100 milyon üyeyi saklamak gigabaytlar demektir.
- **Geo**: koordinat ve yarıçap sorgusu (`GEOADD`, `GEOSEARCH`).

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İhtiyaca göre veri yapısı seçimi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İhtiyaç → doğru yapı</text>
  <rect x="24" y="44" width="185" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="116" y="66" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Skor tablosu (sıralı)</text>
  <text x="116" y="82" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">→ Sorted Set</text>
  <rect x="227" y="44" width="185" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="319" y="66" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Nesne alanları</text>
  <text x="319" y="82" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">→ Hash</text>
  <rect x="430" y="44" width="185" height="46" rx="9" fill="#FBEDD9" stroke="#B26B0E" stroke-width="1.3"/>
  <text x="522" y="66" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Benzersiz sayım (yaklaşık)</text>
  <text x="522" y="82" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B0E" font-family="system-ui">→ HyperLogLog</text>
  <rect x="24" y="104" width="185" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="116" y="126" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Günlük aktif (bit)</text>
  <text x="116" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">→ Bitmap</text>
  <rect x="227" y="104" width="185" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="319" y="126" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Olay akışı / log</text>
  <text x="319" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">→ Stream</text>
  <rect x="430" y="104" width="185" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="522" y="126" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Yakındaki konumlar</text>
  <text x="522" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">→ Geo</text>
  <rect x="120" y="166" width="400" height="34" rx="9" fill="#FBE9E7" stroke="#B23B2E" stroke-width="1.3"/>
  <text x="320" y="188" text-anchor="middle" font-size="11.5" fill="#B23B2E" font-family="system-ui">Tuzak: tek anahtarda milyonlarca üye = big key</text>
</svg>
```

**İç gösterim (encoding): her yapının iki hâli.** Bir koleksiyon küçükken Redis onu bellek dostu, kompakt bir biçimde tutar. Bu biçimler şunlardır: **listpack** (küçük hash/list/zset), **intset** (yalnız tam sayı içeren küçük set), **embstr** (kısa metin).

`hash-max-listpack-entries`, `set-max-intset-entries` ve `zset-max-listpack-entries` gibi eşikler aşılınca yapı otomatik olarak **hashtable / skiplist** biçimine döner. Böylece büyük boyutta O(1) ve O(log N) korunur; karşılığında daha çok bellek harcanır. Gerçek biçimi `OBJECT ENCODING key` gösterir; dönen değer tipe ve boyuta göre `listpack`, `intset`, `skiplist`, `embstr` ya da `raw` olabilir. Önemli ayrıntı: bu geçiş pratikte **tek yönlüdür**. Koleksiyon sonradan küçülse bile genelde kompakt biçime geri dönmez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Küçük koleksiyon kompakt encoding, eşik aşılınca tam yapıya döner">
  <defs>
    <marker id="rdata-enc-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B26B00"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Encoding: eşik aşılınca tam yapıya döner</text>
  <rect x="34" y="60" width="200" height="96" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="134" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Küçük koleksiyon</text>
  <text x="134" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">listpack / intset</text>
  <text x="134" y="128" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">embstr</text>
  <text x="134" y="147" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bellek dostu, kompakt</text>
  <line x1="240" y1="108" x2="398" y2="108" stroke="#B26B00" stroke-width="1.8" marker-end="url(#rdata-enc-ok)"/>
  <text x="319" y="98" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">eşik aşıldı</text>
  <text x="319" y="126" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">hash-max-listpack-entries</text>
  <rect x="404" y="60" width="202" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="505" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Tam yapı</text>
  <text x="505" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">hashtable / skiplist</text>
  <text x="505" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">O(1) / O(log N) korunur</text>
  <text x="505" y="147" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">daha çok bellek</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Geçiş tek yönlü: küçülünce genelde kompakta geri DÖNMEZ</text>
</svg>
```

**Büyük anahtar (big key) tuzağı.** En sık görülen kıdemli hatası, tek bir anahtarda milyonlarca üye biriktirmektir. Üç bela getirir.

(1) Cluster'da bir anahtar **tek yuvaya ve tek düğüme** gider; otomatik bölünmez. O düğüm sıcak nokta (hot spot) olur ve yük dengesizleşir. (2) Üzerinde çalışan O(N) komutlar ve büyük bir `DEL` tek komut iş parçacığını uzun süre tıkar. (3) Kopyalamada ve bellek kullanımında dalgalanma yaratır.

Çaresi dört maddedir: anahtarı alanlara ya da kovalara bölmek, silmeyi arka plana atan **`UNLINK`** (lazy-free) kullanmak, `redis-cli --bigkeys` ile taramak ve okumayı `HSCAN`/`SSCAN`/`ZSCAN` ile parçalara ayırmak.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Big key tek slota sıkışıp o nodeda sıcak nokta oluşturur">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Big key = tek node'da sıcak nokta</text>
  <rect x="40" y="56" width="150" height="120" rx="10" fill="#F6E4E4" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="115" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Node A (slot 42)</text>
  <text x="115" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek anahtar:</text>
  <text x="115" y="122" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">milyonlarca üye</text>
  <text x="115" y="146" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">O(N) + DEL bloke eder</text>
  <text x="115" y="164" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">yük ağır ⚠</text>
  <rect x="245" y="76" width="150" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="320" y="104" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Node B</text>
  <text x="320" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">boşta / az yük</text>
  <rect x="450" y="76" width="150" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="525" y="104" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Node C</text>
  <text x="525" y="128" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">boşta / az yük</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Çözüm: alanlara böl (shard) · UNLINK/lazy-free · redis-cli --bigkeys</text>
</svg>
```

### Mülakatta ne sorulur?

- "Skor tablosunu neyle tutarsın?" → Sorted Set. List sırayı, Hash alanı tutar; ikisi de skora göre sıralamaz, Set zaten sırasızdır.
- "Nesnenin tek alanını güncelleyeceğim" → Hash. String'e JSON koymak her değişiklikte oku-yaz döngüsü demektir.
- "100 milyon tekil ziyaretçiyi minik bellekte say" → HyperLogLog (yaklaşık, ~12 KB). Set kesin sayar ama gigabaytlarca yer ister.
- "İki günün ortak aktif kullanıcısı?" → Bitmap + `BITOP AND`. HyperLogLog yalnız sayar, kesişim yapmaz.
- "Encoding değişir mi?" → Evet; eşik aşılınca kompakt biçimden tam yapıya geçer ve bu geçiş tek yönlüdür.
- "Büyük anahtar neden kötü?" → Tek yuvaya sıkışır; üzerindeki O(N) komutlar ve silme işlemi sunucuyu tıkar; yük dengesizleşir.

### Tek cümlede

Redis'te doğru veri yapısını seçmek bir tasarım kararıdır. Yapı hangi işlemin ucuz olacağını belirler. İç gösterim ne kadar bellek harcanacağını belirler. Anahtarın boyutu ise tek bir komutun herkesi bekletip bekletmeyeceğini belirler.

# rdata-1 | Temel
## Soru
Bir oyunda skor tablosu (leaderboard) tutacaksın: skora göre sıralı, O(log N) güncelleme ve "ilk 10" sorgusu istiyorsun. En uygun yapı?
## A
List
🤖 List ekleme sırasını tutar ama skora göre sıralamaz; "ilk 10 skor" O(N) sıralama gerektirir.
## B
Sorted Set (ZSet)
🤖 Doğru: her üyenin bir skoru var, skora göre sıralı; ZADD/ZREVRANGE ile O(log N).
## C
Hash
🤖 Hash alan-değer tutar ama sıralama sunmaz; ilk 10 için tüm alanları okuyup sıralaman gerekir.
## D
Set
🤖 Set benzersiz üyeler tutar ama SIRASIZDIR; skora göre sıralama veya aralık sorgusu yapamazsın.
## Doğru
B
## Açıklama
**Sorted Set (ZSet)** her üyeye bir **skor** bağlar ve üyeleri bu skora göre sıralı tutar. `ZADD board 1500 ali` ile ekleme O(log N), `ZREVRANGE board 0 9 WITHSCORES` ile ilk 10 O(log N + K)'dır — skor tablosunun doğal aracıdır. List (A) yalnız ekleme sırasını tutar, Hash (C) sıralama sunmaz, Set (D) sırasızdır. ZSet içte **hashtable + skiplist** birlikte tutar: hashtable üye→skor erişimini O(1), skiplist ise sıralı aralık sorgusunu sağlar.

# rdata-2 | Temel
## Soru
Bir kullanıcının profil alanlarını (ad, e-posta, yaş) tek anahtarda tutup **tek bir alanı** verimli güncellemek istiyorsun. En uygun yapı?
## A
String'e JSON serileştirmek
🤖 Çalışır ama tek alanı değiştirmek için tüm JSON'u okuyup yeniden yazman gerekir.
## B
List
🤖 List sıralı elemanlar içindir; alan adıyla erişim (ad→"Ali") sunmaz.
## C
Set
🤖 Set benzersiz üyeler tutar; alan-değer eşlemesi diye bir kavramı yoktur.
## D
Hash
🤖 Doğru: alan bazlı HGET/HSET ile tek alanı diğerlerine dokunmadan güncellersin.
## Doğru
D
## Açıklama
**Hash**, tek anahtar altında **alan-değer (field-value)** çiftleri tutar: `HSET user:1 ad Ali email a@x.com yas 30`. Tek alanı `HSET user:1 yas 31` ile diğerlerine dokunmadan güncellersin; String'e JSON koymak (A) ise her değişiklikte tüm belgeyi okuyup yazmayı zorunlu kılar. List (B) ve Set (C) alan-değer erişimi sunmaz. Küçük hash'ler bellek dostu **listpack** encoding'inde tutulduğu için Hash, nesne saklamanın hem hızlı hem ucuz yoludur.

# rdata-3 | Orta
## Soru
Bir sayfayı ziyaret eden ~100 milyon **benzersiz** kullanıcıyı, ufak bir hata payına razı olarak, sabit ve minik bellekte saymak istiyorsun. Hangisi?
## A
HyperLogLog
🤖 Doğru: ~%0,81 hatayla, üye sayısından bağımsız ~12 KB'de benzersiz sayım verir.
## B
Set
🤖 Kesin sayar ama her benzersiz üyeyi saklar; 100M üyede gigabaytlarca bellek yer.
## C
Bitmap
🤖 Yoğun tamsayı id'lerde iyidir ama rastgele/string id'lerde ve bu ölçekte uygun değildir.
## D
Sorted Set
🤖 Skorlu sıralama içindir; benzersiz sayım için gereksiz ağır ve büyüktür.
## Doğru
A
## Açıklama
**HyperLogLog** olasılıksal bir yapıdır: üyeleri saklamaz, yalnız **kardinaliteyi** (benzersiz sayıyı) ~**%0,81** standart hatayla tahmin eder ve üye sayısından bağımsız **~12 KB** kullanır (`PFADD visitors id`, `PFCOUNT visitors`). Set (B) kesin sayar ama her üyeyi tuttuğu için 100 milyonda bellek patlar. Bitmap (C) yalnız yoğun ve ardışık tamsayı id'lerde uygundur. Tam kesinlik gerekmeyen "kaç tekil ziyaretçi" sorusunun standart cevabı HLL'dir; iki HLL `PFMERGE` ile birleştirilebilir.

# rdata-4 | Orta
## Soru
Kullanıcı id'leri 1'den başlayan yoğun tamsayılar. "Bugün giriş yapanlar" kümesini kullanıcı başına 1 bit ile tutup, iki günün **kesişimini** (ikisinde de aktif) almak istiyorsun. Hangisi?
## A
HyperLogLog
🤖 Yalnız sayım verir; iki küme arasında bit düzeyi kesişim (AND) yapamazsın.
## B
Hash
🤖 Alan-değer tutar; milyonlarca kullanıcı için bit yoğunluğu sunmaz ve BITOP çalışmaz.
## C
Bitmap
🤖 Doğru: SETBIT ile işaretle, BITCOUNT ile say, BITOP AND ile iki günü kesiştir.
## D
Stream
🤖 Olay günlüğü içindir; kullanıcı başına bit sunmaz, kesişim işlemi yoktur.
## Doğru
C
## Açıklama
**Bitmap**, kullanıcı id'sini bit ofseti olarak kullanır: `SETBIT active:2026-07-24 1234 1`. Bir günün aktif sayısı `BITCOUNT`, iki günün kesişimi `BITOP AND dest gun1 gun2` ile alınır — kullanıcı başına yalnız 1 bit, yoğun id'lerde son derece verimli. HyperLogLog (A) sayar ama kesişim yapmaz, Hash (B) bit yoğunluğu sunmaz, Stream (D) olay akışıdır. Dikkat: id'ler seyrek veya çok büyükse bitmap devasa boşluk içerir; o durumda uygun değildir.

# rdata-5 | İleri
## Soru
Redis'te "encoding" (iç gösterim) hakkında hangisi doğrudur?
## A
Bir yapının encoding'i oluşturulurken belirlenir ve çalışma boyunca hiç değişmez
🤖 Koleksiyon büyüyünce encoding kompakt biçimden hashtable/skiplist'e döner.
## B
Küçük koleksiyonlar kompakt biçimde tutulur, eşik aşılınca tam yapıya dönüşür
🤖 Doğru: küçükken bellek için kompakt, büyüyünce performans için tam yapı.
## C
Encoding yalnız String tipinde vardır, koleksiyon tiplerinde böyle bir kavram yoktur
🤖 Hash/Set/ZSet/List hepsinin kompakt ve genişletilmiş encoding'leri vardır.
## D
`OBJECT ENCODING` komutu tip ne olursa olsun her zaman `raw` değerini döndürür
🤖 Yapıya ve boyuta göre `listpack`, `intset`, `skiplist`, `embstr` gibi değerler döner.
## Doğru
B
## Açıklama
Redis her yapıyı boyutuna göre farklı **encoding**'de saklar: küçük hash/list/zset **listpack**, yalnız tamsayı içeren küçük set **intset**, kısa string **embstr** — hepsi bellek için optimize edilmiştir. `hash-max-listpack-entries`, `set-max-intset-entries`, `zset-max-listpack-entries` gibi eşikler aşılınca yapı otomatik olarak **hashtable/skiplist**'e dönüşür ki büyük boyutta O(1)/O(log N) korunsun (A yanlış: değişir). Encoding tüm tiplerde vardır (C yanlış) ve gerçek değeri `OBJECT ENCODING key` gösterir (D yanlış). Bu geçiş tek yönlüdür: koleksiyon küçülse bile genelde kompakt biçime geri dönmez.

# rdata-6 | İleri
## Soru
Tek bir anahtarda milyonlarca üye tutan bir "big key" neden tehlikelidir?
## A
Yalnız fazladan disk alanı kullanır; bellek ve gecikme tarafında bir etkisi olmaz
🤖 Sorun disk değil; tek thread'i bloke etmesi ve cluster yükünü bozmasıdır.
## B
Redis onu arka planda otomatik olarak birden çok node'a bölüp dağıtır (auto-shard)
🤖 Böyle bir otomatik bölme yoktur; tek anahtar tek slot ve tek node'a sıkışır.
## C
Hiçbir sorun değildir; Redis her komutu anahtarın boyutundan tamamen bağımsız olarak O(1) çalıştırır
🤖 `HGETALL`/`SMEMBERS` ve silme O(N)'dir; büyük anahtarda çok uzun sürer.
## D
Tek slot'a sıkışır; O(N) komutlar ve silme tek thread'i uzun süre bloke eder
🤖 Doğru: sıcak nokta oluşturur ve büyük bir silme tüm sunucuyu dondurur.
## Doğru
D
## Açıklama
**Big key**, tek anahtarda çok sayıda üye barındıran yapıdır ve üç bela getirir: (1) Cluster'da bir anahtar tek **slot/node**'a gider, otomatik bölünmez (B yanlış) — o node **sıcak nokta** olur, yük dengesizleşir. (2) Üzerindeki O(N) komutlar (`HGETALL`, `SMEMBERS`) ve büyük bir `DEL` tek komut thread'ini uzun süre bloke eder (C yanlış). (3) Bellek ve replikasyon dalgalanması yaratır; sorun disk değildir (A yanlış). Çözüm: anahtarı alanlara göre parçalamak, silmeyi arka plana atan **`UNLINK`**/lazy-free kullanmak, `redis-cli --bigkeys` ile taramak.
