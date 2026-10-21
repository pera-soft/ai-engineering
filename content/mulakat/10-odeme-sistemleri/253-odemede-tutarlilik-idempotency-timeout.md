---
slug: odemede-tutarlilik-idempotency-timeout
baslik: "Ödemede Tutarlılık: Idempotency Anahtarı, Timeout Belirsizliği ve Çifte Tahsilat"
kademe: Uzman
sira: 253
etiketler: 11
---

## Konu anlatımı

### Bu ne işe yarar?

Telefonla sipariş verdiğini düşün: "iki pizza" dedin, karşı taraf "tamam" demeden hat kesildi. Şimdi elinde tek bir soru var ve cevabı yok: sipariş geçti mi, geçmedi mi? Tekrar arayıp aynı siparişi verirsen dört pizza gelebilir; aramazsan aç kalabilirsin. Doğru davranış üçüncüsüdür — arayıp **"benim şu numaralı siparişim ne durumda?"** diye sormak. Ödeme sistemlerinin tüm tutarlılık disiplini bu tek cümlenin mühendislik hâlidir.

Ödemede hata pahalıdır, çünkü para tek yönlü akar. Bir arama sorgusunu iki kez çalıştırmanın bedeli yoktur. Bir tahsilatı iki kez çalıştırmanın bedeli ise müşterinin cebinden çıkan ikinci tutardır. Üstelik bunu geri almak "silmek" değildir; yeni bir iade işlemi açmak demektir.

Bu yüzden ödeme ekiplerinin mülakatı ağırlıklı olarak burada geçer. Kart akışını (`250`) ve güvenlik katmanını (`251`) bilmek giriş şartıdır. Adayı ayıran şey, **belirsizlik altında doğru davranmayı** anlatabilmektir. Tekrar edilse de sonucu değişmeyen işlemenin (idempotency) genel mekaniği `130` numaralı konuda anlatıldı. Burada anlatılan, o mekanizmanın para söz konusu olduğunda kazandığı ek kurallardır.

### Temel kavramlar

- **Idempotency anahtarı (idempotency key):** istemcinin bir iş girişimi için ürettiği benzersiz kimlik; aynı anahtarla gelen ikinci istek yeni bir yan etki üretmez. Genel mekanizma `130`.
- **Belirsiz sonuç (unknown outcome):** çağrının başarılı mı başarısız mı bittiğinin bilinemediği durum. Ödemede **üçüncü bir cevap sınıfıdır** — evet, hayır, bilmiyorum.
- **Sorgulama ucu (inquiry / status endpoint):** PSP'nin "şu referanslı işlemin akıbeti ne?" sorusuna cevap veren servisi; timeout sonrası ilk başvurulacak yer.
- **Ters kayıt (reversal / refund):** gerçekleşmiş bir para hareketini silmek değil, anlamca tersine çeviren **yeni bir işlem** açmak (`126`).
- **Mutabakat (reconciliation):** kendi kayıtlarınla karşı tarafın settlement dosyasının karşılaştırılması; belirsiz kalan işlemlerin son mahkemesi (`252`).
- **En az bir kez teslimat (at-least-once):** mesajlaşmanın normal garantisi; aynı ödeme olayının tüketiciye iki kez gelebilmesi demektir (`110`, `116`).

### Ödemede idempotency: anahtar tek başına yetmez, sonuçla birlikte saklanır

Genel idempotency anlatımında (`130`) vurgu "aynı isteği iki kez uygulama"dır. Ödemede bir adım fazlası gerekir: **ilk isteğin sonucunun da saklanması.** Çünkü ikinci istek geldiğinde doğru davranış "hata döndürmek" değil, **ilk kez ne olduysa onu aynen döndürmektir** — müşteri iki farklı ekran görmemelidir.

Mekanizma dört parçadır:

1. **Anahtar istemcide, işin başında üretilir.** Ödeme ekranına girildiği anda değil, **sipariş/rezervasyon oluşturulduğu anda** üretilir. Sebep basittir: yeniden deneme yapan istemcinin aynı anahtarı taşıması gerekir. Anahtarı her istekte yeniden üreten bir istemci, tekilleştirmeyi kâğıt üzerinde uygulamıştır; pratikte hiç uygulamamıştır.
2. **Sunucu anahtarı benzersiz kısıtlı bir tabloda saklar.** Tekilliği garanti eden şey uygulama kodundaki "önce bak, yoksa yaz" kontrolü değildir; **veritabanı kısıtıdır**. Çünkü iki eşzamanlı istekte, kontrol ile yazma arasına başka bir istek girebilir. Buna kontrol-anı/kullanım-anı yarışı (time-of-check/time-of-use) denir. Veritabanı kısıtı bu aralığı kapatır.
3. **Kayıt, sonucu da taşır.** Kayıtta işlem durumu, PSP referansı ve dönen yanıt bulunur. Aynı anahtarla gelen ikinci istek PSP'ye hiç gitmez; saklanan yanıt döner.
4. **Aynı anahtar PSP'ye de iletilir.** Bu adım sık atlanır ama en kritik olanıdır. Ağ zaman aşımında yaptığın yeniden deneme senin tarafında tekilleşebilir. Yine de ilk isteğin PSP'ye ulaşıp ulaşmadığını bilmiyorsun. Anahtar karşı tarafa da gidiyorsa PSP ikinci çağrıyı ilkiyle aynı işlem sayar ve ikinci bir tahsilat açmaz.

Bir ayrıntı daha: anahtar, **saklanmış istek imzasıyla** birlikte tutulur. Aynı anahtarla farklı bir tutar gelirse bu bir tekrar değil, bir hatadır. Sessizce eski sonucu döndürmek yerine açık bir çakışma hatası dönmek doğrudur.

