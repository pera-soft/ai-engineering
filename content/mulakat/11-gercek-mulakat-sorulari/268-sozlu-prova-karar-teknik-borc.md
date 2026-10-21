---
slug: sozlu-prova-karar-teknik-borc
baslik: "Sözlü Prova · Karar, Ölçüm ve Teknik Borç: KPI, Monolit Kararı, Kalite Kapıları ve Olay Önceliği"
kademe: İleri
sira: 268
etiketler: 11
---

## Konu anlatımı

Bu dosyada altı gerçek mülakat sorusu var. Hepsi açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz; onu kendi geçmişinden çıkarman için sorular sorulur. Altı soru tek eksende ilerler: neyi ölçersin, neye göre karar verirsin, neyi zorunlu kılarsın, kısıtlı zamanı nereye koyarsın.

Önemli bir çerçeve: bu soruların bir kısmı yönetici sorusu gibi durur. Sen proje yöneticisi değilsin, kıdemli bir geliştiricisin. Cevapların da geliştirici gözünden kurulmalı. Yani "süreç sahibi" gibi değil, süreçle çalışan ve onu iyileştiren mühendis gibi. Bu, cevabı zayıflatmaz; tersine dürüst ve inandırıcı yapar. Rolünü olduğundan büyük gösteren aday ikinci soruda yakalanır.

### "Ekip yönetimini nasıl organize ettiniz, hangi KPI'larla başarıyı ölçtünüz?"

**Soru aslında ne ölçüyor?**

Bu soru yönetici arayışı gibi durur ama öyle değildir. Geliştirici mülakatında görüşmeci iki şeye bakar. Birincisi, bu aday işin uçtan uca nasıl aktığını görüyor mu, yoksa yalnız kendi görevine mi bakıyor. İkincisi ve asıl ayrıştırıcı olanı: neyi başarı saydığın. Seçtiğin sayı, mühendislik olgunluğunu tek cümlede ele verir. Ağırlık muhakemededir. "Ekip yönettim" demek zorunda değilsin; akışın içinde hangi rolü aldığını anlatmak yeterlidir.

**Sözlü cevap iskeleti**

1. **Rolünü dürüstçe konumla.** "Ekip yönetmedim ama akışın içindeydim" cümlesi zayıflık değildir. Ardından somutla: iş kalemini bölmek, tahmin vermek, inceleme yapmak, dağıtımı izlemek.
2. **Organizasyonu akışla anlat, unvanla değil.** Kısa günlük senkron, her kalemin tek sahibi, tanımlanmış bitiş, engelin aynı gün görünür olması. Bunlar somut ve doğrulanabilir cümlelerdir.
3. **Sonuç metriklerine geç ve dörtlüyü say.** Dağıtım sıklığı, değişiklik öncü süresi (lead time for changes), değişiklik hata oranı, ortalama kurtarma süresi (`191`).
4. **Dörtlünün neden birlikte okunduğunu söyle.** İlk ikisi hızı, son ikisi kararlılığı ölçer. Yalnız hıza bakan ekip üretimi kırar; yalnız kararlılığa bakan ekip donar.
5. **Sahte metrikleri adlandır ve zararını ver.** Satır sayısı, kapatılan iş sayısı, commit sayısı. Bunlar çıktı ölçer, sonuç ölçmez; ölçülen sayı hedefe dönüşünce oyuna açılır.
6. **Kendi ölçütünle kapat.** Ölçtüğüm şey kullanıcıya ulaşan değer ve üretimdeki sağlıktır. Sayılar kişiyi değil akışı değerlendirir; kişi bazlı sıralama güveni bitirir (`195`).

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cikti metrikleri ile sonuc metrikleri karsilastirmasi ve olcut bozulmasi uyarisi">
  <defs>
    <marker id="sozlu7-kpi" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Çıktı ölçen sayı hareketi sayar; sonuç ölçen sayı değeri sayar</text>
  <rect x="14" y="36" width="316" height="152" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Çıktı metrikleri — sahte başarı</text>
  <text x="172" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yazılan satır sayısı</text>
  <text x="172" y="100" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapatılan iş kalemi sayısı</text>
  <text x="172" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">commit ve PR sayısı</text>
  <text x="172" y="140" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kişi bazlı sıralama tablosu</text>
  <text x="172" y="164" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">hepsi artabilir, kullanıcıya hiçbir şey ulaşmayabilir</text>
  <line x1="332" y1="112" x2="342" y2="112" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-kpi)"/>
  <rect x="344" y="36" width="302" height="152" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sonuç metrikleri — DORA dörtlüsü</text>
  <text x="495" y="80" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">hız: dağıtım sıklığı</text>
  <text x="495" y="100" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">hız: değişiklik öncü süresi</text>
  <text x="495" y="120" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">kararlılık: değişiklik hata oranı</text>
  <text x="495" y="140" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">kararlılık: ortalama kurtarma süresi</text>
  <text x="495" y="164" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">dördü birlikte okunur; ikisi tek başına yanıltır</text>
  <rect x="14" y="202" width="632" height="104" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="224" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Her ölçüt hedefe dönüşünce bozulur</text>
  <text x="330" y="248" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kapatılan iş sayısı hedefse iş kalemleri bölünür; satır sayısı hedefse kod şişer.</text>
  <text x="330" y="268" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">İnceleme sayısı hedefse onaylar hızlanır ama sığlaşır; sayı yükselir, kalite düşer.</text>
  <text x="330" y="292" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">Panzehir: sayıyı kişiye değil akışa bağla, hız ile kararlılığı hep birlikte oku.</text>
