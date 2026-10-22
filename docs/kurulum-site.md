# Kurulum ve işletme kılavuzu — çok kullanıcılı site (pera)

> Faz 9d (ADR-0012 site, ADR-0013 SMTP, ADR-0014 PostgreSQL, ADR-0015 ekip kodu). Bu belge sunucuyu kuracak ve işletecek
> kişi içindir. Gerçek sunucu adresi, şifre, anahtar BU BELGEYE YAZILMAZ — yer tutucular `<...>` biçimindedir.
> `egitim.pera.software` örnek alan adıdır.

## 1. Genel resim

```
tarayıcı ──https──▶ nginx (443, TLS, certbot) ──http──▶ uygulama 127.0.0.1:8090 (systemd, Java 17)
                                                            │
                                                            ├──▶ PostgreSQL 127.0.0.1:5432 (ai_ogretmen)
                                                            ├──▶ SMTP (YALNIZ APP_SITE_KAYIT=eposta)
                                                            └──▶ OpenRouter (şirket anahtarı)
```

- **Kayıt ortak ekip koduyla** (ADR-0015, varsayılan): site **e-posta göndermez**. Kayıt sayfasında
  `@pera.software` adresi + şifre + ekip kodu istenir; hesap hemen açılır. Şifresini unutanlara sıfırlama
  bağlantısını site sorumlusu üretir (§8.5). Eski e-postalı akış `APP_SITE_KAYIT=eposta` ile açılabilir
  (o zaman SMTP şarttır, ADR-0013).

- Uygulama **yalnız 127.0.0.1**'e bağlanır; dışarıya yalnız nginx açılır (AGENTS.md NEVER kuralı).
- Docker yok: uygulama bir **systemd servisi**dir (`deploy/ai-ogretmen-site.service`).
- Sunucudaki dizin düzeni (uygulama dosyaları bu dizine GÖRELİ okunur):

```
/opt/ai-ogretmen/
  ai-ogretmen.jar          # uygulama
  content/                 # müfredat (repodan kopya) — açılışta belleğe yüklenir
  prompts/                 # AI prompt şablonları (repodan kopya)
  data/                    # YAZILABİLİR: kişisel hafıza + rapor önbelleği (yedeklenir)
    hafiza/kullanicilar/<sha256(e-posta)>/...
    raporlar/kullanicilar/<sha256(e-posta)>/...
/etc/ai-ogretmen/site.env  # sırlar (deploy/site.env.ornek şablonundan)
```

## 2. Önkoşullar

Sunucu (Ubuntu 22.04/24.04 veya Debian 12 varsayıldı; komutlar `apt` ile):

| Gereken | Komut / not |
|---|---|
| Java 17 çalışma ortamı (JRE) | `sudo apt install openjdk-17-jre-headless` → `java -version` 17 göstermeli |
| PostgreSQL 14+ | `sudo apt install postgresql` (yalnız `localhost` dinlesin — varsayılan) |
| nginx | `sudo apt install nginx` |
| certbot | `sudo apt install certbot` |
| DNS | `egitim.pera.software` A/AAAA kaydı sunucuyu göstermeli (pera açar) |
| Güvenlik duvarı | Dışarıya yalnız 80 + 443 (+ yönetim için SSH). 8090 ve 5432 KAPALI kalır. |

Elinde olması gerekenler: şirket OpenRouter anahtarı; ekip kodu (§3.4'te üretilir). SMTP hesabı yalnız
`APP_SITE_KAYIT=eposta` seçilirse gerekir. Şifre sıfırlama betiği `psql` ister (`postgresql` paketiyle gelir).

## 3. İlk kurulum (adım adım)

Komutlar sunucuda **sudo yetkili kendi yönetici hesabınla** çalıştırılır (doğrudan root oturumu
açılmaz). `<yonetici>@<sunucu>` senin SSH hesabındır.

### 3.1 Sistem kullanıcısı ve dizinler
```bash
sudo useradd --system --home-dir /opt/ai-ogretmen --shell /usr/sbin/nologin ai-ogretmen
sudo install -d -m 0755 -o root -g root /opt/ai-ogretmen
sudo install -d -m 0750 -o ai-ogretmen -g ai-ogretmen /opt/ai-ogretmen/data
sudo install -d -m 0750 -o root -g ai-ogretmen /etc/ai-ogretmen
```
Uygulama kullanıcısı jar/content/prompts'u yalnız OKUR; yazabildiği tek yer `data/` (systemd
`ProtectSystem=strict` + `ReadWritePaths` bunu ayrıca zorlar).

