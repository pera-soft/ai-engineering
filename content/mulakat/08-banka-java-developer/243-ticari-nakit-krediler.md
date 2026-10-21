---
slug: ticari-nakit-krediler
baslik: "Ticari Nakit Krediler: Spot, Rotatif, Taksitli — Dört Ayrı Faiz Motoru ve Tek Model"
kademe: İleri
sira: 243
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir marangoz atölyesi düşün. Ustanın elinde tek bir tezgâh var ama üzerinde dört ayrı iş yapıyor: biri kesiyor, biri zımparalıyor, biri delik açıyor, biri cilalıyor. Tezgâh aynı, aparatlar farklı.

Ticari nakit krediler de böyledir. Müşteri aynı, limit aynı, sözleşme altyapısı aynıdır. Ama üç şey üründen ürüne tamamen değişir: **paranın ne zaman çıktığı, faizin nasıl işlediği ve borcun nasıl kapandığı.**

Bir bankada "kredili sistemler" ekibinde çalışan geliştiricinin ilk öğrendiği şey budur. Dört ürünü dört ayrı program gibi yazarsan bakımı imkânsızlaşır. Tek bir program gibi yazarsan yanlış hesaplarsın. Doğru cevap ikisinin ortasındadır ve bu konu tam olarak o ortayı anlatır.

Önemli bir bağlam notu: bu konudaki tüm ürünler **ticari/kurumsal** taraftadır. Krediyi alan bir tüketici değil, bir **firmadır**. Bireysel kredideki "aylık taksit, gelir belgesi, tüketim" mantığı burada geçerli değildir. Firma parayı harcamak için değil, **iş döngüsünü çevirmek için** alır.

### Nakit / gayrinakdi ayrımı: kurumsal bankacılığın ilk çatalı

Bir firmaya kredi tahsis edildiğinde bu limit iki farklı biçimde kullanılabilir:

- **Nakit kredi:** Bankadan gerçekten **para çıkar**. Firmanın hesabına tutar geçer. Bankanın bu işten kazancı **faizdir**. Bankanın riski, verilen anaparanın geri dönmemesidir.
- **Gayrinakdi kredi:** Bankadan **para çıkmaz**. Banka bir **taahhüt** verir: "bu firma işini yapmazsa ya da borcunu ödemezse ben öderim". Teminat mektubu, harici garanti, kontrgaranti ve kabul/aval bu gruptadır. Bankanın kazancı faiz değil **komisyondur**. Bankanın riski ise taahhüdün bir gün **tazmin edilmesidir**; yani gerçekten ödeme yapmak zorunda kalmasıdır.

Kritik nokta şudur: **ikisi de aynı limitten düşer.** Firmaya bir milyon liralık limit tahsis edildiğini düşün. Bunun bir kısmını nakit, bir kısmını teminat mektubuyla kullanmak arasında limit açısından fark yoktur; toplam risk aynı limite yazılır. Bu yüzden limit servisi "ne kadar nakit kullandırıldı" sorusunu değil, **"toplam risk ne"** sorusunu yanıtlamak zorundadır.

Genel bankacılık uygulamasında gayrinakdi risk, tazmin hâlinde **nakde döner**. Banka öder ve firmadan alacaklı hâle gelir. Yani gayrinakdi bir kayıt, nakit bir alacağa dönüşebilir. Sistemin bu geçişi modelleyebilmesi gerekir. Gayrinakdi ürünlerin ayrıntısı `244` konusundadır; bu konu nakit tarafını anlatır.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Tek limitten beslenen nakit ve gayrinakdi kredi ayrimi">
  <defs>
    <marker id="ticarikr-lim-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek limit, iki kullanım biçimi</text>
  <rect x="180" y="34" width="300" height="42" rx="9" fill="#F2F5F7" stroke="#1C3247" stroke-width="1.6"/>
  <text x="330" y="53" text-anchor="middle" font-size="12" font-weight="700" fill="#1C3247" font-family="system-ui">Firmaya tahsis edilen kredi limiti</text>
  <text x="330" y="69" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">her iki tür de AYNI limitten düşer</text>
  <path d="M300 76 L180 104" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ticarikr-lim-arw)"/>
  <path d="M360 76 L480 104" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#ticarikr-lim-arw)"/>
  <rect x="26" y="108" width="294" height="122" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="173" y="130" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">NAKİT KREDİ</text>
  <text x="173" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">para çıkar · gelir: FAİZ</text>
  <rect x="36" y="158" width="90" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="81" y="177" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Spot</text>
  <rect x="132" y="158" width="88" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="176" y="177" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Rotatif</text>
  <rect x="226" y="158" width="88" height="30" rx="7" fill="#FFFFFF" stroke="#186399" stroke-width="1.2"/>
  <text x="270" y="177" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Taksitli</text>
  <text x="173" y="210" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">risk: verilen anaparanın geri dönmemesi</text>
  <rect x="340" y="108" width="294" height="122" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.6"/>
  <text x="487" y="130" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">GAYRİNAKDİ KREDİ</text>
  <text x="487" y="148" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">para çıkmaz · gelir: KOMİSYON</text>
  <rect x="350" y="158" width="90" height="30" rx="7" fill="#FFFFFF" stroke="#B07D20" stroke-width="1.2"/>
  <text x="395" y="177" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Teminat mektubu</text>
  <rect x="446" y="158" width="88" height="30" rx="7" fill="#FFFFFF" stroke="#B07D20" stroke-width="1.2"/>
  <text x="490" y="177" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Garanti</text>
  <rect x="540" y="158" width="88" height="30" rx="7" fill="#FFFFFF" stroke="#B07D20" stroke-width="1.2"/>
  <text x="584" y="177" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kabul / aval</text>
  <text x="487" y="210" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">risk: taahhüdün tazmin edilmesi</text>
  <rect x="120" y="244" width="420" height="44" rx="9" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="330" y="264" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Tazmin halinde gayrinakdi risk nakde döner (genel uygulama)</text>
  <text x="330" y="280" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">model bu geçişi taşıyabilmeli · ayrıntı: 244</text>
