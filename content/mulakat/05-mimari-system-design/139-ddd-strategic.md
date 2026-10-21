---
slug: ddd-strategic
baslik: "DDD Stratejik Tasarım: Ubiquitous Language, Bounded Context, Context Map"
kademe: İleri
sira: 139
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Büyük sistemlerde en pahalı hatalar teknik değil **anlam** hatalarıdır. İki ekip aynı kelimeyi farklı anlamda kullanır: "müşteri", "sipariş", "iptal". Bunu fark etmeden tek bir ortak model yazmaya çalışırlar. Model de her iki tarafı mutsuz eden, her yeni istekte biraz daha şişen bir canavara döner. Kod kusursuz olabilir; sistem yine de yanlıştır.

DDD'nin **stratejik (strategic)** tasarımı bu sorunu ele alır ve iki soru sorar. Birincisi: alanı hangi **sınırlardan** böleceğiz, yani anlam nerede değişiyor? İkincisi: sınırlı mühendislik gücümüzü **nereye** yığacağız? Bu sorular kod desenlerinden (entity, aggregate) önce gelir ve etkileri daha büyüktür. Yanlış sınırla kurulmuş bir sistemi mükemmel desenlerle kurtaramazsın.

### Temel kavramlar

- **Alan (domain) ve alt-alan (subdomain):** alan, yazılımın çözdüğü iş dünyasının tamamı; alt-alanlar bunun anlamlı parçalarıdır (katalog, fiyatlandırma, kargo, faturalama).
- **Ubiquitous language (her yerde geçerli dil):** alan uzmanlarının ve geliştiricilerin **aynı terimleri aynı anlamla** kullandığı ortak dil. Toplantıda da, dokümanda da, **kodda da** yaşar: iş insanının "sipariş onaylama" dediği şey kodda `siparis.onayla()` olur. Konuşulan insan dili (Türkçe/İngilizce) ya da programlama dili seçimiyle ilgisi yoktur.
- **Bounded context (sınırlı bağlam):** belirli bir modelin ve dilin **tutarlı ve geçerli** olduğu açık sınır. Aynı kelime iki context'te farklı bir model demektir; sınır, anlamın değiştiği yeri işaretler.
- **Context map (bağlam haritası):** bağlamlar arası ilişkileri gösteren haritadır. **Yukarı akış (upstream)** modelini dayatabilen taraftır; **aşağı akış (downstream)** ondan etkilenen taraftır. Başlıca desenler şunlardır: **ACL**, **Conformist**, **Open Host Service**, **Published Language**, **Shared Kernel**, **Separate Ways**.
- **Core / supporting / generic subdomain:** sırasıyla rekabet avantajı yaratan, gerekli ama ayırt edici olmayan ve herkeste standart olan alt-alanlar.
- **Event storming:** alan uzmanları ile geliştiricilerin olayları duvara dizerek alanı birlikte keşfettiği atölye.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İki bounded context ve aralarında ACL çevirisi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bounded context: aynı kelime, ayrı model</text>
  <rect x="40" y="50" width="200" height="120" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="140" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Satış context'i</text>
  <text x="140" y="104" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Müşteri = potansiyel</text>
  <text x="140" y="124" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">alıcı, fırsat, indirim</text>
  <rect x="400" y="50" width="200" height="120" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="76" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Destek context'i</text>
  <text x="500" y="104" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">Müşteri = kayıtlı</text>
  <text x="500" y="124" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">kullanıcı, talep, SLA</text>
  <rect x="278" y="92" width="84" height="40" rx="8" fill="#FFF6E5" stroke="#B8860B" stroke-width="1.5"/>
  <text x="320" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#8A6400" font-family="system-ui">ACL</text>
  <text x="320" y="124" text-anchor="middle" font-size="9.5" fill="#8A6400" font-family="system-ui">çeviri</text>
  <text x="258" y="88" text-anchor="middle" font-size="16" fill="#5D7789" font-family="system-ui">→</text>
