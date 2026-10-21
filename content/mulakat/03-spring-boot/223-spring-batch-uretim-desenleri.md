---
slug: spring-batch-uretim-desenleri
baslik: "Üretimde Spring Batch: Yeniden Başlatma, Hata Toleransı, Ölçekleme ve Gün Sonu Penceresi"
kademe: İleri
sira: 223
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Uzun yolda giden bir kamyon düşün. Şoför her dinlenme tesisinde takograf kâğıdına kaç kilometre yaptığını yazıyor. Yolun ortasında lastik patlarsa yolculuk sıfırlanmaz: kâğıda bakılır, kalınan yerden devam edilir. Ama bir şart var — daha önce teslim edilen paketler ikinci kez teslim edilmemeli. **Yeniden başlatılabilirlik** (restartability) ve **idempotentlik** işte bu ikilidir. İkincisi şu demektir: aynı iş ikinci kez çalışsa da sonuç değişmez.

Bankada gün sonu tahakkuk işi saat 23:00'te başlar ve 04:00'te bitmek zorundadır. Diyelim 02:40'ta veri tabanı bağlantısı koptu. Burada iki soru sorulur: "Nereden devam edeceğim?" ve "Devam ederken hiçbir müşteriye iki kez faiz işlemeyeceğimden emin miyim?" Spring Batch birincisini altyapı olarak verir; ikincisini **sen tasarlarsın**. Üçüncü soru da penceredir: iş dört saate sığmıyorsa nasıl hızlandırılır?

### Yeniden başlatılabilirlik nasıl mümkün olur?

Spring Batch her parçayı (chunk) commit ederken, **aynı işlemin içinde** StepExecution sayaçlarını ve reader'ın okuma konumunu `ExecutionContext`'e yazar. Bu bilgi `BATCH_STEP_EXECUTION_CONTEXT` tablosunda durur. İş düştüğünde son commit edilmiş parçaya kadar olan veri **ve** o parçaya kadar geldiğini söyleyen kayıt birlikte kalıcıdır.

Yeniden başlatma şudur: işi **aynı tanımlayıcı parametrelerle** yeniden başlatırsın. JobRepository aynı JobInstance'ı bulur, durumu `FAILED` olduğu için yeni bir **JobExecution** açar ve adım, ExecutionContext'teki konumdan devam eder. Önceden `COMPLETED` biten adımlar varsayılan olarak **atlanır**; bir adımın her yeniden başlatmada tekrar çalışmasını istiyorsan `allowStartIfComplete(true)` verirsin (ör. her seferinde sıfırdan üretilmesi gereken bir hazırlık adımı). Bu ayar **adım düzeyindedir**; `COMPLETED` bitmiş bir **işin** tümünü yeniden çalıştırmaz — onun için farklı bir parametre verip yeni bir JobInstance açman gerekir.

```svg
<svg viewBox="0 0 660 246" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yeniden başlatma: commit edilmiş parçalar kalır, ExecutionContext konumundan devam edilir">
  <defs>
    <marker id="sbatchp-rs-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">3.000.000 kayıt · 1.800.000'de düştü</text>
  <rect x="24" y="46" width="330" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="189" y="65" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">commit edilmiş parçalar (1.800.000)</text>
  <text x="189" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">veri kalıcı · geri alınmaz</text>
  <rect x="358" y="46" width="82" height="46" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="399" y="68" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">açık parça</text>
  <text x="399" y="84" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">rollback</text>
  <rect x="444" y="46" width="192" height="46" rx="9" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3" stroke-dasharray="5 4"/>
  <text x="540" y="74" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">henüz işlenmemiş (1.200.000)</text>
  <rect x="24" y="108" width="612" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="129" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">BATCH_STEP_EXECUTION_CONTEXT → okuma konumu: 1.800.000</text>
  <text x="330" y="149" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">parça verisiyle AYNI işlemde yazıldığı için tutarlıdır</text>
  <path d="M399 92 L399 106" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sbatchp-rs-arw)"/>
  <rect x="24" y="176" width="612" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="197" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Aynı parametrelerle yeniden başlat → aynı JobInstance, YENİ JobExecution</text>
  <text x="330" y="217" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1.800.001'den devam — ama yan etki idempotent değilse çift işlem riski sürer</text>
</svg>
```

**Yeniden başlatma idempotent olmak zorundadır.** Spring Batch'in verdiği güvence tek bir sınırla geçerlidir: **parça işleminin içinde kalan** veritabanı değişiklikleri. Yan etkin bu sınırın dışına taşıyorsa (başka bir servise HTTP çağrısı, mesaj kuyruğuna yayım, ayrı bir veritabanına yazım, dosyaya ekleme) o etki geri alınmaz ve yeniden başlatmada tekrarlanabilir. İkinci tehlike, **biriktirici** (accumulative) güncellemelerdir: "bakiyeye faizi ekle" gibi bir `UPDATE ... SET tutar = tutar + :faiz` ifadesi aynı kayıt üzerinde iki kez çalışırsa sonucu bozar; buna karşılık "hesaplanan değeri yaz" biçimindeki atayıcı güncelleme doğal olarak idempotenttir. Pratik korumalar şunlardır. İşlenmiş kayda **damga** koymak: `tahakkuk_donemi` sütunu ve `WHERE tahakkuk_donemi <> :donem` koşulu. Hedef tabloda **benzersiz anahtar** kullanmak, örneğin `(kredi_no, donem)`. Yan etkiyi mümkün olduğunca aynı işlem sınırına almak. Kuyruk yayımını ise **giden kutusu tablosu** (outbox) deseniyle veri tabanına yazıp ayrı bir süreçle göndermek.

