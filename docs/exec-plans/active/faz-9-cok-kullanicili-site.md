# ExecPlan: Faz 9 — pera ekibi için çok kullanıcılı eğitim sitesi

> Yaşayan belge. Yalnızca bu belgeden işi yeniden başlatmak mümkün olmalı.
> Kaynak: kullanıcı isteği (2026-10-22) — "pera'da birçok kullanıcı var; login olsun, her kullanıcı
> kendi durumunu görsün, herkes farklı hafta/günde olabilir". `docs/gelecek-cok-kullanicili-dagitim.md`
> notundaki ERTELEME bu istekle kalktı.

## Neden önemli (kullanıcı bakışı)
AI Öğretmen bugün tek kişilik, yerel bir araç. pera'nın 50–60 backend geliştiricisi aynı 12 haftalık
programı **bir eğitim sitesi gibi** (Udemy/Coursera hissi) kullanacak: tarayıcıdan girer, kendi
hesabıyla login olur, **yalnız kendi** ilerlemesini, quiz'lerini, sohbetini ve AI öğretmenin kendisi
hakkındaki hafızasını görür; herkes kendi hızında ilerler.

## Kararlar
### Kullanıcının kararları (2026-10-22)
1. **Kullanıcılar:** 50–60 kişi, hepsi backend geliştirici, web sitesinden erişir.
2. **Takip:** Yönetici/lider paneli YOK, sıralama tablosu YOK. Herkes yalnız kendi durumunu görür.
3. **AI maliyeti:** pera'nın **şirket OpenRouter hesabı**, tek merkezi anahtar. **Kişi başı kota YOK.**
4. **Login:** e-posta (kullanıcı adı) + şifre. Google/Microsoft SSO gerekmez.
5. **Kayıt:** herkes kendi kaydolur, ama **yalnız `@pera.software`** adresiyle.
6. **E-posta:** pera'nın elindeki 3 mail hesabından biri gönderici (SMTP) olarak kullanılır.
7. **Sunucu:** pera ayrı bir sunucu verecek.
9. **Yeni bağımlılıklar ONAYLANDI (2026-10-22):** `spring-boot-starter-mail` (9c) ve
   `org.postgresql:postgresql` (9d) — her biri için yine kısa ADR yazılır.
10. Brainstorming'i kısa kesti: "işi yapmaya başlayalım" — kalan ayrıntılar aşağıda "benim kararım".

### Benim kararlarım (kullanıcı onaylamadı; itiraz edilebilir)
- **E-posta doğrulaması zorunlu:** yalnız sonek kontrolü `uydurma@pera.software` kaydını engellemez;
  doğrulama bağlantısına tıklanmadan hesap açılmaz. "Şifremi unuttum" aynı e-posta altyapısıyla.
- **Kullanım sayacı kalır** (`ai_kullanim`, kullanıcı bazlı) — engellemez, yalnız görünürlük. Aylık
  harcama tavanı OpenRouter panelinden şirket tarafından konur (sitenin işi değil; önerildi).
- **Kaba kuvvet koruması:** aynı hesaba art arda başarısız login → geçici kilit (ör. 5 deneme / 15 dk).
- **Şifre:** BCrypt; en az 10 karakter.
- **Oturum:** sunucu tarafı HTTP oturumu (tek örnek/instance yeterli), çerez `HttpOnly`, `Secure`,
  `SameSite=Lax`. Spring Security CSRF token'ı açık.
