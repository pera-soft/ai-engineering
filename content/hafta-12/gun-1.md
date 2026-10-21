---
hafta: 12
gun: 1
baslik: "Beceri körelmesi: kanıt ve panzehir"
---

## Ders

### Bugün ne öğreneceksin?
AI yardımcıları juniorların öğrenmesini neden bazen hızlandırıp bazen durdurduğunu — ve bunun AI'ı kullanıp kullanmamakla değil, **nasıl** kullandığınla ilgili olduğunu. Sonunda ekibine koyabileceğin somut bir eşiğin olacak.

### Temel kavramlar
- **Mentorluk:** Birinin yerine iş yapmak değil, o kişinin **kendi başına karar verebilir hâle gelmesini** sağlamak. Cevabı vermek kısa vadede hızlıdır; soruyu sordurmak uzun vadede ekip üretir.
- **Beceri körelmesi (skill atrophy):** Bir yeteneğin, kullanılmadığı için zamanla zayıflaması. Kod bağlamında: iş teslim edilir, ama teslim edenin kafasında sistemin **zihinsel modeli** oluşmaz.
- **Tam delege (full delegation):** Görevi AI'a verip çıktıyı okumadan kabul etmek.
- **Conceptual Inquiry (kavramsal sorgulama):** AI'ı cevap makinesi değil, "neden böyle?" diye düşündüren bir öğretmen gibi kullanmak.
- **Üretken zorlanma (productive struggle):** Öğrenmenin, biraz zorlanmadan gerçekleşmediği gerçeği. Zorlanmayı ortadan kaldıran her kısayol, öğrenmeyi de kaldırır.

Benzetme: tam delege, spor salonuna gidip **ağırlıkları başkasına kaldırtmaktır**. Ağırlık kalkar, kayıtta "20 tekrar" yazar — ama senin kasın büyümez. Conceptual Inquiry ise antrenörle çalışmaktır: ağırlığı **sen** kaldırırsın, antrenör "neden bu duruş, bu hareketin riski ne?" diye sorar. Daha yorucudur; büyüyen kas seninkidir.

### Somut örnek
Aynı görevi alan iki junior: *"Sipariş servisine yeniden deneme (retry) mantığı ekle."*

**Junior A — tam delege.** AI'a yazar, çıkan kodu okumadan kopyalar, testler yeşil, merge. İş **20 dakikada** biter. Ama "neden üstel geri çekilme (exponential backoff) var?" sorusuna cevabı yoktur; retry'ın idempotent olmayan bir çağrıda **aynı siparişi iki kez** oluşturabileceğini fark etmemiştir. Kavrama sınavında %40'ın altı. Üç ay sonra gece 03:00'te alarm çaldığında, **kendi merge ettiği** kodun neden öyle davrandığını açıklayamaz: elinde kod var, model yok.

**Junior B — Conceptual Inquiry.** Aynı görevi AI'a verir ama çıktıyı sorguya çeker: *"Bunu neden böyle yaptın? Alternatifi neydi? Hangi durumda patlar? Çalışmadığını nereden anlarım?"* AI cevaplar: sabit aralıklı retry, zaten boğulmuş servisi aynı ritimde döverek çöküşü derinleştirir (retry storm). Sonra Junior B **kendi cümleleriyle**, ekrana bakmadan anlatır: "Retry geçici hatayı kurtarır ama servis yüklüyse yükü artırır; o yüzden aralık büyür. Çağrı idempotent değilse çift kayıt olur; o yüzden istek anahtarı gerekir." Aynı iş **45 dakika** sürer — iki katı. Kavrama %65+. Üç ay sonra alarmı **açıklayabilir**.

| | Junior A | Junior B |
|---|---|---|
| AI'ın rolü | Cevap makinesi | Düşündüren öğretmen |
| Doğrulama | Testler yeşilse tamam | Kendi cümleleriyle anlatabiliyorsa tamam |
| Kısa vade | Hızlı (20 dk) | Yavaş (45 dk) |
| Uzun vade | Sisteme sahip değil | Sisteme sahip |

### Sık yapılan hata
Yanlış teşhis: "AI juniorları bozuyor, kullanımı kısıtlayalım." İkisi de AI kullandı; fark **kullanma biçimindeydi**. Yasak, hem gerçekçi değildir hem de yanlış yeri hedefler. İkinci hata, hızı öğrenme sanmaktır: 20 dakikada biten iş, üç ay sonra saatlerce süren bir arıza olarak geri döner.

### Görevlere geçmeden önce
Ekibine koyacağın eşik tek cümledir: **"Merge ettiğin kodu kendi cümlelerinle anlatamıyorsan, o kod merge edilmez."** Bu bir ceza değil, bir öğrenme kapısıdır. Not: bu programın kendisi de aynı ilkeyle çalışır — seni yönlendirir, düşünmeyi ve öz-anlatımı sana bırakır. Junior'larına da aynısını yap.

## Görevler
- [ ] Çalışmanın bulgularını ekip sunumu için 5 slayta indir
- [ ] Bu haftanın kaynaklarındaki 'Haftalık Junior Mentorluk Akışı' şablonunu kendi ekibine uyarla
