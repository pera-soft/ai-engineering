---
slug: manuel-test-disiplini
baslik: "Manuel Test Disiplini: Süreci Kurmak, Senaryo ve Hata Raporu Yazmak, İzlenebilirlik ve Bitiş Kararı"
kademe: Orta
sira: 287
etiketler: 11
---

## Konu anlatımı

Bu ilanın en ağır maddesi burasıdır. İlan otomasyon süreçlerinde orta-iyi seviye isterken manuel test süreçlerinde çok iyi seviye istiyor. Sıralama tesadüf değildir. Bu bir otomasyon ilanı değil, test disiplini ilanıdır. Hazırlığın ağırlığı da buraya konur.

Önce yaygın bir yanlış anlamayı temizleyelim. Manuel test, otomasyonu bilmeyenin işi değildir. İkisi farklı iş yapar. Otomasyon **bilinen** bir davranışın hâlâ doğru çalıştığını tekrar tekrar doğrular; ucuzdur, hızlıdır ve yorulmaz. Manuel test ise **bilinmeyeni** arar. Senaryoyu ilk kez koşan insan, senaryoda yazmayan şeyi de görür: tuhaf duran mesajı, mantıksız sıralamayı, iki ekran arasında uyuşmayan tutarı. Kısacası otomasyon dünün doğrusunu korur, manuel test bugünün yanlışını bulur. Bir otomasyon senaryosu ancak birinin daha önce elle keşfettiği bir davranışı kodlar.

Bu ayrımı mülakatın ilk dakikasında kurmak işe yarar. Çünkü karşı taraf manuel testçi ararken çoğu adayın "aslında ben otomasyona geçmek istiyorum" dediğini duymuştur. Sen tersini yapacaksın: manuel testi bir disiplin olarak anlatacaksın.

Aşağıda altı gerçek mülakat sorusu var. Hepsi açık uçlu; şık yok, anlatım var. Her sorunun altında dört parça bulacaksın: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı, çünkü senin adına deneyim uydurulmaz. Onu kendi geçmişinden çıkarman için sorular sorulur; çıkmıyorsa `256`'daki dürüst köprü kurulur.

### "Bir özellik geldi, test sürecini nasıl kurarsın?"

**Soru aslında ne ölçüyor?**

Görüşmeci burada test bilgisi değil, düzen arıyor. Elinde yeni bir iş kalemi olan testçi ne yapar: hemen tıklamaya mı başlar, önce soru mu sorar. Zayıf aday cevaba "senaryoları yazarım" diye başlar. Güçlü aday ilk cümlesinde gereksinimi okumaktan ve belirsizliği sormaktan söz eder. İkinci ayrım önceliklendirmede çıkar. Her şeyi test edecek zaman hiçbir projede yoktur. Bunu kabul edip riske göre sıralayan aday, süreci gerçekten yönetmiş demektir. Ağırlık sıradadır; araç adları burada hiçbir şey kazandırmaz.

**Sözlü cevap iskeleti**

1. **Gereksinimi oku, belirsizliği sor, kabul ölçütünü (acceptance criteria) netleştir.** İlk iş kod değil, metindir. "Bu alan zorunlu mu", "geçmiş tarih girilebilir mi", "yetkisiz kullanıcı ne görür" soruları burada sorulur. Bu aşamada bulunan eksik en ucuz hatadır; henüz kod yazılmamıştır (`228`, `199`).
2. **Riske göre önceliklendir.** Risk iki şeyin çarpımıdır: bozulma olasılığı ve bozulunca oluşacak zarar. Para, yetki ve geri alınamaz işlem içeren akışlar öne alınır. Sıra sezgiden değil bu ölçütten çıkar.
3. **Senaryoları tasarım tekniğiyle yaz.** Denklik sınıfı (equivalence partitioning), sınır değer analizi (boundary value analysis), karar tablosu (decision table) ve durum geçişi (state transition) kullanılır. Sezgiyle yazılan senaryo hep aynı yerlere bakar.
4. **Veri ve ortamı hazırla.** Test verisi işin yarısıdır: uç değerler, çok alfabeli isimler, büyük hacim, eski kayıtlar. Ortam üretime ne kadar benzerse sonuç o kadar anlamlıdır (`234`).
5. **Koş ve kanıt biriktir.** Her senaryonun sonucu kaydedilir: geçti, kaldı, engellendi. Kanıtı olmayan sonuç sonradan savunulamaz; hafızaya dayanan rapor tartışmayı kaybeder.
6. **Raporla ve kapsam dışını yaz.** Neyi test etmediğini söylemek, neyi test ettiğini söylemek kadar önemlidir. Sessizce atlanan alan gizli risktir (`229`).

```svg
<svg viewBox="0 0 660 322" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ozellik geldiginde test surecinin alti adimi ve risk bazli onceliklendirme">
  <defs>
    <marker id="mnl-akis" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Test kodlamadan sonra başlamaz; gereksinimi okuduğun anda başlar</text>
  <rect x="14" y="36" width="196" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="112" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 · Gereksinimi oku</text>
  <text x="112" y="77" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">belirsizliği sor</text>
  <text x="112" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kabul ölçütünü yazdır</text>
  <line x1="212" y1="72" x2="228" y2="72" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mnl-akis)"/>
  <rect x="232" y="36" width="196" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 · Riske göre sırala</text>
  <text x="330" y="77" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">olasılık × zarar</text>
  <text x="330" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">para ve yetki akışları önce</text>
  <line x1="430" y1="72" x2="446" y2="72" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mnl-akis)"/>
  <rect x="450" y="36" width="196" height="72" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="548" y="57" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · Senaryoyu tasarla</text>
  <text x="548" y="77" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">denklik sınıfı, sınır değer</text>
  <text x="548" y="95" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">karar tablosu, durum geçişi</text>
  <path d="M548 110 L548 116 L112 116 L112 121" fill="none" stroke="#5D7789" stroke-width="1.4" stroke-dasharray="4 3" marker-end="url(#mnl-akis)"/>
  <rect x="14" y="124" width="196" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="112" y="145" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">4 · Veri ve ortam</text>
  <text x="112" y="165" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uç değer, büyük hacim</text>
  <text x="112" y="183" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ortam üretime benzesin</text>
  <line x1="212" y1="160" x2="228" y2="160" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mnl-akis)"/>
  <rect x="232" y="124" width="196" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="330" y="145" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">5 · Koş ve kanıt biriktir</text>
  <text x="330" y="165" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geçti / kaldı / engellendi</text>
  <text x="330" y="183" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ekran görüntüsü ve log</text>
  <line x1="430" y1="160" x2="446" y2="160" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mnl-akis)"/>
  <rect x="450" y="124" width="196" height="72" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="548" y="145" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">6 · Raporla ve kapat</text>
  <text x="548" y="165" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çıkış ölçütü karşılandı mı</text>
  <text x="548" y="183" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kapsam dışını yazılı bırak</text>
  <rect x="14" y="212" width="632" height="98" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="234" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Risk bazlı önceliklendirme: her şeyi test edemezsin</text>
  <text x="330" y="257" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Risk = bozulma olasılığı × bozulunca oluşacak zarar. Sıra buradan çıkar, sezgiden değil.</text>
  <text x="330" y="276" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Yüksek riskli akış önce ve derin test edilir; düşük riskli alan bilerek yüzeysel geçilir.</text>
  <text x="330" y="295" text-anchor="middle" font-size="10" fill="#B23B3B" font-family="system-ui">Kapsam dışı bırakılan her şey raporda görünür. Sessizce atlanan alan gizli risktir.</text>
</svg>
```

