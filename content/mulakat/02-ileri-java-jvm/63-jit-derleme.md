---
slug: jit-derleme
baslik: "JIT Derleme: Katmanlı Derleme ve Optimizasyonlar"
kademe: İleri
sira: 63
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Yabancı dilde bir kitabı okumanın iki yolu vardır. Ya yanınızda simultane tercüman olur — cümle cümle çevirir, hemen başlarsınız ama her okuyuşta baştan çevrilir. Ya da kitabı önceden bastırırsınız — hazırlığı zaman alır, sonra okuma çok hızlıdır. Java ikisini birden yapar: programı "tercümanla" anında başlatır, hangi sayfaların defalarca okunduğunu not eder ve yalnız o sayfaları basılı, hızlı sürüme çevirir. Böylece hem çabuk başlar hem uzun vadede hızlanır.

### Temel kavramlar

- **Bayt kodu (bytecode)**: `javac`'ın ürettiği ara dil. Makine kodu değildir; onu JVM okur.
- **Yorumlama (interpretation)**: bayt kodunu komut komut, adım adım yürütmek. Anında başlar, derleme beklemesi yoktur, ama yavaştır.
- **Derleme (compilation)**: kodu, işlemcinin doğrudan çalıştırabileceği yerel (native) komutlara çevirmek. Çevirmenin bir maliyeti vardır, ama çalıştırma çok hızlıdır.
- **JIT (Just-In-Time, tam-zamanında derleme)**: bu çeviriyi önceden değil, program ÇALIŞIRKEN yapmak.
- **Profil (profile)**: çalışma sırasında toplanan istatistik — hangi metot kaç kez çağrıldı, döngü kaç tur döndü, bir dal ne sıklıkla alındı, bir sanal çağrı hangi tipe gitti. JIT kararlarının hammaddesidir.
- **Sıcak nokta (hot spot)**: çok sık çalışan, dolayısıyla derlenmeye değen kod. HotSpot JVM adını buradan alır.
- **Isınma (warm-up)**: sürecin ilk saniyelerinde kodun henüz tepe hıza ulaşmamış olması.
- **Sayaçlar**: her metodun bir **çağrı sayacı**, her döngünün bir **geri-dal (back-edge) sayacı** vardır; eşik aşılınca kod "sıcak" sayılır.
- **Kaçış (escape)**: bir nesnenin, oluşturulduğu metodun dışına sızması — döndürülmesi, bir alana atanması, senkronize edilmesi.

### Nasıl çalışır?

Java kaynağı önce bayt koduna derlenir; bayt kodu ise çalışma zamanında ya yorumlanır ya da JIT ile yerel makine koduna derlenir. JVM ikisini birlikte kullanır: **katmanlı derleme (tiered compilation)**.

- **Yorumlayıcı (interpreter)**: anında başlar, yavaş çalışır, ama aynı zamanda **profil toplar**. Yani yalnızca bir yürütücü değil, aynı zamanda profilleyicidir; C1/C2'nin iyimser optimizasyonlarının temeli budur. Yorumlayıcı yerel kod üretip diske yazmaz, derleyicilerin çıktısını doğrulayıp kapatmaz (tersine onları besler), GC tetiklemez ve kod önbelleğini yönetmez.
- **C1 (client derleyici)**: hızlı derler, hafif optimizasyon yapar; erken hızlanma sağlar.
- **C2 (server derleyici)**: yavaş derler ama **agresif** optimize eder; sıcak kod için en yüksek performans.

