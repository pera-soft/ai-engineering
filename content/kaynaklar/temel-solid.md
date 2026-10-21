---
baslik: "Temeller Tazeleme: SOLID Prensipleri"
yazar: "Faz 0 — Mühendislik Temelleri"
tarih: "2026-07-10"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 5,6,9
---

### Bu metin ne anlatıyor?
Bu metin, kod yazarken işleri hangi parçaya koyacağına karar vermenin beş temel kuralını
anlatıyor. Bu beş kuralın İngilizce baş harfleri **SOLID** kelimesini verir. Hepsinin ortak
derdi tek: yarın bir şey değiştiğinde programın her yerini elden geçirmek zorunda kalma.
Her kural için önce kısa tanım, sonra günlük hayattan bir örnek, sonra "bu kural nerede
abartıya kaçar" uyarısı bulacaksın.

## Neden bu özet
SOLID, sınıf ve modül tasarımının değişime dayanıklı olmasını sağlayan beş prensiptir;
kötü bağımlılık yönü ve şişkin sorumluluklar sistem büyüdükçe onu kırılganlaştırır. Bu özet
aynı zamanda AI'ya verdiğimiz talimatlara (prompt) da giriyor: yapay zekânın ürettiği kod bu
prensiplere uygun mu diye ölçmek için ortak bir dile ihtiyacımız var. Amaç ezber değil;
terimleri net tanımlamak ve her prensibin "ne zaman dogmaya dönüştüğü" sınırını göstermek.

### Bu metinde geçen temel terimler
Aşağıdaki kelimeler beş prensibin tamamında geçiyor; önce bunları oturtalım.

- **Sınıf (class):** Veriyi ve o veriyle çalışan işlemleri bir arada tutan kod birimi.
- **Modül (module):** Birlikte iş gören sınıfların oluşturduğu daha büyük paket.
- **Arayüz (interface):** Bir sınıfın dışarıya söz verdiği işlemlerin listesi; içeride nasıl
  yapıldığını söylemez, yalnızca "şu işlemler var" der.
- **Soyutlama (abstraction):** Ayrıntıyı gizleyip yalnızca gereken kadarını gösteren tanım;
  pratikte çoğu zaman bir arayüzdür.
- **Gerçekleme (implementation):** Bir arayüzdeki sözü fiilen yerine getiren somut sınıf.
- **Kalıtım (inheritance):** Bir sınıfın başka bir sınıfın özelliklerini devralması;
  devralana **alt tür (subtype)**, devredilene **üst tür (supertype)** denir.
- **Kompozisyon (composition):** Kalıtım yerine, işi başka bir nesneyi *içinde tutup* ona
  yaptırma yöntemi.
- **İstemci (client):** Bir sınıfı ya da arayüzü kullanan taraftaki kod.
- **Sözleşme (contract):** Bir işlemin çağıranına verdiği söz: hangi girdiyi kabul eder, ne
  döndürür, hangi durumda hata verir.

## Beş prensip kısa kısa

### SRP — Tek Sorumluluk (Single Responsibility)
Bir sınıfın değişmesi için **tek bir nedeni** olmalı; yani tek bir **aktöre** hizmet etmeli.
Buradaki aktör (actor), o koddan değişiklik talep edebilecek insan grubudur: muhasebe birimi,
operasyon ekibi, pazarlama… Farklı aktörler farklı zamanlarda farklı şeyler ister; bu yüzden
ikisine birden hizmet eden sınıf iki ayrı sebeple değişir. Günlük dille: bir kişi hem yemek pişirip hem muhasebe tutarsa, mutfak kuralı da vergi kuralı da
onu değiştirir — ikisini ayır. **Fazla olduğu yer:** her metodu ayrı sınıfa bölmek anemik,
gezinmesi zor bir tasarım üretir; sorumluluk = "değişme nedeni"dir, "satır sayısı" değil.

### OCP — Açık/Kapalı (Open/Closed)
Modül **eklemeye açık, değiştirmeye kapalı** olmalı: yeni davranışı mevcut kodu bozmadan yeni kod
ekleyerek getir. Günlük dille: yeni bir ödeme tipi eklerken çalışan `if/else` bloğunu her seferinde
kesip biçmek yerine yeni bir strateji sınıfı ekle. **Fazla olduğu yer:** henüz gelmeyen değişiklik
için soyut eklenti noktaları açmak (spekülatif genellik) — gerçek ikinci vaka gelince soyutla.

