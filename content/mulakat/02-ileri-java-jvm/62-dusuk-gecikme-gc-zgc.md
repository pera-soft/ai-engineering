---
slug: dusuk-gecikme-gc-zgc
baslik: "Düşük Gecikmeli Çöp Toplama: ZGC"
kademe: Uzman
sira: 62
etiketler: 8
---

## Konu anlatımı

### Bu ne işe yarar?

Bir program çalışırken sürekli yeni nesneler üretir ve çoğunu kısa sürede bırakır. Java'da bu artıkları otomatik temizleyen hizmetliye **çöp toplayıcı** denir. Sorun şudur: eski toplayıcılar temizlik yaparken programı tamamen durdurur — dolu bir restoranın ortasında "herkes dursun, masaları topluyoruz" demek gibi. Müşteri bunu bekleme olarak hisseder. ZGC, temizliği servis sürerken yapmayı hedefler; böylece bekleme neredeyse hiç hissedilmez.

### Temel kavramlar

- **Yığın (heap)**: nesnelerin yaşadığı bellek alanı. `new` ile ürettiğiniz her nesne buraya konur.
- **Çöp toplama (garbage collection, GC)**: artık hiçbir yerden erişilemeyen nesnelerin belleğinin geri alınması.
- **Duraklama (pause) / dünyayı durdurma (stop-the-world, STW)**: GC'nin işini yapabilmek için uygulamanın TÜM iş parçacıklarını dondurduğu an. Program o süre boyunca hiçbir iş yapmaz; kullanıcı bunu "takılma" olarak görür.
- **Gecikme (latency) ve verim (throughput)**: gecikme, TEK bir isteğin ne kadar sürede bittiğidir; verim, birim zamanda toplam ne kadar iş bitirildiğidir. Aynı şey değildirler — saatte çok iş bitiren bir sistem arada yarım saniyelik duraklamalar yaşayabilir.
- **Kuyruk gecikmesi (tail latency, p99/p999)**: isteklerin en yavaş yüzde biri (p99) ya da binde biri (p999). Ortalama iyi görünürken kuyruk berbat olabilir; SLA'lar genelde kuyruğa bakar.
- **Ayak izi (footprint)**: uygulamanın kapladığı toplam bellek.
- **Eşzamanlı (concurrent)**: GC'nin, uygulama iş yaparken aynı anda çalışması.
- **Taşıma / sıkıştırma (relocation / compaction)**: nesneleri bellekte başka bir yere kopyalayıp boşlukları birleştirme. Parçalanmayı önler, ama nesnenin ADRESİ değişir — asıl zorluk buradadır.
- **Nesil (generation)**: nesneleri yaşına göre ayırmak. "Çoğu nesne genç ölür" gözlemine **zayıf nesil hipotezi (weak generational hypothesis)** denir; yaşamayı sürdüren nesne yaşlı nesle **terfi (promotion)** eder.

Çöp toplayıcılar üç eksende yarışır: **duraklama · verim (throughput) · ayak izi (footprint)**. Üçünü birden en iyi yapan yoktur; her toplayıcı birini seçip ötekilerden ödün verir.

```svg
<svg viewBox="0 0 640 220" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ZGC eşzamanlı toplama ile alt-milisaniye duraklama">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">GC üçlemesi: duraklama · throughput · footprint</text>
  <rect x="24" y="46" width="190" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="119" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#25804C" font-family="system-ui">ZGC</text>
  <text x="119" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">duraklama: alt-ms</text>
  <text x="119" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">eşzamanlı taşıma</text>
  <text x="119" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">renkli işaretçi +</text>
  <text x="119" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yükleme bariyeri</text>
  <text x="119" y="174" text-anchor="middle" font-size="10.5" fill="#B4322B" font-family="system-ui">maliyet: throughput/footprint</text>
  <rect x="225" y="46" width="190" height="140" rx="12" fill="#EAF0F6" stroke="#2C5A82" stroke-width="1.5"/>
  <text x="320" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#2C5A82" font-family="system-ui">G1</text>
  <text x="320" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">duraklama: 10-200ms</text>
  <text x="320" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">dengeli varsayılan</text>
  <text x="320" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">bölge (region) tabanlı</text>
  <text x="320" y="160" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">iyi throughput</text>
  <rect x="426" y="46" width="190" height="140" rx="12" fill="#FBF1E6" stroke="#B5701B" stroke-width="1.5"/>
  <text x="521" y="72" text-anchor="middle" font-size="13.5" font-weight="700" fill="#B5701B" font-family="system-ui">Shenandoah</text>
  <text x="521" y="96" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">duraklama: düşük</text>
  <text x="521" y="116" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">eşzamanlı taşıma</text>
  <text x="521" y="136" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">yönlendirme işaretçisi</text>
  <text x="521" y="152" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">(forwarding pointer)</text>
  <text x="320" y="208" text-anchor="middle" font-size="11.5" font-weight="600" fill="#1C3247" font-family="system-ui">Büyük yığın + sıkı gecikme SLA'sı → ZGC; dengeli genel amaç → G1</text>
</svg>
```

