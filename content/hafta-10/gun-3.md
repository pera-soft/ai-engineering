---
hafta: 10
gun: 3
baslik: "ADR: advice process'in omurgası"
---

## Ders

### Bugün ne öğreneceksin?
Danışmayı öğrendin, forumu tasarladın. Peki konuşulan her şey nereye gidiyor? Bugün ADR'yi (Architecture Decision Record — Mimari Karar Kaydı) Advice Process'in omurgası olarak öğreneceksin. Hafta 2'de ADR'nin **biçimini** görmüştün; bugün üzerine **süreç** boyutunu ekliyorsun: alınan danışma ve kararın geri alınabilirliği.

### Temel kavramlar
- **ADR:** tek bir mimari kararı, sebebiyle birlikte anlatan kısa bir metin dosyası. Kod deposunda, koda yakın durur; wiki'de değil.
- **Bağlam (context):** kararı verdiren durum. "O gün ne biliyorduk, hangi kısıtlar vardı?" Bir kararın doğru mu yanlış mı olduğu ancak bağlamıyla anlaşılır.
- **Alınan danışma:** kime danışıldığı, ne söylendiği ve hangi tavsiyenin **neden** alınmadığı. ADR'yi bir karar duyurusundan, süreç kaydına çeviren bölüm budur.
- **Superseded (yerine geçildi):** bir kararın iptal edilme biçimi. Eski ADR silinmez; durumu "superseded" yapılır ve yeni ADR'ye işaret eder. Böylece hem eski gerekçe hem yeni gerekçe okunabilir kalır.

Kilit ayrım: ADR burada bir **dayatma** aracı değil, bir **kolaylaştırma** aracıdır. Kimseye "böyle yapacaksın" demez; "böyle karar verildi, sebebi buydu, kime danışıldı" der. Fark önemlidir: dayatan belge okunmaz, açıklayan belge aranır.

Bir de rahatlatıcı taraf: ADR yazmak, kararı ağırlaştırmaz — **hafifletir**. Karar geri alınabilir olduğunda insanlar daha hızlı karar verir. Yanlış çıktığında yapılacak şey suçlu aramak değil, yeni bilgiyle yeni bir kayıt açmaktır.

### Somut örnek (bu proje)
`docs/adr/` dizinindeki dokuz kaydın hepsi aynı dört başlığı taşır: **Durum (Status)**, **Bağlam (Context)**, **Karar (Decision)**, **Sonuçlar (Consequences)**; çoğunda bir de **Değerlendirilen alternatifler** bölümü vardır. Alternatifler bölümü, danışmanın yazıya dökülmüş hâlidir: "şunu da düşündük, şu yüzden almadık".

ADR-0005 geri alınabilirliğin ders kitabı örneğidir. Vektör deposu olarak bellek-içi `SimpleVectorStore` seçilir, ama karar kendi ömrünü peşinen ilan eder: not tabanı büyürse pgvector'a geçiş bu ADR'yi **supersede** eder ve `AnlamArama` arayüzü sayesinde service değişmez. Yani karar hem yazılıdır, hem tarihlidir, hem de geri alınması ucuz olacak şekilde tasarlanmıştır (Hafta 9'un port'u burada işe yarar). ADR-0007 ise başka bir kalıbı gösterir: karar aynı kalır, ama fazlar geçtikçe kaydın altına tarihli notlar eklenir.

### Sık yapılan hata
En yaygın hata "ne yaptığımızı" yazıp "neden yaptığımızı" yazmamaktır; o zaman ADR, kodun yavaş bir kopyası olur ve kimse okumaz. İkincisi, alternatifleri gizlemek — sonraki okuyucu senin zaten elediğin yolu baştan tartışmaya açar. Üçüncüsü, eski kararı **silmek**: karar tarihi silinince ekip aynı hatayı iki yıl arayla tekrar yapar; doğrusu superseded işaretlemektir. Dördüncüsü, ADR'yi karardan haftalar sonra yazmak; o noktada gerekçe artık hatırlanan değil, uydurulan bir şeydir.

### Görevlere geçmeden önce
Mevcut bir ADR'yi genişletirken "Alınan danışma" bölümüne üç şey yaz: kime danıştım, ne dediler, hangi tavsiyeyi **neden** almadım. Üçüncüsü en değerlisidir, çünkü kararın sınırlarını gösterir. Ekip anlaşması cümlesini yazarken de kısa tut ve mekanik olsun; örneğin: "Geri dönmesi pahalı her karar, önce etkilenen ve uzmana danışılır, sonra `docs/adr/` altına bir kayıt olarak açılır; karar değişirse eski kayıt superseded işaretlenir."

## Görevler
- [ ] Bir ADR'yi 'alınan danışma' bölümü ekleyerek genişlet
- [ ] Advice Process + ADR'yi ekip anlaşmasına bir cümleyle yaz
