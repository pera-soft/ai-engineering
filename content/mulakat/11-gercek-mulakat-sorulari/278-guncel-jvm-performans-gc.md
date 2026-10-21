---
slug: guncel-jvm-performans-gc
baslik: "Sözlü Prova · JVM Performansı 2026: Çöp Toplayıcı Seçimi, ZGC, Teşhis ve Native Image"
kademe: İleri
sira: 278
etiketler: 11
---

## Konu anlatımı

Bu dosyada beş açık uçlu mülakat sorusu var. Hepsi aynı sinir ucuna dokunuyor: bu aday performansı tahminle mi yönetiyor, ölçerek mi. Sorular 2026'da sık soruluyor, çünkü ZGC ve native image ekosistemde artık deneysel değil. Ama çoğu aday bunları duymuş, üretimde kullanmamıştır; görüşmeci de bunu bilir. Ölçtüğü şey bu yüzden sürüm ezberi değil, kavrayış ve karar disiplinidir. Her sorunun altında dört parça var: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı; senin adına deneyim uydurulmaz.

Bütün bölümü tek bir cümle taşır: **ölçmeden seçim yapılmaz.** Toplayıcı seçimi de, ayar değişikliği de, native image kararı da bu cümlenin arkasına dizilir.

### "Hangi çöp toplayıcıyı seçersin, neye göre?"

**Soru aslında ne ölçüyor?**

Bu soru bayrak ezberi istemiyor. Görüşmeci `-XX:+UseZGC` yazmayı zaten biliyor. Merak ettiği şey, adayın seçimi hangi eksende kurduğudur. Zayıf cevap bir toplayıcı adı söyler ve durur. Güçlü cevap önce ekseni kurar, sonra iş yükünü sorar, en sonunda ad verir. İkinci ayrıştırıcı da şudur: aday ölçmeden karar verdiğini fark ediyor mu. "En yenisini seçerim" diyen aday, mühendislik değil moda anlatıyor sayılır.

**Sözlü cevap iskeleti**

1. **Ekseni ilk cümlede kur.** Toplayıcılar üç eksende yarışır: duraklama (pause), iş hacmi (throughput), ayak izi (footprint). Üçünü birden en iyi yapan toplayıcı yoktur (`61`, `62`).
2. **Soruyu iş yüküne çevir.** Önce şunu sorarım: hedef nedir? Kullanıcıya cevap veren bir servis mi, gece çalışan bir toplu iş mi? Yığın ne kadar büyük? Kuyruk gecikmesi için bir hedef yazılmış mı (`151`, `152`).
3. **Eşlemeyi ver ve gerekçesini söyle.** Dengeli genel amaç için G1; JDK 9'dan beri varsayılandır, bölge (region) tabanlıdır ve duraklama hedefi ayarlanabilir. Gecikme kritikse ZGC. Duraklamaya toleranslı, iş hacmi öncelikli toplu iş için Parallel (`61`).
4. **Varsayılanı savun.** Çoğu uygulamada doğru karar G1'de kalmaktır. Toplayıcıyı değiştirmek, ölçüm bir sorun gösterdiğinde gündeme gelir; önce değil.
5. **Ölçüm kuralını söyle.** Toplayıcı değişikliği tek başına bir hipotezdir. Aynı yük altında ölçülür, tek değişken değiştirilir, aynı ölçüm tekrarlanır (`64`).
6. **Sürüm konusunda dürüst ol.** Bayrak ve sürüm numarasından emin değilsen söyleme. Onun yerine hangi sorunu çözdüğünü anlat; yanlış sürüm söylemek güvenilirliği düşürür (`256`).

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="GC seciminin uc ekseni ve toplayici eslemesi">
  <defs>
    <marker id="gncjvm-eksen" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç eksen: duraklama · iş hacmi · ayak izi — üçü birden en iyi olmaz</text>
  <rect x="14" y="36" width="204" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Duraklama (pause)</text>
  <text x="116" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tek isteğin beklediği süre</text>
  <text x="116" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kuyruk gecikmesini belirler</text>
  <text x="116" y="119" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hedef p95 ve p99 ile yazılır</text>
  <text x="116" y="139" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kullanıcıya bakan servisin derdi</text>
  <rect x="228" y="36" width="204" height="112" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İş hacmi (throughput)</text>
  <text x="330" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">birim zamanda biten iş</text>
  <text x="330" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">duraklama uzasa da toplam önemli</text>
  <text x="330" y="119" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">toplu iş ve analitik yükler</text>
  <text x="330" y="139" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">CPU'yu toplamaya vermeye razı</text>
  <rect x="442" y="36" width="204" height="112" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="544" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Ayak izi (footprint)</text>
  <text x="544" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">toplam bellek tüketimi</text>
  <text x="544" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">konteyner limitini belirler</text>
  <text x="544" y="119" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yoğun ölçekte maliyet kalemi</text>
  <text x="544" y="139" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">meta veri ve bariyer de sayılır</text>
  <line x1="116" y1="150" x2="116" y2="166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncjvm-eksen)"/>
  <line x1="330" y1="150" x2="330" y2="166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncjvm-eksen)"/>
  <line x1="544" y1="150" x2="544" y2="166" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncjvm-eksen)"/>
  <rect x="14" y="170" width="204" height="102" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="116" y="191" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">ZGC</text>
  <text x="116" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">duraklamayı seçer</text>
  <text x="116" y="231" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">büyük yığın, sıkı gecikme hedefi</text>
  <text x="116" y="252" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">bedeli: iş hacmi ve ayak izi</text>
  <text x="116" y="266" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">bayrakla açıkça seçilir</text>
  <rect x="228" y="170" width="204" height="102" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="191" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">G1 · varsayılan</text>
  <text x="330" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dengeyi seçer</text>
  <text x="330" y="231" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bölge tabanlı, duraklama hedefli</text>
  <text x="330" y="252" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">çoğu serviste doğru karar</text>
  <text x="330" y="266" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">JDK 9'dan beri varsayılan</text>
  <rect x="442" y="170" width="204" height="102" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="544" y="191" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Parallel</text>
  <text x="544" y="212" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş hacmini seçer</text>
  <text x="544" y="231" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">toplu iş, analitik, gece penceresi</text>
  <text x="544" y="252" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">bedeli: uzayan duraklamalar</text>
  <text x="544" y="266" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kullanıcıya bakan serviste zayıf</text>
  <rect x="14" y="284" width="632" height="38" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="299" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kural: ölçmeden seçim yapılmaz</text>
  <text x="330" y="315" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Toplayıcı değişikliği bir hipotezdir; aynı yük altında ölçülür, tek değişken değiştirilir, ölçüm tekrarlanır.</text>
