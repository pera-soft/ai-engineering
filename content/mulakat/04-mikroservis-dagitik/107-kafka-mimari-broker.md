---
slug: kafka-mimari-broker
baslik: "Kafka Mimarisi: Dağıtık Commit-Log, Broker/Cluster ve KRaft"
kademe: Orta
sira: 107
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Bir okul düşün: gün içinde olan her şey büyük bir deftere **sırayla** yazılıyor — "08.10 zil çaldı", "08.15 yoklama alındı", "09.00 birinci ders başladı". Defteri okuyan öğretmen okuduğu satırı **silmez**; sadece "ben 12. satıra kadar okudum" diye kendine not tutar. Ertesi gün başka bir öğretmen aynı defteri baştan okuyabilir, çünkü yazılanlar hâlâ oradadır.

Kafka tam olarak böyle bir defterdir. İki program birbirine doğrudan telefon açar gibi bağlanırsa (biri çağırır, öbürü yanıt verene kadar bekler), karşı taraf kapalıyken iş durur. Araya defteri koyarsak: yazan yazar, okuyan hazır olduğunda okur. Programların birbirine mesaj bırakarak haberleşmesine **mesajlaşma** (messaging) denir; Kafka bunun dağıtık ve kalıcı biçimidir.

### Temel kavramlar

- **Mesaj / olay (message, event):** deftere yazılan tek satır. Bir **anahtarı** (key) ve bir **değeri** (value) olur.
- **Günlük (log):** yalnızca **sona eklenen (append-only)** defter. Araya satır sokulmaz; yazılmış satır sonradan değişmez. Kafka'nın tuttuğu bu yapının adı **sona ekleyen kayıt defteri (commit log)**'dir.
- **Üretici (producer):** deftere yazan program. **Tüketici (consumer):** defteri okuyan program.
- **Konu (topic):** deftere verilen ad, ör. `siparisler`. Mantıksal bir akıştır.
- **Bölüm (partition):** bir topic'in fiziksel parçası. Defteri birkaç cilde ayırmak gibidir: ciltler paralel işlenir, **her cilt kendi içinde sıralıdır**.
- **Okuma konumu (offset):** bir bölüm içindeki satır numarası. Sürekli artar (monotonik) ve **yalnız o bölümde** anlamlıdır; konunun tamamı için tek bir sayaç yoktur.
- **Segment:** bir bölümün diskteki dosya parçaları. Silme tek tek mesaj değil, **segment** düzeyinde yapılır.
- **Kafka sunucusu (broker):** defterleri saklayan sunucu. Birkaç broker bir araya gelip bir **küme (cluster)** kurar; bölümler bu broker'lara dağıtılır.
- **Saklama süresi (retention):** mesajın defterde ne kadar kalacağı. Kafka mesajı **okununca silmez**; süre ya da boyut eşiği dolunca siler.
- **Dayanıklılık (durability):** yazılan verinin çökme sonrasında da duruyor olması. Kafka bunu diske yazarak ve kopyalayarak sağlar.

