---
slug: mulakat-anlatim-teknikleri
baslik: "Mülakat Anlatım Teknikleri: Zayıf Noktayı Çerçeveleme, Bilmiyorum Demek ve Projeyi Kanıta Çevirmek"
kademe: Orta
sira: 256
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Aynı gün, aynı pozisyon için iki aday geliyor. İkisinin de özgeçmişi neredeyse aynı: on yıla yakın Java, Spring Boot, PostgreSQL, mikroservis. İkisinin de Oracle deneyimi yok. Mülakatçı ikisine de aynı soruyu soruyor: "Bizde veri tabanı Oracle, PL/SQL tarafında da iş var. Oracle deneyiminiz nasıl?"

Birinci aday: "Oracle kullanmadım ama SQL sonuçta SQL, kısa sürede alışırım." İkinci aday: "Oracle ile üretimde çalışmadım. PostgreSQL ile çalıştım ve orada yürütme planı okumaya, indeks tasarımına ve büyük tabloları bölümlemeye kadar indim. Oracle'a geçtiğimde farkın nerede olduğunu biliyorum. Kimlik üretimi `sequence.NEXTVAL` ile yapılıyor. Saklı yordamlar `DECLARE/BEGIN/EXCEPTION/END` blok yapısında yazılıyor. Varsa güncelle, yoksa ekle (upsert) işlemi için `MERGE` var. Sorgu iyileştiriciye (optimizer) yön veren ipucu (hint) yazılabiliyor. Bir de sessiz bir tuzak var: Oracle boş metni `NULL` sayıyor. Benim kapatmam gereken kısım PL/SQL yazımı ve Oracle'ın plan çıktısını okuma biçimi. İlk haftalarda mevcut paketlerden birini satır satır okuyarak başlarım."

İkisinin de **bildiği** aynı. Ama ilki "bu adam Oracle'ı hiç düşünmemiş" izlenimi bıraktı, ikincisi "bu adam Oracle'a hazırlanmış, farkı biliyor" izlenimi. Aradaki fark bilgi değil, **anlatım**. Bu konu ne bildiğinle ilgilenmiyor; bildiğini nasıl çerçevelediğinle, bilmediğini nasıl söylediğinle ve yaptığın işi nasıl kanıta çevirdiğinle ilgileniyor. Davranışsal soruların içeriği ayrı bir konudur (`242`); burada anlatılan şey o soruların **teslim biçimidir**.

### Temel kavramlar

Hiç duymadığını varsayarak:

- **Çerçeveleme (framing):** aynı olguyu farklı bir kurgu içinde sunmak. "Oracle bilmiyorum" ile "PostgreSQL'de derinim, Oracle farkları şunlar, kapatma yolum bu" aynı olgunun iki çerçevesidir; ikisi de doğrudur, biri eleyicidir.
- **Aktarılabilir deneyim (transferable experience):** teknoloji adı farklı olsa da altındaki problemi aynı olan deneyim. İndeks tasarımı, işlem yönetimi, eşzamanlılık, zarif kapanış — bunlar ürünle değil problemle tanımlanır, o yüzden taşınır.
- **Kalibrasyon:** emin olduğun bilgi ile tahmin ettiğin bilgiyi birbirinden ayırıp bunu karşı tarafa **duyurabilmek**. "Bundan eminim" ile "burada tahmin yürütüyorum" arasındaki farkı söylemek, mülakatçının sana ne kadar güvenebileceğini kalibre etmesini sağlar.
- **Köprü cümlesi:** "X'i kullanmadım, ama aynı problemi Y ile şöyle çözdüm" kalıbı. Boşluğu inkâr etmeden doldurur.
- **Kanıt (evidence):** iddianın gösterilebilir hali — çalışan kod, açılabilir bir repo, ekranda gösterilebilen bir demo, ölçülmüş bir sayı. "Yaparım" bir iddiadır; "şu an açıp gösterebilirim" kanıttır.
- **Pitch:** kendi işini üç dakikada, problemden ölçülebilir sonuca kadar anlatan hazır iskelet. Ezberlenir, sonra soruya göre kısaltılır.
- **Eleyici cevap:** teknik olarak yanlış olmasa bile tek başına olumsuz karar ürettiren cevap. Genellikle iki uçtan gelir: abartılı özgüven ya da aşırı alçakgönüllülük.

### Zayıf noktayı çerçeveleme: dört adımlı kalıp

Her mülakatta, ilanla senin geçmişin arasında en az bir boşluk vardır. Bu boşluk sorulduğunda çoğu aday iki uçtan birine savrulur. Birinci uç **abartılı özgüvendir**: "o da benziyor, hallederim." Bu cevap tek bir ayrıntı sorusuyla çöker. Çöktüğünde kaybettiğin şey o soru değildir; o ana kadar verdiğin bütün cevapların güvenilirliğidir. İkinci uç **aşırı alçakgönüllülüktür**: "hayır, o konuda deneyimim yok." Bu cevap dürüsttür ama mülakatçının elinde yalnızca senin eksiğini bırakır; o boşluğu kimse senin yerine dolduramaz.

Doğru cevap dört adımdan oluşur ve sırası önemlidir:

1. **Neyi bilmediğini dürüstçe söyle.** Tek cümle, özür yok, uzatma yok. Özür dilemek boşluğu büyütür; savunmaya geçmek boşluğu mülakatın merkezine taşır.
2. **Komşu deneyimini somutla.** Genel bir "benzer şeyler yaptım" değil; hangi problemi hangi araçla nereye kadar çözdüğün. Somutluk burada tek ölçüttür.
3. **Farkın ne olduğunu bildiğini göster.** Bu adım cevabın omurgasıdır ve en çok atlanan adımdır. Farkı sayabiliyorsan, o teknolojiyi kullanmamış olsan bile **üzerine düşünmüş** olduğunu kanıtlarsın. Farkı sayamayan bir aday, aslında ne bilmediğini de bilmiyordur.
4. **Nasıl kapatacağını söyle.** Takvimli ve somut: ilk hafta ne okuyacaksın, neyi kendi elinle çalıştıracaksın, kimden ne isteyeceksin. "Öğrenirim, hızlı öğrenirim" bu adımın boş versiyonudur ve hiçbir şey söylemez.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zayif noktayi cerceveleme kalibinin dort adimi ve iki hatali uc">
  <defs>
    <marker id="anlatim-adim" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"Bu teknolojiyi kullanmadım" — dört adımlı çerçeveleme</text>
  <rect x="18" y="44" width="148" height="82" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="92" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Dürüst sınır</text>
  <text x="92" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"Bunu kullanmadım."</text>
  <text x="92" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tek cümle · özür yok</text>
  <text x="92" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">savunmaya geçme</text>
  <line x1="168" y1="85" x2="178" y2="85" stroke="#5D7789" stroke-width="1.5" marker-end="url(#anlatim-adim)"/>
  <rect x="180" y="44" width="148" height="82" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="254" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Komşu deneyim</text>
  <text x="254" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hangi problemi</text>
  <text x="254" y="105" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">hangi araçla, nereye</text>
  <text x="254" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kadar çözdün</text>
  <line x1="330" y1="85" x2="340" y2="85" stroke="#5D7789" stroke-width="1.5" marker-end="url(#anlatim-adim)"/>
  <rect x="342" y="44" width="148" height="82" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="416" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Farkı say</text>
  <text x="416" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">cevabın omurgası</text>
  <text x="416" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">farkı sayamayan aday</text>
  <text x="416" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ne bilmediğini bilmiyordur</text>
  <line x1="492" y1="85" x2="502" y2="85" stroke="#5D7789" stroke-width="1.5" marker-end="url(#anlatim-adim)"/>
  <rect x="504" y="44" width="148" height="82" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="578" y="68" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">4 · Kapatma planı</text>
  <text x="578" y="88" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ilk hafta ne okurum,</text>
  <text x="578" y="105" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">neyi çalıştırırım</text>
  <text x="578" y="120" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"hızlı öğrenirim" değil</text>
  <rect x="18" y="146" width="206" height="86" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="121" y="170" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Uç 1 — abartılı özgüven</text>
  <text x="121" y="190" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1. adım atlanır: "o da benziyor"</text>
  <text x="121" y="208" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tek ayrıntı sorusunda çöker</text>
  <text x="121" y="225" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tüm cevaplar şüpheli hale gelir</text>
  <rect x="232" y="146" width="206" height="86" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="335" y="170" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Uç 2 — aşırı alçakgönüllülük</text>
  <text x="335" y="190" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yalnız 1. adım: "deneyimim yok"</text>
  <text x="335" y="208" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dürüst ama yarım</text>
  <text x="335" y="225" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">geriye yalnız eksiğin kalır</text>
  <rect x="446" y="146" width="206" height="86" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="549" y="170" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Dört adımın tamamı</text>
  <text x="549" y="190" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">boşluk kabul edilir ve doldurulur</text>
  <text x="549" y="208" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">güvenilirlik zarar görmez</text>
  <text x="549" y="225" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">konu "eksik"ten "plan"a döner</text>