**Elenme sebepleri**

- Cevaba "senaryoları yazarım" diye başlamak. Gereksinimi okumadan senaryo yazan testçi, yanlış şeyi doğru yöntemle test eder.
- Hiç soru sormamak. Belirsiz gereksinimi olduğu gibi kabul etmek, hatayı teste değil üretime bırakır.
- Önceliklendirmeden söz etmemek. Sonsuz zaman varsayan plan ilk sıkışmada dağılır ve sıra rastgeleye döner.
- Kabul ölçütünü atlamak. Ölçüt yoksa "bitti" kararı kişisel kanaate kalır; tartışma testten sonra başlar.
- Test verisini sonradan düşünmek. Veri hazır değilse koşum durur ve bekleme süresi teste yazılır.
- Kapsam dışını söylememek. Test edilmeyen alan raporda görünmüyorsa ekip onu test edilmiş sanır.
- Süreci yalnız araç adlarıyla anlatmak. Jira ve Excel bir süreç değildir; süreç sıradır ve ölçüttür (`229`).

**Kendi hikâyen**

Buraya kendi vakan gelecek. Uydurma; aşağıdaki sorular seni kendi geçmişine götürsün:

- Son aldığın özellikte gereksinim ne kadar netti? Hangi soruyu sorunca bir eksik ortaya çıktı?
- Kabul ölçütü yazılı mıydı, yoksa sözlü mü kaldı? Sonunda "ben böyle anlamıştım" tartışması çıktı mı?
- Test penceresi kaç gündü? Sıkıştığında neyi kestin ve kesme kararını kim onayladı?
- Riskli gördüğün akış hangisiydi? Neden riskli gördün: para mı, yetki mi, geri alınamazlık mı?
- Test verisi nereden geldi? Elle mi üretildi, üretimden maskelenerek mi alındı, hazır bir setten mi?
- Ortam farkı yüzünden koşum durdu mu? Ne kadar süre kaybedildi ve kim çözdü?
- Raporunda kapsam dışı bir bölüm var mıydı? Onu okuyan çıktı mı, bir karara dönüştü mü?

Böyle bir deneyimin yoksa uydurma. `256`'daki köprüyü kur: hangi adımı yaşadığını söyle, yaşamadığını adlandır, kapatma planını ver. Küçük bir ekipte tek başına test etmiş olmak da gerçek bir cevaptır. Yeter ki onu kurumsal bir test süreci gibi anlatma.

### "İyi bir test senaryosu neye benzer?"

**Soru aslında ne ölçüyor?**

Bu soru göründüğünden fazlasını ölçer. İlan "yazılı iletişim güçlü" diyor ve test senaryosu bir belgedir. Ölçülen şey şudur: bu adayın yazdığı senaryoyu başka biri okuyup aynı şekilde koşabilir mi. Zayıf aday senaryoyu kendine not gibi yazar; çalışır, çünkü ürünü zaten bilir. Güçlü aday senaryoyu devredilebilir yazar. Ayrım en net biçimde beklenen sonuç alanında görünür. "Doğru çalışmalı" cümlesi bir sonuç değil, bir temennidir.

**Sözlü cevap iskeleti**

1. **Tek amaç.** Bir senaryo tek bir kuralı doğrular. İki amaç birleşince senaryo kaldığında hangisinin bozulduğu bilinmez.
2. **Ön koşul açık.** Hangi kullanıcı, hangi yetki, hangi veri durumu. Ön koşulsuz senaryo yalnız yazarının makinesinde çalışır.
3. **Adımlar kısa ve somut.** "Kullanıcı yönetimi ekranını aç", "doğum tarihine 2010-05-01 gir", "Kaydet'e bas". Belirsiz fiil kullanma.
4. **Beklenen sonuç ölçülebilir.** Asıl ayrım burada. Sonuç gözlenebilir bir davranışa, ekrandaki bir mesaja ya da veritabanındaki bir duruma bağlanır.
5. **Bağımsız ve tekrar edilebilir.** Senaryo başka bir senaryonun bıraktığı veriye dayanmaz. Kendi verisini kurar, ardını toplar (`203`).
6. **İzlenebilir.** Her senaryo bir gereksinime bağlanır. Karşılığı olmayan senaryo ya artıktır ya da yazılmamış bir kuralın izidir (`229`).

**Kötü senaryo ile iyi senaryonun karşılaştırması**

- Başlık: "Kayıt ekranı testi" ↔ "Reşit olmayan kullanıcının kaydı reddedilir".
- Ön koşul: yok ↔ "Kayıt ekranı açık, oturum yok, aynı kimlik numarasıyla kayıt bulunmuyor".
- Adım: "Sistemi kontrol et" ↔ "Doğum tarihi alanına bugünden on yedi yıl önceki tarihi gir ve Kaydet'e bas".
- Beklenen sonuç: "Doğru çalışmalı" ↔ "Kayıt oluşmaz, form üstünde 'Yaş 18'den küçük olamaz' uyarısı görünür, tabloda yeni satır yoktur".
- Kapsam: "Tüm alanları dene" ↔ "Yalnız yaş kuralı; diğer zorunlu alan kuralları ayrı senaryolarda".

Bu karşılaştırmayı mülakatta sesli söylemek işe yarar. Çünkü ikinci sütun okunduğunda dinleyen kişi senaryoyu zihninde koşabilir, birincisi okunduğunda koşamaz. Kural olarak yaş sınırı 18 ise senaryolar 17, 18, 65 ve 66 gibi sınır değerleri ayrı ayrı içerir; ortadaki değerler tek bir denklik sınıfını temsil eder ve tek senaryo yeter.

**Elenme sebepleri**

- Beklenen sonucu "doğru çalışmalı" diye yazmak. Bu soruda en hızlı eleyen cevaptır ve ölçütsüzlüğü tek cümlede gösterir.
- Bir senaryoya beş kuralı birden koymak. Kaldığında hangi kuralın bozulduğu raporda görünmez.
- Ön koşulu atlamak. Senaryo yalnız belirli bir veri durumunda çalışır ve devredildiğinde kalır.
- Senaryoları birbirine bağlamak. İkinci senaryo birincinin bıraktığı kayda güvenirse sıra bozulunca hepsi düşer (`203`).
- Sınır değerleri hiç anmamak. Hatalar çoğunlukla ortada değil, sınırda oturur.
- Negatif senaryo yazmamak. Yalnız mutlu yolu test eden set, gerçek kullanımın yarısını görmez (`201`).
- Senaryoyu gereksinime bağlamamak. Bağı olmayan senaryo yönetilemez ve değişiklikte hangisinin etkilendiği bulunamaz.

