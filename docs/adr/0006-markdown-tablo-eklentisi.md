# ADR-0006: Markdown tablo desteği — commonmark GFM Tables eklentisi

## Durum (Status)
Accepted — 2026-07-13

## Bağlam (Context)
Müfredat içeriği (`content/hafta-NN/gun-N.md`, `content/kaynaklar/*.md`) markdown olarak
yazılır ve `commonmark-java` ile HTML'e çevrilir (ADR-0002). Üç serviste aynı kurulum var:
`MufredatService`, `RaporService`, `KaynakService` — hepsi `Parser.builder().build()` +
`HtmlRenderer.builder().escapeHtml(true).build()`.

Sorun: **commonmark çekirdeği tablo desteklemez** (tablolar CommonMark spec'inde değil,
GitHub Flavored Markdown eklentisidir). Eklentisiz kurulumda bir markdown tablosu
`<table>` olarak değil, **ham boru işaretli paragraf** olarak render ediliyor:

```
<p>| | Junior A | Junior B |
|---|---|---|
| AI'ın rolü | Cevap makinesi | Düşündüren öğretmen |</p>
```

Bu okunaksız. Sorun canlıda doğrulandı (`<table>` üretilmiyor).

Etkilenen içerik: 6 gün dersi (kavram derinleştirmelerindeki karşılaştırma tabloları) +
`content/kaynaklar/hafta12-liderlik-sablonlari.md` (bu, eklendiğinden beri bozuk render
ediyormuş — fark edilmemişti).

Neden tablo gerekli: Eğitim içeriğinin en güçlü anlatım biçimlerinden biri **yan yana
karşılaştırma** (Junior A ↔ Junior B; tam delege ↔ Conceptual Inquiry; single-leader ↔
multi-leader ↔ leaderless; prompt ↔ RAG ↔ fine-tune; stil puan kartı). Bunları madde
listesine çevirmek anlatımı belirgin biçimde zayıflatır.

## Alternatifler (Considered Options)
1. **Tabloları madde listesine çevir** (kod değişikliği yok).
   Reddedildi: yan yana karşılaştırmanın pedagojik netliği kayboluyor; ayrıca içerik
   yazarken sürekli "tablo kullanma" kısıtı taşımak gerekiyor.
2. **HTML tabloyu doğrudan markdown içine göm** (`<table>...`).
   Reddedildi: renderer `escapeHtml(true)` ile çalışıyor (XSS güvenliği) — ham HTML
   kaçırılır, render edilmez. Bunu gevşetmek güvenlik duruşunu bozar.
3. **GFM Tables eklentisi ekle** — SEÇİLEN.

## Karar (Decision)
`org.commonmark:commonmark-ext-gfm-tables` (commonmark ile **aynı grup ve aynı sürüm**,
0.24.0) bağımlılığı eklenir ve üç renderer'a bağlanır:

```java
private static final List<Extension> MD_EKLENTILER = List.of(TablesExtension.create());
private final Parser mdParser = Parser.builder().extensions(MD_EKLENTILER).build();
private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
        .extensions(MD_EKLENTILER).escapeHtml(true).build();
```

`escapeHtml(true)` **korunur**: eklenti `<table>` etiketlerini kendi renderer'ıyla üretir;
escapeHtml yalnız kaynaktaki ham HTML'i kaçırır. Yani güvenlik duruşu değişmez.

## Sonuçlar (Consequences)
- ✅ Karşılaştırma tabloları düzgün `<table>` olarak render eder; içerik yazarken tablo
  serbest hale gelir.
- ✅ `hafta12-liderlik-sablonlari.md`'deki bozuk tablo kendiliğinden düzelir.
- ✅ Güvenlik değişmez (`escapeHtml(true)` yerinde).
- ➕ Tablolar için `style.css`'e sade bir tablo stili eklenir (okuma metni + ders içinde).
- ⚠️ Bir bağımlılık daha (küçük, resmî commonmark eklentisi, ek geçişli bağımlılık yok).
- ⚠️ Üç serviste tekrarlanan renderer kurulumu var; ileride ortak bir `MarkdownRenderer`
  bileşenine çıkarılabilir (borç: `docs/tech-debt-tracker.md`).
