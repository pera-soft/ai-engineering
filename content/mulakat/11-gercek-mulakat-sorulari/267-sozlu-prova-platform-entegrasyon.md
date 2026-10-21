---
slug: sozlu-prova-platform-entegrasyon
baslik: "Sözlü Prova · Platform, Entegrasyon ve Gözlemlenebilirlik: Dayanıklılık, Araç Seçimi, Üç Sütun ve Sözleşme"
kademe: İleri
sira: 267
etiketler: 11
---

## Konu anlatımı

Bu dosyada altı gerçek mülakat sorusu var. Altısı da açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı. Senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur. Altı soru tek bir eksende ilerler: sistemi ayakta tutan şey nedir, onu nasıl görürsün, sözleşmeyi kırmadan nasıl değiştirirsin. İlk üç soru platformun kendisiyle ilgilidir. Son üç soru değişimin yönetimiyle ilgilidir.

### "Orta katman mimarinizde ölçeklenebilirlik ve dayanıklılığı artırmak için hangi tasarım ilkelerini uygularsınız?"

**Soru aslında ne ölçüyor?**

Bu soru bir ilke listesi istiyor gibi görünür ama aslında sıralama ister. Görüşmeci şuna bakar: aday ölçeklenebilirlik ile dayanıklılığı ayırabiliyor mu. İkisi aynı şey değildir. Ölçeklenebilirlik yükü karşılamakla ilgilidir, dayanıklılık ise yük karşılanamadığında ne olacağıyla. Zayıf aday yalnız "kopya sayısını artırırım" der. Güçlü aday kopya artırmanın önkoşulunu söyler ve sistem doyduğunda ne yapacağını anlatır. Ağırlık muhakemededir; deneyim varsa cevabı iki kat güçlendirir.

**Sözlü cevap iskeleti**

1. **Önkoşulu ilk cümlede söyle.** Yatay ölçek durumsuzluk (stateless) ister. Kopya kendi belleğinde oturum tutuyorsa kopya eklemek işe yaramaz.
2. **Durumu nereye taşıdığını anlat.** Oturum paylaşılan bir depoya ya da imzalı jetona gider. Yapışkan oturum bir çözüm değil, ertelemedir (`106`).
3. **Zaman aşımı bütçesini kur.** Her dış çağrının süresi bellidir ve toplamı üst sınırı aşmaz. Bütçe yoksa iş parçacıkları (thread) yavaş bir servisin arkasında birikir (`101`).
4. **İzolasyonu ekle.** Bölme duvarı (bulkhead) her bağımlılığa ayrı havuz verir. Böylece tek bir yavaş servis tüm uygulamayı düşürmez; devre kesici (circuit breaker) de bu havuzun sinyaliyle çalışır.
5. **Geri basıncı (backpressure) anlat.** Sınırsız kuyruk gecikmeyi gizler ve sonunda belleği yer. Kuyruk sınırlanır, aşan istek hızlıca reddedilir; hızlı ret sessiz beklemeden iyidir.
6. **Kapanışı ve probları söyle.** Zarif kapanış (graceful shutdown) yeni isteği almaz, akandaki işi bitirir. Hazırlık ve canlılık probları ayrı şeylerdir; ölçekleme kararı buna dayanır (`218`).
7. **Hedefi ölçütle bağla.** Ölçeklenebilirlik bir his değildir. Hedef p95 ve p99 ile yazılır, sonra yük altında doğrulanır (`151`).

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Dayaniklilik ilkeleri: durumsuz kopya, izolasyon, yuk denetimi, zaman asimi butcesi ve zarif kapanis">
  <defs>
    <marker id="sozlu6-dayanik" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önce durumu çıkar, sonra izole et, en son yükü sınırla</text>
  <rect x="14" y="36" width="196" height="126" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Durumsuz kopya</text>
  <text x="112" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">oturum bellekte tutulmaz</text>
  <text x="112" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">paylaşılan depo ya da jeton</text>
  <text x="112" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yapışkan oturum bir çözüm değil</text>
  <text x="112" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kazanç: kopya serbestçe eklenir</text>
  <line x1="212" y1="99" x2="228" y2="99" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu6-dayanik)"/>
  <rect x="232" y="36" width="196" height="126" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · İzolasyon</text>
  <text x="330" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bağımlılık başına ayrı havuz</text>
  <text x="330" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bölme duvarı (bulkhead)</text>
  <text x="330" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">devre kesici ve yedek davranış</text>
  <text x="330" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kazanç: arıza tek alanda kalır</text>
  <line x1="430" y1="99" x2="446" y2="99" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu6-dayanik)"/>
  <rect x="450" y="36" width="196" height="126" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Yük denetimi</text>
  <text x="548" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kuyruk sınırlı tutulur</text>
  <text x="548" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geri basınç (backpressure)</text>
  <text x="548" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hızlı ret, sessiz beklemeden iyi</text>
  <text x="548" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kazanç: gecikme gizlenmez</text>
  <rect x="14" y="176" width="316" height="110" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Zaman aşımı bütçesi olmazsa</text>
  <text x="172" y="220" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yavaş bir bağımlılık havuzu doldurur</text>
  <text x="172" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş parçacıkları beklemede birikir</text>
  <text x="172" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ilgisiz uç noktalar da yanıt vermez</text>
  <text x="172" y="278" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek yavaşlık tüm uygulamayı düşürür</text>
  <rect x="344" y="176" width="302" height="110" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="495" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kapanış ve problar</text>
  <text x="495" y="220" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zarif kapanış: yeni istek alma, işi bitir</text>
  <text x="495" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hazırlık probu trafiği açar ya da keser</text>
  <text x="495" y="260" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">canlılık probu yeniden başlatmayı tetikler</text>
  <text x="495" y="278" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">hedef p95 ve p99 ile yazılır, yükte doğrulanır</text>
