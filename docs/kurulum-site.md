# Kurulum ve işletme kılavuzu — girişsiz eğitim sitesi

> Faz 10 (ADR-0017 girişsiz site + kendi anahtarın, ADR-0014 PostgreSQL). Bu belge sunucuyu kuracak ve
> işletecek kişi içindir. Gerçek sunucu adresi, şifre, anahtar BU BELGEYE YAZILMAZ — yer tutucular `<...>`
> biçimindedir. `egitim.ornek.com` örnek alan adıdır; kendi alan adınla değiştir.

## 1. Genel resim

```
tarayıcı ──https──▶ nginx (443, TLS, certbot) ──http──▶ uygulama 127.0.0.1:8090 (systemd, Java 17)
   │                                                        │
   │ (ziyaretçinin kendi OpenRouter anahtarı,               ├──▶ PostgreSQL 127.0.0.1:5432 (ai_ogretmen)
   │  yalnız sohbet isteğinde başlıkta)                     └──▶ OpenRouter (YALNIZ sohbet; ziyaretçinin anahtarıyla)
```

- **Giriş/kayıt yok** (ADR-0017). Her ziyaretçi tarayıcısındaki rastgele `aio_ziyaretci` çereziyle tanınır;
  ilerlemesi sunucuda o kimlikle saklanır. Başka tarayıcıda devam etmek için "İlerleme kodum" → "Geri yükle".
  Hesap, e-posta, şifre, ekip kodu, SMTP **yoktur**.
- **Sunucuda OpenRouter anahtarı yoktur.** AI yalnız isteğe bağlı sohbet botudur; kullanmak isteyen ziyaretçi
  **kendi** anahtarını sohbet paneline yapıştırır (tarayıcıda durur, her sohbet isteğiyle gönderilir, sunucu
  o istekte kullanıp unutur — hiçbir yere yazılmaz). Rapor, AI değerlendirme, AI quiz, kitap notu sitede yok.
- Uygulama **yalnız 127.0.0.1**'e bağlanır; dışarıya yalnız nginx açılır (AGENTS.md NEVER kuralı).
- Docker yok: uygulama **tek örnekli** bir **systemd servisi**dir (`deploy/ai-ogretmen-site.service`). Tek
  örnek şarttır: hız sınırları JVM belleğinde tutulur, gece temizliği kilitsiz çalışır (ADR-0017 §7–§8).
- Sunucudaki dizin düzeni (uygulama dosyaları bu dizine GÖRELİ okunur):

```
/opt/ai-ogretmen/
  ai-ogretmen.jar          # uygulama
  content/                 # müfredat (repodan kopya) — açılışta belleğe yüklenir
  prompts/                 # AI prompt şablonları (repodan kopya)
  data/                    # YAZILABİLİR: ziyaretçinin AI hafızası (yedeklenir)
    hafiza/kullanicilar/<sha256("z-<ilerleme-kodu>")>/...
/etc/ai-ogretmen/site.env  # ortam değişkenleri (deploy/site.env.ornek şablonundan)
```

## 2. Önkoşullar

Sunucu (Ubuntu 22.04/24.04 veya Debian 12 varsayıldı; komutlar `apt` ile):

| Gereken | Komut / not |
|---|---|
| Java 17 çalışma ortamı (JRE) | `sudo apt install openjdk-17-jre-headless` → `java -version` 17 göstermeli |
| PostgreSQL 14+ | `sudo apt install postgresql` (yalnız `localhost` dinlesin — varsayılan) |
| nginx | `sudo apt install nginx` |
| certbot | `sudo apt install certbot` |
| DNS | `egitim.ornek.com` A/AAAA kaydı sunucuyu göstermeli |
| Güvenlik duvarı | Dışarıya yalnız 80 + 443 (+ yönetim için SSH). 8090 ve 5432 KAPALI kalır. |

