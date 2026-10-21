---
slug: sozlu-prova-uygulama-katmani
baslik: "Sözlü Prova · Uygulama Katmanı: REST Tasarımı, Hata Toleransı ve Test Sınırları"
kademe: İleri
sira: 263
etiketler: 11
---

## Konu anlatımı

Bu dosyadaki üç soru gerçek mülakatlarda soruldu. Üçü de aynı kalıptadır: önce tanım, sonra karar, sonra deneyim. Tanımı bilmek tek başına yetmez; görüşmeci senden bir tercih ve gerekçe duymak ister. Her sorunun altında dört parça bulunur. Önce sorunun ne ölçtüğü, ardından sözlü cevabın adım adım iskeleti gelir. Sonra elenme sebepleri ve kendi hikâyeni yerleştireceğin boşluk gelir. Dosyanın sonunda bu malzemeyi nasıl prova edeceğin anlatılır.

### "Spring Boot ile bir REST API tasarlarken katmanlama, DTO kullanımı ve doğrulama yaklaşımınız nedir?"

**Soru aslında ne ölçüyor?**

Görüşmeci burada tanım da istiyor, ama ağırlık **muhakemede**. Katmanları saymak kolaydır; her kıdemli aday sayabilir. Ayrıştırıcı olan şey, katmanların **neyi ayırdığını** söyleyebilmektir. Bu soru aslında tek bir şeyi yokluyor: dış sözleşme ile iç veri modelini birbirinden ayırıyor musun? Doğrulama kısmı ise sessiz bir tuzaktır: çoğu aday doğrulamayı anotasyona indirger ve iş kuralı doğrulamasından hiç söz etmez. Bu ayrımı yapan aday, aynı dakikada diğerlerinden ayrışır.

**Sözlü cevap iskeleti**

1. Önce katmanları ve her birinin **tek** sorumluluğunu bir cümlede ver. Denetleyici (controller) sözleşmeyi karşılar, servis iş kuralını yürütür, depo (repository) veriye erişir.
2. Sonra sınırı koy: varlık (entity) nesnesi dışarı çıkmaz. Üç zararını somut say — iç modelin sözleşmeye sızması, döngüsel serileştirme, istemeden alan ifşası.
3. Girdi ve çıktı DTO'larını ayırdığını söyle ve gerekçesini kısa tut: istek alanları ile yanıt alanları aynı küme değildir, salt okunur alanların isteğe girmesi de risklidir.
4. Doğrulamayı **iki seviyeye** ayır: biçim doğrulaması sınırda durur (`@Valid` ve kısıt anotasyonları), iş kuralı doğrulaması ise servis katmanındadır çünkü ek veri ve bağlam ister.
5. Hata sözleşmesini anlat: bütün uç noktalarda tek biçim bir gövde kullandığını söyle (`ProblemDetail`) ve durum kodu seçimini gerekçelendir.
6. Son adımda kendi projendeki bir karara geçerek tanımdan deneyime köprü kur; iskeletin geri kalanı artık senin hikâyendir.

**Elenme sebepleri**

- "DTO gereksiz katman, varlık nesnesini doğrudan dönüyoruz" demek: sözleşme ile tablo şemasının aynı şey sanıldığını gösterir.
- Doğrulamayı yalnız anotasyonlara indirgemek: iş kuralı doğrulaması yok sayılır, kural denetleyiciye ya da hiçbir yere düşer.
- Katmanları ezberden saymak ama neyi ayırdığını söyleyememek: papağan cevap olarak okunur.
- İş kuralını denetleyicinin içine koymak: katman ayrımının sözde kaldığı anlaşılır.
- Her hatada 200 dönüp gövdeye hata alanı koymak: istemci tarafında hata işlemeyi imkânsızlaştırır.
- Her metotta ayrı `try/catch` yazmak: merkezî hata işleyiciden habersiz olunduğunu gösterir.
- 400 ile 422 ya da 409 arasındaki farkı hiç düşünmemiş olmak: sözleşme tasarımının ciddiye alınmadığı izlenimi verir.

**Kendi hikâyen**

Buraya senin örneğin gelecek. Şu sorularla kendi vakanı çıkar:

- Hangi projede varlık nesnesi doğrudan dışarı veriliyordu ve bunu sana fark ettiren şey ne oldu?
- İç modelde yapılan bir değişiklik istemciyi hiç kırdı mı, kırdıysa durum nasıl toparlandı?
- Doğrulamayı nereye koydun, neden orası, sonradan yerini değiştirmek zorunda kaldın mı?
- Hata sözleşmesini standart bir biçime çektin mi, öncesinde istemci tarafındaki ekip ne yaşıyordu?
- Bir alanın istemeden dışarı sızdığı bir durum oldu mu, bunu nasıl kapattın?

Bu deneyimlerin hiçbiri yoksa uydurma; onun yerine dürüst köprü kalıbını kur, yani sınırı söyle, komşu deneyimini somutla ve farkı bildiğini göster. Kalıbın tamamı `256` numaralı konudadır. İçerik derinliği için `76`, `77`, `78`, `137` ve `154` numaralı konulara bak.

