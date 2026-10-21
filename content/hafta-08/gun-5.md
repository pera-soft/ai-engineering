---
hafta: 8
gun: 5
baslik: "Sentez + quiz"
---

## Ders

### Bugün ne öğreneceksin?
İki haftalık AI mühendisliği bilgisini tek bir **karar ağacına** indiriyoruz. Amaç ezber değil: önüne yeni bir AI fikri geldiğinde, hangi soruyu hangi sırayla soracağını bilmek. Günün sonunda elinde çizilmiş bir ağaç ve çözülmüş bir quiz olacak.

### Temel kavramlar
Karar ağacının gövdesi beş sorudur; sırası önemlidir çünkü her basamak bir öncekinden pahalıdır:

1. **Bu iş gerçekten AI istiyor mu?** Kural, arama ya da düz kod yetiyorsa AI koymak, çözülmüş bir problemi belirsiz hâle getirmektir. İlk ve en çok atlanan soru budur.
2. **Prompt yeter mi?** Net talimat + biçim + 2-3 örnek. Vakaların çoğu burada biter. (Hafta 7, Gün 3)
3. **Model bir şeyi BİLMİYOR mu? → RAG.** Şirket belgesi, güncel veri, kişiye özel not. Boru hattı: chunk → embed → ara → (gerekirse yeniden sırala) → bağlama koy → cevapla. (Gün 1)
4. **Model biliyor ama İSTEDİĞİM GİBİ DAVRANMIYOR mu, ve prompt çare olmadı mı? → Fine-tune.** Elinde temiz eğitim verisi ve onu güncel tutacak biri varsa. (Gün 2)
5. **Nasıl ölçeceğim?** Altın küme + rubrik + bağımsız judge; yayın öncesi (offline) ölçüm, canlı sinyalle (online) tamamlanır. (Gün 3)

Ağaca bir de **çalışma anı** dalı ekle: seçilen çözüm ne kadar tutuyor, ne kadar bekletiyor? Bağlamı kırp, değişmeyen kısmı önbelleğe al, kolay adımları ucuz modele indir. (Gün 4)

İki haftanın tek cümlesi şudur: **hazır modeli al, en ucuz adaptasyonla başla, her adımı ölç.** Dikkat et — ağacın hem başında hem sonunda ölçüm var. Ölçmediğin bir iyileştirme, iyileştirme değil, tahmindir.

Kendini şu dört soruyla sına: (1) Bilgi eksikliğiyle davranış eksikliğini nasıl ayırt ederim? (2) Embedding modelini değiştirirsem ne yapmam gerekir? (3) Judge'ın en bilinen iki yanlılığı nedir ve nasıl azaltılır? (4) Faturayı düşürmenin ilk hamlesi hangisidir?

### Somut örnek
Ağacı bu projenin kendi kararları üzerinden okuyabilirsin: öğretmenin üslubu **prompt** ile kuruldu (`prompts/`), kitap notlarına erişim **RAG** ile çözüldü (ADR-0005), re-ranking bilinçli olarak **eklenmedi** çünkü koleksiyon küçük, model **kademelendi** (güçlü/hızlı), bağlam ise sabit bir sınırda kırpılıyor. Fine-tune ise hiç yapılmadı — çünkü üslup hâlâ değişiyor ve prompt'ta düzeltmek bir dosya değişikliği. Kendi ağacını çizerken her dala böyle bir "neden bu, neden şu değil" notu düş; karar belgesini değerli kılan şey seçilen yol değil, **elenen yolun gerekçesidir.**

### Sık yapılan hata
- **Ağacı en alttan başlatmak.** Toplantılar çoğu zaman "fine-tune mu yapsak?" diye açılır; doğru açılış "prompt'u düzgün yazdık mı?"dır.
- **Ölçüm dalını çizmemek.** Ağaç "RAG kuralım" diye biterse, kurduğun şeyin işe yarayıp yaramadığını asla bilemezsin.
- **RAG ile fine-tune'u alternatif sanmak.** Sık sık birlikte kullanılırlar: bilgi RAG'dan gelir, biçim fine-tune'dan.
- **Quiz'i puan için çözmek.** Yanlış yaptığın soru, hangi güne döneceğini söyleyen tek işarettir; doğru şıkkı bulduktan sonra diğer üçünün neden yanlış olduğunu da bir cümleyle yaz.

### Görevlere geçmeden önce
Ağacı çizerken her düğüme **karar kriterini** yaz, sadece seçeneği değil: "prompt yeter mi?" değil, "altın kümede %90'ın üstünde mi?" Kriteri olmayan düğüm, karar değil tartışma başlığıdır. Çizimi `docs/design-docs/` altına koyduktan sonra quiz'e geç.

## Görevler
- [ ] Karar ağacını çiz ve docs/design-docs/ altına koy
- [ ] Haftalık quiz'i çöz
