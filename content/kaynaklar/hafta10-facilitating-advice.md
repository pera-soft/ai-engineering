---
baslik: "Hafta 10 Görsel Özet: Advice Process ve Merkeziyetsiz Mimari Karar"
yazar: "Facilitating Software Architecture (Harmel-Law, O'Reilly 2024) — kavram özeti"
tarih: "2026-07-11"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 10
---

### Bu metin ne anlatıyor?
Bir yazılım ekibi büyüdükçe şu soru ortaya çıkar: önemli teknik kararları kim verecek? Klasik
cevap "bir baş mimar ya da bir onay komitesi" olur; ama bu, herkesin onay için beklediği bir
kuyruk yaratır. Bu metin başka bir yolu anlatıyor: kararı **işi yapan kişi verir, ama karar
vermeden önce belirli insanlara danışmak zorundadır.** Danışma zorunlu, tavsiyeye uymak
değil. Metin ayrıca bu kararların nasıl görünür kılındığını (forum ve yazılı karar kaydı) ve
teknoloji seçiminin ortak bir dille nasıl hizalandığını (radar) gösteriyor.

### Bu metinde geçen temel terimler
- **Mimari karar (architectural decision):** Geri dönüşü pahalı olan, birden çok ekibi veya
  sistemin genelini etkileyen teknik karar (ör. "veritabanını hangi teknolojiyle kuralım").
- **Advice process (danışma süreci):** Bu metnin ana konusu. Kararı tek bir kişi verir; ama
  karardan etkilenenlere ve konuyu bilenlere danışmadan veremez.
- **Konsensüs (consensus):** Herkesin uzlaşması. Advice process bu **değildir** — karışmasın.
- **ADR (Architecture Decision Record / Mimari Karar Kaydı):** Bir kararı, bağlamını, alınan
  tavsiyeleri ve sonuçlarını yazan kısa belge.

## Neden bu özet
Mimari kararı tek bir mimara ya da bir onay komitesine sıkıştırmak, sistem büyüdükçe
darboğaza (bottleneck) döner: kararı verecek kişi işi en iyi bilenden uzaktır ve herkes
onay için bekler. Harmel-Law'ın önerdiği yol tam tersidir — *advice process* (danışma
süreci) kararı verme yetkisini yayar ama danışmayı zorunlu kılar. Bu üç görsel, kitabın
merkeziyetsiz (decentralized) karar modelini bir çırpıda bakılacak referans niyetinedir.

## 1. Advice Process (Danışma süreci)
Temel kural: **herkes** her mimari kararı verebilir — ama kararı vermeden önce **iki gruba
danışmak zorundadır**: (1) karardan etkilenenler, (2) o konunun uzmanları. Alınan tavsiyeyi
uygulamak zorunlu değildir; ama samimiyetle danışmak zorunludur.

Bu ayrımı kaçırmamak için üç şeyi yan yana koy:

| | Kim karar verir? | Danışma | Tıkanma riski |
|---|---|---|---|
| **Oylama** | Çoğunluk | — | Azınlığın haklı olduğu durumu ezer |
| **Konsensüs** | Herkes birlikte | Sonsuz | Bir kişi itiraz ederse iş durur |
| **Advice process** | **Tek kişi** (işi yapan) | **Zorunlu** | Yok — karar hiçbir yerde beklemez |

Advice process bir konsensüs ya da komite oylaması **değildir**: karar tek bir kişinindir,
danışma ise pazarlıksızdır. Kimsenin veto hakkı yoktur — ama kimse danışmayı da atlayamaz.
Verilen tavsiyeye uymuyorsan, **neden uymadığını kayda geçmen** beklenir; tavsiye ile ona
verilen cevap birlikte kararın gerekçesinin parçası olur.

Sonuç; hız, sahiplik (ownership — kararın arkasında duran belli bir kişi olması) ve hizalanmanın
(alignment — ekiplerin birbiriyle uyumlu yönde ilerlemesi) aynı anda korunmasıdır.