```svg
<svg viewBox="0 0 660 268" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="REST katmanlari, DTO siniri ve varligi disari vermenin uc zarari">
  <defs>
    <marker id="uygkat-ok1" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Katmanlar ve sözleşme sınırı</text>
  <rect x="14" y="40" width="116" height="96" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="72" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">İstemci</text>
  <text x="72" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">JSON sözleşmesi</text>
  <text x="72" y="98" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sürüm bağı burada</text>
  <text x="72" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kırılırsa sen ödersin</text>
  <line x1="132" y1="88" x2="143" y2="88" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok1)"/>
  <rect x="145" y="40" width="116" height="96" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="203" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">Denetleyici</text>
  <text x="203" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">girdi DTO / çıktı DTO</text>
  <text x="203" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">biçim doğrulaması</text>
  <text x="203" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">iş kuralı YOK</text>
  <line x1="263" y1="88" x2="274" y2="88" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok1)"/>
  <rect x="276" y="40" width="116" height="96" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="334" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">Servis</text>
  <text x="334" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iş kuralı doğrulaması</text>
  <text x="334" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">işlem sınırı</text>
  <text x="334" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">alan modeli burada</text>
  <line x1="394" y1="88" x2="405" y2="88" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok1)"/>
  <rect x="407" y="40" width="116" height="96" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="465" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">Depo</text>
  <text x="465" y="82" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">varlık nesnesi</text>
  <text x="465" y="98" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sorgu ve eşleme</text>
  <text x="465" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kalıcı bağlam</text>
  <line x1="525" y1="88" x2="536" y2="88" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok1)"/>
  <rect x="538" y="40" width="108" height="96" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="592" y="62" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">Veritabanı</text>
  <text x="592" y="82" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">tablo şeması</text>
  <text x="592" y="98" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">kısıtlar</text>
  <text x="592" y="118" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">son savunma</text>
  <line x1="138" y1="34" x2="138" y2="146" stroke="#B23B3B" stroke-width="2" stroke-dasharray="5 4"/>
  <text x="138" y="158" text-anchor="middle" font-size="10" font-weight="700" fill="#B23B3B" font-family="system-ui">sözleşme sınırı: varlık nesnesi bu çizgiyi geçmez</text>
  <text x="330" y="186" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Varlık nesnesi dışarı verilirse ne olur</text>
  <rect x="14" y="196" width="206" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="117" y="216" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">1 · İç model sözleşme olur</text>
  <text x="117" y="234" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">sütun adı değişince</text>
  <text x="117" y="249" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">istemci kırılır</text>
  <rect x="228" y="196" width="206" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="331" y="216" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">2 · Serileştirme tuzağı</text>
  <text x="331" y="234" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çift yönlü ilişki döngüsü</text>
  <text x="331" y="249" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">tembel alanda hata</text>
  <rect x="442" y="196" width="204" height="62" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="544" y="216" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">3 · İstemsiz alan ifşası</text>
  <text x="544" y="234" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">iç kullanım alanları</text>
  <text x="544" y="249" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yanıtta görünür</text>
</svg>
```

### "Mikroservislerde hata toleransı için timeout, retry, circuit breaker ve idempotency'yi nasıl uygularsınız?"

**Soru aslında ne ölçüyor?**

Bu soruda ağırlık açıkça **muhakemededir**, çünkü dört terimin tanımını hemen herkes ezberleyebilir. Görüşmecinin aradığı şey başka: bunların **tek bir zincir** olduğunu görüyor musun? Zayıf aday dört maddeyi sırayla tanımlar ve orada durur; güçlü aday ise aralarındaki bağı kurar. Zincirin halkaları birbirini besler ve sırası vardır. Zaman aşımı yoksa devre kesici (circuit breaker) hata sinyali alamaz. Tekrar güvenliği (idempotency) yoksa yeniden deneme (retry) çift işlem üretir; cevabının merkezine bu iki cümleyi koy.

**Sözlü cevap iskeleti**

1. İlk cümlede çerçeveyi kur ve "bunlar dört ayrı özellik değil, tek bir savunma zinciri" de; bu tek cümle bile seni çoğu adaydan ayırır.
2. Zinciri sırayla kur ve her halkanın işini tek cümlede ver: zaman aşımı bekleyişi sınırlar, yeniden deneme geçici hatayı yutar, devre kesici bozuk bağımlılığa gitmeyi durdurur, tekrar güvenliği ise yeniden denemeyi zararsız kılar.
3. Sıranın neden önemli olduğunu iki cümleyle söyle: zaman aşımı olmadan çağrı hata üretmez ve devre kesici de sayamaz. Tekrar güvenliği olmadan yeniden deneme aynı işi ikinci kez yaptırır.
4. Yeniden denemenin **ne zaman yapılmaması** gerektiğini say: kalıcı hatalarda deneme boşunadır, çünkü istek zaten geçersizdir. Tekrar güvenli olmayan işlemlerde ise deneme doğrudan tehlikelidir.
5. Geri çekilmeyi (backoff) ve dalgalandırmayı (jitter) açıkla: sabit aralık yükü aynı anda geri getirir, dalgalandırma ise bu dalgayı zamana yayar.
6. Yedek davranışın (fallback) teknik değil bir **iş kararı** olduğunu söyle: eski fiyatı göstermek ürün sahibinin kararıdır, geliştiricinin değil.