Klasik **mesaj kuyruğu** (message queue) ile fark tam burada: kuyrukta mesaj okunur okunmaz kaybolur; Kafka'da defterde kalır, isteyen ofseti geri sarıp aynı veriyi tekrar okur.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kafka topic partition offset yapisi ve commit-log">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Topic → Partition → append-only log (offset ile)</text>
  <text x="40" y="60" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">siparisler (topic)</text>
  <rect x="40" y="72" width="420" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="30" y="94" font-size="11" fill="#5D7789" font-family="system-ui">P0</text>
  <rect x="52" y="78" width="40" height="22" rx="3" fill="#D3E4F0" stroke="#186399" stroke-width="1"/><text x="72" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">0</text>
  <rect x="96" y="78" width="40" height="22" rx="3" fill="#D3E4F0" stroke="#186399" stroke-width="1"/><text x="116" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1</text>
  <rect x="140" y="78" width="40" height="22" rx="3" fill="#D3E4F0" stroke="#186399" stroke-width="1"/><text x="160" y="93" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">2</text>
  <rect x="184" y="78" width="40" height="22" rx="3" fill="#F4F8FB" stroke="#8FB4CE" stroke-width="1" stroke-dasharray="3 2"/><text x="204" y="93" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">3</text>
  <text x="250" y="94" font-size="11" fill="#25804C" font-family="system-ui">→ yeni yazma sona eklenir</text>
  <text x="40" y="140" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Broker cluster</text>
  <rect x="40" y="150" width="120" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/><text x="100" y="170" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Broker 1</text><text x="100" y="187" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">P0, P2</text>
  <rect x="172" y="150" width="120" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/><text x="232" y="170" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">Broker 2</text><text x="232" y="187" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">P1</text>
  <rect x="304" y="150" width="156" height="46" rx="8" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.5"/><text x="382" y="170" text-anchor="middle" font-size="11" fill="#B5701B" font-family="system-ui">Controller quorum</text><text x="382" y="187" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">KRaft metadata (Raft)</text>
  <text x="484" y="90" font-size="11" fill="#B4322B" font-family="system-ui" transform="rotate(90 484 90)">okuma silmez</text>
</svg>
```

### Nasıl çalışır?

1. İstemci, `bootstrap.servers` ile verilen adreslerden **birine** bağlanır. Bu adres yalnızca bir **giriş kapısıdır (keşif / discovery)**. İstemci oradan kümenin haritasını indirir: hangi broker'lar var, her bölümün **lideri** kim. Bu haritanın adı **küme bilgisi (metadata)**'dir. Bütün broker'ları listelemek şart değildir; biri kapalıyken diğerinden girilebilsin diye birkaç adres yazılır.
2. Üretici mesajı, yazacağı bölümün **liderine** yollar; mesaj günlüğün **sonuna** eklenir ve bir ofset alır.
3. Broker mesajı diske **sıralı** olarak yazar. Veri önce işletim sisteminin **sayfa önbelleğine** (OS page cache) düşer; yani sıcak veri fiilen RAM'den servis edilir.
4. Tüketici "şu bölümde 5001. ofsetten itibaren ver" der ve nerede kaldığını kendisi takip eder. Okuma mesajı **silmez**, ofseti geri sarıp aynı veriyi tekrar okuyabilir.
5. Bölüm dosyası tek parça değildir: **segment**lere bölünür. Yeni yazma daima **aktif (head)** segmente gider; saklama eşiği dolunca yalnız **kapanmış** segmentler silinir.

Bölüm dosyasının diskteki görünümü şöyledir:

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Partition log segmentlere bolunur eski segment retention ile silinir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Partition = sıralı segment zinciri · retention eski segmenti siler</text>
  <text x="30" y="52" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">P0 log dosyası — offset artışı →</text>
  <rect x="30" y="66" width="130" height="60" rx="8" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.4" stroke-dasharray="4 3"/>
  <text x="95" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">segment 0</text>
  <text x="95" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">base offset 0</text>
  <text x="95" y="119" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">retention → sil</text>
  <rect x="172" y="66" width="130" height="60" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="237" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">segment 1</text>
  <text x="237" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">base offset 5000</text>
  <text x="237" y="119" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.log + .index</text>
  <rect x="314" y="66" width="130" height="60" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="379" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">segment 2</text>
  <text x="379" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">base offset 9000</text>
  <text x="379" y="119" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.log + .index</text>
  <rect x="456" y="66" width="150" height="60" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="531" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">aktif (head)</text>
  <text x="531" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">base offset 13000</text>
  <text x="531" y="119" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yeni yazma buraya</text>
  <text x="30" y="152" font-size="10.5" fill="#5D7789" font-family="system-ui">Silme/saklama segment düzeyindedir: yalnız KAPANMIŞ segmentler silinebilir; aktif segmente dokunulmaz.</text>
  <text x="30" y="172" font-size="10.5" fill="#5D7789" font-family="system-ui">Her segment kendi .log (veri) ve .index (offset→konum) dosyalarını taşır; boyut segment.bytes ile ayarlanır.</text>
  <text x="30" y="192" font-size="10.5" fill="#186399" font-family="system-ui">Aktif segment dolunca kapanır ve yeni bir aktif segment açılır → retention penceresine girer.</text>
</svg>
```

