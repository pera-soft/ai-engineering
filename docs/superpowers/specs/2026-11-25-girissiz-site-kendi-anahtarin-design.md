# Tasarım — Girişsiz site + "kendi OpenRouter anahtarın" ile sohbet

- **Tarih:** 2026-11-25
- **Durum:** onaylandı ve uygulandı (2026-11-25); karar kaydı: [ADR-0017](../../adr/0017-girissiz-site-ve-kendi-anahtarin.md). Kapsam: site internete açılmaya hazır biçimde — hız sınırları, 180 gün
  temizliği, `deploy/` ve kurulum belgesi bu işe dahil, ertelenmez (*benim kararım*: kullanıcı bana bıraktı;
  "sıfır teknik borç" kuralı + açıldığı gün güvenli olsun).
- **Etkilediği kararlar:** ADR-0012 (site profili), ADR-0013 (SMTP), ADR-0015 (ekip kodu) → yeni **ADR-0017**
  ile değişir/geçersiz olur. ADR-0014 (PostgreSQL) ve ADR-0016 geçerli kalır.

## 1. Niyet (neden)

Site bugün yalnız Pera ekibine açık: `@pera.software` e-postası + ekip kodu ile kayıt, şifreli giriş, sunucunun
(şirketin) tek OpenRouter anahtarı. Kullanıcı bunu istemiyor:

- **Pera'ya özel her şey kalksın.** Site herkese açık bir eğitim sitesi olsun.
- **Giriş/kayıt hiç olmasın.** Gelen kişi doğrudan çalışmaya başlasın.
- **Sohbet botu isteğe bağlı olsun;** kullanmak isteyen **kendi OpenRouter anahtarını** girsin → AI maliyeti
  sitenin değil, kullananın.
- **Haftalık rapor, AI değerlendirme vb. sitede gerekmiyor** — AI yalnız sohbet botu.

## 2. Hedefler / hedef olmayanlar / başarısızlık sayılacaklar

**Hedefler**
1. `site` profilinde kimlik sistemi (kayıt, giriş, şifre, e-posta doğrulama, şifre sıfırlama, ekip kodu, SMTP)
   tamamen kalkar; Pera'ya özel metin/kural/örnek kalmaz.
2. Her ziyaretçi tarayıcısındaki **rastgele kimlik çereziyle** tanınır; ilerlemesi sunucuda bu kimlikle saklanır.
3. **İlerleme kodu:** ziyaretçi kimliğini görüp başka tarayıcıda geri yükleyebilir.
4. Sohbet paneli, ziyaretçinin girdiği OpenRouter anahtarıyla çalışır; anahtar yoksa panel "anahtarını ekle"
   der, sitenin geri kalanı tam çalışır.
5. Anahtarsız ziyaretçi görevleri **kendisi işaretler**; anahtarı olan mevcut "AI öğretmenle geç" akışını
   (oku → yansıtma sorusu, tartış → öğretmen "geçtin") kullanır.

**Hedef olmayanlar**
- Yerel tek kullanıcı modu (profil yok, `OPENROUTER_API_KEY` sunucu anahtarı) **olduğu gibi kalır** — rapor,
  değerlendirme, AI quiz, kitap notu, mülakat orada çalışmaya devam eder.
- `mcp` / `mcp-http` (JWT) profilleri değişmez.
- "OpenRouter ile bağlan" (OAuth/PKCE) düğmesi — sonraki adım (bkz. §4 Yaklaşımlar, B).
- Hesap, e-posta, kullanıcı adı, profil — hiçbiri yok.

**Başarısızlık sayılacaklar (çalışsa bile)**
- Ziyaretçinin anahtarı sunucuda **kalıcı** bir yere düşerse (veritabanı, log, hata mesajı, `ai_kullanim`,
  hafıza dosyası, istisna yığını).
- Bir ziyaretçi başka ziyaretçinin ilerlemesini/sohbetini/hafızasını görebilirse.
- Sunucu, anahtar yokken herhangi bir yoldan **sunucu anahtarıyla** AI çağırırsa (sitede sunucu anahtarı yok).
- Anahtarsız ziyaretçi bir haftayı bitiremezse (kilitli kalırsa).

