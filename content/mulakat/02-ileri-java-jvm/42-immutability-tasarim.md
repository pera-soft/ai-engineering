---
slug: immutability-tasarim
baslik: "Değişmezlik Tasarımı: Immutable Sınıflar ve Defensive Copy"
kademe: Orta
sira: 42
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir kitabın basılı sayfasını düşün: bir kez basıldıktan sonra kimse üzerindeki cümleyi değiştiremez. Aynı sayfayı on kişiye aynı anda okutabilirsin; kimse kimsenin okuduğunu bozamaz, sıraya girmeleri de gerekmez. Yazı tahtası ise tam tersidir: herkes silip yeniden yazabilir, aynı anda iki kişi yazarsa ortaya karışık bir şey çıkar. **Değişmez (immutable) nesne**, basılı sayfadır: oluşturulduktan sonra durumu HİÇ değişmez. Bu tek karar, eşzamanlılık hatalarının büyük kısmını daha tasarım aşamasında ortadan kaldırır.

### Temel kavramlar

- **Durum (state):** nesnenin alanlarında tuttuğu değerlerin bütünü.
- **Değişmezlik (immutability):** nesne kurulduktan sonra durumunun bir daha değişmemesi.
- **`final` alan:** yalnız **referansın yeniden atanmasını** engeller. İşaret ettiği nesnenin İÇİ değişebilir — bu ayrımı kaçırmak en sık yapılan hatadır.
- **Sığ (shallow) değişmezlik:** referans kilitli, içerik serbest. `private final List liste` alanına yeniden atama derleme hatasıdır ama `liste.add(x)` hâlâ mümkündür. Record'un varsayılanı budur.
- **Derin (deep) değişmezlik:** içerik de kilitli. `List.copyOf(liste)` ile saklanan listeye `add` denenirse `UnsupportedOperationException` gelir.
- **Savunmacı kopya (defensive copy):** dışarıdan gelen ya da dışarıya verilen değiştirilebilir (mutable) nesnenin aslı yerine bir kopyasının kullanılması.
- **Thread-safety:** birden çok iş parçacığı aynı nesneye eriştiğinde yanlış sonuç oluşmaması.

Değişmez sınıfın klasik reçetesi beş maddedir:

1. Sınıfı `final` yap (bir alt sınıf metotları geçersiz kılıp değişmezlik yanılsaması yaratmasın).
2. Tüm alanları `private final` yap. `public` alan hem kapsüllemeyi kırar hem de tip mutable ise içeriğini dış müdahaleye açar.
3. **Setter koyma**; durumu değiştiren hiçbir metot olmasın.
4. Kurucuda (constructor), dışarıdan gelen **değiştirilebilir** referansları (dizi, `List`, `Date`) **defensive copy** ile kopyala.
5. Getter'larda da değiştirilebilir alanların **kopyasını** ya da salt-okunur görünümünü döndür (iç referansı sızdırma).

Not: sınıfı `abstract` yapmak değişmezliği sağlamaz — tam tersine değişmez sınıflar tipik olarak `final`'dır.

```svg
<svg viewBox="0 0 640 230" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="değişmez sınıf reçetesi beş kural">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Değişmez sınıf reçetesi (5 kural)</text>
  <rect x="60" y="44" width="520" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="64" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">1 · sınıfı final yap (alt sınıf bozmasın)</text>
  <rect x="60" y="80" width="520" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="100" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">2 · tüm alanlar private final</text>
  <rect x="60" y="116" width="520" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="136" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">3 · setter YOK; durumu değiştiren metot yok</text>
  <rect x="60" y="152" width="520" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="172" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">4 · kurucuda mutable girdi → defensive copy (giriş)</text>
  <rect x="60" y="188" width="520" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="320" y="208" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">5 · getter'da kopya / salt-okunur görünüm (çıkış)</text>
</svg>
```

### Nasıl çalışır?

