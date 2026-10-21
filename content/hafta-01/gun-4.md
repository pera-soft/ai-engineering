---
hafta: 1
gun: 4
baslik: "Kendini-değerlendirme tuzağı"
---

## Ders

### Bugün ne öğreneceksin?
Bugün sensor'ların en kırılgan türünü tanıyacaksın: **ajanın kendi işini değerlendirmesi.** Ölçülmüş bulgu şu — ajan kendi çıktısını kendinden emin bir dille över. Bu yüzden işi YAPAN ile YARGILAYAN ayrılır; ayrım prompt'ta değil, **oturumda** yapılır.

### Temel kavramlar
- **Üretici (generator):** işi yapan ajan — kodu yazan taraf.
- **Değerlendirici (evaluator / judge):** çıktıyı eleştirel inceleyen AYRI ajan. Ayrı bir oturumda çalışması şarttır.
- **Yanlılık (bias):** rastgele değil, sistematik olarak tek yöne sapan hata. Buradaki sapma olumlu yönedir: "iyi görünüyor", "doğru çalışıyor", "testler yeterli".
- **Bağlam kirlenmesi:** aynı oturumda ajanın önünde kendi gerekçeleri durur. "Bunu neden böyle yaptım"ın cevabı zaten bağlamdadır; ajan o gerekçeleri yeniden okuyup kendini haklı çıkarır. Temiz bir oturumda o gerekçeler yoktur — yalnız **kod** vardır.

### Somut örnek
Aynı 30 satırlık fonksiyonu iki farklı biçimde sorgula:
1. **Aynı oturumda:** ajana fonksiyonu yazdır, hemen ardından "işini değerlendir" de. Tipik cevap: *"Kod temiz, hata yönetimi yerinde, testler yeterli."* Bir onay listesi ve övgü alırsın.
2. **Temiz oturumda:** yeni bir oturum aç, yalnız kodu ver ve *"bu kodu eleştirel incele, 3 somut kusur bul"* de. Tipik cevap: eksik sınır durumu, yanıltıcı isimlendirme, testin gerçekte neyi doğrulamadığı.

Fark, ikinci modelin daha zeki olmasından gelmez; ikinci oturumda **savunacak bir geçmişi olmamasından** gelir. OpenAI'ın deneyinde bu bir döngüye dönüşmüştü: kodu yazan ajan, PR'ı bitirmek için AYRI inceleyici ajanlardan geri bildirim ister, düzeltir, tekrar ister — tüm inceleyiciler tatmin olana kadar (dünkü "Ralph Wiggum döngüsü"). İnsan bu döngüye yalnız yargı gerektiğinde girer.

Önemli bir sınır: ayrı değerlendirici de sonuçta bir modeldir, o da yanılır. En güvenilir sensor hâlâ **fikri olmayan** sensor'dur — derleyici, test, ArchUnit. Değerlendirici ajan, otomatikleştiremediğin yargılar (okunabilirlik, isimlendirme, tasarım kokusu) için vardır; testin yerine değil, **yanına** konur.

### Sık yapılan hata
En yaygın yanılgı, sorunu **prompt'a bir cümle ekleyerek** çözdüğünü sanmaktır: *"Yazdıktan sonra kendi kodunu acımasızca incele."* Bu, yanlılığı bir miktar azaltır ama ortadan kaldırmaz — çünkü değerlendirmeyi yapan hâlâ kendi gerekçeleri önünde duran aynı ajandır. Ayrımı prompt'ta değil, oturumda yap.

İkinci hata, değerlendiriciye **"iyi mi?"** diye sormaktır. Evet/hayır soruları övgü davet eder. Bunun yerine sayı ve biçim dayat: *"3 somut kusur bul"*, *"bu kodun kırılacağı 2 girdi örneği ver"*, *"testin doğrulamadığı bir davranış söyle"*. Cevabın **iddia** değil **kanıt** içermesini iste.

Üçüncüsü: değerlendiriciye kodu değil, *"az önce yaptığım işi değerlendir"* diye özeti vermek. Özeti yazan da üretici ajandır; özet zaten savunmadır.

### Görevlere geçmeden önce
Aşağıdaki iki görev bu farkı sana **okutmaz, gösterir**: aynı işi bir kez aynı oturumda, bir kez temiz oturumda değerlendirteceksin. İki cevabı yan yana koyduğunda aradaki tonu kendin göreceksin — bu, bir daha unutmayacağın türden bir gözlemdir.

## Görevler
- [ ] Claude Code/Codex'te küçük bir işi yaptır, sonra AYNI oturumda 'işini değerlendir' de — cevabı gözlemle
- [ ] Aynı işi TEMİZ bir oturumda 'bu kodu eleştirel incele' diye ver — farkı not al
