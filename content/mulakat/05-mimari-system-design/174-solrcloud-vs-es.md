---
slug: solrcloud-vs-es
baslik: "SolrCloud vs Elasticsearch: shard/replica, ZooKeeper, source-of-truth tuzağı"
kademe: İleri
sira: 174
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş sitesinde arama kutusuna "kırmızı koşu ayakkabısı" yazdığını düşün. Sistemin milyonlarca ürün arasından ilgili olanları saniyenin altında bulup sıralaması gerekir. Bu işi yapan yazılıma **arama motoru** (search engine) denir; Apache Solr ve Elasticsearch bu alanın en yaygın iki ürünüdür.

Arama motoru verileri **belge** (document) denen kayıtlar hâlinde tutar: bir ürün, bir haber, bir CV — her biri bir belgedir. Belgeleri hızlı bulabilmek için bir **indeks (index)** çıkarır. Bu, kitabın arkasındaki dizin gibidir: "hangi kelime hangi belgelerde geçiyor" listesi. Kullanıcının yazdığı arama isteğine de **sorgu** (query) denir.

Sorun şudur: veri büyüyünce tek bilgisayar yetmez. Ne disk yeter, ne bellek, ne de saniyedeki sorgu sayısı. İşte o noktada Solr **SolrCloud** moduna geçer. Bu mod bir **kümedir (cluster)**: birden çok makine birlikte tek bir arama sistemi gibi davranır.

### Temel kavramlar

Bir kütüphane düşün. Kitaplar o kadar çoğaldı ki tek binaya sığmıyor.

- **Bölme (shard)**: Kitapları ikiye ayırıp yarısını A binasına, yarısını B binasına koyuyorsun. Her bina koleksiyonun **bir dilimini** tutar ve toplam kapasite ikiye katlanır. SolrCloud'da bir uygulamanın tüm belgeleri **koleksiyon (collection)** adlı mantıksal indekste durur. Koleksiyon da bölmelere **ayrılır (partition)**.
- **Kopya (replica)**: A binası yanarsa kitapların yarısı gider. Bu yüzden her binanın birebir yedeğini başka bir adreste tutuyorsun. Kopya (replica) tam olarak budur: aynı bölmenin **birebir eşi**. İki fayda verir. Bir makine düşse de veri kaybolmaz; buna dayanıklılık denir. Ayrıca okuyucular kopyalara dağıtılabildiği için **okuma kapasitesi** artar.

Bölmek ve kopyalamak birbirinden bağımsız iki boyuttur. Bölme (shard) veriyi **böler**; kopya (replica) her bölmeyi **çoğaltır**. Her bölmenin kopyalarından biri **lider (leader)** seçilir. Yazmaları lider koordine eder; diğerleri ondan eşitlenir.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SolrCloud collection shard replica ve ZooKeeper">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Collection → Shard (bölme) → Replica (kopya)</text>
  <rect x="40" y="40" width="560" height="30" rx="8" fill="#F0E9F4" stroke="#7A4FA0" stroke-width="1.4"/>
  <text x="320" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#7A4FA0" font-family="system-ui">Collection "urunler"</text>
  <rect x="55" y="84" width="255" height="86" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="182" y="104" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Shard 1 (belge dilimi A)</text>
  <rect x="70" y="114" width="105" height="44" rx="6" fill="#fff" stroke="#25804C" stroke-width="1.2"/>
  <text x="122" y="133" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Replica (leader)</text>
  <text x="122" y="148" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">NRT</text>
  <rect x="185" y="114" width="105" height="44" rx="6" fill="#fff" stroke="#25804C" stroke-width="1.2"/>
  <text x="237" y="133" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Replica</text>
  <text x="237" y="148" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">PULL (okuma)</text>
  <rect x="330" y="84" width="255" height="86" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="457" y="104" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Shard 2 (belge dilimi B)</text>
  <rect x="345" y="114" width="105" height="44" rx="6" fill="#fff" stroke="#25804C" stroke-width="1.2"/>
  <text x="397" y="133" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Replica (leader)</text>
  <text x="397" y="148" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">NRT</text>
  <rect x="460" y="114" width="105" height="44" rx="6" fill="#fff" stroke="#25804C" stroke-width="1.2"/>
  <text x="512" y="133" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Replica</text>
  <text x="512" y="148" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">TLOG</text>
  <rect x="200" y="186" width="240" height="34" rx="8" fill="#F5EFE0" stroke="#B8860B" stroke-width="1.4"/>
  <text x="320" y="207" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A6608" font-family="system-ui">ZooKeeper: küme durumu + leader seçimi</text>
