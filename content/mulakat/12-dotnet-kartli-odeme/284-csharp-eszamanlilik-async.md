---
slug: csharp-eszamanlilik-async
baslik: "C# Eşzamanlılık ve async/await: Durum Makinesi, Bloklama Tuzağı, Kilit Seçimi"
kademe: İleri
sira: 284
etiketler: 11
---

## Konu anlatımı

Bu dosyada altı mülakat sorusu var. Altısı da açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına C# deneyimi uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur.

Bu bölümün Java tarafı modülde zaten var ve tekrarlanmayacak. Eşzamanlılık temeli `46`, kilitler `47`, görünürlük ve atomiklik `48`, yürütücüler `49`, `CompletableFuture` `50`, eşzamanlı koleksiyonlar `51`, senkronizasyon yardımcıları `52`, sanal iplikler `56`. Burada anlatılan şey o bilginin C# tarafındaki karşılığı ve iki ekosistemin aynı soruna verdiği farklı cevap.

Bu alanın çerçevesini unutma: görüşmeci senin C# geçmişini büyük olasılıkla biliyor. Ölçtüğü şey "bu dili yazmış mı" değil, "kavramı anlamış mı, dili öğrenir mi". Bu yüzden en güçlü cevap dil bilgisi gösterisi değil, iki modeli karşılaştırabilen muhakemedir.

### "C#'ta async/await nasıl çalışır?"

**Soru aslında ne ölçüyor?**

Bu soru tanım istemiyor, model istiyor. Görüşmecinin gerçekte aradığı tek şey şu: bu aday `await` ile iş parçacığı (thread) açmayı karıştırıyor mu. En sık duyulan yanlış cevap "await yeni bir iş parçacığı başlatır" cümlesidir ve bu cümle tek başına eleyicidir. Güçlü aday önce çözülen problemi söyler, sonra mekanizmayı anlatır. En güçlü aday buraya bir karşılaştırma ekler: aynı problemi Java başka bir yoldan çözdü.

**Sözlü cevap iskeleti**

1. **Önce problemi koy.** Bir istek ağdan ya da veri tabanından cevap beklerken iş parçacığı hiçbir şey yapmaz. İşletim sistemi iş parçacığı pahalı bir kaynaktır; boş beklemek israftır.
2. **Mekanizmayı söyle.** Derleyici `async` metodu bir durum makinesine (state machine) çevirir. `await` noktasında metot geri döner, iş parçacığı havuza serbest bırakılır. Sonuç geldiğinde kalan iş sürdürme (continuation) olarak çalıştırılır.
3. **Yanlış anlamayı baştan kapat.** Bekleme sırasında ayrılmış bir iş parçacığı yoktur. İş parçacığı ayıran şey `await` değil, `Task.Run` gibi açık çağrılardır. Ağ beklemesi işlemci işi değildir; onu bir iş parçacığına vermek kazanç değil kayıptır.
4. **Java karşılığını kur.** `CompletableFuture` zinciri aynı niyeti taşır: sonucu bloklayarak bekleme, sonuç gelince ne yapılacağını bırak (`50`). Fark okunabilirliktedir; C# aynı zinciri sıradan sıralı kod gibi yazdırır.
5. **Asıl karşılaştırmayı yap — bu cevabın en değerli cümlesidir.** Java 21 ile sanal iplikler kalıcılaştı ve **bloklayan kodu ucuzlattı**: kodun görünüşü değişmez, beklemenin bedeli düşer (`56`). C# ise **bloklamayan kodu dile gömdü**: bekleme bedeli aynı kalır, kod bloklamayacak biçimde yazılır. İki farklı çözüm, aynı sorun.
6. **Bedeli de söyle.** C# modelinde `async` çağrı zinciri boyunca yayılır; imzalar değişir, "baştan sona async" bir disiplin hâline gelir. Java modelinde imza değişmez ama kazanç çalışma zamanının desteğine bağlıdır. İkisinde de bedava öğle yemeği yoktur.

```svg
<svg viewBox="0 0 660 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ayni sorun iki cozum: Java sanal iplik ile bloklamayi ucuzlatir, C sharp async await ile bloklamayi kaldirir">
  <defs>
    <marker id="csas-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı sorun, iki çözüm: bekleyen iş parçacığı bir israftır</text>
  <rect x="14" y="34" width="632" height="58" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Ortak sorun</text>
  <text x="330" y="76" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Ağ, disk ve veri tabanı beklemesinde iş parçacığı hiçbir iş yapmaz; ama pahalı olduğu için sayısı sınırlıdır.</text>
  <line x1="180" y1="94" x2="180" y2="108" stroke="#5D7789" stroke-width="1.4" marker-end="url(#csas-ok)"/>
  <line x1="490" y1="94" x2="490" y2="108" stroke="#5D7789" stroke-width="1.4" marker-end="url(#csas-ok)"/>
  <rect x="14" y="112" width="310" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="133" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Java 21 · sanal iplik</text>
  <text x="169" y="154" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Bloklayan kodu ucuzlattı</text>
  <text x="169" y="176" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kod sıradan ve bloklayan kalır</text>
  <text x="169" y="196" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iplik ucuzlar, milyonlarca olabilir</text>
  <text x="169" y="216" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">metot imzası değişmez</text>
  <text x="169" y="238" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bedel: kazanç çalışma zamanına bağlıdır</text>
  <text x="169" y="256" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ayrıntı için 56 ve 50</text>
  <rect x="336" y="112" width="310" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="491" y="133" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">C# · async / await</text>
  <text x="491" y="154" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Bloklamayan kodu dile gömdü</text>
  <text x="491" y="176" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">derleyici durum makinesi üretir</text>
  <text x="491" y="196" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">await noktasında iplik serbest kalır</text>
  <text x="491" y="216" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sonuç gelince sürdürme çalışır</text>
  <text x="491" y="238" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bedel: async çağrı zinciri boyunca yayılır</text>
  <text x="491" y="256" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">imzalar değişir, disiplin gerekir</text>
  <rect x="14" y="276" width="632" height="56" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="297" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">İki modelde de aynı hata: bekleyen işi bir iş parçacığına vermek</text>
  <text x="330" y="318" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">await yeni iplik açmaz; iplik açan şey Task.Run gibi açık çağrılardır. Ağ beklemesi işlemci işi değildir.</text>
</svg>
```

**Elenme sebepleri**

