---
baslik: "Liderlik ve Mentorluk Şablonları (D.3–D.6)"
ozet: "Spec yazımı, AI-çağı code-review, ekip AI anlaşması ve junior mentorluk için doğrudan kopyalanıp uygulanabilir Türkçe şablonlar."
etiketler: 12
---

### Bu metin ne anlatıyor?
Bu metin okunacak bir ders değil, **kullanılacak bir alet çantasıdır**. İçinde beş hazır form
var: bir işi koda başlamadan önce yazıya dökmek için bir taslak, başkasının (ya da yapay zekânın)
yazdığı kodu incelerken tek tek işaretleyeceğin bir kontrol listesi, ekipçe "yapay zekâyı nasıl
kullanacağız" diye anlaşmak için altı maddelik bir metin, yeni bir geliştiriciyi haftalık bir
düzen içinde yetiştirmek için bir takvim ve pratik yapmak için örnek bir tasarım senaryosu.
Hepsini olduğu gibi kopyalayıp kendi işine uyarlaman beklenir.

### Bu metinde geçen temel terimler
- **Spec (spesifikasyon / şartname):** Yapılacak işin ne olduğunu, sınırlarını ve "bitti"nin
  neye benzediğini anlatan yazılı metin. Koda başlamadan önce yazılır.
- **Code review (kod incelemesi):** Bir kod değişikliğinin, ana koda katılmadan önce başka
  biri tarafından okunup değerlendirilmesi.
- **PR (pull request / birleştirme isteği):** Kod incelemesinin yapıldığı yer — değişikliğini
  ana koda katmak için açtığın, tartışmanın ve onayın üstünde yürüdüğü istek.
- **Commit:** Kod deposuna kaydedilmiş tek bir değişiklik paketi. "Commit eden kişi", o
  değişikliği depoya gönderen kişidir.
- **Ajan (agent):** Kendisine verilen hedefe ulaşmak için kendi kendine adım atabilen —
  dosya okuyup yazabilen, komut çalıştırabilen — yapay zekâ programı.
- **Sandbox (kum havuzu):** Bir programın yalnızca kendisine açılmış dar bir alanda
  çalışmasını sağlayan yalıtım düzeni; dışına çıkmaya çalışırsa engellenir.
- **AGENTS.md / CLAUDE.md:** Ajanın işe başlarken okuduğu proje talimat dosyası. Ajan okur ve
  genellikle uyar; ancak bu bir rica olduğu için uyacağının garantisi yoktur.

## Neden bu şablonlar
Bu hafta liderlik (leadership) ve mentorluk pratiğini kuruyorsun. Aşağıdaki dört şablon,
günlük görevlerinde adı geçen araçların TAM içeriğidir: bir iş problemini yazılı
spesifikasyona (spec) dökmek, AI'ın ürettiği kodu güven altında incelemek, ekipçe bir
AI kullanım anlaşması yapmak ve junior'ı üretken zorlanma (productive struggle — kişinin
kendi başına çabalarken öğrendiği, çok kolay da çok zor da olmayan zorlanma bandı) içinde
tutan haftalık bir ritim kurmak. Dördünün ardından, öğrendiklerini denemek için örnek bir
tasarım senaryosu (kata) geliyor. Her birini olduğu gibi kopyala, kendi ekibine ve
projenin gerçekliğine uyarla.

> Bu şablonların **resmi** (beceri körelmesi, mentorluk ritmi, otonomi sınırı — üç diyagram)
> `hafta12-gorsel-ozet` kaynağında. Önce oraya bakıp bütünü görmek, sonra buradan kopyalamak
> daha kolay gelir.

---

## 1. Spesifikasyon (Spec) Şablonu
Gerçek bir iş problemini, ajana (veya insana) verilebilecek net bir yazıya çevirmek için.
Spec, "ne istediğini bilmeden koda başlama" disiplininin somut halidir. Aşağıdaki
bölümleri madde madde doldur.

**Başlık:** (Tek cümlede ne yapılacak?)

**1. Bağlam (context) — neden şimdi?**
- Hangi gerçek problem/acı var? Kim etkileniyor?
- Bugün nasıl çözülüyor (varsa) ve neden yetersiz?

