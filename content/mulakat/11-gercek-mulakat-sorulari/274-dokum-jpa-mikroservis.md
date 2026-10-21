---
slug: dokum-jpa-mikroservis
baslik: "Mülakat Dökümü · JPA, Mikroservis ve Dayanıklılık: Verdiğin Cevap ve Güçlendirilmiş Hâli"
kademe: İleri
sira: 274
etiketler: 11
---

## Konu anlatımı

Bu dosya tahmin üzerine kurulmadı. Elimizde gerçek bir teknik mülakatın dökümü var. Mülakatçının sorduğu sorular ve senin o an verdiğin cevaplar kayıtlı. Yani burada iskelet değil, performans verisi işleniyor. Her sorunun altında dört parça bulacaksın: soruldu ve ne cevap verdin, o cevap yeterli miydi, mülakatçı bundan sonra ne sorardı, aynı soruya daha güçlü hâl nasıl verilir. Cevabın iyiyse iyi denecek; şişirme yok. Zayıfsa açıkça yazacak, çünkü şişirilmiş değerlendirme işe yaramaz.

Bu dosyadaki on bir soru üç kümede toplanıyor. Birinci küme deneyim doğrulaması: mikroservis, kuyruk, önbellek. İkinci küme JPA derinliği: N+1, getirme stratejisi, sorgu türleri. Üçüncü küme dayanıklılık: tekrar güvenliği, hız sınırı, saga, devre kesici, Feign. Üç kümede senin performansın belirgin biçimde farklı çıktı. Bunun nedeni bilgi eksikliği değil, cevap uzunluğu. Teknik sorularda uzun ve doyurucu konuştun; deneyim sorularında tek cümlede kaldın.

### "Mikroservis mimarisiyle çalıştın mı?"

**Soruldu / Verdiğin cevap**

Mülakatçı doğrudan mikroservis deneyimini sordu.

> Teknosa ve eBebek projelerinde mikroservis tabanlı yapılarda çalıştım.

**Değerlendirme**

🟡 Doğru ama eksik. Cevap dürüst ve nettir; sorun içerikte değil, uzunluktadır. Bu bir deneyim doğrulama sorusudur ve mülakatçı burada asla durmaz. Tek cümlelik cevap, soruyu mülakatçının eline geri verir. Böyle olunca konuyu sen değil o seçer. Kendi anlatacağın kısmı seçme hakkını daha ilk cümlede kaybedersin.

**Takip sorusu riski**

- **"Kaç servis vardı, sen hangisinden sorumluydun?"** → Cevap çekirdeği: sayıyı ve kendi rolünü ayrı ayrı söyle. Sahiplendiğin parçayı adlandır, tamamını sahiplenme.
- **"Servis sınırlarını kim çizdi, hangi ölçütle?"** → Cevap çekirdeği: sınır iş yeteneğinden mi geçiyordu, teknik katmandan mı. Kararı sen vermediysen bunu söyle; kararı okuyabilmek de bir yetkinliktir (`139`, `95`).
- **"Servisler birbiriyle nasıl konuşuyordu?"** → Cevap çekirdeği: senkron mu asenkron mu, hangisi nerede. Bu soru bir sonraki iki soruya köprü kurar (`98`, `124`).
- **"Ayrıştırmanın size maliyeti ne oldu?"** → Cevap çekirdeği: takas cümlesi. Hata ayıklama, uçtan uca test ve sürüm uyumu zorlaşır (`96`).

**Güçlendirilmiş cevap**

Cevabını çöpe atma; üzerine üç parça ekle. İskelet şudur: **rol + sorumluluk + somut bir teknik karar.** Önce hangi projede hangi rolde olduğunu söyle. Sonra hangi parçadan sorumlu olduğunu daralt; "tüm mimari" demek inandırıcılığı düşürür. Son olarak yaşadığın bir teknik kararı adlandır ve gerekçesini bir cümlede ver. Üçüncü parça asıl ayrıştırıcıdır, çünkü karar anlatmak deneyimin kanıtıdır. Bu üç parçayı kendi hafızandan doldur; burada senin adına ayrıntı yazılmaz. Dökümde bu iki firmadan başka bilgi yok ve olmayan ayrıntı uydurmak en hızlı eleyen davranıştır.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Deneyim sorusunda kisa cevap, derinlesme merdiveni ve guclu cevap iskeleti">
  <defs>
    <marker id="dkmjpa-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Deneyim sorusunda kısa cevap, konuyu mülakatçının eline verir</text>
  <rect x="14" y="36" width="192" height="184" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="110" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Verdiğin cevap</text>
  <text x="110" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek cümle, tek bilgi</text>
  <text x="110" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">rol yok</text>
  <text x="110" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sorumluluk sınırı yok</text>
  <text x="110" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">teknik karar yok</text>
  <text x="110" y="170" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">konuşulacak konuyu</text>
  <text x="110" y="187" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">artık karşı taraf seçer</text>
  <line x1="208" y1="128" x2="228" y2="128" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjpa-ok)"/>
  <rect x="232" y="36" width="196" height="184" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Derinleşme merdiveni</text>
  <text x="330" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kaç servis, hangisi senindi?</text>
  <text x="330" y="106" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sınırı kim çizdi, hangi ölçütle?</text>
  <text x="330" y="130" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">servisler nasıl konuşuyordu?</text>
  <text x="330" y="154" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ayrıştırmanın maliyeti neydi?</text>
  <text x="330" y="182" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">her basamak bir öncekinin boşluğundan doğar</text>
  <line x1="430" y1="128" x2="450" y2="128" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjpa-ok)"/>
  <rect x="454" y="36" width="192" height="184" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="550" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Güçlü cevap iskeleti</text>
  <text x="550" y="82" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">1 · rol</text>
  <text x="550" y="99" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hangi projede ne yaptın</text>
  <text x="550" y="122" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">2 · sorumluluk</text>
  <text x="550" y="139" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hangi parça senindi</text>
  <text x="550" y="162" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">3 · teknik karar</text>
  <text x="550" y="179" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">ne seçtin, neden</text>
  <text x="550" y="200" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">karar anlatmak deneyimin kanıtıdır</text>
  <rect x="14" y="232" width="632" height="60" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="252" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Sınır: eksik olanı uzatmak serbest, olmayanı eklemek yasak</text>
  <text x="330" y="271" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Hatırlamadığın ayrıntıyı uydurma; takip sorusu tam oraya gelir ve cevapsız kalırsın.</text>
  <text x="330" y="286" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"O kısmı ben yürütmedim, kararı şu ölçütle verilmişti" cümlesi güven kaybettirmez.</text>