## 3. Kısıtlar

- Katman yönü `types → config → repo → service → ai → web` (ArchUnit); AI çağrısı yalnız `ai/` paketinden,
  prompt'lar `prompts/` dizininden.
- Uygulama yalnız `127.0.0.1`'e bağlanır; public erişim nginx/TLS önünden.
- Şema `schema.sql` ile (her açılışta, idempotent `IF NOT EXISTS`); Liquibase/Flyway yok. Canlıda PostgreSQL,
  yerelde H2 — ikisinde de çalışmalı (`SitePostgresEntegrasyonTest`).
- Tüm kullanıcıya görünen metin Türkçe; kullanıcı İngilizce bilmiyor (anahtar alma yardımı Türkçe olmalı).
- Yeni bağımlılık eklenmez (gerekirse önce ADR). Mevcut `spring-boot-starter-mail` sitede gereksizleşir —
  yerel modda da kullanılmıyorsa kaldırılır.
- Tek sunucu örneği (systemd, tek JVM) — bkz. §6.6 zamanlanmış iş.

## 4. Yaklaşımlar

### 4.1 İlerleme nerede tutulur (kullanıcı seçti: **1**)
1. **Tarayıcıya rastgele kimlik çerezi, veri sunucuda** — *seçildi.* Mevcut `kullanici_id` izolasyonu aynen
   kullanılır; değişen yalnız "aktif kullanıcı"nın kaynağı (e-posta → çerez kimliği). Eksisi: çerez silinirse
   ilerleme kaybolur → ilerleme kodu ile hafifletilir.
2. Her şey yalnız tarayıcıda (localStorage) — reddedildi: ilerleme/hafıza katmanını baştan yazmayı gerektirir,
   AI hafızası her istekte tarayıcıdan taşınmak zorunda kalır.

### 4.2 Anahtar nasıl alınır/kullanılır (kullanıcı seçti: **A**)
- **A. Sohbet paneline yapıştır; anahtar tarayıcıda durur, her sohbet isteğiyle gönderilir, sunucu o istekte
  kullanıp unutur** — *seçildi.* En basit; sunucuda kimsenin sırrı birikmez.
- B. "OpenRouter ile bağlan" (OAuth PKCE) — A'nın üstüne sonradan eklenebilir; şimdilik kapsam dışı.
- Reddedilenler: anahtarı sunucuda çerez kimliğiyle saklamak (DB sızıntısı = herkesin anahtarı sızar);
  tarayıcının OpenRouter'ı doğrudan çağırması (sunucudaki bağlam/prompt'lar ve "AI yalnız `ai/`" kuralı bozulur).

### 4.3 Anahtarsız ziyaretçi görevleri nasıl bitirir (kullanıcı seçti: **1**)
1. **Anahtarsız → kendisi işaretler; anahtarlı → AI öğretmenle geçer** — *seçildi.*
2. Herkes kendisi işaretler, AI kapısı tamamen kalkar — reddedildi (AI'la geçme deneyimi kaybolur).
3. Kilit tamamen kalkar — reddedildi.

## 5. Kullanıcının kararları (aynen)

- Giriş/kayıt yok. Pera'ya özel her şey kalkar.
- İlerleme: çerez kimliği + sunucuda veri; "ilerleme kodunu kaydet/geri yükle" olur.
- AI anahtarı **yalnız sohbet botu için** istenir; rapor, AI değerlendirme vb. sitede yok.
- Anahtar: sohbet paneline yapıştır (A).
- Anahtarsız kişi görevleri kendisi işaretler; anahtarlı AI öğretmenle geçer.

## 6. Tasarım

### 6.1 Ziyaretçi kimliği (çerez)
- Çerez adı `aio_ziyaretci`; değer: `SecureRandom` ile 128 bit rastgele, URL-güvenli Base64 (ya da UUIDv4).
  `HttpOnly`, `SameSite=Lax`, `Secure` (mevcut `guvenli-cerez` kuralı: true → her zaman; false → yalnız
  `X-Forwarded-Proto=https` ise), `Path=/`, ömür **1 yıl**, ziyarette yenilenir (kaydırmalı).
- Çerez **yoksa veya biçimi geçersizse** filtre yeni kimlik üretir ve yazar. Biçim doğrulaması zorunlu
  (çerezle gelen keyfi metin `kullanici_id`'ye ya da dosya yoluna — `data/hafiza/<id>` — girmesin; mevcut
  `KisiselDizin` yol güvenliği korunur).
- `kullanici_id` = `z-<kimlik>` (önek, eski `yerel` ve e-posta kimliklerinden ayırt etmek için — *builder'a
  bırakılan biçim ayrıntısı, ama önek şart*).
- Yeni `KullaniciBaglami` uygulaması (`ZiyaretciKullaniciBaglami`, `@Profile("site")`) mevcut
  `OturumKullaniciBaglami`'nin yerini alır; **fail-closed** kalır (kimlik yoksa hata, asla `yerel`'e düşmez).
