---
slug: bankada-davranissal-ve-kariyer-sorulari
baslik: "Bankada Davranışsal ve Kariyer Soruları: STAR, Zor Sorular ve Dürüst Köprü Kurmak"
kademe: Orta
sira: 242
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Banka bir krediyi verirken geleceği göremez. Onun yerine **geçmiş ödeme davranışına** bakar. Elindeki en iyi tahmin aracı budur: insanın daha önce ne yaptığı, bundan sonra ne yapacağının en güvenilir habercisidir. Davranışsal mülakat da aynı mantıkla çalışır. Yazılım müdürü sana "zor bir kararı anlat", "üretimde hata yaptığın bir anı anlat", "ekipte anlaşmazlık yaşadın mı" diye sorar. Bunu yaparken kişilik testi yapmıyor. **Geçmiş davranışından gelecekteki davranışını skorluyor.** Bu yüzden davranışsal kısım, teknik kısmın "yumuşak" eki değildir. Para akışına dokunan bir ekipte güvenilirlik, en az algoritma bilgisi kadar belirleyicidir.

Bu konu, teknik mülakatın davranışsal ve kariyer sorularına odaklanır. Üç şey öğreneceksin: hikâyeni nasıl kuracağın, zor soruları **dürüstlükten ödün vermeden** nasıl cevaplayacağın ve bankacılık ortamının hangi davranışları beklediği. Zor sorular şunlardır: neden ayrıldın, bankacılık deneyimin yok, maaş beklentin ne.

### Temel kavramlar

Hiç duymadığını varsayarak:

- **Davranışsal soru (behavioral question):** geçmişten **somut bir olay** isteyen sorudur. Kalıbı şudur: "Bir zamanlar ... yaptığın bir durumu anlat". "Ne yapardın" değil, "ne yaptın" sorulur.
- **STAR yöntemi:** cevabı dörde bölen anlatım iskeletidir. **Durum** (Situation): bağlam neydi. **Görev** (Task): senin sorumluluğun neydi. **Eylem** (Action): **sen** ne yaptın. **Sonuç** (Result): ne değişti, hangi sayı iyileşti, ne öğrendin.
- **Doksan saniye disiplini:** bir davranışsal cevabın ideal uzunluğu kabaca 60-120 saniyedir. Daha kısası bilgi vermez, daha uzunu dinleyeni kaybeder.
- **Dürüst köprü kurma:** olmayan deneyimi uydurmamaktır. Bunun yerine "bunu yapmadım" der, sonra **en yakın gerçek deneyimini** o alana bağlarsın.
- **Görevler ayrılığı (segregation of duties):** kodu yazan kişi, o kodu üretime tek başına çıkaramaz; üretim verisini de tek başına değiştiremez. Amacı hem hatayı hem kötüye kullanımı engellemektir.
- **Değişiklik yönetimi (change management):** üretime çıkacak her değişikliğin kaydı, onayı ve planlı zamanı olur. Ayrıca bir **geri alma (rollback)** planı bulunur.
- **Denetim izi (audit trail):** sonradan tek bir soruyu cevaplayabilmek için tutulan kayıttır. Soru şudur: kim, ne zaman, neyi, hangi yetkiyle değiştirdi?
- **Olay bildirim zinciri:** üretimde bir sorun çıktığında kimin, kime, hangi sırayla haber vereceğinin önceden yazılı olmasıdır.

### Nasıl çalışır?

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Doksan saniyelik STAR cevabının zaman bütçesi">
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Bir davranışsal cevabın zaman bütçesi (~90 saniye)</text>
  <rect x="30" y="48" width="100" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Durum</text>
  <text x="80" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~15 sn</text>
  <rect x="130" y="48" width="67" height="52" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="163" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Görev</text>
  <text x="163" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~10 sn</text>
  <rect x="197" y="48" width="300" height="52" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="347" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Eylem — SEN ne yaptın</text>
  <text x="347" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~45 sn · cevabın ağırlık merkezi</text>
  <rect x="497" y="48" width="133" height="52" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="563" y="70" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Sonuç</text>
  <text x="563" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">~20 sn</text>
  <rect x="30" y="118" width="300" height="74" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="180" y="140" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">En sık yapılan iki hata</text>
  <text x="180" y="160" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">1) Durum'u uzatıp 4 dakikaya yayılmak</text>
  <text x="180" y="178" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2) Sonuç'u hiç söylememek</text>
  <rect x="346" y="118" width="284" height="74" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="488" y="140" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Sonuç bölümü ne olabilir?</text>
  <text x="488" y="160" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sayı (hata %60 azaldı, süre yarıya indi)</text>
  <text x="488" y="178" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ya da öğrenilen ders (başarısızlık sorusunda)</text>
