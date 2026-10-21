---
hafta: 2
gun: 5
baslik: "Doküman bahçıvanlığı planı + quiz"
---

## Ders

### Bugün ne öğreneceksin?
Bu hafta bir bilgi sistemi kurdun: AGENTS.md haritası (Gün 1) → docs/ ağacı (Gün 2) → ADR karar tarihçesi (Gün 3) → aşamalı açığa çıkarma (Gün 4). Bugün onun **çürüme yasasını** ve panzehrini öğreneceksin: belgeler bakımsız kalınca yalnız işe yaramaz hâle gelmez — **aktif olarak zarar verir**.

### Temel kavramlar
- **Bayat belge (stale doc):** kod değişmiş ama belge eski hâlini anlatıyor. Yanlış olduğu dışarıdan belli değildir; en tehlikeli yanı budur.
- **Çekici tehlike (attractive nuisance):** hukuktan ödünç alınmış terim — çocuğu içine çeken bakımsız bir havuz gibi, güven verirken zarar veren şey. Bayat belge tam olarak budur: ajan ona **güvenir**, çünkü depoda durmaktadır ve depodaki bilgi "doğru" varsayılır.
- **Doküman bahçıvanlığı (doc-gardening):** düzenli aralıklarla belgeleri tarayıp bayatlayanları düzelten ritüel. Bahçe benzetmesi kasıtlı: tek seferlik bir iş değil, **mevsimlik bakım**.
- **Entropi:** düzenin kendiliğinden bozulması. Kimse belgeyi bozmaya çalışmaz; kod değiştikçe belge kendiliğinden yanlışlaşır.

### Somut örnek
Vaka bu dersin kendisinden: Hafta 2 / Gün 1 metni bir dönem *"bu projenin AGENTS.md'si 30 satır"* ve *"5 ADR'nin tamamı rafta durur"* diyordu. Bugün dosya **45 satır**, ADR sayısı **9**. Kimse yalan yazmadı — sayılar yazıldığı gün doğruydu. Depo büyüdü, metin yerinde kaldı.

Zararı gör: bu metni okuyan bir ajan, "AGENTS.md 30 satır" bilgisine dayanıp *"demek ki şişmemiş, bir kural daha ekleyebilirim"* diye akıl yürütebilir. Yanlış bilgi doğrudan yanlış karara dönüşür. Oysa aynı bilgi hiç yazılmasaydı, ajan `wc -l AGENTS.md` komutunu çalıştırıp **gerçeği** ölçerdi. Bayat belge burada, boşluktan daha kötü davrandı.

Buradan iki pratik kural çıkar:
- **Ölçülebilen sayıyı belgeye gömme, ölçme komutunu göm.** "45 satır" bayatlar; `wc -l AGENTS.md` bayatlamaz. Aynı mantık sürüm numaraları, dosya sayıları ve dizin listeleri için de geçerlidir.
- **Bakımı mekanikleştir.** OpenAI'ın deneyinde bunu bir ajan yapıyor: periyodik olarak belgeleri tarar, çapraz bağlantıları ve güncelliği kontrol eder, küçük düzeltme PR'ları açar. Linter'lar ve CI işleri bilgi tabanının tutarlılığını doğrular. Aynı ekip başta her Cuma'yı elle temizliğe ayırmıştı — ölçeklenmedi; sürekli, küçük ve otomatik bakım kazandı.

### Sık yapılan hata
- **"Belgeyi sonra güncellerim."** Sonra gelmez. Kural: belgeyi bozan değişiklikle **aynı** commit'te düzelt. Ayrı bir iş olarak planlanan doküman güncellemesi hiç yapılmaz.
- **Doğrulayamadığın bayat belgeyi silmek yerine "biraz düzeltmek".** Emin olmadığın bir belgeyi silmek, yanlış bırakmaktan iyidir. Boşluk dürüsttür; yanlış bilgi değildir.
- **Bahçıvanlığı gönüllülüğe bırakmak.** Takvimde bloğu yoksa ritüel değildir. Sabit 30 dakikalık haftalık tur, ayda bir yapılacak "büyük temizlik" girişiminden çok daha fazla iş görür — çünkü büyük temizlik hiç başlamaz.
- **Yalnız insan turu kurmak.** İnsan turu bulur, ajan düzeltir: taramayı ve düzeltme önerisini otomatikleştirdiğin ölçüde ritüel yaşar.

### Görevlere geçmeden önce
İlk görev bilerek küçüktür: bu haftanın tüm kazanımını koruyan şey, kurduğun yapı değil ona **dokunmayı sürdürmen**. Takvime konmayan bakım yapılmaz. İkinci görev haftanın kavramlarını ölçer — quiz'de yanlış yaptığın soru, hangi günü tekrar okuman gerektiğini söyler.

## Görevler
- [ ] Takvimine haftalık 30 dk 'doc-gardening' bloğu koy
- [ ] Haftalık quiz'i çöz
