---
slug: sozlu-prova-yazilim-denetimi
baslik: "Sözlü Prova · Yazılım Denetimi (BT Audit): Risk, Değişiklik Kontrolü, Kod İncelemesi ve Kanıt"
kademe: İleri
sira: 270
etiketler: 11
---

## Konu anlatımı

Önce bir çerçeve: bu altı soru bir BT denetçisi (IT auditor) rolüne aittir, geliştirici rolüne değil. Denetçi kod yazmaz; bir kontrolün tasarlandığını ve gerçekten çalıştığını kanıtla gösterir. Sen bu soruları alırsan amacın denetçi gibi cevap vermek değildir. Amacın, denetlenen taraf olarak kontrolleri anlamak ve kendi işini o dille anlatabilmektir. Denetçi "kanıt nerede" diye sorar; geliştirici "hat yeşildi" der. Bu iki cümle arasındaki farkı görebilen aday, denetim deneyimi olmasa bile ciddiye alınır. Denetim geçmişin yoksa uydurma; `256`'daki dürüst köprüyü kur ve denetlenen tarafta ne yaşadığını anlat.

Dosyanın kalıbı diğerleriyle aynıdır. Her sorunun altında dört parça var: soru neyi ölçüyor, sözlü cevabın iskeleti, elenme sebepleri, kendi hikâyenin yeri. Son parça bilerek boş bırakıldı. Senin adına deneyim uydurulmaz; onun yerine hikâyeyi çıkaracak sorular sorulur.

### "Yazılım denetiminde risk değerlendirmesini nasıl yapar, öncelikleri hangi kriterlerle belirlersiniz?"

**Soru aslında ne ölçüyor?**

Denetimin kaynağı sınırlıdır ve hiçbir plan her şeyi kapsayamaz. Görüşmeci burada tek şeye bakar: bu aday nereye bakacağına nasıl karar veriyor. Zayıf cevap kontrol listesi sayar. Güçlü cevap riski ölçülebilir bir eksene koyar, kapsamı o eksenden çıkarır. Ağırlık muhakemededir; terim bilgisi ikinci sıradadır. Denetim deneyimi olmayan aday bile yöntemi doğru kurarsa buradan geçebilir.

**Sözlü cevap iskeleti**

1. **Amacı ilk cümlede söyle.** Risk değerlendirmesi denetimin nereye bakacağını belirler. Her sistemi aynı derinlikte denetlemek mümkün değildir.
2. **Varlıkları çıkar, kritikliği iş üzerinden ver.** Hangi uygulama hangi iş sürecini taşıyor. Ödeme akışıyla iç raporlama aracı aynı sınıfta değildir.
3. **Riski iki eksende ölç.** Olasılık ve etki. Etkiyi tek kelimeyle geçme; mali kayıp, yasal yaptırım, itibar ve kesinti süresi ayrı ayrı konuşulur.
4. **Doğal risk ile kalan riski ayır.** Doğal risk (inherent risk) kontrol hiç yokmuş gibi bakar. Kalan risk (residual risk) mevcut kontrol çalıştıktan sonra geriye kalandır. Plan kalan riske göre yapılır.
5. **Risk iştahını kurumdan al.** Kabul edilebilir seviye denetçinin kişisel görüşü değildir. Yönetim belirler, denetçi eşiği ona göre uygular.
6. **Kapsamı örneklemeden önce dondur.** Önce hangi kontrolün test edileceği yazılır. Örnek seçimi ondan sonra gelir. Sıra tersine dönerse kapsamı bulgu belirler, denetçi değil.

```svg
<svg viewBox="0 0 660 316" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Denetimde risk degerlendirmesi: dogal risk, kontrol, kalan risk ve kapsam sirasi">
  <defs>
    <marker id="sozlu9-risk" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Denetim planı doğal riske değil, kalan riske göre kurulur</text>
  <rect x="14" y="36" width="146" height="112" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="87" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">1 · Varlık</text>
  <text x="87" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hangi uygulama</text>
  <text x="87" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hangi iş sürecini taşır</text>
  <text x="87" y="114" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veri sınıfı ve sahibi</text>
  <text x="87" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">kritiklik iş dilinde</text>
  <line x1="162" y1="92" x2="176" y2="92" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu9-risk)"/>
  <rect x="178" y="36" width="146" height="112" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="251" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">2 · Doğal risk</text>
  <text x="251" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kontrol yokmuş gibi</text>
  <text x="251" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">olasılık × etki</text>
  <text x="251" y="114" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">en kötü hâl varsayımı</text>
  <text x="251" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek başına plan yapmaz</text>
  <line x1="326" y1="92" x2="340" y2="92" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu9-risk)"/>
  <rect x="342" y="36" width="146" height="112" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="415" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">3 · Kontrol</text>
  <text x="415" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tasarım uygun mu</text>
  <text x="415" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gerçekten işliyor mu</text>
  <text x="415" y="114" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kanıtı saklanıyor mu</text>
  <text x="415" y="136" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">iki ayrı test, iki ayrı sonuç</text>
  <line x1="490" y1="92" x2="504" y2="92" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu9-risk)"/>
  <rect x="506" y="36" width="140" height="112" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="576" y="57" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">4 · Kalan risk</text>
  <text x="576" y="78" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">kontrol sonrası kalan</text>
  <text x="576" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iştah eşiğiyle karşılaş</text>
  <text x="576" y="114" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">eşiğin üstü kapsama girer</text>
  <text x="576" y="136" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">plan buradan çıkar</text>
  <rect x="14" y="164" width="316" height="118" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="172" y="185" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Etki tek kelime değildir</text>
  <text x="172" y="208" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">mali kayıp · yasal yaptırım</text>
  <text x="172" y="228" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">itibar · kesinti süresi</text>
  <text x="172" y="248" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">veri gizliliği ve bütünlüğü</text>
  <text x="172" y="270" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">her biri ayrı puanlanır, sonra birleştirilir</text>
  <rect x="344" y="164" width="302" height="118" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="495" y="185" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Sıra bozulursa denetim bozulur</text>
  <text x="495" y="208" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">doğru: kapsam yazılır, sonra örnek seçilir</text>
  <text x="495" y="228" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">yanlış: örneğe bakılır, kapsam sonra yazılır</text>
  <text x="495" y="250" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">ikinci yolda bulgu kapsamı kendisi belirler</text>
  <text x="495" y="270" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">risk iştahını yönetim belirler, denetçi uygular</text>
  <text x="330" y="302" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Değerlendirme bir kez yapılmaz; sistem, tehdit ve mevzuat değiştikçe yenilenir.</text>
</svg>
```

**Elenme sebepleri**

