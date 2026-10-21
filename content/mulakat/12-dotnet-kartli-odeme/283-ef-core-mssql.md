---
slug: ef-core-mssql
baslik: "EF Core ve MS SQL: İzleme, N+1, Göç, İşlem ve Sorgu Planı"
kademe: İleri
sira: 283
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş açık uçlu mülakat sorusu var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri ve kendi hikâyenin yeri. Son parça bilerek boş bırakıldı; senin adına deneyim uydurulmaz, onu kendi geçmişinden çıkarman için sorular sorulur.

Bu dosyanın çalışma biçimi diğerlerinden farklıdır. Sen JPA ve Hibernate biliyorsun. EF Core'un anlattığı fikirlerin neredeyse tamamının Java tarafında bir karşılığı var ve o karşılığı zaten kullandın. Bu yüzden burada yeni bir kalıcılık modeli öğretilmiyor; bildiğin modelin **kelimeleri** değiştiriliyor. JPA tarafını tekrar anlatmıyoruz, atıf veriyoruz: kalıcı bağlam ve kirli kontrol için `80` ve `81`, N+1 için `82`, işlem sınırı için `83` ve `84`, iyimser kilit için `225`.

Eşleme kabaca şudur. `DbContext` bir kalıcı bağlamdır (persistence context). İzleme (change tracking) kirli kontroldür (dirty checking). `AsNoTracking` salt okunur sorgudur. `Include`/`ThenInclude`, `JOIN FETCH` ve `@EntityGraph`'tir. `rowversion` ile eşzamanlılık belirteci `@Version`'dır. Göç (migration) araçları Flyway ve Liquibase'in yerinde durur. Bu kadarını söyleyebilen aday, EF Core yazmamış olsa bile kalıcılık katmanını anladığını göstermiş olur.

İki başlık bu dosyada ayrı ağırlık taşır ve mülakatta seni ayrıştırır. Birincisi, `DbContext`'in **iş parçacığı güvenli olmadığı**; ikincisi, MS SQL'e özgü olan kümelenmiş indeks ayrımı ve parametre koklamasıdır. Oracle'ı bilen bir aday için bu iki konu gerçekten yeni bilgidir; Oracle kıyası için `227`'ye bak.

Bir uyarı: bu dosya seni C# geliştiricisi yapmaz, yapmamalı da. Mülakatta "şu projede EF Core kullandım" cümlesi kurulmaz. Kurulacak cümle şudur: "Bu fikri JPA tarafında şu şekilde yaşadım, EF Core'daki karşılığı şudur." Dürüst köprü kalıbı için `256`.

### "EF Core'da izleme (change tracking) nedir, ne zaman kapatırsın?"

**Soru aslında ne ölçüyor?**

Görüşmecinin merak ettiği şey bir tanım değil. Bakılan şu: aday nesne ile satır arasındaki farkı görüyor mu. Zayıf aday izlemeyi sihir sanır ve `SaveChanges`'in her şeyi hallettiğini düşünür. Güçlü aday izlemenin bir bedeli olduğunu bilir, o bedeli ölçer ve gerektiğinde kapatır. Bu soru ayrıca bir tuzak barındırır: izlemeyi her yerde kapatmak da yanlıştır.

**Sözlü cevap iskeleti**

1. **Mekanizmayı tek cümlede ver.** `DbContext`, kendisi üzerinden yüklenen varlıkların ilk hâlinin bir kopyasını tutar. `SaveChanges` çağrıldığında bu kopyayla güncel hâli karşılaştırır ve yalnız değişen sütunlar için komut üretir.
2. **JPA karşılığını hemen kur.** Bu, kalıcı bağlam ve kirli kontroldür; fikir birebir aynıdır (`80`, `81`). Sen `setUnvan` çağırıp `UPDATE` yazmamaya alışkınsın; EF Core'da olan da budur.
3. **Bedeli söyle.** Her izlenen varlık için bellekte bir kopya durur ve `SaveChanges` sırasında karşılaştırma yapılır. Yüz kayıtta fark edilmez, elli bin kayıtlık bir raporda belirgin hâle gelir.
4. **Kapatma kuralını ver.** Sonuç yalnız okunacaksa izleme kapatılır: `AsNoTracking`. JPA tarafındaki karşılığı salt okunur sorgu ya da doğrudan DTO projeksiyonudur.
5. **Projeksiyonu bir adım öteye koy.** `Select` ile varlık yerine dar bir nesne çekersen zaten izlenecek bir varlık kalmaz. Hem daha az sütun okunur hem izleme sorunu ortadan kalkar. Rapor uç noktalarında tercih edilen yol budur.
6. **İnce ayrımı ekle.** İzlenen sorguda aynı anahtar için tek nesne örneği döner; bu, JPA'daki birinci seviye önbelleğin kimlik güvencesidir. İzleme kapatılınca aynı satır iki ayrı nesne olarak gelebilir. Buna ihtiyaç varsa kimlik çözümlemeli bir izlemesiz sorgu seçeneği vardır; adı ve davranışı sürümüne göre değişir, dokümandan teyit edilir.
7. **Bağlam ömrünü bağla.** İzleyici yalnız bağlam yaşadığı sürece büyür. Bu yüzden `DbContext` kısa ömürlüdür; istek başına ya da iş birimi başına açılır. `AddDbContext` bunu kapsamlı (scoped) olarak kaydeder. Java'daki `EntityManager` disiplininin aynısıdır.

```svg
<svg viewBox="0 0 660 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="EF Core izleme secenekleri: izlenen sorgu, izlemesiz sorgu ve projeksiyon">
  <defs>
    <marker id="efc-izleme-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İzleme bedava değildir; yazacaksan aç, okuyacaksan kapat</text>
  <rect x="14" y="36" width="204" height="196" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İzlenen sorgu</text>
  <text x="116" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ilk hâlin kopyası tutulur</text>
  <text x="116" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SaveChanges farkı bulur</text>
  <text x="116" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız değişen sütun yazılır</text>
  <text x="116" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı anahtar, tek nesne örneği</text>
  <text x="116" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">JPA: kalıcı bağlam</text>
  <text x="116" y="184" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ve kirli kontrol</text>
  <text x="116" y="210" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">bedel: bellek ve karşılaştırma</text>
  <line x1="220" y1="134" x2="238" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#efc-izleme-ok)"/>
  <rect x="242" y="36" width="204" height="196" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="344" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">AsNoTracking</text>
  <text x="344" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kopya tutulmaz</text>
  <text x="344" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">SaveChanges bu nesneyi görmez</text>
  <text x="344" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bellek ve süre düşer</text>
  <text x="344" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı satır iki nesne olabilir</text>
  <text x="344" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">JPA: salt okunur sorgu</text>
  <text x="344" y="184" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">yazma niyeti yoksa varsayılan</text>
  <text x="344" y="210" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">yazacaksan burada kapatma</text>
  <line x1="448" y1="134" x2="466" y2="134" stroke="#5D7789" stroke-width="1.4" marker-end="url(#efc-izleme-ok)"/>
  <rect x="470" y="36" width="176" height="196" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="558" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Projeksiyon (Select)</text>
  <text x="558" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">varlık hiç oluşmaz</text>
  <text x="558" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız gereken sütun okunur</text>
  <text x="558" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">izleme sorunu ortadan kalkar</text>
  <text x="558" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ağ üzerinden az veri geçer</text>
  <text x="558" y="166" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">JPA: DTO projeksiyonu</text>
  <text x="558" y="184" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">rapor uç noktasının yolu</text>
  <text x="558" y="210" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">okuma tarafında en ucuzu</text>
  <rect x="14" y="244" width="632" height="82" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="265" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">İzleyici yalnız bağlam yaşadığı sürece büyür</text>
  <text x="330" y="286" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">DbContext kısa ömürlüdür: istek başına ya da iş birimi başına açılır, kapsamlı (scoped) kaydedilir.</text>
  <text x="330" y="304" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Uzun ömürlü bağlam bellekte birikir, bayat veri gösterir ve hataları uzak bir noktada patlatır.</text>
  <text x="330" y="320" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">Java'daki EntityManager disiplininin aynısı; kelime değişti, kural değişmedi.</text>
</svg>
```

