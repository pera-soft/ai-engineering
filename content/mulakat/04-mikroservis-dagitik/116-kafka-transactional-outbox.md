---
slug: kafka-transactional-outbox
baslik: "Transactional Outbox: Dual-Write Problemi, CDC (Debezium) ve İdempotent Tüketim"
kademe: Uzman
sira: 116
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Bir sipariş servisi düşünün. Bu servis iki iş yapıyor: siparişi kendi veritabanına kaydediyor **ve** "sipariş oluşturuldu" haberini Kafka'ya duyuruyor. Haberi duyuruyor ki stok, fatura ve kargo servisleri harekete geçsin. Yani iki iş var ve bu işler iki ayrı yerde yapılıyor.

Peki tam ortada elektrik keserse ne olur? İki kötü sonuçtan biri çıkar: ya sipariş kaydedilir ama kimse haber almaz, ya da haber gider ama sipariş kaydedilmez. **Transactional outbox** — Türkçesiyle **işlem paketine dahil giden kutusu** — bu "yarım kalma" ihtimalini ortadan kaldıran desenin adıdır.

### Temel kavramlar

- **İşlem paketi (transaction):** bir veritabanında birlikte yapılan yazmalar demetidir. Ya **hepsi** kalıcı olur (buna **kalıcı yazma / commit** denir) ya da **hiçbiri** olmaz (buna **geri alma / rollback** denir). Bu "ya hep ya hiç" özelliğinin adı **atomikliktir**.
- **Olay (event) yayınlamak:** "şu oldu" bilgisini Kafka'ya yazmak.
- **Çift yazma (dual-write):** aynı iş içinde **iki ayrı sisteme** yazmak zorunda kalmaktır; burada bu iki sistem veritabanı ile Kafka'dır. Bu iki sistemin **ortak bir atomik kalıcı yazma sınırı yoktur**. Dağıtık işlem (2PC/XA) böyle bir sınır kurmaya çalışır, ama pratikte tercih edilmez.
- **Kısmi başarısızlık:** ikisinden birinin başarılı, diğerinin başarısız olması. Kalıcı tutarsızlığın kaynağı budur.
- **Giden kutusu (outbox):** aynı veritabanında duran, gönderilecek olayları tutan sıradan bir tablo.
- **Aktarıcı (relay):** giden kutusundaki satırları okuyup Kafka'ya yayınlayan ayrı süreç.
- **En az bir kez (at-least-once):** "kaybetmem, ama bazen aynı mesajı iki kez gönderebilirim" garantisi.
- **Tekrar edilse de sonucun değişmemesi (idempotentlik):** bir işi bir kez de yapsanız beş kez de yapsanız sonucun aynı kalması.

### Nasıl çalışır?

Önce **naif çözümlerin neden hepsinin bozuk olduğunu** görelim.

"Önce veritabanına kalıcı yaz, sonra Kafka'ya yayınla" dersen şu olur: kalıcı yazma bittikten sonra, yayınlama çalışmadan önce süreç çökebilir. JVM ölür, ağ kopar, kapsayıcı (container) öldürülür. O anda veritabanı günceldir ama **olay hiç gitmez**. Tüketiciler siparişten asla haberdar olmaz ve kalıcı tutarsızlık doğar.

"Önce Kafka'ya yayınla, sonra veritabanına yaz" dersen bu kez tersi olur: olay gider, ama kalıcı yazma başarısız olursa ortada karşılığı olmayan bir **hayalet olay (phantom event)** kalır.

Görüldüğü gibi sıralamayı değiştirmek sorunu çözmez, yalnız şeklini değiştirir. Hiçbir sıralama iki ayrı sistemi atomik yapamaz. Sorunun kaynağı da gecikme, Kafka kapasitesi, satır kilidi (deadlock) ya da kopyalama (replication) değildir. Sorun tektir: **farklı türden sistemler arasında ortak bir atomiklik sınırı yoktur.** `try/catch` ve yeniden deneme (retry) eklemek de yetmez; çünkü süreç tamamen ölürse yeniden deneyecek kimse kalmaz.

