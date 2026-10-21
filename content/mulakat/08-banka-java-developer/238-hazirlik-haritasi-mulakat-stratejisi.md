---
slug: hazirlik-haritasi-mulakat-stratejisi
baslik: "Hazırlık Haritası ve Mülakat Stratejisi: İlan Maddesinden Cevap Kalıbına"
kademe: Temel
sira: 238
etiketler: 12
---

## Konu anlatımı

### Bu ne işe yarar?

Uzun bir yola çıkmadan önce haritaya bakarsın. Nereye gideceğini bilmek yetmez. Hangi yolun kapalı olduğunu, nerede yakıt alacağını ve kaç saatin kaldığını da bilmen gerekir. Teknik mülakat hazırlığı da böyledir.

Elinde bir **ilan metni** vardır ve o metin bir varış tabelasıdır. Şunları yazar: "Spring Boot", "mikroservis", "Docker", "Kubernetes", "güncel Java teknolojileri". Ama mülakatta sana bu kelimeler sorulmaz. Bu kelimelerin **arkasındaki somut konular** sorulur.

Bu konu üç iş yapar. İlandaki her maddeyi mülakatta fiilen sorulan konulara çevirir. Sınırlı zamanı hangi sırayla harcayacağını söyler. Cevap verirken kullanacağın kalıpları verir. Teknik derinlik bu dosyada yoktur; derinlik Kademe 1-6'daki konularda durur. Burada olan şey **haritanın kendisidir**.

### Temel kavramlar

- **İlan maddesi → konu eşlemesi:** ilandaki genel bir başlığın mülakattaki somut karşılığıdır. Örneğin "Spring Boot" başlığını düşün. Görüşmeci "Spring Boot biliyor musun?" diye sormaz. "`@Transactional` neden aynı sınıf içinden çağrıda çalışmaz?" diye sorar.
- **Elenme riski yüksek konu:** bilinmediğinde kötü izlenim bırakan çekirdek konulardır. Görüşmeci "bu kişi üretim kodu yazmamış" diye düşünür. Bunlar egzotik değildir; sıradan ama sık yanlış bilinen konulardır.
- **Cevap kalıbı:** bir soru türüne verilecek cevabın iskeletidir. Örneğin "hangisi daha iyi?" sorularının kalıbı şudur: önce bağlamı sor, sonra ölçütü koy, sonra koşullu cevap ver.
- **Sesli düşünme (thinking aloud):** cevaba giderken zihinsel adımları duyulur biçimde anlatmaktır. Görüşmeci çoğu zaman nihai cevaptan çok **nasıl düşündüğünü** ölçer. Sessiz kalıp tek seferde sonuç söylemek, bu bilgiyi ondan saklar.
- **Köprü cevap:** deneyimin olmadığı bir alanda önce "yapmadım" dersin. Sonra sahip olduğun en yakın deneyimi o alana bağlarsın. Yalan söylemez ama boş da bırakmaz.
- **Kapsam sorusu (clarifying question):** açık uçlu bir tasarım sorusunda, cevaba başlamadan önce sorulan netleştirme sorusudur. Örnekler: kaç kullanıcı, hangi tutarlılık, hangi entegrasyon. Zayıflık değil, kıdem işaretidir.
- **STAR:** davranışsal sorularda kullanılan anlatım iskeletidir. Dört parçası vardır: Durum (Situation), Görev (Task), Eylem (Action), Sonuç (Result). Ayrıntısı `195` numaralı konudadır.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="İlandaki maddelerin mülakatta sorulan konulara eşlenmesi">
  <defs>
    <marker id="harita-map-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">İlan ne yazıyor → mülakatta fiilen ne sorulur</text>
  <text x="139" y="46" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">İLANDAKİ MADDE</text>
  <text x="452" y="46" text-anchor="middle" font-size="11" font-weight="700" fill="#5D7789" font-family="system-ui">SORULAN KONU (modül numaraları)</text>
  <rect x="24" y="58" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="85" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Spring Boot</text>
  <rect x="268" y="58" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="76" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">IoC/DI, auto-config, @Transactional tuzakları, N+1</text>
  <text x="452" y="92" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">68 · 71 · 72 · 73 · 76 · 83 · 84 · 82</text>
  <path d="M256 80 L266 80" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-map-arw)"/>
  <rect x="24" y="108" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="135" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Mikroservis</text>
  <rect x="268" y="108" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="126" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">servis sınırı, dayanıklılık, tutarlılık, saga/outbox</text>
  <text x="452" y="142" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">95 · 97 · 99 · 101 · 116 · 126 · 130</text>
  <path d="M256 130 L266 130" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-map-arw)"/>
  <rect x="24" y="158" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="185" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Docker / Kubernetes</text>
  <rect x="268" y="158" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="176" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">imaj katmanı, probe, requests/limits, zarif kapanış</text>
  <text x="452" y="192" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">214 · 215 · 105 · 216 · 217 · 218 · 219</text>
  <path d="M256 180 L266 180" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-map-arw)"/>
  <rect x="24" y="208" width="230" height="44" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="139" y="235" text-anchor="middle" font-size="12" font-weight="700" fill="#186399" font-family="system-ui">Güncel Java teknolojileri</text>
  <rect x="268" y="208" width="368" height="44" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.4"/>
  <text x="452" y="226" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">record/sealed, pattern matching, sanal thread, ZGC</text>
  <text x="452" y="242" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">37 · 38 · 39 · 40 · 56 · 62 · 74</text>
  <path d="M256 230 L266 230" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-map-arw)"/>
