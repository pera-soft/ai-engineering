---
baslik: "Monolit → Mikroservis: Ne Zaman ve Genel Hatalar"
yazar: "Serpiştirme — Mimari stiller (Hard Parts özü)"
tarih: "2026-07-11"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 6
---

### Bu metin ne anlatıyor?
Bir programı tek parça hâlinde mi yazmalı, yoksa küçük parçalara bölüp her birini ayrı ayrı
mı çalıştırmalı? Bu metin bu kararı anlatıyor. Bölmek kulağa modern gelir ama bedava
değildir: parçalar artık birbirini ağ üzerinden çağırır ve ağ yavaşlar, kopar, yanılır. Metin
önce seçenekleri sıralıyor, sonra "ne zaman bölmeli", en sonda da en sık yapılan altı hatayı
gösteriyor.

### Bu metinde geçen temel terimler
- **Monolit** (monolith): Programın tamamının tek bir çalışan parça olarak paketlenip
  yayına alınması. "Tek parça" demek.
- **Mikroservis** (microservice): Programın küçük, ayrı ayrı yayına alınabilen, kendi
  verisine sahip parçalara bölünmesi.
- **Dağıtım birimi** (deployment unit): Yayına alınan tek paket. Monolitte bir tane vardır.
- **Alan / sınırlı bağlam** (domain / bounded context): İşin anlamlı bir bütünlük oluşturan
  parçası — ör. "sipariş", "ödeme", "stok". Sınırlı bağlam, o parçanın kendi terimlerinin
  geçerli olduğu çitin adıdır: "müşteri" kelimesi faturalama bağlamında başka, kargo
  bağlamında başka bir şey ifade edebilir.
- **Bağlaşım** (coupling): İki parçanın birbirine ne kadar yapışık olduğu. Biri değişince
  öteki de değişmek zorundaysa bağlaşım yüksektir — istenmeyen durum.
- **Senkron / asenkron çağrı**: Senkron = çağıran taraf yanıt gelene kadar bekler.
  Asenkron = mesajı bırakır, beklemez; yanıt sonra gelir.

## Neden bu özet
Mikroservis, "modern olan" değildir; bir karmaşıklık **satın alma** kararıdır. Her
servisi ağın öbür ucuna koyduğunda ölçek ve özerklik kazanırsın ama karşılığında
dağıtık sistem yükünü (ağ, tutarlılık, gözlemlenebilirlik) ödersin. Bu yüzden çoğu
sistem mikroservisle değil, **modüler monolitle (modular monolith)** başlamalı ve
ancak gerçek bir ihtiyaç kanıtlanınca servis ayırmalıdır.

## Türkçe çalışma metni

### (a) Olgunluk ekseni: Monolit → Modüler Monolit → SOA → Mikroservis
Mimari bir "seviye atlama" merdiveni değil; ihtiyaca göre seçilen bir eksendir. Sağa
gittikçe **bağımsızlık artar** ama **karmaşıklık da artar**.

- **Monolit:** Tek dağıtım birimi (deployment unit), tek veritabanı. Ne zaman uygun?
  Yeni ürün, küçük ekip, belirsiz alan (domain) sınırları. En hızlı başlangıç.
- **Modüler Monolit (modular monolith):** Hâlâ tek dağıtım birimi, ama içeride net
  modül sınırları (bounded context) var. Ne zaman uygun? **Neredeyse her yeni sistemin
  varsayılanı.** Mikroservisin faydalarının çoğunu (net sınır, ekip ayrımı) ağ
  bedeli ödemeden verir.
- **SOA (hizmet odaklı mimari):** Kaba taneli (coarse-grained) paylaşılan hizmetler,
  çoğu zaman merkezi bir orkestrasyon/veri katmanı. Ne zaman? Kurumsal entegrasyon,
  paylaşılan yetenekleri birden çok uygulamaya sunma.
- **Mikroservis:** İnce taneli, bağımsız dağıtılan, **her biri kendi verisine sahip**
  servisler. Ne zaman? Aşağıdaki (b) maddesindeki ihtiyaçlar KANITLANINCA.