### Hata toleransı: skip mi retry mi?

Varsayılan davranış serttir: tek bir bozuk kayıt tüm adımı düşürür. `faultTolerant()` ile iki farklı araç açılır ve **ikisi farklı sorunlar içindir**:

- **Retry (yeniden deneme)**: Hata **geçicidir** — kilit zaman aşımı, kilitlenme kurbanı olma (deadlock loser), ağ kesintisi, kısa süreli erişilemezlik. Aynı kayıt için işlem yeniden denenir: `.retryLimit(3).retry(DeadlockLoserDataAccessException.class)`. Doğru kullanıldığında kayıt kaybı olmaz.
- **Skip (atlama)**: Hata **kalıcıdır** — kaydın kendisi bozuktur: biçimi hatalı satır, ayrıştırılamayan tutar, iş kuralına aykırı veri. Yeniden denemenin faydası yoktur, kayıt **atlanır**: `.skipLimit(50).skip(FlatFileParseException.class)`. Atlanan kayıt sessizce kaybolmamalıdır: `SkipListener` (`onSkipInRead` / `onSkipInProcess` / `onSkipInWrite`) ile hata tablosuna ya da "reddedilenler" dosyasına yazılır ve iş sonunda `JobExecutionListener.afterJob` içinde **rapor edilir** (kaç kayıt atlandı, hangi sebeplerle). `skipLimit` aşılırsa adım yine de düşer — bu kasıtlıdır: yüzlerce kaydın atlanması veri kaynağında sistemik bir bozukluk demektir.
- **`noRollback`**: Belirli bir istisna için parça işleminin **geri alınmamasını** söyler; iş kuralı doğrulaması gibi veritabanına dokunmayan hatalarda gereksiz geri alma maliyetinden kaçınmak için kullanılır.

Önemli bir maliyet: writer'da atlanabilir bir istisna oluşursa Spring Batch, hangi kaydın bozuk olduğunu bilemediği için parçayı geri alır ve kayıtları **tek tek** yeniden işleyerek suçluyu bulur. Yani `skip` writer tarafında etkinleştirildiğinde bozuk kayıt içeren parçalar belirgin biçimde yavaşlar. Bu yüzden doğrulamayı mümkün olduğunca **processor** aşamasına çekmek, hatalı kaydı daha ucuza elemeyi sağlar.

```svg
<svg viewBox="0 0 660 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Skip kalıcı bozuk kaydı atlar, retry geçici hatayı yeniden dener">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki farklı araç, iki farklı hata türü</text>
  <rect x="22" y="44" width="302" height="132" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="173" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B07D20" font-family="system-ui">retry — GEÇİCİ hata</text>
  <text x="173" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kilit zaman aşımı · deadlock</text>
  <text x="173" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ağ kesintisi · geçici erişilemezlik</text>
  <text x="173" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">aynı kayıt yeniden denenir</text>
  <text x="173" y="152" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kayıt KAYBOLMAZ</text>
  <text x="173" y="168" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">retryLimit(3)</text>
  <rect x="336" y="44" width="302" height="132" rx="11" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="487" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B23B3B" font-family="system-ui">skip — KALICI bozuk kayıt</text>
  <text x="487" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ayrıştırılamayan satır</text>
  <text x="487" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">geçersiz tutar · kural ihlali</text>
  <text x="487" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kayıt atlanır, iş devam eder</text>
  <text x="487" y="152" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">SkipListener → hata tablosu</text>
  <text x="487" y="168" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">skipLimit(50) aşılırsa adım düşer</text>
  <text x="330" y="202" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Geçici hataya skip vermek veri kaybıdır; kalıcı hataya retry vermek zaman kaybıdır.</text>
  <text x="330" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Atlanan kayıt iş sonunda mutlaka raporlanır (afterJob özeti).</text>
</svg>
```

### Ölçekleme: doğru sırayla dene

Gün sonu penceresi daralınca refleks "iş parçacığı ekleyelim" olur; oysa doğru sıra ucuzdan pahalıya doğrudur:

1. **Önce tek kanallı işi hızlandır.** Okuma sorgusunun dizinleri (index), `fetchSize`, parça boyutu, gereksiz N+1 sorguları, writer'ın toplu (batch) yazması. Çoğu gün sonu işi bu adımda iki-üç kat hızlanır ve hiçbir eşzamanlılık riski doğmaz.
2. **Çok kanallı adım (multi-threaded step)**: Tek adımın parçaları bir `TaskExecutor` ile paralel işlenir. Ucuzdur ama **tehlikelidir**: standart reader'ların çoğu **durum tutar** ve iş parçacığı güvenli (thread-safe) değildir; birden çok kanal aynı reader'ı çağırınca kayıt atlanabilir veya tekrarlanabilir. Ayrıca durum kaydı anlamsızlaştığı için `saveState(false)` verilir ve **yeniden başlatılabilirlik kaybolur**. Milyonlarca kredi kaydını "kaldığı yerden devam" güvencesiyle işlemek isteyen bir tahakkuk işinde bu ağır bir bedeldir.
3. **Paralel akış (parallel flow)**: **Birbirinden bağımsız** adımlar/akışlar aynı anda çalışır (ör. "kart mutabakatı" ile "kredi tahakkuku" ayrı akışlar). Basit ve güvenlidir ama yalnız iş doğal olarak ayrıksa uygulanabilir; tek bir dev adımı hızlandırmaz.
4. **Bölümleme (partitioning)**: Asıl çözüm budur. Bir **ana (manager) adım**, veriyi **çakışmayan aralıklara** böler — ör. müşteri numarası aralıkları, şube kodu, hesap numarasının son hanesi. Her bölüm (partition) **kendi StepExecution'ı, kendi reader'ı ve kendi işlem sınırıyla** çalışır. Sonuçlar: paylaşılan durum yoktur, her bölümün ilerlemesi ayrı ayrı meta veriye yazılır, dolayısıyla **yeniden başlatma korunur** — üstelik yeniden başlatmada yalnız başarısız bölümler yeniden çalışır. Bölümler aynı JVM'de (yerel `TaskExecutorPartitionHandler`) veya ayrı düğümlerde çalışabilir.
5. **Uzak parçalama (remote chunking)**: Okuma tek düğümde kalır, okunan kayıtlar bir mesaj kuyruğu üzerinden uzak işçilere (worker) dağıtılır, işlenip yazılır. Yalnız **darboğaz işleme/yazma tarafındaysa ve okuma ucuzsa** anlamlıdır; ağ üzerinden kayıt taşıdığı için hem karmaşık hem maliyetlidir. Darboğaz okumadaysa hiçbir faydası yoktur — o durumda bölümleme doğru araçtır.