Terfi sayaçlarla yönetilir: eşik aşılınca metot yorumlayıcıdan C1'e, oradan C2'ye yükselir. İşte bu yüzden JVM'de **ısınma** vardır — ilk saniyelerde kod yavaştır, profil biriktikçe tepe performansa ulaşılır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Katmanlı derleme: yorumlayıcıdan C1 ve C2'ye terfi">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Katmanlı derleme: sıcaklık arttıkça terfi</text>
  <rect x="24" y="60" width="150" height="90" rx="12" fill="#EAF0F6" stroke="#2C5A82" stroke-width="1.5"/>
  <text x="99" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#2C5A82" font-family="system-ui">Yorumlayıcı</text>
  <text x="99" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">anında başlar</text>
  <text x="99" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">profil toplar</text>
  <rect x="245" y="60" width="150" height="90" rx="12" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.5"/>
  <text x="320" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#B5701B" font-family="system-ui">C1</text>
  <text x="320" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hızlı derler</text>
  <text x="320" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">hafif optimizasyon</text>
  <rect x="466" y="60" width="150" height="90" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="541" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">C2</text>
  <text x="541" y="114" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">agresif optimize</text>
  <text x="541" y="132" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tepe performans</text>
  <path d="M174 105 L245 105" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#jit-ok)"/>
  <path d="M395 105 L466 105" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#jit-ok)"/>
  <defs>
    <marker id="jit-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="192" text-anchor="middle" font-size="11.5" font-weight="600" fill="#B4322B" font-family="system-ui">Varsayım bozulursa: deoptimizasyon → yorumlayıcıya geri düşer</text>
</svg>
```

Önemli bir düzeltme: JIT **her şeyi** derlemez. Yalnız sıcak metotlar derlenir; bir kez çalışan soğuk kod yorumlanmaya devam eder. Peki metot bir kez çağrılıp içinde milyon kez dönüyorsa? O zaman **OSR (on-stack replacement, yığın-üstü değiştirme)** devreye girer: JVM, çalışmakta olan çerçeveyi döngünün ORTASINDA derlenmiş sürüme geçirir.

C2'nin güçlü optimizasyonları:

- **Satır içine alma (inlining)**: küçük bir metodun gövdesini çağıranın içine kopyalar. İki kazanç verir: çağrının doğrudan maliyeti (yığın çerçevesi kurma, atlama) silinir; daha da önemlisi metot SINIRI kalkar, böylece sabit katlama (constant folding), ölü kod eleme ve kaçış analizi sınırı aşarak uygulanabilir. Bu yüzden "diğer optimizasyonları açan kapı" sayılır. Kod silip ikiliyi küçültmez — derlenmiş kod aksine büyür; metot sınırlarını korumaz, kaldırır; sanal çağrı da üretmez, tersine sanal çağrıyı çözüp (devirtualization) doğrudan gömerek ondan kurtarır.
- **Kaçış analizi (escape analysis) + skaler değiştirme (scalar replacement)**: nesne metottan kaçmıyorsa yığında (heap) ayrılmaz; alanları doğrudan yazmaçlara/çağrı yığıtına (stack) konur. Ayırma TÜMDEN yok olabilir, GC baskısı düşer. Bu, "GC'ye ipucu verip nesneyi daha ucuza toplatmak" değildir — ayırma hiç olmaz. Nesnenin kopyasını çıkarmakla ya da `volatile` işaretlemekle de ilgisi yoktur. Nesne herhangi bir yolla kaçarsa (döndürülür, alana atanır, senkronize edilir) optimizasyon uygulanamaz.
- **Deoptimizasyon (deoptimization)**: C2, profile dayanarak **iyimser varsayımlar** yapar ("bu sanal çağrı hep `ArrayList`'e gidiyor", "bu dal hiç alınmıyor", "bu sınıf hiç yüklenmedi") ve çok agresif kod üretir. Varsayım çalışma zamanında bozulursa derlenmiş kod geçersizleşir; yürütme güvenle **yorumlayıcıya** geri düşer, metot daha sonra güncellenmiş profille yeniden derlenebilir. Bu bir GC olayı, kapanış temizliği ya da periyodik tazeleme DEĞİLDİR — istisnai, varsayım-ihlali tetiklemeli bir güvenlik ağıdır.

```java
// KAÇMAZ → heap ayırması tümden kalkabilir
int uzunlukKare(int x, int y) {
    Nokta n = new Nokta(x, y);
    return n.x * n.x + n.y * n.y;
}

