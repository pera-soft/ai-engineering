---
slug: iyimser-kotumser-kilit
baslik: "İyimser ve Kötümser Kilitleme: @Version, FOR UPDATE, Yeniden Deneme ve Kilitlenme"
kademe: İleri
sira: 225
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Ortak bir dolapta tek nüsha bir dosya var ve iki kişinin de o dosyaya not yazması gerekiyor. İki yöntem var. **Birincisi**: dosyayı alırken kapıya "meşgul" tabelası asarsın, işin bitene kadar kimse giremez — kimse çakışmaz ama herkes bekler. **İkincisi**: dosyanın fotokopisini alıp masanda çalışırsın; geri koyarken "ben aldığımda 7. revizyondaydı, hâlâ 7 mi?" diye bakarsın. Hâlâ 7 ise notunu yazarsın; 8 olmuşsa biri araya girmiştir, işini **baştan yaparsın**. Birincisi **kötümser kilitleme** (pessimistic locking), ikincisi **iyimser kilitlemedir** (optimistic locking). Bankacılıkta bu seçim doğrudan paraya dokunur: aynı krediye aynı anda iki ödeme geldiğinde, iki tahsilat da bakiyeyi düşürmeli — biri sessizce kaybolmamalı.

Somut kurgu şu: `Kredi` satırında `kalanBorc = 10.000`. Ödeme servisi "satırı oku, kalan borçtan tahsilatı düş, yaz" biçiminde yazılmış. Kullanıcı mobilden 3.000, şubeden aynı anda 2.000 ödüyor. İkisi de 10.000 okur; biri 7.000 yazar, diğeri 8.000 yazar. Müşteri 5.000 ödemiştir ama kayıtta 2.000'lik ödeme yok olmuştur. Bu **kayıp güncellemedir** ve hiçbir hata mesajı üretmez. Kilitleme, tam olarak bu boşluğu kapatmak içindir.

### Temel kavramlar

- **İyimser kilitleme (optimistic locking)**: Hiç kilit tutmadan çalışıp, çakışmayı **yazma anında** yakalama yaklaşımı. "Çakışma nadirdir" varsayımına dayanır.
- **Sürüm sütunu (`@Version`)**: Varlıktaki, her güncellemede bir artan sayaç. Çakışma tespiti buna dayanır.
- **Kötümser kilitleme (pessimistic locking)**: Satırı okurken veritabanı kilidi alıp, işin bitene kadar başkasını bekleten yaklaşım. "Çakışma sıktır" varsayımına dayanır.
- **`SELECT ... FOR UPDATE`**: Okunan satırları işlem sonuna kadar yazma için kilitleyen SQL cümlesi.
- **NOWAIT**: Kilit meşgulse beklemeden hemen hata almak.
- **SKIP LOCKED**: Kilitli satırları beklemeden **atlayıp** bir sonrakine geçmek.
- **Yeniden deneme (retry)**: Çakışma yüzünden düşen işlemi baştan çalıştırmak.
- **Atomik koşullu güncelleme**: Okuma ile yazmayı tek `UPDATE` cümlesinde birleştirip koşulu `WHERE` içine koymak.
- **Kilitlenme (deadlock)**: İki işlemin karşılıklı olarak birbirinin tuttuğu kilidi beklemesi; hiçbiri ilerleyemez.

```svg
<svg viewBox="0 0 660 252" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İyimser ve kötümser kilitleme akışlarının karşılaştırması">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı krediye iki eşzamanlı ödeme: iki yaklaşım</text>
  <rect x="12" y="34" width="312" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="54" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İYİMSER — kilit yok, çakışmayı yakala</text>
  <text x="26" y="76" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">1. oku: kalanBorc=10.000, surum=7</text>
  <text x="26" y="94" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">2. iş mantığı çalışır — KİLİT YOK</text>
  <text x="26" y="112" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">3. UPDATE ... WHERE id=? AND surum=7</text>
  <text x="26" y="130" text-anchor="start" font-size="10.5" fill="#B23B3B" font-family="system-ui">4. 0 satır etkilendi → çakışma istisnası</text>
  <text x="26" y="148" text-anchor="start" font-size="10.5" fill="#25804C" font-family="system-ui">5. YENİDEN DENE (yeni bir işlemle)</text>
  <text x="26" y="172" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">Kimse beklemez; çakışırsa iş boşa gider.</text>
  <rect x="336" y="34" width="312" height="152" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="492" y="54" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">KÖTÜMSER — önce kilitle, sonra çalış</text>
  <text x="350" y="76" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">1. SELECT ... FOR UPDATE (satır kilitli)</text>
  <text x="350" y="94" text-anchor="start" font-size="10.5" fill="#B23B3B" font-family="system-ui">2. rakip işlem BEKLER (ya da zaman aşımı)</text>
  <text x="350" y="112" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">3. iş mantığı çalışır, yazma yapılır</text>
  <text x="350" y="130" text-anchor="start" font-size="10.5" fill="#25804C" font-family="system-ui">4. COMMIT → kilit bırakılır</text>
  <text x="350" y="148" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">5. rakip uyanır, TAZE veriyle devam eder</text>
  <text x="350" y="172" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">İş boşa gitmez; sıra beklenir, verim düşer.</text>
  <rect x="12" y="196" width="636" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="215" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Seçim ölçütü: çekişme oranı (aynı satıra kaç istek düşüyor) ve işlem süresi.</text>
  <text x="330" y="232" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Çekişme düşük + işlem kısa → iyimser. Çekişme yüksek + iş kısa ve kesin sıralanmalı → kötümser.</text>
</svg>
```

