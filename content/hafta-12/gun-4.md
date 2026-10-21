---
hafta: 12
gun: 4
baslik: "Mimari kata günü"
---

## Ders

### Bugün ne öğreneceksin?
Mimari düşünme kasını en hızlı büyüten egzersizi: **kata**. Bugün eksik bilgiyle karar vermeyi, verdiğin kararı **savunmayı** ve bir mimariyi yapılandırılmış biçimde değerlendirmeyi öğreneceksin. Kod yazmayacaksın — bu kasıtlıdır.

### Temel kavramlar
- **Kata:** Dövüş sanatlarından gelen bir kelime: **tekrarlanan alıştırma**. Yazılımda, gerçek olmayan bir problem üzerinde gerçek karar pratiği yapmak demektir. Riski sıfırdır, öğrenmesi yüksektir.
- **RFP (Request for Proposal — teklif talebi):** Hayali bir müşterinin "şöyle bir sistem istiyorum" metni. Kasten **eksiktir**; mimarın ilk işi kod tasarlamak değil, **eksik olanı sormaktır**.
- **Moderatör:** Müşteriyi canlandıran kişi. Sorulmayan hiçbir şeyi söylemez. Buradaki ders şudur: gerçek hayatta da kimse sana bilmediğin kısıtı kendiliğinden anlatmaz.
- **Trade-off (takas):** "En iyi mimari" yoktur; her seçim bir şeyi kazanıp başka bir şeyi kaybeder (Hafta 5'in dili). Kata'nın notu, seçtiğin mimariye değil, **takası anlatabilmene** verilir.
- **Savunma:** Kata tek atışla bitmez; teslim, çapraz sorguyla biter ("neden mikroservis? tek servis olsa ne kaybederdin? bu karar geri alınabilir mi?"). Dünya standardı böyledir: O'Reilly katası çok turlu ve jüri geri beslemelidir; iSAQB CPSA-A sınavı yazılı vaka + **sözlü savunma** ister.
- **ATAM-lite:** Bir mimariyi değerlendirmenin kısa yolu. **Fayda ağacı** (utility tree): kalite hedeflerini önem ve zorluk sırasına dizmek. Sonra **senaryolar** ("yük 10 katına çıkarsa?"), **riskler** ve **hassas noktalar** (küçük bir değişikliğin çok şeyi bozduğu yerler).
- **C4:** Mimariyi dört zoom seviyesinde çizme yöntemi (Hafta 6). Kata çıktısını Context + Container seviyesinde çizmek yeterlidir.

### Somut örnek
RFP: *"Ülke genelinde 400 şubeli bir eczane zinciri için stok ve reçete sistemi istiyoruz."* Acemi refleksi hemen kutu çizmektir. Mimarın refleksi **sormaktır**: Şube internetsiz kalırsa satış durmalı mı, yoksa çevrimdışı devam edip sonra eşitlenmeli mi? Reçete verisi ne kadar süre saklanacak, kim erişecek? Aynı ilacın stoğu iki şubeden aynı anda düşerse hangisi kazanır?

Bu üçüncü soru bir tuzaktır ve **Hafta 11'e** açılır: çevrimdışı çalışma istiyorsan çok-liderli (multi-leader) replikasyona ve **yazma çakışması** çözümüne mecbur kalırsın; istemiyorsan tek liderle sadeleşirsin ama şube internetsizken satış yapamaz. İşte savunulacak takas budur — ve cevabı teknik değil, **iş kararıdır**.

Yalnız çalışıyorsan **solo kata protokolü**: AI öğretmen önce Moderatör'ü (müşteri) oynar, sonra çapraz sorgucuyu. Beş adım: gereksinim sorgusu → taslak tasarım → AI'a savunma → ATAM-lite (fayda ağacı + riskler) → C4 çizimi. Ayrıntı "Mimari Değerlendirme ve Kata Pratiği" kaynağındadır.

### Sık yapılan hata
Üç tuzak: (1) **Soru sormadan çizmeye başlamak** — kata'nın asıl sınavı sorulardır, çizim sonrasıdır. (2) **Tek atışta bitirmek** — kata seri hâlinde yapılır; ikinci turda aynı RFP'yi farklı bir kısıtla ("bütçe yarıya indi") çözmek en öğretici andır. (3) **Modaya oynamak** — "mikroservis + Kafka" demek kolaydır; 400 şubelik bir eczanenin operasyon ekibi olmadan bu mimari bir yük olur. Savunamadığın mimari, yanlış mimaridir.

### Görevlere geçmeden önce
Kata'ya girerken tek hedef koy: **kararını değil, kararının gerekçesini savunmak.** Bir junior'a en hızlı öğreteceğin şey doğru cevap değil, doğru soruların sırasıdır.

## Görevler
- [ ] Solo kata protokolüyle (kaynaktaki 5 adım) bugünkü RFP'yi uçtan uca çöz: gereksinim sorgusu → tasarım → AI'a savun → ATAM-lite (fayda ağacı + riskler)
- [ ] Ekiple 90 dk'lık ilk kata seansını planla ve yap
- [ ] Çıkan mimariyi C4 Context+Container ile çizdir (juniorlara)
