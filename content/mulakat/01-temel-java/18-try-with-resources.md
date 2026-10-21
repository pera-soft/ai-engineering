---
slug: try-with-resources
baslik: "try-with-resources: Otomatik Kaynak Kapatma ve Kapatma Sırası"
kademe: Orta
sira: 18
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Kütüphaneden kitap alırsan geri vermen gerekir; vermezsen kitap raftan eksik kalır ve kimse kullanamaz. Programda da dosya, veritabanı bağlantısı, ağ soketi gibi şeyler işletim sisteminden "ödünç" alınır ve iş bitince geri verilmelidir. Geri verilmezse **kaynak sızıntısı (resource leak)** olur; uygulama zamanla açık dosya veya bağlantı limitine çarpar. try-with-resources, bu geri verme işini senin yerine ve unutulmayacak biçimde yapan dil yapısıdır.

### Temel kavramlar

**Kaynak (resource)**: kullanıldıktan sonra kapatılması gereken nesne — dosya akışı (stream), JDBC bağlantısı, kilit sarmalayıcısı.

**`close()`**: kaynağı serbest bırakan metot. Eski yöntem, `try-finally` bloğunda bunu **elle** çağırmaktı; hataya çok açıktı.

**try-with-resources** (Java 7) bu işi otomatikleştirir:

- `try (...)` parantezinde verilen kaynak, **`AutoCloseable`** (veya alt arayüzü `Closeable`) uygulamak zorundadır — yani bir `close()` metodu bulunmalıdır. Metot adı sözleşme gereği `close`'tur; `Serializable` olmak, `final` sınıf olmak ya da parametresiz constructor bulundurmak gerekmez.
- Blok normal bitse de, istisna fırlatsa da `close()` **otomatik** çağrılır — aynı iş parçacığında, blok biter bitmez; arka planda gecikmeli bir kapatma yoktur.
- Çoklu kaynak varsa, **açılışın TERSİ sırayla** kapatılırlar (son açılan ilk kapanır).
- Kapatma sırasında oluşan istisna, asıl istisnayı gölgelemesin diye **suppressed (bastırılmış)** olarak eklenir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kaynaklar açılışın tersi sırayla kapatılır">
  <text x="320" y="24" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">Açılış sırası vs kapatma sırası (ters)</text>
  <rect x="60" y="50" width="150" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="135" y="75" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1. aç: A</text>
  <rect x="245" y="50" width="150" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="75" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">2. aç: B</text>
  <rect x="430" y="50" width="150" height="40" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="505" y="75" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">3. aç: C</text>
  <rect x="60" y="132" width="150" height="40" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="135" y="157" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">son kapat: A</text>
  <rect x="245" y="132" width="150" height="40" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="157" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">2. kapat: B</text>
  <rect x="430" y="132" width="150" height="40" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="505" y="157" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ilk kapat: C</text>
  <path d="M505 90 L505 132" stroke="#5D7789" stroke-width="1.5" marker-end="url(#twrArrow)"/>
  <text x="560" y="115" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ters</text>
  <defs>
    <marker id="twrArrow" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

Önce gövde çalışır, sonra kaynaklar ters sırayla kapatılır:

```java
class Kaynak implements AutoCloseable {
    String ad;
    Kaynak(String ad) { this.ad = ad; }
    public void close() { System.out.print(ad); }
}

try (Kaynak a = new Kaynak("A"); Kaynak b = new Kaynak("B")) {
    System.out.print("X");
}
// çıktı: XBA
```

Gövde `X` yazar; blok bitince en son açılan `b` önce kapanır (`B`), ardından `a` (`A`). Ters sıralama bilinçli bir tasarımdır: sonradan açılan kaynak genelde önce açılana bağımlıdır — örneğin bir `BufferedReader` alttaki `FileReader`'ı sarar; bağımlı olanı önce kapatmak tutarlıdır.

İkinci mekanizma **suppressed**'dir. Hem gövde hem `close()` istisna fırlatırsa öncelik **gövdedeki asıl istisnadadır**:

```java
class K implements AutoCloseable {
    public void close() { throw new RuntimeException("KAPAT"); }
}

try (K k = new K()) {
    throw new RuntimeException("GOVDE");
}
// çağırana GOVDE gider; KAPAT ona suppressed olarak eklenir
```

