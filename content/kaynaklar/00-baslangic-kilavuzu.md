---
baslik: "Başlangıç Kılavuzu — 12 Haftada AI Çağı Yazılım Mimarı (önce bunu oku)"
yazar: "AI Öğretmen — program giriş belgesi"
tarih: "2026-07-15"
dil: "Türkçe program haritası (diyagramlı)"
etiketler: 1,2,3,4,5,6,7,8,9,10,11,12
---

### Bu metin ne anlatıyor?
Önündeki 12 haftalık eğitimin haritasıdır. Neyi, neden tam bu sırayla öğreneceğini; her günün
nasıl geçeceğini ve program bittiğinde neleri yapabiliyor olacağını anlatır. Ders değil, derse
başlamadan önce okunacak giriş belgesidir.

## Bu belge nedir — ve neden ilk bunu okumalısın
Elindeki program **12 hafta** sürüyor; her hafta **5 gün ders + görev + quiz** içeriyor. Ama daha ilk
güne başlamadan bir soruya cevap vermen gerekiyor: **neden buradasın ve bu 12 haftanın sonunda kim
olacaksın?** Bir yolculuğa haritayı görmeden çıkarsan, üçüncü haftada "ben aslında ne öğreniyorum?"
diye sorarsın. Bu belge o haritadır: **nereye gittiğini, neden tam bu sırayla gittiğini ve her gün
nasıl çalışacağını** baştan gösterir. On beş dakikanı ayır; sonra Hafta 1'e çok daha net başlarsın.

## Neden bu eğitim — çünkü senin işin değişti
Yıllarca "iyi yazılımcı" demek "iyi kod yazan" demekti. Artık **kodun büyük kısmını yapay zekâ
(AI) yazabiliyor.** Bu, işinin bittiği anlamına gelmiyor — **işinin yer değiştirdiği** anlamına
geliyor. AI bir satırı senden hızlı yazar; ama şunları yazamaz:

- **NE inşa edileceğine** karar vermeyi (hangi problem, hangi kapsam),
- **NEDEN böyle** olduğunu (hangi ödünleşmeyi (trade-off) neden seçtin),
- **HANGİ BEDELLE** olduğunu (bu tasarım hız mı, güvenlik mi, maliyet mi feda ediyor),
- ve bütün bunları **bir ekibe ve bir makineye anlatmayı.**

Bu program sana **AI'nin yapamadığı işi** öğretir: makineyi yöneten, mimari kararı gerekçelendiren,
niyeti hem insana hem ajana net anlatan **mimar** olmayı. Bir cümlede özeti şudur:

> **Kodu makine yazar; ama NE, NEDEN ve HANGİ BEDELLE sorularının cevabı sana kalır. Bu program o
> cevapları vermeyi öğretir.**

## Bu eğitimin sonunda ne yapabiliyor olacaksın
12 hafta bittiğinde — bunlar hedef değil, **çıktı**:

