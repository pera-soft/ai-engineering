---
slug: saga-pattern
baslik: "Saga Deseni: Koreografi/Orkestrasyon, Telafi İşlemleri ve Neden 2PC Değil"
kademe: Uzman
sira: 126
etiketler: 10
---

## Konu anlatımı

### Bu ne işe yarar?

Tek veritabanıyla çalışırken hayat kolaydır. "Parayı düş" ve "siparişi yaz" adımlarını tek bir **işlem paketi (transaction)** içine koyarsın. İşlem paketi, "ya hepsi olur ya hiçbiri" garantisi verir; iş ortada yarım kalmaz. Bir adım patlarsa veritabanı yapılan her şeyi geri sarar. Buna **geri alma (rollback)** denir. Paket sorunsuz kapanır ve yazılanlar kalıcı olursa buna da **kalıcı yazma (commit)** denir.

Mikroserviste bu paket kurulamaz. Sebebi şu: her servisin **kendi** veritabanı vardır. Sipariş bir yerde, ödeme başka yerde, stok bambaşka yerde durur. Sipariş servisi ödeme veritabanını geri saramaz; oraya erişimi bile yoktur.

**Saga** — Türkçesiyle **adım adım telafili işlem zinciri** — tam bu boşluğu doldurur. Dağıtık iş akışını tek bir dev pakete sokmaya çalışmaz. Onun yerine akışı bir zincir olarak kurar: her halka, kendi veritabanında ayrı ayrı kalıcı yazılan bir **yerel işlem paketidir**. Bir adım patlarsa daha önce kapanmış adımlar **telafi işlemleriyle** geri alınır.

### Temel kavramlar

- **Yerel işlem paketi (local transaction):** tek bir servisin kendi veritabanında yaptığı, ya hep ya hiç çalışan işlem. Saga'nın her adımı budur.
- **Telafi işlemi (compensating transaction):** kalıcı yazılmış bir adımın etkisini **anlamca tersine çeviren yeni bir iş işlemidir**. Veritabanının geri alma (rollback) mekanizması değildir. "Ödemeyi iade et", "rezervasyonu bırak", "siparişi iptal işaretle" gibi ileriye doğru çalışan gerçek işlemlerdir. Yani silgi değil, ters kayıt.
- **Nihai tutarlılık (eventual consistency):** akış sürerken sistem kısa süre tutarsız görünür, sonunda toparlanır.
- **Tekrar edilse de sonucu değişmeyen (idempotent):** bir işlemi bir kez ya da beş kez uygulamak **aynı** sonucu verir. Asansör düğmesine beş kez basmak asansörü beş kez çağırmaz.
- **Pivot adım:** telafisi mümkün olmayan, geri dönülemez adım (gönderilmiş bir e-posta geri alınamaz).

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Saga zinciri ve telafi işlemleri">
  <defs>
    <marker id="saga-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#25804C"/>
    </marker>
    <marker id="saga-comp" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#B0453B"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İleri yol commit, geri yol telafi</text>
  <rect x="24" y="48" width="120" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="84" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sipariş</text>
  <text x="84" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">oluştur ✓</text>
  <rect x="184" y="48" width="120" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="244" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Ödeme</text>
  <text x="244" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tahsil ✓</text>
  <rect x="344" y="48" width="120" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="404" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Stok</text>
  <text x="404" y="90" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">düş ✓</text>
  <rect x="504" y="48" width="112" height="52" rx="10" fill="#FBECEA" stroke="#B0453B" stroke-width="1.5"/>
  <text x="560" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B0453B" font-family="system-ui">Kargo</text>
  <text x="560" y="90" text-anchor="middle" font-size="10" fill="#B0453B" font-family="system-ui">HATA ✗</text>
  <line x1="144" y1="74" x2="180" y2="74" stroke="#25804C" stroke-width="2" marker-end="url(#saga-ok)"/>
  <line x1="304" y1="74" x2="340" y2="74" stroke="#25804C" stroke-width="2" marker-end="url(#saga-ok)"/>
  <line x1="464" y1="74" x2="500" y2="74" stroke="#25804C" stroke-width="2" marker-end="url(#saga-ok)"/>
  <text x="320" y="140" text-anchor="middle" font-size="12" font-weight="700" fill="#B0453B" font-family="system-ui">Kargo başarısız → telafiler geriye doğru çalışır</text>
  <line x1="404" y1="170" x2="244" y2="170" stroke="#B0453B" stroke-width="2" stroke-dasharray="5 3" marker-end="url(#saga-comp)"/>
  <line x1="244" y1="196" x2="84" y2="196" stroke="#B0453B" stroke-width="2" stroke-dasharray="5 3" marker-end="url(#saga-comp)"/>
  <text x="330" y="166" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">stok geri ekle</text>
  <text x="170" y="192" text-anchor="middle" font-size="10.5" fill="#B0453B" font-family="system-ui">ödeme iade + sipariş iptal</text>
