---
baslik: "Hafta 11 Görsel Özet: Veri-Yoğun Sistemler (DDIA)"
yazar: "DDIA 2e (Kleppmann & Riccomini) — kavram özeti"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 11
---

### Bu metin ne anlatıyor?
Veri çok büyüdüğünde ya da çok hızlı değiştiğinde tek bir bilgisayar yetmez; veriyi birden
çok makineye **kopyalamak** ve **bölmek** gerekir. Bu metin o iki işlemin nasıl yapıldığını,
karşılığında hangi yeni sorunların doğduğunu ve bir tasarımın iyi olup olmadığını hangi
ölçütlerle yargılayacağını anlatır. Sonunda kendini sınayacağın sorular var.

## Neden bu özet
Veri-yoğun sistemlerde asıl iş, tek bir doğru cevabı bulmak değil, **denge (trade-off)**
kurmaktır. Bu özet, DDIA'nın üç temel kavramını — replikasyon (replication), bölümleme
(partitioning) ve fonksiyonel-olmayan gereksinimler (nonfunctional requirements) — üç
diyagramla, tek bakışta hatırlanacak biçimde toplar.

## "Veri-yoğun" ne demek
Bir uygulama **hesaplama-yoğun (compute-intensive)** ise darboğazı işlemci gücüdür.
**Veri-yoğun (data-intensive)** ise darboğaz verinin **miktarı**, **karmaşıklığı** ve **değişme
hızıdır**. Zorluk "bu hesabı yapabilir miyim" değil, "bu kadar veriyi kaybetmeden ve tutarlı
biçimde nasıl saklarım ve okuturum" sorusudur. Metnin geri kalanında sürekli geçecek üç terim:

- **Düğüm (node):** kümedeki tek bir sunucu.
- **Replikasyon (replication):** aynı verinin birden çok düğümde **kopyasını** tutmak — arızaya
  dayanmak, okumayı dağıtmak, veriyi kullanıcıya yaklaştırmak için.
- **Bölümleme (partitioning / sharding):** veriyi parçalara ayırıp **farklı** düğümlere
  dağıtmak — tek düğüme sığmayan veriyi ve yükü taşımak için.

İkisi bağımsızdır ve gerçek sistemlerde **birlikte** kullanılır: veri önce bölümlere ayrılır,
sonra her bölüm ayrıca çoğaltılır.

## Diyagramlar

Aşağıdaki ilk diyagram, en yaygın replikasyon biçimini gösterir: yazma (write)
yalnız lidere gider, okuma (read) takipçilere dağılır ve lider değişiklikleri bir
akış (replication log) hâlinde takipçilere iletir.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek-lider replikasyon: yazma lidere, okuma takipçilere">
  <defs>
    <marker id="h11ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="h11ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek-lider replikasyon (Single-leader replication)</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Yazma yalnız lidere · okuma takipçilere · lider akıtır</text>
  <text x="320" y="60" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">Yazma (write) ↓</text>
  <line x1="320" y1="64" x2="320" y2="72" stroke="#186399" stroke-width="1.8" marker-end="url(#h11ok1)"/>
  <rect x="232" y="76" width="176" height="62" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="104" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">LİDER (Leader)</text>
  <text x="320" y="124" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tüm yazmalar buraya</text>
  <text x="320" y="164" text-anchor="middle" font-size="11" font-weight="600" fill="#25804C" font-family="system-ui">replikasyon logu (replication log)</text>
  <line x1="286" y1="140" x2="176" y2="186" stroke="#25804C" stroke-width="1.8" marker-end="url(#h11ok2)"/>
  <line x1="354" y1="140" x2="464" y2="186" stroke="#25804C" stroke-width="1.8" marker-end="url(#h11ok2)"/>
  <rect x="56" y="190" width="180" height="64" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="146" y="218" text-anchor="middle" font-size="13.5" font-weight="700" fill="#4F46E5" font-family="system-ui">TAKİPÇİ 1</text>
  <text x="146" y="238" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">okuma (read)</text>
  <rect x="404" y="190" width="180" height="64" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="494" y="218" text-anchor="middle" font-size="13.5" font-weight="700" fill="#4F46E5" font-family="system-ui">TAKİPÇİ 2</text>
  <text x="494" y="238" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">okuma (read)</text>
  <text x="320" y="284" text-anchor="middle" font-size="11.5" font-weight="600" fill="#186399" font-family="system-ui">Okuma (read) takipçilere dağılır ↑</text>
  <line x1="146" y1="288" x2="146" y2="258" stroke="#186399" stroke-width="1.8" marker-end="url(#h11ok1)"/>
  <line x1="494" y1="288" x2="494" y2="258" stroke="#186399" stroke-width="1.8" marker-end="url(#h11ok1)"/>
