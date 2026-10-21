---
slug: javadan-csharpa-gecis-haritasi
baslik: "Java'dan C#'a Geçiş Haritası: Dil Sorusuna Dürüst Cevap, Kavram Eşlemesi ve Geçiş Planı"
kademe: Orta
sira: 281
etiketler: 11
---

## Konu anlatımı

Bu dosya alanın giriş kapısıdır. Önce ilanı bir kez daha okuyalım, çünkü hazırlık stratejisi oradan çıkar. İlanda yalnız iki madde **Zorunlu** işaretlidir: kartlı ödeme takas ve gün sonu süreçlerinde deneyim, ve minimum sekiz yıl geliştirici kıdemi. Dil maddesi "C# dilinde geliştirme yapan" diye yazar ama zorunlu işareti taşımaz. Bu ayrım küçük görünür, aslında her şeyi belirler.

Çünkü iki zorunlu maddenin ikisi de sende var. Takas ve gün sonu bilgisi bu modülde `250`, `252`, `253` ve `254` içinde derinlemesine duruyor. Kıdem ise ana dilinde geçirdiğin yıllarla karşılanır; madde yıl ister, dil adı yazmaz. Boşluk yalnız dil ve ekosistem tarafındadır. Bu alan tam o boşluk için yazıldı.

Bir uyarıyı baştan koyalım. Bu dosya seni C# uzmanı yapmayacak, yapmamalı da. Amacı başkadır: kavramların Java karşılığını zaten bildiğin için, C# tarafını **kavram eşlemesiyle** hızla anlaşılır kılmak. Böyle okuduğunda yeni bir şey öğrenmiyorsun, bildiğini taşıyorsun. En hızlı yol budur ve mülakatta en inandırıcı duyulan da budur.

Ve alanın en katı kuralı: dil deneyimi konusunda uydurma yapılmaz. "Şu projede C# yazdım" cümlesi kurulmaz. Bunun yerine her soruda `256`'daki **dürüst köprü** çalışır: sınırı söyle, komşu deneyimini somutla, farkı adlandır, kapatma planını ver. Bu dosyada dört soru var. Üçü açık uçlu ve doğrudan senin durumunu hedefler; dördüncüsü kıdem maddesiyle ilgili kısa bir bölümdür.

### "C# deneyiminiz nedir?"

**Soru aslında ne ölçüyor?**

Bu, alanın en kritik sorusudur ve bir bilgi sorusu değildir. Görüşmeci özgeçmişini okudu; C# geçmişini zaten biliyor. İK bu pozisyonu sana teklif ettiğine göre dil boşluğu baştan bilinen bir şeydir. O hâlde soru bir tuzak değil, bir doğrulamadır. Ölçtüğü iki şey var. Birincisi, kendi sınırını süslemeden söyleyebiliyor musun. İkincisi, geçişin ne kadar süreceğini gerçekçi tahmin edebiliyor musun. Bu ikisi birlikte tek bir şeyi gösterir: bu adayla çalışırken sürprizle karşılaşır mıyım.

**Sözlü cevap iskeleti**

1. **Sınırı ilk cümlede ver.** "Üretim kodunu C# ile yazmadım." Tek cümle, özür yok, uzatma yok. Özür dilemek boşluğu büyütür; savunmaya geçmek onu mülakatın merkezine taşır (`256`).
2. **Ana dildeki kıdemi somutla.** 2018'den beri Java ve Spring Boot ile üretim kodu yazıyorsun. Somutluk burada tek ölçüttür: hangi problemi, hangi araçla, nereye kadar çözdün.
3. **İki ekosistemin kavramsal yakınlığını göster.** İkisi de yönetilen çalışma zamanı üstünde çalışır; JVM'in karşılığı CLR'dir. İkisinde de çöp toplama (garbage collection), güçlü tipleme ve sınıf temelli nesne yönelimi var. Spring'in `@Service` ve kurucu enjeksiyonu, .NET tarafında `AddScoped` ve kurucu enjeksiyonudur; fark kapsam adlandırmasındadır (`282`).
4. **Gerçekten farklı olanı adlandır.** Cevabın omurgası burasıdır. "İki dil aynı" demek zayıftır. Değer tipi (`struct`), tip silmenin olmaması, uzantı metotları ve özellik (property) Java'da doğrudan karşılığı olmayan şeylerdir. Farkı sayabilen aday, kullanmamış olsa da üzerine düşünmüş demektir.
5. **Takvimli ve gerçekçi bir söz ver.** "İki günde öğrenirim" güven değil hafiflik işaretidir. Söylenecek şey ölçülebilir olmalı: sözdizimi ve araç zinciri kısa sürede oturur, ekosistem alışkanlıkları ve tuzaklar daha uzun sürer. İlk haftalarda kod incelemesinde çok soru soracağını baştan söyle.
6. **Alan bilgisine köprü kur ve orada dur.** Bu ilanda asıl zor madde dil değil, takas ve gün sonu bilgisidir; zorunlu işaretli olan odur ve sende var (`250`, `252`). Dil öğrenilir; bir kartlı ödeme sisteminin gün sonunda nasıl davrandığı kitaptan öğrenilmez.