Not: **JDK 9'dan bu yana varsayılan toplayıcı G1'dir**; ZGC ve Shenandoah açıkça bayrakla seçilir (`-XX:+UseZGC`, `-XX:+UseShenandoahGC`).

### Nasıl çalışır?

ZGC duraklamayı önceliklendirir: hedefi, yığın ne kadar büyürse büyüsün duraklamaların **yığın boyutundan bağımsız** ve **alt-milisaniye (sub-ms)** kalmasıdır. Bunu, işaretlemenin (marking) ve taşımanın BÜYÜK kısmını uygulama iş parçacıklarıyla eşzamanlı yürüterek başarır. Sihrin iki bileşeni vardır:

- **Renkli işaretçiler (colored pointers)**: nesne referansının kullanılmayan bitlerine GC durum bilgisi (işaretlendi mi, taşındı mı) gömülür. Böylece nesne başlığında ayrı bir bayrağa gerek kalmaz.
- **Yükleme bariyeri (load barrier)**: bir referans OKUNDUĞU anda araya giren küçük bir kod parçası. İşaretçi "bayat" ise — yani gösterdiği nesne çoktan taşınmışsa — bariyer referansı yeni adrese düzeltip öyle döndürür (kendini onarma, self-healing). Bu yüzden nesneler uygulama durmadan taşınabilir.

Kuşaklı ZGC, nesiller arası referansları izleyebilmek için ayrıca bir **yazma bariyeri (store barrier)** kullanır.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="ZGC yükleme bariyeri: bayat işaretçiyi okuma anında yeni adrese düzeltir">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yükleme bariyeri: eşzamanlı taşımanın motoru</text>
  <rect x="24" y="52" width="132" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="80" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Uygulama</text>
  <text x="90" y="100" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">referansı okur</text>
  <rect x="196" y="42" width="150" height="86" rx="10" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="271" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#6B3FA0" font-family="system-ui">Yükleme bariyeri</text>
  <text x="271" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">renkli bit "bayat" mı?</text>
  <text x="271" y="108" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">taşındı mı diye bakar</text>
  <rect x="386" y="42" width="230" height="40" rx="10" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.4"/>
  <text x="501" y="66" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bayat → yeni adrese düzelt (self-heal)</text>
  <rect x="386" y="90" width="230" height="38" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="501" y="113" text-anchor="middle" font-size="10.5" fill="#25804C" font-family="system-ui">güncel → doğrudan döndür</text>
  <path d="M156 85 L196 85" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#zgc-bar-ok)"/>
  <path d="M346 72 L386 62" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#zgc-bar-ok)"/>
  <path d="M346 98 L386 108" stroke="#5D7789" stroke-width="2" fill="none" marker-end="url(#zgc-bar-ok)"/>
  <defs>
    <marker id="zgc-bar-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="320" y="160" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Uygulama durmadan nesneler taşınır; okuma anında işaretçi kendini onarır.</text>
  <text x="320" y="184" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Renkli işaretçi: GC durumu referansın kullanılmayan bitlerine gömülüdür.</text>
