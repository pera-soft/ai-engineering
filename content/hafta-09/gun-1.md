---
hafta: 9
gun: 1
baslik: "Neden katmanlı mimari yetmez"
---

## Ders

### Bugün ne öğreneceksin?
Bugün tek bir soruyu cevaplayacaksın: bir programın parçaları hangi sırayla birbirine yaslanmalı? Katmanlı (layered) mimarinin ne olduğunu, neden yıllarca varsayılan seçim sayıldığını ve Hombergs'in kitabının ona tam olarak hangi noktada itiraz ettiğini göreceksin. Günün sonunda "veritabanı-güdümlü tasarım" cümlesini kendi kelimelerinle anlatabilmelisin.

### Temel kavramlar
- **Mimari:** kodun nereye yazılacağını önceden belirleyen kurallar toplamı. Bina planı gibidir: her tuğlayı tek tek düşünmezsin, plan söyler.
- **Katman (layer):** benzer işi yapan sınıfların oluşturduğu şerit. Klasik dizilim: web (dış dünyayla konuşan) → iş mantığı (service) → veri erişimi (repository) → veritabanı.
- **Bağımlılık (dependency):** A sınıfı B'yi kullanıyorsa (`import` ediyor, çağırıyorsa) A, B'ye bağımlıdır; ok A'dan B'ye gider. B değişirse A kırılabilir, tersi geçerli değildir. Mimarideki en önemli soru budur: **oklar hangi yöne bakıyor?**
- **Alan (domain):** yazılımın gerçekten değerli kısmı, yani iş kuralları. "Bakiyeden fazlasını transfer edemezsin", "quiz sonucu eşiği geçerse hafta tamamlanır". Domain, hiçbir teknoloji seçilmeden de düşünülebilen kısımdır.
- **Hexagonal (altıgen) mimari:** domain'i merkeze koyan, veritabanını/HTTP'yi/e-postayı merkeze *takılan parçalar* olarak gören yaklaşım. Asıl adı "ports & adapters"tır; altıgen çizimi rastlantısaldır, kenar sayısının bir anlamı yoktur.

### Somut örnek (bu proje)
Bu projenin katman kuralı şudur: `types → config → repo → service → ai → web`. Ok soldan sağa **tek yönlüdür**: `web`, `service`'i çağırabilir; `service`, `web`'i asla çağıramaz. Bu kural bir belge değil, çalışan bir testtir (`MimariTestleri`) — ihlal edilirse build kırmızı yanar.

Şimdi tehlikeyi gör. Katmanlı bir projede işe genelde tablodan başlanır: önce `quiz_sonuc` tablosu, sonra ona birebir benzeyen bir entity, sonra o entity'yi taşıyan bir service, en sonda onu JSON'a çeviren bir controller. Bir süre sonra iş kuralı ("hafta tamamlandı mı?") artık bir `SELECT` sorgusunun `WHERE` cümlesinde yaşar. Kuralı okumak için SQL okuman, test etmek için veritabanı ayağa kaldırman gerekir. İş mantığı **altyapıya sızmıştır** (Hafta 5'in diliyle: yüksek coupling).

Katmanlı mimari bunu yasaklamaz; hatta kolaylaştırır, çünkü yığının en altı veritabanıdır ve her şey ona yaslanır. Hexagonal'ın yaptığı şey yığını devirip merkeze domain'i koymaktır.

### Sık yapılan hata
En yaygın yanılgı: "Ben zaten katman kullanıyorum, demek ki mimarim temiz." Temiz (clean) mimari **katman sayısıyla** ya da klasörleri düzgün adlandırmakla ilgili değildir; **okların yönüyle** ilgilidir. `service` paketinde JPA entity'si dolaşıyorsa klasör adı ne olursa olsun domain kirlenmiştir. İkinci yaygın hata: "Hexagonal, altı katman demek." Hayır — altıgen yalnızca bir çizim.

### Görevlere geçmeden önce
Elinde iki soru olsun: (1) bu kod parçası veritabanı olmadan çalıştırılıp test edilebilir mi? (2) iş kuralını okumak için bir çerçevenin (framework) belgelerine bakmam gerekiyor mu? Birine bile "hayır" diyemiyorsan orada sızıntı vardır. İkinci görevdeki iki örneği ararken bu iki soruyu kullan, bulduklarını birer cümleyle not et — yarın port/adapter ile onları nasıl temizleyeceğini göreceksin.

## Görevler
- [ ] İlk bölümleri oku
- [ ] Kendi kod tabanında 'domain'e sızmış altyapı' 2 örneği bul
