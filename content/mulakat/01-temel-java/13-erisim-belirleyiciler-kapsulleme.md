---
slug: erisim-belirleyiciler-kapsulleme
baslik: "Erişim Belirleyiciler ve Kapsülleme: private'dan public'e, Immutability"
kademe: Temel
sira: 13
etiketler: 2
---

## Konu anlatımı

### Bu ne işe yarar?

Bir evin odalarını düşün: yatak odana kimse giremez, salona ev halkı girer, bahçeye komşular da uğrar, kapının önü herkese açıktır. Erişim belirleyiciler (access modifier) kodda bu kapıları koyar. Çözdükleri problem şudur: bir sınıfın iç ayrıntısına herkes dokunabiliyorsa, o sınıf kendi kurallarını koruyamaz ve içindeki hiçbir şeyi bir daha rahatça değiştiremezsin — kimin neye bağlandığını bilemezsin. Kapıları daraltmak hem veriyi korur hem de sana sonradan değiştirme özgürlüğü bırakır.

### Temel kavramlar

Önce iki kelime: **paket (package)**, birbiriyle ilgili sınıfları toplayan klasör benzeri gruptur. **Üye (member)** ise bir sınıfın alanı ya da metodudur.

Java'da dört erişim düzeyi vardır — en dardan en genişe:

- **`private`**: Yalnız aynı **sınıf** içinden erişilir. Kapsüllemenin temel taşı.
- **paket-özel / package-private**: Yalnız aynı **paket** içinden. Buna "default" erişim de denir ama `default` diye bir anahtar kelime **yazılmaz**; belirtecin **yokluğu** bu düzeyi ifade eder (`int x;`). "Belirteç yok" ile `public` aynı şey **değildir**; `private` ya da `protected` de değildir. Başka paketler, alt sınıf olsalar bile erişemez.
- **`protected`**: Aynı paketteki tüm kod (package-private gibi) **artı farklı paketteki alt sınıflar** — ama yalnız **kalıtım yoluyla**, yani kendi devraldıkları üye üzerinden. Başka bir nesnenin `protected` üyesine erişemezler. Aynı paketten de olmayan, alt sınıf da olmayan yabancı kod erişemez; yani `protected`, `public` **değildir**. Bu, "alt sınıflara açık ama genel API'nin parçası değil" ara düzeyidir ve framework'lerin genişletme (extension) noktalarında sık kullanılır.
- **`public`**: Her yerden.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Erisim duzeyleri gorunurluk">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Erişim düzeyleri: dardan genişe</text>
  <rect x="30" y="44" width="580" height="34" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="66" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">private → yalnız aynı sınıf</text>
  <rect x="30" y="84" width="580" height="34" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="106" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">(belirteç yok) package-private → aynı paket</text>
  <rect x="30" y="124" width="580" height="34" rx="7" fill="#FBF0E6" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="146" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">protected → aynı paket + alt sınıflar (farklı pakette bile)</text>
  <rect x="30" y="164" width="580" height="34" rx="7" fill="#F3ECF7" stroke="#7A3E9D" stroke-width="1.5"/>
  <text x="320" y="186" text-anchor="middle" font-size="12" fill="#1C3247" font-family="system-ui">public → her yerden</text>
