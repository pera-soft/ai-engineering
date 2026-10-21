---
slug: veri-bolumleme
baslik: "Veri Bölümleme (Partitioning/Sharding): Yatay/Dikey, Consistent Hashing, Hotspot ve Shard Key"
kademe: İleri
sira: 147
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uygulamanın verileri **veritabanı** denen programda, bir **sunucu** (hiç kapanmayan bilgisayar) üzerinde durur. Başlangıçta tek sunucu yeter. Ama veri büyür: 50 milyon kullanıcı, günde milyarlarca satır. Bir noktada tek makinenin diski, belleği ve işlemcisi yetmez. Daha güçlü makine almak (**dikey ölçekleme**) bir yere kadar gider. Sonrasında ne fiyatı ne de fiziği buna izin verir.

**Bölümleme (partitioning)**, veriyi parçalara ayırıp bu parçaları farklı sunuculara yerleştirmektir. Parçalar ayrı düğümlere dağıtıldığında bu işin yaygın adı **parçalamadır (sharding)**. Her parçaya **bölüm (shard)**, her sunucuya **düğüm (node)** denir. Böylece kapasiteyi makine ekleyerek büyütürsün; buna **yatay ölçekleme** denir.

Benzetme: kütüphane tek bir dev odada değil, harflere göre ayrılmış birçok odada tutulur. Aramanın hangi odaya gideceğini bilmen gerekir. O "hangi odaya" sorusunun cevabını veren kurala **bölüm anahtarı (shard key)** denir; bu da işin en kritik kararıdır.

### Temel kavramlar

- **Shard / düğüm**: verinin bir parçası ve onu barındıran sunucu.
- **Shard key**: bir kaydın hangi shard'a düşeceğini belirleyen sütun.
- **Aralık (range) bölümleme**: anahtarın değer aralığına göre ayırma (kullanıcı 1–1M shard A).
- **Özet (hash) bölümleme**: anahtarı bir özet fonksiyonundan geçirip düğüme eşleme. Dağılım düzgün olur, ama art arda gelen değerleri isteyen aralık sorgusu bütün düğümlere dağılır.
- **Dizin (directory) bölümleme**: ayrı bir arama tablosu, hangi anahtarın nerede olduğunu tutar. Esnektir; ama o tablo tek arıza noktası hâline gelebilir.
- **Sıcak nokta (hotspot)**: yükün belirli bir bölümde yığılması. Sürekli aynı anahtara vurulduğunda buna **sıcak anahtar (hot key)** denir.
- **Yeniden dengeleme (rebalancing)**: bölüm sayısı değişince veriyi düğümler arasında taşıma işi.
- **Kardinalite (cardinality)**: bir alanın kaç farklı değer alabildiği. `kullanici_id` yüksek kardinalitelidir, `aktif_mi` ise yalnız iki değerlidir.

### Nasıl çalışır?

Özet bölümlemenin en basit hâli `hash(anahtar) % düğüm_sayısı` formülüdür. Basittir ama **kırılgandır**. Bir düğüm eklenip çıkınca `N` değişir; modül aritmetiği de neredeyse **tüm** anahtarların eşlemesini bozar. Dağıtık önbellekte bu, önbelleğin tamamının ıskalanması demektir. Bölümlemede ise devasa bir veri taşıma dalgası demektir.

**Tutarlı özetleme (consistent hashing)** bunu çözer. Hem anahtarlar hem düğümler aynı özet halkasına (0..2^m) yerleştirilir. Bir anahtar, halkada saat yönünde rastladığı ilk düğüme aittir. Düğüm eklenince ya da çıkınca yalnız o düğümün komşu yayındaki anahtarlar taşınır; bu da toplamın yaklaşık 1/N'i, yani ~K/N kadardır. Geri kalan her şey yerinde kalır. Amaç güvenlik sağlamak ya da çakışmayı yok etmek değildir; amaç **taşınan veri miktarını en aza indirmektir**.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="mod-N hashing ve consistent hashing yeniden dağıtım farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Düğüm eklenince ne kadar veri taşınır?</text>
  <rect x="28" y="44" width="272" height="150" rx="12" fill="#FBEEE6" stroke="#C25A2E" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#C25A2E" font-family="system-ui">hash(key) % N</text>
  <text x="164" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">N değişince modül değişir</text>
  <text x="164" y="120" text-anchor="middle" font-size="11" fill="#B23A48" font-family="system-ui">neredeyse TÜM anahtarlar taşınır</text>
  <text x="164" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">devasa yeniden dağıtım dalgası</text>
  <text x="164" y="166" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">cache/shard için yıkıcı</text>
  <rect x="340" y="44" width="272" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Consistent hashing</text>
  <text x="476" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">anahtar+düğüm bir halkada</text>
  <text x="476" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">yalnız komşu anahtarlar taşınır</text>
  <text x="476" y="142" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">~K/N anahtar hareket eder</text>
  <text x="476" y="166" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">virtual node ile dağılım düzelir</text>
