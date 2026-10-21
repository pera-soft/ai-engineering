---
hafta: 10
gun: 4
baslik: "Technology Radar: teknoloji kararlarını sosyalleştirme"
---

## Ders

### Bugün ne öğreneceksin?
Bugüne kadar tek tek kararları konuştuk. Peki "biz bu ekipte hangi teknolojileri kullanıyoruz?" sorusuna kim cevap verecek? Bugün Technology Radar'ı (Teknoloji Radarı) öğreneceksin: merkezî bir onaycı olmadan ekibi aynı hizada tutan basit bir araç. Günün sonunda kendi ekibinin radarını çizebilecek durumda olacaksın.

### Temel kavramlar
- **Technology Radar:** ThoughtWorks kökenli, kullanılan ve düşünülen teknolojileri dört halkaya (ring) yerleştiren tek sayfalık bir görsel. Harmel-Law bunu merkeziyetsiz yönetişimde (governance) kullanır.
- **Halka (ring):** bir teknolojinin ekipteki **duruşu**. Dört tanedir:
  - **Adopt (benimse):** güvenli, varsayılan seçim. Yeni işte bunu kullan.
  - **Trial (dene):** umut verici. Riski kaldırabilecek gerçek bir işte, sınırlı biçimde dene.
  - **Assess (araştır):** ilginç. Okuyup deneyerek öğren, ama henüz üretime koyma.
  - **Hold (beklet):** **yeni yerde kullanma.** Dikkat: "yasak" ya da "hemen sök" demek değildir; mevcut kullanım devam edebilir, yenisi eklenmez.
- **Yönetişim (governance):** kararların nasıl verildiğini düzenleyen çerçeve. Radar, yönetişimi bir onay makamına değil, ortak bir tabloya bağlar.

Radarın gücü şuradan gelir: bir teknolojiyi tartışmak yerine **konumunu** tartışırsın. "Kafka iyi mi kötü mü" sonu gelmez bir sohbettir; "Kafka'yı Trial'dan Adopt'a alalım mı?" ise cevaplanabilir bir sorudur, çünkü hangi kanıtın gerektiğini herkes bilir.

Radar Gün 1'deki Advice Process'in teknoloji-seçimi karşılığıdır: merkezî bir "onaylayıcı" yoktur, ekip halkaları birlikte günceller, karar yine kullanacak kişinindir — ama artık ortak bir zemin üstünde verilir.

### Somut örnek (bu proje)
Bu projenin ADR'leri okunursa radar zaten örtük olarak vardır. Spring Boot + Spring AI (ADR-0001) ve sağlayıcı olarak OpenRouter (ADR-0004) fiilen **Adopt** halkasındadır: yeni iş bunların üstüne kurulur. Bellek-içi `SimpleVectorStore` (ADR-0005) tipik bir **Trial**'dır — bilinçli olarak küçük ölçek için seçilmiştir ve pgvector'a geçişin bu kaydı supersede edeceği baştan yazılmıştır. `docs/tech-debt-tracker.md` ise **Hold** listesine benzer: "bunu yeni yerde tekrarlama" denen kalıplar oradadır. Bir de projenin bağlayıcı kuralı radarın disiplinini gösterir: yeni bağımlılık eklemeden önce ADR açılır. Yani bir şeyin halkasını değiştirmek, bir yazı yazmayı gerektirir.

### Sık yapılan hata
En yaygın hata radarı **onay listesine** çevirmektir: "Adopt'ta olmayanı kullanamazsın." O an merkezî mimara geri dönmüş olursun; radar bir duruş beyanıdır, izin belgesi değil. İkinci hata Hold'u "yasak" sanmak — Hold, yeni kullanımları durdurur, çalışan sistemi söktürmez. Üçüncüsü, radarı bir kez çizip unutmak; güncellenmeyen radar altı ay içinde yalan söylemeye başlar. Dördüncüsü, her şeyi Adopt'a doldurmak: her teknolojinin varsayılan olduğu bir listede hiçbir teknoloji varsayılan değildir.

### Görevlere geçmeden önce
Sekiz-on teknolojiyi yerleştirirken adil ol: bir-iki tanesi Hold'a düşmüyorsa muhtemelen dürüst davranmıyorsundur. Her yerleştirmeyi tek cümlelik gerekçeyle yaz, çünkü asıl değer halkada değil gerekçededir. İkinci görevde Hold ve Trial kararlarını yazarken şu iki soruyu cevapla: Hold için "bunu bir daha seçersem hangi bedeli ödeyeceğim?", Trial için "bu denemenin başarılı sayılması için ne görmem gerekir ve ne zaman karar vereceğim?" Tarihsiz bir Trial, Trial değil sadece bir umuttur.

## Görevler
- [ ] Ekibinin kullandığı 8-10 teknolojiyi 4 halkaya yerleştir
- [ ] 1 'Hold' ve 1 'Trial' kararını gerekçesiyle yaz
