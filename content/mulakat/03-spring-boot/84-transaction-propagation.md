---
slug: transaction-propagation
baslik: "İşlem Yayılımı ve İzolasyon: REQUIRED/REQUIRES_NEW/NESTED ve Okuma Anomalileri"
kademe: İleri
sira: 84
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Restoranda arkadaşınla oturuyorsun. Masaya yeni biri geldiğinde iki seçenek var: ya sizin adisyonunuza yazılır, ya kendi ayrı hesabını açar. Aynı adisyondaysanız biri hesabı iptal ettirince herkesinki gider; ayrı hesaptaysa onunki durur. `@Transactional` bir metot, `@Transactional` başka bir metodu çağırdığında da aynı soru sorulur: iç metot dış işleme **katılsın** mı, yoksa **ayrı** bir işlem mi açsın? Cevabı **yayılım** (propagation) ayarı verir. İkinci soru da şudur: aynı anda çalışan işlemler birbirinin yarım kalmış verisini ne kadar **görsün**? Onu da **izolasyon** (isolation) belirler.

### Temel kavramlar

- **İşlem (transaction)**: Ya tamamı başarılı olan (commit) ya da tamamı iptal edilen (rollback) veritabanı iş paketi.
- **Yayılım (propagation)**: İç metodun mevcut işlemle ilişkisini belirleyen kural.
- **Askıya alma (suspend)**: Dış işlemi geçici olarak bir kenara koymak, ayrı bir işlem çalıştırmak, sonra dış işleme dönmek.
- **Kayıt noktası (savepoint)**: İşlemin içine konan ara işaret. Hata olunca tüm işlem yerine yalnız o işarete kadar geri sarılır.
- **Rollback-only işareti**: "Bu işlem artık commit edilemez, sonu kesin geri alma" damgası.
- **İzolasyon (isolation)**: Eşzamanlı işlemlerin birbirini ne kadar gördüğünü belirleyen düzey.
- **Kirli okuma (dirty read)**: Başka bir işlemin henüz **commit etmediği** veriyi okumak; o işlem geri alınırsa hiç var olmamış veriyi görmüş olursun.
- **Tekrarlanamayan okuma (non-repeatable read)**: Aynı satırı bir işlem içinde iki kez okuyunca, arada başkası güncelleyip commit ettiği için **farklı değer** görmek.
- **Hayalet okuma (phantom read)**: Aynı koşullu sorguyu (`WHERE x > 5`) iki kez çalıştırınca, arada eklenen satırlar yüzünden ikinci sonuçta **yeni satırların belirmesi**.

İzolasyon `READ_UNCOMMITTED → READ_COMMITTED → REPEATABLE_READ → SERIALIZABLE` doğrultusunda yükseldikçe bu üç anomali sırasıyla engellenir. Bedeli şudur: kilitleme ve sıraya sokma arttığı için **aynı anda çalışabilen iş sayısı (concurrency) düşer**.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İzolasyon arttıkça anomaliler engellenir, eşzamanlılık düşer">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İzolasyon ↑ → anomali ↓, eşzamanlılık ↓</text>
  <rect x="20" y="44" width="145" height="120" rx="9" fill="#FBECEC" stroke="#B4442E" stroke-width="1.3"/>
  <text x="92" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#B4442E" font-family="system-ui">READ_UNCOMMITTED</text>
  <text x="92" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dirty read: VAR</text>
  <text x="92" y="110" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">en yüksek eşzamanlılık</text>
  <rect x="173" y="44" width="145" height="120" rx="9" fill="#FBF4E6" stroke="#B67A1A" stroke-width="1.3"/>
  <text x="245" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#B67A1A" font-family="system-ui">READ_COMMITTED</text>
  <text x="245" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dirty read: yok</text>
  <text x="245" y="110" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">non-repeat: olabilir</text>
  <rect x="326" y="44" width="145" height="120" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="398" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">REPEATABLE_READ</text>
  <text x="398" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">non-repeat: yok</text>
  <text x="398" y="110" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">phantom: olabilir</text>
  <rect x="479" y="44" width="141" height="120" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="549" y="66" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">SERIALIZABLE</text>
  <text x="549" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">phantom: yok</text>
  <text x="549" y="110" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">en düşük eşzamanlılık</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">DB varsayılanı farklıdır: PostgreSQL/Oracle READ_COMMITTED, MySQL/InnoDB REPEATABLE_READ</text>
