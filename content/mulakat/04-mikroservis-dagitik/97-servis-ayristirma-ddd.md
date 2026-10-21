---
slug: servis-ayristirma-ddd
baslik: "Servis Ayrıştırma ve DDD: Bounded Context, Context Mapping ve Alt-Alan Sınıflandırması"
kademe: İleri
sira: 97
etiketler: 9
---

## Konu anlatımı

### Bu ne işe yarar?

Bir hastaneyi düşün. "Hasta" sözcüğü poliklinikte "randevusu olan kişi", eczanede "ilaç reçetesi sahibi", muhasebede "faturalanacak hesap" demektir. Herkesi tek bir dev "Hasta" formuna zorlarsan form şişer, kimse tam anlamaz ve bir bölümün küçük isteği tüm hastaneyi durdurur. Doğru yol, her bölümün kendi sözlüğüyle kendi formunu tutması, bölümler arasında yalnız gerekli bilgiyi paylaşmasıdır. Bu konu, yazılımı **nereden böleceğini** aynı mantıkla anlatır: bölme çizgisi teknik değil, **işin doğal sınırlarından** gelir.

### Temel kavramlar

- **Alan (domain)**: yazılımın çözdüğü gerçek iş konusu (e-ticaret, bankacılık, kargo).
- **Alt-alan (subdomain)**: alanın anlamlı bir parçası (sipariş, ödeme, kimlik doğrulama).
- **Model**: iş kavramlarının koddaki temsili — sınıflar, alanlar, kurallar.
- **Servis**: bağımsız çalıştırılan, dışarıya yalnız **ağ çağrısıyla** (milisaniye ölçeğinde, hata verebilen çağrı) erişilen uygulama parçası.
- **DDD (Domain-Driven Design / Alan Odaklı Tasarım)**: modeli ve sınırları işin dilinden türeten tasarım yaklaşımı.
- **Ubiquitous language (her yerde geçerli dil)**: iş uzmanı ile geliştiricinin ortak kullandığı, kodda birebir karşılığı olan terim seti.
- **Bounded context (sınırlı bağlam)**: bir modelin ve dilinin **tek anlamlı ve geçerli** olduğu açık sınır.
- **Canonical model (tek kurumsal ortak model)**: tüm bağlamların paylaştığı tek dev model — DDD'nin kaçındığı tuzak.
- **Yukarı akış / aşağı akış (upstream / downstream)**: bir ilişkide modeli belirleyen taraf ile ona uymak zorunda kalan taraf.
- **Context mapping (bağlam haritalama)**: bağlamların birbirine nasıl bağlandığını ve aradaki güç ilişkisini gösteren harita.
- **Legacy (eski sistem)**: hâlâ çalışan ama değiştirilmesi zor, çoğunlukla kötü modellenmiş eski yazılım.
- **EventStorming**: iş uzmanlarıyla birlikte olayları duvara dizerek bağlamları keşfetme atölyesi.

### Nasıl çalışır?

"Sistemi mikroservise böleceğim" dendiğinde asıl soru **nereden bölüneceğidir**. Yanlış sınır, servisleri sürekli birlikte değişmeye zorlar ve dağıtık monolit üretir. DDD'nin yanıtı: sınırı veritabanı tablolarından, teknik katmanlardan veya kod satır sayısından değil, **bounded context'lerden** çıkar.

**Bounded context**, belirli bir modelin ve dilinin tutarlı olduğu açık sınırdır. Kritik içgörü: aynı sözcük farklı bağlamlarda **farklı şey demektir**. "Müşteri" satışta bir alıcı adayı, faturalandırmada bir ödeme hesabı, kargoda bir teslimat adresidir. Her bağlam **kendi modelini** tutar; bağlamlar arasında tam model değil yalnız **kimlik (ID)** ve gerekli birkaç alan paylaşılır. Bu yüzden bounded context, mikroservis sınırı için **birinci adaydır**: içeride tek tutarlı model, dışarıya net sözleşme.

