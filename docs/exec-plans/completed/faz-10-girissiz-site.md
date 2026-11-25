# ExecPlan: Faz 10 — girişsiz site + "kendi OpenRouter anahtarın"

> Tamamlandı (2026-11-25). Karar kaydı: [ADR-0017](../../adr/0017-girissiz-site-ve-kendi-anahtarin.md).
> Tasarım: [spec](../../superpowers/specs/2026-11-25-girissiz-site-kendi-anahtarin-design.md) ·
> Plan: [uygulama planı](../../superpowers/plans/2026-11-25-girissiz-site-kendi-anahtarin.md)

## Neden önemli (kullanıcı bakışı)
Faz 9 sitesi yalnız Pera ekibine açıktı (`@pera.software`, ekip kodu, SMTP, şirketin tek OpenRouter anahtarı).
Kullanıcı bunu istemedi: site **herkese açık** bir eğitim sitesi olacak; giriş/kayıt hiç olmayacak; AI yalnız
isteğe bağlı sohbet botu olacak ve onu kullanan **kendi OpenRouter anahtarını** girecek.

## Kararlar (kullanıcının kararları)
1. Giriş/kayıt yok; Pera'ya özel her şey kalktı.
2. İlerleme sunucuda, çerez kimliğiyle; "ilerleme kodu" ile başka tarayıcıya taşınır.
3. AI anahtarı yalnız sohbet botu için; rapor/AI değerlendirme/AI quiz/örnek/atölye değerlendirmesi sitede yok (yerelde var).
4. Anahtar sohbet paneline yapıştırılır; sunucuda hiçbir yere yazılmaz.
5. Anahtarsız ziyaretçi görevleri kendisi işaretler; anahtarlı AI öğretmenle geçer.

## Yapılanlar (9 görev, her biri ayrı incelendi)
1. Ziyaretçi çerezi `aio_ziyaretci` + girişsiz güvenlik zinciri (permitAll, STATELESS, CSRF korunur).
2. Kimlik sistemi ve Pera izleri silindi (KimlikController/Service, e-posta, giriş/kayıt sayfaları, `spring-boot-starter-mail`).
3. `ziyaretci` tablosu + ilerleme kodu (`GET /api/ziyaretci/kod`, `POST /api/ziyaretci/geri-yukle`).
4. Hız sınırları: yazma 60/dk/IP, sohbet 20/dk/ziyaretçi, geri-yükle 10/saat/IP, yeni ziyaretçi 20/saat/IP.
5. Sitede sohbet dışı AI uçları 404; `/api/ozellikler` = {mulakat, site, sunucuAi}.
6. Kendi anahtarınla AI istemcisi: `X-OpenRouter-Anahtar`, istek başına ChatModel, önbellek yok; 400/401/402/429.
7. Terk edilmiş ziyaretçi verisinin temizliği: her gece 03:30, 180 gün gelmeyen ziyaretçi.
8. Ön yüz: anahtar kartı, "Okudum/Yaptım/Tartıştım ✓" ile kendin işaretle, ilerleme kodu.
9. Belgeler, `deploy/` şablonları, son doğrulama.

## Doğrulama
- `./mvnw clean verify` YEŞİL: 71 test sınıfında 427 test, 0 hata, 5 atlanan (PostgreSQL opt-in).
- `SitePostgresEntegrasyonTest` yerel PostgreSQL 14.15'te 5/5 geçti (atılabilir DB, sonra silindi); son düzeltmelerden sonra `20ee07a` üzerinde yeniden 5/5.
- Site headless tarayıcıda uçtan uca denendi: anahtarsız günü bitirme, sahte anahtar 401, ilerleme kodu geri yükleme.
- Herkese açık repo `pera-soft/ai-engineering` main = `5832baf` (`tools/yayin-hazirla.sh`, geçmişsiz yayın dalı).

## İncelemelerin yakaladığı 4 hata (düzeltildi)
1. Hız sınırı haritası sonsuz büyüyordu (bayat anahtar) + yüzde-kodlu yol karşılaştırması → süpürme + çözülmüş yol.
2. Spring Data JDBC deposu sınıf düzeyinde `@Transactional(readOnly=true)`; işlem dışında `deleteBy…` PostgreSQL'de
   patlar, H2 göstermez → temizlik ziyaretçi başına tek okuma-yazma işleminde.
3. Eski anahtarla giden isteğe geç gelen 401 yeni anahtarı siliyordu → karşılaştır-ve-sil.
4. Yayın sızıntı taraması sahte test anahtarlarına takıldı → test anahtarları yeniden biçimlendi, kontroller tam anahtarı arar.

## Kapanan küçük bulgular ve son inceleme
- Görev incelemelerinin ertelediği küçük bulgular (tech-debt #10, #12–#17) `5a0966d` ile kapandı; #11 (JS davranış
  testi) yeni bağımlılık gerektirdiği için onay bekleyen **karar** olarak duruyor.
- Bütün dalın son incelemesi (2026-11-25): kritik/önemli bulgu yok, "merge'e hazır". 9 küçük bulgu `a87528a`,
  yeniden incelemenin 5 kalıntısı `20ee07a` ile kapandı: imleçli (keyset) temizlik sayfalaması, silmede yaş yeniden
  kontrolü, GET'te `son_gorulme` tazeleme, ayarlanabilir hız sınırları (`APP_SITE_HIZ_*`), 429'da `Retry-After`.

## Sıradakiler
- Girişsiz siteyi bir sunucuya kurmak: sunucu + alan adı + PostgreSQL ([kurulum-site.md](../../kurulum-site.md), `deploy/`);
  şirket OpenRouter anahtarı / ekip kodu / SMTP ARTIK GEREKMEZ. Tek NAT arkasında kalabalık bir grup varsa
  `APP_SITE_HIZ_*` sınırlarını kurulumda yükselt.
- İstenirse: "OpenRouter ile bağlan" (OAuth PKCE) düğmesi (ADR-0017'de sonraya bırakıldı); JS davranış testleri
  (tech-debt #11 kararı — yeni bağımlılık, ADR + onay).
