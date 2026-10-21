---
slug: object-metotlari-io-temel
baslik: "Object Metotları ve Temel I/O: Stream, Reader/Writer, clone Tuzağı"
kademe: Temel
sira: 28
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bu konu iki şeyi birleştirir: her nesnenin doğuştan taşıdığı ortak yetenekler, ve programın dış dünyayla (dosya, ağ) veri alışverişi.

Birincisi için bir fabrikayı düşün: bantta ne üretilirse üretilsin, her parçanın üstünde aynı standart bilgiler bulunur — okunur bir ad, seri numarası, "bu parça şununla aynı mı?" karşılaştırması, bir de "kopyasını çıkar" düğmesi. Java'da her sınıf `Object`'ten türediği için her nesne bu standart yeteneklerle doğar.

İkincisi için bir kuyu düşün. Suyu bardak bardak çekersen her seferinde kuyuya inip çıkarsın, iş korkunç yavaşlar; kovayla çekip bardakları kovadan doldurursan iş katlanarak hızlanır. Ayrıca kuyudan çektiğin şeyin su mu kum mu olduğu, hangi kapla taşıyacağını belirler.

### Temel kavramlar

- **`Object`**: Java'daki tüm sınıfların atası. Sen yazmasan da her sınıf ondan türer.
- **Override (ezme)**: miras alınan bir metodu kendi sınıfında yeniden yazmak.
- **Akış (stream)**: veriyi bir uçtan diğerine parça parça taşıyan kanal. (Java 8'in `Stream` API'siyle karıştırma; buradaki I/O akışıdır.)
- **Karakter kodlaması (charset)**: harfleri sayılara çeviren kural tablosu, ör. UTF-8. "ş" harfinin hangi baytlarla yazılacağını bu belirler.
- **Sistem çağrısı (system call)**: programın işletim sisteminden bir iş istemesi (ör. "diskten oku"). Pahalı bir işlemdir.
- **Tampon (buffer)**: bellekte tutulan geçici toplama alanı — kuyu benzetmesindeki kova.

`Object`'ten miras alınan çekirdek metotlar:

- `toString()` — nesnenin okunur metin gösterimi. Override edilmezse varsayılan olarak `getClass().getName() + "@" + Integer.toHexString(hashCode())` döner, ör. `Kisi@1b6d3586`. `System.out.println(nesne)` içeride `String.valueOf(nesne)` → `nesne.toString()` çağırır. Alanları otomatik döken bir davranış YOKTUR (ne boş string basar, ne derleme hatası verir); anlamlı çıktıyı ya elle yazarsın ya IDE/Lombok üretir.
- `equals(Object)` / `hashCode()` — mantıksal eşitlik ve hash değeri. **Birlikte ve tutarlı** override edilmelidir (kontrat): `a.equals(b)` doğruysa `a.hashCode() == b.hashCode()` de olmalıdır.
- `getClass()` — nesnenin çalışma zamanı (runtime) sınıfı. `final`'dır, override edilemez.
- `clone()` — nesnenin kopyasını üretir; iki ayrı tuzağı vardır.

```svg
<svg viewBox="0 0 660 248" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Object'ten miras alınan çekirdek metotlar tablosu">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Object'ten miras çekirdek metotlar</text>
  <rect x="24" y="40" width="180" height="34" rx="6" fill="#1C3247"/>
  <rect x="210" y="40" width="426" height="34" rx="6" fill="#1C3247"/>
  <text x="114" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Metot</text>
  <text x="423" y="62" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Ne yapar / dikkat</text>
  <rect x="24" y="78" width="180" height="38" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1"/>
  <text x="114" y="102" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">toString()</text>
  <rect x="210" y="78" width="426" height="38" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="224" y="102" font-size="10.5" fill="#1C3247" font-family="system-ui">okunur metin; varsayılan SinifAdi@onaltılıkHash</text>
  <rect x="24" y="120" width="180" height="38" rx="6" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <text x="114" y="144" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">equals()/hashCode()</text>
  <rect x="210" y="120" width="426" height="38" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1"/>
  <text x="224" y="144" font-size="10.5" fill="#1C3247" font-family="system-ui">mantıksal eşitlik + hash; BİRLİKTE ve tutarlı ezilmeli (kontrat)</text>
  <rect x="24" y="162" width="180" height="38" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1"/>
  <text x="114" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">getClass()</text>
  <rect x="210" y="162" width="426" height="38" rx="6" fill="#FFFFFF" stroke="#186399" stroke-width="1"/>
  <text x="224" y="186" font-size="10.5" fill="#1C3247" font-family="system-ui">çalışma-zamanı (runtime) sınıfı; final, ezilemez</text>
  <rect x="24" y="204" width="180" height="38" rx="6" fill="#FBEEDC" stroke="#B26B00" stroke-width="1"/>
  <text x="114" y="228" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">clone()</text>
  <rect x="210" y="204" width="426" height="38" rx="6" fill="#FFFFFF" stroke="#B26B00" stroke-width="1"/>
  <text x="224" y="228" font-size="10.5" fill="#1C3247" font-family="system-ui">sığ kopya; Cloneable şart, referans alanını paylaştırır (tuzak)</text>
</svg>
```