</svg>
```

Solr'da üç **kopya tipi** vardır. **NRT** belgeleri kendisi indeksler ve saniyeler içinde aranabilir kılar; lider olabilir. **TLOG** işlem kaydından (transaction log) indeksler; lider olabilir, ama NRT'nin sunduğu anında görünürlüğü vermez. **PULL** ise hiç indekslemez: liderden **hazır segment** dosyalarını çeker ve yalnız arama yüküne katkı verir. Bu yüzden **lider olamaz**.

### Nasıl çalışır?

Kümede "hangi shard hangi makinede, kim leader" bilgisinin tek bir yerde tutulması gerekir. SolrCloud bu işi **ZooKeeper**'a verir. ZooKeeper üç şeyi koordine eder: küme durumu, merkezî yapılandırma ve şema, bir düğüm düştüğünde yapılan **lider seçimi (leader election)**. SolrCloud onsuz çalışmaz. Dikkat: ZooKeeper belge indekslemez, sorgu skorlamaz, belgeleri bölmelere dağıtmaz ve sonuçları birleştirmez. Bunların hepsi Solr düğümlerinin işidir. ZooKeeper yalnızca kümenin "gerçek defteri"dir.

Yazma yolu şöyle işler. Belge gelir. Yönlendirici (router) kimlik değerinin özetini alıp belgenin hangi bölmeye düştüğünü hesaplar. O bölmenin lideri belgeyi yazar ve kopyalarına yayar. Okuma yolunda ise sorguyu alan düğüm tüm bölmelere sorar; dönen sonuçları birleştirip sıralar.

Şimdi en kritik mimari konu. Arama motoru bir **türetilmiş indekstir (derived index)**; **kayıt sistemi (system of record)** değildir. Gerçek kaynak, dayanıklı bir veritabanı olmalıdır (Postgres gibi). Arama motoru oradan beslenir: **CDC (Change Data Capture — değişiklik yakalama)**, olay akışı ya da **giden kutusu tablosu (outbox)** deseniyle. Gerektiğinde de kaynaktan **yeniden inşa edilebilir**.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Doğru mimari: veritabanı kayıt sistemi, Solr türev indeks, CDC ile beslenir">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Arama motoru türev indekstir, kayıt sistemi değil</text>
  <rect x="34" y="60" width="170" height="70" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="119" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Veritabanı</text>
  <text x="119" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kayıt sistemi</text>
  <text x="119" y="121" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(Postgres) — gerçek kaynak</text>
  <rect x="264" y="70" width="112" height="50" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="92" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">CDC / outbox</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">olay akışı</text>
  <rect x="436" y="60" width="170" height="70" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="521" y="86" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Solr / ES</text>
  <text x="521" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">türev indeks</text>
  <text x="521" y="121" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">arama / facet için</text>
  <line x1="204" y1="95" x2="262" y2="95" stroke="#1C3247" stroke-width="1.6"/>
  <polygon points="262,95 253,91 253,99" fill="#1C3247"/>
  <line x1="376" y1="95" x2="434" y2="95" stroke="#1C3247" stroke-width="1.6"/>
  <polygon points="434,95 425,91 425,99" fill="#1C3247"/>
  <path d="M204 130 Q320 174 436 130" fill="none" stroke="#8A2E2E" stroke-width="1.5" stroke-dasharray="5 4"/>
  <polygon points="436,130 426,129 431,136" fill="#8A2E2E"/>
  <text x="320" y="166" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">şema/analiz değişince kaynaktan REINDEX; bozulmada yeniden inşa</text>
</svg>
```

Bu neden bu kadar önemli? Sebebi şudur. Analiz zinciri (tokenizer, stemmer, eş anlamlılar) ya da şema değişirse, yeni davranış **yalnız o andan sonra indekslenen belgelere** uygulanır. Mevcut segmentler değişmez (immutable) olduğu için eski belgelere **geriye dönük uygulanmaz**. Tutarlılık için tüm veriyi **yeniden indekslemek (reindex)** gerekir.

Yeniden indeksleme ise ham veriyi bir kaynaktan okumak zorundadır. Veri yalnız arama motorundaysa o kaynak yoktur: analizden geçmiş terimler ve docValues sana ham metni geri vermez. Üstelik o durumda bir indeks bozulması doğrudan veri kaybına dönüşür.