Zincirin son halkası için bir ayrıntı ekleyebilirsen puan alırsın. Tekrar güvenliği anahtarının kaydı ile işin kendisi aynı işlemde olmalıdır. Anahtar önce kaydedilip iş sonra yapılırsa, arada düşen bir süreç işi hiç yapmamış olur. İş önce yapılıp anahtar sonra kaydedilirse, aynı istek ikinci kez işlenir.

**Elenme sebepleri**

- Dördünü ayrı ayrı tanımlayıp aralarındaki bağı hiç kurmamak: ezber izlenimi bırakır.
- "Yeniden deneme koyarız" deyip tekrar güvenliğinden söz etmemek: bu soruda en sık eleyen cevaptır.
- Sabit aralıklı ve sınırsız yeniden deneme önermek: çöken servise yükü geri yığar.
- Devre kesici eşiklerini "kütüphane varsayılanı" diye geçiştirmek: eşiklerin trafiğe göre seçildiği bilinmiyor demektir.
- Zaman aşımını yalnız bağlantı zaman aşımı sanmak: asıl tehlike okuma zaman aşımının olmamasıdır.
- Yedek cevabı teknik bir ayrıntı sanmak: ödemede yanlış yedek davranış para kaybettirir.
- Katman katman konan yeniden denemelerin çarpıldığını görmemek: üç katman üç deneme dokuz çağrı eder.

**Kendi hikâyen**

Şu sorularla kendi vakanı çıkar:

- Hangi entegrasyonda zaman aşımı tanımsız kalmıştı, belirtisi neydi, iş parçacıkları mı doldu?
- Yeniden deneme yüzünden çift kayıt ya da çift çekim yaşandı mı, bu nasıl anlaşıldı?
- Tekrar güvenliği anahtarını akışın neresine koydun ve anahtarı hangi alanlardan ürettin?
- Devre kesici eşiğini neye göre seçtin, sonradan bu eşiği değiştirmek zorunda kaldın mı?
- Yedek davranışa kim karar verdi ve o kararın iş tarafındaki karşılığı neydi?

Deneyimin yoksa uydurma; `256` numaralı konudaki dürüst köprü kalıbını kur. Derinleşmek için `101`, `130`, `126`, `116`, `153` ve ödeme tarafı için `251` numaralı konulara bak.

```svg
<svg viewBox="0 0 660 286" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Zaman asimi, yeniden deneme, devre kesici ve tekrar guvenligi zinciri ve sirasi">
  <defs>
    <marker id="uygkat-ok2" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Dört mekanizma değil, tek zincir</text>
  <rect x="14" y="38" width="146" height="88" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="87" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#186399" font-family="system-ui">1 · Zaman aşımı</text>
  <text x="87" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bekleyişi sınırlar</text>
  <text x="87" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bağlantı ve okuma</text>
  <text x="87" y="114" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">hatayı görünür kılar</text>
  <line x1="162" y1="82" x2="174" y2="82" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok2)"/>
  <rect x="176" y="38" width="146" height="88" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="249" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#25804C" font-family="system-ui">2 · Yeniden deneme</text>
  <text x="249" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geçici hatayı yutar</text>
  <text x="249" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">geri çekilme + dalgalandırma</text>
  <text x="249" y="114" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">sınırlı sayıda</text>
  <line x1="324" y1="82" x2="336" y2="82" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok2)"/>
  <rect x="338" y="38" width="146" height="88" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="411" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#B07D20" font-family="system-ui">3 · Devre kesici</text>
  <text x="411" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">bozuk hedefe gitmeyi</text>
  <text x="411" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">durdurur</text>
  <text x="411" y="114" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">açık · yarı açık · kapalı</text>
  <line x1="486" y1="82" x2="498" y2="82" stroke="#5D7789" stroke-width="1.5" marker-end="url(#uygkat-ok2)"/>
  <rect x="500" y="38" width="146" height="88" rx="10" fill="#F2F5F7" stroke="#5D7789" stroke-width="1.5"/>
  <text x="573" y="60" text-anchor="middle" font-size="11.5" font-weight="700" fill="#1C3247" font-family="system-ui">4 · Tekrar güvenliği</text>
  <text x="573" y="80" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">aynı istek iki kez</text>
  <text x="573" y="96" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">gelse de tek işlem</text>
  <text x="573" y="114" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">istek anahtarı ile</text>
  <text x="330" y="152" text-anchor="middle" font-size="12.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Halka eksikse zincir ne yapar</text>
  <rect x="14" y="162" width="316" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="172" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Zaman aşımı yoksa (1 eksik)</text>
  <text x="172" y="201" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">çağrı askıda kalır · hata sayılmaz · devre kesici kör kalır</text>
  <rect x="340" y="162" width="306" height="52" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="493" y="182" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B23B3B" font-family="system-ui">Tekrar güvenliği yoksa (4 eksik)</text>
  <text x="493" y="201" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">her yeniden deneme yeni bir işlem üretir · çift çekim</text>
  <rect x="14" y="224" width="632" height="50" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="330" y="244" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Sıra neden önemli</text>
  <text x="330" y="263" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">1 ölçmeyi başlatır · 2 ancak 4 varsa güvenlidir · 3 ancak 1 varsa sinyal alır</text>
</svg>
```

