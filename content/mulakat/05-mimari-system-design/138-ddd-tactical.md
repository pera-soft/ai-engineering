---
slug: ddd-tactical
baslik: "DDD Taktiksel Desenler: Entity, Value Object, Aggregate, Domain Event, Repository"
kademe: İleri
sira: 138
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**DDD** (Domain-Driven Design, alan güdümlü tasarım), yazılımı veritabanı tablolarından değil **işin kendi kavramlarından** yola çıkarak kurma yaklaşımıdır. Buradaki **alan** (domain), yazılımın çözdüğü iş dünyasıdır: bir e-ticaret sisteminde sipariş, müşteri, ödeme, kargo.

DDD'nin **taktiksel** (tactical) desenleri, bu kavramları koda dökerken kullanılan yapı taşlarıdır. Neden gerekli? "Her kavram bir sınıf olsun" dediğinde iki soru cevapsız kalır. Birincisi: iki nesnenin "aynı" olduğuna neye göre karar veriyoruz? İkincisi: bir kural bozulmasın diye hangi nesneler birlikte ve aynı anda korunmalı? Taktiksel desenler tam olarak bu iki soruya standart cevaplar verir. Böylece model kodda dağılmadan, kurallarını koruyarak yaşar.

### Temel kavramlar

- **Kimlik (identity):** bir nesneyi, alanları değişse bile "aynı nesne" yapan etiket (id). Kimlikle tanımlanan nesneye **Entity** (varlık) denir: `Musteri#42` adını ve adresini değiştirse de aynı müşteridir.
- **Value Object (değer nesnesi):** kimliği olmayan, yalnız **değeriyle** tanımlanan tiptir. İki örnek bütün alanları eşitse eşit sayılır. Ayrıca **değişmez (immutable)** olmalıdır: değiştirmek yerine yeni bir örnek üretilir (`Para`, `Adres`, `TarihAraligi`). Sezgisi şudur: cüzdanındaki 10 liralık banknotun hangisi olduğu önemsizdir (değer). Ama banka hesabının hangi hesap olduğu önemlidir (kimlik).
- **Değişmez (invariant):** her an doğru kalması gereken iş kuralı: "sipariş toplamı satır kalemlerinin toplamına eşittir", "stok negatif olamaz".
- **Transaction (işlem):** ya tamamı uygulanan ya da hiçbiri uygulanmayan veritabanı değişikliği kümesi. Tutarlılık garantisi buradan gelir.
- **Aggregate (bütünleşke):** bir değişmezi birlikte korumak zorunda olan Entity/Value Object kümesi ve etrafına çizilen **tutarlılık sınırı**. Dışarıya açılan tek kapı **aggregate root** (kök).
- **Repository:** aggregate kökünü sanki bellek içi bir koleksiyondan alıyormuş/saklıyormuş gibi soyutlayan arayüz.
- **Domain event (alan olayı):** alanda olmuş bitmiş, iş açısından anlamlı bir olgu; adı geçmiş zamandadır (`SiparisOnaylandi`).
- **Domain service:** doğal olarak tek bir nesneye ait olmayan alan mantığını taşıyan, durumsuz nesne.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aggregate sınırı ve kök üzerinden erişim">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aggregate: köke referans, içe değil</text>
  <rect x="60" y="44" width="230" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="175" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Siparis (aggregate)</text>
  <rect x="90" y="80" width="170" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="100" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Siparis (kök / root)</text>
  <rect x="90" y="118" width="170" height="28" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1"/>
  <text x="175" y="137" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">SatirKalemi (iç entity)</text>
  <rect x="90" y="152" width="170" height="28" rx="6" fill="#FFFFFF" stroke="#5D7789" stroke-width="1"/>
  <text x="175" y="171" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Para (value object)</text>
  <text x="440" y="112" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓ dışarıdan yalnız köke erişim</text>
  <text x="440" y="140" text-anchor="middle" font-size="12" fill="#B23B3B" font-family="system-ui">✗ iç nesneye doğrudan erişim</text>
  <text x="440" y="168" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">başka aggregate'e: id ile referans</text>