</svg>
```

Varsayılan `Isolation.DEFAULT` bir düzey **dayatmaz**; izolasyonu **veritabanının kendi varsayılanına** bırakır. Sorun şu ki her motorun varsayılanı farklıdır: PostgreSQL ve Oracle `READ_COMMITTED`, MySQL/InnoDB `REPEATABLE_READ` kullanır. Aynı kod bu yüzden iki ortamda farklı anomali davranışı gösterebilir — testlerde sessizce geçip üretimde bozulabilir. İzolasyon uygulama kodunda değil **veritabanı motorunda** uygulanır; taşınabilir davranış istiyorsan açıkça yaz: `@Transactional(isolation = Isolation.READ_COMMITTED)`.

### Nasıl çalışır?

Yayılımın en kritik üç değeri şunlardır:

- **REQUIRED** (varsayılan): Mevcut bir işlem varsa **ona katıl**, yoksa yeni aç. İç içe metotlar tek mantıksal işlemi paylaşır; biri düşerse hepsi birlikte geri alınır.
- **REQUIRES_NEW**: Mevcut işlemi **askıya al**, tamamen **bağımsız** yeni bir işlem aç, onu ayrıca commit/rollback et.
- **NESTED**: Mevcut işlemin **içinde** bir **savepoint** kur; iç kısım düşerse yalnız oraya kadar geri sarılır, dış işlem devam edebilir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REQUIRED katılır, REQUIRES_NEW askıya alıp bağımsız açar, NESTED savepoint kurar">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dış işlem varken iç metot ne yapar?</text>
  <rect x="20" y="42" width="196" height="130" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="118" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">REQUIRED</text>
  <text x="118" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dış işleme KATILIR</text>
  <text x="118" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek mantıksal işlem</text>
  <text x="118" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">biri düşerse hepsi</text>
  <text x="118" y="144" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">birlikte geri alınır</text>
  <text x="118" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(varsayılan)</text>
  <rect x="222" y="42" width="196" height="130" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">REQUIRES_NEW</text>
  <text x="320" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dışı ASKIYA ALIR</text>
  <text x="320" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bağımsız işlem açar</text>
  <text x="320" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ayrı commit/rollback</text>
  <text x="320" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ayrı bağlantı kullanır</text>
  <text x="320" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(audit log)</text>
  <rect x="424" y="42" width="196" height="130" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="522" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">NESTED</text>
  <text x="522" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SAVEPOINT kurar</text>
  <text x="522" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynı işlem içinde</text>
  <text x="522" y="128" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iç düşerse savepoint'e</text>
  <text x="522" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dış düşerse iç de gider</text>
  <text x="522" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(kısmi geri alma)</text>
</svg>
```

Kalan dört seçenek daha seyrek kullanılır: **SUPPORTS** (işlem varsa katıl, yoksa işlemsiz çalış), **MANDATORY** (işlem yoksa hata fırlat), **NEVER** (işlem varsa hata fırlat), **NOT_SUPPORTED** (işlem varsa askıya al, işlemsiz çalış).

