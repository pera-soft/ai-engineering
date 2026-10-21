---
slug: design-patterns-behavioral
baslik: "Davranışsal Kalıplar: Strategy (lambda), Observer, Template Method, Command, State, Chain"
kademe: Orta
sira: 135
etiketler: 6
---

## Konu anlatımı

### Bu ne işe yarar?

Bir restoranın mutfağını düşün. Aynı "sipariş geldi" olayına aşçı, kasa ve kurye farklı tepkiler verir. Aynı yemek farklı pişirme yöntemleriyle yapılabilir. Sipariş fişi bir kâğıda yazılır: kuyruğa alınsın, sırası gelince yapılsın, iptal edilirse geri alınsın diye. Davranışsal (behavioral) kalıplar tam olarak bunu düzenler: nesneler arasında **kim ne yapacak ve nasıl haberleşecek**. Ortak temaları, **değişen davranışı onu kullanan koddan ayırmaktır** — böylece yeni bir davranış eklerken çalışan kodu açmak zorunda kalmazsın.

### Temel kavramlar

- **Algoritma**: bir işi yapmanın adım adım yolu (kargo ücretini hesaplama gibi). Aynı iş için birden çok algoritma olabilir.
- **Bağlam (context)**: algoritmayı kullanan nesne. Bağlam **hangi** algoritmanın koştuğunu bilmek zorunda değildir; yalnız arayüzü çağırır.
- **Olay (event) ve dinleyici (listener)**: bir şey olduğunda haber veren tarafa **yayıncı (subject)** denir. Haberi alıp tepki veren taraflar ise **abonelerdir (observer)**.
- **İskelet (template)**: bir akışın değişmeyen sırası. Değişen kısımlar "boş bırakılmış adımlardır".
- **Fonksiyonel arayüz**: tek soyut metodu olan arayüz. Java 8 ve sonrasında böyle bir arayüzü ayrı bir sınıf yazmadan doldurabilirsin. Yerine **lambda** (`x -> x.multiply(TWO)`) ya da metot referansı verilir.

Altı kalıp:

- **Strategy (Strateji)** — birbirinin yerine geçebilen bir **algoritma ailesini** arayüz ardına alır; algoritma **dışarıdan seçilir** ve çalışma zamanında değiştirilebilir.
- **Observer (Gözlemci)** — bir nesnenin durumu değişince **kayıtlı tüm dinleyicilere** haber verir; yayıncı abonelerin somut tiplerini bilmez.
- **Template Method (Şablon Metot)** — algoritmanın **iskeletini** üst sınıfta sabitler, değişen adımları alt sınıfa bırakır. "Hollywood ilkesi": *bizi arama, biz seni ararız.*
- **Command (Komut)** — bir isteği **nesneye** dönüştürür; böylece saklanabilir, kuyruğa alınabilir, loglanabilir ve **geri alınabilir (undo)**.
- **State (Durum)** — nesnenin davranışı **iç durumuna** göre değişir. Her durum ayrı bir sınıf olur ve **durumlar birbirine geçer** (`Taslak → Onaylı → Kargoda`); böylece uzun `if/switch` zinciri ortadan kalkar.
- **Chain of Responsibility (Sorumluluk Zinciri)** — istek, işleyecek olanı bulana kadar bir **zincir boyunca** ilerler (filtre/middleware halkaları gibi).

```svg
<svg viewBox="0 0 640 195" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Strategy: algoritma çalışma zamanında değiştirilebilir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Strategy: aynı bağlam, değişen algoritma</text>
  <rect x="40" y="70" width="170" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="125" y="94" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Bağlam (Sepet)</text>
  <text x="125" y="113" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">strateji.hesapla()</text>
  <rect x="270" y="42" width="150" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="345" y="67" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">NakliyeUcuz</text>
  <rect x="270" y="90" width="150" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="345" y="115" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">NakliyeHizli</text>
  <rect x="270" y="138" width="150" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="345" y="163" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">NakliyeUcretsiz</text>
  <path d="M210 100 L270 62" stroke="#186399" stroke-width="1.4"/>
  <path d="M210 100 L270 110" stroke="#186399" stroke-width="1.4"/>
  <path d="M210 100 L270 158" stroke="#186399" stroke-width="1.4"/>
  <rect x="460" y="82" width="150" height="56" rx="10" fill="#FBE7D8" stroke="#B9791E" stroke-width="1.5"/>
  <text x="535" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B9791E" font-family="system-ui">Java 8+</text>
  <text x="535" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">lambda = strateji</text>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bağlam hangi algoritmayı kullandığını bilmez</text>
</svg>
```

