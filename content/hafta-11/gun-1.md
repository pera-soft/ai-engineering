---
hafta: 11
gun: 1
baslik: "Güvenilirlik, ölçeklenebilirlik, bakım"
---

## Ders

### Bugün ne öğreneceksin?
Bir veri sisteminin "iyi" olması ne demektir? Bugün bu belirsiz soruyu üç ölçülebilir başlığa indiriyoruz: **güvenilirlik, ölçeklenebilirlik, bakım kolaylığı**. Ayrıca "sistemimiz hızlı" gibi hiçbir şey söylemeyen bir cümleyi, savunulabilir bir sayıya nasıl çevireceğini öğreneceksin.

### Temel kavramlar
Hiç duymadığını varsayarak sıfırdan tanımlıyoruz:

- **Veri-yoğun sistem:** Zorlandığı yer işlemcinin hesap gücü değil, **verinin miktarı, karmaşıklığı ve değişme hızı** olan sistem. Bu haftanın kitabı (DDIA) baştan sona bunu anlatır.
- **Güvenilirlik (reliability):** Bir şeyler ters gittiğinde bile sistemin **doğru çalışmaya devam etmesi**. Ters giden şeyler üç gruptur: donanım arızası, yazılım hatası ve — en sık olanı — insan hatası. Dikkat: güvenilir sistem, hata yaşamayan sistem değildir; **hatayı tolere eden** sistemdir.
- **Yük parametresi (load parameter):** Yükü tarif eden **sayı**. "Sistem yoğun" demek yerine: saniyedeki yazma sayısı, eşzamanlı kullanıcı sayısı, kullanıcı başına ortalama takipçi sayısı... Doğru parametreyi seçmek işin yarısıdır, çünkü yanlış parametreyi optimize edersen boşa çalışırsın.
- **Ölçeklenebilirlik (scalability):** Bir sistemin sahip olduğu bir **özellik değil**, sorulan bir **sorudur**: "Şu yük parametresi 10 katına çıkarsa, performansı korumak için neyi değiştirmem gerekir?" "Sistemimiz ölçeklenebilir" cümlesi tek başına anlamsızdır.
- **Yüzdelik dilim (percentile):** 100 isteği süreye göre sıralarsan **p50** ortadaki, **p99** sondan ikincidir. "p99 = 300 ms" demek: isteklerin %99'u 300 ms'nin altında bitti, %1'i daha uzun sürdü. p99 en yavaş dilimin eşiğidir — en hızlının değil.
- **Bakım kolaylığı (maintainability/operability):** Sistemi üç yıl sonra, gece 03:00'te, seni hiç tanımayan biri ayağa kaldıracak. Operasyonel sadelik bir mimari karakteristiktir; Hafta 5'te öğrendiğin "-ility"lerin veri katmanındaki yüzüdür.

### Somut örnek
Bu projeyi (AI Öğretmen) ele alalım. "Uygulama yavaş" cümlesini sayıya çevirmek şöyle görünür:

- **Yük parametreleri:** eşzamanlı öğrenci sayısı, günde sorulan soru sayısı, soru başına gönderilen bağlam uzunluğu (token).
- **Ölçüm:** ortalama yanıt 2 saniye. Kulağa iyi geliyor. Ama p99 = 9 saniye.
- **Ne demek bu?** Her 100 sorudan 1'i dokuz saniye bekliyor. Ve o soru rastgele değil — en uzun bağlamı gönderen, yani **en çok çalışan öğrencinin** sorusu. Ortalama tam da en değerli kullanıcının acısını sakladı.
- **Karar:** "p99 < 4 sn" hedefini yaz, aşınca alarm kur. Artık tartışma "yavaş mı?" değil, "hedefi tutturuyor muyuz?" olur.

### Sık yapılan hata
En yaygın hata **ortalamaya bakıp rahatlamaktır**. Ortalama, kuyruktaki acıyı matematiksel olarak düzler; kullanıcı ise ortalamayı değil, kendi isteğini yaşar. İkinci hata: p99 kötü çıkınca refleksle "sunucuyu büyütelim" demek. Önce **hangi isteklerin** kuyruğa düştüğüne bak — genelde belirli bir sorgu tipi ya da belirli bir kullanıcı profilidir; makine büyütmek o sorguyu düzeltmez, faturayı büyütür.

### Görevlere geçmeden önce
Şu üç cümleyi kurabiliyor olmalısın: (1) Sistemimin yükünü tarif eden parametre şudur. (2) Bu parametre 10 katına çıkarsa ilk kırılacak yer şurasıdır. (3) Gecikme hedefim p99 cinsinden şu sayıdır. Bu üçü olmadan aşağıdaki görevler sadece okuma olur; bunlarla birlikte bir **mimari sözleşmeye** dönüşür.

## Görevler
- [ ] Bu haftanın kaynaklarındaki DDIA Türkçe çalışma metnini oku (İngilizce kitap Bölüm 1 opsiyonel-tamamlayıcı)
- [ ] Kendi sisteminin yük parametrelerini ve p99 hedefini yaz
