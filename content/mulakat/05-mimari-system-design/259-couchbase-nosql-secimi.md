---
slug: couchbase-nosql-secimi
baslik: "Couchbase ve NoSQL Seçimi: Belge Modeli, Bellek Öncelikli Mimari ve Karar Matrisi"
kademe: İleri
sira: 259
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

İş ilanlarında sık geçen bir madde vardır: "RDBMS (Oracle, MSSQL) ve NoSQL (Couchbase, MongoDB) deneyimi". Bu madde aslında tek bir ürün sormaz. Sorduğu şey şudur: veriyi nereye koyacağına neye bakarak karar veriyorsun?

İlişkisel veritabanını zaten biliyorsun: veri tablolara bölünür, buna **normalizasyon (normalization)** denir. Sorgu anında tablolar JOIN ile birleştirilir. Para, muhasebe ve sipariş işlerinde bu model hâlâ en sağlam seçenektir.

Ama bazı ekranların **erişim deseni (access pattern)** bambaşkadır. Bir tarife ekranı düşün: saniyede on binlerce kez açılıyor ve her açılışta beş tablo birleştiriliyor. Dönen cevap ise hep aynı nesne olduğu için burada gerçek ihtiyaç JOIN değildir. İhtiyaç şudur: "tek okumada tüm nesneyi ver".

**Couchbase** tam bu ihtiyaca oturan bir **belge veritabanıdır (document database)**. Veriyi JSON belgeler hâlinde tutar, belleği önceler ve hem önbellek (cache) hem kalıcı depo olarak çalışabilir.

Bu konu bir ürün reklamı değildir. Amaç şu refleksi kurmandır: mülakatta "hangisi daha iyi" diye cevap verilmez, "erişim desenin ne" diye sorulur (`145`, `141`).

### Temel kavramlar

- **Belge (document)**: bir varlığın tamamını tutan JSON metni; satır ve sütun yerine iç içe alanlar vardır.
- **Denormalizasyon (denormalization)**: birlikte okunan veriyi birlikte saklamak; tekrar pahasına okuma ucuzlar.
- **Bellek öncelikli mimari (memory-first)**: yazma önce belleğe alınır, diske arkadan iner.
- **Kova, kapsam, koleksiyon (bucket, scope, collection)**: Couchbase'in üç seviyeli veri organizasyonu; kabaca veritabanı, şema ve tablo karşılığıdır.
- **N1QL (SQL++)**: JSON belgeler üzerinde çalışan, SQL'e çok benzeyen sorgu dili.
- **Dayanıklılık seviyesi (durability level)**: bir yazmanın kaç yerde onaylanacağını belirleyen ayar.
- **XDCR (cross data center replication)**: veri merkezleri arası kopyalama (replication).

### Belge veritabanı nasıl çalışır?

İlişkisel modelde bir müşteri üç tabloya dağılır: müşteri, adres, tercih. Ekranda ise üçü birden lazım olduğu için her istekte üç tablo birleştirilir. Veri tertemizdir ama okuma pahalıdır.

Belge modelinde aynı müşteri tek bir JSON belgesidir ve adres ile tercih o belgenin içindedir. Tek anahtarla tek okuma yaparsın, birleştirme hiç olmaz ve gecikme (latency) düşer.

Bunun bir bedeli var: aynı veri birden çok belgede tekrar edebilir. Bir şehir adı değişirse tek satır değil, birçok belge güncellenir. İlişkisel model bu tekrarı önler, belge modeli ise okumayı ucuzlatır; seçim tam burada yapılır.

Kural tek cümledir: **birlikte okunan veri birlikte saklanır**, ayrı ayrı okunan veri ise ayrı belgelere bölünür. Yani belgeyi tabloya göre değil, ekranın ihtiyacına göre tasarlarsın.

