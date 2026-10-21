---
slug: sozlu-prova-kalite-uretim
baslik: "Sözlü Prova · Kalite, Süreç ve Üretim: Test Stratejisi, Kod İnceleme, Kesinti ve SQL"
kademe: İleri
sira: 265
etiketler: 11
---

## Konu anlatımı

Bu dosyadaki beş soru gerçek mülakatlarda soruldu. İlk soru diğerlerinden ayrı duruyor: getirilen yedi soru setinin beşinde birden çıktı. Bu yüzden prova zamanının en büyük payını ona ayır. En sık tekrarlanan soru, en çok hazırlık isteyen sorudur. Beşinin ortak yanı şudur: hepsi süreç sorusu gibi görünür, aslında karar sorusudur. Görüşmeci senden bir liste değil, bir tercih ve gerekçe bekler. Her sorunun altında dört parça var. Önce sorunun ne ölçtüğü, sonra sözlü cevabın adım adım iskeleti gelir. Ardından elenme sebepleri ve kendi hikâyeni yerleştireceğin boşluk gelir. Dosyanın sonunda bu malzemeyi nasıl prova edeceğin anlatılır.

### "CI/CD hattında test stratejiniz nedir; birim, entegrasyon ve sözleşme testlerini nasıl konumlandırırsınız?"

**Soru aslında ne ölçüyor?**

Bu soruda üçü de var, ama ağırlık açıkça **muhakemededir**. Piramidi çizmek kolaydır ve neredeyse her aday çizer. Ayrıştırıcı olan şey, her katmanın **hangi soruyu cevapladığını** söyleyebilmektir. İkinci ayrıştırıcı hattın kendisidir: hangi test hangi aşamada koşar ve neden orada koşar. Görüşmeci burada senin hız ile güven arasındaki takası nasıl kurduğunu dinler. Sözleşme testi (contract test) kısmı ise sessiz bir filtredir. Çoğu aday adını duymuştur, ama ne zaman gerektiğini söyleyemez. Bu soru gerçek mülakatlarda en sık tekrarlanan sorudur; iskeleti akıcı hâle gelene kadar tekrarla.

**Sözlü cevap iskeleti**

1. Çerçeveyi ilk cümlede kur: test stratejisi bir piramit resmi değil, bir **geri bildirim hızı** kararıdır. Her katman farklı bir soruya cevap verir ve farklı bir bedel öder.
2. Katmanları isimle değil **soruyla** tanımla: birim testi kuralın doğru mu diye sorar. Entegrasyon testi parçalar gerçek altyapıyla birleşince çalışıyor mu diye sorar. Sözleşme testi iki servis aynı sözleşmeyi mi anlıyor diye sorar. Uçtan uca test kritik iş akışı ayakta mı diye sorar.
3. Hattın aşamalarını anlat, çünkü sorunun asıl gövdesi budur. Her commit'te derleme, birim testler ve hızlı entegrasyon testleri koşar; hedef birkaç dakikadır. Ana dala birleşme sonrasında daha geniş entegrasyon ve sözleşme testleri koşar. Uzun süren uçtan uca ve yük testleri gece koşumuna bırakılır.
4. Sözleşme testinin **ne zaman gerektiğini** söyle. İki servis ayrı ekiplerdeyse, ayrı depolardaysa ve ayrı takvimle yayınlanıyorsa gerekir. Tüketicinin (consumer) beklentisi kayda alınır ve sağlayıcının (provider) hattında koşturulur. Böylece kırılma üretimde değil, sağlayıcı yayınlamadan önce görünür. Tek ekip tek yayın yapıyorsa entegrasyon testi çoğu zaman yeter; gereksiz sözleşme testi bakım yükü üretir.
5. Entegrasyon testinde **gerçek** bağımlılık kullandığını söyle. Veritabanını taklit etmek yanlış güven verir; kısıtlar, sorgu davranışı ve göç betikleri sınanmadan kalır. Testcontainers gibi bir araçla gerçek veritabanı test için ayağa kaldırılır.
6. Hattın hızlı ve güvenilir kalmasını nasıl koruduğunu söyleyerek bitir. Kararsız test (flaky test) karantinaya alınır, sahibi ve tarihi olur; sessizce yeniden koşturmak yasaktır. Kapsam yüzdesini hedef yapmak yerine kritik iş yolunun kapsandığını doğrularsın.

Bir cümle daha eklersen fark yaratırsın. Sözleşme testinin değeri, kırılmayı **hangi tarafın** hattında gösterdiğidir. Tüketicinin kendi hattında kurduğu taklit yalnız kendi varsayımını sınar. Sağlayıcı bir alanı değiştirdiğinde o taklit sessizce eskir ve kimse fark etmez.

**Elenme sebepleri**

- Piramidi ezberden çizip her katmanın hangi soruyu cevapladığını söyleyememek: ezber izlenimi bırakır.
- Sözleşme testini uçtan uca testin başka bir adı sanmak: terimi duyduğu ama kullanmadığı anlaşılır.
- Sözleşme testini her yerde şart koşmak: tek ekipte gereksiz bakım yükü olduğu görülmemiş demektir.
- Her commit'te bütün test kümesini koşturmak: hat yavaşlar, geliştirici beklemeyi bırakır, testler baypas edilir.
- Kararsız testi yeniden koşturup geçince kapatmak: hattın verdiği sinyal tümüyle değersizleşir.
- Kapsam yüzdesini kalitenin ölçüsü saymak: yüksek kapsamın boş iddia üretebildiği bilinmiyor demektir.
- Entegrasyon testinde veritabanını taklit etmek: kısıtlar ve gerçek sorgu davranışı hiç sınanmaz.
- Testleri yayından sonra elle koşulan bir aşamaya bırakmak: hattın kapı olma özelliği kaybolur.

**Kendi hikâyen**

Buraya senin örneğin gelecek. Şu sorularla kendi vakanı çıkar:

- Çalıştığın hatta bir koşum kaç dakika sürüyordu ve bu süreyi düşürmek için ne yaptın?
- Üretime kadar giden bir kırılma oldu mu; hangi test katmanı olsaydı onu daha erken yakalardı?
- İki servis arasında bir sözleşme kırılması yaşadın mı, kırılmayı ilk fark ettiren şey neydi?
- Kararsız bir testle nasıl baş ettin; karantinaya aldın mı, sonunda kök nedeni buldun mu?
- Entegrasyon testinde gerçek veritabanına ne zaman geçtiniz, öncesinde neyi kaçırıyordunuz?
- Hattaki bir kapı sayesinde durdurulan bir yayın oldu mu, o kapıyı kim koymuştu?

Bu deneyimlerin hiçbiri yoksa uydurma; onun yerine dürüst köprü kalıbını kur, yani sınırı söyle, komşu deneyimini somutla ve farkı bildiğini göster. Kalıbın tamamı `256` numaralı konudadır. İçerik derinliği için `187`, `188`, `90`, `220`, `261` ve ön yüz ekibiyle sözleşme tarafı için `213` numaralı konulara bak.

```svg
<svg viewBox="0 0 660 326" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Test katmanlari hangi soruyu cevaplar ve hatta hangi asamada kosar">
  <defs>
    <marker id="kalur-ok1" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Test stratejisi: hangi soru, hangi aşama</text>
  <text x="172" y="40" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Katman hangi soruyu cevaplar</text>
  <text x="495" y="40" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Hatta hangi aşamada koşar</text>
  <rect x="14" y="50" width="316" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Birim testi</text>
  <text x="172" y="88" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kural doğru mu? · saniyeler sürer · en ucuz</text>
  <rect x="14" y="106" width="316" height="50" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="172" y="126" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Entegrasyon testi</text>
  <text x="172" y="144" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">parçalar gerçek altyapıyla çalışıyor mu?</text>
  <rect x="14" y="162" width="316" height="50" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="172" y="182" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Sözleşme testi</text>
  <text x="172" y="200" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iki servis aynı sözleşmeyi mi anlıyor?</text>
  <rect x="14" y="218" width="316" height="50" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="172" y="238" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Uçtan uca test</text>
  <text x="172" y="256" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kritik iş akışı ayakta mı? · pahalı, kırılgan</text>
  <rect x="344" y="50" width="302" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="495" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Her commit</text>
  <text x="495" y="88" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">derleme + birim + hızlı entegrasyon</text>
  <text x="495" y="105" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hedef: birkaç dakika</text>
  <line x1="495" y1="116" x2="495" y2="122" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok1)"/>
  <rect x="344" y="124" width="302" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="144" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Ana dala birleşme sonrası</text>
  <text x="495" y="162" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geniş entegrasyon + sözleşme testleri</text>
  <text x="495" y="179" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gerçek veritabanı ile koşar</text>
  <line x1="495" y1="190" x2="495" y2="196" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok1)"/>
  <rect x="344" y="198" width="302" height="66" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="495" y="218" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Gece koşumu</text>
  <text x="495" y="236" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uçtan uca, yük ve uzun süren testler</text>
  <text x="495" y="253" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gündüz hattını yavaşlatmaz</text>
  <rect x="14" y="276" width="632" height="40" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="294" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kararsız test karantinaya alınır: sahibi ve tarihi olur</text>
  <text x="330" y="310" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sessizce yeniden koşturmak hattın verdiği sinyali değersizleştirir</text>
</svg>
```

### "Kod inceleme süreciniz nasıl işler; kalite, güvenlik ve standartları nasıl tutarlı tutarsınız?"

**Soru aslında ne ölçüyor?**

Burada ağırlık **deneyimde** ve çalışma tarzındadır. Teknik cevabı vermek kolaydır; görüşmeci senin insan tarafını dinler. Sorunun içinde iki ayrı yarı var. Birinci yarı tutarlılıktır: standardı nasıl herkes için aynı yapıyorsun? İkinci yarı ise incelemenin **neye** ayrıldığıdır. Güçlü adayı ayıran cümle şudur: otomatikleştirilebilen hiçbir şey incelemede tartışılmaz. Görüşmeci ayrıca senin geri bildirim dilini merak eder, çünkü bu ekip içi sürtünmeyi doğrudan belirler.

**Sözlü cevap iskeleti**

1. Ayrımı ilk cümlede koy: makinenin karar verebileceği hiçbir konu insan incelemesine kalmaz. Biçim, içe aktarma sırası ve basit kod kokusu araca aittir.
2. Aracın kapsamını say: biçimlendirici (formatter), statik analiz (static analysis), bağımlılık zafiyet taraması ve gizli anahtar taraması; bunlar hattın zorunlu kapılarıdır, tartışma konusu değildir.
3. İnsanın neye baktığını somut say: isimlendirme ve niyet, sınır durumları, hata yolları, testin varlığı ve doğru şeyi sınayıp sınamadığı, yetki kontrolü, geriye uyumluluk.
4. Geri bildirim dilini anlat: kod hakkında konuş, kişi hakkında değil. Emir yerine soru kur; "burada değer boş gelirse ne olur" cümlesi "bunu düzelt"ten daha iyi çalışır.
5. Engelleyen yorumla öneriyi ayır ve her yoruma bir etiket koy: engelleyici, öneri, soru ya da not. Etiketsiz yorum yazarı gereksiz yere kilitler.
6. Süreci hızlı tutan tek kuralı söyle: küçük PR. Büyük PR'da inceleme "iyi görünüyor" cevabına döner. Tekrarlanan tartışmayı da yazılı bir karara bağla, böylece aynı konu her PR'da yeniden açılmaz.

**Elenme sebepleri**

