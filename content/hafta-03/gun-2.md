---
hafta: 3
gun: 2
baslik: "Plan modu ve ExecPlan"
---

## Ders

### Bugün ne öğreneceksin?
Dün tek bir görevin tarifini yazdın. Bugün **birkaç güne yayılan** işlerde tarifin yetmediğini ve devreye **planın** girdiğini göreceksin: ajan önce planı yazar, sen onaylarsın, uygulama ondan sonra başlar. Amaç şu: oturum kapansa, bilgisayar kapansa, sen tatile çıksan bile iş kaldığı yerden devam edebilsin.

### Temel kavramlar
- **Plan modu (plan mode):** ajanın kod yazmadan önce yalnız araştırıp bir yol haritası önerdiği çalışma kipi. Claude Code'da `shift+tab` ile açılır. Kritik faydası: **yanlış yolu, kod yazılmadan** yakalarsın; düzeltmenin maliyeti bir cümledir, bir PR değil.
- **ExecPlan (yürütme planı):** işin adımlarını, durumunu ve alınan kararları tutan markdown dosyası. Bu projede yeri: `docs/exec-plans/active/`.
- **Yaşayan belge (living document):** bir kez yazılıp unutulan değil, iş ilerledikçe güncellenen belge. Adım bitince plandaki durumu da değişir.
- **Karar notu:** "bunu neden böyle yaptık" cevabı. Planın en kolay atlanan, en değerli parçasıdır.
- **Amnezi testi:** planı hiçbir şey hatırlamayan yeni bir ajana verme denemesi. Kaldığın yerden devam edebiliyorsa plan iyidir.

Altın kural tek cümledir: **yalnızca ExecPlan'e bakarak işi baştan başlatmak mümkün olmalı.** Benzetme: ExecPlan bir **yemek tarifidir**, sohbet geçmişi değil. Tarif kâğıda yazılıysa mutfağa başka bir aşçı girse bile yemek biter. "Dün ne konuşmuştuk?" ile çalışıyorsan, aşçı değişince yemek de biter — ama başka anlamda.

### Somut örnek
Dünkü "ilerleme yüzdesi" görevinin planı, `docs/exec-plans/active/ilerleme-yuzdesi.md`:

- **Durum:** Adım 2'ye kadar tamam, Adım 3 sırada. *(Bu satır her adım sonunda güncellenir — "yaşayan belge" bu demek.)*
- **Adım 1 (BİTTİ):** `service/IlerlemeService`'e `yuzde(hafta, gun)` hesabı ve birim testi eklendi. Doğrulama: `./mvnw test` yeşil.
- **Adım 2 (BİTTİ):** `web/ProgramController` yanıtına `yuzde` alanı eklendi, mevcut alanlar korundu. Doğrulama: `./mvnw verify` yeşil.
- **Adım 3 (SIRADA):** `static/app.js` içindeki gün kartına `3/5 görev (%60)` metnini bas.
- **Karar notu:** Yüzde web'de değil service'te hesaplanıyor. Sebep: katman kuralı, ArchUnit `MimariTestleri` bunu zorluyor.

Bu dosya varken oturum çökse bile kayıp yoktur: yeni ajan dosyayı okur, **Adım 3'ten** devam eder, Adım 1-2'yi tekrar yapmaz ve karar notu sayesinde hesabı yanlışlıkla web katmanına taşımaz. Bu projede aynı işi oturum boyu taşıyan ikinci bir yer daha var: `thoughts/ledgers/` altındaki oturum defterleri. Plan "ne yapılacak"ı, defter "şu an neredeyiz"i tutar.

### Sık yapılan hata
En yaygın hata, planı **yalnız adım listesi** sanmaktır. Adımlar yazılır, gerekçeler sohbette bırakılır. Sonuç: yeni oturum adımları uygular ama *neden* öyle yapıldığını bilmediği için ilk fırsatta bozar — yüzdeyi "daha kolay" diye web katmanına taşır, mimari test kırmızı yanar, iş başa döner. İkinci sık hata, planı yazıp **güncellememektir**: durumu eski kalan plan, güvenilmez olduğu için okunmaz hâle gelir ve kimse bakmadığı bir dosyaya dönüşür.

### Görevlere geçmeden önce
Ölçüt basit: **planı okuyan biri sana tek soru sormadan devam edebiliyorsa** plan iyidir. "Bunu neden böyle yapmıştık?" sorusu doğuyorsa, o cevabı plana yaz. Bugün önce plan modunu küçük bir işte dene (kod yazdırmadan, yalnız planı oku), sonra orta boy bir iş için `docs/exec-plans/active/` altına planını yaz.

## Görevler
- [ ] Claude Code'da plan modunu bir görevde kullan (shift+tab)
- [ ] Orta boy bir iş için docs/exec-plans/active/ altına ExecPlan yaz