### 3.2 PostgreSQL rolü ve veritabanı
Şablonu sunucuya kopyala, şifreyi değiştir, postgres yönetici hesabıyla çalıştır:
```bash
cp deploy/postgres-kurulum.sql.ornek /tmp/pg-kurulum.sql
nano /tmp/pg-kurulum.sql                       # <veritabani-sifresi> → güçlü bir şifre
sudo -u postgres psql -v ON_ERROR_STOP=1 -f /tmp/pg-kurulum.sql
shred -u /tmp/pg-kurulum.sql                   # şifre diskte kalmasın
```
Rol süper kullanıcı değildir; yalnız `ai_ogretmen` veritabanının sahibidir. Tablolar **uygulama ilk
açıldığında** `schema.sql` ile kurulur (her açılışta tekrar çalışır, idempotent — göç aracı yok).

### 3.3 Uygulamayı derle ve kopyala (geliştirici makinesinde)
```bash
./mvnw -DskipTests package                     # → target/ai-ogretmen-0.1.0-SNAPSHOT.jar
scp target/ai-ogretmen-0.1.0-SNAPSHOT.jar <yonetici>@<sunucu>:/tmp/ai-ogretmen.jar
rsync -a --delete content prompts <yonetici>@<sunucu>:/tmp/ai-ogretmen-yukle/
```
Sunucuda yerine koy (sahibi root, uygulama yalnız okur):
```bash
sudo install -m 0644 -o root -g root /tmp/ai-ogretmen.jar /opt/ai-ogretmen/ai-ogretmen.jar
sudo rsync -a --delete --chown=root:root /tmp/ai-ogretmen-yukle/content/ /opt/ai-ogretmen/content/
sudo rsync -a --delete --chown=root:root /tmp/ai-ogretmen-yukle/prompts/ /opt/ai-ogretmen/prompts/
sudo chmod -R a+rX /opt/ai-ogretmen/content /opt/ai-ogretmen/prompts
```
İşletmen betiğini de koy (şifre sıfırlama bağlantısı, §8.5):
```bash
scp tools/sifre-baglantisi.sh <yonetici>@<sunucu>:/tmp/            # geliştirici makinesinde
sudo install -D -m 0755 -o root -g root /tmp/sifre-baglantisi.sh /opt/ai-ogretmen/tools/sifre-baglantisi.sh
```
> İsteğe bağlı: bu belgeyi de `/opt/ai-ogretmen/docs/` altına koyarsan systemd `Documentation=` bağlantısı çalışır.

### 3.4 Ortam değişkenleri (sırlar)
```bash
sudo install -m 0640 -o root -g ai-ogretmen deploy/site.env.ornek /etc/ai-ogretmen/site.env
sudo nano /etc/ai-ogretmen/site.env
```
Doldurulacaklar: `OPENROUTER_API_KEY`, `SPRING_DATASOURCE_PASSWORD`, `APP_SITE_EKIP_KODU`, gerekiyorsa
alan adı (`APP_SITE_TEMEL_URL`, `APP_SITE_IZINLI_ORIGINLER`). Ekip kodunu rastgele üret ve ekibe güvenli bir
kanaldan (şirket içi mesaj) ilet — git'e, Obsidian Vault'a, bilet sistemine YAZMA:
```bash
openssl rand -base64 18        # 24 karakter; en az 12 karakter zorunlu
```
`APP_SITE_KAYIT=eposta` seçersen ayrıca `SPRING_MAIL_*` + `APP_SITE_GONDERICI` (o modda SMTP zorunlu).
Zorunlu olanlar eksikse uygulama **açılmaz** ve nedenini loga yazar (bkz. §9).

### 3.5 systemd servisi
```bash
sudo cp deploy/ai-ogretmen-site.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now ai-ogretmen-site
sudo systemctl status ai-ogretmen-site
```
Sunucu küçükse/büyükse `ExecStart` içindeki `-Xmx768m`'i ayarla (60 kullanıcı için 512m–1g yeter).

