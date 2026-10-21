# ADR-0003: Model kademeleme politikası

## Durum (Status)
Accepted — 2026-07-03

## Bağlam (Context)
API kullanımı kullandıkça ödemeli. Her iş için en pahalı modeli kullanmak gereksiz;
maliyet mühendisliği programın öğrenme hedeflerinden biri.

## Karar (Decision)
İki kademe, `application.yml`'de property olarak (koda gömülmez):

| Property | Model | Kullanım | max_tokens |
|---|---|---|---|
> Kanonik kimlikler `application.yml`'de (OpenRouter, ADR-0004): `anthropic/claude-sonnet-4.6` / `anthropic/claude-haiku-4.5`. Aşağıdaki tablo bunlara atıfta bulunur.

| `app.ai.model.guclu` | `anthropic/claude-sonnet-4.6` | değerlendirme (2000), sohbet (1500), haftalık rapor (3000), sayfa fotoğrafı OCR (4000) | işe göre |
| `app.ai.model.hizli` | `anthropic/claude-haiku-4.5` | hafıza güncelleme (2000), kitap notu işleme (2000) | 2000 |
| `app.ai.model.hizli` | `anthropic/claude-haiku-4.5` | quiz üretimi — İSTİSNA: 10 soruya kadar seçenek+açıklama sığması için | 3000 |

Her AI çağrısının token kullanımı loglanır (Spring AI usage metadata);
`GET /api/kullanim` toplamı gösterir.

## Değerlendirilen alternatifler
- **Tek model (sonnet)** — basit ama quiz üretimi gibi mekanik işlerde ~3x maliyet. Reddedildi.
- **`claude-sonnet-5`** (daha yeni Sonnet) — geçerli seçenek; tanıtım fiyatıyla daha ucuz
  olabilir. Property olduğu için tek satırla geçilebilir; şimdilik brief'teki 4-6 korundu.

## Sonuçlar (Consequences)
- (+) Maliyet görünür ve yönetilebilir; model değişimi konfigürasyonla.
- (−) İki modelin çıktı kalitesi farkı quiz üretiminde izlenmeli — kalite düşerse
  quiz üretimi güçlü modele taşınır (tech-debt-tracker'a izleme notu).