</svg>
```

### Nasıl çalışır?

Sınır bulmanın en pratik yolu **dili dinlemektir**. Aynı kelimenin anlamı değişiyorsa orada bir sınır vardır. Örnek: "müşteri" satışta potansiyel bir alıcıdır — fırsat, indirim, teklif kavramlarıyla anılır. Destekte ise kayıtlı bir kullanıcıdır — talep, SLA, geçmiş kavramlarıyla anılır. DDD, tek bir merkezî model dayatmak yerine her bağlamın **kendi** modelini tutmasını söyler. Her bounded context kendi dilini, kendi modelini ve tercihen kendi verisini sahiplenir; sınır genellikle takım ve servis sınırlarıyla hizalanır.

Sınırlar arası entegrasyon **context map** desenleriyle yapılır.

**ACL (Anti-Corruption Layer — bozulmayı önleyen katman)**, dış ya da eski bir sistemin kavramlarını kendi diline **çeviren** koruyucu katmandır. Maliyetlidir; ama modelini temiz tutar.

**Conformist (uyum sağlayan)**, aşağı akıştaki ekibin ilişkiyi etkileyemediği için yukarı akışın modeline **çeviri yapmadan uyum sağlamasıdır**. Ucuzdur; bedeli, yukarı akışın tuhaflıklarını da miras almaktır.

**Open Host Service (herkese açık servis)**, çok sayıda tüketicisi olan yukarı akışın her ekip için ayrı köprü yazmak yerine **tek, belgelenmiş ve herkese açık bir protokol** yayımlamasıdır. Bu protokolün ortak sözlüğü **Published Language** olarak yazıya dökülür.

İkinci karar **yatırımın nereye yapılacağıdır**. **Core domain (çekirdek alan)**, işletmeye rekabet avantajı kazandıran alt-alandır; bir pazaryeri için fiyatlandırma motoru gibi. En iyi mühendisler, en özenli model ve en çok zaman oraya ayrılır. **Supporting (destekleyici)** alan gereklidir ama ayırt edici değildir. **Generic (genel)** alan ise herkeste aynıdır: e-posta gönderimi, kimlik doğrulama, ödeme altyapısı. Bunlar mümkünse **satın alınır** ya da hazır kütüphaneyle çözülür; herkesin yaptığı işi baştan yazmak avantaj getirmez.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Core, supporting ve generic subdomain yatırım önceliği">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çabayı core'a yığ, generic'i satın al</text>
  <rect x="30" y="46" width="185" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="2"/>
  <text x="122" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Core domain</text>
  <text x="122" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">rekabet avantajı</text>
  <text x="122" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ör. fiyatlandırma</text>
  <text x="122" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">en çok yatırım</text>
  <text x="122" y="166" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en iyi mühendislik</text>
  <rect x="227" y="46" width="185" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="319" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Supporting</text>
  <text x="319" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gerekli ama</text>
  <text x="319" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ayırt edici değil</text>
  <text x="319" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">orta yatırım</text>
  <rect x="424" y="46" width="185" height="132" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="516" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Generic</text>
  <text x="516" y="94" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">herkeste standart</text>
  <text x="516" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ör. e-posta, kimlik</text>
  <text x="516" y="148" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">satın al /</text>
  <text x="516" y="166" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">kütüphane</text>
</svg>
```

### Mülakatta ne sorulur?

**"Ubiquitous language nedir?"** — Ortak terim değil, ortak **anlam** demektir. Kodda da birebir yaşar; belirsizleşirse model çürür.

**"Bounded context mikroservisle aynı şey mi?"** — Hayır. Bounded context kavramsal bir sınırdır; mikroservis bir dağıtım birimidir. İyi tasarımda hizalanırlar, ama bir bağlam birden çok servis içerebilir.

**"Aynı kelime iki ekipte farklı anlamda; ne yaparsın?"** — Sınır çizerim ve her bağlama kendi modelini bırakırım. Aralarına da açık bir çeviri/entegrasyon katmanı koyarım.

**"ACL mi Conformist mi?"** — Güç dengesine ve çevirinin maliyetine bakarım.

**"Neye yatırım yaparsın?"** — Çekirdek alana. Genel olanı satın alırım.

**Event storming**'in amacı da sorulur: kod veya şema üretmek değil, alan uzmanlarıyla **ortak anlayış** kurmak ve sınırları keşfetmektir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Context map: upstream downstream ilişkisinde ACL ile conformist">
  <defs>
    <marker id="dds-arrow" markerWidth="9" markerHeight="9" refX="7" refY="3" orient="auto">
      <path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Downstream'in seçeneği: çevir ya da uy</text>
  <rect x="240" y="42" width="160" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="62" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Upstream context</text>
  <text x="320" y="80" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">güç burada, model dayatır</text>
  <line x1="292" y1="88" x2="200" y2="118" stroke="#5D7789" stroke-width="2" marker-end="url(#dds-arrow)"/>
  <line x1="348" y1="88" x2="440" y2="118" stroke="#5D7789" stroke-width="2" marker-end="url(#dds-arrow)"/>
  <rect x="40" y="122" width="240" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="160" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">ACL ile çevir</text>
  <text x="160" y="166" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dış modeli tercüme et,</text>
  <text x="160" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kendi modelini koru (maliyetli)</text>
  <rect x="360" y="122" width="240" height="76" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="480" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Conformist: uy</text>
  <text x="480" y="166" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çeviri yapmadan dış modeli</text>
  <text x="480" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">aynen benimse (tuhaflığı miras)</text>
