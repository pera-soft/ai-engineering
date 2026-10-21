---
baslik: "Claude Code Nasıl Yapıldı — Köken Miti ve Aktarılabilir 3 Ders"
yazar: "Anthropic — 'The Making of Claude Code' sözlü tarihi (first-party marka anlatısı) — Türkçe çalışma metni"
tarih: "2026-07-20"
dil: "Türkçe kavram özeti (anlatı türü — tek diyagram)"
etiketler: 1,12
---

### Bu metin ne anlatıyor?
Bu metin, Claude Code adlı aracın nasıl ortaya çıktığının hikâyesini ve o hikâyeden alınabilecek
üç dersi anlatıyor. Hikâyeyi anlatan, aracı yapan şirketin kendisi — yani bir **başarı anlatısı**
okuyorsun. Bu yüzden metni iki iş için birden kullanıyoruz: bir yandan gerçekten işe yarayan
dersleri çıkarıyoruz, bir yandan da "başarı hikâyesi" okumayı öğreniyoruz — hangi kısmına
güvenilir, hangi kısmı sadece iyi hissettirir?

## Neden bu özet
Bu metin diğer kaynaklardan **tür olarak** farklıdır: mekanik değil, **anlatıdır** — Claude Code'un
köken hikâyesi, Anthropic'in kendi ağzından (Ben Mann, Boris Cherny, Dawn Drain, Cat Wu ve dış
kullanıcılar). Bir mühendislik dokümanı gibi okunmaz; **iki ayrı gözle** okunur:

> **Sol göz:** İçinde gerçekten aktarılabilir, mimari-ilgili birkaç ders var — çıkar ve sakla (§1–§3).
> **Sağ göz:** Bu bir **köken mitidir** (satıcının kendi başarı anlatısı) — anlatı yanlılığını gör,
> kaçınılmazlık hissini iskonto et (§4). İki göz aynı anda açık olmalı.

Bu yüzden konvansiyonun 3-diyagram kuralı burada uygulanmaz (CLAUDE.md istisnası: rapor/anlatı türü
diyagramsız kalabilir); tek bir diyagram, yazının en görselleştirilebilir tek fikrini taşır. Kardeş
kaynaklar: [`model-ve-caba.md`](model-ve-caba.md) "model donmuş" der — bu metin o donmuşluğun **zaman
eksenini** verir; [`harness-engineering-openai.md`](harness-engineering-openai.md) primitif/harness
tasarımını, [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) satıcı-anlatısı okuma refleksini kurar.

## Türkçe çalışma metni

### 1. Ders — "Bir sonraki model için inşa et" (build for the next model)
Yazının en değerli mühendislik dersi Ben Mann'den: model üretkenleştirmede uzun süre çalışmamış birine
sezgisel gelmez, ama —

> "Şimdi **%20-30 çalışan** bir şey inşa etmen gerekir; ki sonraki model geldiğinde **%80** çalışsın.
> Ve bu, pazar çekişi için yeterlidir. Sonraki model %90'ları bulur, işte o zaman gerçekten ilerlersin.
> Yüksek acı toleransın olmalı, çünkü tekrar tekrar yanılacaksın. **Şimdide olmalısın, ama aynı anda
> geleceğe bakmalısın.**"