`close()` istisna fırlatabilir (derleme sorunu yoktur) ve hiçbir istisna yutulmaz: `KAPAT`, `govdeException.getSuppressed()` ile okunabilir ve yığın izinde "Suppressed: ...KAPAT" satırıyla görünür. Eski `try-finally` deseninde ise `finally` içindeki `close()` istisnası gövdedeki gerçek hatayı **eziyor** ve kök neden kayboluyordu; try-with-resources'ın en önemli kazancı budur — sadece daha az kod değil, **doğruluk**.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="suppressed exception: close hatası asıl hataya eklenir">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">İki hata çakışırsa: asıl fırlar, close hatası "suppressed"</text>
  <rect x="30" y="46" width="240" height="66" rx="12" fill="#FBEDEA" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="150" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">try gövdesi hata: E1</text>
  <text x="150" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ASIL istisna (fırlatılır)</text>
  <rect x="30" y="126" width="240" height="60" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="150" y="151" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">close() hata: E2</text>
  <text x="150" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bastırılır (suppressed)</text>
  <rect x="360" y="78" width="250" height="80" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="485" y="106" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Çağırana E1 gider</text>
  <text x="485" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">E2 → E1.getSuppressed()</text>
  <text x="485" y="148" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ikisi de kaybolmaz</text>
  <path d="M270 88 L360 104" stroke="#B23B3B" stroke-width="1.6" marker-end="url(#twr18)"/>
  <path d="M270 150 L360 132" stroke="#B26B00" stroke-width="1.6" marker-end="url(#twr18)"/>
  <defs>
    <marker id="twr18" markerWidth="9" markerHeight="9" refX="7" refY="4" orient="auto">
      <path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Mülakatta ne sorulur?

**`AutoCloseable` mi `Closeable` mı?** `AutoCloseable` (Java 7) **üst** arayüzdür; `close()` metodu **herhangi bir `Exception`** fırlatabilir, bu yüzden genel amaçlı kaynaklar (JDBC bağlantısı, kilit sarmalayıcısı) için uygundur. `java.io.Closeable` (daha eski, `java.io` paketinde) onu **genişletir** ve I/O'ya özeldir. Onun `close()` metodu yalnızca `IOException` fırlatabilir. Ayrıca sözleşmesi gereği **idempotent** olması beklenir; yani birden çok kez çağrılınca sorun çıkarmamalıdır. Yani her `Closeable` aynı zamanda bir `AutoCloseable`'dır, tersi değildir. İlişki asla ters yönde kurulmaz. try-with-resources temel sözleşme olarak `AutoCloseable` ister; `Closeable` onun alt türü olduğu için elbette çalışır.

**Sürüm gerçeği:** Java 7-8'de kaynağı parantez içinde **yerinde tanımlamak** zorunluydu (`try (Kaynak k = new Kaynak())`). **Java 9** ile kural gevşetildi: dışarıda tanımlanmış **effectively final** (veya `final`) bir değişkeni `try (k) { ... }` biçiminde doğrudan verebilirsin. Effectively final olması şarttır, aksi hâlde kapatılacak nesne belirsizleşirdi. Bu yazımda da kapatma garantisi aynen korunur: kaynak **bir kez** ve doğru sırayla kapatılır — "kapatmaz" ya da "iki kez kapatır" gibi bir davranış yoktur.

**Pratik kural:** kapatılması gereken her şeyi (I/O, JDBC, kilit sarmalayıcıları) mümkünse try-with-resources ile yönet; elle `finally`'de `close()` yazmaktan kaçın. Kapatma ortadan kalkmaz — yalnızca otomatik, ters sıralı ve suppressed-korumalı hâle gelir.

### Tek cümlede

try-with-resources, `AutoCloseable` kaynakları blok biter bitmez açılışın tersi sırayla otomatik kapatır ve kapatma hatasını asıl hatanın üstünü örtmeden "suppressed" olarak saklar.

# twr-1 | Temel
## Soru
try-with-resources ile bir kaynağın otomatik kapatılabilmesi için sınıfının hangi koşulu sağlaması gerekir?
## A
`AutoCloseable` (veya alt arayüzü `Closeable`) uygulamalı, yani bir `close()` metodu olmalıdır
🤖 Doğru: try-with-resources yalnızca `AutoCloseable` uygulayan tiplerle çalışır.
## B
`Serializable` arayüzünü uygulamalı, yani nesnesi diske yazılabilir olmalıdır
🤖 `Serializable` kaynak kapatmayla ilgisizdir; gereken `AutoCloseable`'dır.
## C
`static` bir `kapat()` metodu tanımlamalı ve bu metodu public yapmalıdır
🤖 Hem isim hem static olması yanlış; arayüz sözleşmesi gereği `close()` gerekir.
## D
`final` bir sınıf olmalı ve parametresiz bir constructor'ı bulunmalıdır
🤖 Ne `final` olması ne de parametresiz constructor gerekir; tek şart `AutoCloseable`'dır.
## Doğru
A
## Açıklama
try-with-resources yalnızca **`AutoCloseable`** arayüzünü (veya onun I/O'ya özel alt arayüzü `Closeable`'ı) uygulayan tiplerle çalışır. Bu arayüzün tek metodu `close()`'tur; blok bitince JVM bu metodu otomatik çağırır. `Closeable.close()` yalnızca `IOException` fırlatabilirken, `AutoCloseable.close()` herhangi bir `Exception` fırlatabilir — bu yüzden `AutoCloseable` daha geneldir. `Serializable` (B) veri serileştirmeyle ilgilidir, kapatmayla değil; metot adı da sözleşme gereği `close` olmalıdır (C yanlış).