</svg>
```

### Nasıl çalışır?

Zincir önce ileri yönde çalışır: sipariş oluşturuldu ✓ → ödeme tahsil edildi ✓ → stok düşüldü ✓ → kargo... hata. Artık ilk üç adım **kapanmış ve kalıcı yazılmış** işlem paketleridir. Onları veritabanına geri sardıramazsın. Bunun yerine saga geriye doğru telafileri tetikler: stok geri eklenir, ödeme iade edilir, sipariş iptal edilir.

Koordinasyon iki biçimde kurulabilir.

**Koreografi (choreography)** yönteminde merkez yoktur. Her servis kendisini ilgilendiren **olayı (event)** dinler, kendi adımını atar ve yeni bir olay yayınlar. Az adımlı akışlarda bu yaklaşım hafiftir ve fazladan altyapı istemez. Ama akış büyüdükçe "şu an neredeyiz, neden takıldık" sorusu cevapsız kalır. Ayrıca servisler birbirinin olayını tetikleye tetikleye döngüsel bağımlılık oluşturabilir.

**Orkestrasyon (orchestration)** yönteminde ise merkezi bir orkestratör vardır. Adımları sırayla açıkça çağırır, her adımın sonucunu izler ve gerekirse telafileri başlatır. Böylece akışın tamamı tek bir yerde, açık bir **durum makinesi** olarak görünür. Bedeli şudur: orkestratör yeni bir bağımlılıktır ve darboğaza dönüşebilir.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Saga ile iki fazlı commit karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Saga vs 2PC/XA</text>
  <rect x="30" y="44" width="270" height="178" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Saga</text>
  <text x="50" y="98" font-size="10.5" fill="#1C3247" font-family="system-ui">• her adım bağımsız commit</text>
  <text x="50" y="122" font-size="10.5" fill="#1C3247" font-family="system-ui">• kilit tutmaz → ölçeklenir</text>
  <text x="50" y="146" font-size="10.5" fill="#1C3247" font-family="system-ui">• hata → telafi işlemi</text>
  <text x="50" y="170" font-size="10.5" fill="#1C3247" font-family="system-ui">• koordinatör darboğazı yok</text>
  <text x="50" y="200" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">bedel: eventual consistency</text>
  <rect x="340" y="44" width="270" height="178" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="475" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">2PC / XA</text>
  <text x="360" y="98" font-size="10.5" fill="#1C3247" font-family="system-ui">• prepare→commit tek atomik</text>
  <text x="360" y="122" font-size="10.5" fill="#1C3247" font-family="system-ui">• kaynakları KİLİTLER → gecikme</text>
  <text x="360" y="146" font-size="10.5" fill="#1C3247" font-family="system-ui">• koordinatör çökerse in-doubt</text>
  <text x="360" y="170" font-size="10.5" fill="#1C3247" font-family="system-ui">• koordinatör = tek hata noktası</text>
  <text x="360" y="200" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">kazanç: strong consistency</text>
</svg>
```

Klasik alternatif **iki fazlı kalıcı yazma (two-phase commit, 2PC/XA)**'dır. Burada bir koordinatör iş yapar. Birinci fazda herkese "hazır mısın?" diye sorar (prepare). İkinci fazda, hepsi evet derse "kalıcı yaz" (commit) emrini verir.

