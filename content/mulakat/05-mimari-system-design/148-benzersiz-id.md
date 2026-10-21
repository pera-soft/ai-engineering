---
slug: benzersiz-id
baslik: "Dağıtık Benzersiz ID: UUID vs Auto-Increment, Snowflake, ULID, Sıralılık ve İndeks Dostluğu"
kademe: Orta
sira: 148
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

**Veritabanı**, uygulamanın kalıcı bilgilerini (kullanıcılar, siparişler, mesajlar) saklayan programdır. Orada duran her kaydın, diğerlerinden ayrılmasını sağlayan bir **kimliğe (id)** ihtiyacı vardır — insanların T.C. kimlik numarası gibi. Bu kimlik üç işe yarar: kaydı bulmak, ona atıf yapmak (bir sipariş hangi kullanıcıya ait?) ve iki kaydın birbirine karışmamasını sağlamak. Veritabanı dilinde adı **birincil anahtardır (primary key)**.

Tek bir veritabanı varsa iş kolaydır: veritabanı bir sayaç tutar, her yeni kayda bir öncekinden bir fazlasını verir (**auto-increment**). Ama bugünün sistemleri onlarca **sunucuda** aynı anda kayıt üretir; hatta farklı kıtalardaki veri merkezlerinde. O zaman soru şu olur: birbiriyle hiç konuşmayan makineler **asla çakışmayan** kimlikleri nasıl üretecek? Üstelik bu kimlik veritabanının **indeksini** yormadan üretilebilir mi? (İndeks, kayıtları hızlı bulmak için tutulan sıralı ek yapıdır.) Bu, göründüğünden ciddi bir tasarım problemidir.

### Temel kavramlar

- **Auto-increment**: veritabanının tuttuğu tek bir sayaçtan sırayla verilen numara.
- **UUID (v4)**: 128 bitlik, tamamen **rastgele** üretilen kimlik; koordinasyon gerektirmez.
- **Snowflake**: 64 bit içine zaman + makine + sıra bilgisi yerleştiren yapılandırılmış kimlik.
- **ULID / UUIDv7**: 128 bitlik ama **zaman-önekli**, dolayısıyla sıralanabilir modern kimlikler.
- **İndeks-dostu olmak**: yeni kayıtların indeksin sonuna eklenmesi; az sayfa bölünmesi, iyi yerellik.
- **Kümeleme (clustered) indeksi**: satırların fiziksel olarak birincil anahtar sırasına göre saklanması.

### Nasıl çalışır?

**Auto-increment** kompakttır, sıralıdır ve doğal olarak indeks dostudur. Ama üretimi **merkezîdir**: bir sonraki değeri veren tek bir sayaca bağımlıdır. Dağıtık ortamda bu üç sorun doğurur. Birincisi **darboğazdır**: her yeni satır o merkezî sayaca uğrar. İkincisi **tek arıza noktasıdır (SPOF)**: o düğüm düşünce kimlik üretimi durur. Üçüncüsü, birden çok yazar ya da bölge varsa aynı değerin iki yerde üretilme riskidir.

Ayrıca ardışık ve tahmin edilebilir kimlikler bir gizlilik açığı olabilir: dışarıdan biri kayıt sayısını ve komşu kayıtları tahmin edebilir. Blok/segment tahsisi (Hi/Lo) merkeziliği hafifletir; ama tümüyle kaldırmaz.

**UUIDv4** tam tersidir. 128 bitin neredeyse tamamı rastgeledir. Her düğüm başkasına danışmadan üretir ve çakışma pratikte imkânsızdır. Bedeli indekste ödenir.

Çoğu ilişkisel veritabanında birincil anahtar aynı zamanda **kümeleme indeksidir**: satırlar fiziksel olarak anahtar sırasına dizilir. Sıralı bir anahtarla yeni satırlar hep indeksin **sonuna** eklenir. Sonuç: tek bir sıcak sayfa, en az sayfa bölünmesi ve iyi önbellek yerelliği. Rastgele bir UUIDv4 ile ise her ekleme ağacın **rastgele bir yerine** düşer. Bu da sık **sayfa bölünmesi (page split)**, parçalanma (fragmentation) ve her eklemede farklı bir sayfayı belleğe getirme zorunluluğu demektir.

