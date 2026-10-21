---
slug: guncel-spring-2026
baslik: "Sözlü Prova · Spring 2026: Boot 4, Sanal Thread, Üretime Hazırlık ve Gözlemlenebilirlik"
kademe: İleri
sira: 279
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş açık uçlu mülakat sorusu var. Şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş; senin adına deneyim uydurulmaz, onu kendi geçmişinden çıkarman için soru sorulur. Bu beş sorunun ortak bir özelliği var: hepsi sürüme duyarlı. Adayların çoğu bu konuları duymuştur ama üretimde yaşamamıştır. Görüşmeci de bunu bilir; bu yüzden ölçtüğü şey ezber değil, ne işe yaradığını anlamış olmaktır. Bir kural baştan geçerlidir: emin olmadığın sürüm numarasını söyleme, ne işe yaradığını anlat.

### "Spring Boot 4 ve Framework 7 ile ne değişti? Yükseltirken pratikte ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Görüşmeci burada özellik listesi dinlemek istemez. Merak ettiği tek şey vardır: bu aday hangi kırılımın hangi geçişte olduğunu ayırabiliyor mu. Çünkü yükseltme eforu tam olarak bu ayrımdan hesaplanır. En sık hata, bir önceki neslin kırılımını yeni nesle yazmaktır (`74`). İkinci ölçtüğü şey dürüstlüktür. Sürüm numarasını yanlış söyleyen aday, doğru anlattığı her şeyi de şüpheli hâle getirir.

**Sözlü cevap iskeleti**

1. **Bunun bir nesil değişimi olduğunu söyle.** Spring Framework 7 ve Spring Boot 4 Kasım 2025'te yayımlandı ve yeni bir nesil başlattı. Yalnız özellik eklemedi, altındaki temelleri değiştirdi (`74`).
2. **Tabanı doğru ver.** Minimum Java sürümü 17'de korundu; en yeni uzun süreli destekli sürüm olan Java 25 önerilir. "Boot 4 Java 25 zorunlu kılar" yaygın bir yanılgıdır (`74`).
3. **İki geçişi ayır.** `javax` ad alanından `jakarta`ya göç Boot 2'den 3'e geçişin kırılımıydı. Boot 4 zaten `jakarta` çizgisindedir ve geriye dönüş yoktur (`74`).
4. **Boot 4'ün kendi getirdiklerini say.** Jakarta EE 11 tabanı, varsayılan Jackson 3, JSpecify null-güvenliği, çekirdeğe giren HTTP API sürümleme ve dayanıklılık anotasyonları (`74`).
5. **Yükseltmede gerçekte ne yapıldığını anlat.** En somut iş Jackson 2'den 3'e geçiştir; paket alanı `com.fasterxml.jackson` yerine `tools.jackson` olur. Bazı üçüncü taraf kütüphaneler hâlâ Jackson 2'ye bağlı olabilir, iki sürüm geçici olarak bir arada bulunur (`74`).
6. **Kaldırılmayanı da söyle.** Spring MVC kaldırılmadı; MVC ve WebFlux ikisi de desteklenir. "MVC bitti, WebFlux'a geçin" cümlesi bu soruda anında yakalanır (`74`, `88`).

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Boot 2-3 kirilimi ile Boot 3-4 nesil yenilenmesinin ayrimi ve yaygin yanilgilar">
  <defs>
    <marker id="gncspr-ok1" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki geçiş, iki ayrı fatura — karıştırma efor tahminini bozar</text>
  <rect x="14" y="38" width="200" height="164" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="114" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Boot 2 → Boot 3</text>
  <text x="114" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kırılım: ad alanı değişti</text>
  <text x="114" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">javax.* → jakarta.*</text>
  <text x="114" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her import satırı dokunulur</text>
  <text x="114" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">taban Java 17'ye çıktı</text>
  <text x="114" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iş: geniş ama mekanik</text>
  <text x="114" y="186" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">büyük ölçüde araçla taranır</text>
  <line x1="216" y1="120" x2="234" y2="120" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncspr-ok1)"/>
  <rect x="238" y="38" width="200" height="164" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="338" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Boot 3 → Boot 4</text>
  <text x="338" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">nesil yenilenmesi</text>
  <text x="338" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Jakarta EE 11 tabanı</text>
  <text x="338" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Jackson 3 → tools.jackson</text>
  <text x="338" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">JSpecify · API sürümleme</text>
  <text x="338" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iş: dar ama bağımlılığa bağlı</text>
  <text x="338" y="186" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iki Jackson bir arada olabilir</text>
  <line x1="440" y1="120" x2="458" y2="120" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncspr-ok1)"/>
  <rect x="462" y="38" width="184" height="164" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="554" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Söylenmeyecekler</text>
  <text x="554" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"Java 25 zorunlu"</text>
  <text x="554" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"javax göçü Boot 4'te"</text>
  <text x="554" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"Spring MVC kaldırıldı"</text>
  <text x="554" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"jakarta'dan geri dönülür"</text>
  <text x="554" y="170" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">dördü de tek cümlede</text>
  <text x="554" y="188" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">güvenilirlik düşürür</text>
  <rect x="14" y="214" width="632" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="234" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Sürüm cümlesini kurarken</text>
  <text x="330" y="254" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Taban: Java 17 (minimum) · Önerilen: Java 25 · Yayın: Kasım 2025 (Framework 7 ve Boot 4 birlikte)</text>
  <text x="330" y="274" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Emin değilsen numara verme; ne işe yaradığını anlat — yanlış numara doğru anlatımı da gölgeler.</text>