### Nasıl çalışır?

**Strategy.** Ödeme yöntemleri ortak bir arayüzün ayrı implementasyonları olur; bağlam yalnız arayüzü çağırır:

```java
interface OdemeStratejisi { Sonuc ode(BigDecimal tutar); }
sepet.setStrateji(new Kripto());   // çalışma zamanında değişir
```

Arayüz fonksiyonelse strateji ayrı bir sınıf bile olmayabilir: `Map.of("ucuz", a -> a.multiply(TWO), "hizli", a -> a.multiply(TEN))`. Kalıbın **niyeti** korunur; yalnız sınıf iskeleti buharlaşır.

```svg
<svg viewBox="0 0 640 196" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Observer: bir olay, çok sayıda bağımsız dinleyiciye bildirilir">
  <defs>
    <marker id="behav-obs-ok" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0,0 L8,4 L0,8 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Observer: bir olay → çok bağımsız tepki (gevşek bağ)</text>
  <rect x="34" y="76" width="170" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="119" y="100" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Sipariş (subject)</text>
  <text x="119" y="120" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">"onaylandı" yayınlar</text>
  <rect x="440" y="36" width="170" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="61" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">E-posta modülü</text>
  <rect x="440" y="86" width="170" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="111" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Stok modülü</text>
  <rect x="440" y="136" width="170" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="525" y="161" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Analitik modülü</text>
  <line x1="204" y1="100" x2="438" y2="58" stroke="#186399" stroke-width="1.5" marker-end="url(#behav-obs-ok)"/>
  <line x1="204" y1="106" x2="438" y2="106" stroke="#186399" stroke-width="1.5" marker-end="url(#behav-obs-ok)"/>
  <line x1="204" y1="112" x2="438" y2="154" stroke="#186399" stroke-width="1.5" marker-end="url(#behav-obs-ok)"/>
  <text x="320" y="190" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yayıncı abonelerin somut kimliğini bilmez; yeni abone kodu değiştirmez</text>
</svg>
```

**Observer.** "Sipariş onaylandı" olayına e-posta, stok ve analitik modülleri **birbirinden habersiz** tepki verir. Yeni bir modül abone olduğunda sipariş kodu hiç değişmez. **Gevşek bağın (loose coupling)** tanımı tam olarak budur: taraflar birbirinin somut tipini bilmez. Spring'de bu genelde `ApplicationEvent` / `@EventListener` ile uygulanır.

```svg
<svg viewBox="0 0 640 198" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Template Method: sabit iskelet, değişen adımlar alt sınıfta">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Template Method: iskelet sabit, adımlar değişir</text>
  <rect x="150" y="40" width="340" height="30" rx="8" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="320" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">final calistir()  — akış sabit (üst sınıf)</text>
  <rect x="40" y="88" width="110" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="95" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">ac()</text>
  <rect x="164" y="88" width="130" height="34" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="229" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">ayristir() *</text>
  <rect x="308" y="88" width="130" height="34" rx="7" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.6"/>
  <text x="373" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B26B00" font-family="system-ui">dogrula() *</text>
  <rect x="452" y="88" width="70" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="487" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">kaydet()</text>
  <rect x="536" y="88" width="64" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="568" y="110" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">kapat()</text>
  <text x="320" y="150" text-anchor="middle" font-size="11" fill="#B26B00" font-family="system-ui">* soyut adım: CsvIceAktarici / JsonIceAktarici doldurur</text>
  <text x="320" y="176" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hollywood ilkesi: üst sınıf akışı yönetir, alt sınıfın adımını çağırır</text>
</svg>
```

**Template Method.** Akışın sırası sabit, bazı adımlar değişkense:

```java
abstract class VeriIceAktarici {
    public final void calistir() { ac(); var s = ayristir(); dogrula(s); kaydet(s); kapat(); }
    protected abstract List<Satir> ayristir();      // adım değişir
    protected abstract void dogrula(List<Satir> s);
}
```

**Command.** İşlem artık "çağrılıp unutulan bir metot" değil, saklanabilen bir **veridir**. İki parça yeter: `interface Komut { void execute(); void undo(); }` ve bir `Deque<Komut> gecmis` yığını. Kuyruğa alma, kayıt tutma (log), **yeniden deneme (retry)** ve geri alma/ileri alma (undo/redo) hep bu nesneleşmeden doğar.

