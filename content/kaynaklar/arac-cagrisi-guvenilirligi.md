---
baslik: "Araç Çağrısı Güvenilirliği — Ajanın Eli Aynı Yere İki Kez Değmesin"
yazar: "AI Öğretmen çalışma metni — AI Engineering from Scratch (Rohit Ghumare, MIT) dersleri: 13-29, 13-13, 13-01, 13-03, 13-28, 13-09, 16-22, 15-15, 15-16 + bu deponun kendi vakası (gorev_isaretle)"
tarih: "2026-10-25"
url: "https://github.com/rohitg00/ai-engineering-from-scratch"
dil: "Türkçe kavram özeti (diyagramlı)"
etiketler: 9,10,11
---

### Bu metin ne anlatıyor?
Ajan bir aracı çağırır, yanıt gelmez, ajan yeniden dener. Sonuç: ödeme iki kez çekilir, e-posta
iki kez gider, işaretlediğin görev geri açılır. Bu metin, ajanın dünyaya dokunan araç çağrılarını
**tekrarlandığında zarar vermeyecek** biçimde kurmayı anlatır. Akılda kalacak altı kural:

1. **Yeni istek kimliği (RPC id) idempotency değildir.** Aynı iş niyetini tanıyan bir
   **idempotency anahtarı**, argümanların **parmak izi** (aynı anahtar + farklı argüman = reddet)
   ve anahtarı sahiplenmeyi etkiyle **tek transaction'da** (ya da outbox'ta) birleştiren sınır şart.
2. **Yeniden denemeyi sınıflandır:** yan etkisiz okuma = güvenli; anahtarlı yazma = yalnız *aynı
   anahtar + aynı argümanla* koşullu; anahtarsız yazma = güvensiz: otomatik deneme yok, önce
   durumu yeniden oku. Denemeler bütçeli, üstel geri çekilmeli ve **jitter**'lı olur.
3. **İki saat kur:** boşta kalma süresi (ilerleme sıfırlar) + kesin üst sınır (hiçbir şey
   uzatmaz). İptal bir yarıştır; ajan döngüsünün tavanı ve devre kesicisi olur.
4. **Uzun ömürlü durum açık bir tutamaç (handle) altında** paylaşılan depoda yaşar, yapışkan
   oturumda (sticky session) değil. Tutamacı döndürmeden önce kaydet.
5. **Önemli eylem: öner → onayla → ön koşulu kontrol et → uygula → yeniden okuyarak doğrula**;
   doğrulama tutmazsa geri al.
6. Bunların hepsi **Spring + PostgreSQL** ile kurulur; iş akışı motoruna ölçüp yetmediğini görünce geç.

Sonda bu deponun `gorev_isaretle` aracı laboratuvardır: 2026-10-26'ya kadar bir toggle'dı; eski
kodu okuyup tekrar çağrıda ne olduğunu, sonra düzeltmeyi ve mock'lu testin gizlediği hatayı göreceksin.

## Neden bu özet
[MCP adapter](mcp-adapter.md) §7'deki hata tablosu "belirsiz yazmada otomatik yeniden deneme
YASAK" der ve durur; *o zaman ne yapılır?* cevapsızdı. [API tasarımı](api-tasarim-grpc.md)
idempotency anahtarını tek satırla anar; outbox, jitter ve "etkin-tek-sefer" hiçbir kaynakta yoktu.
Ajan bu eksiği büyütür: insan butona iki kez basar, LLM ise **kendi kendine** yeniden dener.
Bu metin Hafta 9'u (araç = adapter) Hafta 11'e ([DDIA özeti](hafta11-ddia-veri-yogun.md): kayıp
güncelleme, kendi yazdığını okuma, çift yazma) bağlar; Hafta 10'un ADR disiplini de §7'deki
"motor mu, PostgreSQL mi" kararının biçimidir.

Kardeş metinler (burada tekrarlanmaz): harcama tavanı, kill switch ve insan onayı beklemesinin kalıcı
durum olması [ajan çalışma zamanı kontrolleri](ajan-calisma-zamani-kontrolleri.md)'nde; ajan döngüsü
ve durma kuralı [ajan mimarisi desenleri](ajan-mimarisi-desenleri.md)'nde; LLM sağlayıcısı çağrısının
fallback zinciri [LLM üretim yolu](llm-uretim-yolu.md)'nda. Burada yalnız **araç çağrısının**
kendisi var: modelin "şunu yap" dediği anla dünyanın gerçekten değiştiği an arasındaki boşluk.

Üç diyagram fikri kısaca gösterir; ayrıntı metindedir.

## 1. Sorun: her parça doğru davrandı, sistem yanlış
Dersin açılış sahnesi bir dağıtık sistemler klasiğidir. İstemci bir aracı çağırır, sunucu işe
başlar, ilerleme bildirimleri gelir, araya giren bir ters vekil (reverse proxy) akışı tamponlar.
İstemcinin süresi dolar ve bağlantıyı keser. Sunucu işi **bir milisaniye sonra** bitirir. İstemci
**yeni bir JSON-RPC id** ile yeniden dener ve değişiklik ikinci kez çalışır. Her bileşen yerel
olarak doğru davrandı; sistem bütün olarak yanlış sonuç verdi.

MCP mesajın ve taşımanın kurallarını tanımlar. Şunlar ise **senin uygulamanın** sorumluluğudur:
zaman bütçeleri, iş düzeyinde idempotency, sınırlı kuyruklar, yeniden deneme sınıflandırması,
kalıcı görev durumu, yeniden bağlanma politikası. Protokol bunları senin yerine çözmez.

**Ajan bu sorunu neden büyütür?** Üç sebebi var:

- **Yeniden deneyen katman sayısı artar.** HTTP istemcisi dener, harness dener, model de zaman
  aşımını "araç başarısız oldu" diye okuyup aynı çağrıyı *yeniden üretir*. Üç katman üçer kez
  denerse tek bir niyet en kötü durumda 3 × 3 × 3 = 27 çağrıya dönüşür. Bu yüzden yeniden
  deneme **tek katmanda** ve bir **deneme bütçesiyle** (retry budget) yapılır.
- **Model paralel çağrı üretir.** Sağlayıcıların çoğu modelin bir turda birden çok araç
  çağrısı yaymasına izin verir. Okumalar için bu iyidir. Ama sıra bağımlı çağrılar (`dosya_olustur`
  sonra `dosya_yaz`) ve **aynı kaynağa yapılan iki yazma** asla paralel gitmez. Her araca kayıt
  defterinde (tool registry) bir `parallel_safe` işareti koy ve fan-out'un alt sistemlerin hız
  sınırını (rate limit) aşmayacağını kontrol et. *Şerh:* ders paralel çağrının "duvar saatinde
  %60–70 kazanç" getirdiğini söyler; bu, iş yüküne bağlı genel bir benchmark'tır. Kendi araç
  gecikmelerinle ölçmeden rakam olarak alma.
- **Araçların güven sınıfı farklıdır.** 13-01 dersi her aracı ikiye ayırır: **saf (pure)**
  araç okur, yan etkisizdir, spekülatif çağrılabilir; **sonuçlu (consequential)** araç durumu
  değiştirir, para harcar, kullanıcı verisine dokunur. Kapı (onay, kayıt, kısıt) tam bu
  ayrımın üzerine kurulur. Bu metnin geri kalanı sonuçlu araçlarla ilgilidir.

## 2. Yeni RPC id idempotency değildir — anahtar, parmak izi, atomik sahiplenme
JSON-RPC `id` alanının tek işi bir **mesajı** yanıtıyla eşleştirmektir. Bir **iş işlemini**
tanımlamaz. İstemci `id: 41` ile bir ödeme gönderir, yanıtı kaybeder, `id: 42` ile yeniden dener:
sunucu iki farklı mesaj görür ve uygulama düzeyinde bir anahtar olmadan bunların aynı ödeme
olduğunu bilemez.