</svg>
```

**Elenme sebepleri**

- Yalnız "kopya sayısını artırırım" demek. Durumsuzluktan söz etmeyen aday önkoşulu atlamış olur.
- Yapışkan oturumu kalıcı çözüm sanmak. Kopya düştüğünde oturum yine kaybolur, sorun geri gelir.
- Zaman aşımı vermemek. Süresiz bekleyen bir çağrı, tüm uygulamayı tek bağımlılığa bağlar.
- Kuyruğu sınırsız bırakmak. Sınırsız kuyruk gecikmeyi gizler, sonra belleği tüketir ve süreci düşürür.
- Devre kesiciyi ölçütsüz anlatmak. Hangi eşikte açılacağı ve kapalıyken ne döneceği söylenmezse cevap eksiktir.
- Zarif kapanışı hiç anmamak. Dağıtım sırasında düşen istekler çoğu ekipte fark edilmeden birikir.
- Hedefi sayıyla söylememek. "Daha ölçeklenebilir olur" cümlesi bir tasarım kararı değil, bir dilektir.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Çalıştığın uygulama kaç kopya çalışıyor? Kopya sayısı elle mi değişiyor, otomatik mi?
- Uygulamanın belleğinde tutulan bir durum var mı? Oturum, önbellek, sayaç ya da zamanlanmış iş kilidi?
- Hiç yavaş bir dış servis yüzünden ilgisiz bir uç nokta yanıt vermez oldu mu?
- Dış çağrılarda zaman aşımı tanımlı mı? Değeri nereden geldi, ölçümle mi seçildi?
- Dağıtım sırasında hata oranında sıçrama görülüyor mu? Zarif kapanış açık mı?
- Bir yük testi yapıldı mı? Hangi eşikte ne bozuldu: yanıt süresi mi, bellek mi, bağlantı havuzu mu?
- Sonuç nasıl ölçüldü? p95 kaça indi, hata oranı ne oldu, kaç kopyayla karşılandı?

Böyle bir deneyimin yoksa uydurma; bunun yerine `256`'daki dürüst köprüyü kur. Tek kopyalı bir uygulamada zaman aşımı ve yeniden deneme ayarlamış olmak da gerçek bir temeldir. Onu anlat, sonra "çok kopyada şu değişir" diyerek muhakemeni göster.

### "API ağ geçidi, servis keşfi ve yapılandırma yönetiminde hangi araçları seçer, nasıl gerekçelendirirsiniz?"

**Soru aslında ne ölçüyor?**

Bu soru araç adı istiyor gibi durur ama asıl ölçtüğü şey gerekçedir. Marka listesi sayan aday sıradan görünür. Ayrışan aday önce ihtiyacı sorar, sonra platformun zaten ne verdiğine bakar. Kubernetes gibi bir platform servis keşfini kendisi sağlıyorsa ayrı bir keşif aracı ikinci bir gerçeklik üretir. Görüşmecinin aradığı cümle şudur: bu aracın işletme maliyetini kim taşıyacak. Ağırlık muhakemededir; ürün bilgisi ikinci sıradadır.

**Sözlü cevap iskeleti**

1. **Üç ihtiyacı ayır.** Ağ geçidi kenar sorumluluğunu üstlenir. Keşif adres bulmayı çözer. Yapılandırma yönetimi ayarların koddan ayrılmasını sağlar. Üçü farklı sorunlardır.
2. **Ağ geçidinin işini sınırla.** Yönlendirme, kimlik doğrulama, hız sınırlama, gövde boyutu ve gözlemlenebilirlik kancaları. İş kuralı ağ geçidine girerse orası yeni bir monolit olur (`99`).
3. **Keşfi platformdan iste.** Kubernetes ortamında servis nesnesi ve küme içi DNS keşfi zaten verir. Ayrı bir kayıt defteri (registry) yalnız platform vermiyorsa gerekir (`100`, `218`).
4. **Seçim ölçütünü açıkça say.** Ekibin bildiği teknoloji, işletme maliyeti, platformun hazır verdiği, satıcıya bağlanma riski ve topluluk desteği. Bu beş ölçüt cevabın omurgasıdır (`260`).
5. **Yapılandırmayı koddan ayır.** Ortama göre değişen her şey dışarıdan gelir. Ortam değişkeni, yapılandırma sunucusu ya da platformun kendi nesneleri kullanılır (`102`, `106`).
6. **Sırları ayrı ele al.** Parola ve anahtar yapılandırmayla aynı yerde durmaz. Sır kasası kullanılır, erişim yetkiye bağlanır, değer asla depoya yazılmaz.
7. **Değişikliğin izini bırak.** Yapılandırma da bir dağıtımdır. Kim ne zaman neyi değiştirdi sorusunun cevabı olmalıdır; sürümlenmeyen ayar en sinsi kesinti kaynağıdır.

**Elenme sebepleri**

- Yalnız ürün adı saymak. Gerekçe verilmeden söylenen araç listesi ezber olarak duyulur.
- Platformun verdiğini görmezden gelmek. Küme zaten keşif veriyorsa ayrı kayıt defteri gereksiz bir bileşendir.
- İşletme maliyetini hiç anmamak. Her yeni bileşenin yedeklemesi, sürüm yükseltmesi ve izlemesi vardır.
- Ağ geçidine iş kuralı koymak. Kenar bileşeni büyüdükçe tüm ekipler onun sırasında bekler.
- Yapılandırmayı imajın içine gömmek. Ortam değiştiğinde yeniden derleme gerekir; bu on iki faktör ilkesine aykırıdır.
- Sırları yapılandırmayla aynı sepete koymak. Depoya düşen tek bir anahtar tüm ortamı riske atar.
- Satıcıya bağlanma riskini konuşmamak. Geçiş maliyeti seçim anında ölçülmezse sonradan hiç ölçülmez.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Şu anki sistemde önde bir ağ geçidi var mı? Hangi işleri üstleniyor, hangilerini üstlenmiyor?
- Servisler birbirini nasıl buluyor? Sabit adres mi, DNS mi, kayıt defteri mi?
- Yapılandırma nereden geliyor? Dosya mı, ortam değişkeni mi, merkezi bir sunucu mu?
- Ayar değişikliği yeniden başlatma gerektiriyor mu? Gerekiyorsa bunun bir maliyeti oldu mu?
- Sırlar nerede duruyor? Kasa var mı, yoksa yapılandırma dosyasında mı bekliyor?
- Hiç yanlış ortam ayarı yüzünden olay yaşandı mı? Nasıl fark edildi, ne kadar sürdü?
- Bir araç seçimine katıldın mı? Hangi seçenekler vardı, karar hangi ölçütle verildi?

Deneyimin yoksa uydurma. `256`'daki köprü burada da işler. Küçük bir projede yapılandırmayı ortam değişkenine taşımış olmak bile anlatılabilir bir karardır. Onu abartmadan söyle, ölçütü kendi cümlenle ver.

### "Mikroservislerde gözlemlenebilirlik için log, metrik ve dağıtık izleme stratejinizi nasıl kurgularsınız?"

**Soru aslında ne ölçüyor?**

Üç sütunun adını herkes sayar; bu soruda ayrım orada kurulmaz. Görüşmecinin baktığı şey şudur: aday hangi sorunun hangi sütunla cevaplandığını biliyor mu. Metrik "kötü mü" sorusunu cevaplar, iz "nerede" sorusunu, log "tam olarak ne oldu" sorusunu. Bu eşleşmeyi kuran aday hemen ayrışır. İkinci ayrım maliyet tarafındadır. Gözlemlenebilirlik bedava değildir; etiket çeşitliliği ve log hacmi bütçelenmezse sistem kendi ağırlığında çöker.

**Sözlü cevap iskeleti**

1. **Eşleşmeyi ilk cümlede kur.** Metrik kötü olup olmadığını söyler. İz nerede olduğunu gösterir. Log ne olduğunu anlatır (`193`).
2. **Logu yapılandırılmış yaz.** Serbest metin aranamaz. Anahtar-değer biçimi, sabit alan adları ve kişisel verinin maskelenmesi şarttır (`104`).
3. **Bağlayıcıyı koy.** Aynı isteğin izi, logu ve metriği bir correlation-id ile birleşir. Bu kimlik ağ geçidinde üretilir, tüm servislere taşınır (`103`).
4. **Metrikte RED üçlüsünü ver.** İstek sayısı, hata sayısı, süre dağılımı. Kaynak metrikleri bunların yanına gelir, yerine değil (`89`).
5. **Etiket çeşitliliğini bütçele.** Her benzersiz etiket değeri ayrı bir zaman serisi üretir. Kullanıcı kimliği metrik etiketi olmaz; o bilgi log ve iz tarafında durur.
6. **Örneklemeyi konuş.** İzin tamamını saklamak pahalıdır. Hatalı ve yavaş istekler her zaman saklanır, kalanı örneklenir.
7. **Alarmı belirtiye değil hedefe bağla.** Alarm SLO ihlaline yaklaşınca çalar. İşlemci yüzde seksen oldu diye çalan alarm, birkaç hafta içinde susturulur (`151`).

```svg
<svg viewBox="0 0 660 312" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Gozlemlenebilirlik uc sutun: metrik nerede, iz hangi adim, log tam olarak ne oldu">
  <defs>
    <marker id="sozlu6-gozlem" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her sütun ayrı bir soruyu cevaplar; sıra soruyu daraltır</text>
  <rect x="14" y="36" width="196" height="134" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Metrik</text>
  <text x="112" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">durum kötü mü?</text>
  <text x="112" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istek, hata, süre dağılımı</text>
  <text x="112" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ucuz, sürekli, toplanmış</text>
  <text x="112" y="142" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sınırı: tek isteği anlatamaz</text>
  <text x="112" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alarm buradan doğar</text>
  <line x1="212" y1="103" x2="228" y2="103" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu6-gozlem)"/>
  <rect x="232" y="36" width="196" height="134" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İz (trace)</text>
  <text x="330" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">nerede yavaşladı?</text>
  <text x="330" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">servis servis süre dağılımı</text>
  <text x="330" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bağımlılık çağrı ağacı</text>
  <text x="330" y="142" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sınırı: hepsini saklamak pahalı</text>
  <text x="330" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">hatalı ve yavaş istek hep saklanır</text>
  <line x1="430" y1="103" x2="446" y2="103" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu6-gozlem)"/>
  <rect x="450" y="36" width="196" height="134" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Log</text>
  <text x="548" y="78" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">tam olarak ne oldu?</text>
  <text x="548" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yapılandırılmış, sabit alanlı</text>
  <text x="548" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kişisel veri maskelenir</text>
  <text x="548" y="142" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sınırı: hacmi ve saklama maliyeti</text>
  <text x="548" y="160" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">serbest metin aranamaz</text>
  <rect x="14" y="186" width="316" height="112" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="172" y="207" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Üçünü birleştiren şey: correlation-id</text>
  <text x="172" y="230" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kimlik ağ geçidinde üretilir</text>
  <text x="172" y="250" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her çağrıda başlıkla taşınır</text>
  <text x="172" y="270" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">log satırına ve ize aynı alan yazılır</text>
  <text x="172" y="289" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">bağ yoksa üç sütun üç ayrı ada kalır</text>
  <rect x="344" y="186" width="302" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="207" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Maliyet: etiket çeşitliliği</text>
  <text x="495" y="230" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her benzersiz değer ayrı zaman serisidir</text>
  <text x="495" y="250" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kullanıcı kimliği metrik etiketi olmaz</text>
  <text x="495" y="270" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">o bilgi log ve iz tarafında durur</text>
  <text x="495" y="289" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alarm SLO ihlaline bağlanır, eşiğe değil</text>
