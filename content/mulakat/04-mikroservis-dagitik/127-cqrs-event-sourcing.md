---
slug: cqrs-event-sourcing
baslik: "CQRS ve Event Sourcing: Ayrı Okuma Modeli, Snapshot ve Şema Evrimi"
kademe: İleri
sira: 127
etiketler: 10,11
---

## Konu anlatımı

### Bu ne işe yarar?

Alışılmış uygulamada tek bir veri modeli her işi görür. Aynı sınıflar ve aynı tablolar hem "sipariş oluştur" işini hem de "son 30 günün satış özetini getir" işini taşır. Bir süre sonra bu model iki tarafa da dar gelmeye başlar. Yazma tarafı iş kuralları yüzünden ağırlaşır. Okuma tarafı ise beş tabloyu birleştiren (join) sorgularla yavaşlar.

**CQRS** tek bir fikre dayanır: komut yolunu sorgu yolundan ayır. **Komut (command)** durumu değiştiren işlemdir, yani "şunu yap" der. **Sorgu (query)** ise durumu döndürür ve hiçbir şeyi değiştirmez. Ayrıldıklarında her biri kendi ihtiyacına göre şekillenebilir.

Benzetme mutfak ile vitrindir. Mutfakta kurallar, ölçüler ve disiplin vardır. Vitrinde ise ürün sadece hızlı görülsün diye hazır dizilir. Bir noktaya dikkat: bu, veritabanına bir okuma kopyası (read replica) ekleyip sorguları oraya yöneltmekle aynı şey değildir. Okuma kopyası bir altyapı ölçekleme yöntemidir; CQRS ise ayrı **modeller** ve ayrı kod yolları demektir.

**Event sourcing** ayrı ama sık birlikte anılan bir fikirdir: durumu bir tabloda değil, başa gelmiş **değişmez olaylar** dizisi olarak sakla. Benzetme: banka hesabında bakiyeyi tek bir sayı olarak tutmak yerine tüm yatırma/çekme hareketlerini saklamak; bakiye bu hareketlerin toplamıdır.

### Temel kavramlar

- **Olay (event):** olmuş bitmiş bir gerçeğin kaydı (`ParaÇekildi`). **Değişmezdir** (immutable): geçmiş düzeltilmez, üstüne yeni olay yazılır.
- **Okuma modeli (read model):** sorgunun tam ihtiyacına göre önceden hazırlanmış veridir. Çoğu zaman **denormalize** tutulur; yani birleştirme (join) gerekmesin diye aynı bilgi bilinçli olarak tekrarlanır.
- **Yansıtma (projection):** yazma tarafındaki olayları okuyup okuma modelini güncelleyen süreçtir.
- **Nihai tutarlılık (eventual consistency):** okuma modeli kısa bir gecikmeyle güncellenir; yazmadan hemen sonraki okuma henüz eski değeri gösterebilir.
- **Yeniden oynatma (replay / fold):** olayları baştan sırayla uygulayarak durumu yeniden hesaplamak.
- **Anlık görüntü (snapshot):** yeniden oynatmayı kısaltmak için belirli aralıklarla alınan durum fotoğrafı.
- **Upcasting (sürüm yükseltme):** okuma anında eski sürüm bir olayı yeni şemaya çeviren dönüştürücü.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CQRS yazma ve okuma yollarının ayrımı">
  <defs>
    <marker id="cqrs-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yazma ve okuma ayrı yollar</text>
  <rect x="30" y="86" width="96" height="48" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="78" y="115" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">İstemci</text>
  <rect x="188" y="46" width="150" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="263" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Komut Modeli</text>
  <text x="263" y="88" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iş kuralları, yazma</text>
  <rect x="188" y="122" width="150" height="52" rx="10" fill="#F3EEF8" stroke="#6B4E9E" stroke-width="1.5"/>
  <text x="263" y="146" text-anchor="middle" font-size="12" font-weight="700" fill="#6B4E9E" font-family="system-ui">Sorgu Modeli</text>
  <text x="263" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">okuma modeli</text>
  <rect x="400" y="46" width="140" height="52" rx="10" fill="#FFF" stroke="#25804C" stroke-width="1.5"/>
  <text x="470" y="70" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">Event Store /</text>
  <text x="470" y="86" text-anchor="middle" font-size="11.5" fill="#25804C" font-family="system-ui">yazma DB</text>
  <rect x="400" y="122" width="140" height="52" rx="10" fill="#FFF" stroke="#6B4E9E" stroke-width="1.5"/>
  <text x="470" y="146" text-anchor="middle" font-size="11.5" fill="#6B4E9E" font-family="system-ui">okuma DB</text>
  <text x="470" y="164" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">(denormalize)</text>
  <line x1="126" y1="100" x2="186" y2="74" stroke="#186399" stroke-width="2" marker-end="url(#cqrs-ar)"/>
  <line x1="126" y1="120" x2="186" y2="146" stroke="#186399" stroke-width="2" marker-end="url(#cqrs-ar)"/>
  <line x1="338" y1="72" x2="398" y2="72" stroke="#186399" stroke-width="2" marker-end="url(#cqrs-ar)"/>
  <line x1="338" y1="148" x2="398" y2="148" stroke="#186399" stroke-width="2" marker-end="url(#cqrs-ar)"/>
  <line x1="470" y1="98" x2="470" y2="120" stroke="#B0453B" stroke-width="2" stroke-dasharray="4 3" marker-end="url(#cqrs-ar)"/>
  <text x="560" y="112" text-anchor="middle" font-size="9.5" fill="#B0453B" font-family="system-ui">asenkron</text>
  <text x="560" y="126" text-anchor="middle" font-size="9.5" fill="#B0453B" font-family="system-ui">projeksiyon</text>
