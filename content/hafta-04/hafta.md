---
hafta: 4
baslik: "Guardrail'lar: Mimari Zorlama + Güvenlik"
hedef: "ArchUnit (Java) ve import-linter (Python) ile katman kurallarını mekanikleştir; SAST kapılarını kur."
kaynak: "Kaynak: ArchUnit / import-linter docs + Veracode 2025 GenAI raporu"
---

## Quiz
### S1: Ajanlar bir repodaki kötü desenleri neden çoğaltır?
- [ ] Eğitim verisindeki kötü kodu olduğu gibi ezberden kopyaladıkları için
- [ ] AGENTS.md okunmadığı için; belge okunursa desen sorunu tümüyle kalkar
- [ ] Daha çok satır ürettikleri için hata olasılığı istatistiksel olarak artar
- [x] Repodaki mevcut kodu örnek aldıkları için; ihlal yeni örneğe dönüşür
> Açıklama: Ajan tabelaya değil patikaya bakar: bağlamındaki mevcut kod onun için "burada böyle yapılıyor" demektir. Bir kez ihlal edilmiş kural artık bir örnektir ve sonraki ajan onu çoğaltır — buna drift (sapma) denir. Belge okumak yardımcı olur ama yetmez; kuralın mekanik zorlanması gerekir.

### S2: ArchUnit'in Python ekosistemindeki karşılığı olan import-linter ne yapar?
- [ ] Kullanılmayan import satırlarını bulup dosyalardan otomatik olarak temizler
- [x] Katman/yasak/bağımsızlık sözleşmeleri tanımlar; ihlalde CI kırmızı yanar
- [ ] Modüllerin içe aktarma süresini ölçüp yavaş yüklenen paketleri raporlar
- [ ] Eksik bağımlılıkları tespit edip requirements.txt dosyasına kendisi ekler
> Açıklama: import-linter, `pyproject.toml` veya `.importlinter` içinde `layers` (katman yönü), `forbidden` (yasak bağımlılık) ve `independence` (birbirine değmeyen modüller) sözleşmeleri kurar; CI'da `lint-imports` bloklayıcı kapı olur. Java'da ArchUnit ne yapıyorsa aynısı — aynı fikir, farklı araç.

### S3: ArchUnit'te paketler arası döngüsel bağımlılığı yasaklayan yapı hangisidir?
- [x] `slices().matching(...).should().beFreeOfCycles()`
- [ ] `layeredArchitecture().whereLayer(...).mayOnlyBeAccessedByLayers(...)`
- [ ] `noClasses().that()...should().dependOnClassesThat()...`
- [ ] `classes().that()...should().onlyBeAccessed().byAnyPackage(...)`
> Açıklama: `slices()` paketleri dilimlere böler, aralarındaki bağımlılık grafiğini çıkarır ve `beFreeOfCycles()` ile halka arar (bu projede `MimariTestleri.dongusuz`). `layeredArchitecture()` katman **yönünü** zorlar, aynı seviyedeki iki paketin A↔B sarmalını görmez. `noClasses()`/`classes()` ise tek tek bağımlılık yasağı koyar.

### S4: "Slopsquatting" riski nedir?
- [ ] Ajanın bağımlılık sürümünü eskiye sabitlemesi ve güvenlik yamalarının gelmemesi
- [ ] Ajanın lisansı projeyle uyumsuz bir paketi bağımlılık listesine eklemesi
- [x] Ajanın uydurduğu paket adının saldırganlarca kaydedilip zararlı yayınlanması
- [ ] Ajanın aynı işi gören iki kütüphaneyi birden ekleyip derlemeyi şişirmesi
> Açıklama: Ajan var olmayan bir paket adı halüsine eder ("slop"); saldırgan bu adı gerçekten yayınlar; bir sonraki kurulum zararlı kodu çeker — klasik tedarik zinciri saldırısı. Veracode 2025: AI üretimi kodun %45'i güvenlik testlerinden kalıyor, Java'da bu oran %72. Bu yüzden her yeni bağımlılık doğrulanır ve ADR ister (AGENTS.md: ASK FIRST).

### S5: "İyi sensor" sayılan bir hata mesajı neyi içerir?
- [ ] İhlal eden çağrının tam yığın izini (stack trace) ve derleme günlüğünü
- [ ] Kuralın adını, ihlalin dosya yolunu ve satır numarasını; gerisi fazlalıktır
- [ ] Kuralı geçici olarak devre dışı bırakmak için çalıştırılacak komutu
- [x] Ne bozuldu, neden yanlış ve nasıl düzeltilir bilgisinin üçünü birden
> Açıklama: Böckeler'in kuralı — sensor çıktısı LLM için optimize edilirse ajan kendini düzeltir ve döngü insan beklemeden kapanır. Kör mesaj ("layer violation") ajanı tahmine iter; genelde testi devre dışı bırakmayı dener. Bu projede düzeltme talimatı, ArchUnit'in `.because(...)` cümlesiyle ihlal çıktısının içine taşınır.