```svg
<svg viewBox="0 0 660 310" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dil sorusuna durust kopru: bes adim, iki zayif uc ve yasak cumle">
  <defs>
    <marker id="jcs-kopru" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"C# deneyiminiz nedir?" — dürüst köprünün beş adımı</text>
  <rect x="14" y="38" width="114" height="112" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="71" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Sınır</text>
  <text x="71" y="79" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">"Üretim kodunu</text>
  <text x="71" y="94" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">C# ile yazmadım."</text>
  <text x="71" y="115" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">tek cümle, özür yok</text>
  <text x="71" y="132" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">savunmaya geçme</text>
  <line x1="129" y1="94" x2="142" y2="94" stroke="#5D7789" stroke-width="1.4" marker-end="url(#jcs-kopru)"/>
  <rect x="144" y="38" width="114" height="112" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="201" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Kıdem</text>
  <text x="201" y="79" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">2018'den beri</text>
  <text x="201" y="94" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Java ve Spring Boot</text>
  <text x="201" y="115" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">yıl değil, karar anlat</text>
  <text x="201" y="132" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">somut problem ver</text>
  <line x1="259" y1="94" x2="272" y2="94" stroke="#5D7789" stroke-width="1.4" marker-end="url(#jcs-kopru)"/>
  <rect x="274" y="38" width="114" height="112" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="331" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">3 · Yakınlık</text>
  <text x="331" y="79" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">JVM ↔ CLR</text>
  <text x="331" y="94" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">çöp toplama, güçlü tip</text>
  <text x="331" y="115" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">kurucu enjeksiyonu</text>
  <text x="331" y="132" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">ikisinde de kültürdür</text>
  <line x1="389" y1="94" x2="402" y2="94" stroke="#5D7789" stroke-width="1.4" marker-end="url(#jcs-kopru)"/>
  <rect x="404" y="38" width="114" height="112" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.8"/>
  <text x="461" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">4 · Gerçek fark</text>
  <text x="461" y="79" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">değer tipi (struct)</text>
  <text x="461" y="94" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tip silme yok</text>
  <text x="461" y="109" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">uzantı metodu, property</text>
  <text x="461" y="132" text-anchor="middle" font-size="8.5" fill="#B07D20" font-family="system-ui">cevabın omurgası</text>
  <line x1="519" y1="94" x2="532" y2="94" stroke="#5D7789" stroke-width="1.4" marker-end="url(#jcs-kopru)"/>
  <rect x="534" y="38" width="112" height="112" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="590" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">5 · Plan + köprü</text>
  <text x="590" y="79" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">takvimli söz ver</text>
  <text x="590" y="94" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">asıl zor madde:</text>
  <text x="590" y="109" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">takas ve gün sonu</text>
  <text x="590" y="132" text-anchor="middle" font-size="8.5" fill="#25804C" font-family="system-ui">o madde sende var</text>
  <rect x="14" y="164" width="316" height="76" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="185" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Zayıf uç · abartılı özgüven</text>
  <text x="172" y="206" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"İkisi de aynı, iki günde öğrenirim."</text>
  <text x="172" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Tek ayrıntı sorusuyla çöker.</text>
  <rect x="344" y="164" width="302" height="76" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="185" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Zayıf uç · kuru ret</text>
  <text x="495" y="206" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"C# bilmiyorum." deyip susmak.</text>
  <text x="495" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Dürüsttür ama geriye yalnız eksik kalır.</text>
  <rect x="14" y="252" width="632" height="46" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="272" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Kurulmayacak cümle: "şu projede C# yazdım"</text>
  <text x="330" y="290" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Uydurmanın maliyeti asimetriktir: kazanılan tek soru, kaybedilen mülakatın tamamıdır.</text>
</svg>
```

**Elenme sebepleri**

- "Java ile C# neredeyse aynı, hafta sonunda öğrenirim." Tek bir ayrıntı sorusuyla çöker; çöktüğünde o ana kadarki bütün cevapların güveni de gider.
- Kuru bir "deneyimim yok" deyip susmak. Dürüsttür ama görüşmecinin elinde yalnız senin eksiğin kalır.
- Küçük bir temasi büyük bir deneyim gibi anlatmak. Kıdemli görüşmeci bunu tek soruda yakalar ve o andan sonra doğru cevaplarına da güvenmez (`256`).
- Dil sorusunu hiç cevaplamadan alan bilgisine kaçmak. Konu değiştirmek kaçınma olarak okunur ve soruyu ikinci kez sordurur.
- Farkı hiç sayamamak. "Benziyorlar" demek, üzerine düşünmediğini gösteren en hızlı cümledir.
- Süre için boş söz vermek. "Hızlı öğrenirim" ölçülemez; takvim ve somut adım olmadan hiçbir bilgi taşımaz.
- Özür dileyip savunmaya geçmek. Boşluk büyür ve mülakatın geri kalanı o boşluğun etrafında döner.

**Kendi hikâyen / dürüst köprü**

Burada anlatılacak hikâye C# hikâyesi değildir, **öğrenme** hikâyesidir. Uydurma; şu sorular seni kendi geçmişine götürsün:

- Java tarafında en zor öğrendiğin konu neydi ve ne kadar sürdü? Bu, öğrenme hızın için elindeki tek gerçek kanıttır.
- Daha önce yeni bir çatıya ya da araca sıfırdan geçtin mi? Hangisi, ne kadar sürdü, ilk hafta neye takıldın?
- Hiç dokunmadığın büyük bir kod tabanını okumak zorunda kaldın mı? Nereden başladın, ne işe yaradı?
- AOP ve araya girici (interceptor) konusunda .NET tarafındaki temasın hangi bağlamdaydı? Ne yaptın, nereye kadar gittin? Bunu tam olduğu kadar anlat, bir gram fazlasını değil.
- Ekipte biri sana yeni bir teknolojiyi nasıl öğretti? Kod incelemesi mi, eşli çalışma mı, kendi okuman mı?
- Öğrendiğini neyle ölçtün? İlk bağımsız değişikliğin ne zaman geldi, ne kadar yorum aldı?
- İlk günden değer üretebileceğin bir alan var mı? Takas akışının analizi dil bilmeden de yapılabilir; bunu söylemek güçlü bir cümledir.

### "Java ile C# arasındaki farklar neler?"

**Soru aslında ne ölçüyor?**