UUIDv4 zaman bilgisi taşımaz. Metin olarak saklanmak zorunda da değildir; 16 bayt ikili olarak tutulur. Çakışma riski de yoktur. Tek gerçek zaafı bu **rastgele ekleme** maliyetidir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Rastgele UUIDv4 ve zaman-sıralı id indeks ekleme farkı">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İndekse ekleme: rastgele mi, sıralı mı?</text>
  <rect x="28" y="44" width="272" height="140" rx="12" fill="#FBEEE6" stroke="#C25A2E" stroke-width="1.5"/>
  <text x="164" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#C25A2E" font-family="system-ui">UUID v4 (rastgele)</text>
  <text x="164" y="98" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">dağıtık üretim ✓, çakışma yok</text>
  <text x="164" y="120" text-anchor="middle" font-size="11" fill="#B23A48" font-family="system-ui">B-tree'de her yaprağa dağılır</text>
  <text x="164" y="142" text-anchor="middle" font-size="11" fill="#B23A48" font-family="system-ui">sayfa bölünmesi, zayıf yerellik</text>
  <text x="164" y="164" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">128-bit, yer kaplar</text>
  <rect x="340" y="44" width="272" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="72" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">Snowflake / ULID (sıralı)</text>
  <text x="476" y="98" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">dağıtık üretim ✓</text>
  <text x="476" y="120" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">zaman-önekli → kabaca sıralı</text>
  <text x="476" y="142" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">indeksin sonuna ekler, az bölünme</text>
  <text x="476" y="164" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">saat/makine id yönetimi gerekir</text>
</svg>
```

**Snowflake** (Twitter) ikisinin arasını bulur. 64 biti anlamlı alanlara böler: 1 işaret biti, **41 bit zaman damgası** (özel bir başlangıç anından itibaren milisaniye), **10 bit makine/düğüm kimliği** ve **12 bit sıra numarası**. Sıra numarası, aynı milisaniyede aynı düğümde üretilen kimlikleri birbirinden ayırır.

Kazançları üç tanedir. Her düğüm kendi makine kimliğiyle **başkasına danışmadan** üretir. Zaman en anlamlı bitlerde durduğu için kimlikler **kabaca sıralıdır (k-sorted)** ve indeks dostudur. 64 bit olduğu için UUID'nin yarısı kadar yer kaplar.

```svg
<svg viewBox="0 0 640 168" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Snowflake 64-bit kimliğinin alan yerleşimi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Snowflake 64-bit yerleşimi</text>
  <rect x="20" y="56" width="24" height="48" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.4"/>
  <rect x="44" y="56" width="330" height="48" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="209" y="85" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">zaman damgası (ms)</text>
  <rect x="374" y="56" width="120" height="48" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="434" y="85" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">makine id</text>
  <rect x="494" y="56" width="126" height="48" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="557" y="85" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">sıra no</text>
  <text x="32" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1 işaret</text>
  <text x="209" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">41 bit</text>
  <text x="434" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">10 bit</text>
  <text x="557" y="122" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">12 bit</text>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">zaman en anlamlı bitlerde → kabaca sıralı, indeks-dostu</text>
