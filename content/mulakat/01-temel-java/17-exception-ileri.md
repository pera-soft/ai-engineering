---
slug: exception-ileri
baslik: "İleri İstisna: Zincirleme, multi-catch, suppressed, sarmalama"
kademe: İleri
sira: 17
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kargo şirketini düşün. Paketin kayboldu; müşteri hizmetleri sana "işlem başarısız" derse hiçbir şey öğrenmezsin. "Depo yangını nedeniyle paket teslim edilemedi" derse hem ne olduğunu hem nedenini bilirsin. Yazılımda da bir hatayı yakalayıp yeniden fırlatırken **asıl nedeni kaybetmemek** gerekir. Bu bölüm, istisnaları yalnız yakalamayı değil; onları anlamlı, izlenebilir ve katmanlara uygun biçimde iletmeyi anlatır.

### Temel kavramlar

**Kök neden (root cause)**: sorunun ilk çıktığı yerdeki gerçek hata. **Yığın izi (stack trace)**: hatanın hangi metotlardan geçerek buraya geldiğini gösteren döküm.

Üç mekanizma bilinir:

- **Zincirleme (chaining / cause)**: düşük seviye bir istisnayı sarıp (wrap) üst seviye bir istisna fırlatırken orijinali `cause` olarak korursun — `throw new ServisHatasi("...", e)`. Böylece kök neden yığın izinde kaybolmaz; `Throwable(String, Throwable)` constructor'ı ya da `initCause()` bunun içindir. Zincirleme bir tip dönüşümü değildir (checked'i unchecked yapmaz), aynı anda iki istisna fırlatmaz ve loglamayı kapatmaz — yalnızca nedeni ilişkilendirir.
- **multi-catch**: birden çok istisna tipini tek blokta yakalarsın — `catch (IOException | SQLException e)`. Değişkenin statik tipi listelenen tiplerin **ortak üst tipidir** ve değişken **örtük olarak `final`'dır** (implicitly final): blok içinde yeniden atanamaz, cast etmek de zorunlu değildir. Birleştirilen tipler birbirinin alt/üst türü OLMAMALIDIR (`IOException | Exception` derlenmez); listelenen tiplerin hepsi gerçekten yakalanır.
- **suppressed (bastırılmış) istisna**: try-with-resources'ta kapatma sırasında oluşan istisna, asıl istisnayı gölgelemesin diye "bastırılmış" olarak asıl istisnaya eklenir; `getSuppressed()` ile okunur.

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Düşük seviye istisna sarılıp cause olarak korunur">
  <rect x="30" y="66" width="180" height="70" rx="12" fill="#FBEDEA" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="120" y="96" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">SQLException</text>
  <text x="120" y="118" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kök neden (cause)</text>
  <rect x="360" y="66" width="250" height="70" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="485" y="90" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">KullaniciKayitHatasi</text>
  <text x="485" y="112" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">üst seviye + cause=SQLException</text>
  <text x="485" y="128" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">stack trace korunur</text>
  <path d="M210 101 L360 101" stroke="#186399" stroke-width="2" marker-end="url(#exiArrow)"/>
  <text x="285" y="92" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">wrap (cause)</text>
  <defs>
    <marker id="exiArrow" markerWidth="10" markerHeight="10" refX="8" refY="4" orient="auto">
      <path d="M0 0 L9 4 L0 8 z" fill="#186399"/>
    </marker>
  </defs>
</svg>
```

### Nasıl çalışır?

Zincirlemenin somut hâli şudur: alt katman JDBC'ye özgü bir `SQLException` fırlatır; servis katmanı bunu kendi soyutlamasına uygun bir istisnaya sarar ama orijinali `cause` olarak taşır.

```java
try {
    dao.kaydet(kullanici);
} catch (SQLException e) {
    throw new VeriErisimHatasi("kayıt okunamadı", e);   // cause = e
}
```

Yığın izinde artık iki bölüm görünür: üstte `VeriErisimHatasi`, altında **`Caused by: SQLException...`** satırı. Cause'u constructor'a vermeyi unutursan (`new VeriErisimHatasi("...")`) orijinal hatanın nerede oluştuğu tamamen kaybolur.

Bu desenin adı **sar ve yeniden fırlat** (wrap and rethrow) ve bir katmanlama kararıdır: `throws SQLException` ile hatayı olduğu gibi yukarı taşımak, JDBC ayrıntısını tüm üst katmanlara sızdırır ve soyutlamayı kırar. Sarma ise hem ayrıntıyı içeride tutar hem kök nedeni korur.

`catch` bloklarının sırası da burada kritiktir: bloklar yukarıdan aşağıya denenir, ilk eşleşen çalışır. Bu yüzden özel tip önce, genel tip sonra yazılır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="catch sıralaması: özel önce genel sonra">
  <text x="320" y="22" text-anchor="middle" font-size="14" font-weight="700" fill="#1C3247" font-family="system-ui">catch sırası: özel (specific) ÖNCE, genel (general) SONRA</text>
  <rect x="30" y="44" width="275" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="167" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">DOĞRU (derlenir)</text>
  <text x="167" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">catch (IOException e)</text>
  <text x="167" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">özel — önce</text>
  <text x="167" y="146" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">catch (Exception e)</text>
  <text x="167" y="164" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">genel — sonra · ikisi de ulaşılır</text>
  <rect x="335" y="44" width="275" height="150" rx="12" fill="#FBE9E9" stroke="#8A2E2E" stroke-width="1.5"/>
  <text x="472" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">HATA (derlenmez)</text>
  <text x="472" y="98" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">catch (Exception e)</text>
  <text x="472" y="116" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">genel — önce</text>
  <text x="472" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">catch (IOException e)</text>
  <text x="472" y="164" text-anchor="middle" font-size="10" fill="#8A2E2E" font-family="system-ui">ULAŞILAMAZ — "already caught"</text>
</svg>
```