**2. Hedef (goal) — "bitti" neye benziyor?**
- Başarı ölçülebilir tek cümleyle: kullanıcı/işletme için hangi sonuç doğar?
- Çözüm sızıntısı testi: hedef cümlesi bir **biçim** mi dayatıyor ("haftalık AI özeti üret"), yoksa
  gözlemlenebilir bir **sonuç** mu söylüyor ("ekip lideri pazartesi 5 dakikada riskli PR'ları görür")?
  Biçim dayatıyorsa onu çözüm alanına taşı, hedefe sonucu yaz.

**3. Kapsam-içi (in scope)**
- [ ] Bu iş kesinlikle şunları KAPSAR: …

**4. Kapsam-dışı (out of scope)**
- [ ] Bu iş bilinçli olarak şunları KAPSAMAZ: … (kapsam kaymasını / scope creep önler)

**5. Kabul kriterleri (acceptance criteria)**
- [ ] Doğrulanabilir madde: "… olduğunda … görünür/olur."
- [ ] Her kriter test edilebilir olmalı (gözlemlenebilir çıktı).
- [ ] **Kanıt, iddiayla aynı katmanı test eder.** Birim testi yerel bir fonksiyon sözleşmesini,
  HTTP testi serileştirme + taşımayı, tarayıcı yolculuğu arayüz yolunu, denetim kaydı (audit log)
  yetki sınırını kanıtlar. Alt katmanın kanıtını üst katmanın iddiası için kabul etme ("servis testi
  geçti" ≠ "kullanıcı ekranda doğru mesajı görüyor"). "Testler geçiyor" bir kanıt planı değildir —
  yetkili testi ve desteklediği iddiayı adıyla yaz.
- [ ] Örnek seti: en az bir **normal**, bir **sınır** (edge), bir **hata** ve bir **yasak** durum
  örneği. Örnekler niyeti sıfatlardan ("sağlam", "kullanıcı dostu") iyi taşır — ama bir değişmezin
  (invariant) yerini tutmaz: tek geçen örnek evrensel bir güvenlik kuralını kanıtlamaz.

**6. Riskler ve varsayımlar (risks & assumptions)**
- Varsayım: "… doğru kabul ediyoruz." (yanlışsa iş çöker mi?)
- Risk: "… olursa ne yaparız?" (özellikle geri dönüşü zor olanlar)
- **Geri alınamaz adım kuralı:** planda geri alınamaz ilk adım (şema göçü, veri silme, dış sözleşme),
  bağlı olduğu belirsizlik çözülmeden **önce** geliyorsa plan reddedilir. Sıra: önce belirsizliği
  ucuz ve geri alınabilir bir adımla çöz, sonra geri dönüşü olmayana geç.

**7. Bağımlılıklar ve açık sorular**
- Neyi/kimi bekliyoruz? Cevabı olmayan hangi soru işi bloke eder?
- Her bilinmeyeni **dört türden birine** ayır (bilinmeyen kontrollü bir boşluktur; varsayım ise o
  boşluğa verilmiş kontrolsüz bir cevap):

  | Tür | Anlamı | Ajan ne yapar? |
  |---|---|---|
  | Keşfedilebilir | depo ya da çalışan sistem cevaplayabilir | araştırır, devam eder |
  | Karar verilebilir | sözleşme ajana seçme yetkisi veriyor | seçer, gerekçesini yazar, devam eder |
  | İnsan | ürün davranışını, maliyeti, riski ya da dış uyumluluğu değiştirir | **durur, sorar** |
  | Ertelenebilir | bu dilimin dışında | kapsam-dışına (alan 4) yazar |

  Ajan yalnız **"insan"** türünde durur — seçim koda gömülmeden önce.

**8. Repo gerçekleri — kanıtla (receipts)**
- Spec'teki her "depo böyle çalışıyor" cümlesi bir **makbuzla** gelir: `dosya:satır` ya da komut
  çıktısı. Örnek: "AI çağrısı yalnız `ai/` paketinden yapılabilir —
  `src/test/java/tr/velzon/aiogretmen/MimariTestleri.java:58` (`claude_yalniz_ai_paketinden`)."
  Makbuzu olmayan cümle gerçek değil, varsayımdır → alan 6'ya (riskler ve varsayımlar) taşı.
- Keşif (reconnaissance) bütün depoyu okumak değildir; değişikliği **kısıtlayan** yüzeyleri arar:
  mevcut davranış ve çağıranı, en yakın test, dışa açık sözleşme/serileştirilmiş biçim, o yolu
  yöneten talimat (AGENTS.md), derleme/doğrulama komutu, benzer bitmiş bir değişiklik. Her planlı
  karar ya kanıtla desteklendiğinde, ya açıkça devredildiğinde ya da bilinmeyen olarak listelendiğinde
  dur — ötesi çoğu zaman kaçınmadır.

**9. Karar politikası — her karar işaretli**
Her önemli karar üç moddan biriyle, **gerekçesiyle** işaretlenir (gerekçe, sonraki ekibin kararı
arkeoloji yapmadan değiştirebilmesi içindir):

| Mod | Ne zaman | Bu projeden örnek |
|---|---|---|
| **Kilitli** (locked) — ajan seçemez | dış uyumluluk, yetki, güvenlik, geri alınamaz maliyet, ürün taahhüdü | uygulama yalnız 127.0.0.1'e bağlanır (CLAUDE.md NEVER) |
| **Sınırlı** (bounded) — sınır içinde seçer | arama/yeniden deneme bütçesi, izinli bağımlılıklar, bilinen arayüz ailesi | yalnız mevcut bağımlılıklarla; yenisi ADR ister (ASK FIRST) |
| **Ajana bırakılmış** (delegated) — seçer ve açıklar | yerel yapı, isimler, geri alınabilir refactor | servis içi yardımcı metodun adı ve bölünmesi |

İki uç da kötüdür: eksik tanımlı spec ajana sistemi **tahmin** ettirir; aşırı tanımlı spec ona
yanlış olabilecek bir tasarımı **dikte** ettirir. Ucuz ve geri alınabilir yerde yargıyı ajana
bırak; sonucu ağır ya da dışa açık yerde insan kontrol noktası koy.

**10. Ölçüm planı — sonucu görmeden ÖNCE yazılır**
- **Geçti / kaldı / belirsiz** yolları ölçümden önce yazılır; yoksa ekip eşiği, inşa ettiğini
  korumak için kaydırır. Örnek: *geçti* = doğru tanı oranı ≥ 0,9 ve ortanca süre ≤ 120 sn;
  *kaldı* = herhangi bir üretim yazması ya da doğru oranı < 0,75; *belirsiz* = küçük iyileşme, geniş
  sapma → daha büyük tekrar seti.
- Her metriğin sözleşmesi: ad, yön (en az / en çok), eşik, pencere, kaynak, nüfus. Kaynağı ve
  penceresi olmayan sayı tekrar üretilemez; eşiği olmayan sayı karar sürdüremez.
- **Sonuç metriği** (istenen durum iyileşti mi?) + **koruyucu metrik** (guardrail — sabit bir kısıt
  hâlâ doğru mu? ör. tanı salt-okur kaldı mı?) + **karşı metrik** (counter-metric — yerel iyileşme
  maliyeti başka yere mi itti? ör. nöbetçinin iş yükü, kaçan alarm).
- **Durma kuralı:** "her sonuç 'devam et' diyorsa bu bir deney değildir." En küçük test edilebilir
  dilimi seç ve sahte minimumlardan kaçın — yalnız arayüz, yalnız mutlu yol, yalnız demo dilimi
  asıl riski test etmez.

> İpucu: Spec bittiğinde kendine sor — "Bunu hiç konuşmadığım biri okusa, benimle aynı
> şeyi mi inşa eder?" Cevap "hayır" ise henüz spec hazır değil.

---

## 2. AI-Çağı Code-Review Kontrol Listesi
Review'un merkez sorusu değişti: "neden böyle yazdın?" değil, **"AI ne uyguladı,
anlıyor musun?"** Amaç üretimi değil kavrayışı ve doğrulamayı denetlemek. Bu listeyi
PR şablonuna (GitHub PR template) göm ve zorunlu kıl.

**Kavrayış (bunu commit eden kişi doldurur)**
- [ ] Değişikliğin ne yaptığını satır satır AÇIKLAYABİLİYORUM (AI yazdı diye atlamıyorum).
- [ ] Neden bu yaklaşımın seçildiğini ve ELenmiş bir alternatifi söyleyebiliyorum.
- [ ] Kodda anlamadığım tek bir satır YOK; varsa işaretledim ve sordum.

**Doğruluk ve kapsam**
- [ ] Değişiklik spec'teki kabul kriterlerini karşılıyor (fazlasını değil).
- [ ] Yeni davranış için test var; test gerçekten davranışı doğruluyor (yeşil olsun diye değil).
- [ ] Sınır (edge) durumları düşünüldü: boş girdi, hata yolu, eşzamanlılık.

**Güvenlik (security)**
- [ ] Dış girdi sınırda doğrulanıyor (parse/validate); "YOLO usulü" veri şekli yoklaması yok.
- [ ] Sır (secret)/API anahtarı koda veya log'a sızmıyor.
- [ ] Yetki/erişim kontrolü atlanmamış; kullanıcı verisi gereksiz yere açığa çıkmıyor.
- [ ] Enjeksiyon (injection) yüzeyleri (SQL, komut, prompt) ele alındı.

**Okunurluk ve borç**
- [ ] Değişiklik projenin katman yönüne ve kalıplarına uyuyor.
- [ ] Yeni teknik borç bilinçli ve not edilmiş (gizli değil).

### İNSAN ONAYINA SABİTLE
Aşağıdaki işlemler **geri dönüşü zordur**; ajan otonomisinin dışında tutulur. AGENTS.md
"ihtimali azaltır", ama asıl korumayı **politika + sandbox** verir: bunlar bir insanın
açık onayı olmadan birleştirilemez.
- [ ] **Şema göçü (schema migration):** veri kaybı/kilitlenme riski; geri alma planı yazılı mı?
- [ ] **Dış sözleşme / API değişikliği:** başka ekipler/istemciler kırılır mı? Sürümleme var mı?
- [ ] **Veri silme (destructive data op):** yedek ve geri dönüş (rollback) yolu doğrulandı mı?

---

## 3. Ekip AI Kullanım Anlaşması Şablonu
Bu bir DAYATMA değil, ekiple BİRLİKTE gözden geçirilip imzalanan bir anlaşmadır.
Altı başlığı ekip toplantısında tartış, kendi diline çevir, v1'i yayınla.

**1. Sorumluluk (accountability) — "commit eden sahiptir"**
- Kodu gönderen kişi, AI üretmiş olsa bile tüm sorumluluğu taşır. Sorumluluk AI'a
  devredilemez. Bu kural hem kaliteyi hem junior'ın kavrama zorunluluğunu korur.

**2. Doğrulama zinciri (verification chain)**
- AI çıktısı doğrulanmadan "bitti" sayılmaz: test + gözden geçirme + (gerekirse) elle
  çalıştırıp gözlemleme. "Yeşil çıktı" tek başına doğrulama değildir.

**3. Şeffaflık (transparency)**
- Önemli kararların ve AI'ın belirgin katkısının izi repoda kalır (PR açıklaması, ADR).
  "İnsanların kafasındaki bilgi sistemde yok demektir."

**4. Prompt / skill kütüphanesi**
- Tekrar eden iyi prompt'lar ve beceriler ortak bir dizine yazılır (`.claude/commands`
  veya `.agents/skills`), versiyonlanır; herkes aynı kaliteli girdiden yararlanır.