**Elenme sebepleri**

- İzlemeyi tanımlayamamak. "Otomatik kaydediyor" cevabı, kalıcılık katmanını hiç açmadığını gösterir.
- Bedelden hiç söz etmemek. İzlemenin bellek ve karşılaştırma maliyeti yoksa neden kapatılsın sorusu cevapsız kalır.
- Her yerde `AsNoTracking` kullanmak. Yazma niyeti olan sorguda izleme kapatılırsa değişiklik sessizce kaydedilmez.
- `AsNoTracking` ile projeksiyonu aynı şey saymak. Biri izlemeyi kapatır, diğeri sütun sayısını da düşürür.
- `DbContext`'i uzun ömürlü tutmak. Tekil (singleton) kaydedilen bir bağlam hem birikir hem eşzamanlılık hatası üretir.
- JPA karşılığını hiç kurmamak. Aynı fikri iki adla bilmek, bu mülakatta cevabın en güçlü tarafıdır.
- Ölçmeden konuşmak. "Hızlanır" demek yerine hangi sorguda kaç kaydın okunduğunu söylemek gerekir.

**Kendi hikâyen — dürüst köprü**

Burada anlatacağın deneyim Java tarafındandır ve bu tamamen meşrudur. Şu sorular onu çıkarır:

- Hangi projede kalıcı bağlamın büyümesi sorun oldu? Toplu iş miydi, rapor uç noktası mıydı?
- `flush` ya da `clear` çağırmak zorunda kaldığın bir yer oldu mu? Neden gerekti?
- Salt okunur sorguya ya da DTO projeksiyonuna ne zaman geçtin? Ölçtüğün fark neydi?
- İstemeden yapılan bir güncelleme yaşadın mı? Kirli kontrol, sen `save` çağırmadan bir alanı yazdı mı?
- Açık oturum kalıbı (open session in view) tartışması yaşandı mı? Hangi tarafta durdun?
- Bellek sorunu bir yığın dökümüyle mi anlaşıldı, yoksa yavaşlıkla mı?

Bunları anlattıktan sonra köprüyü tek cümleyle kur: "EF Core'da bunun adı izlemedir ve `AsNoTracking` benim salt okunur sorgumun karşılığıdır." C# ile bunu yaptığını söyleme; fikri bildiğini söyle (`256`).

### "EF Core'da N+1 nasıl oluşur, nasıl çözülür?"

**Soru aslında ne ölçüyor?**

Bu soru her ORM mülakatında sorulur, ama ayrıştırıcı kısmı çözümde değildir. `Include` cevabını çoğu aday verir. Fark üç yerde çıkar. Birincisi, adayın sorunu **nasıl fark ettiği**; ikincisi, `Include`'un kendi tuzağını bilip bilmediği; üçüncüsü, ne zaman `Include` yerine projeksiyon seçtiği. Görüşmeci bu soruda üretilen SQL'e bakan bir mühendis arıyor.

**Sözlü cevap iskeleti**

1. **Oluşma biçimini anlat.** Bir listede yüz sipariş çekersin, sonra her siparişin kalemlerine erişirsin. Erişim başına ayrı bir sorgu gider; toplam bir artı yüz olur. Sorun tek sorgunun yavaşlığı değil, gidiş dönüş sayısıdır.
2. **İki ekosistemin farkını burada söyle.** Hibernate'te koleksiyonlar varsayılan olarak tembeldir ve N+1 sessizce oluşur. EF Core'da tembel yükleme kendiliğinden açık değildir; ayrı paket ve yapılandırma ister. Bu yüzden EF Core'da N+1 daha çok **döngü içinde sorgu yazmaktan** doğar.
3. **Tespiti çözümden önce koy.** Üretilen SQL loglanır ve tek bir iş için kaç komut gittiği sayılır. Bu, bu sorunun en değerli cümlesidir; ölçmeden yapılan düzeltme tahmindir (`82`).
4. **Çözümü ver ve JPA karşılığını kur.** `Include` ve `ThenInclude` ilişkiyi aynı sorguda getirir; Java'daki `JOIN FETCH` ve `@EntityGraph` ile aynı işi yapar.
5. **`Include`'un tuzağını söyle.** İki ayrı koleksiyon birden `Include` edilirse satırlar çarpılır; buna kartezyen patlama denir. Üç sipariş kalemi ve dört ödeme hareketi on iki satır üretir ve aynı veri tekrar tekrar taşınır.
6. **Bölünmüş sorguyu getir.** `AsSplitQuery` her koleksiyonu ayrı komutla çeker; çarpım kalkar. Bedeli de söyle: gidiş dönüş sayısı artar ve komutlar tek bir işlem içinde değilse aradaki değişiklikler tutarsız bir tablo verebilir.
7. **Projeksiyonu bir seçenek olarak sun.** Ekranda beş sütun gösteriliyorsa varlık ağacını hiç getirme; `Select` ile yalnız o beş sütunu çek. Okuma tarafında en ucuz çözüm çoğu zaman budur.
8. **Çeviri sınırını ekle.** Yazdığın ifade SQL'e çevrilemezse iş istemci tarafına düşebilir ve tablo tamamen belleğe çekilir. Üretilen SQL'i okumak bunu da yakalar.

