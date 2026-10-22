# ADR-0015 — E-posta yerine ortak ekip kodu ile kayıt (Faz 9, 9e öncesi)

- **Durum:** Kabul edildi
- **Tarih:** 2026-10-22
- **Bağlam sahibi:** kullanıcı kararı (pera ekibi e-posta gönderimini istemedi) + uygulayıcı kararları
  (aşağıda işaretli)
- **İlişkili:** ADR-0012 (site profili), ADR-0013 (SMTP — bu ADR ile varsayılan olmaktan çıktı, kod duruyor)

## Bağlam

ADR-0012/0013 ile kayıt, `@pera.software` adresine giden doğrulama bağlantısıyla açılıyordu; "şifremi
unuttum" da e-postayla çalışıyordu. pera ekibi (30–60 kişi) sitenin **hiç e-posta göndermemesini**
istedi (SMTP hesabı ayırmak/işletmek istemiyor). Yine de kaydın yalnız ekip üyelerine açık kalması ve
şifresini unutanın kurtarılabilmesi gerekiyor.

## Karar

1. **Yeni ayar `app.site.kayit`** (`APP_SITE_KAYIT`): `ekip-kodu` (**varsayılan**) | `eposta` (Faz 9c
   akışının değişmemiş hâli). Bilinmeyen değerde site açılmaz.
2. **Ortak ekip kodu `app.site.ekip-kodu`** (`APP_SITE_EKIP_KODU`): `ekip-kodu` modunda yoksa, boşsa ya
   da kırpıldıktan sonra **12 karakterden kısaysa site AÇILMAZ** (fail-closed; Türkçe mesaj, değer
   mesaja/loga yazılmaz). Üretim önerisi `openssl rand -base64 18` (24 karakter, 144 bit).
   `SiteAyarlari.toString()` kodu maskeler.
3. **Kayıt** `POST /api/kimlik/kayit` `{email, sifre, ekipKodu}`:
   - Kod **önce** denetlenir — kırpılır, UTF-8 baytları `MessageDigest.isEqual` ile sabit zamanlı
     karşılaştırılır. Yanlış/eksik kod → 400 `{"hata":"Geçersiz istek","detay":"Ekip kodu hatalı. Kodu
     site sorumlusundan al."}`; koddan önce hiçbir kural/hesap bilgisi dönmez.
   - Sonra bugünkü kurallar: yalnız `@pera.software`, şifre ≥ 10 karakter (≤ 72 bayt), BCrypt.
   - Doğru kod → hesap **doğrulanmış + etkin** açılır; token yok, e-posta yok. Yanıt
     `{"durum":"kayit-tamam"}` → ön yüz `giris.html?kayit=1`'e gider.
   - **Mevcut hesap (eşitlik korunur):** her durumda aynı 200 `kayit-tamam` yanıtı; doğrulanmış hesapta
     hiçbir şey değişmez (şifre ele geçirilemez) — bugünkü e-posta modundaki davranışın aynısı.
     Doğrulanmamış (e-posta döneminden kalma) hesapta şifre güncellenir ve hesap doğrulanır
     *(uygulayıcı kararı: e-posta modunda da doğrulanmamış hesaba yeniden kayıt şifreyi günceller;
     ekip kodu modunda doğrulama adımı yok)*.
4. **E-posta yolu kapalı:** `ekip-kodu` modunda `EpostaGondericiSecimi` SMTP istemez
   (`app.site.eposta-zorunlu` yalnız `eposta` modunda anlamlıdır); bağlantıyı loglamayan, çağrılırsa
   hata fırlatan bir gönderici kurulur. `GET /api/kimlik/dogrula`, `POST /api/kimlik/dogrulama-yeniden`
   ve `POST /api/kimlik/sifremi-unuttum` **404** döner. `POST /api/kimlik/sifre-sifirla` **açık kalır**.
5. **Ön yüz bayrağı** *(uygulayıcı kararı)*: `/api/ozellikler` sitede kimlik ister; giriş/kayıt
   sayfaları için kimliksiz `GET /api/kimlik/ayarlar` → `{"kayit":"ekip-kodu"|"eposta"}` eklendi.
   Kayıt sayfası ekip kodu alanını gösterir; giriş sayfası "Şifremi unuttum" yerine "Şifreni
   unuttuysan site sorumlusuna yaz." gösterir. E-posta modunda arayüz eskisi gibidir.
