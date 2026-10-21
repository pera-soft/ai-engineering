---
slug: solid
baslik: "SOLID: Beş Prensip, SRP Sınırı, LSP İhlali ve DIP–DI İlişkisi"
kademe: Orta
sira: 131
etiketler: 5
---

## Konu anlatımı

### Bu ne işe yarar?

Bir apartman düşün: mutfaktaki musluğu değiştirmek için salonun duvarını yıkmak gerekiyorsa, o bina kötü tasarlanmıştır. Yazılımda da aynısı olur. "İndirim oranı değişsin" gibi küçücük bir istek geldiğinde on ayrı dosyayı açmak zorunda kalırsın. Dokunduğun her yerde de yeni bir hata doğma riski vardır. SOLID, nesne yönelimli kodu **değişime dayanıklı** kılmak için önerilen beş kuralın baş harfleridir. Tek bir amacı vardır: yeni bir gereksinim geldiğinde **az yeri, güvenle** değiştirebilmek.

### Temel kavramlar

Önce sözlük:

- **Sınıf (class)**: veriyi ve o veriyle yapılan işleri bir arada tutan kod birimi.
- **Bağımlılık (dependency)**: A sınıfı işini görmek için B'yi kullanıyorsa A, B'ye **bağımlıdır**; B değişince A kırılabilir.
- **Kuplaj / bağlılık (coupling)**: iki kod parçasının birbirine ne kadar sıkı yapıştığı. Sıkı kuplajda birini değiştirmek diğerini bozar; hedef **gevşek kuplajdır**.
- **Soyutlama (abstraction)**: "nasıl yapıldığını" saklayıp yalnız "ne yapıldığını" gösteren sadeleştirilmiş görüntü. Direksiyon bir soyutlamadır: motorun içini bilmeden araba kullanırsın.
- **Arayüz (interface)**: Java'da soyutlamanın somut aracı; gövdesiz metot imzalarından oluşan bir **sözleşme**. `interface OdemeGecidi { Sonuc ode(BigDecimal tutar); }` şunu der: "benim yerime geçecek her sınıf `ode` sunmalı." Nasıl yaptığı kendi bileceği iştir.
- **Çok biçimlilik (polimorfizm)**: aynı arayüzün farklı gerçekleştirimlerinin (implementation) birbirinin yerine geçebilmesi. Çağıran kod hiç değişmez; hangi somut sınıfın geldiğini bilmesi bile gerekmez.

Beş prensip:

- **SRP (Single Responsibility)** — bir sınıfın **değişmesi için tek bir nedeni** olmalı. Buradaki "neden" bir paydaş/değişim eksenidir; metot sayısı, alan sayısı ya da implemente edilen arayüz adedi değildir.
- **OCP (Open/Closed)** — davranış **genişletmeye açık**, çalışan kod **değiştirmeye kapalı** olmalı: yeni bir tür eklerken mevcut sınıfı açıp bozmazsın.
- **LSP (Liskov Substitution)** — alt tip, üst tipin **davranışsal beklentilerini** bozmadan onun yerine geçebilmeli.
- **ISP (Interface Segregation)** — istemciyi **kullanmadığı** metotlara bağımlı bırakma; şişman arayüzü küçük rollere böl.
- **DIP (Dependency Inversion)** — üst seviye politika alt seviye ayrıntıya değil, **ikisi de soyutlamaya** bağlansın; ok yönü tersine döner.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="DIP: üst ve alt seviye ikisi de soyutlamaya bağlanır">
  <defs>
    <marker id="solid-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">DIP: ok yönü tersine döner</text>
  <rect x="40" y="60" width="200" height="56" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="140" y="84" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Üst seviye politika</text>
  <text x="140" y="103" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">SiparisServisi</text>
  <rect x="230" y="150" width="180" height="46" rx="10" fill="#FBE7D8" stroke="#B9791E" stroke-width="1.5"/>
  <text x="320" y="171" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B9791E" font-family="system-ui">«arayüz»</text>
  <text x="320" y="188" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">OdemeGecidi</text>
  <rect x="400" y="60" width="200" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="500" y="84" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Alt seviye ayrıntı</text>
  <text x="500" y="103" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">StripeAdaptoru</text>
  <line x1="140" y1="116" x2="270" y2="150" stroke="#186399" stroke-width="1.6" marker-end="url(#solid-ok)"/>
  <line x1="500" y1="116" x2="370" y2="150" stroke="#186399" stroke-width="1.6" marker-end="url(#solid-ok)"/>
  <text x="320" y="146" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ikisi de soyutlamaya bağlanır</text>