**Giden kutusunun fikri şudur:** atomiklik sınırını tek bir veritabanının içine çekmek.

İş verisini `siparis` tablosuna, olayı temsil eden satırı da `outbox` tablosuna yazarsınız. Kritik nokta şu: bu iki yazma **aynı yerel işlem paketi (transaction) içinde** yapılır. Dolayısıyla ikisi ya birlikte kalıcı olur ya birlikte geri alınır. Bunu bedava alırsınız, çünkü ortada aynı veritabanının tek bir işlemi vardır.

Kafka bu sınırın **tamamen dışında** kalır. Onu iki fazlı kalıcı yazmaya (2PC) sokmaya çalışmazsınız; tam tersine 2PC'den kaçınmış olursunuz. Olayın Kafka'ya gitmesi ikinci bir bileşenin işidir: ayrı çalışan bir **aktarıcı (relay)** süreci giden kutusunu okur, Kafka'ya yayınlar ve satırı işaretler ya da siler. "İş verisi kaydedildi ama olay kayboldu" hâli böylece imkânsızlaşır.

```svg
<svg viewBox="0 0 640 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dual-write problemi ile transactional outbox çözümünün karşılaştırması">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çift yazma (dual-write) problemi ve outbox çözümü</text>

  <!-- SOL PANEL: dual-write problemi -->
  <rect x="24" y="44" width="286" height="330" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="167" y="68" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">Naif: önce DB, sonra Kafka</text>

  <rect x="118" y="82" width="98" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="167" y="104" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Servis</text>

  <line x1="167" y1="116" x2="167" y2="140" stroke="#5D7789" stroke-width="1.4" marker-end="url(#koutbox-arrow)"/>
  <rect x="60" y="140" width="98" height="40" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="109" y="159" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">DB commit</text>
  <text x="109" y="173" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">OK ✓</text>

  <line x1="167" y1="180" x2="167" y2="212" stroke="#5D7789" stroke-width="1.4" marker-end="url(#koutbox-arrow)"/>
  <text x="240" y="200" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">...crash...</text>
  <rect x="176" y="212" width="110" height="40" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="231" y="231" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Kafka publish</text>
  <text x="231" y="245" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">HİÇ ÇALIŞMADI</text>

  <rect x="48" y="286" width="238" height="72" rx="9" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="167" y="308" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sonuç: OLAY KAYBI</text>
  <text x="167" y="327" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">DB güncel, ama olay hiç gitmedi</text>
  <text x="167" y="344" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ kalıcı tutarsızlık</text>

  <!-- SAĞ PANEL: outbox çözümü -->
  <rect x="330" y="44" width="286" height="330" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="473" y="68" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">Outbox: tek atomik commit</text>

  <rect x="424" y="82" width="98" height="34" rx="7" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="473" y="104" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Servis</text>

  <line x1="473" y1="116" x2="473" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#koutbox-arrow)"/>
  <rect x="352" y="134" width="242" height="70" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.6"/>
  <text x="473" y="152" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">TEK DB TRANSACTION (atomik)</text>
  <rect x="364" y="160" width="104" height="34" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1.1"/>
  <text x="416" y="181" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">siparis tablosu</text>
  <rect x="478" y="160" width="104" height="34" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.1"/>
  <text x="530" y="181" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">outbox tablosu</text>

  <line x1="473" y1="204" x2="473" y2="226" stroke="#5D7789" stroke-width="1.4" marker-end="url(#koutbox-arrow)"/>
  <rect x="388" y="226" width="170" height="38" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="473" y="245" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Relay / CDC (Debezium)</text>
  <text x="473" y="258" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">outbox → log-tailing → yayınla</text>

  <line x1="473" y1="264" x2="473" y2="286" stroke="#5D7789" stroke-width="1.4" marker-end="url(#koutbox-arrow)"/>
  <rect x="404" y="286" width="138" height="38" rx="7" fill="#186399"/>
  <text x="473" y="309" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kafka</text>
  <text x="473" y="344" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Olay kaybı imkansız: aynı commit'te dayanıklı</text>

  <defs>
    <marker id="koutbox-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

Tipik bir `outbox` tablosu şu alanları taşır: benzersiz **olay/mesaj kimliği**, **agrega kimliği** (olay hangi varlığa ait), **olay tipi** (hedef konu ve işleyici bunun üzerinden seçilir), **gövde (payload)** — genelde JSON — ve **zaman damgası**. İstenirse bir de gönderim durumu alanı eklenir.

Yayınlanan satırlar tabloda birikir. Bu yüzden bir **temizlik/saklama (retention)** süreci şarttır: satırlar ya silinir ya da arşiv tablosuna taşınır. "Denetim için sonsuza dek tutalım" demek tabloyu şişirir ve sorguları yavaşlatır.

**Aktarıcıyı iki şekilde kurabilirsiniz.**

*Yoklamalı yayıncı (polling publisher)*, giden kutusu tablosunu belirli aralıklarla `SELECT` ile tarar. Basittir ve ek altyapı istemez. Karşılığında iki bedeli vardır: olay, yoklama aralığı kadar gecikerek çıkar ve veritabanına sürekli tekrarlanan sorgu yükü biner.

*Değişiklik yakalama (CDC — Change Data Capture)* ise başka bir yol izler; bu alanda en yaygın araç **Debezium**'dur. CDC, veritabanının **işlem günlüğünü (transaction log)** izler; bu günlük PostgreSQL'de WAL, MySQL'de binlog adını alır ve veritabanının yaptığı her değişikliği sırayla tutar. Böylece gecikme düşer ve veritabanına ek sorgu yükü binmez. Bedeli operasyoneldir: bağlayıcı (connector), kopyalama yuvası (replication slot), okuma konumu (offset) ve şema evrimi yönetmeniz gerekir.

Küçük bir yanlış inanışa da şerh düşelim: Debezium ilişkisel veritabanlarıyla gayet iyi çalışır, "yalnız NoSQL ile kullanılır" gibi bir kısıt yoktur.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Outbox aktarıcısı: polling publisher SELECT ile tarar, CDC/Debezium transaction log'unu izler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aktarıcı: Polling vs CDC (Debezium)</text>

  <rect x="20" y="42" width="290" height="182" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Polling publisher</text>
  <text x="40" y="94" font-size="10.5" fill="#1C3247" font-family="system-ui">outbox tablosunu periyodik SELECT</text>
  <text x="40" y="120" font-size="10.5" fill="#25804C" font-family="system-ui">+ basit, ek altyapı yok</text>
  <text x="40" y="146" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− yoklama gecikmesi (latency)</text>
  <text x="40" y="172" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− DB'ye tekrarlı sorgu yükü</text>
  <text x="40" y="200" font-size="10" fill="#5D7789" font-family="system-ui">WHERE gonderildi = false</text>

  <rect x="330" y="42" width="290" height="182" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">CDC / Debezium</text>
  <text x="350" y="94" font-size="10.5" fill="#1C3247" font-family="system-ui">transaction log (WAL/binlog) tailing</text>
  <text x="350" y="120" font-size="10.5" fill="#25804C" font-family="system-ui">+ düşük gecikme</text>
  <text x="350" y="146" font-size="10.5" fill="#25804C" font-family="system-ui">+ DB'ye sorgu yükü bindirmez</text>
  <text x="350" y="172" font-size="10.5" fill="#8A2E2E" font-family="system-ui">− connector / log-slot yönetimi</text>
  <text x="350" y="200" font-size="10" fill="#5D7789" font-family="system-ui">ikisi de en-az-bir-kez teslim eder</text>
</svg>
```