</svg>
```

Eşlemeyi biraz açalım, çünkü her maddenin arkasında **beklenen cevabın tonu** farklıdır:

- **"Spring Boot" dendiğinde** şunlar sorulur. Kap (container) ve **bağımlılık enjeksiyonu** (`68`). Bean kapsamı ile tekil bean'e prototip enjekte etme tuzağı (`69`). `@Qualifier`/`@Primary` ve döngüsel bağımlılık (`71`). **Vekil nesne (proxy) mekaniği ve nesnenin kendi metodunu kendi içinden çağırması (self-invocation)** (`72`, `83`). Otomatik yapılandırmanın nasıl devreye girip nasıl ezildiği (`73`). REST katmanında durum kodu ve hata sözleşmesi (`76`, `78`). İşlem yayılımı ve izolasyon (`84`). JPA tarafında **N+1** (`82`). Bu maddede kesin ve net bilgi beklenir; "sanırım" cevabı burada pahalıya patlar.
- **"Mikroservis" dendiğinde** şunlar sorulur. Parçalamanın maliyeti ve monolit/modüler monolitin ne zaman doğru olduğu (`95`, `96`). Servis sınırının nasıl çizildiği (`97`). Ağ geçidinin (gateway) sorumluluğu (`99`). **Devre kesici ve yeniden deneme (retry)** (`101`). Dağıtık işlem yerine **saga** ve **telafi işlemi** (`126`). Çift-yazma sorunu ile **giden kutusu tablosu (outbox) + tekrar güvenliği (idempotency)** (`116`, `130`). Burada beklenen ton "her şeyi mikroservis yaparım" değildir; **takas (trade-off) konuşabilmektir**.
- **"Docker / Kubernetes" dendiğinde** şunlar sorulur. İmaj katmanı ve çok aşamalı derleme (`214`). Konteynerde sinyal ve kaynak sınırı (`215`). **Ayakta mı / hazır mı / açılıyor mu denetimleri (liveness, readiness, startup probe)** arasındaki fark ve `OOMKilled` (`105`, `218`, `219`). Deployment/Service/Ingress rolleri (`216`, `217`). **Zarif kapanış** sırası (`105`, `218`). Bu maddede en sık elenme sebebi, ayakta mı denetimi ile hazır mı denetimini karıştırmaktır.
- **"Güncel Java teknolojileri" dendiğinde** şunlar sorulur. `record` (`37`), `sealed` (`38`), `switch` üzerinde **desen eşleme (pattern matching)** (`39`), metin bloğu ve `var` (`40`). **Sanal iş parçacıkları (virtual threads)** ve iğnelenme (pinning) (`56`, `57`). Düşük gecikmeli çöp toplayıcı **ZGC** (`62`). Spring Boot 4 / Jakarta 11 geçişi (`74`). Buradaki tuzak, sürüm numarası ezberlemektir. "Hangi sürümde geldi?" sorusuna emin değilsen **kesin sürüm iddiası verme**. Onun yerine ne işe yaradığını ve hangi sorunu çözdüğünü anlat.

### Nasıl çalışır?

Yedi günün var ve bu yedi gün rastgele okumayla geçmemeli. Sıralamanın mantığı şudur. **Her mülakat Spring ve veri katmanından başlar**, çünkü günlük iş orasıdır. Dağıtım ve altyapı ikinci halkadır. Eşzamanlılık ve JVM ise "derinliği ölçen" halkadır. Alan bilgisi ile davranışsal kısım en sona bırakılır; çünkü onların en taze kalması gerekir.

1. **Gün 0 (yarım gün) — Sahadan gelen veri.** `255`. **Bu maddeyi atlama.** İlk teknik görüşmede fiilen sorulan 45 sorunun tamamı orada. Hangi alandan kaç soru geldiği ve görüşmecinin verdiği geri bildirim de orada. İkinci görüşme aynı konuların **derinini** sorar. Neyi tekrar edeceğini tahminle değil, bu listeyle seçersin.
2. **Gün 1 — Spring çekirdeği ve işlem yönetimi.** `68`, `69`, `71`, `72`, `73`, `76`, `78`, `83`, `84`. Neden ilk gün? Her teknik mülakat buradan başlar ve `@Transactional` soruları neredeyse garantidir. `76` içinde **Filter ↔ Interceptor** ayrımı da var; ilk görüşmede soruldu.
3. **Gün 2 — JPA, SQL, indeks, para ve kilit.** `80`, `81`, `82`, `29`, `146`, `147`, `227`, `224`, `225`, `226`. Neden ikinci gün? Kredili sistemlerde veri katmanı işin merkezidir. Şunlar birlikte sorulur: **N+1**, **izolasyon**, **kilit**, **BigDecimal** ve **bölümleme/parçalama (partitioning/sharding)**.
4. **Gün 3 — Mikroservis, Docker, Kubernetes.** `95`, `97`, `98`, `99`, `100`, `101`, `214`, `215`, `105`, `216`, `217`, `218`, `219`. Neden üçüncü gün? Bunlar ilanda açıkça yazan maddeler. `100` OpenFeign ilk görüşmede soruldu.
5. **Gün 4 — Mesajlaşma, saga/giden kutusu, yeniden deneme/DLQ ve toplu iş.** `107`-`114`, **`116`**, **`118`**, `119`-`122`, `124`, `125`, **`126`**, **`130`**, `222`, `223`, `221`. **Görüşmecinin "saga ve mikroservis yapılarını daha iyi bilmelisin" dediği yer tam burası.** Kalın olanlar öncelikli. `118` ve `122` yeniden denemeyi ve ölü mektup kuyruğunu (DLQ) taşır. `221` ise eski sistem entegrasyonudur (SOAP/JMS).
6. **Gün 5 — Eşzamanlılık, JVM/GC ve güncel Java.** `45`, `46`, `47`, `48`, `49`, `51`, `54`, `56`, `62`, `65`, `37`-`40`. İlk görüşmede beş konu birden soruldu ve zayıf kalındı: **yarış koşulu (race condition), iş parçacığı güvenliği (thread-safe), `synchronized`, `AtomicInteger`, `volatile`**. Bu gün pazarlıksızdır.
7. **Gün 6 — SOLID/kalıplar, sistem tasarımı, Redis ve izlenebilirlik.** `131`, `133`, `134`, `135`, `141`, `142`, `144`, `155`, `157`, `160`; ardından `104`, `103`, `193`, `89`. `133` ile `135`, **Factory ile Strategy farkı** sorusunun kaynağıdır. Son dörtlü ise "üretimde hata oldu, ne yaparsın?" sorusunun cevabıdır: ilişkilendirme kimliğiyle (correlation-id) uçtan uca izleme.
8. **Gün 7 — Bankanın gerçek ürünleri, güvenlik ve davranışsal.** Önce **`243`, `244`, `245`**. Hedef banka **ticari/kurumsal ve ihracat finansmanı** odaklıdır; alan bilgisi burada. `239` bireysel krediyi anlatır. Genel kültür olarak değerlidir ama **bu bankanın ürünü değildir**. Üstüne `240`, `241`, `86`, `87`, `190` ve davranışsal `242`, `256`, `195` gelir. Bu gün yeni teknik konu açma; tekrar et ve yüksek sesle prova yap.

```svg
<svg viewBox="0 0 660 300" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Yedi günlük çalışma planı ve her günün konu numaraları">
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">7 günlük çalışma planı — hangi gün, hangi konu numaraları</text>
  <rect x="24" y="38" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="58" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 1</text>
  <text x="134" y="52" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Spring çekirdeği + işlem yönetimi</text>
  <text x="134" y="66" font-size="10.5" fill="#5D7789" font-family="system-ui">68 · 69 · 71 · 72 · 73 · 76 · 78 · 83 · 84 — her mülakat buradan başlar</text>
  <rect x="24" y="74" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="94" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 2</text>
  <text x="134" y="88" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">JPA, SQL, indeks, para ve kilit</text>
  <text x="134" y="102" font-size="10.5" fill="#5D7789" font-family="system-ui">80 · 81 · 82 · 29 · 146 · 227 · 224 · 225 · 226 — elenme riski en yüksek küme</text>
  <rect x="24" y="110" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="130" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 3</text>
  <text x="134" y="124" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Mikroservis + Docker/Kubernetes</text>
  <text x="134" y="138" font-size="10.5" fill="#5D7789" font-family="system-ui">95 · 97 · 98 · 99 · 101 · 103 · 214 · 215 · 105 · 216-219</text>
  <rect x="24" y="146" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="166" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 4</text>
  <text x="134" y="160" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Eşzamanlılık + JVM/GC + güncel Java</text>
  <text x="134" y="174" font-size="10.5" fill="#5D7789" font-family="system-ui">45-51 · 54 · 56 · 59 · 60 · 61 · 62 · 65 · 37-40</text>
  <rect x="24" y="182" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="202" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 5</text>
  <text x="134" y="196" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Mesajlaşma, saga/outbox, toplu iş</text>
  <text x="134" y="210" font-size="10.5" fill="#5D7789" font-family="system-ui">107-114 · 118 · 119-124 · 125 · 126 · 130 · 116 · 222 · 223 · 221</text>
  <rect x="24" y="218" width="96" height="30" rx="8" fill="#EAF1F7" stroke="#186399" stroke-width="1.4"/>
  <text x="72" y="238" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Gün 6</text>
  <text x="134" y="232" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">SOLID/kalıplar, sistem tasarımı, Redis</text>
  <text x="134" y="246" font-size="10.5" fill="#5D7789" font-family="system-ui">131-136 · 140 · 141 · 142 · 144 · 147 · 154 · 155 · 157 · 159 · 160</text>
  <rect x="24" y="254" width="96" height="30" rx="8" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.4"/>
  <text x="72" y="274" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Gün 7</text>
  <text x="134" y="268" font-size="10.5" font-weight="700" fill="#1C3247" font-family="system-ui">Güvenlik, gözlemlenebilirlik, alan bilgisi, davranışsal</text>
  <text x="134" y="282" font-size="10.5" fill="#5D7789" font-family="system-ui">86 · 87 · 189 · 190 · 89 · 193 · 239 · 195 — yeni konu açma, prova yap</text>