**Varsayılan kural:** "Önce modüler monolit." Sınırları kodun içinde ucuzken çiz;
gerçekten gerekince o sınırdan bir servis kes.

Aşağıdaki diyagram bu ekseni ve "soldan başla, kanıt olunca sağa git" kuralını gösterir.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Olgunluk ekseni: Monolit, Modüler Monolit, SOA, Mikroservis; sağa gittikçe bağımsızlık ve karmaşıklık artar">
  <defs>
    <marker id="mm-ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Olgunluk / karar ekseni</text>
  <text x="320" y="43" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Çoğu sistem soldan başlar, kanıt olunca sağa gider</text>
  <rect x="16" y="70" width="132" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="82" y="94" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Monolit</text>
  <text x="82" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tek birim · tek DB</text>
  <rect x="172" y="70" width="132" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="238" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Modüler Monolit</text>
  <text x="238" y="108" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">net modül sınırı</text>
  <text x="238" y="120" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">← varsayılan</text>
  <rect x="328" y="70" width="132" height="52" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="394" y="94" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">SOA</text>
  <text x="394" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kaba hizmetler</text>
  <rect x="484" y="70" width="140" height="52" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="554" y="94" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">Mikroservis</text>
  <text x="554" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">bağımsız · kendi DB'si</text>
  <line x1="148" y1="96" x2="170" y2="96" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mm-ok1)"/>
  <line x1="304" y1="96" x2="326" y2="96" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mm-ok1)"/>
  <line x1="460" y1="96" x2="482" y2="96" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mm-ok1)"/>
  <line x1="40" y1="160" x2="600" y2="160" stroke="#5D7789" stroke-width="1.8" marker-end="url(#mm-ok1)"/>
  <text x="40" y="182" text-anchor="start" font-size="10.5" fill="#5D7789" font-family="system-ui">az bağımsızlık · az karmaşıklık</text>
  <text x="600" y="182" text-anchor="end" font-size="10.5" fill="#C77A17" font-family="system-ui">çok bağımsızlık · çok karmaşıklık</text>