```svg
<svg viewBox="0 0 660 306" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Idempotency anahtari ile cift istegin tekillestirilmesi: ilk istek islenir, ikinci istek saklanan sonucu alir">
  <defs>
    <marker id="odetut-idem-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="odetut-idem-grn" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı anahtar, tek tahsilat</text>
  <rect x="18" y="44" width="150" height="56" rx="10" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.5"/>
  <text x="93" y="65" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">1. istek</text>
  <text x="93" y="82" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">key: RZV-8371</text>
  <path d="M168 72 L214 72" fill="none" stroke="#25804C" stroke-width="2" marker-end="url(#odetut-idem-grn)"/>
  <rect x="218" y="40" width="216" height="64" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="326" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Ödeme servisi</text>
  <text x="326" y="79" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">key UNIQUE · sonuç da saklanır</text>
  <text x="326" y="95" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">durum + PSP referansı + yanıt</text>
  <path d="M434 72 L482 72" fill="none" stroke="#25804C" stroke-width="2" marker-end="url(#odetut-idem-grn)"/>
  <rect x="486" y="40" width="156" height="64" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="564" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">PSP</text>
  <text x="564" y="79" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">AYNI key iletilir</text>
  <text x="564" y="95" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">orada da tekilleşir</text>
  <rect x="18" y="150" width="150" height="56" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="93" y="171" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">2. istek</text>
  <text x="93" y="188" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çift tıklama / retry</text>
  <path d="M168 178 L214 178" fill="none" stroke="#B07D20" stroke-width="2" marker-end="url(#odetut-idem-arw)"/>
  <rect x="218" y="146" width="216" height="64" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="326" y="168" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Anahtar zaten var</text>
  <text x="326" y="185" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">yeni işlem AÇILMAZ</text>
  <text x="326" y="201" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">saklanan ilk yanıt döner</text>
  <path d="M434 178 L482 178" fill="none" stroke="#B23B3B" stroke-width="1.8" stroke-dasharray="5 3"/>
  <text x="564" y="174" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">PSP'ye hiç gidilmez</text>
  <text x="564" y="191" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ikinci tahsilat yok</text>
  <rect x="18" y="232" width="624" height="60" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="253" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Anahtar sipariş/rezervasyon anında üretilir — ödeme isteğinde değil</text>
  <text x="330" y="270" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">her istekte yeni anahtar üreten istemci idempotency'yi fiilen hiç uygulamamıştır</text>
  <text x="330" y="285" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aynı anahtarla farklı tutar gelirse bu tekrar değil çakışmadır: açık hata dönülür</text>
</svg>
```

### Timeout "işlem olmadı" demek değildir

Bu konunun tek cümlelik özü şudur: **zaman aşımı bir "hayır" değil, bir "bilmiyorum"dur.** İstek karşı tarafa ulaşmamış olabilir; ulaşmış, işlenmiş ama cevabı yolda kaybolmuş da olabilir. İkinci ihtimalde para çekilmiştir ve senin kaydında hiçbir iz yoktur.

Buradan iki yasak doğar. Birincisi, **körlemesine yeniden deneme yasaktır**. İşlem gerçekten başarılı olmuşsa ikinci deneme çifte tahsilat demektir. Tekilleştirme anahtarı PSP'ye taşınıyorsa bu risk kapanır; taşınmıyorsa yeniden deneme bir kumardır.

İkincisi, **körlemesine iade de yasaktır**. Hiç çekilmemiş bir paranın iadesini açarsan bankaya karşı eksi bakiyeli bir kayıt üretirsin ve mutabakat bozulur. Her iki hata da aynı kökten gelir: bilinmeyeni bilinen sanmak.

Doğru sıra üç adımlıdır:

1. **Sorgula.** PSP'nin inquiry/status ucuna kendi işlem referansınla git: "şu referanslı işlem ne oldu?" Cevap netse akış devam eder — başarılıysa ileri, başarısızsa yeni deneme veya kullanıcıya red.
2. **Sorgu da cevapsızsa durumu modelde tut.** İşlem `UNKNOWN` (ya da `PENDING`) durumuna alınır. Bu bir hata bayrağı değil, **gerçek bir iş durumudur** ve ödeme durum makinesinde açıkça bulunur (`250`). Kullanıcıya "ödemeniz doğrulanıyor" denir; ne "başarılı" ne "başarısız" yalanı söylenir.
3. **Mutabakatla kapat.** Gün sonu takas dosyası geldiğinde belirsiz işlem netleşir. Dosyada satırı varsa tahsilat gerçekleşmiştir; ilgili sipariş tamamlanır ya da hizmet verilemediyse iade açılır. Dosyada yoksa işlem hiç olmamıştır ve kayıt kapatılır (`252`).

Bu tasarımın yan etkisi şudur: ödeme durum makinesi **iki uçlu değildir.** "Başarılı / başarısız" ikilisiyle modellenmiş bir sistem, belirsizliği saklayacak yer bulamadığı için ya yanlış tarafa yuvarlar ya da veriyi kaybeder. Ara durumlar tasarımın eksiği değil, gereğidir.

```svg
<svg viewBox="0 0 660 320" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Timeout sonrasi dogru akis: once sorgula, cevapsizsa UNKNOWN durumu, mutabakatla kapat">
  <defs>
    <marker id="odetut-to-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
    <marker id="odetut-to-red" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#B23B3B"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Timeout = "sonucu bilmiyorum"</text>
  <rect x="232" y="40" width="196" height="46" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="330" y="59" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">PSP çağrısı zaman aşımı</text>
  <text x="330" y="76" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">para çekilmiş de olabilir</text>
  <path d="M280 86 L150 122" fill="none" stroke="#B23B3B" stroke-width="1.8" stroke-dasharray="5 3" marker-end="url(#odetut-to-red)"/>
  <path d="M380 86 L508 122" fill="none" stroke="#25804C" stroke-width="1.8" marker-end="url(#odetut-to-arw)"/>
  <rect x="18" y="126" width="270" height="72" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="153" y="147" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">YANLIŞ iki refleks</text>
  <text x="153" y="165" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">körlemesine retry → çifte tahsilat</text>
  <text x="153" y="181" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">körlemesine iade → olmayan parayı iade</text>
  <rect x="372" y="126" width="270" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="507" y="147" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">1. SORGULA (inquiry)</text>
  <text x="507" y="165" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"şu referanslı işlem ne oldu?"</text>
  <text x="507" y="181" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">cevap netse akış devam eder</text>
  <path d="M507 198 L507 226" fill="none" stroke="#5D7789" stroke-width="1.6" marker-end="url(#odetut-to-arw)"/>
  <text x="596" y="216" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sorgu da cevapsız</text>
  <rect x="372" y="230" width="270" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="507" y="251" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2. UNKNOWN durumu</text>
  <text x="507" y="268" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">durum makinesinde gerçek durum (250)</text>
  <text x="507" y="284" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">3. mutabakat dosyası netleştirir (252)</text>
  <rect x="18" y="230" width="270" height="66" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="153" y="251" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kullanıcıya söylenen</text>
  <text x="153" y="268" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"ödemeniz doğrulanıyor"</text>
  <text x="153" y="284" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ne başarılı ne başarısız denir</text>
</svg>
```

