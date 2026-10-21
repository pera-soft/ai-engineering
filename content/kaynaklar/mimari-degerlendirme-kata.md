---
baslik: "Mimari Değerlendirme ve Kata Pratiği — Kurmak Yetmez, Sorgulamayı Öğren"
yazar: "iSAQB · SEI/CMU (ATAM · QAW) · Neal Ford / O'Reilly Katas — Türkçe çalışma metni"
tarih: "2026-07-19"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 5,6,10,12
---

### Bu metin ne anlatıyor?
Bir yazılımın büyük yapısını tasarlamayı öğrenmek işin yarısıdır. Diğer yarısı, tasarladığın
şeyi başkası "neden böyle yaptın?" diye sorduğunda savunabilmek ve başkasının tasarımını
sistemli biçimde sorgulayabilmektir. Bu metin işte o ikinci yarıyı anlatıyor: dünyada bu
beceriyi ölçmek için kullanılan iki yöntem (ATAM ve QAW), bir alıştırma biçimi (kata) ve
bunların tek başına, yapay zekâ yardımıyla nasıl çalışılabileceği. Sonunda somut bir
protokol var: iki haftada bir tekrarlanan beş adımlı bir alıştırma.

### Bu metinde geçen temel terimler ve kısaltmalar
- **Mimari** (architecture): Bir yazılımın büyük parçaları, aralarındaki ilişkiler ve
  geri dönmesi pahalı olan kararlar.
- **Kalite niteliği** (quality attribute): Sistemin "ne yaptığı" değil, "nasıl yaptığı" —
  hızlı mı, güvenli mi, ayakta mı kalıyor, değiştirmesi kolay mı.
- **Senaryo** (scenario): Bir kalite niteliğinin ölçülebilir hâli. "Hızlı olsun" bir dilek;
  "pazartesi 09:00'da 1000 eşzamanlı istekte yanıtların %95'i 1 saniyenin altında kalsın"
  bir senaryodur.
- **Ödünleşim / takas** (trade-off): Bir şeyi kazanmak için başkasından vazgeçmek.
- **ATAM** (Architecture Tradeoff Analysis Method): Var olan bir mimariyi senaryolarla
  sorgulayıp risklerini çıkaran yöntem.
- **QAW** (Quality Attribute Workshop): Mimari daha yokken kalite senaryolarını yazdıran
  yöntem.
- **SEI / CMU**: Software Engineering Institute — Carnegie Mellon Üniversitesi'ne bağlı,
  ATAM ve QAW'ı geliştiren araştırma kurumu.
- **iSAQB**: Avrupa merkezli yazılım mimarlığı sertifika kurumu. **CPSA-F** başlangıç,
  **CPSA-A** ileri seviye sertifikası; **ARCEVAL** ve **ADOC** bu kurumun ileri seviye ders
  modülleridir (sırasıyla değerlendirme ve dokümantasyon).
- **Kata**: Dövüş sanatlarından ödünç alınmış bir kelime — aynı hareket dizisini tekrar
  tekrar çalışmak. Burada: aynı tasarım-savunma döngüsünü periyodik tekrarlamak.
- **RFP** (Request for Proposal, teklif çağrısı): Müşterinin "şöyle bir sistem istiyorum"
  diye yazdığı, çoğu zaman eksik olan ilk metin.
- **ADR** (Architecture Decision Record, mimari karar kaydı): Verilen bir kararı, gerekçesini
  ve vazgeçilen alternatifleri yazan kısa belge.
- **C4**: Mimariyi dört farklı yakınlıkta (bağlam, konteyner, bileşen, kod) çizmenin yaygın
  bir yöntemi.
- **H5, H6, H12…**: Bu programın hafta numaraları (Hafta 5, Hafta 6, Hafta 12).
- **LLM** (Large Language Model, büyük dil modeli): Bu metinde "AI öğretmen" derken kastedilen
  teknoloji.

## Neden bu özet
Program mimariyi **kurmayı** öğretiyor: Hafta 5-6 kalite nitelikleri ve trade-off analizi,
Hafta 9 hexagonal mimari. Bu kaynak eksik kalan ikinci beceriyi ekler: **sorgulamayı.** Yani
mevcut bir mimariyi sistematik biçimde değerlendirmek (ATAM/QAW), kendi tasarımını çapraz sorgu
altında **savunmak** (design defense) ve bunu tek seferlik bir sınav değil, **tekrarlanan kata
pratiğiyle** kas hâline getirmek.

Bunlar "aynı işin devamı" değil, dünya standartlarında **ayrı beceri alanları** sayılıyor:
iSAQB'nin Advanced programında mimari **değerlendirme** (ARCEVAL) ve mimari **dokümantasyon**
(ADOC) ayrı resmî modüllerdir; CPSA-A sınavı ise çoktan seçmeli bir test değil, ~40 çalışma
saatlik bir vaka çözümü **artı sözlü savunmadır.** Kurabilen ama savunamayan mimar, dünyanın
ölçtüğü çıtanın yarısındadır.