Bu yöntem güçlü tutarlılık verir. Ama dört ağır bedeli vardır. Birincisi, katılımcı veritabanları "hazır mısın?" ile "kalıcı yaz" arasındaki tüm süre boyunca kaynakları **kilitli** tutar; bu da gecikme (latency) ve düşük iş hacmi (throughput) demektir. İkincisi, koordinatör tam o aralıkta çökerse katılımcılar ne kalıcı yazabilir ne geri alabilir; askıda (in-doubt) kalırlar. Üçüncüsü, koordinatörün kendisi **tek hata noktasıdır**; o düşerse akış durur. Dördüncüsü, farklı veritabanı ve mesaj teknolojileri arasında XA desteği kırılgandır.

Saga bunların hiçbirini kullanmaz. Kilit tutmaz, koordinatör onayı beklemez. Karşılığında akış sürerken araya kısa bir tutarsızlık penceresi girer. Yani seçim şudur: "güçlü tutarlılık + kilit + kırılganlık" mı, yoksa "kullanılabilirlik + gevşek bağlama + nihai tutarlılık" mı?

Saga bir "dağıtık transaction" değil, "telafi edilebilir adımlar zinciridir" ve üç direk üstünde durur:

1. **Tekrar edilse de sonucu değişmeyen adımlar.** Mesajlaşma neredeyse her zaman *en az bir kez* (at-least-once) teslim eder. Yani aynı adım iki kez tetiklenebilir. İkinci tetikleme hiçbir zarar vermemelidir.
2. **Tekrar edilse de sonucu değişmeyen telafiler.** Telafi de bir mesajla tetiklenir ve o mesaj da tekrar gelebilir. "İki kez iade et" komutu tek bir iade üretmelidir. Bunun yolu basittir: kayıt "iade edildi" diye işaretlenir, tekrar gelen istek atlanır.
3. **Kalıcı saga durumu.** Bir saga dakikalar, bazen günler sürer. Bu süre içinde orkestratör yeniden başlatılır, yeni sürümü yayına alınır (deploy) ya da çöker. Bu yüzden "nerede kaldık" bilgisi bir **saga günlüğünde (saga log)** veya durum tablosunda kalıcı olarak durmalıdır. Ancak o zaman orkestratör ayağa kalktığında kaldığı yerden devam eder. Bu bilgiyi yalnız bellekte tutmak, hiç saklamamak ya da istemciye emanet etmek akışı kaybettirir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sağlam saga tasarımının üç direği">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sağlam saga'nın üç direği</text>
  <rect x="30" y="52" width="185" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="122" y="80" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">İdempotent adım</text>
  <text x="122" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">retry güvenli</text>
  <text x="122" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">at-least-once altında</text>
  <text x="122" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tekrar zararsız</text>
  <rect x="227" y="52" width="185" height="120" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="319" y="80" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">İdempotent telafi</text>
  <text x="319" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">geri alma tekrarlanabilir</text>
  <text x="319" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iki kez iade =</text>
  <text x="319" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek iade</text>
  <rect x="424" y="52" width="186" height="120" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="517" y="80" text-anchor="middle" font-size="12.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Kalıcı durum</text>
  <text x="517" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">saga log / durum tablosu</text>
  <text x="517" y="130" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">orkestratör çökse de</text>
  <text x="517" y="148" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaldığı yerden devam</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Üçü birlikte: kısmi başarısızlığa dayanıklı iş akışı</text>
