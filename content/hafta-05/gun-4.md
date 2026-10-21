---
hafta: 5
gun: 4
baslik: "Mimari fitness fonksiyonları"
---

## Ders

### Bugün ne öğreneceksin?
Bugün bir mimari kuralı **dilekten kontrole** çevirmeyi öğreneceksin. Gün 2'de nitelikleri adlandırdın, Gün 3'te bağları adlandırdın; bugün bunları **her derlemede kendiliğinden sınanan** koda dönüştürüyorsun.

### Temel kavramlar
- **Fitness function (uygunluk fonksiyonu):** Bir mimari niteliği **sürekli ve mekanik** biçimde ölçen otomatik kontrol. Kilit sözcük "sürekli": tek seferlik denetim değil, her derlemede koşan bir sınamadır.
- **Neden bu ad?** Terim evrimsel hesaplamadan gelir: bir adayın hedefe ne kadar "uygun" olduğunu ölçen fonksiyon. Mimaride hedef, seçtiğin niteliğin **bozulmadan kalmasıdır**.
- **Guardrail (koruma bariyeri):** Yolun kenarındaki bariyer benzetmesi. Bariyer sana nereye gideceğini söylemez; **uçuruma gitmeni engeller.** Fitness function da tasarım yapmaz, tasarımın bozulmasını engeller.
- **Aynı fikrin üç adı:** Hafta 1'de **"Sensor"**, Hafta 4'te **"guardrail / mimari test"**, bugün **"fitness function"**. Üçü de aynı ailedendir; kitap kavramı ile kendi harness pratiğini birleştir.
- **Atomik / bütünsel ayrımı:** *Atomik* fitness function tek bir kuralı sınar (ör. "web katmanı repo'ya erişemez"). *Bütünsel* olan birden çok niteliğin birlikte davranışını sınar (ör. şifreleme açıkken yanıt süresi hâlâ eşiğin altında mı).

Bir kural üç durumdan birindedir: **kafada** (kimse bilmez), **belgede** (bilinir, unutulur), **testte** (bozulunca kırmızı yanar). Yalnız üçüncüsü mimaridir; ilk ikisi temennidir.

### Somut örnek (bu projeden)
`src/test/java/tr/velzon/aiogretmen/MimariTestleri.java` dosyasının tamamı fitness function'lardan oluşur. Her kuralın koruduğu bir nitelik vardır:

| Kural | Koruduğu nitelik |
|---|---|
| `katman_kurali` — `types → config → repo → service → ai → web` yönü | **maintainability**: bağımlılık tek yöne akar, döngü oluşmaz |
| `claude_yalniz_ai_paketinden` — LLM çağrısı yalnız `ai/` paketinden | **değiştirilebilirlik + maliyet denetimi**: sağlayıcı değişince tek paket değişir |
| `types_bagimsiz` — `types` hiçbir üst katmana bağlanamaz | **testability**: en alt katman tek başına test edilebilir |
| `mcp_llmsiz` — MCP adaptörü Spring AI'yı çağıramaz | **öngörülebilirlik**: dış istemci, sunucuda gizli LLM maliyeti doğuramaz |

Dikkat et: bu kurallar Gün 3'ün diliyle **statik connascence'ı sabitler** — bağın *yönünü* kodda görünür kılar. Yani mimari test, bağı yok etmez; onu **derleyici seviyesinde konuşulur** hâle getirir.

Bir de karşı örnek: "Yanıt 200 ms'de dönsün" bir nitelik hedefidir ama bu projede onu koruyan bir fitness function **yoktur**. Yani o hedef bugün yalnızca temennidir. Eksik olanı görmek de bu dersin parçasıdır.

### Sık yapılan hata
- **Test yerine belge yazmak.** `ARCHITECTURE.md`'de "web katmanı repo'ya erişmez" yazması hiçbir şeyi engellemez; ArchUnit kuralı engeller.
- **Kural kırılınca kuralı gevşetmek.** Kırmızı yanan test bir engel değil, bir bilgidir: ya bağımlılığı ters çevir ya işi doğru katmana taşı. Gerçekten yeni bir katman gerekiyorsa **önce ADR aç** (bu projedeki yazılı kural budur).
- **Ölçülemeyen niteliğe fitness function aramak.** "Kod temiz olsun" sınanamaz; "hiçbir sınıf 400 satırı geçmesin" sınanır. Önce niteliği ölçülebilir cümleye çevir.
- **Yalnız kuruluşta koşan kontrol.** Ayda bir elle koşulan denetim fitness function değildir; her derlemede koşmayan kontrol zamanla susar.

### Görevlere geçmeden önce
İlk görevde her mevcut testin yanına tek kelimeyle koruduğu niteliği yaz (yukarıdaki tabloyu şablon olarak kullanabilirsin). İkinci görevde önce **eksik niteliği** seç, sonra onu ölçülebilir bir cümleye çevir, en sonunda testin nasıl olacağını taslakla. Fikir yeter, kodu yazmak zorunda değilsin — ama fikir şu üç boşluğu doldurmalı: **"… niteliğini korumak için, … koşulunu, … anında sınayacağım."**

## Görevler
- [ ] Mevcut mimari testlerini 'hangi karakteristiği koruyor?' diye etiketle
- [ ] Eksik kalan 1 karakteristik için yeni fitness function fikri yaz
