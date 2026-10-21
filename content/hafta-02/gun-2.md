---
hafta: 2
gun: 2
baslik: "docs/ iskeletini kur"
---

## Ders

### Bugün ne öğreneceksin?
Dün AGENTS.md'yi bir **haritaya** indirgedin. Bugün haritanın işaret ettiği yeri kuracaksın: `docs/` ağacı. Amaç, her bilgi türüne önceden bir **ev** vermek — çünkü evi olmayan bilgi ya AGENTS.md'yi şişirir ya da hiç yazılmaz.

### Temel kavramlar
- **docs/ ağacı:** derin bilginin yaşadığı, alt dizinlere ayrılmış klasör. AGENTS.md'nin işaretçileri buraya bakar.
- **ADR (Mimari Karar Kaydı / Architecture Decision Record):** "şunu şu yüzden seçtik" kaydı; yarın ayrıntısına gireceksin. Yeri: `docs/adr/`.
- **ExecPlan (yürütme planı):** birden çok adımlı bir işin planı ve ilerleme günlüğü. Yeri: `docs/exec-plans/`, tercihen `active/` ve `completed/` diye ikiye ayrılır — böylece "hangisi hâlâ geçerli" sorusu klasör adından cevaplanır.
- **Design doc (tasarım belgesi):** bir özelliğin nasıl kurulacağının, kod yazılmadan önceki anlatımı.
- **Teknik borç (technical debt):** bilerek ertelenmiş düzeltme. Yazılmazsa unutulur; yazılırsa takip edilebilir bir kaleme dönüşür.
- **İskelet (skeleton):** içi henüz boş ama başlıkları ve dizinleri hazır yapı.

### Somut örnek
OpenAI'ın deneyindeki ağaç şuydu: `design-docs/`, `exec-plans/active` + `exec-plans/completed`, `product-specs/`, `references/`, artı kökte `ARCHITECTURE.md`, `QUALITY_SCORE.md`, `SECURITY.md`. Bu bir **menü**, zorunlu liste değil.

Bu depo aynı fikrin küçük hâlini uyguluyor:
- `ARCHITECTURE.md` — katman tablosu ve tek yönlü bağımlılık kuralları. Kökte durur, çünkü en sık bakılan belgedir.
- `docs/adr/` — numaralı karar kayıtları (`0001-spring-boot-spring-ai.md` … `0009-...`).
- `docs/exec-plans/` — faz planları.
- `docs/tech-debt-tracker.md` — bilinen borçlar.
- `docs/SECURITY.md` — güvenlik notları.

Kritik nokta: `AGENTS.md`'nin "Nerede ne var" başlığı bu dosyaların **tam yolunu** verir. Yapı ile harita birbirine kilitlidir — ağaca yeni bir dal eklersen haritaya bir satır eklersin; haritada karşılığı olmayan dal ajan için görünmezdir.

Boş dizin bile işe yarar. `docs/exec-plans/` bugün boşsa bile, yarın plan yazman gerektiğinde "bunu nereye koysam?" diye düşünmezsin — ve daha önemlisi, o planı AGENTS.md'nin içine yazma dürtüsüne kapılmazsın. İskeletin asıl işi, yanlış yere yazmayı **zorlaştırmaktır**.

### Sık yapılan hata
İki uç var, ikisi de sık:
- **Kopyala-yapıştır iskelet.** Yukarıdaki yedi başlığı olduğu gibi kurup hiçbirini doldurmamak. Altı ay sonra elinde boş klasörlerden oluşan bir müze olur; ajan `docs/product-specs/` diye bir yer olduğunu görür, açar, boş bulur ve haritaya olan güvenini yitirir. Kural: **doldurmayacağın dalı açma.** Üç dalla başla, ihtiyaç çıkınca ekle.
- **Her şeyi tek dosyaya yığmak.** `docs/notlar.md`. Bu, AGENTS.md şişmesinin kılık değiştirmiş hâlidir: ajan aradığını bulamaz, sen de yeni bilgiyi nereye yazacağını bilemezsin.

Bir de sessiz hata: dizini kurup **AGENTS.md'ye işaretçi eklememek.** Depoda duran ama hiçbir haritada görünmeyen belge, Hafta 1'in diliyle yok hükmündedir.

### Görevlere geçmeden önce
Aşağıdaki üç görev iskeleti gerçek yapar: önce dizinleri kuracaksın, sonra ARCHITECTURE.md'ye 10 satırlık bir harita çizeceksin (10 satır kısıtı kasıtlı — sistemin özünü zorla süzdürür), en sonda teknik borç dosyasını üç gerçek borçla açacaksın. Boş dosya bırakmamak, bugünkü dersin kendi kuralıdır.

## Görevler
- [ ] docs/ iskeletini repoya ekle
- [ ] ARCHITECTURE.md'ye mevcut sistemin 10 satırlık haritasını çiz
- [ ] tech-debt-tracker.md dosyasını başlat, bilinen 3 borcu yaz
