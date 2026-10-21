---
slug: kafka-retention-compaction
baslik: "Retention ve Log Compaction: Tombstone, Changelog ve Tiered Storage"
kademe: Orta
sira: 112
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Sınıf defteri sonsuza kadar büyüyemez; bir yerde eski sayfalar atılmalıdır. İki farklı "temizlik" mantığı vardır:

- **Zamana ya da boyuta göre atmak:** "3 aydan eski sayfaları at" veya "defter şu kalınlığı geçmesin". Sayfada ne yazdığına bakılmaz.
- **Her öğrencinin yalnız son notunu tutmak:** Ali'nin notu önce 40, sonra 60 olduysa 40'ı sil, 60 kalsın. Burada **kimin** notu olduğu, yani anahtar önemlidir.

Kafka'da birincisi **saklama süresi (retention)**, ikincisi **anahtar başına son kaydı tutma (log compaction)**'dır. Kafka mesajı **okununca silmez**; ne zaman sileceğini bu iki politika belirler.

Buradaki defterin adı **log**'dur: kayıtların sırayla sonuna eklendiği, ortasından değiştirilmeyen bir dosya.

### Temel kavramlar

- **Saklama süresi (retention):** mesajın log'da ne kadar kalacağı — `retention.ms` (süre) veya `retention.bytes` (**bölüm (partition)** başına boyut). Bölüm, bir konunun (topic) paralel çalışan alt-günlüklerinden biridir. Silme **tüketimden bağımsızdır**: mesaj okunmuş olsun olmasın, eşiği aşan eskiler gider.
- **Segment (parça dosya):** bir bölümün diskteki dosya parçası. Log tek bir dev dosya değildir; arka arkaya gelen segmentlerden oluşur. Silme tek tek mesaj değil, **segment** düzeyinde yapılır. Şu an yazılmakta olan **aktif segmente (head segment)** dokunulmaz.
- **`cleanup.policy`:** `delete` (varsayılan; zaman/boyuta göre eski segmentleri sil), `compact` (anahtar başına son değeri tut) ya da `delete,compact` (ikisi birlikte).
- **Anahtar (key):** mesajın kime/neye ait olduğunu söyleyen alan (ör. `musteri-42`). Compaction anahtar bazlıdır; anahtarsız mesajda tekilleştirilecek bir şey yoktur.
- **Log compaction (anahtar başına son kaydı tutma):** her anahtar için yalnız **en son değeri** korumak; aynı anahtarın eski değerleri arka planda temizlenir.
- **Tombstone (silme işareti):** değeri **`null`** olan özel mesaj. "Bu anahtar silindi" bilgisini yayar; tüketiciler bu silmeyi görebilsin diye bir süre (`delete.retention.ms`) durur, sonra kendisi de temizlenir.
- **Sıkıştırma ≠ compaction:** `compression.type` veriyi baytça küçültür (lz4/zstd/gzip); `compact` ise kayıt eler. İsim benzerliği klasik bir tuzaktır.
- **Katmanlı depolama (tiered storage):** eski segmentleri S3 gibi ucuz uzak depoya taşımak.

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Log compaction key basina son deger ve tombstone">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Log compaction: her key için son değer + tombstone (null = sil)</text>
  <text x="40" y="56" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Compaction öncesi</text>
  <rect x="40" y="66" width="560" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="70" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">k1=A</text>
  <text x="140" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">k2=X</text>
  <text x="210" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">k1=B</text>
  <text x="285" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">k2=Y</text>
  <text x="360" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">k1=C</text>
  <text x="445" y="86" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">k2=null</text>
  <text x="530" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">k3=Z</text>
  <text x="40" y="132" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Compaction sonrası</text>
  <rect x="40" y="142" width="560" height="30" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="90" y="162" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">k1=C (son)</text>
  <text x="250" y="162" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">k3=Z (son)</text>
  <text x="430" y="162" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">k2 → tombstone ile silindi</text>
  <text x="320" y="198" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Offset'ler seyrekleşir ama monotonik kalır · aktif (head) segment dokunulmaz</text>
