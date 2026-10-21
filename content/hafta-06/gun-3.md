---
hafta: 6
gun: 3
baslik: "C4 Modeli"
---

## Ders

### Bugün ne öğreneceksin?
Bugün mimariyi **anlatmayı** öğreneceksin. C4'ün dört zoom seviyesini, hangi soruya hangi seviyenin cevap verdiğini ve iyi bir diyagramı ölçen tek soruyu: *"Dinleyicim bunu tek başına anladı mı?"*

### Temel kavramlar
- **C4 modeli:** Simon Brown'ın önerdiği, sisteme dört farklı zoom düzeyinden bakma yöntemi: **Context → Container → Component → Code**. Benzetmesi **Google Maps**: aynı şehre bakarsın ama zoom'a göre farklı şey görürsün ve **her zoom farklı bir soruya** cevap verir. Dünya haritası = Context, şehir haritası = Container, mahalle = Component, bina planı = Code.
- **Tek kuralı:** **bir diyagram = bir zoom = bir dinleyici.** Dünya haritasına daire planı çizmezsin.
- **Seviye 1 — Context (Bağlam): "Bu sistem kiminle konuşuyor?"** Dinleyici: herkes; teknik bilgi gerektirmez. Gösterdiği: senin sistemin **tek bir kutu**, etrafında kullanıcı tipleri ve dış sistemler. Göstermediği: teknoloji, veritabanı, sınıf. İçini açmazsın.
- **Seviye 2 — Container (Konteyner): "Neyi ayrı ayrı çalıştırıp dağıtıyoruz?"** Dinleyici: geliştiriciler ve operasyon. Kutular **ayrı çalışan birimlerdir** (uygulamalar, veritabanları, dosya depoları); her kutuya teknolojisi, her oka protokolü yazılır. **Uyarı:** buradaki "container" **Docker konteyneri değildir**; kastedilen "kendi başına çalışan şey"dir.
- **Seviye 3 — Component (Bileşen): "Bir konteynerin içinde ne var?"** Dinleyici: yalnız o konteyner üzerinde çalışan geliştiriciler. Gösterdiği: sorumluluk blokları ve aralarındaki bağımlılık okları.
- **Seviye 4 — Code (Kod): "Bir bileşenin içindeki sınıflar."** Dinleyici: neredeyse kimse. **Somut tavsiye: bunu genelde çizme.** Kod her gün değişir, diyagram değişmez → diyagram yalancıya döner. Bu seviyeyi IDE'n zaten istediğin an üretir. Yalnızca açıklanması gerçekten zor tek bir algoritma varsa çiz.

### Somut örnek (bu projeden)
- **Context:** `Öğrenci (kullanıcı)` → `AI Öğretmen (sistem — tek kutu)` → `OpenRouter API (dış sistem, LLM sağlayıcı)`. Toplam **3 kutu, 2 ok.** Bitti.
- **Container — 4 kutu:** (1) `Tarayıcı arayüzü`, (2) `AI Öğretmen uygulaması` *(Spring Boot / Java, 127.0.0.1:8090)*, (3) `H2 veritabanı` (öğrenci ilerlemesi/hafızası), (4) `content/ dosya deposu` *(frontmatter'lı Markdown)*. Oklar: Tarayıcı →(HTTP/JSON)→ Spring Boot →(JDBC)→ H2; Spring Boot →(dosya okuma)→ content/; Spring Boot →(HTTPS)→ OpenRouter.
- **Component — Spring Boot konteynerinin içi:** `MufredatService` (müfredat okur), `KaynakService` (kaynak metinleri), `OgretmenAiClient` (`ai/` paketi — LLM'e giden **tek kapı**). Oklar bu projenin katman yönünü izler: `web → service → ai`. Ve diyagramda **`web`'den doğrudan `repo`'ya ok YOKTUR** — çünkü Hafta 4'teki ArchUnit testi (`MimariTestleri`) buna izin vermez. **Diyagram ile kod aynı şeyi söyler; söylemediği an biri yalan söylüyordur.**

Hangi soruya hangi seviye:
- Patron: *"Bu OpenRouter denen şeye ne kadar bağımlıyız?"* → **Context** (tek ok gösterir, biter).
- Yeni geliştirici: *"Uygulamayı çalıştırmak için neyi ayağa kaldırmam lazım?"* → **Container.**
- Ekip arkadaşı: *"LLM çağrısını nereye eklemeliyim?"* → **Component** (cevap diyagramdan okunur: `ai/` paketindeki `OgretmenAiClient`).

Yanlış seviyeyi seçersen ya çok fazla ya çok az anlatırsın; ikisi de iletişimi öldürür.

### Sık yapılan hata
- **Kutu-ok tuzağı:** İsimsiz kutular ve etiketsiz oklar. "Servis A → Servis B" ne anlatır? Ok ne taşıyor: HTTP isteği mi, olay mı, dosya mı? Yönü ne demek: çağıran mı, veri akışı mı? Etiketsiz diyagramı **okuyan herkes farklı anlar** — yani diyagram tek bir anlam taşımaz, tartışmayı da bitirmez. Kural: her kutu bir **ad + sorumluluk**, her ok bir **fiil + protokol** taşısın.
- **Her şeyi tek diyagrama sığdırmak.** Kullanıcıdan sınıflara kadar her şeyin olduğu diyagram, kimsenin sorusuna cevap vermez.
- **Notasyon takıntısı.** C4'ün değeri katı bir standart olmasında değil, sana "kime, ne kadar zoom?" diye sormasında.
- **Code seviyesini elle çizmek.** Bakımı imkânsızdır; iki ay sonra kod ile diyagram çelişir.

### Görevlere geçmeden önce
Görevde yalnız iki seviye isteniyor: **Context ve Container** — pratikte en çok işe yarayan ikisi bunlardır. Aracın önemi yok (mermaid, draw.io, Structurizr); ama mermaid seçersen diyagram repoda metin olarak yaşar, sürümlenir ve yarın (Gün 4) göreceğin gibi ajan da okuyabilir. Çizdikten sonra tek testi uygula: diyagramı hiç açıklama yapmadan birine göster ve ne anladığını sor.

## Görevler
- [ ] Kendi sistemin için Context + Container diyagramı çiz (Structurizr/draw.io/mermaid)
- [ ] Diyagramı docs/design-docs/ altına koy ve AGENTS.md'den işaret et