</svg>
```

Az sayıda fiziksel düğüm halkaya birer noktayla konursa dağılım çarpık olur. Bazı düğüme büyük bir yay, bazısına küçük bir yay düşer. Üstelik bir düğüm çökünce **tüm** yükü tek bir komşusuna biner.

Çözüm **sanal düğümdür (virtual node / vnode)**: her fiziksel düğüm halkada birçok noktaya, örneğin 128–256 noktaya yerleştirilir. Böylece yay parçaları çok sayıda küçük dilime bölünür ve dağılım istatistiksel olarak düzgünleşir. Bir düğüm eklenince ya da çıkınca yükü tek bir komşu değil, **tüm düğümler** dengeli biçimde devralır. **Farklı güçteki sunucuları (heterojen kapasite)** yönetmek de kolaylaşır: güçlü sunucuya daha çok sanal düğüm verirsin. Sanal düğüm fiziksel makine sayısını azaltmaz ve aralık sorgusunu hızlandırmaz; tek işi yük dağılımını düzeltmektir.

Bölmenin ekseni de üç türlüdür.

**Yatay (horizontal / sharding)**: aynı şemaya sahip **satırlar** düğümlere dağıtılır. **Dikey (vertical)**: **sütunlar** ayrılır. Sık erişilen dar sütunlar bir depoda durur; büyük ve nadir kullanılan sütunlar (örneğin `avatar_blob`) ayrı bir depoya taşınır. Böylece sık koşan sorgular küçük satırlar üzerinde çalışır ve hızlanır. **Fonksiyonel**: veri, varlığa ya da özelliğe göre ayrı veritabanlarına konur (siparişler ↔ kullanıcılar). Mikroservislerdeki "her servise kendi veritabanı" ilkesinin temeli budur.

```svg
<svg viewBox="0 0 640 226" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yatay (satır) ve dikey (sütun) bölümleme karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yatay (satır) vs Dikey (sütun) bölme</text>
  <rect x="20" y="40" width="290" height="172" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="64" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Yatay / Sharding — satırları böl</text>
  <rect x="48" y="80" width="234" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="165" y="101" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Shard A</text>
  <text x="165" y="121" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanıcı 1 – 1M</text>
  <rect x="48" y="142" width="234" height="52" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="165" y="163" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Shard B</text>
  <text x="165" y="183" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanıcı 1M – 2M</text>
  <rect x="330" y="40" width="290" height="172" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="64" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Dikey — sütunları böl</text>
  <rect x="352" y="80" width="122" height="114" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="413" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Sık sütunlar</text>
  <text x="413" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">id, ad, email</text>
  <text x="413" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">küçük, sıcak</text>
  <rect x="484" y="80" width="122" height="114" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="545" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Büyük sütunlar</text>
  <text x="545" y="126" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">avatar_blob</text>
  <text x="545" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">nadir erişilen</text>
