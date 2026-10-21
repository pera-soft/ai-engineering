---
slug: ddia-veri-yogun
baslik: "DDIA Desenleri: LSM-tree vs B-tree, Replikasyon/Partition, Batch/Stream ve Türetilmiş Veri"
kademe: Uzman
sira: 194
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

*Designing Data-Intensive Applications* (DDIA, Martin Kleppmann) veri-yoğun uygulamaların el kitabıdır. "Veri-yoğun" demek, darboğazın işlemci gücü değil **verinin miktarı, karmaşıklığı ve değişim hızı** olması demektir. Mülakattaki karşılığı şudur: tek doğru cevaplı bir soru sorulmaz; hangi ödünü (trade-off) neden seçtiğini anlatabiliyor musun, ona bakılır.

### Temel kavramlar

- **Düğüm (node):** veriyi tutan tek bir makine; sistem genelde birçok düğümden oluşur.
- **Depolama motoru (storage engine):** veri tabanının veriyi diske yazma ve okuma iç mekanizması. İki aile: **B-tree** ve **LSM-tree**.
- **Replikasyon (replication):** aynı veriyi birden çok düğümde kopya tutmak — fotokopi gibi. Amaç: bir makine ölünce veri kaybolmasın, okuma yükü dağılsın. Üç düzen: **tek-lider** (yazma tek düğüme gider, ötekiler takipçi/follower), **çok-lider** (birden çok yazma noktası, çakışma çözümü ister), **lidersiz** (çoğunluk/quorum kuralıyla).
- **Replikasyon gecikmesi (lag):** kopyanın lideri geriden takip edip bir süre eski veriyi göstermesi.
- **Tutarlılık (consistency):** sistemin her yerinden çelişkisiz cevap alabilme garantisi. İki tanıdık garanti: **read-your-writes** (kendi yazdığını mutlaka gör) ve **monotonic reads** (okuma zamanda geri gitmesin).
- **Bölümleme / partition (sharding):** veriyi parçalara bölüp farklı düğümlere dağıtmak — kütüphaneyi raflara ayırmak gibi.
- **ACID:** Atomiklik (ya hep ya hiç), Tutarlılık, İzolasyon, Dayanıklılık. Uyarı: satıcı "ACID" dese de izolasyon çoğu kez serializable değil, read committed veya snapshot'tır; lost update ve write skew anomalileri hâlâ mümkündür.
- **Kayıt sistemi (system of record):** gerçeğin tek kaynağı. **Türetilmiş veri:** ondan üretilen indeks, önbellek, arama motoru — silinse yeniden kurulur. **CDC:** kayıt sistemindeki değişiklikleri sıralı akış olarak dışarı vermek.

### Nasıl çalışır?