- Biçim tartışmasını incelemenin merkezine koymak: araçla çözülebilecek işe insan zamanı harcandığı görülür.
- İncelemeyi tek bir kıdemli kişiye devretmek: darboğaz oluşur ve bilgi tek noktada toplanır.
- "Kodunu beğenmedim" gibi kişiye dönük dil kullanmak: incelemenin savunma refleksi ürettiği anlaşılır.
- Her yorumu engelleyici gibi yazmak: küçük bir öneri yüzünden teslimat günlerce bekler.
- Güvenlik ve yetki kontrolünü hiç anmamak: incelemenin yalnız okunabilirlik sanıldığı görülür.
- Büyük PR'ları normal saymak: incelemenin biçimsel bir onaya döndüğü anlaşılır.
- Statik analiz bulgularını uyarı olarak bırakıp hiç kapı yapmamak: uyarılar birikir ve kimse okumaz.

**Kendi hikâyen**

Şu sorularla kendi vakanı çıkar:

- İncelemede tekrar tekrar aynı konuyu tartışıyor muydunuz; bunu neyle çözdün?
- Hangi aracı hatta zorunlu kapı yaptın ve bu karar ekipte nasıl karşılandı?
- Bir incelemede yakaladığın en değerli bulgu neydi, üretime gitseydi ne olurdu?
- Geri bildirim yüzünden gerilen bir durum yaşadın mı, dili nasıl değiştirdin?
- PR boyutunu küçültmek için bir kural koydunuz mu, inceleme süresi ne oldu?

Deneyimin yoksa uydurma; `256` numaralı konudaki dürüst köprü kalıbını kur. Derinleşmek için `132`, `190`, `191` ve yapay zekâ destekli inceleme tarafı için `207` numaralı konulara bak.

### "Üretimde kritik kesinti olduğunda olay yönetimini ve kök neden analizini nasıl organize edersiniz?"

**Soru aslında ne ölçüyor?**

Bu soruda ağırlık **deneyimdedir**, çünkü kesinti yaşamamış aday sıralamayı doğru kuramaz. Ölçülen asıl şey baskı altında öncelik verebilmektir. Ayrıştırıcı cümle çok kısadır: önce durdur, sonra anla. Çoğu aday doğrudan kök nedene atlar; bu, kullanıcı etkisi sürerken teşhis yapmak demektir. İkinci ayrıştırıcı kapanış tarafındadır: suçsuz postmortem (blameless postmortem) ve sahibi olan eylem maddeleri, süreci gerçekten yaşamış birinin ayrıntısıdır.

**Sözlü cevap iskeleti**

1. Sırayı ilk cümlede ver: önce etkiyi durdur, kök nedeni sonra ara. Kesinti anında amaç öğrenmek değil, kullanıcıyı kurtarmaktır.
2. Hafifletme seçeneklerini say ve **geri almayı** en üste koy: yayını geri al, özellik bayrağını (feature flag) kapat, trafiği sağlıklı sürüme çevir. Geri alma en öngörülebilir hamledir; acele yazılan ileri düzeltme doğrulanmamıştır.
3. Rolleri ayır: olay komutanı (incident commander) karar verir, bir kişi iletişimi yürütür, teknik kişiler tek konuya bakar. Herkesin aynı loglara aynı anda bakması olayı uzatır.
4. İletişim ritmini söyle: sabit aralıkla durum güncellemesi verilir ve bilinenle bilinmeyen ayrılır. Sessizlik paydaşı en çok yoran şeydir, teknik kayıptan büyük güven kaybı üretir.
5. Zaman çizelgesini **olay sırasında** tut: kim ne zaman ne yaptı, hangi metrik ne zaman döndü. Sonradan hatırlanarak yazılan çizelge güvenilmez olur.
6. Kapanışı suçsuz postmortem ile anlat; soru "kim yaptı" değil, "bu değişikliğin üretime çıkmasına ne izin verdi" olur. Her eylem maddesinin sahibi ve tarihi bulunur; sahipsiz madde rafta kalır.

**Elenme sebepleri**

- Kesinti anında doğrudan kök neden aramaya girişmek: etki sürerken teşhis yapılır, zarar büyür.
- Geri almayı hiç düşünmeyip acele ileri düzeltme yazmak: doğrulanmamış yama olayı daha da uzatır.
- Rol dağılımı yapmadan herkesin aynı işe bakması: karar çıkmaz ve iş tekrarlanır.
- Paydaşa haber vermeyip "önce çözelim" demek: güven kaybı teknik kayıptan ağır olur.
- Postmortem'de kişi adı geçirmek: bir sonraki olayda kimse erken haber vermez.
- Eylem maddelerini sahipsiz ve tarihsiz bırakmak: aynı kesinti bir süre sonra tekrarlanır.
- Olayı "insan hatası" diye kapatmak: hatayı mümkün kılan sistem hiç sorgulanmaz.
- Kesintiyi ilk fark edenin müşteri olmasını normal saymak: gözlemlenebilirlik eksiği kabullenilmiş demektir.

**Kendi hikâyen**

Şu sorularla kendi vakanı çıkar:

- Yaşadığın kesintide ilk hamlen ne oldu, geri alma seçeneğiniz var mıydı?
- Olayı ilk fark ettiren şey neydi: alarm mı, müşteri araması mı? Bu ayrım güçlü bir sinyaldir.
- Etki kaç dakikada durdu, kök neden kaç saatte bulundu, aradaki fark ne kadardı?
- Postmortem yapıldı mı; çıkan eylem maddelerinden kaçı gerçekten kapandı?
- O olaydan sonra hangi kalıcı adım eklendi: bir alarm mı, bir test mi, bir hat kapısı mı?