**State.** Sipariş `iptalEt()` çağrısına `Taslak` iken "sil", `Kargoda` iken "iade başlat" der. Her durum ayrı bir sınıftır ve **bir sonraki duruma geçişi kendisi (ya da bağlam) belirler**. Nesne dışarıdan bakınca hep aynı metotları sunar; değişen şey davranıştır.

### Mülakatta ne sorulur?

- **Strategy ile State farkı.** Sınıf diyagramları neredeyse aynıdır (bağlam + arayüz + implementasyonlar). Fark: Strategy'de algoritmayı **dışarısı** seçer ve stratejiler birbirinden **habersizdir**; State'te davranışı nesnenin **iç durumu** belirler ve durumlar **birbirine geçiş yapar**. İkisi de çalışma zamanında değişir; ikisi de arayüz kullanır — ayrım yapıda değil, **kontrolün kimde olduğundadır**.
- **Strategy ile Template Method farkı.** Template Method **tüm akışı** değil yalnız **değişen adımları** dışarı verir ve kalıtım tabanlıdır; hangi adımın koşacağı derleme zamanında belli olur. Strategy ise tüm algoritmayı sarmalama (kompozisyon) ile dışarı verir ve seçim çalışma zamanında yapılır.
- **Observer ile olay veri yolu (event bus) farkı.** Klasik GoF Observer'da yayıncı, dinleyicilerin **referansını doğrudan** tutar. Bildirim genelde **eşzamanlıdır (senkron)**: aynı iş parçacığında ve aynı süreç içinde koşar. Olay veri yolu ya da mesaj kuyruğunda ise araya bir **aracı (broker)** girer. Yayıncı abonelerin varlığından habersizdir; iletişim **eşzamansız (asenkron)** olur ve süreçler ya da servisler arasına yayılabilir (Kafka, RabbitMQ).
- **Modernleşme.** Strategy, Command ve bazı Observer kullanımları artık lambda ile ifade edilir. Kalıbın **niyetini** koru, **ağır sınıf iskeletini** dilin hafif aracıyla kur.

### Tek cümlede

Davranışsal kalıplar, değişen davranışı bir arayüzün ardına alır. Sonra onu ya dışarıdan seçtirir (Strategy), ya iç duruma bağlar (State), ya çok sayıda dinleyiciye duyurur (Observer), ya da saklanabilir bir nesneye çevirir (Command).

# behav-1 | Temel
## Soru
Bir ödeme sisteminde çalışma zamanında "kredi kartı", "havale", "kripto" gibi farklı hesaplama/işleme algoritmaları arasında geçiş yapmak istiyorsunuz. Hangi kalıp uygundur?
## A
Template Method; ödeme akışının iskeleti üst sınıfta sabitlenip adımlar bölünür
🤖 Burada birkaç adım değil, algoritmanın tamamı değişiyor; ayrıca kalıtım çalışma zamanı geçişi sunmaz.
## B
Observer; ödeme yöntemleri birbirinin durum değişikliğini dinleyip tepki verir
🤖 Ortada bildirim ihtiyacı yok; gereken şey, aralarından birinin seçilip çalıştırılmasıdır.
## C
Strategy; her yöntem ortak bir arayüzün ayrı implementasyonu olur
🤖 Doğru: Strategy algoritmayı soyutlar ve çalışma zamanında yer değiştirmeye izin verir.
## D
Command; her ödeme, geri alınabilir bir işlem nesnesine dönüştürülüp kuyruğa alınır
🤖 Geri alma ya da kuyruklama istenmedi; asıl ihtiyaç algoritma seçimidir.
## Doğru
C
## Açıklama
**Strategy**, birbirinin yerine geçebilen bir **algoritma ailesini** ortak bir arayüz ardına alıp **çalışma zamanında değiştirilebilir** kılmak için tasarlanmıştır — tam da farklı ödeme yöntemleri senaryosu:

```java
interface OdemeStratejisi { Sonuc ode(BigDecimal tutar); }
class KrediKarti implements OdemeStratejisi { /* ... */ }
class Kripto implements OdemeStratejisi { /* ... */ }
sepet.setStrateji(new Kripto());   // bağlam hangi somut stratejiyle çalıştığını bilmez
```

