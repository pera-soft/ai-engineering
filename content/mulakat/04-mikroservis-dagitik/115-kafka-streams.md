---
slug: kafka-streams
baslik: "Kafka Streams: KStream/KTable/GlobalKTable, State Store ve Windowing"
kademe: İleri
sira: 115
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Kafka'dan mesaj okumak kolaydır. Zor olan, okurken **hatırlamaktır**. Şu üç soru bunu iyi anlatır: "Bu kullanıcının son 5 dakikadaki tıklama sayısı kaç?" · "Bu hesabın güncel bakiyesi ne?" · "Bu siparişe müşteri adını nasıl eklerim?" Bunları düz bir tüketiciyle yazarsanız üç işi elle yönetmek zorunda kalırsınız: hafızayı, çökme sonrası kurtarmayı ve tekrar işlemeyi. **Kafka Streams** tam olarak bu zor kısımları hazır verir.

### Temel kavramlar

- **Stream (akış):** durmadan gelen mesaj dizisi.
- **Kütüphane, küme değil:** Kafka Streams ayrı bir sunucu kümesi kurmanızı istemez; kendi uygulamanızın içinde çalışan bir kütüphanedir.
- **KStream:** her kaydı **bağımsız bir olay** sayar. Aynı anahtarla gelen iki kayıt iki ayrı gerçektir ("hesaba +10", sonra "+20"); hiçbiri diğerini ezmez.
- **KTable:** her kaydı anahtar başına bir **güncelleme** (upsert) sayar; yalnız o anahtarın **son değeri** tutulur. Değeri `null` olan kayıt ise o anahtarın **silinmesi** demektir (buna *tombstone*, mezar taşı denir).
- **GlobalKTable:** KTable'ın bölümlenmemiş kardeşi; tablonun **tam kopyası her uygulama örneğinde** durur.
- **Durum (state):** işleyicinin geçmişten hatırladığı bilgi (sayaç, toplam, son değer).
- **Durum deposu (state store):** bu hafızanın tutulduğu gömülü anahtar-değer motorudur. Varsayılan olarak **RocksDB** kullanır ve yerel diske yazar.
- **Değişiklik günlüğü (changelog topic):** her durum deposunun Kafka'daki yedeğidir. Bu topic **log-compacted** (günlüğü sıkıştırılmış) tutulur; yani Kafka her anahtar için yalnız en son değeri saklar, eskilerini temizler.
- **Pencere (window):** zamanı dilimlere bölüp "her dilim için ayrı hesap tut" demenin adı.

### Nasıl çalışır?

**Model seçimi** verinin şekline değil, anlamına bakar. Kayıtlar birbirini ezmeyen olaylarsa **KStream** seçilir. Anahtar başına güncel durum tutuluyorsa **KTable** seçilir. İkisi aynı topic'ten okusa bile davranışları özdeş değildir: KStream ekler, KTable üzerine yazar. Anahtarın tekrar etmesi tek başına KTable'ı gerektirmez. Toplam almak istiyorsanız KStream'i `groupByKey().reduce(...)` ile birleştirir ve sonucu KTable olarak alırsınız.

**Durum ve dayanıklılık.** Toplama (aggregate), pencereleme ve birleştirme (join) gibi durum tutan işlemler verilerini yerel durum deposunda saklar. Ama yerel disk kalıcı değildir: makine çöker ya da iş yükü bomboş bir makineye taşınır. Bu yüzden her depo otomatik olarak bir **changelog topic**'e yedeklenir.

