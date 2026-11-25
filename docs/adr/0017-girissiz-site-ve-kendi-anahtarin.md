# ADR-0017 — Girişsiz site, ziyaretçi çerezi ve "kendi OpenRouter anahtarın" ile sohbet (Faz 10)

- **Durum:** Kabul edildi
- **Tarih:** 2026-11-25
- **Bağlam sahibi:** kullanıcı kararı (§Karar 1–5, aynen) + uygulayıcı kararları (aşağıda *uygulayıcı kararı*
  diye işaretli; kullanıcı ayrıntıyı uygulayıcıya bıraktı)
- **Değiştirdiği kararlar:** ADR-0012 (site profili — kimlik bölümleri geçersiz, profil ve izolasyon kalır),
  ADR-0013 (SMTP — tamamen geçersiz, bağımlılık kaldırıldı), ADR-0015 (ekip kodu — tamamen geçersiz).
  ADR-0014 (PostgreSQL) ve ADR-0016 geçerli kalır.
- **Tasarım:** `docs/superpowers/specs/2026-11-25-girissiz-site-kendi-anahtarin-design.md`

## Bağlam

Site (ADR-0012) yalnız Pera ekibine açıktı: `@pera.software` e-postası + ekip kodu ile kayıt, şifreli giriş ve
sunucunun (şirketin) tek OpenRouter anahtarı. Kullanıcı bunu istemedi: site **herkese açık** bir eğitim sitesi
olacak, Pera'ya özel hiçbir şey kalmayacak, giriş/kayıt hiç olmayacak; AI yalnız **isteğe bağlı sohbet botu**
olacak ve onu kullanan **kendi OpenRouter anahtarını** girecek (maliyet sitenin değil, kullananın).

Başarısızlık ölçütleri: ziyaretçinin anahtarının sunucuda kalıcı bir yere düşmesi (veritabanı, log, hata
mesajı, `ai_kullanim`, hafıza dosyası, istisna zinciri); bir ziyaretçinin başkasının verisini görmesi; sunucunun
sitede **sunucu anahtarıyla** AI çağırması; anahtarsız ziyaretçinin bir haftayı bitirememesi; yerel modun ya da
`mcp`/`mcp-http` profillerinin bozulması.

## Karar

### Kullanıcının kararları (aynen)
1. Giriş/kayıt yok. Pera'ya özel her şey kalkar.
2. İlerleme: çerez kimliği + sunucuda veri; "ilerleme kodunu kaydet/geri yükle" olur.
3. AI anahtarı **yalnız sohbet botu için** istenir; rapor, AI değerlendirme vb. sitede yok.
4. Anahtar: sohbet paneline yapıştırılır (yaklaşım A).
5. Anahtarsız kişi görevleri kendisi işaretler; anahtarlı kişi AI öğretmenle geçer.

### 1. Ziyaretçi kimliği = rastgele çerez, veri sunucuda
- Çerez `aio_ziyaretci`: 22 karakterlik URL-güvenli rastgele kimlik (128 bit, `SecureRandom`), `HttpOnly`,
  `SameSite=Lax`, `Secure` (`APP_SITE_GUVENLI_CEREZ`), `Path=/`, ömür 365 gün, ziyarette yenilenir.
  Çerez yoksa ya da **biçimi geçersizse** yenisi üretilir — çerezle gelen keyfi metin `kullanici_id`'ye ya da
  dosya yoluna girmez.
- `kullanici_id` = `"z-" + kimlik` (eski `yerel` ve e-posta kimliklerinden ayırt edilir). Sitede
  `ZiyaretciKullaniciBaglami` **fail-closed**: kimlik yoksa hata, asla `yerel`'e düşmez. Hiçbir uç dışarıdan
  `kullanici_id` almaz; ADR-0008'den beri var olan `kullanici_id` izolasyonu aynen kullanılır.
- `ziyaretci(id, olusturma, son_gorulme)` tablosu; satır **yalnız ilk yazmada** açılır (salt bakan ziyaretçi
  veritabanında iz bırakmaz), `son_gorulme` en çok günde bir güncellenir — satırı olan ziyaretçinin salt okuyan
  `GET /api/**` istekleri de tazeler (satır açmadan), yani "180 gün gelmeyen" gerçekten gelmeyendir.
