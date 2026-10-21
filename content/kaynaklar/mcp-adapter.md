---
baslik: "MCP: Uygulamanı Bir AI Ajanına Araç Olarak Aç"
yazar: "AI Öğretmen — Hafta 9 (hexagonal/adapter) laboratuvarı"
tarih: "2026-07-16"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 9
---

### Bu metin ne anlatıyor?
Yazdığın bir uygulamanın yaptığı işleri, yapay zekâ asistanlarının da kullanabilmesi için nasıl
dışarı açarsın? Bu metin bunun ortak kuralını, yani **MCP**'yi anlatıyor. MCP'yi bilgisayardaki
USB girişi gibi düşün: yeteneğini bir kez bu standarda göre takarsan, her asistan ona bağlanabilir.
Bir de para tarafı var: doğru kurarsan yapay zekânın faturası sana değil, bağlanan kişiye çıkar.

## Neden bu özet
Hafta 9'da hexagonal mimariyi öğrendin: çekirdek mantık ortada durur, dış dünyaya **adapter'larla**
bağlanır — REST bir adapter, veritabanı bir adapter. Bu kaynak, 2026'nın fiili standardı olan
**MCP'yi** (Model Context Protocol) tam da bir **adapter** olarak tanıtır: uygulamanın çekirdeğini
değiştirmeden, ona AI ajanlarının bağlanacağı yeni bir kapı açarsın. Üstelik bu kapı, kendi
uygulaman (AI Öğretmen) üzerinden anlatılır — yani yine "öğrettiğinin canlı örneği". Ve bir bonus:
doğru kurulursa, başkaları uygulamanı kullanır ama **senin AI faturanı ödemez.**

## Türkçe çalışma metni

### 1. MCP nedir — evrensel bir priz
**MCP** (Model Context Protocol — "model bağlam protokolü"), Anthropic'in Kasım 2024'te açık
standart olarak yayımladığı ve 2026'da yaygınlaşan bir **protokoldür** (iki programın birbirini
anlaması için üzerinde anlaştığı ortak konuşma kuralı). Tek cümlede: **bir AI ajanının (Claude
Desktop, Claude Code, Cursor gibi) dış araçlara ve verilere bağlanması için evrensel bir priz.**

En iyi benzetme **USB'dir.** USB'den önce her cihazın kendi kablosu vardı; USB gelince tek bir
standart port oldu — fare de, klavye de, disk de aynı prize takılıyor. MCP de AI dünyasında bunu
yapar: bir yeteneği **bir kez** MCP olarak yazarsan, **herhangi bir AI ajanı** onu kullanabilir
(bkz. **Diyagram 1**). Her ajan için ayrı entegrasyon yazmazsın.

Bu kazancın literatürdeki adı **M×N → M+N**'dir. Elinde **M** tane ajan ve **N** tane uygulama
varsa, ortak bir protokol yokken her ajanı her uygulamaya ayrı ayrı bağlaman gerekir: **M×N** tane
entegrasyon. MCP ile her ajan protokolü **bir kez** öğrenir, her uygulama protokolü **bir kez**
konuşur: **M+N** tane iş. 3 ajan + 4 uygulamada fark 12'ye karşı 7'dir; 10 ajan + 20 uygulamada
200'e karşı 30. Yani MCP'nin asıl vaadi hız değil, **bağlantı sayısının patlamasını durdurmaktır.**

Üç rolü baştan ayır, çünkü adları sonra sık geçecek:

- **Sunucu (server):** yeteneği dışarı açan taraf — bu metinde **senin uygulaman.**
- **İstemci (client):** sunucuya bağlanan taraf; ajan uygulamasının içinde yaşar ve her sunucu
  için bir tane bulunur.
- **Barındırıcı (host):** istemcileri içinde çalıştıran ajan uygulamasının kendisi (Claude Desktop,
  Claude Code). Dil modelini çağıran ve kararı veren odur — **sunucu değil.** Bu ayrım §3'teki
  "token kimde yanar" sorusunun tüm cevabıdır.