</svg>
```

**Elenme sebepleri**

- Rolü olduğundan büyük anlatmak. "Ekibi ben yönettim" cümlesi ikinci soruda çöker ve tüm cevabın güvenilirliğini alır.
- Metrik olarak satır sayısı veya kapatılan iş sayısı vermek. Bu tek başına yeterli bir eleme sebebidir.
- Yalnız hız metriklerinden konuşmak. Kararlılığı anmayan aday, hızın bedelini görmemiş sayılır.
- Sayıyı kişiye bağlamak. Kişi bazlı sıralama iş birliğini bitirir ve ölçümü oyuna açar.
- Hiç sayı söylememek. "İletişim iyiydi, ekip uyumluydu" cümlesi ölçüm değil, izlenimdir.
- Ölçütün bozulabileceğini hiç düşünmemek. Hedefe dönüşen her sayının nasıl şişirileceğini söylemek güçlü bir sinyaldir.
- Ölçümü bir yöntem gibi anlatıp hiç eyleme bağlamamak. Ölçülen sayı bir karar değiştirmiyorsa toplanmasının anlamı yoktur.

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Çalıştığın ekipte iş nasıl akıyordu? Kim önceliklendiriyordu, kalemler nasıl bölünüyordu?
- Sen akışın hangi noktasındaydın? Tahmin, inceleme, dağıtım, nöbet, teknik yönlendirme?
- Ne kadar sıklıkla üretime çıkıyordunuz? Haftada bir mi, ayda bir mi, günde birkaç kez mi?
- Bir değişiklik yazıldıktan sonra kaç günde üretime ulaşıyordu? Bekleme nerede birikiyordu?
- Çıkan dağıtımların yaklaşık kaçı geri alındı ya da acil yama gerektirdi?
- Bir kesinti başladığında toparlanma ne kadar sürüyordu? Bunu ölçen bir kayıt var mıydı?
- Ölçülen bir sayı hiç yanlış davranış üretti mi? Örneğin kalemler yapay biçimde bölündü mü?
- Bir ölçüm sonucunda ne değişti? Değişmediyse o ölçümün neden işe yaramadığını anlatmak da güçlüdür.

Böyle bir deneyimin yoksa uydurma. `256`'daki dürüst köprüyü kur: ekip yönetmediğini açıkça söyle, akış içinde yaptığın somut işi anlat, sonra "bu ekipte hangi sayılara bakardım" diye devam et. Ölçmediğin bir sayıyı ölçmüş gibi anlatmak, bu soruda en çabuk yakalanan hatadır.

### "Mikroservis ve monolit arasında kararı hangi kriterlerle verirsiniz?"

**Soru aslında ne ölçüyor?**

Bu soru bilgi değil, moda direnci ölçer. Görüşmeci mikroservisin ne olduğunu zaten biliyor. Merak ettiği şu: bu aday kararı gerekçeyle mi veriyor, alışkanlıkla mı. Güçlü cevabın iki işareti vardır. Birincisi bir varsayılan seçenek koyabilmek, ikincisi ayrıştırmanın faturasını sayabilmek. Ağırlık muhakemededir. Deneyim ikinci sıradadır ama bir ayrıştırmanın bedelini yaşamış aday bunu cümlelerinden belli eder.

**Sözlü cevap iskeleti**

1. **Varsayılanı ilk cümlede koy.** Varsayılan monolittir. Mikroservis, kanıtlanmış bir ihtiyaç karşılığında satın alınan pahalı bir yapıdır.
2. **Faturayı say.** Ağ gecikmesi ve kısmi başarısızlık, dağıtık veri tutarlılığı, sürüm uyumu, uçtan uca izleme, dağıtım ve nöbet maliyeti. Bu bedeller ilk günden ödenir (`95`).
3. **Ayrıştırmayı haklı çıkaran ölçütleri ver.** Bağımsız ölçekleme ihtiyacı, ayrı ekip sahipliği, farklı değişim hızı, farklı uygunluk gereksinimi. Bunlardan biri yoksa gerekçe zayıftır.
4. **Ekip yapısını dâhil et.** Conway yasası sistemin ekip iletişimine benzediğini söyler. Tek ekibin sahiplendiği sekiz servis çoğu zaman tek servis gibi davranır.
5. **Ara yolu sun.** Modüler monolit sınırı pakette çizer, dağıtımı tek tutar. Sınır burada tutuyorsa ayrıştırma sonra yapılabilir; tutmuyorsa mikroservis de kurtarmaz (`96`, `97`).
6. **Yanlış kararın adını koy.** Sınır bilinmeden bölünürse çıkan şey dağıtık monolittir. Ağın maliyeti alınır, bağımsızlık alınmaz; geri dönmek de pahalıdır.

**Elenme sebepleri**

- "Mikroservis her zaman daha iyidir" demek. Takas cümlesi kurmayan aday kararı değil modayı anlatıyor sayılır.
- Faturayı hiç anmamak. Dağıtım, izleme, sürümleme ve nöbet maliyetleri kararın asıl parçasıdır.
- Kararı yalnız kod boyutuna bağlamak. "Dosya büyüdü" bir sınır ölçütü değildir; büyük dosya modüler de olabilir.
- Ekip yapısını dışarıda bırakmak. Servis sayısı ekip sayısını aşınca sahiplik dağılır ve kimse sorumlu olmaz.
- Modüler monoliti hiç bilmemek. Ara yolu görmeyen aday kararı ikili bir seçime indirger.
- Veri tarafını atlamak. Ayrıştırmanın en pahalı kısmı kod değil, paylaşılan veriyi bölmektir.
- Geri dönüşü konuşmamak. İki servisi birleştirmek de meşru bir karardır ve olgunluk gösterir.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir. Şu sorular onu çıkarır:

- Çalıştığın sistem monolit miydi, mikroservis miydi, karışık mıydı?
- Karar sen gelmeden mi verilmişti? Gerekçesini bilen biri var mıydı?
- Yeni bir servis çıkarma tartışması yaşandı mı? Hangi gerekçe öne sürüldü?
- Ayrıştırmadan sonra ne zorlaştı? Yerel hata ayıklama, uçtan uca test, sürüm uyumu, veri tutarlılığı?
- İki servisi aynı anda çıkarmak zorunda kaldınız mı? Bu, sınırın yanlış yerde olduğunun en net belirtisidir.
- Paylaşılan bir tablo var mıydı? Kim yazıyordu, kim okuyordu, sorun çıktı mı?
- Servis sayısı ekip sayısından fazla mıydı? Nöbet ve sahiplik nasıl dağılmıştı?
- Bir servisin geri birleştirildiğini gördün mü? Gerekçesi neydi?

Deneyimin yoksa uydurma. `256`'daki köprü burada rahat kurulur. Modüler bir monolitte paket sınırı çizmiş olmak gerçek bir cevaptır. Bunu mikroservis ayrıştırması gibi anlatma; "sınırı pakette çizdim, dağıtımı bölmedim" demek daha güçlüdür.

### "CI/CD hattında hangi kontrolleri ZORUNLU kılarsınız ve neden?"

**Soru aslında ne ölçüyor?**

Buradaki asıl kelime "zorunlu". Görüşmeci araç listesi istemiyor; nereye kapı koyduğunu ve bunun bedelini bilip bilmediğini ölçüyor. İki uç da zayıftır. Hiç kapı koymayan aday kaliteyi tesadüfe bırakır; her şeyi zorunlu kılan aday hattı durdurur. Güçlü cevap, engelleyen ile uyaran kontrolü ayırır ve her kapının süre maliyetini söyler. Ağırlık muhakemededir; deneyim varsa süre rakamıyla desteklenir.

**Sözlü cevap iskeleti**

1. **Ölçütü baştan söyle.** Bir kontrol ancak iki şart birlikte sağlanınca zorunlu olur: bulduğu sorun pahalıdır ve sonucu güvenilirdir. Kararsız sonuç veren kontrol asla kapı olamaz.
2. **Zorunlu kapıları say ve gerekçesini ver.** Derleme ve birim testler, statik analiz kritik kuralları, bağımlılık ve gizli anahtar taraması, veri tabanı göç (migration) denetimi (`220`, `190`).
3. **Kapsam eşiği tuzağını aç.** Global yüzde eşiği anlamsız test üretir. Doğrusu kritik yol kapsamıdır: ödeme, yetkilendirme, para hesabı gibi akışlar test edilmeden geçemez.
4. **Uyaran kontrolleri ayır.** Karmaşıklık, yinelenme, kapsam trendi ve yavaş testler bilgi olarak gösterilir. Bunlar hattı durdurmaz; inceleme sırasında konuşulur (`191`).
5. **Hızı bir gereksinim gibi konuş.** Ana hat birkaç dakikada bitmelidir. Uzun süren taramalar gece koşar; hattı yavaşlatan kapı sessizce baypas edilmeye başlanır.
6. **Kararsız testi kurala bağla.** Kararsız (flaky) test derhal karantinaya alınır, sahibi ve tarihi yazılır. Yeniden çalıştırmayı alışkanlık hâline getiren ekip hatta güvenmeyi bırakır.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="CI hattinda zorunlu kapilar, uyaran kontroller ve hattin hiz maliyeti">
  <defs>
    <marker id="sozlu7-kapi" markerWidth="8" markerHeight="8" refX="6.5" refY="4" orient="auto"><path d="M0 0 L8 4 L0 8 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Zorunlu kapı: pahalı sorunu bulur ve güvenilir sonuç verir</text>
  <rect x="14" y="36" width="120" height="122" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="74" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Derleme</text>
  <text x="74" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ve birim testler</text>
  <text x="74" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en ucuz sinyal</text>
  <text x="74" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en hızlı geri dönüş</text>
  <text x="74" y="142" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">saniyeler</text>
  <line x1="136" y1="97" x2="140" y2="97" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-kapi)"/>
  <rect x="142" y="36" width="120" height="122" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="202" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">2 · Statik analiz</text>
  <text x="202" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız kritik kural</text>
  <text x="202" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">gürültü kapıyı</text>
  <text x="202" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">değersizleştirir</text>
  <text x="202" y="142" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">bir iki dakika</text>
  <line x1="264" y1="97" x2="268" y2="97" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-kapi)"/>
  <rect x="270" y="36" width="120" height="122" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">3 · Güvenlik</text>
  <text x="330" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bağımlılık ve sır taraması</text>
  <text x="330" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sızan anahtar</text>
  <text x="330" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">geri alınamaz</text>
  <text x="330" y="142" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">dakikalar</text>
  <line x1="392" y1="97" x2="396" y2="97" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-kapi)"/>
  <rect x="398" y="36" width="120" height="122" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="458" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">4 · Kritik yol</text>
  <text x="458" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapsamı</text>
  <text x="458" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ödeme, yetki, para</text>
  <text x="458" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">testsiz geçemez</text>
  <text x="458" y="142" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">hedefli ölçüm</text>
  <line x1="520" y1="97" x2="524" y2="97" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-kapi)"/>
  <rect x="526" y="36" width="120" height="122" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="586" y="57" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">5 · Göç denetimi</text>
  <text x="586" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geri alınabilir mi?</text>
  <text x="586" y="100" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">şema değişimi</text>
  <text x="586" y="118" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en pahalı hatadır</text>
  <text x="586" y="142" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">otomatik kontrol</text>
  <rect x="14" y="172" width="316" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="193" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Uyaran kontrol — hattı durdurmaz</text>
  <text x="172" y="215" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karmaşıklık, yinelenme, kapsam trendi</text>
  <text x="172" y="235" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yavaş test listesi, yeni uyarı sayısı</text>
  <text x="172" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">inceleme sırasında konuşulur</text>
  <text x="172" y="275" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonucu kararsız olan hiçbir kontrol kapı olamaz</text>
  <rect x="344" y="172" width="302" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="193" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Her kapının bir bedeli var</text>
  <text x="495" y="215" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yavaşlayan hat baypas edilmeye başlanır</text>
  <text x="495" y="235" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kararsız test hatta güveni bitirir</text>
  <text x="495" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">global kapsam eşiği anlamsız test üretir</text>
  <text x="495" y="275" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">uzun taramalar gece koşar, ana hat kısa kalır</text>
</svg>
```

