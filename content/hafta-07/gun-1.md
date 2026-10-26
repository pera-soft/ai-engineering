---
hafta: 7
gun: 1
baslik: "AI Engineering ≠ ML Engineering"
---

## Ders

### Bugün ne öğreneceksin?
Bugüne kadar mimari konuştuk; bu hafta **yapay zekâ mühendisliği** başlıyor. Günün sonunda üç şeyi kendi cümlelerinle anlatabileceksin: "dil modeli" dediğimiz şey aslında ne yapıyor, AI mühendisliği klasik makine öğrenmesinden neden farklı, ve hazır bir modeli kendi işine uydurmanın üç yolu hangi **ayrı** problemi çözüyor.

### Temel kavramlar
- **Dil modeli (LLM — Large Language Model, "büyük dil modeli"):** İnternet ölçeğinde metinle beslenmiş, tek bir şey yapan bir program: *"bu metinden sonra en olası devam nedir?"* Cevabı kelime kelime tahmin ederek üretir. Düşünmez, aramaz, hatırlamaz — **tahmin eder**. Bu cümleyi aklında tut; haftanın yarısı bundan çıkıyor.
- **Token:** Modelin gördüğü en küçük metin parçası; kabaca bir kelime ya da kelimenin bir parçası. Model harf değil token sayar ve ücretlendirme token başınadır.
- **Prompt (yönerge):** Modele verdiğin metin talimat. Modelin gördüğü tek gerçeklik budur; prompt'ta olmayan hiçbir şeyi bilmez.
- **Temel model (foundation model):** Başkasının milyonlarca dolar harcayarak eğittiği, genel amaçlı dev model. Sen onu eğitmezsin, **kiralarsın**.
- **Adaptasyon:** Hazır modeli kendi işine uydurma. Üç yolu var: prompt, RAG, fine-tune.

Klasik makine öğrenmesi: veri topla → model eğit → yayına al. AI mühendisliği: **hazır modeli al → adapte et → değerlendir → servis et.** Giriş bariyeri düştü; ama model artık senin ürettiğin bir şey olmadığı için **değerlendirme** işin en zor parçası hâline geldi.

Üç adaptasyon yolunu, modeli **çok zeki ama şirketini hiç tanımayan yeni bir stajyer** gibi düşünerek ayır:

- **Prompt = "soruyu düzgün sor."** Stajyer zeki; "bir şeyler yap" dersen saçmalar, "şu faturayı, şu biçimde, şu 3 adımla çıkar" dersen yapar. Modele dokunmazsın, talimatı düzeltirsin.
- **RAG = "yanına açık kitap koy."** Stajyer sizin belgelerinizi hiç okumamış. Ezberletmezsin; soru gelince **ilgili sayfaları önüne koyarsın**, o da okuyup cevaplar.
- **Fine-tune (ince ayar) = "kişiyi alıştır."** Binlerce örnek üzerinde çalıştırırsın; davranışı, üslubu, biçimi kemikleşir. **Modelin ağırlıkları (parametreleri) değişir.**

> **RAG = BİLGİ ekler. Fine-tune = DAVRANIŞ/ÜSLUP kazandırır.** Fine-tune, yeni bilgi öğretmek için pahalı ve güvenilmez bir yoldur. Bilgi eksikse cevap her zaman **RAG**'dır.

| | Prompt | RAG | Fine-tune |
|---|---|---|---|
| **Neyi çözer** | Talimat belirsiz | Model **bilmiyor** | Biliyor ama istediğin gibi davranmıyor |
| **Model değişir mi** | Hayır | Hayır | **Evet** (ağırlıklar) |
| **Bilgi güncellenince** | — | Belgeyi değiştir, bitti | Yeniden eğit |
| **Maliyet / süre** | Dakikalar, bedava | Saatler–günler, orta | Günler, pahalı; eğitim verisi şart |
| **Deneme sırası** | **Her zaman önce** | Prompt yetmezse | En son çare |

### Somut örnek
Şu an kullandığın bu proje tam olarak böyle çalışıyor. Kendi modelimizi eğitmedik; OpenRouter üzerinden hazır bir temel modeli çağırıyoruz. AI Öğretmen'in kişiliği ve kuralları `prompts/sistem-kimlik.st` dosyasında **düz metin** olarak duruyor — yani davranışı fine-tune ile değil, **prompt ile** kuruldu. Haftanın içeriğini bilmesi ise `content/` altındaki metinlerin çağrı anında prompt'a eklenmesiyle olur: bu da RAG'ın en yalın hâlidir.

### Sık yapılan hata
En sık refleks: *"şirket belgelerimizle modeli eğitelim."* Neredeyse her zaman yanlıştır. Belge yarın değişirse fine-tune'u baştan yaparsın; RAG'de dosyayı değiştirir, geçersin. Üstelik RAG **kaynak gösterebilir** ("bu cevap şu belgenin 3. sayfasından"), fine-tune gösteremez. İkinci hata bunun tersi: prompt'la beş dakikada çözülecek bir biçim sorununu fine-tune'a taşımak — en pahalı aracı en ucuz problem için seçmek.

### Görevlere geçmeden önce
Tek soruyu ezberle: **"Modelin eksiği bilgi mi, davranış mı, yoksa sadece kötü talimat mı?"** Kötü talimat → prompt. Eksik bilgi → RAG. Yanlış davranış (ve prompt çare olmadı) → fine-tune. Görevde senaryolarını bu üç kutuya yerleştirirken her biri için bu soruyu yüksek sesle cevapla; kutulardan hangisine düştüğünü gerekçesiyle yaz.

**Derinleşmek istersen:** "Bu haftanın kaynakları" kutusundaki **Ajan Mimarisi Desenleri** metninin §1 "İş akışı mı ajan mı — grafı kim sahipleniyor?" bölümü, bugünkü "hazır modeli al → adapte et" fikrinin bir adım sonrasını anlatır: prompt, RAG ve araçlarla güçlendirilmiş model çağrılarını (augmented LLM) önceden yazılmış senin kodun mu sıralıyor (iş akışı / workflow), yoksa model çalışma anında kendisi mi seçiyor (ajan / agent)? Bugünkü "önce prompt, en son fine-tune" sırası gibi metnin kuralı da en basitle başlamaktır: çoğu iş için sabit bir iş akışı yeter. Şimdilik zorunlu değil; ajanlara geldiğinde geri dönebilirsin.

## Görevler
- [ ] Türkçe çalışma metnini oku ('Bu haftanın kaynakları' kutusundan; kaynak: kitap Böl. 1)
- [ ] Şirketindeki AI kullanım senaryolarını 'adaptasyon tekniği' eksenine yerleştir (prompt mu, RAG mi, fine-tune mu?)