Peki Elasticsearch? O da aynı **Lucene** çekirdeğini kullanır. Farklar üst katmandadır: JSON tabanlı sorgu dili (Query DSL), ELK/Elastic Stack ekosistemi ve koordinasyonu **kendi içinde** yapması. Yani ayrı bir ZooKeeper istemez.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Solr ile Elasticsearch: ortak Lucene çekirdeği, farklı üst katman">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Solr vs Elasticsearch: ortak çekirdek, farklı katman</text>
  <rect x="160" y="34" width="320" height="26" rx="7" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="320" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">ortak: Apache Lucene çekirdeği</text>
  <rect x="26" y="74" width="288" height="112" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="170" y="96" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Solr</text>
  <text x="46" y="118" font-size="10.5" fill="#1C3247" font-family="system-ui">• koordinasyon: ZooKeeper</text>
  <text x="46" y="138" font-size="10.5" fill="#1C3247" font-family="system-ui">• güçlü faceting / streaming</text>
  <text x="46" y="158" font-size="10.5" fill="#1C3247" font-family="system-ui">• Apache açık kaynak, config esnek</text>
  <text x="46" y="177" font-size="10.5" fill="#5D7789" font-family="system-ui">→ zengin arama / analitik</text>
  <rect x="326" y="74" width="288" height="112" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="96" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Elasticsearch</text>
  <text x="346" y="118" font-size="10.5" fill="#1C3247" font-family="system-ui">• koordinasyon: kendi içinde</text>
  <text x="346" y="138" font-size="10.5" fill="#1C3247" font-family="system-ui">• JSON Query DSL, REST öncelikli</text>
  <text x="346" y="158" font-size="10.5" fill="#1C3247" font-family="system-ui">• ELK / Elastic Stack ekosistemi</text>
  <text x="346" y="177" font-size="10.5" fill="#5D7789" font-family="system-ui">→ log / gözlemlenebilirlik</text>