**Kendi hikâyen**

Bu soruda hikâye küçük olabilir; yeter ki gerçek olsun. Şu sorular onu çıkarır:

- Senaryoları nerede tutuyordun? Araç içinde mi, tabloda mı, belgede mi?
- Senin yazdığın bir senaryoyu başkası koştu mu? Anlamadığı yer neresiydi?
- Bir senaryo yüzünden yanlış hata açıldı mı? Beklenen sonuç net olsaydı açılır mıydı?
- Sınır değer yüzünden bulduğun bir hata var mı? Hangi alandaydı ve sınır neredeydi?
- Senaryo setin kaç kalemdi? Zamanla büyüdü mü, hiç temizlendi mi?
- Birbirine bağımlı senaryolar yüzünden koşum çöktü mü? Nasıl ayırdın?
- Bir gereksinim değişince senaryolarını nasıl buldun? Arama mı yaptın, bağ mı vardı?

Deneyimin yoksa uydurma. `256`'daki köprü burada çok rahat işler: kendi kullandığın bir uygulamanın kayıt ekranı için beş senaryo yazmak gerçek bir alıştırmadır ve mülakatta dürüstçe öyle anlatılır.

### "İyi bir hata (bug) raporu nasıl yazılır?"

**Soru aslında ne ölçüyor?**

Hata raporu, testçinin en görünür ürünüdür. Geliştirici testçiyi konuşmalarından değil, raporlarından tanır. Görüşmecinin baktığı iki şey var. Birincisi, bu raporu okuyan geliştirici ek soru sormadan işe başlayabilir mi. İkincisi, aday severity ile priority arasındaki farkı biliyor mu. İkinci nokta bu sorunun ayrıştırıcısıdır; çünkü çok aday ikisini eş anlamlı kullanır ve bunu fark etmez. Ağırlık yazım disiplinindedir.

**Sözlü cevap iskeleti**

1. **Başlık tek cümlede sorunu söyler.** "Çalışmıyor" değil, "Sepette üçten fazla ürün varken kargo ücreti iki kez ekleniyor". Başlık listede tek başına okunur ve sıralanır.
2. **Adımlar yeniden üretilebilir (reproducibility).** Numaralı, eksiksiz, hangi veriyle. Geliştirici raporu okuyup hatayı görebilmeli; göremezse rapor geri döner (`234`).
3. **Beklenen ile gözlenen ayrı yazılır.** Bu ikisi tek paragrafta birleşirse tartışma "sen ne bekliyordun" noktasına düşer ve zaman kaybolur.
4. **Ortam bilgisi verilir.** Sürüm ve derleme numarası, ortam adı, tarayıcı, kullanıcı rolü, tarih ve saat, ilgili kayıt numarası.
5. **Kanıt eklenir.** Ekran görüntüsü, kısa video, log parçası, istek ve yanıt gövdesi. Kanıt tartışmayı kısaltır, kimseyi haklı çıkarmaya çalışmaz.
6. **Severity ile priority ayrılır.** Severity teknik etkidir ve testçi belirler. Priority iş önceliğidir ve ürün sahibi belirler. Bu ayrımı kurmak bu soruda en çok ayrıştıran cümledir (`229`).

```svg
<svg viewBox="0 0 660 376" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Geri donen hata raporu ile islenen hata raporu ve severity priority matrisi">
  <defs>
    <marker id="mnl-rapor" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Rapor, geliştiricinin ek soru sormadan işe başlayabildiği andan itibaren iyidir</text>
  <rect x="14" y="36" width="310" height="150" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="169" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Geri dönen rapor</text>
  <text x="169" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Başlık: «çalışmıyor»</text>
  <text x="169" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Adım: «kayıt ekranından denedim»</text>
  <text x="169" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Beklenen sonuç yok, gözlenen yok</text>
  <text x="169" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Sürüm ve ortam yazılmamış</text>
  <text x="169" y="159" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Kanıt yok, görüntü yok, log yok</text>
  <text x="169" y="178" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Sonuç: yeniden üretilemez, kapanır</text>
  <line x1="326" y1="111" x2="332" y2="111" stroke="#5D7789" stroke-width="1.4" marker-end="url(#mnl-rapor)"/>
  <rect x="336" y="36" width="310" height="150" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="491" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İşlenen rapor</text>
  <text x="491" y="79" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Başlık sorunu tek cümlede söyler</text>
  <text x="491" y="99" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Adımlar numaralı, veri değerleriyle</text>
  <text x="491" y="119" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Beklenen ve gözlenen ayrı satırda</text>
  <text x="491" y="139" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Sürüm, ortam, rol, saat, kayıt no</text>
  <text x="491" y="159" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">Kanıt: görüntü, log, istek ve yanıt</text>
  <text x="491" y="178" text-anchor="middle" font-size="9.5" font-weight="700" fill="#25804C" font-family="system-ui">Sonuç: geliştirici hatayı görür</text>
  <text x="330" y="208" text-anchor="middle" font-size="13" font-weight="700" fill="#1C3247" font-family="system-ui">Severity ile priority aynı şey değildir; aynı hata iki ayrı eksende ölçülür</text>
  <text x="330" y="228" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Severity teknik etkidir, testçi belirler · priority iş önceliğidir, ürün sahibi belirler</text>
  <text x="249" y="245" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Priority: düşük</text>
  <text x="511" y="245" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Priority: yüksek</text>
  <text x="64" y="278" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Severity</text>
  <text x="64" y="293" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">yüksek</text>
  <rect x="120" y="252" width="258" height="56" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="249" y="274" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yılda bir koşan raporda çökme</text>
  <text x="249" y="292" text-anchor="middle" font-size="9" fill="#B07D20" font-family="system-ui">etkisi ağır ama bugün acil değil</text>
  <rect x="382" y="252" width="258" height="56" rx="8" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="511" y="274" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ödeme tutarı yanlış hesaplanıyor</text>
  <text x="511" y="292" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">sürüm durur, hemen düzeltilir</text>
  <text x="64" y="338" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Severity</text>
  <text x="64" y="353" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">düşük</text>
  <rect x="120" y="312" width="258" height="56" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="249" y="334" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iç ekranda hizalama bozuk</text>
  <text x="249" y="352" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">biriktir, uygun sürümde topla</text>
  <rect x="382" y="312" width="258" height="56" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="511" y="334" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">giriş ekranında kurum adı yanlış</text>
  <text x="511" y="352" text-anchor="middle" font-size="9" fill="#186399" font-family="system-ui">küçük hata, itibar riski büyük</text>
</svg>
```