- **Veritabanı:** sitede **PostgreSQL**; yerel geliştirme H2 ile çalışmaya devam eder.
- **Dağıtım:** Docker DEĞİL — uygulama sunucuda **systemd servisi**, `127.0.0.1`'e bağlı; önde HTTPS'li
  **nginx**. (Proje kuralı: uygulama 127.0.0.1 dışına bağlanmaz; Docker'da port yayınlamak 0.0.0.0
  gerektirirdi.) Adres örneği: `egitim.pera.software` (DNS'i pera açar).
- **Modlar:** mevcut tek kişilik yerel mod (kimliksiz, `yerel` kullanıcı) **bozulmaz**; çok kullanıcılı
  davranış yeni bir **`site` profili** ile açılır.
- **Sitede kapalı kalanlar:** mülakat modülü (ADR-0011), MCP sunucusu, kitap notu uçları (arayüzü yok;
  vektör deposu tüm kullanıcılar için ortak — kişiye ayırmak ayrı iş). `/api/kullanim` yalnız kendi
  kullanımını gösterir.
- **Kişisel dosya verisi:** öğrenci hafızası (`data/hafiza/`) ve rapor önbelleği (`data/raporlar/`)
  kullanıcı başına ayrı dizine taşınır; sıfırlama yalnız o kullanıcınınkini siler.
- **Hesap kapatma** (işten ayrılan): yönetici paneli yok; işletmen komut satırından/SQL ile
  `etkin=false` yapar (runbook'ta tarif edilir).
- **KVKK:** kayıt sayfasında kısa aydınlatma metni. **Self-servis hesap silme YOK** — kullanıcı kararı
  (2026-10-22): 9b'de eklenen "hesabımı ve verimi sil" kaldırıldı; ayrılan kullanıcının hesabını işletmen
  elle kapatır (`etkin=false`, ADR-0012 §9).

### Amaçlar / amaç dışı / başarısızlık
- **Amaç:** 60 kişinin aynı anda, birbirinin verisini görmeden, kendi hızında programı yürütmesi.
- **Amaç dışı:** yönetici paneli, raporlama, sıralama, SSO, kota, çok örnekli (cluster) dağıtım.
- **Başarısızlık (çalışsa bile):** bir kullanıcının başka birinin cevabını/sohbetini/hafızasını
  görebilmesi; pera dışından birinin kayıt olup şirket anahtarını harcayabilmesi; yerel modun bozulması.

## Mevcut durum (başlangıç noktası)
- DB tabloları zaten `kullanici_id` taşır, sorgular filtreli (ADR-0008); kimlik `KullaniciBaglami`
  arayüzünden gelir (`YerelKullaniciBaglami` = sabit `yerel`, `JwtKullaniciBaglami` = mcp-http).
- Tek kişilik kalanlar: `HafizaService` (`data/hafiza/OGRENCI.md`, `hafta-NN/notlar.md`),
  `RaporService` (`data/raporlar/`), `KitapService` + vektör deposu (ortak).
- `web/CsrfKoruma` izinli origin'i `127.0.0.1|localhost:<gerçek port>` sayar → nginx arkasında gerçek
  origin `https://<alan-adı>` olacağından **yapılandırılabilir izinli origin** gerekir.
- `spring-boot-starter-security` zaten bağımlılık (yeni bağımlılık değil).

## Fazlar
- [x] **9a — Kimlik (yeni bağımlılık YOK)** — 2026-10-22 tamam, ADR-0012; `SiteKimlikTest` + `SiteCerezTest` + yerel smoke (iki kullanıcı izole). Açık sızıntılar 9b listesinde (ADR-0012 Sonuçlar): `kullanici` tablosu (id, email, sifre_hash, dogrulandi,
  etkin, olusturma, basarisiz_deneme, kilit_bitis); `site` profilinde Spring Security form/oturum
  login + kayıt (`@pera.software` zorunlu) + çıkış; giriş/kayıt sayfaları (statik, CSP-temiz);
  oturumdan `KullaniciBaglami`; CSRF token + izinli origin yapılandırması; kaba kuvvet kilidi.
  E-posta doğrulaması bu fazda **arayüz/akış olarak hazır**, gönderim 9c'de bağlanır (o zamana dek
  `site` profili doğrulamasız hesap açmaz → yerel testte doğrulama bağlantısı loga yazılır).
  Testler: izolasyon (A, B'nin verisini göremez), login/kayıt/sonek reddi, yerel mod değişmedi.
- [x] **9b — Kişisel dosya verisi** — 2026-10-22 tamam (ADR-0012 §10): hafıza + rapor dizinleri kullanıcı
  başına (`<kök>/kullanicilar/<sha256(e-posta)>/`; `yerel` eski köke eşlenir → yerel veri taşınmadı);
  sıfırlama yalnız o kullanıcının DB satırları + hafıza + rapor önbelleği; `ai_kullanim.kullanici_id`
  (idempotent göç, eski satırlar `yerel`) → `/api/kullanim` yalnız kendi; `site`ta kitap uçları 404 +
  vektör deposu/kitap bağlamı kapalı, mülakat bayraktan bağımsız kapalı, MCP ile birlikte açılış hata;
  (9b'deki `POST /api/kimlik/hesabi-sil` + arayüz düğmesi 2026-10-22 kullanıcı kararıyla KALDIRILDI —
  hesap kapatma işletmende, `etkin=false`). Testler: `SiteKisiselVeriTest`, `SiteKapaliModullerTest`, `KisiselDizinTest`, `AiKullanimGocTest`,
  `SpringAiOgretmenClientTest` + birim güncellemeleri; `site` smoke (iki/üç kullanıcı) geçti.
- [x] **9c — E-posta (`spring-boot-starter-mail`, ADR-0013)** — 2026-10-22 tamam: `SmtpEpostaGonderici`
  (SMTP ayarları yalnız env; STARTTLS zorunlu; arka plan kuyruğu, hata isteği bozmaz, sır loglanmaz);
  `site` SMTP'siz açılmaz (`APP_SITE_EPOSTA_ZORUNLU=false` yalnız yerel deneme → log gönderici); Türkçe
  düz metin + HTML şablonlar (`resources/eposta/`); "şifremi unuttum" (nötr yanıt, 1 saatlik tek
  kullanımlık token, başarıda kilit temiz + tüm oturumlar düşer) + "doğrulama e-postasını yeniden
  gönder"; adres+tür başına saatte 3 e-posta. Testler: `SmtpEpostaGondericiTest` (sahte JavaMailSender),
  `SiteSifreSifirlamaTest`, `SiteEpostaSecimiTest`, `EpostaHizSiniriTest`. Aynı turda self-servis hesap
  silme kaldırıldı (yukarı bkz.).
- [x] **9d — PostgreSQL + dağıtım paketi (`org.postgresql:postgresql`, ADR-0014)** — 2026-10-22 tamam:
  `site` veritabanını yalnız `SPRING_DATASOURCE_URL/_USERNAME/_PASSWORD`'dan alır (yoksa açılmaz —
  `SiteVeritabaniDenetimi`; sürücü URL'den); tek taşınabilir `schema.sql` (`IDENTITY` → `GENERATED BY
  DEFAULT AS IDENTITY`, `CLOB` → `TEXT`, `ADD CONSTRAINT IF NOT EXISTS` → `CREATE UNIQUE INDEX IF NOT
  EXISTS`); Spring Data JDBC tırnaklaması kapalı (`config/JdbcAdlandirma` — PostgreSQL `"KULLANICI"`
  sorunu); XSRF çerezi vekil arkasında https ise Secure. Dağıtım: `deploy/` (systemd birimi, nginx
  şablonu, `site.env.ornek`, `postgres-kurulum.sql.ornek`) + runbook `docs/kurulum-site.md`. Testler:
  `SemaTasinabilirlikTest`, `SiteVeritabaniTest`, `SiteVekilBasliklariTest` (gerçek Tomcat,
  X-Forwarded-*), opt-in `SitePostgresEntegrasyonTest` (`PG_TEST_URL`; yerel PostgreSQL 14'te geçti).
- [x] **9e öncesi — ekip kodu ile kayıt (ADR-0015)** — 2026-10-22: pera ekibi e-posta gönderimini
  istemedi. Varsayılan `APP_SITE_KAYIT=ekip-kodu`: kayıt `{email, sifre, ekipKodu}`, kod sabit zamanlı
  karşılaştırılır, hesap doğrulanmış açılır (`{"durum":"kayit-tamam"}`); `APP_SITE_EKIP_KODU` yok/<12
  karakter → site açılmaz; SMTP gerekmez; `dogrula`/`dogrulama-yeniden`/`sifremi-unuttum` 404,
  `sifre-sifirla` açık. Kimliksiz `GET /api/kimlik/ayarlar` ön yüze modu söyler. İşletmen
  `tools/sifre-baglantisi.sh` (psql ile Java'yla aynı SHA-256 token satırı, 1 saat). E-posta modu
  `APP_SITE_KAYIT=eposta` ile aynen duruyor. Testler: `SiteEkipKoduTest`, `SiteEkipKoduAcilisTest`,
  `KimlikServiceOzetTest`, opt-in `SitePostgresEntegrasyonTest` (betik uçtan uca).
- [ ] **9e — Sunucuya kurulum:** pera sunucusu + DNS bilgileri gelince (kullanıcıyla birlikte); ekip kodu
  üretilip ekibe iletilir (SMTP gerekmez).

## Doğrulama
`./mvnw verify` yeşil (ArchUnit dahil); her fazda yeni davranışa test; 9a/9b sonunda `site`
profiliyle uygulamayı yerelde açıp iki kullanıcıyla uçtan uca izolasyon denemesi.
