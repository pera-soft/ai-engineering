---
slug: sql-nosql-secim
baslik: "SQL vs NoSQL Seçimi: İlişkisel/KV/Doküman/Kolon/Graf, ACID vs BASE, Poliglot Persistence"
kademe: İleri
sira: 145
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uygulamanın sakladığı bilgiler (kullanıcılar, siparişler, ölçümler) kapanınca kaybolmasın diye **veritabanı** (database) denen özel bir programda tutulur. Veritabanı yalnız bir "dosya" değildir: veriyi düzenli saklar, aynı anda binlerce **istek** karşılar, arıza hâlinde veriyi korur ve sorgulara cevap verir.

Ama tek tip veritabanı yoktur. Doğru seçimi iki şey belirler. Birincisi verinin şeklidir: bir sipariş mi, bir sosyal ağ mı, saniyede yüz bin sensör ölçümü mü? İkincisi ona nasıl eriştiğindir: kimlikle mi, ilişkileri gezerek mi, zaman aralığıyla mı? Bu iki soru birbirinden çok farklı depolara götürür. Mülakatta "SQL mü NoSQL mü?" sorusunun doğru cevabı bu yüzden **"duruma göre"**dir. Senden beklenen, veri modelini ve **erişim desenini (access pattern)** gerekçelendirerek eşleştirmendir.

### Temel kavramlar

- **Tablo / satır / sütun**: ilişkisel veritabanında veri, sütunları önceden belirlenmiş bir çizelgede satır satır durur.
- **Şema (schema)**: hangi alanların bulunacağını ve tiplerini tanımlayan kurallar. Katı şemada her satır aynı sütunlara sahiptir.
- **JOIN**: iki tabloyu ortak bir alan üzerinden birleştirip tek sorguda okuma.
- **İşlem (transaction)**: birden çok değişikliğin "ya hep ya hiç" olarak uygulanması.
- **ACID / BASE**: iki farklı tutarlılık felsefesi (aşağıda).
- **Erişim deseni**: uygulamanın gerçekte hangi sorguları, hangi sıklıkla çalıştırdığı.

### Nasıl çalışır?

**İlişkisel (relational / SQL)** veritabanları veriyi normalize edilmiş tablolara böler, JOIN ile birleştirir ve **ACID** işlemler sunar. **ACID** (Atomicity, Consistency, Isolation, Durability) üç şeyi garanti eder. Her işlem ya tümüyle uygulanır ya hiç uygulanmaz. Aynı anda koşan işlemler birbirini bozmaz. Yazılan veri kalıcıdır. Okuma da her zaman en güncel, tutarlı durumu görür.

**BASE** (Basically Available, Soft state, Eventual consistency) ise CAP teoreminin baskısı altında erişilebilirliği ve ölçeği önceler. Sistem her zaman yanıt verir. Buna karşılık bir yazma tüm kopyalara yayılana kadar bazı okumalar kısa süre **eski (stale)** veri görebilir. Sonunda tüm kopyalar tutarlı hâle gelir; buna **nihai tutarlılık (eventual consistency)** denir.

Bu bir "iyi/kötü" ayrımı değil, bir **ödünleşimdir**. Banka bakiyesi ACID ister; beğeni sayacı BASE'in kısa gecikmesini rahatça kaldırır. Modern gerçek şudur: birçok NoSQL artık **ayarlanabilir tutarlılık (tunable consistency)** sunar ve aradaki sınır bulanıklaşmıştır.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ACID ve BASE tutarlılık modelleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki tutarlılık felsefesi</text>
  <rect x="28" y="44" width="272" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">ACID (çoğu SQL)</text>
  <text x="164" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">güçlü tutarlılık, işlem garantisi</text>
  <text x="164" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yaz-oku hep en güncel değeri verir</text>
  <text x="164" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">finans, sipariş, envanter</text>
  <text x="164" y="164" text-anchor="middle" font-size="11" fill="#B23A48" font-family="system-ui">yatay ölçeklemek daha zor</text>
  <rect x="340" y="44" width="272" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">BASE (çoğu NoSQL)</text>
  <text x="476" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">nihai tutarlılık, yüksek erişim</text>
  <text x="476" y="118" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">okuma kısa süre eski görebilir</text>
  <text x="476" y="140" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">sosyal akış, katalog, telemetri</text>
  <text x="476" y="164" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">yatay ölçeklemek daha kolay</text>