</svg>
```

### Nasıl çalışır?

**`delete` politikası:** temizleyici (cleaner) yalnız **kapanmış** segmentlere bakar. `retention.ms` süresini geçmiş ya da `retention.bytes` eşiğini aşan eski segmentleri **bütün hâlinde** siler. Aktif segment silinmez.

Bunun pratik bir sonucu var. Çok yavaş bir tüketici, mesajları işlemeye fırsat bulamadan kaçırabilir: mesaj hâlâ okunmayı beklerken saklama süresi dolar ve segment silinir. **Tüketicinin geride kalma miktarını (consumer lag)** izlemenin asıl gerekçesi budur.

**`compact` politikası:** temizleyici kapanmış segmentleri tarar, her anahtarın **son** değerini tutar, eskilerini atar. Üç incelik kritik:

1. Compaction **asenkrondur**. Bir anahtara yeni değer yazdığın anda eski değer kaybolmaz; temizleyici o segmente gelene kadar **eski değer de okunabilir**. Yani "her anahtar için tek değer" **nihai (eventual)** bir durumdur, anlık bir garanti değildir.
2. **Okuma konumları (offset) yeniden numaralanmaz.** Offset, bir mesajın bölüm içindeki sıra numarasıdır. Aradan kayıt kalktığı için numaralar **seyrekleşir** (0, 1, 4, 7 gibi), ama daima artmaya devam eder; tüketici şaşırmaz.
3. **Aktif (head) segmente dokunulmaz**; en taze veri her zaman ham hâliyle durur.

Varsayılan `delete` politikasında silme, anahtardan bağımsız olarak **zaman/boyut eşiğini** aşan eski segmentlere uygulanır:

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="cleanup policy delete eski segmentleri zamana veya boyuta gore siler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">cleanup.policy=delete: eşiği aşan eski segmentler silinir</text>
  <text x="40" y="52" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">eski ←  Partition segmentleri  → yeni</text>
  <rect x="40" y="64" width="130" height="52" rx="8" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.4" stroke-dasharray="4 3"/>
  <text x="105" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">segment 0</text>
  <text x="105" y="104" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">retention.ms aştı ✗</text>
  <rect x="180" y="64" width="130" height="52" rx="8" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.4" stroke-dasharray="4 3"/>
  <text x="245" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">segment 1</text>
  <text x="245" y="104" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">retention.bytes aştı ✗</text>
  <rect x="320" y="64" width="130" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="385" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">segment 2</text>
  <text x="385" y="104" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">eşik içinde → tutulur</text>
  <rect x="460" y="64" width="140" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="530" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">aktif (head)</text>
  <text x="530" y="104" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yeni yazma · silinmez</text>
  <text x="40" y="150" font-size="10.5" fill="#5D7789" font-family="system-ui">retention.ms → zamana göre, retention.bytes → partition boyutuna göre eşik. Silme SEGMENT düzeyindedir.</text>
  <text x="40" y="172" font-size="10.5" fill="#5D7789" font-family="system-ui">Silme tüketimden BAĞIMSIZDIR: mesaj okunmuş olsun olmasın, eşiği aşan eski segment gider.</text>
  <text x="40" y="194" font-size="10.5" fill="#186399" font-family="system-ui">Bir tüketici çok yavaşsa, işleyemeden mesaj retention ile silinebilir → veri "kaçırılır".</text>
</svg>
```

Compaction; **değişiklik günlüğü (changelog)** ve **veritabanı değişikliklerini yakalama (CDC — change data capture)** kalıplarının temelidir. Compaction açık bir topic, her anahtar için bir varlığın (kullanıcı, hesap, ürün) **güncel durumunu** taşır. Yani veritabanındaki **hazır sonuç tablosu (materialized view)** gibi okunabilir.

Tüketici topic'i baştan sona okur ve her anahtarın son değerini belleğe yükler. Böylece **durumu sıfırdan yeniden inşa edebilir**. Kafka Streams'in durum deposu (state store) ve Debezium'un CDC çıktısı tam olarak bu mantıkla çalışır.

