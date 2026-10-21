---
hafta: 1
gun: 1
baslik: "'Humans steer, agents execute' ilkesi"
---

## Ders

### Bugün ne öğreneceksin?
Bugünün tek hedefi şu: yazılım mühendisinin işi "kod yazmak"tan "ajanın güvenilir çalışacağı ortamı kurmak"a kayıyor — ve bu ortamın adı **harness**. Bunu bilmezsen ajanla çalışırken ömrün çıktı düzeltmekle geçer; bilirsen düzeltme işini sistemin kendisine yaptırırsın. Sloganı ezberle: *"Humans steer. Agents execute."* (İnsanlar yönlendirir, ajanlar uygular.)

### Temel kavramlar
- **Ajan (agent):** senin adına adım adım iş yapan, kendi kararıyla dosya okuyup yazan, komut çalıştıran yazılım. Sohbet botundan farkı: cevap vermekle kalmaz, **uygular**.
- **Harness (koşum takımı):** ajanın etrafına kurduğun ortam — kurallar, örnekler ve otomatik kontroller. Kelime at arabasındaki dizgin/kayış takımından gelir ve benzetme tam yerinde: **arabayı sen itmezsin; gücü zaten olan atı yönlendirirsin.**
- **Guide (yol gösterici):** iş BAŞLAMADAN önce ajana "ne yapacağını ve neyin doğru sayıldığını" söyleyen metin. Örnek: AGENTS.md, spec, kod şablonu.
- **Sensor (sensör):** iş BİTTİKTEN sonra sonucu ölçen ve hatayı ajana **anlatan** otomatik kontrol. Örnek: test, derleyici, CI.
- **Spec (spesifikasyon):** yapılacak işin ve "bitti" sayılma koşulunun yazılı hâli.

### Somut örnek
Şu an okuduğun uygulamanın kendi deposundan, iki yarımın birlikte çalışması:
- **Guide:** Depo kökündeki `AGENTS.md` şunu yazar: *"katman yönü: types → config → repo → service → ai → web"* ve *"Claude çağrısı yalnız ai/ paketinden"*. Ajan kod yazmadan **önce** bu dosyayı okur.
- **Sensor:** `src/test/java/tr/velzon/aiogretmen/MimariTestleri.java` (ArchUnit) aynı kuralı **mekanik** olarak ölçer. Ajan web katmanından doğrudan repo'ya erişirse `./mvnw verify` kırmızı yanar ve testin adı hatayı söyler: `katman_kurali`.
- **Döngü:** ajan kodu yazar → test patlar → ajan hata metnini okur → kendi düzeltir → yeşil. Sen katman kuralını elle kontrol etmezsin; kural mekanik olarak korunur.

Bu döngünün ölçekli hâli OpenAI'ın deneyidir: üç mühendis boş bir depodan başlayıp beş ayda ~1 milyon satırlık gerçek bir ürünü **tek satır elle kod yazmadan** çıkardı. Değerli olan sayı değil, insanların hangi işe kaydığı: önceliklendirme, spec yazma, doğrulama, harness iyileştirme. Kod incelemesi bile büyük ölçüde ajandan ajana yapıldı — yazının esprili adıyla **"Ralph Wiggum döngüsü"**: inceleme–düzeltme, tüm inceleyiciler tatmin olana dek sabırla yinelenir. İnsan buraya yalnız yargı gerektiğinde girer.

### Sık yapılan hata
Ajan yanlış yaptığında refleks olarak **"bir daha dene", "prompt'u güzelleştir"** demek. Bu, aynı ortamda aynı hatayı yeniden üretir. Doğru soru şudur: *"Hangi yetenek eksikti ve bunu ajan için hem **okunur** (bir yerde yazılı) hem **zorlanabilir** (otomatik kontrol edilebilir) nasıl yaparım?"* Yani tek tek hataları düzeltmezsin — hatayı bir daha yapamayacağı ortamı kurarsın. Deneyin başında ilerlemenin yavaş olması da normaldi; sebebi ajanın yetersizliği değil, ortamın henüz tanımlanmamış olmasıydı.

### Görevlere geçmeden önce
Aşağıdaki görevler kavramı senin durumuna indirmek içindir: önce kaynağı okuyacaksın, sonra yeni rolü kendi cümlelerinle yazacaksın (kavramı gerçekten anladığın ancak orada belli olur), en sonda hâlâ elle yaptığın işleri listeleyeceksin — o liste, bu programın geri kalanında kuracağın harness'ın ilk malzemesidir.

## Görevler
- [ ] OpenAI Harness Engineering yazısını oku (belgedeki metin sende var)
- [ ] Kendi cümlelerinle 5 maddede 'benim yeni rolüm ne?' yaz
- [ ] Kendi (ya da bildiğin bir ekibin) iş akışında insanların hâlâ elle yaptığı 3 işi listele