### Mülakatta ne sorulur?

**İstisnayı yutmak (swallowing)** en yaygın anti-patterndir: `catch (Exception e) { }` yasal olarak **derlenir** — sorun teknik değil, sonuçlarıdır. Hata hiçbir iz bırakmadan kaybolur: loglanmaz, yeniden fırlatılmaz, ele alınmaz. Program bozuk bir durumda sessizce devam eder ve gerçek arıza çok sonra, bambaşka bir yerde ortaya çıkar. Ek olarak `catch (Exception e)` fazla geniştir; hem checked hem unchecked (`RuntimeException`) yakalar, yani ele almayı hiç düşünmediğin hataları da toplar. Asıl sorun performans değildir. Doğrusu: en özel tipi yakala ve en azından logla ya da sar-ve-yeniden-fırlat. Aynı şekilde `printStackTrace()` yazıp `null` döndürmek de hatayı gizler ve çağırana yanıltıcı bir değer verir.

**Özel (custom) istisna tasarımı**: uygulama hataları için `Exception`'dan türet; unchecked isteniyorsa `RuntimeException`'dan. `Error`'dan türetmek yanlıştır — o JVM düzeyi kurtarılamaz durumlar içindir, "daha ciddi görünsün" diye kullanılmaz. Doğrudan `Throwable`'dan türetmek de alışılmadık ve kafa karıştırıcıdır. İyi tasarlanmış bir istisna sınıfı üst sınıfın constructor'larını yansıtır: en azından `(String mesaj)` ve **`(String mesaj, Throwable cause)`**. İkinci constructor olmadan zincirleme yapılamaz ve kök neden kaybolur; "sınıfı `final` yapıp hiç constructor tanımlamamak" bu yüzden hatalıdır.

**suppressed istisna**: try-with-resources'ta hem gövde hem `close()` istisna fırlatırsa, **gövdedeki asıl istisna yayılır**; `close()`'unki ona suppressed olarak eklenir ve `getSuppressed()` ile okunur, yığın izinde "Suppressed:" satırıyla görünür. İki istisna birleşip tek nesneye dönüşmez, hiçbiri yutulmaz. Bu, eski `finally` desenindeki "close istisnası gerçek hatayı eziyor" tuzağının çözümüdür.

### Tek cümlede

İstisnayı yutma ya da olduğu gibi yukarı taşıma; kendi katmanının istisnasına **cause'u koruyarak** sar ki kök neden "Caused by:" satırıyla izlenebilir kalsın.

# exc-ileri-1 | Orta
## Soru
İstisna zincirleme (chaining / cause) ne işe yarar?
## A
Orijinal istisnayı yeni istisnaya `cause` olarak bağlar; kök neden (root cause) kaybolmaz
🤖 Doğru: orijinal hata `cause` olarak saklanır, izlenebilirlik kaybolmaz.
## B
İki istisnanın aynı anda, tek bir `throw` deyimiyle fırlatılmasını sağlar
🤖 Aynı anda iki istisna fırlatılamaz; chaining yalnızca kök nedeni korur.
## C
Sarılan istisnanın loglanmasını ve yığın izinin basılmasını otomatik olarak kapatır
🤖 chaining loglamayı kapatmaz; aksine hata bilgisini zenginleştirir.
## D
Checked bir istisnayı, derleyici zorlaması ortadan kalksın diye unchecked'e dönüştürür
🤖 chaining bir tip dönüşümü değildir; sadece nedeni ilişkilendirir.
## Doğru
A
## Açıklama
İstisna zincirleme, bir katmanın kendi soyutlamasına uygun bir istisna fırlatırken **orijinal (düşük seviye) istisnayı kaybetmeden** iletmesini sağlar. `throw new KullaniciKayitHatasi("kayıt başarısız", sqlEx)` yazdığında, `sqlEx` yeni istisnanın `cause`'u olur; yığın izinde "Caused by: SQLException..." satırıyla **kök neden** görünür. Bu olmadan (örn. `throw new KullaniciKayitHatasi("...")`) orijinal hatanın nerede oluştuğu kaybolur ve hata ayıklama zorlaşır. `Throwable(String, Throwable)` constructor'ı veya `initCause()` bunun için vardır.