**Katmanlı depolama (tiered storage)** ise depolamayı sıcak (yerel) ve soğuk (uzak, ucuz) diye ikiye ayırır. Eski segmentler S3 benzeri bir depoya taşınır. Böylece **Kafka sunucusunun (broker)** pahalı yerel diski taze veriye kalır; haftalarca veya aylarca saklama diski şişirmez. Bu bir sıkıştırma algoritması değildir ve `compact` politikasına bağlı değildir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tiered storage sicak segment yerel disk soguk segment uzak S3 depo">
  <defs>
    <marker id="kret-tier-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B26B00"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tiered Storage (KIP-405, 4.0 GA): sıcak yerelde, soğuk uzak depoda</text>
  <rect x="30" y="48" width="250" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="155" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Broker yerel diski (sıcak)</text>
  <rect x="50" y="88" width="90" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/><text x="95" y="108" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">yeni seg</text>
  <rect x="150" y="88" width="90" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/><text x="195" y="108" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">yeni seg</text>
  <text x="155" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">düşük gecikmeli, pahalı disk</text>
  <text x="155" y="158" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">sıcak veriye ayrılır</text>
  <path d="M286 108 L354 108" stroke="#B26B00" stroke-width="2" marker-end="url(#kret-tier-arrow)"/>
  <text x="320" y="98" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">eski segment</text>
  <text x="320" y="126" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">taşınır →</text>
  <rect x="360" y="48" width="250" height="120" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="485" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Uzak depo — S3 (soğuk)</text>
  <rect x="380" y="88" width="90" height="30" rx="5" fill="#FBF7EF" stroke="#B26B00" stroke-width="1.2"/><text x="425" y="108" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">eski seg</text>
  <rect x="480" y="88" width="90" height="30" rx="5" fill="#FBF7EF" stroke="#B26B00" stroke-width="1.2"/><text x="525" y="108" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">eski seg</text>
  <text x="485" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ucuz, yüksek kapasiteli</text>
  <text x="485" y="158" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">uzun retention ucuzlar</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Depolama hesaplamadan ayrışır; broker diski şişmez. Sıkıştırma değildir, compaction'a bağlı değildir.</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Kafka mesajı ne zaman siler?"** Tüketildiğinde değil, **retention eşiğinde**. Politika topic ve bölüm düzeyindedir; tüketiciye özel bir kopya ya da tüketiciye özel saklama yoktur.
- **"`cleanup.policy` seçenekleri?"** `delete`, `compact` ve ikisinin birleşimi `delete,compact`.
- **"Compaction bir sıkıştırma mıdır?"** Hayır. Sıkıştırma `compression.type`'tır; compaction **anahtar başına son değeri** tutmaktır.
- **"Tombstone nedir?"** Değeri `null` olan mesaj: anahtarın silindiğini yayar ve `delete.retention.ms` sonrası kendisi de temizlenir — kalıcı değildir.
- **"Compacted topic ne işe yarar?"** Changelog / CDC, state store, materialized view: durumu baştan okuyarak yeniden inşa etmek.
- **"Compaction ofsetleri değiştirir mi?"** Hayır; ofsetler seyrekleşir ama **monotonik** kalır, yeniden numaralanmaz.
- **"'Compacted = her anahtar için tek değer' doğru mu?"** Nihai olarak evet, **anlık olarak hayır**: asenkron çalışır ve aktif segmente dokunmaz.
- **"Tiered Storage nedir?"** KIP-405; eski segmentler uzak/ucuz depoda tutulur, depolama hesaplamadan ayrışır. ZooKeeper ile ilgisi yoktur, `compact` gerektirmez.

### Tek cümlede

`delete` zamana/boyuta göre eski **segmentleri** siler, `compact` her **anahtarın** son değerini tutar (silme `null` tombstone ile yayılır) ve compaction asenkron olduğu için "tek değer" anlık değil nihai bir durumdur.

# kret-1 | Temel
## Soru
Kafka'da retention (saklama) politikası ne yapar?
## A
Kafka her mesajı bir tüketici okur okumaz hemen siler; disk böylece boşaltılmış olur
🤖 silme tüketimden bağımsızdır; mesajlar saklama süresi boyunca durur.
## B
Mesajı tüketiciden bağımsız olarak zamana (`retention.ms`) veya boyuta (`retention.bytes`) göre saklar
🤖 Doğru: commit-log doğası gereği silme tüketime değil, retention eşiklerine bağlıdır.
## C
Mesajlar sonsuza dek saklanır; Kafka'da veri hiçbir koşulda otomatik olarak silinmez
🤖 retention ile mesajlar zamana/boyuta göre silinir; sonsuz saklama zorunlu değildir.
## D
Retention her tüketici için ayrı ayarlanır; tüketici okuyunca kendi kopyası silinmiş olur
🤖 retention topic/partition düzeyindedir, tüketiciye özel değildir.
## Doğru
B
## Açıklama
Kafka bir **commit-log** olduğundan mesajlar tüketilince silinmez (A yanlış). Ne zaman silineceğini **retention politikası** belirler: **zamana** göre (`retention.ms` — ör. 7 gün) veya **boyuta** göre (`retention.bytes` — partition başına). Eşiği aşan **eski segmentler** silinir; bu, **tüketici davranışından bağımsızdır** ve topic/partition düzeyinde ayarlanır (D yanlış — tüketiciye özel değildir). Sonsuz saklama zorunlu değildir (C); retention tam da veriyi sınırlı tutmak içindir (ya da bilinçli olarak çok uzun ayarlanır).

