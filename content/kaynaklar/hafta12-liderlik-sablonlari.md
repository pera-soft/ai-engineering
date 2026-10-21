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

**3. Kapsam-içi (in scope)**
- [ ] Bu iş kesinlikle şunları KAPSAR: …

**4. Kapsam-dışı (out of scope)**
- [ ] Bu iş bilinçli olarak şunları KAPSAMAZ: … (kapsam kaymasını / scope creep önler)

**5. Kabul kriterleri (acceptance criteria)**
- [ ] Doğrulanabilir madde: "… olduğunda … görünür/olur."
- [ ] Her kriter test edilebilir olmalı (gözlemlenebilir çıktı).

**6. Riskler ve varsayımlar (risks & assumptions)**
- Varsayım: "… doğru kabul ediyoruz." (yanlışsa iş çöker mi?)
- Risk: "… olursa ne yaparız?" (özellikle geri dönüşü zor olanlar)

**7. Bağımlılıklar ve açık sorular**
- Neyi/kimi bekliyoruz? Cevabı olmayan hangi soru işi bloke eder?

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
