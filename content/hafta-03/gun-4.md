---
hafta: 3
gun: 4
baslik: "Öz-denetimli inceleme döngüsü (kaynakta: Ralph Wiggum): ajan-ajan"
---

## Ders

### Bugün ne öğreneceksin?
Bugün ajanın işini **teslim etmeden önce denetlenmesini** sağlayan döngüyü kuracaksın. Amaç, hataları senin gözünle değil, **kriterlerle** yakalamak: ajan üretir, sonuç somut bir listeye karşı denetlenir, eksikler geri beslenir, kriterler karşılanana kadar döner. Sen döngünün her turunda değil, yalnız sonunda varsın.

### Temel kavramlar
- **İnceleme döngüsü (review loop):** üret → denetle → düzelt → tekrar denetle sırasının, kriterler yeşile dönene kadar tekrarlanması.
- **Öz-denetim (self-review):** ajanın kendi çıktısını denetlemesi. Yalnız **nesnel** bir listeye karşı yapılırsa işe yarar.
- **İnceleyici ajan (reviewer agent):** işi yapan ajandan **ayrı** çalışan, tek görevi denetlemek olan ikinci ajan. Başlıktaki "ajan-ajan" budur.
- **Nesnel kriter:** "iyi görünüyor" değil, tek tek işaretlenebilen madde: doğruluk, güvenlik, dış sözleşme, test kapsamı.
- **Aşırı mühendislik (over-engineering):** gerekmediği hâlde soyutlama, katman ve seçenek ekleyerek işi büyütmek.
- **Kapsam sınırı (scope boundary):** inceleyiciye "neyi rapor etmeyeceğini" söyleyen cümle.

Bu, Hafta 1 Gün 4'teki **"yapan ≠ yargılayan"** ilkesinin pratik hâlidir. Görünürde bir çelişki var: "ajan kendi ödevini notlandıramaz" demiştik, şimdi kendi işini denetlettiriyoruz. Çelişki değil: orada yasaklanan **öznel öz-övgüydü** ("bence çok iyi oldu"). Burada denetim, **dışarıdan verilmiş nesnel kriterlere** — ya da ayrı bir ajana — karşı yapılıyor. Ölçüt ajanın kendi kanısı değil, listenin kendisi.

### Somut örnek
Bu projede döngünün nesnel ayağı hazır: `./mvnw verify` hem birim testleri hem `MimariTestleri`'ni çalıştırır. Yani "katman kuralı çiğnendi mi?" sorusunun cevabını **kimse yorumlamaz**, komut söyler. İnceleme döngüsü bunun üstüne kurulur:

- **Tur 1:** Ajan `yuzde` alanını ekler, `./mvnw verify` çalıştırır → ArchUnit kırmızı: hesap `web` katmanında.
- **Tur 2:** Hesabı `service/IlerlemeService`'e taşır, tekrar çalıştırır → yeşil.
- **Tur 3:** Şimdi *makinenin göremediği* kısım gelir. Ayrı bir inceleyici ajana PR'ı verirsin ve şu listeye karşı denetletirsin: **doğruluk** (0 görevli günde sıfıra bölme var mı?), **güvenlik** (yanıtta sızan bilgi var mı?), **dış sözleşme** (var olan bir API alanı silinmiş veya adı değişmiş mi?), **test** (yeni davranışın testi var mı?).
- İnceleyicinin prompt'unda tek satırlık sınır bulunur: *"Yalnız doğruluğu, güvenliği veya gereksinimleri etkileyen bulguları işaretle. Stil ve tercih meselelerini raporlama."*

Sonuç: mekanik kriterleri `verify`, mekanikleştirilemeyenleri kontrol listesi yakalar. İkisi de senin dikkatine bağlı değildir.

### Sık yapılan hata
En sık hata, inceleyiciye **sınır koymamaktır**. "Bu kodda boşluk ara" diye prompt'lanan bir ajan **her zaman** bir şey bulur — bulunacak bir şey olmasa bile. Rapor şişer: "burada bir arayüz olabilirdi", "bu değer yapılandırmadan gelmeli", "önbellek eklenebilir". İş sağlamdır ama artık üç günlük yeni işin vardır; buna aşırı mühendislik denir. İkinci hata, denetimi **öznel** bırakmaktır: "kod kalitesini değerlendir" gibi bir istek, ölçülemediği için her turda farklı cevap üretir ve döngü hiç kapanmaz.

### Görevlere geçmeden önce
Bugün gerçek bir PR üzerinde döngüyü kur. Önce kontrol listesini yaz (doğruluk, güvenlik, dış sözleşme, test), sonra ajana bu listeye karşı incelet. En son, inceleyici prompt'una kapsam sınırını ekle ve **aynı PR'ı** tekrar incelet: bulgu sayısının nasıl düştüğünü ve kalanların gerçekten önemli olduğunu kendi gözünle gör.

## Görevler
- [ ] Bir PR'da ajana kendi işini bir inceleme kontrol listesine (doğruluk, güvenlik, dış sözleşme, test) karşı incelet
- [ ] Reviewer prompt'una 'sadece doğruluk/gereksinim etkileyenler' sınırını ekle