</svg>
```

**STAR ve doksan saniye.** STAR işe yarar, çünkü dinleyene bir raf sistemi verir. Bağlamı, sorumluluğu, eylemi ve çıktıyı ayrı ayrı yerleştirir. Böylece dağınık bir anlatıda kaybolan "peki sen ne yaptın" sorusu kendiliğinden cevaplanır. Uygulamada iki hata her şeyi bozar. Birincisi **Durum'u uzatmaktır**: projenin tarihçesini anlatırsın, cevabın asıl sinyali olan eylem kısmına hiç gelemezsin. İkincisi ve daha sık olanı **Sonuç'u atlamaktır**. Hikâye "sonra düzelttik" diye biter, ne değiştiği söylenmez.

Sonuç mutlaka sayı olmak zorunda değildir. Ölçemediğin bir işte somut bir çıktı da yeter: "artık aynı hata tekrar etmedi" ya da "kod incelemesi süremiz kısaldı" gibi. Başarısızlık sorularında **öğrenilen ders** de sonuç sayılır. Bir de dil meselesi var. Eylem kısmını "biz yaptık" diye anlatırsan katkın görünmez olur. Orada birinci tekil şahıs kullan: "ben önerdim", "ben yazdım".

**"Kendinizi tanıtır mısınız?"** Bu soru ısınma değildir, çerçeve kurma fırsatıdır. Mülakatın geri kalanı çoğu zaman senin burada attığın kancalardan devam eder. İki dakikalık, dört parçalı bir iskelet yeterlidir. (1) **Şu an ne yapıyorum, neyle uğraşıyorum.** (2) **Hangi teknolojilerle çalıştım** — liste değil, en çok kullandığın üç-dört tanesi ve nerede kullandığın. (3) **En güçlü olduğum alan** — bir cümlelik iddia ve onu destekleyen tek somut örnek. (4) **Neden buradayım** — bu pozisyonla kendi yönün arasındaki bağ. Özgeçmişi kronolojik olarak baştan sona okumak en zayıf versiyondur; karşındaki onu zaten okudu.

**"Neden ayrıldınız / işten çıkarıldınız?"** Türkiye'de ekonomik daralma, ekip kapanması ve yeniden yapılanma yüzünden işten çıkarılmak yaygındır. Deneyimli bir yazılım müdürü bunu bilir; durum **damgalayıcı değildir**. Damgalayıcı olan, bu soruya verilen kötü cevaptır.

Üç tuzak vardır. Birincisi eski işvereni kötülemektir; karşındaki "bir sonraki işinde bizi böyle anlatır" diye düşünür. İkincisi savunmaya geçip uzun uzun haklılığını ispatlamaya çalışmaktır; bu, soruyu senin en hassas noktan hâline getirir. Üçüncüsü kendini gereğinden fazla suçlamaktır. Doğru cevap **kısa, nötr ve ileri bakan** bir cevaptır. Ne olduğunu bir-iki cümleyle olgusal biçimde söyle, kimseyi suçlama, sonra hızla şimdiki hedefine dön. Yalan söylemek burada özellikle risklidir: bankalarda referans ve geçmiş kontrolü olağan bir adımdır.

**"Neden bankacılık / neden bu iş?"** "Kurumsal bir yapıda çalışmak istiyorum" gibi her yere yapışan cevaplar sinyal taşımaz. Somut bir bağ kur. Para akışına dokunan sistemlerde **doğruluk ve dayanıklılık** ödün verilemez niteliktedir. Bir tutarın bir kuruş sapması kabul edilebilir hata değildir; bir işlemin iki kez işlenmesi de öyle. İşlem bütünlüğü, hata toleransı ve tekrar çalıştırılabilir toplu işler seni gerçekten ilgilendiriyorsa bunu söyle. İlgi alanın ile işin doğası arasındaki bu örtüşme yeterli bir gerekçedir. Uydurma bir tutku anlatmana gerek yok; gerçek olan bağ yeter.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uydurma yolu ile dürüst köprü kurma yolunun karşılaştırması">
  <defs>
    <marker id="bankadav-ok" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
    <marker id="bankadav-okk" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#B23B3B"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"Kredi ekranı yaptınız mı?" — iki yol</text>
  <rect x="20" y="42" width="620" height="88" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="120" y="66" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">"Evet, benzerini yaptım"</text>
  <text x="120" y="86" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">uydurma / şişirme</text>
  <line x1="222" y1="80" x2="262" y2="80" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#bankadav-okk)"/>
  <text x="372" y="72" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tek bir ayrıntı sorusu: "tahakkukta yuvarlama nasıldı?"</text>
  <text x="372" y="92" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">cevap gelmez → anlatılan her şey şüpheli hale gelir</text>
  <line x1="488" y1="80" x2="528" y2="80" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#bankadav-okk)"/>
  <text x="586" y="76" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">güven biter</text>
  <text x="586" y="96" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">telafisi yok</text>
  <rect x="20" y="146" width="620" height="106" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="34" y="164" width="180" height="72" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="124" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">1 · Açıkça söyle</text>
  <text x="124" y="206" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"Kredi ekranı yapmadım."</text>
  <text x="124" y="224" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">özür dileme, uzatma</text>
  <line x1="216" y1="200" x2="238" y2="200" stroke="#5D7789" stroke-width="1.5" marker-end="url(#bankadav-ok)"/>
  <rect x="242" y="164" width="196" height="72" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="340" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · En yakın gerçek deneyim</text>
  <text x="340" y="206" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">işlem bütünlüğü · tutar hassasiyeti</text>
  <text x="340" y="224" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">eşzamanlılık · denetim izi</text>
  <line x1="440" y1="200" x2="462" y2="200" stroke="#5D7789" stroke-width="1.5" marker-end="url(#bankadav-ok)"/>
  <rect x="466" y="164" width="160" height="72" rx="8" fill="#FFFFFF" stroke="#25804C" stroke-width="1.2"/>
  <text x="546" y="186" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">3 · Öğrenme planı</text>
  <text x="546" y="206" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">"alan bilgisini nasıl</text>
  <text x="546" y="224" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kapatırım" — somut</text>
</svg>
```

