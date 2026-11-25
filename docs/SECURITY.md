# SECURITY.md — Tehdit modeli ve alınan önlemler

## Tehdit modeli
Yerel mod (`local`): yerel, tek kullanıcılı eğitim aracı; `127.0.0.1:8090`'a bağlanır, internete açılmaz.
Ağ üzerinden doğrudan saldırı kapsam dışı. GEÇERLİ tehditler: kullanıcının tarayıcısındaki
başka bir sekmenin localhost'a istek göndermesi (CSRF), kullanıcı/AI verisinin arayüzde
işlenmesi (XSS) ve AI çıktılarının kalıcı dosyalara yazılması (hafıza zehirleme).

## Önlemler (2026-07-03 code review döngüsünde eklendi)
| Tehdit | Önlem | Yer |
|---|---|---|
| Siteler-arası istek (CSRF) | `Sec-Fetch-Site: cross-site` ve yabancı `Origin` → 403; izinli origin = `127.0.0.1`/`localhost` + sunucunun soketten okunan gerçek portu (Host başlığı değil) | `web/CsrfKoruma` |
| Stored XSS (kitap adı) | Kullanıcı verisi DOM'a `textContent` ile basılır; CSP `default-src 'self'` | `app.js`, `index.html` |
| Yan etkili GET | Rapor yenileme POST'a taşındı (`/api/rapor/{h}/yenile`) | `OgretmenController` |
| Asılı AI çağrısı | Backend connect 5sn / read 120sn; frontend AbortController 180sn | `AiYapilandirma`, `app.js` |
| Dev girdi → maliyet/context aşımı | Metin girdileri 20K, kitap adı 200 karakter; anlamlı 400 | controller'lar |
| Upload | 10MB sınırı (yml + controller), MIME beyaz listesi | `KitapController` |
| Path traversal | Slug'lama `[^a-z0-9]+`; dosya adları `%02d` formatlı int | `KitapService` vd. |
| SQL injection | Yalnız türetilmiş/parametresiz sorgular | repo katmanı |
| Sır | Anahtar yalnız env→property→bean; değeri asla loglanmaz | `AiYapilandirma` |

## `site` profili (herkese açık, girişsiz — Faz 10, ADR-0017)
Yukarıdaki yerel model `local` için geçerlidir. `site` profili internete (nginx/TLS arkasında) açılır; giriş yoktur.
| Tehdit | Önlem | Yer |
|---|---|---|
| Ziyaretçi kimliği | HttpOnly `aio_ziyaretci` çerezi (22 karakter, 365 gün); biçimi geçersiz çerez yok sayılıp yenisi üretilir; `kullanici_id = "z-" + kimlik`, hiçbir uç dışarıdan `kullanici_id` almaz | `ZiyaretciCerezi`, `ZiyaretciKullaniciBaglami` |
| CSRF | Korunur: `XSRF-TOKEN` çerezi + Origin kontrolü; oturum yok (STATELESS, `JSESSIONID` yok) | `SiteGuvenlikYapilandirma` |
| Kötüye kullanım | Bellek içi hız sınırları (tek JVM): yazma 60/dk/IP, sohbet 20/dk/ziyaretçi, geri-yükle 10/saat/IP, yeni ziyaretçi 20/saat/IP (varsayılan; `APP_SITE_HIZ_*` ile ayarlanır); 429 + `Retry-After` | `HizSiniriInterceptor` |
| Ziyaretçi anahtarı sızıntısı | Başlık `X-OpenRouter-Anahtar`'ı ön yüz yalnız `/api/sohbet`'e gönderir; sunucu başka hiçbir yerde saklamaz/loglamaz (bozuk biçimli başlık her uçta 400 döner); desen doğrulanır; istek başına ChatModel, önbellek yok; DB/log/istisna/dosyaya yazılmaz, ham sağlayıcı yanıtı istisna cause'unda taşınmaz; sitede sunucu anahtarı hiç okunmaz | `ai/` paketi |
| Maliyet / yetkisiz AI | Sohbet dışı AI uçları sitede 404; maliyet ziyaretçinin kendi anahtarına yazılır | `web/` |
| Terk edilmiş veri | Her gece 03:30, 180 gün gelmeyen ziyaretçinin tüm verisi silinir | temizlik işi |
| XSS | Güvenilmeyen metin (AI çıktısı, kullanıcı girdisi) `textContent` ile basılır; CSP korunur | `app.js` |
- Ziyaretçi kodu (ilerleme kodu) bir sır gibi davranır: bilen, ilerlemeyi geri yükleyebilir; hesap/şifre yoktur.
- Bilinçli sınır: hız sınırları tek JVM'de bellektedir (çok pod'a çıkılırsa paylaşımlı depo gerekir).

## Bilinçli kabul edilen riskler
- **Hafıza/kitap notu zehirleme (dolaylı prompt injection):** AI'nin ürettiği hafıza ve
  kitap özetleri sonraki prompt'lara girer. Tek kullanıcı + ayrıcalıksız asistan bağlamında
  zarar sınırlı (yanlış yönlendirme); dosyalar `data/` altında elle düzeltilebilir/silinebilir.
- **MIME magic-byte doğrulaması yok:** Baytlar yerelde parse edilmeden sağlayıcıya gider;
  bilinen image-parsing zafiyet zinciri yok.
- **500 yanıtında `detay: e.getMessage()`:** Yerel araçta hata ayıklanabilirlik tercih edildi.