Şu an senin AI Öğretmen uygulaman AI'yı **çağırıyor** (OpenRouter'a prompt gönderiyor); ok tek
yönlü: `uygulama → AI`. MCP'nin açtığı şey **ters yön**: bir AI ajanı, senin uygulamandaki bir
yeteneği **araç olarak** kullanabilir — örneğin "öğrenci hangi haftada?" diye sorar ya da "hafta 3
içeriğini getir" der.

### 2. Üç yapı taşı — Tools, Resources, Prompts
MCP'nin sunduğu üç şey var (bkz. **Diyagram 2**):

- **Araçlar (Tools):** Ajanın **çağırabileceği eylem/veri metotları.** Senin uygulamanda:
  "ilerlememi kaydet", "hafta detayını getir", "atölye kategorilerini listele". Bir araç, senin bir
  servis metodunun (ör. `MufredatService.haftaDetay`) dışarıya açılmış halidir.
- **Kaynaklar (Resources):** Ajanın **okuyabileceği içerik.** Senin uygulamanda: haftalık ders
  metni, atölye alıştırmaları, çalışma kaynakları. Bir **URI** (bir şeyin benzersiz adresi — web
  adresi gibi) ile adreslenir: `content://hafta/3`.
- **Prompt'lar (Prompts):** Ajana **verilen hazır talimat şablonları.** Senin uygulamanda:
  "öğretmen kimliği + Sokratik yöntem", değerlendirme rubriği. İşte can alıcı nokta bir sonraki
  bölümde: bu prompt'u **ajan alır ve muhakemeyi kendisi yapar.**

### 3. Kritik olay — token kimin? (ve MCP neden bir adapter)
Programın ana derdi hep şuydu: uygulaman AI'yı çağırıyor, yani **her kullanıcı senin OpenRouter
token'ını yakıyor.** Çok kullanıcı = fatura patlar. MCP bunu tersine çevirir (bkz. **Diyagram 3**):

- **Klasik model:** Kullanıcı → **senin uygulaman → LLM (OpenRouter)**. Muhakemeyi sunucun yapar,
  **token sende yanar.**
- **MCP modeli:** Kullanıcının **kendi Claude'u** senin MCP sunucuna bağlanır. Sunucun yalnızca
  **araç + içerik + prompt** sunar — **LLM çağırmaz.** Öğretmenlik muhakemesini **kullanıcının
  Claude'u, kullanıcının token'ıyla** yapar. **Token kullanıcıda yanar, sende değil.**

Yani sunucun bir "öğretmen" değil, bir **öğretmen donanımı** olur: müfredatı, öğrencinin durumunu ve
"nasıl öğretileceğinin talimatını" verir; öğretme işini bağlanan Claude yapar.

**Peki bu neden bir adapter (Hafta 9)?** Çünkü MCP tam olarak hexagonal'daki bir **adapter'dır.**
REST bir adapter, veritabanı bir adapter, MCP de bir adapter. Senin çekirdek servislerin
(`MufredatService`, `IlerlemeService`) **hiç değişmeden**, onlara yeni bir dış kapı (MCP) eklersin.
Hafta 9'da teorisini gördüğün port-adapter, burada gerçek bir MCP adapter'ıyla somutlaşır.

### 4. En güzel yanı — kural mimariyle mühürlenir
"MCP LLM çağırmasın ki token kullanıcıda kalsın" bir **niyet** değil, **derleme-zamanı garantisi**
olabilir. Bu uygulamanın **ArchUnit** (Java projelerinde mimari kuralları normal bir test gibi
yazıp doğrulatan kütüphane) kuralı (`MimariTestleri` → `claude_yalniz_ai_paketinden`), Spring
AI / Claude sınıflarına yalnız `ai/` paketinin bağlanmasına izin verir. Yeni `mcp/` paketi Spring
AI'yı **import etmediği sürece** LLM çağıramaz — ve eğer yanlışlıkla çağırırsan **build kırmızı
olur.** Yani Hafta 4'te öğrendiğin "guardrail" (belgeleme değil, zorlama), token modelini de mühürler:
maliyet güvencesi bir yorum satırı değil, geçen ya da kalan bir testtir.