**Son halka en çok atlanan halkadır:** her iki aktarıcı da **en az bir kez** teslim eder. Çökme veya yeniden deneme sonrasında aynı olay tekrar yayınlanabilir. Yani "tam olarak bir kez" (exactly-once) bir yanılsamadır.

Gerçek dayanıklılığı **idempotent tüketici** sağlar. Kurulumu şöyledir: her olaya benzersiz bir **mesaj kimliği** verilir. Tüketici, işlediği kimlikleri bir `islenmis_id` tablosunda tutar. Daha önce gördüğü bir kimlik yeniden gelirse o mesajı **eler**; buna tekilleştirme (deduplication) denir. Böylece olay iki kez gelse bile yan etki yalnız bir kez uygulanır.

Burada iki tuzağa dikkat edin. Birincisi, `enable.idempotence=true` ayarı **üretici ile Kafka sunucusu arasındaki** tekrarları engeller; tüketicideki iş mantığının tekrar çalışmasını kapsamaz. İkincisi, tüketicide paralelliği kapatmak ya da okuma konumunu erkenden otomatik işaretlemek (auto-commit) de tekrarı önlemez.

```svg
<svg viewBox="0 0 640 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="En-az-bir-kez teslimde aynı olay iki kez gelir; tüketici islenmis_id tablosuyla tekrarı eler, etki bir kez uygulanır">
  <defs>
    <marker id="koutbox-dedup-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İdempotent tüketici: en-az-bir-kez'i doğrular</text>

  <rect x="20" y="44" width="150" height="64" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="95" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Relay / Kafka</text>
  <text x="95" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">at-least-once</text>

  <text x="256" y="58" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">msgId=E7 (1. kez)</text>
  <line x1="170" y1="66" x2="340" y2="66" stroke="#5D7789" stroke-width="1.5" marker-end="url(#koutbox-dedup-ok)"/>
  <text x="256" y="104" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">msgId=E7 (tekrar)</text>
  <line x1="170" y1="90" x2="340" y2="90" stroke="#8A2E2E" stroke-width="1.5" stroke-dasharray="4 3" marker-end="url(#koutbox-dedup-ok)"/>

  <rect x="342" y="44" width="150" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="417" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Tüketici</text>
  <text x="417" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">msgId gördü mü?</text>

  <line x1="492" y1="76" x2="524" y2="76" stroke="#5D7789" stroke-width="1.5" marker-end="url(#koutbox-dedup-ok)"/>
  <rect x="526" y="44" width="94" height="64" rx="10" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.4"/>
  <text x="573" y="68" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">islenmis_id</text>
  <text x="573" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">E7 …</text>

  <rect x="60" y="150" width="240" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="180" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">1. kez: id yok → işle + kaydet</text>
  <text x="180" y="200" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yan etki uygulanır</text>

  <rect x="340" y="150" width="240" height="70" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="460" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">tekrar: id var → ATLA</text>
  <text x="460" y="200" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yan etki bir kez kalır</text>

  <text x="320" y="244" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Üretici atomikliği (outbox) + tüketici idempotentliği (dedup) = uçtan uca doğruluk.</text>
</svg>
```