**Dürüst köprü kurma.** Bu pozisyonun en olası zor sorusu şudur: "Bankacılık deneyiminiz yok, kredi ekranları yapmadınız; nasıl uyum sağlarsınız?" Buradaki en büyük risk, sıkışıp deneyimi varmış gibi göstermektir. Sebebi basit: alan bilgisi sorularının **derinliği sınırsızdır**. "Evet, benzerini yaptım" dediğin anda karşındaki tek bir ayrıntı sorar. Örneğin taksit tahakkukunda yuvarlamanın nasıl yapıldığını ya da erken kapamada faiz iadesinin nasıl hesaplandığını sorar. Cevap gelmediğinde kaybettiğin şey o soru değildir; **o ana kadar anlattığın her şeyin güvenilirliğidir.**

Üç adımlı köprü çok daha güçlüdür. (1) Yapmadığını net ve özür dilemeden söyle. (2) En yakın **gerçek** deneyimini anlat. Şu konular bankacılığa doğrudan transfer olur: para ve tutar hassasiyeti, işlem bütünlüğü (bir işin ya tamamen olması ya hiç olmaması), aynı kaydın iki kez işlenmesini engelleme, eşzamanlı erişim, kim ne zaman ne değiştirdi kaydı. (3) Alan bilgisini nasıl kapatacağını somutla. Örnek cümle: "Ürün dokümanını ve mevcut akışı okuyup, ilk haftada bir başvuru vakasını uçtan uca izlemek isterim." Bu cevap "bilmiyorum" değildir; **bildiğini alana bağlayan** bir cevaptır. Alan bilgisi öğrenilebilir, güvenilirlik geri kazanılamaz.

**Klasik davranışsal set.** Hemen her mülakatta şu altısından en az ikisi gelir: *ekipte yaşadığın bir anlaşmazlık*, *ürettiğin bir üretim hatası*, *aldığın zor bir teknik karar*, *kod incelemesinde (code review) sert eleştiri aldığın bir an*, *sana kalsa çalıştığın sistemde neyi değiştirirdin*, *baskı altında çalıştığın bir dönem*. Bunlar için mülakat sırasında hikâye icat edilmez. Önceden **üç-dört gerçek olay** seçilir, STAR ile hazırlanır, sonra soruya göre uyarlanır.

Her hikâyede aranan sinyal farklıdır. Anlaşmazlık hikâyesinde aranan, kimin haklı çıktığı değildir; tartışmayı kişiselleştirmeden nesnelleştirebilmendir. Kod incelemesi hikâyesinde aranan, eleştiriyi savunmaya geçmeden alabilmen ve gerektiğinde "haklıydı" diyebilmendir. Kendi ürettiğin hata hikâyesinde en güçlü versiyon, hatayı sahiplenip **tekrarını önleyen** kalıcı adımı anlatandır.

```svg
<svg viewBox="0 0 660 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Üretimde hata çıktığında izlenecek sıra ve bankaya özgü paralel adımlar">
  <defs>
    <marker id="bankadav-akis" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">"Üretimde hata çıktı, ne yaparsın?" — sıra önemlidir</text>
  <rect x="16" y="46" width="112" height="70" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="72" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · Etkiyi durdur</text>
  <text x="72" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">geri al / kapat</text>
  <text x="72" y="107" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kanamayı kes</text>
  <line x1="130" y1="81" x2="143" y2="81" stroke="#5D7789" stroke-width="1.5" marker-end="url(#bankadav-akis)"/>
  <rect x="145" y="46" width="112" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="201" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 · Kök neden</text>
  <text x="201" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">log, veri, zaman</text>
  <text x="201" y="107" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">çizelgesi</text>
  <line x1="259" y1="81" x2="272" y2="81" stroke="#5D7789" stroke-width="1.5" marker-end="url(#bankadav-akis)"/>
  <rect x="274" y="46" width="112" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · Kalıcı düzelt</text>
  <text x="330" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">yama değil</text>
  <text x="330" y="107" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">gerçek düzeltme</text>
  <line x1="388" y1="81" x2="401" y2="81" stroke="#5D7789" stroke-width="1.5" marker-end="url(#bankadav-akis)"/>
  <rect x="403" y="46" width="112" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="459" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">4 · Tekrarı önle</text>
  <text x="459" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">test + uyarı</text>
  <text x="459" y="107" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">(alarm) ekle</text>
  <line x1="517" y1="81" x2="530" y2="81" stroke="#5D7789" stroke-width="1.5" marker-end="url(#bankadav-akis)"/>
  <rect x="532" y="46" width="112" height="70" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="588" y="70" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">5 · Değerlendirme</text>
  <text x="588" y="90" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">suçlamadan</text>
  <text x="588" y="107" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sistemi sorgula</text>
  <rect x="16" y="134" width="628" height="80" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="158" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Bankada bunlara paralel yürüyen üç şey</text>
  <text x="330" y="180" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">olay kaydının açılması · bildirim zincirinin işletilmesi (yönetici, operasyon, gerekiyorsa iş birimi)</text>
  <text x="330" y="200" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">her müdahalenin denetim izine yazılması — üretim verisine tek başına, kayıtsız dokunulmaz</text>
</svg>
```

