---
baslik: "Domain Storytelling — İş Süreçlerini Konuşarak Modellemek"
yazar: "Stefan Hofer, Henning Schwentner (Addison-Wesley) — Türkçe çalışma metni"
tarih: "2026-07-14"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 3,6,9
---

### Bu metin ne anlatıyor?
Yazılım projelerinin çoğu teknik sebeplerle değil, **yanlış anlaşılma** yüzünden batar: işi bilen
kişi bir şey anlatır, kodu yazan kişi başka bir şey anlar. Bu metin, o yanlış anlaşılmayı daha ilk
gün yakalayan bir yöntemi anlatıyor. Yöntem çok basit: işi bilen kişi **tek bir somut olayı**
baştan sona anlatırken, dinleyen kişi onu aynı anda çöp adamlar ve oklarla **çizer** — sonra çizdiği
hikâyeyi geri anlatır. Uyuşmayan yer o anda ortaya çıkar. Metnin ikinci yarısı bu resimden nasıl
gereksinim, sistem sınırı ve sonunda kod çıkardığını gösteriyor.

> **Baştan tek terim: "domain".** Bu metnin en sık geçen kelimesi. **Domain (iş alanı)**, yazılımın
> hizmet ettiği işin kendi dünyasıdır: o işin nesneleri, kuralları, rolleri ve kelimeleri. Bir
> sinemanın domain'inde "seans", "koltuk", "bilet" vardır; bir leasing şirketininkinde "sözleşme",
> "taksit", "risk". Yazılım **domain hakkında** yazılır, ama domain yazılımdan **önce** ve ondan
> **bağımsız** olarak vardır. **Alt-domain** ise bu dünyanın kendi içinde ayrılan bölgeleridir
> (satış · risk · faturalama). Metinde "domain" kelimesi Türkçeleştirilmeden bırakıldı, çünkü
> alanın literatüründe ve iş hayatında bu adla dolaşıyor.

## Neden bu özet
Domain Storytelling, iş uzmanlarıyla geliştiricileri aynı odaya oturtup **bir iş sürecinin tek bir
somut hikâyesini** konuşarak ve aynı anda çizerek modelleyen bir **kolektif modelleme tekniğidir**
(collaborative modeling — birden çok kişinin aynı anda, birlikte model kurması). Kitabın tezi tek cümlede şudur: yazılımı batıran şey nadiren tekniktir;
asıl vebayı **kötü iletişim** yayar; **panzehir de bu tekniğin kendisidir** — çünkü domain bilgisini
etkili iş yazılımına dönüştürür.

Kitap üç haftaya birden bağlanıyor:

- **Hafta 3 (gereksinim / spec-driven):** Bölüm 11, domain story cümlelerinden nasıl kullanıcı
  hikâyesi (user story) ve backlog omurgası çıkarıldığını adım adım verir. Gereksinim "toplanmaz",
  konuşmadan **inşa edilir**.
- **Hafta 6 (mimari stiller, sınırlar, modülerlik):** Bölüm 10, tek bir COARSE-GRAINED (kaba taneli,
  yani geniş açıdan bakan) hikâyeden alt-domain'leri, oradan **sınırlı bağlamları** (bounded
  context — "bu sınırın içinde her terimin tek ve net bir anlamı vardır" denen model bölgesi;
  ayrıntısı §9'da), oradan da **Conway yasasıyla** takım sınırlarını türetir. Monolit bölmenin ve mikroservis sınırı çizmenin "domain tarafı" burasıdır.
- **Hafta 9 (hexagonal / clean):** Bölüm 12, "iş nesneleri sınıfa, faaliyetler metoda dönüşür"
  altın kuralını verir — ve kitabın kendi dipnotu, katmanlı mimarinin neden yetmediğini, domain'in
  neden bir **katman değil bir halka** olduğunu söyler. Hafta 9'un tam da kalbi.

Kitabın lakabı **"Ayçiçeği Kitabı"dır**: ayçiçeği güneşsiz yaşayamaz, güneşe benzer, gün boyu başını
ona çevirir ve tarlada asla tek başına bitmez — domain story ile domain'in ilişkisi de böyledir.

Kitap 2021'in sonunda yayımlandı (kapaktaki telif yılı 2022) ve ikinci baskısı gelmedi; ama
2025–2026'da **kitapta olmayan** bir şey oldu:
domain story, **dil modelinin bağlamı** hâline geldi ve sınırlı bağlam, **AI ajanının sınırı** olarak
kullanılmaya başlandı. Bu kaynağın son bölümü (**§15 — 2026 notu**) bunu ayrı ve açıkça işaretlenmiş
biçimde anlatır; öncesindeki her şey kitabın kendisidir.

## Türkçe çalışma metni

### 1. Domain Storytelling nedir — notasyon değil, bir konuşma tekniği
Kitabın kendi tanımı: *"Domain Storytelling, insanların birlikte nasıl çalıştığını öne çıkaran bir
kolektif modelleme tekniğidir. Birincil amacı: **domain bilgisini iş yazılımına dönüştürmek**."*

Bunun neden bir "teknik" değil de "konuşma" olduğunu Alberto Brandolini'nin alıntısı özetler:

> **"Üretime çıkan şey, uzmanın bilgisi değil, GELİŞTİRİCİNİN (yanlış) anlayışıdır."**

Yani kafandaki model üretime çıkar; uzmanın kafasındaki değil. O yüzden yöntem, uzmandan **soyut
kurallar** değil **somut hikâyeler** ister: "eğer"siz, "olabilir"siz, siperden gerçek örnekler.

> **"Kötü bir soyutlamadansa iyi bir örnekten daha çok şey öğrenirsin."**

**Metropolis örneği (kitabın açılışı).** Matthew küçük bir sanat sineması işletiyor; okul arkadaşı
Anna yazılım geliştiriyor. Matthew "online bilet uygulaması" istiyor. Anna beyaz tahtaya geçiyor ve
"bugün gişede bileti nasıl satıyorsun?" diye soruyor. Matthew anlattıkça Anna çiziyor: bir çöp adam
(**gişe görevlisi**), ikinci bir çöp adam (**sinemasever**), bir konuşma balonu (**seans**), bir
ızgara (**koltuk planı**)… Sonunda Anna hikâyeyi **baştan geri anlatıyor** (retelling) — ve asıl
kazanç orada çıkıyor:

- Anna, "yabancı dildeki filmler için ayrı bilet satılıyor" sanıyordu.
- Matthew: *"Hayır hayır! Ekstra bilet satmıyoruz; sadece sinemaseverlere filmin hangi dilde
  gösterileceğini söylüyoruz."*
- Yani Anna'nın bir **süreç** sandığı şey, aslında 4. cümleye düşülecek bir **nottu**.

Bu yanlış varsayım, hikâye anlatılmasa fark edilmeyecek, koda gömülecek ve haftalar sonra "yanlış
yazılım" olarak geri dönecekti. Kitabın çıkarımı bu yüzden şudur:

> **Modelleme eylemi, çoğu zaman modelin kendisinden daha önemlidir.**
> Bitmiş bir domain story'ye bakmak, onun yapılışına katılmakla aynı şey değildir — çünkü Domain
> Storytelling **kolektif bir öğrenme deneyimidir.**

Bu yüzden ortaya çıkan resim **bağımsız (stand-alone) bir belge olarak tasarlanmamıştır**: her şeyden
önce **hikâyeyi anlatanlar için**, sonra da katılanlar için bir **hafıza yardımcısıdır** (ve hikâyeyi
başkalarına anlatmaya yarar). Resmi tek başına birine yollayıp "işte analiz" demek, yöntemin
kendisini ıskalamaktır.

### 2. Piktografik dil — altı yapı taşı
Yöntem = **piktografik dil + çalıştay biçimi**. Dilin yapı taşları (bkz. **Diyagram 1**):

- **Aktör (actor):** Hikâyede **aktif rol** oynayan kişi, insan grubu ya da **yazılım sistemi**.
  Cümlenin **öznesidir**. Kişi adıyla değil **rolle** etiketlenir ("gişe görevlisi", "Matthew" değil).
  > **İstisna (kitabın kendi kaçamağı):** Katılımcılar hiçbir konuda anlaşamıyorsa aktörü **insan
  > adıyla** somutlaştır. Metropolis'te bileti tamamen farklı biçimlerde satan iki kasiyer varsa
  > (Charlotte ve Carlos), ikisini birden "kasiyer" diye modelleme: önce **Charlotte** ile başla,
  > sonra Carlos'a sürecinin nerede ayrıldığını sor. **Farkı görünür kılmak, onu çözmenin ilk
  > adımıdır.**
- **İş nesnesi (work object):** Aktörlerin yarattığı, işlediği, devrettiği şey. Dil, **nesnenin
  kendisiyle, dijital temsiliyle ve o bilginin taşındığı ortamla arasında ayrım yapmaz**: basılı
  bilet de, e-posta eki de, "bileti sormak için yapılan telefon görüşmesi" de birer iş nesnesidir.
- **Faaliyet (activity):** Ok + **domain dilinden bir fiil** ("satar", "önerir", "oylar").
  **Kural:** aktörler ve iş nesneleri **isimdir**, faaliyetler **fiildir**.
- **Sıra numarası (sequence number):** Okun çıkış noktasına yazılır.
  **Sıra numaraları, domain story içinde ZAMANI ifade eder.** (Eşzamanlı faaliyetlere aynı numara
  verilebilir ama bunu az kullan: hikâyeyi zayıflatır.)
- **Not (annotation):** Varyasyonlar, bir faaliyetin **amacı**, terim tanımları, varsayımlar. Bir
  öğeye, bir cümleye ya da **hikâyenin bütününe** iliştirilebilir.
- **Grup (group):** Birbirine ait parçaları çevreleyen etiketli çerçeve — tekrarlanan/isteğe bağlı
  faaliyetler, farklı konumlar, **organizasyonel sınırlar** ve en önemlisi **alt-domain'ler**.

**Altın kural:** **Aktörler hikâye başına bir kez görünür; iş nesneleri birden çok kez görünebilir.**
Her cümle **kendi** iş nesnesini alır — çünkü aynı nesne hikâye boyunca **temsil değiştirebilir**
("boş koltuklar" aranırken koltuk ikonuyla, sinemasevere önerilirken konuşma balonuyla çizilir).

**Ve yöntemin en ayırt edici kuralı: KOŞUL YOK (no conditionals).**

> Piktografik dilde koşul, alternatif ya da varyasyon için **sembol yoktur — bu kasıtlıdır.**
> Gerekçe: *"Bir notasyon alternatifleri modellemek için koşul sunuyorsa, insanlar büyük olasılıkla
> onu kullanır."* (**Notasyon** = bir şeyi çizerken kullanılan semboller ve kuralları bütünü.)
> **BPMN**'in (Business Process Model and Notation — iş süreçlerini çizmek için kullanılan yaygın,
> resmî bir standart) **gateway**'leri (karar/dallanma sembolleri) tam da bunu yapar: bütün
> vakaları tek diyagrama tıkar.
>
> *Adil not — kitap bunu kendi söylüyor:* BPMN gateway kullanmaya **zorlamaz.** Ayrıntı §7'de.

Bunun yerine yalnızca **en önemli alternatifler** modellenir — **her biri ayrı, baştan sona anlatılan
bir domain story olarak.**

### 3. Gramer ve iyi dil üslubu
Temel sözdizimi: **özne (aktör) – yüklem (faaliyet) – nesne (iş nesnesi)**.
Kitabın saydığı cümle biçimleri: (1) aktör bir iş nesnesi üzerinde çalışır; (2) bir nesne üzerinde
çalışarak başka bir nesneyi düzenler; (3) nesneyi başka bir aktöre **devreder** ya da bilgiyi onunla
**paylaşır** (ikinci ok genelde bir **edat** ile etiketlenir); (4) aynısı ama birden çok alıcıya;
(5) iki aktör bir nesne üzerinde **işbirliği** yapar (imzalarlar, anlaşırlar).

