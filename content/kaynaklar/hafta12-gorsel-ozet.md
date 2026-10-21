---
baslik: "Hafta 12 Görsel Özet: Liderlik, Mentorluk ve Otonomi Sınırı"
yazar: "Anthropic 'AI assistance & coding skills' RCT (29 Oca 2026) + bu haftanın Türkçe şablonları — kavram özeti"
tarih: "2026-07-17"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 12
---

### Bu metin ne anlatıyor?
Bu haftaya kadar öğrendiklerin senin *kendi* çalışmanla ilgiliydi. Bu metin bir adım yukarısını
anlatıyor: yanında çalışan daha az deneyimli bir kişinin ve senin adına iş yapan yapay zekâ
ajanlarının nasıl çalışacağına karar vermeyi. Üç fikri var: yapay zekâya işi tümüyle
devretmenin öğrenmeyi nasıl kösteklediği, bunun panzehiri olan haftalık bir mentorluk düzeni,
ve bir ajana asla devredilmemesi gereken işlerin nerede başladığı.

### Bu metinde geçen temel terimler
- **Junior (yeni geliştirici):** Mesleğe yeni başlamış, deneyimi az yazılımcı.
- **Mentor / mentorluk:** Deneyimli birinin, daha az deneyimliyi düzenli olarak yönlendirmesi.
- **Ajan (agent):** Kendisine verilen hedefe ulaşmak için kendi kendine adım atabilen —
  dosya okuyup yazabilen, komut çalıştırabilen — yapay zekâ programı.
- **Otonomi (autonomy):** Ajanın insana sormadan iş yapabilme serbestliği. "Otonomi sınırı",
  bu serbestliğin nerede bittiğidir.
- **Sandbox (kum havuzu):** Bir programın yalnızca kendisine açılmış dar bir alanda
  çalışmasını sağlayan yalıtım düzeni; dışına çıkmaya çalışırsa engellenir.
- **Guardrail (koruma bariyeri):** Yanlış işlemi *mümkün olmaktan* çıkaran teknik önlem;
  "lütfen yapma" demenin değil, yapamamanın adı.
- **PR (pull request / birleştirme isteği):** Yaptığın kod değişikliğini ana koda katmak için
  açtığın, başkalarının inceleyip yorum yaptığı istek.
- **RCT (randomized controlled trial / randomize kontrollü deney):** Katılımcıların rastgele
  gruplara ayrıldığı, bir grubun bir müdahaleyi alıp diğerinin almadığı bilimsel deney düzeni.

## Neden bu özet
Program 5–11. haftalarda her hafta için bir görsel özet veriyor; **12. hafta bunsuz kalmıştı** —
oysa burada öğrendiğin şey en soyut olanı: *başkalarının* (ve ajanların) nasıl çalışacağına karar
vermek. Bu haftanın üç fikri bir yaprakta:

1. **Beceri körelmesi** — AI'ı yanlış kullanan junior hızlanır ama öğrenmez; panzehiri var ve ölçülmüş.
2. **Mentorluk bir ritimdir** — iyi niyet değil, haftalık bir takvim.
3. **Otonomi sınırı** — ajana neyi asla devretmezsin, ve bu sınırı ne korur.

Şablonların tamamı (spec, code-review listesi, ekip anlaşması, kata) ayrı kaynakta:
`hafta12-liderlik-sablonlari`. Burada tekrar edilmez — burada **resmi** görürsün.

---

## 1. Beceri körelmesi ve panzehiri — aynı görev, iki junior
Anthropic'in çalışması iki deseni ayırıyor: işi **tam delege eden** junior (AI kodu yazar, o
birleştirir) kavramada **%40'ın altında** kalıyor; AI'ı *"bu neden böyle, alternatifi ne"* diye
**kavramsal soru** için kullanan junior **%65+**'a çıkıyor. İkisi de işi bitiriyor — fark
teslim edilen kodda değil, **kafada kalan modelde**.

Metafor haftanın kendi cümlesi: tam delege, **spor salonuna gidip ağırlıkları başkasına
kaldırtmaktır.** Ağırlık kalkar, kayıtta "20 tekrar" yazar — ama kas senin değildir.

