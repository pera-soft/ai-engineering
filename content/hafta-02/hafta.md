---
hafta: 2
baslik: "Repo Bilgi Sistemi: AGENTS.md + docs/"
hedef: "Repoyu 'kayıt sistemi' yap: içindekiler-tablosu AGENTS.md, yapılandırılmış docs/, ilk ADR'ler."
kaynak: "Kaynak: OpenAI docs/ yapısı + agents.md standardı + Nygard ADR formatı"
---

## Quiz
### S1: Monolitik (dev) AGENTS.md'nin başarısız olmasının temel nedeni nedir?
- [ ] Ajanlar belli bir satır sayısından sonra dosyayı hiç okuyamaz
- [ ] Uzun markdown dosyaları git'te sürekli birleştirme çakışması yaratır
- [x] Bağlam kıttır; dev dosya görevi ve kodu bağlamın dışına iter
- [ ] Talimat uzadıkça model daha yavaş cevap verir; asıl sorun gecikmedir
> Açıklama: Sorun okuma yeteneği, git ya da hız değil, yer paylaşımıdır: talimata ayrılan her satır asıl göreve ve koda ayrılan yerden düşer. İkinci etki de var — uzun metinde ortadaki kural fiilen görünmez olur (talimat şişmesi → bağlam çürümesi). Kural dosyada durur ama uygulanmaz.

### S2: AGENTS.md ile CLAUDE.md'nin zamanla birbirinden sapmasını (drift) ne engeller?
- [ ] İki dosyayı düzenli aralıklarla elle kopyalamak
- [x] Symlink kurmak: ln -s AGENTS.md CLAUDE.md
- [ ] CI'da iki dosyanın farkını raporlayan bir kontrol
- [ ] Hiçbiri gerekmez; araçların hepsi iki dosyayı da okur
> Açıklama: Claude Code CLAUDE.md, çoğu araç AGENTS.md arar. Symlink'te fiziksel dosya tektir, sapma imkânsızdır. Elle kopyalama unutulur; CI kontrolü sapmayı yalnız FARK ETTİRİR, engellemez. Bu depoda CLAUDE.md gerçekten AGENTS.md'ye symlink'tir — `ls -la CLAUDE.md` ile görebilirsin.

### S3: Accepted (kabul edilmiş) bir ADR'de fikir değişirse ne yapılır?
- [ ] ADR silinir; yanlış bir karar repoda kalmamalıdır
- [ ] ADR'nin Karar bölümü yeni kararla değiştirilir
- [ ] ADR'nin durumu Rejected'a çevrilir ve dosya olduğu gibi bırakılır
- [x] Yeni ADR yazılır; eskisi 'Superseded by' ile işaretlenir
> Açıklama: ADR'ler karar tarihçesidir; geçmiş silinmez ya da üzerine yazılmaz, yoksa 'o zaman neden böyle düşünmüştük' bilgisi kaybolur. Rejected hiç kabul edilmemiş bir öneri içindir; kabul edilmiş ama sonradan aşılmış karar Superseded olur.

### S4: Hangi karar ADR'yi hak eder?
- [ ] Girintinin tab mı boşluk mu olacağı
- [ ] Bir fonksiyonun en fazla 20 satır olması kuralı
- [x] Oturum durumunu veritabanında tutma kararı
- [ ] Bu hafta hangi hatanın önce düzeltileceği
> Açıklama: Ölçüt 'yük taşıyor mu' (load-bearing): geri alınması pahalıysa ve üstüne başka kararlar biniyorsa ADR'dir. Biçim tercihleri formatlayıcıya, kod konvansiyonları linter'a, iş sırası plana aittir — hepsini ADR yapmak gürültü üretir ve gerçek kararları görünmez kılar.

### S5: 'Progressive disclosure' ajan bağlam yönetiminde ne demektir?
- [ ] Belgeleri kolaydan zora sıralayıp ajana bu sırayla okutmak
- [x] Küçük sabit bir giriş noktası verip gerisini ihtiyaç anına bırakmak
- [ ] Hassas bilgiyi ajandan kalıcı olarak gizleyip yalnız koda erişim vermek
- [ ] Her prompt'a docs/ dizininin tamamını yükleyip hiçbir şey kaçırmamak
> Açıklama: 'Aşamalı' olan, belgenin zorluğu değil AÇILMA anıdır: ajan her oturumda AGENTS.md'yi okur, derin belgeyi ancak görev gerektirince açar. Bilgi silinmez ya da gizlenmez — ertelenir. OpenAI'ın deyişiyle: ajana 1000 sayfalık kılavuz değil, harita ver.
