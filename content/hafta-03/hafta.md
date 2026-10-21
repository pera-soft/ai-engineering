---
hafta: 3
baslik: "Spec-Driven Akış ve Ajan Yönetimi"
hedef: "İyi task spec yazmayı ve Research → Plan → Execute → Review → Ship akışını kurmayı öğren."
kaynak: "Kaynak: OpenAI Codex best practices (Goal/Context/Constraints/Completion) + Anthropic plan modu"
---

## Quiz
### S1: İyi bir task spec'i (görev tarifi) hangi dört unsur oluşturur?
- [ ] Başlık, sorumlu kişi, teslim tarihi, öncelik etiketi
- [ ] Problem tanımı, çözüm önerisi, maliyet ve süre tahmini
- [x] Hedef, bağlam, kısıtlar ve bitiş kriteri
- [ ] Kod, test, dokümantasyon ve dağıtım (deployment) adımları
> Açıklama: OpenAI Codex best practices'in çekirdeği: Goal, Context, Constraints, Completion Criteria. Bitiş kriteri mekanik doğrulanabilir olmalı — "düzgün çalışsın" değil, "`./mvnw verify` yeşil". Her görevde geçerli olan kalıcı kurallar spec'e değil AGENTS.md'ye yazılır (Hafta 2); spec yalnız o göreve özgü kısıtları taşır.

### S2: Bağlam dolduğunda "compaction" ile "context reset" arasındaki fark nedir?
- [x] Compaction aynı oturumu özetleyip sürdürür; reset devir notuyla yeni oturum açar
- [ ] Compaction modelin bağlam penceresini büyütür; reset onu varsayılana döndürür
- [ ] İkisi de aynı işlemin adıdır; biri araçların, diğeri kullanıcıların terimidir
- [ ] Compaction sohbeti diske yedekler; reset o yedeği yeni oturumda geri yükler
> Açıklama: Compaction = aynı oturumda bağlamı yerinde özetleyip küçültmek (ucuz, ama ayrıntı kaybeder). Context reset = temiz bir devir notu bırakıp sıfırdan oturum açmak (temiz, ama notu yazmak sana iş çıkarır). Hiçbiri pencere boyutunu değiştirmez; pencere sabittir, yönetilen şey içine ne koyduğundur.

### S3: ExecPlan'in altın kuralı nedir?
- [ ] Plan bir kez onaylandıktan sonra iş bitene kadar hiç değiştirilmemelidir
- [ ] Plan yalnız adımları taşır; kararların gerekçesi sohbet geçmişinde kalır
- [ ] Doğrulama komutları plana yazılmaz, uygulama sırasında ekipçe belirlenir
- [x] Hiçbir şey hatırlamayan yeni bir oturum yalnız planı okuyup devam edebilmeli
> Açıklama: "Amnezi testi": planı hiçbir bağlamı olmayan bir ajana ver — kaldığın yerden devam edebiliyor mu? ExecPlan yaşayan belgedir: durum, adımlar ve karar gerekçeleri iş ilerledikçe güncellenir. Gerekçe planda değilse sonraki oturum aynı yanlışı yeniden yapar.

### S4: İnceleyici (reviewer) ajana kapsam sınırı koymazsan ne olur?
- [ ] Bulgu üretmez; yalnızca "onaylandı" der ve incelemeyi hızlandırır
- [x] İş sağlam olsa bile bulgu üretir; aşırı mühendislik önerileri doğar
- [ ] Yalnız biçim ve isimlendirme uyarıları verir, mantık hatalarını atlar
- [ ] İnceleme daha uzun sürer ama bulguların içeriği yine de aynı kalır
> Açıklama: Boşluk aramak üzere prompt'lanan inceleyici her zaman bir şey bulur — bulunacak bir şey olmasa bile. Çözüm sınırdır: "yalnız doğruluğu, güvenliği veya gereksinimleri etkileyen bulguları işaretle." Sınırsız inceleyici işi bitirmez, büyütür.

### S5: Üç-ajanlı (plancı/üretici/değerlendirici) desende değerlendirici ne zaman değer katar?
- [ ] Her görevde; ne kadar çok ajan varsa sonuç o kadar güvenilir olur
- [ ] Yalnız birim testi yazılamayan görevlerde; test varsa gereksizdir
- [x] Görev, modelin tek başına güvenilir yapabildiği sınırı aştığında
- [ ] Bütçe elverdiğinde; maliyeti yoksa her akışa eklemek en iyisidir
> Açıklama: Anthropic'in kuralı sabit bir "evet" değil, koşulludur: değerlendirici ekstra tur, ekstra token ve ekstra gecikme demektir. En basit akışla başla; görev modelin solo sınırını aşıyorsa (uzun-koşan, çok adımlı işler) değerlendiricinin maliyeti karşılığını verir.