1. Bir AI ajanına iş yaptırırken **kod diktiren değil, onun çalışacağı ORTAMI tasarlayan** kişi
   olacaksın: ajana hangi bilgiyi (bağlam), hangi araçları ve nasıl bir geri beslemeyi vereceğini
   sen kurarsın. Şöyle düşün: AI, işe yeni başlamış **çok yetenekli ama şirketi hiç bilmeyen bir
   çalışan** gibidir — elleri hızlıdır ama kuralları, işi, sınırları bilmez. Sen onun yerine kod
   yazmazsın; ona **doğru ortamı, net görevi ve sonucu denetleyen bir kontrolü** verirsin. (Bu işin
   İngilizce adı **harness engineering**. "Harness" = koşum takımı, yani bir atı arabaya bağlayıp
   yönlendiren kayışlar; nasıl koşum atın gücünü arabacının istediği yöne çeviriyorsa, senin
   kurduğun ortam da AI'nin gücünü senin niyetine çevirir.)
2. Bir repoyu, hem insanın hem ajanın güvenebileceği bir **"kayıt sistemine"** çevireceksin
   (AGENTS.md, karar kayıtları (ADR), belge bahçesi).
3. Belirsiz bir isteği **net bir spec'e (şartnameye)** dökecek ve mimari kuralı **otomatik zorlayan**
   testler (ArchUnit gibi) yazacaksın — kural belgede değil, **çalışan sınırda** duracak.
4. Bir mimari kararı, moda ya da alışkanlıkla değil, **karakteristikler ve ödünleşmelerle**
   gerekçelendirebileceksin; kararı bir **C4 diyagramıyla** anlatabileceksin.
5. AI'yi bir oyuncak değil, bir **sistem parçası** olarak inşa edeceksin: model seçimi, **RAG**
   (modelin bilmediği bilgiyi soru anında ona okutma yöntemi — retrieval-augmented generation),
   değerlendirme (evaluation), gecikme (latency) ve **maliyet**.
6. Temiz mimariyi (**hexagonal / port-adapter**) sadece anlatmayacak, **kendi elinle koda dökecek**
   ve ArchUnit'le mühürleyeceksin.
7. Kararı tek kişinin sırtından alıp **ekibe ve sürece** taşıyacaksın (danışarak karar süreci —
   Advice Process), bir junior'ı (yeni geliştirici)
   yetiştirebileceksin.
8. Ölçekli, **veri-yoğun** bir sistemin (replikasyon, bölümleme, tutarlılık) ödünleşmelerini
   konuşabileceksin.

## Yol haritası — 12 hafta, 6 faz
Program rastgele bir konu listesi değil; **bir tırmanıştır.** Altı fazın her biri iki hafta sürer ve
seni bir öncekinden bir kat yukarı taşır (bkz. **Diyagram 1**).

```svg
<svg viewBox="0 0 640 470" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="12 haftalık programın 6 fazlı yol haritası: alttan üste zemin, disiplin, mimari düşünce, AI'yi inşa et, ustalaşma, zirve">
  <defs>
    <marker id="bg1-up" markerWidth="10" markerHeight="10" refX="4" refY="8" orient="auto"><path d="M4,0 L8,8 L0,8 Z" fill="#5D7789"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">12 hafta = 6 faz: bir tırmanış</text>
  <text x="320" y="43" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Her faz iki hafta; her faz seni bir öncekinin üstüne çıkarır.</text>

  <line x1="42" y1="430" x2="42" y2="66" stroke="#5D7789" stroke-width="1.6" marker-end="url(#bg1-up)"/>
  <text x="30" y="440" text-anchor="start" font-size="9" fill="#5D7789" font-family="system-ui">başladığın yer</text>
  <text x="30" y="60" text-anchor="start" font-size="9" font-weight="700" fill="#1C3247" font-family="system-ui">AI-çağı mimarı</text>

  <rect x="72" y="66" width="544" height="52" rx="9" fill="#1C3247" stroke="#1C3247" stroke-width="1.5"/>
  <rect x="72" y="66" width="8" height="52" rx="0" fill="#C77A17"/>
  <text x="92" y="88" text-anchor="start" font-size="11.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">FAZ 6 · ZİRVE</text>
  <text x="92" y="106" text-anchor="start" font-size="10" fill="#EAF1F7" font-family="system-ui">H11–12: Veri-yoğun sistemler (DDIA) + Liderlik, mentorluk, capstone</text>

  <rect x="72" y="128" width="544" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="72" y="128" width="8" height="52" fill="#186399"/>
  <text x="92" y="150" text-anchor="start" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">FAZ 5 · USTALAŞMA</text>
  <text x="92" y="168" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">H9–10: Hexagonal/Clean mimariyi elle uygula + Advice Process</text>

  <rect x="72" y="190" width="544" height="52" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <rect x="72" y="190" width="8" height="52" fill="#4F46E5"/>
  <text x="92" y="212" text-anchor="start" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">FAZ 4 · AI'yi İNŞA ET</text>
  <text x="92" y="230" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">H7–8: AI Engineering I–II — model, prompt, RAG, değerlendirme, maliyet</text>

  <rect x="72" y="252" width="544" height="52" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="72" y="252" width="8" height="52" fill="#25804C"/>
  <text x="92" y="274" text-anchor="start" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">FAZ 3 · MİMARİ DÜŞÜNCE</text>
  <text x="92" y="292" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">H5–6: Nitelikler ve ödünleşmeler + Mimari stiller ve C4 diyagramlama</text>

  <rect x="72" y="314" width="544" height="52" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <rect x="72" y="314" width="8" height="52" fill="#C77A17"/>
  <text x="92" y="336" text-anchor="start" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">FAZ 2 · DİSİPLİN</text>
  <text x="92" y="354" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">H3–4: Spec-driven akış + Guardrail'lar (kuralı otomatik zorla)</text>

  <rect x="72" y="376" width="544" height="52" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="72" y="376" width="8" height="52" fill="#186399"/>
  <text x="92" y="398" text-anchor="start" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">FAZ 1 · ZEMİN</text>
  <text x="92" y="416" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">H1–2: Harness mühendisliği + Repo bilgi sistemi (AGENTS.md, ADR)</text>
</svg>
```

**Faz faz, ne yapacaksın:**

- **Faz 1 · Zemin (Hafta 1–2).** AI ile çalışmanın altyapısı. Rolünün nasıl değiştiğini
  (kod yazan → ortam tasarlayan) kavrarsın; sonra repoyu, ajanın güvenle çalışabileceği bir **kayıt
  sistemine** çevirirsin: `AGENTS.md`, `docs/`, ilk karar kayıtların (ADR).
- **Faz 2 · Disiplin (Hafta 3–4).** Ortam kuruldu; şimdi **niyeti** net ifade etmeyi (task spec) ve
  makinenin kurala uymasını **garanti etmeyi** öğrenirsin. Kural artık bir belgede yatan iyi niyet
  değil, **çalışan bir sınırdır**: ArchUnit gibi testler yanlış katman erişimini kırmızıya çevirir.
- **Faz 3 · Mimari düşünce (Hafta 5–6).** Artık makineyi yönetiyorsun; ama **ne** inşa edeceğine sen
  karar veriyorsun. Mimarinin özü **ödünleşmedir**: her "-lik" (güvenilirlik, ölçeklenebilirlik…)
  bir başkasını zorlar. Kararı bir **C4 diyagramıyla** anlatmayı öğrenirsin.
- **Faz 4 · AI'yi inşa et (Hafta 7–8).** AI'yi bir araç olarak kullandın; şimdi AI'yi bir **sistem
  parçası** olarak inşa edersin: doğru modeli seçmek, prompt disiplini, RAG, değerlendirme
  (evaluation) ve **maliyet/gecikme** hesabı.
- **Faz 5 · Ustalaşma (Hafta 9–10).** Faz 3'ün teorisini **koda dökersin**: hexagonal/port-adapter
  mimarisini kendi elinle Java'da uygular, ArchUnit'le mühürlersin. Ve kararı tek kişiden alıp
  **ekibe** taşırsın (Advice Process, mimari karar forumu).
- **Faz 6 · Zirve (Hafta 11–12).** En zorlu teknik alan — **veri-yoğun sistemler** (replikasyon,
  bölümleme, tutarlılık) — artı **insan boyutu**: junior mentorluğu, ekip AI anlaşması ve her şeyi
  birleştiren bir **capstone** (bitirme projesi).

## Neden tam bu sıra — sırayı atlarsan üst kat çöker
En çok merak edilen soru: "neden önce şu değil de bu?" Cevap basit: **her hafta bir öncekinin üstüne
oturuyor.** Bu bir konu yığını değil, bir **taşıyıcı yapıdır** (bkz. **Diyagram 2**).

```svg
<svg viewBox="0 0 640 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Programın bağımlılık yapısı: altta ortam, üstünde disiplin, mimari düşünce, AI sistem, ustalaşma ve zirve katları; her kat altındakine dayanır">
  <defs>
    <marker id="bg2-up" markerWidth="10" markerHeight="10" refX="4" refY="8" orient="auto"><path d="M4,0 L8,8 L0,8 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Her kat, altındakine dayanır</text>
  <text x="320" y="43" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Alttaki sağlam olmadan üsttekini kuramazsın — sıra bilinçlidir.</text>

  <rect x="130" y="60" width="380" height="46" rx="8" fill="#1C3247" stroke="#1C3247" stroke-width="1.4"/>
  <text x="320" y="79" text-anchor="middle" font-size="10.5" font-weight="700" fill="#FFFFFF" font-family="system-ui">H11–12 · Ölçek + Liderlik</text>
  <text x="320" y="96" text-anchor="middle" font-size="9" fill="#EAF1F7" font-family="system-ui">veri-yoğun sistem + insan boyutu (mentorluk, capstone)</text>

  <rect x="130" y="112" width="380" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="131" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">H9–10 · Ustalaşma</text>
  <text x="320" y="148" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">teoriyi koda dök; kararı ekibe taşı</text>

  <rect x="130" y="164" width="380" height="46" rx="8" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="320" y="183" text-anchor="middle" font-size="10.5" font-weight="700" fill="#4F46E5" font-family="system-ui">H7–8 · AI'yi sistem yap</text>
  <text x="320" y="200" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">model, RAG, değerlendirme, maliyet</text>

  <rect x="130" y="216" width="380" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="320" y="235" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">H5–6 · Mimari düşünce</text>
  <text x="320" y="252" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">NE inşa edileceğine karar ver (ödünleşme)</text>

  <rect x="130" y="268" width="380" height="46" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="320" y="287" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">H3–4 · Disiplin</text>
  <text x="320" y="304" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">niyeti spec'e dök, kuralı otomatik zorla</text>

  <rect x="130" y="320" width="380" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="320" y="339" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">H1–2 · Zemin (temel)</text>
  <text x="320" y="356" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">AI'ye çalışacağı yeri ve kuralları ver</text>

  <line x1="545" y1="366" x2="545" y2="70" stroke="#25804C" stroke-width="1.8" marker-end="url(#bg2-up)"/>
  <text x="558" y="220" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui" transform="rotate(90 558 220)">dayanır</text>
</svg>
```

Somut örnekle: **Hafta 9'da** hexagonal mimariyi kendi elinle koda dökeceksin. Bunun için önceden
görmüş olman gereken **iki** şey var: **Hafta 4'te** katman kuralını otomatik zorlamayı (guardrail'lar)
ve **Hafta 5–6'da** o kuralın neden var olduğunu — hangi mimari ödünleşmeye hizmet ettiğini. İkisi de
Hafta 9'dan **önce** gelir; işte sıra bu yüzden böyle. Kuralı uygulamayı ve nedenini görmeden H9'da
hexagonal yazsan, ezberlemiş olursun — anlamamış. Programın bütün mantığı budur: **önce AI'yi
yönetmeyi öğren, sonra niyeti disipline et, sonra ne inşa edeceğine karar vermeyi öğren, sonra AI'yi
sistem yap, sonra ustalaş, sonunda ölçekle ve liderlik et.** Bir katı atlarsan üsttekini
ezberlersin — anlamazsın.