</svg>
```

Beşi tek bir cümlede toplanabilir: **birlikte değişenler bir arada dursun, ayrı değişenler ayrılsın.**

```svg
<svg viewBox="0 0 640 150" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SOLID beş prensibin özeti">
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Beş prensip, tek amaç: az yeri güvenle değiştir</text>
  <rect x="14" y="38" width="112" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="70" y="66" text-anchor="middle" font-size="21" font-weight="700" fill="#186399" font-family="system-ui">S</text>
  <text x="70" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">SRP</text>
  <text x="70" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tek değişme</text>
  <text x="70" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">nedeni</text>
  <rect x="139" y="38" width="112" height="98" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="195" y="66" text-anchor="middle" font-size="21" font-weight="700" fill="#25804C" font-family="system-ui">O</text>
  <text x="195" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">OCP</text>
  <text x="195" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">genişlet,</text>
  <text x="195" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">değiştirme</text>
  <rect x="264" y="38" width="112" height="98" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="21" font-weight="700" fill="#B26B00" font-family="system-ui">L</text>
  <text x="320" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">LSP</text>
  <text x="320" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yerine güvenle</text>
  <text x="320" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">geçebilme</text>
  <rect x="389" y="38" width="112" height="98" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="445" y="66" text-anchor="middle" font-size="21" font-weight="700" fill="#6B3FA0" font-family="system-ui">I</text>
  <text x="445" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">ISP</text>
  <text x="445" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">küçük rol</text>
  <text x="445" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">arayüzleri</text>
  <rect x="514" y="38" width="112" height="98" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="570" y="66" text-anchor="middle" font-size="21" font-weight="700" fill="#8A2E2E" font-family="system-ui">D</text>
  <text x="570" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">DIP</text>
  <text x="570" y="106" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">soyutlamaya</text>
  <text x="570" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağlan</text>
</svg>
```

### Nasıl çalışır?

**OCP pratikte**: `if (tür == ÖĞRENCİ) ... else if (tür == EMEKLİ) ...` zinciri her yeni türde açılıp değiştirilir — yani genişlemeye kapalı, değişikliğe açıktır; tam tersi olmalıydı. Davranışı bir arayüzün arkasına alırsın:

```java
public interface IndirimStratejisi { BigDecimal uygula(BigDecimal tutar); }
public final class OgrenciIndirimi implements IndirimStratejisi { /* ... */ }
// yeni tür = yeni sınıf; çalışan kod hiç açılmaz
```

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="OCP: if-else zinciri yerine polimorfik strateji">
  <defs>
    <marker id="solid-ocp-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">OCP: yeni tür = yeni sınıf, çalışan kod dokunulmaz</text>
  <text x="160" y="48" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">İhlal — if/else zinciri</text>
  <rect x="34" y="58" width="252" height="104" rx="10" fill="#EAF1F7" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="52" y="84" text-anchor="start" font-size="11" fill="#5D7789" font-family="system-ui">if tür==ogrenci ...</text>
  <text x="52" y="106" text-anchor="start" font-size="11" fill="#5D7789" font-family="system-ui">else if tür==emekli ...</text>
  <text x="52" y="128" text-anchor="start" font-size="11" fill="#8A2E2E" font-family="system-ui">else if tür==YENI ...  (ekle)</text>
  <text x="160" y="152" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">her türde bu bloğu değiştir</text>
  <text x="482" y="48" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Çözüm — strateji arayüzü</text>
  <rect x="356" y="58" width="250" height="36" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="481" y="81" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">«arayüz» IndirimStratejisi</text>
  <rect x="356" y="110" width="120" height="32" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="416" y="131" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">OgrenciIndirimi</text>
  <rect x="486" y="110" width="120" height="32" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="546" y="131" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">EmekliIndirimi</text>
  <line x1="416" y1="94" x2="416" y2="110" stroke="#25804C" stroke-width="1.4" marker-end="url(#solid-ocp-ok)"/>
  <line x1="546" y1="94" x2="546" y2="110" stroke="#25804C" stroke-width="1.4" marker-end="url(#solid-ocp-ok)"/>
  <text x="481" y="160" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">yeni tür = yeni implementasyon, mevcut kod kapalı</text>
</svg>
```