</svg>
```

**Elenme sebepleri**

- Doğrudan bir ad söyleyip durmak. "ZGC kullanırım" cevabı, ekseni hiç kurmadığını gösterir.
- İş yükünü hiç sormamak. Hedefi bilmeden toplayıcı seçmek, hastayı görmeden ilaç yazmaktır.
- En yeniyi en iyi saymak. Yenilik bir ölçüt değildir; ölçüt duraklama, iş hacmi ve ayak izidir.
- Bedeli söylememek. Bir toplayıcıyı seçip neyden vazgeçtiğini anlatmayan cevap reklam gibi duyulur.
- Varsayılanı küçümsemek. Çoğu serviste doğru karar G1'de kalmaktır; bunu söylemek olgunluk göstergesidir.
- Sürüm ve bayrak ezberine sığınmak. Yanlış hatırlanan bir bayrak, bilgiden çok güvenilirlik kaybettirir.
- Ölçümden hiç söz etmemek. Ölçmeden yapılan ayar bir tahmindir ve genellikle yeni bir sorun üretir.

**Kendi hikâyen / dürüst köprü**

Bu soruda üretim deneyimi olmayabilir; o zaman uydurma. Şu sorular sende gerçek bir malzeme var mı diye yoklar:

- Çalıştığın uygulamada toplayıcı hiç konuşuldu mu? Varsayılanda mı kalındı, bilerek mi değiştirildi?
- Yığın boyutu neye göre verildi? Konteyner limiti ile `-Xmx` arasında bir ilişki kuruldu mu?
- Bir gecikme hedefi yazılı mıydı? Yoksa "yavaş" kelimesiyle mi konuşuluyordu?
- Toplayıcı değişikliği önerildiyse ne ölçüldü, öncesi ve sonrası karşılaştırıldı mı?
- Toplu iş ile kullanıcıya bakan servis aynı ayarla mı çalışıyordu?

Hiçbiri yoksa `256`'daki dürüst köprüyü kur: "üretimde toplayıcı değiştirmedim" de, sonra ekseni anlat. Kendi projende bir uç noktaya yük verip GC logunu açmak da gerçek bir gözlemdir. Bunu bir üretim vakası gibi anlatma; gözlem olarak anlat, değeri düşmez.

### "ZGC ne getirdi, bedeli ne?"

**Soru aslında ne ölçüyor?**

ZGC'nin adını herkes duydu. Ayrım tanımdan sonra başlar. Görüşmeci üç şeye bakar. Birincisi, adayın "düşük gecikme" ile "GC bitti" arasındaki farkı bilmesi. İkincisi, bedeli adlandırabilmesi; bedelsiz kazanç anlatan aday takas kavramını kurmamıştır. Üçüncüsü, kuşaklı (generational) mod hakkında doğru konuşabilmesi. Bu son nokta sürüme duyarlıdır; emin değilsen mekanizmayı anlat, tarih verme.

**Sözlü cevap iskeleti**

1. **Hedefi tek cümlede söyle.** ZGC bir düşük gecikme toplayıcısıdır. Amacı, duraklamaların yığın boyutundan bağımsız ve tipik olarak alt-milisaniye kalmasıdır (`62`).
2. **Mekanizmayı iki kavramla ver.** Renkli işaretçiler (colored pointers) GC durumunu referansın kullanılmayan bitlerine gömer. Yükleme bariyeri (load barrier) referans okunduğu anda bayat işaretçiyi yeni adrese düzeltir. Bu ikisi sayesinde nesneler uygulama durmadan taşınır.
3. **Bedeli açıkça adlandır.** Kazanç bedava değildir. Her referans okumasına eklenen bariyer ve eşzamanlı toplama işi, uygulamayla CPU paylaşır. Sonuç: ham iş hacminde bir miktar düşüş ve büyüyen bir ayak izi. ZGC ayak izi şampiyonu değildir.
4. **Sınırı söyle.** Alt-milisaniye bir hedeftir, mutlak garanti değil. Uygulamanın ayırma hızı eşzamanlı toplama hızını aşarsa ayırma bekletmesi (allocation stall) görülür. "ZGC ile duraklama tamamen biter" cümlesi yanlıştır.
5. **Kuşaklı modu doğru anlat.** Kuşaklı ZGC, JDK 21'de `-XX:+ZGenerational` bayrağıyla isteğe bağlı geldi. JDK 23'te varsayılan mod oldu ve nesilsiz mod kullanımdan kaldırıldı; JDK 24'te nesilsiz mod tamamen çıkarıldı. Bugün `-XX:+UseZGC` demek doğrudan kuşaklı ZGC demektir (`62`).
6. **Neden kuşaklı olduğunu ekle.** Çoğu nesne genç ölür. Nesilleri ayrı toplamak, alt-milisaniye hedefini korurken CPU ve bellek maliyetini düşürür (`61`).

**Elenme sebepleri**

- "ZGC duraklamayı tamamen kaldırır" demek. Toplama devam eder; yalnız duraklama olarak görünmez hale gelir.
- Bedeli hiç anmamak. Üç eksenden birini seçmek, diğer ikisinden ödün vermektir; bu söylenmezse takas kurulmamış olur.
- ZGC'yi ayak izi çözümü sanmak. Renkli işaretçi ve meta veri belleği büyütür; ZGC düşük bellek için seçilmez.
- Shenandoah ile mekanizmayı karıştırmak. Shenandoah tarihsel olarak nesne başlığında yönlendirme işaretçisi tutar; renkli işaretçi ZGC'ye özgüdür.
- Sürüm numarasını yanlış söylemek. Emin değilsen tarih verme; kuşaklı toplamanın ne işe yaradığını anlat, o yeter.
- ZGC'yi küçük yığınlarla sınırlı sanmak. Tam tersine büyük yığınlar için tasarlanmıştır.
- Kararı ölçüme bağlamamak. "Gecikme sorunumuz vardı, ölçtük, şu hedefi tutturamadık" cümlesi olmadan seçim gerekçesiz kalır.

**Kendi hikâyen / dürüst köprü**

Bu konuda üretim deneyimi mülakata gelen adayların çoğunda yoktur. Görüşmeci de bunu bekler. Şunları kendine sor:

- Hiç bir uygulamada duraklama şikâyeti duydun mu? Belirti neydi: ara ara donma mı, zaman aşımı mı?
- GC logunu hiç açtın mı? `-Xlog:gc*` çıktısında duraklama sürelerine baktın mı?
- Yığın büyüdükçe duraklamanın uzadığını gözlemledin mi?
- Bir gecikme hedefi konuldu mu, konulduysa kim yazdı: ekip mi, iş tarafı mı?

Deneyimin yoksa `256`'daki köprüyü kur. Şöyle bir cümle dürüst ve güçlüdür: "ZGC'yi üretimde kullanmadım; ama G1 ile çalışan bir serviste duraklama ölçtüm ve şu farkı gördüm." Ardından mekanizmayı anlat. Uydurulmuş bir üretim hikâyesi, ilk ayrıntı sorusunda dağılır.

### "Üretimde GC kaynaklı bir yavaşlığı nasıl teşhis edersin?"

**Soru aslında ne ölçüyor?**

Bu, dosyanın en ayrıştırıcı sorusudur ve cevabı bir araç listesi değildir. Görüşmeci sıraya bakar. Doğru sıra aramayı her adımda daraltır; yanlış sıra doğrudan en pahalı araca atlar. İkinci ayrıştırıcı, adayın "GC sorunu" ile "bellek sızıntısı" arasındaki farkı görmesidir. İkisi aynı belirtiyi verir ama tedavileri farklıdır. Üçüncüsü de şudur: aday sorunun GC'de olmayabileceğini kabul ediyor mu.

**Sözlü cevap iskeleti**

1. **Önce belirtiyi ölçüte çevir.** "Yavaş" bir duygudur. Hangi uç nokta, hangi yük, hangi yüzdelik dilim? Ortalama saklar, yüzdelik dilim gösterir (`151`, `152`).
2. **Metrikle başla.** Metrik nerede yavaşladığını söyler: uç nokta süreleri, hata oranı, havuz doluluğu, GC duraklama toplamı. Arama alanı burada tüm sistemdir (`104`, `193`).
3. **GC loguna in.** `-Xlog:gc*` duraklama süresini, toplama sıklığını ve yığın doluluk eğilimini gösterir. Asıl bakılan şey, toplama sonrası canlı tabandır (`64`).
4. **Duraklama mı sızıntı mı, burada ayır.** Canlı taban her toplamadan sonra aynı seviyeye dönüyorsa sorun duraklamadadır. Taban kalıcı olarak tırmanıyorsa sızıntı vardır ve toplayıcı değiştirmek işe yaramaz (`65`).
5. **Sızıntı yönünde yığın dökümü al.** Yığın dökümünü MAT'ta aç, hâkim ağacıyla en çok belleği tutan kökü bul, oraya giden referans zincirini izle (`65`).
6. **Duraklama yönünde profilleyiciye geç.** JFR veya async-profiler örnekleme yapar, ek yükü düşüktür ve üretimde açık bırakılabilir. Ayırma hızını ve sıcak çağrı yollarını gösterir (`64`).
7. **Ölçüm disiplinini kapanışa koy.** Tek değişken değiştirilir, aynı ölçüm tekrarlanır, sonuç sayı ile söylenir. Kalıcı çözüm bir alarma bağlanır.

```svg
<svg viewBox="0 0 660 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tesis sirasi: metrik, GC logu, yigin dokumu, profilleyici ve duraklama-sizinti ayrimi">
  <defs>
    <marker id="gncjvm-tani" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Teşhis sırası: her adım arama alanını daraltır</text>
  <rect x="14" y="36" width="152" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="90" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Metrik</text>
  <text x="90" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">nerede yavaş?</text>
  <text x="90" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">uç nokta süresi, hata oranı</text>
  <text x="90" y="116" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">havuz doluluğu, kuyruk</text>
  <text x="90" y="134" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yüzdelik dilim: p95, p99</text>
  <text x="90" y="156" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alan: tüm sistem</text>
  <line x1="168" y1="102" x2="174" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncjvm-tani)"/>
  <rect x="176" y="36" width="152" height="132" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="252" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · GC logu</text>
  <text x="252" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">GC gerçekten suçlu mu?</text>
  <text x="252" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">duraklama süresi ve sıklığı</text>
  <text x="252" y="116" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">toplama sonrası canlı taban</text>
  <text x="252" y="134" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yığın doluluk eğilimi</text>
  <text x="252" y="156" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alan: tek süreç</text>
  <line x1="330" y1="102" x2="336" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncjvm-tani)"/>
  <rect x="338" y="36" width="152" height="132" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="414" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Yığın dökümü</text>
  <text x="414" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">belleği kim tutuyor?</text>
  <text x="414" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hâkim ağacı, tuttuğu boyut</text>
  <text x="414" y="116" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">köke giden referans zinciri</text>
  <text x="414" y="134" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">pahalı adım: sıra gelince alınır</text>
  <text x="414" y="156" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alan: tek nesne grafiği</text>
  <line x1="492" y1="102" x2="498" y2="102" stroke="#5D7789" stroke-width="1.4" marker-end="url(#gncjvm-tani)"/>
  <rect x="500" y="36" width="146" height="132" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="573" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">4 · Profilleyici</text>
  <text x="573" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hangi kod üretiyor?</text>
  <text x="573" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">JFR, async-profiler</text>
  <text x="573" y="116" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">ayırma hızı, sıcak çağrı yolu</text>
  <text x="573" y="134" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">ek yükü düşük, üretimde açık</text>
  <text x="573" y="156" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alan: tek çağrı yolu</text>
  <rect x="14" y="182" width="316" height="118" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="203" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Duraklama sorunu</text>
  <text x="172" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">canlı taban her toplamadan sonra aynı seviyeye döner</text>
  <text x="172" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yığın testere dişi çizer, alt zarf yataydır</text>
  <text x="172" y="266" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çare: ayırmayı azaltmak, yığını sabitlemek</text>
  <text x="172" y="288" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">toplayıcı değişikliği burada anlamlı olabilir</text>
  <rect x="344" y="182" width="302" height="118" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="203" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Bellek sızıntısı</text>
  <text x="495" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">canlı taban her toplamadan sonra biraz daha yükselir</text>
  <text x="495" y="246" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alt zarf tırmanır, sonu bellek hatasıdır</text>
  <text x="495" y="266" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çare: tutan referansı bulmak ve kırmak</text>
  <text x="495" y="288" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">toplayıcı değiştirmek burada hiçbir şeyi çözmez</text>