</svg>
```

Snowflake'in bedeli **saat bağımlılığıdır**. Benzersizlik "aynı düğüm + aynı milisaniye + sıra no" üçlüsünün tekliğine dayanır. Bir düğümün saati NTP düzeltmesiyle **geriye** giderse ne olur? Düğüm daha önce kullandığı milisaniyeye geri döner ve daha önce ürettiği bir kimliği tekrar üretebilir. Yani ortaya bir **çakışma riski** çıkar. Sıra numarası alanı yalnız *aynı* milisaniye içinde ayrım yapar; geriye giden saati telafi etmez.

Standart önlem şudur: son görülen zaman damgasını bellekte tut. Saat ondan geriye gitmişse üretimi **durdur** — ya hata dön ya da saat yetişene kadar bekle. Kayma büyükse düğümü havuzdan çıkar. Bunların yanında monoton (geri gitmeyen) bir saat kaynağı ve sıkı NTP disiplini kullan. İkinci bir işletme yükü de makine id'lerinin benzersiz atanmasıdır.

**ULID** (Universally Unique Lexicographically Sortable Identifier) ve **UUIDv7**, UUID'nin kolaylığı ile sıralı kimliğin indeks dostluğunu birleştirir. Yöntem basittir: en anlamlı bitlere **zaman damgası** konur, kalan bitler rastgele doldurulur. Böylece kimlikler **alfabetik sırayla dizilebilir** ve yeni kayıtlar indeksin sonuna yakın eklenir. Makine kimliği dağıtmak gibi bir koordinasyon da gerekmez. İkisi de 128 bittir: Snowflake'ten büyük, UUID ile aynı boydadır. Bir uyarı: oluşturma zamanını dışarıya sızdırırlar; gizlilik önemliyse dikkat et.

```svg
<svg viewBox="0 0 640 226" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dört id türünün dağıtık üretim, sıralılık ve boyut karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dört id türü: üretim, sıralılık, boyut</text>
  <rect x="20" y="38" width="600" height="34" fill="#EAF1F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="34" y="60" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Tür</text>
  <text x="270" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Dağıtık üretim</text>
  <text x="420" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Sıralı / indeks-dostu</text>
  <text x="575" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Boyut</text>
  <line x1="200" y1="38" x2="200" y2="210" stroke="#5D7789" stroke-width="1"/>
  <line x1="340" y1="38" x2="340" y2="210" stroke="#5D7789" stroke-width="1"/>
  <line x1="510" y1="38" x2="510" y2="210" stroke="#5D7789" stroke-width="1"/>
  <text x="34" y="94" font-size="11" fill="#1C3247" font-family="system-ui">Auto-increment</text>
  <text x="270" y="94" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">✗ merkezi</text>
  <text x="420" y="94" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓</text>
  <text x="575" y="94" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">64-bit</text>
  <line x1="20" y1="106" x2="620" y2="106" stroke="#5D7789" stroke-width="0.7"/>
  <text x="34" y="128" font-size="11" fill="#1C3247" font-family="system-ui">UUID v4</text>
  <text x="270" y="128" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓</text>
  <text x="420" y="128" text-anchor="middle" font-size="12" fill="#8A2E2E" font-family="system-ui">✗ rastgele</text>
  <text x="575" y="128" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">128-bit</text>
  <line x1="20" y1="140" x2="620" y2="140" stroke="#5D7789" stroke-width="0.7"/>
  <text x="34" y="162" font-size="11" fill="#1C3247" font-family="system-ui">Snowflake</text>
  <text x="270" y="162" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓</text>
  <text x="420" y="162" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓</text>
  <text x="575" y="162" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">64-bit</text>
  <line x1="20" y1="174" x2="620" y2="174" stroke="#5D7789" stroke-width="0.7"/>
  <text x="34" y="196" font-size="11" fill="#1C3247" font-family="system-ui">ULID / UUIDv7</text>
  <text x="270" y="196" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓</text>
  <text x="420" y="196" text-anchor="middle" font-size="12" fill="#25804C" font-family="system-ui">✓</text>
  <text x="575" y="196" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">128-bit</text>
  <rect x="20" y="38" width="600" height="172" fill="none" stroke="#5D7789" stroke-width="1.2"/>