### LSP — Liskov Yerine Geçme (Liskov Substitution)
Alt tür, üst türün yerine sürprizsiz geçebilmeli: sözleşmeyi (contract) daraltmamalı, beklenmedik
istisna atmamalı. Klasik örnek: `Kare`'yi `Dikdörtgen`'den türetmek, `genişlik` set edince
`yükseklik`in de değişmesiyle çağıranı şaşırtır. **Fazla olduğu yer:** her kalıtımı LSP korkusuyla
kompozisyona çevirmek; asıl soru davranışın gerçekten yerine geçip geçmediğidir.

### ISP — Arayüz Ayrımı (Interface Segregation)
İstemciyi kullanmadığı metotlara bağımlı bırakma; **küçük ve odaklı arayüzler** birden çok işi olan
dev arayüzlere yeğdir. Günlük dille: sadece yazıcıya ihtiyacı olan koda tarama+faks+yazma içeren bir
"çok fonksiyonlu" arayüz dayatma. **Fazla olduğu yer:** her metot için tek metotlu arayüz üretmek
gereksiz parçalanma yaratır; ölçüt istemcilerin gerçek kullanım kümeleridir.

### DIP — Bağımlılığın Ters Çevrilmesi (Dependency Inversion)
Üst düzey politika, alt düzey ayrıntıya değil; **her ikisi de soyutlamaya** bağlı olmalı — ve
soyutlama ayrıntıya değil, ayrıntı soyutlamaya bağlanmalı. Günlük dille: sipariş servisi doğrudan
`PostgresRepo`'yu değil `SiparisDeposu` arayüzünü tanısın; veritabanı bir ayrıntı olsun.
**Fazla olduğu yer:** tek gerçeklemesi olan ve hiç değişmeyecek şeye arayüz koyup her yeri
dolaylılaştırmak okunabilirliği düşürür.

**DIP ≠ DI — karıştırmayın.** DIP bir *tasarım prensibidir*: "bağımlılık oku somut sınıfa değil
soyutlamaya baksın" der. **Bağımlılık Enjeksiyonu (Dependency Injection, DI)** ise bir
*tekniktir*: bir nesnenin ihtiyaç duyduğu parçayı kendi içinde `new` ile yaratması yerine, o
parçayı dışarıdan (çoğunlukla yapıcı metottan) alması. İkisi aynı şey değildir: somut bir
`PostgresRepo`'yu yapıcı metottan geçirirsen DI yapmış ama yine somut sınıfa bağımlı kalmış
olursun — DIP yoktur. Tersi de mümkündür: DI çatısı hiç kullanmadan, elle bağlayarak da
soyutlamaya bağımlı kalabilirsin. Kısacası DI, DIP'i uygulamayı kolaylaştıran araçlardan
biridir; DIP'in kendisi değildir.

## Diyagramlar

### 1. SOLID tek bakışta
Beş prensip ve her birinin tek cümlelik özü. Tazeleme için haritanın tamamı burada.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SOLID beş prensip tek bakışta: SRP, OCP, LSP, ISP, DIP kutuları ve tek cümlelik özetleri">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">SOLID tek bakışta</text>
  <rect x="20" y="44" width="116" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="78" y="72" text-anchor="middle" font-size="20" font-weight="700" fill="#186399" font-family="system-ui">S</text>
  <text x="78" y="94" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">SRP</text>
  <text x="78" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">tek değişme</text>
  <text x="78" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">nedeni olsun</text>
  <rect x="146" y="44" width="116" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="204" y="72" text-anchor="middle" font-size="20" font-weight="700" fill="#25804C" font-family="system-ui">O</text>
  <text x="204" y="94" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">OCP</text>
  <text x="204" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">eklemeye açık</text>
  <text x="204" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">değiştirmeye kapalı</text>
  <rect x="272" y="44" width="116" height="132" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="330" y="72" text-anchor="middle" font-size="20" font-weight="700" fill="#4F46E5" font-family="system-ui">L</text>
  <text x="330" y="94" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">LSP</text>
  <text x="330" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">alt tür sürprizsiz</text>
  <text x="330" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yerine geçsin</text>
  <rect x="398" y="44" width="116" height="132" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="456" y="72" text-anchor="middle" font-size="20" font-weight="700" fill="#C77A17" font-family="system-ui">I</text>
  <text x="456" y="94" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">ISP</text>
  <text x="456" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">küçük odaklı</text>
  <text x="456" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">arayüzler</text>
  <rect x="524" y="44" width="116" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="582" y="72" text-anchor="middle" font-size="20" font-weight="700" fill="#186399" font-family="system-ui">D</text>
  <text x="582" y="94" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">DIP</text>
  <text x="582" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">soyutlamaya</text>
  <text x="582" y="138" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bağımlı ol</text>