Bu soru bir liste sorusu gibi görünür ama değildir. Fark saymak kolaydır ve ezberlenebilir. Görüşmecinin baktığı şey **ayrımdır**: aday hangi farkın yalnız kelime farkı, hangisinin gerçek bir zihin değişimi olduğunu ayırabiliyor mu. Ayrımı yapan aday iki ekosistemi de kavram düzeyinde anlamış demektir. Ayrım yapmayan aday bir liste ezberlemiştir ve bu, ikinci soruda anlaşılır. Bu dosyanın en değerli tek noktası burasıdır.

**Sözlü cevap iskeleti**

1. **Cevabı ikiye böleceğini ilk cümlede söyle.** "Farkları iki gruba ayırırım: aynı kavramın farklı adı olanlar, ve gerçekten farklı olanlar." Bu tek cümle seni listeyi sayan adaylardan ayırır.
2. **Birinci grubu hızlı geç.** Paket ↔ ad alanı (namespace). Arayüz ve soyut sınıf ikisinde de aynıdır. Lambda ikisinde de vardır. Stream ↔ LINQ. Maven ve Gradle ↔ NuGet ile `dotnet` komut satırı. JPA ve Hibernate ↔ EF Core (`283`). Spring'in kabı ↔ ASP.NET Core'un yerleşik kabı (`282`). JVM ↔ CLR. `Optional` ↔ null olabilen referans tipleri; ikisi aynı derdi farklı yerden çözer.
3. **İkinci grupta yavaşla — değer tipi.** C#'ta `struct` ile kendi değer tipini tanımlarsın; atandığında kopyalanır ve yığında (heap) ayrılma yapmayabilir. Java'da bunun karşılığı yoktur. Bu fark, ileride `Task` ile `ValueTask` ayrımına kadar uzanır (`284`).
4. **Tip silmenin olmaması.** Java jenerikleri çalışma anında tip argümanını kaybeder. C# jenerikleri çalışma anında da tiplidir. Sonucu somuttur: `List<int>` kutulama (boxing) yapmadan sayı tutar ve tip çalışma anında sorgulanabilir.
5. **Uzantı metotları ve özellik.** Uzantı metoduyla var olan bir tipe, kaynağına dokunmadan metot eklenir; LINQ'in tamamı bunun üstüne kuruludur. Özellik (property) ise `get` ve `set` işini dile gömer: alan gibi yazılır, metot gibi çalışır. Java'da getter ve setter bir dil kuralı değil, bir alışkanlıktır.
6. **Kapanışı ayrımla yap.** "Birinci grup kısa sürede oturur, ikinci grup dikkat ister" cümlesi cevabı kapatır ve gerçekçi görünmeni sağlar. Bedeli de söyle: değer tipini yanlış kullanmak sessiz kopyalar üretir, uzantı metodu aşırı kullanılınca kodun nereden geldiği kaybolur.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Java ile C# farklari: kelime farki olan grup ve gercekten farkli olan grup">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Farkları saymak değil, ikiye ayırmak ayrıştırır</text>
  <rect x="14" y="36" width="316" height="234" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kavram aynı, yalnız kelime farklı</text>
  <text x="172" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">paket ↔ ad alanı (namespace)</text>
  <text x="172" y="103" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">arayüz ve soyut sınıf: ikisinde de aynı</text>
  <text x="172" y="124" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">lambda: ikisinde de var</text>
  <text x="172" y="145" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Stream ↔ LINQ</text>
  <text x="172" y="166" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Maven/Gradle ↔ NuGet + dotnet</text>
  <text x="172" y="187" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">JPA/Hibernate ↔ EF Core</text>
  <text x="172" y="208" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Spring kabı ↔ yerleşik DI kabı</text>
  <text x="172" y="229" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">JVM ↔ CLR · Optional ↔ nullable tip</text>
  <text x="172" y="255" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">bu grup kısa sürede oturur; hızlı geçilir</text>
  <rect x="344" y="36" width="302" height="234" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.8"/>
  <text x="495" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gerçekten farklı olanlar</text>
  <text x="495" y="82" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">değer tipi (struct)</text>
  <text x="495" y="98" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">atanınca kopyalanır; Java'da karşılığı yok</text>
  <text x="495" y="124" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">tip silme (erasure) YOK</text>
  <text x="495" y="140" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">jenerik çalışma anında da tiplidir</text>
  <text x="495" y="166" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">uzantı metotları</text>
  <text x="495" y="182" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tipe dokunmadan metot; LINQ buna dayanır</text>
  <text x="495" y="208" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">özellik (property)</text>
  <text x="495" y="224" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alan gibi yazılır, metot gibi çalışır</text>
  <text x="495" y="252" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">bu grup dikkat ister; ezberle geçilmez</text>
  <rect x="14" y="282" width="632" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="302" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Ayrımı yapan aday ayrışır</text>
  <text x="330" y="320" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"İkisi de aynı" ve "tamamen farklı diller" cümleleri aynı ölçüde yanlıştır.</text>