</svg>
```

Alt-milisaniye bir **hedeftir, mutlak garanti değil**. Duraklamalar yığın boyutundan bağımsızdır, ama uygulamanın ayırma hızı eşzamanlı toplama hızını aşarsa uygulama bellek beklemek zorunda kalabilir (**ayırma bekletmesi, allocation stall**). "ZGC ile duraklama tamamen biter" demek yanlıştır; doğru ifade "duraklamalar yığından bağımsız ve tipik olarak alt-ms kalır"dır.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Nesil-tabanlı ZGC: genç nesil sık, yaşlı nesil seyrek toplanır">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Nesil-tabanlı ZGC (JDK 23+ varsayılan)</text>
  <rect x="40" y="48" width="250" height="104" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Genç nesil</text>
  <text x="165" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çoğu nesne genç ölür</text>
  <text x="165" y="115" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SIK toplanır — küçük, ucuz</text>
  <text x="165" y="135" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(weak generational hypothesis)</text>
  <rect x="350" y="48" width="250" height="104" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="475" y="74" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Yaşlı nesil</text>
  <text x="475" y="96" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">uzun yaşayan nesneler</text>
  <text x="475" y="115" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SEYREK toplanır</text>
  <text x="475" y="135" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">terfi (promotion) ile dolar</text>
  <path d="M290 100 L350 100" stroke="#B26B00" stroke-width="2" fill="none" marker-end="url(#zgc-gen-ok)"/>
  <text x="320" y="92" text-anchor="middle" font-size="10.5" fill="#B26B00" font-family="system-ui">terfi</text>
  <defs>
    <marker id="zgc-gen-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto">
      <path d="M0 0 L9 4.5 L0 9 z" fill="#B26B00"/>
    </marker>
  </defs>
  <text x="320" y="180" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Nesilleri ayrı toplamak, alt-ms hedefini korurken CPU/bellek maliyetini düşürür.</text>
</svg>
```

### Mülakatta ne sorulur?

**Sürüm gerçeği.** Nesil-tabanlı (kuşaklı, generational) ZGC **JDK 21'de** `-XX:+ZGenerational` bayrağıyla isteğe bağlı geldi, **JDK 23'te varsayılan mod** oldu (nesilsiz mod aynı sürümde kullanımdan kaldırıldı/deprecated), **JDK 24'te nesilsiz mod tamamen çıkarıldı**. Bugün `-XX:+UseZGC` demek doğrudan kuşaklı ZGC demektir; `-XX:+UnlockExperimentalVMOptions` gibi ek bir deneysel kilit gerekmez. Nesilleri ayrı toplamak, alt-ms hedefini korurken CPU ve bellek maliyetini çoğu iş yükünde ciddi düşürür.

**ZGC ≠ Shenandoah.** İkisi de düşük gecikmelidir ve nesneleri eşzamanlı taşır, ama mekanizmaları farklıdır. ZGC referansın KENDİ bitlerine renk gömer. **Shenandoah** ise tarihsel olarak her nesnenin BAŞLIĞINDA bir **yönlendirme işaretçisi (Brooks forwarding pointer)** tutar. Nesne taşınınca bu işaretçi yeni konumu gösterir ve erişimler oradan yönlendirilir. Aynı hedefe iki farklı tasarımla giderler; ikisi de stop-the-world sıkıştırmadan kaçınır.

**Bedeli nedir?** Alt-ms kazancının karşılığı **ham verimde (throughput) hafif düşüş ve bir miktar CPU/bellek ayak izidir**. Her referans okumasına eklenen yükleme bariyeri ve eşzamanlı iş, uygulamayla CPU paylaşır. Renkli işaretçiler ve meta veri de bellek ayak izini (footprint) büyütür. ZGC footprint şampiyonu DEĞİLDİR. JIT ile ilgisi yoktur (ikisi birlikte sorunsuz çalışır), çok iş parçacıklılığı yasaklamaz (tersine yüksek çekirdekli sistemler için tasarlandı) ve 4 GB gibi bir üst sınırı yoktur — terabaytlara ölçeklenir.

**Ne yapar, ne yapmaz?** ZGC **sıkıştıran (compacting)** bir toplayıcıdır: nesneleri gerçekten taşır, yalnız işaretleyip boşalan blokları serbest listeye eklemez. Yığının ikinci bir kopyasını tutup takas etmek gibi bir mekanizma da yoktur — bellek maliyetini ikiye katlardı. GC hiçbir zaman ortadan kalkmaz; sadece duraklama olarak görünmez hale gelir.

