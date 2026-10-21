---
hafta: 5
gun: 1
baslik: "Mimarın ilk yasası"
---

## Ders

### Bugün ne öğreneceksin?
Bugün mimarlığın iki temel yasasını öğrenecek ve bir kararı "takas dili"yle yazmayı deneyeceksin. Gün sonunda şu cümleyi kurabiliyor olman gerekir: *"X'i seçtim; Y'yi kazandım, Z'den vazgeçtim; çünkü bu üründe Z'nin bedeli düşük."* Bu cümle, mimarlığın tamamının sıkıştırılmış hâlidir.

### Temel kavramlar
- **Yazılım mimarisi (software architecture):** Bir sistemin, sonradan değiştirilmesi *pahalı* olan kararları. Bir düğmenin rengi mimari değildir (beş dakikada değişir); verinin nerede tutulduğu mimaridir (aylar sürer). Ölçüt "büyük karar" değil, **geri dönüş maliyeti**dir.
- **Bağlam (context):** Kararın içinde verildiği koşullar — kaç kullanıcı, hangi bütçe, kaç kişilik ekip, ne kadar süre. Aynı karar bir bağlamda doğru, ötekinde yanlıştır. Mimaride "genel geçer doğru" yoktur; yalnızca "bu bağlamda doğru" vardır.
- **Takas (trade-off):** Bir şeyi kazanmak için başka bir şeyden vazgeçmek. Terazi benzetmesi: bir kefeye koyduğunu öteki kefeden alırsın. Hız istiyorsan basitlikten, güvenlik istiyorsan kullanım kolaylığından ödersin.
- **Birinci yasa:** *"Yazılım mimarisinde her şey bir takastır."* Bir çözüm sana bedelsiz görünüyorsa, bedelini henüz görmemişsindir — bedel yok olmaz, sadece gizlenir.
- **İkinci yasa:** *"Neden, nasıldan önemlidir."* Kodun *nasıl* çalıştığı zaten kodda yazar; zamanla kaybolan şey o kararın *neden* verildiğidir.
- **ADR (Architecture Decision Record — mimari karar kaydı):** Tek bir kararı, gerekçesini ve feda edilenleri anlatan kısa metin. Hafta 2'de yazmayı öğrendin; bugün *neden* var olduğunu öğreniyorsun.

### Somut örnek (bu projeden)
`docs/adr/0004-openrouter-saglayici.md`: bu proje dil modeline doğrudan tek bir satıcı üzerinden değil, OpenRouter adlı aracı üzerinden bağlanıyor.
- **Kazanılan:** tek bir arayüzle birçok model; model değiştirmek kod işi olmaktan çıkıp ayar işine dönüşüyor.
- **Feda edilen:** araya bir aracı girdi — ek gecikme, ek bağımlılık, aracının kendi kesintileri.

Bu iki maddeyi yan yana yazdığın an mimarlık yapmış olursun. Yalnız birincisini yazarsan reklam, yalnız ikincisini yazarsan şikâyet yazmış olursun.

İkinci yasanın değeri tam burada görünür: altı ay sonra biri gecikmeyi fark edip "şu aracıyı kaldıralım" diyecek. Kayıtlı gerekçe yoksa **bilinçli bir takas "hata" sanılıp geri alınır** — ve model değiştirmek yeniden kod işine döner. ADR, kararı savunabilen tek şeydir; çünkü kararı veren kişi o odada olmayabilir.

### Sık yapılan hata
- **"En iyi mimari"yi aramak.** En iyi yoktur; bağlama göre "en az kötü" vardır. Bir blog yazısındaki mimari senin bağlamında değil, yazarın bağlamında doğrudur.
- **Bedeli söylememek.** "Mikroservise geçelim, ölçeklenir" cümlesi yarımdır. "…karşılığında dağıtık hata ayıklama ve ağ gecikmesi geliyor" kısmı olmadan bu bir öneri değil, bir dilektir.
- **Kararı sözlü bırakmak.** Toplantıda herkesin anladığı gerekçe, iki ay sonra kimsenin hatırlamadığı bir şeydir.

### Görevlere geçmeden önce
Kaynak kitap FoSA 2e'nin 1–2. bölümleri İngilizcedir; okumak zorunda değilsin. "Bu haftanın kaynakları" kutusundaki **Türkçe çalışma metni ve öz-sınav** aynı içeriği verir. İkinci görev için tek bir şablon kullan: **"… kararını verdim. Kazandığım: … Feda ettiğim: … Bunu kabul edilebilir kılan bağlam: …"** Üç boşluğu da dolduramıyorsan, henüz bir mimari karar değil, bir alışkanlık yazmışsındır.

## Görevler
- [ ] Bölüm 1–2'yi oku
- [ ] Kendi sistemindeki 1 kararı 'trade-off' diliyle yeniden yaz (kazanılan/feda edilen)