```svg
<svg viewBox="0 0 660 258" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ölçekleme merdiveni: ayar, çok kanallı adım, paralel akış, bölümleme, uzak parçalama">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ölçekleme merdiveni — ucuzdan pahalıya</text>
  <rect x="24" y="40" width="612" height="38" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="44" y="58" text-anchor="start" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">1 · Ayar</text>
  <text x="160" y="58" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">dizin · fetchSize · parça boyutu · toplu yazma</text>
  <text x="160" y="72" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">risk yok, yeniden başlatma bozulmaz</text>
  <rect x="24" y="84" width="612" height="38" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="44" y="102" text-anchor="start" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">2 · Çok kanallı adım</text>
  <text x="200" y="102" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">reader iş parçacığı güvenli değil</text>
  <text x="200" y="116" text-anchor="start" font-size="10" fill="#B23B3B" font-family="system-ui">saveState(false) → yeniden başlatma KAYBOLUR</text>
  <rect x="24" y="128" width="612" height="38" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="44" y="146" text-anchor="start" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Paralel akış</text>
  <text x="200" y="146" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">bağımsız adımlar aynı anda</text>
  <text x="200" y="160" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">yalnız iş doğal olarak ayrıksa işe yarar</text>
  <rect x="24" y="172" width="612" height="42" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="44" y="191" text-anchor="start" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">4 · Bölümleme</text>
  <text x="200" y="191" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">çakışmayan aralıklar · her bölüm ayrı StepExecution</text>
  <text x="200" y="206" text-anchor="start" font-size="10" fill="#186399" font-family="system-ui">ayrı işlem sınırı · yeniden başlatma KORUNUR ✓</text>
  <rect x="24" y="220" width="612" height="34" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="44" y="241" text-anchor="start" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">5 · Uzak parçalama</text>
  <text x="200" y="241" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">okuma tek düğümde · yalnız darboğaz işleme/yazmadaysa</text>
</svg>
```

### Okuma performansı ve klasik tuzaklar

- **`JdbcCursorItemReader`**: Tek bir sorgu açar, tek bir bağlantı üzerinde imleci (cursor) ilerleterek satırları akıtır. Tek geçişte tutarlı bir görünüm verir ve sayfa sorgusu maliyeti yoktur; buna karşılık bağlantıyı **adım boyunca meşgul tutar** ve iş parçacığı güvenli değildir. `fetchSize` ayarı performansı ciddi biçimde etkiler.
- **`JdbcPagingItemReader`**: Her sayfa için **ayrı bir sorgu** çalıştırır, sayfalar arasında bağlantıyı bırakır ve iş parçacığı güvenlidir (bölümleme/çok kanallı senaryolarda tercih edilir). Bedeli: **kararlı sıralama zorunludur**. `sortKeys` verilmezse ya da sıralama anahtarı benzersiz değilse veritabanı satır sırasını garanti etmez; iş sürerken tablo değişiyorsa (çevrimiçi kanal kayıt ekliyor/güncelliyor) **sayfa sınırları kayar**: bazı kayıtlar hiç okunmaz, bazıları iki kez okunur. Doğru desen: değişmeyen **benzersiz** bir anahtara göre sırala (ör. birincil anahtar) ve işi mümkünse sabit bir kümeye kilitle — dönem/durum sütunuyla süz, ya da işlenecek kayıtları önce bir çalışma tablosuna al. Derin sayfalamada `OFFSET` maliyeti de büyür; anahtar temelli sayfalama (`WHERE id > :sonId`) bunu ortadan kaldırır.
- **JPA tabanlı okuma**: JPA/Hibernate okunan her varlığı **birinci seviye önbellekte** (persistence context) tutar. Yüz binlerce kayıtta bu bağlam şişer ve bellek tükenir; ayrıca `flush` sırasında kirli kontrol (dirty checking) tüm bağlamı taradığı için iş giderek yavaşlar. Çözüm bağlamı düzenli **`clear()`** etmektir — Spring Batch'in JPA reader'ları bunu sayfa sınırında yapar, ama kendi yazdığın DAO/writer'da bunu **sen** yapmalısın. Salt okuma ağırlıklı işlerde JPA yerine düz JDBC daha öngörülebilirdir.
- **Parça boyutunun üç etkisi**: Birincisi **işlem süresi**. Büyük parça uzun işlem demektir; veri tabanının geri alma alanını ve günlüğünü zorlar. İkincisi **geri alma maliyeti**. Hata anında büyük parça daha çok işi çöpe atar; skip devredeyse tek tek yeniden işleme cezası da büyür. Üçüncüsü **kilit çekişmesi**. İşlem uzadıkça satır ve sayfa kilitleri daha uzun tutulur; çevrimiçi kanal hâlâ açıksa çekişme artar. Gün sonu penceresinde çevrimiçi trafik varsa parça boyutunu küçük tutmak bilinçli bir tercihtir.
- **Kilitlenme (deadlock) ve çekişme**: Bölümler aynı satırlara **farklı sıralarda** dokunuyorsa kilitlenme kaçınılmazdır. Üç önlem: bölümleri gerçekten **çakışmayan** aralıklara ayır; güncellemeleri her zaman **aynı sırayla** yap (ör. hep birincil anahtar artan); yine de olabileceğini kabul edip `DeadlockLoserDataAccessException` için **retry** tanımla. Ortak sayaç/özet tablosuna her bölümün yazması da klasik bir sıcak nokta (hot spot) üretir; özet, bölüm başına ayrı satıra yazılıp iş sonunda toplanmalıdır.