### Nasıl çalışır?

**clone'un birinci tuzağı**: `Cloneable` metotsuz bir **işaretleyici arayüzdür** (marker interface). Sınıf onu uygulamazsa `Object.clone()` `CloneNotSupportedException` fırlatır. Sınıfın `final` olması, hiç alanı olmaması ya da `equals` yazmamış olması bu istisnayla ilgisizdir — tek koşul `Cloneable`'dır. Metot `Object`'te bulunmasına rağmen davranışının ayrı bir arayüzün varlığına bağlı olması, clone API'sinin tuhaf tasarımının parçasıdır.

**İkinci tuzağı**: `Object.clone()` **sığ kopya** (shallow copy) üretir.

```java
class Takim implements Cloneable {
    List<String> oyuncular = new ArrayList<>();
    public Takim clone() throws CloneNotSupportedException {
        return (Takim) super.clone();
    }
}
```

`b = a.clone()` sonrası `b.oyuncular.add("X")` çağırırsan `a.oyuncular` DA değişir. Sebep: ilkel alanlar kopyalanır, ama referans alanları (liste, dizi, iç nesne) kopyalanmaz — iki nesne AYNI listeyi paylaşır. Burada ne derleme hatası ne istisna vardır; sinsi bir çalışma zamanı hatasıdır. Derin kopya (deep copy) için referans alanları elle klonlanır: `kopya.oyuncular = new ArrayList<>(oyuncular);`. Bu tuzaklar yüzünden clone genelde kötü tasarlanmış bir API sayılır; yerine kopyalama constructor'ı (copy constructor) veya factory tercih edilir.

**`equals`'te tür kontrolü**: iki yol vardır ve seçim davranışı değiştirir.

- `o instanceof Nokta` — alt sınıf örneklerini de kabul eder (esnek). Argüman null ise sessizce `false` döner, yani null-güvenlidir. Dikkatsiz mirasta simetri kuralını (`a.equals(b) == b.equals(a)`) bozabilir.
- `getClass() != o.getClass()` — **tam tür eşitliği** ister; `Nokta` ile alt sınıfı `RenkliNokta` asla eşit sayılmaz. Katıdır, ama simetriyi garantiler. Argüman null olabileceğinden önce null kontrolü gerekir.

İkisi de çalışma zamanı kontrolüdür; fark alt sınıf davranışındadır. Effective Java bu ayrımı klasik bir tasarım tartışması olarak ele alır.

**I/O'da iki eksen**:

- **Byte akışı (byte stream)**: `InputStream`/`OutputStream` — ikili (binary) veri: resim, PDF, ses, ağ. Birim bayttır; charset UYGULANMAZ, baytlar olduğu gibi taşınır.
- **Karakter akışı (char stream)**: `Reader`/`Writer` — metin. Araya bir charset koyup baytları karaktere çevirir. Birim karakterdir.

Kural: metin → Reader/Writer, ikili → InputStream/OutputStream. Bir JPEG'i `Reader` ile okursan charset dönüşümü baytları sessizce bozar; "ikisi de aynı sonucu verir" doğru değildir. Köprü sınıfları `InputStreamReader`/`OutputStreamWriter` bayt↔karakter dönüşümünü, verdiğin charset ile yapar.

**Buffered I/O**: tamponsuz `fis.read()` her çağrıda bir sistem çağrısı yapar; bayt bayt okumada bu milyonlarca kez tekrarlanır. `BufferedInputStream`/`BufferedReader` araya bir bellek tamponu koyar: bir seferde büyük bir blok okunur, sonraki `read()`'ler o tampondan karşılanır — sistem çağrısı sayısı, dolayısıyla süre dramatik düşer. Burada ne sıkıştırma (compression) ne paralellik vardır; tampon RAM'dedir ve diske gidiş sayısını azaltır. Aynı mantık `BufferedWriter` için de geçerlidir.