</svg>
```

### 2. Bağımlılığın Ters Çevrilmesi (DIP)
Hem üst hem alt modül aynı soyutlamaya bakar; okların yönü soyutlamaya doğrudur. Slogan:
"detaya değil, soyutlamaya bağımlı ol".

```svg
<svg viewBox="0 0 640 226" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Bağımlılığın ters çevrilmesi: üst modül ve alt modül aynı arayüz soyutlamasına bağımlı">
  <defs>
    <marker id="solid-dip-ok" markerWidth="11" markerHeight="11" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">DIP: detaya değil, soyutlamaya bağımlı ol</text>
  <rect x="40" y="52" width="200" height="66" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="140" y="82" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">ÜST MODÜL</text>
  <text x="140" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">politika · SiparisServisi</text>
  <rect x="400" y="52" width="200" height="66" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="82" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">ALT MODÜL</text>
  <text x="500" y="102" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ayrıntı · PostgresDepo</text>
  <rect x="220" y="150" width="200" height="60" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.8"/>
  <text x="320" y="176" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">SOYUTLAMA</text>
  <text x="320" y="196" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">«arayüz» SiparisDeposu</text>
  <line x1="150" y1="118" x2="270" y2="148" stroke="#186399" stroke-width="1.8" marker-end="url(#solid-dip-ok)"/>
  <line x1="490" y1="118" x2="370" y2="148" stroke="#186399" stroke-width="1.8" marker-end="url(#solid-dip-ok)"/>
  <text x="320" y="140" text-anchor="middle" font-size="10.5" font-weight="600" fill="#5D7789" font-family="system-ui">ikisi de aynı soyutlamaya bağımlı — oklar yukarıdan soyutlamaya bakar</text>
</svg>
```

### 3. SRP ihlali → ayrıştırma
Solda üç sorumluluğu tek sınıfta taşıyan "tanrı sınıf"; sağda her sorumluluk kendi sınıfında.
Aynı davranış, tek değişme nedeni kuralına uygun hale gelir (before → after).

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SRP ihlali ayrıştırması: solda üç sorumluluklu tek sınıf, sağda üç ayrı sınıf">
  <defs>
    <marker id="solid-srp-ok" markerWidth="12" markerHeight="12" refX="8" refY="3.5" orient="auto"><path d="M0,0 L8,3.5 L0,7 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="150" y="24" text-anchor="middle" font-size="13.5" font-weight="700" fill="#1C3247" font-family="system-ui">ÖNCE — ihlal</text>
  <text x="500" y="24" text-anchor="middle" font-size="13.5" font-weight="700" fill="#1C3247" font-family="system-ui">SONRA — ayrıştırılmış</text>
  <rect x="42" y="52" width="216" height="150" rx="12" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.8"/>
  <text x="150" y="78" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17" font-family="system-ui">Rapor</text>
  <line x1="62" y1="90" x2="238" y2="90" stroke="#C77A17" stroke-width="1"/>
  <text x="150" y="114" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">veriYukle()  → veri</text>
  <text x="150" y="140" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">bicimlendir()  → sunum</text>
  <text x="150" y="166" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">yazdir()  → çıktı</text>
  <text x="150" y="190" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">3 aktör → 3 değişme nedeni</text>
  <line x1="266" y1="127" x2="326" y2="127" stroke="#C77A17" stroke-width="2" marker-end="url(#solid-srp-ok)"/>
  <rect x="352" y="46" width="240" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="472" y="68" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">VeriKaynagi</text>
  <text x="472" y="86" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">veriYukle()</text>
  <rect x="352" y="106" width="240" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="128" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Bicimlendirici</text>
  <text x="472" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">bicimlendir()</text>
  <rect x="352" y="166" width="240" height="52" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="472" y="188" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">Yazici</text>
  <text x="472" y="206" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">yazdir()</text>
</svg>
```

## Öz-sınav (kendini test et)
1. SRP'deki "sorumluluk" neden "satır sayısı" değil de "değişme nedeni / aktör" olarak tanımlanır?
2. OCP'yi sağlamak isterken ne zaman spekülatif genelliğe (henüz gelmeyen değişiklik için soyutlama) kaçmış olursun?
3. `Kare`'nin `Dikdörtgen`'den türetilmesi LSP'yi tam olarak nasıl ihlal eder — çağıranın hangi beklentisi kırılır?
4. DIP'te "soyutlama ayrıntıya değil, ayrıntı soyutlamaya bağlanmalı" ifadesindeki ok yönünün pratikte karşılığı nedir?
5. Tek gerçeklemesi olan ve değişmeyecek bir tip için araya arayüz koymak hangi prensibin
   abartısıdır ve bunun maliyeti nedir?
6. "Bağımlılığı yapıcı metottan veriyorum, demek ki DIP uyguluyorum" iddiası neden her zaman
   doğru değildir? DIP ile DI'ın farkını bir cümleyle söyle.