</svg>
```

**Elenme sebepleri**

- Doğrudan yığın dökümü almak. Pahalı adımdır ve sıra ona gelmeden alınırsa çoğu zaman boşa gider.
- `-Xmx` değerini büyütmeyi ilk çözüm yapmak. Sızıntı varsa bu yalnız bellek hatasını erteler.
- Duraklama ile sızıntıyı ayırmamak. İkisi aynı belirtiyi verir; ayrımı canlı tabanın eğilimi kurar.
- Ortalamayla konuşmak. Duraklama sorunları kuyrukta yaşanır; ortalama onları örter.
- Ölçmeden toplayıcı değiştirmek. Değişiklik bir hipotezdir, hipotezin de ölçümü olmalıdır.
- GC dışını hiç düşünmemek. Yavaşlığın kaynağı çoğu zaman veri tabanı beklemesi ya da kilit çekişmesidir.
- Sonucu izlenimle bildirmek. "Belirgin şekilde düzeldi" bir ölçüm değil, bir duygudur.

**Kendi hikâyen / dürüst köprü**

Burada küçük bir vaka bile işe yarar, yeter ki gerçek olsun:

- Hiç bir uygulamada bellek hatası gördün mü? Mesaj tam olarak neydi, yığın mı doldu, başka bir alan mı?
- Nasıl fark edildi? Alarm mı, gösterge panosu mu, kullanıcı şikâyeti mi?
- Hangi araca baktın ve hangi sırayla? Hangisi seni asıl yere götürdü?
- Kök neden neydi? Büyüyen bir önbellek, temizlenmeyen bir dinleyici, statik bir koleksiyon?
- Nasıl onarıldı? Kod mu değişti, yoksa yeniden başlatan bir iş mi kondu? İkincisi ise bunu dürüstçe söyle.
- Sonra ne ölçüldü? Aynı belirti bir daha görüldü mü, bir alarm eklendi mi?

Böyle bir vakan yoksa uydurma. `256`'daki köprü burada da işler. Kendi projende bilerek büyüyen bir koleksiyon yazıp GC logunda tabanın tırmandığını görmek gerçek bir gözlemdir. Görüşmeciye onu gözlem olarak anlatmak, sahte bir üretim vakasından çok daha güçlüdür.

### "GraalVM native image ne zaman doğru karar?"

**Soru aslında ne ölçüyor?**

Bu soruda görüşmeci bir teknoloji tanımı değil, bir maliyet muhakemesi arar. Native image kulağa hep iyi gelir: hızlı açılır, az bellek yer. Ayrım, adayın bedeli sayabilmesindedir. Üç bedel vardır ve üçünü de söylemek gerekir. Ayrıca doğru cevap "her yerde kullanırım" değildir; native image bazı iş yüklerinde JVM'in gerisinde kalır. Bunu söyleyen aday, moda değil karar anlatıyor demektir.

**Sözlü cevap iskeleti**

1. **Kazançla başla.** Native image, uygulamayı build zamanında tek bir yerel ikiliye derler. Isınma yoktur; milisaniye düzeyinde açılış ve düşük bellek ayak izi verir (`66`).
2. **Uygun adayı adlandır.** Sık soğuk başlayan sunucusuz (serverless) işlevler, komut satırı araçları, hızlı ölçeklenen kısa ömürlü konteynerler. Ortak nokta: ömür kısa, açılış sık.
3. **Kısıtı kavramla ver.** Kapalı-dünya varsayımı (closed-world assumption): derleyici ikiliye neyin gireceğine build zamanında karar verir. Statik analizde görünmeyen kod dışlanır.
4. **Birinci bedel: yansıma yapılandırması.** Yansıma (reflection), dinamik vekil nesne (proxy), JNI ve kaynak yükleme statik analize görünmez. Bunlar erişilebilirlik meta verisiyle önceden bildirilmelidir. Bildirilmezse kod JVM'de çalışır, ikilide çalışma anında patlar (`66`, `92`).
5. **İkinci bedel: build maliyeti.** Native image üretimi bütün kapalı-dünya analizini yapar. Normal derlemeye kıyasla çok daha uzun sürer, çok daha fazla bellek ister ve her hedef platform için ayrı yapılır.
6. **Üçüncü bedel: tepe iş hacmi.** JIT çalışma-zamanı profiline uyarlanır; native image uyarlanamaz. Uzun ömürlü, iş hacmi kritik yüklerde native, JVM'in altında kalabilir.
7. **Kararı tek cümleye bağla.** Kısa ömürlü ve sık açılan iş için native; uzun ömürlü ve iş hacmi kritik iş için JVM. Yoğun yansıma kullanan eski uygulamalar en zahmetli adaydır.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Native image karari: kazanc, bedel ve uygun uygunsuz is yukleri">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Native image: kazancı da bedeli de aynı cümlede söylenir</text>
  <rect x="14" y="36" width="316" height="128" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kazanç</text>
  <text x="172" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">build zamanında tek yerel ikiliye derlenir</text>
  <text x="172" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ısınma yok: milisaniye düzeyinde açılış</text>
  <text x="172" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">JIT altyapısı taşınmaz: düşük ayak izi</text>
  <text x="172" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ulaşılamayan kod atılır</text>
  <text x="172" y="157" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">çöp toplama yine vardır, kalkmaz</text>
  <rect x="344" y="36" width="302" height="128" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Bedel (üçü birden söylenir)</text>
  <text x="495" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 · yansıma ve vekil için yapılandırma</text>
  <text x="495" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">2 · uzun ve pahalı build, platform başına ayrı</text>
  <text x="495" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">3 · tepe iş hacmi JVM'in altında kalabilir</text>
  <text x="495" y="140" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">bildirilmeyen yansıma çalışma anında patlar</text>
  <text x="495" y="157" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">JVM'de çalışan kod ikilide çalışmayabilir</text>
  <rect x="14" y="176" width="316" height="104" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Uygun aday</text>
  <text x="172" y="220" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sık soğuk başlayan sunucusuz işlev</text>
  <text x="172" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">komut satırı aracı</text>
  <text x="172" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hızlı ölçeklenen kısa ömürlü konteyner</text>
  <rect x="344" y="176" width="302" height="104" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="495" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Uygun olmayan aday</text>
  <text x="495" y="220" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">saatlerce çalışan, iş hacmi kritik toplu iş</text>
  <text x="495" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uzun ısınmadan fayda gören motor</text>
  <text x="495" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yoğun yansıma kullanan eski uygulama</text>
  <rect x="14" y="292" width="632" height="36" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="330" y="315" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Karar kuralı: ömür kısa ve açılış sık ise native · ömür uzun ve iş hacmi kritik ise JVM</text>
</svg>
```