**"Üretimde hata çıktı, ne yaparsın?"** Bu soruda ölçülen şey teknik çözüm değil, **sıralamadır**. Doğru sıra beş adımdır. Önce **etkiyi durdur**: değişikliği geri al, işi durdur, akışı kapat — yani kanamayı kes. Sonra **kök nedeni bul**: log kaydı, veri ve zaman çizelgesi. Sonra **kalıcı düzeltmeyi** yap; geçici yama ile bırakma. Sonra **tekrarını önle**: bu senaryoyu yakalayan bir test ve gerçekleşirse haber veren bir uyarı ekle. En sonunda **suçlamadan değerlendirme** yap. Soru "kim yaptı" değil, "bu hatanın üretime çıkmasına sistem nasıl izin verdi" olmalıdır.

Bankada bu akışın üç ek boyutu vardır ve bunları söylemen olgunluk sinyalidir. Birincisi olay kaydının açılmasıdır. İkincisi **bildirim zincirinin** işletilmesidir; sorunu sessizce düzeltip geçmek kabul edilmez, para etkisi varsa iş birimi de bilmelidir. Üçüncüsü her müdahalenin **denetim izine** düşmesidir. "Üretim veritabanına bağlanıp tutarları elle düzelttim" cümlesi teknik olarak sorunu çözse bile, bir bankada büyük kırmızı bayraktır.

**Bankanın beklediği davranışlar.** Kurumsal bankacılıkta yazılım süreci bilinçli olarak yavaştır. Çünkü optimize edilen şey hız değil, **hatanın maliyetidir**. Bir sosyal medya uygulamasında yanlış giden dağıtım kullanıcıyı rahatsız eder. Kredi sisteminde yanlış giden bir tahakkuk ise binlerce müşterinin borcunu bozar, geri almak günler sürer ve düzenleyici tarafı vardır. Bu yüzden "hızlı hareket et, bir şeyler kırılsın" kültürü burada geçerli değildir.

Mülakatta değer gören davranışlar şunlardır. **Değişiklik yönetimine saygı**: planlı çıkış, geri alma planı, kayıt. **Görevler ayrılığını anlamak**: kodu yazan kişinin üretime tek başına çıkaramaması bir güvensizlik işareti değildir, hem hataya hem kötüye kullanıma karşı bir kontroldür. **Gizlilik**: üretim verisi — müşteri adı, kimlik, bakiye — geliştirme ortamına kopyalanmaz; hata ayıklamak için maskelenmiş veya üretilmiş veri kullanılır. Bunlara **belgeleme alışkanlığı** ve düzenleme farkındalığı eklenir.

En güçlü duruş ikili bir duruştur. Bu süreçleri "bürokrasi" diye eleştirme, gerekçesini anladığını göster. Ama aynı zamanda süreci **hızlandıracak otomasyonu** önerebil: otomatik testler, tekrarlanabilir dağıtım paketi, hazır geri alma adımı. Böylece kontrolü kaldırmayı değil, kontrolün maliyetini düşürmeyi önermiş olursun.

**"Yapay zekâ kullanıyor musun?"** 2026'da bu soru neredeyse standarttır. Doğru cevap ne "hayır, her satırı kendim yazarım" ne de "artık kodun çoğunu o yazıyor"dur. Birincisi inandırıcı değildir; ikincisi sorumluluğu devrettiğin izlenimi verir.

Olgun cevap şudur: kullanıyorum, ama **çıktının sorumluluğu bende**. Üretilen kodu anlamadan koda almam; gözden geçiririm, testini yazarım, üretilen açıklamaları doğrularım. Ayrıca müşteri verisi veya kurum içi gizli bilgi hiçbir dış araca gönderilmez. Bu konunun ayrıntısı iki ayrı konuda işleniyor. Yapay zekâ çıktısının hesap verebilirliği ve kod incelemesi kültürü için **207**'ye bak; günlük geliştirme akışına oturtulması için **175**'e bak. Buradaki tek mesaj şudur: araç kullanmak zayıflık değildir, **doğrulamadan güvenmek** zayıflıktır.

**Kendi projeni anlatmak.** İşten çıkarılmış bir adayın kendi yazdığı bir projesi olması değerlidir. Ama dikkat: yan proje mülakatta **iş deneyiminin yerine geçmez**, geçtiğini iddia etmek de zayıf görünür. Onun yerine ne işe yaradığını doğru konumla: yan proje senin **düşünme biçimini** gösterir.

Anlatırken teknoloji listesi saymak en zayıf versiyondur. "Spring Boot, H2, mimari testler, bir dil modeli entegrasyonu" demek bir alışveriş listesidir; karar içermez. Güçlü versiyon dört adımlıdır: **problem** (neyi çözmeye çalıştım), **seçim** (hangi yolu seçtim), **gerekçe** (neden onu seçtim, alternatif neydi), **ödün** (bu seçimin bedeli neydi, neyi feda ettim).