</svg>
```

**Elenme sebepleri**

- Üç sütunu sayıp orada durmak. Hangi sorunun hangisiyle cevaplandığını söylemeyen cevap ezber sayılır.
- Correlation-id'den hiç söz etmemek. Bağ kurulmayan üç sistem, olay anında üç ayrı arama demektir.
- Serbest metin log yazmak. Aranamayan log, kesinti anında en çok zaman kaybettiren şeydir.
- Kullanıcı kimliğini metrik etiketi yapmak. Zaman serisi sayısı patlar, sorgular yavaşlar, maliyet tırmanır.
- Her şeyi loglamak. Sınırsız hacim hem pahalıdır hem de asıl satırı gürültüde kaybeder.
- Alarmı kaynak eşiğine bağlamak. Kullanıcıyı etkilemeyen alarm zamanla susturulur ve gerçek olay kaçar.
- Kişisel veriyi maskelemeden yazmak. Bu bir tasarım hatasıdır ve mülakatta güvenlik zaafı olarak okunur.

**Kendi hikâyen**

Bu soruda hikâye genelde bir kesinti anından gelir. Şu sorular onu çıkarır:

- Bir olayda cevabı hangi sütundan buldun? Metrikten mi, izden mi, logdan mı?
- Aynı isteği servisler arasında takip edebiliyor musun? Bunu sağlayan alan ne?
- Log biçiminiz nedir? Yapılandırılmış mı, yoksa satır satır serbest metin mi?
- Hiç metrik sistemi maliyeti ya da yavaşlığı konuşuldu mu? Sebep etiket çeşitliliği miydi?
- Kaç alarmınız var ve kaçı gerçekten harekete geçiriyor? Susturulan alarm var mı?
- Bir gösterge panosu kurdun mu? Üstünde hangi üç sayı duruyor?
- Bir olayda teşhis süresi ne kadardı? Sonrasında hangi kalıcı ölçüm eklendi?

Bu deneyim yoksa uydurma. `256`'daki köprüyü kur. Kendi projende bir uç noktaya süre metriği ve correlation-id ekleyip farkı görmek gerçek bir gözlemdir. Onu olduğu gibi anlat, üretim vakası gibi sunma.

### "Yüksek trafikte performans sorununu teşhis edip kalıcı çözüm üretme adımlarınız nedir?"

**Soru aslında ne ölçüyor?**

Bu soru yöntem sorusudur. Görüşmeci araç adı değil sıra arar. Zayıf aday doğrudan bir çözüme atlar; genellikle önbellek ya da sunucu ekleme önerir. Güçlü aday önce ölçer, sonra daraltır, en son değiştirir. Sorunun ikinci yarısı daha az adayın hazırlıklı geldiği yerdir: kalıcı çözüm ne demek. Sorunu geçici olarak kapatmak ile tekrarını engellemek farklı şeylerdir. Ağırlık yöntemdedir.

**Sözlü cevap iskeleti**

1. **Kuralı en başta koy.** Önce ölç, sonra değiştir. Ölçmeden yapılan iyileştirme tahmindir ve çoğu zaman yeni sorun üretir.
2. **Ölçütü tanımla.** Hangi uç nokta, hangi yük, hangi hedef. "Yavaş" bir duygudur; "p95 300 ms altında" bir ölçüttür (`152`).
3. **Sırayı anlat.** Metrik nerede yavaş olduğunu söyler. İz hangi adımda kaybedildiğini gösterir. Profilleyici hangi kodun ya da kilidin yediğini bulur (`64`).
4. **Ortalamayı reddet.** Ortalama dağılımı saklar. Şikâyet eden kullanıcı p99'dadır; hedefler yüzdelik dilimle yazılır.
5. **Sık nedenleri sırala.** Veri tabanı erişimi, N+1 getirme, kilit ve havuz çekişmesi, sıcak anahtar üzerinde yığılma (`166`).
6. **Tek değişken kuralını uygula.** Bir seferde tek değişiklik yapılır, sonra aynı ölçüm tekrarlanır. İki değişiklik birlikte yapılırsa hangisinin işe yaradığı bilinmez.
7. **Kalıcılığı tanımla.** Kalıcı çözüm iki şeyle kilitlenir: nöbeti uyandıran bir alarm ve gerilemeyi yakalayan bir test. İkisi yoksa aynı sorun birkaç ay sonra geri gelir.

**Elenme sebepleri**

- Doğrudan çözüme atlamak. "Önbellek koyarım" cevabı ölçmeden karar veren bir mühendis izlenimi bırakır.
- Yalnız ortalamadan konuşmak. Yüzdelik dilimi anmayan aday kuyruk davranışını görmüyor demektir.
- Sunucu büyütmeyi ilk seçenek yapmak. Kaynak eklemek belirtiyi öteler ve maliyeti kalıcılaştırır.
- Aynı anda birkaç değişikliği birden yapmak. Sonuç iyileşse bile hangisinin etkili olduğu bilinmez.
- Üretim ile yerel ortamı aynı saymak. Veri kümesi ve eşzamanlılık farkı teşhisi tamamen değiştirir.
- Kalıcı adımı söylememek. Alarm ya da test eklenmezse aynı sorun sessizce geri döner.
- Sonucu sayı ile vermemek. "Belirgin biçimde hızlandı" cümlesi ölçüm değil, izlenimdir.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası iki sayıdır: önce ne, sonra ne. Onları bul:

- Hangi uygulamada yoğun trafik konuşuldu? Kampanya, gün sonu ya da toplu iş penceresi var mıydı?
- Belirti neydi? Yanıt süresinde tırmanma mı, zaman aşımı mı, kuyruk birikmesi mi?
- Nasıl fark edildi? Alarm mı, gösterge panosu mu, kullanıcı şikâyeti mi?
- Neye hangi sırayla baktın? Hangi adım seni asıl yere götürdü?
- Kök neden neydi? Eksik indeks, N+1, kilit çekişmesi, küçük havuz, yavaş dış servis?
- Neyi düzelttin ve neyi ertelemeye karar verdin? Erteleme gerekçen güçlü bir cümledir.
- Sonrasında ne eklendi? Yeni bir alarm mı, yük testi mi, gerileme testi mi?

Üretimde böyle bir vakan yoksa uydurma. Bu soruda gerçek gözlem üretmek kolaydır. Kendi projende bir uç noktaya yük ver, p95 değerini ölç, tek bir değişiklik yap ve tekrar ölç. Gördüğün fark gerçek bir ölçümdür ve `256`'daki köprüyle dürüstçe anlatılır.

### "Versiyonlama, geriye uyumluluk ve sözleşme testleriyle entegrasyon kırılmalarını nasıl önlersiniz?"

**Soru aslında ne ölçüyor?**

Bu dosyanın en ayrıştırıcı sorusu budur. Çoğu aday "v1, v2 açarım" der ve orada durur. Oysa asıl mesele sürüm numarası değil, geçiş planıdır. Görüşmeci üç şeye bakar. Birincisi, aday hangi değişikliğin kırıcı olduğunu sayabiliyor mu. İkincisi, istemciler aynı anda güncellenemediğinde ne yaptığını biliyor mu. Üçüncüsü, uyumu bir alışkanlığa değil bir teste bağlıyor mu. Ağırlık muhakemededir.

**Sözlü cevap iskeleti**

1. **Kırıcı olanı ayır.** Alan eklemek genellikle kırmaz. Alan silmek, adını değiştirmek, türünü daraltmak ve zorunlu alan eklemek kırar (`154`).
2. **Toleranslı okuyucuyu şart koş.** İstemci bilmediği alanı görmezden gelir, alan sırasına bağlanmaz. Bu tek alışkanlık kırılmaların çoğunu önler.
3. **Genişlet-ve-daralt planını anlat.** Önce yeni alanı ekle ve eskisini koru. Sonra istemcileri taşı. En sonda eskisini kaldır (`117`).
4. **Sürümleme stratejisini gerekçelendir.** Ana sürüm pahalıdır; iki hattı birlikte işletmek gerekir. Bu yüzden ana sürüm son çaredir, ilk hamle değil (`98`).
5. **Sözleşme testini yerleştir.** Tüketici beklentisini yazar, sağlayıcı bunu kendi hattında doğrular. Böylece kırılma üretimde değil derleme aşamasında görülür (`213`).
6. **Şema kayıt defterini olayların tarafına koy.** Mesaj tabanlı akışta uyumluluk kuralı merkezî olarak zorlanır. Uyumsuz şema yayına hiç çıkmaz.
7. **Kaldırma tarihini yaz.** Eski alanın ne zaman kalkacağı duyurulur ve kullanımı ölçülür. Kullanımı sıfırlanmadan kaldırma yapılmaz.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Genislet ve daralt: once yeni alan eklenir, istemciler tasinir, en son eski alan kaldirilir">
  <defs>
    <marker id="sozlu6-surum" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Genişlet-ve-daralt: kesinti üretmeden alan değiştirme</text>
  <rect x="14" y="36" width="196" height="128" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">1 · Genişlet</text>
  <text x="112" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni alan eklenir</text>
  <text x="112" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski alan aynen durur</text>
  <text x="112" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ikisi birlikte doldurulur</text>
  <text x="112" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">eski istemci hiç etkilenmez</text>
  <line x1="212" y1="99" x2="228" y2="99" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu6-surum)"/>
  <rect x="232" y="36" width="196" height="128" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Taşı</text>
  <text x="330" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemciler yeni alana geçer</text>
  <text x="330" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski alanın kullanımı ölçülür</text>
  <text x="330" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kaldırma tarihi duyurulur</text>
  <text x="330" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">geçiş süresi burada geçer</text>
  <line x1="430" y1="99" x2="446" y2="99" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu6-surum)"/>
  <rect x="450" y="36" width="196" height="128" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Daralt</text>
  <text x="548" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kullanım sıfırlanınca kaldır</text>
  <text x="548" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eski alan sözleşmeden çıkar</text>
  <text x="548" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sözleşme testi güncellenir</text>
  <text x="548" y="145" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ana sürüm açmaya gerek kalmaz</text>
  <rect x="14" y="180" width="316" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="201" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kırıcı değişiklikler</text>
  <text x="172" y="224" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alan silmek ya da adını değiştirmek</text>
  <text x="172" y="244" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">türü daraltmak, zorunlu alan eklemek</text>
  <text x="172" y="264" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hata biçimini ya da durum kodunu değiştirmek</text>
  <text x="172" y="283" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">alan eklemek genellikle kırmaz</text>
  <rect x="344" y="180" width="302" height="112" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="495" y="201" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Uyumu alışkanlığa değil teste bağla</text>
  <text x="495" y="224" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">toleranslı okuyucu: bilinmeyen alanı yok say</text>
  <text x="495" y="244" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sözleşme testi tüketici beklentisini yazar</text>
  <text x="495" y="264" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sağlayıcı bunu kendi hattında doğrular</text>
  <text x="495" y="283" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">olay akışında şema kayıt defteri aynı işi yapar</text>
</svg>
```