</svg>
```

### "Kuyruk yapılarıyla, Kafka ile çalıştın mı?"

**Soruldu / Verdiğin cevap**

Mesajlaşma altyapısı deneyimi soruldu.

> RabbitMQ kullandım, sonra Kafka ile de çalıştım.

**Değerlendirme**

🟡 Doğru ama eksik. Bir önceki soruyla aynı durum. İki ürün adı saydın, aralarındaki farkı söylemedin. Bu cevaptan sonra takip sorusu kesin gelir. Mülakatçının sorusu neredeyse zorunlu hâle gelmiştir: **"İkisi arasında ne fark var, hangisini neden seçersin?"** İyi haber şu: bu takibin cevabı öğrenilebilir ve dosyada var. Yani buradaki eksik deneyim eksiği değil, hazırlık eksiği.

**Takip sorusu riski**

- **"Kafka ile RabbitMQ arasındaki temel fark nedir?"** → Kafka kalıcı ve sıralı bir kayıt günlüğüdür; mesaj okunduktan sonra da durur. RabbitMQ kuyruk temellidir; mesaj onaylanınca kuyruktan düşer (`124`).
- **"Hangi işte hangisini seçersin?"** → Olay akışı, yeniden oynatma ve yüksek iş hacmi (throughput) gerekiyorsa Kafka. Zengin yönlendirme, iş dağıtımı ve mesaj başına onay gerekiyorsa RabbitMQ (`107`, `119`).
- **"Kafka'da sıralama nasıl garanti edilir?"** → Sıra yalnız bölüm (partition) içinde garantidir. Aynı anahtar aynı bölüme gider; sıra istiyorsan anahtarı doğru seç (`107`).
- **"Aynı mesaj iki kez gelirse ne olur?"** → Yaygın uygulamada teslim en az bir kezdir. Tüketici tekrar edilse de sonucu değişmeyen (idempotent) olmalıdır (`130`).

**Güçlendirilmiş cevap**

Aynı iki cümleyi söyle, sonra farkı sen getir. Şunu eklersen ayrışırsın: "İkisini aynı işin farklı ihtiyaçları için kullandım; RabbitMQ'da iş kuyruğu ve yönlendirme, Kafka'da olay akışı öne çıkıyor." Ardından tek cümlelik ayrımı ver: Kafka'da tüketici kendi konumunu (offset) tutar, RabbitMQ'da kuyruk mesajı tüketiciye dağıtır. Sonra bir seçim ölçütü söyle: geçmişi yeniden okumak gerekiyorsa Kafka, mesaj başına onay ve ölü mektup kuyruğu gerekiyorsa RabbitMQ. Bu üç cümle takip sorusunu senin cevabının içine alır ve mülakatçıya sormak yerine dinlemek kalır.

### "Redis kullandın mı, ne için?"

**Soruldu / Verdiğin cevap**

Önbellek deneyimi soruldu.

> Cache olarak kullandık. Önce cache'e bakarız, yoksa veri tabanına gideriz.

**Değerlendirme**

✅ Yeterli, ama bir kelime eksik. Anlattığın şey aslında bilinen bir desendir: **yanda önbellek (cache-aside)**. Uygulamayı doğru tarif ettin, adını söylemedin. Mülakatta bu fark küçük değildir. Deseni adıyla söyleyen aday, o desenin bilinen tuzaklarını da biliyor sayılır. Adını söylemeyen aday ise tek bir projedeki alışkanlığını anlatıyor gibi duyulur. Aynı cevap, tek kelime eklenince sınıf atlar.

**Takip sorusu riski**

- **"Bu desenin adı ne?"** → Yanda önbellek (cache-aside). Yazma yolunda okuma yolundan farklı davranır; veriyi uygulama yerleştirir (`157`).
- **"Veri değişince önbelleği nasıl güncellersin?"** → Geçersiz kılma (invalidation) kararı. Yaygın tercih güncellemek değil silmektir; yazma sonrası anahtarı düşürürsün (`159`).
- **"Popüler bir anahtarın süresi dolunca ne olur?"** → Önbellek çığı (stampede). Aynı anda yüzlerce istek veri tabanına iner; kilit veya erken yenileme ile önlenir (`159`).
- **"Tek bir anahtara aşırı trafik gelirse?"** → Sıcak anahtar (hot key) sorunu. Yerel ikinci katman önbellek ya da anahtarı bölmek gündeme gelir (`166`).
- **"Önbellek düşerse sistem ne yapar?"** → Önbellek isteğe bağlı olmalıdır. Redis erişilemezken uygulama veri tabanına düşerek çalışmaya devam edebilmelidir.

**Güçlendirilmiş cevap**

Cevabına iki ekleme yeter. Birincisi ad: "Bu yanda önbellek desenidir; okuma yolunda ıska olunca veri tabanına gider, dönen değeri önbelleğe yazarız." İkincisi tuzaklar: "Yazma yolunda anahtarı güncellemek yerine siliyorduk, çünkü güncelleme yarış durumu üretebiliyor." Buna bir de süre kararını ekle: her anahtara yaşam süresi (TTL) verilir, süresi ise verinin ne kadar eski kalabileceğine göre seçilir. Son cümle olarak tutarlılığın iş kararı olduğunu söyle. Fiyat ile kampanya metninin eskime toleransı aynı değildir.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yanda onbellek deseni: okuma yolu, yazma yolu ve bilinen tuzaklar">
  <defs>
    <marker id="dkmjpa-cache" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="dkmjpa-cache-kirmizi" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yanda önbellek (cache-aside): veriyi önbelleğe uygulama koyar</text>
  <rect x="16" y="42" width="118" height="44" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="75" y="69" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">istek gelir</text>
  <line x1="136" y1="64" x2="152" y2="64" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjpa-cache)"/>
  <rect x="154" y="42" width="150" height="44" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="229" y="69" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">önbellekte var mı?</text>
  <line x1="306" y1="56" x2="330" y2="56" stroke="#25804C" stroke-width="1.4" marker-end="url(#dkmjpa-cache)"/>
  <text x="368" y="52" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">isabet (hit)</text>
  <rect x="418" y="36" width="226" height="34" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="531" y="58" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">değeri doğrudan döndür</text>
  <line x1="306" y1="76" x2="330" y2="86" stroke="#B07D20" stroke-width="1.4" marker-end="url(#dkmjpa-cache)"/>
  <text x="368" y="97" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">ıska (miss)</text>
  <rect x="418" y="80" width="226" height="34" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="531" y="102" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">veri tabanından oku</text>
  <line x1="531" y1="116" x2="531" y2="128" stroke="#5D7789" stroke-width="1.4" marker-end="url(#dkmjpa-cache)"/>
  <rect x="418" y="132" width="226" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="531" y="154" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">önbelleğe yaz, süre ver, döndür</text>
  <rect x="16" y="104" width="288" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="160" y="124" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Yazma yolu</text>
  <text x="160" y="143" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veri tabanına yaz, sonra anahtarı SİL</text>
  <text x="160" y="159" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">güncellemek yerine silmek yarışı azaltır</text>
  <line x1="160" y1="102" x2="160" y2="90" stroke="#B23B3B" stroke-width="1.3" marker-end="url(#dkmjpa-cache-kirmizi)"/>
  <rect x="16" y="180" width="152" height="132" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="92" y="200" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Yaşam süresi (TTL)</text>
  <text x="92" y="222" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">her anahtara süre verilir</text>
  <text x="92" y="240" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">süre = eskime toleransı</text>
  <text x="92" y="258" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tolerans iş kararıdır</text>
  <text x="92" y="282" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">süresiz anahtar</text>
  <text x="92" y="298" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sessiz bayat veri üretir</text>
  <rect x="176" y="180" width="152" height="132" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="252" y="200" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Önbellek çığı</text>
  <text x="252" y="222" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">popüler anahtarın süresi</text>
  <text x="252" y="238" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">aynı anda dolar</text>
  <text x="252" y="258" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yüzlerce istek DB'ye iner</text>
  <text x="252" y="282" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çözüm: tek yenileyici kilit</text>
  <text x="252" y="298" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">veya erken yenileme</text>
  <rect x="336" y="180" width="152" height="132" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="412" y="200" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Sıcak anahtar</text>
  <text x="412" y="222" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek anahtara aşırı trafik</text>
  <text x="412" y="240" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek düğüm doyar</text>
  <text x="412" y="258" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">gecikme sıçrar</text>
  <text x="412" y="282" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çözüm: yerel ikinci katman</text>
  <text x="412" y="298" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">veya anahtarı bölmek</text>
  <rect x="496" y="180" width="148" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="570" y="200" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Önbellek düşerse</text>
  <text x="570" y="222" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">önbellek isteğe bağlıdır</text>
  <text x="570" y="240" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">uygulama DB'ye düşer</text>
  <text x="570" y="258" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yavaşlar, durmaz</text>
  <text x="570" y="282" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">Redis'siz çalışamayan</text>
  <text x="570" y="298" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sistem önbellek değil bağımlılıktır</text>
</svg>
```