</svg>
```

### İşletme sermayesi kredisi: neyi finanse eder?

Bir üretici firmanın parası şu döngüde döner: **hammadde al → üret → sat → tahsil et**. Sorun şudur: bu döngünün başı ile sonu arasında aylar geçebilir. Firma hammaddeyi bugün peşin öder, malı üç ay sonra satar, parayı beş ay sonra tahsil eder. Aradaki boşluğu kapatan finansmana **işletme sermayesi kredisi** denir.

Bu, bireysel kredideki "tüketim" mantığından temelde farklıdır. Bireysel kredide para harcanır ve geri gelmez; taksit, kişinin gelirinden ödenir. İşletme sermayesi kredisinde ise **kredi kendi geri ödemesini üretir**. Firma parayı üretime koyar ve satıştan gelen tahsilatla krediyi kapatır.

Bu farkın sistemsel sonucu şudur: vade yapısı firmanın **nakit akışına** göre kurgulanır. Mevsimsel iş yapan bir firma yazın ağır, kışın hafif ödeme isteyebilir. Bireysel kredide "her ay aynı taksit" varsayımı yeterlidir. Ticaride ise **esnek plan** normal bir gereksinimdir.

Aynı işletme sermayesi ihtiyacı üç farklı ürünle karşılanabilir: **spot**, **rotatif** veya **taksitli** kredi. Taksitli olan da **sabit faizli** ya da **değişken faizli** olabilir. Geri ödemesi **aylık eşit taksit** ya da **esnek ödemeli** kurgulanabilir. Yani tek bir ihtiyaç, sistemde dört ayrı hesaplama davranışına dağılır.

### Spot kredi: tek vade kaydı, tahakkuk tahsilattan ayrıdır

Spot kredi, faiz oranının **kullanım tarihinde belirlenip sabitlendiği** bir nakit kredi tipidir. **Faiz ve anapara ödemeleri vade sonunda** gerçekleştirilir. Sistem tarafındaki sonuçları şunlardır:

- **Taksit planı yoktur.** Ödeme planı tablosuna sekiz satır yazılmaz; **tek bir vade kaydı** vardır. Ekranda "ödeme planı" sekmesi göstermeye çalışan bir tasarım burada boş kalır. Ekran ürüne göre farklı davranmalıdır.
- **Oran kullandırımda dondurulur.** Kullandırım anındaki oran sözleşmenin parçasıdır. Piyasa oranı sonradan değişse de bu kredinin oranı değişmez.
- **Erken kapatma genelde yoktur ya da cezalıdır.** Banka bu krediyi belirli bir vade için fonlamıştır. Bu yüzden erkenden geri alınması bir maliyet doğurur. Sistemdeki karşılığı şudur: spot ürününün erken kapama davranışı diğer ürünlerden **farklıdır**. Rotatifte geri ödeme her an serbesttir; spotta ise bu bir istisna işlemidir.

En kritik ayrım şudur: **tahakkuk ile tahsilat aynı şey değildir.** Müşteri parayı vade sonunda ödese bile, muhasebe **dönemsellik** ilkesi gereği o güne ya da aya düşen faizi **gelir olarak yazar**. Bu işleme **tahakkuk** denir. Dönem sonunda henüz tahsil edilmemiş faizin kayda alınmasına ise genel uygulamada **reeskont** denir.

Yani üç aylık bir spot kredide, üç ay boyunca her gün faiz **birikir ve gelir yazılır**. Ama kasaya para yalnızca vade günü girer. Mülakatta "spot kredide faiz geliri ne zaman yazılır?" sorusuna "vade sonunda" demek eleyici bir cevaptır. Doğru cevap şudur: "Gelir dönemsel tahakkukla yazılır; **tahsilat** vade sonundadır."

```svg
<svg viewBox="0 0 660 396" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Spot rotatif taksitli sabit ve taksitli degisken faiz urunlerinin zaman ekseninde karsilastirmasi">
  <defs>
    <marker id="ticarikr-tl-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dört ürün, dört ayrı faiz motoru (aynı zaman ekseni)</text>
  <text x="20" y="62" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">SPOT</text>
  <text x="20" y="77" font-size="9.5" fill="#5D7789" font-family="system-ui">tek vade kaydı</text>
  <line x1="160" y1="70" x2="636" y2="70" stroke="#5D7789" stroke-width="1.2" marker-end="url(#ticarikr-tl-arw)"/>
  <line x1="172" y1="52" x2="172" y2="88" stroke="#25804C" stroke-width="1.8"/>
  <text x="172" y="45" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">kullandırım</text>
  <line x1="600" y1="52" x2="600" y2="88" stroke="#B23B3B" stroke-width="1.8"/>
  <text x="628" y="45" text-anchor="end" font-size="9.5" fill="#B23B3B" font-family="system-ui">vade sonu: faiz + anapara</text>
  <line x1="176" y1="70" x2="596" y2="70" stroke="#B07D20" stroke-width="1.6" stroke-dasharray="4 4"/>
  <text x="386" y="104" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">tahakkuk her gün işler (reeskont) · tahsilat vade sonunda</text>
  <text x="20" y="142" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">ROTATİF</text>
  <text x="20" y="157" font-size="9.5" fill="#5D7789" font-family="system-ui">gün × bakiye</text>
  <line x1="160" y1="152" x2="636" y2="152" stroke="#5D7789" stroke-width="1.2" marker-end="url(#ticarikr-tl-arw)"/>
  <polyline points="170,152 205,152 205,130 265,130 265,114 330,114 330,138 400,138 400,122 470,122 470,148 545,148 545,152 630,152" fill="none" stroke="#186399" stroke-width="1.8"/>
  <text x="395" y="172" text-anchor="middle" font-size="9.5" fill="#186399" font-family="system-ui">her kullandırım/geri ödeme bakiyeyi değiştirir → faiz gün×bakiye birikimi</text>
  <text x="20" y="224" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">TAKSİTLİ</text>
  <text x="20" y="239" font-size="9.5" fill="#5D7789" font-family="system-ui">sabit faiz</text>
  <line x1="160" y1="232" x2="636" y2="232" stroke="#5D7789" stroke-width="1.2" marker-end="url(#ticarikr-tl-arw)"/>
  <rect x="180" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="235" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="290" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="345" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="400" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="455" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="510" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <rect x="565" y="206" width="34" height="26" fill="#E9F4EE" stroke="#25804C" stroke-width="1.2"/>
  <text x="395" y="252" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">plan bir kez üretilir, kullandırımda sabitlenir ve değişmez</text>
  <text x="20" y="312" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">TAKSİTLİ</text>
  <text x="20" y="327" font-size="9.5" fill="#5D7789" font-family="system-ui">değişken faiz</text>
  <line x1="160" y1="320" x2="636" y2="320" stroke="#5D7789" stroke-width="1.2" marker-end="url(#ticarikr-tl-arw)"/>
  <rect x="180" y="294" width="34" height="26" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="235" y="294" width="34" height="26" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <rect x="290" y="294" width="34" height="26" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="336" y1="276" x2="336" y2="336" stroke="#B07D20" stroke-width="1.8" stroke-dasharray="4 3"/>
  <text x="342" y="272" font-size="9.5" font-weight="700" fill="#B07D20" font-family="system-ui">fonlama maliyeti değişti</text>
  <rect x="345" y="286" width="34" height="34" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <rect x="400" y="286" width="34" height="34" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <rect x="455" y="286" width="34" height="34" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <rect x="510" y="286" width="34" height="34" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <rect x="565" y="286" width="34" height="34" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.2"/>
  <text x="395" y="340" text-anchor="middle" font-size="9.5" fill="#B07D20" font-family="system-ui">geçmiş taksitler dondurulur · kalan bakiye yeni oranla yeniden üretilir</text>
  <text x="330" y="362" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Eski plan silinmez: plan versiyonlanır (v1, v2 …) — müşteri bildirimi ve denetim izi</text>
  <text x="330" y="382" text-anchor="middle" font-size="10.5" fill="#B23B3B" font-family="system-ui">Esnek planda satırlar düzensizdir; ANAPARA toplamı kullandırılan anaparaya kuruşu kuruşuna eşittir</text>