Elinde olması gereken tek sır: veritabanı şifresi (§3.2'de sen belirlersin). OpenRouter anahtarı ve SMTP
hesabı **gerekmez**.

**Dağıtımdan önce bir kez:** `SitePostgresEntegrasyonTest`'i atılabilir bir PostgreSQL veritabanına karşı
koş (§9). Faz 10 dalında yerel PostgreSQL 14.15'e karşı 5/5 geçti (ADR-0017); canlı sunucunun sürümü
farklıysa orada da bir kez koş.

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
> İsteğe bağlı: bu belgeyi de `/opt/ai-ogretmen/docs/` altına koyarsan systemd `Documentation=` bağlantısı çalışır.

### 3.4 Ortam değişkenleri
```bash
sudo install -m 0640 -o root -g ai-ogretmen deploy/site.env.ornek /etc/ai-ogretmen/site.env
sudo nano /etc/ai-ogretmen/site.env
```
Doldurulacaklar: `SPRING_DATASOURCE_PASSWORD` ve alan adın (`APP_SITE_IZINLI_ORIGINLER`).
`OPENROUTER_API_KEY` **yazma** — sitede kullanılmaz (yazılsa bile site yolu onu okumaz; şablonda yok).
Zorunlu olanlar eksikse uygulama **açılmaz** ve nedenini loga yazar (bkz. §8).

### 3.5 systemd servisi
```bash
sudo cp deploy/ai-ogretmen-site.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now ai-ogretmen-site
sudo systemctl status ai-ogretmen-site
```
Sunucu küçükse/büyükse `ExecStart` içindeki `-Xmx768m`'i ayarla (512m–1g yeter). **Tek örnek** çalıştır.

### 3.6 nginx + TLS sertifikası
```bash
sudo install -d /var/www/certbot
# 1) Önce yalnız 80 portlu bloğu etkinleştirip sertifika al:
sudo cp deploy/nginx-site.conf.ornek /etc/nginx/sites-available/ai-ogretmen-site.conf
#    (alan adını kendi adınla değiştir; sertifika henüz yoksa 443 bloğunu geçici olarak yorum satırı yap)
sudo ln -s /etc/nginx/sites-available/ai-ogretmen-site.conf /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot certonly --webroot -w /var/www/certbot -d egitim.ornek.com
# 2) 443 bloğunu aç, tekrar:
sudo nginx -t && sudo systemctl reload nginx
```
Yenileme: certbot'un zamanlayıcısı (`systemctl list-timers | grep certbot`) otomatik yeniler; yenileme
sonrası nginx'in yeniden yüklenmesi için:
`echo 'systemctl reload nginx' | sudo tee /etc/letsencrypt/renewal-hooks/deploy/nginx-reload.sh && sudo chmod +x $_`

nginx'in `X-Forwarded-For` başlığı şarttır: hız sınırları istemci IP'sine göre sayılır ve uygulama gerçek IP'yi
yalnız 127.0.0.1'deki vekilden gelen bu başlıktan öğrenir. Başlık yoksa bütün ziyaretçiler tek IP sayılır.

## 4. İlk açılış ve sağlık kontrolleri

```bash
sudo journalctl -u ai-ogretmen-site -f            # "Started AiOgretmenApplication" görünmeli
curl -sI http://127.0.0.1:8090/ | head -1         # HTTP/1.1 200
curl -s http://127.0.0.1:8090/api/ozellikler      # {"mulakat":false,"site":true,"sunucuAi":false} (alan sırası değişebilir)
curl -sI https://egitim.ornek.com/ | grep -i -E '^HTTP|strict-transport|set-cookie'
#   → 200; HSTS bir kez; aio_ziyaretci ve XSRF-TOKEN çerezleri "Secure", aio_ziyaretci "HttpOnly"
sudo -u postgres psql -d ai_ogretmen -c '\dt'     # gorev_ilerleme, gonderim, quiz_sonuc, sohbet_mesaj, ziyaretci, ...
```
Logda şu satır beklenir: `site profili — AI ziyaretçinin kendi OpenRouter anahtarıyla (istek başına); sunucu
anahtarı kullanılmaz.` Sonra tarayıcıdan siteyi aç → bir görevi "Okudum ✓" ile işaretle → "İlerleme kodum"dan
kodu kopyala → başka bir tarayıcıda (ya da gizli pencerede) "Geri yükle" ile aynı ilerlemeyi gör. Sohbeti
denemek için kendi OpenRouter anahtarını sohbet paneline yapıştır (harcama limitli bir deneme anahtarı).

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
- Yeniden başlatma ziyaretçileri düşürmez (kimlik tarayıcıdaki çerezde, oturum yok); yalnız bellekteki hız
  sınırı sayaçları sıfırlanır. O anda süren bir sohbet isteği yarıda kalabilir.
- Geri dönüş: `ai-ogretmen.jar.onceki`'yi geri kopyala + restart (şema yalnız eklediği için eski sürüm çalışır).

### 5.1 Eski (girişli) siteden geçiş
Faz 9 veritabanında `kullanici` ve `kullanici_token` tabloları **kalır** — yeni `schema.sql` onları
oluşturmaz ama silmez de (ADR-0017). Eski e-posta kimlikli ilerleme satırları sahipsiz kalır ve 180 gün
temizliğine girmez (`ziyaretci` tablosunda yoklar). Silmek ayrı, bilinçli bir karardır; karar verilirse önce
yedek al (§6), sonra:
```bash
sudo -u postgres psql -d ai_ogretmen -c "SELECT COUNT(*) FROM kullanici;"   # önce ne silineceğini gör
```
Eski `site.env`'yi yeni şablonla (`deploy/site.env.ornek`) karşılaştır; şablonda olmayan satırları (OpenRouter
anahtarı, kayıt yöntemi, ekip kodu, SMTP ve gönderici, temel URL) sil — artık okunmuyorlar. Eski şirket
anahtarını OpenRouter panelinden de iptal et.