- "await yeni bir iş parçacığı açar." Bu soruda en hızlı eleyen cümledir; modelin tamamını yanlış kurar.
- "async kodu hızlandırır." Tek bir isteğin süresi kısalmaz; kazanç aynı donanımda daha çok isteğe hizmet etmektir.
- İşlemci yoğun işi `async` yapmak. Beklemesi olmayan işte kazanç yoktur; asıl araç paralellik ve ayrı bir yürütücüdür.
- Yalnız sözdizimi anlatmak. Durum makinesi ve sürdürme kavramı geçmiyorsa cevap ezber gibi duyulur.
- Çözülen problemi hiç söylememek. Mekanizmayı bilip niye var olduğunu söyleyememek yarım cevaptır.
- "Java'da bunun karşılığı yok" demek. Vardır ve iki tanedir; bu cümle Java tarafındaki kıdemi de zayıf gösterir.
- Sanal iplik ile async/await'i aynı şey saymak. Amaç ortaktır ama biri ipliği ucuzlatır, diğeri iplik tüketimini kaldırır.

**Kendi hikâyen / dürüst köprü**

Burada C# hikâyesi uydurma. Dil deneyimini olduğu gibi söyle, sonra kavramın Java'daki karşılığındaki gerçek deneyimine köprü kur (`256`). Şu sorular o köprünün malzemesini çıkarır:

- Hangi projede eşzamansız çağrı kurdun? `CompletableFuture`, `@Async`, reaktif bir istemci ya da mesaj tüketicisi var mıydı?
- Bloklayan bir çağrı yüzünden bir darboğaz yaşadın mı? Belirti neydi: yanıt süresinde tırmanma mı, zaman aşımı mı?
- Sanal iplikleri denedin mi ya da okudun mu? Denemediysen bunu dürüstçe söyle; okuduğunu anlatmak da bir cevaptır.
- Yürütücü havuzu ayarı yaptın mı? Havuz boyutunu neye göre seçtin, hangi ölçüme baktın?
- Dış servis çağrısı için zaman aşımı ve yeniden deneme koydun mu? Bu, eşzamansız düşünmenin somut kanıtıdır.
- İki bağımsız çağrıyı paralel yapıp sonuçlarını birleştirdiğin bir yer oldu mu? Kazancı ölçtün mü?

Bu deneyimlerden hangisi varsa onu anlat, sonra tek cümleyle bağla: kavram aynı, sözdizimi farklı, öğrenilecek olan sözdizimi.

### "`.Result` ve `.Wait()` kullanmak neden tehlikeli?"

**Soru aslında ne ölçüyor?**

Bu soru bir tuzak sorusu değil, olgunluk sorusudur. Görüşmeci adayın kuralı ezberden söyleyip söylemediğine bakar. Zayıf cevap "kilitlenme olur" deyip durur. Güçlü cevap iki ayrı arıza biçimini ayırır ve hangisinin nerede görüldüğünü söyler. Bu ayrım önemlidir, çünkü sunucu tarafında asıl tehlike kilitlenme değil, sessizce yayılan havuz açlığıdır.

**Sözlü cevap iskeleti**

1. **Kuralı en başta koy.** Eşzamansız kodun ortasında bloklanmaz. Kural şudur: baştan sona async. Zincirin bir yerinde bloklarsan zincirin tamamının faydası gider.
2. **Birinci arızayı anlat: kilitlenme (deadlock).** Sürdürmenin belli bir bağlama dönmesi gereken ortamlarda, o bağlamı zaten bloklayan çağrı tutuyordur. Sürdürme bağlamı bekler, bağlam sürdürmeyi bekler; ikisi de ilerlemez.
3. **İkinci arızayı anlat: iş parçacığı havuzu açlığı (thread pool starvation).** Bloklanan iş parçacığı havuza geri dönmez. Yük arttıkça boştaki iş parçacığı kalmaz, istekler kuyrukta bekler, yanıt süresi tırmanır ve zaman aşımları başlar.
4. **İnce noktayı ekle.** Modern sunucu ortamlarında dönülecek bir bağlam genellikle yoktur; bu kilitlenme riskini azaltır ama bloklamayı ortadan kaldırmaz. Havuz açlığı riski aynen sürer.
5. **Java karşılığını kur.** `Future.get()` ile bloklamak ve yürütücü havuzunu tüketmek birebir aynı arızadır (`49`, `50`). Belirtisi de aynıdır: işlemci boş, sistem yavaş.
6. **Meşru istisnayı söyle.** Uygulamanın giriş noktası, konsol programı ya da eşzamansız olamayan bir arayüz sınırı gibi yerlerde bloklama kaçınılmaz olabilir. Kural şudur: bir tane olsun, sınırda olsun, bilinçli olsun.
7. **Teşhisi ver.** Belirti işlemci kullanımında değil, bekleyen iş parçacıklarındadır. Yığın dökümünde bloklanmış çağrıları görmek teşhisi tek adımda bitirir.

```svg
<svg viewBox="0 0 660 326" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Esszamansiz kodun ortasinda bloklamak: kilitlenme ve is parcacigi havuzu acligi">
  <defs>
    <marker id="csas-kirmizi" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Eşzamansız kodun ortasında bloklamanın iki ayrı arızası</text>
  <rect x="14" y="34" width="316" height="182" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · Kilitlenme (deadlock)</text>
  <text x="172" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sürdürmenin belli bir bağlama dönmesi gerekir</text>
  <rect x="34" y="88" width="130" height="36" rx="7" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="99" y="110" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bloklayan çağrı</text>
  <rect x="180" y="88" width="130" height="36" rx="7" fill="#FFFFFF" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="245" y="110" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bekleyen sürdürme</text>
  <line x1="166" y1="100" x2="178" y2="100" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#csas-kirmizi)"/>
  <line x1="178" y1="114" x2="166" y2="114" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#csas-kirmizi)"/>
  <text x="172" y="146" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bağlamı tutan taraf sonucu bekler</text>
  <text x="172" y="164" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sonuç ise bağlamın boşalmasını bekler</text>
  <text x="172" y="188" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek bağlamlı ortamlarda görülür</text>
  <text x="172" y="206" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bağlam yoksa bu risk azalır</text>
  <rect x="344" y="34" width="302" height="182" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="495" y="55" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">2 · Havuz açlığı (starvation)</text>
  <text x="495" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bloklanan iş parçacığı havuza dönmez</text>
  <circle cx="392" cy="98" r="8" fill="#B23B3B"/><circle cx="424" cy="98" r="8" fill="#B23B3B"/><circle cx="456" cy="98" r="8" fill="#B23B3B"/><circle cx="488" cy="98" r="8" fill="#B23B3B"/><circle cx="520" cy="98" r="8" fill="#B23B3B"/><circle cx="552" cy="98" r="8" fill="#B23B3B"/><circle cx="584" cy="98" r="8" fill="#5D7789"/>
  <text x="495" y="126" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kırmızı: bloklanmış · gri: kalan tek boş iplik</text>
  <text x="495" y="148" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istekler kuyrukta bekler</text>
  <text x="495" y="166" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yanıt süresi tırmanır, zaman aşımı başlar</text>
  <text x="495" y="188" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">belirti işlemcide değil beklemededir</text>
  <text x="495" y="206" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bağlam olmayan ortamda da olur</text>
  <rect x="14" y="228" width="632" height="84" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="249" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kural: baştan sona async</text>
  <text x="330" y="270" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Zincirin bir halkasında bloklarsan zincirin tamamının faydası gider.</text>
  <text x="330" y="290" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Meşru istisna sınırdadır: uygulama giriş noktası ya da eşzamansız olamayan bir arayüz.</text>
  <text x="330" y="306" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Java karşılığı aynıdır: Future.get() ile bloklamak ve havuzu tüketmek (49, 50)</text>
</svg>
```