</svg>
```

## Replikasyonun üç modeli
**1. Tek-lider (single-leader).** Yukarıdaki diyagramdaki model: bütün yazmalar **lidere**
gider, lider değişiklikleri takipçilere akıtır. En yaygın ve en basit modeldir çünkü
**çakışma (conflict) yoktur** — her yazma tek bir yerde sıraya girer. Bedeli, liderin hem
yazma darboğazı hem tek arıza noktası olmasıdır: düştüğünde **devir (failover)** gerekir,
devir sırasında henüz aktarılmamış yazmalar kaybolabilir ve iki düğümün birden kendini lider
sanması (**split brain**) riski doğar.

**2. Çok-lider (multi-leader).** Birden çok düğüm yazma kabul eder; tipik kullanımı her veri
merkezinde bir lider bulundurmaktır. Yazma gecikmesi düşer, bir bölge tamamen düşse bile
yazma sürer. Bedeli ağırdır: aynı kayıt iki yerde aynı anda değişirse **çakışma** olur ve
bunu **sen** çözmek zorundasın. "Son yazan kazanır" (last write wins) en kolay çözümdür ama
sessizce veri kaybettirir.

**3. Lidersiz (leaderless).** Lider yoktur; istemci aynı yazmayı birden çok kopyaya gönderir,
okurken de birden çok kopyadan okur. Kural **çoğunluktur (quorum)**: `w + r > n` — yazılan ve
okunan kopya sayısının toplamı, toplam kopya sayısını aşarsa en az bir güncel kopyayı görmüş
olursun. Cassandra ve DynamoDB bu ailedendir. Bedeli, tutarlılık garantilerinin zayıflaması ve
eskimiş kopyaların **okuma onarımı (read repair)** gibi ek mekanizmalarla düzeltilmesidir.

Her modelde bir seçim daha var: **senkron replikasyon** (lider, takipçi onaylamadan "tamam"
demez — güvenli ama yavaş) ve **asenkron replikasyon** (lider hemen "tamam" der — hızlı, ama
lider düşerse son yazmalar kaybolur). Çoğu sistem karma çalışır: bir takipçi senkron, ötekiler
asenkron.

## Replikasyon gecikmesi ve "kendi yazdığını okuma"
Asenkron replikasyonda takipçi liderin **gerisinde kalır**; aradaki farka **replikasyon
gecikmesi (replication lag)** denir. Normalde milisaniyelerdir, yük altında saniyelere çıkar.
Kullanıcı açısından sonucu şudur: bir şey yazarsın, hemen okursun, **yazdığını göremezsin**.
Üç garanti bu üç şikâyeti karşılar:

- **Kendi yazdığını okuma (read-your-writes).** Kullanıcı kendi değişikliğini daima görmelidir.
  Çözüm: kullanıcının kendi düzenleyebildiği veriyi **liderden** oku.
- **Monotonik okuma (monotonic reads).** Kullanıcı zamanda geriye gitmemelidir: gördüğü yorum
  sayfayı yenileyince kaybolmamalıdır. Çözüm: her kullanıcı hep **aynı** takipçiden okusun.
- **Tutarlı önek okuma (consistent prefix reads).** Neden-sonuç sırası bozulmamalıdır; cevap
  sorudan önce görünmemelidir. Özellikle bölümlenmiş sistemlerde ortaya çıkar.

Asıl ders: **"nihai tutarlılık" (eventual consistency) bir söz değil, bir belirsizliktir.**
"Sonunda tutarlı olur" cümlesi *ne zaman* sorusuna cevap vermez; hangi anomaliyi kullanıcıya
yaşatamayacağını önceden yazmak zorundasın.

Bölümleme (partitioning / sharding), tek bir düğümün taşıyamayacağı kadar büyük
veriyi anahtara göre (by key) parçalara böler. Böylece her düğüm yalnızca kendi
dilimini tutar ve sistem yatay olarak (horizontal scale) büyüyebilir.

```svg
<svg viewBox="0 0 640 270" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bölümleme: büyük veri anahtara göre üç düğüme bölünür">
  <defs>
    <marker id="h11ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bölümleme / parçalama (Partitioning / Sharding)</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Büyük veri anahtara göre (by key) N düğüme bölünür</text>
  <rect x="34" y="96" width="158" height="90" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="113" y="132" text-anchor="middle" font-size="13.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Büyük veri</text>
  <text x="113" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">anahtara göre</text>
  <text x="113" y="168" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">(a–z)</text>
  <line x1="194" y1="128" x2="356" y2="90" stroke="#25804C" stroke-width="1.8" marker-end="url(#h11ok3)"/>
  <line x1="194" y1="141" x2="356" y2="141" stroke="#25804C" stroke-width="1.8" marker-end="url(#h11ok3)"/>
  <line x1="194" y1="154" x2="356" y2="192" stroke="#25804C" stroke-width="1.8" marker-end="url(#h11ok3)"/>
  <rect x="360" y="66" width="246" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="483" y="94" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Düğüm A — Bölüm A (a–h)</text>
  <rect x="360" y="120" width="246" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="483" y="148" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Düğüm B — Bölüm B (i–p)</text>
  <rect x="360" y="174" width="246" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="483" y="202" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Düğüm C — Bölüm C (q–z)</text>
  <text x="320" y="250" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">Her düğüm (node) verinin bir dilimini tutar → yatay ölçek (horizontal scale)</text>