### 5. Yerelden uzağa — iki taşıma (transport)
**Taşıma (transport)**, protokolün mesajlarını fiziksel olarak hangi kanaldan geçirdiğidir —
protokolün *ne söylediği* değil, *nasıl iletildiği*. MCP bugün iki taşıma tanımlar:

- **stdio (yerel, tek kişi):** "Standart girdi/çıktı" — iki programın birbirine boru gibi
  bağlanması. Uygulamanı kendi makinende çalıştırırsın; Claude Desktop onu bir alt-süreç olarak
  başlatır ve mesajlar bu boru üzerinden akar. Ağ yok, kimlik doğrulama yok. Öğrenmek ve kendi
  kullanımın için ideal.
- **Streamable HTTP (uzak, çok kişi):** Sunucun herkese açık bir adreste HTTP ile yayınlanır;
  birçok kişi kendi Claude'uyla bağlanır. Kimlik doğrulama (**OAuth** — kullanıcının parolasını
  vermeden uygulamalara sınırlı izin verdiği yetkilendirme standardı) ve HTTPS gerekir.
  "Başkalarına açmak" senaryosu budur — ama gerçek kullanıcı verisi işleyeceğin için burada
  KVKK/gizlilik (bkz. `ai-yonetisim-uyum` kaynağı) mimari bir karar haline gelir.

> **Tarih notu (eskiyebilir):** Uzak bağlantı için ilk sürümlerde ayrı bir "HTTP+SSE" taşıması
> vardı; 2025'teki protokol revizyonunda yerini **Streamable HTTP**'ye bıraktı ve eski taşıma
> geriye dönük uyumluluk için tutulan bir miras hâline geldi. Eski bir eğitimde "SSE endpoint'i
> aç" diyorsa, o metin bu değişiklikten önceye aittir. Taşıma katmanı değişse de §1-§4'te
> anlattığımız hiçbir şey değişmez — çünkü **protokol taşımadan bağımsızdır**; zaten iyi bir
> katman ayrımının anlamı budur.

### 6. MCP tek katman değil — A2A onun **üstünde** durur
Piyasada "MCP mi A2A mı?" diye bir tartışma göreceksin. **Yanlış soru** — ikisi rakip değil, farklı
sınırların protokolü. Linux Foundation'ın kendi tanımıyla: *A2A, ajanların **kurumlar arası** nasıl
konuşup koordine olduğunu; MCP, ajanların **iç araçlara ve veri kaynaklarına** nasıl bağlandığını
tanımlar.* İkisi de artık aynı çatı altında (MCP, Aralık 2025'te Agentic AI Foundation'a bağışlandı).

Bunu Hafta 9'un diliyle oku: **ikisi de adapter, ama farklı port'ların adapter'ı.** MCP dikey
(ajan → senin araçların), A2A yatay (ajan ↔ başka kurumun ajanı). Yani bu kaynakta öğrendiğin
MCP adapter işi **A2A ile geçersizleşmez**; A2A gerekirse onun üstüne biner.

> **Durum notu (Temmuz 2026 — eskiyebilir):** A2A ölü değil: ilk kararlı sürüm **v1.0** (Nisan 2026),
> Java SDK GA (Haziran 2026), bir yılda 50'den 150'yi aşkın kuruluş desteği. **Ama abartma:**
> "150+ kuruluş destekliyor" ≠ "150+ üretimde çalışıyor" — üretim kullanımının çoğu hâlâ **PoC**
> (proof of concept — "olabilirliğini göstermek için yapılan deneme sürümü"; gerçek kullanıcıya
> açılmış bir sistem değil) ve
> kamuya açık retansiyon verisi yok. Bu yüzden bu program A2A'ya ayrı bir hafta ayırmıyor:
> **bilmen gereken bir sınır, bugün kurman gereken bir sistem değil.**