## 6. Yedekleme ve geri yükleme

Yedeklenecek iki şey var: **PostgreSQL** (ziyaretçiler, ilerleme, quiz, sohbet, kullanım) ve **`data/`**
(ziyaretçinin AI hafızası). content/prompts repodan yeniden kurulur, yedek gerekmez.

```bash
sudo install -d -m 0700 -o postgres -g postgres /var/backups/ai-ogretmen
# Veritabanı (sıkıştırılmış özel biçim):
sudo -u postgres pg_dump -Fc -d ai_ogretmen -f /var/backups/ai-ogretmen/db-$(date +%F).dump
# Kişisel dosyalar:
sudo tar -C /opt/ai-ogretmen -czf /var/backups/ai-ogretmen/data-$(date +%F).tar.gz data
```
Günlük otomatik yedek (ör. `/etc/cron.d/ai-ogretmen-yedek`; 14 gün saklar). 03:30'daki ziyaretçi
temizliğiyle çakışmasın diye 04:30'da:
```
30 4 * * * root /usr/bin/sudo -u postgres pg_dump -Fc -d ai_ogretmen -f /var/backups/ai-ogretmen/db-$(date +\%F).dump && tar -C /opt/ai-ogretmen -czf /var/backups/ai-ogretmen/data-$(date +\%F).tar.gz data && find /var/backups/ai-ogretmen -mtime +14 -delete
```
Yedekleri sunucu DIŞINA da kopyala. Yedekler ziyaretçi verisi içerir (sohbet metinleri) — buna göre sakla.

**Geri yükleme:**
```bash
sudo systemctl stop ai-ogretmen-site
sudo -u postgres pg_restore --clean --if-exists --no-owner --role=ai_ogretmen -d ai_ogretmen \
  /var/backups/ai-ogretmen/db-<TARIH>.dump
sudo rm -rf /opt/ai-ogretmen/data && sudo tar -C /opt/ai-ogretmen -xzf /var/backups/ai-ogretmen/data-<TARIH>.tar.gz
sudo chown -R ai-ogretmen:ai-ogretmen /opt/ai-ogretmen/data
sudo systemctl start ai-ogretmen-site
```

## 7. Rutin işletme işleri

### 7.1 Loglar

| Ne | Nerede |
|---|---|
| Uygulama | `sudo journalctl -u ai-ogretmen-site` (`-f` canlı, `--since "1 hour ago"`) |
| nginx | `/var/log/nginx/ai-ogretmen-site.access.log`, `.error.log` |
| PostgreSQL | `/var/log/postgresql/` |

Loglarda ziyaretçinin OpenRouter anahtarı ve ilerleme kodu YAZILMAZ; sağlayıcı hatasında yalnız HTTP kodu
ve kategori yazılır (`AI sağlayıcı hatası: islem=SOHBET kod=401 neden=ANAHTAR_GECERSIZ`).