</svg>
```

### Nasıl çalışır?

Model kurarken ilk soru **kimlik sorusudur**: "bu şeyi neye göre aynı sayıyoruz?" Cevap "taşıdığı değerlere göre" ise Value Object, "kendine ait bir kimliği olduğu için" ise Entity. Value Object'i değişmez yapmak akıl yürütmeyi kolaylaştırır: paylaşılan bir `Para` nesnesi birinin elinde sessizce değişemez, dolayısıyla eşzamanlılık ve gizli yan etki sorunu doğurmaz.

İkinci soru **sınır sorusudur**: bir değişmez hangi nesneleri kapsıyorsa, aggregate sınırı oradan geçer. Sınırın üç kuralı vardır.

(1) Dışarıdan yalnız **köke** referans verilir. İç nesnelere doğrudan dokunulmaz; her değişiklik kökün metotlarından geçer. Böylece değişmez (invariant) hiç ihlal edilmez.

(2) **Bir işlemde (transaction) yalnız bir aggregate** değiştirilir. Çünkü tutarlılık sınırı aynı zamanda kilit sınırıdır. Birden fazla aggregate'i tek işlemde kilitlemek çekişme (contention) ve dağıtık işlem sorunu yaratır.

(3) Aggregate'ler birbirine **nesne referansıyla değil, kimlikle** bağlanır. Örneğin `Siparis` yalnız `MusteriId` tutar.

Peki iki aggregate'in birlikte güncellenmesi gerekiyorsa ne olur? Birinci aggregate kendi işleminde değişir. İşlem kalıcı yazıldıktan (commit) sonra bir **alan olayı (domain event)** yayınlanır. İkinci aggregate bu olayı **ayrı bir işlemde** işler. Bunun bedeli, ikisinin bir süre farklı görünmesidir; buna **nihai tutarlılık (eventual consistency)** denir. Kural bu yüzden "aggregate'i küçük tut"tur.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Entity kimlikle, value object değerle tanımlanır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kimlik mi, değer mi?</text>
  <rect x="40" y="42" width="250" height="142" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Entity: kimlikle</text>
  <text x="165" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Musteri#42 — adı değişse de</text>
  <text x="165" y="108" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">aynı nesne kalır</text>
  <text x="165" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eşitlik: id'ye göre</text>
  <text x="165" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değişebilir (mutable)</text>
  <rect x="350" y="42" width="250" height="142" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Value Object: değerle</text>
  <text x="475" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Para(10, TRY) — kimliksiz</text>
  <text x="475" y="108" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">alanları eşitse eşittir</text>
  <text x="475" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">eşitlik: alanlara göre</text>
  <text x="475" y="162" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değişmez (immutable)</text>
</svg>
```

### Mülakatta ne sorulur?

Klasik sorular şunlardır.

**Entity mi Value Object mi?** — Cevabı kimlik belirler; veritabanında ayrı tablo olması değil.

**Aggregate root ne yapar?** — Değişmezleri korur ve tek erişim kapısıdır. Anemik bir veri torbası değildir.

**Aggregate'ler arası referans nasıl kurulur?** — Kimlikle. Çünkü nesne referansı sınırları bulanıklaştırır, dev nesne grafikleri ve tembel yükleme (lazy loading) sorunları doğurur ve "tek işlem, tek aggregate" kuralını kırar.

**Repository ne kadar ince taneli olmalı?** — Tablo başına ya da sınıf başına değil, **aggregate root başına**. İç entity ve value object'lerin repository'si olmaz.

**Domain event nedir?** — Komut (command) değildir. Komut, gelecekte bir şeyin yapılmasını **emreder**; olay ise olmuş biteni **bildirir**. Teknik bir log kaydı ya da veritabanı commit'i de değildir.