</svg>
```

### (b) Mikroservis NE ZAMAN gerçekten gerekir?
Servisi ayırmayı haklı çıkaran ihtiyaçlar (biri bile yeterli olabilir, ama **kanıt**
gerekir, tahmin değil):

- **Bağımsız ölçek (independent scaling):** Sistemin bir parçası (ör. arama) diğerinden
  10 kat fazla yük alıyor ve ayrı ölçeklenmesi para/kaynak kazandırıyor.
- **Bağımsız deploy:** Bir parçayı, diğerlerini riske atmadan günde 20 kez yayına
  almak gerekiyor.
- **Ekip özerkliği (team autonomy):** Farklı ekipler farklı hızda ilerlemek, birbirinin
  koduna dokunmadan sahiplenmek istiyor.
- **Farklı teknoloji ihtiyacı:** Bir parça için farklı bir dil/çalışma zamanı (runtime)
  ya da özel bir veri deposu gerçekten gerekli.

**Gerekmediğinde ödediğin bedel:** Dağıtık sistem karmaşıklığı, ağ gecikmesi (latency)
ve ağ hataları, veri tutarlılığı problemleri (ACID transaction kaybı — *ACID*, bir
veritabanı işleminin ya tamamen olup ya hiç olmamasını garanti eden kurallar bütünüdür), ve
gözlemlenebilirlik (observability) yükü — dağıtık izleme (tracing), log toplama,
servisler arası hata ayıklama. Bu bedeli ihtiyaç olmadan ödemek net zarardır.

#### (b1) Kullanılabilirlik çarpımı — kimsenin baştan hesaplamadığı matematik
Servisleri bölmek, sistemin ayakta kalma oranını **kendiliğinden artırmaz**; yanlış
kurarsan düşürür. Sebep basit bir çarpma işlemidir.

Bir isteği karşılamak için A servisi B'yi, B de C'yi **senkron** olarak çağırıyorsa, istek
ancak üçü birden ayaktayken başarılı olur. Zincirin kullanılabilirliği, halkaların
kullanılabilirliğinin **çarpımıdır**:

- Her servis tek başına %99,9 ayakta olsun (ayda ~43 dakika kesinti).
- 3 servis zinciri: 0,999 × 0,999 × 0,999 ≈ **%99,7** → ayda ~2 saat.
- 10 servis zinciri: 0,999^10 ≈ **%99,0** → ayda ~7 saat, yılda ~87 saat.

Yani her servis "çok iyi" olduğu hâlde sistem gözle görülür biçimde kötüleşir. Monolitte bu
çarpım yoktur, çünkü ortada tek bir halka vardır.

Bunun üç sonucu var:

1. **Senkron zinciri kısalt.** Bir isteğin dokunduğu servis sayısı, kullanılabilirliğin
   doğrudan üssüdür.
2. **Zorunlu olmayan çağrıyı asenkrona çevir.** Mesaj kuyruğuna bırakılan iş, alıcı o an
   ayakta değilse bile kaybolmaz; zinciri kırar.
3. **Her çağrıya arıza planı koy.** Zaman aşımı (timeout), yeniden deneme (retry) ve
   **devre kesici** (circuit breaker — sürekli hata veren bir servise çağrı yapmayı bir
   süre tamamen durduran, böylece çöküşün yayılmasını engelleyen desen). Yedek yanıt
   (fallback) verebiliyorsan, o servisin çökmesi tüm isteği düşürmez.

> **Kural:** Mikroservis, kullanılabilirliği ancak parçalar **birbirinden bağımsız
> çalışabildiğinde** artırır. Zincirleme senkron bağımlılık, bölmenin faydasını yiyip
> bitirir.

#### (b2) Conway yasası — sınırı çizen aslında ekip yapısıdır
1967'de Melvin Conway'in ortaya koyduğu gözlem şudur: **bir kurum, ürettiği sistemi kendi
iletişim yapısının kopyası olarak tasarlar.** Yani ekipler nasıl bölünmüşse, yazılım da o
sınırlardan bölünmeye meyleder.

Somut hâli: Üç ekibin varsa (ön yüz ekibi, arka uç ekibi, veritabanı ekibi) sistem büyük
olasılıkla "UI servisi + iş mantığı servisi + veri servisi" diye bölünür — yani (c)
bölümündeki **2 numaralı hataya** (teknik katmana göre bölme) organizasyon eliyle sürüklenir.
Kimse bunu tercih etmemiştir; yapı kendini dayatmıştır.

Bunun iki pratik sonucu var:

- **Ters Conway manevrası** (inverse Conway maneuver): İstediğin mimariyi almak için önce
  **ekipleri** o mimariye göre böl. Sipariş, ödeme ve stok servisleri istiyorsan sipariş,
  ödeme ve stok ekipleri kur — mimari peşinden gelir.
- **Ekip yapısına aykırı mimari yaşamaz.** Beş servisi tek bir ekip sahiplenmek zorunda
  kalıyorsa, o beş servis pratikte tek bir birim gibi yayına alınmaya başlar; dağıtık
  monolite (c/1) giden en sessiz yol budur.

> **Kural:** Servis sınırını çizmeden önce ekip sınırına bak. İkisi uyuşmuyorsa, uzun vadede
> kazanan ekip sınırıdır.

### (c) Genel hatalar (pitfalls)
Her biri: **ne olduğu · neden kötü · nasıl kaçınılır.**

1. **Dağıtık monolit (distributed monolith)** — *Ne:* Servisler ayrı süreçlere
   bölünmüş ama o kadar sıkı bağlı ki birini değiştirince ötekiler de değişmek ve
   birlikte deploy edilmek zorunda. *Neden kötü:* İki dünyanın en kötüsü — monolitin
   kırılganlığı + dağıtık sistemin ağ/işletim bedeli, faydası yok. *Kaçın:* Sınırları
   iş yeteneğine göre çiz; servisler birbirinden bağımsız deploy edilemiyorsa henüz
   ayrılmaya hazır değildir.
2. **Yanlış servis sınırları** — *Ne:* Sistemi alan/bağlam (domain / bounded context)
   yerine teknik katmana göre bölmek (ör. "UI servisi", "iş mantığı servisi",
   "veri servisi"). *Neden kötü:* Tek bir iş değişikliği tüm katman servislerine
   yayılır; bağımsızlık kazanılmaz. *Kaçın:* Sınırı iş yeteneği/bounded context
   boyunca çiz — bir servis bir işi baştan sona sahiplenmeli.
3. **Paylaşılan veritabanı (shared database)** — *Ne:* Birden çok servisin aynı
   veritabanına/şemaya doğrudan yazması. *Neden kötü:* Şema bir servise bağlar,
   bağımsız deploy ölür, gizli kuplaj (coupling) oluşur. *Kaçın:* Her servis kendi
   verisine sahip olsun; başka servisin verisine yalnızca onun API'si üzerinden eriş.
4. **Nano-servis (aşırı ince granülerlik)** — *Ne:* Servisleri gereğinden çok küçük
   parçalara (tek fonksiyon başına servis) bölmek. *Neden kötü:* Basit bir iş için
   onlarca ağ çağrısı; yönetim, dağıtım ve hata ayıklama patlar. *Kaçın:* Servis
   sınırını iş yeteneği belirlesin; birlikte değişen şeyler birlikte kalsın.
5. **Dağıtık veri tutarlılığı** — *Ne:* Bir iş birden çok servisin verisini
   değiştirdiğinde tek ACID transaction artık yok. *Neden kötü:* Tutarlılığı elle
   yönetmek zorundasın — **saga** (işi küçük adımlara bölüp her adımın bir "geri alma"
   adımı olduğu desen), **telafi işlemi** (compensation — yarıda kalan işin yaptığını
   geri alan ters adım, ör. "parayı iade et"), **nihai tutarlılık** (eventual consistency —
   veri bir süre için servisler arasında farklı görünür, sonunda aynılaşır) karmaşıklığı.
   *Kaçın:* Bir transaction'da
   olması gereken veriyi aynı servis sınırının içinde tut; ayırmak zorundaysan
   saga/olay tabanlı yaklaşımı bilinçli seç.
6. **Gevezelik (chatty calls)** — *Ne:* Tek bir kullanıcı isteğini karşılamak için
   servislerin birbirine çok sık, senkron çağrı yapması. *Neden kötü:* Gecikmeler
   üst üste yığılır (her çağrı ağ turu), bir servisin yavaşlığı zincire yayılır.
   *Kaçın:* Sınırları çağrıyı azaltacak şekilde çiz; kaba taneli çağrı, asenkron
   mesajlaşma ve gerektiğinde veri çoğaltma (replication) kullan.

Aşağıdaki iki diyagram önce dağıtık monolit tuzağını (sözde vs. gerçek mikroservis),
sonra altı hatayı kart olarak özetler.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dağıtık monolit tuzağı: solda sıkı bağlı sözde mikroservisler paylaşılan DB ile, sağda gevşek bağlı gerçek mikroservisler her biri kendi DB'si ile">
  <defs>
    <marker id="mm-ok2" markerWidth="9" markerHeight="9" refX="6.5" refY="3" orient="auto"><path d="M0,0 L6.5,3 L0,6 Z" fill="#C77A17"/></marker>
    <marker id="mm-ok3" markerWidth="9" markerHeight="9" refX="6.5" refY="3" orient="auto"><path d="M0,0 L6.5,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <rect x="14" y="34" width="300" height="200" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="164" y="56" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">Sözde mikroservis (dağıtık monolit)</text>
  <rect x="34" y="72" width="80" height="40" rx="8" fill="#ffffff" stroke="#C77A17" stroke-width="1.3"/>
  <text x="74" y="97" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis A</text>
  <rect x="124" y="72" width="80" height="40" rx="8" fill="#ffffff" stroke="#C77A17" stroke-width="1.3"/>
  <text x="164" y="97" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis B</text>
  <rect x="214" y="72" width="80" height="40" rx="8" fill="#ffffff" stroke="#C77A17" stroke-width="1.3"/>
  <text x="254" y="97" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis C</text>
  <line x1="114" y1="86" x2="122" y2="86" stroke="#C77A17" stroke-width="2.4" marker-end="url(#mm-ok2)"/>
  <line x1="204" y1="86" x2="212" y2="86" stroke="#C77A17" stroke-width="2.4" marker-end="url(#mm-ok2)"/>
  <line x1="204" y1="100" x2="122" y2="100" stroke="#C77A17" stroke-width="2.4" marker-end="url(#mm-ok2)"/>
  <rect x="94" y="150" width="140" height="34" rx="8" fill="#ffffff" stroke="#C77A17" stroke-width="1.5"/>
  <text x="164" y="172" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Paylaşılan DB</text>
  <line x1="74" y1="112" x2="120" y2="150" stroke="#C77A17" stroke-width="1.3"/>
  <line x1="164" y1="112" x2="164" y2="150" stroke="#C77A17" stroke-width="1.3"/>
  <line x1="254" y1="112" x2="208" y2="150" stroke="#C77A17" stroke-width="1.3"/>
  <text x="164" y="206" text-anchor="middle" font-size="10.5" font-weight="600" fill="#C77A17" font-family="system-ui">Sıkı bağlı · birlikte deploy zorunlu</text>
  <text x="164" y="222" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">paylaşılan DB · gizli kuplaj</text>
  <rect x="326" y="34" width="300" height="200" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="476" y="56" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Gerçek mikroservis</text>
  <rect x="346" y="72" width="80" height="40" rx="8" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="386" y="97" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis A</text>
  <rect x="436" y="72" width="80" height="40" rx="8" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="476" y="97" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis B</text>
  <rect x="526" y="72" width="80" height="40" rx="8" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="566" y="97" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Servis C</text>
  <line x1="426" y1="92" x2="435" y2="92" stroke="#25804C" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#mm-ok3)"/>
  <line x1="516" y1="92" x2="525" y2="92" stroke="#25804C" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#mm-ok3)"/>
  <rect x="352" y="150" width="68" height="30" rx="7" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="386" y="169" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">DB A</text>
  <rect x="442" y="150" width="68" height="30" rx="7" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="476" y="169" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">DB B</text>
  <rect x="532" y="150" width="68" height="30" rx="7" fill="#ffffff" stroke="#25804C" stroke-width="1.3"/>
  <text x="566" y="169" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">DB C</text>
  <line x1="386" y1="112" x2="386" y2="150" stroke="#25804C" stroke-width="1.2"/>
  <line x1="476" y1="112" x2="476" y2="150" stroke="#25804C" stroke-width="1.2"/>
  <line x1="566" y1="112" x2="566" y2="150" stroke="#25804C" stroke-width="1.2"/>
  <text x="476" y="206" text-anchor="middle" font-size="10.5" font-weight="600" fill="#25804C" font-family="system-ui">Gevşek/asenkron · bağımsız deploy</text>
  <text x="476" y="222" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her servis kendi DB'sine sahip</text>
</svg>
```