- **Satır yalnız ilk yazmada oluşur** (görev işaretleme, sohbet, quiz) — sadece bakıp çıkan ziyaretçi DB'de iz
  bırakmaz. `ziyaretci` tablosu: `id`, `olusturma`, `son_gorulme` (bkz. §6.6). `son_gorulme` en çok günde bir
  güncellenir (her istekte yazma yok).
- Sahiplik: hiçbir uç dışarıdan `kullanici_id` almaz; her sorgu çerezden gelen kimlikle süzülür (bugünkü
  izolasyon testleri — `SiteKisiselVeriTest` — ziyaretçi kimliğiyle yeniden yazılır).

### 6.2 İlerleme kodu
- Üst barda/ayarlarda **"İlerleme kodum"**: kimliği gösterir + kopyala düğmesi + uyarı metni ("Bu kod şifre
  gibidir; kim bilirse ilerlemeni görür. Başka tarayıcıda devam etmek için orada 'Geri yükle'ye yapıştır.").
- `GET /api/ziyaretci/kod` → mevcut kod. `POST /api/ziyaretci/geri-yukle {kod}` → kod biçimi geçerli **ve**
  `ziyaretci` tablosunda varsa çerezi o kimliğe çevirir; yoksa 422 ("Bu kodla kayıtlı ilerleme bulunamadı").
  Bu tarayıcının önceki (anonim) ilerlemesi birleştirilmez — üzerine geçilir; ön yüz bunu onaylatır.
- Kaba kuvvet: 128 bit tahmin edilemez; yine de `geri-yukle` IP başına hız sınırlı (mevcut
  `EpostaHizSiniri`/`GirisDenemeTakibi` deseninden uyarlanır).
- **Yazma uçlarının tümü** (`/api/gorev/durum`, `/api/quiz/statik-sonuc`, `/api/sifirla`, `/api/sohbet`,
  `geri-yukle`) IP başına hız sınırlı — girişsiz sitede bir betik her istekte yeni çerezle sınırsız `ziyaretci`
  satırı üretemesin (*benim kararım*). Ayrıca **yeni ziyaretçi satırı oluşturma** IP başına saatlik sınırlı.
- Mevcut `POST /api/sifirla` (temiz başlangıç) aynen kalır.

### 6.3 Sohbet anahtarı (A)
**Ön yüz**
- Sohbet paneli, anahtar yoksa mesaj kutusu yerine kısa bir kart gösterir: "AI öğretmenle sohbet için kendi
  OpenRouter anahtarını ekle" + anahtar alanı + "Bu cihazda hatırla" (işaretliyse `localStorage`, değilse
  `sessionStorage`) + "Anahtarı nasıl alırım?" açılır yardımı (Türkçe adımlar: openrouter.ai hesap → Keys →
  Create Key → **harcama limiti koy** → yapıştır; maliyetin kendisine yazılacağı açıkça söylenir).
- Anahtar varken panelde "Anahtarı kaldır" bağlantısı.
- Anahtar yalnız **sohbet uçlarına** giden isteklerde `X-OpenRouter-Anahtar` başlığıyla gönderilir (başka
  hiçbir uca gönderilmez).
- Okuma sonrası yansıtma sorusu ve tartış görevi bugün sohbet üstünden yürüyor → anahtarlıyken aynen çalışır.