**Domain service ile application service farkı nedir?** — Birincisi tek bir nesneye sığmayan alan kuralını taşır (para transferi gibi). İkincisi akışı yönetir: işlemi açar, repository'yi çağırır ve sonucu döner.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek transaction tek aggregate ve commit sonrası domain event">
  <defs>
    <marker id="ddt-arrow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek TX: tek aggregate, sonra olay</text>
  <rect x="30" y="50" width="200" height="110" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5" stroke-dasharray="6 4"/>
  <text x="130" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">transaction sınırı</text>
  <rect x="55" y="86" width="150" height="56" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="130" y="108" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Siparis (kök)</text>
  <text x="130" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">değişmezi korur</text>
  <line x1="230" y1="105" x2="300" y2="105" stroke="#5D7789" stroke-width="2" marker-end="url(#ddt-arrow)"/>
  <text x="265" y="97" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">commit</text>
  <rect x="304" y="82" width="150" height="46" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="379" y="102" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">SiparisOnaylandi</text>
  <text x="379" y="119" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">domain event</text>
  <line x1="454" y1="105" x2="520" y2="105" stroke="#5D7789" stroke-width="2" marker-end="url(#ddt-arrow)"/>
  <rect x="524" y="82" width="94" height="46" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="571" y="102" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">başka</text>
  <text x="571" y="119" text-anchor="middle" font-size="10.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">aggregate</text>
  <text x="320" y="182" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ikinci aggregate ayrı TX'te, olayla güncellenir (nihai tutarlılık)</text>