```svg
<svg viewBox="0 0 640 252" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ilişkisel normalizasyon ile belge denormalizasyonunun karşılaştırması">
  <defs>
    <marker id="cbase259-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı ekran, iki farklı saklama kararı</text>
  <rect x="14" y="40" width="276" height="196" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="152" y="63" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">İlişkisel: parçala, sorguda birleştir</text>
  <rect x="30" y="78" width="104" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="82" y="98" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">musteri</text>
  <rect x="30" y="116" width="104" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="82" y="136" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">adres</text>
  <rect x="30" y="154" width="104" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="82" y="174" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">tercih</text>
  <line x1="138" y1="93" x2="184" y2="124" stroke="#5D7789" stroke-width="1.4" marker-end="url(#cbase259-ok)"/>
  <line x1="138" y1="131" x2="184" y2="131" stroke="#5D7789" stroke-width="1.4" marker-end="url(#cbase259-ok)"/>
  <line x1="138" y1="169" x2="184" y2="138" stroke="#5D7789" stroke-width="1.4" marker-end="url(#cbase259-ok)"/>
  <rect x="192" y="110" width="82" height="44" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="233" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">JOIN</text>
  <text x="233" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her istekte</text>
  <text x="152" y="206" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">artı: tekrar yok, güncelleme tek yerden</text>
  <text x="152" y="224" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">eksi: her okumada birleştirme maliyeti</text>
  <rect x="350" y="40" width="276" height="196" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="488" y="63" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Belge: birlikte oku, birlikte sakla</text>
  <rect x="368" y="76" width="240" height="110" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="382" y="96" font-size="10.5" fill="#1C3247" font-family="system-ui">{ "id": "M-1042",</text>
  <text x="382" y="114" font-size="10.5" fill="#1C3247" font-family="system-ui">  "ad": "Ayse Yilmaz",</text>
  <text x="382" y="132" font-size="10.5" fill="#1C3247" font-family="system-ui">  "adres": { "sehir": "Ankara" },</text>
  <text x="382" y="150" font-size="10.5" fill="#1C3247" font-family="system-ui">  "tercihler": { "dil": "tr" },</text>
  <text x="382" y="168" font-size="10.5" fill="#1C3247" font-family="system-ui">  "surum": 3 }</text>
  <text x="488" y="206" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">artı: tek anahtar, tek okuma</text>
  <text x="488" y="224" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">eksi: veri tekrarı, güncelleme dağılır</text>
</svg>
```

Yukarıdaki belgede `surum` alanına dikkat et. Belge biçimi zamanla mutlaka değişir ve sürüm alanı olmadan eski belgelerle yenilerini birbirinden ayıramazsın.

### Couchbase'in karakteri

Couchbase'i mülakatta beş başlıkla anlatabilirsin.

**Bellek öncelikli mimari.** Yazma önce bellekteki katmana alınır ve istemciye onay oradan dönebilir; diske yazma ile kopyaya gönderme arkada devam eder. Gecikmenin bu kadar düşük olmasının sebebi budur.

**Dayanıklılık seviyesi.** "Onay aldım" demek her zaman "veri diskte" demek değildir. İstersen daha güçlü bir seviye talep edersin: yazma kopyaya (replica) ulaşsın, ya da diske insin. Güvenlik arttıkça gecikme de artar; bu bilinçli bir ödünleşimdir.

**memcached mirası.** Couchbase'in kökeninde bir önbellek sunucusu vardır ve ikili (binary) anahtar-değer protokolü hâlâ oradadır. Bu yüzden ayrı bir önbellek katmanı kurmadan da çok hızlı anahtar erişimi verebilir.

**Kova, kapsam, koleksiyon.** Veri bu üç seviyede düzenlenir; aynı kova içinde farklı iş alanlarını kapsamlara ayırabilirsin. SQL bilen biri için tanıdık bir hiyerarşidir.

**N1QL ve indeksler.** JSON belgeler üzerinde doğrudan SELECT yazarsın, ama sorgunun hızlanması için **ikincil indeks (GSI, global secondary index)** tanımlaman gerekir. İndekssiz sorgu tüm belgeleri tarar ve bu üretimde kabul edilmez.