# kret-2 | Orta
## Soru
`cleanup.policy` hangi seçenekleri sunar?
## A
Yalnızca `delete` olabilir; Kafka'da mesaj temizliği için başka bir politika seçeneği hiç bulunmaz
🤖 delete dışında compact ve ikisinin birleşimi de vardır.
## B
`compact`, mesajları gzip ile sıkıştırarak diskte yer kazandıran bir politikadır; kayıt sayısı değişmez
🤖 compact sıkıştırma (compression) değildir; key başına son değeri tutmaktır. İsim benzerliği yanıltır.
## C
`compact`, topic'teki tüm eski mesajları anahtardan bağımsız olarak koşulsuz siler; geriye yeni mesajlar kalır
🤖 compact her key'in SON değerini korur; hepsini silmez.
## D
`delete` (zamana/boyuta göre silme), `compact` (key başına son değeri tutma) ya da ikisi birlikte (`delete,compact`)
🤖 Doğru: iki politika ve birleşimi mümkündür.
## Doğru
D
## Açıklama
`cleanup.policy` üç biçimde çalışır: **`delete`** (varsayılan) mesajları **zamana/boyuta** göre segment segment siler; **`compact`** her **anahtar** için yalnız **en son değeri** korur (geçmiş değerleri temizler); ve **`delete,compact`** ikisini birlikte uygular (hem compaction yapar hem de çok eski segmentleri sürede siler). `compact` bir **sıkıştırma** değildir (B — isim benzerliği yanıltır; sıkıştırma `compression.type`'tır) ve tüm mesajları silmez (C — key başına son değer kalır). Yalnız `delete` var demek (A) compaction tabanlı changelog/CDC kullanımlarını gözden kaçırmaktır.

# kret-3 | Orta
## Soru
Log compaction nasıl çalışır ve "tombstone" nedir?
## A
Her anahtar için yalnız son değeri korur; `null` değerli mesaj (tombstone) anahtarı silinmiş işaretler
🤖 Doğru: compaction key'in son halini tutar; tombstone anahtarın silinmesini yayar.
## B
Compaction, log'da kalan mesajların offset'lerini sıfırdan başlayarak yeniden numaralandırır; eski offset'ler geçersizleşir
🤖 compaction offset'leri değiştirmez; mevcut offset'ler korunur, yalnız aradakiler seyrekleşir.
## C
Tombstone, işaretlediği anahtarı kalıcı olarak, sonsuza dek log'da tutmaya yarar; hiçbir zaman temizlenmez
🤖 tombstone anahtarı silmek içindir; kendisi de bir süre (delete.retention.ms) sonra temizlenir.
## D
Compaction, anahtarı olmayan (null key) mesajlarda içeriğe bakarak tekilleştirme yapar; aynı içerikliler birleşir
🤖 compaction anahtar bazlıdır; key'siz mesajlarda tekilleştirilecek anahtar yoktur.
## Doğru
A
## Açıklama
**Log compaction**, her **anahtar (key)** için yalnızca **en son değeri** korur; aynı anahtarın eski değerleri arka planda temizlenir. Değeri **`null`** olan özel mesaj bir **tombstone**'dur: bir anahtarın **silindiğini** yayar (tüketiciler bu silmeyi görür), sonra `delete.retention.ms` süresi geçince tombstone da temizlenir (C tersini iddia eder — kalıcı değildir). Compaction **offset'leri yeniden numaralamaz** (B yanlış); mevcut offset'ler korunur, yalnız aradaki eski kayıtlar kalktığı için offset'ler **seyrekleşir** ama monotonik kalır. Compaction **anahtar** bazlı olduğundan key'siz mesajlarda anlamsızdır (D).

# kret-4 | İleri
## Soru
Compacted bir topic changelog / CDC senaryolarında nasıl kullanılır?
## A
Compacted topic yalnızca ham log arşivi içindir; güncel durum (state) taşıma amacı yoktur
🤖 compacted topic tam da key başına güncel durumu taşır; arşivden fazlasıdır.
## B
CDC (Change Data Capture) compaction ile uyumsuzdur; ikisi aynı topic'te kullanılamaz
🤖 Debezium gibi CDC araçları compaction'dan yararlanır; uyumsuzluk yoktur.
## C
Key başına son durumu tuttuğundan materialized view / changelog gibi okunur; durum yeniden inşa edilebilir
🤖 Doğru: key başına güncel değer, durumu baştan kurmayı (rebuild) mümkün kılar.
## D
Changelog için `cleanup.policy=delete` şarttır; compaction changelog semantiğini bozar
🤖 changelog tam da compaction ister; delete eski durumu kaybettirir.
## Doğru
C
## Açıklama
Compacted topic her **anahtar** için **son değeri** tuttuğundan, bir varlığın (kullanıcı, hesap, ürün) **güncel durumunu** taşır — yani bir **materialized view** / **changelog** gibi okunabilir. Bir tüketici topic'i baştan okuyup her anahtarın son değerini yükleyerek **durumu yeniden inşa edebilir** (state rebuild). Bu, **Kafka Streams state store** ve **Debezium CDC** gibi araçların temelidir (B'nin "uyumsuz" iddiası yanlış). Changelog için gereken politika **compact**'tir; `delete` (D) eski durumu silip yeniden inşayı bozardı. Compacted topic'i salt arşiv sanmak (A), durum taşıma yeteneğini görmezden gelmektir.