</svg>
```

Veri modeli aileleri şunlardır.

**İlişkisel**: katı şema, JOIN ve güçlü işlem desteği vardır. Karmaşık ilişkiler, önceden bilinmeyen (ad-hoc) sorgular ve kritik tutarlılık varsa idealdir.

**Anahtar-değer (key-value)**: en basit modeldir; anahtarı verirsin, değeri alırsın (Redis, DynamoDB). Değer uygulama açısından **kapalı bir kutudur (opak)**: içindeki alana göre filtreleme yapamaz, tek bir alanı kısmen güncelleyemezsin. Oturum, önbellek ve sayaç için biçilmiş kaftandır.

**Doküman**: JSON benzeri iç içe belgeler tutar (MongoDB). Şema esnektir, "bir varlık = bir belge" mantığı geçerlidir. Alan bazlı filtreleme ve kısmi güncelleme mümkündür.

**Kolon-ailesi / geniş-kolon**: satır anahtarı altında dinamik kolonlar tutar (Cassandra, HBase). Çok yüksek yazma hacmi ve zaman serisi için uygundur; ama sorgu deseninin önceden bilinmesi gerekir.

**Graf**: düğüm ve kenarlardan oluşur (Neo4j). Burada ilişkilerin **kendisi** sorgunun konusudur. Kenarlar fiziksel işaretçilerle saklandığı için (index-free adjacency) bir komşuya geçmek sabit maliyetlidir. Derinlik arttıkça ilişkiseldeki gibi her seviye için pahalı bir JOIN eklenmez.

```svg
<svg viewBox="0 0 640 248" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Beş veri modeli ailesi ve tipik kullanımları">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Beş veri modeli ailesi</text>
  <rect x="16" y="40" width="176" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="104" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İlişkisel (SQL)</text>
  <text x="206" y="62" font-size="11" fill="#1C3247" font-family="system-ui">tablolar, JOIN, ACID · finans, sipariş</text>
  <rect x="16" y="82" width="176" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="104" y="104" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Anahtar-Değer</text>
  <text x="206" y="104" font-size="11" fill="#1C3247" font-family="system-ui">anahtarla O(1) · oturum, cache, sayaç</text>
  <rect x="16" y="124" width="176" height="34" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="104" y="146" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Doküman</text>
  <text x="206" y="146" font-size="11" fill="#1C3247" font-family="system-ui">JSON belge, esnek şema · id ile tüm varlık</text>
  <rect x="16" y="166" width="176" height="34" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="104" y="188" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Kolon-ailesi</text>
  <text x="206" y="188" font-size="11" fill="#1C3247" font-family="system-ui">dinamik kolon · yüksek yazma, zaman-serisi</text>
  <rect x="16" y="208" width="176" height="34" rx="8" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="104" y="230" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">Graf</text>
  <text x="206" y="230" font-size="11" fill="#1C3247" font-family="system-ui">düğüm + kenar · ilişki gezinme, öneri</text>
