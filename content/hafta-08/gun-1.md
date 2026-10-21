---
hafta: 8
gun: 1
baslik: "RAG mimarisi"
---

## Ders

### Bugün ne öğreneceksin?
Hafta 7'de RAG'ı "modele açık kitap koymak" diye duydun. Bugün o kitabın nasıl kurulduğunu öğreniyorsun: bir belge yığınından, soruyla ilgili doğru sayfaları saniyeler içinde bulan boru hattı. Günün sonunda beş adımı sırayla sayabilecek ve kendi ürünün için bir eskiz çizebileceksin.

### Temel kavramlar
- **RAG (Retrieval-Augmented Generation — "getirmeyle güçlendirilmiş üretim"):** Soru geldiğinde ilgili belge parçalarını bulup prompt'a ekleme, modelin cevabı **onlara bakarak** üretmesi.
- **Vektör:** Bir metnin anlamını temsil eden sayı dizisi (ör. 768 ya da 1536 sayı). İnsana anlamsız görünür; makine için "anlam haritasındaki koordinat"tır.
- **Embedding (gömme):** Metni vektöre çeviren işlem/model. Anlamca yakın metinler **birbirine yakın** koordinatlar alır: "Köpek havladı" ile "It havladı" farklı kelimelerdir ama yakın vektörlerdir; "Faiz oranı arttı" uzakta kalır.
- **Cosine benzerliği:** İki vektörün ne kadar aynı yöne baktığını ölçen sayı. Yakınlık kararı buradan çıkar.

Boru hattının adımları — her birini "açık kitap" benzetmesiyle eşleştir:

1. **Chunking (parçalama)** = *kitabı sayfalara böl.* Uzun belgeyi anlamlı küçük parçalara ayır. Tek parça yaparsan arama işe yaramaz; çok ufak parçalarsan bağlam kopar.
2. **Embedding (gömme)** = *her sayfayı anlamını temsil eden vektöre çevir.*
3. **Vektör arama (retrieve)** = *sorunun vektörünü hesapla, en yakın k parçayı bul* (ör. en yakın 6).
4. **Re-ranking (yeniden sıralama)** = *adaylar arasından en alakalıyı öne al.* Vektör araması hızlı ama kabadır; daha titiz bir model adayları tekrar puanlar. **İsteğe bağlıdır**, küçük koleksiyonlarda atlanabilir.
5. **Bağlama yerleştirme (augment) → cevap (generate)** = *sayfaları modelin önüne koy:* "İşte kaynak metinler; cevabını BUNLARA dayandır."

Üstüne iki kritik kural:

- **Embedding modeli tutarlılığı.** Belgeler hangi modelle gömüldüyse **sorgu da aynı modelle** gömülmelidir. Farklı modellerin ürettiği sayılar karşılaştırılamaz; karıştırırsan sistem hata vermez, sessizce alakasız sonuç döndürür. Bu yüzden embedding modelini değiştirmek = **tüm koleksiyonu yeniden indekslemek.**
- **Hibrit arama.** Vektör araması anlamı yakalar, kelimeyi kaçırabilir: ürün kodu "XR-4471" ya da nadir bir özel isim aranınca anlam benzerliği işe yaramaz. Çözüm, klasik kelime aramasıyla vektör aramasını birleştirip sonuçları harmanlamaktır.

### Somut örnek
Bu projenin kendi RAG'i (ADR-0005) tam olarak bu boru hattıdır:
- **Chunking:** Kitap notların `data/hafiza/kitaplar/*.md` altında not blokları hâlinde durur (~133 blok). Her blok bir "sayfa"dır; blok sınırı seçildi çünkü bir bloğun özeti + anahtar kavramları tek anlam taşır.
- **Embedding:** Her blok OpenRouter üzerinden `google/gemini-embedding-001` modeline gönderilip vektöre çevrilir. Yeni not eklendiği **an** gömülür; açılışta depo boşsa hepsi toplu indekslenir.
- **Arama:** Vektörler `data/hafiza/vektor.json` içinde tutulur (Spring AI `SimpleVectorStore`, düz cosine). Soru sorulunca en yakın 6 blok döner.
- **Re-ranking:** **YOK** — bilinçli karar. ~133 blokta düz cosine zaten kesin sonuç verir; re-rank yalnız gecikme ve maliyet ekler. Milyonlarca vektörde denge tersine döner.
- **Yerleştirme:** Dönen bloklar ~6000 karakterlik sınıra kırpılıp öğretmenin prompt'una eklenir — yani AI Öğretmen sana cevap verirken senin kendi notlarına bakıyor.
- **Geri düşüş:** Anahtar yoksa veya embedding hata verirse arama boş döner ve sistem hafta etiketi eşleşmesine düşer; özellik uygulamayı hiçbir koşulda bozmaz.

### Sık yapılan hata
- **"RAG halüsinasyonu bitirir" sanmak.** Azaltır. Yanlış parça getirilirse model o yanlışa sadık kalır; kazanç, cevabın **kaynağının gösterilebilmesi**dir.
- **Parça boyutunu düşünmeden seçmek.** Çok büyük parça alakasız metni de içeri sokar ve pencereyi doldurur; çok küçük parça cümleyi bağlamından koparır.
- **Embedding modelini sessizce değiştirmek** — arama hata vermeden çöpe döner.
- **k'yı büyütmeyi kalite artışı sanmak.** Daha fazla parça = daha fazla gürültü, daha fazla token, daha yavaş cevap.

### Görevlere geçmeden önce
Eskizini beş kutuyla çiz: kaynak → chunk → embed → ara → cevapla. Her okun üstüne bir karar yaz: parça neye göre bölünüyor, hangi embedding modeli, kaç sonuç (k), re-rank var mı, hiçbir şey bulunamazsa ne oluyor. Son soru en çok atlanan ve canlıda en çok canı yakandır.

## Görevler
- [ ] Türkçe çalışma metnini oku ('Bu haftanın kaynakları' kutusundan; kaynak: kitap RAG bölümü)
- [ ] Kendi ürünün için 1 RAG boru hattı eskizi çiz (kaynak → chunk → embed → ara → cevapla)