**DIP pratikte**: `SiparisServisi` içinde `new StripeIstemcisi()` yazarsan üst seviye politika, alt seviye ayrıntıya bağlanmış olur. Bunun yerine `OdemeGecidi` arayüzü tanımlanır, `StripeAdaptoru` onu implemente eder. Kritik incelik **arayüzün kime ait olduğudur**. Soyutlama, onu **kullanan** üst seviye katmana aittir; ayrıntı ona uyar. Arayüzü adaptörün paketine koyarsan politika yine ayrıntının paketine bağlanır. O zaman tersine çevirme (inversion) iptal olur.

**DIP ile DI aynı şey değildir.** DIP bir **prensiptir**: bağımlılığı somut sınıftan soyutlamaya çevir. DI (Dependency Injection), yani **bağımlılığın dışarıdan verilmesi**, o prensibi hayata geçiren bir **tekniktir**. Bağımlılığı sınıfın içinde `new` ile üretmek yerine **yapıcı metottan (constructor)** dışarıdan alırsın. DI kullanmak tek başına DIP garantisi vermez: yapıcı metottan **somut** bir sınıf enjekte edersen bağımlılık hâlâ ayrıntıya bakar. Tersi de mümkündür — DI olmadan, bir fabrika üzerinden de DIP'e uyulabilir.

### Mülakatta ne sorulur?

- **SRP'nin tanımı.** "Sınıf tek iş yapsın" yüzeysel cevaptır; doğrusu "tek **değişme nedeni**". Bir `Rapor` sınıfı hem hesabı hem PDF biçimini içeriyorsa, muhasebe ve tasarım ekipleri onu **farklı nedenlerle** değiştirir — iki eksen ayrılmalıdır. SRP bir **birleşiklik (cohesion)** ölçüsüdür.
- **LSP ihlali örneği.** Klasik örnek `Kare extends Dikdortgen`'dir. `Dikdortgen`'in davranışsal sözleşmesi, iki kenarın **bağımsız** değişebilmesidir. `Kare` ise bir kenarı değiştirince diğerini de eşitler ve bu beklentiyi bozar (alan 20 yerine 16 çıkar). Kalıtım "is-a" (bir türüdür) sözcüğüne değil, **davranışsal olarak yerine geçebilmeye** dayanmalı. Alt tipte istisna atmak veya sözleşmeyi daraltmak da LSP ihlalidir.
- **ISP kokusu.** Şişman bir `Calisan` arayüzü `RobotCalisan`'a `yemekYe()` implemente ettirir; boş gövde ya da `UnsupportedOperationException` çıkar. Bu aynı zamanda LSP kokusu üretir ama **kök neden arayüzün fazla yüklenmiş olmasıdır**; çözüm rol arayüzlerine bölmektir (`Calisabilir`, `Beslenebilir`, `Ucretlendirilebilir`).
- **Ölçü sorusu.** SOLID bir din değil, **koku tespit aracıdır**. Her sınıfa beş prensibi zorla dayatmak aşırı mühendisliğe (over-engineering) götürür. Prensipleri **değişimin gerçekten acıttığı** yerlerde uygula.

### Tek cümlede

SOLID, kodu "birlikte değişenleri bir araya, ayrı değişenleri ayrı yerlere" koyarak düzenleyen ve bağımlılığı somut ayrıntıya değil soyutlamaya çeviren beş kuralın adıdır.

# solid-1 | Temel
## Soru
SRP'deki "tek sorumluluk" en doğru şekilde nasıl tanımlanır?
## A
Bir sınıf en fazla tek bir public metot sunmalı, gerisi private kalmalıdır
🤖 SRP metot sayısını sınırlamaz; onlarca uyumlu metodu olan bir sınıf pekâlâ tek sorumluluk taşıyabilir.
## B
Bir sınıf tek bir alan tutmalı, veri büyüyünce yeni sınıflara bölünmelidir
🤖 Sorumluluk veri miktarıyla ölçülmez; ölçüt sınıfın hangi nedenle değiştiğidir.
## C
Bir sınıfın değişmesi için yalnız tek bir nedeni olmalıdır
🤖 Doğru: SRP'nin özü "tek değişme nedeni"dir; farklı nedenler farklı sınıflara ayrılır.
## D
Bir sınıf yalnız tek bir arayüzü implemente etmeli, çoklu arayüzden kaçınmalıdır
🤖 Arayüz adedi SRP'nin konusu değildir; bu ölçüt daha çok ISP tartışmasıyla karıştırılır.
## Doğru
C
## Açıklama
Robert Martin'in tanımıyla SRP, "bir modülün **değişmesi için tek bir nedeni** olmalıdır" der; buradaki "neden" bir **paydaş grubu / değişim eksenidir**. Örneğin bir `Rapor` sınıfı hem veriyi hesaplıyor hem de PDF'e biçimliyorsa, muhasebe (hesap kuralı) ve tasarım (biçim) ekipleri onu **farklı nedenlerle** değiştirir; bu iki eksen ayrılmalıdır. Metot sayısı (A), alan sayısı (B) veya implemente edilen arayüz sayısı (D) sorumlulukla doğrudan ilgili değildir — bunlar ölçülmesi kolay ama yanlış ölçütlerdir. SRP bir **birleşiklik (cohesion)** ölçüsüdür: birlikte değişenler bir arada dursun. Pratik test: "bu sınıfı değiştirmemi kim isteyebilir?" sorusuna birden fazla ekip adı veriyorsan sınıf bölünmelidir.

