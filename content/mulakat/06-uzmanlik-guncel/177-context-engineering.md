---
slug: context-engineering
baslik: "Bağlam Mühendisliği: Pencere Yönetimi, Context Stuffing Tuzağı, Bellek/Özetleme ve Token Bütçesi"
kademe: İleri
sira: 177
etiketler: 7,8
---

## Konu anlatımı

### Bu ne işe yarar?

Sınavda yanına yalnız tek bir A4 kâğıt alabildiğini düşün. Kâğıda ne yazacağın sınavı belirler: en kritik formülleri seçip düzenli yazarsan işine yarar; paniğe kapılıp tüm kitabı minicik puntoyla sığdırmaya çalışırsan sınavda aradığını bulamazsın. Yapay zekâ modelleriyle çalışırken de elimizde tek bir kâğıt vardır: **bağlam penceresi**. Bağlam mühendisliği, o sınırlı alana **hangi bilgiyi, ne kadarını, hangi sırayla** koyacağını tasarlama işidir. Daha çok bilgi koymak, daha iyi cevap demek değildir.

### Temel kavramlar

- **Büyük dil modeli (LLM)**: Metnin devamını olasılığa göre üreten program. Hiçbir şeyi "hatırlamaz"; her çağrıda yalnızca o an kendisine verilen metni görür.
- **Token (jeton)**: Metnin en küçük işleme parçası; kabaca bir kelime veya kelime parçası. Hem ücret hem de sınır token üzerinden sayılır.
- **Bağlam penceresi (context window)**: Modelin tek seferde işleyebileceği toplam token sayısı — talimat, veri, sohbet geçmişi ve üretilecek cevap hep bu tavanın altına sığmalıdır. Büyük olması (ör. 200K) sınırın kalktığı anlamına gelmez.
- **Token bütçesi**: Bu sonlu tavanı bölümlere ayırma kararı: sistem talimatı için şu kadar, kullanıcının sorusu için şu kadar, getirilen kanıt için şu kadar, geçmiş/özet için şu kadar.
- **"Ortada kaybolma" (lost in the middle)**: Uzun bağlamlarda modelin, metnin **başındaki ve sonundaki** bilgiye ortasındakinden daha güvenilir eriştiği, ölçümlerle gösterilmiş davranış.
- **Pencereyi tıka basa doldurma (context stuffing)**: "Ne olur ne olmaz, hepsini koyayım" alışkanlığı. Bu bir çözüm değil, bir tuzaktır.
- **Özetleme / sıkıştırma (summarization, compaction)**: Eskiyen ayrıntıyı yoğun bir özete indirgeyip pencerede az yer kaplatmak.
- **Geri getirme (retrieval)**: Soru her sorulduğunda, o soruya **o an ilgili** parçaları bir bilgi tabanından çekip yalnız onları pencereye koymak.

### Nasıl çalışır?

1. Toplam pencere tavanını öğrenirsin ve bölümlere **bütçe** ayırırsın.
2. Sistem talimatı ve o anki soru sabit ve kısa tutulur — bunlar pazarlık dışıdır.
3. Kanıt tarafı **geri getirme** ile doldurulur: tüm belgeler değil, yalnız en ilgili ilk k parça.
4. Sohbet geçmişi bütçeyi zorlamaya başlayınca eskiyen kısım **özetlenir**, kalıcı olgular dışsal belleğe yazılır.
5. Kritik kanıt bağlamın **başına ya da sonuna** konumlandırılır; ortada gömülü bırakılmaz.

```svg
<svg viewBox="0 0 640 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Doldurulmuş pencere vs seçilmiş bağlam">
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Doldurmak değil, seçmek</text>
  <rect x="30" y="44" width="270" height="140" rx="12" fill="#FBF1F1" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="165" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Context Stuffing (tuzak)</text>
  <rect x="48" y="80" width="234" height="14" rx="3" fill="#E7B4B4"/>
  <rect x="48" y="100" width="234" height="14" rx="3" fill="#E7B4B4"/>
  <rect x="48" y="120" width="234" height="14" rx="3" fill="#8FB89A"/>
  <rect x="48" y="140" width="234" height="14" rx="3" fill="#E7B4B4"/>
  <text x="165" y="174" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeşil kanıt gürültüde kaybolur (ortada)</text>
  <rect x="340" y="44" width="270" height="140" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="66" text-anchor="middle" font-size="12.5" font-weight="700" fill="#25804C" font-family="system-ui">Seçilmiş Bağlam</text>
  <rect x="358" y="86" width="234" height="16" rx="3" fill="#7FB292"/>
  <rect x="358" y="110" width="234" height="16" rx="3" fill="#7FB292"/>
  <rect x="358" y="134" width="234" height="16" rx="3" fill="#7FB292"/>
  <text x="475" y="174" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yalnız ilgili parçalar, net dikkat</text>
</svg>
```