**Elenme sebepleri**

- Sürümlemeyi tek çözüm sanmak. "v2 açarım" cevabı geçiş planını ve iki hattın maliyetini hiç konuşmaz.
- Kırıcı değişikliği tanımlayamamak. Hangi değişikliğin neden kırdığı söylenmezse cevabın dayanağı kalmaz.
- Eski alanı duyuruyla kaldırmak. Duyuru bir güvence değildir; kullanım ölçülmeden kaldırma yapılmaz.
- İstemcilerin aynı anda güncelleneceğini varsaymak. Mobil ve dış ortaklarda bu varsayım hiç tutmaz.
- Sözleşme testinden hiç söz etmemek. Uyum insana bırakılırsa kırılma üretimde görülür.
- Ağ geçidinde yanıt yeniden yazarak sorunu gizlemek. Sözleşme borcu büyür ve kenar bileşenine iş kuralı sızar.
- Olay akışında şema uyumunu konuşmamak. Kuyruğa düşen uyumsuz mesaj tüketicileri tek tek durdurur.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir. Şu sorular onu çıkarır:

- Hiç bir API değişikliği bir istemciyi kırdı mı? Ne değişmişti: alan adı mı, tür mü, zorunluluk mu?
- Nasıl fark edildi? Test mi yakaladı, üretimde hata mı çıktı, ortak ekip mi haber verdi?
- Kaç istemcin vardı ve hepsini aynı anda güncelleyebiliyor muydun?
- Sürümleme yapıyor musunuz? Yapıyorsanız kaç hat aynı anda ayakta duruyor?
- Eski bir alanı hiç kaldırdın mı? Kaldırmadan önce kullanımını ölçtün mü?
- Sözleşme testi ya da şema kaydı var mı? Yoksa uyumu ne sağlıyor?
- Kırılma sonrası kalıcı olarak ne eklendi? Test mi, gözden geçirme kuralı mı, kaldırma takvimi mi?