`REQUIRES_NEW`'in klasik kullanım yeri **denetim kaydıdır** (audit log): ana iş başarısız olsa bile "şu kullanıcı şunu denedi" kaydının kalması gerekir. Bağımsız iç işlem, dış işlemi beklemeden metot biter bitmez kendi commit'ini yapar; dış işlem sonradan geri alınsa da kayıt yerinde durur.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REQUIRES_NEW ile yazılan audit log dış işlem geri alınsa da kalıcı olur">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">REQUIRES_NEW: dış rollback iç commit'i bozmaz</text>
  <rect x="30" y="42" width="580" height="70" rx="10" fill="#FBECEC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="55" y="66" text-anchor="start" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">DIŞ işlem — siparis() @Transactional (REQUIRED)</text>
  <text x="55" y="88" text-anchor="start" font-size="10.5" fill="#1C3247" font-family="system-ui">... sonradan istisna fırlar → ROLLBACK</text>
  <text x="55" y="104" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">sipariş değişiklikleri geri alınır</text>
  <rect x="200" y="126" width="240" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İÇ: auditLog() REQUIRES_NEW</text>
  <text x="320" y="170" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">askıya alır → ayrı commit</text>
  <text x="320" y="186" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">KAYIT KALICI ✓</text>
  <path d="M320 112 L320 126" stroke="#5D7789" stroke-width="1.5" marker-end="url(#txprop-ok)"/>
  <text x="452" y="150" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">dış rollback</text>
  <text x="452" y="164" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">iç kaydı</text>
  <text x="452" y="178" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">etkilemez</text>
  <defs>
    <marker id="txprop-ok" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
      <path d="M0 0 L6 3 L0 6 Z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

Bunun bir bedeli vardır: bağımsız işlem **ayrı bir veritabanı bağlantısı** kullanır. İç içe çok sayıda `REQUIRES_NEW`, bağlantı havuzunu tüketip kilitlenmeye (deadlock) yol açabilir.

`NESTED` ise ayrı bağlantı açmaz, aynı fiziksel işlem içinde savepoint kullanır. Bu yüzden iç kısım başarıyla bitse bile **dış işlem geri alınırsa iç değişiklikler de gider** — `REQUIRES_NEW`'den temel farkı budur. Ayrıca sürücü/veritabanı tarafında **savepoint desteği** ister; her ortamda bulunmaz ve varsayılan yayılım değildir.

### Mülakatta ne sorulur?

- **"Varsayılan yayılım nedir?"** → `REQUIRED`: varsa katıl, yoksa aç. `readOnly` ile ilgisi yoktur. Anahtar sonuç: iç metottaki hata, aynı işleme katıldığı için dış metodu da geri aldırır.
- **"REQUIRES_NEW vs NESTED?"** → `REQUIRES_NEW` dışı askıya alır, **ayrı bağlantıda bağımsız** işlem açar, dış rollback onu bozmaz. `NESTED` bağımsız değildir; aynı işlem içinde **savepoint**'tir, dış rollback onu da götürür. İkisi aynı şey değildir.
- **"`UnexpectedRollbackException` neden fırlar?"** → `REQUIRED` ile iç metot dış işleme **katılır**. İç metotta oluşan istisna işlemi "yalnız geri alınabilir" (rollback-only) diye işaretlemiş olabilir. Dış metot bu istisnayı yakalayıp **yutabilir** ve normal bitebilir. O zaman commit denemesi başarısız olur ve Spring `UnexpectedRollbackException` fırlatır. Özeti şudur: "her şey yolunda sandın, oysa işlem çoktan geri almaya damgalanmıştı." İç adımı gerçekten yutulabilir kılmak için onu `REQUIRES_NEW` ya da `NESTED` ile ayırman gerekir.
- **"Üç okuma anomalisi nedir?"** → Kirli okuma: commit edilmemiş veriyi okumak. Tekrarlanamayan okuma: aynı satırda **farklı değer** görmek. Hayalet okuma: **yeni satırların** belirmesi. `READ_UNCOMMITTED` hiçbirini engellemez ve en yüksek eşzamanlılığı verir; `SERIALIZABLE` en katıdır, hiçbirine izin vermez, eşzamanlılığı en düşüktür.
- **"Aynı kod PostgreSQL ve MySQL'de neden farklı davranır?"** → `Isolation.DEFAULT` düzeyi veritabanına devreder; PostgreSQL/Oracle `READ_COMMITTED`, MySQL/InnoDB `REPEATABLE_READ`. Spring `DEFAULT`'u `SERIALIZABLE`'a yükseltmez.
- **"Üç mikroservis, üç ayrı veritabanı — nasıl atomik yaparsın?"** → Yapamazsın: `@Transactional`'ın sınırı **tek bir veritabanıdır**. Klasik çözüm **iki fazlı commit** (2PC) tüm katılımcıları bir koordinatör altında kilitler; ölçeklenmez ve kırılgandır, mikroservislerde genelde kaçınılır. Standart yaklaşım **adım adım telafili işlem zinciridir** (Saga deseni). Akış bir dizi **yerel işleme** bölünür ve her adım kendi veritabanında commit eder. Bir adım düşerse önceki adımlar **telafi (compensating) işlemleriyle** geri alınır: "ödemeyi al" adımının telafisi "ödemeyi iade et"tir. Bu bir veritabanı rollback'i değil, **semantik** (iş düzeyinde) geri sarmadır; ayrıca Saga, 2PC'nin başka bir adı değildir. Uygulaması koreografiyle (olay tabanlı) veya orkestrasyonla (merkezi yönetici) yapılır. Ayrı veritabanları birbirini kendiliğinden geri almaz; telafi mantığını sen kurarsın.