</svg>
```

## Bölümlemenin iki yolu ve sıcak nokta tehlikesi
**Anahtar aralığına göre (key range)** bölmede anahtarlar sıralanır ve aralıklar düğümlere
dağıtılır (diyagramdaki a–h, i–p, q–z gibi). Aralık sorguları kolaydır; tehlikesi, sıralı bir
anahtar seçersen — örneğin zaman damgası — bütün yeni yazmaların **tek** bölüme gitmesidir.
**Hash'e göre (hash of key)** bölmede anahtar bir karma fonksiyonundan geçer; yük düzgün
dağılır ama aralık sorgusunu kaybedersin.

**Sıcak nokta (hot spot)**, yükün tek bir bölüme yığılmasıdır. En zor hâli **sıcak anahtardır**
(hot key): çok takipçili bir hesap gibi tek bir anahtar, tek başına bir bölümü ezer. Hash
bölümleme bunu kurtarmaz, çünkü sorun dağılım değil **tek** anahtarın yüküdür. Çare, anahtarın
sonuna küçük bir rastgele ek koyup yükü birkaç bölüme yaymaktır; bedeli okurken bütün parçaları
toplamaktır.

Bir uyarı daha: bölümü seçerken **"hash mod N"** yapma — düğüm sayısı değiştiği anda neredeyse
bütün anahtarlar yer değiştirir. Bunun yerine düğümden çok daha fazla sayıda sabit bölüm
oluşturulur ve düğüm eklendikçe bölümler bütün hâlinde taşınır; buna **yeniden dengeleme
(rebalancing)** denir.

## Veri diske nasıl yazılır: B-ağacı ve LSM
Veritabanının altındaki **depolama motoru (storage engine)** iki aileden birine girer ve seçim
sistemin yazma/okuma karakterini belirler.

**B-ağacı (B-tree).** Veriyi sabit boyutlu **sayfalarda (page)** tutar, güncellemeyi **yerinde
(in-place)** yapar: değişen kaydın sayfası diskte yeniden yazılır. Okuma maliyeti
öngörülebilirdir, aralık sorguları iyidir, güçlü işlem desteğiyle iyi anlaşır. İlişkisel
veritabanlarının (PostgreSQL, MySQL) varsayılanıdır.

**LSM-ağacı (Log-Structured Merge-tree).** Yazmayı önce bellekte sıralı bir yapıya alır, dolunca
diske **ardışık** yazar, arka planda dosyaları birleştirir (**sıkıştırma/compaction**). Ardışık
yazma diskin en sevdiği erişim biçimi olduğu için yazma verimi yüksektir, veri daha iyi sıkışır.
Bedeli: bir okuma birden çok dosyaya bakabilir ve arka plandaki birleştirme disk bant genişliğini
yiyerek gecikmeyi zaman zaman zıplatır. RocksDB ve Cassandra bu ailedendir.

Kaba kural: **yazma ağırlıklı yük → LSM; okuma, aralık sorgusu ve güçlü işlem ağırlıklı yük →
B-ağacı.**

## İşlem (transaction), ACID ve göz ardı edilen anomaliler
**İşlem**, bir grup okuma ve yazmanın tek bir birim gibi ya hep ya hiç gerçekleşmesidir.
**Atomiklik (Atomicity)** işlemin yarıda kalmamasıdır; **tutarlılık (Consistency)** aslında
veritabanının değil **uygulamanın** sorumluluğudur (ACID'in en zayıf harfi); **yalıtım
(Isolation)** eşzamanlı işlemlerin birbirini görmemiş gibi davranmasıdır; **dayanıklılık
(Durability)** onaylanan yazmanın kaybolmamasıdır.

Asıl iş yalıtımdadır, çünkü tam yalıtım pahalıdır ve veritabanları onu varsayılan olarak
vermez. Zayıf yalıtımda çıkan anomaliler:

- **Kirli okuma (dirty read):** henüz onaylanmamış (commit edilmemiş) veriyi okumak.
- **Okuma çarpıklığı (read skew):** aynı işlem içinde verinin iki farklı anına ait tutarsız
  görüntüsünü almak. Çözümü **anlık görüntü yalıtımıdır (snapshot isolation)**.
- **Kayıp güncelleme (lost update):** iki işlem "oku – değiştir – yaz" yaparsa biri ötekini
  ezer. Çözüm: atomik güncelleme ya da açık kilit.
- **Yazma çarpıklığı (write skew):** iki işlem farklı satırları okur ve yazar, tek tek doğrudur
  ama **birlikte** bir kuralı bozarlar (iki nöbetçi doktorun aynı anda nöbetten çıkması).
  Yalnızca **serileştirilebilir (serializable)** yalıtım tam çözer.

Akılda kalacak tek cümle: **çoğu veritabanının varsayılan yalıtım seviyesi serileştirilebilir
değildir.** "Veritabanı halleder" varsayımı, üretimdeki en pahalı hataların kaynağıdır.

Son diyagram, DDIA'nın tasarımı değerlendirdiği üç ekseni özetler. Bunlar
fonksiyonel-olmayan gereksinimlerdir (nonfunctional requirements): kod ne
yaparsa yapsın, sistemin nasıl davranması gerektiğini tarif ederler.

```svg
<svg viewBox="0 0 640 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="DDIA üç ekseni: güvenilirlik, ölçeklenebilirlik, sürdürülebilirlik">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">DDIA üç ekseni (fonksiyonel-olmayan gereksinimler)</text>
  <text x="320" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">Her tasarım kararı bu üçü arasında bir denge (trade-off)</text>
  <rect x="20" y="60" width="190" height="112" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="115" y="90" text-anchor="middle" font-size="14" font-weight="700" fill="#186399" font-family="system-ui">Güvenilirlik</text>
  <text x="115" y="109" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">(Reliability)</text>
  <text x="115" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Arıza, hata ve yükte</text>
  <text x="115" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">bile doğru çalışır</text>
  <rect x="225" y="60" width="190" height="112" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="320" y="90" text-anchor="middle" font-size="13.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Ölçeklenebilirlik</text>
  <text x="320" y="109" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">(Scalability)</text>
  <text x="320" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Artan yük altında</text>
  <text x="320" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">başa çıkabilme</text>
  <rect x="430" y="60" width="190" height="112" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="525" y="90" text-anchor="middle" font-size="13.5" font-weight="700" fill="#C77A17" font-family="system-ui">Sürdürülebilirlik</text>
  <text x="525" y="109" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">(Maintainability)</text>
  <text x="525" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Zamanla kolay</text>
  <text x="525" y="152" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">geliştirilip bakılabilme</text>
  <text x="320" y="204" text-anchor="middle" font-size="11.5" font-weight="600" fill="#25804C" font-family="system-ui">Birini iyileştirmek çoğu zaman ötekinden ödün ister (trade-off)</text>