Deneyimin yoksa dürüst köprüyü kur (`256`). İçerik derinliği için `195`, `192` ve gözlemlenebilirlik tarafı için `193` ile `104` numaralı konulara bak.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Olay yonetimi sirasi, roller ve yanlis sira">
  <defs>
    <marker id="kalur-ok2" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kesintide sıra: önce durdur, sonra anla</text>
  <rect x="14" y="36" width="118" height="88" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="73" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">1 · Tespit</text>
  <text x="73" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">alarm mı,</text>
  <text x="73" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">müşteri mi?</text>
  <text x="73" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">şiddet belirle</text>
  <line x1="134" y1="80" x2="142" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok2)"/>
  <rect x="144" y="36" width="118" height="88" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="203" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">2 · Hafiflet</text>
  <text x="203" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yayını geri al</text>
  <text x="203" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bayrağı kapat</text>
  <text x="203" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">trafiği çevir</text>
  <line x1="264" y1="80" x2="272" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok2)"/>
  <rect x="274" y="36" width="118" height="88" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="333" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · Anla</text>
  <text x="333" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">log · metrik · iz</text>
  <text x="333" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zaman çizelgesi</text>
  <text x="333" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tek değişken</text>
  <line x1="394" y1="80" x2="402" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok2)"/>
  <rect x="404" y="36" width="118" height="88" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="463" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">4 · Kalıcı düzelt</text>
  <text x="463" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kök nedeni</text>
  <text x="463" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapatan değişiklik</text>
  <text x="463" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">test + alarm</text>
  <line x1="524" y1="80" x2="532" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok2)"/>
  <rect x="534" y="36" width="112" height="88" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="590" y="58" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">5 · Postmortem</text>
  <text x="590" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">suçsuz dil</text>
  <text x="590" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sahip + tarih</text>
  <text x="590" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">takibi yapılır</text>
  <rect x="14" y="140" width="316" height="88" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Roller ayrılmazsa olay uzar</text>
  <text x="172" y="180" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">olay komutanı karar verir</text>
  <text x="172" y="198" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bir kişi iletişimi yürütür</text>
  <text x="172" y="216" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">teknik kişiler tek konuya bakar</text>
  <rect x="340" y="140" width="306" height="88" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="493" y="160" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">Sık yapılan hata: önce anlamaya çalışmak</text>
  <text x="493" y="180" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kullanıcı etkisi sürerken teşhis yapılır</text>
  <text x="493" y="198" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geri alma seçeneği hiç konuşulmaz</text>
  <text x="493" y="216" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">olay dakikalar yerine saatler sürer</text>
  <rect x="14" y="240" width="632" height="48" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="260" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Postmortem çıktısı bir liste değil, bir taahhüttür</text>
  <text x="330" y="278" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her eylem maddesinin sahibi ve tarihi olur; sahipsiz madde rafta kalır</text>
</svg>
```

### "SQL sorgu performansını artırmak için hangi yöntemleri kullanırsınız?"

**Soru aslında ne ölçüyor?**

Görünüşte teknik bir bilgi sorusudur, ama içinde gizli bir **yöntem** sorusu vardır. Görüşmeci senin tahmin mi ettiğini yoksa ölçtüğünü mü anlamak ister. Bu yüzden ilk cümlen belirleyicidir. "Önce yürütme planına bakarım" diye başlayan cevap ile "indeks eklerim" diye başlayan cevap aynı ligde değildir. İkinci ölçülen şey bedel bilincidir. İndeks eklemenin bir faturası olduğunu söyleyen aday, konuyu ezberden değil pratikten bildiğini gösterir.

**Sözlü cevap iskeleti**

1. Yöntemi ilk cümlede ver: sorguyu değiştirmeden önce **yürütme planına** (execution plan) bakarım; plan, tarama mı arama mı yapıldığını ve hangi indeksin seçildiğini söyler. Tahmini satır sayısı ile gerçek satır sayısı arasındaki büyük fark, istatistiklerin eskidiğini gösterir.
2. İndeksin işe yaraması için **seçicilik** (selectivity) gerektiğini söyle. Tablonun yarısını döndüren bir kolonda indeks çoğu zaman seçilmez, çünkü tarama daha ucuzdur.
3. Bileşik indekste **kolon sırasının** belirleyici olduğunu anlat. Eşitlik koşullu kolonlar başa, aralık ve sıralama kolonu sona konur. Aralık kolonu başa alınırsa sonraki kolonlar etkin biçimde kullanılamaz.
4. Kapsayan indeksi (covering index) anlat: sorgunun ihtiyaç duyduğu tüm kolonlar indekste varsa tabloya hiç gidilmez. `SELECT *` bu kazancı doğrudan yok eder, çünkü indekste olmayan kolonları da ister.
5. İki yaygın tuzağı say; birincisi kolonu fonksiyona sokmaktır, indeks kolonun ham değerini tutar, fonksiyonun sonucunu değil. İkincisi derin `OFFSET` sayfalamasıdır; atlanan satırlar yine üretilir, bu yüzden imleç tabanlı sayfalamaya geçilir.
6. Bedeli söyleyerek bitir: indeks bedava değildir, çünkü her yeni indeks yazma maliyeti ve yer maliyeti ekler. Bu yüzden ölç, değiştir, sonra tekrar ölç; iyileştirmeyi rakamla kapat.

**Elenme sebepleri**

- "İndeks eklerim" deyip yürütme planından hiç söz etmemek: ölçmeden değiştiren bir yaklaşım okunur.
- Her kolona indeks açmayı çözüm saymak: yazma maliyeti ve bakım yükü görülmüyor demektir.
- Bileşik indekste kolon sırasını önemsiz sanmak: en sık ve en pahalı yanlış budur.
- `SELECT *` alışkanlığını savunmak: kapsayan indeks kazancının bilinmediği anlaşılır.
- `WHERE` içinde kolonu fonksiyona sokmanın indeksi düşürdüğünü bilmemek: temel bir boşluktur.
- Derin sayfalamada `OFFSET` maliyetini sıfır sanmak: büyük tabloda sayfa ilerledikçe sorgu yavaşlar.
- İyileştirmeyi ölçmeden "hızlandı" demek: sonuç iddiası doğrulanamaz kalır.
- Küçük bir test tablosunda ölçüp sonucu üretime genellemek: plan veri hacmine göre değişir.

**Kendi hikâyen**

Şu sorularla kendi vakanı çıkar:

- Hangi sorgu seni gerçekten uğraştırdı, şikâyet nereden geldi: kullanıcıdan mı, alarmdan mı?
- Yürütme planında seni şaşırtan ne oldu, beklediğin indeks seçilmiş miydi?
- Hangi indeksi ekledin ya da kaldırdın, süre kaç milisaniyeden kaça indi?
- Sorguyu mu değiştirdin, şemayı mı, yoksa erişim desenini mi? Neden o yolu seçtin?
- Sayfalama yüzünden yavaşlayan bir ekran yaşadın mı, çözümü ne oldu?

Deneyimin yoksa uydurma; dürüst köprüyü kur (`256`). Derinleşmek için `29`, `146`, `227` ve JPA tarafındaki sorgu sayısı sorunu için `82` numaralı konulara bak.

```svg
<svg viewBox="0 0 660 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="SQL sorgu iyilestirme sirasi ve indeksi ise yaramaz kilan tuzaklar">
  <defs>
    <marker id="kalur-ok3" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="20" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Önce plana bak, sonra değiştir</text>
  <rect x="14" y="34" width="146" height="92" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="87" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 · Plana bak</text>
  <text x="87" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">EXPLAIN çıktısı</text>
  <text x="87" y="93" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tarama mı arama mı</text>
  <text x="87" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tahmini ↔ gerçek satır</text>
  <line x1="162" y1="80" x2="170" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok3)"/>
  <rect x="176" y="34" width="146" height="92" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="249" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">2 · Seçicilik</text>
  <text x="249" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">az satır döndüren</text>
  <text x="249" y="93" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kolon indekse değer</text>
  <text x="249" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yarısını dönerse taranır</text>
  <line x1="324" y1="80" x2="332" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok3)"/>
  <rect x="338" y="34" width="146" height="92" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="411" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Kolon sırası</text>
  <text x="411" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eşitlik kolonları önce</text>
  <text x="411" y="93" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aralık ve sıralama sonra</text>
  <text x="411" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sıra yanlışsa indeks yarım</text>
  <line x1="486" y1="80" x2="494" y2="80" stroke="#5D7789" stroke-width="1.5" marker-end="url(#kalur-ok3)"/>
  <rect x="500" y="34" width="146" height="92" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="573" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">4 · Kapsayan indeks</text>
  <text x="573" y="76" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gerekli kolonlar indekste</text>
  <text x="573" y="93" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tabloya hiç gidilmez</text>
  <text x="573" y="112" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">SELECT * bunu bozar</text>
  <text x="330" y="146" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">İndeksi işe yaramaz kılan tuzaklar</text>
  <rect x="14" y="156" width="206" height="64" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="117" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Kolonu fonksiyona sokmak</text>
  <text x="117" y="194" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">UPPER(eposta) = ? koşulu</text>
  <text x="117" y="211" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">indeks ham değeri tutar</text>
  <rect x="228" y="156" width="206" height="64" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="331" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Derin OFFSET sayfalama</text>
  <text x="331" y="194" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">atlanan satırlar yine üretilir</text>
  <text x="331" y="211" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">imleç tabanlı sayfalamaya geç</text>
  <rect x="442" y="156" width="204" height="64" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="544" y="176" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Eski istatistikler</text>
  <text x="544" y="194" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">plan yanlış tahmine kurulur</text>
  <text x="544" y="211" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tahmin ↔ gerçek farkına bak</text>
  <rect x="14" y="232" width="632" height="46" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="251" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">İndeks bedava değildir</text>
  <text x="330" y="269" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her indeks yazma ve yer maliyeti ekler · ölç, değiştir, tekrar ölç</text>