```svg
<svg viewBox="0 0 660 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="EF Core N+1 olusumu, uc cozum yolu ve tespit yontemi">
  <defs>
    <marker id="efc-nplus-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sorun sorgunun hızında değil, gidiş dönüş sayısındadır</text>
  <rect x="14" y="36" width="632" height="76" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">N+1 nasıl oluşur</text>
  <text x="330" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">1 sorgu yüz siparişi getirir · her siparişin kalemlerine erişim ayrı bir sorgu açar · toplam 1 + 100</text>
  <text x="330" y="98" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Hibernate'te koleksiyon tembeldir ve sessizce oluşur · EF Core'da çoğunlukla döngü içinde sorgudan doğar</text>
  <rect x="14" y="124" width="204" height="122" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="145" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Include / ThenInclude</text>
  <text x="116" y="167" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ilişkiyi aynı sorguda getirir</text>
  <text x="116" y="187" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">JPA: JOIN FETCH, @EntityGraph</text>
  <text x="116" y="211" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">tuzak: iki koleksiyon birden</text>
  <text x="116" y="229" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">kartezyen patlama üretir</text>
  <rect x="228" y="124" width="204" height="122" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="145" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Bölünmüş sorgu</text>
  <text x="330" y="167" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her koleksiyon ayrı komutla</text>
  <text x="330" y="187" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">satır çarpımı ortadan kalkar</text>
  <text x="330" y="211" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">bedel: gidiş dönüş artar</text>
  <text x="330" y="229" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">tek işlem yoksa görüntü kayabilir</text>
  <rect x="442" y="124" width="204" height="122" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="544" y="145" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Projeksiyon (Select)</text>
  <text x="544" y="167" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">varlık ağacı hiç kurulmaz</text>
  <text x="544" y="187" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız ekranda görünen sütun</text>
  <text x="544" y="211" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">okuma tarafında en ucuzu</text>
  <text x="544" y="229" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">yazma yapacaksan uygun değil</text>
  <line x1="330" y1="250" x2="330" y2="266" stroke="#5D7789" stroke-width="1.4" marker-end="url(#efc-nplus-ok)"/>
  <rect x="14" y="272" width="632" height="62" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="293" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Tespit çözümden önce gelir</text>
  <text x="330" y="313" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Üretilen SQL loglanır · tek iş için kaç komut gittiği sayılır · sayıyı sabitleyen bir test yazılır.</text>
  <text x="330" y="329" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">Çevrilemeyen ifade işi istemciye düşürür; tablonun tamamı belleğe çekilebilir.</text>
</svg>
```

**Elenme sebepleri**

- Sorunu yalnız "yavaşlık" olarak anlatmak. N+1'in belirtisi süre değil, komut sayısıdır.
- Tespitten hiç söz etmemek. Üretilen SQL'e bakmayan aday, aynı hatayı bir sonraki uç noktada tekrar yapar.
- `Include`'u tek çözüm sanmak. Yalnız okunacak veride projeksiyon çoğu zaman daha ucuzdur.
- Kartezyen patlamayı bilmemek. İki koleksiyonu birden `Include` etmek çözüm gibi görünüp yeni bir sorun üretir.
- Bölünmüş sorgunun bedelini atlamak. Ayrı komutlar tek işlem içinde değilse tutarsız bir görüntü çıkabilir.
- Tembel yüklemeyi açmayı çözüm sanmak. Tembel yükleme N+1'in kaynağıdır, çaresi değildir.
- Her ilişkiyi hevesle `Include` etmek. Gereksiz veri getirmek de bir performans sorunudur.

**Kendi hikâyen — dürüst köprü**

N+1 senin en rahat anlatacağın konu; Java tarafında bunu büyük ihtimalle yaşadın. Şu sorular hikâyeyi çıkarır:

- Hangi ekranda ya da uç noktada N+1 çıktı? Liste ekranı mıydı, dışa aktarım mıydı?
- Nasıl fark ettin? SQL logu mu, yavaş uç nokta alarmı mı, veri tabanı tarafındaki sorgu sayısı mı?
- Kaç sorgu gidiyordu, düzelttikten sonra kaça indi? Bu iki sayı hikâyenin omurgasıdır.
- `JOIN FETCH` mi kullandın, `@EntityGraph` mi, yoksa DTO projeksiyonuna mı geçtin? Neden o?
- Birden fazla koleksiyonu birlikte getirmeye çalıştın mı? Ne oldu?
- Aynı hatayı engelleyen bir şey bıraktın mı? Sorgu sayısını sabitleyen bir test yeterlidir.

Deneyimin varsa olduğu gibi anlat, sonra köprüyü kur: "EF Core'da bunun karşılığı `Include` ve `ThenInclude`, kartezyen patlamanın çaresi ise bölünmüş sorgu." Bunu okuyarak bildiğini söylemek, kullandığını uydurmaktan güçlüdür.

### "Göç (migration) yönetimini ve üretim şemasını nasıl kurgularsın?"

**Soru aslında ne ölçüyor?**

Bu soru araç bilgisi ölçmez; üretim disiplini ölçer. Görüşmeci şunu arıyor: aday şema değişikliğini bir dağıtım (deployment) olayı olarak görüyor mu. Bankacılık ve ödeme tarafında bu daha da ağırdır, çünkü şema değişikliği çoğu zaman denetimden geçer ve geri alınamaz. Zayıf cevap komut adı sayar; güçlü cevap sıra, onay ve geri dönüş planı anlatır.

**Sözlü cevap iskeleti**

1. **Modeli tarif et.** EF Core göçleri modeldeki değişiklikten üretilir; her göç ileri ve geri adımlarını içerir. Uygulanan göçler veri tabanındaki bir geçmiş tablosunda tutulur.
2. **Java karşılığını kur ve farkı adlandır.** Flyway ve Liquibase betik önceliklidir; sen SQL'i yazarsın. EF Core model önceliklidir; aracı sen değil, model farkı yönlendirir. Ortak nokta ikisinde de aynıdır: sıralı, sürümlenmiş ve kayıt altına alınmış bir geçmiş (`220`).
3. **Üretimde otomatik göçe itiraz et.** Uygulama açılışında göç uygulamak üç şey yapar: kimsenin görmediği bir şema değişikliği çalıştırır, çalışma anında yüksek veri tabanı yetkisi ister ve dağıtımı geri almayı zorlaştırır. Doğru yol, göç betiğini önceden üretip incelemek ve dağıtımın ayrı bir adımında uygulamaktır.
4. **Betiğin tekrar edilse de sonucu değişmeyen (idempotent) olmasını iste.** Aynı betik iki kez koşarsa ikinci koşu bir şey yapmamalıdır. Bu, otomatik dağıtım hatlarının en temel güvenliğidir.
5. **Geri alma iddiasını düzelt.** Geri adımı yazmak iyi bir alışkanlıktır ama üretim geri dönüş planı değildir. Silinen sütunun verisi geri gelmez. Üretimde yol ileriye doğru düzeltmektir.
6. **Genişlet ve daralt kalıbını anlat.** Kademeli dağıtımda eski ve yeni kod bir süre birlikte çalışır. Bu yüzden sıra şudur: yeni sütun boş kabul edilir biçimde eklenir, kod iki alana birden yazar, geçmiş veri doldurulur, alan zorunlu yapılır, en sonunda eski sütun kaldırılır. Her adım kendi başına dağıtılabilir olmalıdır (`213`).
7. **Kilit ve süreyi hesaba kat.** Büyük tabloda şema değişikliği kilit üretebilir ve gün sonu penceresine çakışabilir. Hangi işlemin çevrimiçi yapılabildiği sürümüne ve sürüm türüne göre değişir; dokümandan teyit edilir ve önce üretime yakın bir veri kümesinde denenir.
8. **Veri göçünü şema göçünden ayır.** Milyonlarca satırı dolduran bir işi göç adımının içine koymak dağıtımı kilitler. Doldurma işi parçalara bölünmüş, durdurulabilir ve devam ettirilebilir ayrı bir iş olmalıdır.

**Elenme sebepleri**