### "JUnit ve Mockito ile sınırlarınızı nasıl belirlersiniz; test edilebilir tasarım için ne yaparsınız?"

**Soru aslında ne ölçüyor?**

Sorunun içindeki "sınır" kelimesi belirsiz bırakılmıştır ve bu kasıtlıdır. Görüşmeci senin bu belirsizliği nasıl çözdüğünü izler. Zayıf aday sınırı **kapsam yüzdesi** sanır ve sayı verir. Doğru okuma şudur: sınır, **neyi taklit ettiğindir**. Sorunun ikinci yarısı ise aslında bir tasarım sorusudur: test yazması zor olan kod, çoğu zaman kötü tasarlanmış koddur. Görüşmeci senin bu bağı kurup kurmadığına bakar.

**Sözlü cevap iskeleti**

1. Önce soruyu yeniden çerçevele ve "benim için sınır kapsam oranı değil, neyi taklit ettiğim" de; bu cümle konuşmanın tonunu belirler.
2. Kuralı tek cümlede ver: kendi kodunu taklit etme, yalnız sistem sınırlarını taklit et. Sınır demek dış servis, saat, rastgelelik, kuyruk ve dosya sistemi demektir.
3. Aşırı taklidin bedelini anlat: test üretim kodunun aynasına döner ve davranış hiç değişmeden yapılan bir yeniden düzenleme testleri kırar. O noktada test bir güvenlik ağı olmaktan çıkıp yüke dönüşür.
4. Birim ile entegrasyon sınırını çiz: birim testi kuralın doğruluğunu sorar, entegrasyon testi ise parçalar birleşince çalışıp çalışmadığını sorar. İkisi farklı soruya cevap verir ve biri diğerinin yerine geçmez.
5. Test edilebilir tasarımı **somut** karşılıklarla say. Kurucu enjeksiyonu, saatin bir `Clock` olarak dışarıdan verilmesi, rastgeleliğin dışarıdan verilmesi, statik çağrılardan kaçınma, iş kuralının saf fonksiyonlara çekilmesi.
6. Son adımda dayanıklılık ayarlarını nasıl sınadığını söyle: taklidi servis arayüzünde değil, doğrudan HTTP sınırında kurarsın. Kontrollü hata üreten sahte bir sunucu kullanırsan yeniden deneme ve devre kesici gerçekten devreye girer.

**Elenme sebepleri**

- Sınırı kapsam yüzdesiyle tanımlamak: kapsamın bir yan çıktı olduğu bilinmiyor demektir.
- Her işbirlikçiyi taklit edip testi çağrı sırasına bağlamak: her yeniden düzenleme testleri kırar.
- Statik çağrıyı taklit eden eklentiyi çözüm diye sunmak: tasarım sorunu yerinde kalır, üstü örtülür.
- "Entegrasyon testi yazarız, birim teste gerek yok" demek: geri bildirim döngüsü dakikalara çıkar.
- Testte gerçek dış sağlayıcıya bağlanmak: testler yavaşlar ve başkasının ortamına bağımlı olur.
- Zamana bağlı davranışı `Thread.sleep` ile sınamak: test hem yavaş hem kararsız olur.
- Test edilebilir tasarımı yalnız slogan olarak söylemek: somut karşılık verilemezse ezber sayılır.

**Kendi hikâyen**

Şu sorularla kendi vakanı çıkar:

- Hangi testin bakımı seni yordu, test neden kırılıyordu, davranış gerçekten değişiyor muydu?
- Test yazamadığın için tasarımı değiştirdiğin bir an oldu mu, tam olarak neyi değiştirdin?
- Saat ya da rastgelelik yüzünden kararsız bir test yaşadın mı, bunu nasıl sabitledin?
- Testleri hızlandırmak için hangi kararı verdin ve koşum süresi ne kadar düştü?
- Dış servise bağlı bir testi sınıra taşıdın mı, taşıdıysan sonuç ne oldu?

Deneyimin yoksa dürüst köprüyü kur (`256`). İçerik derinliği için `90`, `187`, `188`, `261` ve tasarım tarafı için `131` numaralı konulara bak.