## Nasıl çalışacaksın — günün ritmi
Her hafta 5 gündür. Her gün aynı basit döngüyü izler; ve o döngünün kalbinde **AI Öğretmen** (bu
uygulama) vardır: bir görevi bitirdiğini söylersin, sana sorular sorar, yanlış anladığın yeri
yakalar (bkz. **Diyagram 3**).

```svg
<svg viewBox="0 0 640 372" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Günlük öğrenme döngüsü: oku, yap, konuş, test; ve bu uygulamanın öğrettiğinin canlı örneği olduğunu gösteren kartlar">
  <defs>
    <marker id="bg3-a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="bg3-b" markerWidth="9" markerHeight="9" refX="6" refY="3" orient="auto"><path d="M0,0 L6,3 L0,6 Z" fill="#C77A17"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Günlük ritim — ve neden bu uygulama dersin bir parçası</text>

  <rect x="24" y="50" width="128" height="58" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="88" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1. OKU</text>
  <text x="88" y="92" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">günün dersi</text>

  <rect x="186" y="50" width="128" height="58" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="250" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2. YAP</text>
  <text x="250" y="92" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">görevler (elle uygula)</text>

  <rect x="348" y="50" width="128" height="58" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="412" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">3. KONUŞ</text>
  <text x="412" y="92" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">AI Öğretmen sorar</text>

  <rect x="510" y="50" width="106" height="58" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="563" y="74" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">4. TEST</text>
  <text x="563" y="92" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">haftalık quiz</text>

  <line x1="152" y1="79" x2="184" y2="79" stroke="#186399" stroke-width="1.8" marker-end="url(#bg3-a)"/>
  <line x1="314" y1="79" x2="346" y2="79" stroke="#186399" stroke-width="1.8" marker-end="url(#bg3-a)"/>
  <line x1="476" y1="79" x2="508" y2="79" stroke="#186399" stroke-width="1.8" marker-end="url(#bg3-a)"/>
  <path d="M563,108 L563,132 L88,132 L88,110" fill="none" stroke="#C77A17" stroke-width="1.5" stroke-dasharray="5 4" marker-end="url(#bg3-b)"/>
  <text x="320" y="128" text-anchor="middle" font-size="9" fill="#C77A17" font-family="system-ui">ertesi gün baştan</text>

  <line x1="24" y1="158" x2="616" y2="158" stroke="#D6DEE5" stroke-width="1"/>
  <text x="320" y="180" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Bu uygulamanın KENDİSİ, öğrettiğinin canlı örneğidir</text>
  <text x="320" y="197" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Öğrendiğin her kavramı, bu repoda çalışırken görürsün:</text>

  <rect x="24" y="212" width="288" height="62" rx="9" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="168" y="232" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Hafta 2 → AGENTS.md</text>
  <text x="168" y="250" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">bu repoda AGENTS.md / CLAUDE.md dosyası var;</text>
  <text x="168" y="264" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">komutlar, sınırlar, "nerede ne var" orada.</text>

  <rect x="328" y="212" width="288" height="62" rx="9" fill="#FFFFFF" stroke="#4F46E5" stroke-width="1.3"/>
  <text x="472" y="232" text-anchor="middle" font-size="10" font-weight="700" fill="#4F46E5" font-family="system-ui">Hafta 2 &amp; 10 → ADR (karar kaydı)</text>
  <text x="472" y="250" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">docs/adr/ altında gerçek karar kayıtları:</text>
  <text x="472" y="264" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">her "neden böyle" bir dosyada yazılı.</text>

  <rect x="24" y="284" width="288" height="62" rx="9" fill="#FFFFFF" stroke="#25804C" stroke-width="1.3"/>
  <text x="168" y="304" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">Hafta 4 &amp; 9 → ArchUnit</text>
  <text x="168" y="322" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">bu uygulamanın katman kuralı testle mühürlü:</text>
  <text x="168" y="336" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">yanlış yön yazarsan build KIRMIZI olur.</text>

  <rect x="328" y="284" width="288" height="62" rx="9" fill="#FFFFFF" stroke="#C77A17" stroke-width="1.3"/>
  <text x="472" y="304" text-anchor="middle" font-size="10" font-weight="700" fill="#C77A17" font-family="system-ui">Hafta 5–6 → Katman mimarisi</text>
  <text x="472" y="322" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">types → config → repo → service → ai → web;</text>
  <text x="472" y="336" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek yön, tıpkı öğreneceğin gibi.</text>
</svg>
```