</svg>
```

**Elenme sebepleri**

- "Boot 4 Java 25 zorunlu kılar" demek. Taban 17'dir; yanlış sürüm iddiası bu soruda en hızlı güven kaybettiren cümledir.
- `javax` göçünü Boot 4'e yazmak. Kırılım bir önceki nesildeydi; karıştırmak yükseltme eforunu yanlış tahmin ettirir.
- "Spring MVC kaldırıldı, WebFlux'a geçilecek" demek. İki yığın da desteklenir; bu cümle okumamış aday izlenimi bırakır.
- Jackson geçişini yalnız "sürüm arttı" diye anlatmak. Görünür işaret paket alanının değişmesidir, sayının artması değil.
- Yükseltmeyi tek adımda biten bir iş gibi sunmak. Ekosistemdeki bağımlılıklar kademeli geçişi çoğu zaman zorunlu kılar.
- Yenilikleri sayıp hiçbirinin ne işe yaradığını söylememek. Liste ezberi kavrayışın yerini tutmaz.
- Emin olmadığı numarayı yine de söylemek. "Tam hatırlamıyorum" demek kaybettirmez; yanlış söylemek kaybettirir.

**Kendi hikâyen / dürüst köprü**

Buraya kendi vakan gelecek. Uydurma; şu sorular seni kendi geçmişine götürsün:

- Hiç büyük sürüm yükseltmesi yaptın mı? Hangi çerçeve, hangi sürümden hangisine?
- Yükseltmeyi kim istedi? Güvenlik yaması mı, bir özellik ihtiyacı mı, desteğin bitmesi mi?
- Hangi bağımlılık seni durdurdu? Yükseltilemeyen tek bir kütüphane bütün planı geciktirdi mi?
- Geçişi nasıl doğruladın? Testler yeterli miydi, yoksa tek bir ortamda mı denediniz?
- Geri dönüş yolu var mıydı? Bir şey patlasa nasıl eski hâle döneceğiniz baştan yazılmış mıydı?
- Ne kadar sürdü? Tahminle gerçek arasındaki fark, bu soruda anlatılabilecek en dürüst rakamdır.

Böyle bir deneyimin yoksa uydurma. `256`'daki dürüst köprüyü kur: "Üretimde büyük sürüm yükseltmesi yapmadım" de, sonra en yakın gerçek deneyime bağlan. Kendi projende bağımlılık yükseltip kırılan yeri onarmış olmak da gerçek bir gözlemdir. Yeter ki onu kurumsal bir göç projesi gibi anlatma.

### "Sanal thread'i Spring'de nasıl kullanırsınız? WebFlux hâlâ gerekli mi?"

**Soru aslında ne ölçüyor?**

Bu aslında iki sorudur ve ağırlık ikincisindedir. İlk yarısı ayar bilgisidir; hazırlanan her aday onu söyler. İkinci yarısı karardır ve az aday hazırlıklı gelir. Görüşmeci burada moda takibi ile muhakemeyi ayırır. "Sanal thread geldi, reactive öldü" diyen aday da, "WebFlux her zaman daha ölçeklenir" diyen aday da aynı yerden elenir: ikisi de takas cümlesi kurmamıştır (`56`, `88`).

**Sözlü cevap iskeleti**

1. **Kazancın nereden geldiğini söyle.** Sanal iş parçacığı (virtual thread) bloke eden girdi/çıktı beklemesini ucuzlatır. Bloke olunca altındaki taşıyıcı iş parçacığı (carrier thread) serbest kalır (`56`, `91`).
2. **Spring'de nereden açıldığını söyle.** Boot 3.2 ve sonrasında `spring.threads.virtual.enabled=true` ayarı bunu web istekleri, `@Async` metotları ve zamanlanmış görevler için açar. Elle `Executors.newVirtualThreadPerTaskExecutor()` de verilebilir (`91`).
3. **Sınırı hemen ekle.** İşlemci yoğun işlerde sanal thread avantaj sağlamaz; iş zaten işlemciyi meşgul eder. Orada sınırlı bir platform havuzu daha uygundur (`91`).
4. **Darboğazın yer değiştirdiğini söyle.** Thread ucuzlayınca sınır aşağı kayar: veri tabanı bağlantı havuzu, dış servis kotası, kilit çekişmesi. Eş zamanlılık bir yerde yine sınırlanmalıdır.
5. **WebFlux sorusunu ikiye böl.** Reactive'in tarihsel gerekçesi ölçekti; sanal thread'li MVC bu gerekçeyi büyük ölçüde karşıladı. Basit CRUD servisi için reactive'in bilişsel maliyeti artık gereksizdir (`88`).
6. **Nerede hâlâ gerekli olduğunu söyle.** Akış işleme, geri basınç (backpressure) ve olay birleştirme. Talep tabanlı akış sanal thread ile birebir karşılanmaz; ayrıca WebFlux'ta olay döngüsü asla bloke edilmez (`88`).

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sanal threadin Springde nerede kazandigi, nerede kazanmadigi ve WebFlux karari">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sanal thread beklemeyi ucuzlatır — hesabı hızlandırmaz</text>
  <rect x="14" y="38" width="200" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="114" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kazandığı yer</text>
  <text x="114" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bloke eden girdi/çıktı işi</text>
  <text x="114" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veri tabanı, dış servis çağrısı</text>
  <text x="114" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istek başına bir sanal thread</text>
  <text x="114" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bloke olunca taşıyıcı serbest</text>
  <text x="114" y="170" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">basit senkron kod korunur</text>
  <rect x="230" y="38" width="200" height="152" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kazanmadığı yer</text>
  <text x="330" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işlemci yoğun hesaplama</text>
  <text x="330" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">rapor üretimi, ağır dönüşüm</text>
  <text x="330" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">orada sınırlı platform havuzu</text>
  <text x="330" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">olay döngüsünde bloke çağrı</text>
  <text x="330" y="170" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">thread ucuzlar, iş ucuzlamaz</text>
  <rect x="446" y="38" width="200" height="152" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="546" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">WebFlux kararı</text>
  <text x="546" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sıradan CRUD → MVC yeter</text>
  <text x="546" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">akış / olay yayını → reactive</text>
  <text x="546" y="122" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geri basınç gerekiyor → reactive</text>
  <text x="546" y="142" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uçtan uca non-blocking şart</text>
  <text x="546" y="170" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ölçek için değil, akış için</text>
  <rect x="14" y="202" width="632" height="86" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="222" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Thread ucuzlayınca darboğaz kaybolmaz, yer değiştirir</text>
  <text x="330" y="242" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yeni sınır: veri tabanı bağlantı havuzu · dış servis kotası · kilit çekişmesi · bellek</text>
  <text x="330" y="262" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">on binlerce sanal thread, elli bağlantılık bir havuzu daha hızlı hâle getirmez.</text>
  <text x="330" y="280" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Bu yüzden eş zamanlılık bir yerde yine bilerek sınırlanır.</text>
</svg>
```