# solid-2 | Temel
## Soru
DIP (Dependency Inversion) ile DI (Dependency Injection) arasındaki ilişki nedir?
## A
DIP bir tasarım prensibidir; DI ise onu hayata geçiren bir tekniktir
🤖 Doğru: DIP "ne"yi (bağımlılığı soyutlamaya çevir), DI "nasıl"ı (dışarıdan enjekte et) söyler.
## B
İkisi aynı şeyin iki adıdır; DI kullanan her kod otomatik olarak DIP'e de uymuş olur
🤖 DI tek başına yetmez: constructor'dan somut bir sınıf enjekte edersen bağımlılık hâlâ ayrıntıya bakar.
## C
DI bir prensiptir; DIP ise onu uygulayan, Spring'e özgü bir çerçeve tekniğidir
🤖 Ters ve yanlış: DIP prensip, DI tekniktir; ayrıca DIP hiçbir çerçeveye bağlı değildir.
## D
DIP arayüz kullanmak, DI ise kalıtımla üst sınıftan davranış devralmak demektir
🤖 DIP "arayüz kullan" demeye eşit değildir; DI de kalıtım değil, bağımlılığın dışarıdan verilmesidir.
## Doğru
A
## Açıklama
**DIP** bir **prensiptir**: üst seviye modüller alt seviye ayrıntılara değil, ikisi de bir **soyutlamaya** bağlanmalı ve bağımlılık oku ayrıntıdan soyutlamaya doğru "ters" akmalıdır. **DI** ise bu prensibe uymanın yaygın bir **uygulama tekniğidir**: bağımlılığı sınıfın içinde `new`'lemek yerine dışarıdan (genellikle constructor'dan) enjekte etmek. İkisi eşit değildir ve biri diğerini garanti etmez (B): constructor'dan `StripeIstemcisi` gibi **somut** bir tip enjekte edersen DI yapmış ama DIP'e uymamış olursun; tersine, DI olmadan bir fabrika üzerinden de DIP'e uyulabilir. Rolleri ters çevirmek (C) yaygın bir karıştırmadır ve DIP çerçeveden bağımsız, evrensel bir prensiptir. DIP'i "arayüz kullanmak" sanmak (D) da eksiktir: mesele arayüzün varlığı değil, **kimin kime bağlandığıdır**.

# solid-3 | Orta
## Soru
Yeni bir indirim türü eklerken `if/else if` zincirine sürekli yeni dal ekleyen kod hangi prensibi ihlal eder ve doğru çözüm nedir?
## A
LSP'yi ihlal eder; çözüm desteklenmeyen dalları alt sınıflarda istisna atarak kapatmaktır
🤖 Konu yerine geçebilirlik değil, genişleme kolaylığıdır; istisna atmak durumu daha da kötüleştirir.
## B
OCP'yi ihlal eder; çözüm her türü ayrı bir strateji sınıfına almaktır
🤖 Doğru: yeni davranış eklerken çalışan kodu değiştirmemek için polimorfizmle genişlet.
## C
SRP'yi ihlal eder; çözüm tüm indirim kurallarını tek bir yardımcı sınıfta toplamaktır
🤖 Tüm kuralları tek sınıfta toplamak sorumluluğu daha da yığar; sorun genişleme biçimidir.
## D
Hiçbir prensibi ihlal etmez; if/else zinciri en hızlı ve en okunur çözümdür
🤖 Her yeni türde çalışan kodu açmak OCP ihlalidir; hız burada belirleyici bir ölçüt değildir.
## Doğru
B
## Açıklama
Her yeni indirim türünde mevcut `if/else` bloğunu **açıp değiştirmek** OCP'yi (Open/Closed) ihlal eder: kod genişlemeye kapalı, değişikliğe açık hâldedir — oysa tersi olmalı. Doğru çözüm davranışı bir **soyutlamanın arkasına** almaktır:

```java
public interface IndirimStratejisi {
    BigDecimal uygula(BigDecimal tutar);
}
// Yeni tür = yeni sınıf; mevcut kod dokunulmaz kalır
public final class OgrenciIndirimi implements IndirimStratejisi { /* ... */ }
```

Böylece yeni bir türü **yeni bir sınıf ekleyerek** (genişleme) getirir, çalışan sınıfları hiç değiştirmezsin. Bu, OCP'nin polimorfizmle klasik uygulamasıdır; seçim çalışma zamanında bir `Map<Tür, IndirimStratejisi>` ya da Spring'in enjekte ettiği bir liste ile yapılabilir. Alt sınıfta istisna atmak (A) LSP'yi de kırar; kuralları tek sınıfta yığmak (C) SRP'yi bozar; "en hızlı çözüm" savunması (D) ise yanlış ekseni tartışır — buradaki maliyet çalışma hızı değil, **her değişiklikte çalışan koda dokunma riskidir**.

# solid-4 | Orta
## Soru
Aşağıdaki `Kare extends Dikdortgen` tasarımında `genislikAyarla(5)` sonra `yukseklikAyarla(4)` çağrılıp alan 20 beklenirken 16 çıkması hangi prensibin ihlalidir?

```java
class Dikdortgen { void genislikAyarla(int g){...} void yukseklikAyarla(int y){...} }
class Kare extends Dikdortgen { /* iki kenarı da eşitler */ }
```
## A
OCP; yeni bir şekil eklendiğinde mevcut sınıfları açıp değiştirmek gerekiyor
🤖 Burada sorun genişleme değil, alt tipin üst tipin davranışsal beklentisini bozması.
## B
ISP; üst sınıf, istemcinin kullanmadığı metotları da dayattığı için arayüz şişmiş
🤖 İki setter şişman arayüz sayılmaz; sorun yerine geçebilirliktedir.
## C
SRP; sınıf hem geometri hem de ölçü doğrulama sorumluluğunu birlikte taşıyor
🤖 Tek sorumluluk var (dörtgen davranışı); mesele davranışsal uyum, sorumluluk sayısı değil.
## D
LSP; Kare, üst tipin "kenarlar bağımsız değişir" beklentisini bozuyor
🤖 Doğru: alt tip üst tipin davranışsal sözleşmesini kırdığı için onun yerine geçemez.
## Doğru
D
## Açıklama
Bu, LSP'nin (Liskov Substitution) klasik "Kare–Dikdörtgen" karşı örneğidir. `Dikdortgen`'in **davranışsal sözleşmesi** genişlik ve yüksekliğin **bağımsız** değiştirilebilmesidir. `Kare` bir kenarı değiştirince diğerini de zorla eşitleyerek bu beklentiyi bozar; dolayısıyla `Dikdortgen` bekleyen bir kod `Kare` aldığında **sürpriz sonuç** üretir (16 ≠ 20). Alt tip, üst tipin yerine **güvenle geçemiyorsa** LSP ihlal edilmiştir. Ders: kalıtım "is-a" (bir tür) sözcüğüne değil, **davranışsal ikame edilebilirliğe** dayanmalı. Matematikte kare bir dikdörtgendir ama **davranışsal olarak** bu kalıtım hiyerarşisi yanlıştır; iki tipi ayırmak ya da nesneyi değişmez (immutable) yapmak sorunu çözer. Burada genişleme (A), arayüz şişkinliği (B) veya sorumluluk çokluğu (C) yoktur; ihlal edilen tek şey **davranışsal sözleşmedir**.