**5. Bağlam hijyeni (context hygiene)**
- Göreve yalnız ilgili bağlamı ver; alakasız dosyaları/geçmişi taşıma. Uzun oturumlarda
  bağlamı temizle; açık kimlik/kapsamla çalış. Kirli bağlam = kararsız sonuç.

**6. KVKK / gizlilik (privacy)**
- Kişisel veri, müşteri verisi ve sırlar AI araçlarına gönderilmez; gerekiyorsa
  maskelenir/anonimleştirilir. Veri işleme kurallarına (KVKK) uyum herkesin görevidir.

---

## 4. Haftalık Junior Mentorluk Akışı
Amaç: junior'ı AI ile hızlandırırken üretken zorlanma (productive struggle) içinde
tutmak — yani zoru AI'a tam delege ederek kavramayı köreltmesini engellemek. Aşağıdaki
ritmi kendi ekibinin takvimine uyarla.

| Gün | Ritüel | Neden |
|-----|--------|-------|
| Pazartesi | **Haftalık hedef + spec:** junior küçük bir işi kendisi spec'e döker, mentor gözden geçirir | Net niyet olmadan koda başlamamayı öğretir |
| Salı | **Conceptual Inquiry (kavramsal sorgulama) günü:** junior AI'ı "kod yaz" için değil, "bu neden böyle / alternatifi ne" diye SORU sormak için kullanır | Ölçülen en yüksek kavrama bu desende (≥%65)\* |
| Çarşamba | **AI'sız gün (yarım gün yeter):** küçük bir görevi AI'sız çöz | Temel kasların körelmemesi için |
| Perşembe | **Review'da soru sorma:** mentor junior'ın PR'ında "burayı bana anlat" der; cevap üretimi değil kavrayışı yoklar | Review = mentorluğun ana sahnesi |
| Cuma | **Retrospektif:** "bu hafta AI olmadan yapamayacağın ne öğrendin?" | Öğrenmeyi görünür kılar, bağımlılığı fark ettirir |