### Tek cümlede

Yayılım iç metodun dış işleme katılıp katılmayacağını, izolasyon ise eşzamanlı işlemlerin birbirinin yarım verisini ne kadar göreceğini belirler — ikisinin de varsayılanı (`REQUIRED` ve veritabanına devredilen `DEFAULT`) sessiz sürprizler üretebilir.

# txprop-1 | Temel
## Soru
`@Transactional`'ın varsayılan yayılımı (propagation) `REQUIRED` ne yapar?
## A
Her çağrıda mutlaka yeni bir işlem açar ve mevcut işlemi görmezden gelir.
🤖 O davranış REQUIRES_NEW'e aittir; REQUIRED mevcut işleme katılır.
## B
Mevcut bir işlem (transaction) varsa ona katılır; yoksa yeni bir işlem açar.
🤖 Doğru: REQUIRED "varsa katıl, yoksa aç" davranışıdır.
## C
Yalnız `readOnly = true` işaretli işlemlerde geçerlidir; yazma işlemlerinde yok sayılır.
🤖 REQUIRED yayılımın readOnly ile ilgisi yoktur; her tür işlemde geçerlidir.
## D
Mevcut işlemi askıya alıp bağımsız yeni bir işlem açar, sonra eskisine döner.
🤖 O da REQUIRES_NEW davranışıdır; REQUIRED askıya almaz, katılır.
## Doğru
B
## Açıklama
`REQUIRED` Spring'in varsayılan yayılım (propagation) düzeyidir ve mantığı basittir: **"Bir işlem zaten çalışıyorsa ona katıl; yoksa yeni bir işlem başlat."** Böylece iç içe çağrılan `@Transactional` metotlar tek bir mantıksal işlemi paylaşır — biri başarısız olursa tümü birlikte geri alınır (atomiklik). Bu, çoğu iş mantığı için istenen davranıştır. "Her zaman yeni açmak" (A) ve "askıya alıp bağımsız açmak" (D) `REQUIRES_NEW`'in davranışıdır; `readOnly` ile bir ilgisi yoktur (C). REQUIRED'ın anahtar sonucu: iç metottaki bir hata, dış metodun işlemini de geri aldırır (aynı işleme katıldığı için).