### Aynı iş iki kez başlamasın: zamanlama ve tek çalıştırma

Spring Batch'in kendi koruması iki katmanlıdır. Aynı JobInstance için hâlihazırda çalışan bir JobExecution varsa, ikinci başlatma `JobExecutionAlreadyRunningException` ile reddedilir. Tamamlanmış bir örnek ise `JobInstanceAlreadyCompleteException` verir. Ama bu korumanın iki şartı vardır: başlatma JobRepository'ye ulaşabilmeli ve parametreler aynı olmalı. İki kopya işi biraz farklı bir zaman damgasıyla başlatırsa iki ayrı JobInstance doğar; koruma hiç devreye girmez. Bu yüzden tetikleme katmanında da tek çalıştırma sağlanmalıdır.

Zamanlayıcı seçenekleri:

- **Spring `@Scheduled`**: En basiti; uygulama sürecinin içinde çalışır. **Tuzağı şudur**: küme farkındalığı yoktur, üç kopya (replica) ile çalışan bir serviste **her kopyada ayrı ayrı tetiklenir** — iş üç kez başlar. Ayrıca kalıcı değildir; uygulama o anda kapalıysa tetikleme kaybolur (kaçırılan tetiklemeyi telafi etmez). Çok kopyalı ortamda kullanılacaksa dağıtık kilitle (ör. veritabanı tabanlı bir kilit kütüphanesi) sarılmalıdır.
- **Quartz (kümelenmiş / clustered mod)**: Zamanlama bilgisini **veritabanında** tutar, küme içinde tetiklemeyi **yalnız bir düğüme** verir, kaçırılan tetikleme (misfire) politikası ve çalışma zamanında iş ekleme/değiştirme imkânı sunar. Karmaşık takvimleri ve dinamik zamanlamayı uygulama içinde yönetmek gerekiyorsa doğru araçtır; bedeli ek tablolar ve işletim yüküdür.
- **Kubernetes CronJob**: Toplu işi çevrimiçi servisin **dışında**, her çalıştırma için ayrı bir pod'da başlatır. Gün sonu işleri için genellikle en temiz seçenektir: kaynakları çevrimiçi servisten ayrılır, kendi CPU/bellek sınırı ve yeniden deneme politikası olur. Tek çalıştırma garantisi `concurrencyPolicy: Forbid` ile verilir — önceki çalıştırma hâlâ sürüyorsa yenisi başlatılmaz (`Allow` üst üste binmeye izin verir, `Replace` öncekini öldürür). `startingDeadlineSeconds` ile gecikmiş tetiklemenin ne kadar süre sonra hâlâ geçerli sayılacağı, `backoffLimit` ile yeniden deneme sayısı ayarlanır. Not: CronJob "en fazla bir kez" garantisi vermez; nadiren aynı tetikleme için iki pod doğabilir, bu yüzden işin kendisi yine idempotent olmalıdır.

### Gözlemlenebilirlik ve operasyon el kitabı

Gün sonu işinin bir **hizmet düzeyi hedefi (SLA)** vardır: "04:00'te bitmiş olmalı". Bunu ölçemiyorsan yönetemezsin. Spring Batch 5, Micrometer ile hazır ölçümler üretir: iş süresi (`spring.batch.job`), o an çalışan iş sayısı, adım süresi (`spring.batch.step`), okuma/işleme/yazma süreleri ve parça yazma süresi. Bunların üstüne kurulacak asgari uyarı seti şudur:

- **Süre uyarısı**: İş normalde 90 dakika sürüyorsa 120 dakikada uyar — pencere dolmadan **önce** haber ver, sonrasında değil.
- **Başlamama uyarısı**: 23:15'e kadar `STARTED` bir JobExecution yoksa uyar. Sessizce hiç başlamamak, düşmekten daha tehlikelidir çünkü kimse fark etmez.
- **Sonuç uyarısı**: `FAILED` biten iş ve **atlanan kayıt sayısı** eşiği (ör. 100'ün üstü) uyarı üretir.
- **İş kuralı doğrulaması**: "İşlenen kayıt sayısı ile kaynak tablodaki uygun kayıt sayısı tutuyor mu?" gibi bir mutabakat adımı, sessiz veri kaybını yakalayan en güçlü kontroldür.

Başarısız iş için **operasyon el kitabı** (runbook) önceden yazılır. Şu soruları cevaplar:

- Hata hangi adımda ve hangi kayıtta oldu? (`BATCH_STEP_EXECUTION` sayaçları ve `EXIT_MESSAGE`)
- Yeniden başlatma güvenli mi? (İş idempotent mi, yarım kalan yan etki var mı?)
- Yeniden başlatma komutu nedir, hangi parametrelerle verilir?
- Pencere kaçarsa iş sahibi kim ve hangi telafi planı devreye girer?

"Sabah gelince bakarız" bir plan değildir.

### Mülakatta ne sorulur / tuzaklar

- **"İş yarıda düştü, ne yaparsın?"** → Aynı tanımlayıcı parametrelerle yeniden başlatırım: aynı JobInstance, yeni JobExecution, ExecutionContext'teki konumdan devam. Ama önce **idempotentliği** doğrularım; değilse önce damga/benzersiz anahtar korumasını eklerim.
- **"skip ile retry farkı?"** → Retry **geçici** hata içindir ve kayıt kaybolmaz; skip **kalıcı bozuk kayıt** içindir ve kayıt atlanır. Atlananlar `SkipListener` ile kaydedilip iş sonunda raporlanmalıdır. "İkisi aynı şey" demek elenme sebebidir.
- **"İşi hızlandır."** → Önce ayar (dizin, fetchSize, parça boyutu, toplu yazma), sonra **bölümleme**. Çok kanallı adımı ilk çözüm diye söylemek zayıf cevaptır: reader'lar iş parçacığı güvenli değildir ve `saveState(false)` ile yeniden başlatılabilirliği kaybedersin.
- **"Sayfalı okuyucuda kayıt neden atlanıyor?"** → Kararlı ve benzersiz bir `ORDER BY` yoksa ya da veri iş sırasında değişiyorsa sayfa sınırları kayar. Çözüm: değişmeyen benzersiz anahtara göre sıralama ve mümkünse sabit bir kayıt kümesi seçmek.
- **"Üç kopyalı serviste `@Scheduled` ne yapar?"** → Üç kez tetikler. Çözüm: tetiklemeyi tek yere almak (Kubernetes CronJob `concurrencyPolicy: Forbid`, kümelenmiş Quartz) ya da dağıtık kilit. Ayrıca işin kendisi idempotent olmalıdır.
- **"Meta veriyi ayrı veritabanına koyalım mı?"** → Hayır; parça verisi ile ilerleme kaydı aynı işlemde commit edilemezse yeniden başlatma yanlış konumdan devam edip çift işlem üretebilir.
- **"Nasıl izlersin?"** → İş süresi metriği ve pencere SLA'si, başlamama uyarısı, `FAILED` ve atlanan kayıt eşiği, mutabakat kontrolü; üstüne yazılı bir operasyon el kitabı.

### Tek cümlede

Üretimde Spring Batch'in değeri hızından çok **kaldığı yerden güvenle devam edebilmesinden** gelir: ilerleme veriyle aynı işlemde yazılır, geçici hatalar yeniden denenir, bozuk kayıtlar kaydedilerek atlanır, ölçekleme bölümlemeyle yapılır. Bunların hepsi ancak iş **idempotent** ve tetikleme **tek** ise anlam taşır.

# sbatchp-1 | Orta
## Soru
3 milyon kredi kaydını işleyen tahakkuk adımı, 1,8 milyonuncu kayıtta veritabanı bağlantısı koptuğu için düştü. İşi aynı parametrelerle yeniden başlattığınızda ne olur?
## A
İş baştan başlar; Spring Batch nerede kalındığını bilmez, ilerleme bilgisi yalnızca bellekte tutulur
🤖 ilerleme bellekte değil, ExecutionContext olarak veritabanına yazılır.
## B
Aynı parametrelerle başlatmak yeni bir JobInstance yaratır ve bu yeni örnek veriyi baştan işler
🤖 aynı tanımlayıcı parametreler aynı JobInstance'a düşer; yeni örnek yaratılmaz.
## C
Aynı JobInstance için yeni bir JobExecution açılır ve adım, StepExecution'ın ExecutionContext'inde saklanan okuma konumundan devam eder
🤖 Doğru: commit edilmiş parçalar kalır, konum bilgisi onlarla aynı işlemde yazıldığı için tutarlıdır.
## D
Meta veri tabloları temizlenmeden yeniden başlatma yapılamaz; önce `BATCH_STEP_EXECUTION` kayıtları silinmelidir
🤖 yeniden başlatma tam da o kayıtlara dayanır; silmek kaldığı yeri yok eder.
## Doğru
C
## Açıklama
Spring Batch her parçayı commit ederken, **aynı işlemin içinde** StepExecution sayaçlarını ve reader'ın okuma konumunu `ExecutionContext`'e yazar; bu bilgi `BATCH_STEP_EXECUTION_CONTEXT` tablosunda kalıcıdır. İş düştüğünde son commit edilmiş parçaya kadar olan **veri** ve "buraya kadar geldim" **kaydı** birlikte kalır, açık parça geri alınır. İşi **aynı tanımlayıcı parametrelerle** başlatmak bir yeniden başlatmadır: JobRepository aynı **JobInstance**'ı bulur (yeni örnek yaratmaz — B yanlış), durumu `FAILED` olduğu için ona **yeni bir JobExecution** ekler ve adım kaydedilmiş konumdan devam eder. İlerleme bellekte değil veritabanında tutulduğu için baştan başlamaz (A yanlış); meta veri kayıtlarını silmek (D) ise kaldığı yeri yok edip işi sıfırdan çalıştırır ve denetim izini bozar. İki şart vardır: reader durumunu kaydeden türden olmalı (çok kanallı adımda `saveState(false)` verilmişse bu güvence yoktur) ve işin yan etkileri **idempotent** olmalıdır — Spring Batch'in güvencesi yalnız parça işleminin içinde kalan değişiklikleri kapsar.

# sbatchp-2 | Orta
## Soru
`faultTolerant()` yapılandırmasında **skip** ile **retry** arasındaki fark nedir?
## A
Retry geçici hatalarda (kilit zaman aşımı, deadlock, ağ) aynı kaydı yeniden dener ve kayıt kaybolmaz; skip kalıcı olarak bozuk kaydı atlar
🤖 Doğru: biri geçici arızayı tolere eder, diğeri bozuk veriyi ayıklar.
## B
İkisi aynı mekanizmadır; `retry` eski sürümlerdeki adı, `skip` ise Spring Batch 5 ile gelen yeni adıdır
🤖 ikisi ayrı ayrı yapılandırılan farklı mekanizmalardır, biri diğerinin yeni adı değildir.
## C
Skip yalnızca ItemWriter aşamasında, retry yalnızca ItemReader aşamasında çalışır; diğer aşamalarda etkisizdirler
🤖 her ikisi de okuma, işleme ve yazma aşamalarında yapılandırılabilir.
## D
Skip edilen kayıt sistemden tamamen silinir ve hangi kayıtların atlandığını öğrenmenin bir yolu yoktur
🤖 skip kaydı silmez, yalnız atlar; atlananlar SkipListener ile kaydedilip raporlanabilir.
## Doğru
A
## Açıklama
İki mekanizma **farklı hata türleri** içindir. **Retry**, hatanın **geçici** olduğu durumlar içindir: kilit zaman aşımı, kilitlenme kurbanı olma, ağ kesintisi, kısa süreli erişilemezlik. Aynı kayıt için işlem yeniden denenir (`.retryLimit(3).retry(...)`) ve başarılı olursa **hiçbir kayıt kaybolmaz**. **Skip**, hatanın **kalıcı** olduğu durumlar içindir: ayrıştırılamayan satır, geçersiz tutar, iş kuralına aykırı veri. Yeniden denemenin faydası yoktur; kayıt atlanır ve iş devam eder (`.skipLimit(50).skip(...)`). İkisi birbirinin yeni adı değildir (B) ve ikisi de okuma/işleme/yazma aşamalarında ayrı ayrı yapılandırılabilir (C yanlış). Atlanan kayıt **silinmez** ve izlenebilir (D yanlış): `SkipListener` (`onSkipInRead`/`onSkipInProcess`/`onSkipInWrite`) ile hata tablosuna veya reddedilenler dosyasına yazılır, iş sonunda `afterJob` içinde özetlenir. `skipLimit` aşılırsa adım yine de düşer — bu kasıtlıdır, çünkü yüzlerce atlanan kayıt tek tek bozukluk değil kaynakta sistemik bir sorun demektir. Bir maliyet notu: writer'da atlanabilir istisna çıkarsa Spring Batch parçayı geri alıp kayıtları tek tek yeniden işleyerek suçluyu bulur; bu yüzden doğrulamayı processor aşamasına çekmek daha ucuzdur.

# sbatchp-3 | İleri
## Soru
`JdbcPagingItemReader` ile taksit tablosunu okuyan gün sonu adımı, çevrimiçi kanal hâlâ kayıt eklerken çalışıyor. İş bitince bazı taksitlerin hiç işlenmediği, bazılarının iki kez işlendiği görülüyor. En olası kök neden nedir?
## A
Sayfalı okuyucu tek bir bağlantı üzerinde açık imleç tuttuğu için uzun süren işlerde bağlantı zaman aşımına uğrar ve satırlar bozulur
🤖 açık imleci cursor okuyucu tutar; sayfalı okuyucu her sayfa için ayrı sorgu çalıştırır.
## B
Sayfa boyutu `commit-interval` değerinden büyük seçildiği için her commit'te bir sayfanın artakalan kayıtları düşer
🤖 sayfa boyutu ile parça boyutu bağımsız ayarlardır; farklı olmaları kayıt düşürmez.
## C
Sayfalı okuyucu iş parçacığı güvenli olmadığından, tek kanallı çalışsa bile kayıtları karıştırır
🤖 sayfalı okuyucu iş parçacığı güvenlidir ve sorun tek kanallı çalışmada da ortaya çıkar.
## D
Sayfalama her sayfa için ayrı sorgu çalıştırır; kararlı ve benzersiz bir sıralama yoksa veya sıralama anahtarındaki veri iş sırasında değişiyorsa sayfa sınırları kayar
🤖 Doğru: kayan sayfa sınırı hem atlanan hem tekrarlanan kayıt üretir.
## Doğru
D
## Açıklama
`JdbcPagingItemReader`, `JdbcCursorItemReader`'dan temel olarak farklıdır: imleç okuyucu **tek bir sorgu** açıp tek bağlantı üzerinde satırları akıtırken (A'da tarif edilen davranış onundur), sayfalı okuyucu **her sayfa için ayrı bir sorgu** çalıştırır ve sayfalar arasında bağlantıyı bırakır. Bu yüzden sayfalı okuyucu iş parçacığı güvenlidir (C yanlış) ve bölümleme senaryolarında tercih edilir — ama karşılığında **kararlı sıralama zorunludur**. `sortKeys` benzersiz ve değişmeyen bir anahtar üzerinden tanımlanmazsa veritabanı satır sırasını garanti etmez; üstelik iş sürerken tabloya kayıt eklenip güncelleniyorsa sayfa sınırları **kayar**: bir kayıt önceki sayfaya geçip hiç okunmaz, bir başkası sonraki sayfaya kayıp iki kez okunur. Sayfa boyutu ile `commit-interval` bağımsız ayarlardır ve farklı olmaları kayıt kaybı üretmez (B yanlış). Doğru desen: değişmeyen **benzersiz** bir anahtara göre sırala (tercihen birincil anahtar), mümkünse işlenecek kümeyi sabitle (dönem/durum sütunuyla süz veya kayıtları önce bir çalışma tablosuna al) ve derin sayfalamadaki `OFFSET` maliyetinden kaçınmak için anahtar temelli sayfalama (`WHERE id > :sonId`) kullan.