Somut bir örnek verelim. Katmanlar arası bağımlılığı otomatik denetleyen mimari testler koyduysan, ilginç olan testin varlığı değildir. İlginç olan şu cümledir: "Ekip büyümeden önce katman ihlallerini derleme zamanında yakalamak istedim, karşılığında biraz esneklik kaybettim." Bir de dürüstlük payı bırak. Projenin gerçek sınırlarını kendin söylemek güven artırır: tek kullanıcı, gerçek yük altında denenmedi.

**Maaş ve beklenti sorusu.** Mülakatta ilk sayıyı söylemek zorunda değilsin. "Önce rolün kapsamını netleştirip konuşmayı tercih ederim" demek meşrudur ve genellikle kabul edilir. Israr edilirse tek bir sayı verme, **araştırmaya dayalı bir aralık** ver. Aralığı, benzer deneyim ve pozisyonlar için piyasada gördüğün seviyeye dayandır. Alt sınırını da **gerçekten kabul edebileceğin bir sayı** olarak seç; çünkü aralığın alt ucu, karşı tarafın duyduğu tek sayı olma eğilimindedir.

Gerçeği de kabul et: acil iş ihtiyacı pazarlık gücünü düşürür, bu matematiksel bir durumdur. Ama bunu masaya koymak pazarlık gücünü sıfırlar. "İşsizim, ne verirseniz kabul ederim" demek, değer algısını da düşürür. Panik gösterme; esnekliğini profesyonel bir çerçevede ifade et: "toplam paket ve rolün kapsamına göre değerlendiririm". Ayrıca birden fazla süreci paralel yürüt. Tek bir teklife mahkûm olmamanın en gerçekçi yolu budur.

**Senin soracağın sorular.** "Sorunuz var mı?" bir nezaket sorusu değildir, **değerlendirmenin bir kalemidir**. Hazırlık düzeyini, işi ne kadar ciddiye aldığını ve neye önem verdiğini gösterir. "Sorum yok" cevabı ilgisizlik olarak okunur. İnternetten bulunabilecek şeyleri sorma; işin gerçeğine dokunan şeyleri sor.

Örnek sorular: ekibin gündelik akışı, kod incelemesi ve test alışkanlığı, üretime çıkış sıklığı ve süreci, yeni gelen birinin ilk üç ayda neyi başarmış olması beklendiği, bu rolde başarının nasıl ölçüldüğü, ekibin en çok zorlandığı teknik konu. Bu sorular aynı zamanda **senin** için bilgi toplar. İş arıyor olmak, her işin sana uygun olduğu anlamına gelmez.

### Mülakatta ne sorulur / tuzaklar

Beklenecek sorular: kendini tanıtma, ayrılık nedeni, neden bankacılık, bankacılık deneyiminin olmaması, bir çatışma, bir üretim hatası, zor bir karar, eleştiri alma, yapay zekâ kullanımı, kendi projen, maaş beklentisi ve senin soruların.

Sık düşülen tuzaklar üç kümede toplanır. Anlatım tuzakları: **hikâye icat etmek** (bir ayrıntı sorusuyla çöker), Sonuç bölümü olmayan hikâyeler, "biz" dili, cevabı dört dakikaya yaymak. Tutum tuzakları: eski işvereni kötülemek, süreç ve kontrolleri "bürokrasi" diye küçümsemek, üretim verisine erişim konusunda rahat konuşmak. Konumlandırma tuzakları: yapay zekâ sorusuna ya inkârla ya da sorumluluğu devrederek cevap vermek, yan projeyi iş deneyimi gibi sunmak, aciliyeti pazarlık masasına koymak ve "sorum yok" demek. Ortak payda tek bir ilkede toplanır: **abartma da, küçültme de yapma; olduğun yeri net söyle ve oradan köprü kur.**

### Tek cümlede

Bankada davranışsal mülakat üç şeyi ölçer: gerçek olayları STAR ile doksan saniyede sonucuyla birlikte anlatabilmeni, olmayan deneyimi uydurmak yerine en yakın gerçek deneyimine köprü kurabilmeni ve süreç, gizlilik ve denetim kontrollerinin neden var olduğunu anladığını gösterebilmeni.