> **Her cümle, yüksek sesle okunduğunda anlamlı olmalı.**

Eric Evans buna **sesli modelleme** (modeling out loud) diyor: *"Bir modeli iyileştirmenin en iyi
yollarından biri konuşarak keşfetmektir… **Pürüzleri duymak kolaydır.**"* Grameri doğru kullanmaktan
önemlisi **ifade gücüdür**.

**Altı üslup kuralı:**
1. **Her cümleye kendi iş nesnelerini ver** (nesneleri yeniden kullanma) — yoksa oklar birikir,
   gruplar üst üste biner.
2. **İş nesnelerini açık yap**, fiilin içine gizleme. "…içinde **boş koltuk** arar" yanlış; koltuk
   sinema domain'inin ana kavramıdır, ayrı bir iş nesnesi olmalıdır. (Part II'deki her şey — sınır
   bulmak, koda dökmek — **kavramların dile doğru eşlenmesine** dayanır.)
3. **Her yapı taşını etiketle.** Sana bariz olan başkasına bariz değildir; bugün bariz olan bir ay
   sonra kafa karıştırır. Kısalık yerine **okunabilirlik**.
4. **Aktörler ve iş nesneleri için farklı ikonlar kullan.**
5. **"Loopback"ten kaçın:** oklar **fonksiyon/metot çağrısı değildir**. Fiyat hesaplayan sistem
   basitçe bir faaliyettir; kendine dönen ok çizme.
6. **"İstek–yanıt" (request & response) deseninden kaçın.** İkna edici hikâye anlatmaz, teknik
   gerçeklemeye özgüdür ve gerçekleme değişince **bayatlar**. Bunun yerine **niyeti** modelle:
   kullanıcının hedefi ne, yazılım ona nasıl yardım ediyor? **Her faaliyet hikâyeyi ileri itmeli.**

### 4. Senaryo temelli modelleme — "bir diyagram, bir hikâye"
Cyrille Martraire'in deyişiyle: **"Bir diyagram, bir hikâye."** Bu, BPMN'in tam tersidir: BPMN tüm
vakaları tek diyagramda toplar, Domain Storytelling **tek yürütme yolunu** gösterir (tıpkı UML sekans
diyagramı gibi).

**Senaryo nedir?** (John Carroll): insanlar ve faaliyetleri hakkında, **sabit bir bağlamda** geçen,
**hedefleri olan aktörler** içeren, bir **olay örgüsü** olan hikâye.

> **Bir senaryo, bir iş sürecinin BİR ÖRNEĞİDİR (one instance).**

**Avrupa yolculuğu benzetmesi (Jörn Koch):** Avrupa keşfetmek istediğin **domain**'dir. Lizbon'dan
Helsinki'ye tek bir rota izlersen birçok ülkeden (**alt-domain**) ve sınırdan geçersin — ama tüm
Alpler'i, tüm Akdeniz'i **tümüyle kaçırırsın.** Avrupa'nın sınırlarını kavramak için **birkaç
kapsamlı yolculuk** — yani **birkaç senaryo** — gerekir.

**Nereden başlanır?** Önce **varsayılan vaka**, yani "%80 vakası" ve **mutlu yol** (happy path):
istisna yok, hata yok, güneş parlıyor. Metropolis'te Anna bunu Matthew'a şöyle daralttı: tek seans
bileti satışı gelirin %75'i → hikâye o vaka üzerine kuruldu. **Sonra** "ne ters gidebilir?" diye
sorulur ve önemli varyasyonlar **ayrı hikâyeler** olarak modellenir (Metropolis 3: "seans dolu" —
mutlu yolun kopyası olarak başlar, 3. cümleden sonra hikâye hattı ayrılır). Küçük varyasyonlar ise
ayrı hikâyeyi hak etmez; onlar için **not** kullanılır.

**Uyarı — erken soyutlama:**
> **Kötü soyutlamalar domain'i gizler; iyi soyutlamalar domain diline kesinlik katar.**
> Önce tipik vakaları sağlam anla — **hikâye anlat.** Ancak ondan sonra başka ne olabileceğini
> tartış — **kural topla.**

**Mutlu yol yoksa?** Yazarlar bir liman acil müdahale ekibine naif biçimde "tipik vakanız nedir?"
diye sordu. Cevap: *"Biz **yalnızca** beklenmedik olanla ve kuralların istisnalarıyla uğraşırız."*
Bunun üzerine **artan karmaşıklıkta üç gerçek vaka** istediler: sürüklenen boş bir konteyner →
fırtınada elektrik kesintisi ve mahsur kalan tren → karaya oturmuş, patlama riski taşıyan yük gemisi.

Ve genel görünümü korumanın kuralı:

> **Tamlık değil, TEMSİLİ BİR ÖRNEKLEM hedefle!**

### 5. Kapsam = Granülarite × Domain Saflığı × Zaman Noktası
Kitabın kendi formülü (bir **çarpım kümesi**):

> **Kapsam (scope) = Granülarite × Domain Saflığı × Zaman Noktası**

**(a) Granülarite (granularity): COARSE-GRAINED (kaba taneli) ↔ FINE-GRAINED (ince taneli)**, arada
**MEDIUM-GRAINED (orta taneli)**. Spektrum süreklidir. İfade gücünü artırmak için Alistair Cockburn'ün
**okyanus metaforu** (hedef seviyeleri) ödünç alınır:

| Seviye | Ne demek |
|---|---|
| **Bulut (cloud)** | En tepe. Tüm organizasyonu **tek bir aktör** olarak modellemek. |
| **Uçurtma (kite)** | Deniz seviyesi hedeflerinin özeti; uçtan uca iş süreci (Metropolis 1). |
| **Deniz (sea)** | Kullanıcının **tek oturuşta** başarmak istediği şey (Metropolis 2). |
| **Balık (fish)** | Deniz seviyesinin altı; daha ince adımlar. |
| **İstiridye (clam)** | En dip. Cockburn'ün deyişiyle "çok alçak". |

Moderatör ipucu: bu ikonları yapışkan notlara çizip duvara as. Bir süre sonra odada şunu duyarsın:
*"Şu uçurtmayı deniz seviyesine indirelim!"* ya da *"Şimdi balık seviyesine dalmanın sırası değil!"*
**Çok derine indiğinin işareti:** "müşteri butona tıklar" gibi kullanıcı–sistem etkileşimlerini ya
da nesneler arası çağrıları modelliyorsan, muhtemelen fazla ileri gittin.

**(b) Domain saflığı (domain purity): PURE ↔ DIGITALIZED.**
- **PURE:** hikâyede **hiç yazılım sistemi yok.** Domain'i, mevcut yazılımın kattığı **arızi
  karmaşıklıktan** (accidental complexity — işin doğasından değil, onu çözmek için seçtiğimiz
  araçlardan gelen zorluk) arındırılmış olarak görürsün. Soru: *"Tüm faaliyetler
  yalnızca domain tarafından motive edilseydi işler nasıl yapılırdı?"* / *"Bunu bilgisayarsız
  yapsanız nasıl yapardınız?"*
- **DIGITALIZED:** yazılım sistemleri de birer **aktördür**; yazılımın işlevselliği **ve
  eksiklikleri** hikâyenin parçası olur.

> **Birçok organizasyonda domain modeli, onlarca yıllık kötü modellenmiş yazılım sistemlerinin
> altına gömülmüştür.**

**(c) Zaman noktası (point in time): AS-IS ↔ TO-BE.** AS-IS = **problem uzayı** (analiz, anlamak);
TO-BE = **çözüm uzayı** (tasarım, yeni bir şey yaratmak). TO-BE çalıştayları aslında **tasarım
oturumlarıdır** → %50 daha fazla zaman ayır, çok daha fazla tartışmaya hazır ol ve
**belirli bir gelecek noktası** tanımla (birinin 6 ay, diğerinin 24 ay sonrasını düşünmesi büyük
fark yaratır).

**Tipik yolculuk — Alphorn Auto Leasing vakası (Bölüm 8).** Alphorn araba kiralayan bir şirkettir;
patron Becky yeni bir leasing sistemi istiyor. Beş hikâyenin kapsamı, üç eksende gezinir
(bkz. **Diyagram 2**):

| Hedef | Hikâye | Granülarite | Zaman | Saflık |
|---|---|---|---|---|
| Yeni bir domain'i **keşfet** | Alphorn 1 | COARSE | AS-IS | PURE |
| Alt-domain'e **derine in** | Alphorn 2 | FINE | AS-IS | DIGITALIZED |
| Dağınık alt-domain'i **temizle** | Alphorn 3 | FINE | AS-IS | PURE |
| İş sürecini **iyileştir** | Alphorn 4 | FINE | TO-BE | PURE |
| Yeni yazılımı **tanıt** | Alphorn 5 | FINE | TO-BE | DIGITALIZED |

Yani: **keşif → derinleşme → temizleme (jargondan arınma) → tasarım → dijitalleştirme.** Alphorn
2→3 hareketi, "risk yöneticilerinin yazılımdan devraldığı tüm terminolojiden kaçınarak hikâyeyi
tekrar anlatmasıyla" yapıldı. 3→4 hareketinde, saf süreç duvara çıkar çıkmaz Raymond'un patronu
itiraz etti: *"Sözleşmeyi satış temsilcisine imzalatmak gereksiz iş yaratıyor."* — süreç iyileşti.

### 5b. Araçlar — beyaz tahta, yapışkan not ve Egon.io
Basit bir **beyaz tahta + kalem** çoğu zaman yeter. Yazarların en iyi bulduğu analog düzen şudur:

> **Aktör ve iş nesnelerini YAPIŞKAN NOTA çiz; oku, etiketini ve sıra numarasını DOĞRUDAN tahtaya
> yaz.** Bu, yeniden düzenleme kolaylığı ile temiz görünüm arasındaki **en iyi uzlaşmadır.**

Pratik ayrıntı: iş nesneleri için **kare**, aktörler için daha **büyük dikdörtgen** not (aktörler
kolay ayırt edilsin); sıra numarasını okun başına yaz ve etrafına **daire** çiz.

Analog'un dijitale bir üstünlüğü var: **tüm katılımcıların modele "yazma erişimi" olur** — modelleme
gerçekten paylaşımlı ve işbirlikçi hâle gelir.

**UYARI — ikonlar süs değildir.** Beyaz tahtalar (analog ve dijital), insanı **ikonları tümüyle
atlayıp sadece metinli yapışkan not kullanmaya ayartır.**

> İkonlar **anlam taşırlar ve tartışma açarlar.** *"Bu iş nesnesini hangi ikon temsil eder?"*
> tartışması **gizli varsayımları söker.** Metne kaçarsan o tartışmayı da kaybedersin.

**Yazarların bir araçtan üç şartı:**
1. Domain story'nin **sözdizim kurallarını ZORLASIN** — *"Biz çizim değil, **modelleme** aracı
   istiyoruz."*
2. Cümleleri **otomatik numaralasın** (ve sırayı değiştirmeyi kolaylaştırsın).
3. Cümle dizisini **canlandırarak (animasyon)** hikâye anlatmaya yardım etsin.

**Egon.io.** Yazarların WPS'te geliştirdiği **açık kaynak** araç; **kitaptaki tüm şekiller onunla
üretilmiş.** Numaralandırmayı ve modeli yeniden düzenlemeyi destekler. Asıl özelliği **REPLAY (geri
oynatma) modudur**: hikâyeyi **cümle cümle geri anlatır** → bu, Bölüm 1'deki **yeniden anlatma
(retelling)** ilkesinin araç hâlidir.