# twr-2 | Orta
## Soru
Aşağıdaki kod ne yazdırır?

```java
class Kaynak implements AutoCloseable {
    String ad;
    Kaynak(String ad) { this.ad = ad; }
    public void close() { System.out.print(ad); }
}
// main içinde:
try (Kaynak a = new Kaynak("A"); Kaynak b = new Kaynak("B")) {
    System.out.print("X");
}
```
## A
`XAB`
🤖 Kapatma ters sırayla olur; önce B kapanır, `AB` değil `BA` beklenir.
## B
`ABX`
🤖 Kapatma gövdeden SONRA olur; `X` başta yazılır.
## C
`XBA`
🤖 Doğru: gövde `X`, sonra açılışın tersiyle önce `B` sonra `A` kapanır.
## D
`XAB` ve ardından istisna
🤖 İstisna yok; sıra da ters olmalı, `AB` değil `BA`.
## Doğru
C
## Açıklama
Önce `try` gövdesi çalışır ve `X` yazdırılır. Blok bitince kaynaklar **açılışın TERSİ sırayla** kapatılır: en son açılan `b` ("B") önce kapanır, sonra `a` ("A"). Bu yüzden kapatma çıktısı `BA` olur ve toplam sonuç `XBA`'dır. Ters sıralama bilinçli bir tasarımdır: genelde sonradan açılan kaynak, önce açılana bağımlıdır (örn. bir `BufferedReader` alttaki `FileReader`'ı sarar); bağımlı olanı önce kapatmak tutarlıdır.

# twr-3 | Orta
## Soru
Eski `try-finally` ile kaynak kapatmaya kıyasla try-with-resources'ın temel avantajı nedir?
## A
Kaynak kapatmayı tamamen gereksiz kılar; `close()` artık hiçbir koşulda çağrılmaz
🤖 `close()` yine çağrılır; sadece OTOMATİK ve doğru şekilde çağrılır.
## B
Yalnızca performans kazandırır; kapatma davranışı eski desenle birebir aynıdır
🤖 Davranış farkı önemlidir: suppressed istisna ve sıra garantisi eski desende yoktur.
## C
Kaynakları arka planda ayrı bir thread'de, gecikmeli olarak kapatır
🤖 Kapatma aynı thread'de, blok biter bitmez yapılır; ayrı thread yok.
## D
Kapatmayı otomatikleştirir, çoklu kaynağı ters sırayla kapatır ve kapatma istisnasını "suppressed" ile korur
🤖 Doğru: otomatik, ters sıralı, suppressed-korumalı; boilerplate ve tuzakları giderir.
## Doğru
D
## Açıklama
Eski `try-finally` deseninde iki yaygın hata vardı: (1) `finally` içindeki `close()`'un kendisi istisna fırlatırsa, `try`'daki **asıl istisnayı eziyor** ve kök neden kayboluyordu; (2) çoklu kaynakta doğru sırayla ve her kaynak için ayrı ayrı kapatma yazmak zahmetli ve hataya açıktı. try-with-resources bunları çözer: `close()`'u **otomatik**, açılışın **tersi sırayla** çağırır ve kapatma istisnasını asıl istisnaya **suppressed (bastırılmış)** olarak ekleyerek ikisini de korur. Yani `close()` yine çağrılır (A yanlış) ama doğru ve güvenli biçimde; kazanç sadece performans değil, doğruluktur (B yanlış).

# twr-4 | İleri
## Soru
Aşağıdaki kodda asıl (yayılan) istisna hangisidir?