Kümenin kendine ait bir kayıt defteri daha vardır: hangi konular var, her bölümün lideri kim, kopyalar hangi sunucuda duruyor. Buna **küme bilgisi (metadata)** denir.

Kafka bu bilgiyi uzun süre ayrı bir **ZooKeeper** kümesinde tuttu. **Kafka 4.0 ile ZooKeeper tamamen kaldırıldı.** Artık metadata **KRaft (Kafka Raft)** ile yönetilir. Seçilmiş **denetleyiciler (controller)** bir **çoğunluk kurulu (Raft quorum)** oluşturur ve bilgi yine dahili bir log'da tutulur. Raft, bir grup sunucunun oy çokluğuyla ortak karara varmasını sağlayan bir uzlaşma algoritmasıdır.

Kazanç üç başlıkta toplanır: işletilecek tek sistem, daha hızlı lider devri (failover) ve çok daha yüksek bölüm ölçeği. KRaft bir "deneme modu" değildir; üretim için resmî ve **tek** moddur.

Verimin mimari sırrı ise **kopyasız aktarım (zero-copy)**'dır. `sendfile` sistem çağrısı sayesinde veri, diskten ya da sayfa önbelleğinden doğrudan ağ soketine geçer. Arada Java'nın kendi belleğine (JVM heap) kopyalanmaz; böylece her mesaj için iki gereksiz kopyalama adımı ortadan kalkar.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zero-copy sendfile OS page cache ile yuksek throughput">
  <defs>
    <marker id="kmimari-zc-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Zero-copy: disk/page-cache → soket, user-space'e kopyasız</text>
  <rect x="200" y="46" width="240" height="42" rx="8" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.4" stroke-dasharray="4 3"/>
  <text x="320" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">User-space kopya (JVM heap)</text>
  <text x="320" y="80" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">zero-copy bunu ATLAR ✗</text>
  <rect x="40" y="120" width="150" height="54" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="115" y="143" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Disk (segment)</text>
  <text x="115" y="161" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıralı okuma</text>
  <rect x="245" y="120" width="150" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="143" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">OS page cache</text>
  <text x="320" y="161" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıcak veri RAM'de</text>
  <rect x="450" y="120" width="150" height="54" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="525" y="143" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Ağ soketi (NIC)</text>
  <text x="525" y="161" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tüketiciye</text>
  <path d="M190 147 L243 147" stroke="#25804C" stroke-width="2" marker-end="url(#kmimari-zc-arrow)"/>
  <path d="M395 147 L448 147" stroke="#25804C" stroke-width="2" marker-end="url(#kmimari-zc-arrow)"/>
  <text x="320" y="112" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">sendfile() — tek sistem çağrısı</text>
  <text x="40" y="200" font-size="10.5" fill="#5D7789" font-family="system-ui">Veri diskten/page-cache'ten doğrudan sokete geçer: gereksiz bellek kopyaları ve context-switch elenir.</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Kafka bir mesaj kuyruğu mudur?"** Hayır — **dağıtık, kalıcı bir commit-log**'dur. Tüketim mesajı silmez; silme yalnız retention'a bağlıdır ve tüketiciden bağımsızdır. Bu yüzden birden çok tüketici grubu aynı veriyi bağımsız okuyabilir.
