---
hafta: 7
gun: 5
baslik: "Sentez + quiz"
---

## Ders

### Bugün ne öğreneceksin?
Bugün yeni kavram yok; dört günü tek bir zincire dizip **kendi cümlelerinle** anlatmayı öğreniyorsun. Bir konuyu bildiğinin tek kanıtı, onu kaynağa bakmadan yazabilmendir. Günün sonunda yarım sayfalık bir özetin ve çözülmüş bir quiz'in olacak.

### Temel kavramlar
Haftanın zinciri şu:

- **Gün 1 — AI mühendisliği nedir:** Model artık senin eğittiğin değil, kiraladığın bir bileşen. İş, "eğitmek"ten "adapte etmek + değerlendirmek"e kaydı. Üç adaptasyon yolu: prompt (kötü talimat), RAG (eksik bilgi), fine-tune (yanlış davranış).
- **Gün 2 — Model seçimi:** Kalite ↔ gecikme ↔ maliyet üçgeni. Lider tabloları eleme yapar, karar vermez; kararı kendi 20-50 örneğinden oluşan değerlendirme kümen verir. Seçim, ayar dosyasında durursa geri alınabilir kalır.
- **Gün 3 — Prompt disiplini:** Sistem promptu + görev + kısıt + biçim + örnekler. Sıcaklık tutarlılığı, bağlam penceresi ise neyin görüldüğünü belirler. Prompt koddur: dosyada durur, sürümlenir, sınanır.
- **Gün 4 — Halüsinasyon:** Uydurma bir arıza değil, "en olası devamı üret" tasarımının yan ürünü. Savunma katmanlıdır ve katman sayısı, yanlış cevabın maliyetine göre belirlenir.

Ortak iplik tek cümlede: **hazır modeli al, disiplinli adapte et, sıkı değerlendir.** Dikkat et — dördünde de tekrar eden şey ölçüm. Model seçiminde ölçüm, prompt değişikliğinde ölçüm, halüsinasyon savunmasında ölçüm. Hafta 8 bu ipliği alıp merkeze koyacak.

Özeti yazarken kendini şu dört soruyla sına: (1) LLM ne yapan bir programdır? (2) Prompt/RAG/fine-tune arasındaki karar hangi tek soruyla verilir? (3) Bir modeli neye göre seçerim? (4) Yanlış cevabın maliyeti yükselince ne değişir?

### Somut örnek
Bu haftayı bu projenin kendisi üzerinden okuyabilirsin: model seçimi ayar dosyasında ve iki kademeli, prompt'lar `prompts/` altında ayrı dosyalarda sürümleniyor, ders içeriği `content/` altından prompt'a ekleniyor, çıktılar ise serbest metin olarak kabul edilmeden ayrıştırılıyor. Yani dört günün dördü de tek bir uygulamada aynı anda çalışıyor. Özetini yazarken örneklerini buradan seçmen, kavramları havada kalmaktan kurtarır.

### Sık yapılan hata
- **Özeti kopyalayarak yazmak.** Metne bakarak yazılan özet öğrenmez; kapat, yaz, sonra karşılaştır.
- **Özeti tanımlar listesine çevirmek.** Değerli olan tanım değil, **bağlantı**: "model seçimi neden prompt disiplinini etkiler?" gibi cümleler kur.
- **Quiz'i "kaç doğru yaptım" diye çözmek.** Amaç puan değil; yanlış yaptığın soru, haftanın hangi gününe geri dönmen gerektiğini söyleyen tek işarettir.
- **Şıkkı eleyerek bulup geçmek.** Doğru cevabı işaretledikten sonra, diğer üç şıkkın **neden yanlış** olduğunu da bir cümleyle söyle.

### Görevlere geçmeden önce
Özetin şu iskeletle yarım sayfayı geçmesin: dört gün için birer cümle + haftanın tek ana fikri + kendi işinde bu hafta uygulayacağın **bir** somut değişiklik. Sonra quiz'i çöz; yanlış çıkan her soru için ilgili günün "Temel kavramlar" bölümüne dön ve o kavramı bir cümleyle yeniden yaz.

## Görevler
- [ ] Yarım sayfalık özet yaz
- [ ] Haftalık quiz'i çöz
