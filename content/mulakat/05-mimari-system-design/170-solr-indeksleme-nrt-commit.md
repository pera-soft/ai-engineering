---
slug: solr-indeksleme-nrt-commit
baslik: "Solr İndeksleme: hard vs soft commit (NRT), commit storm, _version_"
kademe: Uzman
sira: 170
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir alışveriş sitesinde arama kutusuna "kırmızı koşu ayakkabısı" yazarsın ve sonuçlar bir saniyeden kısa sürede gelir. Bu işi yapan yazılıma **arama motoru** (search engine) denir; Apache Solr böyle bir arama motorudur.

Asıl mesele iki sorudur. Mağazaya yeni bir ürün eklendiğinde o ürün aramada **ne zaman görünür**? Sunucu tam o anda çökerse ürün **kalıcı** mıdır? Solr'da indeksleme ve commit konusunun tamamı bu iki sorunun cevabıdır. Yanlış ayar üretimde üç biçimde geri döner: "kullanıcı yeni ürünü göremiyor", "sunucu çöktü, veri uçtu" ya da "arama sunucusu CPU'yu tavan yaptı, cevap vermiyor".

### Temel kavramlar

- **Belge (document)**: Arama motorunun sakladığı tek bir kayıt — bir ürün, bir haber, bir CV. Üstünde kutucuklar olan bir fiş gibi düşün.
- **Alan (field)**: Belgenin üstündeki tek bir kutucuk: başlık, fiyat, açıklama.
- **Terim (term)**: Metin parçalara ayrıldığında ortaya çıkan tek tek kelimeler. "kırmızı koşu ayakkabısı" → kırmızı, koşu, ayakkabı.
- **İndeks (index)**: Kitabın arkasındaki dizin gibidir. Dizin "kırmızı → sayfa 12, 40, 91" der; arama motoru da "kırmızı → belge 12, 40, 91" listesini tutar. Arama bu yüzden tüm belgeleri baştan sona okumadan yapılır.
- **Sorgu (query)**: Kullanıcının yazdığı arama. Motor sorguyu terimlere böler, dizinden listeleri alır, kesiştirir ve sıralar.
- **Segment**: İndeks tek bir dosya değil, **değişmez** (immutable) parçalardan oluşur. Yeni belgeler yeni parçalara yazılır, eski parça hiç değiştirilmez.
- **Searcher**: İndeksin belli bir andaki fotoğrafı. Sorgular searcher'a sorulur; searcher açıldıktan sonra eklenen belgeleri göremez.
- **Commit**: "Şu ana kadar yazdıklarımı işle" komutu. İki farklı türü ve iki farklı garantisi vardır — mülakatın kalbi burasıdır.

### Nasıl çalışır?

Belge Solr'a gönderildiğinde önce **transaction log**'a (tlog — yazma günlüğü) ve belleğe alınır; henüz aranabilir değildir, çünkü açık searcher onu görmez.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hard commit dayanıklılık, soft commit görünürlük">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki commit, iki garanti</text>
  <rect x="30" y="46" width="270" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Hard commit</text>
  <text x="165" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Diske kalıcı (fsync)</text>
  <text x="165" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Garanti: DAYANIKLILIK</text>
  <text x="165" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Pahalı → seyrek + openSearcher=false</text>
  <text x="165" y="154" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çökme sonrası veri kaybı yok</text>
  <rect x="340" y="46" width="270" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="72" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Soft commit</text>
  <text x="475" y="96" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Yeni searcher açar</text>
  <text x="475" y="116" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Garanti: GÖRÜNÜRLÜK (NRT)</text>
  <text x="475" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ucuz → sık (~1 sn)</text>
  <text x="475" y="154" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">disk garantisi YOK (tlog'a güven)</text>
  <rect x="120" y="184" width="400" height="34" rx="8" fill="#F7E9E9" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="320" y="205" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">TUZAK: her istekte commit → commit storm → searcher çöplüğü</text>