</svg>
```

**Bölüm anahtarı** seçimi iki niteliği birlikte ister. Birincisi **eşit dağılımdır**: anahtar yüksek kardinaliteli ve dengeli değerlere sahip olmalı ki yük bölümlere yayılsın. İkincisi **sorgu yerelliğidir**: en yaygın sorgu **tek bölümün içinde** kalmalı. Aksi hâlde her sorgu tüm bölümlere dağılıp sonuçları toplamak zorunda kalır; buna pahalı **saç-topla (scatter-gather)** sorgusu denir.

Kötü seçimin sonucu **sıcak noktadır**. Üç klasik sebebi vardır. (a) **Çarpık erişim**: bir "ünlü kullanıcı" (celebrity problem) ya da viral içerik kendi bölümüne orantısız trafik çeker. (b) **Sürekli artan anahtar**: otomatik artan id ya da zaman damgası, tüm yeni yazmaları hep "son" bölüme yığar (yazma sıcak noktası). (c) **Düşük kardinaliteli anahtar**: iki değerli bir durum alanı gibi.

Etkisi ağırdır: bir bölüm doygunlukta boğulurken diğerleri boşta kalır ve yatay ölçeklemenin faydası kaybolur. Tutarlı özetleme genel dağılımı düzeltir; ama **tek bir sıcak anahtarı** çözmez. Onun için anahtarı yapay olarak dağıtmak (salting / key splitting), sıcak varlığı ayrı ele almak ya da önbelleğe almak gerekir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hotspot: yükün tek shard'da yığılması, diğer shard'ların boşta kalması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hotspot: yük tek shard'da yığılır</text>
  <line x1="40" y1="176" x2="600" y2="176" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="90" y="56" width="90" height="120" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="135" y="118" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">~%100</text>
  <text x="135" y="194" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Shard A</text>
  <text x="135" y="41" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">ünlü / monoton key</text>
  <rect x="280" y="140" width="90" height="36" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="325" y="163" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">%15</text>
  <text x="325" y="194" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Shard B</text>
  <rect x="470" y="140" width="90" height="36" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="515" y="163" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">%12</text>
  <text x="515" y="194" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Shard C</text>
</svg>
```

Son olarak **yeniden bölümleme (resharding)** zor bir iştir. Üç sebebi vardır.

(1) **Tutarlılık**: taşıma sürerken bir anahtar hem eski hem yeni bölümde bulunabilir. Okuma ve yazmanın doğru yere gitmesi gerekir. Bu yüzden genelde bir çift yazma (dual-write) ve geçiş (cutover) dönemi yönetilir. (2) **Kesintisizlik**: iş canlı trafik altında, kullanıcıya kesinti vermeden yapılmalıdır. (3) **G/Ç ve trafik dalgası**: büyük veri kopyalamak disk ile ağı doyurur, ama bu sırada normal üretim yükü de karşılanmalıdır.

Bölüm sayısı arttığında anahtar-bölüm eşlemesi değişir; bu yüzden bölüm anahtarı aynı kalsa bile veri taşınır. Tutarlı özetleme bu maliyeti çok azaltır ama sıfırlamaz. Bu nedenle işe bol sayıda sanal düğüm ya da bölümle başlamak tercih edilir.

### Mülakatta ne sorulur?

- **Yatay ve dikey bölümleme farkı?** Yatay satırları düğümlere dağıtır, dikey sütun gruplarını ayrı depolara ayırır. İkisi de hem SQL hem NoSQL'de mümkündür.
- **Mesajlaşmada bölüm anahtarı ne olmalı?** `sohbet_id` gibi yüksek kardinaliteli bir anahtar. Bu anahtar bir sohbetin bütün mesajlarını tek bölümde toplar. İki değerli bir durum alanı, sürekli artan bir id ya da `ulke` kötü seçimlerdir.
- **Neden `% N` değil, tutarlı özetleme?** `% N` kullanırsan düğüm sayısı değişince neredeyse tüm anahtarlar taşınır. Halkada ise yalnız ~K/N kadarı taşınır.
- **Sanal düğüm (vnode) neden var?** Yükü daha eşit dağıtmak, çıkan düğümün yükünü tek komşuya değil tüm düğümlere yaymak ve farklı güçteki sunucuları dengelemek için.
- **Sıcak nokta nedir?** Yükün tek bölümde yığılmasıdır. Sebebi ünlü kullanıcı, sürekli artan anahtar ya da düşük kardinalitedir. Sorguların tüm bölümlere dağılması ise ayrı bir sorundur (saç-topla).
- **Canlıda yeniden bölümleme neden zor?** Tutarlılık, kesintisizlik ve G/Ç dalgası yüzünden. "Bedava ve anlık" değildir; ama imkânsız da değildir.

### Tek cümlede

Bölümleme, tek makineye sığmayan veriyi ve yükü birçok düğüme yayarak yatay ölçeklenmeyi mümkün kılar. Başarısı ise neredeyse tümüyle doğru bölüm anahtarı seçimine bağlıdır.

