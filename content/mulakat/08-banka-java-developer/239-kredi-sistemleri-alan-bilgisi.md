---
slug: kredi-sistemleri-alan-bilgisi
baslik: "Kredi Sistemleri Alan Bilgisi: Ürünler, Başvurudan Takibe Yaşam Döngüsü, Temel Kavramlar"
kademe: Orta
sira: 239
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir komşundan borç para aldığını düşün. Ne kadar aldığın, ne zaman ödeyeceğin ve karşılığında ne vereceğin bellidir. Ödemeyi geciktirirsen araya başka bir konuşma girer. Banka aynı işi yüz binlerce kişi için, kayıt altında ve kurala bağlı yapar. O kural setinin adı **kredi ürünüdür**. Kaydın adı ise **başvuru, sözleşme, ödeme planı ve muhasebe fişidir**.

Bir Java geliştiricisi olarak kredi ekranlarının arkasındaki servisleri yazacaksan bu kelimeleri bilmen gerekir. Çünkü iş birimi seninle bu dille konuşur. "Kullanılabilir limit", "tahakkuk", "erken kapama", "fek" ve "valör" kelimelerini bilmeyen geliştirici, doğru kodu yanlış gereksinim üzerine yazar.

Bu konu teknik derinlik vermez; **alan bilgisini** verir. Teknik karşılıkları için ilgili konulara atıf yapılır: para hesabı `226`, kilitleme `225`, izolasyon `224`, tekrar güvenliği ve giden kutusu tablosu `130`, toplu iş `222`/`223`, Oracle `227`.

### Kredi ürünleri ve farkları

Ürünler iki büyük aileye ayrılır ve bu ayrım doğrudan **veri modelini** değiştirir:

- **Taksitli (kapalı devre) krediler:** Para tek seferde kullandırılır; bazen birkaç dilimde verilir. Karşılığında bir **ödeme planı** üretilir ve plan bitince kredi kapanır. **İhtiyaç kredisi** teminatsızdır. **Taşıt kredisi** genelde araç üzerinde **rehin** ile teminatlandırılır; **konut kredisi** ise taşınmaz üzerinde **ipotek** ile. **Ticari/kurumsal krediler** hem taksitli hem farklı teminat yapılarıyla kurgulanabilir.
- **Rotatif (döner) krediler:** Bir **limit** tanımlanır. Müşteri bu limiti tekrar tekrar kullanır ve ödedikçe limit yeniden doğar. Sabit bir ödeme planı yoktur. **KMH (kredili mevduat hesabı)** budur: vadesiz hesap eksiye düştüğünde limitten kullanılmış olur. Faiz de **kullanılan tutar ve kullanılan gün** üzerinden işler. **Kredi kartı** da rotatiftir; ekstre, asgari ödeme ve **taksitli nakit avans** gibi ek kavramlar taşır.
- **Spot ile taksitlinin farkı:** **Spot kredi** genelde ticari tarafta görülür. Anapara vade sonunda tek seferde ödenir; faiz ayrı bir düzende tahsil edilir. **Taksitli kredide** ise anapara ve faiz taksitlere dağıtılır. Sistem açısından fark, ödeme planının satır sayısı ve tahsilat tetikleyicisidir.

**Teminatlı ile teminatsız ürünün sistem tarafındaki farkı** şudur. Teminatlı üründe başvuru akışına üç adım eklenir: **değerleme/ekspertiz**, **teminat kaydı** ve **rehin/ipotek tesisi**. Kredi kapandığında ise teminatın çözülmesi gerekir; buna **fek** denir.

Yani teminatlı ürün, teminatsıza göre ek bir varlık (teminat) ve ek bir durum makinesi demektir. Ayrıca çoğu zaman dış kurum entegrasyonu içerir. Bu adımlar yavaştır, hataya açıktır ve **yeniden denenebilir** olmalıdır.

```svg
<svg viewBox="0 0 660 232" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Taksitli ve rotatif kredi ürünlerinin karşılaştırması">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İki ürün ailesi: taksitli (kapalı devre) ve rotatif (döner limit)</text>
  <rect x="28" y="44" width="280" height="156" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="168" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#186399" font-family="system-ui">TAKSİTLİ ÜRÜNLER</text>
  <text x="46" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">· İhtiyaç (teminatsız)</text>
  <text x="46" y="110" font-size="10.5" fill="#1C3247" font-family="system-ui">· Taşıt (rehinli) · Konut (ipotekli)</text>
  <text x="46" y="128" font-size="10.5" fill="#1C3247" font-family="system-ui">· Ticari taksitli · spot (vade sonu)</text>
  <text x="46" y="152" font-size="10.5" fill="#5D7789" font-family="system-ui">Kullandırım tek sefer → ödeme planı</text>
  <text x="46" y="170" font-size="10.5" fill="#5D7789" font-family="system-ui">üretilir; plan bitince kredi kapanır.</text>
  <text x="46" y="188" font-size="10.5" fill="#5D7789" font-family="system-ui">Limit kullandırımda düşer.</text>
  <rect x="352" y="44" width="280" height="156" rx="12" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="492" y="68" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B07D20" font-family="system-ui">ROTATİF ÜRÜNLER</text>
  <text x="370" y="92" font-size="10.5" fill="#1C3247" font-family="system-ui">· KMH (kredili mevduat hesabı)</text>
  <text x="370" y="110" font-size="10.5" fill="#1C3247" font-family="system-ui">· Kredi kartı</text>
  <text x="370" y="128" font-size="10.5" fill="#1C3247" font-family="system-ui">· Taksitli nakit avans</text>
  <text x="370" y="152" font-size="10.5" fill="#5D7789" font-family="system-ui">Limit tekrar tekrar kullanılır;</text>
  <text x="370" y="170" font-size="10.5" fill="#5D7789" font-family="system-ui">sabit ödeme planı yoktur. Faiz</text>
  <text x="370" y="188" font-size="10.5" fill="#5D7789" font-family="system-ui">kullanılan tutar ve gün üzerinden.</text>
  <text x="330" y="222" text-anchor="middle" font-size="11" fill="#B23B3B" font-family="system-ui">Teminatlı ürün = ek adımlar: değerleme · teminat kaydı · rehin/ipotek tesisi · kapanışta fek</text>
</svg>
```