// KAÇAR (referans dışarı veriliyor) → heap'te ayrılır
Nokta olustur(int x, int y) {
    return new Nokta(x, y);
}
```

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kaçış analizi ve skaler değiştirme: kaçmayan nesne yığına ayrılmaz">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kaçış analizi → skaler değiştirme</text>
  <rect x="250" y="40" width="140" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">metotta new Nesne()</text>
  <path d="M300 80 L175 116" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#jit-esc-ok)"/>
  <path d="M340 80 L470 116" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#jit-esc-ok)"/>
  <rect x="45" y="118" width="250" height="74" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="170" y="142" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">KAÇAR: döndürülür / atanır</text>
  <text x="170" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yığında (heap) ayrılır</text>
  <text x="170" y="183" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">GC baskısı sürer</text>
  <rect x="345" y="118" width="250" height="74" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="470" y="142" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">KAÇMAZ</text>
  <text x="470" y="164" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">alanlar yazmaç/yığıta (stack)</text>
  <text x="470" y="183" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">heap ayırması YOK</text>
  <defs>
    <marker id="jit-esc-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
</svg>
```

### Mülakatta ne sorulur?

**Isınma kimi vurur?** Isınma bir GC olgusu değildir, yok sayılamaz ("JVM ilk komuttan itibaren tepe hızındadır" yanlıştır) ve 32/64-bit mimariyle ilgisi yoktur — katmanlı derlemenin doğal sonucudur, her JVM'de vardır. En çok **kısa ömürlü süreçlerde** acıtır: CLI araçları, sunucusuz (serverless) işlevler ve kısa batch işleri, kod tepe hıza ulaşamadan sonlanır. Uzun ömürlü servislerde ısınma bir kez ödenir ve önemsizleşir. Bu maliyet, AOT/GraalVM native ilgisini besleyen ana motivasyonlardandır.

```svg
<svg viewBox="0 0 640 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Isınma eğrisi: performans yorumlayıcıdan C1 ve C2 platosuna kademeli yükselir">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Isınma (warm-up) eğrisi: tepe performansa kademeli ulaşma</text>
  <line x1="70" y1="56" x2="70" y2="192" stroke="#5D7789" stroke-width="1.5"/>
  <line x1="70" y1="192" x2="606" y2="192" stroke="#5D7789" stroke-width="1.5"/>
  <text x="36" y="124" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui" transform="rotate(-90 36 124)">performans</text>
  <line x1="210" y1="56" x2="210" y2="192" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>
  <line x1="360" y1="56" x2="360" y2="192" stroke="#5D7789" stroke-width="1" stroke-dasharray="4 4"/>
  <path d="M70 178 L195 172 L210 172 L300 130 L360 98 L470 80 L606 78" fill="none" stroke="#186399" stroke-width="2.5" marker-end="url(#jit-warm-ok)"/>
  <text x="135" y="164" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yorumlayıcı</text>
  <text x="282" y="120" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">C1</text>
  <text x="500" y="70" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">C2 platosu</text>
  <text x="500" y="210" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">zaman / çağrı sayısı →</text>
  <defs>
    <marker id="jit-warm-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#186399"/>
    </marker>
  </defs>
  <text x="320" y="236" text-anchor="middle" font-size="11" font-weight="600" fill="#8A2E2E" font-family="system-ui">Kısa ömürlü süreç platoya ulaşamadan biter → ısınma maliyeti baskın</text>
</svg>
```

**Graal JIT nerede durur?** GraalVM iki ayrı şey sunar. JIT tarafı, HotSpot'un katmanlı derlemesinde tepe (top-tier) derleyici olan **C2'nin yerine geçebilen alternatif bir tepe derleyicidir**. Java ile yazılmıştır ve JVMCI arayüzü üzerinden takılır. Farklı optimizasyon geçişlerine sahiptir; bazı iş yüklerinde, özellikle yoğun soyutlama ve fonksiyonel kodda, daha iyi kod üretebilir. Çöp toplayıcı değildir; yorumlayıcının yerini almaz (profil yine toplanır); `javac` da değildir — `javac` derleme-zamanı aracıdır, Graal JIT ise çalışma-zamanı derleyicisi. **Sürüm gerçeği: bu derleyici bugün GraalVM dağıtımıyla gelir; OpenJDK'ye gömülü deneysel sürümü JDK 17'de kaldırılmıştır.** GraalVM'in DİĞER yüzü olan **native image (AOT)** ise ayrı bir konudur.

### Tek cümlede