### "N+1 problemi nedir, nasıl çözersin?"

**Soruldu / Verdiğin cevap**

JPA tarafına geçildi ve doğrudan N+1 soruldu.

> Ana sorgudan sonra her kayıt için ayrı sorgu atılması. `JOIN FETCH`, `EntityGraph` ya da fetch stratejisini düzenleyerek çözerim.

**Değerlendirme**

✅ Yeterli. Tanım doğru, çözümler doğru ve isimleriyle sayılmış. Bu cevap mülakatçıyı tatmin eder ve dökümde de öyle olmuş. Ancak dosyanın başında yazan kural burada da geçerli: doyurucu cevap bir üst basamağa çıkarır. Nitekim hemen ardından getirme stratejisi sorulmuş. Yani bu cevabın ödülü bir sonraki sorudur.

**Takip sorusu riski**

- **"`JOIN FETCH` ile sayfalama yaparsan ne olur?"** → Koleksiyon getirmesi satırı çoğaltır. Hibernate tüm sonucu belleğe alıp sayfalamayı orada yapar; büyük veride tehlikelidir (`82`).
- **"İki koleksiyonu birden `JOIN FETCH` edersen?"** → Kartezyen çarpım oluşur. Yaygın çözüm iki ayrı sorgu ya da toplu getirme (batch size).
- **"Toplu getirme nedir?"** → Ayrı sorguları gruplayarak N sorguyu N bölü küme sayısına indirir. Tek sorgu vermez ama tahmin edilebilir davranır.
- **"Listeleme uç noktasında en iyi çözüm ne?"** → Çoğu zaman varlık değil DTO projeksiyonu. Yalnız gereken kolonlar çekilir; kalıcı bağlam (persistence context) da yorulmaz (`80`).

**Güçlendirilmiş cevap**

Cevabını koru, üstüne seçim ölçütü ekle. Şunu eklersen ayrışırsın: "Tek bir kaydı ilişkileriyle çekiyorsam `JOIN FETCH`, tekrar eden bir senaryo ise `EntityGraph` kullanırım." Sonra bir uyarı ver: "Sayfalama varsa `JOIN FETCH` tek başına yanlış çözümdür; orada toplu getirme veya ikinci sorgu tercih ederim." Son olarak ölçüm cümlesini kur: sorgu sayısını görmeden çözdüm demek doğru değildir. İstatistik açmak ya da günlüğe düşen sorguları saymak, cevabı iddiadan gözleme çevirir.

### "LAZY ve EAGER farkı nedir? İçinde `User` olan bir varlık `EAGER` olsa ne olur?"

**Soruldu / Verdiğin cevap**

Getirme stratejisi ve `EAGER` senaryosu soruldu.

> `LAZY` ihtiyaç anında, `EAGER` hemen getirir. Ama her iki durumda da N+1 oluşabilir.

**Değerlendirme**

✅ **Bu güçlü bir cevap.** Çoğu aday `EAGER`'ı N+1'in çözümü sanır ve tam burada elenir. Sen tersini söyledin ve doğru söyledin. `EAGER`, ilişkinin **ne zaman** getirileceğini söyler; **nasıl** getirileceğini söylemez. Bu yüzden bir liste sorgusunda `EAGER` ilişki de ayrı sorgularla çekilebilir ve N+1 aynen kalır. Cevabın ince olduğu için mülakatçının bir üst basamağa çıkması beklenir.

**Takip sorusu riski**

- **"Öyleyse `EAGER` ne zaman doğru seçim?"** → Neredeyse hiç. Varsayılan `LAZY` tutulur; ihtiyaç olan yerde sorgu düzeyinde getirme yapılır (`82`).
- **"`LAZY` bırakırsan hangi hatayı alırsın?"** → `LazyInitializationException`. Oturum kapandıktan sonra ilişkiye dokunulursa çıkar (`80`).
- **"`open-in-view` bunu çözmez mi?"** → Hatayı gizler, sorunu büyütür. Bağlantı istek boyunca açık kalır ve N+1 web katmanında sessizce oluşur.
- **"Doğru sınır nerede?"** → Veriyi işlem sınırı içinde, ihtiyaca göre getirmek. Web katmanına varlık değil DTO taşımak bu tartışmayı bitirir (`81`).

