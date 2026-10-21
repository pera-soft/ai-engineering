---
baslik: "FoSA Tartışma Soruları — Bölüm Bölüm Öz-Sınav"
yazar: "Mark Richards & Neal Ford (O'Reilly, 2. Baskı Eki)"
tarih: "2026-07-03"
url: "https://fundamentalsofsoftwarearchitecture.com"
etiketler: 1,2,4,5,6,8,9,10,11,12
---

### Bu metin ne anlatıyor?
Bu bir anlatım metni değil, bir **soru bankasıdır**. *Fundamentals of Software Architecture*
(Yazılım Mimarisinin Temelleri) kitabının her bölümünün sonunda yazarların koyduğu tartışma
soruları burada Türkçeye çevrildi. Cevaplar bilerek verilmedi: soruların işi seni kitaba geri
göndermek ve bildiğini **kapalı kitapla** hatırlamaya zorlamak.

Bu yüzden burada diyagram yok — anlatılan bir konu değil, sınanan bir bilgi var.

**Nasıl kullanılır:** Bir bölümü çalıştıktan sonra soruları **kitaba bakmadan** yanıtlamayı
dene; emin olmadıklarını sağ paneldeki Değerlendir'e yazıp öğretmene puanlat. Cevabı hemen
aramak yerine önce zorlanmak, öğrenmeyi kalıcı kılan şeyin ta kendisidir (buna *geri getirme
pratiği*, retrieval practice denir).

### Sorularda geçen kısaltmalar
- **FoSA**: *Fundamentals of Software Architecture* — bu soruların geldiği kitap.
- **Anti-desen** (anti-pattern): İlk bakışta makul görünen ama tekrar tekrar zarar verdiği
  görülmüş yaygın çözüm. Yani "yapılmaması gereken kalıp".
- **Karakteristik / "-ility"**: Sistemin kalite özelliği — ölçeklenebilirlik, güvenilirlik,
  test edilebilirlik gibi. İngilizcede çoğu `-ility` ile bittiği için kitap onlara toplu
  olarak böyle der.
- **Domain** (alan): İşin kendisi — sipariş, ödeme, stok gibi. Teknik katmanın karşıtı.
- **Quantum** (mimari kuantum): Kendi başına yayına alınabilen, çalışması için başka bir
  parçaya muhtaç olmayan en küçük bağımsız birim.

## Bölüm 1 — Giriş (Hafta 5)
- Yazılım mimarisini tanımlayan dört boyut nedir?
- Mimari karar ile tasarım ilkesi (design principle) arasındaki fark nedir?
- Bir yazılım mimarından beklenen sekiz temel beklentiyi say.
- Yazılım Mimarisinin Birinci Yasası nedir?

## Bölüm 2 — Mimari Düşünme (Hafta 5)
- Bir kararın mimariyle mi tasarımla mı ilgili olduğunu belirleyen üç ölçüt nedir?
- Bilgi piramidindeki üç bilgi düzeyini (bildiklerin · bildiğini bilmediklerin · bilmediğini
  bilmediklerin) birer örnekle say.
- Mimar için teknik **genişlik** (çok konuda yeterli bilgi) neden teknik **derinlikten**
  (tek konuda uzmanlık) daha önemlidir?
- Mimar olarak elini kirletmeye devam etmenin (hands-on kalmanın — kod yazmayı sürdürmenin)
  yolları nelerdir?

## Bölüm 3 — Modülerlik (Hafta 5, 9)
- Modülerlik (parçalara ayrılmışlık) ile granülarite (parçaların ne kadar küçük olduğu)
  arasındaki farkı birer örnekle açıkla.
- Bağlaşım (coupling — parçaların birbirine ne kadar yapışık olduğu) ile kohezyon
  (cohesion — bir parçanın içindeki işlerin birbirine ne kadar ait olduğu) arasındaki fark
  nedir?
- Connascence terimi ne anlama gelir? (Türkçesi kabaca *birlikte-değişme bağı*: biri
  değişince ötekinin de değişmek zorunda olduğu iki kod parçası arasındaki ilişki.)
- Statik (kod okunarak görülebilen) ve dinamik (yalnızca program çalışırken ortaya çıkan)
  connascence arasındaki fark nedir?
- En güçlü connascence biçimi hangisidir? En zayıfı hangisidir?
- Kod tabanında hangisi tercih edilir: statik mi dinamik connascence mı? Neden?