**Altın kural — alan sınırı:**

> **Bir domain story, ekranın (ya da tahtanın/kâğıdın) boyutunu — ya da yaklaşık **20 cümleyi** —
> geçmemelidir.** Geçiyorsa böl.

Ve ters bir sezgi: flip chart/beyaz tahtadaki **sınırlı alan bir dezavantaj değildir** — hikâyeleri
derli toplu ve öz tutmaya yardım eder. Dijital araçların sunduğu **sınırsız alan + zoom** ise
**disiplinli bir moderatör** gerektirir.

*(§15'te göreceğin gibi Egon.io hâlâ aktif ve 2026'da domain story'yi makine-okunur metin olarak dışa
aktarabiliyor — bu, kaynağın son bölümünün dayanağıdır.)*

### 6. Çalıştay — roller, güvenlik ve sınırlar
> **Domain Storytelling bir konuşma (sohbet) tekniğidir.** En önemli faktör: **doğru insanların
> odada olması.**

**Roller:**
- **Ev sahibi (host):** çalıştayı düzenleyen, katılımcıları davet eden, sonucunda çıkarı olan kişi.
- **Moderatör (moderator):** çalıştaya liderlik eder; anlatıcılarla dinleyicileri buluşturup **ortak
  anlayış** yaratmalarına yardım eder.
- **Modelleyici (modeler):** hikâyeyi çizen kişi. **Başlangıçta bu iki rolü ayırmanız önerilir** —
  ikisi de zahmetli iştir.
- **Anlatıcı (storyteller):** domain uzmanı; siperdeki gerçek uzman, "kulaktan dolma bilen vekil"
  değil.
- **Dinleyici (listener):** öğrenmeye gelen — çoğu kez geliştirme takımı.

**Psikolojik güvenlik.** Bazı organizasyonlarda domain uzmanları, **üstleri odadayken problemler
hakkında açık konuşmaz.** Yazarlar, uzmanların patronlarını düzeltmek ya da onlara "aslında şirket
prosedürlerini uygulamadıklarını" söylemek zorunda kaldığı durumlarla karşılaştı.

> Bu tür etkileşimler ancak **güvenli bir alanda** olur ve **psikolojik güvenlik** gerektirir.
> Şirket açık iletişimi teşvik etmiyorsa bile, **en azından sen bunu çalıştayda sağlayabilirsin.**

**Olay örgüsü boşluğu (plot hole).** Hikâyede **oklarla bağlanmamış parçalar** varsa, orada bir boşluk
vardır: hikâye sinemaseverden yer göstericiye **atlıyorsa**, "yer gösterici bileti nasıl alıyor?
Bilet içeri girmeden önce mi sonra mı kontrol ediliyor?" diye sorulmalıdır. Piktografik dil, **doğru
soruları sormana yardım eder.**