> **Şerh (sayılara karşı):** Buradaki %40 ve %65 rakamları **tek bir çalışmadan** gelir; belirli
> bir katılımcı grubunda, belirli görevlerle ve o çalışmanın kendi kavrama testiyle ölçülmüştür.
> Doğa yasası değildir: başka bir ekipte, başka görevlerde sayılar farklı çıkar. Bu metinde
> güvenilmesi istenen şey rakamların kendisi değil, **yönü** — yani "işi tümüyle devretmek işi
> bitirir ama kavramayı bitirmez, soru sorarak kullanmak ikisini birden verir" bulgusu. Rakamı
> ekibinde bir hedef ("%65'e çıkalım") gibi kullanma; kendi ekibinde gözlemleyebileceğin işareti
> ara: junior kendi yazdığı kodu anlatabiliyor mu, anlatamıyor mu?

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Aynı görevi alan iki junior: solda tam delege eden junior AI'dan hazır kod alır, işi biter ama kavraması yüzde kırkın altında kalır; sağda kavramsal soru soran junior AI'ı düşündüren öğretmen gibi kullanır, işi biter ve kavraması yüzde altmış beşin üzerine çıkar; her ikisinde de kod teslim edilir, fark kafada kalan modeldir">
  <defs>
    <marker id="h12ok1" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
    <marker id="h12ok2" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Aynı görev: "Sipariş servisine yeniden deneme ekle"</text>

  <text x="165" y="50" text-anchor="middle" font-size="12" font-weight="700" fill="#B3402F" font-family="system-ui">Tam delege</text>
  <rect x="42" y="60" width="246" height="46" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="165" y="80" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">"Retry mantığı yaz" → hazır kod</text>
  <text x="165" y="97" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">AI = cevap makinesi</text>
  <line x1="165" y1="108" x2="165" y2="140" stroke="#B3402F" stroke-width="1.6" marker-end="url(#h12ok1)"/>
  <rect x="42" y="144" width="246" height="52" rx="10" fill="#FFFFFF" stroke="#B3402F" stroke-width="1.8"/>
  <text x="165" y="166" text-anchor="middle" font-size="13" font-weight="700" fill="#B3402F" font-family="system-ui">Kavrama &lt; %40</text>
  <text x="165" y="184" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iş bitti, öğrenme yok</text>

  <text x="495" y="50" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Conceptual Inquiry (panzehir)</text>
  <rect x="372" y="60" width="246" height="46" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="80" text-anchor="middle" font-size="11.5" fill="#1C3247" font-family="system-ui">"Neden retry? Alternatifi ne?"</text>
  <text x="495" y="97" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">AI = düşündüren öğretmen</text>
  <line x1="495" y1="108" x2="495" y2="140" stroke="#25804C" stroke-width="1.6" marker-end="url(#h12ok2)"/>
  <rect x="372" y="144" width="246" height="52" rx="10" fill="#FFFFFF" stroke="#25804C" stroke-width="1.8"/>
  <text x="495" y="166" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Kavrama %65+</text>
  <text x="495" y="184" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">iş bitti, model kafada kaldı</text>

  <rect x="42" y="212" width="576" height="42" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="330" y="230" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">İkisinde de kod teslim edildi. Fark teslim edilen kodda değil — kafada kalan modelde.</text>
  <text x="330" y="246" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Tam delege = spor salonunda ağırlığı başkasına kaldırtmak: kayıtta "20 tekrar" yazar, kas senin değildir.</text>