- **İlerleme kodu:** `GET /api/ziyaretci/kod` kimliği gösterir; `POST /api/ziyaretci/geri-yukle {kod}` biçimi
  geçerli **ve** `ziyaretci` tablosunda kayıtlı kodu çereze yazar (biçimsiz → 400, kayıtsız → 422). Bu
  tarayıcının önceki ilerlemesi birleştirilmez, üzerine geçilir (ön yüz onaylatır). Kod şifre gibidir.
- Reddedilen: her şeyi yalnız tarayıcıda (localStorage) tutmak — ilerleme/hafıza katmanını baştan yazmayı ve AI
  hafızasını her istekte tarayıcıdan taşımayı gerektirirdi.

### 2. Kimlik sistemi kaldırıldı
Kayıt, giriş, şifre, e-posta doğrulama, şifre sıfırlama, ekip kodu, SMTP; `kimlik.js` ve kimlik sayfaları,
`KimlikController`/`KimlikService`, e-posta göndericileri, `tools/sifre-baglantisi.sh`, form login ve
oturum (`JSESSIONID` yok, stateless). `spring-boot-starter-mail` bağımlılığı kaldırıldı (ADR-0013 geçersiz).
`SiteAyarlari`'nda yalnız `guvenli-cerez` ve `izinli-originler` kaldı. CSRF korunur (çerez tabanlı kimlik
olduğu için): `XSRF-TOKEN` çerez deposu + `CsrfKoruma` (Origin/Sec-Fetch-Site).
`kullanici` ve `kullanici_token` tabloları `schema.sql`'den çıkarıldı (yeni kurulumda oluşmaz); **mevcut
veritabanında silinmez** (DROP yok — veri silme ayrı ve bilinçli bir adım). Eski e-posta kimlikli satırlar
sahipsiz kalır ve 180 gün temizliğine girmez.

### 3. Sohbet ziyaretçinin kendi anahtarıyla (yaklaşım A)
- Ön yüz: sohbet panelinde isteğe bağlı anahtar kartı ("Bu cihazda hatırla" → `localStorage`, değilse
  `sessionStorage`) + Türkçe "anahtarı nasıl alırım" yardımı (harcama limiti koy). Anahtar **yalnız**
  `POST /api/sohbet` isteğine `X-OpenRouter-Anahtar` başlığıyla eklenir (app.js'te tek çağrı noktası; test
  bekçisi).
- Sunucu: başlık biçimi `sk-or-` + 10–200 URL-güvenli karakter. Başlık yoksa 400
  `{"hata": …, "neden": "ANAHTAR_GEREKLI"}`, biçimsizse 400 `{"hata": "Geçersiz istek", "detay": …,
  "neden": "ANAHTAR_BICIMI"}` — AI çağrılmaz; ön yüz metne değil bu kararlı koda göre dallanır.
  **Sunucu anahtarına düşülmez:** `application-site.yml` `spring.ai.openai.api-key`'i zorla boşaltır ve site
  AI yolu o özelliği hiç okumaz (`OPENROUTER_API_KEY` ortamda olsa bile kullanılmaz).
- `ai/` paketinde istek başına, ziyaretçinin anahtarıyla kurulan `ChatModel` (`ZiyaretciChatModelKaynagi`);
  anahtar başına önbellek **yok**. `OgretmenAiClient.aktif()`'in sitedeki anlamı: "bu istek geçerli bir
  ziyaretçi anahtarı taşıyor". Her 4 mesajda bir yapılan öğrenci hafızası güncellemesi aynı isteğin
  anahtarıyla, senkron çalışır. Modeller sunucu ayarındaki gibi (sohbet `guclu`, hafıza `hizli`).
- OpenRouter hataları ziyaretçiye göre: 401/403 → HTTP 401 "anahtarın geçersiz" (ön yüz anahtarı siler ve
  kartı yeniden açar), 402 → 402 "kredin bitti", 429 → 429 "biraz bekle".
- Reddedilenler: anahtarı sunucuda çerez kimliğiyle saklamak (veritabanı sızıntısı = herkesin anahtarı
  sızar); tarayıcının OpenRouter'ı doğrudan çağırması (bağlam/prompt'lar sunucuda, "AI yalnız `ai/`" kuralı
  bozulur). "OpenRouter ile bağlan" (OAuth PKCE, yaklaşım B) A'nın üstüne sonradan eklenebilir — kapsam dışı.

