---
slug: merkezi-loglama-metrik
baslik: "Merkezî Loglama ve Metrik: Yapılandırılmış Log, RED/USE ve Kardinalite"
kademe: Orta
sira: 104
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Bir uçak kazasında ilk aranan şey kara kutudur: uçuş boyunca olup biten her olayı sırayla yazmıştır. Hastane odasındaki monitör ise başka bir iş yapar; nabzı ve ateşi saniye saniye çizer, bir eşik aşılınca alarm çalar. Yazılımda kara kutunun karşılığı log, monitörün karşılığı metriktir. Log "tam olarak ne oldu" sorusuna cevap verir. Metrik ise "genel gidişat nasıl, bozuluyor mu" sorusuna cevap verir. Sistem onlarca makineye dağıldığında bu iki kaydın tek merkezde toplanması şarttır. Yoksa arıza anında bakılacak bir yer kalmaz.

### Temel kavramlar

- **Log (kayıt satırı):** programın çalışırken "şunu yaptım", "şu hata oldu" diye yazdığı metin satırı. Her satırın bir seviyesi (level) vardır: DEBUG (ince ayrıntı), INFO (normal akış), WARN (şüpheli durum), ERROR (hata). Üretimde genelde INFO ve üstü yazılır.
- **Yapılandırılmış log (structured logging):** satırı serbest cümle olarak değil, adlandırılmış alanlardan oluşan bir JSON nesnesi olarak yazmak: zaman (timestamp), seviye, servis adı, `traceId`, mesaj. Makine metni tahmin etmez, alanı okur.
- **Metrik (ölçüm):** tek bir sayının zaman içindeki seyri, yani zaman serisi. Üç temel tip: **sayaç (counter)** yalnız artar (toplam istek), **ölçer (gauge)** inip çıkar (anlık bellek), **histogram** değerleri kovalara dağıtıp dağılımı verir (gecikme).
- **Etiket (label):** metriğe iliştirilen ad-değer çiftidir (`method="GET"`, `status="500"`). Aynı metriği parçalara ayırıp ayrı ayrı incelemeyi sağlar.
- **Çeşitlilik sayısı (kardinalite / cardinality):** bir alanın alabileceği farklı değerlerin sayısıdır. `method` yalnız birkaç değer alır, yani kardinalitesi düşüktür. `userId` ise kullanıcı sayısı kadar farklı değer alır, yani pratikte sınırsızdır.
- **Yüzdelik dilim (percentile), p99:** isteklerin yüzde 99'unun altında kaldığı süre.
- **Gösterge paneli (dashboard):** metrik sorgularının grafiğe dizildiği ekran. **Uyarı (alert):** bir sorgunun eşiği aşması hâlinde nöbetçiye giden otomatik haber.
- **İzleme kaydı (trace):** tek bir isteğin servisten servise yolculuğunun ağaç biçimindeki kaydıdır. Bu ağacın tamamını birbirine bağlayan ortak kimliğe `traceId` denir.

### Nasıl çalışır?

Log yolu dört adımdır. (1) Uygulama logu kendi diskindeki dosyaya değil, standart çıktıya (stdout) JSON satırlar hâlinde yazar. (2) Her makinede duran bir toplayıcı ajan (Fluent Bit, Promtail) bu satırları okur. (3) Satırlar merkezî bir depoya akar: Loki ya da Elasticsearch/OpenSearch. (4) Geliştirici tek arayüzden `servis=odeme AND level=ERROR` gibi alan tabanlı bir sorgu yazar; düz metinden düzenli ifadeyle (regex) alan ayıklamak gerekmez.

