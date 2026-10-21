# ADR-0011 — Mülakat modülü varsayılan kapalı (özellik bayrağı)

- **Durum:** Kabul edildi
- **Tarih:** 2026-10-21
- **Bağlam sahibi:** kullanıcı kararı

## Bağlam

Mülakat hazırlık modülü (Faz 1; ADR-0010) şu an kullanılmıyor; `mulakat.velzon.tr` kapatıldı.
Kullanıcı modülün arayüzde ve arka uçta tamamen kapalı olmasını, ama kodun ve `content/mulakat/`
içeriğinin **silinmeden** repoda kalmasını istedi (ileride yeniden açılabilir).

## Karar

`app.mulakat.etkin` özelliği (`AppProperties.Mulakat`), varsayılan **false**.
Açmak için: `app.mulakat.etkin=true` (ör. `APP_MULAKAT_ETKIN=true ./mvnw spring-boot:run`).

- **Arka uç:** `@MulakatEtkin` (= `@ConditionalOnProperty(app.mulakat.etkin=true)`) şu bean'lere
  konur: `MulakatController`, `MulakatService`, `MulakatAciklamaService`, `MulakatRepo`. Kapalıyken
  `content/mulakat/` hiç okunmaz, `/api/mulakat/**` kayıtlı değildir → 404 (HataYonetimi'nin
  NoResourceFound eşlemesi). MCP tarafında mülakat aracı/kaynağı zaten yoktu.
- **Bilinçli istisna:** `MulakatCevapRepo` (Spring Data JDBC arayüzü) ve `mulakat_cevap` tablosu
  koşulsuz kalır — `SifirlamaService` onu kullanır; boş tablo zararsızdır, sıfırlama kodu dallanmaz.
- **Arayüz:** statik `index.html` sunucuda işlenmediği (CSP: yalnız `'self'` betik) için bayrak
  `GET /api/ozellikler` → `{"mulakat": bool}` ucuyla okunur. Sol menüdeki Mülakat bölümü
  `hidden` başlar; `mulakat.js` bayrak false ise hiçbir istek atmadan çıkar; karşılama
  sayfasındaki Mülakat kartı/metni gösterilmez. Uç hata verirse güvenli varsayılan: kapalı.

## Sonuçlar

- Bayrak `true` iken davranış öncekiyle birebir aynıdır (testler bayrak açıkken koşar).
- Canlı dağıtımlarda modül istenirse servis ortamına `APP_MULAKAT_ETKIN=true` eklenmelidir;
  aksi halde bir sonraki dağıtımda Mülakat görünmez.
- Testler: `MulakatBayragiTest` (kapalı: bean yok, uçlar 404, sıfırlama çalışır; açık: uç 200).