# solid-5 | İleri
## Soru
Tek bir dev `Calisan` arayüzü `calis()`, `yemekYe()`, `maasHesapla()` metotlarını içeriyor; `RobotCalisan` ise yemek yemiyor. Bu durum hangi prensibi ihlal eder ve en iyi çözüm nedir?
## A
ISP'yi ihlal eder; çözüm şişman arayüzü küçük rol arayüzlerine bölmektir
🤖 Doğru: istemci kullanmadığı metoda bağımlı kalmasın diye arayüz rollere ayrılır.
## B
DIP'i ihlal eder; çözüm RobotCalisan'ı doğrudan somut Calisan sınıfına bağlamaktır
🤖 Sorun bağımlılık yönü değil; somuta bağlamak zaten DIP'e aykırıdır.
## C
LSP'yi ihlal eder; çözüm yemekYe() gövdesini boş bırakıp sessizce geçmektir
🤖 Boş gövde bir LSP kokusu üretir ama kök neden şişman arayüzün ta kendisidir.
## D
OCP'yi ihlal eder; çözüm eksik davranışları arayüze yeni metotlar ekleyerek tamamlamaktır
🤖 Arayüzü daha da büyütmek derdi katmerler; sorun genişleme değil ayrıştırmadır.
## Doğru
A
## Açıklama
`RobotCalisan`'ı `yemekYe()` metoduna **bağımlı** bırakan şişman arayüz ISP'yi (Interface Segregation) ihlal eder: istemci **kullanmadığı** bir davranışı implemente etmek (genelde boş gövde ya da `UnsupportedOperationException`) zorunda kalır. Bu boş/istisna atan implementasyonlar aynı zamanda bir LSP kokusu üretir (C'nin kısmen doğru gördüğü yer budur) ama **kök neden** arayüzün fazla iş yüklenmiş olmasıdır ve C'nin önerdiği çözüm — sessizce hiçbir şey yapmamak — sorunu gizler, çözmez. Doğru çözüm arayüzü **kullanıma göre küçük rollere** ayırmaktır:

```java
interface Calisabilir { void calis(); }
interface Beslenebilir { void yemekYe(); }
interface Ucretlendirilebilir { BigDecimal maasHesapla(); }
```

`InsanCalisan` üçünü de, `RobotCalisan` yalnız `Calisabilir` + `Ucretlendirilebilir`'i implemente eder. Her istemci **yalnız ihtiyacı olan** role bağlanır; gereksiz bağımlılık ortadan kalkar. Somuta bağlanmak (B) DIP'i bozar, arayüzü büyütmek (D) ise mevcut sorunu derinleştirir.

# solid-6 | İleri
## Soru
DIP'e gerçekten uymak için `OdemeGecidi` arayüzü hangi katmana ait olmalı ve neden?
## A
Adaptörün paketine; arayüzü implemente eden sınıfın yanında durması en doğal yerdir
🤖 Arayüzü ayrıntının yanına koymak tersine çevirmeyi iptal eder; üst seviye yine ayrıntı paketine bağlanır.
## B
Ortak bir "utils" paketine; arayüzler katmanlardan bağımsız ortak alanda toplanmalıdır
🤖 Nötr çöp paket sahipliği belirsizleştirir; DIP soyutlamanın politikaya ait olmasını ister.
## C
Üst seviye politikanın paketine; arayüzün sahibi onu kullanan istemcidir
🤖 Doğru: soyutlama istemciye aittir, ayrıntı ona uyar; bağımlılık oku böylece tersine döner.
## D
Hiçbir yere; arayüz yerine somut sınıfı doğrudan enjekte etmek daha sade bir tasarımdır
🤖 Somut sınıf enjekte etmek DIP'i bozar; bağımlılık yine ayrıntıya döner.
## Doğru
C
## Açıklama
DIP'in en sık atlanan inceliği **arayüzün mülkiyetidir** (ownership). Bağımlılığı gerçekten tersine çevirmek için soyutlama, onu **kullanan** üst seviye politikaya ait olmalıdır; implementasyon (ayrıntı) bu soyutlamaya **uyar**. Yani `OdemeGecidi` arayüzü `SiparisServisi`'nin katmanında/paketinde tanımlanır, `StripeAdaptoru` onu **dışarıdan implemente eder**. Böylece derleme-zamanı bağımlılık oku ayrıntıdan (Stripe) soyutlamaya (arayüz) doğru akar — "inversion" tam da budur. Arayüzü adaptörün yanına koymak (A) politikanın yine ayrıntı paketine bağlanmasına yol açar ve tersine çevirmeyi **iptal eder**; nötr bir `utils` paketi (B) sahipliği belirsizleştirir ve genelde çöp kutusuna dönüşür; somut sınıfı enjekte etmek (D) ise DI yapıp DIP'i atlamanın klasik örneğidir. Bu mülkiyet kuralı, hexagonal (port-adapter) mimarinin de temelidir: **port'lar (arayüzler) çekirdeğe aittir.**