**Depolama motorları.** **B-tree** sayfaları (page) diskte **yerinde günceller**: dengeli bir ağaç gezilir, ilgili sayfa bulunur, üstüne yazılır (önce WAL'e kayıt düşülür). Okuma öngörülebilir, aralık (range) sorgusu güçlüdür; bedeli her yazmanın rastgele disk I/O'su olmasıdır. **LSM-tree** tersini yapar: yazmalar önce bellekteki `memtable`'a girer, dolunca diske **sıralı** bir dosya (**SSTable**) olarak dökülür; arka planda **compaction** bu dosyaları birleştirir. Sıralı yazma diskin en verimli modudur, yazma verimi yüksektir. Ödünü çift yönlüdür: bir anahtar birden çok SSTable'da bulunabildiğinden okuma birkaç dosyaya bakar ve compaction sürekli CPU/disk harcar — yazma büyümesi (write amplification) yok olmaz, yalnız biçim değiştirir.

```svg
<svg viewBox="0 0 640 208" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="LSM-tree ve B-tree karşılaştırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki depolama motoru, iki optimizasyon</text>
  <rect x="28" y="42" width="280" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">LSM-tree (yazma-optimize)</text>
  <text x="168" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">memtable → SSTable (sıralı flush)</text>
  <text x="168" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">compaction ile birleşir</text>
  <text x="168" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ yüksek yazma verimi (append)</text>
  <text x="168" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">− okuma çok SSTable'a bakar</text>
  <text x="168" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">→ bloom filter ile hızlandır</text>
  <rect x="332" y="42" width="280" height="150" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">B-tree (okuma-optimize)</text>
  <text x="472" y="90" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">sayfa (page) yerinde güncellenir</text>
  <text x="472" y="110" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">dengeli ağaç · WAL</text>
  <text x="472" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ güçlü range sorgusu</text>
  <text x="472" y="150" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ öngörülebilir okuma gecikmesi</text>
  <text x="472" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">− rastgele yazma I/O</text>
</svg>
```

**Okuma yolu ve bloom filter.** LSM'de bir anahtar aranırken önce `memtable`, sonra SSTable'lar yeniden eskiye taranır. Var olmayan bir anahtar en pahalı durumdur — çünkü hepsine bakmak gerekir. Bunu **bloom filter** çözer: her SSTable için tutulan olasılıksal üyelik yapısı "burada **kesinlikle yok**" (asla yanlış-negatif) ya da "belki var" (küçük yanlış-pozitif payı) der. "Kesinlikle yok" cevabı o dosyanın disk okumasını tümden atlatır. SSTable'ın min/max anahtar aralığına bakmak da gerçek bir optimizasyondur ama tek başına yetmez; compaction ise sorgu yolunda değil, arka planda çalışır.

**Replikasyon gecikmesi.** Tek-liderli, **eş-zamansız (async)** replikasyonda lider yazmayı onaylar, kopyalara gecikmeli yayar. Kullanıcı profilini günceller, sayfayı yeniler ve okuma geride kalmış bir takipçiye düşerse kendi yazdığını göremez. Bu bir bozulma ya da önbellek sorunu değil, beklenen bir anomalidir. Çözüm her şeyi senkron yapmak değil (yavaşlatır), hedefli bir garantidir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Async replikasyon lag ve read-your-writes çözümü">
  <defs>
    <marker id="ddiad-a" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#25804C"/></marker>
    <marker id="ddiad-b" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#8A2E2E"/></marker>
    <marker id="ddiad-c" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Async replikasyon: kendi yazını okuyamama (lag)</text>
  <rect x="60" y="50" width="180" height="62" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="150" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Lider</text>
  <text x="150" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yazma buraya (v2)</text>
  <rect x="400" y="50" width="180" height="62" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="490" y="76" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Follower (geride)</text>
  <text x="490" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hâlâ eski (v1)</text>
  <line x1="240" y1="81" x2="396" y2="81" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="5 4" marker-end="url(#ddiad-c)"/>
  <text x="318" y="73" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">async (gecikmeli)</text>
  <line x1="150" y1="150" x2="150" y2="116" stroke="#25804C" stroke-width="1.5" marker-end="url(#ddiad-a)"/>
  <text x="150" y="166" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">kullanıcı yazar → v2</text>
  <line x1="490" y1="150" x2="490" y2="116" stroke="#8A2E2E" stroke-width="1.5" marker-end="url(#ddiad-b)"/>
  <text x="490" y="166" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">okur → v1 (eski) görür</text>
  <rect x="60" y="178" width="520" height="26" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="320" y="195" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">read-your-writes: kendi yazdığın veriyi liderden / yeterince güncel replikadan oku</text>
</svg>
```

**Bölümleme (partition).** Tablo büyüyünce tek düğüme sığmaz, parçalara ayrılır. **Özet tabanlı bölümleme (hash partition)** anahtarı bir özet fonksiyonundan geçirir ve parçayı ona göre seçer. Çarpık ya da ardışık anahtarlar bile eşit dağılır; **sıcak nokta** (hot spot) riski düşer. Bedeli şudur: mantıksal komşu anahtarlar farklı parçalara serpilir, bu yüzden bir aralık sorgusu tüm parçalara sormak zorunda kalır. **Aralık tabanlı bölümleme (range partition)** ise anahtarları sıralı aralıklara böler. Aralık sorgusu tek ya da az sayıda parçaya iner. Buna karşılık sürekli artan bir anahtar — zaman damgası, otomatik id — bütün yazmayı son parçaya yığar. İkisi de "aynı şey" değildir; pratik çözüm bileşik anahtardır (`bolge#zaman`).

**Değişmez log, batch ve stream.** Buradaki "log" hata ayıklama günlüğü değil, **sona eklemeli (append-only), değişmez bir olay dizisidir** — Kafka'nın konusu, veri tabanının commit/replikasyon log'u. Üstüne yazılmaz; yalnız sona eklenir. Bu tek soyutlama iki dünyayı birleştirir: **batch** log'un tamamını topluca işler, **stream** ucuna eklenen yeni olayları geldikçe işler. Fark doğada değil, "ne kadarını ne zaman" işlediğindedir. Değişmezlik iki süper güç verir: türetilmiş görünümler log **yeniden işlenerek** (reprocessing) sıfırdan kurulabilir; hata yapılırsa geçmiş silinmez, düzeltici olay eklenir (denetlenebilirlik).

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kayıt sistemi ve CDC ile türetilmiş veri">
  <defs>
    <marker id="ddiad-f" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek yazma yolu: kayıt sistemi + CDC ile türet</text>
  <rect x="24" y="72" width="150" height="64" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="99" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">PostgreSQL</text>
  <text x="99" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kayıt sistemi</text>
  <rect x="238" y="72" width="150" height="64" rx="12" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="313" y="98" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">CDC / log</text>
  <text x="313" y="118" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">değişmez olay akışı</text>
  <rect x="452" y="44" width="164" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="534" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Elasticsearch</text>
  <text x="534" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">türetilmiş (arama)</text>
  <rect x="452" y="112" width="164" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="534" y="134" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Redis</text>
  <text x="534" y="152" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">türetilmiş (önbellek)</text>
  <line x1="174" y1="104" x2="234" y2="104" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ddiad-f)"/>
  <line x1="388" y1="100" x2="448" y2="72" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ddiad-f)"/>
  <line x1="388" y1="108" x2="448" y2="136" stroke="#5D7789" stroke-width="1.5" marker-end="url(#ddiad-f)"/>
  <text x="320" y="186" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">türetilmiş sistemler her an log'dan yeniden kurulabilir (dual-write yok)</text>
