---
slug: hazirlik-haritasi-havacilik-ilani
baslik: "Hazırlık Haritası: Havacılık İlanının Sökümü ve Mülakat Stratejisi"
kademe: Temel
sira: 246
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Bir pilot, hiç inmediği bir havalimanına yaklaşırken paniklemez. Çünkü elinde bir yaklaşma haritası vardır: hangi irtifada döneceği, hangi frekansı dinleyeceği, pist görünmezse ne yapacağı önceden yazılıdır.

Teknik mülakat hazırlığı da aynı mantıkla çalışır. Elindeki ilan dört cümleliktir: "Java ve Spring Boot teknolojilerinde güçlü deneyim", "React ile çalışmış", "yazılım mimarisi konusunda çözüm üretebilen", "yenilikçi projelerde sorumluluk almaktan keyif alan". Mülakatta sana bu cümleler sorulmaz; bu cümlelerin **arkasındaki somut konular** sorulur.

Bu konu üç iş yapar. İlandaki her maddeyi modüldeki konu numaralarına çevirir. "İş ortağı ilanı" gerçeğinin stratejiyi nasıl değiştirdiğini anlatır. Yedi günü nasıl böleceğini söyler. Teknik derinlik burada değil, işaret edilen konulardadır; burada olan şey haritanın kendisidir.

### Temel kavramlar

- **İlan maddesi → konu eşlemesi:** ilandaki genel başlığın mülakatta karşılığı olan somut soru kümesi. Görüşmeci "Spring Boot biliyor musun?" demez; "`@Transactional` aynı sınıf içinden çağrıda neden çalışmaz?" der.
- **İş ortağı (danışmanlık) ilanı:** ilanı veren firma seni kendi bünyesi için değil, "lider iş ortağı" dediği bir **müşteri firma** için arıyor. Hedef firma ilanda yazmıyor ve bilinmiyor.
- **İki aşamalı görüşme:** iş ortağı ilanlarında yaygın düzen — önce danışman firmanın eleme görüşmesi, sonra müşteri firmanın teknik/alan görüşmesi. İki görüşmenin ölçtüğü şeyler farklıdır.
- **Akıllı soru listesi:** hedef firma bilinmediği için görüşmede **senin** soracağın, işin gerçeğini açan sorular: hangi ürün, hangi ekip, ne kadar eski (legacy) kod.
- **Köprü cevap:** deneyimin olmadığı alanda önce dürüstlük ("yapmadım"), sonra en yakın gerçek deneyimini o alana bağlamak. Olmayan deneyimi varmış gibi anlatmak değildir.
- **Bağlam-ölçüt-koşul kalıbı:** "hangisi daha iyi?" sorularının cevap iskeleti — önce bağlamı soran bir-iki soru, sonra ölçütler, sonra koşullu cevap.
- **Sesli düşünme (thinking aloud):** cevaba giderken zihinsel adımları duyulur kılmak; görüşmeci çoğu zaman sonucu değil düşünme biçimini ölçer.
- **STAR:** davranışsal soruların anlatım iskeleti — Durum, Görev, Eylem, Sonuç. Ayrıntısı `195` numaralı konudadır.

### Nasıl çalışır?

İlanın dört maddesini tek tek sökelim; her maddenin arkasında beklenen cevabın tonu farklıdır:

- **"Java ve Spring Boot teknolojilerinde güçlü deneyim":** en ağır madde budur. "Güçlü" kelimesi tuzaklı soru demektir. Java çekirdeği `1`-`36`, güncel Java özellikleri `37`-`40`, eşzamanlılık ve JVM `45`-`65`. Spring tarafında kap ve bağımlılık enjeksiyonundan işlem yönetimine `68`-`84`, güvenlik `86`-`87`. Elenme riski en yüksek küme buradadır: nesnenin kendi metodunu kendi içinden çağırması ve `@Transactional` (`83`), N+1 sorgu sorunu (`82`), `equals`/`hashCode` (`7`). Bu maddede kesin ve net bilgi beklenir.
- **"React ile çalışmış":** ilan "uzman" demiyor, "çalışmış" diyor. Beklenen şey, çekirdek kavramları savunabilmektir. SPA, çerçeve karşılaştırması, CORS ve API sözleşmesi `208`-`213` ön yüz bloğundadır. React'in kendisi `249` numaralı konudadır: bileşen, props/state, hook'lar, sanal DOM ve Spring Boot ile birlikte yaşama.
- **"Yazılım mimarisi konusunda çözüm üretebilen":** bu, açık uçlu tasarım sorusu demektir. SOLID `131`-`136`, mimari ve sistem tasarımı `131`-`144` (özellikle `141` beş adımlı tasarım iskeleti), mikroservis ödünleşmeleri `95`-`105`. Burada beklenen ton taraf tutmak değildir; ödünleşme (trade-off) konuşabilmektir.
- **"Yenilikçi projelerde sorumluluk almaktan keyif alan":** davranışsal sorular — sahiplik aldığın, belirsizlikte karar verdiğin somut bir hikâye. İskelet için `195` (STAR).
- İlanda yazmayan ama sektör ilanı olduğu için sorulması muhtemel beşinci madde: **havacılık alan bilgisi**. Yolcu yolculuğu, PNR, e-bilet, GDS/NDC `247`; bu kavramların mühendislik karşılığı `248`.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Havacılık ilanındaki maddelerin mülakatta sorulan konulara eşlenmesi">
  <defs>
    <marker id="havmap-map-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İlan ne yazıyor → mülakatta fiilen ne sorulur</text>
  <text x="139" y="46" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">İLANDAKİ MADDE</text>
  <text x="452" y="46" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">SORULAN KONU (modül numaraları)</text>
  <rect x="24" y="58" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="85" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Java + Spring Boot (güçlü)</text>
  <rect x="268" y="58" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">çekirdek, @Transactional tuzakları, N+1, eşzamanlılık</text>
  <text x="452" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">1-36 · 37-40 · 45-65 · 68-84 · 86-87</text>
  <path d="M256 80 L266 80" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havmap-map-arw)"/>
  <rect x="24" y="108" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="135" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">React ile çalışmış</text>
  <rect x="268" y="108" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">bileşen, props/state, hook, CORS, API sözleşmesi</text>
  <text x="452" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">208-213 · 249</text>
  <path d="M256 130 L266 130" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havmap-map-arw)"/>
  <rect x="24" y="158" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="185" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Yazılım mimarisi</text>
  <rect x="268" y="158" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">SOLID, tasarım iskeleti, mikroservis takasları</text>
  <text x="452" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">131-136 · 141 · 142 · 95-105</text>
  <path d="M256 180 L266 180" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havmap-map-arw)"/>
  <rect x="24" y="208" width="230" height="44" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="139" y="235" text-anchor="middle" font-size="12" font-weight="700" fill="#B07D20" font-family="system-ui">Havacılık sektörü (örtük)</text>
  <rect x="268" y="208" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="226" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">PNR, e-bilet, GDS/NDC, teknik desenler + davranışsal</text>
  <text x="452" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">247 · 248 · 195</text>
  <path d="M256 230 L266 230" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#havmap-map-arw)"/>
</svg>
```

### İş ortağı ilanı: iki aşamalı görüşme ve akıllı sorular

Bu ilan bir danışmanlık / iş-ortağı ilanıdır. Yani seni arayan firma ile çalışacağın firma büyük olasılıkla aynı değildir. Yaygın düzen iki aşamalıdır.

**Birinci aşama** danışman firmanın eleme görüşmesidir: CV doğrulama, genel teknik tarama, iletişim ve beklenti uyumu. **İkinci aşama** müşteri firmanın görüşmesidir: derin teknik sorular ve kendi sistemine özgü senaryolar.

Hedef firma bilinmediği için firmaya özel hazırlık yapamazsın. Bu bir eksik değildir; stratejiyi belirleyen bir veridir. Strateji şudur: **genel havacılık BT bilgisiyle git** (`247`, `248`) ve boşluğu görüşmede **senin soracağın sorularla** doldur.

Sorulacak akıllı sorular şunlardır. Hangi ürün alanında çalışacağım — rezervasyon/biletleme mi, uçuş operasyonu mu, kargo mu, bakım mı? Ekip yapısı nasıl; müşteri tarafında mı oturacağım, danışman ekibiyle mi? Sistemin ne kadarı eski (legacy) kod, ne kadarı yeni geliştirme? Üretime çıkış süreci ve sıklığı nedir?

Bu sorular iki iş birden yapar: hem gerçek bilgi üretir hem de görüşmeciye sektörü ciddiye aldığını gösterir. Maaş ve yan hak soruları da meşrudur ama yeri teknik görüşme değildir; İK/danışman aşamasıdır.

```svg
<svg viewBox="0 0 660 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İş ortağı ilanında iki aşamalı görüşme süreci ve adayın soracağı sorular">
  <defs>
    <marker id="havmap-asama-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İş ortağı ilanı: iki aşamalı görüşme</text>
  <rect x="24" y="44" width="280" height="76" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="164" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">1 · Danışman firma görüşmesi</text>
  <text x="164" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">CV doğrulama · genel teknik tarama</text>
  <text x="164" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">iletişim ve beklenti uyumu</text>
  <path d="M306 82 L352 82" fill="none" stroke="#5D7789" stroke-width="1.4" marker-end="url(#havmap-asama-arw)"/>
  <rect x="356" y="44" width="280" height="76" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="496" y="66" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">2 · Müşteri firma görüşmesi</text>
  <text x="496" y="86" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">derin teknik + alan senaryoları</text>
  <text x="496" y="104" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">firma burada belli olur</text>
  <rect x="24" y="140" width="612" height="84" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="330" y="162" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Hedef firma bilinmiyor → görüşmede SENİN soracağın sorular</text>
  <text x="330" y="184" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Hangi ürün: rezervasyon mu, operasyon mu, kargo mu? · Ekip yapısı: müşteri tarafında mı?</text>
  <text x="330" y="204" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">Eski (legacy) kod oranı ne? · Üretime çıkış süreci ve sıklığı nasıl?</text>