**İdempotency anahtarı (idempotency key)** iş niyetini tanımlar. Sunucu üç şey saklar: anahtar,
argümanların **parmak izi** (fingerprint: işlem adı + kiracı + normalleştirilmiş argümanların
hash'i) ve kesinleşmiş sonuç. Kurallar:

- Aynı anahtar + aynı parmak izi → **kayıtlı sonucu döndür**, işlemi yeniden çalıştırma.
- Aynı anahtar + **farklı** parmak izi → **reddet.** Aksi hâlde yanlışlıkla yeniden kullanılan bir
  anahtar başka bir iş niyetine takma ad olur ve "tekrar" sandığın şey farklı bir işlemi yutar.
- Anahtar yok → bu çağrı otomatik yeniden denemeye **kapalıdır** (§3).

**Anahtarı kim üretir?** Bu, ajana özgü bir tuzaktır. Anahtarı her çağrıda model üretirse,
yeniden üretilen çağrı yeni bir anahtar taşır ve koruma boşa çıkar. Anahtar **iş niyetinden
türetilmeli** ve harness ya da sunucu tarafından sabitlenmelidir: bir öneri kaydının kimliği ya da
15-15 dersindeki gibi `hash(iş_akışı_kimliği, eylem_imzası)`. Aynı dersin alıştırması bunu tersinden
gösterir: anahtara zaman damgası eklersen yeniden deneme yeni bir anahtarla gelir ve eylem iki kez
çalışır.

### Sınır atomik ve kalıcı olmalı
Şu sıra **güvensizdir**:

```java
// YANLIŞ: üç ayrı adım
if (!idempotencyRepo.varMi(anahtar)) {      // 1. kontrol et
    odemeServisi.cek(hesap, tutar);          // 2. etkiyi yap
    idempotencyRepo.kaydet(anahtar, sonuc);  // 3. sonucu yaz
}
```

İki işçi aynı anda "anahtar yok" görür ve ikisi de para çeker. Ya da süreç 2. adımdan sonra,
3. adımdan önce çöker; yeniden denemede anahtar yine "yok" görünür. Doğrusu, anahtarı
**sahiplenmeyi** ve iş değişikliğini **tek transaction**'a koymaktır:

```sql
BEGIN;
INSERT INTO idempotency (anahtar, parmak_izi, durum)
     VALUES (:k, :pi, 'ISLENIYOR')
ON CONFLICT (anahtar) DO NOTHING;      -- 0 satır: başkası sahiplendi → kayıtlı sonucu oku, izi karşılaştır
UPDATE hesap SET bakiye = bakiye - :tutar WHERE id = :hesap;
INSERT INTO outbox (anahtar, tur, govde) VALUES (:k, 'ODEME_BILDIRIMI', :json);
UPDATE idempotency SET durum = 'TAMAM', sonuc = :sonuc WHERE anahtar = :k;
COMMIT;
```

PostgreSQL'de aynı anahtarla eşzamanlı ikinci `INSERT`, birincinin transaction'ı bitene kadar
bekler, sonra çakışmayı görür. Yani iki bağlantı yarışsa da **bir** kesinleşmiş sonuç ve **bir**
yürütme kalır. Ders bunu SQLite'ın `BEGIN IMMEDIATE`'iyle gösterir; fikir aynıdır.

İki uyarı dersin kendisinden geliyor ve önemli:

- **Yerel bir tablo, dış bir etkiyi atomik yapmaz.** Yukarıdaki transaction, *veritabanı içindeki*
  değişikliği korur. Gerçek bir ödeme sağlayıcısı, e-posta ya da dağıtım çağrısı bu
  transaction'ın içinde değildir. Üç çıkış yolu var: dış sistemin **aynı anahtarı kabul etmesi**
  (ödeme sağlayıcılarının idempotency başlıkları gibi), **transactional outbox** ya da ikisi birlikte.
- **Süreç kilidi yetmez.** Java'da `synchronized` ya da bellek içi bir `Map`, birden çok replikayı
  korumaz ve yeniden başlatmada silinir. Sınır paylaşılan, kalıcı depoda olmalıdır.

### Outbox: çift yazmanın uygulama düzeyindeki cevabı
[DDIA özeti](hafta11-ddia-veri-yogun.md) **çift yazmanın** (dual write: aynı değişikliği iki
sisteme ayrı ayrı yazmak) sessiz tutarsızlık ürettiğini ve çözümün tek bir yazma noktası olduğunu
anlatır. **Outbox deseni** bunun uygulama düzeyindeki hâlidir: dış etki önce iş değişikliğiyle
**aynı transaction'da** bir `outbox` satırı olarak yazılır. Ayrı bir işleyici satırı alır, dış
sisteme **aynı anahtarla** iletir ve "gönderildi" diye işaretler. İşleyici çökerse satırı yeniden
gönderir. Yani teslimat **en az bir kezdir** (at-least-once). Alıcı anahtarla tekrarı ayıkladığı
için sonuç **etkin-tek-sefer** olur (exactly-once effective). Gerçek "tam bir kez" teslimat diye
bir şey yoktur; olan, *en az bir kez + tekrar ayıklama*dır (bkz. **Diyagram 2**).

## Diyagram 2 — İdempotency anahtarı + outbox akışı
Anahtar sahiplenme, iş değişikliği ve outbox satırı tek transaction'dadır. Dış etki ayrı bir
işleyiciyle, aynı anahtarı taşıyarak gider. Altta üç tekrar senaryosu var.

```svg
<svg viewBox="0 0 640 430" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İdempotency anahtarı ve outbox akışı: ajan anahtar ve argümanlarla çağırır, sunucu parmak izi hesaplar, PostgreSQL tek transaction'da anahtar, iş değişikliği ve outbox satırını yazar, outbox işleyici dış sisteme aynı anahtarla en az bir kez teslim eder">
  <defs>
    <marker id="ag-2a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="ag-2b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#25804C"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Anahtar + parmak izi + outbox: etkin-tek-sefer</text>
  <text x="320" y="42" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Sahiplenme ile etki AYNI transaction'da; dış etki ayrı işleyiciyle, aynı anahtarla.</text>

  <rect x="16" y="76" width="124" height="64" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="78" y="100" text-anchor="middle" font-size="11.5" font-weight="700" fill="#4F46E5" font-family="system-ui">Ajan / istemci</text>
  <text x="78" y="117" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">anahtar: k-7</text>
  <text x="78" y="131" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">+ argümanlar</text>

  <rect x="166" y="76" width="134" height="64" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="233" y="100" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Araç sunucusu</text>
  <text x="233" y="117" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">parmak izi =</text>
  <text x="233" y="131" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">hash(işlem, kiracı, arg)</text>

  <line x1="140" y1="108" x2="164" y2="108" stroke="#186399" stroke-width="1.6" marker-end="url(#ag-2a)"/>
  <line x1="300" y1="108" x2="324" y2="108" stroke="#186399" stroke-width="1.6" marker-end="url(#ag-2a)"/>

  <rect x="326" y="58" width="298" height="156" rx="11" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="475" y="78" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">PostgreSQL: TEK transaction</text>
  <rect x="342" y="88" width="266" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="475" y="104" text-anchor="middle" font-size="10" font-weight="600" fill="#1C3247" font-family="system-ui">1. idempotency(anahtar, parmak_izi, sonuç)</text>
  <text x="475" y="117" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">INSERT … ON CONFLICT DO NOTHING (sahiplen)</text>
  <rect x="342" y="128" width="266" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="475" y="144" text-anchor="middle" font-size="10" font-weight="600" fill="#1C3247" font-family="system-ui">2. iş tablosu değişikliği</text>
  <text x="475" y="157" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ör. bakiye, görev durumu</text>
  <rect x="342" y="168" width="266" height="34" rx="7" fill="#FFFFFF" stroke="#25804C" stroke-width="1.1"/>
  <text x="475" y="184" text-anchor="middle" font-size="10" font-weight="600" fill="#1C3247" font-family="system-ui">3. outbox satırı: yapılacak dış etki</text>
  <text x="475" y="197" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">anahtarı da taşır</text>

  <rect x="400" y="250" width="200" height="54" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="500" y="272" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Outbox işleyici (ayrı süreç)</text>
  <text x="500" y="290" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">en az bir kez teslim eder</text>
  <line x1="500" y1="214" x2="500" y2="248" stroke="#C77A17" stroke-width="1.6" marker-end="url(#ag-2b)"/>

  <rect x="140" y="250" width="200" height="54" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="240" y="272" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">Dış sistem (e-posta, ödeme)</text>
  <text x="240" y="290" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı anahtarı tanır → tekrarı ayıklar</text>
  <line x1="400" y1="277" x2="342" y2="277" stroke="#25804C" stroke-width="1.6" marker-end="url(#ag-2b)"/>

  <text x="24" y="336" text-anchor="start" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Tekrar gelince ne olur?</text>
  <text x="24" y="354" text-anchor="start" font-size="10" fill="#25804C" font-family="system-ui">• aynı anahtar + aynı parmak izi → kayıtlı sonucu döndür, yeniden çalıştırma</text>
  <text x="24" y="372" text-anchor="start" font-size="10" fill="#C77A17" font-family="system-ui">• aynı anahtar + farklı parmak izi → REDDET (anahtar başka niyete takma ad olamaz)</text>
  <text x="24" y="390" text-anchor="start" font-size="10" fill="#BE4433" font-family="system-ui">• anahtarsız, yalnız yeni RPC id → iki kez çalışır (asıl hata budur)</text>
  <text x="24" y="414" text-anchor="start" font-size="10" fill="#5D7789" font-family="system-ui">YANLIŞ sıra: kontrol et → etkiyi yap → sonucu yaz. İki işçi de "anahtar yok" görür.</text>
</svg>
```

## 3. Yeniden deneme sınıfları — önce sınıflandır, sonra dene
Yeniden deneme kodunu yazmadan önce her aracı üç sınıftan birine koy (bkz. **Diyagram 1**):

| Sınıf | Örnek | Kural |
|---|---|---|
| **Güvenli** | yan etkisiz, deterministik okuma (`hafta_detay`) | hata sınırı anlaşıldıktan sonra **yeni** RPC id ile yeniden dene |
| **Koşullu** | kalıcı idempotency anahtarlı yazma | yalnız **aynı anahtar + aynı argümanlarla** yeniden dene |
| **Güvensiz** | iş düzeyinde tekrar ayıklaması olmayan yazma | otomatik deneme **yok**; önce **mutabakat** (reconcile) |

Mutabakat, "yan etki oldu mu?" sorusunu **yetkili kaynaktan durumu yeniden okuyarak**
cevaplamaktır: kayıt var mı, bakiye değişti mi, e-posta gönderilenlerde mi? Cevaba göre ya hiçbir
şey yapmazsın ya da *bilerek* yeniden çalıştırırsın. [MCP adapter](mcp-adapter.md) §7'deki tablo
hatanın **türünü** sınıflandırır (geçici, doğrulama, iş kuralı, yetki, belirsiz yazma); bu tablo
ise aracın **yeniden denemeye ne kadar dayanıklı olduğunu** sınıflandırır. İkisi ayrı eksenlerdir:
"geçici altyapı hatası" bile güvensiz bir araçta otomatik denemeyi haklı çıkarmaz.

**Geri çekilme ve jitter.** Denemeler arasında bekleme süresi **üstel** büyür (exponential backoff)
ve bir **tavanla** sınırlanır. Her beklemeye rastgele bir pay eklenir: buna **jitter** denir. Jitter
olmazsa, sunucu düştüğünde 10.000 istemci tam aynı saniyede yeniden bağlanır ve toparlanan sunucuyu
yeniden düşürür (sürü etkisi, thundering herd). Ders 250 ms → 500 ms → 1 s … 8 s tavanı örneğini
verir; rakamlar örnektir, değişmez kural **dağıtmaktır**.

**Annotation'lar karar girdisi değildir.** MCP araç tanımında `readOnlyHint`, `destructiveHint`,
`idempotentHint` gibi ipuçları (annotation) olabilir. Resmî belirtim (2026-07-28, Tools) açıkça
söyler: istemci bunları **güvenilir sunucudan gelmedikçe güvenilmez** saymalıdır. Bir aracın
yeniden denemeye uygun olup olmadığına uygulama sözleşmesi ve sunucunun kodu karar verir. Üzerinde
`idempotentHint: true` yazan bir araç, 2026-10-26'ya kadarki `gorev_isaretle` gibi bir toggle
olabilir (§8.1).

**Çıktı sözleşmesi hata durumunda da geçerlidir.** 13-28 dersinin uyarısı: aracın yayımladığı
`outputSchema`, `isError: true` döndüğünde de bir sözleşmedir. İstemci sonucu modele vermeden önce
doğrular. Şemaya uymayan bir hata yanıtındaki `retryable: true` alanına güvenip yeniden deneme yapma.

## Diyagram 1 — Yeniden deneme karar ağacı
Soru "hata geçici mi?" değil, "bu çağrıyı tekrarlamak dünyayı iki kez değiştirir mi?" sorusudur.

```svg
<svg viewBox="0 0 640 420" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yeniden deneme karar ağacı: yan etkisi yoksa güvenli, yeni RPC id ile dene; yan etkisi var ve kalıcı idempotency anahtarı varsa koşullu, aynı anahtar ve argümanla dene; anahtar yoksa güvensiz, otomatik deneme yok, önce mutabakat">
  <defs>
    <marker id="ag-1a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
  </defs>
  <text x="320" y="24" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Yeniden denemeden önce: hangi sınıf?</text>
  <text x="320" y="42" text-anchor="middle" font-size="11" fill="#5D7789" font-family="system-ui">Hata türü değil, tekrarın dünyayı iki kez değiştirip değiştirmediği belirler.</text>

  <rect x="200" y="56" width="240" height="36" rx="9" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.3"/>
  <text x="320" y="79" text-anchor="middle" font-size="11" font-weight="600" fill="#1C3247" font-family="system-ui">Araç çağrısı başarısız / yanıt kayboldu</text>
  <line x1="320" y1="92" x2="320" y2="108" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-1a)"/>

  <polygon points="320,110 440,140 320,170 200,140" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="320" y="137" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">Yan etkisi var mı?</text>
  <text x="320" y="152" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">(saf mı, sonuçlu mu)</text>

  <polyline points="200,140 130,140 130,198" fill="none" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-1a)"/>
  <text x="160" y="132" text-anchor="middle" font-size="10" font-weight="600" fill="#25804C" font-family="system-ui">Hayır</text>
  <rect x="30" y="200" width="200" height="74" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.6"/>
  <text x="130" y="222" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">GÜVENLİ</text>
  <text x="130" y="240" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">deterministik okuma</text>
  <text x="130" y="256" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeni RPC id ile dene</text>
  <text x="130" y="268" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ör. hafta_detay, kaynak</text>

  <polyline points="440,140 470,140 470,188" fill="none" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-1a)"/>
  <text x="458" y="132" text-anchor="middle" font-size="10" font-weight="600" fill="#BE4433" font-family="system-ui">Evet</text>

  <polygon points="470,190 610,222 470,254 330,222" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.5"/>
  <text x="470" y="218" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">Kalıcı idempotency anahtarı</text>
  <text x="470" y="233" text-anchor="middle" font-size="10.5" font-weight="700" fill="#C77A17" font-family="system-ui">+ parmak izi var mı?</text>

  <polyline points="330,222 340,222 340,296" fill="none" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-1a)"/>
  <text x="318" y="262" text-anchor="middle" font-size="10" font-weight="600" fill="#25804C" font-family="system-ui">Evet</text>
  <rect x="250" y="298" width="180" height="70" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.6"/>
  <text x="340" y="320" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">KOŞULLU</text>
  <text x="340" y="338" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">AYNI anahtar + AYNI</text>
  <text x="340" y="352" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">argümanla yeniden dene</text>

  <polyline points="610,222 620,222 620,296" fill="none" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-1a)"/>
  <text x="600" y="262" text-anchor="middle" font-size="10" font-weight="600" fill="#BE4433" font-family="system-ui">Hayır</text>
  <rect x="444" y="298" width="186" height="70" rx="10" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.6"/>
  <text x="537" y="320" text-anchor="middle" font-size="12" font-weight="700" fill="#BE4433" font-family="system-ui">GÜVENSİZ</text>
  <text x="537" y="338" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">otomatik deneme YOK</text>
  <text x="537" y="352" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">önce durumu yeniden oku</text>
  <text x="537" y="364" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ör. eski (toggle) gorev_isaretle</text>

  <text x="320" y="394" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">Her sınıfta: deneme bütçesi + üstel geri çekilme + jitter · devre kesici açıksa hiç deneme yok.</text>
  <text x="320" y="411" text-anchor="middle" font-size="10" fill="#C77A17" font-family="system-ui">readOnlyHint / idempotentHint bu ağaçta girdi DEĞİL: sözleşme ve sunucu kodu karar verir.</text>
</svg>
```

## 4. Zaman: iki saat, iptal yarışı, döngü tavanı
**Tek bir zaman aşımı yetmez.** İki sınır kur:

1. **Boşta kalma süresi (idle timeout):** isteğin hiçbir yararlı etkinlik üretmeden ne kadar
   bekleyebileceği.
2. **Kesin üst sınır (maximum timeout):** istek başladığından beri geçen mutlak süre.

İlerleme bildirimi (progress) boşta kalma saatini sıfırlayabilir, ama **kesin üst sınırı asla
kaldıramaz**. Dersin örneği: boşta kalma 500 ms, üst sınır 2000 ms, ilerleme 400, 800 ve 1200
ms'de. 1500 ms'de istek hâlâ canlıdır, çünkü son ilerleme yalnız 300 ms önce geldi. 2000 ms'de ise
1999 ms'de yeni bir ilerleme gelmiş olsa bile iptal edilir (bkz. **Diyagram 3**'ün alt şeridi). İki
ince nokta:

- Sunucu bir ilerleme belirteci (progress token) alıp hiç ilerleme göndermeyebilir. Belirtecin
  varlığını **sonsuz süre** diye okuma.
- Bağlantıyı canlı tutmak için gönderilen boş SSE yorumu (`:` satırı, keepalive) ilerleme
  **değildir**. Vekillerin bağlantıyı kapatmasını önler, ama işin anlamsal boşta kalma saatini
  sıfırlamamalıdır.

**İptal bir yarıştır.** İki sıra da geçerlidir. *İptal kazanırsa:* sunucu isteği iptal edildi diye
işaretler, iş bitse bile yanıtı **bastırır**. *Tamamlanma kazanırsa:* yanıt gider, geç gelen iptal
**yok sayılır**. İstemci de terk ettiği bir isteğin geç yanıtını yok sayar, çünkü ağ gecikmesi
yüzünden iki taraf da öbürünün hangi olayı önce gördüğünü kanıtlayamaz. İptalin tel üzerindeki
biçimi taşımaya göre değişir: stdio'da `notifications/cancelled` bildirimi gönderilir, Streamable
HTTP'de o isteğin yanıt akışı **kapatılır**. İki şeyi karıştırma: tek bir isteği iptal etmek
başka, kalıcı bir görevi (`tasks/cancel`) iptal etmek başkadır. İkincisinin başarılı yanıtı yalnız
"iptal niyeti alındı" demektir; işçi bir sonraki kontrol noktasına gelene kadar çalışmaya devam
edebilir. Arayüzde "durduruldu" yazmadan önce görevin **son durumunu** sorgula.

**Geri basınç (backpressure) doğruluğun parçasıdır.** Üretici ilerlemeyi tüketicinin
okuyabileceğinden hızlı üretirse sınırsız kuyruk, yavaşlığı bellek tükenmesine çevirir. Kuyruğu
sınırla ve neyin kaybolabileceğini **önceden yaz**: ilerleme değiştirilebilirdir (yenisi eskisinin
yerini alır, birleştirilebilir, düşürülebilir); **son yanıt değiştirilemez**, asla düşürülmez.
İlerleme düşürüldüyse akışa "yetkili kaynaktan yeniden okunmalı" işareti konur. Sessiz kayıp bir
strateji değildir.

**Yeniden bağlanmak = yeniden okumak.** MCP'nin 2026-07-28 revizyonu Streamable HTTP'den SSE akışını
sürdürmeyi (`Last-Event-ID`) **kaldırdı**: kopan yanıt akışı, uçuştaki isteği kaybettirir ve
istemci onu yeni bir RPC id ile yeni bir istek olarak göndermek **zorundadır** (bkz. [MCP adapter](mcp-adapter.md)
§5 sürüm notu). Bu yüzden kopma sonrası sıra şudur: yeni istek aç, etkilenen durumu yetkili
metotlardan yeniden oku, kararlı kimliklerle tekrarları ayıkla, yanıtı kaybolan güvensiz bir
değişikliği **yeniden oynatma**. Yeniden bağlanma denemeleri de jitter'lı olur.