```svg
<svg viewBox="0 0 660 262" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Neyi taklit etmeli neyi etmemeli ve asiri taklidin bedeli">
  <defs>
    <marker id="uygkat-ok3" markerWidth="9" markerHeight="9" refX="7" refY="4.5" orient="auto"><path d="M0 0 L9 4.5 L0 9 z" fill="#5D7789"/></marker>
  </defs>
  <text x="330" y="22" text-anchor="middle" font-size="15" font-weight="700" fill="#1C3247" font-family="system-ui">Sınır sorusunun cevabı: neyi taklit ediyorsun</text>
  <rect x="14" y="38" width="310" height="128" rx="10" fill="#E9F4EE" stroke="#25804C" stroke-width="1.5"/>
  <text x="169" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#25804C" font-family="system-ui">Taklit ET — sistem sınırları</text>
  <text x="169" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">dış servis çağrısı (HTTP, gRPC)</text>
  <text x="169" y="100" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">saat: Clock dışarıdan verilir</text>
  <text x="169" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">rastgelelik ve kimlik üretimi</text>
  <text x="169" y="136" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kuyruk, dosya sistemi, e-posta</text>
  <text x="169" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">senin denetiminde olmayan her şey</text>
  <rect x="336" y="38" width="310" height="128" rx="10" fill="#FBE9E9" stroke="#B23B3B" stroke-width="1.5"/>
  <text x="491" y="60" text-anchor="middle" font-size="12" font-weight="700" fill="#B23B3B" font-family="system-ui">Taklit ETME — kendi kodun</text>
  <text x="491" y="82" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kendi alan sınıfların ve kuralların</text>
  <text x="491" y="100" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">saf yardımcı fonksiyonlar</text>
  <text x="491" y="118" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">değer nesneleri ve DTO'lar</text>
  <text x="491" y="136" text-anchor="middle" font-size="10" fill="#1C3247" font-family="system-ui">kullandığın kütüphanenin iç sınıfları</text>
  <text x="491" y="156" text-anchor="middle" font-size="9.5" fill="#5D7789" font-family="system-ui">gerçeğini kullan, daha hızlı ve daha dürüst</text>
  <line x1="491" y1="170" x2="491" y2="184" stroke="#B23B3B" stroke-width="1.5" marker-end="url(#uygkat-ok3)"/>
  <rect x="336" y="188" width="310" height="60" rx="10" fill="#FDF3E0" stroke="#B07D20" stroke-width="1.5"/>
  <text x="491" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#B07D20" font-family="system-ui">Aşırı taklidin bedeli</text>
  <text x="491" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test üretim kodunun aynası olur</text>
  <text x="491" y="242" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">davranış değişmeden testler kırılır</text>
  <line x1="169" y1="170" x2="169" y2="184" stroke="#25804C" stroke-width="1.5" marker-end="url(#uygkat-ok3)"/>
  <rect x="14" y="188" width="310" height="60" rx="10" fill="#EAF1F7" stroke="#186399" stroke-width="1.5"/>
  <text x="169" y="208" text-anchor="middle" font-size="10.5" font-weight="700" fill="#186399" font-family="system-ui">Kazanç</text>
  <text x="169" y="226" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">yeniden düzenleme testleri kırmaz</text>
  <text x="169" y="242" text-anchor="middle" font-size="9.5" fill="#1C3247" font-family="system-ui">test davranışı sabitler, biçimi değil</text>
</svg>
```

### Provayı nasıl yaparsın?

Bu dosya okunmak için değil, **sesli çalışılmak** için yazıldı. Sırayı bozma:

1. Soru başlığını oku, sonra iskeleti **görmeden** kapat ve cevabı yüksek sesle ver; içinden geçirmek prova sayılmaz.
2. Süre tut ve 90 saniyeyi hedefle; çok kısa cevap yüzeysel, çok uzun cevap dağınık algılanır.
3. Cevabını telefonla kaydet ve sonra dinle; kendi sesini dinlemek elde edebileceğin en hızlı geri bildirimdir.
4. Şimdi iskeletle karşılaştır ve hangi adımı hiç söylemediğini, hangisini sıradan önce söylediğini işaretle.
5. Elenme sebeplerini tek tek gözden geçir ve kendi cevabında bunlardan birinin geçip geçmediğine bak.
6. "Kendi hikâyen" sorularını cevapla ve çıkan malzemeyi tek paragraf hâlinde yaz; yazmadan ezberlemeye çalışma.
7. Ertesi gün aynı soruyu iskelete hiç bakmadan tekrarla; ikinci turda hedef süre 75 saniyedir.

Bir uyarı: iskeleti kelimesi kelimesine ezberleme, çünkü ezberlenmiş cevap tonundan belli olur ve güven kaybettirir. Ezberlenecek şey cümleler değil **sıradır**; sıra aklında kalırsa cümleleri o an rahatça kurarsın.

### Tek cümlede

Bu üç soruda kazanan cevap tanımı sayan değil, ayrımı ve sırayı kuran cevaptır: sözleşmeyi iç modelden ayırmak, dört dayanıklılık halkasını tek zincir olarak anlatmak, taklit sınırını sistem sınırına koymak.