**Seçim kuralı.** Onlarca-yüzlerce GB yığın + sıkı p99/p999 SLA'sı (ör. 64 GB yığınla çalışan bir alım-satım sistemi, ya da duraklaması kullanıcıya asla yansımaması gereken bir servis) → **ZGC**. Duraklamaya makul toleransı olan, buna karşılık toplam verimi ve düşük CPU/bellek maliyetini önemseyen dengeli batch/servis yükü → **G1**: bölge (region) tabanlı, duraklama hedefi ayarlanabilir, tipik 10-200 ms duraklamalı, olgun ve genel amaçlı bir varsayılan. "Hem binlerce milisaniyelik duraklama serbest hem en düşük gecikme şart" gibi ifadeler kendi içinde çelişkilidir.

### Tek cümlede

ZGC, renkli işaretçi ve yükleme bariyeri sayesinde işaretleme ile taşımayı uygulama çalışırken yapar; böylece duraklamayı yığın boyutundan bağımsız ve tipik olarak alt-milisaniye tutar — bedeli bir miktar verim ve ayak izidir.

# zgc-1 | Uzman
## Soru
ZGC'nin temel tasarım hedefi nedir?
## A
Bellek ayak izini (footprint) mümkün olan en küçük değere indirmek
🤖 ZGC footprint şampiyonu değildir; renkli işaretçi ve bariyer ek maliyet getirir.
## B
Yığın boyutundan bağımsız olarak duraklama sürelerini alt-milisaniye düzeyinde tutmak
🤖 Doğru: ZGC gecikme (latency) odaklıdır; işaretleme ve taşımayı eşzamanlı yürütür.
## C
Ham verimliliği (throughput) diğer tüm toplayıcıların üstüne çıkarmak
🤖 Eşzamanlı iş ve bariyer ham verimden biraz götürür; hedef throughput değil.
## D
Çöp toplamayı tamamen ortadan kaldırmak
🤖 GC'yi yok etmez; duraklamayı küçültür, toplama yine gerçekleşir.
## Doğru
B
## Açıklama
ZGC bir **düşük gecikme (low-latency)** toplayıcısıdır: tasarım hedefi, yığın boyutu büyüdükçe bile duraklamaların yığından **bağımsız** ve **alt-milisaniye** kalmasıdır. Bunu, işaretleme ve nesne taşımanın (relocation) neredeyse tamamını uygulama iş parçacıklarıyla eşzamanlı çalıştırarak başarır. Bu odak, throughput (C) ve footprint (A) eksenlerinde ödün vermek pahasına gelir — bariyerler ve renkli işaretçiler CPU/bellek maliyeti ekler. GC hiçbir zaman ortadan kalkmaz (D); sadece duraklama olarak görünmez hale gelir.

# zgc-2 | Uzman
## Soru
ZGC, nesneleri uygulama çalışırken (eşzamanlı) taşıyabilmesini hangi mekanizmayla sağlar?
## A
Yükleme bariyeri (load barrier): referans okunduğu anda işaretçiyi denetleyip gerekirse yeni adrese düzeltir
🤖 Doğru: okuma anında araya giren bariyer, eşzamanlı taşımayı güvenli kılar.
## B
Uygulama iş parçacıklarını taşıma boyunca durdurup adresleri toplu günceller (stop-the-world)
🤖 Bu klasik durdurmalı taşımadır; ZGC tam da bundan kaçınmak için tasarlandı.
## C
Nesneleri hiç taşımaz; yalnız işaretleyip boşalan blokları serbest listeye ekler
🤖 ZGC sıkıştıran (compacting) bir toplayıcıdır; nesneleri taşır, parçalanmayı azaltır.
## D
Yığının tam bir kopyasını tutar; toplamayı kopyada yapıp sonra iki kopyayı takas eder
🤖 Çift yığın kopyası diye bir mekanizma yok; bellek maliyeti iki katına çıkarırdı.
## Doğru
A
## Açıklama
ZGC'nin eşzamanlı taşımadaki anahtarı **yükleme bariyeridir**: uygulama bir nesne referansını yüklediği (okuduğu) anda, bariyer o işaretçinin renkli bitlerine bakar. İşaretçi "bayat" ise — yani nesne zaten yeni bir adrese taşınmışsa — bariyer referansı yeni adrese **düzeltir** (self-healing) ve öyle döndürür. Böylece uygulama durdurulmadan (B yanlış) nesneler taşınabilir. ZGC sıkıştıran bir toplayıcıdır, nesneleri gerçekten taşır (C yanlış). Çift yığın kopyası (D) diye bir yaklaşım yoktur. Renkli işaretçiler + yükleme bariyeri ikilisi, ZGC'nin alt-ms duraklama vaadinin motorudur.