Kritik nokta 4. ve 5. maddedir, çünkü `final` yalnız referansı kilitler. Kurucuya gelen mutable bir `Date`/`List` doğrudan alana atanırsa, **çağıran taraf da hâlâ aynı nesneye referans tutar** ve onu sonradan değiştirebilir (`date.setTime(...)`, `list.add(...)`). Böylece "değişmez" sanılan nesnenin durumu dışarıdan değişir. Bu bir derleme hatası değildir — sessizce olur. Java **otomatik derin kopya yapmaz**; kopyalamayı sen açıkça yapmalısın: `new ArrayList<>(list)`, `List.copyOf(list)`, `new Date(d.getTime())`.

Aynı sızıntı çıkış tarafında da olur: getter içteki mutable `List` referansını doğrudan döndürürse, çağıran `list.add(...)` / `list.clear()` ile iç durumu değiştirir. Çözüm ya bir **kopya** döndürmek (`new ArrayList<>(ic)`) ya bir **değiştirilemez görünüm** vermek (`Collections.unmodifiableList(ic)`) ya da alanı en baştan `List.copyOf` ile değişmez tutmaktır. Getter'ı `synchronized` yapmak (bu bir görünürlük/kilit meselesidir) ya da alanı `static` yapmak bu sızıntıyı **çözmez**.

```java
public final class Rezervasyon {
    private final String ad;
    private final List<String> misafirler;      // mutable tip
    public Rezervasyon(String ad, List<String> misafirler) {
        this.ad = ad;
        this.misafirler = List.copyOf(misafirler);   // defensive copy (değişmez kopya)
    }
    public List<String> getMisafirler() {
        return misafirler;                            // zaten değişmez → sızıntı yok
    }
}
```

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="defensive copy ile referans sızıntısını engelleme">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Defensive Copy: mutable alanı iki uçta da kopyala</text>
  <rect x="30" y="48" width="270" height="150" rx="12" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="165" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#B4322B" font-family="system-ui">KOPYA YOK → sızıntı</text>
  <text x="165" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">this.tarih = disaridanGelenTarih</text>
  <text x="165" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çağıran aynı Date'i tutar</text>
  <text x="165" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">.setTime(...) ile içi değişir</text>
  <text x="165" y="176" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">değişmezlik BOZULUR</text>
  <rect x="340" y="48" width="270" height="150" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="74" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">KOPYA VAR → güvenli</text>
  <text x="475" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ctor: this.tarih = new Date(g.getTime())</text>
  <text x="475" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">getter: return new Date(tarih.getTime())</text>
  <text x="475" y="150" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">iç referans hiç sızmaz</text>
  <text x="475" y="176" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">değişmezlik KORUNUR</text>
</svg>
```

`record` bu tabloda özel bir yerdedir: bileşenlerini `private final` yaptığı için **sığ** değişmezlik verir. Bileşen mutable bir nesneyse (`List`, `byte[]`) içi hâlâ değişebilir; record gelen bileşeni **kopyalamaz**, erişimci de her çağrıda kopya döndürmez — iç referansı doğrudan verir. Yani `new Kayit(list)` sonrası dışarıdan `list.add(...)` record'un görünen durumunu değiştirir. Mutable bileşen tutmak derleme hatası değildir; derin değişmezlik istiyorsan compact constructor'da defensive copy yapmalısın (`Kayit { veri = List.copyOf(veri); }`) ve gerekiyorsa erişimciyi override edip kopya döndürmelisin.

```svg
<svg viewBox="0 0 640 205" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="sığ ve derin değişmezlik farkı">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">final: referansı kilitler, İÇERİĞİ kilitlemez</text>
  <rect x="30" y="46" width="275" height="140" rx="12" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="167" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">Sığ (shallow)</text>
  <text x="167" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">private final List liste</text>
  <text x="167" y="120" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">liste = ... → derleme hatası</text>
  <text x="167" y="142" text-anchor="middle" font-size="10.5" fill="#8A2E2E" font-family="system-ui">liste.add(x) → HÂLÂ mümkün</text>
  <text x="167" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">record varsayılanı budur</text>
  <rect x="335" y="46" width="275" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="472" y="72" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Derin (deep)</text>
  <text x="472" y="98" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">List.copyOf(liste) sakla</text>
  <text x="472" y="120" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">liste.add(x) → UnsupportedOp</text>
  <text x="472" y="142" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">içerik de kilitli</text>
  <text x="472" y="170" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">defensive copy ile elde edilir</text>