</svg>
```

### Nasıl çalışır?

CQRS'te komut yolu iş kurallarını uygular ve değişikliği yazma deposuna kaydeder. Bu depo klasik bir tablo da olabilir, bir olay deposu (event store) da. Değişiklik oradan bir **yansıtmayla** okuma modeline taşınır ve bu taşıma tipik olarak **eşzamansızdır (asenkron)**.

Bunun kaçınılmaz sonucu nihai tutarlılıktır: kullanıcı kaydettikten hemen sonra listede eski değeri görebilir. Bu, bilinçli kabul edilen bir bedeldir ve arayüz buna göre tasarlanır; örneğin sonuç daha kaydedilmeden ekranda güncellenmiş gibi gösterilir. İki yanlış yol vardır. Okuma modelini yazmayla aynı işlem (transaction) içinde eşzamanlı güncellemek ayrışmayı bozar. Hiç güncellememek ya da işi gecelik toplu göreve bırakmak ise okuma modelini bayatlatır.

Event sourcing'de güncel durum hiçbir yerde **saklanmaz**. Bir varlığın (aggregate) olay akışı olay deposundan okunur. Başlangıç durumundan itibaren her olay sırayla uygulanır ve durum böyle **yeniden kurulur**.

Akış uzadıkça bu iş pahalılaşır. Standart çözüm **anlık görüntüdür**. Durumun fotoğrafı belirli aralıklarla saklanır, kurtarma en son fotoğraftan başlar ve yalnız ondan sonraki olaylar yeniden oynatılır. Anlık görüntü bir **hızlandırmadır**, gerçeğin kaynağı değildir. Tek gerçeklik kaynağı (source of truth) hâlâ olay akışıdır; fotoğraf bozulsa bile olaylardan yeniden üretilebilir. Olayları silmek ya da güncel durumu ayrı bir tabloya taşıyıp asıl gerçek saymak, bu tasarımı büsbütün terk etmek demektir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Event sourcing: olaylar katlanarak durum kurulur, snapshot replay maliyetini azaltır">
  <defs>
    <marker id="cqrs-es-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#6B3FA0"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Durum saklanmaz, olaylardan katlanır (fold)</text>
  <text x="60" y="58" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Değişmez olay akışı</text>
  <rect x="40" y="70" width="70" height="40" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="75" y="94" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">E1</text>
  <rect x="120" y="70" width="70" height="40" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="155" y="94" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">E2</text>
  <rect x="200" y="70" width="70" height="40" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="235" y="94" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">E3</text>
  <rect x="280" y="70" width="70" height="40" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="315" y="94" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">E4</text>
  <rect x="360" y="70" width="70" height="40" rx="6" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="395" y="94" text-anchor="middle" font-size="10.5" fill="#6B3FA0" font-family="system-ui">E5</text>
  <rect x="200" y="128" width="150" height="34" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="275" y="150" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">Snapshot (E3 sonrası)</text>
  <line x1="235" y1="110" x2="255" y2="126" stroke="#B26B00" stroke-width="1.5"/>
  <rect x="470" y="66" width="140" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="540" y="88" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Güncel durum</text>
  <text x="540" y="105" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">replay/fold sonucu</text>
  <line x1="430" y1="90" x2="468" y2="90" stroke="#6B3FA0" stroke-width="2" marker-end="url(#cqrs-es-ar)"/>
  <path d="M350 145 C 420 145, 434 108, 466 96" fill="none" stroke="#B26B00" stroke-width="1.8" stroke-dasharray="5 3"/>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Snapshot'tan sonra yalnız E4, E5 replay edilir → maliyet düşer</text>
  <text x="320" y="208" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Olaylar tek gerçeklik; snapshot yalnız optimizasyon</text>
</svg>
```