Yani bu uygulama bir **ders kitabı değil, canlı bir organizmadır.** Hafta 2'de "AGENTS.md nedir"
öğreneceksin — ve bu repoda gerçek bir AGENTS.md var. Hafta 4'te ArchUnit'le kuralı zorlamayı
göreceksin — bu uygulamanın kendi katmanları ArchUnit'le korunuyor. **Öğrettiğimiz her pratiğin
çalışan bir örneğinin üstünde duruyorsun.**

Pratik notlar:
- **Sıra önemli.** Bir hafta, bir önceki bitmeden tam açılmayabilir; bu bir aksaklık değil,
  yukarıdaki "her kat altındakine dayanır" ilkesinin uygulamadaki hâlidir.
- **Görevler "okudum" demekle bitmez.** Asıl öğrenme, **yaptığın** ve AI Öğretmen'e
  **anlattığın** yerde olur. Bir kavramı kendi cümlelerinle anlatamıyorsan, henüz öğrenmemişsindir.
- **Kaynaklar** (bu belge gibi diyagramlı özetler) her haftanın altında listelenir; bir kitabın
  aylarını dakikalara indiren yol arkadaşlarıdır — ama dersin yerine geçmez, dersi **derinleştirir.**

## Başlamadan önce — küçük bir anlaşma
Bu program seni "AI kod yazıyor, ben ne yapacağım?" kaygısından çıkarıp, **"AI'nin yapamadığını ben
yaparım"** güvenine taşımak için var. Ama tek şartı var: **acele etme, atlama.** Her hafta bir
öncekinin üstüne oturuyor; hızlı geçmek, üst katı ezberlemek demektir.