# part-1 | Orta
## Soru
Yatay (horizontal) ve dikey (vertical) bölümlemenin farkı nedir?
## A
Yatay sütunları, dikey ise satırları böler; isimler bu eksenlerden gelir
🤖 Ters; yatay *satırları*, dikey *sütunları* böler.
## B
İkisi aynı şeydir; yalnız farklı satıcıların kullandığı terimlerden ibarettir
🤖 Kavramsal olarak farklıdırlar: biri satır, diğeri sütun ekseninde böler.
## C
Yatay yalnız NoSQL'de, dikey ise yalnız ilişkisel veritabanlarında mümkündür
🤖 İkisi de her iki dünyada uygulanabilir; teknolojiye bağlı bir ayrım değildir.
## D
Yatay satırları düğümlere dağıtır; dikey sütun gruplarını ayrı depolara ayırır
🤖 Doğru: yatay = satır ekseni (sharding), dikey = sütun ekseni.
## Doğru
D
## Açıklama
**Yatay bölümleme (horizontal / sharding)** tabloyu **satır ekseninde** böler: aynı şemaya sahip satır kümeleri farklı düğümlere dağıtılır (örn. kullanıcı id 1–1M shard A'da, 1M–2M shard B'de). Amaç, tek düğüme sığmayan hacmi ve yükü yatay ölçeklemektir. **Dikey bölümleme (vertical)** ise **sütun ekseninde** böler: sık erişilen dar sütunlar bir depoda, büyük/nadir kullanılan sütunlar (örn. `profil_foto_blob`, uzun metin) ayrı bir depoda tutulur — böylece sıcak sorgular küçük satırlarla hızlanır. Üçüncü bir yaklaşım **fonksiyonel bölümlemedir**: veriyi varlığa/özelliğe göre ayrı veritabanlarına koymak (siparişler ↔ kullanıcılar), mikroservislerin "her servise kendi veritabanı" ilkesinin temeli. Hiçbiri belirli bir veritabanı ailesine özgü değildir.