# sbatchp-4 | İleri
## Soru
Gün sonu penceresi 4 saatten 90 dakikaya indirildi. Ekip önce adımı çok kanallı (multi-threaded) hale getirdi; sonuçlar tutarsız çıktı ve yeniden başlatma çalışmaz oldu. Milyonlarca kredi kaydını işleyen bu adım için doğru ölçekleme yaklaşımı nedir?
## A
`commit-interval` değerini 100.000'e çıkarmak; parça sayısı azaldıkça commit maliyeti düşeceği için iş pencereye sığar
🤖 çok büyük parça belleği şişirir, geri alma maliyetini ve kilit süresini büyütür; ölçekleme sorununu çözmez.
## B
Bölümleme (partitioning): veriyi çakışmayan aralıklara bölmek, her bölümün kendi StepExecution'ı ve kendi işlem sınırıyla çalışması
🤖 Doğru: paylaşılan durum kalmadığı için hem paralellik hem yeniden başlatılabilirlik korunur.
## C
Çok kanallı adımda iş parçacığı sayısını artırmak ve tutarsızlığı gidermek için parça boyutunu küçültmek
🤖 sorun kanal sayısı veya parça boyutu değil, reader'ın durum tutması ve iş parçacığı güvenli olmamasıdır.
## D
Doğrudan uzak parçalamaya (remote chunking) geçmek; dağıtık ölçeklemede her zaman ilk tercih edilmesi gereken desen budur
🤖 uzak parçalama okumayı tek düğümde bırakır ve ağ maliyeti getirir; yalnız darboğaz işleme/yazma tarafındaysa anlamlıdır.
## Doğru
B
## Açıklama
**Çok kanallı adımın** (multi-threaded step) iki bilinen sorunu vardır: standart ItemReader'ların çoğu **durum tutar** ve **iş parçacığı güvenli değildir** — birden çok kanal aynı reader'ı çağırınca kayıt atlanır veya tekrarlanır (gözlenen tutarsızlık); ayrıca durum kaydı anlamını yitirdiği için `saveState(false)` verilir ve **yeniden başlatılabilirlik kaybolur**. Milyonlarca kredi kaydını işleyen bir tahakkuk işinde bu kabul edilemez bir bedeldir. Doğru araç **bölümlemedir**: bir ana (manager) adım veriyi **çakışmayan** aralıklara böler (ör. müşteri numarası aralıkları, şube kodu), her bölüm **kendi StepExecution'ı, kendi reader'ı ve kendi işlem sınırıyla** çalışır. Paylaşılan durum olmadığı için tutarsızlık doğmaz; her bölümün ilerlemesi ayrı ayrı meta veriye yazıldığı için yeniden başlatma korunur ve yalnız başarısız bölümler yeniden çalışır. Parça boyutunu 100.000'e çıkarmak (A) belleği şişirir, geri alma maliyetini ve kilit süresini büyütür — üstelik tek kanallı işi ölçeklemez. Kanal sayısını artırmak (C) kök nedene dokunmaz. Uzak parçalama (D) okumayı tek düğümde bıraktığı ve kayıtları ağ üzerinden taşıdığı için ancak darboğaz **işleme/yazma** tarafındaysa ve okuma ucuzsa anlamlıdır; varsayılan ilk tercih değildir. Bölümlemede dikkat edilecek nokta, bölümlerin aynı satırlara farklı sıralarda dokunmamasıdır — aksi halde kilitlenme (deadlock) çıkar.