</svg>
```

### Tek cümlede

Taktiksel DDD iki soruya cevap verir. "Neye göre aynı?" sorusunu Entity ile Value Object ayrımıyla cevaplar. "Neyi birlikte tutarlı tutacağız?" sorusunu ise aggregate sınırıyla cevaplar: bu sınıra yalnız kök üzerinden erişilir, tek işlemde tek başına değişir ve dışarıya kimlikle bağlanır.

# ddt-1 | İleri
## Soru
Bir `Para` (Money) tipi tutar ve para birimi taşıyor; iki `Para` örneği aynı tutar ve birime sahipse "aynı" sayılıyor ve nesne hiç değiştirilmiyor. Bu tip DDD'de nedir?
## A
Entity; her `Para` örneğinin veritabanında benzersiz bir kimliği (id) vardır
🤖 `Para` kimlikle değil değeriyle tanımlanıyor; eşitlik alanlarına göre, bu Entity değil.
## B
Aggregate root; para daima bir tutarlılık sınırının kökünde yer alır
🤖 Kök olmak için değişmez koruyan bir sınır gerekir; `Para` sadece bir değer taşır.
## C
Value Object; kimliği yok, değeriyle tanımlanır ve değişmezdir
🤖 Doğru: değere göre eşitlik ve değişmezlik value object'in tanımıdır.
## D
Domain event; hesaplar arasında olmuş bir para hareketini temsil eder
🤖 `Para` bir olay değil bir değerdir; olmuş bitmiş bir olguyu ifade etmez.
## Doğru
C
## Açıklama
**Value Object**, kimliği (id) olmayan, yalnız **değeriyle** tanımlanan tiptir: iki örnek bütün alanları eşitse **eşit** sayılır (`equals`/`hashCode` alanlara göre yazılır). **Değişmez** (immutable) olmalıdır — değiştirmek yerine yeni bir örnek üretilir; bu, nesneyi paylaşmayı güvenli, davranışı öngörülebilir kılar. `Para`, `Adres`, `TarihAraligi` klasik örneklerdir. Buna karşılık **Entity** kimliğiyle tanımlanır: alanları değişse bile aynı nesne kalır (adını değiştiren bir `Musteri` hâlâ aynı müşteridir). `Para`'yı Entity yapmak (A) gereksiz kimlik yükü getirir ve değer semantiğini bozar; aggregate root (B) olmak ise korunacak bir değişmez ve sınır gerektirir.

# ddt-2 | İleri
## Soru
Aşağıdakilerden hangisi bir **aggregate root** (kök) sorumluluğunu doğru tanımlar?
## A
Aggregate içindeki değişmezleri (invariant) korur ve dışarıya tek erişim noktasıdır
🤖 Doğru: kök, sınır içi tutarlılığı garanti eden tek kapıdır.
## B
Veritabanındaki bir tabloyla birebir eşleşen, davranışsız bir veri taşıyıcısıdır
🤖 Kök veri torbası değil; değişmezleri koruyan davranış taşır, anemik değildir.
## C
Aggregate içindeki her nesneye dışarıdan doğrudan erişim ve değiştirme izni verir
🤖 Tersine; kök iç nesnelere doğrudan dış erişimi engeller.
## D
Birden çok aggregate'i tek bir transaction içinde güncelleyen servis katmanıdır
🤖 O bir servis işi değil; üstelik kural tek transaction'da tek aggregate'tir.
## Doğru
A
## Açıklama
**Aggregate root**, aggregate'in kimliğini taşıyan ve **değişmezlerini koruyan** tek nesnedir. Dış dünya aggregate ile **yalnız kök üzerinden** etkileşir; iç entity ve value object'lere doğrudan referans verilmez (C yanlış). Kök, iç durumu değiştiren her işlemi kendi metotlarından geçirerek değişmezlerin hiçbir an ihlal edilmemesini sağlar — örneğin "sipariş toplamı satır kalemleri toplamına eşittir" kuralı, satır ekleme/çıkarma yalnız `siparis.satirEkle(...)` üzerinden yapıldığı için korunur. Bu yüzden kök anemik bir veri torbası değil (B), davranış içeren zengin bir nesnedir. Birden çok aggregate'i tek transaction'da güncellemek (D) kural gereği kaçınılan bir şeydir; ikinci aggregate ayrı transaction'da, domain event ile güncellenir.

# ddt-3 | İleri
## Soru
İki aggregate arasında (örneğin `Siparis` ve `Musteri`) referans nasıl tutulmalıdır ve neden?
## A
Doğrudan nesne referansıyla (`Musteri musteri`); böylece nesne grafiğinde her yere gezilebilir
🤖 Doğrudan referans sınırları bulanıklaştırır, dev nesne grafikleri ve yükleme sorunu doğurur.
## B
Kimlikle (`MusteriId musteriId`); her aggregate kendi tutarlılık sınırında kalır
🤖 Doğru: id ile referans sınırları net tutar ve tek-aggregate transaction kuralını korur.
## C
Hiç referans tutulmamalı; iki aggregate birbirinin varlığını hiç bilmemelidir
🤖 İlişki gereklidir; yalnızca nesne yerine kimlikle ifade edilir, tamamen kesilmez.
## D
İkisi tek bir büyük aggregate'te birleştirilip aynı sınırın içine alınmalıdır
🤖 Birleştirmek aggregate'i şişirir, kilit ve çekişme yaratır; ayrılık bilinçli tasarımdır.
## Doğru
B
## Açıklama
Aggregate'ler birbirine **nesne referansıyla değil, kimlikle** (id) referans verir. Doğrudan nesne referansı (A) tutarlılık sınırlarını bulanıklaştırır; kontrolsüz büyük nesne grafikleri, tembel-yükleme (lazy loading) sorunları ve "bir transaction'da yalnız bir aggregate" kuralının ihlali riski doğar. `Siparis` yalnız `MusteriId` tutar; `Musteri` gerekiyorsa kendi repository'sinden ayrıca yüklenir. Bu, her aggregate'i **küçük ve bağımsız** tutar, eşzamanlılık çekişmesini azaltır ve hangi verinin hangi sınıra ait olduğunu netleştirir. İlişkiyi tamamen kesmek (C) ya da devasa tek aggregate kurmak (D) iki yanlış uçtur; ikincisi kilit alanını büyütüp sistemi yavaşlatır.

# ddt-4 | İleri
## Soru
DDD'de bir **domain event** (alan olayı) için hangisi doğrudur?
## A
Gelecekte yapılacak bir işi emreden komuttur; sistemden bir eylem talep eder
🤖 Emir "command"dir; domain event ise olmuş bir olguyu bildirir, emretmez.
## B
Yalnızca teknik bir loglama kaydıdır; iş açısından bir anlam taşımaz
🤖 Domain event iş açısından anlamlıdır; teknik log kaydı değildir.
## C
Bir veritabanı transaction'ının teknik olarak commit edildiğini bildirir
🤖 O altyapı ayrıntısı; domain event alan diliyle bir iş olgusunu ifade eder.
## D
Alanda olmuş bitmiş, iş açısından anlamlı bir olguyu geçmiş zamanla bildirir
🤖 Doğru: `SiparisOnaylandi` gibi geçmiş zamanlı, olmuş bir iş olgusudur.
## Doğru
D
## Açıklama
**Domain event**, alanda **zaten gerçekleşmiş**, iş açısından **anlamlı** bir olguyu ifade eder ve adı **geçmiş zamandadır**: `SiparisOnaylandi`, `OdemeAlindi`. Bu, gelecekte bir şey yapılmasını isteyen **command** (emir) ile taban tabana zıttır: komut reddedilebilir, olay olmuş bitmiştir ve reddedilemez (A yanlış). Domain event **ubiquitous language**'in parçasıdır; teknik log (B) veya veritabanı commit'i (C) gibi altyapı kavramları değildir. Genellikle transaction commit edildikten sonra yayınlanır ve başka aggregate'lerin ya da bounded context'lerin **ayrı transaction'da**, gevşek bağlı biçimde tepki vermesini sağlar; denetim izi (audit) de doğal bir yan faydasıdır.

# ddt-5 | İleri
## Soru
DDD'de **repository** hangi granülerlikte tanımlanmalıdır?
## A
Her veritabanı tablosu için bir repository; sınır tabloya göre çizilir
🤖 Tablo-başına repository altyapı odaklıdır; DDD'nin sınırı aggregate'tir, tablo değil.
## B
Her aggregate root için bir repository; kökü koleksiyon gibi alır ve saklar
🤖 Doğru: repository aggregate kökü başına tanımlanır, iç nesneler için değil.
## C
Her entity ve her value object için ayrı birer repository tanımlanır
🤖 İç entity/value object'lere ayrı repository verilmez; erişim kök üzerindendir.
## D
Tüm alan için tek bir genel repository; her tür nesne oradan alınır
🤖 Tek genel repository sınır ayrımını yok eder; her kök kendi repository'sini alır.
## Doğru
B
## Açıklama
**Repository**, yalnız **aggregate root başına** tanımlanır ve kökü sanki bellek içi bir **koleksiyondan** alıyormuş/saklıyormuş gibi soyutlar: `SiparisRepository.bul(id)`, `.kaydet(siparis)`. Böylece kalıcılık ayrıntısı (JPA, JDBC, NoSQL) alan modelinden gizlenir ve alan modeli veritabanını hiç bilmeden test edilebilir. İç entity ve value object'lerin kendi repository'si **olmaz** (C yanlış); onlara yalnız kök üzerinden erişilir, çünkü aggregate sınırı ve değişmezin korunması buna bağlıdır. Tablo başına (A) veya tek genel repository (D) yaklaşımları, DDD'nin sınır birimi olan **aggregate** yerine altyapıyı temel alır ve modeli parçalar.

# ddt-6 | İleri
## Soru
Bir "para transferi" işleminde tutar bir hesaptan çıkıp diğerine geçiyor; mantık ne tek başına `Gonderen` ne de `Alici` hesabına doğal olarak ait. Bu davranış nereye konmalıdır?
## A
Rastgele seçilen hesap sınıfının içine; ikisi de hesap olduğuna göre fark etmez
🤖 Keyfi yerleştirme modeli bozar; iki aggregate'i eşit ilgilendiren mantık hesaba ait değil.
## B
Sunum katmanındaki controller'a; isteği zaten orası karşıladığı için en pratik yer
🤖 İş kuralını controller'a koymak alan mantığını dışarı sızdırır (anemik model).
## C
Tek bir entity'ye ait olmayan bu alan mantığını taşıyan bir domain service'e
🤖 Doğru: birden çok aggregate'i ilgilendiren, tek nesneye ait olmayan mantık domain service'e gider.
## D
Bir value object'in içine; değişmez olduğu için en güvenli yer orasıdır
🤖 Değişmezlik burada ilgisiz; iki hesabı koordine eden davranış value object'e ait değil.
## Doğru
C
## Açıklama
**Domain service**, doğal olarak tek bir entity veya value object'e ait **olmayan**, genellikle birden fazla aggregate'i koordine eden alan mantığını taşır. Para transferi tipik örnektir: kural ne `Gonderen` ne de `Alici` hesabına tek başına aittir, ikisini birden ilgilendirir; bu yüzden bir `ParaTransferiServisi` (domain service) bu işi üstlenir. Domain service **alan dilinde** konuşur ve durumsuzdur (stateless). İş kuralını controller'a koymak (B) anemik model üretir, keyfi bir entity'ye tıkmak (A) veya value object'e koymak (D) modeli çarpıtır. Not: domain service, uygulama akışını yöneten **application service**'ten farklıdır — birincisi alan kuralını, ikincisi orkestrasyonu (transaction, repository çağrısı, yetki) taşır. İki hesap iki ayrı aggregate olduğundan, güncelleme çoğu tasarımda tek transaction yerine olayla, nihai tutarlılıkla yürütülür.