```svg
<svg viewBox="0 0 640 190" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Token bütçesi: sonlu pencereyi bileşenlere bilinçle tahsis et">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Token bütçesi: pencereyi bilinçle tahsis et</text>
  <text x="40" y="58" font-size="10.5" fill="#5D7789" font-family="system-ui">Bağlam penceresi (sonlu toplam)</text>
  <rect x="40" y="66" width="110" height="50" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <rect x="150" y="66" width="120" height="50" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <rect x="270" y="66" width="210" height="50" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <rect x="480" y="66" width="120" height="50" fill="#F3EEF9" stroke="#6B3FA0" stroke-width="1.5"/>
  <text x="95" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">System</text>
  <text x="95" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">talimat</text>
  <text x="210" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Görev + soru</text>
  <text x="210" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">ne isteniyor</text>
  <text x="375" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#B26B00" font-family="system-ui">Alınan kanıt (retrieved)</text>
  <text x="375" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">o an ilgili parçalar</text>
  <text x="540" y="88" text-anchor="middle" font-size="11" font-weight="700" fill="#6B3FA0" font-family="system-ui">Geçmiş / özet</text>
  <text x="540" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">damıtılmış</text>
  <text x="320" y="150" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Her bölüme ayrı bütçe — toplam pencere sınırını aşamaz</text>
  <text x="320" y="172" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">bir bölüm şişerse diğerini gölgeler</text>
</svg>
```

### Mülakatta ne sorulur?

**"İstem (prompt) mühendisliğinden farkı ne?"** İstem mühendisliği "talimatı nasıl yazarım" sorusuyla ilgilenir: rol, biçim, örnekler. Bağlam mühendisliği ise "pencereye ne koyarım, ne kadar koyarım, neyi atarım" sorusuyla. İkisi tamamlayıcıdır; iyi bir talimat, kötü seçilmiş bir bağlamda işe yaramaz.

**"Neden hepsini koymuyoruz?"** Dört somut sebep var. (1) Her jeton maliyet ve gecikme demektir. (2) İlgisiz metin dikkati seyreltir ve doğru bilgiyi gölgeler. (3) Uzun bağlamın **ortasındaki** bilgi fark edilmeyebilir. (4) Çelişen içerik modeli yanıltır. Model bu durumda hata da vermez; kalite **sessizce** düşer, ki bu daha tehlikelidir.

```svg
<svg viewBox="0 0 640 200" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Ortada kaybolma: bağlamın baş ve sonundaki bilgiye orta bölümden daha güvenilir erişilir">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Ortada kaybolma: baş ve son güçlü, orta zayıf</text>
  <text x="34" y="80" font-size="10.5" fill="#5D7789" font-family="system-ui" transform="rotate(-90 34 80)">bulunma olasılığı</text>
  <line x1="70" y1="162" x2="580" y2="162" stroke="#5D7789" stroke-width="1.5"/>
  <rect x="110" y="62" width="110" height="100" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="165" y="182" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Bağlam başı</text>
  <text x="165" y="54" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yüksek</text>
  <rect x="265" y="122" width="110" height="40" rx="4" fill="#FBEEDC" stroke="#B26B00" stroke-width="1.5"/>
  <text x="320" y="182" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Bağlam ortası</text>
  <text x="320" y="114" text-anchor="middle" font-size="11" font-weight="700" fill="#8A2E2E" font-family="system-ui">düşük — kaybolur</text>
  <rect x="420" y="62" width="110" height="100" rx="4" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="475" y="182" text-anchor="middle" font-size="11" fill="#1C3247" font-family="system-ui">Bağlam sonu</text>
  <text x="475" y="54" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">yüksek</text>
</svg>
```

**"Pencere 200K, o zaman sorun kalmadı, değil mi?"** Hayır. Büyük pencere bir bütçe artışıdır, savurganlık ruhsatı değil. Her istekte tüm belgeleri göndermek maliyeti ve gecikmeyi katlar. Üstelik ortada kaybolma ve gürültü yüzünden doğruluğu da düşürebilir. Doğrusu, geri getirme ile **soruya özgü, küçük ve ilgili** bir bağlam kurmaktır.