# davranis-1 | Temel
## Soru
Yazılım müdürü "Önceki işinizden neden ayrıldınız?" diye soruyor. Sen ekonomik nedenlerle yapılan bir küçülmede işten çıkarıldın. Hangi cevap stratejisi en sağlamdır?
## A
Şirketin ve yöneticinin yaptığı hataları ayrıntılı anlatıp kararın ne kadar haksız olduğunu açıklamak; böylece suçun sende olmadığı anlaşılır
🤖 eski işvereni kötülemek karşı tarafta "bir sonraki işinde bizi de böyle anlatır" izlenimi bırakır ve odağı senden uzaklaştırır.
## B
Kendi isteğimle ayrıldım diyerek çıkarılma bilgisini gizlemek; çıkarılmış olmak olumsuz bir etiket yaratır
🤖 bankalarda referans ve geçmiş kontrolü olağandır; yakalanan tek bir yalan tüm anlatılanların güvenilirliğini yok eder.
## C
Ne olduğunu bir-iki cümleyle olgusal ve nötr biçimde söyleyip kimseyi suçlamadan hızla şimdiki hedefine ve bu pozisyona dönmek
🤖 Doğru: kısa, nötr ve ileri bakan cevap konuyu kapatır ve kontrolü sana bırakır.
## D
Uzun uzun kendini sorgulayıp yeterince iyi olmadığını, daha çok çalışması gerektiğini anlatmak; alçakgönüllülük olumlu karşılanır
🤖 gereğinden fazla kendini suçlamak alçakgönüllülük değil özgüven eksikliği olarak okunur ve soruyu senin zayıf noktan haline getirir.
## Doğru
C
## Açıklama
Ekonomik daralma ve yeniden yapılanma nedeniyle işten çıkarılmak Türkiye'de yaygındır ve deneyimli bir yazılım müdürü bunu bilir; **damgalayıcı olan durum değil, verilen kötü cevaptır**. Doğru cevap üç özelliğe sahiptir: **kısa** (konuyu büyütmez), **nötr** (kimseyi suçlamaz) ve **ileri bakar** (hemen şimdiki hedefe döner). A eski işvereni kötüler ve karşı tarafta gelecekte senin de onları böyle anlatacağın izlenimini bırakır. B doğrudan yalandır ve bankacılıkta referans kontrolü olağan bir adım olduğu için yüksek risklidir; yakalanan bir yalan sadece o konuyu değil **anlattığın her şeyin güvenilirliğini** yok eder. D ise aşırı kendini suçlamayla özgüven eksikliği sinyali verir ve mülakatçının odağını senin en hassas noktana kilitler.

# davranis-2 | Orta
## Soru
Mülakatçı "Bankacılık deneyiminiz yok, kredi ekranlarının arka ucunu hiç yazmamışsınız; nasıl uyum sağlarsınız?" diyor. En güçlü cevap stratejisi hangisidir?
## A
Kredi ekranı yazmadığını açıkça söyleyip en yakın gerçek deneyimini (işlem bütünlüğü, tutar hassasiyeti, eşzamanlılık, denetim izi) anlatmak ve alan bilgisini nasıl kapatacağını somutlamak
🤖 Doğru: dürüst köprü hem güvenilirliği korur hem transfer edilebilir yetkinliği görünür kılar.
## B
"Aslında çok benzerini yaptım" deyip anlatımı genel tutmak; alan farkı zaten kısa sürede kapanır, gereksiz yere zayıf görünmeye gerek yok
🤖 alan sorularının derinliği sınırsızdır; tek bir ayrıntı sorusu cevapsız kalınca o ana kadar anlattığın her şey şüpheli hale gelir.
## C
Kısaca "Hayır, kredi ekranı yapmadım" deyip cevabı orada bitirmek; dürüst olmak yeterlidir, fazlasını söylemek savunmaya geçmek olur
🤖 dürüstlük gereklidir ama tek başına yetmez; köprü kurulmayınca mülakatçının elinde yalnızca eksiğin kalır.
## D
Soruyu geçiştirip bildiğin teknolojileri (Spring Boot, veri tabanı, konteyner) sıralamak; teknik yetkinlik alan bilgisinden daha belirleyicidir
🤖 teknoloji listesi sorulan soruya cevap vermez ve karar içermediği için düşünme biçimini de göstermez.
## Doğru
A
## Açıklama
Bu sorunun asıl testi alan bilgisi değil **dürüstlüktür**. Uydurmanın (B) riski asimetriktir: kazancın küçük, kaybın toplamdır — çünkü "taksit tahakkukunda yuvarlama nasıl yapılıyordu" gibi tek bir ayrıntı sorusu cevapsız kaldığında kaybettiğin şey o soru değil, **anlattığın her şeyin güvenilirliğidir**. Doğru yaklaşım üç adımlı köprüdür: (1) yapmadığını özür dilemeden net söyle; (2) en yakın **gerçek** deneyimini anlat — bir işin ya tamamen olması ya hiç olmaması, aynı kaydın iki kez işlenmesini engelleme, para/tutar hassasiyeti, eşzamanlı erişim, kim ne zaman neyi değiştirdi kaydı gibi konular bankacılığa doğrudan transfer olur; (3) alan bilgisini nasıl öğreneceğini somutla. C dürüsttür ama yarım kalır: köprü kurulmadığı için geriye yalnız eksik kalır. D ise soruyu cevaplamayan bir teknoloji listesidir. Özet: **alan bilgisi öğrenilebilir, güvenilirlik geri kazanılamaz.**