### Nasıl çalışır? — Başvurudan takibe yaşam döngüsü

Her adım sistemde **bir kayıt** bırakır; bankada hiçbir adım "izsiz" geçmez.

1. **Başvuru.** Talep bir kanaldan alınır: şube, internet ya da mobil. **Benzersiz bir başvuru numarası** üretilir. Bu numara sonraki bütün adımların ortak referansıdır. Müşteri talebi tekrar gönderirse aynı başvurunun iki kez açılmaması gerekir (tekrar güvenliği — `130`).
2. **Ön değerlendirme.** Kimlik doğrulama, KVKK aydınlatma ve gerekli onaylar, iç kontrol listeleri, ürün uygunluk kuralları çalışır. Bu adım genelde hızlıdır ve erken **ret** üretebilir. Ret de bir kayıttır; silinmez.
3. **Skorlama / kredi notu.** Dış kaynaktan bir **kredi notu** sorgusu yapılır; örneğin KKB'nin sunduğu Findeks gibi servisler. Bu sorgu, bankanın **iç skor kartıyla** birlikte bir risk sınıfı üretir. Burası bir **dış servis çağrısıdır**. Dolayısıyla zaman aşımı, devre kesici ve yeniden deneme konuları buradadır. Sorgunun iki kez faturalanmaması ve iki kez işlenmemesi de burada çözülür (`101`, `130`). Sorgunun kendisi de kayıt bırakır.
4. **Tahsis / limit tesisi.** Risk sınıfı ile gelir ve teminat bilgisine göre **onaylanacak limit** belirlenir. Koşullar da belirlenir: vade, oran, teminat. Otomatik onay eşiğinin üstündeki tutarlar **tahsis birimindeki bir insana** düşer. Sonuç, bir müşteri/ürün limiti kaydıdır.
5. **Sözleşme ve onay.** Sözleşme metni üretilir ve müşteri onayı alınır; onay ıslak imza ya da dijital olabilir. Onayın kim tarafından ve ne zaman verildiği saklanır. Sözleşme olmadan para çıkmaz.
6. **Kullandırım (ödeme).** Para müşterinin hesabına aktarılır. **Muhasebe fişi** oluşur ve limit düşer. Bu adım **geri alınamazdır**. Yanlışlık varsa kayıt silinmez; **ters kayıt (storno/düzeltme)** ile düzeltilir. Her iki kayıt da tarihte durur.
7. **Ödeme planı ve tahakkuk.** Taksitli üründe **ödeme planı** satırları üretilir. Satır alanları şunlardır: taksit no, vade tarihi, taksit tutarı, faiz, anapara, vergiler, kalan anapara. Dönemsel olarak **tahakkuk** işler; yani henüz tahsil edilmemiş faiz ve gelir muhasebeleştirilir. Bu genellikle **gün sonu (EOD)** toplu işlerinin işidir (`222`, `223`).
8. **Tahsilat.** Vade gününde müşteri hesabından otomatik tahsilat denenir ya da müşteri elle öder. **Kısmi tahsilat** mümkündür. Tahsil edilen tutarın hangi kaleme, hangi sırayla mahsup edileceği bir **iş kuralıdır**. Kalemler şunlardır: gecikme faizi, faiz, anapara, masraf. Bu sıra iş biriminden yazılı alınır; geliştirici varsayamaz.
9. **Gecikme ve takip.** Vadesi geçen taksit **gecikmiş** duruma geçer. Gecikme gün sayacı işler ve **gecikme faizi** tahakkuk eder. Eşikler aşıldıkça önce **idari takip** devreye girer: hatırlatma ve arama. Sonra **yasal takip** süreci başlar. Her aşama ayrı kayıt ve ayrı muhasebe etkisi üretir.
10. **Yeniden yapılandırma, erken kapama ve kapanış.** Yapılandırma, eski planı kapatıp **yeni bir plan** üretir. Eski plan silinmez; kapanmış olarak durur. Erken kapamada borç bugüne göre hesaplanır, kredi kapanır ve teminat varsa **fek** adımı işletilir.