- Riski tek bir sayıya indirmek. Olasılık ile etki ayrı konuşulmazsa sıralama anlamını kaybeder.
- "Her şeyi denetleriz" demek. Kaynak planı olmayan denetim ne biter ne de öncelik üretir.
- Doğal risk ile kalan riski ayırmamak. Bu ayrım denetçi dilinin en temel ayrımıdır.
- Risk iştahını kendi belirlemek. Kabul edilebilir seviyeyi yönetim koyar, denetçi ölçer.
- Kapsamı örnekleme sırasında büyütmek. Kayan kapsam hem bitmez hem de karşılaştırılabilir olmaz.
- Teknik riski iş etkisine çevirmemek. "Kütüphane eski" cümlesi yönetim kurulunda karar üretmez.
- Değerlendirmeyi yıllık bir tören saymak. Yeni entegrasyon veya yeni mevzuat riski aynı gün değiştirir.

**Kendi hikâyen**

Burada denetçi hikâyesi aranmıyor olabilir; denetlenen taraftaki hikâyen de sayılır. Şu sorular onu çıkarır:

- Çalıştığın kurumda hiç denetim geçirdin mi? İç denetim miydi, dış denetim mi, müşteri denetimi mi?
- Senden ne istendi? Kod mu, log mu, onay kaydı mı, dağıtım listesi mi?
- İstenen kanıtı bulmak kolay mıydı? Bulamadığın bir kanıt oldu mu, ne yaptın?
- Hangi sistem kritik sayılıyordu? Bu kritiklik hangi ölçüte göre belirlenmişti?
- Risk konuşması hiç yapıldı mı? Bir işi "riskli" diye ertelediğin ya da öne aldığın oldu mu?
- Riski iş diline çevirmen gerekti mi? Teknik bir sorunu yöneticiye nasıl anlattın?
- Denetim sonrası ne değişti? Yeni bir kontrol mü kondu, yoksa kâğıt üstünde mi kaldı?

Hiç denetim görmediysen uydurma. `256`'daki köprü burada da işler: risk değerlendirmesinin mantığını anlat, sonra kendi projende önceliklendirmeyi nasıl yaptığını somutla. Etki ve olasılık ekseni bir hata ayıklama sırasında da kullanılır. Yeter ki bunu denetim deneyimi gibi sunma.

### "SDLC ve değişiklik yönetimi kontrollerinde en sık gördüğünüz eksiklikler nelerdir?"

**Soru aslında ne ölçüyor?**

Bu soru tanım sormuyor, gözlem soruyor. Görüşmeci adayın gerçek ortam görüp görmediğini anlamak istiyor. Kitaptan öğrenilen cevap genel konuşur; sahadan gelen cevap belirti anlatır. Bir de ikinci katman var: aday kontrolü neden var olduğunu anlıyor mu, yoksa bürokrasi mi sanıyor. Geliştirici için en verimli soru budur, çünkü kontrollerin çoğu zaten senin günlük akışında duruyor.

**Sözlü cevap iskeleti**

1. **Kontrolün amacını tek cümlede koy.** Üretime giden her değişiklik izinli, test edilmiş ve izlenebilir olmalıdır. Üç kelime de ayrı ayrı denetlenir.
2. **Görevler ayrılığından başla.** Geliştiren kişi kendi kodunu tek başına üretime çıkarmamalıdır. En sık bulgu buradadır ve en çok tartışılan da budur.
3. **Küçük ekip itirazına hazırlıklı ol.** Tam ayrılık her yerde mümkün değildir. O zaman telafi edici kontrol (compensating control) devreye girer: zorunlu ikinci onay, otomatik hat üzerinden dağıtım, değiştirilemez dağıtım kaydı (`220`).
4. **İzlenebilirlik zincirini say.** Talep, onay, iş kalemi, commit, inceleme, derleme, dağıtım kaydı. Zincirin kopan halkası doğrudan bulgudur.
5. **Acil değişiklik yolunu anlat.** Acil değişiklik yasak değildir; kayıtsız acil değişiklik yasaktır. Sonradan onay ve gerekçe kaydı şarttır. Uygulamada en sık suistimal edilen yol budur.
6. **Test kanıtına dokun.** "Hat yeşildi" bir kanıt değildir. Koşu kaydı, sürüm etiketi ve sonuç raporu saklanmalıdır (`220`).
7. **Yetki ve sır tarafını kapat.** Dağıtım hesabının yetkisi dar olmalı, üretim verisine erişim ayrı onaya bağlanmalıdır. Sırlar depoda değil sır yönetiminde durur.

```svg
<svg viewBox="0 0 660 330" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Degisiklik yonetimi: izlenebilirlik zinciri, gorevler ayriligi ve acil degisiklik yolu">
  <defs>
    <marker id="sozlu9-zincir" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İzlenebilirlik zinciri: her halkanın bir kanıtı olmalı</text>
  <rect x="16" y="38" width="116" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="74" y="58" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Talep</text>
  <text x="74" y="74" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">iş kalemi, gerekçe</text>
  <line x1="134" y1="61" x2="146" y2="61" stroke="#5D7789" stroke-width="1.3" marker-end="url(#sozlu9-zincir)"/>
  <rect x="148" y="38" width="116" height="46" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="206" y="58" text-anchor="middle" font-size="10" font-weight="700" fill="#186399" font-family="system-ui">Onay</text>
  <text x="206" y="74" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">kim, ne zaman onayladı</text>
  <line x1="266" y1="61" x2="278" y2="61" stroke="#5D7789" stroke-width="1.3" marker-end="url(#sozlu9-zincir)"/>
  <rect x="280" y="38" width="116" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="338" y="58" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">Kod ve inceleme</text>
  <text x="338" y="74" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">commit, PR onayı</text>
  <line x1="398" y1="61" x2="410" y2="61" stroke="#5D7789" stroke-width="1.3" marker-end="url(#sozlu9-zincir)"/>
  <rect x="412" y="38" width="112" height="46" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="468" y="58" text-anchor="middle" font-size="10" font-weight="700" fill="#25804C" font-family="system-ui">Test kanıtı</text>
  <text x="468" y="74" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">koşu kaydı saklanır</text>
  <line x1="526" y1="61" x2="538" y2="61" stroke="#5D7789" stroke-width="1.3" marker-end="url(#sozlu9-zincir)"/>
  <rect x="540" y="38" width="106" height="46" rx="8" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.4"/>
  <text x="593" y="58" text-anchor="middle" font-size="10" font-weight="700" fill="#1C3247" font-family="system-ui">Dağıtım</text>
  <text x="593" y="74" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">sürüm ve zaman kaydı</text>
  <text x="330" y="102" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">Kopan tek halka bulgudur: üretimdeki sürümü hangi talebin doğurduğu gösterilemiyorsa zincir yoktur.</text>
  <rect x="14" y="116" width="316" height="122" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="137" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Görevler ayrılığı bozulunca</text>
  <text x="172" y="160" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geliştiren kişi kendi kodunu çıkarır</text>
  <text x="172" y="180" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">üretim yetkisi geniş ve kalıcıdır</text>
  <text x="172" y="200" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dağıtım kaydı sonradan silinebilir</text>
  <text x="172" y="226" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">tek başına yapılan değişiklik denetlenemez</text>
  <rect x="344" y="116" width="302" height="122" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="137" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Küçük ekipte telafi edici kontrol</text>
  <text x="495" y="160" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">zorunlu ikinci onay, kendi PR'ını onaylayamama</text>
  <text x="495" y="180" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dağıtım yalnız otomatik hattan yapılır</text>
  <text x="495" y="200" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dağıtım kaydı değiştirilemez biçimde saklanır</text>
  <text x="495" y="226" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">ayrılık kişiyle sağlanamıyorsa araçla sağlanır</text>
  <rect x="14" y="250" width="632" height="66" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="270" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Acil değişiklik yasak değildir; kayıtsız acil değişiklik yasaktır</text>
  <text x="330" y="290" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Sonradan onay, gerekçe ve etkilenen sürüm kaydı zorunludur; yol açık ama izli olmalıdır.</text>
  <text x="330" y="308" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Acil kaydı oranı yükseliyorsa bu bir istisna değil, sürecin kendisi bozulmuş demektir.</text>
</svg>
```