Bunun [`model-ve-caba.md`](model-ve-caba.md) ile bağı doğrudandır: model **donmuştur** (ağırlıklar
salt-okunur — modelin "bildiği" şey kullanım sırasında değişmez), yani *bugünkü* modelin sınırına
göre ürünü küçültmek yerine, ürünü **bir üst yetenek eşiğine göre** tasarlarsın. **clide** (Claude
Code'un atası; Anthropic içinde yıllar önce denenmiş, terminalden çalışan ilk kodlama asistanı) tam
bu yüzden "zamanının çok ilerisinde ama kullanışsız"dı: **form** (ürünün biçimi — nasıl bir şey
olduğu, nereden kullanıldığı) doğruydu, model henüz değildi. Ders tersten de okunur — bugün
*mükemmel*
çalışmayan bir şey, yanlış ürün değil, **erken ürün** olabilir. Karar sorusu: "bu bugün kötü çünkü
fikir mi kötü, yoksa model mi henüz gelmedi?"

## Diyagram 1 — Yetenek eşiği: aynı ürün, ardışık modellerle yükselir
Ürünün "işe yarama oranı" tek atışta değil, her yeni modelde **basamak basamak** yükselir. clide
zamanının ilerisindeydi ama takeoff, model eşiği (Claude 4) ile iş modeli (abonelik) **aynı anda**
geldiğinde oldu.

```svg
<svg viewBox="0 0 660 366" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yetenek eşiği eğrisi: yatay eksen zaman ve ardışık model sürümleri, dikey eksen aynı ürünün işe yarama oranı yüzde; erken dönemde clide zamanının ilerisinde ama yaklaşık yüzde yirmi otuz çalışıyor yani form doğru model henüz değil, Claude 4 modelleriyle birlikte yüzde seksene sıçrıyor ve tam bu anda abonelik iş modeli de gelince takeoff oluyor yani model inovasyonu ile iş modeli inovasyonu birlikte, sonraki modellerle yüzde doksanları buluyor; ders şu ürünü bugünkü modelin sınırına göre küçültme bir sonraki yetenek eşiğine göre tasarla çünkü model donmuştur ama ardışık modeller aynı ürünü yükseltir">
  <defs>
    <marker id="mcc-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yetenek eşiği — aynı ürün, ardışık modellerle yükselir</text>
  <text x="330" y="43" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">"bir sonraki model için inşa et" — bugün %20-30, sonraki modelde %80</text>

  <line x1="66" y1="300" x2="66" y2="62" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mcc-1a)"/>
  <line x1="66" y1="300" x2="636" y2="300" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mcc-1a)"/>
  <text x="34" y="180" font-size="10" font-weight="700" fill="#5D7789" font-family="system-ui" transform="rotate(-90 34 180)" text-anchor="middle">işe yarama oranı %</text>
  <text x="350" y="322" text-anchor="middle" font-size="10" font-weight="700" fill="#5D7789" font-family="system-ui">zaman → ardışık model sürümleri</text>

  <line x1="66" y1="250" x2="636" y2="250" stroke="#E5E7EB" stroke-width="1"/>
  <line x1="66" y1="140" x2="636" y2="140" stroke="#E5E7EB" stroke-width="1"/>
  <line x1="66" y1="96" x2="636" y2="96" stroke="#E5E7EB" stroke-width="1"/>
  <text x="60" y="253" text-anchor="end" font-size="8.5" fill="#9CA3AF" font-family="system-ui">%25</text>
  <text x="60" y="143" text-anchor="end" font-size="8.5" fill="#9CA3AF" font-family="system-ui">%80</text>
  <text x="60" y="99" text-anchor="end" font-size="8.5" fill="#9CA3AF" font-family="system-ui">%90+</text>

  <path d="M96,252 L232,246 L300,238 L300,142 L470,138 L470,100 L620,94" fill="none" stroke="#4F46E5" stroke-width="2.6"/>
  <circle cx="164" cy="249" r="4" fill="#186399"/>
  <circle cx="300" cy="142" r="5" fill="#25804C"/>
  <circle cx="470" cy="100" r="4" fill="#4F46E5"/>

  <rect x="104" y="196" width="150" height="40" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.3"/>
  <text x="179" y="213" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">clide / erken (2022–24)</text>
  <text x="179" y="227" text-anchor="middle" font-size="8.5" fill="#5D7789" font-family="system-ui">form doğru, model değil</text>

  <rect x="230" y="150" width="176" height="44" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="318" y="167" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Claude 4 + abonelik (2025)</text>
  <text x="318" y="181" text-anchor="middle" font-size="8" fill="#5D7789" font-family="system-ui">model eşiği + iş modeli = TAKEOFF</text>

  <rect x="486" y="60" width="150" height="34" rx="8" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.3"/>
  <text x="561" y="81" text-anchor="middle" font-size="9.5" font-weight="700" fill="#4F46E5" font-family="system-ui">sonraki modeller → %90+</text>

  <rect x="90" y="332" width="480" height="26" rx="8" fill="#1C3247"/>
  <text x="330" y="350" text-anchor="middle" font-size="10" font-weight="700" fill="#FFFFFF" font-family="system-ui">Ürünü bugünkü sınıra göre küçültme — bir sonraki eşiğe göre tasarla</text>
</svg>
```

### 2. Ders — Primitif minimalizmi (read + edit + bash)
Ramp'ten Austin Ray'in ilk beş dakikada aldığı "ürperti"nin sebebi tek cümlede:

> "Eğer **okuyabiliyor, düzenleyebiliyor ve bash çalıştırabiliyorsa**, her şeyi yapabilir. Bu
> primitifler, geri kalan her şeyi inşa etmek için gereken tek şeydi."

Buradaki mühendislik dersi **azlıktır**: güçlü bir ajan, yüzlerce özel araçla değil, **birkaç
kompoze edilebilir primitifle** kurulur. (**Primitif** = daha küçük parçaya bölünemeyen temel
yapı taşı; **kompozisyon** = bu taşları birleştirerek karmaşık işi kurmak. Üç primitif: dosyayı
*oku*, dosyayı *düzenle*, komut *çalıştır* — gerisi bunların birleşimidir.) Dawn Drain'in yıllarca
clide'a **diff** (iki metin arasındaki farkı gösteren biçim) yazdırmaya uğraşması, sonra bash
aracının gelmesiyle işin açılması bunu doğrular — clide'ın "bebek-ajan" kalmasının sebebi
bash primitifinin yokluğuydu (Adam Wolff). Bu, [`harness-engineering-openai.md`](harness-engineering-openai.md)'nin
harness tasarımıyla ve senin projendeki **bilinçli araç azlığıyla** aynı ilkedir: bu projede MCP
üzerinden yalnızca 8 araç açıldı, çünkü her araç tanımı modelin bağlamında yer kaplar — buna
**"bağlam vergisi"** (context tax) diyoruz. Her yeni araç, asıl işe kalan yeri azaltır; primitif
kompozisyonunu tercih et.

### 3. Ders — Ürünün biçimini (form factor) yetenek eşiği açığa çıkarır (+ iş modeli)
**Form factor** = ürünün aldığı biçim: terminalde çalışan bir komut mu, tarayıcıda bir site mi,
editörün içinde bir panel mi. **Yetenek eşiği** = modelin, o biçimi kullanışlı kılacak seviyeye
ulaştığı an. Dawn Drain'in en çarpıcı (ve satıcı anlatısına ters) cümlesi:

> "Aslında Claude Code'un clide'a **o kadar da borçlu olduğunu düşünmüyorum**. Model yetenek eşiğini
> bir kez geçtiğinde, form factor kendini **kendiliğinden açığa çıkarır**."

Yani "doğru ürün fikri"ni bulmak sandığın kadar kahramanca olmayabilir; yeterli model gelince form
zaten belli olur. Boris bunu iş tarafıyla tamamlar: **takeoff**'u (havalanma — ürünün kullanımının
birden hızla artmaya başladığı an) sağlayan **iki** şey vardı — "bir **iş
modeli inovasyonu** (abonelik) ile bir **model inovasyonu** (Claude 4) **birlikte**." Meaghan Choi da
UX açısından aynısını söyler: "Claude 4 gelene kadar yapabileceğimiz pek UX tasarımı yoktu; model
istediğimiz ürün için hazır değildi." Ders: bir AI ürününün başarısı çoğu zaman **tek bir dahi fikir**
değil, **model eşiği + dağıtım/iş modeli** kesişimidir — ve zamanlaması senin kontrolünde değildir.