### 4. Ziyaretçi anahtarı sunucuda kalıcı olarak TUTULMAZ
Anahtar yalnız o isteğin ömrü boyunca bellekte yaşar: veritabanına (`ai_kullanim` dahil), loga, hafıza
dosyasına, hata gövdesine yazılmaz; hiçbir `toString()`'de görünmez. Ziyaretçi anahtarıyla oluşan sağlayıcı
hatasında `AiSaglayiciException` Spring AI'ın asıl istisnasını **cause olarak taşımaz** (o mesaj ham sağlayıcı
gövdesini içerir ve gövde `Authorization` başlığını yansıtabilir); teşhis için yalnız HTTP kodu ve kategori
kalır. Yerel modda (sunucu anahtarı) asıl istisna cause olarak korunur. Testler: `SiteSohbetAnahtarTest`
(log, `ai_kullanim`, hafıza dosyası, yanıt gövdesi, istisna zinciri), `AiHataCeviriciTest`.

### 5. Görevler: anahtarsız kendin işaretlersin, anahtarlı AI öğretmenle geçersin
`/api/ozellikler` = `{mulakat, site, sunucuAi}`. Ön yüzde iki bayrak: `sunucuAi` (sitede hep `false`) ve
`sohbetAcik` (yerelde = `sunucuAi`; sitede = tarayıcıda anahtar var mı). Anahtarsız: oku → "Okudum ✓",
yaz → "Yaptım ✓", tartış → "Tartıştım ✓" doğrudan `POST /api/gorev/durum`. Anahtarlı: oku ve tartış mevcut
sohbet kapısıyla. Yaz görevi sitede herkes için "Yaptım ✓" (AI değerlendirmesi sitede yok) ve kutucuğu geri
alınabilir. Kilit/sıra mantığı aynen.
Reddedilenler: herkesin kendisi işaretlemesi (AI'la geçme deneyimi kaybolur); kilidin tamamen kalkması.

### 6. Sitede kapanan AI uçları
Sohbet dışındaki tüm AI uçları (`/api/degerlendir`, `/api/ornek`, `/api/aciklama`, `/api/quiz/uret`,
`/api/rapor/**`, AI atölye uçları, `/api/kitap-notu/**`, `/api/kitaplar`, mülakat) sitede **kayıtlı değil**
(`@Profile("!site")` → 404); ön yüz bu bölümleri sitede göstermez. Statik quiz, içerik, kaynaklar, ilerleme ve
sohbet geçmişi çalışır. Yerel mod hepsini sunucu anahtarıyla eskisi gibi çalıştırır.

### 7. Kötüye kullanım sınırları *(uygulayıcı kararı)*
Girişsiz sitede bir betik her istekte yeni çerezle sınırsız satır üretmesin, sunucu "açık vekil" olmasın:
yazan istekler (`POST/PUT/DELETE /api/**`) IP başına 60/dk; sohbet ziyaretçi başına 20/dk; `geri-yukle` IP
başına 10/saat; yeni ziyaretçi satırı IP başına 20/saat. Aşımda 429 `{"hata": …}` + `Retry-After` başlığı (en
uzun kalan pencere süresi, saniye, en az 1). Dört sayı `APP_SITE_HIZ_*` ortam değişkenleriyle ayarlanabilir (tek
NAT arkasındaki sınıf/ofis için; `docs/kurulum-site.md` §7.6), pencereler sabit. Mesaj uzunluk sınırları,
sistem prompt'u ve model sabit.

### 8. 180 gün temizliği — ShedLock YOK (bilinçli sapma) *(uygulayıcı kararı)*
Her gece 03:30 (`@Scheduled`) `son_gorulme`'si 180 günden eski ziyaretçilerin tüm satırları (görev, gönderim,
quiz, sohbet, mülakat, `ai_kullanim`, `ziyaretci`) ve kişisel `data/hafiza`/`data/raporlar` dizinleri silinir;
ziyaretçi başına **tek okuma-yazma işlemi** (PostgreSQL salt-okunur işlemde DELETE'i reddeder). İşlemin ilk
adımı `ziyaretci` satırını **yaş yeniden denetlenerek** siler (`… AND son_gorulme < eşik`): listeleme ile silme
arasında geri dönen ziyaretçide 0 satır → işlem geri alınır, hiçbir verisi/dosyası silinmez. Sonraki adımlardan
biri hata verirse de hepsi birden geri alınır, sonraki gece yeniden denenir.
Ekip standardı her `@Scheduled` işe `@SchedulerLock` ister (çok pod). **Burada uygulanmadı**, gerekçe:
site tek JVM'dir (systemd, tek örnek — `deploy/ai-ogretmen-site.service`; Docker/k8s yok) ve iş **idempotenttir**
(iki kez çalışsa ikinci tur silinecek satır bulamaz, zarar yok). ShedLock yeni bağımlılık + kilit tablosu
demekti; kazancı yok. Site birden çok örneğe çıkarsa bu karar yeniden açılır.

## Gözden geçirilmiş uç durumlar (bilinçli kabuller)

- **Yerel mod, sunucu anahtarı yok:** eskiden "AI kapalı" uyarısı (alert) çıkan yaz/tartış görevlerinde artık
  "kendin işaretle" düğmeleri görünür. Kabul edilmiş iyileştirme (yerel mod davranışı bu noktada değişti).
- **İlerleme kodu ilk yazmadan önce çalışmaz:** satır yalnız ilk yazmada açıldığından, henüz hiçbir şey
  işaretlememiş birinin kodu başka tarayıcıda 422 ("kayıtlı ilerleme bulunamadı") verir. Kabul edildi: kaydedecek
  ilerleme yok.
- **Silinemeyen kişisel dizin, satırları da bekletir:** temizlikte ziyaretçinin hafıza/rapor dizini silinemezse
  (izin, disk) işlem geri alınır — veritabanı satırları ve `ziyaretci` satırı kalır, hata loglanır, sonraki gece
  yeniden denenir; engel kalkınca hepsi birlikte silinir (yetim dizin kalmaz; tech-debt #16). Temizlik en eskiden
  başlayarak 500'lük partilerle ilerler; sayfalama imleçlidir (keyset: önceki partinin son `(son_gorulme, id)`'sinden
  sonrası) — o turda başarısız olan ya da arada geri dönen kimlik yeniden denenmez ve sayfayı kaydırmaz; tam bir
  parti kalıcı başarısız olsa bile arkasındaki eski ziyaretçiler aynı turda silinir, imleç hep ilerlediği için
  sonsuz döngü olmaz (tech-debt #17, son inceleme 2026-11-25). Geri dönen ziyaretçi hata sayılmaz ("atlanan").
- **Hız sınırları JVM başına bellekte:** yeniden başlatmada sıfırlanır, birden çok örnek arasında paylaşılmaz.
  Tek örnekli site için yeterli; çok örneğe çıkılırsa ortak depo gerekir.
- **`SitePostgresEntegrasyonTest` 2026-11-25'te yerel PostgreSQL 14.15'e karşı 5/5 geçti**, son inceleme düzeltmelerinden
  sonra da (`20ee07a`, imleçli temizlik sayfalaması dahil) yeniden koşuldu (atılabilir veritabanı, test sonrası silindi; ziyaretçi izolasyonu, ilerleme kodu ve 180 gün temizliği dahil). Test
  `PG_TEST_URL` yoksa atlanır; canlı sunucunun PostgreSQL sürümü farklıysa dağıtımdan önce orada da bir kez
  koşulmalı (`docs/kurulum-site.md` §9).

## Sonuçlar

- Site herkese açık; hesap, e-posta, şifre, ekip kodu yok. `deploy/site.env.ornek`'te OpenRouter ve SMTP
  blokları yok; zorunlu tek sır veritabanı şifresi.
- AI maliyeti sitenin değil, sohbeti kullanan ziyaretçinin. Sunucuda kimsenin sırrı birikmez.
- Çerez silinirse ilerleme kaybolur — ilerleme kodu ile hafifletilir.
- Eski `kullanici`/`kullanici_token` tabloları mevcut veritabanında kalır; silinmeleri ayrı karar.
