---
hafta: 10
gun: 1
baslik: "Advice Process: merkeziyetsiz mimari karar"
---

## Ders

### Bugün ne öğreneceksin?
Hafta 1-9'da sistemleri kurmayı öğrendin; Hafta 10-12 **liderlik** bloğudur: kararları nasıl kolaylaştıracağın, insanı ve ajanı nasıl aynı hizada tutacağın. Bugün Harmel-Law'ın Advice Process'ini (Danışma Süreci) öğreneceksin. Günün sonunda "kim karar verir?" sorusuna ekibinde uygulanabilir bir cevabın olacak.

### Temel kavramlar
- **Mimari karar:** geri dönmesi pahalı olan teknik seçim. "Redis ekleyelim mi", "bu iki servisi ayıralım mı" mimari karardır; değişken adı seçmek değildir.
- **Danışma (advice):** bir kararı vermeden önce, o karardan etkilenecek ve o konuyu bilen insanlara sorup gerçekten dinlemek. Onay istemek değildir, oy toplamak hiç değildir.
- **Advice Process:** **Herkes** bir mimari karar verebilir, ama iki gruba danışmak **zorundadır**: (1) karardan anlamlı biçimde **etkilenenler**, (2) o konuda **uzmanlığı** olanlar. Karar veren, gelen tavsiyeyi uygulamak zorunda değildir; samimiyetle danışıp dinlemek zorundadır.
- **Konsensüs (consensus):** herkesin uzlaşması. Advice Process bu **değildir**: tek karar verici vardır, kimsenin veto hakkı yoktur.

En kolay benzetme: **ameliyat kararını cerrah verir.** Ama önce hastaya (etkilenen) ve anestezi uzmanına (uzman) danışmak zorundadır. Ne hasta oylama yapar, ne hastane komitesi toplanır — karar cerrahındır, ama danışmadan bıçağa uzanamaz.

Gerçek bir sahne: **Ayşe** sipariş sorgularının yavaşladığını görüyor ve "önbellek (cache) olarak Redis ekleyelim" diyor.

1. **Kararı Ayşe verir** — yöneticisi ya da baş mimar değil. Sorunu gören, bağlamı taşıyan kişi odur; onay için sıraya girmez.
2. **Etkilenenlere danışır.** Redis'i o yazacak ama gece 03:00'te o ayağa kaldırmayacak. Nöbet ekibi sorar: "Yedeklemesi, izlemesi, alarmı var mı? Nöbet listemize bir bileşen daha ekliyorsun." Diğer geliştiriciler sorar: "Yerel ortamda herkes bir Redis daha kaldıracak mı?"
3. **Uzmana danışır.** Daha önce Redis işletmiş **Mehmet**: "Önbellek geçersizleştirmeye dikkat — sipariş güncellenince eski kaydı silmezsen kullanıcıya eski veri gösterirsin. Bellek dolunca Redis kayıt atmaya başlar; önbelleği 'kaynak' sanıp veriyi orada tutarsan kaybedersin."
4. **Dinlemek zorunda, uygulamak zorunda değil.** Ayşe uyarıyı tartıp yine Redis'e karar verebilir — ya da "asıl sorun N+1 sorgusuymuş, önce indeks ekleyeyim" deyip kendi fikrinden vazgeçebilir. Sürecin en sık faydası budur: kötü kararların çoğu danışma sırasında kendiliğinden ölür.
5. **Kararı ve aldığı danışmayı ADR'ye yazar:** karar, bağlam, kime danıştım, ne dediler, hangi tavsiyeyi neden almadım.

| Yol | Kim karar verir? | Sorunu |
|---|---|---|
| Klasik (tek mimar onaylar) | Baş mimar | Darboğaz: herkes onun takvimini bekler, o da her sistemi bilemez |
| Konsensüs (herkes uzlaşmalı) | Herkes | Yavaş ve sulandırılmış: tek "hayır" kilitler, sonuç kimsenin inanmadığı orta yol olur |
| Advice Process | Karar sahibi (tek kişi) | Danışma emek ister; karşılığında hız, bağlam ve net sorumluluk |

### Somut örnek (bu proje)
`docs/adr/0004-openrouter-saglayici.md` bu şeklin küçük hâlidir: sağlayıcı seçimi bir kişinin kararıdır, ama bağlamı, alternatifleri ve sonuçları yazılıdır. `docs/adr/0005` ise vektör deposu için aynı şeyi yapar ve peşinen "not tabanı büyürse pgvector'a geçiş bu ADR'yi supersede eder" der — karar en baştan **geri alınabilir** ilan edilmiştir.

### Sık yapılan hata
En yaygın hata, danışmayı onaya çevirmektir: "Herkes olur derse yaparım." O an konsensüse geçmişsindir ve süreç durur. İkincisi, gelen her tavsiyeyi uygulamak zorunda hissetmek — bu da kararı kalabalığa devretmektir. Üçüncüsü, danışmayı kararı verdikten sonra yapmak; buna danışma değil, duyuru denir.

### Görevlere geçmeden önce
Danışmanın iki listesini ayrı tut: **etkilenenler** (bu kararla yaşayacak olanlar: nöbet, test, diğer ekipler) ve **uzmanlar** (bunu daha önce yapmış olanlar). Görevde kendi son kararını incelerken bu iki listeyi isim isim yaz ve şunu sor: bu kişilerle konuşsaydım kararım değişir miydi? Cevap "belki" bile olsa, süreç zaten kazandırmış demektir.

## Görevler
- [ ] Türkçe çalışma metnini oku ("Bu haftanın kaynakları" kutusundan)
- [ ] Son verdiğin bir teknik kararı al; kimlere (etkilenen + uzman) danışman gerekirdi, listele