Zaman geçtikçe olayların yapısı da değişir: yeni bir alan gerekir ya da bir alanın adı değiştirilir. Ama olaylar **değişmezdir**. Bu yüzden depodaki eski kayıtları toplu bir göç (migration) işlemiyle düzenlemek yanlıştır. Hepsini atıp sıfırdan yeni bir depo kurmak da öyle. Her ikisi de tarihsel gerçeği ve denetim izini bozar.

Doğru yöntem ileriye doğru çalışır. Olaylara bir **sürüm numarası** eklenir (event versioning). Eski sürüm olaylar okunurken **upcasting** ile, çalışma anında yeni şemaya çevrilir. Bir alternatif de **toleranslı şema** kullanmaktır: eksik alanı varsayılan değerle doldur, tanımadığın alanı yok say. Böylece eski ve yeni olaylar aynı depoda bir arada yaşar. Tüketiciler ise tek bir güncel modele göre çalışmayı sürdürür.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Şema evrimi: eski olay okuma anında upcasting ile yeni şemaya çevrilir">
  <defs>
    <marker id="cqrs-up-ar" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0,0 L9,4.5 L0,9 Z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Olaylar değişmez → upcasting okuma anında çevirir</text>
  <rect x="30" y="66" width="150" height="80" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="105" y="90" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">v1 olay (eski)</text>
  <text x="105" y="112" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">store'da olduğu gibi</text>
  <text x="105" y="130" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">DEĞİŞMEZ kalır</text>
  <rect x="245" y="66" width="150" height="80" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Upcaster</text>
  <text x="320" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">v1 → v2 dönüştür</text>
  <text x="320" y="134" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(eksik alan varsayılan)</text>
  <rect x="460" y="66" width="150" height="80" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="535" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">v2 olay</text>
  <text x="535" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tüketici tek güncel</text>
  <text x="535" y="134" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">modele karşı çalışır</text>
  <line x1="180" y1="106" x2="243" y2="106" stroke="#186399" stroke-width="2" marker-end="url(#cqrs-up-ar)"/>
  <line x1="395" y1="106" x2="458" y2="106" stroke="#186399" stroke-width="2" marker-end="url(#cqrs-up-ar)"/>
  <text x="320" y="180" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">Depodaki olayları toplu düzenleme YOK — geçmiş korunur</text>