**Güçlendirilmiş cevap**

Cevabına bir cümle ekle, ayrım tamamlansın: "`EAGER` zamanı belirler, çözümü değil; getirme biçimini sorgu belirler." Sonra kendi kuralını söyle: varsayılan `LAZY`, ihtiyaç olan yerde `JOIN FETCH` ya da `EntityGraph`. Ardından `LazyInitializationException` riskini kendin getir ve nerede çıktığını anlat. Son olarak `open-in-view` konusunda net bir duruş göster; bunu kapatıp veriyi işlem içinde toplamak daha öngörülebilir bir tasarımdır. Bu dört cümle seni "tanımı bilen" aday sınıfından "kararı olan" aday sınıfına taşır.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="N+1 problemi: LAZY ve EAGER ayni sonucu verebilir, cozumler ve sayfalama tuzagi">
  <defs>
    <marker id="dkmjpa-nplus" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">EAGER ne zaman getirileceğini söyler, nasıl getirileceğini değil</text>
  <rect x="14" y="38" width="310" height="118" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="169" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">LAZY · liste sorgusu</text>
  <text x="169" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 sorgu: 100 sipariş gelir</text>
  <text x="169" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">döngüde ilişkiye dokunulur</text>
  <text x="169" y="118" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">100 ek sorgu → toplam 101</text>
  <text x="169" y="140" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tetikleyici: erteleme sonradan bozulur</text>
  <rect x="336" y="38" width="310" height="118" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="491" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">EAGER · liste sorgusu</text>
  <text x="491" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 sorgu: 100 sipariş gelir</text>
  <text x="491" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ilişki hemen istenir</text>
  <text x="491" y="118" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">yine 100 ek sorgu → toplam 101</text>
  <text x="491" y="140" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">EAGER çözüm değildir, yalnız zamanı öne alır</text>
  <text x="330" y="176" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Çözüm sorgu düzeyindedir</text>
  <rect x="14" y="188" width="152" height="88" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="90" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">JOIN FETCH</text>
  <text x="90" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek sorguda birleştirir</text>
  <text x="90" y="246" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek kayıt için ideal</text>
  <text x="90" y="266" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sayfalamayla birlikte riskli</text>
  <rect x="176" y="188" width="152" height="88" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="252" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">EntityGraph</text>
  <text x="252" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">getirme planı bildirimsel</text>
  <text x="252" y="246" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">aynı depo, farklı plan</text>
  <text x="252" y="266" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tekrar eden senaryoya uygun</text>
  <rect x="338" y="188" width="152" height="88" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="414" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Toplu getirme</text>
  <text x="414" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">N sorguyu gruplar</text>
  <text x="414" y="246" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">101 yerine birkaç sorgu</text>
  <text x="414" y="266" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sayfalamayla uyumludur</text>
  <rect x="500" y="188" width="146" height="88" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="573" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">DTO projeksiyonu</text>
  <text x="573" y="228" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yalnız gereken kolonlar</text>
  <text x="573" y="246" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">varlık yüklenmez</text>
  <text x="573" y="266" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">listeleme için çoğu zaman en iyisi</text>
  <rect x="14" y="288" width="632" height="42" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="306" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Tuzak: koleksiyonu JOIN FETCH edip sayfalamak</text>
  <text x="330" y="323" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Satır sayısı çoğalır; tüm sonuç belleğe alınır ve sayfalama orada yapılır. Büyük veride bellek şişer.</text>