# txprop-2 | Orta
## Soru
Bir sipariş işleme metodu `REQUIRED` işlem içindeyken, denetim kaydını (audit log) `REQUIRES_NEW` bir metotla yazıyorsun. Sipariş metodu daha sonra bir istisna fırlatıp geri alınırsa denetim kaydına ne olur?
## A
Denetim kaydı da geri alınır; `REQUIRES_NEW` aslında dış işleme katıldığı için tüm değişiklikler tek blok halinde birlikte gider.
🤖 REQUIRES_NEW dış işleme katılmaz; bağımsız commit eder.
## B
Denetim kaydı hiç yazılmaz; `REQUIRES_NEW` iç işlemin commit'ini dış işlem bitene kadar bekletir, dış işlem düşünce yazma iptal olur.
🤖 Bağımsız iç işlem dışı beklemez; metot biter bitmez kendi commit'ini yapar.
## C
Denetim kaydı KALICI olur; `REQUIRES_NEW` bağımsız bir işlemde ayrı commit ettiğinden dış rollback onu etkilemez.
🤖 Doğru: bağımsız iç işlem ayrı commit eder; dış rollback onu bozmaz.
## D
Denetim kaydı belirsiz kalır; `REQUIRES_NEW` iç ve dış işlemi aynı bağlantıda çalıştırdığından sonucu veritabanı ve sürücü belirler.
🤖 REQUIRES_NEW ayrı bir bağlantı/işlem kullanır; belirsizlik yoktur.
## Doğru
C
## Açıklama
`REQUIRES_NEW`, mevcut (dış) işlemi **askıya alır** (suspend) ve tamamen **bağımsız** yeni bir işlem açar; bu iç işlem kendi başına **commit** olur ve dış işlemin sonucundan etkilenmez. Bu yüzden denetim kaydı, sipariş işlemi sonradan geri alınsa (rollback) bile veritabanında **kalıcı** kalır. Bu tam olarak `REQUIRES_NEW`'in kullanım amacıdır: "ana iş başarısız olsa bile mutlaka kalması gereken" yan kayıtlar (audit, deneme sayacı, bildirim kaydı). Dikkat: bağımsız işlem **ayrı bir bağlantı** (connection) kullanır — iç içe çok sayıda `REQUIRES_NEW` bağlantı havuzunu tüketip kilitlenmeye (deadlock) yol açabilir. "Dış işleme katılır" (A), "commit dış işlemi bekler" (B) veya "aynı bağlantıyı paylaşır" (D) yanlıştır.

# txprop-3 | Orta
## Soru
`Propagation.NESTED` yayılımı `REQUIRES_NEW`'den nasıl farklıdır?
## A
`NESTED` mevcut işlemin içinde bir kayıt noktası (savepoint) kurar; dış işlem geri alınırsa iç kısım da gider.
🤖 Doğru: NESTED savepoint tabanlıdır ve dış işleme bağımlı kalır; REQUIRES_NEW bağımsızdır.
## B
`NESTED` ile `REQUIRES_NEW` tamamen aynıdır; ikisi de dış işlemi askıya alıp bağımsız bir işlem açar ve kendi başına commit eder.
🤖 NESTED bağımsız değildir; dış işleme bağlı bir savepoint'tir.
## C
`NESTED` her çağrıda ayrı bir veritabanı bağlantısı açar; bu yüzden dış işlemden tamamen izoledir ve dış rollback onu etkilemez.
🤖 O REQUIRES_NEW davranışı; NESTED aynı bağlantı/işlem içinde savepoint kullanır.
## D
`NESTED` bütün veritabanı ve sürücülerde desteklenir; ayrıca JPA kullanan projelerde varsayılan yayılım düzeyi odur.
🤖 NESTED savepoint desteği ister ve varsayılan değildir; varsayılan REQUIRED'dır.
## Doğru
A
## Açıklama
`NESTED`, `REQUIRES_NEW`'in aksine **bağımsız bir işlem açmaz**; mevcut işlemin **içinde** bir **kayıt noktası** (savepoint) oluşturur. İç kısım başarısız olursa yalnız o savepoint'e kadar geri alınır ve dış işlem devam edebilir (kısmi geri alma). Ancak iç kısım başarılı olsa bile, **dış işlem geri alınırsa** iç kısımdaki değişiklikler de onunla birlikte gider — çünkü aynı fiziksel işleme bağlıdır. Bu, `REQUIRES_NEW`'in "dış ne olursa olsun kalıcı" davranışından temel farktır. `NESTED` **savepoint** desteği gerektirir (bazı ortam/sürücülerde yoktur); ayrı bağlantı açmaz (C), REQUIRES_NEW ile aynı değildir (B) ve varsayılan yayılım REQUIRED'dır (D).