</svg>
```

Değişmezliğin en büyük kazancı **thread-safety**'dir: durum hiç değişmediği için yarış koşulu (race condition), kayıp güncelleme, görünürlük sorunu ya da kilit (lock) **gerekmez**; nesne doğası gereği paylaşıma güvenlidir. Buna Java Bellek Modeli'nin (JMM) `final` alanlara verdiği ek güvence eşlik eder: **kurucu düzgün tamamlandıysa, diğer iş parçacıkları `final` alanların doğru kurulmuş değerlerini `volatile` ya da senkronizasyon olmadan da görür.** Dikkat edilecek bir nokta var: değişmezlik çöp toplayıcıyı (GC) durdurmaz ve paralel yürütme başlatmaz. "Her yazmayı anında yayar" demek de anlamsızdır; zaten yazma yoktur.

Alan sayısı arttığında ve bazıları isteğe bağlı (optional) olduğunda kurulum okunmaz hale gelir. Her kombinasyon için ayrı kurucu yazmak (**telescoping constructor**) bir çözüm değildir. `new X(a, null, null, 0, false)` gibi çağrılar da hangi argümanın ne olduğunu gizler. Bu durumda **builder deseni** kullanılır. Değerler akıcı (fluent) metotlarla (`.ad("...").yas(30)`) bir builder nesnesinde toplanır. Doğrulama `build()` içinde tek noktada yapılır ve `build()` çağrısı değişmez nesneyi **tek seferde** üretir. Böylece hedef nesne hiçbir zaman yarı kurulmuş, tutarsız bir durumda görünmez. Alternatifler kötüdür: argümansız kurucu + setter'larla doldurmak değişmezliği bozar ve nesneyi bir süre tutarsız bırakır; alanları `public` yapmak hem kapsüllemeyi hem değişmezliği yıkar.

### Mülakatta ne sorulur?

**"Bir sınıfı değişmez yapmak için ne gerekir?"** — `private final` alanlar + setter yokluğu + `final` sınıf; üstüne mutable alanlar için defensive copy.

**"`final` yeter mi?"** — Hayır. `final` referansı kilitler, içeriği değil. Sığ/derin değişmezlik ayrımını söyleyebilmek beklenir.

**"Defensive copy'yi nerede yaparsın?"** — İki uçta: kurucuda (giriş) ve getter'da (çıkış). `synchronized` ya da `static` bu sorunu çözmez.

**"Record değişmez değil mi?"** — Sığ olarak evet; mutable bileşende derin değişmezlik için compact constructor'da kopya şart.

**"Eşzamanlılıkta ne kazandırır?"** — Doğal thread-safety: yarış koşulu, görünürlük sorunu ve kilit gereği ortadan kalkar. Ek olarak JMM'nin `final` alan güvencesi: kurucu düzgün bittiyse diğer thread'ler doğru değerleri `volatile` olmadan görür.

**"Çok alanlı, opsiyonelli değişmez nesneyi nasıl kurarsın?"** — Builder deseni; telescoping constructor değil.

### Tek cümlede

Değişmezlik, durumu kurulumdan sonra hiç değiştirmeyerek nesneyi kilitsiz paylaşılabilir kılan bir tasarım kararıdır; bedeli `final` alanların yanında **her iki uçta defensive copy** yapmak, karmaşık kurulumun çözümü ise **builder** desenidir.

# imm-1 | Temel
## Soru
Bir sınıfı değişmez (immutable) yapmak için aşağıdakilerden hangisi GEREKLİDİR?
## A
Her alan için bir setter metodu ekleyip durumun yalnız kontrollü biçimde güncellenmesini sağlamak
🤖 Tam tersi; değişmez sınıfta setter OLMAMALIDIR, durum hiç değiştirilemez.
## B
Tüm alanları `public final` yapıp getter yazmaktan kaçınmak, böylece dışarıya doğrudan okuma sunmak
🤖 Alanlar `private` olmalı; `public` alan mutable bir tipse içeriği dışarıdan değiştirilebilir ve kapsülleme kaybolur.
## C
Sınıfı `abstract` yapıp durumun nasıl tutulacağını alt sınıflara bırakmak
🤖 `abstract` değişmezliği sağlamaz; tam tersine değişmez sınıf genelde `final` yapılır ki alt sınıf garantiyi bozmasın.
## D
Tüm alanları `private final` yapmak, setter koymamak ve sınıfı `final` yapmak
🤖 Doğru: değişmez sınıfın temel kuralları private final alanlar, setter yokluğu ve final sınıftır.
## Doğru
D
## Açıklama
Değişmez sınıfın klasik kuralları (D doğru): (1) tüm alanlar **`private final`** — dışarıdan erişilemez ve kurulumdan sonra yeniden atanamaz; (2) durumu değiştiren HİÇBİR metot (setter dahil) yok; (3) sınıf **`final`** — bir alt sınıf metotları geçersiz kılıp değişmezlik yanılsaması yaratamaz. Setter eklemek (A) doğrudan değişmezliği bozar; alanları `public` yapmak (B) kapsüllemeyi kırar ve alan mutable bir tipse (`List`, `Date`) içeriğini dış müdahaleye açar; `abstract` (C) ise değişmezliği sağlamaz (değişmez sınıflar tipik olarak `final`'dır, `abstract` değil). Bu kurallara ek olarak, mutable tipte alanlar için defensive copy de gerekir — ama sorunun sorduğu temel iskelet private final alanlar + setter yokluğu + final sınıftır. Değişmezlik, hataların büyük kısmını daha tasarım aşamasında eler.

# imm-2 | Orta
## Soru
Kurucusuna (constructor) dışarıdan `Date` ya da `List` gibi değiştirilebilir (mutable) bir nesne alan değişmez bir sınıf, bu referansı doğrudan alanına atarsa hangi sorun oluşur?
## A
Derleme hatası oluşur; mutable nesne değişmez sınıfta tutulamaz
🤖 Derleme hatası olmaz; sorun runtime'da değişmezliğin sessizce bozulmasıdır.
## B
Hiçbir sorun olmaz; `final` alan referansı zaten nesnenin içini korur
🤖 `final` yalnız referansın yeniden atanmasını engeller; işaret edilen nesnenin İÇİ yine değişebilir.
## C
Çağıran taraf hâlâ aynı nesneye referans tuttuğu için onu dışarıdan değiştirebilir ve değişmezlik bozulur
🤖 Doğru: paylaşılan mutable referans dışarıdan değiştirilebilir; kurucuda kopya alınmalıdır.
## D
Nesne kurucuda otomatik olarak derin kopyalanır (deep copy), bu yüzden dışarıdan değiştirilemez
🤖 Java otomatik derin kopya yapmaz; kopyalamayı sen açıkça yapmalısın.
## Doğru
C
## Açıklama
`final` bir alan yalnız **referansın yeniden atanmasını** engeller; işaret ettiği nesnenin İÇ durumunu korumaz. Kurucuya gelen mutable bir `Date`/`List` doğrudan alana atanırsa, ÇAĞIRAN taraf da hâlâ aynı nesneye referans tutar ve onu sonradan değiştirebilir (`date.setTime(...)`, `list.add(...)`) — böylece değişmez sanılan nesnenin durumu dışarıdan DEĞİŞİR (C doğru). Bu bir derleme hatası değildir (A yanlış), `final` bunu engellemez (B yanlış) ve Java otomatik derin kopya yapmaz (D yanlış). Çözüm **defensive copy**: kurucuda gelen nesnenin bir kopyasını al (`new ArrayList<>(list)`, `List.copyOf(list)`, `new Date(d.getTime())`) ve o kopyayı sakla. Böylece dış referansla iç durum arasındaki bağ kesilir ve değişmezlik korunur.

# imm-3 | Orta
## Soru
Değişmez bir sınıfın, içinde tuttuğu mutable bir `List` alanını bir getter ile döndürürken değişmezliği KORUMAK için ne yapması gerekir?
## A
İç listenin bir kopyasını (ya da değiştirilemez görünümünü) döndürmek
🤖 Doğru: getter iç mutable referansı verirse çağıran onu değiştirebilir; kopya/salt-okunur görünüm dönmelidir.
## B
İç listeyi doğrudan döndürmek; `final` alan olduğu için güvenlidir
🤖 `final` referansı korur ama liste içeriği çağıran tarafından değiştirilebilir; sızıntı olur.
## C
Getter'ı `synchronized` yapmak
🤖 Senkronizasyon görünürlük/kilit içindir; referans sızıntısını engellemez.
## D
Listeyi `static` yapmak
🤖 `static` paylaşımı artırır, sızıntıyı çözmez; değişmezlikle ilgisizdir.
## Doğru
A
## Açıklama
Değişmezlik iki uçta da korunmalıdır: kurucuda (giriş) VE getter'da (çıkış). Bir getter, içteki mutable `List` referansını DOĞRUDAN döndürürse, çağıran taraf `list.add(...)`/`list.clear()` ile nesnenin iç durumunu dışarıdan değiştirebilir — değişmezlik yine bozulur. Çözüm: getter'da ya bir **kopya** döndür (`new ArrayList<>(ic)`) ya da bir **değiştirilemez görünüm** ver (`Collections.unmodifiableList(ic)` veya alanı en baştan `List.copyOf` ile değişmez tutmak) (A doğru). `final` alan iç içeriği korumaz (B yanlış); `synchronized` (C) görünürlük/kilit meselesidir, sızıntıyı engellemez; `static` (D) tamamen alakasızdır. Bu "getter'dan referans sızdırma" hatası, defensive copy'nin çıkış tarafıdır ve mülakatta sık sorulur.

# imm-4 | Orta
## Soru
`record` kullanmak bir sınıfı bileşenleri açısından değişmez yapar; ancak bir record bileşeni mutable bir nesne (ör. `List`, dizi) tutuyorsa hangi ifade doğrudur?
## A
Derleyici, canonical constructor'da tüm mutable bileşenleri otomatik olarak değişmez bir kopyaya çevirir; ek önlem gerekmez
🤖 Yanlış; record gelen bileşeni KOPYALAMAZ, referansı olduğu gibi saklar.
## B
Bileşen REFERANSI değişmezdir (yeniden atanamaz) ama işaret ettiği nesnenin içi değişebilir; tam değişmezlik için compact constructor'da defensive copy gerekir
🤖 Doğru: record sığ (shallow) değişmezlik verir; derin değişmezlik için elle kopya alınmalıdır.
## C
Record içinde mutable bir bileşen tutmak derleme hatasıdır; record yalnız ilkel tipleri ve değişmez tipleri bileşen olarak kabul eder
🤖 Yanlış; record her tipte bileşen tutabilir, yalnız derin değişmezliği garanti etmez.
## D
Üretilen erişimci (accessor) her çağrıldığında bileşenin yeni bir kopyasını döndürür, bu yüzden iç referans hiç sızmaz
🤖 Yanlış; erişimci iç referansı doğrudan döndürür, hiçbir kopya üretmez.
## Doğru
B
## Açıklama
Record, bileşenlerini `private final` yapar; yani bileşen **referansı** yeniden atanamaz — bu **sığ (shallow) değişmezliktir**. Ancak bileşen mutable bir nesneyse (ör. `List`, `byte[]`), işaret edilen nesnenin İÇİ hâlâ değişebilir: record bunu KOPYALAMAZ (A yanlış), erişimci de her çağrıda kopya döndürmez, iç referansı verir (D yanlış). Yani `new Kayit(list)` sonrası dışarıdan `list.add(...)` record'un görünen durumunu değiştirir. **Derin (deep) değişmezlik** istiyorsan compact constructor'da defensive copy yapmalısın: `Kayit { veri = List.copyOf(veri); }` ve gerekiyorsa erişimciyi override edip kopya döndürmelisin (B doğru). Mutable bileşen tutmak derleme hatası değildir (C yanlış). Özet: record değişmezliği bileşen referansı düzeyinde verir; tam güvenlik için mutable bileşenlerde defensive copy şarttır.

# imm-5 | İleri
## Soru
Değişmezliğin (immutability) çok iş parçacıklı (multithreaded) kodda sağladığı en önemli kazanç nedir?
## A
Nesneler daha az bellek kullandığı için GC hiç çalışmaz
🤖 Değişmezlik GC'yi durdurmaz; kazanç bellek değil, eşzamanlı güvenliktir.
## B
Nesneler otomatik olarak birden çok CPU çekirdeğinde paralel çalışır
🤖 Değişmezlik paralel yürütme başlatmaz; sağladığı şey paylaşımın güvenli olmasıdır.
## C
Durum hiç değişmediği için nesne doğası gereği thread-safe olur; yarış koşulu, görünürlük sorunu ve kilit gerekmez
🤖 Doğru: değişmeyen durum eşzamanlı okumada güvenlidir; senkronizasyona gerek kalmaz.
## D
Değişmez nesneler `volatile` olmadan bile her yazmayı diğer thread'lere anında yansıtır
🤖 Yazma zaten yoktur; değişmez nesnede güncelleme olmadığından yayılacak bir yazma da yoktur.
## Doğru
C
## Açıklama
Değişmezliğin eşzamanlılıktaki büyük kazancı: **doğal thread-safety** (C doğru). Bir nesnenin durumu oluşturulduktan sonra HİÇ değişmiyorsa, birden çok iş parçacığı onu aynı anda okurken yarış koşulu (race condition), kayıp güncelleme ya da görünürlük sorunu OLUŞAMAZ — çünkü değiştirilecek/görünürlüğü bozulacak bir yazma yoktur. Bu yüzden değişmez nesneler kilit (lock) ya da senkronizasyon GEREKTİRMEDEN güvenle paylaşılır. Ek olarak, `final` alanlar için Java Bellek Modeli özel bir garanti verir: kurucu düzgün tamamlandıysa, diğer thread'ler `final` alanların doğru kurulmuş değerlerini `volatile` olmadan bile görür. Değişmezlik GC'yi durdurmaz (A), paralel yürütme başlatmaz (B) ve "her yazmayı yansıtma" (D) yanlıştır çünkü yazma zaten yoktur. Bu nedenle değişmezlik, eşzamanlı tasarımın en yalın güvenlik stratejisidir.

# imm-6 | İleri
## Soru
Çok sayıda alanı olan ve bazıları isteğe bağlı (optional) olan değişmez bir nesneyi okunur biçimde kurmak için hangi desen sıkça kullanılır?
## A
Her alan kombinasyonu için ayrı bir telescoping constructor yazmak
🤖 Teleskopik kurucular çoğaldıkça okunmaz hale gelir; builder tam da bu sorunu çözmek için vardır.
## B
Nesneyi argümansız kurucuyla boş kurup alanları setter'larla tek tek doldurmak
🤖 Setter kullanmak değişmezliği bozar; ayrıca nesne bir süre yarı kurulmuş, tutarsız durumda kalır.
## C
Tüm alanları `public` yapıp nesne oluşturulduktan sonra dışarıdan doğrudan atamak
🤖 Public alanlar hem kapsüllemeyi hem değişmezliği bozar; kabul edilebilir bir çözüm değildir.
## D
Builder deseni: değerler bir kurucu (builder) nesnesinde toplanıp `build()` ile tek seferde değişmez nesne üretilir
🤖 Doğru: builder, çok/opsiyonel alanlı değişmez nesneleri okunur ve güvenli biçimde kurar.
## Doğru
D
## Açıklama
Alan sayısı arttıkça ve bazıları opsiyonel olunca, tek bir dev kurucu (ya da her kombinasyon için ayrı **telescoping constructor**) hem okunmaz hem hataya açık olur (A yanlış): `new X(a, null, null, b, 0, false)` gibi çağrılar hangi argümanın ne olduğunu gizler. **Builder deseni** bunu çözer (D doğru): akıcı (fluent) metotlarla (`.ad("...").yas(30)`) değerler bir builder nesnesinde toplanır, sonra `build()` çağrısı **tek seferde** değişmez nesneyi üretir — böylece hedef nesne hiçbir zaman yarı-kurulmuş, tutarsız bir durumda görünmez. Builder, opsiyonel alanları atlamayı ve doğrulamayı `build()` içinde tek noktada yapmayı da kolaylaştırır. Setter'larla doldurmak (B) değişmezliği bozar; alanları `public` yapmak (C) hem kapsüllemeyi hem değişmezliği yıkar. Değişmezlik + builder, hem güvenli hem okunur nesne kurulumunun standart birleşimidir.