### 4. ⚠️ ZORUNLU ŞERH — bu bir köken mitidir
Bu metin [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md)'nun refleksini ister, ama **yeni bir
biçimde**. Önceki satıcı şerhleri ("in our testing", `model-ve-caba` §5) ölçülebilir bir **iddiayı**
işaretliyordu; buradaki yanlılık daha **sinsidir**, çünkü ölçülecek bir sayı yok — anlatının kendisi
taraf:

- **Kaçınılmazlık yanılsaması (narrative determinism).** Geriye dönüp anlatılan hikâye
  (retrospektif anlatı), olayları temiz bir nedensellik zincirine dizer: "gördük, birleştirdik,
  patladı." Oysa yol boyunca sayısız çıkmaz, şans ve iptal edilmiş yön vardı; bunlar ya kısaca
  geçilir ya hiç anılmaz. Ders "işe yaradı" anlatısından çıkarılır; **işe yaramayanlar sayılmaz**
  — buna **hayatta kalanların yanlılığı** (survivorship bias) denir: başarısız denemeler
  anlatılmadığı için, başarı oranı olduğundan yüksek görünür.
- **Kahraman anları seçilir.** Mutfakta dans, ASCII logo, "iki-üç like", "88 commit" — bunlar
  ilham verir ama **kanıt değildir**; markanın istediği duyguyu üretmek için seçilmiş sahnelerdir.
- **En güvenilir kısım, satıcıyı en çok kesen kısımdır.** İçerideki **muhalefet** en değerli
  veridir: Dawn Drain "Claude Code clide'a o kadar borçlu değil" der (kendi ürünlerinin efsanesini
  küçültür); Tristan Hume, Claude'un sıfırdan yazdığı Jupyter alternatifini "işe yarıyordu ama
  **kullanmayı sevmedim** — *tada* sahip bir Claude bekliyorum" der. Bir marka parçasında, markanın
  lehine olmayan cümlelere daha çok güven — onlar süzgeçten *rağmen* geçmiştir.

> **Pratik sonuç:** Köken mitinden **mekanizmayı** al (bir sonraki model için inşa et, primitif
> minimalizmi, eşik+iş modeli), **kaçınılmazlık hissini** bırak. "Onlar başardı, demek ki bu yol
> garanti" çıkarımı, tam da anlatının üretmek istediği ve **desteklemediği** çıkarımdır.