**Sunucu**
- Başlık yalnız sohbet uç(lar)ında okunur; biçim kontrolü (boş değil, makul uzunluk sınırı, `sk-or-` öneki
  beklenir — önek yoksa 400 Türkçe mesaj). Başlık yoksa **400/422 "anahtar gerekli"** — sunucu anahtarına
  düşmek YOK (sitede sunucu anahtarı tanımlı olmaz; `site.env` şablonundan `OPENROUTER_API_KEY` kalkar).
- AI istemcisi istek başına anahtarı alır: `ai/` paketinde, verilen anahtarla `OpenAiApi`/`ChatModel` kuran bir
  yol (yerel mod mevcut tek-anahtarlı bean ile kalır). Anahtar servis katmanından `ai/`'ye **parametre ya da
  istek-kapsamlı bağlam** ile taşınır — hangi mekanizma olduğu builder'ın kararı, ama:
  - anahtar hiçbir `record`/sınıfın `toString()`'inde görünmez (maskeleme kaynağında),
  - loglanmaz, istisna mesajına girmez, `ai_kullanim`'a yazılmaz,
  - istek bittikten sonra bellekte tutulmaz (anahtar başına istemci önbelleği **yok**; gerekirse ayrı karar).
- AI istemcisi bugün açılışta bir kez kurulur; sitede sunucu anahtarı olmadığından bean devre dışı taslaktır
  (`aktif()=false`). Kişi başı anahtar için **yeni bir istemci yolu** gerekir ve `aktif()` kontrollerinin
  (`SohbetService`, `HafizaService` …) sitedeki anlamı "bu istekte ziyaretçi anahtarı var mı" olur.
- Sohbetin her 4 mesajda bir tetiklediği **öğrenci hafızası güncellemesi** (`SohbetService.hafizaGerekirseGuncelle`
  → `HafizaService.guncelle`) istek iş parçacığında **senkron** çalışır → aynı isteğin anahtarıyla yapılır.
  Bugün `aiClient.aktif()` false ise sessizce atlanıyor — sitede anahtarlı istekte çalışması sağlanmalı (test).
  İstisnayı yutup `e.getMessage()` logluyor → bu log satırına anahtarın sızamadığı test edilir.
- OpenRouter hata eşlemesi `AiHataCevirici.kodaGore`'de zaten var (401/403, 402, 429); yalnız Türkçe mesajlar
  ziyaretçi anahtarına göre yazılır: 401 → "Anahtar geçersiz", 402 → "OpenRouter kredin bitti / limitine
  ulaştın", 429 → "Çok hızlı, biraz bekle". Ön yüz 401'de "anahtarı değiştir" önerir.
- Model: sunucu ayarındaki modeller aynen — sohbet `guclu`, hafıza güncellemesi `hizli`; ziyaretçi seçmez.
- Kötüye kullanım: sunucu bir "açık vekil" olmasın → sohbet ucu ziyaretçi + IP başına hız sınırlı, mevcut mesaj
  uzunluğu sınırları korunur, sistem prompt'u ve model sabit.
- `ai_kullanim` sitede ziyaretçi kimliğiyle token/süre kaydını sürdürür (anahtar olmadan).
- **`aiAktif` bayrağı ikiye ayrılır** (*benim kararım*): bugün ön yüz `aiAktif`'i `/api/kullanim`'dan (sunucu
  anahtarı) alıyor ve bu tek bayrak sohbet, rapor, AI quiz, yaz değerlendirmesi ve görev kapısını birlikte
  yönetiyor. Yeni: (a) **sunucu AI özellikleri** → `/api/ozellikler`'den (sitede hep kapalı); (b) **ziyaretçinin
  anahtarı var mı** → tarayıcıdaki anahtardan. `/api/kullanim` sitede yalnız ziyaretçinin kendi kullanımını
  gösterir; sayfa açılışı ona bağlı olmaz.

### 6.4 Görev işaretleme
- `/api/ozellikler` sitede `site: true`, `kullanici` alanı **kalkar** (e-posta yok).
- Bugünkü gerçek: "AI kapısı" yalnız ön yüzde. Yansıtma sorusu ve tartış açılışı `app.js`'te sabit metin;
  başarılı bir `/api/sohbet` cevabından sonra ön yüz `mesajSubstantiveMi` ile mesajı yeterli bulursa
  `/api/gorev/durum`'u çağırır. Sunucu hiçbir şeyi zorlamaz; bu tasarım da zorlatmaz.
