---
slug: agentic-sistemler
baslik: "Agentic Sistemler: Agent Loop, Araç Orkestrasyonu ve Idempotent Çağrı"
kademe: Uzman
sira: 184
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir **LLM** (Large Language Model — büyük dil modeli) tek başına yalnızca metin yazar. **Ajan (agent)**, o modele **araç (tool)** verip bir hedefe ulaşana kadar tekrar tekrar çalıştırdığın sistemdir. Araç, modelin çağırabildiği gerçek bir işlevdir: "web'de ara", "veritabanından çek", "e-posta gönder", "ödeme al". Model artık yalnız yazmaz, **yapar** — ve bu hem gücün hem riskin kaynağıdır.

Örnek: "Geçen ayki faturalarımı özetle ve ödenmemişleri hatırlat." Ajan sırayla fatura API'sini çağırır, dönen sonucu okur, ödenmemişleri bulur, hatırlatma e-postası taslağı hazırlar ve göndermeden önce onay ister.

### Temel kavramlar

- **Araç çağrısı (tool call / function call)**: modele araçların adı, ne işe yaradığı ve **parametre şeması** (alanlar, tipler, zorunluluk) verilir. Model hangi aracı hangi argümanla çağıracağını seçer; uygulama aracı çalıştırıp sonucu modele geri besler.
- **Ajan döngüsü (agent loop)**: plan → araç çağır → gözlem → yineleme. Model bağlamı okur, sonraki eylemi seçer, araç çalışır, sonuç bağlama eklenir, döngü baştan başlar; durma koşulu sağlanınca biter.
- **Araç kullanan, kendi adımını kendi planlayan yapı (agentic)**: "agentic sistem" denince kastedilen budur — modele hedef verilir, ara adımları model seçer.
- **ReAct (Reason + Act — düşün ve uygula)**: bu döngünün klasik desenidir. Model önce kısa bir akıl yürütme (düşünce) üretir, sonra bir eylem (araç çağrısı) seçer, gelen gözlemi okur ve tekrar düşünür. Bugünkü araç çağıran ajanlar bu şablonun türevleridir.
- **Yan etki (side effect)**: bir aracın dış dünyada bıraktığı kalıcı iz — para tahsil etmek, kayıt silmek, e-posta göndermek. Yan etkisiz araçlar (arama, okuma) risksizdir.
- **Tekrar edilse de sonucu değişmeyen çağrı (idempotency)**: aynı isteği iki kez çalıştırmak tek etki üretir. Pratikte bir **idempotency anahtarı** ile sağlanır.
- **İnsan-döngüde (human-in-the-loop)**: geri döndürülemez eylemde insan onayı kapısı.

### Nasıl çalışır?

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Agent loop: düşün, araç çağır, gözlemle, durma koşulu">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Agent loop ve durma koşulu</text>
  <rect x="60" y="46" width="150" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="135" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1) Düşün</text>
  <text x="135" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonraki eylemi seç</text>
  <rect x="245" y="46" width="150" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2) Araç çağır</text>
  <text x="320" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tool orchestration</text>
  <rect x="430" y="46" width="150" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="505" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">3) Gözlemle</text>
  <text x="505" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonucu bağlama ekle</text>
  <text x="320" y="128" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">Döngü tekrarlanır → durma koşulu sağlanınca biter</text>
  <rect x="150" y="146" width="340" height="52" rx="10" fill="#FBEDEA" stroke="#B4442E" stroke-width="1.5"/>
  <text x="320" y="170" text-anchor="middle" font-size="12" font-weight="700" fill="#B4442E" font-family="system-ui">Koruma: max adım/bütçe + döngü tespiti</text>
  <text x="320" y="188" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sonsuz döngü maliyeti ve latency'yi patlatır</text>