- **"Offset global midir?"** Hayır, **bölüme (partition) özeldir** ve daima artar. Broker kimliği de zaman damgası da değildir.
- **"Kafka 4.0'da ZooKeeper?"** Yoktur. Tek metadata modu **KRaft**'tır; controller quorum'u metadata'yı bir Raft log'unda tutar.
- **"`bootstrap.servers` ne işe yarar?"** Yalnız ilk bağlantı ve keşif noktasıdır; gerçek yönlendirme metadata'dan gelir, tüm broker'ları listelemek gerekmez.
- **"Kafka neden bu kadar hızlı?"** Sıralı disk erişimi + **OS page cache** + **zero-copy (`sendfile`)** + batching. "Her şeyi JVM heap'inde/RAM'de tutuyor" cevabı yanlıştır.
- **"Topic ile partition farkı?"** Topic mantıksal ad, partition fiziksel ve sıralı parçadır; hem paralelliğin hem sıra garantisinin birimi partition'dır.

### Tek cümlede

Kafka, olayları bölümlere ayrılmış ve yalnız sonuna eklenen bir günlükte tutan dağıtık olay akışı platformudur. Günlük Kafka sunucularına (broker) dağıtılır ve okunduğunda silinmez. Küme bilgisi 4.0'dan beri KRaft ile yönetilir; hız ise sayfa önbelleği ve kopyasız aktarımdan gelir.

# kmimari-1 | Temel
## Soru
Kafka'yı en doğru tanımlayan ifade hangisidir?
## A
Dağıtık, kalıcı bir commit-log'dur; mesaj okununca silinmez, saklama süresince tekrar okunabilir
🤖 Doğru: Kafka append-only bir log'dur; tüketim mesajı silmez, farklı tüketiciler kendi offset'iyle tekrar okur.
## B
Geleneksel bir mesaj kuyruğudur; mesaj tüketilir tüketilmez kuyruktan silinir ve bir daha okunamaz
🤖 Bu klasik kuyruk (queue) davranışıdır; Kafka'nın commit-log modeli mesajı okununca silmez.
## C
Olayları tablolarda tutan ilişkisel bir veritabanıdır; birincil erişim yolu SQL sorgularıdır
🤖 Kafka bir veritabanı değil, olay akış platformudur; birincil erişim SQL değil, offset ile sıralı okumadır.
## D
Yalnızca bellek içi (in-memory) bir önbellektir; sunucu yeniden başlarsa veriler kaybolur
🤖 Aksine Kafka veriyi diske yazar ve saklama süresi boyunca kalıcı tutar; yeniden başlatmada veri durur.
## Doğru
A
## Açıklama
Kafka bir **dağıtık commit-log**tur: mesajlar partition'ların sonuna eklenir ve tüketildiklerinde **silinmez**. Silme, tüketimden bağımsız olarak **saklama politikasıyla** (retention) yapılır. Bu yüzden birden çok tüketici grubu aynı veriyi bağımsız okuyabilir, bir tüketici geçmişe dönüp offset'i geri sararak tekrar işleyebilir. Klasik kuyruk (B) mesajı tüketince siler; Kafka'yı kuyruk sanmak sıralama, tekrar-okuma ve tüketici grubu davranışını yanlış modellemene yol açar.