</svg>
```

### Rotatif kredi: hareket bazlı bakiye, günlük faiz işletimi

Rotatif kredi, kısa süreli nakit ihtiyacı için kullanılan bir nakit kredi ürünüdür. Özelliği şudur: **tahsis edilen limit dahilinde istenildiği zaman kullandırım ve geri ödeme** yapılabilir. Firma bugün limitin üçte birini kullanır, iki hafta sonra tahsilat gelince kapatır, ayın sonunda yeniden kullanır.

Sistemde bunun karşılığı, taksitli ürünlerden tamamen farklı bir veri modelidir:

- **Önceden üretilmiş bir ödeme planı yoktur.** Ne zaman ne kadar ödeneceği belli değildir. Ödeme takvimini müşterinin hareketleri belirler.
- **Bakiye hareket bazlıdır.** Her kullandırım bakiyeyi artırır, her geri ödeme azaltır. Doğruluk açısından kritik olan nokta şudur: bakiye tek bir alanda "güncel değer" olarak tutulmaz, **hareketlerden türetilebilir** olmalıdır. Hareket kayıtları gerçeğin kaynağıdır; bakiye onların toplamıdır.
- **Faiz günlük bakiye üzerinden işler.** Faiz, "gün sayısı × o gündeki bakiye" birikimidir. Bu yüzden bir günün ortasında yapılan geri ödemenin o güne nasıl yansıyacağı tanımlı olmalıdır; buna valör kuralı denir. Bu kural üründe parametredir ve koda gömülmez.
- **Tahsilat dönemseldir.** Biriken faiz belirli dönemlerde, genellikle ay ya da üç ay sonlarında, müşteriden tahsil edilir. Anapara ise limitin kullanılmaya devam edip etmemesine göre değişir.

Rotatifin sistemsel zorluğu, **geriye dönük hareket** geldiğinde ortaya çıkar. Geriye dönük hareket bir düzeltme, iptal ya da valör değişikliği olabilir. O tarihten sonraki tüm günlerin faizi yanlış hesaplanmış olur ve yeniden hesaplanması gerekir. Bu yüzden faiz tahakkuku **tarih bazlı ve yeniden çalıştırılabilir** kurgulanır. "Bir kez hesapladım, bir daha bakmam" tasarımı rotatifte tutmaz.

### Taksitli ticari kredi — sabit faiz

Taksitli ticari kredi, geri ödemelerin belirli vadelerde taksitler hâlinde yapıldığı nakit kredi türüdür. **Sabit** ya da **değişken** faiz oranlı olabilir.

Sabit faizlide, faiz oranı ve geri ödeme tutarları **kredi kullandırım anında belirlenir ve sabitlenir**. Bu, **fonlama maliyetinden bağımsız** olarak yapılır. Sistem açısından bu en kolay üründür: plan **bir kez** üretilir, kaydedilir ve vade boyunca değişmez.

Aylık eşit taksitli kurguda her taksitin içinde bir **anapara** ve bir **faiz** bileşeni vardır. Başlarda faiz payı ağır basar, sonlara doğru anapara payı ağır basar. Taksit tutarı ise sabit kalır.

Buradaki tek gerçek risk şudur: bankanın **fonlama maliyeti** yükselirse bu kredi bankaya daha az kazandırır. Ama bu bir iş riskidir, sistem sorunu değildir. Sistemin görevi planı doğru üretmek ve ona sadık kalmaktır.

### Taksitli ticari kredi — değişken faiz ve plan versiyonlama

Değişken faizli kredide **fonlama maliyeti değiştikçe ödenmemiş kredi bakiyesi üzerinden alınan faiz oranı ve tahsil edilen faiz tutarı değişir.** Bu cümlenin iki parçası da önemlidir. Değişen şey hem **orandır** hem de fiilen **tahsil edilen tutardır**. Ayrıca değişim **ödenmemiş bakiye** üzerinden uygulanır; geçmişe dönük uygulanmaz.

Bu, sistemde bu konunun en zor gereksinimidir. Çünkü ödeme planı artık **değişmez bir kayıt değildir; yeniden üretilebilir bir çıktıdır.** Yanlış tasarım şudur: plan satırlarını yerinde güncellemek, yani üzerine yazmak. O anda üç şey birden kaybolur. Müşteriye daha önce bildirilmiş tutarlar kaybolur. Bankanın o tarihte hangi oranı uyguladığının kanıtı kaybolur. Bir itiraz hâlinde denetime gösterilecek iz kaybolur.

Doğru tasarım **plan sürümlemedir (versiyonlama)**:

1. Oran değişimi tetiklendiğinde, o ana kadar **tahsil edilmiş taksitler dondurulur**. Bunlar gerçekleşmiş olaylardır; yeniden hesaplanmaz.
2. **Kalan anapara bakiyesi** ve kalan vade alınır. Yeni oranla **yeni bir plan sürümü** üretilir.
3. Önceki sürüm **silinmez**; geçerlilik tarihi kapatılarak saklanır. Her sürümün hangi tarihte, hangi oranla ve hangi gerekçeyle üretildiği kayıtlanır.
4. Yeni sürüm müşteriye **bildirilir**. Bildirimin ne zaman ve hangi sürüm için yapıldığı da kayda geçer.

Bunun pratik faydası şudur. Altı ay sonra müşteri "bana bu tutar bildirilmemişti" derse, sistem hangi sürümün ne zaman geçerli olduğunu gösterebilir. Ayrıca raporlama tarafında "geçen ay hangi planla çalışıyorduk" sorusu ancak sürümlü veriyle yanıtlanabilir. Kısacası: **değişken faizli üründe plan bir tablo değil, bir tarihçedir.**

Sık yapılan bir başka hata, oran değişimini şöyle çözmektir: "Krediyi kapat, yeni oranla yeni kredi aç." Bu yaklaşım sözleşme kimliğini ve kullandırım geçmişini koparır. Böylece raporlamayı, teminat eşleşmesini ve müşteri geçmişini bozar. Oran değişimi **aynı kredinin yeni bir sürümüdür**; yeni bir kredi değildir.

### Esnek ödemeli plan: düzensiz satır, kuruşu kuruşuna toplam

Taksitli kredilerin geri ödemeleri **aylık eşit taksitler hâlinde** ya da **esnek ödemeli** olarak yapılabilir. Esnek plan, mevsimsel iş yapan bir firma için doğal bir gereksinimdir. Turizmci yazın yüksek, kışın düşük öder; tarım firması hasat sonrası ağır öder.

Sistemde esnek plan satırları iki yolla belirlenebilir: bir kurala göre (örneğin mevsim katsayısı) ya da doğrudan elle. Ama esneklik **tek bir kuralı asla bozamaz**:

> Plandaki **anapara** satırlarının toplamı, kullandırılan anaparaya **kuruşu kuruşuna** eşit olmak zorundadır.

Bu kural görünürde basittir ama uygulamada en sık hata kaynağıdır. Nedeni bölme işlemidir. Tutar taksit sayısına bölündüğünde küsurat çıkar. Satır satır yuvarlama yapıldığında da toplam, kullandırılan anaparadan birkaç kuruş sapar. Doğru yaklaşım şudur (`226`):

- Tutarlar **ikilik kayan noktalı türle tutulmaz** (double/float); **ondalık duyarlı türle** tutulur. Sebebi şudur: ikilik kayan nokta, ondalık kesirleri tam temsil edemez ve para hesabında kullanılmaz.
- Yuvarlama kuralı, yani yön ve hane sayısı, **açıkça belirlenir** ve koda dağıtılmaz.
- Bölmeden doğan **kalan kuruş farkı son taksite** yazılır. Böylece hem her satır makul görünür hem de toplam tutar.
- Plan veritabanına yazılmadan önce **toplam denetimi** yapılır. Anapara toplamı kullandırılan anaparaya eşit değilse plan üretimi başarısız sayılır. Bu denetim bir "iyi olurdu" değildir; üretime çıkma şartıdır.

### TL, döviz ve dövize endeksli: kur değerleme ve kur farkı

İşletme kredileri **TL ve döviz cinsinden** kullanılabilir. Üç durumu ayırmak gerekir:

- **TL kredi:** Para TL verilir, borç TL'dir. Kur konusu yoktur.
- **Döviz kredi:** Para döviz verilir, borç dövizdir.
- **Dövize endeksli kredi:** Para **TL verilir** ama borç bir dövize bağlanır. Müşteri TL alır. Geri öderken kurun gittiği yere göre daha fazla veya daha az TL öder. Bu ürün sıkça yanlış anlaşılır. "Döviz almadıysa kur riski yok" düşüncesi hatalıdır. Riski taşıyan taraf müşteridir ve bankanın da bu bakiyeyi kur üzerinden takip etmesi gerekir.

**Kambiyo mevzuatı kısıtı.** Döviz kredisi kullanımı serbest değildir. Kimin hangi koşulda döviz kredisi kullanabileceği mevzuatla sınırlanır. Tipik koşullar ihracat ya da döviz kazandırıcı faaliyettir.

Mülakatta buradaki doğru duruş şudur: **madde numarası, oran veya tarih ezberden söylenmez.** Doğru cevap şöyledir: "Kural **tarih geçerlilikli bir parametre** olarak tutulur. Kredi tahsis anında geçerli sürüm okunur ve uygulanan sürüm kayda geçer. Kural değişikliği **iş biriminden yazılı teyitle** alınır." Mevzuat değiştiğinde kod değil parametre değişir. Eski işlemler de eski parametreyle açıklanabilir kalır. Bu duruşun kendisi teknik olgunluk göstergesidir.

**Kur değerleme ve kur farkı.** Döviz bakiyesi dönem sonunda **güncel kurla değerlenir**. Değerleme sonucunda TL karşılığı değişir. Aradaki fark **kur farkı** olarak muhasebeye **ayrı bir kalem** yazılır. Üç nokta kritiktir:

- Kur farkı **faiz değildir.** Faiz tahakkuku ile kur farkı ayrı gelir/gider kalemleridir. İkisini tek kaleme toplamak raporlamayı ve mutabakatı bozar; denetimde de savunulamaz.
- Değerleme bir **gün sonu toplu işidir** (`222`, `223`). Tüm döviz bakiyeleri taranır, değerlenir ve fark kayıtları üretilir. Bu iş yeniden çalıştırılabilir olmalı ve tekrar edilse de sonucu değişmemelidir (idempotent). Yani ikinci çalıştırma, ikinci bir kur farkı kaydı üretmemelidir.
- **Hangi kurun kullanıldığı kayıtlanır.** Alış kuru mu, satış kuru mu, merkez bankası kuru mu? Hangi tarih ve saatteki değer alındı? Bu bilgi kaydedilmezse ay sonu farkını kimse açıklayamaz. Kur kaynağı ve türü de tarih geçerlilikli parametredir.

Ayrıca tutarlar **para birimiyle birlikte** taşınmalıdır (`226`). "1000" tek başına bir bilgi değildir; "1000 USD" bilgidir. İki farklı para birimindeki tutarı doğrudan toplayan bir kod, bulunması en zor hata türlerinden birini üretir.

### Tasarım: dört ürün, tek model

Şimdi bu konunun zirvesine geldik. Dört ürünü (spot, rotatif, sabit taksitli, değişken taksitli) tek bir sistemde nasıl tasarlarsın?

**Yanlış yol 1 — tek sınıf + tip alanı + if-zinciri.** `Kredi` sınıfına bir `tip` alanı konur. Servis içinde `if (tip == SPOT) ... else if (tip == ROTATIF) ...` yazılır. Sorun şudur: bu zincir **tek bir yerde kalmaz**. Plan üretiminde bir zincir, faiz tahakkukunda bir zincir, erken kapamada bir zincir, ekstre üretiminde bir zincir olur.

Yeni bir ürün eklendiğinde bu zincirlerin **hepsini bulup açman** gerekir. Birini unutursan hata üretimde, ay sonunda ve para üzerinde ortaya çıkar. Bu, **açık-kapalı ilkesinin** (`131`) tam ihlalidir. O ilke şunu der: yazılım genişlemeye açık, değişikliğe kapalı olmalıdır.

**Yanlış yol 2 — her ürüne ayrı tablo, ayrı servis, ayrı uç nokta.** Bu sefer ürünler birbirine karışmaz. Ama **gerçekten ortak olan** her şey dörde kopyalanır: limit düşümü, teminat eşleşmesi, denetim izi, muhasebe eşlemesi, müşteri ilişkisi, para birimi taşıması. Bir denetim kuralı değiştiğinde dört yerde düzeltmen gerekir ve biri mutlaka unutulur.

**Doğru yol — ortak varlık + hesaplama stratejisi.** Ortak olan her şey ortak `Kredi` varlığında durur: müşteri, tahsis/limit ilişkisi, para birimi ile tutar, kullandırım tarihi, vade, sözleşme referansı, durum, denetim izi. Üründen ürüne **değişen davranışlar** ise bir strateji arayüzünün arkasına alınır (`135`). En az üç davranış vardır:

- **Plan üretimi:** Spotta plan yoktur, tek vade kaydı vardır. Sabit taksitlide plan bir kez üretilir. Değişken taksitlide yeniden üretilebilir ve sürümlenir. Rotatifte ise hiç plan yoktur.
- **Faiz tahakkuku:** Spotta oran sabittir ve faiz gün gün birikir. Rotatifte gün × değişen bakiye olarak işler. Taksitlide plandaki faiz bileşenine göre işler.
- **Erken kapama:** Spotta yoktur ya da cezalıdır. Rotatifte serbest geri ödeme zaten ürünün doğasıdır. Taksitlide kalan anapara ve hak edilmemiş faiz hesabıyla yapılır.

Bu ayrımın kazancı somuttur: **yeni bir ürün eklemek, mevcut koda dokunmadan yeni bir uygulama sınıfı yazmak demektir.** Ortak akış değişmez; yani limit kontrolü, kullandırım, muhasebe ve denetim aynı kalır. Yalnızca hesaplama davranışı eklenir.

İki tasarım kuralı daha bu modelin ayrılmaz parçasıdır:

- **Para birimi ve tutar birlikte taşınır.** Strateji arayüzü çıplak sayı almaz; para birimiyle birlikte tutar alır ve döndürür (`226`).
- **Tahakkuk motoru tarih bazlı çalışır.** Arayüzün sorusu "faiz ne kadar?" değildir. Soru şudur: **"Şu tarih aralığında ne birikti?"** Böylece geriye dönük bir düzeltme geldiğinde ilgili aralık yeniden hesaplanabilir. Rotatif ve değişken faizli üründe bu zorunludur.

```svg
<svg viewBox="0 0 660 332" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="If zinciri yerine ortak Kredi varligi ve urune gore hesaplama stratejisi">
  <defs>
    <marker id="ticarikr-str-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#25804C"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Tek Kredi modeli, ürüne göre değişen hesaplama stratejisi</text>
  <rect x="16" y="40" width="290" height="132" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="161" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">YANLIŞ: tip alanına bakan if-zinciri</text>
  <text x="30" y="84" font-size="10" fill="#1C3247" font-family="system-ui">if (tip == SPOT) { … }</text>
  <text x="30" y="100" font-size="10" fill="#1C3247" font-family="system-ui">else if (tip == ROTATIF) { … }</text>
  <text x="30" y="116" font-size="10" fill="#1C3247" font-family="system-ui">else if (tip == TAKSITLI_SABIT) { … }</text>
  <text x="30" y="132" font-size="10" fill="#1C3247" font-family="system-ui">else if (tip == TAKSITLI_DEGISKEN) { … }</text>
  <text x="161" y="152" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">aynı zincir plan · tahakkuk · kapama · ekstrede tekrarlar</text>
  <text x="161" y="166" text-anchor="middle" font-size="9.5" font-weight="700" fill="#B23B3B" font-family="system-ui">açık-kapalı ilkesi (131) ihlal edilir</text>
  <rect x="330" y="40" width="314" height="132" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="487" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">DOĞRU: strateji deseni (135)</text>
  <text x="344" y="84" font-size="10" fill="#1C3247" font-family="system-ui">Kredi (ortak varlık): müşteri, tahsis, vade,</text>
  <text x="344" y="100" font-size="10" fill="#1C3247" font-family="system-ui">para birimi + tutar birlikte (226), denetim izi</text>
  <text x="344" y="120" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">KrediHesaplamaStratejisi (arayüz)</text>
  <text x="344" y="136" font-size="10" fill="#1C3247" font-family="system-ui">planUret() · faizTahakkukEt(tarihAraligi) · erkenKapat()</text>
  <text x="487" y="160" text-anchor="middle" font-size="9.5" font-weight="700" fill="#186399" font-family="system-ui">yeni ürün = yeni sınıf; mevcut kod değişmez</text>
  <path d="M487 172 L487 184" fill="none" stroke="#25804C" stroke-width="1.4"/>
  <line x1="87" y1="184" x2="573" y2="184" stroke="#25804C" stroke-width="1.4"/>
  <path d="M87 184 L87 194" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#ticarikr-str-arw)"/>
  <path d="M249 184 L249 194" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#ticarikr-str-arw)"/>
  <path d="M411 184 L411 194" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#ticarikr-str-arw)"/>
  <path d="M573 184 L573 194" fill="none" stroke="#25804C" stroke-width="1.4" marker-end="url(#ticarikr-str-arw)"/>
  <rect x="12" y="198" width="150" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="87" y="217" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">SpotStrateji</text>
  <text x="87" y="233" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">plan yok, tek vade</text>
  <text x="87" y="247" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tahakkuk ≠ tahsilat</text>
  <rect x="174" y="198" width="150" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="249" y="217" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">RotatifStrateji</text>
  <text x="249" y="233" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hareket bazlı bakiye</text>
  <text x="249" y="247" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">günlük bakiye faizi</text>
  <rect x="336" y="198" width="150" height="58" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="411" y="217" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">SabitTaksitStrateji</text>
  <text x="411" y="233" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">plan bir kez üretilir</text>
  <text x="411" y="247" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">oran kullandırımda sabit</text>
  <rect x="498" y="198" width="150" height="58" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.3"/>
  <text x="573" y="217" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">DegiskenTaksitStrateji</text>
  <text x="573" y="233" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">plan yeniden üretilir</text>
  <text x="573" y="247" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sürüm saklanır</text>
  <rect x="20" y="268" width="620" height="50" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4" stroke-dasharray="5 3"/>
  <text x="330" y="288" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Tahakkuk motoru tarih bazlıdır: her ürün "bu aralıkta ne birikti?" sorusunu kendi kuralıyla yanıtlar</text>
  <text x="330" y="306" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kur değerlemesi ve kur farkı ayrı kalemdir; gün sonu toplu işidir (222/223) ve idempotent olmalıdır</text>