Diske değil stdout'a yazmanın iki sebebi vardır. Birincisi: kap (container) ve pod geçicidir (ephemeral); öldüğünde diski de onunla gider. İkincisi: servis 20 kopya hâlinde çalışırken makinelere tek tek girip dosya okumak ölçeklenmez. Merkezî depo ise logu kalıcı hâle getirir ve tek yerden aranabilir kılar.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Loglar ve metrikler merkezî sisteme akar">
  <defs>
    <marker id="log-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Pod'lardan merkezî gözlemlenebilirliğe</text>
  <rect x="24" y="55" width="120" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="84" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">pod A</text>
  <text x="84" y="87" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">JSON log + metrik</text>
  <rect x="24" y="110" width="120" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="84" y="127" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">pod B</text>
  <text x="84" y="142" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">correlationId (MDC)</text>
  <rect x="270" y="52" width="150" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="345" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Log toplayıcı</text>
  <text x="345" y="86" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ELK / Loki</text>
  <rect x="270" y="110" width="150" height="44" rx="10" fill="#FBF3E2" stroke="#B8860B" stroke-width="1.4"/>
  <text x="345" y="128" text-anchor="middle" font-size="11" font-weight="700" fill="#B8860B" font-family="system-ui">Prometheus</text>
  <text x="345" y="144" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">metrikleri çeker (pull)</text>
  <rect x="500" y="80" width="140" height="48" rx="10" fill="#EDE7F6" stroke="#6A4CA6" stroke-width="1.5"/>
  <text x="570" y="100" text-anchor="middle" font-size="11" font-weight="700" fill="#6A4CA6" font-family="system-ui">Grafana</text>
  <text x="570" y="116" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">panolar + alarm</text>
  <path d="M144 75 L268 74" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#log-arw)"/>
  <path d="M144 130 L268 132" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#log-arw)"/>
  <path d="M420 74 L560 92" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#log-arw)"/>
  <path d="M420 132 L560 116" fill="none" stroke="#186399" stroke-width="1.3" marker-end="url(#log-arw)"/>
</svg>
```

Metrik yolu ters yönde işler. Uygulama Micrometer gibi bir kütüphaneyle sayaç, ölçer ve histogram tutar. Bunları bir uç noktada yayımlar; Spring'de bu uç `/actuator/prometheus` adresidir. Prometheus bu uçları belirli aralıklarla kendisi ziyaret edip okur. Buna **çekme (pull / scrape)** modeli denir: veriyi merkez toplar, uygulama Prometheus'a veri **itmez (push)**. Tek istisna, ömrü kısa olan toplu işlerdir; onlar için araya Pushgateway konur.

Okunan değerler zaman serisi olarak saklanır, PromQL diliyle sorgulanır ve Grafana'da panele ile uyarıya dönüşür. Prometheus yalnız son anlık değeri değil, geçmişi de tutar. Buna karşılık log satırlarını tam metin olarak indekslemez; çünkü sakladığı şey olay değil, sayıdır.

### Mülakatta ne sorulur?

**RED mi, USE mu?** RED (Rate, Errors, Duration) istek odaklıdır. Üç şeye bakar: saniyedeki istek sayısı, hata oranı ve gecikme dağılımı. Yani servisin müşteri gözünden görünümüdür. USE (Utilization, Saturation, Errors) ise kaynak odaklıdır. O da CPU ile bellek kullanımına, kuyruk doygunluğuna ve donanım hatalarına bakar.

İkisi eş anlamlı değildir; biri diğerinin eski adı da değildir. Veritabanı ya da ön yüz gibi teknoloji katmanlarına göre bölünmezler. Biri dışarıdan, diğeri içeriden bakar ve birlikte kullanılırlar. Hiçbiri güvenlik veya maliyet çerçevesi değildir.

**Kardinalite patlaması (cardinality explosion).** Bir metriğin her benzersiz etiket birleşimi ayrı bir zaman serisi demektir. Her seri de bellek ve disk tüketir. Etikete sınırsız değer alan bir alan koyarsanız seri sayısı patlar. Böyle alanlara örnek: `userId`, e-posta, sipariş numarası, istek kimliği, tam URL. Bu durumda seri sayısı kullanıcı ya da istek sayısı kadar büyür. Milyonlarca seri Prometheus'un belleğini şişirir ve süreci devirir.

Sorunun kaynağı etiket değerinin veri tipi ya da metrik adının uzunluğu değildir. Prometheus etiketleri elbette destekler; sorun benzersiz birleşim sayısıdır. Kural şudur: etiket değerleri sınırlı ve düşük kardinaliteli olmalıdır (`method`, `endpoint`, `status`). Kullanıcıya özgü alanlar metrik etiketine yazılmaz; onların yeri log satırı ya da izleme kaydıdır.

```svg
<svg viewBox="0 0 660 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="RED ve USE metrik çerçeveleri karşılaştırması">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki metrik çerçevesi: RED ve USE</text>
  <rect x="28" y="44" width="290" height="140" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="173" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">RED — istek/servis odaklı</text>
  <text x="173" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Rate · Errors · Duration</text>
  <text x="173" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"servisim müşteriye</text>
  <text x="173" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">nasıl görünüyor?"</text>
  <text x="173" y="164" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">dış görünüm (kara kutu)</text>
  <rect x="342" y="44" width="290" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="487" y="70" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">USE — kaynak odaklı</text>
  <text x="487" y="94" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Utilization · Saturation · Errors</text>
  <text x="487" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"kaynağım ne durumda,</text>
  <text x="487" y="134" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tıkanıyor mu?"</text>
  <text x="487" y="164" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">CPU/bellek/kuyruk (altyapı)</text>