```svg
<svg viewBox="0 0 660 245" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kredi yaşam döngüsünün on adımı ve her adımın bıraktığı kayıt">
  <defs>
    <marker id="kredi-dongu-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yaşam döngüsü ve her adımın bıraktığı kayıt</text>
  <rect x="24" y="42" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="80" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">1 · Başvuru</text>
  <text x="80" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">başvuru no</text>
  <path d="M138 68 L147 68" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="149" y="42" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="205" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 · Ön değerlend.</text>
  <text x="205" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kontrol kaydı</text>
  <path d="M263 68 L272 68" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="274" y="42" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="330" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">3 · Skorlama</text>
  <text x="330" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sorgu kaydı</text>
  <path d="M388 68 L397 68" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="399" y="42" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="455" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">4 · Tahsis/limit</text>
  <text x="455" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">limit kaydı</text>
  <path d="M513 68 L522 68" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="524" y="42" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="580" y="64" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">5 · Sözleşme</text>
  <text x="580" y="82" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">onay kaydı</text>
  <path d="M580 96 L580 116 L80 116 L80 138" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="24" y="140" width="112" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="80" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">6 · Kullandırım</text>
  <text x="80" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">muhasebe fişi</text>
  <path d="M138 166 L147 166" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="149" y="140" width="112" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="205" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">7 · Plan/tahakkuk</text>
  <text x="205" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">plan satırları</text>
  <path d="M263 166 L272 166" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="274" y="140" width="112" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="330" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">8 · Tahsilat</text>
  <text x="330" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">tahsilat hareketi</text>
  <path d="M388 166 L397 166" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="399" y="140" width="112" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.4"/>
  <text x="455" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#B23B3B" font-family="system-ui">9 · Gecikme/takip</text>
  <text x="455" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">takip kaydı</text>
  <path d="M513 166 L522 166" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#kredi-dongu-arw)"/>
  <rect x="524" y="140" width="112" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="580" y="162" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">10 · Kapanış</text>
  <text x="580" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kapanış + fek</text>
  <text x="330" y="220" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Hiçbir adım silinmez: düzeltme ters kayıtla yapılır, yapılandırma eski planı kapatıp yenisini üretir.</text>
</svg>
```

### Temel finansal kavramlar (geliştirici gözüyle)

- **Anapara (principal):** kullandırılan asıl tutardır. Ödendikçe azalan kısmına **kalan anapara (bakiye)** denir. Erken kapama ve gecikme hesapları buna dayanır.
- **Faiz oranı:** aylık mı yıllık mı olduğu **her zaman açıkça yazılmalıdır**. Aylık oranı yıllık sanan bir kod, taksit tutarını on iki kat yanlış üretir. Hangi alanın hangi periyodu taşıdığını iş biriminden yazılı teyit et. Periyodu alan adının içine yaz.
- **Ödeme planı (taksit planı):** kredinin sözleşmesidir. Her satır tipik olarak şunları taşır: taksit numarası, **vade tarihi**, toplam taksit tutarı, içindeki **faiz**, içindeki **anapara**, vergi/fon kalemleri ve o taksit sonrası **kalan anapara**.
- **Eşit taksitli (anüite) mantığı:** taksit tutarı sabittir ama **içindeki ayrışma değişir**. Başlarda taksitin büyük kısmı faizdir; sonlara doğru anapara ağır basar. Bu yüzden "kredinin yarısını ödedim, borcumun yarısı bitti" doğru değildir; kalan anapara yarıdan fazladır. Kredi ekranında gösterilen "kalan borç" ile "kalan taksitler toplamı" farklı şeylerdir.
- **BSMV ve KKDF:** **BSMV** Banka ve Sigorta Muameleleri Vergisi'dir; **KKDF** ise Kaynak Kullanımını Destekleme Fonu'dur. İkisi de faiz üzerinden hesaplanır ve uygulandıkları yerde taksite eklenir. Oranları, hangi ürüne uygulandığı ve istisnaları mevzuatla belirlenir ve zaman içinde değişir. Bu yüzden koda gömülmez. **Parametre tablosundan tarih geçerliliğiyle** okunur ve kural, iş biriminden ya da mevzuattan **yazılı** teyit edilir. Mülakatta oran söyleme; "parametreden okunur, yazılı teyit ederim" demek doğru duruştur.
- **Dosya masrafı / tahsis ücreti:** ürün ve mevzuata bağlı olarak alınabilen bir kalemdir. Kullandırım anında ya da taksitlere yayılarak yansıtılabilir.
- **Maliyet oranı:** müşterinin gerçekte katlandığı toplam yükü tek bir orana indirger. Bu yük faiz + vergi/fon + masraftan oluşur. Dolayısıyla maliyet oranı **faiz oranından yüksektir**. Ekranda faiz oranıyla maliyet oranını karıştırmak klasik bir hatadır.
- **Gecikme faizi:** vadesinde ödenmeyen tutara uygulanan ek yüktür. Sözleşme ve mevzuatın izin verdiği çerçevede uygulanır. Normal (akdi) faizden ayrı bir kalemdir ve ayrı muhasebeleşir.
- **Erken kapama:** borç **bugüne göre** hesaplanır. Formül şudur: kalan anapara + o güne kadar işlemiş faiz + bağlı vergi/fon kalemleri. Mevzuata bağlı bir erken ödeme bedeli varsa o da eklenir. Bu tutar "kalan taksitlerin toplamı" **değildir**; çünkü o toplam, henüz yaşanmamış dönemlerin faizini de içerir. Hesap güne bağlı olduğu için **valör** ile **iş günü/tatil takvimi** sonucu doğrudan etkiler.
- **Yuvarlama — sistemin asıl sorunu.** Formülü ezberlemek yerine şunu bil: sistem, anaparayı ve faizi taksitlere dağıtırken **iki ondalığa (kuruşa) yuvarlamak zorundadır**. Yuvarlanmış taksitlerin toplamı da neredeyse hiçbir zaman anaparayı tam tutmaz.

  Doğru desen üç parçalıdır. (1) Tüm hesap `BigDecimal` ile yapılır; `double` asla kullanılmaz (`226`). (2) Ölçek (scale) ve yuvarlama modu iş kuralına bağlanır ve her satır tek tek hesaplanır. (3) Ortaya çıkan **kuruş farkı bilinçli bir yere yazılır**; yaygın uygulama son taksite yazmaktır.

  Üstüne bir **denetim** konur: plan satırlarındaki anapara kalemlerinin toplamı, kullandırılan anaparaya **kuruşu kuruşuna** eşit olmalıdır. Bu eşitlik tutmazsa plan hatalıdır. Ayrıca `BigDecimal` karşılaştırmasında `equals` yerine `compareTo` kullanılır; çünkü `equals` ölçeği de karşılaştırır.