# sbatchp-5 | İleri
## Soru
Kredi servisi Kubernetes'te 3 kopya (replica) ile çalışıyor ve tahakkuk işini uygulama içindeki `@Scheduled` metodu her gece 00:05'te tetikliyor. Risk nedir ve doğru çözüm hangisidir?
## A
Zamanlayıcı her kopyada ayrı ayrı tetiklendiği için iş üç kez başlar; tetiklemeyi tek bir yere almak gerekir (ör. `concurrencyPolicy: Forbid` ile Kubernetes CronJob)
🤖 Doğru: `@Scheduled` küme farkında değildir; tek çalıştırma tetikleme katmanında sağlanmalıdır.
## B
Risk yoktur; Spring'in zamanlayıcısı küme farkındadır ve kopyalar arasında lider seçimi yaparak tetiklemeyi tek düğüme verir
🤖 Spring `@Scheduled` küme farkında değildir ve lider seçimi yapmaz.
## C
Tek çözüm kopya sayısını 1'e düşürmektir; çok kopyalı ortamda toplu iş çalıştırmanın başka güvenli yolu yoktur
🤖 bu erişilebilirliği feda eder ve güncelleme sırasında kopyaların üst üste bindiği anlarda yine iki tetikleme olabilir.
## D
`@Scheduled(cron = ...)` yerine `@Scheduled(fixedDelay = ...)` kullanmak; sabit gecikme aynı işin iki kez başlamasını engeller
🤖 `fixedDelay` yalnız aynı kopya içindeki ardışık çalıştırmaları ayırır; kopyalar arası çoklu tetiklemeyi engellemez.
## Doğru
A
## Açıklama
Spring'in `@Scheduled` mekanizması **süreç içidir ve küme farkında değildir**: üç kopya ile çalışan bir serviste zamanlayıcı **her kopyada ayrı ayrı** ateşlenir, yani iş üç kez başlar. Spring lider seçimi yapmaz (B yanlış) ve `fixedDelay`'e geçmek yalnız aynı kopya içindeki ardışık çalıştırmaları ayırır, kopyalar arası çoklu tetiklemeye dokunmaz (D yanlış). Kopya sayısını 1'e düşürmek (C) çevrimiçi servisin erişilebilirliğini feda eder ve yuvarlanan güncelleme (rolling update) sırasında eski ve yeni pod bir süre birlikte yaşadığı için tek tetikleme garantisi de vermez. Doğru yaklaşım tetiklemeyi **tek bir yere** almaktır: gün sonu işleri için en temizi ayrı bir **Kubernetes CronJob**'dur — iş çevrimiçi servisin dışında, kendi pod'unda, kendi kaynak sınırıyla çalışır ve `concurrencyPolicy: Forbid` önceki çalıştırma sürerken yenisinin başlamasını engeller. Alternatifler: **kümelenmiş Quartz** (zamanlamayı veritabanında tutar, tetiklemeyi yalnız bir düğüme verir, kaçırılan tetikleme politikası sunar) ya da `@Scheduled`'ı dağıtık bir kilitle sarmak. Spring Batch'in `JobRepository` koruması bir güvenlik ağıdır (aynı JobInstance için çalışan bir execution varsa `JobExecutionAlreadyRunningException` verir), ama kopyalar işi **farklı parametrelerle** (ör. zaman damgası) başlatırsa iki ayrı JobInstance doğar ve koruma devreye girmez; bu yüzden işin kendisi de idempotent olmalıdır.