**Mezuniyet sonrası ölçülebilir bir dış hedef (isteğe bağlı):** Anthropic'in resmî
**Claude Certified Architect – Foundations** sınavı, bu programın işlediği alanla (araç arayüzü,
**MCP** — modelin dış araçlara ve verilere bağlanması için kullanılan açık protokol, Model
Context Protocol; ajan desenleri, bağlam yönetimi, eval, maliyet) büyük ölçüde örtüşüyor. İyi bir
hazırlık haritası: Daron Yöndem'in topluluk çalışma rehberi
(github.com/daronyondem/claude-architect-exam-guide — İngilizce; sınavın kendisi de İngilizce,
bunu hesaba kat).

> **Şerh:** Sertifika, bir satıcının kendi ürün ekosistemi üzerinden verdiği bir belgedir;
> mimarlık yetkinliğinin ölçüsü değildir ve kapsamı zamanla değişir. Burada hedef olarak değil,
> **turnusol** olarak anılıyor — program sertifika için değil, rol değişimi için var. Sınav
> içeriğini ve geçerliliğini kayıt olmadan önce kaynağından doğrula.

> **Kodu makine yazacak. NE, NEDEN ve HANGİ BEDELLE sorularının cevabını veren mimar SEN olacaksın.**
> Şimdi Hafta 1'e geç — ve rolünün nasıl değiştiğiyle başla.