</svg>
```

### Yedi günlük plan

Aşağıdaki liste **sırayla** çalışılacak biçimde dizilmiştir; numaralar bu modüldeki konu numaralarıdır. Her akşamın son işi **Hatalı Cevaplar** bölümüdür.

1. **Gün 1 — İlan ve sektör (4 konu):** `246` (bu konu) · `247` havacılık BT alan bilgisi · `248` havacılık teknik desenleri · `249` React, Java geliştirici gözüyle. Sektörü tanıdığını gösteren blok budur; `248`'deki "son koltuk", "çift tıklama" ve "ödeme yarıda kaldı" senaryolarının sözlü cevap iskeletlerini yüksek sesle prova et.
2. **Gün 2 — Ödeme sistemleri (5 konu):** `250` kart ödeme akışı ve aktörler · `251` ödeme güvenliği (3DS, PCI, tokenizasyon, dolandırıcılık) · `252` mutabakat, takas, itiraz · `253` ödemede tutarlılık (tekilleştirme anahtarı, zaman aşımı) · `254` havacılıkta ödeme ve biletleme. **Sırayla çalış**; beşi birbirinin üstüne kuruluyor.
3. **Gün 3 — Spring çekirdeği ve işlem yönetimi:** `68`, `69`, `71`, `72`, `73`, `76`, `78`, `83`, `84`. Her teknik mülakat buradan başlar; `@Transactional` ve self-invocation soruları neredeyse garantidir.
4. **Gün 4 — Veri katmanı, kilit ve tutar disiplini:** `80`, `81`, `82`, `29`, `146`, `224`, `225`, `226`. Rezervasyonda "son koltuk" eşzamanlılığı ve para yuvarlaması bu kümeden sorulur.
5. **Gün 5 — Mikroservis, olay güdümlü mimari ve dayanıklılık:** `95`, `97`, `99`, `101`, `125` olay güdümlü mimari, `126` saga, `130` idempotency+outbox, `116` transactional outbox, `118` Spring Kafka retry+DLT, `122` RabbitMQ DLX/DLQ, `153` rate limiting. **Retry ve ölü mektup kuyruğu** soruları `118` ve `122`'de.
6. **Gün 6 — Eşzamanlılık, JVM ve izlenebilirlik:** `45`, `46`, `47`, `48`, `51`, `54`, `56`, `62`, `37`-`39` · ardından `104` merkezî loglama, `103` dağıtık izleme, `193` gözlemlenebilirlik. "Üretimde hata oldu, ne yaparsın?" sorusunun cevabı son üçünde: correlation-id ile uçtan uca izleme.
7. **Gün 7 — Mimari, kalıplar ve davranışsal:** `131` SOLID, `133` yaratımsal, `134` yapısal, `135` davranışsal kalıplar, `141` system design iskeleti · `256` anlatım teknikleri, `195` davranışsal. Bu gün **yeni teknik konu açma**; tekrar et ve yüksek sesle prova yap.

> Banka görüşmen için ayrı sıra `238` numaralı konudadır; `255` orada sorulan 45 sorunun kaydını taşır.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Havacılık ilanı için yedi günlük çalışma planı">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">7 günlük plan — hangi gün, hangi konu numaraları</text>
  <rect x="24" y="38" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 1</text>
  <text x="134" y="52" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">İlan ve sektör — havacılığı tanıdığını göster</text>
  <text x="134" y="66" font-size="10.5" fill="#5D7789" font-family="system-ui">246 · 247 · 248 · 249 — senaryo cevap iskeletlerini prova et</text>
  <rect x="24" y="74" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 2</text>
  <text x="134" y="88" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Ödeme sistemleri — sırayla, üst üste kuruluyor</text>
  <text x="134" y="102" font-size="10.5" fill="#5D7789" font-family="system-ui">250 · 251 · 252 · 253 · 254</text>
  <rect x="24" y="110" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 3</text>
  <text x="134" y="124" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Spring çekirdeği + işlem yönetimi</text>
  <text x="134" y="138" font-size="10.5" fill="#5D7789" font-family="system-ui">68 · 69 · 71 · 72 · 73 · 76 · 78 · 83 · 84 — her mülakat buradan başlar</text>
  <rect x="24" y="146" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="166" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 4</text>
  <text x="134" y="160" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Veri katmanı, kilit, tutar disiplini</text>
  <text x="134" y="174" font-size="10.5" fill="#5D7789" font-family="system-ui">80 · 81 · 82 · 29 · 146 · 224 · 225 · 226 — son koltuk eşzamanlılığı</text>
  <rect x="24" y="182" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 5</text>
  <text x="134" y="196" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Mikroservis, olay güdümlü mimari, retry/DLQ</text>
  <text x="134" y="210" font-size="10.5" fill="#5D7789" font-family="system-ui">95 · 97 · 99 · 101 · 125 · 126 · 130 · 116 · 118 · 122 · 153</text>
  <rect x="24" y="218" width="96" height="30" rx="8" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="72" y="238" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Gün 6</text>
  <text x="134" y="232" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Eşzamanlılık + JVM + izlenebilirlik</text>
  <text x="134" y="246" font-size="10.5" fill="#5D7789" font-family="system-ui">45-48 · 51 · 54 · 56 · 62 · 37-39 — sonra 104 · 103 · 193</text>
  <rect x="24" y="254" width="96" height="30" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="72" y="274" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gün 7</text>
  <text x="134" y="268" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Mimari, kalıplar, davranışsal + prova</text>
  <text x="134" y="282" font-size="10.5" fill="#5D7789" font-family="system-ui">131 · 133 · 134 · 135 · 141 — sonra 256 · 195, yeni konu açma</text>
</svg>
```