**Elenme sebepleri**

- Native image'i her yerde daha hızlı sanmak. Açılış hızlıdır; tepe iş hacmi uzun ömürlü yükte JVM'in altında kalabilir.
- Yansıma sorununu hiç anmamak. Kapalı-dünya kısıtı bu teknolojinin en pratik bedeli ve en sık dert kaynağıdır.
- Yansıma hatasını derleme hatası sanmak. Hata çalışma anında çıkar; bu ayrım cevabın ciddiyetini belli eder.
- Çözümü "yansımayı sil" demek. Çözüm silmek değil, erişilebilirlik meta verisi bildirmektir.
- Build maliyetini atlamak. Uzayan sürüm hattı bir mühendislik maliyetidir ve karara girer.
- "Native image ile GC kalkar" demek. Native ikili de bir çöp toplayıcı kullanır.
- Karar kuralı vermemek. Kazanç ve bedeli sayıp seçim ölçütünü söylemeyen cevap yarım kalır.

**Kendi hikâyen / dürüst köprü**

Native image'i üretimde kullanmış aday sayısı azdır. Uydurma; şu sorular gerçek malzemeyi çıkarır:

- Hiç native image derlemesi denedin mi? Kendi küçük projende bile olsa, ne kadar sürdü?
- Bir yansıma hatası aldın mı? Hata mesajı neydi, nasıl çözdün?
- Uygulamanın açılış süresi hiç sorun oldu mu? Ölçtün mü, yoksa his mi?
- Konteyner bellek limitine takıldığın oldu mu? Limit neye göre verilmişti?
- Sürüm hattının süresi tartışıldı mı? Uzun build kime maliyet çıkarıyordu?

