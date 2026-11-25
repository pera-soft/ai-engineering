# Girişsiz site + kendi OpenRouter anahtarın — Uygulama Planı

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `site` profilinde giriş/kayıt sistemini kaldırıp ziyaretçiyi rastgele çerezle tanımak; sohbet botunu
ziyaretçinin kendi OpenRouter anahtarıyla çalıştırmak; sohbet dışı AI özelliklerini sitede kapatmak.

**Architecture:** "Aktif kullanıcı" kaynağı e-posta oturumundan `aio_ziyaretci` çerezine geçer
(`KullaniciBaglami` yeni uygulaması); mevcut `kullanici_id` izolasyonu aynen kalır. AI istemcisi `ChatModel`'i
bir kaynaktan alır: yerelde sabit (sunucu anahtarı), sitede istek başına ziyaretçinin `X-OpenRouter-Anahtar`
başlığından kurulur. Kimlik sistemi (form login, e-posta, şifre, ekip kodu) silinir.

**Tech Stack:** Java 17, Spring Boot 3.5, Spring Security, Spring AI (OpenAI-uyumlu → OpenRouter), Spring Data
JDBC, H2/PostgreSQL, vanilla JS ön yüz.

**Spec:** `docs/superpowers/specs/2026-11-25-girissiz-site-kendi-anahtarin-design.md`

## Global Constraints

- Katman yönü `types → config → repo → service → ai → web` (ArchUnit, `MimariTestleri`); Spring AI sınıfları yalnız `ai/`.
- Tüm kullanıcıya görünen metin Türkçe; hata gövdesi mevcut sözleşme `{"hata": "..."}` (`HataYonetimi`) — değişmez.
- Yeni Maven bağımlılığı YOK. `spring-boot-starter-mail` kaldırılır (Task 2).
- Şema yalnız `schema.sql` (idempotent `IF NOT EXISTS`); H2 ve PostgreSQL'de çalışmalı. `DROP TABLE` YOK.
- Uygulama yalnız `127.0.0.1`; `server.forward-headers-strategy: native` (site) korunur → `request.getRemoteAddr()` istemci IP'si.
- Ziyaretçi anahtarı hiçbir `toString()`, log satırı, istisna mesajı, `ai_kullanim` satırı veya dosyada görünmez.
- Yerel mod (profil yok) ve `mcp`/`mcp-http` davranışı değişmez.
- Commit: küçük, tek amaçlı, mesaj `[faz-10] özet`; commit **`/commit` skill'i ile** (doğrudan `git commit` değil).
  Her task sonunda `./mvnw verify` yeşil.