**Elenme sebepleri**

- Yalnız "kilitlenme olur" deyip durmak. Sunucu tarafındaki asıl arıza olan havuz açlığı atlanmış olur.
- "Bağlam yoksa bloklamak zararsızdır" demek. Kilitlenme riski azalır ama iş parçacığı yine serbest kalmaz.
- Havuzu büyüterek çözmeyi önermek. Belirti ötelenir, kök neden kalır ve maliyet kalıcılaşır.
- Bir çağrı biçimini diğerinden güvenli sanmak. Bloklama bloklamadır; değişen yalnız istisnanın nasıl sarmalandığıdır.
- Teşhis yolunu söyleyememek. Yığın dökümü ve bekleyen iş parçacığı sayısı bu arızanın en doğrudan kanıtıdır.
- Java'daki aynı arızayı bağlamamak. `Future.get()` deneyimi varsa söylenmemesi kayıp, çünkü cevabı gerçek deneyime bağlar.
- Hiçbir meşru istisna tanımamak. Mutlak kural, uygulamanın giriş noktasını nasıl yazacağını bilmediğini gösterir.

**Kendi hikâyen / dürüst köprü**

Bu soruda anlatılacak vaka Java tarafından gelir ve tam olarak sayılır. Şu sorular onu çıkarır:

- Bir yürütücü havuzunun tükendiği bir olay yaşadın mı? Belirti neydi, nasıl fark edildi?
- Dış servis yavaşladığında sistemin tamamı yavaşladı mı? Kaskad etkisini gördün mü?
- Havuz boyutunu ya da bağlantı havuzunu ayarladın mı? Hangi ölçüme bakarak karar verdin?
- Yığın dökümü aldın mı? Bloklanmış iş parçacıklarını orada gördün mü?
- Zaman aşımı koymadığın bir çağrı yüzünden bir şey öğrendin mi? Bu, en anlatılabilir hikâyelerden biridir.
- Devre kesici ya da bölmeleme (bulkhead) benzeri bir koruma kurdun mu?

### "`Task` ile `ValueTask` arasındaki fark nedir?"

**Soru aslında ne ölçüyor?**

Bu soru bir bilgi sorusu gibi başlar ama karar sorusudur. Görüşmeci farkı saymanı değil, ne zaman kullanılacağını duymak ister. Ayrım şurada çıkar: aday `ValueTask`'in bir iyileştirme aracı olduğunu ve bedelinin ne olduğunu söylüyor mu. Bedelini söylemeden savunan aday, ölçmeden iyileştirme yapan mühendis izlenimi bırakır. Bu soru ayrıca Java ile C# arasındaki gerçek bir dil farkını konuşma fırsatıdır.

**Sözlü cevap iskeleti**

1. **`Task`'i tanımla.** `Task` bir sınıftır; yığında (heap) nesne olarak yer ayrılır. Her eşzamansız çağrı bir ayırma (allocation) üretir ve o nesne sonunda çöp toplayıcıya iş çıkarır.
2. **Problemi göster.** Çoğu çağrı aslında hazır sonuç döndürür: önbellek isabeti, arabellekte hazır bekleyen veri, erken çıkan bir doğrulama. Sıcak bir kod yolunda bu ayırmalar birikir.
3. **`ValueTask`'i tanımla.** `ValueTask` bir değer tipidir (struct). Sonuç zaten hazırsa değeri doğrudan taşır ve ayırma yapılmaz. Kazanç bu noktadadır.
4. **Bedeli açıkça söyle.** `ValueTask` tek kez tüketilmek üzere tasarlanmıştır. İki kez `await` edilmesi, saklanıp sonra beklenmesi ya da aynı anda iki yerde kullanılması doğru değildir; gerekirse `AsTask` ile dönüştürülür.
5. **Kuralı ver.** Varsayılan `Task`'tir. `ValueTask` yalnız ölçülmüş, sıcak ve çoğunlukla eşzamanlı tamamlanan yollarda kullanılır. Ölçmeden yapılan bu değişiklik kazanç değil hata yüzeyi üretir.
6. **Java farkını kur.** Java'da doğrudan karşılığı yoktur. Java'da her `CompletableFuture` bir nesnedir; kullanıcının tanımladığı değer tipi kavramı yoktur. C#'ta `struct` gerçek bir dil özelliğidir ve bu, "kelime farkı" değil gerçek bir fark grubuna girer (`281`).

**Elenme sebepleri**

- Farkı yalnız "daha hızlı" diye özetlemek. Nerede ve neden hızlı olduğu söylenmezse cevap reklam gibi duyulur.
- Bedelini hiç anmamak. Tek kez tüketilme kısıtını söylemeyen aday, aracın tuzağını bilmiyor sayılır.
- Her yerde `ValueTask` kullanmayı önermek. Ölçüm yoksa bu bir iyileştirme değil, gereksiz risktir.
- Değer tipi ile referans tipi ayrımını kuramamak. Bu ayrım cevabın çekirdeğidir; kurulmazsa gerisi ezberdir.
- "Java'da da aynısı var" demek. Yoktur; yanlış eşleme, doğru eşlemelerin güvenilirliğini de düşürür.
- Ölçüm yolunu söylememek. Ayırma sayısını ve çöp toplama davranışını ölçmeden karar verilmez.

**Kendi hikâyen / dürüst köprü**

Bu soruda C# hikâyesi zaten beklenmez; beklenen şey ölçerek iyileştirme alışkanlığıdır. Şu sorular onu çıkarır:

- Java tarafında ayırma ya da çöp toplama kaynaklı bir sorun yaşadın mı? Nasıl fark ettin?
- Sıcak bir kod yolunu ölçüp iyileştirdiğin bir vaka var mı? Öncesi ve sonrası sayıları neydi?
- Bir iyileştirmeyi ölçüm göstermediği için geri aldığın oldu mu? Bu, güçlü bir olgunluk cümlesidir.
- Küçük kazanç için okunabilirlikten vazgeçmeyi reddettiğin bir karar var mı?
- Değer tipi kavramını bir yerde konuştun mu? Yoksa dürüstçe "C#'ta yeni öğrendiğim gerçek bir fark" de.