</svg>
```

```svg
<svg viewBox="0 0 640 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Erisim gorunurluk matrisi">
  <text x="320" y="20" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Görünürlük matrisi: erişim düzeyi nereden görünür?</text>
  <rect x="18" y="32" width="604" height="30" rx="5" fill="#1C3247"/>
  <text x="100" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Erişim düzeyi</text>
  <text x="237" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Aynı sınıf</text>
  <text x="347" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Aynı paket</text>
  <text x="457" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Alt sınıf*</text>
  <text x="567" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#FFFFFF" font-family="system-ui">Dünya</text>
  <rect x="18" y="64" width="604" height="34" fill="#F4F8FB"/>
  <rect x="18" y="98" width="604" height="34" fill="#EAF1F7"/>
  <rect x="18" y="132" width="604" height="34" fill="#F4F8FB"/>
  <rect x="18" y="166" width="604" height="34" fill="#EAF1F7"/>
  <path d="M182,32 L182,200" stroke="#CBD8E2" stroke-width="1"/>
  <path d="M292,32 L292,200" stroke="#CBD8E2" stroke-width="1"/>
  <path d="M402,32 L402,200" stroke="#CBD8E2" stroke-width="1"/>
  <path d="M512,32 L512,200" stroke="#CBD8E2" stroke-width="1"/>
  <text x="100" y="86" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">private</text>
  <text x="237" y="86" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="347" y="86" text-anchor="middle" font-size="13" fill="#9AA8B2" font-family="system-ui">✗</text>
  <text x="457" y="86" text-anchor="middle" font-size="13" fill="#9AA8B2" font-family="system-ui">✗</text>
  <text x="567" y="86" text-anchor="middle" font-size="13" fill="#9AA8B2" font-family="system-ui">✗</text>
  <text x="100" y="120" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">(yok) package-private</text>
  <text x="237" y="120" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="347" y="120" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="457" y="120" text-anchor="middle" font-size="13" fill="#9AA8B2" font-family="system-ui">✗</text>
  <text x="567" y="120" text-anchor="middle" font-size="13" fill="#9AA8B2" font-family="system-ui">✗</text>
  <text x="100" y="154" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">protected</text>
  <text x="237" y="154" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="347" y="154" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="457" y="154" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="567" y="154" text-anchor="middle" font-size="13" fill="#9AA8B2" font-family="system-ui">✗</text>
  <text x="100" y="188" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">public</text>
  <text x="237" y="188" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="347" y="188" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="457" y="188" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="567" y="188" text-anchor="middle" font-size="14" font-weight="700" fill="#25804C" font-family="system-ui">✓</text>
  <text x="320" y="222" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">* farklı paketteki alt sınıf, yalnız kalıtımla devraldığı üye üzerinden</text>