### Mülakatta ne sorulur?

- **"Hangisi daha iyi?" soruları** (önbellek mi doğrudan sorgu mu, monolit mi mikroservis mi) tuzaktır; kalıp **bağlam-ölçüt-koşul**dur: önce bir-iki netleştirme sorusu, sonra ölçütler, sonra koşullu cevap. Koşulsuz taraf tutmak kıdemsizlik işareti okunur.
- **Bilmediğin soruda** uydurma; dürüst köprü kur: bilmediğini söyle, en yakın bildiğin kavramdan akıl yürüt, nasıl doğrulayacağını ekle. Emin tonla söylenen yanlış, sonraki bütün cevaplarına gölge düşürür.
- **React sorusunda** deneyimini büyütme: ne yaptığını somut anlat (bileşen yazdım, state yönettim, API bağladım), düştüğün bir tuzağı söyle; bilmediğin yerde köprü kur (`249`).
- **Havacılık deneyimin yoksa** alanı küçümseme; `247`'deki yolcu akışını sırasıyla sayabilmek ve benzer teknik işini (eşzamanlı stok düşümü, durum makinesi, dış servis entegrasyonu) o akışa bağlamak güçlü bir köprüdür.
- **Sesli düşün** ve kesin sürüm/oran iddiasından kaçın; emin değilsen mekanizmayı anlat, sayıyı "büyüklük mertebesi olarak" çerçevele.

### Tek cümlede

İlandaki dört maddeyi modül konularına çevir. İş ortağı ilanının iki aşamalı sürecine, genel havacılık BT bilgisi ve firmaya soracağın akıllı sorularla hazırlan. Yedi günü yukarıdaki plana göre böl. Cevaplarda önce bağlam sor, sesli düşün ve deneyimin olmayan yerde yalan yerine köprü kur.

# havmap-1 | Temel

## Soru
İlanda "Java ve Spring Boot teknolojilerinde güçlü deneyim" yazıyor. Bu madde mülakatta fiilen neye dönüşür?

## A
Çekirdek mekanizmalara inen somut sorulara: bağımlılık enjeksiyonu, `@Transactional` tuzakları, N+1 gibi üretim kodunun göstergesi sayılan konulara
🤖 Doğru: görüşmeci başlığın kendisini değil, arkasındaki üretim deneyimini ölçen somut soruları sorar.