Bu deneyim yoksa uydurma; burası uydurmanın çabuk yakalandığı sorulardan biridir. `256`'daki köprüyü kur. Tek istemcili bir API'de alan eklemiş olmak da bir temeldir. Ondan sonra "çok istemcide şu değişir" diyerek genişlet-ve-daralt planını anlat.

### "Platform ekibiyle geliştirici ekipler arasında öncelik, SLA ve teknik borç dengesini nasıl kurarsınız?"

**Soru aslında ne ölçüyor?**

Bu soru teknik değil, işbirliği sorusudur; ama teknik cevap ister. Görüşmeci adayın platformu nasıl gördüğüne bakar. Zayıf cevap platformu bir bilet kuyruğu gibi anlatır. Güçlü cevap platformu bir ürün gibi ele alır; kullanıcısı geliştirici ekiplerdir. İkinci ayrım dildedir. Öncelik tartışması sezgiyle yapılırsa her istek acil olur. Ölçütle yapılırsa tartışma kısalır. Sen proje yöneticisi değilsin; cevabı mühendis gözünden kur.

**Sözlü cevap iskeleti**

1. **Platformu ürün gibi tanımla.** Kullanıcısı geliştirici ekiplerdir. Talep toplanır, öncelik konur, kullanım ölçülür (`191`).
2. **Altın yolu (golden path) anlat.** Yeni servis için hazır şablon, hat, izleme ve dağıtım. Ekipler bunu kullandığında hem hızlanır hem standart kendiliğinden oturur.
3. **Sapmayı yasaklama, maliyetini göster.** Altın yolun dışına çıkan ekip bunu yapabilir. Karşılığında bakımını da üstlenir; kural bu şekilde kendini taşır.
4. **SLA yerine SLO ile konuş.** Hedef ölçülebilir olmalı ve gerçek kullanıma dayanmalı. Ulaşılamayan bir taahhüt kimseyi korumaz (`151`).
5. **Hata bütçesini ölçüt yap.** Bütçe varken yeni özellik önceliklidir. Bütçe tükendiğinde öncelik dayanıklılığa kayar. Böylece tartışma sayıya taşınır.
6. **Teknik borcu iş diline çevir.** Borç "kod çirkin" değildir. Yavaşlayan teslimat, artan olay sayısı ve uzayan kurtarma süresidir (`195`).
7. **Borca sabit pay ayır.** Her dönemde küçük ve düzenli bir pay ayrılır. Büyük yeniden yazım sözü genellikle tutulmaz; küçük ve sürekli iyileştirme tutar.

