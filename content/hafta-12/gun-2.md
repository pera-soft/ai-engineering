---
hafta: 12
gun: 2
baslik: "Ekip AI anlaşmasını yayınla"
---

## Ders

### Bugün ne öğreneceksin?
Bir ekipte AI kullanımını yöneten şey yasak listesi değil, **birlikte yazılmış bir anlaşmadır**. Bugün bu anlaşmanın hangi maddelerden oluştuğunu ve neden "dayatma" ile "anlaşma" arasındaki farkın sonucu belirlediğini öğreneceksin.

### Temel kavramlar
- **Ekip anlaşması (team agreement):** Ekibin, nasıl çalışacağına dair birlikte karar verip **yazıya döktüğü** kurallar. Yöneticinin tek başına yazdığı belge kural olur; ekibin birlikte yazdığı belge **norm** olur. Kurallara uyulur ya da uyulmaz; normlar ekipte birbirini hatırlatarak yaşar.
- **Sorumluluk maddesi — "commit eden sahiptir":** Kodu gönderen kişi, üreteni AI olsa bile sonucun sahibidir. "AI yazdı" bir savunma değildir.
- **Doğrulama zinciri:** AI çıktısının hangi adımlardan geçtikten sonra kabul edildiği: derleniyor mu → testler var mı → gerçekten çalıştırıldı mı → insan okudu mu. Zincirin her halkası ayrı ayrı yazılır, "dikkatli olalım" denmez.
- **Şeffaflık:** AI'ın anlamlı katkı yaptığı yerin saklanmaması. Amaç etiketleme değil, ekibin **neyin nasıl üretildiğini** konuşabilmesi.
- **Bağlam hijyeni (Hafta 3):** Modele ne verildiğinin bilinçli seçilmesi — gereksiz dosya, eski karar, çelişkili talimat vermemek.
- **KVKK / kişisel veri:** Gerçek müşteri verisi, kimlik bilgisi, sağlık verisi ve **sır** (şifre, token, anahtar) modele gönderilmez. Bu madde tartışmaya açılmaz; anlaşmanın sert sınırıdır.
- **Prompt/skill kütüphanesi:** İyi çalışan yönergelerin kişisel not defterlerinde değil, **repoda** durması. Böylece bilgi kişiye değil ekibe ait olur.

### Somut örnek
Bu proje bir örnektir: kök dizindeki `CLAUDE.md` (AGENTS.md) tam olarak böyle bir anlaşmadır. İçinde ALWAYS / ASK FIRST / NEVER başlıkları vardır: "Claude çağrısı yalnız `ai/` paketinden", "yeni bağımlılık için önce ADR", "API anahtarını koda/git'e koymak yasak". Dikkat et — bu maddeler ne belirsizdir ne de temenni: her biri **denetlenebilir**. Kimisini ArchUnit testi zorlar, kimisini insan gözden geçirir. Bir anlaşma maddesi, "nasıl kontrol edeceğiz?" sorusuna cevap veremiyorsa, o madde bir dilektir.

### Sık yapılan hata
Üç tuzak: (1) **Anlaşmayı tek kişinin yazıp duyurması.** Tartışılmadan gelen belge ilk sıkışık günde ihlal edilir; birlikte yazılan belge hatırlatılır. (2) **Yasak listesi yapmak.** "AI'la şunu yapmayın" listesi eskir ve insanları gizli kullanıma iter; bunun yerine **hangi doğrulamadan sonra kabul edilir** diye yaz. (3) **v1'i mükemmelleştirmeye çalışmak.** Anlaşma yaşayan bir belgedir; eksik ama yayınlanmış bir v1, mükemmel ama yayınlanmamış bir taslaktan iyidir — tarih koy, üç ay sonra gözden geçir.

### Görevlere geçmeden önce
Toplantıya "onaylayın" diye değil, **üç soruyla** git: (1) Hangi işlerde AI'a güveniyoruz, hangilerinde güvenmiyoruz? (2) Bir AI çıktısının kabul edilmesi için hangi adımlar zorunlu? (3) Hangi veriler hiçbir koşulda modele gitmez? Bu üç sorunun cevabı, anlaşmanın iskeletidir; gerisi biçimdir.

**Derinleşmek istersen:** "Bu haftanın kaynakları" kutusundaki **Ajan Çalışma-Zamanı Kontrolleri** metninin §3 "Görev başına yetki merdiveni" ve §5 "Ajanın uzanamadığı kontroller — kill switch, canary, log" bölümleri, bugünkü ilk soruya ("hangi işlerde AI'a güveniyoruz?") denetlenebilir bir cevap biçimi verir. Yetki merdiveni güveni "evet/hayır" yerine görev başına basamaklara böler (salt-oku, öner, onaylı yaz, serbest); kill switch (acil durdurma anahtarı) ise ajanın okuyabildiği ama yazamadığı, yeniden açılması bir insanın açık eylemi olan bir bayraktır — ikisi de "nasıl kontrol edeceğiz?" sorusuna cevap verebilen anlaşma maddeleri olarak v1'e girebilir. Toplantı için şart değil; maddelerini somutlaştırmak istersen oku.

## Görevler
- [ ] Ekip toplantısında anlaşmayı tartışıp v1'i yayınla
- [ ] Prompt/skill kütüphanesi için repo dizinini aç (.claude/commands veya .agents/skills)