## B
Hangi özelliğin hangi Java ve Spring sürümünde geldiğinin ezbere sayılmasına; "güçlü deneyim" öncelikle sürüm tarihçesine hâkimiyet demektir
🤖 sürüm ezberi deneyim göstergesi değildir; emin olunmayan sürüm iddiası mülakatta ayrıca risklidir.

## C
Yalnız geçmiş projelerin adlarının ve kullanılan teknolojilerin listelenmesine; teknik ayrıntı CV zaten yazdığı için tekrar sorulmaz
🤖 CV'de yazan her şey tam tersine derinlemesine sorulur; proje adı saymak teknik doğrulamanın yerine geçmez.

## D
Toplam deneyim yılının sorgulanmasına; "güçlü deneyim" süreyle ölçüldüğü için kaç yıl çalışıldığı cevabı maddeyi kapatır
🤖 süre tek başına yeterlilik kanıtı değildir; görüşmeci yılı değil o yılda ne öğrenildiğini somut sorularla ölçer.

## Doğru
A

## Açıklama
İlan maddeleri varış tabelasıdır; mülakatta sorulan şey tabelanın kendisi değil arkasındaki yoldur. "Java ve Spring Boot'ta güçlü deneyim" maddesi, görüşmecinin elinde şu somut sorulara dönüşür: bağımlılık enjeksiyonu ve bean yaşam döngüsü (`68`, `69`), proxy mekaniği ve `@Transactional` self-invocation tuzağı (`72`, `83`), işlem yayılımı ve geri alma kuralları (`84`), JPA tarafında N+1 (`82`), `equals`/`hashCode` sözleşmesi (`7`, `81`). Bunlar "üretim kodu yazmış mı?" sorusunun dolaylı biçimleridir. Sürüm tarihçesi ezberi beklenmez; tersine, emin olunmayan sürüm iddiası vermek risklidir — mekanizmayı ve çözdüğü sorunu anlatmak daha güçlüdür. Proje adı listelemek de yeterli değildir, çünkü CV'de yazan her satır derinlemesine kazılabilir; adını yazıp savunamadığın teknoloji artı değil eksi puandır. Deneyim yılı ise tek başına hiçbir şey kanıtlamaz; görüşmeci süreyi değil, o sürede biriken yargı gücünü ölçer. Hazırlığın doğru birimi bu yüzden başlık değil, konu numarasıdır: Java çekirdeği `1`-`36`, güncel Java `37`-`40`, eşzamanlılık/JVM `45`-`65`, Spring `68`-`84`.

# havmap-2 | Temel

## Soru
İlan bir iş ortağı (danışmanlık) ilanı ve hedef firma bilinmiyor. Bu gerçek hazırlık stratejisini nasıl etkiler?

## A
Firma bilinmediği için alan hazırlığı anlamsızlaşır; hazırlık tamamen Java ve Spring sorularına daraltılır, sektör konuları hiç çalışılmaz
🤖 sektör ilanda açıkça yazıyor; müşteri firma görüşmesinde alan soruları gelme olasılığı yüksektir ve boş bırakılmamalıdır.

## B
Sektörün en bilinen firması hedef varsayılır ve hazırlık o firmanın bilinen teknoloji yığınına ve ürünlerine özel yapılır
🤖 doğrulanmamış varsayım yanlış firmaya hazırlanma riski taşır; görüşmede yanlış firmaya özgü iddialar güven de zedeler.

## C
Genel havacılık BT bilgisiyle hazırlanılır ve görüşmede firmaya sorulacak akıllı sorular (hangi ürün, ekip yapısı, legacy oranı) önceden listelenir
🤖 Doğru: firma bilinmediğinde genel sektör bilgisi + doğru sorular, hem hazırlığı hem görüşmedeki izlenimi güçlendirir.

## D
Teknik hazırlık ilk görüşmeye kadar ertelenir; danışman firma süreçte hedef firmayı açıkladığında hazırlığa o gün başlanır
🤖 ilk görüşmenin kendisi de teknik eleme içerir; hazırlıksız girilen eleme aşaması ikinci aşamaya ulaşmayı engelleyebilir.

## Doğru
C

