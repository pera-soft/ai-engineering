---
hafta: 12
gun: 5
baslik: "Capstone + final quiz"
---

## Ders

### Bugün ne öğreneceksin?
12 haftanın tüm ipliklerini tek projede toplamayı — ve son bir liderlik becerisini: **teknik olanı iş diline çevirmeyi**. Capstone bir bitirme ödevi değil, artık böyle çalıştığının kanıtıdır.

### Temel kavramlar
- **Capstone:** Ayrı ayrı öğrenilen becerilerin tek bir gerçek problem üzerinde birleştirildiği bitirme projesi. Yeni bir konu öğretmez; öğrendiklerinin **birlikte** çalıştığını gösterir.
- **Zincir:** Gerçek bir iş problemi al ve sırayla işlet — **spec yaz** (Hafta 3 + Spesifikasyon Şablonu) → **mimari karar ver ve ADR yaz** (Hafta 2 + Hafta 5 trade-off dili) → **ajanla uygula**, sen yönlendir ajan uygular (Hafta 1) → **incele ve mühürle**: Code-Review Kontrol Listesi + fitness function/ArchUnit (Gün 3 + Hafta 4/9) → **docs/'u güncelle** (Hafta 2).
- **Teknik borç (technical debt):** Bugün hızlı gitmek için bilinçli seçilen kısayolun, ileride faiziyle ödenmesi. Krediye benzer: borç almak kötü değildir, **faizini görmemek** kötüdür. Faiz burada "her yeni özellik biraz daha yavaş çıkıyor" şeklinde ödenir.
- **İş etkisine tercüme:** Yöneticiler "kod karmaşık" cümlesiyle karar veremez. Aynı gerçeği ölçülebilir sonuçlara çevirmek gerekir: teslim süresi, arıza sıklığı, arızadan dönüş süresi, harcanan adam-gün, kaybedilen gelir. Teknik borcu **sayıyla** anlatamıyorsan, o borç bütçe almaz.
- **Kararın geri alınabilirliği:** Kolay geri alınan kararı hızlı ver; geri alınamayan kararı yavaşlat, danış (Hafta 10) ve yaz. Capstone sunumunda bu ayrımı göstermek, kıdemli mimarı acemiden ayıran cümledir.
- **Geri getirme pratiği (retrieval practice):** Bilgiyi tekrar **okumak** değil, kapalı kitap **hatırlamaya çalışmak**. Kalıcılığı sağlayan budur; final tekrarını bu yüzden karma soru olarak istiyorsun.

### Somut örnek
Capstone sunumunda aynı gerçeğin iki anlatımı:

- **Tercüme edilmemiş:** "Sipariş modülü çok bağlı (coupled), testler kırılgan, refactor lazım." Yönetici duyduğu şey: mühendis kod beğenmemiş. Sonuç: bütçe yok.
- **Tercüme edilmiş:** "Sipariş modülündeki her değişiklik 3 ayrı yeri kırıyor. Son çeyrekte bu alanda 6 üretim hatası çıktı, her biri ortalama 4 saat kesinti demek. Küçük bir özellik 2 gün yerine 5 gün sürüyor. 8 günlük bir ayrıştırma çalışması bu maliyeti ortadan kaldırır; ayrıca geri alınabilir bir adım — beğenmezsek eski yapıya dönebiliriz." Sonuç: bu artık bir mühendis şikâyeti değil, **yatırım teklifidir**.

Fark cümlenin uzunluğunda değil: ikincisi maliyeti, faydayı ve **geri dönüş riskini** birlikte veriyor.

### Sık yapılan hata
İki tuzak: (1) **Capstone'u demo sanmak.** Değerlendirilen şey çalışan ekran değil, **karar zinciridir**: spec var mı, ADR'de reddedilen alternatif yazılı mı, inceleme mühürlü mü, dokümantasyon güncel mi? (2) **Teknik borcu ahlaki bir mesele yapmak.** "Doğru olan bu" demek karar aldırmaz; borcu iş etkisine çevirmek aldırır. Ve unutma: bilinçli alınmış, yazılı ve ölçülen borç bir hata değil — **karardır**.

### Görevlere geçmeden önce
Sunumunu 15 dakikaya sığdırmak için üç soruyu cevaplayacak biçimde kur: ne karar verdim, neyi feda ettim, yanılırsam nasıl geri döneceğim? Sonra final quiz'ini çöz ve 1-11. haftalardan karma tekrar iste. Program bittiğinde iş bitmiyor: **harness'ini her yeni modelde yeniden gözden geçir.**

## Görevler
- [ ] Capstone projesini tamamla ve ekibe 15 dk sun
- [ ] Final quiz'ini çöz + AI öğretmenden 1-11. haftalardan KARMA final tekrarı iste (geri getirme
      pratiği — 12 haftanın kalıcılığını yeniden-okuma değil, hatırlama sağlar)
- [ ] Programı bitirdin — şimdi harness'ini her yeni modelde gözden geçirmeyi unutma 🏗️
