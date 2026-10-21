---
hafta: 1
gun: 3
baslik: "Ajan okunabilirliği (agent legibility)"
---

## Ders

### Bugün ne öğreneceksin?
Bugünün tek cümlesi: **ajanın çalışırken bağlamında göremediği bilgi, fiilen yoktur.** Bu, dün öğrendiğin guide ve sensor'ların önkoşuludur — repoda yaşamayan bir guide, guide değildir. Kavramın adı **ajan okunabilirliği**.

### Temel kavramlar
- **Bağlam (context):** ajanın o an önünde duran metnin tamamı — senin isteğin, okuduğu dosyalar, kurallar, buraya kadarki konuşma. Ajanın "gördüğü" tek yer burasıdır; bağlamın dışı onun için karanlıktır.
- **Ajan okunabilirliği (agent legibility):** bir bilginin ajan tarafından bulunabilir ve okunabilir olması. Ölçüsü basit: bilgi depoda, düz metin/markdown olarak duruyor mu?
- **Artefakt (artifact):** repoya işlenmiş, sürüm takibi yapılan somut dosya. Konuşma artefakt değildir; markdown dosyası artefakttır.
- **Kurumsal örtük bilgi (tribal knowledge):** yalnız insanların kafasında ve sohbetlerde yaşayan, hiçbir yere yazılmamış bilgi. Ekip büyüdükçe en pahalı kayıp kalemi budur.

### Somut örnek
Bu depoda gerçek bir karar var: model erişimi doğrudan Anthropic API yerine **OpenRouter** üzerinden yapılıyor. Karar bir konuşmada alındı. İki senaryoyu karşılaştır:
- **Yazılmasaydı:** Ajan yapılandırmada `base-url: https://openrouter.ai/api` satırını görür, "bu garip, resmi Anthropic starter'ına geçireyim" der ve **iyi niyetle** kararı bozar. Sen fark edene kadar da geri döner; fark edince yeniden anlatırsın, sonraki oturumda yine bozar.
- **Yazıldığı için:** Karar `docs/adr/0004-openrouter-saglayici.md` dosyasında yaşıyor — bağlamı ("tek anahtarla çok sağlayıcı, harcama limiti"), kararı, elenen alternatifi ve artı/eksi sonuçlarıyla. Ajan dosyayı okur, kararın **gerekçesini** görür ve bozmaz; gerçekten değişmesi gerekiyorsa yeni bir karar kaydı önerir.

Sınama cümlesi şu: *"Üç ay sonra işe girecek birine bu bilgi görünür mü?"* Cevap hayırsa ajana da görünmez. Aynı ölçüt bu depoda kural olarak yazılıdır: anlamlı iş bitince yaz — eşik, *"3 ay sonraki okuyucu bunu bilmek ister mi?"*.

### Sık yapılan hata
Üç tuzak var, üçü de yaygın:
- **"Prompt'a yapıştırırım."** Bilgiyi her seferinde isteğin içine kopyalamak tek seferlik bir yamadır: unutulur, sürümlenmez, ekip arkadaşına geçmez, ölçeklenmez. Kalıcı bilgi prompt'a değil **repoya** gider.
- **"Ajan nasılsa bulur."** Ajan yalnız kendisine verilen ya da depoda arayabildiği yerlerde arar. Slack, e-posta, toplantı kaydı ve senin hafızan bu yerlerin dışındadır.
- **Yazmak ama yanlış yazmak.** Bayat bir belge hiç olmamasından kötüdür, çünkü ajan ona güvenir ve yanlış bilgiyi yetkili sanır. (Bu tuzağın panzehri Hafta 2'nin son gününde.)

Bir de tersi: bu ilke "her şeyi yaz" demek değildir. Yazılacak olan **kararlar ve gerekçeleri**dir; kodun kendisinden okunabilen şeyi tekrar yazmak yalnızca bakım yükü üretir.

### Görevlere geçmeden önce
Aşağıdaki iki görev ilkeyi doğrudan uygulatır: önce yalnız kafalarda/sohbette yaşayan üç kritik bilgiyi avlayacaksın (bunları bulmanın en kolay yolu, yeni gelen birine ilk hafta ne anlattığını hatırlamaktır), sonra bir tanesini gerçekten markdown'a çevirip repoya koyacaksın. Yazma alışkanlığı ancak bir kez yaparak başlar.

## Görevler
- [ ] Kendi ekibinde (ya da bildiğin bir projede) 'sadece Slack/kafalarda yaşayan' 3 kritik bilgiyi tespit et
- [ ] Bunlardan 1 tanesini markdown'a çevirip repoya koy