**"Uzun oturumda geçmişi ne yaparsın?"** Ham geçmişi taşımak değil, **damıtmak**: eskiyen ayrıntı özetlenir, kalıcı olgular dışsal belleğe yazılır, gerektiğinde geri getirilir. Her şeyi unutturmak da yanlıştır; amaç seçici koruma.

**"Kanıt bölümü şişerse?"** Bütçe sabittir; bir bölüm şişerse başkasını gölgeler. Çözüm üç adımlıdır: kanıt sayısını `k` ile sınırla, **yeniden sıralama (reranking)** ile en ilgilileri öne al, kalanı bud. Sistem talimatını kısaltmak çözüm değildir.

### Tek cümlede

Bağlamı bir bütçe gibi yönet: doldurmak yerine seç, eskiyi özetle, ilgiliyi geri getir — az ama ilgili bağlam, her zaman çok ama gürültülü bağlamı yener.

# ctxeng-1 | Orta
## Soru
Bağlam mühendisliği ile prompt mühendisliği arasındaki temel odak farkı nedir?
## A
Prompt talimatın nasıl yazılacağına, bağlam pencereye ne konacağına odaklanır
🤖 Doğru: biri talimat tasarımı, diğeri pencere içeriği ve token bütçesi yönetimidir.
## B
İkisi tamamen aynı şeydir; yalnızca farklı dönemlerde farklı isimler almışlardır
🤖 Aynı değildir; talimat yazımı ile pencere içeriğini yönetmek farklı problemlerdir.
## C
Bağlam mühendisliği yalnızca modelin sıcaklık ve örnekleme parametrelerini ayarlamaktır
🤖 Sıcaklık bir örnekleme parametresidir; bağlam mühendisliği pencereye ne konduğuyla ilgilenir.
## D
Prompt mühendisliği modeli yeniden eğitmek, bağlam mühendisliği eğitim verisini temizlemektir
🤖 İkisi de eğitim değildir; her ikisi de çıkarım (inference) anında girdi tasarımıdır.
## Doğru
A
## Açıklama
İki disiplin tamamlayıcıdır ama odakları farklıdır: **prompt mühendisliği** "talimatı nasıl ifade ederim" (rol, format, few-shot) sorusuna; **bağlam mühendisliği** "sınırlı pencereye hangi bilgiyi, ne kadar, hangi sırayla koyarım" sorusuna odaklanır (A). Bağlam mühendisliği token bütçesi, alaka/gürültü dengesi, bellek/özetleme ve getirme kararlarını kapsar. Bunları aynı saymak (B), sıcaklıkla karıştırmak (C) veya eğitimle karıştırmak (D) yanlıştır. İyi bir prompt, kötü seçilmiş bir bağlamda işe yaramaz; ikisi birlikte tasarlanır.

# ctxeng-2 | Orta
## Soru
Bir geliştirici, doğru cevap alma ihtimalini artırmak için eldeki tüm belgeleri bağlam penceresine dolduruyor ("context stuffing"). Bu yaklaşımın temel riski nedir?
## A
Model her şeyi bir kerede gördüğü için daha hızlı çalışır; tek maliyet biraz fazla bellektir
🤖 Aksine, uzun bağlam gecikmeyi ve token maliyetini artırır; hız kazanılmaz.
## B
Hiçbir risk yoktur; daha fazla bağlam her koşulda daha iyi cevap anlamına gelir
🤖 "Daha çok bağlam = daha iyi cevap" bir yanılgıdır; gürültü doğru bilgiyi gölgeler.
## C
Maliyet ve gecikme artar; gürültü dikkati seyreltir, kritik bilgi ortada kaybolur
🤖 Doğru: doldurma alaka oranını düşürür ve uzun bağlamın ortasındaki bilginin bulunma olasılığını azaltır.
## D
Model bağlamı çok uzun bulup çalışmayı reddeder ve tamamen boş çıktı döndürür
🤖 Model reddetmez; asıl tehlike sessizce düşen kalite ve artan maliyettir.
## Doğru
C
## Açıklama
**Context stuffing** ("ne olur ne olmaz hepsini koy") klasik bir tuzaktır. Riskleri: (1) her token maliyet ve gecikme demektir, (2) ilgisiz metin modelin dikkatini seyreltir ve doğru bilgiyi gölgeler, (3) uzun bağlamın **ortasındaki** bilginin fark edilme olasılığı düşer ("lost in the middle"), (4) çelişen içerik modeli yanıltır (C). Yani **daha çok bağlam ≠ daha iyi cevap** (B yanlış). Doldurma hız kazandırmaz (A) ve modeli durdurmaz (D) — kaliteyi sessizce düşürür, ki fark edilmesi en zor bozulma budur. Doğru yol: az ama ilgili.

