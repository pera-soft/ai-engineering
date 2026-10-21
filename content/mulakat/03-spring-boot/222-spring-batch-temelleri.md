---
slug: spring-batch-temelleri
baslik: "Spring Batch Temelleri: Job, Step, Chunk ve JobRepository"
kademe: Orta
sira: 222
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Postanede bir mektubu elden alıp adrese götürmek ile bir günün tüm mektuplarını taşımak aynı iş değildir. İkincisinde mektupları tek tek arabaya bindirmezsin: çuvala doldurursun, çuval dolunca **mühürlersin** ve yola çıkarsın. Mühürlenmiş çuval artık geride kalmıştır; yolda bir aksilik olursa yalnız **elindeki açık çuval** boşaltılır, mühürlenenler yerinde durur. Bankada gün sonu (end of day) işleri de böyledir: çevrimiçi kanal kapandıktan sonra milyonlarca krediye faiz **tahakkuk** ettirilir, taksitler vadesine göre işaretlenir, mutabakat dosyası üretilir. Bunu tek tek istek işleyen web katmanıyla yapmaya kalkarsan ne pencereye yetişir ne de yarıda kalınca kaldığın yeri bulabilirsin. **Spring Batch** tam olarak bu "çuval" mantığını — parçalara bölerek işleme, her parçayı ayrı mühürleme, nerede kaldığını yazılı tutma — hazır veren çatıdır.

**Toplu iş (batch) ne zaman doğru araçtır?** Üç işaret birden varsa:

1. İş **çevrimiçi işlemden ayrık** yapılabiliyorsa, yani kullanıcı ekran başında cevabı beklemiyorsa.
2. Kayıt sayısı **yüz binler ya da milyonlar** düzeyindeyse.
3. İşin bir **penceresi** varsa: "saat 23:00 ile 04:00 arasında bitecek" gibi.

Buna karşılık kullanıcının anında cevap beklediği bir kredi başvurusu değerlendirmesi toplu iş değildir; onu web katmanında bırak.

### Temel kavramlar

- **Job (iş)**: Baştan sona çalıştırılacak toplu işin **tanımı** — bir veya birden çok adımdan oluşan şablon. Kod içinde tek bir bean'dir, yani Spring'in yönettiği tek bir nesnedir; "gün sonu tahakkuk işi" gibi.
- **Step (adım)**: İşin bağımsız bir aşaması. Bir Job birden çok Step'i sırayla (ya da koşullu akışla) çalıştırır: "dosyayı al", "tahakkuk hesapla", "mutabakat çıktısını yaz".
- **JobInstance (iş örneği)**: **İş adı + tanımlayıcı (identifying) parametreler** ikilisinin belirlediği **mantıksal** çalıştırma. "gunSonu işinin 28 Temmuz 2026 çalıştırması" bir JobInstance'tır ve hayatı boyunca tektir.
- **JobExecution (iş çalıştırması)**: Bir JobInstance'ı çalıştırma **denemesi**. İlk deneme başarısız olup ikincisi başarılı olursa aynı JobInstance'a bağlı **iki** JobExecution vardır. Durumu (`STARTED`, `COMPLETED`, `FAILED`), başlangıç/bitiş zamanı ve hata bilgisi burada durur.
- **StepExecution (adım çalıştırması)**: Bir JobExecution içinde bir adımın çalışması. Okunan/işlenen/yazılan/atlanan kayıt sayaçlarını tutar.
- **ExecutionContext (çalışma bağlamı)**: Spring Batch'in adım ve iş düzeyinde tuttuğu, **veritabanına yazılan** küçük anahtar-değer torbası. Reader'ın "kaçıncı kayıttayım" bilgisi buraya yazılır; yeniden başlatmayı mümkün kılan da budur.
- **JobParameters (iş parametreleri)**: İşi başlatırken verilen anahtar-değer çifti (`tarih=2026-07-28` gibi). Tanımlayıcı parametreler JobInstance'ın **kimliğini** oluşturur; `identifying=false` işaretlenen parametreler kimliğe girmez (ör. bir günlük dosyası yolu).
- **JobRepository (iş defteri)**: Tüm bu meta veriyi **veritabanına** yazan bileşen. Spring Batch'in hafızasıdır; olmadan ne yeniden başlatma ne de "bu iş bugün çalıştı mı?" sorusu cevaplanabilir.
- **Parça (chunk)**: Tek bir işlemde (transaction) işlenip yazılan kayıt grubu. Büyüklüğünü `commit-interval` belirler.
- **ItemReader / ItemProcessor / ItemWriter**: Sırasıyla kaydı **okuyan**, **dönüştüren/eleyen** ve **toplu yazan** üçlü.
- **Tasklet**: Kayıt akışı olmayan, tek seferlik iş yapan adım tipi (dosya taşıma, tablo temizleme).