Diyagramın sol alt ve sağ üst köşeleri bu ayrımın neden önemli olduğunu gösterir. Kurum adının yanlış yazılması teknik olarak hiçbir şeyi bozmaz; severity düşüktür. Ama giriş ekranında duruyorsa iş tarafı bugün düzelmesini ister; priority yüksektir. Tersi de doğrudur: yılda bir koşan bir raporun çökmesi ağır bir kusurdur ama sırası gelene kadar bekleyebilir. Bu iki eksen birleştirilirse sıralama anlamını yitirir.

**Elenme sebepleri**

- "Çalışmıyor" başlığı yazmak. Listede hiçbir şey anlatmaz; okuyan her kaydı açmak zorunda kalır ve sıralayamaz.
- Adımları eksik bırakmak. Yeniden üretilemeyen hata kapanır; kapanan hata çoğu zaman üretimde geri gelir.
- Beklenen sonucu yazmamak. Geliştirici davranışın hata mı yoksa tasarım kararı mı olduğunu ayırt edemez.
- Severity ile priority'yi eş anlamlı kullanmak. Bu soruda en sık duyulan hatadır ve ortak dili bilmediğini gösterir.
- Her hatayı en yüksek severity ile açmak. Hepsi kritikse hiçbiri kritik değildir; sıralama işlevini kaybeder.
- Tek kayıtta birkaç hatayı birden açmak. Biri düzelir, kayıt açık kalır ve yaşam döngüsü tıkanır (`229`).
- Kişiyi hedef alan dil kurmak. "Yine aynı ekran bozulmuş" cümlesi raporu teknik bir belge olmaktan çıkarır.

**Kendi hikâyen**

Bu soruda hikâyenin kanıtı senin yazdığın gerçek bir kayıttır. Şu sorular onu çıkarır:

- En son geri dönen raporun hangisiydi? Neden geri döndü: adım mı eksikti, ortam mı yazılmamıştı?
- Severity ile priority'nin ayrıldığı bir vaka yaşadın mı? Kim hangi değeri belirledi?
- Priority konusunda ürün sahibiyle görüş ayrılığına düştün mü? Nasıl sonuçlandı?
- Hangi kanıt türü işini en çok kolaylaştırdı: ekran görüntüsü mü, video mu, log mu?
- Kapanmış bir hatanın üretimde geri geldiği oldu mu? Rapor yeterince açık mıydı?
- Aynı hatanın iki kez açıldığı oldu mu? Kopya kayıtları nasıl fark ediyordunuz?
- Rapor şablonunuz var mıydı? Şablonu sen mi kurdun, hazır mı geldi?

Bu deneyim yoksa uydurma. `256`'daki köprüyü kur. Kullandığın herhangi bir uygulamada bulduğun bir davranışı tam kalıba göre yazmak gerçek bir örnektir ve mülakatta gösterilebilir.

### "İzlenebilirlik matrisi ne işe yarar?"

**Soru aslında ne ölçüyor?**

Bu soru terim sorusu gibi durur ama değildir. Çoğu aday tanımı söyler ve durur: "gereksinimlerle testleri eşleştiren tablo". Tanım doğrudur ve hiçbir şey kazandırmaz. Ayrım, matrisi günlük işte nerede kullandığını söylemekle kurulur. İki gerçek kullanımı vardır: kapsama boşluğunu bulmak ve değişiklikte etki analizi yapmak. İkincisini söyleyen aday matrisin denetim için tutulan ölü bir belge olmadığını biliyor demektir.

**Sözlü cevap iskeleti**

1. **Neyi neye bağladığını söyle.** Matris üç şeyi bağlar: gereksinim, onu doğrulayan test senaryosu ve o senaryodan çıkan hata kaydı (`229`).
2. **Soldan sağa oku: kapsama boşluğu.** Hiçbir senaryosu olmayan gereksinim test edilmemiştir. Matrisin en hızlı verdiği cevap budur.
3. **Sağdan sola oku: sahipsiz test.** Hiçbir gereksinime bağlanmayan senaryo ya artıktır ya da yazılmamış bir kuralın izidir. İkisi de konuşulmalıdır.
4. **Değişiklikte etki analizi için kullan.** Asıl günlük faydası budur. Gereksinim değişince o satırdaki senaryolar işaretlenir; gerileme testinin (regression) kapsamı tahminle değil bağla belirlenir (`187`).
5. **Nerede tutulduğunu söyle.** Küçük projede tablo yeter. Kurumsal ve regülasyonlu ortamda araç içinde tutulur; bağın kendisi denetim izidir.
6. **Sınırını da söyle.** Matris kapsamayı gösterir, kaliteyi göstermez. Her gereksinimin karşısında bir senaryo bulunması, o senaryonun iyi olduğunu söylemez.

```svg
<svg viewBox="0 0 660 322" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Izlenebilirlik matrisi: gereksinim, test senaryosu ve hata bagi ile kapsama boslugu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Matris iki yönde okunur: karşılıksız gereksinim ve sahipsiz test</text>
  <rect x="14" y="36" width="632" height="28" rx="6" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="66" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Gereksinim</text>
  <text x="153" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T-1</text>
  <text x="223" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T-2</text>
  <text x="293" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T-3</text>
  <text x="363" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T-4</text>
  <text x="433" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">T-5</text>
  <text x="557" y="55" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Bağlı hata kaydı</text>
  <rect x="14" y="66" width="632" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="66" y="86" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">G-1 kayıt</text>
  <circle cx="153" cy="81" r="5" fill="#25804C"/>
  <circle cx="223" cy="81" r="5" fill="#25804C"/>
  <text x="557" y="85" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">HTA-118 · açık</text>
  <rect x="14" y="100" width="632" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="66" y="120" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">G-2 yetki</text>
  <circle cx="293" cy="115" r="5" fill="#25804C"/>
  <text x="557" y="119" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kayıt yok</text>
  <rect x="14" y="134" width="632" height="30" rx="6" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.6"/>
  <text x="66" y="154" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">G-3 iptal</text>
  <text x="293" y="153" text-anchor="middle" font-size="9" font-weight="700" fill="#B23B3B" font-family="system-ui">hiç senaryo yok — kapsama boşluğu</text>
  <text x="557" y="153" text-anchor="middle" font-size="9" fill="#B23B3B" font-family="system-ui">test edilmemiş alan</text>
  <rect x="14" y="168" width="632" height="30" rx="6" fill="#EAF1F7" stroke="#186399" stroke-width="1.2"/>
  <text x="66" y="188" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">G-4 rapor</text>
  <circle cx="363" cy="183" r="5" fill="#25804C"/>
  <circle cx="433" cy="183" r="5" fill="#25804C"/>
  <text x="557" y="187" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">HTA-131 · kapandı</text>
  <line x1="118" y1="36" x2="118" y2="198" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="468" y1="36" x2="468" y2="198" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="14" y="212" width="204" height="98" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="116" y="234" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Soldan sağa</text>
  <text x="116" y="256" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hangi gereksinim karşılıksız?</text>
  <text x="116" y="275" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">G-3 satırı boş kalmış</text>
  <text x="116" y="294" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">kapsama boşluğu buradan çıkar</text>
  <rect x="228" y="212" width="204" height="98" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="234" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Sağdan sola</text>
  <text x="330" y="256" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bu test neyi doğruluyor?</text>
  <text x="330" y="275" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gereksinime bağlanmayan senaryo</text>
  <text x="330" y="294" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">ya artıktır ya yazılmamış kural</text>
  <rect x="442" y="212" width="204" height="98" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="544" y="234" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Değişiklik geldiğinde</text>
  <text x="544" y="256" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">değişen gereksinim işaretlenir</text>
  <text x="544" y="275" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">o satırdaki testler yeniden koşulur</text>
  <text x="544" y="294" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">gerileme kapsamı tahminle seçilmez</text>
</svg>
```