</svg>
```

Modelleme yaklaşımı da farklıdır. İlişkiselde **veri-önce** düşünürsün: önce normalize edersin, sonra istediğin sorguyu JOIN'lerle çalıştırırsın.

Cassandra ve DynamoDB gibi sistemlerde ise **erişim deseni önce gelir (query-first)**. "Hangi sorguyu hangi anahtarla çalıştıracağım?" sorusu, tabloların ve bölüm (partition) / sıralama anahtarlarının tasarımını belirler. JOIN çoğunlukla ya yoktur ya da pahalıdır. Bu yüzden veri bilerek **denormalize edilip tekrarlanır**: aynı veri, farklı sorguları ucuza karşılamak için birden çok tabloda kopyalanabilir.

Bunun bedeli yazma anındaki karmaşıklık ve tutarlılık yönetimidir. Karşılığı ise devasa ölçekte öngörülebilir ve düşük gecikmedir. "Şemasız" olmak modelleme yapılmadığı anlamına gelmez; tam tersine erişim desenini önceden tasarlamayı zorunlu kılar.

Son olarak, gerçek sistemlerde tek bir "her işe uygun" veritabanı yoktur. **Poliglot kalıcılık (polyglot persistence)**, her iş yükünün kendine en uygun deposunu kullanmasıdır. Örnek: siparişler PostgreSQL'de, oturum ve önbellek Redis'te, tam metin arama Elasticsearch'te, öneri grafiği Neo4j'de, telemetri Cassandra'da durur. Bunun karşılığı, her deponun ayrı bir işletme ve tutarlılık maliyeti getirmesidir. Bu yüzden çeşitlilik gerekçeli olmalıdır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Poliglot persistence: her iş yükü kendi deposunu kullanır">
  <defs>
    <marker id="sqln145-arw" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Poliglot persistence: her işe uygun depo</text>
  <rect x="245" y="34" width="150" height="42" rx="10" fill="#EAF1F7" stroke="#1C3247" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Uygulama</text>
  <rect x="8" y="132" width="116" height="60" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="66" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">PostgreSQL</text>
  <text x="66" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sipariş · ACID</text>
  <rect x="132" y="132" width="116" height="60" rx="9" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="190" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Redis</text>
  <text x="190" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">oturum, cache</text>
  <rect x="256" y="132" width="116" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="314" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Elasticsearch</text>
  <text x="314" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tam-metin arama</text>
  <rect x="380" y="132" width="116" height="60" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="438" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Neo4j</text>
  <text x="438" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">öneri grafiği</text>
  <rect x="504" y="132" width="128" height="60" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="568" y="156" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Cassandra</text>
  <text x="568" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">telemetri</text>
  <line x1="300" y1="76" x2="80" y2="130" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sqln145-arw)"/>
  <line x1="308" y1="76" x2="196" y2="130" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sqln145-arw)"/>
  <line x1="320" y1="76" x2="316" y2="130" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sqln145-arw)"/>
  <line x1="332" y1="76" x2="436" y2="130" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sqln145-arw)"/>
  <line x1="340" y1="76" x2="560" y2="130" stroke="#5D7789" stroke-width="1.5" marker-end="url(#sqln145-arw)"/>
</svg>
```

### Mülakatta ne sorulur?

- **ACID ile BASE farkı?** ACID güçlü tutarlılık ve işlem garantisi; BASE erişilebilirlik/ölçek için nihai (eventual) tutarlılığı kabul eder. Atomiklik ve izolasyon ACID'in harfleridir, BASE'in değil.
- **Her ürünün özellikleri farklı, üstelik alana göre filtre ve kısmi güncelleme gerekiyor.** Cevap doküman modelidir: esnek şema ve tek belgede tüm varlık. Katı şema burada yüzlerce boş sütun ya da hantal bir EAV tablosu doğurur. Anahtar-değer ise değeri kapalı kutu gördüğü için alan bazlı işlem yapamaz.
- **NoSQL modellemenin ilişkiselden farkı?** Query-first: tablolar sorgulara göre kurulur, JOIN yerine denormalizasyon.
- **"Arkadaşın arkadaşının beğendiği ürünler" gibi çok seviyeli gezinme?** Graf; ilişkisel her seviyede yeni bir self-join ister, maliyet derinlikle patlar.
- **Saniyede yüz binlerce sensör ölçümü ve "şu cihazın şu aralığı" sorgusu?** Kolon-ailesi (Cassandra/HBase) ya da özel bir zaman serisi deposu. Bölüm anahtarı cihaz, sıralama anahtarı zaman olur. Yanlış bölüm anahtarı sıcak nokta (hotspot) yaratır.
- **Poliglot kalıcılık nedir?** Tek bir veritabanının her işe yetmeyeceğinin kabulüdür: her iş yüküne uygun depo seçilir. Programlama diliyle ya da yedeklemeyle ilgili bir kavram değildir.

### Tek cümlede

Veritabanı seçimi bir "din" değil, her alt-problem için erişim deseni ve tutarlılık ihtiyacına bakılarak ayrı ayrı verilen bir mühendislik kararıdır.