**Elenme sebepleri**

- Kontrolleri gereksiz bürokrasi diye anlatmak. Denetim mülakatında bu cümle sohbeti bitirir.
- Görevler ayrılığını hiç anmamak. Değişiklik yönetiminin ilk kontrolü budur ve atlanması dikkat çeker.
- "Küçük ekipte bu kurallar geçmez" demek. Doğru cevap kuralı düşürmek değil, telafi edici kontrol kurmaktır.
- Acil değişikliği tamamen yasaklamayı önermek. Yasaklanan yol kayıt dışına çıkar ve görünmez olur.
- Test kanıtı ile test sonucu farkını kaçırmak. Kanıt saklanmadıysa test yapılmış sayılmaz.
- İzlenebilirliği yalnız commit mesajına indirmek. Zincir talepten dağıtıma kadar bütün olmalıdır.
- Üretim verisine erişimi normal saymak. Hata ayıklama gerekçesi kalıcı yetkiyi meşrulaştırmaz.

**Kendi hikâyen**

Bu soruda hikâyeyi bulmak kolaydır, çünkü kontroller senin günlük akışında zaten var. Şunları sor:

- Senin projende üretime kim çıkarıyor? Geliştiricinin kendisi mi, ayrı bir ekip mi, otomatik hat mı?
- Kendi PR'ını onaylayabiliyor musun? Araç bunu engelliyor mu, yoksa yalnız teamül mü?
- Bugün üretimde duran bir sürümü hangi talebe kadar geriye izleyebilirsin? Kaç adımda kopuyor?
- Acil değişiklik yaptığın oldu mu? Kayıt sonradan girildi mi, yoksa hiç girilmedi mi?
- Test kanıtı saklanıyor mu? Hat çıktısı ne kadar süre duruyor, kimse ona bakıyor mu?
- Üretim verisine erişimin var mı? Nasıl verildi, süreli miydi, geri alındı mı?
- Bir denetim sonrası süreç değişti mi? Hangi adım eklendi, hangi adım gerçekten uygulandı?

Bunların hiçbiri yoksa bu da bir cevaptır ve dürüstçe söylenir. `242`'de bu tür soruların bankacılık ortamındaki karşılığı var. Eksikliği görüp adlandırabilmek, kontrolü ezberlemekten daha güçlü bir sinyaldir.

### "Kaynak kod incelemesi ve statik analizde hangi araç ve metrikleri kullanır, bulguları nasıl doğrularsınız?"

**Soru aslında ne ölçüyor?**

Sorunun ağırlığı son iki kelimede: nasıl doğrularsınız. Araç adı saymak kolaydır ve ayırt etmez. Görüşmeci burada disiplini ölçer: bu aday aracın çıktısını kanıt sanıyor mu. Denetimde bir uyarı, doğrulanana kadar yalnız adaydır. Yanlış pozitifi elemeyen denetçi rapor şişirir ve güvenilirliğini kaybeder. Ağırlık muhakemede; araç bilgisi destekleyici unsurdur.

**Sözlü cevap iskeleti**

1. **Aracın rolünü doğru koy.** Statik analiz kanıt üretmez, aday liste üretir. Doğrulama insanın işidir.
2. **Ne aradığını söyle.** Enjeksiyon, eksik yetki kontrolü, koda gömülü sır, zayıf kripto, doğrulanmamış girdi. Bunlar tesadüfi değil, bilinen sınıflardır (`190`).
3. **Bağımlılık tarafını ayrı tut.** Kendi kodun temiz olabilir; kütüphanede bilinen açık olabilir. Bileşen envanteri ve sürüm listesi ayrı bir kontroldür (`191`).
4. **Metrikleri seçerken bedelini söyle.** Karmaşıklık ve yinelenme bakım riskini gösterir. Kapsam yüzdesi tek başına ölçüt değildir; kritik yolun kapsanması sorulur (`132`, `261`).
5. **Yanlış pozitifi eleme adımını anlat.** Üç soru sorulur: bu kod yolu dışarıdan gelen girdiyle ulaşılabilir mi, veri gerçekten güvenilmez mi, telafi eden bir kontrol var mı.
6. **Bulguyu tekrarlanabilir hâle getir.** Dosya, satır, sürüm, tarih ve üretme adımı yazılır. Başka biri aynı adımla aynı sonuca varmalıdır.
7. **Süreci de denetle.** Kural setini kim değiştiriyor, hat gerçekten kapı gibi mi çalışıyor, yoksa uyarılar atlanabiliyor mu (`191`).

**Elenme sebepleri**

- Araç adı listesiyle yetinmek. Soru araç değil yöntem soruyor; liste ayırt etmez.
- Aracın çıktısını doğrudan bulgu yazmak. Doğrulanmamış uyarı raporu şişirir ve güveni düşürür.
- Yanlış pozitif kavramını hiç anmamak. Elemeyi anlatmayan aday sahayı görmemiş sayılır.
- Test kapsamı yüzdesini tek ölçüt yapmak. Yüksek kapsam, kritik yolun test edildiğini kanıtlamaz.
- Bağımlılıkları unutmak. Bugünkü açıkların önemli bölümü kendi kodunda değil, kullandığın kütüphanededir.
- Bulguyu sürüm ve tarih vermeden yazmak. Tekrarlanamayan bulgu tartışmada ayakta kalmaz.
- Yalnız otomatik taramaya güvenmek. İş kuralı hatası ve yetki mantığı hatası çoğu araçtan kaçar.

**Kendi hikâyen**

Burada kendi kod inceleme alışkanlığın doğrudan malzemedir. Şu sorular onu çıkarır:

- Projende statik analiz koşuyor mu? Hatta zorunlu kapı mı, yoksa uyarı olarak mı geçiyor?
- Hiç yanlış pozitif elediğin oldu mu? Nasıl karar verdin, neye baktın?
- Bir uyarıyı bastırdınız mı? Gerekçe yazıldı mı, kim onayladı?
- Bağımlılık taraması var mı? Bilinen bir açık için sürüm yükselttiğin oldu mu?
- Kod incelemede en sık hangi hatayı yakalıyorsun? Bu hatayı otomatikleştirmek mümkün müydü?
- Kapsam eşiğiniz var mı? Eşik kritik yolu gerçekten kapsıyor mu, yoksa sayıyı mı kurtarıyor?
- Bir güvenlik bulgusunu sen mi buldun? Nasıl doğruladın, gerçekten sömürülebilir miydi?