</svg>
```

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kardinalite patlaması: yüksek kümeli etiket seri sayısını patlatır">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kardinalite: her etiket kombinasyonu = 1 zaman serisi</text>
  <rect x="28" y="44" width="290" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="173" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">sınırlı (low-cardinality)</text>
  <text x="173" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">method, status, endpoint</text>
  <text x="173" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">az sayıda olası değer</text>
  <text x="173" y="150" text-anchor="middle" font-size="22" font-weight="700" fill="#25804C" font-family="system-ui">~onlarca seri</text>
  <text x="173" y="176" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">Prometheus rahat</text>
  <rect x="342" y="44" width="290" height="150" rx="12" fill="#FBEEDC" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="487" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#8A2E2E" font-family="system-ui">+ userId (unbounded)</text>
  <text x="487" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kullanıcı sayısı kadar değer</text>
  <text x="487" y="112" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sınırsız kombinasyon</text>
  <text x="487" y="152" text-anchor="middle" font-size="22" font-weight="700" fill="#8A2E2E" font-family="system-ui">milyonlarca seri</text>
  <text x="487" y="176" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">bellek şişer → çöker</text>
</svg>
```

**Hangi sinyale bakılır?** "Sistem şu an bozuluyor mu, hata oranı arttı mı?" sorusu metrikle cevaplanır. "Şu tek istekte tam olarak ne oldu, hangi hata fırlatıldı?" sorusu logla cevaplanır. "Bu isteğin gecikmesi hangi servisin içinde birikti?" sorusu ise izleme kaydıyla cevaplanır. Üçü birbirinin yerine geçmez: metrik toplu eğilimi, log tek tek olayları, iz ise tek bir isteğin haritasını verir.

**Neden ortalama değil p99?** Ortalama, birkaç çok yavaş isteği kalabalığın içinde gizler. p99 ise en kötü deneyimi görünür kılar. Uyarılar da bu yüzden ortalama gecikmeye değil, p99'a ve hata oranına kurulur. İyi bir uyarı, insanın gerçekten eyleme geçebileceği bir durumu gösterir; aksi hâlde gürültüye dönüşür ve bir süre sonra yok sayılır.

**`traceId`'yi log satırına bas.** Tek bir istek beş servise dokunduğunda o isteğe ait satırları ayırmanın tek yolu ortak bir kimliktir. Bu kimliğe **ilişkilendirme kimliği (correlation ID)** denir; pratikte çoğu zaman izleme kaydının `traceId` değeridir.

Spring'de MDC (Mapped Diagnostic Context) bu kimliği, ilgili iş parçacığının yazdığı her log satırına otomatik ekler. Sonrasında merkezî depoda `traceId=X` sorgusu beş servisin satırlarını yan yana getirir. Zaman damgasına bakarak eşleştirme yapmak, eşzamanlı binlerce istek varken çalışmaz. Satırları alfabetik sıraya dizmek de bir isteği tanımlamaz.

### Tek cümlede

Logu yapılandırılmış ve `traceId` taşıyan biçimde stdout'a yazıp merkezde topla, metriği düşük kardinaliteli etiketlerle üretip Prometheus'a çektir; servisin müşteriye görünüşünü RED, kaynağın durumunu USE ile izle.

