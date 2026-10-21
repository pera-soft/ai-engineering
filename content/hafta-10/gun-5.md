---
hafta: 10
gun: 5
baslik: "Sentez: insan VE ajan kararlarını hizalama + quiz"
---

## Ders

### Bugün ne öğreneceksin?
Bugün haftanın dört parçasını tek bir sisteme bağlayacaksın: **Advice Process (Gün 1) + Danışma Forumu (Gün 2) + ADR (Gün 3) + Technology Radar (Gün 4)** = merkeziyetsiz ama tutarlı mimari yönetişim. Sonra bu haftanın asıl sorusuna geleceksin: kodun bir kısmını ajanlar yazıyorsa, mimari kararları kim veriyor?

### Temel kavramlar
- **Yönetişim (governance):** kararların nasıl verildiğini, nasıl kaydedildiğini ve nasıl geri alındığını düzenleyen çerçeve. Dördü birlikte bunu kurar: danışma kaliteyi, forum görünürlüğü, ADR hafızayı, radar ortak zemini sağlar.
- **Minimum viable agreement (asgari yeterli hizalama):** herkesin her konuda anlaşması değil, birlikte çalışmayı mümkün kılacak **en az** ortak karar. Fazlası bürokrasi, azı kaostur.
- **Ajan kararı:** bir ajan "şu kütüphaneyi ekleyelim", "bu mantığı service'e koyalım" dediğinde aslında bir mimari karar önermiştir — üstelik genellikle en kısa yolu seçerek. Kararı **sen** verirsin; ajan uygular.

Ajanlar için kural değişmez, sadece süreç somutlaşır. Ajanın önerdiği karar da (1) etkilenenlere ve uzmana danışmalı — pratikte bu, kararın insan gözünden geçmesi ve gerekirse forumda konuşulması demektir; (2) ADR'ye düşmeli; (3) radardaki halkalara uymalı — Hold'daki bir teknolojiyi ajan "pratik" diye geri getiremez.

Bir de şu asimetriyi unutma: ajan çok hızlı üretir, ama bağlamı senin kadar taşımaz. Bu yüzden hızın karşılığında **mekanik sınır** koyarsın: Hafta 9'da yazdığın ArchUnit kuralları, ajanın danışmadan verdiği kararı build kırmızısına çevirir. Yönetişimin ajan çağındaki hâli budur: insan için yazılı süreç, makine için çalışan kural.

### Somut örnek (bu proje)
Bu proje tam olarak bu üç katmanla yürür. **Yazılı süreç:** yeni bağımlılık eklenmeden önce `docs/adr/` altına ADR açılır — ajan da olsa kural aynıdır. **Çalışan kural:** `MimariTestleri` içindeki `claude_yalniz_ai_paketinden` ve `mcp_llmsiz` kuralları, "LLM çağrısı yalnız `ai/` paketinden" ve "MCP katmanı LLM'siz kalır" kararlarını mekanik hâle getirir; ADR-0009 zaten adını "MCP LLM'siz mührünü mekanikleştirme" koymuştur. **Hafıza:** ADR-0005 gibi kayıtlar kararın ne zaman supersede edileceğini önceden söyler. Sonuç: karar insana ait, uygulama ajana açık, sınır makineye emanet.

### Sık yapılan hata
En yaygın hata, ajanın çıktısını "karar verilmiş" saymaktır: kod çalışıyorsa tartışma kapanmış gibi gelir, oysa hiç kimseye danışılmamıştır. İkincisi, ajana yasak listesi vermek yerine her seferinde hatırlatmak — hatırlatma unutulur, test unutulmaz. Üçüncüsü, hizalamayı abartmak: her küçük seçim için forum ve ADR isteyen ekip, iki hafta sonra ikisini de bırakır. Asgari yeterli hizalama, "geri dönmesi pahalı olan" kararlara odaklanmak demektir.

### Görevlere geçmeden önce
Beş maddeni yazarken şu iskeleti kullan: (1) ajanın önerisini karar olarak adlandır ("bu bir mimari karardır"), (2) etkilenen ve uzman listesini çıkar, (3) radardaki halkasını kontrol et, (4) kararı ve alınan danışmayı ADR'ye yaz, (5) kuralı mümkünse bir teste dönüştür. Quiz'e geçerken haftanın tek cümlesini yanında tut: **karar tek kişide kalır, danışma zorunludur, kayıt geleceğe konuşur.**

## Görevler
- [ ] 'Ajanın verdiği mimari kararı advice process'ten nasıl geçiririm' 5 madde
- [ ] Haftalık quiz'i çöz (aşağıda)