### 5. Bu projedeki bağ
- **"Bir sonraki model için inşa et":** AI Öğretmen'i bugünün modelinin sınırına göre değil, bir üst
  eşiğe göre tasarla; ADR-0003 model kademelemesi zaten bu esnekliği kuruyor — Sokratik tartışma
  bugün orta çalışıyorsa, bu "yanlış özellik" değil "erken özellik" olabilir.
- **Primitif minimalizmi:** 8 MCP tool'u bilinçli az tuttun (context tax) — bu, Ray'in "read+edit+bash"
  dersinin senin yüzeyindeki karşılığı. Yeni araç eklemeden önce sor: bu bir primitif mi, yoksa var
  olanların kompozisyonu mu?
- **Tek kişilik ekip + dogfooding** (kendi ürününü her gün kendin kullanmak; "kendi mamanı ye"
  deyiminden)**:** Boris'in "takımı küçük tutmak bizi Claude'u daha çok kullanmaya
  zorladı, aşırı-mühendislikten kaçındırdı" dersi senin durumunun birebir aynısı — kısıt, disiplini
  zorluyor. Bu, Hafta 12'nin liderlik/kaynak-yönetimi temasına bağlanır.

## Öz-sınav (kendini test et)
1. "Bir sonraki model için inşa et" ne demek? %20-30 çalışan bir şeyi yayınlamak neden mantıklıdır,
   ve bu [`model-ve-caba.md`](model-ve-caba.md)'nin "model donmuş" dersinin **zaman ekseni** neden olur?
2. clide "zamanının ilerisinde ama kullanışsız"dı. Bu, "yanlış ürün" mü yoksa "erken ürün" mü — ayrımı
   yapan **karar sorusu** nedir ("bugün kötü çünkü ... mi henüz gelmedi")?
3. Austin Ray'in "read + edit + bash → her şeyi yapabilir" cümlesindeki mühendislik dersi nedir? Senin
   projendeki **8 tool bilinçli az** kararı (MCP context tax) bu derse nasıl bağlanır?
4. clide neden "bebek-ajan" kaldı (hangi primitifin yokluğu)? "Az primitif, çok kompozisyon" ilkesini
   [`harness-engineering-openai.md`](harness-engineering-openai.md) harness tasarımına bağla.
5. Dawn Drain "Claude Code clide'a o kadar borçlu değil, model eşiği geçilince form kendini açığa
   çıkarır" diyor. Bu cümle neden aynı zamanda §4'ün en **güvenilir** kanıtlarından biridir (satıcıyı
   kesen ifade)?
6. Boris'in "iş modeli inovasyonu + model inovasyonu birlikte" tespiti: bir AI ürününün takeoff'u neden
   tek bir dahi fikir değildir? Zamanlamanın senin kontrolünde olmaması bunu nasıl etkiler?
7. **Köken miti** ile "in our testing" (`model-ve-caba` §5) şerhi arasındaki fark nedir? Buradaki
   yanlılık neden daha **sinsidir** (ölçülecek sayı yokken)?
8. "Kaçınılmazlık yanılsaması" ve "survivorship bias" bu metinde nasıl işler? Bir marka parçasında
   neden **markanın lehine olmayan** cümlelere (Drain, Hume) daha çok güvenilir?
9. Köken mitinden **alınacak** üç şey ve **bırakılacak** bir şey nedir? "Onlar başardı, demek ki bu yol
   garanti" çıkarımı neden tam da anlatının desteklemediği çıkarımdır?

## Kaynaklar
- **Anthropic — "The Making of Claude Code"** (sözlü tarih, first-party marka anlatısı; 2026). Katkı
  verenler: Ben Mann, Boris Cherny, Dawn Drain, Shauna Kravec, Cat Wu, Meaghan Choi, Sid Bidasaria,
  Adam Wolff, Igor Kofman ve dış kullanıcılar (Austin Ray/Ramp, Jarred Sumner/Bun, Kyle Easterly,
  Tristan Hume) — *şerh: köken miti; mekanizmayı al, kaçınılmazlık hissini bırak (§4). Mimari-ilgili
  aktarılabilir ders §1–§3'te; gerisi ilham veren ama aktarılamaz tarih.*

### Bu programdaki ilgili kaynaklar
- [`model-ve-caba.md`](model-ve-caba.md) — "model donmuş"; bu metnin "bir sonraki model için inşa et"
  dersi onun zaman eksenidir.
- [`harness-engineering-openai.md`](harness-engineering-openai.md) — harness/primitif tasarımı; §2'nin
  "az primitif, çok kompozisyon" dersinin zemini.
- [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md) — satıcı-anlatısı okuma refleksi; §4 şerhin kökü.