### "`ConfigureAwait(false)` ne işe yarar, nerede kullanılır?"

**Soru aslında ne ölçüyor?**

Bu, dosyanın en ayrıştırıcı sorusudur. Çünkü herkesin bildiği bir kural vardır ve o kural bugün her yerde aynı ağırlıkta geçerli değildir. Görüşmeci adayın kuralı mı yoksa kuralın gerekçesini mi bildiğine bakar. Gerekçeyi bilen aday nerede işe yaradığını, nerede etkisinin sınırlı kaldığını ayırabilir. Bu ayrımı yapabilmek küçük bir ayrıntı gibi görünür ama çok şey söyler.

**Sözlü cevap iskeleti**

1. **Önce kavramı tanımla.** Eşzamanlama bağlamı (synchronization context), "beklemeden sonra kalan iş nerede çalışsın" sorusunun cevabını tutan bir kuraldır.
2. **Neden var olduğunu söyle.** Arayüz uygulamalarında kalan işin belirli bir iş parçacığına dönmesi zorunludur; bileşenlere yalnız oradan dokunulabilir. Bağlam bunu sağlar.
3. **Çağrının ne yaptığını söyle.** `ConfigureAwait(false)`, "kalan işi geldiği yere döndürme, uygun olan yerde çalıştır" demektir. Böylece dönüş maliyeti ve dönüş beklemesi ortadan kalkar.
4. **Nerede önerildiğini söyle.** Kütüphane kodunda önerilir, çünkü kütüphane kendisini kimin çağırdığını bilmez. Çağıranın bağlamına bağımlı olmamak kütüphaneyi hem hızlı hem güvenli tutar.
5. **İnce ayrımı ver — bu cevabın ayrıştırıcı cümlesidir.** ASP.NET Core'da dönülecek böyle bir bağlam bulunmaz. Bu yüzden uygulama kodunda etkisi sınırlıdır ve her satıra eklemek çoğu zaman gürültüdür. Kütüphane kodunda ise gerekçe hâlâ ayaktadır.
6. **Sınırı çiz.** Bu çağrı bloklamayı güvenli hâle getirmez. `.Result` ile bloklarsan havuz açlığı riski aynen sürer; ilgili tek etkisi kilitlenme ihtimalini azaltmaktır.
7. **Temkinli konuş.** Ayrıntı sürüme ve çalışılan çatıya göre değişebilir. Doğru duruş şudur: davranışı dokümandan teyit ederim, kuralı körü körüne uygulamam.

**Java karşılığı**

Java'da bu isimde bir çağrı yoktur ama karar vardır. `CompletableFuture` zincirinde `thenApply` ile `thenApplyAsync(fn, executor)` arasındaki seçim tam olarak aynı soruyu sorar: kalan iş hangi iş parçacığında çalışsın (`50`). Kavram aynı, kelime farklı.

**Elenme sebepleri**

- "Her yere eklerim, performans artar" demek. Gerekçeyi değil kuralı ezberlediğini gösterir.
- Eşzamanlama bağlamını hiç tanımlamamak. Kavram kurulmadan çağrının ne yaptığı anlatılamaz.
- Bloklamayı güvenli hâle getirdiğini sanmak. Havuz açlığı bu çağrıyla ortadan kalkmaz.
- Kütüphane kodu ile uygulama kodu ayrımını yapmamak. Bu soruda ayrımın kendisi cevabın değeridir.
- Sürüme duyarlı bir davranışı kesin dille söylemek. Ölçülmemiş kesinlik mülakatta güven değil şüphe üretir.
- "Bu artık tamamen gereksiz" demek. Uygulama kodunda etkisi sınırlıdır, kütüphane kodunda gerekçe sürer.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâye yerine duruş anlatılır. Şu sorular malzemeyi verir:

- Java'da bir sürdürmenin hangi yürütücüde çalışacağını bilinçli seçtiğin oldu mu?
- Bir kural ezberlendiği için yanlış yerde uygulandığına şahit oldun mu? Sonucunda ne oldu?
- Kütüphane yazdığın ya da ortak modül sahiplendiğin bir iş oldu mu? Çağıranı bilmemenin getirdiği kısıtları düşündün mü?
- Bir davranışı dokümandan teyit ederek tartışmayı bitirdiğin bir an var mı? Bu duruş burada tam yerindedir.

### "`lock` ile `SemaphoreSlim` arasında nasıl seçim yaparsın?"

**Soru aslında ne ölçüyor?**

Bu soru iki şeyi birden ölçer. Birincisi, adayın karşılıklı dışlama (mutual exclusion) kavramını bilip bilmediği; bu kısım kolaydır. İkincisi ve asıl ölçülen, adayın eşzamansız kod ile senkron kilitlerin neden birlikte çalışmadığını anlayıp anlamadığıdır. Bu, C#'a geçen Java geliştiricisinin ilk çarptığı duvardır ve tam da bu yüzden iyi bir sorudur.

**Sözlü cevap iskeleti**

1. **`lock`'u tanımla.** Bir nesne üzerinden karşılıklı dışlama sağlar, aynı iş parçacığı için tekrar girilebilir ve senkrondur. **Java karşılığı `synchronized`** ile birebir aynı fikirdir (`47`).
2. **Kısıtı söyle ve gerekçelendir.** `lock` gövdesinde `await` kullanılamaz. Gerekçe önemlidir: kilit sahipliği iş parçacığına bağlıdır, oysa beklemeden sonra kalan iş başka bir iş parçacığında sürebilir.
3. **`SemaphoreSlim`'i getir.** Eşzamansız bekleme sağlar. Sayısı bir verildiğinde karşılıklı dışlama olur ve kritik bölüm bir bekleme içerebilir. **Java karşılığı** `Semaphore` ve `ReentrantLock` ailesidir (`52`, `46`).
4. **Bedelini söyle.** Tekrar girilebilir değildir; aynı iş parçacığı ikinci kez girmeye çalışırsa kendini kilitler. Bırakma işlemi `finally` içinde yapılır, yoksa bir hata tüm bölümü kalıcı olarak kapatır.
5. **Seçim ölçütünü ver.** Kritik bölüm senkron ve kısaysa `lock`. Bölüm içinde eşzamansız bir çağrı varsa `SemaphoreSlim`. Sayaç bir değil de birkaç ise, yani eşzamanlı çağrı sayısını sınırlıyorsan yine `SemaphoreSlim`.
6. **Asıl tasarım kuralını ekle.** Kilit tutarken ağ ya da veri tabanı çağrısı yapılmaz. Kritik bölüm ne kadar kısa olursa çekişme o kadar azalır; en iyi kilit hiç kurulmayan kilittir.
7. **Kapsam uyarısını yap.** İkisi de süreç içidir. Uygulama birkaç kopya (replica) hâlinde koşuyorsa hiçbiri kopyalar arasında bir şey garanti etmez.