**Elenme sebepleri**

- Yalnız tanımı söyleyip durmak. Tanım herkeste var; kullanım yeri ayrıştırır.
- Matrisi denetim belgesi saymak. "Sadece dış denetimde isterler" cevabı günlük faydayı görmediğini gösterir.
- Etki analizinden hiç söz etmemek. Matrisin en çok işe yarayan tarafı budur ve atlanması dikkat çeker.
- Yalnız tek yönde okumak. Karşılıksız gereksinim kadar sahipsiz test de bilgi taşır.
- Matrisi elle güncellenen ayrı bir tablo olarak anlatmak. Elle tutulan bağ birkaç sürümde eskir ve yanıltır.
- Kapsamayı kaliteyle karıştırmak. Her satırın dolu olması testlerin iyi olduğunu göstermez.
- Hata bağını unutmak. Gereksinim ile senaryo bağı tek başına, hangi kuralın kaç kez bozulduğunu göstermez.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir. Şu sorular onu çıkarır:

- Çalıştığın projede gereksinim ile test arasında bir bağ var mıydı? Nerede tutuluyordu?
- Bağ elle mi kuruluyordu, araç içinde kayıt olarak mı duruyordu?
- Hiç kapsama boşluğu buldun mu? Hangi gereksinim gözden kaçmıştı ve nasıl fark ettin?
- Bir gereksinim değiştiğinde etkilenen testleri nasıl buluyordunuz: arama mı, hafıza mı, bağ mı?
- Hiçbir gereksinime bağlanmayan eski senaryolarınız var mıydı? Ne yaptınız?
- Dış denetim ya da müşteri sorusu geldiğinde bu bağ işinize yaradı mı?
- Bağı tutmanın maliyeti neydi? Kim güncelliyordu ve ne sıklıkla eskiyordu?

Deneyimin yoksa uydurma. `256`'daki köprüyle şunu söyleyebilirsin: bağın kurulmadığı bir projede hangi sorunları yaşadığını anlatmak da gerçek bir gözlemdir ve matrisin ne işe yaradığını en iyi o anlatır.

### "Teste ne zaman 'bitti' dersin?"

**Soru aslında ne ölçüyor?**

Bu, dosyanın en olgunluk isteyen sorusudur. Görüşmeci bir tuzak kurar ve çoğu aday içine düşer: "bütün senaryolar geçtiyse bitmiştir" demek. Bu cümle testin birinci ilkesine aykırıdır. Test hatanın varlığını gösterir, yokluğunu kanıtlayamaz. İkinci ölçülen şey ise karar sahipliğidir. "Bitti" kararı testçinin tek başına verdiği bir karar değildir; testçi ölçümü sunar, kararı ekip verir. Ağırlık muhakemededir ve cevabın gücü baştaki ilkeden gelir.

**Sözlü cevap iskeleti**

1. **"Hata kalmadı" diyemeyeceğini baştan söyle.** Test hatanın varlığını gösterir, yokluğunu kanıtlayamaz. Bu cümle sorunun açılışıdır (`228`).
2. **Çıkış ölçütünü (exit criteria) baştan tanımla.** Bitiş kararı test sonunda değil, test planında verilir. Sonradan konan ölçüt sonuca göre eğilir.
3. **Ölçütü sayıya bağla.** Planlanan senaryoların yüzde kaçı koşuldu, kaçı geçti, açık kritik ve yüksek severity hata kaç tane, duman testi (smoke test) geçiyor mu.
4. **Kalan riski adlandır ve kabul ettir.** Kapatılmayan hatalar kimin kararıyla kalıyor, kullanıcıyı nasıl etkiler, geçici bir çözüm var mı.
5. **Kapanış raporunu yaz.** İçinde ne olur: kapsam ve kapsam dışı, koşum özeti, açık hatalar ve gerekçeleri, kalan risk, süreçten çıkan ders (`187`).
6. **Zaman baskısını dürüst yönet.** Süre bitti diye test bitmez. Süre bitince ölçütün ne kadarının karşılandığı yazılır ve karar iş tarafına gider.

**Elenme sebepleri**

- "Tüm senaryolar geçti, ürün hatasız" demek. Tek cümlede testin birinci ilkesini ihlal eder ve bu soruda en hızlı eleyen cevaptır.
- Çıkış ölçütünü hiç anmamak. Ölçütsüz bitiş kararı kanaate kalır ve her sürümde yeniden tartışılır.
- Ölçütü sonuca göre gevşetmek. Sonuca göre eğilen ölçüt, ölçüt değildir.
- Takvimi tek başına ölçüt saymak. Süre bir kısıttır; kalan riskle birleştirilmeden karar üretmez.
- Kalan riski yazmamak. Yazılmayan risk kabul edilmiş sayılmaz, gizlenmiş sayılır.
- Kararı tek başına vermek ya da tek başına reddetmek. Testçi ölçümü sunar, "çıkar" ya da "çıkmaz" kararının sahibi ekiptir.
- Kapanış raporunu atlamak. Ders çıkarılmayan süreç bir sonraki sürümde aynı yerde tıkanır.

**Kendi hikâyen**

Bu soruda hikâyenin omurgası bir karardır: neyle bitirdin ve neyi kabul ettin. Şu sorular onu çıkarır:

- Çalıştığın yerde yazılı bir çıkış ölçütü var mıydı? Kim yazmıştı?
- Süre bitmeden ölçüt karşılanmadığı bir sürüm oldu mu? Ne oldu, karar nasıl verildi?
- Açık hatayla çıkılan bir sürüm hatırlıyor musun? Riski kim kabul etti ve yazıya döküldü mü?
- Kapanış raporu yazıyor muydun? İçinde ne vardı ve kim okuyordu?
- "Bitti" dedikten sonra üretimde çıkan bir hata oldu mu? Nereden kaçmıştı?
- Duman testi setiniz var mıydı? Sürüm sonrası ilk kontrol neydi?
- Zaman baskısı altında kestiğin bir kapsam var mı? O kararı bugün nasıl anlatırsın?