# sozlu2-1 | Temel
## Soru
Bir başvuru uç noktasına gelen istekte iki ayrı kontrol gerekiyor. Birincisi: `tutar` alanı boş olmamalı ve pozitif olmalı. İkincisi: müşterinin kalan limiti bu tutarı karşılamalı. Bu iki kontrolün doğru yerleşimi hangisidir?
## A
İkisi de istek DTO'suna kısıt anotasyonu olarak yazılır, servis ayrıca doğrulama yapmaz
🤖 limit kontrolü müşteri verisi ister; anotasyon istek gövdesinden başkasını göremez.
## B
İkisi de servis katmanında elle yazılır, DTO tarafında hiç kısıt anotasyonu kullanılmaz
🤖 biçim kontrolünü elle yazmak tekrar üretir ve hata biçimini tutarsızlaştırır.
## C
Biçim kontrolü DTO kısıtlarıyla sınırda, limit kontrolü servis katmanında iş kuralı olarak
🤖 Doğru: iki seviye ayrıdır, biçim sınırda durur, iş kuralı servis içinde kalır.
## D
İkisi de veritabanı kısıtlarına bırakılır, uygulama yalnız oluşan hatayı yakalayıp döndürür
🤖 veritabanı kısıtı son savunmadır; istemciye anlamlı bir hata da dönmez.
## Doğru
C

## Açıklama
Doğrulamanın iki seviyesi vardır ve karıştırılmamalıdır. **Biçim doğrulaması** isteğin kendisine bakar: alan var mı, boş mu, aralıkta mı, biçimi doğru mu. Bu kontrol sınırda, yani DTO üzerinde kısıt anotasyonlarıyla yapılır ve `@Valid` ile tetiklenir. Tek bir istek gövdesine bakarak karar verilebildiği için sınırda durması doğrudur (`77`). **İş kuralı doğrulaması** ise başka veri ister. Müşterinin kalan limiti veritabanından okunur, belki başka servisten gelir, belki iş gününe bağlıdır. Böyle bir kural anotasyonla ifade edilemez; servis katmanına aittir (`137`). A şıkkı bu farkı görmez ve anotasyondan yapamayacağı şeyi bekler. B şıkkı biçim kontrolünü elle yazdırır; hem kod tekrarı üretir hem de hata yanıtlarının biçimi uç noktadan uç noktaya değişir. D şıkkı kontrolü tamamen veritabanına bırakır. Veritabanı kısıtı değerli bir son savunmadır, ama istemciye "hangi alan neden reddedildi" bilgisini veremez. Hata yanıtının tek biçimde dönmesi için merkezî bir hata işleyici ve standart bir gövde kullanılır (`78`, `154`).

# sozlu2-2 | Orta
## Soru
Bir ekip JPA varlık sınıflarını denetleyiciden doğrudan döndürüyor. Varlıkta bazı ilişkiler tembel yükleniyor ve yalnız iç kullanım için tutulan alanlar da var. Bu tercihin en doğru okuması hangisidir?
## A
İç veri modeli dış sözleşmeye dönüşür, şema değişimi istemciyi kırar ve alanlar ifşa olur
🤖 Doğru: sözleşme ile tablo şeması aynı şey değildir, ayrımı DTO korur.
## B
Sorun yalnızca başarımdır, yanıt gövdesi biraz büyür ama sözleşme açısından bir risk doğmaz
🤖 asıl risk sözleşmenin iç modele bağlanmasıdır, gövde boyutu ikincil kalır.
## C
Tembel alanlar serileştirme sırasında sessizce boş döner, bunun dışında bir etkisi olmaz
🤖 sessizce boş dönmez; oturum kapalıysa serileştirme hata fırlatır.
## D
Yanıt sınıfına `@JsonIgnore` eklenince sorun tamamen biter, ayrıca DTO'ya gerek kalmaz
🤖 anotasyon tek tek alan gizler; sözleşme yine varlık şemasına bağlı kalır.
## Doğru
A

## Açıklama
Varlık nesnesini doğrudan döndürmek üç ayrı zarar üretir. Birincisi ve en ağırı **sözleşmenin iç modele bağlanmasıdır**. Bir sütunu yeniden adlandırdığında ya da bir alanı kaldırdığında istemci kırılır; artık veritabanı şemanı serbestçe değiştiremezsin (`154`). İkincisi **serileştirme tuzağıdır**. Çift yönlü ilişkiler döngü üretir, tembel yüklenen bir ilişki de kalıcı bağlam kapandıktan sonra serileştirilmeye çalışılırsa hata fırlatır. Üçüncüsü **istemsiz alan ifşasıdır**: iç kullanım için tutulan bir alan, kimse fark etmeden yanıtta görünür hale gelir. Çözüm girdi ve çıktı için ayrı DTO kullanmaktır (`76`). B şıkkı sorunu başarıma indirger; oysa asıl maliyet sürüm bağıdır. C şıkkı davranışı yanlış anlatır, tembel alan sessizce boş dönmez. D şıkkı bir yama önerir. `@JsonIgnore` tek tek alanları gizler, ama sözleşme hâlâ varlık sınıfının yapısına bağlıdır ve her yeni alan varsayılan olarak dışarı açıktır.