</svg>
```

### Mülakatta ne sorulur?

- **Dağıtıkta auto-increment neden sakıncalı?** Üretim tek bir merkezî sayaca bağlıdır: darboğaz, tek arıza noktası ve çok yazarlı kurulumda çakışma riski. Sorun boyut ya da sıralılık değildir; auto-increment zaten kompakt ve sıralıdır.
- **UUIDv4'ü kümeleme anahtarı yapmanın bedeli?** Rastgele ekleme: sayfa bölünmesi, fragmentasyon, zayıf önbellek yerelliği. Çakışma veya "metin olarak saklanma" değil.
- **Snowflake 64 bitte neyi kodlar?** Zaman damgası + makine id + sıra numarası.
- **ULID/UUIDv7 neyi hedefler?** Zaman öneki sayesinde sıralanabilirliği ve indeks dostluğunu hedefler. Dağıtık üretim avantajını korurken UUIDv4'ün rastgelelik sorununu çözer. Daha kısa değildir, şifreleme yapmaz ve merkezî bir servis gerektirmez.
- **UUID mü auto-increment mi?** Bu bir ödünleşimdir: bir yanda koordinasyonsuz üretim ve tahmin edilemezlik, öbür yanda kompaktlık ve doğal sıralılık. Modern orta yol Snowflake, ULID ve UUIDv7'dir.
- **Snowflake düğümünün saati geri giderse?** Daha önce üretilmiş kimlikler tekrar üretilebilir; düğüm saat yetişene kadar üretimi durdurmalıdır.

### Tek cümlede

Dağıtık kimlik tasarımı, "koordinasyonsuz üretilebilme" ile "indeksi yormayacak kadar sıralı olma" arasındaki gerilimi çözme işidir. Snowflake ve ULID/UUIDv7 bu ikisini aynı anda vermeye çalışır.

# uid-1 | Orta
## Soru
Onlarca uygulama sunucusunun aynı anda kayıt oluşturduğu dağıtık bir sistemde, birincil anahtar olarak veritabanı auto-increment'ine güvenmenin temel sakıncası nedir?
## A
Auto-increment değerleri her yeniden başlatmada sıfırlanır; benzersizlik bozulur
🤖 Sayaç kalıcıdır, yeniden başlatmada sıfırlanmaz; sorun üretimin merkeziliğidir.
## B
Auto-increment id'ler indeks-dostu değildir; sık sayfa bölünmesine yol açarlar
🤖 Tam tersi; sıralı oldukları için indeks-dostudurlar, bölünme sorunu UUIDv4'ündür.
## C
Auto-increment yalnız 32-bit tamsayı destekler; milyarlarca kayıtta taşma yaşanır
🤖 64-bit (bigint) sayaçlar standarttır; tip genişliği asıl mesele değildir.
## D
Üretim tek bir merkezî sayaca bağlıdır; bu darboğaz ve tek nokta hatası yaratır
🤖 Doğru: dağıtık ortamda merkezî sayaç ölçeklenmez ve tek nokta bağımlılığı doğurur.
## Doğru
D
## Açıklama
Auto-increment sıralı ve indeks-dostudur ama üretimi **merkezîdir**: bir sonraki değeri veren tek bir sıralayıcıya (genelde tek bir veritabanı) bağımlıdır. Dağıtık bir sistemde bu birkaç sorun doğurur: (1) **darboğaz** — her yeni satır o merkezî sayaca gitmelidir; (2) **tek nokta hatası (SPOF)** — o düğüm düşerse id üretimi durur; (3) **çoklu-yazar/çoklu-bölge** kurulumda (birden çok master) aynı değerin iki yerde üretilmesi **çakışma** riski. Ayrıca ardışık, tahmin edilebilir id'ler bir gizlilik/güvenlik açığı olabilir (kayıt sayısını ve başka kayıtları tahmin etme). Bu yüzden dağıtık sistemlerde koordinasyonsuz üretilebilen id'ler (UUID, Snowflake, ULID) tercih edilir. Not: DB tarafı segment/blok tahsisi (örn. Hi/Lo, sequence blokları) merkeziliği hafifletebilir ama tamamen kaldırmaz.

# uid-2 | İleri
## Soru
UUIDv4'ü (tamamen rastgele) büyük bir tablonun kümeleme (clustered) birincil anahtarı yapmanın performans dezavantajı nedir?
## A
Rastgelelik yüzünden eklemeler indeksin her yerine dağılır; sayfa bölünmesi artar
🤖 Doğru: rastgele anahtar sıralı ekleme yerelliğini bozar, ekleme maliyetini artırır.
## B
UUIDv4'te çakışma olasılığı yüksektir; benzersizlik pratikte garanti edilemez
🤖 128-bit rastgelede çakışma pratikte imkânsızdır; sorun ekleme dağınıklığıdır.
## C
UUIDv4 yalnız metin olarak saklanabilir; ikili karşılaştırma hiç yapılamaz
🤖 UUID 16 bayt ikili olarak saklanıp karşılaştırılabilir; sorun bu değildir.
## D
UUIDv4 zaman bilgisi taşıdığı için üretim sırası dışarıdan tahmin edilebilir
🤖 UUIDv4 zaman bilgisi *içermez* (tamamen rastgeledir); bu iddia yanlıştır.
## Doğru
A
## Açıklama
UUIDv4 **tamamen rastgeledir** ve sıra bilgisi taşımaz. Çoğu ilişkisel veritabanında birincil anahtar aynı zamanda **kümeleme (clustered) indeksidir** — satırlar fiziksel olarak anahtar sırasına göre saklanır. Sıralı bir anahtarla (auto-increment) yeni satırlar hep indeksin **sonuna** eklenir: tek bir sıcak sayfa, minimum bölünme, iyi önbellek yerelliği. Rastgele bir UUIDv4 ile ise her ekleme B-tree'nin **rastgele bir yerine** düşer → sık **sayfa bölünmeleri (page split)**, fragmentasyon ve her ekleme farklı bir sayfayı belleğe getirdiğinden **zayıf önbellek yerelliği**. Sonuç: özellikle yazma-yoğun büyük tablolarda belirgin performans ve şişme maliyeti. Çözüm: sıralanabilir bir kimlik (Snowflake, ULID, UUIDv7) kullanmak veya UUID'yi kümeleme anahtarı yapmayıp ayrı sıralı bir kümeleme anahtarı tutmak.

# uid-3 | Orta
## Soru
Twitter Snowflake id'si 64 bit içinde tipik olarak neyi kodlar?
## A
Hiçbir yapısı yoktur; 64 bitin tamamı kriptografik olarak rastgele üretilir
🤖 Snowflake yapılandırılmıştır; rastgele değil, alanlara bölünmüş bir düzeni vardır.
## B
Zaman damgası (timestamp) + makine/düğüm id + aynı milisaniyedeki sıra numarası
🤖 Doğru: bu üç alan Snowflake'i hem dağıtık üretilebilir hem zamanla sıralı kılar.
## C
UUIDv4'ün ilk 64 biti; yani kısaltılmış bir rastgele kimlik değeri saklanır
🤖 Snowflake bir UUID kırpması değildir; alanları anlamlı ve yapılandırılmıştır.
## D
Üreten sunucunun IP adresi ile port numarasının birleştirilmiş hâli tutulur
🤖 Makine kimliği ayrı ve kısa bir düğüm numarasıdır; IP/port kodlanmaz.
## Doğru
B
## Açıklama
**Snowflake** (Twitter) 64-bit'i anlamlı alanlara böler: (yaklaşık) 1 işaret biti + **41 bit zaman damgası** (özel bir epoch'tan itibaren ms) + **10 bit makine/düğüm id** + **12 bit sıra numarası** (aynı ms içinde aynı düğümde üretilen id'leri ayırır). Bu tasarımın kazançları: (1) **dağıtık üretim** — her düğüm kendi makine id'siyle koordinasyonsuz üretir, çakışma olmaz; (2) **kabaca sıralı (k-sorted)** — zaman damgası en anlamlı bitlerde olduğundan id'ler zamanla artar, bu da **indeks-dostudur** (eklemeler indeksin sonuna yakın gider); (3) **kompakt** — 64-bit, UUID'nin yarısı. Bedeli: düğümlerin senkron saatlere ihtiyacı (saat geriye giderse sorun) ve makine id'lerinin benzersiz atanması gereğidir.

# uid-4 | İleri
## Soru
ULID (ve benzeri UUIDv7), UUIDv4'e kıyasla hangi temel avantajı hedefler?
## A
128 bit yerine 64 bit'e sığması; böylece indekste çok daha az yer kaplaması
🤖 ULID de 128-bit'tir (UUID ile aynı boy); avantajı boyut değil, sıralanabilirliktir.
## B
Zaman damgasını gizleyip kimliğin oluşturma anını dışarıdan saklaması
🤖 Aksine ULID oluşturma zamanını *açığa çıkarır*; gizlemek gibi bir amacı yoktur.
## C
Üretimi merkezî bir servise taşıyıp boşluksuz artan numara garantisi vermesi
🤖 ULID de dağıtık/koordinasyonsuz üretilir; boşluksuz sayı garantisi vermez.
## D
Zaman-önekli olduğu için sözlüksel sıralanabilmesi ve indeks-dostu olması
🤖 Doğru: ULID zamanı öne koyar, sıralılık ile dağıtık üretimi birleştirir.
## Doğru
D
## Açıklama
**ULID** (Universally Unique Lexicographically Sortable Identifier) ve **UUIDv7**, UUIDv4'ün dağıtık üretim (koordinasyonsuz, çakışmasız) avantajını korurken onun en büyük zaafını — **rastgeleliğin indekse verdiği zarar** — gidermeyi hedefler. Bunu, kimliğin **en anlamlı bitlerine zaman damgası** koyarak yapar: aynı milisaniyede üretilen id'ler rastgele son eklerle ayrışır, ama genel sıralama zamanı izler. Sonuç: id'ler **sözlüksel olarak sıralanabilir** ve yeni kayıtlar B-tree indeksin **sonuna yakın** eklenir — Snowflake'teki gibi az sayfa bölünmesi, iyi yerellik. ULID 128-bit'tir (UUID ile aynı boyutta, Snowflake'ten büyük) ve makine id koordinasyonu gerektirmez; buna karşılık oluşturma zamanını dışarıya sızdırır. Böylece "UUID'nin kolaylığı + sıralı id'nin indeks dostluğu" bir arada sunulur.

# uid-5 | Orta
## Soru
UUID (dağıtık üretilebilir) ile auto-increment (sıralı, kompakt) arasındaki seçim esasen neyin ödünleşimidir?
## A
Koordinasyonsuz dağıtık üretim ile kompaktlık ve doğal sıralılık arasındaki denge
🤖 Doğru: biri dağıtık üretim/gizlilik, diğeri boyut/sıralılık lehine; seçim ihtiyaca göredir.
## B
Güvenlik ile hız arasındaki denge; UUID daima daha yavaş ama daha güvenlidir
🤖 Fazla indirgeyici; UUID'nin bedeli "yavaşlık" değil rastgele ekleme maliyetidir.
## C
Depolama ile okuma hızı arasındaki denge; UUID daha az yer kaplar ama yavaştır
🤖 UUID (128-bit) auto-increment'ten *daha çok* yer kaplar; öncül yanlıştır.
## D
Yalnız satıcıya bağlı bir tercih; PostgreSQL UUID, MySQL auto-increment zorunlu kılar
🤖 İki veritabanı da her iki yaklaşımı destekler; seçim teknik ihtiyaca bağlıdır.
## Doğru
A
## Açıklama
Seçim birkaç eksenin ödünleşimidir. **Auto-increment**: kompakt (küçük int/bigint), doğal olarak sıralı ve dolayısıyla **indeks-dostu**; ama merkezî üretim (dağıtık ortamda darboğaz/SPOF) ve tahmin edilebilirlik dezavantajları var. **UUIDv4**: her düğümde **koordinasyonsuz** üretilir (dağıtık sistem için ideal), tahmin edilemez (gizlilik); ama 128-bit ve **rastgele** olduğundan kümeleme indeksinde ekleme maliyeti yüksektir. Karar bağlama göre verilir: tek DB + basitlik istiyorsan auto-increment; dağıtık üretim şartsa UUID ailesi. Modern orta yol, **Snowflake / ULID / UUIDv7** ile "dağıtık üretim + sıralılık"ı birleştirmektir — böylece ikisinin de iyi yanları alınır. "UUID daima yavaş/güvenli" gibi genellemeler yanıltıcıdır; doğru çerçeve üretim modeli, boyut ve indeks davranışı ekseninde düşünmektir.

# uid-6 | İleri
## Soru
Snowflake id üreten bir düğümün saati, NTP düzeltmesi yüzünden birkaç saniye **geriye** alınırsa ne olur ve tipik önlem nedir?
## A
Hiçbir şey olmaz; sıra numarası alanı geriye giden saati kendiliğinden telafi eder
🤖 Sıra no yalnız aynı milisaniye içinde ayrım yapar; geriye giden saati telafi etmez.
## B
Id'ler kalıcı olarak çakışır; bu yüzden Snowflake tek düğümde çalıştırılmalıdır
🤖 Risk geçicidir ve önlenebilir; Snowflake tam da çok düğümlü üretim için tasarlandı.
## C
Daha önce üretilmiş id'ler yeniden üretilebilir; düğüm saat düzelene kadar beklemelidir
🤖 Doğru: aynı (zaman, makine, sıra) üçlüsü tekrar üretilebilir; beklemek standart önlemdir.
## D
Yalnız sıralama bozulur; benzersizlik hiçbir koşulda etkilenmez, önlem almak gerekmez
🤖 Sıralamanın yanı sıra benzersizlik de risk altındadır; önlem almak şarttır.
## Doğru
C
## Açıklama
Snowflake'te benzersizlik **"aynı düğüm + aynı milisaniye + sıra numarası"** üçlüsünün tekliğine dayanır; hiçbir merkezî kontrol yoktur. Saat geriye alınırsa düğüm daha önce kullandığı bir milisaniyeye geri döner, sıra numarası da o milisaniye için sıfırdan başlar — yani **daha önce ürettiği bir id'yi tekrar üretebilir**. Sıra numarası alanı yalnız *aynı* milisaniye içindeki id'leri ayırır, zamanda geriye gidişi telafi etmez. Standart önlem, son görülen zaman damgasını bellekte tutmak ve saat ondan geriye gitmişse **üretimi durdurmaktır**: küçük kaymalarda saat yetişene kadar beklenir, büyük kaymalarda hata dönülür ve düğüm havuzdan çıkarılır. Ek olarak monoton saat kaynağı ve sıkı NTP disiplini (adım atlamak yerine "slew" ile kademeli düzeltme) kullanılır. Aynı işletme dikkati makine id'lerinin benzersiz atanması için de gerekir — iki düğüm aynı makine id'sini alırsa çakışma yine kaçınılmazdır.
