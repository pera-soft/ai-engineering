# ADR-0002: İçerik deposu olarak frontmatter'lı markdown

## Durum (Status)
Accepted — 2026-07-03

## Bağlam (Context)
12 haftalık müfredat (60 gün ders/görev + 12 quiz) bir yerde yaşamalı. AI öğretmen bu
içeriği prompt bağlamına enjekte edecek; kullanıcı içeriği elle düzenleyebilmeli.

## Karar (Decision)
Müfredat `content/` altında frontmatter'lı markdown dosyalarında tutulacak
(`hafta-NN/hafta.md` + `gun-N.md`). Kaynak `mimar-programi_1.html` içindeki PROGRAM
dizisinden tek seferlik script ile üretilir (`tools/html-to-content.mjs`).
Uygulama açılışta okur, bellekte tutar; hot-reload yok (yeniden başlatma yeterli).

## Değerlendirilen alternatifler
- **DB'de içerik** — sorgulanabilir ama ajan-okunabilirliği düşük, elle düzenleme ve
  versiyonlama zor. Reddedildi.
- **HTML'i doğrudan parse etmek (her açılışta)** — kaynak dosyaya kalıcı bağımlılık
  yaratır; markdown ara katmanı hem insan hem ajan için okunaklı. Reddedildi.

## Sonuçlar (Consequences)
- (+) Ajan-okunabilir, git ile versiyonlanır, elle düzeltilebilir.
- (+) AI prompt'larına ham markdown olarak enjekte edilir (dönüşüm gerekmez).
- (−) Frontmatter/quiz formatı için basit bir parser yazılıp test edilmeli.
- Kural: content/ yeniden üretimi kod değişikliğiyle aynı commit'te yapılmaz (AGENTS.md).