Böyle bir deneyimin yoksa uydurma. `256`'daki köprüyü kur ve şunu söyle: çıkış ölçütünün yazılı olmadığı bir ortamda çalıştıysan, bunun yarattığı belirsizliği anlatmak da gerçek bir cevaptır ve ölçütün neden gerektiğini en iyi o gösterir.

### "Geliştirici 'bende çalışıyor' diyor, ne yaparsın?"

**Soru aslında ne ölçüyor?**

Bu bir teknik soru gibi sorulur ama davranışsal bir sorudur. Görüşmeci şunu merak eder: bu aday çatışmayı nasıl taşır. İlan zaten "sözlü ve yazılı iletişim güçlü" diyor; bu soru o maddenin sınavıdır. Zayıf cevap iki uçtan birine düşer: ya geri çekilip hatayı kapatmak, ya da ısrarla aynı raporu yeniden açıp gerilimi büyütmek. Güçlü cevap üçüncü yolu tutar. "Bende çalışıyor" bir itiraz değil, bir bilgidir: iki ortam arasında bir fark var demektir. Sorunun adı artık hata değil, farktır.

**Sözlü cevap iskeleti**

1. **Cümleyi veri olarak al.** İki farklı sonuç varsa arada bir fark vardır. Bu çerçeveyi ilk cümlede kurmak tartışmayı teknik zeminde tutar.
2. **Önce kendi tarafını sağlamla.** Raporu tekrar oku, adımları temiz bir oturumda yeniden koş. Hata sende de çıkmıyorsa bunu kendin söylersin; bu güven kazandırır.
3. **Farkı sistemli ara.** Sürüm ve derleme numarası, veri durumu, yapılandırma, kullanıcı rolü ve yetki, tarayıcı ve sürümü, önbellek, saat dilimi, ağ. Liste hâlinde sormak süreci hızlandırır.
4. **Kanıtı büyüt.** Kısa video, log parçası, istek ve yanıt gövdesi, izleme kimliği. Kanıt tartışmayı kısaltır ve kimseyi haksız çıkarmaya çalışmaz (`229`).
5. **Ortak ortamda birlikte üret.** On dakikalık bir ekran paylaşımı, iki günlük yazışmadan hızlıdır. Ortak ortam farkı zaten ortadan kaldırır.
6. **Dili kişiden olaya çevir.** "Sen yanlış yapmışsın" değil, "iki ortamda farklı sonuç alıyoruz, farkı bulalım". Suçlamayan dil kök nedene daha hızlı gider (`195`).

**Elenme sebepleri**

- Hatayı hemen yöneticiye taşımak. Yükseltme ilk adım değil, son adımdır; ilk adım farkı aramaktır.
- Aynı raporu yeni bilgi eklemeden yeniden açmak. Bilgi eklenmeyen yeniden açma yalnız tartışmayı uzatır.
- Kendi ortamını hiç sorgulamamak. Testçi de eski sürümde ya da kirli veriyle koşuyor olabilir.
- Kanıt eklemeden tartışmak. Kelimeyle kurulan tartışma, kimin daha ısrarcı olduğuna göre biter.
- Kişiselleştirmek. "Geliştiriciler zaten bakmıyor" cümlesi mülakatta ekip çalışmasına dair kırmızı bayraktır.
- Sorunu geri çekip kapatmak. Yeniden üretilemeyen her hata yok değildir; bazıları zamana ve sıraya bağlıdır.
- Fark bulunduktan sonra ders çıkarmamak. Aynı ortam farkı tekrarlıyorsa çözüm rapor değil, ortam yönetimidir.

**Kendi hikâyen**

Bu soruda küçük bir vaka bile yeterlidir; önemli olan anlatırken kullandığın dildir. Şu sorular onu çıkarır:

- Böyle bir tartışma yaşadın mı? Fark nereden çıktı: sürüm mü, veri mi, yetki mi, tarayıcı mı?
- Kaç tur yazıştıktan sonra çözüldü? Ekran paylaşımı denendi mi, ne kadar sürdü?
- Kendi ortamının hatalı olduğu bir vaka oldu mu? Onu nasıl söyledin?
- Yeniden üretilemeyen bir hata sonradan üretimde çıktı mı? Kapatma kararını kim vermişti?
- Ortam farkı tekrarlıyor muydu? Kalıcı bir çözüm kuruldu mu, kim kurdu?
- Tartışma sırasında dilini nasıl seçtin? Yazılı mı ilerledin, yüz yüze mi?
- Bu süreç sonunda rapor şablonunuza yeni bir alan eklendi mi?

Bu deneyim yoksa uydurma; burası uydurmanın en çabuk yakalandığı yerdir, çünkü ayrıntılar sorulur. `256`'daki köprüyü kur. Ortam farkı yüzünden yaşanmış herhangi bir kurulum sorununu anlatmak da yöntemini gösterir.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Soruyu yüksek sesle oku, sonra dosyayı kapat. İskeleti okuyarak cevap vermek prova sayılmaz.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek yetmez; ağız ile beyin aynı hızda çalışmaz.
- Sonra iskeletle karşılaştır ve atladığın adımı işaretle.
- Bu altı soruda en sık atlanan şey ölçüttür. Ölçüt söylenmeyen cevap teknik ama yönsüz duyulur.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- Örnekleri ezberle, cümleleri ezberleme. Kayıt ekranı ve yaş kuralı örneği bu dosyanın omurgasıdır.
- Ölçüt şudur: bir sorunun adımlarını kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Bu bölümden çıkan ders

Altı sorunun ortak paydası tek cümlede toplanır: manuel test bir tıklama işi değil, bir belge ve karar disiplinidir. Görüşmeci her soruda aynı şeyi arar, yalnız kılığını değiştirir. Süreç sorusunda aradığı ölçüt risktir. Senaryo sorusunda ölçüt beklenen sonucun gözlenebilir olmasıdır. Hata raporu sorusunda ölçüt severity ile priority ayrımıdır; severity teknik etkidir ve testçi belirler, priority iş önceliğidir ve ürün sahibi belirler. İzlenebilirlik sorusunda ölçüt gereksinim bağıdır. Bitiş sorusunda çıkış ölçütü ve kabul edilen kalan risktir. Çatışma sorusunda ise ölçüt kanıttır. Ölçütünü söyleyen aday sürecin sahibi gibi konuşur; ölçütsüz konuşan aday koşum yapan bir el gibi duyulur. İlan "manuel test süreçlerinde çok iyi" derken aradığı şey tam olarak budur, ve bu ilan otomasyon bilgisiyle değil bu disiplinle kazanılır.