# sqlnosql-1 | Orta
## Soru
ACID ile BASE arasındaki temel felsefi fark nedir?
## A
ACID nihai tutarlılık sunar; BASE ise anlık ve güçlü tutarlılık garantisi verir
🤖 Tam tersi; nihai tutarlılık BASE'in, güçlü tutarlılık ACID'in özelliğidir.
## B
BASE işlemlerde atomiklik ve izolasyon garantisi verir; ACID bunları vermez
🤖 Atomiklik ve izolasyon ACID'in (A ve I) tam da harfleridir; karıştırılmış.
## C
İkisi aynı garantileri verir; aradaki tek fark SQL/NoSQL etiketinden ibarettir
🤖 Garantiler farklıdır: ACID güçlü tutarlılık, BASE erişilebilirlik lehine gevşeklik.
## D
ACID güçlü tutarlılık verir; BASE ölçek için nihai (eventual) tutarlılığı kabul eder
🤖 Doğru: ACID sağlamlık, BASE erişilebilirlik/ölçek öncelikli bir felsefedir.
## Doğru
D
## Açıklama
**ACID** (Atomicity, Consistency, Isolation, Durability) her işlemin ya tümüyle uygulanmasını ya da hiç uygulanmamasını, eşzamanlı işlemlerin birbirini bozmamasını ve yazılanın kalıcı olmasını garanti eder — okuma her zaman en güncel, tutarlı durumu görür. **BASE** (Basically Available, Soft state, Eventual consistency) ise CAP teoremi baskısı altında ölçek ve erişilebilirliği önceler: sistem her zaman yanıt verir ama bir yazma tüm kopyalara yayılana kadar bazı okumalar **kısa süre eski (stale)** veri görebilir; sonunda (eventually) tüm kopyalar tutarlı hâle gelir. Fark bir "iyi/kötü" değil, bir **ödünleşimdir**: banka bakiyesi ACID ister, sosyal medya beğeni sayacı BASE'in gecikmesini rahatça tolere eder.

# sqlnosql-2 | İleri
## Soru
Bir ürün kataloğunda her ürünün özellik seti tamamen farklı (kitapta sayfa sayısı, telefonda pil, ayakkabıda numara). Erişim çoğunlukla "ürün id'siyle tüm ürünü getir"; ayrıca zaman zaman tek bir alanı güncellemek ve alana göre filtrelemek gerekiyor. Hangi model en uygunudur?
## A
Doküman veritabanı; her ürün esnek şemalı tek bir belge olarak saklanır
🤖 Doğru: heterojen alanlar + id ile tek-varlık erişimi doküman modeliyle örtüşür.
## B
Graf veritabanı; ürünler arasındaki ilişkiler sorgunun asıl konusudur
🤖 Senaryo ilişki gezinmesi değil, tekil ürün erişimi; graf'ın gücü burada kullanılmaz.
## C
Katı şemalı ilişkisel tablo; her olası özellik için ayrı bir sütun açılır
🤖 Heterojen özellikler katı şemada yüzlerce boş sütun ya da hantal bir EAV doğurur.
## D
Anahtar-değer; ürünün tamamı tek parça, opak bir değer olarak saklanır
🤖 KV değeri opak tutar; alana göre filtreleme ve kısmi güncelleme sunmaz.
## Doğru
A
## Açıklama
Özellik setleri ürünler arasında **çok değişken (heterojen)** ve erişim çoğunlukla "id ile tüm varlığı getir" olduğunda **doküman** modeli en iyi uyumu sağlar: her ürün esnek şemalı bir JSON belgesi olarak saklanır — bir kitapta `sayfaSayisi`, bir telefonda `pilKapasitesi` alanı bulunur, boş sütun sorunu yaşanmaz; üstelik doküman veritabanları alan bazlı filtreleme ve **kısmi güncelleme** sunar. İlişkisel modelde bunu yapmak ya yüzlerce çoğu null sütun ister ya da sorgusu zor bir EAV (entity-attribute-value) deseni doğurur. Graf, ilişki gezinmesi olmadığı için gereksizdir; anahtar-değer ise değeri **opak** tuttuğundan alana göre filtreleme ve kısmi güncelleme veremez. Not: ürünler arasında güçlü JOIN ilişkileri ve karmaşık ad-hoc sorgular gerekseydi ilişkisel yeniden öne çıkardı — karar erişim desenine bağlıdır.