</svg>
```

### Elenme riski yüksek konular

Zaman kısıtlıysa önce bunlar. Bu listedeki bir soruya yanlış cevap vermek, diğer on doğru cevabı gölgeleyebilir. Sebebi şudur: hepsi "üretim kodu yazmış mısın?" sorusunun dolaylı biçimidir.

- **`@Transactional` ve nesnenin kendi metodunu kendi içinden çağırması (self-invocation)** (`83`): aynı sınıf içinden çağrılan metotta işlem başlamaz. Çünkü çağrı vekil nesneye (proxy) uğramaz. `private` ve `final` metotta da çalışmaz. Ayrıca varsayılan olarak yalnız **denetlenmeyen (unchecked)** istisnalarda geri alma (rollback) olur.
- **N+1 sorgu problemi** (`82`): tembel (lazy, yani ihtiyaç olunca yüklenen) ilişkiye döngü içinde erişmektir. Çözüm `JOIN FETCH`, entity graph veya toplu getirmedir. Koleksiyon `JOIN FETCH` ile sayfalama birleşince bellekte sayfalama tuzağı doğar.
- **İzolasyon ve kilit** (`224`, `225`): hangi anomalinin hangi seviyede kalktığını bil. **İyimser kilit** (`@Version`) ile **kötümser kilit** (`FOR UPDATE`) arasında ne zaman seçim yapıldığını da bil.
- **BigDecimal ile para** (`226`): `double` ile para tutulmaz. `equals` yerine `compareTo` kullanılır. Ölçek (scale) ve yuvarlama modu bilinçli seçilir.
- **Denetimler ve `OOMKilled`** (`105`, `218`, `219`): ayakta mı denetimi (liveness) uygulamayı yeniden başlatır, hazır mı denetimi (readiness) ise trafikten çıkarır. Bellek limiti aşımı `OOMKilled` üretir; işlemci limiti aşımı ise yalnız kısıtlama (throttling) üretir.
- **Tekrar güvenliği ve giden kutusu tablosu** (`130`, `116`): yeniden deneme (retry) varsa aynı istek iki kez işlenmemelidir. Bir de veritabanı ile mesaj kuyruğuna çift yazma sorunu vardır.
- **`equals`/`hashCode`** (`7`, `81`): sözleşme, `HashMap` davranışı ve JPA varlıklarındaki kimlik tuzağı.
- **İstisna hiyerarşisi** (`16`, `17`): `Error`/`Exception`/`RuntimeException` ayrımı ve `finally` davranışı. Ayrıca istisnayı yutmamak ve sarmalarken nedeni (cause) kaybetmemek.

### Teknik mülakatın tipik akışı

```svg
<svg viewBox="0 0 660 168" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Teknik mülakatın beş aşaması ve her aşamada beklenen davranış">
  <defs>
    <marker id="harita-akis-arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="#5D7789"/>
    </marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Teknik mülakatın beş aşaması</text>
  <rect x="24" y="48" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="80" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Isınma</text>
  <text x="80" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">kendini tanıt</text>
  <text x="80" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">2-3 dakika,</text>
  <text x="80" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">role odaklı özet</text>
  <path d="M138 74 L147 74" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-akis-arw)"/>
  <rect x="149" y="48" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="205" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">2 · CV derinleşme</text>
  <text x="205" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">yazdığın her satır</text>
  <text x="205" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">CV'deki her şey</text>
  <text x="205" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sorulabilir</text>
  <path d="M263 74 L272 74" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-akis-arw)"/>
  <rect x="274" y="48" width="112" height="52" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">3 · Dil ve çatı</text>
  <text x="330" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">Java · Spring</text>
  <text x="330" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">kesin bilgi ve</text>
  <text x="330" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">tuzak soruları</text>
  <path d="M388 74 L397 74" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-akis-arw)"/>
  <rect x="399" y="48" width="112" height="52" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="455" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">4 · Senaryo</text>
  <text x="455" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sistem tasarımı</text>
  <text x="455" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">sesli düşün,</text>
  <text x="455" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">takası gerekçelendir</text>
  <path d="M513 74 L522 74" fill="none" stroke="#5D7789" stroke-width="1.3" marker-end="url(#harita-akis-arw)"/>
  <rect x="524" y="48" width="112" height="52" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="580" y="70" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">5 · Soru sorma</text>
  <text x="580" y="88" text-anchor="middle" font-size="10.5" fill="#5D7789" font-family="system-ui">sıra sende</text>
  <text x="580" y="124" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">ekip, süreç,</text>
  <text x="580" y="140" text-anchor="middle" font-size="10.5" fill="#1C3247" font-family="system-ui">teknik borç</text>
