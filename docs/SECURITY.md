# SECURITY.md — Tehdit modeli ve alınan önlemler

## Tehdit modeli
Yerel, tek kullanıcılı eğitim aracı; `127.0.0.1:8090`'a bağlanır, internete açılmaz.
Ağ üzerinden doğrudan saldırı kapsam dışı. GEÇERLİ tehditler: kullanıcının tarayıcısındaki
başka bir sekmenin localhost'a istek göndermesi (CSRF), kullanıcı/AI verisinin arayüzde
işlenmesi (XSS) ve AI çıktılarının kalıcı dosyalara yazılması (hafıza zehirleme).

## Önlemler (2026-07-03 code review döngüsünde eklendi)
| Tehdit | Önlem | Yer |
|---|---|---|
| Siteler-arası istek (CSRF) | `Sec-Fetch-Site: cross-site` ve yabancı `Origin` → 403 | `web/CsrfKoruma` |
| Stored XSS (kitap adı) | Kullanıcı verisi DOM'a `textContent` ile basılır; CSP `default-src 'self'` | `app.js`, `index.html` |
| Yan etkili GET | Rapor yenileme POST'a taşındı (`/api/rapor/{h}/yenile`) | `OgretmenController` |
| Asılı AI çağrısı | Backend connect 10sn / read 150sn; frontend AbortController 180sn | `AiYapilandirma`, `app.js` |
| Dev girdi → maliyet/context aşımı | Metin girdileri 20K, kitap adı 200 karakter; anlamlı 400 | controller'lar |
| Upload | 10MB sınırı (yml + controller), MIME beyaz listesi | `KitapController` |
| Path traversal | Slug'lama `[^a-z0-9]+`; dosya adları `%02d` formatlı int | `KitapService` vd. |
| SQL injection | Yalnız türetilmiş/parametresiz sorgular | repo katmanı |
| Sır | Anahtar yalnız env→property→bean; değeri asla loglanmaz | `AiYapilandirma` |

## Bilinçli kabul edilen riskler
- **Hafıza/kitap notu zehirleme (dolaylı prompt injection):** AI'nin ürettiği hafıza ve
  kitap özetleri sonraki prompt'lara girer. Tek kullanıcı + ayrıcalıksız asistan bağlamında
  zarar sınırlı (yanlış yönlendirme); dosyalar `data/` altında elle düzeltilebilir/silinebilir.
- **MIME magic-byte doğrulaması yok:** Baytlar yerelde parse edilmeden sağlayıcıya gider;
  bilinen image-parsing zafiyet zinciri yok.
- **500 yanıtında `detay: e.getMessage()`:** Yerel araçta hata ayıklanabilirlik tercih edildi.
