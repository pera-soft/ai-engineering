---
slug: sozlu-prova-liderlik-karar
baslik: "Sözlü Prova · Liderlik ve Karar: Ölçüt, Paydaş ve Ekip"
kademe: İleri
sira: 266
etiketler: 11
---

## Konu anlatımı

Bu altı soru kıdemli geliştirici mülakatlarında sorulur. Takım lideri pozisyonlarında da aynı başlıklar çıkar. Sen proje yöneticisi değilsin ve cevapların da yönetici ağzıyla kurulmaz. Görüşmecinin aradığı kişi süreç sahibi değil, süreçle çalışan ve onu iyileştiren mühendistir. Bu yüzden her iskelette ölçüt, takas ve kanıt vardır; slogan yoktur. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı. Senin adına deneyim uydurulmaz; hikâyeni kendi geçmişinden çıkarman için sorular sorulur.

### "Büyük ölçekli bir Java projesinde mimari kararları nasıl verirsiniz, hangi ölçütleri kullanırsınız?"

**Soru aslında ne ölçüyor?**

Ağırlık burada muhakemededir. Görüşmeci teknoloji ismi duymak istemez, karar disiplini duymak ister. "Kafka kullanırım" bir karar değil, bir tercih beyanıdır. Asıl bakılan şey şudur: bu aday seçtiği şeyin bedelini konuşabiliyor mu. İkinci bakılan şey kaydetmedir; kararın gerekçesi altı ay sonra kimsenin aklında kalmaz. Kıdemli aday burada bir de üçüncü şeyi gösterir: her karara aynı emeği harcamaz, kararları ayırır.

**Sözlü cevap iskeleti**

1. **Kararın hangi soruyu cevapladığını söyle.** Önce gereksinimi kalite niteliğine çevirirsin: gecikme, değiştirilebilirlik, işletilebilirlik, maliyet (`140`). Ham istek bir ölçüt değildir.
2. **Ölçütü sayıya bağla.** "Hızlı olsun" ile karar verilmez, "p95 gecikme 200 ms altında kalsın" ile verilir. Sayı yoksa iki seçenek arasında hakem de yoktur.
3. **En az iki seçenek koy ve takasını söyle.** Tek seçenekli karar, karar değil varsayımdır. Her seçenekte neyin kazanıldığını ve neyin feda edildiğini tek cümlede ver (`141`).
4. **Kararı ikiye ayır: geri dönülebilir mi, dönülemez mi.** Geri dönülebilir kararı hızlı verirsin ve deneyerek öğrenirsin. Geri dönülemez kararda yavaşlarsın; prototip, ölçüm ve ikinci göz istersin.
5. **Kararı ADR ile kaydet.** Bağlam, değerlendirilen seçenekler, verilen karar ve sonuçları yazılır (`140`). Karar sonradan değişirse eski kayıt silinmez; yenisi eskisini geçersiz kılar ve iz kalır.
6. **Sonra deneyime geç.** Hangi kararı verdin, neyi feda ettin, altı ay sonra ne öğrendin. Ölçüt cümlelerinden sonra gelen somut karar, cevabı ezberden ayırır.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Mimari karar akisi: gereksinim, kalite niteligi, secenekler, geri donulebilirlik ayrimi ve ADR kaydi">
  <defs>
    <marker id="sozlu5-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Mimari karar tercihle değil ölçütle başlar</text>
  <rect x="14" y="32" width="146" height="70" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="87" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">1 · Ham istek</text>
  <text x="87" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">"rapor hızlı açılsın"</text>
  <text x="87" y="90" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">henüz ölçüt değil</text>
  <line x1="162" y1="67" x2="170" y2="67" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ok)"/>
  <rect x="172" y="32" width="146" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="245" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 · Kalite niteliği</text>
  <text x="245" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gecikme · değiştirilebilirlik</text>
  <text x="245" y="90" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">p95 200 ms altında</text>
  <line x1="320" y1="67" x2="328" y2="67" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ok)"/>
  <rect x="330" y="32" width="146" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="403" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · En az iki seçenek</text>
  <text x="403" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her biri için takas yazılır</text>
  <text x="403" y="90" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">tek seçenek = varsayım</text>
  <line x1="478" y1="67" x2="486" y2="67" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ok)"/>
  <rect x="488" y="32" width="158" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="567" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">4 · Ölçütle karşılaştır</text>
  <text x="567" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hangi nitelik feda ediliyor</text>
  <text x="567" y="90" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bedeli kim ödüyor</text>
  <line x1="330" y1="104" x2="330" y2="116" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ok)"/>
  <rect x="176" y="120" width="308" height="38" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="144" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">5 · Bu karar geri dönülebilir mi?</text>
  <line x1="230" y1="160" x2="180" y2="176" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ok)"/>
  <line x1="430" y1="160" x2="480" y2="176" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ok)"/>
  <rect x="14" y="180" width="310" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="200" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Evet · hızlı ver</text>
  <text x="169" y="219" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">küçük deneme yap, ölç, gerekirse geri al</text>
  <rect x="336" y="180" width="310" height="56" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="491" y="200" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Hayır · yavaşla</text>
  <text x="491" y="219" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">prototip, ölçüm ve ikinci göz iste</text>
  <rect x="14" y="244" width="632" height="48" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="264" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">6 · ADR: bağlam · seçenekler · karar · sonuçlar</text>
  <text x="330" y="282" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Karar değişince eski kayıt silinmez; yenisi eskisini geçersiz kılar, gerekçe izi korunur.</text>