### 3.6 nginx + TLS sertifikası
```bash
sudo install -d /var/www/certbot
# 1) Önce yalnız 80 portlu bloğu etkinleştirip sertifika al:
sudo cp deploy/nginx-site.conf.ornek /etc/nginx/sites-available/ai-ogretmen-site.conf
#    (sertifika henüz yoksa 443 bloğunu geçici olarak yorum satırı yap)
sudo ln -s /etc/nginx/sites-available/ai-ogretmen-site.conf /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot certonly --webroot -w /var/www/certbot -d egitim.pera.software
# 2) 443 bloğunu aç, tekrar:
sudo nginx -t && sudo systemctl reload nginx
```
Yenileme: certbot'un zamanlayıcısı (`systemctl list-timers | grep certbot`) otomatik yeniler; yenileme
sonrası nginx'in yeniden yüklenmesi için:
`echo 'systemctl reload nginx' | sudo tee /etc/letsencrypt/renewal-hooks/deploy/nginx-reload.sh && sudo chmod +x $_`

## 4. İlk açılış ve sağlık kontrolleri

```bash
sudo journalctl -u ai-ogretmen-site -f            # "Started AiOgretmenApplication" görünmeli
curl -sI http://127.0.0.1:8090/giris.html | head -1   # HTTP/1.1 200
curl -sI https://egitim.pera.software/ | grep -i -E '^HTTP|^location'
#   → 302 ve Location: https://egitim.pera.software/giris.html  (http DEĞİL — vekil başlıkları çalışıyor)
curl -sI https://egitim.pera.software/giris.html | grep -i -E 'strict-transport|set-cookie'
#   → HSTS bir kez; XSRF-TOKEN çerezi "Secure"
sudo -u postgres psql -d ai_ogretmen -c '\dt'     # 8 tablo: kullanici, kullanici_token, gorev_ilerleme, ...
```
```bash
curl -s http://127.0.0.1:8090/api/kimlik/ayarlar        # {"kayit":"ekip-kodu"}
```
Logda şunlar beklenir: `E-posta: kullanılmıyor (app.site.kayit=ekip-kodu ...)` (eposta modunda:
`E-posta: SMTP <sunucu>:587 üzerinden ...`). Sonra tarayıcıdan bir `@pera.software` adresi + ekip koduyla
kayıt ol → giriş sayfasına "Hesabın açıldı" ile dönmeli → giriş yap → bir görevi işaretle. Kendine
§8.5'teki betikle bir sıfırlama bağlantısı üretip şifre değiştirmeyi de bir kez dene.

## 5. Yeni sürüme güncelleme

1. Geliştirici makinesinde: `./mvnw verify` YEŞİL → `./mvnw -DskipTests package`.
2. Önce yedek al (§6).
3. Jar + content/prompts'u §3.3'teki gibi `/tmp`'ye kopyala; sunucuda:
```bash
sudo cp /opt/ai-ogretmen/ai-ogretmen.jar /opt/ai-ogretmen/ai-ogretmen.jar.onceki   # geri dönüş için
sudo install -m 0644 -o root -g root /tmp/ai-ogretmen.jar /opt/ai-ogretmen/ai-ogretmen.jar
sudo rsync -a --delete --chown=root:root /tmp/ai-ogretmen-yukle/content/ /opt/ai-ogretmen/content/
sudo rsync -a --delete --chown=root:root /tmp/ai-ogretmen-yukle/prompts/ /opt/ai-ogretmen/prompts/
sudo systemctl restart ai-ogretmen-site && sudo journalctl -u ai-ogretmen-site -n 50
```
- Şema değişikliği varsa açılışta `schema.sql` kendiliğinden uygular (yalnız ekleyen, idempotent ifadeler).
- **content/ yalnız açılışta okunur** → müfredat güncellemesinden sonra da `restart` gerekir.
- Yeniden başlatma bütün oturumları düşürür (oturumlar bellekte): kullanıcılar tekrar giriş yapar.
  Mesai dışında yapılması önerilir.
- Geri dönüş: `ai-ogretmen.jar.onceki`'yi geri kopyala + restart (şema yalnız eklediği için eski sürüm çalışır).

## 6. Yedekleme ve geri yükleme

Yedeklenecek iki şey var: **PostgreSQL** (hesaplar, ilerleme, quiz, sohbet, kullanım) ve **`data/`**
(kişisel AI hafızası + rapor önbelleği). content/prompts repodan yeniden kurulur, yedek gerekmez.