```java
class K implements AutoCloseable {
    public void close() { throw new RuntimeException("KAPAT"); }
}
// main içinde:
try (K k = new K()) {
    throw new RuntimeException("GOVDE");
}
```
## A
`RuntimeException("KAPAT")` yayılır; gövde istisnası kaybolur
🤖 Tam tersi: gövde istisnası öncelenir, KAPAT bastırılmış olur.
## B
Derleme hatası; `close()` istisna fırlatamaz
🤖 `AutoCloseable.close()` istisna fırlatabilir; derleme sorunu yok.
## C
İki istisna da yutulur; hiçbiri yayılmaz
🤖 İstisnalar yutulmaz; gövde istisnası kesinlikle yayılır.
## D
`RuntimeException("GOVDE")` yayılır; `KAPAT` ona suppressed (bastırılmış) olarak eklenir
🤖 Doğru: asıl istisna gövdeninkidir; close istisnası suppressed listesine girer.
## Doğru
D
## Açıklama
try-with-resources'ta **gövdedeki istisna asıldır** ve yayılır: burada `RuntimeException("GOVDE")`. Kapatma sırasında `close()` ayrı bir istisna (`"KAPAT"`) fırlatsa da, bu asıl istisnayı gölgelemez; onun yerine asıl istisnaya **suppressed (bastırılmış)** olarak eklenir ve `govdeException.getSuppressed()` ile erişilebilir; yığın izinde "Suppressed: ...KAPAT" satırı görünür. Bu, eski `finally` desenindeki "close istisnası gerçek hatayı eziyor" tuzağının (A) tam da çözümüdür. `close()` istisna fırlatabilir (B yanlış) ve hiçbir şey yutulmaz (C yanlış).

# twr-5 | İleri
## Soru
Java 9+ ile try-with-resources hakkında hangisi doğrudur?

```java
Kaynak k = new Kaynak("A");  // effectively final
try (k) {
    // ...
}
```
## A
Java'nın hiçbir sürümünde parantez içinde önceden tanımlı değişken kullanılamaz
🤖 Java 9'dan itibaren effectively final bir değişken doğrudan parantezde kullanılabilir.
## B
Java 9'dan itibaren `try (k)` biçiminde, effectively final bir değişken doğrudan parantezde kullanılabilir
🤖 Doğru: Java 9 bu kolaylığı ekledi; değişken effectively final olmalı.
## C
`try (k)` yazımı kaynağı kapatmaz; yalnızca yerinde tanımlar (`try (Kaynak k = ...)`) kapatır
🤖 Java 9+ bu biçimde de kaynağı kapatır; kapatma garantisi korunur.
## D
Bu yazım kaynağı iki kez kapatır: bir kez blok sonunda, bir kez de değişken kapsam dışına çıkınca
🤖 Kaynak yalnızca bir kez kapatılır; çift kapatma diye bir davranış yok.
## Doğru
B
## Açıklama
Java 7-8'de try-with-resources parantezinde kaynağı **yerinde tanımlamak** zorunluydu (`try (Kaynak k = new Kaynak())`). Java 9 ile bu gevşetildi: parantez içinde, dışarıda tanımlanmış **effectively final** (veya `final`) bir değişkeni doğrudan referansla verebilirsin — `try (k) { ... }`. Bu, aynı kaynağı birden çok yerde kullanırken ya da kaynağı bir metottan alırken kodu sadeleştirir. Değişkenin effectively final olması şarttır (aksi halde kapatılacak nesne belirsizleşirdi). Kapatma garantisi aynen korunur; kaynak bir kez ve doğru sırayla kapatılır.

# twr-6 | İleri
## Soru
`Closeable` ve `AutoCloseable` arasındaki ilişki/fark için hangisi doğrudur?
## A
İkisi birbirinden tamamen bağımsız arayüzlerdir; aralarında kalıtım ilişkisi yoktur
🤖 İlişkilidirler: `Closeable`, `AutoCloseable`'ı genişletir.
## B
`Closeable`, `AutoCloseable`'ı genişletir; `close()`'u yalnız `IOException` fırlatır ve idempotent olması beklenir
🤖 Doğru: `Closeable` daha dar (I/O odaklı) sözleşme, `AutoCloseable` daha genel.
## C
`AutoCloseable`, `Closeable`'ı genişletir; bu yüzden `AutoCloseable` daha kısıtlı bir sözleşmedir
🤖 İlişki ters: `Closeable` `AutoCloseable`'ı genişletir, `AutoCloseable` daha geneldir.
## D
try-with-resources yalnızca `Closeable` uygulayan tiplerle çalışır; `AutoCloseable` tek başına yetmez
🤖 try-with-resources temel olarak `AutoCloseable` ile çalışır; `Closeable` onun alt türüdür.
## Doğru
B
## Açıklama
`AutoCloseable` (Java 7) **üst** arayüzdür ve `close()` metodu **herhangi bir `Exception`** fırlatabilir — genel amaçlı kaynaklar (JDBC, kilitler vb.) için uygundur. `Closeable` (java.io, daha eski) ise `AutoCloseable`'ı **genişletir** ve I/O'ya özeldir: `close()` yalnızca `IOException` fırlatabilir ve sözleşmesi gereği **idempotent** olması (birden çok kez çağrılınca sorun çıkarmaması) beklenir. Yani her `Closeable` aynı zamanda bir `AutoCloseable`'dır ama tersi değil. try-with-resources temel sözleşme olarak `AutoCloseable` ister; `Closeable` onun alt türü olduğu için elbette çalışır (D yanlış).
