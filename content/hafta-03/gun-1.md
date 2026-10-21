---
hafta: 3
gun: 1
baslik: "Task spec anatomisi"
---

## Ders

### Bugün ne öğreneceksin?
Bugün bir yapay zekâ ajanına iş verirken kullanılan **görev tarifini** (İngilizcesi: task spec) yazmayı öğreneceksin. Aynı ajan, aynı model, aynı kod tabanı — ama tarif iyiyse iş bir seferde biter, kötüyse günün düzeltmeyle geçer. Bu haftaki akışın (tarif → plan → yürütme → inceleme) ilk halkası budur.

### Temel kavramlar
- **Ajan (agent):** senin adına kod okuyan, kod yazan ve komut çalıştıran yapay zekâ programı. Claude Code bir ajandır.
- **Task spec (görev tarifi):** ajana verdiğin yazılı iş emri. Sohbette dağınık cümleler değil, dört başlık altında toplanmış bir metin.
- **Goal (hedef):** işin sonunda ortada ne olacağı. Yöntemi değil, **sonucu** yazarsın.
- **Context (bağlam):** ajanın dokunacağı yer — ilgili dosyalar, mevcut yapı, daha önce alınmış kararlar (`docs/adr/`).
- **Constraints (kısıtlar):** yalnız bu göreve ait yasaklar; "şuna dokunma" listesi.
- **Completion Criteria (bitiş kriteri):** "bitti"nin tanımı. **Mekanik** olmalı: çalıştırılabilen bir komut yeşil ya da kırmızı yanmalı.

Benzetme: bir ustaya iş veriyorsun. Kötü emir: *"şu duvarı bir düzelt."* İyi emir: *"Salon duvarını beyaza boya (**hedef**); altında eski yağlı boya var, astar gerekir (**bağlam**); parkeyi örtmeden başlama (**kısıt**); bitti demek = iki kat boya kurumuş ve leke kalmamış olması (**bitiş kriteri**)."* Aynı usta, aynı el — bambaşka sonuç.

Dört unsurun her biri farklı bir kazayı önler: hedef yoksa ajan **yanlış işi kusursuz** yapar; bağlam yoksa var olan kodu bulamaz ve **paralel bir kopyasını** yazar; kısıt yoksa hedefe ulaşırken mimariyi kırar; bitiş kriteri yoksa "bitti" demenin hakemi **sen** olursun — ve elle doğrulama ölçeklenmez.

### Somut örnek
Bu projeden gerçek bir görev: *"Gün kartlarına ilerleme yüzdesi ekle."*

- **Hedef:** Hafta sayfasındaki her gün kartında tamamlanma oranı görünsün: `3/5 görev (%60)`.
- **Bağlam:** Görev işaretleri `GOREV_ILERLEME` tablosunda `types/GorevIlerlemeKaydi` olarak tutulur; okuma `repo/IlerlemeRepo`, iş kuralı `service/IlerlemeService` üzerinden yapılır. Bir günün toplam görev sayısı içerikten gelir (`types/Gun` kaydındaki `gorevler` listesi). Kartların HTML'i `static/app.js` içinde üretilir, veriyi `web/ProgramController` servis eder.
- **Kısıtlar:** Yüzde hesabı **service katmanında** yapılacak — `web/` doğrudan `repo/`'ya erişemez (AGENTS.md katman kuralı). Yeni bağımlılık yok. Mevcut API yanıtına alan **eklenir**, var olan alan silinmez. `content/` altındaki müfredat dosyalarına dokunulmaz.
- **Bitiş kriteri:** (1) `./mvnw verify` yeşil. (2) Yeni birim testi geçiyor: 0/5 → %0, 3/5 → %60, 5/5 → %100, hiç görevi olmayan günde sıfıra bölme yok. (3) `./mvnw -Dtest=MimariTestleri test` yeşil. (4) Hafta 3 sayfasındaki kartta `3/5 görev (%60)` yazıyor.

Bu tarifle ajan **kendi kendini denetler**: kodu yazar → `./mvnw verify` çalıştırır → ArchUnit "web repo'ya erişemez" diye patlarsa hesabı `IlerlemeService`'e taşır → tekrar çalıştırır → yeşil. Sen yalnızca sonucu onaylarsın.

### Sık yapılan hata
- **Ölçülemez bitiş kriteri.** *"İlerleme yüzdesi düzgün görünsün, çalışsın."* — "düzgün"ü ölçen bir komut yok, hakem sensin. Doğrusu: *"`./mvnw verify` yeşil ve 3/5 senaryosunu doğrulayan yeni test geçiyor."* Hakem makine olunca ajan sen uyanmadan "bitti" diyemez, çünkü kırmızıysa bitmemiştir.
- **Kalıcı kuralı prompt'a doldurmak.** *"Katman yönüne uy"* her görevde geçerlidir; yeri `AGENTS.md`'dir (Hafta 2). *"content/'e dokunma"* yalnız bu göreve aittir; yeri spec'tir. Kalıcı kuralları her prompt'a kopyalamak, kıt olan bağlamı israf eder.

### Görevlere geçmeden önce
Spec, Hafta 1'deki **Guide** fikrinin en somut hâlidir: iş başlamadan yönlendirir. Şimdi kendi şablonunu kur ve gerçek bir işte dene. Yazarken kendine tek soru sor: *"Bu tarifi hiç tanımadığım bir ajana versem, 'bitti' dediğinde ona inanabilir miyim?"* Cevabın dayanağı çalıştırılabilir bir komutsa, tarifin hazırdır.

## Görevler
- [ ] Spec çatısını (Goal/Context/Constraints/Completion) bir şablon dosyası olarak repoya koy, ekibine uyarla
- [ ] Gerçek bir görevi bu şablonla yaz