Bağlam (context) sadece arayüzü çağırır; hangi somut algoritmanın koştuğunu bilmediği için yeni yöntem eklemek OCP'ye uygun olur. Template Method (A) **tüm algoritmayı** değil yalnız değişen **adımları** alt sınıfa bırakır ve kalıtım tabanlı olduğu için seçim derleme zamanında donar; Observer (B) bir olaya çoklu bildirim, Command (D) saklanabilir/geri alınabilir işlem içindir. Not: `OdemeStratejisi` fonksiyonel bir arayüz olduğunda basit stratejiler ayrı sınıf yerine **lambda** olarak da verilebilir; kalıbın niyeti değişmez, yalnız iskeleti hafifler.

# behav-2 | Temel
## Soru
Bir veri içe aktarma akışında adımların sırası hep aynı ("dosyayı aç → satırları ayrıştır → doğrula → kaydet → kapat") ama "ayrıştır" ve "doğrula" adımları formata göre (CSV, JSON) değişiyor. İskeleti sabit tutup sadece değişen adımları alt sınıfa bırakan kalıp hangisidir?
## A
Template Method; iskelet üst sınıfta sabit kalır, değişen adımlar soyut metot olur
🤖 Doğru: sabit akış + değişen adımlar tam olarak Template Method'un tanımıdır.
## B
Strategy; tüm içe aktarma algoritması dışarıdan tek bir nesne olarak verilir
🤖 Burada akışın çoğu sabit; Strategy algoritmanın tamamını dışarı verir, ortak iskeleti korumaz.
## C
Observer; her adım bir sonrakini olay yayarak tetikler ve akış böyle zincirlenir
🤖 Adımlar arasında bildirim ilişkisi yok; sıra zaten baştan belli ve sabittir.
## D
Command; her adım execute/undo sunan ayrı bir işlem nesnesine dönüştürülür
🤖 Geri alma ya da kuyruklama ihtiyacı yok; sorun sabit akıştaki değişken adımları soyutlamak.
## Doğru
A
## Açıklama
**Template Method**, bir algoritmanın **değişmeyen iskeletini** üst sınıfta (genellikle `final` bir şablon metotta) sabitler ve yalnız **değişen adımları** alt sınıfların doldurması için **soyut metotlara** bırakır:

```java
abstract class VeriIceAktarici {
    public final void calistir() {          // iskelet sabit
        ac(); var s = ayristir(); dogrula(s); kaydet(s); kapat();
    }
    protected abstract List<Satir> ayristir();  // adım değişir
    protected abstract void dogrula(List<Satir> s);
}
class CsvIceAktarici extends VeriIceAktarici { /* iki adımı doldurur */ }
```

Bu "Hollywood ilkesidir": *"Bizi arama, biz seni ararız"* — üst sınıf akışı yönetir, alt sınıfın adımlarını çağırır. Strategy (B) **tüm** algoritmayı dışarı verir ve ortak iskeleti garanti etmez; Observer (C) bildirim, Command (D) saklanabilir işlem içindir. Template Method kalıtım tabanlıdır, dolayısıyla seçim derleme zamanında sabitlenir; aynı ihtiyaç çalışma zamanında değiştirilebilir olmalıysa kompozisyona, yani Strategy'ye kayılır.

# behav-3 | Orta
## Soru
Bir sipariş onaylandığında birbirinden habersiz birçok modülün (e-posta, stok, analitik) tepki vermesi; yeni modül eklerken sipariş kodunun değişmemesi isteniyor. Hangi kalıp uygundur?
## A
Strategy; her modül siparişe enjekte edilen ayrı bir algoritma olarak verilir
🤖 Strategy seçilen tek algoritmayı çalıştırır; burada aynı olaya çok sayıda bağımsız tepki gerekiyor.
## B
Observer; sipariş kayıtlı dinleyicilere haber verir, kimliklerini bilmez
🤖 Doğru: bir-çok bildirim ve gevşek bağ tam Observer'ın tanımıdır; yeni abone yayıncıyı değiştirmez.
## C
Template Method; onay akışının iskeleti sabitlenip adımlar alt sınıflara bırakılır
🤖 İhtiyaç sabit akış değil, tek bir olaya çok sayıda bağımsız tepki üretmektir.
## D
Command; her tepki geri alınabilir bir işlem nesnesi olarak kuyruğa alınır
🤖 Geri alma ya da kuyruklama istenmiyor; istenen, ilgilenen herkese duyuru yapmaktır.
## Doğru
B
## Açıklama
**Observer**, bir nesnenin (subject/yayıncı) durumu değiştiğinde **kayıtlı birçok dinleyiciye** otomatik haber vermesini sağlar ve yayıncı ile aboneler arasında **gevşek bağ (loose coupling)** kurar: yayıncı, abonelerin **somut tiplerini bilmez**, yalnız bir dinleyici arayüzü üzerinden bildirir. "Sipariş onaylandı" olayına e-posta, stok ve analitik modüllerinin bağımsızca tepki vermesi — ve yeni bir modülün sipariş kodunu **değiştirmeden** abone olabilmesi — tam bu kalıbın işidir; bu yönüyle OCP'nin doğal bir uygulamasıdır. Strategy (A) aralarından **tek** birini seçip çalıştırır, çoklu bildirim yapmaz; Template Method (C) sabit bir iskeletin değişken adımlarını, Command (D) saklanabilir/geri alınabilir işlemi konu alır. Spring'de bu genelde `ApplicationEvent` / `@EventListener` ile uygulanır — Observer'ın çerçeve düzeyinde, olay yayını tabanlı bir varyantıdır.