JVM kodu önce yorumlayıp profil toplar, sıcaklaştıkça C1 ve C2 ile derler; inlining ve kaçış analizi gibi optimizasyonları iyimser varsayımlarla uygular ve varsayım bozulunca deoptimizasyonla yorumlayıcıya güvenle geri döner.

# jit-1 | İleri
## Soru
Katmanlı derlemede (tiered compilation) yorumlayıcının JIT dışındaki önemli görevi nedir?
## A
Derlediği yerel kodu diske önbelleğe alıp sonraki çalıştırmalarda yeniden kullanmak
🤖 Yorumlayıcı kod üretip diske yazmaz; bayt kodunu adım adım yürütür.
## B
C1 ve C2'nin ürettiği kodu doğrulamak, hatalıysa o derleyiciyi devre dışı bırakmak
🤖 Yorumlayıcı derleyici denetlemez; aksine onlara besleyecek profili toplar.
## C
Çalışma-zamanı profili toplamak: sıcak metotları ve dal/tip istatistiklerini kaydetmek
🤖 Doğru: bu profil, C1/C2'nin iyimser optimizasyonlarının temelidir.
## D
Çöp toplamayı tetikleyip kod önbelleğinde yer açmak, eski derlenmiş kodu atmak
🤖 GC tetikleme yorumlayıcının işi değildir; kod önbelleği yönetimi de ayrı bir mekanizmadır.
## Doğru
C
## Açıklama
Yorumlayıcı yalnız "yavaş ama anında başlayan" bir yürütücü değildir; aynı zamanda **profilleyicidir**. Çağrı sayaçları, döngü tekrar sayıları ve dal/tip istatistikleri toplar. Bu profil iki işe yarar: (1) hangi metotların "sıcak" olup derlenmeye değer olduğunu belirlemek, (2) JIT'in **iyimser varsayımlar** (bu çağrı hep şu tipe gidiyor, bu dal hep alınıyor) ile agresif optimize etmesini beslemek. Yorumlayıcı yerel kod üretip diske yazmaz (A), derleyicilerin çıktısını doğrulayıp kapatmaz (B — tam tersine onları besler) ve GC/kod önbelleği yönetiminden sorumlu değildir (D). Profil toplama, katmanlı derlemenin çalışmasının önkoşuludur.

# jit-2 | İleri
## Soru
Satır içine alma (inlining) neden JIT'in "anahtar" optimizasyonu sayılır?
## A
Az kullanılan metotları derlenmiş koddan atarak ikilinin (binary) boyutunu küçültür
🤖 Inlining kod silme değil kod GÖMME optimizasyonudur; üstelik derlenmiş kodu büyütür.
## B
Metot gövdesini çağıranın içine gömer; çağrı maliyetini siler ve diğer optimizasyonların önünü açar
🤖 Doğru: inlining hem çağrı maliyetini siler hem de diğer optimizasyonları mümkün kılar.
## C
Metot sınırlarını koruyup her metodun ayrı ayrı, bağımsız optimize edilmesini sağlar
🤖 Tam tersi: inlining metot sınırını KALDIRIR, böylece optimizasyon sınırı aşar.
## D
Metot çağrılarını sanal (virtual) çağrıya çevirip her çağrıda tip denetimi ekler
🤖 Tersi; inlining sanal çağrıyı çözüp (devirtualization) doğrudan gömerek çağrıdan kurtarır.
## Doğru
B
## Açıklama
Inlining, çağrılan (genelde küçük) bir metodun gövdesini **çağıranın içine kopyalar**. İki kazanç sağlar: (1) metot çağrısının doğrudan maliyeti (yığın çerçevesi kurma, atlama) yok olur; (2) daha da önemlisi, gömülen kod artık çağıranın bağlamında görülür — böylece **kaçış analizi, sabit katlama (constant folding), ölü kod eleme** gibi optimizasyonlar sınırları aşarak uygulanabilir. Bu yüzden inlining "diğer optimizasyonları açan kapı" sayılır. Kod silip ikiliyi küçültmez (A — aksine derlenmiş kod büyür), metot sınırlarını korumaz (C — tam tersine kaldırır) ve sanal çağrı üretmez; tersine sanal çağrıyı çözüp (devirtualization) doğrudan gömerek ondan kurtarır (D).