```sql
SELECT m.ad, m.adres.sehir
FROM musteriler m
WHERE m.adres.sehir = "Ankara"
LIMIT 20;
```

Bunlara **XDCR** eklenir: veri merkezleri arasında sürekli kopyalama sağlar. Felaket kurtarma ve coğrafi yakınlık senaryolarında kullanılır.

```svg
<svg viewBox="0 0 640 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Couchbase bellek öncelikli yazma yolu ve dayanıklılık seviyeleri">
  <defs>
    <marker id="cbase259-akis" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
    <marker id="cbase259-onay" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yazma önce belleğe: onayı nereden istersin?</text>
  <rect x="14" y="88" width="104" height="52" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="66" y="112" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">İstemci</text>
  <text x="66" y="130" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Java uygulaması</text>
  <line x1="120" y1="102" x2="176" y2="102" stroke="#186399" stroke-width="1.6" marker-end="url(#cbase259-akis)"/>
  <text x="148" y="94" text-anchor="middle" font-size="10" fill="#186399" font-family="system-ui">yaz</text>
  <line x1="176" y1="128" x2="120" y2="128" stroke="#25804C" stroke-width="1.6" stroke-dasharray="5 3" marker-end="url(#cbase259-onay)"/>
  <text x="148" y="146" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">onay</text>
  <rect x="180" y="76" width="150" height="78" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="255" y="102" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Aktif düğüm</text>
  <text x="255" y="122" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">BELLEK</text>
  <text x="255" y="140" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">seviye 1: bellekte</text>
  <line x1="332" y1="98" x2="396" y2="70" stroke="#186399" stroke-width="1.5" marker-end="url(#cbase259-akis)"/>
  <line x1="332" y1="132" x2="396" y2="170" stroke="#186399" stroke-width="1.5" marker-end="url(#cbase259-akis)"/>
  <rect x="400" y="44" width="226" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="513" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kopya düğümün belleği</text>
  <text x="513" y="84" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">seviye 2: kopyada onaylı</text>
  <rect x="400" y="146" width="226" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="513" y="168" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Disk</text>
  <text x="513" y="186" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">seviye 3: diskte onaylı</text>
  <line x1="60" y1="216" x2="580" y2="216" stroke="#5D7789" stroke-width="1.2"/>
  <text x="60" y="236" font-size="10.5" fill="#25804C" font-family="system-ui">hızlı, garantisi zayıf</text>
  <text x="580" y="236" text-anchor="end" font-size="10.5" fill="#B23B3B" font-family="system-ui">yavaş, garantisi güçlü</text>
  <text x="320" y="256" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">seviyeyi iş kuralı seçer: para akışı sağa, sayaç sola</text>
</svg>
```

### Ne zaman hangisi? — karar matrisi

Bu bölüm konunun kalbidir. Mülakatta genelde "Couchbase mi MongoDB mi?" diye sorulur, ama doğru cevap bir ürün adı değildir. Doğru cevap, erişim desenini tarif etmektir.

**İlişkisel (Oracle, MSSQL).** Çok tablolu birleştirme var ve önceden bilinmeyen sorgular geliyor. Para ve muhasebe kuralları katı, işlem (transaction) ile ACID garantisi şart; cevap ilişkiseldir (`227`).

**Couchbase.** Okuma ve yazma hızı çok yüksek olmalı, veri esnek belge biçiminde. Üstelik hem önbellek hem kalıcı depo tek üründe isteniyor. Tipik işler: oturum durumu, kullanıcı profili, ürün ve tarife önbelleği.

**MongoDB.** Belge modeli yeterli, ama zengin sorgu ve **toplama (aggregation)** gerekiyor. Geniş ekosistem ve tanıdık araçlar burada avantajdır.