### Limit kavramı ve eşzamanlılık

- **Müşteri limiti:** müşterinin bankaya karşı taşıyabileceği toplam risk tavanıdır. **Ürün limiti:** o tavanın belirli bir ürüne ayrılmış kısmıdır.
- **Kullanılabilir limit = tahsis edilen limit − kullandırılan − bloke.** **Bloke (rezerve)** kısmı, onaylanmış ama henüz kullandırılmamış tutardır; bekleyen bir işlem için ayrılmış da olabilir. Hesaba katılmazsa aynı limit iki kez taahhüt edilir.
- Limit düşümü klasik bir **kontrol et → sonra yaz** desenidir ve bu desen eşzamanlılıkta bozulur. İki başvuru aynı anda kalan limiti okur. İkisi de "yeter" der, ikisi de düşer ve **limit aşılır**. İzolasyon seviyesini yükseltmek tek başına yeterli değildir (`224`). Doğru çözüm, satır üzerinde bir kilit kullanmaktır: **iyimser kilit** (`@Version` ile sürüm damgası) ya da **kötümser kilit** (`SELECT ... FOR UPDATE`) (`225`). Yoğun çakışmanın olduğu sıcak satırlarda kötümser kilit tercih edilir. Seyrek çakışmada ise iyimser kilit ve yeniden deneme yeterlidir.
- Limit düşümü ile kullandırım **aynı işlem sınırında** olmalıdır. Ayrı işlemlerdeyse **giden kutusu tablosu ve tekrar güvenliği** ile korunmalıdır (`130`, `116`). Aksi hâlde tutarsız durumlar oluşur: para çıkar ama limit düşmez, ya da tersi olur.

```svg
<svg viewBox="0 0 660 210" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Kullanılabilir limit formülü ve eşzamanlı düşümdeki yarış koşulu">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Kullanılabilir limit ve eşzamanlı düşüm riski</text>
  <rect x="20" y="50" width="148" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="94" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Tahsis edilen limit</text>
  <text x="94" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">onaylanan tavan</text>
  <text x="178" y="82" text-anchor="middle" font-size="18" font-weight="700" fill="#5D7789" font-family="system-ui">−</text>
  <rect x="190" y="50" width="124" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="252" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Kullandırılan</text>
  <text x="252" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">para çıkmış</text>
  <text x="324" y="82" text-anchor="middle" font-size="18" font-weight="700" fill="#5D7789" font-family="system-ui">−</text>
  <rect x="336" y="50" width="124" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="398" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#B07D20" font-family="system-ui">Bloke/rezerve</text>
  <text x="398" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">taahhüt edilmiş</text>
  <text x="470" y="82" text-anchor="middle" font-size="18" font-weight="700" fill="#5D7789" font-family="system-ui">=</text>
  <rect x="482" y="50" width="158" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="561" y="72" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">Kullanılabilir limit</text>
  <text x="561" y="90" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yeni işleme açık</text>
  <rect x="24" y="122" width="612" height="66" rx="12" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="144" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Yarış koşulu: kontrol et → sonra yaz</text>
  <text x="330" y="162" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">İki işlem aynı anda okur → ikisi de "limit yeter" der → ikisi de düşer → limit aşılır</text>
  <text x="330" y="180" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Çözüm: iyimser kilit (@Version) ya da satır kilidi (SELECT ... FOR UPDATE) — konu 225</text>
</svg>
```

### Aktörler, roller ve dört göz ilkesi

- **Müşteri:** başvurunun sahibidir. **Şube/çağrı merkezi personeli:** başvuruyu sisteme girer ve belge toplar.
- **Tahsis birimi:** otomatik onay eşiğini aşan taleplerde limiti ve koşulları belirler. Kredinin "evet"ini veren birimdir.
- **Operasyon:** kullandırım, teminat tesisi ve belge kontrolü gibi işleyiş adımlarını yürütür. **Muhasebe:** kayıtların doğru hesaplara düşmesinden sorumludur. **Risk:** portföyün risk ölçümünü, skor kartını ve limit politikasını yönetir. **İç denetim:** her şeyin izini geriye doğru sürebilen taraftır.
- **Görevler ayrılığı (segregation of duties):** aynı kişi bir işlemin hem tamamını yapan hem de onaylayan olamaz. **Dört göz ilkesi (maker-checker)** bunun somut hâlidir: **kaydı oluşturan (maker)** ile **onaylayan (checker)** farklı kullanıcılar olmalıdır.
- **Sistem tarafındaki karşılığı** şudur. Kayıt önce "onay bekliyor" durumunda tutulur. Onay adımı, kaydı oluşturan kullanıcı tarafından yapılamaz; sistem bunu **reddeder**. Yetki rol tabanlıdır. Onay kaydı, kim ve ne zaman onayladı ve hangi kanaldan geldi bilgisiyle saklanır.

  Geliştirici için bir tuzak vardır. Test ortamını kolaylaştırmak için "kendi kaydını onaylayabilir" kısayolu konur. Bu kısayol yapılandırma yoluyla üretime sızarsa denetimde doğrudan bulgu olur.

### Bankada geliştiriciyi bağlayan gerçekler

