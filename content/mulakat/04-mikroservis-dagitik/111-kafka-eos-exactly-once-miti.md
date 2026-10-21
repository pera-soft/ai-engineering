---
slug: kafka-eos-exactly-once-miti
baslik: "Exactly-Once Miti: Kafka Transactions, read-process-write ve EOS'un Sınırları"
kademe: Uzman
sira: 111
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Bir arkadaşına para gönderiyorsun ve uygulama "bağlantı hatası" diyor. Para gitti mi, gitmedi mi? Tekrar denersen **iki kez** mi gider? Dağıtık sistemlerin en eski derdi budur. Mesaj karşıya ulaşmış ama onay dönerken hat kopmuşsa, gönderen "bilmiyorum" durumunda kalır. Elinde iki seçenek vardır: **tekrar dene** (yinelenme riski) ya da **vazgeç** (kayıp riski).

Buradan üç "teslim sözü" doğar: en fazla bir kez, en az bir kez, tam bir kez. "Tam bir kez" (exactly-once) kulağa en iyisi gibi gelir. Bu bölümün konusu tam olarak şudur: bu söz **nerede gerçek, nerede mit**?

### Temel kavramlar

- **at-most-once (en fazla bir kez):** 0 ya da 1 kez teslim; **kayıp** mümkün, yinelenme yok. Tüketicide "commit et → sonra işle" sırası bunu üretir.
- **at-least-once (en az bir kez):** en az 1 kez teslim; **yinelenme** mümkün, kayıp yok. "İşle → sonra commit et" sırası bunu üretir ve pratikte varsayılan hedeftir.
- **exactly-once (tam bir kez):** ne kayıp ne yinelenme. Bedava değildir, mekanizma ister.
- **Tekrar edilse de sonucu değişmeyen işlem (idempotent işlem):** aynı mesaj iki kez işlense de sonucu değişmeyen işlem. "Bakiyeyi 100 yap" böyledir; "bakiyeye 100 ekle" değildir, çünkü ikinci çalıştırmada bakiye 200 olur.
- **Kafka işlemi (transaction):** birden çok **bölüme (partition)** ve konuya (topic) yazmayı, ayrıca okunan **okuma konumunun (offset)** kaydını **tek bir bölünmez (atomik) birim** hâline getiren mekanizma. Ya hepsi görünür olur ya hiçbiri. `transactional.id` ayarıyla açılır. Akış şu çağrılarla yürür: `initTransactions()`, `beginTransaction()`, `sendOffsetsToTransaction()`, `commitTransaction()`.
- **`isolation.level` (yalıtım seviyesi):** tüketicinin, bir işlem içinde yazılmış mesajlardan hangisini göreceğini belirler. Varsayılan **`read_uncommitted`** hepsini gösterir. **`read_committed`** ise yalnız işlemi başarıyla tamamlanmış (commit) mesajları gösterir; iptal edilenleri (abort) ve henüz sonuçlanmamışları atlar.
- **EOS (Exactly-Once Semantics — tam bir kez teslim semantiği):** idempotent üretici + işlem (transaction) + `read_committed` bileşimi.
- **Yan etki (side effect):** Kafka dışında kalıcı iz bırakan iş — veritabanı satırı, e-posta, ödeme çağrısı.