### 7. Priz takıldıktan sonra — araç arayüzü ve hata sözleşmesi
Sunucuyu açmak işin yarısı; ajan araçlarını **adından, açıklamasından ve parametre şemasından**
seçer. Yani araç tasarımı = **prompt tasarımı + API tasarımı**. İyi bir arayüz, doğru eylemi kolay,
yanlış eylemi zor ya da imkânsız kılar. İyi bir araç açıklaması altı şeyi söyler: ne yapar · ne
zaman kullanılır · **ne zaman kullanılMAZ** · girdi biçimi · çıktının içeriği · sınırlar/güvenlik.

İki tasarım deseni ezber değerinde:
- **Enum kullan** (kapalı kümelerde): **enum**, "izin verilen değerlerin önceden sayıldığı liste"
  demektir (ör. durum = `bekliyor | onaylandı | iptal`). Modele serbest metin yazdırma; geçerli
  değerleri şemada tek tek sırala — yazamadığı şeyi uyduramaz.
- **Bul-sonra-eylem (lookup-then-act):** kullanıcı varlığa bulanık adla değinirken önce `ara(sorgu)`
  kimlik döndürür, sonra eylem **yalnızca kesin kimlikle** çalışır. Aday birden çoksa modele
  tahmin ettirme — ayırt edici alanlarla kullanıcıya seçtir. (Genel bir kabuk komutu yerine adanmış
  araç açmanın nedeni de bu: `eposta_gonder` aracı onaya bağlanabilir, `bash -c "curl ..."` bağlanamaz.)

**Hata sözleşmesi:** genel bir "hata oluştu", modeli tahmine zorlar — yeniden mi denesin, sorsun mu,
dursun mu? Üretim aracı hatayı SINIFLANDIRIR:

| Kategori | Örnek | Doğru davranış |
|---|---|---|
| Geçici altyapı | zaman aşımı, 503 | güvenliyse **aracın içinde** yeniden dene — model ilk başarısız denemeyi görmemeli |
| Kalıcı doğrulama | bozuk tarih, geçersiz enum | yapılandırılmış detay dön — model düzeltsin ya da sorsun |
| İş kuralı | uygun değil, mükerrer | yeniden-denenemez + kullanıcıya açıklama |
| Yetki | erişim yok | yeniden-denenemez + yükseltme yolu |
| **Belirsiz yazma durumu** | ödeme/bildirim gönderildikten sonra zaman aşımı | **belirsizliği raporla, otomatik yeniden deneme YASAK** (yan etki olmuş olabilir) |

MCP'de uygulama-düzeyi hata normal araç sonucu içinde `isError: true` ile döner (protokol hatası
değil) — kategori + `retryable` + sonraki adımlar alanlarıyla. Bu, `llm-uygulama-guvenligi`'ndeki
"LLM-dostu hata" fikrinin araç-sözleşmesi hâli.

**Bu projeye uygula (lab + doğrulanmış cevap):** 8 aracımızı bu gözle denetledik — sonuç olumlu:
`OgretmenMcpAraclari.arac()` sarmalayıcısı `BulunamadiException`/`IllegalArgumentException`'ı yakalayıp
**`isError: true`** ile zarif sonuç dönüyor ("Kaynak bulunamadı: X"); çıplak istisna istemciye sızmıyor.
Yani sözleşmenin çekirdeği (uygulama hatası ≠ protokol hatası) yerinde. İyileştirme payı da dürüstçe
kayıtlı (borç listesi): düz metin mesaj, kategorili yapıya (`error_category` + `retryable` + kurtarma
yolu — "geçerli slug'lar `kaynaklar` aracında") yükseltilebilir. `gorev_isaretle` için düşün:
toggle yarı-yolda zaman aşımına uğrarsa istemcinin körlemesine tekrar denemesi işareti GERİ alır —
"belirsiz yazma durumu" dersinin bizdeki somut yüzü.

> Bu bölümün çerçevesi, Claude Certified Architect – Foundations sınavının topluluk rehberinden
> (Daron Yöndem) süzülmüştür — sınav, araç arayüzünü ve hata sözleşmesini **mimari beceri** olarak
> ölçüyor; bkz. Başlangıç Kılavuzu'ndaki dış hedef notu.