- "Uygulama açılışta göçü uygular" cevabını sorgusuz vermek. Üretim tarafında en hızlı eleyen cevaplardan biridir.
- Geri adımı üretim geri dönüş planı saymak. Silinen veri geri alınmaz; bu cümle deneyimsizliği hemen gösterir.
- Kademeli dağıtımı hesaba katmamak. Eski ve yeni kodun aynı anda çalıştığı pencereyi görmeyen aday şemayı kırar.
- Şemayı elle değiştirmek. Sürümlenmemiş her değişiklik ortamlar arasında sessiz bir fark üretir.
- Veri doldurmayı göç adımının içine koymak. Uzun süren tek bir işlem hem kilitler hem yarıda kalır.
- Göç dosyasını üretim sonrası düzenlemek. Uygulanmış bir göçü değiştirmek ortamları geri dönülmez biçimde ayırır.
- Kilit ve bakım penceresini hiç anmamak. Gün sonu koşan bir sistemde şema değişikliğinin saati bir tasarım kararıdır.

**Kendi hikâyen — dürüst köprü**

Bu başlıkta deneyimin doğrudan taşınır; araç adı değişir, disiplin aynıdır. Şu sorular hikâyeyi çıkarır:

- Şema değişikliklerini nasıl yönetiyordun? Flyway mı, Liquibase mi, elle betik mi?
- Betikler kod incelemesinden geçiyor muydu? Kim onaylıyordu?
- Kademeli dağıtımda eski kodun yeni şemayla çalıştığı bir pencere oldu mu? Ne yaşandı?
- Bir sütunu kaldırman gerekti mi? Kaç adımda yaptın, arada ne kadar bekledin?
- Büyük bir tabloya indeks ya da sütun eklerken kilit ya da süre sorunu yaşadın mı?
- Bir göç geri alınmak zorunda kaldı mı? Nasıl çözüldü: geri adım mı, ileri düzeltme mi?
- Ortamlar arasında şema farkı çıktı mı? Nasıl fark edildi?

Bunlar gerçek cevaplardır. Sonunda tek cümlelik köprüyü kur: "EF Core göçleri bu işin model tarafından üretilen hâlidir; disiplin aynı kalır, üretimde betik üretilip denetimli uygulanır."

### "İşlem (transaction) ve eşzamanlılık EF Core'da nasıl yönetilir?"

**Soru aslında ne ölçüyor?**

Bu dosyanın en ayrıştırıcı sorusu budur ve iki kat ölçer. Birinci kat işlem sınırıdır: aday sınırı nereye çiziyor. İkinci kat eşzamanlılıktır: aynı kaydı iki kişi güncellerse ne oluyor. Ödeme ve mutabakat tarafında ikinci kat özellikle önemlidir, çünkü sessiz üzerine yazma para farkı üretir. Ayrıca burada .NET'e özgü bir tuzak var ve adayların çoğu ona hazırlıksız gelir: `DbContext` iş parçacığı güvenli değildir.

**Sözlü cevap iskeleti**

1. **Varsayılanı söyle.** `SaveChanges` çağrısı, ürettiği bütün komutları tek bir işlem içinde çalıştırır. Yani tek bir kaydetme zaten atomiktir; bunun için ek bir şey yapmak gerekmez.
2. **Açık işlemin ne zaman gerektiğini ver.** Birden çok `SaveChanges` çağrısı tek bir bütün olacaksa, ya da okuma ile yazma aynı sınırda durmalıysa işlem açıkça başlatılır ve kesinleştirilir.
3. **Java ile farkı adlandır.** Spring'de sınır bildirimseldir; `@Transactional` bir vekil nesne (proxy) üzerinden uygulanır (`83`, `84`). .NET tarafında buna birebir karşılık gelen yerleşik bir öznitelik yoktur; sınır ya `SaveChanges` çağrısıdır ya da açıkça açtığın işlemdir. Bunun bir yan faydası vardır: aynı sınıf içinden çağırınca işlemin çalışmaması gibi bir vekil tuzağı da yoktur.
4. **İş parçacığı güvenliğini vurgula.** Bir `DbContext` aynı anda tek bir işlem yürütür. Aynı bağlamı paralel görevlere verirsen çalışma anında hata alırsın. Kural şudur: her paralel iş kendi bağlamını alır. Arka plan servisi tekil (singleton) yaşadığı için orada bağlam kapsam açarak ya da bir fabrika ile üretilir. Java'daki `EntityManager` de iş parçacığı güvenli değildir; Spring onu senin yerine iş parçacığına bağlar, .NET'te bu sorumluluk daha görünürdür.
5. **İyimser eşzamanlılığı kur.** Satır sürümü sütunu eşzamanlılık belirteci yapılır. EF Core güncelleme komutuna eski sürüm değerini koşul olarak ekler; etkilenen satır sayısı sıfır çıkarsa özel bir eşzamanlılık hatası fırlatır. **Bu, JPA'daki `@Version` ile birebir aynı fikirdir** (`225`). MS SQL tarafında bu iş için otomatik artan bir satır sürümü tipi vardır.
6. **Çatışma çözümünü de anlat.** Hata yakalandığında üç yol vardır: istemci kazanır, veri tabanı kazanır, ya da alan alan birleştirilir. Para alanında kör üzerine yazma seçilmez; kayıt yeniden okunur ve karar iş kuralına bırakılır.
7. **Kötümser kilidi ayrı bir araç olarak koy.** EF Core'da kilitli okuma için yerleşik bir ifade yoktur; MS SQL tarafında sorgu ipuçlarıyla ham SQL yazılır. JPA'daki kilit modlarına göre bu gerçek bir farktır ve söylemek iyi olur. Kısa süreli, dar kapsamlı ve sıralı erişimle kullanılır (`225`).
8. **Yeniden denemeyi ekle.** Geçici bağlantı hatalarına karşı yeniden deneme açılabilir. Ama senin açtığın bir işlem varsa, yeniden denenen şey tüm iş birimidir; bu yüzden iş biriminin tekrar edilse de sonucu değişmeyen (idempotent) olması gerekir.
9. **İzolasyon seviyesini bilinçli seç.** Varsayılana güvenmek yerine hangi anomaliyi kabul ettiğini söyle (`224`). Ödeme akışında bunun karşılığı somuttur: çift kayıt ve hayalet okuma para farkı demektir.

**Elenme sebepleri**

- Her `SaveChanges` için ayrı işlem açmak. Gereksizdir ve işlem sınırının nasıl çalıştığını bilmediğini gösterir.
- `DbContext`'i paylaşmak. Tekil kaydetmek ya da paralel görevlere aynı bağlamı vermek bu soruda anında eler.
- İyimser kilidi hiç anmamak. Aynı kaydı iki kişi güncelleyebiliyorsa sessiz üzerine yazma bir hata değil, tasarım eksikliğidir.
- Önce okuyup sonra karşılaştırmayı çözüm sanmak. Okuma ile yazma arasında pencere kalır; bu bir yarış koşuludur.
- İzolasyon seviyesini yükselterek her sorunu çözmeye çalışmak. Kilit süresi ve iş hacmi bedeli konuşulmalıdır.
- Çatışma yakalandığında kullanıcıya hiçbir şey söylememek. Sessizce ezmek para tarafında en pahalı seçenektir.
- Uzun süren dış çağrıyı işlem içinde tutmak. Açık işlem boyunca kilit tutulur ve kilitlenme olasılığı artar.