**Elenme sebepleri**

- `lock` gövdesinde bekleme yapılabileceğini söylemek. Bu, eşzamansız model ile kilit sahipliği ilişkisini hiç kurmadığını gösterir.
- Kilit tutarken sonucu bloklayarak beklemek. Aynı anda hem iş parçacığını hem kritik bölümü tutan en pahalı çözümdür.
- `SemaphoreSlim`'in tekrar girilebilir sandığını belli etmek. İkinci giriş denemesi bölümü kalıcı olarak kilitler.
- Bırakma işlemini `finally` dışında yapmak. Tek bir hata bölümü sonsuza kadar kapatır ve belirtisi kalıcı takılmadır.
- Kritik bölüm içinde uzun süren çağrı yapmayı sorun görmemek. Çekişme burada darboğaza dönüşür.
- Kapsam uyarısını hiç yapmamak. Çok kopyalı ortamda ikisinin de bir şey garanti etmediği söylenmezse cevap eksik kalır.
- Seçimi ölçütsüz yapmak. "Ben hep şunu kullanırım" cümlesi karar değil, alışkanlık anlatır.

**Kendi hikâyen / dürüst köprü**

Bu soruda anlatılacak vaka Java tarafından gelir ve tam olarak sayılır (`256`). Şu sorular onu çıkarır:

- Hangi kodda karşılıklı dışlama gerekti? Paylaşılan bir sayaç mı, önbellek mi, dosya mı, bir dış kaynak mı?
- `synchronized` mi kullandın, açık bir kilit mi? Seçimi neye göre yaptın?
- Eşzamanlı çağrı sayısını sınırlaman gereken bir yer oldu mu? Dış servise giden istekleri kısmak gibi.
- Kilit yüzünden performans düştüğü bir an oldu mu? Kritik bölümü kısaltarak mı çözdün?
- Kilit tutarken uzun süren bir çağrı yapıldığını fark ettiğin bir kod incelemesi oldu mu?
- Bir kilidin bırakılmadığı bir hata yaşadın mı? Belirti neydi ve nasıl teşhis edildi?

### "Bir yarış koşulunu (race condition) nasıl tespit eder ve çözersin?"

**Soru aslında ne ölçüyor?**

Bu sorunun cevabı çoğu adayda "kilit koyarım" ile başlar ve orada biter. Ayrım bir basamak yukarıda çıkar: aday garantiyi hangi sınırda kurduğunu soruyor mu. Süreç içi bir yarış ile kopyalar arası bir yarış aynı araca sığmaz. Bu ilan bir ödeme sistemidir; orada çift kayıt para demektir. Bu yüzden kapsam sorusunu soran aday burada belirgin biçimde öne çıkar.

**Sözlü cevap iskeleti**

1. **Önce tanımı net koy.** Yarış koşulu, sonucun işlerin çakışma sırasına göre değişmesidir. En sık biçimi oku-değiştir-yaz üçlüsünün atomik olmamasıdır.
2. **Kapsam sorusunu sor — bu cevabın belkemiğidir.** Yarış tek süreç içindeki iş parçacıkları arasında mı, yoksa ayrı kopyalar ve ayrı süreçler arasında mı? Cevap aracı belirler.
3. **Tek süreç içindeyse araçları sırala.** Tek değişkenlik sayaç ve bayraklarda `Interlocked` yeter; **Java karşılığı atomik sınıflardır** (`48`). Paylaşılan koleksiyonda eşzamanlı koleksiyonlar kullanılır (`51`). Birden çok değişkeni birlikte tutan bir değişmez varsa kilit gerekir.
4. **Kopyalar arasıysa süreç içi kilidi eleme.** Süreç içi kilit yalnız kendi kopyasını bağlar. Garanti paylaşılan kaynağa taşınır: benzersiz kısıt, koşullu güncelleme, iyimser eşzamanlılık için satır sürümü (`225`) ya da dağıtık kilit.
5. **Tasarımla çözmeyi öne al.** En sağlam çözüm paylaşılan değişebilir durumu kaldırmaktır. İşlemi tekrar edilse de sonucu değişmeyen (idempotent) hâle getirmek, kilit tartışmasını çoğu zaman gereksiz kılar.
6. **Tespiti anlat.** Yarış koşulu testle kanıtlanmaz, yalnız yakalanır. Yük altında tekrarlanan koşu, kod incelemesi ve en önemlisi veri tabanındaki değişmezleri kısıtla zorlamak asıl güvencedir.
7. **Sonucu kilitle.** Bulunan yarışın çözümü bir kısıt, bir alarm ya da bir testle sabitlenir. Yalnız kodu düzeltmek, aynı hatanın yeniden yazılmasını engellemez.

```svg
<svg viewBox="0 0 660 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yaris kosulu icin koruma merdiveni: Interlocked, lock, SemaphoreSlim ve veritabani seviyesi">
  <defs>
    <marker id="csas-adim" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önce kapsamı sor: garanti hangi sınıra kadar geçerli olmalı?</text>
  <rect x="14" y="36" width="152" height="150" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Interlocked</text>
  <text x="90" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek değişken</text>
  <text x="90" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sayaç, bayrak, artırma</text>
  <text x="90" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kapsam: tek süreç</text>
  <text x="90" y="140" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">Java: atomik sınıflar</text>
  <text x="90" y="156" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">(48, 51)</text>
  <text x="90" y="176" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en ucuz araç</text>
  <line x1="168" y1="110" x2="182" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#csas-adim)"/>
  <rect x="184" y="36" width="152" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="260" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">lock</text>
  <text x="260" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çok değişkenli değişmez</text>
  <text x="260" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">senkron ve kısa bölüm</text>
  <text x="260" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kapsam: tek süreç</text>
  <text x="260" y="140" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">Java: synchronized (47)</text>
  <text x="260" y="162" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">gövdesinde await olmaz</text>
  <text x="260" y="178" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tekrar girilebilir</text>
  <line x1="338" y1="110" x2="352" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#csas-adim)"/>
  <rect x="354" y="36" width="152" height="150" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="430" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">SemaphoreSlim</text>
  <text x="430" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bölüm içinde await var</text>
  <text x="430" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sayı 1 ise karşılıklı dışlama</text>
  <text x="430" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kapsam: tek süreç</text>
  <text x="430" y="140" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">Java: Semaphore (52, 46)</text>
  <text x="430" y="162" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">tekrar girilebilir değil</text>
  <text x="430" y="178" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bırakma finally içinde</text>
  <line x1="508" y1="110" x2="522" y2="110" stroke="#5D7789" stroke-width="1.4" marker-end="url(#csas-adim)"/>
  <rect x="524" y="36" width="122" height="150" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="585" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Veri tabanı</text>
  <text x="585" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">benzersiz kısıt</text>
  <text x="585" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">koşullu güncelleme</text>
  <text x="585" y="114" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">satır sürümü (225)</text>
  <text x="585" y="136" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">kapsam: tüm kopyalar</text>
  <text x="585" y="158" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dağıtık kilit de bu</text>
  <text x="585" y="174" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">basamaktadır</text>
  <rect x="14" y="198" width="632" height="60" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="219" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">En sık hata: kopyalar arası yarışı süreç içi kilitle çözmeye çalışmak</text>
  <text x="330" y="240" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Süreç içi kilit yalnız kendi kopyasını bağlar. Üç kopya varsa üç ayrı kilit vardır ve hiçbiri diğerini görmez.</text>
  <rect x="14" y="270" width="632" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="291" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">En sağlam çözüm kilit değil, tasarımdır</text>
  <text x="330" y="312" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Paylaşılan değişebilir durumu kaldır; işlemi tekrar edilse de sonucu değişmeyen (idempotent) hâle getir.</text>
</svg>
```