```svg
<svg viewBox="0 0 640 215" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="read-process-write EOS akisi ve Kafka disi sinir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">read-process-write EOS: yazma + offset tek transaction'da</text>
  <rect x="20" y="48" width="120" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="74" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Kafka'dan OKU</text>
  <text x="80" y="94" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">girdi topic</text>
  <rect x="170" y="48" width="120" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="230" y="74" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">İŞLE</text>
  <text x="230" y="94" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">dönüştür</text>
  <rect x="320" y="48" width="150" height="60" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="395" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kafka'ya YAZ</text>
  <text x="395" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">+ sendOffsetsToTx</text>
  <text x="395" y="102" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">commitTransaction()</text>
  <path d="M140 78 L168 78" stroke="#5D7789" stroke-width="1.5"/><polygon points="168,74 176,78 168,82" fill="#5D7789"/>
  <path d="M290 78 L318 78" stroke="#5D7789" stroke-width="1.5"/><polygon points="318,74 326,78 318,82" fill="#5D7789"/>
  <rect x="20" y="128" width="450" height="34" rx="8" fill="#F3F7EE" stroke="#25804C" stroke-width="1.2" stroke-dasharray="4 3"/>
  <text x="245" y="150" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">EOS geçerli: yazma + offset atomik · isolation.level=read_committed</text>
  <rect x="490" y="48" width="126" height="114" rx="10" fill="#FBECEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="553" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#B4322B" font-family="system-ui">Kafka DIŞI</text>
  <text x="553" y="94" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">DB / e-posta</text>
  <text x="553" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ödeme / REST</text>
  <text x="553" y="134" text-anchor="middle" font-size="9.5" fill="#B4322B" font-family="system-ui">EOS kapsam DIŞI</text>
  <text x="553" y="150" text-anchor="middle" font-size="9.5" fill="#B4322B" font-family="system-ui">idempotent olmalı</text>
  <text x="320" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">EOS yalnız Kafka→Kafka. Harici yan etkiler için: at-least-once + idempotent tüketim.</text>
</svg>
```

### Nasıl çalışır?

**read-process-write** (oku-işle-yaz) deseni adım adım şöyledir:

1. Üretici `transactional.id` ile açılır ve `initTransactions()` çağrılır.
2. `beginTransaction()` → tüketiciden okunan kayıtlar işlenir → sonuç `producer.send(...)` ile çıktı topic'ine yazılır.
3. **Kritik adım:** okunan okuma konumu, ayrı bir `commitSync` çağrısıyla **değil**, `sendOffsetsToTransaction(...)` ile **aynı işlemin içine** eklenir.
4. `commitTransaction()` çağrılır; yazma ile okuma konumu **birlikte** görünür olur. Çökme olursa işlem **iptal edilir (abort)**. O zaman ne yarım bir çıktı görünür ne de okuma konumu yanlış ilerler.
5. Tüketici tarafında `isolation.level=read_committed` şarttır. Yoksa yarım kalmış ya da iptal edilmiş işlerin mesajları da okunur. Bu ayar küçük bir gecikme ekleyebilir; çünkü sonucu henüz belli olmayan bir işlemin ötesindeki mesajları bekletebilir.

Bu kadar uğraşmanın nedeni, üç kademenin risk profilinin farklı olmasıdır:

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teslim semantigi kademeleri at most once at least once exactly once">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Teslim semantiği: kayıp mı, yinelenme mi, yoksa ikisi de yok mu?</text>
  <rect x="24" y="44" width="188" height="118" rx="10" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="118" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">at-most-once</text>
  <text x="118" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0 veya 1 kez</text>
  <text x="118" y="110" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">KAYIP mümkün</text>
  <text x="118" y="130" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yinelenme yok</text>
  <text x="118" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">commit-sonra-işle</text>
  <rect x="226" y="44" width="188" height="118" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="320" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">at-least-once</text>
  <text x="320" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">≥ 1 kez</text>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">YİNELENME mümkün</text>
  <text x="320" y="130" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kayıp yok</text>
  <text x="320" y="150" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">pratik hedef + idempotent</text>
  <rect x="428" y="44" width="188" height="118" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.6"/>
  <text x="522" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">exactly-once</text>
  <text x="522" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tam 1 kez</text>
  <text x="522" y="110" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kayıp da yinelenme de yok</text>
  <text x="522" y="130" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">bedava DEĞİL</text>
  <text x="522" y="150" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">transaction + idempotency</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Dağıtık sistemlerde ağ/crash gerçekleri yüzünden sağlam ve pratik hedef çoğu zaman at-least-once + idempotent tüketim.</text>