# behav-4 | Orta
## Soru
Bir metin editöründe her kullanıcı işlemini (yaz, sil, biçimlendir) geri alınabilir (undo) ve loglanabilir kılmak; işlemleri kuyruğa alıp sonra çalıştırabilmek istiyorsunuz. Hangi kalıp uygundur?
## A
Strategy; her işlem, seçilebilir bir algoritma olarak ortak arayüz ardına alınır
🤖 Strategy algoritma seçer ama işlemi saklanabilir bir nesneye çevirmez; undo/kuyruk doğmaz.
## B
Observer; işlemler yayınlanır, bir geçmiş modülü bunları dinleyip biriktirir
🤖 Dinleyici olan biteni kaydedebilir ama işlemi geri alma yeteneğini kendiliğinden vermez.
## C
Template Method; işlem akışının iskeleti sabitlenir, değişen adımlar alt sınıfa kalır
🤖 Sabit iskelet sorunu değil; işlemin saklanıp sonradan çalıştırılması/geri alınması gerekiyor.
## D
Command; her işlem `execute()`/`undo()` sunan bir nesneye dönüştürülür
🤖 Doğru: isteği nesneleştirmek Command'ın özüdür; undo, kuyruk ve log bu sayede mümkün olur.
## Doğru
D
## Açıklama
**Command**, bir **isteği/işlemi bir nesneye** dönüştürür. İşlem artık "çağrılıp unutulan bir metot" değil, saklanabilen ve geçirilebilen bir **veridir**; bu da onu kuyruğa almayı, loglamayı, yeniden denemeyi ve en önemlisi **geri almayı (undo)** mümkün kılar:

```java
interface Komut { void execute(); void undo(); }
class YazKomutu implements Komut { /* execute: ekle, undo: sil */ }
Deque<Komut> gecmis = new ArrayDeque<>();  // undo yığını
```

Her işlem `execute()` ile uygulanır ve bir **geçmiş yığınına** konur; `undo()` ile geri alınır. Bu, editör/CAD gibi undo-redo gerektiren ya da işlemlerin kuyruğa alınıp asenkron çalıştırıldığı sistemlerin bel kemiğidir. Strategy (A) değiştirilebilir algoritma, Template Method (C) sabit iskelet içindir. B en çekici çeldiricidir: Observer ile "ne olduğunu" dinleyip **loglayabilirsin**, ama geri almak için işlemin **kendisini ve tersini** taşıyan bir nesne gerekir — Observer bunu vermez. Basit komutlar Java'da `Runnable`/lambda ile de temsil edilebilir.