```svg
<svg viewBox="0 0 660 252" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Job, JobInstance, JobExecution, Step ve StepExecution ilişkisi">
  <defs>
    <marker id="sbatch-map-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tanım → mantıksal örnek → fiziksel deneme</text>
  <rect x="24" y="44" width="190" height="76" rx="11" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="119" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">Job</text>
  <text x="119" y="87" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iş TANIMI (şablon)</text>
  <text x="119" y="105" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ör. gunSonuTahakkuk</text>
  <rect x="235" y="44" width="190" height="76" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">JobInstance</text>
  <text x="330" y="87" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ad + tanımlayıcı parametre</text>
  <text x="330" y="105" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ör. tarih=2026-07-28</text>
  <rect x="446" y="44" width="190" height="76" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="541" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">JobExecution</text>
  <text x="541" y="87" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek bir DENEME</text>
  <text x="541" y="105" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1. FAILED · 2. COMPLETED</text>
  <path d="M214 82 L233 82" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sbatch-map-arw)"/>
  <path d="M425 82 L444 82" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sbatch-map-arw)"/>
  <rect x="90" y="148" width="220" height="68" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="200" y="170" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Step</text>
  <text x="200" y="190" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">adımın tanımı</text>
  <text x="200" y="207" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">chunk ya da tasklet</text>
  <rect x="350" y="148" width="240" height="68" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="470" y="170" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">StepExecution</text>
  <text x="470" y="190" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sayaçlar + ExecutionContext</text>
  <text x="470" y="207" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"kaçıncı kayıttayım"</text>
  <path d="M310 182 L348 182" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sbatch-map-arw)"/>
  <text x="330" y="240" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hepsi JobRepository tarafından BATCH_* tablolarına yazılır</text>
</svg>
```

### Nasıl çalışır?

1. **İşi parametreyle başlat.** `JobLauncher.run(job, jobParameters)` çağrılır. JobRepository önce "bu ad + bu tanımlayıcı parametrelerle bir JobInstance var mı?" diye bakar; yoksa yaratır, varsa ona bağlanır.
2. **Kimlik kuralını uygula.** Aynı JobInstance **başarıyla tamamlanmışsa** yeniden çalıştırılamaz: Spring Batch `JobInstanceAlreadyCompleteException` fırlatır. Bu bir arıza değil, **bilinçli bir koruma**dır — gün sonu tahakkuku iki kez çalışmasın diye. Bu yüzden gün sonu işlerine **tarih parametresi** vermek şarttır: her gece farklı bir `tarih` değeri farklı bir JobInstance üretir. Parametresiz başlatırsan ilk gece çalışır, ikinci gece aynı örneğe düşer ve patlar.
3. **Adımları sırayla çalıştır.** Job, tanımlı akışa göre Step'leri yürütür; her Step için bir StepExecution açılır ve JobRepository'ye yazılır.
4. **Parça temelli işle.** Parça (chunk) tipi bir adımda döngü şudur. ItemReader, parça boyutu kadar kaydı **tek tek** okur. ItemProcessor her kaydı **tek tek** dönüştürür; `null` döndürürse o kayıt elenir. Sonunda ItemWriter **listenin tamamını toplu** yazar. Ardından işlem commit edilir ve yeni parça başlar.
5. **Sayaçları ve konumu kaydet.** Commit'ten hemen önce Spring Batch, StepExecution sayaçlarını ve reader'ın okuma konumunu ExecutionContext'e yazar — **aynı işlemin içinde**. Yani veri ile "nerede kaldım" bilgisi birlikte kalıcı olur.
6. **Bitir ve durumu yaz.** Tüm adımlar bitince JobExecution `COMPLETED` (ya da `FAILED`) olarak damgalanır.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Parça temelli işleme: reader, processor, writer ve commit-interval işlem sınırı">
  <defs>
    <marker id="sbatch-chunk-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir parça (chunk) = bir işlem (transaction)</text>
  <rect x="20" y="42" width="620" height="132" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6" stroke-dasharray="6 4"/>
  <text x="330" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">commit-interval = 500 → 500 kayıtlık tek işlem sınırı</text>
  <rect x="44" y="74" width="164" height="70" rx="10" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="126" y="97" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">ItemReader</text>
  <text x="126" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">TEK TEK okur</text>
  <text x="126" y="133" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">500 kez çağrılır</text>
  <rect x="248" y="74" width="164" height="70" rx="10" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="330" y="97" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">ItemProcessor</text>
  <text x="330" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">TEK TEK dönüştürür</text>
  <text x="330" y="133" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">null dönerse kayıt elenir</text>
  <rect x="452" y="74" width="164" height="70" rx="10" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="534" y="97" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">ItemWriter</text>
  <text x="534" y="116" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">LİSTEYİ toplu yazar</text>
  <text x="534" y="133" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1 kez çağrılır</text>
  <path d="M210 109 L246 109" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#sbatch-chunk-arw)"/>
  <path d="M414 109 L450 109" fill="none" stroke="#186399" stroke-width="1.4" marker-end="url(#sbatch-chunk-arw)"/>
  <text x="330" y="164" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">yazma bitince → sayaçlar + okuma konumu yazılır → COMMIT → sonraki parça</text>
  <rect x="20" y="186" width="620" height="38" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="330" y="210" text-anchor="middle" font-size="10.5" fill="#B07D20" font-family="system-ui">Hata olursa YALNIZ açık parça geri alınır; önceki parçalar commit edilmiş kalır</text>