</svg>
```

**Kapsülleme (encapsulation)** bu araçlarla yapılır: alanları `private` yapıp erişimi kontrollü metotlarla (getter/setter ya da iş metotları) sağlamak. Amaç yalnız veriyi saklamak değildir; iki asıl hedefi vardır. (1) Sınıf **kendi değişmezlerini (invariant) koruyabilir** — geçersiz durumları metot içinde reddedersin, dışarıdan kimse alanı bozamaz. (2) **İç temsili özgürce değiştirebilirsin** (bilgi gizleme / information hiding) — çağıran kod yalnız arayüze bağımlı olduğu için, verinin nasıl saklandığını değiştirmen dış dünyayı bozmaz. Kapsülleme bir şifreleme yöntemi **değildir** (derlenmiş sınıfı okunamaz hale getirmez) ve bir performans tekniği de **değildir**; "bir sınıftan çok sayıda bağımsız nesne üretebilmek" ise ayrı bir kavramdır (sınıf/nesne ilişkisi).

### Nasıl çalışır?

**Getter/setter otomatizmi kapsülleme değildir.** Her alana düşünmeden getter ve sınırsız setter üretirsen okuma da yazma da tümüyle açılır. Bu, alanı `public` yapmaya yakınsar. Setter her değeri kabul ettiği için değişmez koruması kalmaz. Çağıran kod alanın adına ve tipine bağlandığı için iç temsili değiştirme özgürlüğün de kalkar. Sorun performans değildir — JIT bu çağrıları zaten satır içine (inline) alır; Java `private` alana getter/setter yazmayı elbette destekler, hatta olağan kullanım budur. Gerçek kapsülleme şu soruyla başlar: "**bu davranış dışarıya gerçekten gerekli mi?**" Belki setter hiç olmamalı (değişmez nesne); belki setter yerine doğrulamalı bir iş metodu olmalı (`hesap.paraCek(x)`); belki getter kopyalanmış ya da salt-okunur veri döndürmeli.

**Klasik ihlal: değiştirilebilir (mutable) iç nesneyi sızdırmak.**

```java
class Takim {
    private final List<String> uyeler;
    Takim(List<String> uyeler) { this.uyeler = uyeler; }
    public List<String> getUyeler() { return uyeler; }   // iç listeyi doğrudan döndürüyor
}
```

`private final` yalnız **referansın** değişmezliğini sağlar — listenin *hangi* nesne olduğunu; listenin **içeriğini** korumaz. Getter iç listenin doğrudan referansını döndürdüğü için çağıran `takim.getUyeler().add("davetsiz")` diyerek nesnenin iç durumunu dışarıdan bozabilir (mutable state leak). Alanı `public final` yapmak sızıntıyı büsbütün kötüleştirir; getter'ı `static` yapmak da ilgisizdir — sorun üye/statik ayrımı değil, **referans paylaşımıdır**. Doğru çözümler:

- **savunmacı kopya (defensive copy)**: `return new ArrayList<>(uyeler);` — çağırana kopya gider, iç liste korunur.
- **salt-okunur görünüm**: `return Collections.unmodifiableList(uyeler);` — değiştirme denemesi istisna atar.

Constructor da gelen listeyi savunmacı kopyalamalıdır (`this.uyeler = new ArrayList<>(uyeler);`), yoksa dışarıda kalan referansla iç durum yine değiştirilebilir.

### Mülakatta ne sorulur?

- **Değişmez (immutable) sınıf reçetesi ve "hangisi gerekli değil?" sorusu.** Gerekli olanlar dört tanedir. (1) Tüm alanlar **`private final`** olmalı. (2) **Hiçbir mutasyon metodu / setter bulunmamalı.** (3) Alt sınıflama engellenmeli: sınıf `final` olmalı ya da tüm constructor'lar gizlenmeli. Yoksa bir alt sınıf değiştirilebilir davranış ekleyip sözleşmeyi kırar. (4) Değiştirilebilir (mutable) alanlar için constructor'da **ve** getter'da savunmacı kopya alınmalı. **Gerekli olmayan** ise durumu okuyan metotları `synchronized` yapmaktır. Gerçekten değişmez bir nesnenin durumu kurulduktan sonra hiç değişmez; bu yüzden nesne **doğası gereği thread-safe**'tir, yani iş parçacığı güvenlidir. Birden çok iş parçacığı (thread) onu kilitsiz, güvenle paylaşabilir. Bu, immutability'nin en büyük pratik faydası ve kapsüllemenin en güçlü biçimidir; `String`, `Integer`, `LocalDate` böyle tasarlanmıştır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Degismez nesne recetesi">
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Değişmez (immutable) nesne reçetesi</text>
  <rect x="30" y="36" width="580" height="30" rx="7" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="56" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">1) tüm alanlar private final</text>
  <rect x="30" y="72" width="580" height="30" rx="7" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="92" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">2) setter / mutasyon metodu YOK</text>
  <rect x="30" y="108" width="580" height="30" rx="7" fill="#FBEEDC" stroke="#B4690E" stroke-width="1.5"/>
  <text x="320" y="128" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">3) final class (alt sınıf değişmezliği bozamaz)</text>
  <rect x="30" y="144" width="580" height="30" rx="7" fill="#F3ECF7" stroke="#7A3E9D" stroke-width="1.5"/>
  <text x="320" y="164" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">4) mutable alanlarda savunmacı kopya (ctor + getter)</text>
  <rect x="90" y="180" width="460" height="26" rx="7" fill="#1C3247"/>
  <text x="320" y="198" text-anchor="middle" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Sonuç: doğası gereği thread-safe → synchronized GEREKMEZ</text>
</svg>
```

- **`record` (Java 16+)** bu reçetenin çoğunu hazır verir: `private final` alanlar, setter yok, örtük `final` sınıf. Ama bir bileşen `List` gibi değiştirilebilir bir tipse savunmacı kopyayı yine **sen** yazmalısın.
- **Görünürlük ayrıntıları.** Belirteç yokluğu = paket-özel; `protected` farklı paketteki alt sınıfı kapsar ama yabancı sınıfı kapsamaz ve `public` ile birebir aynı değildir. Sıra: `private` (sınıf) < package-private (paket) < `protected` (paket + alt sınıflar) < `public` (her yer).
- **Kapsüllemenin tanımı sorulduğunda** doğru cevap "veriyi `private` yapıp kontrollü arayüzle değişmezleri korumak ve iç temsili özgürce değiştirebilmek"tir; şifreleme, hız ya da "çok nesne üretmek" çeldiricidir.