```svg
<svg viewBox="0 0 680 260" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Altı mikroservis hatası kartı: dağıtık monolit, yanlış sınır, paylaşılan DB, nano-servis, veri tutarlılığı saga, gevezelik">
  <text x="340" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">6 genel hata (pitfall)</text>
  <rect x="16" y="40" width="208" height="94" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="32" y="64" text-anchor="start" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">1 · Dağıtık monolit</text>
  <text x="32" y="86" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">Ayrı süreçler ama sıkı bağlı;</text>
  <text x="32" y="100" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">birlikte deploy zorunlu →</text>
  <text x="32" y="114" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">iki dünyanın en kötüsü.</text>
  <rect x="236" y="40" width="208" height="94" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="252" y="64" text-anchor="start" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2 · Yanlış sınır</text>
  <text x="252" y="86" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">Teknik katmana göre bölmek</text>
  <text x="252" y="100" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">(domain yerine) → tek değişiklik</text>
  <text x="252" y="114" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">her servise yayılır.</text>
  <rect x="456" y="40" width="208" height="94" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="472" y="64" text-anchor="start" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">3 · Paylaşılan DB</text>
  <text x="472" y="86" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">Servisler aynı DB'yi paylaşınca</text>
  <text x="472" y="100" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">bağımsız deploy ölür →</text>
  <text x="472" y="114" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">gizli kuplaj.</text>
  <rect x="16" y="146" width="208" height="94" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="32" y="170" text-anchor="start" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">4 · Nano-servis</text>
  <text x="32" y="192" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">Aşırı ince granülerlik → basit</text>
  <text x="32" y="206" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">iş için onlarca çağrı,</text>
  <text x="32" y="220" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">yönetilemez trafik.</text>
  <rect x="236" y="146" width="208" height="94" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="252" y="170" text-anchor="start" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">5 · Veri tutarlılığı</text>
  <text x="252" y="192" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">ACID transaction gider →</text>
  <text x="252" y="206" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">saga · nihai tutarlılık</text>
  <text x="252" y="220" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">karmaşıklığı.</text>
  <rect x="456" y="146" width="208" height="94" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="472" y="170" text-anchor="start" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">6 · Gevezelik</text>
  <text x="472" y="192" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">Çok sık senkron çağrı →</text>
  <text x="472" y="206" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">gecikme üst üste yığılır,</text>
  <text x="472" y="220" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">yavaşlık zincire yayılır.</text>
</svg>
```