**Elenme sebepleri**

- Doğrudan "kilit koyarım" demek. Kapsam sorusu sorulmadığı için cevap çok kopyalı ortamda yanlış olur.
- Süreç içi kilidin kopyalar arasında geçtiğini sanmak. Ödeme akışında bu hata doğrudan çift kayıt üretir.
- Yalnız `lock` bilmek. Tek değişken için kilit kullanmak gereksiz çekişme, koleksiyon için yetersiz koruma verir.
- Veri tabanı seviyesindeki garantileri hiç anmamak. Benzersiz kısıt ve satır sürümü en ucuz ve en kesin araçlardır.
- Idempotency kavramını atlamak. Yeniden deneme olan her sistemde kilit tek başına yetmez.
- "Testle bulurum" demek. Yarış koşulu testte görünmeyebilir; kısıt ve değişmez denetimi asıl güvencedir.
- Kilit tutarken uzun süren çağrı yapmayı sorun görmemek. Bu, çekişmeyi darboğaza çevirir.

**Kendi hikâyen / dürüst köprü**

Bu soruda anlatacağın vaka büyük ihtimalle Java tarafındadır ve tam olarak geçerlidir (`256`). Şu sorular onu çıkarır:

- Çift kayıt ya da çift işlem yaşandığı bir olay oldu mu? Nasıl fark edildi: mutabakat farkı mı, müşteri şikâyeti mi?
- Uygulama birden çok kopya hâlinde mi koşuyordu? Süreç içi bir çözüm denendi mi, sonucu ne oldu?
- Benzersiz kısıt ya da sürüm alanı kullandın mı? Kısıt ihlalini nasıl karşıladın?
- Bir işi tekrar edilse de sonucu değişmeyen hâle getirmek zorunda kaldın mı? Anahtarı neydi?
- Zamanlanmış bir işin iki kopyada birden koştuğu oldu mu? Nasıl engellendi?
- Bir kilit yüzünden performans düştü mü? Kritik bölümü kısaltarak mı, kapsamı değiştirerek mi çözüldü?

### Bu bölümden çıkan ders

Bu altı sorunun tamamı tek bir eksende toplanır: bekleme pahalıdır ve onu kimin ödediğini bilmek gerekir. `async/await` beklerken iş parçacığı harcamamanın yoludur, `.Result` ise o kazancı tek satırda geri verir. `ValueTask` aynı disiplinin bir alt basamağıdır ve ölçüm olmadan kullanılmaz. `ConfigureAwait(false)` kuralı ezberlenerek değil, gerekçesi bilinerek uygulanır; kütüphane kodunda yeri vardır, uygulama kodunda etkisi sınırlıdır. Kilit seçiminde belirleyici olan araç değil kapsamdır: garantinin nereye kadar geçmesi gerektiğini sormayan aday, üç kopyalı bir ödeme servisinde çift kayıt üretir. Ve bu bölümün mülakatta en çok değer getiren tek cümlesi şudur: Java 21 sonrası Java bloklayan kodu ucuzlattı, C# ise bloklamayan kodu dile gömdü. Bu cümleyi kurabilen aday iki ekosistemi de anladığını gösterir ve dil sorusu bir eksik olmaktan çıkıp bir güç kartına dönüşür. Kalan her şey sözdizimidir; sözdizimi öğrenilir, muhakeme öğrenilmez.

# csas-1 | Orta
## Soru
Bir aday şöyle diyor: "`await` satırına gelindiğinde yeni bir iş parçacığı açılır ve kalan iş orada sürer." Bu ifadenin doğrusu nedir?
## A
İfade doğrudur; her `await` çağrısı iş parçacığı havuzundan bir iş parçacığı ayırır ve kalan işi orada yürütür
🤖 `await` iş parçacığı ayırmaz; ayıran şey `Task.Run` gibi açık çağrılardır.
## B
İfade yalnız işlemci yoğun işler için doğrudur; ağ çağrılarında `await` tamamen eşzamanlı biçimde çalışır
🤖 tam tersi; eşzamansızlığın asıl kazancı ağ ve disk beklemesindedir.
## C
İfade doğrudur ama yalnız ilk `await` için geçerlidir; sonraki bekleme noktaları aynı iş parçacığında kalır
🤖 böyle bir kural yoktur; kalan işin nerede koştuğu bağlama göre değişir.
## D
İfade yanlıştır; derleyici bir durum makinesi üretir, bekleme noktasında metot geri döner ve iş parçacığı serbest kalır
🤖 Doğru: bekleme sırasında ayrılmış bir iş parçacığı yoktur.
## Doğru
D
## Açıklama
`async` bir metot, derleyici tarafından bir **durum makinesine** (state machine) çevrilir. `await` noktasına gelindiğinde metot geri döner ve o iş parçacığı havuza bırakılır. Sonuç hazır olduğunda kalan iş bir sürdürme (continuation) olarak çalıştırılır. Bekleme boyunca hiçbir iş parçacığı ayrılmış değildir; asıl kazanç budur. A şıkkı en sık duyulan yanlıştır ve modelin tamamını ters kurar. İş parçacığı ayıran şey `Task.Run` gibi açık çağrılardır; ağ beklemesi işlemci işi değildir, ona iş parçacığı vermek kazanç değil kayıptır. B şıkkı ilişkiyi tersine çevirir. Java tarafındaki karşılık `CompletableFuture` zinciridir: sonucu bloklayarak bekleme, gelince ne yapılacağını bırak (`50`). Asıl kıyas ise şudur: Java 21 sanal ipliklerle **bloklayan kodu ucuzlattı**, C# ise `async/await` ile **bloklamayan kodu dile gömdü** (`56`). İki farklı çözüm, aynı sorun. C# modelinin bedeli, `async`'in çağrı zinciri boyunca yayılması ve imzaları değiştirmesidir.