# kret-5 | Orta
## Soru
Tiered Storage (KIP-405) nedir?
## A
Tiered Storage, mesajları katman katman sıkıştırarak yer kazandıran bir sıkıştırma algoritmasıdır; depolama yeri değişmez
🤖 sıkıştırma değil; eski segmentleri uzak/ucuz depoya taşıyan bir depolama katmanlamasıdır.
## B
Eski log segmentlerini uzak ucuz depoya (S3 vb.) taşır; yerel disk sıcak veriye kalır
🤖 Doğru: sıcak veri yerelde, soğuk veri uzak depoda tutulur; uzun retention ucuzlar.
## C
Tiered Storage çalışmak için metadata'yı tutan ayrı bir ZooKeeper kümesi gerektirir; KRaft ile çalışmaz
🤖 ZooKeeper 4.0'da kaldırıldı; Tiered Storage'ın ZooKeeper ile ilgisi yoktur.
## D
Tiered Storage yalnızca `cleanup.policy=compact` ayarlı topic'lerde çalışır; delete politikasında tamamen devre dışıdır
🤖 Tiered Storage compaction'a bağlı değildir; ağırlıkla delete/retention'lı topic'lerde eski segmentleri taşır.
## Doğru
B
## Açıklama
**Tiered Storage** (KIP-405; 3.6'da erken erişim, **3.9'dan itibaren üretime hazır**), Kafka'nın depolamasını iki katmana ayırır: **sıcak** (yeni) segmentler broker'ın **yerel diskinde**, **soğuk** (eski) segmentler ise **S3** gibi **uzak, ucuz** bir depoda. Böylece uzun retention (haftalar/aylar) broker'ın pahalı yerel diskini **şişirmez**, depolama hesaplamadan ayrışır ve maliyet düşer. Bir sıkıştırma algoritması değildir (A), ZooKeeper gerektirmez (C — 4.0'da zaten yok) ve `compact`'e bağımlı değildir (D); tipik olarak retention'lı (delete) topic'lerde eski segmentleri katmanlar.

# kret-6 | İleri
## Soru
[TUZAK] "cleanup.policy=compact ayarladık, artık her anahtar için topic'te her zaman tek bir değer bulunur." Bu iddia neden tam doğru değildir?
## A
Doğrudur; compaction anlıktır, bir anahtarın eski değeri yeni değer yazılır yazılmaz hemen silinir
🤖 compaction anlık değildir; eski değer bir süre daha okunabilir kalır.
## B
Doğrudur; aktif (head) segment de yeni yazımla eşzamanlı biçimde anında compact edilir
🤖 compaction aktif (head) segmente dokunmaz; yalnız kapanmış segmentleri işler.
## C
Compaction offset sürekliliğini bozar; bu yüzden tüketici hangi değerin güncel olduğunu şaşırır
🤖 offset'ler monotonik kalır; compaction karışıklık yaratmaz, sadece asenkron çalışır.
## D
Compaction asenkron çalışır ve aktif (head) segmente dokunmaz; bir anahtarın eski değeri bir süre daha okunabilir
🤖 Doğru: compaction arka planda, kapanmış segmentlerde çalışır; eski değerler bir süre daha görünür.
## Doğru
D
## Açıklama
Compaction **arka planda, asenkron** çalışır ve **aktif (head) segmente dokunmaz** — yalnızca kapanmış segmentleri işler (B yanlış). Dolayısıyla bir anahtara yeni değer yazsan bile, eski değer **compaction thread'i o segmenti işleyene kadar** log'da kalır ve baştan okuyan bir tüketici tarafından **hâlâ görülebilir** (A'nın "anlık" iddiası yanlış). Offset'ler compaction sonrası **seyrekleşir** ama **monotonik** kalır; tüketici karışmaz (C yanlış). Yani "compacted topic = her anahtar için tek değer" **nihai (eventual)** bir durumdur, **anlık bir garanti değildir**. Uygulama, aynı anahtarın birden çok değerini geçici olarak görebileceğini varsaymalıdır (özellikle baştan okuma / rebuild sırasında).