</svg>
```

**Tek yazma yolu.** Uygulamanın aynı anda hem veri tabanına hem arama motoruna hem önbelleğe yazmasına **çift yazma** (dual write) denir. Sinsi bir tutarsızlık kaynağıdır. Dağıtık atomiklik olmadığı için, ikinci yazma başarısız olursa ya da sıra karışırsa sistemler **kalıcı** olarak ayrışır.

DDIA'nın reçetesi şudur. Bir **kayıt sistemi** seç, örneğin PostgreSQL. Geri kalan her şeyi **türetilmiş veri** say. Onları da kayıt sisteminin **değişiklik yakalama** (CDC — change data capture) akışından besle; örneğin Debezium ile yazma öncesi günlüğünden (WAL) Kafka'ya. Böylece tek bir yazma yolu olur. Akış idempotent kalır, yani tekrar işlense de sonucu değişmez; gerekirse yeniden işlenebilir. Arama indeksi bozulursa günlükten yeniden kurulur. Ters kurgu — arama motorunu birincil yapmak — ya da gece işiyle elle mutabakat, kırılgan ve ölçeksizdir.

### Mülakatta ne sorulur?

Sorulanlar şunlardır. Yazma ağırlıklı yükte LSM mi B-tree mi seçilir ve ödünü nedir? "Kullanıcı kendi güncellemesini göremiyor" vakasının teşhisi (replikasyon gecikmesi) ve tedavisi (kendi yazdığını okuma — read-your-writes) nedir? Özet ile aralık bölümlemenin karşılıklı bedeli nedir? Bloom filtresinin okuma yolundaki rolü nedir? Değişmez günlük, toplu işi (batch) ve akışı (stream) neden birleştirir? PostgreSQL + Elasticsearch + Redis üçlüsünü tutarlı tutmanın yolu nedir (kayıt sistemi + CDC, çift yazma değil)? Zayıf cevap teknoloji adı sayar; güçlü cevap ödünü söyler.

### Tek cümlede

DDIA'nın özü, her seçimde "neyi kazanıp neyi kaybediyorum" sorusunu açıkça yanıtlamak ve sistemi tek bir kayıt sistemi üzerine, türetilmiş veriyi yeniden kurulabilir biçimde oturtmaktır.

# ddiad-1 | İleri
## Soru
Yazma-ağırlıklı bir zaman-serisi/telemetri iş yükü için depolama motoru seçiyorsun. LSM-tree'yi B-tree'ye tercih etmenin temel gerekçesi ve ödünü (trade-off) nedir?
## A
LSM-tree okuma-optimizedir; tekil anahtar okuması B-tree'den her zaman hızlıdır çünkü aranan veri hep bellekteki `memtable`'da bulunur ve diske hiç inilmez
🤖 Ters: LSM yazma-optimizedir; okuma birden çok SSTable'a bakabildiği için tekil okumada B-tree'den avantajlı değildir.
## B
İki motorun performans profili aynıdır; fark yalnız arayüzdedir, seçim ekibin alışkanlığına ve ürünün lisansına bağlıdır
🤖 Profiller belirgin biçimde farklıdır (yazma vs okuma optimizasyonu); seçim teknik bir ödündür.
## C
Yazmaları bellekte toplayıp diske sıralı döktüğü için rastgele yazma I/O'sunu keser; ödünü çok SSTable'a bakan okuma ve compaction yüküdür
🤖 Doğru: sıralı yazma kazancı, okuma ve compaction maliyeti karşılığında alınır.
## D
LSM-tree compaction sayesinde yazma büyümesini (write amplification) tümden ortadan kaldırır; bu yüzden ödünsüz bir kazançtır
🤖 Compaction yazma büyümesini yok etmez, biçim değiştirir: arka planda sürekli CPU ve disk harcar.
## Doğru
C
## Açıklama
**B-tree** sayfaları **yerinde günceller** — her yazma, ilgili disk sayfasına (çoğu kez rastgele konumda) yazma ve önce WAL'e kayıt demektir; bu okuma için mükemmel (öngörülebilir, tek ağaç gezintisi) ama yazma-yoğun yükte rastgele I/O darboğazı yaratır. **LSM-tree** yazmaları `memtable`'da toplar, dolunca diske **sıralı bir SSTable** olarak döker; sıralı yazma diskin (SSD dahil) en verimli modudur, dolayısıyla yazma verimi yüksektir. Bedel: bir anahtar farklı SSTable'larda olabileceğinden okuma birkaç dosyaya bakar (bunu **bloom filter** ile "bu SSTable'da yok" diyerek hızlandırırsın) ve **compaction** sürekli CPU/disk harcar. A okuma/yazma optimizasyonunu ters çevirir, D compaction'ın bedelini yok sayar (write amplification kaybolmaz, biçim değiştirir), B ise ödünün varlığını inkâr eder.

# ddiad-2 | Uzman
## Soru
Lider-tabanlı, **eş-zamansız** (async) replikasyonda bir kullanıcı profilini güncelledi, hemen ardından sayfayı yeniledi ve **eski** değerini gördü. Kök neden ve bunu çözecek tutarlılık garantisi nedir?
## A
Lider düğüm çökmüş olmalı; tek çözüm tüm replikasyonu senkron (sync) yapmaktır, böylece hiçbir takipçi asla geri kalmaz
🤖 Lider çökmesi şart değil; asıl neden geride kalmış takipçiden okumaktır ve tam-senkron aşırı/pahalı bir çözümdür.
## B
Veri tabanı bozulmuş; takipçileri sıfırdan kurup onarım (repair) çalıştırmak gerekir, yoksa kayıp kalıcı olur
🤖 Bozulma değil, beklenen bir tutarlılık anomalisi (replikasyon gecikmesi) söz konusudur.
## C
Uygulama önbelleği (cache) eski değeri döndürüyor; önbellek ömrünü (TTL) düşürmek sorunu kalıcı olarak çözer
🤖 Belirti önbellek gibi görünse de kök neden replikasyon gecikmesidir; TTL bunu güvenilir çözmez.
## D
Yazma lidere gitti ama okuma geride kalmış bir takipçiden yapıldı (replikasyon gecikmesi); çözüm read-your-writes tutarlılığıdır
🤖 Doğru: klasik replikasyon gecikmesi anomalisi; read-your-writes ile giderilir.
## Doğru
D
## Açıklama
Eş-zamansız replikasyonda lider yazmayı onayladıktan sonra follower'lara **gecikmeli** yayılır (**replication lag**). Kullanıcı lidere yazıp, yenilemede lag'li bir follower'dan okursa kendi yazdığını göremez. Bu bir bozulma değil, sistemin bilinen bir davranışıdır. Çözüm **read-your-writes consistency** (okuma-kendi-yazını): kullanıcının kendi yazabileceği veriyi (profili) okurken ya doğrudan liderden oku, ya da "bu kullanıcının son yazma zaman-damgasından daha güncel bir replikadan oku" kuralını uygula. İlgili garantiler: `monotonic reads` (zamanda geri gitmeme). A (aşırı çözüm), B (yanlış teşhis), C (belirtiyi neden sanma) kök nedeni ıskalar.

# ddiad-3 | İleri
## Soru
Büyük bir tabloyu partition'lara (shard) bölerken **hash** partition ile **range** partition arasındaki temel ödünü hangisi doğru anlatır?
## A
Hash partition komşu anahtarları yan yana tuttuğu için aralık sorgusunu hızlandırır; range partition ise anahtarları dağıtıp yükü düzler
🤖 Tam ters: hash yükü dağıtır ama aralık sorgusunu böler; range sorguyu kolaylaştırır ama sıcak-nokta riski taşır.
## B
Hash yükü ve sıcak-noktayı düzler ama komşu anahtarları dağıttığı için aralık sorgusunu verimsiz kılar; range partition tersini yapar
🤖 Doğru: dağıtım ile aralık-sorgu verimliliği arasındaki klasik ödün.
## C
İki yöntem de aynı fiziksel yerleşimi üretir; partition anahtarını seçmek yalnız isimlendirme meselesidir, erişim desenini etkilemez
🤖 Yöntemler farklı erişim desenleri için optimize eder; anahtar seçimi kritiktir.
## D
Range partition her zaman hash'ten üstündür; sıralı okumaya izin verdiği için hem sorgu hem yazma dağılımı daha iyidir
🤖 Range'in aralık-okuma avantajı vardır ama artan anahtarda sıcak-nokta yaratır; "her zaman üstün" yanlıştır.
## Doğru
B
## Açıklama
**Hash partition**: anahtarı bir özet (hash) fonksiyonundan geçirip sonuca göre partition seçer. Bu, ardışık veya çarpık anahtarları bile eşit dağıtır — **sıcak-nokta** riskini düşürür — ama mantıksal olarak komşu anahtarlar (örn. belirli tarih aralığı) farklı partition'lara dağıldığından bir **range sorgusu** tüm partition'lara sormak (scatter-gather) zorunda kalır. **Range partition**: anahtarları sıralı aralıklara böler; range sorgusu tek/az partition'a iner (verimli), ama monotonik artan anahtar (timestamp, auto-increment id) hep son partition'a yazma yığar — **hot spot**. Pratikte çözüm bileşik anahtar/salt (örn. `region#timestamp`) ile ikisini dengelemektir. A ters, C farkı yok sayar, D tek yönlü genelleme yapar.

