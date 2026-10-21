---
hafta: 12
baslik: "Liderlik, Mentorluk ve Capstone"
hedef: "Junior mentorluk sistemini kur, ekip anlaşmasını yayınla, mimari kata ile her şeyi birleştir."
kaynak: "Kaynak: Anthropic 'AI assistance & coding skills' RCT (29 Oca 2026) özeti + bu haftanın Türkçe şablonları (Liderlik ve Mentorluk Şablonları, kaynak kutusu) + Neal Ford kata formatı + trade-off tekniği (capstone'da uygula)"
---

## Quiz
### S1: Anthropic'in beceri çalışmasında juniorların kavrama puanını asıl belirleyen şey neydi?
- [ ] AI'ı hiç kullanmamak; yardımcıdan uzak duran grup en yüksek kavrama puanını aldı
- [ ] Göreve harcanan toplam süre; aynı işe daha çok saat ayıranlar daha iyi kavradı
- [x] AI'ın kullanılma biçimi; çıktıyı sorgulayanlar tam delege edenleri geride bıraktı
- [ ] Seçilen modelin gücü; daha yetenekli modeli kullananlar konuyu daha iyi kavradı
> Açıklama: Tam delege edenlerde kavrama %40'ın altı, kavramsal sorgulama (Conceptual Inquiry) yapanlarda %65 ve üzeri. Belirleyici olan AI'ı kullanıp kullanmadığın ya da hangi modeli seçtiğin değil, nasıl kullandığın: cevap makinesi mi, düşündüren öğretmen mi?

### S2: 'Commit eden sahiptir' kuralı pratikte ne anlama gelir?
- [x] Kodu gönderen kişi, üreteni AI olsa bile sonucun tüm sorumluluğunu taşır
- [ ] AI'ın ürettiği satırlar commit mesajında etiketlenir; sorumluluk böylece paylaşılır
- [ ] Bir dosyayı yalnızca onu ilk yazan kişi değiştirebilir; sahiplik dosyaya kilitlenir
- [ ] AI çıktıları ayrı bir gözden geçirme sırasına alınır; insan kodu öncelik kazanır
> Açıklama: Sorumluluk devredilemez; "AI yazdı" bir savunma değildir. Kural iki şeyi birden korur: kaliteyi (gönderen anlamak zorundadır) ve junior'ın öğrenme zorunluluğunu. Sahiplik, dosyayı kilitlemek değil, sonuca kefil olmaktır.

### S3: Suçsuz (blameless) postmortem'in amacı nedir?
- [ ] Hatanın sorumlusunu net biçimde belirleyip tekrarı caydırıcı sonuçlara bağlamak
- [ ] Olayı kayda geçirmeden kapatarak ekibin moralini ve teslim hızını korumak
- [ ] Kök nedeni tek bir kişinin kararına indirip düzeltmeyi ona görev olarak vermek
- [x] Kişiyi değil sistemi sorgulamak; hatayı mümkün kılan koşulları bulup değiştirmek
> Açıklama: İnsanlar cezalandırılacağını düşünürse bilgi saklar ve öğrenme durur. Suçsuzluk "kimse sorumlu değil" demek DEĞİLDİR; soruyu "kim yaptı?"dan "bu hatayı mümkün kılan neydi, hangi korkuluğu koyarız?"a çevirmektir. Olayı hiç yazmamak ise en pahalısıdır: aynı hata geri gelir.

### S4: Hangi işlemler ajan otonomisinin dışında tutulup her durumda insan onayına bağlanmalıdır?
- [ ] Test eklemek ve README güncellemek gibi ekipte sık tekrarlanan rutin işler
- [x] Şema göçü, dış sözleşme değişikliği, veri silme gibi geri dönüşü zor işler
- [ ] Değişken adlandırma ve biçimlendirme gibi okunabilirliği etkileyen işler
- [ ] Bağımlılık sürümü yükseltmek gibi derleme çıktısını değiştirebilen işler
> Açıklama: Ölçüt "önemli mi" değil, GERİ ALINABİLİR Mİ. Geri alınabilen hata ucuzdur, geri alınamayan hata pahalıdır. Ayrıca AGENTS.md gibi talimatlar yanlış işin ihtimalini azaltır, imkânını kaldırmaz; imkânı kaldıran şey politika ve sandbox'tır.

### S5: Mimari kata'nın temel eğitim değeri nedir?
- [ ] Kod yazma hızını artırması; aynı problemi tekrarlayıp yazma refleksi kazandırması
- [ ] Doğru mimari desenleri ezberletip standart çözümü hatırlamayı kolaylaştırması
- [x] Belirsizlik altında trade-off tartışıp verilen kararı savunma pratiği yaptırması
- [ ] Gerçek projedeki mimari kararları düşük riskle önceden onaylatma imkânı vermesi
> Açıklama: Kata'da kod yazılmaz: eksik bilgiyle karar verilir, sonra o karar çapraz sorguya karşı savunulur — mimarın gerçek işi budur. Gerçek proje kararını onaylatmak için değil, düşük riskli ortamda tekrar tekrar pratik yapmak için kullanılır.