### (d) Altın kural
> **Önce modüler monolit; gerçek bir bağımsızlık ihtiyacı KANITLANINCA servisi ayır.**

Sam Newman ve *Software Architecture: The Hard Parts* çizgisi aynı yeri işaret eder:
Mikroservis bir hedef değil, belirli bir problemi (ölçek, bağımsız deploy, ekip
özerkliği) çözmek için ödenen bir bedeldir. Problem yoksa bedel de yoktur. Sınırları
kodun içinde ucuzken (modül olarak) çiz, taşımayı gerçekten gerektiren kanıt gelince
o sınırdan bir servis kes.

## Öz-sınav (kendini test et)
1. Üç "mikroservisin" var ama birini değiştirince ötekileri de yeniden deploy etmek
   zorunda kalıyorsun. Hangi hata bu, ve kök nedeni ne olabilir?
2. Yeni bir ürüne 4 kişilik bir ekiple başlıyorsun, alan sınırları henüz belirsiz.
   Eksenin neresinden başlarsın ve neden?
3. Sipariş servisi ile stok servisi aynı veritabanı tablosuna yazıyor. Hangi ilke
   çiğnenmiş, ve bağımsızlık için ne değişmeli?
4. Tek bir "ürün sayfasını göster" isteği, servisler arası 12 senkron çağrı tetikliyor
   ve sayfa yavaş. Bu hangi hata, sınırları nasıl yeniden düşünürsün?
5. Bir iş, iki farklı servisin verisini aynı anda tutarlı değiştirmek zorunda. Tek
   ACID transaction yokken hangi seçeneklerin var ve bunları seçmeden önce hangi
   soruyu sormalısın?
6. Her biri %99,9 ayakta olan 5 servis, bir isteği karşılamak için senkron zincir hâlinde
   birbirini çağırıyor. Zincirin kullanılabilirliği kabaca ne olur, ve bu sayıyı iyileştirmek
   için hangi üç hamleyi yaparsın?
7. Conway yasası ne der? Şirketinde ön yüz, arka uç ve veritabanı diye üç ayrı ekip varsa
   mimarinin hangi hataya kayması beklenir — ve "ters Conway manevrası" bunu nasıl önler?
8. "Servisleri böldük, artık sistem daha güvenilir" diyen bir ekip arkadaşına
   kullanılabilirlik çarpımını nasıl anlatırsın? Hangi koşulda haklı olabilir?
9. Devre kesici (circuit breaker) hangi problemi çözer? Yeniden denemeden (retry) farkı
   nedir, ve yalnızca yeniden deneme kullanmak durumu ne zaman kötüleştirir?