## Bölüm 4 — Mimari Karakteristikler (Hafta 5)
- Bir özniteliğin mimari karakteristik sayılması için karşılaması gereken üç ölçüt nedir?
- Örtük (kimsenin istemesine gerek olmayan, varsayılan beklenen) ve açık (istekte adı geçen)
  karakteristik arasındaki fark nedir? Birer örnek ver.
- Operasyonel (çalışma zamanına dair), yapısal (koda dair) ve kesişen (her ikisine de
  değmeyen ama her yeri ilgilendiren) karakteristiklere birer örnek ver.
- Sektör çapında standart bir karakteristik listesi oluşturmak neden imkânsızdır?

## Bölüm 5 — Karakteristikleri Belirlemek (Hafta 5)
- Mimarinin desteklediği karakteristik ("-ility") sayısını sınırlamak neden iyi pratiktir?
- Doğru mu yanlış mı: *Karakteristiklerin çoğu iş gereksinimlerinden ve kullanıcı
  hikâyelerinden (user story) gelir.* Cevabını **gerekçelendir** — gelmiyorsa nereden gelir?
- Paydaş "pazara çıkış süresi en önemli iş kaygımız" derse mimari hangi karakteristikleri
  desteklemelidir?
- Ölçeklenebilirlik (daha çok yükü kaldırabilme) ile esneklik (elasticity — yükün ani
  inişine/çıkışına hızla uyum sağlama) arasındaki fark nedir?
- Bileşik (composite) mimari karakteristik nedir? Bir örnek ver.

## Bölüm 6 — Ölçüm ve Yönetişim (Hafta 4, 5)
- Cyclomatic complexity (çevrimsel karmaşıklık — bir kod parçasındaki bağımsız karar yolu
  sayısı) mimari analizde neden bu kadar önemli bir metriktir?
- Mimari fitness fonksiyonları (uygunluk fonksiyonu — bir mimari kuralın bozulup bozulmadığını
  otomatik ölçen test) nedir? Mimariyi analiz etmek için nasıl kullanılır?
- Ölçeklenebilirliği ölçen bir fitness fonksiyonu örneği ver.
- Fitness fonksiyonu yazılabilmesi için karakteristiğin taşıması gereken en önemli özellik
  nedir?

## Bölüm 7 — Karakteristiklerin Kapsamı (Hafta 5, 10)
- Mimari quantum nedir ve mimari için neden önemlidir?
- Tek bir kullanıcı arayüzü (UI) ve her biri kendi veritabanına sahip dört bağımsız servis
  düşün: bu sistem tek quantum mu, dört quantum mu? Neden?
- Statik bağlaşım (parçanın çalışabilmek için neye muhtaç olduğu) ile dinamik bağlaşım
  (parçaların çalışma anında birbirine nasıl seslendiği) arasındaki fark nedir? Birer örnek
  ver.
- Senkron iletişim (çağıran taraf yanıtı bekler), operasyonel karakteristikleri neden
  asenkrondan (beklemeden) daha çok etkiler?

## Bölüm 8 — Bileşen Tabanlı Düşünme (Hafta 5, 6)
- Bileşenler (component) uygulama içinde tipik olarak nasıl vücut bulur?
- Teknik bölümleme (katmana göre bölme) ile domain bölümleme (işe göre bölme) arasındaki
  fark nedir? Birer örnek ver.
- Domain bölümlemenin avantajı nedir? Teknik bölümleme ne zaman daha iyi seçimdir?
- Entity Trap (Varlık Tuzağı — bileşenleri veritabanı tablolarının birebir kopyası olarak
  çıkarma alışkanlığı) nedir? Bileşen belirlemede neden kötü bir yaklaşımdır?
- Bileşen bulmak için Workflow (iş akışı) yaklaşımını, Actor/Action (Aktör/Eylem)
  yaklaşımının yerine ne zaman seçersin?

## Bölüm 9 — Stillerin Temelleri (Hafta 6, 10)
- Dağıtık hesaplamanın sekiz yanılgısını (fallacies — "ağ güvenilirdir", "gecikme sıfırdır"
  gibi yanlış varsayımlar) say.
- Dağıtık mimarilerin monolitlerde olmayan üç zorluğunu adlandır.
- Stamp coupling (damga bağlaşımı — servisler arasında ihtiyaçtan fazlasını taşıyan şişkin
  veri sözleşmesi) nedir? Nasıl giderilir?