# log-1 | Orta
## Soru
Dağıtık bir sistemde log satırlarını neden serbest metin yerine **yapılandırılmış** (structured, ör. JSON) yazmak tercih edilir?
## A
JSON satırlar düz metinden daha az yer kapladığı için merkezî depolama maliyeti düşer; asıl kazanç boyuttan gelir
🤖 JSON, alan adlarını her satırda tekrarladığı için genelde daha çok yer kaplar; kazanç boyutta değildir.
## B
Terminalde renkli ve hizalı göründüğü için geliştirici satırları gözüyle daha hızlı tarar; asıl kazanç okunabilirliktir
🤖 renk ve hizalama insan içindir; merkezî loglamanın kazancı makinenin alan üzerinden sorgulayabilmesidir.
## C
Serbest metin loglar merkezî toplayıcıda saklanamaz; toplama ajanları yalnız JSON satırlarını kabul eder, diğerlerini atar
🤖 düz metin de toplanıp saklanır; kısıt "saklanamaz" değil, "alan bazında verimli aranamaz"dır.
## D
Alanlara (zaman, seviye, servis, `traceId`) ayrıldığı için merkezî depoda doğrudan alan üzerinden süzülüp sorgulanabilir
🤖 Doğru: adlandırılmış alanlar filtreleme ve toplulaştırmayı regex'e gerek kalmadan mümkün kılar.
## Doğru
D
## Açıklama
**Yapılandırılmış loglama** log satırını makine-okunur anahtar-değer alanlarına (`timestamp`, `level`, `service`, `traceId`, `message`) dönüştürür. Faydası **aranabilirlik ve toplulaştırmadır**: merkezî sistemde "şu servisin `level=ERROR` ve `traceId=X` olan satırları" gibi bir sorgu doğrudan alan üzerinde çalışır — düz metinden kırılgan regex'le alan çıkarmaya gerek kalmaz. Boyut genelde artar, azalmaz (A yanlış); renk ve hizalama insan içindir, merkezî sorgulamayı kolaylaştırmaz (B yanlış); düz metin de toplanıp saklanabilir, sadece verimli aranamaz (C yanlış). Doğru gerekçe alan tabanlı makine sorgulanabilirliğidir (D).