> **Özet:** MCP, uygulamanın yeteneklerini AI ajanlarına açan bir **adapter**'dır. Doğru kurulursa
> (sunucu LLM çağırmaz) muhakeme ve maliyet bağlanan kullanıcıda kalır. Bu, hem Hafta 9'un
> (hexagonal) hem Hafta 4'ün (guardrail) canlı bir sentezidir.

## Diyagram 1 — MCP: bir kez yaz, her ajan bağlansın (evrensel priz)

```svg
<svg viewBox="0 0 640 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MCP evrensel priz: farklı AI ajanları (Claude Desktop, Claude Code, Cursor) tek bir MCP protokolü üzerinden senin uygulamanın araçlarına bağlanır">
  <defs>
    <marker id="mcp1-ok" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">MCP: bir kez yaz — herhangi bir AI ajanı bağlansın</text>
  <text x="320" y="45" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">USB gibi: tek standart priz, farklı cihazlar aynı porta takılır.</text>

  <rect x="24" y="78" width="150" height="40" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="99" y="103" text-anchor="middle" font-size="11" font-weight="600" fill="#4F46E5" font-family="system-ui">Claude Desktop</text>
  <rect x="24" y="128" width="150" height="40" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="99" y="153" text-anchor="middle" font-size="11" font-weight="600" fill="#4F46E5" font-family="system-ui">Claude Code</text>
  <rect x="24" y="178" width="150" height="40" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="99" y="203" text-anchor="middle" font-size="11" font-weight="600" fill="#4F46E5" font-family="system-ui">Cursor / başka ajan</text>
  <text x="99" y="238" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">AI ajanları (istemci)</text>

  <rect x="266" y="120" width="108" height="56" rx="10" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.8"/>
  <text x="320" y="145" text-anchor="middle" font-size="13" font-weight="700" fill="#C77A17" font-family="system-ui">MCP</text>
  <text x="320" y="164" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">ortak protokol</text>

  <rect x="466" y="120" width="150" height="56" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="541" y="143" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Senin uygulaman</text>
  <text x="541" y="161" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">araçlar · içerik · prompt</text>

  <line x1="174" y1="98" x2="264" y2="140" stroke="#186399" stroke-width="1.6" marker-end="url(#mcp1-ok)"/>
  <line x1="174" y1="148" x2="264" y2="148" stroke="#186399" stroke-width="1.6" marker-end="url(#mcp1-ok)"/>
  <line x1="174" y1="198" x2="264" y2="156" stroke="#186399" stroke-width="1.6" marker-end="url(#mcp1-ok)"/>
  <line x1="374" y1="148" x2="464" y2="148" stroke="#186399" stroke-width="1.8" marker-end="url(#mcp1-ok)"/>

  <text x="320" y="270" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Her ajan için ayrı entegrasyon yok — hepsi aynı MCP prizinden bağlanır.</text>
</svg>
```

## Diyagram 2 — Üç yapı taşı: Tools, Resources, Prompts