```svg
<svg viewBox="0 0 660 250" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Advice process akışı: karar veren önce etkilenenlere ve uzmanlara danışır, sonra kararı kendi verir ve ADR'ye kaydeder; konsensüs ya da komite değildir">
  <defs>
    <marker id="h10ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="330" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Advice Process — tek karar verici, zorunlu danışma</text>
  <rect x="18" y="46" width="140" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="88" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Karar veren</text>
  <text x="88" y="87" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(herkes olabilir)</text>
  <rect x="180" y="46" width="140" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="250" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Danış</text>
  <text x="250" y="87" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(zorunlu adım)</text>
  <rect x="342" y="46" width="140" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="412" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Kendi kararını ver</text>
  <text x="412" y="87" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(sorumluluk sende)</text>
  <rect x="504" y="46" width="138" height="52" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="573" y="70" text-anchor="middle" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">ADR'ye kaydet</text>
  <text x="573" y="87" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(kalıcı iz)</text>
  <line x1="160" y1="72" x2="178" y2="72" stroke="#186399" stroke-width="1.8" marker-end="url(#h10ok1)"/>
  <line x1="322" y1="72" x2="340" y2="72" stroke="#186399" stroke-width="1.8" marker-end="url(#h10ok1)"/>
  <line x1="484" y1="72" x2="502" y2="72" stroke="#186399" stroke-width="1.8" marker-end="url(#h10ok1)"/>
  <line x1="250" y1="98" x2="250" y2="122" stroke="#186399" stroke-width="1.8" marker-end="url(#h10ok1)"/>
  <rect x="138" y="124" width="106" height="46" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="191" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">1 · Etkilenenler</text>
  <text x="191" y="161" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bundan etkilenen</text>
  <rect x="256" y="124" width="106" height="46" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="309" y="146" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · Uzmanlar</text>
  <text x="309" y="161" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">konuyu bilen</text>
  <rect x="18" y="188" width="624" height="54" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="330" y="210" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Konsensüs / komite DEĞİL — kararı tek kişi verir, ama danışma zorunludur.</text>
  <text x="330" y="230" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Tavsiyeyi uygulamak zorunlu değil; samimiyetle danışmak zorunlu.</text>
</svg>
```

## 2. Merkezi vs merkeziyetsiz karar
Aynı organizasyon, kararı iki şekilde alabilir. Solda tek bir mimar/komite her şeyi onaylar:
işi bilen ekipler beklerken karar tek noktada tıkanır. Sağda advice process çalışır: her ekip
kendi kararını verir ve danışır; karar akışı paralel ilerler, kimse kuyrukta beklemez.

```svg
<svg viewBox="0 0 660 290" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Solda merkezi mimar veya komite darboğaz oluşturur, ekipler onay için bekler; sağda advice process ile her ekip kendi kararını verip danışır, akış paralel ilerler">
  <defs>
    <marker id="h10ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#C77A17"/></marker>
    <marker id="h10ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="330" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Merkezi (darboğaz) vs Merkeziyetsiz (akış)</text>
  <rect x="14" y="40" width="306" height="234" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="167" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Merkezi mimar / komite</text>
  <text x="167" y="79" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">herkes onay için BEKLER</text>
  <rect x="30" y="90" width="76" height="34" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="68" y="111" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Ekip 1</text>
  <rect x="129" y="90" width="76" height="34" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="167" y="111" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Ekip 2</text>
  <rect x="228" y="90" width="76" height="34" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="266" y="111" text-anchor="middle" font-size="10.5" fill="#186399" font-family="system-ui">Ekip 3</text>
  <line x1="68" y1="125" x2="138" y2="194" stroke="#C77A17" stroke-width="1.6" marker-end="url(#h10ok2)"/>
  <line x1="167" y1="125" x2="167" y2="194" stroke="#C77A17" stroke-width="1.6" marker-end="url(#h10ok2)"/>
  <line x1="266" y1="125" x2="196" y2="194" stroke="#C77A17" stroke-width="1.6" marker-end="url(#h10ok2)"/>
  <rect x="104" y="198" width="126" height="46" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="167" y="220" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">Mimar / Komite</text>
  <text x="167" y="236" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">(darboğaz)</text>
  <text x="167" y="264" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Tek karar noktası → yavaş</text>
  <rect x="340" y="40" width="306" height="234" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="493" y="62" text-anchor="middle" font-size="12.5" font-weight="700" fill="#1C3247" font-family="system-ui">Advice Process</text>
  <text x="493" y="79" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">herkes KARAR verir + danışır</text>
  <rect x="356" y="90" width="150" height="44" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="431" y="110" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Ekip 1 · karar verir</text>
  <text x="431" y="126" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">danışır ✓</text>
  <rect x="406" y="148" width="150" height="44" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="481" y="168" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Ekip 2 · karar verir</text>
  <text x="481" y="184" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">danışır ✓</text>
  <rect x="456" y="206" width="150" height="44" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.4"/>
  <text x="531" y="226" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Ekip 3 · karar verir</text>
  <text x="531" y="242" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">danışır ✓</text>
  <line x1="447" y1="134" x2="472" y2="148" stroke="#25804C" stroke-width="1.6" marker-end="url(#h10ok3)"/>
  <line x1="497" y1="192" x2="522" y2="206" stroke="#25804C" stroke-width="1.6" marker-end="url(#h10ok3)"/>
  <text x="493" y="266" text-anchor="middle" font-size="10.5" font-weight="600" fill="#1C3247" font-family="system-ui">Dağıtık karar → hızlı akış</text>
</svg>
```