**Kendi hikâyen — dürüst köprü**

Bu başlıkta Java deneyimin doğrudan kullanılır. Şu sorular onu çıkarır:

- İşlem sınırını nereye çiziyordun? Servis metodunda mı, daha dışarıda mı?
- `@Transactional`ın çalışmadığı bir durum yaşadın mı? Aynı sınıf içinden çağırma tuzağına düştün mü?
- `@Version` kullandığın bir varlık oldu mu? Çatışma hatası kullanıcıya nasıl gösterildi?
- Sessiz üzerine yazma yaşandı mı? Nasıl fark edildi: müşteri şikâyeti mi, mutabakat farkı mı?
- Kilitlenme (deadlock) gördün mü? Erişim sırasını değiştirerek mi çözdün, işlemi kısaltarak mı?
- Bir işlem içinde dış servis çağırdığın oldu mu? Sonucunda ne yaşandı?
- Toplu işte paralel çalışan iş parçacıkları aynı kaynağı paylaştı mı? Nasıl ayırdın?

Bunlardan sonra köprü tek cümledir: "EF Core'da bu fikrin adı satır sürümüdür ve `@Version` ile aynı işi yapar; farkı, `DbContext`'in paylaşılamamasına daha dikkat etmem gerektiği."

### "MS SQL'de sorgu performansını nasıl iyileştirirsin; Oracle'dan farkı nedir?"

**Soru aslında ne ölçüyor?**

Bu soru ilanın MS SQL maddesinin karşılığıdır ve senin için özel bir fırsat taşır. Sen Oracle tarafını biliyorsun (`227`). Görüşmecinin duymayı beklediği şey ezber bir indeks anlatımı değil; iki veri tabanının **gerçekten farklı** olduğu yerleri adlandırabilmen. İki başlık bunu tek başına yapar: kümelenmiş indeks ayrımı ve parametre koklaması. İkincisinin adını söyleyen aday sayısı azdır.

**Sözlü cevap iskeleti**

1. **Yöntemi baştan koy.** Önce yürütme planına bakılır, sonra değişiklik yapılır. Planda aranan şey maliyet yüzdesi değil, kaç satır okunduğu ve tahmin ile gerçeğin ne kadar ayrıştığıdır (`146`, `29`).
2. **Kümelenmiş indeksi anlat; bu MS SQL'in belirgin özelliğidir.** Kümelenmiş indeks tablonun kendisidir: yaprak sayfalarda satırların tamamı, anahtar sırasına göre durur. Bu yüzden tabloda yalnız bir tane olabilir.
3. **Kümelenmemiş indeksi karşısına koy.** Onun yaprağında yalnız indeks anahtarı ve satıra giden bir işaretçi vardır. Sorgu indekste olmayan bir sütun isterse veri tabanı satırın kendisine geri gitmek zorunda kalır; buna anahtar arama (key lookup) denir ve satır sayısı büyüdükçe pahalılaşır.
4. **Kapsayan indeksi çözüm olarak ver.** Sorgunun ihtiyaç duyduğu sütunlar indekse dahil edilirse geri gidiş ortadan kalkar. Buna kapsayan indeks denir ve MS SQL'de `INCLUDE` ile kurulur.
5. **Kümeleme anahtarının seçimini bir tasarım kararı olarak sun.** Kümelenmiş anahtar bütün kümelenmemiş indekslerin içinde tekrar eder. Bu yüzden dar ve artan bir anahtar seçilir; geniş ya da rastgele anahtar hem indeksleri şişirir hem sayfa bölünmesi üretir.
6. **Oracle farkını burada söyle.** Oracle'da varsayılan tablo bir yığın (heap) tablodur ve indeks yaprağında satır kimliği durur. Kümelenmiş indeksin karşılığı indeks düzenli tablodur ve varsayılan değildir. Yani MS SQL'de her tabloda verdiğin bir karar, Oracle'da özel bir tercihtir (`227`).
7. **Parametre koklamasını getir; bu ayrıştırıcıdır.** MS SQL parametreli bir sorgunun planını ilk çalıştırmadaki parametre değerine göre üretir ve önbelleğe alır. Veri dağılımı çarpıksa aynı sorgu bir değer için çok hızlı, başka bir değer için çok yavaş çalışır.
8. **Belirtiyi tarif et; asıl kanıt budur.** Kod değişmediği hâlde aynı sorgu bazen hızlı bazen çok yavaştır. İstatistik güncellemesi ya da yeniden başlatma sonrası davranış değişir. Bu tabloyu tarif eden aday sorunu yaşamış gibi konuşur.
9. **Çareleri say, ama abartma.** Plan yeniden derletilebilir, bilinmeyen değer için iyimser plan istenebilir, sorgu ikiye ayrılabilir ya da daha seçici bir indeks eklenebilir. Plan geçmişini tutan ve bir planı sabitlemeye izin veren araçlar vardır; ayrıntısı sürümüne göre değişir, dokümandan teyit edilir.
10. **Okuyucu ile yazıcı ilişkisini kur.** MS SQL'in varsayılan okuma seviyesi paylaşımlı kilit kullanır; okuyucu yazıcıyı bekletebilir. Anlık görüntüye dayalı okuma kesinleşmiş seviyesi açıldığında okuma satır sürümlerinden yapılır ve bu engelleme kalkar. Oracle'da okuyucular zaten yazıcıları engellemez; bu yüzden Oracle'dan gelen bir ekip MS SQL'deki beklemeyi beklemez. Bedeli de vardır: sürüm deposu yer tutar (`224`).
11. **Kilitlenmeyi ve `NOLOCK`'u kapanışa koy.** Kilitlenme genellikle ters sıralı erişimden doğar; çare tutarlı erişim sırası, kısa işlemler ve doğru indekstir. Kurban seçilen işlem yeniden denenir, bu yüzden iş tekrar güvenli olmalıdır. `NOLOCK` ise çözüm değildir: kesinleşmemiş veri okunur, satır atlanabilir ya da iki kez görülebilir. Mutabakat raporunda bu kabul edilemez.