# sbatchp-6 | Uzman
## Soru
Tahakkuk işi yarıda düştü, yeniden başlatıldı ve tamamlandı. Sonrasında bazı kredilerde faizin **iki kez** işlendiği görüldü. Kök neden ve doğru çözüm nedir?
## A
Spring Batch yeniden başlatmada son parçayı her zaman yeniden işler; bu kaçınılmaz bir davranıştır, tek çare işi baştan çalıştırmaktır
🤖 yeniden başlatma son COMMIT edilmiş parçadan devam eder; geri alınmış parçanın veritabanı etkisi zaten kalmamıştır.
## B
Adıma `allowStartIfComplete(true)` verilmediği için adım yeniden başlatmada iki kez çalıştı; bu ayarı eklemek sorunu çözer
🤖 bu ayar tersine, tamamlanmış adımın yeniden çalışmasını sağlar; çift işlemin sebebi o değildir.
## C
Yeniden başlatma parça sınırından devam eder ama işin yan etkisi idempotent değilse tekrarlanır; çözüm işlenmişlik damgası/benzersiz anahtarla korumak ve yan etkiyi aynı işlem sınırına almaktır
🤖 Doğru: Spring Batch'in güvencesi yalnız parça işleminin içindeki değişiklikleri kapsar; dışarı taşan etkiyi sen korumalısın.
## D
Meta veri tabloları uygulama verisiyle aynı veritabanında olduğu için ilerleme kaydı bozulmuştur; meta veriyi ayrı bir veritabanına taşımak gerekir
🤖 aynı veritabanında olmaları atomikliği sağlar; ayırmak çift işlem riskini artırır, azaltmaz.
## Doğru
C
## Açıklama
Spring Batch'in yeniden başlatma güvencesi net ama **sınırlıdır**: parça (chunk) işleminin **içinde** kalan veritabanı değişiklikleri ile ilerleme kaydı aynı işlemde commit edildiği için, iş son **commit edilmiş** parçadan devam eder — geri alınmış açık parçanın veritabanı etkisi zaten yoktur (A yanlış). Çift işlem, etki bu sınırın **dışına** taştığında doğar: başka bir servise yapılan HTTP çağrısı, mesaj kuyruğuna yayım, ayrı bir veritabanına yazım, dosyaya ekleme. İkinci ve bankacılıkta daha sinsi kaynak, **biriktirici** güncellemelerdir: `UPDATE ... SET bakiye = bakiye + :faiz` aynı kayıt için iki kez çalışırsa sonucu bozar; "hesaplanan değeri ata" biçimindeki güncelleme ise doğal olarak idempotenttir. Çözüm kümesi: işlenmiş kayda **damga** koymak (`tahakkuk_donemi` sütunu + `WHERE tahakkuk_donemi <> :donem`), hedef tabloda **benzersiz anahtar** tanımlamak (ör. `(kredi_no, donem)`) böylece ikinci yazım veritabanı düzeyinde reddedilsin, yan etkiyi mümkün olduğunca aynı işlem sınırına almak ve kuyruk yayımını **outbox** deseniyle önce veritabanına yazıp ayrı bir süreçle göndermek. `allowStartIfComplete(true)` (B) tam tersini yapar — tamamlanmış bir adımı yeniden başlatmada **tekrar çalıştırır**; çift işlemin çaresi değil, dikkatsiz kullanılırsa sebebidir. Meta veriyi ayrı veritabanına taşımak (D) durumu kötüleştirir: veri ile ilerleme kaydı artık tek işlemde commit edilemez, aralarında bir pencere doğar ve yeniden başlatma yanlış konumdan devam edebilir.