</svg>
```

---

## 2. Mentorluk bir ritimdir — haftalık akış
Mentorluk "kapım açık" demek değildir; bir **takvimdir**. Amaç junior'ı AI ile hızlandırırken
**üretken zorlanma (productive struggle)** içinde tutmak: zoru tam delege ederek kavramayı
köreltmesini engellemek. Ritmi kendi ekibinin takvimine uyarla — önemli olan günler değil,
**her haftada bu beş işlevin bulunması**.

```svg
<svg viewBox="0 0 660 236" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Haftalık junior mentorluk ritmi beş adımlı bir döngüdür: pazartesi haftalık hedef ve spec, salı kavramsal soru günü, çarşamba yapay zekasız yarım gün, perşembe review'da anlat sorusu, cuma retrospektif; cuma tekrar pazartesiye bağlanır ve döngü haftalık olarak tekrarlanır">
  <defs>
    <marker id="h12ok3" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Haftalık mentorluk ritmi — beş işlev, bir döngü</text>

  <rect x="20" y="52" width="116" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="78" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Pazartesi</text>
  <text x="78" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Hedef + spec</text>
  <text x="78" y="106" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">niyetsiz koda başlama</text>
  <line x1="138" y1="85" x2="158" y2="85" stroke="#186399" stroke-width="1.6" marker-end="url(#h12ok3)"/>

  <rect x="162" y="52" width="116" height="66" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.8"/>
  <text x="220" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Salı</text>
  <text x="220" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Conceptual Inquiry</text>
  <text x="220" y="106" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">en yüksek kavrama</text>
  <line x1="280" y1="85" x2="300" y2="85" stroke="#186399" stroke-width="1.6" marker-end="url(#h12ok3)"/>

  <rect x="304" y="52" width="116" height="66" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="362" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Çarşamba</text>
  <text x="362" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">AI'sız yarım gün</text>
  <text x="362" y="106" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">temel kaslar</text>
  <line x1="422" y1="85" x2="442" y2="85" stroke="#186399" stroke-width="1.6" marker-end="url(#h12ok3)"/>

  <rect x="446" y="52" width="116" height="66" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="504" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Perşembe</text>
  <text x="504" y="90" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">"Burayı anlat"</text>
  <text x="504" y="106" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">review = mentorluk sahnesi</text>

  <rect x="272" y="146" width="192" height="58" rx="10" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.5"/>
  <text x="368" y="166" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">Cuma — Retrospektif</text>
  <text x="368" y="184" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">"AI olmadan yapamayacağın ne öğrendin?"</text>

  <path d="M562,118 L562,175 L472,175" fill="none" stroke="#186399" stroke-width="1.6" marker-end="url(#h12ok3)"/>
  <path d="M264,175 L78,175 L78,126" fill="none" stroke="#186399" stroke-width="1.6" stroke-dasharray="4 3" marker-end="url(#h12ok3)"/>
  <text x="170" y="192" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">döngü haftaya yeniden başlar</text>

  <text x="330" y="222" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Junior takılınca cevabı verme: "sence neden olmuyor, ilk nereye bakarsın?" — zorlanma anı, öğrenmenin gerçekleştiği andır.</text>
</svg>
```

---

## 3. Otonomi sınırı — insan onayına sabitlenen kararlar
Ajana ne kadar güvenirsen güven, bazı işlemler **geri dönüşü zordur**. Bunları otonominin dışında
tutarsın. Kritik ayrım şu: **`AGENTS.md` ihtimali azaltır, asıl korumayı politika + sandbox verir.**

`AGENTS.md` (bu projede `CLAUDE.md`), ajanın işe başlarken okuduğu talimat dosyasıdır: projenin
kuralları, sınırları ve alışkanlıkları burada yazılıdır. Ajan bunu okur ve **genellikle** uyar —
ama uyacağının garantisi yoktur, çünkü metin bir yönlendirmedir, bir kısıt değil. Talimat dosyası
bir *rica*dır; sandbox bir *duvar*: izin verilmemiş bir işlem denendiğinde ajan onu yapamaz,
"yapmamayı tercih etmez". İkisini karıştırma — birincisine güvenip ikincisini kurmamak, Hafta 4'ün
(guardrail) tüm dersini çöpe atmaktır.

```svg
<svg viewBox="0 0 660 282" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Otonomi sınırı: solda ajanın serbest çalışabileceği geri alınabilir işler (kod yazma, test, refactor, dokümantasyon); sağda insan onayı zorunlu geri dönüşü zor üç işlem (şema göçü, dış sözleşme değişikliği, veri silme); altta iki koruma katmanı: AGENTS.md yalnızca ihtimali azaltır, asıl korumayı politika ve sandbox verir">
  <defs>
    <marker id="h12ok4" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#B3402F"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Otonomi sınırı — neyi asla devretmezsin</text>

  <rect x="24" y="44" width="278" height="128" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="163" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Ajan serbest — geri alınabilir</text>
  <text x="163" y="88" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kod yazma · refactor · test ekleme</text>
  <text x="163" y="106" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">dokümantasyon · yerel deney</text>
  <text x="163" y="130" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Hata olursa: geri al, testler yakalar,</text>
  <text x="163" y="145" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">maliyet = biraz zaman</text>
  <text x="163" y="163" text-anchor="middle" font-size="9.5" font-style="italic" fill="#25804C" font-family="system-ui">hız burada kazanılır</text>

  <line x1="316" y1="50" x2="316" y2="166" stroke="#B3402F" stroke-width="2" stroke-dasharray="5 4"/>
  <text x="316" y="188" text-anchor="middle" font-size="10" font-weight="700" fill="#B3402F" font-family="system-ui">SINIR</text>

  <rect x="330" y="44" width="306" height="128" rx="12" fill="#FFFFFF" stroke="#B3402F" stroke-width="1.8"/>
  <text x="483" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B3402F" font-family="system-ui">İNSAN ONAYI ZORUNLU — geri dönüşü zor</text>
  <text x="483" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1. Şema göçü — veri kaybı / kilitlenme</text>
  <text x="483" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2. Dış sözleşme / API — başka ekipler kırılır</text>
  <text x="483" y="130" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">3. Veri silme — yedek ve rollback doğrulandı mı?</text>
  <text x="483" y="155" text-anchor="middle" font-size="9.5" font-style="italic" fill="#B3402F" font-family="system-ui">açık insan onayı olmadan birleştirilemez</text>

  <rect x="24" y="204" width="290" height="62" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="169" y="226" text-anchor="middle" font-size="11.5" font-weight="700" fill="#C77A17" font-family="system-ui">AGENTS.md — ihtimali AZALTIR</text>
  <text x="169" y="244" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bir ricadır; ajan okur, genellikle uyar</text>
  <text x="169" y="259" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ama garanti etmez</text>

  <rect x="346" y="204" width="290" height="62" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.8"/>
  <text x="491" y="226" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Politika + sandbox — KORUR</text>
  <text x="491" y="244" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">bir duvardır; izin yoksa işlem olmaz</text>
  <text x="491" y="259" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">asıl koruma buradadır (Hafta 4)</text>

  <line x1="314" y1="235" x2="342" y2="235" stroke="#B3402F" stroke-width="1.6" marker-end="url(#h12ok4)"/>