</svg>
```

### Üç işlenmiş örnek: Oracle, Kubernetes, sektör

**"Oracle kullanmadım."** Bu cevabın gövdesi PostgreSQL derinliğidir. Ama derinlik "PostgreSQL biliyorum" diyerek gösterilmez; ne yaptığını söyleyerek gösterilir. Örnekler: yavaş sorguların yürütme planına bakıp indeks eklemek, var olan indeksin neden kullanılmadığını bulmak, bileşik indekste sütun sırasının önemini bilmek, büyük tabloları tarihe göre bölümlere ayırmak, gün sonu toplu işlerde plan değişimini izlemek.

Üçüncü adımda Oracle'a özgü farkları sayarsın. Kimlik üretimi `sequence.NEXTVAL` ile yapılır. Saklı yordamlar `DECLARE / BEGIN / EXCEPTION / END` blok yapısındadır. Varsa güncelle, yoksa ekle (upsert) işlemi için `MERGE` vardır. Sorgu iyileştiriciye (optimizer) yön veren ipucu (hint) yazılabilir. `DUAL` tablosu, `NVL`, `ROWNUM`, `VARCHAR2` gibi Oracle'a özgü isimler kullanılır. Sonuna sezgiye aykırı ama gerçek bir ayrıntı koy: **Oracle'da boş metin (`''`) `NULL` sayılır**, PostgreSQL'de sayılmaz. Bu yüzden `WHERE ad = ''` biçiminde yazılmış bir koşul Oracle'a taşındığında hata vermez; sessizce hiçbir satır döndürmez. Böyle bir ayrıntıyı söyleyen aday, kullanmadığı bir veri tabanına gerçekten hazırlanmış olduğunu kanıtlar.

Cevabı bir de ölçekleyerek bitir. Uygulama JPA/Hibernate üzerinden konuşuyorsa, veri tabanı lehçesi (dialect) farkının büyük kısmını çerçeve zaten gizler. Senin kapatman gereken alan asıl olarak PL/SQL yazımı ve Oracle'ın plan çıktısını okuma biçimidir. Ayrıntılar `227` numaralı konuda.

**"Kubernetes'i üretimde yönetmedim."** Bu cümledeki en önemli iş, **iki farklı rolü ayırmaktır**. Küme (cluster) yönetimi platform ekibinin işidir: düğüm (node) ekleyip çıkarmak, etcd yedeği almak, sürüm yükseltmek, ağ eklentisi kurmak. Bu işler çoğu kurumda geliştiricinin eline zaten geçmez. Senin iddian "küme yönettim" değil, "**bu platforma uygulama kuran (deploy eden) geliştiriciydim**" olmalı.

Bu rolün içeriği fazlasıyla somuttur. Uygulamanın Dockerfile'ını yazmak ve imaj katmanlarını küçültmek (`214`). `deployment`, `service` ve `configmap` tanım dosyalarını (manifest) değiştirmek (`216`, `217`). **Ayakta mı denetimi (liveness) ile hazır mı denetimini (readiness) ayırmak**: liveness kabı yeniden başlatır, readiness yalnız trafikten çıkarır. İkisini aynı uca bağlamak, yavaş açılan bir uygulamada sonsuz yeniden başlatma üretir (`218`). `SIGTERM` sinyali geldiğinde açık isteklerin bitmesini bekleyen zarif kapanışı ayarlamak (`215`). Ve **kaynak isteği/sınırı (resource request/limit) ile JVM yığın (heap) ayarını uyumlu tutmak**: bellek sınırının altında bırakılmayan bir yığın, kabın `OOMKilled` ile ölmesi demektir.

Teşhis tarafını da somutla: `oc logs` ve `describe pod` çıktısıyla `CrashLoopBackOff` ile `OOMKilled`'ı ayırt edebildiğini söyle (`219`). Bu cevap, küme yönetmediğini saklamadan seni "Kubernetes'e yabancı" kutusundan çıkarır.

**"Sektör deneyimim yok."** Burada durum ilk iki örnekten farklıdır. Sektör deneyimi tanım gereği geçmişte kazanılır; bir hafta içinde kapatılamaz. O yüzden ikinci ve dördüncü adımlar birleşir ve tek bir soruya dönüşür: **iş alanını (domain) öğrenmek için fiilen ne yaptın?** "Deneyimim yok ama öğrenmeye açığım" cümlesi ile "alanı öğrenmek için şunu yaptım, isterseniz göstereyim" cümlesi arasında uçurum vardır. İkincisi, alan bilgisini bir niyet olmaktan çıkarıp kanıta çevirir.

Kanıt üç biçimde verilir. Birincisi, alanın yaşam döngüsünü doğru sırayla sayabilmek: bankada başvuru → skorlama → tahsis → kullandırım → taksit planı → tahsilat → takip; havacılıkta rezervasyon → PNR → biletleme → check-in → uçuş → mutabakat. İkincisi, alanın kendi terimlerini doğru kullanmak. Üçüncüsü, o alanın kurallarını kodlayan küçük ama çalışan bir şey yazmış olmak. Kredi tarafında örnek: taksit planı üreten, faiz ve vergi kalemlerini ayrı satırlarda tutan, kuruş farkını son taksite yazan bir modül. Ödeme tarafında örnek: idempotency anahtarıyla (aynı istek ikinci kez gelse de tek kez işlensin diye kullanılan anahtar) çift çekimi engelleyen bir akış. Bunlar iş deneyiminin yerine geçmez ve geçtiğini iddia etmemelisin. Ama "bu insan alanı ciddiye almış" sinyalini tek başına üretir. Alan içeriği için `239` (kredi), `247` (havacılık), `250`-`253` (ödeme).

### "Bilmiyorum"u doğru söylemek

Mülakatta uydurmanın maliyeti sanıldığından **asimetriktir**; yani kazanç ile kayıp aynı boyda değildir. Kazandığın şey tek bir sorudur; kaybettiğin şey ise tüm mülakattır. Kıdemli bir mülakatçı sorduğu şeyin cevabını bilir; uydurulmuş cevabı anında yakalar ve o andan itibaren daha önce verdiğin doğru cevaplara da "acaba bu da mı ezber" diye bakar. Tek bir uydurma, geriye dönük olarak her şeyi şüpheli hale getirir. Bu yüzden "bilmiyorum" demek bir kayıp değil, **maliyeti sınırlama hamlesidir.**

Ama kuru bir "bilmiyorum" da eksik cevaptır: dürüsttür, sana zarar vermez, ama hiçbir bilgi de üretmez. Üç parçalı bir yapı kur. Önce sınırı söyle. Sonra **köprü kur**: "Bunu kullanmadım, ama aynı problemi şununla şöyle çözdüm." Köprünün işe yaraması için problemin aynı olduğunu göstermen gerekir. Bazı kavramlar araç değişse de aynı kalır: dağıtık işlem yerine adım adım telafili işlem zinciri (saga), çift yazma yerine giden kutusu tablosu (outbox), yeniden denemede tekrar edilse de sonucu değişmeyen işlem (idempotency).

Üçüncü olarak, tahmin yürütüyorsan **tahmin yürüttüğünü söyle**. Kalıp şudur: "Emin değilim, ama muhakememi söyleyeyim." Ardından muhakemeni açarsın: bu mekanizma şöyle çalışıyorsa sonuç şu olmalı; pratikte dokümandan ya da küçük bir denemeyle doğrularım.

Bu cümle mülakatçıya en değerli şeyi verir: senin bilgi ile tahmini birbirinden ayırabildiğini. Aynı disiplini kod içinde de gösteriyor olman gerekir; emin olmadığı yeri işaretleyen geliştirici, üretimde en az sorun çıkaran geliştiricidir.

```svg
<svg viewBox="0 0 660 246" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bilmedigin bir soruya verilebilecek uc cevap yolu ve sonuclari">
  <defs>
    <marker id="anlatim-yol" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bilmediğin soru geldi — üç yol, üç sonuç</text>
  <rect x="18" y="42" width="190" height="56" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="113" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Uydur</text>
  <text x="113" y="85" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">emin tonla makul cevap üret</text>
  <line x1="210" y1="70" x2="228" y2="70" stroke="#5D7789" stroke-width="1.5" marker-end="url(#anlatim-yol)"/>
  <rect x="232" y="42" width="420" height="56" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="442" y="64" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">mülakatçı doğrusunu bilir → tek yakalanma her cevabı geriye dönük şüpheli yapar</text>
  <text x="442" y="84" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">kazanç: 1 soru · kayıp: tüm mülakat</text>
  <rect x="18" y="108" width="190" height="56" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="113" y="132" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Kuru "bilmiyorum"</text>
  <text x="113" y="151" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">söyle ve sus</text>
  <line x1="210" y1="136" x2="228" y2="136" stroke="#5D7789" stroke-width="1.5" marker-end="url(#anlatim-yol)"/>
  <rect x="232" y="108" width="420" height="56" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <text x="442" y="130" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">zarar vermez ama bilgi de üretmez → düşünme biçimin görünmez kalır</text>
  <text x="442" y="150" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">nötr: fırsat harcanır</text>
  <rect x="18" y="174" width="190" height="60" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="113" y="196" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sınır + köprü + kalibrasyon</text>
  <text x="113" y="214" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"kullanmadım · aynı problemi</text>
  <text x="113" y="229" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">şöyle çözdüm · burası tahmin"</text>
  <line x1="210" y1="204" x2="228" y2="204" stroke="#5D7789" stroke-width="1.5" marker-end="url(#anlatim-yol)"/>
  <rect x="232" y="174" width="420" height="60" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="442" y="196" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dürüstlük korunur · aktarılabilir deneyim görünür olur</text>
  <text x="442" y="214" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">bilgi ile tahmini ayırabildiğin kanıtlanır</text>
  <text x="442" y="230" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">kazanç: güvenilirlik</text>
