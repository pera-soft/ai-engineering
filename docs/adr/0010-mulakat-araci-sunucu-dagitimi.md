# ADR-0010 — Mülakat çalışma aracının kendi sunucusuna dağıtımı

- **Durum:** Kabul edildi
- **Tarih:** 2026-08-06
- **Bağlam sahibi:** kullanıcı kararı (laptop kaynak tüketimi)

## Bağlam

Mülakat modülü bugüne kadar yalnız `127.0.0.1:8090`'da, kullanıcının dizüstü bilgisayarında
çalışıyordu (ledger'daki kapsam notu: "mülakat modülü YERELDE kalır"). Modül 10 alan / 257 konu /
1551 soruya büyüdü ve uygulamayı sürekli açık tutmak makineyi yavaşlatıyordu. Kullanıcı çalışmayı
sunucuya taşımak istedi.

Hedef sunucu ayrı bir uygulama sunucusu — üzerinde üretim yükleri var: velzonapi (Spring Boot,
8080), Django + Daphne (8001/8002), Jenkins (8090), PostgreSQL, Redis, crypto-ws-worker.
Kısıt: **çalışan hiçbir projeye dokunulmayacak.**

Not: aynı kod tabanı `ai.velzon.tr` üzerinde `/opt/ai-ogretmen`'de de çalışıyor.
Kullanıcı yeni kurulumu bilerek `.159`'a istedi; sonuç olarak **aynı uygulamanın iki dağıtımı** var
ve `content/` değişikliğinde iki hedef düşünülmelidir.

## Karar

Ayrı bir systemd servisi olarak `/opt/ai-ogretmen-mulakat` altına kuruldu; adres
**https://mulakat.velzon.tr**.

1. **Port 8095** — 8090 Jenkins tarafından kullanılıyor. `application.yml` değiştirilmedi; port
   systemd'de `SERVER_PORT` ile geçersiz kılındı. `server.address: 127.0.0.1` **korundu**
   (CLAUDE.md "NEVER: 127.0.0.1 dışına bind" kuralı); dış erişim yalnız nginx üzerinden.
2. **Komşuları koruma (asıl mühendislik kararı):** servis cgroup ile sınırlandı —
   `MemoryMax=768M`, `MemoryHigh=640M`, `CPUQuota=150%` ve **`OOMScoreAdjust=500`**. Sonuncusu
   kritik: bellek daralırsa çekirdek **önce bu servisi** öldürür, üretimdeki velzonapi'ye dokunmaz.
   Ölçüm: uygulama 336 MB'de oturuyor, sunucuda 2.1 GB kullanılabilir bellek + 1.7 GB boş swap var;
   30 günde çekirdek OOM sayısı 0.
3. **Ayrı sistem kullanıcısı** `aiogretmen` + systemd sertleştirme (`ProtectSystem=strict`,
   `NoNewPrivileges`, `PrivateTmp`, `ReadWritePaths` yalnız kendi dizini).
4. **TLS:** Let's Encrypt + certbot (kutudaki diğer sitelerle aynı desen), Cloudflare **Proxied**.
   İlk sertifika alınırken Cloudflare SSL modu **Full (strict)** olduğu için 526 yumurta-tavuk
   sorunu yaşandı; kayıt geçici olarak DNS-only yapılıp sertifika alındı, sonra tekrar proxy'ye
   dönüldü. **Yenilemelerde tekrar etmez** (sertifika var olduğu için CF origin'e HTTPS ile ulaşır).
5. **Veri:** yerel `data/ogrenme.mv.db` sunucuya taşındı (17 doğru cevap / 32 cevaplanmış soru
   korundu). H2 dosya tabanlı ve **tek yazarlı** → laptop ve sunucu aynı veritabanını paylaşamaz.
   Kullanıcı kararı: **laptop yalnız geliştirme, çalışma sunucuda.**

## Giriş koruması — bilinçli kabul edilen risk

Kullanıcı giriş penceresi (Cloudflare Access / Basic Auth) **istemedi**. Uygulama herkese açık.
Kod okunarak somut risk yüzeyi çıkarıldı ve nginx seviyesinde daraltıldı:

- `POST /api/ogretmen/sohbet`, `/api/ogretmen/degerlendir`, `/api/atolye/degerlendir` → **403**.
  Bunlar serbest metin alıp LLM'e iletiyordu; açık bırakılsalar uygulama, hesap sahibinin
  kredisiyle çalışan bir genel amaçlı LLM proxy'sine dönerdi.
- `POST /api/program/sifirla` → **403** (gövdesiz tek istekle ilerleme silinebiliyordu).
- `POST /api/mulakat/acikla` → **dakikada 20 istek** (`limit_req`, anahtar `CF-Connecting-IP`,
  burst 10). Doğrulandı: 40 eşzamanlı istekte 29'u 503. Tek kullanıcı bunu hissetmez.
- Mülakat 🤖 ucunun kendisi enjeksiyona kapalı: prompt sunucuda kanonik sorudan kurulur,
  istemciden yalnız `soruId` + `secilenIndex` gelir → genel amaçlı üretim yaptırılamaz.

**Kalan kabul edilmiş risk:** içerik (gerçek mülakat kaydı, davranışsal/maaş notları, ilerleme
verisi) adresi bilen herkese açıktır. Adres, sertifika şeffaflık (CT) kayıtlarına düştüğü için
taranabilir — "kimse bilmez" varsayımı geçerli değildir. Giriş koruması istenirse Cloudflare Access
sonradan **kod değişikliği olmadan** eklenebilir.

## Sonuçlar

- İçerik değişince artık **iki hedef** var: `ai.velzon.tr` (eğitim/MCP) ve `mulakat.velzon.tr`.
  Senkron: `content/` + `prompts/` rsync → `systemctl restart ai-ogretmen-mulakat`.
- Yerelde `./mvnw spring-boot:run` artık **çalıştırılmaz** (H2 tek yazarlı; iki ayrı ilerleme oluşur).
- Yeni sürüm dağıtımı: `./mvnw -DskipTests package` → `app.jar` kopyala → servisi yeniden başlat.

## Kapanış (2026-10-21)
Canlı sunucu kapatıldı; bu ADR'deki dağıtım adımları artık uygulanmaz. Uygulama yalnız yerelde
(127.0.0.1:8090) çalışır; `tools/deploy.sh` kaldırıldı (git geçmişinde duruyor).
