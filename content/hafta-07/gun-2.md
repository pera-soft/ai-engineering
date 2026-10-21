---
hafta: 7
gun: 2
baslik: "Model seçim çerçevesi"
---

## Ders

### Bugün ne öğreneceksin?
Piyasada onlarca model var ve hepsi "en iyisi" olduğunu söylüyor. Bugün, "hangisi daha zeki?" sorusunu bırakıp mühendisin sorduğu soruya geçiyoruz: **"benim işim için, kabul edilebilir hız ve maliyetle, yeterince iyi olan hangisi?"** Günün sonunda bir model seçimini üç sayı ve bir kanıtla savunabileceksin.

### Temel kavramlar
- **Kapalı (closed) model:** Ağırlıkları sende olmayan, yalnız internet üzerinden bir adrese istek atarak kullandığın model. Kolay, bakımsız, ama satıcıya bağımlısın ve veri dışarı çıkar.
- **Açık (open) model:** Ağırlıkları indirilebilen model. Kendi sunucunda çalıştırabilirsin: veri içeride kalır, sürüm senin kontrolünde; karşılığında donanım ve işletme yükünü sen taşırsın.
- **Gecikme (latency):** İsteği gönderdikten sonra cevabın gelmesi için geçen süre. İki parçası vardır: ilk kelimenin gelme süresi (kullanıcı "takıldı mı?" diye buna bakar) ve cevabın tamamlanma süresi.
- **Token maliyeti:** Ücret, gönderdiğin ve aldığın token sayısıyla hesaplanır — girdi ve çıktı genelde farklı fiyatlanır. Bu yüzden maliyet "kaç kullanıcı" değil, **"kaç token"** sorusudur.
- **Benchmark (ölçüt sınavı):** Modelleri ortak sorularla karşılaştıran genel sınavlar ve onlardan çıkan lider tabloları.
- **Kontaminasyon (bulaşma):** Modelin, sınav sorularını eğitim verisinde görmüş olması. O zaman yüksek puan yeteneği değil, ezberi gösterir.

Seçim her zaman bir **üçgen dengesi**dir: kalite ↔ gecikme ↔ maliyet. Üçünü aynı anda en iyi yapamazsın; Hafta 5'in birinci yasası burada da geçerli, her seçim bir ödünleşmedir (trade-off). Bir sohbet ekranında gecikme; gece çalışan toplu bir işte maliyet; hukuki bir özette kalite ağır basar. Önce **hangisinden ödün veremeyeceğini** yaz, sonra model bak.

Benchmark'lar tümüyle çöp değildir — kaba bir eleme yapar ("bu model kod yazabiliyor mu?"). Ama karar mercii değildir, çünkü genel yeteneği ölçerler, senin ürününün başarı ölçütünü değil. Karar, **kendi değerlendirme kümenden** çıkar: 20-50 gerçek örnek, beklenen cevaplarıyla birlikte. Bu küçük kümeyi iki aday modele koşturmak, tüm lider tablolarından daha çok şey söyler.

### Somut örnek
Bu projede model seçimi koda gömülü değil, `application.yml` içinde ayar olarak duruyor ve iki kademe var: **güçlü** model uzun sokratik sohbet için, **hızlı** model kısa yardımcı işler için. Sağlayıcı olarak OpenRouter seçildi (ADR-0004) — tek bir adres üzerinden farklı satıcıların modellerine erişilebildiği için model değiştirmek kod değişikliği değil, ayar değişikliği. Bu, "en iyi model" tartışmasını **geri alınabilir bir karara** çevirmenin mimari yoludur.

### Sık yapılan hata
- **Lider tablosuna bakıp seçmek.** Sınav birincisi senin işinde ikinci olabilir; üstelik soruları görmüş olabilir.
- **Tek çağrının fiyatına bakmak.** "Bin token 0,01 dolar, ucuzmuş" dersin; günde 50 bin çağrıda aynı sayı bambaşka görünür. Maliyeti **aylık toplam** olarak hesapla.
- **Kalite farkını gözle ölçmek.** İki modeli üç soruyla deneyip "bu daha iyiymiş" demek ölçüm değil, izlenimdir. Aynı 30 örneği ikisine de ver, sonuçları yan yana koy.
- **Model kararını geri alınamaz hâle getirmek.** Model adı kodun içinde 12 yere dağılmışsa, seçim değil evlilik yapmışsındır.

### Görevlere geçmeden önce
Karşılaştırma tablonu dört sütunla kur: **kalite (kendi örneklerinde), gecikme, 1000 istekteki maliyet, riskler** (veri nereye gidiyor, satıcı bağımlılığı). Sonuç cümlen şu kalıpta olsun: *"X'i seçiyorum, çünkü kendi 30 örneğimde Y ile aynı kalitede ama iki kat ucuz; kalite düşerse Y'ye geçiş bir ayar değişikliği."*

## Görevler
- [ ] İlgili bölümü oku
- [ ] Bir iş senaryon için 2 model adayını maliyet/gecikme/kalite tablosuyla karşılaştır