</svg>
```

**Elenme sebepleri**

- Farkları ayrımsız bir liste hâlinde saymak. Ezber izlenimi verir ve hiçbir muhakeme göstermez.
- "Aslında ikisi de aynı" demek. Değer tipi ve tip silme farkını görmediğini tek cümlede söyler.
- "Tamamen farklı diller" demek. Aynı ölçüde yanlıştır; iki ekosistemin kavramsal yakınlığı gerçektir ve inkârı bilgisizlik okunur.
- Yalnız sözdizimi farkı saymak. Büyük harfli tip adları ya da anahtar kelime farkları yüzeyde kalır.
- Jenerikleri ikisinde de aynı sanmak. Tip silme bu soruda en değerli tek maddedir; atlanması pahalıya patlar.
- LINQ'i "Stream'in aynısı" diye geçmek. Benzerdir, ama uzantı metotları üstünde durur ve EF Core'un sorgu çevirisi buna dayanır (`283`).
- Bir dili küçümsemek. "C# daha modern" ya da "Java daha ciddi" cümleleri teknik değil, taraf tutmadır.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâye C# tarafından gelmez, Java tarafından gelir. Her farkın altına kendi deneyimini koy:

- Jeneriklerin tip silmesi yüzünden hiç sorun yaşadın mı? Çalışma anında tip bilgisine ihtiyaç duyup `Class` nesnesi taşıdığın oldu mu?
- Kutulama (autoboxing) yüzünden performans ya da null sorunu gördün mü? Değer tipi kavramına en yakın deneyimin budur.
- `Optional`ı nerede kullanıyorsun, null denetimini ekipçe nasıl yönetiyorsunuz? Aynı derdin C# tarafındaki karşılığını buradan anlatırsın.
- Stream API'yi hangi işlerde kullandın? Aynı zihinsel modeli LINQ okurken kurabiliyor musun?
- Bağımlılık ve sürüm çakışmasını nasıl çözdün? Aynı problem NuGet tarafında da aynı biçimde var.
- Getter ve setter üretimini nasıl yapıyorsun? Özellik farkını en somut buradan anlatabilirsin.
- Bir kütüphanenin kaynağına dokunmadan davranışını genişletmen gerekti mi? Java'da nasıl çözdün; uzantı metodu aynı ihtiyacı karşılar.

### "Bu geçişi nasıl yapacaksın, planın ne?"

**Soru aslında ne ölçüyor?**

Bu bir vaat sorusu değil, plan sorusudur. Görüşmeci senin ne kadar hızlı öğreneceğini bilemez; ölçebileceği tek şey planının gerçekçi olup olmadığıdır. İki uç da eler. "Zaten aynı şey, plana gerek yok" hafiflik okunur. "Hiç bilmiyorum ama öğrenirim" ise boş bir sözdür ve karşı tarafa hiçbir bilgi vermez. Aradaki cevap somut, takvimli ve ekibe maliyetini kabul eden cevaptır. Görüşmecinin gerçek endişesi şudur: bu adayı işe alırsam ekibim ilk üç ay ne kadar yavaşlar.

**Sözlü cevap iskeleti**

1. **Planı üç pencereye böl.** İlk hafta, ilk ay, ilk çeyrek. Bu bölme tek başına ciddiyet gösterir ve cevabı ezberden ayırır.
2. **İlk hafta: araç zinciri.** `dotnet` komut satırı, çözüm ve proje dosyası düzeni, NuGet, test çatısı, hata ayıklayıcı. Bu katman çabuk oturur çünkü karşılıkları biliniyor: Maven ve Gradle'ın yerinde NuGet, JUnit'in yerinde .NET test çatıları var.
3. **İlk ay: ekosistem kalıpları.** Kap ve kapsamlar, ara katman (middleware) hattı, yapılandırma düzeni (`282`); EF Core'da izleme (change tracking) ve N+1 (`283`). Burada öğrenilen şey dil değil, çatının alışkanlıklarıdır.
4. **Asıl yol: ekibin kendi kodunu okumak ve incelemeyi araç yapmak.** Yeni bir ekosisteme en hızlı giriş, o ekibin var olan kod tabanıdır; adlandırma, katman düzeni ve kabul edilmiş kalıplar oradadır. Küçük ve düşük riskli bir işle başlamayı iste, kod incelemesinde çok soru soracağını baştan söyle.
5. **Bilinen tuzakları önceden işaretle.** Nereye dikkat edeceğini söylemek plana güven verir: `.Result` ile bloklamanın kilitlenme riski (`284`), izlemenin gereksiz yere açık kalması (`283`), uzun ömürlü nesneye kısa ömürlü bağımlılık enjekte etmek (`282`).
6. **Ölçütü ver.** "Öğrendim" bir duygudur. Ölçüt somut olur: ilk bağımsız değişikliğin ne zaman gelir, ilk uçtan uca özelliği ne zaman teslim edersin, incelemede aldığın yorum sayısı ne zaman düşer.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gecis plani: ilk hafta arac zinciri, ilk ay ekosistem kaliplari, ilk ceyrek uretim refleksi">
  <defs>
    <marker id="jcs-plan" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Geçiş planı üç pencerede anlatılır, tek cümlede değil</text>
  <rect x="14" y="36" width="204" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İlk hafta · araç zinciri</text>
  <text x="116" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dotnet komut satırı</text>
  <text x="116" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">NuGet ve proje düzeni</text>
  <text x="116" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test çatısı, hata ayıklayıcı</text>
  <text x="116" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">karşılıkları biliniyor:</text>
  <text x="116" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Maven/Gradle ↔ NuGet</text>
  <line x1="219" y1="106" x2="232" y2="106" stroke="#5D7789" stroke-width="1.4" marker-end="url(#jcs-plan)"/>
  <rect x="228" y="36" width="204" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İlk ay · çatı kalıpları</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kap ve kapsam seçimi</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ara katman hattı, yapılandırma</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">EF Core: izleme ve N+1</text>
  <text x="330" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">öğrenilen şey dil değil,</text>
  <text x="330" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">çatının alışkanlıklarıdır</text>
  <line x1="433" y1="106" x2="446" y2="106" stroke="#5D7789" stroke-width="1.4" marker-end="url(#jcs-plan)"/>
  <rect x="442" y="36" width="204" height="140" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="544" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">İlk çeyrek · refleks</text>
  <text x="544" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ekibin kendi kodunu oku</text>
  <text x="544" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">incelemede soru sor</text>
  <text x="544" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tuzakları önceden işaretle</text>
  <text x="544" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">.Result kilitlenmesi, açık</text>
  <text x="544" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">izleme, yanlış kapsam</text>
  <rect x="14" y="190" width="316" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="210" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">"Zaten aynı şey, plana gerek yok"</text>
  <text x="172" y="230" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hafiflik okunur; ekibin maliyetini görmez</text>
  <rect x="344" y="190" width="302" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="210" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">"Hiç bilmiyorum ama öğrenirim"</text>
  <text x="495" y="230" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">boş sözdür; ölçülebilir hiçbir şey söylemez</text>
  <rect x="14" y="252" width="632" height="44" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="271" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Planın ölçütü söylenmezse plan bir niyet beyanıdır</text>
  <text x="330" y="289" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">ilk bağımsız değişiklik · ilk uçtan uca özellik · incelemede azalan yorum sayısı</text>
</svg>
```