### Dış PSP çağrısında dayanıklılık

Ödeme servisinin en kırılgan noktası kendi kodu değil, **dışarıya açtığı bağlantıdır.** Dört ayarın hepsi bilinçli verilmelidir:

- **Zaman aşımı (timeout) kısa tutulur.** Bağlanma ve okuma için ayrı ayrı, saniyeler mertebesinde sınırlar konur. Gerekçe teknik değil, davranışsaldır. Müşteri ödeme ekranında sonsuza kadar bekleyemez; beklerse sayfayı yeniler. Yenileme yeni bir istek üretir ve tam da önlemeye çalıştığın tekrar sorunu doğar. Ayrıca sınırsız zaman aşımıyla üretime çıkmak, tek bir yavaş bağımlılığın tüm iş parçacığı havuzunu (thread pool) yutmasına kapı açar.
- **Yeniden deneme yalnızca güvenliyse yapılır.** Güvenli olmasının tek şartı, isteğin karşı tarafta tekilleşen bir anahtar taşımasıdır. Şart sağlanıyorsa 5xx hatalarında ve zaman aşımlarında bir-iki deneme uygundur. Denemeler arası bekleme kademeli artar ve üzerine rastgele sapma eklenir (**backoff + jitter**). Rastgele sapma olmadan yapılan sabit aralıklı denemeler, aynı anda düşen istemcileri aynı anda geri getirir ve karşı tarafa ikinci bir dalga vurur. Şart sağlanmıyorsa yeniden deneme yerine sorgulama yapılır.
- **Devre kesici (circuit breaker) devreyi açar.** PSP art arda hata veriyorsa her isteği ona taşımak iki zarar verir: müşteriyi bekletir ve düşmüş sistemin toparlanmasını engeller (`101`). Devre açıkken ne yapılacağı **bir iş kararıdır**, teknik detay değildir. Alternatif PSP'ye geçilecek mi, yoksa kullanıcıya "ödemeniz alınamadı, lütfen tekrar deneyin" mi denecek? Alternatife geçmenin bedeli, aynı işlemin iki sağlayıcıda birden denenme riskidir. Bu yüzden ancak ilk denemenin akıbeti bilindiğinde güvenlidir.
- **Bölme duvarı (bulkhead) izolasyonu.** Ödeme entegrasyonuna ayrılmış bağlantı ve iş parçacığı havuzu, uygulamanın geri kalanından ayrılır. Böylece PSP yavaşladığında yalnız ödeme yavaşlar. Rezervasyon, arama ve check-in akışları ayakta kalır. Tek ortak havuz kullanan bir uygulamada yavaş bir dış çağrı, ilgisiz tüm uçları da beraberinde indirir.

Bu dördü aynı amacın parçalarıdır: dış sistemin kötü günü, senin sisteminin kötü günü olmamalıdır.

### Saga bağlantısı: telafi rollback değil, ters kayıttır

Ödeme kendi başına duran bir işlem değildir. Sipariş/rezervasyon–ödeme–teslim (havacılıkta biletleme) zincirinin ortasındaki adımdır. Zincir dağıtıktır ve adımlar farklı sistemlerdedir. Harici PSP de iki aşamalı işleme (2PC) katılmaz. Bu yüzden kullanılan desen **sagadır**, yani adım adım telafili işlem zinciridir (`126`).

Ödeme bağlamında saganın kendine özgü kuralı şudur: **telafi bir geri sarma değildir.** Para el değiştirdiyse o hareket olmamış hâle gelmez. Anlamca tersini yapan **yeni bir işlem** açılır.

Pratikte iki ayrı araç vardır ve bunları karıştırmak klasik hatadır. Henüz takas edilmemiş bir işlem **void** edilebilir; müşteri ekstresinde hiç görünmez. Takas olmuş bir işlem ise ancak **refund** ile iade edilir; bu ayrı bir finansal kayıttır ve günler sürebilir. Sistem, hangi aracın uygun olduğunu işlemin durumuna bakarak seçmelidir.

İkinci kural, ara durumların modelde açıkça bulunmasıdır. "Ödeme alındı ama teslim henüz yapılmadı" bir gerçektir ve isimlendirilmelidir. İsimlendirilmezse akış, ödeme başarılı olduğu anda "tamamlandı" sayılır ve teslim adımının düşmesi hiç fark edilmez.

Cevapsız kalan adımlar için bir **zaman aşımı işi** gerekir. Bu iş, belirli süre ara durumda kalan kayıtları toplar; onları ya ileri sürer ya da telafiyi tetikler. Telafinin kendisi de tekrar edilse de sonucu değişmeyen bir işlem olmalıdır: "iki kez iade et" mesajı tek iade üretmelidir (`126`, `130`).

### Aynı problemin üç kaynağı

Mülakatta çoğu aday çifte tahsilatı tek bir sebeple, "kullanıcı iki kez tıkladı" ile açıklar. Oysa aynı sonucu üreten **üç bağımsız kaynak** vardır ve üçü de aynı savunmayla karşılanır:

1. **Kullanıcı tekrarı:** çift tıklama, sayfa yenileme, ikinci sekme, mobil uygulamanın yeniden denemesi. Ön yüzde düğmeyi kilitlemek nezakettir, güvenlik sınırı değildir — API doğrudan da çağrılabilir.
2. **Ağ/istemci retry'ı:** zaman aşımı sonrası HTTP istemcisinin veya araya giren bir proxy'nin otomatik tekrarı. Kullanıcı hiçbir şey yapmamıştır, istek yine iki kez gitmiştir.
3. **Mesajlaşmanın "en az bir kez" teslimatı (at-least-once):** ödeme olayı Kafka'dan tüketiciye iki kez gelebilir. Okuma konumu (offset) kaydedilmeden önce tüketici çökerse aynı kayıt yeniden işlenir (`110`). Giden kutusu tablosu (outbox) deseni yayının kaybolmamasını garanti eder ama tekrarı ortadan kaldırmaz; onun verdiği garanti de "en az bir kez"dir (`116`).

Üçünün ortak savunması aynıdır: **iş anahtarına dayalı tekilleştirme, benzersiz kısıt ve tekrar edilse de sonucu değişmeyen işleme.** "Tam bir kez (exactly-once) teslimat açarım" cevabı eleyicidir. Uçtan uca tam bir kez teslimat pratikte kurulmaz. Kurulan şey, en az bir kez teslimatın üzerine oturan tekilleştirilmiş işlemedir.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Cifte tahsilatin uc kaynagi ve ortak savunma katmanlari">
  <defs>
    <marker id="odetut-3k-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Üç ayrı kaynak, tek savunma</text>
  <rect x="16" y="44" width="196" height="60" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="114" y="65" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Kullanıcı tekrarı</text>
  <text x="114" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">çift tıklama · yenileme</text>
  <text x="114" y="97" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">düğme kilidi yetmez</text>
  <rect x="232" y="44" width="196" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="65" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Ağ retry'ı</text>
  <text x="330" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">timeout sonrası otomatik</text>
  <text x="330" y="97" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kullanıcı farkında değil</text>
  <rect x="448" y="44" width="196" height="60" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="546" y="65" text-anchor="middle" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">at-least-once</text>
  <text x="546" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kafka tekrar teslimi (110)</text>
  <text x="546" y="97" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">outbox da bunu verir (116)</text>
  <path d="M114 104 L300 138" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#odetut-3k-arw)"/>
  <path d="M330 104 L330 138" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#odetut-3k-arw)"/>
  <path d="M546 104 L360 138" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#odetut-3k-arw)"/>
  <rect x="118" y="144" width="424" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.7"/>
  <text x="330" y="165" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İş anahtarına dayalı tekilleştirme</text>
  <text x="330" y="183" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">idempotency anahtarı (130) + benzersiz kısıt + idempotent işleme</text>
  <rect x="60" y="212" width="540" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="232" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Eleyici cevap: "broker'da exactly-once açarım"</text>
  <text x="330" y="248" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">uçtan uca tam bir kez teslimat kurulmaz; kurulan şey idempotent işlemedir</text>