## Açıklama
İş ortağı ilanının iki sonucu vardır: görüşme muhtemelen **iki aşamalıdır** (önce danışman firmanın eleme görüşmesi, sonra müşteri firmanın derin teknik ve alan görüşmesi) ve **hedef firma ilanda yazmaz**. Bu belirsizlik hazırlığı iptal etmez, biçimini değiştirir: firmaya özel hazırlık yapılamayacağı için **genel havacılık BT bilgisi** çalışılır (`247` alan kavramları, `248` teknik desenler) ve boşluk, görüşmede **senin soracağın akıllı sorularla** doldurulur — hangi ürün alanında çalışacağım (rezervasyon mu, operasyon mu, kargo mu), ekip yapısı nasıl, sistemin ne kadarı eski (legacy) kod, üretime çıkış süreci nedir. Bu sorular hem karar için gerçek bilgi üretir hem de sektörü ciddiye aldığını gösterir. Alan hazırlığını tamamen atlamak, ilanda açıkça yazan sektör sinyalini görmezden gelmektir. Bilinen bir firmayı hedef varsaymak ise doğrulanmamış varsayımla yanlış hedefe koşmaktır; görüşmede o varsayıma dayalı iddialar tutarsızlık üretir. Hazırlığı firmanın açıklanmasına ertelemek de ilk aşamanın kendisinin teknik eleme içerdiğini unutur — o kapıdan geçemeyen, ikinci aşamayı hiç göremez.

# havmap-3 | Orta

## Soru
Yedi günlük planda React'e ayrı bir yarım gün ayrılıyor ve havacılık alan bilgisi son güne bırakılıyor. Bu düzenin gerekçesi nedir?

## A
Ön yüz konularına arka uç konuları tamamen bitmeden başlamak tekniken verimsizdir; React'in yeri bu bağımlılık sırası yüzünden sondadır
🤖 böyle bir teknik bağımlılık yoktur; React bloğu bağımsız çalışılabilir, sıralamanın gerekçesi ilan ve tazelik mantığıdır.

## B
React ilanda ayrı bir madde olduğu için kendi bloğunu hak eder; alan bilgisi ise en taze kalması gereken bilgi olduğu için sona konur
🤖 Doğru: plan ilan maddelerine ve bilginin tazelik ihtiyacına göre sıralanır, zorluk veya alfabetik sıraya göre değil.

## C
Alan bilgisinin mülakatta sorulmayacağı neredeyse kesindir; bu yüzden plana en sona eklenir ve vakit kalmazsa sessizce atlanabilir
🤖 sektör ilanı olduğu için alan soruları gelme olasılığı yüksektir; son güne konması atlanabilir olduğundan değil, taze kalması gerektiğindendir.

## D
Plan kolaydan zora ilerler; React modüldeki en kolay konulardan biri olduğu için sona yaklaştırılır, en kolay olan alan bilgisi de en sona konur
🤖 planın ölçütü kolaylık değildir; sıralama elenme riski, ilan maddeleri ve tazelik ihtiyacıyla belirlenir.

## Doğru
B

