# ADR-0004: Model erişimi OpenRouter üzerinden

## Durum (Status)
Accepted — 2026-07-03

## Bağlam (Context)
Faz 2'ye kadar doğrudan Anthropic API (spring-ai-starter-model-anthropic) hedefleniyordu.
Kullanıcı, model erişimini OpenRouter (openrouter.ai) üzerinden yapmak istedi —
tek anahtarla çok sağlayıcı, kullanım paneli ve harcama limiti imkânı.

## Karar (Decision)
OpenRouter'ın OpenAI-uyumlu API'sini kullanacağız: `spring-ai-starter-model-openai`
+ `base-url: https://openrouter.ai/api` + `OPENROUTER_API_KEY` ortam değişkeni.
Model kimlikleri OpenRouter formatında ve application.yml'de property:
`anthropic/claude-sonnet-4.6` (guclu), `anthropic/claude-haiku-4.5` (hizli).
ChatModel bean'i yine yalnızca ai/AiYapilandirma'da, anahtar doluysa kurulur.

## Değerlendirilen alternatifler
- **Doğrudan Anthropic API** — resmi yol, Anthropic'e özgü özellikler (thinking vb.)
  eksiksiz; ama kullanıcı OpenRouter istedi. Geri dönüş kolay: bu ADR'yi supersede et,
  starter'ı ve base-url'ü değiştir — OgretmenAiClient arayüzü sayesinde servisler değişmez.

## Sonuçlar (Consequences)
- (+) Değişiklik yalnız ai/ paketi + config'e dokundu — katman mimarisi işini yaptı.
- (+) Tek panelden maliyet takibi; model değiştirmek property değişikliği.
- (−) Anthropic'e özgü gelişmiş özellikler (adaptive thinking parametresi vb.) OpenAI-uyumlu
  yüzeyde sınırlı; şu anki kullanım (chat completions) için yeterli.
- (−) Araya bir sağlayıcı daha girdi (gecikme + kullanım koşulları); kişisel eğitim
  aracı için kabul edilebilir.