```svg
<svg viewBox="0 0 660 386" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MS SQL kumelenmis ve kumelenmemis indeks farki, kapsayan indeks, Oracle kiyasi ve parametre koklamasi">
  <defs>
    <marker id="efc-mssql-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">MS SQL'in ayrımı: kümelenmiş indeks tablonun kendisidir</text>
  <rect x="14" y="36" width="300" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="164" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kümelenmiş indeks (clustered)</text>
  <text x="164" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yaprak sayfada satırın tamamı durur</text>
  <text x="164" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">satırlar anahtar sırasına göre dizilir</text>
  <text x="164" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tabloda yalnız bir tane olabilir</text>
  <text x="164" y="139" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">anahtar dar ve artan seçilir</text>
  <text x="164" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">geniş anahtar tüm indeksleri şişirir</text>
  <rect x="346" y="36" width="300" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="496" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kümelenmemiş indeks (nonclustered)</text>
  <text x="496" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yaprakta anahtar ve satır işaretçisi var</text>
  <text x="496" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eksik sütun için satıra geri gidilir</text>
  <text x="496" y="119" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bu geri gidişin adı: anahtar arama</text>
  <text x="496" y="139" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">INCLUDE ile kapsayan indeks kurulur</text>
  <text x="496" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kapsayan indekste geri gidiş kalkar</text>
  <line x1="440" y1="182" x2="250" y2="182" stroke="#B23B3B" stroke-width="1.4" marker-end="url(#efc-mssql-ok)"/>
  <text x="345" y="196" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">satır sayısı büyüdükçe anahtar arama pahalılaşır</text>
  <rect x="14" y="206" width="300" height="88" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="164" y="227" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Oracle'dan fark</text>
  <text x="164" y="248" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Oracle'da varsayılan tablo yığın tablodur</text>
  <text x="164" y="266" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karşılığı indeks düzenli tablo, varsayılan değil</text>
  <text x="164" y="285" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Oracle'da okuyucu yazıcıyı engellemez</text>
  <rect x="346" y="206" width="300" height="88" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="496" y="227" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Parametre koklama (parameter sniffing)</text>
  <text x="496" y="248" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">plan ilk çalıştırmanın değerine göre üretilir</text>
  <text x="496" y="266" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">önbelleğe alınır, sonraki değerlere de uygulanır</text>
  <text x="496" y="285" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">dağılım çarpıksa bir değer için çok yavaş olur</text>
  <rect x="14" y="304" width="632" height="72" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="325" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Belirtiyi tarif etmek, çareyi ezberlemekten değerlidir</text>
  <text x="330" y="345" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kod değişmedi ama sorgu bazen hızlı bazen çok yavaş · istatistik ya da yeniden başlatma sonrası davranış değişti.</text>
  <text x="330" y="364" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">NOLOCK bir çare değildir: kesinleşmemiş veri okunur, satır atlanabilir ya da iki kez görülebilir.</text>
</svg>
```

**Elenme sebepleri**

- Kümelenmiş ile kümelenmemiş indeksi ayıramamak. MS SQL isteyen bir ilanda bu ayrım temel kabul edilir.
- Her tabloya çok sayıda indeks önermek. Her indeks yazma maliyetidir; gün sonu yükünde bu doğrudan hissedilir.
- Anahtar aramayı görmezden gelmek. Planda geri gidişi fark etmeyen aday kapsayan indeks fikrine hiç ulaşamaz.
- Parametre koklamasını bilmemek. Sorunu "bazen yavaşlıyor" diye bırakmak, teşhis edemediğini gösterir.
- `NOLOCK`'u performans çözümü olarak sunmak. Mutabakat ve tutar raporunda bu tek başına eleyicidir.
- Sütun üzerine fonksiyon yazıp indeksin kullanılmasını beklemek. İndeksi devre dışı bırakan en yaygın hatadır.
- Oracle ile MS SQL'i tamamen aynı saymak. İki motorun kilit ve okuma davranışı gerçekten farklıdır.
- Planı hiç açmadan indeks eklemek. Ölçmeden yapılan her iyileştirme bir tahmindir.

**Kendi hikâyen — dürüst köprü**

Burada anlatacağın deneyim büyük ihtimalle Oracle tarafındandır ve bu tam olarak istenen şeydir. Şu sorular onu çıkarır:

- Hangi sorguyu yürütme planına bakarak düzelttin? Planda seni asıl yere ne götürdü?
- Eksik indeks yüzünden yavaşlayan bir uç nokta oldu mu? İndeksi hangi sütunlarla kurdun?
- İndeks eklemenin yazma tarafına maliyeti konuşuldu mu? Kim itiraz etti, nasıl karar verildi?
- Aynı sorgunun bazen hızlı bazen yavaş çalıştığı bir durumu yaşadın mı? Nasıl açıkladınız?
- Kilitlenme ya da uzun bekleme gördün mü? Erişim sırasını mı değiştirdiniz, işlemi mi kısalttınız?
- Gün sonu ya da ay sonu penceresinde sorgu davranışı değişti mi? Veri hacmi mi, plan mı değişti?
- Bir sorguyu ne kadar hızlandırdın? Önce ve sonra sayıları hikâyenin en güçlü kısmıdır.

Ardından köprüyü kur: "Oracle tarafında plan okuma ve indeks tasarımı yaptım; MS SQL'de bunun üstüne öğrenmem gereken iki şey var, kümelenmiş indeks seçimi ve parametre koklaması." Bu cümle dürüsttür, hazırlıklı olduğunu gösterir ve uydurulmuş bir deneyimden her zaman güçlüdür (`256`).

### Bu bölümden çıkan ders

Bu beş sorunun tamamı aynı yerden beslenir: kalıcılık katmanının fikirleri iki ekosistemde de aynıdır, değişen kelimelerdir. İzleme kirli kontroldür, `AsNoTracking` salt okunur sorgudur, `Include` `JOIN FETCH`'tir, satır sürümü `@Version`'dır, göç araçları Flyway'in yerinde durur. Bu eşlemeyi kurabilen aday, EF Core yazmamış olsa bile kalıcılık katmanını yönetebileceğini kanıtlar. Buna karşılık gerçekten yeni olan üç şey vardır ve mülakatta ayrımı onlar yapar: `DbContext`'in iş parçacığı güvenli olmaması, kümelenmiş indeksin tablonun kendisi olması ve parametre koklaması. Bu üçünü adlandırıp bedelini söylemek, on tane komut adı saymaktan daha çok konuşur. Ve hiçbir cümlede olmayan bir C# deneyimi uydurulmaz; fikri bildiğini söylemek, kullandığını uydurmaktan her zaman güçlüdür.

# efc-1 | Orta
## Soru
Bir rapor uç noktası elli bin kaydı ekranda göstermek için okuyor; hiçbir güncelleme yapmıyor. Bellek yüksek ve yanıt yavaş. İlk ve en yerinde adım hangisidir?
## A
Sorguyu izlemesiz çalıştırmak, tercihen `Select` ile yalnız gereken sütunları çekmek; JPA'daki salt okunur sorgunun karşılığıdır
🤖 Doğru: yazma niyeti yoksa izlemenin maliyeti bedavaya ödenir.
## B
`DbContext`'i tekil (singleton) kaydetmek; böylece her istekte yeni bağlam kurma maliyeti ortadan kalkar
🤖 tekil bağlam hem birikir hem eşzamanlılık hatası üretir; sorunu büyütür.
## C
Sorguyu açık bir işlem (transaction) içine almak; işlem sınırı belirlenince izleme maliyeti düşer
🤖 işlem sınırı ile izleme maliyeti ayrı şeylerdir; biri diğerini değiştirmez.
## D
Tüm ilişkileri `Include` ile birlikte getirmek; tek sorguda gelen veri her zaman daha az bellek tutar
🤖 daha çok veri getirmek belleği artırır; ayrıca sorun ilişkilerde değil.
## Doğru
A
## Açıklama
`DbContext`, kendisi üzerinden yüklenen her varlığın ilk hâlinin bir kopyasını tutar. `SaveChanges` bu kopyayla güncel hâli karşılaştırır ve yalnız değişen sütunlar için komut üretir. Bu, JPA'daki kalıcı bağlam ve kirli kontrolün birebir aynısıdır (`80`, `81`). Fikir aynı olduğu için bedeli de aynıdır: her izlenen varlık bellekte iki kez durur. Yüz kayıtta fark edilmez, elli binde belirgin olur. Yazma niyeti yoksa izleme kapatılır; bu `AsNoTracking` ile yapılır ve Java tarafındaki salt okunur sorguya karşılık gelir. Bir adım daha ileri gitmek genellikle daha iyidir: `Select` ile dar bir nesneye projeksiyon yaparsan ortada izlenecek varlık kalmaz, üstelik daha az sütun okunur ve ağdan daha az veri geçer. B şıkkı en tehlikeli seçenektir; `DbContext` kısa ömürlü olmalıdır ve paylaşılamaz. C şıkkı iki ayrı kavramı birbirine bağlar. D şıkkı ise sorunu büyütür. Tek uyarı şudur: yazma yapacağın sorguda izlemeyi kapatırsan değişiklik sessizce kaydedilmez.

