---
hafta: 11
gun: 2
baslik: "Depolama motorları"
---

## Ders

### Bugün ne öğreneceksin?
Veritabanı "veriyi saklayan kutu" değildir. İçinde, veriyi diske **nasıl** yazacağına karar veren bir motor vardır ve bu motorun seçimi, sisteminin hangi işte hızlı hangi işte yavaş olacağını belirler. Bugün iki büyük aileyi — **B-tree** ve **LSM-tree** — ve iki iş yükü tipini — **OLTP** ve **OLAP** — sıfırdan öğreneceksin.

### Temel kavramlar
- **Depolama motoru (storage engine):** Veritabanının, veriyi diske yazma ve bulma işini yapan iç parçası. PostgreSQL'in, MySQL'in, Cassandra'nın hepsinde vardır; farkları buradadır.
- **İndeks (index):** Kitabın arkasındaki dizin gibi. "Ahmet" kaydını bulmak için tüm sayfaları taramak yerine dizine bakarsın. **Bedeli var:** her yeni kayıt eklendiğinde dizini de güncellemek gerekir. Yani her indeks **okumayı hızlandırır, yazmayı yavaşlatır**. Hafta 5'in birinci yasası burada da geçerlidir: her şey bir takas (trade-off).
- **B-tree:** Veriyi diskte sabit boyutlu sayfalarda, **sıralı** tutar ve bir kayıt değişince onu **yerinde günceller**. Kütüphanedeki rafa gidip kitabı çıkarıp yerine yenisini koymak gibi. Okuma öngörülebilir ve hızlıdır; yazma, disk üzerinde gidip yer bulmayı gerektirdiği için pahalıdır. PostgreSQL/MySQL'in varsayılanıdır.
- **LSM-tree (Log-Structured Merge-tree):** Yazmayı **yerinde yapmaz**. Önce belleğe alır, dolunca diske **ardışık** (baştan sona, hiç geri dönmeden) tek parça hâlinde döker. Masaya not kâğıdı üstüne kâğıt biriktirip, sonra oturup hepsini temize çekmek gibi. Bu "temize çekme" işine **compaction (birleştirme)** denir.
- **Bloom filtresi:** LSM'de veri birden çok katmana dağıldığı için okuma zorlaşır. Bloom filtresi, "bu anahtar bu dosyada **kesinlikle yok**" diye ucuz cevap veren küçük bir yapıdır; gereksiz disk okumasını engeller.
- **OLTP (anlık işlem):** Çok sayıda, çok küçük okuma/yazma. "Şu siparişi kaydet", "şu kullanıcıyı getir." Kullanıcının beklediği yer burasıdır.
- **OLAP (analitik):** Az sayıda, çok büyük tarama. "Son 3 ayın ciro raporu." Kimse milisaniye beklemez ama milyonlarca satır okunur.

### Somut örnek
Bir e-ticaret sitesinde iki ayrı ihtiyaç yan yana durur. **Sipariş yazma yolu:** saniyede binlerce küçük yazma, hep "en son sipariş" ekleniyor — LSM'nin ardışık yazması burada kazanır. **Yönetici raporu:** "geçen ayın kategori bazlı cirosu" — bu sorgu 5 milyon satırı taramak zorunda; OLTP veritabanında çalıştırırsan sipariş yolunu da yavaşlatırsın. Çözüm motor değiştirmek değil, **işi ayırmaktır**: raporlar ayrı bir analitik depoya (kolon tabanlı) taşınır. "Her iş için tek veritabanı" devrinin kapanma sebebi tam olarak budur.

### Sık yapılan hata
İki tuzak: (1) **"LSM daha yeni, o hâlde daha iyi."** LSM'nin yazma avantajı, okumada birden çok katmana bakma ve arka planda compaction için disk/CPU harcama bedeliyle gelir. Yoğun okuma yapan bir sistemde B-tree çoğu zaman daha iyidir. (2) **Her sütuna indeks eklemek.** "Ne olur ne olmaz" diye eklenen indeksler yazma yolunu sessizce boğar; indeks bedava değildir, **yazma hızından ödünç alınır**.

### Görevlere geçmeden önce
Şu soruyu cevaplayabiliyor olmalısın: "Benim sistemim ağırlıklı olarak okuyor mu, yazıyor mu; sorgularım küçük ve noktasal mı, büyük ve tarayıcı mı?" Cevabı bilmeden motor tartışmak, ihtiyacı bilmeden araba seçmektir.

## Görevler
- [ ] Bu haftanın kaynaklarındaki DDIA Türkçe çalışma metnini oku (İngilizce kitap depolama bölümü opsiyonel-tamamlayıcı)
- [ ] Kullandığın DB'nin motor ailesini ve bunun iş yüküne uygunluğunu değerlendir
