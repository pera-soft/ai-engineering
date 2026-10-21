---
hafta: 1
baslik: "Harness Mühendisliği Temelleri"
hedef: "Rolün nasıl değiştiğini kavra: kod yazan değil, ortam/niyet/geri-besleme tasarlayan mühendis."
kaynak: "Kaynak: OpenAI 'Harness Engineering' yazısı + martinfowler.com (Böckeler) 'Agent = Model + Harness'"
---

## Quiz
### S1: OpenAI deneyinde insan mühendislerin birincil işi neye dönüştü?
- [ ] Ajanın ürettiği kodu satır satır elle gözden geçirip yeniden yazmak
- [x] Ortam, niyet ve geri-besleme döngüsü tasarlamak
- [ ] Aynı görevi daha iyi prompt'larla tekrar tekrar denemek
- [ ] Kodlamayı tümüyle bırakıp yalnızca ürün önceliklendirmesi yapmak
> Açıklama: Yazının özü 'design environments, specify intent, and build feedback loops'. Bir iş başarısız olunca soru 'daha çok deneyelim mi' değil, 'hangi yetenek eksikti ve bunu ajan için nasıl okunur ve zorlanabilir kılarız' olur. Önceliklendirme bu işin yalnızca bir parçasıdır.

### S2: 'Guide' ile 'Sensor' arasındaki fark nedir?
- [ ] Guide sonucu ölçer, sensor işi baştan yönlendirir
- [x] Guide iş başlamadan yönlendirir, sensor iş bitince ölçer
- [ ] İkisi de iş bittikten sonra denetler; fark yalnızca kullanılan araçtadır
- [ ] Guide insanlar için, sensor ajanlar için yazılır
> Açıklama: Guide ileri beslemedir (AGENTS.md, spec, kod şablonu): metindir, ajan okur — uyabilir de atlayabilir de. Sensor geri beslemedir (test, derleyici, ArchUnit mimari testi): koddur, modelden bağımsız çalışır, atlanamaz.

### S3: 'Ajan okunabilirliği' ilkesine göre Slack'te kalan bir mimari karar…
- [ ] Ajan gerektiğinde Slack'i arayıp o kararı bulabilir
- [ ] Ajan için sorun değildir; model doğru kararı zaten tahmin eder
- [x] Ajan için yok hükmündedir; repoya markdown olarak girmelidir
- [ ] Her prompt'un başına elle kopyalanarak çözülür
> Açıklama: Ajanın çalışırken bağlamından erişemediği bilgi fiilen yoktur — tıpkı üç ay sonra işe girecek birine görünmez olacağı gibi. Prompt'a elle kopyalamak tek seferlik bir yamadır: unutulur, sürümlenmez, ekip arkadaşına geçmez.

### S4: Ajanların kendi işini değerlendirmesindeki bilinen sorun nedir?
- [ ] Kendi çıktılarını okuyamadıkları için değerlendirme yapamamaları
- [ ] Yalnızca biçim hatalarını görüp mantık hatalarını hiç görememeleri
- [ ] Aynı oturumda değerlendirmenin maliyeti ikiye katlaması
- [x] Kendi işlerini kendinden emin bir dille övmeleri
> Açıklama: Anthropic'in ölçtüğü davranış: 'agents tend to respond by confidently praising the work'. Sorun yetenek ya da maliyet değil, tek yöne sapan bir yanlılıktır; bu yüzden işi YAPAN ajan ile YARGILAYAN ajan ayrılır.

### S5: Harness bileşenleri için 'rippable' (sökülebilir) olma kuralı ne anlama gelir?
- [ ] Harness bir kez kurulur; model değişse de olduğu gibi korunmalıdır
- [ ] Model her güçlendiğinde harness sıfırdan yeniden yazılmalıdır
- [x] Yük taşımayan parçalar, varsayımları geçersizleşince çıkarılmalıdır
- [ ] Harness parçaları birbirine hiç bağlanmamalı, tümü bağımsız olmalıdır
> Açıklama: Her harness parçası modelin bugünkü bir eksiğini varsayarak vardır; varsayım bayatlayınca parça yük taşımayı bırakır. Örnek: model uzun dosyaları güvenilir özetleyemediği için kurduğun parçalama adımı, bağlam penceresi büyüyünce gereksizleşir — sil. 'Sökülebilir', ne 'sıfırdan yaz' ne de 'modüler ol' demektir.