## Açıklama
Yedi günlük planın sıralama mantığı üç ölçüte dayanır: **elenme riski** (Spring çekirdeği ve veri katmanı öne alınır çünkü her mülakat oradan başlar), **ilanın maddeleri** (ilan React'i ayrı bir cümleyle istiyor; ayrı istenen şey plan içinde eritilmez, `208`-`213` ve `249` ile kendi yarım gününü alır) ve **tazelik** (alan bilgisi `247`-`248` ile davranışsal anlatılar `195`, mülakat günü zihinde en taze durması gereken bilgilerdir; erken çalışılırsa ayrıntıları uçar). Ön yüz/arka uç arasında bir teknik bağımlılık sırası yoktur; React bloğu istenirse ikinci gün de çalışılabilirdi, ama tazelik ve risk sıralaması onu sona yaklaştırır. "Alan bilgisi zaten sorulmaz" varsayımı sektör ilanında tehlikelidir: müşteri firma görüşmesinde "sektörü biliyor mu?" sorusu neredeyse kesin gündeme gelir ve `247`'deki yolcu akışını sayabilmek tek başına güçlü izlenim bırakır. Kolaydan zora sıralama da yanlış ölçüttür: kolay ama sorulmayacak konuya harcanan saat hiçbir riski kapatmaz. Son günün bir kuralı daha vardır: yeni konu açılmaz; tekrar, yüksek sesle prova ve firmaya sorulacak soruların üzerinden geçilir.

# havmap-4 | Orta

## Soru
Görüşmeci "React ile neler yaptınız?" diye soruyor. React deneyimin gerçek ama sınırlı: birkaç ekran, temel hook'lar. Doğru cevap hangisidir?

## A
Deneyimi güçlü göstermek için "büyük bir SPA'yı uçtan uca ben yönettim" biçiminde anlatmak; ilanda React yazdığı için mütevazı cevap eleyicidir
🤖 abartılan deneyim bir sonraki soruda çöker; performans optimizasyonu gibi bir ayrıntı sorulduğunda tutarsızlık bütün cevaplara gölge düşürür.

## B
"React'i gerçek anlamda bilmiyorum" deyip konuyu hızla güçlü olduğun Java tarafına çevirmek; yanlış bir şey söyleme riski böylece sıfırlanır
🤖 gerçek deneyimi olduğundan küçük göstermek ve konu kaçırmak, ilanın açıkça istediği maddeyi savunmasız bırakır.

## C
React ekosisteminin genişliğini (durum yönetimi kütüphaneleri, SSR çatıları, derleme araçları) teorik olarak anlatıp kendi projene hiç girmemek
🤖 teorik genişlik somut deneyim sorusuna cevap değildir; görüşmeci "neler yaptınız" diye sordu, "neler var" diye değil.

## D
Somut ve dürüst iskeletle anlatmak: hangi bileşenleri yazdım, state'i nasıl yönettim, API'yi nasıl bağladım, hangi tuzağa düştüm; bilmediğim yerde köprü kurmak
🤖 Doğru: sınırlı ama gerçek deneyimi somut anlatmak, abartılmış veya teorik cevaptan daha güçlüdür.

## Doğru
D

## Açıklama
İlan "React ile çalışmış" diyor — "React uzmanı" değil. Bu yüzden beklenen şey, sınırlı da olsa **gerçek** deneyimin somut anlatımıdır: hangi bileşenleri yazdın, props/state ayrımını nasıl kullandın, `useEffect` ile API çağrısını nasıl bağladın, hangi tuzağa düştün (örneğin bağımlılık dizisi yüzünden yinelenen istek). Düşülen tuzağı anlatmak zayıflık değil, tersine gerçek deneyim kanıtıdır — tuzağa düşmemiş kişi tuzağı bilmez. Deneyimi abartmak en riskli seçenektir: "uçtan uca yönettim" cümlesinin arkasından gelen ilk derin soru (performans, uzlaştırma ayrıntısı) tutarsızlığı açığa çıkarır ve yalnız bu cevabı değil önceki doğru cevapların güvenilirliğini de götürür. Deneyimi küçültüp konuyu Java'ya kaçırmak, ilanın açık maddesini savunmasız bırakır ve konu kaçırma olarak okunur. Ekosistem turu anlatmak ise "neler yaptınız" sorusuna "neler var" cevabı vermektir. Bilmediğin bir React sorusu gelirse (ileri performans optimizasyonu gibi) kalıp yine dürüst köprüdür: "kullanmadım; ama sorunun render maliyetiyle ilgili olduğunu biliyorum, şöyle yaklaşırdım…" Cevap iskeletinin tamamı ve React çekirdeği `249` numaralı konudadır.

# havmap-5 | İleri

## Soru
Görüşmeci soruyor: "Uçuş arama sonuçlarını önbellekte mi tutarsın, her istekte veritabanından mı okursun?" Bu tür sorularda beklenen cevap kalıbı nedir?

## A
Modern ve performanslı olan seçeneği (önbellek) net biçimde savunmak; arama trafiğinde önbelleksiz tasarım savunulamaz olduğu için kararlı durmak
🤖 koşulsuz taraf tutmak bağlamı yok sayar; ölçülen şey tercih değil, karar ölçütlerini konuşabilmektir.

## B
İki yaklaşımın tanımını ve genel artı/eksilerini sırayla anlatıp tercihi görüşmeciye bırakmak; taraf tutmamak en güvenli tutumdur
🤖 tanım turu karar üretmez; ölçüt koyup koşullu karar vermeyen cevap boş kalır.

## C
Önce bağlamı soran sorular sormak (okuma/yazma oranı, bayatlık toleransı, tutarlılık ihtiyacı), sonra ölçütleri koyup koşullu cevap vermek
🤖 Doğru: "hangisi daha iyi" sorularının kalıbı bağlam-ölçüt-koşuldur; kıdem işareti netleştirme sorusudur.

## D
Kendi son projendeki tercihi anlatıp aynı çözümü bu senaryoya da önermek; kanıtlanmış deneyim her zaman en güçlü gerekçedir
🤖 tek deneyimi genel kurala çevirmek bağlam farklarını yok sayar; deneyim örnek olur ama gerekçenin yerine geçmez.

## Doğru
C

## Açıklama
"Hangisi daha iyi?" soruları tuzaktır: doğru cevap seçeneklerden biri değil, **karar ölçütleridir**. Kalıp üç adımdır. Önce **bağlam soruları**: bu sorgu ne kadar okuma-ağırlıklı (arama/rezervasyon asimetrisi tam da budur — arama trafiği rezervasyonun katbekat üstündedir), kaç saniyelik/dakikalık bayatlık kabul edilebilir, bayat verinin iş sonucu ne (görünen koltuk satılmış olabilir)? Sonra **ölçütler**: gecikme hedefi, veritabanı yükü, tutarlılık ihtiyacının kritik noktası. Sonra **koşullu cevap**: "arama sonuçları kısa ömürlü önbelleğe alınabilir çünkü zaten anlık bir fotoğraftır; ama rezervasyon anında fiyat ve koltuk mutlaka yeniden doğrulanır — kritik doğruluk noktası orasıdır." Bu cevap hem teknik derinlik hem yargı gücü gösterir; önbellek mekaniği `157`-`160`, konunun havacılık karşılığı `248` numaralı konudadır. Koşulsuz "önbellek" demek bağlamı yok sayar; iki tanımı sıralayıp tercih bırakmamak karar üretmez; kendi projendeki çözümü genellemek ise tek örneği kurala çevirir. Görüşmecinin ölçtüğü şey netleştirme sorusu sorup sormadığındır — kapsam sorusu zayıflık değil, kıdem işaretidir.

# havmap-6 | İleri

## Soru
Görüşmenin sonunda söz sana verildi. İş ortağı ilanı olduğu ve müşteri firma hâlâ netleşmediği düşünülürse en değerli soru kümesi hangisidir?

## A
Maaş bandı, yan haklar ve izin düzeni; danışmanlık modelinde en belirsiz konular bunlar olduğu için ilk teknik görüşmede netleştirilmelidir
🤖 bu konular meşrudur ama yeri teknik görüşme değil danışman/İK aşamasıdır; burada sorulması odak kayması okunur.

## B
Hangi ürün alanında çalışılacağı (rezervasyon mu, operasyon mu, kargo mu), ekip yapısı, eski (legacy) kod oranı ve üretime çıkış süreci
🤖 Doğru: bu sorular belirsiz olan asıl bilgiyi açar ve sektörü ciddiye alan bir aday izlenimi bırakır.

## C
Hiç soru sormamak; danışman firma süreci zaten anlatacağı için soru sormak hazırlıksızlık veya sürece güvensizlik izlenimi verir
🤖 tam tersi geçerlidir; soru sormamak neredeyse her zaman ilgisizlik veya hazırlıksızlık olarak okunur.

## D
Müşteri firmanın adını, iç mimari şemasını ve güvenlik ayrıntılarını ısrarla istemek; bilgi verilmeden değerlendirme yapılamayacağını belirtmek
🤖 danışman firmanın açıklayamayacağı bilgiyi ısrarla talep etmek sınır ihlali okunur; aynı bilgi doğru sorularla dolaylı öğrenilir.

## Doğru
B

## Açıklama
Soru sorma faslı görüşmenin hâlâ değerlendirilen parçasıdır ve iş ortağı ilanında iki kat önemlidir: hedef firma bilinmediği için asıl bilgi açığı **senin tarafındadır** ve onu ancak doğru sorular kapatır. En değerli küme, işin gerçeğini açan sorulardır: hangi ürün alanında çalışacağım — rezervasyon/biletleme mi, uçuş operasyonu mu, kargo mu, bakım mı (her biri bambaşka alan bilgisi ve teknik karakter demektir, `247`); ekip yapısı nasıl — müşteri tarafında mı oturacağım, danışman ekibiyle mi; sistemin ne kadarı eski (legacy) kod, entegrasyon yükü ne durumda; üretime çıkış süreci ve sıklığı nedir. Bu sorular hem karar vermen için gerçek veri üretir hem de sektörün yapısını bildiğini gösterir — "rezervasyon mu operasyon mu?" diye soran aday, havacılık BT'sinin tek parça olmadığını biliyordur. Maaş ve yan haklar meşru konulardır ama yeri teknik görüşme değil, danışman firmanın İK aşamasıdır. Hiç soru sormamak ilgisizlik okunur ve kolayca kaçınılabilir bir kayıptır. Müşteri firmanın kimliğini ve iç ayrıntılarını ısrarla istemek ise danışmanın açıklayamayacağı bilgiyi zorlamaktır; aynı bilginin sorulabilir hali, ürün alanı ve ekip yapısı sorularıdır.