</svg>
```

### "Son projenizde karşılaştığınız en zor hata neydi, nasıl teşhis edip çözdünüz?"

**Soru aslında ne ölçüyor?**

Bu tümüyle bir **hikâye sorusudur**, ama ölçtüğü şey hikâye değildir. Görüşmeci hatanın ne kadar zor olduğuyla değil, senin problemi nasıl daralttığınla ilgilenir. En güçlü sinyal yöntem cümleleridir. "Önce kodu okumadım, çıktıya baktım" ya da "önce hatayı deterministik hâle getirdim" gibi cümleler tek başına puan getirir. İkinci ölçülen şey kapanıştır: kalıcı bir adım eklenmemişse hikâye yarım kalır. Aynı hata tekrar edebilir durumdaysa, çözüm sayılmaz.

**Sözlü cevap iskeleti**

1. **Belirtiyle** başla, teşhisle değil: ne görüldü, kim gördü, ne zaman ve ne sıklıkta görüldü.
2. Yeniden üretmeyi anlat; rastgele görünen bir hatayı deterministik hâle getirmek işin en zor ve en değerli kısmıdır. Hangi girdi, hangi zamanlama, hangi eşzamanlılık koşulu tetikliyordu?
3. Neye baktığını söyle: log, metrik, iz, yığın dökümü ya da profilleyici. Önemli olan araç adı değil, hangi çıktının hangi ihtimali elediğidir.
4. Hipotezleri sırayla denediğini ve her seferinde **tek değişken** değiştirdiğini söyle. Aynı anda iki şey değişirse hangisinin işe yaradığı bilinmez.
5. Kök nedeni tek cümlede ver; burada uzun anlatım değil kısalık etkiler, çünkü kök nedeni tek cümleye sığdıramamak onu tam anlamadığını gösterir.
6. Düzeltmeyi ve **tekrarını önleyen kalıcı adımı** söyleyerek bitir: kök nedeni yakalayan bir test, bir alarm ya da bir hat kapısı ekle. Mümkünse sonucu bir ölçüyle kapat.

**Elenme sebepleri**

- Hikâyeyi teşhisle başlatmak: dinleyici bağlamı kuramaz ve anlatı havada kalır.
- "Sonunda kendiliğinden düzeldi" demek: kök nedenin hiç bulunmadığı anlaşılır.
- Yöntem cümlesi kurmadan yalnız sonucu anlatmak: şansla mı çözüldüğü belli olmaz.
- Kalıcı adımdan söz etmemek: aynı hatanın tekrar edebileceği kabul edilmiş olur.
- Suçu başka ekibe ya da kişiye yıkmak: ekip içi çalışma tarzı hakkında kötü sinyal verir.
- Aynı anda birçok şey değiştirip düzelince durmak: hangi değişikliğin çözdüğü bilinmez.
- Hatanın zorluğunu abartıp doğrulanamaz iddia kurmak: ayrıntı sorulduğunda anlatı çöker.
- Etki ve süre vermemek: kaç kullanıcı, kaç saat, ne kadar kayıp sorusu boşta kalır.

**Kendi hikâyen**

Bu sorunun tamamı senin hikâyendir; iskelet yalnız sıradır. Şu sorularla malzemeyi çıkar:

- Hangi hata seni en çok uğraştırdı; belirti tam olarak neydi ve ilk kim fark etti?
- Hatayı istediğin an tekrar üretebilir hâle geldin mi, bunu nasıl başardın?
- Hangi çıktı seni doğru yola soktu: log mu, metrik mi, yığın dökümü mü?
- Yanlış çıkan bir hipotezin oldu mu; onu neye bakarak eledin?
- Kök neden tek cümleyle nasıl ifade edilir? Bunu yazıp yüksek sesle söyle.
- Düzeltmeden sonra hangi kalıcı adımı ekledin ve o adım sonradan işe yaradı mı?

Anlatacak bir vakan yoksa uydurma. Bunun yerine küçük ölçekli ama gerçek bir hata seç ve yöntemi onun üzerinden anlat. Yöntem doğruysa vakanın küçük olması sorun değildir. Dürüst köprü kalıbı `256` numaralı konudadır; olay yönetimi tarafı için `195` numaralı konuya bak.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, **sesli çalışılmak** için yazıldı. Sırayı bozma:

1. Soru başlığını oku, sonra iskeleti **görmeden** kapat ve cevabı yüksek sesle ver; içinden geçirmek prova sayılmaz.
2. Süre tut ve 90 saniyeyi hedefle; çok kısa cevap yüzeysel, çok uzun cevap dağınık algılanır.
3. Cevabını telefonla kaydet ve sonra dinle; kendi sesini dinlemek en hızlı geri bildirimdir.
4. Şimdi iskeletle karşılaştır ve hangi adımı hiç söylemediğini, hangisini sıradan önce söylediğini işaretle.
5. Elenme sebeplerini tek tek gözden geçir ve kendi cevabında bunlardan birinin geçip geçmediğine bak.
6. "Kendi hikâyen" sorularını cevapla ve çıkan malzemeyi tek paragraf hâlinde yaz; yazmadan ezberlemeye çalışma.
7. Ertesi gün aynı soruyu iskelete hiç bakmadan tekrarla; ikinci turda hedef süre 75 saniyedir.

Bu dosyada bir öncelik kuralı daha var. İlk soru en sık tekrarlanan sorudur, bu yüzden ona iki kat prova ayır. İskeleti kelimesi kelimesine ezberleme, çünkü ezberlenmiş cevap tonundan belli olur. Ezberlenecek şey cümleler değil **sıradır**; sıra aklında kalırsa cümleleri o an rahatça kurarsın.

### Tek cümlede

Bu beş soruda kazanan cevap süreç adı sayan değil, sırayı ve gerekçeyi kuran cevaptır: testleri geri bildirim hızına göre yerleştirmek, otomatikleştirilebileni insana bırakmamak, kesintide önce durdurup sonra anlamak, sorguyu değiştirmeden önce plana bakmak ve hata hikâyesini kalıcı bir adımla kapatmak.

# sozlu4-1 | Temel
## Soru
Bir ekibin hattında yalnız uçtan uca testler var. Her commit'te kırk dakika koşuyorlar ve sık kırılıyorlar. Birim ve entegrasyon testi yazılmıyor. Bu tablonun en doğru okuması hangisidir?
## A
Uçtan uca test tüm akışı kapsadığı için alt katmanlara gerek yoktur
🤖 kapsamak ile teşhis etmek ayrı şeydir; kırılan test hatanın yerini söylemez.
## B
Uçtan uca testlerin bir kısmını gece koşumuna alıp hattı hızlandırmak yeterlidir
🤖 süre düşer ama geri bildirim daha da gecikir, katman eksikliği yerinde kalır.
## C
Sorun test sayısıdır, sayı azaltılırsa hat hızlanır ve kırılmalar da azalır
🤖 sayıyı azaltmak güveni azaltır; buradaki sorun sayı değil katman dağılımıdır.
## D
Geri bildirim geç gelir ve kırılan test hatanın yerini göstermez
🤖 Doğru: alt katmanlar hem hızlı hem de teşhis edici geri bildirim verir.
## Doğru
D

## Açıklama
Her test katmanı **farklı bir soruya** cevap verir ve farklı bir bedel öder. Birim testi kuralın doğru olup olmadığını saniyeler içinde söyler ve hatanın yerini de gösterir. Entegrasyon testi parçaların gerçek altyapıyla birleşince çalışıp çalışmadığını sorar. Uçtan uca test ise yalnız "kritik iş akışı ayakta mı" sorusuna cevap verir; pahalıdır, yavaştır ve kırılgandır (`187`). Yalnız uçtan uca teste dayanan bir hatta iki sorun birden çıkar. Birincisi **geç geri bildirimdir**: geliştirici kırk dakika bekler, bu sürede başka işe geçer ve bağlam kaybolur. İkincisi **teşhis edememektir**: test kırıldığında hatanın hangi katmanda olduğu belli değildir, çünkü akışın tamamı sınanmıştır. A şıkkı kapsamayı teşhisle karıştırır. B şıkkı testleri geceye taşır; koşum süresi düşer ama geri bildirim ertesi güne kayar. C şıkkı sayıyı sorun sanır; oysa aynı sayıda testin alt katmanlara dağıtılması hem hızı hem teşhisi düzeltir (`90`, `220`).

# sozlu4-2 | Orta
## Soru
İki ekip ayrı depolarda çalışıyor ve servisleri farklı takvimlerle yayınlıyor. Sağlayıcı bir yanıt alanının adını değiştirdi, kırılma ancak üretimde görüldü. Bunu yayından önce yakalayacak en doğru önlem hangisidir?
## A
Tüketici ekibin kendi hattında sağlayıcıyı taklit eden testleri çoğaltmak
🤖 taklit yalnız senin varsayımını sınar; sağlayıcı değişince taklit sessizce eskir.
## B
Tüketicinin beklentisini kayda alıp sağlayıcının hattında sözleşme testi koşturmak
🤖 Doğru: kırılma sağlayıcı yayınlamadan önce, onun hattında görünür.
## C
Her iki servisi kapsayan uçtan uca testleri gece koşumuna eklemek
🤖 sinyal geç gelir ve hangi alanın kırıldığını doğrudan söylemez.
## D
Sağlayıcı ekibin değişikliği sürüm notlarında duyurmasını zorunlu kılmak
🤖 iletişim yardımcı olur ama denetimi insana bırakır, otomatik kapı yoktur.
## Doğru
B

## Açıklama
Sözleşme testinin değeri, kırılmayı **hangi tarafın hattında** gösterdiğidir. Tüketici kendi hattında bir taklit kurarsa yalnız kendi varsayımını sınar. Sağlayıcı alan adını değiştirdiğinde o taklit eskimiş olur, ama testler yeşil kalır ve kimse fark etmez. Sözleşme testinde ise tüketicinin beklentisi bir kayda dönüşür ve bu kayıt **sağlayıcının hattında** koşturulur. Sağlayıcı alanı değiştirdiği anda kendi hattı kırılır; kırılma üretime hiç ulaşmaz (`213`). Bu yaklaşım özellikle servisler ayrı ekiplerde, ayrı depolarda ve ayrı takvimlerle yayınlanıyorsa gerekir. Tek ekip tek yayın yapıyorsa entegrasyon testi çoğu zaman yeter ve gereksiz sözleşme testi bakım yükü üretir (`187`). C şıkkı uçtan uca teste güvenir; sinyal geç gelir, kırılgandır ve hangi alanın değiştiğini söylemez. D şıkkı iletişimi kural yapar; iletişim faydalıdır ama unutulabilir, hattaki otomatik kapının yerini tutmaz (`220`).

# sozlu4-3 | Orta
## Soru
Bir ekipte PR yorumlarının çoğu boşluk, içe aktarma sırası ve parantez biçimi üzerine. İncelemeler uzuyor, mimari ve güvenlik konuları tartışılmadan geçiyor. En doğru düzeltme hangisidir?
## A
Biçimi araçla hatta zorunlu kılmak, insan incelemesini niyete ve riske ayırmak
🤖 Doğru: otomatikleştirilebilen tartışılmaz, insan zamanı riskli alana kalır.
## B
İnceleme başına yorum sayısına üst sınır koyup fazla yorumları elemek
🤖 belirtiyi bastırır; hangi yorumun gereksiz olduğunu da tanımlamaz.
## C
Biçim yorumlarını serbest bırakıp inceleme için hedef süre belirlemek
🤖 süre hedefi baskı üretir, tartışmanın konusu aynı kalır.
## D
İncelemeyi tek bir kıdemli geliştiriciye devredip diğerlerini süreçten çıkarmak
🤖 darboğaz ve tek noktada bilgi üretir, ortak sahiplenme kaybolur.
## Doğru
A

## Açıklama
Kod incelemesinin temel kuralı şudur: **makinenin karar verebileceği hiçbir konu insana kalmaz**. Biçim, içe aktarma sırası ve basit kod kokusu bir biçimlendirici ile statik analiz aracının işidir. Bunlar hattın zorunlu kapısı yapılır; PR'da tartışılmaz, otomatik olarak uygulanır (`191`). Böylece insan incelemesine kalan zaman gerçekten insan gerektiren konulara ayrılır: isimlendirme ve niyet, sınır durumları, hata yolları, testin doğru şeyi sınayıp sınamadığı, yetki kontrolü ve geriye uyumluluk (`132`, `190`). B şıkkı yorum sayısını sınırlar; bu bir belirti bastırmadır, çünkü hangi yorumun değerli olduğunu belirlemez. C şıkkı süre hedefi koyar; inceleyen kişi aynı biçim tartışmasını daha hızlı yapmaya çalışır, konu değişmez. D şıkkı incelemeyi tek kişiye devreder; bu hem darboğaz üretir hem de bilgiyi tek noktada toplar. Ayrıca ekip ortak sahiplenmeyi kaybeder ve o kişi izinliyken süreç durur (`207`).

# sozlu4-4 | İleri
## Soru
Üretimde yeni yayınlanan sürümden sonra hata oranı hızla yükseldi ve kullanıcılar etkileniyor. Kök neden henüz bilinmiyor. En doğru ilk hamle hangisidir?
## A
Kök neden bulunana kadar bekleyip ekipçe log incelemesine odaklanmak
🤖 kullanıcı etkisi sürerken teşhis beklemek zararı büyütür.
## B
Hızlı bir düzeltme yazıp doğrudan üretime çıkararak sorunu kaynağında kapatmak
🤖 doğrulanmamış yama yeni hata ekler ve olayı uzatır.
## C
Önce yayını geri alıp etkiyi durdurmak, kök nedeni sonra sakin sakin aramak
🤖 Doğru: önce hafiflet sonra anla; geri alma en öngörülebilir hamledir.
## D
Etkilenen kullanıcı sayısı ölçülene kadar müdahaleyi bekletip ölçümü tamamlamak
🤖 ölçüm gerekir ama müdahaleyi bekletmez; etki zaten görünür durumdadır.
## Doğru
C

## Açıklama
Olay yönetiminin sırası tek cümlede özetlenir: **önce durdur, sonra anla**. Kesinti anında amaç öğrenmek değil, kullanıcıyı kurtarmaktır. Kök neden analizi değerlidir, ama etki sürerken yapılırsa her dakika zarar büyür (`195`). Hafifletme seçenekleri arasında **geri alma** en üsttedir, çünkü hedef durum bilinir ve sonuç öngörülebilir. Özellik bayrağını kapatmak ve trafiği sağlıklı sürüme çevirmek de aynı ailedendir (`192`). A şıkkı teşhisi öne alır ve kullanıcıyı beklemede bırakır. B şıkkı acele bir ileri düzeltme yazar; bu yama test edilmemiştir, kök neden de henüz bilinmemektedir. Böyle bir değişiklik olayı çözmek yerine ikinci bir olay üretebilir. D şıkkı ölçümü müdahalenin önüne koyar. Etki ölçümü ve şiddet sınıflandırması gereklidir, fakat bunlar müdahaleyi geciktirmez; ikisi paralel yürür (`104`). Etki durduktan sonra zaman çizelgesi tamamlanır ve suçsuz postmortem ile eylem maddeleri sahibiyle ve tarihiyle kaydedilir (`193`).

# sozlu4-5 | İleri
## Soru
Bir sorgu `musteri_id` kolonunda eşitlik, `olusma_tarihi` kolonunda aralık filtresi kullanıyor ve sonucu tarihe göre sıralıyor. Tabloda yalnız `(olusma_tarihi)` indeksi var ve sorgu yavaş. En doğru iyileştirme hangisidir?
## A
`(olusma_tarihi, musteri_id)` bileşik indeksini oluşturmak ve eskisini kaldırmak
🤖 aralık kolonu başa gelirse sonraki kolon etkin biçimde kullanılamaz.
## B
`(musteri_id, olusma_tarihi)` bileşik indeksini oluşturmak ve eskisini kaldırmak
🤖 Doğru: eşitlik kolonu önce, aralık ve sıralama kolonu sonra gelir.
## C
`musteri_id` için ayrı bir indeks daha ekleyip iki indeksi birlikte bırakmak
🤖 iki ayrı indeks çoğu planda birleştirilmez, bileşik indeks kadar etkili olmaz.
## D
`BETWEEN` koşulunu kaldırıp tarih filtresini uygulama tarafında yapmak
🤖 veritabanından gereksiz satır çeker; ağ ve bellek maliyeti büyür.
## Doğru
B

## Açıklama
Bileşik indekste **kolon sırası** belirleyicidir. Genel kural şudur: eşitlik koşullu kolonlar başa, aralık ve sıralama kolonu sona konur. Burada `musteri_id` eşitlikle filtreleniyor, `olusma_tarihi` ise aralıkla süzülüyor ve sıralamayı da o belirliyor. `(musteri_id, olusma_tarihi)` indeksi önce tek bir müşteriye iner, sonra o müşterinin satırları zaten tarih sırasında durduğu için aralık taraması ve sıralama bedavaya gelir (`146`). A şıkkı sırayı ters kurar. Aralık kolonu başa alındığında indeks tarih aralığında yürür, fakat `musteri_id` bu yürüyüş içinde etkin bir arama anahtarı olamaz; ilgisiz çok sayıda satır okunur. C şıkkı iki ayrı indeks önerir. Çoğu yürütme planı iki indeksi birleştirmek yerine birini seçer, çünkü birleştirme ek maliyet üretir (`29`). D şıkkı filtreyi uygulamaya taşır; bu, veritabanının en iyi yaptığı işi ondan almak demektir ve gereksiz satır transferi doğurur (`227`). Karar vermeden önce yürütme planına bakılır ve değişiklik sonrası tekrar ölçülür.

# sozlu4-6 | Uzman
## Soru
`WHERE UPPER(eposta) = ?` koşulu kullanan bir sorgu, `eposta` kolonunda indeks olmasına rağmen tam tablo taraması yapıyor. Yürütme planı indeksi hiç aday olarak değerlendirmiyor. En doğru açıklama ve çözüm hangisidir?
## A
İndeks parçalanmıştır, yeniden oluşturulunca yürütme planı indeksi tekrar seçer
🤖 parçalanma maliyeti etkiler, fakat burada indeks zaten aday bile değildir.
## B
Tablo istatistikleri eskimiştir, istatistik güncellenince plan indekse geri döner
🤖 eski istatistik yanlış plan üretebilir, ama buradaki engel ifadenin kendisidir.
## C
Optimize ediciye ipucu (hint) verilerek indeks kullanımı zorla seçtirilmelidir
🤖 ipucu belirtiyi bastırır; kolon yine fonksiyon içindedir, arama yapılamaz.
## D
Kolon fonksiyona sokulduğu için indeks aranabilir değil, ifade indeksi gerekir
🤖 Doğru: indeks kolonun ham değerini tutar, fonksiyonun sonucunu değil.
## Doğru
D

## Açıklama
Bir indeks, kolonun **ham değerini** sıralı biçimde saklar. `UPPER(eposta)` yazıldığında aranan şey artık o ham değer değil, her satır için hesaplanacak yeni bir sonuçtur. Bu yüzden indeks aranabilir olmaktan çıkar ve yürütme planı onu aday olarak bile değerlendirmez; tabloyu baştan sona taramak zorunda kalır (`146`). Çözüm iki yoldan biridir. Birincisi ifadenin kendisi üzerinde bir indeks tanımlamaktır; veritabanı bu durumda hesaplanmış değeri saklar. İkincisi veriyi yazarken normalleştirmektir, yani kolonu zaten tek biçimde tutup sorguda fonksiyon kullanmamaktır (`29`). A şıkkı parçalanmayı suçlar. Parçalanma tarama maliyetini etkileyebilir, ama indeksin hiç seçilmemesini açıklamaz. B şıkkı istatistikleri suçlar. Eski istatistik gerçekten yanlış plan üretebilir ve tahmini satır ile gerçek satır farkına bakmak iyi bir alışkanlıktır; ancak fonksiyonlu kolonda indeks istatistikten bağımsız olarak kullanılamaz (`227`). C şıkkı ipucu ile zorlamayı önerir. İpucu belirtiyi bastırır ve genellikle daha kötü bir plana yol açar, çünkü kolon hâlâ fonksiyonun içindedir (`82`).