### Mülakatta ne sorulur?

Çift yazma (dual-write) tam olarak nedir? Giden kutusu olay kaybını hangi mekanizmayla önler? Yoklama ile CDC/Debezium arasındaki denge nedir? En az bir kez teslimde tüketicide zorunlu olan nedir? Giden kutusu tablosu hangi alanları taşır ve nasıl temizlenir? Bir de klasik tuzak sorulur: "önce `save`, sonra `send`; sıra doğru olduğu için güvenli" iddiası neden yanlıştır?

### Tek cümlede

Giden kutusu deseni, olayı iş verisiyle **aynı veritabanı işlem paketi içinde** yazar ve olayı Kafka'ya taşıma işini ayrı bir aktarıcıya bırakır. Uçtan uca doğruluk ise iki parçanın birleşiminden gelir: **üretici tarafında atomiklik + tüketici tarafında idempotentlik.**

# koutbox-1 | İleri
## Soru
Bir sipariş servisi hem `siparis` tablosuna yazıp hem de Kafka'ya `SiparisOlusturuldu` olayı yayınlamak istiyor. "Çift yazma" (dual-write) problemi tam olarak nedir?
## A
Kafka'nın tek bir topic'e aynı anda birden fazla mesajı yazamaması ve kuyruk oluşturmasıdır
🤖 Kafka aynı topic'e sınırsız mesaj alır; sorun tek sistemin içinde değil, iki AYRI sistem arasındadır.
## B
Aynı veritabanı satırının iki farklı transaction tarafından güncellenmeye çalışılıp kilitlenmesidir (deadlock)
🤖 Bu bir eşzamanlılık/kilit sorunudur; çift yazma ise iki farklı depoya yazmanın atomik olamamasıdır.
## C
Veritabanının birincil ve yedek kopyalarına iki kez yazmanın zamanla senkron kalamamasıdır
🤖 Bu replikasyon konusudur; çift yazma DB ile mesaj aracısı gibi FARKLI sistemleri kapsar.
## D
DB ve Kafka ayrı sistemlerdir; ikisine tek atomik işlemde yazılamaz, tutarsızlık doğar
🤖 Doğru: iki heterojen sistem arasında ortak atomik commit yoktur; biri başarılı biri başarısız olabilir.
## Doğru
D
## Açıklama
**Çift yazma** (dual-write), bir işlemin **iki ayrı sisteme** (ör. ilişkisel DB ve Kafka) yazması gerektiğinde ortaya çıkar. Bu sistemlerin **ortak bir atomik commit** sınırı yoktur; dağıtık işlem (2PC) genelde uygulanmaz. Dolayısıyla biri başarılı olup diğeri başarısız olabilir (**kısmi başarısızlık**) ve kalıcı **tutarsızlık** doğar. Çeldiriciler sorunu tek sistem içi kilit (B), replikasyon (C) veya Kafka kapasitesi (A) sanır; oysa mesele **heterojen sistemler arası atomiklik yokluğudur**.