### Nasıl çalışır?

**İyimser kilitleme** varlığa bir `@Version` alanı eklemekle başlar (`int`, `long`, `short`, `Instant`, `Timestamp` tipleriyle çalışır). Bundan sonra Hibernate ürettiği her `UPDATE` cümlesine sürüm koşulunu ekler:

```sql
UPDATE kredi SET kalan_borc = ?, surum = surum + 1 WHERE id = ? AND surum = ?
```

İkinci işlemin elindeki sürüm eskimişse bu cümle **0 satır** etkiler. Hibernate bunu çakışma olarak yorumlayıp istisna fırlatır: JPA katmanında `OptimisticLockException`, Spring'in çeviri katmanından geçmiş hâli `ObjectOptimisticLockingFailureException`. Dikkat: bu **hiçbir veritabanı kilidi tutmaz**; "iyimser kilit" adı yanıltıcıdır, ortada kilit yoktur, yalnız bir çakışma tespiti vardır.

Sürüm alanı bulunmayan ilişkili satırların da korunması gerekebilir. Bunun için iki ek kip vardır. `LockModeType.OPTIMISTIC`, okunan satırın sürümünü commit anında yeniden doğrular. `LockModeType.OPTIMISTIC_FORCE_INCREMENT` ise satır hiç değişmese bile sürümü artırır. Örnek: taksit satırı eklendiğinde ana kredi satırının sürümünü artırıp "bu kredi değişti" demek istersin.

Çakışma yakalandıktan sonra ne yapılacağı asıl mesele: **yeniden deneme**. Buradaki en sık hata, yeniden denemeyi `@Transactional` metodun **içinde** yapmaktır. İstisna fırladığında o işlem çoktan "yalnız geri alınabilir" (rollback-only) damgasını yemiştir. Kalıcı bağlam (persistence context) da tutarsız duruma düşmüştür. Aynı bağlamda ikinci deneme ya aynı hatayı verir, ya da commit anında `UnexpectedRollbackException` ile patlar.

Yeniden deneme **işlem sınırının dışında**, **yeni bir işlem** açacak biçimde kurulmalıdır. Kurulumu şöyledir: dıştaki bir bean'in (Spring'in yönettiği nesne) metoduna `@Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 50, multiplier = 2, random = true))` konur. O metot da içeriden `@Transactional` metodu çağırır. Elle kurmak istersen `RetryTemplate` aynı işi yapar. Bekleme süresine rastgelelik (jitter) eklemek de önemlidir; yoksa düşen istekler hep birlikte aynı anda geri döner ve yeni bir çakışma dalgası yaratır.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yeniden deneme döngüsü işlem sınırının dışında kurulur">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yeniden deneme, işlem sınırının DIŞINDA olur</text>
  <rect x="20" y="36" width="620" height="126" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6" stroke-dasharray="6 4"/>
  <text x="36" y="56" text-anchor="start" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">@Retryable / RetryTemplate — işlemsiz dış katman</text>
  <rect x="52" y="68" width="330" height="76" rx="9" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.4"/>
  <text x="217" y="90" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">@Transactional { ... } — HER DENEMEDE YENİ</text>
  <text x="217" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oku (surum=7) → hesapla → UPDATE</text>
  <text x="217" y="130" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">0 satır → ObjectOptimisticLockingFailureException</text>
  <rect x="404" y="68" width="216" height="76" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="512" y="90" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">çakışma mı?</text>
  <text x="512" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">evet → kısa bekle (jitter)</text>
  <text x="512" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ve baştan çalıştır (en çok 3)</text>
  <path d="M382 106 L400 106" stroke="#5D7789" stroke-width="1.5" fill="none" marker-end="url(#kilit-ok)"/>
  <path d="M512 68 L512 58 L217 58 L217 66" stroke="#25804C" stroke-width="1.5" fill="none" marker-end="url(#kilit-ok2)"/>
  <rect x="20" y="172" width="620" height="52" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="192" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Aynı işlemin İÇİNDE yakalayıp tekrar denemek işe yaramaz</text>
  <text x="330" y="210" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">işlem rollback-only damgalıdır, kalıcı bağlam bozuktur → UnexpectedRollbackException</text>
  <defs>
    <marker id="kilit-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
    <marker id="kilit-ok2" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#25804C"/>
    </marker>
  </defs>