</svg>
```

- **Soft commit** = **görünürlük**. Yeni bir searcher açar ve yeni belgeler aranabilir hâle gelir. Buna **NRT (Near Real-Time — neredeyse gerçek zamanlı)** denir. Ucuzdur; ama diske kalıcılığı garanti etmez. Çökme sonrası kurtarma tlog'a bağlıdır.
- **Hard commit** = **dayanıklılık** (durability). Segmentleri diske **fsync**'ler ve tlog'u kırpar. Çökme sonrası veri kaybı olmaz. Disk maliyeti yüksektir.

Doğru üretim deseni ikisini **ayırmaktır**. Görünürlük için sık `autoSoftCommit` kullanırsın (~1 sn). Dayanıklılık için seyrek `autoCommit` kullanırsın (15-60 sn) ve yanına **`openSearcher=false`** koyarsın.

`openSearcher=false` şu demektir: "diske yaz, tlog'u kırp, ama yeni searcher **açma**". Commit iptal olmaz. Yalnızca görünürlük işi tamamen soft commit'e bırakılır. Böylece pahalı searcher açma ve ısıtma işi iki ayrı yerden iki kez tetiklenmez.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sık soft commit görünürlük, seyrek hard commit dayanıklılık zaman çizelgesi">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Doğru üretim: soft (sık) + hard (seyrek), ayrı ayrı</text>
  <text x="40" y="58" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">autoSoftCommit ~1 sn — GÖRÜNÜRLÜK (NRT)</text>
  <line x1="40" y1="78" x2="600" y2="78" stroke="#5D7789" stroke-width="1.3"/>
  <line x1="70" y1="70" x2="70" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="115" y1="70" x2="115" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="160" y1="70" x2="160" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="205" y1="70" x2="205" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="250" y1="70" x2="250" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="295" y1="70" x2="295" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="340" y1="70" x2="340" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="385" y1="70" x2="385" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="430" y1="70" x2="430" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="475" y1="70" x2="475" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="520" y1="70" x2="520" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <line x1="565" y1="70" x2="565" y2="86" stroke="#25804C" stroke-width="2.5"/>
  <text x="320" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeni belgeler ~1 sn'de aranabilir (searcher açılır, diske garanti yok)</text>
  <text x="40" y="140" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">autoCommit ~30 sn, openSearcher=false — DAYANIKLILIK</text>
  <line x1="40" y1="160" x2="600" y2="160" stroke="#5D7789" stroke-width="1.3"/>
  <line x1="150" y1="150" x2="150" y2="170" stroke="#186399" stroke-width="3"/>
  <line x1="370" y1="150" x2="370" y2="170" stroke="#186399" stroke-width="3"/>
  <line x1="590" y1="150" x2="590" y2="170" stroke="#186399" stroke-width="3"/>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">diske fsync + tlog kırpma; searcher AÇMAZ → çökmede veri kaybı yok</text>
</svg>
```

**Isıtma (warming)**: Yeni bir searcher açıldığında önbellekleri **boştur**. Sebebi şudur: segmentler değişmiştir ve eski önbellek girdileri olduğu gibi taşınamaz.

Isıtma, yeni searcher'ı **trafiğe almadan önce** iki şey çalıştırır: yapılandırmada tanımlı statik sorgular ve eski searcher'ın sık sorguları (**autowarm**). Böylece filterCache ile queryResultCache dolar. Isıtılmamış bir searcher'a düşen ilk sorgular soğuk önbellek yüzünden yavaştır. Bu sırada eski searcher, yenisi hazır olana kadar trafiğe cevap vermeye devam eder. Yani ısıtma eskiyi kapatmak değil, yeniyi hazırlamaktır.