# ddiad-4 | Uzman
## Soru
DDIA'nın "değişmez log" (immutable, append-only log) fikri neden batch ve stream işlemenin ortak temeli sayılır?
## A
Log değişmez ve sıralı bir olay dizisidir; batch tümünü, stream ucuna geleni işler ve türetilmiş görünümler log'dan yeniden kurulabilir
🤖 Doğru: değişmez log, batch ile stream'i birleştirir ve türetilmiş veriyi yeniden-üretilebilir kılar.
## B
Sona eklemeli log her yazmada eski kaydın üstüne yazar; sağladığı depolama tasarrufu yüzünden iki işleme biçimi de onu tercih eder
🤖 Tersine, sona eklemeli log üzerine yazmaz; değeri değişmezliğinden gelir, tasarruftan değil.
## C
Batch ve stream birbirinden bağımsız dünyalardır; log yalnız batch işlemenin girdisidir, stream ise doğrudan veri tabanını dinler
🤖 Log tam da ikisinin ortak soyutlamasıdır; stream de aynı log'un tüketicisidir.
## D
Buradaki log uygulamanın hata ayıklama günlüğüdür; veri akışında değil yalnız sorun çözmede rol oynar
🤖 Buradaki "log" uygulama günlüğü değil, olay/commit log'u — veri akışının çekirdeğidir.
## Doğru
A
## Açıklama
DDIA'nın birleştirici içgörüsü: **değişmez, sıralı bir olay log'u** (Kafka'nın konusu, veri tabanının commit/replikasyon log'u) hem batch hem stream işlemenin ortak soyutlamasıdır. **Batch**, log'un (veya onun bir anlık kopyasının) tamamını topluca işler; **stream**, aynı log'un ucuna eklenen yeni olayları geldikçe işler — ikisi arasındaki fark "log'un ne kadarını, ne zaman" işlediğindir, doğası değil. Değişmezlik iki süper güç verir: (1) türetilmiş görünümler (arama indeksi, önbellek, materialized view) log'u baştan **yeniden işleyerek** yeniden kurulabilir; (2) hata yaparsan log'u değiştirmeden düzeltici olay eklersin (denetlenebilirlik/auditability). B, C, D "log" kavramını (üzerine-yazma, ilgisizlik, sadece-debug) yanlış tanımlar.