Bu deneyim yoksa uydurma. Kendi projende bir statik analiz aracı koşup çıkan uyarıları elemek gerçek bir gözlemdir. `256`'daki köprüyle bunu dürüstçe anlatırsın: yöntemi biliyorum, kurumsal ölçekte uygulamadım.

### "Erişim kontrolleri ve yetkilendirme süreçlerini denetlerken hangi kanıtları toplar, nasıl örnekleme yaparsınız?"

**Soru aslında ne ölçüyor?**

Bu soru iki parçadan oluşuyor ve adaylar genellikle ilkini cevaplayıp ikincisini geçiyor. Kanıt listesi saymak öğrenilebilir. Örneklemeyi doğru anlatmak ise denetim disiplinini gösterir. Görüşmecinin asıl aradığı şu: bu aday listenin kendisine güveniyor mu, yoksa listenin tamlığını da sorguluyor mu. Ağırlık yöntemdedir. Bir de sessiz bir kontrol var: aday en az yetki ilkesini teoride mi biliyor, uygulamada mı.

**Sözlü cevap iskeleti**

1. **Kontrol hedefini yaz.** Doğru kişi, doğru yetki, doğru süre. Üçü de ayrı ayrı test edilir.
2. **Kanıt listesini say.** Rol matrisi, kullanıcı listesi, onay kayıtları, işten ayrılanlar listesi, ayrıcalıklı hesap dökümü, erişim logu örneği (`189`).
3. **Kanıtın kaynağını sorgula.** Liste hangi sistemden, hangi tarihte, kim tarafından alındı. Ekran görüntüsü tek başına zayıf kanıttır.
4. **Nüfusu (population) önce tanımla.** Örnek seçmeden önce listenin tam olduğunu doğrula. Eksik listeden seçilen örnek hiçbir şeyi temsil etmez.
5. **Örnekleme yöntemini gerekçelendir.** Rastgele seçim tarafsızdır; riske göre seçim yüksek yetkiye odaklanır. Ayrıcalıklı hesaplarda örnekleme yerine tam sayım tercih edilir.
6. **Somut testleri say.** İşten ayrılan kullanıcı hâlâ aktif mi. Gerçek yetkiler rol matrisiyle uyuşuyor mu. Periyodik gözden geçirme yapılmış ve onaylanmış mı. Ortak hesabın sahibi belli mi (`86`, `87`).
7. **Bulguyu etkiye bağla.** "Üç kullanıcı fazla yetkili" zayıftır. "Ayrılan üç kullanıcı ödeme onayı yetkisini koruyor" karar ürettirir.

**Elenme sebepleri**

- Listenin tamlığını hiç sorgulamamak. Eksik nüfustan seçilen örnek denetimi baştan geçersiz kılar.
- Örnek sayısını gerekçesiz vermek. Sabit bir sayı söylemek, yöntemi bilmediğini gösterir.
- Ayrıcalıklı hesapları normal kullanıcı gibi örneklemek. Bu hesaplarda beklenti tam incelemedir.
- Rol matrisi ile gerçek yetkiyi karşılaştırmamak. Kâğıttaki rol ile sistemdeki yetki çoğu yerde ayrışır.
- İşten ayrılan kullanıcı testini atlamak. Bu, en hızlı ve en çok bulgu üreten testlerden biridir.
- Servis hesaplarını kapsam dışı bırakmak. Sahipsiz servis hesabı en az yetki ilkesini sessizce deler.
- Bulguyu iş etkisi olmadan yazmak. Yetki fazlalığı hangi işlemi mümkün kılıyor, söylenmelidir.

**Kendi hikâyen**

Erişim konusunda hemen herkesin bir hikâyesi vardır; genellikle fark edilmez. Şu sorular onu çıkarır:

- Projende yetkiler nasıl veriliyor? Rol üzerinden mi, kişiye özel mi, ikisi karışık mı?
- Kendi yetkilerini hiç saydın mı? İhtiyacın olmayan bir yetkin var mı?
- Ayrılan bir arkadaşının hesabı ne kadar sürede kapandı? Kapandığını kim doğruladı?
- Ortak kullanılan bir hesap var mı? Parolası nerede duruyor, kaç kişi biliyor?
- Servis hesaplarının sahibi belli mi? Yetkileri son bir yılda gözden geçirildi mi?
- Bir yetkiyi geçici aldın mı? Süre sonunda geri alındı mı, yoksa unutuldu mu?
- Yetki değişikliği kayıtlanıyor mu? Kim, kime, ne zaman verdi; bu bilgiye ulaşabiliyor musun?

Denetim tarafında deneyimin yoksa uydurma. Kendi uygulamanda yetkilendirmeyi tasarlamış olmak da anlatılabilir bir temeldir (`86`, `87`). `256`'daki köprüyle farkı adlandırırsın: ben kontrolü kurdum, kontrolün kanıtını denetleyen taraf olmadım.

### "Denetimde kritik bir güvenlik açığı bulduğunuzda paydaş iletişimini ve düzeltici aksiyon takibini nasıl yürütürsünüz?"

**Soru aslında ne ölçüyor?**

Bu soru teknik değil, olgunluk sorusudur. Kritik bir açık bulmak heyecan vericidir ve acele ettirir. Görüşmeci tam burada soğukkanlılık arar. İki hata yaygındır: doğrulamadan bildirmek ve açığı çok geniş bir kitleye duyurmak. İkisi de zarar üretir. Ağırlık yargı ve iletişimdedir; teknik derinlik burada ikinci sıradadır.

**Sözlü cevap iskeleti**

1. **Önce doğrula.** Yanlış alarm bir denetçinin en pahalı hatasıdır. Bir kez güven kaybedilirse sonraki bulgular da tartışılır.
2. **Şiddeti sınıflandır ve gerekçesini yaz.** Erişim gerektiriyor mu, veri sızıyor mu, internete açık mı. Sınıflandırma bir duygu değil, ölçüttür.
3. **Kanalı bilerek daralt.** Açık kapanmadan geniş duyuru riski büyütür. Sistem sahibi, güvenlik ve gereken yönetici seviyesi yeterlidir.
4. **Bulguyu dört parçayla yaz.** Durum nedir, ölçüt nedir, etkisi nedir, önerin nedir. Bu biçim tartışmayı kişiden çıkarır.
5. **Geçici önlem ile kalıcı çözümü ayır.** Erişimi kapatmak bugünü kurtarır. Kalıcı düzeltme ayrı bir kalem olarak takip edilir.
6. **Sahip ve tarih ata.** Sahipsiz aksiyon kapanmaz. Tarihi denetçi tek başına koymaz; sahibiyle birlikte belirlenir (`195`).
7. **Kapanışı kanıtla doğrula.** "Düzelttik" cümlesi kapatma sebebi değildir. Yeniden test edilir, kanıt eklenir, sonra kapatılır (`189`).