Hiçbirine gerçek bir cevabın yoksa `256`'daki köprüyü kur. "Native image'i üretimde kullanmadım; denedim ve şu kısıtla karşılaştım" cümlesi dürüsttür ve karşındaki bunu olumlu okur. Ardından takası anlat: kazanç açılış ve bellek, bedel yansıma yapılandırması, build süresi ve tepe iş hacmi.

### "Spring Boot AOT ne yapıyor?"

**Soru aslında ne ölçüyor?**

Bu soru genellikle bir öncekinin devamı olarak gelir ve iki şeyi ölçer. Birincisi, adayın Spring AOT işlemesi ile GraalVM native derlemesini ayırabilmesi. Bu ikisi zincirlemedir ama aynı şey değildir; karıştıran aday konuyu ikinci elden duymuştur. İkincisi, AOT'un ne tür işi taşıdığını bilmesi: açılışta yapılan karar işi, derleme zamanına alınır. İnce nokta da şudur: Spring AOT işlemesi yalnız native için değildir, JVM üzerinde de açılışı hızlandırır.

**Sözlü cevap iskeleti**

1. **İki aşamayı ayır.** Spring AOT işlemesi build zamanında uygulama bağlamını önden değerlendirir. GraalVM native derlemesi ise ayrı bir adımdır ve ortaya yerel ikiliyi çıkarır (`92`).
2. **AOT işlemesinin ne yaptığını söyle.** Koşullu yapılandırmaları build anında çözer, hangi Spring'in yönettiği nesnelerin (bean) oluşacağını belirler ve bu kararları üretilmiş Java koduna döker.
3. **Kazancı adlandır.** Açılışta yapılan tarama ve karar işi derleme zamanına taşınır. Uygulama daha az iş yaparak açılır; bu kazanç JVM üzerinde de görülür.
4. **Native ile ilişkiyi kur.** AOT işlemesi ayrıca erişilebilirlik meta verisi üretir. Yansıma ve vekil nesne ihtiyaçlarının büyük kısmını çerçeve kendisi bildirir; bu yüzden native derleme pratikte mümkün olur (`66`).
5. **Sınırı da söyle.** Kararlar build anında donar. Çalışma anında koşula göre değişen dinamik yapılandırma bu modelde daralır; test ve geliştirme genellikle JVM üzerinde sürer.
6. **Yükseltme kararına bağla.** Yeni sürümlerin getirdiği kazanımlar ilgi çekicidir; ama sürüm numarası ve tarih vermeden ne işe yaradığını anlatmak daha güvenlidir (`74`, `256`).