</svg>
```

Döngü yapısı gereği kendi kendine durmaz. Model aynı aramayı biraz farklı argümanlarla defalarca deneyip sonuç alamadan dönebilir. Her tur bir model çağrısı daha ve bir araç **gecikmesi (latency)** daha demektir. Üstelik bağlam her turda büyüdüğü için maliyet tur sayısından daha hızlı artar.

Bu yüzden durma güvenceleri **mimari zorunluluktur**. Üç tanesi şart: en fazla kaç adım atılacağı, en fazla kaç **jeton (token — metnin model için sayılan birimi)** ve kaç para harcanacağı, bir de aynı ya da benzer çağrının tekrarını yakalayan **döngü tespiti**. Sınır aşılınca ajan durdurulur veya işi insana devreder.

Araç hataları (API 500, timeout) ajan yaşamının normal parçasıdır. Sağlam desen, hatayı **yapılandırılmış bir gözlem** olarak bağlama geri vermektir — hangi araç, hangi argüman, hangi hata — ki model yeniden denesin, alternatif araca geçsin veya insana devretsin. Hatayı yutup "başarılı" gibi devam etmek ajanı yanlış duruma sokar; ilk hatada çökmek ise geçici sorunlarda dayanıklılığı yok eder.

Yeniden deneme ise doğrudan **idempotency** sorununu doğurur. Ağ zaman aşımında istek sunucuya ulaşmış ama cevap kaybolmuş olabilir; ajan aynı çağrıyı tekrarlar. "Ödeme al" aracı idempotent değilse müşteri iki kez tahsil edilir. Çözüm, modelin iyi davranmasını ummak değil **aracın sözleşmesini** düzeltmektir: her mantıksal işleme benzersiz bir **idempotency anahtarı** verilir, sunucu o anahtarı daha önce gördüyse işlemi tekrar uygulamaz, ilk sonucu döner.

```svg
<svg viewBox="0 0 640 214" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Idempotency anahtarı: yeniden denenen ödeme çağrısı tek tahsilat üretir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Idempotency anahtarı: iki çağrı → tek etki</text>
  <rect x="26" y="52" width="150" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="101" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ödeme al</text>
  <text x="101" y="89" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">key = abc</text>
  <rect x="26" y="120" width="150" height="46" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="101" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yeniden dene (timeout)</text>
  <text x="101" y="157" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">key = abc</text>
  <line x1="176" y1="75" x2="286" y2="99" stroke="#5D7789" stroke-width="2"/>
  <path d="M290 100 l-10 -1 l3 -8.6 z" fill="#5D7789"/>
  <line x1="176" y1="143" x2="286" y2="119" stroke="#5D7789" stroke-width="2"/>
  <path d="M290 118 l-10 1 l3 8.6 z" fill="#5D7789"/>
  <rect x="292" y="82" width="164" height="54" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="374" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Sunucu: anahtarı gördün mü?</text>
  <text x="374" y="123" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">varsa ilk sonucu döndür</text>
  <line x1="456" y1="109" x2="500" y2="109" stroke="#5D7789" stroke-width="2"/>
  <path d="M504 109 l-10 -4.5 l0 9 z" fill="#5D7789"/>
  <rect x="506" y="82" width="108" height="54" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="560" y="104" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">TEK</text>
  <text x="560" y="123" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tahsilat</text>
  <text x="320" y="196" text-anchor="middle" font-size="11" fill="#8A2E2E" font-family="system-ui">Anahtar olmadan tekrar çağrı = çift tahsilat / çift sipariş.</text>