# csas-2 | İleri
## Soru
ASP.NET Core üzerinde koşan bir serviste, eşzamansız bir çağrının sonucu `.Result` ile alınıyor. Yük arttığında görülmesi en olası belirti hangisidir?
## A
İşlemci kullanımı tavana çıkar, çünkü bloklanan iş parçacıkları sonucu meşgul döngü içinde yoklayarak bekler
🤖 bloklanan iş parçacığı işlemci yakmaz; belirti beklemede görülür.
## B
İş parçacığı havuzu tükenir; istekler kuyrukta birikir, yanıt süresi tırmanır ve zaman aşımları başlar
🤖 Doğru: bloklanan iş parçacığı havuza geri dönmez.
## C
Hiçbir sorun çıkmaz; ASP.NET Core'da eşzamanlama bağlamı bulunmadığı için `.Result` kullanımı güvenlidir
🤖 bağlamın olmaması kilitlenmeyi azaltır ama bloklamayı ortadan kaldırmaz.
## D
Çöp toplama süreleri uzar, çünkü tamamlanmamış görev nesneleri bellekte birikir ve öbek sürekli büyür
🤖 asıl sorun bellekte değil, serbest kalmayan iş parçacıklarındadır.
## Doğru
B
## Açıklama
Eşzamansız kodun ortasında bloklamanın iki ayrı arızası vardır ve bunları ayırmak bu soruda ayrıştırıcıdır. Birincisi **kilitlenmedir**: kalan işin belirli bir bağlama dönmesi gereken ortamlarda, o bağlamı zaten bloklayan çağrı tutar ve iki taraf birbirini bekler. İkincisi **iş parçacığı havuzu açlığıdır**: bloklanan iş parçacığı havuza geri dönmez, yük arttıkça boşta iş parçacığı kalmaz. C şıkkı tam da bu ayrımı kaçırdığı için en çekici yanlıştır; bağlamın bulunmaması kilitlenme riskini azaltır ama bloklamayı kaldırmaz, dolayısıyla açlık riski aynen sürer. A şıkkı bloklamayı meşgul bekleme sanır; belirti işlemcide değil beklemededir. Java karşılığı birebir aynıdır: `Future.get()` ile bloklamak yürütücü havuzunu tüketir ve sistem işlemci boşken yavaşlar (`49`, `50`). Doğru duruş "baştan sona async" kuralıdır. Meşru istisna yalnız sınırdadır: uygulamanın giriş noktası ya da eşzamansız olamayan bir arayüz. Teşhis yolu da nettir: yığın dökümünde bloklanmış çağrıları görmek sorunu tek adımda bitirir.

# csas-3 | İleri
## Soru
Sıcak bir kod yolunda `Task` yerine `ValueTask` kullanılmasına karar verildi. Bu tercihin asıl bedeli aşağıdakilerden hangisidir?
## A
`ValueTask` yalnız değer döndürmeyen metotlarda kullanılabilir; sonuç taşıyan metotlarda `Task` kullanmak zorunludur
🤖 sonuç taşıyan genel biçimi vardır; kısıt burada değildir.
## B
`ValueTask` çağrıyı eşzamanlı hâle getirir; beklenen iş artık ayrı bir iş parçacığına devredilemez hâle gelir
🤖 eşzamanlılık modeli değişmez; değişen şey sonucun nasıl taşındığıdır.
## C
Tek kez tüketilmek üzere tasarlanmıştır; iki kez beklenmesi ya da saklanıp sonra beklenmesi doğru değildir
🤖 Doğru: bu yüzden varsayılan tercih `Task` olarak kalır.
## D
Değer tipi olduğu için sonucu her çağrıda kopyalar ve bu yüzden genel olarak `Task`'ten daha çok bellek harcar
🤖 kazanç ayırmanın kalkmasındadır; genel bellek kaybı iddiası yanlıştır.
## Doğru
C
## Açıklama
`Task` bir sınıftır; her eşzamansız çağrı yığında (heap) bir nesne ayırır (allocation). Çoğu çağrı aslında hazır sonuç döndürür: önbellek isabeti, arabellekte bekleyen veri, erken çıkan bir doğrulama. Sıcak bir kod yolunda bu ayırmalar birikir ve çöp toplayıcıya iş çıkarır. `ValueTask` bir **değer tipidir** (struct); sonuç hazırsa değeri doğrudan taşır ve ayırma yapılmaz. Kazanç buradadır, bedeli de bellidir: tek kez tüketilmek üzere tasarlanmıştır. İki kez beklenmesi, saklanıp sonra beklenmesi ya da aynı anda iki yerde kullanılması tanımlı davranış değildir; gerekiyorsa `AsTask` ile dönüştürülür. Bu yüzden kural nettir: varsayılan `Task`'tir, `ValueTask` yalnız ölçülmüş ve çoğunlukla eşzamanlı tamamlanan yollarda kullanılır. Ölçmeden yapılan bu değişiklik kazanç değil hata yüzeyi üretir. Java tarafında doğrudan bir karşılığı yoktur: her `CompletableFuture` bir nesnedir ve kullanıcının tanımladığı değer tipi kavramı Java'da bulunmaz. Bu, "aynı kavram, farklı kelime" değil, gerçek bir dil farkıdır (`281`).

# csas-4 | İleri
## Soru
Bir ekip, ASP.NET Core uygulamasındaki tüm bekleme satırlarına `ConfigureAwait(false)` eklemeyi öneriyor; gerekçe olarak "performans artar" deniyor. En yerinde değerlendirme hangisidir?
## A
ASP.NET Core'da dönülecek bir eşzamanlama bağlamı bulunmadığından etkisi sınırlıdır; asıl yeri kütüphane kodudur
🤖 Doğru: kural körü körüne değil, gerekçesiyle uygulanır.
## B
Öneri doğrudur; her bekleme noktasında bağlam değişimi olduğu için eklenmeyen her satır ölçülebilir bir gecikme üretir
🤖 dönülecek bir bağlam yoksa kaldırılacak bir maliyet de yoktur.
## C
Öneri gereksizdir, çünkü bu çağrı yalnız masaüstü uygulamalarında derlenir ve sunucu tarafında hiç kullanılamaz
🤖 böyle bir kısıt yoktur; çağrı her ortamda geçerlidir.
## D
Öneri doğrudur ve ek kazanç da sağlar; eklendiğinde `.Result` ile bloklamak da güvenli hâle gelmiş olur
🤖 bloklama sürer; iş parçacığı havuzu açlığı riski ortadan kalkmaz.
## Doğru
A
## Açıklama
Eşzamanlama bağlamı (synchronization context), "beklemeden sonra kalan iş nerede çalışsın" sorusunun cevabını tutan kuraldır. Arayüz uygulamalarında kalan işin belirli bir iş parçacığına dönmesi zorunludur; bağlam bunu sağlar. `ConfigureAwait(false)` ise "geldiğin yere dönme, uygun olan yerde çalış" demektir. Bu yüzden asıl yeri **kütüphane kodudur**: kütüphane kendisini kimin çağırdığını bilmez, çağıranın bağlamına bağımlı olmaması onu hem hızlı hem güvenli tutar. ASP.NET Core'da dönülecek böyle bir bağlam bulunmadığı için uygulama kodunda etkisi sınırlıdır; her satıra eklemek çoğu zaman gürültüdür. Bu ince ayrım, kuralı ezberleyen adayı gerekçesini bilenden ayırır. D şıkkı ayrı bir hata yapar: bu çağrı bloklamayı güvenli hâle getirmez, havuz açlığı riski aynen sürer. Java'da bu isimde bir çağrı yoktur ama karar vardır: `thenApply` ile `thenApplyAsync(fn, executor)` seçimi tam olarak aynı soruyu sorar (`50`). Sürüme ve çatıya duyarlı ayrıntılarda doğru duruş şudur: davranış dokümandan teyit edilir.

