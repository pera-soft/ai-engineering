---
hafta: 7
baslik: "AI Engineering I: Stack, Modeller, Prompt"
hedef: "Chip Huyen'in kitabının ilk yarısı: AI stack, foundation model seçimi, prompt engineering disiplini."
kaynak: "Kitap: AI Engineering (Huyen, O'Reilly 2025) — Bölüm 1–5 civarı"
---

## Quiz
### S1: AI Engineering'i klasik ML Engineering'den ayıran temel fark nedir?
- [ ] Veriyi temizleme adımının tümüyle ortadan kalkmış olması
- [x] Hazır temel modeli adapte edip değerlendirmenin merkeze geçmesi
- [ ] Üretimde izleme ve ölçüm ihtiyacının büyük ölçüde azalması
- [ ] AI mühendisliğinin çok daha derin matematik bilgisi gerektirmesi
> Açıklama: Model artık senin ürettiğin değil, kiraladığın bir bileşen. İş, "en iyi modeli eğitmek"ten "hazır modeli işine uydurmak ve doğru ölçmek"e kaydı. Veri kalitesi ve izleme kaybolmaz — model kutunun içinde kaldığı için ölçüm daha da kritikleşir.

### S2: Model seçiminde yayınlanmış benchmark sonuçlarına neden tam güvenilmez?
- [ ] Benchmark sonuçları satıcılar tarafından tümüyle uydurulduğu için
- [ ] Benchmark çalıştırmak haftalar süren ve çok pahalı bir iş olduğu için
- [x] Test soruları eğitime sızmış olabilir; üstelik senin işine benzemez
- [ ] Lider tablosundaki modellere API üzerinden erişilemediği için
> Açıklama: İki ayrı sorun var. Kontaminasyon: model test sorularını eğitim sırasında görmüş olabilir, o zaman puan ezberi ölçer. İkincisi ve daha önemlisi: lider tablosu genel yeteneği ölçer, senin ürününün başarı ölçütünü değil. Karar, kendi örneklerinden kurduğun küçük değerlendirme kümesinden çıkmalı.

### S3: Prompt'u "mühendislik ürünü" yapan pratik hangisidir?
- [x] Prompt'u dosyada versiyonlayıp değişikliği sabit örneklerle sınamak
- [ ] Prompt'u her çağrıda doğaçlama yazıp iyi çıkan sürümü akılda tutmak
- [ ] Prompt'u olabildiğince uzatıp her ihtimali tek metne sığdırmak
- [ ] Prompt'u kaynak kodun içine sabit metin olarak gömüp gizlemek
> Açıklama: Prompt da koddur: dosyada durur, sürümlenir, incelenir, değişince aynı örneklerle yeniden sınanır. Bu projede `prompts/` dizini tam olarak bunun için var. Uzatmak tek başına kalite getirmez; kaliteyi getiren şey değişikliğin etkisini ölçebilmektir.

### S4: Yanlış cevabın maliyetinin felaket olduğu bir AI özelliğinde ne yapılır?
- [ ] Daha büyük ve pahalı bir model seçmek tek başına yeterlidir
- [ ] Arayüze "hatalı olabilir" uyarısı koymak sorumluluğu ortadan kaldırır
- [ ] Özellik iptal edilir; bu alanda AI hiçbir biçimde kullanılamaz
- [x] Riske orantılı savunma katmanı eklenir; kritik adımda insan onaylar
> Açıklama: Savunma derinliği riske orantılanır. Düşük riskte hız kazanır; yüksek riskte kaynağa dayandırma, çıktının makineyle doğrulanması ve son adımda insan onayı devreye girer. Büyük model hata oranını düşürür ama sıfırlamaz; uyarı yazısı ise mühendislik değil, sorumluluk kaydırmadır.

### S5: Bağlam penceresi (context window) dolduğunda ne olur?
- [ ] Model kalan metni kendi diskine kaydeder ve sonra hatırlar
- [x] Sığmayan eski kısım düşer; model onu artık göremez
- [ ] Model otomatik olarak daha geniş pencereli sürüme yükseltilir
- [ ] Ücretlendirme durur, çünkü fazla token okunmadan atılır
> Açıklama: Bağlam penceresi, modelin tek seferde okuyabildiği token sınırıdır. Taşan kısım sessizce kırpılır ve model onu hiç görmemiş gibi davranır. Modelin istekler arasında kalıcı hafızası yoktur — her çağrı sıfırdan okur; bu yüzden uzun sohbetlerde önemli bilgiyi yeniden vermek gerekir.