</svg>
```

### Mülakatta ne sorulur?

- "Bölme (shard) ile kopya (replica) farkı nedir?" — En sık gelen soru. Tek cümlelik cevap: bölme veriyi **böler** ve kapasite verir; kopya **çoğaltır**, dayanıklılık ve okuma kapasitesi verir. Tuzak: ikisini eş anlamlı sanmak ya da rolleri ters söylemek.
- "ZooKeeper ne yapar; Elasticsearch'te karşılığı nedir?" — Küme durumu + config/şema + lider seçimi; ES aynı işi kendi içinde çözer. Tuzak: ZooKeeper'a indeksleme, skorlama, belge yönlendirme veya sonuç birleştirme gibi Solr işlerini yüklemek.
- "Solr mu Elasticsearch mü?" — Beklenen olgun cevap şudur: çekirdek ikisinde de ortaktır (Lucene). Seçim; ekosistem, ekip tecrübesi ve işletme kolaylığıyla ilgilidir. Tuzak: "biri Lucene kullanır, diğeri kullanmaz" demek ya da sorgu dili / koordinasyon rollerini ters çevirmek.
- "Analiz zincirini değiştirdik, eski belgeler neden hâlâ eski davranıyor?" — Geriye dönük uygulanmaz, reindex gerekir. Hemen ardından gelen asıl soru: "Reindex'i neyden yapacaksın?"
- "Verimizi yalnızca Solr'da tutsak olur mu?" — Hayır: türev indeks kayıt sistemi olamaz. İyi cevap, CDC/outbox ile beslenen ve kaynaktan yeniden inşa edilebilen bir mimari anlatır.
- "PULL replica'yı ne zaman kullanırsın?" — Ağır okuma, hafif yazma senaryosunda; indekslemediği için ucuz okuma ölçeği verir, ama leader olamaz.

### Tek cümlede

SolrCloud veriyi bölmelerle (shard) böler, kopyalarla (replica) çoğaltır ve ZooKeeper ile koordine eder. Elasticsearch aynı Lucene çekirdeğini farklı bir üst katmanla sunar. Ama ikisi de kayıt sistemi değildir; kaynaktan yeniden inşa edilebilen birer türetilmiş indekstir.

# scloud-1 | İleri
## Soru
SolrCloud'da **shard** ile **replica** arasındaki fark nedir?
## A
İkisi aynı şeyin iki adıdır; shard eski sürümlerin, replica yeni sürümlerin terimidir
🤖 Eş anlamlı değiller: biri veriyi böler, diğeri o bölümü kopyalar.
## B
Shard koleksiyonun yatay bölümüdür (kapasite); replica bir shard'ın kopyasıdır (dayanıklılık, biri leader)
🤖 Doğru: shard böler (partition), replica çoğaltır (replication).
## C
Shard bir shard'ın kopyasıdır (dayanıklılık); replica ise koleksiyonu yatay bölen birimdir (kapasite)
🤖 Roller ters yazılmış: bölen shard'dır, kopyalayan replica'dır.
## D
Shard yalnız yazma trafiğini, replica yalnız okuma trafiğini taşıyan iki ayrı düğüm rolüdür
🤖 Böyle bir okuma/yazma rol ayrımı yok; leader replica yazma da alır.
## Doğru
B
## Açıklama
İki farklı boyut: **Shard** = **bölme** (partition). Koleksiyonun belgeleri shard'lara dağıtılır; her shard verinin **bir dilimini** tutar. Daha çok shard = daha çok kapasite ve **yatay ölçek** (veri tek makineye sığmadığında). **Replica** = **kopya**. Bir shard'ın birden çok replica'sı olur; hepsi aynı veriyi taşır, **dayanıklılık** (biri düşerse diğeri devam eder) ve **okuma kapasitesi** (sorgular replica'lara dağılır) sağlar; her shard için bir replica **leader** seçilir ve yazmaları koordine eder. Yani shard "veriyi böler", replica "her bölümü çoğaltır". (A) eş anlamlı değiller, sürüm terminolojisi meselesi hiç değil; (C) roller tam ters verilmiş; (D) okuma/yazma diye bir rol ayrımı yoktur — yazma leader replica'ya gider, okuma tüm replica'lara dağılır. DDIA'nın partitioning + replication ikilisi tam olarak budur.

# scloud-2 | İleri
## Soru
SolrCloud'da **ZooKeeper**'ın rolü nedir?
## A
Küme durumunu, config/şemayı ve lider seçimini koordine eder; SolrCloud ZooKeeper olmadan çalışmaz
🤖 Doğru: ZooKeeper kümenin koordinasyon ve durum beynidir.
## B
Gelen her belgeyi hash'leyip hangi shard'a düşeceğini hesaplar; tüm indeksleme trafiği üzerinden geçer
🤖 Belge yönlendirmeyi (router) Solr istemcisi/düğümü yapar; trafik ZooKeeper'dan geçmez.
## C
Replica'lar arasındaki segment kopyalamayı yürütür; indeks dosyalarını leader'dan çekip dağıtan odur
🤖 Segment replikasyonu doğrudan Solr düğümleri arasında olur, ZooKeeper taşımaz.
## D
Shard'lardan dönen kısmi sonuçları toplayıp skorlarına göre tek sonuç kümesinde birleştirir
🤖 Sonuçları birleştiren, sorguyu alan koordinatör Solr düğümüdür; ZooKeeper değil.
## Doğru
A
## Açıklama
**ZooKeeper**, SolrCloud'un **dağıtık koordinasyon** katmanıdır: kümenin **canlı durumunu** tutar (hangi collection'ın hangi shard'ı hangi node'da, hangi replica leader), merkezî **config/şemayı** saklar ve bir node düştüğünde **lider seçimini** (leader election) yürütür. Solr node'ları küme hakkındaki gerçeği ZooKeeper'dan öğrenir; bu yüzden SolrCloud **ZooKeeper olmadan çalışmaz** (yeni sürümlerde gömülü/alternatif dağıtım seçenekleri tartışılsa da rolü aynıdır). Kritik nokta: ZooKeeper **veri yolunda değildir**. (B) Belgenin hangi shard'a gideceğini hesaplayan document router istemci/Solr node tarafında çalışır; indeksleme trafiği ZooKeeper'a uğramaz. (C) Segment replikasyonu leader ile replica arasında doğrudan yapılır. (D) Kısmi sonuçları toplayıp birleştiren, sorguyu karşılayan koordinatör Solr node'udur. Elasticsearch'ün ise ayrı bir ZooKeeper'a ihtiyacı yoktur; koordinasyonu kendi içinde yapar — iki ürünün en görünür operasyonel farklarından biri budur.

# scloud-3 | İleri
## Soru
Solr ve Elasticsearch karşılaştırmasında hangisi doğrudur?
## A
İkisi farklı arama çekirdeği kullanır: Solr Lucene üzerine, Elasticsearch kendi yazdığı motor üzerine kuruludur
🤖 Elasticsearch de Lucene üzerine kuruludur; ayrı bir çekirdek yazmamıştır.
## B
Sorgu dili rolleri şöyledir: JSON tabanlı Query DSL Solr'ın, parametre tabanlı (q, fq) arayüz Elasticsearch'ün imzasıdır
🤖 Roller ters: JSON Query DSL Elasticsearch'ün, parametre tabanlı arayüz Solr'ındır.
## C
Koordinasyon rolleri şöyledir: Elasticsearch küme durumu için ZooKeeper'a bağımlıdır, SolrCloud ise koordinasyonu kendi içinde yapar
🤖 Roller ters: ZooKeeper'a bağımlı olan SolrCloud, koordinasyonu içeride yapan Elasticsearch'tür.
## D
İkisi de aynı Lucene çekirdeğini paylaşır; farklar sorgu dili, ekosistem ve koordinasyon gibi üst katmanlardadır
🤖 Doğru: ortak çekirdek, farklı üst katman.
## Doğru
D
## Açıklama
Solr ve Elasticsearch **aynı Lucene çekirdeğini** paylaşır; ayrım **üst katmanlardadır**. Elasticsearch **JSON Query DSL** ve REST-öncelikli bir API sunar, **ELK/Elastic Stack** (Kibana + Beats + Logstash) ile log toplama ve gözlemlenebilirlikte pazar lideridir, koordinasyonu **kendi içinde** yapar (ayrı ZooKeeper yok). Solr ise Apache açık kaynak yönetişimi, güçlü faceting/streaming ifade gücü ve derin config esnekliğiyle öne çıkar; sorgu arayüzü klasik olarak **parametre tabanlıdır** (q, fq, facet…) ve koordinasyon **ZooKeeper** üzerinedir. Seçim genelde saf teknik üstünlük değil; ekip tecrübesi, mevcut ekosistem (log yığını ELK ise ES doğal) ve operasyon tercihiyle belirlenir. (A) Elasticsearch de Lucene tabanlıdır, ayrı bir çekirdeği yoktur. (B) sorgu dili rollerini ters çevirir. (C) koordinasyon rollerini ters çevirir — ZooKeeper bağımlısı olan SolrCloud'dur.

# scloud-4 | İleri
## Soru
Bir ekip tüm ürün verisini **yalnızca Solr'da** tutuyor, kalıcı veritabanı yok. Analiz zinciri değişince ne sorunla karşılaşırlar?
## A
Sorun çıkmaz; Solr'ın transaction log'u ham belgeleri kalıcı sakladığı için kayıt sistemi görevini üstlenir
🤖 Transaction log dayanıklılık için kısa ömürlü bir tampondur, kalıcı kayıt sistemi değildir.
## B
Analiz değişikliği, arka planda segment birleştirme (merge) sırasında eski belgelere de otomatik uygulanır
🤖 Merge segmentleri yalnız birleştirir; belgeleri yeni analizle yeniden çözümlemez.
## C
Analiz/şema değişimi eski belgelere geriye dönük uygulanmaz; reindex gerekir ama besleyecek güvenilir bir kaynak yoktur
🤖 Doğru: reindex için kaynak yoksa değişiklik uygulanamaz, veri riske girer.
## D
Solr her segmentte kendi analiz zincirini saklar ve sorgu anında dönüştürerek yeniden indekslemeyi gereksiz kılar
🤖 Sorgu anında böyle bir geriye dönük dönüştürme yoktur; segmentler değişmezdir.
## Doğru
C
## Açıklama
Analiz zinciri (tokenizer, stemmer, synonym…) veya şema değişikliği **yalnız yeni indekslenen belgelere** uygulanır; mevcut segmentler değişmez (immutable) olduğu için değişiklik **geriye dönük uygulanmaz** — tutarlı olmak için tüm veriyi **yeniden indekslemek** (reindex) gerekir. Reindex ham veriyi bir **kaynaktan** okumayı gerektirir. Eğer tek yer Solr ise (stored alanlar her zaman tam ham veriyi taşımaz; docValues ve analizden geçmiş terimler ham metni geri vermez), reindex edecek güvenilir kaynak **yoktur** ve değişiklik güvenle uygulanamaz; üstelik indeks bozulması doğrudan veri kaybı olur. (A) transaction log yalnızca commit öncesi dayanıklılık tamponudur, kalıcı kayıt sistemi değildir. (B) segment merge sadece segmentleri birleştirir, belgeleri yeni analizle yeniden çözümlemez. (D) Solr sorgu anında eski belgeleri yeni analize göre dönüştürmez. Ders: reindex yeteneği için kalıcı ve güvenilir bir kaynak şarttır.

# scloud-5 | Uzman
## Soru
Arama motorunun (Solr/ES) doğru mimari konumu nedir?
## A
Kayıt sistemi (system of record): tüm yazımlar önce ona gitmeli, veritabanı ondan türetilerek beslenmelidir
🤖 Yön ters: kayıt sistemi dayanıklı veritabanıdır, arama motoru ondan beslenir.
## B
İlişkisel veritabanının tam yerine geçen birincil depo; transaction garantilerini aynı düzeyde o da sunar
🤖 Arama motoru ACID transaction ve tutarlılık garantilerini veritabanı düzeyinde sunmaz.
## C
Sıcak veri için önbellek katmanı; Redis gibi TTL'li anahtar-değer deposu olarak konumlandırılmalıdır
🤖 Arama motoru anahtar-değer önbelleği değildir; işi tam-metin ve facet aramadır.
## D
Türev indeks (derived index): gerçek kaynak dayanıklı bir veritabanıdır, arama motoru oradan CDC/outbox ile beslenir
🤖 Doğru: arama motoru türev indekstir, kaynaktan senkronla beslenir ve yeniden inşa edilir.
## Doğru
D
## Açıklama
Arama motoru bir **türev indekstir** (derived data / secondary index) — hızlı arama, faceting ve alaka için optimize edilmiştir ama **kayıt sistemi (system of record)** değildir: güçlü transaction/tutarlılık garantileri, kolay yedek/geri-yükleme ve şema evrimi için değil, arama için tasarlanmıştır. Doğru mimari (DDIA'nın "derived data" ilkesi): **gerçek kaynak dayanıklı bir veritabanıdır** (Postgres/MySQL); arama motoru oradan **CDC** (Change Data Capture), olay akışı (event stream) veya **outbox** deseniyle beslenir. Böylece analiz/şema değişiminde kaynaktan **reindex** edilir, bozulmada yeniden inşa edilir, tutarsızlık geçici kalır. (A) yönü tersine çevirir: yazmaları önce arama motoruna almak, reindex kaynağını ve dayanıklılığı riske atar. (B) arama motoru ilişkisel veritabanının transaction garantilerini vermez. (C) o bir key-value önbellek de değildir; Redis'in yerine geçmez.

# scloud-6 | İleri
## Soru
SolrCloud'da **PULL** tipi replica'nın temel özelliği nedir?
## A
Belgeleri kendisi indeksler, transaction log tutar ve gerekirse leader seçilip yazmaları koordine eder
🤖 Bu NRT/TLOG davranışıdır; PULL indekslemez ve leader olamaz.
## B
Kendisi indekslemez; leader'dan hazır segmentleri çeker ve yalnız arama yüküne katkı verir, leader olamaz
🤖 Doğru: PULL salt-okuma ölçeği içindir; indekslemez, leader olmaz.
## C
Yalnız yazma trafiği alır; arama isteği karşılamaz, gelen sorguları NRT replica'lara yönlendirir
🤖 Yön tam ters: PULL arama/okuma içindir, yazma ve indeksleme yapmaz.
## D
ZooKeeper'daki küme durumunu diğer düğümlere dağıtan koordinasyon replica'sıdır; belge verisi tutmaz
🤖 PULL bir veri replica'sıdır; koordinasyon işi ZooKeeper'ın, belge segmentlerini de tutar.
## Doğru
B
## Açıklama
SolrCloud'un üç replica tipi indeksleme ve okuma yükünü ayırmayı sağlar. **NRT** hem indeksler hem near-real-time arama sunar (leader olabilir). **TLOG** transaction log'dan indeksler, leader olabilir ama soft-commit'in NRT görünürlüğünü sağlamaz. **PULL** ise **hiç indekslemez**; leader'dan **hazır segmentleri çeker** (eski master-slave replikasyonuna benzer) ve yalnız **arama/okuma** yüküne katkı verir — bu yüzden **leader olamaz**. PULL, ağır okuma-hafif yazma senaryolarında okuma kapasitesini ucuza ölçeklemek için kullanılır (indeksleme CPU'sunu harcamaz). (A) indeksleme + transaction log + leader olabilme NRT/TLOG özellikleridir. (C) yön terstir — PULL yazma değil okuma tarafındadır. (D) PULL bir koordinasyon bileşeni değildir; küme durumunu dağıtan ZooKeeper'dır ve PULL belge segmentlerini fiilen tutar. Doğru replica karışımı, yük profiline göre indeksleme ve arama kaynaklarını dengeler.