- **Denetim izi (audit trail):** kim, ne zaman, neyi, hangi eski ve yeni değerle değiştirdi. Kredi tarafında bu isteğe bağlı değildir.
- **Geri alınamazlık ve ters kayıt:** para hareketi silinmez. `UPDATE` ile geçmişe dönülüp düzeltilmez. Hata varsa **ters kayıt (storno/düzeltme fişi)** atılır. Hem yanlış kayıt hem de düzeltmesi tarihte kalır.
- **Muhasebe fişi denkliği:** her para hareketi çift taraflıdır. **Borç toplamı = alacak toplamı** olmalıdır. Fiş denk değilse işlem geçersizdir.
- **Gün sonu (EOD) penceresi:** tahakkuk, gecikme güncelleme, mutabakat ve raporlama toplu işleri belirli bir zaman penceresinde biter. Pencere daralınca üç konu devreye girer: yeniden başlatılabilirlik, kaldığı yerden devam etme ve ölçekleme (`222`, `223`).
- **KVKK ve veri maskeleme:** kimlik numarası ve hesap/kart numarası gibi veriler loglara maskesiz yazılmaz. Üretim verisi de test ortamına maskelenmeden kopyalanmaz.
- **Üretim verisine erişim:** geliştiricinin üretim veritabanında serbest sorgu ya da değişiklik yapması beklenmez. Erişim kontrollüdür ve izlenir; değişiklikler değişiklik yönetimi sürecinden geçer.
- **Mevzuat kuralları koda gömülmez.** Oran, eşik, tarih ve istisnalar değişir. Bunlar parametre olarak ve **tarih geçerliliğiyle** tutulur. Mülakatta kesin oran ya da madde söyleme. Doğru ve güçlü cevap şudur: "Kuralı mevzuattan ve iş biriminden **yazılı** teyit ederim; parametre olarak tarih bazlı saklarım."

### Sözlük

- **Cari (hesap):** müşterinin hareketlerinin işlendiği hesaptır. **Ekstre:** belirli bir döneme ait hareket ve bakiye dökümüdür.
- **Mutabakat (reconciliation):** iki ayrı kaydın birbirini tutup tutmadığının karşılaştırılmasıdır. Örneğin banka içi kayıt ile karşı kurumdan gelen dosya karşılaştırılır. Tutmayanlar **fark listesine** düşer.
- **Valör:** bir işlemin faiz ve bakiye hesaplarında dikkate alındığı tarihtir. İşlem tarihiyle aynı olmayabilir.
- **Tahakkuk:** henüz tahsil edilmemiş gelir ya da giderin ilgili döneme kaydedilmesidir. **Storno:** bir kaydın ters kayıtla iptalidir.
- **Referans numarası:** bir işlemi uçtan uca izlemeye yarayan benzersiz numaradır. Tekrar güvenliği ile destek ve hata takibi bunun üzerinden yürür.
- **İş günü / tatil takvimi:** vade, valör ve tahsilat hesapları tatil günlerine göre kayar. Takvim bir **veridir**; koda gömülmez.
- **Rehin / ipotek / fek:** teminatın tesisi ve çözülmesidir. Rehin taşınır malda, ipotek taşınmazda kurulur. Borç bitince teminatın çözülmesine fek denir.

### Mülakatta ne sorulur / tuzaklar

- **"Kredi ekranı yaptın mı?"** Doğru cevap, deneyimi abartmak değil **alan bilgisini göstermektir**. Yaşam döngüsünü doğru sırayla sayabilmek ve her adımın sistemde ne bıraktığını söyleyebilmek çoğu zaman yeterlidir (kalıp için `238`).
- **Para hesabını `double` ile yapmak.** Tek başına eleyici bir hatadır. Beklenen şudur: `BigDecimal`, bilinçli ölçek ve bilinçli yuvarlama modu (`226`).
- **Limit düşümünü korumasız bırakmak.** "Önce kontrol ettim, sonra güncelledim" cevabı, yarış koşulunu görmemek demektir (`225`).
- **Erken kapamayı "kalan taksitlerin toplamı" sanmak.** Bu, yaşanmamış dönemlerin faizini de tahsil etmek olur. Doğru temel **kalan anaparadır**.
- **Kayıt silmek veya geçmişi güncellemek.** Bankada düzeltme silme değil **ters kayıttır**. Bunu bilmeyen tasarım denetimde geçmez.
- **Kesin oran, madde ya da tarih iddiası.** Mevzuat değişir. Sayı ezberlemek yerine "parametre + yazılı teyit" duruşunu savun.
- **Alan dilini bilmemek.** "Tahakkuk", "valör", "fek", "mutabakat" ve "bloke" kelimelerine boş bakmak, iş birimiyle çalışamayacağın izlenimi bırakır.

### Tek cümlede

Kredi sistemi, taksitli ve rotatif ürünleri şu zincirle yönetir: başvuru → skorlama → tahsis → sözleşme → kullandırım → ödeme planı/tahakkuk → tahsilat → gecikme/takip → kapanış. Geliştirici için asıl konu formül değildir. Asıl konu beş şeydir: kuruş hassasiyeti, limitin eşzamanlı düşümü, dört göz onayı, silinmeyen ve ters kayıtla düzeltilen denetim izi ve mevzuat kurallarının parametreden okunması.

