---
hafta: 6
baslik: "Mimari Düşünme II: Stiller ve İletişim"
hedef: "Fundamentals 2e ikinci yarı: mimari stiller (katmanlı/layered, olay-güdümlü/event-driven, servis-tabanlı, mikroservis…) + C4 ile diyagramlama. (Hexagonal/Clean mimari ayrı bir haftada — Hafta 9.)"
kaynak: "Kitap: FoSA 2e — ikinci yarı + Simon Brown C4 kitabı (Tem 2026'da yayımlandı: ~150 sf, yarım günlük REFERANS — tam slot değil)"
---

## Quiz
### S1: C4 modelinin dört seviyesi, en geniş zoom'dan en dara doğru hangisidir?
- [ ] Concept, Container, Class, Code
- [x] Context, Container, Component, Code
- [ ] Context, Cluster, Class, Cloud
- [ ] Container, Component, Class, Cloud
> Açıklama: Dört seviye bir harita zoom'udur: Context (sistem ve komşuları), Container (ayrı ayrı çalışan/dağıtılan birimler), Component (tek bir konteynerin içi), Code (sınıf ayrıntısı). Her seviye farklı bir dinleyiciye konuşur; hepsini tek diyagrama sıkıştırmak en sık yapılan hatadır.

### S2: Mikroservis stili hakkında doğru ifade hangisidir?
- [ ] Aynı işi yapan monolitten hem daha ucuz hem daha basit bir çözümdür
- [ ] Kod kalitesi yeterince yükselen her sistemin doğal olarak ulaştığı son aşamadır
- [x] Bağımsız dağıtım ve servis başına ölçek karşılığında işletme karmaşıklığı satın alır
- [ ] Yalnızca çok büyük şirketlerin karşılayabildiği, teknik faydası olmayan bir modadır
> Açıklama: Kazanç gerçektir (bağımsız dağıtım, parça parça ölçek, hata izolasyonu) ama bedel de gerçektir: ağ gecikmesi, dağıtık hata ayıklama, veri tutarlılığı, izleme altyapısı. Soru "modern mi" değil, "bu bedeli ödemeyi haklı çıkaran bir sorunum var mı".

### S3: Bir mimari stilin puan kartını (karakteristik yıldızlarını) doğru okumak ne demektir?
- [x] Stilin zayıf kaldığı hanelerin, senin gerçekten umursadığın haneler olup olmadığına bakmak
- [ ] En çok yıldızı toplayan stili seçmek, çünkü toplam puan genel kaliteyi gösterir
- [ ] Yıldızsız her haneyi ek araç ve altyapı ekleyerek yıldızlı hâle getirmeye çalışmak
- [ ] Sadece performans ve ölçek hanelerine bakıp gerisini sonraki sürüme bırakmak
> Açıklama: Puan kartı bir yarışma tablosu değil, eşleştirme aracıdır. Bu projede (AI Öğretmen) Layered stil elasticity hanesinde zayıftır; ama tek kullanıcılı, 127.0.0.1'e bağlı bir eğitim uygulamasında elasticity umursanmayan bir hanedir — dolayısıyla seçim doğrudur.

### S4: Conway yasası bir mimari kararı nasıl etkiler?
- [ ] Bir modülün karmaşıklığı, içindeki satır sayısıyla doğru orantılı olarak artar
- [ ] Ekibe eklenen her yeni geliştirici toplam verimi kaçınılmaz biçimde düşürür
- [ ] Sistemin hızı, seçilen programlama dilinin derleme hızıyla sınırlı kalır
- [x] Sistemin yapısı, onu üreten ekiplerin iletişim yapısına benzemeye eğilimlidir
> Açıklama: Conway (1967): bir tasarım, onu üreten organizasyonun iletişim yollarının kopyasıdır. Pratik sonucu: üç ayrı ekip varsa sistem er geç üç parçaya ayrılır. Bu yüzden "mikroservise bölelim" kararı aynı zamanda bir ekip kararıdır; ekip yapısı değişmeden yapılan bölme kâğıt üstünde kalır.

### S5: Diyagramı mermaid gibi metin biçiminde repoda tutmanın kazancı nedir?
- [ ] Görsel araçla çizilen diyagramdan daha hızlı render edilir ve daha az yer kaplar
- [x] Kodla birlikte sürümlenir, incelemede değişikliği görülür ve ajan tarafından okunabilir
- [ ] Metin diyagramların notasyonu daha katı olduğu için teknik olarak daha doğrudurlar
- [ ] Diyagram metin biçiminde olduğu için kod değiştikçe kendini otomatik günceller
> Açıklama: Görsel bir araca hapsolmuş diyagram ne PR'da incelenebilir ne de ajanın okuyabileceği bir kaynaktır; metin diyagram ise repo bilgisine dönüşür (agent legibility). Ama kendi kendini güncellemez: diyagramı kodla tutarlı tutmak yine insanın sorumluluğudur.