```bash
sudo install -d -m 0700 -o postgres -g postgres /var/backups/ai-ogretmen
# Veritabanı (sıkıştırılmış özel biçim):
sudo -u postgres pg_dump -Fc -d ai_ogretmen -f /var/backups/ai-ogretmen/db-$(date +%F).dump
# Kişisel dosyalar:
sudo tar -C /opt/ai-ogretmen -czf /var/backups/ai-ogretmen/data-$(date +%F).tar.gz data
```
Günlük otomatik yedek (ör. `/etc/cron.d/ai-ogretmen-yedek`; 14 gün saklar):
```
30 3 * * * root /usr/bin/sudo -u postgres pg_dump -Fc -d ai_ogretmen -f /var/backups/ai-ogretmen/db-$(date +\%F).dump && tar -C /opt/ai-ogretmen -czf /var/backups/ai-ogretmen/data-$(date +\%F).tar.gz data && find /var/backups/ai-ogretmen -mtime +14 -delete
```
Yedekleri sunucu DIŞINA da kopyala (pera'nın yedek alanına). Yedekler kişisel veri içerir (KVKK).

**Geri yükleme:**
```bash
sudo systemctl stop ai-ogretmen-site
sudo -u postgres pg_restore --clean --if-exists --no-owner --role=ai_ogretmen -d ai_ogretmen \
  /var/backups/ai-ogretmen/db-<TARIH>.dump
sudo rm -rf /opt/ai-ogretmen/data && sudo tar -C /opt/ai-ogretmen -xzf /var/backups/ai-ogretmen/data-<TARIH>.tar.gz
sudo chown -R ai-ogretmen:ai-ogretmen /opt/ai-ogretmen/data
sudo systemctl start ai-ogretmen-site
```

## 7. Loglar

| Ne | Nerede |
|---|---|
| Uygulama | `sudo journalctl -u ai-ogretmen-site` (`-f` canlı, `--since "1 hour ago"`) |
| nginx | `/var/log/nginx/ai-ogretmen-site.access.log`, `.error.log` |
| PostgreSQL | `/var/log/postgresql/` |

Loglarda şifre, SMTP şifresi, doğrulama/sıfırlama bağlantısı YAZILMAZ (ADR-0013); e-posta adresleri yazılır.

## 8. Rutin işletme işleri

### 8.1 Ayrılan kullanıcının hesabını kapatma
Self-servis hesap silme yok (ADR-0012 §9); kapatmayı işletmen yapar. E-postalar **küçük harfle** saklanır:
```bash
sudo -u postgres psql -d ai_ogretmen -c \
  "UPDATE kullanici SET etkin = FALSE WHERE email = lower('Ad.Soyad@pera.software');"
```
- Kapalı hesap giriş yapamaz (`kapali`), şifre sıfırlama bağlantısı alamaz (betik reddeder; e-posta
  modunda e-posta da gitmez).
- **Anında etkili:** o anda açık bir oturumu varsa bir sonraki isteğinde düşer (API 401, sayfa →
  `giris.html?hata=kapali`); yeniden başlatma GEREKMEZ. Aynı şey satır silinince de geçerlidir.
- Yeniden açmak: `... SET etkin = TRUE ...`.
- Kişisel verinin TAMAMEN silinmesi istenirse (KVKK talebi):
```bash
E=ad.soyad@pera.software
sudo -u postgres psql -d ai_ogretmen -v e="$E" <<'SQL'
BEGIN;
DELETE FROM gorev_ilerleme WHERE kullanici_id = :'e';
DELETE FROM gonderim       WHERE kullanici_id = :'e';
DELETE FROM quiz_sonuc     WHERE kullanici_id = :'e';
DELETE FROM sohbet_mesaj   WHERE kullanici_id = :'e';
DELETE FROM mulakat_cevap  WHERE kullanici_id = :'e';
DELETE FROM ai_kullanim    WHERE kullanici_id = :'e';
DELETE FROM kullanici      WHERE email = :'e';      -- token'lar ON DELETE CASCADE ile gider
COMMIT;
SQL
D=$(printf '%s' "$E" | sha256sum | cut -d' ' -f1)
sudo rm -rf /opt/ai-ogretmen/data/hafiza/kullanicilar/$D /opt/ai-ogretmen/data/raporlar/kullanicilar/$D
```
Yedeklerdeki kopyalar saklama süresi (14 gün) dolunca kendiliğinden silinir.

### 8.2 Kilitlenen hesabı açma
5 hatalı denemede hesap 15 dk kilitlenir (ADR-0012 §6); beklemek yeterli. Hemen açmak için:
`UPDATE kullanici SET basarisiz_deneme = 0, kilit_bitis = NULL WHERE email = '...';`
Şifresini unutan kullanıcı: ekip kodu modunda site sorumlusuna yazar (§8.5); eposta modunda giriş
sayfasındaki "Şifremi unuttum"u kullanır. Başarılı şifre sıfırlama kilidi ve sayacı da temizler.

### 8.3 OpenRouter anahtarını değiştirme
```bash
sudo nano /etc/ai-ogretmen/site.env      # OPENROUTER_API_KEY=<yeni-anahtar>
sudo systemctl restart ai-ogretmen-site
```
Eski anahtarı OpenRouter panelinden iptal et. Aylık harcama tavanını da OpenRouter panelinden koy
(sitede kota yok — kullanıcı kararı). Kişi başı kullanım: `SELECT kullanici_id, islem, COUNT(*),
SUM(input_tokens), SUM(output_tokens) FROM ai_kullanim GROUP BY 1, 2 ORDER BY 1;`

### 8.4 SMTP / veritabanı şifresini değiştirme
`/etc/ai-ogretmen/site.env`'de ilgili değeri değiştir → `restart`. Veritabanı şifresi için önce
`sudo -u postgres psql -c "ALTER ROLE ai_ogretmen PASSWORD '<yeni>';"`.

### 8.5 Şifre sıfırlama bağlantısı üretme (ekip kodu modu)
Kullanıcı "şifremi unuttum" diye yazınca, kimliğinden emin olduktan sonra (ör. şirket içi mesajdan):
```bash
sudo bash /opt/ai-ogretmen/tools/sifre-baglantisi.sh Ad.Soyad@pera.software /etc/ai-ogretmen/site.env
# → https://egitim.pera.software/sifre-sifirla.html?token=...
```
- Çıktıdaki bağlantıyı **yalnız o kişiye** güvenli kanaldan ilet (bağlantıyı açan şifreyi değiştirebilir).
  1 saat geçerli, tek kullanımlık; yeni bağlantı üretmek eskisini geçersiz kılar.
- Kullanıcı yeni şifreyi koyunca bütün açık oturumları düşer, kilidi kalkar.
- Betik veritabanına `site.env`'deki `SPRING_DATASOURCE_*` ile bağlanır; adresi küçük harfe çevirir; hesap
  yoksa, kapalıysa (`etkin=false`) ya da doğrulanmamışsa bağlantı üretmez. Uygulamayla **aynı sunucuda**
  çalıştır (süre, sunucunun yerel saatiyle yazılır).

### 8.6 Ekip kodunu değiştirme (sızdıysa ya da ekipten ayrılan oldu)
```bash
openssl rand -base64 18                      # yeni kod
sudo nano /etc/ai-ogretmen/site.env          # APP_SITE_EKIP_KODU=<yeni-kod>
sudo systemctl restart ai-ogretmen-site
```
Mevcut hesaplar etkilenmez (kod yalnız kayıtta istenir). Sızıntı döneminde açılmış tanımadığın hesapları
gözden geçir ve gerekirse kapat (§8.1):
`sudo -u postgres psql -d ai_ogretmen -c "SELECT email, olusturma FROM kullanici ORDER BY olusturma DESC LIMIT 20;"`

## 9. Sorun giderme

| Belirti | Neden / çözüm |
|---|---|
| Servis açılmıyor, logda `site profili veritabanı adresini ister: SPRING_DATASOURCE_URL` | `site.env`'de `SPRING_DATASOURCE_URL` yok/boş. Site H2'ye sessizce düşmez (ADR-0014). |
| Logda `password authentication failed for user "ai_ogretmen"` | `SPRING_DATASOURCE_PASSWORD` yanlış ya da `pg_hba.conf` 127.0.0.1 için `scram-sha-256` izin vermiyor. |
| Servis açılmıyor, logda `site profili ekip kodu olmadan açılamaz` | `APP_SITE_EKIP_KODU` yok, boş ya da 12 karakterden kısa (ADR-0015). `openssl rand -base64 18` ile üret. |
| Servis açılmıyor, logda `Failed to bind properties under 'app.site.kayit'` | `APP_SITE_KAYIT` yalnız `ekip-kodu` ya da `eposta` olabilir. |
| Servis açılmıyor, logda `site profili SMTP olmadan açılamaz` | Yalnız `APP_SITE_KAYIT=eposta`: `SPRING_MAIL_HOST` (+ kullanıcı/şifre) eksik (ADR-0013). |
| Kayıtta "Ekip kodu hatalı" | Kullanıcı eski/yanlış kodu giriyor; güncel kodu ilet (baştaki/sondaki boşluklar önemsiz, büyük-küçük harf önemli). |
| `sifre-baglantisi.sh`: `hesap yok` / `kapalı` | Adres yanlış yazıldı ya da kullanıcı hiç kayıt olmadı / hesap kapatılmış (§8.1). |
| (eposta modu) Kayıt "e-posta gönderildi" diyor ama e-posta gelmiyor | `journalctl -u ai-ogretmen-site | grep 'E-posta gönderilemedi'` → hata sınıfı yazar: kimlik doğrulama (şifre), bağlantı (port 587 dışarı kapalı mı?), STARTTLS. Gereksiz kutusuna bak; adres+tür başına saatte en çok 3 e-posta gider. |
| Form gönderimleri (giriş, kayıt, görev işaretleme, quiz, sohbet) 403 veriyor | `APP_SITE_IZINLI_ORIGINLER` tarayıcının gördüğü adresle **birebir** olmalı: `https://egitim.pera.software` (sonda `/` yok, şema dahil). Değiştirdiysen `restart`. |
| Giriş yapınca tekrar giriş sayfasına dönüyor | Site **http** üzerinden açılıyor: `Secure` çerezleri tarayıcı http'de geri göndermez. https adresini kullan; nginx `X-Forwarded-Proto` gönderiyor mu kontrol et. Yalnız yerel denemede `APP_SITE_GUVENLI_CEREZ=false`. |
| Yönlendirmeler `http://127.0.0.1:8090/...` adresine gidiyor | nginx `X-Forwarded-*` başlıklarını göndermiyor (şablondaki `proxy_set_header` satırları) ya da nginx uygulamaya 127.0.0.1 dışından bağlanıyor (uygulama yalnız 127.0.0.1 vekile güvenir). |
| nginx 502 Bad Gateway | Uygulama kapalı/açılıyor: `systemctl status ai-ogretmen-site`. |
| nginx 504 / AI cevabı yarıda kesiliyor | AI çağrısı 150 sn'yi aştı (OpenRouter yavaş) ya da `proxy_read_timeout` < 180s. |
| AI özellikleri "devre dışı" | `OPENROUTER_API_KEY` boş/yanlış ya da kredi bitti (402). |
| Sıfırlama/doğrulama bağlantısı yanlış adrese gidiyor | `APP_SITE_TEMEL_URL` (bağlantı kökü Host başlığından alınmaz). |

## 10. Geliştirici notu: PostgreSQL uyumluluk testi

`schema.sql` hem H2 (yerel mod) hem PostgreSQL'de çalışmak zorunda. Normal `./mvnw verify` PostgreSQL'e
bağlanmaz; yerel bir PostgreSQL varsa **atılabilir** bir veritabanıyla elle koş:
```bash
createdb ai_ogretmen_test_x
PG_TEST_URL=jdbc:postgresql://localhost:5432/ai_ogretmen_test_x PG_TEST_USERNAME=<kullanici> \
  PG_TEST_PASSWORD=<sifre> ./mvnw -Dtest=SitePostgresEntegrasyonTest test
dropdb ai_ogretmen_test_x
```
Bu test `tools/sifre-baglantisi.sh`'yi de aynı veritabanına karşı çalıştırır (`psql` gerekir): üretilen
bağlantının ~1 saat geçerli olduğunu ve `/api/kimlik/sifre-sifirla` ile çalıştığını doğrular.

**Yerel deneme:** `site` profilini veritabanı kurmadan denemek için H2 adresini açıkça ver (ekip kodu
modunda SMTP gerekmez; yerelde herhangi bir ≥12 karakterlik kod yeter):
```bash
SPRING_DATASOURCE_URL='jdbc:h2:file:./data/site-deneme' APP_SITE_EKIP_KODU=yerel-deneme-kodu \
  APP_SITE_GUVENLI_CEREZ=false OPENROUTER_API_KEY=... ./mvnw spring-boot:run -Dspring-boot.run.profiles=site
```
E-postalı akışı yerelde denemek için `APP_SITE_KAYIT=eposta APP_SITE_EPOSTA_ZORUNLU=false` ekle (bağlantılar
loga yazılır). Şifre sıfırlama betiği yalnız PostgreSQL ile çalışır (H2 dosyasına bağlanmaz).