**Elenme sebepleri**

- Spring AOT ile native derlemeyi aynı şey sanmak. İkisi zincirlemedir, aynı adım değildir.
- AOT'u bir küçültme aracı sanmak. Bayt kodu küçültmez; açılış kararlarını önden üretilmiş koda döker.
- AOT'u yalnız native'e özgü sanmak. JVM üzerinde de açılışı hızlandırır; bu ince nokta iyi karşılanır.
- Yansıma meta verisinden hiç söz etmemek. Native derlemeyi mümkün kılan asıl parça budur.
- Sınırı söylememek. Build anında donan kararlar, dinamik yapılandırma esnekliğini daraltır.
- Sürüm numarası uydurmak. Emin olmadığın bir sürümü söylemek, tüm cevabın güvenilirliğini düşürür.

**Kendi hikâyen / dürüst köprü**

Bu soruda dürüstlük en kolay kazanılan puandır:

- Projende açılış süresi ölçüldü mü? Kaç saniyeydi, bunu kim önemsiyordu?
- Koşullu yapılandırma kullandın mı? Hangi koşul hangi nesneyi getiriyordu?
- Sürüm hattında AOT adımı var mı? Varsa build süresine etkisi konuşuldu mu?
- Konteyner başına düşen bellek ve açılış süresi bir maliyet kalemi olarak gündeme geldi mi?

Deneyimin yoksa `256`'daki köprüyü kur. "AOT'u üretimde kullanmadım" demek, yanlış bir açıklama yapmaktan çok daha iyidir. Ardından mekanizmayı anlat: açılışta yapılan iş derleme zamanına taşınır, meta veri üretilir, karşılığında dinamiklik daralır.

### Bu bölümden çıkan ders

Beş sorunun ortak omurgası tek bir disiplindir: seçeneği söyle, ölçütü söyle, bedeli söyle, ölçülen sonucu söyle. Toplayıcı sorusunda kazanan cümle en baştadır; üç eksen vardır ve üçü birden en iyi olmaz. ZGC sorusunda ayrım, kazancın bedelini adlandırmakla kurulur: alt-milisaniye duraklama, iş hacmi ve ayak izi karşılığında alınır. Teşhis sorusunda araç listesi değil sıra konuşur; metrik, GC logu, yığın dökümü, profilleyici. O sırayı taşıyan asıl ayrım da toplama sonrası canlı tabanın eğilimidir: yatay ise duraklama sorunu, tırmanıyorsa sızıntı. Native image sorusunda kazanç kolaydır, bedeli saymak zordur; yansıma yapılandırması, uzayan build ve tepe iş hacmindeki geri kalış birlikte söylenir. AOT sorusunda ise Spring'in build zamanı işlemesi ile yerel derlemeyi ayırmak yeter. Bütün bunların üstünde iki kural durur. Birincisi, ölçmeden seçim yapılmaz; ikincisi, emin olmadığın sürüm bilgisini söylemek yerine mekanizmayı anlat. Ve hiçbir soruda olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# gncjvm-1 | Orta
## Soru
Bir ekip "gecikme önemli" diyerek varsayılan toplayıcıyı bırakıp ZGC'ye geçmeyi öneriyor. Henüz hiçbir ölçüm yapılmamış. Sözlü cevapta en yerinde ilk adım hangisidir?
## A
Öneriyi kabul edip geçişi yapmak; ZGC daha yeni olduğu için her iş yükünde varsayılandan iyi sonuç verir
🤖 yenilik bir ölçüt değildir; kazanç iş hacmi ve ayak izi karşılığında alınır.
## B
Toplayıcıyı değiştirmeden yığın boyutunu iki katına çıkarmak; bellek artınca duraklama sorunu kendiliğinden kapanır
🤖 yığın büyütmek duraklamayı çoğu zaman kısaltmaz, sorunu erteler.
## C
Doğrudan Parallel toplayıcıya geçmek; gecikme sorunlarının kaynağı genellikle yetersiz toplama gücüdür
🤖 Parallel iş hacmi içindir; gecikme hedefi olan serviste tersi yönde çalışır.
## D
Hedefi ölçüte çevirmek: hangi uç nokta, hangi yük ve hangi yüzdelik dilim hedefi; sonra mevcut duraklamayı ölçmek
🤖 Doğru: eksen ve hedef kurulmadan toplayıcı seçimi tahmindir.
## Doğru
D
## Açıklama
Toplayıcı seçimi üç eksende yapılır: **duraklama, iş hacmi (throughput) ve ayak izi (footprint)**. Üçünü birden en iyi yapan toplayıcı yoktur (`61`, `62`). Bu yüzden ilk adım bir ad seçmek değil, hedefi ölçüte çevirmektir. "Gecikme önemli" bir duygudur; "şu uç noktada p99 şu değerin altında kalmalı" bir ölçüttür (`151`, `152`). Ölçüt yazıldıktan sonra mevcut durum ölçülür: GC logunda duraklama süreleri ve sıklığı görülür (`64`). Ancak o zaman toplayıcı değişikliği bir hipotez olarak denenir; tek değişken değiştirilir ve aynı ölçüm tekrarlanır. A şıkkı yeniliği ölçüt sanır; ZGC'nin alt-milisaniye kazancı iş hacmi ve ayak izi karşılığında alınır. B şıkkı yığın büyütmeyi genel çözüm sanar. C şıkkı Parallel'in rolünü ters çevirir; Parallel iş hacmi odaklıdır ve duraklamaları uzayabilir. Çoğu serviste doğru karar, ölçüm aksini göstermedikçe varsayılan G1'de kalmaktır.