# zgc-3 | İleri
## Soru
JDK 23 ve sonrasında ZGC'nin nesil (generational) durumu nedir?
## A
Nesil kavramı ZGC'de hiç desteklenmez
🤖 Aksine, generational ZGC artık tek ve varsayılan moddur.
## B
Nesilsiz mod varsayılan, generational ise deneyseldir
🤖 Tersi doğru; nesilsiz mod kaldırıldı, generational varsayılan oldu.
## C
Generational ZGC varsayılan ve tek moddur; eski nesilsiz mod kaldırılmıştır
🤖 Doğru: JDK 23'ten itibaren ZGC genç/yaşlı nesilleri ayrı toplar.
## D
Generational mod yalnızca `-XX:+UnlockExperimentalVMOptions` ile açılır
🤖 Deneysel kilit gerekmez; generational mod standart ve varsayılandır.
## Doğru
C
## Açıklama
İlk ZGC nesilsiz (non-generational) idi: tüm yığını tek yaş grubu gibi tarardı. JDK 21'de `-XX:+ZGenerational` bayrağıyla isteğe bağlı gelen **nesil-tabanlı (kuşaklı) ZGC**, "çoğu nesne genç ölür" (weak generational hypothesis) gözlemini kullanarak genç ve yaşlı nesilleri AYRI toplar. **JDK 23'te kuşaklı mod varsayılan oldu** ve nesilsiz mod kullanımdan kaldırıldı (deprecated); **JDK 24'te nesilsiz mod tamamen çıkarıldı**. Yani JDK 23 ve sonrasında pratikte tek yol kuşaklı ZGC'dir. Sonuç: aynı alt-ms duraklama hedefi korunurken CPU ve bellek maliyeti belirgin düşer. Artık `-XX:+UseZGC` seçmek doğrudan kuşaklı toplayıcıyı getirir; ek deneysel bayrak (D) gerekmez.

# zgc-4 | Uzman
## Soru
Shenandoah toplayıcısı, eşzamanlı taşımayı ZGC'den farklı olarak hangi klasik tekniğiyle yapar?
## A
Nesneleri asla taşımaz; parçalanmayı yalnız serbest blokları birleştirerek yönetir
🤖 Shenandoah da sıkıştıran/taşıyan bir toplayıcıdır; taşımayı eşzamanlı yapar.
## B
Uygulamayı tümüyle durdurup tek seferde sıkıştırır, sonra bariyerleri kaldırır
🤖 Bu düşük gecikme hedefiyle çelişir; Shenandoah taşımayı eşzamanlı yapar.
## C
Referansın kullanılmayan bitlerine renk gömer; yani ZGC ile aynı mekanizmayı kullanır
🤖 Renkli işaretçi ZGC'ye özgüdür; Shenandoah tarihsel olarak farklı bir yol tuttu.
## D
Her nesnenin başlığında bir yönlendirme işaretçisi (Brooks forwarding pointer) tutar
🤖 Doğru: Shenandoah'ın ayırt edici tekniği başlıktaki forwarding pointer'dır.
## Doğru
D
## Açıklama
Shenandoah da ZGC gibi **düşük gecikmeli, eşzamanlı taşıyan** bir toplayıcıdır, ama mekanizması tarihsel olarak farklıdır: her nesnenin başlığında ekstra bir **yönlendirme işaretçisi (Brooks forwarding pointer)** tutar. Nesne taşındığında bu işaretçi yeni konumu gösterir ve erişimler oradan yönlendirilir. ZGC ise ayrı bir başlık alanı yerine **referansın kendi bitlerine renk gömer (colored pointers)** ve yükleme bariyeriyle düzeltir (C yanlış — mekanizma aynı değil). İkisi de taşır (A yanlış) ve ikisi de stop-the-world sıkıştırmadan kaçınır (B yanlış). Sonuçta benzer hedefe (alt-ms duraklama) iki farklı tasarımla ulaşırlar.