```svg
<svg viewBox="0 0 640 288" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="MCP üç primitifi: araçlar (eylem/veri), kaynaklar (içerik), prompt'lar (talimat şablonu) — her biri AI Öğretmen örneğiyle">
  <text x="320" y="26" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">MCP'nin üç yapı taşı</text>
  <text x="320" y="45" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Ajana ne sunarsın: eylem, içerik, talimat.</text>

  <rect x="24" y="66" width="188" height="188" rx="12" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="118" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#186399" font-family="system-ui">Araçlar (Tools)</text>
  <text x="118" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ajanın çağırdığı eylem/veri</text>
  <line x1="44" y1="124" x2="192" y2="124" stroke="#CBD9E4" stroke-width="1"/>
  <text x="118" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"ilerlememi kaydet"</text>
  <text x="118" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"hafta 3'ü getir"</text>
  <text x="118" y="186" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">"atölyeyi listele"</text>
  <text x="118" y="222" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">= bir servis metodu,</text>
  <text x="118" y="238" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">dışa açılmış</text>

  <rect x="226" y="66" width="188" height="188" rx="12" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="320" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#25804C" font-family="system-ui">Kaynaklar (Resources)</text>
  <text x="320" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ajanın okuduğu içerik</text>
  <line x1="246" y1="124" x2="394" y2="124" stroke="#C4E0CF" stroke-width="1"/>
  <text x="320" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">ders metni</text>
  <text x="320" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">atölye alıştırmaları</text>
  <text x="320" y="186" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">çalışma kaynakları</text>
  <text x="320" y="222" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">URI ile adreslenir:</text>
  <text x="320" y="238" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">content://hafta/3</text>

  <rect x="428" y="66" width="188" height="188" rx="12" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.6"/>
  <text x="522" y="92" text-anchor="middle" font-size="13" font-weight="700" fill="#4F46E5" font-family="system-ui">Prompt'lar (Prompts)</text>
  <text x="522" y="112" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">ajana verilen talimat</text>
  <line x1="448" y1="124" x2="596" y2="124" stroke="#CBCBEF" stroke-width="1"/>
  <text x="522" y="146" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">öğretmen kimliği</text>
  <text x="522" y="166" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">Sokratik yöntem</text>
  <text x="522" y="186" text-anchor="middle" font-size="10" fill="#5D7789" font-family="system-ui">değerlendirme rubriği</text>
  <text x="522" y="222" text-anchor="middle" font-size="9.5" font-weight="700" fill="#4F46E5" font-family="system-ui">muhakemeyi AJAN yapar</text>
  <text x="522" y="238" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">(token onda!)</text>
</svg>
```

## Diyagram 3 — Token kimde? Klasik vs MCP (ve adapter)

```svg
<svg viewBox="0 0 640 340" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Klasik modelde sunucu LLM çağırır token sende yanar; MCP modelinde kullanıcının Claude'u muhakemeyi yapar token kullanıcıda yanar; MCP bir adapter'dır">
  <defs>
    <marker id="mcp3-a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#BE4433"/></marker>
    <marker id="mcp3-b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Token kimde yanar? Klasik vs MCP</text>

  <rect x="24" y="44" width="592" height="118" rx="11" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.3"/>
  <text x="44" y="66" text-anchor="start" font-size="11.5" font-weight="700" fill="#BE4433" font-family="system-ui">KLASİK — muhakeme sunucuda, TOKEN SENDE</text>
  <rect x="48" y="80" width="120" height="52" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.2"/>
  <text x="108" y="110" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kullanıcı</text>
  <rect x="260" y="80" width="130" height="52" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="325" y="103" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">senin uygulaman</text>
  <text x="325" y="120" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">LLM'i çağırır</text>
  <rect x="482" y="80" width="120" height="52" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.3"/>
  <text x="542" y="103" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">LLM (OpenRouter)</text>
  <text x="542" y="120" text-anchor="middle" font-size="9" fill="#BE4433" font-family="system-ui">💸 fatura SENDE</text>
  <line x1="168" y1="106" x2="258" y2="106" stroke="#5D7789" stroke-width="1.5" marker-end="url(#mcp3-a)"/>
  <line x1="390" y1="106" x2="480" y2="106" stroke="#BE4433" stroke-width="1.8" marker-end="url(#mcp3-a)"/>

  <rect x="24" y="176" width="592" height="118" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.3"/>
  <text x="44" y="198" text-anchor="start" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">MCP — muhakeme istemcide, TOKEN KULLANICIDA</text>
  <rect x="48" y="212" width="150" height="52" rx="8" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.3"/>
  <text x="123" y="235" text-anchor="middle" font-size="10.5" font-weight="700" fill="#4F46E5" font-family="system-ui">kullanıcının Claude'u</text>
  <text x="123" y="252" text-anchor="middle" font-size="9" fill="#25804C" font-family="system-ui">💳 fatura KULLANICIDA</text>
  <rect x="290" y="212" width="150" height="52" rx="8" fill="#FFFFFF" stroke="#186399" stroke-width="1.3"/>
  <text x="365" y="232" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">MCP sunucun</text>
  <text x="365" y="249" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">LLM çağırmaz — araç/içerik/prompt</text>
  <line x1="198" y1="238" x2="288" y2="238" stroke="#25804C" stroke-width="1.8" marker-end="url(#mcp3-b)"/>
  <line x1="288" y1="228" x2="200" y2="228" stroke="#25804C" stroke-width="1.4" marker-end="url(#mcp3-b)"/>
  <text x="470" y="234" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">Sunucu araç+talimat verir;</text>
  <text x="470" y="250" text-anchor="start" font-size="10" fill="#1C3247" font-family="system-ui">muhakemeyi Claude yapar.</text>

  <text x="320" y="316" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">MCP bir ADAPTER'dır (Hafta 9): çekirdek servisler değişmez, yalnız yeni bir dış kapı eklenir.</text>
  <text x="320" y="332" text-anchor="middle" font-size="10" fill="#C77A17" font-family="system-ui">Ve "MCP LLM çağırmaz" kuralını ArchUnit derleme zamanında mühürler (Hafta 4 guardrail).</text>
</svg>
```

