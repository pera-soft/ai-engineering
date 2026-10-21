---
slug: bellek-sizintisi-oom
baslik: "Bellek Sızıntısı ve OutOfMemoryError"
kademe: İleri
sira: 65
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Evdeki dolabı düşünün. Temizlikçi, "artık kimsenin işine yaramayan" eşyaları atıyor. Ama siz her eski kutunun üstüne "belki lazım olur" etiketi yapıştırırsanız, temizlikçi hiçbirini atamaz — dolap dolar taşar. Java'da da öyle: çöp toplayıcı, hâlâ bir yerden ERİŞİLEBİLEN hiçbir nesneyi toplayamaz. Bellek sızıntısı, tam olarak bu istemsiz etiketlerin birikmesidir; sonu `OutOfMemoryError`'dur.

### Temel kavramlar

- **Erişilebilirlik (reachability)**: bir nesneye, program başlangıcından (GC köklerinden — statik alanlar, çalışan iş parçacıklarının yığıtları) referans zinciriyle ulaşılabiliyor mu? Ulaşılabiliyorsa nesne "canlı" sayılır ve toplanamaz.
- **Bellek sızıntısı (memory leak)**: C'de "serbest bırakmayı unutmak"tır. **Java'da ise, artık gerekmeyen nesnelere hâlâ erişilebilir bir referansın kalmasıdır** — GC bozuk olduğu için değil, kod nesneyi elde tuttuğu için. Java'da elle `free()` yoktur; "mantıken ölü ama teknik olarak canlı" nesneler birikir.
- **GC kökü (GC root)**: erişilebilirlik zincirinin başladığı yer. `static` alanlar tipik köklerdir; uygulama boyunca yaşarlar, dolayısıyla içindekiler de yaşar.
- **Güçlü / zayıf referans (strong / weak reference)**: güçlü referans nesneyi canlı tutar; zayıf referans tutmaz, GC gerektiğinde toplayabilir.
- **OOM (`OutOfMemoryError`)**: JVM'in "istediğin belleği veremiyorum" hatası. Tek çeşit değildir — mesajın SONEKİ hangi bellek alanının tükendiğini söyler.
- **Yığın (heap) ve heap DIŞI bellek**: `-Xmx` yalnız nesne yığınını sınırlar. Sınıf meta verisi, doğrudan tamponlar ve iş parçacığı yığıtları bu sınırın DIŞINDADIR.

Büyük bir nesne ayırmak tek başına sızıntı değildir; sızıntı, ZAMANLA biriken erişilebilir çöptür.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Java sızıntısı: erişilebilir ama gereksiz nesneler">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Java sızıntısı = gereksiz ama erişilebilir referans</text>
  <rect x="40" y="48" width="150" height="60" rx="10" fill="#EAF0F6" stroke="#2C5A82" stroke-width="1.5"/>
  <text x="115" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#2C5A82" font-family="system-ui">static Map</text>
  <text x="115" y="94" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">GC kökü — hep canlı</text>
  <rect x="40" y="140" width="150" height="56" rx="10" fill="#FBEAEA" stroke="#B4322B" stroke-width="1.5"/>
  <text x="115" y="164" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B4322B" font-family="system-ui">eski girdiler</text>
  <text x="115" y="183" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">mantıken ölü</text>
  <path d="M115 108 L115 140" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#leak-ok)"/>
  <text x="200" y="128" font-size="10.5" fill="#B4322B" font-family="system-ui">referans → toplanamaz</text>
  <rect x="360" y="60" width="250" height="44" rx="8" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.3"/>
  <text x="376" y="87" font-size="11" fill="#1C3247" font-family="system-ui">OOM: heap / Metaspace</text>
  <rect x="360" y="112" width="250" height="44" rx="8" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.3"/>
  <text x="376" y="139" font-size="11" fill="#1C3247" font-family="system-ui">OOM: direct buffer / native thread</text>
  <defs>
    <marker id="leak-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="210" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Leak avı: live set izle → heap dump → dominator tree → kök referans</text>