- Anahtarsız işaretleme kısmen var (`aiAktif=false` iken okuma doğrudan tamamlanıyor) ama yaz/tartış "AI kapalı"
  uyarısı veriyor ve bayrak sunucu anahtarına bağlı. Yeni kural ziyaretçi anahtarına bağlanır (§6.3 bayrak ayrımı).
- Ön yüz, anahtar **yoksa**: oku görevi "Okudum ✓", yaz görevi "Yaptım ✓", tartış görevi "Tartıştım/geç ✓"
  ile doğrudan `POST /api/gorev/durum` çağırır. Kilit/sıra mantığı aynen işler (gün/hafta açılır, quiz açılır).
- Anahtar **varsa**: oku ve tartış mevcut sohbet akışını kullanır. **Yaz** görevi AI değerlendirmesi sitede
  olmadığından herkes için "Yaptım ✓" ile işaretlenir (*benim kararım — değerlendirme sitede yok*).
- Ana sayfadaki "Nasıl çalışırım" metni (index.html §2–4) sitede yeni akışa göre yazılır: kutucukları
  kendin de işaretleyebilirsin; AI öğretmen isteğe bağlı; rapor bölümü sitede görünmez.

### 6.5 Sitede kapanan özellikler
- Uçlar `site` profilinde **kapalı** (404; kayıtlı bile değilse en iyisi — mevcut `MulakatEtkin`/`CalismaModu`
  deseniyle): `/api/degerlendir`, `/api/ornek`, `/api/aciklama`, `/api/quiz/uret`, `/api/rapor/**`,
  `/api/atolye/**` (değerlendirme), `/api/kitap-notu/**`, `/api/kitaplar` (zaten sitede kapalı), mülakat
  (zaten kapalı).
- Ön yüz bu özelliklerin düğmelerini/bölümlerini sitede **göstermez** (pasif değil, yok). Kaynak: `/api/ozellikler`.
- Statik quiz, içerik, kaynaklar (`/api/kaynak/**`), ilerleme, sohbet geçmişi (`GET /api/sohbet/{hafta}`)
  çalışır.
- Yerel mod bu özelliklerin hepsini sunucu anahtarıyla eskisi gibi çalıştırır.

### 6.6 Terk edilmiş ziyaretçi verisinin silinmesi
- Zamanlanmış iş: `son_gorulme` **180 günden** eski ziyaretçilerin tüm satırları (görev, gönderim, quiz, sohbet,
  ai_kullanim, ziyaretci) ve `data/hafiza/<id>`, `rapor` dizinleri silinir. Mevcut `SifirlamaService`
  silme mantığı yeniden kullanılır.
- Günde bir çalışır. **ShedLock yok** — gerekçe: site tek JVM (systemd, tek örnek; `deploy/`); işin kendisi
  idempotent (iki kez çalışsa zarar yok). Bu sapma ADR-0017'de yazılır.