# koutbox-2 | İleri
## Soru
Transactional outbox kalıbı, olay kaybını hangi mekanizmayla önler?
## A
Kafka publish çağrısını try/catch ile sarıp hata olursa yeniden deneyerek (retry)
🤖 Retry çökmeyi çözmez; süreç publish'ten önce ölürse yeniden deneyecek kimse kalmaz.
## B
İş verisini ve olay kaydını aynı yerel transaction'da yazar; ayrı bir relay bunu Kafka'ya taşır
🤖 Doğru: iki yazma tek transaction'da atomiktir, Kafka bu sınırın dışına alınır; olay iş verisiyle birlikte dayanıklı olur.
## C
Kafka ile DB'yi tek bir XA/2PC dağıtık işlemine sokarak ortak atomiklik sağlar
🤖 Outbox'ın amacı tam da 2PC'den KAÇINMAKtır; yerel tek-DB transaction'ı kullanır.
## D
Olayı önce Kafka'ya yazıp yayın başarılıysa ardından DB'ye commit eder
🤖 Bu hâlâ çift yazma; DB commit başarısız olursa "hayalet olay" kalır, atomiklik yoktur.
## Doğru
B
## Açıklama
**Outbox** kalıbı, olay temsilini bir **`outbox` tablosuna**, iş verisiyle **aynı yerel transaction** içinde yazar. Bu iki yazma birlikte commit/rollback olur — **atomiktir**, çünkü aynı veritabanının tek işlemidir. Kafka bu sınırın **tamamen dışındadır**; ayrı bir **aktarıcı** (relay) sonradan outbox'ı okuyup yayınlar. Böylece "iş verisi kaydedildi ama olay kayboldu" durumu imkansızlaşır. Retry (A) çökmeyi çözmez, 2PC (C) kaçınılan şeydir, önce-Kafka (D) yine çift yazmadır.