# davranis-3 | Orta
## Soru
"Geliştirirken yapay zekâ araçları kullanıyor musunuz?" sorusuna hangi cevap en olgun sinyali verir?
## A
"Hayır, kullanmıyorum; her satırı kendim yazarım" demek; araç kullandığını söylemek yetkinliğinin sorgulanmasına yol açar
🤖 2026'da bu cevap hem inandırıcı değildir hem de araçla çalışma disiplinin hakkında hiçbir şey göstermez.
## B
"Evet, kodun çoğunu artık o yazıyor; ben de hızlıca geçip birleştiriyorum" demek; verimlilik artışını vurgulamak güçlü bir izlenim bırakır
🤖 doğrulamadan birleştirmek sorumluluğu araca devretmek demektir ve özellikle para işleyen sistemlerde ciddi bir risk sinyalidir.
## C
"Şirket politikası neyse ona uyarım" deyip konuyu kapatmak; kendi tercihini söylemek gereksiz risk yaratır
🤖 kaçamak cevap kişisel çalışma disiplinini gizler ve soruyu sorulmamış gibi bırakır.
## D
Kullandığını açıkça söyleyip çıktının sorumluluğunun sende olduğunu vurgulamak: anlamadan koda almamak, gözden geçirmek, testini yazmak ve gizli veriyi dış araca göndermemek
🤖 Doğru: araç kullanımını kabul edip doğrulama ve gizlilik disiplinini göstermek hesap verebilirlik sinyalidir.
## Doğru
D
## Açıklama
2026'da bu soru neredeyse standarttır ve ölçtüğü şey aracın kendisi değil, **hesap verebilirliktir**. A inandırıcı değildir ve inandırıcı olsa bile araçla çalışırken hangi disipline sahip olduğunu göstermez. B en tehlikeli cevaptır: doğrulamadan birleştirmek, üretilen kodun sonuçlarını üstlenmediğin anlamına gelir — para akışına dokunan bir sistemde bu doğrudan risk sinyalidir. C kaçamaktır; kişisel çalışma biçimini gizlediği için soru cevapsız kalır. Doğru duruş nettir: **kullanmak zayıflık değildir, doğrulamadan güvenmek zayıflıktır.** Somutlaştır: üretilen kodu anlamadan koda almam, gözden geçiririm, testini yazarım, ürettiği açıklamaları doğrularım; ayrıca müşteri verisi ve kurum içi gizli bilgi dış araçlara gönderilmez. Bu konunun ayrıntısı **207** (yapay zekâ hesap verebilirliği ve kod incelemesi kültürü) ve **175** (yapay zekâ destekli geliştirme akışı) konularında işlenir.

# davranis-4 | İleri
## Soru
Gece yarısı, kredi taksitlerini hesaplayan gün sonu toplu işinin bir kısım müşteriye yanlış tutar yazdığı fark ediliyor. "Ne yaparsın?" sorusuna hangi cevap doğru sıralamayı ve bankacılık olgunluğunu birlikte gösterir?
## A
Önce kök nedeni tam olarak anlamaya odaklanmak; neyin bozulduğu netleşmeden hiçbir şeye dokunmamak, aksi halde durumu daha da karıştırma riski vardır
🤖 kök neden analizi şarttır ama sırası ikincidir; yanlış tutar yazmaya devam eden bir iş çalışırken analiz süresi zarar olarak birikir.
## B
Önce etkiyi durdurup değişikliği geri almak, olay kaydını açıp bildirim zincirini işletmek; ardından kök neden, kalıcı düzeltme, tekrarı önleyecek test ve uyarı, en sonunda suçlamadan değerlendirme
🤖 Doğru: önce kanamayı kesip olayı kayda alan, sonra kalıcı düzeltme ve önlemeye giden sıra hem teknik hem kurumsal olarak doğrudur.
## C
Üretim veritabanına bağlanıp yanlış yazılan tutarları hemen elle düzeltmek ve iş sabah normal görünsün diye durumu ayrıca bildirmemek
🤖 üretim verisine tek başına ve kayıtsız müdahale görevler ayrılığını ve denetim izini ihlal eder; sorunu gizlemek ise ayrı bir ihlaldir.
## D
Sabahı beklemek; gece yapılan müdahaleler hata riskini artırır, toplu iş zaten ertesi gün yeniden koşacağı için sorun kendiliğinden düzelir
🤖 yanlış tutarlar müşteriye yansımaya devam eder ve toplu işin tekrar koşması bozulmuş veriyi kendiliğinden düzeltmez.
## Doğru
B
## Açıklama
Bu soruda ölçülen şey çözümün kendisi değil **sıralamadır**: önce **etkiyi durdur** (kanamayı kes), sonra **kök nedeni bul**, sonra **kalıcı düzeltmeyi** yap, sonra **tekrarını önle** (bu senaryoyu yakalayan test ve gerçekleşirse haber veren uyarı), en sonunda **suçlamadan değerlendirme** — soru "kim yaptı" değil, "bu hata üretime nasıl çıkabildi" olmalıdır. A analizi öne alır ve zararın birikmesine izin verir. D "kendiliğinden düzelir" varsayımına yaslanır; bozulmuş veri tekrar koşmayla düzelmez ve yanlış tutarlar müşteriye yansımaya devam eder. C teknik olarak sorunu bir an için kapatsa da bankada en ağır hatadır: üretim verisine tek başına ve kayıtsız dokunmak **görevler ayrılığını** ve **denetim izini** ihlal eder, üstüne durumu bildirmemek bunu gizlemeye dönüştürür. Bankada doğru akışa paralel olarak olay kaydının açılması, **bildirim zincirinin** işletilmesi ve her müdahalenin denetim izine düşmesi gerekir.