# zgc-5 | İleri
## Soru
Aşağıdaki iş yüklerinden hangisi ZGC yerine G1 tercih etmek için en makul gerekçedir?
## A
64 GB yığınla çalışan ve p999 gecikmesini 1 ms altında tutması gereken bir alım-satım (trading) sistemi
🤖 Bu tam ZGC senaryosu; büyük yığın + sıkı kuyruk gecikmesi ZGC ister.
## B
Duraklama toleransı makul, ama toplam verimi (throughput) ve düşük CPU/bellek maliyetini önemseyen dengeli bir batch/servis iş yükü
🤖 Doğru: dengeli, gecikmeye görece toleranslı iş yüklerinde G1 iyi bir varsayılandır.
## C
Yüzlerce GB yığında, çöp toplama duraklamalarının kullanıcıya asla yansımaması gereken bir servis
🤖 Bu da ZGC'nin güçlü olduğu alandır; büyük yığında düşük duraklama.
## D
Duraklamaların binlerce milisaniyeye çıkmasının sorun olmadığı ama en düşük gecikmenin şart olduğu bir sistem
🤖 Kendi içinde çelişkili: hem uzun duraklama serbest hem en düşük gecikme şart olamaz.
## Doğru
B
## Açıklama
ZGC ve G1 seçimi **gecikme (latency) mi yoksa denge/verim mi** sorusuna iner. ZGC, büyük yığın (on-yüzlerce GB) ve **sıkı kuyruk gecikmesi SLA'sı** (A, C) olan sistemlerde parlar — alt-ms duraklamayı yığından bağımsız verir. Ama bu, throughput ve CPU/footprint maliyeti demektir. Duraklamaya makul toleransı olan, buna karşılık **toplam verimi ve düşük kaynak maliyetini** önemseyen dengeli iş yüklerinde (B) G1 daha iyi bir varsayılandır: bölge-tabanlı (region), duraklama hedefi ayarlanabilir ve genel amaçlı olgun bir toplayıcıdır. D seçeneği mantıksal olarak çelişkilidir. Kural: "büyük yığın + sıkı gecikme → ZGC; dengeli genel amaç → G1".

# zgc-6 | Uzman
## Soru
ZGC'nin alt-ms duraklama avantajının bedeli tipik olarak nedir?
## A
Ham verimlilikte (throughput) ve bir miktar CPU/bellek ayak izinde ödün verilmesi
🤖 Doğru: gecikme kazancının karşılığı throughput ve footprint maliyetidir.
## B
Kodun JIT ile derlenememesi; ZGC'nin yalnız yorumlanan (interpreted) modda çalışması
🤖 ZGC ile JIT derleme ilgisizdir; ikisi birlikte sorunsuz çalışır.
## C
Uygulamanın çok iş parçacıklı olamaması; toplamanın tek çekirdeğe bağlanması
🤖 Böyle bir kısıt yok; ZGC yüksek çekirdekli, çok iş parçacıklı sistemler için tasarlandı.
## D
Yığın boyutunun 4 GB ile sınırlanması; büyük yığınlarda kullanılamaması
🤖 Tam tersi; ZGC terabaytlara varan çok büyük yığınlar için ölçeklenir.
## Doğru
A
## Açıklama
Hiçbir GC üç ekseni (duraklama, throughput, footprint) birden en iyi yapamaz. ZGC duraklamayı seçer; bedeli **ham verimlilikte hafif düşüş ve bir miktar CPU/bellek ayak izidir**. Her referans okumasına eklenen **yükleme bariyeri** ve işaretleme/taşımanın eşzamanlı yürütülmesi, uygulamayla CPU paylaşır — bu da mutlak throughput'u G1'in biraz altına çekebilir; renkli işaretçiler ve meta veri de footprint ekler. ZGC yorumlama moduyla sınırlı değildir (B), çok iş parçacıklılığı yasaklamaz (C — tam tersine onun için tasarlandı) ve küçük yığınla sınırlı değildir (D — terabayt ölçeğine çıkar). Doğru cevap, gecikme kazancının karşılığında verilen throughput/footprint ödünüdür.