</svg>
```

1. **Isınma.** "Kendinden bahseder misin?" Bu bir hayat hikâyesi isteği değildir. İki-üç dakikada **role bağlanan** bir özet beklenir: ne yaptın, hangi teknolojiyle, hangi sonucu ürettin, neden bu pozisyon. Kronolojik olarak her işi tek tek anlatmak, ilgiyi daha başta düşürür.
2. **CV üzerinden derinleşme.** Görüşmeci CV'deki bir satırı seçip kazmaya başlar. Buradaki tek kural şudur: **CV'de yazan her şey sorulabilir.** Yazdığın ama derinine inemeyeceğin bir teknoloji, artı değil eksi puandır.
3. **Dil ve çatı soruları.** Java ve Spring'in çekirdeği sorulur. Çoğu kısa, kesin cevaplı ve tuzaklıdır. Burada "duruma göre" demek beklenmez; net cevap vermek beklenir.
4. **Senaryo ve sistem tasarımı.** "Şöyle bir akışı nasıl tasarlarsın?" Burada tek doğru cevap yoktur. Önce **kapsam sorusu sorman**, sonra kaba akışı çizmen ve her kararın takasını gerekçelendirmen beklenir. İskelet için `141`.
5. **Soru sorma faslı.** Görüşmenin son beş dakikasıdır. Buradaki soruların, teknik cevaplar kadar bilgi verdiğini unutma.

### Mülakatta ne sorulur / tuzaklar

- **Bilmediğin soru geldiğinde ne yapılır?** Uydurma. Emin bir tonla söylenen yanlış bilgi, mülakattaki en pahalı hatadır. Çünkü görüşmeci doğrusunu bilir ve bundan sonra söylediğin her şeye şüpheyle bakar. Doğru kalıp üç parçalıdır. (1) Bilmediğini açıkça söyle. (2) Bildiğin en yakın kavramdan yola çıkarak **nasıl akıl yürüteceğini** anlat. (3) Gerçek hayatta bunu nasıl doğrulayacağını söyle: "dokümandan ya da ölçerek teyit ederdim". "Bilmiyorum" deyip susmak da eksik cevaptır; dürüsttür ama düşünme biçimini göstermez.
- **"Hangisi daha iyi?" tuzağı.** Örnekler: "Monolit mi mikroservis mi?", "SQL mi NoSQL mi?", "iyimser kilit mi kötümser mi?" Bu soruların doğru cevabı **bağlama bağlıdır**. Görüşmeci çoğu zaman senin bağlam sorup sormadığını ölçer. Kalıp şudur: önce bir-iki netleştirme sorusu sor (yük, ekip, tutarlılık ihtiyacı, dağıtım olgunluğu), sonra ölçütleri say, sonra koşullu cevap ver. Örnek koşullu cevap: "Çakışma seyrekse iyimser kilit, sıcak satırda yoğun çakışma varsa kötümser kilit." Kesin ve koşulsuz bir taraf tutmak, kıdemsizlik işareti olarak okunur.
- **Sesli düşün.** Özellikle senaryo aşamasında sessiz kalıp tek seferde sonuç söylemek, görüşmeciden en değerli bilgiyi saklar. Varsayımlarını yüksek sesle kur. Örnek: "Veri tutarlılığını güçlü varsayıyorum, çünkü para hareketi var." Sonra ilerle.
- **Kesin rakam ve sürüm iddiasından kaçın.** "Bu özellik şu sürümde geldi" ya da "şu oran şudur" gibi iddialar doğrulanabilir. Emin değilsen risklidir. Onun yerine ne işe yaradığını ve hangi sorunu çözdüğünü anlat. Sayı gerekiyorsa "büyüklük mertebesi olarak" diyerek çerçevele.
- **Deneyimin olmadığı alanda köprü kur.** Alan sorusu geldiğinde üç seçenek vardır ve yalnız biri doğrudur. Örnek soru: "Bankacılık kredi kullanıcı sayfaları yaptınız mı?" Yalan söylemek en kötüsüdür; bir sonraki soru "peki taksit planını nasıl ürettiniz?" olur ve cevap çöker. Kuru bir "hayır" ise sohbeti bitirir. Doğru kalıp şöyledir: "Doğrudan kredi ekranı geliştirmedim ama akışı biliyorum: başvuru, skorlama, tahsis, kullandırım, taksit planı, tahsilat, takip. Benzer olarak şu işi yaptım: para tutarını `BigDecimal` ile ölçek ve yuvarlama kuralına bağladım, sınırlı bir kaynaktan eşzamanlı düşümü sürüm damgasıyla (`@Version`) korudum, durum makinesini ve denetim izini kurdum. Kredi tarafındaki karşılığı şudur…" Bu cevap hem dürüsttür hem de boşluğu doldurur. Alan bilgisinin tamamı için `239`.
- **CV'de yazan her şey sorulabilir.** Emin olmadığın bir teknolojiyi CV'ye yazma. Yazdıysan o konuyu mutlaka gözden geçir. Öte yandan **kendi yazdığın bir proje anlatabilmek** büyük avantajdır. Örnek bir proje düşün: Spring Boot ile yazılmış, harici bir yapay zekâ sağlayıcısına (OpenRouter) HTTP üzerinden bağlanan, geliştirmede H2 veritabanı kullanan ve katman kurallarını **ArchUnit** testleriyle zorlayan bir eğitim platformu. Bu tek örnekle beş konuyu somut olarak konuşabilirsin: bağımlılık enjeksiyonu, katmanlı mimari, dış servis çağrısında zaman aşımı ve hata yönetimi, test disiplini, mimari kuralların otomatik denetimi. Bu, soyut "bilirim" cevabından çok daha güçlüdür.
- **Soru sorma faslında ne sorulur, ne sorulmaz.** Sorulur: ekibin büyüklüğü ve çalışma biçimi, üretime çıkış sıklığı ve süreci (`191`, `220`), test ve teknik borç durumu, kod inceleme kültürü, ilk üç ayda senden beklenen katkı, sistemin bugünkü en büyük teknik sıkıntısı. Sorulmaz: hiç soru sormamak (ilgisizlik okunur), yalnız izin/yan hak/maaş sormak (bu konuşma İK aşamasına aittir), diğer adayların durumu, şirketin paylaşmayacağı iç güvenlik ve mimari ayrıntıları.
- **Alan bilgisi eksikliğini hafife alma.** Kredili sistemler ekibinde "Java Java'dır, alan fark etmez" tavrı olumsuz okunur. Çünkü bankacılıkta hatanın bedeli paradır. Üstelik geri alınamazlık, denetim izi ve mevzuat bağımlılığı yazılımı doğrudan biçimlendirir.

### Tek cümlede

İlandaki her maddeyi mülakatta fiilen sorulan konulara çevir. Yedi günü, elenme riski yüksek çekirdekten başlayarak sırala. Cevaplarda bağlam sor ve sesli düşün. Bilmediğini dürüstçe söyleyip akıl yürütmeni göster. Deneyimin olmayan alanda ise yalan yerine köprü kur.

# harita-1 | Temel
## Soru
Teknik mülakatta hiç bilmediğin bir konuda soru geldi. Aşağıdakilerden hangisi doğru davranıştır?
## A
Emin bir tonla makul görünen bir cevap üretmek; kararsız görünmek bilgisizlikten daha kötü değerlendirilir
🤖 emin tonla söylenen yanlış bilgi güveni topluca yıkar; görüşmeci doğrusunu bilir ve sonraki tüm cevaplarına şüpheyle bakar.
## B
"Bilmiyorum" deyip susmak ve bir sonraki soruya geçilmesini beklemek; bu durumda dürüstlüğün tek başına yeterli olacağını düşünmek
🤖 dürüsttür ama eksiktir; görüşmeciye nasıl düşündüğünü gösterme fırsatını hiç kullanmamış olursun.
## C
Bilmediğini açıkça söyleyip bildiğin en yakın kavramdan yola çıkarak nasıl yaklaşacağını ve sonucu nasıl doğrulayacağını sesli anlatmak
🤖 Doğru: dürüstlüğü korurken akıl yürütme biçimini gösterir; mülakatın asıl ölçtüğü şey budur.
## D
Soruyu kibarca çevirip iyi bildiğin bir konuyu uzun uzun anlatmak; böylece güçlü olduğun alan öne çıkar
🤖 konu kaçırma olarak okunur ve sorulan soruya cevap verilmediği için hem bilgi hem güven kaybı olur.
## Doğru
C
## Açıklama
Mülakatın ölçtüğü şey ansiklopedik bilgi değil, **bilinmeyenle nasıl baş ettiğindir**. Doğru kalıp üç parçalıdır: bilmediğini açıkça söyle, bildiğin en yakın kavramdan yola çıkarak nasıl akıl yürüteceğini anlat, sonra bunu gerçek hayatta nasıl doğrulayacağını ekle ("dokümandan bakar / küçük bir denemeyle ölçerim"). Emin bir tonla cevap **uydurmak** en pahalı hatadır: görüşmeci konunun doğrusunu bilir, yanlışı yakaladığı anda daha önce verdiğin doğru cevaplara da güvenmez. Yalnız "bilmiyorum" deyip susmak dürüsttür ama **eksiktir**; düşünme biçimini gösterme fırsatını harcar. Soruyu başka bir konuya çevirmek ise kaçınma olarak okunur ve çoğu görüşmeci seni aynı soruya geri getirir. Aynı ilke kesin rakam ve sürüm iddiaları için de geçerlidir: emin değilsen sayı vermek yerine mekanizmayı ve hangi sorunu çözdüğünü anlat.

# harita-2 | Orta
## Soru
Görüşmeci "Sence monolit mi daha iyidir, mikroservis mi?" diye soruyor. Bu tür "hangisi daha iyi" sorularında beklenen cevap kalıbı nedir?
## A
Önce bağlamı netleştiren birkaç soru sorup (ekip, yük, tutarlılık ihtiyacı, dağıtım olgunluğu) ölçütleri koymak ve koşullu cevap vermek
🤖 Doğru: bu sorularda ölçülen şey tarafın değil, karar ölçütlerini ve takasları konuşabilme yeteneğidir.
## B
Sektörde daha modern kabul edilen seçeneği net biçimde savunmak; kararsız görünmemek için tek bir taraf tutmak
🤖 koşulsuz taraf tutmak kıdemsizlik işareti okunur; her iki seçeneğin de doğru olduğu bağlamlar vardır.
## C
Her iki seçeneğin tanımını sırayla anlatıp "ikisi de yaygın olarak kullanılıyor" diyerek herhangi bir tercih belirtmemek
🤖 tanım tekrarı takas konuşması değildir; sonunda hiçbir ölçüt veya karar üretilmediği için cevap boş kalır.
## D
Adayın kendi son projesinde hangisini kullandıysa onu doğru cevap olarak sunmak ve o mimariyi genellemek
🤖 tek bir deneyimi genel kurala çevirmek, bağlam farklarını yok saydığı için zayıf bir gerekçelendirmedir.
## Doğru
A
## Açıklama
"Hangisi daha iyi?" soruları (monolit/mikroservis, SQL/NoSQL, iyimser/kötümser kilit, REST/gRPC) **tuzak sorularıdır**: doğru cevap seçeneklerden biri değil, **karar ölçütleridir**. Beklenen kalıp önce bir-iki **kapsam sorusu** sormak (kaç kişilik ekip, hangi yük, hangi tutarlılık ihtiyacı, dağıtım/otomasyon olgunluğu ne durumda), sonra ölçütleri açıkça saymak, sonra koşullu karar vermektir: "Ekip küçük ve alan sınırları henüz oturmamışsa modüler monolit; bağımsız ölçekleme ve bağımsız dağıtım gerçek bir ihtiyaçsa ve operasyon olgunluğu varsa mikroservis." Koşulsuz olarak modern görünen tarafı savunmak, bağlamı yok saydığı için kıdemsizlik işaretidir. İki seçeneğin tanımını art arda anlatıp tercih belirtmemek ise takas konuşması değil ansiklopedi tekrarıdır. Kendi son projesindeki tercihi genel kurala çevirmek de aynı hatanın başka biçimidir: tek bir deneyimden genel kural çıkmaz. Konuyla ilgili derinlik için `95` ve `96`.

# harita-3 | Orta
## Soru
Görüşmeci "Daha önce bankacılık kredi kullanıcı ekranları geliştirdiniz mi?" diye soruyor ve senin bu alanda doğrudan deneyimin yok. Hangi cevap doğrudur?
## A
"Evet, kredi başvuru ve limit ekranlarını uçtan uca geliştirdim" demek; alan deneyimi olmadan elenme riski çok yüksektir
🤖 bir sonraki soru mutlaka ayrıntıya iner ve tutarsızlık ortaya çıkınca yalnız bu cevap değil tüm mülakat değersizleşir.
## B
"Hayır, o alanda çalışmadım" deyip cevabı orada bitirmek; kısa ve dürüst cevabın en güvenli seçenek olduğunu düşünmek
🤖 dürüsttür ama sohbeti kapatır; sahip olduğun aktarılabilir deneyimi görüşmeciye hiç göstermemiş olursun.
## C
Alan bilgisinin önemsiz olduğunu, teknoloji ve dil aynı olduğu için sektörün ve iş alanının fark etmediğini anlatmak
🤖 bankacılıkta geri alınamazlık, denetim izi ve mevzuat yazılımı doğrudan biçimlendirir; alanı küçümsemek olumsuz okunur.
## D
Doğrudan geliştirmediğini söyleyip kredi yaşam döngüsüne dair alan bilgisini göstermek ve benzer teknik işlerini o akışa bağlamak
🤖 Doğru: dürüstlüğü koruyup boşluğu aktarılabilir deneyimle doldurur; bu "köprü cevap" kalıbıdır.
## Doğru
D
## Açıklama
Deneyimin olmadığı alanda doğru kalıp **köprü cevaptır**: önce dürüstlük, sonra aktarılabilir deneyim. Yalan söylemek en riskli seçenektir çünkü alan soruları katman katman derinleşir; "taksit planını nasıl ürettiniz, kuruş farkını nereye yazdınız?" sorusunda tutarsızlık anında görünür ve yalnız o cevap değil önceki doğru cevapların güvenilirliği de gider. Kuru bir "hayır" dürüsttür ama **eksiktir**: görüşmeciye aslında sahip olduğun ve doğrudan işe yarayan deneyimi (para tutarını `BigDecimal` ile ölçek ve yuvarlama kuralına bağlamak — `226`, sınırlı bir kaynaktan eşzamanlı düşümü sürüm damgasıyla korumak — `225`, durum makinesi ve denetim izi kurmak) göstermeden konuyu kapatır. Alanı önemsizleştirmek ise ayrı bir hatadır: bankacılıkta para hareketi **geri alınamaz**, düzeltme ters kayıtla yapılır, her işlem denetim izi bırakır ve kurallar mevzuata bağlıdır — bunlar yazılım tasarımını doğrudan değiştirir. Alan bilgisinin tamamı `239` numaralı konudadır; oradaki yaşam döngüsünü (başvuru → skorlama → tahsis → kullandırım → taksit → tahsilat → takip) doğru sırayla sayabilmek tek başına bu soruyu güçlü bir cevaba çevirir.

# harita-4 | İleri
## Soru
Mülakata bir haftan var ve bütün konulara yetişmen mümkün değil. Hangi önceliklendirme mantığı doğrudur?
## A
En rahat olduğun ve en çok sevdiğin konuları tekrar etmek; özgüvenle girmek genel performansı yükseltir
🤖 zaten bildiğin konuya harcanan saat yeni bir risk kapatmaz; boşluklar olduğu yerde durur.
## B
Elenme riski yüksek çekirdek konuları (self-invocation, N+1, izolasyon/kilit, BigDecimal) öne almak, egzotik konuları sona bırakmak
🤖 Doğru: bu konular günlük üretim kodunun göstergesi sayıldığı için bilinmemesi tek başına eleyici olur.
## C
İlanda geçmeyen ama sektörde popüler olan konularla (GraalVM native image, WebFlux, Kafka Streams) başlamak
🤖 ilanla eşleşmeyen konular önce sorulmaz; çekirdek boşken ileri konu anlatmak inandırıcılığı da düşürür.
## D
Tüm haftayı tek bir konuya (ör. Kubernetes) ayırıp o alanda mümkün olan en yüksek derinliğe ulaşmak
🤖 mülakat yüzeyi geniştir; tek alanda derinleşip diğer dört alanı boş bırakmak beklenen kapsamı karşılamaz.
## Doğru
B
## Açıklama
Sınırlı zamanda önceliklendirmenin ölçütü **elenme riski**dir: bilinmediğinde tek başına olumsuz karar ürettirebilecek konular önce çalışılır. Bu konular egzotik değildir; `@Transactional` self-invocation (`83`), N+1 (`82`), izolasyon ve kilitleme (`224`, `225`), `BigDecimal` ile para (`226`), probe ve `OOMKilled` (`105`, `218`), idempotency ve outbox (`130`, `116`), `equals`/`hashCode` (`7`, `81`) ve istisna hiyerarşisi (`16`, `17`) — hepsi görüşmeci gözünde "bu kişi üretim kodu yazmış mı?" sorusunun dolaylı biçimidir. Zaten iyi bildiğin konuları tekrar etmek psikolojik olarak rahatlatır ama **hiçbir riski kapatmaz**; boşluklar olduğu yerde kalır. İlanda geçmeyen popüler konularla başlamak, sorulma olasılığı düşük alana yatırım yapmaktır ve çekirdek boşken ileri konu anlatmak inandırıcılığı da azaltır. Tüm haftayı tek konuya ayırmak ise mülakatın geniş yüzeyini karşılamaz: aynı görüşmede Spring, veri katmanı, mikroservis, konteyner ve eşzamanlılık başlıklarının hepsine dokunulur. Doğru düzen, yedi günü bu risk sırasına göre bölmek ve son günü yeni konu açmadan tekrar ile provaya ayırmaktır.

# harita-5 | İleri
## Soru
Mülakatın sonunda "Senin bize sormak istediğin bir şey var mı?" deniyor. Bu fasılda hangi yaklaşım doğrudur?
## A
Hiç soru sormamak ve görüşmeyi erken bitirerek görüşmecinin zamanını gereksiz yere işgal etmemiş olmak
🤖 soru sormamak neredeyse her zaman ilgisizlik veya hazırlıksızlık olarak okunur.
## B
Yalnızca maaş, yan haklar, izin süresi ve mesai düzenini sormak; asıl belirleyici konular bunlardır
🤖 bu konular önemlidir ama teknik görüşmenin değil İK aşamasının konusudur; burada sorulması odak kaymasıdır.
## C
Ekibin çalışma biçimi, üretime çıkış sıklığı, test ve teknik borç durumu, ilk üç ayda beklenen katkı gibi işin gerçeğini ölçen sorular sormak
🤖 Doğru: hem senin için gerçek bilgi üretir hem de mühendislik olgunluğunu görüşmeciye gösterir.
## D
Şirketin iç sistemlerinin mimari ve güvenlik ayrıntılarını, diğer adayların durumunu ve kendi değerlendirme notunu sormak
🤖 paylaşılamayacak bilgileri talep etmek ve kıyas istemek sınır ihlali olarak okunur.
## Doğru
C
## Açıklama
Soru sorma faslı, mülakatın hâlâ değerlendirilen bir parçasıdır ve iki yönlü çalışır: hem sen şirketi ölçersin hem görüşmeci senin nelere önem verdiğini görür. İyi sorular işin gerçeğini açar: ekip kaç kişi ve nasıl çalışıyor, **üretime ne sıklıkla çıkılıyor** ve süreç nasıl işliyor (`191`, `220`), otomatik test ve kod inceleme kültürü ne durumda, bugün sistemin en büyük teknik sıkıntısı ne, benden ilk üç ayda ne bekleniyor. Hiç soru sormamak, hazırlıksızlık ya da ilgisizlik olarak okunur ve kolayca kaçınılabilecek bir kayıptır. Maaş, yan hak ve izin soruları meşrudur ama **yeri teknik görüşme değildir**; bunları İK aşamasına bırakmak hem odak hem görgü açısından doğrudur. Şirketin iç sistemlerinin güvenlik/mimari ayrıntılarını, diğer adayların durumunu veya kendi puanını sormak ise paylaşılamayacak bilgiyi talep etmektir ve sınır ihlali olarak değerlendirilir. Sorularını önceden hazırla ve görüşme sırasında duyduğun bir ayrıntıya bağla — hazır listeden okumak yerine dinlediğini göstermek daha güçlüdür.

# harita-6 | İleri
## Soru
Görüşmeci açık uçlu bir senaryo veriyor: "Kredi başvurusu alan servisi nasıl tasarlarsın?" Bu aşamada beklenen davranış nedir?
## A
Doğrudan bir teknoloji listesi sayarak başlamak (Kafka, Redis, Kubernetes) ve sonra bunların üzerine akışı kurmak
🤖 gereksinim netleşmeden seçilen teknoloji, çözülmemiş bir soruna verilmiş cevaptır; araç gerekçesiz kalır.
## B
Önce gereksinim ve kısıtları netleştirmek, sonra kaba akışı kurup her kararın takasını sesli gerekçelendirmek ve varsayımları söylemek
🤖 Doğru: tasarım sorusunda ölçülen şey nihai şema değil, gereksinimden karara giden akıl yürütmedir.
## C
Sessizce düşünüp tasarımı zihinde bitirmek ve yalnız nihai çözümü tek seferde, baştan sona derli toplu biçimde anlatmak
🤖 görüşmeci sonucu değil süreci ölçer; sessiz kalmak ara adımları ve varsayımları görünmez kılar.
## D
En yüksek ölçeği hedefleyen, en fazla bileşen içeren tasarımı seçmek; böylece büyük sistem deneyimi gösterilmiş olur
🤖 gereksinimin ötesinde karmaşıklık üretmek aşırı mühendislik sayılır ve olgunluk değil tecrübesizlik işaretidir.
## Doğru
B
## Açıklama
Sistem tasarımı aşamasının tek doğru cevabı yoktur; ölçülen şey **gereksinimden karara giden yoldur**. Doğru sıra şudur: önce **kapsam soruları** (kim başvuruyor, günlük/anlık hacim ne, hangi dış servislere bağımlıyız ve yavaşlarlarsa ne olacak, hangi adım güçlü tutarlılık istiyor, hangi kayıtlar denetim izi gerektiriyor), sonra kaba akış (başvuru kaydı → doğrulamalar → dış skor sorgusu → karar → limit tesisi → sonuç bildirimi), sonra her kararın **takası**: dış servis çağrısına zaman aşımı ve devre kesici (`101`), yeniden denemede aynı başvurunun iki kez işlenmemesi için idempotency (`130`), limit düşümünde eşzamanlılık koruması (`225`), adımlar arası tutarlılık için saga ve telafi (`126`). Teknoloji listesiyle başlamak, henüz tanımlanmamış bir soruna araç seçmektir ve her seçimi gerekçesiz bırakır. Sessizce düşünüp yalnız sonucu söylemek, görüşmeciden asıl değerlendirdiği ara adımları saklar — bu yüzden **sesli düşünmek** ve varsayımları duyurmak beklenir. En büyük ölçeği hedefleyip bileşen sayısını artırmak ise aşırı mühendisliktir; gereksinim yokken karmaşıklık eklemek olgunluk değil deneyimsizlik işareti sayılır. Beş adımlı iskelet için `141`, kapasite tahmini için `142`.