# part-2 | İleri
## Soru
Bir mesajlaşma uygulamasında mesajları shard'lamak için shard key seçiyorsun. Aşağıdakilerden hangisi en iyi seçimdir?
## A
`sohbet_id`; yüksek kardinaliteli ve bir sohbeti tek shard'da toplayan bir anahtar
🤖 Doğru: eşit dağılım + sorgu yerelliği ideal shard key'in iki temel niteliğidir.
## B
`mesaj_durumu` (okundu/okunmadı); yalnız iki değer aldığı için yönetimi kolaydır
🤖 Çok düşük kardinalite; tüm veri iki shard'a yığılır, dengesiz ve hotspot'lu olur.
## C
Monoton artan `mesaj_id`; sıralı olması dağıtımı öngörülebilir ve düzgün kılar
🤖 Monoton anahtar tüm yeni yazmaları hep son shard'a yığar (write hotspot).
## D
`ulke`; değer sayısı az ve sabit olduğu için shard yönetimini basitleştirir
🤖 Az değer + dengesiz nüfus (bir ülke çok büyük) hotspot ve düşük kardinalite doğurur.
## Doğru
A
## Açıklama
İyi bir shard key iki niteliği birlikte taşır: (1) **eşit dağılım** — yükü shard'lara dengeli yayar (yüksek kardinalite, dengeli değer dağılımı); (2) **sorgu yerelliği** — en yaygın sorguyu **tek shard içinde** tutar ki pahalı çapraz-shard (scatter-gather) sorgusu gerekmesin. Mesajlaşmada `sohbet_id` iyi bir adaydır: bir sohbetin tüm mesajları aynı shard'a düşer (o sohbeti okumak tek shard'a gider) ve çok sayıda sohbet olduğundan yük dağılır. `mesaj_durumu` (2 değer) düşük kardinaliteyle dengesizdir; monoton `mesaj_id` tüm yeni yazmaları **son shard'a** yığarak yazma hotspot'u yaratır; `ulke` hem az değerli hem dengesiz nüfusludur. Shard key'i sonradan değiştirmek çok pahalıdır, bu yüzden erken ve dikkatli verilen bir karardır.

# part-3 | İleri
## Soru
Düz `hash(key) % N` yerine consistent hashing (tutarlı hash) tercih etmenin temel sebebi nedir?
## A
Consistent hashing hash çakışmalarını (collision) tümüyle ortadan kaldırır
🤖 Amaç çakışma değil; mesele düğüm sayısı değişince taşınan anahtar miktarıdır.
## B
Eşitlik sorgularını aralık sorgularına dönüştürüp tarama yapılmasını sağlar
🤖 Sorgu türüyle ilgisi yok; konu anahtar-düğüm eşlemesinin kararlılığıdır.
## C
`% N`'de N değişince tüm anahtarlar taşınır; halkada yalnız ~K/N anahtar taşınır
🤖 Doğru: asıl kazanç, düğüm eklenip çıktığında minimum veri taşımadır.
## D
Her shard'ın tam olarak aynı miktarda veri tutmasını matematiksel olarak garanti eder
🤖 Böyle bir garanti yok; dağılımı düzeltmek için ayrıca sanal düğüm gerekir.
## Doğru
C
## Açıklama
`hash(key) % N` basittir ama **kırılgandır**: bir düğüm eklenip çıkınca `N` değişir, modül aritmetiği neredeyse **tüm anahtarların** düğüm eşlemesini değiştirir — dağıtık cache'te bu tüm önbelleğin ıskalanması (mass miss), shard'da ise devasa bir veri taşıma dalgası demektir. **Consistent hashing**, hem anahtarları hem düğümleri aynı hash halkasına (0..2^m) yerleştirir; bir anahtar, halkada saat yönünde rastladığı ilk düğüme aittir. Düğüm eklenince/çıkınca yalnız o düğümün halkadaki komşu yayındaki anahtarlar (~toplamın 1/N'i, yani ~K/N) taşınır, geri kalanı yerinde kalır. Bu yüzden dinamik olarak büyüyen/küçülen dağıtık sistemlerin (Cassandra, DynamoDB, dağıtık cache'ler) temel tekniğidir. Mükemmel eşit dağılımı garanti etmez — bunun için sanal düğüm kullanılır.

# part-4 | İleri
## Soru
Consistent hashing halkasında "sanal düğüm (virtual node / vnode)" kullanmanın amacı nedir?
## A
Aynı anahtarın birden çok kopyasını (replica) tutmayı zorunlu hâle getirmek
🤖 vnode bir çoğaltma (replication) mekanizması değildir; konu yük dağılımının düzgünlüğüdür.
## B
Her fiziksel düğümü halkada birçok noktaya koyup yükü daha eşit dağıtmak
🤖 Doğru: çok sayıda vnode dağılımı düzeltir ve yükü tüm düğümlere dengeli yayar.
## C
Fiziksel düğüm sayısını azaltmak; bir vnode bir sunucunun yerini tutar
🤖 vnode fiziksel düğüm sayısını değiştirmez; yalnız halkadaki temsilini çoğaltır.
## D
Halka üzerinde aralık (range) sorgularını mümkün kılmak ve tarama açmak
🤖 vnode aralık sorgusuyla ilgili değil; amaç yük dengeleme ve düzgün yeniden dağıtımdır.
## Doğru
B
## Açıklama
Az sayıda fiziksel düğüm halkaya birer noktayla konursa dağılım **çarpık** olur: bazı düğümlere halkanın büyük bir yayı, bazılarına küçük bir yayı düşer — yük dengesiz olur. Ayrıca bir düğüm çökünce onun **tüm** yükü tek bir komşuya biner. **Sanal düğüm (vnode)** çözümü: her fiziksel düğümü halkada **birçok** noktaya (örn. 128–256 sanal nokta) yerleştirmek. Böylece (1) yay parçaları çok sayıda küçük dilime bölünür, dağılım istatistiksel olarak düzgünleşir; (2) bir düğüm eklenince/çıkınca yükü **tek bir komşu değil, tüm düğümler** dengeli olarak devralır; (3) **heterojen kapasite** kolay yönetilir — güçlü sunucuya daha çok vnode vererek orantılı yük verilir. Cassandra ve benzeri sistemler tam da bu yüzden vnode kullanır.

# part-5 | İleri
## Soru
Sharding'de "hotspot" (sıcak nokta) sorununun tipik bir sebebi ve etkisi nedir?
## A
Trafiğin belirli bir shard'da yığılması; o shard boğulurken diğerleri boşta kalır
🤖 Doğru: dengesiz erişim tek shard'ı doyurur, dağıtımın avantajını yok eder.
## B
Tüm shard'ların eşit ve düşük yük alması; sistemin gereğinden fazla soğuk çalışması
🤖 "Sıcak nokta" tam tersidir; sorun eşitlik değil, *dengesiz* ve aşırı yüklü bir shard'dır.
## C
Her sorgunun tüm shard'lara dağılması (scatter-gather) ve gecikmenin artması
🤖 Bu ayrı bir sorun — çapraz-shard sorgu maliyeti; hotspot yük dağılımı dengesizliğidir.
## D
İndekslerin bellekte fazla yer kaplaması; shard başına belleğin yetmez hâle gelmesi
🤖 Bu bir indeks/bellek sorunudur; hotspot ise erişim ve veri dağılımı sorunudur.
## Doğru
A
## Açıklama
**Hotspot (sıcak nokta)**, yükün veya verinin shard'lara **dengesiz** dağılıp belirli bir shard'da yığılmasıdır. Tipik sebepler: (1) **çarpık erişim** — bir "ünlü kullanıcı" (celebrity problem) veya viral içerik, kendi shard'ına orantısız trafik çeker; (2) **monoton artan shard key** — auto-increment id veya zaman damgası, tüm yeni yazmaları hep "son" shard'a yığar (write hotspot); (3) düşük kardinaliteli anahtar. Etkisi: bir shard %100 doygunlukta boğulurken diğerleri boşta kalır — yatay ölçeklemenin tüm avantajı kaybolur, o shard tüm sistemin darboğazı olur. Consistent hashing genel dağılımı düzeltir ama **tek bir sıcak anahtarı** çözmez; bunun için anahtarı yapay olarak dağıtma (salting / key splitting), sıcak varlığı ayrı ele alma veya önbellekleme gibi ek teknikler gerekir. Doğru shard key seçimi hotspot'ı baştan azaltmanın en etkili yoludur.

# part-6 | İleri
## Soru
Canlı bir sistemde shard sayısını artırmak (resharding / yeniden dengeleme) neden zordur?
## A
Yeni shard'lar boş başlar; eski veri hiç taşınmaz, yalnız yeni yazmalar oraya gider
🤖 Shard sayısı değişince eşleme de değişir; mevcut anahtarların bir kısmı taşınmak zorundadır.
## B
Modern veritabanları resharding'i tamamen bedava ve anlık yapar; planlanacak bir şey yoktur
🤖 Aksine; büyük veri taşımak I/O, tutarlılık ve kesinti riski taşır, bedava değildir.
## C
Shard key değişmediği sürece hiçbir veri düğümler arasında taşınmaz; eşleme sabit kalır
🤖 Shard sayısını artırmak, key aynı kalsa bile anahtar-shard eşlemesini değiştirir.
## D
Canlı veriyi taşırken tutarlılık, kesintisizlik ve I/O dalgası birlikte yönetilmelidir
🤖 Doğru: canlı veri taşımanın tutarlılık, kesintisizlik ve I/O yükü zorlukları vardır.
## Doğru
D
## Açıklama
Resharding (yeniden dengeleme), çalışan bir sistemde veriyi düğümler arasında **fiziksel olarak taşımayı** gerektirir ve bu birkaç sebeple zordur: (1) **Tutarlılık** — taşıma sürerken anahtar hem eski hem yeni shard'da geçici olarak bulunabilir; okuma/yazma doğru yere gitmeli, çoğu zaman bir **çift-yazma (dual-write) / geçiş (cutover)** dönemi yönetilir. (2) **Kesintisizlik** — taşıma canlı trafik altında, kullanıcıya kesinti vermeden yapılmalı. (3) **I/O ve trafik dalgası** — büyük veri kopyalamak disk/ağı doyurur, aynı anda üretim yükünü de karşılamak gerekir. **Consistent hashing** taşınacak veri miktarını `hash % N`'e göre çok azaltır (yalnız ~K/N anahtar), böylece bu operasyonu **çok daha ucuz** kılar ama **sıfırlamaz** — yine planlı, kademeli ve izlenen bir süreç gerekir. Bu yüzden shard sayısını ve stratejisini baştan büyümeye göre tasarlamak (örn. bol vnode / yeterince partition ile başlamak) tercih edilir.