# efc-2 | İleri
## Soru
Bir sorgu, siparişle birlikte hem kalemleri hem ödeme hareketlerini `Include` ediyor. Dönen satır sayısı beklenenin çok üstünde ve sorgu yavaş. Doğru teşhis ve çözüm hangisidir?
## A
`Include` ifadeleri kaldırılıp tembel yüklemeye geçilir; ilişkiler ihtiyaç anında tek tek yüklenir
🤖 bu, kartezyen patlamayı N+1 ile değiştirir; sorun yer değiştirir.
## B
Sorguya izlemesiz çalışma eklenir; izleme kapatılınca veri tabanından dönen satır sayısı düşer
🤖 izleme istemci tarafındadır; üretilen SQL'i ve satır sayısını değiştirmez.
## C
İki koleksiyonun çarpımı kartezyen patlama üretir; bölünmüş sorgu ya da projeksiyon kullanılır
🤖 Doğru: her koleksiyon ayrı komutla çekilince çarpım kalkar.
## D
İki `Include` tek bir `ThenInclude` zincirine çevrilir; zincir hâline gelen sorgu çarpımı kaldırır
🤖 `ThenInclude` derinliği ifade eder, genişliği değil; çarpım yerinde kalır.
## Doğru
C
## Açıklama
İki ayrı koleksiyon aynı sorguda getirildiğinde veri tabanı bunların çarpımını üretir. Üç sipariş kalemi ve dört ödeme hareketi on iki satır demektir ve sipariş bilgisi on iki kez tekrar taşınır. Buna kartezyen patlama denir; Hibernate tarafında da aynı sorun vardır ve orada birden çok koleksiyonu birlikte getirmeye çalışmak çoğu zaman doğrudan hata verir (`82`). Çözüm iki tanedir. Birincisi bölünmüş sorgudur: her koleksiyon ayrı bir komutla çekilir, çarpım ortadan kalkar. Bedeli söylenmelidir; gidiş dönüş sayısı artar ve komutlar tek bir işlem içinde değilse aradaki değişiklikler tutarsız bir görüntü verebilir. İkincisi projeksiyondur: ekranda yalnız birkaç sütun görünüyorsa varlık ağacını hiç kurma, `Select` ile o sütunları çek. A şıkkı sorunu N+1'e çevirir; tembel yükleme çözüm değil, N+1'in kaynağıdır. B şıkkı izlemeyi SQL üreticisi sanır; izleme istemci tarafındadır. D şıkkı derinlik ile genişliği karıştırır. Her durumda ilk adım aynıdır: üretilen SQL'i logla ve tek iş için kaç komut gittiğini say.

# efc-3 | İleri
## Soru
Bir dağıtım hattı, uygulama açılışında EF Core göçlerini otomatik uyguluyor. Sistem gün sonu koşan bir ödeme uygulaması. En yerinde itiraz hangisidir?
## A
Her göç için geri adım yazmak yeterlidir; sorun çıkarsa şema önceki hâline döndürülür
🤖 geri adım silinen veriyi geri getirmez; üretim geri dönüş planı değildir.
## B
Şema değişikliği kimse görmeden çalışır; betik önceden üretilip incelenmeli ve ayrı bir adımda uygulanmalıdır
🤖 Doğru: şema değişikliği bir dağıtım kararıdır, açılış yan etkisi değil.
## C
Sakıncası yoktur; açılışta göç uygulandığı sürece kod ile şema her zaman uyumlu kalır
🤖 kademeli dağıtımda eski ve yeni kod bir süre birlikte çalışır; uyum garantisi yoktur.
## D
Bütün göçler tek bir büyük göçte birleştirilmelidir; adım sayısı azalınca risk de azalır
🤖 tek adımda daha çok şey değişir; hata alanı büyür, teşhis zorlaşır.
## Doğru
B
## Açıklama
Açılışta otomatik göç uygulamak üç ayrı sorun üretir. Birincisi görünürlüktür: şema değişikliği hiç incelenmeden üretimde çalışır. İkincisi yetkidir: uygulama çalışma anında şema değiştirebilecek kadar geniş bir veri tabanı yetkisi taşımak zorunda kalır. Üçüncüsü zamanlamadır; gün sonu koşan bir sistemde şema değişikliğinin hangi saatte çalıştığı bir tasarım kararıdır. Doğru yol şudur: göç betiği önceden üretilir, kod incelemesinden geçer ve dağıtımın ayrı, denetimli bir adımında uygulanır. Betiğin tekrar edilse de sonucu değişmeyen (idempotent) olması gerekir (`220`). A şıkkı en sık duyulan yanılgıdır; geri adım şemayı çevirse bile silinen sütunun verisini geri getirmez, üretimde yol ileriye doğru düzeltmektir. C şıkkı kademeli dağıtımı hesaba katmaz: eski ve yeni kod bir süre birlikte çalışır, bu yüzden genişlet ve daralt kalıbı kullanılır. Önce boş kabul edilen sütun eklenir, kod iki alana birden yazar, geçmiş veri parça parça doldurulur, alan zorunlu yapılır, en sonunda eski sütun kaldırılır (`213`). D şıkkı hata alanını büyütür.