### 7.2 180 gün temizliği
Her gece 03:30'da (sunucu saati) 180 gündür gelmeyen ziyaretçilerin bütün satırları ve hafıza dizini silinir.
Kontrol:
```bash
sudo journalctl -u ai-ogretmen-site --since today | grep 'Ziyaretçi temizliği'
#   → "Ziyaretçi temizliği tamam: N ziyaretçi silindi, M geri döndüğü için atlandı, K başarısız"
```
`… başarısız (sonraki turda yeniden denenecek)` görürsen o ziyaretçinin silinmesi geri alınmıştır; sonraki
gece yeniden denenir. `kişisel dizin silinemedi` nedeninde hafıza/rapor dizini (izin, disk) silinemediği için
veritabanı satırları da geri alınmıştır — izinleri düzelt, sonraki gece hepsi birlikte silinir (ADR-0017).
`Ziyaretçi geri döndü, atlandı` (INFO) hata değildir: ziyaretçi listelemeyle silme arasında geldi, verisi
korundu; özetteki "atlandı" sayısına girer, "başarısız"a girmez.

### 7.3 Bir ziyaretçinin verisini silme (KVKK talebi)
Ziyaretçi kendi tarayıcısında "🔄 Sıfırla" düğmesiyle ilerlemesini silebilir. Tamamen silinmesini
isterse **ilerleme kodunu** göndermesi gerekir (başka tanımlayıcı yok):
```bash
K='z-<ilerleme-kodu>'
sudo -u postgres psql -d ai_ogretmen -v k="$K" <<'SQL'
BEGIN;
DELETE FROM gorev_ilerleme WHERE kullanici_id = :'k';
DELETE FROM gonderim       WHERE kullanici_id = :'k';
DELETE FROM quiz_sonuc     WHERE kullanici_id = :'k';
DELETE FROM sohbet_mesaj   WHERE kullanici_id = :'k';
DELETE FROM mulakat_cevap  WHERE kullanici_id = :'k';
DELETE FROM ai_kullanim    WHERE kullanici_id = :'k';
DELETE FROM ziyaretci      WHERE id = :'k';
COMMIT;
SQL
D=$(printf '%s' "$K" | sha256sum | cut -d' ' -f1)
sudo rm -rf /opt/ai-ogretmen/data/hafiza/kullanicilar/$D /opt/ai-ogretmen/data/raporlar/kullanicilar/$D
```
Yedeklerdeki kopyalar saklama süresi (14 gün) dolunca kendiliğinden silinir.

### 7.4 Veritabanı şifresini değiştirme
`sudo -u postgres psql -c "ALTER ROLE ai_ogretmen PASSWORD '<yeni>';"` → `/etc/ai-ogretmen/site.env`'de
`SPRING_DATASOURCE_PASSWORD`'ü değiştir → `sudo systemctl restart ai-ogretmen-site`.

### 7.5 Kullanım
Sitede kota yok; AI maliyeti ziyaretçinin kendi anahtarına yazılır. Ziyaretçi başı token/süre (anahtarsız):
`SELECT kullanici_id, islem, COUNT(*), SUM(input_tokens), SUM(output_tokens) FROM ai_kullanim GROUP BY 1, 2 ORDER BY 1;`

### 7.6 Hız sınırları (bilgi)
Aşımda 429 Türkçe mesaj döner: yazan istekler IP başına 60/dk, sohbet ziyaretçi başına 20/dk, ilerleme kodu
geri yükleme IP başına 10/saat, yeni ziyaretçi IP başına 20/saat (varsayılanlar). Sayaçlar bellekte; restart
sıfırlar. Pencereler sabit, sayılar `/etc/ai-ogretmen/site.env`'den ayarlanır (sonra `systemctl restart`):

| Değişken | Varsayılan | Anlamı |
|---|---|---|
| `APP_SITE_HIZ_YAZMA_DAKIKA` | 60 | yazan istek / dakika / IP |
| `APP_SITE_HIZ_SOHBET_DAKIKA` | 20 | sohbet mesajı / dakika / ziyaretçi |
| `APP_SITE_HIZ_GERI_YUKLE_SAAT` | 10 | ilerleme kodu geri yükleme / saat / IP |
| `APP_SITE_HIZ_YENI_ZIYARETCI_SAAT` | 20 | yeni ziyaretçi / saat / IP |

**Tek NAT arkasında sınıf/ofis:** herkes aynı genel IP'den gelir, IP başına sınırlar bütün sınıfa paylaşılır
(ör. 30 kişilik sınıf ilk derste 20 yeni ziyaretçiden sonra 429 alır). Böyle bir kullanım bekleniyorsa
`APP_SITE_HIZ_YENI_ZIYARETCI_SAAT` (ör. 60) ve `APP_SITE_HIZ_YAZMA_DAKIKA` (ör. 300) yükseltilir; sohbet sınırı
ziyaretçi başına olduğundan etkilenmez. Değerler en az 1 olmalı (yoksa uygulama açılmaz).