</svg>
```

### Nasıl çalışır?

Üç klasik sızıntı kalıbı vardır ve üçü de aynı sebebe iner — istenmeyen erişilebilirlik:

- **Statik koleksiyon (static collection)**: `static Map`/`List` içine sürekli ekleyip hiç temizlememek. Statik alan uygulama boyunca yaşar, içindekiler de yaşar.
- **Kaydı silinmeyen dinleyici (listener/callback)**: bir nesneyi olay kaynağına dinleyici olarak eklemek ama iş bitince `removeListener` çağırmamak. Kaynak, olayları iletebilmek için dinleyiciye REFERANS tutar; dinleyicinin kendisi kapsam dışına çıksa bile bu referans onu erişilebilir kılar. Dolayısıyla dinleyici ve onun dolaylı tuttuğu tüm nesne grafiği toplanamaz. Uzun ömürlü bir kaynak (singleton event bus gibi) varsa bu birikir. Çözüm: simetrik ekleme/çıkarma disiplini ya da zayıf referanslı dinleyici kaydı. Dinleyicinin anonim/iç sınıf olması sızıntıyı yalnızca BÜYÜTÜR (dış nesneyi de tutar), sebebi değildir; dinleyici kaydı iş parçacığı da açmaz.
- **ThreadLocal**: iş parçacığı havuzunda (thread pool) `ThreadLocal.set` yapıp `remove` etmemek. Kendi başına yaratılıp ölen bir iş parçacığında değer, iş parçacığıyla birlikte gider; ama HAVUZ iş parçacıkları görevden göreve yeniden kullanılır ve havuz kapanana dek yaşar. `remove()` çağrılmazsa değer o iş parçacığında kalıcılaşır, hatta sonraki alakasız görev eski değeri görebilir. Sık duyulan "`ThreadLocalMap` anahtarları zayıf referans, kendiliğinden temizlenir" savunması yanıltıcıdır: zayıf olan **ANAHTAR**tır; **DEĞER güçlü referansla** tutulur ve ancak o iş parçacığı map'e tekrar dokunduğunda temizlenebilir — havuzda bu hiç olmayabilir. Doğru pratik: `try { tl.set(...) ... } finally { tl.remove(); }`.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üç klasik sızıntı kaynağı: statik koleksiyon, ThreadLocal, dinleyici">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">Üç klasik sızıntı kaynağı</text>
  <rect x="24" y="46" width="184" height="104" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="116" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">static koleksiyon</text>
  <text x="116" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ekle-ekle, temizle yok</text>
  <text x="116" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">static alan hep canlı</text>
  <rect x="228" y="46" width="184" height="104" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.4"/>
  <text x="320" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#6B3FA0" font-family="system-ui">ThreadLocal (havuz)</text>
  <text x="320" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">set var, remove yok</text>
  <text x="320" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">havuz thread'i yaşar</text>
  <rect x="432" y="46" width="184" height="104" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="524" y="74" text-anchor="middle" font-size="12" font-weight="700" fill="#B26B00" font-family="system-ui">dinleyici (listener)</text>
  <text x="524" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">removeListener yok</text>
  <text x="524" y="124" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kaynak dinleyiciyi tutar</text>
  <text x="320" y="182" text-anchor="middle" font-size="11.5" font-weight="700" fill="#8A2E2E" font-family="system-ui">Hepsi: mantıken ölü ama erişilebilir → GC toplayamaz</text>
</svg>
```

**OOM türleri — hepsi "heap doldu" değildir:**