> **Tek cümle: Mimarlık iki yönlü bir beceridir — kurarken karar verirsin, sorgularken kararın
> hesabını verirsin. Program birincisini verdi; bu kaynak ikincisini ekler ve ikisini 2 haftada
> bir dönen bir döngüye bağlar.**

Üç diyagram fikri kısaca gösterir; ayrıntı metindedir.

## Türkçe çalışma metni

### 1. Dünya nasıl ölçüyor — çıta
Önce çıtayı görelim. Avrupa'nın yazılım mimarlığı sertifika standardı **iSAQB CPSA-A**
(Certified Professional for Software Architecture — Advanced) sınavı şöyle çalışır:

- Sınav **çoktan seçmeli DEĞİLDİR.** Aday, üç vaka tipinden birini seçer (bilgi sistemi, web
  sistemi ya da gömülü sistem) ve **yaklaşık 40 çalışma saati** süren **yazılı bir vaka çözümü**
  üretir: gereksinim analizi, mimari tasarım, kararlar, dokümantasyon.
- Çözümü **iki bağımsız denetçi** (examiner) değerlendirir.
- Sonra **telefonla sözlü savunma** gelir. iSAQB'nin kendi ifadesiyle: *"The examiners will then
  talk to you... you have to explain and defend your solution."* — denetçiler seninle konuşur;
  çözümünü **açıklamak ve savunmak** zorundasın.
- **Dokümantasyon kalitesi açık bir not kriteridir** — "kafamda vardı" geçmez; yazmadıysan yok.

İki ayrıntı daha, bu kaynağın tezini doğrudan destekler:

- iSAQB Advanced'ın modül kataloğunda **ARCEVAL** (Architecture Evaluation — mimari
  değerlendirme) ve **ADOC** (Architecture Documentation) **ayrı resmî modüllerdir.** Yani
  standardı koyan kurum, "mimari kurmak", "mimariyi değerlendirmek" ve "mimariyi dokümante
  etmek" işlerini bağımsız beceri alanları olarak ayırmış durumda. "Kurmayı öğrendim,
  değerlendirmeyi de biliyorumdur" varsayımını standart kendisi reddediyor.
- Foundation seviyesinde çözümlü ve çözümsüz **deneme sınavları (mock exams)** resmî olarak
  yayımlanır — geri getirme pratiği (retrieval practice) sistemin gömülü bir mekaniğidir:
  öğrenmek yetmez, bildiğini sınav koşulunda **geri çağırmak** ayrıca çalışılır.

> **Ders: Dünya, mimarı "ne biliyor" ile ölçmüyor. "Kurduğunu savunabiliyor mu ve başkasının
> kurduğunu sistematik sorgulayabiliyor mu" ile ölçüyor.** Bu kaynağın geri kalanı, bu iki
> beceriyi solo çalışılabilir hâle getirir.

### 2. ATAM — mevcut mimariyi sistematik sorgulamak
**ATAM** (Architecture Tradeoff Analysis Method), SEI/CMU'nun (Software Engineering Institute,
Carnegie Mellon) resmî mimari değerlendirme yöntemidir. Ne yapar? Bir mimariyi **kalite niteliği
hedeflerine göre** değerlendirir ve iş hedeflerini tehdit eden **riskleri açığa çıkarır.**
"Beğendim / beğenmedim" değil; senaryo tabanlı, ölçülebilir bir sorgudur.

**9 adım** (özetle):

1. Yöntemin sunumu (herkes aynı usulü bilsin)
2. **İş sürücülerinin** (business drivers) sunumu — mimari neye hizmet ediyor?
3. **Mimarinin** sunumu
4. Mimari **yaklaşımların** (approaches) tespiti
5. **Kalite niteliği fayda ağacı** (utility tree) üretimi — kök "fayda", dallar nitelikler,
   yapraklar **senaryolar**: uyaran (stimulus) + yanıt (response) + öncelik
6. Mimari yaklaşımların bu senaryolara karşı **analizi**
7. Paydaşlarla senaryo **beyin fırtınası** ve **oylamayla önceliklendirme**
8. Yeni senaryoların **test vakası olarak** yeniden analizi
9. Bulguların sunumu

**Somut çıktılar** — ATAM'ın gücü burada: fayda ağacı · önceliklendirilmiş senaryolar ·
**riskler VE risk-olmayanlar** (non-risks: "bunu inceledik, sorun değil" kaydı da değerlidir) ·
**duyarlılık noktaları** (sensitivity points: tek bir kararın bir niteliği güçlü etkilediği
yerler) · **trade-off noktaları** (aynı kararın bir niteliği iyileştirip diğerini bozduğu
yerler) · iş hedeflerini tehdit eden **risk temaları.** Değerlendirme, "bence iyi olmuş" cümlesi
değil, **ölçülebilir artefakt** üretir.

İki çıktı türü özellikle H5'in trade-off diliyle örtüşür ve karıştırılmamalıdır:

- **Duyarlılık noktası:** tek bir mimari kararın **tek bir** kalite niteliğini güçlü etkilediği
  yer. "Önbellek süresi uzarsa performans belirgin değişir" — bir nitelik, bir kaldıraç.