**Elenme sebepleri**

- "Sanal thread geldi, reactive öldü" demek. Aşırı genellemedir; akış ve geri basınç senaryolarında reactive üstünlüğünü korur.
- "Sanal thread her işi hızlandırır" demek. İşlemci yoğun işte kazanç yoktur; bu cümle mekanizmayı anlamadığını gösterir.
- Ayarı söyleyip mekanizmayı hiç anlatmamak. Tek satır yapılandırma bilgisi, taşıyıcı thread kavramının yerini tutmaz.
- Darboğazın yer değiştirdiğini atlamak. Bağlantı havuzu sabitken thread sayısını artırmak yalnız kuyruğu büyütür.
- WebFlux'ta olay döngüsünün bloke edilebileceğini sanmak. Zincirin ortasındaki bloke çağrı iş hacmini çökertir.
- Sanal thread'i WebFlux'a özel sanmak. Asıl ucuzlattığı şey bloke eden klasik istek başına thread modelidir.
- Kararı ölçütsüz vermek. "Modern olan" diye seçim yapan aday, mimari kararı değil eğilimi anlatıyor sayılır.

**Kendi hikâyen / dürüst köprü**

Bu soruda küçük bir gözlem bile yeterlidir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Çalıştığın uygulamada bir istek zamanının çoğunu nerede geçiriyor? Beklemede mi, hesapta mı?
- Hiç thread havuzu ayarı değiştirdin mi? Neden değiştirdin, sonucu nasıl ölçtün?
- Bağlantı havuzu doldu mu hiç? Belirti neydi: zaman aşımı mı, kuyrukta bekleme mi?
- Reactive kod yazdığın ya da okuduğun bir yer oldu mu? Hata ayıklamak sana nasıl geldi?
- Bir akış senaryon var mıydı? Sunucu-gönderimli olay, dosya aktarımı, kuyruk tüketimi?
- Sanal thread'i denediysen nerede denedin ve ne değişti? Değişmediyse onu söylemek de bir cevaptır.

Üretim deneyimin yoksa uydurma. Bu soruda uydurma hemen yakalanır, çünkü devamında "hangi metrik değişti" diye sorulur. `256`'daki köprüyü kur: kavramı doğru anlat, sonra "kendi projemde şunu denedim, gördüğüm şuydu" de. Denemediysen de dürüstçe söyle ve nasıl deneyeceğini anlat.

### "Bir Spring servisini üretime hazır saymak için neye bakarsınız?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi değil, olgunluk ölçer. Zayıf aday özellik sayar: "testler geçiyor, kod incelemesi yapıldı." Güçlü aday, servisi kendi başına ayakta duran bir şey olarak görür ve gece üçte alarm çaldığında ne olacağını anlatır. Görüşmecinin dinlediği şey sıralamadır: sağlık, sinyaller, sırlar, kapanış. Sıra kurabilen aday bu işi daha önce düşünmüş demektir (`89`, `193`).

**Sözlü cevap iskeleti**

1. **Sağlığı ikiye ayır.** Ayakta mı denetimi (liveness) başarısız olursa Kubernetes pod'u öldürüp yeniden başlatır. Hazır mı denetimi (readiness) başarısız olursa pod yaşar, yalnız yük dengeleyici havuzundan çıkar (`89`, `105`).
2. **Bu ayrımın bedelini söyle.** Dış bir bağımlılığın kesintisini ayakta mı denetimine bağlarsan, geçici bir kesinti kalıcı yeniden başlatma döngüsüne dönüşür (`89`).
3. **Yönetim uçlarının varsayılanını doğru ver.** Actuator eklenince uçlar tanımlanır ama HTTP üzerinden varsayılan olarak yalnız `/actuator/health` erişilebilir. Gerisi `management.endpoints.web.exposure.include` listesiyle açılır (`89`).
4. **Güvenlik cümlesini kur.** Her ucu açıp `/actuator`'ı kimlik doğrulamasız bırakmak bilgi ifşasıdır: yapılandırma uçları sır sızdırır, yığın dökümü belleği indirtir. Yalnız gereken uç açılır, tercihen ayrı porta ve iç ağa alınır (`89`).
5. **Üç sinyali bağla.** Metrikler Micrometer cephesinden akar; izler `micrometer-tracing` ve OpenTelemetry köprüsüyle üretilir, Sleuth öldü. Loglar yapılandırılmış olur ve istek kimliğini taşır (`89`, `103`, `104`).
6. **Sırları ve kapanışı ekle.** Parola ve anahtar depoya girmez, loga yazılmaz. Zarif kapanışta önce hazır mı denetimi başarısız olur, akan istekler bitirilir, sonra süreç iner (`105`).

**Elenme sebepleri**

- İki sağlık denetimini aynı şey sanmak. Rolleri ters söylemek bu soruda en klasik tuzaktır.
- Dış bağımlılık denetimini ayakta mı denetimine bağlamak. Bir sağlayıcının kesintisi tüm sürüyü yeniden başlatır.
- Yönetim uçlarını "salt okunur, zararsız" saymak. Yapılandırma ve bellek dökümü uçları doğrudan sır sızdırır.
- Tüm uçları açıp güvenliği sonraya bırakmak. Üretime hazırlık listesinde bu madde ertelenemez.
- Gözlemlenebilirliği "log yazıyoruz" diye özetlemek. Üç sinyalden yalnız birini sayan aday teşhis sırasını kuramaz.
- Kapanışı hiç anmamak. Zarif kapanış yoksa her dağıtımda bir miktar kullanıcı hata görür.
- Sırları ortam dosyasında bırakıp bunu sorun saymamak. Depoya giren tek anahtar tüm ortamı yakar.