**Döngü tavanı ve devre kesici.** Ajan döngüsü, model araç çağırmayı bırakınca ya da harness bir
**azami tur sayısına** ulaşınca biter. Tavanı olmayan döngü, "ajan gece boyunca API çağrısı
yaktı" türünden olay raporlarının kaynağıdır. Aynı aracın arka arkaya aynı hatayı vermesi ise
**devre kesiciyle** (circuit breaker) kesilir: belirli sayıda hatadan sonra devre "açılır", bir
süre o araca hiç çağrı gitmez, sonra "yarı açık" durumda tek bir deneme yapılır. Bu, yeniden
deneme fırtınasını (retry storm) durdurur. *Şerh:* 13-01 dersi "Claude Code varsayılanı 20 tur,
OpenAI Assistants 10, Cursor 25" der ve "400 dolarlık gece" anekdotunu anar; ikisi için de kaynak
göstermez. Bu rakamları ürün varsayılanı diye alıntılama; kendi tavanını kendi görev uzunluğuna
göre koy. Harcama tavanı ve kill switch için [ajan çalışma zamanı kontrolleri](ajan-calisma-zamani-kontrolleri.md).

## 5. Dört durum ömrü — uzun ömürlü durum açık bir tutamaç altında
MCP'nin 2026-07-28 revizyonu protokol düzeyindeki oturumu (`initialize` el sıkışması,
`Mcp-Session-Id`) kaldırdı; her istek kendini tanıtır (bkz. [MCP adapter](mcp-adapter.md) §5).
Bu **durumsuz protokol** demektir, **durumsuz uygulama** değil. Alışveriş sepeti, taslak, uzun
süren rapor hâlâ var. Soru şu: bu durum nerede yaşar? 13-13 dersi dört ömrü ayırır:

| Durum | Ömrü | Nerede yaşar |
|---|---|---|
| Protokol üst verisi | tek istek | isteğin `_meta` alanı, her çağrıda yeniden doğrulanır |
| Taşıma işi | tek stdio isteği / tek HTTP yanıtı | sınırlı süreli, bellek içi koordinatör |
| Çok turlu devam (MRTR) | bir yeniden deneme dizisi | bütünlüğü korunmuş `requestState` |
| Kalıcı görev / taslak | istekler, replikalar, yeniden başlatmalar boyunca | paylaşılan depo, yetkili bir **tutamaçla** anahtarlı |

Uzun ömürlü durum için kural: sunucu bir **tutamaç** (handle: `sepet_id`, `taskId`) üretir ve
normal bir araç sonucu olarak döndürür; istemci sonraki çağrılarda onu açık argüman olarak taşır.
Tutamaç **tahmin edilemez** olur, **sahibine bağlanır** (kimlik ve kiracı), **süresi dolar** ve her
kullanımda **yeniden yetkilendirilir**. Resmî belirtimin Tools sayfası (2026-07-28, bağlayıcı olmayan
"Stateful Tools" notu) aynı şeyi söyler: kimlik doğrulamalı sunucuda tutamaç *bir addır, yetki
değildir*.

**Yapışkan oturum neden çözüm değil?** Durumu bir replikanın belleğinde tutup yük dengeleyiciyi
"aynı kullanıcı hep aynı sunucuya" diye ayarlamak (sticky routing) belirtiyi gizler. Yeniden
başlatma, dağıtım ya da devir (failover) olduğunda sıradaki istek başka bir replikaya düşer ve kayıt
yoktur. Replika belleği önbellek olabilir, doğruluk için gereken **tek** kopya olamaz.

**Tutamacı döndürmeden önce kaydet.** Sunucu, `tasks/get` o kimliği gerçekten bulabilene kadar
tutamacı döndürmemelidir. Aksi hâlde istemci geçerli görünen bir kimlik alır ve hemen "bulunamadı"
yanıtıyla karşılaşır. Bu, [DDIA özeti](hafta11-ddia-veri-yogun.md)'ndeki **kendi yazdığını okuma**
(read-your-writes) garantisinin araç düzeyindeki hâlidir: eşzamansız replikasyonlu bir depoda okuma
görünür olana kadar bekle ya da tutamacın okumasını liderden yap.

**Görev ne zaman değer?** İş sıradan bir zaman aşımını aşabiliyorsa, yürütmeyi zaten bir kuyruk
yönetiyorsa, istemcinin kendi yeniden başlatmasından sonra sonucu alması gerekiyorsa ya da iş
ortasında kullanıcıdan girdi bekleniyorsa. Ucuz, deterministik bir okuma için tutamaç, kalıcılık,
yoklama (polling) ve süre sonu **gerçek karmaşıklıktır**; ekleme. Not: belirtim değişiklik günlüğüne
göre görevler (Tasks) çekirdekten çıkıp resmî bir uzantıya taşındı. Ders, uzantının belirtiminin hâlâ
taslak yüzey olduğunu söyler; SDK'nın desteklediği sürümü sabitle.

Bu depoda: sekiz MCP aracının hepsi **tek çağrılıktır**; tutamaç gerektiren bir akış yok.
Sahiplik bağlaması ise zaten var: kullanıcıya bağlı her araç kullanıcı kimliğini (`kullaniciId`, ADR-0008) açıkça
servis katmanına geçirir, örtük bir bağlantı durumuna güvenmez.

## 6. Öner → onayla → uygula → doğrula
Geri dönüşü zor eylemler için idempotency tek başına yetmez. 15-15 ve 15-16 dersleri bunu tek bir
akışa bağlar: **öner → onayla → uygula → doğrula** (propose-then-commit). Önce sınıflandır:

- **Sonuçlu** (her zaman bu akış): geri alınamaz yazma, para hareketi, dışarıya giden iletişim,
  üretim veritabanı değişikliği, yıkıcı dosya işlemi.
- **Geri alınabilir** (bazen): yerel dosya düzenleme, test ortamı değişikliği, geri alma yolu açık yazma.
- **Okuma** (hiçbir zaman): dosya okumak, kaynak listelemek, salt-okunur API.

Akışın adımları (bkz. **Diyagram 3**):

1. **Öner (propose).** Ajan önerilen eylemi **kalıcı bir kayda** yazar: niyet (neden), veri kökeni
   (bu öneriye hangi kaynak yol açtı), dokunulan yetkiler, **etki alanı** (blast radius: en kötü
   durumda ne bozulur), **geri alma planı** ve **idempotency anahtarı**. Aynı öneri yeniden
   gönderilirse aynı kayıt döner.