</svg>
```

**Elenme sebepleri**

- Teknoloji ismi saymak: "Kafka, Redis, K8s kullanırım" cümlesi ölçüt içermez ve karar anlatmaz.
- Ölçütü sayıya bağlamamak: "Performanslı olsun" ifadesiyle iki seçenek arasında seçim yapılamaz.
- Tek seçenek anlatmak: alternatifi olmayan karar, sorgulanmamış bir alışkanlıktır.
- Takas cümlesini hiç kurmamak: her mimari kararın bir bedeli vardır; bedeli olmayan karar anlatılmamıştır.
- Kararı kaydetmekten söz etmemek: gerekçesi yazılmayan karar birkaç ay sonra efsaneye döner.
- Her kararı aynı ağırlıkta ele almak: geri dönülebilir kararı aylarca tartışmak da bir hatadır.
- "Ben karar vermem, mimar verir" demek: kıdemli adaydan beklenen şey seçenek ve maliyet üretmektir.

**Kendi hikâyen**

Buraya senin verdiğin gerçek bir karar gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Hangi projede iki yol arasında seçim yapmak zorunda kaldın — kuyruk mu doğrudan çağrı mı, tek uygulama mı ayrı servis mi, kütüphane mi kendi kodun mu?
- O kararı tetikleyen sıkıntı neydi — yavaşlık mı, sık kırılma mı, ekip bağımlılığı mı?
- Hangi ölçüte baktın — yanıt süresi, dağıtım sıklığı, hata oranı, geliştirme süresi?
- Reddettiğin seçenek neydi ve neden reddettin — bu cümle cevabının en güçlü yeridir.
- Neyi feda ettin — basitlik mi, tutarlılık mı, maliyet mi?
- Karar yazıya döküldü mü — ADR yoksa bile bir e-posta, bir sayfa not veya PR açıklaması var mıydı?
- Sonradan ne öğrendin — kararın hangi kısmı tuttu, hangi kısmını bugün başka türlü yapardın?

Böyle bir kararın yoksa uydurma. Kod seviyesindeki tasarım kararları da gerçek örnektir; arayüz ayırma veya bağımlılığı tersine çevirme bir karardır (`131`). `256`'daki dürüst köprüyü kur: sınırı söyle, komşu deneyimini somutla, farkı adlandır.

### "Sprint planlama, backlog önceliklendirme ve teslimat takibini nasıl yürütürsünüz?"

**Soru aslında ne ölçüyor?**

Bu soru süreç bilgisi gibi görünür, aslında rol netliği ölçer. Görüşmeci senin Scrum eğitmeni olmanı beklemez. Tören adı saymak burada en zayıf cevaptır. Aranan şey şudur: süreç tıkandığında ne yapıyorsun. İkinci aranan şey tahmin dürüstlüğüdür; kötü haberi ne zaman verdiğin bu soruda ortaya çıkar. Ağırlık deneyimdedir, tanım kısmı yalnız otuz saniye alır.

**Sözlü cevap iskeleti**

1. **Rolünü doğru yerden kur.** "Süreci ben sahiplenmiyorum, içinde çalışıyorum ve tıkandığı yeri düzeltiyorum." Bu cümle seni hem samimi hem sorumlu gösterir.
2. **Planlamanın asıl işinin bölmek olduğunu söyle.** Bir kalem sprinte sığmıyorsa dikey dilimlere bölünür: uçtan uca çalışan küçük parça. Katmana göre bölmek yanlıştır, çünkü hiçbir parça tek başına değer üretmez.
3. **Tahmin ile taahhüdü ayır.** Tahmin belirsizlik taşır, taahhüt söz demektir. Aralık ver ve varsayımı söyle: "iki ile beş gün, şu servis hazırsa".
4. **Önceliklendirmede iki ekseni söyle.** İş değeri ve risk birlikte bakılır; en belirsiz parça erken çekilir. Teknik borç backlog'da görünür bir kalem olarak durur, gizli mesai olarak değil (`132`, `195`).
5. **Tanımlanmış bitişi (definition of done) somutla.** Test yazıldı, inceleme yapıldı, dağıtıldı, ölçümü var. Ölçütü olmayan "bitti" kelimesi sonraki sprintte geri gelir (`261`).
6. **Takibi görünürlük olarak anlat.** Günlük konuşmanın işi durum raporu değil, engel bildirmektir. Sprint ortasında kapsam değişirse reddetmezsin; takası gösterirsin. "Bu girerse şu çıkar" cümlesi profesyonel cevaptır.

**Elenme sebepleri**

- Tören adı saymak: planlama, günlük, gözden geçirme, retro sıralaması hiçbir şey ölçmez.
- "Scrum master ne derse onu yaparım" demek: kıdemli adaydan pasiflik beklenmez.
- Tahmini taahhüt gibi vermek: tek sayı söyleyip varsayım koymamak, sonraki tartışmayı garanti eder.
- Hızı (velocity) başarı ölçüsü sanmak: hız kapasite planlama aracıdır, ekip kıyaslama ölçüsü değildir.
- Teknik borcu süreç dışında tutmak: görünmeyen iş planlanamaz ve her sprint tahmini bozar.
- Sprint ortası eklemeye koşulsuz "olur" demek: takas gösterilmeden kabul edilen iş kaliteden kesilir.
- Gecikmeyi sprint sonunda haber vermek: erken uyarı yoksa süreç bilgisi bir işe yaramaz.

**Kendi hikâyen**

Bu soruda hikâye küçük ve somut olmalı. Şu sorular malzemeyi çıkarır:

- Hangi ekipte hangi düzenle çalıştın — iki haftalık sprint mi, sürekli akış mı, ikisinin karışımı mı?
- Sprintte hangi kalem sığmadı — onu nasıl böldün, bölünce ne değişti?
- Tahminin en çok saptığı iş hangisiydi — sapmanın sebebi belirsizlik miydi, bağımlılık mıydı?
- Kötü haberi ne zaman verdin — sapmayı gördüğün gün mü, sonunda mı?
- Teknik borcu backlog'a taşıyabildin mi — nasıl anlattın, kabul edildi mi?
- Sprint ortasında acil iş geldiğinde ne çıkardın — karşılığında neyi ertelediniz?
- Süreçte bozuk olan neyi düzelttin — kısaltılan bir toplantı, netleşen bir bitiş tanımı, azalan bir devir işi?

Böyle bir deneyimin sınırlıysa uydurma. Küçük bir ekipte gayriresmî çalıştıysan onu dürüstçe söyle. `256`'daki köprü kalıbı burada da geçerlidir: neyi yaşamadığını söyle, neyi yaşadığını somutla.

### "Kapsam, zaman ve bütçeyi nasıl planlar ve kontrol edersiniz?"

**Soru aslında ne ölçüyor?**

Bu soru geliştiriciye sorulduğunda proje planı beklenmez. Ölçülen şey gerçekçiliktir. Görüşmeci iki cümle arar: neyin ayarlanabilir olduğunu biliyor musun ve kötü haberi ne zaman veriyorsun. Üçgeni ezberden saymak yeterli değildir; ağırlık, sıkışma anında ne yaptığındadır. Kalite kelimesini nereye koyduğun da bu soruda duyulur.

**Sözlü cevap iskeleti**

1. **Üçgeni tek cümlede kur.** Kapsam, zaman ve kaynak birlikte hareket eder; biri sabitlenince başkası esner. Kalite bu üçlünün içinde bir pazarlık kalemi değildir.
2. **Hangi değişkenin sabit olduğunu sor.** Tarih sabitse ayarlanacak değişken kapsamdır. Bu soruyu soran aday, plan konuşmasının içine girmiş olur.
3. **Belirsizliği tahminle değil erken teslimle azalt.** En riskli parçayı önce yaparsın, çalışan ince bir dilim çıkarırsın. Tahminin doğruluğu ancak gerçek kodla artar.
4. **Tahmini aralık ve varsayımla ver.** Tek sayı güven verir ama yanlıştır. Varsayımı yazmak, sonradan yapılacak tartışmanın yarısını baştan bitirir.
5. **Kapsam sızmasını adlandırabil.** Küçük eklemeler tek tek makul görünür, toplamı tarihi kaydırır. Her ekleme bir çıkarma sorusu doğurmalıdır.
6. **Geliştiricinin asıl katkısını söyle: erken uyarı.** Sapmayı gördüğün gün söylersin. Geç haber, gecikmenin kendisinden pahalıdır. Geç kalan işe insan eklemek de çoğu zaman işi daha da geciktirir.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kapsam zaman kaynak ucgeni, kalitenin pazarlik disi olmasi ve erken uyari kurali">
  <defs>
    <marker id="sozlu5-ucgen" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç değişken birlikte hareket eder, kalite pazarlık kalemi değildir</text>
  <polygon points="176,52 74,196 278,196" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="176" y="45" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kapsam</text>
  <text x="66" y="212" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Zaman</text>
  <text x="288" y="212" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Kaynak</text>
  <text x="176" y="130" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kalite</text>
  <text x="176" y="148" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">dışarıdan kısılamaz</text>
  <text x="176" y="164" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kısılırsa borç olur</text>
  <rect x="322" y="36" width="324" height="176" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="484" y="56" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Biri sabitlenince ne esner?</text>
  <rect x="334" y="66" width="300" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="484" y="85" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Tarih sabit → ayarlanacak değişken kapsamdır</text>
  <rect x="334" y="100" width="300" height="30" rx="6" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="484" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Kapsam sabit → tarih ya da kaynak esner</text>
  <rect x="334" y="134" width="300" height="30" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.2"/>
  <text x="484" y="153" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">İkisi de sabit → kaliteden kesilir, sonra faizle döner</text>
  <rect x="334" y="168" width="300" height="32" rx="6" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <text x="484" y="188" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Geciken işe insan eklemek kısa vadede yavaşlatır</text>
  <line x1="290" y1="124" x2="318" y2="124" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ucgen)"/>
  <rect x="14" y="226" width="632" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="247" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Geliştiricinin iki katkısı: gerçekçi tahmin ve erken uyarı</text>
  <text x="330" y="266" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Belirsizliği tahmin azaltmaz; en riskli parçayı önce çalıştırmak azaltır.</text>
  <text x="330" y="283" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Sapmayı gördüğün gün söyle: geç haber, gecikmenin kendisinden pahalıdır.</text>
</svg>
```