# exc-ileri-2 | Orta
## Soru
Aşağıdaki multi-catch kullanımı için hangisi doğrudur?

```java
try {
    islem();
} catch (IOException | SQLException e) {
    logla(e);
}
```
## A
`e` değişkeni içinde yeniden atanabilir ve alt tiplerinden birine cast edilmesi zorunludur
🤖 multi-catch değişkeni örtük olarak `final`'dır; yeniden atanamaz, cast de zorunlu değildir.
## B
`IOException` ve `SQLException` birbirinin alt/üst tipi olmalıdır, yoksa derlenmez
🤖 Aksine: multi-catch tipleri birbiriyle alt/üst ilişkili OLMAMALIDIR.
## C
Tek blokta birden çok istisna tipini yakalar; `e` ortak üst tiptedir ve örtük olarak `final`'dır
🤖 Doğru: kod tekrarını önler; `e`'nin tipi ortak atadır, blok içinde yeniden atanamaz.
## D
İki istisnadan yalnızca ilki (`IOException`) gerçekten yakalanır
🤖 İkisi de yakalanır; `|` her iki tip için de bloğu çalıştırır.
## Doğru
C
## Açıklama
multi-catch (`catch (A | B e)`), birden çok istisna tipini **tek blokta** ele alarak kod tekrarını önler. Yakalanan `e` değişkeninin statik tipi, listelenen tiplerin **ortak üst tipidir** (burada `Exception`) ve değişken **örtük olarak `final`'dır** (implicitly final) — bloğun içinde yeniden atanamaz. Önemli kısıt: birleştirilen tipler birbirinin **alt/üst türü OLMAMALIDIR** (örn. `IOException | Exception` derlenmez), çünkü bu durumda biri diğerini gereksiz kılar. Burada `IOException` ve `SQLException` kardeş tiplerdir, sorun yoktur ve ikisi de yakalanır.

# exc-ileri-3 | Orta
## Soru
Kod tabanında şu deseni gördün: `catch (Exception e) { }`. Bu neden bir anti-patterndir?
## A
Boş `catch` bloğu derlenmez, önce düzeltilmelidir
🤖 Boş `catch` yasal olarak derlenir; sorun teknik değil, sonuçlarıdır.
## B
İstisnayı sessizce yutar; hata kaybolur, loglanmaz ve ayıklama imkânsızlaşır
🤖 Doğru: yutulan istisna iz bırakmaz; kök neden görünmez olur.
## C
`catch (Exception e)` yalnızca checked istisnaları yakalar, unchecked'leri kaçırır
🤖 `Exception` hem checked hem unchecked (RuntimeException) yakalar; sorun kapsam değil.
## D
Performansı ciddi biçimde düşürür
🤖 Asıl sorun performans değil; hatanın sessizce yok olmasıdır.
## Doğru
B
## Açıklama
Boş `catch` bloğu (**istisnayı yutma / swallowing**) yasal olarak derlenir ama tehlikelidir: oluşan hata **hiçbir iz bırakmadan** kaybolur — loglanmaz, yeniden fırlatılmaz, ele alınmaz. Sonuç olarak program bozuk bir durumda sessizce devam eder ve gerçek arıza çok sonra, bambaşka bir yerde ortaya çıkar; kök nedeni bulmak neredeyse imkânsızlaşır. Ek olarak `catch (Exception e)` fazla geniştir: ele almayı düşünmediğin `RuntimeException`'ları da yakalar. Doğru yaklaşım: en özel tipi yakala, ve en azından logla veya sar-ve-yeniden-fırlat (wrap and rethrow).

