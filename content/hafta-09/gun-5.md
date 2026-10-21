---
hafta: 9
gun: 5
baslik: "Mini uygulama + quiz"
---

## Ders

### Bugün ne öğreneceksin?
Bugün okuduğunu ele alıp **çalışan koda** çevireceksin. Küçük bir use-case'i (kitabın buckpal örneğindeki gibi bir "para transferi") baştan sona hexagonal iskelede kuracaksın. Asıl öğreneceğin şey ise şu: mimari kararları sen verirsin, kodu ajan yazabilir — ve bu iki işi karıştırmamak, ajanla çalışmanın tüm farkını yaratır.

### Temel kavramlar
- **Use-case (kullanım senaryosu):** kullanıcının sistemden istediği tek bir iş: "para transfer et", "quizi tamamla". Hexagonal'da her use-case bir incoming port'a karşılık gelir.
- **Yürüyen iskelet (walking skeleton):** baştan sona ince ama **tam** bir dilim: web adapter → incoming port → domain → outgoing port → persistence adapter. Önce derinliği değil, uçtan uca bağlantıyı kurarsın; sonra iş kuralını zenginleştirirsin.
- **Yönlendirme (direct) ve uygulama (execute) ayrımı:** Hafta 1'in ilkesi. Yönlendirme = "outgoing port'u domain'de tanımla, JPA'yı adapter'da tut, sınırda iki yönlü eşleme kullan". Uygulama = bu talimatın kodunu yazmak. İlkini devretmezsin, ikincisini rahatça devredersin.

Sıra şu olsun: (1) use-case'i tek cümleyle yaz. (2) Incoming port arayüzünü çıkar. (3) Domain'in dışarıdan neye ihtiyacı olduğunu sor, outgoing port'u domain'in içinde tanımla. (4) İki adapter yaz. (5) Outgoing port'un bellek-içi sahte (fake) uygulamasıyla iş kuralını test et. (6) ArchUnit kuralını çalıştır.

### Somut örnek (bu proje)
Bu proje tam olarak böyle büyüdü. `ai/AnlamArama` bir outgoing port'tur; `ai/SpringAiAnlamArama` onun satıcıya bağlı adapter'ıdır; `service/KitapService` ise ikisinin hangisiyle konuştuğunu bilmez. Karar ADR-0005'te yazılıdır ve şunu söyler: depo yarın pgvector'a taşınırsa arayüz sayesinde service değişmez. Kararın kendisi (`docs/adr/`), mührü (`MimariTestleri`) ve uygulaması (adapter sınıfı) üç ayrı yerdedir — üçünü de sen kurarsın, kodun çoğunu ajan yazabilir. Ajana verilecek talimat şuna benzer: "Bu port arayüzünü değiştirmeden bir adapter yaz; Spring AI tipleri `ai/` paketinden dışarı çıkmasın; `./mvnw -Dtest=MimariTestleri test` yeşil kalsın."

### Sık yapılan hata
En sık hata, ajana "para transferi özelliğini ekle" deyip mimariyi ona bıraktığın durumdur: sonuç genellikle service'in içine gömülmüş JPA çağrıları olur, çünkü en kısa yol odur. İkinci hata, mini uygulamayı gereğinden büyük seçmek — üç use-case'li bir modül, öğrenmeyi değil yorgunluğu üretir. Üçüncüsü: sahte adapter yerine mock kütüphanesine sarılmak; port varsa on satırlık bir `Map` tabanlı sınıf zaten yeter. Dördüncüsü: kodu bitirip ArchUnit kuralını çalıştırmayı unutmak — mühürlenmemiş sınır, sınır değildir.

### Görevlere geçmeden önce
Bitmiş sayılman için üç kanıt iste: (1) domain sınıfların hiçbir çerçeve tipini import etmiyor, (2) iş kuralı testi veritabanı olmadan koşuyor, (3) `./mvnw verify` yeşil. Bu üçü sağlanmadan quize geçme. Quiz'i çözerken de her soruyu bu haftanın tek cümlesine bağla: **bağımlılık daima içeriye, domain'e doğru akar** — geri kalan her şey bunun sonucudur.

## Görevler
- [ ] Mini modülü tamamla (ajan kullanımı serbest, mimari kararlar sende)
- [ ] Haftalık quiz'i çöz
