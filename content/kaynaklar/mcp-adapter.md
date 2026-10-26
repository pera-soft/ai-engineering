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

> **Kim seçer? Yapı taşını buna göre seç.** Üçü arasındaki asıl fark *ne* sundukları değil,
> **kimin devreye soktuğudur.** Resmî belirtim (2026-07-28) üçünü böyle tanımlar:
> **araç = model denetimli** (model-controlled: model bağlama bakıp kendisi çağırır),
> **kaynak = uygulama güdümlü** (application-driven: barındırıcı uygulama neyi bağlama koyacağına
> kendisi karar verir; listeden seçtirmek de olur, otomatik eklemek de),
> **prompt = kullanıcı denetimli** (user-controlled: kullanıcı açıkça seçer, tipik olarak bir eğik
> çizgi komutuyla `/sokratik`). Belirtim şunu da ekler: bu "kimin *ne zaman* kullandığı"
> sorusudur, içeriği kimin yazdığı değil. Prompt metnini sunucu yazar. Yukarıdaki "ajan alır"
> cümlesinin doğru okuması da bu: prompt'u **kullanıcı seçer**, muhakemeyi **ajan** yapar.
> İki pratik sonuç var. (1) Aynı işlemi üç yapı taşından birden sunma; kimin tetiklemesi
> gerektiğine karar ver. "Hafta 3'ü getir" kullanıcı istemeden modelin de çağırabileceği bir okuma
> ise araçtır, kullanıcının bağlama iliştireceği bir belgeyse kaynaktır. (2) Kaynak içeriği **veridir,
> talimat değildir.** İçinde "önceki talimatları yok say" yazan bir ders metni komut değil, metindir
> (bkz. `llm-uygulama-guvenligi`, dolaylı prompt enjeksiyonu). Bizim sunucuda prompt'lar
> (`ogretmen-sistem`, `sokratik`, `degerlendir`, `atolye-degerlendir`, `quiz-uret`) kullanıcının
> seçeceği oturum kipleridir; `content://hafta/{no}` kaynakları uygulamanın iliştireceği metinlerdir;
> `hafta_detay` aracı ise modelin kendi başına çağırdığı okumadır (ADR-0007).

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

> **Sampling notu (Ekim 2026):** MCP'de bir de **Sampling** özelliği vardı: sunucu, *barındırıcının*
> modelini kullanarak LLM çağrısı yaptırabiliyordu. 2026-07-28 revizyonu Sampling'i (Roots ve
> Logging ile birlikte) **kullanım dışı** (deprecated) ilan etti. Kullanım dışı bırakılma penceresi
> boyunca çalışmaya devam eder, ama yeni uygulamalara eklenmemesi istenir. Önerilen geçiş yolu
> "LLM sağlayıcısının API'sini doğrudan çağır"dır; bu, yukarıdaki **klasik model**dir ve token yine
> sende yanar. Bizim token modelimizi bu değişiklik **etkilemez**, çünkü sunucumuz hiçbir LLM
> çağrısı yapmaz; ne doğrudan ne Sampling üzerinden. Muhakeme baştan beri istemcidedir.

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