Yaygın üç yanlış bölme ölçütü vardır. Birincisi **tablo başına servis**: birlikte çalışan davranışı parçalara ayırır. Tek bir sorgu için verileri ağ üzerinden birleştirmek gerekir; buna **dağıtık birleştirme (distributed join)** denir, hem yavaştır hem kırılgandır. İkincisi **katmana göre bölme** (bir servis arayüz, bir servis veri): yatay dilimler üretir. Böyle bölünce tek bir iş akışı bütün servislere yayılır, küçük bir değişiklik hepsine dokunur. Üçüncüsü **eşit kod büyüklüğü**: işle hiç ilgisi olmayan, tamamen yapay bir ölçüttür.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bounded context ve alt-alan sınıflandırması">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı sözcük, farklı bağlam, farklı model</text>
  <rect x="30" y="42" width="180" height="86" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="120" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Satış bağlamı</text>
  <text x="120" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"Müşteri" = alıcı adayı</text>
  <text x="120" y="110" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">core subdomain</text>
  <rect x="230" y="42" width="180" height="86" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Fatura bağlamı</text>
  <text x="320" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"Müşteri" = ödeme hesabı</text>
  <text x="320" y="110" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">supporting subdomain</text>
  <rect x="430" y="42" width="180" height="86" rx="11" fill="#F4EFE4" stroke="#9A7B2E" stroke-width="1.5"/>
  <text x="520" y="64" text-anchor="middle" font-size="12" font-weight="700" fill="#9A7B2E" font-family="system-ui">Kimlik bağlamı</text>
  <text x="520" y="88" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">"Müşteri" = kullanıcı kimliği</text>
  <text x="520" y="110" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">generic → satın al</text>
  <line x1="210" y1="85" x2="230" y2="85" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="4 3"/>
  <line x1="410" y1="85" x2="430" y2="85" stroke="#5D7789" stroke-width="1.5" stroke-dasharray="4 3"/>
  <text x="320" y="158" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Context Mapping: bağlamlar arası ilişki</text>
  <rect x="90" y="172" width="200" height="40" rx="8" fill="#FBEAEA" stroke="#B23B3B" stroke-width="1.3"/>
  <text x="190" y="190" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Anti-Corruption Layer (ACL)</text>
  <text x="190" y="205" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">dış/eski modeli tampon çevirir</text>
  <rect x="350" y="172" width="200" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="450" y="190" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Open Host + Published Language</text>
  <text x="450" y="205" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">çok tüketicili genel API</text>
</svg>
```

Alt-alanlar stratejik olarak üçe ayrılır ve bu sınıflandırma **nereye yatırım yapılacağını** belirler. **Core subdomain** (çekirdek) işletmeyi rakiplerinden ayıran kısımdır: en iyi mühendisler buraya, **kendin yaz**. **Supporting subdomain** (destekleyici) gerekli ama ayırt edici değildir: basit tut. **Generic subdomain** (jenerik) herkeste aynı olan çözülmüş problemdir (kimlik doğrulama, ödeme, e-posta): **satın al**, yeniden yazma. Dikkat: "jenerik" demek "önemsiz" demek değildir — kimlik doğrulama yanlış yapılırsa şirketi batırır; anlamı, mühendislik gücünü oraya değil çekirdeğe harcamaktır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Alt-alan sınıflandırması ve yatırım stratejisi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Alt-alan sınıflandırması = yatırım stratejisi</text>
  <rect x="26" y="44" width="188" height="130" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="120" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Core (çekirdek)</text>
  <text x="120" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">rekabet avantajı</text>
  <text x="120" y="116" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">KENDİN YAZ</text>
  <text x="120" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">en iyi mühendisler</text>
  <text x="120" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">buraya</text>
  <rect x="226" y="44" width="188" height="130" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="320" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Supporting</text>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">gerekli, ayırt edici değil</text>
  <text x="320" y="116" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">BASİT TUT</text>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gerekirse dış ekip</text>
  <rect x="426" y="44" width="188" height="130" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="520" y="68" text-anchor="middle" font-size="13" font-weight="700" fill="#B26B00" font-family="system-ui">Generic (jenerik)</text>
  <text x="520" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çözülmüş problem</text>
  <text x="520" y="116" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">SATIN AL</text>
  <text x="520" y="140" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kimlik · ödeme · e-posta</text>
  <text x="520" y="158" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeniden yazma</text>
</svg>
```

**Context mapping**, bağlamların birbirine nasıl bağlandığını gösterir. Ekipler arasındaki güç ilişkisini de modeller. Başlıca desenler şunlardır:

- **Partnership (ortaklık)**: iki ekip birlikte evrilir, kararları birlikte alır.
- **Customer-Supplier (müşteri-tedarikçi)**: aşağı akıştaki ekibin istekleri, yukarı akıştaki ekibin planına girer.
- **Conformist (uyum sağlayan)**: aşağı akış, yukarı akışın modelini olduğu gibi kabul eder.
- **Anti-Corruption Layer / ACL (yozlaşmayı önleyen çeviri katmanı)**: dış veya eski bir modeli kendi diline çeviren koruyucu tampon.
- **Open Host Service + Published Language (açık sunucu servisi + yayımlanmış dil)**: birden çok tüketiciye hizmet veren, sözleşmesi açıkça yayımlanmış genel API.

Conformist ile ACL aynı sorunun iki farklı cevabıdır. Conformist'te çeviri maliyeti yoktur ama bağımlılık yüksektir: yukarı akış değişince aşağı akış da değişmek zorundadır. ACL'de araya bir **çeviri katmanı** konur; maliyet çeviri kodunun geliştirilip bakılmasıdır, kazanç ise **model yalıtımıdır**. Seçim ekonomiktir. Yukarı akış temiz ve kararlıysa Conformist ucuzdur. Yukarı akış karmakarışık veya eski (legacy) ise ya da kendi alanın çekirdekse, ACL'nin sağladığı yalıtım maliyetine değer. ACL, monolitten kademeli geçişte de işe yarar: yeni servisi eski sistemden yalıtır. Bu kademeli geçiş yöntemine **sarmaşık deseni (strangler fig)** denir; eski sistemin işlevleri parça parça yeni servislere taşınır, sonunda eski sistem kurur.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yukarı akış ilişkisi: Conformist teslim olur, ACL tampon kurar">
  <defs>
    <marker id="servis-ayristirma-ddd-ak" markerWidth="10" markerHeight="10" refX="8" refY="3.5" orient="auto">
      <path d="M0,0 L8,3.5 L0,7 Z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yukarı akışa bağlanma: Conformist vs ACL</text>
  <rect x="250" y="40" width="140" height="34" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="62" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Yukarı akış (upstream)</text>
  <line x1="150" y1="74" x2="150" y2="104" stroke="#5D7789" stroke-width="1.8" marker-end="url(#servis-ayristirma-ddd-ak)"/>
  <line x1="490" y1="74" x2="490" y2="104" stroke="#5D7789" stroke-width="1.8" marker-end="url(#servis-ayristirma-ddd-ak)"/>
  <rect x="40" y="108" width="220" height="86" rx="11" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="150" y="130" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Conformist</text>
  <text x="150" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">modeli olduğu gibi benimser</text>
  <text x="150" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ çeviri maliyeti yok</text>
  <text x="150" y="188" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">- yüksek bağımlılık</text>
  <rect x="380" y="108" width="220" height="86" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="490" y="130" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Anti-Corruption Layer</text>
  <text x="490" y="152" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çeviri tamponu ile korur</text>
  <text x="490" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">+ model yalıtımı</text>
  <text x="490" y="188" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">- çeviri/bakım maliyeti</text>