6. **İşletmen şifre sıfırlama** — `tools/sifre-baglantisi.sh <email> [env-dosyasi]`: PostgreSQL'e
   (`psql`, servisle aynı `SPRING_DATASOURCE_*`) Java ile **birebir aynı** biçimde token yazar ve
   `${APP_SITE_TEMEL_URL}/sifre-sifirla.html?token=...` basar:
   token = 32 rastgele bayt, base64url dolgusuz; DB'ye yalnız `SHA-256(UTF-8)` küçük harf hex özeti;
   `tur = SIFRE_SIFIRLAMA`; `son_gecerlilik` = uygulamanın yerel saatiyle şimdi + 1 saat (kolon saat
   dilimsiz `TIMESTAMP`, JVM `Instant`'ı yerel saatle yazar → betik `date +%z` farkıyla aynı saati
   yazar; betik uygulamayla **aynı sunucuda** çalıştırılır); hesabın önceki sıfırlama token'ları silinir;
   tek kullanımlık (Java tüketir). Yalnız var olan, `etkin=true` ve doğrulanmış hesaba üretir; e-posta
   küçük harfe çevrilir; çıktıda sır olarak yalnız bağlantı vardır (DB şifresi/özet yazılmaz).
   Bağlantı kullanıcıya güvenli bir kanaldan elle iletilir.
7. **Kaba kuvvet** *(uygulayıcı kararı)*: kayda IP başına deneme sınırı **eklenmedi**. Var olan
   sayaçlar uymuyor: giriş kilidi hesap başınadır (kayıtta hesap yok), `EpostaHizSiniri` e-posta+tür
   başınadır ve belleği yalnız var olan hesaplarla sınırlı olduğu için rastgele IP anahtarlarıyla
   büyümeye açıktır. Yerine: kod ≥ 12 karakter zorunlu + rastgele üretim önerisi (144 bit) → çevrim içi
   tahmin pratikte imkânsız; yanlış kod denemeleri `WARN` loglanır (değersiz), nginx erişim logunda IP
   görünür. Gerekirse nginx `limit_req` ile `/api/kimlik/kayit`'e oran sınırı konabilir.

## Değerlendirilen alternatifler

- **E-postayı korumak (ADR-0013):** ekip istemedi. Kod silinmedi; `APP_SITE_KAYIT=eposta` ile döner.
- **Kişiye özel davet kodları/bağlantıları:** e-posta sahipliğini kanıtlamaya daha yakın ama her kişi
  için işletmen işi + yeni tablo/uç. 30–60 kişilik iç ekip için ortak kod yeterli görüldü.
- **Yönetici onaylı kayıt (bekleyen hesaplar):** yönetim arayüzü gerektirir; kapsam dışı.
- **IP başına kayıt sınırı (bellek içi):** §7 — yeni, sınırsız büyüyebilen yapı; ≥12 karakter kod ile
  gereksiz. Reddedildi.

## Sonuçlar

- (+) SMTP hesabı/işletmesi gerekmez; site e-posta göndermez. Kayıt anında giriş yapılabilir.
- (−) **E-posta sahipliği kanıtlanmaz:** kodu bilen herkes herhangi bir `@pera.software` adresiyle
  hesap açabilir (başkasının adresini önce kapmak dahil). Kabul edildi: kod yalnız ekip içinde
  paylaşılır; adresi kapılan kişi işletmene başvurur (hesap kapatılır/silinir ya da sıfırlama
  bağlantısı gerçek sahibine verilir).
- (−) **Kod sızarsa:** `APP_SITE_EKIP_KODU` değiştirilir + `systemctl restart`; **mevcut hesaplar
  etkilenmez** (kod yalnız kayıtta bakılır). Sızıntı döneminde açılmış yabancı hesaplar
  `SELECT email, olusturma FROM kullanici ORDER BY olusturma DESC` ile gözden geçirilip kapatılır.
- (−) "Şifremi unuttum" self-servis değil: kullanıcı site sorumlusuna yazar, sorumlu betikle 1 saatlik
  bağlantı üretip iletir (runbook §8).
- (=) Giriş, kilit, oturum düşürme, hesap kapatma, şifre sıfırlama ucu değişmedi; e-posta modu ve
  testleri aynen çalışır (`app.site.kayit=eposta`).
