---
hafta: 8
gun: 2
baslik: "Fine-tune ne zaman?"
---

## Ders

### Bugün ne öğreneceksin?
"Modeli kendi verimizle eğitelim" cümlesi toplantılarda en sık kurulan ve en sık yanlış olan cümledir. Bugün fine-tune'un gerçekte ne yaptığını, ne yapmadığını ve hangi koşullar sağlanmadan ona geçilmeyeceğini öğreneceksin. Günün sonunda bir "fine-tune yapalım" önerisini gerekçeyle kabul ya da reddedebileceksin.

### Temel kavramlar
- **Ağırlıklar (parametreler):** Modelin içindeki milyarlarca sayı. Modelin "bildiği" her şey bu sayılarda kodludur.
- **Fine-tune (ince ayar):** Hazır bir modele, senin hazırladığın *girdi → istenen çıktı* örneklerini göstererek ağırlıklarını **bir miktar değiştirmek**. Model sıfırdan eğitilmez; var olan model senin örneklerine doğru eğilir.
- **Eğitim verisi:** Fine-tune'un yakıtı. Genelde binlerce, en azından yüzlerce **temiz ve tutarlı** örnek gerekir. Örnekler kendi içinde çelişkiliyse model çelişkiyi öğrenir.
- **Karar merdiveni:** Prompt → RAG → fine-tune. En ucuz ve en hızlı geri alınabilenden başla; her basamak bir öncekinin yetmediği kanıtlandığında çıkılır.

Ayrımı tek satırda tut: **bilgi eksikse RAG, davranış eksikse fine-tune.** Fine-tune modele "şirketimizin fiyat listesi şudur" diye bilgi yüklemek için kötü bir araçtır — bilgi ağırlıkların içine dağılır, güncellenemez, kaynağı gösterilemez ve zamanla uydurmaya dönüşür. Buna karşılık fine-tune şunlarda gerçekten kazandırır:

- Model biçimi **binde bir bile** bozmasın isteniyorsa (kesintisiz geçerli çıktı üretimi).
- Çok özel bir üslup ya da alan jargonu kalıcı olarak istendiğinde.
- Uzun few-shot örnekler prompt'u şişirip maliyeti yukarı çektiğinde: davranış ağırlıklara girince prompt kısalır, **çağrı başına maliyet düşebilir.**
- Küçük ve ucuz bir modele, büyük modelin belirli bir işteki davranışı öğretilmek istendiğinde.

Karşılığında ödediğin bedel **bakımdır**: temel model yeni sürüme geçtiğinde ince ayarını yeniden yapmak zorunda kalırsın, elinde ayrı bir eğitim verisi hattı ve ayrı bir sürüm yönetimi olur. Yani fine-tune bir seferlik iş değil, **süregiden bir yükümlülüktür.**

Fine-tune'a geçmeden önce üç kapı: (1) Prompt ve few-shot gerçekten denendi mi, ölçülerek mi elendi? (2) Eksik olan şey bilgi mi (öyleyse dur, cevap RAG)? (3) Elinde birkaç yüz-bin temiz örnek ve bunu güncel tutacak biri var mı? Üçüne de "evet" diyemiyorsan cevap hayırdır.

### Somut örnek
Bu projede AI Öğretmen'in çok belirgin bir üslubu var: Türkçe konuşur, teknik terimlerin İngilizcesini parantezle verir, sokratik sorular sorar. Bu, tam da "davranış/üslup" ihtiyacı — yani fine-tune adayı gibi görünür. Ama fine-tune yapılmadı; davranış `prompts/sistem-kimlik.st` ve `prompts/sokratik-sohbet.st` dosyalarındaki **düz metin talimatlarla** kuruldu. Sebebi net: üslup daha oturmuş değil, hâlâ haftada birkaç kez düzeltiliyor. Prompt'ta düzeltmek bir dosya değişikliği; fine-tune'da düzeltmek yeni bir eğitim turu demek. Öğrencinin kitap notlarına erişimi ise bilgi ihtiyacı olduğu için RAG ile çözüldü (ADR-0005).

### Sık yapılan hata
- **Bilgi sorununu fine-tune ile çözmeye çalışmak.** En pahalı, en kırılgan yol; belge yarın değişince baştan başlarsın.
- **"Fine-tune halüsinasyonu bitirir" sanmak.** Bitirmez; biçimi sağlamlaştırır, uydurmayı yalnızca dolaylı olarak azaltabilir.
- **Prompt'u ciddi denemeden geçmek.** Prompt'la 5 dakikada çözülecek işi haftalara yaymak.
- **Kirli veriyle eğitmek.** Yüz çelişkili örnek, sıfır örnekten daha zararlıdır — model kararsızlığı öğrenir.
- **Ölçüm kurmadan başlamak.** "Daha iyi oldu mu?" sorusunu cevaplayacak sabit bir örnek kümen yoksa, fine-tune'un işe yarayıp yaramadığını asla bilemezsin.

### Görevlere geçmeden önce
Görevdeki "fine-tune yapalım" fikrini değerlendirirken üç kapıyı sırayla sor ve cevabı şu kalıpta yaz: *"Eksik olan ______ (bilgi/davranış/talimat). Bu yüzden çözüm ______. Fine-tune'a geçersek yılda ______ kez yeniden eğitmemiz ve ______ kişinin veriyi güncel tutması gerekir."* Son cümleyi yazamıyorsan karar henüz hazır değildir.

## Görevler
- [ ] İlgili bölümü oku
- [ ] Ekipte konuşulan 1 'fine-tune yapalım' fikrini bu çerçeveyle değerlendir: gerçekten gerekli mi?