# exc-ileri-4 | İleri
## Soru
Özel (custom) bir checked istisna tasarlarken en doğru yaklaşım hangisidir?
## A
`Error`'dan türet; böylece daha ciddi görünür
🤖 `Error` JVM hataları içindir; uygulama istisnası `Exception`'dan türer.
## B
`Throwable`'dan doğrudan türet; en esnek yol budur
🤖 Doğrudan `Throwable` alışılmadık ve kafa karıştırıcıdır; `Exception`/`RuntimeException` tercih edilir.
## C
`Exception`'dan türet ve `(String mesaj, Throwable cause)` constructor'ını sağla
🤖 Doğru: `Exception` alt sınıfı + cause alan constructor, zincirlemeyi mümkün kılar.
## D
İstisnayı `final` bir sınıf yapıp hiç constructor tanımlama; varsayılan constructor yeter
🤖 cause alan constructor olmadan zincirleme yapılamaz; kök neden kaybolur.
## Doğru
C
## Açıklama
Özel bir **checked** istisna için `Exception`'dan türetmek doğru yerdir (unchecked isteniyorsa `RuntimeException`'dan türetilir); `Error` (A) JVM düzeyi kurtarılamaz durumlar içindir, uygulama hataları için uygun değildir. İyi tasarlanmış bir istisna sınıfı, üst sınıfın constructor'larını yansıtır: en azından `(String mesaj)` ve **`(String mesaj, Throwable cause)`** — ikincisi, düşük seviye bir hatayı sarıp kök nedeni koruyarak yeniden fırlatmanı (chaining) sağlar. Böylece hem anlamlı bir üst seviye mesaj verir hem de "Caused by:" ile orijinal hatayı izlenebilir kılarsın.

# exc-ileri-5 | İleri
## Soru
try-with-resources'ta hem `try` gövdesi hem de kaynağın `close()` metodu istisna fırlatırsa ne olur?
## A
İki istisna birleşip tek bir yeni istisna nesnesine dönüşür
🤖 Birleşme yok; biri asıl, diğeri bastırılmış (suppressed) olarak taşınır.
## B
Her ikisi de yutulur; hiçbir istisna yayılmaz
🤖 İstisnalar yutulmaz; asıl istisna yayılır, diğeri erişilebilir kalır.
## C
`close()` istisnası yayılır; `try` gövdesinin istisnası tamamen kaybolur
🤖 Eski `finally` deseninde olan budur; try-with-resources tam da bunu önler.
## D
`try` gövdesinin istisnası yayılır; `close()` istisnası ona "suppressed" (bastırılmış) olarak eklenir
🤖 Doğru: asıl istisna öncelenir, kapatma istisnası suppressed listesine girer.
## Doğru
D
## Açıklama
try-with-resources'ta öncelik **`try` gövdesindeki asıl istisnadadır**: o yayılır (propagate). Kapatma sırasında `close()` ayrı bir istisna fırlatırsa, bu asıl istisnayı **gölgelemesin** diye ona **suppressed (bastırılmış)** olarak eklenir ve `asilException.getSuppressed()` ile okunabilir. Bu, eski `finally` desenindeki ciddi bir hatayı düzeltir: orada `finally` içindeki `close()` istisnası, `try`'daki gerçek hatayı **eziyor** ve kök neden kayboluyordu (C). Suppressed mekanizması sayesinde her iki istisna da kaybolmadan yığın izinde ("Suppressed:" satırıyla) görünür.

# exc-ileri-6 | İleri
## Soru
Bir DAO katmanı `SQLException` (checked) fırlatıyor; servis katmanın bunu üst katmana nasıl iletmelidir? En sağlam yaklaşım hangisidir?
## A
`catch (SQLException e) {}` ile yut; üst katman zaten null kontrolü yapar
🤖 Yutma anti-patterni; hata kaybolur, üst katman sorunu asla anlayamaz.
## B
`throw new VeriErisimHatasi("kayıt okunamadı", e)` gibi alana özgü bir istisnaya, cause'u koruyarak sar ve yeniden fırlat
🤖 Doğru: cause korunarak sarma, JDBC ayrıntısını sızdırmadan kök nedeni taşır.
## C
`throws SQLException` ile imzaya ekleyip aynen yukarı fırlat; her katman `SQLException` bilsin
🤖 Alt katman ayrıntısını (JDBC) tüm üst katmanlara sızdırır; soyutlama kırılır.
## D
`SQLException`'ı `printStackTrace()` ile yazıp `null` döndür
🤖 `printStackTrace`+null, hatayı gizler ve çağırana yanıltıcı `null` verir; kötü pratik.
## Doğru
B
## Açıklama
En sağlam yaklaşım **sarma (wrap) ve yeniden fırlatmadır**: düşük seviye `SQLException`'ı, katmanın soyutlamasına uygun bir istisnaya (özel bir `VeriErisimHatasi` ya da `RuntimeException`) **cause'u koruyarak** sar. Böylece hem JDBC ayrıntısı üst katmanlara sızmaz (C'nin sorunu), hem kök neden zincirleme sayesinde izlenebilir kalır. Yutma (A) ve `printStackTrace`+null (D) hatayı gizleyen anti-patternlerdir. `SQLException`'ı olduğu gibi tüm imzalara `throws` ile taşımak, alt katman detayını her yere yayarak soyutlamayı ve bakımı bozar. Not: cause'u constructor'a vermeyi (`new ...(e)`) unutma; yoksa kök neden kaybolur.
