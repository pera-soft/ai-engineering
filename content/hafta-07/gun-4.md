---
hafta: 7
gun: 4
baslik: "Halüsinasyon ve savunma hatları"
---

## Ders

### Bugün ne öğreneceksin?
Bugün haftanın en tehlikeli konusu: model **emin bir üslupla yanlış** söylediğinde ne olur. Günün sonunda halüsinasyonun neden bir "arıza" değil, modelin çalışma biçiminin doğal sonucu olduğunu anlayacak; riske göre kaç savunma katmanı koyacağına karar verebileceksin.

### Temel kavramlar
- **Halüsinasyon (uydurma):** Modelin, gerçekte var olmayan bir bilgiyi akıcı ve kendinden emin biçimde üretmesi. Olmayan bir kanun maddesi, var olmayan bir kütüphane fonksiyonu, uydurulmuş bir kaynak.
- **Neden olur:** Dün öğrendiğin cümleyi hatırla — model "doğruyu bulmaya" değil, **"en olası devamı üretmeye"** ayarlıdır. "Bilmiyorum" da olası bir devamdır ama eğitildiği metinlerde akıcı cevaplar daha sık geçtiği için model boşluğu doldurmayı tercih eder. Yani uydurma bir hata değil, **tasarımın yan ürünüdür** — bu yüzden hiçbir ayar onu sıfırlamaz.
- **Kaynağa dayandırma (grounding):** Cevabı modelin hafızasına değil, prompt'a eklediğin gerçek belgelere dayandırmak. Bu RAG'ın (Hafta 8) temel işlevidir.
- **Çıktı doğrulama:** Modelin verdiği cevabı, bir insan görmeden önce **makineyle** kontrol etmek: JSON şemaya uyuyor mu, verdiği ürün kodu veritabanında var mı, sayılar toplamı tutuyor mu.
- **İnsan onay noktası (human in the loop):** Sonucu geri alınamaz bir eylemden önce insanın onaylaması. Para transferi, e-posta gönderimi, veri silme.

Savunmayı tek bir duvar gibi değil, **katmanlar** hâlinde düşün. Sıra şudur: (1) prompt'ta net kural — "yalnız sana verilen belgelere dayan, belgede yoksa 'bilgi yok' de"; (2) kaynağa dayandırma ve kaynak gösterme; (3) makineyle doğrulama; (4) insan onayı. Her katman bir öncekinin kaçırdığını yakalamak içindir.

Kaç katman koyacağını **yanlış cevabın maliyeti** belirler. Üç kutuya ayır:
- **Düşük:** kullanıcı hatayı hemen görür ve bedeli yok (fikir listesi, taslak metin) → prompt kuralı yeter.
- **Orta:** hata iş yükü ya da itibar kaybı yaratır (müşteriye giden özet) → dayandırma + doğrulama.
- **Felaket:** geri alınamaz (para, hukuk, sağlık, veri silme) → hepsi + zorunlu insan onayı.

Buradaki disiplin yeni değil: Hafta 4'te öğrendiğin "sınırda doğrula" kuralının AI çıktısına uygulanmış hâli. Model çıktısı, dışarıdan gelen bir kullanıcı girdisiyle aynı güven seviyesinde ele alınır — yani **güvenilmez.** "Parse, don't validate" (doğrulama değil, ayrıştır) ilkesi de burada işler: gelen metni sınırda güvenli bir tipe dönüştür, sisteme serbest metin olarak sızmasın.

### Somut örnek
Bu projede AI Öğretmen sana ders anlatırken kendi hafızasından değil, `content/` altındaki hafta metinlerinden ve `content/kaynaklar/` altındaki Türkçe çalışma notlarından besleniyor: bu metinler prompt'a eklenir ve sistem kimliğinde "bu kaynaklara dayan" talimatı vardır. Bir de sistemin doğrulama katmanı var: quiz üretimi serbest metin olarak kabul edilmez, beklenen biçimde ayrıştırılır — biçim tutmazsa çıktı reddedilir, öğrenciye bozuk soru gitmez.

### Sık yapılan hata
- **"RAG halüsinasyonu bitirir" sanmak.** Bitirmez, **azaltır.** Yanlış belge getirilirse model yanlış belgeye sadık kalarak yine yanlış cevap verir; ayrıca doğru belgeyi yanlış yorumlayabilir. RAG'ın asıl kazancı, cevabın **kaynağının gösterilebilmesi** ve hatanın izlenebilir hâle gelmesidir.
- **Daha büyük modele geçerek çözmeye çalışmak.** Oran düşer, sorun kalır.
- **Kullanıcıya "hatalı olabilir" yazıp geçmek.** Bu bir savunma katmanı değil, sorumluluk kaydırmadır.
- **Modelin kendine "emin misin?" diye sordurulması.** Model kendi güvenini ölçemez; sorunca çoğu zaman aynı yanlışı daha kararlı tekrarlar. Kontrol dışarıdan gelmeli.

### Görevlere geçmeden önce
Görevde sınıflandırma yaparken şu cümleyi tamamla: *"Bu özellik yanlış cevap verirse, en kötü ihtimalle ______ olur ve bunu ______ fark eder."* İkinci boşluk "hiç kimse" çıkıyorsa, risk düşündüğünden yüksektir ve bir doğrulama katmanı eklemen gerekir.

## Görevler
- [ ] İlgili bölümü oku
- [ ] Kendi AI özelliğinde 'yanlış cevabın maliyeti'ni sınıflandır (düşük/orta/felaket) ve savunma kat sayısını buna göre belirle