# log-2 | Orta
## Soru
Bir istek 5 servise dokunuyor ve bir hata oluştu. Bu tek isteğe ait **tüm** servislerin loglarını tek sorguda toplamak için ne kullanılır?
## A
Her log satırındaki zaman damgası yeterlidir; hata anının çevresindeki dar bir zaman aralığını süzmek aynı isteğin satırlarını kendiliğinden bir araya getirir
🤖 aynı milisaniyede binlerce istek işlenir; zaman aralığı tek isteği diğerlerinden ayıramaz.
## B
İlişkilendirme kimliği (correlation ID, çoğunlukla `traceId`): tek isteğe atanan ortak kimlik her log satırına basılır, loglar bu kimliğe göre süzülür
🤖 Doğru: ortak kimlik, beş servise dağılmış satırları tek isteğe bağlayan anahtardır.
## C
Servislerin log satırlarını alfabetik sıraya dizmek yeterlidir; aynı isteğe ait satırlar bu sıralamada kendiliğinden yan yana düşer
🤖 sıralama bir isteği tanımlamaz; farklı isteklerin satırları yine iç içe geçer.
## D
Her servisin logunu ayrı bir dosyaya yazıp dosyaları elle karşılaştırmak; isteğin yolu insan gözüyle takip edilir
🤖 elle karşılaştırma onlarca replikada uygulanamaz ve pod ölünce dosya da kaybolur.
## Doğru
B
## Açıklama
Eşzamanlı binlerce istek varken tek bir isteğin log satırlarını ayırmak için **ortak bir kimlik** gerekir: **correlation ID** (çoğu zaman dağıtık izlemenin `traceId`'si). Bu kimlik isteğin girişinde üretilir/okunur, servisler arası taşınır ve **her log satırına** basılır. Spring'de bunu **MDC** (Mapped Diagnostic Context) sağlar: bir kez MDC'ye konan `traceId`/`correlationId`, o thread'in tüm log satırlarına otomatik eklenir. Merkezî toplayıcıda `correlationId=X` sorgusu 5 servisin ilgili satırlarını tek yerde toplar. Zaman damgasıyla eşleştirme eşzamanlı istekleri ayıramaz (A yanlış); alfabetik sıralama isteği tanımlamaz (C yanlış); elle dosya karşılaştırmak ise geçici pod'larda ve onlarca replikada uygulanamaz (D yanlış).

# log-3 | İleri
## Soru
Bir geliştirici Prometheus metriğine `userId` etiketini (label) ekledi; birkaç gün sonra Prometheus belleği şişip çöktü. Neden?
## A
`userId` bir metin alanıdır; Prometheus etiket değerlerinde yalnız sayı tutabildiği için oluşan tip uyuşmazlığı zamanla belleği bozar
🤖 etiket değerleri zaten metindir; sorun veri tipi değil, benzersiz değerlerin sayısıdır.
## B
Metrik adı çok uzun olduğu için indeks bozulur; Prometheus ad uzunluğuna göre yer ayırdığından uzun adlar belleği zamanla doldurur
🤖 bellek tüketimini ad uzunluğu değil, etiketlerin ürettiği ayrı zaman serisi sayısı belirler.
## C
Kardinalite patlaması: her benzersiz etiket kombinasyonu ayrı bir zaman serisidir; sınırsız değerli `userId` milyonlarca seri üretip belleği tüketir
🤖 Doğru: yüksek kardinaliteli etiket seri sayısını patlatır ve Prometheus'u devirir.
## D
Prometheus etiketleri hiç desteklemez; yalnız metrik adını sakladığı için fazladan etiket eklemek depolama biçimini bozar
🤖 etiketler Prometheus'un temel özelliğidir; sorun etiketin varlığı değil, değerinin sınırsız olmasıdır.
## Doğru
C
## Açıklama
Prometheus'ta bir metriğin **her benzersiz etiket kombinasyonu ayrı bir zaman serisidir** ve her seri bellek/depolama tüketir. `userId` gibi **sınırsız kümeli** (unbounded / high-cardinality) bir değeri etikete koymak, kullanıcı sayısı kadar (milyonlarca) seri üretir — buna **kardinalite patlaması** (cardinality explosion) denir; Prometheus'un belleğini şişirir ve çökertir. Kural: etiket değerleri **sınırlı ve düşük kümeli** olmalı (`method`, `endpoint`, `status` gibi az sayıda olası değer); `userId`, `orderId`, `email`, tam URL, request-id gibi yüksek-kardinaliteli veriler **asla** etikete konmaz (onlar için log/trace kullanılır). Sorun veri tipi (A), ad uzunluğu (B) veya destek eksikliği (D) değildir.

# log-4 | Orta
## Soru
RED ve USE metrik çerçeveleri neyi ölçer ve nasıl farklılaşır?
## A
RED istek odaklıdır (Rate/Errors/Duration — servisin dışarıdan görünümü); USE kaynak odaklıdır (Utilization/Saturation/Errors — altyapının durumu)
🤖 Doğru: biri servisin müşteriye görünümünü, diğeri kaynağın durumunu ölçer; ikisi tamamlayıcıdır.
## B
İkisi de aynı üç ölçümü farklı adlarla anlatır; USE, RED çerçevesinin daha eski adıdır ve zamanla onun yerine geçmiştir
🤖 odakları farklıdır (istek ile kaynak) ve biri diğerinin eski adı değildir.
## C
RED yalnız veritabanı ve depolama katmanı için, USE ise yalnız tarayıcıdaki ön yüz (frontend) için tanımlanmış çerçevelerdir
🤖 RED istekler, USE kaynaklar içindir; teknoloji katmanına göre bölünmezler.
## D
RED güvenlik olaylarını, USE ise bulut maliyetini ölçen çerçevelerdir; ikisi de performansla ilgilenmez
🤖 her ikisi de performans ve sağlık çerçevesidir; güvenlik ya da maliyetle ilgili değildir.
## Doğru
A
## Açıklama
**RED** ve **USE** tamamlayıcı iki metrik çerçevesidir. **RED** (Rate, Errors, Duration) **istek/servis odaklıdır**: servisin dışarıdan, müşteri gözünden sağlığını ölçer — saniyedeki istek, hata oranı ve gecikme dağılımı. **USE** (Utilization, Saturation, Errors) **kaynak odaklıdır**: CPU, bellek, disk, kuyruk gibi altyapı kaynaklarının kullanımı, doygunluğu ve hataları. Kabaca: RED "servisim kullanıcıya nasıl görünüyor?", USE "kaynaklarım ne durumda, tıkanıyor mu?" sorusunu yanıtlar. İkisi eş anlamlı (B) değil, katmana göre bölünmüş (C) değil, güvenlik/maliyet (D) değildir — farklı ama birbirini tamamlayan bakış açılarıdır.

# log-5 | Orta
## Soru
Üretimde bir hata var. Junior geliştirici "pod'a `kubectl exec` ile girip `tail -f` yapayım" diyor. Dağıtık sistemde neden merkezî loglama (ELK/Loki) tercih edilir?
## A
Kubernetes'te `kubectl exec` tamamen yasaktır; küme bu komutu kapattığı için çalışan bir pod'un içine hiçbir şekilde girilemez
🤖 `exec` yasak değildir; sadece ölçekli üretimde hata ayıklamanın doğru aracı değildir.
## B
Merkezî loglama yalnız daha derli toplu bir arayüz sunar; hangi yöntemle bakarsan bak aynı veriye ulaşırsın, teknik bir fark yoktur
🤖 fark tekniktir — pod ölünce log gider ve tek tek erişim ölçeklenmez; mesele arayüz değildir.
## C
Pod'a girip `tail` çalıştırmak log dosyasını kilitler ve o ana kadar birikmiş satırları siler; bu yüzden canlıda yapılmaz
🤖 `tail` dosyayı ne kilitler ne siler; asıl sorun pod'un geçiciliği ve replika sayısıdır.
## D
Pod'lar geçicidir: öldüğünde diskteki logu kaybolur ve onlarca replikaya tek tek girmek ölçeklenmez; merkezî toplayıcı logu kalıcılaştırır
🤖 Doğru: geçici pod ve çok replika gerçeği, logu dışarı akıtıp merkezîleştirmeyi zorunlu kılar.
## Doğru
D
## Açıklama
Kubernetes pod'ları **geçicidir** (ephemeral): bir pod öldüğünde, yeniden planlandığında (rescheduled) veya ölçek düşürüldüğünde diskindeki loglar **kaybolur** — tam da hata sonrası incelemek istediğin an gitmiş olur. Ayrıca bir servis 20 replika ile çalışıyorsa hangi pod'a gireceğini bilemezsin; tek tek `exec` ölçeklenmez. Bu yüzden loglar bir ajanla (Fluent Bit/Promtail) **pod'dan dışarı akıtılıp** merkezî bir sisteme (ELK/Loki) kalıcılaştırılır ve tek yerden, correlation ID ile aranır. `exec` yasak değildir (A yanlış); merkezî loglamanın farkı estetik değil tekniktir (B yanlış); `tail` de log dosyasını kilitleyip silmez (C yanlış). Doğru gerekçe geçicilik ve ölçektir (D).

# log-6 | İleri
## Soru
Prometheus'un metrik toplama modeli ve loglardan farkı hakkında hangisi doğrudur?
## A
Prometheus her servise bağlanıp metrikleri onlara gönderir (push) ve aynı satırları log olarak da saklar; metrik ile log tek bir veri türüdür
🤖 yön terstir, Prometheus veriyi çeker; ayrıca metrik ile log ayrı veri türleridir.
## B
Prometheus metrikleri servislerin uç noktasından çeker (pull); metrikler sayısal zaman serisidir, loglar ise ayrık olay kayıtlarıdır
🤖 Doğru: çekilen sayısal zaman serileri ile olay kayıtları farklı amaçlara hizmet eder.
## C
Metrikler ve loglar aynı verinin iki adıdır; Prometheus gelen tüm log satırlarını tam metin indeksleyip arama sunar
🤖 Prometheus tam metin log indekslemez; sakladığı şey sayısal zaman serileridir.
## D
Prometheus yalnız en son okuduğu anlık değeri saklar; geçmişi görmek için ayrıca bir zaman serisi veritabanı kurmak gerekir
🤖 Prometheus'un özü zaman serisidir; değerlerin geçmiş seyrini saklar ve PromQL ile sorgular.
## Doğru
B
## Açıklama
Prometheus bir **çekme (pull) modeli** kullanır: yapılandırılmış hedeflerin metrik uç noktalarını (Spring'de `/actuator/prometheus`) düzenli aralıklarla **kazır** (scrape) — servisler Prometheus'a veri itmez (A yanlış; kısa ömürlü işler için Pushgateway istisnadır). Sakladığı şey **sayısal zaman serileridir** (bir metriğin zamana göre değeri), ve PromQL ile toplulaştırma/oran/tahmin yapılır (D yanlış). **Log** ise ayrık, bağlam-zengin **olay kayıtlarıdır** ve tam-metin/etiketli sorgulanır (ELK/Loki). İkisi gözlemlenebilirliğin **ayrı sütunlarıdır**: metrik "ne kadar/ne oranda" (toplu eğilim, alarm), log "tam olarak ne oldu" (tekil olay ayrıntısı). Prometheus tam-metin log indekslemez (C).