# txprop-4 | İleri
## Soru
İzolasyon düzeyleriyle önlenen üç okuma anomalisi (read phenomena) doğru tanımlanmış hangi seçenektir?
## A
`READ_UNCOMMITTED` hayalet okumayı (phantom read) engeller ama kirli okumaya (dirty read) izin verir; bu yüzden ikinci en katı düzey sayılır.
🤖 En düşük düzey hiçbirini engellemez; kirli okumaya bile izin verir.
## B
`SERIALIZABLE` en gevşek düzeydir; kirli okumaya izin verir ve karşılığında en yüksek eşzamanlılığı (concurrency) sunar.
🤖 SERIALIZABLE en katı düzeydir; hiçbir anomaliye izin vermez, eşzamanlılığı en düşüktür.
## C
Tekrarlanamayan okuma (non-repeatable read), aynı sorgunun iki kez çalıştırılıp farklı SATIR SAYISI döndürmesi, yani araya yeni satırların girmesidir.
🤖 O tanım hayalet okumaya (phantom) aittir; non-repeatable read aynı satırın değerinin değişmesidir.
## D
Kirli okuma: commit edilmemiş veriyi okumak. Tekrarlanamayan okuma: aynı satırda farklı DEĞER görmek. Hayalet okuma: yeni SATIRLARIN belirmesi.
🤖 Doğru: üç anomali ve izolasyonla ilişkisi tam olarak budur.
## Doğru
D
## Açıklama
Üç okuma anomalisi ve tanımları: (1) **Kirli okuma** (dirty read) — başka bir işlemin henüz commit **etmediği** veriyi okumak; o işlem geri alınırsa "olmayan" veriyi görmüş olursun. (2) **Tekrarlanamayan okuma** (non-repeatable read) — aynı satırı bir işlem içinde iki kez okuyunca, arada başka işlem güncelleyip commit ettiği için **farklı değer** görmek. (3) **Hayalet okuma** (phantom read) — aynı koşullu sorguyu (`WHERE x > 5`) iki kez çalıştırınca, arada eklenen satırlar yüzünden ikinci sonuçta **yeni satırların belirmesi**. İzolasyon `READ_UNCOMMITTED`'tan `SERIALIZABLE`'a çıktıkça bu üçü sırasıyla engellenir; ama kilitleme/sıralama arttığı için **eşzamanlılık düşer**. C, phantom ile non-repeatable'ı karıştırır; A ve B düzeylerin yönünü ters çevirir.