> **Tarih notu (güncellendi: Ekim 2026, eskiyebilir):** Uzak bağlantı için ilk sürümlerde ayrı bir
> "HTTP+SSE" taşıması vardı; 2025'teki revizyonda yerini **Streamable HTTP**'ye bıraktı. Eski bir
> eğitimde "SSE endpoint'i aç" diyorsa, o metin bu değişiklikten önceye aittir.
> Sonra daha büyük bir değişiklik geldi. Resmî belirtimin sürüm sayfasına göre **güncel (current)
> sürüm 2026-07-28'dir** (taslak değil; https://modelcontextprotocol.io/specification/versioning).
> Değişiklik günlüğüne göre (https://modelcontextprotocol.io/specification/2026-07-28/changelog)
> bu revizyon çekirdeği **durumsuz** (stateless) yaptı:
> - `initialize` el sıkışması ve protokol oturumu (`Mcp-Session-Id` başlığı) **kaldırıldı**. Her
>   istek kendi protokol sürümünü ve istemci yeteneklerini `_meta` alanında taşır.
> - Sunucular **`server/discover`** RPC'sini uygulamak zorundadır (desteklenen sürümler, yetenekler,
>   kimlik). İstemcinin bunu çağırması isteğe bağlıdır.
> - Streamable HTTP'de SSE akışını sürdürme (`Last-Event-ID`) kaldırıldı: kopan akış uçuştaki
>   isteği kaybettirir ve istemci onu yeni bir istek olarak gönderir. Eski GET uç noktası yerine
>   tek bir uzun ömürlü POST yanıtı (`subscriptions/listen`) geldi.
> - Görevler (Tasks) çekirdekten çıkıp resmî bir uzantıya taşındı. **Roots, Sampling ve Logging
>   kullanım dışı** (deprecated) ilan edildi (bkz. §3 notu). Eski HTTP+SSE taşıması da resmen
>   "Deprecated" sınıfına alındı.
>
> Mimari ders şu: **durumsuz protokol ≠ durumsuz uygulama.** Sepet, taslak ya da uzun süren bir iş
> hâlâ durum ister. Belirtimin önerisi, bu durumu bağlantıya değil, sunucunun ürettiği **açık bir
> tutamaca** (handle) bağlamak ve tutamacı sıradan bir araç argümanı olarak taşımaktır. Ayrıntı, dört
> durum ömrü ve "yapışkan oturum neden çözüm değil" sorusu için
> [araç çağrısı güvenilirliği](arac-cagrisi-guvenilirligi.md) §5. Bu deponun sunucusu (MCP Java SDK
> 0.18.3, ADR-0007) el sıkışmalı **eski nesilden** bir sürüm konuşur: ADR'deki doğrulama
> `initialize` + `tools/list` ile yapıldı. SDK'nın `ProtocolVersions` sınıfı yalnız 2024-11-05,
> 2025-03-26, 2025-06-18 ve 2025-11-25 sürümlerini tanır; yani bu depo henüz 2025-11-25'te ve
> durumsuz 2026-07-28'i konuşmaz. Geçiş = SDK yükseltmesi + oturum varsayımlarının gözden
> geçirilmesi. Yeni belirtim el sıkışmalı sürümlerle birlikte çalışma
> için bir geriye dönük uyumluluk bölümü tanımlar. Taşıma ve oturum katmanı değişse de §1-§4'te
> anlattığımız hiçbir şey değişmez, çünkü **protokol taşımadan bağımsızdır**; iyi bir katman
> ayrımının anlamı budur.

### 5.1 Uzak sunucuda yetkilendirme — üç kural
Streamable HTTP'ye geçince sunucun bir **OAuth 2.1 kaynak sunucusu** (resource server) olur: token'ı
kendisi basmaz, harici bir yetkilendirme sunucusunun (Auth0 gibi) bastığı token'ı **doğrular.**
(stdio'da bu bölüm geçerli değildir: belirtime göre stdio uygulamaları OAuth akışını izlememeli,
kimlik bilgisini ortamdan almalıdır.) Üç kural ezber değerindedir:

1. **Kitle bağlama (audience binding, RFC 8707).** Sunucu yalnız **kendisi için** basılmış token'ı
   kabul eder. İstemci token isterken `resource` parametresiyle sunucunun kanonik adresini (ör.
   `https://sunucu.ornek/mcp`) bildirmek zorundadır; sunucu da token'daki `aud` değerinin bu adres
   olduğunu kontrol etmek zorundadır. Bu yapılmazsa, başka bir hizmet için alınmış bir token seninkini
   de açar.
2. **Token aktarımı (token passthrough) YOK.** Belirtimin cümlesi açıktır: MCP sunucuları kendi
   kaynakları için geçerli olmayan token'ları **kabul etmemeli ve başka yere taşımamalıdır.**
   Sunucun bir alt API'yi (GitHub, takvim) çağıracaksa, istemcinin sana verdiği token'ı ona
   **iletmez**; o API için ayrı, doğru kitleli bir token alır. Aksi hâlde "kafası karışmış vekil"
   (confused deputy) olursun: alt API, isteğin gerçekte kimden geldiğini bilemez.
3. **Asgari kapsam, gerektikçe yükselt.** İstemci başta yalnız gerekli kapsamı (scope) ister.
   Kapsamı yetmeyen bir çağrıda sunucu **403** + `WWW-Authenticate: … error="insufficient_scope",
   scope="…"` döner, istemci yeni bir onayla kapsamını yükseltir (step-up). "Her şeye baştan izin"
   istemek en az ayrıcalık ilkesine aykırıdır.