# koutbox-3 | Uzman
## Soru
Outbox aktarıcısını (relay) **polling publisher** yerine **CDC (Debezium)** ile kurmanın temel avantajı ve maliyeti nedir?
## A
CDC olayları exactly-once teslim ettiği için tüketici tarafında idempotentliğe hiç gerek kalmaz
🤖 CDC de en-az-bir-kez çalışır; yeniden dengeleme/retry sonrası tekrar olabilir, idempotentlik hâlâ şart.
## B
CDC daha basittir çünkü hiç ek altyapı gerektirmez; polling ise connector kurulumu ister
🤖 Tam tersi: polling sade bir SELECT döngüsüdür; CDC connector/log-slot yönetimiyle daha karmaşıktır.
## C
CDC, DB'nin transaction log'unu (WAL/binlog) izleyip düşük gecikme sağlar; karşılığında operasyonel karmaşıklık getirir
🤖 Doğru: log-tailing düşük gecikme ve sorgusuz okuma verir, ama işletim karmaşıklığı artar.
## D
CDC yalnız NoSQL veritabanlarında çalışır; polling ise yalnız ilişkisel veritabanlarında kullanılabilir
🤖 Böyle bir kısıt yok; Debezium Postgres/MySQL gibi ilişkisel DB'lerin log'unu okur.
## Doğru
C
## Açıklama
**Polling publisher** `outbox` tablosunu periyodik `SELECT` ile tarar: basittir ama **yoklama gecikmesi** ve DB'ye tekrarlı sorgu yükü getirir. **CDC** (Change Data Capture / **Debezium**) veritabanının **transaction log'unu** (Postgres WAL, MySQL binlog) kuyruk-takibiyle izler: **düşük gecikme** ve DB'ye sorgu bindirmeme sağlar, ama **connector**, replication slot, ofset ve şema evrimi yönetimiyle **operasyonel karmaşıklık** doğurur. CDC de **en-az-bir-kez** çalıştığından idempotentlik ihtiyacını ortadan kaldırmaz (A yanlış).

# koutbox-4 | Uzman
## Soru
Outbox aktarıcısı **en-az-bir-kez** (at-least-once) teslim ettiği için tüketici aynı olayı birden çok kez alabilir. Uçtan uca doğruluğu sağlamak için tüketici tarafında ZORUNLU olan nedir?
## A
Her olaya benzersiz bir mesaj kimliği verip tüketicinin işlediği kimlikleri saklaması ve tekrarları elemesi
🤖 Doğru: benzersiz kimlik + işlenmiş-id kaydı ile tekrar gelen olay güvenle atlanır; etki bir kez uygulanır.
## B
Kafka'da `enable.idempotence=true` ayarını açmak; bu ayar tüketici tarafındaki tekrarları da otomatik önler
🤖 Bu ayar ÜRETİCİ tarafında broker'a çift yazımı önler; tüketicinin iş mantığı tekrarını kapsamaz.
## C
Tüketici grubunda tek bir consumer bırakıp paralelliği kapatmak, böylece aynı mesaj iki kez gelmez
🤖 Paralelliği kapatmak tekrarları (retry/rebalance kaynaklı) engellemez; sadece verimi düşürür.
## D
Olayları auto-commit ile hemen onaylayıp offset'i hızla ilerletmek, böylece tekrar hiç oluşmaz
🤖 Erken auto-commit tekrarı önlemez, aksine işlenmeden commit edilirse kayıp/tekrar riskini artırır.
## Doğru
A
## Açıklama
Aktarıcı **en-az-bir-kez** teslim ettiğinden — çökme/retry sonrası aynı olay yeniden yayınlanabilir — "exactly-once" bir **yanılsamadır**. Gerçek dayanıklılığı **idempotent tüketici** sağlar: her olaya **benzersiz mesaj kimliği** verilir, tüketici işlediği kimlikleri bir **`islenmis_id`** tablosunda tutar ve daha önce gördüğü kimliği **eler** (deduplication). Böylece olay iki kez gelse de yan etki bir kez uygulanır. `enable.idempotence` (B) üretici-broker tarafına aittir; tekli tüketici (C) ve auto-commit (D) sorunu çözmez.