# gncjvm-2 | İleri
## Soru
Bir aday ZGC'yi anlatırken "duraklamalar tamamen ortadan kalkar ve bellek kullanımı da düşer" diyor. Bu cümlenin iki hatası nedir?
## A
Duraklama gerçekten sıfırlanır ama bellek artar; tek hata bellek yönündedir
🤖 duraklama sıfırlanmaz; alt-milisaniye bir hedeftir, mutlak garanti değil.
## B
Duraklama sıfırlanmaz, alt-milisaniye hedeflenir; ayak izi de bariyer ve meta veri yüzünden düşmez
🤖 Doğru: toplama sürer, kazancın bedeli iş hacmi ve ayak izidir.
## C
Duraklama sıfırlanmaz ama bellek gerçekten düşer; ZGC ayak izi açısından en ekonomik toplayıcıdır
🤖 ZGC ayak izi şampiyonu değildir; renkli işaretçi ve meta veri belleği büyütür.
## D
İki ifade de doğrudur; ZGC eşzamanlı çalıştığı için hem duraklamayı hem belleği birlikte iyileştirir
🤖 üç eksenin üçünü birden en iyi yapan toplayıcı yoktur.
## Doğru
B
## Açıklama
ZGC bir **düşük gecikme** toplayıcısıdır. Hedefi, duraklamaların yığın boyutundan bağımsız ve tipik olarak **alt-milisaniye** kalmasıdır; bu bir hedeftir, mutlak garanti değil. Uygulamanın ayırma hızı eşzamanlı toplama hızını aşarsa **ayırma bekletmesi (allocation stall)** görülebilir. Toplama hiçbir zaman ortadan kalkmaz, yalnız duraklama olarak görünmez hale gelir. İkinci hata bellek yönündedir: ZGC'nin motoru **renkli işaretçiler (colored pointers)** ve her referans okumasına eklenen **yükleme bariyeridir (load barrier)**. Bariyer uygulamayla CPU paylaşır, renkli işaretçiler ve meta veri ise **ayak izini büyütür**. Yani ZGC ayak izi şampiyonu değildir (`62`). Doğru cümle şudur: gecikme kazancı, ham iş hacmi ve ayak izi karşılığında alınır. A şıkkı duraklamanın sıfırlandığını kabul eder, C şıkkı belleğin düştüğünü, D şıkkı ikisini birden — üçü de takas kuralına aykırıdır (`61`).

# gncjvm-3 | İleri
## Soru
Üretimdeki bir servis ara ara takılıyor. Elinde henüz yalnız gösterge panosu var. Teşhis sırasında hangi adım dizisi en yerindedir?
## A
Önce yığın dökümü almak, MAT'ta incelemek, gerekirse GC logunu açmak ve en sonunda metriklere bakmak
🤖 en pahalı adımla başlar; arama alanı daraltılmadan alınan döküm çoğu zaman boşa gider.
## B
Önce `-Xmx` değerini artırıp yeniden ölçmek, düzelmezse toplayıcıyı değiştirmek, sonra profilleyici açmak
🤖 iki değişikliği ölçmeden yapar; hangisinin ne etkisi olduğu bilinemez.
## C
Metrikle nerede yavaşladığını bulmak, GC logunda duraklama ve canlı taban eğilimine bakmak, sonra döküm veya profilleyiciye geçmek
🤖 Doğru: her adım arama alanını daraltır ve pahalı araç en sona kalır.
## D
Doğrudan profilleyici açıp alev grafiğine bakmak; sıcak çağrı yolu bulunursa diğer adımlara gerek kalmaz
🤖 profilleyici sıcak kodu gösterir ama duraklama ile sızıntı ayrımını kurmaz.
## Doğru
C
## Açıklama
Teşhis bir araç listesi değil, bir **sıradır**; her adım arama alanını daraltır. **Metrik** nerede yavaşladığını söyler ve alan tüm sistemdir (`104`, `193`). **GC logu** (`-Xlog:gc*`) sorunun gerçekten toplamada olup olmadığını gösterir: duraklama süreleri, sıklık ve en önemlisi toplama sonrası **canlı taban (live set)** eğilimi (`64`). Bu eğilim asıl ayrımı kurar. Taban her toplamadan sonra aynı seviyeye dönüyorsa sorun duraklamadadır. Taban kalıcı olarak tırmanıyorsa ortada bir **bellek sızıntısı** vardır ve toplayıcı değiştirmek hiçbir şeyi çözmez (`65`). Ancak bu ayrımdan sonra pahalı araca geçilir: sızıntı yönünde **yığın dökümü** ve hâkim ağacı, duraklama yönünde **profilleyici** ile ayırma hızı ve sıcak çağrı yolları. A şıkkı en pahalı adımla başlar. B şıkkı ölçmeden iki değişiklik yapar; sonuç iyileşse bile hangisinin işe yaradığı bilinmez. D şıkkı profilleyiciyi tek başına yeterli sanır; oysa duraklama ile sızıntı ayrımı GC logundan gelir.

# gncjvm-4 | İleri
## Soru
Bir uygulamanın GC logunda, her toplamadan sonra kalan canlı taban her turda biraz daha yükseliyor ve sonunda bellek hatası alınıyor. Bu tabloda en yerinde adım hangisidir?
## A
Yığın dökümü alıp hâkim ağacıyla belleği tutan kökü ve ona giden referans zincirini bulmak
🤖 Doğru: tırmanan canlı taban sızıntı imzasıdır; kök referans zinciriyle bulunur.
## B
Duraklama süresi uzadığı için ZGC'ye geçmek; düşük gecikmeli toplayıcı tabanın yükselmesini durdurur
🤖 toplayıcı değişikliği erişilebilir kalan referansı serbest bırakmaz.
## C
`-Xmx` değerini iki katına çıkarmak; yığın büyüyünce taban artışı sorun olmaktan çıkar
🤖 yalnız bellek hatasını erteler; taban tırmanmaya devam eder.
## D
Uygulamayı her gece yeniden başlatan bir zamanlanmış iş kurmak ve teşhisi kapatmak
🤖 belirtiyi maskeler, kök nedeni gizler ve borcu büyütür.
## Doğru
A
## Açıklama
Toplama sonrası **canlı tabanın sürekli ve kalıcı yükselmesi**, bellek sızıntısının GC logundaki imzasıdır. Sağlıklı bir uygulamada testere dişi grafiğin alt zarfı yataydır; her toplama tabana döner. Sızıntıda ise serbest bırakılamayan nesneler biriktiği için alt zarf tırmanır ve sonu bellek hatasıdır (`64`, `65`). Bu tablo bir **duraklama** sorunu değildir, dolayısıyla toplayıcı değiştirmek (B) hiçbir şeyi çözmez: sorun toplamanın hızında değil, hâlâ erişilebilir kalan referanslardadır. `-Xmx` büyütmek (C) yalnız hatayı erteler; taban tırmanmaya devam eder. Gece yeniden başlatma (D) belirtiyi maskeler ve teknik borcu büyütür. Doğru adım sıradaki bir sonraki araçtır: **yığın dökümü** alınır, MAT'ta **hâkim ağacı** ile en çok belleği tutan kök bulunur, oraya giden referans zinciri izlenir ve "bu neden hâlâ erişilebilir?" sorusu yanıtlanır. Yaygın kökler büyüyen önbellekler, temizlenmeyen dinleyiciler ve statik koleksiyonlardır.