</svg>
```

Asıl sınır şudur: bu bölünmezlik yalnız **Kafka kaynakları** içindir. Kafka'nın işlemi bir veritabanı satırını, gönderilen bir e-postayı ya da çağrılan bir ödeme servisini **içine alamaz**. XA gibi birden çok sistemi kapsayan dağıtık bir işlem değildir.

Yan etki dış bir sistemdeyse "tam bir kez teslim" bir **mittir**. Doğru desen şudur: **en az bir kez teslim (at-least-once) + tekrar edilse de sonucu değişmeyen (idempotent) tüketim**. Her mesajı bir **iş anahtarıyla** işlersin; bu anahtar sipariş numarası ya da isteğe özel üretilmiş bir kimlik (idempotency key) olabilir. Aynı mesaj tekrar geldiğinde **varsa güncelle-yoksa ekle (upsert)** veya **tekrarı ele (dedup)** yaklaşımıyla ikinci kez etki üretmezsin.

Tüketicinin transactional mesajlardan hangisini gördüğü `isolation.level` ile belirlenir:

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="isolation level read committed yalniz commit edilmis mesaji gosterir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">isolation.level: tüketici transactional mesajın hangisini görür?</text>
  <text x="30" y="54" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">Log (transactional mesajlar)</text>
  <rect x="30" y="62" width="120" height="34" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/><text x="90" y="83" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">commit'li</text>
  <rect x="160" y="62" width="120" height="34" rx="5" fill="#FBECEA" stroke="#8A2E2E" stroke-width="1.4"/><text x="220" y="83" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">abort'lu</text>
  <rect x="290" y="62" width="150" height="34" rx="5" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/><text x="365" y="83" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">henüz commit değil</text>
  <rect x="450" y="62" width="120" height="34" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/><text x="510" y="83" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">commit'li</text>
  <text x="30" y="128" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">read_uncommitted (varsayılan)</text>
  <text x="30" y="146" font-size="10.5" fill="#1C3247" font-family="system-ui">HEPSİNİ gösterir: commit'li + abort'lu + henüz-commit-değil → yarım işleri okuyabilir</text>
  <text x="30" y="176" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">read_committed (EOS için)</text>
  <text x="30" y="194" font-size="10.5" fill="#1C3247" font-family="system-ui">yalnız commit'li olanları gösterir; abort'lu ve henüz-commit-değil atlanır (küçük gecikme ekleyebilir)</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Üç semantiğin farkı?"** at-most-once kayıp riski taşır, at-least-once yinelenme riski taşır, exactly-once ikisini de istemez ama bedellidir. at-least-once **kayıp yapmaz** — bunu tersine bilmek klasik hatadır.
- **"Kafka transaction ne sağlar?"** Çok bölüme yazma ile okuma konumu kaydının birlikte bölünmez olmasını, **tek küme (cluster)** içinde sağlar. Kümeler arası değildir, XA benzeri de değildir.
- **"read-process-write'ta EOS nasıl kurulur?"** Ofseti `sendOffsetsToTransaction` ile aynı transaction'a alıp tek commit yaparak. Ofseti ayrı `commitSync` ile commit etmek EOS'u bozar.
- **"`read_committed` ne yapar, varsayılan mıdır?"** Yalnız commit'li transactional mesajları gösterir; **varsayılan değildir** (varsayılan `read_uncommitted`), açıkça ayarlanır ve küçük bir gecikme ekleyebilir.
- **"EOS'un kapsam sınırı?"** Yalnız **Kafka→Kafka**. Harici yan etkiler kapsam dışıdır ve EOS onları idempotent hâle getirmez.
- **"Gerçek bir mikroservis akışında ne yaparsın?"** **at-least-once + idempotent tüketici** (anahtarla dedup/upsert). "Her yerde exactly-once" pazarlama dilidir; TCP de bu sorunu çözmez.

### Tek cümlede

Kafka'nın exactly-once'ı gerçektir ama **dardır**: idempotent üretici + transaction + `read_committed` ile Kafka'dan okuyup Kafka'ya yazan akışlarda geçerlidir, dış yan etki girdiği anda doğru cevap at-least-once + idempotent tüketimdir.

# keos-1 | Orta
## Soru
Teslim semantiği kademeleri (at-most-once / at-least-once / exactly-once) için hangisi doğrudur?
## A
at-most-once kayıp riski taşır, at-least-once yinelenme; dağıtık sistemlerde pratik hedef çoğu zaman at-least-once'tır
🤖 Doğru: üç kademe farklı risk profilini tanımlar; en yaygın gerçekçi hedef at-least-once'tır.
## B
exactly-once, her sistemde ek çaba gerektirmeden gelen varsayılan teslim davranışıdır; ayrıca ayar gerekmez
🤖 exactly-once bedava değildir; transaction, idempotency gibi mekanizmalar gerektirir.
## C
at-least-once, tanımı gereği mesajların bir kısmını mutlaka kaybeder ve bu kaybı telafi etmez
🤖 at-least-once kaybetmez, gerekirse tekrar teslim eder (yinelenme riski taşır).
## D
Üç semantik de pratikte aynı sonucu üretir; aralarındaki fark yalnızca isimlendirmeden ibarettir
🤖 kayıp ve yinelenme davranışları belirgin biçimde farklıdır.
## Doğru
A
## Açıklama
Üç kademe farklı risk profilleri tanımlar: **at-most-once** mesajı en fazla bir kez teslim eder (crash penceresinde **kayıp** mümkün); **at-least-once** en az bir kez teslim eder (**yinelenme** mümkün, ama kayıp yok — C tersini iddia eder); **exactly-once** tam bir kez ister ama **bedava değildir**, transaction/idempotency gerektirir (B yanlış). Dağıtık sistemlerde ağ ve crash gerçekleri yüzünden pratik ve sağlam hedef çoğu zaman **at-least-once + idempotent tüketim**'dir. Üçünü eşitlemek (D) sistemin kayıp/yinelenme davranışını yanlış tasarlamaktır.

# keos-2 | İleri
## Soru
Kafka transactions ne sağlar?
## A
İki ayrı Kafka cluster'ı arasındaki yazmaları atomik hâle getirir; küme sınırını aşan garanti verir
🤖 transaction tek cluster içindir; cluster'lar arası atomiklik kapsamı dışıdır.
## B
Birden çok partition'a yazmayı ve tüketilen offset'in commit'ini atomik yapar: ya hepsi görünür ya hiçbiri
🤖 Doğru: transaction'ın özü, çok-partition yazma + offset commit atomikliğidir.
## C
Tüketici tarafındaki çift işlemeyi, ek bir ayar gerekmeden tek başına ortadan kaldırır
🤖 tüketicinin abort edilmişleri atlaması için read_committed gerekir; transaction tek başına yetmez.
## D
Kafka dışı veritabanı satırlarını da aynı transaction'a dahil eder (XA benzeri dağıtık işlem)
🤖 Kafka transaction'ı harici sistemleri (XA) kapsamaz; yalnız Kafka kaynaklarını atomik yapar.
## Doğru
B
## Açıklama
Kafka **transactions**, birden çok partition'a (hatta birden çok topic'e) yapılan yazmaları **ve** tüketilen offset'in commit'ini **tek atomik birim** yapar: transaction commit edilirse hepsi görünür olur, abort edilirse **hiçbiri**. API `transactional.id` ile başlar (üreticiyi kalıcı olarak tanımlar), `initTransactions/beginTransaction/.../commitTransaction` ile yürür. Ama kapsamı **tek Kafka cluster**'dır (A yanlış), **harici sistemleri kapsamaz** (D — XA benzeri değildir) ve tüketicinin gerçekten faydalanması için `isolation.level=read_committed` gerekir (C — tek başına yetmez).

# keos-3 | Uzman
## Soru
"read-process-write" akışında exactly-once nasıl doğru kurulur?
## A
Tüketilen offset'i ayrı bir `commitSync` çağrısıyla commit etmek atomikliği sağlamaya yeter
🤖 ayrı commitSync, yazma ile offset'i farklı atomik birimlere böler; crash arada tutarsızlık yaratır.
## B
`producer.send` yeterlidir; offset'i transaction'dan bağımsız biçimde elle commit etmek gerekir
🤖 offset'i transaction'dan bağımsız commit etmek EOS'u bozar; ikisi tek transaction'da olmalı.
## C
Tüketilen offset'i `sendOffsetsToTransaction` ile üretim transaction'ına dahil edip tek commit yaparsın
🤖 Doğru: offset commit'i yazmayla aynı transaction'a almak EOS'un anahtarıdır.
## D
read-process-write akışında exactly-once teknik olarak imkânsızdır; en fazla at-least-once olur
🤖 Kafka→Kafka akışında transaction ile EOS mümkündür; imkânsız değildir.
## Doğru
C
## Açıklama
**read-process-write** deseninde EOS'un püf noktası, tüketilen **offset'in commit'ini** çıktının **yazma transaction'ına dahil etmektir**: `sendOffsetsToTransaction(...)` ile offset transaction'a eklenir ve `commitTransaction()` ile yazma + offset **tek atomik commit** olarak yürür. Böylece crash olursa transaction abort olur; ne yarım yazma görünür ne de offset yanlış ilerler. Offset'i **ayrı** commit etmek (A, B) iki farklı atomik birim yaratır ve crash arada tutarsızlık (çift yazma veya kayıp offset) doğurur. Kafka→Kafka akışında bu **mümkündür**, imkânsız değildir (D).

# keos-4 | İleri
## Soru
`isolation.level=read_committed` tüketicide ne yapar?
## A
Tüketiciyi hiç yavaşlatmaz; açık bir transaction olsa bile tüm mesajları beklemeden okur
🤖 read_committed, açık transaction'ların sonucu belli olana dek ilgili mesajları bekletebilir.
## B
Varsayılan değer zaten read_committed'tır; EOS için ayrıca bir şey ayarlamaya gerek yoktur
🤖 varsayılan read_uncommitted'tır; commit'lileri süzmek için read_committed'ı açıkça ayarlaman gerekir.
## C
read_committed, abort edilmiş transaction mesajlarını da tüketiciye teslim eder; yalnız sırayı değiştirir
🤖 read_committed abort edilenleri gizler; tam da onları atlamak için vardır.
## D
Tüketici yalnız commit edilmiş transactional mesajları görür; abort edilenler ve bekleyenler atlanır
🤖 Doğru: read_committed, transactional akışta yalnız kesinleşmiş veriyi okutur.
## Doğru
D
## Açıklama
`isolation.level=read_committed` ayarıyla tüketici yalnız **commit edilmiş** transactional mesajları görür; **abort edilenleri** ve **henüz commit edilmemişleri** atlar (C tersini iddia eder). Bu, EOS akışlarında tüketicinin yarım kalmış işleri okumamasını sağlar. Varsayılan ise **`read_uncommitted`**'tır ve **her şeyi** gösterir (B yanlış) — EOS istiyorsan bunu açıkça değiştirmelisin. Ayrıca read_committed, henüz sonucu belli olmayan açık transaction'ların ötesindeki mesajları bekletebileceğinden küçük bir gecikme ekleyebilir (A'nın "hiç yavaşlatmaz" iddiası bu yüzden yanlış).

# keos-5 | Uzman
## Soru
[TUZAK] Kafka'nın exactly-once garantisinin en önemli kapsam sınırı nedir?
## A
Kafka'nın EOS'u, harici DB ve REST çağrıları dahil bütün downstream yan etkileri de kapsar
🤖 EOS harici sistemleri kapsamaz; asıl sınır tam da budur.
## B
EOS, bağlanan her downstream sistem için ek bir yapılandırma olmadan otomatik geçerlidir
🤖 otomatik değildir; yalnız Kafka→Kafka transactional akışında geçerlidir.
## C
EOS yalnız Kafka'dan okuyup Kafka'ya yazan akışlar içindir; harici sistemdeki yan etkiler kapsam dışıdır
🤖 Doğru: EOS Kafka kaynaklarıyla sınırlıdır; harici yan etkiler kapsam dışıdır.
## D
EOS, harici yan etkileri de otomatik olarak idempotent hâle getirir; ek bir önlem gerekmez
🤖 EOS harici çağrıları idempotent yapmaz; idempotency'yi uygulama sağlamalıdır.
## Doğru
C
## Açıklama
Kafka'nın transactional **EOS garantisi yalnız Kafka→Kafka** akışları içindir: Kafka'dan oku, işle, Kafka'ya yaz ve tüketilen offset'i **aynı transaction'a** dahil et. İşin içine **harici bir sistem** (veritabanı satırı, e-posta gönderimi, ödeme API'si) girdiğinde Kafka'nın transaction'ı o çağrıyı **kapsamaz** — o yan etki transaction'a dahil değildir ve idempotent değilse (aynı mesaj tekrar işlenirse çift kayıt/çift ödeme) **EOS kırılır**. Kafka bu çağrıları otomatik idempotent yapmaz (D) ve garanti her downstream için otomatik değildir (A, B). Sınırı bilmek, "EOS açtık, artık her yerde tam bir kez" yanılgısından korur.

# keos-6 | Uzman
## Soru
[TUZAK] "Sistemimizin her yerinde exactly-once istiyoruz." Harici yan etkileri (DB yazma, ödeme) olan gerçek bir mikroservis akışında en sağlam pratik yaklaşım nedir?
## A
at-least-once teslim + idempotent tüketici: bir iş anahtarıyla dedup / upsert yaparak tekilliği sağlamak
🤖 Doğru: dağıtık yan etkilerde tekilliği tüketici idempotency'si sağlar; at-least-once + dedup en dayanıklı desendir.
## B
Harici çağrılar dahil tüm sistemi tek bir Kafka transaction'ının içine sarmak
🤖 Kafka transaction'ı harici sistemleri kapsayamaz; bu mümkün değildir.
## C
at-most-once seçip olası kayıpları kabul etmek en temiz ve en öngörülebilir çözümdür
🤖 at-most-once veri kaybı riski taşır; kritik akışlarda kabul edilemez ve tekilliği çözmez.
## D
Ağ katmanında TCP kullanmak uygulama düzeyinde de uçtan uca exactly-once sağlar
🤖 TCP paket teslimini yönetir; uygulama düzeyi crash/retry kaynaklı yinelenmeyi çözmez.
## Doğru
A
## Açıklama
"Her yerde exactly-once" bir **mittir**; harici yan etkileri olan gerçek akışlarda Kafka'nın transaction'ı o çağrıları kapsayamaz (B imkânsızı ister). En sağlam desen: **at-least-once teslim + idempotent tüketici**. Tüketici, her mesajı bir **anahtarla** (doğal iş anahtarı ya da üretilmiş bir dedup anahtarı) işler; aynı mesaj tekrar gelirse **upsert / dedup** ile ikinci kez yan etki üretmez (ör. ödemede idempotency key). Böylece yinelenmeye dayanıklı, kayıpsız bir sistem kurarsın. at-most-once (C) kayıp riski taşır ve tekilliği çözmez; TCP (D) yalnız paket teslimini yönetir, uygulama düzeyi crash/retry yinelenmesine çare değildir.