# efc-4 | Uzman
## Soru
Gün sonu dosyalarını işleyen bir arka plan servisi, hızlansın diye kayıtları paralel görevlere bölüyor ve hepsi aynı `DbContext` örneğini kullanıyor. Çalışma anında "bu bağlamda ikinci bir işlem başlatıldı" hatası alınıyor. En doğru düzeltme hangisidir?
## A
Bağlama erişen bütün çağrılar bir kilit (`lock`) ile sarılır; böylece aynı anda tek işlem çalışır
🤖 hatayı susturur ama paralelliği yok eder; uzun ömürlü izleyici de büyümeye devam eder.
## B
Sorgulara izlemesiz çalışma eklenir; izleme kapatılınca bağlam eşzamanlı erişime dayanır
🤖 izlemeyi kapatmak bağlamı iş parçacığı güvenli yapmaz; sorun ilgisiz bir yerde.
## C
Bağlam tekil (singleton) kaydedilir; tek örnek olduğu için erişim kendiliğinden düzene girer
🤖 tam tersi olur; paylaşım artar, çakışma ve bellek birikimi büyür.
## D
Her paralel iş kendi bağlamını alır; kapsam açılarak ya da bir bağlam fabrikasıyla üretilir
🤖 Doğru: `DbContext` iş parçacığı güvenli değildir, paylaşılmaz.
## Doğru
D
## Açıklama
`DbContext` **iş parçacığı güvenli değildir**; aynı anda tek bir işlem yürütür. Aynı örneği paralel görevlere verirsen bu hata çalışma anında ortaya çıkar. Kural nettir: her paralel iş kendi bağlamını alır. Burada ikinci bir tuzak daha var. Arka plan servisleri tekil yaşar, oysa `AddDbContext` bağlamı kapsamlı (scoped) kaydeder. Bu yüzden tekil bir servis bağlamı doğrudan kuruculuğundan alamaz; ya bir kapsam açar ya da bağlam fabrikası kullanır. Java tarafında kural aynıdır: `EntityManager` de iş parçacığı güvenli değildir. Fark şudur ki Spring onu senin yerine iş parçacığına bağlar, .NET'te bu sorumluluk daha görünür durur. A şıkkı hatayı susturur ama paralelliği anlamsızlaştırır ve uzun ömürlü bağlamın bellekte birikmesini çözmez. B şıkkı izlemeyi eşzamanlılık aracı sanır. C şıkkı sorunu doğrudan büyütür. Bu soru gün sonu bağlamında ayrıca önemlidir: paralel işlenen dosya parçaları aynı kaydı güncelleyebilir, bu yüzden bağlam ayrımının üstüne bir de eşzamanlılık belirteci gerekir (`225`).

# efc-5 | İleri
## Soru
Bir mutabakat kaydını iki operatör aynı anda açıp güncelliyor. İkincinin kaydı, birincinin değişikliğini sessizce eziyor. EF Core'da doğru yaklaşım ve JPA karşılığı hangisidir?
## A
İzolasyon seviyesi en yükseğe çekilir; böylece iki güncelleme sıraya girer ve kayıp güncelleme kalmaz
🤖 kayıtlar ayrı işlemlerde ve arada kullanıcı düşünme süresi var; seviye bunu kapsamaz.
## B
Okuma sorguları `NOLOCK` ipucuyla çalıştırılır; kilit beklemesi kalkınca çakışma da ortadan kalkar
🤖 kilitsiz okuma yazma çakışmasını çözmez; üstüne kesinleşmemiş veri okutur.
## C
Satır sürümü sütunu eşzamanlılık belirteci yapılır; sürüm uyuşmazsa güncelleme hata verir — JPA'daki `@Version` ile aynı fikir
🤖 Doğru: güncelleme koşula bağlanır, etkilenen satır sıfırsa çakışma bildirilir.
## D
Kaydetmeden hemen önce satır yeniden okunur ve değerler karşılaştırılır; farklıysa işlem durdurulur
🤖 okuma ile yazma arasında pencere kalır; klasik bir yarış koşuludur.
## Doğru
C
## Açıklama
Bu, iyimser eşzamanlılığın ders kitabı örneğidir. Çözüm satır sürümü sütunudur: MS SQL'de bu iş için otomatik artan bir satır sürümü tipi vardır ve EF Core'da eşzamanlılık belirteci olarak işaretlenir. O andan sonra EF Core güncelleme komutuna eski sürüm değerini koşul olarak ekler. Başkası araya girmişse etkilenen satır sayısı sıfır çıkar ve özel bir eşzamanlılık hatası fırlatılır. **Bu, JPA'daki `@Version` ile birebir aynı fikirdir**; kelime değişti, mekanizma aynı kaldı (`225`). Hatayı yakalamak yetmez, çözüm politikası da söylenmelidir: istemci kazanır, veri tabanı kazanır ya da alan alan birleştirilir. Para ve mutabakat alanında kör üzerine yazma seçilmez; kayıt yeniden okunur, fark kullanıcıya gösterilir ve karar iş kuralına bırakılır. A şıkkı izolasyon seviyesini çare sanar; oysa iki güncelleme ayrı işlemlerdedir ve arada kullanıcının ekranda geçirdiği süre vardır (`224`). B şıkkı hem sorunu çözmez hem kesinleşmemiş veri okutur. D şıkkı önce kontrol edip sonra yazar; iki adım arasında pencere kalır ve yarış koşulu sürer. Doğru olan, kontrolü güncelleme komutunun kendisine gömmektir.

# efc-6 | Uzman
## Soru
Parametreli bir mutabakat raporu sorgusu üye iş yerlerinin çoğu için hızlı, biri için çok yavaş çalışıyor. Kod değişmedi; sunucu yeniden başlatıldıktan sonra yavaşlayan taraf bazen değişiyor. En olası açıklama hangisidir?
## A
Parametre koklama (parameter sniffing); önbellekteki plan ilk çalıştırmanın değerine göre üretilmiş, dağılım çarpık
🤖 Doğru: aynı plan çok farklı satır sayıları için yeniden kullanılıyor.
## B
Bağlantı havuzu küçük kalmış; havuz dolunca bekleyen istekler sırayla ilerlediği için sorgu yavaşlıyor
🤖 havuz darlığı belirli bir parametreyi değil, bütün istekleri etkiler.
## C
EF Core sorguyu SQL'e çeviremiyor ve işi istemci tarafında yapıyor; tablo belleğe çekiliyor
🤖 çeviri sorunu olsaydı yavaşlık parametreden bağımsız ve sürekli olurdu.
## D
İndeks parçalanmış; parçalanma arttıkça okuma maliyeti yükseliyor ve sorgu yavaşlıyor
🤖 parçalanma yeniden başlatmayla yön değiştirmez ve tek parametreye bağlanmaz.
## Doğru
A
## Açıklama
Tarif edilen tablo parametre koklamasının klasik imzasıdır. MS SQL, parametreli bir sorgunun planını **ilk çalıştırmadaki parametre değerine** göre üretir ve önbelleğe alır. Sonraki çağrılar aynı planı yeniden kullanır. Veri dağılımı çarpıksa, yani bir üye iş yerinin birkaç yüz, başkasının milyonlarca hareketi varsa, tek plan ikisine birden uymaz. Küçük değere göre üretilmiş bir plan büyük değerde felaket olur. Belirtiler de bunu doğrular: kod değişmemiştir, davranış yeniden başlatma ya da istatistik güncellemesiyle yön değiştirir. B şıkkı doğru olsaydı bütün istekler yavaşlardı, tek parametre değil. C şıkkı gerçek bir EF Core riskidir ama sürekli ve parametreden bağımsız bir yavaşlık üretirdi. D şıkkındaki parçalanma yeniden başlatmayla taraf değiştirmez. Çareler arasında planı her çağrıda yeniden derletmek, bilinmeyen değere göre plan istemek, sorguyu iki ayrı yola bölmek ya da daha seçici bir indeks eklemek sayılabilir. Plan geçmişini tutan ve bir planı sabitlemeye izin veren araçlar da vardır; ayrıntısı sürümüne göre değişir, dokümandan teyit edilir. Oracle tarafında bağlama değişkeni gözetlemesi benzer bir yer tutar, ama motorların davranışı aynı değildir (`227`, `146`).
