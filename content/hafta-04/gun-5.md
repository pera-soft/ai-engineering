---
hafta: 4
gun: 5
baslik: "LLM-dostu hata mesajları + quiz"
---

## Ders

### Bugün ne öğreneceksin?
Bu hafta çitleri kurdun: mekanik zorlama (Gün 1), ArchUnit (Gün 2), import-linter (Gün 3), güvenlik kapıları (Gün 4). Bugün son adım: bu çitleri **ajana okunur** yapmak. Çünkü kırmızı yanan ama ne yapılacağını söylemeyen bir kontrol, ajanı düzeltmeye değil **tahmine** iter. Haftayı quizle kapatacaksın.

### Temel kavramlar
- **Hata mesajı = arayüz:** ihlal anında ajanın gördüğü tek şey mesajdır. Mesaj neyse, ajanın bildiği odur.
- **Remediation guidance (düzeltme talimatı):** hatanın **nasıl** giderileceğini söyleyen kısım. İyi mesajın ayırt edici parçası budur.
- **Kör mesaj:** yalnız "bir şey bozuldu" diyen mesaj. Kural adı ve satır numarası verir, çıkış yolu vermez.
- **`.because(...)`:** ArchUnit'te kuralın gerekçesini **ihlal çıktısına basan** cümle. Javadoc yorumundan farkı: yorumu ajan ancak dosyayı açarsa görür, `.because(...)` metni **hata çıktısına** girer.
- **Kapanan döngü:** ajanın kırmızıyı görüp kendi kendine düzeltmesi ve sana hiç sormaması.

Benzetme: **yazıcı.** Kâğıdı bitmiş bir yazıcı ekrana `Error 0x8007000E` yazarsa arama motoruna koşarsın. **"Kâğıt bitti — 2. çekmeceye A4 koyun"** yazarsa 20 saniyede halledersin. Hata aynı hatadır; fark, mesajın **ne yapacağını söylemesidir**.

Ajan için de böyledir, ama bir farkla: **ajanın arama motoru yoktur, senin reponden başka referansı yoktur.** Mesaj körse tahmine başlar; en sık seçtiği yol kuralı gevşetmek ya da testi devre dışı bırakmaktır — yani çiti kesip patikayı açmak (Gün 1).

İyi mesajın üç parçası vardır:
- **NE bozuldu** — ihlal edilen kural, insan diliyle (yalnız kural adıyla değil).
- **NEDEN yanlış** — hangi ilke veya karar çiğnendi. Ajanın "kuralı silerim" refleksini kesen kısım budur.
- **NASIL düzeltilir** — somut adım ve **nereye bakacağı**. Mesajın içine ansiklopedi koyma, **adres** koy (Hafta 2'deki işaretçi mantığı).

### Somut örnek
Bu projede `web` katmanı `repo`'ya doğrudan eriştiğinde:

- **Kör mesaj (kötü):** `ERROR: layer violation in MimariTestleri.katman_kurali` — Ajanın anladığı: "bir şey kırıldı." Muhtemel tepkisi: testi devre dışı bırakmak ya da katman tanımını değiştirmek.
- **Yol gösteren mesaj (iyi):**
  `ERROR: web katmanı repo'ya doğrudan erişemez (ProgramController → IlerlemeRepo).`
  `[Neden: katman yönü types → config → repo → service → ai → web tek yönlüdür; web yalnız service'i çağırır.]`
  `[Düzeltme: Çağrıyı service/IlerlemeService üzerinden yap. Kuralı gevşetme. Gerçekten yeni bir katman gerekiyorsa önce docs/adr/ altına ADR aç — bkz AGENTS.md katman kuralı.]`

Döngü artık insan beklemeden kapanır: ajan kodu yazar → `./mvnw verify` → **kırmızı** → mesajı okur → çağrıyı `IlerlemeService`'e taşır → tekrar çalıştırır → **yeşil**. Sen bu döngünün hiçbir adımında yoksun.

Uygulama ayrıntısı: `MimariTestleri` içindeki `[Guidance: ...]` notları şu an **Javadoc yorumunda** duruyor — ajan dosyayı açarsa görür, hata çıktısında **görmez**. Talimatı çıktıya taşımanın yolu, kurala `.because(...)` eklemektir: `.because("web yalnız service'i çağırır; çağrıyı IlerlemeService'e taşıyın — bkz AGENTS.md")`. Kural aynı kalır; **ajana ulaşan bilgi** değişir.

### Sık yapılan hata
İki uçtan biri: mesajı **çok kısa** bırakmak (yalnız kural adı) ya da **çok uzun** yazmak (mesaja üç paragraf mimari felsefesi doldurmak). İkincisi de zarar verir; her ihlalde o metin bağlamı işgal eder. Doğru denge: kısa gerekçe + tek somut adım + bir **adres**. Üçüncü hata, "neden" kısmını atlamaktır — gerekçesiz bir yasak, ajan için gevşetilecek keyfî bir engeldir.

### Görevlere geçmeden önce
Ölçüt şu: hata mesajını, kod tabanını hiç tanımayan birine göster. *"Şimdi ne yapmalıyım?"* sorusuna mesajın kendisi cevap veriyorsa mesaj LLM-dostudur; cevap için başka yere bakması gerekiyorsa **o adresi mesaja ekle**. Bugün bir özel lint kuralını seçip mesajını üç parçalı hâle getir, sonra haftalık quizi çöz.

## Görevler
- [ ] 1 özel lint kuralına remediation guidance ekle
- [ ] Haftalık quiz'i çöz