Yeni bir uygulama örneği ayağa kalkınca bu changelog'u **yeniden oynatarak (replay)** deposunu kurar. Kaynak topic'i baştan alıp tüm iş mantığını tekrar çalıştırmaz; çünkü changelog zaten hesaplanmış son durumu taşır. Durum, ZooKeeper veya KRaft üst veri katmanında saklanmaz. `num.standby.replicas` ayarıyla sıcak yedek örnekler tutarak bu geri yükleme süresini kısaltabilirsiniz.

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="KStream olay akışı ile KTable son durum farkı ve state store changelog yedeği">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">KStream (olay) vs KTable (son durum) + changelog yedeği</text>

  <rect x="24" y="44" width="288" height="118" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="13.5" font-weight="700" fill="#186399" font-family="system-ui">KStream — olay akışı (append)</text>
  <text x="168" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">(A, +10) (A, +20) (A, -5)</text>
  <text x="168" y="108" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">her kayıt bağımsız gerçek;</text>
  <text x="168" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">hiçbiri diğerini ezmez</text>
  <text x="168" y="146" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">3 olay = 3 kayıt kalır</text>

  <rect x="328" y="44" width="288" height="118" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">KTable — changelog (upsert)</text>
  <text x="472" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">(A, 10) (A, 30) (A, 25)</text>
  <text x="472" y="108" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">anahtar başına son değer;</text>
  <text x="472" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">null = silme (tombstone)</text>
  <text x="472" y="146" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">A için tek satır: 25</text>

  <rect x="140" y="196" width="200" height="72" rx="10" fill="#ffffff" stroke="#1C3247" stroke-width="1.5"/>
  <text x="240" y="222" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">State Store (RocksDB)</text>
  <text x="240" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yerel disk — kalıcı değil</text>
  <text x="240" y="258" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çökünce yeniden kurulur</text>

  <rect x="404" y="196" width="204" height="72" rx="10" fill="#186399" stroke="#1C3247" stroke-width="1.5"/>
  <text x="506" y="222" text-anchor="middle" font-size="12.5" font-weight="700" fill="#ffffff" font-family="system-ui">changelog topic</text>
  <text x="506" y="242" text-anchor="middle" font-size="10.5" fill="#EAF1F7" font-family="system-ui">compacted, fault-tolerant</text>
  <text x="506" y="258" text-anchor="middle" font-size="10.5" fill="#EAF1F7" font-family="system-ui">replay ile geri yükleme</text>

  <line x1="340" y1="232" x2="402" y2="232" stroke="#25804C" stroke-width="2" marker-end="url(#kstream-arrow)"/>
  <line x1="404" y1="250" x2="342" y2="250" stroke="#5D7789" stroke-width="2" stroke-dasharray="4 3" marker-end="url(#kstream-arrow)"/>
  <text x="372" y="226" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">yaz</text>
  <text x="372" y="266" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">restore</text>

  <defs>
    <marker id="kstream-arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M0,0 L10,5 L0,10 z" fill="#1C3247"/>
    </marker>
  </defs>