# gncjvm-5 | Uzman
## Soru
Bir aday native image'i savunurken "JVM'de sorunsuz çalışan kod ikilide de aynen çalışır, ayrıca tepe iş hacmi de artar" diyor. Bu cümlenin temel kusuru nedir?
## A
Yalnız ikinci yarısı yanlıştır; kod uyumluluğu tamdır, tek sorun iş hacmindeki düşüştür
🤖 kod uyumluluğu tam değildir; bildirilmeyen yansıma çalışma anında hata verir.
## B
Yalnız ilk yarısı yanlıştır; yansıma sorun çıkarır ama native image tepe iş hacminde JVM'i geçer
🤖 uzun ömürlü yükte tepe iş hacmi genellikle JIT'in üstünlüğüdür.
## C
İki yarı da yanlıştır: bildirilmeyen yansıma ikilide çalışma anında patlar, tepe iş hacmi ise JVM'in altında kalabilir
🤖 Doğru: kapalı-dünya kısıtı ve uyarlanamayan derleme birlikte söylenmeli.
## D
Cümle doğrudur; erişilebilirlik analizi tüm dinamik yolları otomatik bulduğu için ek yapılandırma gerekmez
🤖 statik analiz yansıma, vekil nesne ve kaynak yükleme yollarını göremez.
## Doğru
C
## Açıklama
Cümlenin iki yarısı da yanlıştır. Birinci yarı **kapalı-dünya varsayımını (closed-world assumption)** yok sayar: derleyici ikiliye neyin gireceğine build zamanında karar verir. **Yansıma (reflection), dinamik vekil nesne (proxy), JNI ve kaynak yükleme** statik analize görünmez, bu yüzden varsayılan olarak dışlanır. Bildirilmezlerse kod JVM'de sorunsuz çalışırken ikilide **çalışma anında** hata verir; bu bir derleme hatası değildir ve çözümü yansımayı silmek değil, **erişilebilirlik meta verisi** bildirmektir (`66`, `92`). İkinci yarı ise takası ters çevirir. JIT çalışma-zamanı profiline uyarlanır; native image uyarlanamaz. Uzun ömürlü ve iş hacmi kritik yüklerde native, JVM'in **altında** kalabilir. Native image'in iki bayrak kazancı başlangıç süresi ve ayak izidir; buna karşılık üç bedel vardır: yansıma yapılandırması, uzun ve platform başına yinelenen build, ve tepe iş hacmindeki geri kalış. Karar kuralı basittir: ömür kısa ve açılış sıksa native, ömür uzun ve iş hacmi kritikse JVM.

# gncjvm-6 | Uzman
## Soru
Bir görüşmeci "Spring Boot AOT işlemesi tam olarak ne yapıyor?" diye soruyor. En doğru cevap hangisidir?
## A
Bayt kodunu küçültüp kullanılmayan sınıfları atar; ürettiği şey daha küçük bir jar dosyasıdır
🤖 AOT işlemesi bir küçültme aracı değildir; açılış kararlarını koda döker.
## B
Uygulamayı doğrudan yerel bir ikiliye derler; GraalVM adımıyla aynı işi yapar, yalnız adı farklıdır
🤖 iki aşama zincirlemedir ama aynı değildir; yerel ikiliyi native derleme üretir.
## C
Çalışma anında JIT derlemesini devre dışı bırakır; böylece açılışta ısınma maliyeti ödenmez
🤖 AOT işlemesi JIT'i kapatmaz; build zamanı bağlam değerlendirmesidir.
## D
Build zamanında uygulama bağlamını önden değerlendirir; koşulları çözer, oluşacak nesneleri belirler ve kararları üretilmiş koda döker
🤖 Doğru: açılışta yapılan karar işi derleme zamanına taşınır.
## Doğru
D
## Açıklama
İki aşamayı ayırmak bu sorunun tamamıdır. **Spring AOT işlemesi** build zamanında uygulama bağlamını önden değerlendirir: koşullu yapılandırmaları çalıştırır, hangi Spring'in yönettiği nesnelerin (bean) oluşacağını çözer ve bu kararları doğrudan üretilmiş Java koduna döker. **GraalVM native derlemesi** ise ayrı bir adımdır; üretilen kodu ve erişilebilirlik meta verisini kullanarak ulaşılabilir tüm kodu tek bir yerel ikiliye derler (`92`). B şıkkı bu iki aşamayı birbirine karıştırır. Kazanç şudur: normalde açılışta yapılan tarama ve karar işi derleme zamanına taşınır, uygulama daha az iş yaparak açılır. İnce nokta da buradadır — bu kazanç yalnız native'e özgü değildir, JVM üzerinde de görülür. AOT işlemesi ayrıca yansıma ve vekil nesne ihtiyaçlarını meta veriyle bildirir; native derlemeyi pratikte mümkün kılan parça budur (`66`). Bedeli de vardır: kararlar build anında donar, çalışma anında değişen dinamik yapılandırma daralır. A şıkkı AOT'u küçültme aracı, C şıkkı JIT anahtarı sanır; ikisi de yanlıştır.