**Elenme sebepleri**

- Platformu yalnız bilet kuyruğu gibi anlatmak. Ürün bakışı yoksa öncelik hep sesi yüksek olana gider.
- Her isteği acil kabul etmek. Ölçüt konmazsa sıralama yapılamaz ve platform ekibi tükenir.
- SLA sayısını gerçek ölçüme dayandırmamak. Ulaşılamayan taahhüt ilk olayda güveni bitirir.
- Hata bütçesini yetki aracı sanmak. Bütçe reddetme hakkı değil, ortak ve önceden anlaşılmış bir eşiktir.
- Teknik borcu teknik dille savunmak. İş tarafı "kod kalitesi" cümlesini önceliğe çeviremez.
- Borcu tek büyük yeniden yazıma bağlamak. Uzun ve sonucu belirsiz bir iş çoğu zaman yarıda kalır.
- Altın yolu zorunlu kılıp gerekçe vermemek. Faydası anlatılmayan standart, ekipler tarafından delinir.

**Kendi hikâyen**

Bu soruda hikâye küçük ve gerçek olmalı. Şu sorular onu çıkarır:

- Şirkette ayrı bir platform ya da altyapı ekibi var mı? Talepler onlara nasıl gidiyor?
- Yeni bir servis açmak ne kadar sürüyor? Hazır bir şablon ya da hat var mı?
- Hiç bekleyen bir platform işi yüzünden iş kalemin gecikti mi? Nasıl çözüldü?
- Yazılı bir hedef var mı? Kullanılabilirlik ya da yanıt süresi için sayı konuşuluyor mu?
- Teknik borç nerede duruyor? Backlog'da görünür bir kalem mi, yoksa sohbet konusu mu?
- Borç için hiç zaman ayrıldı mı? Ayrıldıysa nasıl gerekçelendirildi?
- Bir iyileştirmenin etkisini ölçebildin mi? Dağıtım süresi, olay sayısı ya da kurtarma süresi değişti mi?

Böyle bir deneyimin yoksa uydurma. `256`'daki köprü burada işini görür. Kendi ekibinde küçük bir tekrarlayan işi otomatikleştirmiş olmak da anlatılabilir bir örnektir. Kazanılan zamanı söyleyebiliyorsan cevap gerçek bir ölçüme oturur.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Önce iskeleti okuma. Soruyu yüksek sesle oku, sonra dosyayı kapat.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek prova sayılmaz; ağız ile beyin aynı hızda çalışmaz.
- Sonra iskeletle karşılaştır. Hangi adımı atladın, işaretle.
- Bu altı soruda en sık atlanan adım maliyet cümlesidir. Seçtiğin aracın işletme yükünü söylemezsen cevap reklam gibi duyulur.
- İkinci sırada atlanan şey ölçüttür. "Daha iyi olur" yerine hangi sayının ne olacağını söyle.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- Altı soruyu üst üste yapma. İkişerli çalış ve arada ara ver; yorgun ses ikna etmez.
- Ölçüt şudur: bir sorunun adımlarını kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Tek cümlede

Bu altı soru araç bilgisini değil, platform kararını anlatma becerisini ölçer. Birincisinde kazanan cümle en baştadır: yatay ölçek durumsuzluk ister. İkincisinde araç adı değil seçim ölçütü konuşulur; platformun zaten verdiği şey ikinci kez satın alınmaz. Üçüncüsünde ayrım üç sütunu saymakta değil, hangi sorunun hangisiyle cevaplandığını söylemektedir. Dördüncüsünde yöntem sırası her araç listesinden güçlüdür: metrik, iz, profilleyici. Beşincisinde sürüm numarası değil geçiş planı ayrıştırır; genişlet, taşı, daralt. Altıncısında tartışma sezgiden ölçüte taşınır; hata bütçesi ve görünür teknik borç bunu yapar. Altısında da aynı disiplin geçerlidir: ihtiyacı söyle, ölçütü söyle, maliyeti söyle, ölçülen sonucu söyle. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# sozlu6-1 | Orta
## Soru
Bir Spring Boot servisi oturum bilgisini kendi belleğinde tutuyor. Servis iki kopyaya çıkarılınca kullanıcılar rastgele oturum düşmesi yaşıyor. En yerinde çözüm hangisidir?
## A
Yük dengeleyicide yapışkan oturum açmak; her istek hep aynı kopyaya yönlendirilir
🤖 belirtiyi gizler; kopya düştüğünde ya da yeniden dağıtımda oturum yine kaybolur.
## B
Kopya sayısını bire indirip yalnız dikey ölçekleme yapmak; tek kopyada bu sorun görülmez
🤖 yatay ölçeği tamamen bırakır; tek kopya aynı zamanda tek arıza noktasıdır.
## C
Oturum durumunu servisten çıkarmak; paylaşılan bir depoya ya da imzalı jetona taşımak
🤖 Doğru: durumsuz kopya serbestçe eklenip çıkarılabilir.
## D
Kopyalar arasında oturumları çoğaltmak; her kopya diğerinin oturum verisini kendine kopyalar
🤖 kopya sayısı arttıkça çoğaltma maliyeti ve tutarsızlık penceresi büyür.
## Doğru
C
## Açıklama
Yatay ölçeğin önkoşulu **durumsuzluktur** (stateless). Kopya kendi belleğinde oturum tutuyorsa, ikinci kopyaya düşen istek o oturumu bulamaz. Sorun kopya sayısında değil, durumun yerindedir. Doğru hamle durumu servisin dışına almaktır: paylaşılan bir oturum deposu ya da istemcide taşınan imzalı bir jeton. Böylece her kopya her isteği karşılayabilir; kopya eklemek, çıkarmak ve yeniden başlatmak ucuzlar (`106`). A şıkkındaki yapışkan oturum yalnız erteler. Kopya düştüğünde, yeniden dağıtımda ve otomatik ölçeklemede oturum yine kaybolur; ayrıca yük dağılımı bozulur. B şıkkı sorunu ölçeği bırakarak çözer ve dayanıklılığı da düşürür. D şıkkı çoğaltmayı önerir; kopya sayısı arttıkça ağ trafiği ve tutarsızlık artar, işletmesi zorlaşır. Durum çıkarıldıktan sonra sıradaki ilkeler gelir: zaman aşımı bütçesi, bölme duvarı, sınırlı kuyruk ve zarif kapanış (`101`, `218`).