# koutbox-5 | İleri
## Soru
Tipik bir `outbox` tablosu şeması hangi alanları içerir ve şişmeyi (tablo büyümesi) önlemek için ne gerekir?
## A
Yalnız `payload` alanı yeterlidir; başka meta veriye gerek yoktur ve satırlar kalıcı tutulur
🤖 Tek payload yetmez; hangi topic'e, hangi tipte, hangi kimlikle gideceği gibi meta veri şarttır ve satırlar birikir.
## B
Agrega kimliği, olay tipi, payload ve zaman damgası bulunur; yayınlanan satırlar temizlenir veya arşivlenir
🤖 Doğru: yönlendirme ve sıralama için meta veri gerekir, gönderilmiş satırlar temizlenmezse tablo sürekli şişer.
## C
Tabloda yalnız Kafka ofseti tutulur; olay içeriği Kafka'da olduğu için DB'de hiç saklanmaz
🤖 Ofset outbox'ın işi değil; olay içeriği tam da atomik yazılmak için outbox'ta durur.
## D
Şema önemsizdir; satırlar asla silinmez çünkü denetim (audit) için sonsuza dek gerekir
🤖 Sınırsız büyüme ciddi bir performans borcudur; denetim gerekiyorsa arşive taşınır, aktif tabloda tutulmaz.
## Doğru
B
## Açıklama
Sağlam bir `outbox` şeması en az şunları taşır: benzersiz **olay/mesaj kimliği** (idempotentlik için), **aggregate kimliği** (agrega/varlık id — sıralama ve yönlendirme), **olay tipi** (event type — hedef topic/işleyici seçimi), **payload** (genelde JSON) ve **zaman damgası**. En-az-bir-kez tekrar tespiti için işlenme/gönderilme durumu da tutulabilir. Yayınlanan satırlar birikir; bu yüzden bir **retention/temizlik** (silme veya arşive taşıma) süreci olmazsa tablo şişer, sorgular yavaşlar. Denetim gerekiyorsa satırlar **arşive** taşınır, aktif tabloda sonsuza dek tutulmaz (D yanlış).

# koutbox-6 | Uzman
## Soru
Bir geliştirici çift yazmayı şöyle "çözdüğünü" söylüyor: "İş kodunda önce `repository.save(siparis)` ile DB'ye commit ediyorum, hemen ardından `kafkaTemplate.send(olay)` ile yayınlıyorum. Sıra doğru, o yüzden güvenli." Bu yaklaşımın kritik kusuru nedir? **[TUZAK]**
## A
Hiçbir kusur yoktur; DB commit'ten sonra publish yapmak olay kaybını garantili biçimde önler
🤖 Tuzak bu: iki yazma atomik değildir; commit ile publish arasında çökme olursa olay kalıcı olarak kaybolur.
## B
Kusur performanstır; iki çağrı sıralı olduğu için gecikme (latency) artar, ama tutarlılık garantidir
🤖 Sorun gecikme değil; asıl kusur commit'ten sonra publish çalışmazsa olayın hiç gitmemesi ve tutarsızlık.
## C
Publish çağrısı DB commit'ten ÖNCE yapılmalı; sıralama yanlış, düzeltilince sorun tümüyle biter
🤖 Sıra ters çevrilince bu sefer "hayalet olay" riski doğar; hiçbir sıralama iki sistemi atomik yapmaz.
## D
İki yazma atomik değildir; commit'ten sonra publish'ten önce çökme olursa olay kalıcı olarak kaybolur
🤖 Doğru: iki yazma tek atomik sınırda değil; kayıp senaryosunu ancak outbox + ayrı relay kapatır.
## Doğru
D
## Açıklama
Bu klasik **tuzaktır**: "önce DB commit, sonra Kafka publish" sıralaması güvenli görünür ama iki yazma **atomik değildir**. `save` **commit** olduktan sonra, `send` çalışmadan önce süreç **çökerse** (JVM ölümü, ağ, container kill), DB **güncel** ama olay **hiç yayınlanmaz** — tüketiciler siparişten asla haberdar olmaz, **kalıcı tutarsızlık** doğar. Sırayı ters çevirmek (C) sorunu çözmez, sadece "hayalet olay" riskine dönüştürür; hiçbir sıralama iki heterojen sistemi atomik yapmaz. Tek doğru çözüm, olayı iş verisiyle **aynı DB transaction'ında** `outbox` tablosuna yazmak ve Kafka'ya taşımayı **ayrı bir relay/CDC** sürecine bırakmaktır.