# sozlu2-3 | Orta
## Soru
Bir servis, dış sağlayıcıya gönderdiği isteklerde **tüm** hata türlerinde üç kez yeniden deniyor. Denemeler arasındaki bekleme süresi sabit. Bu kurulumun en doğru düzeltmesi hangisidir?
## A
Deneme sayısını beşe çıkarmak ve aralığı sabit tutarak başarı ihtimalini yükseltmek
🤖 sayıyı artırmak kalıcı hatayı çözmez, yalnız bozuk hedefe binen yükü büyütür.
## B
Yeniden denemeyi tümüyle kaldırmak ve oluşan her hatayı doğrudan istemciye döndürmek
🤖 geçici hatalar da elenir; yutulabilecek anlık kesintiler kullanıcıya yansır.
## C
Yeniden denemeyi aynen korumak, yalnız zaman aşımı süresini belirgin biçimde kısaltmak
🤖 süreyi kısaltmak kalıcı hatada denemeyi hızlandırır, kök sorunu değiştirmez.
## D
Yalnız geçici hatalarda denemek, işlemi tekrar güvenli kılmak, geri çekilme ve dalgalandırma eklemek
🤖 Doğru: kalıcı hata tekrarlanmaz, tekrar güvenliği çift işlemi engeller.
## Doğru
D

## Açıklama
Bu kurulumda üç ayrı hata birden var. Birincisi **her hatada denemek**. Geçersiz istek ya da yetkisiz erişim gibi kalıcı hatalar tekrarlandığında sonuç değişmez; yalnız yük artar. Yeniden deneme yalnız geçici hatalar için anlamlıdır: zaman aşımı, bağlantı kopması, geçici erişilemezlik (`101`). İkincisi **tekrar güvenliğinin sorgulanmamış olması**. İstek karşı tarafa ulaşıp yanıt dönerken kaybolmuşsa, yeniden deneme aynı işi ikinci kez yaptırır. Ödeme ya da sipariş gibi akışlarda bu doğrudan çift işlem demektir; çözüm istek anahtarıyla tekrar güvenliğidir (`130`, `251`). Üçüncüsü **sabit aralıktır**. Aynı anda birçok istemci sabit aralıkla denerse, yük toparlanmaya çalışan servise dalga hâlinde geri gelir. Üstel geri çekilmeye rastgele bir dalgalandırma eklenir; böylece denemeler zamana yayılır (`153`). A şıkkı yükü büyütür. B şıkkı geçici hataları da kullanıcıya yansıtır. C şıkkı yanlış yerde ayar yapar; zaman aşımı gereklidir ama tek başına bu üç sorunun hiçbirini çözmez.

# sozlu2-4 | İleri
## Soru
Bir ekip dış çağrılara devre kesici koydu, ancak istemci tarafında okuma zaman aşımı tanımlanmadı. Sağlayıcı yanıt vermemeye başlıyor; bağlantılar açık kalıyor ve istekler askıda bekliyor. Bu tablonun en doğru sonucu hangisidir?
## A
Devre kesici kısa sürede açılır ve çağrıları hızla reddeder, zaman aşımı yalnız bir iyileştirmedir
🤖 açılmak için hata sinyali gerekir; askıda bekleyen çağrı hata üretmez.
## B
Devre kesici hata sinyali alamaz, çağrılar askıda kalır ve iş parçacığı havuzu tükenir
🤖 Doğru: zaman aşımı olmadan başarısızlık sayılamaz, zincir ilk halkasından kopar.
## C
Yeniden deneme mekanizması devreye girip durumu telafi eder, eksik ayar görünmez kalır
🤖 askıda kalan çağrı sonuçlanmadığı için yeniden deneme de hiç tetiklenmez.
## D
Bağlantı havuzu boşaldığı anda devre kesici kendiliğinden yarı açık duruma geçer
🤖 yarı açık geçiş bekleme süresiyle olur, havuzun durumuyla ilgisi yoktur.
## Doğru
B

## Açıklama
Bu soru zincirin **sırasını** ölçüyor. Devre kesici, başarısızlık oranına bakarak açılır. Başarısızlığın sayılabilmesi için çağrının bir sonuca ulaşması gerekir. Okuma zaman aşımı tanımlı değilse çağrı ne başarılı olur ne başarısız; sonsuza yakın bekler. Devre kesici bu bekleyişi göremez, sayaç dolmaz, devre kapalı kalır (`101`). Bu arada her istek bir iş parçacığını tutar. Havuz dolduğunda yalnız o entegrasyon değil, **tüm uygulama** yanıt veremez hale gelir. Yavaş bir bağımlılığın sağlıklı bir servisi düşürmesi bu yolla olur. Doğru kurulumda zaman aşımı zincirin ilk halkasıdır: bağlantı zaman aşımı ve okuma zaman aşımı ayrı ayrı tanımlanır. A şıkkı zaman aşımını isteğe bağlı bir iyileştirme sanır. C şıkkı yeniden denemenin askıda çağrıyı kurtaracağını varsayar; oysa deneme ancak çağrı sonuçlanınca tetiklenir. D şıkkı yarı açık duruma geçişi bağlantı havuzuna bağlar; geçiş, açık durumda geçen bekleme süresine bağlıdır.