**Elenme sebepleri**

- "Zaten aynı şey" demek. Planı gereksiz görmek, ekibe çıkaracağın maliyeti hiç düşünmediğini gösterir.
- "Hızlı öğrenirim" deyip bitirmek. Bu cümle bir plan değil, bir temennidir ve herkes söyler.
- Üç ay kitap okuyup sonra koda başlamayı önermek. Kod yazmayan plan ne değer üretir ne de gerçekçidir.
- Ekipten hiç yardım istememek. Tek başına öğrenme sözü kahramanlık gibi durur, aslında risktir.
- Mevcut kod tabanını hiç anmamak. Ekibin kodunu okumayan bir öğrenme planı havada kalır.
- Ölçüt vermemek. Ne zaman "geçti" sayılacağı belli değilse plan denetlenemez.
- Tuzakları hiç bilmemek. Nereye dikkat edeceğini söylemeyen aday henüz haritayı çıkarmamıştır.

**Kendi hikâyen / dürüst köprü**

Buradaki hikâye "geçmişte yaptığım bir geçiş" hikâyesidir. Küçük olabilir, yeter ki gerçek olsun:

- Daha önce hangi teknolojiye sıfırdan geçtin? Süre neydi, ilk hafta hangi adımı attın?
- Hiç dokunmadığın bir kod tabanına katıldığın oldu mu? Okumaya nereden başladın: uç noktalardan mı, testlerden mi, veri modelinden mi?
- İlk katkını ne kadar sürede verdin? O katkı ne kadar yorum aldı, ikincisi kaç yorum aldı?
- Öğrenirken kimden ne istedin? Eşli çalışma, kısa bir tur, ya da yalnızca bir okuma listesi?
- Bir teknolojinin tuzağını önceden bilmen seni hiç kurtardı mı? Bunu anlatmak plan sorusunda çok işe yarar.
- Ekipte yeni gelen birine sen mentorluk yaptın mı? Öğreten kişi, öğrenme planını daha gerçekçi kurar.
- Bugün elinde ne var? Kendi makinende küçük bir .NET projesi ayağa kaldırmak gerçek ve dürüstçe anlatılabilir bir adımdır.

### "Minimum sekiz yıl deneyim" maddesini nasıl karşılarsın?

**Soru aslında ne ölçüyor?**

Bu madde bir filtredir, bir ölçüt değil. Yıl eşiği geçildikten sonra kimse yılları saymaz; ölçülen şey kararlarının kalitesidir. Görüşmeci "sekiz yıl" derken aslında şunu sorar: bu aday bir işi analizden üretime kadar tek başına götürebilir mi, ve verdiği kötü bir karardan geri dönebilir mi. Madde yıl deneyimi ister; hangi dilde geçtiğini yazmaz. Bu ayrımı sakin bir sesle söylemek, maddeyi savunmaya geçmeden karşılar.

**Sözlü cevap iskeleti**

1. **Eşiği tek cümlede karşıla.** 2018'den beri profesyonel geliştiricisin. Abartma, savunmaya geçme, tarih oynatma. Tarihler özgeçmişte yazılıdır ve doğrulanır.
2. **Kıdemi kararla kanıtla.** Yıl anlatılmaz, karar anlatılır: hangi seçenekler vardı, neyi seçtin, bedeli neydi, sonucu nasıl ölçtün (`195`).
3. **Alan bilgisini öne al.** Zorunlu işaretli iki maddeden biri kartlı ödeme takas ve gün sonu deneyimidir. Kıdemini bu alanda geçirdiğin yıllara bağlarsan iki maddeyi tek cümlede karşılamış olursun (`250`, `252`).
4. **Dil farkını ayrı bir başlık olarak bırak.** Kıdem cevabının içine dil savunması sıkıştırma; karıştırırsan iki cevap da zayıflar. Dil sorusu gelirse dürüst köprüyü ayrıca kur (`242`, `256`).

**Elenme sebepleri**

- Yıl sayısını esnetmek. Doğrulanabilir bir bilgideki tutarsızlık, mülakatın tamamının güvenini bitirir.
- "Yıl önemli değil" diyerek maddeyi küçümsemek. İlanı yazan kişiyle tartışmak iyi bir açılış değildir.
- Yılları görev listesiyle anlatmak. "Şunu yaptım, bunu yaptım" kıdem göstermez; karar gösterir.
- Kıdem cevabının içine dil savunması karıştırmak. İki ayrı soru tek cevapta erirse ikisi de eksik kalır.
- Alan bilgisini hiç anmamak. Bu ilanda en pahalı atlama budur; zorunlu madde orada duruyor.

**Kendi hikâyen / dürüst köprü**

Bu bölümde uydurmaya zaten gerek yok; anlatılacak şey senin kendi yıllarındır. Şu sorular onu çıkarır:

- Bu yıllarda geri dönüşü en pahalı olan hangi kararı verdin? Alternatifi neydi, neyi feda ettin?
- Hangi kararın yanlış çıktı ve nasıl geri döndün? Bu soruya cevabı olan aday kıdemli sayılır (`195`).
- Bir işi analizden üretime kadar tek başına götürdüğün bir örnek var mı?
- Kaç kişilik ekipte çalıştın, kime ne öğrettin, hangi kararı sen sahiplendin?
- Ödeme, mutabakat, gün sonu ya da toplu iş tarafında hangi işlere dokundun? Bu ilanda en değerli cümleler oradan çıkar (`250`, `254`).
- Üretimde bir olay yaşadın mı? Nasıl teşhis ettin ve sonra neyi kalıcı olarak değiştirdin?
- Bir teknik borcu iş tarafına nasıl anlattın ve nasıl önceliklendirdin?

### Bu bölümden çıkan ders

Bu dosyanın tek bir omurgası var: dil boşluğu saklanmaz, çerçevelenir. İlanın zorunlu iki maddesi sende duruyor; eksik olan tek şey dil ve o, bu ilanda zorunlu işaretli bile değil. "C# deneyiminiz nedir?" sorusunda kazanan cevap beş adımlıdır: sınırı tek cümlede söyle, Java kıdemini somutla, iki ekosistemin kavramsal yakınlığını göster, gerçekten farklı olanı adlandır, takvimli bir plan ve alan bilgisine köprü ver. "Farklar neler?" sorusunda kazanan hamle listeyi saymak değil, ikiye ayırmaktır; kelime farkı olanlar hızlı geçilir, değer tipi ile tip silmenin olmaması üzerinde durulur. "Planın ne?" sorusunda kazanan şey üç pencere ve bir ölçüttür. Kıdem maddesinde kazanan şey yıl sayısı değil, o yıllarda verdiğin kararlardır. Ve dördünde de aynı yasak geçerlidir: olmayan bir C# deneyimi uydurulmaz, çünkü uydurmanın maliyeti asimetriktir — kazanılan tek soru, kaybedilen ise mülakatın tamamıdır.

# jcs-1 | Temel
## Soru
Görüşmeci ilk sorusunu soruyor: "C# deneyiminiz nedir?" Aşağıdaki yaklaşımlardan hangisi bu soruda en güçlü cevabı üretir?
## A
Java ile C# neredeyse aynı dillerdir; bir hafta sonunda tamamını öğrenip üretim kodu yazabileceğini söylemek
🤖 abartılı özgüven tek ayrıntı sorusuyla çöker ve önceki cevapların güvenini de götürür.
## B
C# bilmediğini söyleyip susmak; görüşmeci isterse konuyu kendisi açar, fazla söz güveni zedeler
🤖 dürüsttür ama nötrdür; geriye yalnız eksik kalır, hiçbir bilgi üretilmez.
## C
Sınırı tek cümlede söyle, Java kıdemini somutla, gerçek farkları adlandır, takvimli bir kapatma planı ver
🤖 Doğru: dürüst köprü hem sınırı korur hem düşünme biçimini görünür kılar.
## D
Dil sorusunu cevaplamadan doğrudan takas ve gün sonu deneyimine geçmek; ilanda zorunlu olan madde odur
🤖 konu değiştirmek kaçınma olarak okunur ve soruyu ikinci kez sordurur.
## Doğru
C
## Açıklama
Bu soruda görüşmeci yeni bir bilgi öğrenmiyor; özgeçmişini okudu ve dil boşluğunu zaten biliyor. Ölçtüğü şey **teslim biçimidir**: kendi sınırını süslemeden söyleyebilen ve geçiş süresini gerçekçi tahmin eden bir aday, üretimde de az sürpriz çıkarır. A şıkkı abartılı özgüven ucudur; tek bir ayrıntı sorusu bu cevabı çökertir ve çöktüğünde o ana kadar verdiğin doğru cevaplara da güven kalmaz. B şıkkı dürüsttür ama nötrdür; görüşmecinin elinde yalnız senin eksiğin kalır ve o boşluğu kimse senin yerine dolduramaz. D şıkkı kaçınmadır; alan bilgisi bu ilanda gerçekten güçlü tarafın ama soru sorulmuşken konu değiştirmek olumsuz okunur. Doğru yapı dört adımlı çerçeveleme kalıbıdır: dürüst sınır, komşu deneyimin somutlanması, farkın adlandırılması ve takvimli kapatma planı (`256`). Beşinci adım bu ilana özeldir: cevabı alan bilgisine bağlayıp orada bırakmak. Dil öğrenilir; bir kartlı ödeme sisteminin gün sonu davranışı kitaptan öğrenilmez (`250`, `252`).