# mnl-1 | Temel
## Soru
Bir test senaryosunun beklenen sonuç alanında "sistem doğru çalışmalı" yazıyor. Bu senaryonun temel kusuru nedir?
## A
Senaryo fazla kısadır; beklenen sonuç en az üç cümleyle ayrıntılı biçimde yazılmalıdır
🤖 uzunluk bir ölçüt değildir; kısa ama ölçülebilir sonuç yeterlidir.
## B
Ön koşul yazılmadığı için senaryo yalnız temiz bir veritabanında koşulabilir hâle gelmiştir
🤖 ön koşul ayrı bir eksiklik olabilir; sorulan kusur beklenen sonuçtadır.
## C
Beklenen sonuç ölçülebilir değildir; iki testçi aynı ekrandan farklı karar çıkarabilir
🤖 Doğru: sonuç gözlenebilir bir davranışa ve mesaja bağlanmalıdır.
## D
Kusur yoktur; ayrıntı koşum sırasında testçinin ürün bilgisine bırakılabilir
🤖 bilgi kişiye bağlanırsa senaryo devredilemez ve tekrar edilemez.
## Doğru
C
## Açıklama
"Doğru çalışmalı" bir sonuç değil, bir temennidir. Senaryonun beklenen sonucu **gözlenebilir** olmalıdır: ekranda hangi mesaj çıkar, hangi kayıt oluşur ya da oluşmaz, hangi alan hangi değeri alır. Ölçülebilir yazılmış bir sonuç şuna benzer: "Kayıt oluşmaz, form üstünde 'Yaş 18'den küçük olamaz' uyarısı görünür ve tabloda yeni satır yoktur." Bu fark, senaryonun devredilebilir olup olmadığını belirler. Ölçütsüz senaryoyu yalnız yazarı koşabilir, çünkü kararı kendi ürün bilgisiyle verir; başka bir testçi aynı ekrandan farklı sonuç çıkarır ve iki koşum uyuşmaz. D şıkkı tam bu tuzağı savunur ve senaryoyu kişiye bağlar. A şıkkı uzunlukla netliği karıştırır; tek cümlelik bir sonuç da ölçülebilir olabilir. B şıkkı gerçek bir eksikliğe işaret eder ama sorulan kusur bu değildir; ön koşul senaryonun başlangıç durumunu, beklenen sonuç ise bitiş durumunu tanımlar. Aynı disiplin gerileme setinin bakımını da kolaylaştırır: ölçüte bağlı senaryo değiştiğinde neyin değiştiği görünür (`234`, `203`).

# mnl-2 | Orta
## Soru
Kurumsal portalın giriş ekranında kurum adı yanlış yazılmış. Sistem işlevsel olarak sorunsuz çalışıyor, ama iş tarafı hatanın bugün düzelmesini istiyor. Bu hatayı nasıl sınıflarsın?
## A
Severity düşük, priority yüksek; severity teknik etkiyi, priority işin aciliyetini anlatır
🤖 Doğru: iki ayrı eksendir ve farklı roller belirler.
## B
Severity de priority de yüksek; bir hata acilse teknik etkisi de yüksek kabul edilir
🤖 aciliyet teknik etkiyi büyütmez; iki eksen bağımsız ölçülür.
## C
Severity de priority de düşük; işlev bozulmadığı için hata küçük kalır ve beklemeye alınır
🤖 iş tarafı bugün istiyorsa priority düşük olamaz.
## D
Sınıflandırma testçinin işi değildir; iki değeri de ürün sahibi belirler, testçi yalnız kaydı açar
🤖 severity'yi teknik etkiyi gören testçi belirler.
## Doğru
A
## Açıklama
Severity ve priority iki **ayrı eksendir** ve karıştırılmaları klasik bir elenme sebebidir. Severity teknik etkidir: hata sistemin işleyişini ne kadar bozuyor. Bunu gözlemleyen kişi testçidir, çünkü davranışı o görür. Priority ise iş önceliğidir: bu hata ne zaman düzelmeli. Bunu ürün sahibi belirler, çünkü sıralamanın maliyetini o taşır. Bu örnekte kurum adının yanlış yazılması hiçbir işlevi bozmaz; severity düşüktür. Ama giriş ekranında durduğu ve itibar riski taşıdığı için iş tarafı bugün ister; priority yüksektir. Tersi de sık görülür: yılda bir koşan bir raporun çökmesi ağır bir kusurdur ama sırası gelene kadar bekleyebilir; severity yüksek, priority düşüktür. İki ekseni birleştirmek sıralamayı anlamsızlaştırır. B ve C şıkları tam bunu yapar. D şıkkı ise sahipliği tümüyle ürün sahibine verir; oysa teknik etkiyi ölçmek testçinin işidir ve o ölçüm priority tartışmasının girdisidir (`229`).

# mnl-3 | Orta
## Soru
Geniş bir özellik için üç günlük test penceresi var ve planlanan senaryoların tamamı bu sürede koşulamıyor. En yerinde davranış hangisidir?
## A
Senaryoları yazılış sırasına göre koşup süre dolunca kalanları bir sonraki sürüme bırakmak
🤖 yazılış sırası riskle ilgisizdir; kritik akış sona kalabilir.
## B
Testi reddetmek ve süre uzatılmadan koşuma hiç başlamamak
🤖 hiç test edilmemiş sürüm, kısmen test edilmişten daha risklidir.
## C
Bütün senaryolara yüzeysel dokunup her alanı bir kez görmüş olmak
🤖 her yere değmek hiçbir yeri yeterince doğrulamaz.
## D
Riske göre sıralayıp yüksek riskli akışları derin test etmek, kapsam dışını raporda yazmak
🤖 Doğru: sıra riskten çıkar, atlanan alan yazılı kalır.
## Doğru
D
## Açıklama
Test penceresinin dar olması istisna değil, kuraldır. Bu yüzden **risk bazlı önceliklendirme** manuel test disiplininin çekirdeğidir. Risk iki şeyin çarpımıdır: bozulma olasılığı ve bozulunca oluşacak zarar. Para hareketi, yetkilendirme ve geri alınamaz işlem içeren akışlar bu çarpımın tepesinde durur ve önce, derinlemesine test edilir. Düşük riskli alan bilerek yüzeysel geçilir; bu bir eksiklik değil, **yazılı bir karardır**. Kararın yazılı olması cevabın ikinci yarısıdır: kapsam dışı bırakılan alan raporda görünmezse ekip onu test edilmiş sanır ve risk gizlenir. A şıkkı sıralamayı rastlantıya bırakır; yazılış sırasının riskle hiçbir bağı yoktur. B şıkkı sorumluluğu reddeder ve durumu kötüleştirir; kısmi test hiç testten iyidir. C şıkkı en aldatıcı seçenektir, çünkü çalışkan görünür: her ekrana bir kez bakmak kapsama gibi durur ama hiçbir kuralı doğrulamaz. Sıkışan takvimde doğru cümle şudur: neyi test ettiğimi ve neyi test etmediğimi ölçütle söylerim (`228`, `229`).