**Elenme sebepleri**

- Araç adı listelemek. Hangi kontrolün neyi engellediğini söylemeyen cevap, kararı değil kurulumu anlatır.
- Her kontrolü zorunlu yapmak. Hattı dakikalarca durduran kapılar kısa sürede atlanmaya başlanır.
- Kapsam eşiğini tek kalite ölçütü saymak. Yüzde hedefi, riskin nerede olduğunu hiç sormadan test yazdırır.
- Kararsız testi yeniden çalıştırarak geçmek. Bu alışkanlık hattı bir kapı olmaktan çıkarır.
- Güvenlik taramasını hiç anmamak. Depoya sızan bir anahtar, silinse bile geçmişte kalır ve iptal edilmelidir.
- Veri tabanı göçünü kontrol dışında bırakmak. Geri alınamayan şema değişikliği en pahalı üretim hatasıdır.
- Kapıları yalnız ana dala koymak. Birleştirmeden sonra bakılan hata, düzeltilmesi en pahalı hatadır.

**Kendi hikâyen**

Bu soruda hikâye çok somut olabilir. Şu sorular onu çıkarır:

- Şu an çalıştığın hatta hangi adımlar var? Hangileri gerçekten birleştirmeyi engelliyor?
- Hattın tamamı ne kadar sürüyor? Bu süre insanların davranışını değiştirdi mi?
- Hiç kararsız testle uğraştın mı? Nasıl fark ettin, karantinaya alındı mı, sahibi kimdi?
- Bir kontrol yüzünden kırmızıya düşen bir birleştirme oldu mu? Sonucunda ne yapıldı?
- Statik analiz uyarıları okunuyor mu, yoksa sayı büyüdükçe görmezden mi geliniyor?
- Bir sır veya anahtar depoya sızdı mı? Nasıl yakalandı, sonrasında ne değişti?
- Veri tabanı göçleri nasıl denetleniyor? Geri alma senaryosu hiç denendi mi?
- Bir kapıyı sen mi eklettin? Gerekçeyi nasıl anlattın, ekip nasıl karşıladı?