Peki alınan kararlar ortada nasıl görünür kalır? İki mekanizmayla: kararların açıkça
konuşulduğu **Architecture Advice Forum (AAF)** ve her kararın yazıya döküldüğü **ADR**.

**Architecture Advice Forum (AAF):** düzenli aralıkla toplanan, şeffaf bir forumdur.
Kararlar ve alınan danışmalar burada herkese açık paylaşılır. Amacı **onay vermek değildir** —
görünürlük (visibility) ve ortak öğrenmedir; kimse kararı forumdan geçirmek zorunda değildir,
ama forum sayesinde herkes neyin neden kararlaştırıldığını görür. Kararlar ADR'ye kaydedilir.

**ADR = omurga:** Her mimari karar bir *Architecture Decision Record* ile yazıya geçer:
kararın kendisi, **bağlamı (context)**, **alınan danışma**, ve **sonucu/etkileri**. ADR bir
dayatma aracı değil, bir kolaylaştırma (facilitation) aracıdır — kararın gerekçesini ve
danışıldığı kişileri kalıcı hale getirir, böylece gelecekteki ekipler "neden böyle?" sorusunun
cevabını bulabilir.

> **Şerh (abartıya karşı):** Advice process bedava ya da her ortamda uygulanabilir bir tarif
> değildir. Çalışması için birkaç önkoşulu vardır: kararı verecek kişilerin o konuda yeterli
> deneyimi olmalı (yeni başlayanlardan kurulu bir ekipte "herkes karar verebilir" kaosa döner);
> danışılan kişinin "bu fikir kötü" diyebileceği bir güven ortamı bulunmalı; ve danışma ile
> kararın kaydı gerçekten tutulmalı — tutulmazsa süreç birkaç ay içinde "kimseye sormadan
> yapma"ya çürür. Ayrıca bazı kararlar (yasal zorunluluk, güvenlik politikası, düzenlemeye
> tabi alanlar) tanım gereği merkezîdir ve bu sürecin dışındadır. Kitap bunu bir kültür
> değişimi olarak sunar; bir araç kurup bir toplantı koyarak kurulmuş sayılmaz.

## 3. Technology Radar (Teknoloji radarı)
Teknoloji seçimini de merkeziyetsiz ama hizalı tutmanın yolu radardır (aslen Thoughtworks'ün
altı ayda bir yayımladığı Technology Radar'dan gelen, ekiplerin kendi iç radarını kurması için
uyarlanan bir düzen): her aday teknoloji dört halkadan birine yerleştirilir. Merkeze yakın olan
daha olgun ve güvenlidir; dışa doğru gittikçe temkin artar. Böylece ekipler kendi seçimlerini
yapar ama ortak bir dil paylaşır — "bu kütüphane bizde Assess'te" cümlesi, uzun bir tartışmayı
tek kelimeye indirir.

