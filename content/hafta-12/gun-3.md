---
hafta: 12
gun: 3
baslik: "AI-çağı code review kültürü"
---

## Ders

### Bugün ne öğreneceksin?
Kod incelemesinin (code review) merkez sorusu değişti. Bugün yeni soruyu, iyi geri bildirimin nasıl verildiğini, hangi kararların **asla** ajana bırakılmadığını ve iş ters gittiğinde ekibi öğrenen hâle getiren aracı — **suçsuz postmortem**'i — öğreneceksin.

### Temel kavramlar
- **Code review:** Bir değişikliğin, gönderilmeden önce başka bir insan tarafından okunup sorgulanması. Amacı hata yakalamak kadar **bilgiyi yaymaktır**.
- **Değişen soru:** Eskiden "neden böyle yazdın?" sorulurdu. Şimdi asıl soru: **"AI ne uyguladı, sen bunu anlıyor musun?"** İnceleme artık üretimi değil, **kavrayışı ve doğrulamayı** denetler — mentorluğun ana sahnesi burasıdır.
- **Geri bildirim (feedback):** İyi geri bildirim kişiyi değil davranışı hedefler, somuttur ve bir sonraki adımı söyler. "Bu kod kötü" bilgi taşımaz; "bu retry idempotent olmayan bir çağrıyı sarıyor, çift kayıt riski var — istek anahtarı ekleyelim mi?" hem sorun hem yol gösterir. Soru biçimindeki geri bildirim, junior'ı düşünmeye zorlar; emir biçimindeki geri bildirim yalnızca uygulatır.
- **Geri dönüşü zor karar:** Yapılınca geri alınamayan ya da çok pahalıya alınabilen iş: şema göçü (migration), dış sözleşme (API) değişikliği, veri silme, üretim verisine dokunan işlemler. Ölçüt "önemli mi" değil, **geri alınabilir mi**.
- **Suçsuz (blameless) postmortem:** Bir arıza sonrası yapılan, **kişiyi değil sistemi sorgulayan** inceleme. Soru "kim yaptı?" değil, "bu hatayı mümkün kılan neydi ve hangi korkuluğu koyarız?"dır. Suçsuzluk, kimsenin sorumlu olmaması demek değildir; insanlar cezalandırılacağını düşündüğünde bilgi sakladığı ve öğrenmenin durduğu için seçilen bir yöntemdir.

### Somut örnek
Bir junior, AI ile üretilmiş bir göçü (migration) merge ediyor; göç bir sütunu **siliyor** ve üretimde iki saat veri kaybı yaşanıyor.

**Kötü tepki:** "Kim merge etti?" Junior savunmaya geçer, bir daha inisiyatif almaz, ekip hiçbir şey öğrenmez.

**Suçsuz postmortem:** Zaman çizelgesi yazılır, sonra sistem sorgulanır. Bulgular: (a) PR şablonunda "geri dönüşü zor mu?" diye soran bir madde yoktu, (b) göçleri ayrı bir onaya bağlayan kural yoktu, (c) üretim öncesi yedek doğrulanmamıştı. **Aksiyonlar:** PR şablonuna geri-dönülemezlik maddesi, göç dosyalarına zorunlu ikinci onay, yedekten geri dönüş tatbikatı. Dikkat: aksiyonların hiçbiri "daha dikkatli ol" değil — hepsi **sistemi** değiştiriyor. Bu tam da Hafta 11'de gördüğün derse benzer: kimse hata yapmamıştı, sistem hatayı mümkün kılıyordu.

### Sık yapılan hata
İki tuzak: (1) **"Testler yeşil, onaylandı."** Yeşil test, kodun anlaşıldığını göstermez; AI çağında incelemenin işi tam olarak burada başlar. (2) **Suçsuzluğu "kimse hesap vermez" sanmak.** Suçsuz postmortem hesap vermeyi kaldırmaz, hedefini değiştirir: kişiden sisteme. Üçüncü ve sessiz tuzak: olayı hiç yazmamak — o zaman aynı arıza altı ay sonra bir başkasının başına gelir.

### Görevlere geçmeden önce
Kontrol listesini PR şablonuna gömerken iki madde mutlaka bulunsun: **"Bu değişiklik geri alınabilir mi?"** ve **"Gönderen, değişikliği kendi cümleleriyle anlatabiliyor mu?"** İlk 3 PR'da listeyi denetim aracı gibi değil, **mentorluk aracı** gibi kullan: madde okumak yerine soru sor.

## Görevler
- [ ] Kontrol listesini PR şablonuna göm (GitHub PR template)
- [ ] İlk 3 PR'da listeyi mentorluk aracı olarak kullan (juniora sorular sorarak)