- **Java heap space**: nesne yığını doldu (klasik sızıntı ya da yetersiz `-Xmx`).
- **GC overhead limit exceeded**: yığın teknik olarak henüz dolmadı ama GC toplam zamanın ~%98'ini harcayıp her seferinde çok az bellek geri kazanıyor. Sızıntının "OOM'dan hemen önceki" tipik habercisidir. Bu kontrol paralel/G1 gibi toplayıcılarda vardır; ZGC gibi eşzamanlı toplayıcılar bunun yerine ayırma bekletmesiyle kendini gösterir.
- **Metaspace**: sınıf meta verisi alanı doldu. Heap DIŞIDIR ve yüklenen sınıfların yapı bilgisini tutar. Tipik sebep kontrolsüz sınıf yüklemedir: çalışma zamanında sürekli dinamik proxy/sınıf üreten çerçeveler ya da eski sınıf yükleyicilerin serbest kalmayıp yükledikleri sınıfları canlı tutması (**classloader leak** — özellikle uygulama sunucularında yeniden dağıtımda).
- **Direct buffer / off-heap**: `DirectByteBuffer`/native bellek tükendi. Heap dışıdır, `-Xmx` kapsamaz.
- **Unable to create native thread**: işletim sisteminin iş parçacığı ya da native bellek sınırına takılındı.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="OOM türleri: heap, Metaspace, direct buffer, native thread">
  <text x="320" y="24" text-anchor="middle" font-size="14.5" font-weight="700" fill="#1C3247" font-family="system-ui">OOM türleri: hepsi 'heap doldu' değil</text>
  <rect x="24" y="42" width="285" height="66" rx="9" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.3"/>
  <text x="40" y="66" font-size="11.5" font-weight="700" fill="#B26B00" font-family="system-ui">Java heap space</text>
  <text x="40" y="90" font-size="10.5" fill="#1C3247" font-family="system-ui">nesne yığını doldu (heap içi)</text>
  <rect x="331" y="42" width="285" height="66" rx="9" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.3"/>
  <text x="347" y="66" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Metaspace</text>
  <text x="347" y="90" font-size="10.5" fill="#1C3247" font-family="system-ui">sınıf meta verisi (heap DIŞI)</text>
  <rect x="24" y="120" width="285" height="66" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="40" y="144" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">direct buffer / off-heap</text>
  <text x="40" y="168" font-size="10.5" fill="#1C3247" font-family="system-ui">native bellek, -Xmx kapsamaz</text>
  <rect x="331" y="120" width="285" height="66" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="347" y="144" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">unable to create native thread</text>
  <text x="347" y="168" font-size="10.5" fill="#1C3247" font-family="system-ui">OS thread/bellek sınırı</text>
  <text x="320" y="208" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Hata mesajının soneki kaynağı söyler → doğru teşhis</text>