# sozlu6-2 | İleri
## Soru
Ekip Kubernetes üzerinde çalışıyor ve servisler birbirini küme içi DNS ile buluyor. Bir geliştirici ayrıca bağımsız bir servis keşif kaydı (registry) kurmayı öneriyor. En yerinde değerlendirme hangisidir?
## A
Platform keşfi zaten sağlıyor; ayrı kayıt ikinci bir gerçeklik ve ek işletme yükü getirir
🤖 Doğru: aynı bilgiyi iki yerde tutmak keşfi çözmez, çoğaltır.
## B
Öneri doğrudur; istemci tarafı keşif her ortamda sunucu tarafı keşiften daha güvenilirdir
🤖 mutlak bir üstünlük yoktur; seçim platforma, ekibe ve maliyete bağlıdır.
## C
Öneri doğrudur; bağımsız kayıt olmadan servislerin sağlık denetimi hiçbir biçimde yapılamaz
🤖 sağlık denetimini platform hazırlık ve canlılık problarıyla yapar.
## D
Fark yoktur; ikisi de aynı bileşen olduğu için seçim tamamen ekibin zevkine kalmıştır
🤖 ikisi ayrı bileşendir; işletme, yedekleme ve sürüm maliyetleri de farklıdır.
## Doğru
A
## Açıklama
Araç seçiminde ilk soru şudur: bu ihtiyacı platform zaten karşılıyor mu. Kubernetes ortamında servis nesnesi ve küme içi DNS, adres bulma işini kutudan verir. Buna ek olarak bağımsız bir kayıt defteri kurmak **ikinci bir gerçeklik** üretir; iki kaynak uyuşmadığında hata ayıklamak zorlaşır. Yeni bileşenin görünmeyen bir faturası da vardır: yedekleme, sürüm yükseltme, izleme ve nöbet (`100`, `218`). B şıkkı istemci tarafı keşfe mutlak üstünlük atfeder; oysa seçim ölçütü ekip bilgisi, işletme maliyeti ve platformun hazır verdikleridir (`260`). C şıkkı sağlık denetimini yalnız kayıt defterine bağlar; platform bunu hazırlık ve canlılık problarıyla yapar. D şıkkı kararı zevke indirger. Aynı disiplin ağ geçidi ve yapılandırma için de geçerlidir: ağ geçidi kenar işleriyle sınırlı kalır, iş kuralı almaz (`99`); yapılandırma koddan ayrılır ve sırlar ayrı bir kasada durur (`102`).

# sozlu6-3 | İleri
## Soru
Bir ekip her HTTP isteği için metriğe `kullanici_id` etiketi ekledi. Birkaç gün sonra metrik sistemi aşırı bellek tüketiyor ve panolar yavaşlıyor. Temel sebep nedir?
## A
Toplama aralığı çok kısadır; aralık uzatılırsa bu etiketin herhangi bir maliyeti kalmaz
🤖 aralık maliyeti bir miktar azaltır ama seri sayısındaki patlamayı çözmez.
## B
Loglar metriklerle aynı sistemde tutuluyordur; ikisi ayrılırsa sorun kendiliğinden kalkar
🤖 sorun depolamanın yerinde değil, etiketin ürettiği seri sayısındadır.
## C
Metrik türü yanlış seçilmiştir; sayaç yerine histogram kullanılırsa kaynak tüketimi düşer
🤖 histogram genelde daha pahalıdır; tür seçimi burada asıl neden değildir.
## D
Her benzersiz etiket değeri ayrı bir zaman serisi üretir; kullanıcı sayısı kadar seri oluşur
🤖 Doğru: yüksek etiket çeşitliliği metrik sistemini şişirir.
## Doğru
D
## Açıklama
Metrik sistemleri veriyi **zaman serisi** olarak saklar. Bir serinin kimliği, metrik adı ile etiketlerinin birleşiminden doğar. `kullanici_id` etiketi eklendiğinde her kullanıcı için ayrı bir seri açılır. Yüz bin kullanıcı yüz bin seri demektir; bellek, disk ve sorgu süresi buna göre büyür. Buna yüksek etiket çeşitliliği denir ve gözlemlenebilirlik maliyetinin en sık kaçırılan kalemidir (`104`). A şıkkı aralığı suçlar; aralık örnek sayısını azaltır, seri sayısını değil. B şıkkı depolamanın yerini suçlar; oysa etiket ayrı bir sistemde de aynı patlamayı üretir. C şıkkı tür seçimini gösterir; histogram genelde daha pahalıdır ve buradaki nedeni açıklamaz. Doğru tasarım şudur: kullanıcıya özgü bilgi metriğe değil, log ve iz tarafına yazılır. Metrik "kötü mü" sorusunu cevaplar, iz "nerede" sorusunu, log "tam olarak ne oldu" sorusunu (`193`, `103`). Üçü correlation-id ile birleşir ve alarm SLO ihlaline bağlanır (`89`).

