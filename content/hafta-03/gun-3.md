---
hafta: 3
gun: 3
baslik: "Bağlam hijyeni"
---

## Ders

### Bugün ne öğreneceksin?
Bugün ajanın **hafızasını temiz tutmayı** öğreneceksin. Aynı sohbette saatlerce çalışırsan ajan yavaşlar, eski ve alakasız ayrıntılara takılır, hatta daha önce doğru yaptığı şeyi bozar. Sebebi model değil, **dolmuş bağlamdır**. Bunun üç aletini öğreneceksin: yeni oturum (`/clear`), özetleme (compaction) ve devir notu.

### Temel kavramlar
- **Bağlam (context):** ajanın o an "aklında" tuttuğu her şey — talimat dosyaları, açtığı dosyalar, komut çıktıları, senin mesajların, kendi cevapları.
- **Bağlam penceresi (context window):** bu hafızanın **sabit** üst sınırı. Büyütülemez; yalnız içine ne koyduğun yönetilir.
- **Bağlam kirliliği:** pencerede işe yaramayan içerik birikmesi. Terk edilmiş bir deneme, 500 satırlık hata çıktısı, iptal edilmiş bir yaklaşım... Ajan bunları da "geçerli bilgi" sanar.
- **`/clear`:** oturumu sıfırlayıp bağlamı boşaltan komut. Yeni bir sayfa açmak gibidir.
- **Compaction (özetleme):** **aynı** oturumda kalıp bağlamı yerinde özetleyerek küçültme. Hızlıdır ama özet ayrıntı kaybeder.
- **Context reset (bağlam sıfırlama):** temiz bir **devir notu** bırakıp **yeni** bir oturuma geçme. Temizdir ama notu yazmak sana iş çıkarır.
- **Devir notu (handoff):** yeni oturumun işi devralması için gereken asgari bilgi: hedef, şu ana kadar yapılanlar, sıradaki adım, alınan kararlar, ilgili dosyalar.

Benzetme: bağlam bir **çalışma masasıdır**. Masa büyük olabilir ama sonsuz değildir. Üç ayrı işin kâğıtları üst üste yığıldığında, doğru kâğıdı bulmak zorlaşır — ve yanlış kâğıda bakma ihtimali artar. Compaction, masadaki kâğıtları özetleyip tek sayfaya indirmektir. Context reset ise masayı boşaltıp yalnız bir sayfalık notla yeni işe başlamaktır.

### Somut örnek
Bu proje bu işi bir konvansiyona bağlamış: uzun işlerin durumu `thoughts/ledgers/CONTINUITY_CLAUDE-<is-adi>.md` dosyasında tutulur ve **`/clear` öncesi güncellenir** (bkz. AGENTS.md, "Oturum defterleri"). Pratikte akış şudur:

- Sabah "mülakat modülü" üzerinde çalışıyorsun; oturum uzuyor, bağlam doluyor.
- Sonraki adıma geçmeden defteri güncelliyorsun: hedef, biten adımlar, sıradaki adım, kararlar, dokunulan dosyalar.
- `/clear` diyorsun. Yeni oturum defteri okuyor ve **hiçbir şey sormadan** devam ediyor.
- Öğleden sonra bambaşka bir işe (ör. quiz denetimi) geçiyorsun: bu **ayrı** bir oturum olur; mülakat işinin kalıntıları quiz işine karışmaz.

Ölçüt olarak iki pratik kural: **görev başına tek oturum**, ve bağlam kabaca **%40'ı geçtiğinde** özetle ya da devir notuyla yeni oturuma geç. Bu, Gün 2'deki ExecPlan'in tamamlayıcısıdır: plan "ne yapılacak"ı, defter "şu an neredeyiz"i taşır.

### Sık yapılan hata
En sık hata, **her şeyi tek sohbette sürdürmektir**: "nasılsa geçmişi hatırlıyor" diye üç ayrı iş aynı oturuma yığılır. Ajan bir süre sonra terk edilmiş bir yaklaşımı geri getirir, düzelttiğin hatayı tekrar yapar. İkinci hata, **not bırakmadan `/clear` demektir** — bu bağlam hijyeni değil, hafıza kaybıdır; yeni oturum sıfırdan başlar ve sen aynı şeyleri baştan anlatırsın. Üçüncüsü, compaction'ı sihir sanmaktır: özet **kayıplıdır**, kritik kararlar dosyaya yazılmadıysa özet sırasında buharlaşabilir.

### Görevlere geçmeden önce
Bugün iki şeyi bilerek yapacaksın: her işe ayrı oturum açacaksın ve bir işi kasten ikiye böleceksin — devir notu yaz, `/clear` de, ikinci oturumda devam et. İkinci oturum sana **soru sorduysa** notun eksiktir; eksik olan bilgiyi nota ekle. Bu, Gün 2'deki "amnezi testi"nin canlı provasıdır.

## Görevler
- [ ] Bugünkü işinde görev başına ayrı oturum kuralını uygula
- [ ] Bir işi bilerek /clear + devir notu ile ikinci oturumda sürdür, deneyimi not al