`File` sınıfı yalnız dosyanın yolunu, varlığını, izin gibi meta bilgisini temsil eder — içeriği değil; okumak için yine bir akış gerekir. Modern kodda `java.nio.file.Path`/`Files` tercih edilir.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Byte akışı ikili veri, karakter akışı metin; buffered system call azaltır">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hangi akış? + neden Buffered?</text>
  <rect x="24" y="38" width="300" height="96" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="174" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Byte akışı</text>
  <text x="174" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">InputStream / OutputStream</text>
  <text x="174" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ikili veri: resim, PDF, ağ</text>
  <text x="174" y="120" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">birim: bayt (charset YOK)</text>
  <rect x="336" y="38" width="300" height="96" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="486" y="60" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Karakter akışı</text>
  <text x="486" y="80" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Reader / Writer</text>
  <text x="486" y="100" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">metin; charset (UTF-8) uygular</text>
  <text x="486" y="120" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">birim: karakter</text>
  <rect x="24" y="146" width="612" height="76" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="330" y="168" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B26B00" font-family="system-ui">Buffered: araya tampon (buffer)</text>
  <text x="330" y="190" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tamponsuz: her read() → 1 sistem çağrısı (system call) → yavaş</text>
  <text x="330" y="210" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tamponlu: blok blok oku → system call sayısı azalır → hız katlanır</text>