**Elenme sebepleri**

- Doğrulamadan alarm vermek. Yanlış pozitif bir kez bile bildirilse sonraki raporun ağırlığı düşer.
- Açığı geniş kitleye duyurmak. Kapanmamış bir açığın dağıtımı riski çoğaltır.
- Şiddeti gerekçesiz vermek. "Kritik" kelimesi ölçüt olmadan pazarlık konusu olur.
- Aksiyona sahip ve tarih yazmamak. Sahipsiz madde bir sonraki denetimde aynen durur.
- Geçici önlemi kalıcı çözüm saymak. Erişimi kapatmak kök nedeni ortadan kaldırmaz.
- Kapanışı beyanla kabul etmek. Kanıtsız kapatma, denetimin kendi kontrolünü zayıflatır.
- Suçlayıcı dille yazmak. Ekip savunmaya geçerse düzeltme yavaşlar; dil kişiye değil kontrole yönelir.

**Kendi hikâyen**

Bu soruda güvenlik bulgusu deneyimi olmayabilir; kötü haber taşıma deneyimi genellikle vardır. Şunları sor:

- Hiç kritik bir sorunu sen mi fark ettin? Ne zaman ve nasıl fark ettin?
- Kime söyledin? Doğrudan yöneticine mi, ekibe mi, güvenlik tarafına mı?
- Söylemeden önce doğruladın mı? Nasıl doğruladın, elinde ne vardı?
- Tepki ne oldu? Ciddiye alındı mı, ertelendi mi, savunmaya mı geçildi?
- Geçici bir önlem alındı mı? Ne kadar sürede alındı, kim karar verdi?
- Kalıcı düzeltme takip edildi mi? Kim sahiplendi, tarihi tuttu mu?
- Kapandığını nasıl anladın? Yeniden test edildi mi, yoksa sözlü onayla mı kapandı?

Böyle bir vaka yoksa uydurma; güvenlik konusunda uydurulan hikâye en çabuk yakalanandır. `256`'daki köprüyü kur ve `195`'teki suçsuz yaklaşımı anlat. Kötü haberi zamanında ve doğru kişiye söylemiş olmak, tek başına anlatılmaya değer bir davranıştır.

### "Uyum gereksinimlerini denetim kriterlerine nasıl çevirir, çalışma kağıtlarını nasıl oluşturursunuz?"

**Soru aslında ne ölçüyor?**

Bu dosyanın en ayrıştırıcı sorusu budur ve çoğu aday hazırlıksız yakalanır. Uyum metinleri soyut yazılır; denetim ise somut kanıt ister. Aradaki mesafeyi kapatmak denetçiliğin asıl işidir. Görüşmeci burada bir çeviri becerisi arar: soyut cümleyi test edilebilir adıma dönüştürebiliyor musun. İkinci ölçüt tekrarlanabilirliktir. Senin kağıdınla başka bir denetçi aynı sonuca varamıyorsa, ortada denetim değil kişisel kanaat vardır.

**Sözlü cevap iskeleti**

1. **Zinciri baştan söyle.** Gereksinim, kontrol hedefi, kontrol, test adımı, kanıt, sonuç. Cevabın omurgası bu altı halkadır.
2. **Soyut ifadeyi ölçülebilir hâle getir.** "Yetkisiz erişim engellenmeli" test edilemez. "Üretim veritabanı erişimi onaylı olmalı ve üç ayda bir gözden geçirilmeli" test edilebilir.
3. **Kontrolün tasarımı ile işleyişini ayır.** Tasarım testi kontrolün doğru kurgulandığını sorar. İşletim etkinliği testi dönem boyunca gerçekten çalıştığını sorar. İki ayrı sonuç üretirler.
4. **Eşleme tablosu kur.** Bir kontrol birden çok gereksinimi karşılayabilir. Eşleme, aynı testi tekrar tekrar koşmayı önler.
5. **Test adımını yazarken kanıtı adresle.** Hangi sistemden, hangi dönem için, hangi çıktı alınacak. Adım okunduğunda uygulanabilir olmalıdır.
6. **Çalışma kağıdının alanlarını say.** Amaç, nüfus, örnek ve seçim yöntemi, uygulanan adım, kanıt referansı, sonuç, hazırlayan ve tarih.
7. **Tekrarlanabilirlik ölçütünü söyle.** Başka bir denetçi aynı kağıtla aynı sonuca varmalıdır. Varamıyorsa kağıt eksiktir, sonuç değil.

```svg
<svg viewBox="0 0 660 336" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Uyum gereksinimini denetim kriterine cevirme zinciri ve calisma kagidi alanlari">
  <defs>
    <marker id="sozlu9-kanit" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Soyut gereksinim, kanıta bağlanmadan denetlenemez</text>
  <rect x="16" y="38" width="150" height="62" rx="9" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="91" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gereksinim</text>
  <text x="91" y="76" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">"yetkisiz erişim</text>
  <text x="91" y="91" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">engellenmelidir"</text>
  <line x1="168" y1="69" x2="182" y2="69" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu9-kanit)"/>
  <rect x="184" y="38" width="150" height="62" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="259" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kontrol hedefi</text>
  <text x="259" y="76" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">üretim erişimi onaylı ve</text>
  <text x="259" y="91" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">dönemsel gözden geçirilir</text>
  <line x1="336" y1="69" x2="350" y2="69" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu9-kanit)"/>
  <rect x="352" y="38" width="150" height="62" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="427" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#25804C" font-family="system-ui">Test adımı</text>
  <text x="427" y="76" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">25 kullanıcı seç, onay ve</text>
  <text x="427" y="91" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">gözden geçirme kaydı iste</text>
  <line x1="504" y1="69" x2="518" y2="69" stroke="#5D7789" stroke-width="1.4" marker-end="url(#sozlu9-kanit)"/>
  <rect x="520" y="38" width="126" height="62" rx="9" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="583" y="58" text-anchor="middle" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Kanıt ve sonuç</text>
  <text x="583" y="76" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">tarihli çıktı, imzalı kayıt</text>
  <text x="583" y="91" text-anchor="middle" font-size="8.5" fill="#1C3247" font-family="system-ui">uygun ya da bulgu</text>
  <rect x="14" y="114" width="316" height="134" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="172" y="135" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Çalışma kağıdının alanları</text>
  <text x="172" y="157" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">amaç ve kontrol hedefi</text>
  <text x="172" y="176" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">nüfus, tamlık doğrulaması</text>
  <text x="172" y="195" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">örnek ve seçim yöntemi</text>
  <text x="172" y="214" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">uygulanan adım, kanıt referansı</text>
  <text x="172" y="236" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">sonuç · hazırlayan · tarih</text>
  <rect x="344" y="114" width="302" height="134" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="495" y="135" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">İki ayrı test, iki ayrı sonuç</text>
  <text x="495" y="157" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tasarım testi: kontrol doğru kurgulanmış mı</text>
  <text x="495" y="176" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işletim testi: dönem boyunca çalışmış mı</text>
  <text x="495" y="195" text-anchor="middle" font-size="9.5" fill="#B23B3B" font-family="system-ui">tasarım iyi, işleyiş kötü olabilir</text>
  <text x="495" y="214" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bir kontrol birden çok gereksinimi karşılayabilir</text>
  <text x="495" y="236" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">eşleme tablosu tekrar eden testi önler</text>
  <rect x="14" y="260" width="632" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="330" y="280" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Ölçüt: başka bir denetçi aynı kağıtla aynı sonuca varmalı</text>
  <text x="330" y="299" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Varamıyorsa eksik olan sonuç değil, kağıttır: adım, nüfus ya da kanıt referansı yazılmamıştır.</text>
  <text x="330" y="315" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Kanıt kaynağı, tarihi ve alan kişi yazılmayan bir çıktı tartışmada dayanak olmaz.</text>
</svg>
```