</svg>
```

### Kendi işini anlatmak: üç dakikalık tanıtım (pitch) ve STAR ile hata hikâyesi

Mülakatın bir yerinde mutlaka "bize bir projenizden bahseder misiniz" gelir. Burada en sık yapılan hata **teknoloji listesi saymaktır**: "Spring Boot, PostgreSQL, Kafka, Docker kullandık." Bu bir alışveriş listesidir; içinde tek bir karar yoktur, dolayısıyla senin hakkında hiçbir şey söylemez. Onun yerine ezberlenecek dört parçalı bir iskelet kur ve toplam üç dakikayı geçme.

**Problem** (yaklaşık yarım dakika): neyi çözüyordu, kim acı çekiyordu. **Senin rolün** (yarım dakika): ekipte kaç kişiydiniz, hangi parça senindi — "biz" değil "ben". **Teknik kararlar ve gerekçeleri** (bir buçuk dakika, pitch'in ağırlık merkezi): iki üç karar seç ve her birini "şunu seçtim çünkü şu alternatife göre şunu kazandırdı, karşılığında şunu feda ettim" kalıbında anlat. **Ölçülebilir sonuç** (yarım dakika): süre, hata sayısı, maliyet, kapasite. Ölçemediğin bir işte bile "aynı hata bir daha çıkmadı", "gün sonu işi altı saatten iki saate indi" gibi somut bir çıktı bulunur.

Pitch'i bitirirken kullanabileceğin en güçlü cümle şudur: **"İsterseniz anlatmak yerine göstereyim."** Anlatmak bir iddiadır, göstermek kanıttır ve mülakatçının zihninde ikisi aynı rafta durmaz. Çalışan bir demo, ekranı paylaşıp gezdirebileceğin bir repo, ya da özgeçmişte duran bir bağlantı, on dakikalık anlatımın yapamadığı şeyi otuz saniyede yapar. Kendi yazdığın bir proje varsa sınırlarını da kendin söyle — tek kullanıcı, gerçek yük altında denenmedi — çünkü sınırı kendin söylediğinde geri kalan her iddian daha inandırıcı olur.

Hata (bug) hikâyesi ayrı bir türdür ve **STAR** iskeletiyle anlatılır: Durum, Görev, Aksiyon, Sonuç. İskeletin kendisi `195` ve `242` numaralı konularda ayrıntılı işlenir. Anlatım tekniği açısından burada iki şey belirleyicidir.

Birincisi, hikâyeyi mutlaka **ölçülebilir bir sonuçla** bitirmektir. "Sonra düzeldi" bir sonuç değildir. "Günde ortalama kırk hatalı kayıt üretiliyordu; düzeltmeden sonra sıfırlandı ve aynı senaryoyu yakalayan bir test eklendi" sonuçtur.

İkincisi ve daha az bilineni: aksiyon kısmında **yöntem cümleleri** kullanmaktır. Örnekler: "Önce kodu okumaya değil çıktıya baktım." "Hatayı elimde tekrar üretebilir hale getirdim; her denemede aynı sonucu almadan düzeltmeye başlamadım." "Hipotezi tek bir değişkeni değiştirerek test ettim." Bu cümleler, hangi hatayı çözdüğünden çok daha fazla bilgi taşır. Çünkü mülakatçı senin bir daha o hatayla karşılaşmayacağını biliyor; merak ettiği şey **bir sonrakinde nasıl davranacağın**. Sistematik hata ayıklama dilini kullanan aday, henüz görmediği bir hatayı da çözeceği izlenimi bırakır.

```svg
<svg viewBox="0 0 660 212" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uc dakikalik proje anlatimi iskeleti ve zaman butcesi">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç dakikalık proje pitch'i — zaman bütçesi</text>
  <rect x="18" y="46" width="105" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="70" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Problem</text>
  <text x="70" y="89" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kim acı çekiyordu</text>
  <text x="70" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">~30 sn</text>
  <rect x="123" y="46" width="105" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="175" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Senin rolün</text>
  <text x="175" y="89" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"biz" değil "ben"</text>
  <text x="175" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">~30 sn</text>
  <rect x="228" y="46" width="315" height="66" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="385" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Teknik kararlar ve gerekçeleri</text>
  <text x="385" y="89" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"şunu seçtim çünkü ... karşılığında şunu feda ettim"</text>
  <text x="385" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">~90 sn · ağırlık merkezi · teknoloji listesi değil, karar</text>
  <rect x="543" y="46" width="109" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="597" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Ölçülebilir sonuç</text>
  <text x="597" y="89" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">süre · hata · maliyet</text>
  <text x="597" y="105" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">~30 sn</text>
  <rect x="18" y="126" width="634" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="335" y="150" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Bitiriş cümlesi: "İsterseniz anlatmak yerine göstereyim."</text>
  <text x="335" y="171" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">anlatmak = iddia · göstermek = kanıt — çalışan demo, açılabilir repo, ekranda gezilen akış</text>
  <text x="335" y="188" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">projenin sınırını kendin söyle: sınırı kabul eden aday, geri kalan iddialarında daha inandırıcı olur</text>