# behav-5 | İleri
## Soru
Strategy ile State kalıplarının sınıf diyagramı neredeyse aynıdır (bağlam + arayüz + implementasyonlar). Aralarındaki temel fark nedir?
## A
Strategy'de algoritmayı dışarısı seçer; State'te iç durum belirler ve durumlar birbirine geçer
🤖 Doğru: yapı aynı, kontrol farklı — biri dışarıdan seçilir, diğeri nesnenin durumuna bağlıdır.
## B
Strategy çalışma zamanında değiştirilebilir; State ise derleme zamanında sabitlenir
🤖 İkisi de çalışma zamanında değişir; State'in tüm anlamı zaten durum geçişlerinin runtime'da olmasıdır.
## C
Fark terstir: State'te davranış dışarıdan enjekte edilir, Strategy'de nesne kendi durumunu değiştirir
🤖 Roller ters çevrilmiş; enjekte edilen Strategy'dir, kendi geçişini yöneten State'tir.
## D
Strategy arayüz kullanmak zorundadır; State ise yalnız kalıtım hiyerarşisiyle yazılabilir
🤖 İkisi de aynı araçlarla, arayüz ya da soyut sınıfla yazılabilir; ayrım araçta değil niyettedir.
## Doğru
A
## Açıklama
İki kalıp **yapısal olarak** ayırt edilemez: bağlam, bir arayüze bağlıdır ve o arayüzün birden çok implementasyonu vardır. Ayrım **kontrolün kimde olduğudur**. **Strategy**'de algoritmayı **dışarısı seçer** — istemci ya da konfigürasyon `sepet.setStrateji(new Kripto())` der; stratejiler birbirinden **habersizdir**, birbirine geçiş yapmaz ve bağlamın durumunu değiştirmez. **State**'te davranış nesnenin **iç durumuna** bağlıdır: bir `Siparis` nesnesi `Taslak` iken `iptalEt()` çağrısında kaydı siler, `Kargoda` iken iade süreci başlatır. Üstelik durumlar **birbirine geçer** (`Taslak → Onaylı → Kargoda`) ve geçiş kuralı durum sınıflarının (ya da bağlamın) içinde yaşar; istemci hangi durumda olunduğunu bilmek zorunda değildir. State'in tipik faydası, duruma göre dallanan uzun `if/switch` zincirlerini ortadan kaldırmasıdır. Diğer şıklar yanlıştır: State de çalışma zamanında değişir (B) — zaten geçişler runtime'da olur; roller ters çevrilemez (C); ve ikisi de aynı dil araçlarıyla yazılır (D). Pratik ayrım sorusu: **"davranışı dışarıdan biri mi seçiyor, yoksa nesnenin o anki durumu mu belirliyor?"**

# behav-6 | İleri
## Soru
Klasik GoF Observer'ı (nesne, dinleyici listesi tutar ve doğrudan çağırır) bir mesaj kuyruğu / event bus üzerinden yayınlanan olaylardan (event-driven) ayıran temel fark nedir?
## A
Aracı olup olmaması fark etmez; event bus yalnızca Observer kalıbının yeni adıdır
🤖 Kavramsal akrabalar ama eşdeğer değiller; bağ derecesi, senkronluk ve süreç sınırı farklıdır.
## B
Klasik Observer daima asenkron çalışır; event bus ise bildirimi senkron hâle getirir
🤖 Ters ve aşırı genelleme: klasik Observer genelde senkron, event bus ise asenkron/dağıtık olabilir.
## C
Observer'da yayıncı dinleyiciyi doğrudan tutar; event bus'ta araya bir broker girer
🤖 Doğru: belirleyici olan aracının varlığı; bağ derecesi, senkronluk ve süreç sınırı buradan çıkar.
## D
Observer bir arayüz tekniği, event bus ise bir veritabanı özelliğidir; ortak yanları yoktur
🤖 Event bus veritabanı özelliği değildir; ikisi de aynı "haber ver" fikrinin farklı ölçeklerdeki hâlidir.
## Doğru
C
## Açıklama
İkisi de "bir şey olunca ilgilenenlere haber ver" fikrini paylaşır ama **bağ derecesi ve sınırlar** farklıdır. **Klasik GoF Observer**'da subject, gözlemci nesnelerin **referanslarını doğrudan** bir listede tutar; bildirim genellikle **senkron**, **aynı thread ve aynı süreç** içinde, düz bir metot çağrısıyla olur. Yayıncı aboneleri arayüz üzerinden tanısa da onların **canlı referansına sahiptir** — görece sıkı bağ, ve bir dinleyicinin yavaşlaması yayıncıyı bekletir. **Event-driven / message bus** yaklaşımında ise yayıncı olayı bir **aracıya (broker / event bus)** verir; abonelerin varlığından bile habersizdir. İletişim **asenkron** olabilir, **süreçler hatta servisler arası** yayılabilir (Kafka, RabbitMQ, Spring `ApplicationEventPublisher`) ve dayanıklılık/yeniden deneme gibi özellikler aracıdan gelir. Asıl eksen budur: **doğrudan referans + senkron + tek süreç** (Observer) ↔ **aracı + asenkron + dağıtık** (event bus). İkisi aynı şey değildir (A); senkronluk iddiaları terstir (B); ve event bus bir veritabanı özelliği değil, bir iletişim altyapısıdır (D).