**Acı noktalarını (pain points) görünür kıl:** katılımcılar şikâyet ettiğinde ("bu sürekli ters
gidiyor!"), bunu farklı renkte bir notla işaretle. Kapsam dışı tartışmalar için ise bir **"OTOPARK"
(parking lot)** aç — ertelenen fikirler için görsel bir alan.

**Cümle sınırı (altın kural):**

| Araç | Sınır |
|---|---|
| Flip chart | **10 cümle** |
| Büyük beyaz tahta | **15 cümle** |
| Dijital araçlar | **20 cümle** |

Hikâye taşarsa: daha COARSE bir kapsama geç, olay örgüsünü incelt, ilk adımları **varsayımla** atla,
ya da hikâyeyi **böl** — iyi bölme noktaları: bir ara sonuca ulaşılması, bir iş nesnesinin **devri**
(handover), ya da hikâyenin bir alt-domain'den diğerine geçmesi.
*Unutma: "cliffhanger"lar sevdiğin dizide heyecan vericidir; domain story'de değil.*

**Çelişkiyi saklama:**
> **Çelişen görüşlerden kaçınmak için soyutlamaya atlama.** Çözülemeyen farkları **notlarla**
> herkesin dikkatine sun. **Görünür kılmak, tartışmaya kalite katar.**

### 7. Diğer yöntemlerle ilişki — alet çantasındaki bir alet
- **EventStorming (Brandolini):** Olayları soldan sağa bir **zaman çizgisine** dizer.
  > **Bir event storm NE OLDUĞUNU gösterir; bir domain story KİMİN KİMİNLE NE YAPTIĞINI gösterir.**
  EventStorming'i tercih et: domain çok **yapılandırılmamışsa**, **durum değişimleriyle** karakterize
  ediliyorsa, ya da **yüzlerce olayı** yönetmen gerekiyorsa (notasyonu daha iyi ölçeklenir).
  Domain Storytelling'i tercih et: **çok sayıda aktör** varsa, aktörlerin **nasıl işbirliği yaptığı**
  merak ediliyorsa, **TO-BE tasarımı** yapılacaksa ya da **belgeleme** bir hedefse.
- **Example Mapping (Wynne):** Örtüşme az, çünkü farklı yönlere bakarlar:
  > **Bir domain story, iş sürecinin BİR SOMUT YOLUNU görselleştirir. Example Mapping ise O YOLUN
  > SAĞINDA ve SOLUNDA ne olduğunu keşfetmekle ilgilidir** — sürekli sorar: "Başka ne olabilirdi?",
  > "Bu vakayı diğerlerinden farklı kılan ne?" → İkisi birbirini **çok iyi tamamlar.**
- **User Story Mapping (Patton):** Yatay eksende kullanıcı yolculuğu, dikey eksende detay ve
  önceliklendirme. **Büyük fark: bir story map aktörlerin nasıl etkileştiğini göstermez** — ama
  gereksinimleri planlamakta güçlüdür. **"Düz backlog tuzağı"** (flat backlog trap) buradan gelir:
  yüzlerce gereksinim, faydalı bir ürün oluşturduklarına dair perspektifi öldürür.
- **Use case'ler (Cockburn/Jacobson):** Metinsel senaryo tarifi. Domain story'yi belgelemek için —
  özellikle **ihale** gibi, konuşma yerine metin+resim paylaşılacaksa — hâlâ önerilir.
  > Belgelemeyi düşünmesen bile birkaç use case şablonuna bak: bir Domain Storytelling **konuşması
  > için kontrol listesi** olarak oldukça faydalılar.
- **UML** (Unified Modeling Language — yazılımın yapısını ve akışını çizmek için kullanılan klasik
  standart notasyon; sınıf diyagramı, sekans diyagramı gibi): *"Bizim için UML her şeyden önce
  **geliştiriciler arası** bir iletişim aracıdır. Domain
  Storytelling ise çoğunlukla **geliştirici ile domain uzmanı** arasında bir iletişim aracıdır."*
- **BPMN:** Formal, hatta bir **süreç motorunda çalıştırılabilir**. Ama alternatifleri
  **gateway**'lerle (karar/dallanma sembolleri) tek diyagrama tıkar ve **uzman modelleyicilere**
  yöneliktir. Birleştirme yolu: önce Domain Storytelling ile TO-BE süreçleri tasarla, **mutlu yolu
  formal BPMN'e çevir**, alternatifleri ve hata vakalarını **parça parça** BPMN modeline entegre et.
  > *Adil not — kitap bunu kendi söylüyor:* BPMN gateway kullanmaya **zorlamaz.** Jakob Freund ve
  > Bernd Rücker (*Real-Life BPMN*), modelin ilk sürümünün **tek vakaya** odaklanmasını — yani
  > gateway'leri atlamayı — iyi pratik sayar. Sorun notasyonun kendisi değil, **sunduğu imkânın
  > kullanılmasıdır.** (Kitabın asıl itirazı başka: BPMN uzman modelleyicilere yönelik bir araçtır;
  > geliştirici ↔ domain uzmanı **konuşması** için Domain Storytelling daha iyi bir yoldur.)
- **DDD (Domain-Driven Design — "alan-güdümlü tasarım"; Eric Evans'ın 2003 tarihli kitabıyla
  yerleşen, yazılımı işin kendi kavram ve diline göre kurma yaklaşımı):** Domain Storytelling,
  DDD'nin "kolektif modelleme" boşluğunu doldurur:
  **ubiquitous language** için dil öğrenmek (Böl.9), **stratejik tasarım** için sınır bulmak
  (Böl.10), **taktik tasarım** için kodda modellemek (Böl.12).

### 8. Domain dilini öğrenmek — her yerde geçerli dil
Yabancı dil öğrenir gibi: **dinle, duyduğunu tekrar et, geri bildirime dikkat et.** Kaydederken
duyduğunu tekrar et, tanımadığın terimleri sor. Görsel modelleme, uzmanın anlattığını **hem
duymasını hem görmesini** sağlar → **iki geri bildirim kanalı, birden iyidir.**

**Züllighoven'in aşçı benzetmesi (gözlemin sınırı):**
> Domates sosu karıştıran bir aşçıyı izliyorsun. Saati tutuyorsun: tam **beş dakika** karıştırıyor.
> "Domates sosu 5 dakika karıştırılmalı" sonucunu çıkarıyorsun. Evde deniyorsun ve 5 dakika sonra
> sosun hâlâ **domates çorbası gibi sulu.**
> **Belirleyici faktör zaman değildi:** aşçı sosu **doğru kıvama gelene kadar** karıştırmıştı.
Yani sadece **izleyerek**, konuşmadan domain öğrenilmez.

**"Hesap" homonim hikâyesi (Stefan'ın tarayıcı oyunları pazar yeri projesi).** Domain uzmanları
"hesap (account)" nedir konusunda **anlaşamadı**; hikâye her değiştiğinde biri itiraz etti. Detaylar
sorulunca her müşteri için **iki ayrı hesap** olduğu ortaya çıktı:
- biri **müşteriye** bağlı (faturalama ve ödeme bilgisi) → "müşteri hesabı",
- biri **oyunlarla** ilişkili (müşterinin davranışı, hangi oyunları oynadığı) → "oyun hesabı".

O andan itibaren iki hesap için **iki farklı iş nesnesi ikonu** kullanıldı → uzmanlar hangi hesaptan
bahsettikleri konusunda **açık olmak zorunda kaldı.** Bu ayrım, sonraki çalıştaylara **doğru
insanları** davet etmek için de kritik oldu. Aynı kelimenin farklı bağlamlarda farklı anlama gelmesi
(**eşadlı / homonym**) ve farklı kelimelerin aynı anlama gelmesi (**eşanlamlı / synonym**) doğaldır:

> **Organizasyonlar tek, birleşik, evrensel bir domain dili konuşmaz!**
> Dil farklarını **soyutlama inşa ederek ya da yapay bir dil sentezleyerek** çözmeye çalışma. Bunun
> yerine: COARSE hikâyelerde farkları **notla** işaretle, sonra alt-domain'lere **FINE, PURE, AS-IS**
> hikâyelerle kaz ve o alt-domain'e **özgü, belirsizlik içermeyen** bir dil çıkar. DDD'de böyle bir
> dile **her yerde geçerli dil** (ubiquitous language) denir.

**Sözlük (glossary):** Domain'in **tüm** terimlerini tanımlamak zahmetlidir ve çoğu kimseye yaramaz.
**Sadece açıklamayı hak eden** terimleri al — bunlar zaten hikâyede **tanımlayıcı bir not** taşırlar.
Ve sadece isimler değil, **fiiller** de:

| Terim | Tanım |
|---|---|
| **kredi notu (credit rating)** | Müşterinin kredi riskinin değerlendirmesi; A (mükemmel) – D (zayıf). |
| **oylamak (to vote)** | Bir sözleşmenin riskinin, anlaşmayı kapatacak kadar düşük olup olmadığına karar vermek. |

> Sözlük eskirse büyük sorun değil: **onu yazmak dili öğrenmene yardım ettiyse, zahmete değmiştir.**
> Ama sözlüğü, uzmanlarla **konuşmaktan kaçınmanı sağlayan bir kısayol** sanma —
> **yabancı bir dili sadece sözlük okuyarak öğrenmeyi düşünür müydün?**

**Çevirmen değil moderatör.** Birçok organizasyon iş analistini/gereksinim mühendisini profesyonel
bir **çevirmen** gibi kullanır. Bu sorunludur: **nüans çeviride kolayca kaybolur**, çevirmenler
varsayımlarını gerçek gibi iletebilir — ve daha kötüsü, **geliştiriciler artık domain dilini
öğrenmelerine gerek olmadığına inandırılır.** Öneri:

> **Aracıyı ÇEVİRMEN olarak değil, MODERATÖR olarak kullan.** Moderatörün hedefi: **geliştirme
> takımının domain dilinde akıcı hale gelmesine yardım etmek.**

> **Madalyonun diğer yüzü:** İş analistleri ve gereksinim mühendisleri, domain uzmanlarında çoğu kez
> **eksik** olan becerilere sahiptir — bir yazılımdan ne beklendiğini ifade edebilmek, **doğru
> soyutlama seviyesini** bulmak. Ayrıca birçok geliştirici doğru bilgiyi domain uzmanından **tek
> başına** alamaz. Sorun rolün kendisi değil, o rolün **çevirmen** olarak kullanılmasıdır.

### 9. Sınırları bulmak — Hafta 6'nın çekirdeği
> **Birçok domain, bir bütün olarak anlaşılamayacak ve modellenemeyecek kadar büyüktür.**

Martin Fowler: *"Tek bir kavramsal model, çalışması zor bir canavardır."* Yazarlar, teknik mimarisi
yeterli olmasına rağmen geliştiricilerince **büyük çamur topu** (big ball of mud — hiçbir net yapısı
kalmamış, her şeyin her şeye bağlı olduğu sistem) sayılan, **1 milyon satırın üzerinde** **legacy**
(eski, hâlâ çalışan ama devralınmış ve değiştirmesi zor) sistemler gördü. Kaynaktaki temel yanlış anlama şudur: yazılımın domain'in
**tek, ortak, belirsizlik içermeyen ve gerçek dünyanın tüm özelliklerini içeren** bir modeline
dayanması gerektiği fikri.

> **Saf ve belirsizliksiz tek bir gerçek kaynağı olarak "o tek model" bir İLÜZYONDUR.**
> **Büyük çamur topu model → büyük çamur topu yazılım.**

**Alt-domain bulma heuristiği (sezgisel kural):**
> **Alt-domain'ler, AKTÖRÜN BAKIŞ AÇISINDAN birbirine ait olan faaliyetlerden oluşur.**

**Dört adımlı tarif:**
1. Domain'in **özünü kapsayan anlamlı senaryolar** bul.
2. Onları **COARSE-GRAINED, PURE, AS-IS** (bulut ya da uçurtma seviyesi) domain story olarak modelle.
   **PURE en iyi çalışır**: mevcut sistemlerin organizasyon üzerindeki etkisini kaldırıp
   alt-domain'leri **sıfırdan** düşünmek istersin.
3. Hikâyenin içinde **birbirine ait olan faaliyetleri** bul.
4. Faaliyetleri **gruplara kümele.** **Aktörler birden fazla alt-domain'de rol alabileceğinden onları
   grupların DIŞINA yerleştir.**

Alphorn'da bu tarif tek bir hikâyeden (**Alphorn 1**) **dört alt-domain** çıkardı. Satış temsilcisi
Sandy'nin cümlesi kararı verdi: *"1'den 5'e kadarki cümleler hikâyenin 'teklif' kısmı. Sözleşmeyi
risk yöneticisine geçirdiğim an, iş kelimenin tam anlamıyla elimden çıkıyor."*

**Altı sınır göstergesi** (kanıt değil, ipucu):
1. **Aktör sonucu kendi başına üretir:** birkaç ilişkili faaliyeti bağımsızca tamamlayıp anlamlı bir
   çıktı üretir, sonra devreder.
2. **Tek yönlü bilgi akışı:** A, B'ye bir iş nesnesi verir ve A'ya geri hiç bilgi akmaz.
3. **Farklı tetikleyiciler:** müşterinin sorgusu (**istek-temelli**) ↔ aylık taksit (**zaman-temelli**).
4. **Resimde olmayan bir şeyi destekleyen faaliyetler:** üretilen "risk raporu" bu hikâyedeki hiçbir
   aktör tarafından kullanılmıyorsa, **başka bir alt-domain'de** kullanılıyordur.
5. **Dil farklılıkları:** aynı nesneye farklı ad, ya da aynı ada (hafifçe) farklı anlam. *(İkincisini
   fark etmek çok daha zordur.)*
6. **Aynı şeyin farklı kullanımı:** Alphorn'da sözleşme müşteri tarafından **imzalanır**, risk
   yöneticilerince **oylanır.** İmzalamak **hukuki** bir eylemdir (ilişki yaratır); oylamak
   **finansal** bir eylemdir (riski azaltır).

**Alt-domain → sınırlı bağlam (bounded context).** Alt-domain'ler **problem uzayında** bulunur;
yazılım inşa edeceksen **çözüm uzayına** geçip **sınırlı bağlam tasarlaman** gerekir. Bir sınırlı
bağlam, **belirsizlik içermeyen bir domain modelini bir ubiquitous language ile hayata geçirir.**
Bu bir **tasarım sürecidir**; Nick Tune beş kriter kategorisi sayar: **iş değeri, domain,
sosyopolitik, teknik, kullanıcı deneyimi.** Yani domain'le hizalanma **sadece bir kriterdir**; tek ve
bariz bir çözüm çoğu zaman yoktur, **ödünleşmeler** düşünülmelidir.

**Üç görünüm:**
- **Domain Storytelling → davranışsal (behavioral) görünüm**
- **Context map → yapısal (structural) görünüm**
- **EventStorming → zamansal (temporal) görünüm**

> **Bağlam sınırları modelleri ayırmalıdır, İNSANLARI DEĞİL.**
> Domain Storytelling sadece sınır **bulma** aracı değil; **bağlam sınırları arasındaki işbirliğini
> görselleştirme** aracıdır da.

**Sınırdan takıma — Conway yasası.**
> **Conway yasası:** Yazılımın mimarisi, onu inşa eden **takımların organizasyonuyla aynı yapıya**
> sahip olacaktır.

Bu yüzden takımları **mimari katmana göre** bölmek (bir UI takımı, bir iş mantığı takımı, bir
veritabanı takımı = **yatay kesit**) kötü fikirdir. Modern fikir **çapraz-fonksiyonel takımlar**dır
(**dikey kesit**) — ve sınır **sınırlı bağlamlardan** gelir:

> **Bir sınırlı bağlam yalnızca bir takıma ait olmalıdır.** (Tersi geçerli değil: bir takımın birden
> fazla bağlamı olabilir.) **Kolektif kod sahipliği takım İÇİNDE arzu edilir, takım sınırlarının
> ötesinde değil.**

Ve sınırlar sadece **keşfedilmez**, **tasarlanabilir** de: bir organizasyonun iletişim yapısını
değiştirerek arzu ettiğin mimariyi teşvik edebilirsin — buna **ters Conway manevrası** (inverse
Conway maneuver) denir.

**Henning'in uçak bakım hikâyesi:** 20 kişilik bir grupla COARSE/PURE hikâyeler çizdiler. Uzmanlar,
"bu hikâyelerdeki cümlelerin **kendi başlarına birer iş süreci** sayılabileceğini" söyledi — iş
nesneleri her hikâyede yalnızca **bir kez** görünüyordu; cümleler birbirinin üstüne inşa olan bir
hikâyeden çok **bir kontrol listesinin maddeleri** gibiydi. Bu, kapsamın **bulut seviyesinde** olduğunun
işaretiydi. Kapsam daraltılıp **uçurtma seviyesine** inildiğinde tutarlı faaliyet grupları çıktı ve
bunlar **20 bounded context adayına** dönüştü. Damıtılan **context map**, sistemin **yeni mimarisinin
ve takım organizasyonunun temeli** oldu. (Bkz. **Diyagram 3**.)

> **Sınırları bulmak zordur — aslında bu kitapta yazdığımız muhtemelen en zor meydan okuma.**

### 10. Gereksinimler — Hafta 3'ün çekirdeği
> **Gereksinimlerle çalışmak nesnel, katı analitik bir süreç değildir.** Christiane Floyd:
> *"Gereksinimleri **analiz etmeyiz**; onları **kendi perspektifimizden inşa ederiz.**"*

**Domain story'yi parçalama tarifi (5 adım):**
1. Takım, iş sürecini **MEDIUM–FINE-GRAINED** bir domain story olarak modeller — **en önemli
   senaryoyla (mutlu yol)** başlayarak.
2. Her faaliyet için sor: **"Bu, yeni yazılım tarafından desteklenmeli mi?"**
   > **Altın kural:** BT tarafından desteklenecekse, onun için **uçurtma seviyesinde** bir gereksinim
   > yaz. Bu gereksinimler **backlog**'unun (yapılacak işlerin öncelik sırasıyla tutulduğu liste)
   > **OMURGASI (backbone)** olacak.
3. Alternatif senaryoları ve daha FINE hikâyeleri modelle; istisnaları, hataları, uç vakaları
   **not** olarak topla.
4. Yeni hikâyeler + notlar üzerinden yürü: her cümle için **uçurtma ya da deniz seviyesinde** bir
   gereksinim yaz.
5. Gereksinimleri **omurgaya eşle** → kullanıcı hikâyesi haritası (user story map).

**Şablon eşleşmesi (asıl numara burada):**

> `< kullanıcı tipi > olarak, < bir hedef > istiyorum, çünkü/böylece < bir sebep >.`
> `< AKTÖR >          olarak, < FAALİYET + İŞ NESNESİ > istiyorum, böylece …`

- **aktör → kullanıcı tipi**
- **faaliyet + iş nesnesi → hedefi tarif eder**
- **"sebep" (neden) → bazen notlarda, bazen doğrudan konuşmalarda** bulunur.

Alphorn'da omurga şöyle çıktı: "Müşteri **katalogdan** araba seçer" cümlesi → uçurtma seviyesinde
**"araba seç"** gereksinimi. *"Katalogdan"* kısmı bilinçli olarak dışarıda bırakıldı — çünkü o,
hedefe ulaşmanın **birkaç yolundan birini** tarif ediyor ve zaten **deniz seviyesinde** bir gereksinim
(4. adıma saklandı). 2. ve 3. cümleler tek bir uçurtma gereksiniminde yoğunlaştırıldı:
**"sözleşme teklif et"**.

> **Dikkat: "user story" kulağa "domain story" gibi geliyor ama farklı şeyler. Biraz Java ile
> JavaScript gibi.** Bir user story **"bir konuşma için verilen sözdür"** (a promise for a
> conversation) — yazılı metin bir **yan üründür**, iletişimin yerine geçen bir şey değil.

**Ve en sert uyarı:**

> **DOMAIN UZMANLARININ SANA SÖYLEDİĞİNİ KÖRÜ KÖRÜNE MODELLEME!**
> Uzmanlar (özellikle TO-BE tasarlarken) **yanlıdır**: vizyonları **bildikleriyle sınırlıdır** —
> yani mevcut süreçler ve mevcut yazılım. Sonuç olarak kökten değişmiş bir süreçten çok, mevcuda
> **küçük iyileştirmeler** çıkması daha olasıdır.

**John Ferguson Smart:** *"Kullanıcılar, problemlerini çözeceğini düşündükleri **çözümler** hakkında
memnuniyetle konuşurken, çözmeye çalıştıkları **alttaki problemler** hakkında nadiren konuşurlar…
Domain uzmanlarının ve kullanıcıların **istediği**, aldıkları şey değildir; **aldıkları** şey de
ihtiyaçları olan şey değildir."*

**Eric Evans:** *"Programcılar domain'le ilgilenmiyorlarsa, sadece uygulamanın **ne yapması
gerektiğini** öğrenirler, **ardındaki ilkeleri değil.** Bu şekilde faydalı yazılım inşa edilebilir,
ama proje asla, güçlü yeni özelliklerin eskilerin doğal sonuçları olarak açıldığı bir noktaya
varamaz."*

Çıkarım: kullanıcıyla iletişim **doğru** yazılımı inşa etmek için gereklidir; ama gerçekten **güçlü**
iş yazılımı için geliştiricilerin domain'i **kısmen domain uzmanı hâline gelecek ölçüde** anlaması
gerekir.

*(Stefan'ın ihale hikâyesi bunu doğrular: **300 sayfalık**, **80 use case**'lik mükemmel yazılmış bir
gereksinim belgesiyle işe başladılar — ve yazılıma dokunur dokunmaz fark ettiler ki **use case'ler
domain'i gerçekten anlamak için yeterli değildi.** Bir yan kazanç: takım ayda bir geliştirici
büyüyordu ve **domain story'leri yeniden anlatmak, işe alıştırma sürecinin parçası oldu.*)

### 11. Kodda modelleme — Hafta 9 bağı
> Nihai hedefin yazılımsa, bir noktada **yapışkan notla modellemekten programlama diliyle
> modellemeye** geçmen gerekir. Ve domain'in terimlerini **doğrudan kodunda** kullanmalısın.

**Piktografik dil, DDD'nin taktik yapı taşlarına doğal oturur:**

> **ALTIN KURAL: İş nesneleri SINIFA dönüşür, faaliyetler METODA dönüşür.**

**Moderatörün kodda sorduğu iki soru:**
- **"Bu bir ŞEY (thing) mi, yoksa bir DEĞER (value) mi?" / "Kimliği (identity) var mı?"**
  → **varlık (entity)** ile **değer nesnesi (value object)** ayrımı.
- **"Hangi iş nesneleri birlikte saklanmalı?"** → **küme (aggregate) sınırları** ve **depo**
  (repository).

Alphorn'da: `Contract` (sözleşme) hikâyelerde tekrar tekrar geçiyor ve **sözleşme numarasıyla
kimliği var** → **entity**. `installment` (taksit), `price` (fiyat), `term` (vade), `interest`
(faiz) → **kimlikleri yok** → **value object** (ve `calculateInstallment()`'ın parametreleri).

**Ama kitabın kendi iki itirafı — altın kural MEKANİK DEĞİLDİR:**

> 1. Sınıf diyagramında tasarlanan `fillOut()` metodu koda **girmedi**; Java'da doldurmanın (yani
>    oluşturmanın) deyimsel yolu **kurucudur (constructor)**. Kitabın sözü: *"Bu, programlama dilinin
>    tasarımı etkilemesinin bir örneğidir. Her zaman bir **ödünleşme** vardır: **(a)** domain'i en saf
>    ve en 'doğru' biçimde ifade etmek ↔ **(b)** programlama diliyle kavga etmemek ve o dilde
>    'deyimsel olmayan' (unidiomatic) görünen kod yazmamak."*
> 2. `Price` ve `Installment` **ayrı tip olmadı**; ikisi de "bir para miktarı" olarak ifade edildiği
>    için tek bir `Amount` tipi kuruldu → o iş nesneleri **tip değil, değişken ve parametre adı** oldu.
>
> Yani **"iş nesnesi → sınıf" bir YÖNDÜR, otomatik bir eşleme değil.**

**Anemik (cılız) model — YAPMA.** Domain'i düzgün bilmeden koda başlarsan modelin **anemik** olur:
veri + o veriyi kurcalayan setter'lar. Kitabın örneği: `setSignatureDate(LocalDate date)` — imza
tarihi **her zaman herhangi bir değere** ayarlanabilir. Oysa "teklif" bağlamında bir sözleşme ancak
**(a) taksit hesaplanmışsa** ve **(b) daha önce imzalanmamışsa** imzalanabilir. Bu bir **iş
kuralıdır** (politika).

> **İyi bir domain modeli, programcıların (ve kullanıcıların) domain'in kurallarını çiğnemesini
> ENGELLER.**

Bu iş kuralının arkasında bir yapı var. Sözleşmenin yaşam döngüsü aslında basit bir **durum makinesidir
(state machine)** (bkz. **Diyagram 4**):

> `doldurulmuş → hesaplanmış → teklif edilmiş → imzalanmış`
> (filled out → calculated → offered → signed)

İş kuralı, bu makinenin bir **geçiş kısıtıdır**: `sign()` yalnızca *hesaplanmış* durumundan
çağrılabilir.

**Sözleşmeye göre tasarım (Design by Contract).** Metotlar **ön koşul** (precondition) ve **son
koşulla** (postcondition) genişletilir: `sign()` çağrılmadan önce `isCalculated()` doğru ve
`isSigned()` yanlış olmalı; çağrıldıktan sonra `isSigned()` doğru olmalıdır. Çoğu dilde yerleşik
desteği yoktur → **bir düşünce aracı olarak kullan**, gerekirse `assert` ile taklit et.

**Fonksiyonel tarafta (kitapta örnek dil: F#)** aynı durum makinesi **tip sistemine gömülür**: her
durum için **ayrı bir tip** tanımlanır (`FilledOutContract`, `CalculatedContract`, …) ve `Contract`
bunların birleşimi olur. Hedef tek cümlede şudur:

> **Geçersiz durumu temsil edilemez kılmak** (make illegal state unrepresentable).

İş nesneleri **tipe**, faaliyetler **fonksiyona** dönüşür — altın kuralın fonksiyonel karşılığı.

**Ve kitabın kendi dipnotu — Hafta 9'un tam kalbi:**

> *"DDD tasarlandığında **katmanlı mimari** son teknolojiydi ve 'domain **katmanı**' adı o zamandan
> kalma. Bu arada topluluk öğrendi ki katmanlı mimarinin **'üstte/altta' ayrımı, mimarinin ne
> olmasını istediğimizi hayal etmeye YETMİYOR.** Bu yüzden bugün **'içeride/dışarıda' ayrımını** da
> kullanma eğilimindeyiz. Bu, daha gelişmiş stillerde ifade ediliyor: **Hexagonal, Onion, Clean
> Architecture.** Yani bahsettiğimiz domain 'katmanı' aslında daha çok bir domain **HALKASIDIR
> (ring)**."*

**Her zaman zengin model gerekmez.** Domain mantığı için dört örüntü var: **transaction script**
(her işlem için baştan sona akan tek bir yordam), **table module** (tablo başına bir sınıf),
**anemic domain model** (veri + setter), **domain model** (kuralları kendi içinde taşıyan zengin
model). Zengin bir domain modelinin **bir bedeli vardır**; domain'in bazı parçaları doğası gereği
**veri-merkezlidir** ve **CRUD** (create-read-update-delete — "oluştur, oku, güncelle, sil"; kayıt
tutmaktan ibaret temel işlemler) **yeter**.

> **Domain tarafından haklı çıkarılmayan bir şeyi icat etmenin anlamı yoktur.** CRUD modelleme kendi
> başına kötü olmak zorunda değil. Ama domain'in **daha ilginç** parçaları, veri-merkezli olmayanlardır
> — ve bu tipik olarak DDD'nin **çekirdek domain** (core domain) dediği yerde geçerlidir.
> Zengin **domain model** stili, çekirdek domain için **tipik seçimdir.**

Son olarak, kabul testi (acceptance test) domain story'den doğrudan çıkar: "Satış temsilcisi
sözleşme için taksidi hesaplar" cümlesi → `Given` doldurulmuş sözleşme + fiyat/vade/faiz, `When`
hesaplandığında, `Then` taksit W EUR olur. Ama **domain story'ler genelde bu granülaritede
modellenmez** → somut değerler için uzmandan (Sandy) ek girdi gerekir.

### 12. Gölge BT'yi bulmak
**Gölge BT (shadow IT)**, bilgi işlem biriminin bilgisi ve onayı dışında, işi yapan kişilerce
kurulmuş ve kullanılan yazılımdır — çoğu zaman bir Excel dosyası.

> Belli bir büyüklüğün üstündeki **her** şirket, merkezî BT'nin haberi olmadığı yazılımlar kullanır.
> İş departmanlarında koşan, neredeyse kimsenin bilmediği o küçük çözümler **çoğu kez iş-kritiktir.**

**Uçak fabrikası zinciri (Henning ve Carola'nın MES analizi).** (**MES** = manufacturing execution
system — fabrikadaki üretimin planlanmasını ve takibini yapan yazılım.) Amaç üretim duruşunu
(downtime) en
aza indirmekti; bir günlük duruş milyonlara mal oluyordu. DIGITALIZED, AS-IS hikâyeler modellendi ve
şu zincir çıktı:

> **Planlama sistemi (bilinen BT) → gövde tasarımını belli bir sabit diske bir PDF olarak dışa
> aktarıyor → bu PDF bir EXCEL dosyasına çevriliyor (GÖLGE BT!) → bu dosyayı üretim kontrolörü okuyup
> gövde üretimini başlatıyor.**
> Öğrenilen: **gövde tasarımı Excel dosyası olmadan üretim durur.**

Alphorn'da da aynı avı yaptılar. Risk yöneticisi Raymond'a "sözleşmeyi oylarken araba fiyatı kararını
nasıl etkiliyor?" diye soruldu; cevap incelikliydi (*"pahalı arabaları tipik olarak zenginler kiralar
— satın alabilirler ama vergi sebebiyle kiralarlar; faturalarını da öderler. Yani pahalı arabada risk
düşüktür."*). Sonrası, moderatörün **her cevabı bir soruyla eşelemesinin** ders niteliğinde örneğidir:

- *"Peki riski nasıl değerlendiriyorsun?"* → **"Araba fiyatını **fiyat risk tablosuyla**
  karşılaştırıyorum."**
- *"Fiyat risk tablosu nedir?"* → **"Bir arabanın fiyatına dayanarak **ödeme kaçırma olasılığını**
  hesaplar."**
- *"Bunu açıklar mısın?"* → **"Bir müşteri taksidi ödemediğinde **muhasebe bize haber verir.**"**
- → **"Ödenmemiş taksidi duyduğumda onu **eksik ödeme takibine** (missing payments tracking)
  giriyorum. Oradan da fiyat risk tablosu bir **VLOOKUP** (Excel'in "bu değeri şu tabloda ara ve
  karşılığını getir" formülü — Türkçe Excel'de DÜŞEYARA) ile çıkarılıyor."**

Sonuç: **"eksik ödeme takibi" bir Excel sayfası, "fiyat risk tablosu" bir başkası.** İş mantığının
önemli bir kısmı Excel dosyalarına dayanıyordu.

**Yakalama sinyalleri (pratik heuristik):**
> **Gizli yazılım kullanımı, iş nesneleri "birdenbire, durup dururken" ortaya çıktığında belli olur.**
1. Uzman *"XYZ listesine bakarım"* der.
2. *"Bunu nereden biliyorsun?"* sorusu *"Onu ABC raporundan aldım"* diye yanıtlanır.

Peşine düşünce genellikle **Microsoft Access, Excel, Lotus Notes ya da shell script'ler** çıkar.
Kitabın dengeli hükmü: **BT'ci olmayan insanların yazılım yazması kendi başına kötü değildir; kötü
olan, bu yazılımın sadece eğitimli gözün fark edebileceği tüm risklere açık olmasıdır** (yedekleme,
sürüm kontrolü, bilgi tekeli, güvenlik).

### 13. Değişimi görselleştirmek — renkli diff ve make/buy
Yeni yazılımın amacı işi daha iyi yapmaktır. Ama:

> **Bu hedefe KÖTÜ MANUEL SÜREÇLERİ DİJİTALLEŞTİREREK ulaşılmaz.** Bir yığın gereksinim de sihirli
> biçimde kusursuz bir iş akışına dönüşmez. **Geleceğin çalışma biçimini TASARLAMAN gerekir.**

**Renkli diff (kitabın lejantı):**

| Öğe | Renk |
|---|---|
| **Kaldırılan** ya da değişen öğeler | **KIRMIZI** |
| **Yeni** ya da değişen öğeler | **YEŞİL** |
| **Değişmeyen** öğeler | **SİYAH** |

AS-IS ve TO-BE hikâyeleri yan yana konup renklendirilince, risk yönetimi takımının her üyesi şunu
görebiliyor: *"Bu ikon **beni** temsil ediyor. Ona bağlı **kırmızı** oklar hangi görevlerimin
değişeceğini, **yeşil** oklar benim için neyin yeni olacağını gösteriyor."* Değişimi **kafaya, kalbe
ve ele** birden anlatmanın en ucuz yolu budur. *(Renk yerine gruplama da denenebilir ama yazarların
deneyimine göre **o kadar faydalı değildir**: değişen parçalar kolayca gruplanamaz.)*

**Make/buy — Paynator vs. GreatPay.** Alphorn "ödeme" bağlamı için hazır ürün almayı düşünüyor. Önce
**PURE** süreçler modellendi (Alphorn 8: standart ödeme; Alphorn 9: eksik ödeme). Sonra **aynı
hikâye, her aday sistemle DIGITALIZED olarak yeniden** modellendi:

- **Standart vaka:** Paynator ile muhasebecinin işi **azalıyor**; GreatPay ile **tamamen otomatik**.
  → İlk bakışta **GreatPay kazanan** görünüyor.
- **Eksik ödeme vakası:** Paynator, muhasebecinin önceden elle yaptığı **her şeyi** yapıyor; GreatPay
  ile **ihtar/takip (dunning) işini muhasebeci yapmak zorunda.**
- **Karar:** Geçmişte elle yapılan ihtar çok hataya sebep olduğu için yönetim **Paynator'ü** seçiyor.

> **Kitabın dürüst uyarısı:** *"İtiraf edelim, Alphorn örneği oldukça basitleştirilmiş."* Gerçek
> dünyada karar sadece hangi sistemin iş süreçlerini en iyi desteklediğine bakmaz; **maliyet, teknik
> uyum, kullanılabilirlik** gibi ölçütler de tartılır. Domain story'nin katkısı şudur: bir sistemin
> **kullanıcıların çalışma biçimini nasıl değiştireceğini** görünür kılar — kararın tamamını vermez,
> ama doğru olanı seçmeye yardım eder.

> **Metodolojik öz: Bir senaryoda kazanan, diğerinde kaybedebilir → en az İKİ senaryo (standart +
> istisna/hata vakası) modellenmelidir.**

> **Son bir uyarı — kitap kendi kurallarını kanun saymıyor.** Deneyim arttıkça piktografik dilin
> grameri konusunda rahatlarsın; bir domain story'nin nasıl görünmesi gerektiğini bildiğinde
> **yardımcı tekerlekler çıkabilir.** *"Sonuçta amaç **anlam aktarmaktır**, katı kurallara uymak
> değil."* Yazarlar yazdıklarını çöpe atmanı önermiyor — ama bir noktadan sonra bu kuralları
> **kanun değil, tavsiye** olarak gör.

### 14. Yöntemin özü — yedi değişmez
1. **Kolektif:** Birkaç kişi **tek bir hikâyeyi** anlatmaya ve dinlemeye katılır.
2. **Konuşmaya dayalı:** Anlatıcılar ile **soru soran** dinleyicilerin konuşmasıdır; bir moderatör
   kolaylaştırır.
3. **Görsel:** Görselleştirme **ek bir geri bildirim kanalı** açar. *Hikâye anlatmak aynı zamanda
   bir modelleme eylemidir.*
4. **Senaryo temelli:** Bir domain story, iş sürecinin **tek, somut, anlamlı bir örneği** hakkındadır.
5. **Amaçlı:** Kendi başına bir amaç değil, bir **hedefe ulaşma aracıdır.**
6. **Çok yönlü:** Kapsam granülaritede, zaman noktasında ve domain saflığında değişebilir.
7. **Entegre:** DDD, gereksinim mühendisliği, test etme ve çevik geliştirmeyle birleştirilebilir.

### 15. 2026 notu — kitaptan sonra ne oldu

> **Bu bölüm kitapta YOKTUR.** Buraya kadarki her şey Hofer ve Schwentner'ın kitabından; bu bölüm
> ise 2026 itibarıyla dışarıda ne olduğunun notudur. İkisini karıştırma — kitabın kendi zinciri
> (Diyagram 3) olduğu gibi durur; aşağıdaki, o zincire dışarıdan eklenen bir halkadır.

Kitabın **ikinci baskısı yok**, yöntemin özü değişmedi. Değişen üç şey var.

**(a) Araç yaşıyor ve artık makine-okunur çıktı veriyor.** Yazarların açık kaynak aracı **Egon.io**
2025'te baştan yazıldı, 2026 boyunca sürüm almaya devam etti (v3.2.1, Haziran 2026). **Yeniden
anlatım (replay) modu duruyor** — hatta gelişti. Asıl önemlisi: bir domain story **metin dosyası
olarak** (ya da içine o metin gömülmüş bir SVG olarak) dışa aktarılabiliyor. Yani bir domain story
**zaten bir metin artefaktıdır** — ve (c) maddesini mümkün kılan şey budur.

**(b) Yöntem kurumsallaştı.** DDD Europe (2026) programında Hofer ve Schwentner hâlâ çalıştay
veriyor; kolektif modelleme kampı CoMoCamp sürüyor. EventStorming'le ilişki artık "rekabet" değil
**boru hattı** olarak anlaşılıyor: Domain Storytelling ile **kimin kiminle ne yaptığını** keşfet →
design-level EventStorming ile **yazılımın nasıl çalışacağını** tasarla.

**(c) VE ASIL OLAY: domain story, dil modelinin (LLM) BAĞLAMI hâline geldi.**

> **"Dil modelleri domain işinde zeki olmadıkları için başarısız olmuyor. Senin domain'ine
> ERİŞİMLERİ OLMADIĞI için başarısız oluyorlar."** — Annegret Junker (codecentric), Mart 2026

2026'da yazılı hâle gelen pratik şu: **domain story + görsel sözlük** (visual glossary) doğrudan
dil modeline verilir; model tıklanabilir bir prototip ya da bir API sözleşmesi üretir. İş bölümü
nettir:

> **"Domain story, yapay zekâya HANGİ ekranları HANGİ SIRADA kuracağını söyler.
> Görsel sözlük ise ona her şeye NE AD vereceğini söyler."**

Ve şu cümle, kitabın **Bölüm 9'daki** iddiasının ("dil, kodda kullanılarak *her yerde geçerli*
olur") makine çağındaki karşılığıdır:

> **"O dilin kalitesi, üretilen çıktının kalitesini doğrudan belirler."**

**Asıl kazanç hız değil, geri bildirim döngüsünün kısalması.** Junker'in bisiklet kiralama
çalıştayında prototip **çalıştay hâlâ sürerken** hazır oluyor; domain uzmanlarından biri ekrana
**iki dakika** bakıp soruyor:

> *"Peki bir kullanıcı rafa vardığında bisiklet çoktan gitmişse ne olacak?"*

Bu bir **yarış durumudur** (race condition) ve çalıştayda kimse dile getirmemiştir. Yani prototip,
kitabın 1. bölümündeki **yeniden anlatmanın (retelling)** makineyle yapılmış hâlidir: Anna'nın
hikâyeyi baştan geri anlatıp yanlış anlaşılmayı patlatması gibi, dil modeli de anladığını
**çalıştırılabilir bir şey olarak** geri anlatır — ve boşluk orada görünür olur.

**(d) Sınırlı bağlam = AI ajanının sınırı.** Kitabın Bölüm 10'daki sınır bulma işi, 2026'da yeni
bir müşteri buldu: **ajan mimarisi.** Siemens'ten Nikita Golovko'nun tespiti (Şubat 2026): agentic
sistemlerde prompt'lar şişip **"prompt spagettisine"** dönüşüyor — bir ajanın prompt'unun *"yaklaşık
%85'i ayrıştırma ve entegrasyon mantığı"* oluyor, domain değil. Reçetesi doğrudan Evans'tan:

- **"Bir ajan = bir sınırlı bağlam = bir sorumluluk."**
- **"Doğal dili API'miz olarak kullanmayı bırakmalıyız. Sözleşme olarak yapılandırılmış şemalar
  kullanmalıyız."**
- **"Yozlaşma-önleyici katman (anti-corruption layer) bizim semantik güvenlik duvarımızdır."**

Kitabın *"bağlam sınırları modelleri ayırmalıdır, insanları değil"* kuralı bugün bir adım daha
gidiyor: **sınırlar modelleri ayırır — ve artık ajanları da.** Ama uyarı da var: ajanlar
kendilerine çizilen sınırı **kendiliğinden aşar** (yardım masası botu bir bakmışsın politika
önerisi yapıyor), yani sınır statik değil, **bakım isteyen yaşayan bir sınırdır.**

**(e) Madalyonun karanlık yüzü: yapay zekâ, her yerde geçerli dili AŞINDIRIR.** Kodlama
asistanları **anlamsal kaymayı** (semantic drift) hızlandırır: domain terimini alıp yerine daha
"yaygın" jenerik bir ad koyar — `Manager`, `Handler`, `Processor`. Duncan Brown'ın formülü:

> **"Yapay zekâ, FORMALİZE EDİLMEMİŞ bir domain sözlüğüne saygı gösteremez."**

Kitabın Bölüm 9'daki homonim/eşanlamlı avı ("hesap" hikâyesi) bu yüzden 2026'da **daha da**
yakıcıdır. Kitabın öğüdü — *"sözlük yaz, ama sözlüğü konuşmanın yerine koyma"* — hâlâ geçerli;
ama sözlüğün artık **ikinci bir okuyucusu** var. 2026 pratiği: sözlüğü **kanonik ve makine-okunur**
bir dosyada tut, **kod gibi sürümle ve gözden geçir**, ve **ajanın prompt'una kısıt olarak ver.**

> Kitap "sözlüğü yazmak dili öğrenmene yardım ettiyse zahmete değmiştir" diyordu.
> 2026'da bir de **makinenin dili öğrenmesine** yarıyor.

**Kaynaklar (14 Tem 2026'da doğrulandı):** Annegret Junker, codecentric —
*"From Stories to Code"* (4 Mar 2026) ve *"From Domain Story to Prototype"* (2 Nis 2026) ·
Nikita Golovko (Siemens), AI Coding Summit 2026 — *"From Prompt Spaghetti to Bounded Contexts"*
(26 Şub 2026) · Duncan Brown — *"AI Coding Assistants and the Erosion of Ubiquitous Language"*
(2025) · Egon.io sürüm geçmişi (github.com/WPS/egon.io, v3.2.1 / Haz 2026).

## Diyagram 1 — Piktografik dil: bir cümle nasıl kurulur
Metropolis'in 1. cümlesi: *"Sinemasever gişe görevlisine seansı söyler."* Cümle **özne (aktör) –
yüklem (faaliyet + sıra numarası) – nesne (iş nesnesi)** biçimindedir. Ve dilde **koşul yoktur.**

```svg
<svg viewBox="0 0 640 372" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Piktografik dil: sinemasever aktörü, seans iş nesnesi ve gişe görevlisi aktörü arasında numaralı bir faaliyet oku; yanında not kutusu, koşul yok uyarısı ve yapı taşları lejantı">
  <defs>
    <marker id="dsok-a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="dsok-b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="dsok-f" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#1C3247"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Piktografik dil: bir cümle = özne – yüklem – nesne</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Metropolis 1. cümle: “Sinemasever gişe görevlisine seansı söyler.”</text>

  <rect x="24" y="72" width="140" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <circle cx="94" cy="92" r="7" fill="none" stroke="#186399" stroke-width="1.6"/>
  <line x1="94" y1="99" x2="94" y2="118" stroke="#186399" stroke-width="1.6"/>
  <line x1="81" y1="106" x2="107" y2="106" stroke="#186399" stroke-width="1.6"/>
  <line x1="94" y1="118" x2="85" y2="132" stroke="#186399" stroke-width="1.6"/>
  <line x1="94" y1="118" x2="103" y2="132" stroke="#186399" stroke-width="1.6"/>
  <text x="94" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">sinemasever</text>
  <text x="94" y="163" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">aktör · ÖZNE</text>

  <rect x="250" y="80" width="140" height="88" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <rect x="296" y="92" width="48" height="28" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <path d="M312,120 L308,132 L322,120 Z" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="320" y="148" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">seans</text>
  <text x="320" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iş nesnesi · NESNE</text>

  <rect x="476" y="72" width="140" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <circle cx="546" cy="92" r="7" fill="none" stroke="#186399" stroke-width="1.6"/>
  <line x1="546" y1="99" x2="546" y2="118" stroke="#186399" stroke-width="1.6"/>
  <line x1="533" y1="106" x2="559" y2="106" stroke="#186399" stroke-width="1.6"/>
  <line x1="546" y1="118" x2="537" y2="132" stroke="#186399" stroke-width="1.6"/>
  <line x1="546" y1="118" x2="555" y2="132" stroke="#186399" stroke-width="1.6"/>
  <text x="546" y="150" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">gişe görevlisi</text>
  <text x="546" y="163" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">aktör</text>

  <line x1="166" y1="120" x2="246" y2="120" stroke="#186399" stroke-width="2" marker-end="url(#dsok-a)"/>
  <circle cx="180" cy="120" r="11" fill="#FFFFFF" stroke="#186399" stroke-width="1.6"/>
  <text x="180" y="124" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1</text>
  <text x="216" y="108" text-anchor="middle" font-size="10.5" font-weight="600" fill="#186399" font-family="system-ui">söyler</text>
  <line x1="392" y1="120" x2="472" y2="120" stroke="#5D7789" stroke-width="1.8" marker-end="url(#dsok-b)"/>
  <text x="432" y="108" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">-e (edat)</text>

  <line x1="94" y1="168" x2="94" y2="182" stroke="#C77A17" stroke-width="1.4" stroke-dasharray="4 3"/>
  <rect x="24" y="182" width="200" height="56" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="124" y="202" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">NOT (annotation)</text>
  <text x="124" y="217" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">“sinemaseverin aboneliği yok”</text>
  <text x="124" y="231" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">öğe · cümle · hikâye hakkında</text>

  <rect x="250" y="182" width="366" height="56" rx="8" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="433" y="202" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">KOŞUL YOK (no conditionals)</text>
  <text x="433" y="217" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">“eğer” / “veya” için sembol YOKTUR — bu kasıtlıdır.</text>
  <text x="433" y="231" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Her önemli alternatif = AYRI bir domain story.</text>

  <line x1="24" y1="256" x2="616" y2="256" stroke="#D6DEE5" stroke-width="1"/>
  <text x="24" y="274" text-anchor="start" font-size="10" font-weight="700" fill="#5D7789" font-family="system-ui">LEJANT</text>

  <circle cx="44" cy="298" r="5" fill="none" stroke="#186399" stroke-width="1.5"/>
  <line x1="44" y1="303" x2="44" y2="316" stroke="#186399" stroke-width="1.5"/>
  <line x1="35" y1="308" x2="53" y2="308" stroke="#186399" stroke-width="1.5"/>
  <line x1="44" y1="316" x2="38" y2="326" stroke="#186399" stroke-width="1.5"/>
  <line x1="44" y1="316" x2="50" y2="326" stroke="#186399" stroke-width="1.5"/>
  <text x="64" y="303" text-anchor="start" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">aktör — ÖZNE</text>
  <text x="64" y="318" text-anchor="start" font-size="8.5" fill="#5D7789" font-family="system-ui">kişi / yazılım sistemi</text>

  <rect x="182" y="298" width="26" height="20" rx="5" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="218" y="303" text-anchor="start" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">iş nesnesi — NESNE</text>
  <text x="218" y="318" text-anchor="start" font-size="8.5" fill="#5D7789" font-family="system-ui">belge · bilgi · ortam</text>

  <line x1="336" y1="308" x2="370" y2="308" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-f)"/>
  <circle cx="344" cy="308" r="8" fill="#FFFFFF" stroke="#1C3247" stroke-width="1.4"/>
  <text x="344" y="311" text-anchor="middle" font-size="8.5" font-weight="700" fill="#1C3247" font-family="system-ui">n</text>
  <text x="380" y="303" text-anchor="start" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">faaliyet + sıra no</text>
  <text x="380" y="318" text-anchor="start" font-size="8.5" fill="#5D7789" font-family="system-ui">fiil; sıra no = ZAMAN</text>

  <rect x="486" y="298" width="26" height="20" rx="5" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="522" y="303" text-anchor="start" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">not / grup</text>
  <text x="522" y="318" text-anchor="start" font-size="8.5" fill="#5D7789" font-family="system-ui">açıklama · gruplama</text>

  <text x="320" y="352" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Altın kural: aktörler hikâye başına BİR KEZ görünür; iş nesneleri BİRDEN ÇOK KEZ görünebilir.</text>
</svg>
```

## Diyagram 2 — Kapsam küpü: üç eksen ve Alphorn'un yolculuğu
Kapsam üç ekseni çarpar. Alphorn 1→5, bu üç eksende gezinen tipik bir yolculuktur: **keşif →
derinleşme → temizleme → tasarım → dijitalleştirme.**

```svg
<svg viewBox="0 0 640 428" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kapsam üç ekseni: granülarite bulut uçurtma deniz balık istiridye, domain saflığı pure digitalized, zaman noktası as-is to-be; altında Alphorn 1'den 5'e beş duraklı yolculuk">
  <defs>
    <marker id="dsok-c" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
    <marker id="dsok-d" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#1C3247"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kapsam = Granülarite × Domain Saflığı × Zaman Noktası</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Üç eksenin çarpımı; her domain story bu üç ekseni birden ayarlar.</text>

  <text x="24" y="88" text-anchor="start" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Granülarite</text>
  <text x="150" y="88" text-anchor="start" font-size="9" font-weight="700" fill="#5D7789" font-family="system-ui">COARSE</text>
  <text x="616" y="88" text-anchor="end" font-size="9" font-weight="700" fill="#5D7789" font-family="system-ui">FINE</text>
  <line x1="150" y1="100" x2="616" y2="100" stroke="#5D7789" stroke-width="1.2" marker-end="url(#dsok-c)"/>
  <circle cx="192" cy="100" r="4.5" fill="#186399"/>
  <circle cx="288" cy="100" r="4.5" fill="#186399"/>
  <circle cx="384" cy="100" r="4.5" fill="#186399"/>
  <circle cx="480" cy="100" r="4.5" fill="#186399"/>
  <circle cx="576" cy="100" r="4.5" fill="#186399"/>
  <text x="192" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bulut</text>
  <text x="288" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">uçurtma</text>
  <text x="384" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">deniz</text>
  <text x="480" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">balık</text>
  <text x="576" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">istiridye</text>

  <text x="24" y="153" text-anchor="start" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Domain saflığı</text>
  <text x="200" y="153" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">yazılım YOK</text>
  <text x="580" y="153" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">yazılım DAHİL</text>
  <line x1="150" y1="165" x2="616" y2="165" stroke="#5D7789" stroke-width="1.2"/>
  <circle cx="200" cy="165" r="5" fill="#25804C"/>
  <circle cx="580" cy="165" r="5" fill="#4F46E5"/>
  <text x="200" y="183" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">PURE</text>
  <text x="580" y="183" text-anchor="middle" font-size="10" font-weight="700" fill="#4F46E5" font-family="system-ui">DIGITALIZED</text>

  <text x="24" y="218" text-anchor="start" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Zaman noktası</text>
  <text x="200" y="218" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">problem uzayı</text>
  <text x="580" y="218" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">çözüm uzayı</text>
  <line x1="150" y1="230" x2="616" y2="230" stroke="#5D7789" stroke-width="1.2"/>
  <circle cx="200" cy="230" r="5" fill="#186399"/>
  <circle cx="580" cy="230" r="5" fill="#C77A17"/>
  <text x="200" y="248" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">AS-IS</text>
  <text x="580" y="248" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">TO-BE</text>

  <line x1="24" y1="266" x2="616" y2="266" stroke="#D6DEE5" stroke-width="1"/>
  <text x="24" y="286" text-anchor="start" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">Alphorn Auto Leasing: tipik yolculuk (Bölüm 8)</text>

  <rect x="24" y="296" width="104" height="88" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="76" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Alphorn 1</text>
  <text x="76" y="332" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">KEŞİF</text>
  <text x="76" y="348" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">COARSE</text>
  <text x="76" y="362" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">PURE</text>
  <text x="76" y="376" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">AS-IS</text>

  <rect x="146" y="296" width="104" height="88" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="198" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Alphorn 2</text>
  <text x="198" y="332" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">DERİNLEŞME</text>
  <text x="198" y="348" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">FINE</text>
  <text x="198" y="362" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">DIGITALIZED</text>
  <text x="198" y="376" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">AS-IS</text>

  <rect x="268" y="296" width="104" height="88" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="320" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Alphorn 3</text>
  <text x="320" y="332" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">TEMİZLEME</text>
  <text x="320" y="348" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">FINE</text>
  <text x="320" y="362" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">PURE</text>
  <text x="320" y="376" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">AS-IS</text>

  <rect x="390" y="296" width="104" height="88" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="442" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Alphorn 4</text>
  <text x="442" y="332" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">TASARIM</text>
  <text x="442" y="348" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">FINE</text>
  <text x="442" y="362" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">PURE</text>
  <text x="442" y="376" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">TO-BE</text>

  <rect x="512" y="296" width="104" height="88" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="564" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Alphorn 5</text>
  <text x="564" y="332" text-anchor="middle" font-size="9.5" font-weight="700" fill="#1C3247" font-family="system-ui">DİJİTALLEŞME</text>
  <text x="564" y="348" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">FINE</text>
  <text x="564" y="362" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">DIGITALIZED</text>
  <text x="564" y="376" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">TO-BE</text>

  <line x1="130" y1="340" x2="143" y2="340" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-d)"/>
  <line x1="252" y1="340" x2="265" y2="340" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-d)"/>
  <line x1="374" y1="340" x2="387" y2="340" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-d)"/>
  <line x1="496" y1="340" x2="509" y2="340" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-d)"/>

  <text x="320" y="404" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">2→3: teknik jargondan arın · 3→4: süreci iyileştir · 4→5: yazılım desteğini geri koy</text>
  <text x="320" y="419" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Yolculuk yinelemelidir: kapsamı gerektikçe değiştirirsin, sıralı gitmek zorunda değilsin.</text>
</svg>
```

## Diyagram 3 — Hikâyeden sınıra ve koda: kitabın uçtan uca zinciri
Aynı hikâye, önce **alt-domain gruplarına**, oradan **sınırlı bağlama**, oradan **takım sınırına**
(Conway) ve nihayet **sınıf ve metoda** dönüşür.

```svg
<svg viewBox="0 0 640 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zincir: domain story, alt-domain grupları, sınırlı bağlam, takım sınırı ve kod; altta iş nesnesi sınıfa faaliyet metoda eşlemesi">
  <defs>
    <marker id="dsok-e" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#1C3247"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Hikâyeden sınıra ve koda</text>
  <text x="320" y="44" text-anchor="middle" font-size="11.5" fill="#5D7789" font-family="system-ui">Tek bir COARSE · PURE · AS-IS hikâye, mimarinin ve takımın temeline dönüşür.</text>

  <rect x="22" y="70" width="108" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="76" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">1. Domain story</text>
  <text x="76" y="114" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek senaryo</text>
  <text x="76" y="128" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">mutlu yol</text>
  <text x="76" y="148" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">COARSE · PURE</text>

  <rect x="144" y="70" width="108" height="96" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="198" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">2. Alt-domain</text>
  <text x="198" y="114" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">faaliyet grupları</text>
  <text x="198" y="128" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">(aktörler dışta)</text>
  <text x="198" y="148" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">6 sınır göstergesi</text>

  <rect x="266" y="70" width="108" height="96" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="320" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#4F46E5" font-family="system-ui">3. Sınırlı bağlam</text>
  <text x="320" y="114" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">bounded context</text>
  <text x="320" y="128" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kendi dili + modeli</text>
  <text x="320" y="148" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">context map</text>

  <rect x="388" y="70" width="108" height="96" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="442" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">4. Takım sınırı</text>
  <text x="442" y="114" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">Conway yasası</text>
  <text x="442" y="128" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">1 bağlam = 1 takım</text>
  <text x="442" y="148" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">dikey kesit</text>

  <rect x="510" y="70" width="108" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="564" y="94" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">5. Kod</text>
  <text x="564" y="114" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">sınıf + metot</text>
  <text x="564" y="128" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">entity / value object</text>
  <text x="564" y="148" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">domain HALKASI</text>

  <line x1="132" y1="118" x2="142" y2="118" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-e)"/>
  <line x1="254" y1="118" x2="264" y2="118" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-e)"/>
  <line x1="376" y1="118" x2="386" y2="118" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-e)"/>
  <line x1="498" y1="118" x2="508" y2="118" stroke="#1C3247" stroke-width="1.8" marker-end="url(#dsok-e)"/>

  <rect x="22" y="190" width="290" height="100" rx="10" fill="#FFFFFF" stroke="#25804C" stroke-width="1.6"/>
  <text x="167" y="214" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">İş nesnesi → SINIF</text>
  <text x="167" y="236" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">“sözleşme” — kimliği var → entity Contract</text>
  <text x="167" y="254" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">“taksit, fiyat, vade” — kimliksiz → value object</text>
  <text x="167" y="276" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Soru: “Bu bir ŞEY mi, bir DEĞER mi?”</text>

  <rect x="328" y="190" width="290" height="100" rx="10" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="473" y="214" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Faaliyet → METOT</text>
  <text x="473" y="236" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">“hesapla” → calculateInstallmentFor(...)</text>
  <text x="473" y="254" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">“imzala” → sign() · ön/son koşul (DbC)</text>
  <text x="473" y="276" text-anchor="middle" font-size="9.5" fill="#C77A17" font-family="system-ui">Anemik model = kural yok = YAPMA</text>

  <text x="320" y="314" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Kitabın kendi dipnotu: katmanlı mimarinin “üstte/altta” ayrımı YETMİYOR →</text>
  <text x="320" y="330" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">“içeride/dışarıda” gerekir; domain bir KATMAN değil, bir HALKA (Hexagonal · Onion · Clean).</text>
</svg>
```

## Diyagram 4 — Sözleşmenin durum makinesi: anemik model vs. zengin model
İş kuralı ("bir sözleşme ancak taksit hesaplanmışsa ve daha önce imzalanmamışsa imzalanabilir") aslında
bir **durum geçişi kısıtıdır.** Anemik model bu kuralı hiçbir yerde tutmaz; zengin model onu **durum
makinesine gömer** — böylece geçersiz durum baştan imkânsızlaşır.

```svg
<svg viewBox="0 0 640 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sözleşmenin durum makinesi: doldurulmuş, hesaplanmış, teklif edilmiş, imzalanmış durumları arasında metot geçişleri; sign metodunun ön ve son koşulları; altta anemik model ile zengin modelin karşılaştırması">
  <defs>
    <marker id="dsok-g" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="dsok-h" markerWidth="9" markerHeight="9" refX="6" refY="3" orient="auto"><path d="M0,0 L6,3 L0,6 Z" fill="#4F46E5"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sözleşmenin yaşam döngüsü bir durum makinesidir</text>
  <text x="320" y="41" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">İş kuralı = geçiş kısıtı. Zengin model onu koda gömer; anemik model gömmez.</text>

  <rect x="24" y="62" width="100" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="74" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">doldurulmuş</text>
  <text x="74" y="101" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">filled out</text>

  <rect x="196" y="62" width="100" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="246" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">hesaplanmış</text>
  <text x="246" y="101" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">calculated</text>

  <rect x="368" y="62" width="100" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="418" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">teklif edilmiş</text>
  <text x="418" y="101" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">offered</text>

  <rect x="540" y="62" width="76" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="578" y="86" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">imzalanmış</text>
  <text x="578" y="101" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">signed</text>

  <line x1="124" y1="88" x2="194" y2="88" stroke="#186399" stroke-width="1.8" marker-end="url(#dsok-g)"/>
  <text x="159" y="80" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">taksit hesapla</text>
  <line x1="296" y1="88" x2="366" y2="88" stroke="#186399" stroke-width="1.8" marker-end="url(#dsok-g)"/>
  <text x="331" y="80" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">teklif et</text>
  <line x1="468" y1="88" x2="538" y2="88" stroke="#186399" stroke-width="1.8" marker-end="url(#dsok-g)"/>
  <text x="503" y="80" text-anchor="middle" font-size="8.5" font-weight="700" fill="#4F46E5" font-family="system-ui">sign()</text>

  <line x1="503" y1="114" x2="503" y2="132" stroke="#4F46E5" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#dsok-h)"/>
  <rect x="336" y="134" width="280" height="60" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="476" y="152" text-anchor="middle" font-size="10" font-weight="700" fill="#4F46E5" font-family="system-ui">sign() — Sözleşmeye Göre Tasarım (DbC)</text>
  <text x="476" y="169" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">ön koşul: isCalculated()=doğru, isSigned()=yanlış</text>
  <text x="476" y="185" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">son koşul: isSigned()=doğru</text>

  <rect x="24" y="214" width="290" height="104" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="169" y="234" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">ANEMİK (cılız) MODEL — YAPMA</text>
  <text x="169" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">setSignatureDate(date)</text>
  <text x="169" y="273" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">imza tarihi her durumdan, her değere yazılır</text>
  <text x="169" y="291" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kural yok → geçersiz durum mümkün</text>
  <text x="169" y="309" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">veri + setter yığını; domain'i yansıtmaz</text>

  <rect x="326" y="214" width="290" height="104" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="471" y="234" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">ZENGİN MODEL</text>
  <text x="471" y="255" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kural koda gömülü — sign() yalnız:</text>
  <text x="471" y="273" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">hesaplanmış → imzalanmış</text>
  <text x="471" y="291" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">değişmezlerle bütünlük; kural çiğnenemez</text>
  <text x="471" y="309" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">F#'ta: geçersiz durumu temsil edilemez kıl</text>
</svg>
```

## Öz-sınav (kendini test et)
1. Piktografik dilde koşul (if/veya) sembolü **kasıtlı olarak** yoktur. Bu kısıt, BPMN'in gateway'li
   yaklaşımına kıyasla domain'i anlamana nasıl yardım ediyor — ve alternatifleri nereye koyuyorsun?
2. "Aktörler hikâye başına bir kez, iş nesneleri birden çok kez görünür" kuralının pratik gerekçesi
   ne? Aynı biletin iki farklı ikonla çizilmesi neyi ifade eder?
3. Elindeki hikâyede bir faaliyet "koltuk planında **boş koltukları** arar" diye etiketlenmiş.
   Kitabın hangi üslup kuralı çiğnenmiş, ve bunu düzeltmek Bölüm 10/12'de (sınır bulmak, koda dökmek)
   neyi kurtarır?
4. Bir alt-domain'i AS-IS/DIGITALIZED modelledin ve terimler sana çok teknik geliyor. Kapsam
   ekseninde hangi yöne hareket edersin, domain uzmanlarına hangi soruyu sorarsın ve bu hangi Alphorn
   geçişine karşılık gelir?
5. Bir çalıştayda hikâyede "oklarla bağlanmamış" parçalar görüyorsun. Buna ne denir, hangi soruları
   sorman gerekir, ve neden bunu "soyutlamaya atlayarak" kapatmamalısın?
6. Alt-domain sınırı göstergelerinden ikisini seç ve Alphorn'daki karşılığını anlat. Bir gösterge
   neden "sınır olduğunun kesin kanıtı" sayılmaz?
7. "Bağlam sınırları modelleri ayırmalıdır, insanları değil" cümlesi ile "bir sınırlı bağlam yalnızca
   bir takıma ait olmalıdır" kuralı çelişiyor mu? Conway yasası ve ters Conway manevrası bu ikisini
   nasıl uzlaştırıyor?
8. Bir domain story cümlesini kullanıcı hikâyesi (user story) şablonuna eşle. Şablondaki "sebep"
   (neden) kısmı hikâyenin neresinden gelir, ve neden "omurga" (backbone) uçurtma seviyesinde yazılır?
9. Bir sözleşme sınıfına `setSignatureDate(...)` koydun. Bu neden anemik bir modeldir, hangi iş kuralı
   kaçırılmıştır, ve Design by Contract ile durum makinesi bunu nasıl kurtarır?
10. Uzman sana "bunu XYZ listesinden alıyorum" dedi. Hangi olguyu avlıyorsun, izini nasıl sürersin ve
    uçak fabrikası örneğinde bu zincirin sonunda ne bulundu?
11. *(2026)* Bir domain story'yi ve sözlüğü bir dil modeline verip prototip ürettin. Bu, kitabın
    1. bölümündeki hangi eyleme karşılık geliyor — ve neden asıl kazanç prototipin kendisi değil,
    domain uzmanının ona bakınca sorduğu sorudur?
12. *(2026)* "Bir ajan = bir sınırlı bağlam" kuralı, kitabın "bir sınırlı bağlam = bir takım"
    kuralının aynısı mı? Ajan sınırının takım sınırından farkı ne — ve neden "yaşayan sınır"
    demek gerekiyor?
13. *(2026)* Yapay zekâ `Sozlesme` yerine `ContractManager` yazdı. Bu hangi olgudur, kitabın
    Bölüm 9'undaki hangi derdin devamıdır, ve panzehiri nedir?