# sqlnosql-3 | İleri
## Soru
NoSQL (örn. Cassandra/DynamoDB) veri modelleme yaklaşımının ilişkiselden en temel farkı nedir?
## A
Önce veriyi normalize edersin; hangi sorguların geleceğini sonra düşünürsün
🤖 Bu ilişkisel yaklaşımdır; NoSQL'de sıra tam tersine döner.
## B
NoSQL şema istemez; bu yüzden veri modelleme diye bir aşama zaten yoktur
🤖 "Şema esnek" modelleme yok demek değildir; NoSQL modeli bilinçli kurulur.
## C
NoSQL her zaman JOIN destekler; tablo tasarımı ilişkiseldekiyle birebir aynıdır
🤖 Çoğu NoSQL JOIN'i desteklemez veya kaçınır; tasarım bilerek denormalizedir.
## D
Önce erişim deseni gelir; tablolar sorgulara göre kurulur, veri denormalize edilir
🤖 Doğru: NoSQL'de "query-first" tasarım; şema sorguya göre şekillenir.
## Doğru
D
## Açıklama
İlişkisel tasarım **veri-önce (normalize et, sonra istediğin sorguyu JOIN'lerle çalıştır)** yaklaşımıdır. Cassandra/DynamoDB gibi sistemlerde ise **erişim deseni önce gelir (query-first)**: "hangi sorguları hangi anahtarla çalıştıracağım?" sorusu, tabloların ve partition/sort anahtarlarının tasarımını belirler. JOIN genelde yoktur veya pahalıdır; bu yüzden veri bilerek **denormalize edilip tekrarlanır** — aynı veri, farklı sorguları ucuz karşılamak için birden çok tabloda kopyalanabilir. Bunun bedeli yazma-zamanı karmaşıklığı ve tutarlılık yönetimidir, karşılığı ise devasa ölçekte öngörülebilir düşük gecikmedir. "Şemasız" olması modelleme yapılmadığı anlamına gelmez; aksine erişim desenini önceden ve dikkatlice tasarlamayı zorunlu kılar.

# sqlnosql-4 | İleri
## Soru
"Bir kullanıcının arkadaşlarının arkadaşlarının beğendiği ürünler" gibi çok seviyeli ilişki gezinmelerini en verimli hangi model yapar?
## A
İlişkisel; her seviye için bir JOIN eklenir ve maliyet derinlikten etkilenmez
🤖 Çok seviyeli gezinmede JOIN sayısı arttıkça maliyet üstel büyür; ilişkisel zayıflar.
## B
Graf; kenarları doğrudan işaretçilerle gezer, derinlikte JOIN patlaması olmaz
🤖 Doğru: graf, ilişki gezinmesini yerel işaretçi takibiyle yapar; çok seviyede üstündür.
## C
Anahtar-değer; ilişkiler değerin içine gömülür ve tek okumada hepsi birden gelir
🤖 KV gezinme (traversal) sunmaz; her seviyede ayrı anahtar okuması gerekir.
## D
Kolon-ailesi; geniş satırlar tüm ilişkileri tek satırda tuttuğu için hızlı gezinir
🤖 Wide-column yüksek yazma/zaman serisi içindir; çok-hop gezinme onun alanı değildir.
## Doğru
B
## Açıklama
İlişkilerin **kendisi** sorgunun konusuysa ve gezinme **çok seviyeliyse** (arkadaşın arkadaşının...), **graf** veritabanı en verimli olandır. Graf, düğümler arası kenarları fiziksel işaretçiler (index-free adjacency) olarak sakladığından bir düğümden komşularına geçmek sabit maliyetlidir; derinlik arttıkça ilişkiseldeki gibi her seviye için pahalı bir JOIN eklenmez. İlişkisel modelde aynı sorgu, her ilişki seviyesi için bir self-join gerektirir ve maliyet derinlikle üstel büyür. Graf'ın tipik kullanım alanları: sosyal ağlar, öneri motorları, dolandırıcılık halkası tespiti, erişim/yetki grafikleri. Buna karşılık graf, basit id-tabanlı toplu okuma veya devasa yazma hacmi gibi işlerde en iyi seçim değildir.

# sqlnosql-5 | İleri
## Soru
Saniyede yüz binlerce sensör ölçümünü (zaman-serisi) yazacak, sorguları da "şu cihazın şu zaman aralığındaki verileri" olacak bir sistem için hangi model iyi ölçeklenir?
## A
Tek düğümlü ilişkisel veritabanı; tüm ölçümler tek bir tabloya yazılır
🤖 Tek düğümlü ilişkisel, bu yazma hacmi altında dikey ölçek sınırına hızla çarpar.
## B
Graf veritabanı; her ölçüm bir düğüm, her cihaz bir komşuluk olarak tutulur
🤖 Milyarlarca ilişkisiz ölçüm için graf hem gereksiz hem verimsizdir.
## C
Kolon-ailesi (Cassandra); cihaz+zaman anahtarıyla yatay olarak ölçeklenir
🤖 Doğru: yüksek yazma + zaman-aralığı sorgusu wide-column'ın klasik senaryosudur.
## D
Anahtar-değer; her ölçüm rastgele üretilmiş bir anahtarla saklanır
🤖 Rastgele anahtarla zaman-aralığı sorgusu yapılamaz; KV aralık taramaz.
## Doğru
C
## Açıklama
Çok yüksek **yazma hacmi** ve "belirli bir cihazın belirli zaman aralığı" gibi öngörülebilir sorgular, **kolon-ailesi / geniş-kolon** (Cassandra, HBase, Bigtable) veya özel zaman-serisi depolarının klasik güçlü alanıdır. Bu sistemler yazmayı birçok düğüme **yatay** dağıtır (lineer yazma ölçeği) ve satır anahtarını `cihazId` (partition) + `zaman` (clustering/sort) olarak tasarlarsan, bir cihazın belirli aralığı tek partition içinde sıralı ve verimli okunur. Tek düğümlü ilişkisel bu hacimde dikey ölçek duvarına çarpar; graf ilişkisiz telemetri için anlamsızdır; düz anahtar-değer ise aralık (range) sorgusu sunmaz. Modelleme yine erişim desenine göre (query-first) yapılır: partition anahtarı yanlış seçilirse "sıcak nokta" (hotspot) oluşur.

# sqlnosql-6 | Orta
## Soru
"Poliglot persistence" (polyglot persistence) ne demektir?
## A
Tek bir veritabanına birden çok programlama dilinden erişilebilmesi
🤖 Bu sürücü/istemci desteğidir; poliglot persistence *depoların* çeşitliliğiyle ilgilidir.
## B
Tüm veriyi hem SQL hem NoSQL'de birebir kopyalayarak yedeklemek
🤖 Amaç körü körüne çoğaltmak değil; her iş yükünü en uygun depoya yerleştirmektir.
## C
Her iş yükünün kendine en uygun veritabanını kullanması ilkesi
🤖 Doğru: farklı problemler için farklı, uygun depolar bir arada kullanılır.
## D
Zamanla tüm NoSQL'den vazgeçip tek bir ilişkisel veritabanına göçmek
🤖 Bu bir tekleştirmedir; poliglot yaklaşım çeşitliliği benimser, tekleştirmez.
## Doğru
C
## Açıklama
**Poliglot persistence**, tek bir "her işe uygun" veritabanının olmadığı gerçeğinden hareketle, bir sistemin **her iş yükü için o işe en uygun deposunu** kullanmasıdır: örneğin siparişler/ödemeler güçlü tutarlılık için PostgreSQL'de, oturum ve önbellek Redis'te, tam-metin arama Elasticsearch'te, öneri grafiği Neo4j'de, telemetri Cassandra'da tutulabilir. Karşılığı, her deponun ayrı işletme/tutarlılık maliyetidir; bu yüzden çeşitlilik bilinçli ve gerekçeli olmalı — gereksiz depo çoğaltmak operasyonel yükü artırır. Anahtar fikir: veritabanı seçimi tek seferlik bir "din" değil, her alt-problem için ayrı verilen bir mühendislik kararıdır.