# mnl-4 | Orta
## Soru
Yayın öncesi bir gereksinim değişti. Hangi test senaryolarının yeniden koşulması gerektiğini en güvenilir biçimde ne gösterir?
## A
Son koşum geçmişi; en son hangi senaryoların kaldığına bakılarak kapsam belirlenir
🤖 geçmiş koşum sonucu, değişikliğin etkisini göstermez.
## B
İzlenebilirlik matrisi; gereksinim ile senaryo bağı etkilenen satırları doğrudan verir
🤖 Doğru: etki analizi bu bağ üzerinden yapılır.
## C
Kapsam seçmeye gerek yok; gerileme setinin tamamı her değişiklikte baştan koşulur
🤖 her seferinde tümünü koşmak çoğu takvimde mümkün değildir.
## D
Geliştiricinin değiştirdiği dosyaların listesi; etkilenen ekranlar oradan çıkarılır
🤖 kod dosyası listesi gereksinim kapsamını vermez.
## Doğru
B
## Açıklama
İzlenebilirlik matrisinin (traceability matrix) günlük işteki asıl faydası **etki analizidir**. Matris gereksinimi, onu doğrulayan senaryoları ve o senaryolardan çıkan hata kayıtlarını birbirine bağlar. Gereksinim değişince o satır işaretlenir ve etkilenen senaryolar tahminle değil bağla belirlenir. Matrisin ikinci faydası kapsama boşluğudur: hiçbir senaryosu olmayan gereksinim test edilmemiştir ve bu, tabloya bakınca hemen görünür. A şıkkı geçmiş koşum sonucuna bakar; oysa geçen bir senaryo da değişiklikten etkilenebilir, sonuç geçmişi etki hakkında bilgi taşımaz. C şıkkı teorik olarak güvenlidir ama pratikte uygulanamaz; manuel gerileme setinin tamamını her değişiklikte koşacak takvim yoktur ve otomasyon varsa bile süre bir kısıttır. D şıkkı kod tarafından bakar; dosya listesi hangi iş kuralının etkilendiğini söylemez ve testçiyi geliştiricinin yorumuna bağımlı kılar. Matrisin sınırını da bilmek gerekir: kapsamayı gösterir, kaliteyi göstermez. Dolu bir satır, oradaki senaryonun iyi yazıldığını kanıtlamaz (`229`, `187`).

# mnl-5 | İleri
## Soru
Bir test kapanış raporunda "tüm senaryolar geçti, üründe hata kalmamıştır" cümlesi yazıyor. Bu cümlenin temel sorunu nedir?
## A
Test hatanın varlığını gösterir, yokluğunu kanıtlayamaz; bitiş kararı çıkış ölçütüyle verilir
🤖 Doğru: hatasızlık iddiası testin birinci ilkesine aykırıdır.
## B
Sorun yoktur; planlanan senaryoların tamamı geçtiyse ürün hatasız kabul edilebilir
🤖 planlanan senaryolar yalnız düşünülmüş durumları kapsar.
## C
Yalnız üslup sorunudur; teknik olarak doğru ama fazla iddialı bir ifadedir
🤖 iddia üslup düzeyinde değil, olgu düzeyinde yanlıştır.
## D
Cümle yalnız otomasyon koşumu için yazılabilir; manuel koşumda böyle bir sonuç çıkarılamaz
🤖 otomasyon da hatasızlığı kanıtlayamaz; kapsamı daha dardır.
## Doğru
A
## Açıklama
"Hata kalmamıştır" cümlesi testin birinci ilkesini ihlal eder: **test hatanın varlığını gösterir, yokluğunu gösteremez**. Senaryolar yalnız düşünülmüş durumları kapsar; düşünülmemiş girdi, sıra ve ortam kombinasyonları her zaman kalır. Tam test zaten imkânsızdır, çünkü girdi uzayı ve yol sayısı sonludur ama pratikte tükenmez. Doğru cümle şöyle kurulur: planlanan senaryoların şu kadarı koşuldu, şu kadarı geçti, şu severity seviyesinde şu kadar açık hata kaldı ve kalan risk şudur. Yani bitiş kararı bir iddia değil, **çıkış ölçütüne** (exit criteria) ve kabul edilen kalan riske dayanan bir karardır. Ölçüt test planında baştan yazılır; sonradan yazılırsa sonuca göre eğilir. B şıkkı hatalı cümleyi savunur. C şıkkı sorunu üsluba indirger; oysa yanlışlık olgusaldır. D şıkkı otomasyona ayrıcalık tanır; otomasyon yalnız kendisine yazılmış kontrolleri doğrular ve kapsamı manuel koşumdan da dardır. Kapanış raporunda kalan riskin yazılmaması onu ortadan kaldırmaz, yalnız görünmez yapar (`228`, `187`).

# mnl-6 | İleri
## Soru
Açtığın hatayı geliştirici "bende çalışıyor" diyerek geri gönderdi. İlk olarak ne yapmalısın?
## A
Kaydı yöneticiye taşıyıp önceliğin yükseltilmesini istemek
🤖 yükseltme ilk adım değil, son adımdır.
## B
Aynı raporu yeni bilgi eklemeden yeniden açmak ve koşumun tekrarlanmasını beklemek
🤖 bilgi eklenmeyen yeniden açma yalnız tartışmayı uzatır.
## C
Adımları temiz oturumda sağlamlayıp sürüm, veri, yetki ve yapılandırma farkını aramak
🤖 Doğru: "bende çalışıyor" bir itiraz değil, ortam farkı işaretidir.
## D
Hatayı yeniden üretilemez olarak kapatıp bir sonraki sürümde tekrar bakmak
🤖 kapatılan hata çoğu zaman üretimde geri gelir.
## Doğru
C
## Açıklama
"Bende çalışıyor" cümlesi bir itiraz gibi duyulur ama aslında bir **bilgidir**: iki ortam farklı sonuç veriyor, demek ki arada bir fark var. Doğru davranış sorunun adını değiştirmektir; artık aranan şey hata değil, farktır. İlk adım kendi tarafını sağlamlamaktır: raporu tekrar oku, adımları temiz bir oturumda yeniden koş. Hata sende de çıkmıyorsa bunu kendin söylemek güven kazandırır. Sonra fark sistemli aranır: sürüm ve derleme numarası, veri durumu, yapılandırma, kullanıcı rolü ve yetki, tarayıcı ve sürümü, önbellek, saat dilimi. Bu liste hâlinde sorulunca süreç hızlanır. Ardından kanıt büyütülür ve mümkünse ortak bir ortamda birlikte koşulur; on dakikalık ekran paylaşımı iki günlük yazışmadan hızlıdır. A şıkkı yükseltmeyi ilk adım yapar ve gerilimi teknik zeminden çıkarır. B şıkkı bilgi eklemeden ısrar eder; yeni veri gelmeyen bir kayıt döngüye girer. D şıkkı ise problemi gizler: yeniden üretilemeyen her hata yok değildir, bazıları zamana, sıraya ya da yarış durumuna bağlıdır ve kapatılınca üretimde geri gelir. Kullanılan dil de değerlendirilir; suçlamayan dil kök nedene daha hızlı gider (`229`, `195`).
