---
slug: jira-test-yonetimi-hata-yasam-dongusu
baslik: "Jira ve Test Yönetimi: Hata Yaşam Döngüsü, Severity/Priority ve İzlenebilirlik"
kademe: Orta
sira: 229
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir hastanenin acil servisini düşün. Gelen her hasta için bir **kayıt** açılır; kayıtta şikâyet, ne zaman başladığı, hangi belirtilerin görüldüğü ve hangi tetkiklerin yapıldığı yazar. Hasta triyajdan geçer: durumu **ne kadar ağır** (tıbbi şiddet) ve **ne kadar acil müdahale gerekiyor** (sıra önceliği) ayrı ayrı değerlendirilir — parmağı kesilen hasta ağrı içindedir ama göğüs ağrısı olan önce alınır. Hasta bir doktora **atanır**, tedavi edilir, sonra kontrole çağrılır; şikâyet geçmediyse dosya yeniden açılır. Kimse "hasta iyileşti mi?" sorusunu koridorda sözlü olarak takip etmez, çünkü hasta sayısı bunu imkânsız kılar.

Bir yazılım ekibinde **Jira** bu kayıt sistemidir, **hata yaşam döngüsü (bug life cycle)** triyaj ve takip sürecidir, **şiddet (severity) / öncelik (priority)** ayrımı ise triyaj mantığının ta kendisidir. Bir e-ticaret firmasında kampanya haftasında yüzlerce kayıt üretilir; ekibi ayakta tutan şey testçilerin hafızası değil, **kaydın kalitesi ve akışın disiplinidir**. Mülakatta "Jira kullandın mı?" sorusu aslında "kayıt disiplinin var mı, önceliklendirme yapabiliyor musun, izini sürebiliyor musun?" sorusudur.

### Temel kavramlar

- **Jira.** Atlassian'ın iş takip (issue tracking) ve proje yönetim aracıdır. Yazılım ekiplerinde işin (özellik, görev, hata) tek kayıt yeri olarak kullanılır. "Hata takip aracı" olarak tanıtmak eksik bir tanımdır — hata, Jira'daki iş tiplerinden yalnızca biridir.
- **İş tipleri (issue types).** **Epic**: birden çok sprinte yayılabilen geniş hedef ("Kampanya sayfası yenileme"). **Story**: kullanıcı değeri taşıyan, kabul kriterleri olan iş ("Sepette kupon kodu girilebilsin"). **Task**: doğrudan kullanıcı değeri olmayan teknik iş ("Kupon servisi uç noktası yazılsın"). **Sub-task**: bir Story/Task'ın kırılımı. **Bug**: hatalı davranış kaydı; bağımsız açılabilir ama genellikle bir Story veya Epic'e bağlanır.
- **Alanlar (fields).** Özet (summary), açıklama (description), atanan (assignee), raporlayan (reporter), bileşen (component), etiket (label), **etkilenen sürüm (affects version)** ve **düzeltildiği sürüm (fix version)**, sprint, öncelik, şiddet, ortam. `fix version` sürüm notlarının ve "bu sürümde ne kapandı?" sorusunun temelidir.
- **İş akışı (workflow).** Bir iş tipinin geçebileceği **durumlar** ve bu durumlar arası **izinli geçişler**. Jira'da iş akışı yapılandırılabilir; her ekibin durum adları farklı olabilir ama mantık aynıdır. Geçişlere koşul konabilir (ör. "Kapandı"ya yalnız testçi geçirebilir).
- **Pano (board), backlog, sprint.** Backlog sıralanmış iş havuzudur; sprint bu havuzdan seçilip bir zaman kutusuna alınan iştir; pano işin durumunu sütunlar hâlinde gösterir (Scrum panosu sprint temellidir, Kanban panosu sürekli akıştır).
- **JQL (Jira Query Language).** Kayıtları filtrelemek için kullanılan sorgu dili. Test raporlaması ve kapanış kriteri ölçümü büyük ölçüde JQL üzerinden yapılır.
- **Şiddet (severity).** Hatanın **teknik/işlevsel etkisinin büyüklüğü**: çökme mi, veri kaybı mı, kozmetik bir bozukluk mu? Genelde **testçi** belirler ve gözleme dayanır.
- **Öncelik (priority).** Hatanın **ne kadar acil düzeltilmesi gerektiği**: iş etkisi, müşteri görünürlüğü, takvim. Genelde **ürün sahibi / triyaj kurulu** belirler ve bağlama dayanır. Bu ikisi **bağımsız eksenlerdir**; karıştırmak mülakatta en sık düşülen tuzaktır.
- **Test yönetim araçları.** **Xray**, **Zephyr** (Jira içine kurulan eklentiler) ve **TestRail** (Jira ile entegre olan ayrı ürün). Sağladıkları kavramlar: **test senaryosu (test case)**, **test seti/planı**, **test yürütme (test execution)** kaydı ve bunların gereksinim/hata kayıtlarına bağlanması. Satıcı pazarlaması "tam izlenebilirlik" vaat eder; gerçekte izlenebilirlik ancak ekip **her senaryoyu bir gereksinime bağlama disiplinini** sürdürürse oluşur — araç bunu kendiliğinden üretmez.
- **Gereksinim izlenebilirlik matrisi (requirement traceability matrix, RTM).** Her gereksinimi onu kapsayan test senaryolarına, o senaryoların son yürütme sonucuna ve açılan hatalara eşleyen tablo. İki soruyu cevaplar: "hangi gereksinim hiç test edilmedi?" ve "bu gereksinimin durumu nedir?"