</svg>
```

Geri döndürülemez ve yüksek etkili eylemlerde (toplu silme, para transferi, dışarıya e-posta) son savunma hattı **insan onayıdır**: ajan eylemi ve gerekçesini önerir, insan onaylamadan uygulanmaz. Bu kapı iki tehlikeye karşı birden çalışır: modelin kendi hatasına ve bir **istem enjeksiyonuna (prompt injection — okunan bir metnin içine gizlenmiş, modeli kandıran komut)**. İkincisinde ajan, dışarıdan gelen bir belgedeki gizli talimatı kendi görevi sanıp kötü yöne sürüklenir.

Son takas: **tek ajan mı, çok ajan mı?** Çoklu-ajan mimarisinde bir yönetici (orchestrator) alt-ajanlara görev dağıtır. Gerçekten ayrıştırılabilir, paralel işlerde güçlüdür. Bedeli ağırdır: koordinasyon karmaşıklığı, her ajanın ek model çağrılarından gelen jeton ve para artışı, birbirini bekleyen adımlardan doğan gecikme, bir de genişleyen hata yüzeyi. Basit veya iyi tanımlı bir işte tek, iyi kurulmuş bir ajan hem daha ucuz hem daha güvenilirdir.

```svg
<svg viewBox="0 0 640 224" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek ajan basit ve ucuz, çok-ajan güçlü ama koordinasyon maliyet latency yükü getirir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek ajan mı, çok-ajan mı? Bedeli tart</text>
  <rect x="26" y="42" width="284" height="164" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="168" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Tek ajan</text>
  <rect x="118" y="82" width="100" height="44" rx="10" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="168" y="102" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ajan</text>
  <text x="168" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">düşün→araç→gözlem</text>
  <text x="168" y="156" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">basit iş: daha ucuz</text>
  <text x="168" y="176" text-anchor="middle" font-size="11" fill="#25804C" font-family="system-ui">daha güvenilir</text>
  <text x="168" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">dar hata yüzeyi</text>
  <rect x="330" y="42" width="284" height="164" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="472" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Çok-ajan (orkestrasyon)</text>
  <rect x="432" y="80" width="80" height="30" rx="8" fill="#FFFFFF" stroke="#B26B00" stroke-width="1.2"/>
  <text x="472" y="100" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yönetici</text>
  <rect x="352" y="124" width="66" height="28" rx="7" fill="#FFFFFF" stroke="#6B3FA0" stroke-width="1.1"/>
  <text x="385" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alt-ajan</text>
  <rect x="439" y="124" width="66" height="28" rx="7" fill="#FFFFFF" stroke="#6B3FA0" stroke-width="1.1"/>
  <text x="472" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alt-ajan</text>
  <rect x="526" y="124" width="66" height="28" rx="7" fill="#FFFFFF" stroke="#6B3FA0" stroke-width="1.1"/>
  <text x="559" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alt-ajan</text>
  <line x1="472" y1="110" x2="385" y2="124" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="472" y1="110" x2="472" y2="124" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="472" y1="110" x2="559" y2="124" stroke="#5D7789" stroke-width="1.2"/>
  <text x="472" y="176" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">güçlü ama: koordinasyon,</text>
  <text x="472" y="194" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">+token/maliyet, +latency, geniş hata yüzeyi</text>