**Redis.** İş saf önbellek, sayaç, dağıtık kilit veya kuyruk. Kalıcılık ikincil bir istektir ve veri belleğe sığmalıdır (`155`, `157`).

**Cassandra.** Yazma hacmi çok yüksek, veri zaman serisi ve sorgu deseni önceden biliniyor. Bölüm anahtarı (partition key) tasarımı işin merkezindedir (`147`).

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Erişim desenine göre veri deposu seçim matrisi">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önce erişim deseni, sonra ürün adı</text>
  <rect x="14" y="36" width="376" height="30" rx="7" fill="#1C3247"/>
  <text x="30" y="56" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Erişim deseni ve ihtiyaç</text>
  <rect x="396" y="36" width="230" height="30" rx="7" fill="#1C3247"/>
  <text x="412" y="56" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Uygun depo</text>
  <rect x="14" y="72" width="376" height="38" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="30" y="96" font-size="11" fill="#1C3247" font-family="system-ui">Çok tablolu birleştirme, katı ACID, para ve muhasebe</text>
  <rect x="396" y="72" width="230" height="38" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="412" y="96" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">İlişkisel (Oracle, MSSQL)</text>
  <rect x="14" y="116" width="376" height="38" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="30" y="140" font-size="11" fill="#1C3247" font-family="system-ui">Esnek belge, çok yüksek okuma, önbellek de olsun</text>
  <rect x="396" y="116" width="230" height="38" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="412" y="140" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Couchbase</text>
  <rect x="14" y="160" width="376" height="38" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="30" y="184" font-size="11" fill="#1C3247" font-family="system-ui">Belge modeli, zengin sorgu ve toplama (aggregation)</text>
  <rect x="396" y="160" width="230" height="38" rx="7" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="412" y="184" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">MongoDB</text>
  <rect x="14" y="204" width="376" height="38" rx="7" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="30" y="228" font-size="11" fill="#1C3247" font-family="system-ui">Saf önbellek, sayaç, kilit, kuyruk; kalıcılık ikincil</text>
  <rect x="396" y="204" width="230" height="38" rx="7" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="412" y="228" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Redis</text>
  <rect x="14" y="248" width="376" height="38" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="30" y="272" font-size="11" fill="#1C3247" font-family="system-ui">Çok yüksek yazma, zaman serisi, bilinen sorgu deseni</text>
  <rect x="396" y="248" width="230" height="38" rx="7" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.3"/>
  <text x="412" y="272" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">Cassandra</text>
