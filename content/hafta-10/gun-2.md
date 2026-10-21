---
hafta: 10
gun: 2
baslik: "Architecture Advice Forum (Mimari Danışma Forumu)"
---

## Ders

### Bugün ne öğreneceksin?
Dün öğrendiğin Advice Process tek bir kişinin tek bir kararı içindi. Peki ekipte haftada on karar veriliyorsa? Herkes herkese ayrı ayrı mı koşacak? Bugün bu ölçek sorununun cevabını öğreneceksin: Architecture Advice Forum (Mimari Danışma Forumu). Günün sonunda kendi ekibinde böyle bir forumu kurabilecek kadar somut bir şablonun olacak.

### Temel kavramlar
- **Forum:** düzenli aralıklarla (ör. haftada bir, 30-45 dakika) toplanan, katılımı açık bir buluşma. Karar sahibi kararını anlatır, kalanlar dinler ve danışman (advisor) olarak konuşur.
- **Danışman (advisor) rolü:** forumda kimse patron değildir. Katılanlar soru sorar, deneyim aktarır, risk gösterir. Kimse "onaylıyorum/reddediyorum" demez — o cümle bu forumda yoktur.
- **Şeffaflık (visibility):** kararın ve alınan danışmanın herkese açık olması. Amaç kontrol değil, aynı bağlamı paylaşmaktır.
- **Kurumsal öğrenme:** bir kişinin yaptığı hatanın, forumu dinleyen on kişi tarafından bir daha yapılmaması. Forumun asıl getirisi budur; kararın kendisi çoğu zaman ikinci plandadır.

Forumun akışı sadedir: gündem önceden paylaşılır (hangi kararlar konuşulacak), her karar için sahibi 5 dakikada bağlamı ve önerdiği yolu anlatır, 10 dakika soru-tavsiye alınır, sonunda **karar yine sahibinde kalır**. Toplantıda oy yoktur, kapanışta "karar şu oldu" cümlesi yoktur; sahibi çıkışta kendi kararını yazar.

Neden bu kadar işe yarar: Advice Process'in en pahalı kısmı danışacak insanları bulmaktır. Forum bu maliyeti düşürür — etkilenenler ve uzmanlar zaten oradadır. Ayrıca "kim neye karar veriyor" bilgisi tek bir yerde toplanır; kimse iki hafta sonra "benim haberim yoktu" demez.

### Somut örnek (bu proje)
Tek kişilik bir projede forum yok gibi görünür, ama işlevi vardır: bu projede `docs/adr/` dizini ve oturum defterleri (`thoughts/ledgers/`) forumun yazılı hâlidir. Bir karar (ör. ADR-0007 MCP sunucusu) yalnız "şunu yaptım" demez; bağlamı, alternatifleri ve sonucu anlatır. Üç ay sonraki okuyucu — ki o okuyucu çoğu zaman sensin ya da senin adına çalışan ajandır — forumda oturmuş gibi bağlamı alır. Kural olarak: kararın kendisi kadar, **kararı görülebilir kılan yer** de bir tasarım seçimidir.

### Sık yapılan hata
En yaygın hata forumu bir **onay kapısına** çevirmektir: gündem "onaya sunulacak kararlar" olur, insanlar el kaldırır, üç hafta içinde forum eski merkezî mimarın yeni adı hâline gelir. İkinci hata, forumu yalnız kıdemlilere açmak; o an hem bağlam hem öğrenme kaybolur. Üçüncüsü, sadece "büyük" kararları getirmek — küçük kararlar forumun yakıtıdır, çünkü asıl öğrenme onlarda olur. Dördüncüsü, konuşulanı yazmamak: yazılmayan forum, üç ay sonra hiç yapılmamış sayılır.

### Görevlere geçmeden önce
Forum tasarımını beş maddede yazarken şu beş soruya cevap ver: (1) ne sıklıkla ve kaç dakika? (2) gündeme kim, nasıl madde ekler? (3) kimler davetli — sınır var mı? (4) çıktısı nerede yazılı olacak? (5) hangi cümle **yasak** (ipucu: "onaylıyorum"). İkinci görevde kararını yazarken üç başlık kullan: *Karar — Kime danıştım — Ne dediler ve ben ne yaptım*. Bu üçlü, yarınki ADR gününün taslağıdır.

## Görevler
- [ ] Ekibinde nasıl bir danışma forumu kurulabilir, 5 maddede tasarla
- [ ] Bir kararı forumda sunar gibi 'karar + kime danıştım + ne dediler' formatında yaz