### Tek cümlede

Erişim belirleyiciler bir üyenin nereden görülebileceğini dardan genişe belirler (`private` → paket-özel → `protected` → `public`); kapsülleme ise bu kapıları dar tutup sınıfın değişmezlerini koruma ve iç temsilini serbestçe değiştirebilme disiplinidir. En güçlü biçimi de değişmez (immutable) nesnedir.

# acc-1 | Temel
## Soru
Bir üye (member) için **hiçbir** erişim belirleyici yazılmazsa (`int x;`) erişim düzeyi ne olur?
## A
`public` (her yerden erişilebilir)
🤖 Varsayılan public değildir; belirteç yokluğu paket düzeyi erişim demektir.
## B
`private` (yalnız aynı sınıf)
🤖 Varsayılan private de değildir; paket-özel private'tan daha geniştir.
## C
Paket-özel / package-private (yalnız aynı paketten erişilir)
🤖 Doğru: belirteç yazılmaması "default" yani paket-özel erişim anlamına gelir.
## D
`protected` (paket + alt sınıflar)
🤖 protected'ı almak için açıkça yazmak gerekir; varsayılan bu değildir.
## Doğru
C
## Açıklama
Java'da erişim belirleyici **yazılmadığında**, üye **paket-özel (package-private)** olur — buna "default" erişim de denir ama `default` diye bir anahtar kelime YAZILMAZ; belirtecin **yokluğu** bu düzeyi ifade eder. Bu düzeyde üyeye **yalnız aynı paketteki** kod erişebilir; başka paketler (alt sınıf olsalar bile) erişemez. Görünürlük sırası: `private` (sınıf) < package-private (paket) < `protected` (paket + alt sınıflar) < `public` (her yer). Bu, mülakatta sık atlanan bir ayrımdır: "belirteç yok" ile `public` aynı şey DEĞİLDİR.

# acc-2 | Temel
## Soru
`protected` bir üyeye, tanımlandığı sınıfın **başka bir paketteki** alt sınıfından erişilebilir mi?
## A
Hayır; `protected` yalnız aynı paketle sınırlıdır, alt sınıf olsa bile fark etmez
🤖 Bu package-private tanımı; protected buna ek olarak farklı paketteki alt sınıfları da kapsar.
## B
Hayır; `protected` yalnız aynı sınıftan erişilir
🤖 Bu private tanımı; protected çok daha geniştir.
## C
Evet; `protected` aynı pakete ek olarak farklı paketteki alt sınıflardan da erişilebilir
🤖 Doğru: protected'ın package-private'tan farkı tam olarak budur.
## D
Evet; `protected` `public` ile birebir aynıdır
🤖 protected public değildir; alt sınıf olmayan yabancı paket kodu erişemez.
## Doğru
C
## Açıklama
`protected`, iki grup erişime izin verir: (1) **aynı paketteki** tüm kod (package-private gibi) ve (2) **farklı paketteki alt sınıflar**, ancak yalnız **kalıtım yoluyla** (kendi devraldıkları üye üzerinden). Yani başka paketteki bir alt sınıf, üst sınıftan devraldığı `protected` üyeye kendi içinden erişebilir; ama aynı paketten olmayan, alt sınıf da olmayan yabancı bir sınıf erişemez (D yanlış — `public` değildir). Bu, "alt sınıflara açık ama genel API'nin parçası değil" ara düzeyidir; framework'lerin genişletme (extension) noktalarında sık kullanılır. Not: farklı paketteki alt sınıf, başka bir nesnenin protected üyesine değil, yalnız kendi devraldığı üyeye erişebilir.