# ddiad-5 | Uzman
## Soru
Bir sistemde PostgreSQL ana veriyi tutuyor; Elasticsearch arama, Redis önbellek görevi görüyor. Bu üçünü tutarlı tutmanın DDIA'ya uygun (sağlam) yolu nedir?
## A
Uygulama kodunda her yazmada üç sisteme de sırayla yaz; üçü de aynı istek içinde güncellendiği için tutarlılık garanti olur
🤖 Çift/üçlü yazma (dual write) atomik değildir; biri başarısız olursa sistemler kalıcı olarak tutarsızlaşır.
## B
Elasticsearch'ü birincil (primary) yapıp PostgreSQL'i ondan türet; arama motoru zaten en zengin ve en güncel veri modelini tutar
🤖 Ters kurgu: arama/önbellek türetilmiş veridir, kayıt sistemi olmaya uygun değildir.
## C
Tutarlılık garanti edilemez; gece çalışan bir işle üç sistemi karşılaştırıp farkları elle düzeltmek tek gerçekçi yoldur
🤖 Elle mutabakat kırılgan ve ölçeksizdir; asıl çözüm tek yazma yolu ve CDC ile türetmedir.
## D
PostgreSQL tek kayıt sistemi olsun; Elasticsearch ve Redis türetilmiş veri olarak CDC akışından beslensin, her an yeniden kurulabilsin
🤖 Doğru: tek gerçek kaynağı + CDC, dual-write yerine idempotent ve yeniden-üretilebilir akış verir.
## Doğru
D
## Açıklama
**Çift yazma** (dual write) — uygulamanın aynı anda hem DB'ye hem arama/önbelleğe yazması — dağıtık atomiklik olmadığından sinsi bir tutarsızlık kaynağıdır: ikinci yazma başarısız olur veya sıralama (race) bozulursa sistemler **kalıcı** olarak ayrışır. DDIA'nın reçetesi: **tek bir kayıt sistemi** (system of record — burada PostgreSQL) seç; diğer her şey **türetilmiş veridir** (derived data). Türetilmiş sistemleri, kayıt sisteminin sıralı değişiklik akışından — **CDC** (ör. Debezium ile WAL'i okuyup Kafka'ya) — besle. Böylece tek yazma yolu vardır, akış **idempotent** ve **yeniden işlenebilirdir** (Elasticsearch indeksini bozarsan log'dan yeniden kurarsın). A dual-write tuzağı, B kayıt-sistemi seçimini ters çevirir, C yeniden-üretilebilir akış yerine elle mutabakata düşer.

# ddiad-6 | İleri
## Soru
LSM-tabanlı bir depoda (örn. Cassandra/RocksDB) var-olmayan bir anahtar için `get` isteği geldi. **Okuma yolunun** (read path) bu isteği verimli reddetmesini sağlayan mekanizma nedir?
## A
Okuma yolu her SSTable'ı baştan sona tarar; var-olmayan anahtar en hızlı yanıtlanan durumdur çünkü karşılaştırılacak değer bulunmaz
🤖 Ters: filtre olmadan tüm SSTable'ları taramak var-olmayan anahtar için en pahalı senaryodur.
## B
Önce `memtable`'a, sonra her SSTable'ın bloom filter'ına bakılır; "kesinlikle yok" yanıtı o dosyanın disk okumasını atlatır
🤖 Doğru: bloom filter negatif sorguda gereksiz disk I/O'sunu keser (olası yanlış-pozitif, asla yanlış-negatif).
## C
Her SSTable'ın yalnız min/max anahtar aralığına bakılır; anahtar aralığın dışındaysa dosya atlanır ve bu tek başına yeterlidir
🤖 Aralık kontrolü gerçek bir optimizasyondur ama aralık içine düşen anahtarları eleyemez; asıl eleme bloom filter ile olur.
## D
Okuma yolu önce compaction'ı tetikler; tüm SSTable'lar tek dosyada birleştikten sonra tek bakışta yanıt verilir
🤖 Okuma compaction tetiklemez; compaction arka plan işidir, sorgu yolunda değildir.
## Doğru
B
## Açıklama
LSM okuma yolu katman katmandır: önce bellekteki **memtable** (en yeni yazmalar), sonra diskteki SSTable'lar en yeniden en eskiye. Bir anahtar hiç yoksa, filtre olmadan tüm SSTable'ları okumak gerekirdi — negatif sorgu en pahalı hâle gelirdi. **Bloom filter** her SSTable için tutulan olasılıksal bir üyelik yapısıdır: "anahtar burada **kesinlikle yok**" (asla yanlış-negatif) ya da "**belki var**" (küçük yanlış-pozitif olasılığı) der. "Kesinlikle yok" yanıtı o SSTable'ın disk okumasını tümden atlatır; böylece var-olmayan anahtar, birkaç bellek-içi bit kontrolüyle elenir. Yanlış-pozitif nadiren gereksiz bir disk okumasına yol açar, ama **doğruluğu bozmaz** (sonra gerçek arama yapılır). C'deki min/max anahtar aralığı kontrolü gerçek ve yararlı bir optimizasyondur, ama aralığa düşen anahtarları eleyemediği için tek başına yetmez; A okuma yolunun maliyetini ters çevirir, D ise compaction'ı sorgu yoluna sokar.