Radar bir yasak listesi değildir: **Hold**, "kullanmak yasak" demek değil, "yeni işlerde bunu
varsayılan seçme, seçeceksen gerekçeni yaz" demektir. Radarın değeri, hangi teknolojinin nerede
durduğunun **düzenli aralıklarla yeniden konuşulmasıdır**; bir kez doldurulup unutulan radar,
kısa sürede gerçeği yansıtmayan bir duvar süsüne döner.

```svg
<svg viewBox="0 0 660 258" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Technology Radar dört halkası: Adopt benimse, Trial denemeye değer, Assess araştır, Hold kullanmayı ertele">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Technology Radar — dört halka</text>
  <text x="330" y="44" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">teknoloji seçimini merkeziyetsiz ama hizalı yapar; merkeze yakın = daha olgun</text>
  <rect x="30" y="58" width="290" height="78" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="46" y="86" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Adopt · Benimse</text>
  <text x="46" y="106" font-size="10.5" fill="#1C3247" font-family="system-ui">kanıtlanmış; güvenle kullan —</text>
  <text x="46" y="123" font-size="10.5" fill="#1C3247" font-family="system-ui">varsayılan (default) seçenek.</text>
  <rect x="340" y="58" width="290" height="78" rx="11" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="356" y="86" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">Trial · Denemeye değer</text>
  <text x="356" y="106" font-size="10.5" fill="#1C3247" font-family="system-ui">umut verici; sınırlı bir</text>
  <text x="356" y="123" font-size="10.5" fill="#1C3247" font-family="system-ui">projede denemeye değer.</text>
  <rect x="30" y="148" width="290" height="78" rx="11" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="46" y="176" font-size="12.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Assess · Araştır</text>
  <text x="46" y="196" font-size="10.5" fill="#1C3247" font-family="system-ui">izle ve incele; henüz</text>
  <text x="46" y="213" font-size="10.5" fill="#1C3247" font-family="system-ui">ciddi işe bağlanma.</text>
  <rect x="340" y="148" width="290" height="78" rx="11" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="356" y="176" font-size="12.5" font-weight="700" fill="#C77A17" font-family="system-ui">Hold · Kullanmayı ertele</text>
  <text x="356" y="196" font-size="10.5" fill="#1C3247" font-family="system-ui">yeni işlerde başlama;</text>
  <text x="356" y="213" font-size="10.5" fill="#1C3247" font-family="system-ui">mevcutta temkinli ol.</text>
  <text x="330" y="248" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Sıra (olgunluktan temkine): Adopt → Trial → Assess → Hold</text>
</svg>
```

## Öz-sınav (kendini test et)
1. Advice process'te kararı kim verir, ve karar vermeden önce hangi iki gruba danışmak zorunludur?
2. "Danışma zorunlu ama tavsiyeyi uygulamak zorunlu değil" cümlesi ne anlama gelir? Bu neden konsensüs/komite değildir?
3. Architecture Advice Forum'un amacı onay vermek midir? Değilse, ne sağlar?
4. Bir ADR hangi dört şeyi kaydeder ve neden "dayatma değil kolaylaştırma" aracı sayılır?
5. Technology Radar'ın dört halkasını olgunluk sırasına diz ve "Assess" ile "Hold" arasındaki farkı bir cümleyle açıkla.
6. Oylama, konsensüs ve advice process'i "kararı kim verir" ve "iş nerede tıkanır" sorularıyla
   karşılaştır. Advice process neden bu üçü içinde tıkanmayanıdır?
7. Aldığın tavsiyeye uymamaya karar verdin. Süreç senden ne yapmanı bekler — ve bu neden
   sadece bir kayıt işi değil, sürecin çalışmasının şartıdır?
8. Advice process'in çalışması için hangi önkoşullar gerekir? Bu koşullar yoksa süreç ne hâle
   gelir?