- *(Bölüm 8'de de soruldu — kasıtlı tekrar, geri getirme pratiği:)* Teknik ve domain
  bölümleme arasındaki fark nedir? Bu kez cevabını **stil seçimi** bağlamında ver.
- Mimari **stili**, mimari **desenden** ayıran üç özelliği say.

## Bölüm 10 — Katmanlı Mimari (Hafta 6, 9)
- Açık katman (atlanabilir) ile kapalı katman (atlanamaz) arasındaki fark nedir?
- Yalıtım katmanları (layers of isolation) kavramını ve faydalarını açıkla.
- Architecture Sinkhole (Mimari Çukuru) anti-deseni nedir?
- Hangi karakteristikler seni katmanlı mimariye yönlendirir?
- Katmanlı mimaride test edilebilirlik ve çeviklik neden zayıftır?

## Bölüm 11 — Modüler Monolit (Hafta 6, 9)
- Modüler monolit, n-katmanlı mimariden nasıl ayrılır?
- Modüller arası iletişimde peer-to-peer (modüller doğrudan birbirine seslenir) ile mediator
  (arada bir aracı modül vardır) yaklaşımlarının farkı nedir?
- Bu stille ilişkili üç yaygın risk nedir?
- Üç temel gücünü ve ne zaman kullanılması gerektiğini söyle.
- Modüler olmasına rağmen ölçeklenebilirlik ve hata toleransı neden zayıftır?

## Bölüm 12 — Pipeline Mimarisi (Hafta 6, 9)
- Bu stilde veri, **boru** (pipe) denen kanallardan geçerek **filtre** denen işleme
  adımlarına ulaşır. Borular çift yönlü olabilir mi? Neden?
- Dört filtre türünü ve amaçlarını say.
- Bir filtre birden çok boruya veri gönderebilir mi? Cevabını gerekçelendir.
- Pipeline mimarisi buluta neden uygundur?
- Teknik mi domain bölümlemeli midir? Modülerliği nasıl destekler?

## Bölüm 13 — Mikrokernel (Hafta 6, 9)
- Mikrokernel (mikro-çekirdek: sabit bir çekirdek + takılıp çıkarılabilen eklentiler)
  mimarisinin diğer adı nedir?
- Plug-in'lerin (eklentilerin) birbirine bağımlı olması hangi durumlarda kabul edilebilir?
- Plug-in yönetiminde kullanılan araç/çatılar (framework) nelerdir?
- Standart sözleşmeye (contract) uymayan üçüncü taraf bir plug-in ile ne yaparsın?
- İki mikrokernel örneği ver. Bir sistemin "ne kadar mikrokernel sayıldığını" ne belirler?
- Mikrokernel neden her zaman tek quantum'dur?
- Domain/mimari izomorfizmi (işin doğal şeklinin mimarinin şekline benzemesi) nedir?

## Bölüm 14 — Servis Tabanlı Mimari (Hafta 6, 10)
- Bu stilin servisleri neden "domain servisleri" olarak adlandırılır?
- İki yaygın riskini adlandır.
- Hangi veritabanı topolojileri (verinin servisler arasında nasıl paylaştırıldığı düzenleri)
  kullanılabilir? Veritabanı şeması değişiklikleri nasıl yönetilir?
- Domain servisleri konteyner (container) gerektirir mi? Cevabını gerekçelendir.
- Hangi karakteristikleri iyi destekler? Esneklik (elasticity) neden zayıftır?
- Bu stilde quantum sayısı nasıl artırılır?

## Bölüm 15 — Event-Driven Mimari (Hafta 6, 10)
- Olay (event — "şu oldu" bildirimi) ile mesaj (message — belirli bir alıcıya gönderilen
  istek) arasındaki dört farkı say.
- Başlatıcı olay (initiating event) ile türetilmiş olay (derived event) arasındaki fark nedir?
- Poison event (zehirli olay — işlenmeye her çalışıldığında hata verip kuyruğu tıkayan mesaj)
  nedir?
- Bir işlemci birden çok türetilmiş olay tetikleyebilir mi? Bunu neden istersin?
- Şu an kimsenin dinlemediği bir olayı neden yine de yayınlarsın?
- Asenkron işlemenin olumsuz ödünleşimleri nelerdir?
- Swarm of Gnats (Sivrisinek Sürüsü) anti-desenini açıkla — neden kaçınmalısın?
- Olay güdümlü mimarinin yüksek tepkiselliğinin (responsiveness) iki ana sebebi nedir?
- Kuyruktan mesaj alışverişinde veri kaybını önleme teknikleri nelerdir?

## Bölüm 16 — Space-Based Mimari (Hafta 10, 11)
- Bu stil (Türkçe kabaca *alan-temelli*: veritabanını sıcak yoldan çıkarıp veriyi bellekte
  tutan yaklaşım) adını nereden alır? Onu diğer stillerden ayıran temel özellik nedir?
- Sanallaştırılmış ara katmanı (virtualized middleware) oluşturan dört bileşeni say.
- Data writer'ın (veri yazıcı) rolü nedir? Data reader'a (veri okuyucu) hangi koşullarda
  ihtiyaç duyulur?
- Küçük cache boyutu veri çarpışması olasılığını artırır mı, azaltır mı? Neden?
- Replike cache (her düğümde verinin tam kopyası) ile dağıtık cache (veri düğümlere
  bölünmüş) farkı nedir? Bu stilde tipik olarak hangisi kullanılır?
- En güçlü üç karakteristiğini say. Test edilebilirlik neden bu kadar düşüktür?

## Bölüm 17 — Orkestrasyon Güdümlü SOA (Hafta 6, 10)
- SOA'nın (Service-Oriented Architecture — hizmet odaklı mimari) arkasındaki ana itici güç
  neydi?
- Dört temel servis türü nedir?
- Bu stilin çöküşüne yol açan etkenler nelerdi?
- Teknik mi domain bölümlemeli midir?
- Domain (iş) ve operasyonel (teknik) yeniden kullanım bu stilde nasıl ele alınır?
- Modern sistemlerdeki başlıca kullanımı nedir?

## Bölüm 18 — Mikroservisler (Hafta 6, 9, 10)
- Bounded context (sınırlı bağlam — bir iş teriminin geçerli olduğu çit) kavramını açıkla:
  mikroservis için neden kritiktir?
- Veri yalıtımı (her servisin kendi verisine sahip olması) neden bu kadar önemlidir?
- "Protocol-aware heterogeneous interoperability" ne demektir? (Kabaca: servisler farklı
  dillerde yazılabilir ama hangi protokolle konuşulacağı ortaktır.)
- Orkestrasyon (merkezî bir yönetici adımları sırayla yürütür) ile koreografi (servisler
  merkezî yönetici olmadan olaylarla birbirini tetikler) farkı nedir? Her birinin uygun
  olduğu birer örnek ver.
- Neden database-per-service (servis başına ayrı veritabanı) kullanılır? Bunun yerine
  monolitik ya da domain düzeyinde ortak bir veritabanı kullanılabilir mi?
- En büyük iki riski nedir?
- Çeviklik, test edilebilirlik ve dağıtılabilirlik neden bu kadar iyidir?
- Performansın sorun olmasının üç sebebi nedir?
- Bir mikroservis ekosisteminin TEK quantum'a düştüğü bir topoloji tarif et.

## Bölüm 19 — Stil Seçimi (Hafta 5, 6, 10)
- Veri mimarisi stil seçimini nasıl etkiler?
- Mimarın stil + veri bölümleme + iletişim tarzını belirleme adımlarını sırala.
- Hangi etken mimarı dağıtık mimariye yönlendirir?
- Stil seçmek için hangi iki girdinin analizi gerekir?

## Bölüm 20 — Mimari Desenler (Hafta 6, 9)
- Operasyonel (teknik) ve domain (iş) kaygılarının ayrılması ilkesini uygulayan iki deseni
  adlandır.
- Orkestre edilmiş iş akışında olup koreografide olmayan bileşen nedir?
- Tek broker (tüm mesajların geçtiği tek merkezî aracı) kullanmanın avantajları ve
  dezavantajları nelerdir?
- CQRS (Command Query Responsibility Segregation) hangi iki veri işlemini birbirinden ayırır?

## Bölüm 21 — Mimari Kararlar / ADR (Hafta 2)
- Covering Your Assets (kabaca *kendini sağlama alma*: karar vermek yerine sorumluluğu
  dağıtacak biçimde yazma) anti-deseni nedir?
- Email-Driven Architecture'dan (mimari kararların e-postalarda kaybolması) kaçınma
  teknikleri nelerdir?
- Michael Nygard'ın "mimari açıdan önemli" saydığı beş faktör nedir?
- ADR'nin (Architecture Decision Record — mimari karar kaydı) beş temel bölümü nedir?
  Kararın gerekçesi hangi bölüme yazılır?
- Ayrı bir "Alternatives" (alternatifler) bölümü yoksa alternatifleri nereye listelersin?
- ADR durumunu "Proposed" (önerildi) işaretleyeceğin üç temel senaryo nedir?

## Bölüm 22 — Risk Analizi (Hafta 5, 10)
- Risk matrisinin iki boyutu nedir?
- Risk fırtınasının (risk storming — ekibin bir mimariyi birlikte risk açısından taradığı
  oturum) üç ana etkinliğini tarif et.
- Risk fırtınası neden kolaboratif (birlikte) yapılmalıdır? Buna karşılık **tanımlama fazı**
  neden BİREYSEL yapılmalıdır?
- Risk fırtınasında aynı riske üç katılımcı 6, bir katılımcı 3 puan verirse ne yaparsın?
- Henüz kanıtlanmamış ya da ekibin bilmediği bir teknolojiye hangi risk puanını verirsin?

## Bölüm 23 — Diyagramlama (Hafta 2, 5)
- Irrational artifact attachment (kendi ürettiğin diyagram/belgeye mantıksız bağlanma, onu
  atamama) nedir, neden önemlidir?
- C4 modelinin dört C'si nedir?
- Bileşenler arasındaki kesikli çizgi ne anlama gelir?
- Başlık ve gösterge/lejant (key) neden her diyagramda olmalı?

## Bölüm 24 — Ekipleri Etkili Kılmak (Hafta 12)
- Üç mimar kişiliği (kontrolcü · gölge · etkili mimar) nedir? Her biri ekibe nasıl bir sınır
  üretir?
- Mimarın ekiple katılım düzeyini belirleyen beş faktör nedir?
- Bir ekibin fazla büyüdüğünün üç alarm işareti nedir?
- Geliştirme ekibi için faydalı üç temel kontrol listesi say.

## Bölüm 25 — Müzakere ve Liderlik (Hafta 12)
- Müzakere mimar için neden bu kadar önemli bir beceridir?
- Paydaş "beş dokuz" (%99,999 — yılda ~5 dakika kesinti) istiyor ama işe "üç dokuz" (%99,9 —
  yılda ~9 saat kesinti) yetiyorsa hangi müzakere tekniklerini kullanırsın?
- Bir paydaşın "bu dün lazımdı" cümlesinden ne çıkarırsın?
- "Gösterim tartışmayı yener" (demonstration defeats discussion) tekniği neden bu kadar
  etkilidir?
- Böl-ve-yönet (divide and conquer) kuralı nedir? Karakteristik müzakeresinde nasıl
  uygulanır? Örnek ver.
- Mimarinin 4 C'sini (communication · collaboration · clarity · conciseness) say.
- Mimar neden hem pragmatik hem vizyoner olmalıdır?
- Davet edildiğin toplantı sayısını azaltma teknikleri nelerdir?

## Bölüm 26 — Mimarinin Kesişimleri (Hafta 5, 8, 10)
- İmplementasyonun mimariyle **yapısal hizasını** (yazılan kodun çizilen mimariye uyması)
  hangi tekniklerle sağlarsın?
- Mimari-altyapı hizası neden önemlidir? Bir örnek ver.
- Ekip topolojisinin (ekiplerin nasıl bölündüğünün) mimariyle hizasız olduğu bir örnek ver.
- Sistem entegrasyonu kesişimi neden önemlidir? Bir hizasızlık örneği ver.
- *(Bölüm 13'te de soruldu — kasıtlı tekrar:)* Domain/mimari izomorfizmi nedir? Bu kez
  **iş kesişimi** açısından neden önemli olduğunu açıkla.
- Mimari-kurum (organizasyon) kesişimine bir örnek ver.

## Bölüm 27 — Yasalar, Yeniden (Hafta 5, 10)
- Paylaşılan kütüphanenin (shared library) paylaşılan servise göre üç genel avantajını say.
  Ardından tersini de değerlendir: paylaşılan servisin kütüphaneye göre hangi avantajları
  vardır, ve mimar bu ikisi arasında neye bakarak seçim yapar?
- Birinci Yasa'nın birinci doğal sonucu (corollary) nedir?
- İkinci doğal sonuç, ödünleşim analizinin neden tekrar tekrar yapılmasında ısrar eder?
- Mimari kararların çoğu neden ikili (evet/hayır) değil, bir spektrum olarak anlaşılır?