</svg>
```

Son bir netleştirme: CQRS, event sourcing'i **gerektirmez**. Event sourcing de CQRS olmadan kullanılabilir. Birlikte iyi çalışırlar ama aynı şey değildirler.

İkisi de bedava değildir. Getirdikleri yük şudur: iki ayrı model, yansıtma altyapısı, nihai tutarlılıkla başa çıkma, olay deposunun yönetimi, anlık görüntüler ve şema evrimi. Bu bedel yalnız belirli koşullarda haklı çıkar: okuma ile yazma yükleri gerçekten ayrıştığında, iş alanı (domain) karmaşık olduğunda ya da güçlü bir denetim izi veya geçmişe dönüp bakma ihtiyacı olduğunda. Basit CRUD uygulamalarında ve dengeli yüklerde aynı bedel karşılıksız kalır; buna **aşırı mühendislik** denir.

### Mülakatta ne sorulur?

- CQRS'in tanımı; read replica eklemekten farkı nedir?
- Okuma modeli nasıl güncellenir ve doğrudan sonucu nedir?
- Event sourcing'de güncel durum nasıl elde edilir?
- Replay pahalılaşınca ne yaparsın? Snapshot gerçeğin kaynağı mıdır?
- Olaylar değişmezken şema nasıl evrilir? (Sürümleme + upcasting.)
- CQRS+ES ne zaman aşırı mühendisliktir?

### Tek cümlede

CQRS yazma ile okumayı ayrı modellere böler ve bedeli nihai tutarlılıktır; event sourcing ise durumu olay günlüğünü yeniden oynatarak türetir, anlık görüntüyle hızlanır ve şemasını olayları hiç bozmadan upcasting ile evriltir.

# cqrs-1 | Orta
## Soru
CQRS'in (Command Query Responsibility Segregation) temel fikri nedir?
## A
Durumu değiştiren komut yolu ile veri döndüren sorgu yolunu ayrı modellere bölmek
🤖 Doğru: CQRS tam olarak sorumluluğu komut ve sorgu olarak ayırmaktır.
## B
Tüm okuma ve yazmaları tek, paylaşılan bir domain modeli üzerinden geçmeye zorlamak
🤖 Bu CQRS'in tersidir; CQRS tam da bu tek-model baskısını kaldırmak için vardır.
## C
Event sourcing ile birebir aynı şeydir; biri olmadan diğeri hiç uygulanamaz
🤖 Yaygın karışıklık: CQRS event sourcing gerektirmez, event sourcing de CQRS'siz kullanılabilir.
## D
Veritabanına okuma replikası ekleyip tüm sorguları oraya yönlendirmekten ibarettir
🤖 Read replica bir ölçek tekniğidir; CQRS ise ayrı modeller ve ayrı kod yollarıyla ilgilidir.
## Doğru
A
## Açıklama
CQRS, tek bir modelin hem karmaşık yazma kurallarını hem de çeşitli okuma ihtiyaçlarını iyi karşılayamadığı gözleminden doğar. Çözüm: **komut** (durumu değiştiren) ve **sorgu** (durum döndüren, yan etkisiz) sorumluluklarını ayrı modellere ve yollara bölmek. Yazma modeli tutarlılığa ve iş kurallarına, okuma modeli sorgu performansına ve şekline odaklanır. CQRS event sourcing'i **gerektirmez** (C yanlış); tek modele zorlamak (B) CQRS'in reddettiği şeydir; salt read replica eklemek (D) ise CQRS değil, altyapı ölçeklemesidir.

# cqrs-2 | İleri
## Soru
CQRS'te ayrı **okuma modeli** (read model) tipik olarak nasıl güncellenir ve bunun doğrudan sonucu nedir?
## A
Yazmayla aynı transaction içinde senkron güncellenir; okuma her zaman anında tutarlı olur
🤖 Senkron güncelleme CQRS'in ölçek ve ayrışma amacını çoğu durumda bozar; tipik model asenkrondur.
## B
Yalnız bir kez, sistem ilk kurulurken doldurulur; sonrasında elle bakım gerektirir
🤖 Okuma modeli yazmalarla sürekli güncel tutulmalıdır; aksi halde bayat kalır ve işe yaramaz.
## C
Gece çalışan bir toplu iş (batch) ile günde bir kez baştan yeniden üretilir
🤖 Gecelik batch bazı raporlarda kabul edilebilir ama CQRS'in tipik güncelleme yolu değildir.
## D
Olaylardan beslenen bir projeksiyonla asenkron güncellenir; sonuç nihai tutarlılık olur
🤖 Doğru: asenkron projeksiyon → nihai tutarlılık, CQRS'in kabul edilen ödünleşmesidir.
## Doğru
D
## Açıklama
CQRS'te okuma modeli genelde yazma tarafında olan değişikliklerden (olaylar veya değişiklik akışı) beslenen bir **projeksiyon** ile **asenkron** güncellenir. Bunun kaçınılmaz sonucu **nihai tutarlılıktır**: bir yazmadan hemen sonra okuma modeli o değişikliği henüz göstermeyebilir (kısa bir gecikme penceresi). Bu, CQRS'i benimserken bilinçli kabul edilen ödünleşmedir ve arayüz bunu tolere edecek şekilde tasarlanır (ör. iyimser güncelleme). Aynı transaction içinde senkron güncelleme (A) ayrışmayı bozar; bir kez doldurup bırakmak (B) veya gecelik batch (C) okuma modelini bayat bırakır.

# cqrs-3 | İleri
## Soru
CQRS + Event Sourcing hangi durumda **aşırı mühendislik** (over-engineering) olur?
## A
Her CRUD uygulamasında; bu desen ölçekten ve domainden bağımsız olarak daima gerekli olduğu için
🤖 Tersine, basit CRUD tam da CQRS+ES'in gereksiz olduğu yerdir; "daima gerekli" yanlıştır.
## B
Basit CRUD ve benzer okuma/yazma yüklerinde; getirdiği maliyet faydayı aştığı için
🤖 Doğru: fayda yokken maliyet ödemek klasik over-engineering'dir.
## C
Okuma ve yazma yüklerinin ciddi biçimde ayrıştığı yüksek ölçekli sistemlerde
🤖 Bu tam da CQRS'in parladığı senaryodur; over-engineering değil, yerinde kullanımdır.
## D
Zengin denetim (audit) ve zaman-yolculuğu gerektiren finansal domainlerde
🤖 Bu event sourcing'in en güçlü olduğu alandır; over-engineering değil, doğru araçtır.
## Doğru
B
## Açıklama
CQRS+ES bedava değildir: iki model, projeksiyon altyapısı, nihai tutarlılıkla başa çıkma, event store yönetimi, snapshot ve şema evrimi yükü getirir. Bu maliyet ancak **fayda** varsa haklıdır — okuma/yazma yükleri gerçekten ayrıştığında, domain karmaşıksa, güçlü denetim ve zaman-yolculuğu gerekiyorsa. **Basit CRUD**'da, düşük karmaşıklıkta ve benzer yüklerde aynı maliyet karşılıksız kalır; işte **aşırı mühendislik** budur. Senior refleksi "bu desen havalı" değil, "bu problem bu deseni hak ediyor mu"dur. C ve D deseni haklı çıkaran senaryolardır; "daima gerekli" (A) tehlikeli bir genellemedir.

# cqrs-4 | İleri
## Soru
Event sourcing kullanan bir sistemde bir **aggregate**'in güncel durumu nasıl elde edilir?
## A
Bir durum tablosundaki son satır okunur; olaylar yalnız denetim için saklanır
🤖 Bu CRUD'dur; event sourcing'de kaynak-of-truth durum tablosu değil olay akışıdır.
## B
Yalnız bir önbellekten (cache) okunur; olay akışı bu aşamada hesaba katılmaz
🤖 Cache bir hızlandırma katmanıdır; kaynak-of-truth olay akışıdır, olaylar yok sayılamaz.
## C
Olay akışı okunup baştan veya son snapshot'tan katlanarak (fold) yeniden kurulur
🤖 Doğru: durum saklanmaz, olaylardan yeniden inşa edilir.
## D
Sistem açılışında tüm aggregate durumları tek seferde hesaplanıp bellekte sabitlenir ve orada kalır
🤖 Durum talep anında (veya snapshot'la) türetilir; bir kez sabitlemek yeni olaylarla güncelliğini yitirir.
## Doğru
C
## Açıklama
Event sourcing'de güncel durum bir yerde **saklanmaz**; aggregate'in event store'daki **değişmez olay akışı** okunur ve bir başlangıç durumundan itibaren her olay sırayla uygulanarak (fold/replay) durum **yeniden kurulur** (state reconstruction). Bu, geçmişin tam kaydını (denetim) ve herhangi bir andaki durumu üretebilmeyi (zaman yolculuğu) verir. Uzun akışlarda replay pahalılaşacağı için **snapshot** kullanılır (bkz. sonraki soru). Son satırı okumak (A) CRUD'dur; cache'e (B) güvenmek kaynak-of-truth'u değiştirmez; açılışta bir kez sabitlemek (D) sonraki olaylarla güncelliği bozar.

# cqrs-5 | İleri
## Soru
Bir aggregate binlerce olay biriktirdi; durumu her seferinde baştan replay etmek pahalı hale geldi. Standart çözüm nedir?
## A
Snapshot alınır; replay yalnız son snapshot'tan sonraki olaylarla sınırlanır
🤖 Doğru: snapshot, replay maliyetini son snapshot sonrası kısa kuyrukla sınırlar.
## B
Eski olaylar kalıcı olarak silinir; depoda yalnız son N olay saklanmaya devam eder
🤖 Olayları silmek denetimi ve yeniden projeksiyon yeteneğini yok eder; event sourcing'in özüne aykırıdır.
## C
Güncel durum ayrı bir tabloya yazılıp kaynak-of-truth olarak oraya taşınır
🤖 Durumu gerçeğin kaynağı yapmak event sourcing'i terk etmektir; snapshot ise yalnızca bir optimizasyondur.
## D
Event store ilişkisel bir CRUD tablosuna dönüştürülüp olaylardan tümüyle vazgeçilir
🤖 Bu da deseni terk etmektir; sorun tasarımın içinde snapshot ile çözülür.
## Doğru
A
## Açıklama
**Snapshot**, event sourcing'in standart performans tekniğidir: aggregate'in durumu belirli aralıklarla (ör. her N olayda bir) bir **anlık görüntü** olarak saklanır. Durum kurulurken sistem en son snapshot'ı yükler ve yalnız **ondan sonraki** olayları replay eder; böylece maliyet tüm geçmişle değil kısa bir kuyrukla sınırlanır. Snapshot bir **optimizasyondur**, kaynak-of-truth hâlâ olay akışıdır (snapshot bozulsa bile olaylardan yeniden üretilebilir) — bu yüzden durumu kalıcı gerçek kaynağı yapmak (C) farklı ve yıkıcı bir karardır. Olayları silmek (B) denetimi ve yeniden projeksiyonu yok eder; event store'dan vazgeçmek (D) problemi çözmez, deseni terk eder.

# cqrs-6 | Uzman
## Soru
Event store'daki **eski olayların** yapısı değişti (yeni bir alan gerekiyor, bir alan yeniden adlandırıldı). Olaylar **değişmez** (immutable) olduğuna göre bu şema evrimiyle nasıl başa çıkılır?
## A
Depodaki tüm eski olaylar toplu bir migration ile yeni şemaya göre güncellenir
🤖 Olaylar değişmezdir ve olan biteni kaydeder; onları geriye dönük düzenlemek denetimi ve tarihsel gerçeği bozar.
## B
Sıfırdan yeni bir event store kurulur; eski olaylar bir arşive taşınıp kullanımdan kaldırılır
🤖 Bu tarihsel olayları devre dışı bırakmaktır; şema evrimi tam da olayları koruyarak yönetilir.
## C
Olaylar değişmez kalır; sürümleme ve okuma anındaki upcasting ile yeni şemaya çevrilir
🤖 Doğru: olayları koruyup okuma anında yeni şemaya çevirmek standart yaklaşımdır.
## D
Tüketicilerden hangi sürümü bekledikleri sorulur; uyuşmazlıkta sistem hata verip durur
🤖 Amaç eski ve yeni olayların bir arada yaşamasıdır; uyuşmazlıkta durmak evrimi imkânsız kılar.
## Doğru
C
## Açıklama
Event sourcing'de olaylar **olmuş bitmiş gerçeklerdir** ve değişmezdir — geçmişe dönüp onları düzenlemek (A) veya kullanımdan kaldırmak (B) sistemin denetim ve yeniden projeksiyon garantilerini yok eder. Şema evrimi bunun yerine **ileriye** yönetilir: olaylara bir **sürüm** eklenir (event versioning) ve eski sürüm olaylar okunurken **upcasting** ile (bir dönüştürücü zinciriyle) çalışma zamanında yeni şemaya çevrilir; alternatif olarak **toleranslı şema** kullanılır (eksik alanı varsayılanla doldur, bilinmeyeni yok say). Böylece eski ve yeni olaylar aynı depoda bir arada yaşar ve tüketiciler tek bir güncel modele karşı çalışır. Uyuşmazlıkta durmak (D) evrimi imkânsız kılar.