# kredi-1 | Orta
## Soru
Taksitli bir bireysel kredide, başvurudan tahsilata kadar olan adımların doğru sırası aşağıdakilerden hangisidir?
## A
Başvuru → tahsis (limit tesisi) → skorlama → sözleşme ve onay → kullandırım → ödeme planı ve tahakkuk → tahsilat → gecikme/takip
🤖 limit, risk değerlendirmesinin sonucudur; skorlama tahsisten sonra yapılamaz.
## B
Başvuru → ön değerlendirme ve skorlama → tahsis (limit tesisi) → sözleşme ve onay → kullandırım → ödeme planı ve tahakkuk → tahsilat → gecikme/takip
🤖 Doğru: risk önce ölçülür, limit ona göre tesis edilir, sözleşme imzalanmadan para çıkmaz, plan kullandırımla üretilir.
## C
Başvuru → kullandırım → skorlama → tahsis (limit tesisi) → sözleşme ve onay → ödeme planı ve tahakkuk → tahsilat → gecikme/takip
🤖 para, risk değerlendirmesi ve imzalı sözleşme öncesinde çıkamaz; kullandırım geri alınamaz bir adımdır.
## D
Başvuru → sözleşme ve onay → skorlama → kullandırım → tahsis (limit tesisi) → ödeme planı ve tahakkuk → tahsilat → gecikme/takip
🤖 sözleşme koşulları (limit, vade, oran) tahsis kararına dayanır; tahsis en sona bırakılamaz.
## Doğru
B
## Açıklama
Zincirin mantığı "önce ölç, sonra karar ver, sonra taahhüt et, en son parayı çıkar" biçimindedir. **Başvuru** benzersiz bir numara ile açılır; **ön değerlendirme** kimlik/uygunluk kontrollerini yapar; **skorlama** dış kredi notu ile iç skor kartını birleştirip risk sınıfını üretir. Bu çıktı olmadan **tahsis** yapılamaz, çünkü limit ve koşullar (vade, oran, teminat) doğrudan risk değerlendirmesinin sonucudur — bu yüzden tahsisi skorlamanın önüne koyan sıralama yanlıştır. **Sözleşme ve onay** alınmadan **kullandırım** yapılamaz; kullandırım muhasebe fişi üreten ve **geri alınamayan** adımdır, dolayısıyla parayı skorlama veya sözleşme öncesine koyan sıralama da yanlıştır. Sözleşmeyi tahsisin önüne alan sıralama ise imkânsızdır: imzalanacak koşullar henüz belirlenmemiştir. Kullandırımdan sonra **ödeme planı** üretilir, dönemsel **tahakkuk** işler (genelde gün sonu toplu işiyle — `222`, `223`), vade gününde **tahsilat** denenir; ödenmeyen taksit **gecikme ve takip** koluna geçer.

# kredi-2 | İleri
## Soru
Bir müşterinin kullanılabilir limiti nasıl hesaplanır ve aynı anda gelen iki kullandırım talebinde bu hesap nasıl korunur?
## A
Tahsis edilen limitten yalnız kullandırılan tutar düşülür; bloke tutar para çıkışı olmadığı için hesaba katılmaz, eşzamanlılık içinse işlem öncesi kontrol yeterlidir
🤖 bloke, taahhüt edilmiş tutardır; sayılmazsa aynı limit iki kez taahhüt edilir ve kontrol-sonra-yaz yarışı sürer.
## B
Tahsis edilen limit doğrudan kullanılabilir limit sayılır; düşümler gün sonu toplu işinde toplu olarak işlenir ve gün içinde hesap hiç değişmez
🤖 gün içinde düşülmeyen limit aynı gün defalarca kullanılabilir; toplu işe bırakmak limit aşımını kesinleştirir.
## C
Kullanılabilir limit, kullandırılan ile blokenin toplamıdır; eşzamanlılık sorunu ise izolasyon seviyesini READ COMMITTED yapmakla ortadan kalkar
🤖 formül ters kurulmuş; ayrıca izolasyon seviyesi tek başına kontrol-sonra-yaz yarışını engellemez.
## D
Kullanılabilir limit = tahsis − kullandırılan − bloke; eşzamanlı düşümde kontrol-sonra-yaz yarışı doğduğu için satır üzerinde iyimser (@Version) ya da kötümser kilit gerekir
🤖 Doğru: bloke taahhüdü de düşülür ve düşüm satır düzeyinde kilitle/sürümle korunur.
## Doğru
D
## Açıklama
**Kullanılabilir limit = tahsis edilen limit − kullandırılan − bloke (rezerve)** biçiminde hesaplanır. **Bloke**, onaylanmış ama henüz kullandırılmamış ya da bekleyen bir işlem için ayrılmış tutardır; "para çıkmadı" diye hesaba katılmazsa aynı limit iki farklı işleme taahhüt edilir. Formülü ters kuran (kullandırılan + bloke) seçenek zaten kullanılabilir tutarı değil kullanılmış tutarı verir. Düşümü gün sonu toplu işine bırakmak, gün içinde limitin defalarca kullanılmasına kapı açar; limit gerçek zamanlı düşmelidir. Eşzamanlılık tarafında sorun klasiktir: iki işlem kalan limiti **okur**, ikisi de "yeter" sonucuna varır, ikisi de düşer ve **limit aşılır** — bu, kontrol et → sonra yaz yarışıdır. İzolasyon seviyesini yükseltmek bunu tek başına çözmez (`224`); gereken şey satır düzeyinde koruma: seyrek çakışmada **iyimser kilit** (`@Version` sürüm damgası + yeniden deneme), sıcak satırda yoğun çakışmada **kötümser kilit** (`SELECT ... FOR UPDATE`) — ayrıntısı `225`. Ayrıca limit düşümü ile kullandırımın aynı işlem sınırında olması, ayrıysa outbox/idempotency ile korunması gerekir (`130`).