# acc-3 | Temel
## Soru
Aşağıdakilerden hangisi kapsüllemenin (encapsulation) asıl amacını en doğru anlatır?
## A
İç veriyi `private` yapıp kontrollü arayüz sunarak sınıfın değişmezlerini (invariant) korumak
🤖 Doğru: veri gizleme + kontrollü erişim + iç detayı değiştirebilme özgürlüğü.
## B
Bir sınıftan, her biri kendi durumunu taşıyan çok sayıda bağımsız nesne üretebilmek
🤖 Bu sınıf/nesne kavramıyla ilgilidir, kapsüllemeyle değil.
## C
Alan erişimini metot çağrısına dönüştürerek programın daha hızlı çalışmasını sağlamak
🤖 Kapsülleme bir performans tekniği değildir; tasarım ve bakım kolaylığı sağlar.
## D
Kodu şifreleyip derlenmiş sınıfın içeriğini dışarıdan okunamaz hale getirmek
🤖 Kapsülleme şifreleme değildir; erişim kontrolü ve değişmez korumasıyla ilgilidir.
## Doğru
A
## Açıklama
Kapsülleme, veriyi (`private` alanlar) ve onu işleyen davranışı bir sınıfta toplayıp dışarıya **kontrollü bir arayüz** (metotlar) sunmaktır. İki kritik faydası: (1) Sınıf **kendi değişmezlerini (invariant) koruyabilir** — geçersiz durumları metot içinde reddedersin, dışarıdan kimse alanı bozamaz. (2) İç temsili **özgürce değiştirebilirsin** (bilgi gizleme / information hiding) — çağıran kod yalnız arayüze bağımlı olduğu için, alanların nasıl saklandığını değiştirmen dış dünyayı bozmaz. Şifreleme (D) veya performans (C) ile ilgisi yoktur.

# acc-4 | Temel
## Soru
Her `private` alana otomatik olarak hem getter hem setter üretmek, çoğu zaman neden gerçek kapsülleme sağlamaz?
## A
Getter/setter çağrıları doğrudan alan erişiminden yavaştır ve gereksiz yük getirir
🤖 Sorun performans değil, tasarımsaldır; JIT bu çağrıları zaten satır içine (inline) alır.
## B
Denetimsiz bir setter alanı okuma+yazmaya açar, `public` yapmaya yakınsar; değişmez koruması ve bilgi gizleme kalmaz
🤖 Doğru: kontrolsüz setter, private'ın koruyucu değerini büyük ölçüde iptal eder.
## C
Java, `private` alanlar için getter/setter yazılmasına izin vermez
🤖 Java izin verir, hatta olağan kullanım budur; sorun izin değil, tasarım disiplinidir.
## D
Getter/setter yalnız `public` alanlar için yazılabilir; `private` alanlara erişemez
🤖 Getter/setter sınıfın kendi içinde tanımlandığı için private alana erişir; bu ifade yanlıştır.
## Doğru
B
## Açıklama
Kapsülleme "alanı `private` yap + mekanik getter/setter üret" değildir. Bir alana **hem getter hem sınırsız setter** koyarsan, dışarıdan okuma ve yazma tümüyle açılır — bu, alanı büyük ölçüde `public` yapmaya yakınsar: değişmez koruması yoktur (setter her değeri kabul eder), iç temsili değiştirme özgürlüğün kalkar (çağıranlar alanın adına/tipine bağlanır). Gerçek kapsülleme, "**bu davranış dışarıya gerçekten gerekli mi?**" sorusuyla başlar: belki setter hiç olmamalı (değişmez nesne), belki setter yerine anlamlı bir iş metodu olmalı (`hesap.paraCek(x)` gibi, doğrulamalı), belki getter kopyalanmış/salt-okunur veri döndürmeli. Getter/setter bir araçtır, kapsüllemenin kendisi değil.

# acc-5 | Orta
## Soru
Aşağıdaki sınıf kapsüllemeyi nasıl ihlal ediyor ve doğru çözüm nedir?