**Kendi hikâyen / dürüst köprü**

Bu soruda hikâye küçük olabilir; yeter ki bir üretim anına dokunsun. Şu sorular onu çıkarır:

- Çalıştığın serviste sağlık uçları var mıydı? İkisi ayrılmış mıydı, yoksa tek bir uç mu vardı?
- Hiç gereksiz yeniden başlatma gördün mü? Sebebi neydi, nasıl anlaşıldı?
- Yönetim uçları kime açıktı? İç ağ mı, kimlik doğrulama mı, yoksa herkese açık mıydı?
- Bir sorun çıktığında ilk nereye baktın? Panoya mı, loga mı, doğrudan sunucuya mı?
- Loglarda istek kimliği var mıydı? Yoksa iki servisin logunu nasıl eşleştiriyordunuz?
- Sırlar nerede duruyordu? Ortam değişkeni, sır yöneticisi, yoksa yapılandırma dosyası mı?
- Dağıtım sırasında kullanıcı hata gördü mü? Gördüyse bunu kim fark etti?

Bu deneyim yoksa uydurma. `256`'daki köprü burada iyi işler: "Üretim nöbeti tutmadım" de, sonra kendi projende kurduğun sağlık ucunu ya da eklediğin metriği anlat. Küçük ama gerçek bir kurulum, uydurulmuş bir olay anlatısından her zaman güçlüdür.

### "Gözlemlenebilirlikte üç sütun nedir? Hangisi hangi soruyu cevaplar?"

**Soru aslında ne ölçüyor?**

Üç sütunun adını herkes sayar: log, metrik, iz. Ayrım tam bundan sonra başlar. Görüşmecinin beklediği şey, her birinin **hangi soruyu** cevapladığını söyleyebilmektir. Bunu söyleyen aday teşhis sırası da kurar. İkinci ayrım noktası maliyettir: üçünü sınırsız toplamak mümkün değildir ve nereye ne konacağı bir karardır (`193`).

**Sözlü cevap iskeleti**

1. **Üçünü soruyla eşle.** Metrik "ne kadar, kaç tane" sorusunu cevaplar. İz (trace) "nerede yavaşladı" sorusunu cevaplar. Log "tam olarak ne oldu" sorusunu cevaplar (`193`).
2. **Maliyet farkını söyle.** Metrik ucuzdur ve maliyeti neredeyse sabittir; alarm ve pano oradan kurulur. Log en zengin bilgiyi verir ama hacimle pahalanır (`193`).
3. **Asıl değerin korelasyonda olduğunu söyle.** Her isteğe başında benzersiz bir kimlik verilir. Bu kimlik yapılandırılmış logun içine alan olarak yazılır ve metriğe örnek nokta olarak iliştirilir (`193`, `103`).
4. **Teşhis akışını anlat.** Grafikte sıçramayı gör, o noktanın örnek izine geç, yavaş adımı bul, aynı kimlikli logları oku. Panoları saat damgasıyla gözle eşleştirmek yüksek trafikte işlemez (`193`).
5. **Etiket çeşitliliği kuralını ver.** Bir metriğin maliyeti ürettiği zaman serisi sayısıyla orantılıdır, o da etiket değerlerinin çarpımıdır. Kullanıcı kimliği gibi çok değerli bilgi metriğe değil ize ve loga aittir (`193`).
6. **Alarmı doğru yere kur.** Nedene değil belirtiye alarm verilir. İşlemci kullanımına alarm kurmak boşa çaldırır; hizmet hedefi ve hata bütçesinin tüketim hızı esas alınır (`193`, `104`).

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uc sutunun hangi soruyu cevapladigi, ortak kimlik ekseni ve etiket cesitliligi kurali">
  <defs>
    <marker id="gncspr-ok3" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her sütun ayrı bir soruyu cevaplar — teşhis sırası buradan çıkar</text>
  <rect x="14" y="38" width="200" height="140" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="114" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Metrik</text>
  <text x="114" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">"ne kadar, kaç tane?"</text>
  <text x="114" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata oranı, süre, istek hızı</text>
  <text x="114" y="124" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ucuz, maliyeti sabite yakın</text>
  <text x="114" y="150" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alarm ve pano buradan</text>
  <text x="114" y="168" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">"hangi kullanıcı" yazmaz</text>
  <line x1="216" y1="108" x2="234" y2="108" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncspr-ok3)"/>
  <rect x="238" y="38" width="200" height="140" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="338" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · İz (trace)</text>
  <text x="338" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">"nerede yavaşladı?"</text>
  <text x="338" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">isteğin servis servis yolu</text>
  <text x="338" y="124" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her durak bir izleme adımı</text>
  <text x="338" y="150" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">arama alanını daraltır</text>
  <text x="338" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">örnekleme ile maliyeti tutulur</text>
  <line x1="440" y1="108" x2="458" y2="108" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncspr-ok3)"/>
  <rect x="462" y="38" width="184" height="140" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="554" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Log</text>
  <text x="554" y="82" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">"tam olarak ne oldu?"</text>
  <text x="554" y="104" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">en zengin bağlam</text>
  <text x="554" y="124" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yapılandırılmış, alan-değer</text>
  <text x="554" y="150" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">hacim arttıkça pahalanır</text>
  <text x="554" y="168" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">saklama süresi bir karardır</text>
  <rect x="14" y="190" width="632" height="42" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="216" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Üçünü bağlayan tek şey: her isteğe verilen ortak kimlik (trace_id)</text>
  <rect x="14" y="242" width="632" height="46" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="262" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Etiket çeşitliliği: seri sayısı = etiket değerlerinin çarpımı</text>
  <text x="330" y="280" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kullanıcı ya da istek kimliği metriğe konmaz; o bağlam ize ve loga aittir.</text>