</svg>
```

**Birleştirme (join) ve eş-bölümleme.** KStream ile KTable birleştirilebilir. Bu, akıştaki kaydı ek bilgiyle zenginleştirmenin (enrichment) tipik yoludur. Ancak **eş-bölümleme (co-partitioning)** şartı vardır. Şart şudur: iki taraf aynı sayıda **bölüme (partition)** sahip olmalı ve aynı anahtara göre bölümlenmiş olmalıdır.

Şart tutmazsa eşleşmesi gereken anahtarlar farklı bölümlere düşer. Aynı görev onları hiçbir zaman bir araya getiremez ve sonuç `null` gelir. Çözüm, `selectKey` veya `repartition` ile iki tarafı hizalamaktır. **GlobalKTable** ise tablonun tam kopyasını tuttuğu için eş-bölümleme **istemez**; birleştirme anahtarı akışın anahtarından farklı da olabilir. Küçük ve sık okunan referans tabloları için idealdir. Bedeli, tüm veriyi her uygulama örneğinde taşımaktır.

**Pencereler.** *Tumbling* pencereler sabit boyutludur, çakışmaz ve birbirine bitişiktir; adım aralığı pencere boyutuna eşittir. "Her 5 dakikalık dilim" tam olarak budur. *Hopping* pencerelerde adım aralığı boyuttan küçük olabilir. Bu yüzden pencereler **çakışır** ve tek bir olay birden çok pencereye girebilir. *Session* penceresinin sabit bir boyutu yoktur; belirlenen **hareketsizlik boşluğu (inactivity gap)** aşılana dek büyümeye devam eder. *Sliding* pencere ise olaylar arasındaki zaman farkına göre tanımlanır.

```svg
<svg viewBox="0 0 640 290" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Pencere türleri: tumbling çakışmaz, hopping çakışır, session hareketsizlik boşluğuna göre">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pencere (window) türleri</text>

  <text x="24" y="56" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Tumbling — sabit, çakışmayan, bitişik</text>
  <rect x="24" y="64" width="120" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="148" y="64" width="120" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <rect x="272" y="64" width="120" height="30" rx="5" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="84" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0–5 dk</text>
  <text x="208" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">5–10 dk</text>
  <text x="332" y="84" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">10–15 dk</text>
  <text x="410" y="84" font-size="10.5" fill="#5D7789" font-family="system-ui">adım = boyut</text>

  <text x="24" y="128" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Hopping — çakışır (adım &lt; boyut)</text>
  <rect x="24" y="136" width="150" height="26" rx="5" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <rect x="84" y="166" width="150" height="26" rx="5" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <rect x="144" y="136" width="150" height="26" rx="5" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="99" y="154" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">0–5 dk</text>
  <text x="159" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2–7 dk</text>
  <text x="410" y="158" font-size="10.5" fill="#5D7789" font-family="system-ui">bir olay çok pencerede</text>

  <text x="24" y="226" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Session — hareketsizlik boşluğuna göre</text>
  <rect x="24" y="234" width="90" height="28" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <rect x="150" y="234" width="170" height="28" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="69" y="253" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oturum A</text>
  <text x="235" y="253" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">oturum B</text>
  <text x="132" y="253" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">gap</text>
  <text x="410" y="252" font-size="10.5" fill="#5D7789" font-family="system-ui">boşluk aşılınca kapanır</text>
</svg>
```

**Zaman anlayışı.** Doğru pencereyi bulmak için olayın **gerçekten olduğu** an gerekir. Buna **olay zamanı (event time)** denir ve Kafka Streams'in varsayılanı budur. Kaydın zaman damgası bir `TimestampExtractor` ile okunur. Damga mesajın gövdesine gömülüyse özel bir extractor yazılır. Böylece 9 saniye geç gelen bir olay bile doğru pencereye düşer.

Diğer iki seçenek yanıltır. **İşleme zamanı (processing time)** olayı yanlış, genellikle sonraki pencereye koyar. **Alım zamanı (ingestion time)** ise olayın broker'a yazıldığı anı yansıtır. Geç gelen olaylar için bir **erteleme payı (grace period)** tanımlanır; bu süre dolduktan sonra gelenler düşürülür.

```svg
<svg viewBox="0 0 640 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Olay 12:00:00'da gerçekleşti; event time doğru pencereye, processing time yanlış pencereye koyar">
  <defs>
    <marker id="kstream-time-ok" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Event time vs processing time</text>

  <line x1="40" y1="72" x2="600" y2="72" stroke="#5D7789" stroke-width="1.4" marker-end="url(#kstream-time-ok)"/>
  <circle cx="120" cy="72" r="5" fill="#25804C"/>
  <text x="120" y="58" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">gerçekleşti 12:00:00</text>
  <circle cx="420" cy="72" r="5" fill="#B26B00"/>
  <text x="420" y="58" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">ulaştı 12:00:09</text>
  <circle cx="472" cy="72" r="5" fill="#8A2E2E"/>
  <text x="512" y="92" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">işlendi 12:00:10</text>

  <rect x="40" y="118" width="270" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="175" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Event time (TimestampExtractor)</text>
  <text x="175" y="163" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ 12:00:00 penceresine düşer ✓</text>

  <rect x="330" y="118" width="270" height="62" rx="10" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="465" y="142" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">Processing time</text>
  <text x="465" y="163" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">→ yanlış (sonraki) pencereye düşer ✗</text>

  <text x="320" y="214" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Kafka Streams varsayılanı event time'dır; geç olaylar için grace period tanımlanır.</text>