# jcs-2 | Temel
## Soru
Java ile .NET ekosistemi arasında kavram eşlemesi kuruyorsun. Aşağıdaki gruplardan hangisi baştan sona doğrudur?
## A
paket ↔ ad alanı (namespace) · Stream ↔ LINQ · JPA/Hibernate ↔ EF Core · JVM ↔ CLR
🤖 Doğru: dördü de aynı kavramın iki ekosistemdeki adıdır.
## B
paket ↔ derleme birimi (assembly) · Stream ↔ Task · Hibernate ↔ Dapper · JVM ↔ ara dil (IL)
🤖 Task eşzamanlılık soyutlamasıdır, veri akışı değil; JVM bir çalışma zamanıdır, ara dil değil.
## C
Spring kabı ↔ WCF · Maven ↔ MSBuild · JDBC ↔ LINQ · çöp toplama ↔ değer tipi (struct)
🤖 WCF eski nesil bir servis çatısıdır, bağımlılık kabı değil; son eşleme büsbütün ilgisizdir.
## D
arayüz ↔ uzantı metodu · Optional ↔ struct · lambda ↔ özellik (property) · JUnit ↔ NuGet
🤖 dördü de farklı kavramları eşler; NuGet bir paket yöneticisidir, test çatısı değil.
## Doğru
A
## Açıklama
Bu eşlemeler geçişin **birinci grubudur**: kavram aynıdır, yalnız adı değişir. Paketin karşılığı ad alanıdır (namespace). Stream API ile LINQ aynı işi görür; ikisi de koleksiyon üstünde zincirlenen dönüşümler kurar. Nesne-ilişkisel eşleme tarafında JPA ve Hibernate'in karşılığı EF Core'dur; kalıcı bağlam ile izleme (change tracking) aynı fikirdir (`283`). JVM'in karşılığı CLR'dir; ikisi de yönetilen çalışma zamanıdır, çöp toplama yapar ve ara dili makine koduna çevirir. B şıkkında `Task` bir eşzamanlılık soyutlamasıdır ve Stream'in karşılığı değildir (`284`); ayrıca Hibernate ile Dapper farklı düzeydedir, çünkü Dapper hafif bir eşleyicidir. C şıkkında WCF eski nesil bir servis çatısıdır ve bağımlılık kabıyla ilgisi yoktur; Spring'in kabının karşılığı ASP.NET Core'un yerleşik kabıdır (`282`). D şıkkı kavramları rastgele eşler. Bu grubu hızlı geçmek önemlidir, çünkü mülakatta değerli olan ikinci gruptur: gerçekten farklı olanlar.

# jcs-3 | Orta
## Soru
"Java ile C# arasındaki farklar neler?" sorusunda farkları ikiye ayırıyorsun. Hangi grup "gerçekten farklı olan", yani yalnız kelime farkı olmayan gruptur?
## A
ad alanı, arayüz, soyut sınıf ve lambda; bu dört yapı Java tarafında bulunmaz
🤖 arayüz, soyut sınıf ve lambda Java'da da vardır; ad alanı yalnız paketin başka adıdır.
## B
bağımlılık enjeksiyonu, nesne-ilişkisel eşleme ve paket yöneticisi kullanımı; bu üçü .NET'e özgüdür
🤖 üçünün de Java tarafında yerleşik karşılığı vardır; bunlar kelime farkı grubundadır.
## C
çöp toplama, güçlü tipleme ve yönetilen çalışma zamanı; bu üç özellik yalnız CLR tarafında bulunur
🤖 üçü de iki ekosistemin ortak zeminidir; fark değil, yakınlık kanıtıdır.
## D
değer tipi (struct), tip silmenin olmaması, uzantı metotları ve özellik (property)
🤖 Doğru: dördünün de Java'da doğrudan karşılığı yoktur.
## Doğru
D
## Açıklama
Bu soru dosyanın en değerli ayrımını sınar. Farkları saymak kolaydır; ayırmak zordur. **Kelime farkı grubunda** paket ile ad alanı, Stream ile LINQ, Maven ile NuGet, Spring kabı ile yerleşik kap durur. Bunlar hızlı geçilir. **Gerçekten farklı grupta** ise dört madde vardır. Değer tipi (`struct`) atandığında kopyalanır ve yığında ayrılma yapmayabilir; Java'da kendi değer tipini tanımlayamazsın ve bu fark `ValueTask` gibi API'lere kadar uzanır (`284`). Tip silmenin olmaması ikinci maddedir: C# jenerikleri çalışma anında da tiplidir, Java jenerikleri değildir. Uzantı metotları var olan bir tipe kaynağına dokunmadan metot ekler ve LINQ'in tamamı bunun üstüne kuruludur. Özellik (property) ise `get` ve `set` işini dile gömer; Java'da getter ve setter bir dil kuralı değil, bir alışkanlıktır. C şıkkındaki üç madde iki ekosistemin **ortak** zeminidir ve mülakatta yakınlık kanıtı olarak kullanılır, fark olarak değil. Ayrımı yapan aday, iki ekosistemi kavram düzeyinde anlamış sayılır.

# jcs-4 | Orta
## Soru
Java'da `List<String>` çalışma anında tip argümanını taşımaz; buna tip silme (erasure) denir. C# jeneriklerinde durum nedir ve bunun somut sonucu nedir?
## A
C# tarafında da tip silinir; iki platformda davranış aynıdır, fark yalnız sözdizimindedir
🤖 bu, mülakatta en sık kaçırılan farktır; C# jenerikleri çalışma anında tipini korur.
## B
Tip çalışma anında korunur; `List<int>` kutulama yapmadan sayı tutar ve tip sorgulanabilir
🤖 Doğru: jenerik bilgisi çalışma zamanına kadar taşınır.
## C
C# tarafında jenerik yoktur; aynı ihtiyaç dinamik tiplerle ve `object` kullanımıyla karşılanır
🤖 jenerikler dilin yerleşik parçasıdır; koleksiyon kütüphanesi bunun üstüne kuruludur.
## D
Jenerik yalnız derleyici denetimidir; çalışma anında bütün jenerik tipler `object` hâline döner
🤖 bu Java'nın davranışının tarifidir; C# tarafında böyle bir dönüşüm olmaz.
## Doğru
B
## Açıklama
Java jenerikleri **derleme zamanı** özelliğidir. Derleyici denetimi yapar, sonra tip argümanını siler; çalışma anında elinde yalnız `List` kalır. Bu yüzden Java'da `List<String>` ile `List<Integer>` çalışma anında ayırt edilemez ve ilkel tipler kutulanarak (boxing) saklanır. C# tarafında jenerik bilgisi çalışma zamanına kadar taşınır. Bunun iki somut sonucu vardır. Birincisi bellek ve hız tarafındadır: `List<int>` sayıları kutulamadan tutabilir, çünkü değer tipi için ayrı bir gerçekleşme üretilir. İkincisi yansıma (reflection) tarafındadır: bir nesnenin hangi tip argümanıyla oluşturulduğu çalışma anında sorulabilir. Bu fark, dosyanın "gerçekten farklı olanlar" grubundadır ve mülakatta tek başına ayrıştırıcıdır; çünkü çoğu aday farkları sayarken bu maddeyi atlar. A şıkkı en sık yapılan hatadır. C şıkkı jeneriklerin varlığını inkâr eder. D şıkkı aslında Java'nın davranışını C#'a yakıştırır. Cevabın devamında bir cümle daha söylenebilir: değer tipi kavramı bu farkın kardeşidir ve ikisi birlikte öğrenilir.