# kmimari-2 | Temel
## Soru
Topic ile partition arasındaki ilişki nedir?
## A
Bir topic her zaman tek bir partition'dan oluşur; birden fazla partition oluşturulamaz
🤖 Tam tersine bir topic ölçeklenmek için birden çok partition'a bölünebilir; tek partition sadece özel bir durumdur.
## B
Topic mantıksal bir isimdir; bir veya daha çok partition'a bölünür, her partition sıralı bir log'dur
🤖 Doğru: partition topic'in fiziksel, sıralı parçasıdır; paralelliğin birimidir.
## C
Topic ile partition eş anlamlıdır; ikisi de aynı fiziksel dosyayı gösteren iki addır
🤖 Farklıdırlar: topic mantıksal akış, partition onun fiziksel, offset'li parçasıdır.
## D
Partition'ları tüketici kendi tarafında dinamik oluşturur; broker'da kalıcı bir karşılığı yoktur
🤖 Partition'lar topic yapılandırmasıyla broker'da oluşturulup saklanır; tüketici onları okur, yaratmaz.
## Doğru
B
## Açıklama
**Topic** mantıksal bir akış adıdır; ölçeklenebilmesi için fiziksel olarak **partition**'lara bölünür. Her partition **kendi içinde sıralı, append-only bir log**tur ve broker'lara dağıtılır. Partition sayısı hem tüketici paralelliğini (bir grup içinde bir partition'ı en fazla bir tüketici okur) hem de sıralama garantisinin kapsamını belirler. Topic'i tek partition sanmak (A) veya topic=partition demek (C) Kafka'nın ölçekleme modelini bozar.

# kmimari-3 | Orta
## Soru
Kafka'da "offset" kavramı için hangisi doğrudur?
## A
Offset, mesajı hangi broker'ın tuttuğunu gösteren broker kimliğidir; partition'dan bağımsızdır
🤖 Offset broker kimliği değil; partition içindeki konum numarasıdır.
## B
Offset, topic'teki tüm partition'lar arasında paylaşılan tek bir global sayaçtır; iki partition aynı offset'i kullanmaz
🤖 Offset global değildir; her partition kendi bağımsız offset dizisini tutar.
## C
Offset, bir partition içinde mesajın monotonik artan konumudur; partition ise segment adlı dosyalara bölünür
🤖 Doğru: offset partition'a özel, sürekli artan konumdur; depolama segment segment yapılır.
## D
Offset, mesajın oluşturulma zaman damgasıdır (timestamp); aynı anda gelen iki mesaj aynı offset'i alır
🤖 Zaman damgası ayrı bir alandır; offset ise konum sırasıdır, zamandan bağımsızdır.
## Doğru
C
## Açıklama
**Offset**, bir partition içindeki mesajın **monotonik artan konum numarasıdır** ve yalnızca o partition kapsamında anlamlıdır — farklı partition'lar bağımsız offset dizileri kullanır (B yanlış). Fiziksel depolamada partition, **segment** adı verilen dosyalara bölünür; saklama ve silme (retention) bu segmentler üzerinden yürür. Tüketici, işlediği son offset'i saklayarak kaldığı yerden devam eder. Offset'i broker kimliği (A) veya zaman damgası (D) sanmak, tüketici konumu ve tekrar-okuma mantığını yanlış kurar.

# kmimari-4 | Orta
## Soru
Kafka 4.0'da cluster metadata yönetimi ve ZooKeeper'ın durumu için hangisi doğrudur?
## A
Kafka 4.0 çalışmak için hâlâ zorunlu olarak ayrı bir ZooKeeper kümesi gerektirir
🤖 4.0 ile ZooKeeper bağımlılığı tamamen kaldırıldı, artık zorunlu değil (mevcut bile değil).
## B
4.0'da ZooKeeper ve KRaft eşit desteklenir; kurulumda ikisinden birini seçebilirsin
🤖 Geçiş sürümlerinde ikisi bir arada bulunuyordu; 4.0'da ZooKeeper modu tamamen kaldırıldı, seçenek yok.
## C
KRaft yalnızca test/geliştirme ortamı içindir; üretimde metadata için ZooKeeper şarttır
🤖 KRaft üretim için GA'dır ve 4.0'da tek metadata modudur; test-özel değildir.
## D
4.0'da ZooKeeper kaldırıldı; metadata KRaft ile controller quorum'unda bir Raft log olarak tutulur
🤖 Doğru: metadata KRaft (Kafka Raft) controller quorum'unda yönetilir; ZooKeeper 4.0'da yoktur.
## Doğru
D
## Açıklama
Kafka **4.0** ile mimari sadeleşti: **ZooKeeper tamamen kaldırıldı** ve cluster metadata'sı (topic/partition tanımları, lider atamaları, ISR bilgisi) **KRaft** (Kafka Raft) ile yönetilen bir **controller quorum**'unda, dahili bir Raft log olarak tutulur. Bu; ayrı bir ZooKeeper cluster'ı işletme yükünü kaldırır, metadata ölçeklenmesini ve failover süresini iyileştirir. Önceki sürümlerde ZooKeeper ve KRaft bir süre yan yana desteklendi (B'nin yanılttığı nokta), ama 4.0 yalnız KRaft'tır. KRaft'ı "test-özel" sanmak (C) da yanlıştır; üretim için GA'dır.