\* **Şerh (sayılara karşı):** %65 ve %40 rakamları `hafta12-gorsel-ozet`te anlatılan **tek bir
çalışmadan** gelir; belirli bir katılımcı grubunda, belirli görevlerle ölçülmüştür. Doğa yasası
değil, bir işarettir. Ritmi bu sayıları hedeflemek için değil, arkasındaki bulgu için kur:
işi tümüyle devretmek işi bitirir ama kavramayı bitirmez. Ekibinde takip edeceğin gösterge
rakam değil, gözlem olsun — junior kendi gönderdiği kodu anlatabiliyor mu?

> İpucu: Junior takılınca hemen cevabı verme; önce "sence neden olmuyor, ilk nereye
> bakarsın?" diye sor. Zorlanma anı öğrenmenin gerçekleştiği andır.

> Hazır başlangıç malzemesi: Junior'ın (ya da ekipteki teknik olmayan birinin) Claude'la HİÇ
> tanışıklığı yoksa, sıfırdan başlayan Türkçe bir rehber var — bahadir.digital "Yeni Başlayanlar
> İçin Claude" (17 sayfa, interaktif; kurulumdan prompt temellerine). Sen mimari tarafı öğret,
> tanışma turunu bu rehbere bırak — mentorluk saatini temellere harcama.