# jit-3 | İleri
## Soru
Kaçış analizi (escape analysis) bir nesnenin metottan "kaçmadığını" belirlerse, skaler değiştirme (scalar replacement) ile ne olabilir?
## A
Nesne yine heap'te ayrılır, ama GC onu genç nesilde tutup daha ucuza toplar
🤖 Optimizasyon GC'ye ipucu vermek değil; ayırmanın TÜMDEN yok olmasıdır.
## B
Nesne otomatik olarak `volatile` işaretlenip iş parçacıkları arasında güvenli kılınır
🤖 `volatile` eşzamanlılık anahtarıdır; kaçış analiziyle ilgisi yoktur.
## C
Nesnenin bir kopyası çıkarılır; her çağrı kendi kopyasıyla çalışır
🤖 Kopya çoğaltma diye bir optimizasyon yok; amaç ayırmayı azaltmaktır.
## D
Heap ayırması tümden atlanabilir; alanlar doğrudan yazmaçlara/yığıta (stack) konur
🤖 Doğru: kaçmayan nesne heap'e hiç ayrılmadan skaler alanlara dağıtılabilir.
## Doğru
D
## Açıklama
**Kaçış analizi**, bir nesnenin ömrünün oluşturulduğu metotla sınırlı olup olmadığını (yani başka iş parçacığına/döndürülen değere/statik alana "kaçıp kaçmadığını") belirler. Kaçmıyorsa, JIT **skaler değiştirme** uygular: nesneyi heap'te ayırmak yerine alanlarını doğrudan **yazmaçlara veya yığıta (stack)** koyar. Sonuç: heap ayırması tümden ortadan kalkabilir, dolayısıyla GC baskısı da azalır — ölçülebilir bir performans kazancı. Nesne "daha ucuz toplanmaz" (A); ayırma hiç olmaz. Bu optimizasyonun `volatile` işaretlemeyle (B) veya nesneyi kopyalamayla (C) da ilgisi yoktur. Not: nesne herhangi bir yolla kaçarsa (döndürülür, alana atanır, senkronize edilir) bu optimizasyon uygulanamaz.

# jit-4 | İleri
## Soru
Deoptimizasyon (deoptimization) ne zaman ve neden gerçekleşir?
## A
Profil temelli iyimser bir varsayım çalışma zamanında bozulunca; yürütme yorumlayıcıya geri düşer
🤖 Doğru: iyimser varsayım (tip, dal) çürüyünce derlenmiş koddan güvenli geri çekilme yapılır.
## B
Yığın (heap) dolup GC baskısı arttığında, bellekte yer açmak için GC tarafından tetiklenir
🤖 Deoptimizasyon bir GC olayı değildir; JIT'in varsayımlarının çürümesiyle ilgilidir.
## C
Uygulama kapanırken derlenmiş kod önbelleğinin düzenli biçimde boşaltılması için
🤖 Kapanış temizliği değil; uygulama çalışırken, varsayım ihlaliyle olur.
## D
Derlenmiş kod bayatlamasın diye JVM'in belirli aralıklarla yaptığı düzenli tazeleme
🤖 Düzenli tazeleme diye bir mekanizma yok; olay istisnaidir, varsayım bozulunca olur.
## Doğru
A
## Açıklama
C2, yorumlayıcının topladığı profile dayanarak **iyimser varsayımlar** yapar: "bu sanal çağrı hep `ArrayList` tipine gidiyor", "bu dal hiç alınmıyor", "bu sınıf hiç yüklenmedi". Bu varsayımlarla çok agresif (ve hızlı) kod üretir. Ama varsayım çalışma zamanında **bozulursa** (beklenmedik tip gelir, o dal ilk kez alınır, yeni sınıf yüklenir), derlenmiş kod artık yanlış olur. **Deoptimizasyon**, bu noktada derlenmiş koddan güvenle geri çekilip yürütmeyi **yorumlayıcıya** aktarır; metot daha sonra güncellenmiş profille yeniden derlenebilir. Bu bir GC olayı (B), kapanış temizliği (C) veya periyodik tazeleme (D) değildir — istisnai, varsayım-ihlali tetiklemeli bir güvenlik ağıdır.