</svg>
```

### Mülakatta ne sorulur / tuzaklar

- **"Spot ile rotatif arasındaki fark nedir?"** Ürün broşürünü tekrarlamak yetmez; **sistemdeki** farkı söyle. Spotta taksit planı yoktur, tek vade kaydı vardır. Rotatifte ise plan hiç yoktur; bakiye hareketlerden türetilir ve faiz gün × bakiye olarak işler.
- **"Spot kredide faiz geliri ne zaman yazılır?"** "Vade sonunda" cevabı eleyicidir. Doğru cevap şudur: **tahakkuk dönemseldir (reeskont), tahsilat vade sonundadır.** Bu iki kavramı ayıramayan aday, muhasebe tarafını hiç görmemiş sayılır.
- **"Değişken faizli kredide oran değişti, ne yaparsın?"** Plan satırlarının **üzerine yazmak** yanlış cevaptır. Beklenen cevap şudur: geçmiş dondurulur, kalan bakiye yeni oranla yeniden hesaplanır, **yeni plan sürümü** üretilir, eski sürüm saklanır ve müşteriye bildirilir. "Krediyi kapatıp yenisini açarım" da yanlıştır.
- **"Esnek planda kuruş farkını ne yaparsın?"** "Yuvarlarım" yetersizdir. Beklenen dört şeydir: ondalık duyarlı tür, tanımlı yuvarlama kuralı, **kalan kuruşun son taksite** yazılması ve kaydetmeden önce **anapara toplamı denetimi**.
- **"Döviz kredide dönem sonunda ne olur?"** Kur değerlemesi yapılır. **Kur farkı ayrı kalem** olarak yazılır. Bu iş bir gün sonu toplu işidir ve **hangi kurun** kullanıldığı kayıtlanır. Kur farkını faize katmak yanlıştır.
- **"Dövize endeksli kredide kur riski var mı?"** Var. Para TL verilir ama borç dövize bağlıdır. Riski müşteri taşır; banka da bakiyeyi kur üzerinden takip eder.
- **"Kambiyo mevzuatında şu oran nedir?"** Ezberden madde ya da oran söyleme. Doğru duruş şudur: kural **tarih geçerlilikli parametreden** okunur, uygulanan sürüm kayıtlanır ve değişiklik **iş biriminden yazılı teyitle** alınır.
- **"Dört ürünü tek sistemde nasıl tasarlarsın?"** `tip` alanına bakan if-zinciri **yanlış cevaptır**. Çünkü zincir tek yerde kalmaz ve her yeni ürün mevcut kodu açmayı gerektirir. Beklenen cevap şudur: ortak `Kredi` varlığı + ürüne göre değişen **hesaplama stratejisi**. Plan üretimi, tahakkuk ve erken kapama, strateji davranışlarıdır.
- **Sık yapılan hata:** ürünleri ezberleyip sistemsel sonucunu söylememek. Mülakatta değerli olan "spot şudur" cümlesi değildir. Değerli olan şudur: **"Spot olduğu için sistemde şu yoktur, şu vardır."**

### Tek cümlede

Ticari nakit krediler tek limitten beslenir ama dört ayrı faiz motoru gerektirir. **Spot** tek vade kaydıdır ve tahakkuku tahsilatından ayrıdır. **Rotatif**, hareket bazlı bakiye üzerinde gün × bakiye faizi işletir. **Sabit taksitli**, planı bir kez üretip dondurur. **Değişken taksitli** ise planı yeniden üretilebilir ve **sürümlenebilir** kılmak zorundadır. Esnek planda satırlar düzensiz olabilir ama **anapara toplamı kuruşu kuruşuna** kullandırılan anaparaya eşittir. Dövizde bakiye dönem sonunda değerlenir ve **kur farkı ayrı kalem** olarak yazılır. Bu dördünü tek modelde toplamanın yolu, `tip` alanına bakan if-zinciri değildir; ortak `Kredi` varlığı ile **ürüne göre değişen hesaplama stratejisidir**.

# ticari-1 | Orta
## Soru
Spot ve rotatif ticari kredilerin aynı çekirdek üzerinde tutulması isteniyor. Bu iki ürünün sistemdeki temel farkı nedir?
## A
Her ikisinde de taksit planı üretilir; fark yalnız taksit sayısındadır — spot tek satırlık, rotatif ise çok satırlı bir plan tablosu kullanır
🤖 rotatifte önceden üretilmiş bir plan yoktur; ödeme takvimini müşterinin kullandırım ve geri ödeme hareketleri belirler.
## B
Spot kredide faiz oranı vade boyunca değişebilir, rotatifte kullandırım anında sabitlenir; iki motorun farkı bu oran davranışından doğar
🤖 durum tam tersidir — spotta oran kullanım tarihinde belirlenip sabitlenir, rotatifte faiz değişen bakiye üzerinden gün gün işler.
## C
Spotta plan yok, tek vade kaydı vardır ve ödeme vade sonundadır; rotatifte bakiye hareketlerden türetilir, faiz günlük bakiye üzerinden işler
🤖 Doğru: iki ürünün veri modeli ve faiz hesaplama biçimi birbirinden tamamen farklıdır.
## D
Fark yalnızca muhasebeseldir; her iki üründe de faiz aynı motorla, kalan anapara üzerinden eşit taksit formülüyle hesaplanıp dönemlere dağıtılır
🤖 eşit taksit formülü taksitli ürünlere aittir; spot tek vadelidir, rotatifte ise formül değil hareket bazlı birikim vardır.
## Doğru
C
## Açıklama
**Spot kredi**, faiz oranının kullanım tarihinde belirlenip sabitlendiği, **faiz ve anaparanın vade sonunda** ödendiği üründür; sistemde bunun karşılığı ödeme planı tablosunda satırlar değil **tek bir vade kaydıdır**. **Rotatif kredi** ise tahsis edilen limit dahilinde **istenildiği zaman kullandırım ve geri ödeme** yapılabilen üründür; burada önceden üretilmiş bir plan hiç yoktur, bakiye hareketlerden türetilir ve faiz **gün sayısı × o gündeki bakiye** birikimi olarak işler. "Fark taksit sayısıdır" demek rotatifin doğasını kaçırır: rotatifte taksit kavramı yoktur, ödeme takvimini müşteri belirler. "Spotta oran değişkendir" iddiası ürün tanımının tersidir — spotta oran kullandırımda dondurulur. "Aynı motor, eşit taksit formülü" cevabı ise iki ürünü de yanlış anlatır: eşit taksit formülü **taksitli** ürünlere aittir, spotta tek ödeme vardır, rotatifte ise formül yerine gün gün biriken bir hesap vardır. Bu farkın tasarımdaki sonucu, plan üretimi ve faiz tahakkukunun ürüne göre **ayrı davranışlar** olmasıdır.

# ticari-2 | İleri
## Soru
Değişken faizli taksitli ticari kredide fonlama maliyeti değişti ve müşteriye daha önce bir ödeme planı bildirilmişti. Sistem bu değişimi nasıl işlemelidir?
## A
Mevcut plan satırları yeni oranla yerinde güncellenir ve eski değerler saklanmaz; tek geçerli plan bulunması ekranda ve bildirimde karışıklığı önler
🤖 müşteriye bildirilmiş tutarlar ve o tarihte uygulanan oranın kanıtı kaybolur, itiraz halinde gösterilecek denetim izi kalmaz.
## B
Tahsil edilmiş taksitler dondurulur, kalan bakiye yeni oranla yeniden hesaplanır ve yeni bir plan sürümü üretilir; önceki sürüm saklanır
🤖 Doğru: plan değişmez bir kayıt değil yeniden üretilebilir bir çıktı olarak modellenir ve tarihçe korunur.
## C
Plan hiç değiştirilmez; oran değişiminden doğan faiz farkı biriktirilip vade sonunda tek kalemde tahsil edilir, taksit tutarları sabit kalır
🤖 üründe tahsil edilen faiz tutarı ödenmemiş bakiye üzerinden değişmelidir; farkı vadeye ötelemek ürünü fiilen sabit faizliye çevirir.
## D
Kredi kapatılıp aynı müşteriye yeni oranla yeni bir kredi açılır; her oran için tek ve net bir kayıt oluşur, geçmiş plan kapanmış kredide kalır
🤖 sözleşme kimliği ve kullandırım geçmişi kopar, raporlama, teminat eşleşmesi ve müşteri geçmişi bozulur.
## Doğru
B
## Açıklama
Değişken faizli kredide **fonlama maliyeti değiştikçe ödenmemiş bakiye üzerinden alınan oran ve tahsil edilen faiz tutarı değişir.** Bunun sistemdeki karşılığı, ödeme planının değişmez bir kayıt değil **yeniden üretilebilir bir çıktı** olmasıdır. Doğru akış dört adımlıdır: tahsil edilmiş taksitler **dondurulur** (gerçekleşmiş olaylar yeniden hesaplanmaz), **kalan bakiye** ve kalan vade yeni oranla yeniden hesaplanır, **yeni bir plan sürümü** üretilir ve önceki sürüm silinmeden geçerlilik tarihiyle saklanır, son olarak yeni sürüm müşteriye **bildirilir** ve bildirim kayda geçer. Plan satırlarını yerinde güncellemek en yaygın yanlıştır: aynı anda müşteriye bildirilen tutarlar, o tarihte uygulanan oranın kanıtı ve denetime gösterilecek iz kaybolur — altı ay sonra "bana bu bildirilmemişti" itirazı yanıtsız kalır. Farkı vade sonuna ötelemek ürün tanımını bozar; değişimin **ödenmemiş bakiye üzerinden** yansıması gerekir, aksi halde kredi fiilen sabit faizli hale gelir. Krediyi kapatıp yenisini açmak ise en zararlı çözümdür: oran değişimi **aynı kredinin yeni bir sürümüdür**, yeni bir kredi değildir; kimliği koparmak kullandırım geçmişini, teminat eşleşmesini ve raporlama sürekliliğini bozar. Özetle değişken faizli üründe plan bir tablo değil, bir **tarihçedir**.

# ticari-3 | Orta
## Soru
Mevsimsel çalışan bir firmaya esnek ödemeli taksitli kredi planı üretiliyor; satır tutarları düzensiz ve bölme işleminden küsurat çıkıyor. Doğru yaklaşım nedir?
## A
Her taksit tutarı yukarı yuvarlanır; oluşan küçük fark banka lehinedir ve kuruş düzeyinde kaldığı için müşteri açısından önemsizdir
🤖 satır satır yukarı yuvarlama toplamı kullandırılan anaparanın üzerine çıkarır ve plan anapara toplamı denetimini geçemez.
## B
Tutarlar kayan noktalı türle (double) hesaplanıp ekranda iki ondalık haneyle gösterilir; görüntüdeki tutarlar doğru olduğu sürece sorun oluşmaz
🤖 ikilik kayan nokta ondalık kesirleri tam temsil edemez, birikerek sapma üretir ve para hesabında kullanılmaz.
## C
Küsurat tüm taksitlere eşit dağıtılır, dağıtımdan artan kuruş yok sayılır; kuruş düzeyindeki sapma raporlamada ihmal edilebilir kalır
🤖 yok sayılan kuruş toplamı bozar; anapara toplamının kullandırılan anaparaya eşitliği ihmal edilebilir bir ayrıntı değil, üretim şartıdır.
## D
Ondalık duyarlı tür ve tanımlı yuvarlama kuralı kullanılır, kalan kuruş farkı son taksite yazılır ve kayıttan önce anapara toplamı denetlenir
🤖 Doğru: hem her satır makul kalır hem toplam kuruşu kuruşuna tutar ve hata plan kaydedilmeden yakalanır.
## Doğru
D
## Açıklama
Esnek ödemeli planda satır tutarları düzensiz olabilir — mevsimsel iş yapan firmada bazı aylar ağır, bazıları hafiftir — ama esneklik tek bir kuralı bozamaz: **anapara satırlarının toplamı, kullandırılan anaparaya kuruşu kuruşuna eşit olmalıdır.** Hatanın kaynağı bölmedir: tutar taksitlere bölündüğünde küsurat çıkar ve satır satır yuvarlandığında toplam sapar. Doğru yöntem dört parçalıdır (`226`): tutarlar **ondalık duyarlı türle** tutulur, **yuvarlama kuralı** (yön ve hane) açıkça belirlenir, bölmeden artan **kuruş farkı son taksite** yazılır ve plan veritabanına yazılmadan **toplam denetimi** yapılır; denetim tutmuyorsa plan üretimi başarısız sayılır. Satır satır yukarı yuvarlamak toplamı kullandırılan anaparanın üzerine çıkarır ve bu denetimi geçemez. **Kayan noktalı tür** (double/float) para hesabında kullanılmaz; ikilik gösterim ondalık kesirleri tam temsil edemediği için hata birikir ve "ekranda doğru görünüyor" durumu asıl tehlikeyi gizler. "Artan kuruşu yok saymak" ise sorunu adlandırıp çözmemektir: yok sayılan kuruş toplamı bozar, kredi bakiyesi hiçbir zaman tam kapanmaz ve mutabakatta açık kalır.

# ticari-4 | İleri
## Soru
Döviz cinsinden kullandırılmış bir ticari kredide dönem sonunda kur değişti. Sistemin bu durumda yapması gereken nedir?
## A
Döviz bakiyesi dönem sonunda güncel kurla değerlenir ve kur farkı ayrı bir kalem yazılır; bu bir gün sonu toplu işidir, kullanılan kur kayıtlanır
🤖 Doğru: değerleme dönemseldir, kur farkı faizden ayrı bir kalemdir ve kullanılan kurun kaynağı sonradan açıklanabilir olmalıdır.
## B
Döviz bakiyesi kullandırım günündeki kurla sabit tutulur ve kur etkisi yalnız kredi kapandığında tek seferde hesaplanıp muhasebeleştirilir
🤖 dönem sonu değerlemesi yapılmazsa bakiyenin güncel TL karşılığı ve taşınan risk mali tablolarda görünmez.
## C
Kur farkı dönemin faiz tahakkukuna eklenerek tek bir gelir kalemi halinde muhasebeleştirilir; böylece kredinin toplam getirisi tek yerde görünür
🤖 faiz ile kur farkı ayrı gelir/gider kalemleridir; birleştirmek raporlamayı ve mutabakatı bozar, denetimde savunulamaz.
## D
Dövize endeksli kredilerde para döviz olarak kullandırıldığından bakiye zaten döviz cinsindedir; ayrıca kur değerlemesi yapılmasına gerek yoktur
🤖 dövize endekslide para TL verilir, borç dövize bağlıdır; tam da bu yüzden bakiyenin kur üzerinden takibi gerekir.
## Doğru
A
## Açıklama
Döviz bakiyesi dönem sonunda **güncel kurla değerlenir**; TL karşılığındaki değişim **kur farkı** olarak muhasebeye **ayrı bir kalem** yazılır. Üç ayrıntı mülakatta ayırt edicidir. Birincisi, kur farkı **faiz değildir**; faiz tahakkuku ve kur farkı ayrı gelir/gider kalemleridir ve tek kaleme toplanırsa hem raporlama hem mutabakat bozulur. İkincisi, değerleme bir **gün sonu toplu işidir** (`222`, `223`): tüm döviz bakiyeleri taranır ve fark kayıtları üretilir; iş yeniden çalıştırılabilir ve idempotent olmalıdır, ikinci çalıştırma ikinci bir kur farkı kaydı üretmemelidir. Üçüncüsü, **hangi kurun** kullanıldığı — alış mı, satış mı, merkez bankası kuru mu, hangi tarih ve saatteki değer — kayıtlanmalıdır; kaydedilmezse ay sonundaki farkı kimse açıklayamaz, kur kaynağı ve türü de tarih geçerlilikli parametredir. Bakiyeyi kullandırım günündeki kurla dondurmak, taşınan riski mali tablolardan gizler ve değerlemenin amacını ortadan kaldırır. **Dövize endeksli** kredide ise para **TL verilir** ama borç dövize bağlıdır; "döviz almadı, kur riski yok" düşüncesi hatalıdır — riski müşteri taşır ve bankanın da bu bakiyeyi kur üzerinden takip etmesi gerekir. Ayrıca tutar her zaman **para birimiyle birlikte** taşınmalıdır (`226`); çıplak sayı bir bilgi değildir.

# ticari-5 | Uzman
## Soru
Spot, rotatif, sabit faizli taksitli ve değişken faizli taksitli ürünleri tek bir kredi sisteminde tasarlaman isteniyor. Hangi yaklaşım doğrudur ve neden?
## A
Tek bir Kredi sınıfı ve bir tip alanı tutulur; plan üretimi, faiz tahakkuku ve erken kapama servis içinde tipe bakan if/else zincirleriyle ayrıştırılır
🤖 aynı zincir plan, tahakkuk, kapama ve ekstre üretiminde tekrarlanır; yeni ürün eklemek mevcut kodu her yerde açmayı gerektirir.
## B
Her ürün için ayrı tablo, ayrı servis ve ayrı uçnokta oluşturulur; ortak soyutlama kurulmaz, böylece hiçbir ürün diğerini etkilemez
🤖 limit düşümü, teminat eşleşmesi, denetim izi ve muhasebe eşlemesi gibi gerçekten ortak davranışlar dört yere kopyalanır.
## C
Ortak Kredi varlığı ile ürüne göre değişen hesaplama stratejisi ayrılır; plan üretimi, tahakkuk ve erken kapama strateji davranışlarıdır
🤖 Doğru: ortak akış tek yerde kalır, değişen davranış izole edilir ve genişleme mevcut koda dokunmadan yapılır.
## D
Ürünler arasındaki farklar veritabanında yapılandırma satırlarına indirgenir ve tek bir genel faiz formülü bu parametrelerle çalıştırılır
🤖 dört ürünün farkı parametre değil davranış farkıdır; spotta plan hiç yoktur, rotatifte faiz gün×bakiye birikimidir, değişkende plan yeniden üretilir.
## Doğru
C
## Açıklama
Doğru tasarım, **ortak olanı ortak varlıkta, değişeni strateji arkasında** tutmaktır. `Kredi` varlığında müşteri, tahsis/limit ilişkisi, para birimiyle birlikte tutar, kullandırım tarihi, vade, sözleşme referansı, durum ve denetim izi durur. Üründen ürüne değişen en az üç davranış ise bir strateji arayüzüne alınır (`135`): **plan üretimi** (spotta plan yok, rotatifte hiç yok, sabit taksitlide bir kez, değişkende yeniden üretilebilir ve sürümlü), **faiz tahakkuku** (spotta sabit oranla gün gün, rotatifte gün × değişen bakiye, taksitlide plandaki faiz bileşenine göre) ve **erken kapama** (spotta yok/cezalı, rotatifte serbest, taksitlide kalan anapara hesabıyla). `tip` alanına bakan **if/else zinciri** ilk akla gelen ama en pahalı yoldur: zincir tek bir yerde kalmaz, plan üretiminde, tahakkukta, erken kapamada ve ekstre üretiminde ayrı ayrı tekrarlanır; yeni ürün eklendiğinde hepsini bulup açmak gerekir ve unutulan biri hatayı üretimde, ay sonunda, para üzerinde gösterir — bu **açık-kapalı ilkesinin** (`131`) doğrudan ihlalidir. Her ürüne tamamen ayrı yığın kurmak ters uçtaki hatadır: ürünler karışmaz ama limit düşümü, teminat eşleşmesi, denetim izi ve muhasebe eşlemesi gibi gerçekten ortak davranışlar dört kez yazılır ve bir kural değiştiğinde biri mutlaka güncellenmeden kalır. Farkları tek bir genel formülün parametrelerine indirgemek de mümkün değildir; aradaki fark sayısal değil **yapısaldır**. Modeli tamamlayan iki kural: tutar **para birimiyle birlikte** taşınır (`226`) ve tahakkuk motoru **tarih bazlı** çalışır — sorusu "faiz ne kadar?" değil "şu tarih aralığında ne birikti?" olmalıdır, çünkü geriye dönük düzeltmede ilgili aralığın yeniden hesaplanması gerekir.

# ticari-6 | İleri
## Soru
Üç ay vadeli bir spot ticari kredi kullandırıldı; faiz ve anapara vade sonunda tahsil edilecek. Ay sonu kapanışında muhasebe açısından ne olur?
## A
Faiz geliri yalnız vade sonunda, tahsilat gerçekleştiğinde yazılır; ara dönemlerde herhangi bir gelir ya da alacak kaydı oluşmaz
🤖 dönemsellik gereği o döneme düşen faiz gelir olarak tahakkuk ettirilir; gelir kaydı için tahsilat beklenmez.
## B
O döneme düşen faiz tahakkukla gelir yazılır (reeskont), nakit tahsilat vade sonundadır; tahakkuk ile tahsilat ayrı kavramlardır
🤖 Doğru: gelir hak edildiği dönemde kayda alınır, para ise sözleşmedeki vade gününde kasaya girer.
## C
Faizin tamamı kullandırım gününde peşin gelir olarak yazılır; oran kullandırımda sabitlendiği için toplam tutar zaten baştan bellidir
🤖 tutarın baştan belli olması gelirin hak edildiği anlamına gelmez; gelir vade boyunca dönemlere dağıtılır.
## D
Ay sonunda müşteriden o aya düşen faiz kısmi olarak tahsil edilir, kalan faiz ve anapara vade sonunda birlikte alınır
🤖 spot üründe faiz ve anapara vade sonunda ödenir; tahakkuk bir muhasebe kaydıdır, müşteriden para talebi değildir.
## Doğru
B
## Açıklama
Bu soru **tahakkuk** ile **tahsilat** ayrımını ölçer ve mülakatta en sık yanlış cevaplanan noktalardan biridir. Spot kredide **faiz ve anapara ödemeleri vade sonunda** gerçekleşir — bu **tahsilatın** ne zaman olacağını söyler. Muhasebe ise **dönemsellik** ilkesiyle çalışır: gelir, hak edildiği dönemde kayda alınır. Dolayısıyla üç aylık bir spot kredide faiz her gün **birikir ve dönem sonlarında gelir yazılır**; dönem sonunda henüz tahsil edilmemiş faizin kayda alınmasına genel uygulamada **reeskont** denir. Kasaya para yalnızca vade günü girer. "Gelir yalnız tahsilatta yazılır" cevabı dönemselliği yok sayar ve ara dönem mali tablolarını olduğundan kötü gösterir. "Faizin tamamı kullandırımda peşin yazılır" cevabı ters yöndeki hatadır: oranın ve tutarın baştan belli olması, gelirin baştan **hak edildiği** anlamına gelmez; henüz geçmemiş döneme ait faiz gelir sayılamaz. "Ay sonunda kısmi tahsilat" cevabı ise iki kavramı karıştırır — tahakkuk bir **muhasebe kaydıdır**, müşteriden para talebi değildir; müşteriye ay sonunda bir ödeme yükümlülüğü doğsaydı ürün zaten spot olmazdı. Sistem tarafındaki sonucu şudur: spot ürününün tahakkuk davranışı, plan satırlarından okuyan taksitli üründen tamamen farklı bir motordur; tahakkuk **tarih bazlı** çalışır ve tahsilat kaydından bağımsız üretilir.