# kredi-3 | İleri
## Soru
Müşteri taksitli kredisini vadesinden önce tamamen kapatmak istiyor. Sistemin hesaplaması gereken kapama tutarı nedir?
## A
Kalan anapara + o güne kadar işlemiş faiz + bağlı vergi/fon kalemleri (+ varsa mevzuat ve sözleşmeye bağlı erken ödeme bedeli)
🤖 Doğru: borç bugüne göre hesaplanır; henüz yaşanmamış dönemlerin faizi tahsil edilmez.
## B
Kalan ödenmemiş taksitlerin tutar toplamı; plan zaten kesin olduğu için bu toplam doğrudan kapama tutarıdır
🤖 bu toplam gelecek dönemlerin faizini de içerir; müşteriden yaşamadığı dönemin faizi tahsil edilmiş olur.
## C
İlk taksitteki tutarın kalan taksit sayısıyla çarpımı; eşit taksitli planda taksit tutarı sabit olduğu için sonuç aynıdır
🤖 taksit sabit olsa da toplamı gelecek faizi içerir ve bu yöntem de kalan anaparayı değil plan toplamını verir.
## D
Toplam kredi tutarından şimdiye kadar ödenen taksitlerin toplamının çıkarılmasıyla bulunan kalan tutar
🤖 ödenen taksitlerin bir kısmı faiz ve vergidir; bu fark kalan anaparayı değil karışık bir tutarı verir.
## Doğru
A
## Açıklama
Erken kapamanın temeli **kalan anaparadır**, plan satırlarının toplamı değil. Eşit taksitli (anüite) planda taksit tutarı sabittir ama **içindeki faiz-anapara ayrışması** değişir: başlarda faiz ağır basar, sonlara doğru anapara. Bu yüzden "kalan taksitlerin toplamı" ya da "ilk taksit × kalan taksit sayısı" hesabı, henüz **yaşanmamış dönemlerin faizini** de içerir ve müşteriden fazla tahsilat anlamına gelir. "Toplam kredi tutarı − ödenenler" yöntemi de yanlıştır, çünkü ödenen taksitlerin bir bölümü faiz ve vergi kalemidir; aradaki fark kalan anaparaya karşılık gelmez. Doğru hesap **bugüne göre** yapılır: kalan anapara, son tahsilattan bugüne kadar **işlemiş faiz**, bunlara bağlı vergi/fon kalemleri ve — varsa — mevzuat ile sözleşmenin izin verdiği erken ödeme bedeli. Hesap güne bağlı olduğu için **valör** ve **iş günü/tatil takvimi** sonucu değiştirir; gün farkı doğrudan faize yansır. Oranlar ve erken ödeme bedelinin uygulanabilirliği mevzuata bağlı olduğu ve değiştiği için koda gömülmez, **tarih geçerlilikli parametreden** okunur ve iş biriminden yazılı teyit alınır.

# kredi-4 | İleri
## Soru
Eşit taksitli bir planda taksit tutarları kuruşa yuvarlanınca taksitlerdeki anapara kalemlerinin toplamı, kullandırılan anaparayı birkaç kuruş tutmuyor. Doğru yaklaşım hangisidir?
## A
Hesabı `double` ile yapıp yuvarlamayı yalnız en sonda, ekrana yazarken uygulamak; ara adımlarda hassasiyet kaybı yaşanmaz
🤖 `double` ikili kayan noktadır ve ondalık para tutarlarını tam temsil edemez; hata ara adımlarda birikir.
## B
Taksit tutarını her zaman yukarı yuvarlayıp oluşan fazlalığı bir düzeltme kaydı olmadan bırakmak; fark küçük olduğu için önemsizdir
🤖 kuruş farkı mutabakatta ve muhasebe denkliğinde bulgu üretir; "küçük fark" bankada kabul edilmez.
## C
Tüm hesabı `BigDecimal` ile yapmak, ölçek ve yuvarlama modunu iş kuralına bağlamak ve kalan kuruş farkını bilinçli biçimde son taksite yazmak
🤖 Doğru: kuruş farkı kaybolmaz, bilinçli bir satıra yazılır ve plan bir eşitlik denetimiyle doğrulanır.
## D
Taksitleri kuruş yerine daha yüksek ölçekte (ör. altı ondalık) saklayıp ekranda yuvarlamak; böylece kayıttaki toplam her zaman tutar
🤖 tahsilat gerçek para biriminde yapılır; kayıt ile tahsil edilen tutar ayrışır ve mutabakat bozulur.
## Doğru
C
## Açıklama
Ödeme planı üretmenin kaçınılmaz gerçeği şudur: anapara ve faiz taksitlere bölünürken **kuruşa yuvarlamak zorunludur** ve yuvarlanmış satırların toplamı kullandırılan anaparayı çoğu zaman tam tutmaz. Doğru desen üç parçalıdır. (1) Hesap **`BigDecimal`** ile yapılır; `double` ikili kayan nokta olduğu için ondalık para tutarlarını tam temsil edemez ve hata ara adımlarda birikir — "sonda yuvarlarım" bu sorunu çözmez (`226`). (2) **Ölçek (scale) ve yuvarlama modu** keyfi değil, iş kuralına bağlı seçilir ve satırlar tek tek üretilir. (3) Ortaya çıkan **kuruş farkı yok sayılmaz**, bilinçli bir yere yazılır; yaygın uygulama farkı **son taksite** yansıtmaktır. Üstüne bir doğrulama konur: plan satırlarındaki anapara kalemlerinin toplamı, kullandırılan anaparaya **kuruşu kuruşuna** eşit olmalıdır. Farkı görmezden gelmek bankada kabul edilmez; kuruş farkları mutabakatta ve muhasebe denkliğinde bulgu üretir. Tutarları daha yüksek ölçekte saklayıp yalnız ekranda yuvarlamak da çözüm değildir: tahsilat gerçek para biriminde yapılır, kayıttaki tutar ile tahsil edilen tutar ayrışır ve mutabakat bozulur. Not: `BigDecimal` karşılaştırmasında `equals` ölçeği de karşılaştırdığı için `compareTo` kullanılır.