# ctxeng-3 | Orta
## Soru
Saatlerce süren bir asistan oturumunda konuşma geçmişi bağlam bütçesini aşmaya başlıyor. En sağlıklı bağlam yönetimi tekniği hangisidir?
## A
Sıcaklığı düşürerek modelin geçmişi kendiliğinden sıkıştırmasını sağlamak
🤖 Sıcaklık üretim çeşitliliğini ayarlar; geçmişin uzunluğunu yönetmez.
## B
Eskiyen ayrıntıyı özetlemek, kalıcı olguları dışsal belleğe yazmak
🤖 Doğru: özetleme ve dışsal bellek pencereyi sıkı tutar; alaka korunurken bütçe aşılmaz.
## C
Tüm ham geçmişi her istekte yeniden göndermeye devam edip bütçeyi zorlamak
🤖 Ham geçmişi büyütmek bütçeyi aşar, maliyeti ve gürültüyü artırır; sürdürülemez.
## D
Her mesajda oturumu sıfırlayıp önceki her şeyi tamamen unutturmak
🤖 Tümden unutturmak süreklilik ve tutarlılığı kaybettirir; amaç seçici koruma olmalıdır.
## Doğru
B
## Açıklama
Uzun oturumlarda çözüm **ham geçmişi taşımak değil, damıtmaktır**: eskiyen ayrıntı **özetlenir** (summarization/compaction) ve yoğun bir özet olarak taşınır; kalıcı olması gereken olgular **dışsal belleğe** (memory) yazılıp gerektiğinde geri getirilir (B). Böylece pencere sıkı tutulur, alaka korunur ve token bütçesi aşılmaz. Sıcaklık geçmiş yönetmez (A); ham geçmişi büyütmek bütçeyi patlatır ve gürültüyü artırır (C); her şeyi unutturmak (D) sürekliliği yok eder. İlke: hatırlanması gerekeni özetle veya sakla, gerisini bırak.

# ctxeng-4 | İleri
## Soru
Bir soru-cevap asistanında getirilen (retrieved) kanıt bölümü büyüdükçe sistem talimatı ve kullanıcının sorusu pencereden taşmaya başlıyor. En sağlıklı tasarım hangisidir?
## A
Sistem talimatını kısaltıp boşalan yeri tamamen kanıta ayırmak
🤖 Davranış kurallarını feda etmek biçim ve güvenlik garantilerini bozar; taşma sorunu yine çözülmez.
## B
Bütçe koymayıp, taşan kısmı modelin kendiliğinden eleyeceğine güvenmek
🤖 Taşma sessizce baştan/sondan kırpılmaya yol açar; model neyin düştüğünü sana söylemez.
## C
Her bölüme ayrı token bütçesi verip kanıtı k ile sınırlamak ve budamak
🤖 Doğru: sabit tavan bölümlere paylaştırılır; kanıt sıralanıp en ilgili k parça alınır, kalanı budanır.
## D
Kanıtı pencereye sığacak şekilde sondan rastgele kesip kalanını göndermek
🤖 Kör kesme cümleyi ortasından koparır ve doğru kanıtı atabilir; alakaya göre seçim gerekir.
## Doğru
C
## Açıklama
Bağlam penceresi **sabit bir tavandır** ve bir bölüm şişerse diğerini gölgeler. Mühendislik cevabı, pencereyi bilinçli **paylaştırmaktır**: sistem talimatı, soru, getirilen kanıt ve geçmiş/özet için ayrı token bütçeleri tanımlanır; kanıt tarafı `k` parça ile sınırlanır, yeniden sıralamayla en ilgililer öne alınır ve bütçeye sığmayan kısım budanır (C). Sistem talimatını feda etmek (A) davranış garantilerini bozar; bütçesiz çalışmak (B) sessiz kırpılmaya davetiyedir; kör kesme (D) cümleyi bağlamından koparıp doğru kanıtı çöpe atabilir. Kural: bütçeyi önce tasarla, sonra doldur.