# kmimari-5 | Orta
## Soru
`bootstrap.servers` yapılandırması istemci için ne işe yarar?
## A
Cluster'daki tek bir broker'ın adresi olmalıdır; birden fazla adres yazılırsa istemci başlamaz
🤖 birden çok adres yazmak önerilir (yüksek erişilebilirlik); tek zorunluluk yoktur.
## B
Cluster'daki bütün broker'ları eksiksiz listelemek zorunludur; eksik kalırsa bazı partition'lara erişilemez
🤖 Gerekmez; istemci ilk bağlantıdan sonra tüm cluster haritasını metadata ile öğrenir.
## C
İlk bağlantı ve keşif (discovery) noktasıdır; istemci cluster metadata'sını ve partition liderlerini oradan öğrenir
🤖 Doğru: bootstrap sadece giriş kapısıdır; gerçek yönlendirme metadata'dan gelir.
## D
Yalnızca controller quorum'unun adreslerini içermelidir; veri broker'ları ayrı bir ayarla verilir
🤖 Bootstrap herhangi bir broker adresi olabilir; controller adresini elle vermek gerekmez.
## Doğru
C
## Açıklama
`bootstrap.servers` bir **keşif noktasıdır**: istemci listedeki herhangi bir broker'a bağlanıp cluster'ın **metadata**'sını (tüm broker adresleri ve her partition'ın **lider** broker'ı) indirir, sonra doğrudan ilgili liderlerle konuşur. Bu yüzden tüm broker'ları listelemek şart değildir (B); yüksek erişilebilirlik için birkaç adres yazılır ki biri kapalıysa diğerinden bootstrap yapılabilsin. Tek adres zorunluluğu (A) veya controller adresi verme gereği (D) yanlış varsayımlardır — istemci gerisini metadata ile halleder.

# kmimari-6 | İleri
## Soru
Kafka'nın yüksek iş hacmini (throughput) en çok hangi mekanizma açıklar?
## A
Her mesajı JVM heap'inde tutup diske hiç yazmaz; bu yüzden okuma çok hızlı olur
🤖 Kafka veriyi diske yazar; hız sırrı heap'te tutmak değil, OS page cache ve zero-copy'dir.
## B
OS page cache ve zero-copy (sendfile) kullanır; veriyi user-space'e kopyalamadan sokete geçirir
🤖 Doğru: sıralı disk erişimi + page cache + sendfile ile kopya ve context-switch en aza iner.
## C
Her mesajı ayrıca sıkıştırıp yalnızca RAM'de tutar; böylece diski tamamen devre dışı bırakır
🤖 Sıkıştırma opsiyoneldir ve veri yine diske yazılır; RAM-only bir model değildir.
## D
Her tüketici için verinin ayrı bir kopyasını önceden çıkarıp hazır bekletir; okuma böyle hızlanır
🤖 Aksine; tek log paylaşılır, tüketici başına kopya çıkarmak verimi düşürürdü.
## Doğru
B
## Açıklama
Kafka'nın yüksek throughput'u üç mimari seçime dayanır: **sıralı disk yazımı/okuması** (rastgele erişimden çok daha hızlı), **OS page cache**'e güvenmek (JVM heap yerine işletim sistemi önbelleği) ve **zero-copy** — `sendfile` sistem çağrısıyla disk/page-cache verisini **user-space'e kopyalamadan** doğrudan ağ soketine aktarmak. Bu, gereksiz bellek kopyalarını ve context-switch'leri ortadan kaldırır. Veriyi heap'te tutmak (A) veya RAM-only olmak (C) Kafka'nın modeli değildir; tek log tüm tüketicilerce paylaşılır, tüketici başına kopya çıkarılmaz (D).
