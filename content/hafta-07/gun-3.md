---
hafta: 7
gun: 3
baslik: "Prompt engineering'in mühendislik hali"
---

## Ders

### Bugün ne öğreneceksin?
"Prompt yazmak" kulağa yazarlık gibi gelir; bugün onu **mühendisliğe** çeviriyoruz. Günün sonunda bir prompt'un hangi parçalardan oluştuğunu, sıcaklık ve bağlam penceresi ayarlarının ne yaptığını bileceksin ve bir prompt'u dosyaya alıp sürümlenir hâle getirmiş olacaksın.

### Temel kavramlar
- **Sistem promptu:** Modele "kim olduğunu ve hangi kurallara uyacağını" söyleyen, kullanıcının görmediği sabit talimat. Rolü, dili, yasakları burada tanımlarsın.
- **Kullanıcı promptu:** O anki soru ya da görev. Değişken olan parça budur.
- **Few-shot (birkaç örnekle yönlendirme):** Prompt'un içine 2-3 tane *girdi → istenen çıktı* örneği koymak. Model kuralı tarif etmenden çok, örneği taklit etmekte iyidir; biçim sorunlarının çoğu burada çözülür.
- **Bağlam penceresi (context window):** Modelin tek seferde okuyabildiği toplam token sınırı. Sistem promptu, örnekler, sohbet geçmişi, eklenen belgeler — hepsi aynı pencereyi paylaşır. Taşan kısım **sessizce düşer**; model onu hiç görmemiş gibi davranır.
- **Sıcaklık (temperature):** Modelin tahmin ederken ne kadar risk alacağını ayarlayan sayı. Düşük değer (0'a yakın) en olası kelimeyi seçtirir: tekrarlanabilir, tutarlı, sıkıcı. Yüksek değer çeşitlilik getirir: yaratıcı, ama savrulma riski yüksek. Veri çıkarma ve sınıflandırmada düşük, fikir üretmede yüksek tutulur.
- **Modelin hafızası yoktur.** Her çağrı sıfırdan okunur. "Hatırlıyor" gibi görünmesi, geçmiş konuşmanın her seferinde yeniden gönderilmesindendir — ve bu her seferinde yeniden ödediğin token demektir.

İyi bir prompt genelde şu sırayla kurulur: **rol → görev → kısıtlar → biçim → örnekler → asıl girdi.** Uzun belgeleri en sona ya da en başa koy; ortaya gömülen talimatların gözden kaçma ihtimali daha yüksektir. Kural sayısı arttıkça netlik düşer: on emir yerine üç net emir daha iyi çalışır.

Asıl mühendislik kısmı şu: prompt **koddur.** Dosyada durur, sürüm kontrolüne girer, incelenir (review), ve değiştiğinde aynı örneklerle yeniden sınanır. "Şurayı biraz düzelttim" diyerek canlıdaki prompt'a dokunmak, teste sokulmadan üretime kod atmakla aynı şeydir.

### Somut örnek
Bu projede tek bir prompt bile Java kodunun içinde yazılı değil; hepsi `prompts/` dizininde ayrı dosyalar hâlinde duruyor: `sistem-kimlik.st` ortak kimliği, `sokratik-sohbet.st` öğretme üslubunu, `quiz-uret.st` soru üretimini, `degerlendirme.st` puanlamayı taşır. Dosyaların içinde `{hafta_icerik}`, `{kaynaklar}`, `{hafiza}` gibi yer tutucular var — çalışma anında bunların yerine gerçek metin konur. Böylece prompt bir metin dosyası olarak incelenebilir, git geçmişinde değişimi görülebilir ve düzeltme yapmak için uygulamayı yeniden derlemek gerekmez.

### Sık yapılan hata
- **Prompt'u koda gömmek.** Sürümlenemez, incelenemez, karşılaştırılamaz hâle gelir.
- **"Daha uzun yazarsam daha iyi anlar" sanmak.** Uzun prompt hem pahalıdır hem dikkati dağıtır; ayrıca bağlam penceresini doldurup asıl belgeyi dışarı iter.
- **Sıcaklığı hiç düşünmemek.** Fatura verisi çıkaran bir işi varsayılan ayarla çalıştırıp "bazen farklı sonuç veriyor" diye şaşırmak.
- **Örnek yerine sıfat kullanmak.** "Profesyonel bir üslupla yaz" belirsizdir; iki örnek çıktı göstermek belirsizliği bitirir.
- **İki değişikliği aynı anda yapmak.** Prompt'u ve modeli birlikte değiştirirsen, iyileşmenin hangisinden geldiğini asla bilemezsin.

### Görevlere geçmeden önce
Görevde bir prompt'u dosyaya taşırken şunları da ekle: dosyanın başına **bir satır amaç yorumu**, sonuna **beklenen çıktı biçimi**, ve yanına 3-5 örnekten oluşan küçük bir sınama listesi. Prompt'u her değiştirdiğinde bu listeyi yeniden çalıştır — hafta 8'de bu listeye "altın küme" diyeceğiz.

## Görevler
- [ ] Prompt bölümünü oku
- [ ] Ekipte tekrar kullanılan 1 prompt'u versiyonlanır dosya haline getir (repoya)
