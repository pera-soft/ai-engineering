---
hafta: 8
gun: 4
baslik: "Gecikme ve maliyet mühendisliği"
---

## Ders

### Bugün ne öğreneceksin?
Bir AI özelliği demoda harika, canlıda iflas ettirici olabilir. Bugün faturanın ve bekleme süresinin nereden geldiğini, ve ikisini de kaliteyi düşürmeden nasıl aşağı çekeceğini öğreniyorsun. Günün sonunda bir iş akışına bakıp "şu adım ucuz modele inebilir" diyebileceksin.

### Temel kavramlar
- **Token maliyeti:** Ücret token başınadır ve girdi ile çıktı ayrı fiyatlanır — çıktı genelde daha pahalıdır. Bu yüzden maliyet sorusu "kaç kullanıcı?" değil, **"istek başına kaç token?"** sorusudur.
- **Gecikmenin iki yüzü:** İlk kelimenin gelme süresi (kullanıcı "donmuş mu?" diye buna bakar) ve cevabın bitme süresi. Akışlı (streaming) yanıt ikincisini kısaltmaz ama ilkini hissettirir; algılanan hız böyle kazanılır.
- **Prompt caching (bağlam önbelleği):** İsteklerin başında **değişmeyen** uzun bir bölüm varsa (sistem promptu, kurallar, sabit belgeler), sağlayıcı bunu önbelleğe alır ve sonraki isteklerde çok daha ucuza ve hızlıca işler. Şartı, sabit kısmın **prompt'un başında ve birebir aynı** olmasıdır — bir karakter değişirse önbellek ıskalanır. Dikkat: bu cevap önbelleği değildir; aynı soruya aynı cevabı garanti etmez.
- **Model kademeleme (cascading):** Önce ucuz ve hızlı model dener; sonuç yetersiz ya da iş riskliyse pahalı modele yükseltirsin. Yükseltme kararını bir eşiğe bağlarsın: model "emin değilim" dedi, doğrulama başarısız oldu, girdi çok uzun.
- **Toplu işleme (batch):** Acelesi olmayan işleri (gece raporu, toplu etiketleme) biriktirip indirimli/yavaş kanaldan geçirmek.

Maliyeti düşürmenin sırası da bir merdivendir: (1) **daha az token gönder** — prompt'u kısalt, gereksiz belgeyi ekleme, geçmişi sınırla; (2) **tekrar edeni önbelleğe al**; (3) **kolay işleri ucuz modele indir**; (4) **acelesi olmayanı topluya al**. Kaliteyi bozmadan en çok kazandıran neredeyse hep birincisidir — çünkü şişmiş bağlam hem parayı hem doğruluğu birlikte yer.

"Her isteğe en pahalı model" bir anti-desendir: gereksiz maliyet, gereksiz gecikme ve — kimsenin beklemediği biçimde — bazen daha kötü kullanıcı deneyimi getirir, çünkü kullanıcı yavaş mükemmel cevap yerine hızlı yeterli cevabı tercih eder.

### Somut örnek
Bu projede kademeleme baştan kurulu: uzun sokratik sohbet güçlü modelde, kısa yardımcı işler (başlık üretme, küçük özet, biçim düzeltme) hızlı modelde çalışır. RAG tarafında da aynı disiplin var: aramadan dönen bloklar doğrudan prompt'a basılmaz, `BaglamKirpici` ile ~6000 karakterlik bir sınıra kırpılır. Bu tek satırlık kural hem token faturasını sabitler hem de bağlam penceresinin dolup asıl talimatı dışarı itmesini engeller. Prompt'ların `prompts/` altında sabit dosyalar hâlinde durması ise önbelleklemeye uygun yapıdır: değişmeyen kısım hep aynı yerde ve hep başta.

### Sık yapılan hata
- **Tek çağrının fiyatına bakıp rahatlamak.** Maliyeti günlük istek sayısıyla çarpıp **aylık** olarak yaz; karar orada değişir.
- **Çıktı token'ını unutmak.** "Ayrıntılı açıkla" diyen bir prompt, faturanın pahalı yarısını büyütür. Uzunluk sınırı koy.
- **Sohbet geçmişini sınırsız göndermek.** Her turda geçmişin tamamı yeniden ödenir; özetleyerek taşı.
- **Önbelleği bozan değişkenler.** Prompt'un başına tarih/saat ya da kullanıcı adı koyarsan sabit bölüm her istekte değişir ve önbellek hiç tutmaz.
- **Kademelemeyi eşiksiz kurmak.** "Gerekirse büyük modele çık" kuralı ölçülebilir bir eşiğe bağlanmazsa, pratikte her istek büyük modele çıkar.
- **Maliyeti kalitesiz ölçmek.** Ucuz modele inerken altın kümeni (dünkü ders) koşturmazsan, tasarrufun kaliteye ne kadar mal olduğunu bilemezsin.

### Görevlere geçmeden önce
Görevde bir iş akışı seç ve adımlarını tek tek yaz. Her adım için üç soru: bu adım **kullanıcıyı bekletiyor mu**, çıktısı **başka bir adım tarafından mı** tüketiliyor, ve yanlış olursa **fark edilir mi**? Kullanıcıyı bekletmeyen, çıktısı makineye giden ve hatası sonraki adımda yakalanan adımlar, ucuz modele inmek için en güvenli adaylardır.

**Derinleşmek istersen:** "Bu haftanın kaynakları" kutusundaki **Canlıda LLM** metninin §1 "Gecikmenin anatomisi — TTFT, TPOT, uçtan uca", §2 "Goodput — SLO'yu çok kısıtlı yazmak" ve §3 "Yük testi — tek prompt'u tekrarlama tuzağı" bölümleri, bugünkü "gecikmenin iki yüzü"nü ölçülebilir hâle getirir: ilk kelimenin gelme süresi TTFT (time to first token), sonraki her kelimenin süresi TPOT (time per output token) olur ve hangisi baskınsa kaldıracın (prompt'u kırpmak mı, çıktıyı sınırlamak mı) o olduğunu gösterir. §3 ise bugünkü önbelleğin yük testinde nasıl yanılttığını anlatır: aynı prompt'u tekrarlayan bir test, önbellek isabeti yüzünden kapasiteyi olduğundan yüksek gösterir. Görev için şart değil; bugünkü kavramların canlıdaki karşılığını merak edersen oku.

## Görevler
- [ ] İlgili bölümü oku
- [ ] Bir AI iş akışında kademeleme fırsatı bul: hangi adım ucuz modele inebilir?