**Elenme sebepleri**

- Uyum maddesini olduğu gibi kontrol hedefi yazmak. Soyut cümle test adımı üretmez.
- Kanıtı adreslememek. Hangi sistemden, hangi dönem için alınacağı yazılmayan adım uygulanamaz.
- Tasarım testi ile işletim testini ayırmamak. Doğru kurgulanmış bir kontrol dönem boyunca hiç çalışmamış olabilir.
- Beyanı kanıt saymak. Sorumlunun yazılı ifadesi destekleyicidir, tek başına yeterli değildir.
- Ekran görüntüsünü tek kanıt yapmak. Kaynağı ve tarihi belirsiz görüntü tartışmayı taşımaz.
- Aynı kontrolü her gereksinim için baştan test etmek. Eşleme tablosu yoksa denetim gereksiz yere uzar.
- Çalışma kağıdını kendine not gibi yazmak. Okuyan kişi adımı tekrarlayamıyorsa kağıt işlevsizdir.

**Kendi hikâyen**

Bu soruda hikâye çoğu geliştiricide dolaylıdır; yine de vardır. Şunları sor:

- Hiç uyum gereksinimi gördün mü? Hangi alandaydı: bankacılık, sağlık, kişisel veri?
- Sana somut olarak ne yaptırdı? Log saklama süresi, maskeleme, erişim kaydı, onay adımı?
- Gereksinimi kim yorumladı? Sen mi, hukuk mu, güvenlik mi; yorum farkı çıktı mı?
- Kanıt üretmen istendi mi? Neyi çıkardın: ekran görüntüsü, sorgu çıktısı, konfigürasyon dosyası?
- Aynı kanıtı ikinci kez üretebildin mi? Nasıl ürettiğini yazmış mıydın?
- Bir maddeyi teknik adıma çevirmen gerekti mi? Hangi cümleyi hangi kontrole bağladın?
- Denetçi bir kanıtını reddetti mi? Gerekçesi neydi, sonra ne değişti?

Hiç uyum işi görmediysen uydurma. `256`'daki köprü şöyle kurulur: çeviri mantığını anlat, sonra kendi işinden bir örnek ver. Bir kabul ölçütünü test adımına çevirmiş olmak da aynı düşünme biçimidir. Farkı adlandır; denetim kağıdı yazmadığını dürüstçe söyle.

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, konuşulmak için yazıldı. Kullanımı şöyledir:

- Önce iskeleti okuma. Soruyu yüksek sesle oku, sonra dosyayı kapat.
- Süre tut. Hedef doksan saniyedir. Kısası eksik kalır, uzunu görüşmecinin ilgisini kaybettirir.
- Yüksek sesle cevapla ve kaydet. İçinden geçirmek prova sayılmaz.
- Sonra iskeletle karşılaştır. Hangi adımı atladın, işaretle.
- Bu altı soruda en sık atlanan adım kanıt cümlesidir. Kontrolü anlatıp kanıtını söylemezsen cevap yarım kalır.
- İkinci en sık eksik, denetlenen taraf ile denetçi rolünü karıştırmaktır. Rolünü baştan söyle.
- İkinci turda yalnız eksik adımı çalış. Baştan anlatmak zamanı harcar, eksiği kapatmaz.
- Deneyimin olmadığı sorularda köprü cümlesini de prova et. Dürüst köprü hazırlıksız kurulunca özür gibi duyulur.
- Ölçüt şudur: bir sorunun adımlarını kâğıda bakmadan sırayla sayabiliyorsan o soru hazır demektir.

### Tek cümlede

Bu altı soru denetçi dilinde sorulur, ama seni denetçi yapmaya çalışmaz. Hepsinin altında tek bir alışkanlık yatar: bir şeyin yapıldığını söylemek yetmez, kanıtını gösterebilmek gerekir. Risk sorusunda kazandıran ayrım doğal risk ile kalan risktir. Değişiklik sorusunda kazandıran cümle görevler ayrılığı ve izlenebilirlik zinciridir. Kod incelemede kazandıran şey araç listesi değil, yanlış pozitifi eleme disiplinidir. Erişim sorusunda örnek seçmeden önce nüfusun tamlığını sormak ayırt eder. Kritik açık sorusunda sıra bellidir: önce doğrula, sonra dar kanaldan bildir, sahip ve tarih ata, kapanışı kanıtla. Uyum sorusunda ise ölçüt tekrarlanabilirliktir; başka bir denetçi aynı kağıtla aynı sonuca varmalıdır. Denetim deneyimin yoksa bunu söyle ve denetlenen taraftaki gözlemini anlat; uydurulmuş bir denetim hikâyesi ilk ayrıntıda dağılır.

# sozlu9-1 | Temel
## Soru
Bir denetçi yıllık denetim planını hazırlıyor. İki uygulamadan birinde güçlü kontroller çalışıyor, diğerinde hiçbir kontrol yok. Plan hangi risk türüne göre önceliklendirilir?
## A
Doğal riske göre; kontroller zamanla değişebildiği için yok sayılır ve iki sistem eşit derinlikte denetlenir
🤖 doğal risk başlangıç noktasıdır; mevcut kontrol etkisi hesaba katılmadan plan yapılmaz.
## B
Yalnız geçen yılın bulgu listesine göre; yeni risk kaynakları bir sonraki döneme bırakılır
🤖 geçmiş bulgu bir girdidir, tek ölçüt değildir; risk her dönem yeniden değerlendirilir.
## C
Yalnız etki eksenine göre; olasılık denetim planında ölçülemediği için hesaba katılmaz
🤖 olasılık da değerlendirilir; tek eksenli bakış öncelik sırasını bozar.
## D
Kalan riske göre; mevcut kontroller çalıştıktan sonra geriye kalan risk kapsamı belirler
🤖 Doğru: plan kontrol sonrası kalan riske göre önceliklendirilir.
## Doğru
D
## Açıklama
Denetimde iki risk kavramı ayrı ayrı kullanılır. **Doğal risk** (inherent risk), hiçbir kontrol yokmuş gibi bakıldığında ortaya çıkan risktir. **Kalan risk** (residual risk) ise mevcut kontroller çalıştıktan sonra geriye kalandır. Denetim planı kalan riske göre kurulur, çünkü denetimin işi kontrolü olmayan ya da kontrolü zayıf alanları bulmaktır. Güçlü kontrolü olan sistemde kalan risk düşer; kaynak, kontrolsüz sisteme kaydırılır. A şıkkı doğal riski tek ölçüt yapar ve kontrol yatırımını görmezden gelir; sonuçta her sistem eşit derinlikte denetlenir, kaynak boşa gider. C şıkkı riski tek eksene indirir; oysa etki ve olasılık birlikte değerlendirilir ve etki de tek kalem değildir. Mali kayıp, yasal yaptırım, itibar ve kesinti süresi ayrı ayrı puanlanır. B şıkkı geçmiş bulguları tek girdi sayar; geçmiş yararlıdır ama yeni entegrasyon, yeni mevzuat ve yeni tehdit her dönem risk haritasını değiştirir. Kabul edilebilir seviyeyi, yani risk iştahını denetçi değil yönetim belirler; denetçi bu eşiği ölçer ve uygular.