# txprop-5 | İleri
## Soru
Kodunda izolasyon belirtmeden `@Transactional` kullandın (yani `Isolation.DEFAULT`). Aynı kodu PostgreSQL'de ve MySQL/InnoDB'de çalıştırınca davranış neden farklı olabilir?
## A
Çünkü `DEFAULT` izolasyonu veritabanına bırakır; PostgreSQL `READ_COMMITTED`, MySQL/InnoDB `REPEATABLE_READ` kullanır.
🤖 Doğru: DEFAULT, DB varsayılanına devreder ve bu varsayılan ortama göre değişir.
## B
Çünkü tüm veritabanları varsayılan olarak `SERIALIZABLE` çalışır; aradaki fark yalnız performanstadır, görülen veri her ortamda aynı olur.
🤖 Veritabanları SERIALIZABLE'ı varsayılan yapmaz; varsayılanlar da farklıdır.
## C
Çünkü Spring, `Isolation.DEFAULT`'u her zaman `SERIALIZABLE`'a yükseltir; fark, JDBC sürücülerinin bu isteği yok saymasından doğar.
🤖 DEFAULT, DB'nin varsayılanına devreder; SERIALIZABLE'a zorlamaz.
## D
Çünkü izolasyon yalnız uygulama kodunda anlamlıdır; veritabanı motorunun görülen veriye hiçbir etkisi yoktur, fark rastlantısaldır.
🤖 İzolasyon veritabanı motorunda uygulanır; DB'nin doğrudan etkisi vardır.
## Doğru
A
## Açıklama
`Isolation.DEFAULT`, "izolasyonu **veritabanının kendi varsayılanına** bırak" demektir — Spring bir düzey dayatmaz. Sorun şu: her motorun varsayılanı farklıdır. **PostgreSQL** ve **Oracle** varsayılan `READ_COMMITTED`; **MySQL/InnoDB** varsayılan `REPEATABLE_READ`. Aynı kod, `REPEATABLE_READ`'de tekrarlanamayan okumaları engelleyip `READ_COMMITTED`'de engellememesi gibi **farklı anomali davranışları** gösterebilir — üstelik testlerde sessizce geçip üretimde bozulabilir. Taşınabilir ve öngörülebilir davranış için, gerekli olduğunda izolasyonu **açıkça** belirt: `@Transactional(isolation = Isolation.READ_COMMITTED)`. İzolasyon veritabanı motorunda uygulanır (D yanlış); DEFAULT SERIALIZABLE'a zorlamaz (B/C yanlış).

# txprop-6 | İleri
## Soru
Sipariş, ödeme ve stok üç ayrı mikroservistedir ve her biri kendi veritabanına sahiptir. Bir "sipariş oluştur" akışını tek bir `@Transactional` ile atomik yapamıyorsun. Standart yaklaşım nedir?
## A
Üç servisi tek bir `@Transactional` metotta çağırmak; Spring dağıtık işlemi arka planda otomatik yönetir ve ACID garantisi böylece korunur.
🤖 Ayrı veritabanlarına tek yerel @Transactional ACID veremez; dağıtık işlem böyle çalışmaz.
## B
İki fazlı commit (2PC) ile üç servisi küresel bir kilit altında toplamak; Saga zaten 2PC'nin başka bir adıdır ve aynı işi yapar.
🤖 Saga 2PC değildir; aksine dağıtık kilitlerden kaçınmak için telafi kullanır.
## C
Saga deseni: her adım kendi yerel işlemini commit eder, başarısızlıkta önceki adımlar telafi (compensating) işlemleriyle geri alınır.
🤖 Doğru: Saga, yerel işlemler + başarısızlıkta telafi eylemleri zinciridir.
## D
Hiçbir şey yapmaya gerek yoktur; bir adım başarısız olursa veritabanları birbirini haberdar edip tüm servisleri otomatik geri alır.
🤖 Ayrı veritabanları birbirini otomatik geri almaz; telafi mantığını sen kurarsın.
## Doğru
C
## Açıklama
Ayrı veritabanlarına sahip mikroservislerde tek bir yerel `@Transactional` **ACID atomiklik** sağlayamaz (A) — işlem sınırı tek bir veritabanı/bağlantı ile sınırlıdır. Klasik dağıtık işlem çözümü **iki fazlı commit** (2PC) tüm katılımcıları koordinatör altında kilitler; ölçeklenmez, kırılgandır ve mikroservislerde genelde kaçınılır (B — Saga 2PC değildir). Modern standart **Saga** desenidir: iş akışı bir dizi **yerel işleme** bölünür; her adım kendi veritabanında commit eder. Bir adım başarısız olursa, daha önce commit edilmiş adımlar **telafi (compensating) işlemleriyle** geri alınır — örneğin "ödemeyi al" adımının telafisi "ödemeyi iade et"tir. Bu **semantik** (iş düzeyinde) bir geri sarmadır, veritabanı rollback'i değil (D). Saga koreografiyle (olay tabanlı) veya orkestrasyonla (merkezi yönetici) uygulanır.