- **Trade-off noktası:** aynı kararın **birden fazla** niteliği zıt yönde etkilediği yer.
  "Önbellek süresi uzarsa performans iyileşir ama verinin tazeliği (doğruluk) bozulur" —
  H5'te yazdığın trade-off cümlelerinin değerlendirme sırasında yeniden bulunmuş hâli.

**Hafta 5-6 köprüsü:** fayda ağacı, FoSA'nın "mimari karakteristikler" kavramının operasyonel
hâlidir. H5'te "performans önemli" demeyi öğrendin; fayda ağacı seni bir adım ileri zorlar:
*hangi uyaran, hangi yanıt hedefi, hangi öncelik?* "Performans" bir kelimedir; "tepe saatte
1000 eşzamanlı istek → p95 < 1 sn, öncelik yüksek" bir **senaryodur** — ve yalnızca senaryo
test edilebilir. (**p95**: isteklerin en yavaş %5'i dışarıda bırakıldığında kalan gecikme
tavanı. Ortalama yanıltır; kullanıcının "yavaş" dediği yer bu uç değerdir.)

### 3. QAW — mimari YOKKEN kalite senaryoları
**QAW** (Quality Attribute Workshop), yine SEI'nin yöntemidir ve ATAM'ın **tamamlayıcısıdır**:
ATAM var olan mimariyi sorgular; QAW ise mimari **ortaya çıkmadan ÖNCE**, iş/misyon
hedeflerinden mimari-kritik kalite niteliklerini **senaryolaştırır.**

**8 adım:** yöntemin sunumu → iş sunumu → mimari **plan** sunumu (henüz mimari yok, plan var)
→ **mimari sürücülerin** (architectural drivers) tespiti → senaryo **beyin fırtınası** →
senaryoların **birleştirilmesi** → **oylamayla önceliklendirme** → **ilk 4-5 senaryonun** iş
hedefleri ve kalite nitelikleriyle **rafine edilmesi.**

İkili ders şudur:

> **QAW kurarken (önce), ATAM sorgularken (sonra).** Gereksinim çıkarma (elicitation) ile
> değerlendirme (evaluation) ayrı ama birbirini besleyen pratiklerdir: QAW'da yazdığın
> senaryolar, ATAM'da mimariyi sınayan test vakaları olur.

Bu, kod dünyasındaki bir refleksin mimari karşılığıdır: testi önce yaz, kodu ona uydur;
senaryoyu önce yaz, mimariyi ona uydur — sonra aynı senaryoyla geri dön ve sorgula.