# sozlu2-5 | İleri
## Soru
Bir hesaplama, ay sonu davranışını belirlemek için metot gövdesinde `LocalDate.now()` çağırıyor. Ekip bu davranışı birim testiyle sınamak istiyor ama testte tarihi sabitleyemiyor. En doğru düzeltme hangisidir?
## A
Saati bir `Clock` bağımlılığı olarak dışarıdan vermek ve testte sabit bir saat geçirmek
🤖 Doğru: sistem sınırı dışarı alınır, tarihi test belirler, üretim kodu sadeleşir.
## B
Testte statik çağrıyı taklit eden bir eklenti kurup `now()` çağrısını yakalayıp değiştirmek
🤖 çalışır ama testi gerçekleme ayrıntısına bağlar, tasarım sorunu yerinde kalır.
## C
Test koşumu sırasında işletim sistemi saatini beklenen tarihe alıp sonra geri almak
🤖 koşumu ortama bağlar; paralel koşan testler birbirinin tarihini bozar.
## D
Ay sonu davranışını birim testinde bırakmayıp yalnız uçtan uca testte doğrulamaya taşımak
🤖 geri bildirimi geciktirir ve tarihi sabitleme sorunu orada da aynen sürer.
## Doğru
A

## Açıklama
Saat bir **sistem sınırıdır**; senin denetiminde değildir. Metot gövdesinde `LocalDate.now()` çağırmak, o sınırı sınıfın içine gömer. Bu yüzden test tarihi belirleyemez ve ay sonu gibi kenar durumlar sınanamaz. Test edilebilir tasarımın somut karşılığı budur: saat, rastgelelik ve kimlik üretimi gibi belirsiz kaynaklar **dışarıdan verilir**. Java'da bunun standart yolu bir `Clock` bağımlılığıdır; üretimde sistem saati, testte sabit saat geçilir (`131`). B şıkkı statik çağrıyı taklit eden bir eklenti önerir. Bu yol çalışır, ama testi kodun iç yapısına bağlar ve asıl sorunu gizler; bu tür araçlar bir tasarım kokusunun işaretidir (`188`). C şıkkı testi makinenin saatine bağımlı kılar; paralel koşumda testler birbirini bozar, koşum ortamı da her yerde aynı olmaz. D şıkkı sorunu yukarı taşır. Uçtan uca testte geri bildirim dakikalara çıkar ve tarihi sabitleme sorunu ortadan kalkmaz (`187`).

# sozlu2-6 | Uzman
## Soru
Ekip, yeniden deneme ve devre kesici ayarlarının gerçekten çalıştığını testle görmek istiyor. Şu anda dış sağlayıcı, servis arayüzü seviyesinde Mockito ile taklit ediliyor ve dayanıklılık davranışı testlerde hiç tetiklenmiyor. En doğru yaklaşım hangisidir?
## A
Dayanıklılık kütüphanesinin kendi sınıflarını taklit edip açılma çağrısının yapıldığını doğrulamak
🤖 kütüphanenin kendi kodu sınanır; senin eşiklerin ve kuralların yine sınanmamış kalır.
## B
Bu davranışı testte hiç sınamamak ve yalnız üretimdeki ölçüm panolarıyla izlemeye bırakmak
🤖 ölçüm gereklidir ama geç kalır; yanlış eşik ancak olay anında görünür hale gelir.
## C
Taklidi HTTP sınırına indirip kontrollü hata ve gecikme üreten sahte bir sunucu kullanmak
🤖 Doğru: sarmalayıcı gerçekten devreye girer, davranış sınırda ve tekrarlanabilir sınanır.
## D
Testlerde taklidi bırakıp doğrudan gerçek sağlayıcının deneme ortamına bağlanarak koşmak
🤖 testler yavaşlar ve kırılganlaşır; hata senaryolarını istediğin anda da üretemezsin.
## Doğru
C

## Açıklama
Sorunun kökü taklidin **yanlış seviyede** kurulmuş olmasıdır. Dayanıklılık sarmalayıcısı genellikle dış çağrıyı yapan istemcinin etrafına konur. Taklit servis arayüzü seviyesinde kurulunca o sarmalayıcı devrenin dışında kalır; test hiçbir zaman gerçek bir hata ya da gecikme görmez. Doğru hamle sınırı aşağı indirmektir: taklit HTTP seviyesinde kurulur ve kontrollü biçimde hata kodu, gecikme ya da bağlantı kopması üretilir (`188`). Böylece zaman aşımı ölçer, yeniden deneme tetiklenir, devre kesicinin eşiği gerçekten sınanır (`101`). A şıkkı kütüphanenin kendi sınıflarını taklit eder; bu, başkasının kodunu test etmektir ve senin ayarların hakkında hiçbir şey söylemez. B şıkkı doğrulamayı üretime erteler; ölçüm gereklidir ama yanlış eşik ancak bir olay sırasında anlaşılır. D şıkkı testi başkasının ortamına bağlar; koşum yavaşlar, sonuçlar kararsızlaşır ve devre kesiciyi açacak hata dizisini istediğin anda üretemezsin (`187`).