```svg
<svg viewBox="0 0 660 258" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Jira iş tipleri hiyerarşisi: Epic, Story, Task, Sub-task ve Bug">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Jira iş tipleri: Bug, hiyerarşinin dışında değil içindedir</text>
  <rect x="170" y="38" width="320" height="46" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="330" y="58" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Epic — Kampanya sayfası yenileme</text>
  <text x="330" y="75" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">geniş hedef, birden çok sprinte yayılır</text>
  <polyline points="330,84 330,96 165,96 165,110" fill="none" stroke="#5D7789" stroke-width="1.4"/>
  <polyline points="330,96 495,96 495,110" fill="none" stroke="#5D7789" stroke-width="1.4"/>
  <rect x="40" y="110" width="250" height="48" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="165" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Story — Kupon kodu girişi</text>
  <text x="165" y="148" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kullanıcı değeri + kabul kriterleri</text>
  <rect x="370" y="110" width="250" height="48" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="495" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Task — Kupon servisi uç noktası</text>
  <text x="495" y="148" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">teknik iş, doğrudan kullanıcı değeri yok</text>
  <line x1="165" y1="158" x2="165" y2="178" stroke="#5D7789" stroke-width="1.4"/>
  <line x1="495" y1="158" x2="495" y2="178" stroke="#B23B3B" stroke-width="1.4" stroke-dasharray="5 4"/>
  <rect x="40" y="178" width="250" height="44" rx="9" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="165" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sub-task — Locator'ları güncelle</text>
  <text x="165" y="213" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Story'nin kırılımı</text>
  <rect x="370" y="178" width="250" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="495" y="197" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Bug — Kupon toplamı güncellenmiyor</text>
  <text x="495" y="213" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ayrı iş tipi, ilgili işe bağlanır</text>
  <text x="330" y="245" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Alanlar: özet · açıklama · atanan · bileşen · etiket · fix version · sprint · öncelik · şiddet · ortam</text>
</svg>
```

### Nasıl çalışır? / Pratikte nasıl yapılır?

**1. Hata yaşam döngüsü (bug life cycle).** Ana hat şudur:

1. **Yeni (New).** Testçi kaydı açar. Henüz kimse üstlenmemiştir.
2. **Atandı (Assigned).** Triyajdan geçer, bir geliştiriciye atanır. Bu adımda şiddet ve öncelik netleşir.
3. **Açık / Üzerinde çalışılıyor (Open, In Progress).** Geliştirici hatayı üretir ve düzeltir.
4. **Düzeltildi (Fixed / Resolved).** Geliştirici düzeltmeyi teslim eder, `fix version` alanı doldurulur. Kayıt **testçiye döner**.
5. **Yeniden test (Retest / Ready for QA).** Testçi kayıttaki adımları aynı veriyle tekrar koşar.
6. **Kapandı (Closed).** Yeniden test geçtiyse kaydı **testçi** kapatır — geliştirici değil. "Düzelttim, kapattım" akışı, düzeltmenin doğrulanmadan kapanması demektir ve iyi bir iş akışı bu geçişe izin vermez.

**Yan dallar** ana hattan ayrılır ve her biri kaydı belirli birine geri düşürür:

- **Yeniden Açıldı (Reopened).** Yeniden testte hata hâlâ üretiliyor → kayıt **geliştiriciye** geri döner. Yeni kayıt açmak yerine aynı kaydı yeniden açmak, o hatanın geçmişini tek yerde tutar.
- **Tekrarlanamadı (Not Reproducible / Cannot Reproduce).** Geliştirici hatayı üretemedi → kayıt **testçiye** döner. Testçinin işi tartışmak değil, kanıtı zenginleştirmektir: tam ortam, tarayıcı sürümü, kullanıcı hesabı, saat, video, ağ günlüğü, Playwright trace. Bu durum genellikle **kötü yazılmış hata kaydının** faturasıdır.
- **Kopya (Duplicate).** Aynı kök neden başka bir kayıtta zaten var → asıl kayda bağlanır, kapatılır.
- **Hata Değil / Tasarım Böyle (Not a Bug / As Designed) ve Düzeltilmeyecek (Won't Fix).** Davranış kasıtlıdır ya da düzeltme maliyeti değerinden yüksektir. Bu karar **testçinin tek başına** verdiği bir karar olmamalıdır; ürün sahibinin onayı gerekir.
- **Ertelendi (Deferred).** Gerçek bir hatadır ama bu sürümde düzeltilmeyecektir; bir sonraki sürüme/backlog'a taşınır. Kapatılmaz, çünkü kapatılan hata görünmez olur.

```svg
<svg viewBox="0 0 660 302" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Hata yaşam döngüsü ana hattı ve yan dalları">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hata yaşam döngüsü: ana hat ve yan dallar</text>
  <rect x="30" y="38" width="186" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="123" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Yeni</text>
  <text x="123" y="74" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">testçi kaydı açar</text>
  <line x1="216" y1="61" x2="235" y2="61" stroke="#5D7789" stroke-width="1.6"/>
  <rect x="237" y="38" width="186" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Atandı</text>
  <text x="330" y="74" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">triyaj: şiddet + öncelik</text>
  <line x1="423" y1="61" x2="442" y2="61" stroke="#5D7789" stroke-width="1.6"/>
  <rect x="444" y="38" width="186" height="46" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="537" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Üzerinde çalışılıyor</text>
  <text x="537" y="74" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">geliştirici üretir ve düzeltir</text>
  <polyline points="537,84 537,104 123,104 123,124" fill="none" stroke="#5D7789" stroke-width="1.6"/>
  <rect x="30" y="124" width="186" height="46" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="123" y="144" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Düzeltildi</text>
  <text x="123" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">fix version dolar, testçiye döner</text>
  <line x1="216" y1="147" x2="235" y2="147" stroke="#5D7789" stroke-width="1.6"/>
  <rect x="237" y="124" width="186" height="46" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="144" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Yeniden test</text>
  <text x="330" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">aynı adımlar, aynı veri</text>
  <line x1="423" y1="147" x2="442" y2="147" stroke="#25804C" stroke-width="1.6"/>
  <rect x="444" y="124" width="186" height="46" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.6"/>
  <text x="537" y="144" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Kapandı</text>
  <text x="537" y="160" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">kapatan: testçi</text>
  <rect x="30" y="188" width="600" height="98" rx="11" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="209" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yan dallar — her biri kaydı birine geri düşürür</text>
  <text x="330" y="230" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Yeniden Açıldı (Reopened): yeniden testte hata hâlâ var → geliştiriciye döner</text>
  <text x="330" y="248" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Tekrarlanamadı (Not Reproducible): geliştirici üretemedi → testçiye döner, kanıt zenginleşir</text>
  <text x="330" y="266" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Kopya (Duplicate) · Tasarım Böyle (As Designed) · Düzeltilmeyecek (Won't Fix) → triyaj kararı</text>
  <text x="330" y="280" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Ertelendi (Deferred): gerçek hata, bu sürümde değil — kapatılmaz, taşınır</text>
</svg>
```

**2. İyi bir hata kaydı nasıl yazılır.** Kaydın tek amacı vardır: **geliştirici, seni hiç aramadan hatayı kendi makinesinde üretebilsin.** Gerekli parçalar:

1. **Net başlık.** Nerede, ne oluyor. Kötü: "Sepet bozuk". İyi: "Sepette kupon uygulandığında genel toplam güncellenmiyor (ara toplam doğru)".
2. **Yeniden üretme adımları.** Numaralı, eksiksiz, kullanılan veriyle birlikte. "Sepete ürün ekle" yeterli değildir; "250 TL'lik Ürün A'yı 1 adet ekle" gerekir.
3. **Beklenen sonuç vs gerçekleşen sonuç.** İkisi ayrı ayrı yazılır. Bu ikisi olmadan kayıt bir şikâyettir, hata kaydı değildir; ayrıca "beklenen" alanı, kaydın gerçekten bir hata mı yoksa yanlış anlaşılma mı olduğunu ortaya çıkarır.
4. **Ortam bilgisi.** Test/hazırlık (staging)/canlı, tarayıcı ve sürümü, işletim sistemi, cihaz/çözünürlük, kullanıcı hesabı türü (üye/misafir), tarih-saat.
5. **Kanıt.** Ekran görüntüsü, video, tarayıcı konsolu ve ağ günlüğü, sunucu log parçası, otomasyondan geliyorsa **Playwright trace** dosyası.
6. **Tekil ve atomik olması.** Bir kayıt = bir hata. Üç hatayı tek kayda yazmak, ikisi düzelip biri kalınca kaydın ne durumda olduğunu belirsizleştirir.
7. **Kullanıcı etkisi.** Kaç kullanıcıyı, hangi akışta, ne kadar sıklıkta etkiliyor — önceliğin gerekçesi budur.

Kötü kaydın maliyeti somuttur: geliştirici üretemez, kayıt **Tekrarlanamadı** olur, testçiye döner, tekrar denenir, tekrar atanır. Bu gidip gelme (ping-pong) hem takvim yakar hem de gerçek hataların "bunu zaten üretemiyoruz" diye küçümsenmesine yol açar.

**3. Şiddet (severity) ile öncelik (priority) farkı.** Şiddet **teknik etkiyi**, öncelik **iş aciliyetini** ölçer; dördü de birleşebilir:

- **Yüksek şiddet + yüksek öncelik.** Ödeme adımı bütün kullanıcılarda hata veriyor. Tartışmasız: hemen.
- **Yüksek şiddet + düşük öncelik.** Yılda birkaç kez kullanılan "fatura adresini dışa aktar" ekranı uygulamayı çökertiyor. Etki büyük (çökme) ama kaç kişiyi ne zaman etkilediği küçük; sonraki sürüme alınabilir.
- **Düşük şiddet + yüksek öncelik.** Ana sayfadaki logoda firma adı yanlış yazılmış ve yarın kampanya duyurusu var. İşlev tam çalışıyor ama marka görünürlüğü nedeniyle bugün düzeltilmeli.
- **Düşük şiddet + düşük öncelik.** Yardım sayfasında bir metin hizalaması bozuk.

Bu ayrımı **tek cümlede** verebilmek mülakatta neredeyse kesin sorulan bir maddedir: "Şiddet hatanın ne kadar büyük olduğunu, öncelik ne kadar acil düzeltileceğini söyler; birincisini genelde testçi, ikincisini ürün sahibi belirler."

```svg
<svg viewBox="0 0 660 292" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Şiddet ve öncelik matrisinin dört bileşimi ve e-ticaret örnekleri">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Şiddet (severity) × Öncelik (priority): dört bileşim de gerçektir</text>
  <text x="234" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Düşük öncelik</text>
  <text x="492" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Yüksek öncelik</text>
  <text x="57" y="106" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Yüksek</text>
  <text x="57" y="122" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">şiddet</text>
  <text x="57" y="208" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Düşük</text>
  <text x="57" y="224" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">şiddet</text>
  <rect x="110" y="62" width="248" height="94" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="234" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Yüksek şiddet · düşük öncelik</text>
  <text x="234" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"Fatura adresini dışa aktar" ekranı</text>
  <text x="234" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çöküyor — yılda birkaç kez kullanılıyor</text>
  <text x="234" y="144" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">etki büyük, aciliyet düşük → sonraki sürüm</text>
  <rect x="368" y="62" width="248" height="94" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="492" y="86" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yüksek şiddet · yüksek öncelik</text>
  <text x="492" y="108" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Ödeme adımı tüm kullanıcılarda</text>
  <text x="492" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">hata veriyor, sipariş tamamlanmıyor</text>
  <text x="492" y="144" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">sürüm durdurucu (blocker) → hemen</text>
  <rect x="110" y="164" width="248" height="94" rx="10" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.4"/>
  <text x="234" y="188" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">Düşük şiddet · düşük öncelik</text>
  <text x="234" y="210" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Yardım sayfasında metin hizalaması</text>
  <text x="234" y="226" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bozuk, içerik okunabiliyor</text>
  <text x="234" y="246" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">backlog'a alınır</text>
  <rect x="368" y="164" width="248" height="94" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="492" y="188" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Düşük şiddet · yüksek öncelik</text>
  <text x="492" y="210" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Ana sayfadaki logoda firma adı</text>
  <text x="492" y="226" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yanlış yazılmış, yarın kampanya var</text>
  <text x="492" y="246" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">işlev sağlam, marka riski → bugün</text>
  <text x="330" y="282" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Şiddeti genelde testçi (gözleme dayalı), önceliği ürün sahibi (bağlama dayalı) belirler.</text>
</svg>
```

**4. Test senaryosu yönetimi ve izlenebilirlik.** Jira tek başına test senaryosu yönetmez; bunun için **Xray** veya **Zephyr** gibi eklentiler ya da **TestRail** gibi entegre ürünler kullanılır. Getirdikleri temel kavramlar:

1. **Test senaryosu (test case).** Ön koşul, adım, beklenen sonuç ve test verisini taşıyan kayıt. Jira'da ayrı bir iş tipi olarak durur, böylece bir Story'ye bağlanabilir.
2. **Test seti / test planı.** Belirli bir amaç için (regresyon paketi, kampanya duman paketi) gruplanmış senaryolar.
3. **Test yürütme (test execution).** Bir senaryonun **belirli bir sürümde, belirli bir ortamda** koşulmasının kaydı: sonuç (geçti/kaldı/bloke), tarih, koşan kişi/CI işi, kanıt. Aynı senaryonun onlarca yürütme kaydı olabilir — **senaryo ile yürütmeyi karıştırmamak** önemlidir.
4. **Bağlantılar.** Gereksinim (Story) ↔ test senaryosu ↔ yürütme ↔ hata zinciri kurulur. RTM bu zincirin tablo hâlidir ve iki boşluğu görünür kılar: hiç senaryosu olmayan gereksinim, hiç koşulmamış senaryo.

**5. Otomasyon sonuçlarını Jira'ya bağlamak — ve tuzağı.** CI koşumu bittiğinde sonuçlar test yönetim eklentisine gönderilir; format genelde JUnit XML'dir. Böylece bir **test yürütme kaydı** oluşur. Artık rapor, "sürüm 3.4.0'da regresyon paketinin %96'sı geçti" cümlesini kanıtlarıyla üretebilir. Buraya kadar sağlıklıdır.

Tuzak, bir adım ötesindedir: **"başarısız her testi otomatik olarak Jira'da yeni bir Bug açsın"** yapılandırması. Kulağa verimli gelir, pratikte backlog'u çöpe çevirir, çünkü:

- **Kararsız (flaky) testler** her koşumda farklı yerlerde düşer ve her düşüşte yeni kayıt üretir — gerçek hata olmayan yüzlerce kayıt.
- **Tek kök neden** onlarca testi birden düşürür (ör. giriş servisi bozuksa giriş gerektiren bütün senaryolar kalır) — onlarca kopya kayıt.
- Gürültü büyüdükçe ekip Jira'daki hata sayısına güvenmeyi bırakır; **gerçek hatalar da görünmez olur**. Kapanış kriteri olarak kullanılan "açık hata sayısı" metriği anlamını yitirir.

Sağlıklı yaklaşım şudur. Koşum sonucunu **her zaman** test yürütme kaydına yazın; bu izlenebilirlik için gereklidir. Ama **Bug kaydı açmayı insan triyajına bırakın**. En fazla şu kadarı otomatikleştirilebilir: arka arkaya birden çok koşumda aynı şekilde düşen ve kararsız olarak etiketlenmemiş testler için otomatik kayıt açmak. Kararsız testler ise ayrı bir "flaky" etiketiyle karantinaya alınır.

**6. Kapanış kriteri ve triyaj toplantısı.** Sürümün "test edildi" sayılması için kullanılan tipik ölçütler JQL ile ölçülür:

```
project = ETIC AND type = Bug AND fixVersion = "3.4.0" AND status != Closed
project = ETIC AND type = Bug AND priority in (Highest, High) AND status != Closed
project = ETIC AND type = Bug AND status = Reopened AND sprint in openSprints()
```

Çıkış kriteri "hiç açık hata olmayacak" değildir; bu gerçekçi değildir. Gerçekçi kriter şudur: **açık kritik veya yüksek öncelikli hata sıfır olacak; kalan hatalar da şiddet dağılımıyla birlikte yazılı olarak kabul edilmiş olacak.**

E-ticaret örneğine bakalım. Kampanya (Black Friday) öncesi ekip bir **hata triyaj toplantısı** yapar. Masada QA, ürün sahibi, geliştirme lideri ve müşteri hizmetleri temsilcisi bulunur. Her açık kayıt tek tek okunur. Şiddet **gözleme** göre, öncelik ise **kampanya günü ciro etkisine** göre yeniden değerlendirilir. Toplantının çıktısı üç kümedir: kampanya öncesi düzeltilecekler, **Ertelendi** olarak sonraki sürüme taşınanlar ve kapatılanlar (kopya kayıt ya da tasarım böyle). Bu üç kümeyi kayıt üzerinden gösteremeyen bir ekip, kampanya günü hangi riski taşıdığını da bilmiyor demektir.

### Mülakatta ne sorulur / tuzaklar

- **"Şiddet (severity) ile öncelik (priority) farkı nedir? Örnek ver."** — Neredeyse kesin gelir. Tanımı söylemek yetmez, **çapraz iki örneği** (yüksek şiddet/düşük öncelik ve düşük şiddet/yüksek öncelik) verebilmek beklenir. "İkisi aynı şey" veya "yüksek şiddet her zaman yüksek önceliktir" demek elenme sebebidir.
- **"İyi bir hata kaydında ne bulunur?"** — Beklenen liste: net başlık, numaralı yeniden üretme adımları + kullanılan veri, beklenen vs gerçekleşen sonuç, ortam bilgisi, kanıt, tek hata, kullanıcı etkisi.
- **"Bir hatayı kim kapatır?"** — Testçi. Geliştirici yalnız "Düzeltildi" durumuna alır. Bu, doğrulanmamış düzeltmenin kapanmasını engelleyen bir iş akışı kuralıdır.
- **"Geliştirici 'bende üretemiyorum' dedi, ne yaparsın?"** — Beklenen: tartışmak değil kanıtı zenginleştirmek (ortam, sürüm, hesap, saat, video, log, trace), gerekirse birlikte ekran paylaşımıyla üretmek. Kaydı öfkeyle yeniden açmak değil, üretilebilir hâle getirmek.
- **"Aynı hata düzeltilmemişse yeni kayıt mı açarsın?"** — Hayır; aynı kaydı **Yeniden Açıldı (Reopened)** yaparsın, böylece o hatanın bütün geçmişi tek yerde kalır ve "kaç kez geri döndü?" ölçülebilir.
- **"Otomasyon başarısız olunca otomatik hata açalım mı?"** — Tuzak soru. Beklenen: koşum sonucu her zaman test yürütme kaydına yazılır. Ama otomatik Bug açmak gürültü üretir; sebebi kararsız testler ve tek kök nedenli toplu düşüşlerdir. Kayıt açma kararı triyajda verilmelidir.
- **"RTM ne işe yarar?"** — Hangi gereksinimin hangi testle kapsandığını, o testlerin son sonucunu ve açılan hataları gösterir. Ayrıca kapsanmayan gereksinimi ortaya çıkarır. "Test sayısını raporlamak için" demek yüzeysel bir cevaptır.
- **"Sürümü ne zaman çıkarılabilir sayarsın?"** — Yazılı çıkış kriterine göre. Kriterler: açık kritik veya yüksek öncelikli hata yok, regresyon paketi yeşil, kalan hatalar şiddet dağılımıyla kabul edilmiş. "Bütün hatalar kapandığında" cevabı gerçekçi değildir.
- **Terim tuzağı:** test **senaryosu** ile test **yürütmesini** karıştırmak. Senaryo bir kez yazılır, yürütme her sürümde/ortamda yeniden oluşur.

### Tek cümlede

Jira, işin (Epic/Story/Task/Sub-task/Bug) ve durumunun tek kayıt yeridir; hata yaşam döngüsü de kaydın her an kimde olduğunu belli eder. İyi yazılmış bir kayıt bu döngüyü hızlandırır: adımlar, beklenen ile gerçekleşen sonuç, ortam ve kanıt. **Şiddet teknik etkiyi, öncelik iş aciliyetini** ölçtüğü için ikisi bağımsızdır. Xray/Zephyr/TestRail ile kurulan gereksinim–senaryo–yürütme–hata zinciri (RTM) ise sürüm kapanış kararını kanıta bağlar. Otomasyon sonuçları bu zincire yazılmalıdır; ama otomatik hata açmak, kararsız testlerin gürültüsü yüzünden dikkatle sınırlanmalıdır.

# jira-1 | Temel
## Soru
E-ticaret ekibi "Kampanya sayfası yenileme" çalışmasını Jira'da kırıyor. İş tipleri arasındaki ilişki en doğru nasıl kurulur?
## A
Epic bir Story'nin alt kırılımıdır; en üstteki kapsayıcı iş tipi Story'dir ve Epic'ler ona bağlanır
🤖 hiyerarşi tersine çevrilmiş; Epic üstteki kapsayıcıdır, Story ona bağlanır.
## B
Bug yalnızca Epic seviyesinde açılabilir; Story veya Task ile ilişkilendirilemez
🤖 Bug bağımsız açılabilir ve genellikle ilgili Story ya da Task ile ilişkilendirilir.
## C
Sub-task bağımsız bir iş tipidir ve tanım gereği hiçbir üst işe bağlanmaz, backlog'da tek başına durur
🤖 Sub-task tanımı gereği bir üst işin kırılımıdır, tek başına var olamaz.
## D
Epic geniş hedefi kapsar, altında Story ve Task'lar bulunur, Sub-task bunların kırılımıdır; Bug ise ayrı bir iş tipi olarak ilgili işe bağlanır
🤖 Doğru: Epic → Story/Task → Sub-task hiyerarşisi standarttır ve Bug bu hiyerarşinin dışında değil, içine bağlanan ayrı bir tiptir.
## Doğru
D
## Açıklama
Jira'da **Epic**, birden çok sprinte yayılabilen geniş bir hedefi ("Kampanya sayfası yenileme") temsil eder; altında kullanıcı değeri taşıyan **Story**'ler ("Sepette kupon kodu girilebilsin") ve doğrudan kullanıcı değeri olmayan teknik **Task**'lar ("Kupon servisi uç noktası") bulunur. **Sub-task** bunların daha küçük parçalara kırılmış hâlidir ve tanımı gereği bir üst işe bağlıdır — bu yüzden C yanlıştır. **Bug** ise ayrı bir iş tipidir; bağımsız açılabilir ama izlenebilirlik için ilgili Story veya Epic'e bağlanması beklenir, dolayısıyla B'nin "yalnızca Epic seviyesinde" kısıtı gerçek dışıdır. A ise hiyerarşiyi tam ters kurar; Story, Epic'i kapsayan değil Epic'in kapsadığı iş tipidir.

# jira-2 | Orta
## Soru
Bir testçi şu kaydı açtı: başlık "Sepet bozuk", açıklama "çalışmıyor, ekran görüntüsü ekte", ekte tek bir ekran görüntüsü var. Kayıt geliştiriciye atandı. Bu kaydın temel sorunu ve muhtemel sonucu nedir?
## A
Sorun yok; başlığın kısa olması iyidir, asıl eksiklik "atanan" alanının doldurulmamış olmasıdır
🤖 kayıt zaten atanmış durumda ve kısa başlık değil belirsiz başlık sorundur.
## B
Yeniden üretme adımları, beklenen vs gerçekleşen sonuç ve ortam bilgisi yok; bu eksiklik geliştiriciyle gidip gelmeye ve kaydın "Tekrarlanamadı" ile geri dönmesine yol açar
🤖 Doğru: geliştirici hatayı kendi makinesinde üretemeyeceği için kayıt testçiye geri düşer, takvim boşa yanar.
## C
Ekran görüntüsü eklendiği için kayıt yeterlidir; yeniden üretme adımlarını bulmak geliştiricinin sorumluluğudur
🤖 kaydın amacı geliştiricinin hatayı aramadan üretebilmesidir, adımları çıkarmak testçinin işidir.
## D
Kayıt içerik olarak eksiksizdir; tek eksik şiddet alanının doldurulmamış olmasıdır ve bu yüzden triyajda sıraya girmez
🤖 şiddet alanı eksik olsa da asıl eksik, hatayı üretmeyi sağlayan bilgilerin tamamının yokluğudur.
## Doğru
B
## Açıklama
Bir hata kaydının tek işlevsel amacı, **geliştiricinin testçiyi hiç aramadan hatayı kendi ortamında üretebilmesidir**. Bunun için gereken asgari bilgi: net başlık (nerede, ne oluyor), numaralı yeniden üretme adımları ve kullanılan test verisi, **beklenen sonuç ile gerçekleşen sonucun ayrı ayrı** yazılması, ortam bilgisi (ortam adı, tarayıcı ve sürümü, cihaz, hesap türü, zaman) ve kanıt. Örnekteki kayıt bunların neredeyse hepsinden yoksundur. Ekran görüntüsü tek başına yalnızca son anı gösterir, oraya nasıl gelindiğini göstermez — bu yüzden C'nin "yeterli" iddiası yanlıştır ve adım çıkarma işini geliştiriciye yükler. A ve D ise sorunu tek bir eksik alana indirger; oysa kayıt atanmış durumdadır ve şiddet alanı doldurulsa bile hata hâlâ üretilemez. Böyle bir kaydın tipik sonu **Tekrarlanamadı (Not Reproducible)** durumuyla testçiye geri dönmesidir; bu gidip gelme hem zaman yakar hem de gerçek hataların küçümsenmesine zemin hazırlar.

# jira-3 | Orta
## Soru
Aynı gün iki hata bildirildi: (1) ana sayfadaki logoda firma adı yanlış yazılmış — işlevsel bir bozukluk yok, ama yarın büyük kampanya duyurusu var; (2) yılda birkaç kez kullanılan "fatura adresini dışa aktar" ekranında uygulama tamamen çöküyor. Şiddet (severity) ve öncelik (priority) nasıl atanır?
## A
Logo hatası düşük şiddet / yüksek öncelik; dışa aktarma çökmesi yüksek şiddet / düşük öncelik
🤖 Doğru: şiddet teknik etkiyi (çökme büyüktür, yazım hatası küçüktür), öncelik iş aciliyetini (kampanya yarın) ölçer.
## B
İkisi de yüksek şiddet ve yüksek önceliktir, çünkü her ikisi de canlı ortamda görülen gerçek hatalardır
🤖 canlıda olmak tek başına hem şiddeti hem önceliği yükseltmez; ikisi ayrı ölçütlerle değerlendirilir.
## C
Şiddet ve öncelik aynı kavramın iki adıdır; bu yüzden her iki kayda da tek bir değer verilir
🤖 biri teknik etkiyi diğeri iş aciliyetini ölçer ve birbirinden bağımsız olarak birleşebilirler.
## D
Logo hatası yüksek şiddet, dışa aktarma çökmesi düşük şiddettir; çünkü şiddet, özelliğin ne sıklıkta kullanıldığına göre belirlenir
🤖 kullanım sıklığı önceliği etkiler, şiddeti değil; şiddet gözlenen teknik etkinin büyüklüğüdür.
## Doğru
A
## Açıklama
**Şiddet (severity)** hatanın **teknik/işlevsel etkisinin büyüklüğünü** ölçer ve genelde testçi tarafından gözleme dayanarak verilir: uygulamanın tamamen çökmesi yüksek şiddettir, bir yazım hatası düşük şiddettir. **Öncelik (priority)** ise **ne kadar acil düzeltilmesi gerektiğini** ölçer ve genelde ürün sahibi tarafından iş bağlamına göre verilir: yarın kampanya duyurusu yapılacak bir ana sayfadaki marka hatası bugün düzeltilmelidir, yılda birkaç kez açılan bir ekranın çökmesi ise sonraki sürüme kalabilir. Bu yüzden doğru atama çapraz olur. B, "canlıda olmak" ölçütünü her iki eksene birden uygulayarak triyajı anlamsızlaştırır — her hata yüksek öncelikli olursa hiçbiri öncelikli değildir. C, mülakatta en sık düşülen tuzaktır: iki kavramı eşitlemek. D ise kullanım sıklığını şiddet ölçütü sanır; sıklık bir **iş etkisi** göstergesidir ve önceliği besler, gözlenen teknik etkiyi değiştirmez.

# jira-4 | İleri
## Soru
İki kayıt aynı gün ilerledi: (1) geliştiricinin "Düzeltildi" yaptığı bir hata, yeniden testte hâlâ aynı adımlarla üretiliyor; (2) başka bir kayıt için geliştirici "bende hiç üretemedim" notu bıraktı. Hata yaşam döngüsünde doğru işleyiş nedir?
## A
Her iki kayıt da kapatılır ve yerlerine sıfırdan iki yeni kayıt açılır; böylece geçmiş temizlenmiş olur
🤖 kaydı kapatıp yenisini açmak hatanın geçmişini ve kaç kez geri döndüğü bilgisini kaybettirir.
## B
Her iki kayıt da "Kopya (Duplicate)" olarak işaretlenip asıl kayıtlara bağlanır
🤖 Kopya durumu yalnız aynı kök nedenin başka bir kayıtta zaten var olduğu durumda kullanılır.
## C
Hâlâ üretilen hata "Yeniden Açıldı (Reopened)" olup geliştiriciye döner; üretilemeyen kayıt "Tekrarlanamadı" olup testçiye döner
🤖 Doğru: her yan dal kaydı belirli bir tarafa geri düşürür ve o tarafın yapması gereken iş nettir.
## D
Her iki kayıt da "Tasarım Böyle (As Designed)" olarak kapatılır, çünkü geliştirici davranışı doğrulamış sayılır
🤖 bu durum yalnız davranışın kasıtlı olduğu hâller içindir ve ürün sahibinin onayını gerektirir.
## Doğru
C
## Açıklama
Hata yaşam döngüsünün yan dalları, kaydın **kimin masasında** olduğunu belirsizlikten kurtarır. Yeniden testte hâlâ üretilebilen bir hata **Yeniden Açıldı (Reopened)** durumuna alınır ve geliştiriciye geri döner; yeni kayıt açılmaz, çünkü aynı kayıt üzerinde kalmak o hatanın bütün geçmişini (kaç kez geri döndüğü dâhil) tek yerde tutar — bu yüzden A yanlıştır ve ayrıca ölçümü bozar. Geliştiricinin üretemediği kayıt **Tekrarlanamadı (Not Reproducible)** durumuyla testçiye döner; buradaki doğru davranış tartışmak değil kanıtı zenginleştirmektir: birebir yeniden üretim adımları, tam ortam adı, tarayıcı sürümü, kullanılan hesap, saat, video, ağ günlüğü, otomasyondan geliyorsa Playwright trace dosyası. B'deki **Kopya (Duplicate)** yalnızca aynı kök nedenin başka bir kayıtta zaten bulunduğu durumda kullanılır; burada böyle bir bilgi yoktur. D'deki **Tasarım Böyle (As Designed)** ise davranışın kasıtlı olduğu hâllere aittir ve testçinin ya da geliştiricinin tek başına vereceği bir karar değil, ürün sahibinin onayladığı bir triyaj kararıdır.

# jira-5 | İleri
## Soru
Bir ekip CI'ı, gecelik koşumda başarısız olan **her** testi Jira'da otomatik olarak yeni bir Bug kaydına dönüştürecek şekilde yapılandırdı. İki hafta sonra backlog yüzlerce kayıtla doldu, ekip artık açık hata sayısına güvenmiyor. Sorunun kökü ve doğru yaklaşım nedir?
## A
Kök sorun Jira'nın kayıt kapasitesidir; kayıtlar ayrı bir projeye taşınırsa backlog temizlenir ve sayı yeniden anlamlı hâle gelir
🤖 sorun kayıtların nerede durduğu değil, gerçek olmayan kayıtların üretilmesidir; taşımak gürültüyü yalnız gizler.
## B
Kararsız (flaky) testler ve tek kök nedenin çok sayıda testi birden düşürmesi gürültü üretiyor; koşum sonucu test yürütme kaydına yazılır, Bug açma kararı triyaja kalır
🤖 Doğru: izlenebilirlik yürütme kaydıyla korunur, kayıt açma kararı ise insan triyajının işidir.
## C
Otomatik kayıt açmak her koşulda doğru bir uygulamadır; ekibin yapması gereken kayıtları daha hızlı kapatmayı öğrenmektir
🤖 gerçek olmayan kayıtları hızlı kapatmak da zaman yakar ve gerçek hataları gürültü içinde görünmez bırakır.
## D
Çözüm, CI'da başarısız olan testleri otomatik olarak atlayıp (skip) raporu yeşile döndürmek ve kayıtları hiç açmamaktır
🤖 başarısızlığı gizlemek riski ortadan kaldırmaz, yalnız görünmez kılar ve gerçek hataları da saklar.
## Doğru
B
## Açıklama
Otomatik kayıt açma iki yapısal nedenle gürültü üretir. Birincisi **kararsız (flaky) testlerdir**: her koşumda farklı yerlerde düşerler ve her düşüş yeni bir kayıt doğurur, oysa ortada bir ürün hatası yoktur. İkincisi **tek kök nedenin toplu etkisidir**: giriş servisi bozulduğunda giriş gerektiren bütün senaryolar birden kalır ve onlarca kopya kayıt açılır. Gürültü büyüdükçe "açık hata sayısı" gibi kapanış kriterleri anlamını yitirir; ekip Jira'ya güvenmeyi bırakınca gerçek hatalar da görünmez olur. Doğru yaklaşım ikiyi ayırmaktır: koşum sonucu **her zaman** test yürütme (execution) kaydına yazılır — izlenebilirlik böyle korunur — ama Bug kaydı açmak insan triyajının kararıdır; en fazla, kararsız etiketi taşımayan ve arka arkaya birden çok koşumda aynı şekilde düşen testler için otomatik kayıt açılabilir. A sorunu depolama yeri sanır ve gürültüyü yalnız başka bir yere taşır. C, kayıtları hızlı kapatmayı çözüm sayar; oysa gerçek olmayan yüzlerce kaydı okumak ve kapatmak da ekibin zamanını yakar. D ise başarısızlığı gizleyerek raporu yeşile boyar — bu, riski ortadan kaldırmaz, yalnız ölçülemez hâle getirir.

# jira-6 | İleri
## Soru
Sürüm kapanış toplantısında şu soru soruldu: "Hangi gereksinimin hangi test senaryolarıyla kapsandığını, o senaryoların son yürütme sonucunu ve ilgili açık hataları tek bakışta görebiliyor muyuz?" Bu soruyu doğrudan cevaplayan araç/kavram hangisidir ve neden?
## A
Sprint burndown grafiği; sprint boyunca kalan iş miktarının günlere göre azalışını gösterdiği için kapsamı da ortaya koyar
🤖 burndown kalan iş miktarını gösterir, gereksinim ile test senaryosu arasında hiçbir eşleme kurmaz.
## B
`assignee = currentUser()` gibi bir JQL filtresi; kayıtların kime atandığını listelediği için kapsam bilgisi verir
🤖 bu filtre iş dağılımını gösterir, hangi gereksinimin test edildiğine dair bilgi taşımaz.
## C
Allure raporundaki toplam başarı yüzdesi; koşulan testlerin ne kadarının geçtiğini gösterdiği için gereksinim kapsamını da kanıtlar
🤖 başarı yüzdesi koşulan testlere dairdir; hiç senaryosu yazılmamış bir gereksinimi hiçbir zaman göstermez.
## D
Gereksinim izlenebilirlik matrisi (RTM); her gereksinimi kapsayan senaryolara, onların son yürütme sonucuna ve açılan hatalara eşlediği için kapsanmayan gereksinimi de görünür kılar
🤖 Doğru: RTM tam olarak gereksinim–senaryo–yürütme–hata zincirini tablolaştırdığı için bu soruyu doğrudan cevaplar.
## Doğru
D
## Açıklama
**Gereksinim izlenebilirlik matrisi (requirement traceability matrix, RTM)**, her gereksinimi (Jira'da genelde bir Story) onu kapsayan test senaryolarına, o senaryoların **son yürütme (execution) sonucuna** ve bu yürütmelerden doğan hata kayıtlarına eşleyen tablodur. Değeri yalnızca durum göstermesi değil, **boşlukları göstermesidir**: hiç senaryosu yazılmamış bir gereksinim ve hiç koşulmamış bir senaryo ancak burada fark edilir. C şıkkının en sinsi tarafı da budur — bir Allure raporu yalnızca **koşulan** testlerin sonucunu bilir, hiç yazılmamış bir testin eksikliğini asla gösteremez, dolayısıyla %100 başarı yüzdesi kapsam kanıtı değildir. A'daki burndown grafiği kalan iş miktarının zamana göre azalışını gösteren bir ilerleme aracıdır ve gereksinim–test eşlemesi hakkında hiçbir şey söylemez. B'deki atama filtresi ise iş dağılımını gösterir; "kim neyle uğraşıyor" sorusunu cevaplar, "hangi gereksinim test edildi" sorusunu değil. RTM'yi kurmayı Xray, Zephyr veya TestRail gibi araçlar kolaylaştırır, ancak matrisi anlamlı kılan şey aracın kendisi değil, ekibin her senaryoyu bir gereksinime bağlama disiplinidir.