</svg>
```

### Mülakatta ne sorulur?

- **"`toString` ezilmezse `println(nesne)` ne basar?"** → `SinifAdi@onaltılıkHash`; otomatik alan dökümü yoktur.
- **"JPEG'i hangi akışla okursun?"** → Byte akışı; karakter akışı charset uygulayıp ikili veriyi bozar. `File` tek başına içerik okuyamaz.
- **"Buffered neden hızlı?"** → Sistem çağrısı sayısını azaltır; sıkıştırma veya paralellik değil.
- **"`clone` neden tehlikeli?"** → `Cloneable` yoksa `CloneNotSupportedException`; olsa bile sığ kopya referans alanlarını paylaştırır. Çözüm: derin kopya ya da copy constructor.
- **"`equals`'te `instanceof` mı `getClass()` mü?"** → `getClass()` tam tür eşitliği ve simetri garantisi; `instanceof` esneklik ve null güvenliği. Tersini söylemek (`getClass()` NPE'yi önler) klasik yanlıştır.
- **"`equals`/`hashCode` neden birlikte?"** → Kontrat; kırılırsa hash tabanlı koleksiyonlar sessizce yanlış çalışır.

### Tek cümlede

Her nesne `Object`'ten dört kritik metot miras alır — `toString` okunurluk, `equals`/`hashCode` eşitlik kontratı, `getClass` gerçek tür, `clone` ise iki tuzaklı sığ kopya — ve I/O tarafında kural nettir: metin Reader/Writer, ikili InputStream/OutputStream, hepsi tamponlu.

# obj-1 | Temel
## Soru
Bir sınıf `toString()`'i override ETMEZSE, `System.out.println(nesne)` ne yazdırır?
## A
Nesnenin tüm alanlarını otomatik olarak listeler
🤖 Otomatik alan dökümü yoktur; bunu ancak override ederek sağlarsın.
## B
Boş string basar
🤖 Boş değil; sınıf adı ve hash'in onaltılık hali basılır.
## C
`SinifAdi@onaltılıkHash` biçiminde varsayılan gösterim (ör. `Kisi@1b6d3586`)
🤖 Doğru: `Object.toString()` sınıf adı + `@` + hashCode'un onaltılığını döndürür.
## D
Derleme hatası verir çünkü `toString` tanımlı değildir
🤖 `toString` `Object`'ten miras alınır; her zaman vardır, hata olmaz.
## Doğru
C
## Açıklama
`Object.toString()` varsayılan olarak `getClass().getName() + "@" + Integer.toHexString(hashCode())` döndürür — ör. `Kisi@1b6d3586` (C). Bu insan için anlamsızdır; anlamlı çıktı için sınıf `toString`'i override etmelidir. `println(nesne)` içeride `String.valueOf(nesne)` → `nesne.toString()` çağırır. Alanların otomatik dökümü (A) yoktur; bunu Lombok/IDE ile üretir ya da elle yazarsın.

# obj-2 | Temel
## Soru
Bir JPEG resim dosyasını (ikili veri) okuyup kopyalamak için hangi akış (stream) türü doğrudur?
## A
`InputStream`/`OutputStream` (byte akışı); ikili veri kodlama uygulanmadan işlenmelidir
🤖 Doğru: baytlar olduğu gibi, charset dönüşümü olmadan taşınır.
## B
`Reader`/`Writer` (karakter akışı) çünkü diskteki tüm dosyalar aslında metin olarak saklanır
🤖 Resim metin değildir; karakter akışı charset uygular ve baytları BOZAR.
## C
Fark etmez; JVM akış türünü içeriğe göre seçtiği için ikisi de aynı sonucu verir
🤖 Fark eder; karakter akışı charset dönüşümüyle ikili veriyi bozar.
## D
Yalnız `File` sınıfı; akış açmadan doğrudan dosya kopyalanır
🤖 `File` yalnız yolu/meta bilgiyi temsil eder; içeriği bir akış olmadan okuyamaz.
## Doğru
A
## Açıklama
İkili veri (resim, PDF, ses) **byte akışıyla** (`InputStream`/`OutputStream`) işlenir; baytlar olduğu gibi, kodlama uygulanmadan taşınır (A). `Reader`/`Writer` (karakter akışı) araya bir charset (ör. UTF-8) koyup baytları karaktere çevirir — metin için doğru, ama ikili veride bu dönüşüm baytları sessizce bozar (C yanlış). Kural: metin → Reader/Writer, ikili → InputStream/OutputStream. `File` (D) yalnız yol/meta bilgidir, içerik okumaz.

# obj-3 | Orta
## Soru
Bir dosyayı bayt bayt (`fis.read()`) okumak yerine `BufferedInputStream` ile sarmalamak performansı neden ciddi artırır?
## A
Buffered akış veriyi sıkıştırır (compress); diskten daha az bayt okunduğu için süre kısalır
🤖 Buffer sıkıştırmaz; işletim sistemi çağrısı sayısını azaltır.
## B
Buffered akış veriyi RAM yerine doğrudan diskten okur, ara kopyalama olmadığı için hızlıdır
🤖 Tam tersi; tampon RAM'dedir ve diske gidiş sayısını azaltır.
## C
Buffered akış veriyi paralel iş parçacıklarında okur
🤖 Paralellik yoktur; kazanç toplu (batch) okuma ile system call azaltmadır.
## D
Tampon (buffer) sayesinde veri toplu okunur; sistem çağrısı (system call) sayısı büyük ölçüde azalır
🤖 Doğru: bir blok tampona alınır, sonraki `read()`'ler bellekten karşılanır.
## Doğru
D
## Açıklama
Tamponsuz `read()` her çağrıda bir işletim sistemi çağrısı yapar; bunlar pahalıdır ve bayt bayt okumada milyonlarca kez tekrarlanır. `BufferedInputStream` araya bir bellek tamponu koyar: bir seferde büyük bir blok okur, sonraki `read()`'ler bu tampondan karşılanır — system call sayısı ve dolayısıyla süre dramatik düşer (D). Sıkıştırma (A) ya da paralellik (C) söz konusu değildir. Aynı mantık `BufferedReader`/`BufferedWriter` için de geçerlidir.

# obj-4 | Orta
## Soru
`Takim a`'yı klonlayıp `b = a.clone()` dedikten sonra `b.oyuncular.add("X")` çağrılırsa ne olur?

```java
class Takim implements Cloneable {
    List<String> oyuncular = new ArrayList<>();
    public Takim clone() throws CloneNotSupportedException {
        return (Takim) super.clone();
    }
}
```
## A
Yalnız `b`'nin listesi değişir; `clone()` iç alanları da kopyaladığı için `a` etkilenmez
🤖 `super.clone()` sığ kopyadır; iki nesne AYNI liste nesnesini paylaşır, `a` da değişir.
## B
`a.oyuncular` da değişir; `super.clone()` sığ kopya (shallow copy) yapar, liste paylaşılır
🤖 Doğru: referans alanı kopyalanmadığı için iki nesne aynı listeyi işaret eder.
## C
`CloneNotSupportedException` fırlar; `clone()` her durumda bu kontrollü istisnayı atar
🤖 Sınıf `Cloneable` uyguladığı için istisna fırlamaz; sorun paylaşılan referanstır.
## D
Derleme hatası; `List` gibi generic alanlar klonlanamadığından kod derlenmez
🤖 Derleme sorunu yoktur; sorun runtime'da paylaşılan iç listedir.
## Doğru
B
## Açıklama
`Object.clone()` **sığ kopya** (shallow copy) üretir: ilkel alanları kopyalar ama referans alanlarını (buradaki `oyuncular` listesi) KOPYALAMAZ, aynı nesneyi paylaştırır (B). Sonuç: `b.oyuncular` ile `a.oyuncular` aynı listedir; birine ekleme diğerinde de görünür — sinsi bir hata. Derin kopya (deep copy) için referans alanları elle klonlanmalıdır (ör. `clone.oyuncular = new ArrayList<>(oyuncular)`). clone'un bu tuzakları yüzünden birçok uzman onun yerine kopyalama constructor'ı / factory önerir.

# obj-5 | Orta
## Soru
`Object.clone()` bir nesne üzerinde `CloneNotSupportedException` fırlatırsa en olası sebep nedir?
## A
Sınıf `Cloneable` işaretleyici arayüzünü (marker interface) uygulamamış
🤖 Doğru: `Cloneable` yoksa `Object.clone()` bu istisnayı fırlatır.
## B
Sınıfın `equals` metodu yok
🤖 `equals` ile `clone` ilgisizdir; sorun `Cloneable` arayüzünün eksikliğidir.
## C
Sınıf `final` olarak işaretlenmiş
🤖 `final` sınıflar da klonlanabilir; kısıt `Cloneable` ile ilgilidir.
## D
Nesnenin hiç alanı yok
🤖 Alansız nesne de klonlanır; sorun `Cloneable` eksikliğidir.
## Doğru
A
## Açıklama
`Cloneable` metotsuz bir **işaretleyici arayüzdür** (marker interface): bir sınıf onu uygulamazsa `Object.clone()` `CloneNotSupportedException` fırlatır (A). Bu, clone API'sinin garip tasarımının bir parçasıdır — `clone` metodu `Object`'te bulunmasına rağmen davranışı ayrı bir arayüzün varlığına bağlıdır. `equals` (B), `final` (C) ya da alan sayısı (D) ile ilgisi yoktur. Bu tuhaflıklar, clone yerine kopyalama constructor'ı tercih edilmesinin bir başka nedenidir.

# obj-6 | İleri
## Soru
Bir `equals` uygularken tür kontrolü için `instanceof` yerine `getClass() != o.getClass()` kullanmanın ana sonucu nedir?
## A
Hiçbir fark yoktur; ikisi de aynı tür kontrolünü yapar, yalnız yazımları farklıdır
🤖 Alt sınıflar söz konusu olunca davranış ayrışır; fark simetri ve miras ilişkisindedir.
## B
`getClass()` `NullPointerException` riskini ortadan kaldırır, `instanceof` kaldırmaz
🤖 Aksine `instanceof` null için `false` döner (null-güvenli); `getClass()` null argümanda NPE riski taşır.
## C
`getClass()` tam tür eşitliği ister, alt sınıfı üst sınıfa asla eşit saymaz; `instanceof` alt sınıfı da kabul eder
🤖 Doğru: `getClass()` katı tam-tür eşitliği, `instanceof` daha esnek alt-tür kabulü verir.
## D
`instanceof` derleme-zamanı, `getClass()` çalışma-zamanı kontrolüdür; sonuç değişmez
🤖 İkisi de runtime tür kontrolüdür; asıl fark alt sınıf davranışıdır.
## Doğru
C
## Açıklama
`getClass() != o.getClass()` **tam tür eşitliği** ister: `Nokta` ile onun alt sınıfı `RenkliNokta` asla eşit olamaz. Bu, `equals`'in simetri kuralını (`a.equals(b) == b.equals(a)`) alt sınıflar arasında bozmadan korur ama katıdır (C). `instanceof` ise alt sınıfı da kabul eder — daha esnek, ama dikkatsiz miras kullanımında simetri ihlali doğurabilir. Ayrıca `o instanceof T` null için sessizce `false` döner (null-güvenli); `getClass()` yaklaşımında argüman null olabileceğinden önce null kontrolü gerekir (B tersine yanlış). Effective Java bu ayrımı klasik bir tasarım tartışması olarak ele alır.