Deneyimin yoksa uydurma. `256`'daki köprüyü kur. Kendi projende basit bir hat kurup birim testi ve statik analizi zorunlu yapmak gerçek bir deneyimdir. "Bunu küçük ölçekte yaptım, kurumsal ölçekte şu iki şeyin ekleneceğini biliyorum" cümlesi dürüst ve güçlüdür.

### "Teknik borcu nasıl görünür kılar, önceliklendirir ve iş hedefleriyle dengelersiniz?"

**Soru aslında ne ölçüyor?**

Bu, dosyanın en ayrıştırıcı sorusudur. Teknik borcun ne olduğunu herkes söyler. Ayrım üç yerde çıkar. Birincisi, borcu iş diline çevirebilmek; teknik gerekçe iş tarafında karşılık bulmaz. İkincisi, hepsini değil doğru olanı seçebilmek. Üçüncüsü, "hepsini bırakıp baştan yazalım" tuzağına düşmemek. Görüşmeci burada bir mühendisten çok bir muhatap arıyor. Ağırlık muhakemede ve iletişimdedir.

**Sözlü cevap iskeleti**

1. **Görünür kılmakla başla.** Borç, konuşmada değil listede yaşar. Aynı backlog'da, aynı biçimde, sahibi ve gerekçesiyle duran bir kalem olur (`195`).
2. **İş diline çevir.** "Kod kirli" bir gerekçe değildir. "Bu modülde her değişiklik iki gün sürüyor ve dörtte biri geri dönüyor" bir gerekçedir.
3. **Faiz metaforunu doğru kullan.** Borcun kendisi değil faizi acıtır. Faiz, o alana her dokunduğunda ödediğin fazladan süredir; ödenmezse birikir (`132`).
4. **Önceliklendirme ölçütünü ver.** Değişim sıklığı ile acının kesişimi. Sık dokunulan ve sık hata çıkaran yer önce gelir; ölü koddaki borç beklemeye devam eder.
5. **Dengeyi bir kurala bağla.** Her sprintte sabit bir pay ayır. Sabit pay, borcu "boş zaman işi" olmaktan çıkarır ve pazarlığı her hafta yeniden yapmayı önler.
6. **Büyük yeniden yazım tuzağını adlandır.** Baştan yazma riski ve süreyi birlikte büyütür. Doğrusu kademeli ilerlemedir: dokunduğun yeri iyileştir, sınırı önce testle çevrele (`140`).

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teknik borcu gorunur kilma, is diline cevirme ve onceliklendirme akisi">
  <defs>
    <marker id="sozlu7-borc" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Görünmeyen borç tartışılamaz; tartışılmayan borç ödenmez</text>
  <rect x="14" y="36" width="196" height="190" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · Görünmez borç</text>
  <text x="112" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yalnız koridorda konuşulur</text>
  <text x="112" y="102" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ayrı bir listede saklanır</text>
  <text x="112" y="124" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sahibi ve gerekçesi yoktur</text>
  <text x="112" y="146" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">boş zaman işi sayılır</text>
  <text x="112" y="174" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">belirti: basit istek</text>
  <text x="112" y="192" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iki güne yayılır, tahminler</text>
  <text x="112" y="210" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sürekli aşılır</text>
  <line x1="212" y1="131" x2="228" y2="131" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-borc)"/>
  <rect x="232" y="36" width="196" height="190" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2 · İş diline çeviri</text>
  <text x="330" y="80" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">zayıf: "kod kirli"</text>
  <text x="330" y="102" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">güçlü: teslim süresi uzuyor</text>
  <text x="330" y="124" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">güçlü: hata oranı artıyor</text>
  <text x="330" y="146" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">güçlü: kesinti riski büyüyor</text>
  <text x="330" y="168" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">güçlü: yeni kişi geç üretken olur</text>
  <text x="330" y="196" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">her cümlede bir sayı bulunsun;</text>
  <text x="330" y="214" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sayı olmadan gerekçe duyulmaz</text>
  <line x1="430" y1="131" x2="446" y2="131" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu7-borc)"/>
  <rect x="450" y="36" width="196" height="190" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">3 · Önceliklendirme</text>
  <text x="548" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ölçüt: değişim sıklığı × acı</text>
  <text x="548" y="102" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">sık dokunulan + sık kırılan: önce</text>
  <text x="548" y="124" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">sık dokunulan + sakin: izle</text>
  <text x="548" y="146" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ölü kod + çirkin: bekleyebilir</text>
  <text x="548" y="174" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">faizi nerede ödüyorsan</text>
  <text x="548" y="192" text-anchor="middle" font-size="9" fill="#5D7789" font-família="system-ui">anaparayı orada kapat</text>
  <rect x="14" y="240" width="632" height="66" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="261" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Tuzak: "durup her şeyi baştan yazalım"</text>
  <text x="330" y="281" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Yeniden yazım süreyi ve riski birlikte büyütür; eski sistem bu sırada değişmeye devam eder.</text>
  <text x="330" y="299" text-anchor="middle" font-size="10" fill="#25804C" font-family="system-ui">Doğrusu kademeli: dokunduğun yeri iyileştir, önce testle çevrele, her sprintte sabit pay ayır.</text>
