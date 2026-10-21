# ADR-0001: Backend olarak Spring Boot + Spring AI seçimi

## Durum (Status)
Accepted — 2026-07-03

## Bağlam (Context)
Yerel çalışan bir "AI Öğretmen" uygulaması gerekiyor: müfredat sunumu, ilerleme takibi
ve arka planda Claude API ile değerlendirme/sohbet. Kullanıcı Java geliştiricisi ve
12 haftalık programının hedeflerinden biri Spring AI öğrenmek.

## Karar (Decision)
Java 17 + Spring Boot 3.5.x + Spring AI 1.1.x (Anthropic starter:
`spring-ai-starter-model-anthropic`) kullanacağız. Not: brief'te geçen
`spring-ai-anthropic-spring-boot-starter` adı Spring AI 1.0 öncesi eski addır.
Java 21 yerine 17: makinede kurulu olan sürüm; Boot 3.5 + Spring AI ile tam uyumlu,
yükseltme tek satır (`java.version`).

## Değerlendirilen alternatifler
- **Saf Anthropic Java SDK** — daha ince kontrol; ama Spring AI öğrenme hedefini
  karşılamıyor, prompt şablonu/starter kolaylıkları elle yazılırdı. Reddedildi.
- **Python FastAPI** — hızlı prototip; ama kullanıcının ana dili Java ve proje
  ekosistem uyumu hedefliyor. Reddedildi.

## Sonuçlar (Consequences)
- (+) Spring ekosistemi konvansiyonları (starter, ConfigurationProperties, Data JDBC)
  harness'lanabilirliği artırır; ArchUnit ile mimari mekanik zorlanır.
- (+) Spring AI ChatClient soyutlaması ile model kademeleme (ADR-0003) kolay.
- (−) Spring AI hızlı evrilen bir API; sürüm yükseltmelerinde breaking change riski —
  Claude çağrıları tek pakete (ai/) hapsedilerek yüzey daraltıldı.