# ctxeng-5 | İleri
## Soru
Uzun bir bağlamın ORTASINA yerleştirilen kritik bir olgu, model tarafından bazen gözden kaçırılıyor. Bu gözlem hangi olguyu doğrular ve pratik çıkarımı nedir?
## A
"Ortada kaybolma": baş ve sona erişim daha güvenilirdir; kritik kanıt öne/sona konumlandırılır
🤖 Doğru: uzun bağlamda orta bölge en zayıf halkadır; bağlam kısa tutulur, kanıt konumlandırılır.
## B
Modelin tamamen bozuk olduğunu; tek çözüm modeli başka bir sağlayıcınınkiyle değiştirmektir
🤖 Bu genel bir uzun-bağlam davranışıdır, tekil bir arıza değil; konumlandırma ve budama ile yönetilir.
## C
Sıcaklığın çok yüksek ayarlandığını; sıfıra çekilirse ortadaki bilgi her zaman bulunur
🤖 Sıcaklık bu konumsal dikkat etkisini çözmez; sorun bağlamın uzunluğu ve bilginin konumudur.
## D
Bağlamın yetersiz kaldığını; daha fazla belge eklenirse orta bölge güçlenecektir
🤖 Daha çok doldurmak orta bölgeyi büyütür ve riski artırır; tam ters yönde bir çıkarımdır.
## Doğru
A
## Açıklama
Bu, ölçümlerle belgelenmiş **"lost in the middle" (ortada kaybolma)** olgusudur: modeller uzun bağlamın **baş ve son** bölümündeki bilgiye, **orta** bölümündekinden daha güvenilir erişir (A). Pratik çıkarımlar: (1) kritik kanıtı bağlamın başına veya sonuna konumlandır, (2) bağlamı kısa ve ilgili tut — gereksiz doldurma orta bölgeyi büyütüp riski artırır, (3) çok sayıda parça varsa yeniden sıralama (reranking) ile en ilgiliyi öne al. Bunu modelin toptan arızası saymak (B), sıcaklığa bağlamak (C) veya daha fazla doldurmayı önermek (D) yanlıştır. Konum ve alaka, uzun bağlamda birinci sınıf tasarım değişkenleridir.

# ctxeng-6 | Uzman
## Soru
Bir mimar, 200K token pencereli bir modelde "pencere büyük, o zaman tüm belgeleri her istekte gönderelim" diyor. En doğru mühendislik itirazı hangisidir?
## A
İtiraz yok; büyük pencere tüm belgeleri her istekte göndermeyi güvenli ve ideal hâle getirir
🤖 Büyük pencere sınırın kalktığı anlamına gelmez; maliyet, gecikme ve dikkat sorunları sürer.
## B
Büyük pencere de sonlu ve pahalıdır; getirme ile küçük, ilgili bağlam kurmak daha doğrudur
🤖 Doğru: pencere büyüklüğü doldurmayı haklı çıkarmaz; seçici getirme hem ucuz hem daha isabetlidir.
## C
Tek gerçek sorun gizliliktir; onun dışında tüm belgeleri göndermek en yüksek doğruluğu verir
🤖 Doldurma yalnız gizliliği değil isabeti de bozar; sorun tek boyutlu değildir.
## D
Pencere büyükse özetleme ve getirme tamamen gereksizdir; bunlar küçük pencere çözümleridir
🤖 Aksine, büyük pencerede bile maliyet ve dikkat nedeniyle özetleme ve getirme değer üretir.
## Doğru
B
## Açıklama
Büyük bağlam penceresi (200K vb.) doldurma davranışını **haklı çıkarmaz**. Pencere hâlâ **sonlu ve pahalı** bir kaynaktır: her istekte tüm belgeleri göndermek token maliyetini ve gecikmeyi katlar, "ortada kaybolma" ve gürültüyle doğruluğu düşürebilir (B). Doğru yaklaşım, getirme (retrieval) ile **soruya özgü, ilgili ve küçük** bir bağlam kurmaktır — hem daha ucuz hem çoğu zaman daha isabetli. "Büyük pencere = doldurmak güvenli" (A), sorunu yalnız gizliliğe indirgemek (C) veya büyük pencerede özetleme/getirmeyi gereksiz saymak (D) yanlıştır. Pencere kapasitesi bir bütçe artışıdır, savurganlık ruhsatı değil.