---

## 5. Örnek Mimari Kata (RFP)
Gün 4'ün kata seansına doğrudan girdi. **Kata**, dövüş sanatlarından ödünç alınmış bir terimdir:
gerçek bir iş değil, aynı hareketi tekrar ederek beceri kazandıran bir alıştırma. Mimari kata
şöyle işler: 3–5 kişi, bir **RFP** (Request for Proposal — bir müşterinin "şöyle bir sistem
istiyorum" diye anlattığı talep senaryosu), moderatöre sorular, taslak mimari, kısa sunum +
savunma. Aşağıdaki senaryoyla başla; ısındıktan sonra kendi bağlamından benzer bir senaryo üret.

**RFP — "Mahalle Fırını Zinciri Sipariş Sistemi"**
Şehirde 40 şubesi olan bir fırın zinciri, telefonla alınan siparişleri bırakıp mobil
uygulama + web üzerinden sipariş almak istiyor. Sabah 07:00–09:00 arası yoğun bir tepe
(peak) var; müşteriler "siparişim hazır mı" bildirimini gerçek zamanlı görmek istiyor.
Şubelerin bazılarında internet zayıf. Ödeme üçüncü bir sağlayıcıyla yapılacak. Ekip
küçük; ilk sürüm 3 ayda istenmiş.

**Moderatöre sorulacak örnek sorular** (kata'nın kalbi — belirsizliği daralt):
- Tepe saatte eşzamanlı sipariş büyüklüğü ne (yüzler mi, binler mi)? **p99** hedefi var mı?
  (p99 = isteklerin %99'unun altında kaldığı süre; "en yavaş %1 hariç herkes şu kadar bekler".
  Ortalama süre yanıltıcıdır, çünkü birkaç çok yavaş isteği gizler.)
- "Gerçek zamanlı bildirim" ne kadar gecikmeye izin veriyor (saniye mi, dakika mı)?
- Zayıf internetli şubede sipariş offline alınıp sonra mı senkronlanmalı?
- Ödeme sağlayıcı çökerse sipariş yine de alınsın mı (bekleyen ödeme)?
- İlk sürümde 40 şubenin hepsi mi, pilot birkaç şube mi?

**Beklenen çıktı:** Bir **C4** Context+Container taslağı (C4, sistemi dört yakınlık düzeyinde
çizen bir diyagram yöntemi; ilk iki düzey yeter — *Context*: sistem dış dünyada kimlerle
konuşuyor, *Container*: sistemin içinde hangi ayrı çalışan parçalar var), 2–3 kilit trade-off
(ör. offline sipariş ↔ tutarlılık; anlık bildirim ↔ altyapı maliyeti) ve kararların GEREKÇELİ
savunması.

> İpucu: "Doğru" mimari yok; belirsizliği soruyla daraltıp kararını SAVUNABİLMEK var.
> Bir varsayımı açıkça yazıp üstüne karar kurmak, sessizce varsaymaktan iyidir.

> **Kutu — Hafta 12 bitirme projesi fikirleri** (her biri §1 spec şablonuyla başlar; AI Engineering
> from Scratch Faz 19 bitirme projelerinden uyarlandı)
> - **Kod göçü ajanı (en uygun seçim):** Java 8/11 → 17/21 ya da Spring Boot 2 → 3. Önce
>   **deterministik** dönüşüm (OpenRewrite tarifleri), ajan yalnız **kalan** kısımda; kapsama düşerse
>   kırmızı olan bir kapı ve bütçe tavanı. Asıl teslim: göçün **hata taksonomisi** — hangi tür kırılma
>   tarifle, hangisi ajanla, hangisi insanla çözüldü.
> - **AI Öğretmen'in kendi etkinlik çalışması:** bir haftalık modülü, uyarlanmayan bir tabana
>   (ör. aynı içeriğin düz metni) karşı küçük bir **ön test / son test** ile ölç. Kavrama ölçülür,
>   memnuniyet değil — [`ai-verimlilik-olcumu.md`](ai-verimlilik-olcumu.md)'nün deney refleksiyle.
> - **Salt-okur teşhis ajanı:** bir Spring uygulamasının log ve metriklerinden tanı koyar; varsayılan
>   salt-okurdur, düzeltici araçlar ajanın **hiç tutmadığı** bir onay jetonunun arkasındadır; denetim
>   kaydı yalnız çalıştırılan değil **değerlendirilen** komutları da yazar (kıl payı kaçanları gösterir).
> - **Etiketten PR'a ajan:** kendi deponda bir issue etiketi → PR. Kısa ömürlü, dar yetkili jeton;
>   "main'e push yok / force-push yok" uygulama izniyle değil **dal korumasıyla** zorlanır; jeton
>   izinleri yol bazlı olmadığından "`.github/workflows`'a yazma yok" diff üzerinde kontrol edilir;
>   depo/gün başına bütçe.

---

## Öz-sınav (kendini test et)
1. Spec'te "kapsam-dışı (out of scope)" bölümünü ayrı yazmak neyi önler? Bu bölümü boş bırakılmış
   bir spec, iş ilerledikçe nasıl bozulur?
2. Bir spec'i "hazır" saymadan önce kendine sorman gereken tek soru neydi? Bu soru neyi test eder —
   yazının uzunluğunu mu, açıklığını mı?
3. AI-çağı code-review'da merkez soru "neden böyle yazdın?" değil de ne oldu? Bu yeni soru neyi
   denetler, ve incelemenin sonucunu nasıl değiştirir?
4. Şema göçü, dış sözleşme değişikliği ve veri silme neden `AGENTS.md` ile değil de politika/sandbox
   ile korunmalı? Bu üç işlemin ortak özelliği nedir?
5. Ekip AI kullanım anlaşmasındaki "commit eden sahiptir" kuralı kaldırılsa ne olurdu? Bu kural
   kaliteden başka neyi daha korur?
6. Haftalık mentorluk akışındaki "AI'sız gün" hangi riski hedefler, ve bu riskin varlığını ekipte
   neye bakarak anlarsın?
7. Mimari kata'da "moderatöre soru sormak" neden kata'nın kalbi sayılıyor? Hiç soru sormadan
   doğrudan çizime başlayan bir ekip neyi kaçırır?
8. Spec'indeki bir bilinmeyen "giriş e-postası büyük/küçük harf duyarlı mı?". Bu dört türden hangisi
   olabilir, ajan hangisinde durur? Aynı spec'te "uygulama 127.0.0.1'e bağlanır" kararı hangi moddadır
   (kilitli / sınırlı / ajana bırakılmış) — neden?
9. Hedefin "tanı süresini kısalt". Ölçümden önce hangi üç yolu (geçti/kaldı/belirsiz) yazarsın, ve
   hangi koruyucu + karşı metrik "hızlı ama güvensiz" bir sonucu yakalar? Bir servis birim testi,
   "kullanıcı ekranda doğru hata mesajını görüyor" iddiasının kanıtı olur mu?
10. Bitirme projesi olarak kod göçü ajanında neden önce OpenRewrite, sonra ajan? Asıl teslimin neden
    "göç tamamlandı" değil de bir hata taksonomisi?

### Cevap ipuçları (8–10)
- **8:** Kod/test mevcut davranışı gösteriyorsa **keşfedilebilir**; spec seçimi ajana bırakmışsa **karar
  verilebilir**; değişiklik mevcut kullanıcıların girişini etkileyecekse (dışa açık davranış) **insan** —
  ajan yalnız burada durur. 127.0.0.1 **kilitli**dir: güvenlik/yetki sınırı, CLAUDE.md NEVER.
- **9:** Eşikli geçti/kaldı/belirsiz yolları sonucu görmeden yaz; koruyucu = tanı salt-okur kaldı mı
  (üretim yazması sıfır), karşı = nöbetçi iş yükü, kaçan alarm. Hayır: birim testi alt katmandır;
  iddia arayüz katmanındadır, kanıt da orada olmalı.
- **10:** Deterministik dönüşüm ucuz, tekrarlanabilir ve denetlenebilir; ajan yalnız kalan belirsiz işe
  harcanır. Taksonomi tekrar kullanılabilir bilgidir: bir sonraki göçte hangi kırılma türünün tarife,
  hangisinin insana gittiğini söyler.

## Kaynak ve lisans
- §1 spec şablonundaki eklemeler (2, 5, 6, 7. alanlara eklenen maddeler; yeni 8–10. alanlar) ve §5
  sonundaki bitirme projesi kutusu: **AI
  Engineering from Scratch — Rohit Ghumare** (github.com/rohitg00/ai-engineering-from-scratch, MIT
  lisansı, "Copyright (c) 2026 Rohit Ghumare"). Dersler: Faz 14 (agent engineering) 43
  frame-the-task-before-code, 44 plan-from-evidence, 47 outcomes-before-output, 50
  choose-the-smallest-testable-slice, 51 write-specifications-that-preserve-judgment, 52
  design-success-metrics; Faz 19 bitirme projeleri 06 (DevOps teşhis ajanı), 09 (kod göçü ajanı),
  16 (issue'dan PR'a ajan), 17 (kişisel AI öğretmen). *Uyarlama notu: çeviri değil sentezdir; ders
  kodları alınmadı, örnekler bu projenin dosyalarıyla değiştirildi. §10'daki ölçüm örneği dersin
  kendi örnek eşikleridir — bir hedef değil, biçim örneğidir.*