**Bu depoda (ADR-0007, `mcp-http` profili; sunucu 2026-10-21'de kapatıldı):** Kural 1 uygulanmıştı.
`mcp-server-security` modülünün `JwtResourceValidator`'ı `aud` değerinin MCP kaynak adresini
(istek kökeni + `/mcp`) içermesini şart koşuyordu (`validate-audience: true`). Nginx arkasında
bunun çalışması için `server.forward-headers-strategy: framework` gerekti; yoksa köken
`http://127.0.0.1:8091` kalır ve audience eşleşmezdi. Auth0 tarafında `resource_parameter_profile=compatibility`
şarttı, çünkü Auth0 varsayılanda `resource=` parametresine değil `audience=`'a bakıyordu. Kural 2
doğal olarak sağlanıyordu: sunucu hiçbir alt API çağırmıyor, LLM bile çağırmıyor (§4'ün ArchUnit
mührü). Kural 3 ise **eksikti**: kodda kapsam kontrolü yok, geçerli bir token sekiz aracın hepsini
açıyordu (yazan iki araç `gorev_isaretle` ve `statik_quiz_kaydet` dahil). Asgari kapsam ilkesine göre
sonraki adım okuma/yazma kapsam ayrımı olurdu. Bir not daha: 2026-07-28 revizyonu Dinamik İstemci
Kaydını (DCR) **İstemci Kimliği Üst Veri Belgeleri** (CIMD) lehine kullanım dışı bıraktı. ADR-0007'de
CIMD'yi Auth0 reddettiği için kapatıp DCR'ye düşmüştük. Bugün yeniden kursaydık, CIMD'yi destekleyen
bir yetkilendirme sunucusu seçmek bir kriter olurdu.

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
2026-10-26'ya kadar araç bir toggle'dı; yarı-yolda zaman aşımına uğrarsa istemcinin körlemesine
tekrar denemesi işareti GERİ alıyordu — "belirsiz yazma durumu" dersinin bizdeki somut yüzü.
ADR-0016 bunu "çevir" yerine "ayarla"ya çevirdi (opsiyonel `hedef_durum`, varsayılan `true`); artık
tekrar denemek sonucu değiştirmez. Eski kodun tam davranışı (sıralı ve eşzamanlı tekrar, kayıp
güncelleme), düzeltme ve düzeltme sırasında çıkan istisna sarma bulgusu için bkz.
[araç çağrısı güvenilirliği](arac-cagrisi-guvenilirligi.md) §8.

### 7.1 Araç arayüzünü zaman içinde korumak — üç kural daha
Araç tasarımı bir kez yapılıp bitmez; araç yayımlandıktan sonra **bir sözleşmedir.** Üç kural:

- **`action` argümanı 3'ten fazla değer alıyorsa aracı böl.** `notlar(action: "listele" | "ara" |
  "olustur" | "sil", ...)` gibi tek bir "İsviçre çakısı" araç DRY (tekrarsız) görünür, ama modeli
  doğru eylemi bir dizeden, doğru seçenekleri de tipsiz bir sözlükten seçmeye zorlar. Model aracı
  **adından** seçer; dört ayrı ad (`not_listele`, `not_ara`, `not_olustur`, `not_sil`) dört ayrı
  açıklama, dört ayrı şema ve dört ayrı onay kapısı demektir. Silme aracını onaya bağlamak, ancak
  silme ayrı bir araçsa mümkündür. Büyük kayıt defterlerinde ortak önek (`not_*`) ve kimlik
  alanlarına `pattern` (ör. `^not-[0-9]{8}$`, uydurma kimliği şemada yakalar) eklenir. Bizim sekiz
  aracımızın hiçbirinde `action` argümanı yok; her araç tek bir iş yapıyor.
- **Yeniden adlandırma yerine sürümle.** Yayımlanmış bir aracın adını değiştirmek ya da argüman
  tipini değiştirmek **kırıcı değişikliktir**: o adı ezberlemiş istemciler, kayıtlı prompt'lar ve
  testler sessizce bozulur. Doğru yol yeni bir ad eklemek (`gorev_durum_ayarla` ya da `..._v2`),
  eskisini bir sürüm boyunca "kullanım dışı" diye işaretlemek ve sonra kaldırmaktır. **Anlamı**
  değiştirmek de adı değiştirmek kadar kırıcıdır: `gorev_isaretle`'yi aynı adla "toggle"dan
  "ayarla"ya çevirmek, onu eski anlamıyla çağıran istemciyi bozar. (Bu depoda tam olarak bu yapıldı —
  aşağıdaki kutu kararı dürüstçe tartışıyor.)
- **Annotation'lar ipucudur, zorlama değil.** MCP araç tanımına `readOnlyHint`, `destructiveHint`,
  `idempotentHint`, `openWorldHint` gibi ipuçları eklenebilir. Barındırıcı bunları onay penceresi ve
  sunum için kullanır. Ama resmî belirtim (2026-07-28, Tools) istemcinin bunları **güvenilir bir
  sunucudan gelmedikçe güvenilmez** saymasını şart koşar. Yani `destructiveHint: false` yazmak hiçbir
  şeyi korumaz; gerçek yetki kontrolünü yine **sunucu** yapar. Yeniden denemenin güvenli olup
  olmadığına da ipucu değil, sözleşme ve kod karar verir (bkz.
  [araç çağrısı güvenilirliği](arac-cagrisi-guvenilirligi.md) §3). Bizim araçlarımız bugün hiç
  annotation taşımıyor (`Tool.builder()` yalnız ad, açıklama ve şema alıyor).

> **Gerçek vaka — kuralı bilerek esnettik (ADR-0016, 2026-10-26).** `gorev_isaretle` toggle'dan
> "ayarla"ya çevrildi ve **adı korundu**; yeni bir `gorev_durum_ayarla` eklenmedi. Yani yukarıdaki
> ikinci kuralın tam karşısındaki şey yapıldı: aynı ad, yeni anlam. Bunu yapmak neden savunulabilir
> oldu? (1) MCP sunucuları 2026-10-21'de kapandı; tek istemci yereldeki stdio bağlantısı — eski anlamı
> ezberlemiş uzak bir istemci yok. (2) Yeni `hedef_durum` parametresi **opsiyonel**; eski üç argümanla
> gelen çağrı hâlâ geçerli. (3) Varsayılan `true`, yani "işaretle" adının sözlük anlamıyla uyumlu:
> eski çağrıların neredeyse tamamı zaten "tamamlandı yap" niyetiyle yapılıyordu; değişen tek davranış,
> tamamlanmış görevi aynı çağrıyla **geri açmanın** artık mümkün olmaması (bunun için açıkça
> `hedef_durum: false` gerekir). Ama dürüst olalım: bu, kuralın **istisnası**dır, yeni kuralı değil.
> Uzakta çalışan, birden çok istemcili bir sunucuda doğru yol yeni araç adı (ya da sürüm) eklemek,
> eskisini bir sürüm boyunca "kullanım dışı" diye işaretlemek ve sonra kaldırmak olurdu — çünkü
> hangi istemcinin toggle davranışına (ör. "aynı çağrıyla geri al") dayandığını bilemezsin. Web
> tarafında aynı kararın tersi verildi: `POST /api/gorev/toggle` sessizce yeniden anlamlandırılmadı,
> **kaldırılıp** yerine yeni ad (`POST /api/gorev/durum`, `tamamlandi` zorunlu) geldi.

> *Şerh:* Bu kuralların kaynağı olan ders (AIEFS 13-05), açıklama ve ad iyileştirmesinin seçim
> doğruluğunu "10–20 puan" (Composio), "%62'den %89'a" (Databricks) artırdığını ve tek parça
> araçların "%15–30 daha kötü seçildiğini" söyler. Bunlar satıcıların kendi iç benchmark'larıdır,
> bağımsız doğrulanmamıştır ve kendi araç setine taşınmaz. Kuralların kendisi sağlam mühendislik
> gerekçesine dayanır (ad = seçim yüzeyi, ayrı araç = ayrı kapı); rakamlar dayanmaz. Kendi
> değerlendirme setinle ölç (bkz. `ai-verimlilik-olcumu`).

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
11. MCP'nin üç yapı taşını **kimin devreye soktuğuna** göre eşle (model / uygulama / kullanıcı).
    Bizim `sokratik` prompt'umuz bu ayrımda nereye düşer? "Kaynak içeriği veridir, talimat değildir"
    neden bir güvenlik kuralıdır?
12. Uzak bir MCP sunucusu için üç yetkilendirme kuralını say (kitle bağlama, token aktarımı yok,
    asgari kapsam). ADR-0007'deki `mcp-http` profili bunlardan hangisini uyguluyordu, hangisi eksikti?
13. "2026-07-28 revizyonu protokolü durumsuz yaptı" cümlesi, "artık uygulamam da durumsuz olmalı"
    anlamına gelir mi? Bu depoda `gorev_isaretle` 2026-10-26'da aynı adla toggle'dan "ayarla"ya
    çevrildi (ADR-0016). Bu karar §7.1'deki sürümleme kuralını hangi koşullar sayesinde esnetebildi,
    ve aynı değişiklik uzak, çok istemcili bir sunucuda yapılsaydı doğru yol ne olurdu?

### Cevaplar (11–13)
11. Araç = model denetimli, kaynak = uygulama güdümlü, prompt = kullanıcı denetimli. `sokratik`
    kullanıcının açıkça seçtiği bir oturum kipidir (prompt); metnini sunucu yazar, muhakemeyi ajan
    yapar. Kaynak metni bağlama girer ama komut yetkisi taşımaz; içine gömülmüş "talimatı yok say"
    satırı dolaylı prompt enjeksiyonudur ve veri olarak işlenmelidir.
12. Kitle bağlama: sunucu yalnız `aud` değeri kendi kaynak adresi olan token'ı kabul eder. Token
    aktarımı yok: istemcinin token'ı alt API'ye iletilmez, ayrı token alınır. Asgari kapsam: başta az
    iste, 403 + `insufficient_scope` ile yükselt. ADR-0007 kitle bağlamayı uyguluyordu
    (`JwtResourceValidator`, `validate-audience: true`, forward-headers). Token aktarımı sorusu doğal
    olarak kapalıydı, çünkü sunucu hiçbir alt API çağırmıyordu. Kapsam ayrımı eksikti: tek bir token
    yazan araçlar dahil hepsini açıyordu.
13. Hayır: durumsuz protokol ≠ durumsuz uygulama. Uzun ömürlü durum, sunucunun ürettiği açık bir
    tutamaç altında paylaşılan depoda yaşar. Esnetmeyi mümkün kılan koşullar: MCP sunucuları kapalı,
    tek istemci yerel; yeni `hedef_durum` opsiyonel; varsayılanı (`true`) adın anlamıyla uyumlu.
    Uzak, çok istemcili bir sunucuda anlam değişikliği kırıcıdır: yeni bir araç eklenir (ör.
    `gorev_durum_ayarla` ya da `..._v2`), eskisi bir sürüm boyunca kullanım dışı işaretlenip kaldırılır.

## Kaynak ve lisans
- **Bu deponun kendi vakası:** ADR-0007 (MCP sunucusu, `mcp-http` profili, OAuth 2.1 resource server),
  `mcp/OgretmenMcpAraclari.java`, `application-mcp-http.yml`, `docs/tech-debt-tracker.md`;
  ADR-0016 (`gorev_isaretle` toggle → ayarla, adın korunması; 2026-10-26).
- **Model Context Protocol belirtimi** (modelcontextprotocol.io, 2026-10-25'te okundu): Versioning
  sayfası (güncel sürüm 2026-07-28), 2026-07-28 Changelog, Tools / Resources / Prompts sayfaları
  (kimin denetlediği, annotation'ların güvenilmezliği, durumlu araç notu) ve Authorization sayfası
  (RFC 8707 kitle doğrulaması, token aktarımı yasağı, kapsam yükseltme, stdio istisnası). §2, §3,
  §5 ve §7.1'deki sürüm ve kural bilgileri bu sayfalardan doğrulandı.
- **AI Engineering from Scratch** — Rohit Ghumare, github.com/rohitg00/ai-engineering-from-scratch,
  MIT Lisansı, "Copyright (c) 2026 Rohit Ghumare". Büyütmede yararlanılan dersler: Faz 11 · 14 Model
  Context Protocol; Faz 13 · 05 Tool Schema Design, 06 MCP Fundamentals, 07 Building an MCP Server,
  09 MCP Transports, 10 MCP Resources and Prompts, 11 MCP Sampling, 16 MCP Security: OAuth 2.1.
  Uyarlama notu: çeviri değil sentezdir. Ders kodu kopyalanmadı. Derslerdeki satıcı benchmark
  rakamları §7.1'de *Şerh* olarak işaretlendi. Derslerin MCP sürüm iddiaları üçüncü taraf
  kaynaklara değil, yukarıdaki resmî belirtime göre yazıldı.
- §7'nin çerçevesi: Claude Certified Architect – Foundations topluluk rehberi (Daron Yöndem), §7'deki
  nota bakınız.
