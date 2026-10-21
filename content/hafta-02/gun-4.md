---
hafta: 2
gun: 4
baslik: "Aşamalı Açığa Çıkarma (Progressive disclosure) pratiği"
---

## Ders

### Bugün ne öğreneceksin?
Üç gündür parçaları kurdun: harita (Gün 1), ağaç (Gün 2), karar kayıtları (Gün 3). Bugün bunların birlikte oluşturduğu tekniğin adını ve **çalıştığını nasıl sınayacağını** öğreneceksin: aşamalı açığa çıkarma (progressive disclosure).

### Temel kavramlar
- **Aşamalı açığa çıkarma (progressive disclosure):** bilgiyi baştan yüklemek yerine **ihtiyaç anına ertelemek**. Benzetme: havaalanı. Girişte sana 400 sayfalık el kitabı vermezler; **tabela** koyarlar — *"Gidiş ▲ 2. kat"*. 2. katta yeni bir tabela: *"Check-in D kontuarları →"*. Her adımda yalnız bir sonraki adım kadar bilgi alırsın, yine de doğru kapıya varırsın.
- **Katman 1 — her zaman okunan (küçük, sabit):** `AGENTS.md`. Her oturumda bağlama girer, bu yüzden ~45 satırdır.
- **Katman 2 — işaretçinin gösterdiği (orta):** `ARCHITECTURE.md`, `docs/adr/`, `docs/exec-plans/`, `docs/tech-debt-tracker.md`. Yalnız o görev gerektirince açılır.
- **Katman 3 — kodun kendisi (büyük):** ilgili sınıf ve test dosyaları. En sona kalır, çünkü en çok yer kaplayan ve en göreve özel olan budur.

Gün 1'de bağlamın **kıt** olduğunu gördün. Aşamalı açığa çıkarma bu kıtlığın çözümüdür: bilgiyi silmezsin, **ertelersin**.

### Somut örnek
Bu projede gerçek bir soru: *"Cevap önbelleği için Redis ekleyelim mi?"*
- **Adım 1 (Katman 1):** Ajan `AGENTS.md`'yi okur. Sınırlar bölümünde şu satırı bulur: **"ASK FIRST: yeni bağımlılık (önce docs/adr/'ye ADR)."** Bu satır cevabı vermez — **kapıyı gösterir.**
- **Adım 2 (Katman 2):** İşaretçiyi izler, `docs/adr/` dizinini açar. `0001-spring-boot-spring-ai.md` ve `0004-openrouter-saglayici.md` dosyalarını görür; bağımlılık kararlarının bu depoda nasıl gerekçelendirildiğini (Durum / Bağlam / Karar / Alternatifler / Sonuçlar) oradan öğrenir.
- **Adım 3 (Katman 3):** Ancak bundan sonra koda iner ve gerçekten bir önbellek katmanı gerekip gerekmediğine bakar.
- **Sonuç:** Ajan **kod yazmaya başlamadan** durur ve *"Bu yeni bir bağımlılık; önce ADR-0010 taslağı yazayım, onaylar mısın?"* der. Toplam okuduğu: 45 satır AGENTS.md artı 1-2 ADR. Ansiklopedi yaklaşımında 9 ADR'nin ve tüm mimari metnin tamamı baştan yüklenecek, valizde koda yer kalmayacaktı.

Sistemin çalıştığının kanıtı şu testtir: ajan **bilmediği** bir şeyi, **nerede arayacağını bilerek** bulabiliyor mu? AGENTS.md'yi okuduktan sonra "bilmiyorum" diyorsa harita eksiktir; her şeyi baştan okuyorsa harita ansiklopedileşmiştir. Doğru davranış: **kısa okuma → doğru sıçrama → derin okuma.**

### Sık yapılan hata
- **Haritayı da varış noktası yapmak.** Ajan bir şeyi bulamayınca cevabı doğrudan AGENTS.md'ye yazarsın. Birkaç hafta sonra harita ansiklopediye döner ve teknik çöker, çünkü Katman 1 artık küçük değildir. Her ekleme öncesi sor: *bu satır bir sıçrama tahtası mı, yoksa varış noktası mı?*
- **İşaretçiyi belirsiz bırakmak.** *"Mimari kararlar belgelenmiştir"* bir işaretçi değildir; *"Kararlar: docs/adr/"* işaretçidir. Ajan tahmin etmez, adres izler.
- **Aşamalı açığa çıkarmayı "bilgiyi gizlemek" sanmak.** Hiçbir şey gizlenmiyor; her şey erişilebilir durumda, sadece hepsi aynı anda **açık** değil.
- **"Aşamalı"yı zorluk sırası sanmak.** Aşamalı olan belgenin zorluğu değil, açılma **anıdır**.

### Görevlere geçmeden önce
Aşağıdaki iki görev sistemi iki yönden sınar: birincisi haritadan ağaca doğru bakar (her başlığın gerçekten bir karşılığı var mı — kırık işaretçi, olmayan işaretçiden kötüdür), ikincisi ajanın gözünden bakar. İkinci görevde izleyeceğin şey cevabın doğruluğu değil, **ajanın hangi dosyaları açtığıdır**.

## Görevler
- [ ] AGENTS.md'ndeki her başlığın docs/ altında karşılığı olduğunu doğrula
- [ ] Claude Code/Codex'e repoda bir soru sor; doğru belgeye gidip gitmediğini izle