## Öz-sınav (kendini test et)
1. Bu program, AI kod yazabildiği için işini kaybedeceğini değil, işinin **yer değiştirdiğini**
   söylüyor. Yeni işin hangi üç soruyla tanımlanıyor ve bunlar neden makineye devredilemiyor?
2. "Harness" kelimesi neden koşum takımından ödünç alınmış? Bu benzetmeye göre senin AI ile
   ilişkindeki rolün nedir — ve rolünü koda dokunarak mı, ortamı kurarak mı oynarsın?
3. Faz sırasının bilinçli olduğu söyleniyor. Hafta 9'daki hexagonal uygulamanın önüne konan iki
   önkoşulu ve her birinin neyi sağladığını yaz. Bu sıra bozulursa öğrenmede ne değişir?
4. Günlük ritim dört adımdan oluşuyor. Görevi bitirip quiz'e geçmek, hangi adımı atlamak olur ve
   bu adım neden "okudum" demekten farklı bir şey ölçer?
5. Bu uygulamanın kendisi neden dersin bir parçası sayılıyor? Öğreneceğin kavramlardan ikisini,
   bu repoda karşılığı olan somut dosya ya da mekanizmayla eşleştir.
6. Metinde adı geçen sertifika neden "hedef değil turnusol" diye niteleniyor? Bir sertifikayı
   yetkinlik kanıtı saymanın sakıncası nedir?

### Cevap anahtarı (önce kendin cevapla, sonra bak)
1. **NE** inşa edileceği, **NEDEN** öyle olduğu (hangi ödünleşme seçildi) ve **HANGİ BEDELLE**
   olduğu. Devredilemez, çünkü üçü de bağlam, sorumluluk ve değer yargısı gerektirir; model
   bunları veriden türetemez, kendisine verilmesini bekler.
2. Koşum takımı, atın gücünü arabacının istediği yöne çeviren kayışlardır. Sen atın yerine
   koşmazsın; **yönü, sınırı ve geri beslemeyi kurarsın**. Rolünü ortamı kurarak oynarsın: hangi
   bağlam, hangi araçlar, hangi denetim.
3. Hafta 4 (kuralı otomatik zorlama — guardrail'lar) kuralın çalışan bir sınır hâline gelmesini
   sağlar; Hafta 5–6 (nitelikler ve ödünleşmeler) o kuralın **neden** var olduğunu verir. Sıra
   bozulursa hexagonal öğrenilmez, ezberlenir: yapı taklit edilir ama gerekçesi bilinmez.
4. "Konuş" adımını atlamak olur. Bu adım, kavramı **kendi cümlelerinle** kurabiliyor musun onu
   ölçer; okumak tanımayı, anlatmak anlamayı gösterir.
5. Çünkü uygulamanın kendisi öğretilen pratiklerle kurulmuştur. Örnekler: AGENTS.md/CLAUDE.md
   dosyası (repo bilgi sistemi), `docs/adr/` altındaki karar kayıtları, ArchUnit'le mühürlenen
   `types → config → repo → service → ai → web` katman yönü.
6. Çünkü sertifikayı veren taraf aynı zamanda ürünü satan taraftır ve kapsamı zamanla değişir;
   ölçtüğü şey bir ekosistem bilgisidir, mimari yargı değildir. Sakınca, belgeyi yetkinlik
   sanıp asıl ölçütü — gerçek bir sistemde karar verip gerekçelendirebilmeyi — atlamaktır.