</svg>
```

### Tek cümlede

Stratejik DDD, alanı anlamın değiştiği yerlerden bounded context'lere böler ve her bağlama kendi ubiquitous language'ini bırakır. Aralarındaki ilişkiyi context map desenleriyle (ACL, Conformist, Open Host Service) açıkça yönetir. Mühendislik gücünü de rekabet avantajı yaratan çekirdek alana yığar.

# dds-1 | İleri
## Soru
DDD'de **ubiquitous language** (her yerde geçerli dil) ne demektir?
## A
Alan uzmanı ile geliştiricinin aynı anlamla kullandığı, koda da yansıyan ortak terimler
🤖 Doğru: aynı terimlerin aynı anlamla hem konuşmada hem kodda kullanılması.
## B
Ekiplerin toplantı ve kodda İngilizce konuşmasını zorunlu kılan bir ekip kuralı
🤖 Konuşulan insan diliyle ilgisi yok; kavramsal bir ortak iş dilidir.
## C
Yalnızca API dokümantasyonunda geçen, biçimsel olarak tanımlanmış terim sözlüğü
🤖 Sadece dokümanda değil; günlük konuşmada ve kodda da yaşayan bir dildir.
## D
Kullanılacak programlama dilinin (Java, Python) tüm ekiplerce standartlaştırılması
🤖 Teknik dil seçimi değil; alanın kavramlarını ifade eden ortak iş dilidir.
## Doğru
A
## Açıklama
**Ubiquitous language**, alan uzmanları ile geliştiricilerin **aynı terimleri aynı anlamla** kullandığı; toplantıda, dokümanda ve **kodda** (sınıf ve metot adlarında) birebir yaşayan ortak dildir. Amacı "çeviri kaybını" ortadan kaldırmaktır: iş insanının "sipariş onaylama" dediği şey kodda `siparis.onayla()` olur, böylece kod alan uzmanına okunabilir hale gelir ve yanlış anlamalar erken yakalanır. Bu dil bir **bounded context içinde** geçerlidir; sınırın dışına çıkınca aynı kelimenin anlamı değişebilir. Konuşulan insan dili (B), programlama dili (D) veya salt dokümantasyon sözlüğü (C) ile karıştırılmamalıdır — dil belirsizleşirse model de çürür.

# dds-2 | İleri
## Soru
"Müşteri" kavramı satış ekibinde bir şey, destek ekibinde başka bir şey ifade ediyor ve tek bir ortak model ikisini de mutsuz ediyor. DDD bu duruma nasıl yaklaşır?
## A
Tek bir kanonik "Müşteri" modeli dayatılır; bütün ekipler ona uymak zorundadır
🤖 Tek dev model tam da sorunun kaynağı; DDD sınırla ayırmayı önerir, dayatmayı değil.
## B
"Müşteri" kelimesi sözlükten çıkarılır; isim çakışması böylece ortadan kalkar
🤖 Sorun isim değil anlam; kelimeyi silmek modeli değil yalnızca terimi budar.
## C
Kavram veritabanı düzeyinde tek bir tabloya indirgenip fiziksel olarak birleştirilir
🤖 Fiziksel şema kararı kavramsal sınır sorununu çözmez; anlam farkı sürer.
## D
Her bağlam kendi bounded context'inde kendi "Müşteri" modelini ve dilini tutar
🤖 Doğru: her context kendi tutarlı modelini korur, sınır anlamın değiştiği yeri işaretler.
## Doğru
D
## Açıklama
**Bounded context**, belirli bir modelin ve dilin **tutarlı ve geçerli** olduğu açık sınırdır. Aynı kelime (`Müşteri`) iki context'te **farklı model** demektir: satışta "potansiyel alıcı, fırsat, indirim"; destekte "kayıtlı kullanıcı, talep, SLA". DDD tek bir kanonik model dayatmak yerine (A) her context'in **kendi** modelini tutmasını önerir; sınır tam da anlamın değiştiği yeri işaretler. Böylece her model kendi bağlamında sade ve doğru kalır, "herkese uyayım" derken hiçbir yere uymayan şişkin bir model oluşmaz. Context'ler birbirinden habersiz kalmaz; bir **context map** üzerinde ilişkilendirilir ve aralarına açık çeviri/entegrasyon konur. Kelimeyi silmek (B) veya tek tabloya indirgemek (C) kavramsal ayrımı çözmez, yalnızca gizler.

# dds-3 | İleri
## Soru
Eski (legacy) bir ödeme sisteminin garip veri modeli, yeni ve temiz alan modelinize sızmasın istiyorsunuz. Hangi desen bunu sağlar?
## A
Anti-Corruption Layer: dış modeli kendi modelinize çeviren koruyucu katman
🤖 Doğru: ACL dış/eski modeli çevirerek kendi modelinizi korur.
## B
Conformist: dış modeli olduğu gibi benimseyip ona çeviri yapmadan uyum sağlarsınız
🤖 Conformist tam tersini yapar; çeviri koymadan dış modele teslim olur.
## C
Shared Kernel: iki context ortak bir model parçasını paylaşıp birlikte sahiplenir
🤖 Paylaşılan çekirdek koruma değil ortaklık kurar; sızıntıyı engellemez.
## D
Big Ball of Mud: sınırların olmadığı, her şeyin birbirine karıştığı tek model
🤖 O bir anti-desendir; koruma sağlamaz, tersine sınırları büsbütün yok eder.
## Doğru
A
## Açıklama
**Anti-Corruption Layer (ACL)**, bir context'in dış ya da eski bir sistemin modelini **çevirerek** kendi temiz modelini yabancı kavramlardan **koruyan** katmandır. Dışarıdan gelen veri ACL'de kendi ubiquitous language'inize **tercüme** edilir; legacy'nin garip yapısı sınırın içine geçemez, değişirse yalnız çeviri katmanı güncellenir. Bedeli ek kod ve bakım maliyetidir. Bu, **Conformist** deseninin (B) zıddıdır: conformist'te güç dış context'te olduğu ve çeviri maliyeti göze alınmadığı için onun modeline **aynen uyulur**. **Shared Kernel** (C) iki context'in bilinçli olarak ortak sahiplendiği model parçasıdır — koruma değil, ortaklık ve dolayısıyla karşılıklı bağımlılık kurar. **Big Ball of Mud** (D) sınırların çöktüğü bir anti-desendir.

# dds-4 | İleri
## Soru
Bir ödeme context'i, kendisini kullanan her yeni ekip için ayrı ve özel bir entegrasyon yazmaktan bunalmış durumda. Yukarı akıştaki (upstream) bu context'in uygulaması gereken desen hangisidir?
## A
Conformist: tüketici ekipler upstream modelini çeviri yapmadan aynen benimser
🤖 Conformist aşağı akışın kararıdır; yayıncının entegrasyon yükünü azaltan bir desen değil.
## B
Open Host Service: herkese açık, iyi tanımlı tek bir protokol/servis yayımlanır
🤖 Doğru: tek ve belgeli ortak arayüz, tüketici başına özel entegrasyonu ortadan kaldırır.
## C
Anti-Corruption Layer: upstream, tüketicilerin modelini kendi tarafında çevirir
🤖 ACL aşağı akışın kendini koruma katmanıdır; yayıncının sunum biçimi değildir.
## D
Separate Ways: taraflar entegrasyondan tamamen vazgeçip ayrı yollarına gider
🤖 İhtiyaç sürüyor; entegrasyonu kesmek talebi karşılamaz, yalnızca sorunu yok sayar.
## Doğru
B
## Açıklama
**Open Host Service (OHS)**, çok sayıda aşağı akış (downstream) tüketicisi olan bir upstream context'in, her tüketici için ayrı köprü yazmak yerine **iyi tanımlı, belgelenmiş ve herkese açık tek bir protokol/servis** yayımlamasıdır. Böylece entegrasyon maliyeti tüketici sayısıyla artan özel bağlantılardan tek bir sözleşmeye iner; sözleşmenin ortak sözlüğü genellikle **Published Language** olarak yazıya dökülür (paylaşılan olay veya DTO şeması). **Conformist** (A) ve **ACL** (C) aşağı akışın verdiği kararlardır: biri upstream modeline aynen uyar, diğeri onu kendi modeline çevirerek korunur; ikisi de yayıncının sunum biçimi değildir. **Separate Ways** (D) entegrasyondan büsbütün vazgeçmektir ve ihtiyaç sürerken uygulanamaz. Bu desenler bir **context map** üzerinde ekipler arası güç ve bağımlılık dengesini görünür kılar.

# dds-5 | İleri
## Soru
Bir sistemde "fiyatlandırma motoru" şirkete rakiplerinden ayrışan asıl değeri katıyor; "e-posta gönderimi" ise standart bir ihtiyaç. DDD'nin subdomain sınıflandırması bu iki alana nasıl yaklaşmayı önerir?
## A
İkisine de eşit mühendislik yatırımı yap; bütün alt-alanlar aynı önem düzeyindedir
🤖 Eşit dağıtım kıt kaynağı çarçur eder; core domain daha fazlasını hak eder.
## B
Fiyatlandırma core domain'dir, en iyi mühendislik oraya; e-posta generic'tir, satın alınır
🤖 Doğru: rekabet avantajı yaratan core'a yatır, generic olanı satın al.
## C
E-postaya daha çok yatır; altyapı işlevleri her zaman iş mantığından önce gelir
🤖 Ters öncelik; rekabet avantajı iş core'undadır, jenerik altyapıda değil.
## D
İkisini de dış sağlayıcıdan satın al; şirket içinde hiç geliştirme yapılmamalıdır
🤖 Core domain satın alınmaz; onu içeride geliştirmek rekabet avantajının kaynağıdır.
## Doğru
B
## Açıklama
DDD alanı üç tür alt-alana ayırır. **Core domain** işletmeye **rekabet avantajı** kazandırır (fiyatlandırma motoru) — en iyi mühendisler, en çok zaman ve en özenli model **oraya** gider; şirketin farkı buradan doğduğu için dışarıdan satın alınamaz. **Supporting subdomain** gerekli ama ayırt edici değildir, makul bir çözüm yeter. **Generic subdomain** herkeste ortak standart ihtiyaçtır (e-posta gönderimi, kimlik doğrulama, ödeme altyapısı) — mümkünse **satın alınır** veya hazır kütüphane kullanılır, baştan yazmak avantaj getirmez, yalnız bakım yükü ekler. Eşit yatırım (A) kıt mühendislik kaynağını çarçur eder; her şeyi satın almak (D) ise rekabet avantajını dışarı devretmektir. Bu önceliklendirme stratejik tasarımın kalbidir.

# dds-6 | İleri
## Soru
**Event storming** atölyesinin temel amacı nedir?
## A
Üretimdeki gerçek olay akışını Kafka'dan toplayıp görsel bir grafiğe dökmek
🤖 O teknik gözlemleme; event storming bir keşif atölyesidir, üretim analizi değil.
## B
Bir mikroservisin birim testlerini olay tabanlı biçimde yazıp otomatikleştirmek
🤖 Test tekniği değil; alan keşfi için insanların bir araya geldiği çalıştaydır.
## C
Veritabanı şemasını normalleştirmek için tabloları ve ilişkileri kağıda dökmek
🤖 Şema tasarımı değil; amaç alan olaylarını ve sınırları keşfetmektir.
## D
Alan uzmanı ve geliştiricilerin olayları duvara dizip akışı ve sınırları keşfetmesi
🤖 Doğru: ortak keşif atölyesi; olaylardan modeli ve bounded context'leri çıkarır.
## Doğru
D
## Açıklama
**Event storming**, Alberto Brandolini'nin geliştirdiği, alan uzmanları ile geliştiricileri aynı odada (veya sanal bir duvarda) toplayıp **domain event'leri** turuncu kağıtlarla zaman sırasına dizerek alanı **birlikte keşfetme** atölyesidir. Olaylardan komutlar, aktörler, politikalar ve **bounded context sınırları** ortaya çıkar; çelişkiler ve belirsizlikler ("hot spot") hızla görünür olur. Çıktısı kod ya da şema değil, **paylaşılan anlayış** ve ubiquitous language'in temelidir; girdisi ise insanların kafasındaki bilgidir. Üretim olay analizi (A), birim test (B) veya şema normalleştirme (C) ile karıştırılmamalıdır — bunların hiçbiri alan uzmanıyla ortak bir zihinsel model kurmayı hedeflemez.