# csas-5 | Uzman
## Soru
Bir kritik bölümün içinde eşzamansız bir veri tabanı çağrısı yapılması gerekiyor ve bölüme aynı anda tek çağrı girmelidir. Doğru yaklaşım hangisidir?
## A
`lock` bloğu kullanmak ve içindeki eşzamansız çağrıyı `.Result` ile bekleyerek karşılıklı dışlamayı korumak
🤖 kilidi tutarken bloklamak hem havuzu hem kilidi aynı anda tüketir.
## B
Sayısı bir olan `SemaphoreSlim` kullanmak; giriş eşzamansız beklemeyle alınır, bırakma `finally` içinde yapılır
🤖 Doğru: `lock` gövdesinde bekleme yapılamaz.
## C
`lock` bloğu kullanmak; derleyici gövdedeki bekleme satırını görünce kilidi kendiliğinden eşzamansız biçime çevirir
🤖 böyle bir dönüşüm yoktur; o gövdede bekleme kullanılamaz.
## D
Kilit hiç kullanmamak ve tüm çağrıları tek bir iş parçacığına sabitlemek; böylece karşılıklı dışlama kendiliğinden sağlanır
🤖 sabitleme ölçeği öldürür ve çok kopyalı ortamda zaten garanti vermez.
## Doğru
B
## Açıklama
`lock`, bir nesne üzerinden karşılıklı dışlama (mutual exclusion) sağlar; senkrondur ve aynı iş parçacığı için tekrar girilebilir. Java'daki `synchronized` ile aynı fikirdir (`47`). Kritik kısıt şudur: `lock` gövdesinde bekleme yapılamaz. Gerekçesi teknik değil kavramsaldır — kilit sahipliği iş parçacığına bağlıdır, oysa beklemeden sonra kalan iş başka bir iş parçacığında sürebilir. `SemaphoreSlim` bu boşluğu doldurur: eşzamansız bekleme sağlar ve sayısı bir verildiğinde karşılıklı dışlama olur. Java karşılığı `Semaphore` ve `ReentrantLock` ailesidir (`52`, `46`). Bedeli de söylenmelidir: tekrar girilebilir değildir, aynı iş parçacığı ikinci kez girmeye çalışırsa kendini kilitler; bırakma `finally` içinde yapılmazsa tek bir hata bölümü kalıcı olarak kapatır. A şıkkı iki arızayı birleştirir: kilidi tutarken bloklamak hem iş parçacığını hem kritik bölümü aynı anda tutar. Asıl tasarım kuralı ise şudur: kilit tutarken ağ veya veri tabanı çağrısı yapılmaz, kritik bölüm elden geldiğince kısa tutulur (`46`).

# csas-6 | Uzman
## Soru
Bir ödeme servisi üç kopya (replica) hâlinde koşuyor ve aynı işlemin iki kez kaydedildiği görülüyor. Ekip sorunu bir `lock` ekleyerek çözmek istiyor. En yerinde itiraz hangisidir?
## A
`lock` yerine `SemaphoreSlim` kullanılmalı; eşzamansız beklemeyi desteklediği için çift kayıt bu değişiklikle kapanır
🤖 ikisi de süreç içidir; kopya sınırını hiçbiri aşmaz.
## B
`lock` yerine `Interlocked` kullanılmalı; atomik bir işlem olduğu için çift kayıt kendiliğinden engellenmiş olur
🤖 tek değişkeni korur; iki ayrı süreçteki yazmayı düzenlemez.
## C
Kilit doğru araçtır ama kapsamı dardır; blok tüm iş akışını saracak biçimde genişletilirse sorun ortadan kalkar
🤖 kapsamı büyütmek süreç sınırını değiştirmez, yalnız iş hacmini düşürür.
## D
Süreç içi kilit yalnız kendi kopyasını bağlar; garanti veri tabanına taşınmalıdır: benzersiz kısıt, koşullu güncelleme ya da satır sürümü
🤖 Doğru: koruma, paylaşılan kaynağın bulunduğu yerde kurulur.
## Doğru
D
## Açıklama
Bu sorunun cevabı araç seçiminde değil **kapsam** sorusundadır: yarış tek süreç içindeki iş parçacıkları arasında mı, yoksa ayrı kopyalar arasında mı? Burada üç kopya vardır, dolayısıyla süreç içi her araç elenir. `lock`, `SemaphoreSlim` ve `Interlocked` yalnız kendi süreçlerini bağlar; üç kopyada üç ayrı kilit oluşur ve hiçbiri diğerini görmez. C şıkkı kilidi genişletmeyi önerir; bu, süreç sınırını değiştirmediği için sorunu çözmez ve yalnız iş hacmini düşürür. Doğru çözüm garantiyi **paylaşılan kaynağa**, yani veri tabanına taşımaktır: iş anahtarı üzerinde benzersiz kısıt, koşullu güncelleme, iyimser eşzamanlılık için satır sürümü (`225`) ya da gerçekten gerekiyorsa dağıtık kilit. Ödeme akışında bu ayrımın bedeli doğrudan paradır; çift kayıt mutabakat farkı olarak geri döner. Tasarım düzeyinde daha sağlam olan yol, işlemi tekrar edilse de sonucu değişmeyen (idempotent) hâle getirmektir; o zaman kilit tartışması çoğu zaman gereksizleşir. Java tarafındaki karşılıklar aynıdır: atomik sınıflar ve eşzamanlı koleksiyonlar süreç içidir (`48`, `51`).
