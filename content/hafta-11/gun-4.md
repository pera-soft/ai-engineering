---
hafta: 11
gun: 4
baslik: "Partitioning ve transaction'lar"
---

## Ders

### Bugün ne öğreneceksin?
İki ayrı ama birbirini besleyen konu: veri tek makineye sığmadığında onu **parçalara bölmeyi** (bölümleme / partitioning), ve birden çok işlemi "ya hep ya hiç" yapan **işlemleri** (transaction) — özellikle ACID'in gerçekte ne vaat ettiğini.

### Temel kavramlar
- **Bölümleme (partitioning / sharding):** Veriyi parçalara ayırıp her parçayı farklı makinede tutmak. Amaç: tek makineye sığmayan veriyi ve tek makinenin kaldıramadığı yükü dağıtmak. (Replikasyondan farkı: replikasyon **aynı** veriyi çoğaltır, bölümleme **farklı** veriyi ayırır. Gerçek sistemlerde ikisi birlikte kullanılır.)
- **Anahtar aralığına göre bölümleme (key-range):** Ansiklopedinin ciltleri gibi: A–F birinci makine, G–M ikinci... Aralık sorguları tek makinede biter, ama dağılım bozulabilir.
- **Hash'e göre bölümleme:** Anahtarı bir özet fonksiyonundan geçirip sonuca göre makine seçmek. Yükü dengeler, ama **komşuluğu yok eder**: "1–31 Ocak siparişleri" artık tüm makinelere sorulur.
- **Sıcak nokta (hot spot):** Yükün tek bir parçaya yığılması. Klasik örnek: anahtar olarak tarih seçmek — bugünün verisi hep aynı makineye düşer, diğerleri boş durur.
- **İşlem (transaction):** Birden çok okuma/yazmayı **tek bir bütün** sayan mekanizma. Ya hepsi olur, ya hiçbiri.
- **ACID'in gerçek anlamı:** *Atomicity* = "birden çok iş aynı anda çalışır" DEĞİL; "yarıda kalırsa hepsi geri alınır" demektir. *Consistency* aslında veritabanının değil, **uygulamanın** sorumluluğudur (kural sende, veritabanı sadece taşır). *Isolation* = eşzamanlı işlemlerin birbirini görmemesi. *Durability* = onayladıysam diskte kaldı.
- **İzolasyon seviyeleri (zayıftan güçlüye):** **Read committed** (yalnız onaylanmış veriyi okursun), **snapshot/repeatable read** (işlem boyunca dünyayı sabit bir fotoğraf gibi görürsün), **serializable** (sonuç, işlemler sırayla çalışmış gibi olur).
- **Anomaliler:** *Kirli okuma* (dirty read) — henüz onaylanmamış veriyi görmek. *Kayıp güncelleme* (lost update) — iki kişi okuyup değiştirince biri silinir. *Yazma eğriltmesi* (write skew) — her işlem tek başına kurala uyar, ikisi birlikte kuralı bozar.

### Somut örnek
Bir hastanede kural şu: **her an en az bir doktor nöbette olmalı.** Nöbetteki iki doktor, Ayşe ve Can, aynı anda "nöbetten çık" düğmesine basıyor. Ayşe'nin işlemi bakıyor: "benden başka Can var" → izin veriyor. Can'ın işlemi bakıyor: "benden başka Ayşe var" → izin veriyor. Her ikisi de tek başına doğruydu; sonuçta **nöbette kimse kalmadı**. Bu **yazma eğriltmesidir** ve snapshot izolasyonu bunu engellemez; engelleyen tek seviye **serializable**'dır. Aynı sistemde "profil fotoğrafı güncelle" akışı için serializable kullanmak ise gereksiz bir performans faturasıdır.

### Sık yapılan hata
En yaygın yanılgı: **"Veritabanım ACID, o hâlde eşzamanlılık sorunlarım yok."** Neredeyse hiçbir veritabanı varsayılan olarak serializable çalışmaz — PostgreSQL varsayılanı read committed'dır. Yani ACID rozeti sende, ama write skew ve lost update riskleri de sende. İkinci hata: bölümleme anahtarını **veriye bakarak değil, koda bakarak** seçmek; müşteri kimliğine göre bölümlersen ve bir müşteri toplam yükün yarısını üretiyorsa (bir ünlünün hesabı gibi), hash bile seni kurtarmaz.

### Görevlere geçmeden önce
İki cümleyi hazırla: "Veritabanımın varsayılan izolasyon seviyesi şudur" ve "bu seviyede bana açık kalan anomali şudur". Bunu bilmiyorsan sistemin doğruluğu hakkında bir varsayımın var, bilgin yok — ve bu tür varsayımlar üretimde, en yoğun saatte kendini gösterir.

## Görevler
- [ ] Bu haftanın kaynaklarındaki DDIA Türkçe çalışma metnini oku (İngilizce kitap ilgili bölümler opsiyonel-tamamlayıcı)
- [ ] Kendi DB'nin izolasyon seviyesini öğren ve 1 potansiyel anomaliyi adlandır