## Öz-sınav (kendini test et)
1. MCP'yi "evrensel priz (USB)" benzetmesiyle anlat. "Bir kez yaz, her ajan bağlansın" ne demek —
   MCP olmadan aynı yeteneği üç farklı ajana açmak için ne yapman gerekirdi? **M×N → M+N** ifadesini
   5 ajan ve 6 uygulama için sayı vererek doldur.
2. Barındırıcı (host), istemci (client) ve sunucu (server) rollerini kendi cümlenle ayır. Bu üçünden
   **hangisi dil modelini çağırır** — ve bu cevap §3'teki "token kimde yanar" sorusuyla nasıl aynı
   cevaptır?
3. MCP'nin üç yapı taşını (tool/resource/prompt) kendi uygulamandan birer örnekle eşle. "Prompt"
   yapı taşı neden diğer ikisinden farklı — muhakemeyi kim yapar?
4. Klasik modelde bir öğrenci senin uygulamanı kullanınca token kimde yanar? MCP modelinde kim?
   Bu farkın çok-kullanıcı senaryosundaki sonucu ne?
5. "MCP LLM çağırmamalı" kuralı neden sadece bir yorum satırı değil de bir **derleme-zamanı
   garantisi** olabiliyor? Hangi ArchUnit kuralı bunu sağlıyor, ve yanlışlıkla ihlal edersen ne olur?
6. MCP neden bir "adapter" (Hafta 9 hexagonal)? Uygulamanın çekirdeğine (MufredatService gibi) MCP
   eklerken ne değişir, ne değişmez?
7. stdio ile Streamable HTTP taşımaları arasındaki fark ne? "Başkalarına açmak" istiyorsan hangisi
   gerekir ve bu hangi yeni sorumlulukları (güvenlik, gizlilik) getirir? Taşımanın değişmesi
   §1-§4'teki hangi şeyleri değiştirmez — ve bunun sebebi nedir?
8. Bir arkadaşın "MCP sunucumu kurdum ama hâlâ benim OpenRouter faturam artıyor" diyor. Muhtemel
   hata ne — MCP sunucusu bu senaryoda ne yapmamalıydı?
9. Birisi "MCP öldü, artık A2A var" diyor. Neden bu cümle bir kategori hatası? İki protokolün
   tanımladığı **sınırlar** nasıl farklı, ve bu ayrım hexagonal mimarinin hangi kavramına denk düşer?
10. "A2A'yı 150'den fazla kuruluş destekliyor" cümlesinden yola çıkarak "A2A üretimde yaygın"
    sonucuna varmak neden hatalı bir çıkarım? Bir mimar bir protokolün olgunluğunu değerlendirirken
    hangi kanıtı arar (bkz. `ai-verimlilik-olcumu` kaynağı — kanıt okuma disiplini)?