- Sabitler: çerez `aio_ziyaretci`, ömür 365 gün; `kullanici_id` = `"z-" + kimlik`; kimlik = 16 bayt `SecureRandom`
  → URL-güvenli Base64 (padding'siz, 22 karakter), doğrulama deseni `^[A-Za-z0-9_-]{22}$`; başlık
  `X-OpenRouter-Anahtar`, kabul deseni `^sk-or-[A-Za-z0-9_-]{10,200}$`; temizlik eşiği 180 gün.

## Review Focus

1. **Aynı anda iki sekmede ilk ziyaret** (çerez henüz yok, iki paralel istek) → iki farklı kimlik oluşur; ikinci
   yazma ilkini ezmemeli, `ziyaretci` upsert'i çakışmada hata vermemeli. Test: Task 3 `esZamanliDokunmaHataVermez`.
2. **Anahtar OpenRouter'da geçersiz (401)** → kullanıcı Türkçe "anahtar geçersiz" görür, sohbet kaydı yazılmaz,
   anahtar log/hata mesajında yok. Test: Task 6 `gecersizAnahtar401TurkceVeSizintisiz`.
3. **Geri yüklenen kod bu tarayıcının mevcut kimliğiyle aynı** ya da boşluklu yapıştırılmış → aynı kod 200 (no-op),
   baştaki/sondaki boşluk kırpılır. Test: Task 3 `geriYukleBoslukKirpilirAyniKodNoOp`.
4. **Hafıza güncellemesi anahtarlı sitede gerçekten çalışır** (4. mesajda) ve anahtarsız istekte AI'a gitmez.
   Test: Task 6 `dorduncuMesajdaHafizaZiyaretciAnahtariylaGuncellenir`.
5. **Kapalı AI ucuna eski ön yüz/yer imiyle istek** → 404 JSON `{"hata": ...}`, 500 değil. Test: Task 5 `kapaliUclarSitede404`.

---

### Task 1: Ziyaretçi çerezi + girişsiz güvenlik zinciri

**Files:**
- Create: `src/main/java/tr/velzon/aiogretmen/config/ZiyaretciCerezi.java` (OncePerRequestFilter + sabitler)
- Create: `src/main/java/tr/velzon/aiogretmen/config/ZiyaretciKullaniciBaglami.java`
- Modify: `src/main/java/tr/velzon/aiogretmen/config/SiteGuvenlikYapilandirma.java` (zincir baştan)
- Delete: `src/main/java/tr/velzon/aiogretmen/config/OturumKullaniciBaglami.java`
- Modify: `src/main/java/tr/velzon/aiogretmen/config/KullaniciBaglami.java` (javadoc: site modu = ziyaretçi çerezi)
- Test: `src/test/java/tr/velzon/aiogretmen/web/SiteZiyaretciKimlikTest.java`

**Interfaces:**
- Produces:
  - `ZiyaretciCerezi.AD = "aio_ziyaretci"`, `ZiyaretciCerezi.ISTEK_OZNITELIGI = "aio.ziyaretci"` (istekteki kimlik, öneksiz)
  - `static boolean ZiyaretciCerezi.gecerliMi(String kimlik)` — desen `^[A-Za-z0-9_-]{22}$`
  - `static String ZiyaretciCerezi.yeniKimlik()`
  - `static void ZiyaretciCerezi.yaz(HttpServletRequest, HttpServletResponse, String kimlik, boolean guvenliCerez)` — Task 3 geri yüklemede kullanır
  - `ZiyaretciKullaniciBaglami.aktifKullanici()` → `"z-" + kimlik`; `static String ONEK = "z-"`

- [ ] **Step 1: Failing testleri yaz** (`@SpringBootTest` + `@ActiveProfiles("site")` + MockMvc, H2 mem; mevcut `SiteKisiselVeriTest` başlığı örnek)
  - `cerezsizIlkIstekGecerliCerezYazar`: `GET /api/program` → 200, `Set-Cookie` `aio_ziyaretci` değeri `gecerliMi`, `HttpOnly`, `SameSite=Lax`, `Max-Age=31536000`.
  - `gecersizCerezYenisiyleDegisir`: çerez `../../x` → yanıt yeni geçerli çerez yazar.
  - `girisYokTumSayfalarAcik`: `GET /` ve `GET /index.html` → 200 (yönlendirme yok); `JSESSIONID` çerezi yazılmaz.
  - `ayniCerezAyniKullanici`: `POST /api/gorev/durum` (CSRF ile) çerez A → `GET /api/program` aynı çerezle işaretli görevi görür; çerez B görmez.
  - `csrfsizYazmaReddedilir`: `POST /api/gorev/durum` CSRF token'sız → 403.
- [ ] **Step 2: Çalıştır, FAIL gör:** `./mvnw -Dtest=SiteZiyaretciKimlikTest test`
- [ ] **Step 3: Uygula**
  - `ZiyaretciCerezi`: çerezi okur; yok/geçersizse `yeniKimlik()` üretip yazar; kimliği `ISTEK_OZNITELIGI`'ne koyar. Secure kuralı mevcut CSRF çereziyle aynı (`guvenliCerez` true → her zaman; değilse `request.isSecure()`). Her yanıtta ömrü yenilemek için yeniden yazar.
  - `ZiyaretciKullaniciBaglami` (`@Component @Profile("site")`): `RequestContextHolder`'dan özniteliği okur; yoksa `IllegalStateException` (fail-closed, asla `YEREL`).
  - Zincir: `authorizeHttpRequests(anyRequest().permitAll())`, `formLogin`/`logout`/`sessionManagement(maximumSessions)`/`HesapEtkinlikFiltresi`/`SessionRegistry`/`HttpSessionEventPublisher`/`PasswordEncoder` KALKAR; `sessionCreationPolicy(STATELESS)`; CSRF (`CookieCsrfTokenRepository` + `CsrfCerezFiltresi`) aynen; `ZiyaretciCerezi` `CsrfFilter`'dan önce eklenir; `httpBasic` kapalı; `/api/**` için `HttpStatusEntryPoint(401)` artık gereksiz — kaldır. MCP ile birlikte açılmama kontrolü (constructor) kalır.
- [ ] **Step 4: PASS gör:** `./mvnw -Dtest=SiteZiyaretciKimlikTest test`
- [ ] **Step 5:** Uygulama bu task sonunda AÇILMALI: kimlik sınıfları (Task 2'de silinecek) hâlâ `PasswordEncoder` ve `SessionRegistry` bean'lerine bağlı → bu iki bean bu task'ta `SiteGuvenlikYapilandirma`'da **kalır** (zincir onları kullanmaz), Task 2 tüketicileriyle birlikte siler. `./mvnw verify` → giriş akışına bağlı eski testler (`SiteKimlikTest`, `SiteSifreSifirlamaTest`, `SiteEkipKoduTest`, `SiteHataSayfalariTest`'in giriş kısımları, `SiteCerezTest`) kırılır — **bu task'ta silinir** (`SiteHataSayfalariTest`'in giriş dışı senaryoları çerezle yeniden yazılır). Kalan suite yeşil.
- [ ] **Step 6: Commit** — `/commit`: `[faz-10] Site: ziyaretçi çerezi ile girişsiz kimlik (ADR-0017)`

### Task 2: Kimlik sistemini ve Pera izlerini kaldır

**Files:**
- Delete (main): `web/KimlikController.java`; `service/KimlikService.java`, `KullaniciDetayServisi.java`, `GirisDenemeTakibi.java`, `EpostaGonderici.java`, `EpostaGondericiSecimi.java`, `SmtpEpostaGonderici.java`, `LogEpostaGonderici.java`; `repo/KullaniciRepo.java`, `KullaniciTokenRepo.java`; `types/Kullanici*.java`, `types/KayitGecersizException.java` (yalnız kimlik için kullanılan diğer types da — `grep` ile doğrula); `resources/eposta/`; `static/giris.html`, `kayit.html`, `dogrulama-bekleniyor.html`, `dogrulama-sonuc.html`, `sifremi-unuttum.html`, `sifre-sifirla.html`, `kimlik.js`; `tools/sifre-baglantisi.sh`.
- Delete (test): `KimlikServiceOzetTest`, `SmtpEpostaGondericiTest`, `SiteEpostaSecimiTest`, `SiteEkipKoduAcilisTest`; `EpostaHizSiniriTest` Task 4'te `HizSiniriTest` olur.
- Modify: `config/SiteGuvenlikYapilandirma.java` — Task 1'de bırakılan `PasswordEncoder`, `SessionRegistry`, `HttpSessionEventPublisher` bean'leri ve `HesapEtkinlikFiltresi` sınıfı silinir.
- Modify: `config/SiteAyarlari.java` — yalnız `izinliOriginler`, `guvenliCerez` kalır. `application-site.yml` buna göre; başlıktaki "pera ekibi" yorumu nötr.
- Modify: `schema.sql` — `kullanici`, `kullanici_token` CREATE'leri ve ilgili yorumlar çıkar (DROP yok).
- Modify: `pom.xml` — `spring-boot-starter-mail` çıkar.
- Modify: `app.js` — `durum.site` üst bar e-posta/"Çıkış" bloğu (`~1569-1590`) ve 401→giriş yönlendirmesi (`~74`) çıkar.
- Modify (Pera metni → nötr): kalan testlerde `@pera.software` → `@ornek.com`, `egitim.pera.software` → `egitim.ornek.com` (`CsrfKorumaTest`, `SiteVekilBasliklariTest`, `SitePostgresEntegrasyonTest`, `SiteKisiselVeriTest`, `SiteKapaliModullerTest`, `AiOgretmenApplicationTest`); `SiteAyarlari`/`OzellikController`/`KullaniciBaglami` javadoc'ları.
- Test: `src/test/java/tr/velzon/aiogretmen/web/SiteKimlikKaldirildiTest.java`

- [ ] **Step 1: Failing test** `SiteKimlikKaldirildiTest`:
  - `kimlikUclariYok`: `POST /api/kimlik/kayit`, `POST /api/kimlik/giris`, `GET /api/kimlik/ayarlar` → 404.
  - `kimlikSayfalariYok`: `GET /giris.html`, `/kayit.html`, `/kimlik.js` → 404.
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest=SiteKimlikKaldirildiTest test`
- [ ] **Step 3: Sil/düzenle** (yukarıdaki liste). `SiteKisiselVeriTest`'teki `oturumAc(email)` yardımcıları ziyaretçi çereziyle (`Cookie("aio_ziyaretci", kimlik)`) yeniden yazılır; e-posta kayıt akışına bağlı satırlar gider; izolasyon iddiaları (A'nın hafızası/raporu/kullanımı B'ye görünmez, sıfırlama yalnız sahibini etkiler) aynen kalır.
- [ ] **Step 4: Doğrula:** `./mvnw verify` yeşil; `grep -rniI 'pera\.software\|AI Öğretmen · pera\|pera ekibi' src deploy tools` → boş (deploy Task 9'da; burada `src`, `tools` boş olmalı).
- [ ] **Step 5: Commit** — `[faz-10] Kimlik sistemi (kayıt/giriş/e-posta/ekip kodu) ve Pera izleri kaldırıldı`

### Task 3: Ziyaretçi kaydı + ilerleme kodu

**Files:**
- Modify: `schema.sql` — `CREATE TABLE IF NOT EXISTS ziyaretci (id VARCHAR(64) PRIMARY KEY, olusturma TIMESTAMP NOT NULL, son_gorulme TIMESTAMP NOT NULL)`; `CREATE INDEX IF NOT EXISTS ix_ziyaretci_son_gorulme ON ziyaretci (son_gorulme)` — yorum: "Task 7 temizlik sorgusu `WHERE son_gorulme < ?` için".
- Create: `repo/ZiyaretciRepo.java` (JdbcTemplate; mevcut repo stiline uy)
- Create: `service/ZiyaretciService.java` (`@Profile("site")`)
- Create: `web/ZiyaretciDokunmaInterceptor.java` + kayıt (`WebMvcConfigurer`, site profili)
- Create: `web/ZiyaretciController.java` (`@Profile("site")`, `/api/ziyaretci`)
- Test: `service/ZiyaretciServiceTest.java`, `web/SiteIlerlemeKoduTest.java`

**Interfaces:**
- Consumes: `ZiyaretciCerezi.ISTEK_OZNITELIGI`, `gecerliMi`, `yaz(...)`, `ZiyaretciKullaniciBaglami.ONEK` (Task 1)
- Produces:
  - `ZiyaretciRepo.dokun(String kullaniciId, Instant simdi)` — yoksa ekle; varsa `son_gorulme` yalnız 1 günden eskiyse güncelle. Çakışmaya dayanıklı (INSERT çakışırsa `DuplicateKeyException` yut + UPDATE).
  - `ZiyaretciRepo.varMi(String kullaniciId): boolean`
  - `ZiyaretciRepo.eskiler(Instant esik): List<String>`, `ZiyaretciRepo.sil(String kullaniciId)` — Task 7
  - `ZiyaretciService.dokun(String kullaniciId)`, `ZiyaretciService.varMi(String kullaniciId)`
  - `GET /api/ziyaretci/kod` → `{"kod": "<22 karakter>"}`; `POST /api/ziyaretci/geri-yukle {kod}` → 200 `{"durum":"geri-yuklendi"}` + yeni çerez; geçersiz biçim → 400; kayıtsız → 422 `{"hata":"Bu kodla kayıtlı ilerleme bulunamadı."}`

- [ ] **Step 1: Failing testler**
  - `ZiyaretciServiceTest.ilkDokunmaSatirOlusturur`, `ayniGunIkinciDokunmaYazmaz` (sahte `Clock`), `birGunSonraSonGorulmeGuncellenir`, `esZamanliDokunmaHataVermez` (aynı id, 8 iş parçacığı, hepsi başarılı, tek satır).
  - `SiteIlerlemeKoduTest.salaBakanSatirBirakmaz` (`GET /api/program` → `ziyaretci` boş), `yazmaSatirOlusturur` (`POST /api/gorev/durum` → 1 satır), `kodMevcutKimligiDoner`, `geriYuklemeCereziDegistirir` (A işaretler; B tarayıcısı A'nın koduyla geri yükler → B çereziyle A'nın ilerlemesi görünür), `olmayanKod422`, `bozukKod400`, `geriYukleBoslukKirpilirAyniKodNoOp`.
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest='ZiyaretciServiceTest,SiteIlerlemeKoduTest' test`
- [ ] **Step 3: Uygula.** Interceptor yalnız `POST/PUT/DELETE /api/**` için `dokun` çağırır (`preHandle`). Geri yükleme önce `kod.strip()`, sonra `gecerliMi`, sonra `varMi("z-"+kod)`.
- [ ] **Step 4: PASS gör** (aynı komut) + `./mvnw -Dtest=SemaTasinabilirlikTest test`
- [ ] **Step 5: Commit** — `[faz-10] Ziyaretçi kaydı ve ilerleme kodu (kaydet/geri yükle)`

### Task 4: Hız sınırları

**Files:**
- Rename/genelleştir: `service/EpostaHizSiniri.java` → `service/HizSiniri.java`; test `EpostaHizSiniriTest` → `HizSiniriTest`
- Create: `web/HizSiniriInterceptor.java` (site profili; Task 3'teki `WebMvcConfigurer`'a eklenir, dokunma interceptor'ından ÖNCE)
- Create: `types/HizSiniriAsildiException.java` → `HataYonetimi`'nde 429 `{"hata":"Çok sık istek gönderdin, biraz bekleyip tekrar dene."}`
- Test: `web/SiteHizSiniriTest.java`

**Interfaces:**
- Produces: `boolean HizSiniri.izinVer(String anahtar, int enCok, Duration pencere)` (kayan pencere, reddedilen deneme yazılmaz — mevcut davranış). Eski bellek sızıntısı riskine karşı: boşalan kuyruklar `compute` içinde `null` döndürülerek haritadan silinir.
- Sınırlar (sabitler `HizSiniriInterceptor`'da):
  - tüm yazma uçları (`POST/PUT/DELETE /api/**`): IP başına 60 / 1 dk — anahtar `"yaz|" + ip`
  - `POST /api/sohbet`: ziyaretçi başına 20 / 1 dk — `"sohbet|" + kullaniciId`
  - `POST /api/ziyaretci/geri-yukle`: IP başına 10 / 1 saat — `"geri|" + ip`
  - yeni ziyaretçi satırı (yazma isteğinde `ziyaretci` kaydı henüz yoksa): IP başına 20 / 1 saat — `"yeni|" + ip`

- [ ] **Step 1: Failing testler** — `HizSiniriTest` (mevcut senaryolar yeni imzayla + `bosKuyrukHaritadanSilinir`); `SiteHizSiniriTest.yazmaSiniri61inci429`, `sohbetSiniri21inci429` (sahte AI), `geriYukle11inci429`, `yeniZiyaretci21inci429` (her istek farklı çerez, aynı `remoteAddr`), `farkliIpEtkilenmez`.
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest='HizSiniriTest,SiteHizSiniriTest' test`
- [ ] **Step 3: Uygula.**
- [ ] **Step 4: PASS gör.**
- [ ] **Step 5: Commit** — `[faz-10] Girişsiz site için IP/ziyaretçi hız sınırları`

### Task 5: Sitede sohbet dışı AI uçlarını kapat + özellik bayrakları

**Files:**
- Modify: `web/OgretmenController.java`, `web/AtolyeController.java`, `web/KitapController.java` — sitede kapalı uçlar: `/api/degerlendir`, `/api/ornek`, `/api/aciklama`, `/api/quiz/uret`, `/api/rapor/**`, `/api/atolye/degerlendir`, `/api/kitap-notu/**`, `/api/kitaplar`. Mekanizma: bu uçları ayrı bir `@Profile("!site")` controller'a taşı (`OgretmenAiOzellikController`) — kayıtlı olmadıkları için 404, `HataYonetimi.kaynakYok` JSON döner. `/api/sohbet`, `/api/sohbet/{hafta}`, `/api/kullanim` `OgretmenController`'da kalır.
- Modify: `web/OzellikController.java` — `kullanici` alanı kalkar; yeni `"sunucuAi": !site && aiClient.aktif()` (yerelde rapor/değerlendirme/AI quiz düğmeleri bunu kullanır); `"site"` kalır.
- Test: `web/SiteKapaliAiUclariTest.java`; mevcut `OgretmenControllerTest` yerel modda yeşil kalmalı.

**Interfaces:**
- Produces: `/api/ozellikler` → `{"mulakat": bool, "site": bool, "sunucuAi": bool}`

- [ ] **Step 1: Failing test** `kapaliUclarSitede404` (her uç için 404 + gövdede `hata`), `sohbetGecmisiSitedeAcik` (`GET /api/sohbet/1` → 200), `ozelliklerSitede` (`site=true`, `sunucuAi=false`, `kullanici` yok), `ozelliklerYerelde` (`site=false`).
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest=SiteKapaliAiUclariTest test`
- [ ] **Step 3: Uygula.**
- [ ] **Step 4: PASS** + `./mvnw -Dtest=OgretmenControllerTest test`
- [ ] **Step 5: Commit** — `[faz-10] Sitede sohbet dışı AI uçları kapalı; /api/ozellikler sunucuAi bayrağı`

### Task 6: Ziyaretçinin kendi anahtarıyla AI istemcisi

**Files:**
- Create: `config/AiAnahtarBaglami.java` (arayüz) + `config/BaslikAiAnahtarBaglami.java` (`@Profile("site")`) + `config/YokAiAnahtarBaglami.java` (`@Profile("!site")`, hep boş)
- Create: `types/AnahtarGerekliException.java` → `HataYonetimi` 400 `{"hata":"AI öğretmenle sohbet için kendi OpenRouter anahtarını eklemelisin."}`
- Create: `ai/ChatModelKaynagi.java` (arayüz, `ai` içi)
- Modify: `ai/SpringAiOgretmenClient.java` — `ChatModel` yerine `ChatModelKaynagi` alır
- Modify: `ai/AiYapilandirma.java` — `ogretmenAiClient` bean'i: site profilinde ziyaretçi kaynağı; değilse mevcut davranış (anahtar boşsa devre dışı taslak, doluysa sabit kaynak). `zamanAsimliFabrika`, `yenidenDenemeSablonu`, `sohbetModeli` yeniden kullanılır.
- Modify: `ai/AiHataCevirici.java` — ziyaretçi modunda mesajlar: 401/403 → "OpenRouter anahtarın geçersiz ya da iptal edilmiş.", 402 → "OpenRouter kredin bitti ya da anahtarının harcama limitine ulaştın.", 429 → "OpenRouter çok fazla istek dedi, biraz bekleyip tekrar dene." (mod bilgisi `cevir(...)`'e parametre olarak gelir; yerel mesajlar değişmez)
- Modify: `application-site.yml` — `spring.ai.openai.api-key` sitede **boş zorlanır** (`""`); sunucu anahtarı ortamda olsa bile kullanılmaz.
- Test: `ai/ZiyaretciChatModelKaynagiTest.java`, `config/BaslikAiAnahtarBaglamiTest.java`, `web/SiteSohbetAnahtarTest.java`

**Interfaces:**
- Produces:
  - `interface AiAnahtarBaglami { Optional<String> anahtar(); }` — site: `RequestContextHolder`'daki istekte `X-OpenRouter-Anahtar` başlığı; `strip()`; boş → `Optional.empty()`; desene uymuyorsa `IllegalArgumentException("OpenRouter anahtarının biçimi geçersiz — 'sk-or-' ile başlamalı.")` (mesaj anahtarı İÇERMEZ).
  - `interface ChatModelKaynagi { boolean hazir(); ChatModel model(); }` — sabit: `hazir()=true`; ziyaretçi: `hazir()=anahtarBaglami.anahtar().isPresent()`, `model()` her çağrıda verilen anahtarla `OpenAiApi` + `OpenAiChatModel` kurar (önbellek YOK), anahtar yoksa `AnahtarGerekliException`.
  - `SpringAiOgretmenClient.aktif()` → `kaynak.hazir()`.

- [ ] **Step 1: Failing testler**
  - `BaslikAiAnahtarBaglamiTest`: `basliksizBos`, `bosluklarKirpilir`, `bozukBicim400VeMesajdaAnahtarYok`.
  - `ZiyaretciChatModelKaynagiTest`: `anahtarYokkenHazirDegil`, `anahtarYokkenModelAnahtarGerekli`.
  - `SiteSohbetAnahtarTest` (OpenRouter yerine yerel sahte HTTP sunucusu: `com.sun.net.httpserver.HttpServer`, `spring.ai.openai.base-url` ona yönlendirilir — yeni bağımlılık yok):
    - `basliksizSohbet400VeAiCagrilmaz` (sahte sunucuya istek düşmez; `sohbet_mesaj` boş)
    - `baslikliSohbetZiyaretciAnahtariniKullanir` (sahte sunucu `Authorization: Bearer sk-or-test...` görür)
    - `gecersizAnahtar401TurkceVeSizintisiz` (sahte sunucu 401 → yanıt 4xx, gövde "geçersiz" içerir; `OutputCaptureExtension` logunda, yanıt gövdesinde ve `ai_kullanim`'da anahtar metni yok; `sohbet_mesaj` boş)
    - `dorduncuMesajdaHafizaZiyaretciAnahtariylaGuncellenir` (4 mesaj → sahte sunucu 5. çağrıyı `hizli` modelle alır; ziyaretçinin `OGRENCI.md`'si oluşur)
    - `ikiZiyaretciAnahtarlariKarismaz` (paralel iki istek, her biri kendi Bearer'ı)
    - `sunucuAnahtariSitedeKullanilmaz` (`OPENROUTER_API_KEY` dolu verilse de başlıksız istek 400)
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest='BaslikAiAnahtarBaglamiTest,ZiyaretciChatModelKaynagiTest,SiteSohbetAnahtarTest' test`
- [ ] **Step 3: Uygula.** `HafizaService.guncelle` içindeki `catch` logu `e.getMessage()` kullanıyor — `AiSaglayiciException` mesajları ham gövdesiz Türkçe olduğundan anahtar içermez; test bunu kanıtlar, kod değişikliği gerekirse yalnız log satırı.
- [ ] **Step 4: PASS** + `./mvnw -Dtest='SpringAiOgretmenClientTest,AiHataCeviriciTest,AiYenidenDenemeTest,MimariTestleri' test`
- [ ] **Step 5: Commit** — `[faz-10] Sohbet ziyaretçinin kendi OpenRouter anahtarıyla (X-OpenRouter-Anahtar)`

### Task 7: Terk edilmiş ziyaretçi verisinin temizliği

**Files:**
- Create: `service/ZiyaretciTemizlikService.java` (`@Profile("site")`)
- Modify: `AiOgretmenApplication` (ya da site config) — `@EnableScheduling` yalnız site profilinde (ayrı `@Configuration @Profile("site") @EnableScheduling`)
- Test: `service/ZiyaretciTemizlikServiceTest.java`

**Interfaces:**
- Consumes: `ZiyaretciRepo.eskiler(Instant)`, `ZiyaretciRepo.sil(String)` (Task 3); `SifirlamaService.sifirla(String kullaniciId)` (mevcut — tablolar + hafıza/rapor dizinleri)
- Produces: `int ZiyaretciTemizlikService.temizle()` — silinen ziyaretçi sayısı; `@Scheduled(cron = "0 30 3 * * *")` sarmalayıcı `zamanlanmisTemizlik()`. `ai_kullanim` satırları da silinir (`SifirlamaService` silmiyorsa burada). ShedLock YOK — gerekçe ADR-0017'de (tek JVM, idempotent).

- [ ] **Step 1: Failing test** `eskiZiyaretciTamamenSilinir` (son_gorulme 181 gün önce; görev/sohbet/quiz/ai_kullanim satırları + `KisiselDizin` hafıza dosyası → hepsi gider), `yeniZiyaretciKalir` (179 gün), `ikiKezCalismakZararsiz`.
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest=ZiyaretciTemizlikServiceTest test`
- [ ] **Step 3: Uygula** (sahte `Clock` enjekte edilebilir).
- [ ] **Step 4: PASS gör.**
- [ ] **Step 5: Commit** — `[faz-10] 180 gün gelmeyen ziyaretçinin verisi silinir`

### Task 8: Ön yüz — anahtar kartı, kendin işaretle, ilerleme kodu

**Files:**
- Modify: `src/main/resources/static/app.js`, `index.html`, `style.css`
- Test: `src/test/java/tr/velzon/aiogretmen/web/SiteOnYuzIcerikTest.java` (statik dosya içerik iddiaları) + elle doğrulama

**Interfaces:**
- Consumes: `/api/ozellikler` (`site`, `sunucuAi`), `/api/ziyaretci/kod`, `/api/ziyaretci/geri-yukle`, `X-OpenRouter-Anahtar`, 400 "anahtar" / 401 "geçersiz" / 429 yanıtları.

Davranış (spec §6.3, §6.4, §6.2):
- `durum.aiAktif` ikiye ayrılır: `durum.sunucuAi` (`/api/ozellikler`) → rapor (`~156`), AI quiz (`~193`), yaz değerlendirmesi, örnek/açıklama düğmeleri yalnız bunda görünür; `durum.sohbetAcik` = yerelde `sunucuAi`, sitede "tarayıcıda anahtar var". Görev kapısı (`~358`, `~494`, `~693`, `~705`, `~748`) ve sohbet paneli (`~1144`, `~1278`, `~1338`, `~1366`, `~1369`) `sohbetAcik`'a bağlanır. Sayfa açılışı `/api/kullanim`'a bağlı olmaz (`~1596`).
- Anahtar saklama: anahtar `localStorage["aio.openrouter"]` ("Bu cihazda hatırla" işaretliyse) yoksa `sessionStorage["aio.openrouter"]`; erişim `try/catch`. Yalnız `api("/api/sohbet", ...)` çağrısına başlık eklenir.
- Sohbet paneli anahtarsızken kart: başlık "AI öğretmenle sohbet (isteğe bağlı)", anahtar alanı (`type=password`, `autocomplete=off`), "Bu cihazda hatırla", "Kaydet", açılır "Anahtarı nasıl alırım?": 1) openrouter.ai'de hesap aç 2) Keys → Create Key 3) **Credit limit koy** (ör. 5 $) 4) `sk-or-…` ile başlayan anahtarı buraya yapıştır. "Ücret OpenRouter hesabına yazılır; anahtarın yalnız bu tarayıcıda durur, sunucumuz kaydetmez."
- 400 anahtar / 401 geçersiz → kart yeniden açılır, hata metni gösterilir; 429 → mesaj.
- Anahtarsız görev düğmeleri: oku "Okudum ✓", yaz "Yaptım ✓" (sitede anahtarlı da), tartış "Tartıştım ✓" → `POST /api/gorev/durum`.
- Üst bar (yalnız site): "İlerleme kodum" → kod + Kopyala + uyarı ("Bu kod şifre gibidir…"); "Geri yükle" → onay ("Bu tarayıcıdaki ilerleme bu kodla değişecek") → `geri-yukle` → sayfa yenilenir.
- `index.html` "Nasıl çalışırım" §2–4: sitede "kutucukları kendin de işaretleyebilirsin", "AI öğretmen isteğe bağlı, kendi anahtarınla", rapor paragrafı gizli (sitede `data-yalniz-yerel` ile işaretlenip JS gizler).

- [ ] **Step 1: Failing test** `SiteOnYuzIcerikTest`: `app.js` içinde `"X-OpenRouter-Anahtar"` yalnız 1 kez geçer (tek çağrı noktası); `index.html`'de `pera` geçmez; `app.js`'te `/api/kullanim` açılış bağımlılığı yok (`aiAktif` kelimesi yok).
- [ ] **Step 2: FAIL gör:** `./mvnw -Dtest=SiteOnYuzIcerikTest test`
- [ ] **Step 3: Uygula.** AwesomeDesign standartları: `/Users/felece/Documents/Obsidian Vault/CLAUDE CODE/AwesomeDesign.md` (mevcut `style.css` dilini koru).
- [ ] **Step 4: PASS** + elle doğrulama (`run` skill'i ya da puppeteer), site profili H2 ile:
  `SPRING_DATASOURCE_URL='jdbc:h2:file:./data/site-deneme' APP_SITE_GUVENLI_CEREZ=false ./mvnw spring-boot:run -Dspring-boot.run.profiles=site`
  Kontrol: anahtarsız → bir günün tüm görevlerini işaretle, sonraki gün açılır; sohbet kartı görünür; geçersiz anahtar → "geçersiz" mesajı; ilerleme kodu → ikinci tarayıcı profilinde geri yükle, ilerleme görünür; rapor/AI quiz düğmeleri yok. Yerel mod (`./mvnw spring-boot:run`, anahtarlı) — rapor/değerlendirme eskisi gibi.
- [ ] **Step 5: Commit** — `[faz-10] Ön yüz: isteğe bağlı anahtar kartı, kendin işaretle, ilerleme kodu`

### Task 9: Belgeler, dağıtım şablonları, son doğrulama

**Files:**
- Create: `docs/adr/0017-girissiz-site-ve-kendi-anahtarin.md` — ADR-0012'yi değiştirir, 0013 + 0015'i geçersiz kılar; kararlar spec §5; ShedLock sapması gerekçesi; anahtarın sunucuda kalıcı tutulmaması; reddedilen yollar (spec §4).
- Modify: `docs/adr/0012…`, `0013…`, `0015…` — başa "Değişiklik notu (2026-11-25, ADR-0017)" satırı (içerik silinmez).
- Modify: `CLAUDE.md` (Komutlar: ekip kodu, SMTP, `sifre-baglantisi.sh`, `APP_SITE_KAYIT` satırları kalkar; site çalıştırma = `--spring.profiles.active=site` + `SPRING_DATASOURCE_URL`; OpenRouter anahtarı sitede gerekmez), `ARCHITECTURE.md`, `docs/kurulum-site.md`, `deploy/site.env.ornek` (OpenRouter ve SMTP blokları çıkar), `deploy/nginx-site.conf.ornek` (`egitim.ornek.com`), `docs/tech-debt-tracker.md` (ilgili kalem varsa kapat).
- Modify: `SitePostgresEntegrasyonTest` — kimlik/şifre betiği senaryoları çıkar; `ziyaretci` tablosu + izolasyon senaryosu PostgreSQL'de.

- [ ] **Step 1:** Belgeleri yaz.
- [ ] **Step 2: Doğrula:** `./mvnw verify` yeşil; `grep -rniI 'pera\.software\|ekip-kodu\|EKIP_KODU\|SPRING_MAIL' src deploy tools CLAUDE.md docs/kurulum-site.md` → boş; PostgreSQL varsa `PG_TEST_URL=... ./mvnw -Dtest=SitePostgresEntegrasyonTest test` (yoksa "çalıştırılmadı" diye raporla).
- [ ] **Step 3:** `qa-code-reviewer` ajanıyla tüm dalı incelet; bulguları aynı turda düzelt.
- [ ] **Step 4: Commit** — `[faz-10] ADR-0017 + belgeler ve dağıtım şablonları girişsiz siteye göre`
- [ ] **Step 5:** Obsidian Vault notu (`docs/obsidian-vault-rehberi.md`'yi önce oku; sır yazma).