# jit-5 | İleri
## Soru
"Isınma (warm-up)" olgusu JVM'de neden vardır ve hangi iş yükünde en çok sorun olur?
## A
Isınma yalnız çöp toplama duraklamalarından kaynaklanır ve uzun servislerde en kötüdür
🤖 Isınma GC değil, JIT'in henüz tepe kodu üretememesidir; asıl kısa süreçlerde acıtır.
## B
Isınma diye bir şey yoktur; JVM ilk komuttan itibaren tepe hızındadır
🤖 Yanlış; ilk saniyelerde kod yorumlanır/C1'dedir, tepe hıza sonra ulaşılır.
## C
JIT tepe performansa profil biriktikçe ulaşır; en çok kısa ömürlü CLI/sunucusuz süreçlerde acıtır
🤖 Doğru: kısa yaşayan süreçler tepe koda ulaşamadan biter; ısınma orada acıtır.
## D
Isınma yalnız 32-bit JVM'lerde görülür; 64-bit JVM'ler ilk komuttan itibaren tepe hızdadır
🤖 Mimariyle ilgisi yok; ısınma katmanlı derlemenin doğal sonucudur, her JVM'de vardır.
## Doğru
C
## Açıklama
JVM koda **kademeli** hız kazandırır: önce yorumlayıcı (yavaş, profil toplar), sonra C1, en sonunda sıcak metotlar için C2 (tepe performans). Bu terfi için **yeterli profil birikmesi** gerektiğinden, sürecin ilk saniyeleri görece yavaştır — buna **ısınma (warm-up)** denir. Isınma özellikle **kısa ömürlü süreçlerde** (CLI araçları, sunucusuz/serverless işlevler, kısa batch işleri) acı verir: süreç, kod tepe hıza ulaşamadan sonlanır. Uzun ömürlü servislerde ise ısınma bir kez ödenir ve önemsizleşir. Isınma bir GC olgusu değildir (A), yok sayılamaz (B) ve mimariden bağımsızdır (D). Bu maliyet, AOT/GraalVM native ilgisini besleyen ana motivasyonlardandır.

# jit-6 | İleri
## Soru
GraalVM'in JIT derleyicisi klasik HotSpot yığınında hangi bileşenin yerine geçebilir?
## A
Çöp toplayıcının (GC) yerine geçer; bellek yönetimini Java ile yeniden yazar
🤖 GraalVM JIT bir çöp toplayıcı değildir; görevi tepe derleyici olmaktır.
## B
Tepe (top-tier) derleyici C2'nin yerine geçebilen alternatif bir JIT derleyicisidir
🤖 Doğru: Graal, C2 yerine kullanılabilen alternatif tepe derleyicidir.
## C
Yorumlayıcının yerine geçer, böylece profil toplama kalkar
🤖 Yorumlayıcının yerini almaz; profil yine toplanır, Graal derleme katmanında çalışır.
## D
Java kaynağını bayt koduna derleyen `javac`'ın yerine geçer
🤖 `javac` derleme-zamanı aracıdır; Graal JIT çalışma-zamanı tepe derleyicisidir.
## Doğru
B
## Açıklama
GraalVM iki ayrı şey sunar; burada kastedilen **Graal JIT derleyicisidir**. HotSpot'un çalışma-zamanı katmanlı derlemesinde tepe derleyici geleneksel olarak **C2**'dir. Graal JIT, C2'nin **yerine geçebilen** alternatif bir tepe derleyicidir (Java ile yazılmış, JVMCI arayüzü üzerinden takılır, farklı optimizasyon geçişlerine sahip) ve bazı iş yüklerinde (özellikle yoğun soyutlama/fonksiyonel kod) daha iyi kod üretebilir. Pratik not: bu derleyici bugün **GraalVM dağıtımıyla** gelir; OpenJDK'ye gömülü deneysel sürümü JDK 17'de kaldırılmıştır. Bu bir GC (A), yorumlayıcı (C) veya `javac` (D) değildir — hepsi ayrı katmanlardır. Not: GraalVM'in DİĞER yüzü olan **native image (AOT)** ise ayrı bir konudur; bu soru JIT rolüyle ilgilidir.