**Commit fırtınası (commit storm)**: Uygulama her yazmadan sonra `commit=true` gönderirse Solr saniyede onlarca searcher açmaya çalışır. Her biri ısıtma ister. Eskisi kapanmadan yenisi açılır ve `maxWarmingSearchers` sınırı aşılır. Log uyarılarla dolar, CPU tavan yapar ve arama gecikmesi patlar. Kural nettir: uygulamadan **elle commit gönderme**; işi `autoCommit` ve `autoSoftCommit` ayarlarına bırak.

**`_version_` ve çakışma**: Her belgede bir `_version_` alanı vardır. Güncellemeyi beklenen sürüm numarasıyla birlikte gönderirsin: "yalnız hâlâ v=7 ise yaz". Araya başka bir istemci girip sürümü artırmışsa Solr **HTTP 409** döner; uygulama belgeyi yeniden okuyup tekrar dener. Buna **iyimser eşzamanlılık (optimistic concurrency)** denir ve **kayıp güncellemeyi (lost update)** önler. Solr pesimist satır kilidi almaz, güncellemeleri sihirli bir kuyruğa dizmez, alanları kendiliğinden birleştirmez.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Optimistic concurrency: _version_ ile kayıp güncelleme önlenir, çakışmada 409">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Optimistic concurrency (_version_)</text>
  <rect x="24" y="44" width="140" height="50" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="94" y="66" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">İstemci A okur</text>
  <text x="94" y="84" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="monospace">_version_=7</text>
  <rect x="24" y="120" width="140" height="50" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="94" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">İstemci B okur</text>
  <text x="94" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="monospace">_version_=7</text>
  <line x1="164" y1="69" x2="248" y2="69" stroke="#25804C" stroke-width="1.4"/>
  <polygon points="248,69 239,65 239,73" fill="#25804C"/>
  <line x1="164" y1="145" x2="248" y2="145" stroke="#8A2E2E" stroke-width="1.4"/>
  <polygon points="248,145 239,141 239,149" fill="#8A2E2E"/>
  <rect x="250" y="44" width="180" height="50" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="340" y="66" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">A yazar (v=7 ile)</text>
  <text x="340" y="84" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">✓ kabul → v=8</text>
  <rect x="250" y="120" width="180" height="50" rx="8" fill="#ffffff" stroke="#8A2E2E" stroke-width="1.6"/>
  <text x="340" y="142" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">B yazar (v=7 ile)</text>
  <text x="340" y="160" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">✗ sürüm artık 8</text>
  <line x1="430" y1="145" x2="450" y2="145" stroke="#B26B00" stroke-width="1.4"/>
  <polygon points="450,145 441,141 441,149" fill="#B26B00"/>
  <rect x="452" y="120" width="162" height="50" rx="8" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="533" y="142" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">HTTP 409</text>
  <text x="533" y="160" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">oku-tekrar dene</text>
  <text x="533" y="66" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kayıp update</text>
  <text x="533" y="83" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">önlenir</text>