# sozlu6-4 | İleri
## Soru
Yoğun saatte bir uç noktanın p99 gecikmesi tırmanıyor, ortalama neredeyse sabit kalıyor. Elinde metrik, dağıtık iz ve profilleyici var. En yerinde sonraki adım hangisidir?
## A
Profilleyiciyi üretimde açıp en çok işlemci tüketen metodu bulmak ve orayı yeniden yazmak
🤖 arama alanı hâlâ çok geniş; zamanın hangi adımda kaybolduğu bilinmiyor.
## B
İzlere bakıp yavaş isteklerin hangi adımda zaman kaybettiğini bulmak
🤖 Doğru: metrikten sonraki adım aramayı tek servise daraltır.
## C
Bağlantı havuzunu ve iş parçacığı sayısını artırıp gecikmenin düşüp düşmediğine bakmak
🤖 ölçmeden yapılan ayar tahmindir; kuyruğu başka yere taşıyabilir.
## D
Ortalama sabit olduğu için sorunu istemci tarafına yazmak ve ölçümü kapatmak
🤖 ortalama dağılımı saklar; p99 gerçek kullanıcı deneyiminin ucudur.
## Doğru
B
## Açıklama
Teşhis, arama alanını kademe kademe daraltarak ilerler. Metrik zaten konuşmuş durumdadır: sorun belli bir uç noktada ve dağılımın ucundadır. Sıradaki adım **iz** (trace) incelemektir; yavaş isteklerin çağrı ağacı, zamanın hangi serviste ya da hangi bağımlılıkta kaybolduğunu gösterir. Ancak ondan sonra profilleyici anlamlı olur, çünkü artık tek bir çağrı yoluna bakılır (`152`, `64`). A şıkkı sırayı atlar; üretimde profilleyici açmak maliyetlidir ve geniş alanda yanıltır. C şıkkı ölçmeden ayar yapar; havuz büyütmek bazen işe yarar ama kuyruğu çoğu zaman bir sonraki darboğaza taşır. D şıkkı ortalamaya güvenir; oysa ortalama dağılımı saklar ve şikâyet eden kullanıcı tam olarak p99'dadır. Sık nedenler bellidir: veri tabanı erişimi, N+1 getirme, kilit ve havuz çekişmesi, sıcak anahtar üzerinde yığılma (`166`). Kalıcı çözüm iki şeyle kilitlenir: bir alarm ve gerilemeyi yakalayan bir test.

# sozlu6-5 | Uzman
## Soru
Bir REST yanıtındaki `adres` alanının adı `teslimatAdresi` olacak. Birden çok istemci var ve hepsi aynı anda güncellenemiyor. En güvenli yol hangisidir?
## A
Alanı tek dağıtımda yeniden adlandırmak ve istemcilere değişikliği önceden duyurmak
🤖 duyuru bir güvence değildir; geçmemiş istemci ilk dağıtımda kırılır.
## B
Yeni bir ana sürüm açıp tüm uç noktaları kopyalamak ve eski sürümü hemen kapatmak
🤖 tek alan için ana sürüm pahalıdır; hemen kapatmak yine kırar.
## C
Alan adını hiç değiştirmeyip yanıtı ağ geçidinde istemciye göre yeniden yazmak
🤖 sözleşme borcunu gizler; kenar bileşenine iş kuralı sızar.
## D
Önce iki alanı birlikte yayınlamak, istemciler geçtikten sonra eskisini kaldırmak
🤖 Doğru: genişlet-ve-daralt kesintisiz geçiş sağlar.
## Doğru
D
## Açıklama
Alan adını değiştirmek **kırıcı** bir değişikliktir; eski istemci beklediği alanı bulamaz. İstemciler aynı anda güncellenemiyorsa tek dağıtımda yapılan değişiklik kesinti üretir. Doğru desen **genişlet-ve-daralt**tır (expand-contract). Önce yeni alan eklenir ve eskisi aynen korunur; ikisi birlikte doldurulur. Sonra istemciler yeni alana taşınır ve eski alanın kullanımı ölçülür. Kullanım sıfırlanınca eski alan kaldırılır (`117`, `154`). Bu planı ayakta tutan iki alışkanlık vardır: toleranslı okuyucu, yani bilinmeyen alanı yok sayan istemci; ve tüketici beklentisini yazan sözleşme testi (`213`). A şıkkı duyuruyu güvence sanır. B şıkkı tek alan için ana sürüm açar; iki hattı birlikte işletmek pahalıdır ve hemen kapatmak aynı kırılmayı üretir (`98`). C şıkkı sorunu ağ geçidinde gizler; borç büyür ve kenar bileşeni istemciye özel mantık taşımaya başlar. Olay akışında aynı işi şema kayıt defteri yapar; uyumsuz şema yayına hiç çıkmaz.

# sozlu6-6 | Uzman
## Soru
Platform ekibi ile ürün ekipleri arasında sürekli öncelik çatışması var; gelen her istek "acil" diye işaretleniyor. Hata bütçesi (error budget) yaklaşımı bu tartışmayı nasıl değiştirir?
## A
Kalan bütçe sayısal bir ölçüt verir; bütçe tükendiğinde öncelik dayanıklılığa kayar
🤖 Doğru: tartışma sezgiden önceden anlaşılmış bir ölçüte taşınır.
## B
Hata bütçesi kesinti olasılığını doğrudan düşürür; böylece öncelik tartışması kendiliğinden biter
🤖 bütçe olasılığı düşürmez; yalnız kararın dilini değiştirir.
## C
Bütçe, platform ekibine ürün ekiplerinin taleplerini reddetme yetkisi verir
🤖 amaç yetki dağıtmak değil, ortak ve önceden konuşulmuş bir eşik kurmaktır.
## D
Bütçe yalnız yönetime raporlama içindir; günlük öncelik kararlarına girmesi beklenmez
🤖 bütçenin değeri tam olarak karar anında kullanılmasındadır.
## Doğru
A
## Açıklama
Hata bütçesi, hizmet seviyesi hedefinin (SLO) tersidir. Hedef yüzde doksan dokuz buçuk ise, kalan pay tolere edilebilen hata payıdır. Bu sayı tartışmanın dilini değiştirir. Bütçe varken risk almak makuldür; yeni özellik ve daha hızlı dağıtım önceliklidir. Bütçe tükendiğinde öncelik kendiliğinden dayanıklılığa kayar ve bu kural **önceden** anlaşılmıştır (`151`). Değerli olan yanı budur: karar anında kimin sesi daha yüksek çıktığı önemsizleşir. B şıkkı bütçeye teknik bir etki atfeder; bütçe ölçer, engellemez. C şıkkı bunu bir yetki aracına çevirir; oysa amaç ortak eşiktir, veto değildir. D şıkkı bütçeyi rapora hapseder ve tek faydasını yok eder. Aynı yaklaşım teknik borç için de geçerlidir: borç iş diline çevrilir, yani yavaşlayan teslimat, artan olay sayısı ve uzayan kurtarma süresi olarak konuşulur (`195`). Platform ise ürün gibi ele alınır; altın yol hazır sunulur, sapmanın bakım maliyeti sapan ekipte kalır (`191`).