# jcs-5 | İleri
## Soru
Görüşmeci soruyor: "Bu geçişi nasıl yapacaksınız, planınız ne?" Hangi cevap bu soruda en güçlüdür?
## A
İlk hafta araç zinciri, ilk ay çatı kalıpları, sonrasında ekibin kod tabanını okuyarak ilerlemek ve ölçüt vermek
🤖 Doğru: takvimli, somut ve denetlenebilir bir plan güven verir.
## B
Plana gerek olmadığını, iki ekosistem kavramsal olarak aynı olduğu için ilk günden aynı hızda üretebileceğini söylemek
🤖 ekibe çıkacak maliyeti görmez; hafiflik olarak okunur.
## C
Üç ay boyunca yalnız kitap ve doküman okumak, ondan sonra ilk kod satırını yazmaya başlamak
🤖 kod yazmayan plan ne değer üretir ne de gerçekçidir; öğrenme yavaşlar.
## D
Konuyu ekibe hiç açmamak; sorular çıktıkça tek başına araştırıp kimseye maliyet çıkarmamayı taahhüt etmek
🤖 tek başına öğrenme sözü kahramanlık gibi durur, aslında gecikme riskidir.
## Doğru
A
## Açıklama
Bu soruda ölçülen şey öğrenme hızı değil, **planın gerçekçiliğidir**. Görüşmecinin asıl endişesi şudur: bu adayı alırsam ekibim ilk aylarda ne kadar yavaşlar. Güçlü cevap planı üç pencereye böler. İlk hafta araç zinciri oturur: `dotnet` komut satırı, proje düzeni, NuGet, test çatısı, hata ayıklayıcı; karşılıkları bilindiği için bu katman çabuk geçilir. İlk ay çatı kalıpları gelir: kap ve kapsam seçimi, ara katman hattı, yapılandırma (`282`), EF Core'da izleme ve N+1 (`283`). Sonrasında asıl yol ekibin kendi kodunu okumak ve kod incelemesini bir öğrenme aracına çevirmektir. Cevabın gücünü artıran iki ek vardır. Birincisi bilinen tuzakları önceden işaretlemek: `.Result` ile bloklamanın kilitlenme riski (`284`), uzun ömürlüye kısa ömürlü enjekte etmek (`282`). İkincisi ölçüt vermek: ilk bağımsız değişiklik, ilk uçtan uca özellik, incelemede azalan yorum sayısı. B ve D şıkları iki farklı biçimde aynı hatayı yapar; ekibin gerçekliğini hesaba katmazlar. C şıkkı ise öğrenmeyi kodun dışına taşır.

# jcs-6 | İleri
## Soru
Görüşmeci şunu söylüyor: "İlanda minimum sekiz yıl deneyim yazıyor, ama siz bu yılları C# ile geçirmemişsiniz." En güçlü karşılık hangisidir?
## A
.NET tarafındaki temasları da yıla katarak toplam süreyi sekiz yıla yaklaştırmak ve tek rakam vermek
🤖 doğrulanabilir bir bilgide esneme, mülakatın tamamının güvenini bitirir.
## B
Yıl sayısının gerçekte bir şey ölçmediğini söyleyip maddenin kendisini tartışmaya açmak
🤖 ilanı yazan kişiyle tartışmak iyi bir açılış değildir; savunmaya geçmiş görünür.
## C
Eşiği Java kıdemiyle karşıla; kanıtı verdiğin kararlara ve takas alan bilgisine bağla, dil farkını ayrı tut
🤖 Doğru: madde yıl ister, dil adı yazmaz; kanıt karardır.
## D
Yalnız takas ve gün sonu deneyimini anlatıp kıdem ile dil sorusunu hiç açmadan konuyu kapatmak
🤖 sorulan maddeyi cevapsız bırakır; görüşmeci aynı soruyu ikinci kez sorar.
## Doğru
C
## Açıklama
Sekiz yıl maddesi bir **filtredir**, bir ölçüt değil. Eşik geçildikten sonra kimse yılları saymaz; ölçülen şey karar kalitesidir. Doğru karşılık üç parçalıdır. Önce eşik sakin bir cümleyle karşılanır: 2018'den beri profesyonel geliştiricisin ve madde yıl deneyimi ister, hangi dilde geçtiğini yazmaz. Sonra kanıt verilir; kıdem yılla değil kararla anlatılır: hangi seçenekler vardı, neyi seçtin, bedeli neydi, sonucu nasıl ölçtün (`195`). Son olarak bu kıdem ilanın zorunlu maddesine bağlanır; kartlı ödeme takas ve gün sonu alanında geçirilmiş yıllar bu ilanda en değerli cümleleri üretir (`250`, `252`, `254`). A şıkkı en pahalı seçenektir; tarihler özgeçmişte yazılıdır ve doğrulanır, tutarsızlık yakalandığında geriye dönük olarak bütün cevaplar şüpheli hâle gelir. B şıkkı ilan sahibiyle tartışmaya girer. D şıkkı ise sorulan maddeyi cevapsız bırakır. Dil farkı bu cevabın içine karıştırılmaz; ayrı bir başlık olarak, dürüst köprü kalıbıyla ele alınır (`242`, `256`).