</svg>
```

### Mülakatta ne sorulur?

Sık sorulanlar: "Ajan döngüsünü anlat." "ReAct nedir?" "Ajan sonsuz döngüye girdi, ne yaparsın?" "Araç yeniden denenirse ne olur?" (idempotency anahtarı). "Hangi eylemlerde insan onayı istersin?" "Araç tanımında en kritik şey nedir?" (net amaç, ne zaman kullanılacağı ve doğru parametre şeması). "Çok-ajanın bedeli nedir?"

Yanlış cevap kalıpları: sıcaklığı oynatmak, sistem talimatına bir cümle yazıp bunu güvenlik sanmak, modeli büyütmek.

### Tek cümlede

Ajan, plan→araç→gözlem döngüsünü koşan bir LLM'dir; güvenilirliği modelin iyi niyetinden değil bütçe ve adım tavanından, idempotent araç sözleşmelerinden, hataların gözlem olarak geri verilmesinden ve kritik eylemlerdeki insan onayından gelir.

# agentic-1 | Uzman
## Soru
Bir ajan aynı arama aracını hafifçe farklı argümanlarla defalarca çağırıp sonuç alamadan dönmeye devam ediyor ve maliyet patlıyor. En doğru yapısal önlem hangisidir?
## A
Sistem prompt'una "aynı aramayı tekrarlama" talimatı yazıp modelin buna uymasını beklemek
🤖 Prompt'taki temenni bir durma güvencesi değildir; model yine tekrarlayabilir.
## B
Daha büyük bir model kullanmak; güçlü model kendi kendine döngüden çıkmayı bilir
🤖 Model boyutu durma garantisi vermez; sınır olmadan büyük model de takılabilir.
## C
Araç sonuçlarını bağlama hiç eklememek; bağlam küçük kalırsa döngü kendiliğinden kısalır
🤖 Sonucu bağlama eklememek ajanı kör bırakır, daha da rastgele döner; çözüm değil.
## D
Maksimum adım/bütçe tavanı ve döngü tespiti koyup sınır aşılınca durdurmak veya insana devretmek
🤖 Doğru: adım/bütçe tavanı ve tekrar tespiti sonsuz döngüyü keser ve maliyeti sınırlar.
## Doğru
D
## Açıklama
Agent loop yapısı gereği sonsuza dek dönebilir veya aynı verimsiz adımı tekrarlayabilir. Doğru koruma **açık durma güvenceleridir**: maksimum adım sayısı, maksimum token/$ bütçesi ve aynı ya da benzer araç çağrısının tekrarını yakalayan **döngü tespiti**; sınır aşılınca ajan durdurulur veya insana devredilir. Prompt'a talimat yazmak (A) bir kontrol değil temennidir — model buna uymak zorunda değildir. Model büyütmek (B) durma garantisi vermez. Araç sonuçlarını bağlama eklememek (C) ajanı kör bırakıp durumu kötüleştirir, çünkü model neyi denediğini bile göremez. "Her döngünün bir maliyeti var" kabulüyle bütçe ve tavan baştan konur; bunlar sonradan eklenen lüks değil mimari zorunluluktur.

# agentic-2 | İleri
## Soru
Bir ajanın çağırdığı "ödeme al" aracı ağ hatası nedeniyle yeniden denenebiliyor. Çift tahsilatı önlemek için doğru tasarım nedir?
## A
Aracı hiç yeniden denememek; ilk ağ hatasını kalıcı başarısızlık sayıp işlemi iptal etmek
🤖 Yeniden denemeyi yasaklamak geçici hatalarda başarısızlığı artırır; asıl çözüm idempotency.
## B
Aracı idempotent yapmak: her işleme bir idempotency anahtarı verip tekrarları tek etki saymak
🤖 Doğru: idempotency anahtarı sayesinde tekrar çağrı çift tahsilat üretmez, tek etki olur.
## C
Ödemeyi iki kez alıp fazladan çekileni gece koşan bir mutabakat işiyle iade etmek
🤖 Çift işlem ve iade hem riskli hem gereksiz; idempotency baştan çift etkiyi önler.
## D
Aracı yalnız bir kez çağırması için sistem prompt'una büyük harfle kesin talimat yazmak
🤖 Yan etki güvenliği modele bırakılmaz; idempotency araç sözleşmesinde zorlanmalıdır.
## Doğru
B
## Açıklama
Ajanlar araçları hata veya timeout sonrası yeniden deneyebilir; model de aynı çağrıyı iki kez üretebilir. Bu yüzden **yan etkili araçlar idempotent olmalıdır**. Standart çözüm **idempotency anahtarıdır**: her mantıksal işleme benzersiz bir anahtar atanır, sunucu bu anahtarı daha önce gördüyse işlemi tekrar uygulamak yerine ilk sonucu döner — böylece "ödeme al" iki kez çağrılsa bile **tek tahsilat** olur. Kritik ayrıntı şudur: timeout durumunda isteğin sunucuya ulaşıp ulaşmadığını istemci bilemez, dolayısıyla yeniden denemeyi yasaklamak (A) hem dayanıklılığı düşürür hem sorunu çözmez. İki kez alıp iade etmek (C) riskli ve gereksizdir; prompt'a talimat yazmak (D) bir güvenlik mekanizması değildir. Idempotency, modelin davranışına değil aracın sözleşmesine yaslanır.

# agentic-3 | İleri
## Soru
Bir ajan bir üretim veritabanındaki kayıtları toplu silebiliyor. Bu tür yüksek etkili, geri döndürülemez eylemler için doğru desen nedir?
## A
Ajana tam özerklik verip her şeyi tek başına yaptırmak; onay adımı akışı yavaşlatır
🤖 Geri döndürülemez eylemde tam özerklik felakete davetiye; hata doğrudan üretime yansır.
## B
Silme aracını hiç tanımlamayıp gerektiğinde modelden ham SQL üretmesini istemek
🤖 Ham SQL üretmek yetkiyi daraltmaz, aksine denetlenemez ve daha tehlikeli bir yol açar.
## C
İnsan-döngüde onay kapısı: ajan eylemi önerir, insan onaylamadan geri döndürülemez adım uygulanmaz
🤖 Doğru: yüksek etkili ve geri döndürülemez eylemlerde insan onayı en sağlam korumadır.
## D
Silme işlemini loglayıp gerekirse gecelik yedekten geri dönmek; önlem yerine kurtarma
🤖 Yedekten dönüş veri kaybı ve kesinti demektir; önleme kurtarmadan her zaman ucuzdur.
## Doğru
C
## Açıklama
Geri döndürülemez ve yüksek etkili eylemlerde (toplu silme, para transferi, dışarıya e-posta) doğru desen **insan-döngüde (human-in-the-loop) onaydır**: ajan eylemi ve gerekçesini **önerir**, ama bir insan onaylayana kadar yürütmez. Bu, hem model hatasına hem de bir prompt injection'ın ajanı kötü yöne sürüklemesine karşı son savunma hattıdır. Tam özerklik (A) patlama yarıçapını maksimuma çıkarır. Silme aracını kaldırıp ham SQL ürettirmek (B) yaygın bir yanılgıdır: yetkiyi daraltmaz, aksine denetlenebilir dar bir araç yerine keyfî ve denetlenemez bir yol açar. Yedekten dönmeye güvenmek (D) bir kurtarma planıdır, önleme değil — veri kaybı ve kesinti maliyeti onay kapısından kat kat pahalıdır. Onay kapısı, en az yetki ve idempotency ile birlikte katmanlı güvenliği oluşturur.

# agentic-4 | Uzman
## Soru
Bir çağrı merkezi özetleme görevini ekip, dört alt-ajanlı bir orkestrasyonla çözmüş; tek bir iyi kurulmuş ajan da yeterdi. Çoklu-ajan mimarisinin başlıca dezavantajı nedir?
## A
Ek koordinasyon, artan token/maliyet, daha yüksek latency ve daha geniş hata yüzeyi
🤖 Doğru: multi-agent koordinasyon, maliyet ve latency yükü getirir; basit işte gereksizdir.
## B
Çoklu-ajanın çalışma-zamanı bedeli yoktur; her görevde varsayılan olarak tercih edilmelidir
🤖 Her ajan ek çağrı ve koordinasyon demektir; her zaman en iyi seçenek değildir.
## C
Çoklu-ajan tipik olarak daha ucuzdur; işi bölmek toplam token tüketimini düşürür
🤖 İşi bölmek toplam token'ı düşürmez; her alt-ajan kendi bağlamını yeniden kurar.
## D
Tek dezavantajı kod satırının artmasıdır; maliyet ve gecikme aynı kalır
🤖 Etki çalışma-zamanındadır: maliyet, latency ve hata yüzeyi artar; sadece kod satırı değil.
## Doğru
A
## Açıklama
**Çoklu-ajan (multi-agent)** mimarisi karmaşık, gerçekten ayrıştırılabilir görevlerde güçlüdür; ama bedeli vardır: alt-ajanlar arası **koordinasyon karmaşıklığı**, her ajanın ek model çağrıları nedeniyle **artan token ve maliyet**, seri bağımlılıklardan doğan **daha yüksek latency** ve daha çok bileşenden gelen **daha geniş hata yüzeyi**. Basit veya iyi tanımlı bir görevde (çağrı özeti) tek, iyi kurulmuş bir ajan hem daha ucuz hem daha güvenilirdir. "Bedeli yok" (B) yanlıştır. "Daha ucuz" (C) da yanlıştır ve nedeni öğreticidir: her alt-ajan kendi bağlamını sıfırdan kurar, yönetici raporları yeniden okur, dolayısıyla toplam token azalmaz artar. "Sadece kod satırı artar" (D) etkiyi çalışma zamanından koparır. Kural: en basit yeterli tasarımla başla, karmaşıklığı yalnız kanıtlanmış ihtiyaç için ekle.

# agentic-5 | İleri
## Soru
Ajana bir araç tanımlarken (tool orchestration) modelin aracı doğru kullanması için en kritik olan nedir?
## A
Araç adını kısaltıp açıklamayı hiç yazmamak; model adından ne olduğunu zaten çıkarır
🤖 Açıklama olmadan model aracın ne zaman ve nasıl kullanılacağını bilemez, yanlış çağırır.
## B
Araçları hiç tanımlamayıp modelin hangi API'lerin var olduğunu tahmin etmesini beklemek
🤖 Tanımsız araç çağrılamaz; model var olmayan bir şema uydurur ve çağrı geçersiz olur.
## C
Tüm işlevleri tek bir "komut çalıştır" aracında toplayıp ayrıntıyı modele bırakmak
🤖 Tek genel araç yetkiyi genişletir ve doğrulanamaz hâle getirir; dar araçlar tercih edilir.
## D
Aracın ne yaptığını, ne zaman kullanılacağını ve parametre şemasını net tanımlamak
🤖 Doğru: net amaç, kullanım koşulu ve doğru şema modelin aracı isabetli çağırmasını sağlar.
## Doğru
D
## Açıklama
Araç orkestrasyonunda model, aracı **yalnızca verdiğin tanım kadar** iyi kullanır. Kritik olan üç şey: aracın **ne yaptığı**, **ne zaman kullanılması gerektiği** (ve gerekmediği) ve **parametre şeması** (alan adları, tipler, zorunlu/opsiyonel, kısıtlar). İyi bir açıklama ile kesin bir şema, modelin doğru aracı doğru argümanla çağırma isabetini belirgin artırır ve geçersiz çağrıları azaltır. Adı kısaltıp açıklamayı atmak (A) veya hiç tanımlamamak (B) modeli körleştirir. Her şeyi tek bir "komut çalıştır" aracında toplamak (C) ayrı bir tuzaktır: uygulama artık ne yapıldığını şemadan doğrulayamaz, yetki daraltılamaz ve tehlikeli eylemleri seçici biçimde onaya sokamazsın. Araç tanımı ajanın API sözleşmesidir; net yazılır ve şema doğrulaması uygulama tarafında zorlanır.

# agentic-6 | Uzman
## Soru
Bir ajanın araç çağrısı hata döndürdüğünde (ör. API 500) en sağlam davranış hangisidir?
## A
Hatayı yutup ajanı sanki çağrı başarılıymış gibi bir sonraki adıma devam ettirmek
🤖 Hatayı gizlemek ajanı yanlış duruma sokar; sonraki adımlar hatalı varsayımla ilerler.
## B
Hatayı yapılandırılmış gözlem olarak bağlama geri verip toparlanmaya bütçe sınırı içinde izin vermek
🤖 Doğru: hata gözlem olarak verilince model toparlanabilir; bütçe sınırsız denemeyi keser.
## C
İlk hatada tüm oturumu çökertip hiçbir yeniden denemeye veya devretmeye izin vermemek
🤖 Tek geçici hatada çökmek dayanıklılığı düşürür; kurtarılabilir durumları da öldürür.
## D
Hata mesajını modele hiç göstermeden aynı çağrıyı sınırsız biçimde tekrarlamaya devam etmek
🤖 Model neyin yanlış gittiğini görmeden aynı çağrıyı tekrarlar; bu bir hata döngüsüdür.
## Doğru
B
## Açıklama
Araç hataları ajan yaşamının normal parçasıdır. Sağlam desen, hatayı **yapılandırılmış bir gözlem** olarak bağlama geri vermektir (hangi araç, hangi argüman, hangi hata kodu) ve ajanın **toparlanmasına** izin vermektir: geçici hatada yeniden dene, kalıcı hatada alternatif araca geç veya insana devret. Toparlanmanın **bütçeyle çerçevelenmesi** şarttır, aksi hâlde hata döngüsü maliyeti patlatır. Hatayı yutmak (A) ajanı yanlış duruma sokar — sonraki adımlar var olmayan bir sonucu varsayar. İlk hatada çökmek (C) geçici sorunlarda dayanıklılığı yok eder. Hatayı modele göstermeden körlemesine tekrarlamak (D) en verimsiz seçenektir: model neyin yanlış gittiğini bilmediği için argümanını düzeltemez ve aynı çağrıyı sonsuza dek üretir.