# sozlu9-2 | Orta
## Soru
Beş kişilik bir ekipte geliştiriciler kendi kodlarını üretime çıkarıyor ve ayrı bir dağıtım ekibi kurmak mümkün değil. Denetçinin beklediği en yerinde çözüm hangisidir?
## A
Uygulamayı olduğu gibi kabul etmek; ekip küçük olduğu için görevler ayrılığı bu ölçekte aranmaz
🤖 ölçek kontrolü kaldırmaz; beklenti düşer değil, biçim değişir.
## B
Telafi edici kontrol kurmak: zorunlu ikinci onay, yalnız otomatik hattan dağıtım ve değiştirilemez dağıtım kaydı
🤖 Doğru: ayrılık kişiyle sağlanamıyorsa araçla ve kayıtla karşılanır.
## C
Dağıtım yetkisini tek bir kıdemli geliştiriciye vermek; böylece sorumluluk tek kişide toplanmış olur
🤖 tek kişide toplamak ayrılık üretmez; yalnız riski o kişiye yığar.
## D
Dağıtımları ayda bir toplu pencereye almak; sıklık azalınca kontrol ihtiyacı da kendiliğinden azalır
🤖 seyrek dağıtım riski büyütür; toplu değişiklik hem daha az izlenir hem daha zor geri alınır.
## Doğru
B
## Açıklama
Görevler ayrılığı (segregation of duties), değişiklik yönetiminin ilk kontrolüdür: geliştiren kişi kendi değişikliğini tek başına üretime taşımamalıdır. Küçük ekiplerde bu ayrılık kişi bazında her zaman kurulamaz. Denetim bunu bilir ve kuralı düşürmek yerine **telafi edici kontrol** (compensating control) bekler. Pratikte üç adım yeter. Kendi değişikliğini kendin onaylayamazsın; dağıtım yalnız otomatik hattan yapılır; dağıtım kaydı sonradan değiştirilemez biçimde saklanır (`220`). Bu üçü birlikte, ayrı bir ekip kurmadan aynı güvenceyi üretir. A şıkkı beklentiyi tamamen kaldırır; ölçek küçüldüğünde kontrolün biçimi değişir, varlığı değil. C şıkkı yetkiyi tek kişide toplar; bu bir ayrılık değil, tek nokta riskidir ve o kişi izinliyken süreç durur. D şıkkı sıklığı azaltarak sorunu çözmeye çalışır; oysa seyrek ve büyük dağıtımlar hem daha çok değişiklik taşır hem geri alınması zordur. Denetçinin bu konudaki asıl sorusu tektir: üretimdeki sürümü hangi talebin doğurduğunu kanıtla gösterebiliyor musunuz.

# sozlu9-3 | Orta
## Soru
Statik analiz aracı bir sınıfta SQL enjeksiyonu uyarısı üretti. Denetçi bunu rapora bulgu olarak yazmadan önce hangi adımı atmalıdır?
## A
Uyarıyı doğrudan kritik bulgu olarak raporlamak; aracın çıktısı bağımsız bir kanıt sayılır
🤖 araç çıktısı aday listedir; doğrulanmadan bulgu olmaz.
## B
Kural setini gevşetip uyarı kaybolursa bulguyu kapatmak; eşik ayarı tamamen ekibin tercihidir
🤖 kural değiştirerek uyarıyı susturmak riski kaldırmaz, yalnız görünmez yapar.
## C
Kod yolunun dışarıdan gelen girdiyle ulaşılabilir olduğunu ve telafi eden kontrol bulunmadığını doğrulamak
🤖 Doğru: bulgu, sömürülebilirliği doğrulandıktan sonra raporlanır.
## D
Test kapsamı yüzdesine bakmak; kapsam yüksekse uyarının yanlış pozitif olduğu kabul edilir
🤖 kapsam yüksekliği bir güvenlik açığının yokluğunu kanıtlamaz.
## Doğru
C
## Açıklama
Statik analiz kanıt değil **aday liste** üretir. Denetçinin işi bu listeyi doğrulamaktır; doğrulanmamış uyarıyı rapora yazmak, güvenilirliği düşüren en hızlı yoldur. Doğrulama üç soruyla yapılır. Bu kod yolu dışarıdan gelen bir girdiyle ulaşılabilir mi; veri gerçekten güvenilmez bir kaynaktan mı geliyor; araya giren bir kontrol var mı, örneğin parametreli sorgu ya da giriş kapısındaki doğrulama (`190`). Üçü de olumluysa bulgu gerçektir ve dosya, satır, sürüm ve tarih ile tekrarlanabilir biçimde yazılır. A şıkkı aracı hakem yerine koyar. B şıkkı en tehlikeli seçenektir: kural setini gevşetmek riski kaldırmaz, yalnız uyarıyı susturur; kural değişikliğinin kimin tarafından yapıldığı da ayrı bir denetim konusudur (`191`). D şıkkı iki farklı şeyi karıştırır; test kapsamı bakım riski hakkında fikir verir ama güvenlik açığının yokluğunu göstermez. Kapsam yüzdesi zaten tek başına ölçüt değildir; sorulması gereken, kritik yolun test edilip edilmediğidir (`132`, `261`).