```java
class Takim {
    private final List<String> uyeler;
    Takim(List<String> uyeler) { this.uyeler = uyeler; }
    public List<String> getUyeler() { return uyeler; }   // iç listeyi doğrudan döndürüyor
}
```
## A
İhlal yok; alan `private final` olduğundan listenin içeriği de dışarıdan değiştirilemez
🤖 `final` yalnız referansın değişmezliğini garanti eder; listenin İÇERİĞİ hâlâ dışarıdan değiştirilebilir.
## B
Getter iç `List` referansını sızdırıyor; savunmacı kopya (defensive copy) ya da salt-okunur görünüm döndürülmeli
🤖 Doğru: değiştirilebilir (mutable) iç nesnenin referansını sızdırmak kapsüllemeyi kırar.
## C
Alanı `public final` yapmak sorunu çözer; getter'a hiç gerek kalmaz
🤖 Aksine; public yapmak sızıntıyı kötüleştirir, kalan korumayı da büsbütün kaldırır.
## D
`getUyeler` metodunu `static` yapmak gerekir; böylece nesnenin durumuna bağlı kalmaz
🤖 static yapmak sızıntıyı çözmez; sorun referans paylaşımıdır, üye/statik ayrımı değil.
## Doğru
B
## Açıklama
`private final List<String> uyeler` yalnız **referansın** değişmezliğini sağlar — listenin **hangi** nesne olduğunu; ama listenin **içeriğini** korumaz. `getUyeler()` bu iç listenin **doğrudan referansını** döndürdüğü için çağıran `takim.getUyeler().add("davetsiz")` diyerek nesnenin iç durumunu **dışarıdan bozabilir**; kapsülleme kırılmıştır (mutable state leak). Doğru çözümler: (1) **savunmacı kopya** döndürmek — `return new ArrayList<>(uyeler);` çağırana kopyayı verir, iç liste korunur; (2) **salt-okunur görünüm** döndürmek — `return Collections.unmodifiableList(uyeler);` değiştirme denemesinde istisna atar. Ayrıca constructor da gelen listeyi savunmacı kopyalamalı (`this.uyeler = new ArrayList<>(uyeler);`), yoksa dışarıdaki referansla iç durum yine değiştirilebilir.

# acc-6 | Orta
## Soru
Değişmez (immutable) bir Java sınıfı tasarlarken aşağıdakilerden hangisi **gerekli değildir**?
## A
Tüm alanları `private final` yapmak
🤖 Bu gereklidir; final + private, kurulduktan sonra alanların değişmemesini ve dışa kapalılığı sağlar.
## B
Setter metotları koymamak (durumu değiştiren hiçbir metot sunmamak)
🤖 Bu gereklidir; herhangi bir mutasyon metodu değişmezliği bozar.
## C
Alt sınıflamayı engellemek: sınıfı `final` yapmak ya da tüm constructor'ları gizlemek
🤖 Bu gereklidir; engellenmezse bir alt sınıf değiştirilebilir davranış ekleyip sözleşmeyi kırabilir.
## D
Durumu okuyan tüm metotları `synchronized` yaparak eşzamanlı erişimi kilitlemek
🤖 Doğru (gereksiz olan bu): gerçekten değişmez nesne zaten thread-safe'tir; senkronizasyona gerek yoktur.
## Doğru
D
## Açıklama
Değişmez (immutable) sınıf reçetesi: (A) tüm alanlar **`private final`**; (B) **hiçbir mutasyon metodu (setter) yok**; (C) sınıf **`final`** (veya constructor'lar gizli) ki alt sınıf mutable davranış ekleyip değişmezliği bozamasın; ve ek olarak **mutable alanlar için constructor'da ve getter'larda savunmacı kopya**. Bunların hepsi gereklidir. **Gerekli olmayan** ise (D) metotları `synchronized` yapmaktır: gerçekten değişmez bir nesnenin durumu kurulduktan sonra hiç değişmediği için **doğası gereği thread-safe'tir** — birden çok iş parçacığı (thread) onu güvenle, kilitsiz paylaşabilir. Bu, immutability'nin en büyük pratik faydalarından biridir ve kapsüllemenin en güçlü biçimidir: durum ne dışarıdan ne de eşzamanlılıkla bozulabilir (`String`, `Integer`, `LocalDate` böyle tasarlanmıştır).