# davranis-5 | İleri
## Soru
Mülakatçı "Bizde geliştiriciler kendi kodlarını üretime çıkaramaz; her değişiklik kayıt, onay ve planlı çıkış gerektirir. Bu konuda ne düşünüyorsunuz?" diye soruyor. Hangi cevap en güçlüdür?
## A
Sürecin yavaş olduğunu, modern ekiplerde geliştiricinin kendi kodunu üretime çıkardığını söyleyip bu kontrolün kaldırılmasını önermek
🤖 kontrolün gerekçesini (hata ve kötüye kullanım riski, denetim) anlamadan kaldırılmasını önermek bağlam körlüğü sinyali verir.
## B
Hiçbir görüş belirtmeden "siz nasıl istiyorsanız öyle çalışırım" demek; süreç tartışmasına girmek yeni gelen biri için risklidir
🤖 tümüyle görüşsüz kalmak uyumluluk değil, konu hakkında düşünmemiş olmak diye okunur ve hiçbir sinyal üretmez.
## C
Zaten üretime hiç dokunmak istemediğini, bu tür sorumluluğun kendisinde olmamasını tercih ettiğini söylemek
🤖 sorumluluktan kaçınma olarak okunur; amaç üretimden uzak durmak değil, üretime güvenli biçimde gidebilmektir.
## D
Kontrolün gerekçesini (görevler ayrılığı, denetim izi, hatanın ve kötüye kullanımın maliyeti) anladığını göstermek ve kontrolü kaldırmak yerine maliyetini düşürecek otomasyonu önermek
🤖 Doğru: gerekçeyi anlayıp süreci hızlandıracak otomasyon önermek hem saygı hem katkı sinyalidir.
## Doğru
D
## Açıklama
Kurumsal bankacılıkta süreç bilinçli olarak yavaştır; optimize edilen şey hız değil **hatanın maliyetidir** — kredi sisteminde yanlış giden bir tahakkuk binlerce müşterinin borcunu bozar ve düzenleyici tarafı vardır. **Görevler ayrılığı** (kodu yazanın onu tek başına üretime çıkaramaması) bir güvensizlik işareti değil, hem hataya hem kötüye kullanıma karşı bir kontroldür; **denetim izi** ise "kim, ne zaman, neyi, hangi yetkiyle değiştirdi" sorusunun sonradan cevaplanabilmesini sağlar. A bu gerekçeleri hiç tartmadan kontrolü kaldırmayı önerir — "hızlı hareket et, bir şeyler kırılsın" yaklaşımının bu ortamda neden geçerli olmadığını kaçırır. B görüşsüzlüğü uyum sanır ama hiçbir sinyal üretmez. C sorumluluktan kaçınmadır. En güçlü duruş D'dir: **kontrolü kaldırmayı değil, kontrolün maliyetini düşürmeyi** önermek — otomatik testler, tekrarlanabilir dağıtım paketi, önceden hazır geri alma adımı. Böylece hem süreci anladığını hem katkı üretebileceğini aynı cümlede göstermiş olursun.

# davranis-6 | İleri
## Soru
İşsizsin ve acil iş arıyorsun. Mülakatın sonunda "Maaş beklentiniz nedir?" sorusu geliyor. Hangi yaklaşım en sağlamdır?
## A
İşsiz olduğunu ve acil ihtiyacını açıkça söyleyip "ne verirseniz kabul ederim" demek; esneklik göstermek işe alınma ihtimalini artırır
🤖 aciliyeti masaya koymak pazarlık gücünü sıfırlar ve adayın kendi değer algısını da düşürür.
## B
İlk sayıyı söylemek zorunda olmadığını bilerek rolün kapsamını netleştirmeyi önermek; ısrar edilirse piyasa araştırmasına dayalı, alt sınırı gerçekten kabul edilebilir bir aralık vermek
🤖 Doğru: araştırmaya dayanan ve alt sınırı bilinçli seçilmiş aralık hem gerçekçi hem savunulabilirdir.
## C
Piyasanın belirgin şekilde üstünde bir sayı söylemek; pazarlık nasılsa aşağı ineceği için yüksek başlamak her zaman avantajlıdır
🤖 gerekçesiz yüksek sayı piyasayı bilmediğin izlenimi verir ve süreci gereksiz yere erken bitirebilir.
## D
Soruyu tümüyle reddedip "bu aşamada maaş konuşmayı doğru bulmuyorum" diyerek konuyu kapatmak
🤖 ısrar karşısında soruyu tümden reddetmek işbirliğine kapalı görünür; ilk sayıyı ertelemek meşrudur, konuşmayı reddetmek değil.
## Doğru
B
## Açıklama
Maaş sorusunda iki gerçeği aynı anda taşıman gerekir. Birincisi: **ilk sayıyı söylemek zorunda değilsin** — "önce rolün kapsamını netleştirip konuşmayı tercih ederim" demek meşrudur. Ama D'deki gibi soruyu tümüyle reddetmek işbirliğine kapalılık olarak okunur; ısrar geldiğinde bir aralık vermek gerekir ve o aralık **araştırmaya dayanmalı**, alt sınırı ise gerçekten kabul edebileceğin bir sayı olmalıdır — çünkü aralığın alt ucu, karşı tarafın duyduğu tek sayı olma eğilimindedir. C gerekçesiz yüksek bir sayıyla piyasayı bilmediğin izlenimi verir. İkinci gerçek: acil iş ihtiyacı pazarlık gücünü nesnel olarak düşürür, bunu inkâr etmenin anlamı yok — ama A'daki gibi **aciliyeti masaya koymak** gücü sıfırlar ve değer algısını da aşağı çeker. Doğru duruş, panik göstermeden esnekliği profesyonel bir çerçevede ifade etmektir ("toplam paket ve rolün kapsamına göre değerlendiririm") ve mümkünse birden fazla süreci paralel yürüterek tek bir teklife mahkûm olmamaktır.
