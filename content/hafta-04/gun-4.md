---
hafta: 4
gun: 4
baslik: "AI kodu güvenliği"
---

## Ders

### Bugün ne öğreneceksin?
Bu hafta mimari kuralları mekanikleştirdin. Bugün aynı şeyi **güvenlik** için yapacaksın. Sebebi somut: Veracode'un 2025 GenAI raporuna göre AI üretimi kodun **%45'i** güvenlik testlerinden kaldı; Java'da bu oran **%72**. Ajan hızlı yazar ama güvenliği kendiliğinden getirmez — o yüzden güvenlik de bir **sensör** olarak otomatikleşmek zorundadır.

### Temel kavramlar
- **Kimlik doğrulama (authentication):** "sen kimsin?" sorusunun cevabı.
- **Yetkilendirme (authorization):** "bunu yapmaya hakkın var mı?" sorusunun cevabı. İkisi ayrı şeydir; birini yapıp diğerini atlamak klasik açıktır.
- **Sunucu tarafı girdi doğrulama:** kullanıcıdan gelen verinin **sunucuda** kontrol edilmesi. Tarayıcıdaki kontrol yalnız kullanıcıya kolaylıktır; saldırgan tarayıcıyı atlar.
- **Sır (secret):** API anahtarı, parola, token. Koda ya da git geçmişine asla girmez.
- **Sır taraması (secret scanning):** koda ve git geçmişine sızmış sırları arayan araç — gitleaks, trufflehog.
- **SAST (statik uygulama güvenlik testi):** kodu **çalıştırmadan** tarayıp güvenlik açığı kalıplarını bulan analiz. Örnek: Semgrep'in OWASP kural seti.
- **OWASP Top 10:** en yaygın on web güvenlik açığı listesi; SAST kuralları çoğu zaman buna göre yazılır.
- **Slopsquatting:** ajanın **var olmayan** bir paket adı uydurması ("slop"), saldırganın o adı gerçekten yayınlaması ve bir sonraki kurulumda zararlı kodun projeye girmesi. Tedarik zinciri saldırısıdır.

### Somut örnek
Bu projede güvenlik kuralları belge olarak zaten var — `AGENTS.md`'deki NEVER maddeleri: *"API anahtarını koda/git'e koymak"* ve *"127.0.0.1 dışına bind"* yasak. Gün 1'in sorusunu sor: **çiğnenirse kim söyleyecek?** Bugün cevabı "ben fark ederim"den "bir komut" hâline getiriyorsun:

- **Sır sızıntısı için:** CI'ya `gitleaks` eklenir. Ajan hata edip `OPENROUTER_API_KEY`'i bir `application.yml`'ye yazarsa, PR **kırmızı yanar** ve birleştirilemez. Bu kural insan dikkatine bırakılamaz; sır bir kez git geçmişine girdiğinde silmek pahalıdır.
- **Kod açıkları için:** Semgrep OWASP kural seti PR hattına eklenir. Doğrulanmamış girdi, açıkta kalan uç nokta, tehlikeli deserialization gibi kalıplar merge öncesi yakalanır.
- **Bağımlılıklar için:** Ajan yeni bir kütüphane önerdiğinde iki adım zorunludur — paketin **gerçekten var olduğunu ve doğru paket olduğunu** doğrula (slopsquatting), sonra `docs/adr/` altına bir ADR aç (`AGENTS.md`: ASK FIRST). Yani "ajan önerdi, ekledim" akışı burada bilerek kesilir.

Sonuç, Gün 1'deki tablonun aynısıdır: kural değişmedi, **denetleyici** değişti. Fark şu ki güvenlikte kaçırılan tek bir ihlalin bedeli, mimaride kaçırılan ihlalinkinden çok daha yüksektir.

### Sık yapılan hata
En sık hata, güvenliği **incelemeye bırakmaktır**: "PR'da bakarız." Ajanla çalışırken PR sayısı ve dosya sayısı artar; insan gözü ilk gün yeter, otuzuncu gün yetmez. İkinci hata, taramaları **uyarı** seviyesinde bırakmaktır — bloklamayan tarama, ekibin görmezden gelmeyi öğrendiği bir gürültüye dönüşür. Üçüncüsü, güvenliği yalnız yeni koda uygulayıp **geriye dönük** taramayı atlamaktır; hattı bugün kurmak, dün sızmış bir anahtarı temizlemez.

### Görevlere geçmeden önce
Sırayla ilerle: önce `gitleaks`'i CI'ya ekle (en yüksek fayda/en düşük emek), sonra Semgrep OWASP setini PR hattına koy. En sonda geriye dönük iş var: son 5 AI-üretimi PR'ı dört maddelik listeyle tara — sır sızıntısı, sunucu tarafı girdi doğrulama, yetki kontrolü, eklenen bağımlılıkların gerçekliği. Bulduklarını düzelt; düzeltemediklerini `docs/tech-debt-tracker.md`'ye yaz ki kaybolmasınlar.

## Görevler
- [ ] CI'a gitleaks ekle
- [ ] Semgrep OWASP kural setini PR pipeline'ına ekle
- [ ] Son 5 AI-üretimi PR'ı bir güvenlik kontrol listesiyle (sır sızıntısı, girdi doğrulama, yetki, bağımlılık) geriye dönük tara