**Elenme sebepleri**

- Üçgeni ezberden sayıp örnek vermemek: tanım herkeste var, davranış az adayda.
- "Kaliteden kısarız" demek: bu cümle kısa vadeli görünür ve teknik borcu göz ardı eder.
- Tek sayılık tahmin vermek: varsayımsız tahmin, sonraki toplantının kavga konusudur.
- Riskli parçayı sona bırakmak: kolay işlerle başlamak ilerleme yanılsaması üretir.
- Kapsam eklemelerine karşılıksız evet demek: çıkarma sorusu sorulmadan kabul edilen iş tarihi kaydırır.
- Sapmayı geç bildirmek: görüşmeci burada "ne zaman söyledin" sorusunu mutlaka sorar.
- Sorunu tamamen yöneticiye havale etmek: kıdemli adaydan beklenen şey seçenek üretmektir.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası bir sıkışma anıdır. Şunları kendine sor:

- Hangi işte tarih ile kapsam çakıştı — sürüm mü, entegrasyon mu, mevzuat tarihi mi?
- Sıkışmayı ne zaman fark ettin — ne kadar erken söyledin?
- Neyi kestin — hangi özellik ertelendi, hangi kısım ilk sürüme girmedi?
- Kalite kalemlerinden bir şey feda edildi mi — sonrasında bedelini nasıl ödediniz?
- Tahminin en çok saptığı işte ne unutulmuştu — test, veri göçü, dağıtım, gözden geçirme?
- Erken teslim işe yaradı mı — çalışan ince bir dilim çıkarınca beklenti değişti mi?
- Sonraki tahminlerini nasıl değiştirdin — bir kontrol listesi veya varsayım alışkanlığı oluştu mu?

Bu ölçekte bir deneyimin yoksa uydurma. Küçük bir işte verilen söz de aynı iskeleti taşır. `256`'daki dürüst köprü burada da işini görür.

### "Paydaş beklentileri çakıştığında gereksinimleri nasıl netleştirirsiniz?"

**Soru aslında ne ölçüyor?**

Bu soru çatışma anındaki davranışını ölçer, iletişim teorisini değil. "Yöneticiye iletirim" cevabı burada en hızlı eleyen cümledir. Görüşmecinin aradığı beceri şudur: belirsiz isteği somuta çevirebiliyor musun. İkinci aradığı şey sınır bilincidir; hangi kararın senin, hangisinin başkasının olduğunu biliyor musun. Ağırlık muhakemede, ikinci yarısı deneyimdedir.

**Sözlü cevap iskeleti**