</svg>
```

**Kötümser kilitleme** ise Spring Data'da depo (repository) metodunun üstüne konan `@Lock` ile istenir ve altta gerçek bir veritabanı kilidi alınır:

- `LockModeType.PESSIMISTIC_READ` — paylaşımlı kilit: başkaları okuyabilir, yazamaz.
- `LockModeType.PESSIMISTIC_WRITE` — dışlayıcı kilit: `SELECT ... FOR UPDATE` üretir, satır işlem bitene kadar başkasına kapalıdır. Günlük kullanımda kastedilen budur.
- `LockModeType.PESSIMISTIC_FORCE_INCREMENT` — hem kilidi alır hem sürüm sütununu artırır; kilitli okuduğun satırın "değişti" işaretini de almak istediğinde kullanılır.

Kilit sonsuza kadar beklemesin diye zaman aşımı verilebilir: `@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))` (Jakarta EE öncesi adı `javax.persistence.lock.timeout` idi). İki özel değer vardır: **0 → NOWAIT** (kilit meşgulse hiç bekleme, hemen hata ver) ve **-2 → SKIP LOCKED** (kilitli satırları atla). `SKIP LOCKED`, kuyruk tablosundan iş dağıtımının standart çözümüdür. Diyelim gün sonu tahakkuk işlerini on çalışan (worker) aynı tablodan çekiyor. Her biri `SELECT ... FOR UPDATE SKIP LOCKED` ile **başkasının kilitlemediği** ilk satırı alır. Sonuç: aynı iş iki kez işlenmez ve kimse kimseyi beklemez. `NOWAIT` ise beklemez ama satır da vermez, hata verir; kuyruk dağıtımı için uygun değildir.

Kilitlerle ilgili unutulmaması gereken kural: **kilit işlemin bitiminde bırakılır.** Bu yüzden işlem sınırı ne kadar genişse kilit o kadar uzun tutulur. `@Transactional` metodun içine dış servis çağrısı, dosya yazma ya da e-posta gönderimi koymak, kilidi saniyelerce tutup tüm sistemi yavaşlatır. Aynı nedenle **kullanıcı düşünme süresi** boyunca kilit tutulamaz: kredi başvuru ekranını açan uzman on dakika inceleyip kaydediyorsa, o on dakika boyunca işlem açık bırakılamaz. Bunun deseni **çevrimdışı iyimser kilittir** (offline optimistic lock). Satırın sürüm numarası ekrana ya da forma taşınır. Kaydetme isteğinde geri gönderilir ve kısa bir işlem içinde karşılaştırılır. Sürüm değişmişse kullanıcıya "bu kayıt siz bakarken değişti" denir.

Üçüncü bir yol daha vardır ve tek satırlık sayaç/bakiye güncellemelerinde çoğu zaman **en iyisidir**: **atomik koşullu güncelleme**. Okuma ile yazma arasındaki boşluğu hiç açmazsın, koşulu `WHERE`e koyarsın:

```sql
UPDATE kredi SET kalan_borc = kalan_borc - :tutar WHERE id = :id AND kalan_borc >= :tutar
```

Sonra **etkilenen satır sayısını** kontrol edersin. 1 ise işlem geçerlidir. 0 ise koşul sağlanmamıştır — yetersiz bakiye ya da satır yok — ve isteği reddedersin. Kilit yok, sürüm yok, yeniden deneme yok; çakışma penceresi hiç oluşmaz.

Bedeli şudur: bu bir JPQL/SQL toplu güncellemesidir ve **kalıcı bağlamı atlar**. Bellekteki varlık nesnesi eski değeri taşımaya devam eder; aynı işlem içinde onu okursan bayat veri görürsün. Spring Data'da çözüm `@Modifying(clearAutomatically = true, flushAutomatically = true)` kullanmaktır. `flushAutomatically` bekleyen değişiklikleri sorgudan önce veri tabanına yazar. `clearAutomatically` ise sorgudan sonra bağlamı temizler, böylece bir sonraki okuma taze gelir. Yalnız bu temizleme, bağlamdaki **tüm** varlıkları yönetimden çıkarır. Metodun geri kalanında elindeki nesnelerin artık yönetilmediğini (detached) hesaba katmalısın.

Kilitlerin ikinci klasik tuzağı **kilitlenmedir** (deadlock). Bir virman işlemi Hesap-1'i kilitleyip Hesap-2'yi isterken, eşzamanlı diğer virman Hesap-2'yi kilitleyip Hesap-1'i isterse ikisi de sonsuza kadar bekler. Veritabanı bu döngüyü tespit eder, işlemlerden birini **kurban** (victim) seçip geri alır ve ona hata döner (MySQL'de `Deadlock found when trying to get lock`, PostgreSQL'de `40P01`, Oracle'da `ORA-00060`). Yani veritabanı sistemi kilitli bırakmaz; **hatayı yakalayıp yeniden denemek uygulamanın işidir**. Kalıcı çözüm ise daha basittir: **kaynakları her yerde aynı sırada kilitlemek** — örneğin virmanda hangi hesap gönderen hangisi alan olursa olsun, satırları her zaman hesap numarasına göre artan sırayla kilitlemek. Sıra sabitlenince döngü kurulamaz.

```svg
<svg viewBox="0 0 660 244" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kilitlenme döngüsü ve kilitleri aynı sırada alarak çözme">
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kilitlenme (deadlock): karşılıklı bekleme döngüsü</text>
  <rect x="40" y="38" width="200" height="58" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="140" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">İşlem A (virman 1→2)</text>
  <text x="140" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hesap-1 kilitli · Hesap-2 bekliyor</text>
  <rect x="420" y="38" width="200" height="58" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="520" y="60" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">İşlem B (virman 2→1)</text>
  <text x="520" y="80" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hesap-2 kilitli · Hesap-1 bekliyor</text>
  <path d="M240 56 L418 56" stroke="#B23B3B" stroke-width="1.6" fill="none" marker-end="url(#kilit-dl)"/>
  <path d="M420 82 L242 82" stroke="#B23B3B" stroke-width="1.6" fill="none" marker-end="url(#kilit-dl)"/>
  <text x="330" y="50" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">bekliyor</text>
  <text x="330" y="98" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">bekliyor</text>
  <rect x="40" y="112" width="580" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="131" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Veritabanı döngüyü görür, bir KURBAN seçip geri alır (ORA-00060 / 40P01 / deadlock found)</text>
  <text x="330" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Sistem kilitli kalmaz — ama hatayı yakalayıp yeniden denemek uygulamanın işidir</text>
  <rect x="40" y="170" width="580" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="192" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kalıcı çözüm: kaynakları HER YERDE aynı sırada kilitle</text>
  <text x="330" y="211" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Virmanda gönderen/alan fark etmez: satırlar daima hesap numarasına göre artan sırayla kilitlenir.</text>
  <text x="330" y="226" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Sıra sabitlenince döngü kurulamaz; ayrıca işlemi kısa tut, kilit süresi işlem süresidir.</text>
  <defs>
    <marker id="kilit-dl" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#B23B3B"/>
    </marker>
  </defs>