# kredi-5 | Temel
## Soru
Kredi tahsis ve kullandırım süreçlerinde uygulanan **dört göz ilkesi (maker-checker)** sistem tarafında ne anlama gelir?
## A
Aynı yetkili kullanıcının kaydı iki ayrı adımda iki kez onaylaması; ikinci onay birincisini doğruladığı için hata riski azalır
🤖 aynı kişinin iki kez onaylaması görevler ayrılığını sağlamaz, yalnız adım sayısını artırır.
## B
Kaydı oluşturan (maker) ile onaylayan (checker) farklı kullanıcılar olmalıdır; kayıt onay bekleyen durumda tutulur ve onay izlenebilir biçimde saklanır
🤖 Doğru: görevler ayrılığının somut karşılığı, farklı kullanıcı zorunluluğu ve izlenebilir onay kaydıdır.
## C
İki ayrı sistemin ürettiği kayıtların karşılaştırılıp farkların listelenmesi; tutarsızlık varsa işlem durdurulur
🤖 bu mutabakattır (reconciliation); onay yetkisiyle değil veri karşılaştırmasıyla ilgilidir.
## D
Kod değişikliğinin üretime çıkmadan önce en az iki geliştirici tarafından incelenip onaylanmış olması zorunluluğu
🤖 bu kod inceleme kuralıdır; iş kaydının onaylanmasıyla ilgili bir kontrol değildir.
## Doğru
B
## Açıklama
**Görevler ayrılığı (segregation of duties)** bankacılığın temel kontrolüdür: aynı kişi bir işlemi hem baştan sona yapan hem onaylayan olamaz. Bunun somut hâli **dört göz ilkesidir (maker-checker)**: kaydı oluşturan kullanıcı **maker**, onaylayan **checker**'dır ve **farklı kişiler olmak zorundadır**. Sistem tarafındaki karşılığı nettir: kayıt önce "onay bekliyor" durumunda tutulur, onay uç noktası kaydı oluşturan kullanıcıyla aynı kullanıcı için **reddeder**, yetkilendirme rol tabanlıdır ve onayın kim tarafından, ne zaman, hangi kanaldan verildiği **denetim izine** yazılır. Aynı kullanıcının iki kez onaylaması bu kontrolü sağlamaz — kontrolün amacı adım sayısı değil, **farklı gözdür**. İki sistemin kayıtlarını karşılaştırıp fark listelemek **mutabakattır**; benzer amaca (hata yakalama) hizmet eder ama onay yetkisiyle ilgisi yoktur. İki geliştiricinin kod incelemesi ise yazılım süreci kuralıdır, iş kaydının onay akışı değildir. Geliştirici için klasik tuzak, test kolaylığı için konan "kendi kaydını onaylayabilir" bayrağının yapılandırmayla üretime sızmasıdır; bu, denetimde doğrudan bulgu üretir.

# kredi-6 | Orta
## Soru
Vadesi gelen bir taksit ödenmedi. Sistemde bundan sonra ne olması beklenir?
## A
Ödenmeyen taksit satırı plandan silinir ve kalan taksitler yeniden hesaplanarak müşteriye yeni bir ödeme planı üretilir
🤖 bankada kayıt silinmez; plan ancak yeniden yapılandırma kararıyla, eski plan kapatılarak değiştirilir.
## B
Kredi otomatik olarak kapatılır ve müşterinin borç bakiyesi sıfırlanarak alacak doğrudan zarar yazılır
🤖 tek bir gecikme krediyi kapatmaz; kapanış ve zarar kaydı ayrı kararlara ve eşiklere bağlıdır.
## C
Taksit "gecikmiş" duruma geçer, gecikme gün sayacı işler ve gecikme faizi tahakkuk eder; eşikler aşıldıkça önce idari sonra yasal takip devreye girer
🤖 Doğru: gecikme bir durum geçişidir; sayaç, ek tahakkuk ve kademeli takip adımlarını izlenebilir biçimde tetikler.
## D
Gecikme yalnız raporlama amacıyla işaretlenir; müşterinin borç bakiyesine ve muhasebe kayıtlarına bir etkisi olmaz
🤖 gecikme faizi tahakkuk ettiği ve borcu artırdığı için muhasebe etkisi vardır; salt raporlama etiketi değildir.
## Doğru
C
## Açıklama
Gecikme, plan üzerinde bir **durum geçişidir**: vadesi geçen taksit "gecikmiş" duruma alınır, **gecikme gün sayacı** işlemeye başlar ve sözleşme ile mevzuatın izin verdiği çerçevede **gecikme faizi** tahakkuk eder — yani borcu fiilen artırır ve muhasebeye yansır; bu yüzden gecikmeyi yalnız bir raporlama etiketi sayan yaklaşım yanlıştır. Gecikme günü belirli eşikleri aştıkça süreç kademelenir: önce **idari takip** (hatırlatma, arama, ödeme planı görüşmesi), gerekirse **yasal takip** aşamasına aktarım. Her aşama ayrı kayıt, ayrı bildirim ve ayrı muhasebe etkisi üretir; hepsi **denetim izine** yazılır. Ödenmeyen taksit satırının silinip planın yeniden hesaplanması bankacılık mantığına aykırıdır: kayıt silinmez, plan ancak ayrı bir **yeniden yapılandırma** kararıyla değiştirilir ve o durumda bile eski plan kapatılmış olarak saklanır. Tek bir gecikmenin krediyi otomatik kapatıp bakiyeyi sıfırlaması da yanlıştır; kapanış, zarar yazma ve karşılık ayırma kararları ayrı eşiklere ve süreçlere bağlıdır. Gecikme durumlarının güncellenmesi tipik olarak **gün sonu toplu işlerinin** görevidir (`222`, `223`) ve iş günü/tatil takvimi hesaba katılır.