</svg>
```

> **Liderlik bağı:** Bu üç resim aynı şeyi söylüyor — **hız serbest bırakılan yerde, güvenlik
> sabitlenen yerde kazanılır.** Junior'ı zorlanmadan kurtarmak da, ajana geri dönüşü zor işi
> devretmek de aynı hatanın iki yüzü: kısa vadede iş biter, uzun vadede kapasite kaybolur.
> Ölçüm tarafı için: `ai-verimlilik-olcumu` (AI'ın işe yarayıp yaramadığını *sorarak* bilemezsin).

---

## Öz-sınav (kendini test et)
1. Aynı görevi alan iki junior'dan biri kavramada %40'ın altında, diğeri %65+ çıkıyor. İkisi de işi
   teslim ediyorsa, fark **nerede** birikiyor — ve bu fark ekibe ne zaman fatura olarak döner?
2. "Conceptual Inquiry" ile "AI'a kod yazdırma" arasındaki fark, kullanılan araçta mı yoksa sorulan
   soruda mı? Kendi çalışmanda bugün sorduğun son üç soruyu bu ölçüte vur — hangisi hangisiydi?
3. Haftalık ritimdeki **AI'sız yarım gün** neden var? Bu bir nostalji ritüeli mi, yoksa ölçülebilir
   bir amacı mı var — ve kaldırırsan ilk ne kaybolur?
4. Cuma retrospektifindeki soru neden *"ne yaptın"* değil de *"AI olmadan yapamayacağın ne öğrendin"*?
   Bu iki sorunun ortaya çıkardığı şey nasıl farklılaşır?
5. Perşembe ritüelinde mentor "burayı bana anlat" diyor. Bu, kodu incelemekten neyi farklı yoklar?
   Junior'ın PR'ı temizse ama anlatamıyorsa **ne** öğrenilmiştir?
6. `AGENTS.md`'nin "ihtimali azaltır", politika+sandbox'ın "korur" olması ne demek? Bu ayrımı
   Hafta 4'teki ArchUnit kuralıyla eşle: `claude_yalniz_ai_paketinden` hangi kategoriye girer — rica mı, duvar mı?
7. Otonomi sınırındaki üç işlem (şema göçü, dış sözleşme, veri silme) neden özellikle bunlar?
   Ortak özellikleri ne — ve bu projeye dördüncü bir madde eklesen ne olurdu, neden?
8. Ekibin "ajan zaten AGENTS.md'yi okuyor, sandbox'a gerek yok" diyor. Bu cümledeki mantık hatası ne?
   Karşı argümanını tek cümleyle nasıl kurarsın?
9. Bir junior "AI ile 3 kat hızlıyım" diyor. Bu iddiayı **kanıt** olarak kabul eder misin?
   Etmiyorsan, yerine ne ölçerdin? (Cevabı yokladıktan sonra `ai-verimlilik-olcumu` kaynağına bak.)
10. Bu haftanın üç resmi (körelme · ritim · sınır) tek bir liderlik ilkesinde birleşiyor. O ilkeyi
    kendi cümlenle yaz — ve mimari kata'nda (RFP) nerede işine yarayacağını göster.