</svg>
```

**Birleştirme politikası (merge policy)**: Segmentler biriktikçe arama yavaşlar. Silinen belgeler de "silindi" işaretiyle segmentin içinde durmaya devam eder. **TieredMergePolicy** gibi bir birleştirme politikası, küçük segmentlerin **ne zaman ve nasıl** büyük segmentlerde birleştirileceğini yönetir. Birleştirme sırasında silinmiş belgeler **fiziksel olarak** atılır. Bu politikanın bölme (shard) yönlendirmesiyle, şema kararlarıyla ya da önbellek ısıtmasıyla ilgisi yoktur. Elle `optimize` veya `forceMerge` çağırmak tüm indeksi yeniden yazdığı için çok pahalıdır; rutin bakımda önerilmez.

### Mülakatta ne sorulur?

- "Soft ile hard commit farkı nedir?" Beklenen tek cümle: görünürlük mü, dayanıklılık mı. İkisini ters söylemek en sık elenme sebebidir.
- "Üretimde commit ayarların ne?" Beklenen cevap üç maddedir: sık `autoSoftCommit`, seyrek `autoCommit` + `openSearcher=false` ve uygulamadan hiç elle commit göndermemek.
- "`openSearcher=false` ne kazandırır?" Tuzak: "commit'i iptal eder" demek. Hayır — commit olur, tlog kırpılır, sadece searcher açılmaz.
- "Logda `maxWarmingSearchers aşıldı` uyarısı görüyorsun, ne yaparsın?" Doğru refleks şudur: commit sıklığına ve uygulamadan gelen elle commit'lere bakmak. Sınırı büyütmek çözüm değildir; yalnız belirtiyi gizler.
- "İki servis aynı belgeyi güncelliyor, ne kullanırsın?" `_version_` + HTTP 409 + oku-tekrar dene.
- "İndekste segment sayısı şişti, ne yaparsın?" Merge policy ayarı; ilk refleks olarak `optimize` demek kırmızı bayraktır.

### Tek cümlede

Solr'da soft commit görünürlüğü (NRT), hard commit ise dayanıklılığı sağlar. Doğru kurulum şudur: sık soft commit ve seyrek hard commit (`openSearcher=false`), ikisi de otomatik. Uygulamadan ise asla elle commit gönderilmez.

# nrt-1 | Uzman
## Soru
Belgeler indeksleniyor ama aramada dakikalarca görünmüyor; buna karşılık sunucu çökse bile veri kaybı OLMAMALI. Hangi commit yapılandırması doğrudur?
## A
Yalnız sık soft commit yapılandır; hard commit tamamen kapatılsın, kalıcılık tümüyle tlog'a bırakılsın
🤖 Soft commit diske kalıcılık garanti etmez; hard commit olmadan tlog kırpılmaz, büyür ve çökmede kayıp riski doğar.
## B
Her belge yazımından sonra uygulamadan explicit hard commit gönder; otomatik commit ayarlarını hiç tanımlama
🤖 Her yazımda searcher açılır: commit storm, warming yığılması ve `maxWarmingSearchers` aşımı kaçınılmaz olur.
## C
Sık `autoSoftCommit` görünürlük için, seyrek `autoCommit` + `openSearcher=false` dayanıklılık için
🤖 Doğru: iki garanti iki ayrı mekanizmaya, birbirini boğmayacak frekanslarda dağıtılmış olur.
## D
Yalnız seyrek hard commit yapılandır ve `openSearcher=true` bırak; soft commit hiç tanımlanmasın
🤖 Dayanıklılık sağlanır ama görünürlük hard commit aralığı kadar gecikir; NRT gereksinimi karşılanmaz.
## Doğru
C
## Açıklama
İki ayrı gereksinim, iki ayrı commit türüne karşılık gelir. **Görünürlük** (belgelerin aramada çıkması, NRT) soft commit'in işidir; ucuz olduğu için **sık** çalıştırılır (`autoSoftCommit`, ~1 sn). **Dayanıklılık** (çökmede veri kaybı olmaması) hard commit'in işidir; pahalı olduğu için **seyrek** çalıştırılır (`autoCommit`, 15-60 sn) ve `openSearcher=false` ile searcher açmaması sağlanır. (C) tam olarak bu ayrımı kurar. (A) tek başına soft commit dayanıklılık vermez ve tlog hiç kırpılmadığı için sınırsız büyür. (B) her yazımda commit göndermek klasik **commit storm**'dur; dayanıklılığı sağlasa da arama tarafını çökertir. (D) hard commit tek başına NRT vermez; belgeler ancak hard commit anında görünür olur ve `openSearcher=true` her seferinde pahalı warming'i tetikler.

# nrt-2 | Uzman
## Soru
Üretim `solrconfig.xml` dosyasında `autoCommit` bloğu `openSearcher=false` ile yapılandırılıyor. Bu ayar tam olarak ne sağlar?
## A
Commit'i tamamen bellekte tutar; segmentler diske hiç yazılmaz, böylece hard commit'in disk maliyeti sıfırlanır
🤖 Hard commit tanımı gereği segmentleri diske fsync'ler; bu ayar disk yazımını iptal etmez, yalnız searcher'ı ilgilendirir.
## B
Transaction log kırpmayı erteler; tlog büyümeye devam eder ve çökme sonrası tüm yazımlar baştan oynatılabilir
🤖 `openSearcher` tlog kırpmayı etkilemez; hard commit `openSearcher=false` iken de tlog'u kırpar.
## C
Yeni searcher'ı arka planda ısıtıp hazır olunca devreye alır ve `maxWarmingSearchers` sınırını devre dışı bırakır
🤖 Warming zaten arka planda yapılır; bu ayar searcher'ı hiç açmaz ve hiçbir sınırı kaldırmaz.
## D
Segmentleri diske fsync'ler ve tlog'u kırpar ama yeni searcher açmaz; görünürlüğü soft commit'e bırakır
🤖 Doğru: dayanıklılık hard commit'ten, görünürlük soft commit'ten gelir; pahalı warming iki kez tetiklenmez.
## Doğru
D
## Açıklama
`openSearcher=false`, hard commit'in **dayanıklılık** işini eksiksiz yapmasını ama **görünürlük** işine hiç karışmamasını sağlar: yeni segmentler diske fsync'lenir, transaction log kırpılır — yani çökme sonrası veri kaybı olmaz — fakat yeni bir **searcher açılmaz**. Görünürlük tamamen `autoSoftCommit`'e kalır. Kazanç şudur: searcher açmak beraberinde **warming** (önbellek ısıtma) getirir; hard commit de searcher açsaydı, sık soft commit'in yanında bir de seyrek hard commit warming tetikler, aynı iş iki ayrı kaynaktan yapılır ve `maxWarmingSearchers` baskısı artardı. (A) yanlış, çünkü commit iptal edilmez; hard commit her hâlükârda diske yazar. (B) yanlış, çünkü tlog kırpma hard commit'e bağlıdır, `openSearcher` değerine değil. (C) yanlış, çünkü ayar bir searcher'ı "arka planda ısıtmaz" — hiç açmaz; `maxWarmingSearchers` da yerinde durur.

# nrt-3 | İleri
## Soru
Uygulama her belge yazımından sonra `commit=true` gönderiyor. Yük altında ne olur?
## A
Commit storm: sürekli yeni searcher açılır, warming birikir, `maxWarmingSearchers` aşılır, CPU ve gecikme patlar
🤖 Doğru: sık explicit commit searcher birikmesi ve sürekli önbellek ısıtma yükü üretir.
## B
Solr explicit commit'leri arka planda tamponlar ve saniyede tek bir commit'e indirger; sistem yükü belirgin değişmez
🤖 Solr explicit commit'i istendiği anda uygular; kendiliğinden batch'leme ya da sıklık kısma yapmaz.
## C
Her commit segmentleri anında birleştirir; indeks küçülür, önbellekler korunur ve sorgu gecikmesi düşer
🤖 Segment birleştirme merge policy'nin işidir ve yeni searcher'la önbellekler korunmaz, boşalır.
## D
Transaction log kapasitesi dolduğu için belgeler reddedilir; istemci HTTP 409 alır ve yazımlar geri alınır
🤖 Belgeler indekslenir; HTTP 409 `_version_` çakışmasının kodudur, commit sıklığının değil.
## Doğru
A
## Açıklama
Her yazımda explicit `commit=true` göndermek **commit storm**'a yol açar: her commit yeni bir **searcher** açar, her searcher **warming** ister (statik warming sorguları ve autowarm ile filterCache/queryResultCache yeniden doldurulur). Yüksek frekansta eski searcher kapanmadan yenisi açılmaya çalışılır, `maxWarmingSearchers` sınırı aşılır, log uyarılarla dolar, CPU tükenir ve arama gecikmesi patlar — (A) budur. (B) yanlış: Solr explicit commit'i istendiği an uygular, sihirli bir tamponlama yapmaz; toplu davranışı istiyorsan `autoCommit`/`autoSoftCommit` kullanırsın. (C) yanlış: commit segment birleştirmez (o merge policy'nin işidir) ve yeni searcher açıldığı için önbellekler korunmaz, sıfırdan ısıtılır. (D) yanlış: belgeler reddedilmez, indekslenir; HTTP 409 optimistic concurrency çakışmasının kodudur, commit sıklığıyla ilgisi yoktur.

# nrt-4 | Uzman
## Soru
İki istemci aynı belgeyi neredeyse aynı anda güncelliyor. Kayıp güncellemeyi (lost update) önlemek için Solr'da hangi mekanizma kullanılır?
## A
Solr tüm güncellemeleri belge id'sine göre tek bir kuyruğa alır; bu sıra garantisi sayesinde çakışma hiç oluşmaz
🤖 Solr böyle bir sıralama garantisi vermez; eşzamanlı yazımda son yazan öncekinin değişikliğini sessizce ezer.
## B
`_version_` alanıyla optimistic concurrency; sürüm değişmişse Solr HTTP 409 döner, istemci okuyup yeniden dener
🤖 Doğru: iyimser sürüm kontrolü kayıp güncellemeyi çakışmayı görünür kılarak engeller.
## C
Belge üzerinde pesimist satır kilidi (row lock) alınır; ikinci istemci kilit serbest kalana kadar bloklanır
🤖 Solr belge/satır kilidi kullanmaz; eşzamanlılık kontrolü tamamen sürüm tabanlı ve iyimserdir.
## D
İki güncelleme alan bazında otomatik birleştirilir; atomic update çakışmayı kendiliğinden çözer, hata dönmez
🤖 Atomic update kısmi güncelleme aracıdır, çakışma çözücü değil; güvenliği yine `_version_` sağlar.
## Doğru
B
## Açıklama
Her Solr belgesinin bir `_version_` alanı vardır ve **optimistic concurrency** (iyimser eşzamanlılık) bunu kullanır: güncellemeyi gönderirken beklenen `_version_` değerini de gönderirsin — "yalnız hâlâ bu sürümdeyse yaz". Başka bir istemci araya girip belgeyi değiştirmişse sürüm artmıştır, Solr yazımı reddeder ve **HTTP 409 (conflict)** döner; uygulama belgeyi yeniden okuyup işlemi tekrarlar. Böylece iki istemcinin birbirinin yazımını ezmesi (**lost update**) önlenir — (B) budur. (A) yanlış: Solr güncellemeleri id bazlı bir kuyruğa dizip çakışmayı yok saymaz; korumasız eşzamanlı yazımda son yazan kazanır. (C) yanlış: Solr pesimist satır kilidi kullanmaz, istemciyi bloklamaz. (D) yanlış: **atomic update** (kısmi güncelleme, `set`/`inc`) alanları tek tek güncellemeye yarar; kendi başına çakışma çözmez, o da aynı `_version_` mekanizmasıyla korunur.

# nrt-5 | Uzman
## Soru
Yeni bir searcher açıldığında "warming" (ısıtma) tam olarak ne yapar ve neden gereklidir?
## A
Değişen segmentlerdeki belgeleri analiz zincirinden yeniden geçirip terimleri indekse tekrar yazar
🤖 Warming yeniden indeksleme yapmaz; indeksin içeriğine hiç dokunmaz, yalnız önbelleklerle ilgilenir.
## B
Küçük segmentleri arka planda birleştirir ve silinmiş işaretli belgeleri diskten fiziksel olarak temizler
🤖 Bu merge policy'nin (ör. TieredMergePolicy) işidir; warming segmentlere dokunmaz.
## C
Önbellekleri (filterCache, queryResultCache) searcher trafiğe alınmadan doldurur; soğuk önbellek cezasını önler
🤖 Doğru: statik warming sorguları ve autowarm sayesinde ilk gerçek sorgular soğuk önbellekle karşılaşmaz.
## D
Eski searcher'ı hemen kapatıp önbelleklerini boşaltır; yeni searcher'a JVM heap alanı açarak GC baskısını düşürür
🤖 Eski searcher, yenisi ısınıp devralana kadar trafiğe cevap vermeyi sürdürür; warming kapatma değil hazırlama işidir.
## Doğru
C
## Açıklama
Bir soft ya da hard commit yeni bir **searcher** açtığında o searcher'ın önbellekleri **boştur**, çünkü segment kümesi değişmiştir ve eski önbellek girdileri olduğu gibi devredilemez. **Warming** (ısıtma), yeni searcher'ı **trafiğe almadan önce** önbelleklerini doldurur: yapılandırılmış statik warming sorguları çalıştırılır ve/veya eski searcher'ın en sık kullanılan girdileri **autowarm** ile yeniden üretilir; böylece filterCache ve queryResultCache hazır olur — (C) budur. Isıtılmamış bir searcher'a düşen ilk sorgular soğuk önbellekle yavaştır; warming bu cezayı kullanıcıdan gizler. (A) yanlış: warming belge indekslemez, analiz zincirini yeniden çalıştırmaz. (B) yanlış: segment birleştirme ve silinenlerin fiziksel temizliği merge policy'nin sorumluluğudur. (D) yanlış: eski searcher hemen kapatılmaz; yenisi hazır olana kadar sorguları o karşılar. Ancak sık commit ile ağır warming birleşirse (commit storm) warming'in kendisi darboğaza dönüşür.

# nrt-6 | İleri
## Soru
Lucene/Solr'da **merge policy** (birleştirme politikası) ne yönetir?
## A
Bir sorgunun hangi shard'a yönlendirileceğini ve replikalar arasında yükün nasıl dağıtılacağını
🤖 Bu SolrCloud yönlendirme ve yük dengeleme işidir; merge policy tek bir çekirdeğin segmentleriyle ilgilenir.
## B
Bir alanın indekslenip indekslenmeyeceğini, saklanacağını ve docValues üretilip üretilmeyeceğini
🤖 Bunlar şema (schema) kararlarıdır; merge policy alan tanımlarına hiç karışmaz.
## C
Yeni searcher açıldığında hangi önbelleklerin autowarm ile ısıtılacağını ve kaç girdinin taşınacağını
🤖 Bu önbellek ve warming yapılandırmasıdır; merge policy searcher önbelleklerini yönetmez.
## D
Çok sayıda küçük segmentin ne zaman ve nasıl büyük segmentlerde birleştirileceğini; silinenler bu sırada atılır
🤖 Doğru: segment sayısını ve silinmiş belgelerin fiziksel temizliğini merge policy belirler.
## Doğru
D
## Açıklama
Lucene indeksi çok sayıda **değişmez segment**ten oluşur; her hard commit yeni segmentler ekler ve silinen belgeler "silindi" işaretiyle segmentin içinde birikir. **Merge policy** (ör. **TieredMergePolicy**), bu küçük segmentlerin **ne zaman ve nasıl** daha büyük segmentlerde birleştirileceğini yönetir — (D) budur. Birleştirme sırasında silinmiş belgeler **fiziksel olarak** atılır, segment sayısı düşer ve arama hızlanır; az sayıda büyük segment taramak, çok sayıda küçük segment taramaktan verimlidir. Yanlış ayarlanmış merge ya çok agresif olup I/O harcar ya da segment sayısını şişirip aramayı yavaşlatır; elle `optimize`/`forceMerge` çağırmak ise indeksi baştan yazdığı için rutin bakımda önerilmez. (A) shard yönlendirme/replika dengeleme SolrCloud'un işidir. (B) alanın indexed/stored/docValues davranışı şemada tanımlanır. (C) autowarm ve önbellek boyutları `solrconfig.xml`'deki cache yapılandırmasıdır. Merge policy yalnız segment yaşam döngüsüyle ilgilenir.