# sozlu9-4 | Orta
## Soru
Denetçi erişim yetkilerini test etmek için kullanıcı listesinden örnek seçecek. Örnek seçiminden önce yapılması zorunlu adım hangisidir?
## A
Nüfusun tamlığını doğrulamak: liste hangi sistemden, hangi tarihte ve kim tarafından alındı
🤖 Doğru: eksik listeden seçilen örnek hiçbir sonucu temsil etmez.
## B
Örnek sayısını sabit yirmi kayıt olarak belirlemek; bu sayı standarttır ve nüfustan bağımsızdır
🤖 örnek büyüklüğü riske ve nüfusa göre gerekçelendirilir; sabit sayı yöntem değildir.
## C
Yalnız ayrıcalıklı hesapları seçmek; normal kullanıcılar erişim denetiminin kapsamı dışındadır
🤖 ayrıcalıklı hesap tam incelenir, ama normal kullanıcılar kapsam dışı kalmaz.
## D
Listeyi sistem yöneticisinden alıp doğrudan kullanmak; kaynak sistem çıktısı her zaman güncel kabul edilir
🤖 kaynağı ve tarihi doğrulanmayan liste kanıt değeri taşımaz.
## Doğru
A
## Açıklama
Örnekleme, nüfusun (population) doğru olduğu varsayımına dayanır. Liste eksikse seçilen örnek temsil gücünü kaybeder ve test sonucu geçersiz olur. Bu yüzden ilk adım **tamlık doğrulamasıdır**: çıktı hangi sistemden alındı, hangi tarihte alındı, kim aldı, filtre uygulandı mı. Kayıt sayısı bağımsız bir kaynakla karşılaştırılabiliyorsa daha da iyidir. D şıkkı tam bu adımı atlar; yöneticiden gelen bir çıktı iyi niyetle hazırlanmış olsa bile kaynağı ve tarihi yazılmadan kanıt sayılmaz. B şıkkı örnek büyüklüğünü sabit bir sayıya bağlar; oysa sayı riske, nüfus büyüklüğüne ve kontrolün sıklığına göre gerekçelendirilir. C şıkkı kapsamı daraltır. Ayrıcalıklı hesaplarda beklenti genellikle örnekleme değil tam sayımdır, ama normal kullanıcılar kapsam dışı kalmaz; en az yetki ilkesi herkes için test edilir (`189`). Nüfus doğrulandıktan sonra asıl testler gelir: işten ayrılan kullanıcı hâlâ aktif mi, gerçek yetkiler rol matrisiyle uyuşuyor mu, dönemsel gözden geçirme yapılıp onaylanmış mı (`86`, `87`).

# sozlu9-5 | İleri
## Soru
Denetim sırasında üretim ortamında sömürülebilir kritik bir açık tespit edildi. Denetçinin ilk hamlesi ne olmalıdır?
## A
Bulguyu dönem sonu denetim raporuna yazıp normal raporlama takvimini beklemek
🤖 kritik bulgu takvim beklemez; gecikme riski açık kaldığı sürece büyütür.
## B
Açığı tüm geliştirme ekiplerine ve iş birimlerine aynı anda duyurup farkındalık oluşturmak
🤖 kapanmamış açığın geniş dağıtımı riski çoğaltır; kanal bilerek daraltılır.
## C
Bulguyu doğrulayıp şiddetini sınıflandırmak, sonra dar bir çevreye bildirip geçici azaltıcı önlemi konuşmak
🤖 Doğru: önce doğrula, sonra doğru kişilere dar kanaldan bildir.
## D
Açığı üretim ortamında yeniden sömürerek etkiyi genişletmek ve gerçek kapsamı ölçmek
🤖 üretimde izinsiz sömürü denetim yetkisini aşar ve gerçek zarar üretebilir.
## Doğru
C
## Açıklama
Kritik bulgu iki zıt hatayı aynı anda davet eder: acele edip doğrulamadan bildirmek, ya da bekleyip normal takvime bırakmak. Doğru sıra bellidir. **Önce doğrula**; yanlış alarm bir kez verilirse sonraki raporların ağırlığı düşer. Sonra şiddeti gerekçesiyle sınıflandır: erişim gerekiyor mu, veri sızıyor mu, internete açık mı. Ardından **kanalı daralt**; sistem sahibi, güvenlik ekibi ve gereken yönetici seviyesi yeterlidir. Aynı görüşmede geçici azaltıcı önlem konuşulur, çünkü kalıcı düzeltme genellikle zaman ister. A şıkkı takvimi riskin önüne koyar. B şıkkı iyi niyetlidir ama kapanmamış bir açığı yayar; duyuru, açık kapandıktan sonra öğrenme amacıyla yapılır. D şıkkı en ağır hatadır: üretimde izinsiz sömürü denetçinin yetkisini aşar, veri bütünlüğünü bozabilir ve denetimin kendisini bulgu hâline getirir. Bildirimden sonraki iş takiptir: her aksiyona sahip ve tarih atanır, kapanış beyanla değil yeniden testle doğrulanır, dil kişiye değil kontrole yönelir (`189`, `195`).

# sozlu9-6 | İleri
## Soru
Bir uyum maddesi "üretim verisine yetkisiz erişim engellenmelidir" diyor. Bu maddeyi denetlenebilir kritere çevirmenin en doğru yolu hangisidir?
## A
Maddeyi olduğu gibi kontrol hedefi yazmak; ifadeyi değiştirmek uyum metnine müdahale sayılır
🤖 soyut ifade test adımı üretmez; çeviri müdahale değil, denetçinin asıl işidir.
## B
Erişimi kimin onayladığını, hangi sıklıkta gözden geçirildiğini ve kanıtın nerede saklandığını yazmak
🤖 Doğru: kriter kanıtı adreslediğinde test tekrarlanabilir olur.
## C
Sorumlu ekipten maddenin karşılandığına dair yazılı beyan alıp bu beyanı tek kanıt olarak saklamak
🤖 beyan destekleyicidir; tek başına kontrolün çalıştığını göstermez.
## D
Bir güvenlik aracının ekran görüntüsünü almak; araç açık görünüyorsa madde karşılanmış sayılır
🤖 aracın açık olması kontrolün dönem boyunca çalıştığını kanıtlamaz.
## Doğru
B
## Açıklama
Uyum metinleri bilerek soyut yazılır; denetim ise somut kanıt ister. Aradaki mesafeyi kapatmak denetçinin asıl işidir ve zincir şudur: gereksinim, kontrol hedefi, kontrol, test adımı, kanıt, sonuç. "Yetkisiz erişim engellenmeli" cümlesi test edilemez. Test edilebilir hâli şudur: üretim veritabanı erişimi yazılı onayla verilir, dönemsel olarak gözden geçirilir ve her ikisinin kaydı belirli bir yerde saklanır. Böyle yazıldığında adım uygulanabilir olur; hangi sistemden, hangi dönem için, hangi çıktının alınacağı bellidir. A şıkkı çeviriyi müdahale sanır ve kağıdı uygulanamaz bırakır. C şıkkı beyanı kanıt yerine koyar; yazılı beyan destekleyici bir unsurdur, kontrolün işlediğini göstermez. D şıkkı tek bir anlık görüntüye dayanır; aracın bugün açık olması, kontrolün dönem boyunca çalıştığını kanıtlamaz. Burada **tasarım testi** ile **işletim etkinliği testi** ayrımı da devreye girer; iyi tasarlanmış bir kontrol dönem içinde hiç çalışmamış olabilir. Son ölçüt tekrarlanabilirliktir: başka bir denetçi aynı çalışma kağıdıyla aynı sonuca varabilmelidir.