1. **Çakışmanın türünü ayır.** Gerçek bir öncelik çatışması mı, yoksa aynı kelimeye iki anlam mı yüklenmiş. Çakışmaların çoğu tanım farkından doğar.
2. **Somut örnek üzerinden netleştir.** Soyut tartışma bitmez, örnek bitirir: "şu ekranda kullanıcı şunu yaptığında ne görmeli". Tek bir senaryo, yarım saatlik tartışmadan hızlıdır.
3. **Kelimeyi sayıya çevir.** "Gerçek zamanlı" yerine kabul edilebilir gecikmeyi sor. Ölçülemeyen gereksinim, teslimden sonra yeniden açılır.
4. **Kabul ölçütü yaz ve iki tarafa da göster.** Ölçüt, sonraki ay yapılacak "böyle istememiştik" tartışmasını baştan kapatır.
5. **Kararın sahibini belirle.** Geliştirici seçenekleri, riski ve maliyeti verir; önceliği ürün tarafı seçer. Sahipsiz karar askıda kalır ve gecikmeyi sana yazar.
6. **"Hayır" yerine maliyeti göster.** "Yapılamaz" tartışma açar, "yapılabilir, karşılığı şudur" tartışmayı kapatır. Sonunda mutabakatı yazıya dök; sözlü anlaşma üç hafta sonra iki farklı hatırlanır.

**Elenme sebepleri**

- "Yöneticime iletirim" demek: seçenek ve maliyet üretmeden yapılan devir, sorumluluktan kaçmak gibi duyulur.
- Doğrudan "olmaz" demek: gerekçesiz ret, kendini teknik bahane gibi duyurur.
- İki isteği de aynı anda karşılamaya söz vermek: ölçüt konmadan verilen söz kapsamı sessizce büyütür.
- Tanım farkını hiç düşünmemek: aynı kelimeye iki anlam yüklendiğini görmeyen aday yanlış şeyi kodlar.
- Kabul ölçütü yazmamak: ölçütsüz gereksinim teslimden sonra geri döner.
- Mutabakatı yazıya dökmemek: sözlü anlaşma, hatırlayanların sayısı kadar sürümlenir.
- Paydaşı rakip gibi anlatmak: "Onlar hep saçma ister" cümlesi tek başına eleyicidir.

**Kendi hikâyen**

Buraya senin yaşadığın bir netleştirme gelecek. Sorular şunlar:

- Hangi istekte iki taraf farklı şey anladı — rapor, bildirim, yetki, iade kuralı?
- Hangi kelime belirsizdi — "Gerçek zamanlı", "otomatik", "hızlı", "esnek" gibi bir kelime var mıydı?
- Nasıl netleştirdin — ekran taslağı mı çizdin, örnek veriyle mi gösterdin, soru listesi mi yazdın?
- Ölçüye çevirebildin mi — kaç saniye, kaç kayıt, hangi durumda ne olacak?
- Kararı kim verdi — sen seçenek sundun mu, sahibi belliyken mi ilerlediniz?
- Maliyeti nasıl gösterdin — "Bu girerse şu çıkar" cümlesini kurabildin mi?
- Yazıya döküldü mü — ticket açıklaması, kabul ölçütü, kısa bir not?

Böyle bir vakan yoksa uydurma. Kod incelemesinde çıkan bir gereksinim tartışması da aynı iskeleti taşır. Küçük ama gerçek örnek, büyük ama uydurma örnekten her zaman güçlüdür (`256`).

### "Ekip içi çatışma veya düşük performans durumunda nasıl yaklaşırsınız?"

**Soru aslında ne ölçüyor?**

Görüşmeci kimin haklı olduğunu öğrenmek istemez. Ölçtüğü şey senin tonundur. Eski ekip arkadaşını suçlayarak anlatan aday, gelecekteki ekibini de öyle anlatır. İkinci ölçtüğü şey teşhis alışkanlığıdır: belirtiden nedene geçebiliyor musun. Bu soru geliştiriciye de sorulur, çünkü kıdemli geliştirici zaten çoğu gün başka birinin işini gözden geçirir.

**Sözlü cevap iskeleti**

1. **Önce veri topla.** Nedir, ne zamandan beri sürüyor, hangi somut olaylar var. Genelleme yerine olay anlatılır: tarih, iş, sonuç.
2. **Konuşmayı özel yap.** Herkesin önünde verilen geri bildirim savunma üretir ve öğrenmeyi durdurur (`195`).
3. **Kişiyi değil davranışı ve etkisini anlat.** "Dikkatsizsin" yargıdır. "Son üç istekte test yoktu, hat iki kez kırıldı" gözlemdir.
4. **Üç nedeni ayır: engel mi, beceri mi, isteksizlik mi.** Bu ayrım cevabın omurgasıdır. Engel kaldırılır, beceri öğretilir, isteksizlik açıkça konuşulur. Yanlış teşhis, doğru niyetle bile zarar verir.
5. **Somut ve ölçülebilir plan kur.** Ne yapılacak, ne zamana kadar, nasıl doğrulanacak. Takvimi olmayan plan bir dilek olarak kalır.
6. **Teknik çatışmayı ayrı ele al.** Kişisel değilse kararı ölçüte bağlarsın: küçük bir deney, bir ölçüm, gerekiyorsa kayıt (`140`). Sonucu kabul etmek de ekibe verilen bir örnektir.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dusuk performans teshisi: veri toplama, ozel konusma, engel beceri isteksizlik ayrimi ve olculebilir plan">
  <defs>
    <marker id="sozlu5-ekip" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı belirti, üç ayrı neden: müdahaleyi teşhis belirler</text>
  <rect x="14" y="32" width="196" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="112" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Belirti</text>
  <text x="112" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işler gecikiyor · kalite düşüyor</text>
  <line x1="212" y1="58" x2="222" y2="58" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ekip)"/>
  <rect x="226" y="32" width="196" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="324" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Veri</text>
  <text x="324" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ne zamandan beri · hangi olaylar</text>
  <line x1="424" y1="58" x2="434" y2="58" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ekip)"/>
  <rect x="438" y="32" width="208" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="542" y="52" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Özel konuşma</text>
  <text x="542" y="71" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kişi değil davranış ve etkisi</text>
  <line x1="330" y1="86" x2="330" y2="100" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu5-ekip)"/>
  <rect x="14" y="104" width="204" height="134" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="124" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Engel</text>
  <text x="116" y="145" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bekleyen ortam · eksik yetki</text>
  <text x="116" y="163" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bulanık gereksinim · sık bölünme</text>
  <text x="116" y="188" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">müdahale: engeli kaldır</text>
  <text x="116" y="208" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">çoğu vakanın gerçek nedeni</text>
  <text x="116" y="226" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">burada eğitim vermek işe yaramaz</text>
  <rect x="228" y="104" width="204" height="134" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="124" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Beceri</text>
  <text x="330" y="145" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alan bilgisi yeni · araç yabancı</text>
  <text x="330" y="163" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kod tabanı tanıdık değil</text>
  <text x="330" y="188" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">müdahale: öğret ve eşleştir</text>
  <text x="330" y="208" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">küçük iş ver, geri bildirimi sıklaştır</text>
  <text x="330" y="226" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">burada baskı hatayı çoğaltır</text>
  <rect x="442" y="104" width="204" height="134" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="544" y="124" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">İsteksizlik</text>
  <text x="544" y="145" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">beklenti belirsiz · işe bağ kopuk</text>
  <text x="544" y="163" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karşılıksız emek hissi</text>
  <text x="544" y="188" text-anchor="middle" font-size="10" font-weight="700" fill="#B07D20" font-family="system-ui">müdahale: açıkça konuş</text>
  <text x="544" y="208" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">beklentiyi ve sonucu netleştir</text>
  <text x="544" y="226" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">en son varsayılacak neden budur</text>
  <rect x="14" y="248" width="632" height="50" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="269" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Plan: ne · ne zamana kadar · nasıl doğrulanacak</text>
  <text x="330" y="288" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Takvimi ve doğrulaması olmayan iyileştirme planı bir dilek olarak kalır.</text>
