---
hafta: 1
gun: 2
baslik: "Yol Göstericiler ve Sensörler (Guides/Sensors) çerçevesi"
---

## Ders

### Bugün ne öğreneceksin?
Dün "harness" diye bir şey olduğunu öğrendin. Bugün onu **ikiye** böleceksin: ajanı işten önce yönlendiren parçalar (guide) ve işten sonra ölçen parçalar (sensor). Bu ayrımı yapabilirsen, "ajan yine yanlış yaptı" cümlesini "hangi taraf eksikti?" sorusuna çevirebilirsin — ve bu soru düzeltilebilir bir sorudur.

### Temel kavramlar
- **Guide (yol gösterici) / ileri besleme:** ajanın işe başlamadan ÖNCE okuduğu metin. AGENTS.md, spec, kod şablonu, mimari belgesi. Belirleyici özelliği: **metindir** — ajan okur, uyabilir de **atlayabilir** de.
- **Sensor (sensör) / geri besleme:** iş BİTTİKTEN sonra çalışan, modelden bağımsız otomatik kontrol. Test, derleyici, tip denetimi (type check), mimari test, CI. Belirleyici özelliği: **koddur** — ajan atlayamaz.
- **CI (sürekli entegrasyon / continuous integration):** her değişiklikte testleri otomatik koşturan süreç. "Kapı" (gate) = geçemeyen değişiklik birleştirilemez.
- **Tip denetimi (type check):** kodun veri tiplerinin tutarlı olup olmadığını, kodu çalıştırmadan kontrol eden adım.

İnce nokta — **linter iki tarafa da düşebilir.** Kural dosyası (ör. checkstyle yapılandırması) bir guide'dır: ajana "böyle yaz" der. Aynı linter'ı CI'da bloklayıcı olarak koşturduğun anda sensor olur. Ayrımı belirleyen aracın adı değil, **ne zaman ve zorlayıcı çalışıp çalışmadığıdır.**

### Somut örnek
Bu deponun kendi harness'ını iki sütuna ayıralım:
- **Guide tarafı:** `AGENTS.md` (komutlar, sınırlar, "nerede ne var"), `ARCHITECTURE.md` (katman tablosu), `prompts/` altındaki şablonlar, `docs/adr/` altındaki karar kayıtları.
- **Sensor tarafı:** `./mvnw verify` ile koşan birim testleri, ArchUnit `MimariTestleri` (katman yönü ve "Claude çağrısı yalnız ai/ paketinden" kuralı), derleyicinin kendisi.

Dikkat et: katman kuralı **iki kez** yazılmış — hem AGENTS.md'de bir cümle olarak (guide) hem `katman_kurali` adlı bir testte (sensor). Bu tekrar israf değil, tasarımdır:
- Yalnız **guide** kurarsan, ajan uzun bir bağlamda o cümleyi unutur ya da atlar; kimse fark etmez.
- Yalnız **sensor** kurarsan, ajan kuralın *sebebini* bilmediği için testi geçecek en ucuz numarayı arar; kuralın ruhunu değil harfini uygular.
- İkisi birlikte: guide niyeti anlatır ("neden böyle"), sensor kritik olanı garanti eder ("atlayamazsın").

### Sık yapılan hata
Yeni başlayan, ajan hata yapınca refleks olarak **guide tarafına bir cümle daha ekler**. Bir hafta sonra AGENTS.md üç yüz satırdır ve ajan hâlâ aynı hatayı yapmaktadır — çünkü sorun bilgi eksikliği değil, **zorlama** eksikliğiydi. Kural şu: bir hata TEKRAR ediyorsa çözüm yeni bir guide cümlesi değil, yeni bir sensor'dur.

İkinci hata, guide'a ajanın zaten bildiği genel iyi pratikleri yazmak ("okunaklı kod yaz", "hata yönetimi ekle"). Guide'a yalnız ajanın kendi başına **tahmin edemeyeceği** şeyi yaz: bu projeye özgü kararlar, sınırlar, komutlar.

### Görevlere geçmeden önce
Aşağıdaki üç görev tam olarak bu çerçeveyi bir envantere çevirir: önce guide'ları, sonra sensor'ları listeleyeceksin, en sonda her listedeki **en büyük boşluğu** işaretleyeceksin. O iki boşluk, bu programda kuracağın ilk iki harness parçasının adresidir.

## Görevler
- [ ] Kendi (ya da bildiğin bir) projede mevcut guide'ları listele (varsa)
- [ ] Mevcut sensor'ları listele: test, lint, CI kapıları
- [ ] Her listede en büyük 1 boşluğu işaretle