### 6.7 Kaldırılan kimlik sistemi (site profili)
- Giriş/kayıt/doğrulama/şifre sayfaları ve `kimlik.js`; `KimlikController`, `KimlikService`,
  `KullaniciDetayServisi`, `GirisDenemeTakibi`, `EpostaGonderici*`, `SmtpEpostaGonderici`,
  `LogEpostaGonderici`, `EpostaHizSiniri` (hız sınırı deseni §6.2/§6.3'e taşınacaksa uyarlanır),
  `KullaniciRepo`, `KullaniciTokenRepo`, `types/Kullanici*`, `KayitGecersizException`, e-posta şablonları,
  `tools/sifre-baglantisi.sh`, `OturumKullaniciBaglami`, `SessionRegistry`/`HesapEtkinlikFiltresi`/form login.
  Tam liste builder'ın; ölçüt: **yalnız kimlik sistemi için var olan her şey gider.**
- `SiteAyarlari`: `kayit`, `ekip-kodu`, `eposta-zorunlu`, `gonderici`, `temel-url` (yalnız e-posta
  bağlantıları için ise) kalkar; `izinli-originler`, `guvenli-cerez` kalır.
- Güvenlik zinciri (sitede): tüm sayfalar ve `/api/**` herkese açık (kimlik doğrulama yok); **CSRF korunur**
  (çerez tabanlı kimlik olduğu için durum değiştiren uçlar hâlâ CSRF'e açık) — mevcut `XSRF-TOKEN` çerez
  deposu + `CsrfKoruma` (Origin/Sec-Fetch-Site) kalır. Oturum (`JSESSIONID`) **oluşturulmaz** (stateless).
- **Veritabanı:** `kullanici` ve `kullanici_token` tabloları `schema.sql`'den çıkarılır (yeni kurulumda
  oluşmaz); mevcut veritabanında **silinmez** (DROP yok — geri dönülmez işlem; canlı site 2026-10-21'den beri
  kapalı, ama yine de veri silme ayrı ve bilinçli bir adım). Eski e-posta `kullanici_id`'li satırlar yeni
  modelde sahipsiz kalır ve §6.6 temizliğine girmez (ziyaretci tablosunda yoklar) — gerekirse ayrı karar.
- Pera metinleri: "AI Öğretmen · pera" başlıkları, `@pera.software` placeholder'ları, `deploy/` ve
  `docs/kurulum-site.md` örnek alan adı `egitim.pera.software` → nötr örnek (`egitim.ornek.com`), testlerdeki
  `@pera.software` adresleri (kalan testlerde) nötr örneklerle değişir. Pera adı yalnız site sayfalarında,
  e-posta şablonlarında ve site ayarlarında geçiyor (`index.html`/`app.js`'te yok) → temizlik iki modu da
  etkiler, yerel mod davranışı değişmez (*benim kararım: Pera her yerden çıkar*).

### 6.8 Belgeler
- **ADR-0017** (Türkçe): girişsiz site + ziyaretçi çerezi + kendi anahtarın; ADR-0012'yi değiştirir, 0013 ve
  0015'i geçersiz kılar; ShedLock sapması gerekçesi.
- `CLAUDE.md` komutları (ekip kodu, SMTP, şifre bağlantısı satırları kalkar; site çalıştırma güncellenir),
  `ARCHITECTURE.md`, `docs/kurulum-site.md`, `deploy/site.env.ornek` (OpenRouter anahtarı yok).
- Bitince Obsidian Vault notu (rehbere göre).

### 6.9 Testler (her davranış değişikliğine en az bir test)
- Çerezsiz ilk istek → geçerli çerez yazılır; geçersiz biçimli çerez → yenisi üretilir; yol enjeksiyonu
  (`../`) çerezle hafıza dizinine ulaşamaz.
- İki ziyaretçi birbirinin görev/sohbet/hafızasını göremez (`SiteKisiselVeriTest` uyarlaması).
- İlerleme kodu: geri yükleme var olan kimliğe geçer; olmayan kod 422; hız sınırı.
- Sohbet: başlıksız → anahtar gerekli hatası, AI çağrılmaz; başlıklı → verilen anahtar AI istemcisine ulaşır
  (sahte istemciyle); anahtar loglarda ve `ai_kullanim`'da yok; OpenRouter 401/402 → Türkçe hata.
- Kapalı uçlar sitede 404; yerel modda çalışır.
- Kimlik uçları/sayfaları sitede yok (404).
- Temizlik işi: 180 gün eski ziyaretçinin verisi silinir, yenisi kalır.
- `SitePostgresEntegrasyonTest` yeni şemayla geçer; `MimariTestleri` yeşil.

## 7. Builder'a bırakılanlar
- Anahtarın servis → `ai/` taşınma mekanizması (parametre ya da istek-kapsamlı bean), §6.3 kurallarına uymak şartıyla.
- Kimlik çerezinin kesin biçimi (UUIDv4 / Base64), `z-` öneki şartıyla.
- Hız sınırı değerleri; uzunluk sınırları.
- `/api/kullanim` sitede kalsın mı.
- Uygulama sırası ve commit bölümlemesi (küçük, tek amaçlı commit'ler; `[faz-10] ...`).