</svg>
```

**Elenme sebepleri**

- Üçünü sayıp hangisinin hangi soruyu cevapladığını söylememek. Bu soruda ayrım tam orada kurulur.
- Log ile metriği aynı şey saymak. Biri tek tek olayı, öteki toplamı anlatır; maliyetleri de aynı değildir.
- Korelasyondan hiç söz etmemek. Üç sinyali ayrı ayrı toplamak teşhis süresini kısaltmaz.
- Metriğe kullanıcı kimliği gibi çok değerli etiket koymak. Seri sayısı patlar ve izleme sistemi doyar.
- "Hepsini toplayalım, sonra bakarız" demek. Sınırsız toplama bütçeyi bitirir ve sinyali gürültüye gömer.
- İşlemci kullanımına alarm kurup bunu doğru saymak. Nedene alarm boşa çalar; belirtiye alarm kullanıcıyı temsil eder.
- Tek satıcının ürününde toplamayı korelasyon sanmak. Bağlayan şey ürün değil, paylaşılan kimliktir.

**Kendi hikâyen / dürüst köprü**

Bu soruda gerçek gözlem üretmek kolaydır. Şu sorular seni kendi deneyimine götürür:

- Bir sorunu teşhis ederken en son neye baktın? Sıra neydi, hangisi seni asıl yere götürdü?
- Loglarında ortak bir istek kimliği var mı? Yoksa iki servisin kaydını nasıl eşleştiriyorsun?
- Hiç pano kurdun mu? Hangi ölçümü koydun ve o ölçüm gerçekten bir karara mı hizmet etti?
- Alarm aldın mı hiç? Aldığın alarmların kaçı gerçek bir kullanıcı sorununa karşılık geliyordu?
- Log maliyeti konuşuldu mu? Saklama süresi ya da hacim yüzünden bir şey kısıldı mı?
- Metriklere ne tür etiketler konuyordu? Hiç çok değerli bir alan eklenmeye çalışıldı mı?

Bu deneyim yoksa uydurma. `256`'daki köprüyü kur. Kendi projende yapılandırılmış log kurup istek kimliğini uçtan uca taşımak gerçek bir gözlemdir ve dürüstçe öyle anlatılır. "Üretimde nöbet tutmadım ama şu akışı kendim kurdum" cümlesi bu soruda beklenenden iyi karşılanır.

### "Yükseltme kararını nasıl verirsiniz? Sürümden emin değilseniz ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu soru teknik bilgi değil, risk yönetimi ölçer. Görüşmeci "en yeniyi kullanırım" diyen adayla "dokunmayalım, çalışıyor" diyen adayı aynı kefeye koyar; ikisi de karar vermemiştir. Aranan şey gerekçedir. Sorunun ikinci yarısı ise doğrudan dürüstlük testidir ve 2026 sorularında sık sorulur. Sürüm numarasını uyduran aday, o cümleden sonra söylediği her şeyle birlikte sorgulanır.

**Sözlü cevap iskeleti**

1. **Kararı bir gerekçeye bağla.** Yükseltme kendi başına amaç değildir. Gerekçe dörttür: güvenlik yaması, destek süresinin bitmesi, ihtiyaç duyulan somut bir özellik, ekosistem uyumu.
2. **Destek süresini ilk ölçüt yap.** Desteği bitmiş bir sürümde kalmak güvenlik yaması alamamak demektir. Bu bir teknik borç değil, açık bir risktir.
3. **Ekosistemi kontrol et.** Kullandığın kütüphaneler yeni nesli desteklemiyorsa yükseltme yarıda kalır ve iki sürüm bir arada yaşar (`74`).
4. **Sıçramanın maliyetini söyle.** İki büyük sürümü birden atlamak riski çarpar. Ara sürümden geçmek ve küçük adımlarla ilerlemek hem ölçülebilir hem geri alınabilir kalır.
5. **Doğrulama ve geri dönüş planını ver.** Önce bağımlılıklar, sonra derleme, sonra testler, sonra tek bir ortamda gerçek trafik. Geri dönüş yolu baştan yazılır.
6. **Sürümden emin değilsen ne yapacağını söyle.** Numarayı söyleme. "Sürüm numarasını tam hatırlamıyorum ama ne işe yaradığını anlatayım" cümlesi seni elemez; yanlış numara eler. Görüşmeciler sürüm ezberi değil kavrayış arar (`256`).

**Elenme sebepleri**

- "Her zaman en son sürümü kullanırım" demek. Gerekçesiz yenilik, ekosistem uyumunu hesaba katmayan bir karardır.
- "Çalışan sisteme dokunulmaz" demek. Desteği biten sürüm güvenlik yaması almaz; hareketsizlik de bir risktir.
- Destek süresinden hiç söz etmemek. Yükseltme takvimi çoğu kurumda bu tek ölçütten çıkar.
- İki büyük sürümü birden atlamayı zararsız saymak. Kırılımlar üst üste binince kök neden bulunamaz.
- Geri dönüş planını anlatmamak. Yükseltmenin başarısız olabileceğini varsaymayan plan, plan değildir.
- Emin olmadığı sürüm numarasını söylemek. Tek yanlış sayı, doğru anlattığın her şeyi gölgeler.
- Yükseltmeyi yalnız geliştirici işi sanmak. Test, dağıtım ve izleme tarafı da bu kararın parçasıdır.

**Kendi hikâyen / dürüst köprü**

Bu soruda anlatılacak vaka çoğu zaman küçüktür ve yine de işe yarar:

- Hiç bir bağımlılığı yükselttin mi? Neden yükselttin, ne kırıldı?
- Yükseltmeyi erteleyen bir karar gördün mü? Gerekçe neydi ve sonradan ne oldu?
- Güvenlik uyarısı yüzünden acil bir yükseltme yapıldı mı? Süreç nasıl işledi?
- Ekipte sürüm kararını kim veriyordu? Ölçüt yazılı mıydı, yoksa alışkanlık mıydı?
- Geri dönmek zorunda kaldın mı? Nasıl geri döndün ve ne kadar sürdü?
- Bir mülakatta sürüm sorusuna cevap veremediğin oldu mu? O an ne yaptın?

Deneyimin yoksa uydurma. `256`'daki köprü bu soruda özellikle kolay kurulur, çünkü kişisel projede bile yükseltme kararı verilir. "Kurumsal bir göç yönetmedim" de, sonra kendi projende neden yükselttiğini ya da neden ertelediğini anlat. Ölçütü söyleyebiliyorsan cevap zaten kurulmuştur.

### Bu bölümden çıkan ders

Bu beş soru Spring bilgisini değil, sürüme duyarlı bir alanda dürüst ve ölçütlü konuşabilmeyi ölçer. Birincisinde kazanan cümle kırılımı doğru geçişe yazmaktır: ad alanı göçü bir önceki nesildeydi, yeni nesil Java 17 tabanını koruyup Jakarta EE 11, Jackson 3, JSpecify ve yerleşik API sürümlemesini getirdi. İkincisinde ayrım sanal thread'in neyi ucuzlattığını bilmekle kurulur — beklemeyi ucuzlatır, hesabı değil — ve WebFlux'un ölçek için değil akış için seçildiğini söylemekle tamamlanır. Üçüncüsünde sıralama konuşur: iki ayrı sağlık denetimi, varsayılan olarak yalnız sağlık ucunun açık olduğu bir yönetim yüzeyi, bağlanmış üç sinyal, korunan sırlar ve zarif kapanış. Dördüncüsünde asıl cevap her sütunun hangi soruyu cevapladığıdır ve üçünü ortak kimlik bağlar. Beşincisinde ise ölçüt söylenir: destek, güvenlik, ekosistem, sıçrama maliyeti. Beşinde de aynı disiplin geçerlidir: seçeneği söyle, ölçütü söyle, bedeli söyle. Ve hiçbirinde emin olmadığın sürüm numarasını verme; bilmediğini söylemek elemez, yanlış söylemek eler.

# gncspr-1 | Orta
## Soru
Bir aday, "Spring Boot 3'ten 4'e geçerken en büyük iş tüm `javax` importlarını `jakarta`ya çevirmektir" diyor. Bu cevabın kusuru nedir?
## A
O kırılım Boot 2'den 3'e geçişteydi; Boot 4 zaten `jakarta` çizgisindedir
🤖 Doğru: iki geçiş karıştırılınca yükseltme eforu baştan yanlış tahmin edilir.
## B
Cevap doğrudur ama eksiktir; aynı geçişte importların bir kısmının `javax`a geri çevrilmesi gerekir
🤖 geri dönüş yoktur; Boot 4 hiçbir koşulda `javax` ad alanına dönmez.
## C
Kusur yoktur; Jakarta EE 11 tabanına çıkmak zaten tüm import satırlarının yeniden yazılmasını gerektirir
🤖 Jakarta EE 11 bir taban yükseltmesidir; ad alanı adı bu geçişte değişmez.
## D
Kusur yalnızca sıradadır; önce Java 25'e çıkılmalı, import çevrimi ondan sonra yapılmalıdır
🤖 Java 25 önerilendir, zorunlu değil; taban Java 17'de korunmuştur.
## Doğru
A
## Açıklama
Bu cevap iki ayrı geçişi birbirine karıştırıyor. `javax` ad alanından `jakarta`ya göç, **Boot 2'den 3'e** geçişin kırıcı değişikliğiydi; o yükseltmede binlerce import satırı değişti. **Boot 4** ise zaten `jakarta` çizgisindedir ve geriye dönüş yoktur (`74`). Boot 4'ün kendi getirdiği şeyler farklıdır: Jakarta EE 11 tabanı, varsayılan Jackson 3, JSpecify null-güvenliği ve çekirdeğe giren HTTP API sürümleme. Bu ayrım akademik değildir; doğrudan efor tahminini belirler. Boot 3'ten 4'e geçişte en somut iş Jackson 2'den 3'e geçmektir, çünkü paket alanı `com.fasterxml.jackson` yerine `tools.jackson` olur ve bazı üçüncü taraf kütüphaneler hâlâ eski pakete bağlı kalabilir; bu durumda iki sürüm geçici olarak bir arada bulunur. D şıkkı ayrıca yaygın bir sürüm yanılgısını taşır: taban Java 17'dir, Java 25 önerilen sürümdür. Sözlü cevapta bu soru tek cümleyle açılır: hangi kırılım hangi geçişte olduysa oraya yazılır.

# gncspr-2 | İleri
## Soru
Bir ekip, ağır hesap yapan bir raporlama uç noktası için `spring.threads.virtual.enabled=true` ayarını açtı ama iş hacminde bir değişiklik görmedi. En yerinde açıklama hangisidir?
## A
Sanal thread'ler yalnız WebFlux yığınında etkinleşir; Spring MVC tarafında bu ayarın bir karşılığı yoktur
🤖 sanal thread asıl bloke eden istek başına thread modelini ucuzlatır; yığın kısıtı yoktur.
## B
Ayar yalnız zamanlanmış görevleri kapsar; web istekleri her koşulda platform thread'inde kalmaya devam eder
🤖 ayar web isteklerini, `@Async` metotlarını ve zamanlanmış görevleri birlikte kapsar.
## C
İş işlemci yoğun; sanal thread bekleme süresini ucuzlatır, hesap süresini kısaltmaz
🤖 Doğru: kazanç bloke eden girdi/çıktı işinde ortaya çıkar, hesap işinde değil.
## D
Sanal thread sayısı sınırsız olduğu için çöp toplayıcı doyar ve iş hacmi bu yüzden düşük kalır
🤖 anlatımda böyle bir mekanizma yok; sorun iş yükünün karakterindedir.
## Doğru
C
## Açıklama
Sanal thread'in kazancı tek bir yerden gelir: **bloke eden bekleme ucuzlar**. Bir sanal thread girdi/çıktı işleminde bloke olduğunda altındaki taşıyıcı platform thread'ini serbest bırakır, böylece az sayıda işletim sistemi thread'iyle çok sayıda bekleyen görev taşınabilir (`56`, `91`). Raporlama gibi **işlemci yoğun** bir işte bekleme zaten yoktur; iş işlemciyi meşgul eder ve thread'i ucuzlatmak toplam süreyi kısaltmaz. Bu tür yüklerde sınırlı bir platform havuzu daha uygundur (`91`). A şıkkı ilişkiyi ters kurar: sanal thread asıl olarak klasik istek başına thread modelini ucuzlatır, reactive'e özel değildir (`88`). B şıkkı ayarın kapsamını daraltır; Boot 3.2 ve sonrasında bu ayar web istekleri, `@Async` ve zamanlanmış görevler için birlikte devreye girer. Cevabın devamında söylenecek bir nokta daha vardır: thread ucuzlayınca darboğaz kaybolmaz, yer değiştirir. Yeni sınır çoğu zaman veri tabanı bağlantı havuzu, dış servis kotası ya da kilit çekişmesi olur.

# gncspr-3 | İleri
## Soru
Bir ekip yeni bir servis için WebFlux'a geçmeyi tartışıyor. Servisin çoğu sıradan CRUD, bir de sunucu-gönderimli olay akışı var. 2026 koşullarında en yerinde gerekçe hangisidir?
## A
Tümüyle WebFlux'a geçilmeli; sanal thread'lerden sonra MVC ile yüksek eş zamanlılık artık mümkün değildir
🤖 tersi doğru; sanal thread asıl MVC tarzı bloke kodu ucuzlattı.
## B
Ölçek gerekçesi zayıfladı; reactive akış ve geri basınç senaryolarında değerini korur
🤖 Doğru: CRUD için sanal thread'li MVC yeter, akış tarafında reactive üstün kalır.
## C
Karar teknik değil zevk meselesidir; iki yığın da aynı sorunları aynı maliyetle çözdüğü için fark etmez
🤖 maliyetler farklıdır: reactive'in bilişsel yükü ve uçtan uca non-blocking zorunluluğu vardır.
## D
WebFlux seçilmeli, çünkü zincirdeki bloke JDBC çağrıları Spring tarafından otomatik olarak ayrı havuza taşınır
🤖 böyle bir otomatik taşıma yoktur; bloke çağrı olay döngüsünü kilitler.
## Doğru
B
## Açıklama
Reactive'in tarihsel ana satış argümanı **az kaynakla yüksek eş zamanlılıktı**. Sanal thread'ler istek başına thread modelini neredeyse bedava hâle getirince bu argüman büyük ölçüde karşılandı; basit CRUD servisleri için reactive'in bilişsel maliyetine katlanmak çoğu ekip için gereksizleşti (`88`). Ancak reactive'in **akış işleme, geri basınç (backpressure) ve olay birleştirme** senaryolarındaki ifade gücü sanal thread'lerle birebir karşılanmaz. Talep tabanlı akışta tüketici kapasitesini bildirir, üretici yalnız o kadar yayar; bu, tampon şişmesini ve bellek taşmasını engelleyen bir sözleşmedir (`88`). Buradaki servis karışık bir durumdadır ve doğru cevap tek yığına saplanmak değil, ölçütü söylemektir. A şıkkı aşırı genellemedir ve mekanizmayı ters kurar. C şıkkı kararı zevke indirger; oysa maliyetler ölçülebilir biçimde farklıdır. D şıkkı ise tehlikeli bir yanılgıdır: zincirin ortasındaki bloke çağrı bir olay döngüsü thread'ini kilitler ve iş hacmini çökertir. 2026 dengesi tek cümleyle söylenir: ölçek için değil, akış modeli için reactive seç.

# gncspr-4 | İleri
## Soru
Bir servise Actuator bağımlılığı eklendi. `/actuator/health` cevap veriyor ama `/actuator/metrics` 404 dönüyor. En doğru açıklama hangisidir?
## A
`metrics` ucu yeni sürümlerde kaldırıldı; yerine yalnızca Prometheus biçimindeki çıktı bırakıldı
🤖 uç kaldırılmadı; sorun tanımlanma değil dışa açılma meselesidir.
## B
Actuator uçları yalnız uygulama ayrı bir yönetim portunda çalıştırıldığında cevap verebilir
🤖 ayrı port iyi bir uygulamadır ama bir ön koşul değildir.
## C
Micrometer kayıt (registry) bağımlılığı eklenmeden hiçbir metrik ucu tanımlanamaz
🤖 kayıt bağımlılığı `/actuator/prometheus` içindir; `metrics` ondan bağımsızdır.
## D
Uçlar tanımlandı ama HTTP'den varsayılan olarak yalnız `health` açıktır; gerisi listeye eklenir
🤖 Doğru: `management.endpoints.web.exposure.include` ile açıkça açılır.
## Doğru
D
## Açıklama
Burada karıştırılan iki kavram var: bir ucun **tanımlanması** ile **dışarı açılması** aynı şey değildir. `spring-boot-starter-actuator` eklenince `/actuator/health`, `/actuator/metrics`, `/actuator/info` gibi uçlar tanımlanır; ancak HTTP üzerinden varsayılan olarak yalnız `/actuator/health` erişilebilir. Gerisi `management.endpoints.web.exposure.include` listesiyle açıkça açılır (`89`). C şıkkı doğru bir bilgiyi yanlış yere taşır: `micrometer-registry-prometheus` bağımlılığı `/actuator/prometheus` ucu için gerekir, `/actuator/metrics` için değil. Cevabın devamında güvenlik cümlesi de kurulmalıdır, çünkü görüşmeci genellikle oraya bağlar. Listeyi yıldızla açıp `/actuator` altını kimlik doğrulamasız bırakmak ciddi bir bilgi ifşasıdır: ortam ve yapılandırma uçları veri tabanı parolası ile anahtar sızdırabilir, yığın dökümü bellekteki oturum verisini indirtir, thread dökümü iç çağrı yığınlarını açar (`89`). Doğru yaklaşım yalnız gereken uçları açmak, `/actuator` altını güvenlik katmanıyla korumak ve yönetim uçlarını ayrı bir porta ve iç ağa taşımaktır (`105`).

# gncspr-5 | Uzman
## Soru
Bir ödeme sağlayıcısı üç dakika kesintiye uğradı. O sırada servisin tüm pod'ları yeniden başlatma döngüsüne girdi ve kesinti daha da uzadı. En olası kök neden hangisidir?
## A
Hazır mı denetimi hiç tanımlanmamış; bu durumda Kubernetes pod'ları güvenlik gereği düzenli aralıklarla yeniden başlatır
🤖 hazır mı denetiminin yokluğu restart üretmez; pod trafiğe hazır sayılır.
## B
Dış bağımlılık denetimi ayakta mı yoklamasına bağlanmış; başarısızlığı pod'u öldürür
🤖 Doğru: bu denetim hazır mı yoklamasına aittir, trafik kesilir ama pod yaşar.
## C
Metrik toplama sıklığı yüksek olduğu için sağlık ucu zaman aşımına uğramış ve pod'lar bu yüzden düşmüştür
🤖 metrik sıklığı bu davranışın nedeni değildir; ayrım yoklama türündedir.
## D
Sağlık ucu kimlik doğrulama arkasına alınmış; Kubernetes doğrulama yapamadığı için trafiği kesmiştir
🤖 trafiği kesmek yeniden başlatma döngüsü üretmez; belirti uyuşmuyor.
## Doğru
B
## Açıklama
Bu, sağlık yoklamalarının en pahalı karıştırılma biçimidir. **Ayakta mı** yoklaması (liveness) "uygulama takıldı, öldü mü?" sorusunu sorar; başarısız olursa Kubernetes pod'u **öldürüp yeniden başlatır**. **Hazır mı** yoklaması (readiness) "bu pod şu an istek alabilir mi?" sorusunu sorar; başarısız olursa pod **yaşar**, yalnızca yük dengeleme havuzundan geçici olarak çıkarılır ve hazır olunca geri alınır (`89`). Dış bir bağımlılığın kontrolü ayakta mı yoklamasına bağlanırsa, o bağımlılığın geçici kesintisi tüm sürüyü yeniden başlatma döngüsüne sokar. Sonuç ağırdır: yeniden başlayan uygulamalar önbelleklerini ve bağlantılarını kaybeder, açılış maliyeti eklenir ve kesinti sağlayıcının kesintisinden uzun sürer (`105`). A şıkkı yanlış bir mekanizma uydurur; hazır mı yoklamasının olmaması yeniden başlatma üretmez. D şıkkı belirtiyle uyuşmaz, çünkü trafiğin kesilmesi ayrı bir sonuçtur. Doğru tasarım şudur: ayakta mı yoklaması yalnız sürecin kendi iç durumuna bakar, dış bağımlılıklar hazır mı yoklamasında değerlendirilir.

# gncspr-6 | Uzman
## Soru
Bir ekip, her isteği tek tek izleyebilmek için istek süresi metriğine `user_id` etiketi ekledi. Kısa sürede zaman serisi veri tabanı doydu. En yerinde düzeltme hangisidir?
## A
`user_id` metrikten çıkarılır; bu bağlam ize ve loga yazılır, bağ ortak kimlikle kurulur
🤖 Doğru: seri sayısı etiket değerlerinin çarpımıdır; çok değerli etiket metriğe konmaz.
## B
Histogram kova sayısı azaltılır; seri artışının kaynağı kovalar olduğu için sorun bu adımla tamamen çözülür
🤖 kovalar da seri üretir ama buradaki patlamanın kaynağı etikettir.
## C
Metrik toplama aralığı uzatılır; daha seyrek örnekleme seri sayısını doğrudan ve orantılı biçimde düşürür
🤖 aralık örnek sayısını değiştirir, ayrı seri sayısını değil.
## D
Loglama kapatılıp yalnız metrik saklanır; böylece toplam depolama maliyeti tek yerde toplanmış olur
🤖 log kapatmak "tam olarak ne oldu" sorusunu tümüyle cevapsız bırakır.
## Doğru
A
## Açıklama
Bir metriğin maliyeti, ürettiği **zaman serisi** sayısıyla orantılıdır ve bu sayı etiket değerlerinin çarpımıdır. Az değerli etiketlerle kurulan bir metrik rahatça taşınır; üstüne milyonlarca farklı değer alabilen bir kullanıcı kimliği eklendiğinde seri sayısı çarpılarak patlar ve bellekle disk çöker (`193`). Kural nettir: metriğe yalnız az değerli etiketler konur. Kullanıcı ya da istek kimliği gibi **yüksek çeşitlilikli** bağlam bilgisi ize ve yapılandırılmış loga aittir. Ekibin asıl istediği şey — tek bir isteği takip edebilmek — zaten doğru araçla karşılanır: her isteğe verilen ortak kimlik loga alan olarak yazılır ve metriğe örnek nokta olarak iliştirilir. Böylece grafikteki sıçramadan ize, izden aynı kimlikli loglara geçilir (`193`, `103`). C şıkkı iki farklı büyüklüğü karıştırır: toplama aralığı bir serideki nokta sayısını etkiler, ayrı seri sayısını değil. B şıkkı doğru bir yan bilgiyi tek çözüm gibi sunar. D şıkkı ise en zengin sinyali kapatarak teşhis yeteneğini yok eder (`104`).