Dikkat: QAW'ın çıktısı da ATAM'ınki gibi **oylamayla önceliklendirilir.** Bu ayrıntı önemsiz
değildir — her senaryo eşit değildir ve mimari, en öncelikli 4-5 senaryoya göre şekillenir.
Solo çalışırken "oylama"nın karşılığı, senaryolarını iş değeri ve teknik risk eksenlerinde
açıkça sıralamak ve bu sırayı **yazmaktır** (Diyagram 2'deki H/M etiketleri).

## Diyagram 1 — Kur → Savun → Sorgula döngüsü
QAW senaryoları önce; tasarım (C4 + ADR) sonra; savunma (çapraz sorgu) ve ATAM-lite değerlendirme
en sonda — ve döngü başa döner. **Tek atış değil, 2 haftada bir.**

```svg
<svg viewBox="0 0 660 344" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kur savun sorgula döngüsü: birinci adım QAW ile kalite senaryolarının mimariden önce yazılması, ikinci adım C4 diyagramları ve ADR kararlarıyla tasarım, üçüncü adım tasarımın çapraz sorguyla savunulması, dördüncü adım ATAM-lite ile fayda ağacı ve risklerin çıkarılması; dördüncü adımdan tekrar birinci adıma dönen ok döngünün iki haftada bir tekrarlanan bir seri olduğunu, tek atışlık bir sınav olmadığını gösterir">
  <defs>
    <marker id="mdk-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="mdk-1b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kur → Savun → Sorgula — döngü, sınav değil</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">QAW kurarken (önce) · ATAM sorgularken (sonra)</text>

  <rect x="30" y="66" width="270" height="76" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="165" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1 · QAW-lite — senaryolar (ÖNCE)</text>
  <text x="165" y="108" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">müşteriye 5-8 soru sor;</text>
  <text x="165" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ilk 4-5 senaryo: uyaran → yanıt + öncelik</text>

  <rect x="360" y="66" width="270" height="76" rx="11" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="495" y="90" text-anchor="middle" font-size="12" font-weight="700" fill="#4F46E5" font-family="system-ui">2 · Tasarla — C4 + ADR</text>
  <text x="495" y="108" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Context + Container (H6) · karar özeti;</text>
  <text x="495" y="124" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her büyük karara trade-off cümlesi (H5)</text>

  <rect x="360" y="196" width="270" height="76" rx="11" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.6"/>
  <text x="495" y="220" text-anchor="middle" font-size="12" font-weight="700" fill="#C77A17" font-family="system-ui">3 · Savun — çapraz sorgu</text>
  <text x="495" y="238" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"düşüncemdeki delikleri ara" — en az 5 soru;</text>
  <text x="495" y="254" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">cevaplanamayan her soru = bir risk kaydı</text>

  <rect x="30" y="196" width="270" height="76" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="165" y="220" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">4 · ATAM-lite — değerlendir</text>
  <text x="165" y="238" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">fayda ağacı · riskler / risk-olmayanlar</text>
  <text x="165" y="254" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">duyarlılık + trade-off noktaları → ADR'ye</text>

  <line x1="302" y1="104" x2="358" y2="104" stroke="#186399" stroke-width="2" marker-end="url(#mdk-1a)"/>
  <line x1="495" y1="144" x2="495" y2="194" stroke="#186399" stroke-width="2" marker-end="url(#mdk-1a)"/>
  <line x1="358" y1="234" x2="302" y2="234" stroke="#186399" stroke-width="2" marker-end="url(#mdk-1a)"/>
  <line x1="165" y1="194" x2="165" y2="144" stroke="#25804C" stroke-width="2.2" stroke-dasharray="6 4" marker-end="url(#mdk-1b)"/>
  <text x="158" y="172" text-anchor="end" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">sonraki kata</text>

  <rect x="150" y="292" width="360" height="34" rx="9" fill="#1C3247"/>
  <text x="330" y="314" text-anchor="middle" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">Tek atış değil — 2 haftada bir dönen SERİ</text>
</svg>
```

### 4. Kata — tek atış değil, SERİ; ve dürüst bir sınır
Bu döngüyü kas yapan format **mimari kata**'dır. Neal Ford formatının çekirdeği şudur:

- Takımlar **yabancılardan** kurulur — kural açıktır: *"Co-workers may not be in a group
  together"* (iş arkadaşları aynı grupta olamaz). Amaç, hazır ekip dinamiği olmadan sıfırdan
  müzakere etmek.
- **Moderatör, müşteri/paydaş rolü oynar.** RFP (teklif çağrısı) kasıtlı olarak eksiktir;
  takımlar gereksinimi netleştirmek için Moderatör'e **soru sorar.** Gereksinim çıkarma
  (requirements elicitation) kata'nın parçasıdır — mimarın işi hazır gereksinimle başlamaz.
- İkinci faz **savunmadır**: takım tasarımını sunar; diğer takımlar *"düşüncendeki delikleri
  arayan"* sorular sorar. Format bu sorgunun **kişisel eleştiri olmadığını** açıkça kurala
  bağlar: hedef tasarımdaki boşluk, tasarımcı değil. Bu, CPSA-A'daki sözlü savunmanın
  antrenman hâlidir — savunulmamış tasarım, incelenmemiş tasarımdır.

O'Reilly'nin **Architectural Katas** etkinliği bu formatı yarışmaya çevirir: 3-5 kişilik
takımlar, **çok turlu** yapı (açılış → yarı final → final), turlar arasında **jüri geri
beslemesi** (ev sahipleri Neal Ford ve Mark Richards; baş jüri Jacqui Read), finalde **sunum +
izleyici oyu.** Ve en önemlisi: bu bir **seridir.** 2020-2025 arasında en az **9 döngü**
arşivlenmiştir (GitHub'daki TheKataLog deposu) — "AI-Enabled Architecture" temalı döngüler
dahil. Katılımcılar aynı kası tekrar tekrar çalıştırır.

Neden "seri" olması bu kadar önemli? Çünkü kata kelimesinin geldiği yer (dövüş sanatlarındaki
form tekrarı) tam bunu anlatır: form **bir kez** yapılınca öğrenilmiş sayılmaz. İlk kata'nda
fayda ağacın eksik, trade-off cümlelerin bulanık olur — bu normaldir. Beşinci kata'nda aynı
adımlar refleks hâline gelir. Tek seferlik bir capstone, ne kadar iyi olursa olsun, refleks
üretmez; yalnızca fotoğraf çeker.

> **Bizde 1 capstone kata var; dünyada kata tekrarlanan bir kas.** Programın H12 capstone'u
> doğru bir final, ama tek başına bir "seri" değil. §5'teki protokol bu açığı kapatır.

⚠️ **Dürüst sınır — bunu bilmeden devam etme.** Kata'nın çekirdek değeri **takım
tartışmasıdır**: yabancılarla müzakere, rol dağılımı, grup içinde uzlaşıp grup olarak savunma.
**Bu, solo+AI formatında UYARLANAMAZ.** Uyarlanabilenler şunlardır: Moderatör'ün müşteri rolü
(AI oynayabilir), savunma çapraz sorgusu (AI sorabilir), jüri **rubriği** (değerlendirme
ölçütlerinin puan puan yazıldığı tablo — AI uygulayabilir).
Ama taklit ettiğin şeyin ne **olmadığını** bil: AI'ın sorgusu, üç farklı insanın üç farklı
sezgisinin, itirazının ve kör nokta çeşitliliğinin yerini tutmaz. Solo kata gerçek kata'nın
**antrenman simülatörüdür**, kendisi değil.

### 5. Solo kata protokolü — bu programda nasıl çalışır
Beş adım; hepsi mevcut AI öğretmenle çalışır (Sokratik sohbet + değerlendirme rubriği yeterli —
**yeni kod gerekmez**):

1. **RFP al.** AI'dan bir kata senaryosu iste (ya da aşağıdaki bankadan seç). AI =
   Moderatör/müşteri. RFP kasıtlı eksik olacak; bu bir hata değil, kuraldır.
2. **Gereksinim sorgusu (QAW-lite).** Müşteriye **5-8 soru** sor; cevaplarla **ilk 4-5 kalite
   senaryosunu** uyaran → yanıt biçiminde yaz ve önceliklendir.
3. **Tasarla.** C4 Context + Container diyagramı (H6) + **1 sayfalık karar özeti**; her büyük
   karara bir **trade-off cümlesi** (H5): "X'i seçtim; Y kazandım, Z'den vazgeçtim, çünkü…"
4. **Savun.** AI'a de ki: *"şimdi düşmanca sorgula — düşüncemdeki delikleri ara."* En az 5 soru
   iste. **Cevaplayamadığın her soru bir risk kaydıdır** — utanç değil, envanter.
5. **Değerlendir (ATAM-lite).** Fayda ağacını çıkar; riskler / risk-olmayanlar / duyarlılık
   noktaları / trade-off noktalarını listele. Kayıt: **ADR'ye** (H2 pratiği).

**Kata bankası** — 5 mini brief, **2 haftada bir** (H4 / H6 / H8 / H10 sonu + H12 capstone).
Her biri kasıtlı olarak farklı bir kalite eksenini zorlar; böylece beş kata'da beş farklı
fayda ağacı çıkarmış olursun:

- **(a) İlçe belediyesi randevu sistemi** — yoğun saat trafiği: sabah açılışında yığılan
  istekler; performans + erişilebilirlik ekseni (Diyagram 2'nin örneği budur).
- **(b) E-ticaret iade/İYS entegrasyonu** — dış sözleşmeler: senin kontrolünde olmayan
  API'lerle yaşamak; entegrasyon sınırları ve hata toleransı ekseni.
- **(c) Hastane tahlil sonuç bildirimi** — KVKK + erişilebilirlik: kişisel sağlık verisi ve
  "sonuç zamanında ulaşmalı" baskısının çatıştığı yer; güvenlik/mahremiyet ekseni.
- **(d) Canlı skor platformu** — yayılım/ölçek: bir gol anında milyonlarca istemciye aynı
  saniyede veri; ölçeklenme ve dağıtım ekseni.
- **(e) AI destek botu olan SaaS yardım masası** — H7-8 bilgisiyle: token maliyeti, eval,
  guardrail; olasılıksal bileşeni klasik mimariye yerleştirme ekseni.

Her biri 3-4 cümlelik bir RFP'dir; detayını Moderatör'e (AI) **sorarsın** — detay verilmez,
çıkarılır. Gereksinimi sana teslim eden bir kata, kası çalıştırmayan bir kata'dır.

**Capstone yükseltmesi (CPSA-A tarzı):** H12 kata'sının teslimatı = **yazılı mimari doküman**
(C4 + kararlar + fayda ağacı + riskler) **+ savunma oturumu.** Yani §1'deki çıtanın küçültülmüş
kopyası: **dokümante ettin, açıkladın, savundun** — üçü birden. iSAQB'nin dokümantasyon
kalitesini açık not kriteri yapmasının nedeni burada görünür olur: savunma sırasında elindeki
tek dayanak yazdıklarındır. Yazmadıysan yok; savunmadıysan bitmedi.

## Diyagram 2 — Fayda ağacı (utility tree) örneği: randevu sistemi
Kök "Kalite", dallar nitelikler, yapraklar test edilebilir senaryolar. Öncelik etiketi
(iş değeri, teknik risk): H = yüksek, M = orta.

```svg
<svg viewBox="0 0 660 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Belediye randevu sistemi için kalite niteliği fayda ağacı örneği: kökteki kalite düğümünden dört dal çıkar; performans dalının yaprağı pazartesi 09:00 tepe yükünde bin eşzamanlı istekte p95 yanıt süresinin bir saniyenin altında kalması yüksek öncelikli senaryo; erişilebilirlik dalının yaprağı mesai saatinde sunucu çökmesinde beş dakika içinde otomatik kurtarma; güvenlik dalının yaprağı yetkisiz erişim denemesinde KVKK gereği reddetme ve kayıt tutma; değiştirilebilirlik dalının yaprağı yeni ilçe veya hizmet türünün kod değişikliği olmadan yapılandırmayla eklenmesi; her yaprakta uyarandan yanıta giden senaryo ve iş değeri ile teknik risk öncelik çifti bulunur">
  <defs>
    <marker id="mdk-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Fayda ağacı — "performans önemli" değil, senaryo</text>
  <text x="330" y="44" text-anchor="middle" font-size="12" fill="#5D7789" font-family="system-ui">yaprak = uyaran → yanıt + (iş değeri, teknik risk) önceliği</text>

  <rect x="16" y="184" width="96" height="56" rx="10" fill="#1C3247"/>
  <text x="64" y="208" text-anchor="middle" font-size="12" font-weight="700" fill="#FFFFFF" font-family="system-ui">Kalite</text>
  <text x="64" y="226" text-anchor="middle" font-size="9" fill="#EAF1F7" font-family="system-ui">randevu sistemi</text>

  <rect x="152" y="62" width="150" height="40" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="227" y="87" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Performans</text>
  <rect x="152" y="152" width="150" height="40" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="227" y="177" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Erişilebilirlik</text>
  <rect x="152" y="242" width="150" height="40" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="227" y="267" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">Güvenlik</text>
  <rect x="152" y="332" width="150" height="40" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="227" y="357" text-anchor="middle" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Değiştirilebilirlik</text>

  <line x1="112" y1="196" x2="150" y2="86" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mdk-2a)"/>
  <line x1="112" y1="206" x2="150" y2="172" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mdk-2a)"/>
  <line x1="112" y1="218" x2="150" y2="258" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mdk-2a)"/>
  <line x1="112" y1="228" x2="150" y2="348" stroke="#5D7789" stroke-width="1.6" marker-end="url(#mdk-2a)"/>

  <rect x="342" y="58" width="302" height="52" rx="9" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="493" y="78" text-anchor="middle" font-size="9.8" fill="#1C3247" font-family="system-ui">Pazartesi 09:00 tepe yükü: 1000 eşzamanlı istek →</text>
  <text x="493" y="94" text-anchor="middle" font-size="9.8" font-weight="700" fill="#186399" font-family="system-ui">p95 yanıt &lt; 1 sn · öncelik (H, H)</text>
  <rect x="342" y="148" width="302" height="52" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="493" y="168" text-anchor="middle" font-size="9.8" fill="#1C3247" font-family="system-ui">Mesai saatinde sunucu çöker →</text>
  <text x="493" y="184" text-anchor="middle" font-size="9.8" font-weight="700" fill="#25804C" font-family="system-ui">5 dk içinde otomatik kurtarma · (H, M)</text>
  <rect x="342" y="238" width="302" height="52" rx="9" fill="#FFFFFF" stroke="#C77A17" stroke-width="1.3"/>
  <text x="493" y="258" text-anchor="middle" font-size="9.8" fill="#1C3247" font-family="system-ui">Yetkisiz kişi başkasının randevusunu ister →</text>
  <text x="493" y="274" text-anchor="middle" font-size="9.8" font-weight="700" fill="#C77A17" font-family="system-ui">KVKK: erişim reddi + denetim kaydı · (H, H)</text>
  <rect x="342" y="328" width="302" height="52" rx="9" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1.3"/>
  <text x="493" y="348" text-anchor="middle" font-size="9.8" fill="#1C3247" font-family="system-ui">Yeni ilçe / hizmet türü eklenir →</text>
  <text x="493" y="364" text-anchor="middle" font-size="9.8" font-weight="700" fill="#4F46E5" font-family="system-ui">kod değişikliği yok, yapılandırma · (M, M)</text>

  <line x1="302" y1="82" x2="340" y2="84" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mdk-2a)"/>
  <line x1="302" y1="172" x2="340" y2="174" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mdk-2a)"/>
  <line x1="302" y1="262" x2="340" y2="264" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mdk-2a)"/>
  <line x1="302" y1="352" x2="340" y2="354" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mdk-2a)"/>
</svg>
```

### 6. AI değerlendirici ne kadar iyi? — ölçülü cevap
Solo protokolün kritik varsayımı, AI'ın savunma/değerlendirme partneri olarak işe yaradığıdır.
Bunun için elimizde bir veri noktası var: bir üniversite dersinde yapılan **ilk çalışmada**
(initial study) bir LLM (MS Copilot), öğrencilerin ATAM kalite senaryolarındaki **riskleri,
duyarlılık noktalarını ve trade-off analizini çoğu durumda öğrencilerden daha isabetli** buldu.

⚠️ **Şerh — bu bulguyu büyütme.** Bu bir **ilk çalışmadır**; tek ders, tek model, tek bağlam.
"AI, ATAM'ı kısmen otomatikleştirir" gibi bir genelleme bu kaynaktan **çıkarılamaz** — o genel
sonuç bizim doğrulamamızda ÇÜRÜTÜLDÜ. Doğru okuma daha mütevazıdır: AI, **solo öğrencinin**
savunma ve değerlendirme **pratiği için yeterli bir sparring partneridir** (idman eşi);
sertifika jürisinin ya da deneyimli bir insan değerlendirme ekibinin **yerine geçtiği
kanıtlanmamıştır.** §4'teki dürüst sınırın veri tarafı budur: antrenman simülatörü olarak
yeterli, hakem olarak kanıtsız.

Pratikte bu şu demektir: solo protokolün 4. adımında AI'ın sorduğu beş düşmanca sorunun
**hepsi isabetli olmayabilir** — bazıları senaryonun bağlamını ıskalar. Bu bir kusur değil,
kullanım talimatıdır: AI'ın sorularını da **sorgula**; isabetli olanı risk kaydına çevir,
isabetsiz olanı gerekçesiyle reddet. Reddedebilmek de savunmanın bir parçasıdır — ve
"risk-olmayanlar" listesi tam olarak böyle dolar.

**Sertifika notu (kariyer):** iSAQB CPSA-F/A, dış hedef olarak mevcuttur; bu program sertifika
**vermez.** Ama şunu verir: 2 haftada bir dönen kata'ların arşivi — ADR'ler + savunma
kayıtları — **fiilî bir portfolyodur.** Ve canlı MCP sunucun zaten kendisi bir portfolyo
artefaktıdır: "mimari kurabiliyorum"un kanıtı çalışan sistem, "sorgulayabiliyorum"un kanıtı
kata arşividir.

## Diyagram 3 — Solo uyarlama haritası: ne taşınır, ne taşınmaz
Solda dünya formatı, sağda solo karşılığı. Kırmızı satır uyarlanamaz; yeşiller AI ile taşınır.

```svg
<svg viewBox="0 0 660 384" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Solo uyarlama haritası: dünya kata formatının dört ögesi solda, solo karşılıkları sağda gösterilir; yabancılarla takım tartışması solo formata uyarlanamaz ve kırmızıyla işaretlidir çünkü üç farklı insanın üç farklı sezgisinin yerini yapay zekâ tutmaz; moderatörün müşteri rolünü yapay zekâ oynayabilir, jüri geri beslemesi ve izleyici oyunun yerine yapay zekânın uyguladığı değerlendirme rubriği geçebilir, çok turlu seri formatının yerine iki haftada bir dönen beş senaryoluk kata bankası geçer ve bu üç satır yeşille işaretlidir">
  <defs>
    <marker id="mdk-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
    <marker id="mdk-3b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Solo uyarlama — taklit ettiğinin ne OLMADIĞINI bil</text>
  <text x="168" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">DÜNYA FORMATI</text>
  <text x="492" y="52" text-anchor="middle" font-size="11.5" font-weight="700" fill="#5D7789" font-family="system-ui">SOLO KARŞILIĞI</text>

  <rect x="16" y="64" width="304" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="168" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Takım tartışması</text>
  <text x="168" y="106" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">yabancılarla müzakere · rol dağılımı · grup savunması</text>
  <rect x="340" y="64" width="304" height="62" rx="10" fill="#FFFFFF" stroke="#B3402F" stroke-width="2"/>
  <text x="492" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#B3402F" font-family="system-ui">UYARLANAMAZ</text>
  <text x="492" y="106" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">AI, üç insanın üç farklı sezgisinin yerini tutmaz</text>
  <line x1="322" y1="95" x2="338" y2="95" stroke="#B3402F" stroke-width="2" marker-end="url(#mdk-3b)"/>

  <rect x="16" y="140" width="304" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="168" y="164" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Moderatör = müşteri rolü</text>
  <text x="168" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">RFP eksik; gereksinim soruyla çıkarılır</text>
  <rect x="340" y="140" width="304" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="492" y="164" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">AI müşteri rolü oynar</text>
  <text x="492" y="182" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">QAW-lite: 5-8 soru → 4-5 senaryo</text>
  <line x1="322" y1="171" x2="338" y2="171" stroke="#25804C" stroke-width="2" marker-end="url(#mdk-3a)"/>

  <rect x="16" y="216" width="304" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="168" y="240" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Jüri geri beslemesi + izleyici oyu</text>
  <text x="168" y="258" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">turlar arası jüri notu · finalde sunum + oy</text>
  <rect x="340" y="216" width="304" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="492" y="240" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">AI rubrik uygular</text>
  <text x="492" y="258" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">değerlendirme rubriği + düşmanca çapraz sorgu</text>
  <line x1="322" y1="247" x2="338" y2="247" stroke="#25804C" stroke-width="2" marker-end="url(#mdk-3a)"/>

  <rect x="16" y="292" width="304" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="168" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Çok turlu SERİ</text>
  <text x="168" y="334" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">açılış → yarı final → final · 2020-2025: 9+ döngü</text>
  <rect x="340" y="292" width="304" height="62" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="492" y="316" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kata bankası — 2 haftada bir</text>
  <text x="492" y="334" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">5 mini brief: H4 · H6 · H8 · H10 + H12 capstone</text>
  <line x1="322" y1="323" x2="338" y2="323" stroke="#25804C" stroke-width="2" marker-end="url(#mdk-3a)"/>

  <text x="330" y="376" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Solo kata = antrenman simülatörü; gerçek kata'nın kendisi değil.</text>
</svg>
```

### 7. Kapanış — üç yöntemin tek cümlesi
Toparlayalım: **QAW** senaryoyu mimariden önce yazdırır; **ATAM** aynı senaryoyla mimariyi
sorgular ve riskleri artefakta çevirir; **kata** bu ikisini 2 haftada bir dönen bir antrenmana
bağlar; **savunma** ise hepsinin sınav hâlidir. Dördü birden şu tek cümleye katlanır: *mimari,
kurulduğu gün değil, sorgusuna dayandığı gün biter.* Bir sonraki kata tarihin bellidir —
RFP'yi Moderatör'den iste.

## Öz-sınav (kendini test et)
1. Bu kaynağın merkezî tezini kur: program hangi beceriyi öğretiyor (H5-6, H9), bu kaynak
   hangisini ekliyor? "Kurmak" ile "sorgulamak" neden ayrı beceri alanlarıdır — iSAQB'nin modül
   yapısı (ARCEVAL, ADOC ayrı modüller) bu ayrımı nasıl kanıtlar?
2. CPSA-A sınavının üç bileşenini say (~40 saatlik yazılı vaka · iki bağımsız denetçi · sözlü
   savunma). "The examiners will then talk to you… you have to explain and defend your solution"
   cümlesi, sınavın neyi ölçtüğünü çoktan seçmeli bir testten nasıl farklılaştırır?
3. ATAM'ın 9 adımını kabaca sırala ve **somut çıktılarını** say: fayda ağacı, riskler,
   risk-olmayanlar, duyarlılık noktaları, trade-off noktaları, risk temaları. "Risk-olmayanlar"
   kaydı neden değerlidir — "bence iyi olmuş" cümlesinden farkı ne?
4. Fayda ağacında bir yaprak nasıl yazılır? "Performans önemli" cümlesini, randevu sistemi
   örneğindeki gibi uyaran → yanıt + öncelik biçimine çevir. Bu, H5'teki (FoSA) "mimari
   karakteristikler" kavramının hangi anlamda operasyonel hâlidir?
5. QAW ile ATAM'ın iş bölümünü açıkla: hangisi mimari **yokken**, hangisi mimari **varken**
   çalışır? QAW'ın 8 adımından "ilk 4-5 senaryonun rafine edilmesi" adımı, solo protokolün
   hangi adımına karşılık gelir?
6. Neal Ford kata formatının üç çekirdek kuralını say (yabancılardan takım · Moderatör =
   müşteri · savunma fazı). "Co-workers may not be in a group together" kuralının amacı nedir,
   ve gereksinim çıkarmanın (elicitation) kata'nın **parçası** olması mimarın işine dair ne
   söyler?
7. **Dürüst sınır:** kata'nın hangi çekirdek değeri solo+AI formatına UYARLANAMAZ ve neden?
   Uyarlanabilen üç ögeyi ve solo karşılıklarını eşle. "Antrenman simülatörü" benzetmesi bu
   sınırı nasıl özetler?
8. Solo kata protokolünün 5 adımını kendi capstone'una (H12) uygula: RFP'yi kim verir, kaç soru
   sorarsın, teslimatta neler olur? "Cevaplayamadığın her soru bir risk kaydıdır" kuralı, ATAM
   çıktılarından hangisini beslemektedir ve kayıt hangi formata gider (H2 ADR pratiği)?
9. AI değerlendirici bulgusunu **ölçülü** aktar: ilk çalışma neyi gösterdi, hangi genelleme bu
   kaynaktan çıkarılamaz ve neden? "Sparring partneri" ile "sertifika jürisi" arasındaki fark,
   senin AI öğretmeninin `degerlendir` rubriğini kullanma biçimini nasıl şekillendirmeli?
10. Kata bankasındaki (e) senaryosunu (AI destek botlu SaaS yardım masası) al: H7-8'de öğrendiğin
    maliyet / eval / guardrail bilgisiyle bu RFP'ye hangi 5 soruyu sorardın? C4 Container
    diyagramında (H6) hangi kapları çizerdin ve en büyük trade-off cümlen (H5) ne olurdu?

## Kaynaklar
- **iSAQB — CPSA Advanced Level sınav düzeni** (~40 saatlik vaka + iki denetçi + sözlü savunma):
  https://isaqb.org/certifications/examinations/cpsa-advanced-level-examinations/
- **iSAQB — resmî müfredatlar ve modüller** (ARCEVAL, ADOC dahil; Foundation deneme sınavları):
  https://public.isaqb.org
- **SEI/CMU — Architecture Tradeoff Analysis Method (ATAM) koleksiyonu:**
  https://sei.cmu.edu/library/architecture-tradeoff-analysis-method-collection/
- **SEI/CMU — Quality Attribute Workshop (QAW):**
  https://resources.sei.cmu.edu/library/asset-view.cfm?assetid=474329
- **Neal Ford — Architectural Katas kuralları** (yabancılardan takım, Moderatör = müşteri,
  savunma fazı): https://nealford.com/katas/rules.html
- **O'Reilly — Architectural Katas (Q4 2025: AI-Enabled Architecture):**
  https://oreilly.com/live-events/architectural-katas-q4-2025-ai-enabled-architecture/0642572249625/
- **TheKataLog — kata döngülerinin arşivi (2020-2025, 9+ döngü):** https://github.com/TheKataLog
- **LLM'in ATAM senaryolarını değerlendirmesi üzerine ilk çalışma** (üniversite dersi; *şerh:
  initial study — genelleme taşımaz*): https://arxiv.org/pdf/2506.00150