</svg>
```

**Parça boyutu bir ayardır, sihir değildir.** Çok küçük seçersen (ör. 1) her kayıt için ayrı commit maliyeti ödersin ve iş sürünür. Çok büyük seçersen (ör. 100.000) bellekte tuttuğun liste şişer, geri alma (rollback) maliyeti artar ve uzun süren işlem veritabanında kilit tutar. Uygulamada 100–1000 aralığı yaygın başlangıçtır; doğru değer ölçümle bulunur.

**Tasklet adımı** ise parça döngüsü olmayan adımlardır. `Tasklet.execute` metodu `RepeatStatus.FINISHED` dönene kadar çağrılır ve **her çağrı kendi işleminde** çalışır. Tipik kullanım: mutabakat dosyasını paylaşımlı dizinden alıp arşive taşımak, geçici tabloyu boşaltmak, bir saklı yordamı (stored procedure) tetiklemek. "Bir şey oku, bir şey yaz" akışı yoksa tasklet doğru seçimdir.

### JobRepository ve meta veri tabloları

JobRepository, meta veriyi altı ana tabloda tutar: `BATCH_JOB_INSTANCE` (mantıksal örnekler), `BATCH_JOB_EXECUTION` (denemeler ve durumları), `BATCH_JOB_EXECUTION_PARAMS` (parametreler), `BATCH_JOB_EXECUTION_CONTEXT` ve `BATCH_STEP_EXECUTION_CONTEXT` (serileştirilmiş bağlamlar), `BATCH_STEP_EXECUTION` (adım sayaçları). Şema Spring Boot ile otomatik kurulabilir (`spring.batch.jdbc.initialize-schema`), ama üretimde şemayı elle/göç aracıyla yönetmek yaygındır.

Bunun **işlemsel** bir sonucu vardır: meta veri şeması **uygulama verisiyle aynı veritabanında** ise, parçanın verisi ile "bu parçaya kadar geldim" kaydı **tek bir işlemde** commit edilir. Yani ikisi ya birlikte kalıcı olur ya birlikte geri alınır; yeniden başlatmada tutarlı bir konum bulursun. Meta veriyi ayrı bir veritabanına koyarsan bu atomiklik kaybolur: veri commit edilip meta veri yazılamadığı (veya tersi) bir pencere doğar ve yeniden başlatma yanlış yerden devam edip **çift işlem** üretebilir. Çift kayıt riski taşıyan tahakkuk/taksit işlerinde meta veriyi iş verisiyle aynı veritabanında tutmak varsayılan tercihtir.

```svg
<svg viewBox="0 0 660 244" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aynı parametreyle tamamlanmış iş yeniden çalışmaz, başarısız iş yeniden başlatılır">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">JobParameters: kimlik kuralı</text>
  <rect x="22" y="42" width="302" height="104" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="173" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">tarih=2026-07-28</text>
  <text x="173" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">YENİ JobInstance yaratılır</text>
  <text x="173" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çalışır → COMPLETED</text>
  <text x="173" y="128" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">gün sonu bitti ✓</text>
  <rect x="336" y="42" width="302" height="104" rx="11" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="487" y="64" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">ertesi gece yine tarih=2026-07-28</text>
  <text x="487" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">AYNI JobInstance bulunur</text>
  <text x="487" y="105" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">durumu COMPLETED</text>
  <text x="487" y="128" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">JobInstanceAlreadyCompleteException</text>
  <rect x="22" y="162" width="616" height="62" rx="11" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="330" y="184" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Peki iş FAILED bittiyse?</text>
  <text x="330" y="205" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Aynı parametrelerle başlatmak yeniden başlatmadır: aynı JobInstance, YENİ JobExecution</text>
  <text x="330" y="219" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bu yüzden her gece farklı bir tarih parametresi vermek şarttır</text>