</svg>
```

### Zor mülakatçıyla soğukkanlılık

Her mülakatçı nazik değildir. Bazısı sert sorar, bazısı cevabını yarıda böler, bazısı küçümseyici bir ton kullanır; bir kısmı bunu bilinçli yapar — baskı altında nasıl davrandığını görmek ister. Bu durumda ölçülen şey teknik bilgi değil, **duruşundur**. Bölünmeye sinirlenip sesini yükselten aday da, ilk sert cümlede toparlanamayıp cevaplarını kısaltan aday da aynı kapıya çıkar.

Pratik olarak üç davranış işe yarar. Birincisi: bölündüğünde direnme, kısalt. Mülakatçı seni bölüyorsa çoğu zaman cevabın çok uzadığını söylüyordur; "en kritik noktayı söyleyeyim" deyip özü ver. İkincisi: küçümseyici tonu kişiselleştirme, içeriğe dön. "Haklı olabilirsiniz, benim yaklaştığım açı şuydu" cümlesi tonu düşürür ve tartışmayı teknik zemine çeker.

Üçüncüsü ve en zoru: **mülakatçı teknik olarak yanlış bir şey söylediğinde** ne yapacağın. Burada iki kolay yol da yanlıştır. Susmak, yanlış iddiayı onaylamak anlamına gelir ve konuyu bildiğin izlenimini de siler. Sert bir "hayır, yanlış biliyorsunuz" ise gereksiz bir çatışma açar. Doğru yol nazik ama net itirazdır: "Benim bildiğim biraz farklı — şu mekanizma yüzünden şöyle davrandığını düşünüyorum. Yanılıyor olabilirim, sizin gördüğünüz durum farklıysa merak ettim." Bu cümle üç şeyi aynı anda yapar: bilgini net biçimde ortaya koyar, karşı tarafa çıkış bırakır, öğrenmeye açık olduğunu gösterir. Deneyimli bir mülakatçı bunu meydan okuma değil, olgunluk işareti olarak okur. Hatta bazı mülakatlarda yanlış iddia **kasten** ortaya atılır — tam da itiraz edip etmediğini görmek için.

### Sıra sende: soracağın sorular ve maaş

"Sorunuz var mı?" faslı nezaket değil, değerlendirmenin bir kalemidir. "Sorum yok" cevabı neredeyse her zaman ilgisizlik ya da hazırlıksızlık olarak okunur. İnternette bulunabilecek şeyleri sorma. İşe yarayan beş başlık şudur:

- **Ekip yapısı:** kaç kişi, nasıl bölünmüş, senin gireceğin parça hangisi.
- **Teknoloji sürümleri ve yükseltme planı:** hangi Java, hangi Spring sürümü, yükseltme takvimi var mı. Bu soru sana gireceğin kod tabanının yaşı hakkında da bilgi verir.
- **İlk üç ayda beklenen somut çıktı:** bu rolde başarı neye benziyor.
- **Kod inceleme ve test kültürü:** inceleme zorunlu mu, otomatik test kapsamı ne durumda, üretime ne sıklıkla çıkılıyor.
- **Sürecin sonraki adımı ve takvimi:** kaç aşama kaldı, geri dönüş ne zaman.

Son madde çoğu adayın sormayı unuttuğu ama en pratik olanıdır: paralel yürüttüğün diğer süreçleri buna göre zamanlarsın.

Maaş konuşmasında anlatım tekniği açısından dört kural vardır. Birincisi: tek rakama kilitlenme, **bant ver**. Ama bandın alt ucunu gerçekten kabul edebileceğin bir sayı seç; karşı tarafın hatırladığı sayı genellikle alt uçtur. İkincisi: **net mi brüt mü** konuştuğunuzu açıkça netleştir. Türkiye'de bu ayrımın atlanması, iki tarafın farklı sayılar düşünerek anlaştığı ve teklif aşamasında bozulan konuşmalar üretir.

Üçüncüsü: yan hakları maaşın parçası olarak masaya koy — yemek, yol, özel sağlık sigortası, uzaktan çalışma günü. Bunlar toplam paketi ciddi biçimde değiştirir ve çoğu zaman taban ücretten daha esnektir. Dördüncüsü: klasik "şu an ne kadar alıyorsunuz?" sorusuna, mevcut rakamı çıpaya çevirmeden **hedef bandınla** cevap ver. Örnek: "Mevcut paketimi paylaşmak yerine, bu rol için beklentimi söyleyeyim — şu bant arasında düşünüyorum, toplam pakete göre değerlendiririm." Pazarlık gücünün gerçek kaynağı ne cümle kalıbıdır ne de kararlılık; paralel yürüyen ikinci bir süreçtir. Maaş sorusunun bankacılık bağlamındaki daha geniş işlenişi `242` numaralı konudadır.

### Mülakatta ne sorulur?

Bu konunun soruları ayrı bir başlık altında gelmez; **her sorunun içine gömülüdür**. "Oracle deneyiminiz nasıl", "Kubernetes'te ne yaptınız", "bir projenizi anlatın", "çözdüğünüz zor bir hatayı anlatın", "bunu bilmiyor musunuz", "maaş beklentiniz", "sorunuz var mı" — hepsinde ölçülen şey ikilidir: bildiğin ve **bildiğini nasıl teslim ettiğin**. Sık düşülen tuzaklar şunlardır. Boşluğu inkâr edip abartmak. Boşluğu kabul edip orada bırakmak. Farkı sayamamak, yani üçüncü adımı atlamak. "Hızlı öğrenirim" gibi içi boş kapatma vaatleri vermek. Teknoloji listesi sayarak proje anlatmak. Sonuçsuz hata hikâyesi anlatmak. Tahmini bilgi gibi sunmak. Yanlış bir teknik iddia karşısında susmak. Soru sormamak. Maaşta net/brüt ayrımını atlamak.

Gerçek mülakat kayıtlarından örnekler için `255`. Hangi konunun hangi ilan maddesine karşılık geldiği için `238` (banka) ve `246` (havacılık). Davranışsal soruların içeriği için `242` ve `195`.

### Tek cümlede

Mülakatta değerlendirilen şey, bilginin kendisi kadar teslim biçimidir: eksiğini dört adımda çerçevele, bilmediğinde köprü kur ve tahminini tahmin diye işaretle, projeni kararla ve ölçülebilir sonuçla anlat, mümkünse anlatmak yerine göster.

# anlatim-1 | Orta
## Soru
Banka mülakatında yazılım müdürü şunu soruyor: "Bizde veri tabanı Oracle ve PL/SQL tarafında da iş var. Oracle deneyiminiz nasıl?" Adayın Oracle deneyimi yok, PostgreSQL ile yıllardır çalışıyor. Hangi cevap en güçlüdür?
## A
"Oracle kullanmadım ama SQL sonuçta SQL; PostgreSQL bilen biri Oracle'a bir haftada alışır, bu benim için sorun olmaz" demek
🤖 farkı hiç saymayan bu cevap, o teknolojiye hazırlanmadığını gösterir; ayrıca tek bir ayrıntı sorusuyla boşa düşer.
## B
"Hayır, Oracle deneyimim yok" deyip cevabı orada bitirmek; dürüst olmak yeterlidir, fazlası kendini pazarlamak olur
🤖 dürüsttür ama yarımdır; PostgreSQL derinliğin ve Oracle farkları hakkında hiçbir şey söylemediğin için geriye yalnız eksiğin kalır.
## C
Oracle kullanmadığını söyleyip PostgreSQL'de yaptığı işi somutlamak (yürütme planı, indeks tasarımı, bölümleme), Oracle'a özgü farkları saymak ve kapatma planını takvimlemek
🤖 Doğru: dört adımın tamamı uygulanmış; boşluk kabul edilirken hem derinlik hem hazırlık kanıtlanmış olur.
## D
"Zaten JPA/Hibernate kullanıyoruz, dialect farkını çerçeve soyutluyor; hangi veri tabanı olduğu pratikte fark etmiyor" demek
🤖 doğru bir tespiti soruyu geçiştirmek için kullanmak kaçamaktır; PL/SQL ve plan okuma zaten ORM'in soyutlamadığı kısımdır.
## Doğru
C
## Açıklama
Bu soruda ölçülen şey Oracle bilgisi değil, **eksiği çerçeveleme becerisidir** ve doğru cevap dört adımlıdır: sınırı dürüstçe söyle, komşu deneyimi somutla, farkı say, kapatmayı takvimle. Üçüncü adım cevabın omurgasıdır — `sequence.NEXTVAL`, `DECLARE/BEGIN/EXCEPTION/END` blok yapısı, `MERGE` ile upsert, hint kavramı, `DUAL`/`NVL`/`ROWNUM`/`VARCHAR2` ve özellikle **Oracle'ın boş string'i `NULL` sayması** gibi farkları sayabilen aday, o veri tabanını kullanmamış olsa bile üzerine düşünmüş olduğunu kanıtlar; farkı sayamayan aday ise aslında ne bilmediğini de bilmiyordur. A abartılı özgüven ucudur ve tek ayrıntı sorusunda çöker. B aşırı alçakgönüllülük ucudur: dürüsttür ama boşluğu doldurmaz, mülakatçının elinde yalnızca senin eksiğin kalır. D'deki tespit teknik olarak doğrudur — ORM dialect farkının büyük kısmını gerçekten soyutlar — ama soruyu geçiştirmek için kullanıldığında kaçamağa dönüşür; üstelik sorunun asıl işaret ettiği PL/SQL yazımı ve plan okuma tam da ORM'in soyutlamadığı alandır. Oracle ayrıntıları `227` numaralı konuda.

# anlatim-2 | Orta
## Soru
Mülakatçı, adayın hiç çalışmadığı bir konuda soru soruyor. Aday konuyu tam bilmiyor ama mekanizma hakkında makul bir tahmini var. Hangi anlatım biçimi doğrudur?
## A
Bilmediğini söyleyip aynı problemi hangi araçla çözdüğünü anlatmak, ardından tahmin yürüttüğü yeri açıkça "burada emin değilim, muhakememi söyleyeyim" diye işaretlemek
🤖 Doğru: sınır, köprü ve kalibrasyon birlikte verildiğinde dürüstlük korunur ve düşünme biçimi de görünür olur.
## B
Tahminini emin bir tonla, sanki bildiği bir konuymuş gibi anlatmak; kararsız görünmek bilgisizlikten daha kötü değerlendirilir ve kendinden emin bir anlatım güven izlenimi bırakır
🤖 kıdemli mülakatçı doğrusunu bilir; yakalanan tek uydurma, daha önce verilmiş doğru cevapları da geriye dönük şüpheli hale getirir.
## C
"Bilmiyorum" deyip susmak ve bir sonraki soruyu beklemek; en güvenli hamle konuyu uzatmamak ve kalan süreyi bilinen konulara ayırmaktır
🤖 dürüsttür ve zarar vermez ama hiçbir bilgi de üretmez; muhakemeni gösterme fırsatı harcanmış olur.
## D
Soruyu "biz o problemi hiç yaşamadık" diyerek gereksiz göstermek ve konuşmayı iyi bildiği bir konuya çevirip orada derinleşmek
🤖 konuyu küçümseyip kaydırmak kaçınma olarak okunur; mülakatçı çoğu zaman aynı soruya geri döner.
## Doğru
A
## Açıklama
Uydurmanın maliyeti **asimetriktir**: kazanılan şey tek bir soru, kaybedilen şey mülakatın tamamıdır. Kıdemli bir mülakatçı sorduğu şeyin cevabını bildiği için uydurmayı anında yakalar ve o andan sonra doğru verilmiş cevaplara da güvenmez. Bu yüzden B en pahalı seçenektir. C zarar vermez ama nötrdür; dürüstlük tek başına bilgi üretmez ve mülakatın asıl ölçtüğü akıl yürütme hiç görünmez. D, soruyu küçümseyerek kaydırmaktır ve kaçınma olarak okunur. Doğru yapı üç parçalıdır: **sınır** ("bunu kullanmadım"), **köprü** ("aynı problemi şununla şöyle çözdüm" — dağıtık işlem yerine saga, çift yazma yerine outbox, tekrar denemede idempotency gibi kavramlar araç değişse de aynı kalır) ve **kalibrasyon** ("burada tahmin yürütüyorum, pratikte dokümandan veya küçük bir denemeyle doğrularım"). Üçüncü parça en değerli sinyali verir: bildiğin ile tahmin ettiğini ayırabilen bir geliştirici, üretimde en az sürpriz çıkaran geliştiricidir.

# anlatim-3 | Orta
## Soru
İlanda Kubernetes yazıyor. Aday uygulamalarını Kubernetes'e deploy etmiş ama cluster'ı hiç yönetmemiş: node, etcd, sürüm yükseltme hep platform ekibindeymiş. "Kubernetes deneyiminiz nedir?" sorusuna hangi cevap doğrudur?
## A
"Kubernetes'te çalıştım" deyip cluster yönetimi ile uygulama dağıtımı arasındaki ayrımı hiç açmamak; ayrımı açmak deneyimi küçültür ve ayrıntı sorulursa o zaman açıklamak daha güçlü bir izlenim bırakır
🤖 muğlak bırakılan iddia, gelen ilk cluster yönetimi sorusunda dağılır ve muğlaklığın kasıtlı olduğu izlenimi doğar.
## B
"Kubernetes bilmiyorum, o işi platform ekibi yapıyordu" demek; cluster yönetmeyen birinin Kubernetes deneyimi olduğunu söylemesi doğru olmaz ve fazla iddialı görünmektense sınırı geniş çizmek daha güvenlidir
🤖 fazla geri çekilen bu cevap, fiilen sahip olduğun deploy eden geliştirici deneyimini tümüyle görünmez kılar.
## C
Kubernetes'i öğrenmek için izlediği eğitimleri ve okuduğu dokümanları anlatmak; teorik hazırlık pratiğin yerini tutar ve tamamlanan eğitimler öğrenme isteğinin somut kanıtı olarak sunulabilir
🤖 eğitim listesi kanıt değildir; mülakatçı ne izlediğini değil, çalışan bir sistemde ne yaptığını sorar.
## D
Cluster yönetiminin platform ekibinde olduğunu net söyleyip kendi rolünü somutlamak: Dockerfile, deployment/service/configmap, liveness-readiness ayrımı, zarif kapanış, resource limit ile JVM heap uyumu ve CrashLoopBackOff/OOMKilled teşhisi
🤖 Doğru: iki rolü ayırıp kendi rolünü somut ayrıntılarla doldurmak hem dürüst hem güçlüdür.
## Doğru
D
## Açıklama
Bu cevabın anahtarı **iki rolü ayırmaktır**: cluster yönetimi (node, etcd, sürüm yükseltme, ağ eklentisi) platform ekibinin işidir; senin iddian "**bu platforma deploy eden geliştiriciydim**" olmalıdır. Bu rol boş bir rol değildir ve somutlandığında oldukça zengindir: Dockerfile ve imaj katmanları (`214`), `deployment`/`service`/`configmap` manifest'leri (`216`, `217`), **liveness ile readiness ayrımı** — liveness kabı yeniden başlatır, readiness yalnız trafikten çıkarır; ikisini aynı uca bağlamak yavaş açılan bir uygulamada sonsuz yeniden başlatma üretir (`218`) — `SIGTERM` sonrası zarif kapanış (`215`), resource request/limit ile JVM heap uyumu ve `OOMKilled` ile `CrashLoopBackOff` ayrımı (`219`). A muğlaklık üretir ve ilk cluster sorusunda dağılır; üstelik muğlaklığın kasıtlı olduğu izlenimi güveni doğrudan vurur. B gereğinden fazla geri çekilir ve gerçekten sahip olduğun deneyimi görünmez kılar — bu, aşırı alçakgönüllülük ucunun tipik zararıdır. C eğitim listesiyle pratiğin yerini doldurmaya çalışır; izlenen kurs bir kanıt değildir, çalışan bir sistemde alınan karar kanıttır.

# anlatim-4 | İleri
## Soru
Mülakatın ortasında "Bize bir projenizden bahsedin" deniyor. Adayın kendi yazdığı, çalışır durumda ve repo'su açılabilir bir projesi var. Hangi anlatım biçimi en güçlü sinyali üretir?
## A
Projede kullanılan tüm teknolojileri sırayla saymak (Spring Boot, PostgreSQL, Kafka, Docker, mimari testler) ve her birinin ne olduğunu kısaca açıklamak
🤖 teknoloji listesi bir alışveriş listesidir; içinde karar olmadığı için adayın düşünme biçimi hakkında hiçbir şey söylemez.
## B
Problem, kendi rolü, iki üç teknik karar ve gerekçesi, ölçülebilir sonuç sırasıyla üç dakikada anlatmak ve "isterseniz anlatmak yerine göstereyim" diyerek çalışan hali açmayı önermek
🤖 Doğru: karar odaklı iskelet düşünme biçimini gösterir, gösterme önerisi ise iddiayı kanıta çevirir.
## C
Projeyi baştan sona kronolojik anlatmak: ilk hafta ne yaptı, sonra neyi değiştirdi, hangi kütüphaneyi denedi, en son nereye geldi
🤖 kronolojik anlatım süreyi doldurur ama sinyali seyreltir; mülakatçı zaman çizelgesini değil, alınan kararları değerlendirir.
## D
Projenin eksiklerini ve yapamadıklarını öne çıkarmak, gerçek bir üretim sistemi olmadığını baştan uzun uzun vurgulamak
🤖 sınırı söylemek doğrudur ama anlatımın merkezine konduğunda proje bir kanıt olmaktan çıkıp bir özre dönüşür.
## Doğru
B
## Açıklama
Proje anlatımının iskeleti dört parçalıdır ve ağırlık merkezi üçüncü parçadadır: **problem** (kim acı çekiyordu), **senin rolün** ("biz" değil "ben"), **teknik kararlar ve gerekçeleri** ("şunu seçtim çünkü şu alternatife göre şunu kazandırdı, karşılığında şunu feda ettim") ve **ölçülebilir sonuç**. A'daki teknoloji listesi en zayıf versiyondur çünkü tek bir karar içermez; aynı listeyi projeyi hiç yazmamış biri de sayabilir. C kronolojiye yaslanır, süreyi doldurur ama sinyali seyreltir — mülakatçı zaman çizelgesini değil kararları değerlendirir. D önemli bir doğruyu yanlış yere koyar: projenin sınırlarını **kendin söylemek** güven artırır, ama bunu anlatımın merkezine taşımak projeyi kanıt olmaktan çıkarıp özre dönüştürür; sınır, sonuçtan sonra tek cümleyle söylenir. B'nin ayırt edici üstünlüğü son hamlededir: **"anlatmak yerine göstereyim"** cümlesi, iddiayı kanıta çevirir. Çalışan bir demo ya da ekranda gezilen bir repo, on dakikalık anlatımın yapamadığını otuz saniyede yapar — ve mülakatçının zihninde iddia ile kanıt aynı rafta durmaz.

# anlatim-5 | İleri
## Soru
Mülakatçı sert bir tonda konuşuyor, adayın cevaplarını sık sık bölüyor ve bir noktada teknik olarak yanlış bir iddiada bulunuyor: adayın emin olduğu bir mekanizmayı tersinden anlatıyor. Aday ne yapmalı?
## A
Yanlışı düzeltmemek ve konuyu geçmek; mülakatçıyla tartışmak her durumda olumsuz sonuç doğurur, doğruyu bilmek yeterlidir
🤖 susmak yanlış iddiayı onaylamak anlamına gelir ve o konuyu bildiğin izlenimini de siler.
## B
"Hayır, bu yanlış" deyip doğrusunu net biçimde söylemek ve gerekirse mülakatçının nereden yanlış hatırladığını açıklamak
🤖 içerik doğru olsa da çerçeveleme gereksiz bir çatışma açar; karşı tarafa çıkış bırakmayan itiraz savunma refleksi doğurur.
## C
Kendi bildiğini net söyleyip gerekçesini vermek, ardından "yanılıyor olabilirim, sizin gördüğünüz durum farklıysa merak ettim" diyerek karşı tarafa alan bırakmak
🤖 Doğru: net itiraz ile öğrenmeye açıklık aynı cümlede verildiğinde hem bilgi hem olgunluk sinyali üretilir.
## D
Bölünmelere ve tona odaklanıp mülakatçının üslubunu nazikçe eleştirmek, ardından soruya dönmek
🤖 üslup tartışması teknik zemini tümüyle kaybettirir ve mülakatın kalan kısmını gerginleştirir.
## Doğru
C
## Açıklama
Sert, bölen ya da küçümseyen mülakatçı bazen gerçekten öyledir, bazen de bilinçli bir baskı testidir; her iki durumda da ölçülen şey **duruştur**. Yanlış bir teknik iddia karşısında iki kolay yol da hatalıdır: **susmak** (A) iddiayı onaylamak anlamına gelir ve konuyu bildiğin izlenimini siler — üstelik bazı mülakatlarda yanlış iddia tam da itiraz edip etmeyeceğini görmek için **kasten** ortaya atılır. Sert bir "hayır, yanlış" (B) içerik olarak doğru olsa bile karşı tarafa çıkış bırakmadığı için savunma refleksi doğurur ve tartışmayı egoya taşır. D ise teknik zemini tümüyle terk eder; üslup eleştirisi mülakat masasının işi değildir ve kalan süreyi gerginleştirir. Doğru yol **nazik ama net itirazdır**: kendi bilgini gerekçesiyle koy, sonra "yanılıyor olabilirim, sizin gördüğünüz durum farklıysa merak ettim" diyerek alan bırak. Bu cümle aynı anda üç iş yapar — bilgini ortaya koyar, çatışmayı önler, öğrenmeye açık olduğunu gösterir. Bölünme durumunda ise direnmek yerine kısaltmak işe yarar: bölünüyorsan çoğu zaman cevabın uzadığını söylüyorlardır.

# anlatim-6 | İleri
## Soru
Havacılık bilgi teknolojileri ekibi için görüşülüyor. Aday sektörde hiç çalışmamış ama son haftalarda alanı ciddi biçimde çalışmış ve alanın kurallarını uygulayan küçük bir proje yazmış. "Havacılık deneyiminiz yok, nasıl uyum sağlarsınız?" sorusuna hangi cevap en güçlüdür?
## A
Sektör deneyimi olmadığını net söyleyip alanı öğrenmek için fiilen ne yaptığını somutlamak — yaşam döngüsünü doğru sırayla sayabilmek, terimleri yerinde kullanmak ve alan kurallarını kodlayan çalışan projeyi göstermeyi önermek
🤖 Doğru: alan bilgisini bir niyetten çıkarıp gösterilebilir kanıta çevirdiği için tek başına ayırt edici olur.
## B
"Sektör deneyimim yok ama öğrenmeye çok açığım ve hızlı öğrenirim" deyip motivasyonunu vurgulamak; niyet ve istek göstermek, henüz sahip olunmayan bir deneyimi konuşmaktan daha dürüst bir yol olarak sunulur
🤖 içi doldurulmamış öğrenme vaadi her adayın söylediği cümledir; hiçbir ayırt edici bilgi taşımaz.
## C
Yazdığı projeyi gerçek sektör deneyimiyle eşdeğer sunmak; sonuçta aynı kuralları uygulayan çalışan bir sistem üretilmiş durumda ve çalışan kod, üzerinde çalışılan yılların yerini fazlasıyla tutar
🤖 yan projeyi iş deneyimi yerine koymak abartıdır ve ilk operasyonel ayrıntı sorusunda inandırıcılığı zedeler.
## D
Alanın öğrenilebilir olduğunu, asıl belirleyicinin teknik yetkinlik olduğunu ve sektörün pratikte fark etmediğini anlatmak; iyi bir mühendisin her alana kısa sürede uyum sağladığını örneklerle savunmak
🤖 alanı küçümsemek olumsuz okunur; operasyonel kısıtlar ve mevzuat bu alanlarda yazılım tasarımını doğrudan biçimlendirir.
## Doğru
A
## Açıklama
Sektör deneyimi, diğer boşluklardan farklı olarak bir haftada kapatılamaz; bu yüzden çerçevelemenin ikinci ve dördüncü adımları birleşir ve tek bir soruya dönüşür: **alanı öğrenmek için fiilen ne yaptın?** "Deneyimim yok ama öğrenmeye açığım" (B) ile "domain'i öğrenmek için şunu yaptım, isterseniz göstereyim" arasındaki fark, bir niyet ile bir kanıt arasındaki farktır — ilkini her aday söyler, ikincisini söyleyen az bulunur. Kanıt üç biçimde verilir: yaşam döngüsünü doğru sırayla sayabilmek (rezervasyon → PNR → biletleme → check-in → uçuş → mutabakat), alanın terimlerini yerinde kullanmak ve o alanın kurallarını uygulayan küçük ama çalışan bir şey yazmış olmak. C bu kanıtı fazla ileri taşır: yan proje iş deneyiminin yerine geçmez ve geçtiğini iddia etmek, ilk operasyonel ayrıntı sorusunda geri teper — doğru konumlandırma "iş deneyimi değil, alanı ciddiye aldığımın kanıtı" biçimindedir. D ise alanı küçümseyerek en kolay elenme yolunu seçer; havacılıkta da bankacılıkta da operasyonel kısıtlar, mevzuat ve geri alınamazlık yazılım tasarımını doğrudan biçimlendirir. Alan içeriği için `247` ve `254`, hazırlık haritası için `246`.