</svg>
```

Üç eksenin içi açılırsa: **güvenilirlik**, donanım arızası, yazılım hatası ve insan hatası
karşısında çalışmaya devam etmektir — hatasızlık değil, **hataya dayanıklılık**.
**Ölçeklenebilirlik** tek kelimelik bir sıfat değildir; ancak "şu yük şu kadar artarsa şu
kaynağı şu kadar ekleyerek karşılarız" cümlesiyle tarif edilir. **Sürdürülebilirlik**, yıllar
sonra sistemi devralan kişinin onu anlayıp güvenle değiştirebilmesidir.

## Ölçmenin doğru birimi: ortalama değil, yüzdelik dilim
Ölçeklenebilirliği ölçerken **ortalama yanıt süresi yanıltır**: tek bir çok yavaş istek
ortalamayı şişirir, binlerce hızlı istek onu gizler. Doğru birim **yüzdelik dilimdir
(percentile)** — yanıt sürelerini küçükten büyüğe dizip belirli noktalara bakarsın. **p50
(medyan)** isteklerin yarısının altında kaldığı süredir, yani "tipik" deneyim; **p95 / p99** ise
isteklerin %95'inin / %99'unun altında kaldığı süredir ve buna **kuyruk gecikmesi (tail
latency)** denir.

Kuyruk neden önemli? En yavaş istekler genellikle **en çok verisi olan** kullanıcıların
istekleridir. Üstelik tek bir sayfa arka planda onlarca çağrı yapıyorsa, kullanıcının en az bir
yavaş çağrıya denk gelme olasılığı yüksektir; buna **kuyruk gecikmesi büyütmesi (tail latency
amplification)** denir. Bu yüzden hizmet hedefini (SLO) yüzdelik dilimle yaz: "p99 < 300 ms"
ölçülebilir bir cümledir, "ortalama hızlı olsun" değildir.

## Kayıt sistemi, türetilmiş veri ve CDC
Büyük sistemlerde aynı veri birçok yerde durur: veritabanı, önbellek (cache), arama indeksi,
raporlama ambarı. Karışıklığı önleyen ayrım şudur: **kayıt sistemi (system of record)** bir
olgunun asıl ve yetkili kopyasının tutulduğu yerdir ve her veri için **tek** olmalıdır;
**türetilmiş veri (derived data)** ise kayıt sisteminden yeniden üretilebilen her şeydir —
indeks, önbellek, arama indeksi, materyalize görünüm.

"İki kayıt sistemi" kurmak en pahalı hatalardan biridir: ikisi ayrıştığında hangisinin doğru
olduğunu söyleyecek merci kalmaz. Aynı sebeple **çift yazma (dual write)** — aynı değişikliği
hem veritabanına hem arama indeksine ayrı ayrı yazmak — tehlikelidir; biri başarılı öteki
başarısız olduğunda sistem sessizce tutarsızlaşır.

Doğru yaklaşım **değişiklik verisi yakalamadır (Change Data Capture, CDC)**: veritabanının kendi
yazma kaydını bir olay akışına çevirip türetilmiş sistemleri bu tek akıştan beslersin. Tek bir
yazma noktası kalır ve türetilmiş her şey aynı sıralı akışı izler. Yalnız gerçekçi ol: bu akış
**yakın gerçek zamanlıdır (near real-time)**, anlık değil.

> **Şerh:** "Bu veritabanı sınırsız ölçeklenir" ve "sunucusuz veritabanı sıfır yönetim" gibi
> cümleler satıcı iddialarıdır. Ölçeklenen her sistem karşılığında bir şey verir — tutarlılık
> garantisi, aralık sorgusu, işlem desteği ya da para. Doğru soru "ölçeklenir mi" değil,
> **"benim yük desenimde neyi feda ediyor"** sorusudur.

## Öz-sınav (kendini test et)
1. Çok-lider replikasyon, tek-lidere göre neyi kazandırır ve karşılığında hangi problemi
   **kaçınılmaz** olarak doğurur? "Son yazan kazanır" çözümünün sessiz bedeli nedir?
2. Kullanıcı profilini güncelledi, sayfa yenilendi, eski ad görünüyor. Bu hangi olgunun
   sonucudur ve hangi garantiyle giderilir? Uygulanabilir bir yöntem söyle.
3. Zaman damgasını bölümleme anahtarı yapmak neden sıcak nokta üretir? Sıcak **anahtar** sorunu
   neden hash bölümlemeyle çözülmez ve önerilen çarenin okuma tarafındaki bedeli nedir?
4. B-ağacı ile LSM-ağacı arasındaki temel mekanik fark nedir? Yazma ağırlıklı bir iş yükünde
   hangisini seçersin ve karşılığında neyi kabul etmiş olursun?
5. "Veritabanı kullanıyoruz, eşzamanlılık sorunları bizde olmaz" cümlesi neden yanlıştır?
   Yazma çarpıklığını (write skew) kayıp güncellemeden (lost update) ayıran nedir?
6. Bir arama indeksinin kayıt sistemi olmadığını nereden bilirsin? Çift yazma yerine CDC
   kullanmak hangi arıza türünü ortadan kaldırır?

### Cevap anahtarı (önce kendin cevapla, sonra bak)
1. Kazanç: yazmanın kullanıcıya yakın yapılması ve bir bölge düşse bile yazmanın sürmesi.
   Kaçınılmaz problem: aynı kaydın iki yerde eşzamanlı değiştirilmesi, yani **çakışma**. "Son
   yazan kazanır" çakışmayı kapatır ama kaybeden yazmayı sessizce siler.
2. Asenkron replikasyonun **replikasyon gecikmesinin** sonucudur; **kendi yazdığını okuma**
   garantisiyle giderilir. Yöntem: kullanıcının kendi değiştirdiği veriyi liderden okumak.
3. Zaman damgası sıralıdır; bir andaki bütün yeni yazmalar aynı aralığa, dolayısıyla aynı bölüme
   düşer. Sıcak anahtarda sorun dağılım değil **tek** anahtarın yüküdür; hash onu yine tek bölüme
   gönderir. Çare anahtarı rastgele bir ekle bölmektir; bedeli okurken parçaları toplamaktır.
4. B-ağacı sayfayı diskte **yerinde** günceller; LSM önce belleğe alır, diske **ardışık** yazar,
   dosyaları arka planda birleştirir. Yazma ağırlıklı yükte LSM; karşılığında okumanın birden çok
   dosyaya bakması ve birleştirmenin gecikmeyi ara ara zıplatması kabul edilir.
5. Çünkü varsayılan yalıtım seviyesi genellikle serileştirilebilir değildir. Kayıp güncellemede
   iki işlem **aynı** kaydı yazar ve biri ötekini ezer; yazma çarpıklığında işlemler **farklı**
   kayıtları yazar, tek tek geçerlidir ama birlikte bir kuralı bozar — ancak serileştirilebilir
   yalıtım çözer.
6. Çünkü kaynak veritabanından **yeniden üretilebilir**; silinse yeniden kurulur. CDC, "iki yere
   ayrı ayrı yaz" sırasında birinin başarısız olup sistemlerin sessizce ayrışması arızasını
   ortadan kaldırır.