## 8. Sorun giderme

| Belirti | Neden / çözüm |
|---|---|
| Servis açılmıyor, logda `site profili veritabanı adresini ister: SPRING_DATASOURCE_URL` | `site.env`'de `SPRING_DATASOURCE_URL` yok/boş. Site H2'ye sessizce düşmez (ADR-0014). |
| Logda `password authentication failed for user "ai_ogretmen"` | `SPRING_DATASOURCE_PASSWORD` yanlış ya da `pg_hba.conf` 127.0.0.1 için `scram-sha-256` izin vermiyor. |
| Görev işaretleme, quiz, sohbet 403 veriyor | `APP_SITE_IZINLI_ORIGINLER` tarayıcının gördüğü adresle **birebir** olmalı: `https://egitim.ornek.com` (sonda `/` yok, şema dahil). Değiştirdiysen `restart`. |
| Her sayfa yenilemede ilerleme "kayboluyor" | Site **http** üzerinden açılıyor: `Secure` çerezleri tarayıcı http'de geri göndermez → her istekte yeni ziyaretçi. https adresini kullan; nginx `X-Forwarded-Proto` gönderiyor mu kontrol et. Yalnız yerel denemede `APP_SITE_GUVENLI_CEREZ=false`. |
| Çok sayıda ziyaretçi aynı anda 429 alıyor | nginx `X-Forwarded-For` göndermiyor ya da uygulamaya 127.0.0.1 dışından bağlanıyor → herkes tek IP sayılıyor (§3.6). |
| Yönlendirmeler `http://127.0.0.1:8090/...` adresine gidiyor | nginx `X-Forwarded-*` başlıklarını göndermiyor (şablondaki `proxy_set_header` satırları) ya da nginx uygulamaya 127.0.0.1 dışından bağlanıyor (uygulama yalnız 127.0.0.1 vekile güvenir). |
| "Geri yükle" → "Bu kodla kayıtlı ilerleme bulunamadı" | Kod yanlış kopyalandı ya da kodun sahibi henüz hiçbir şey işaretlememiş (satır ilk yazmada açılır) ya da 180 gündür gelmediği için silinmiş. |
| Sohbet "OpenRouter anahtarın geçersiz" / "kredin bitti" diyor | Ziyaretçinin kendi anahtarıyla ilgili (401 / 402); sunucu tarafında yapılacak bir şey yok. |
| nginx 502 Bad Gateway | Uygulama kapalı/açılıyor: `systemctl status ai-ogretmen-site`. |
| nginx 504 / AI cevabı yarıda kesiliyor | Sohbet çağrısı 150 sn'yi aştı (OpenRouter yavaş) ya da `proxy_read_timeout` < 180s. |

## 9. Geliştirici notu: PostgreSQL uyumluluk testi

`schema.sql` hem H2 (yerel mod) hem PostgreSQL'de çalışmak zorunda. Normal `./mvnw verify` PostgreSQL'e
bağlanmaz; yerel bir PostgreSQL varsa **atılabilir** bir veritabanıyla elle koş (**dağıtımdan önce şart**):
```bash
createdb ai_ogretmen_test_x
PG_TEST_URL=jdbc:postgresql://localhost:5432/ai_ogretmen_test_x PG_TEST_USERNAME=<kullanici> \
  PG_TEST_PASSWORD=<sifre> ./mvnw -Dtest=SitePostgresEntegrasyonTest test
dropdb ai_ogretmen_test_x
```
Test şemayı iki kez uygular (idempotent), ziyaretçi kaydını, iki ziyaretçinin izolasyonunu, ilerleme kodu geri
yüklemesini ve 180 gün temizliğini (tek okuma-yazma işleminde) gerçek PostgreSQL'de doğrular. Temizlik testi
veritabanındaki **bütün** eski ziyaretçileri siler — yalnız atılabilir veritabanıyla koş.

**Yerel deneme:** `site` profilini veritabanı kurmadan denemek için H2 adresini açıkça ver (OpenRouter
anahtarı gerekmez; sohbeti denemek için kendi anahtarını tarayıcıda gir):
```bash
SPRING_DATASOURCE_URL='jdbc:h2:file:./data/site-deneme' APP_SITE_GUVENLI_CEREZ=false \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=site
```