</svg>
```

Cevap kalıbını ezberle: önce erişim desenini sor, sonra tutarlılık ihtiyacını sor, en son ürün adını söyle. Bu sıra kıdemli cevabın imzasıdır (`141`).

Peki her iş için ayrı depo tutmak serbest mi? **Poliglot kalıcılık (polyglot persistence)** kâğıtta çok akıllıca görünür (`145`), ama sahada ciddi bir faturası vardır.

Her yeni depo şunları beraberinde getirir: ayrı işletme, ayrı yedekleme, ayrı izleme ve ayrı ekip bilgisi. Depolar arası tutarlılığı da artık sen taşırsın. "Her servise ayrı veritabanı" cümlesi kolay söylenir, pahalı yaşanır.

### Tuzaklar ve Java tarafı

**Şemasız, şema yok demek değildir.** Şema ortadan kalkmaz, sadece koda taşınır ve eski biçimdeki belgeler yenilerle bir süre yan yana yaşar. Bu yüzden belgeye sürüm alanı koyarsın ve okuma anında eskiyi yeniye çevirirsin.

**Belge büyümesi.** Bir belgenin içindeki listeye sürekli eleman eklenirse belge şişer ve en küçük güncelleme bile koca belgeyi okuyup yazmaya başlar. Çözüm, büyüyen listeyi ayrı belgelere bölmektir.

**Sıcak anahtar (hot key).** Tek bir anahtar tüm trafiği üstüne çeker ve o anahtarın düğümü tıkanır (`166`). Anahtar tasarımı yükü düğümlere dağıtmalıdır (`147`).

**Nihai tutarlılık (eventual consistency).** Kopyalar anında eşitlenmediği için az önce yazdığın kaydı okumaman mümkündür. Kullanıcı bunu "kaydettim ama görünmüyor" diye yaşar. Kritik akışta ya güçlü okuma iste ya da yazan kullanıcıyı kendi yazdığı düğüme yönlendir (`128`).

**Çift yazma (dual-write).** Hem veritabanına yazıp hem olay yayınlamak risklidir, çünkü biri olup diğeri olmayabilir. Çözüm **giden kutusu (outbox)** desenidir: olayı veriyle aynı atomik yazmada sakla, ayrı bir süreç yayınlasın (`130`, `116`).

Şimdi Java tarafına gelelim. İlanlarda "farklı ORM'ler" (object-relational mapping, nesne-ilişkisel eşleme) maddesi geçer ve doğru cevap bir ayrım içerir.

JPA ile Hibernate ilişkisel dünya içindir: nesneyi tabloya eşler, ilişkileri, tembel yüklemeyi ve birinci seviye önbelleği yönetir (`80`).

Belge deposunda bu iş daha basittir: nesnen zaten JSON'a çok yakındır, eşleme katmanı yalnız alan dönüşümü yapar. Tembel yükleme ve JOIN yönetimi diye bir dert kalmaz.

Spring tarafında ortak bir yüz vardır: **Spring Data Couchbase** ve **Spring Data MongoDB**. İkisi de `Repository` soyutlamasını sunar ve metot adından sorgu türetme burada da çalışır (`80`). Yani arayüz tanıdıktır, ama altındaki model bambaşkadır.

Bunu mülakatta tek cümleyle özetle: "ORM ilişkisel bir eşleme sorunudur; belge deposunda eşleme sorunu küçülür, yerine modelleme sorunu büyür."

### Mülakatta ne sorulur?

- **Couchbase'i neden seçersin?** Erişim deseni tek-okumalı, belge esnek ve önbellek ile kalıcı depo tek üründe isteniyorsa. Gerekçeyi ürünle değil desenle kur.
- **Belge modelinin bedeli nedir?** Veri tekrarı ve güncelleme dağılmasıdır: birlikte okunan veriyi birlikte saklarsın, karşılığında tek noktadan güncelleme kolaylığını kaybedersin.
- **"Yazma onaylandı" ne demek?** Varsayılanda veri belleğe alınmıştır; diskte veya kopyada olduğunu istiyorsan dayanıklılık seviyesini açıkça talep edersin.
- **Şemasız olmak avantaj mı?** Kısa vadede evet, uzun vadede disiplin ister. Şema koda taşınır; sürüm alanı ve okuma anında dönüşüm gerekir.
- **Couchbase mi Redis mi?** Redis saf önbellek, sayaç ve kilit içindir; kalıcılık ikincildir (`155`). Kalıcı belge deposu da isteniyorsa Couchbase konuşulur.
- **Veritabanına yazıp Kafka'ya olay yayınlamak?** Bu çift yazmadır. Giden kutusu deseniyle çöz, iki fazlı işlem (2PC) kurmaya kalkma (`130`).

### Tek cümlede

Couchbase, belgeyi bellek öncelikli tutup hem önbellek hem kalıcı depo olabilen bir NoSQL'dir; ama mülakatta değerli olan ürün bilgisi değil, erişim deseninden ürüne giden karar zinciridir.

# cbase-1 | Temel
## Soru
Belge (doküman) veritabanının ilişkisel modelden en temel farkı nedir?
## A
Veriyi yalnız bellekte tutar; ilişkisel veritabanı ise diske yazar
🤖 Bu bir depolama tercihidir, veri modeli farkı değil; belge veritabanları da diske yazar.
## B
Birlikte okunan veri tek bir JSON belgede birlikte saklanır; şema esnektir
🤖 Doğru: belge modeli varlığı bir arada tutar ve birleştirme ihtiyacını azaltır.
## C
Şema tümüyle ortadan kalkar; bu yüzden veri modelleme aşaması gerekmez
🤖 Şema yok olmaz, koda taşınır; modelleme belge modelinde de zorunludur.
## D
Yalnız önbellek görevi görür; kalıcı depo olarak kullanılamaz
🤖 Belge veritabanı kalıcı bir depodur; önbellek olabilmesi ayrı bir yetenektir.
## Doğru
B
## Açıklama
İlişkisel modelde veri **normalize** edilir: bir müşteri; müşteri, adres ve tercih tablolarına dağılır. Ekranda hepsi lazım olduğu için her istekte JOIN yapılır. Belge modelinde ise aynı müşteri **tek bir JSON belgesidir** ve adres ile tercih o belgenin içindedir. Tek anahtarla tek okuma yaparsın, birleştirme olmaz, gecikme düşer. Kural şudur: **birlikte okunan veri birlikte saklanır**. Bedeli veri tekrarıdır — bir şehir adı değişince birçok belge güncellenebilir. Şema da esnektir: her belge aynı alanlara sahip olmak zorunda değildir. Ama "esnek şema" ile "şema yok" aynı şey değildir; şema yalnızca veritabanından uygulama koduna taşınır.

# cbase-2 | Orta
## Soru
Couchbase'e bir yazma yaptın ve istemci "tamam" cevabı aldı. Varsayılan davranışta bu onay neyi gösterir?
## A
Verinin tüm kopyalarda diske yazıldığını garanti eder
🤖 En güçlü garanti budur ama varsayılan değildir; ayrıca talep edilmesi gerekir.
## B
Couchbase disk kullanmaz; onay verinin zaten kalıcı olduğunu gösterir
🤖 Couchbase diske de yazar; "bellek öncelikli" olmak "disksiz" demek değildir.
## C
Onay yalnız sorgu indeksinin güncellendiğini gösterir
🤖 İndeks güncellemesi ayrı bir konudur; onay yazma yolunu anlatır.
## D
Yazmanın belleğe alındığını; disk ve kopya garantisi ayrıca istenir
🤖 Doğru: dayanıklılık seviyesiyle "kopyada" ya da "diskte" onayı talep edilir.
## Doğru
D
## Açıklama
Couchbase **bellek öncelikli (memory-first)** çalışır. Yazma önce bellekteki katmana alınır ve istemciye onay oradan dönebilir. Diske yazma ile kopyaya (replica) gönderme arkada devam eder. Düşük gecikmenin kaynağı budur. Daha güçlü garanti istiyorsan **dayanıklılık seviyesi (durability level)** belirtirsin: yazma kopyanın belleğine ulaşsın, ya da diske insin. Güvenlik arttıkça gecikme de artar; bu bilinçli bir ödünleşimdir. Doğru mühendislik kararı iş kuralına bakar. Para hareketi güçlü seviyeyi hak eder, bir görüntülenme sayacı hak etmez. Mülakatta ayırt edici cümle şudur: "onay aldım" demek her zaman "veri diskte" demek değildir.

# cbase-3 | Orta
## Soru
SQL bilen bir ekip Couchbase'e geçiyor. Sorgulama konusunda hangi ifade doğrudur?
## A
JSON belgeler üzerinde N1QL (SQL++) ile SELECT yazılabilir
🤖 Doğru: SQL'e çok benzeyen bir sorgu dili doğrudan belgeler üzerinde çalışır.
## B
Yalnız anahtarla erişim vardır; SQL benzeri bir sorgu dili bulunmaz
🤖 Bu, memcached mirasına bakan eksik bir görüştür; N1QL sorgu imkânı sunar.
## C
Sorgu için verinin önce ilişkisel tablolara aktarılması gerekir
🤖 Aktarım gerekmez; sorgu doğrudan JSON belgeler üzerinde çalışır.
## D
N1QL için her belgenin aynı katı şemaya uyması zorunludur
🤖 Belge şeması esnektir; farklı alanlara sahip belgeler bir arada sorgulanabilir.
## Doğru
A
## Açıklama
**N1QL (SQL++)**, JSON belgeler üzerinde çalışan SQL benzeri sorgu dilidir. `SELECT m.ad, m.adres.sehir FROM musteriler m WHERE m.adres.sehir = "Ankara"` gibi bir sorgu tanıdık gelir. Fark şudur: iç içe alanlara noktayla inersin ve katı bir tablo şeması zorunlu değildir. Sorgunun hızlı çalışması için **ikincil indeks (GSI)** tanımlaman gerekir; indekssiz sorgu tüm belgeleri tarar ve üretimde kabul edilmez. Veri organizasyonu da üç seviyelidir: **kova → kapsam → koleksiyon (bucket → scope → collection)**. Bu kabaca veritabanı, şema ve tablo karşılığıdır. Couchbase'in memcached kökeni sebebiyle ikili anahtar-değer protokolüyle doğrudan anahtar erişimi de mümkündür; ama "yalnız anahtar var" demek yanlıştır.

# cbase-4 | İleri
## Soru
Bir tarife servisi tasarlıyorsun. Erişim deseni: saniyede on binlerce okuma, esnek belgeler, çok düşük gecikme. Ekip ayrı bir önbellek katmanı işletmek istemiyor ve verinin kalıcı olması da şart. Hangi seçim bu desene en iyi oturur?
## A
Redis; tüm veriyi bellekte tutar ve en düşük gecikmeyi verir
🤖 Redis hızlıdır ama kalıcılık ikincildir; arkada kalıcı bir depo yine gerekir.
## B
Cassandra; yatay ölçekli yazma kapasitesi bu yükü rahat karşılar
🤖 Cassandra çok yüksek yazma ve zaman serisi içindir; buradaki desen okuma ağırlıklı.
## C
Couchbase; bellek öncelikli erişimi kalıcı depoyla tek üründe birleştirir
🤖 Doğru: hem önbellek hem kalıcı depo ihtiyacı tek sistemde karşılanır.
## D
Oracle; ACID garantisi ve olgun optimizasyon her erişim desenine yeter
🤖 ACID güçlüdür ama burada asıl ihtiyaç birleştirme değil, tek-okuma hızıdır.
## Doğru
C
## Açıklama
Senaryodaki dört sinyali oku: okuma ağırlıklı yük, esnek belge, düşük gecikme ve "ayrı önbellek katmanı istemiyoruz". Bu dördü birlikte **Couchbase**'i işaret eder. Bellek öncelikli mimari düşük gecikmeyi verir, memcached mirası hızlı anahtar erişimini sağlar ve veri aynı üründe kalıcıdır. **Redis** de çok hızlıdır, ama kalıcılık orada ikincil bir özelliktir; genelde arkasında ayrı bir kalıcı depo bulunur (`155`). **Cassandra** çok yüksek yazma hacmi ve zaman serisi için tasarlanmıştır; burada yük okuma tarafındadır (`147`). **Oracle** ise çok tablolu birleştirme, katı ACID ve önceden bilinmeyen sorgular olduğunda öne çıkar (`227`). Mülakatta kritik olan cevabın sırasıdır: önce erişim desenini tarif et, sonra ürünü söyle (`141`).

# cbase-5 | İleri
## Soru
"Şemasız" bir belge deposunda uzun vadede karşılaşılan en gerçekçi risk nedir?
## A
Risk yoktur; alan eklemek serbest olduğu için uyumsuzluk oluşmaz
🤖 Alan eklemek serbesttir ama eski belgeler o alanı içermez; uyumsuzluk gerçektir.
## B
Veritabanı eski belgeleri yeni biçime otomatik olarak dönüştürür
🤖 Böyle bir otomatik göç yoktur; dönüşümü uygulama kodu üstlenir.
## C
Şema olmadığı için indeks tanımlanamaz ve sorgu yazılamaz
🤖 İndeks ve sorgu mümkündür; esnek şema sorgulamayı engellemez.
## D
Şema koda taşınır; eski ve yeni belge biçimleri bir arada yaşar
🤖 Doğru: sürüm alanı ve okuma anında biçim dönüşümü gerekir.
## Doğru
D
## Açıklama
"Şemasız" ifadesi yanıltıcıdır. Şema ortadan kalkmaz; veritabanından **uygulama koduna taşınır**. Bir alanı yeniden adlandırdığında veritabanı hiçbir şey söylemez, ama eski belgeler eski adı taşımaya devam eder. Sonuçta aynı koleksiyonda birden çok belge biçimi yan yana yaşar. Disiplinli çözüm iki parçalıdır. Birincisi belgeye bir **sürüm alanı** koymaktır. İkincisi okuma anında eski biçimi yeni biçime çeviren bir katman yazmaktır. İsteğe bağlı olarak arka planda kademeli göç de çalıştırılır. Bu tuzağın kardeşi **belge büyümesidir**: bir belgenin içindeki liste sürekli büyürse en küçük güncelleme bile koca belgeyi okuyup yazar; çözüm listeyi ayrı belgelere bölmektir. İndeks ve sorgu ise esnek şemayla gayet mümkündür.

# cbase-6 | Uzman
## Soru
Bir servis sipariş belgesini Couchbase'e yazıyor, ardından Kafka'ya "sipariş oluştu" olayını yayınlıyor. İkisi ayrı sistem olduğu için biri başarılı olup diğeri başarısız olabiliyor. En sağlam çözüm nedir?
## A
Her iki yazmayı try-catch'e alıp hata hâlinde ikinciyi yeniden denemek
🤖 Süreç iki adım arasında çökerse yeniden deneme hiç çalışmaz; olay kaybolur.
## B
Olayı veriyle aynı atomik yazmada saklayıp ayrı bir süreçle yayınlamak
🤖 Doğru: giden kutusu (outbox) deseni çift yazma sorununu kökten çözer.
## C
İki sistem arasında iki fazlı işlem (2PC) kurmak
🤖 2PC kırılgan, yavaş ve işletmesi pahalıdır; bu akışta tercih edilmez.
## D
Önce Kafka'ya yayınlayıp veritabanı yazmasını dinleyiciye bırakmak
🤖 Sıra tersine döner ama sorun sürer; yayınlanan olay veritabanına yazılamayabilir.
## Doğru
B
## Açıklama
Bu klasik **çift yazma (dual-write)** sorunudur: iki farklı sistemi tek bir atomik adımda güncelleyemezsin. Belge veritabanına yazma başarılı olup Kafka yayını başarısız olursa, sistem sipariş kaydeder ama kimse haberdar olmaz. Ters durumda ise olay yayınlanır, sipariş yoktur. Çözüm **giden kutusu (outbox)** desenidir (`130`, `116`): yayınlanacak olayı veriyle **aynı atomik yazmada** sakla. Belge deposunda bunun doğal yolu, olayı sipariş belgesinin içinde ya da tek işlemde yazılan bir yapıda tutmaktır. Ardından ayrı bir süreç bu kayıtları okuyup Kafka'ya yayınlar ve yayınlananları işaretler. Tüketici tarafında **tekrar edilse de sonucu değişmeyen (idempotent)** işleme şarttır, çünkü bu yaklaşım "en az bir kez" teslim eder. Yeniden deneme tek başına yetmez: süreç iki adım arasında çökerse deneyecek kimse kalmaz. İki fazlı işlem (2PC) teoride mümkündür ama pratikte kilitlenme, yavaşlık ve işletme yükü getirir; mesajlaşma dünyasında yerleşik çözüm giden kutusudur.
