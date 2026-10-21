---
hafta: 8
baslik: "AI Engineering II: RAG, Fine-tune Kararı, Evaluation"
hedef: "Kitabın ikinci yarısı: RAG mimarisi, ne zaman fine-tune, AI-as-judge ile değerlendirme, maliyet/gecikme."
kaynak: "Kitap: AI Engineering (Huyen) — ikinci yarı"
---

## Quiz
### S1: Bir RAG sisteminde embedding modelini değiştirdin. Zorunlu adım nedir?
- [ ] Yalnızca sorgu tarafını yeni modele geçirmek yeterlidir
- [x] Tüm belgeleri yeni modelle baştan indekslemek gerekir
- [ ] Eski ve yeni vektörleri aynı depoda yan yana tutmak gerekir
- [ ] Yalnızca kaç sonuç döneceğini belirleyen k değeri ayarlanır
> Açıklama: Vektörler yalnızca kendi modelinin uzayında anlamlıdır; iki farklı modelin ürettiği sayılar karşılaştırılamaz. Sorgu yeni modelle, belgeler eski modelle gömülü kalırsa benzerlik skoru anlamsızlaşır ve arama hata vermeden alakasız sonuç döndürür. Bu yüzden embedding modeli değişimi = tüm koleksiyonun yeniden indekslenmesi.

### S2: Fine-tuning öncelikle ne için uygundur?
- [ ] Modele dünkü haber gibi güncel bilgiyi kalıcı olarak eklemek
- [ ] Modelin uydurma yapma ihtimalini tümüyle ortadan kaldırmak
- [x] Modele istenen davranışı, biçimi ve üslubu kalıcı kazandırmak
- [ ] Modelin bağlam penceresini kalıcı olarak genişletmek
> Açıklama: Bilgi için RAG, davranış ve biçim için fine-tune. Bilgiyi ağırlıklara gömmek bakımı zorlaştırır: kaynak değişince baştan eğitmen gerekir ve model kaynak gösteremez. Fine-tune uydurmayı azaltabilir ama bitirmez; bağlam penceresi ise modelin mimarisine bağlıdır, ince ayarla genişlemez.

### S3: AI-as-judge yaklaşımının bilinen tuzağı hangisidir?
- [ ] Her çalıştırmada birebir aynı puanı verip hiç sapmaması
- [ ] Yalnızca tek doğrulu, kapalı uçlu soruları puanlayabilmesi
- [ ] Değerlendirme maliyetini insanlı incelemenin üstüne çıkarması
- [x] Önce gelen cevabı ve kendi üslubuna benzeyeni kayırması
> Açıklama: Yargılayan da bir modeldir; yanlılıkları vardır. En bilinen ikisi pozisyon yanlılığı (önce sunulan cevaba yüksek puan) ve kendine benzeyeni sevme. Azaltma yolu: net rubrik, her puana gerekçe zorunluluğu, üreten ile yargılayanın ayrı çağrılar olması ve aynı çifti sıra değiştirilerek ikinci kez sormak.

### S4: Değerlendirmede "altın küme" (golden set) ne işe yarar?
- [x] Prompt değişince aynı örneklerle önce/sonra karşılaştırma yapılır
- [ ] Modelin eğitim verisine eklenip doğruluğunu kalıcı olarak yükseltir
- [ ] Kullanıcıdan gelen her yeni soruyu otomatik olarak yanıtlar
- [ ] Bağlam penceresini örneklerle doldurup token maliyetini düşürür
> Açıklama: Altın küme, beklenen cevabı elle onaylanmış sabit bir örnek listesidir. Prompt'u, modeli ya da parçalama ayarını değiştirdiğinde aynı kümeyi yeniden çalıştırıp puanı karşılaştırırsın; testin koda yaptığını bu küme AI çıktısına yapar. Küme sabit kalmazsa "iyileştim mi?" sorusu ölçülemez hâle gelir. Bu yayın öncesi (offline) ölçümdür; canlıdaki kullanıcı sinyalleri onu tamamlar, yerine geçmez.

### S5: Model kademeleme (cascading) nedir?
- [ ] Modelleri paralel çalıştırıp çıktılarının ortalamasını almak
- [ ] Her isteği en yetenekli modele gönderip kaliteyi sabitlemek
- [x] Ucuz modelle başlayıp yalnız gerektiğinde pahalıya yükseltmek
- [ ] Aynı modeli farklı sıcaklık değerleriyle art arda çağırmak
> Açıklama: Maliyet mühendisliğinin çekirdeği: isteğin zorluğuna göre model seç. Kolay istekler ucuz ve hızlı modelde biter; yalnızca zor ya da riskli olanlar pahalı modele yükselir. Bu projede de aynı kademe var: sohbet güçlü modelde, kısa yardımcı işler hızlı modelde çalışır.