</svg>
```

**Elenme sebepleri**

- Eski arkadaşını suçlayarak anlatmak: görüşmeci burada senin gelecekteki dilini duyar.
- Doğrudan isteksizlik varsaymak: en son düşünülmesi gereken neden, ilk söylenirse teşhis çöker.
- Geri bildirimi herkesin önünde vermek: savunma refleksi açılır ve konu içerikten uzaklaşır.
- Yalnız duygu anlatmak: "Sıkıntı vardı, konuştuk, düzeldi" cümlesi hiçbir yöntem göstermez.
- Somut olay yerine sıfat kullanmak: "Tembeldi", "ilgisizdi" gibi kelimeler gözlem değil yargıdır.
- Ölçülebilir plan kurmamak: takvimsiz iyileştirme sözü altı ay sonra aynı konuşmayı doğurur.
- Teknik çatışmayı kişisel çatışma gibi anlatmak: karar ölçütle çözülebilecekken kavgaya dönüşmüş demektir.

**Kendi hikâyen**

Burada hikâyen küçük olabilir; önemli olan yöntemin görünmesi. Şu soruları cevapla:

- Hangi durumda bir gecikme ya da tekrarlayan hata dikkatini çekti — ne kadar sürdü?
- Neyi veri olarak topladın — gözden geçirme notları, kırılan hat, kaçan tarih, tekrar açılan iş?
- Konuşmayı nasıl kurdun — ne zaman, nerede, hangi cümleyle başladın?
- Neden çıktı ortaya — engel miydi, beceri miydi, beklenti belirsizliği miydi?
- Müdahale ne oldu — eşli çalışma, iş bölümü değişikliği, netleştirilen bitiş tanımı, kaldırılan bir bağımlılık?
- Nasıl doğruladın — iki hafta sonra ne değişmişti, ölçülebilir bir şey var mıydı?
- Teknik bir anlaşmazlığı ölçütle çözdüğün oldu mu — karşı görüş haklı çıktığında ne yaptın?

Yönetsel bir vakan yoksa uydurma. Kod incelemesinde ısrarlı bir anlaşmazlık da bu iskeletle anlatılır. `256`'daki köprüyü kur: sınırı söyle, en yakın gerçek örneği ver.

### "Günlük geliştirme sürecinizde hangi dilleri kullanıyorsunuz, neden?"

**Soru aslında ne ölçüyor?**

Bu soru ısınma sorusu gibi durur, oysa tuzaklıdır. "Java severim" cevabı bir sevgi beyanıdır ve hiçbir ölçüt taşımaz. Ölçülen iki şey vardır. Birincisi seçim yapabilme: aracı işe göre seçiyor musun. İkincisi dürüstlük: kullandığın dilin zayıf tarafını söyleyebiliyor musun. Kıdemli adayda üçüncü bir sinyal daha aranır — yeni bir dili gerektiğinde öğrenebiliyor musun.

**Sözlü cevap iskeleti**

1. **Ne kullandığını söyle, hemen ardından nedene geç.** Liste tek başına cevap değildir. İki cümlede geçiş yap, yoksa envanter sayıyormuş gibi duyulur.
2. **Gerekçeyi üç ölçüte bağla.** Ekosistem olgunluğu, ekibin bildiği, işletme maliyeti. Bu üçlü, tercihi zevkten çıkarır ve mühendislik kararına çevirir.
3. **Java'nın güçlü tarafını somutla.** Olgun kütüphane ekosistemi, güçlü araç desteği, uzun ömürlü kodda geriye uyum, geniş işe alım havuzu. Genel övgü değil, somut başlık ver.
4. **Zayıf tarafını da söyle.** Aynı işi daha çok satırla anlatması, bellek ayak izi, kısa ömürlü işlerde açılış süresi. Bunu söyleyen aday hemen güven kazanır.
5. **Yan dilleri işe göre anlat.** Otomasyon için kabuk betiği, veri işi için SQL, uygun yerde başka bir JVM dili. SQL'i de bir dil saymak önemlidir; performans sorunlarının çoğu orada çözülür (`29`).
6. **Öğrenme eğrisiyle bitir.** Son öğrendiğin şeyi ve nasıl öğrendiğini söyle. Bağlılık değil uyum sinyali ver: "araç değişebilir, ölçütüm değişmez".

**Elenme sebepleri**

- "Java severim, en iyi dildir." Ölçüt yok, karşılaştırma yok; tek cümlede kapalı düşünce sinyali verir.
- Zayıf tarafı hiç söylememek: tek yönlü övgü, aracı sorgulamayan adayı gösterir.
- "Dil fark etmez" demek: ekosistem ve işletme maliyeti farkını yok saymak yüzeysel durur.
- "Ne isterlerse yazarım" demek: uyumlu görünür ama tercih ve gerekçe üretmeyen adayı işaret eder.
- Hiç kullanmadığı dilleri listeye eklemek: görüşmeci bir soruyla altını boşaltır ve güven biter.
- SQL'i hiç saymamak: günlük işin büyük kısmı veriyle geçerken bunu atlamak gerçekçi durmaz.
- Dil savaşına girmek: başka bir dili küçümseyen cevap teknik değil kültürel bir risk gibi okunur.

**Kendi hikâyen**

Bu soruda anlatacağın şey senin gerçek günün. Şunları netleştir:

- Son bir ayda hangi dosyaları açtın — Java dışında hangi diller günlük işinde geçti?
- Java'yı hangi somut nedenle kullanıyorsun — ekip mi, ekosistem mi, mevcut sistem mi?
- Java'nın seni yorduğu bir yer var mı — hangi işte başka bir araç daha kolay olurdu?
- Betik yazdığın oldu mu — ne için: veri düzeltme, dağıtım, log ayıklama, test verisi?
- SQL'de en zorlandığın iş neydi — onu nasıl çözdün?
- Yeni bir dil veya çatı öğrendin mi — ne kadar sürdü, nasıl çalıştın?
- Bir aracı bırakıp başkasına geçtiğin oldu mu — kararın gerekçesi neydi?

Burada uydurmaya hiç gerek yok; bu sorunun malzemesi zaten senin günlük işinde duruyor. Bilmediğin bir dili biliyormuş gibi anlatmak ise en kolay yakalanan abartıdır (`256`).

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, sesli çalışılmak için yazıldı. Sırayı bozma:

1. Soru başlığını oku, sonra iskeleti görmeden kapat ve cevabı yüksek sesle ver. İçinden geçirmek prova sayılmaz.
2. Süre tut ve doksan saniyeyi hedefle. Bu sorularda uzun cevap dağınık, kısa cevap ise yüzeysel algılanır.
3. Cevabını kaydet ve dinle. Bu altı soruda ton, içerik kadar belirleyicidir; suçlayıcı bir cümle kaydında hemen duyulur.
4. Sonra iskeletle karşılaştır ve hangi adımı hiç söylemediğini işaretle. En sık atlanan adım ölçüt cümlesidir.
5. Elenme sebeplerini tek tek oku ve kendi cevabında birinin geçip geçmediğine bak.
6. "Kendi hikâyen" sorularını yazarak cevapla. Yazmadan ezberlemeye çalışma; yazılmayan hikâye mülakatta dağılır.
7. Ertesi gün aynı soruyu iskelete bakmadan tekrarla. İkinci turda hedef süre yetmiş beş saniyedir.

Bir uyarı: bu altı soruda cümleleri ezberlemek özellikle risklidir. Liderlik soruları ezberlendiğinde samimiyetsiz duyulur, teknik sorulardan daha çabuk ele verir. Ezberlenecek şey cümle değil sıradır; sıra aklında kalırsa cümleyi o an kurarsın.

### Tek cümlede

Bu altı soruda kazanan cevap yönetici taklidi yapan cevap değil, kararını ölçüte bağlayan mühendis cevabıdır. Mimari soruda ölçüt, seçenek ve takas aranır; süreç sorularında rol netliği ve erken uyarı aranır. Paydaş ve ekip sorularında ise iki şey ölçülür: belirsizi somuta çevirebilmek ve belirtiden nedene geçebilmek. Dil sorusunda ise seçim gerekçelendirilir, zayıf taraf da söylenir. Hepsinde ortak disiplin aynıdır: ölçüt koy, takası söyle, kararı kaydet, sonucu göster. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# sozlu5-1 | Temel
## Soru
Bir ekip mesajlaşma altyapısını değiştirdi. Altı ay sonra "biz bunu neden böyle yapmıştık" tartışması çıktı. ADR'nin bu tartışmadaki asıl işlevi nedir?
## A
Kararın nasıl uygulanacağını adım adım gösteren teknik kılavuz olmak; ekip koda değil belgeye bakar
🤖 ADR bir uygulama kılavuzu değildir; kararın nasılını değil nedenini tutar.
## B
Kararın bağlamını, değerlendirilen seçenekleri ve sonuçlarını kaydetmek; böylece gerekçe kaybolmaz
🤖 Doğru: ADR'nin asıl değeri reddedilen yolları ve nedenini korumasıdır.
## C
Kararı onaylayan kişiyi kayda geçirerek sorumluluğu belirlemek ve tartışmayı kapatmak
🤖 ADR sorumlu arama belgesi değildir; amacı gerekçeyi gelecekte okunur tutmaktır.
## D
Karar değiştiğinde eski kaydı silmek ve belgeyi her zaman güncel tek sürüm hâlinde tutmak
🤖 eski ADR silinmez; yenisi eskisini geçersiz kılar ve düşünce izi korunur.
## Doğru
B
## Açıklama
ADR'nin işi kararı **anlatmak** değil, kararın **gerekçesini** korumaktır. İçinde dört parça bulunur: bağlam, değerlendirilen seçenekler, verilen karar ve kararın sonuçları (`140`). Altı ay sonra ortaya çıkan tartışmanın kaynağı genellikle şudur: karar hatırlanır ama neyin feda edildiği hatırlanmaz. Reddedilen seçeneği ve ret gerekçesini yazmak, aynı tartışmanın baştan açılmasını engeller. A şıkkı ADR'yi bir kurulum kılavuzu sanır; nasıl yapıldığı koda ve dokümana aittir. C şıkkı belgeyi sorumluluk kaydına indirger; bu, kayıt kültürünü savunmaya çevirir ve kimse yazmaz olur. D şıkkı en yaygın yanlış uygulamadır: eski kayıt silinirse kararın evrimi kaybolur. Doğru pratik, yeni bir ADR yazıp eskisini geçersiz kılmaktır. Sözlü cevapta bu bir cümleyle geçilir, çünkü asıl puan ölçüt ve takas kısmındadır. Kararın geri dönülebilir olup olmadığını sormak da aynı disiplinin parçasıdır (`141`).

# sozlu5-2 | Orta
## Soru
Sprint planlamada bir iş için "beş gün" tahmini verildi. Yönetici bunu teslim sözü olarak duyuyor. Geliştirici gözünden en doğru yaklaşım hangisidir?
## A
Tahmini taahhüt gibi kabul edip gerekirse fazla mesaiyle kapatmak; verilen sayı bir sözdür
🤖 sürdürülebilir değildir ve kaliteden keser; kök sorun tahminin nasıl kurulduğudur.
## B
Tahmini iki katına çıkarıp güvenli tarafta kalmak; şişirilmiş sayı herkesi rahatlatır
🤖 sistemli şişirme güveni bozar ve planlamayı anlamsız hâle getirir.
## C
Tahmin vermeyi reddetmek ve yalnız iş bittiğinde haber vermek; belirsizlik sayıya çevrilemez
🤖 belirsizlik paylaşılmaz, gizlenmiş olur; planlama tümüyle imkânsızlaşır.
## D
Aralık ve varsayımla tahmin vermek, ilerledikçe güncellemek; tahmin belirsizlik taşır, taahhüt söz taşır
🤖 Doğru: ayrımı adlandırmak sonraki tartışmanın yarısını baştan bitirir.
## Doğru
D
## Açıklama
Buradaki asıl mesele **tahmin ile taahhüt ayrımıdır** ve bu ayrım söylenmezse karşı taraf ikisini aynı şey sanar. Tahmin bilinmezlik içeren bir öngörüdür; taahhüt ise verilmiş bir sözdür. Doğru pratik aralık vermek ve varsayımı açıkça söylemektir: "iki ile beş gün, şu servis hazırsa". Varsayımı yazmak, sonradan çıkacak tartışmanın büyük kısmını baştan kapatır. Tahmin ilerledikçe güncellenir; ilk gün verilen sayı en az bilgiyle verilmiştir. A şıkkı kaliteyi ve sürdürülebilirliği harcar; üstelik gizli mesai tahminlerin kalıcı olarak yanlış kalmasına yol açar. B şıkkı şişirmeyi çözüm sanır; şişirme kısa vadede rahatlatır, orta vadede güveni bitirir. C şıkkı belirsizliği paylaşmak yerine saklar; planlama yapılamaz hâle gelir. Aynı disiplinin devamı tanımlanmış bitiştir: test, inceleme ve dağıtım yapılmadan iş bitmiş sayılmaz (`261`). Teknik borç da backlog'da görünür bir kalem olarak durmalıdır (`132`).

# sozlu5-3 | Orta
## Soru
Tarih sabit, kapsam büyük, proje geride kaldı. Yönetim ekibe üç yeni geliştirici eklemeyi öneriyor. En doğru değerlendirme hangisidir?
## A
Ekip büyüdükçe iş hacmi doğru orantılı artar; üç kişi gecikmeyi kabaca üçte bir oranında kapatır
🤖 doğrusal artış varsayımı yanlıştır; iletişim ve yetiştirme maliyeti hesaba katılmamış.
## B
Kalite eşiğini geçici olarak düşürmek en ucuz ayardır; eksik testler sürüm sonrasında tamamlanır
🤖 kalite pazarlık kalemi değildir; ertelenen test borcu faiziyle geri döner.
## C
Yeni kişi kısa vadede mevcut ekibi yavaşlatır; tarih sabitse ayarlanacak değişken kapsamdır
🤖 Doğru: yetiştirme maliyeti gerçektir ve üçgende esneyecek yer kapsamdır.
## D
Üçgende değişkenler birbirinden bağımsızdır; kapsam korunurken tarih tek başına sabitlenebilir
🤖 üç değişken birlikte hareket eder; biri sabitlenince başkası esnemek zorundadır.
## Doğru
C
## Açıklama
Kapsam, zaman ve kaynak birlikte hareket eder; biri sabitlenince başkası esner. Geç kalmış bir işe insan eklemek sezgiye uygun görünür ama kısa vadede ters çalışır. Yeni gelen kişi kod tabanını öğrenene kadar mevcut ekibin zamanını tüketir; ayrıca ekip büyüdükçe iletişim yolları hızla artar. Bu yüzden tarih gerçekten sabitse esnetilecek değişken kapsamdır: hangi özelliğin ilk sürüme girmeyeceğine karar verilir. A şıkkı doğrusal bir varsayım kurar ve yetiştirme maliyetini yok sayar. B şıkkı kaliteyi ayar düğmesi sanır; atlanan test ve inceleme, ilerleyen haftalarda hata ayıklamayla geri ödenir. D şıkkı üç değişkeni bağımsız sanır ve üçgenin anlamını ortadan kaldırır. Geliştiricinin bu tabloda iki katkısı vardır: gerçekçi tahmin ve erken uyarı. Belirsizliği azaltan şey daha uzun tahmin toplantısı değil, en riskli parçanın erken çalıştırılmasıdır. Kalite kalemlerinden kesilen her şey teknik borç olarak kaydedilmelidir (`132`).

# sozlu5-4 | Orta
## Soru
İki paydaş çakışıyor: biri "rapor gerçek zamanlı olsun", diğeri "rapor tutarlı olsun" diyor. İlk adım ne olmalıdır?
## A
"Gerçek zamanlı" ve "tutarlı" ifadelerini ölçüye çevirmek; kabul edilebilir gecikmeyi ve sapmayı sormak
🤖 Doğru: çakışmaların çoğu gerçek öncelik farkı değil tanım farkıdır.
## B
Teknik olarak daha kolay olanı seçmek ve diğer paydaşa gerekçesiz biçimde olmaz demek
🤖 maliyeti gösterilmeden verilen ret tartışmayı kapatmaz, daha da büyütür.
## C
İki isteği de eksiksiz karşılayan bir tasarım aramak; doğru mimari her ikisini birden verir
🤖 ölçüt konmadan verilen "ikisi de" sözü kapsamı sessizce büyütür.
## D
Kararı doğrudan yöneticiye devredip netleşmesini beklemek; öncelik geliştiricinin işi değildir
🤖 seçenek ve maliyet sunulmadan devredilen karar askıda kalır ve geri döner.
## Doğru
A
## Açıklama
Paydaş çakışmalarının büyük kısmı **aynı kelimeye iki anlam yüklenmesinden** doğar. "Gerçek zamanlı" biri için bir saniye, diğeri için beş dakika demek olabilir. "Tutarlı" ise kimi için anlık kesinlik, kimi için gün sonunda mutabık kalmaktır. Bu yüzden ilk adım tartışmayı değil, tanımı ele almaktır: kabul edilebilir gecikme kaç saniye, kabul edilebilir sapma ne kadar. Kelime sayıya çevrildiğinde çakışmanın gerçek olup olmadığı hemen görülür; çoğu zaman iki istek aynı anda karşılanabilir hâle gelir. Netleştirmenin en hızlı aracı somut senaryodur: "kullanıcı şunu yaptığında ekranda ne görmeli". Sonra kabul ölçütü yazılır ve iki tarafa da gösterilir. B şıkkı gerekçesiz ret üretir ve teknik bahane gibi duyulur. C şıkkı ölçüt koymadan söz verir; kapsam sessizce büyür. D şıkkı devri erken yapar. Doğru sıra şudur: geliştirici seçenekleri ve maliyeti verir, önceliği kararın sahibi seçer, mutabakat yazıya geçer.

# sozlu5-5 | İleri
## Soru
Ekipteki bir geliştiricinin işleri son iki aydır sürekli gecikiyor ve gözden geçirmelerde tekrar eden eksikler çıkıyor. İlk müdahaleyi belirleyen en kritik ayrım hangisidir?
## A
Kişinin kıdemi; kıdemliyse doğrudan hedef verilir, yeni başlayan biriyse daha uzun süre tanınır
🤖 kıdem bir teşhis değildir; aynı belirtinin nedeni her kıdem düzeyinde farklı olabilir.
## B
Gecikmenin ekibe maliyeti; maliyet yüksekse kişiyi kritik iş dağıtımının dışında tutmak
🤖 önlem gibi görünür ama nedeni bulmaz; sorunu erteler ve genellikle büyütür.
## C
Konunun ekipçe açıkça konuşulup konuşulmayacağı; şeffaflık her durumda en hızlı çözümdür
🤖 herkesin önünde verilen geri bildirim savunma üretir; önce özel konuşulur.
## D
Nedenin engel mi, beceri mi yoksa isteksizlik mi olduğu; üçünün müdahalesi birbirinden farklıdır
🤖 Doğru: yanlış teşhis, iyi niyetle uygulandığında bile zarar verir.
## Doğru
D
## Açıklama
Aynı belirtinin üç ayrı nedeni olabilir ve müdahaleyi belirleyen şey nedendir. **Engel** varsa kişi zaten çabalıyordur: bekleyen bir ortam, eksik yetki, bulanık gereksinim, sürekli bölünme. Burada eğitim vermek işe yaramaz, engelin kaldırılması gerekir. **Beceri** eksikse öğretmek, eşli çalışmak ve işi küçültmek gerekir; bu durumda baskı hatayı çoğaltır. **İsteksizlik** ise en son varsayılacak nedendir ve açık bir konuşma ister: beklenti nedir, karşılanmazsa ne olur. Pratikte vakaların çoğu birinci gruptadır; bu yüzden veri toplamadan başlanan konuşma yanlış yere gider. A şıkkı kıdemi teşhis sanır. B şıkkı kişiyi işten çekerek belirtiyi gizler ve durumu kalıcı hâle getirir. C şıkkı şeffaflığı yanlış yere uygular; açıkta yapılan geri bildirim savunma refleksi doğurur ve öğrenmeyi durdurur (`195`). Doğru sıra şudur: veri, özel konuşma, davranış ve etkisi, teşhis, sonra ne–ne zamana kadar–nasıl doğrulanacak biçiminde ölçülebilir bir plan.

# sozlu5-6 | İleri
## Soru
"Hangi dilleri kullanıyorsunuz ve neden?" sorusuna verilen aşağıdaki cevaplardan hangisi en güçlü sinyali verir?
## A
"Java severim, on yıldır yazıyorum ve bugün var olan en iyi dil olduğunu düşünüyorum"
🤖 sevgi beyanı bir gerekçe değildir; karşılaştırma ve ölçüt hiç yok.
## B
"Ağırlıklı Java; gerekçem ekosistem, ekip bilgisi ve işletme maliyeti — zayıf yanlarını da sayabilirim"
🤖 Doğru: seçim ölçüte bağlanmış ve tek yönlü övgüye düşülmemiş.
## C
"Dil aslında önemli değildir; iyi bir geliştirici hangi dille olursa olsun aynı işi çıkarır"
🤖 ekosistem, araç ve işletme maliyeti farklarını yok sayan yüzeysel bir genelleme.
## D
"Şirket hangi dili seçerse onu yazarım; benim için hepsi aynı ve tercih belirtmem gerekmez"
🤖 uyumlu görünür ama gerekçe ve tercih üretmeyen adayı işaret eder.
## Doğru
B
## Açıklama
Bu soru ısınma sorusu gibi görünse de iki şeyi birden ölçer: **seçim yapabilme** ve **dürüstlük**. Güçlü cevap dili işe göre seçer ve gerekçeyi üç ölçüte bağlar: ekosistem olgunluğu, ekibin mevcut bilgisi, işletme maliyeti. Ardından somut güçlü yanlar gelir; olgun kütüphaneler, güçlü araçlar, uzun ömürlü kodda geriye uyum. Aynı cevabın içinde zayıf yan da söylenir: aynı işi daha çok satırla anlatmak, bellek ayak izi, kısa ömürlü işlerde açılış süresi. Kendi aracının zayıf yanını söyleyen aday hemen güven kazanır, çünkü bu sorgulayan bir mühendisi gösterir. A şıkkı ölçütsüz bir beğeni ifadesidir ve kapalı düşünce sinyali verir. C şıkkı farkları yok sayar; ekosistem ve işletme maliyeti gerçek maliyetlerdir. D şıkkı uyum gibi paketlenmiş bir kayıtsızlıktır. Cevabın sonu öğrenme eğrisiyle bağlanır: son ne öğrendin ve nasıl. SQL'i de bu listeye katmak gerçekçi durur, çünkü performans sorunlarının önemli kısmı orada çözülür (`29`).