</svg>
```

### Çifte tahsilat: nasıl bulunur, nasıl önlenir

Üretimde bu iş genellikle tersten başlar — önce müşteri şikâyeti gelir, sonra sebep aranır. İki aşamalı yürütülür.

**Bulma.** İlk adım hasarın büyüklüğünü ölçmektir; bu bir mutabakat sorgusudur. Doğal iş anahtarına göre gruplayıp birden fazla kayıt üretenleri çekersin: `GROUP BY siparis_no, tutar ... HAVING COUNT(*) > 1` mantığı. Sonuç listesi hem müşteriye iade edilecek kalemleri hem de sorunun hacmini verir.

İkinci adım kök nedeni bulmaktır. İki isteğin uçtan uca izini **izleme kimliğiyle (correlation-id)** sürersin (`104`, `193`). Üç ihtimal vardır. İki farklı tekilleştirme anahtarı mı geldi? O zaman istemci anahtarı yanlış anda üretiyordur. Aynı anahtar iki kez mi işlendi? O zaman kısıt yoktur ve kontrol yarışa açıktır. Yoksa aynı olay iki kez mi tüketildi? O zaman tüketici tekilleştirme yapmıyordur. Üç kök neden, üç ayrı düzeltme demektir. İz sürmeden yapılan düzeltme genellikle yanlış katmana yapılır.

**Önleme.** Kalıcı çözüm üç katmanlıdır. Birincisi, istemcinin doğru anda ürettiği tekilleştirme anahtarı. İkincisi, sunucuda **doğal iş anahtarına konmuş benzersiz kısıt**; bu son savunma hattıdır. Üçüncüsü, tüketici tarafında işlenmiş olay kaydına dayalı tekilleştirilmiş işleme. Kısıt, "önce oku, yoksa yaz" mantığının yerini almalıdır. Çünkü okuma ile yazma arasındaki boşluk, tam da yarışın yaşandığı yerdir.

Bir uygulama sırası uyarısı daha var; bu, üretim görmüşlüğün en net sinyallerinden biridir. **Benzersiz kısıt, mevcut çift kayıtlar temizlenmeden eklenemez.** Tabloda halihazırda kısıtı ihlal eden satırlar varsa, veritabanı göçü (migration) çalıştığı anda hata verir ve dağıtım yarıda kalır.

Doğru sıra şudur: önce çiftleri bul ve raporla, iş birimiyle hangisinin geçerli sayılacağına karar ver, iadeleri aç, kayıtları düzelt, **sonra** kısıtı ekle. "Benzersizlik kısıtı koyarım" cevabı doğrudur ama eksiktir; sırayı söylemek cevabı tamamlar.

### Mülakatta ne sorulur?

- **"Kullanıcı öde düğmesine iki kez bastı, ne yaparsın?"** — cevap iskeleti (4 adım): (1) "Ön yüzde düğmeyi kilitlerim ama bunu güvence saymam; API doğrudan da çağrılabilir." (2) "İstemci rezervasyon anında bir idempotency anahtarı üretir, tüm retry'lar onu taşır (`130`)." (3) "Sunucu anahtarı benzersiz kısıtlı tabloda **sonuçla birlikte** saklar; ikinci istek işlem yapmadan ilk yanıtı alır." (4) "Aynı anahtarı PSP'ye de iletirim ki ağ tekrarında orada da tekilleşsin."
- **"PSP'den cevap gelmedi, ne yaparsın?"** — eleyici sorudur. "Timeout 'olmadı' demek değil, 'bilmiyorum' demektir" cümlesiyle başla; sonra sırayı ver: inquiry ile sorgula → cevapsızsa `UNKNOWN` durumuna al (`250`) → mutabakat dosyasıyla kapat (`252`). "Retry ederim" veya "iade açarım" cevapları tek başına elemeye yeter.
- **"Retry stratejin nedir?"** Yalnızca istek karşı tarafta tekilleşiyorsa güvenlidir; backoff + jitter ile sınırlı deneme; devre açılırsa fallback bir iş kararıdır (alternatif PSP mi, kullanıcıya red mi) — `101`. Bulkhead ile ödeme entegrasyonu diğer akışları boğmamalıdır.
- **"Para çekildi ama teslim/biletleme yapılamadı?"** Saga (`126`); telafi rollback değil ters kayıttır — takas olmamışsa void, olmuşsa refund; ara durum modelde adlandırılır, timeout işi cevapsız kalanları toplar. Havacılık özelindeki tam akış `254`.
- **"Aynı ödeme olayı Kafka'dan iki kez geldi?"** At-least-once normaldir (`110`); outbox yayının kaybolmamasını sağlar ama tekrarı önlemez (`116`); çözüm işlenmiş olay kaydı + benzersiz kısıt. "Exactly-once açarım" deme.
- **"Sistemde çifte tahsilat var, nasıl bulursun?"** `HAVING COUNT(*) > 1` ile hacmi ölç, correlation-id ile kök nedeni bul (`104`), kısıt + idempotency ile kapat — ve kısıtı eklemeden önce mevcut çiftlerin temizlenmesi gerektiğini söyle.

### Tek cümlede

Ödemede tutarlılık dört şeyle kurulur. Bir: aynı iş girişimini taşıyan bir **tekilleştirme anahtarı** istemciden PSP'ye kadar tüm zincirde taşınır. İki: zaman aşımı bir "hayır" değil **"bilmiyorum"** kabul edilir; önce sorgulama yapılır, sonra mutabakatla kapatılır. Üç: dış çağrı zaman aşımı, yeniden deneme, devre kesici ve bölme duvarı ile sınırlanır. Dört: para hareketinin ancak **ters kayıtla** geri alınabildiği modele işlenir.

# odetut-1 | Orta

## Soru
Bir bilet satış ekranında müşteri "Öde" düğmesine art arda iki kez bastı; ikinci istek sunucuya, ilk isteğin PSP yanıtı dönmeden ulaştı. Çifte tahsilatı önleyen doğru sunucu tasarımı hangisidir?

## A
Ön yüzde düğme ilk tıklamada devre dışı bırakılır ve istek gönderilirken yükleniyor katmanı açılır; tekillik istemci tarafında bu şekilde sağlanır
🤖 istemci önlemi deneyimi iyileştirir ama güvenlik sınırı değildir; API doğrudan çağrılabilir, proxy retry edebilir, ikinci sekme açılabilir.

## B
Sunucu aynı karta kısa bir zaman penceresinde gelen ikinci ödeme isteğini tutar ve kart karşılaştırmasıyla yakalayıp reddeder
🤖 zaman penceresi kırılgandır: meşru ikinci bir satın almayı reddeder, pencere dışına sarkan tekrarı kaçırır.

## C
İstemcinin rezervasyon anında ürettiği idempotency anahtarı istekle gelir; sunucu anahtarı benzersiz kısıtlı tabloda sonucuyla saklar, ikinci istek işlem yapmadan ilk yanıtı alır
🤖 Doğru: tekillik anahtara bağlanır ve saklanan sonuç sayesinde ikinci istek yan etkisiz biçimde aynı cevabı görür.

## D
Ödeme kaydı yazılmadan önce tabloda aynı sipariş için kayıt olup olmadığı sorgulanır; varsa istek reddedilir, yoksa yeni kayıt açılır
🤖 kontrol ile yazma arasındaki boşlukta ikinci istek araya girer (time-of-check/time-of-use); tekilliği garanti eden şey veritabanı kısıtıdır.

## Doğru
C

## Açıklama
Aynı iş girişiminin sunucuya birden çok kez ulaşması ödemede normaldir; savunma **iş anahtarına bağlanmalıdır**. Doğru mekanizma, istemcinin **rezervasyon/sipariş anında** benzersiz bir idempotency anahtarı üretmesi (ödeme isteği anında değil — retry'ların aynı anahtarı taşıyabilmesi için), sunucunun bu anahtarı **benzersiz kısıtlı** bir tabloda **işlem sonucuyla birlikte** saklaması ve aynı anahtarla gelen ikinci isteğe PSP'ye hiç gitmeden saklanan yanıtı döndürmesidir (`130`). Sonucu saklamak ayrıntı değil, tasarımın parçasıdır: ikinci isteğe hata dönmek yerine ilk sonucu döndürmek, müşterinin iki farklı ekran görmesini engeller. Ön yüzde düğmeyi kilitlemek yararlıdır ama güvence değildir — istek ağ katmanınca da tekrarlanabilir, API doğrudan da çağrılabilir. Zaman penceresi + kart karşılaştırması hem yanlış pozitif üretir (aynı kartla bilinçli ikinci alım reddedilir) hem de gecikmeli tekrarları kaçırır. Son şıkkın hatası en incelikli olanıdır: "önce oku, yoksa yaz" mantığı eşzamanlı iki istek arasında yarışa açıktır; okuma ile yazma arasındaki boşluk tam da çiftin doğduğu yerdir ve bu boşluğu ancak veritabanı kısıtı kapatır. Aynı anahtarın PSP'ye de iletilmesi tabloyu tamamlar: ağ zaman aşımında yapılan tekrar karşı tarafta da tekilleşir.

# odetut-2 | Orta

## Soru
Ödeme servisi PSP'ye tahsilat isteği gönderdi ve okuma zaman aşımına düştü; hiçbir yanıt alınamadı. Sistemin bu noktadaki doğru davranışı hangisidir?

## A
Önce PSP'nin sorgulama (inquiry) ucundan işlemin akıbeti sorulur; sorgu da cevapsız kalırsa işlem UNKNOWN durumuna alınır ve mutabakat dosyasıyla netleştirilir
🤖 Doğru: zaman aşımı sonucu bilinmez kılar; doğru sıra önce sorgulamak, kapatamıyorsan belirsizliği modelde tutmaktır.

## B
İşlem başarısız kabul edilip kullanıcıya "ödeme alınamadı" mesajı gösterilir ve akış sonlandırılır; müşteri isterse yeniden dener
🤖 zaman aşımı başarısızlık değildir; para çekilmiş olabilir ve müşteri ikinci denemede ikinci kez ödeyebilir.

## C
Aynı istek hemen yeniden gönderilir; ödeme çağrıları doğası gereği tekrar edilebilir olduğu için ikinci deneme riski ortadan kaldırır
🤖 tahsilat çağrısı kendiliğinden tekrar edilebilir değildir; ilk istek işlenmiş olabilir, körlemesine retry çifte tahsilattır.

## D
Müşteri mağdur olmasın diye hemen bir iade işlemi açılır; böylece para çekildiyse geri verilmiş, çekilmediyse zaten etkisiz kalmış olur
🤖 hiç çekilmemiş bir tutarın iadesi negatif bir finansal kayıt üretir ve mutabakatı bozar; iade etkisiz bir işlem değildir.

## Doğru
A

## Açıklama
Bu sorunun tek doğru başlangıcı şu cümledir: **zaman aşımı "işlem olmadı" demek değil, "sonucu bilmiyorum" demektir.** İstek karşı tarafa hiç ulaşmamış olabilir; ulaşmış, işlenmiş ve yalnızca yanıtı kaybolmuş da olabilir — ikinci ihtimalde para çekilmiştir ve senin kaydında hiçbir iz yoktur. Doğru sıra bu belirsizliği önce **daraltmaya**, kapatamıyorsa **saklamaya** dayanır: PSP'nin inquiry/status ucundan kendi işlem referansınla işlemin akıbeti sorulur; cevap netse akış ona göre ilerler. Sorgu da cevapsız kalırsa işlem `UNKNOWN` (veya `PENDING`) durumuna alınır — bu bir hata bayrağı değil, ödeme durum makinesinde açıkça modellenmiş **gerçek bir durumdur** (`250`) — ve gün sonu settlement/mutabakat akışıyla netleştirilir (`252`). Başarısız saymak (B) müşteriyi ikinci ödemeye iter. Körlemesine retry (C) ancak istek PSP tarafında tekilleşen bir idempotency anahtarı taşıyorsa güvenlidir; taşımıyorsa doğrudan çifte tahsilattır. Refleks iade (D) en tehlikelisidir: hiç gerçekleşmemiş bir tahsilatın iadesi karşı tarafta karşılıksız bir ters kayıt üretir, mutabakatta fark olarak düşer ve düzeltmesi manuel operasyona kalır. Ödeme durum makinesinin iki uçlu (başarılı/başarısız) modellenemeyeceğinin en net gerekçesi budur.

# odetut-3 | İleri

## Soru
Ödeme entegrasyonunun dayanıklılık ayarları gözden geçiriliyor. PSP çağrısı için hangi yapılandırma bütünü doğrudur?

## A
Timeout uzun tutulur ki geç de olsa cevap kaçırılmasın; PSP hata verdikçe istekler sıraya alınıp cevap dönene kadar beklenir
🤖 uzun timeout thread havuzunu tüketir ve müşteriyi ekranda bekletir; bekleyen kullanıcı sayfayı yeniler, tekrar problemi büyür.

## B
Zaman aşımı ve 5xx durumlarında sabit aralıklı sınırsız yeniden deneme uygulanır; işlem eninde sonunda başarılı olacağı için tutarlılık kendiliğinden sağlanır
🤖 sınırsız retry düşmüş sağlayıcıya ikinci dalga vurur; sabit aralık istemcileri senkronize eder ve idempotency yoksa her deneme yeni tahsilat riskidir.

## C
Devre kesici kaldırılır çünkü ödeme kritik bir akıştır ve hiçbir isteğin devre açık diye reddedilmemesi gerekir; her istek mutlaka PSP'ye taşınır
🤖 devreyi kapalı tutmak düşmüş sağlayıcıyı diriltmez, yalnızca kendi kaynaklarını da tüketir; kritiklik daha çok koruma gerektirir.

## D
Connect ve read için agresif timeout, yalnız idempotency anahtarı taşıyan isteklerde backoff+jitter ile sınırlı retry, devre kesici ve iş kararı olarak tanımlanmış fallback, ödeme havuzunu ayıran bulkhead
🤖 Doğru: dördü birlikte dış sistemin kötü gününün kendi sistemini indirmesini engeller.

## Doğru
D

## Açıklama
Dört ayarın hepsi bilinçli verilmelidir ve hiçbiri tek başına yeterli değildir. **Timeout agresif olmalıdır**: gerekçesi teknik olduğu kadar davranışsaldır — müşteri ödeme ekranında sonsuz bekleyemez, beklerse sayfayı yeniler ve bu yenileme tam da önlemeye çalıştığın tekrar isteğini üretir; ayrıca uzun timeout, yavaş bir bağımlılığın thread ve bağlantı havuzunu tüketmesine kapı açar. **Retry ancak güvenliyse yapılır**: güvenliğin tek şartı isteğin karşı tarafta tekilleşen bir anahtar taşımasıdır; şart sağlanıyorsa **backoff + jitter** ile bir-iki deneme uygundur — jitter'sız sabit aralık, aynı anda zaman aşımına düşen tüm istemcileri aynı anda geri getirip düşmüş sağlayıcıya ikinci bir dalga vurur. **Devre kesici** art arda hata veren PSP'ye istek taşımayı keser (`101`); "kritik akış, devre koymayalım" muhakemesi tersine döner — kritik olduğu için korunması gerekir, çünkü açık devre yalnız karşı tarafı değil kendi kaynaklarını da kurtarır. Devre açıkken **fallback bir iş kararıdır**: alternatif PSP'ye failover mı yapılacak yoksa "ödemeniz alınamadı, tekrar deneyin" mi denecek — failover'ın bedeli aynı işlemin iki sağlayıcıda denenme riskidir ve ancak ilk denemenin akıbeti bilindiğinde güvenlidir. **Bulkhead** ise ödeme entegrasyonunun havuzunu diğer akışlardan ayırır: PSP yavaşladığında yalnız ödeme yavaşlar, arama ve rezervasyon ayakta kalır. Sınırsız retry ve devre kesicisiz tasarım, kısmi bir dış arızayı tam bir kesintiye çevirmenin en kısa yoludur.

# odetut-4 | İleri

## Soru
Ödeme başarıyla capture edildi, ancak zincirin sonraki adımı (teslim/biletleme) kalıcı olarak başarısız oldu ve tekrar denemelerle de tamamlanamadı. Sistemin tutarlılığı nasıl sağlanmalıdır?

## A
Ödeme kaydı silinir ve ilgili tüm satırlar temizlenir; hiç olmamış gibi davranmak en temiz sonucu verir
🤖 gerçekleşmiş bir para hareketi kayıt silinerek yok edilemez; PSP tarafındaki işlem yerinde durur ve denetim izi kaybolur.

## B
Saga telafisi çalışır: işlemin durumuna göre void veya refund açılır, kayıt ara durumdan çıkarılır, telafi idempotent tasarlanır ve cevapsız kalanları bir timeout işi toplar
🤖 Doğru: telafi geri sarma değil ters kayıttır ve doğru araç işlemin takas durumuna göre seçilir.

## C
Ödeme, teslim adımıyla birlikte tek bir dağıtık işleme (2PC/XA) alınır; adım düşerse koordinatör tümünü geri alır
🤖 harici PSP dağıtık işleme katılmaz ve kullanıcının ekranda geçirdiği süre boyunca kilit tutmak sistemi kilitler (gerekçe: 126).

## D
Kayıt başarılı sayılıp operasyon ekibinin gün sonu raporunda görmesi beklenir; iade elle açılır ve müşteri çağrı merkezinden bilgilendirilir
🤖 telafiyi tümüyle manuel sürece bırakmak gecikme ve tutarsızlık üretir; ara durum ve otomatik telafi modelin parçası olmalıdır.

## Doğru
B

## Açıklama
Ödeme, dağıtık bir zincirin ortasındaki adımdır ve harici PSP dağıtık işleme katılmadığı için desen **sagadır** (`126`). Ödeme bağlamında saganın kendine özgü kuralı şudur: **telafi bir rollback değil, ters kayıttır.** Para el değiştirdiyse o hareket silinmez; anlamca tersini yapan yeni bir işlem açılır. Araç seçimi işlemin durumuna bağlıdır: henüz takas edilmemiş bir işlem **void** edilebilir — müşteri ekstresinde hiç görünmez; takas olmuş bir işlem ancak **refund** ile iade edilir, ayrı bir finansal kayıttır ve günler sürebilir. İkisini karıştırmak klasik hatadır. Tasarımın diğer iki parçası da şarttır: "ödeme alındı, teslim bekliyor" durumu modelde **açıkça adlandırılmalıdır** — adlandırılmazsa akış, ödeme başarılı olduğu an tamamlanmış sayılır ve düşen teslim adımı hiç fark edilmez; ayrıca bir **timeout işi** belirli süre ara durumda kalan kayıtları toplayıp ya ileri sürmeli ya telafiyi tetiklemelidir, çünkü cevapsız kalan adımlar hiçbir hata mesajı üretmez. Telafinin kendisi de idempotent olmalıdır: "iade et" mesajı iki kez gelirse tek iade oluşmalıdır (`130`). Kayıt silmek (A) hem PSP'deki gerçeği değiştirmez hem denetim izini yok eder. 2PC (C) burada kurulamaz — harici sağlayıcı katılmaz, üstelik kullanıcının ödeme ekranında geçirdiği dakikalar boyunca kilit tutmak sistemi durdurur. Tamamen manuel süreç (D) küçük hacimde idare eder, ölçekte hem müşteri memnuniyetini hem mutabakatı bozar. Havacılıktaki tam karşılığı (capture → ticketing → otomatik refund) `254` numaralı konudadır.

# odetut-5 | Uzman

## Soru
Ödeme servisi başarılı tahsilatı outbox üzerinden Kafka'ya yayınlıyor; sipariş tamamlama tüketicisi bu olayı bazı günlerde aynı kayıt için iki kez işliyor. Doğru teşhis ve çözüm hangisidir?

## A
Bu normal bir durumdur: teslimat en az bir kez garantilidir, tüketici işlenmiş olay kaydı ve doğal iş anahtarına konmuş benzersiz kısıtla idempotent hale getirilir
🤖 Doğru: outbox yayının kaybolmamasını sağlar, tekrarı ortadan kaldırmaz; savunma tüketici tarafındadır.

## B
Outbox deseni yanlış kurulmuştur; olay veritabanı yazımıyla aynı yerel işlemde yazılırsa tekrar teslimat tümüyle ortadan kalkar
🤖 outbox dual-write boşluğunu kapatır ve olayın kaybolmamasını garanti eder; verdiği garanti "en az bir kez"dir, tekrarı engellemez.

## C
Broker tarafında exactly-once teslimat etkinleştirilir; böylece her olay tüketiciye tam olarak bir kez ulaşır ve uygulama tarafında ek önlem gerekmez
🤖 uçtan uca tam bir kez teslimat pratikte kurulmaz; dış sistem çağrısı içeren tüketicide broker garantisi tek başına yetmez.

## D
Tüketici otomatik offset commit'e alınır; offset her mesajda hemen ilerlediği için aynı kaydın yeniden işlenmesi imkânsız hale gelir
🤖 erken commit tekrarı azaltır ama bu kez çöken tüketicide mesaj **kaybolur**; ödemede kayıp, tekrardan daha ağır bir hatadır.

## Doğru
A

## Açıklama
Teşhisin doğrusu, olayı bir arıza değil **beklenen davranış** olarak okumaktır. Mesajlaşmanın normal garantisi **en az bir kez teslimattır**: tüketici mesajı işleyip offset'i commit etmeden çökerse aynı kayıt yeniden teslim edilir (`110`). **Outbox** deseni bu tabloya farklı bir katkı yapar — veritabanı yazımı ile olay yayınının ayrı düşmesi (dual-write) boşluğunu kapatır ve olayın **kaybolmamasını** garanti eder (`116`); ama tekrarı ortadan kaldırmaz, aksine "en az bir kez" tarafını pekiştirir. Dolayısıyla savunma tüketicide kurulur: işlenmiş olay kimliklerinin tutulduğu bir kayıt (dedup tablosu) ve **doğal iş anahtarına konmuş benzersiz kısıt**. Kısıt son savunma hattıdır ve uygulama mantığı bir gün atlansa bile ikinci kaydın oluşmasını fiziken engeller. "Exactly-once açarım" (C) mülakatta doğrudan eleyici bir cevaptır: broker içi kurgular belirli sınırlar içinde çalışsa da dış sistem çağrısı (PSP, biletleme) içeren bir tüketicide uçtan uca tam bir kez etkisi kurulmaz; sektörün kurduğu şey at-least-once teslimat üzerine idempotent işlemedir. Otomatik/erken offset commit (D) problemi ters uçtan bozar: tekrarı azaltırken **kayıp** riskini getirir ve ödemede kayıp, tekrardan çok daha ağır bir hatadır — tekrar idempotencyyle emilir, kayıp emilemez. Aynı savunmanın kullanıcı çift tıklaması ve ağ retry'ı için de geçerli olduğunu görmek bu konunun ana fikridir.

# odetut-6 | Uzman

## Soru
Üretimde bazı siparişler için iki ayrı tahsilat kaydı oluştuğu tespit edildi. Sorunu bulup kalıcı olarak kapatmak için doğru yaklaşım hangisidir?

## A
Uygulamaya, kayıt yazmadan önce aynı sipariş için tahsilat olup olmadığını kontrol eden bir doğrulama eklenir ve mevcut çift kayıtlar sonraki sürümde ele alınır
🤖 uygulama içi kontrol eşzamanlı isteklerde yarışa açıktır; kökü kapatmayan bu düzeltme çiftleri seyrekleştirir, bitirmez.

## B
Tahsilat tablosuna doğrudan benzersiz kısıt eklenir ve migration dağıtılır; kısıt devreye girdiği an sorun kökten kapanır
🤖 kısıt doğru araçtır ama mevcut ihlal eden satırlar temizlenmeden eklenirse migration hata verir ve dağıtım yarıda kalır.

## C
İşlenen ödeme olayları için Kafka topic'i sıfırlanır ve tüketici baştan çalıştırılır; böylece kayıtlar tutarlı biçimde yeniden üretilir
🤖 yeniden işleme mevcut çiftleri temizlemez, üstüne yenilerini ekleyebilir; ayrıca gerçekleşmiş tahsilatlar PSP tarafında yerinde durur.

## D
Mutabakat sorgusuyla çiftler ve hacim çıkarılır, correlation-id ile kök neden bulunur, iadeler açılıp kayıtlar düzeltilir, sonra idempotency anahtarı ve doğal anahtara benzersiz kısıt devreye alınır
🤖 Doğru: önce hasarı ölç ve kök nedeni bul, veriyi temizle, ardından kısıtı ekle — sıralama üretim disiplininin kendisidir.

## Doğru
D

## Açıklama
Bu soru bilgi kadar **sıralama** ölçer. Doğru yol dört adımlıdır. Önce **hasar ölçülür**: doğal iş anahtarına göre gruplayıp birden fazla kayıt üretenler çekilir (`GROUP BY ... HAVING COUNT(*) > 1` mantığı); bu liste hem iade edilecek kalemleri hem sorunun büyüklüğünü verir. Sonra **kök neden bulunur**: correlation-id ile iki isteğin uçtan uca izi sürülür (`104`, `193`) — istemci her denemede yeni bir idempotency anahtarı mı üretiyor, aynı anahtar kısıt yokluğunda iki kez mi işlendi, yoksa aynı olay iki kez mi tüketildi (`110`)? Üç kök neden üç ayrı düzeltmedir; iz sürmeden yapılan düzeltme genellikle yanlış katmana yapılır. Üçüncü adım **veri temizliğidir**: hangi kaydın geçerli sayılacağına iş birimiyle karar verilir, fazla tahsilatlar için iade açılır, kayıtlar düzeltilir. Ancak bundan sonra **benzersiz kısıt** eklenebilir — ve buradaki ayrıntı üretim görmüşlüğün en net sinyallerinden biridir: tabloda ihlal eden satırlar dururken kısıt eklemeye çalışan migration **hata verir ve dağıtım yarıda kalır**; bu yüzden B şıkkı doğru aracı yanlış sırayla kullanır. Uygulama içi "önce oku sonra yaz" kontrolü (A) eşzamanlı iki istek arasındaki boşluğa açıktır; kısıtın yerini tutmaz. Topic sıfırlayıp yeniden işlemek (C) ise mevcut çiftleri temizlemediği gibi yenilerini üretebilir ve zaten gerçekleşmiş tahsilatlar PSP tarafında olduğu yerde durur — veriyi geri sarmak parayı geri sarmaz. Kalıcı çözümün üç katmanı şudur: doğru anda üretilen idempotency anahtarı (`130`), doğal iş anahtarına konmuş benzersiz kısıt ve idempotent tüketim.