</svg>
```

**Elenme sebepleri**

- Borcu yalnız teknik dille anlatmak. "Kod kirli, temizlemek lazım" cümlesi hiçbir öncelik listesinde yer bulmaz.
- Ayrı bir borç listesi tutmak. Ana listede görünmeyen kalem, planlamada hiç var olmamış gibi davranılır.
- "Boş zaman kalırsa yaparız" demek. Boş zaman hiç kalmaz; bu cümle borcun ertelenmesinin kibar hâlidir.
- Bütün borcu eşit saymak. Ölü koddaki çirkinlikle ödeme akışındaki kırılganlık aynı öncelikte olamaz.
- Büyük yeniden yazımı çözüm olarak sunmak. Süre ve risk birlikte büyür, eski sistem bu sırada değişmeye devam eder.
- Hiç sayı vermemek. Süre, hata oranı veya olay sayısı olmadan gerekçe bir görüş olarak duyulur.
- Borcu yalnız başkasının hatası gibi anlatmak. Bilinçli olarak alınmış borç meşrudur; asıl mesele kaydedilip kaydedilmediğidir.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Dokunmaktan çekindiğin bir modül var mı? Çekinme sebebini tek cümlede söyleyebilir misin?
- O modülde basit bir değişiklik ne kadar sürüyor? Aynı iş temiz bir modülde ne kadar sürerdi?
- Aynı yerden tekrar tekrar hata çıkıyor mu? Kaç kez, hangi aralıkla?
- Bilerek alınmış bir borç oldu mu? Teslim tarihi için neyi ertelediniz, bir yere yazıldı mı?
- Borç kalemleri listede görünüyor mu, yoksa yalnız konuşuluyor mu?
- Bir borcu kapatmayı savundun mu? Hangi gerekçeyi kullandın, karşı taraf neye ikna oldu?
- Kapattıktan sonra ne değişti? Süre, hata sayısı ya da olay sayısı ölçüldü mü?
- Hiç "baştan yazalım" önerisi duydun mu? Sonuç ne oldu?

Deneyimin yoksa uydurma. `256`'daki köprü burada da işler. Kendi projende bir modülü testle çevreleyip kademeli iyileştirmiş olmak gerçek bir deneyimdir. Anlatırken ölçtüğün farkı söyle; ölçmediysen ölçmediğini söyle. Bu dürüstlük, uydurulmuş bir kurumsal hikâyeden çok daha güçlü duyulur.

### "Performans, güvenlik ve ölçeklenebilirlik dengesini nasıl kurarsınız?"

**Soru aslında ne ölçüyor?**

Bu soru bir tuzak içerir. Üçünü de "hepsi önemlidir" diye toplamak kolaydır ve hiçbir şey söylemez. Görüşmecinin aradığı şey şu: bu aday bu niteliklerin birbiriyle çatıştığını biliyor mu ve çatışmayı neye göre çözüyor. Ölçüt olmadan denge kurulamaz. İkinci mesele kayıt: kararın gerekçesi yazılmadıysa altı ay sonra kimse hatırlamaz. Ağırlık muhakemededir.

**Sözlü cevap iskeleti**

1. **Çatışmayı açıkça söyle.** Bu üçü aynı yöne çekmez. Şifreleme ve denetim kaydı gecikme ekler; önbellek hızlandırır ama tazeliği ve iptali geciktirir.
2. **Ölçüt koymadan konuşma.** "Hızlı olsun" bir hedef değildir. "p95 300 ms altında, yetki iptali en geç bir dakikada etkili" ölçülebilir hedeflerdir (`141`).
3. **Bütçe dilini kullan.** Gecikme bütçesi belirlenir ve parçalara bölünür. Yeni bir güvenlik kontrolü eklendiğinde bütçeden ne kadar aldığı ölçülür (`189`).
4. **Pazarlığa kapalı olanı ayır.** Kimlik doğrulama, yetkilendirme ve veri gizliliği performans için esnetilmez. Esneyen şey uygulama biçimidir: önbellek süresi, tarama derinliği, kayıt ayrıntısı.
5. **Kararı geri dönülebilirliğe göre hızlandır.** Geri dönülebilir kararlar hızlı verilir ve denenir. Geri dönülemez kararlar yavaş verilir; sızan veri geri alınamaz.
6. **Kararı ve gerekçesini kaydet.** ADR bir bürokrasi değil, hafızadır. Seçenekler, ölçütler, seçilen yol ve reddedilenin sebebi tek sayfada durur (`140`).

**Elenme sebepleri**

- "Hepsi önemlidir, dengede tutarım" demek. Bu cümle bir karar değil, kaçınmadır ve hemen fark edilir.
- Ölçüt vermemek. Sayısı olmayan hedefle denge kurulamaz; tartışma zevke döner.
- Güvenliği tamamen mutlaklaştırmak. "Asla esnetilmez" demek, hangi ayrıntının esnetilebileceğini hiç düşünmediğini gösterir.
- Güvenliği en sona bırakmak. Sonradan eklenen kontrol hem pahalıdır hem tasarımı zorlar.
- Ölçeklenebilirliği yalnız sunucu eklemek sanmak. Durum tutan bir tasarım yatay ölçeği baştan engeller.
- Kararın gerekçesini yazmamak. Belgelenmemiş karar birkaç ay sonra tartışmaya yeniden açılır.
- Geri dönülebilir ile geri dönülemez kararı ayırmamak. İkisine aynı özeni harcamak zamanı yanlış yere koyar.

**Kendi hikâyen**

Bu soruda somut tek bir takas bile yeterlidir. Şu sorular onu çıkarır:

- Hiç güvenlik kontrolü yüzünden gecikme arttı mı? Ne kadar arttı, nasıl ölçüldü?
- Bir önbellek eklendi mi? Süresi neye göre seçildi, bayat veri sorunu yaşandı mı?
- Yetki iptali ne kadar sürede etkili oluyordu? Bu süre kimseyi rahatsız etti mi?
- Oturum durumu nerede tutuluyordu? Yatay ölçek denendi mi, engel çıktı mı?
- Bir performans iyileştirmesi güvenlik gerekçesiyle reddedildi mi? Gerekçe neydi?
- Kararlar bir yere yazılıyor muydu? ADR, karar kaydı ya da en azından bir wiki sayfası?
- Sonradan pişman olunan bir karar var mı? Neyin ölçülmediği anlaşıldı?
- Hedefler sayıyla mı konuşuluyordu, yoksa "yavaş" ve "hızlı" kelimeleriyle mi?

Böyle bir vakan yoksa uydurma. `256`'daki köprüyü kur. Küçük ölçekte de gerçek takas yaşanır: bir uç noktaya önbellek koyup tazelik kaybını gözlemlemek gerçek bir deneyimdir. Onu olduğu ölçekte anlat; büyütmeye çalışırsan ilk detay sorusunda anlaşılır.

### "Üretimde kritik hata çıktığında önceliklendirme ve müdahale planınız nedir?"

**Soru aslında ne ölçüyor?**

Bu soru soğukkanlılık ölçer. Görüşmeci tek şeye bakar: bu aday baskı altında sıralamayı bozuyor mu. En sık yapılan hata, kök nedeni anlamadan hiçbir şey yapmamaktır. İkinci sık hata, anlamadan ileriye doğru yama atmaktır. Doğru sıra bellidir: önce etkiyi durdur, sonra anla. Ağırlık yöntemdedir. Deneyim varsa çok değerlidir; yoksa yöntemi doğru sıralamak yine iyi bir cevaptır.

**Sözlü cevap iskeleti**

1. **Şiddeti sınıflandır.** Kaç kullanıcı etkileniyor, para ya da veri kaybı var mı, geçici çözüm mevcut mu. Sınıflandırma müdahalenin hızını ve kaç kişinin katılacağını belirler.
2. **Önce hafiflet.** Geri alma en hızlı ve en öngörülebilir adımdır. Özellik bayrağını kapatmak, trafiği yönlendirmek veya sürümü geri çekmek anlamaktan önce gelir.
3. **Rolleri dağıt.** Bir kişi kararı yönetir, bir kişi iletişimi yürütür, bir kişi teknik incelemeyi yapar. Herkesin aynı anda log okuması en yavaş yöntemdir (`192`).
4. **İletişimi ritme bağla.** Sabit aralıkla kısa güncelleme yayınla. Bilgi akmayan yerde herkes ayrı kanaldan soru sorar ve müdahale yavaşlar.
5. **Zaman çizelgesi tut.** Ne zaman ne yapıldı, ne gözlendi. Bu kayıt hem müdahale sırasında hem sonrasında en değerli belgedir.
6. **Suçsuz postmortem ile kapat.** Soru "kim yaptı" değil, "bu hata neden bu kadar kolay üretime çıktı". Her eylem maddesinin sahibi ve tarihi olur; olmayan madde yapılmaz (`195`).

**Elenme sebepleri**

- Kök neden bulunana kadar hiçbir şey yapmamak. Bekleme de bir karardır ve etkiyi büyütür.
- Anlamadan ileriye doğru yama atmak. Doğrulanmamış düzeltme kesintiyi uzatır ve ikinci bir olay üretir.
- Geri almayı hiç anmamak. En hızlı hafifletme yolunu bilmeyen aday, olay yönetmemiş sayılır.
- Şiddet sınıflandırması yapmamak. Her olaya aynı tepkiyi vermek ya paniği ya da ilgisizliği kurumsallaştırır.
- İletişimi atlamak. Teknik olarak doğru müdahale, haber verilmediği için başarısız sayılabilir.
- Postmortem'i suçlu arama toplantısına çevirmek. Suçlanan ekip bir sonraki olayı geç bildirir.
- Eylem maddelerini sahipsiz bırakmak. Tarihi ve sahibi olmayan madde bir sonraki postmortem'de aynen tekrar yazılır.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası zaman çizelgesidir. Şu sorular onu çıkarır:

- Yaşadığın bir üretim olayını hatırlıyor musun? Belirtisi neydi, kim fark etti?
- Nasıl fark edildi? Alarm mı, gösterge panosu mu, müşteri şikâyeti mi?
- İlk yapılan şey neydi? Geri alma mı, inceleme mi, iletişim mi?
- Etki ne kadar sürede durdu? Kök neden ne kadar sonra bulundu? İki süre arasındaki fark önemlidir.
- Roller kendiliğinden mi dağıldı, yoksa biri koordinasyonu üstlendi mi?
- Dışarıya ne zaman haber verildi? Güncellemeler düzenli miydi?
- Sonrasında bir postmortem yapıldı mı? Çıkan maddelerin kaçı gerçekten tamamlandı?
- Aynı hata bir daha oldu mu? Olmadıysa bunu engelleyen somut şey neydi: test mi, alarm mı, kapı mı?

Bu deneyim yoksa uydurma. Nöbet tutmamış olmak ayıp değildir; nöbet tutmuş gibi anlatmak elenme sebebidir. `256`'daki köprüyü kur: yöntemi doğru sırayla anlat, sonra "ben bu akışta şu rolde yer aldım" diyerek gerçek katkını söyle. Bir hatayı ölçüp düzelten ve tekrarını bir testle engelleyen küçük bir vaka da gerçek bir cevaptır.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Önce iskeleti okuma. Soruyu yüksek sesle oku, sonra dosyayı kapat.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek prova sayılmaz; ağız ile beyin aynı hızda çalışmaz.
- Sonra iskeletle karşılaştır. Hangi adımı atladın, işaretle.
- Bu altı soruda en sık atlanan adım ölçüt cümlesidir. Neye göre karar verdiğini söylemezsen cevap görüş gibi duyulur.
- İkinci sık atlanan şey roldür. Yönetici gibi konuşup sonra detayda takılmak, cevabın tamamını götürür.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- Sayıyı ezberle, cümleyi ezberleme. Ezberlenmiş cümle mülakatta ezber gibi duyulur.
- Altı soruyu üst üste yapma. Arada ara ver; yorgun ses ikna etmez.
- Ölçüt şudur: bir sorunun adımlarını kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Tek cümlede

Bu altı soru teknik bilgiyi değil, karar verme ve ölçme disiplinini ölçer. Birincisinde kazanan cümle şudur: çıktı değil sonuç ölçülür, sayı kişiye değil akışa bağlanır. İkincisinde varsayılan monolittir ve ayrıştırmanın faturası ilk günden ödenir. Üçüncüsünde zorunlu kapı ancak pahalı sorunu güvenilir biçimde bulduğunda konur. Dördüncüsünde borç iş diline çevrilir ve faizi en çok ödediğin yerden kapatılır. Beşincisinde denge ölçütle kurulur, karar ve gerekçesi yazılır. Altıncısında sıra hiç değişmez: önce hafiflet, sonra anla, sonra tekrarını engelle. Hepsinde aynı disiplin geçerlidir: seçeneği söyle, ölçütü söyle, bedeli söyle, ölçülen sonucu söyle. Ve hiçbirinde olmayan deneyim uydurulmaz; dürüst köprü her zaman uydurmadan güçlüdür.

# sozlu7-1 | Temel
## Soru
Bir yönetici geliştirici başarısını yazılan satır sayısı ve kapatılan iş kalemi sayısıyla ölçmek istiyor. Bu yaklaşımın temel kusuru nedir?
## A
Sayılar doğru ama eşikler düşüktür; hedefler yükseltilirse ölçüm anlamlı hâle gelir
🤖 sorun eşikte değil, ölçülen şeyin türündedir.
## B
Çıktıyı ölçer, sonucu ölçmez; hedefe dönüşünce iş kalemleri bölünür ve kod şişer
🤖 Doğru: değer üretimi değil hareket sayılmış olur.
## C
Ölçüm doğrudur ama sıklığı yanlıştır; aylık yerine haftalık toplanması gerekir
🤖 yanlış metriği daha sık toplamak yanlışı büyütür.
## D
Kusur yoktur; bu iki sayı geliştirici verimliliğinin en nesnel göstergesi sayılır
🤖 nesnel görünmek doğru olmakla aynı şey değildir.
## Doğru
B
## Açıklama
Satır sayısı ve kapatılan iş sayısı **çıktı** metrikleridir; hareketi sayarlar, değeri değil. İkisi de kullanıcıya hiçbir şey ulaşmadan yükselebilir. Daha ağır sorun ise ölçütün hedefe dönüşmesidir: ölçülen her sayı bir hedef hâline gelince oyuna açılır. Kapatılan iş sayısı hedefse kalemler yapay biçimde bölünür. Satır sayısı hedefse kod gereksiz yere şişer, yeniden düzenleme cezalandırılır çünkü satır azaltır. Doğrusu **sonuç** metrikleridir. Yaygın çerçeve DORA dörtlüsüdür: dağıtım sıklığı, değişiklik öncü süresi, değişiklik hata oranı, ortalama kurtarma süresi (`191`). İlk ikisi hızı, son ikisi kararlılığı ölçer ve dördü birlikte okunur. Yalnız hıza bakan ekip üretimi kırar, yalnız kararlılığa bakan ekip donar. Bir nokta daha önemlidir: bu sayılar kişiyi değil akışı değerlendirir. Kişi bazlı sıralama iş birliğini bitirir ve ölçümü doğrudan bozar (`195`). A ve C şıkları ölçütün türünü sorgulamadan eşik ya da sıklık ayarlar.

# sozlu7-2 | Orta
## Soru
Altı kişilik bir ekip yeni bir ürüne başlıyor. İş alanı yeni, servis sınırları henüz belirsiz. En yerinde başlangıç kararı hangisidir?
## A
Baştan sekiz mikroservisle başlamak; sonradan bölmek her zaman daha pahalıya gelir
🤖 sınır bilinmeden bölünen servisler yanlış yerde donar.
## B
Tek uygulama yazmak ama her tabloyu ayrı veri tabanına koymak; veri ayrımı sınırı hazırlar
🤖 veriyi bölmek modül sınırı üretmez, yalnız sorguları zorlaştırır.
## C
Kararı ertelemek ve mimari tartışmasını ilk üretim kesintisine kadar hiç açmamak
🤖 ayrıştırma ertelenebilir, sınır tartışması ertelenemez.
## D
Modüler monolitle başlamak; sınırı pakette çizmek, ihtiyaç kanıtlanınca ayrıştırmak
🤖 Doğru: ucuz sınır önce denenir, pahalı ayrıştırma sonra gelir.
## Doğru
D
## Açıklama
Mikroservis bir varsayılan değil, kanıtlanmış bir ihtiyaç karşılığında satın alınan pahalı bir yapıdır. Faturası ilk günden ödenir: ağ gecikmesi, kısmi başarısızlık, dağıtık veri tutarlılığı, sürüm uyumu, uçtan uca izleme ve nöbet maliyeti (`95`). İş alanı yeniyken sınırlar henüz bilinmez; bu durumda bölme kararı tahmine dayanır. Yanlış yerde çizilen sınır ise ağ üzerinden donar ve düzeltmesi çok pahalı olur. **Modüler monolit** bu yüzden doğru başlangıçtır: sınır paket düzeyinde çizilir, bağımlılık yönü tek taraflı tutulur, dağıtım tek parça kalır (`96`). Sınır burada tutuyorsa ayrıştırma sonra da yapılabilir; tutmuyorsa mikroservise geçmek sorunu çözmez, yalnız araya ağ koyar (`97`). Ekip yapısı da ölçüttür: altı kişilik ekip sekiz servisi sahiplenemez. B şıkkı veriyi böler ama kod bağımlılıklarına dokunmaz; sınır yine oluşmaz. C şıkkı sınır tartışmasını erteler, bu da kararın kendiliğinden ve rastgele verilmesi demektir.

# sozlu7-3 | Orta
## Soru
Bir ekip CI hattında "toplam test kapsamı en az yüzde seksen" kuralını zorunlu kıldı. Kısa sürede önemsiz testler çoğaldı, üretim hataları azalmadı. En yerinde düzeltme hangisidir?
## A
Eşiği kritik iş akışlarının kapsamına taşımak, toplam oranı yalnız bilgi olarak göstermek
🤖 Doğru: ölçüt riskin bulunduğu yere bağlanır.
## B
Eşiği yüzde doksan beşe çıkarmak; kapsam arttıkça üretim hataları kendiliğinden düşer
🤖 eşiği yükseltmek aynı oyunu büyütür, riski hedeflemez.
## C
Kapsam ölçümünü tamamen kaldırmak; kapsam hiçbir koşulda anlamlı bir sinyal taşımaz
🤖 kapsam kötü bir hedeftir ama işe yarar bir göstergedir.
## D
Ölçümü bırakıp test yeterliliğini yalnız kod incelemesinde gözle değerlendirmek
🤖 tamamen insana bırakılan denetim tutarsız ve yavaş kalır.
## Doğru
A
## Açıklama
Global kapsam eşiği, ölçüt hedefe dönüşünce bozulan metriklerin ders kitabı örneğidir. Ekip riskin nerede olduğunu sormadan yüzdeyi yükseltmenin en ucuz yolunu bulur: önemsiz kod parçaları test edilir, oran yükselir, üretim hatası aynı kalır. Doğru düzeltme ölçütü **riskin bulunduğu yere** taşımaktır. Ödeme, yetkilendirme, para hesabı ve veri göçü gibi kritik akışlar testsiz geçemez; bu kapı somut ve savunulabilirdir (`220`). Toplam oran ise silinmez, uyaran bir gösterge olarak kalır: trendi düşüyorsa incelemede konuşulur, ama hattı durdurmaz (`191`). C şıkkı göstergeyi büsbütün atar; oysa sorun ölçümde değil, ölçümün kapı yapılma biçimindedir. D şıkkı denetimi tamamen insana bırakır ve tutarsızlık üretir. Genel kural şudur: bir kontrol ancak bulduğu sorun pahalıysa ve sonucu güvenilirse zorunlu olur. Kararsız sonuç veren hiçbir kontrol kapı olamaz (`190`).

# sozlu7-4 | Orta
## Soru
Elde on kalem teknik borç var ve hepsi gerçek. Bu sprintte yalnız ikisine yer var. Önceliklendirme için en yerinde ölçüt hangisidir?
## A
En eski kalemden başlamak; borç ne kadar uzun beklerse etkisi o kadar büyür
🤖 yaş tek başına etki göstermez; eski borç ölü kodda olabilir.
## B
En kolay iki kalemi seçmek; hızlı kapanan işler listeyi görünür biçimde kısaltır
🤖 liste kısalır ama acı veren yer aynı kalır.
## C
Sık değişen ve sık hata çıkaran yerdeki borcu seçmek; faiz orada ödeniyor
🤖 Doğru: etki, değişim sıklığı ile acının kesişiminde toplanır.
## D
En büyük kalemi seçip o modülü baştan yazmak; parça parça iyileştirme borcu bitirmez
🤖 yeniden yazım süreyi ve riski birlikte büyütür.
## Doğru
C
## Açıklama
Teknik borçta acıtan şey anapara değil **faizdir**. Faiz, o alana her dokunduğunda ödediğin fazladan süredir ve yalnız dokunulan yerde birikir. Bu yüzden ölçüt iki eksenin kesişimidir: değişim sıklığı ve acı. Sık değişen ve sık hata çıkaran modül önce gelir, çünkü orada her sprint faiz ödenir. Hiç dokunulmayan ölü koddaki çirkinlik ise beklemeye devam edebilir; faizi sıfırdır (`132`). A şıkkı yaşı ölçüt sanır; oysa eski bir kalem tam da kimse dokunmadığı için eski kalmış olabilir. B şıkkı listeyi kısaltır ama sorunu değil sayıyı hedefler. D şıkkı büyük yeniden yazım tuzağıdır: süre ve risk birlikte büyür, eski sistem bu sırada değişmeye devam eder ve iki sürümü aynı anda taşımak gerekir. Doğrusu kademeli ilerlemektir; dokunduğun yeri önce testle çevrele, sonra iyileştir. Seçilen kalem iş diline çevrilerek savunulur: teslim süresi, hata oranı, kesinti riski (`140`, `195`).

# sozlu7-5 | İleri
## Soru
p99 gecikmeyi düşürmek için yetkilendirme kararlarının uzun süreli önbelleğe alınması öneriliyor. Bu öneri hakkında en yerinde değerlendirme hangisidir?
## A
Takas gerçektir; yetki iptali gecikir, bu yüzden süre kısaltılır ve iptal olayıyla geçersizleştirilir
🤖 Doğru: karar bir ölçüte bağlanır ve gerekçesiyle kaydedilir.
## B
Öneri reddedilir; güvenlik hiçbir koşulda performans için esnetilmeyen mutlak bir niteliktir
🤖 mutlak dil karar üretmez; her nitelik bir bütçeyle konuşulur.
## C
Öneri sorunsuzdur; yetki verisi nadiren değiştiği için önbellek süresi serbestçe uzatılabilir
🤖 nadir değişim, değişimin gecikmesini zararsız yapmaz.
## D
Önce uygulanır, sorun çıkarsa geri alınır; ölçüt tartışması gereksiz yere zaman harcar
🤖 yetki sızıntısı geri alınamaz; bu karar deneyerek öğrenilmez.
## Doğru
A
## Açıklama
Kalite nitelikleri aynı yöne çekmez; performans, güvenlik ve ölçeklenebilirlik düzenli olarak çatışır. Bu soruda çatışma nettir: önbellek gecikmeyi düşürür ama yetki değişikliğinin etkili olmasını geciktirir. Doğru yaklaşım çatışmayı inkâr etmek değil, **ölçüte bağlamaktır**. İki hedef birlikte yazılır: "p99 şu değerin altında" ve "yetki iptali en geç şu sürede etkili" (`141`). Ardından süre bu ikinci hedefe göre seçilir ve iptal olayı geldiğinde girdi elle geçersizleştirilir. B şıkkı doğru bir sezgiyi mutlak bir cümleye çevirir ve karar üretemez hâle gelir. Pazarlığa kapalı olan kimlik doğrulama ve yetkilendirmenin **varlığıdır**; esneyen şey uygulama ayrıntısıdır (`189`). C şıkkı değişimin nadir olmasını zararsız sanır; oysa tehlikeli olan tam da iptal edilmesi gereken tek yetkidir. D şıkkı geri dönülebilir karar gibi davranır; oysa sızmış bir yetkiyle yapılan işlem geri alınamaz. Karar, gerekçesi ve reddedilen seçeneklerle birlikte kaydedilir (`140`).

# sozlu7-6 | İleri
## Soru
Yeni sürüm dağıtıldıktan on dakika sonra hata oranı yirmi kat arttı. Kök neden henüz bilinmiyor. İlk adım ne olmalıdır?
## A
Kök neden bulunana kadar beklemek; anlaşılmadan yapılan müdahale durumu kötüleştirebilir
🤖 bekleme de bir karardır ve etkiyi büyütmeye devam eder.
## B
Log ve iz kayıtlarını inceleyip bir düzeltme yaması hazırlamak, sonra ileriye doğru dağıtmak
🤖 doğrulanmamış ileri düzeltme kesinti süresini uzatır.
## C
Etkilenen müşterilere duyuru yazıp gelen yanıtları toplamak, sonra teknik incelemeye geçmek
🤖 iletişim şarttır ama hafifletme adımının önüne geçmez.
## D
Son dağıtımı geri almak; önce etkiyi durdur, kök nedeni sonra anla
🤖 Doğru: geri alma en hızlı ve en öngörülebilir hafifletmedir.
## Doğru
D
## Açıklama
Olay yönetiminde sıra sabittir: **önce hafiflet, sonra anla**. Kök neden analizi kesinti sürerken değil, etki durduktan sonra yapılır. Bilinen iyi sürüme dönmek en hızlı ve sonucu en öngörülebilir adımdır; özellik bayrağını kapatmak veya trafiği eski sürüme yönlendirmek de aynı sınıftadır. A şıkkı en sık görülen hatadır: beklemek nötr değildir, etkiyi büyüten bir karardır. B şıkkı ileriye doğru düzeltme önerir; hazırlanan yama denenmemiştir ve ikinci bir olay üretebilir. İleri düzeltme yalnız geri almanın mümkün olmadığı durumlarda seçilir, örneğin geri alınamayan bir şema göçü varsa. C şıkkı iletişimi öne alır; iletişim zorunludur ama hafifletmeyle paralel yürür, onun yerine geçmez. Etki durduktan sonra sıra rollere, zaman çizelgesine ve suçsuz postmortem'e gelir. Postmortem'in sorusu "kim yaptı" değil, "bu değişiklik neden bu kadar kolay üretime çıktı" olmalıdır (`195`, `192`).