</svg>
```

Son olarak dağıtık ortamı konuşalım. Uygulama birden çok örnek (instance) hâlinde çalışıyorsa, Java'nın `synchronized`'ı ya da `ReentrantLock`'ı hiçbir işe yaramaz; o kilit yalnız tek JVM içinde geçerlidir. Akla ilk gelen çözüm Redis tabanlı **dağıtık kilittir**. Ama aradaki fark önemlidir.

Veri tabanı kilidi, veriyi tutan sistemin **kendi içindedir**. Kilitle veri aynı işlemin içinde durur ve işlem bitince kilit kesin olarak bırakılır. "Kilidi tuttuğunu sanan ama süresi dolmuş" bir düğüm veriye yazamaz. Redis kilidi ise veriden **ayrı bir sistemdedir**. Süre aşımı, saat kayması ve ağ gecikmesi yüzünden iki düğüm aynı anda kendini kilit sahibi sanabilir. Doğruluk isteniyorsa yazma tarafında **fencing token** (her kilit sahibine artan bir numara verip eski numaralı yazmayı reddetme) gibi ek bir korumaya ihtiyaç duyulur; bu tartışma 129 ve 160 numaralı konuların ayrıntısıdır.

Pratik kural şudur: **kilitlenecek şey zaten veri tabanındaki bir satırsa, veri tabanı kilidi ya da sürüm sütunu tercih edilir**. Dağıtık kilit ise veri tabanı dışı bir kaynağı korumak gerektiğinde gündeme gelir: dosya, dış API kotası, tekil zamanlanmış iş gibi.

### Mülakatta ne sorulur / tuzaklar

- **"İyimser kilit hangi kilidi tutar?"** → Hiçbirini. `@Version` bir tespit mekanizmasıdır; `SELECT ... FOR UPDATE` üretmez, satırı bloklamaz. "Okurken kilitler" derseniz elenirsiniz.
- **"Çakışmayı yakaladın, ne yaparsın?"** → Yeniden denersin — ama **yeni bir işlemle**. Aynı `@Transactional` metodun içinde yakalayıp döngüye girmek işe yaramaz: işlem rollback-only damgalıdır, kalıcı bağlam bozuktur.
- **"Hangisini ne zaman seçersin?"** → Çekişme düşük ve işlem kısaysa iyimser (kimse beklemez, nadir çakışmada iş tekrarlanır). Çekişme yüksekse iyimser kilit "sürekli yeniden deneme" çöplüğüne döner; kısa ve kesin sıralanması gereken bölge için kötümser kilit daha doğrudur.
- **"On çalışan aynı kuyruk tablosundan iş çekiyor"** → `SELECT ... FOR UPDATE SKIP LOCKED`. `NOWAIT` hata verir, satır vermez; sade `FOR UPDATE` herkesi aynı satırda sıraya sokar; `@Version` ile dokuz çalışan boşuna deneyip düşer.
- **"Bakiye düşümü için en ucuz çözüm?"** → Genelde atomik koşullu güncelleme: `UPDATE ... SET bakiye = bakiye - :tutar WHERE id = :id AND bakiye >= :tutar` ve **etkilenen satır sayısı** kontrolü. JPA ile kullanırken `@Modifying(clearAutomatically = true, flushAutomatically = true)` şart, yoksa bellekteki varlık bayatlar.
- **"Kilitlenme olursa sistem kilitli mi kalır?"** → Hayır; veritabanı döngüyü tespit edip bir kurbanı geri alır. Uygulamanın görevi hatayı yakalayıp yeniden denemek, kalıcı çözümü ise kilitleri hep aynı sırada almaktır.
- **"Kullanıcı formu doldururken satırı kilitleyelim mi?"** → Asla. Kilit işlem bitince bırakılır; kullanıcı düşünme süresi boyunca işlem açık tutulmaz. Doğru desen çevrimdışı iyimser kilittir.
- **"Dağıtık ortamda `synchronized` yeter mi?"** → Hayır, tek JVM içinde kalır. Veritabanındaki bir satır söz konusuysa veritabanı kilidi/sürüm sütunu; veritabanı dışı bir kaynak söz konusuysa dağıtık kilit (ve fencing token) gerekir.

### Tek cümlede

İyimser kilit hiç kilit tutmaz; çakışmayı yazma anında yakalar ve yeniden denemeyi işlem dışında ister. Kötümser kilit ise satırı `FOR UPDATE` ile gerçekten kilitleyip başkasını bekletir. Ama tek satırlık bakiye ve limit düşümlerinde en ucuz, en güvenli yol çoğu zaman şudur: okuma ile yazmayı tek atomik cümlede birleştir ve etkilenen satır sayısını kontrol et.

# kilit-1 | Orta
## Soru
`@Transactional` işaretli bir servis metodunun içinde `ObjectOptimisticLockingFailureException`'ı yakalayıp aynı metot içinde bir `for` döngüsüyle işlemi tekrar denemeye çalışıyorsun. Bu yaklaşımın temel sorunu nedir?
## A
İstisna anında işlem zaten geri alınmaya damgalanmış ve kalıcı bağlam tutarsız duruma düşmüştür; yeniden deneme işlem sınırının dışında, yeni bir işlem açacak biçimde kurulmalıdır.
🤖 Doğru: bozulmuş bir işlemin içinde tekrar denemek çalışmaz; her deneme yeni bir işlem olmalıdır.
## B
Sorun yoktur; JPA istisna sonrasında kalıcı bağlamı (persistence context) otomatik olarak temizleyip taze bir duruma döndürdüğü için aynı işlem içinde tekrar denemek güvenlidir.
🤖 istisnadan sonra bağlam otomatik olarak sağlıklı bir duruma dönmez.
## C
Sorun, döngünün sürüm sütununu artırmamasıdır; elle `surum = surum + 1` yazılırsa aynı işlem içindeki yeniden deneme sorunsuz çalışır.
🤖 sürümü elle artırmak bozulmuş işlemi kurtarmaz, veriyi de bozar.
## D
Sorun, Spring'in elle yazılmış yeniden deneme döngülerini engellemesidir; bu yüzden yalnız `@Retryable` ya da `RetryTemplate` kullanılabilir, elle döngü kurmak yasaktır.
🤖 Spring elle döngü kurmayı engellemez; mesele döngünün nereye konduğudur.
## Doğru
A
## Açıklama
İyimser kilit çakışması fırladığında iki şey birden bozulmuştur: işlem "yalnız geri alınabilir" (rollback-only) olarak **damgalanmıştır** ve **kalıcı bağlam** (persistence context) tutarsızdır — içindeki varlıklar veritabanındaki gerçekle uyuşmayan sürüm bilgisi taşır. Bu yüzden aynı işlem içinde tekrar denemek ya aynı hatayı verir ya da commit anında `UnexpectedRollbackException` ile patlar; JPA bağlamı kendiliğinden onarmaz (B). Sürümü elle artırmak da bir onarım değildir, üstelik çakışmayı yok sayıp veriyi bozar (C). Doğru desen, yeniden denemeyi **işlem sınırının dışına** almaktır: dış bir bean'in metoduna `@Retryable(retryFor = ObjectOptimisticLockingFailureException.class)` konur ve o metot içeriden `@Transactional` metodu çağırır — böylece her deneme **yepyeni bir işlem ve yeni bir kalıcı bağlamla** başlar. Elle döngü kurmak yasak değildir (D); yeter ki döngü işlemi kapsasın, işlemin içinde kalmasın. Beklemeye rastgelelik (jitter) eklemek de düşen isteklerin aynı anda geri dönüp yeni bir çakışma dalgası yaratmasını önler.

# kilit-2 | Orta
## Soru
Bir Spring Data JPA depo metodunun üstüne `@Lock(LockModeType.PESSIMISTIC_WRITE)` koyup kredi satırını okudun. Veritabanı seviyesinde ne olur?
## A
Hibernate satırın bir kopyasını bellekte işaretler ve üretilen SQL sıradan bir `SELECT` olur; kilit yalnız aynı uygulama örneği içindeki iş parçacıkları için geçerlidir.
🤖 kötümser kilit veritabanı kilididir, uygulama belleğinde tutulan bir işaret değildir.
## B
Sorguya sürüm koşulu eklenir ve çakışma yalnız yazma anında `OptimisticLockException` ile bildirilir; okuma sırasında hiçbir satır kilitlenmez.
🤖 bu iyimser kilitleme davranışıdır; PESSIMISTIC_WRITE gerçek kilit alır.
## C
Veritabanına `SELECT ... FOR UPDATE` gönderilir; satır işlem bitene kadar kilitli kalır ve aynı satırı kilitlemek isteyen ikinci işlem bekler.
🤖 Doğru: PESSIMISTIC_WRITE dışlayıcı satır kilidi alır ve rakip işlemi bekletir.
## D
Satır salt okunur olarak işaretlenir; başka işlemler onu serbestçe okuyabilir ama uygulamanın kendisi de o satırı aynı işlem içinde güncelleyemez.
🤖 kilidi alan işlem satırı güncellemek için almıştır; kendisi engellenmez.
## Doğru
C
## Açıklama
`LockModeType.PESSIMISTIC_WRITE` **dışlayıcı bir veritabanı satır kilidi** ister ve altta `SELECT ... FOR UPDATE` üretilir. Kilit alındığı andan **işlemin bitişine** kadar sürer; aynı satırı kilitlemek isteyen ikinci işlem, birincisi commit ya da rollback edene kadar **bekler**. Bu, uygulama belleğinde tutulan bir işaret değildir (A) — tam da bu yüzden birden çok uygulama örneği çalışırken de doğru sonuç verir, oysa `synchronized` vermez. Sürüm koşuluyla çakışmayı yazma anında yakalamak (B) **iyimser** kilitlemenin davranışıdır ve hiçbir kilit tutmaz; ikisi farklı stratejilerdir. Kilidi alan işlemin kendisi satırı elbette güncelleyebilir (D yanlış); "salt okunur" davranış `PESSIMISTIC_READ`'e daha yakındır ve o da paylaşımlı kilittir. Beklemenin sonsuza uzamaması için zaman aşımı verilebilir: `jakarta.persistence.lock.timeout` ipucu; değeri 0 ise `NOWAIT`, -2 ise `SKIP LOCKED` davranışı istenir.

# kilit-3 | İleri
## Soru
Gün sonu tahakkuk işleri bir kuyruk tablosunda tutuluyor ve 10 çalışan (worker) aynı tablodan iş çekiyor. Aynı işin iki kez işlenmemesi, ayrıca çalışanların birbirini beklememesi isteniyor. Hangi yaklaşım bu iki şartı birlikte karşılar?
## A
`PESSIMISTIC_READ` ile paylaşımlı kilit alıp satırı okumak; paylaşımlı kilit birden çok çalışanın aynı anda okumasına izin verdiği için bekleme oluşmaz.
🤖 paylaşımlı kilit hepsinin aynı satırı okumasına izin verir, aynı iş birden çok kez işlenir.
## B
`SELECT ... FOR UPDATE SKIP LOCKED` ile okumak; başkasının kilitlediği satırlar atlanır, her çalışan farklı bir satır alır ve kimse beklemez.
🤖 Doğru: SKIP LOCKED tam olarak kuyruktan iş dağıtımı için tasarlanmıştır.
## C
Kilit zaman aşımını 0 yapıp `NOWAIT` davranışı istemek; kilitli satıra takılan çalışan beklemek yerine hemen hata alıp bir sonraki denemesine geçer.
🤖 NOWAIT satır vermez, hata verir; çalışanlar iş yerine hata toplar.
## D
Varlığa `@Version` ekleyip iyimser kilit kullanmak; çakışan çalışanlar istisna alıp yeniden deneyeceği için sonunda her iş tam bir kez işlenmiş olur.
🤖 on çalışan da aynı ilk satırı seçer, dokuzu boşa çalışıp düşer; verim çöker.
## Doğru
B
## Açıklama
Kuyruk tablosundan iş dağıtımının standart çözümü `SELECT ... FOR UPDATE SKIP LOCKED`'tır (JPA tarafında kilit zaman aşımı ipucuna `-2` verilerek istenir). Davranışı tam olarak istenendir: bir çalışan bir satırı kilitler, diğer çalışanlar o **kilitli satırı atlayıp** bir sonraki uygun satırı alır. Böylece hem aynı iş iki kez işlenmez hem de hiç kimse beklemez; on çalışan on farklı işi paralel yürütür. `PESSIMISTIC_READ` paylaşımlı kilittir — hepsi aynı satırı okuyabilir, dolayısıyla **çift işleme** kapıyı açar (A). `NOWAIT` beklemeyi gerçekten ortadan kaldırır ama kilitli satırı **atlamaz**, hata fırlatır; çalışanlar iş yerine hata toplar (C). `@Version` ile iyimser kilitte on çalışan da aynı ilk satırı seçer, biri kazanır, dokuzu çakışma istisnası alıp yeniden dener; çekişme yüksek olduğu için bu yaklaşım sürekli boşa iş üretir (D) — iyimser kilidin zayıf olduğu senaryo tam da budur.

# kilit-4 | İleri
## Soru
Limit düşümünü `@Modifying @Query("update Kredi k set k.kullanilan = k.kullanilan + :tutar where k.id = :id and k.kullanilan + :tutar <= k.limit")` biçiminde yazdın ve dönen etkilenen satır sayısı 0 ise isteği reddediyorsun. Bu yaklaşımla ilgili hangisi doğrudur?
## A
Yanlış bir yaklaşımdır; JPQL toplu güncelleme cümleleri satır bazında atomik çalışmadığı için eşzamanlılık kontrolü mutlaka bir `@Version` sütunuyla yapılmalıdır.
🤖 tek satırlık koşullu UPDATE veritabanında atomiktir, ek bir sürüm sütunu şart değildir.
## B
Doğru ama gereksizdir; `@Transactional` zaten metodun tamamını yalıttığı için koşulu `where` içine koymanın çakışmaya karşı ek bir katkısı olmaz.
🤖 @Transactional tek başına "oku–karar ver–yaz" boşluğunu kapatmaz.
## C
Doğrudur; ayrıca bu sorgu kalıcı bağlamı (persistence context) da güncellediği için bellekteki `Kredi` nesnesi işlemin geri kalanında taze kalır.
🤖 toplu güncelleme sorgusu kalıcı bağlamı atlar, bellekteki nesne bayat kalır.
## D
Doğrudur; kontrol ile yazma tek cümlede olduğu için çakışma penceresi hiç açılmaz, ancak sorgu kalıcı bağlamı atladığından bellekteki varlık bayatlar.
🤖 Doğru: atomiklik sağlanır ama bağlamın bayatlaması ayrıca ele alınmalıdır.
## Doğru
D
## Açıklama
Tek satır üzerinde koşullu `UPDATE`, veritabanında **atomiktir**: satır kilitlenir, koşul değerlendirilir ve yazma yapılır; okuma ile yazma arasında başkasının araya gireceği bir boşluk **hiç açılmaz**. Bu yüzden ne kilit ne sürüm sütunu ne de yeniden deneme gerekir (A yanlış) ve etkilenen satır sayısı 0 ise koşul sağlanmamış demektir — limit yetmemiştir. `@Transactional` bu garantiyi tek başına vermez (B): bir işlemin varlığı, "önce oku, sonra karar ver, sonra yaz" biçiminde yazılmış kodun `READ COMMITTED` altında kayıp güncelleme üretmesini engellemez; garantiyi veren şey koşulun `WHERE`e taşınmış olmasıdır. Yaklaşımın gerçek bedeli C'nin tersidir: JPQL toplu güncellemeleri **kalıcı bağlamı atlar**, bellekteki `Kredi` nesnesi eski değeri taşımaya devam eder. Çözüm `@Modifying(clearAutomatically = true, flushAutomatically = true)`: `flushAutomatically` bekleyen değişiklikleri sorgudan önce yazar, `clearAutomatically` sorgudan sonra bağlamı temizler; ancak temizleme bağlamdaki **tüm** varlıkları yönetimden çıkardığı için metodun geri kalanında bunu hesaba katmak gerekir.

# kilit-5 | İleri
## Soru
İki eşzamanlı virman işlemi var: biri Hesap-1'den Hesap-2'ye, diğeri Hesap-2'den Hesap-1'e para gönderiyor ve her biri önce gönderen hesabı kilitliyor. Ortaya çıkan kilitlenme (deadlock) hakkında hangisi doğrudur?
## A
Kilitlenme bir izolasyon seviyesi sorunudur; seviyeyi `SERIALIZABLE`'a çıkarmak döngüyü tamamen ortadan kaldırır ve uygulamada yeniden denemeye gerek bırakmaz.
🤖 izolasyonu yükseltmek kilitlenmeyi bitirmez, aksine kilit çekişmesini artırabilir.
## B
Veritabanı bu duruma müdahale etmez; iki işlem de birbirini sonsuza kadar bekler ve tek çözüm uygulama tarafında bir bekleme zaman aşımı tanımlamaktır.
🤖 modern veritabanları kilitlenme döngüsünü tespit edip bir işlemi geri alır.
## C
Veritabanı döngüyü tespit edip bir kurbanı geri alır; uygulama bu hatayı yakalayıp yeniden denemeli, kalıcı çözüm ise satırları hep aynı sırada kilitlemektir.
🤖 Doğru: kurban seçimi veritabanının, yeniden deneme ve sıra disiplini uygulamanın işidir.
## D
Kilitlenme yalnız kötümser kilitlemede görülebilen bir durumdur; `@Version` ile iyimser kilit kullanan sistemlerde teorik olarak imkânsızdır.
🤖 iyimser kilitte de yazma sırasında satır kilitleri alınır, döngü yine kurulabilir.
## Doğru
C
## Açıklama
Kilitlenme, iki işlemin karşılıklı olarak birbirinin tuttuğu kilidi beklemesidir. Modern veritabanları bu döngüyü **tespit eder**, işlemlerden birini **kurban** (victim) seçip geri alır ve ona bir hata döndürür (`ORA-00060`, `40P01`, `Deadlock found ...`). Yani sistem sonsuza kadar kilitli kalmaz (B yanlış), ama veritabanı yalnız düğümü çözer — **düşen işi yeniden denemek uygulamanın sorumluluğudur**. Kalıcı çözüm daha basittir: kaynakları **her yerde aynı sırada** kilitlemek. Virmanda gönderen/alan ayrımı yapmadan satırları daima hesap numarasına göre artan sırayla kilitlersen döngü kurulamaz. İzolasyon seviyesini yükseltmek bu sorunu çözmez (A); aksine daha çok ve daha uzun kilit tutulmasına yol açıp kilitlenme olasılığını artırabilir. İyimser kilit de bağışık değildir (D): sürüm karşılaştırması çakışmayı yakalasa bile `UPDATE` sırasında yine satır kilitleri alınır ve iki işlem satırlara ters sırayla dokunursa aynı döngü oluşur. Genel disiplin ise kilit süresini kısaltmaktır: kilit işlemin bitiminde bırakıldığı için işlem sınırı ne kadar genişse kilit o kadar uzun tutulur.

# kilit-6 | Uzman
## Soru
Kredi başvuru ekranında uzman formu açıp ortalama 10 dakika inceliyor, sonra kaydediyor. Ekip "başvuru satırını form açılırken `PESSIMISTIC_WRITE` ile kilitleyip kaydedene kadar tutalım" öneriyor. Bu öneri hakkında en doğru değerlendirme nedir?
## A
Yanlıştır; veritabanı kilidi ancak işlemin sonuna kadar yaşar, kullanıcı düşünürken işlem açık tutulamaz — sürüm numarasını forma taşıyıp kaydederken karşılaştıran çevrimdışı iyimser kilit kullanılır.
🤖 Doğru: uzun kullanıcı düşünme süresi boyunca işlem/kilit tutulmaz; doğru desen çevrimdışı iyimser kilittir.
## B
Doğrudur; bağlantı havuzu yeterince büyük tutulduğu sürece on dakika açık kalan bir işlemin ve kilidin bir maliyeti olmaz, karşılığında veri bütünlüğü kesin olarak garanti altına alınmış olur.
🤖 uzun süren işlem bağlantıyı ve kilidi tutar, çekişmeyi ve kilitlenme riskini büyütür.
## C
Yanlıştır; doğru çözüm satırı Redis tabanlı bir dağıtık kilitle kilitlemektir, çünkü tek bir veritabanı üzerinde bile dağıtık kilit veritabanı kilidinden daha güvenli bir doğruluk garantisi verir.
🤖 veri zaten veritabanındaysa veritabanı kilidi daha güvenlidir; Redis kilidi süre aşımı riski taşır.
## D
Doğru sayılmasa da zararsızdır; kötümser kilit işlem bitiminde değil oturum (session) kapanınca bırakıldığı için kullanıcı ekranı kapattığında kilit kendiliğinden serbest kalır.
🤖 kilit oturuma değil işleme bağlıdır ve işlem bitince bırakılır.
## Doğru
A
## Açıklama
Kötümser kilit, alındığı **işlemin bitişine** kadar yaşar (D yanlış — kilidin ömrü ekrana ya da oturuma değil işleme bağlıdır). Bu yüzden kilidi kullanıcı formu doldururken tutmak, on dakika boyunca bir veritabanı işlemini ve onunla birlikte bir havuz bağlantısını açık tutmak demektir; bağlantı havuzunu büyütmek bunu çözmez (B) — çekişme, kilit bekleyen istekler ve kilitlenme riski birlikte büyür, bir de kullanıcı sekmeyi kapatırsa kilit zaman aşımına kadar asılı kalır. Bu senaryonun doğru deseni **çevrimdışı iyimser kilittir** (offline optimistic lock): satır kısa bir işlemde okunur, **sürüm numarası** forma gizli alan olarak taşınır, kaydetme isteğinde geri gönderilir ve yine kısa bir işlemde `WHERE id = ? AND surum = ?` koşuluyla yazılır. Sürüm değişmişse 0 satır etkilenir ve kullanıcıya "bu kayıt siz incelerken değişti" denir. Redis tabanlı dağıtık kilit de burada doğru araç değildir (C): korunacak şey zaten veritabanındaki bir satırdır ve veritabanı kilidi veriyle **aynı işlemin içinde** yaşar; Redis kilidi ayrı bir sistemde durduğu için süre aşımı ve saat kayması yüzünden iki düğüm kendini sahip sanabilir ve doğruluk için fencing token gibi ek koruma ister (129 ve 160 numaralı konular). Dağıtık kilit, veritabanı dışı bir kaynağı korumak gerektiğinde anlamlıdır.
