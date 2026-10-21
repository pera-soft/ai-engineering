---
hafta: 9
gun: 3
baslik: "Sınırda eşleme (mapping) stratejileri"
---

## Ders

### Bugün ne öğreneceksin?
Dün port ve adapter'ı kurduk. Bugün sıra o sınırdan **verinin nasıl geçtiğine** geliyor. Hombergs dört eşleme (mapping) stratejisi anlatır; hiçbiri "doğru cevap" değildir, dördü de bir bedelle gelir. Günün sonunda kendi projendeki eşlemeyi adıyla söyleyebilecek ve bunu tesadüf olmaktan çıkarıp bilinçli bir karara çevirebileceksin.

### Temel kavramlar
- **Eşleme (mapping):** bir verinin bir şekilden başka bir şekle çevrilmesi. Örneğin dışarıdan gelen JSON'daki `{"tutar": "150.75"}` metnini, domain'in beklediği `Para` nesnesine (kuruş cinsinden `15075`) dönüştürmek.
- **Sınır (boundary):** dış dünya ile domain'in temas ettiği yer, yani adapter. "Sınırda parse et" ilkesinin adresi burasıdır: ham veri buraya kadar gelir, içeri **çevrilmiş** ve doğrulanmış hâlde girer.
- **Model:** veriyi tutan sınıf. Aynı kavramın üç ayrı modeli olabilir: web modeli (JSON'a uygun), domain modeli (iş kurallarına uygun), persistence modeli (tabloya uygun).

Dört strateji, en ucuzdan en pahalıya:

1. **No mapping (eşleme yok):** tek sınıf her katmanda dolaşır. En az kod, en hızlı başlangıç. Bedeli: aynı sınıf hem JSON alanı hem tablo sütunu hem iş kuralı taşır; tabloya bir sütun eklemek API'yi değiştirir. Küçük CRUD işleri için yeterlidir.
2. **One-way mapping (tek yönlü):** katmanlar ortak bir arayüzü (ör. "durum" davranışı) paylaşır; dönüşüm tek yönde yapılır. Orta yol, ama kimin kimi çevirdiği kafa karıştırabilir.
3. **Two-way mapping (çift yönlü):** her adapter kendi modelini domain modeline ve geri çevirir. Domain temiz kalır, çeviri kodu adapter'da toplanır. En sık kullanılan denge budur.
4. **Full mapping (tam eşleme):** her use-case kendi giriş modelini alır. En izole, en açık; ama en çok kod ve en çok tekrar.

Kural şu: **eşleme kodu sıkıcıdır ama sınır oradadır.** Eşlemeyi kaldırdığında kodu değil, sınırı silmiş olursun.

### Somut örnek (bu proje)
Bu projede `web` katmanı dışarıdan HTTP isteği alır, `service` katmanı iş kuralını çalıştırır, `types` katmanı ise saf veri tiplerini (record'ları) tutar. `MimariTestleri` içindeki `types_bagimsiz` kuralı `types` paketinin hiçbir üst katmana bağımlı olmasını yasaklar — yani domain tipleri, web ya da persistence şeklini **öğrenemez**. ADR-0005'te de aynı çizgi görülür: `service/KitapService` Spring AI'nın `Document` tipini değil, projenin kendi `NotParcasi`/`BenzerNot` tiplerini görür. Aradaki çeviriyi `SpringAiAnlamArama` adapter'ı yapar. Bu, iki yönlü eşlemenin (3. strateji) canlı örneğidir: satıcının veri şekli sınırda kalır, içeri girmez.

### Sık yapılan hata
En yaygın hata, stratejiyi **seçmeden** kullanmaktır: proje "no mapping" ile başlar çünkü hızlıdır, kimse karar verdiğini fark etmez, iki yıl sonra JSON alanının adını değiştirmek veritabanı göçü (migration) gerektirir. İkinci hata, tersi: her yere DTO üretip her katmanda çeviri yazmak ve basit bir okuma uç noktasını (endpoint) beş sınıfa yaymak. Üçüncüsü: doğrulamayı (validation) domain'e ertelemek — ham metin içeri girdikten sonra kim doğrularsa doğrulasın, domain artık kirli veriyi görmüş olur.

### Görevlere geçmeden önce
Kendi kodunda tek bir uç noktayı izle: dışarıdan giren veri kaç kez şekil değiştiriyor? Hiç değiştirmiyorsa "no mapping" yapıyorsun demektir — bu bir hata değil, sadece **bir karardır** ve kaydedilmesi gerekir. Görevde bu kararı adlandırırken tek cümlelik bir gerekçe yaz: "Bu modül küçük ve tek okuyucusu var, no mapping bilinçli seçimdir" ya da "Bu sınır dış satıcıya bakıyor, iki yönlü eşleme kullanıyorum". Adlandırılan trade-off, yarın ArchUnit ile mühürleyeceğin sınırın taslağıdır.

## Görevler
- [ ] Türkçe çalışma metnini/görsel özeti oku ("Bu haftanın kaynakları" kutusundan; kaynak: kitap mapping bölümü)
- [ ] Kendi API→domain eşlemende dört stratejiden hangisini kullandığını adlandır, bilinçli bir karara çevir