</svg>
```

### "Repository katmanında hangi sorguları yazarsın? Native query nedir?"

**Soruldu / Verdiğin cevap**

Sorgu türleri ve native sorgu soruldu.

> JPQL, HQL ve native SQL yazarım. Native sorguda gerçek tablo ve kolon adlarıyla çalışılır.

**Değerlendirme**

✅ Yeterli. Üç türü de saydın ve native'in ayırt edici özelliğini doğru verdin. Eksik olan kısım tanım değil, karar. Cevapta "ne zaman hangisini seçerim" cümlesi yok. Mülakatçı burada genellikle bedeli sorar, çünkü native sorgu bedava değildir.

**Takip sorusu riski**

- **"Native sorgunun bedeli nedir?"** → Taşınabilirlik gider; sorgu bir veri tabanı lehçesine bağlanır. Tip güvenliği de zayıflar, sonuç eşlemesi elle kurulur (`80`).
- **"Ne zaman yine de native yazarsın?"** → Pencere fonksiyonu, toplu güncelleme veya lehçeye özgü ifade gerektiğinde. Ölçülmüş bir performans gerekçesi de meşrudur.
- **"`@Modifying` ile güncelleme yaparsan ne olur?"** → Sorgu doğrudan veri tabanına gider. Kalıcı bağlam (persistence context) bundan haberdar olmaz ve elindeki varlık bayatlar.
- **"Bayatlamayı nasıl önlersin?"** → İşlem sonrası bağlamı temizlemek ya da güncellemeyi ayrı bir işlem sınırında yapmak. Sonra tekrar okumak en güvenlisidir (`81`).

**Güçlendirilmiş cevap**

Cevabına bir sıralama ekle: "Varsayılanım türetilmiş metot, yetmezse JPQL, yine yetmezse native." Bu sıra kararın olduğunu gösterir. Ardından bedeli kendin söyle: native sorgu lehçeye bağlanır ve derleme zamanında korunmazsın. Sonra en ince noktayı getir: toplu güncellemede kalıcı bağlamın senkron kalmadığını bilmek ayrıştırıcıdır. Kapanışta ölçüm cümlesini kur; native tercihini "hızlı olur" diye değil "şu sorguyu ölçtük" diye savun.

### "İdempotent ne demek?"

**Soruldu / Verdiğin cevap**

Tekrar güvenliği kavramı soruldu.

> Aynı isteğin tekrar edilmesi ek yan etki yaratmaz. Finansal bir işlemde ikinci kez emir oluşmamalı.

**Değerlendirme**

✅ **Güçlü cevap.** Tanımı doğru verdin ve arkasına örnek koydun. Örnek vermek burada belirleyicidir; kavramı ezberlemekle kullanmış olmak arasındaki farkı örnek ele verir. Seçtiğin alan da isabetli, çünkü tekrar güvenliğinin en görünür olduğu yer ödeme akışlarıdır. Bu cevaptan sonra mülakatçının tanımı tekrar sorması beklenmez; uygulamayı sorar.

**Takip sorusu riski**

- **"Bunu kodda nasıl sağlarsın?"** → İstemci bir anahtar üretir. Sunucu bu anahtarı saklar ve aynı anahtar tekrar gelirse yeni işlem açmaz (`253`).
- **"Anahtar nerede saklanır?"** → Kalıcı bir tabloda ve **benzersizlik kısıtıyla**. Yalnız bellekte tutmak kopya sayısı artınca çöker.
- **"İlk isteğin sonucu ne olur?"** → Sonuç da saklanmalıdır. Tekrar gelen istek hata değil, ilk sonucun aynısını almalıdır.
- **"HTTP tarafında hangi metotlar doğal olarak böyledir?"** → `GET`, `PUT` ve `DELETE` tanım gereği tekrar güvenlidir; `POST` değildir. Bu yüzden anahtar `POST` uçlarında gerekir.
- **"Mesaj tüketicisinde nasıl olur?"** → Mesaj kimliği saklanır. İkinci kez gelen aynı kimlik işlenmeden atlanır (`130`).

**Güçlendirilmiş cevap**

Tanımı ve örneği koru, üstüne uygulamayı ekle. Şunu söylersen ayrışırsın: "İstemciden gelen anahtarı benzersizlik kısıtı olan bir tabloda saklarım; kısıt ihlali gelirse bu bir tekrardır." Ardından çoğu adayın atladığı yeri getir: sonucu da saklamak. Tekrar gelen isteğe hata dönmek doğru değildir, ilk yanıtın aynısı dönmelidir. Kapanışta yeniden denemeyle (retry) bağını kur. Ağ hatasında yeniden deneme zorunludur ve tekrar güvenliği olmayan uçta yeniden deneme çift kayıt üretir.

### "Rate limit ile ilgili bir deneyimin oldu mu?"

**Soruldu / Verdiğin cevap**

Hız sınırı deneyimi soruldu.

> Bir aracı kurum entegrasyonunda dakikada 100 istek limiti vardı.

**Değerlendirme**

✅ **Somut olduğu için güçlü.** Bu cevap kısa ama bir sayı içeriyor ve sayı hikâyeyi gerçek yapıyor. Önceki deneyim sorularından farkı tam burada: "kullandım" değil, "şu sınır vardı" demişsin. Yine de cevap sınırı anlatıyor, sınırla ne yaptığınızı anlatmıyor. Mülakatçının bir sonraki sorusu neredeyse kesindir: sınıra takılınca ne oldu.

**Takip sorusu riski**

- **"Sınıra takılınca ne yaptınız?"** → Cevap çekirdeği: 429 yanıtı, `Retry-After` başlığı ve üstel geri çekilme (backoff). Sınırsız yeniden deneme sınırı büsbütün kilitler (`153`).
- **"İstekleri nasıl düzenlediniz?"** → Jeton kovası (token bucket) düzgün akıtır ve ani yığılmaya (burst) belli bir pay bırakır. Sabit pencere ise pencere başında sıçrama üretir.
- **"Birden çok kopya çalışıyorsa sayaç nerede?"** → Paylaşımlı bir sayaçta; her kopyada ayrı sayaç toplamda sınırı aşar (`153`).
- **"Sınırı sen koyacak olsan hangi algoritmayı seçerdin?"** → Kayan pencere sayacı ile jeton kovası arasında takas: doğruluk ile bellek maliyeti.

**Güçlendirilmiş cevap**

Sayıyı koru, arkasına davranışı ekle. Şunu eklersen ayrışırsın: "Sınıra takılan istekleri kuyruğa alıp jeton kovasıyla düzgün akıttık" ya da gerçekte ne yaptıysanız onu tek cümlede söyle. Sonra 429 ve `Retry-After` davranışını anlat; sunucunun söylediği süreye uymak temel nezakettir. Ardından yeniden denemenin nasıl yapılması gerektiğini ekle: üstel geri çekilme ve rastgele sapma (jitter). Kapanışta dağıtık sayaç sorununu kendin getir. Çok kopyalı uygulamada sınır ancak paylaşımlı sayaçla korunur; bu cümle konuya dışarıdan bakabildiğini gösterir.

### "Saga ve Outbox desenlerini biliyor musun?"

**Soruldu / Verdiğin cevap**

Dağıtık tutarlılık desenleri soruldu.

> Saga, işlemi adımlara bölüp her adımın telafisini tanımlar. Outbox ise olayı aynı transaction içinde bir tabloya yazıp sonra yayınlar.

**Değerlendirme**

✅ **İkisini de doğru tanımladın.** Özellikle giden kutusu tanımında kritik kelimeyi söyledin: **aynı işlem içinde.** Bu desenin tüm değeri o cümlededir; yazma ile olay atomik olur. Sagada da telafiyi adım başına tanımladın, bu doğru. Bu seviyede cevap alan mülakatçı tanımı bırakır ve seçim sorularına geçer.

**Takip sorusu riski**

- **"Koreografi mi orkestrasyon mu?"** → Az adımda koreografi, çok adımlı ve dallanmalı akışta orkestrasyon. Ölçüt izlenebilirliktir (`126`).
- **"Giden kutusu tablosunu kim okur?"** → İki yol var. Yoklama (polling) yapan bir iş ya da veri tabanı kayıt günlüğünü izleyen değişiklik yakalama (CDC) (`116`).
- **"Telafi ile geri alma aynı şey mi?"** → Değil. Yerel işlem çoktan kesinleşmiştir; telafi yeni bir iş işlemidir. İade kaydı, iptal kaydı, stok serbest bırakma.
- **"Saga izolasyon verir mi?"** → Vermez. Ara durum dışarıdan görünür; iş akışında "beklemede" gibi bir durum tanımlanır (`127`).
- **"Giden kutusu tekrar üretir mi?"** → Evet, en az bir kez teslim olur. Tüketici tekrar güvenli olmalıdır (`130`).

**Güçlendirilmiş cevap**

Tanımların üstüne iki ayrım ekle. Birincisi telafi ayrımı: "Telafi bir geri alma değildir; etkiyi dengeleyen yeni bir iş işlemidir." Bu tek cümle mülakatta en sık eleyen hatanın karşısına geçer. İkincisi seçim ölçütü: adım sayısı ve dallanma arttıkça orkestrasyon okunur kalır. Sonra giden kutusunun neden gerektiğini bir cümlede ver: veri tabanına yazıp ardından mesaj yayımlamak çift yazma sorunudur ve arada süreç düşerse sessiz kayıp olur. Kapanışta sagayı ayakta tutan ikinci ayağı ekle; giden kutusu tekrar üretir, bu yüzden tüketici tekrar güvenli olmalıdır.

### "Circuit breaker kullandın mı?"

**Soruldu / Verdiğin cevap**

Dayanıklılık deseni soruldu.

> Teknosa'da kullandık. Closed, Open ve Half-Open durumları var.

**Değerlendirme**

✅ Yeterli. Durumları saymak burada beklenen minimumdur ve sen verdin. Deneyimi de ilişkilendirmişsin. Eksik kısım ayarlardır: hangi eşikte açılır, pencere ne kadardır, açıkken ne olur. Bir de dökümdeki en kritik boşluk: devre kesicinin zaman aşımı olmadan çalışmadığı hiç söylenmemiş. Bu ayrıntı mülakatta sık sorulur, çünkü sahada en çok bu yüzden hata alınır.

**Takip sorusu riski**

- **"Devre hangi ölçüte göre açılır?"** → Belirli bir pencere içindeki hata oranı ya da yavaş çağrı oranı. Ham sayı değil oran kullanılır (`101`).
- **"Yarı açık durumda ne olur?"** → Sınırlı sayıda deneme isteği geçirilir. Başarılıysa devre kapanır, değilse tekrar açılır.
- **"Devre açıkken kullanıcı ne görür?"** → Yedek davranış (fallback) kararı. Bu bir iş kararıdır: bayat veri mi, boş liste mi, açık hata mı.
- **"Zaman aşımı olmadan devre kesici çalışır mı?"** → Çalışmaz. Sonuçlanmayan çağrı başarısızlık sayılmaz; iş parçacıkları (thread) asılı kalır ve devre beslenmez.
- **"Yeniden deneme ile birlikte nasıl kurgulanır?"** → Sıralama önemlidir. Yeniden deneme devrenin içinde kalırsa hata sayısını yapay olarak şişirir.

**Güçlendirilmiş cevap**

Durumları saydıktan sonra ayarları getir: eşik, pencere ve bekleme süresi. Sonra en değerli cümleyi kur: "Devre kesici tek başına yetmez; her dış çağrının bağlantı ve okuma zaman aşımı olmalı." Gerekçesini de ver; süresiz bekleyen çağrı hata üretmez, yalnız kaynak tüketir. Ardından yedek davranışın iş kararı olduğunu söyle ve iki seçeneği örnekle. Kapanışta ölçüm ekle: devrenin açılma sayısı izlenmezse ayarın doğru olup olmadığı bilinemez (`101`).

### "Feign Client nedir?"

**Soruldu / Verdiğin cevap**

Servisler arası çağrı aracı soruldu.

> REST API'yi Java interface gibi tanımlayıp çağırmayı sağlar.

**Değerlendirme**

✅ Yeterli. Tanım kısa, doğru ve tam yerinde. Bu soru genellikle ısınma sorusudur; asıl mesele hemen ardından gelir. Cevabında eksik olan tek şey, bu kolaylığın gizlediği risktir. Feign çağrıyı yerel metot çağrısı gibi gösterir. Kolaylık iyidir, ama ağın var olduğunu unutturursa tehlikelidir.

**Takip sorusu riski**

- **"Bu kolaylığın riski nedir?"** → Uzak çağrı yerel çağrı gibi görünür. Gecikme (latency), kısmi başarısızlık ve zaman aşımı gözden kaçar (`100`).
- **"Feign çağrısına ne eklemek zorunludur?"** → Bağlantı ve okuma zaman aşımı, ardından devre kesici ve ölçülü yeniden deneme (`101`).
- **"Senkron çağrı zinciri neden riskli?"** → Uçtaki bir servis yavaşlarsa yavaşlık zincir boyunca yukarı yayılır. Buna basamaklı hata denir.
- **"Ne zaman senkron yerine olay kullanırsın?"** → Cevabı hemen gerekmeyen işlerde. Bildirim, denetim kaydı ve rapor tetikleme asenkron olmaya uygundur (`98`).

**Güçlendirilmiş cevap**

Tanımı söyle, hemen ardından takası getir. Şunu eklersen ayrışırsın: "Feign okunabilirliği artırır ama uzak çağrıyı yerel gibi gösterdiği için zaman aşımı ve devre kesici olmadan kullanılmaz." Sonra varsayılan davranış uyarısını ekle; zaman aşımı açıkça verilmezse çağrı beklenenden çok daha uzun asılı kalabilir. Ardından senkron bağımlılığın sistem düzeyindeki bedelini söyle: her senkron çağrı, çağıranın erişilebilirliğini çağrılana bağlar. Kapanışta bir tasarım cümlesi kur. Cevabı hemen gerekmeyen iş, senkron çağrı yerine olayla yürütülürse zincir kısalır.

### Bu bölümden çıkan ders

Bu on bir cevapta bilgi sorunu yok; dağılım sorunu var. Teknik kavram sorularında güçlüsün ve bazı yerlerde beklenenin üstünde cevap verdin. `EAGER` ile de N+1 oluşabileceğini söylemen, giden kutusunda "aynı transaction" demen, tekrar güvenliğine finansal örnek eklemen bunun kanıtı. Buna karşılık deneyim sorularında tek cümlede kaldın: mikroservis, kuyruk ve önbellek soruları birer kelimeyle kapandı. Oysa mülakatta bu üç soru en çok konuşulacak alandır. Kural şudur: kavram sorusuna tanım artı takas ile cevap ver, deneyim sorusuna rol artı sorumluluk artı karar ile cevap ver. İkinci kalıp senin en zayıf yerin ve düzeltmesi en kolay olanı. Bir de eksik bir kelime alışkanlığı var: yaptığın işin desen adını söylemiyorsun. Önbellek cevabın yanda önbellek desenini tarif ediyordu ama adı geçmedi. Adı söylemek, deseni ve tuzaklarını bildiğini bir kelimeyle ilan eder.

# dkmjpa-1 | Orta
## Soru
Bir aday Redis kullanımını şöyle anlatıyor: "Önce önbelleğe bakarız, yoksa veri tabanına gider, dönen değeri önbelleğe yazarız." Bu davranışın adı nedir ve adını söylemek neden önemlidir?
## A
Yanda önbellek (cache-aside); adı söylenince desenin bilinen tuzakları da konuşulabilir hâle gelir
🤖 Doğru: uygulamayı tarif etmek ile deseni bilmek arasındaki farkı ad kapatır.
## B
Arkadan yazma (write-behind); yazma önce önbelleğe gider, veri tabanına sonra taşınır
🤖 bu bir yazma stratejisidir; anlatılan ise okuma yolundaki ıska davranışıdır.
## C
Baştan okuma (read-through); eksik veriyi veri tabanından uygulama değil önbellek kendisi çeker
🤖 burada veriyi önbelleğe koyan uygulamadır, önbellek değil.
## D
Bunun bir adı yoktur; yalnız yaygın bir uygulama alışkanlığıdır, desen sayılmaz
🤖 desendir ve adı vardır; ad, tuzak listesini de beraberinde getirir.
## Doğru
A
## Açıklama
Anlatılan akış **yanda önbellek (cache-aside)** desenidir. Okuma yolunda uygulama önce önbelleğe bakar; ıska olursa veri tabanından okur ve dönen değeri önbelleğe yazar. Kritik nokta şudur: veriyi önbelleğe koyan uygulamadır. Baştan okumada (read-through) bu iş önbelleğin kendisine aittir; arkadan yazma (write-behind) ise okuma değil yazma yolunu tarif eder. Adı söylemek mülakatta neden fark yaratır? Çünkü desen adı, beraberinde bilinen sorun listesini getirir: geçersiz kılma kararı, yaşam süresi (TTL) seçimi, önbellek çığı ve sıcak anahtar (`157`, `159`, `166`). Adı söylemeyen aday tek bir projedeki alışkanlığını anlatmış olur; adı söyleyen aday o alışkanlığın hangi aileye ait olduğunu bildiğini gösterir. Cevabın devamında yazma yolu da anlatılmalıdır. Yaygın tercih anahtarı güncellemek değil silmektir, çünkü güncelleme eş zamanlı yazmalarda yarış durumu üretebilir.

# dkmjpa-2 | İleri
## Soru
Bir `Order` varlığının `user` ilişkisi `EAGER` yapıldı. `findAll()` ile 100 sipariş çekiliyor ama sorgu sayısı hâlâ yüksek. En doğru açıklama hangisidir?
## A
`EAGER` N+1'i tanım gereği ortadan kaldırır; yüksek sorgu sayısının kaynağı başka bir ilişki olmalıdır
🤖 EAGER birleştirme garantisi vermez; N+1 aynı ilişkide de sürebilir.
## B
`EAGER` yalnız tek kayıt getiren çağrılarda geçerlidir; `findAll` için önbellek ayarı gerekir
🤖 böyle bir kural yoktur; sorun önbellekte değil getirme biçimindedir.
## C
`EAGER` ilişkinin ne zaman getirileceğini söyler, nasıl getirileceğini değil; ilişki ayrı sorgularla çekilebilir
🤖 Doğru: zaman kararı ile getirme biçimi ayrı şeylerdir.
## D
Sorunun kaynağı `LAZY` alanlardır; tüm alanlar `EAGER` yapılırsa sorgu sayısı tek sorguya iner
🤖 hepsini EAGER yapmak sorgu sayısını genellikle daha da artırır.
## Doğru
C

## Açıklama
Çoğu aday `EAGER`'ı N+1'in çözümü sanır ve tam burada elenir. Doğrusu şudur: `EAGER` ilişkinin **ne zaman** getirileceğini belirler, **nasıl** getirileceğini belirlemez. Bir liste sorgusunda sağlayıcı ana sorguyu çalıştırır, sonra her satırın ilişkisini ayrı sorguyla çekebilir. Sonuç yine 1 artı N sorgudur. `LAZY` durumunda da aynı sayı çıkar; fark yalnız sorguların ne zaman atıldığındadır. Çözüm getirme stratejisinde değil, sorgu düzeyindedir: `JOIN FETCH`, `EntityGraph`, toplu getirme (batch size) ya da DTO projeksiyonu (`82`, `80`). D şıkkı sorunu büyütür; her ilişkiyi `EAGER` yapmak gereksiz veriyi de her sorguda çeker. Doğru varsayılan `LAZY` bırakmak ve ihtiyaç olan sorguda getirmeyi açıkça istemektir. Bu yaklaşım hem N+1'i hem de gereksiz yüklemeyi aynı anda kontrol altına alır.

# dkmjpa-3 | İleri
## Soru
`select o from Order o join fetch o.items` sorgusu sayfalama ile çağrılıyor. Günlükte uyarı çıkıyor ve bellek kullanımı şişiyor. Sebep nedir?
## A
`join fetch` sayfalamayı veri tabanına iter; uyarı bilgilendirmedir, bellek artışıyla ilgisi yoktur
🤖 tam tersi olur; sayfalama veri tabanında değil bellekte yapılır.
## B
Koleksiyon getirmesi satır sayısını çoğaltır; sağlayıcı tüm sonucu belleğe alıp sayfalamayı orada yapar
🤖 Doğru: satır sayısı ile varlık sayısı artık aynı değildir.
## C
Sayfalama yalnız native sorgularda çalışır; JPQL desteklemediği için tüm satırlar geri döner
🤖 JPQL sayfalamayı destekler; sorun koleksiyon getirmesiyle birleşmesindedir.
## D
Uyarının sebebi sıralama eksikliğidir; sorguya sıralama eklenirse sayfalama veri tabanında yapılır
🤖 sıralama eklemek koleksiyon çoğaltmasını ortadan kaldırmaz.
## Doğru
B
## Açıklama
Bir koleksiyonu `join fetch` ile çekmek, sonuç kümesindeki **satır sayısını** çoğaltır. Üç kalemi olan bir sipariş üç satır olarak döner. Bu durumda "ilk 20 satır" ile "ilk 20 sipariş" artık aynı şey değildir. Veri tabanı düzeyinde sayfalamak yanlış sonuç üretirdi; bu yüzden sağlayıcı güvenli olanı yapar ve tüm sonucu belleğe alıp sayfalamayı orada uygular. Doğru davranıştır ama pahalıdır ve büyük veride bellek şişer. Bu yüzden uyarı basılır. Doğru çözüm iki yoldan biridir. Birincisi kimlikleri sayfalayan bir sorgu çalıştırıp, ikinci sorguda o kimliklerin koleksiyonlarını getirmektir. İkincisi toplu getirme (batch size) kullanmaktır; sorgu sayısı tek sorguya inmez ama tahmin edilebilir kalır ve sayfalama veri tabanında yapılır (`82`). Listeleme uç noktalarında üçüncü ve çoğu zaman en iyi seçenek DTO projeksiyonudur; yalnız gereken kolonlar çekilir, koleksiyon hiç yüklenmez (`80`).

# dkmjpa-4 | İleri
## Soru
Bir ödeme uç noktası tekrar edilse de sonucu değişmeyen (idempotent) hâle getirilecek. İstemci her istekte bir anahtar gönderiyor. En sağlam tasarım hangisidir?
## A
Anahtarı uygulama belleğinde tutmak; aynı anahtar tekrar gelirse isteği reddetmek yeterlidir
🤖 bellek kopya başına ayrıdır ve süreç yeniden başlayınca kaybolur.
## B
Anahtarı yalnız günlüğe yazmak; tekrar gelirse günlüğe bakılıp elle müdahale edilir
🤖 günlük bir kısıt değildir; tekrarı çalışma anında engellemez.
## C
Kaydetmeden önce sorguyla kontrol etmek; kayıt yoksa işleme devam etmek yeterlidir
🤖 kontrol ile yazma arasında yarış vardır; iki istek aynı anda geçebilir.
## D
Anahtarı benzersizlik kısıtıyla saklamak, ilk sonucu da kaydetmek ve tekrarda aynı sonucu döndürmek
🤖 Doğru: kısıt yarışı veri tabanında keser, saklanan sonuç yanıtı tutarlı yapar.
## Doğru
D
## Açıklama
Tekrar güvenliği üç parçadan oluşur. Birincisi **anahtar**: istemci her mantıksal işlem için bir kimlik üretir. İkincisi **kısıt**: bu anahtar kalıcı bir tabloda benzersizlik kısıtıyla saklanır. Kısıt olmadan yalnız sorguyla kontrol etmek yetmez, çünkü kontrol ile yazma arasında yarış vardır; iki eş zamanlı istek kontrolü birlikte geçip iki kayıt üretebilir. Kısıt bu yarışı veri tabanı düzeyinde keser. Üçüncüsü **sonucun saklanması**: tekrar gelen isteğe hata dönmek doğru değildir, ilk yanıtın aynısı dönmelidir. Aksi hâlde istemci gerçekte başarılı olan işlemi başarısız sanır ve yeniden dener. A şıkkı belleğe güvenir; çok kopyalı çalışmada ve yeniden başlatmada çöker. B şıkkı sorunu insana havale eder. Bu tasarım yeniden denemeyle (retry) birlikte anlam kazanır: ağ hatasında yeniden deneme kaçınılmazdır ve tekrar güvenli olmayan uçta yeniden deneme çift kayıt üretir (`130`, `253`).

# dkmjpa-5 | Uzman
## Soru
Feign ile yapılan bir çağrı devre kesiciyle (circuit breaker) sarıldı. Dış servis yanıt vermiyor, istekler asılı kalıyor ve iş parçacığı havuzu doluyor. Devre ise bir türlü açılmıyor. En olası sebep nedir?
## A
Yarı açık (Half-Open) durum kapalıdır; bu durum etkinleştirilirse devre beklendiği gibi açılacaktır
🤖 yarı açık durum kapanma aşamasını yönetir, açılmayı tetiklemez.
## B
Hata eşiği çok yüksek ayarlanmıştır; eşik düşürülürse asılı kalan istekler kendiliğinden sonlanır
🤖 eşik düşürmek asılı çağrıyı sonlandırmaz; ortada sayılacak hata yoktur.
## C
Çağrıda zaman aşımı yoktur; sonuçlanmayan istek başarısızlık sayılmaz, devreyi besleyecek sinyal oluşmaz
🤖 Doğru: devre kesici ancak biten çağrıların sonucunu sayabilir.
## D
Yedek davranış (fallback) tanımlanmamıştır; fallback eklenirse asılı istekler iptal edilir
🤖 fallback devre açıldıktan sonra devreye girer; asılı çağrıyı kesmez.
## Doğru
C
## Açıklama
Devre kesici bir **sonuç sayacıdır**. Belirli bir pencerede biten çağrıların ne kadarının başarısız olduğuna bakar ve eşiği aşınca açılır. Hiç sonuçlanmayan bir çağrı ise ne başarı ne başarısızlıktır; sayaca girmez. Bu yüzden zaman aşımı verilmemiş bir çağrıda devre kesici sessizce işlevsiz kalır. Bu arada asıl zarar başka yerde birikir: her asılı çağrı bir iş parçacığını (thread) ve bir bağlantıyı tutar. Havuz dolunca sağlıklı uç noktalar da yanıt veremez hâle gelir; buna basamaklı hata denir. Doğru kurulum sıralıdır. Önce her dış çağrıya bağlantı ve okuma zaman aşımı verilir. Sonra devre kesici bu zaman aşımlarını başarısızlık olarak sayar ve eşik aşılınca açılır. Devre açıkken yedek davranış çalışır; bunun ne olacağı bir iş kararıdır, teknik ayrıntı değil (`101`, `100`). Yeniden deneme kurgulanacaksa sırası önemlidir; devrenin içinde kalan yeniden deneme hata sayısını yapay olarak şişirir.

# dkmjpa-6 | Uzman
## Soru
Dakikada 100 istek sınırı olan bir dış servise, birden çok kopya çalıştıran bir uygulamadan gidiliyor. Sınır sık sık aşılıyor. En yerinde çözüm hangisidir?
## A
Sayacı paylaşımlı bir yerde tutmak ve jeton kovası gibi düzgün akıtan bir algoritma kullanmak
🤖 Doğru: sınır tek merkezden sayılır, akış düzgünleşir.
## B
Her kopyada ayrı sayaç tutup sınırı kopya sayısına bölmek; trafik dengesiz olsa da toplam korunur
🤖 dengesiz trafikte bir kopya payını doldururken diğerininki boş kalır.
## C
Sınırı istemci tarafında bırakmak ve 429 yanıtı gelirse isteği hemen ve sınırsız biçimde yeniden denemek
🤖 gecikmesiz yeniden deneme sınırı büsbütün kilitler ve yükü artırır.
## D
Sabit pencere sayacını her kopyada ayrı tutmak; pencere başındaki sıçrama ihmal edilebilir düzeydedir
🤖 sabit pencere sınırında iki pencere sınırı arka arkaya çakışabilir.
## Doğru
A
## Açıklama
Sorunun iki katmanı var. Birincisi **sayacın yeri**. Her kopya kendi sayacını tutarsa toplam sınır korunamaz; sınırı kopya sayısına bölmek de yalnız trafik eşit dağıldığında işe yarar. Gerçekte dağılım eşit olmaz: bir kopya payını doldurup beklerken diğerinin payı kullanılmadan kalır. Bu yüzden sayaç paylaşımlı bir yerde tutulur (`153`). İkincisi **algoritma**. Sabit pencere sayacı basittir ama pencere sınırında sıçrama üretir; iki komşu pencerenin uçları birleşince kısa sürede sınırın iki katı istek geçebilir. Jeton kovası isteği düzgün akıtır ve ani yığılmaya (burst) kova kapasitesi kadar pay bırakır. Kayan pencere ise daha doğru sayar, karşılığında daha çok kayıt tutar. C şıkkı en zararlı seçenektir: 429 aldıktan sonra beklemeden yeniden denemek karşı tarafı büsbütün kilitler. Doğru davranış `Retry-After` başlığına uymak, yoksa üstel geri çekilme ve rastgele sapma ile denemektir.