2. **Onayla (commit kararı).** Onaylayan **ajanın kendisi değildir**: bir insan ya da deterministik
   bir politikadır. Yalnız "Onayla?" butonu sorgusuz onaya (rubber-stamp) döner; ders bunun yerine
   onay butonunu açmadan önce cevaplanması gereken kısa bir **kontrol listesi** önerir ("hangi
   kaynağa dokunuyor?", "etki alanı kabul edilebilir mi?", "geri alma planı var mı?").
3. **Ön koşulu kontrol et.** İdempotency çift çalışmayı önler, ama **dünyanın hâlâ onaylandığı gibi
   olduğunu** garanti etmez. Dersin örneği: "bakiye 1000'in üstündeyse A'dan B'ye 100 aktar" onaylanır,
   iş çöker, bu arada başka bir akış bakiyeyi 500'e düşürür, iş yeniden başlar. Anahtar kontrolü
   geçer, ön koşul geçmez. Ön koşulu kontrol etmezsen, onaylanan koşul (bakiye > 1000) artık
   geçerli değilken aktarımı yaparsın. Ön koşul bozuksa
   **dur ve yeniden öner**.
4. **Uygula.** Kritik sıra: önce "uçuşta" (in-flight) niyetini kaydet, sonra eylemi anahtarla
   çalıştır, "tamamlandı" işaretini **doğrulamadan sonra** koy. Dersin "çift yürütme" olayı tam
   bunun tersidir: eylem çalışır, 200 döner, durum yazılmadan süreç çöker; yeniden başlayan akış
   "onaylı ama yapılmamış" görür ve eylemi bir daha çalıştırır. Uçuşta kaydı varsa yeniden başlayan
   akış önce **doğrular**, sonra gerekiyorsa tek bir kez daha çalıştırır.
5. **Doğrula (verify).** "Araç 200 döndü" doğrulama **değildir**. Hedefi yeniden oku: `UPDATE …
   RETURNING` ile dönen satırı beklenenle karşılaştır, gönderilen e-postanın kimliğini gönderilenler
   kutusunda ara, yazdığın dosyayı geri okuyup hash'le, API'de takip `GET`'i yap.
6. **Geri al (rollback), gerekirse.** Üç tür var: **doğrudan geri alma** (in-band: `INSERT`'ten sonra
   `DELETE`), **telafi edici işlem** (compensating transaction: orijinali etkisizleştiren yeni bir
   eylem; saga deseninin adımı) ve **bant dışı** (out-of-band: bir insanı uyar, akışı durdur, bozuk
   durumu incelemeye bırak). "Geri alınamaz" bir eylem öneride **adıyla** yazılır ve daha güçlü onay ister.

Ve bu akışın yeniden deneme kuralı: **yalnız uygulama adımı yeniden denenir ve yalnız idempotent
araçla.** Öneri, onay ve doğrulama yeniden çalıştırılabilir okumalardır; uygulama değildir.

> *Şerh:* 15-15 dersi "AB Yapay Zekâ Yasası'nın 14. maddesi sorgusuz onayı açıkça dışlar" der. Bu,
> maddenin metni değil, dersin dayandığı Microsoft Agent Governance Toolkit'in **yorumudur**.
> Madde 14 yüksek riskli sistemlerde "etkili insan gözetimi" ister, ama nasıl uygulanacağını
> tarif etmez. Ders ayrıca "Anthropic'in ajan güvenliği araştırması kontrol listesine dayalı onayı
> açıkça bir önlem olarak anar" der ve bu cümle için belirli bir yer göstermez. Kontrol listesi
> mantıklı bir mühendislik önlemidir, bir düzenleyici zorunluluk ya da kanıtlanmış bir bulgu diye
> sunma. "2026 uzlaşısı" ve "her SDK aynı şekli sunuyor" ifadeleri de dersin kendi çerçevelemesidir.

## Diyagram 3 — Öner → onayla → uygula → doğrula ve uygulama adımının iki saati
Üstte akış ve iki hata kolu; altta uygulama çağrısının boşta kalma ve kesin üst sınır saatleri
(dersin örnek değerleri).

```svg
<svg viewBox="0 0 640 400" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Öner, onayla, ön koşul kontrolü, uygula, doğrula zaman çizgisi; ön koşul bozuksa dur, doğrulama başarısızsa geri al; altta uygulama adımının boşta kalma süresi her ilerlemeyle sıfırlanır, kesin üst sınır 2000 ms'de iptal eder">
  <defs>
    <marker id="ag-3a" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#186399"/></marker>
    <marker id="ag-3b" markerWidth="10" markerHeight="10" refX="7" refY="3" orient="auto"><path d="M0,0 L7,3 L0,6 Z" fill="#BE4433"/></marker>
  </defs>
  <text x="320" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Öner → onayla → uygula → doğrula</text>

  <rect x="16" y="36" width="110" height="74" rx="9" fill="#EEF0FF" stroke="#4F46E5" stroke-width="1.4"/>
  <text x="71" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#4F46E5" font-family="system-ui">1 ÖNER</text>
  <text x="71" y="72" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kalıcı kayıt: niyet,</text>
  <text x="71" y="85" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">etki alanı, geri alma</text>
  <text x="71" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">planı, anahtar</text>

  <rect x="138" y="36" width="110" height="74" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="193" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">2 ONAYLA</text>
  <text x="193" y="72" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">insan / politika,</text>
  <text x="193" y="85" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">ajanın kendisi değil;</text>
  <text x="193" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">kontrol listesi</text>

  <rect x="260" y="36" width="110" height="74" rx="9" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.4"/>
  <text x="315" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#C77A17" font-family="system-ui">3 ÖN KOŞUL</text>
  <text x="315" y="72" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">durum hâlâ</text>
  <text x="315" y="85" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">onaylandığı gibi mi?</text>
  <text x="315" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">(bakiye &gt; 1000?)</text>

  <rect x="382" y="36" width="110" height="74" rx="9" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="437" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#186399" font-family="system-ui">4 UYGULA</text>
  <text x="437" y="72" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">önce "uçuşta" yaz,</text>
  <text x="437" y="85" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">anahtarla çalıştır;</text>
  <text x="437" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">tek denenebilir adım</text>

  <rect x="504" y="36" width="120" height="74" rx="9" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="564" y="56" text-anchor="middle" font-size="11" font-weight="700" fill="#25804C" font-family="system-ui">5 DOĞRULA</text>
  <text x="564" y="72" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hedefi yeniden oku;</text>
  <text x="564" y="85" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">"200 OK" doğrulama</text>
  <text x="564" y="98" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">değil → sonra "tamam"</text>

  <line x1="126" y1="73" x2="136" y2="73" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-3a)"/>
  <line x1="248" y1="73" x2="258" y2="73" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-3a)"/>
  <line x1="370" y1="73" x2="380" y2="73" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-3a)"/>
  <line x1="492" y1="73" x2="502" y2="73" stroke="#186399" stroke-width="1.5" marker-end="url(#ag-3a)"/>

  <line x1="315" y1="110" x2="315" y2="128" stroke="#BE4433" stroke-width="1.5" marker-end="url(#ag-3b)"/>
  <rect x="226" y="130" width="178" height="40" rx="8" fill="#FBF0DE" stroke="#C77A17" stroke-width="1.3"/>
  <text x="315" y="146" text-anchor="middle" font-size="9.5" font-weight="600" fill="#C77A17" font-family="system-ui">bozuksa: DUR, yeniden öner</text>
  <text x="315" y="161" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">hiçbir şey çalıştırılmaz</text>

  <line x1="564" y1="110" x2="564" y2="128" stroke="#BE4433" stroke-width="1.5" marker-end="url(#ag-3b)"/>
  <rect x="416" y="130" width="208" height="40" rx="8" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.3"/>
  <text x="520" y="146" text-anchor="middle" font-size="9.5" font-weight="600" fill="#BE4433" font-family="system-ui">tutmazsa: GERİ AL</text>
  <text x="520" y="161" text-anchor="middle" font-size="9" fill="#1C3247" font-family="system-ui">doğrudan · telafi (saga) · bant dışı</text>

  <rect x="16" y="130" width="196" height="40" rx="8" fill="#FFFFFF" stroke="#5D7789" stroke-width="1.1"/>
  <text x="114" y="146" text-anchor="middle" font-size="9.5" font-weight="600" fill="#1C3247" font-family="system-ui">Yeniden deneme yalnız 4'te</text>
  <text x="114" y="161" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">ve yalnız idempotent araçla</text>

  <text x="24" y="200" text-anchor="start" font-size="11" font-weight="700" fill="#1C3247" font-family="system-ui">4. adımın iki saati (örnek: idle 500 ms, max 2000 ms)</text>

  <rect x="60" y="212" width="520" height="18" rx="4" fill="#FBEAE7" stroke="#BE4433" stroke-width="1.2"/>
  <text x="320" y="225" text-anchor="middle" font-size="9.5" font-weight="600" fill="#BE4433" font-family="system-ui">kesin üst sınır (max): başlangıçtan itibaren; ilerleme bunu UZATMAZ</text>
  <line x1="580" y1="206" x2="580" y2="300" stroke="#BE4433" stroke-width="2"/>
  <text x="584" y="204" text-anchor="start" font-size="9" font-weight="700" fill="#BE4433" font-family="system-ui">iptal</text>

  <rect x="60" y="240" width="104" height="16" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <rect x="164" y="240" width="104" height="16" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <rect x="268" y="240" width="104" height="16" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1"/>
  <rect x="372" y="240" width="130" height="16" rx="3" fill="#E9F4EE" stroke="#25804C" stroke-width="1" stroke-dasharray="4 3"/>
  <circle cx="164" cy="248" r="5" fill="#25804C"/>
  <circle cx="268" cy="248" r="5" fill="#25804C"/>
  <circle cx="372" cy="248" r="5" fill="#25804C"/>
  <line x1="502" y1="236" x2="502" y2="300" stroke="#C77A17" stroke-width="1.6" stroke-dasharray="4 3"/>
  <text x="498" y="272" text-anchor="end" font-size="9" fill="#C77A17" font-family="system-ui">yeni ilerleme yoksa idle 1700 ms'de dolar</text>
  <text x="320" y="288" text-anchor="middle" font-size="9.5" fill="#25804C" font-family="system-ui">● = gerçek ilerleme: boşta kalma saatini sıfırlar · keepalive yorumu sıfırlamaz</text>

  <line x1="60" y1="304" x2="580" y2="304" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="60" y1="300" x2="60" y2="308" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="190" y1="300" x2="190" y2="308" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="320" y1="300" x2="320" y2="308" stroke="#5D7789" stroke-width="1.2"/>
  <line x1="450" y1="300" x2="450" y2="308" stroke="#5D7789" stroke-width="1.2"/>
  <text x="60" y="322" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">0</text>
  <text x="190" y="322" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">500</text>
  <text x="320" y="322" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">1000</text>
  <text x="450" y="322" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">1500</text>
  <text x="580" y="322" text-anchor="middle" font-size="9" fill="#5D7789" font-family="system-ui">2000 ms</text>

  <text x="320" y="352" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">İptal bir yarıştır: iptal kazanırsa yanıt bastırılır; tamamlanma kazanırsa geç iptal yok sayılır.</text>
  <text x="320" y="370" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">İstemci de terk ettiği isteğin geç yanıtını yok sayar ve yeniden okuyarak karar verir.</text>
  <text x="320" y="390" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">Onay beklemesi saatlerce sürebilir: o bekleme bir zaman aşımı değil, kalıcı bir durumdur.</text>
</svg>
```

## 7. "Ölçüp yetmediğini görene kadar Spring + PostgreSQL yeter"
16-22 dersi, saatlerce süren ajan çalıştırmaları için **dayanıklı yürütmeyi** (durable execution)
anlatır: her adımdan sonra durumu kaydeden bir motor. İşçi çökerse kira (lease) süresi dolar,
başka bir işçi son kontrol noktasından devam eder. Bunun üç ön şartı vardır: durum
**serileştirilebilir** olur (canlı veritabanı bağlantısı taşıyan bir kapanış/closure kaydedilemez),
devam etme **deterministik** olur ve yan etkiler **idempotent** olur ya da bir tekrar ayıklama
anahtarı taşır. Bu son şart bu metnin §2'sidir. Hangi motoru seçersen seç, idempotency işini o
senin yerine yapmaz.

Dersin en değerli kısmı karşı görüştür. Ashpreet Bedi'nin "Scaling Agentic Software" yazısına göre
çoğu ekip yükü ölçmeden aşırı mühendislik yapar. Varsayılan şudur: tek bir web çatısı + PostgreSQL;
her ajan çalıştırması bir satırdır, durum yerinde ve iyimser eşzamanlılıkla (optimistic
concurrency) güncellenir; arka plan işleri basit bir işçiyle yürür; yeniden deneme politikası
uygulama kodundadır. Bizim yığınımızda bunun karşılığı:

- **İdempotency + iş + outbox tek transaction'da** (§2): Spring `@Transactional` + PostgreSQL.
- **Outbox işleyici:** zamanlanmış bir görev (`@Scheduled`) satırları
  `SELECT … FOR UPDATE SKIP LOCKED` ile alır; birden çok replika aynı satırı kapışmadan paylaşır.
- **İyimser kilit:** Spring Data'da `@Version` sütunu; "oku–değiştir–yaz" çakışmasını kayıp
  güncelleme yerine görünür bir hataya çevirir (§8'de neden önemli olduğunu göreceksin).
- **Kontrol noktası:** ajan çalıştırması tablosunda adım numarası + serileştirilmiş durum.

Bu depo PostgreSQL'i zaten seçti (ADR-0014, çok kullanıcılı site). Bir iş akışı motoruna (Temporal,
LangGraph runtime, Restate) geçmek **yeni bir bağımlılıktır**. Bu projenin kuralı gereği önce bir
ADR ister. Hafta 10'un advice process'i de tam bu karar içindir: etkilenenlere danış, gerekçeyi
ve "hangi ölçüm bu kararı geri aldırır" sorusunu kayda geçir. Motoru haklı çıkaran somut sorunlar
derste sayılır: saatlerce ya da günlerce süren insan onayı beklemeleri, bölgeler arası koordinasyon,
karmaşık telafi ve yeniden deneme politikaları. Uzun yaşayan çalıştırmalar için bir not daha: her
dağıtımda uçuştaki çalıştırmaları öldürmemek için eski ve yeni sürümü birlikte çalıştırmak gerekir
(ders bunu Anthropic'in çok-ajanlı araştırma sisteminden "rainbow deployment" diye aktarır).

> *Şerh:* Ders, sadeliğin "~100 eşzamanlı ajan çalıştırmasının altında" çoğu zaman yeteceğini
> söyler. Bu bir ölçüm değil, sezgisel bir eşiktir; kendi yük ölçümünün yerini tutmaz. Dersin
> "LangGraph runtime, Nisan 2026'nın referans üretim tasarımıdır" cümlesi bir çerçevelemedir;
> dayandığı kaynak LangChain'in kendi yazısıdır (satıcı-birincil). "Bir iş parçacığı ~1 MB, 10.000
> çağrıda 10 GB" hesabı kaba bir yaklaşıktır; JVM'de sanal iş parçacıkları (virtual threads) bu
> hesabı zaten değiştirir. Doğru soru "hangi motor popüler" değil, "**hangi ölçtüğüm sorun** bu
> motoru gerektiriyor" sorusudur.

## 8. Laboratuvar: `gorev_isaretle` tekrar çağrılınca ne olur? (önce / sonra)
Bu depodaki araç gerçekten var. ADR-0007'nin sekiz MCP aracından biridir (`mcp/OgretmenMcpAraclari`).
Bu laboratuvarın iki yarısı var. **Önce:** 2026-10-26'ya kadar araç bir "çevirme" (toggle) idi; o
kodu ve üç senaryosunu öğretici olduğu için aynen koruyoruz. **Sonra:** aynı gün ADR-0016 ile
düzeltildi; düzeltmeyi ve düzeltme sırasında çıkan, mock'lu testin gizlediği ikinci hatayı okuyacaksın.

### 8.1 Önce — 2026-10-26'ya kadar böyleydi
Aracın açıklaması şuydu: *"Bir günlük görevin tamamlanma durumunu tersine çevirir (toggle) ve yeni
durumu döndürür."* Araç üç tamsayı (`hafta`, `gun`, `gorev_index`) alıyor, kullanıcı kimliğini istek
bağlamından (`McpKullaniciKimligi`) çözüyor ve `service/IlerlemeService.gorevToggle`'ı çağırıyordu.
Web arayüzü de aynı metoda gidiyordu (`POST /api/gorev/toggle`, `web/ProgramController`). Servisin
çekirdeği şuydu:

```java
public boolean gorevToggle(String kullaniciId, int hafta, int gun, int gorevIndex) {
    try {
        return okuVeCevir(kullaniciId, hafta, gun, gorevIndex);
    } catch (DataIntegrityViolationException yarisCakismasi) {
        return okuVeCevir(kullaniciId, hafta, gun, gorevIndex);   // bir kez yeniden oku-çevir
    }
}
private boolean okuVeCevir(...) {
    GorevIlerlemeKaydi kayit = ilerlemeRepo.findBy...(kullaniciId, hafta, gun, gorevIndex)
            .map(mevcut -> mevcut.durumla(!mevcut.tamamlandi()))       // var: tersine çevir
            .orElseGet(() -> GorevIlerlemeKaydi.yeni(..., true));       // yok: "tamam" olarak oluştur
    return ilerlemeRepo.save(kayit).tamamlandi();
}
```

Tabloda `(kullanici_id, hafta, gun, gorev_index)` üzerinde tekil bir indeks var
(`ux_gorev_ilerleme_kullanici`, `schema.sql`). Metotta `@Transactional` yok, kayıtta `@Version`
alanı yok. Kod okunarak çıkarılan davranış şöyleydi:

| Senaryo | Ne olurdu | Sonuç |
|---|---|---|
| Yanıt kayboldu, istemci/model **sırayla** yeniden çağırdı | ikinci çağrı mevcut kaydı okur, `!true = false` yazar | görev **geri açılır**; araç `tamamlandi:false` döner |
| Kayıt **varken** iki çağrı **eşzamanlı** | ikisi de aynı eski değeri okur, ikisi de aynı yeni değeri yazar | **kayıp güncelleme**: iki çevirme, net **bir** çevirme |
| Kayıt **yokken** iki çağrı eşzamanlı (çift tık) | biri `true` ekler, öbürü tekil indekse çarpar; `catch` yeniden okuyup `false` yazmalıydı | kâğıt üstünde net **sıfır** çevirme — ama §8.3'e bak: bu `catch` gerçek veritabanında hiç çalışmıyordu |

Okuma: aynı iki çağrı, **zamanlamaya göre** farklı sonuçlar verebiliyordu. İkinci satır,
[DDIA özeti](hafta11-ddia-veri-yogun.md)'ndeki "oku–değiştir–yaz" kayıp güncellemesinin bizim
kodumuzdaki birebir örneğiydi. Asıl ders ise birinci satırdadır: **toggle doğası gereği idempotent
değildir.** "Çevir" komutunu iki kez çalıştırmak, bir kez çalıştırmakla aynı sonucu bırakmaz. §3'ün
ağacında araç doğrudan **güvensiz** kutusuna düşüyordu. Model zaman aşımından sonra "işaretlenmedi
galiba" deyip yeniden çağırırsa, öğrencinin tamamladığı görevi **geri açar** ve bunu
`tamamlandi:false` diye dürüstçe raporlardı. Yani yalan söylemez, ama niyeti tersine çevirirdi.
[MCP adapter](mcp-adapter.md) §7 bu riski "belirsiz yazma durumu" diye adlandırmıştı; borç listesi
(`docs/tech-debt-tracker.md`) de açık kalem olarak tutuyordu.

### 8.2 Sonra — ADR-0016 karar 1: "çevir" yerine "ayarla"
Düzeltme üç katmanda yapıldı (ADR-0016, `docs/tech-debt-tracker.md` "Kapanan borçlar" #9):

- **Servis:** `gorevToggle` **kaldırıldı**; yerine
  `gorevDurumuAyarla(kullaniciId, hafta, gun, gorevIndex, tamamlandi)` geldi. Hedef durum artık açık
  bir argüman. Kayıt yoksa o durumda oluşturulur, varsa durum yazılır; kayıt **zaten hedefteyse hiç
  yazma yapılmaz** (tamamlanma zamanı `tarih` tekrarla ezilmesin diye).
- **Web:** `POST /api/gorev/toggle` **kaldırıldı**; yerine `POST /api/gorev/durum`
  `{hafta, gun, gorevIndex, tamamlandi}` geldi. `tamamlandi` zorunludur: istekte yoksa sessizce
  `false`'a düşmesin diye kutulu `Boolean` tutulur ve eksikse **400** döner.
- **MCP:** `gorev_isaretle` adı korundu; opsiyonel `hedef_durum` (boolean) parametresi eklendi,
  verilmezse `true`. Açıklama artık açıkça *"Araç idempotent: aynı çağrıyı tekrarlamak (ör. zaman
  aşımı sonrası) sonucu değiştirmez, işareti geri almaz"* diyor. Adın neden korunduğunu ve bunun §7.1
  kuralıyla nasıl çeliştiğini [MCP adapter](mcp-adapter.md) §7 tartışıyor.

"Şu duruma getir" komutu doğası gereği idempotenttir: N kez çalıştırmak, bir kez çalıştırmakla aynı
sonucu bırakır. Bu yüzden araç **koşullu** değil, doğrudan **güvenli** sınıfa geçti; idempotency
anahtarı gerekmedi. İdempotency anahtarı, doğası gereği tekrarlanamayan etkiler (para çekmek,
e-posta göndermek, yeni satır eklemek) için gerekir. Eski üç senaryo yeni kodda şöyle:

| Senaryo | Şimdi ne olur |
|---|---|
| Yanıt kayboldu, aynı çağrı sırayla tekrarlandı | ikinci çağrı kaydı zaten hedefte bulur, yazmaz; yine `tamamlandi:true` döner |
| Kayıt varken aynı hedefle iki eşzamanlı çağrı | ikisi de aynı değeri yazar ya da hiç yazmaz; kaybolan bir "niyet" yok |
| Kayıt yokken eşzamanlı ilk çağrılar | kaybeden INSERT tekil indekse çarpar, bir kez yeniden okur, mevcut satırı bulur, en fazla UPDATE yapar — sonuç hedeftir |

İki şeyi dürüstçe not et. (1) Eski metnin önerdiği **atomik upsert** (`INSERT … ON CONFLICT DO
UPDATE` / H2 `MERGE`) yapılmadı; kod hâlâ oku–sonra–yaz, ama "ayarla" anlamında yarışın kaybedeni de
aynı hedefi yazdığı için kayıp güncelleme artık bir niyeti yok etmez. (2) **Zıt hedefli** iki
eşzamanlı çağrıda (biri `true`, biri `false`) son yazan kazanır; bu bir idempotency sorunu değil,
iki farklı niyetin çatışmasıdır.

### 8.3 Mock'un gizlediği hata: istisna sarma
Düzeltme sırasında eski `catch`'in kendisinin de bozuk olduğu ortaya çıktı. Eski kod
`DataIntegrityViolationException` yakalıyordu. Ama Spring Data JDBC, `save()` sırasındaki tekil indeks
çakışmasını `DbActionExecutionException`'a **sarar**; bu düz bir `RuntimeException`'dır ve
`DataIntegrityViolationException`'ın alt sınıfı değildir. Yani "yarışta yeniden oku" yolu **gerçek
veritabanında hiç çalışmıyordu**: eşzamanlı ilk çağrının kaybedeni 500 alıyordu.

Bunu neden önceki testler görmedi? `IlerlemeServiceYarisTest` deposu (repository) mock'luydu ve
mock'a `save()` çağrısında doğrudan `DuplicateKeyException` fırlatması söylenmişti. Test, gerçek
çerçevenin **asla fırlatmadığı** bir istisnayı fırlatan bir dünyada yeşildi. Hatayı yeni
`IlerlemeServiceEszamanliTest` yakaladı: gerçek H2 + gerçek tekil indeks üzerinde 8 iş parçacığı
aynı anda `gorevDurumuAyarla(..., true)` çağırır (test kasıtlı olarak işlemsizdir, her iş parçacığı
kendi bağlantısında commit eder) ve hiçbirinin istisna fırlatmadığını, tek satır kaldığını, son
durumun `true` olduğunu doğrular. Çare: servis artık istisnanın **neden zincirini** (`getCause()`)
tarayıp içinde `DataIntegrityViolationException` arar; çakışma değilse istisnayı yeniden fırlatır
(`cakismaDisiHataYenidenDenenmez` testi). Mock'lu test de gerçek sarmalı
(`DbActionExecutionException` içinde `DuplicateKeyException`) fırlatacak biçimde güncellendi.

> **Şerh — "kanıtı kanıtla".** Mock, senin çerçeve hakkındaki **varsayımını** test eder, çerçevenin
> kendisini değil. "Yarışta yeniden oku" yolunun bir testi vardı, Javadoc'u vardı, borç listesinde
> "kabul edilen artık risk" diye anılıyordu — ve hiç çalışmıyordu. Bir koruma yolunun var olduğunu
> iddia etmeden önce onu **gerçek bileşenle** en az bir kez tetikle: burada bu, gerçek veritabanı +
> gerçek eşzamanlılık demekti. Eşzamanlılık ve istisna çevirisi, mock'ların en çok yalan söylediği
> iki yerdir.

**Kendin bul.** `git log -S gorevToggle --oneline` komutu bu adı ekleyen ya da silen commit'leri
listeler; en üstteki (`[faz-9] ADR-0016: görev işaretleme idempotent hedef durum …`), toggle'ın
kaldırıldığı commit'tir.
`git show <hash> -- src/main/java/tr/velzon/aiogretmen/service/IlerlemeService.java` ile diff'i oku:
`okuVeCevir`'in nasıl `okuVeAyarla`'ya, `catch (DataIntegrityViolationException …)`'ın nasıl
`yarisCakismasi(e)` taramasına dönüştüğünü satır satır gör.

### 8.4 Komşu araç: `statik_quiz_kaydet` — bilinçli kabul edilen risk
Aynı soruyu komşu araca da sor. `statik_quiz_kaydet` her çağrıda yeni bir `quiz_sonuc` satırı ekler
(`id` kimlik sütunu, iş anahtarı üzerinde tekillik yok). Yanıtı kaybolan bir çağrının yeniden
denenmesi aynı sonucu **iki satır** yapar. Bu, "ayarla"ya çevrilemeyen bir eklemedir; teknik çare §2
olurdu: istemcinin ürettiği bir deneme kimliği (anahtar) + parmak izi.

ADR-0016 karar 2 bunu **değiştirmedi** ve gerekçesini yazdı: `quiz_sonuc` bilinçli olarak
yalnız-ekle bir deneme günlüğüdür (her deneme bir satır); çift satır yalnız MCP istemcisinin aynı
çağrıyı tekrar göndermesiyle oluşur; MCP sunucuları 2026-10-21'de kapandı ve istemci yalnız yerel;
etkisi istatistikte bir fazla denemedir. Önlemek bir idempotency anahtarı kolonu + araç sözleşmesi
değişikliği ister — kazançtan pahalı. **Yeniden açma koşulu:** MCP sunucusu yeniden uzakta
(`mcp-http`) çalıştırılırsa ya da quiz denemesi bir değerlendirme/sıralamaya girerse.

Buradaki ders, iki aracın aynı soruya **farklı cevap** almasıdır. Toggle bir öğrencinin bitirdiği
işi geri açıyordu (görünür zarar, ucuz düzeltme) → düzeltildi. Quiz çift satırı istatistiği bir
deneme şişirir (küçük zarar, pahalı düzeltme) → yazılı koşulla kabul edildi. Borç değil, **karar**:
kabulün kendisi ve onu geri açacak koşul belgededir.

## Öz-sınav (kendini test et)
1. JSON-RPC `id` neden bir idempotency anahtarı yerine geçmez? İstemci `id: 41` ile gönderip
   `id: 42` ile yeniden denediğinde sunucu neyi bilemez?
2. Parmak izi neden gerekir? "Aynı anahtar + farklı argüman" isteği kabul edilseydi hangi hata
   sessizce oluşurdu?
3. "Kontrol et → etkiyi yap → sonucu yaz" sırası iki ayrı yoldan nasıl çift yürütme üretir?
   Tek transaction bunlardan hangisini çözer, dış bir e-posta gönderimi için neden yetmez?
4. Outbox deseni "tam bir kez teslimat" sağlar mı? Ne sağlar? [DDIA özeti](hafta11-ddia-veri-yogun.md)'ndeki
   çift yazma sorunuyla nasıl ilişkilidir?
5. İdempotency anahtarını neden model değil, harness ya da sunucu sabitlemeli? Anahtara zaman
   damgası eklemek neyi bozar?
6. Üç yeniden deneme sınıfını birer örnekle ver. "Geçici altyapı hatası" bir aracı neden otomatik
   olarak "yeniden denenebilir" yapmaz?
7. Boşta kalma süresi ile kesin üst sınırın farkı nedir? İlerleme bildirimi hangisini sıfırlar?
   Bir keepalive yorumu hangisini sıfırlamalıdır?
8. "Durumsuz protokol ≠ durumsuz uygulama" cümlesini açıkla. Uzun ömürlü durum neden yapışkan
   oturumda değil, açık bir tutamaç altında yaşamalı? Tutamacı neden kaydetmeden döndürmemelisin?
9. Öner→onayla→uygula→doğrula akışında idempotency anahtarı varken ön koşul kontrolü neden yine
   gerekir? "200 OK" neden doğrulama değildir?
10. Eski (2026-10-26 öncesi, toggle) `gorev_isaretle` için: yanıtı kaybolan bir çağrı sırayla
    yeniden denenirse ne olurdu? Kayıt varken iki çağrı eşzamanlı gelirse ne olurdu? ADR-0016'daki
    hangi değişiklik aracı "güvenli" sınıfa taşıdı, ve neden bunun için idempotency anahtarı gerekmedi?
11. **Şerh sorusu:** Ders "Claude Code varsayılanı 20 tur, Cursor 25" der, "AB Yapay Zekâ Yasası
    Madde 14 sorgusuz onayı dışlar" der ve "~100 eşzamanlı çalıştırmaya kadar sadelik yeter" der.
    Bu üç cümleyi birincil kaynak, yorum ve kaynaksız iddia diye ayır. Bir mimar bunlardan hangisini
    bir ADR'ye gerekçe olarak yazabilir?
12. Eski `gorevToggle`'ın "yarışta yeniden oku" `catch`'i neden gerçek veritabanında hiç
    çalışmıyordu? Mock'lu `IlerlemeServiceYarisTest` bunu neden yakalayamadı, hangi test yakaladı?
13. `gorev_isaretle` düzeltildi ama `statik_quiz_kaydet` bilinçli olarak düzeltilmedi. ADR-0016'nın
    bu iki karar arasındaki farkı gerekçesi nedir, ve hangi koşulda quiz kararı yeniden açılır?

### Cevaplar
1. `id` yalnız mesajı yanıtıyla eşler; her yeni istekte değişir. Sunucu 41 ile 42'nin **aynı iş
   niyeti** (aynı ödeme) olduğunu bilemez, ikisini de çalıştırır.
2. Anahtar yanlışlıkla başka bir işlem için yeniden kullanılırsa, ikinci (farklı) işlem "tekrar"
   sanılıp yutulur ve ilk işlemin sonucu döner. Parmak izi bu takma adı yakalayıp reddeder.
3. (a) İki işçi aynı anda "anahtar yok" görür, ikisi de etkiyi yapar; (b) etki yapıldıktan sonra,
   sonuç yazılmadan çökme olur, yeniden denemede anahtar yine yoktur. Tek transaction ikisini de
   **veritabanı içindeki** etki için çözer. E-posta transaction'ın dışındadır: outbox ya da alıcının
   aynı anahtarı tanıması gerekir.
4. Hayır. En az bir kez teslimat + alıcıda anahtarla tekrar ayıklama = **etkin-tek-sefer** sağlar.
   Çift yazmanın (iki sisteme ayrı ayrı yazmak) uygulama düzeyindeki çaresidir: dış etki önce aynı
   transaction'da tek bir yazma noktasına (outbox satırı) iner.
5. Model çağrıyı yeniden ürettiğinde yeni bir anahtar uydurabilir; koruma boşa çıkar. Anahtar iş
   niyetinden (öneri kimliği, `hash(akış, eylem_imzası)`) türetilmelidir. Zaman damgası her denemede
   değişir, yani her deneme "yeni niyet" olur ve eylem iki kez çalışır.
6. Güvenli: `hafta_detay` okuması (ve 2026-10-26'dan beri "ayarla" anlamındaki `gorev_isaretle`).
   Koşullu: anahtarlı ödeme. Güvensiz: eski, anahtarsız `gorev_isaretle` toggle'ı. Hata türü ile yeniden denemeye dayanıklılık ayrı eksenlerdir: geçici bir 503 bile, yan
   etkinin olup olmadığını belirsiz bırakabilir.
7. Boşta kalma süresi son yararlı etkinlikten beri geçen süreyi, kesin üst sınır başlangıçtan beri
   geçen süreyi ölçer. İlerleme yalnız boşta kalma saatini sıfırlar. Keepalive yorumu hiçbirini
   sıfırlamamalıdır, çünkü yalnız taşıma içindir.
8. 2026-07-28 revizyonunda protokol oturumu yok, ama uygulamanın sepeti/taslağı/görevi var. Yapışkan
   oturum, devir ya da yeniden başlatmada başka replikaya düşen istekle birlikte kaybolur. Tutamaç
   paylaşılan depoda, sahibine bağlı ve süreli yaşar. Kaydetmeden döndürülen tutamaç, hemen ardından
   gelen okumada "bulunamadı" verir (kendi yazdığını okuma ihlali).
9. Anahtar çift çalışmayı önler, ama onaydan sonra dünya değişmiş olabilir (bakiye 500'e düştü).
   Ön koşul, durumun hâlâ onaylandığı gibi olduğunu kontrol eder. "200 OK" yalnız çağrının döndüğünü
   söyler; doğrulama hedefi yeniden okumaktır (`RETURNING`, takip `GET`'i, geri okuma).
10. Sırayla yeniden deneme görevi **geri açardı** (`false`). Eşzamanlı iki çağrı kayıp
    güncellemeyle tek bir çevirmeye inerdi. ADR-0016 hedef durumu açık argüman yaptı ("ayarla":
    `gorevDurumuAyarla(..., tamamlandi)`, `POST /api/gorev/durum`, MCP `hedef_durum`). "Şu duruma
    getir" doğası gereği idempotent olduğu için anahtar gerekmedi. Atomik upsert yapılmadı; set
    anlamında yarışın kaybedeni de aynı hedefi yazdığı için gerekmedi. Ad korundu — bunun bedeli ve
    gerekçesi [MCP adapter](mcp-adapter.md) §7'de.
11. Madde 14'ün metni birincil kaynaktır, ama "sorgusuz onayı dışlar" okuması Microsoft
    toolkit'inin **yorumudur**. "20 / 25 tur" kaynaksız bir iddiadır. "~100 eşzamanlı" bir yazarın
    sezgisel eşiğidir. ADR'ye gerekçe olarak ancak kendi ölçümün ve maddenin kendi metni girer;
    yorum "danışılan görüş", rakamlar ise "doğrulanacak hipotez" diye yazılır.
12. Spring Data JDBC tekil indeks çakışmasını `DbActionExecutionException`'a sarar; bu
    `DataIntegrityViolationException` değildir, `catch` onu hiç yakalamaz ve istek 500 ile düşerdi.
    Mock'lu test `save()`'e doğrudan `DuplicateKeyException` fırlattırıyordu — gerçek çerçevenin
    asla fırlatmadığı bir istisna. Hatayı gerçek H2 + tekil indeks üzerinde 8 eşzamanlı çağrı yapan
    `IlerlemeServiceEszamanliTest` yakaladı. Çare: neden zincirini taramak. Ders: mock varsayımını
    test eder; koruma yolunu gerçek bileşenle en az bir kez tetikle.
13. Toggle görünür zarar veriyordu (bitmiş görev geri açılır) ve düzeltmesi ucuzdu. Quiz çift satırı
    yalnız-ekle deneme günlüğünde bir fazla deneme demek; MCP sunucuları kapalı, istemci yalnız yerel;
    önlemek anahtar kolonu + sözleşme değişikliği ister. Yeniden açma koşulu: MCP sunucusu yeniden
    uzakta (`mcp-http`) çalışırsa ya da quiz denemesi bir değerlendirme/sıralamaya girerse.

## Kaynak ve lisans
- **AI Engineering from Scratch** — Rohit Ghumare, github.com/rohitg00/ai-engineering-from-scratch,
  MIT Lisansı, "Copyright (c) 2026 Rohit Ghumare". Kullanılan dersler:
  - Faz 13 (Tools & Protocols): **29 MCP Reliability, Cancellation, and Flow Control** (ana kaynak),
    13 MCP Tasks Extension, 01 The Tool Interface, 03 Parallel and Streaming Tool Calls,
    28 MCP Tool Contracts and Content, 09 MCP Transports.
  - Faz 16 (Multi-Agent & Swarms): 22 Production Scaling — Queues, Checkpoints, Durability.
  - Faz 15 (Autonomous Systems): 15 Human-in-the-Loop: Propose-Then-Commit, 16 Checkpoints and Rollback.
- **Model Context Protocol belirtimi, 2026-07-28 revizyonu** (modelcontextprotocol.io, 2026-10-25'te
  okundu): değişiklik günlüğü (oturum ve `initialize` kaldırıldı, `Last-Event-ID` sürdürme kaldırıldı,
  görevler resmî uzantıya taşındı) ve Tools sayfası (annotation'ların güvenilmez sayılması, "Stateful
  Tools" tutamaç notu). Bu metindeki MCP sürüm bilgileri dersten değil, doğrudan bu sayfalardan alındı.
- **Bu deponun kendi vakası:** `mcp/OgretmenMcpAraclari.java`, `service/IlerlemeService.java`,
  `types/GorevIlerlemeKaydi.java`, `src/main/resources/schema.sql` (`ux_gorev_ilerleme_kullanici`),
  `docs/tech-debt-tracker.md`, ADR-0007, ADR-0008, ADR-0014. Kod 2026-10-25'te okundu; §8.1'deki
  davranış tablosu koddan çıkarıldı, çalıştırılarak değil. §8.2–§8.4: ADR-0016,
  `web/ProgramController.java`, `IlerlemeServiceEszamanliTest`, `IlerlemeServiceYarisTest`
  (2026-10-26'da okundu).
- **Uyarlama notu:** Bu metin bir çeviri değil, sentezdir. Dersler Python simülatörleriyle model
  *kuran* okura yazılmıştır; burada modeli *kullanan* Java/Spring mimarı için yeniden kurgulandı.
  Ders kodu kopyalanmadı; Java/SQL parçaları bu metin için yazılmış örneklerdir. Derslerdeki
  kaynaksız rakamlar ve satıcı çerçevelemeleri metin içinde *Şerh* olarak işaretlendi.

### Bu programdaki ilgili kaynaklar
- [`mcp-adapter.md`](mcp-adapter.md): araç arayüzü, hata sözleşmesi, sürümleme; bu metnin Hafta 9 tarafı.
- [`hafta11-ddia-veri-yogun.md`](hafta11-ddia-veri-yogun.md): kayıp güncelleme, kendi yazdığını okuma,
  çift yazma ve CDC; bu metnin veri tarafı.
- [`ajan-calisma-zamani-kontrolleri.md`](ajan-calisma-zamani-kontrolleri.md): harcama tavanı, kill
  switch, dayanıklı yürütme ve kalıcı insan onayı beklemesi.
- [`ajan-mimarisi-desenleri.md`](ajan-mimarisi-desenleri.md): ajan döngüsü, durma ve "yan etki yalnız
  deterministik adımda" kuralı.
- [`api-tasarim-grpc.md`](api-tasarim-grpc.md): REST düzeyinde idempotentlik (GET/PUT/DELETE ve POST farkı).
