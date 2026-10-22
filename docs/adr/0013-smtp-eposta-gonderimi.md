# ADR-0013 — SMTP ile e-posta gönderimi: `spring-boot-starter-mail` (Faz 9c)

- **Durum:** Kabul edildi
- **Tarih:** 2026-10-22
- **Bağlam sahibi:** kullanıcı kararı (bağımlılık onayı 2026-10-22, pera'nın 3 mail hesabından biri
  gönderici) + uygulayıcı kararları (aşağıda işaretli)

> **Değişiklik notu (2026-10-22, ADR-0015):** pera ekibi e-posta gönderimini istemedi. Varsayılan kayıt
> yöntemi artık **ortak ekip kodu** (`APP_SITE_KAYIT=ekip-kodu`); bu ADR'deki SMTP akışı (doğrulama,
> yeniden gönder, şifremi unuttum) yalnız `APP_SITE_KAYIT=eposta` iken çalışır ve SMTP zorunluluğu
> (`app.site.eposta-zorunlu`) yalnız o modda geçerlidir. Kod ve testler korunuyor.

## Bağlam

`site` profilinde (ADR-0012) kayıt e-posta doğrulamasıyla açılıyor; 9a'da tek gönderici
`LogEpostaGonderici` idi — gizli, tek kullanımlık bağlantıyı uygulama loguna yazar. Canlıda bu kabul
edilemez (log'a erişen hesap doğrular/şifre sıfırlar) ve kullanıcı bağlantıyı hiç görmez. Ayrıca
"şifremi unuttum" akışı da aynı gönderim kapısına ihtiyaç duyuyor.

## Karar

1. **Bağımlılık:** `org.springframework.boot:spring-boot-starter-mail` — sürümü Spring Boot parent
   yönetir (açık sürüm yok). Jakarta Mail + Spring `JavaMailSender` getirir; başka yeni bağımlılık YOK.
2. **Ayarlar yalnız ortam değişkeninden:** `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT` (varsayılan 587),
   `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`; STARTTLS varsayılan AÇIK ve ZORUNLU
   (`APP_SITE_SMTP_STARTTLS=false` yalnız yerel deneme için), bağlantı/okuma/yazma zaman aşımı 10 sn.
   Gönderen `app.site.gonderici` (`APP_SITE_GONDERICI`, varsayılan `AI Öğretmen <noreply@pera.software>`).
   Şifre hiçbir yerde loglanmaz.
3. **Seçim tek yerde** *(uygulayıcı kararı)*: `service/EpostaGondericiSecimi` (`@Profile("site")`)
   `spring.mail.host` doluysa `SmtpEpostaGonderici`, boşsa `LogEpostaGonderici` üretir.
   **`app.site.eposta-zorunlu`** (`APP_SITE_EPOSTA_ZORUNLU`, `application-site.yml`'de varsayılan
   **true**) iken SMTP yoksa uygulama **açılmaz** (`IllegalStateException`) → canlı sessizce log'a
   bağlantı sızdıramaz. Yerel duman testi bunu `false` yapar.
4. **Gönderim isteği bloklamaz/bozmaz** *(uygulayıcı kararı)*: SMTP gönderimi ayrı, sınırlı kuyruklu
   (100) tek iş parçacığında yapılır; hata (ve kuyruk taşması) sırsız loglanır (alıcı + tür + hata
   sınıfı; bağlantı ASLA), HTTP yanıtı yine nötrdür. Yan etki: SMTP gecikmesi "hesap var/yok"
   zamanlama farkı yaratmaz. Kayıt SMTP hatasında da bekleyen hesabı açar ("yeniden gönder" ile
   kurtarılır).
5. **İçerik:** Türkçe, `multipart/alternative` (düz metin + basit HTML), şablonlar
   `src/main/resources/eposta/` altında (`prompts/` yalnız AI prompt'larıdır). Bağlantı kökü
   `app.site.temel-url` — Host başlığından türetilmez (ADR-0012 §5). HTML'e giren değerler kaçışlanır.
6. **Şifremi unuttum:** yalnız doğrulanmış + etkin hesaba bağlantı gider; yanıt her zaman nötr.
   Token 32 bayt `SecureRandom`, DB'de SHA-256 özeti, 1 saat, tek kullanımlık, yenisi eskileri siler
   (`kullanici_token.tur = SIFRE_SIFIRLAMA`). Başarılı sıfırlamada şifre güncellenir, hatalı deneme
   sayacı + kilit temizlenir, kullanıcının BÜTÜN oturumları düşürülür (`SessionRegistry`).
7. **Gönderim hız sınırı** *(uygulayıcı kararı)*: aynı adrese tür başına saatte en çok 3 e-posta
   (doğrulama: kayıt + "yeniden gönder" ortak sayılır; sıfırlama ayrı). Sayaç **bellek içi** ve yalnız
   var olan hesaplar için tutulur (boyutu hesap sayısıyla sınırlı). Yeniden başlatma sayacı sıfırlar —
   amaç posta seli önlemek, tek örnekli dağıtımda yeterli; ADR-0012 §6'daki kilit sayacından farkı:
   kilit güvenlik sınırıdır, bu yalnız nezaket/maliyet sınırı.

## Değerlendirilen alternatifler
- **Yalnız log (bugünkü hâl):** canlıda bağlantı kullanıcıya ulaşmaz, log'a erişen hesap ele geçirir.
  Reddedildi (yalnız `eposta-zorunlu=false` ile yerel denemede kalır).
- **Harici HTTP API (SendGrid/Mailgun/SES SDK'sı):** yeni SDK bağımlılığı + üçüncü taraf hesabı ve
  veri işleyen (KVKK), pera'nın elinde zaten SMTP hesabı var. Reddedildi; gerekirse bu sağlayıcıların
  SMTP uç noktası aynı gerçeklemeyle kullanılabilir.
- **Elle `jakarta.mail` (starter'sız):** yine aynı kütüphane; Boot'un ayar bağlama ve otomatik
  yapılandırmasını yeniden yazmak olurdu. Reddedildi.
- **Senkron gönderim:** SMTP yavaş/kapalıyken kayıt isteği asılır ve zamanlama farkı hesap varlığını
  sızdırır. Reddedildi.
- **Hız sınırı DB'de:** yeni tablo + temizlik işi; tek örnekte bellek içi yeterli. Reddedildi.

## Sonuçlar
- (+) Canlı (9e) gerçek SMTP ile çıkar; SMTP'siz `site` açılmaz. Yerel (profilsiz) mod etkilenmez —
  `spring.mail.host` verilmedikçe Boot `JavaMailSender` bile kurmaz.
- (+) Testler gerçek ağa çıkmaz: `SmtpEpostaGondericiTest` sahte `JavaMailSender` ile; GreenMail vb.
  eklenmedi.
- (−) Kuyruktaki e-postalar uygulama kapanırken kaybolabilir (kapanışta en çok 10 sn beklenir);
  kullanıcı "yeniden gönder"/"şifremi unuttum" ile tekrar ister.
- (−) Hız sınırı yeniden başlatmada sıfırlanır (bilinçli kabul, §7).