</svg>
```

**Neden düz tüketici değil?** Streams üç şeyi hazır verir: durum yönetimini, akışı tarif eden hazır dili (DSL) ve **tam-bir-kez (exactly-once, EOS v2)** garantisini. Düz bir tüketici bunları kendiliğinden sağlamaz.

**ksqlDB** bunların üzerine oturan bir **SQL soyutlamasıdır**. Kod yazmadan akış işleme yapmanızı sağlar. Ancak karmaşık ve özel mantık gerektiğinde yine Streams DSL'ine veya Processor API'ye inilir. ksqlDB bir gösterge paneli (dashboard) aracı değildir ve Streams'in tümüyle yerine de geçmez.

### Mülakatta ne sorulur?

KStream mı KTable mı seçilir; ölçüt nedir (olay mı, güncelleme mi)? Durum deposu çöktüğünde kurtarma nasıl olur (changelog'un yeniden oynatılması)? Tumbling, hopping ve session pencerelerinin farkı nedir? Join'de neden `null` döner (eş-bölümleme) ve GlobalKTable'ın farkı nedir? Olay zamanı ile işleme zamanı arasındaki fark nedir? Streams'in düz tüketiciye üstünlüğü nedir ve ksqlDB nereye oturur?

### Tek cümlede

Kafka Streams akışı KStream (olay) ve KTable (son durum) olarak modeller, durumu changelog'la yedeklenen yerel bir depoda tutar, pencereleri **olay zamanına** göre kapatır ve tam-bir-kez garantisini hazır sunar.

# kstream-1 | İleri
## Soru
Bir ödeme sisteminde aynı `hesapId` anahtarıyla arka arkaya `+10`, `+20`, `-5` kayıtları geliyor. Bu veriyi **KStream** olarak mı yoksa **KTable** olarak mı modellemek gerektiğini şu ölçüte göre seçin: "Her kayıt geçmişte gerçekten olmuş, birbirini ezmeyen bir olay mı, yoksa aynı anahtarın güncel değerinin yeniden yazımı mı?"
## A
Birbirini ezmeyen bağımsız olaylar olduğu için KStream; KTable seçilseydi ara işlemler kaybolurdu
🤖 Doğru: append semantiği olan olaylar KStream; KTable upsert yapıp önceki değerleri ezerdi.
## B
KTable; çünkü aynı anahtar tekrar ettiğinde Kafka Streams her zaman en verimli olan tabloyu seçer
🤖 "Her zaman tablo" diye bir kural yok; tekrar eden anahtar tek başına KTable gerektirmez, semantik belirler.
## C
Fark yoktur; KStream ve KTable aynı topic'ten okunduğunda tümüyle özdeş davranır
🤖 Özdeş değil: KStream append, KTable upsert semantiği uygular; aynı topic'ten farklı sonuç üretirler.
## D
GlobalKTable; tüm ödeme olaylarının her uygulama örneğinde tam kopyası bulunmalı
🤖 GlobalKTable lookup/enrichment içindir; olay akışını modellemek için değil ve her örnekte tam kopya gereksiz maliyettir.
## Doğru
A
## Açıklama
Ölçüt olayların **doğasıdır**. `+10`, `+20`, `-5` birbirini ezmeyen, geçmişte olmuş bağımsız gerçeklerdir → **KStream** (append/olay semantiği). Aynı veriyi KTable olarak okusaydınız, KTable her kaydı anahtar başına **upsert** sayacağı için yalnızca son kayıt (`-5`) kalır, ara işlemler görünmez olurdu. Anahtarın tekrar etmesi (B) tek başına KTable gerektirmez; belirleyici olan kayıtların olay mı yoksa güncelleme mi olduğudur. Toplam bakiyeyi istiyorsanız KStream'i `groupByKey().reduce(...)` ile birleştirir, sonucu bir KTable olarak elde edersiniz.

# kstream-2 | İleri
## Soru
Kafka Streams uygulamanız RocksDB tabanlı bir **state store** kullanıyor. Çalıştığı sunucu çöküyor ve iş yükü tamamen boş bir makinedeki yeni bir örneğe (instance) taşınıyor. Durum deposu nasıl geri kazanılır?
## A
Geri kazanılamaz; state store yerel diskte tutulduğu için sunucu çökünce durum kalıcı olarak kaybolur
🤖 Kaybolmaz; state store'un changelog topic yedeği tam da bu senaryo için vardır.
## B
Kafka Streams, kaynak topic'in tamamını baştan yeniden işleyerek durumu sıfırdan hesaplar
🤖 Kaynak topic'i baştan işlemek gerekmez; changelog doğrudan durumu taşır, bu çok daha hızlı ve doğrudur.
## C
Yeni örnek, her state store'a eşlik eden changelog topic'i yeniden oynatarak (replay) RocksDB deposunu kurar
🤖 Doğru: her state store compacted bir changelog'a yedeklenir; yeni örnek onu replay ederek durumu geri yükler.
## D
Durum, ZooKeeper/KRaft meta veri katmanında saklandığından yeni örnek onu anında oradan okur
🤖 State store verisi meta veri katmanında tutulmaz; kurtarma changelog topic replay'i ile olur.
## Doğru
C
## Açıklama
Her **state store**, Kafka Streams tarafından otomatik olarak bir **changelog topic**'e (log-compacted, hatasız/fault-tolerant) yedeklenir. Yerel RocksDB deposu diskte kalıcı olmadığından (yeni makine, boş disk), yeni örnek atanan bölümlerin (partition) changelog'unu **replay** ederek deposunu yeniden inşa eder. Bu, kaynak topic'in tüm iş mantığını baştan çalıştırmaktan (B) çok daha hızlı ve doğrudur; çünkü changelog zaten hesaplanmış son durumu taşır. `num.standby.replicas` ile sıcak yedek (standby) örnekler tutarak bu geri yükleme süresini (ve failover kesintisini) daha da kısaltabilirsiniz.

# kstream-3 | Orta
## Soru
"Son **5 dakikada** her kullanıcının tıklama sayısı" gibi, sabit uzunlukta, çakışmayan ve bitişik zaman aralıklarında toplama (aggregate) yapmak istiyorsunuz. Hangi pencere (window) türü tam olarak buna uyar?
## A
Kayan pencere (sliding window)
🤖 Sliding window, olaylar arası zaman farkına göre kayan aralıklar üretir; sabit, çakışmayan bloklar tanımlamaz.
## B
Yuvarlanan pencere (tumbling window)
🤖 Doğru: tumbling, sabit boyutlu, çakışmayan ve bitişik pencerelerdir; "her 5 dakikalık blok" tam karşılığıdır.
## C
Zıplayan pencere (hopping window)
🤖 Hopping pencereler çakışabilir (hop < size); bitişik-çakışmayan blok istendiğinde uygun değildir.
## D
Oturum penceresi (session window)
🤖 Session window etkinlik boşluğuna (inactivity gap) göre dinamik uzar; sabit 5 dakikalık aralık vermez.
## Doğru
B
## Açıklama
**Tumbling window** (yuvarlanan pencere), sabit boyutlu, **çakışmayan** ve bitişik zaman bloklarıdır — "her 5 dakikalık dilim" tam olarak budur; adım (advance) = pencere boyutu. **Hopping window** (C) ilerleme adımı pencere boyutundan küçük olabildiği için **çakışır** (bir olay birden çok pencereye girer). **Sliding window** (A) olay çiftleri arasındaki zaman farkına göre tanımlanır (join/aggregate'te). **Session window** (D) sabit değildir; belirlenen **hareketsizlik boşluğu** (inactivity gap) aşılana dek büyür, kullanıcı oturumlarını modellemek için idealdir. Pencereli toplamalarda geç gelen olaylar için ayrıca bir **grace period** (erteleme payı) tanımlanır.

# kstream-4 | İleri
## Soru
Bir `siparisler` **KStream**'ini bir `musteriler` **KTable**'ı ile `musteriId` üzerinden join etmek istiyorsunuz. Uygulama çalışınca beklenen eşleşmelerin çoğunun boş (null) döndüğünü görüyorsunuz. En olası neden ve KTable ile GlobalKTable join arasındaki temel fark nedir?
## A
KStream–KTable join Kafka Streams'te hiçbir zaman desteklenmez; yalnızca KStream–KStream join vardır
🤖 KStream–KTable join tam da bu zenginleştirme senaryosu için vardır; desteklenmiyor demek yanlış.
## B
Sorun anahtarların büyük/küçük harf duyarlılığıdır; eş-bölümleme join doğruluğunu hiç etkilemez
🤖 Asıl mekanik neden co-partitioning eksikliğidir; bölüm hizası bozuksa doğru anahtar bile farklı bölümde kalır.
## C
GlobalKTable da KTable ile aynı co-partitioning şartını dayatır; ikisi arasında join açısından fark yoktur
🤖 GlobalKTable co-partitioning gerektirmez; fark tam da budur, bu yüzden lookup join için tercih edilir.
## D
KTable join eş-bölümleme (co-partitioning) ister; GlobalKTable tam kopya tuttuğu için bu şartı aramaz
🤖 Doğru: KTable join co-partitioning ister; sağlanmazsa eşleşmeler kaçar. GlobalKTable her örnekte tam kopya tuttuğundan co-partitioning gerekmez.
## Doğru
D
## Açıklama
**KStream–KTable** join'i **eş-bölümleme** (co-partitioning) şartına tabidir: iki tarafın **aynı sayıda bölüme** sahip olması ve **aynı anahtarla** bölümlenmesi gerekir; aksi halde eşleşen anahtarlar farklı bölümlere düşer ve aynı görev (task) onları hiç bir araya getiremez → çoğu eşleşme **null**. Çözüm ya tarafları aynı anahtarla yeniden bölümlemek (`repartition`/`selectKey`) ya da bölüm sayılarını hizalamaktır. **GlobalKTable**, tablonun **tam kopyasını her örnekte** tuttuğu için co-partitioning **gerektirmez** ve join anahtarı KStream anahtarından farklı olabilir (bir `KeyValueMapper` ile seçilir) — küçük, sık okunan referans/lookup tabloları için idealdir. Bedeli: her örnekte tüm veriyi tutmanın bellek maliyeti.

# kstream-5 | İleri
## Soru
Bir olay saat 12:00:00'da **gerçekleşti** ama ağ gecikmesi yüzünden Kafka Streams'e 12:00:09'da **ulaştı** ve 12:00:10'da **işlendi**. Pencereli (windowed) toplamaların doğru pencereye düşmesi için hangi zaman anlayışını (time semantics) kullanmalısınız ve bunu nasıl sağlarsınız?
## A
İşleme zamanı (processing time); olay ne zaman işlendiyse o pencereye koymak en doğru sonucu verir
🤖 Processing time geç gelen olayı yanlış (sonraki) pencereye koyar; olayın gerçekleştiği zamanı yansıtmaz.
## B
Alım zamanı (ingestion time); broker'ın kaydı yazdığı an olayın gerçek zamanına en yakın ölçüdür
🤖 Ingestion time olayın gerçekleşme anını değil, broker'a yazılma anını yansıtır; gecikmede sapar.
## C
Olay zamanı (event time); gömülü zaman damgasını bir `TimestampExtractor` ile okuyup pencereyi ona göre kurmak
🤖 Doğru: event time olayın gerçek anını kullanır; TimestampExtractor damgayı okur, olay doğru pencereye düşer.
## D
Zaman anlayışının hiçbir önemi yoktur; Kafka Streams tüm pencereleri sistem duvar saatine (wall clock) göre kapatır
🤖 Kafka Streams varsayılan olarak event time kullanır ve pencereleme buna dayanır; duvar saati zorunlu değildir.
## Doğru
C
## Açıklama
Pencerelemenin doğru olması için **olay zamanı** (event time) gerekir: olayın **gerçekleştiği** an (12:00:00), işlendiği an değil. Kafka Streams varsayılan olarak kaydın zaman damgasını bir **`TimestampExtractor`** ile okur; damga payload'a gömülüyse özel bir extractor ile onu çıkarırsınız. Böylece olay, işleme 10 saniye gecikse bile **12:00:00** penceresine düşer. **Processing time** (A) olayı yanlış/sonraki pencereye koyar, **ingestion time** (B) broker yazım anını yansıtır — ikisi de gerçek olay anını kaçırır. Geç gelen olayların kapanmış pencerelere dahil edilebilmesi için ayrıca bir **grace period** (erteleme payı) tanımlanır; bu süre dolduktan sonra gelenler düşürülür.

# kstream-6 | Orta
## Soru
Bir ekip, birden fazla topic'ten okuyup pencereli toplama ve join yaparak durum tutan, üstelik **tam-bir-kez** (exactly-once) garanti isteyen bir işlem hattı kuracak. Düz consumer/producer API'si yerine **Kafka Streams** kullanmanın temel gerekçesi nedir; ve **ksqlDB** ne zaman devreye girer?
## A
Kafka Streams durum yönetimini, pencereleme/join DSL'ini ve EOS v2 semantiğini hazır sunar; ksqlDB aynı işleri SQL ile ifade eder
🤖 Doğru: Streams durum + DSL + EOS'u hazır verir; ksqlDB bunları SQL ile ifade eden üst katmandır.
## B
Kafka Streams ayrı bir işlem kümesi (cluster) gerektirir ve tek avantajı daha hızlı ağ katmanıdır; ksqlDB ise yalnızca gösterge paneli çizmek içindir
🤖 Streams ayrı küme gerektirmez (uygulama içi kütüphanedir) ve avantajı ağ hızı değil durum/DSL/EOS'tur; ksqlDB dashboard aracı değildir.
## C
Düz consumer/producer da state store, changelog ve exactly-once'ı otomatik sağladığı için Kafka Streams'in hiçbir ek faydası yoktur
🤖 Düz consumer bunları otomatik vermez; durumu, changelog'u ve EOS koordinasyonunu elle kurmak gerekir — Streams'in faydası tam buradadır.
## D
ksqlDB, Kafka Streams'in yerini tümüyle alır ve karmaşık özel işlemler dahil her senaryoda onun yerine kullanılmalıdır
🤖 ksqlDB Streams üzerine kuruludur ama karmaşık/özel mantıkta Streams (veya Processor API) hâlâ gerekir; tümüyle ikame etmez.
## Doğru
A
## Açıklama
**Kafka Streams**'in düz consumer/producer'a üstünlüğü, dağıtık akış işlemenin zor kısımlarını **hazır** vermesidir: yerel **durum yönetimi** (RocksDB state store + otomatik changelog yedeği), pencereleme/join/aggregate için yüksek seviyeli **DSL**, ve **tam-bir-kez** (exactly-once, EOS v2) işleme garantisi. Bunları düz consumer ile elle kurmak (offset+durum+üretim atomikliğini koordine etmek) hataya çok açıktır. Kafka Streams ayrı bir küme değildir; uygulamanızın içinde çalışan bir kütüphanedir. **ksqlDB** bu yeteneklerin üzerine oturan bir **SQL soyutlamasıdır**: `CREATE STREAM/TABLE ... SELECT ...` ile kod yazmadan, operasyonel olarak daha kolay akış işleme sağlar — ama gerçekten karmaşık, özel mantık gerektiğinde yine Kafka Streams DSL veya alt seviye Processor API'ye inilir.