</svg>
```

### Akış denetimi, dinleyiciler ve adımlar arası veri

**Akış (flow)**: Adımlar `next()` ile sırayla bağlanabildiği gibi, adımın **çıkış durumuna** göre dallanabilir: `.from(adim1).on("FAILED").to(hataBildirimAdimi)`, `.from(adim1).on("*").to(adim2)`. Karar bir adımın sonucundan değil de dış bir bilgiden geliyorsa (ör. "bugün ayın son günü mü?") `JobExecutionDecider` yazılır; decider bir durum dizesi döndürür, akış ona göre dallanır.

**Adımlar arası veri taşıma**: Bir adımda hesaplanan küçük bir değeri (ör. okunan kayıt sayısı, üretilen dosya adı) sonraki adımda kullanmak için `StepExecution`'ın ExecutionContext'ine yazılır ve `ExecutionContextPromotionListener` ile **iş düzeyine terfi** ettirilir; sonraki adım onu iş bağlamından okur. Kritik sınır: ExecutionContext veritabanına **serileştirilerek** yazılır ve sütun boyutu sınırlıdır (varsayılan şemada birkaç bin karakter). Oraya kayıt listesi, büyük nesne ya da tüm dosya içeriği koyulmaz — yalnız küçük skaler değerler. Statik alan veya singleton bean'de taşımak da yanlıştır: yeniden başlatmada kaybolur ve iş başka bir süreçte devam ederse hiç yoktur.

**Dinleyiciler (listener)** şunlardır. `JobExecutionListener`: `beforeJob` ve `afterJob` ile başlangıç/bitiş bildirimi ve özet rapor üretir. `StepExecutionListener`: adım sayaçlarını okuyup çıkış durumunu değiştirebilir. `ItemReadListener`, `ItemProcessListener` ve `ItemWriteListener`: kayıt düzeyindeki olayları yakalar. `ChunkListener`: parça sınırında çalışır. `SkipListener`: atlanan kaydı hata tablosuna yazmak için kullanılır. Dinleyici, iş mantığını kirletmeden günlükleme, ölçüm ve bildirim eklemenin standart yoludur.

### Spring Boot ile çalıştırma ve modern sürüm notu

Spring Boot, uygulama açılışında `JobLauncherApplicationRunner` aracılığıyla bağlamdaki Job bean'lerini **kendiliğinden çalıştırır**. Bu davranış web uygulamasının açılışında istenmez; kapatmak için `spring.batch.job.enabled=false` verilir ve iş, tetikleyici geldiğinde `JobLauncher` ile elle başlatılır. Komut satırından çalıştırıldığında parametreler seçenek olmayan argüman olarak geçilir: `java -jar gunsonu.jar tarih=2026-07-28`.

Sürüm notu (2026 gerçeği): **Spring Batch 5** (Spring Boot 3 ile gelen sürüm) Java 17+ ister ve `javax.*` yerine **`jakarta.*`** ad alanını kullanır. En sık takılınan iki değişiklik: (1) `@EnableBatchProcessing` artık Spring Boot 3'te **gerekmez** — otomatik yapılandırma altyapıyı zaten kurar; anotasyonu elle eklemek Boot'un batch otomatik yapılandırmasını **devre dışı bırakır** ve beklenmedik davranış üretir. (2) `JobBuilderFactory` / `StepBuilderFactory` kaldırılmıştır; bunun yerine `new JobBuilder("gunSonu", jobRepository)` ve `new StepBuilder("tahakkuk", jobRepository)` kullanılır, chunk adımına `PlatformTransactionManager` **açıkça** verilir.

### Mülakatta ne sorulur / tuzaklar

- **"Job, JobInstance ve JobExecution farkı?"** → Job tanım, JobInstance ad + tanımlayıcı parametrelerin belirlediği mantıksal çalıştırma, JobExecution ise o örneğin tek bir denemesi. Başarısız bir örneği yeniden başlatınca JobInstance aynı kalır, **yeni bir JobExecution** açılır. "Her çalıştırma yeni bir JobInstance üretir" demek elenme sebebidir.
- **"Gün sonu işi neden ikinci gece patladı?"** → Parametresiz başlattığın için ikinci gece **aynı JobInstance**'a düştün; o örnek `COMPLETED` olduğundan `JobInstanceAlreadyCompleteException` aldın. Çözüm meta veriyi silmek değil, **tarih parametresi** vermektir. Meta veri tablolarını temizlemek denetim izini yok eder ve aynı işin iki kez çalışmasının önünü açar.
- **"`commit-interval` ne demek?"** → Parça boyutu, yani **işlem sınırı**. 500 dersen 500 kayıt okunur, tek tek işlenir, **toplu** yazılır ve tek commit edilir. Toplam kayıt sınırı değildir, iş parçacığı sayısı hiç değildir.
- **"Reader/processor/writer kaç kez çağrılır?"** → Reader ve processor parça başına **N kez**, writer **1 kez** (listeyle). Writer'ın toplu yazması, `JDBC batch update` sayesinde asıl hız kazancını sağlayan yerdir.
- **"Tasklet mi chunk mı?"** → Kayıt akışı varsa chunk, tek seferlik yan iş varsa tasklet. "Dosyayı arşive taşı" işini `commit-interval=1` bir chunk adımıyla yapmaya çalışmak zorlama bir çözümdür.
- **"Meta veri nerede dursun?"** → İş verisiyle aynı veritabanında; böylece parça verisi ve ilerleme kaydı tek işlemde commit edilir. Ayrı veritabanı atomikliği bozar ve yeniden başlatmada çift işlem riski doğurur.
- **"Adımlar arasında veriyi nasıl taşırsın?"** → Küçük değerleri ExecutionContext + `ExecutionContextPromotionListener` ile. Statik alan/singleton bean cevabı yanlıştır; ExecutionContext'e büyük liste koymak da yanlıştır (sütun boyutu sınırlı).
- **"Spring Batch 5'te ne değişti?"** → `jakarta.*` ad alanı, Java 17+, `@EnableBatchProcessing`'in Boot 3'te gereksiz (ve zararlı) hale gelmesi, `JobBuilderFactory`/`StepBuilderFactory`'nin kaldırılıp `JobBuilder`/`StepBuilder`'a geçilmesi.

### Tek cümlede

Spring Batch, büyük veriyi parçalara böler ve her parçayı ayrı bir işlemde işleyerek yazar. Job/Step tanımı ile JobInstance/JobExecution çalıştırmasını birbirinden ayırır. Tüm ilerlemeyi JobRepository üzerinden veri tabanına yazdığı için işin nerede kaldığı her zaman bilinir.

# sbatch-1 | Temel
## Soru
Kredi taksitlerini işleyen bir chunk adımında `commit-interval` (parça boyutu) 500 olarak ayarlandı. Bu ayar tam olarak ne yapar?
## A
Adımın toplamda en fazla 500 kayıt işleyip duracağını, kalan kayıtların bir sonraki çalıştırmaya bırakılacağını belirtir
🤖 parça boyutu toplam kayıt sınırı değildir; adım tüm kayıtları 500'lük gruplar halinde işler.
## B
500 kayıt okunup tek tek işlenir, writer listeyi toplu yazar ve bu 500 kayıt tek bir işlemde (transaction) commit edilir
🤖 Doğru: parça boyutu doğrudan işlem sınırını belirler; hata olursa yalnız o parça geri alınır.
## C
Her kayıt kendi işleminde commit edilir; 500 değeri yalnızca kaç kayıtta bir günlüğe ilerleme satırı yazılacağını belirler
🤖 parça boyutu günlükleme sıklığı değil, commit sınırıdır; kayıt başına commit çok daha yavaş olurdu.
## D
500 kayıt aynı anda 500 ayrı iş parçacığında (thread) paralel işlenir, böylece adım 500 kat hızlanır
🤖 parça boyutunun eşzamanlılıkla ilgisi yoktur; paralellik ayrı bir ölçekleme kararıdır.
## Doğru
B
## Açıklama
**Parça temelli işleme** (chunk-oriented processing) döngüsü şudur: **ItemReader** parça boyutu kadar kaydı **tek tek** okur, **ItemProcessor** her kaydı **tek tek** dönüştürür, sonunda **ItemWriter** biriken **listenin tamamını toplu** yazar ve işlem commit edilir. `commit-interval` bu grubun büyüklüğüdür, yani doğrudan **işlem sınırıdır**: 500'lük bir parçada hata çıkarsa yalnız o parça geri alınır, daha önce commit edilmiş parçalar yerinde kalır. Bu ayar toplam kayıt sınırı değildir (A) — adım tüm veriyi 500'lük gruplarla baştan sona işler. Kayıt başına commit anlamına da gelmez (C); tam tersine, toplu yazma ve tek commit asıl hız kazancını sağlayan yerdir. Eşzamanlılıkla da ilgisi yoktur (D): paralel çalışma ayrı bir ölçekleme kararıdır. Değer seçimi bir dengedir — çok küçük parça commit maliyetini artırır, çok büyük parça belleği şişirir, geri alma maliyetini ve veritabanı kilit süresini büyütür.

# sbatch-2 | Orta
## Soru
Gün sonu tahakkuk işini her gece hiç parametre vermeden `JobLauncher` ile başlatıyorsunuz. İlk gece sorunsuz çalıştı, ikinci gece `JobInstanceAlreadyCompleteException` ile düştü. Sebep ve doğru çözüm nedir?
## A
JobRepository tabloları dolduğu için yeni kayıt yazılamıyor; her gece işten önce `BATCH_JOB_EXECUTION` tablosunu temizlemek gerekir
🤖 hatanın sebebi tablo doluluğu değil kimlik çakışmasıdır; meta veriyi silmek denetim izini yok eder ve çift çalışmanın önünü açar.
## B
Aynı adı taşıyan iki Job bean'i tanımlı olduğu için ad çakışması oluşuyor; işlerden birinin adını değiştirmek gerekir
🤖 istisna bean adı çakışmasını değil, tamamlanmış bir iş örneğinin yeniden başlatılmaya çalışıldığını bildirir.
## C
JobInstance kimliği "iş adı + tanımlayıcı parametreler"dir; parametre verilmeyince her gece aynı örneğe düşülür ve tamamlanmış bir örnek yeniden çalıştırılamaz — çözüm tarih gibi tanımlayıcı bir parametre vermektir
🤖 Doğru: her gece farklı bir tarih değeri farklı bir JobInstance üretir ve kimlik çakışması ortadan kalkar.
## D
Spring Boot açılışta işi kendiliğinden çalıştırdığı için iş iki kez tetikleniyor; `spring.batch.job.enabled=false` vermek bu istisnayı ortadan kaldırır
🤖 o ayar açılışta otomatik çalıştırmayı kapatır; parametresizlikten doğan kimlik çakışmasını çözmez.
## Doğru
C
## Açıklama
Spring Batch'te bir **JobInstance**'ın kimliği **iş adı + tanımlayıcı (identifying) JobParameters** ikilisidir. Hiç parametre vermezsen her çalıştırma aynı kimliğe düşer, yani ikinci gece **yeni bir örnek yaratılmaz**, ilk gecenin örneği bulunur. O örnek `COMPLETED` durumunda olduğu için Spring Batch onu yeniden çalıştırmayı reddeder ve `JobInstanceAlreadyCompleteException` fırlatır. Bu bir arıza değil, **bilinçli bir korumadır**: aynı gün sonu tahakkuku iki kez işlenmesin diye vardır. Doğru çözüm her gece kimliği değiştiren bir **tarih parametresi** vermektir (`tarih=2026-07-28`). Meta veri tablolarını temizlemek (A) hem denetim izini yok eder hem de korumayı devre dışı bırakıp çift tahakkuk riskini geri getirir. İstisna bean adı çakışmasıyla ilgili değildir (B). `spring.batch.job.enabled=false` (D) yalnız Spring Boot'un açılışta işi kendiliğinden çalıştırmasını kapatır; kimlik kuralına dokunmaz. Not: iş `FAILED` bitmişse aynı parametrelerle başlatmak hata değil **yeniden başlatmadır** — aynı JobInstance'a yeni bir JobExecution eklenir.

# sbatch-3 | Orta
## Soru
Job, JobInstance ve JobExecution kavramlarını hangi seçenek doğru tanımlar?
## A
Job işin tanımıdır; JobInstance ad ve tanımlayıcı parametrelerin belirlediği mantıksal çalıştırmadır; JobExecution ise o örneği çalıştırma denemesidir
🤖 Doğru: tanım, mantıksal örnek ve fiziksel deneme üç ayrı katmandır.
## B
Her çalıştırma her koşulda yeni bir JobInstance üretir; JobExecution ise bu örneklerin veritabanındaki arşiv kopyasıdır
🤖 aynı tanımlayıcı parametrelerle yeniden başlatma yeni örnek üretmez; JobExecution arşiv kopyası değil çalıştırma denemesidir.
## C
JobInstance ile JobExecution aynı kavramın iki adıdır; biri kod tarafında, diğeri veritabanı tarafında kullanılır
🤖 bir JobInstance'a birden çok JobExecution bağlanabilir; ikisi farklı kavramlardır.
## D
Job her çalıştırmada yeniden yaratılan geçici bir nesnedir; JobInstance ise adımların sayaçlarını tutan kayıttır
🤖 Job kalıcı bir tanımdır ve sayaçları tutan kayıt StepExecution'dır.
## Doğru
A
## Açıklama
Üç kavram bir katmanlama oluşturur. **Job** işin **tanımıdır** — kodda tanımlı, her gece aynı kalan şablon. **JobInstance** bu tanımın **belirli bir mantıksal çalıştırmasıdır** ve kimliği "iş adı + tanımlayıcı parametreler"dir; "gunSonu işinin 28 Temmuz 2026 çalıştırması" bir örnektir ve hayatı boyunca tektir. **JobExecution** ise o örneği çalıştırma **denemesidir**: ilk deneme `FAILED` biter, aynı parametrelerle yeniden başlatırsan **aynı JobInstance**'a **ikinci bir JobExecution** eklenir — bu yüzden bir örneğe birden çok çalıştırma bağlanabilir (C yanlış). "Her çalıştırma yeni örnek üretir" (B) yaygın ve elenmeye yol açan bir yanlıştır; yeniden başlatmanın nasıl çalıştığını anlamayı engeller. Job geçici bir nesne değildir ve sayaçları (okunan/işlenen/yazılan/atlanan) tutan kayıt **StepExecution**'dır (D yanlış). Bütün bu meta veri **JobRepository** tarafından `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION` gibi tablolara yazılır.

# sbatch-4 | İleri
## Soru
Gün sonu işinin ilk adımı şu: paylaşımlı dizindeki dünkü mutabakat dosyasını arşiv klasörüne taşımak ve geçici çalışma tablosunu boşaltmak. Bu adımı nasıl kurgularsın?
## A
`commit-interval` değeri 1 olan bir chunk adımı; böylece tek seferlik iş de parça döngüsünün içinde çalışmış olur
🤖 ortada okunacak kayıt akışı yok; parça döngüsünü zorlamak gereksiz bir sarmalayıcı üretir.
## B
İlk çağrıda `null` döndüren bir ItemReader ile boş bir chunk adımı; asıl işi ItemWriter'ın içine yazmak
🤖 reader hemen null dönerse writer hiç çağrılmaz; bu kurgu işi çalıştırmaz bile.
## C
Ayrı bir Job olarak tanımlayıp gün sonu işinden önce elle çalıştırmak; tek seferlik işler Step olamaz
🤖 tek seferlik işler pekâlâ Step olur; ayrı işe bölmek akış denetimini ve tek meta veri izini gereksizce parçalar.
## D
Tasklet adımı; `Tasklet.execute` metodu `RepeatStatus.FINISHED` dönene kadar çağrılır ve her çağrı kendi işleminde çalışır
🤖 Doğru: kayıt akışı olmayan tek seferlik işler için tasklet tam olarak bu amaçla vardır.
## Doğru
D
## Açıklama
Spring Batch iki adım tipi sunar. **Chunk** tipi adım, bir **kayıt akışı** üzerinde çalışır: ItemReader okur, ItemProcessor dönüştürür, ItemWriter toplu yazar ve `commit-interval` kadar kayıtta bir commit edilir. **Tasklet** tipi adım ise akışsız, **tek seferlik** iş içindir: dosya taşıma/silme, tablo boşaltma (`TRUNCATE`), saklı yordam çağırma, dış sisteme bildirim. `Tasklet.execute` metodu `RepeatStatus.FINISHED` dönene kadar çağrılır ve **her çağrı kendi işleminde** çalışır. Sorudaki iş tam olarak bu tanıma uyar: okunacak kayıt yoktur, yapılacak iki yan etki vardır. `commit-interval=1` bir chunk adımı (A) çalışır görünse de amaca uymayan zorlama bir kurgudur; hemen `null` dönen bir reader (B) ise writer'ın hiç çağrılmamasına yol açtığı için işi hiç yapmaz — reader `null` döndüğünde adım biter. Bunu ayrı bir Job'a çıkarmak (C) de gereksizdir: tek bir Job içinde `.start(dosyaTasimaAdimi).next(tahakkukAdimi)` yazarak hem akış denetimini hem tek bir meta veri izini korursun; ayrıca ilk adım düşerse ikinci adım hiç başlamaz.

# sbatch-5 | İleri
## Soru
İlk adımda okuduğunuz mutabakat dosyasındaki kayıt sayısını, ikinci adımda üretilecek özet raporda kullanmak istiyorsunuz. Spring Batch'te doğru yöntem hangisidir?
## A
Değeri bir Spring singleton bean'inin alanında (ya da statik bir alanda) tutup ikinci adımda oradan okumak
🤖 bellekteki değer meta veriye yazılmadığı için yeniden başlatmada kaybolur ve iş başka bir süreçte devam ederse hiç bulunmaz.
## B
Değeri StepExecution'ın ExecutionContext'ine yazıp `ExecutionContextPromotionListener` ile iş düzeyine terfi ettirmek, ikinci adımda iş bağlamından okumak
🤖 Doğru: küçük skaler değerler için Spring Batch'in öngördüğü ve yeniden başlatmaya dayanıklı yol budur.
## C
Dosyadan okunan tüm kayıtları bir liste olarak iş düzeyi ExecutionContext'e koymak; ikinci adım hem sayıyı hem kayıtları oradan alsın
🤖 ExecutionContext veritabanına serileştirilir ve sütun boyutu sınırlıdır; oraya büyük liste koymak işi çalışma anında bozar.
## D
İş çalışırken JobParameters'a yeni bir parametre eklemek; parametreler tüm adımlardan görülebildiği için en pratik yol budur
🤖 JobParameters başlatma anında sabitlenir ve tanımlayıcı parametreler örnek kimliğini oluşturur; çalışırken değiştirilmez.
## Doğru
B
## Açıklama
Adımlar arasında küçük değer taşımanın standart yolu **ExecutionContext**'tir. Değer önce adımın kendi (`StepExecution`) bağlamına yazılır; `ExecutionContextPromotionListener` belirtilen anahtarları adım bitiminde **iş (JobExecution) düzeyine terfi** ettirir ve sonraki adım onu iş bağlamından okur. Bu yöntem yeniden başlatmaya dayanıklıdır, çünkü ExecutionContext veritabanına (`BATCH_STEP_EXECUTION_CONTEXT` / `BATCH_JOB_EXECUTION_CONTEXT`) yazılır. Singleton bean veya statik alan (A) bellekte kalır: iş yarıda düşüp yeniden başlatılırsa değer yoktur, iş başka bir süreçte/pod'da devam ederse hiç var olmamıştır. ExecutionContext'e tüm kayıt listesini koymak (C) ayrı bir tuzaktır — bağlam **serileştirilerek** bir sütuna yazılır ve bu sütunun boyutu sınırlıdır (varsayılan şemada birkaç bin karakter); oraya yalnız küçük skaler değerler konur. JobParameters'a çalışma sırasında ekleme yapmak (D) mümkün değildir: parametreler işin başlatılma anında sabitlenir ve tanımlayıcı olanları **JobInstance kimliğini** oluşturduğu için sonradan değiştirilmeleri kimliği bozardı.

# sbatch-6 | İleri
## Soru
Ekip, gün sonu toplu işlerini Spring Boot 3 ve Spring Batch 5'e taşıyor. Yapılandırma konusunda hangisi doğrudur?
## A
`@EnableBatchProcessing` anotasyonu Spring Boot 3'te hâlâ zorunludur; kaldırılırsa JobRepository hiç oluşturulmaz ve uygulama açılmaz
🤖 Boot 3'te altyapıyı otomatik yapılandırma kurar; anotasyon zorunlu değildir.
## B
`JobBuilderFactory` ve `StepBuilderFactory` hâlâ önerilen kurulum yoludur; Spring Batch 5 bunları tek değişiklik olarak `jakarta` paketine taşımıştır
🤖 bu iki fabrika sınıfı Spring Batch 5 hattında kaldırılmıştır; yerlerine JobBuilder/StepBuilder gelmiştir.
## C
Spring Batch 5 `javax.*` ad alanını korur; yalnızca Spring Boot tarafı `jakarta.*` kullandığı için iki ad alanı yan yana çalışır
🤖 Spring Batch 5 tümüyle `jakarta.*` ad alanına geçmiştir; `javax.*` ile karışık kullanım söz konusu değildir.
## D
Boot 3'te otomatik yapılandırma altyapıyı kurar ve `@EnableBatchProcessing` eklemek bu otomatik yapılandırmayı devre dışı bırakır; iş/adım kurulumu `JobBuilder` ve `StepBuilder` ile yapılır
🤖 Doğru: Spring Batch 5 ile kurulum biçimi budur ve anotasyonu elle eklemek Boot'un otomatik yapılandırmasını kapatır.
## Doğru
D
## Açıklama
**Spring Batch 5** (Spring Boot 3 ile gelen hat) üç önemli değişiklik getirdi. (1) **Ad alanı**: `javax.*` tamamen bırakılıp **`jakarta.*`**'a geçildi ve Java 17+ zorunlu oldu — iki ad alanının yan yana çalışması söz konusu değildir (C yanlış). (2) **`@EnableBatchProcessing` artık gerekmez**: Spring Boot 3'ün otomatik yapılandırması `JobRepository`, `JobLauncher` ve gerekli altyapıyı zaten kurar. Dahası, anotasyonu elle eklemek Boot'un batch otomatik yapılandırmasını **devre dışı bırakır** ve `spring.batch.*` özelliklerinin beklenmedik biçimde etkisiz kalması gibi sürprizler üretir — yani "zorunludur" iddiası (A) tersine dönmüştür. (3) **Kurucu (builder) API'si**: `JobBuilderFactory` ve `StepBuilderFactory` kaldırıldı (B yanlış); yerine `new JobBuilder("gunSonu", jobRepository)` ve `new StepBuilder("tahakkuk", jobRepository)` kullanılır. Chunk adımında artık işlem yöneticisi de **açıkça** verilir: `.chunk(500, transactionManager)`. Ayrıca Spring Boot'un açılışta işleri kendiliğinden çalıştırmasını kapatmak için `spring.batch.job.enabled=false` kullanılır ve iş, tetikleyici geldiğinde `JobLauncher` ile başlatılır.
