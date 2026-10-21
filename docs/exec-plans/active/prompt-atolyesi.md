# ExecPlan: Prompt Atölyesi — interaktif prompt teknikleri laboratuvarı

> Yaşayan belge. Yalnızca bu belgeden işi yeniden başlatmak mümkün olmalı.
> Kaynak: kullanıcı isteği (2026-07-15) — "prompt yazma teknikleri net öğretilmeli, 50-60 farklı
> teknik, AI Öğretmen ile deneme-yanılma". Brainstorming kararları aşağıda.

## Neden önemli (kullanıcı bakışı)
Öğrenci 12 hafta boyunca AI Öğretmen ile konuşuyor, prompt yazıyor — ama bu beceri hiçbir yerde
**sistematik** öğretilmiyor (sadece Hafta 7 Gün 4'e sıkışmış). Atölye, öğrencinin **serbestçe**
(kilit yok, sıra yok) girip prompt tekniklerini tek tek **deneyerek** öğrendiği bir laboratuvar
olur: bir teknik oku → kendi prompt'unu yaz → ideal çözümle kıyasla → istersen AI Öğretmen'e
değerlendirt. Baştan erişilebilir olması kritik: prompt becerisi Hafta 1'den itibaren gerekli.

## Brainstorming kararları (2026-07-15, kullanıcı onaylı)
1. **Konum:** Bağımsız Atölye — sol menüde ayrı, **kilitsiz** görünüm (yeni hafta DEĞİL; numaralandırma/
   kilit mantığına dokunmaz).
2. **Değerlendirme:** **Karma** — "İdeal çözümü göster" her alıştırmada statik (JS toggle, TOKEN YOK,
   sınırsız pratik). "AI Öğretmen değerlendirsin" butonu ise **HER alıştırmada** görünür (kullanıcı
   revizyonu 2026-07-15; başlangıçta yalnız kategori başına 1-2 planlanmıştı). Buton yalnız API anahtarı
   varken (aiAktif) görünür. İçerikteki `ai:true/false` alanı korunur (ileride "önerilen" işareti için).
3. **Odak:** **Karma** — teknik evrensel, alıştırma örnekleri yazılıma özgü (kod yazdır, spec, ajan
   talimatı, code review).
4. **Teslim:** **Aşamalı** — önce mekanik + 2 kategori uçtan uca çalışsın (Faz A), doğrulanınca kalan
   kategoriler içerikle doldurulsun (Faz B).

## Mimari kararlar
- **Katman yönü korunur** (types → config → repo → service → ai → web; ArchUnit zorlar). Yeni AI
  çağrısı YALNIZ ai/ paketinden; prompt şablonu prompts/ altından.
- **İçerik = veri, kod değil:** `content/prompt-atolyesi/NN-<slug>.md` — frontmatter (slug, baslik,
  aciklama) + gövdede yapılandırılmış alıştırma blokları. `AtolyeRepo` başlangıçta okur, bellekte
  `List<AtolyeKategori>` tutar (ContentRepo deseni). tools/html-to-content.mjs bunlara DOKUNMAZ.
- **types/** (bağımsız record'lar):
  - `AtolyeKategori(String slug, String baslik, String aciklama, List<AtolyeAlistirma> alistirmalar)`
  - `AtolyeAlistirma(String id, String teknik, String gorev, String kotuOrnek, String ipucu,
    String idealCozum, String neden, boolean aiDegerlendir)`
- **service/** `AtolyeService`:
  - `kategoriler()` → tüm atölye ağacı (idealCozum/neden dahil — istemcide JS ile spoiler gizlenir).
  - `promptDegerlendir(String alistirmaId, String ogrenciPrompt)` → OgretmenAiClient (ai/) +
    yeni `prompts/prompt-degerlendirme.st`. Rubrik: (a) görevi karşılıyor mu, (b) tekniği uygulamış
    mı, (c) net/bağlam/format, (d) somut iyileştirme. JSON sözleşmesi mevcut JsonCevapParser ile
    (bozuk JSON → 1 düzeltme → yine bozuksa ham metin). Token AI_KULLANIM'a yazılır; max_tokens ~800.
- **web/** `AtolyeController`:
  - `GET /api/atolye` → kategoriler.
  - `POST /api/atolye/degerlendir {alistirmaId, ogrenciPrompt}` → değerlendirme sonucu (JSON).
  - Anahtar yoksa (OgretmenAiClient.DevreDisi) değerlendirme 503 + net mesaj; statik akış çalışmaya
    devam eder.
- **Frontend** (static/, class adları korunur, offline/CSP):
  - Sol menüde "🧪 Prompt Atölyesi" satırı (Başlangıç Kılavuzu'nun altında, kilitsiz).
  - Görünüm modu: `durum.gorunum = "program" | "atolye"`. Atölye satırı → orta paneli atölye
    görünümüne çevirir (kategori sekmeleri + alıştırma kartları); sol menü haritası kalır.
  - Alıştırma kartı: teknik + görev + (kötü örnek) + `<textarea>` + "İdeal çözümü göster" (JS toggle
    → idealCozum + neden) + `aiDegerlendir` ise "AI Öğretmen değerlendirsin" (POST → sonuç; aiAktif
    değilse buton gizli).

## İçerik — 7 kategori, ~50 alıştırma (Faz B'de 2026 güncellik araştırmalı)
1. **Temel yapı** — net görev, bağlam verme, rol/persona, çıktı formatı, "ne yapma" (negatif), few-shot
2. **Muhakeme** — zincirleme düşünce, önce-planla, adım-adım böl, kendini denetle, rubrik verme
   (2026 nüansı: reasoning modellerinde zincirleme düşüncenin ne zaman gereksizleştiği)
3. **Yapılandırma** — bölümleme (markdown/XML), sıralama, uzun bağlamda "ortada kaybolma", çıktı iskeleti
4. **Kısıtlama & kontrol** — sınırlar, uzunluk/ton, kabul kriterleri, "emin değilsen sor"
5. **Yazılım-özel** — kod ajanına talimat (AGENTS.md tarzı), spec yazma, test-driven prompt,
   code review isteme, LLM-dostu hata mesajı
6. **İleri / ajansal** — prompt zincirleme, meta-prompting, düşün-eylem (ReAct), çok-adımlı görev bölme
7. **Anti-pattern'ler** — belirsizlik, aşırı yükleme, çelişki, boş övgü, gömülü varsayım

Her kategori ~7 alıştırma; her kategoride 1-2'si `aiDegerlendir: true`.

## Durum (aşamalı teslim)
### Faz A — mekanik uçtan uca (2 kategori: Temel yapı + Anti-pattern'ler) — TAMAM
- [x] A.1 types/: AtolyeKategori, AtolyeAlistirma, AtolyeDegerlendirme record'ları
- [x] A.2 content/prompt-atolyesi/: 01-temel-yapi.md + 07-anti-pattern.md (14 alıştırma)
- [x] A.3 repo/: AtolyeRepo (yapılandırılmış markdown parse) + AtolyeRepoTest (7)
- [x] A.4 prompts/prompt-degerlendirme.st + service/AtolyeService + AtolyeServiceTest (5, AI mock)
- [x] A.5 web/: AtolyeController (GET /api/atolye, POST /api/atolye/degerlendir) + AtolyeControllerTest (4)
- [x] A.6 Frontend: sol menü satırı + atölye görünümü + kart mekaniği (toggle + HER kartta AI değerlendir)
- [x] A.7 ./mvnw verify YEŞİL (139 test, ArchUnit dahil) + tarayıcıda uçtan uca doğrulama:
      atölye açılır, ideal toggle (tokensiz), her kartta AI değerlendir, gerçek değerlendirme (%78
      puan + geri bildirim + iyileştirme), haftaya tıklayınca program görünümüne döner.
### Faz B — içerik tamamlama (kalan 5 kategori) — TAMAM
- [x] B.1 2026 güncellik araştırması: düşünen modele "adım adım düşün" gereksiz/zararlı; lost-in-the-
      middle 1M token'da bile gerçek; jenerik rol-priming zayıfladı; AGENTS.md fiili standart; "format
      vergisi" (önce düşün, sonra formatla); aşırı-emir dili overtrigger; budget_tokens deprecate.
- [x] B.2 content/prompt-atolyesi/02..06: muhakeme(7), yapilandirma(7), kisitlama(6), yazilim-ozel(6),
      ileri-ajansal(6) — 32 alıştırma. 2026 nüansları "Neden" bölümlerine işlendi.
- [x] B.3 Toplam 7 kategori / 46 alıştırma. Test güncellemesi: AtolyeRepoTest + AtolyeServiceTest artık
      içerik büyümesine dayanıklı (sabit "2 kategori" yerine yapısal doğrulama). ./mvnw verify YEŞİL
      (139 test). Tarayıcı: 7 sekme render oluyor, yeni kategoriler kart mekaniğiyle çalışıyor.

## Sonuç
7 kategori × 7 = **49 alıştırma** (04/05/06'ya öncelik-ver / llm-dostu-hata / zero-shot-once eklendi).
Atölye tamamlandı; ileride yeni kategori/alıştırma eklemek yalnızca content/prompt-atolyesi/'ye md
dosyası eklemek — kod değişmez. ./mvnw verify YEŞİL (139 test).

## Kabul kriterleri
- API anahtarı OLMADAN atölye tam gezilebilir; statik "ideal çözümü göster" çalışır; AI değerlendir
  butonu yalnız anahtar varken görünür.
- Mimari testler (ArchUnit) yeşil; yeni davranışlara test.
- Sol menü/hafta/kilit/accordion mantığı BOZULMAZ (atölye ayrı görünüm modu).
- content/ değişiklikleri koddan ayrı commit; tools/html-to-content.mjs çalıştırılmaz.

## Açık sorular
- UNCONFIRMED: Alıştırma başına "kötü örnek" her zaman gerekli mi, yoksa bazıları sıfırdan-yaz mı?
  (Faz A'da ikisini de dene, mekanik otursun.)