</svg>
```

### Mülakatta ne sorulur?

**"Java'da GC var, sızıntı olmaz" tuzağı.** Olur. Tanım: gerekmeyen nesnelere kalan erişilebilir referans. Bu, `free()` unutmak (Java'da yok), GC'nin bozuk olması ya da nesnelerin büyük olması DEĞİLDİR.

**`unable to create native thread` neyi gösterir?** Heap'le İLGİLİ DEĞİLDİR. Her Java iş parçacığı bir yerel (native) iş parçacığına eşlenir ve kendi çağrı yığını (`-Xss`) için native bellek ister. Sınırsız iş parçacığı açmak (her istek için yeni thread, sınırsız havuz, thread sızıntısı) OS sınırına dayanır. İlginç sonuç: `-Xmx`'i büyütmek bunu KÖTÜLEŞTİREBİLİR, çünkü native belleğe daha az yer kalır. Çözüm: sınırlı havuzlar, sanal iş parçacıkları (virtual threads).

**OOM türlerini karıştırmayın.** Şişen koleksiyon → "Java heap space"; kontrolsüz sınıf yükleme → "Metaspace"; heap dışı tampon tükenmesi → "direct buffer memory"; iş parçacığı taşması → "unable to create native thread". Hata mesajını okumak doğru teşhisin ilk adımıdır.

**Leak avı iş akışı (sorulan sıra budur):** (1) GC logunda (`-Xlog:gc*`) toplama sonrası **live set** kalıcı tırmanıyor mu diye bak; ilk kanıt budur. (2) Şüphe kesinleşince **heap dump** al. (3) MAT'ta **dominator tree** ile en çok belleği TUTAN kökü belirle. (4) O köke giden **referans zincirini (GC root path)** izle ve "bu neden hâlâ erişilebilir?" sorusunu yanıtla. `-Xmx`'i iki katına çıkarmak yalnızca OOM'u erteler; körlemesine tüm statikleri/önbellekleri temizlemek davranışı bozar; gece yarısı yeniden başlatan cron işi ise semptomu maskeler ve borcu büyütür.

### Tek cümlede

Java'da sızıntı, artık gerekmeyen nesnelerin (statik koleksiyon, silinmeyen dinleyici, temizlenmeyen `ThreadLocal` yüzünden) erişilebilir kalmasıdır; teşhis, OOM mesajının türünü okuyup live set → heap dump → dominator tree → referans zinciri yolunu izlemekle yapılır.

# leak-1 | İleri
## Soru
Çöp toplayıcısı olan Java'da bir bellek sızıntısı teknik olarak nasıl tanımlanır?
## A
Artık gerekmeyen nesnelere hâlâ erişilebilir (reachable) bir referansın kalması
🤖 Doğru: erişilebilir olan toplanamaz; "ölü ama canlı tutulan" nesneler birikir.
## B
Nesneyi kullanıp bitirince `free()` ya da `dispose()` çağırmayı unutmak
🤖 Java'da elle `free()` yoktur; sızıntı erişilebilirlik sorunudur, elle serbest bırakma değil.
## C
Çöp toplayıcının bazı erişilemez nesneleri gözden kaçıracak kadar hatalı çalışması
🤖 GC nadiren "bozuktur"; sorun kodun gereksiz referansı elde tutmasıdır.
## D
Uygulamanın çok büyük nesneler ayırıp yığını hızla doldurması
🤖 Büyük nesne tek başına sızıntı değildir; sızıntı zamanla biriken erişilebilir çöptür.
## Doğru
A
## Açıklama
Java'da GC, **erişilemez (unreachable)** hale gelen her nesneyi eninde sonunda toplar. Dolayısıyla sızıntı, C/C++'taki "serbest bırakmayı unutma" değildir (B — Java'da elle `free` yoktur). Java sızıntısı, **artık mantıken gerekmeyen ama bir GC kökünden hâlâ erişilebilir** kalan nesnelerdir: static bir koleksiyon, kaydı silinmeyen bir dinleyici veya temizlenmeyen bir ThreadLocal onları canlı tutar. GC "canlı" gördüğü için toplayamaz, bellek birikir ve sonunda OOM gelir. Sorun GC'nin bozukluğu (C) veya nesne boyutu (D) değil, **istemsiz erişilebilirliktir**. Bu tanım, leak avının neden "referans zinciri izleme" işine dönüştüğünü açıklar.

# leak-2 | İleri
## Soru
İş parçacığı havuzunda (thread pool) `ThreadLocal` neden sinsi bir sızıntı kaynağıdır?
## A
Görev bittiğinde havuz iş parçacığı sonlandığı için değer yarım kalır
🤖 Havuz iş parçacığı görev bitince SONLANMAZ; sorun tam da yaşamaya devam etmesidir.
## B
`ThreadLocalMap` anahtarları zayıf referans olduğundan değerler otomatik temizlenir, sızıntı olmaz
🤖 Zayıf olan ANAHTAR'dır; değer güçlü tutulur ve `remove` çağrılmadıkça temizlenmeyebilir.
## C
Değerler tüm iş parçacıkları arasında paylaşıldığı için birbirinin üstüne yazılır
🤖 ThreadLocal değeri paylaşmaz, iş parçacığına bağlar; sorun paylaşım değil kalıcılıktır.
## D
Havuz iş parçacığı görevden göreve yaşamaya devam eder; `remove` edilmeyen değer o iş parçacığında kalıcılaşır
🤖 Doğru: havuz iş parçacığı ölmediği için ThreadLocal değeri birikerek sızar.
## Doğru
D
## Açıklama
`ThreadLocal`, değeri **o iş parçacığına** bağlar. Kendi başına yaratılıp ölen bir iş parçacığında değer, iş parçacığıyla birlikte gider. Ama **iş parçacığı havuzunda** iş parçacıkları görevden göreve **yeniden kullanılır** — havuz kapanana dek yaşarlar. Bir görev `threadLocal.set(...)` yapıp iş bitince `remove()` çağırmazsa, değer o havuz iş parçacığında **kalıcı** olur; hatta bir sonraki, alakasız görev eski değeri görebilir ve zamanla büyük nesneler birikip sızıntıya döner. Doğru pratik: `try { set(...) ... } finally { threadLocal.remove(); }`. Sık duyulan "ama `ThreadLocalMap` anahtarları zayıf referans (WeakReference), o yüzden kendiliğinden temizlenir" savunması (B) yanıltıcıdır: zayıf olan **anahtar**tır; **değer güçlü referansla** tutulur ve ancak o iş parçacığı map'e tekrar dokunduğunda temizlenebilir — havuzda bu hiç olmayabilir. Havuz iş parçacığı görev bitince sonlanmaz (A) ve ThreadLocal değeri iş parçacıkları arasında paylaşılmaz (C) — sorun, havuz iş parçacığının ömrüyle değerin ömrünün istemeden eşleşmesidir.

# leak-3 | İleri
## Soru
`OutOfMemoryError: Metaspace` hatası en olası olarak neyi işaret eder?
## A
Yığındaki (heap) nesnelerin çok büyüdüğünü; koleksiyonların sınırsız dolduğunu
🤖 Metaspace heap değildir; büyüyen koleksiyon "Java heap space" hatası verir.
## B
Sınıf meta verisi alanının dolduğunu — kontrolsüz sınıf yükleme veya classloader sızıntısı
🤖 Doğru: Metaspace sınıf meta verisini tutar; şişmesi sınıf yükleme sorununu işaret eder.
## C
Direct ByteBuffer'ların tükendiğini; heap dışı tampon belleğinin sızdığını
🤖 O ayrı bir hatadır ("direct buffer memory"); Metaspace sınıf meta verisiyle ilgilidir.
## D
Çok fazla iş parçacığı açıldığını; `-Xss` değerinin fazla büyük verildiğini
🤖 Bu "unable to create native thread" hatasıdır; Metaspace ile karışmaz.
## Doğru
B
## Açıklama
JVM'de OOM tek çeşit değildir; hata mesajının **soneki** kaynağı söyler. **Metaspace**, yüklenen sınıfların meta verisini (bytecode değil, yapı bilgisi) tutan, heap DIŞI bir alandır. `OutOfMemoryError: Metaspace`, tipik olarak **kontrolsüz sınıf yüklemeyi** işaret eder: çalışma zamanında sürekli dinamik proxy/sınıf üreten çerçeveler, ya da eski sınıf yükleyicilerin serbest kalmayıp yükledikleri sınıfları canlı tutması (**classloader leak** — özellikle uygulama sunucularında yeniden dağıtımda). Bu; büyük heap nesnesi (A → "Java heap space"), direct buffer tükenmesi (C) veya iş parçacığı taşması (D → "unable to create native thread") ile karıştırılmamalıdır. Doğru teşhis, doğru OOM türünü tanımaktan geçer.

# leak-4 | İleri
## Soru
Kaydı silinmeyen bir dinleyici (listener) neden sızıntıya yol açar?
## A
Dinleyiciyi tutan nesne kapsam dışına çıkınca GC dinleyiciyi de toplar, sorun yalnız gecikmedir
🤖 Toplayamaz: olay kaynağı hâlâ dinleyiciye referans tutar, yani nesne erişilebilir kalır.
## B
İç sınıf (inner class) olarak yazılmadıkça dinleyici sızdırmaz
🤖 İç sınıf dış nesneyi tutarak durumu ağırlaştırır ama sızıntının kaynağı kayıt silinmemesidir.
## C
Olay kaynağı dinleyiciye referans tutar; `remove` edilmezse dinleyici ve tuttuğu grafik toplanamaz
🤖 Doğru: kaynak → dinleyici referansı, artık gerekmeyen dinleyiciyi canlı tutar.
## D
Her dinleyici kendi iş parçacığını açtığı için bellek tükenir
🤖 Dinleyici kaydı iş parçacığı açmaz; sorun iş parçacığı değil, tutulan referanstır.
## Doğru
C
## Açıklama
Bir nesneyi olay kaynağına dinleyici olarak eklediğinizde, kaynak o dinleyiciye bir **referans** tutar (olayları ona iletebilmek için). Dinleyici artık gerekmediğinde (ör. kapatılan bir ekran bileşeni) `source.removeListener(x)` çağrılmazsa, kaynak yaşadığı sürece dinleyici — ve dinleyicinin dolaylı tuttuğu tüm nesne grafiği — **erişilebilir** kalır, dolayısıyla toplanamaz. Uzun ömürlü bir kaynak (singleton event bus gibi) varsa bu birikir ve sızar. Çözüm: simetrik ekleme/çıkarma disiplini ya da zayıf referanslı (weak) dinleyici kaydı. Dinleyiciyi tutan nesnenin kapsam dışına çıkması yetmez (A) — kaynaktan gelen referans onu hâlâ erişilebilir kılar. İç sınıf olup olmaması da belirleyici değildir (B): anonim/iç sınıf dinleyici, dış nesneyi de birlikte tuttuğu için sızıntıyı yalnızca BÜYÜTÜR, sebebi değildir. Dinleyici kaydı iş parçacığı da açmaz (D). Bu, klasik **istenmeyen erişilebilirlik** kalıbıdır.

# leak-5 | İleri
## Soru
Üretimde bellek sızıntısı avlarken en makul iş akışı hangisidir?
## A
GC logunda live set eğilimi → heap dump → dominator tree → referans zinciri
🤖 Doğru: gözlemden köke doğru sistematik daralt; çıktıya bakmadan koda dokunma.
## B
Doğrudan `-Xmx` değerini iki katına çıkarıp OOM'un tekrarlayıp tekrarlamadığına bakmak
🤖 Bu sadece OOM'u erteler; sızıntı sürer, kökü bulunmaz.
## C
Şüpheli görünen tüm static alanları ve önbellekleri temizleyip davranışı gözlemek
🤖 Körlemesine değişiklik davranışı bozar; önce ölçüm ve kanıt gerekir.
## D
Uygulamayı gece yarısı yeniden başlatan bir cron işi kurup belleği sıfırlamak
🤖 Yeniden başlatma semptomu maskeler; kök neden bulunmaz, borç büyür.
## Doğru
A
## Açıklama
Leak avı bir **gözlem** disiplinidir, tahmin değil. Doğru sıra: (1) **GC logunda** (`-Xlog:gc*`) toplama sonrası **live set** kalıcı tırmanıyor mu diye bak — sızıntının ilk kanıtı budur; (2) şüphe kesinleşince **heap dump** al; (3) MAT'ta **dominator tree** ile en çok belleği TUTAN kökü belirle; (4) o köke giden **referans zincirini (GC root path)** izleyip "bu neden hâlâ erişilebilir?" sorusunu yanıtla — kök neden orada çıkar. `-Xmx` büyütmek (B) sadece erteler; körü körüne static silmek (C) davranışı bozar; periyodik restart (D) semptomu maskeler, borcu büyütür. Kanıttan köke giden akış tek sürdürülebilir yoldur.

# leak-6 | İleri
## Soru
`OutOfMemoryError: unable to create native thread` hatası tipik olarak neyi gösterir?
## A
Java heap'in nesnelerle dolduğunu; çözümün `-Xmx` değerini büyütmek olduğunu
🤖 O "Java heap space" hatasıdır; burada `-Xmx` büyütmek durumu kötüleştirebilir bile.
## B
Metaspace'in dolduğunu; aşırı sınıf yüklendiğini
🤖 Metaspace sınıf meta verisidir; bu hata iş parçacığı/native kaynak sınırıyla ilgilidir.
## C
Direct ByteBuffer havuzunun tükendiğini; heap dışı tampon sızıntısını
🤖 Direct buffer ayrı bir OOM türüdür ("direct buffer memory"); bu hata thread oluşturmaya işaret eder.
## D
Çok fazla iş parçacığı açıldığını; işletim sistemi iş parçacığı/native bellek sınırına takılındığını
🤖 Doğru: iş parçacığı taşması; her thread OS kaynağı ve native stack belleği ister.
## Doğru
D
## Açıklama
Bu OOM türü heap'le İLGİLİ DEĞİLDİR. Her Java iş parçacığı, işletim sisteminde bir yerel (native) iş parçacığına eşlenir ve kendi **çağrı yığını (stack)** için native bellek ister (`-Xss` ile ayarlanır). Uygulama sınırsız/çok fazla iş parçacığı açarsa (ör. her istek için yeni thread, sınırsız havuz, thread sızıntısı), OS'in iş parçacığı sayısı ya da native bellek sınırına takılınır ve **`unable to create native thread`** gelir. İlginç bir sonuç: heap'i büyütmek (`-Xmx`) bunu KÖTÜLEŞTİREBİLİR, çünkü native bellek daralır. Çözüm: iş parçacığı sayısını sınırla (sınırlı havuzlar), sanal iş parçacıklarını (virtual threads) düşün. Bu; heap (A), Metaspace (B) veya direct buffer (C) tükenmesiyle karıştırılmamalıdır — hata mesajını okumak doğru teşhisin ilk adımıdır.
