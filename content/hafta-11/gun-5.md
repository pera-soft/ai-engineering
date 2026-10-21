---
hafta: 11
gun: 5
baslik: "Sentez + quiz"
---

## Ders

### Bugün ne öğreneceksin?
Haftanın dört gününü tek bir zihinsel haritada birleştireceksin ve haftanın eksik kalan son parçasını ekleyeceksin: veri **tek bir yerde durmaz**, sistemler arasında akar. Bu akışın adı **türetilmiş veri** (derived data) ve akışın iki hızı vardır: toplu (batch) ve akan (stream).

### Temel kavramlar
Önce haftanın zinciri: Gün 1 **ne kadar iyi olmalı** (güvenilirlik, yük parametresi, p99) → Gün 2 **diske nasıl yazılır** (B-tree / LSM, OLTP / OLAP) → Gün 3 **kaç kopya, ne kadar gecikmeyle** (replikasyon, read-your-writes) → Gün 4 **nasıl bölünür ve nasıl doğru kalır** (bölümleme, izolasyon).

Eksik parçalar:

- **Kayıt sistemi (system of record):** Bir veri için **tek yetkili kaynak**. "Kullanıcının e-postası hangi sistemde doğrudur?" sorusunun cevabı tektir. Diğer her kopya ondan türer.
- **Türetilmiş veri (derived data):** Kayıt sisteminden **üretilmiş**, kaybedilse yeniden üretilebilen veri: arama indeksi, önbellek, rapor tablosu, öneri listesi. Türetilmiş veriyi kayıt sistemi sanmak, mimarideki en pahalı karışıklıklardan biridir.
- **CDC (Change Data Capture — değişiklik yakalama):** Kayıt sistemindeki her değişikliği (ekle/güncelle/sil) bir olay akışına çevirip aşağı akıştaki sistemleri beslemek. Böylece arama indeksi ile veritabanı birbirinden sessizce ayrışmaz.
- **Toplu işleme (batch):** Sabit bir veri yığınını baştan sona işlemek. Gecikme yüksek (dakikalar/saatler), ama **tekrar çalıştırılabilir** — hata bulursan kodu düzeltip aynı girdiyle yeniden koşarsın.
- **Akış işleme (stream):** Olaylar geldikçe işlemek. Gecikme düşük (saniyeler), ama geçmişi düzeltmek zordur; "dün gece yanlış hesapladık" demek pahalıdır.

### Somut örnek
Bu proje (AI Öğretmen) haftanın kavramlarının küçük bir haritasıdır. **Kayıt sistemi:** `content/` altındaki müfredat dosyaları — doğru olan odur. **Türetilmiş veri:** uygulamanın açılışta bellekte kurduğu içerik dizini; silinse yeniden üretilir, bu yüzden yedeklenmesi gerekmez. **Batch akışı:** `node tools/html-to-content.mjs` komutu — HTML'den içerik üretir, istediğin kadar tekrar koşabilirsin. **Tutarlılık sözleşmesi:** içerik commit'i canlıya **otomatik gitmez**; sunucuya senkron edilmezse canlı sistem eski sürümü servis eder. Bu tam olarak Gün 3'teki replikasyon gecikmesidir — sadece kâtip burada bir insandır ve gecikme saatlerle ölçülür.

### Sık yapılan hata
İki tuzak: (1) **Önbelleği kayıt sistemi sanmak.** Redis'e yazıp veritabanına yazmayı unutan sistem, ilk bellek temizliğinde veriyi kaybeder — önbellek türetilmiş veridir, kaynak değildir. (2) **"Gerçek zamanlı olsun" demeyi bedava sanmak.** Akış işleme, geriye dönük düzeltmeyi zorlaştırır; çoğu rapor için gece koşan bir batch hem daha ucuz hem daha doğrudur. Hafta 5'in birinci yasası burada da geçerlidir: her şey bir takas.

### Görevlere geçmeden önce
Bu haftanın kavramları **Hafta 5'in mimari karakteristikleri** ile **Hafta 6'nın dağıtık stillerinin** (mikroservis, olay güdümlü, space-based) veri katmanıdır: dağıtık bir stil seçtiğin an bu veri kararlarıyla yüzleşirsin. Aşağıdaki karar listesini yazarken her maddeyi bir **soru** olarak yaz ("hangi ekranda hangi garanti?"), bir slogan olarak değil ("biz eventual consistency kullanırız"). Sonra quiz'i çöz.

## Görevler
- [ ] 'Veri mimarisi karar listem' — 10 maddelik kişisel kontrol listesi yaz
- [ ] Haftalık quiz'i çöz
