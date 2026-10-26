# ADR-0016 — Araç idempotency'si, AI bağlam bütçesi ve AI ölçümü kararları

- **Durum:** Kabul edildi
- **Tarih:** 2026-10-26
- **Bağlam sahibi:** uygulayıcı kararları (kullanıcı kararı uygulayıcıya devretti, 2026-10-26)
- **İlişkili:** ADR-0003 (model kademeleme), ADR-0004 (OpenRouter), ADR-0007 (MCP sunucusu),
  ADR-0012 (site profili; kota yok)

## Bağlam

AI Engineering from Scratch içerik turu (2026-10-25) sırasında metinler yazılırken kodun gerçek
davranışı okundu ve dört açık çıktı (`content/kaynaklar/arac-cagrisi-guvenilirligi.md` §8,
`llm-uretim-yolu.md` §6–§8):

1. `gorev_isaretle` / `POST /api/gorev/toggle` durumu **tersine çeviriyordu**: zaman aşımı sonrası
   tekrar denenen çağrı işareti geri alıyor, eşzamanlı iki çağrıda güncelleme kayboluyordu.
2. `statik_quiz_kaydet` her çağrıda yeni satır ekliyor; tekrar denenen çağrı çift satır üretir.
3. AI bağlam bütçesi (`KaynakService`, 14 000 karakter, kaynak başı 3 000) hafta başına ~4 kaynak
   özüne yetiyor; çok etiketli haftalarda bazı kaynaklar AI'a ulaşmıyor.
4. `ai_kullanim` çağrı süresini ve sağlayıcının cevapta bildirdiği modeli tutmuyordu; uygulamada
   yedek model zinciri yok.

## Karar

1. **Görev işaretleme idempotent "hedef durumu ayarla" oldu.** Servis kayıt yoksa oluşturur, varsa
   durumu ayarlar; aynı çağrı N kez = aynı sonuç, eşzamanlı aynı hedef çağrıları güvenli. Arayüz
   `POST /api/gorev/durum` `{hafta, gun, gorevIndex, tamamlandi}`; MCP aracı `gorev_isaretle` opsiyonel
   `hedef_durum` alır (yoksa `true`). Toggle ucu ve metodu kaldırıldı.
2. **`statik_quiz_kaydet` değişmedi — kabul edilen risk.** `quiz_sonuc` bilinçli olarak yalnız-ekle
   bir deneme günlüğüdür (her deneme bir satır). Çift satır yalnız MCP istemcisinin aynı çağrıyı
   tekrar göndermesiyle oluşur; MCP sunucuları kapalı (2026-10-21), istemci yalnız yerel; etkisi
   istatistikte bir fazla deneme. Önlemek bir idempotency anahtarı kolonu + araç sözleşmesi
   değişikliği ister — kazançtan pahalı. **Yeniden açma koşulu:** MCP sunucusu yeniden uzakta
   (`mcp-http`) çalıştırılırsa ya da quiz denemesi bir değerlendirme/sıralamaya girerse.
3. **AI bağlam bütçesi büyütülmedi.** Sıra öncelikli yapıldı (haftanın kendi metni → az etiketli →
   çok etiketli); bütçe artık en özel kaynaklara gider. Bütçeyi büyütmek her sohbet çağrısının girdi
   token'ını (maliyet + ilk token gecikmesi) kalıcı olarak artırır; kesilen kaynaklar okuma sayfasında
   tam durur. **Yeniden açma koşulu:** bir haftanın kendi metni ya da tek etiketli bir kaynağı bütçe
   dışında kalırsa.
4. **`ai_kullanim` süre (`sure_ms`, yeniden denemeler dahil duvar saati) ve cevaptaki model
   (`yanit_model`) kaydeder** (eklemeli şema, eski satırlar boş). **Uygulama seviyesinde yedek model
   zinciri kurulmadı:** OpenRouter aynı model için sağlayıcılar arası yedeği varsayılan yapar
   (`allow_fallbacks: true`); başka bir modele sessizce düşmek quiz/değerlendirme JSON biçimini ve
   kaliteyi değiştirir, ve bunu artık `yanit_model` ile görebiliyoruz. **Yeniden açma koşulu:**
   `ai_kullanim` verisi tek modelin süreğen kesintisini gösterirse.

## Sonuçlar

- Görev işaretleme tekrar denemeye ve yarışa dayanıklı; istemci API'si değişti (README API tablosu).
- İki karar (2, 3) borç değil, yeniden açma koşulu yazılı kabul edilen takaslardır.
- Gecikme ve yedeğe geçiş artık veriden okunabilir; özet/pano bilinçli olarak eklenmedi.