</svg>
```

### Mülakatta ne sorulur?

- **Bounded context tanımı**: bir modelin ve dilinin tek anlamlı olduğu sınır — paket adı, tablo veya sınıf hiyerarşisi değil.
- **Bölme ölçütü**: tablo, katman veya kod büyüklüğü değil; iş alanının doğal sınırları.
- **Alt-alan üçlüsü**: core = kendin yaz, supporting = basit tut, generic = satın al. "Nereye en iyi mühendisini koyarsın?" sorusunun cevabı çekirdektir.
- **Canonical model neden anti-desen?** Aynı sözcüğün farklı anlamlarını tek şişkin modele zorlar; her değişiklik tüm bağlamları etkiler, koordineli dağıtım ve kırılganlık getirir. Ekip sayısı azken de bağlaşım oluşur, yalnız acısı geç hissedilir.
- **ACL ne değildir?** Güvenlik katmanı değil, ortak model dayatma aracı değil, genel API yayımlama deseni (Open Host Service) değil — **model çeviri tamponudur**.
- **Conformist vs ACL takası**: çeviri maliyeti yok/bağımlılık yüksek ile maliyet var/yalıtım yüksek arasında.
- **Keşif yöntemi**: EventStorming ve alan analiziyle bağlamları ve dili çıkarmak; sonra gerekirse bölmek.

### Tek cümlede

Servisi nereden böleceğinin cevabı veritabanında veya katmanlarda değil, işin dilinde saklıdır. Her bounded context kendi modelini tutar. Alt-alan sınıflandırması yatırımın nereye gideceğini yönlendirir. Context mapping ise bağlamlar arası bağımlılığı bilinçli bir seçime dönüştürür.

# decomp-1 | Temel
## Soru
DDD'de "bounded context" (sınırlı bağlam) neyi ifade eder?
## A
Bir modelin ve terimlerinin tek anlamlı ve geçerli olduğu açık bir sınır
🤖 Doğru: bounded context, model ve dilin tutarlı anlam taşıdığı sınırı tanımlar.
## B
Bir sınıfın kalıtım hiyerarşisinin en üstündeki soyut taban sınıfı
🤖 Bu nesne yönelimli bir ayrıntıdır; bounded context stratejik bir alan sınırıdır.
## C
Bir veritabanı tablosunun birincil anahtarıyla belirlenen kayıt kümesi
🤖 Bounded context veri modeli düzeyinde değil, iş alanı modeli düzeyinde bir sınırdır.
## D
Tüm ekiplerin üzerinde anlaştığı tek ortak kurumsal veri modeli
🤖 Bu tam tersidir: canonical model tuzağıdır; DDD her bağlamın kendi modelini tutmasını ister.
## Doğru
A
## Açıklama
**Bounded context**, DDD'nin stratejik tasarımının kalbidir: içinde belirli bir **alan modelinin** ve ona eşlik eden **her yerde geçerli dilin** (ubiquitous language) net, tutarlı ve **tek anlamlı** olduğu açık bir sınır. Sınırın içinde "Sipariş" veya "Müşteri" tek bir kesin anlam taşır; sınırın dışında aynı terim başka bir model demek olabilir. Bu sınır neden önemli: karmaşık bir alanı tek bir dev **kurumsal ortak modelle** (canonical model, D) yönetmeye çalışmak her şeyi birbirine bağlar ve kimse tam anlamaz — bu, bounded context'in tanımı değil, tam olarak kaçındığı şeydir. Bounded context modeli **anlaşılabilir parçalara** böler ve mikroservis sınırı için birinci adaydır, çünkü içerisi tutarlı, dışarısı net sözleşmelidir.

# decomp-2 | Temel
## Soru
Mikroservis sınırlarını çizerken DDD'nin önerdiği temel yaklaşım hangisidir?
## A
Servisleri veritabanı tablolarına göre "bir tablo = bir servis" şeklinde bölmek
🤖 Tablo bazlı bölme ilişkili iş mantığını dağıtır ve dağıtık join üretir.
## B
Servisleri iş alanının doğal sınırlarına — bounded context'lere — göre çizmek
🤖 Doğru: DDD sınırları teknik yapıdan değil, iş alanının bounded context'lerinden türetir.
## C
Servisleri teknik katmanlara göre (bir servis arayüz, bir servis veri) bölmek
🤖 Katmana göre bölme yatay dilimler üretir; tek iş akışı tüm servislere yayılır.
## D
Servisleri kod satır sayısına göre yaklaşık eşit büyüklükte parçalara bölmek
🤖 Kod büyüklüğü işle ilgisiz yapay bir ölçüttür; sınırı iş alanı belirler.
## Doğru
B
## Açıklama
DDD'nin merkezi katkısı, servis sınırlarını **teknik değil alansal** ölçütle çizmesidir. Yanlış yaklaşımlar: **tablo bazlı bölme** (A) ilişkili davranışı parçalar ve dağıtık join'e yol açar; **katman bazlı bölme** (C) her iş akışının tüm servislere yayıldığı yatay dilimler üretir ve bağımsızlığı yok eder; **eşit kod büyüklüğü** (D) ise işle hiçbir ilgisi olmayan yapay bir ölçüttür — doğru sınır bazen çok küçük, bazen büyük bir servis verir. Doğru yaklaşım, iş alanını **bounded context'lere** ayırıp her bağlamı bir aday servis olarak ele almaktır. Böylece her servis tek bir tutarlı model içerir, kendi verisine sahiptir ve dışarıya net bir sözleşme sunar. Sınırın iş alanından gelmesi, değişikliklerin genelde tek bir bağlamda kalmasını, dolayısıyla servisin bağımsız evrilebilmesini sağlar.

# decomp-3 | Orta
## Soru
DDD'de bir alt-alan (subdomain) "generic subdomain" (jenerik) olarak sınıflandırıldıysa, stratejik olarak ne yapılmalıdır?
## A
En iyi mühendisleri atayıp sıfırdan özel bir çözüm olarak yazmak
🤖 Bu çekirdek (core) alt-alan stratejisidir; jenerik alanda özel yatırım israftır.
## B
Rekabet avantajı sağladığı için gizli tutup patent başvurusu yapmak
🤖 Jenerik alan tanımı gereği ayırt edici değildir; rekabet avantajı çekirdektedir.
## C
Çözülmüş ortak bir problem olduğu için hazır çözüm satın almak, yeniden yazmamak
🤖 Doğru: jenerik alan için satın al/hazır kullan; mühendislik gücünü çekirdeğe sakla.
## D
Ayırt edici olmadığı için hiç önemsememek, deneyimsiz kişilere yazdırıp doğrulamamak
🤖 "Jenerik" önemsiz demek değildir; kimlik/ödeme yanlış yapılırsa şirketi batırır.
## Doğru
C
## Açıklama
Alt-alan sınıflandırması bir **yatırım stratejisidir**. **Generic subdomain**, işletmeye özgü olmayan, herkeste benzer biçimde bulunan **çözülmüş problemlerdir**: kimlik doğrulama, ödeme işleme, e-posta gönderimi, bildirim. Bunlar gerekli ama **ayırt edici değildir** — kimse "bizim e-posta gönderme kodumuz rakiplerden iyi" diye pazar kazanmaz. Doğru strateji: **satın al veya olgun bir hazır çözüm kullan** (Auth0/Keycloak, Stripe, SendGrid), kendin yazma. Ancak "jenerik" ile "önemsiz" karıştırılmamalıdır (D): kimlik doğrulama ya da ödeme yanlış çalışırsa sonuç yıkıcıdır; sınıflandırmanın söylediği tek şey, **kendi mühendislik gücünü** oraya değil çekirdeğe harcamandır. Buna karşılık **core subdomain** rekabet avantajının kaynağıdır ve en iyi mühendislerle kendin yazılır; **supporting subdomain** gerekli ama sıradandır, basit tutulur.

# decomp-4 | Orta
## Soru
"Anti-Corruption Layer" (ACL / yozlaşma önleyici katman) deseni ne işe yarar?
## A
Veritabanı sorgularını SQL enjeksiyonu gibi saldırılara karşı korur
🤖 Adı çağrıştırsa da ACL bir güvenlik önlemi değildir; koruduğu şey modelin bütünlüğüdür.
## B
Yukarı akış bağlamın modelini tüm ekiplere ortak model olarak dayatır
🤖 Bu canonical model dayatmasıdır; ACL tam tersine kendi modelini dıştan yalıtır.
## C
Bir bağlamın modelini çok sayıda tüketiciye genel bir API olarak yayımlar
🤖 Bu Open Host Service + Published Language desenidir; ACL bir çeviri tamponudur.
## D
Dış veya eski bir sistemin modelini çevirerek kendi modelini kirlenmekten korur
🤖 Doğru: ACL, yabancı kavramların kendi modeline sızmasını engelleyen çeviri tamponudur.
## Doğru
D
## Açıklama
**Anti-Corruption Layer** bir **context mapping** desenidir ve stratejik değeri modelin **saflığını** korumaktır. Bir bağlam, kontrol edemediği bir dış sistemle (üçüncü parti API) veya kötü tasarlanmış eski bir sistemle (legacy) entegre olmak zorunda kaldığında, o sistemin kavramlarının doğrudan içeri girmesi kendi temiz modelini **yozlaştırır** (corrupt): garip alan adları, tutarsız durumlar ve yabancı varsayımlar modele sızar. ACL, iki dünya arasına bir **çeviri tamponu** koyar: dışarının modelini alır, içerinin diline çevirir ve tersini yapar. Böylece dış sistem değişse bile iç model korunur, kirli detaylar tek yerde izole edilir. ACL bir güvenlik önlemi (A), ortak model dayatma aracı (B) veya genel API yayımlama deseni (C — o Open Host Service'tir) değildir. Özellikle monolitten mikroservise **kademeli geçişte** (strangler fig) yeni servisi eski sistemden yalıtmak için kritiktir.

# decomp-5 | İleri
## Soru
Bir ekip aynı gerçek dünya varlığı için ("Müşteri") tüm bağlamlarda tek bir paylaşılan "canonical model" (kurumsal ortak model) kurmaya çalışıyor. DDD açısından bu neden bir anti-desendir?
## A
Aynı sözcük bağlamlara göre farklı anlamlıdır; tek model bağlamları sıkı bağlar
🤖 Doğru: canonical model farklı anlamları tek şişkin modele zorlar ve bağlaşımı artırır.
## B
Canonical model daha hızlı çalışır ama kodun okunmasını ve bakımını zorlaştırır
🤖 Sorun performans ya da okunabilirlik değil; asıl sakınca bağlamları kilitleyen bağlaşımdır.
## C
Canonical model yalnızca ilişkisel veritabanlarında tutarlı biçimde saklanabilir
🤖 Veritabanı türüyle ilgisi yoktur; sorun stratejik bağlaşım ve model çakışmasıdır.
## D
Bağlaşım sorunu ancak çok ekipli şirketlerde doğar, üç-dört ekipte model güvenlidir
🤖 Bağlaşım ekip sayısından bağımsız oluşur; az ekipte yalnızca acısı daha geç hissedilir.
## Doğru
A
## Açıklama
Tek **canonical model** tuzağı, DDD'nin çözmeye çalıştığı asıl problemdir. Gerçek şudur: "Müşteri" satışta alıcı adayı, faturalandırmada ödeme hesabı, kargoda teslimat adresi, destekte bilet sahibidir — **aynı sözcük, farklı modeller**. Hepsini tek modele sığdırmaya çalışmak, birbiriyle çelişen alanlar içeren şişkin bir sınıf üretir; her bağlamın ihtiyacı diğerlerini kısıtlar. Daha kötüsü, tüm bağlamlar bu ortak modele bağımlı olduğundan **her değişiklik hepsini etkiler** — sıkı bağlaşım, koordineli dağıtım, kırılganlık. Bu bağlaşım ekip sayısına bağlı değildir (D): küçük ekipte de oluşur, sadece acısı geç hissedilir. Mikroservise taşındığında sonuç doğrudan **distributed monolith**'tir. DDD'nin yanıtı: her bounded context **kendi modelini** tutar; ortak varlıklar bağlamlar arasında **kimlik** (ID) ile ilişkilendirilir, tam model paylaşılmaz.

# decomp-6 | İleri
## Soru
Context mapping'de "Conformist" (uyumlu) ilişkisi ile "Anti-Corruption Layer" arasındaki temel stratejik fark nedir?
## A
Conformist bir programlama dili, ACL ise bir veritabanı erişim katmanıdır
🤖 İkisi de context mapping desenidir; dil veya veri erişim katmanı değildir.
## B
Conformist yukarı akışın modelini olduğu gibi benimser; ACL araya çeviri katmanı koyar
🤖 Doğru: Conformist teslim olur, ACL tampon kurar; takas çeviri maliyeti ile model yalıtımıdır.
## C
Conformist yalnızca senkron, ACL yalnızca asenkron iletişimde kullanılabilir
🤖 İkisi de iletişim biçiminden bağımsızdır; ayrım modele teslim olma ya da onu yalıtmadadır.
## D
Conformist gelen modeli çevirir, ACL ise kendi modelini yukarı akışa dayatır
🤖 Roller ters yazılmış: çeviren taraf ACL'dir ve hiçbiri modelini yukarı akışa dayatmaz.
## Doğru
B
## Açıklama
Her iki desen de bir bağlamın, kontrol edemediği bir **yukarı akış** (upstream) bağlama nasıl bağlandığını tanımlar; fark **stratejik takastadır**. **Conformist**: aşağı akış bağlam, yukarı akışın modelini **olduğu gibi benimser** ve kendi modelini ona uydurur. Avantajı çeviri maliyeti olmamasıdır; bedeli yukarı akışa **yüksek bağımlılıktır** — yukarı akış değişirse aşağı akış da değişmek zorundadır ve kendi ihtiyaçlarına göre modelleyemez. **Anti-Corruption Layer**: aşağı akış, arasına bir **çeviri katmanı** koyarak kendi temiz modelini korur. Avantajı **yalıtımdır**, bedeli çeviri kodunun geliştirme ve bakım maliyetidir. Çeviren taraf her zaman ACL'dir; Conformist çeviri yapmaz (D'nin karıştırdığı nokta) ve hiçbir desende aşağı akış kendi modelini yukarıya dayatmaz. Seçim ekonomiktir: yukarı akış modeli temiz ve kararlıysa Conformist ucuzdur; yukarı akış kaotik/eski ise veya kendi alanın çekirdekse ACL'nin yalıtım değeri maliyetine değer.