</svg>
```

Bir ek kural daha var. Akış öyle sıralanır ki, geri dönülemez **pivot adımdan sonraki** tüm adımlar **yeniden denenebilir (retriable)** olsun. Böylece pivot noktası geçildikten sonra akış ileri doğru güvenle yeniden denenebilir; geri dönmek gerekmez. "Tam olarak bir kez teslimat" (exactly-once delivery) arayışı bu tasarımın yerini tutmaz. Pratikte kullanılan reçete şudur: en az bir kez teslimat + tekrar edilse de sonucu değişmeyen işleme.

### Mülakatta ne sorulur?

- Commit edilmiş adımlar nasıl geri alınır? (Telafi işlemiyle; rollback değil.)
- Neden 2PC/XA yerine saga? Ödünleşme tam olarak nedir?
- Telafi ikinci kez tetiklenirse sistem nasıl doğru kalır?
- Orkestratörün "hangi adımdayız" bilgisi nerede durmalı?
- Koreografide at-least-once teslimata karşı ne yaparsın?

### Tek cümlede

Saga, kilit ve koordinatör yerine **telafi edilebilir yerel transaction'lar zinciri** kurar; bedeli nihai tutarlılıktır ve ancak idempotent adımlar, idempotent telafiler ve kalıcı saga durumuyla ayakta kalır.

# saga-1 | İleri
## Soru
Karmaşık, çok adımlı bir iş akışında **orkestrasyon** tabanlı saga'nın koreografiye göre temel avantajı nedir?
## A
Daha az altyapı gerektirir; hiçbir ek bileşen kurmadan tüm akışı yönetebilir
🤖 Orkestrasyon tam tersine bir orkestratör bileşeni ekler; avantajı azlık değil, akış yönetimidir.
## B
Servislere tam otonomi ve sıfır coupling verir; kimse kimseye bağımlı kalmaz
🤖 Orkestrasyonda servisler orkestratöre bağlanır; "sıfır coupling" zaten ulaşılabilir bir hedef değildir.
## C
Akış tek yerde açık bir durum makinesi olur; görünürlük ve telafi yönetimi kolaylaşır
🤖 Doğru: akış tek yerde görünür ve yönetilir; ödünleşme orkestratöre olan bağımlılıktır.
## D
İki fazlı commit (2PC) kullanarak tüm adımları güçlü tutarlılıkla tek pakette birleştirir
🤖 Saga 2PC kullanmaz; hem koreografi hem orkestrasyon nihai tutarlılık verir.
## Doğru
C
## Açıklama
Koreografide akış, servislerin birbirinin olaylarına tepkisinden **ortaya çıkar**; adım sayısı arttıkça "şu an nerede kaldık, neden takıldı" sorusunu yanıtlamak zorlaşır ve döngüsel olay bağımlılıkları oluşabilir. **Orkestrasyon** akışı merkezi bir orkestratörde **açık bir durum makinesi** olarak modeller: görünürlük artar, hata yönetimi merkezîleşir ve telafileri tetiklemek kolaylaşır. Bedeli, orkestratörün bir bağımlılık ve olası darboğaz olmasıdır — bu yüzden dayanıklı ve durumu kalıcı olmalıdır. "Daha az altyapı" (A) yanlıştır; "sıfır coupling" (B) yanlış hedeftir; saga 2PC (D) kullanmaz.

# saga-2 | İleri
## Soru
Saga'nın üçüncü adımı başarısız oldu; ilk iki adım kendi veritabanlarında zaten **commit edilmişti**. Bu commit'ler nasıl geri alınır?
## A
Telafi işlemleriyle: etkiyi anlamca tersine çeviren yeni iş işlemleri çalıştırılır
🤖 Doğru: commit edilmiş etkiler ancak "tersine çeviren" ileri işlemlerle düzeltilebilir.
## B
Tüm servislerde otomatik veritabanı rollback tetiklenir; commit'ler tek tek geri sarılır
🤖 Yerel transaction'lar çoktan commit edildi; başka servisin DB'sini geri saramazsın, rollback penceresi kapandı.
## C
İki fazlı commit koordinatörü devreye girip tüm katılımcıları birlikte geri alır
🤖 Saga'da 2PC/koordinatör yoktur; zaten onu kullanmamak için saga seçilir.
## D
Hiçbir şey yapılmaz; saga'da kalıcı tutarsızlık normal kabul edilen bir durumdur
🤖 Nihai tutarlılık geçici bir penceredir, kalıcı tutarsızlık değil; saga tam da telafilerle toparlar.
## Doğru
A
## Açıklama
Her saga adımı **kendi yerel transaction'ında commit** ettiği için, sonraki bir adım patladığında öncekileri klasik anlamda "rollback" edemezsin — o transaction'lar kapandı. Bunun yerine her adımın bir **telafi işlemi** (compensating transaction) tanımlanır: etkiyi **anlamca** tersine çeviren yeni bir ileri işlem (ödemeyi iade et, stok rezervasyonunu serbest bırak, siparişi iptal işaretle). Telafiler geriye doğru, en son başarılı adımdan başlangıca doğru çalışır. Otomatik DB rollback (B) veya 2PC koordinatörü (C) burada yoktur; tutarsızlığı kalıcı bırakmak (D) saga'nın amacına aykırıdır.

# saga-3 | Uzman
## Soru
Mikroservislerde dağıtık bir iş akışı için neden **2PC/XA** yerine **Saga** tercih edilir?
## A
2PC daha yavaştır ama katılımcı sayısı arttıkça Saga'dan daha iyi ölçeklendiği için
🤖 Tam ters: 2PC senkron kilitleriyle ölçeklenmez; Saga ölçek ve dayanıklılık için tercih edilir.
## B
2PC modern diller ve veritabanı sürücüleri tarafından artık desteklenmediği için
🤖 XA teknik olarak hâlâ vardır; tercih dil desteğiyle değil, mimari ödünleşmeyle ilgilidir.
## C
Saga güçlü tutarlılık verirken 2PC yalnızca nihai tutarlılık sağlayabildiği için tercih edilir
🤖 Tersi doğru: 2PC güçlü tutarlılık hedefler, Saga nihai tutarlılık verir.
## D
2PC kaynakları kilitler ve koordinatörü tek hata noktası yapar; Saga bunlardan kaçınır
🤖 Doğru: Saga'nın gerekçesi 2PC'nin kilitleme, koordinatör ve ölçek zayıflıklarından kaçınmaktır.
## Doğru
D
## Açıklama
**2PC/XA** güçlü tutarlılık için katılımcı kaynaklarda **kilitleri prepare fazından commit'e dek tutar**; bu gecikme ve düşük throughput yaratır, koordinatör commit ile abort arasında çökerse katılımcılar belirsizlikte (in-doubt) askıda kalır ve koordinatör bir **tek hata noktasıdır**. Ayrıca farklı veritabanı ve mesaj teknolojileri arasında XA desteği kırılgandır. **Saga**, her adımı bağımsız commit edip başarısızlığı telafiyle çözerek bu kilitlerden ve koordinatör bağımlılığından kaçınır; bedeli, aradaki pencerede sistemin **nihai tutarlı** olmasıdır. Yani seçim "güçlü tutarlılık + kilit + kırılganlık" ile "kullanılabilirlik + gevşek bağlama + nihai tutarlılık" arasındadır. A, B ve C bu ödünleşmeyi ters kurar.

# saga-4 | İleri
## Soru
Bir telafi işlemi çalışırken servis çöktü; mesaj yeniden teslim edildi ve telafi **ikinci kez** tetiklendi. Sistemin doğru kalması için ne gerekir?
## A
Telafiler asla yeniden denenmemeli; ilk denemede patlarsa akış durdurulup elle düzeltilmeli
🤖 Dağıtık sistemde retry kaçınılmazdır; "asla retry etme" gerçekçi değil ve kısmi başarısızlığı çözmez.
## B
Telafi idempotent olmalı; iki kez uygulanması tek uygulamayla aynı sonucu vermeli
🤖 Doğru: telafi de bir mesajla tetiklenir, at-least-once altında iki kez gelebilir; idempotency şarttır.
## C
Çökme sonrası baştan yeni bir saga başlatılmalı; yarım kalan saga terk edilmeli
🤖 Yeni saga başlatmak durumu ikiye katlar ve tutarsızlığı büyütür; mevcut saga kaldığı yerden sürmeli.
## D
Telafi tetiklenmeden önce dağıtık kilit alınmalı; kilit ikinci çalışmayı tümüyle engeller
🤖 Kilit eşzamanlılığı sınırlar ama çökmeden sonraki yeniden teslimi engellemez; çözüm idempotent telafidir.
## Doğru
B
## Açıklama
Kısmi başarısızlık (partial failure) saga'nın normalidir: adımlar da telafiler de mesajlarla tetiklenir ve mesajlaşma genelde **at-least-once**'tır, yani aynı telafi birden çok kez gelebilir. Bu yüzden telafiler **idempotent** olmalıdır — "ödemeyi iade et" iki kez gelirse yalnız bir iade oluşmalıdır (ör. iade edilmiş rezervasyon işaretlenir, tekrar gelen atlanır). Aynı şey ileri adımlar için de geçerlidir. "Asla retry etme" (A) dağıtık gerçekle çelişir; yeni saga başlatmak (C) durumu bozar; dağıtık kilit (D) çökme sonrası yeniden teslimi durdurmaz, çünkü kilit çoktan serbest kalmış olur.

# saga-5 | Uzman
## Soru
Bir **orkestrasyon** saga'da orkestratörün "hangi adımdayız, hangileri tamamlandı" durumu nasıl yönetilmelidir?
## A
Yalnız orkestratörün belleğinde tutulmalı; diske yazmadığı için en hızlı ve en basit çözüm budur
🤖 Bellek uçucudur; orkestratör çökerse akışın nerede kaldığı kaybolur, saga yarım kalır.
## B
Hiç saklanmamalı; her adım stateless kalırsa sistem kendiliğinden kurtarılabilir olur
🤖 Saga doğası gereği uzun ömürlü ve durumludur; durumu saklamamak kurtarmayı imkânsız kılar.
## C
İstemci tarafında (tarayıcı oturumu veya mobil uygulama) tutulup her adımda geri gönderilmeli
🤖 İstemci kaybolabilir/çıkabilir; uzun ömürlü sunucu tarafı iş akışı istemciye emanet edilemez.
## D
Kalıcı bir durum makinesinde (saga log/tablo) saklanmalı; çökme sonrası kaldığı yerden sürmeli
🤖 Doğru: dayanıklı, kalıcı saga durumu kurtarılabilir ilerleme sağlar.
## Doğru
D
## Açıklama
Bir saga saniyeler, dakikalar, bazen günler sürebilen **uzun ömürlü (long-running)** bir iş akışıdır; bu süre boyunca orkestratör süreci yeniden başlayabilir, deploy edilebilir, çökebilir. Bu yüzden "nerede kaldık" bilgisi **kalıcı** olmalıdır — tipik olarak bir **saga log** veya durum tablosunda saklanan açık bir **durum makinesidir** (state machine). Böylece orkestratör kurtulunca durumu okuyup **kaldığı yerden** devam eder, tamamlanmamış adımları veya bekleyen telafileri sürdürür; bu, adım tetikleyicilerinin idempotency'siyle birlikte çalışır. Belleğe (A), hiç saklamamaya (B) veya istemciye (C) güvenmek, orkestratör her yeniden başladığında akışı kaybettirir.

# saga-6 | Uzman
## Soru
Bir **koreografi** saga'da mesajlaşma **at-least-once**'tır; bu yüzden aynı olay bir tüketici tarafından iki kez işlenebilir. Saga'yı bu tekrarlara karşı güvenli kılan nedir?
## A
Tüketiciler ve telafiler idempotent yapılır; pivot adımdan sonrası retriable tasarlanır
🤖 Doğru: at-least-once altında güvenlik idempotency ve pivot sonrası retriable tasarımla sağlanır.
## B
Broker'da exactly-once teslimat açılır; böylece her olay tam bir kez ulaştığı garanti edilir
🤖 Uçtan uca exactly-once teslimat pratikte bir efsanedir; gerçekte at-least-once + idempotent işleme kullanılır.
## C
Her tüketici global bir kilit alıp diğer tüm tüketicileri işlem boyunca bloke eder
🤖 Global kilit koreografinin gevşek bağlama amacını yok eder, ölçeklenmez ve tekrarı da çözmez.
## D
Olaylar TCP üzerinden gittiği için uygulama düzeyinde tekrar zaten oluşamaz
🤖 TCP paket düzeyinde güvenilirlik verir; uygulama düzeyinde retry ve yeniden teslim tekrar üretir.
## Doğru
A
## Açıklama
Koreografi saga'da her adım bir olayla tetiklenir ve broker genelde **at-least-once** teslim eder — yani aynı olay tekrar gelebilir. Güvenlik iki tekniğin birleşimidir: (1) **idempotent tüketiciler ve idempotent telafiler** — işlenen olay/anahtar tekilleştirilir (dedup), tekrar gelen atlanır; (2) akışın, geri dönülemez ya da telafisi imkânsız **"pivot" adımdan** sonraki tüm adımlar **retriable/idempotent** olacak şekilde sıralanması, böylece pivot'tan sonra ilerleyen akış güvenle tekrarlanabilir. "Exactly-once teslimat" (B) bir efsanedir; global kilit (C) koreografiyi anlamsız kılar; TCP (D) uygulama düzeyi tekrarları önlemez.
